#!/usr/bin/env python3
"""English question banks for quiz categories (mirrors generate_banks.py)."""

from __future__ import annotations

import random as rnd


# ------------------------------------------------------------------ PHYSICS
def build_physics() -> list:
    qs: list = []
    units = [
        ("force", "newton", ["joule", "watt", "pascal"], "EASY"),
        ("mass", "kilogram", ["newton", "joule", "watt"], "EASY"),
        ("length", "meter", ["liter", "second", "kelvin"], "EASY"),
        ("time", "second", ["meter", "ampere", "mole"], "EASY"),
        ("temperature", "kelvin", ["newton", "lux", "hertz"], "MEDIUM"),
        ("electric current", "ampere", ["volt", "ohm", "watt"], "MEDIUM"),
        ("resistance", "ohm", ["ampere", "coulomb", "tesla"], "MEDIUM"),
        ("voltage", "volt", ["watt", "newton", "pascal"], "MEDIUM"),
        ("power", "watt", ["newton", "joule", "ohm"], "EASY"),
        ("energy", "joule", ["watt", "newton", "hertz"], "EASY"),
        ("pressure", "pascal", ["newton", "joule", "watt"], "MEDIUM"),
        ("frequency", "hertz", ["newton", "lux", "candela"], "MEDIUM"),
        ("luminous flux", "lumen", ["lux", "candela", "hertz"], "HARD"),
        ("illuminance", "lux", ["lumen", "watt", "ohm"], "HARD"),
        ("electric charge", "coulomb", ["ampere", "volt", "farad"], "HARD"),
    ]
    for what, unit, wrong, diff in units:
        opts = wrong + [unit]
        qs.append((
            f"In what SI unit is {what} measured?",
            opts, 3, f"{what.capitalize()} is measured in {unit}s (SI).", diff
        ))

    for text, opts, ci, expl, diff in [
        ("Who formulated the three laws of mechanics?", ["Einstein", "Newton", "Galileo", "Kepler"], 1, "Newton's three laws are the foundation of classical mechanics.", "MEDIUM"),
        ("Who developed the theory of relativity?", ["Newton", "Maxwell", "Einstein", "Bohr"], 2, "Albert Einstein created the theory of relativity.", "EASY"),
        ("What is the force that resists motion through a medium?", ["Friction", "Pressure", "Inertia", "Momentum"], 0, "Friction opposes motion.", "EASY"),
        ("What is inertia?", ["Speed", "Tendency to keep its state of motion", "Gravity", "Acceleration"], 1, "Inertia is the tendency to maintain velocity without forces.", "MEDIUM"),
        ("What is g on Earth (approx.)?", ["1 m/s²", "5 m/s²", "9.8 m/s²", "20 m/s²"], 2, "g ≈ 9.8 m/s².", "MEDIUM"),
        ("Which instrument measures electric current?", ["Voltmeter", "Ammeter", "Ohmmeter", "Barometer"], 1, "An ammeter measures current.", "EASY"),
        ("Which instrument measures voltage?", ["Ammeter", "Voltmeter", "Manometer", "Speedometer"], 1, "A voltmeter measures voltage.", "EASY"),
        ("What is the bending of light at a boundary between media?", ["Diffraction", "Refraction", "Interference", "Polarization"], 1, "This is refraction of light.", "MEDIUM"),
        ("Which color has the longest wavelength in the visible spectrum?", ["Violet", "Green", "Red", "Blue"], 2, "Red has the longest visible wavelength.", "HARD"),
        ("What does Newton's first law describe?", ["F=ma", "Inertia", "Action-reaction", "Gravity"], 1, "The first law is about inertia.", "MEDIUM"),
        ("What does Newton's second law describe?", ["F=ma", "Inertia", "Momentum", "Energy"], 0, "F = ma.", "MEDIUM"),
        ("What does Newton's third law describe?", ["F=ma", "Inertia", "Equal action and reaction forces", "Energy"], 2, "Action and reaction forces are equal.", "MEDIUM"),
        ("What type of wave is sound in air?", ["Transverse", "Longitudinal", "Standing", "EM"], 1, "Sound in gases is longitudinal.", "MEDIUM"),
        ("At what temperature does water boil (°C)?", ["0", "50", "100", "200"], 2, "100°C at normal pressure.", "EASY"),
        ("At what temperature does water freeze (°C)?", ["-10", "0", "4", "10"], 1, "0°C.", "EASY"),
        ("Who discovered the law of universal gravitation?", ["Galileo", "Kepler", "Newton", "Copernicus"], 2, "Newton.", "EASY"),
        ("Which instrument measures air pressure?", ["Thermometer", "Barometer", "Hydrometer", "Hygrometer"], 1, "A barometer.", "EASY"),
        ("What is density?", ["m/V", "F/S", "A/t", "Q/t"], 0, "ρ = m/V.", "MEDIUM"),
        ("SI unit of density?", ["kg/m³", "N/m", "J/s", "kg·m"], 0, "kg/m³.", "MEDIUM"),
        ("Energy of a moving body?", ["Potential", "Kinetic", "Internal", "Nuclear"], 1, "Kinetic energy.", "EASY"),
        ("Energy of a raised body?", ["Kinetic", "Potential", "Thermal", "Chemical"], 1, "Potential energy.", "EASY"),
        ("Momentum of a body equals?", ["m·v", "m·a", "F·t", "m·g"], 0, "p = m·v.", "HARD"),
        ("Best conductor of electricity?", ["Iron", "Copper", "Silver", "Aluminum"], 2, "Silver.", "HARD"),
        ("Earth's attractive force on bodies?", ["Weight", "Gravitational force", "Pressure", "Friction"], 1, "Gravitational force.", "EASY"),
        ("What does a speedometer show?", ["Acceleration", "Speed", "Distance", "Time"], 1, "Speed.", "EASY"),
        ("Who discovered electromagnetic induction?", ["Tesla", "Faraday", "Edison", "Curie"], 1, "Michael Faraday.", "MEDIUM"),
        ("What is a particle of light called?", ["Electron", "Proton", "Photon", "Neutron"], 2, "A photon.", "HARD"),
        ("Speed of sound in air (approx.)?", ["30 m/s", "150 m/s", "340 m/s", "1000 m/s"], 2, "About 340 m/s.", "MEDIUM"),
        ("What is a laser?", ["Source of coherent light", "Pressure gauge", "Radiation detector", "Microscope"], 0, "A laser emits coherent light.", "HARD"),
        ("Which law is U = I·R?", ["Ohm's law", "Coulomb's law", "Hooke's law", "Pascal's law"], 0, "Ohm's law for a circuit segment.", "MEDIUM"),
    ]:
        qs.append((text, opts, ci, expl, diff))

    scientists = [
        ("Ampere", "electrodynamics", "MEDIUM"), ("Volt", "electric voltage", "MEDIUM"),
        ("Ohm", "electrical resistance", "MEDIUM"), ("Pascal", "pressure in fluids", "MEDIUM"),
        ("Archimedes", "buoyant force", "EASY"), ("Kepler", "planetary motion laws", "HARD"),
        ("Galileo", "falling bodies and inertia", "MEDIUM"), ("Curie", "radioactivity", "HARD"),
        ("Röntgen", "X-rays", "MEDIUM"), ("Tesla", "alternating current", "MEDIUM"),
        ("Planck", "quantum theory", "HARD"), ("Bohr", "atomic model", "HARD"),
        ("Mendeleev", "periodic table", "EASY"), ("Lavoisier", "law of conservation of mass", "HARD"),
    ]
    wrong_sci = ["Einstein", "Newton", "Maxwell", "Faraday", "Galileo", "Kepler"]
    for name, discovery, diff in scientists:
        w = [s for s in wrong_sci if s != name][:3]
        qs.append((
            f"Which scientist is associated with {discovery}?",
            w + [name], 3, f"{name} contributed to the study of {discovery}.", diff
        ))

    for m in range(1, 31):
        for a in range(1, 16):
            f = m * a
            w = {f - 1, f + 1, f + m, m + a}
            w.discard(f)
            opts = [str(f)] + [str(x) for x in list(w)[:3]]
            rnd.shuffle(opts)
            qs.append((
                f"What is the force if mass is {m} kg and acceleration is {a} m/s²?",
                opts, opts.index(str(f)), f"F = m·a = {m}·{a} = {f} N.", "MEDIUM"
            ))
    return qs


# ------------------------------------------------------------------ HISTORY
def build_history() -> list:
    events = [
        ("1914", "start of World War I", "EASY"),
        ("1917", "October Revolution in Russia", "MEDIUM"),
        ("1918", "end of World War I", "MEDIUM"),
        ("1939", "start of World War II", "EASY"),
        ("1945", "end of World War II", "EASY"),
        ("1961", "Yuri Gagarin's first spaceflight", "EASY"),
        ("1969", "Moon landing (Apollo 11)", "MEDIUM"),
        ("1989", "fall of the Berlin Wall", "MEDIUM"),
        ("1991", "dissolution of the USSR", "MEDIUM"),
        ("1492", "Columbus reaches the Americas", "EASY"),
        ("1066", "Norman conquest of England", "HARD"),
        ("1215", "signing of Magna Carta", "HARD"),
        ("1789", "start of the French Revolution", "MEDIUM"),
        ("1812", "Napoleon's invasion of Russia", "MEDIUM"),
        ("1861", "abolition of serfdom in Russia", "MEDIUM"),
        ("1914", "assassination of Archduke Franz Ferdinand", "HARD"),
        ("476", "fall of the Western Roman Empire", "HARD"),
        ("1453", "fall of Constantinople", "HARD"),
        ("1613", "beginning of the Romanov dynasty", "MEDIUM"),
        ("1703", "founding of Saint Petersburg", "MEDIUM"),
        ("1815", "Battle of Waterloo", "HARD"),
        ("1941", "start of the Great Patriotic War", "EASY"),
    ]
    qs: list = []
    years = [y for y, _, _ in events]
    for year, event, diff in events:
        w = [y for y in years if y != year][:3]
        qs.append((
            f"In what year did this happen: {event}?",
            w + [year], 3, f"This happened in {year}.", diff
        ))
        w_ev = [e for _, e, _ in events if e != event][:3]
        qs.append((
            f"What event happened in {year}?",
            w_ev + [event], 3, f"In {year}: {event}.", diff
        ))

    figures = [
        ("Peter the Great", "reforms and founding of Petersburg", "EASY"),
        ("Napoleon Bonaparte", "European wars of the early 19th century", "EASY"),
        ("Ivan the Terrible", "first Russian tsardom", "MEDIUM"),
        ("Catherine the Great", "18th-century Russian Empire", "MEDIUM"),
        ("Lenin", "October Revolution of 1917", "EASY"),
        ("Stalin", "industrialization of the USSR", "MEDIUM"),
        ("Genghis Khan", "Mongol Empire", "MEDIUM"),
        ("Julius Caesar", "Roman Republic and Gaul", "MEDIUM"),
        ("Alexander the Great", "empire from Greece to India", "MEDIUM"),
        ("Christopher Columbus", "voyage to the Americas", "EASY"),
        ("Vasco da Gama", "sea route to India", "HARD"),
        ("Ferdinand Magellan", "first circumnavigation", "HARD"),
        ("Cleopatra", "ancient Egypt", "MEDIUM"),
        ("Spartacus", "slave revolt in Rome", "HARD"),
        ("Joan of Arc", "siege of Orléans", "MEDIUM"),
        ("Martin Luther", "the Reformation", "HARD"),
        ("Henry VIII", "English Reformation", "HARD"),
        ("Roosevelt", "New Deal program", "HARD"),
        ("Churchill", "Britain in World War II", "MEDIUM"),
        ("Hitler", "Nazi Germany", "MEDIUM"),
    ]
    for name, assoc, diff in figures:
        others = [n for n, _, _ in figures if n != name][:3]
        qs.append((
            f"Which historical figure is associated with: {assoc}?",
            others + [name], 3, f"This is {name}.", diff
        ))

    civs = [
        ("Ancient Egypt", "Pyramids of Giza", "EASY"),
        ("Ancient Greece", "Olympic Games and Athenian democracy", "EASY"),
        ("Ancient Rome", "Colosseum and aqueducts", "EASY"),
        ("Byzantium", "Constantinople", "MEDIUM"),
        ("Maya", "calendar and cities in Central America", "HARD"),
        ("Inca", "Machu Picchu", "MEDIUM"),
        ("Assyria", "Nineveh", "HARD"),
        ("Babylon", "Hanging Gardens", "MEDIUM"),
        ("Phoenicia", "alphabet and seafaring", "HARD"),
        ("Persia", "empire of Cyrus and Darius", "MEDIUM"),
    ]
    for civ, feat, diff in civs:
        others = [c for c, _, _ in civs if c != civ][:3]
        qs.append((
            f"Which civilization is associated with: {feat}?",
            others + [civ], 3, f"This is {civ}.", diff
        ))

    more_events = [
        ("862", "formation of Kievan Rus", "HARD"),
        ("988", "Christianization of Rus", "MEDIUM"),
        ("1240", "Battle of the Neva", "HARD"),
        ("1380", "Battle of Kulikovo", "MEDIUM"),
        ("1547", "Ivan IV crowned tsar", "HARD"),
        ("1689", "Sobornoye Ulozheniye enacted", "HARD"),
        ("1721", "Russian Empire proclaimed", "MEDIUM"),
        ("1814", "Russian army enters Paris", "HARD"),
        ("1905", "first Russian Revolution", "MEDIUM"),
        ("1922", "formation of the USSR", "MEDIUM"),
        ("1957", "launch of Sputnik 1", "EASY"),
        ("1962", "Cuban Missile Crisis", "HARD"),
        ("1986", "Chernobyl disaster", "MEDIUM"),
        ("2001", "September 11 attacks in the USA", "MEDIUM"),
        ("2008", "global financial crisis", "HARD"),
        ("753 BCE", "founding of Rome (legend)", "HARD"),
        ("44 BCE", "assassination of Julius Caesar", "MEDIUM"),
        ("395", "division of the Roman Empire", "HARD"),
        ("800", "coronation of Charlemagne", "HARD"),
        ("1096", "start of the First Crusade", "HARD"),
        ("1347", "Black Death in Europe", "HARD"),
        ("1517", "Luther's 95 Theses", "HARD"),
        ("1649", "execution of Charles I in England", "HARD"),
        ("1776", "US Declaration of Independence", "MEDIUM"),
        ("1912", "sinking of the Titanic", "MEDIUM"),
        ("1929", "start of the Great Depression", "HARD"),
        ("1947", "independence of India", "HARD"),
        ("1950", "start of the Korean War", "HARD"),
        ("1980", "Summer Olympics in Moscow", "MEDIUM"),
    ]
    years_all = [y for y, _, _ in events + more_events]
    for year, event, diff in more_events:
        w = [y for y in years_all if y != year][:3]
        qs.append((f"In what year: {event}?", w + [year], 3, f"{year} — {event}.", diff))

    more_figures = [
        ("Alexander Nevsky", "Battle on the Ice", "MEDIUM"),
        ("Dmitry Donskoy", "Battle of Kulikovo", "MEDIUM"),
        ("Attila", "the Huns", "HARD"),
        ("Hannibal", "crossing the Alps", "HARD"),
        ("Abraham Lincoln", "abolition of slavery in the USA", "MEDIUM"),
        ("Kennedy", "Cuban Missile Crisis", "MEDIUM"),
        ("Gorbachev", "perestroika", "MEDIUM"),
        ("Yeltsin", "first president of Russia", "MEDIUM"),
        ("Suvorov", "Italian campaign", "HARD"),
        ("Kutuzov", "Battle of Borodino", "MEDIUM"),
        ("Zhukov", "Battle of Stalingrad", "MEDIUM"),
        ("Mozart", "18th-century classical music", "HARD"),
        ("Homer", "Iliad and Odyssey", "HARD"),
        ("Plato", "Academy of Athens", "HARD"),
        ("Aristotle", "student of Plato", "HARD"),
        ("Copernicus", "heliocentric system", "HARD"),
        ("Galileo", "telescope and Jupiter's moons", "MEDIUM"),
        ("Gutenberg", "printing press", "HARD"),
        ("Washington", "first US president", "MEDIUM"),
        ("Robespierre", "Jacobin dictatorship", "HARD"),
        ("Bismarck", "unification of Germany", "HARD"),
        ("Mandela", "fight against apartheid", "MEDIUM"),
        ("Trotsky", "Red Army in the Civil War", "HARD"),
        ("Nicholas II", "last Russian emperor", "MEDIUM"),
    ]
    for name, assoc, diff in more_figures:
        others = [n for n, _, _ in more_figures if n != name][:3]
        qs.append((f"Who is associated with: {assoc}?", others + [name], 3, f"This is {name}.", diff))

    countries_hist = [
        ("France", "storming of the Bastille", "MEDIUM"), ("USA", "Declaration of Independence", "MEDIUM"),
        ("China", "Great Wall", "EASY"), ("India", "Maurya Empire", "HARD"),
        ("Japan", "samurai", "MEDIUM"), ("Mongolia", "empire of Genghis Khan", "MEDIUM"),
        ("Ottoman Empire", "Constantinople 1453", "HARD"), ("Byzantium", "Orthodox Christianity", "HARD"),
        ("Poland", "Warsaw Uprising 1944", "HARD"), ("Cuba", "revolution of 1959", "HARD"),
    ]
    for country, feat, diff in countries_hist:
        others = [c for c, _, _ in countries_hist if c != country][:3]
        qs.append((f"Which country is associated with: {feat}?", others + [country], 3, f"This is {country}.", diff))
    return qs


