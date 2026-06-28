#!/usr/bin/env python3
"""Fix erroneous quiz questions and expand question banks."""
from __future__ import annotations

import json
import os
from copy import deepcopy
from typing import Any

ASSETS = os.path.join(os.path.dirname(__file__), "..", "app", "src", "main", "assets")


def q(
    qid: str,
    category: str,
    difficulty: str,
    text: str,
    options: list[str],
    explanation: str,
) -> dict[str, Any]:
    return {
        "id": qid,
        "category": category,
        "difficulty": difficulty,
        "text": text,
        "options": options,
        "correctIndex": 0,
        "explanation": explanation,
    }


# Full question replacements keyed by id (RU). EN handled separately.
CHEMISTRY_REPLACE_RU: dict[str, dict[str, Any]] = {
    "chem_061": q(
        "chem_061", "chemistry", "MEDIUM",
        "К какому классу относятся углеводороды с тройными связями C≡C?",
        ["Алкины", "Алкены", "Алканы", "Спирты"],
        "Алкины содержат тройную связь между атомами углерода.",
    ),
    "chem_067": q(
        "chem_067", "chemistry", "EASY",
        "Какой газ необходим большинству процессов горения?",
        ["Кислород", "Азот", "Аргон", "Гелий"],
        "Кислород — окислитель, поддерживающий горение.",
    ),
    "chem_069": q(
        "chem_069", "chemistry", "MEDIUM",
        "Как называется число Авогадро?",
        ["6,022·10²³", "3,14·10²³", "9,81·10²³", "1,602·10²³"],
        "Число Авогадро — 6,022·10²³ частиц в одном моле.",
    ),
    "chem_075": q(
        "chem_075", "chemistry", "MEDIUM",
        "Какой элемент имеет наименьшую электроотрицательность?",
        ["Цезий", "Фтор", "Кислород", "Хлор"],
        "Цезий — самый электроположительный щелочной металл.",
    ),
    "chem_076": q(
        "chem_076", "chemistry", "EASY",
        "Какой индикатор становится розовым в щелочной среде?",
        ["Фенолфталеин", "Метилоранж", "Лакмус только синий", "Йод"],
        "Фенолфталеин бесцветен в кислоте и розовеет в щелочи.",
    ),
    "chem_079": q(
        "chem_079", "chemistry", "HARD",
        "Какой элемент образует газообразное простое вещество H₂?",
        ["Водород", "Гелий", "Азот", "Кислород"],
        "Молекулярный водород — H₂.",
    ),
    "chem_081": q(
        "chem_081", "chemistry", "MEDIUM",
        "Какой процесс описывает переход вещества из газа в жидкость?",
        ["Конденсация", "Сублимация", "Испарение", "Плавление"],
        "При конденсации пар превращается в жидкость.",
    ),
    "chem_082": q(
        "chem_082", "chemistry", "MEDIUM",
        "Какой кислотный остаток входит в состав серной кислоты?",
        ["SO₄²⁻", "NO₃⁻", "CO₃²⁻", "PO₄³⁻"],
        "Серная кислота — H₂SO₄, остаток сульфат-ион.",
    ),
    "chem_084": q(
        "chem_084", "chemistry", "EASY",
        "Какой металл жидок при комнатной температуре?",
        ["Ртуть", "Железо", "Медь", "Алюминий"],
        "Ртуть плавится при −38,8 °C.",
    ),
    "chem_092": q(
        "chem_092", "chemistry", "MEDIUM",
        "Какой газ выделяется при реакции кислоты с карбонатом?",
        ["Углекислый газ", "Водород", "Кислород", "Аммиак"],
        "Кислоты с карбонатами дают CO₂.",
    ),
    "chem_095": q(
        "chem_095", "chemistry", "MEDIUM",
        "Какой элемент обозначается символом K?",
        ["Калий", "Кальций", "Криптон", "Кобальт"],
        "K (kalium) — калий, атомный номер 19.",
    ),
    "chem_097": q(
        "chem_097", "chemistry", "HARD",
        "Какой тип связи характерен для кристаллической решётки металлов?",
        ["Металлическая", "Ионная", "Ковалентная неполярная", "Водородная"],
        "В металлах электроны делокализованы — металлическая связь.",
    ),
    "chem_098": q(
        "chem_098", "chemistry", "MEDIUM",
        "Как называется реакция соединения с выделением тепла?",
        ["Экзотермическая", "Эндотермическая", "Гидролиз", "Электролиз"],
        "Экзотермические реакции выделяют тепло в окружающую среду.",
    ),
    "chem_099": q(
        "chem_099", "chemistry", "EASY",
        "Какой pH соответствует нейтральной среде при 25 °C?",
        ["7", "0", "14", "1"],
        "Нейтральная вода имеет pH = 7.",
    ),
    "chem_100": q(
        "chem_100", "chemistry", "MEDIUM",
        "Какой элемент входит в состав всех органических соединений?",
        ["Углерод", "Кремний", "Азот", "Сера"],
        "Органическая химия изучает соединения углерода.",
    ),
    "chem_101": q(
        "chem_101", "chemistry", "HARD",
        "Какой изомер бутана имеет разветвлённую цепь?",
        ["Изобутан", "н-Бутан", "Пропан", "Пентан"],
        "Изобутан (2-метилпропан) — разветвлённый изомер C₄H₁₀.",
    ),
    "chem_114": q(
        "chem_114", "chemistry", "MEDIUM",
        "Какой закон утверждает постоянство состава соединения?",
        ["Закон Пруста", "Закон Бойля", "Закон Гука", "Закон Ома"],
        "Закон постоянства состава сформулирован Прустом.",
    ),
    "chem_115": q(
        "chem_115", "chemistry", "EASY",
        "Какой газ составляет основную часть атмосферы Земли?",
        ["Азот", "Кислород", "Углекислый газ", "Аргон"],
        "Азот составляет около 78% воздуха.",
    ),
    "chem_116": q(
        "chem_116", "chemistry", "MEDIUM",
        "Какой процесс разделяет смесь жидкостей по температуре кипения?",
        ["Дистилляция", "Фильтрация", "Центрифугирование", "Кристаллизация"],
        "При дистилляции компоненты испаряются при разных температурах.",
    ),
    "chem_118": q(
        "chem_118", "chemistry", "HARD",
        "Какой принцип запрещает двум электронам атома иметь одинаковые все квантовые числа?",
        ["Принцип Паули", "Принцип Гейзенберга", "Правило Хунда", "Закон Гесса"],
        "Принцип запрета Паули действует на фермионы, в т.ч. электроны.",
    ),
}

