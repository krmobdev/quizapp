#!/usr/bin/env python3
"""Validate quiz question JSON banks under app/src/main/assets."""
import json
import glob
import os
import sys

ASSETS = os.path.join(os.path.dirname(__file__), "..", "app", "src", "main", "assets")


def main() -> int:
    errors = []
    warnings = []
    for path in sorted(glob.glob(os.path.join(ASSETS, "questions_*.json"))):
        name = os.path.basename(path)
        expected_category = (
            name.removeprefix("questions_").removesuffix(".json").removesuffix("_en")
        )
        with open(path, encoding="utf-8") as f:
            data = json.load(f)
        ids = set()
        for i, q in enumerate(data):
            qid = q.get("id", f"#{i}")
            loc = f"{name}[{i}] {qid}"
            if qid in ids:
                errors.append(f"DUPLICATE_ID {loc}")
            ids.add(qid)
            if q.get("category") != expected_category:
                errors.append(
                    f"WRONG_CATEGORY {loc}: {q.get('category')} != {expected_category}"
                )
            opts = q.get("options", [])
            ci = q.get("correctIndex")
            if not str(q.get("text", "")).strip():
                errors.append(f"EMPTY_TEXT {loc}")
            if len(opts) != 4:
                errors.append(f"OPTION_COUNT {loc}: {len(opts)}")
            if ci != 0:
                errors.append(f"CORRECT_INDEX_NOT_ZERO {loc}: {ci}")
            if ci is None or ci < 0 or ci >= len(opts):
                errors.append(f"BAD_INDEX {loc}: {ci}")
            if len(set(opts)) != len(opts):
                errors.append(f"DUP_OPTIONS {loc}: {opts}")
            if q.get("difficulty") not in ("EASY", "MEDIUM", "HARD"):
                errors.append(f"BAD_DIFFICULTY {loc}: {q.get('difficulty')}")
            if not q.get("explanation", "").strip():
                warnings.append(f"NO_EXPLANATION {loc}")

    print(f"Files scanned: {len(glob.glob(os.path.join(ASSETS, 'questions_*.json')))}")
    print(f"Errors: {len(errors)}")
    print(f"Warnings: {len(warnings)}")
    for e in errors:
        print(e)
    for w in warnings[:20]:
        print(w)
    if len(warnings) > 20:
        print(f"... and {len(warnings) - 20} more warnings")
    return 1 if errors else 0


if __name__ == "__main__":
    sys.exit(main())