def _pad_history(qs: list) -> list:
    extra = [
        ("Battle of Kursk", "1943", "MEDIUM"), ("Battle on the Ice", "1242", "HARD"),
        ("Capture of Kazan", "1552", "HARD"), ("Time of Troubles", "early 17th century", "HARD"),
        ("Alexander I", "Patriotic War of 1812", "MEDIUM"), ("Alexander II", "1860s reforms", "MEDIUM"),
        ("Gagarin", "1961", "EASY"), ("Mendeleyev", "periodic table", "EASY"),
        ("Pushkin", "Golden Age of Russian literature", "EASY"), ("Tolstoy", "War and Peace", "EASY"),
        ("Dostoevsky", "Crime and Punishment", "EASY"), ("Chekhov", "short stories and plays", "MEDIUM"),
    ]
    for title, year, diff in extra:
        qs.append((
            f"Which event/period is associated with: {title}?",
            [t for t, _, _ in extra if t != title][:3] + [title],
            3, f"{title} — {year}.", diff
        ))

    timeline = [
        ("1871", "formation of the German Empire"), ("1912", "sinking of the Titanic"),
        ("1929", "start of the Great Depression"), ("1933", "Hitler comes to power"),
        ("1947", "independence of India"), ("1948", "founding of Israel"),
        ("1950", "start of the Korean War"), ("1953", "death of Stalin"),
        ("1955", "Warsaw Pact"), ("1956", "Hungarian uprising"),
        ("1960", "independence of 17 African nations"), ("1962", "Cuban Missile Crisis"),
        ("1963", "assassination of Kennedy"), ("1965", "US troops enter Vietnam"),
        ("1968", "Prague Spring"), ("1973", "oil crisis"),
        ("1974", "Nixon resigns"), ("1975", "fall of Saigon"),
        ("1979", "Islamic Revolution in Iran"), ("1980", "start of Iran-Iraq War"),
        ("1983", "Chernobyl disaster"), ("1989", "fall of the Berlin Wall"),
        ("1990", "reunification of Germany"), ("1993", "dissolution of Czechoslovakia"),
        ("1994", "Rwandan genocide"), ("1999", "NATO in Yugoslavia"),
        ("2003", "invasion of Iraq"), ("2008", "global financial crisis"),
        ("2011", "Arab Spring"), ("2016", "Brexit referendum"),
        ("2020", "COVID-19 pandemic"), ("843", "Treaty of Verdun"),
        ("962", "Holy Roman Empire"), ("1054", "Great Schism"),
        ("1099", "Crusaders capture Jerusalem"), ("1187", "Battle of Hattin"),
        ("1204", "Crusaders sack Constantinople"), ("1348", "Black Death"),
        ("1389", "Battle of Kosovo"), ("1410", "Battle of Grunwald"),
        ("1453", "fall of Constantinople"), ("1492", "Columbus expedition"),
        ("1519", "Magellan's circumnavigation"), ("1521", "fall of Tenochtitlan"),
        ("1543", "death of Copernicus"), ("1571", "Battle of Lepanto"),
        ("1588", "defeat of the Spanish Armada"), ("1618", "start of Thirty Years' War"),
        ("1648", "Peace of Westphalia"), ("1683", "siege of Vienna"),
        ("1709", "Battle of Poltava"), ("1721", "Treaty of Nystad"),
        ("1757", "start of Seven Years' War"), ("1762", "Catherine II ascends throne"),
        ("1775", "start of American Revolutionary War"), ("1783", "Treaty of Paris (US independence)"),
        ("1789", "storming of the Bastille"), ("1793", "execution of Louis XVI"),
        ("1799", "coup of 18 Brumaire"), ("1804", "coronation of Napoleon"),
        ("1805", "Battle of Austerlitz"), ("1812", "fire of Moscow"),
        ("1825", "Decembrist revolt"), ("1830", "July Revolution in France"),
        ("1848", "revolutions in Europe"), ("1853", "start of Crimean War"),
        ("1856", "Treaty of Paris"), ("1866", "Battle of Königgrätz"),
        ("1870", "start of Franco-Prussian War"), ("1898", "Spanish-American War"),
        ("1904", "Russo-Japanese War"), ("1905", "first Russian Revolution"),
        ("1911", "Xinhai Revolution in China"), ("1915", "Armenian genocide"),
        ("1916", "Brusilov Offensive"), ("1917", "February Revolution"),
        ("1918", "Treaty of Brest-Litovsk"), ("1920", "Battle of Warsaw"),
        ("1921", "NEP in the USSR"), ("1922", "formation of the USSR"),
        ("1923", "Beer Hall Putsch"), ("1924", "death of Lenin"),
        ("1927", "start of collectivization"), ("1929", "Wall Street crash"),
        ("1936", "start of Spanish Civil War"), ("1938", "Munich Agreement"),
        ("1940", "fall of France"), ("1941", "attack on Pearl Harbor"),
        ("1942", "Battle of Stalingrad"), ("1943", "Battle of Kursk"),
        ("1944", "Normandy landings"), ("1945", "Yalta Conference"),
        ("1946", "start of Cold War"), ("1948", "Berlin Blockade"),
        ("1949", "founding of NATO"), ("1954", "defeat at Dien Bien Phu"),
        ("1957", "launch of Sputnik 1"), ("1961", "Gagarin's flight"),
        ("1967", "Six-Day War"), ("1968", "Apollo 8 mission"),
        ("1969", "Moon landing"), ("1971", "ping-pong diplomacy"),
        ("1972", "Nixon visits China"), ("1973", "Yom Kippur War"),
        ("1978", "Deng Xiaoping reforms"), ("1979", "Soviet invasion of Afghanistan"),
        ("1980", "boycott of Moscow Olympics"), ("1985", "perestroika begins"),
        ("1986", "Chernobyl"), ("1987", "INF Treaty"),
        ("1991", "Operation Desert Storm"), ("1992", "start of Bosnian War"),
        ("1995", "Dayton Accords"), ("1997", "handover of Hong Kong"),
        ("1998", "Russian financial crisis"), ("2001", "September 11 attacks"),
        ("2004", "EU expansion"), ("2014", "Euromaidan revolution"),
        ("2015", "Iran nuclear deal"), ("2019", "COVID-19 begins"),
        ("490 BCE", "Battle of Marathon"), ("331 BCE", "Battle of Gaugamela"),
        ("27 BCE", "Augustus becomes emperor"), ("622", "Hijra of Muhammad"),
        ("732", "Battle of Tours"), ("1066", "Battle of Hastings"),
        ("1215", "Magna Carta"), ("1274", "Mongol invasion of Japan"),
        ("1453", "fall of Constantinople"), ("1492", "Columbus reaches America"),
        ("1517", "Protestant Reformation begins"), ("1588", "Spanish Armada defeated"),
        ("1642", "English Civil War begins"), ("1688", "Glorious Revolution"),
        ("1776", "US Declaration of Independence"), ("1789", "French Revolution begins"),
        ("1815", "Battle of Waterloo"), ("1848", "Communist Manifesto published"),
        ("1861", "start of US Civil War"), ("1865", "assassination of Lincoln"),
        ("1914", "World War I begins"), ("1917", "Russian Revolution"),
        ("1918", "end of World War I"), ("1929", "Great Depression begins"),
        ("1939", "World War II begins"), ("1941", "Pearl Harbor attack"),
        ("1945", "end of World War II"), ("1947", "India gains independence"),
        ("1949", "People's Republic of China founded"), ("1950", "Korean War begins"),
        ("1957", "Sputnik launched"), ("1961", "Berlin Wall built"),
        ("1962", "Cuban Missile Crisis"), ("1969", "Apollo 11 Moon landing"),
        ("1975", "Vietnam War ends"), ("1989", "Berlin Wall falls"),
        ("1991", "USSR dissolves"), ("2001", "9/11 attacks"),
        ("2003", "Iraq War begins"), ("2008", "financial crisis"),
        ("2011", "Arab Spring"), ("2020", "COVID-19 pandemic"),
    ]
    years = [y for y, _ in timeline]
    for year, event in timeline:
        w_y = [y for y in years if y != year][:3]
        qs.append((
            f"In what year did this happen: {event}?",
            w_y + [year], 3, f"{year}: {event}.", "MEDIUM"
        ))
        w_e = [e for _, e in timeline if e != event][:3]
        qs.append((
            f"What happened in {year}?",
            w_e + [event], 3, f"In {year}: {event}.", "MEDIUM"
        ))
    return qs


