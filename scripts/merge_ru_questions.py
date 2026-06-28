#!/usr/bin/env python3
"""Merge curated Russian question batches into app assets."""
from __future__ import annotations

import importlib
import json
import os
import re
import sys

ASSETS = os.path.join(os.path.dirname(__file__), "..", "app", "src", "main", "assets")

PREFIX = {
    "chemistry": "chem",
    "physics": "phys",
    "history": "hist",
    "movies": "movi",
    "art": "art",
    "animals": "anim",
    "geography": "geog",
    "math": "math",
    "informatics": "info",
    "astronomy": "astr",
}


def normalize(text: str) -> str:
    return re.sub(r"\s+", " ", text.strip().lower())


def to_question(category: str, qid: str, difficulty: str, text: str, options: list[str], explanation: str) -> dict:
    if len(options) != 4 or len(set(options)) != 4:
        raise ValueError(f"{qid}: need 4 unique options")
    return {
        "id": qid,
        "category": category,
        "difficulty": difficulty,
        "text": text.strip(),
        "options": [o.strip() for o in options],
        "correctIndex": 0,
        "explanation": explanation.strip(),
    }


def load_batches(category: str, through_batch: int = 5) -> list[tuple]:
    mod = importlib.import_module(f"ru_banks.{category}")
    batches: list[tuple] = []
    for i in range(1, through_batch + 1):
        attr = f"BATCH_{i}"
        if not hasattr(mod, attr):
            raise AttributeError(f"ru_banks.{category} missing {attr}")
        batch = getattr(mod, attr)
        if len(batch) != 40:
            raise ValueError(f"{category} {attr}: expected 40, got {len(batch)}")
        batches.extend(batch)
    return batches


def parse_args(argv: list[str]) -> tuple[int, list[str]]:
    through_batch = 5
    categories: list[str] = []
    i = 0
    while i < len(argv):
        arg = argv[i]
        if arg in ("--through", "-t"):
            through_batch = int(argv[i + 1])
            i += 2
        elif arg.startswith("--through="):
            through_batch = int(arg.split("=", 1)[1])
            i += 1
        else:
            categories.append(arg)
            i += 1
    if not categories:
        categories = list(PREFIX.keys())
    return through_batch, categories


def merge_category(category: str, through_batch: int = 5) -> dict[str, int]:
    path = os.path.join(ASSETS, f"questions_{category}.json")
    with open(path, encoding="utf-8") as f:
        existing = json.load(f)

    seen_texts = {normalize(q["text"]) for q in existing}
    seen_ids = {q["id"] for q in existing}
    prefix = PREFIX[category]

    max_num = 0
    for qid in seen_ids:
        m = re.match(rf"{prefix}_(\d+)$", qid)
        if m:
            max_num = max(max_num, int(m.group(1)))

    added: list[dict] = []
    counter = max_num + 1
    skipped = 0

    for difficulty, text, options, explanation in load_batches(category, through_batch):
        key = normalize(text)
        if key in seen_texts:
            skipped += 1
            continue
        qid = f"{prefix}_{counter:03d}"
        while qid in seen_ids:
            counter += 1
            qid = f"{prefix}_{counter:03d}"
        item = to_question(category, qid, difficulty, text, options, explanation)
        added.append(item)
        seen_texts.add(key)
        seen_ids.add(qid)
        counter += 1

    merged = existing + added
    with open(path, "w", encoding="utf-8") as f:
        json.dump(merged, f, ensure_ascii=False, indent=2)
        f.write("\n")

    return {"existing": len(existing), "added": len(added), "skipped": skipped, "total": len(merged)}


def main() -> int:
    through_batch, categories = parse_args(sys.argv[1:])
    print(f"Merging batches 1..{through_batch}")
    for category in categories:
        stats = merge_category(category, through_batch)
        print(f"{category}: {stats}")
    return 0


if __name__ == "__main__":
    sys.exit(main())