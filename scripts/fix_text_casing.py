#!/usr/bin/env python3
"""Fix whitespace, capitalization and known bad text in question banks."""
from __future__ import annotations

import glob
import json
import os
import re

ASSETS = os.path.join(os.path.dirname(__file__), "..", "app", "src", "main", "assets")

DOUBLE_SPACE = re.compile(r" {2,}")
SPACE_BEFORE_PUNCT = re.compile(r"\s+([?!.,;:])")
TRAILING_SPACE = re.compile(r"\s+$")
MULTI_NEWLINE = re.compile(r"\n{3,}")

# Do not auto-capitalize options matching these prefixes
OPT_SKIP_CAP = re.compile(
    r"^(?:"
    r"[0-9+\-±≈∼~]"
    r"|H[₂2]?O|CO[₂2]?|O[₂2]|NaCl|pH\b|n-|i-|m-|k-|g-|cm|mm|km|mg|kg|ml"
    r"|CH[₄4]?|C[₂2]H"
    r"|\?|:"
    r")",
    re.I,
)

# Full question replacements by id (RU)
REPLACE_BY_ID: dict[str, dict] = {
    "chem_120": {
        "category": "chemistry",
        "difficulty": "MEDIUM",
        "text": "Какой металл легирует сталь, придавая ей стойкость к коррозии?",
        "options": ["Хром", "Медь", "Цинк", "Свинец"],
        "correctIndex": 0,
        "explanation": "Хром образует защитную оксидную плёнку на поверхности нержавеющей стали.",
    },
    "chem_085": {
        "category": "chemistry",
        "difficulty": "EASY",
        "text": "Как называют соединение H₂O в быту и науке?",
        "options": ["Вода", "Поваренная соль", "Углекислый газ", "Аммиак"],
        "correctIndex": 0,
        "explanation": "H₂O — молекула воды.",
    },
    "chem_066": {
        "explanation": "Элементов в честь Ньютона нет; Эйнштейний, Борий и Фермий названы в честь учёных.",
    },
    "math_103": {
        "category": "math",
        "difficulty": "MEDIUM",
        "text": "Чему равна производная sin(x)?",
        "options": ["cos(x)", "−sin(x)", "tan(x)", "1"],
        "correctIndex": 0,
        "explanation": "Производная синуса: (sin x)′ = cos x.",
    },
    "phys_061": {
        "explanation": "Закон всемирного тяготения открыл Исаак Ньютон.",
    },
}

# Field patches: id -> {field: value} or {options: [...]}
PATCHES: dict[str, dict] = {
    "art_071": {
        "options": ["Испанский", "Французский", "Итальянский", "Немецкий"],
        "explanation": "Пабло Пикассо родился в Испании.",
    },
    "art_078": {
        "options": [
            "Утилитарные предметы, возведённые в статус искусства",
            "Серия, ориентированная на цвет и свет, а не на линию",
            "Картины с едва заметным очерченным квадратом",
            "Группа одинаковых стальных коробок, торчащих из стены",
        ],
        "explanation": "Поп-арт возводит предметы массового потребления в ранг искусства.",
    },
    "art_079": {
        "options": ["Остроконечная арка", "Кессонные потолки", "Фасады, увенчанные фронтоном", "Внутренние фрески"],
        "explanation": "Остроконечная арка — характерный признак готической архитектуры.",
    },
    "art_088": {
        "options": ["Испанский", "Итальянский", "Немецкий", "Португальский"],
        "explanation": "Пабло Пикассо был испанцем по происхождению.",
    },
    "art_072": {
        "options": ["Мона Лиза", "Девушка с жемчужной серёжкой", "Звёздная ночь", "Подсолнухи"],
    },
}

TEXT_PATCHES: dict[str, dict] = {
    "otdb_movies_0103": {
        "text": 'In the game "Brawlhalla", what species is the character Bödvar?',
    },
}

EN_PATCHES: dict[str, dict] = {
    "art_078": {
        "options": ["Pointed arch", "Coffered ceilings", "Pedimented facades", "Interior frescoes"],
        "explanation": "The pointed arch is a hallmark of Gothic architecture.",
    },
    "art_079": {
        "options": ["Pointed arch", "Coffered ceilings", "Pedimented facades", "Interior frescoes"],
    },
    "chem_120": {
        "text": "Which metal is alloyed into steel to improve corrosion resistance?",
        "options": ["Chromium", "Copper", "Zinc", "Lead"],
        "explanation": "Chromium forms a protective oxide layer on stainless steel.",
    },
    "chem_085": {
        "text": "What is the common name for H₂O?",
        "options": ["Water", "Table salt", "Carbon dioxide", "Ammonia"],
        "explanation": "H₂O is the water molecule.",
    },
    "math_103": {
        "text": "What is the derivative of sin(x)?",
        "options": ["cos(x)", "−sin(x)", "tan(x)", "1"],
        "explanation": "The derivative of sin x is cos x.",
    },
}

