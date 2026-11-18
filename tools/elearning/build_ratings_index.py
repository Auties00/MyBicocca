"""
Builds a flat JSON ratings index of student opinions published on
https://opinionistudenti.unimib.it/valdid/ keyed by academic year and then
by course (insegnamento). Department and study-course tiers exist only as
crawl scaffolding and are not present in the output.

For every leaf teacher the site publishes three KPIs:

  - overall_satisfaction      (Soddisfazione Complessiva)
  - teaching_effectiveness    (Efficacia Didattica)
  - organizational_aspects    (Aspetti organizzativi)

All values are normalized to the 1-10 scale used from AA 2019/2020 onward;
pre-2019 ratings, originally on a 0-3 scale, are rescaled via
new = 1 + 3*old (this maps the band boundaries 1/2 to 4/7 exactly).

A course's "ratings" is a questionnaire-weighted average of its teachers'
ratings; it is null when no teacher on that course has published data.

Teachers whose data is unpublished (Pubblicazione = NO) are kept in the
index with "ratings": null; "pubblicato" is therefore derivable from
("ratings" != null).

If the script encounters a KPI label outside the known set, or if a
teacher's chart is missing any of the three known KPIs, it aborts.

Output shape:

  {
    "fetched_at": "2026-05-20T12:34:56Z",
    "academic_years": {
      "2024": {
        "id": 2024,
        "courses": {
          "E0201Q005": {
            "id": "E0201Q005",
            "url": "...",
            "questionnaires": 222,
            "ratings": { "overall_satisfaction": ..., "teaching_effectiveness": ..., "organizational_aspects": ... },
            "teachers": [
              { "id": "001625", "url": "...", "questionnaires": 130,
                "ratings": { ... } }
            ]
          }
        }
      }
    }
  }

Requirements:
    pip install httpx beautifulsoup4 lxml

Usage:
    python build_ratings_index.py [--out ratings_index.json] [--concurrency 4]
                                  [--from-year 2013] [--to-year 2024]
"""

from __future__ import annotations

import argparse
import asyncio
import datetime as _dt
import json
import random
import re
import sys
from typing import Any
from urllib.parse import parse_qs, urljoin, urlparse

import httpx
from bs4 import BeautifulSoup, Tag

BASE_URL = "https://opinionistudenti.unimib.it/valdid/"

# Match real Chrome headers. The backend sits behind an Azure Application
# Gateway that uses sticky-session cookies (ApplicationGatewayAffinity*)
# plus a JBoss/Tomcat JSESSIONID. Without those cookies our requests
# round-robin across backend pods with varying load/cache, and some pods
# return a stripped page (200 OK with the template but no dynamic data).
# We send the same headers Chrome does and let httpx.AsyncClient's cookie
# jar accumulate session cookies from the first response.
CHROME_HEADERS: dict[str, str] = {
    "User-Agent": (
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
        "(KHTML, like Gecko) Chrome/148.0.0.0 Safari/537.36"
    ),
    "Accept": (
        "text/html,application/xhtml+xml,application/xml;q=0.9,"
        "image/avif,image/webp,image/apng,*/*;q=0.8,"
        "application/signed-exchange;v=b3;q=0.7"
    ),
    "Accept-Language": "it-IT,it;q=0.9,en-US;q=0.8,en;q=0.7",
    "Accept-Encoding": "gzip, deflate, br",
    "Connection": "keep-alive",
    "Upgrade-Insecure-Requests": "1",
    "Sec-Ch-Ua": (
        '"Chromium";v="148", "Google Chrome";v="148", "Not/A)Brand";v="99"'
    ),
    "Sec-Ch-Ua-Mobile": "?0",
    "Sec-Ch-Ua-Platform": '"Windows"',
    "Sec-Fetch-Dest": "document",
    "Sec-Fetch-Mode": "navigate",
    "Sec-Fetch-Site": "none",
    "Sec-Fetch-User": "?1",
}