CHEMISTRY_PATCH_RU: dict[str, dict[str, Any]] = {
    "chem_062": {"options": ["Магний", "Медь", "Литий", "Свинец"]},
    "chem_070": {"text": "Какой атомный номер урана?"},
    "chem_073": {
        "text": "Сколько объектов соответствует одной моле?",
        "explanation": "В одном моле 6,022·10²³ частиц (число Авогадро).",
    },
    "chem_086": {
        "text": "Как по-латыни называется пупок (медицинский термин)?",
        "options": ["Umbilicus", "Пупок", "Пупочное кольцо", "Омфалос"],
        "explanation": "Медицинский латинский термин — umbilicus.",
    },
    "chem_091": {
        "options": ["Вольфрам", "Углерод", "Железо", "Кремний"],
        "explanation": "Вольфрам плавится при 3422 °C — рекорд среди металлов.",
    },
    "chem_093": {
        "options": ["He", "H", "Ne", "Ho"],
        "explanation": "Символ гелия — He (от hellium).",
    },
    "chem_094": {
        "text": "Деионизированная вода — это вода, из которой удалены преимущественно:",
        "options": ["Ионы минеральных солей", "Железо", "Кислород", "Водород"],
        "explanation": "Деионизация удаляет ионы растворённых солей.",
    },
    "chem_096": {
        "options": ["Витамин B9", "Витамин B1", "Витамин C", "Витамин D"],
        "explanation": "Фолиевая кислота — синтетическая форма витамина B9.",
    },
    "chem_120": q(
        "chem_120", "chemistry", "MEDIUM",
        "Какой металл легирует сталь, придавая ей стойкость к коррозии?",
        ["Хром", "Медь", "Цинк", "Свинец"],
        "Хром образует защитную оксидную плёнку на поверхности нержавеющей стали.",
    ),
}

PHYSICS_REPLACE_RU: dict[str, dict[str, Any]] = {
    "phys_061": q(
        "phys_061", "physics", "MEDIUM",
        "Какая физическая величина измеряется в паскалях?",
        ["Давление", "Сила", "Энергия", "Мощность"],
        "Паскаль (Па) — единица давления в СИ.",
    ),
    "phys_063": q(
        "phys_063", "physics", "MEDIUM",
        "Кто сформулировал три закона механики?",
        ["Исаак Ньютон", "Альберт Эйнштейн", "Галилео Галилей", "Нильс Бор"],
        "Законы Ньютона лежат в основе классической механики.",
    ),
    "phys_066": q(
        "phys_066", "physics", "MEDIUM",
        "Как называется сила, направленная к центру криволинейного движения?",
        ["Центростремительная", "Сила трения", "Сила упругости", "Сила Архимеда"],
        "Центростремительная сила обеспечивает криволинейную траекторию.",
    ),
    "phys_067": q(
        "phys_067", "physics", "HARD",
        "Какой физик предсказал существование антиматерии?",
        ["Поль Дирак", "Эрнест Резерфорд", "Макс Планк", "Вернер Гейзенберг"],
        "Уравнение Дирака допускало отрицательную энергию — античастицы.",
    ),
    "phys_068": q(
        "phys_068", "physics", "MEDIUM",
        "Какой прибор измеряет электрическое сопротивление?",
        ["Омметр", "Амперметр", "Вольтметр", "Ваттметр"],
        "Сопротивление измеряют в омах омметром.",
    ),
    "phys_069": q(
        "phys_069", "physics", "EASY",
        "В каких единицах СИ измеряется работа?",
        ["Джоуль", "Ньютон", "Ватт", "Паскаль"],
        "Работа и энергия измеряются в джоулях.",
    ),
    "phys_070": q(
        "phys_070", "physics", "MEDIUM",
        "Какой закон связывает силу тока, напряжение и сопротивление?",
        ["Закон Ома", "Закон Кулона", "Закон Гука", "Закон Паскаля"],
        "U = I·R — закон Ома для участка цепи.",
    ),
    "phys_071": q(
        "phys_071", "physics", "MEDIUM",
        "Как называется явление отклонения света на границе сред?",
        ["Преломление", "Дифракция", "Интерференция", "Поляризация"],
        "При преломлении меняется направление распространения света.",
    ),
    "phys_074": q(
        "phys_074", "physics", "MEDIUM",
        "Какая частица переносит электрический ток в металлах?",
        ["Электрон", "Протон", "Нейтрон", "Позитрон"],
        "Свободные электроны — носители тока в проводниках.",
    ),
    "phys_077": q(
        "phys_077", "physics", "HARD",
        "Какова формула кинетической энергии тела?",
        ["mv²/2", "mgh", "Fs", "ma"],
        "Кинетическая энергия E = mv²/2.",
    ),
    "phys_079": q(
        "phys_079", "physics", "MEDIUM",
        "Какой тип волны требует упругой среды для распространения?",
        ["Звуковая", "Световая", "Рентгеновская", "Радиоволна в вакууме"],
        "Звук — механические колебания среды.",
    ),
    "phys_081": q(
        "phys_081", "physics", "MEDIUM",
        "Как называется минимальная энергия для выхода электрона из металла?",
        ["Работа выхода", "Энергия связи", "Теплоёмкость", "Потенциал ионизации"],
        "Фотоэффект описывается работой выхода металла.",
    ),
    "phys_083": q(
        "phys_083", "physics", "HARD",
        "Какой закон описывает силу взаимодействия двух точечных зарядов?",
        ["Закон Кулона", "Закон Ома", "Закон Фарадея", "Закон Бойля"],
        "F = k·|q₁·q₂|/r² — закон Кулона.",
    ),
    "phys_085": q(
        "phys_085", "physics", "EASY",
        "Что измеряет барометр?",
        ["Атмосферное давление", "Температуру", "Влажность", "Скорость ветра"],
        "Барометр показывает давление воздуха.",
    ),
    "phys_086": q(
        "phys_086", "physics", "MEDIUM",
        "Какой цвет света имеет наибольшую длину волны в видимом спектре?",
        ["Красный", "Фиолетовый", "Синий", "Зелёный"],
        "Красный свет имеет длину волны около 650–700 нм.",
    ),
    "phys_087": q(
        "phys_087", "physics", "MEDIUM",
        "Как называется единица частоты в СИ?",
        ["Герц", "Ньютон", "Джоуль", "Вольт"],
        "Герц (Гц) — колебаний в секунду.",
    ),
    "phys_088": q(
        "phys_088", "physics", "HARD",
        "Какой коэффициент показывает отношение скорости в среде к скорости в вакууме?",
        ["Показатель преломления", "Коэффициент трения", "Модуль Юнга", "Плотность"],
        "Показатель преломления n = c/v.",
    ),
    "phys_089": q(
        "phys_089", "physics", "MEDIUM",
        "Какой прибор преобразует механическую энергию в электрическую?",
        ["Генератор", "Трансформатор", "Реостат", "Конденсатор"],
        "Генератор вырабатывает электрический ток.",
    ),
    "phys_091": q(
        "phys_091", "physics", "HARD",
        "Какова приблизительная скорость звука в воздухе при 20 °C?",
        ["343 м/с", "1500 м/с", "100 м/с", "3000 м/с"],
        "В воздухе при комнатной температуре звук ~343 м/с.",
    ),
    "phys_092": q(
        "phys_092", "physics", "MEDIUM",
        "Какой закон: «Действие равно противодействию»?",
        ["Третий закон Ньютона", "Первый закон Ньютона", "Закон сохранения энергии", "Закон Гука"],
        "Третий закон Ньютона описывает пары сил.",
    ),
    "phys_093": q(
        "phys_093", "physics", "MEDIUM",
        "Как называется способность тел проводить электрический ток?",
        ["Электропроводность", "Теплопроводность", "Магнитность", "Вязкость"],
        "Электропроводность характеризует носителей заряда.",
    ),
    "phys_096": q(
        "phys_096", "physics", "HARD",
        "Какой физик открыл радиоактивность?",
        ["Анри Беккерель", "Мария Кюри", "Эрнест Резерфорд", "Джеймс Чедвик"],
        "Беккерель обнаружил естественную радиоактивность урановых солей.",
    ),
    "phys_097": q(
        "phys_097", "physics", "MEDIUM",
        "Какой тип линзы собирает параллельные лучи в фокусе?",
        ["Собирающая (выпуклая)", "Рассеивающая (вогнутая)", "Плоская", "Цилиндрическая"],
        "Собирающая линза имеет положительную оптическую силу.",
    ),
    "phys_099": q(
        "phys_099", "physics", "EASY",
        "Какой прибор измеряет силу электрического тока?",
        ["Амперметр", "Вольтметр", "Омметр", "Гальванометр только постоянный"],
        "Амперметр подключают последовательно в цепь.",
    ),
    "phys_101": q(
        "phys_101", "physics", "MEDIUM",
        "Сколько хромосом в диплоидной клетке человека?",
        ["46", "23", "48", "44"],
        "В соматических клетках 46 хромосом (23 пары).",
    ),
    "phys_102": q(
        "phys_102", "physics", "MEDIUM",
        "Какой закон: при постоянной температуре давление газа обратно объёму?",
        ["Закон Бойля — Мариотта", "Закон Гей-Люссака", "Закон Авогадро", "Закон Гука"],
        "p·V = const при T = const.",
    ),
    "phys_103": q(
        "phys_103", "physics", "HARD",
        "Как называется частица-составитель протона и нейтрона?",
        ["Кварк", "Электрон", "Нейтрино", "Мюон"],
        "Протон и нейтрон состоят из кварков u и d.",
    ),
}

