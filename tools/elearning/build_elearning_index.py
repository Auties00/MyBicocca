"""
Builds a JSON search index of all courses published on
https://elearning.unimib.it/ by mirroring the two scraping endpoints from
ElearningCourseApi.kt:

  - getCoursesAreas()             -> top-level areas + categories from the homepage
  - getCourseCategoryContents(c)  -> subcategories + courses for a category page

The script walks the category tree recursively (bounded concurrency) and
emits a single nested JSON tree:

  {
    "fetched_at": "2026-04-27T12:34:56Z",
    "areas": [
      {
        "name": "...",
        "categories": [
          {
            "name": "...",
            "url": "...",
            "categories": [ ... recursive ... ],
            "courses": [
              { "id": ..., "name": "...", "code": "...", "url": "..." }
            ]
          }
        ]
      }
    ]
  }

Requirements:
    pip install httpx beautifulsoup4 lxml

Usage:
    python build_elearning_index.py [--out elearning_index.json] [--concurrency 8]
"""

from __future__ import annotations

import argparse
import asyncio
import json
import random
import sys
from datetime import datetime, timezone
from typing import Any
from urllib.parse import urljoin, urlparse, parse_qs

import httpx
from bs4 import BeautifulSoup, Tag

BASE_URL = "https://elearning.unimib.it/"
USER_AGENT = (
    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
    "(KHTML, like Gecko) Chrome/124.0 Safari/537.36"
)


# ---------- helpers ----------------------------------------------------------

def _text(el: Tag | None) -> str:
    return el.get_text(strip=True) if el is not None else ""


def _abs_href(base: str, el: Tag | None) -> str:
    if el is None:
        return ""
    href = el.get("href", "")
    return urljoin(base, href) if href else ""


def _extract_query_param_int(url: str, *names: str) -> int | None:
    qs = parse_qs(urlparse(url).query)
    for name in names:
        vals = qs.get(name)
        if vals:
            try:
                return int(vals[0])
            except ValueError:
                continue
    return None


# ---------- scrapers (mirror of ElearningCourseApi.kt) ----------------------

async def get_courses_areas(client: httpx.AsyncClient) -> list[dict[str, Any]]:
    """Mirror of ElearningCourseApi.getCoursesAreas()."""
    resp = await client.get(BASE_URL)
    resp.raise_for_status()
    doc = BeautifulSoup(resp.text, "lxml")
    base = str(resp.url)

    areas: list[dict[str, Any]] = []
    for section in doc.select("div.frontpage-box"):
        section_name = _text(section.select_one("h2.navigation-title")) or "Unknown Section"
        categories: list[dict[str, str]] = []
        for item in section.select("div.card .navigation-block"):
            link = item.select_one("a.card-img-overlay")
            if link is None:
                continue
            cat_name = _text(link.select_one("h5.card-title")) or "Unknown Category"
            cat_url = _abs_href(base, link)
            if cat_url:
                categories.append({"name": cat_name, "url": cat_url})
        areas.append({"name": section_name, "categories": categories})
    return areas


async def get_course_category_contents(
    client: httpx.AsyncClient, category_url: str
) -> dict[str, Any]:
    """Mirror of ElearningCourseApi.getCourseCategoryContents()."""
    resp = await client.get(category_url)
    resp.raise_for_status()
    doc = BeautifulSoup(resp.text, "lxml")
    base = str(resp.url)

    name = _text(doc.select_one("h1")) or "Unknown Category"

    subcategories: list[dict[str, str]] = []
    for link in doc.select("div.subcategories .category > a.info"):
        name_tag = link.select_one("h3.categoryname")
        if name_tag is not None:
            for sr in name_tag.select(".sr-only"):
                sr.decompose()
            sub_name = name_tag.get_text(strip=True) or "Unknown Category"
        else:
            sub_name = "Unknown Category"
        sub_url = _abs_href(base, link)
        if sub_url:
            subcategories.append({"name": sub_name, "url": sub_url})

    # hasPassword is intentionally omitted: the listing page has no enrolment
    # info per course. To populate it correctly each course would need its
    # /enrol/index.php?id=<id> page fetched (#fitem_id_nokey present == no key).
    courses: list[dict[str, Any]] = []
    for box in doc.select("div.coursebox"):
        link = box.select_one("a.coursename")
        if link is None:
            continue
        course_url = _abs_href(base, link)

        title_div = box.select_one(".course-fullname")
        if title_div is not None:
            title = title_div.get_text(strip=True)
        elif link.get("title", "").strip():
            title = link["title"].strip()
        else:
            title = link.get_text(strip=True)

        code = _text(box.select_one(".course-shortname"))

        course_id_attr = (box.get("data-courseid") or "").strip()
        course_id: int | None
        if course_id_attr:
            try:
                course_id = int(course_id_attr)
            except ValueError:
                course_id = _extract_query_param_int(course_url, "id")
        else:
            course_id = _extract_query_param_int(course_url, "id")

        if course_id is None:
            continue

        courses.append(
            {
                "id": course_id,
                "name": title,
                "code": code,
                "url": course_url,
            }
        )

    return {"name": name, "subcategories": subcategories, "courses": courses}