# Italian KPI labels from the chart -> English JSON keys.
KPI_LABELS: dict[str, str] = {
    "Soddisfazione Complessiva": "overall_satisfaction",
    "Efficacia Didattica": "teaching_effectiveness",
    "Aspetti organizzativi": "organizational_aspects",
}
KPI_KEYS: frozenset[str] = frozenset(KPI_LABELS.values())

# AAs up to and including 2018 use the 0-3 scale; from 2019 onward, 1-10.
LEGACY_SCALE_LAST_YEAR = 2018


# ---------- parsing helpers --------------------------------------------------

def _text(el: Tag | None) -> str:
    return el.get_text(strip=True) if el is not None else ""


def _abs_href(base: str, value: str | None) -> str:
    return urljoin(base, value) if value else ""


def _parse_int(text: str) -> int | None:
    cleaned = text.replace(",", "").replace(".", "").strip()
    if not cleaned:
        return None
    try:
        return int(cleaned)
    except ValueError:
        return None


def _query_param(url: str, name: str) -> str | None:
    vals = parse_qs(urlparse(url).query).get(name)
    return vals[0] if vals else None


def _normalize_scale(value: float, year: int) -> float:
    if year <= LEGACY_SCALE_LAST_YEAR:
        # linear map [0, 3] -> [1, 10]; 0 -> 1, 3 -> 10.
        return 1.0 + 3.0 * value
    return value


_PAIR_RE = re.compile(
    r"\[\s*['\"]([^'\"]+)['\"]\s*,\s*([0-9.]+)\s*\]"
    r"|"
    r"\[\s*([0-9.]+)\s*,\s*['\"]([^'\"]+)['\"]\s*\]"
)


def _parse_chart_series(html: str) -> list[list[tuple[str, float]]]:
    """Parse $.jqplot('chart', [ [pairs], [pairs], ... ], opts).

    Returns one list of (label, value) pairs per series. Only the first
    $.jqplot('chart', ...) call is parsed; the page repeats the same data
    in horizontal/vertical/resize branches.
    """
    marker = "$.jqplot('chart'"
    idx = html.find(marker)
    if idx < 0:
        return []
    start = html.find("[", idx + len(marker))
    if start < 0:
        return []
    depth = 0
    end = -1
    for i in range(start, len(html)):
        c = html[i]
        if c == "[":
            depth += 1
        elif c == "]":
            depth -= 1
            if depth == 0:
                end = i
                break
    if end < 0:
        return []
    inner = html[start + 1 : end]

    series_blocks: list[str] = []
    depth = 0
    buf: list[str] = []
    for c in inner:
        if c == "[":
            depth += 1
            buf.append(c)
        elif c == "]":
            depth -= 1
            buf.append(c)
            if depth == 0:
                series_blocks.append("".join(buf))
                buf = []
        elif depth > 0:
            buf.append(c)

    out: list[list[tuple[str, float]]] = []
    for blk in series_blocks:
        pairs: list[tuple[str, float]] = []
        for m in _PAIR_RE.finditer(blk):
            if m.group(1) is not None:
                label = m.group(1)
                value = float(m.group(2))
            else:
                label = m.group(4)
                value = float(m.group(3))
            pairs.append((label, value))
        out.append(pairs)
    return out


def _series_to_ratings(
    pairs: list[tuple[str, float]],
    year: int,
    where: str,
) -> dict[str, float]:
    if not pairs:
        raise ValueError(f"no rating pairs found in chart at {where}")
    seen: dict[str, float] = {}
    for label, value in pairs:
        key = KPI_LABELS.get(label)
        if key is None:
            raise ValueError(
                f"unknown rating category {label!r} at {where} "
                f"(known: {sorted(KPI_LABELS)})"
            )
        if key in seen:
            raise ValueError(f"duplicate KPI {label!r} at {where}")
        seen[key] = _normalize_scale(value, year)
    missing = KPI_KEYS - seen.keys()
    if missing:
        raise ValueError(
            f"missing rating categories {sorted(missing)} at {where} "
            f"(got only {sorted(seen)})"
        )
    return seen