PHYSICS_PATCH_RU: dict[str, dict[str, Any]] = {
    "phys_098": {"options": ["Нога", "Рука", "Туловище", "Голова"]},
}

CHEMISTRY_PATCH_EN: dict[str, dict[str, Any]] = {
    "chem_070": {"text": "What is the atomic number of uranium?"},
    "chem_073": {
        "text": "How many objects are in one mole?",
        "explanation": "One mole contains 6.022×10²³ particles (Avogadro's number).",
    },
    "chem_091": {
        "options": ["Tungsten", "Carbon", "Iron", "Silicon"],
        "explanation": "Tungsten melts at 3422 °C — highest among metals.",
    },
    "chem_093": {
        "options": ["He", "H", "Ne", "Ho"],
        "explanation": "Helium's symbol is He.",
    },
    "chem_094": {
        "text": "Deionized water has primarily been stripped of:",
        "options": ["Mineral salt ions", "Iron", "Oxygen", "Hydrogen"],
        "explanation": "Deionization removes dissolved ionic salts.",
    },
    "chem_096": {
        "options": ["Vitamin B9", "Vitamin B1", "Vitamin C", "Vitamin D"],
        "explanation": "Folic acid is the synthetic form of vitamin B9.",
    },
    "chem_120": q(
        "chem_120", "chemistry", "MEDIUM",
        "Which metal is alloyed into steel to improve corrosion resistance?",
        ["Chromium", "Copper", "Zinc", "Lead"],
        "Chromium forms a protective oxide layer on stainless steel.",
    ),
    "chem_062": {"options": ["Magnesium", "Copper", "Lithium", "Lead"]},
    "chem_101": {
        "options": ["Isobutane", "n-Butane", "Propane", "Pentane"],
        "explanation": "Isobutane (2-methylpropane) is the branched C₄H₁₀ isomer.",
    },
}

PHYSICS_PATCH_EN: dict[str, dict[str, Any]] = {
    "phys_098": {"options": ["Leg", "Arm", "Torso", "Head"]},
    "phys_101": {
        "options": ["46", "23", "48", "44"],
        "explanation": "Human somatic cells have 46 chromosomes (23 pairs).",
    },
    "phys_077": {
        "options": ["Vulpes vulpes", "Vulpes redus", "Red fox", "Vulpes vulpi"],
        "explanation": "The red fox is Vulpes vulpes.",
    },
    "phys_091": {
        "text": "What is the approximate speed of sound in air at 20 °C?",
        "options": ["343 m/s", "1500 m/s", "100 m/s", "3000 m/s"],
        "explanation": "Sound travels at about 343 m/s in air at room temperature.",
    },
}

