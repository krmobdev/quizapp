#!/usr/bin/env python3
"""Generate English question bank JSON files (400 questions each)."""

from __future__ import annotations

import json
import random
from pathlib import Path

import generate_banks_en as gb_en

ASSETS = Path(__file__).resolve().parent.parent / "app" / "src" / "main" / "assets"
TARGET = 400

CATEGORIES = {
    "chemistry": "chem",
    "physics": "physics",
    "history": "hist",
    "movies": "mov",
    "art": "art",
    "animals": "anim",
    "geography": "geo",
    "math": "math",
    "informatics": "info",
    "astronomy": "astro",
}


def make_question(
    category: str,
    text: str,
    options: list[str],
    correct_index: int,
    explanation: str,
    difficulty: str,
) -> dict:
    return {
        "category": category,
        "difficulty": difficulty,
        "text": text,
        "options": options,
        "correctIndex": correct_index,
        "explanation": explanation,
    }


def add_unique(pool: list[dict], seen: set[str], candidate: dict) -> bool:
    key = candidate["text"].strip().lower()
    if key in seen:
        return False
    seen.add(key)
    pool.append(candidate)
    return True


def generate_from_bank(category: str, bank: list[tuple], seen: set[str], pool: list[dict], need: int) -> None:
    random.shuffle(bank)
    for text, options, correct, expl, diff in bank:
        q = make_question(category, text, options, correct, expl, diff)
        if add_unique(pool, seen, q) and len(pool) >= need:
            return


# ---------------------------------------------------------------------------
# Chemistry
# ---------------------------------------------------------------------------

ELEMENTS = [
    ("Hydrogen", "H", 1), ("Helium", "He", 2), ("Lithium", "Li", 3), ("Beryllium", "Be", 4),
    ("Boron", "B", 5), ("Carbon", "C", 6), ("Nitrogen", "N", 7), ("Oxygen", "O", 8),
    ("Fluorine", "F", 9), ("Neon", "Ne", 10), ("Sodium", "Na", 11), ("Magnesium", "Mg", 12),
    ("Aluminum", "Al", 13), ("Silicon", "Si", 14), ("Phosphorus", "P", 15), ("Sulfur", "S", 16),
    ("Chlorine", "Cl", 17), ("Argon", "Ar", 18), ("Potassium", "K", 19), ("Calcium", "Ca", 20),
    ("Scandium", "Sc", 21), ("Titanium", "Ti", 22), ("Vanadium", "V", 23), ("Chromium", "Cr", 24),
    ("Manganese", "Mn", 25), ("Iron", "Fe", 26), ("Cobalt", "Co", 27), ("Nickel", "Ni", 28),
    ("Copper", "Cu", 29), ("Zinc", "Zn", 30), ("Gallium", "Ga", 31), ("Germanium", "Ge", 32),
    ("Arsenic", "As", 33), ("Selenium", "Se", 34), ("Bromine", "Br", 35), ("Krypton", "Kr", 36),
    ("Rubidium", "Rb", 37), ("Strontium", "Sr", 38), ("Yttrium", "Y", 39), ("Zirconium", "Zr", 40),
    ("Niobium", "Nb", 41), ("Molybdenum", "Mo", 42), ("Silver", "Ag", 47), ("Cadmium", "Cd", 48),
    ("Indium", "In", 49), ("Tin", "Sn", 50), ("Antimony", "Sb", 51), ("Iodine", "I", 53),
    ("Xenon", "Xe", 54), ("Cesium", "Cs", 55), ("Barium", "Ba", 56), ("Tungsten", "W", 74),
    ("Platinum", "Pt", 78), ("Gold", "Au", 79), ("Mercury", "Hg", 80), ("Lead", "Pb", 82),
    ("Uranium", "U", 92),
]

COMPOUNDS = [
    ("Methane", "CH₄", "EASY"), ("Ethane", "C₂H₆", "MEDIUM"), ("Propane", "C₃H₈", "MEDIUM"),
    ("Butane", "C₄H₁₀", "MEDIUM"), ("Ethylene", "C₂H₄", "MEDIUM"), ("Acetylene", "C₂H₂", "HARD"),
    ("Methanol", "CH₃OH", "MEDIUM"), ("Ethanol", "C₂H₅OH", "MEDIUM"), ("Glucose", "C₆H₁₂O₆", "HARD"),
    ("Sodium hydroxide", "NaOH", "MEDIUM"), ("Potassium hydroxide", "KOH", "MEDIUM"),
    ("Nitric acid", "HNO₃", "MEDIUM"), ("Hydrochloric acid", "HCl", "EASY"),
    ("Carbonic acid", "H₂CO₃", "MEDIUM"), ("Phosphoric acid", "H₃PO₄", "HARD"),
    ("Calcium oxide", "CaO", "MEDIUM"), ("Iron(III) oxide", "Fe₂O₃", "HARD"),
    ("Potassium chloride", "KCl", "MEDIUM"), ("Copper sulfate", "CuSO₄", "MEDIUM"),
    ("Silver nitrate", "AgNO₃", "HARD"), ("Hydrogen peroxide", "H₂O₂", "MEDIUM"),
    ("Sodium bicarbonate", "NaHCO₃", "MEDIUM"), ("Sodium sulfate", "Na₂SO₄", "MEDIUM"),
    ("Calcium fluoride", "CaF₂", "HARD"), ("Sodium silicate", "Na₂SiO₃", "HARD"),
    ("Ammonium nitrate", "NH₄NO₃", "HARD"), ("Gypsum", "CaSO₄·2H₂O", "HARD"),
    ("Chalk", "CaCO₃", "EASY"), ("Borax", "Na₂B₄O₇", "HARD"), ("Table salt", "NaCl", "EASY"),
]