def _weighted_avg_ratings(
    teachers: list[dict[str, Any]],
) -> dict[str, float] | None:
    totals: dict[str, float] = {k: 0.0 for k in KPI_KEYS}
    weight_sum = 0
    for t in teachers:
        ratings = t.get("ratings")
        if ratings is None:
            continue
        w = t.get("questionnaires") or 0
        if w <= 0:
            continue
        weight_sum += w
        for k in KPI_KEYS:
            totals[k] += float(ratings[k]) * w
    if weight_sum == 0:
        return None
    return {k: round(totals[k] / weight_sum, 6) for k in KPI_KEYS}


def _header_field(jumbotron_text: str, label: str) -> str | None:
    for line in jumbotron_text.splitlines():
        line = line.strip()
        if line.startswith(label):
            _, _, rest = line.partition(":")
            v = rest.strip()
            return v or None
    return None


# ---------- crawler ----------------------------------------------------------

def _log(msg: str) -> None:
    print(
        f"[{_dt.datetime.now().strftime('%H:%M:%S')}] {msg}",
        flush=True,
    )


class _LIFOSemaphore:
    """Semaphore that wakes the most-recently-queued waiter first.

    asyncio.Semaphore is strictly FIFO, which combined with the recursive
    gather pattern produces breadth-first traversal: all the dept index
    fetches drain before any CDS fetch starts, all CDS fetches drain before
    any course fetch starts, etc. Parents can never complete because their
    descendants are stuck at the back of the queue.

    LIFO wakes the deepest pending fetch first, which gives depth-first
    drainage: each course finishes promptly with its teachers, parents
    close out, and the in-flight coroutine count stays bounded by
    (concurrency * tree_depth) instead of (total_leaves).
    """

    def __init__(self, value: int) -> None:
        if value < 0:
            raise ValueError("value must be >= 0")
        self._value = value
        self._waiters: list[asyncio.Future[None]] = []

    async def acquire(self) -> None:
        if self._value > 0:
            self._value -= 1
            return
        loop = asyncio.get_running_loop()
        fut: asyncio.Future[None] = loop.create_future()
        self._waiters.append(fut)
        try:
            await fut
        except BaseException:
            try:
                self._waiters.remove(fut)
            except ValueError:
                pass
            raise

    def release(self) -> None:
        while self._waiters:
            fut = self._waiters.pop()
            if not fut.done():
                fut.set_result(None)
                return
        self._value += 1

    async def __aenter__(self) -> "_LIFOSemaphore":
        await self.acquire()
        return self

    async def __aexit__(self, *exc: Any) -> None:
        self.release()