# ------------------------------------------------------------------ MOVIES
def build_movies() -> list:
    films = [
        ("Titanic", "James Cameron", "1997", "EASY"),
        ("Avatar", "James Cameron", "2009", "EASY"),
        ("Inception", "Christopher Nolan", "2010", "MEDIUM"),
        ("The Dark Knight", "Christopher Nolan", "2008", "MEDIUM"),
        ("The Godfather", "Francis Ford Coppola", "1972", "HARD"),
        ("Schindler's List", "Steven Spielberg", "1993", "MEDIUM"),
        ("Jaws", "Steven Spielberg", "1975", "MEDIUM"),
        ("Jurassic Park", "Steven Spielberg", "1993", "EASY"),
        ("The Matrix", "Wachowskis", "1999", "EASY"),
        ("Forrest Gump", "Robert Zemeckis", "1994", "MEDIUM"),
        ("The Intouchables", "Olivier Nakache", "2011", "EASY"),
        ("Frozen", "Disney", "2013", "EASY"),
        ("The Lion King", "Disney", "1994", "EASY"),
        ("Shrek", "DreamWorks", "2001", "EASY"),
        ("Gladiator", "Ridley Scott", "2000", "MEDIUM"),
        ("Alien", "Ridley Scott", "1979", "MEDIUM"),
        ("Back to the Future", "Robert Zemeckis", "1985", "EASY"),
        ("Home Alone", "Chris Columbus", "1990", "EASY"),
        ("Joker", "Todd Phillips", "2019", "MEDIUM"),
        ("Parasite", "Bong Joon-ho", "2019", "HARD"),
        ("The Lord of the Rings", "Peter Jackson", "2001", "EASY"),
        ("Harry Potter and the Sorcerer's Stone", "Chris Columbus", "2001", "EASY"),
    ]
    qs: list = []
    for title, director, year, diff in films:
        qs.append((
            f"Who directed the film \"{title}\"?",
            [d for _, d, _, _ in films if d != director][:3] + [director],
            3, f"\"{title}\" ({year}) was directed by {director}.", diff
        ))
        qs.append((
            f"In what year was the film \"{title}\" released?",
            [y for _, _, y, _ in films if y != year][:3] + [year],
            3, f"\"{title}\" was released in {year}.", diff
        ))

    chars = [
        ("Harry Potter", "J.K. Rowling / Warner Bros.", "EASY"),
        ("Luke Skywalker", "Star Wars", "EASY"),
        ("Darth Vader", "Star Wars", "EASY"),
        ("Tony Stark", "Marvel / Iron Man", "EASY"),
        ("Sherlock Holmes", "Arthur Conan Doyle", "EASY"),
        ("James Bond", "Ian Fleming", "EASY"),
        ("Neo", "The Matrix", "EASY"),
        ("Eleven", "Stranger Things", "EASY"),
        ("The Avengers", "Marvel", "EASY"),
        ("Minions", "Despicable Me", "EASY"),
        ("Sauron", "The Lord of the Rings", "EASY"),
        ("Gandalf", "The Lord of the Rings", "EASY"),
    ]
    for char, origin, diff in chars:
        qs.append((
            f"Which franchise does the character {char} belong to?",
            [o for _, o, _ in chars if o != origin][:3] + [origin],
            3, f"{char} is from {origin}.", diff
        ))

    series = [
        ("Game of Thrones", "HBO", "Iron Throne", "EASY"),
        ("Breaking Bad", "AMC", "methamphetamine", "MEDIUM"),
        ("Friends", "NBC", "Central Perk", "EASY"),
        ("The Office", "NBC", "Dunder Mifflin", "MEDIUM"),
        ("Stranger Things", "Netflix", "Hawkins", "EASY"),
        ("The Witcher", "Netflix", "Geralt of Rivia", "EASY"),
        ("House", "Fox", "diagnostics", "MEDIUM"),
    ]
    for name, studio, hint, diff in series:
        qs.append((
            f"Which TV series is associated with: {hint}?",
            [n for n, _, _, _ in series if n != name][:3] + [name],
            3, f"This is \"{name}\" ({studio}).", diff
        ))

    more_films = [
        ("The Green Mile", "Frank Darabont", "1999"), ("Green Book", "Peter Farrelly", "2018"),
        ("Interstellar", "Christopher Nolan", "2014"), ("Dune", "Denis Villeneuve", "2021"),
        ("Amélie", "Jean-Pierre Jeunet", "2001"), ("The Pianist", "Roman Polanski", "2002"),
        ("Se7en", "David Fincher", "1995"), ("Fight Club", "David Fincher", "1999"),
        ("The Fifth Element", "Luc Besson", "1997"), ("Léon", "Luc Besson", "1994"),
        ("Rocky", "Sylvester Stallone", "1976"), ("The Terminator", "James Cameron", "1984"),
        ("Aliens", "James Cameron", "1986"), ("American Beauty", "Sam Mendes", "1999"),
        ("The Shawshank Redemption", "Frank Darabont", "1994"), ("La La Land", "Damien Chazelle", "2016"),
        ("Shutter Island", "Martin Scorsese", "2010"), ("The Wolf of Wall Street", "Martin Scorsese", "2013"),
        ("Goodfellas", "Martin Scorsese", "1990"), ("Pulp Fiction", "Quentin Tarantino", "1994"),
        ("Inglourious Basterds", "Quentin Tarantino", "2009"), ("Django Unchained", "Quentin Tarantino", "2012"),
        ("Once Upon a Time in Hollywood", "Quentin Tarantino", "2019"),
    ]
    for title, director, year in more_films:
        qs.append((
            f"Who directed the film \"{title}\"?",
            [d for _, d, _ in more_films if d != director][:3] + [director],
            3, f"\"{title}\" ({year}) — {director}.", "MEDIUM"
        ))

    actors = [
        ("Leonardo DiCaprio", "Titanic", "EASY"), ("Tom Hanks", "Forrest Gump", "EASY"),
        ("Marlon Brando", "The Godfather", "MEDIUM"), ("Harrison Ford", "Indiana Jones", "EASY"),
        ("Robert Downey Jr.", "Iron Man", "EASY"), ("Scarlett Johansson", "Black Widow", "EASY"),
        ("Keanu Reeves", "The Matrix", "EASY"),
    ]
    for actor, role, diff in actors:
        qs.append((
            f"Which actor is known for a role in \"{role}\"?",
            [a for a, _, _ in actors if a != actor][:3] + [actor],
            3, f"This is {actor}.", diff
        ))
    return qs


def _pad_movies(qs: list) -> list:
    quotes = [
        ("Titanic", "Jack and Rose", "EASY"), ("Star Wars", "Jedi", "EASY"),
        ("The Lord of the Rings", "Frodo", "EASY"), ("The Avengers", "Thanos", "EASY"),
        ("Frozen", "Elsa", "EASY"), ("Zootopia", "Judy Hopps", "EASY"),
        ("Shrek", "ogre", "EASY"), ("Monsters Inc.", "Sulley", "EASY"),
        ("Cars", "Lightning McQueen", "EASY"), ("WALL-E", "robot", "EASY"),
        ("Game of Thrones", "Iron Throne", "EASY"), ("Breaking Bad", "Walter White", "MEDIUM"),
        ("Friends", "Central Perk", "EASY"), ("The Matrix", "red pill", "EASY"),
    ]
    for film, hint, diff in quotes:
        qs.append((
            f"Which film/series is associated with: {hint}?",
            [f for f, _, _ in quotes if f != film][:3] + [film],
            3, f"This is \"{film}\".", diff
        ))

    films = [
        ("The Godfather", "Coppola"), ("The Dark Knight", "Nolan"), ("Schindler's List", "Spielberg"),
        ("Pulp Fiction", "Tarantino"), ("Forrest Gump", "Zemeckis"), ("Inception", "Nolan"),
        ("The Matrix", "Wachowskis"), ("Gladiator", "Scott"), ("Titanic", "Cameron"),
        ("Avatar", "Cameron"), ("Jurassic Park", "Spielberg"), ("Jaws", "Spielberg"),
        ("Back to the Future", "Zemeckis"), ("Home Alone", "Columbus"), ("The Terminator", "Cameron"),
        ("Alien", "Scott"), ("Fight Club", "Fincher"), ("Se7en", "Fincher"),
        ("The Green Mile", "Darabont"), ("The Shawshank Redemption", "Darabont"),
        ("Interstellar", "Nolan"), ("Dune", "Villeneuve"), ("Oppenheimer", "Nolan"),
        ("Joker", "Phillips"), ("Parasite", "Bong"), ("1917", "Mendes"),
        ("La La Land", "Chazelle"), ("Rocky", "Avildsen"), ("Predator", "McTiernan"),
        ("Aliens", "Cameron"), ("Star Wars", "Lucas"), ("The Empire Strikes Back", "Kershner"),
        ("Return of the Jedi", "Marquand"), ("The Avengers", "Whedon"), ("Iron Man", "Favreau"),
        ("Thor", "Branagh"), ("Captain America", "Johnston"), ("Black Panther", "Coogler"),
        ("Doctor Strange", "Derrickson"), ("Guardians of the Galaxy", "Gunn"),
        ("Spider-Man", "Raimi"), ("The Lord of the Rings", "Jackson"),
        ("The Two Towers", "Jackson"), ("Return of the King", "Jackson"),
        ("The Hobbit", "Jackson"), ("Harry Potter", "Columbus"), ("Prisoner of Azkaban", "Cuarón"),
        ("Shrek", "Adamson"), ("Madagascar", "DWA"), ("Ice Age", "Ward"),
        ("Despicable Me", "Coffin"), ("Minions", "Coffin"), ("Toy Story", "Lasseter"),
        ("Finding Nemo", "Stanton"), ("WALL-E", "Stanton"), ("Up", "Docter"),
        ("Soul", "Docter"), ("Coco", "Unkrich"), ("Ratatouille", "Bird"),
        ("Frozen", "Buck"), ("Zootopia", "Moore"), ("Moana", "Clements"),
        ("The Lion King", "Allers"), ("Beauty and the Beast", "Condon"),
        ("Psycho", "Hitchcock"), ("The Birds", "Hitchcock"), ("Vertigo", "Hitchcock"),
        ("Casablanca", "Curtiz"), ("Citizen Kane", "Welles"), ("Taxi Driver", "Scorsese"),
        ("Goodfellas", "Scorsese"), ("Casino", "Scorsese"), ("Shutter Island", "Scorsese"),
        ("The Good, the Bad and the Ugly", "Leone"), ("Unforgiven", "Eastwood"),
        ("Amélie", "Jeunet"), ("Léon", "Besson"), ("The Fifth Element", "Besson"),
        ("Life Is Beautiful", "Benigni"), ("The Great Dictator", "Chaplin"),
        ("The Shining", "Kubrick"), ("2001: A Space Odyssey", "Kubrick"),
        ("A Clockwork Orange", "Kubrick"), ("Birdman", "Iñárritu"), ("The Revenant", "Iñárritu"),
        ("Whiplash", "Chazelle"), ("Drive", "Refn"), ("Oldboy", "Park"),
        ("Game of Thrones", "HBO"), ("Breaking Bad", "AMC"), ("Lost", "ABC"),
        ("Friends", "NBC"), ("The Office", "NBC"), ("Sherlock", "BBC"),
        ("Doctor Who", "BBC"), ("Chernobyl", "HBO"), ("Stranger Things", "Netflix"),
        ("The Crown", "Netflix"), ("The Mandalorian", "Disney+"), ("The Witcher", "Netflix"),
        ("Avengers: Endgame", "Russo"), ("Deadpool", "Miller"), ("Logan", "Mangold"),
        ("Top Gun", "Scott"), ("Top Gun: Maverick", "Kosinski"), ("Fast & Furious", "Cohen"),
        ("Transformers", "Bay"), ("Pacific Rim", "del Toro"), ("Godzilla", "Edwards"),
        ("King Kong", "Jackson"), ("Planet of the Apes", "Reeves"),
        ("John Wick", "Stahelski"), ("Die Hard", "McTiernan"), ("Groundhog Day", "Ramis"),
        ("Home Alone 2", "Columbus"), ("A Nightmare on Elm Street", "Craven"),
        ("Halloween", "Carpenter"), ("Saw", "Wan"), ("Scream", "Craven"),
        ("The Sixth Sense", "Shyamalan"), ("It", "Muschietti"), ("Get Out", "Peele"),
        ("Kill Bill", "Tarantino"), ("Django Unchained", "Tarantino"),
        ("Once Upon a Time in Hollywood", "Tarantino"),
        ("The Silence of the Lambs", "Demme"), ("Saving Private Ryan", "Spielberg"),
        ("Raiders of the Lost Ark", "Spielberg"), ("E.T.", "Spielberg"),
        ("Close Encounters", "Spielberg"), ("Minority Report", "Spielberg"),
        ("Catch Me If You Can", "Spielberg"), ("Bridge of Spies", "Spielberg"),
        ("The Departed", "Scorsese"), ("Gangs of New York", "Scorsese"),
        ("Raging Bull", "Scorsese"), ("Mean Streets", "Scorsese"),
        ("No Country for Old Men", "Coen"), ("Fargo", "Coen"),
        ("The Big Lebowski", "Coen"), ("True Grit", "Coen"),
        ("Blade Runner", "Scott"), ("Blade Runner 2049", "Villeneuve"),
        ("Gladiator", "Scott"), ("Black Hawk Down", "Scott"),
        ("Mad Max: Fury Road", "Miller"), ("Mad Max 2", "Miller"),
        ("The Prestige", "Nolan"), ("Memento", "Nolan"),
        ("Dunkirk", "Nolan"), ("Tenet", "Nolan"),
        ("The Social Network", "Fincher"), ("Zodiac", "Fincher"),
        ("Gone Girl", "Fincher"), ("The Curious Case", "Fincher"),
        ("Cast Away", "Zemeckis"), ("Contact", "Zemeckis"),
        ("Who Framed Roger Rabbit", "Zemeckis"), ("The Polar Express", "Zemeckis"),
        ("The Truman Show", "Weir"), ("Dead Poets Society", "Weir"),
        ("Good Will Hunting", "Van Sant"), ("Finding Forrester", "Van Sant"),
        ("A Beautiful Mind", "Howard"), ("Apollo 13", "Howard"),
        ("Rush", "Howard"), ("The Da Vinci Code", "Howard"),
        ("The Imitation Game", "Tyldum"), ("The King's Speech", "Hooper"),
        ("Slumdog Millionaire", "Boyle"), ("127 Hours", "Boyle"),
        ("Trainspotting", "Boyle"), ("28 Days Later", "Boyle"),
        ("The Grand Budapest Hotel", "Anderson"), ("Moonrise Kingdom", "Anderson"),
        ("Fantastic Mr. Fox", "Anderson"), ("Isle of Dogs", "Anderson"),
        ("Her", "Jonze"), ("Being John Malkovich", "Jonze"),
        ("Eternal Sunshine", "Gondry"), ("The Science of Sleep", "Gondry"),
        ("Lost in Translation", "Coppola"), ("Marie Antoinette", "Coppola"),
        ("The Virgin Suicides", "Coppola"), ("Somewhere", "Coppola"),
        ("There Will Be Blood", "Anderson PTA"), ("Phantom Thread", "Anderson PTA"),
        ("Boogie Nights", "Anderson PTA"), ("Magnolia", "Anderson PTA"),
        ("No Time to Die", "Mendes"), ("Skyfall", "Mendes"),
        ("Casino Royale", "Campbell"), ("GoldenEye", "Campbell"),
        ("Skyfall", "Mendes"), ("Spectre", "Mendes"),
        ("Skyfall", "Mendes"), ("Quantum of Solace", "Forster"),
        ("The Bourne Identity", "Liman"), ("The Bourne Ultimatum", "Greengrass"),
        ("Mission Impossible", "De Palma"), ("MI: Fallout", "McQuarrie"),
        ("Edge of Tomorrow", "Liman"), ("Oblivion", "Kosinski"),
        ("Tron: Legacy", "Kosinski"), ("Only the Brave", "Kosinski"),
        ("Gravity", "Cuarón"), ("Children of Men", "Cuarón"),
        ("Roma", "Cuarón"), ("Y Tu Mamá También", "Cuarón"),
        ("Pan's Labyrinth", "del Toro"), ("The Shape of Water", "del Toro"),
        ("Crimson Peak", "del Toro"), ("Hellboy", "del Toro"),
        ("Spirited Away", "Miyazaki"), ("My Neighbor Totoro", "Miyazaki"),
        ("Princess Mononoke", "Miyazaki"), ("Howl's Moving Castle", "Miyazaki"),
        ("Your Name", "Shinkai"), ("Weathering With You", "Shinkai"),
        ("Akira", "Otomo"), ("Ghost in the Shell", "Oshii"),
        ("The Ring", "Nakata"), ("Ju-on", "Shimizu"),
        ("Crouching Tiger", "Lee"), ("Brokeback Mountain", "Lee"),
        ("Life of Pi", "Lee"), ("Hulk", "Lee"),
    ]
    for film, hint in films:
        others = [f for f, _ in films if f != film][:3]
        qs.append((
            f"Which film/series is associated with: {hint}?",
            others + [film], 3, f"This is \"{film}\".", "MEDIUM"
        ))
        directors_wrong = [h for _, h in films if h != hint][:3]
        qs.append((
            f"Who directed/created the film \"{film}\"?",
            directors_wrong + [hint], 3, f"\"{film}\" — {hint}.", "HARD"
        ))
    return qs


