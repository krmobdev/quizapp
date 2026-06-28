#!/usr/bin/env python3
import re
from pathlib import Path

path = Path(__file__).parent / "ru_banks" / "math.py"
lines = path.read_text(encoding="utf-8").splitlines()
out = []
for line in lines:
    m = re.match(r'(\s+\("(?:EASY|MEDIUM|HARD)", ")([^"]+)(",)', line)
    if m and not m.group(2).endswith("?"):
        text = m.group(2)
        if text.endswith("."):
            text = text[:-1] + "?"
        else:
            text += "?"
        line = f"{m.group(1)}{text}{m.group(3)}" + line[m.end():]
    out.append(line)
path.write_text("\n".join(out) + "\n", encoding="utf-8")
print("fixed math questions")