CHEM_FACTS = [
    ("Which gas is needed for combustion?", ["Nitrogen", "Oxygen", "Helium", "Argon"], 1, "Combustion requires oxygen.", "EASY"),
    ("Which gas makes up most of the atmosphere?", ["Oxygen", "Nitrogen", "Carbon dioxide", "Hydrogen"], 1, "Nitrogen is about 78% of the atmosphere.", "EASY"),
    ("What is the reaction of a substance with oxygen called?", ["Hydrolysis", "Oxidation", "Distillation", "Sublimation"], 1, "Oxidation is reaction with oxygen.", "EASY"),
    ("What is the pH of a neutral solution?", ["0", "7", "10", "14"], 1, "Neutral pH equals 7.", "EASY"),
    ("What is the pH of a strong acid (approx.)?", ["1–3", "7", "10–12", "14"], 0, "Strong acids have low pH.", "MEDIUM"),
    ("What is the pH of a base (approx.)?", ["1", "5", "7", "11–13"], 3, "Bases have pH above 7.", "MEDIUM"),
    ("What is a catalyst?", ["Speeds reaction without being consumed", "Slows reaction", "Is a product", "Absorbs heat"], 0, "A catalyst speeds up a reaction.", "MEDIUM"),
    ("Direct transition from solid to gas?", ["Melting", "Boiling", "Sublimation", "Condensation"], 2, "Sublimation: solid → gas.", "MEDIUM"),
    ("Transition from gas to liquid?", ["Evaporation", "Condensation", "Sublimation", "Diffusion"], 1, "Condensation: gas → liquid.", "EASY"),
    ("Which element is in all organic compounds?", ["Nitrogen", "Carbon", "Oxygen", "Sulfur"], 1, "Organic chemistry studies carbon compounds.", "EASY"),
    ("What is the formula H₂O?", ["Hydrogen", "Oxygen", "Water", "Peroxide"], 2, "H₂O is water.", "EASY"),
    ("Which gas is often released when acid reacts with metal?", ["Nitrogen", "Hydrogen", "Helium", "Argon"], 1, "Acid + metal → salt + hydrogen.", "MEDIUM"),
    ("Product of acid and base reaction?", ["Salt", "Oxide", "Alcohol", "Ether"], 0, "Neutralization gives salt and water.", "EASY"),
    ("Which metal is liquid at room temperature?", ["Iron", "Mercury", "Copper", "Aluminum"], 1, "Mercury is liquid at 20°C.", "MEDIUM"),
    ("Which gas is used in balloons (light)?", ["Oxygen", "Nitrogen", "Helium", "Chlorine"], 2, "Helium is lighter than air.", "EASY"),
    ("Separation of a mixture by boiling?", ["Filtration", "Distillation", "Electrolysis", "Crystallization"], 1, "Distillation uses different boiling points.", "MEDIUM"),
    ("Which element is Au?", ["Silver", "Gold", "Aluminum", "Copper"], 1, "Au is gold (Latin: aurum).", "EASY"),
    ("Which element is Ag?", ["Gold", "Silver", "Argon", "Aluminum"], 1, "Ag is silver (Latin: argentum).", "EASY"),
    ("Which element is Fe?", ["Fluorine", "Iron", "Phosphorus", "Francium"], 1, "Fe is iron (Latin: ferrum).", "EASY"),
    ("Which element is Cu?", ["Cobalt", "Copper", "Potassium", "Krypton"], 1, "Cu is copper (Latin: cuprum).", "EASY"),
    ("Which element is Pb?", ["Platinum", "Lead", "Palladium", "Polonium"], 1, "Pb is lead (Latin: plumbum).", "EASY"),
    ("Which element is Sn?", ["Sulfur", "Tin", "Selenium", "Strontium"], 1, "Sn is tin (Latin: stannum).", "EASY"),
    ("Which element is K?", ["Calcium", "Potassium", "Krypton", "Cobalt"], 1, "K is potassium (Latin: kalium).", "EASY"),
    ("Which element is Na?", ["Neon", "Sodium", "Nitrogen", "Nickel"], 1, "Na is sodium (Latin: natrium).", "EASY"),
    ("Which element is W?", ["Tungsten", "Vanadium", "Hydrogen", "Bismuth"], 0, "W is tungsten (German: Wolfram).", "MEDIUM"),
    ("Which element is Hg?", ["Helium", "Mercury", "Hafnium", "Holmium"], 1, "Hg is mercury (Latin: hydrargyrum).", "MEDIUM"),
    ("Valence electrons in noble gases (except He)?", ["2", "4", "6", "8"], 3, "Noble gases (except He) have 8 valence electrons.", "HARD"),
    ("Bond type in NaCl?", ["Covalent", "Ionic", "Metallic", "Hydrogen"], 1, "NaCl is typical ionic bonding.", "MEDIUM"),
    ("Bond type in O₂?", ["Ionic", "Covalent", "Metallic", "Van der Waals"], 1, "O₂ has covalent bonding.", "MEDIUM"),
    ("Smallest particle of an element?", ["Molecule", "Atom", "Ion", "Radical"], 1, "An atom is the smallest unit of an element.", "EASY"),
    ("What is an ion?", ["Atom", "Molecule", "Charged particle", "Isotope"], 2, "An ion is an atom or group with a charge.", "MEDIUM"),
    ("What is an isotope?", ["Atoms with different neutrons", "Different elements", "Water molecules", "Metal ions"], 0, "Isotopes: same element, different neutrons.", "HARD"),
    ("Which gas is a greenhouse gas?", ["Nitrogen", "Oxygen", "CO₂", "Argon"], 2, "Carbon dioxide is a greenhouse gas.", "MEDIUM"),
    ("Which metal is used in thermometers?", ["Iron", "Mercury", "Zinc", "Magnesium"], 1, "Mercury expands evenly when heated.", "EASY"),
    ("Decomposition of water by electricity?", ["Hydrolysis", "Electrolysis", "Hydration", "Dehydration"], 1, "Electrolysis uses electric current.", "MEDIUM"),
    ("Products of complete hydrocarbon combustion?", ["CO and H₂O", "CO₂ and H₂O", "Only CO₂", "Only H₂O"], 1, "Complete combustion: CO₂ + H₂O.", "MEDIUM"),
    ("Indicator that turns red in acid?", ["Phenolphthalein", "Litmus", "Methyl orange", "Both litmus and methyl orange"], 3, "Litmus and methyl orange turn red in acid.", "HARD"),
    ("Indicator that turns blue/pink in base?", ["Litmus", "Phenolphthalein", "Methyl orange", "Iodine"], 1, "Phenolphthalein is pink in base.", "MEDIUM"),
    ("Formula of sulfuric acid?", ["H₂SO₄", "HNO₃", "HCl", "H₃PO₄"], 0, "Sulfuric acid is H₂SO₄.", "MEDIUM"),
    ("Gas that smells like rotten eggs?", ["Ammonia", "Hydrogen sulfide", "Chlorine", "Methane"], 1, "H₂S smells like rotten eggs.", "MEDIUM"),
    ("Gas used to disinfect water?", ["Nitrogen", "Chlorine", "Helium", "Methane"], 1, "Chlorine disinfects water.", "MEDIUM"),
    ("Alcohol with formula C₂H₅OH?", ["Methanol", "Ethanol", "Glycerin", "Phenol"], 1, "Ethanol is drinking/technical alcohol.", "EASY"),
    ("Which alcohol is toxic?", ["Ethanol", "Methanol", "Glycerin", "Propanol"], 1, "Methanol (CH₃OH) is dangerous.", "HARD"),
    ("Sugar found in grapes and fruits?", ["Sucrose", "Glucose", "Starch", "Cellulose"], 1, "Glucose is a simple sugar.", "MEDIUM"),
    ("Polymer known as 'organic glass'?", ["Polyethylene", "Polystyrene", "PMMA", "PVC"], 2, "Plexiglas is polymethyl methacrylate.", "HARD"),
    ("Gas released by plants in photosynthesis?", ["CO₂", "O₂", "N₂", "H₂"], 1, "Photosynthesis releases oxygen.", "EASY"),
    ("Gas absorbed by plants in photosynthesis?", ["O₂", "N₂", "CO₂", "H₂"], 2, "Plants use CO₂.", "EASY"),
    ("Process of iron 'rusting'?", ["Oxidation", "Reduction", "Hydration", "Polymerization"], 0, "Rust is iron oxide.", "EASY"),
    ("Metal coated with zinc against corrosion?", ["Gold", "Iron", "Copper", "Silver"], 1, "Galvanizing protects iron.", "MEDIUM"),
    ("Mixture of a metal with other elements?", ["Salt", "Alloy", "Solution", "Suspension"], 1, "An alloy is a metal mixture.", "EASY"),
    ("Brass is mainly made of?", ["Iron and carbon", "Copper and zinc", "Aluminum and magnesium", "Lead and tin"], 1, "Brass is copper and zinc.", "HARD"),
    ("Classic bronze is made of?", ["Copper and tin", "Copper and zinc", "Iron and nickel", "Aluminum and silicon"], 0, "Bronze is copper + tin.", "MEDIUM"),
    ("Element needed for bones and teeth?", ["Sodium", "Calcium", "Potassium", "Magnesium"], 1, "Calcium is vital for bone tissue.", "EASY"),
    ("Element in hemoglobin?", ["Magnesium", "Iron", "Zinc", "Copper"], 1, "Iron binds oxygen in blood.", "MEDIUM"),
    ("Gas used in red neon signs?", ["Argon", "Neon", "Xenon", "Krypton"], 1, "Neon gives red glow.", "MEDIUM"),
    ("Avogadro's number (approx.)?", ["6·10²³", "3·10⁸", "9.8", "6.02·10²⁴"], 0, "6.02·10²³ particles per mole.", "HARD"),
    ("Amount of substance in moles?", ["Mass", "Volume", "Amount of substance", "Density"], 2, "The mole measures amount of substance.", "HARD"),
    ("Law: mass of reactants = mass of products?", ["Law of conservation of mass", "Ohm's law", "Boyle's law", "Hooke's law"], 0, "Law of conservation of mass — Lavoisier.", "MEDIUM"),
    ("Reaction that releases heat?", ["Endothermic", "Exothermic", "Reversible", "Catalytic"], 1, "Exothermic reactions release heat.", "MEDIUM"),
    ("Reaction that absorbs heat?", ["Endothermic", "Exothermic", "Hydrolysis", "Polymerization"], 0, "Endothermic reactions absorb heat.", "MEDIUM"),
    ("Lightest element?", ["Helium", "Hydrogen", "Lithium", "Carbon"], 1, "Hydrogen is the lightest element.", "EASY"),
    ("Heaviest stable element?", ["Uranium", "Lead", "Tungsten", "Mercury"], 1, "Lead ends the stable elements.", "HARD"),
    ("Name of the elements table?", ["Dalton's table", "Periodic table", "Newton's table", "Boyle's table"], 1, "Mendeleev's periodic table.", "EASY"),
    ("How many periods in the periodic table?", ["5", "6", "7", "8"], 2, "There are 7 periods.", "MEDIUM"),
    ("Gas formed when hydrogen burns?", ["Nitrogen", "Water (steam)", "CO₂", "Methane"], 1, "2H₂ + O₂ → 2H₂O.", "MEDIUM"),
    ("Acid in the stomach?", ["Sulfuric", "Hydrochloric", "Acetic", "Citric"], 1, "Hydrochloric acid in gastric juice.", "MEDIUM"),
    ("Vitamin in ascorbic acid?", ["A", "B", "C", "D"], 2, "Vitamin C is ascorbic acid.", "MEDIUM"),
    ("Formation of a precipitate?", ["Distillation", "Precipitation", "Sublimation", "Evaporation"], 1, "Precipitation — solid forms.", "MEDIUM"),
    ("Metal used in beverage cans?", ["Iron", "Aluminum", "Tin", "Zinc"], 1, "Cans are made of aluminum.", "EASY"),
    ("Main component of natural gas?", ["Ethane", "Methane", "Propane", "Butane"], 1, "Methane CH₄ is the main component.", "MEDIUM"),
    ("Scientific name for table salt?", ["Sodium chloride", "Sodium sulfate", "Sodium nitrate", "Sodium carbonate"], 0, "NaCl is sodium chloride.", "EASY"),
    ("Product at cathode in water electrolysis?", ["Oxygen", "Hydrogen", "Nitrogen", "Chlorine"], 1, "Hydrogen forms at the cathode.", "HARD"),
    ("Product at anode in water electrolysis?", ["Hydrogen", "Oxygen", "Nitrogen", "CO₂"], 1, "Oxygen forms at the anode.", "HARD"),
    ("Element in a diamond?", ["Silicon", "Carbon", "Boron", "Nitrogen"], 1, "Diamond is a form of carbon.", "EASY"),
    ("Element in quartz?", ["Carbon", "Silicon", "Aluminum", "Iron"], 1, "SiO₂ is silicon dioxide.", "MEDIUM"),
    ("Acid in vinegar?", ["Lactic", "Acetic", "Oxalic", "Tartaric"], 1, "Vinegar is dilute acetic acid.", "EASY"),
    ("Process of forming polymers?", ["Hydrolysis", "Polymerization", "Distillation", "Crystallization"], 1, "Polymerization joins monomers.", "MEDIUM"),
    ("Formula of ammonia?", ["NH₃", "NO₂", "N₂O", "HNO₃"], 0, "Ammonia is NH₃.", "MEDIUM"),
    ("Smell of ammonia?", ["Sweet", "Sharp alkaline", "Rotten eggs", "Odorless"], 1, "NH₃ has a sharp smell.", "MEDIUM"),
    ("Nitrogen fertilizer example?", ["Superphosphate", "Ammonium nitrate", "Potash", "Lime"], 1, "NH₄NO₃ is a nitrogen fertilizer.", "HARD"),
    ("Formula of ozone?", ["O", "O₂", "O₃", "O₄"], 2, "Ozone is O₃.", "MEDIUM"),
    ("Layer containing ozone?", ["Troposphere", "Stratosphere", "Mesosphere", "Thermosphere"], 1, "Ozone layer in the stratosphere.", "HARD"),
    ("Reaction splitting into simpler substances?", ["Synthesis", "Decomposition", "Substitution", "Exchange"], 1, "Decomposition breaks compounds apart.", "MEDIUM"),
    ("Reaction combining simple substances?", ["Synthesis", "Hydrolysis", "Distillation", "Crystallization"], 0, "Synthesis forms a complex substance.", "MEDIUM"),
    ("Metal that reacts vigorously with acid?", ["Gold", "Sodium", "Copper", "Silver"], 1, "Alkali metals are very reactive.", "MEDIUM"),
    ("Metal that does not react with dilute HCl?", ["Zinc", "Iron", "Copper", "Magnesium"], 2, "Copper does not displace hydrogen from HCl.", "HARD"),
    ("Transfer of electrons between reactants?", ["Redox", "Acid-base", "Polymerization", "Hydration"], 0, "Redox involves electron transfer.", "HARD"),
    ("Basis of silicon electronics?", ["Carbon", "Silicon", "Germanium", "Copper"], 1, "Silicon is a semiconductor.", "MEDIUM"),
    ("CO₂ used in fire extinguishers?", ["Oxygen", "CO₂", "Methane", "Hydrogen"], 1, "CO₂ displaces oxygen.", "MEDIUM"),
    ("Acid + base reaction called?", ["Neutralization", "Combustion", "Polymerization", "Fermentation"], 0, "Acid + base → salt + water.", "EASY"),
    ("Element in all proteins?", ["Only carbon", "C, H, O, N", "Only nitrogen", "Only oxygen"], 1, "Proteins contain C, H, O, N and more.", "MEDIUM"),
    ("Sugar fermentation process?", ["Distillation", "Fermentation", "Electrolysis", "Sublimation"], 1, "Fermentation is microbial breakdown.", "MEDIUM"),
    ("Product of grape fermentation?", ["Beer", "Wine", "Bread", "Cheese"], 1, "Wine comes from fermentation.", "EASY"),
    ("Gas released when dough rises?", ["Oxygen", "CO₂", "Nitrogen", "Hydrogen"], 1, "Yeast releases CO₂.", "EASY"),
    ("Saturated solution means?", ["Excess solute", "Max concentration at given T", "Empty solution", "Concentrated acid"], 1, "No more solute dissolves.", "HARD"),
    ("Universal solvent?", ["Oil", "Water", "Alcohol", "Gasoline"], 1, "Water is the most important solvent.", "EASY"),
    ("Homogeneous mixture?", ["Suspension", "Solution", "Precipitate", "Crystal"], 1, "A solution is homogeneous.", "EASY"),
    ("Heterogeneous mixture with visible particles?", ["Solution", "Suspension", "Emulsion", "Alloy"], 1, "A suspension has visible particles.", "MEDIUM"),
    ("Flame color of copper salts?", ["Yellow", "Green", "Red", "Blue"], 1, "Copper gives a greenish flame.", "HARD"),
    ("Flame color of sodium?", ["Green", "Yellow", "Red", "Violet"], 1, "Sodium has a characteristic yellow flame.", "HARD"),
    ("Flame color of potassium?", ["Yellow", "Green", "Lilac", "Orange"], 2, "Potassium gives a lilac flame.", "HARD"),
    ("Flame color of calcium?", ["Red", "Brick red", "Blue", "White"], 1, "Calcium gives a brick-red flame.", "HARD"),
    ("NaOH is also called?", ["Soda", "Caustic soda", "Chalk", "Saltpeter"], 1, "NaOH is caustic soda.", "MEDIUM"),
    ("NaHCO₃ in everyday use?", ["Soda", "Baking soda", "Saltpeter", "Salt"], 1, "Baking soda is sodium bicarbonate.", "EASY"),
    ("Element in all acids (classically)?", ["Oxygen", "Hydrogen", "Nitrogen", "Carbon"], 1, "Classically acids contain hydrogen.", "MEDIUM"),
    ("Element symbol Si?", ["Sulfur", "Silicon", "Selenium", "Scandium"], 1, "Si is silicon.", "EASY"),
    ("Element symbol S?", ["Selenium", "Sulfur", "Scandium", "Silver"], 1, "S is sulfur.", "EASY"),
    ("Element symbol P?", ["Platinum", "Phosphorus", "Polonium", "Praseodymium"], 1, "P is phosphorus.", "EASY"),
    ("Element symbol Cl?", ["Chromium", "Chlorine", "Calcium", "Cobalt"], 1, "Cl is chlorine.", "EASY"),
    ("Element symbol Zn?", ["Zirconium", "Zinc", "Gold", "Xenon"], 1, "Zn is zinc.", "EASY"),
    ("Element symbol Al?", ["Argon", "Aluminum", "Actinium", "Astatine"], 1, "Al is aluminum.", "EASY"),
    ("Element symbol Mg?", ["Manganese", "Magnesium", "Molybdenum", "Copper"], 1, "Mg is magnesium.", "EASY"),
    ("Element symbol Ca?", ["Calcium", "Potassium", "Cadmium", "Cobalt"], 0, "Ca is calcium.", "EASY"),
    ("Element symbol Ti?", ["Thallium", "Titanium", "Thorium", "Tellurium"], 1, "Ti is titanium.", "MEDIUM"),
    ("Element symbol Cr?", ["Chromium", "Chlorine", "Cesium", "Krypton"], 0, "Cr is chromium.", "MEDIUM"),
    ("Element symbol Mn?", ["Magnesium", "Manganese", "Molybdenum", "Copper"], 1, "Mn is manganese.", "MEDIUM"),
    ("Element symbol Ni?", ["Sodium", "Nickel", "Neon", "Niobium"], 1, "Ni is nickel.", "MEDIUM"),
    ("Element symbol Co?", ["Cobalt", "Calcium", "Krypton", "Cadmium"], 0, "Co is cobalt.", "MEDIUM"),
    ("Element symbol Br?", ["Boron", "Bromine", "Barium", "Beryllium"], 1, "Br is bromine.", "MEDIUM"),
    ("Element symbol I?", ["Indium", "Iodine", "Iridium", "Iron"], 1, "I is iodine.", "MEDIUM"),
    ("Element symbol U?", ["Uranium", "Carbon", "Ununennium", "Tin"], 0, "U is uranium.", "MEDIUM"),
    ("Element symbol Pt?", ["Platinum", "Plutonium", "Polonium", "Praseodymium"], 0, "Pt is platinum.", "MEDIUM"),
    ("Element symbol Ar?", ["Argon", "Arsenic", "Actinium", "Aluminum"], 0, "Ar is argon.", "EASY"),
    ("Element symbol Ne?", ["Neon", "Sodium", "Nickel", "Niobium"], 0, "Ne is neon.", "EASY"),
    ("Element symbol He?", ["Helium", "Hydrogen", "Hafnium", "Holmium"], 0, "He is helium.", "EASY"),
    ("Element symbol N?", ["Neon", "Nitrogen", "Sodium", "Nickel"], 1, "N is nitrogen.", "EASY"),
    ("Element symbol O?", ["Tin", "Oxygen", "Osmium", "Ozone"], 1, "O is oxygen.", "EASY"),
    ("Element symbol C?", ["Calcium", "Carbon", "Cobalt", "Chlorine"], 1, "C is carbon.", "EASY"),
    ("Element symbol H?", ["Helium", "Hydrogen", "Hafnium", "Mercury"], 1, "H is hydrogen.", "EASY"),
    ("Element symbol F?", ["Fluorine", "Iron", "Francium", "Phosphorus"], 0, "F is fluorine.", "EASY"),
    ("Element symbol Li?", ["Lithium", "Lanthanum", "Lutetium", "Lawrencium"], 0, "Li is lithium.", "EASY"),
]