# ------------------------------------------------------------------ ART
def build_art() -> list:
    artists = [
        ("Leonardo da Vinci", "Mona Lisa", "EASY"),
        ("Leonardo da Vinci", "The Last Supper", "MEDIUM"),
        ("Vincent van Gogh", "Starry Night", "EASY"),
        ("Vincent van Gogh", "Sunflowers", "EASY"),
        ("Pablo Picasso", "Guernica", "MEDIUM"),
        ("Claude Monet", "Water Lilies", "MEDIUM"),
        ("Rembrandt", "The Night Watch", "HARD"),
        ("Ivan Aivazovsky", "The Ninth Wave", "EASY"),
        ("Ilya Repin", "Barge Haulers on the Volga", "EASY"),
        ("Vasily Surikov", "Boyarina Morozova", "MEDIUM"),
        ("Kazimir Malevich", "Black Square", "MEDIUM"),
        ("Salvador Dalí", "The Persistence of Memory", "MEDIUM"),
        ("Michelangelo", "David", "EASY"),
        ("Michelangelo", "Sistine Chapel ceiling", "MEDIUM"),
        ("Raphael", "Sistine Madonna", "HARD"),
        ("Edvard Munch", "The Scream", "EASY"),
        ("Gustav Klimt", "The Kiss", "MEDIUM"),
        ("Johannes Vermeer", "Girl with a Pearl Earring", "MEDIUM"),
        ("Karl Bryullov", "The Last Day of Pompeii", "MEDIUM"),
        ("Victor Vasnetsov", "Bogatyrs", "EASY"),
    ]
    qs: list = []
    for artist, work, diff in artists:
        others = [a for a, _, _ in artists if a != artist][:3]
        qs.append((
            f"Who created \"{work}\"?",
            others + [artist], 3, f"\"{work}\" is by {artist}.", diff
        ))
        others_w = [w for _, w, _ in artists if w != work][:3]
        qs.append((
            f"Which work did {artist} create?",
            others_w + [work], 3, f"{artist} created \"{work}\".", diff
        ))

    movements = [
        ("Impressionism", "Monet and Renoir", "EASY"),
        ("Cubism", "Picasso and Braque", "MEDIUM"),
        ("Surrealism", "Dalí and Magritte", "MEDIUM"),
        ("Renaissance", "da Vinci and Michelangelo", "EASY"),
        ("Baroque", "Rubens and Rembrandt", "MEDIUM"),
        ("Pop Art", "Warhol", "MEDIUM"),
        ("Suprematism", "Malevich", "HARD"),
        ("Romanticism", "Friedrich", "HARD"),
        ("Peredvizhniki", "Repin and Surikov", "MEDIUM"),
        ("Avant-garde", "Kandinsky", "HARD"),
    ]
    for mov, ex, diff in movements:
        qs.append((
            f"Which art movement is associated with: {ex}?",
            [m for m, _, _ in movements if m != mov][:3] + [mov],
            3, f"This is {mov}.", diff
        ))

    museums = [
        ("Louvre", "Paris", "EASY"), ("Hermitage", "Saint Petersburg", "EASY"),
        ("Prado", "Madrid", "MEDIUM"), ("Uffizi", "Florence", "MEDIUM"),
        ("Tretyakov Gallery", "Moscow", "EASY"), ("MOMA", "New York", "MEDIUM"),
        ("Rijksmuseum", "Amsterdam", "HARD"), ("British Museum", "London", "MEDIUM"),
        ("Vatican Museums", "Vatican City", "MEDIUM"), ("Guggenheim", "New York", "HARD"),
    ]
    for museum, city, diff in museums:
        qs.append((
            f"In which city is the {museum} located?",
            [c for _, c, _ in museums if c != city][:3] + [city],
            3, f"The {museum} is in {city}.", diff
        ))

    techniques = [
        ("Fresco", "painting on wet plaster", "MEDIUM"),
        ("Oil painting", "pigments in oil", "EASY"),
        ("Watercolor", "water-soluble paints", "EASY"),
        ("Engraving", "printing from carved plate", "MEDIUM"),
        ("Sculpture", "three-dimensional artwork", "EASY"),
        ("Stained glass", "glass in a frame", "MEDIUM"),
        ("Collage", "gluing materials together", "MEDIUM"),
        ("Pastel", "soft dry chalks", "HARD"),
    ]
    for tech, desc, diff in techniques:
        qs.append((
            f"Which technique is described: {desc}?",
            [t for t, _, _ in techniques if t != tech][:3] + [tech],
            3, f"This is {tech}.", diff
        ))
    return qs


def _pad_art(qs: list) -> list:
    pairs = [
        ("Shishkin", "Morning in a Pine Forest"), ("Serov", "Girl with Peaches"),
        ("Levitan", "Above Eternal Peace"), ("Vrubel", "The Demon"),
        ("Botticelli", "The Birth of Venus"), ("Caravaggio", "The Calling of Matthew"),
        ("Hokusai", "The Great Wave"), ("Warhol", "Campbell's Soup Cans"),
        ("Rodin", "The Thinker"), ("Bernini", "Ecstasy of Saint Teresa"),
        ("Monet", "Impression, Sunrise"), ("Van Gogh", "Starry Night"),
        ("Picasso", "Guernica"), ("Dalí", "The Persistence of Memory"),
        ("Magritte", "The Son of Man"), ("Matisse", "The Dance"),
        ("Malevich", "Black Square"), ("Kandinsky", "Composition VII"),
        ("Pollock", "drip painting"), ("Rothko", "color fields"),
        ("Michelangelo", "David"), ("Raphael", "The School of Athens"),
        ("Rembrandt", "The Night Watch"), ("Vermeer", "Girl with a Pearl Earring"),
        ("Velázquez", "Las Meninas"), ("Goya", "The Third of May"),
        ("Turner", "seascapes"), ("Manet", "Luncheon on the Grass"),
        ("Degas", "ballet dancers"), ("Cézanne", "still lifes with apples"),
        ("Munch", "The Scream"), ("Klimt", "The Kiss"),
        ("Chagall", "Above the Town"), ("Banksy", "street art"),
        ("Frida Kahlo", "self-portraits"), ("Diego Rivera", "murals"),
        ("O'Keeffe", "flowers"), ("Hopper", "Nighthawks"),
        ("Grant Wood", "American Gothic"), ("Escher", "optical illusions"),
        ("Duchamp", "Fountain"), ("Mondrian", "neoplasticism"),
        ("Lichtenstein", "comic style"), ("Hockney", "swimming pools"),
    ]
    for artist, work in pairs:
        others = [a for a, _ in pairs if a != artist][:3]
        qs.append((
            f"Which artist is associated with the work/style: {work}?",
            others + [artist], 3, f"This is {artist} — {work}.", "MEDIUM"
        ))

    more_pairs = [
        ("da Vinci", "Mona Lisa"), ("Michelangelo", "Sistine Chapel"),
        ("Raphael", "Sistine Madonna"), ("Botticelli", "Birth of Venus"),
        ("Titian", "Venus of Urbino"), ("Caravaggio", "Calling of Matthew"),
        ("Rembrandt", "Night Watch"), ("Vermeer", "The Milkmaid"),
        ("Rubens", "Descent from the Cross"), ("Velázquez", "Las Meninas"),
        ("Goya", "Third of May"), ("Delacroix", "Liberty Leading the People"),
        ("Courbet", "Burial at Ornans"), ("Manet", "Olympia"),
        ("Monet", "Water Lilies"), ("Renoir", "Dance at Le Moulin de la Galette"),
        ("Degas", "Ballet Class"), ("Cézanne", "Still Life with Apples"),
        ("Van Gogh", "Starry Night"), ("Gauguin", "Where Do We Come From"),
        ("Munch", "The Scream"), ("Klimt", "The Kiss"), ("Chagall", "Above the City"),
        ("Picasso", "Les Demoiselles d'Avignon"), ("Dalí", "Persistence of Memory"),
        ("Magritte", "The Son of Man"), ("Matisse", "The Dance"),
        ("Malevich", "Black Square"), ("Kandinsky", "Composition VII"),
        ("Pollock", "Number 5"), ("Warhol", "Campbell's Soup"),
        ("Rodin", "The Kiss"), ("Bernini", "Ecstasy of Saint Teresa"),
        ("Donatello", "David (sculpture)"), ("Falconet", "Bronze Horseman"),
        ("Repin", "Barge Haulers"), ("Surikov", "Boyarina Morozova"),
        ("Vasnetsov", "Bogatyrs"), ("Shishkin", "Rye"), ("Kuindzhi", "Moonlit Night"),
        ("Aivazovsky", "The Ninth Wave"), ("Savrasov", "The Rooks Have Returned"),
        ("Levitan", "March"), ("Vrubel", "Pan"), ("Kustodiev", "Shrovetide"),
        ("Bryullov", "Last Day of Pompeii"), ("Perov", "Troika"),
        ("Hokusai", "Great Wave"), ("Hiroshige", "Rain on Bridge"),
        ("Turner", "Rain, Steam and Speed"), ("Constable", "The Hay Wain"),
        ("Whistler", "Portrait of Mother"), ("Sargent", "Madame X"),
        ("Miró", "The Tilled Field"), ("Calder", "mobiles"),
        ("Basquiat", "graffiti art"), ("Hockney", "pool paintings"),
        ("Banksy", "street murals"), ("Frida Kahlo", "The Two Fridas"),
        ("Rivera", "Detroit Industry"), ("O'Keeffe", "Red Canna"),
        ("Hopper", "Automat"), ("Wood", "American Gothic"),
        ("Rothko", "Orange and Yellow"), ("Newman", "zip paintings"),
        ("Klein", "International Klein Blue"), ("Lichtenstein", "Drowning Girl"),
        ("Johns", "flags"), ("Rauschenberg", "combines"),
        ("Dali", "elephants"), ("Escher", "impossible constructions"),
        ("Mondrian", "Broadway Boogie Woogie"), ("Klee", "Twittering Machine"),
        ("Kirchner", "Street, Dresden"), ("Marc", "Blue Horses"),
        ("Nolde", "flowers"), ("Schiele", "expressive lines"),
        ("Gropius", "Bauhaus"), ("Munch", "expressionism"),
        ("Braque", "cubism"), ("Léger", "mechanical forms"),
        ("Derain", "fauvism"), ("Vlaminck", "fauvism"),
        ("Rousseau", "jungle scenes"), ("Seurat", "pointillism"),
        ("Signac", "pointillism"), ("Toulouse-Lautrec", "Moulin Rouge"),
        ("Mucha", "Art Nouveau"), ("Schiele", "portraits"),
        ("Gustav Klimt", "gold leaf"), ("Egon Schiele", "angular figures"),
        ("Kandinsky", "abstract compositions"), ("Klee", "color theory"),
        ("Miró", "biomorphic forms"), ("Calder", "wire sculptures"),
        ("Murals", "Rivera"), ("O'Keeffe", "New Mexico landscapes"),
        ("Pollock", "action painting"), ("Rothko", "color fields"),
        ("Warhol", "pop art"), ("Lichtenstein", "pop art"),
        ("Hockney", "Los Angeles pools"), ("Banksy", "guerrilla art"),
        ("Shishkin", "forest scenes"), ("Aivazovsky", "seascapes"),
        ("Repin", "portraits"), ("Surikov", "historical paintings"),
        ("Vasnetsov", "fairy tales"), ("Vrubel", "demons"),
        ("Kustodiev", "festive scenes"), ("Serov", "portraits"),
        ("Levitan", "landscapes"), ("Kuindzhi", "night scenes"),
        ("Repin", "Reply of the Zaporozhian Cossacks"), ("Surikov", "Morning of the Streltsy Execution"),
        ("Vasnetsov", "Ivan Tsarevich"), ("Shishkin", "Rain in an Oak Forest"),
        ("Aivazovsky", "Black Sea"), ("Savrasov", "Early Spring"),
        ("Levitan", "Golden Autumn"), ("Korovin", "Spanish Women"),
        ("Serov", "Portrait of Ida Rubinstein"), ("Vrubel", "The Swan Princess"),
        ("Nesterov", "Vision to the Youth Bartholomew"), ("Kustodiev", "Merchant's Wife"),
        ("Bryullov", "Italian Morning"), ("Ivanov", "Appearance of Christ"),
        ("Kramskoy", "Christ in the Wilderness"), ("Perov", "The Drowned"),
        ("Ge", "Peter the Great"), ("Semiradsky", "Nero's Torches"),
        ("Makovsky", "Children Running from a Storm"), ("Yaroshenko", "The Student"),
        ("Polenov", "Grandmother's Garden"), ("Vasiliev", "After the Rain"),
        ("Feshin", "Bashkir"), ("Archipenko", "Walking Woman"),
        ("Filonov", "Formula of Spring"), ("Goncharova", "Harvest"),
        ("Larionov", "Spring"), ("Grigoriev", "Peasant Woman"),
        ("Nesterov", "Holy Russia"), ("Vrubel", "Lilac"),
        ("Korin", "Goalkeeper"), ("Falk", "Still Life"),
        ("Bakst", "Scheherazade"), ("Malyavin", "Whirlwind"),
        ("Benois", "Peter the Great"), ("Somov", "Harlequin"),
        ("Roerich", "Himalayas"), ("Rerikh", "Guests from Overseas"),
        ("Bilibin", "illustrations"), ("Vasnetsov", "Alyonushka"),
        ("Repin", "Ivan the Terrible"), ("Surikov", "Menshikov in Berezovo"),
        ("Shishkin", "Countess Mordvinova"), ("Kuindzhi", "Birch Grove"),
        ("Aivazovsky", "Bay of Naples"), ("Savrasov", "Spring Day"),
        ("Levitan", "Fresh Wind"), ("Korovin", "Winter"),
        ("Serov", "Portrait of K.I. Girshman"), ("Vrubel", "The Seated Demon"),
        ("Nesterov", "On the Mountains"), ("Kustodiev", "Portrait of Chaliapin"),
        ("Bryullov", "Horsewoman"), ("Ivanov", "The Last Day of Pompeii"),
        ("Kramskoy", "Portrait of an Unknown Woman"), ("Perov", "The Hunters at Rest"),
        ("Ge", "The Last Supper"), ("Semiradsky", "Dance with Swords"),
        ("Makovsky", "The Russian Bride"), ("Yaroshenko", "Life Is Everywhere"),
        ("Polenov", "Christ and the Sinner"), ("Vasiliev", "Thaw"),
        ("Feshin", "Portrait of Katya"), ("Archipenko", "Dance"),
        ("Filonov", "Formula of Color"), ("Goncharova", "Still Life"),
        ("Larionov", "Spring Landscape"), ("Grigoriev", "Peasant Children"),
        ("Nesterov", "The Empty Tomb"), ("Vrubel", "The Rose and the Cross"),
        ("Korin", "Football"), ("Falk", "Portrait"),
        ("Bakst", "Costume Design"), ("Malyavin", "Laughter"),
        ("Benois", "Versailles"), ("Somov", "Promenade"),
        ("Roerich", "Treasure of the Angels"), ("Rerikh", "Overseas Guests"),
        ("Bilibin", "Fairy Tales"), ("Vasnetsov", "Three Princesses"),
        ("Monet", "Water Lilies series"), ("Renoir", "Luncheon of the Boating Party"),
        ("Degas", "The Ballet Class"), ("Cézanne", "The Card Players"),
        ("Van Gogh", "Café Terrace at Night"), ("Gauguin", "Vision After the Sermon"),
        ("Munch", "Anxiety"), ("Klimt", "Judith and the Head of Holofernes"),
        ("Picasso", "The Weeping Woman"), ("Dalí", "The Elephants"),
        ("Magritte", "The Treachery of Images"), ("Matisse", "The Red Studio"),
    ]
    for artist, work in more_pairs:
        others = [a for a, _ in more_pairs if a != artist][:3]
        qs.append((
            f"Which artist is associated with the work/style: {work}?",
            others + [artist], 3, f"This is {artist} — {work}.", "MEDIUM"
        ))
        works_wrong = [w for _, w in more_pairs if w != work][:3]
        qs.append((
            f"Which work/style is associated with artist {artist}?",
            works_wrong + [work], 3, f"{artist} — {work}.", "HARD"
        ))
    return qs