# Global string replacements (apply to all text fields)
GLOBAL_REPLACEMENTS = [
    ("возведенные", "возведённые"),
    ("возведенная", "возведённая"),
    ("возведенный", "возведённый"),
    ("сережкой", "серёжкой"),
    ("сережка", "серёжка"),
    ("  ", " "),
    (" ?","?"),
    (" .", "."),
    (" ,", ","),
    ("The correct answer is ", "The correct answer is "),
]

EXPL_FIX = re.compile(r"The correct answer is (.+?) \.$")
EXPL_FIX_NOSPACE = re.compile(r"The correct answer is([?:][?:].*?)\.$")
RU_EXPL_LC = re.compile(r"(Правильный ответ — )([а-яё][^\.\n]*)")


def apply_global(text: str) -> str:
    for old, new in GLOBAL_REPLACEMENTS:
        text = text.replace(old, new)
    text = DOUBLE_SPACE.sub(" ", text)
    text = SPACE_BEFORE_PUNCT.sub(r"\1", text)
    return text.strip()


def capitalize_option(opt: str, is_en: bool) -> str:
    s = opt.strip()
    if not s or OPT_SKIP_CAP.match(s):
        return s
    # Already starts with upper or special
    if s[0].isupper() or s[0] in "«\"'([":
        return s
    # Latin
    if is_en and s[0].islower():
        return s[0].upper() + s[1:]
    # Cyrillic lowercase start
    if not is_en and s[0] in "абвгдежзийклмнопрстуфхцчшщъыьэюя":
        return s[0].upper() + s[1:]
    return s


def capitalize_sentence(text: str) -> str:
    text = apply_global(text)
    if not text:
        return text
    if text[0].islower():
        return text[0].upper() + text[1:]
    return text


def fix_question(q: dict, is_en: bool) -> dict:
    qid = q.get("id", "")
    if qid in REPLACE_BY_ID and not is_en:
        q = {**q, **REPLACE_BY_ID[qid]}
    if qid in TEXT_PATCHES:
        for key, val in TEXT_PATCHES[qid].items():
            q[key] = val
    patches = EN_PATCHES if is_en else PATCHES
    if qid in patches:
        for key, val in patches[qid].items():
            q[key] = val

    q["text"] = capitalize_sentence(q.get("text", ""))
    expl = q.get("explanation", "")
    if expl:
        expl = apply_global(expl)
        m = EXPL_FIX.match(expl)
        if m:
            expl = f"The correct answer is {m.group(1).strip()}."
        if expl == "The correct answer is?:.":
            expl = 'The correct answer is "?:".'
        elif expl == "The correct answer is ?:.":
            expl = 'The correct answer is "?:".'
        if not is_en:
            def _ru_expl(m: re.Match[str]) -> str:
                ans = m.group(2).strip()
                if ans and ans[0] in "абвгдежзийклмнопрстуфхцчшщъыьэюя":
                    ans = ans[0].upper() + ans[1:]
                return m.group(1) + ans

            expl = RU_EXPL_LC.sub(_ru_expl, expl)
        if expl and expl[0].islower():
            expl = expl[0].upper() + expl[1:]
        q["explanation"] = expl

    opts = q.get("options", [])
    q["options"] = [capitalize_option(apply_global(o), is_en) for o in opts]
    return q


def process_file(path: str) -> int:
    is_en = path.endswith("_en.json")
    with open(path, encoding="utf-8") as f:
        data = json.load(f)
    changed = 0
    for i, q in enumerate(data):
        before = json.dumps(q, ensure_ascii=False, sort_keys=True)
        data[i] = fix_question(q, is_en)
        after = json.dumps(data[i], ensure_ascii=False, sort_keys=True)
        if before != after:
            changed += 1
    with open(path, "w", encoding="utf-8") as f:
        json.dump(data, f, ensure_ascii=False, indent=2)
        f.write("\n")
    return changed


def main() -> int:
    total = 0
    for path in sorted(glob.glob(os.path.join(ASSETS, "questions_*.json"))):
        n = process_file(path)
        if n:
            print(f"{os.path.basename(path)}: {n} questions updated")
        total += n
    print(f"Total updated: {total}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())