MATH_PATCH: dict[str, dict[str, dict[str, Any]]] = {
    "questions_math.json": {
        "math_080": {
            "options": [
                "314,15 кв. дюймов",
                "314,15 дюймов",
                "314,15 кв. см",
                "314,15 см",
            ],
            "explanation": "Площадь круга измеряется в квадратных единицах: πr² ≈ 314,15 кв. дюймов.",
        }
    },
    "questions_math_en.json": {
        "math_080": {
            "options": [
                "314.15 square inches",
                "314.15 inches",
                "314.15 square cm",
                "314.15 cm",
            ],
            "explanation": "Circle area uses square units: πr² ≈ 314.15 square inches.",
        }
    },
}

ANIMALS_PATCH: dict[str, dict[str, dict[str, Any]]] = {
    "questions_animals.json": {
        "anim_061": {
            "options": ["Трутень", "Рабочая пчела", "Матка", "Шмель"],
            "explanation": "Трутень — самец пчелы, развивается из неоплодотворённого яйца.",
        },
        "anim_118": {
            "options": [
                "Haliaeetus leucocephalus",
                "Tyto alba",
                "Cyanocitta cristata",
                "Aquila chrysaetos",
            ],
            "explanation": "Белоголовый орлан — Haliaeetus leucocephalus.",
        },
        "anim_117": {
            "options": [
                "Melopsittacus undulatus",
                "Psittacula krameri",
                "Agapornis roseicollis",
                "Nymphicus hollandicus",
            ],
            "explanation": "Волнистый попугайчик — Melopsittacus undulatus.",
        },
        "anim_100": {
            "options": ["Далматин", "Лабрадор", "Хаски", "Немецкая овчарка"],
        },
    },
    "questions_animals_en.json": {
        "anim_061": {
            "options": ["Drone", "Worker bee", "Queen bee", "Bumblebee"],
            "explanation": "A drone is a male honey bee from an unfertilized egg.",
        },
        "anim_118": {
            "options": [
                "Haliaeetus leucocephalus",
                "Tyto alba",
                "Cyanocitta cristata",
                "Aquila chrysaetos",
            ],
            "explanation": "The bald eagle is Haliaeetus leucocephalus.",
        },
    },
}

MOVIES_PATCH: dict[str, dict[str, dict[str, Any]]] = {
    "questions_movies.json": {
        "movi_076": {
            "text": "Кто главный герой-ребёнок в мультфильме «Железный гигант»?",
            "options": ["Хогарт", "Железный гигант", "Дин", "Кент"],
            "explanation": "Хогарт — мальчик, подружившийся с роботом.",
        }
    },
    "questions_movies_en.json": {
        "movi_076": {
            "text": "Who is the child protagonist in The Iron Giant?",
            "options": ["Hogarth", "The Iron Giant", "Dean", "Kent"],
            "explanation": "Hogarth is the boy who befriends the robot.",
        }
    },
}


def apply_patches(questions: list[dict], patches: dict[str, dict]) -> None:
    for item in questions:
        patch = patches.get(item["id"])
        if patch:
            item.update(patch)


def apply_replacements(questions: list[dict], replacements: dict[str, dict]) -> None:
    by_id = {item["id"]: i for i, item in enumerate(questions)}
    for qid, replacement in replacements.items():
        if qid in by_id:
            questions[by_id[qid]] = replacement


def trim_strings(obj: Any) -> Any:
    if isinstance(obj, str):
        return obj.strip()
    if isinstance(obj, list):
        return [trim_strings(x) for x in obj]
    if isinstance(obj, dict):
        return {k: trim_strings(v) for k, v in obj.items()}
    return obj


def build_en_chemistry_replacements() -> dict[str, dict[str, Any]]:
    mapping = {
        "chem_061": (
            "Which class includes hydrocarbons with triple C≡C bonds?",
            ["Alkynes", "Alkenes", "Alkanes", "Alcohols"],
            "Alkynes contain a triple bond between carbon atoms.",
        ),
        "chem_067": (
            "Which gas is required for most combustion processes?",
            ["Oxygen", "Nitrogen", "Argon", "Helium"],
            "Oxygen acts as the oxidizer in combustion.",
        ),
        "chem_069": (
            "What is Avogadro's number (approximately)?",
            ["6.022×10²³", "3.14×10²³", "9.81×10²³", "1.602×10²³"],
            "One mole contains 6.022×10²³ particles.",
        ),
        "chem_075": (
            "Which element has the lowest electronegativity?",
            ["Cesium", "Fluorine", "Oxygen", "Chlorine"],
            "Cesium is the most electropositive alkali metal.",
        ),
        "chem_076": (
            "Which indicator turns pink in alkaline solution?",
            ["Phenolphthalein", "Methyl orange", "Litmus only blue", "Iodine"],
            "Phenolphthalein is colorless in acid and pink in base.",
        ),
        "chem_079": (
            "Which element forms the diatomic gas H₂?",
            ["Hydrogen", "Helium", "Nitrogen", "Oxygen"],
            "Molecular hydrogen is H₂.",
        ),
        "chem_081": (
            "What process changes a gas into a liquid?",
            ["Condensation", "Sublimation", "Evaporation", "Melting"],
            "Condensation is gas → liquid.",
        ),
        "chem_082": (
            "Which acid residue is in sulfuric acid?",
            ["SO₄²⁻", "NO₃⁻", "CO₃²⁻", "PO₄³⁻"],
            "Sulfuric acid is H₂SO₄.",
        ),
        "chem_084": (
            "Which metal is liquid at room temperature?",
            ["Mercury", "Iron", "Copper", "Aluminum"],
            "Mercury melts at −38.8 °C.",
        ),
        "chem_092": (
            "Which gas is released when acid reacts with a carbonate?",
            ["Carbon dioxide", "Hydrogen", "Oxygen", "Ammonia"],
            "Acids with carbonates produce CO₂.",
        ),
        "chem_095": (
            "Which element has the symbol K?",
            ["Potassium", "Calcium", "Krypton", "Cobalt"],
            "K stands for potassium (kalium), atomic number 19.",
        ),
        "chem_097": (
            "Which bond type is typical in metallic crystals?",
            ["Metallic", "Ionic", "Nonpolar covalent", "Hydrogen"],
            "Delocalized electrons form metallic bonding.",
        ),
        "chem_098": (
            "What is a reaction that releases heat called?",
            ["Exothermic", "Endothermic", "Hydrolysis", "Electrolysis"],
            "Exothermic reactions release heat to surroundings.",
        ),
        "chem_099": (
            "What pH is neutral at 25 °C?",
            ["7", "0", "14", "1"],
            "Neutral water has pH = 7.",
        ),
        "chem_100": (
            "Which element is in all organic compounds?",
            ["Carbon", "Silicon", "Nitrogen", "Sulfur"],
            "Organic chemistry studies carbon compounds.",
        ),
        "chem_101": (
            "Which butane isomer has a branched chain?",
            ["Isobutane", "n-Butane", "Propane", "Pentane"],
            "Isobutane (2-methylpropane) is branched C₄H₁₀.",
        ),
        "chem_114": (
            "Which law states compounds have fixed composition?",
            ["Law of definite proportions", "Boyle's law", "Hooke's law", "Ohm's law"],
            "Proust formulated the law of definite proportions.",
        ),
        "chem_115": (
            "Which gas makes up most of Earth's atmosphere?",
            ["Nitrogen", "Oxygen", "Carbon dioxide", "Argon"],
            "Nitrogen is about 78% of air.",
        ),
        "chem_116": (
            "Which process separates liquids by boiling point?",
            ["Distillation", "Filtration", "Centrifugation", "Crystallization"],
            "Distillation uses different boiling points.",
        ),
        "chem_118": (
            "Which principle forbids identical quantum states for two electrons?",
            ["Pauli exclusion principle", "Heisenberg uncertainty", "Hund's rule", "Hess's law"],
            "Pauli exclusion applies to fermions including electrons.",
        ),
    }
    return {
        qid: q(qid, "chemistry", CHEMISTRY_REPLACE_RU[qid]["difficulty"], text, opts, expl)
        for qid, (text, opts, expl) in mapping.items()
    }