def generate_chemistry(seen: set[str], pool: list[dict], need: int) -> None:
    cat = "chemistry"
    symbols = [s for _, s, _ in ELEMENTS]
    names = [n for n, _, _ in ELEMENTS]

    for name, symbol, number in ELEMENTS:
        diff = "EASY" if number <= 20 else ("MEDIUM" if number <= 50 else "HARD")
        wrong_syms = random.sample([s for s in symbols if s != symbol], 3)
        q = make_question(
            cat,
            f"What is the chemical symbol for {name.lower()}?",
            wrong_syms + [symbol],
            3,
            f"{name} is represented by the symbol {symbol}.",
            diff,
        )
        if add_unique(pool, seen, q) and len(pool) >= need:
            return

        wrong_names = random.sample([n for n in names if n != name], 3)
        q = make_question(
            cat,
            f"Which element has the symbol {symbol}?",
            wrong_names + [name],
            3,
            f"The symbol {symbol} stands for {name}.",
            diff,
        )
        if add_unique(pool, seen, q) and len(pool) >= need:
            return

        if number <= 56:
            q = make_question(
                cat,
                f"What is the atomic number of {name.lower()}?",
                [str(number - 1), str(number), str(number + 1), str(number + 2)],
                1,
                f"{name} has atomic number {number}.",
                diff,
            )
            if add_unique(pool, seen, q) and len(pool) >= need:
                return

    compound_names = [n for n, _, _ in COMPOUNDS]
    for name, formula, diff in COMPOUNDS:
        wrong = random.sample([f for _, f, _ in COMPOUNDS if f != formula], 3)
        q = make_question(
            cat,
            f"What is the chemical formula of {name.lower()}?",
            wrong + [formula],
            3,
            f"The formula of {name.lower()} is {formula}.",
            diff,
        )
        if add_unique(pool, seen, q) and len(pool) >= need:
            return
        wrong_n = random.sample([n for n in compound_names if n != name], 3)
        q = make_question(
            cat,
            f"Which substance has the formula {formula}?",
            wrong_n + [name],
            3,
            f"{formula} is {name.lower()}.",
            diff,
        )
        if add_unique(pool, seen, q) and len(pool) >= need:
            return

    for z in range(1, 119):
        name = next((n for n, _, num in ELEMENTS if num == z), None)
        if not name:
            continue
        q = make_question(
            cat,
            f"Which element has atomic number {z}?",
            [n for n, _, _ in ELEMENTS if n != name][:3] + [name],
            3,
            f"Atomic number {z} is {name}.",
            "EASY" if z <= 20 else "MEDIUM",
        )
        if add_unique(pool, seen, q) and len(pool) >= need:
            return

    for text, options, correct, expl, diff in CHEM_FACTS:
        q = make_question(cat, text, options, correct, expl, diff)
        if add_unique(pool, seen, q) and len(pool) >= need:
            return