# ---------- crawler ----------------------------------------------------------

class Crawler:
    def __init__(
        self,
        client: httpx.AsyncClient,
        concurrency: int,
        max_retries: int = 4,
        base_backoff: float = 1.5,
    ) -> None:
        self.client = client
        self.sem = asyncio.Semaphore(concurrency)
        self.visited: set[str] = set()
        self.failed: list[str] = []
        self.max_retries = max_retries
        self.base_backoff = base_backoff

    async def _fetch_category(self, url: str) -> dict[str, Any]:
        last_err: Exception | None = None
        for attempt in range(1, self.max_retries + 1):
            async with self.sem:
                try:
                    return await get_course_category_contents(self.client, url)
                except Exception as e:  # noqa: BLE001 - retry then give up
                    last_err = e
            # exponential backoff with jitter; released the semaphore first so
            # other workers can proceed while this one sleeps
            if attempt < self.max_retries:
                delay = self.base_backoff * (2 ** (attempt - 1))
                delay += random.uniform(0, delay * 0.25)
                await asyncio.sleep(delay)
        self.failed.append(url)
        print(
            f"  ! failed after {self.max_retries} attempts {url}: {last_err}",
            file=sys.stderr,
        )
        return {"name": "", "subcategories": [], "courses": []}

    async def build_category(self, name: str, url: str) -> dict[str, Any]:
        """Fetch a category and recursively build its nested tree node."""
        if url in self.visited:
            return {"name": name, "url": url, "categories": [], "courses": []}
        self.visited.add(url)

        contents = await self._fetch_category(url)
        resolved_name = contents["name"] or name

        children = await asyncio.gather(
            *(self.build_category(sub["name"], sub["url"]) for sub in contents["subcategories"])
        )

        return {
            "name": resolved_name,
            "url": url,
            "categories": list(children),
            "courses": contents["courses"],
        }


# ---------- entry point ------------------------------------------------------

async def build_index(out_path: str, concurrency: int) -> None:
    timeout = httpx.Timeout(30.0, connect=15.0)
    limits = httpx.Limits(max_connections=concurrency * 2, max_keepalive_connections=concurrency)
    headers = {"User-Agent": USER_AGENT, "Accept-Language": "it-IT,it;q=0.9,en;q=0.8"}

    async with httpx.AsyncClient(
        headers=headers, timeout=timeout, limits=limits, follow_redirects=True
    ) as client:
        print(f"[1/2] Fetching course areas from {BASE_URL} ...")
        top_areas = await get_courses_areas(client)
        total_top = sum(len(a["categories"]) for a in top_areas)
        print(f"      found {len(top_areas)} areas, {total_top} top-level categories")

        crawler = Crawler(client, concurrency)
        print(f"[2/2] Walking category tree (concurrency={concurrency}) ...")

        areas: list[dict[str, Any]] = []
        for area in top_areas:
            categories = await asyncio.gather(
                *(crawler.build_category(cat["name"], cat["url"]) for cat in area["categories"])
            )
            areas.append({"name": area["name"], "categories": list(categories)})

    payload = {
        "fetched_at": datetime.now(timezone.utc).strftime("%Y-%m-%dT%H:%M:%SZ"),
        "areas": areas,
    }
    with open(out_path, "w", encoding="utf-8") as f:
        json.dump(payload, f, ensure_ascii=False, indent=2)

    total_categories, total_courses = _count(areas)
    print(
        f"Done: {len(areas)} areas, {total_categories} categories, "
        f"{total_courses} courses (with duplicates if cross-listed) -> {out_path}"
    )
    if crawler.failed:
        print(
            f"  ! {len(crawler.failed)} categories failed after retries:",
            file=sys.stderr,
        )
        for u in crawler.failed:
            print(f"    {u}", file=sys.stderr)


def _count(areas: list[dict[str, Any]]) -> tuple[int, int]:
    cats = 0
    courses = 0

    def walk(node: dict[str, Any]) -> None:
        nonlocal cats, courses
        cats += 1
        courses += len(node.get("courses", []))
        for child in node.get("categories", []):
            walk(child)

    for area in areas:
        for cat in area["categories"]:
            walk(cat)
    return cats, courses


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--out", default="elearning_index.json", help="output JSON path")
    parser.add_argument(
        "--concurrency", type=int, default=4, help="max parallel category requests"
    )
    args = parser.parse_args()
    asyncio.run(build_index(args.out, args.concurrency))


if __name__ == "__main__":
    main()
