#!/usr/bin/env python3
"""Apply same text normalization to ru_banks tuple sources."""
from __future__ import annotations

import importlib
import os
import re
import sys

sys.path.insert(0, os.path.dirname(__file__))
from fix_text_casing import capitalize_option, capitalize_sentence, apply_global

CATS = [
    "chemistry", "physics", "history", "geography", "astronomy",
    "math", "informatics", "animals", "art", "movies",
]


def fix_tuple(item: tuple) -> tuple:
    diff, text, opts, expl = item
    text = capitalize_sentence(text)
    expl = capitalize_sentence(expl)
    opts = [capitalize_option(apply_global(o), False) for o in opts]
    return (diff, text, opts, expl)


def format_batch(items: list[tuple]) -> str:
    lines = ["["]
    for item in items:
        diff, text, opts, expl = item
        lines.append(f'    ({diff!r}, {text!r}, {opts!r}, {expl!r}),')
    lines.append("]")
    return "\n".join(lines)


def main() -> int:
    for cat in CATS:
        mod = importlib.import_module(f"ru_banks.{cat}")
        parts = [f"# Curated Russian {cat} question batches.\n"]
        for i in range(1, 6):
            batch = [fix_tuple(t) for t in getattr(mod, f"BATCH_{i}")]
            parts.append(f"BATCH_{i} = {format_batch(batch)}\n")
        path = os.path.join(os.path.dirname(__file__), "ru_banks", f"{cat}.py")
        with open(path, "w", encoding="utf-8") as f:
            f.write("\n".join(parts))
        print(f"fixed ru_banks/{cat}.py")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())