# ---------------------------------------------------------------------------
# Geography
# ---------------------------------------------------------------------------

CAPITALS = [
    ("Russia", "Moscow"), ("France", "Paris"), ("Germany", "Berlin"), ("Italy", "Rome"),
    ("Spain", "Madrid"), ("United Kingdom", "London"), ("Poland", "Warsaw"), ("Ukraine", "Kyiv"),
    ("Belarus", "Minsk"), ("Kazakhstan", "Astana"), ("China", "Beijing"), ("Japan", "Tokyo"),
    ("India", "New Delhi"), ("South Korea", "Seoul"), ("Thailand", "Bangkok"), ("Vietnam", "Hanoi"),
    ("Indonesia", "Jakarta"), ("Turkey", "Ankara"), ("Iran", "Tehran"), ("Iraq", "Baghdad"),
    ("Israel", "Jerusalem"), ("Saudi Arabia", "Riyadh"), ("UAE", "Abu Dhabi"),
    ("Egypt", "Cairo"), ("South Africa", "Pretoria"), ("Nigeria", "Abuja"), ("Kenya", "Nairobi"),
    ("Morocco", "Rabat"), ("Ethiopia", "Addis Ababa"), ("USA", "Washington, D.C."), ("Canada", "Ottawa"),
    ("Mexico", "Mexico City"), ("Brazil", "Brasília"), ("Argentina", "Buenos Aires"),
    ("Chile", "Santiago"), ("Colombia", "Bogotá"), ("Peru", "Lima"), ("Venezuela", "Caracas"),
    ("Australia", "Canberra"), ("New Zealand", "Wellington"), ("Norway", "Oslo"),
    ("Sweden", "Stockholm"), ("Finland", "Helsinki"), ("Denmark", "Copenhagen"),
    ("Netherlands", "Amsterdam"), ("Belgium", "Brussels"), ("Switzerland", "Bern"),
    ("Austria", "Vienna"), ("Czech Republic", "Prague"), ("Slovakia", "Bratislava"), ("Hungary", "Budapest"),
    ("Romania", "Bucharest"), ("Bulgaria", "Sofia"), ("Greece", "Athens"), ("Portugal", "Lisbon"),
    ("Ireland", "Dublin"), ("Iceland", "Reykjavik"), ("Serbia", "Belgrade"), ("Croatia", "Zagreb"),
    ("Georgia", "Tbilisi"), ("Armenia", "Yerevan"), ("Azerbaijan", "Baku"), ("Uzbekistan", "Tashkent"),
    ("Kyrgyzstan", "Bishkek"), ("Tajikistan", "Dushanbe"), ("Turkmenistan", "Ashgabat"),
    ("Mongolia", "Ulaanbaatar"), ("Pakistan", "Islamabad"), ("Bangladesh", "Dhaka"),
    ("Sri Lanka", "Colombo"), ("Myanmar", "Naypyidaw"), ("Malaysia", "Kuala Lumpur"),
    ("Singapore", "Singapore"), ("Philippines", "Manila"), ("Cuba", "Havana"), ("Jamaica", "Kingston"),
    ("Ecuador", "Quito"), ("Bolivia", "Sucre"), ("Paraguay", "Asunción"), ("Uruguay", "Montevideo"),
    ("Lebanon", "Beirut"), ("Syria", "Damascus"), ("Jordan", "Amman"), ("Qatar", "Doha"),
    ("Kuwait", "Kuwait City"), ("Oman", "Muscat"), ("Bahrain", "Manama"), ("Nepal", "Kathmandu"),
    ("Cambodia", "Phnom Penh"), ("Laos", "Vientiane"), ("Albania", "Tirana"), ("Slovenia", "Ljubljana"),
    ("Latvia", "Riga"), ("Lithuania", "Vilnius"), ("Estonia", "Tallinn"), ("Moldova", "Chișinău"),
    ("Cyprus", "Nicosia"), ("Malta", "Valletta"), ("Luxembourg", "Luxembourg City"),
    ("Andorra", "Andorra la Vella"), ("Monaco", "Monaco"), ("San Marino", "San Marino"),
    ("Vatican City", "Vatican City"), ("Liechtenstein", "Vaduz"), ("Zimbabwe", "Harare"),
    ("Tanzania", "Dodoma"), ("Ghana", "Accra"), ("Algeria", "Algiers"), ("Tunisia", "Tunis"),
    ("Libya", "Tripoli"), ("Sudan", "Khartoum"), ("Angola", "Luanda"), ("Congo", "Brazzaville"),
    ("Cameroon", "Yaoundé"), ("Senegal", "Dakar"), ("Mali", "Bamako"), ("Niger", "Niamey"),
    ("Chad", "N'Djamena"), ("Zambia", "Lusaka"), ("Botswana", "Gaborone"), ("Namibia", "Windhoek"),
]