def build_en_physics_replacements() -> dict[str, dict[str, Any]]:
    mapping = {
        "phys_061": (
            "Which quantity is measured in pascals?",
            ["Pressure", "Force", "Energy", "Power"],
            "The pascal (Pa) is the SI unit of pressure.",
        ),
        "phys_063": (
            "Who formulated the three laws of mechanics?",
            ["Isaac Newton", "Albert Einstein", "Galileo Galilei", "Niels Bohr"],
            "Newton's laws underpin classical mechanics.",
        ),
        "phys_066": (
            "What force points toward the center in circular motion?",
            ["Centripetal", "Friction", "Elastic", "Buoyant"],
            "Centripetal force enables curved paths.",
        ),
        "phys_067": (
            "Who predicted antimatter?",
            ["Paul Dirac", "Ernest Rutherford", "Max Planck", "Werner Heisenberg"],
            "Dirac's equation allowed negative-energy solutions.",
        ),
        "phys_068": (
            "Which instrument measures electrical resistance?",
            ["Ohmmeter", "Ammeter", "Voltmeter", "Wattmeter"],
            "Resistance is measured in ohms.",
        ),
        "phys_069": (
            "In SI, work is measured in:",
            ["Joule", "Newton", "Watt", "Pascal"],
            "Work and energy use joules.",
        ),
        "phys_070": (
            "Which law relates current, voltage, and resistance?",
            ["Ohm's law", "Coulomb's law", "Hooke's law", "Pascal's law"],
            "U = I·R for a circuit segment.",
        ),
        "phys_071": (
            "What is bending of light at a boundary between media?",
            ["Refraction", "Diffraction", "Interference", "Polarization"],
            "Refraction changes light's direction.",
        ),
        "phys_074": (
            "Which particle carries current in metals?",
            ["Electron", "Proton", "Neutron", "Positron"],
            "Free electrons are charge carriers in conductors.",
        ),
        "phys_077": (
            "What is the kinetic energy formula?",
            ["mv²/2", "mgh", "Fs", "ma"],
            "Kinetic energy E = mv²/2.",
        ),
        "phys_079": (
            "Which wave type needs a material medium?",
            ["Sound", "Light", "X-ray", "Radio in vacuum"],
            "Sound is mechanical vibration of a medium.",
        ),
        "phys_081": (
            "Minimum energy to eject an electron from a metal is called:",
            ["Work function", "Binding energy", "Heat capacity", "Ionization potential"],
            "The photoelectric effect uses the work function.",
        ),
        "phys_083": (
            "Which law describes force between two point charges?",
            ["Coulomb's law", "Ohm's law", "Faraday's law", "Boyle's law"],
            "F = k·|q₁·q₂|/r².",
        ),
        "phys_085": (
            "What does a barometer measure?",
            ["Atmospheric pressure", "Temperature", "Humidity", "Wind speed"],
            "Barometers read air pressure.",
        ),
        "phys_086": (
            "Which visible color has the longest wavelength?",
            ["Red", "Violet", "Blue", "Green"],
            "Red light is about 650–700 nm.",
        ),
        "phys_087": (
            "What is the SI unit of frequency?",
            ["Hertz", "Newton", "Joule", "Volt"],
            "Hertz = cycles per second.",
        ),
        "phys_088": (
            "Ratio of light speed in vacuum to speed in a medium is:",
            ["Refractive index", "Friction coefficient", "Young's modulus", "Density"],
            "n = c/v.",
        ),
        "phys_089": (
            "Which device converts mechanical energy to electrical?",
            ["Generator", "Transformer", "Rheostat", "Capacitor"],
            "Generators produce electric current.",
        ),
        "phys_091": (
            "Approximate speed of sound in air at 20 °C?",
            ["343 m/s", "1500 m/s", "100 m/s", "3000 m/s"],
            "Sound is ~343 m/s in air at room temperature.",
        ),
        "phys_092": (
            "«Action equals reaction» is which law?",
            ["Newton's third law", "Newton's first law", "Energy conservation", "Hooke's law"],
            "Newton's third law describes force pairs.",
        ),
        "phys_093": (
            "Ability of materials to conduct electric current:",
            ["Electrical conductivity", "Thermal conductivity", "Magnetism", "Viscosity"],
            "Conductivity depends on charge carriers.",
        ),
        "phys_096": (
            "Who discovered radioactivity?",
            ["Henri Becquerel", "Marie Curie", "Ernest Rutherford", "James Chadwick"],
            "Becquerel found natural radioactivity in uranium salts.",
        ),
        "phys_097": (
            "Which lens focuses parallel rays at a focal point?",
            ["Converging (convex)", "Diverging (concave)", "Flat", "Cylindrical"],
            "A converging lens has positive optical power.",
        ),
        "phys_099": (
            "Which instrument measures electric current?",
            ["Ammeter", "Voltmeter", "Ohmmeter", "Galvanometer only DC"],
            "Ammeters are connected in series.",
        ),
        "phys_101": (
            "How many chromosomes in a human diploid cell?",
            ["46", "23", "48", "44"],
            "Somatic cells have 46 chromosomes (23 pairs).",
        ),
        "phys_102": (
            "At constant temperature, pressure is inversely proportional to volume:",
            ["Boyle's law", "Gay-Lussac's law", "Avogadro's law", "Hooke's law"],
            "p·V = const at constant T.",
        ),
        "phys_103": (
            "Constituent particles of protons and neutrons:",
            ["Quark", "Electron", "Neutrino", "Muon"],
            "Protons and neutrons are made of u and d quarks.",
        ),
    }
    return {
        qid: q(qid, "physics", PHYSICS_REPLACE_RU[qid]["difficulty"], text, opts, expl)
        for qid, (text, opts, expl) in mapping.items()
    }


