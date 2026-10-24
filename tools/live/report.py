#!/usr/bin/env python3
"""Summarizes the reports of the live tests as Markdown.

Every platform's live test runtime writes one JSON object per line to
`<platform>-<api>.jsonl` (see "Live tests" in common/codegen/README.md). This script reads the
reports of every platform under a directory and prints, per API:

- how many cases passed, failed and were skipped on each platform;
- the failures, with their reasons;
- the cases whose results the platforms decoded differently (their fingerprints differ);
- what responses did that the spec doesn't say: fields it doesn't know, values of another type,
  enum values it doesn't list;
- the fields no response filled, which nothing has confirmed yet;
- the changes earlier runs left in the journal.

It exits with status 1 when a case failed or the platforms disagree, so that CI fails.

Usage: report.py <reports directory>
"""

import json
import sys
from collections import defaultdict
from pathlib import Path

PLATFORMS = ["kotlin", "typescript", "swift"]
MAX_ROWS = 40


def read(directory: Path):
    """The events of every report under `directory`, by API and then platform."""
    apis = defaultdict(lambda: defaultdict(list))
    for file in sorted(directory.rglob("*.jsonl")):
        for line in file.read_text(encoding="utf-8").splitlines():
            if line.strip():
                event = json.loads(line)
                apis[event["api"]][event["platform"]].append(event)
    return apis


def cases(events):
    """The last outcome of each case: a case run twice keeps its second run."""
    return {event["id"]: event for event in events if event["type"] == "case"}


def table(header, rows):
    if not rows:
        return []
    lines = ["| " + " | ".join(header) + " |", "|" + "|".join("---" for _ in header) + "|"]
    for row in rows[:MAX_ROWS]:
        lines.append("| " + " | ".join(str(cell).replace("|", "\\|").replace("\n", " ") for cell in row) + " |")
    if len(rows) > MAX_ROWS:
        lines.append(f"\n…and {len(rows) - MAX_ROWS} more.")
    return lines


def summarize(api, platforms):
    lines = [f"## {api}", ""]
    outcomes = {platform: cases(events) for platform, events in platforms.items()}
    present = [platform for platform in PLATFORMS if platform in platforms] + sorted(set(platforms) - set(PLATFORMS))

    counts = []
    for platform in present:
        statuses = [case["status"] for case in outcomes[platform].values()]
        seed = next((event.get("seed") for event in platforms[platform] if event["type"] == "run"), "")
        counts.append([platform, statuses.count("passed"), statuses.count("failed"), statuses.count("skipped"), seed])
    lines += table(["Platform", "Passed", "Failed", "Skipped", "Seed"], counts) + [""]

    failures = [
        [platform, case_id, case.get("reason", "")[:300]]
        for platform in present
        for case_id, case in sorted(outcomes[platform].items())
        if case["status"] == "failed"
    ]
    if failures:
        lines += ["### Failures", ""] + table(["Platform", "Case", "Reason"], failures) + [""]

    fingerprints = defaultdict(dict)
    for platform in present:
        for case_id, case in outcomes[platform].items():
            if case.get("fingerprint"):
                fingerprints[case_id][platform] = case["fingerprint"]
    mismatches = [
        [case_id] + [by_platform.get(platform, "") for platform in present]
        for case_id, by_platform in sorted(fingerprints.items())
        if len(by_platform) > 1 and len(set(by_platform.values())) > 1
    ]
    compared = sum(1 for by_platform in fingerprints.values() if len(by_platform) > 1)
    lines.append(f"Compared {compared} results across platforms: {len(mismatches)} decoded differently.")
    lines.append("")
    if mismatches:
        lines += ["### Decoded differently", ""] + table(["Case"] + present, mismatches) + [""]

    findings = defaultdict(set)
    details = {}
    for platform in present:
        for event in platforms[platform]:
            if event["type"] == "finding":
                key = (event["kind"], event["record"], event["field"])
                findings[key].add(platform)
                details[key] = event["detail"]
    if findings:
        rows = [
            [kind, f"{record}.{field}".strip("."), details[(kind, record, field)], ", ".join(sorted(seen))]
            for (kind, record, field), seen in sorted(findings.items())
        ]
        lines += ["### What responses did that the spec doesn't say", ""] + table(["Finding", "Where", "Detail", "Seen by"], rows) + [""]

    declared = set()
    seen = set()
    for events in platforms.values():
        for event in events:
            if event["type"] == "declared":
                declared.update(event["fields"])
            elif event["type"] == "seen":
                seen.add(f"{event['record']}.{event['field']}")
    if declared:
        unseen = sorted(declared - seen)
        lines.append(f"Responses filled {len(declared & seen)} of the {len(declared)} fields the spec declares for the results the tests read.")
        if unseen:
            lines += ["", "<details><summary>Fields no response filled</summary>", "", ", ".join(f"`{field}`" for field in unseen), "", "</details>"]
        lines.append("")

    journal = [[platform, event["id"], event["detail"]] for platform in present for event in platforms[platform] if event["type"] == "journal"]
    if journal:
        lines += ["### Changes earlier runs left", ""] + table(["Platform", "Case", "Outcome"], journal) + [""]

    skips = {}
    for events in platforms.values():
        for event in events:
            if event["type"] == "skip":
                skips[event["id"]] = event["reason"]
    if skips:
        by_reason = defaultdict(list)
        for operation, reason in skips.items():
            by_reason[reason].append(operation)
        rows = [[len(operations), reason] for reason, operations in sorted(by_reason.items(), key=lambda item: -len(item[1]))]
        lines += [f"<details><summary>{len(skips)} operations no case calls</summary>", ""] + table(["Operations", "Reason"], rows) + ["", "</details>", ""]

    return lines, bool(failures) or bool(mismatches)


def main():
    if len(sys.argv) != 2:
        sys.exit(__doc__)
    apis = read(Path(sys.argv[1]))
    if not apis:
        print("# Live tests\n\nNo reports found.")
        return 0
    lines = ["# Live tests", ""]
    failed = False
    for api in sorted(apis):
        api_lines, api_failed = summarize(api, apis[api])
        lines += api_lines
        failed = failed or api_failed
    print("\n".join(lines))
    return 1 if failed else 0


if __name__ == "__main__":
    sys.exit(main())