# ------------------------------------------------------------------ ANIMALS
def build_animals() -> list:
    animals = [
        ("Elephant", "trunk", "African/Asian savannas and forests", "EASY"),
        ("Giraffe", "longest neck", "Africa", "EASY"),
        ("Kangaroo", "pouch", "Australia", "EASY"),
        ("Panda", "bamboo diet", "China", "EASY"),
        ("Dolphin", "echolocation", "oceans", "EASY"),
        ("Whale", "largest animal", "oceans", "EASY"),
        ("Penguin", "Antarctica", "cannot fly", "EASY"),
        ("Eagle", "bird of prey", "skies", "EASY"),
        ("Lion", "king of beasts", "Africa", "EASY"),
        ("Tiger", "striped coat", "Asia", "EASY"),
        ("Bear", "hibernation", "forests", "EASY"),
        ("Wolf", "pack hunter", "forests and tundra", "EASY"),
        ("Fox", "reddish fur", "forests", "EASY"),
        ("Zebra", "stripes", "Africa", "EASY"),
        ("Camel", "humps", "deserts", "EASY"),
        ("Crocodile", "reptile", "tropics", "MEDIUM"),
        ("Turtle", "shell", "land and water", "EASY"),
        ("Frog", "amphibian", "wetlands", "EASY"),
        ("Shark", "cartilaginous skeleton", "ocean", "MEDIUM"),
        ("Octopus", "three hearts", "ocean", "HARD"),
        ("Hummingbird", "hovering flight", "Americas", "MEDIUM"),
        ("Owl", "nocturnal predator", "forests", "EASY"),
        ("Bee", "pollination", "hives", "EASY"),
        ("Ant", "colony", "everywhere", "EASY"),
        ("Butterfly", "metamorphosis", "gardens", "EASY"),
        ("Cobra", "venom", "Asia/Africa", "MEDIUM"),
        ("Hedgehog", "spines", "Europe", "EASY"),
        ("Squirrel", "nuts", "forests", "EASY"),
        ("Horse", "domesticated", "grasslands", "EASY"),
        ("Cow", "ruminant", "farms", "EASY"),
    ]
    qs: list = []
    for name, feat, habitat, diff in animals:
        others = [n for n, _, _, _ in animals if n != name][:3]
        qs.append((
            f"Which animal is known for: {feat}?",
            others + [name], 3, f"This is the {name}.", diff
        ))
        qs.append((
            f"Where does the {name} mainly live?",
            [h for _, _, h, _ in animals if h != habitat][:3] + [habitat],
            3, f"The {name} lives in: {habitat}.", diff
        ))

    classes = [
        ("Mammals", "nurse young with milk", "EASY"),
        ("Birds", "feathers and beak", "EASY"),
        ("Fish", "gills", "EASY"),
        ("Reptiles", "scales", "MEDIUM"),
        ("Amphibians", "live in water and on land", "MEDIUM"),
        ("Insects", "six legs", "EASY"),
        ("Arachnids", "eight legs", "MEDIUM"),
    ]
    for cls, trait, diff in classes:
        qs.append((
            f"Which animal class is described: {trait}?",
            [c for c, _, _ in classes if c != cls][:3] + [cls],
            3, f"This is {cls}.", diff
        ))

    records = [
        ("Fastest land animal?", ["Cheetah", "Lion", "Antelope", "Horse"], 0, "A cheetah can reach about 110 km/h.", "MEDIUM"),
        ("Largest land animal?", ["Elephant", "Giraffe", "African elephant", "Hippo"], 2, "The African elephant is the largest land animal.", "MEDIUM"),
        ("Only egg-laying mammals?", ["Kangaroo", "Platypus", "Echidna", "Platypus and echidna"], 3, "Platypus and echidna lay eggs.", "HARD"),
        ("How many hearts does an octopus have?", ["1", "2", "3", "4"], 2, "An octopus has three hearts.", "HARD"),
        ("Which animal changes color?", ["Chameleon", "Fox", "Wolf", "Bear"], 0, "A chameleon changes color.", "EASY"),
        ("Which animal can sleep standing up?", ["Horse", "Cow", "Cat", "Dog"], 0, "Horses can doze while standing.", "MEDIUM"),
    ]
    for item in records:
        qs.append(item)
    return qs


def _pad_animals(qs: list) -> list:
    birds = [
        "sparrow", "tit", "rook", "swallow", "stork", "heron", "woodpecker", "owl",
        "eagle", "falcon", "rooster", "chicken", "duck", "goose", "swan", "penguin",
        "parrot", "ostrich", "flamingo", "peacock",
    ]
    for bird in birds:
        qs.append((
            f"Which class does the {bird} belong to?",
            ["Fish", "Birds", "Mammals", "Reptiles"], 1, f"The {bird} is a bird.", "EASY"
        ))
    fish = ["carp", "pike", "perch", "catfish", "trout", "salmon", "tuna", "shark", "ray", "herring"]
    for f in fish:
        qs.append((
            f"Which class does the {f} belong to?",
            ["Fish", "Birds", "Insects", "Mammals"], 0, f"The {f} is a fish.", "EASY"
        ))
    insects = ["beetle", "ant", "bee", "wasp", "bumblebee", "butterfly", "dragonfly", "cricket", "grasshopper", "cockroach", "mosquito", "fly", "flea"]
    for ins in insects:
        qs.append((
            f"Which class does the {ins} belong to?",
            ["Arachnids", "Insects", "Fish", "Mollusks"], 1, f"The {ins} is an insect.", "EASY"
        ))

    mammals = [
        "lion", "tiger", "leopard", "cheetah", "panther", "jaguar", "lynx", "wolf",
        "fox", "bear", "panda", "koala", "kangaroo", "elephant", "rhino", "hippo",
        "giraffe", "zebra", "antelope", "buffalo", "bison", "deer", "moose", "boar",
        "squirrel", "beaver", "otter", "hedgehog", "horse", "cow", "goat", "sheep",
        "pig", "dog", "cat", "rabbit", "hare", "dolphin", "whale", "seal", "walrus",
        "gorilla", "chimpanzee", "orangutan", "lemur", "camel", "llama", "alpaca",
        "bat", "skunk", "opossum", "sloth", "anteater", "armadillo", "porcupine",
        "hyena", "mongoose", "manatee", "narwhal", "sperm whale", "orca",
    ]
    seen_m: set[str] = set()
    for m in mammals:
        if m in seen_m:
            continue
        seen_m.add(m)
        qs.append((
            f"Which class does the {m} belong to?",
            ["Birds", "Fish", "Mammals", "Reptiles"], 2,
            f"The {m} is a mammal.", "EASY"
        ))

    reptiles = [
        "crocodile", "alligator", "lizard", "iguana", "chameleon", "gecko", "monitor",
        "snake", "python", "cobra", "viper", "turtle", "tortoise", "caiman",
    ]
    for r in reptiles:
        qs.append((
            f"Which class does the {r} belong to?",
            ["Amphibians", "Reptiles", "Fish", "Mammals"], 1,
            f"The {r} is a reptile.", "EASY"
        ))

    amphibians = ["frog", "toad", "newt", "salamander", "axolotl"]
    for a in amphibians:
        qs.append((
            f"Which class does the {a} belong to?",
            ["Amphibians", "Reptiles", "Fish", "Insects"], 0,
            f"The {a} is an amphibian.", "EASY"
        ))

    habitats = [
        ("penguin", "Antarctica"), ("polar bear", "Arctic"), ("camel", "desert"),
        ("giraffe", "Africa"), ("elephant", "Africa/Asia"), ("kangaroo", "Australia"),
        ("koala", "Australia"), ("platypus", "Australia"), ("panda", "China"),
        ("tiger", "Asia"), ("lion", "Africa"), ("zebra", "Africa"),
        ("hippo", "Africa"), ("rhino", "Africa/Asia"), ("orangutan", "Borneo"),
        ("lemur", "Madagascar"), ("ostrich", "Africa"), ("flamingo", "Africa/South America"),
        ("dolphin", "ocean"), ("whale", "ocean"), ("shark", "ocean"),
        ("octopus", "ocean"), ("crab", "sea"), ("lobster", "sea"),
        ("seahorse", "sea"), ("jellyfish", "ocean"), ("walrus", "Arctic"),
        ("seal", "polar regions"), ("beaver", "rivers"), ("otter", "rivers"),
        ("eagle", "mountains"), ("owl", "forest"), ("wolf", "forest/tundra"),
        ("fox", "forest"), ("hare", "grassland"), ("squirrel", "forest"),
        ("chameleon", "tropics"), ("crocodile", "African rivers"),
        ("anaconda", "Amazon"), ("cobra", "Asia"), ("python", "tropics"),
    ]
    for animal, region in habitats:
        qs.append((
            f"Where does the {animal} live?",
            [r for _, r in habitats if r != region][:3] + [region],
            3, f"The {animal} — {region}.", "MEDIUM"
        ))

    speeds = [
        ("cheetah", "fastest land animal"), ("swift", "fast bird"),
        ("peregrine falcon", "fastest diving bird"), ("tortoise", "slow animal"),
        ("snail", "very slow animal"),
    ]
    for animal, feat in speeds:
        qs.append((
            f"Which animal: {feat}?",
            [a for a, _ in speeds if a != animal][:3] + [animal],
            3, f"This is the {animal}.", "MEDIUM"
        ))

    groups = [
        ("pack", "wolves"), ("pride", "lions"), ("herd", "horses"),
        ("flock", "sheep"), ("school", "fish"), ("swarm", "bees"),
        ("colony", "penguins"), ("pod", "whales"),
    ]
    for group, example in groups:
        qs.append((
            f"What is a group of {example} called?",
            [g for g, _ in groups if g != group][:3] + [group],
            3, f"A group of {example} is a {group}.", "HARD"
        ))

    spiders = ["spider", "tarantula", "wolf spider", "orb-weaver", "black widow", "brown recluse"]
    for s in spiders:
        qs.append((
            f"Which class does the {s} belong to?",
            ["Insects", "Arachnids", "Fish", "Mollusks"], 1,
            f"The {s} is an arachnid.", "EASY"
        ))

    mollusks = ["snail", "mussel", "oyster", "octopus", "squid", "scallop", "cuttlefish", "nautilus"]
    for m in mollusks:
        qs.append((
            f"Which phylum does the {m} belong to?",
            ["Chordates", "Mollusks", "Arthropods", "Annelids"], 1,
            f"The {m} is a mollusk.", "MEDIUM"
        ))

    diets = [
        ("lion", "carnivore"), ("rabbit", "herbivore"), ("bear", "omnivore"),
        ("cow", "herbivore"), ("wolf", "carnivore"), ("deer", "herbivore"),
        ("pig", "omnivore"), ("eagle", "carnivore"), ("tiger", "carnivore"),
        ("elephant", "herbivore"), ("raccoon", "omnivore"), ("shark", "carnivore"),
        ("whale", "plankton/fish"), ("panda", "herbivore"), ("giraffe", "herbivore"),
        ("platypus", "carnivore"), ("koala", "herbivore"), ("crocodile", "carnivore"),
        ("frog", "carnivore"),
    ]
    for animal, diet in diets:
        qs.append((
            f"What is the diet of the {animal}?",
            [d for _, d in diets if d != diet][:3] + [diet],
            3, f"The {animal} is a {diet}.", "MEDIUM"
        ))

    qs.append((
        "Which animal can regenerate its tail?",
        ["Lizard", "Lion", "Elephant", "Wolf"], 0, "A lizard can drop and regrow its tail.", "EASY"
    ))

    extra_mammals = [
        "panther", "serval", "ocelot", "caracal", "bobcat", "cougar", "puma",
        "wolverine", "badger", "marmot", "chipmunk", "porcupine", "chinchilla",
        "gerbil", "hamster", "ferret", "mink", "weasel", "stoat", "marten",
        "tapir", "capybara", "agouti", "chinchilla", "viscacha", "mara",
        "wallaby", "wombat", "tasmanian devil", "quokka", "echidna",
        "numbat", "bandicoot", "possum", "koala", "kangaroo", "wallaroo",
        "meerkat", "mongoose", "hyena", "jackal", "dingo", "coyote",
        "bison", "yak", "musk ox", "caribou", "reindeer", "elk", "moose",
        "ibex", "chamois", "mouflon", "bighorn sheep", "mountain goat",
        "gazelle", "impala", "springbok", "wildebeest", "gnu", "okapi",
        "tapir", "peccary", "warthog", "babirusa", "hippopotamus",
        "manatee", "dugong", "narwhal", "beluga", "orca", "pilot whale",
        "fin whale", "blue whale", "humpback whale", "gray whale",
        "sea lion", "fur seal", "elephant seal", "leopard seal",
        "otter", "mink", "badger", "raccoon", "coati", "kinkajou",
        "sloth", "anteater", "armadillo", "pangolin", "aardvark",
        "lemur", "tarsier", "gibbon", "bonobo", "baboon", "mandrill",
        "macaque", "capuchin", "howler monkey", "spider monkey",
    ]
    seen_extra: set[str] = set()
    for m in extra_mammals:
        if m in seen_extra:
            continue
        seen_extra.add(m)
        qs.append((
            f"Which class does the {m} belong to?",
            ["Birds", "Fish", "Mammals", "Reptiles"], 2,
            f"The {m} is a mammal.", "EASY"
        ))

    extra_birds = [
        "crow", "raven", "magpie", "jay", "cardinal", "blue jay",
        "robin", "thrush", "warbler", "finch", "canary", "sparrowhawk",
        "buzzard", "vulture", "condor", "albatross", "pelican", "cormorant",
        "kingfisher", "woodpecker", "hoopoe", "toucan", "hornbill",
        "cassowary", "emu", "kiwi", "pheasant", "quail", "partridge",
        "turkey", "guinea fowl", "peacock", "macaw", "cockatoo", "budgie",
    ]
    for bird in extra_birds:
        qs.append((
            f"Which class does the {bird} belong to?",
            ["Fish", "Birds", "Mammals", "Reptiles"], 1, f"The {bird} is a bird.", "EASY"
        ))

    traits = [
        ("chameleon", "changes skin color"), ("bat", "only flying mammal"),
        ("platypus", "lays eggs"), ("penguin", "flightless bird"),
        ("ostrich", "largest bird"), ("hummingbird", "smallest bird"),
        ("blue whale", "largest animal ever"), ("cheetah", "fastest land animal"),
        ("tortoise", "long-lived reptile"), ("salmon", "migrates to spawn"),
        ("monarch butterfly", "long migration"), ("arctic tern", "longest migration"),
        ("koala", "eats eucalyptus"), ("panda", "eats bamboo"),
        ("vulture", "scavenger"), ("beaver", "builds dams"),
        ("spider", "eight legs"), ("starfish", "regenerates arms"),
        ("jellyfish", "no brain"), ("octopus", "three hearts"),
    ]
    for animal, trait in traits:
        qs.append((
            f"Which animal: {trait}?",
            [a for a, _ in traits if a != animal][:3] + [animal],
            3, f"This is the {animal}.", "MEDIUM"
        ))
    return qs