# New questions to append per category (RU)
NEW_QUESTIONS_RU: dict[str, list[dict[str, Any]]] = {
    "chemistry": [
        q("chem_133", "chemistry", "EASY", "Какой газ используют для наполнения воздушных шаров?", ["Гелий", "Водород", "Азот", "Кислород"], "Гелий легче воздуха и негорюч."),
        q("chem_134", "chemistry", "MEDIUM", "Какой элемент входит в состав поваренной соли?", ["Натрий", "Калий", "Кальций", "Магний"], "NaCl — хлорид натрия."),
        q("chem_135", "chemistry", "HARD", "Какой катализатор ускоряет разложение пероксида водорода в школе?", ["Диоксид марганца", "Медь", "Золото", "Графит"], "MnO₂ катализирует 2H₂O₂ → 2H₂O + O₂."),
        q("chem_136", "chemistry", "EASY", "Какой цвет даёт лакмус в кислой среде?", ["Красный", "Синий", "Зелёный", "Жёлтый"], "Кислота красит лакмус в красный."),
        q("chem_137", "chemistry", "MEDIUM", "Какой элемент имеет символ Fe?", ["Железо", "Фтор", "Франций", "Фосфор"], "Fe — ferrum, железо."),
        q("chem_138", "chemistry", "MEDIUM", "Как называется реакция кислоты с основанием?", ["Нейтрализация", "Горение", "Полимеризация", "Ферментация"], "Кислота + основание → соль + вода."),
        q("chem_139", "chemistry", "HARD", "Какой газ пахнет тухлыми яйцами?", ["Сероводород", "Аммиак", "Хлор", "Метан"], "H₂S имеет характерный запах."),
        q("chem_140", "chemistry", "EASY", "Сколько валентных электронов у натрия?", ["1", "2", "7", "8"], "Натрий — элемент I группы, 1 валентный e."),
    ],
    "physics": [
        q("phys_116", "physics", "EASY", "Какая сила удерживает планеты на орбите Солнца?", ["Гравитация", "Магнетизм", "Трение", "Сила Архимеда"], "Гравитационное притяжение Солнца."),
        q("phys_117", "physics", "MEDIUM", "Какой прибор измеряет электрическое напряжение?", ["Вольтметр", "Амперметр", "Барометр", "Термометр"], "Вольтметр подключают параллельно."),
        q("phys_118", "physics", "HARD", "Как называется изменение направления волны при обходе препятствия?", ["Дифракция", "Рефлексия", "Рефракция", "Абсорбция"], "Дифракция — огибание препятствий."),
        q("phys_119", "physics", "MEDIUM", "Чему равно ускорение свободного падения g на Земле (прибл.)?", ["9,8 м/с²", "1 м/с²", "98 м/с²", "0,98 м/с²"], "g ≈ 9,8 м/с²."),
        q("phys_120", "physics", "EASY", "Какой цвет имеет наименьшую частоту в видимом спектре?", ["Красный", "Фиолетовый", "Синий", "Жёлтый"], "Красный — наименьшая частота."),
        q("phys_121", "physics", "MEDIUM", "Какой закон: F = k·Δx для пружины?", ["Закон Гука", "Закон Ома", "Закон Кулона", "Закон Паскаля"], "Упругая сила пропорциональна деформации."),
        q("phys_122", "physics", "HARD", "Какой элементарный заряд обозначают e?", ["Заряд электрона", "Энергия", "Масса", "Скорость"], "e ≈ 1,602·10⁻¹⁹ Кл."),
        q("phys_123", "physics", "MEDIUM", "Какой тип тока меняет направление периодически?", ["Переменный", "Постоянный", "Импульсный", "Статический"], "В переменном токе направление меняется."),
    ],
    "history": [
        q("hist_133", "history", "EASY", "В каком году началась Вторая мировая война?", ["1939", "1914", "1945", "1929"], "1 сентября 1939 — вторжение в Польшу."),
        q("hist_134", "history", "MEDIUM", "Кто был первым президентом США?", ["Джордж Вашингтон", "Томас Джефферсон", "Авраам Линкольн", "Бенджамин Франклин"], "Вашингтон — 1789–1797."),
        q("hist_135", "history", "HARD", "Как называлась династия, правившая Россией с 1613 года?", ["Романовы", "Рюриковичи", "Голицыны", "Вяземские"], "Дом Романовых до 1917 года."),
    ],
    "geography": [
        q("geog_132", "geography", "EASY", "Какая река самая длинная в мире?", ["Нил", "Амазонка", "Янцзы", "Миссисипи"], "Нил ~6650 км (по классическим оценкам)."),
        q("geog_133", "geography", "MEDIUM", "Столица Канады?", ["Оттава", "Торонто", "Ванкувер", "Монреаль"], "Оттава — политическая столица."),
        q("geog_134", "geography", "EASY", "На каком материке нет постоянного населения?", ["Антарктида", "Австралия", "Европа", "Африка"], "Антарктида — только станции."),
    ],
    "math": [
        q("math_132", "math", "EASY", "Сколько градусов в прямом угле?", ["90", "180", "45", "360"], "Прямой угол = 90°."),
        q("math_133", "math", "MEDIUM", "Чему равно 15% от 200?", ["30", "25", "35", "20"], "0,15 × 200 = 30."),
        q("math_134", "math", "HARD", "Как называется многоугольник с 8 сторонами?", ["Восьмиугольник", "Шестиугольник", "Пятиугольник", "Десятиугольник"], "Октогон — 8 сторон."),
    ],
    "informatics": [
        q("info_133", "informatics", "EASY", "Сколько бит в одном байте?", ["8", "4", "16", "32"], "1 байт = 8 бит."),
        q("info_134", "informatics", "MEDIUM", "Какой язык чаще используют для веб-страниц?", ["HTML", "Python", "C++", "SQL"], "HTML описывает структуру страницы."),
        q("info_135", "informatics", "HARD", "Как называется структура «последний вошёл — первый вышел»?", ["Стек", "Очередь", "Массив", "Дерево"], "LIFO — стек."),
    ],
    "astronomy": [
        q("astr_133", "astronomy", "EASY", "Как называется наша галактика?", ["Млечный Путь", "Андромеда", "Сомбреро", "Водоворот"], "Солнечная система в Млечном Пути."),
        q("astr_134", "astronomy", "MEDIUM", "Какая планета ближе всего к Солнцу?", ["Меркурий", "Венера", "Марс", "Земля"], "Меркурий — первая от Солнца."),
        q("astr_135", "astronomy", "HARD", "Что такое сверхновая?", ["Взрыв массивной звезды", "Чёрная дыра", "Комета", "Астероид"], "Катастрофический конец массивной звезды."),
    ],
    "animals": [
        q("anim_133", "animals", "EASY", "Какое млекопитающее умеет летать?", ["Летучая мышь", "Белка", "Кенгуру", "Выдра"], "Летучие мыши — единственные летающие млекопитающие."),
        q("anim_134", "animals", "MEDIUM", "Сколько сердец у осьминога?", ["3", "1", "2", "4"], "Два жаберных и одно системное."),
        q("anim_135", "animals", "EASY", "Какое животное — символ Австралии?", ["Кенгуру", "Панда", "Слон", "Жираф"], "Кенгуру на гербе Австралии."),
    ],
    "art": [
        q("art_114", "art", "EASY", "Кто написал «Мону Лизу»?", ["Леонардо да Винчи", "Пабло Пикассо", "Клод Моне", "Рафаэль"], "Шедевр Леонардо в Лувре."),
        q("art_115", "art", "MEDIUM", "Какой стиль связан с Винсентом ван Гогом?", ["Постимпрессионизм", "Кубизм", "Барокко", "Рококо"], "Ван Гог — постимпрессионист."),
        q("art_116", "art", "EASY", "Какой цвет получают при смешении синего и жёлтого?", ["Зелёный", "Оранжевый", "Фиолетовый", "Красный"], "Субтрактивное смешение пигментов."),
    ],
    "movies": [
        q("movi_133", "movies", "EASY", "Кто режиссёр фильма «Начало» (2010)?", ["Кристофер Нолан", "Стивен Спилберг", "Квентин Тарантино", "Джеймс Кэмерон"], "Inception — Нолан."),
        q("movi_134", "movies", "MEDIUM", "В каком фильме звучит «I'll be back»?", ["Терминатор", "Хищник", "Чужой", "Робокоп"], "Фраза Арнольда Шварценеггера."),
        q("movi_135", "movies", "EASY", "Как зовут героя «Матрицы»?", ["Нео", "Морфеус", "Тринити", "Смит"], "Томас Андерсон — Нео."),
    ],
}