GEO_FACTS = [
    ("Which river flows through Paris?", ["Seine", "Rhine", "Thames", "Danube"], 0, "The Seine flows through Paris.", "EASY"),
    ("Which river flows through London?", ["Seine", "Thames", "Rhine", "Danube"], 1, "The Thames flows through London.", "EASY"),
    ("Which river flows through Rome?", ["Po", "Tiber", "Aras", "Rhône"], 1, "The Tiber flows through Rome.", "MEDIUM"),
    ("Which river flows through Cairo?", ["Nile", "Euphrates", "Congo", "Niger"], 0, "The Nile flows through Cairo.", "EASY"),
    ("Strait separating England from France?", ["Bosporus", "English Channel", "Gibraltar", "Bering"], 1, "The English Channel separates them.", "MEDIUM"),
    ("On which continent is Brazil?", ["Africa", "Eurasia", "South America", "North America"], 2, "Brazil is in South America.", "EASY"),
    ("On which continent is India?", ["Africa", "Asia", "Europe", "Australia"], 1, "India is in Asia.", "EASY"),
    ("Sea north of Turkey?", ["Red Sea", "Black Sea", "Dead Sea", "Caspian Sea"], 1, "Turkey's north coast is on the Black Sea.", "MEDIUM"),
    ("Ocean east of Australia?", ["Atlantic", "Pacific", "Indian", "Arctic"], 1, "The Pacific Ocean is east of Australia.", "EASY"),
    ("Ocean west of Africa?", ["Pacific", "Atlantic", "Indian", "Arctic"], 1, "The Atlantic is west of Africa.", "EASY"),
    ("Highest mountain in Africa?", ["Kilimanjaro", "Elbrus", "Mont Blanc", "Everest"], 0, "Kilimanjaro is Africa's highest peak.", "MEDIUM"),
    ("Highest mountain in Europe?", ["Mont Blanc", "Elbrus", "Matterhorn", "Monte Rosa"], 1, "Elbrus is Europe's highest peak.", "MEDIUM"),
    ("Country with Mount Fuji?", ["China", "Japan", "Korea", "Vietnam"], 1, "Mount Fuji is in Japan.", "EASY"),
    ("Country with Machu Picchu?", ["Mexico", "Peru", "Chile", "Bolivia"], 1, "Machu Picchu is in Peru.", "MEDIUM"),
    ("City called the 'Eternal City'?", ["Athens", "Rome", "Cairo", "Istanbul"], 1, "Rome is the Eternal City.", "EASY"),
    ("Canal linking Atlantic and Pacific?", ["Suez", "Panama", "Kiel", "Corinth"], 1, "The Panama Canal.", "MEDIUM"),
    ("Canal linking Mediterranean and Red Sea?", ["Panama", "Suez", "Corinth", "Kiel"], 1, "The Suez Canal.", "MEDIUM"),
    ("Desert covering much of North Africa?", ["Gobi", "Sahara", "Kalahari", "Atacama"], 1, "The Sahara covers much of North Africa.", "EASY"),
    ("Desert in Central Asia?", ["Sahara", "Gobi", "Kalahari", "Namib"], 1, "The Gobi is in Mongolia and northern China.", "MEDIUM"),
    ("Peninsula of Spain and Portugal?", ["Apennine", "Iberian", "Scandinavian", "Balkan"], 1, "The Iberian Peninsula.", "MEDIUM"),
    ("Gulf along Florida's coast?", ["Bay of Biscay", "Gulf of Mexico", "Hudson Bay", "Alaska Gulf"], 1, "Florida is on the Gulf of Mexico.", "MEDIUM"),
    ("Country completely surrounded by South Africa?", ["Namibia", "Lesotho", "Botswana", "Zimbabwe"], 1, "Lesotho is an enclave in South Africa.", "HARD"),
    ("Largest island in the Mediterranean?", ["Crete", "Sicily", "Sardinia", "Corsica"], 1, "Sicily is the largest.", "MEDIUM"),
    ("Country with Lake Como?", ["France", "Italy", "Switzerland", "Austria"], 1, "Lake Como is in Italy.", "HARD"),
    ("Capital of Scotland?", ["Glasgow", "Edinburgh", "Aberdeen", "Dundee"], 1, "Edinburgh is Scotland's capital.", "MEDIUM"),
    ("Most populous US state?", ["Texas", "California", "Florida", "New York"], 1, "California is the most populous.", "MEDIUM"),
    ("Country occupying the Australian continent?", ["New Zealand", "Australia", "Papua New Guinea", "Fiji"], 1, "Australia occupies the continent.", "EASY"),
    ("Line dividing Northern and Southern Hemisphere?", ["Greenwich Meridian", "Equator", "Polar Circle", "Tropic of Cancer"], 1, "The equator divides the hemispheres.", "EASY"),
    ("Line of zero longitude?", ["Equator", "Greenwich Meridian", "Tropic of Cancer", "Polar Circle"], 1, "The prime meridian passes through Greenwich.", "MEDIUM"),
    ("Approximate ocean coverage of Earth?", ["30%", "50%", "71%", "90%"], 2, "Oceans cover about 71% of Earth.", "MEDIUM"),
    ("Largest country by area?", ["USA", "Canada", "Russia", "China"], 2, "Russia is the largest country.", "EASY"),
    ("Smallest country by area?", ["Monaco", "Vatican City", "San Marino", "Liechtenstein"], 1, "Vatican City is the smallest.", "MEDIUM"),
    ("Longest river in the world?", ["Amazon", "Nile", "Yangtze", "Mississippi"], 1, "The Nile is the longest river.", "EASY"),
    ("Largest desert in the world?", ["Sahara", "Gobi", "Antarctic", "Arabian"], 2, "Antarctica is technically the largest desert.", "HARD"),
    ("Highest mountain in the world?", ["K2", "Everest", "Kangchenjunga", "Lhotse"], 1, "Mount Everest is 8,848 m.", "EASY"),
    ("Deepest ocean trench?", ["Puerto Rico Trench", "Mariana Trench", "Java Trench", "Peru-Chile Trench"], 1, "The Mariana Trench is the deepest.", "MEDIUM"),
    ("Largest ocean?", ["Atlantic", "Pacific", "Indian", "Arctic"], 1, "The Pacific is the largest ocean.", "EASY"),
    ("Capital of Australia?", ["Sydney", "Melbourne", "Canberra", "Perth"], 2, "Canberra is the capital.", "MEDIUM"),
    ("Capital of Canada?", ["Toronto", "Vancouver", "Ottawa", "Montreal"], 2, "Ottawa is the capital.", "MEDIUM"),
    ("Capital of Brazil?", ["Rio de Janeiro", "São Paulo", "Brasília", "Salvador"], 2, "Brasília is the capital.", "MEDIUM"),
    ("Country with the Great Wall?", ["Japan", "China", "Korea", "Mongolia"], 1, "The Great Wall is in China.", "EASY"),
    ("Country with the Taj Mahal?", ["Pakistan", "India", "Bangladesh", "Nepal"], 1, "The Taj Mahal is in India.", "EASY"),
    ("Country with the Eiffel Tower?", ["Germany", "France", "Belgium", "Switzerland"], 1, "The Eiffel Tower is in Paris, France.", "EASY"),
    ("Country with the Colosseum?", ["Greece", "Italy", "Spain", "Turkey"], 1, "The Colosseum is in Rome, Italy.", "EASY"),
    ("Largest lake in Africa?", ["Lake Tanganyika", "Lake Victoria", "Lake Malawi", "Lake Chad"], 1, "Lake Victoria is Africa's largest lake.", "MEDIUM"),
    ("Sea between Europe and Africa?", ["Red Sea", "Mediterranean Sea", "Black Sea", "Caspian Sea"], 1, "The Mediterranean separates Europe and Africa.", "EASY"),
    ("Mountain range between France and Spain?", ["Alps", "Pyrenees", "Carpathians", "Apennines"], 1, "The Pyrenees form the border.", "MEDIUM"),
    ("Country with the Statue of Liberty?", ["France", "USA", "UK", "Canada"], 1, "The Statue of Liberty is in New York, USA.", "EASY"),
    ("Largest island in the world?", ["Greenland", "New Guinea", "Borneo", "Madagascar"], 0, "Greenland is the largest island.", "MEDIUM"),
]