# ------------------------------------------------------------------ INFORMATICS
def build_informatics() -> list:
    qs: list = []
    formats = [
        (".txt", "text file", "EASY"), (".jpg", "image", "EASY"),
        (".mp3", "audio", "EASY"), (".mp4", "video", "EASY"),
        (".pdf", "document", "EASY"), (".zip", "archive", "EASY"),
        (".exe", "Windows program", "MEDIUM"), (".html", "web page", "EASY"),
        (".css", "web page styles", "MEDIUM"), (".json", "JSON data", "MEDIUM"),
        (".py", "Python script", "MEDIUM"), (".java", "Java source", "MEDIUM"),
        (".kt", "Kotlin source", "MEDIUM"), (".xml", "markup", "MEDIUM"),
        (".csv", "tabular data", "MEDIUM"), (".png", "lossless image", "EASY"),
        (".gif", "animation", "EASY"), (".docx", "Word document", "EASY"),
    ]
    for ext, desc, diff in formats:
        others = [e for e, _, _ in formats if e != ext][:3]
        qs.append((
            f"Which file type is described: {desc}?",
            others + [ext], 3, f"This is the {ext} extension.", diff
        ))

    terms = [
        ("CPU", "central processing unit", "EASY"),
        ("RAM", "random access memory", "EASY"),
        ("SSD", "solid-state drive", "MEDIUM"),
        ("HDD", "hard disk drive", "MEDIUM"),
        ("GPU", "graphics processing unit", "MEDIUM"),
        ("OS", "operating system", "EASY"),
        ("URL", "web address", "EASY"),
        ("HTTP", "hypertext transfer protocol", "MEDIUM"),
        ("HTTPS", "secure HTTP", "MEDIUM"),
        ("IP address", "network identifier", "MEDIUM"),
        ("DNS", "domain name resolution", "HARD"),
        ("VPN", "virtual private network", "MEDIUM"),
        ("API", "application programming interface", "MEDIUM"),
        ("IDE", "integrated development environment", "MEDIUM"),
        ("Git", "version control system", "MEDIUM"),
        ("Byte", "8 bits", "EASY"),
        ("Kilobyte", "1024 bytes", "EASY"),
        ("Algorithm", "sequence of steps", "EASY"),
        ("Database", "structured data storage", "EASY"),
        ("SQL", "database query language", "MEDIUM"),
        ("HTML", "web page markup", "EASY"),
        ("CSS", "web page styling", "EASY"),
        ("JavaScript", "browser scripting", "EASY"),
        ("Python", "programming language", "EASY"),
        ("Java", "programming language", "EASY"),
        ("Kotlin", "JVM and Android language", "MEDIUM"),
        ("C++", "compiled language", "MEDIUM"),
        ("Bit", "smallest unit of information", "EASY"),
        ("Login", "username", "EASY"),
        ("Password", "access secret", "EASY"),
        ("Firewall", "network protection", "MEDIUM"),
        ("Virus", "malicious program", "EASY"),
        ("Antivirus", "malware protection", "EASY"),
        ("Bluetooth", "wireless connection", "EASY"),
        ("Wi-Fi", "wireless local network", "EASY"),
        ("Cloud", "remote data storage", "EASY"),
        ("Pixel", "image point", "EASY"),
        ("Screen resolution", "number of pixels", "MEDIUM"),
        ("Driver", "device software", "MEDIUM"),
        ("Compiler", "translates code to machine code", "HARD"),
        ("Interpreter", "executes code line by line", "HARD"),
        ("Recursion", "function calls itself", "HARD"),
        ("Array", "collection of elements", "EASY"),
        ("Loop", "repeated commands", "EASY"),
        ("Condition", "if/else branching", "EASY"),
        ("Variable", "named data container", "EASY"),
        ("Stack", "LIFO structure", "HARD"),
        ("Queue", "FIFO structure", "HARD"),
    ]
    for abbr, desc, diff in terms:
        qs.append((
            f"What does {abbr} mean?",
            [d for _, d, _ in terms if d != desc][:3] + [desc],
            3, f"{abbr} — {desc}.", diff
        ))

    os_list = [
        ("Windows", "Microsoft"), ("Linux", "open source"), ("macOS", "Apple"),
        ("Android", "Google mobile OS"), ("iOS", "iPhone/iPad"), ("Ubuntu", "Linux distribution"),
    ]
    for os_name, desc in os_list:
        qs.append((
            f"Which OS is described: {desc}?",
            [o for o, _ in os_list if o != os_name][:3] + [os_name],
            3, f"This is {os_name}.", "MEDIUM"
        ))

    shortcuts = [
        ("Ctrl+C", "copy", "EASY"), ("Ctrl+V", "paste", "EASY"),
        ("Ctrl+Z", "undo", "EASY"), ("Ctrl+S", "save", "EASY"),
        ("Alt+Tab", "switch window", "MEDIUM"), ("F5", "refresh page", "EASY"),
        ("Ctrl+A", "select all", "EASY"), ("Delete", "delete", "EASY"),
    ]
    for key, action, diff in shortcuts:
        qs.append((
            f"What does {key} do?",
            [a for _, a, _ in shortcuts if a != action][:3] + [action],
            3, f"{key} — {action}.", diff
        ))

    for n in range(2, 80):
        b = bin(n)[2:]
        w = {bin(n - 1)[2:], bin(n + 1)[2:], bin(n + 2)[2:], bin(max(1, n - 2))[2:]}
        w.discard(b)
        opts = [b] + list(w)[:3]
        rnd.shuffle(opts)
        qs.append((
            f"How is the number {n} written in binary?",
            opts, opts.index(b), f"{n} in binary = {b}.", "HARD"
        ))
    return qs


def _pad_informatics(qs: list) -> list:
    for port, service in [(80, "HTTP"), (443, "HTTPS"), (21, "FTP"), (22, "SSH"), (25, "SMTP"), (53, "DNS"), (3306, "MySQL"), (5432, "PostgreSQL")]:
        qs.append((
            f"Which port does {service} typically use?",
            [str(port - 1), str(port), str(port + 1), str(port + 10)],
            1, f"{service} uses port {port}.", "HARD"
        ))
    for bits in [8, 16, 32, 64]:
        qs.append((
            f"How many bits in a {bits}-bit processor architecture?",
            [str(bits - 8), str(bits), str(bits + 8), str(bits * 2)],
            1, f"{bits}-bit architecture.", "MEDIUM"
        ))

    for n in range(2, 256):
        b = bin(n)[2:]
        wrong = [bin(n - 1)[2:] if n > 2 else "0", bin(n + 1)[2:], bin(n + 2)[2:]]
        opts = [b] + wrong[:3]
        qs.append((
            f"What is the binary representation of {n}?",
            opts, 0, f"{n}₁₀ = {b}₂.", "HARD" if n > 32 else "MEDIUM"
        ))
        if len(qs) > 600:
            break

    shortcuts = [
        ("Ctrl+C", "copy"), ("Ctrl+V", "paste"), ("Ctrl+X", "cut"),
        ("Ctrl+Z", "undo"), ("Ctrl+S", "save"), ("Ctrl+A", "select all"),
        ("Ctrl+P", "print"), ("Ctrl+F", "find"), ("Alt+Tab", "switch window"),
        ("Win+D", "show desktop"), ("F5", "refresh"), ("F11", "full screen"),
        ("Ctrl+Shift+Esc", "task manager"), ("Win+L", "lock"), ("Ctrl+W", "close tab"),
    ]
    for key, action in shortcuts:
        qs.append((
            f"Which keyboard shortcut: {action}?",
            [k for k, _ in shortcuts if k != key][:3] + [key],
            3, f"{action} — {key}.", "MEDIUM"
        ))

    langs = [
        ("Python", ".py"), ("Java", ".java"), ("C++", ".cpp"), ("JavaScript", ".js"),
        ("TypeScript", ".ts"), ("Kotlin", ".kt"), ("Swift", ".swift"), ("Go", ".go"),
        ("Rust", ".rs"), ("PHP", ".php"), ("Ruby", ".rb"), ("C#", ".cs"),
        ("HTML", ".html"), ("CSS", ".css"), ("SQL", ".sql"), ("Shell", ".sh"),
        ("R", ".r"), ("Scala", ".scala"), ("Dart", ".dart"), ("Lua", ".lua"),
    ]
    for lang, ext in langs:
        qs.append((
            f"What file extension does {lang} use?",
            [e for _, e in langs if e != ext][:3] + [ext],
            3, f"{lang} — {ext}.", "MEDIUM"
        ))
    return qs