NEW_QUESTIONS_EN: dict[str, list[dict[str, Any]]] = {
    "chemistry": [
        q("chem_133", "chemistry", "EASY", "Which gas fills party balloons safely?", ["Helium", "Hydrogen", "Nitrogen", "Oxygen"], "Helium is lighter than air and nonflammable."),
        q("chem_134", "chemistry", "MEDIUM", "Which element is in table salt?", ["Sodium", "Potassium", "Calcium", "Magnesium"], "NaCl is sodium chloride."),
        q("chem_135", "chemistry", "HARD", "Which catalyst speeds H₂O₂ decomposition in school demos?", ["Manganese dioxide", "Copper", "Gold", "Graphite"], "MnO₂ catalyzes 2H₂O₂ → 2H₂O + O₂."),
        q("chem_136", "chemistry", "EASY", "Litmus color in acid?", ["Red", "Blue", "Green", "Yellow"], "Acid turns litmus red."),
        q("chem_137", "chemistry", "MEDIUM", "Symbol Fe stands for:", ["Iron", "Fluorine", "Francium", "Phosphorus"], "Fe is ferrum — iron."),
        q("chem_138", "chemistry", "MEDIUM", "Acid + base reaction is called:", ["Neutralization", "Combustion", "Polymerization", "Fermentation"], "Acid + base → salt + water."),
        q("chem_139", "chemistry", "HARD", "Which gas smells like rotten eggs?", ["Hydrogen sulfide", "Ammonia", "Chlorine", "Methane"], "H₂S has a distinctive odor."),
        q("chem_140", "chemistry", "EASY", "How many valence electrons does sodium have?", ["1", "2", "7", "8"], "Sodium is group 1 — one valence electron."),
    ],
    "physics": [
        q("phys_116", "physics", "EASY", "What force keeps planets in orbit around the Sun?", ["Gravity", "Magnetism", "Friction", "Buoyancy"], "Solar gravitational attraction."),
        q("phys_117", "physics", "MEDIUM", "Which instrument measures voltage?", ["Voltmeter", "Ammeter", "Barometer", "Thermometer"], "Voltmeters connect in parallel."),
        q("phys_118", "physics", "HARD", "Wave bending around obstacles is called:", ["Diffraction", "Reflection", "Refraction", "Absorption"], "Diffraction is wave spreading."),
        q("phys_119", "physics", "MEDIUM", "Approximate g on Earth?", ["9.8 m/s²", "1 m/s²", "98 m/s²", "0.98 m/s²"], "g ≈ 9.8 m/s²."),
        q("phys_120", "physics", "EASY", "Lowest frequency visible color?", ["Red", "Violet", "Blue", "Yellow"], "Red has the lowest frequency."),
        q("phys_121", "physics", "MEDIUM", "F = k·Δx describes which law?", ["Hooke's law", "Ohm's law", "Coulomb's law", "Pascal's law"], "Elastic force ∝ deformation."),
        q("phys_122", "physics", "HARD", "Elementary charge e is:", ["Electron charge", "Energy", "Mass", "Speed"], "e ≈ 1.602×10⁻¹⁹ C."),
        q("phys_123", "physics", "MEDIUM", "Current that reverses direction periodically:", ["Alternating", "Direct", "Pulsed", "Static"], "AC changes direction."),
    ],
    "history": [
        q("hist_133", "history", "EASY", "When did World War II begin?", ["1939", "1914", "1945", "1929"], "Germany invaded Poland Sept 1, 1939."),
        q("hist_134", "history", "MEDIUM", "First US president?", ["George Washington", "Thomas Jefferson", "Abraham Lincoln", "Benjamin Franklin"], "Washington 1789–1797."),
        q("hist_135", "history", "HARD", "Dynasty ruling Russia from 1613?", ["Romanov", "Rurikid", "Golitsyn", "Vyazemsky"], "House of Romanov until 1917."),
    ],
    "geography": [
        q("geog_132", "geography", "EASY", "Longest river in the world (classic estimate)?", ["Nile", "Amazon", "Yangtze", "Mississippi"], "Nile ~6650 km."),
        q("geog_133", "geography", "MEDIUM", "Capital of Canada?", ["Ottawa", "Toronto", "Vancouver", "Montreal"], "Ottawa is the capital."),
        q("geog_134", "geography", "EASY", "Continent with no permanent population?", ["Antarctica", "Australia", "Europe", "Africa"], "Only research stations."),
    ],
    "math": [
        q("math_132", "math", "EASY", "Degrees in a right angle?", ["90", "180", "45", "360"], "Right angle = 90°."),
        q("math_133", "math", "MEDIUM", "15% of 200 equals?", ["30", "25", "35", "20"], "0.15 × 200 = 30."),
        q("math_134", "math", "HARD", "Polygon with 8 sides?", ["Octagon", "Hexagon", "Pentagon", "Decagon"], "Octagon has eight sides."),
    ],
    "informatics": [
        q("info_133", "informatics", "EASY", "Bits in one byte?", ["8", "4", "16", "32"], "1 byte = 8 bits."),
        q("info_134", "informatics", "MEDIUM", "Language for web page structure?", ["HTML", "Python", "C++", "SQL"], "HTML structures pages."),
        q("info_135", "informatics", "HARD", "LIFO structure name?", ["Stack", "Queue", "Array", "Tree"], "Last in, first out — stack."),
    ],
    "astronomy": [
        q("astr_133", "astronomy", "EASY", "Our galaxy's name?", ["Milky Way", "Andromeda", "Sombrero", "Whirlpool"], "Solar system is in the Milky Way."),
        q("astr_134", "astronomy", "MEDIUM", "Planet closest to the Sun?", ["Mercury", "Venus", "Mars", "Earth"], "Mercury is innermost."),
        q("astr_135", "astronomy", "HARD", "What is a supernova?", ["Massive star explosion", "Black hole", "Comet", "Asteroid"], "Catastrophic end of a massive star."),
    ],
    "animals": [
        q("anim_133", "animals", "EASY", "Flying mammal?", ["Bat", "Squirrel", "Kangaroo", "Otter"], "Bats are the only flying mammals."),
        q("anim_134", "animals", "MEDIUM", "How many hearts does an octopus have?", ["3", "1", "2", "4"], "Two branchial and one systemic."),
        q("anim_135", "animals", "EASY", "Iconic animal of Australia?", ["Kangaroo", "Panda", "Elephant", "Giraffe"], "Kangaroo on Australia's coat of arms."),
    ],
    "art": [
        q("art_114", "art", "EASY", "Who painted the Mona Lisa?", ["Leonardo da Vinci", "Pablo Picasso", "Claude Monet", "Raphael"], "Leonardo's masterpiece in the Louvre."),
        q("art_115", "art", "MEDIUM", "Van Gogh is associated with:", ["Post-Impressionism", "Cubism", "Baroque", "Rococo"], "Van Gogh was a Post-Impressionist."),
        q("art_116", "art", "EASY", "Blue + yellow pigment gives:", ["Green", "Orange", "Purple", "Red"], "Subtractive color mixing."),
    ],
    "movies": [
        q("movi_133", "movies", "EASY", "Director of Inception (2010)?", ["Christopher Nolan", "Steven Spielberg", "Quentin Tarantino", "James Cameron"], "Inception is by Nolan."),
        q("movi_134", "movies", "MEDIUM", "Which film has «I'll be back»?", ["The Terminator", "Predator", "Alien", "RoboCop"], "Schwarzenegger's famous line."),
        q("movi_135", "movies", "EASY", "Hero of The Matrix?", ["Neo", "Morpheus", "Trinity", "Smith"], "Thomas Anderson is Neo."),
    ],
}