class Crawler:
    def __init__(
        self,
        concurrency: int,
        max_retries: int = 4,
        base_backoff: float = 1.5,
        timeout_secs: float = 30.0,
    ) -> None:
        # We deliberately do NOT keep a long-lived AsyncClient. The valdid
        # backend imposes a per-session rate limit: once a JSESSIONID has
        # successfully served one "heavy" page, the next requests on that
        # session get a 200 OK with the template stripped of dynamic data
        # (no Numero di questionari, no chart, ~22KB instead of ~55KB).
        # Empirically this affects ALL request rates — even truly serial
        # ones with delays. The only stable workaround is a fresh
        # AsyncClient (=> fresh JSESSIONID) per request.
        self.sem = _LIFOSemaphore(concurrency)
        self.max_retries = max_retries
        self.base_backoff = base_backoff
        self.timeout_secs = timeout_secs
        self.failed: list[str] = []
        # progress counters per year
        self._depts_done: dict[int, int] = {}
        self._depts_total: dict[int, int] = {}
        self._cds_done: dict[int, int] = {}
        self._cds_total: dict[int, int] = {}
        self._courses_done: dict[int, int] = {}
        self._courses_total: dict[int, int] = {}
        self._teachers_done: dict[int, int] = {}
        self._teachers_total: dict[int, int] = {}
        # heartbeat
        self._inflight = 0
        self._fetch_count = 0

    def _bump(
        self, bucket: dict[int, int], year: int, delta: int = 1
    ) -> int:
        v = bucket.get(year, 0) + delta
        bucket[year] = v
        return v

    async def _get(self, url: str, *, must_contain: str | None = None) -> str:
        """Fetch with retry, using a fresh AsyncClient (=> fresh
        JSESSIONID) per attempt to bypass the per-session lock. If
        must_contain is set, the response must include that substring or
        the fetch is treated as a failure and retried."""
        attempt_errs: list[str] = []
        for attempt in range(1, self.max_retries + 1):
            async with self.sem:
                self._inflight += 1
                try:
                    async with httpx.AsyncClient(
                        headers=CHROME_HEADERS,
                        timeout=self.timeout_secs,
                        follow_redirects=True,
                    ) as client:
                        resp = await client.get(url)
                        resp.raise_for_status()
                        text = resp.text
                        if must_contain is not None and must_contain not in text:
                            raise RuntimeError(
                                f"sentinel {must_contain!r} missing ({len(text)}B)"
                            )
                        self._fetch_count += 1
                        return text
                except Exception as e:  # noqa: BLE001 - retry then give up
                    attempt_errs.append(f"{type(e).__name__}: {e}")
                finally:
                    self._inflight -= 1
            if attempt < self.max_retries:
                delay = self.base_backoff * (2 ** (attempt - 1))
                delay += random.uniform(0, delay * 0.25)
                await asyncio.sleep(delay)
        self.failed.append(url)
        raise RuntimeError(
            f"failed after {self.max_retries} attempts: {url}\n"
            + "\n".join(f"    [{i + 1}] {e}" for i, e in enumerate(attempt_errs))
        )

    async def heartbeat(self, interval: float = 15.0) -> None:
        """Periodic progress log; cancel to stop."""
        prev_fetches = 0
        while True:
            await asyncio.sleep(interval)
            year_parts = []
            for year in sorted(self._teachers_total.keys() | self._courses_total.keys()):
                year_parts.append(
                    f"{year}:{self._teachers_done.get(year, 0)}/"
                    f"{self._teachers_total.get(year, 0)}t,"
                    f"{self._courses_done.get(year, 0)}/"
                    f"{self._courses_total.get(year, 0)}c"
                )
            delta = self._fetch_count - prev_fetches
            prev_fetches = self._fetch_count
            _log(
                f"HEARTBEAT inflight={self._inflight} fetched={self._fetch_count} "
                f"(+{delta} in {interval:.0f}s) — "
                + (" | ".join(year_parts) if year_parts else "no leaf progress yet")
            )

    async def fetch_year(self, year: int) -> dict[str, Any] | None:
        url = f"{BASE_URL}opinioniAA.vm?idAA={year}"
        _log(f"AA {year}: fetching index")
        try:
            html = await self._get(url)
        except RuntimeError as e:
            print(f"  ! {e}", file=sys.stderr)
            return None
        doc = BeautifulSoup(html, "lxml")
        rows = doc.select("table tbody tr")
        if not rows:
            _log(f"AA {year}: no departments published, skipping")
            return None
        dept_urls = [
            _abs_href(url, tr.get("data-href"))
            for tr in rows
            if tr.get("data-href")
        ]
        self._depts_total[year] = len(dept_urls)
        _log(f"AA {year}: {len(dept_urls)} departments")
        dept_courses = await asyncio.gather(
            *(self._walk_department(year, u) for u in dept_urls)
        )
        merged: dict[str, dict[str, Any]] = {}
        for course_list in dept_courses:
            for course in course_list:
                existing = merged.get(course["id"])
                if existing is not None:
                    raise ValueError(
                        f"duplicate insegnamento {course['id']!r} in AA {year}: "
                        f"seen at {existing['url']} and {course['url']}"
                    )
                merged[course["id"]] = course
        # sort keys for deterministic output
        ordered = {k: merged[k] for k in sorted(merged)}
        _log(
            f"AA {year}: done — {len(ordered)} courses, "
            f"{self._teachers_done.get(year, 0)} teachers"
        )
        return {"id": year, "courses": ordered}

    async def _walk_department(self, year: int, url: str) -> list[dict[str, Any]]:
        dip_id = _query_param(url, "idDIP") or "?"
        try:
            html = await self._get(url, must_contain="opinioniCDS.vm")
        except RuntimeError as e:
            print(f"  ! {e}", file=sys.stderr)
            return []
        doc = BeautifulSoup(html, "lxml")
        cds_urls = [
            _abs_href(url, tr.get("data-href"))
            for tr in doc.select("table tbody tr")
            if tr.get("data-href")
        ]
        self._bump(self._cds_total, year, len(cds_urls))
        nested = await asyncio.gather(
            *(self._walk_study_course(year, u) for u in cds_urls)
        )
        done = self._bump(self._depts_done, year)
        total = self._depts_total.get(year, 0)
        _log(f"AA {year}: dept {dip_id} done ({done}/{total} depts)")
        return [c for sub in nested for c in sub]

    async def _walk_study_course(self, year: int, url: str) -> list[dict[str, Any]]:
        try:
            html = await self._get(url, must_contain="opinioniAD.vm")
        except RuntimeError as e:
            print(f"  ! {e}", file=sys.stderr)
            return []
        doc = BeautifulSoup(html, "lxml")
        course_urls = [
            _abs_href(url, tr.get("data-href"))
            for tr in doc.select("table tbody tr")
            if tr.get("data-href")
        ]
        self._bump(self._courses_total, year, len(course_urls))
        courses = await asyncio.gather(
            *(self.fetch_course(year, u) for u in course_urls)
        )
        self._bump(self._cds_done, year)
        return [c for c in courses if c is not None]

    async def fetch_course(
        self, year: int, url: str
    ) -> dict[str, Any] | None:
        ad_id = _query_param(url, "idAD")
        if not ad_id:
            return None
        try:
            html = await self._get(url, must_contain="opinioniDOC.vm")
        except RuntimeError as e:
            print(f"  ! {e}", file=sys.stderr)
            return None
        doc = BeautifulSoup(html, "lxml")
        jumbo = _text(doc.select_one(".jumbotron"))
        questionnaires = _parse_int(_header_field(jumbo, "Numero di questionari") or "")

        teacher_stubs: list[dict[str, Any]] = []
        for tr in doc.select("table tbody tr"):
            href = tr.get("data-href")
            if not href:
                continue
            doc_id = _query_param(href, "idDOC")
            if not doc_id:
                continue
            cells = [c.get_text(strip=True) for c in tr.find_all("td")]
            # cells: [course_code, course_name, teacher_name, published, reason, count]
            published = (cells[3].upper() == "SI") if len(cells) > 3 else False
            q = _parse_int(cells[5]) if len(cells) > 5 else None
            teacher_stubs.append(
                {
                    "id": doc_id,
                    "url": _abs_href(url, href),
                    "questionnaires": q,
                    "_published": published,
                }
            )

        self._bump(self._teachers_total, year, len(teacher_stubs))
        teachers = await asyncio.gather(
            *(self.fetch_teacher(year, t) for t in teacher_stubs)
        )
        teacher_objs = [t for t in teachers if t is not None]
        done = self._bump(self._courses_done, year)
        total = self._courses_total.get(year, 0)
        if done % 50 == 0 or done == total:
            _log(
                f"AA {year}: progress — {done}/{total} courses, "
                f"{self._teachers_done.get(year, 0)}/"
                f"{self._teachers_total.get(year, 0)} teachers"
            )
        return {
            "id": ad_id,
            "url": url,
            "questionnaires": questionnaires,
            "ratings": _weighted_avg_ratings(teacher_objs),
            "teachers": teacher_objs,
        }

    async def fetch_teacher(
        self, year: int, stub: dict[str, Any]
    ) -> dict[str, Any] | None:
        if not stub.get("_published"):
            self._bump(self._teachers_done, year)
            return {
                "id": stub["id"],
                "url": stub["url"],
                "questionnaires": stub.get("questionnaires"),
                "ratings": None,
            }
        url = stub["url"]
        try:
            html = await self._get(url, must_contain="jqplot(")
        except RuntimeError as e:
            # Some "SI"-marked teachers genuinely have no chart in the
            # public site (the AD page is inconsistent with the DOC page
            # — Chrome shows the same stripped layout). We can't tell
            # this apart from a flaky-server stripped page after retries
            # exhaust, so treat both as unpublished with a warning.
            print(
                f"  ! {e}\n  ! treating DOC {stub['id']} (AA {year}) as unpublished",
                file=sys.stderr,
            )
            self._bump(self._teachers_done, year)
            return {
                "id": stub["id"],
                "url": url,
                "questionnaires": stub.get("questionnaires"),
                "ratings": None,
            }
        doc = BeautifulSoup(html, "lxml")
        jumbo = _text(doc.select_one(".jumbotron"))
        questionnaires = (
            _parse_int(_header_field(jumbo, "Numero di questionari") or "")
            or stub.get("questionnaires")
        )

        series = _parse_chart_series(html)
        # On the DOC page the chart has 5 series:
        # [Ateneo, Dipartimento, Tipo Corso, Corso di Studio, Docente].
        # The teacher's own scores are always the last series.
        if not series:
            raise ValueError(f"no chart series on DOC page: {url}")
        teacher_pairs = series[-1]
        ratings = _series_to_ratings(
            teacher_pairs, year, f"DOC {stub['id']} / AA {year}"
        )
        self._bump(self._teachers_done, year)
        return {
            "id": stub["id"],
            "url": url,
            "questionnaires": questionnaires,
            "ratings": ratings,
        }