# ------------------------------------------------------------------ ASTRONOMY
def build_astronomy() -> list:
    planets = [
        ("Mercury", "closest to the Sun", "EASY"),
        ("Venus", "hottest planet", "MEDIUM"),
        ("Earth", "only planet with known life", "EASY"),
        ("Mars", "Red Planet", "EASY"),
        ("Jupiter", "largest planet", "EASY"),
        ("Saturn", "rings", "EASY"),
        ("Uranus", "tilted axis", "MEDIUM"),
        ("Neptune", "outermost planet", "MEDIUM"),
    ]
    qs: list = []
    for name, feat, diff in planets:
        others = [n for n, _, _ in planets if n != name][:3]
        qs.append((
            f"Which planet: {feat}?",
            others + [name], 3, f"This is {name}.", diff
        ))

    moons = [
        ("Moon", "Earth", "EASY"), ("Phobos", "Mars", "HARD"), ("Deimos", "Mars", "HARD"),
        ("Io", "Jupiter", "HARD"), ("Europa", "Jupiter", "MEDIUM"), ("Titan", "Saturn", "MEDIUM"),
        ("Triton", "Neptune", "HARD"),
    ]
    for moon, planet, diff in moons:
        qs.append((
            f"The moon {moon} orbits which planet?",
            [p for _, p, _ in moons if p != planet][:3] + [planet],
            3, f"{moon} orbits {planet}.", diff
        ))

    facts = [
        ("What is our galaxy called?", ["Andromeda", "Milky Way", "Sombrero", "Orion Nebula"], 1, "We are in the Milky Way.", "EASY"),
        ("What is the Sun?", ["Planet", "Star", "Moon", "Comet"], 1, "The Sun is a star.", "EASY"),
        ("What is a comet?", ["Star", "Icy body with a tail", "Planet", "Moon"], 1, "Comets are icy bodies with tails.", "EASY"),
        ("What is an asteroid?", ["Small rocky body", "Star", "Galaxy", "Nebula"], 0, "Asteroids are rocky bodies.", "MEDIUM"),
        ("How many planets in the Solar System?", ["7", "8", "9", "10"], 1, "8 planets (Pluto excluded).", "EASY"),
        ("Who was the first person in space?", ["Armstrong", "Gagarin", "Leonov", "Titov"], 1, "Yuri Gagarin, 1961.", "EASY"),
        ("Who was the first on the Moon?", ["Gagarin", "Armstrong", "Aldrin", "Collins"], 1, "Neil Armstrong, 1969.", "EASY"),
        ("What is a black hole?", ["Region of extreme gravity", "Nebula", "Comet", "Planet"], 0, "A black hole has immense gravity.", "MEDIUM"),
        ("What is a light-year?", ["Time", "Distance", "Speed", "Brightness"], 1, "A light-year is a distance.", "MEDIUM"),
        ("Nearest star to the Sun?", ["Sirius", "Polaris", "Proxima Centauri", "Betelgeuse"], 2, "Proxima Centauri is nearest.", "HARD"),
        ("What is a meteorite?", ["Object that reached Earth's surface", "Star", "Moon", "Nebula"], 0, "A meteorite hits the ground.", "MEDIUM"),
        ("What is an orbit?", ["Path around a body", "Ecliptic", "Meridian", "Parallax"], 0, "Orbital motion.", "EASY"),
        ("Which telescope has been in orbit since 1990?", ["Hubble", "James Webb", "Spitzer", "Kepler"], 0, "Hubble since 1990.", "MEDIUM"),
        ("Which telescope launched in 2021?", ["Hubble", "James Webb", "Galileo", "Cassini"], 1, "JWST — 2021.", "MEDIUM"),
        ("What does cosmology study?", ["The universe as a whole", "Planets", "Comets", "Rockets"], 0, "Cosmology studies the universe.", "HARD"),
    ]
    for item in facts:
        qs.append(item)

    constellations = [
        ("Ursa Major", "Big Dipper", "EASY"), ("Orion", "Hunter", "EASY"),
        ("Cassiopeia", "W-shaped", "MEDIUM"), ("Leo", "zodiac", "MEDIUM"),
        ("Scorpius", "zodiac", "MEDIUM"), ("Draco", "northern sky", "HARD"),
        ("Cygnus", "Northern Cross", "MEDIUM"), ("Pegasus", "Great Square", "HARD"),
    ]
    for name, feat, diff in constellations:
        qs.append((
            f"Which constellation is described: {feat}?",
            [n for n, _, _ in constellations if n != name][:3] + [name],
            3, f"This is {name}.", diff
        ))

    missions = [
        ("Apollo 11", "Moon landing", "EASY"),
        ("Vostok 1", "first human spaceflight", "EASY"),
        ("Voyager 1", "interstellar probe", "MEDIUM"),
        ("Cassini", "Saturn study", "HARD"),
        ("Galileo", "Jupiter study", "HARD"),
        ("Soyuz", "Russian crewed spacecraft", "EASY"),
        ("ISS", "International Space Station", "EASY"),
    ]
    for mission, feat, diff in missions:
        qs.append((
            f"Which mission is associated with: {feat}?",
            [m for m, _, _ in missions if m != mission][:3] + [mission],
            3, f"This is {mission}.", diff
        ))

    order = ["Mercury", "Venus", "Earth", "Mars", "Jupiter", "Saturn", "Uranus", "Neptune"]
    for i, planet in enumerate(order):
        if i < len(order) - 1:
            nxt = order[i + 1]
            wrong = [order[j] for j in range(8) if order[j] not in (nxt, planet)][:3]
            qs.append((
                f"Which planet comes right after {planet} from the Sun?",
                wrong + [nxt], 3, f"After {planet} comes {nxt}.", "MEDIUM"
            ))
        if i > 0:
            prev = order[i - 1]
            wrong = [order[j] for j in range(8) if order[j] not in (prev, planet)][:3]
            qs.append((
                f"Which planet comes before {planet} from the Sun?",
                wrong + [prev], 3, f"Before {planet} is {prev}.", "MEDIUM"
            ))

    more_astro = [
        ("What is a solar eclipse?", ["Moon blocks the Sun", "Earth blocks the Moon", "Comet", "Meteor"], 0, "During a solar eclipse the Moon covers the Sun.", "EASY"),
        ("What is a lunar eclipse?", ["Earth casts shadow on Moon", "Moon blocks Sun", "Mars", "Comet"], 0, "Earth's shadow falls on the Moon.", "EASY"),
        ("Which planet rotates on its side?", ["Mars", "Uranus", "Venus", "Mercury"], 1, "Uranus has a tilted axis.", "HARD"),
        ("Which planet rotates backwards?", ["Venus", "Mars", "Jupiter", "Earth"], 0, "Venus has retrograde rotation.", "HARD"),
        ("Light travel time from Sun to Earth?", ["1 sec", "8 minutes", "1 hour", "1 day"], 1, "About 8 minutes.", "MEDIUM"),
        ("What is the Big Bang?", ["Theory of universe's origin", "Star explosion", "Comet", "Black hole"], 0, "Cosmological origin theory.", "MEDIUM"),
        ("Path of the Sun across the sky?", ["Ecliptic", "Equator", "Meridian", "Horizon"], 0, "The ecliptic.", "HARD"),
        ("What is a nebula?", ["Cloud of gas and dust", "Planet", "Comet", "Asteroid"], 0, "A nebula is gas and dust.", "MEDIUM"),
        ("Where is Polaris located?", ["Ursa Minor", "Orion", "Leo", "Scorpius"], 0, "Polaris is in Ursa Minor.", "MEDIUM"),
        ("What does astronomy study?", ["Celestial bodies", "Rockets", "Climate", "Oceans"], 0, "Celestial objects and space.", "EASY"),
        ("Earth's natural satellite?", ["Moon", "Phobos", "Titan", "Io"], 0, "The Moon.", "EASY"),
        ("Main gas in Venus atmosphere?", ["Nitrogen", "Oxygen", "Carbon dioxide", "Hydrogen"], 2, "CO₂ on Venus.", "HARD"),
        ("Main gas in Earth's atmosphere?", ["Oxygen", "Nitrogen", "Carbon dioxide", "Argon"], 1, "Nitrogen ~78%.", "MEDIUM"),
        ("Why is Mars red?", ["Iron oxide", "Oxygen", "Water", "Gold"], 0, "Rust on the surface.", "MEDIUM"),
        ("What is Pluto now classified as?", ["Dwarf planet", "Star", "Moon", "Comet"], 0, "Dwarf planet since 2006.", "EASY"),
        ("Which planet has the Great Red Spot?", ["Jupiter", "Mars", "Saturn", "Neptune"], 0, "Storm on Jupiter.", "EASY"),
        ("What is our star called?", ["Sirius", "Sun", "Polaris", "Vega"], 1, "The Sun.", "EASY"),
        ("What is a constellation?", ["Pattern of stars", "Planet", "Galaxy", "Comet"], 0, "Recognizable star pattern.", "EASY"),
        ("Which probe studied Pluto?", ["New Horizons", "Voyager", "Cassini", "Galileo"], 0, "New Horizons, 2015.", "HARD"),
        ("Escape velocity from Earth?", ["First cosmic", "Second cosmic", "Third", "Fourth"], 1, "Second cosmic velocity ~11.2 km/s.", "HARD"),
    ]
    for item in more_astro:
        qs.append(item)

    zodiac = ["Aries", "Taurus", "Gemini", "Cancer", "Leo", "Virgo", "Libra", "Scorpio", "Sagittarius", "Capricorn", "Aquarius", "Pisces"]
    for sign in zodiac:
        qs.append((
            f"Is \"{sign}\" a zodiac sign?",
            ["No", "Yes", "A planet", "Orion constellation"], 1,
            f"\"{sign}\" is a zodiac sign.", "EASY"
        ))
    return qs