def process_file(filename: str) -> None:
    path = os.path.join(ASSETS, filename)
    with open(path, encoding="utf-8") as f:
        data = json.load(f)

    is_en = filename.endswith("_en.json")
    category = filename.removeprefix("questions_").removesuffix("_en.json").removesuffix(".json")

    if category == "chemistry":
        if is_en:
            apply_replacements(data, build_en_chemistry_replacements())
            apply_patches(data, CHEMISTRY_PATCH_EN)
        else:
            apply_replacements(data, CHEMISTRY_REPLACE_RU)
            apply_patches(data, CHEMISTRY_PATCH_RU)
    elif category == "physics":
        if is_en:
            apply_replacements(data, build_en_physics_replacements())
            apply_patches(data, PHYSICS_PATCH_EN)
        else:
            apply_replacements(data, PHYSICS_REPLACE_RU)
            apply_patches(data, PHYSICS_PATCH_RU)

    for patch_file, patches in MATH_PATCH.items():
        if filename == patch_file:
            apply_patches(data, patches)
    for patch_file, patches in ANIMALS_PATCH.items():
        if filename == patch_file:
            apply_patches(data, patches)
    for patch_file, patches in MOVIES_PATCH.items():
        if filename == patch_file:
            apply_patches(data, patches)

    # Trim whitespace in EN files
    if is_en:
        data = trim_strings(data)

    # Append new questions
    existing_ids = {item["id"] for item in data}
    new_pool = NEW_QUESTIONS_EN if is_en else NEW_QUESTIONS_RU
    for new_q in new_pool.get(category, []):
        if new_q["id"] not in existing_ids:
            data.append(new_q)
            existing_ids.add(new_q["id"])

    with open(path, "w", encoding="utf-8") as f:
        json.dump(data, f, ensure_ascii=False, indent=2)
        f.write("\n")


def main() -> None:
    for name in sorted(os.listdir(ASSETS)):
        if name.startswith("questions_") and name.endswith(".json"):
            process_file(name)
            print(f"Updated {name}")


if __name__ == "__main__":
    main()