def generate_geography(seen: set[str], pool: list[dict], need: int) -> None:
    cat = "geography"
    countries = [c for c, _ in CAPITALS]
    capitals = [cap for _, cap in CAPITALS]

    for country, capital in CAPITALS:
        diff = "EASY" if country in ("Russia", "France", "USA", "China", "Japan", "Germany") else (
            "MEDIUM" if len(country) < 15 else "HARD"
        )
        wrong = random.sample([c for c in capitals if c != capital], 3)
        q = make_question(
            cat,
            f"What is the capital of {country}?",
            wrong + [capital],
            3,
            f"The capital of {country} is {capital}.",
            diff,
        )
        if add_unique(pool, seen, q) and len(pool) >= need:
            return

        wrong_c = random.sample([c for c in countries if c != country], 3)
        q = make_question(
            cat,
            f"Which country has the capital {capital}?",
            wrong_c + [country],
            3,
            f"{capital} is the capital of {country}.",
            diff,
        )
        if add_unique(pool, seen, q) and len(pool) >= need:
            return

    for text, options, correct, expl, diff in GEO_FACTS:
        q = make_question(cat, text, options, correct, expl, diff)
        if add_unique(pool, seen, q) and len(pool) >= need:
            return

    rivers = [
        ("Volga", "Russia"), ("Amazon", "South America"), ("Nile", "Africa"),
        ("Mississippi", "USA"), ("Yangtze", "China"), ("Danube", "Europe"),
        ("Rhine", "Europe"), ("Thames", "United Kingdom"), ("Seine", "France"),
        ("Ganges", "India"), ("Mekong", "Southeast Asia"), ("Congo", "Africa"),
        ("Lena", "Russia"), ("Ob", "Russia"), ("Yenisei", "Russia"),
        ("Dnieper", "Ukraine"), ("Po", "Italy"), ("Indus", "Pakistan"),
        ("Murray", "Australia"), ("Colorado", "USA"), ("Orinoco", "Venezuela"),
        ("Paraná", "South America"), ("Zambezi", "Africa"), ("Niger", "Africa"),
        ("Yellow River", "China"), ("Irtysh", "Asia"), ("Amur", "Russia/China"),
        ("Mackenzie", "Canada"), ("St. Lawrence", "Canada"), ("Rio Grande", "USA/Mexico"),
        ("Vistula", "Poland"), ("Elbe", "Germany"), ("Rhône", "France"),
        ("Tigris", "Iraq"), ("Euphrates", "Middle East"), ("Jordan", "Middle East"),
        ("Brahmaputra", "India"), ("Irrawaddy", "Myanmar"), ("Neva", "Russia"),
        ("Don", "Russia"), ("Ural", "Russia"), ("Hudson", "USA"),
        ("Columbia", "USA/Canada"), ("Fraser", "Canada"), ("Sacramento", "USA"),
        ("Potomac", "USA"), ("Ohio", "USA"), ("Missouri", "USA"),
        ("Arkansas", "USA"), ("Rio Negro", "Argentina"), ("Uruguay", "Uruguay"),
        ("Magellan", "Chile"), ("Limpopo", "Africa"), ("Orange", "South Africa"),
        ("Oka", "Russia"), ("Pechora", "Russia"), ("Angara", "Russia"),
        ("Syr Darya", "Central Asia"), ("Amu Darya", "Central Asia"),
        ("Baltic Sea", "Europe"), ("Mediterranean", "Europe/Africa/Asia"),
        ("Red Sea", "Middle East"), ("Caspian Sea", "Eurasia"), ("Black Sea", "Eurasia"),
        ("Caribbean Sea", "Central America"), ("Sea of Japan", "Asia"),
        ("Yellow Sea", "Asia"), ("East China Sea", "Asia"), ("Arabian Sea", "Middle East"),
        ("Andes", "South America"), ("Alps", "Europe"), ("Himalayas", "Asia"),
        ("Ural Mountains", "Russia"), ("Caucasus", "Caucasus"), ("Atlas Mountains", "Africa"),
        ("Appalachians", "USA"), ("Rocky Mountains", "North America"), ("Carpathians", "Europe"),
        ("Pyrenees", "France/Spain"), ("Great Lakes", "USA/Canada"),
        ("Lake Baikal", "Russia"), ("Lake Victoria", "Africa"),
        ("Lake Superior", "North America"), ("Dead Sea", "lowest point"),
        ("Great Barrier Reef", "Australia"), ("Sahara", "North Africa"),
        ("Gobi", "Asia"), ("Kalahari", "Africa"), ("Atacama", "South America"),
        ("Namib", "Africa"), ("Arabian Desert", "Middle East"),
        ("Iceland", "volcanic island"), ("Madagascar", "Indian Ocean"),
        ("Borneo", "Southeast Asia"), ("Sumatra", "Indonesia"),
        ("Honshu", "Japan"), ("Great Britain", "United Kingdom"),
        ("Sicily", "Italy"), ("Crete", "Greece"), ("Corsica", "France"),
        ("Sardinia", "Italy"), ("Tasmania", "Australia"),
    ]
    regions = [r for _, r in rivers]
    for name, region in rivers:
        wrong = random.sample([r for r in regions if r != region], min(3, len(regions) - 1))
        q = make_question(
            cat,
            f"Where is the river/sea/mountain system {name} located?",
            wrong + [region],
            len(wrong),
            f"{name} is associated with: {region}.",
            "MEDIUM",
        )
        if add_unique(pool, seen, q) and len(pool) >= need:
            return

    for peak, height in [
        ("Everest", "8848 m"), ("Elbrus", "5642 m"), ("Mont Blanc", "4808 m"),
        ("Kilimanjaro", "5895 m"), ("Aconcagua", "6961 m"), ("Denali", "6190 m"),
        ("Kosciuszko", "2228 m"), ("Monte Rosa", "4634 m"), ("Matterhorn", "4478 m"),
        ("Vinson", "4892 m"), ("K2", "8611 m"), ("Kangchenjunga", "8586 m"),
        ("Lhotse", "8516 m"), ("Makalu", "8485 m"), ("Cho Oyu", "8188 m"),
        ("Dhaulagiri", "8167 m"), ("Manaslu", "8163 m"), ("Nanga Parbat", "8126 m"),
        ("Annapurna", "8091 m"), ("Broad Peak", "8051 m"), ("Gasherbrum II", "8035 m"),
        ("Shishapangma", "8027 m"), ("Hoverla", "2061 m"), ("Belukha", "4506 m"),
        ("Ararat", "5137 m"), ("Fuji", "3776 m"), ("Teide", "3715 m"),
        ("Olympus", "2917 m"), ("Vesuvius", "1281 m"), ("Etna", "3357 m"),
        ("Sinai", "2285 m"), ("Ruwenzori", "5109 m"), ("Toubkal", "4167 m"),
        ("Damavand", "5610 m"), ("Aragats", "4090 m"),
    ]:
        wrong_h = random.sample([h for _, h in [
            ("Everest", "8848 m"), ("Elbrus", "5642 m"), ("Mont Blanc", "4808 m"),
            ("Kilimanjaro", "5895 m"), ("Fuji", "3776 m"), ("K2", "8611 m"),
        ] if h != height], 3)
        q = make_question(
            cat,
            f"What is the approximate height of {peak}?",
            wrong_h + [height],
            3,
            f"{peak} is about {height}.",
            "HARD",
        )
        if add_unique(pool, seen, q) and len(pool) >= need:
            return