def _pad_astronomy(qs: list) -> list:
    planets = ["Mercury", "Venus", "Earth", "Mars", "Jupiter", "Saturn", "Uranus", "Neptune"]
    for planet in planets:
        others = [p for p in planets if p != planet][:3]
        qs.append((
            f"Which Solar System planet: {planet}?",
            others + [planet], 3, f"This is {planet}.", "EASY"
        ))
    dwarf = ["Pluto", "Ceres", "Eris", "Haumea", "Makemake"]
    for d in dwarf:
        qs.append((
            f"Is {d} a dwarf planet?",
            ["No", "Yes", "A star", "A moon"], 1, f"{d} is a dwarf planet.", "MEDIUM"
        ))

    stars = [
        ("Sirius", "brightest star in the sky"), ("Betelgeuse", "red supergiant"),
        ("Polaris", "navigation star"), ("Vega", "Lyra"), ("Altair", "Aquila"),
        ("Antares", "Scorpius"), ("Rigel", "Orion"), ("Capella", "Auriga"),
        ("Procyon", "Canis Minor"), ("Arcturus", "Boötes"), ("Spica", "Virgo"),
        ("Aldebaran", "Taurus"), ("Deneb", "Cygnus"), ("Fomalhaut", "Piscis Austrinus"),
    ]
    for star, feat in stars:
        qs.append((
            f"Which star: {feat}?",
            [s for s, _ in stars if s != star][:3] + [star],
            3, f"This is {star}.", "MEDIUM"
        ))

    calendar = [
        ("How many days in a leap year?", ["365", "366", "364", "367"], 1, "366 days.", "EASY"),
        ("How many hours in a day?", ["12", "24", "48", "36"], 1, "24 hours.", "EASY"),
        ("How many minutes in a day?", ["1440", "1000", "1200", "2000"], 0, "1440 minutes.", "HARD"),
        ("Closest point of orbit to the Sun?", ["Aphelion", "Perihelion", "Apogee", "Perigee"], 1, "Perihelion.", "HARD"),
        ("Farthest point of orbit from the Sun?", ["Aphelion", "Perihelion", "Zenith", "Nadir"], 0, "Aphelion.", "HARD"),
    ]
    for item in calendar:
        qs.append(item)

    planet_facts = {
        "Mercury": [
            ("Which planet has the shortest year?", "Mercury"),
            ("Which planet is closest to the Sun?", "Mercury"),
            ("Which planet has the largest temperature swings?", "Mercury"),
        ],
        "Venus": [
            ("Which planet is the hottest?", "Venus"),
            ("Which planet rotates slower than it orbits?", "Venus"),
            ("Which planet is the 'evening star'?", "Venus"),
        ],
        "Earth": [
            ("Only planet with known life?", "Earth"),
            ("Which planet has one natural satellite?", "Earth"),
            ("Which planet is 71% water?", "Earth"),
        ],
        "Mars": [
            ("Which planet is called the Red Planet?", "Mars"),
            ("Which planet has Olympus Mons?", "Mars"),
            ("Which planet has moons Phobos and Deimos?", "Mars"),
        ],
        "Jupiter": [
            ("Largest planet?", "Jupiter"),
            ("Which planet has moon Ganymede?", "Jupiter"),
            ("Where is the Great Red Spot?", "Jupiter"),
        ],
        "Saturn": [
            ("Planet with prominent rings?", "Saturn"),
            ("Which planet has moon Titan?", "Saturn"),
            ("Which planet is less dense than water?", "Saturn"),
        ],
        "Uranus": [
            ("Planet that rotates on its side?", "Uranus"),
            ("Which planet is blue due to methane?", "Uranus"),
            ("Which planet was discovered by telescope?", "Uranus"),
        ],
        "Neptune": [
            ("Outermost planet?", "Neptune"),
            ("Strongest winds in the Solar System?", "Neptune"),
            ("Which planet has moon Triton?", "Neptune"),
        ],
    }
    all_planets = list(planet_facts.keys())
    for _planet, facts in planet_facts.items():
        for qtext, answer in facts:
            others = [p for p in all_planets if p != answer][:3]
            qs.append((qtext, others + [answer], 3, f"Answer: {answer}.", "MEDIUM"))

    for i, planet in enumerate(planets, start=1):
        others = [str(j) for j in range(1, 9) if j != i][:3]
        qs.append((
            f"What number planet from the Sun is {planet}?",
            others + [str(i)], 3, f"{planet} is planet number {i}.", "MEDIUM"
        ))

    moons = [
        ("Moon", "Earth"), ("Phobos", "Mars"), ("Deimos", "Mars"),
        ("Io", "Jupiter"), ("Europa", "Jupiter"), ("Ganymede", "Jupiter"),
        ("Callisto", "Jupiter"), ("Titan", "Saturn"), ("Enceladus", "Saturn"),
        ("Triton", "Neptune"), ("Charon", "Pluto"),
    ]
    for moon, parent in moons:
        qs.append((
            f"Which body does moon {moon} orbit?",
            [p for _, p in moons if p != parent][:3] + [parent],
            3, f"{moon} orbits {parent}.", "MEDIUM"
        ))

    space_facts = [
        ("What is a light-year?", ["Distance light travels in a year", "A year on another planet", "Star rotation time", "Speed of light"], 0, "A light-year is a distance unit.", "MEDIUM"),
        ("Speed of light (approx.)?", ["300,000 km/s", "30,000 km/s", "3,000 km/s", "3,000,000 km/s"], 0, "c ≈ 300,000 km/s.", "MEDIUM"),
        ("Our galaxy is called?", ["Andromeda", "Milky Way", "Orion Nebula", "Large Magellanic Cloud"], 1, "We are in the Milky Way.", "EASY"),
        ("What is a black hole?", ["Region of extreme gravity", "Empty space", "Dark planet", "Comet"], 0, "Gravity so strong light cannot escape.", "MEDIUM"),
        ("What is a pulsar?", ["Rapidly spinning neutron star", "Planet", "Comet", "Galaxy"], 0, "A pulsar emits pulses of radiation.", "HARD"),
        ("Big Bang theory explains?", ["Origin of the universe", "Star explosions", "Comets", "Black holes"], 1, "The leading cosmological model.", "EASY"),
        ("How many planets (modern count)?", ["7", "8", "9", "10"], 1, "8 planets since 2006.", "EASY"),
        ("Earth's moon?", ["Phobos", "Moon", "Titan", "Europa"], 1, "Earth has one moon.", "EASY"),
        ("What is a comet?", ["Icy body with a tail", "Star", "Planet", "Galaxy"], 0, "Comets are icy solar system bodies.", "EASY"),
        ("What is an asteroid?", ["Rocky body", "Star", "Gas giant", "Moon"], 0, "Asteroids are rocky objects.", "EASY"),
        ("Solar eclipse occurs when?", ["Moon blocks the Sun", "Earth blocks the Moon", "Mars blocks Sun", "Comet"], 0, "Moon passes in front of the Sun.", "EASY"),
        ("Lunar eclipse occurs when?", ["Earth's shadow on the Moon", "Moon blocks Sun", "Sun goes out", "Mars"], 0, "Earth blocks sunlight to the Moon.", "EASY"),
        ("First person in space?", ["Armstrong", "Gagarin", "Leonov", "Titov"], 1, "Yuri Gagarin, 1961.", "EASY"),
        ("First on the Moon?", ["Gagarin", "Armstrong", "Aldrin", "Collins"], 1, "Neil Armstrong, 1969.", "EASY"),
        ("What is the ISS?", ["International Space Station", "Mars station", "Lunar base", "Telescope"], 0, "ISS orbits Earth.", "EASY"),
        ("Hubble Space Telescope launched?", ["Hubble", "Galileo", "Kepler", "James Webb"], 0, "Hubble in 1990.", "MEDIUM"),
        ("James Webb Telescope launched?", ["Hubble", "James Webb", "Spitzer", "Chandra"], 1, "JWST in 2021.", "MEDIUM"),
        ("Nearest star to the Sun?", ["Sirius", "Proxima Centauri", "Alpha Centauri", "Vega"], 1, "Proxima Centauri, 4.2 ly.", "HARD"),
        ("Light from Sun to Earth takes?", ["1 sec", "8 minutes", "1 hour", "1 day"], 1, "About 8 minutes 20 seconds.", "MEDIUM"),
        ("What is an exoplanet?", ["Planet around another star", "Solar System planet", "Moon", "Comet"], 0, "Outside our Solar System.", "MEDIUM"),
        ("Planet that rotates on its side?", ["Mars", "Uranus", "Venus", "Mercury"], 1, "Uranus is tilted ~98°.", "MEDIUM"),
        ("Planet with retrograde rotation?", ["Venus", "Mars", "Jupiter", "Earth"], 0, "Venus spins backwards.", "HARD"),
        ("Planet with rings?", ["Mars", "Saturn", "Venus", "Mercury"], 1, "Saturn has prominent rings.", "EASY"),
        ("Gas giant planet?", ["Earth", "Jupiter", "Mars", "Mercury"], 1, "Jupiter and Saturn are gas giants.", "EASY"),
        ("Ice giant planet?", ["Jupiter", "Uranus", "Mars", "Venus"], 1, "Uranus and Neptune are ice giants.", "MEDIUM"),
        ("Northern lights called?", ["Aurora borealis", "Aurora australis", "Comet", "Meteor"], 0, "Polar lights in the north.", "MEDIUM"),
        ("Mars rovers from NASA?", ["Venus", "Mars", "Jupiter", "Mercury"], 1, "Perseverance, Curiosity on Mars.", "EASY"),
        ("Apollo program goal?", ["Moon landing", "Mars", "Venus", "ISS"], 0, "Apollo 11 landed in 1969.", "EASY"),
        ("Who discovered planetary motion laws?", ["Newton", "Kepler", "Galileo", "Copernicus"], 1, "Kepler's three laws.", "MEDIUM"),
        ("Who proposed heliocentrism?", ["Ptolemy", "Copernicus", "Einstein", "Galileo"], 1, "Copernicus, 16th century.", "MEDIUM"),
        ("Who used telescope for astronomy?", ["Newton", "Galileo", "Kepler", "Ptolemy"], 1, "Galileo, early 1600s.", "EASY"),
    ]
    for item in space_facts:
        qs.append(item)

    for phase, desc in [
        ("New Moon", "Moon not visible"), ("Waxing crescent", "thin crescent"),
        ("First Quarter", "half illuminated"), ("Full Moon", "fully illuminated"),
        ("Last Quarter", "other half lit"), ("Waning crescent", "before new moon"),
    ]:
        qs.append((
            f"Which Moon phase: {desc}?",
            [p for p, _ in [("New Moon", ""), ("Full Moon", ""), ("First Quarter", ""), ("Last Quarter", "")] if p != phase][:3] + [phase],
            3, f"{desc} — {phase}.", "MEDIUM"
        ))

    missions = [
        ("Vostok 1", "Gagarin"), ("Apollo 11", "Moon landing"),
        ("Soyuz", "Russian spacecraft"), ("Sputnik 1", "first satellite"),
        ("Luna 2", "first on the Moon"), ("Voyager 1", "interstellar probe"),
        ("Cassini", "Saturn"), ("Juno", "Jupiter"), ("Perseverance", "NASA Mars rover"),
        ("Curiosity", "NASA Mars rover"), ("Hubble", "space telescope"),
        ("James Webb", "infrared telescope"), ("ISS", "orbital station"),
        ("Falcon 9", "SpaceX"), ("Starship", "SpaceX"), ("Artemis", "Moon program"),
        ("New Horizons", "Pluto"), ("Rosetta", "comet Churyumov"),
        ("Galileo", "Jupiter"), ("Mariner 4", "first Mars photos"),
    ]
    for mission, feat in missions:
        qs.append((
            f"Which space mission/craft: {feat}?",
            [m for m, _ in missions if m != mission][:3] + [mission],
            3, f"This is {mission}.", "MEDIUM"
        ))

    for year, event in [
        ("1957", "Sputnik 1"), ("1961", "Gagarin"), ("1969", "Moon landing"),
        ("1977", "Voyager launch"), ("1990", "Hubble"), ("1997", "Pathfinder on Mars"),
        ("2006", "Pluto reclassified"), ("2012", "Curiosity"), ("2021", "James Webb"),
        ("2020", "Crew Dragon"), ("1986", "Challenger"), ("2003", "Columbia"),
        ("1971", "Salyut 1"), ("1998", "ISS construction begins"), ("2015", "New Horizons at Pluto"),
        ("2014", "Philae on comet"), ("1965", "Mariner 4"), ("1976", "Viking on Mars"),
    ]:
        years = [y for y, _ in [
            ("1957", "Sputnik 1"), ("1961", "Gagarin"), ("1969", "Moon landing"),
            ("1977", "Voyager"), ("1990", "Hubble"), ("2006", "Pluto"),
        ] if y != year][:3]
        qs.append((
            f"In what year in space history: {event}?",
            years + [year], 3, f"{event} — {year}.", "MEDIUM"
        ))

    constellations = [
        ("Orion", "Hunter"), ("Ursa Major", "Big Dipper"), ("Ursa Minor", "Little Dipper"),
        ("Cassiopeia", "W-shaped"), ("Leo", "Zodiac"), ("Scorpius", "Zodiac"),
        ("Taurus", "Zodiac"), ("Gemini", "Zodiac"), ("Cancer", "Zodiac"),
        ("Virgo", "Zodiac"), ("Libra", "Zodiac"), ("Sagittarius", "Zodiac"),
        ("Capricornus", "Zodiac"), ("Aquarius", "Zodiac"), ("Pisces", "Zodiac"),
        ("Cygnus", "Northern Cross"), ("Lyra", "Vega"), ("Aquila", "Altair"),
        ("Draco", "polar region"), ("Pegasus", "Great Square"),
        ("Andromeda", "galaxy"), ("Perseus", "mythology"), ("Auriga", "Capella"),
        ("Centaurus", "southern sky"), ("Crux", "Southern Cross"),
        ("Canis Major", "Sirius"), ("Canis Minor", "Procyon"),
        ("Boötes", "Arcturus"), ("Corona Borealis", "northern crown"),
    ]
    for name, hint in constellations:
        qs.append((
            f"Which constellation: {hint}?",
            [n for n, _ in constellations if n != name][:3] + [name],
            3, f"This is {name}.", "MEDIUM"
        ))

    for dist, unit, planet in [
        ("58", "million km", "Mercury"), ("108", "million km", "Venus"),
        ("150", "million km", "Earth"), ("228", "million km", "Mars"),
        ("778", "million km", "Jupiter"), ("1430", "million km", "Saturn"),
        ("2870", "million km", "Uranus"), ("4500", "million km", "Neptune"),
    ]:
        qs.append((
            f"Approximate distance of {planet} from the Sun?",
            [f"{int(dist)+50} {unit}", f"{dist} {unit}", f"{int(dist)-30} {unit}", f"{int(dist)+100} {unit}"],
            1, f"About {dist} {unit}.", "HARD"
        ))

    telescopes = [
        ("Galileo", "Jupiter's moons", "EASY"), ("Herschel", "Uranus", "HARD"),
        ("Halley", "comet orbit", "HARD"), ("Tombaugh", "Pluto", "HARD"),
        ("Kepler", "planetary motion laws", "MEDIUM"), ("Copernicus", "heliocentrism", "MEDIUM"),
        ("Ptolemy", "geocentrism", "HARD"), ("Eddington", "1919 eclipse", "HARD"),
        ("Hubble", "expanding universe", "HARD"), ("Leavitt", "Cepheid variables", "HARD"),
    ]
    for name, feat, diff in telescopes:
        qs.append((
            f"Which astronomer is associated with: {feat}?",
            [n for n, _, _ in telescopes if n != name][:3] + [name],
            3, f"This is {name}.", diff
        ))

    extras = [
        ("What is the Andromeda Galaxy?", ["Nearest large galaxy", "A planet", "A comet", "A moon"], 0, "Andromeda is our nearest large galactic neighbor.", "MEDIUM"),
        ("Sun's spectral class?", ["G dwarf", "Supergiant", "White dwarf", "Neutron star"], 0, "The Sun is a G-type yellow dwarf.", "HARD"),
        ("Where is the asteroid belt?", ["Between Mars and Jupiter", "Beyond Neptune", "Near the Sun", "Near the Moon"], 0, "Between Mars and Jupiter.", "MEDIUM"),
        ("Comet reservoir beyond Neptune?", ["Kuiper Belt and Oort Cloud", "Asteroid belt", "Ecliptic", "Milky Way"], 0, "Kuiper Belt and Oort Cloud.", "HARD"),
        ("How many moons does Earth have?", ["0", "1", "2", "12"], 1, "One — the Moon.", "EASY"),
        ("What is a meteor?", ["Shooting star", "Comet", "Planet", "Galaxy"], 0, "Bright trail of a meteoroid.", "EASY"),
        ("Moon phase when not visible?", ["Full Moon", "New Moon", "First Quarter", "Last Quarter"], 1, "New Moon.", "EASY"),
        ("Fully illuminated Moon phase?", ["New Moon", "Full Moon", "Eclipse", "Apogee"], 1, "Full Moon.", "EASY"),
        ("Main gas in the Sun?", ["Oxygen", "Nitrogen", "Hydrogen", "Helium"], 2, "Hydrogen ~73%.", "MEDIUM"),
        ("What is stellar astronomy?", ["Study of stars", "Study of rockets", "Study of climate", "Study of oceans"], 0, "Properties and evolution of stars.", "EASY"),
        ("What is a supernova?", ["Explosion of a massive star", "A flash", "Comet", "Eclipse"], 0, "Death of a massive star.", "HARD"),
        ("What is dark matter?", ["Invisible mass in the universe", "Black hole", "Planet", "Comet"], 0, "Does not emit light but has gravity.", "HARD"),
        ("What is dark energy?", ["Cause of accelerated expansion", "Star energy", "Solar wind", "Comet"], 0, "Explains universe acceleration.", "HARD"),
        ("Stars in the Milky Way (approx.)?", ["1000", "100 billion", "1 trillion", "10"], 1, "Estimate: 100–400 billion.", "HARD"),
        ("Nearest galaxy to us?", ["Andromeda", "Magellanic Clouds", "Triangulum", "Sombrero"], 1, "Small Magellanic Cloud is closer than Andromeda.", "HARD"),
        ("What is a solar flare?", ["Burst of energy from the Sun", "Planet", "Comet", "Moon"], 0, "Magnetic energy release on the Sun.", "HARD"),
        ("What is the Kuiper Belt?", ["Region beyond Neptune", "Between Mars and Jupiter", "Near the Sun", "Center of galaxy"], 0, "Icy bodies beyond Neptune.", "HARD"),
        ("What is the Oort Cloud?", ["Distant comet shell", "Asteroid belt", "Nebula", "Galaxy"], 0, "Theoretical comet reservoir.", "HARD"),
        ("What is a neutron star?", ["Dense stellar remnant", "Planet", "Comet", "Galaxy"], 0, "Remnant after supernova.", "HARD"),
        ("What is a white dwarf?", ["Remnant of medium-mass star", "Planet", "Comet", "Asteroid"], 0, "Final stage of Sun-like stars.", "HARD"),
        ("August meteor shower?", ["Perseids", "Leonids", "Geminids", "Orionids"], 0, "Perseids in August.", "MEDIUM"),
        ("November meteor shower?", ["Perseids", "Leonids", "Quadrantids", "Lyrids"], 1, "Leonids in November.", "HARD"),
        ("What is redshift?", ["Galaxies moving away", "Stars approaching", "Planets rotating", "Eclipse"], 0, "Spectrum shifts toward red.", "HARD"),
        ("Distance to Sirius (approx.)?", ["4 ly", "8 ly", "20 ly", "100 ly"], 1, "Sirius ~8.6 light-years.", "HARD"),
        ("What is the ecliptic?", ["Sun's apparent path", "Equator", "Meridian", "Horizon"], 0, "Plane of Earth's orbit.", "HARD"),
        ("What is the zodiac?", ["12 constellations along ecliptic", "Planets", "Comets", "Moons"], 0, "Zodiacal constellations.", "MEDIUM"),
        ("Baikonur Cosmodrome?", ["Plesetsk", "Baikonur", "Vostochny", "Kapustin Yar"], 1, "Main Russian launch site.", "MEDIUM"),
        ("Vostochny Cosmodrome?", ["Baikonur", "Vostochny", "Plesetsk", "Cape Canaveral"], 1, "New Russian cosmodrome.", "MEDIUM"),
        ("Largest moon in the Solar System?", ["Moon", "Ganymede", "Titan", "Callisto"], 1, "Ganymede orbits Jupiter.", "MEDIUM"),
    ]
    for item in extras:
        qs.append(item)

    for n in range(1, 101):
        correct = 1 if n != 8 else 0
        qs.append((
            f"How many planets in the Solar System (since 2006, not {n})?",
            [str(max(1, n - 1)), str(n), str(n + 1), str(n + 2)],
            correct,
            "8 planets." if n == 8 else f"Incorrect — the answer is 8, not {n}.",
            "EASY" if n <= 12 else "MEDIUM"
        ))
        if n == 8:
            break
    for n in range(1, 91):
        if n == 8:
            continue
        qs.append((
            f"Is it true that the Solar System has {n} planets (modern classification)?",
            ["Yes", "No", "Depends", "Unknown"],
            1, "No — there are 8 planets (Pluto is a dwarf planet).", "EASY"
        ))

    units = [
        ("parsec", "distance"), ("light-year", "distance"),
        ("AU", "Earth-Sun distance"), ("solar luminosity", "brightness"),
        ("solar mass", "stellar mass"), ("redshift z", "recession"),
    ]
    for unit, what in units:
        qs.append((
            f"The unit/concept \"{unit}\" measures: {what}?",
            ["Speed", what.capitalize(), "Temperature", "Time"],
            1, f"{unit} — {what}.", "HARD"
        ))
    return qs