# ---------- entry point ------------------------------------------------------

async def build_index(
    out_path: str, concurrency: int, from_year: int, to_year: int
) -> None:
    crawler = Crawler(concurrency)
    years = list(range(from_year, to_year + 1))
    print(
        f"Crawling academic years {from_year}..{to_year} "
        f"(concurrency={concurrency}) ..."
    )
    hb = asyncio.create_task(crawler.heartbeat())
    try:
        results = await asyncio.gather(*(crawler.fetch_year(y) for y in years))
    finally:
        hb.cancel()
        try:
            await hb
        except asyncio.CancelledError:
            pass

    academic_years: dict[str, Any] = {}
    for entry in results:
        if entry is None:
            continue
        academic_years[str(entry["id"])] = entry

    payload = {
        "fetched_at": _dt.datetime.now(_dt.timezone.utc).strftime("%Y-%m-%dT%H:%M:%SZ"),
        "academic_years": academic_years,
    }
    with open(out_path, "w", encoding="utf-8") as f:
        json.dump(payload, f, ensure_ascii=False, indent=2)

    total_courses, total_teachers = _count(academic_years)
    print(
        f"Done: {len(academic_years)} academic years, "
        f"{total_courses} courses, {total_teachers} teachers -> {out_path}"
    )
    if crawler.failed:
        print(f"  ! {len(crawler.failed)} fetches failed after retries:", file=sys.stderr)
        for u in crawler.failed:
            print(f"    {u}", file=sys.stderr)


def _count(academic_years: dict[str, Any]) -> tuple[int, int]:
    courses = teachers = 0
    for year in academic_years.values():
        for course in year.get("courses", {}).values():
            courses += 1
            teachers += len(course.get("teachers", []))
    return courses, teachers


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument(
        "--out", default="ratings_index.json", help="output JSON path"
    )
    parser.add_argument(
        "--concurrency", type=int, default=4, help="max parallel requests"
    )
    current_year = _dt.date.today().year
    parser.add_argument(
        "--from-year",
        dest="from_year",
        type=int,
        default=2013,
        help="first idAA to crawl (inclusive)",
    )
    parser.add_argument(
        "--to-year",
        dest="to_year",
        type=int,
        default=current_year,
        help="last idAA to crawl (inclusive)",
    )
    args = parser.parse_args()
    if args.to_year < args.from_year:
        parser.error("--to-year must be >= --from-year")
    asyncio.run(
        build_index(args.out, args.concurrency, args.from_year, args.to_year)
    )


if __name__ == "__main__":
    main()