# ---------------------------------------------------------------------------
# Math
# ---------------------------------------------------------------------------

MATH_FACTS = [
    ("How many degrees in a full rotation?", ["90", "180", "270", "360"], 3, "A full rotation is 360°.", "EASY"),
    ("How many centimeters in one meter?", ["10", "100", "1000", "50"], 1, "There are 100 centimeters in a meter.", "EASY"),
    ("How many millimeters in one centimeter?", ["10", "100", "5", "50"], 0, "There are 10 millimeters in a centimeter.", "EASY"),
    ("A number divisible by 2 is called?", ["Odd", "Prime", "Even", "Natural"], 2, "Numbers divisible by 2 are even.", "EASY"),
    ("How many faces does a cube have?", ["4", "6", "8", "12"], 1, "A cube has 6 faces.", "EASY"),
    ("How many edges does a cube have?", ["6", "8", "10", "12"], 3, "A cube has 12 edges.", "MEDIUM"),
    ("How many vertices does a cube have?", ["4", "6", "8", "10"], 2, "A cube has 8 vertices.", "MEDIUM"),
    ("Square root of 81?", ["7", "8", "9", "10"], 2, "√81 = 9.", "EASY"),
    ("Square root of 64?", ["6", "7", "8", "9"], 2, "√64 = 8.", "EASY"),
    ("Square root of 25?", ["3", "4", "5", "6"], 2, "√25 = 5.", "EASY"),
    ("Segment from circle center to circumference?", ["Chord", "Diameter", "Radius", "Tangent"], 2, "The radius connects center to circumference.", "MEDIUM"),
    ("Sum of angles in a quadrilateral?", ["180°", "270°", "360°", "540°"], 2, "Sum of quadrilateral angles is 360°.", "MEDIUM"),
    ("Smallest prime number?", ["0", "1", "2", "3"], 2, "The smallest prime is 2.", "MEDIUM"),
    ("What is 0!?", ["0", "1", "Undefined", "∞"], 1, "By definition 0! = 1.", "HARD"),
    ("Polygon with 6 sides?", ["Pentagon", "Hexagon", "Octagon", "Decagon"], 1, "A hexagon has 6 sides.", "MEDIUM"),
    ("Polygon with 8 sides?", ["Hexagon", "Heptagon", "Octagon", "Nonagon"], 2, "An octagon has 8 sides.", "MEDIUM"),
    ("Tangent of 45°?", ["0", "0.5", "1", "√3"], 2, "tan 45° = 1.", "HARD"),
    ("Cosine of 60°?", ["0", "0.5", "√2/2", "1"], 1, "cos 60° = 0.5.", "HARD"),
    ("Natural numbers from 1 to 10 inclusive?", ["9", "10", "11", "8"], 1, "There are 10 numbers from 1 to 10.", "EASY"),
    ("Reciprocal of 4?", ["1/2", "1/3", "1/4", "4"], 2, "The reciprocal of 4 is 1/4.", "MEDIUM"),
]


