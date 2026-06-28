#!/usr/bin/env python3
"""Audit question text for casing and common typos."""
from __future__ import annotations

import glob
import json
import os
import re

ASSETS = os.path.join(os.path.dirname(__file__), "..", "app", "src", "main", "assets")

ISSUES: list[str] = []

# Lowercase after sentence start in Russian (common mistake)
RU_LC_START = re.compile(r"^[а-яё]")
EN_LC_START = re.compile(r"^[a-z]")

# Double spaces, bad punctuation
DOUBLE_SPACE = re.compile(r"  +")
SPACE_BEFORE_PUNCT = re.compile(r"\s+([,.;:!?])")

# Known bad patterns
BAD_PATTERNS = [
    (re.compile(r"\bмоцарт\b", re.I), "Моцарт"),
    (re.compile(r"\bшекспир\b", re.I), "Шекспир"),
    (re.compile(r"\bplaceholder\b", re.I), "PLACEHOLDER"),
    (re.compile(r"Мейерхольд", re.I), "duplicate check"),
    (re.compile(r"  "), "double space"),
    (re.compile(r"«\s"), "space after open quote"),
    (re.compile(r"\s»"), "space before close quote"),
    (re.compile(r"\?\?"), "double question"),
    (re.compile(r"\.\?"), "period question"),
]

# Option should not be all lowercase sentence in RU if it's a proper noun start
def check_question(path: str, q: dict, idx: int) -> None:
    qid = q.get("id", f"#{idx}")
    loc = f"{os.path.basename(path)}[{idx}] {qid}"
    text = q.get("text", "")
    expl = q.get("explanation", "")
    opts = q.get("options", [])
    is_en = path.endswith("_en.json")

    if not text.strip():
        ISSUES.append(f"EMPTY_TEXT {loc}")
    if is_en:
        if EN_LC_START.match(text.strip()):
            ISSUES.append(f"EN_TEXT_LC_START {loc}: {text[:60]}")
    else:
        if RU_LC_START.match(text.strip()):
            ISSUES.append(f"RU_TEXT_LC_START {loc}: {text[:60]}")

    for field_name, val in [("text", text), ("explanation", expl)]:
        if DOUBLE_SPACE.search(val):
            ISSUES.append(f"DOUBLE_SPACE {loc} {field_name}")
        if SPACE_BEFORE_PUNCT.search(val):
            ISSUES.append(f"SPACE_PUNCT {loc} {field_name}: {val[:50]}")
        if "placeholder" in val.lower():
            ISSUES.append(f"PLACEHOLDER {loc} {field_name}")
        if "fix needed" in val.lower() or "fix:" in val.lower():
            ISSUES.append(f"DRAFT_TEXT {loc} {field_name}")

    for oi, opt in enumerate(opts):
        o = opt.strip()
        if not o:
            ISSUES.append(f"EMPTY_OPT {loc}[{oi}]")
        if DOUBLE_SPACE.search(o):
            ISSUES.append(f"DOUBLE_SPACE_OPT {loc}[{oi}]")
        # RU options that are full sentences starting lowercase
        if not is_en and o and RU_LC_START.match(o) and len(o) > 3:
            # allow chemical formulas, units like "кг/м³", numbers
            if not re.match(r"^[0-9,\.\+\-×÷=]", o) and "м/" not in o[:4]:
                ISSUES.append(f"RU_OPT_LC {loc}[{oi}]: {o[:50]}")

    # duplicate options same casing issue
    if len(set(opts)) != len(opts):
        ISSUES.append(f"DUP_OPTS {loc}")


def main() -> int:
    for path in sorted(glob.glob(os.path.join(ASSETS, "questions_*.json"))):
        with open(path, encoding="utf-8") as f:
            data = json.load(f)
        for i, q in enumerate(data):
            check_question(path, q, i)

    print(f"Issues found: {len(ISSUES)}")
    for item in ISSUES[:80]:
        print(item)
    if len(ISSUES) > 80:
        print(f"... and {len(ISSUES) - 80} more")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())