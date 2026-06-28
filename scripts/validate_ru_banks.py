#!/usr/bin/env python3
import importlib
import sys

sys.path.insert(0, __file__.rsplit("\\", 1)[0] if "\\" in __file__ else __file__.rsplit("/", 1)[0])

CATS = [
    "chemistry", "physics", "history", "geography", "astronomy",
    "math", "informatics", "animals", "art", "movies",
]


def main() -> int:
    failed = 0
    for cat in CATS:
        try:
            m = importlib.import_module(f"ru_banks.{cat}")
            texts: set[str] = set()
            errs: list[str] = []
            total = 0
            for i in range(1, 6):
                batch = getattr(m, f"BATCH_{i}")
                if len(batch) != 40:
                    errs.append(f"BATCH_{i}={len(batch)}")
                total += len(batch)
                for item in batch:
                    diff, text, opts, expl = item
                    if diff not in ("EASY", "MEDIUM", "HARD"):
                        errs.append(f"bad diff {diff}")
                    if len(opts) != 4 or len(set(opts)) != 4:
                        errs.append("bad opts")
                    if not text.strip().endswith("?"):
                        errs.append("no ?")
                    if not expl.strip():
                        errs.append("no expl")
                    texts.add(text.lower().strip())
            if len(texts) != total:
                errs.append(f"dup texts {total - len(texts)}")
            status = "OK" if not errs else ", ".join(errs[:5])
            print(f"{cat}: {total} {status}")
            if errs:
                failed += 1
        except Exception as exc:
            print(f"{cat}: FAIL {exc}")
            failed += 1
    return 1 if failed else 0


if __name__ == "__main__":
    raise SystemExit(main())