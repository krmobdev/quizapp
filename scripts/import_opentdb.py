#!/usr/bin/env python3
"""
Merge multiple-choice questions from a bundled Open Trivia Database dump into
English question banks under app/src/main/assets/.

Local (non-otdb_) questions are kept; previous otdb_* entries are replaced.
License: CC BY-SA 4.0 — https://opentdb.com/
"""
from __future__ import annotations

import html
import json
import os
import re
import sys

ASSETS = os.path.join(os.path.dirname(__file__), "..", "app", "src", "main", "assets")
DUMP = os.path.join(os.path.dirname(__file__), "opentdb_dump.json")

# OTDB category name -> app category ids (science & nature handled separately)
DIRECT_MAP: dict[str, list[str]] = {
    "Geography": ["geography"],
    "History": ["history"],
    "Entertainment: Film": ["movies"],
    "Entertainment: Television": ["movies"],
    "Entertainment: Video Games": ["movies"],
    "Entertainment: Japanese Anime & Manga": ["movies"],
    "Entertainment: Cartoon & Animations": ["movies"],
    "Art": ["art"],
    "Entertainment: Books": ["art"],
    "Animals": ["animals"],
    "Science: Mathematics": ["math"],
    "Science: Computers": ["informatics"],
    "Science: Gadgets": ["informatics"],
}

SCIENCE_CAT = "Science & Nature"

CHEMISTRY_RE = re.compile(
    r"(element|acid|molecule|atom|chemical|compound|periodic|metal|ion|reaction|"
    r"oxid|carbon|hydrogen|oxygen|nitrogen|sodium|chlorine|formula|chemistry|ph\b|gas)",
    re.I,
)
PHYSICS_RE = re.compile(
    r"(force|energy|gravity|velocity|speed|light|wave|electric|magnetic|newton|"
    r"joule|watt|pressure|temperature|physics|quantum|mass|friction|momentum|volt|ohm)",
    re.I,
)
ASTRONOMY_RE = re.compile(
    r"(planet|star|moon|sun|galaxy|solar|orbit|astron|space|universe|mars|jupiter|"
    r"venus|saturn|nebula|comet|asteroid|telescope|milky|lunar)",
    re.I,
)

APP_CATEGORIES = [
    "geography", "history", "movies", "art", "animals",
    "math", "informatics", "chemistry", "physics", "astronomy",
]


def decode_text(value: str) -> str:
    return html.unescape(value).strip()


def map_difficulty(value: str) -> str:
    return {"easy": "EASY", "medium": "MEDIUM", "hard": "HARD"}[value.lower()]


def classify_science(text: str) -> str:
    scores = {
        "chemistry": len(CHEMISTRY_RE.findall(text)),
        "physics": len(PHYSICS_RE.findall(text)),
        "astronomy": len(ASTRONOMY_RE.findall(text)),
    }
    best = max(scores, key=scores.get)
    return best if scores[best] > 0 else "astronomy"


def normalize_key(text: str) -> str:
    return re.sub(r"\s+", " ", decode_text(text).lower())


def to_question(item: dict, category: str, counter: int) -> dict | None:
    question = decode_text(item["question"])
    correct = decode_text(item["correct_answer"])
    incorrect = [decode_text(x) for x in item["incorrect_answers"]]
    options = [correct] + incorrect
    if len(options) != 4 or len(set(options)) != 4:
        return None
    if not question or not correct:
        return None
    qid = f"otdb_{category}_{counter:04d}"
    return {
        "id": qid,
        "category": category,
        "difficulty": map_difficulty(item["difficulty"]),
        "text": question,
        "options": options,
        "correctIndex": 0,
        "explanation": f"The correct answer is {correct}.",
    }


def load_dump(path: str = DUMP) -> dict[str, list[dict]]:
    if not os.path.isfile(path):
        raise FileNotFoundError(
            f"Missing {path}. Download with:\n"
            "  curl -sL "
            "https://gist.githubusercontent.com/jbaranski/a3c10856b750441663eec71739d49e43/raw "
            f"-o {path}"
        )

    with open(path, encoding="utf-8") as f:
        items = json.load(f)

    raw_by_app: dict[str, list[dict]] = {c: [] for c in APP_CATEGORIES}
    skipped = 0

    for item in items:
        if item.get("type") != "multiple":
            skipped += 1
            continue

        cat_name = decode_text(item["category"])
        if cat_name == SCIENCE_CAT:
            text = decode_text(item["question"]) + " " + decode_text(item["correct_answer"])
            bucket = classify_science(text)
            raw_by_app[bucket].append(item)
        elif cat_name in DIRECT_MAP:
            for target in DIRECT_MAP[cat_name]:
                raw_by_app[target].append(item)
        else:
            skipped += 1

    total = sum(len(v) for v in raw_by_app.values())
    print(f"Loaded {len(items)} raw items from {path}")
    print(f"Mapped {total} multiple-choice items ({skipped} skipped)")
    for category in APP_CATEGORIES:
        print(f"  {category}: {len(raw_by_app[category])}")
    return raw_by_app


def merge_into_assets(raw_by_app: dict[str, list[dict]]) -> None:
    stats: dict[str, dict[str, int]] = {}
    for category, raw_items in raw_by_app.items():
        path = os.path.join(ASSETS, f"questions_{category}_en.json")
        if not os.path.isfile(path):
            print(f"Skip missing {path}")
            continue

        with open(path, encoding="utf-8") as f:
            existing = json.load(f)

        local = [q for q in existing if not q.get("id", "").startswith("otdb_")]
        seen = {normalize_key(q["text"]) for q in local}

        converted: list[dict] = []
        counter = 1
        for item in raw_items:
            q = to_question(item, category, counter)
            if q is None:
                continue
            key = normalize_key(q["text"])
            if key in seen:
                continue
            seen.add(key)
            converted.append(q)
            counter += 1

        merged = local + converted
        with open(path, "w", encoding="utf-8") as f:
            json.dump(merged, f, ensure_ascii=False, indent=2)
            f.write("\n")

        stats[category] = {
            "local": len(local),
            "imported": len(converted),
            "total": len(merged),
        }
        print(f"{category}: {stats[category]}")

    summary_path = os.path.join(os.path.dirname(__file__), "opentdb_import_stats.json")
    with open(summary_path, "w", encoding="utf-8") as f:
        json.dump(stats, f, indent=2)
    print(f"Stats: {summary_path}")


def main() -> int:
    raw = load_dump()
    merge_into_assets(raw)
    return 0


if __name__ == "__main__":
    sys.exit(main())