def generate_math(seen: set[str], pool: list[dict], need: int) -> None:
    cat = "math"

    for text, options, correct, expl, diff in MATH_FACTS:
        q = make_question(cat, text, options, correct, expl, diff)
        if add_unique(pool, seen, q) and len(pool) >= need:
            return

    for a in range(2, 120):
        for b in range(2, 80):
            ans = a + b
            wrong = {ans - 1, ans + 1, ans + 2, ans - 2, a + b + 3}
            wrong.discard(ans)
            opts = [str(ans)] + [str(x) for x in list(wrong)[:3]]
            random.shuffle(opts)
            q = make_question(
                cat,
                f"What is {a} + {b}?",
                opts,
                opts.index(str(ans)),
                f"{a} + {b} = {ans}.",
                "EASY" if a + b < 50 else "MEDIUM",
            )
            if add_unique(pool, seen, q) and len(pool) >= need:
                return

    for a in range(2, 25):
        for b in range(2, 25):
            ans = a * b
            wrong = {ans - a, ans + b, ans + 1, a * (b + 1)}
            wrong.discard(ans)
            opts = [str(ans)] + [str(x) for x in list(wrong)[:3]]
            random.shuffle(opts)
            q = make_question(
                cat,
                f"What is {a} × {b}?",
                opts,
                opts.index(str(ans)),
                f"{a} × {b} = {ans}.",
                "EASY" if a * b <= 50 else "MEDIUM",
            )
            if add_unique(pool, seen, q) and len(pool) >= need:
                return

    for a in range(20, 200):
        for b in range(5, min(a, 50)):
            ans = a - b
            wrong = {ans - 1, ans + 1, ans + 2, b}
            wrong.discard(ans)
            opts = [str(ans)] + [str(x) for x in list(wrong)[:3]]
            random.shuffle(opts)
            q = make_question(
                cat,
                f"What is {a} − {b}?",
                opts,
                opts.index(str(ans)),
                f"{a} − {b} = {ans}.",
                "MEDIUM",
            )
            if add_unique(pool, seen, q) and len(pool) >= need:
                return

    for b in range(2, 13):
        for ans in range(2, 13):
            a = b * ans
            wrong = {ans - 1, ans + 1, ans + 2, b}
            wrong.discard(ans)
            opts = [str(ans)] + [str(x) for x in list(wrong)[:3]]
            random.shuffle(opts)
            q = make_question(
                cat,
                f"What is {a} ÷ {b}?",
                opts,
                opts.index(str(ans)),
                f"{a} ÷ {b} = {ans}.",
                "MEDIUM",
            )
            if add_unique(pool, seen, q) and len(pool) >= need:
                return

    for pct in range(5, 100, 5):
        for base in range(20, 500, 20):
            ans = pct * base // 100
            if ans == 0:
                continue
            wrong = {ans - 1, ans + 1, ans + pct, ans - pct}
            wrong.discard(ans)
            opts = [str(ans)] + [str(max(0, x)) for x in list(wrong)[:3]]
            random.shuffle(opts)
            q = make_question(
                cat,
                f"What is {pct}% of {base}?",
                opts,
                opts.index(str(ans)),
                f"{pct}% of {base} = {ans}.",
                "HARD",
            )
            if add_unique(pool, seen, q) and len(pool) >= need:
                return

    for base in range(2, 12):
        for exp in range(2, 6):
            ans = base ** exp
            wrong = {ans - base, ans + 1, (base + 1) ** exp, base ** (exp - 1) if exp > 2 else ans + 2}
            wrong.discard(ans)
            opts = [str(ans)] + [str(x) for x in list(wrong)[:3]]
            random.shuffle(opts)
            q = make_question(
                cat,
                f"What is {base} to the power of {exp}?",
                opts,
                opts.index(str(ans)),
                f"{base}^{exp} = {ans}.",
                "HARD",
            )
            if add_unique(pool, seen, q) and len(pool) >= need:
                return


GENERATORS = {
    "chemistry": generate_chemistry,
    "geography": generate_geography,
    "math": generate_math,
}


def main() -> None:
    random.seed(42)
    bank_builders = {
        "physics": gb_en.build_physics,
        "history": lambda: gb_en._pad_history(gb_en.build_history()),
        "movies": lambda: gb_en._pad_movies(gb_en.build_movies()),
        "art": lambda: gb_en._pad_art(gb_en.build_art()),
        "animals": lambda: gb_en._pad_animals(gb_en.build_animals()),
        "informatics": lambda: gb_en._pad_informatics(gb_en.build_informatics()),
        "astronomy": lambda: gb_en._pad_astronomy(gb_en.build_astronomy()),
    }

    results: list[tuple[str, int, str]] = []

    for category, prefix in CATEGORIES.items():
        seen: set[str] = set()
        pool: list[dict] = []

        if category in GENERATORS:
            GENERATORS[category](seen, pool, TARGET)
        if category in bank_builders:
            bank = bank_builders[category]()
            generate_from_bank(category, bank, seen, pool, TARGET)

        if len(pool) < TARGET and category == "math":
            generate_math(seen, pool, TARGET)

        if len(pool) < TARGET:
            raise RuntimeError(f"{category}: only {len(pool)} questions, need {TARGET}")

        pool = pool[:TARGET]
        for i, q in enumerate(pool, start=1):
            q["id"] = f"{prefix}_{i:03d}"

        out = ASSETS / f"questions_{category}_en.json"
        with out.open("w", encoding="utf-8") as f:
            json.dump(pool, f, ensure_ascii=False, indent=2)
            f.write("\n")

        results.append((category, len(pool), out.name))
        print(f"{category}: {len(pool)} questions written -> {out.name}")

    print("\n--- Summary ---")
    for category, count, filename in results:
        print(f"  {filename}: {count} questions")


if __name__ == "__main__":
    main()