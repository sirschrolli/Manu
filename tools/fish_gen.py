#!/usr/bin/env python3
"""
Die Fische der Biome (seit 0.81): 916 Arten – für jedes eigene Biom der Mod zwölf, für jedes Biom von Minecraft sechs oder
sieben. Zusammen mit den 84 von Hand benannten Arten (tools/fish_textures.py, FishSpecies.java) sind es genau 1000.

Jede Art entsteht aus ihrem Biom: Die Farbwelt des Bioms färbt sie, sein Gewässer bestimmt die Gestalt (Forelle, Wels, Hai,
Rochen, Krabbe, Qualle …), sein Wesen die Wirkung beim Essen. Keine zwei Arten heißen gleich, sehen gleich aus oder wirken
gleich. Alles wird aus festen Tabellen und einem eigenen Zufallsgenerator (unabhängig von der Python-Version) berechnet –
derselbe Aufruf ergibt immer dieselben Arten, neue Biome hängt man hinten an.

Geschrieben werden:
    src/main/resources/data/kingdomsatwar/fishing/species.tsv        die Arten (liest FishSpecies.java beim Laden)
    src/main/resources/data/kingdomsatwar/fishing/species_lang.tsv   Namen und Notizen, englisch und deutsch (liest die Datenerzeugung)
    src/main/resources/assets/kingdomsatwar/textures/item/fish/*.png die Bilder

Aufruf:
    python3 tools/fish_gen.py                  schreibt alles
    python3 tools/fish_gen.py --preview ORDNER nur Übersichtsbilder je Kapitel und eine Namensliste (zum Ansehen)
    python3 tools/fish_gen.py --check          rechnet alles durch und prüft, schreibt nichts
"""
from __future__ import annotations

import colorsys
import hashlib
import os
import re
import sys

from PIL import Image, ImageDraw

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import fish_textures as ft  # noqa: E402

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
DATA = os.path.join(ROOT, "src/main/resources/data/kingdomsatwar/fishing")
TEX = os.path.join(ROOT, "src/main/resources/assets/kingdomsatwar/textures/item/fish")
JAVA = os.path.join(ROOT, "src/main/java/io/github/sirschrolli/kingdomsatwar")
MASK = (1 << 64) - 1
MAX_NAME_DE = 23


# =============================================================================================
# Zufall (SplitMix64, aus einem Text gesät)
# =============================================================================================

class Rng:
    def __init__(self, seed: str):
        self.s = int.from_bytes(hashlib.sha256(seed.encode("utf-8")).digest()[:8], "big")

    def next(self) -> int:
        self.s = (self.s + 0x9E3779B97F4A7C15) & MASK
        z = self.s
        z = ((z ^ (z >> 30)) * 0xBF58476D1CE4E5B9) & MASK
        z = ((z ^ (z >> 27)) * 0x94D049BB133111EB) & MASK
        return z ^ (z >> 31)

    def f(self) -> float:
        return (self.next() >> 11) / float(1 << 53)

    def below(self, n: int) -> int:
        return min(n - 1, int(self.f() * n))

    def uni(self, a: float, b: float) -> float:
        return a + (b - a) * self.f()

    def pick(self, seq):
        return seq[self.below(len(seq))]

    def chance(self, p: float) -> bool:
        return self.f() < p

    def shuffled(self, seq) -> list:
        out = list(seq)
        for i in range(len(out) - 1, 0, -1):
            j = self.below(i + 1)
            out[i], out[j] = out[j], out[i]
        return out


# =============================================================================================
# Farben
# =============================================================================================

def rgb(h: str) -> tuple:
    h = h.lstrip("#")
    return int(h[0:2], 16), int(h[2:4], 16), int(h[4:6], 16)


def hx(c) -> str:
    return "#%02x%02x%02x" % (max(0, min(255, round(c[0]))), max(0, min(255, round(c[1]))), max(0, min(255, round(c[2]))))


def blend(a, b, t: float) -> tuple:
    return tuple(a[i] + (b[i] - a[i]) * t for i in range(3))


def lum(c) -> float:
    return (0.299 * c[0] + 0.587 * c[1] + 0.114 * c[2]) / 255.0


def shift(c, dh: float = 0.0, fs: float = 1.0, fv: float = 1.0) -> tuple:
    h, s, v = colorsys.rgb_to_hsv(c[0] / 255.0, c[1] / 255.0, c[2] / 255.0)
    h = (h + dh / 360.0) % 1.0
    s = max(0.0, min(1.0, s * fs))
    v = max(0.0, min(1.0, v * fv))
    r, g, b = colorsys.hsv_to_rgb(h, s, v)
    return r * 255.0, g * 255.0, b * 255.0


def dim(c, f: float) -> tuple:
    """Dunkler und etwas satter, der Farbton rückt ein wenig ins Kühle – wie Schatten in der Pixelkunst."""
    return shift(c, -6.0, 1.12, f)


WHITE = (255.0, 255.0, 255.0)
BLACK = (8.0, 8.0, 14.0)


# =============================================================================================
# Gestalten
# =============================================================================================

# Gestalt -> (kleinste, größte Länge in cm, Verhalten im Drill, Rang (für die Seltenheit), Namen "Deutsch/Englisch …")
FORMS = {
    "trout": (20, 60, "STEADY DARTER", 1.0, "Forelle/Trout Saibling/Char Äsche/Grayling Lachs/Salmon Renke/Whitefish Huchen/Taimen Maräne/Cisco Stint/Smelt"),
    "perch": (15, 45, "JITTER STEADY", 1.0, "Barsch/Perch Zander/Zander Kaulbarsch/Ruffe Schnapper/Snapper Zackenbarsch/Grouper Buntbarsch/Cichlid Wolfsbarsch/Bass Streber/Darter"),
    "carp": (25, 80, "CALM STEADY", 1.0, "Karpfen/Carp Schleie/Tench Karausche/Crucian Döbel/Chub Barbe/Barbel Koi/Koi Rotauge/Roach Rotfeder/Rudd Nase/Nase Aland/Ide"),
    "bream": (20, 60, "CALM STEADY", 1.0, "Brasse/Bream Meerbrasse/Porgy Goldbrasse/Gilthead Güster/Blicca Diskus/Discus"),
    "pike": (40, 120, "DARTER", 2.5, "Hecht/Pike Barrakuda/Barracuda Schmerle/Loach Pfeilhecht/Arrowpike Hundsfisch/Mudminnow"),
    "gar": (40, 150, "DARTER", 2.5, "Hornhecht/Garfish Knochenhecht/Gar Nadelfisch/Needlefish Schnabelfisch/Beakfish Halbschnäbler/Halfbeak"),
    "catfish": (50, 180, "SINKER", 2.0, "Wels/Catfish Waller/Sheatfish Quappe/Burbot Zwergwels/Bullhead Groppe/Sculpin"),
    "eel": (40, 120, "JITTER MIXED", 1.5, "Aal/Eel Neunauge/Lamprey Schleimaal/Hagfish Schlangenfisch/Snakefish Sandaal/Sandeel Wurmfisch/Wormfish"),
    "moray": (60, 200, "MIXED SINKER", 2.5, "Muräne/Moray Wolfsaal/Wolf_Eel Meeraal/Conger Schlangenaal/Snake_Eel"),
    "sturgeon": (80, 280, "SINKER", 4.0, "Stör/Sturgeon Sterlet/Sterlet Hausen/Beluga Löffelstör/Paddlefish"),
    "tuna": (60, 220, "DARTER STEADY", 3.0, "Thun/Tuna Makrele/Mackerel Bonito/Bonito Stöcker/Scad Stachelmakrele/Jack Wahoo/Wahoo"),
    "sword": (150, 380, "DARTER", 4.5, "Schwertfisch/Swordfish Speerfisch/Spearfish Marlin/Marlin Lanzenfisch/Lancefish"),
    "sail": (150, 340, "DARTER", 4.5, "Segelfisch/Sailfish Fächerfisch/Fanfish Fahnenfisch/Bannerfish Segler/Sailer"),
    "round": (10, 40, "FLOATER CALM", 2.0, "Kugelfisch/Puffer Igelfisch/Porcupinefish Ballonfisch/Balloonfish Kugler/Globefish"),
    "box": (10, 40, "CALM FLOATER", 2.0, "Kofferfisch/Boxfish Kuhfisch/Cowfish Würfelfisch/Cubefish"),
    "sunfish": (80, 280, "FLOATER", 4.0, "Mondfisch/Moonfish Scheibenfisch/Discfish Sonnenfisch/Sunfish Tellerfisch/Platefish Glanzfisch/Opah"),
    "flat": (25, 90, "SINKER", 2.0, "Scholle/Plaice Flunder/Flounder Steinbutt/Turbot Seezunge/Sole Kliesche/Dab Butt/Brill"),
    "angler": (20, 100, "SINKER MIXED", 3.0, "Angler/Angler Seeteufel/Monkfish Schlinger/Gulper Froschfisch/Frogfish Köderfisch/Lurefish"),
    "viper": (15, 60, "DARTER JITTER", 3.0, "Viperfisch/Viperfish Drachenfisch/Dragonfish Zahnfisch/Fangfish Säbelzahn/Sabertooth"),
    "hatchet": (4, 14, "FLOATER JITTER", 0.0, "Beilfisch/Hatchetfish Axtfisch/Axefish Klingenfisch/Bladefish Sichelfisch/Sicklefish"),
    "small": (4, 15, "JITTER STEADY FLOATER", 0.0,
              "Elritze/Minnow Stichling/Stickleback Grundel/Goby Sprotte/Sprat Sardelle/Anchovy Ukelei/Bleak Gründling/Gudgeon Bitterling/Bitterling "
              "Salmler/Tetra Kärpfling/Killifish Sardine/Sardine Guppy/Guppy Ährenfisch/Silverside Bärbling/Rasbora Zwergbarsch/Pygmy_Perch"),
    "ribbon": (120, 500, "MIXED FLOATER", 4.0, "Bandfisch/Ribbonfish Riemenfisch/Oarfish Degenfisch/Scabbardfish Wimpelfisch/Pennantfish Haarschwanz/Hairtail"),
    "shark": (60, 250, "DARTER MIXED", 4.0, "Hai/Shark Dornhai/Dogfish Glatthai/Houndshark Katzenhai/Catshark Ammenhai/Nurse_Shark Riffhai/Reef_Shark"),
    "coel": (80, 200, "SINKER CALM", 5.0, "Quastenflosser/Coelacanth Lungenfisch/Lungfish Flösselhecht/Bichir Urfisch/Elderfish"),
    "goldfish": (8, 30, "FLOATER CALM", 1.0, "Schleierschwanz/Veiltail Goldfisch/Goldfish Kampffisch/Betta Fächerschwanz/Fantail Prachtfisch/Splendorfish"),
    "piranha": (15, 45, "DARTER JITTER", 2.0, "Piranha/Piranha Pacu/Pacu Sägesalmler/Sawtetra Beißer/Biter Schnappfisch/Snapfish"),
    "arapaima": (100, 300, "SINKER DARTER", 4.0, "Arapaima/Arapaima Arowana/Arowana Knochenzüngler/Bonytongue Schlangenkopf/Snakehead"),
    "skipper": (6, 20, "JITTER", 0.0, "Springer/Skipper Hüpfer/Hopper Kletterfisch/Climbing_Perch Schleimfisch/Blenny"),
    "armored": (30, 150, "SINKER", 3.0, "Panzerfisch/Armorfish Harnischwels/Pleco Schildfisch/Shieldfish Panzerhecht/Platepike"),
    "angel": (8, 30, "FLOATER JITTER", 2.0, "Skalar/Angelfish Falterfisch/Butterflyfish Kaiserfisch/Emperorfish Segelflosser/Sailfin"),
    "flyer": (15, 45, "DARTER FLOATER", 2.0, "Flugfisch/Flyingfish Schwalbenfisch/Swallowfish Gleiter/Glider Flatterfisch/Flutterfish"),
    "mahi": (60, 180, "DARTER STEADY", 3.5, "Goldmakrele/Dolphinfish Dorade/Dorado Stirnfisch/Browfish Mähnenfisch/Manefish"),
    # Wesen ohne Fischgestalt
    "ray": (50, 250, "FLOATER SINKER", 3.5, "Rochen/Ray Stechrochen/Stingray Manta/Manta Zitterrochen/Torpedo"),
    "squid": (15, 120, "MIXED DARTER", 2.5, "Kalmar/Squid Tintenfisch/Cuttlefish Sepia/Sepia"),
    "octopus": (30, 150, "MIXED SINKER", 3.0, "Krake/Octopus Oktopus/Devilfish"),
    "seahorse": (5, 30, "FLOATER", 2.0, "Seepferdchen/Seahorse Seenadel/Pipefish Seedrache/Seadragon"),
    "isopod": (8, 45, "SINKER", 2.0, "Assel/Isopod Flohkrebs/Amphipod Panzerassel/Pillbug"),
    "crab": (6, 40, "SINKER JITTER", 1.0, "Krabbe/Crab Taschenkrebs/Rock_Crab Einsiedler/Hermit Winkerkrabbe/Fiddler Spinnenkrabbe/Spider_Crab"),
    "shrimp": (4, 30, "JITTER", 0.5, "Garnele/Shrimp Krill/Krill Schwebgarnele/Mysid"),
    "lobster": (20, 60, "SINKER JITTER", 2.0, "Krebs/Crayfish Hummer/Lobster Languste/Langouste"),
    "jelly": (10, 120, "FLOATER", 2.0, "Qualle/Jellyfish Meduse/Medusa Nesselqualle/Sea_Nettle Schirmqualle/Belljelly"),
    "snail": (3, 20, "CALM", 0.0, "Schnecke/Snail Napfschnecke/Limpet Kauri/Cowrie Wellhorn/Whelk"),
    "starfish": (8, 40, "CALM SINKER", 1.0, "Seestern/Starfish Schlangenstern/Brittlestar Sonnenstern/Sunstar"),
    "olm": (10, 35, "CALM JITTER", 1.5, "Molch/Newt Olm/Olm Salamander/Salamander Lurch/Mudpuppy"),
    "nautilus": (15, 30, "FLOATER", 3.5, "Perlboot/Nautilus Ammonit/Ammonite"),
    "trilobite": (5, 30, "SINKER", 3.5, "Trilobit/Trilobite Pfeilschwanz/Horseshoe_Crab"),
}

# Gewässer -> die Gestalten, die dort leben (mehrfach genannt = häufiger gewählt)
POOLS = {
    "fresh": "trout perch carp pike catfish eel small small goldfish sturgeon bream gar lobster snail olm",
    "fresh_cold": "trout trout perch pike small sturgeon catfish eel bream gar lobster olm carp snail shrimp",
    "fresh_warm": "piranha arapaima catfish eel small goldfish carp perch bream skipper gar lobster snail olm",
    "swamp": "catfish eel carp skipper pike small bream gar lobster snail olm arapaima perch",
    "mountain": "trout trout small pike perch sturgeon eel gar lobster olm bream carp catfish snail shrimp",
    "dry": "small perch carp catfish skipper gar goldfish eel snail lobster bream pike olm piranha",
    "sea": "tuna sword sail shark flat round sunfish small perch moray ray squid octopus seahorse crab shrimp jelly starfish ribbon mahi flyer box bream",
    "sea_cold": "tuna shark flat small perch moray ray squid crab shrimp jelly starfish ribbon sturgeon coel bream lobster sunfish",
    "sea_warm": "angel box round mahi flyer sail small moray ray seahorse crab shrimp jelly starfish octopus squid tuna shark perch sunfish",
    "beach": "flat small crab shrimp starfish snail jelly ray seahorse perch flyer round lobster bream",
    "deep": "angler viper hatchet ribbon coel shark squid isopod jelly small armored nautilus moray flat octopus trilobite",
    "cave": "small eel catfish gar isopod shrimp snail olm armored pike trilobite lobster perch",
    "lava": "armored carp perch pike moray catfish ray angler piranha shark coel crab snail isopod gar bream sturgeon viper lobster",
    "void": "ray ribbon sunfish hatchet small eel tuna sail coel squid jelly nautilus seahorse flyer angel box mahi starfish trilobite",
    "fae": "goldfish round small carp sail seahorse perch eel jelly olm snail angel flyer bream box trout mahi shrimp",
    "shadow": "pike moray catfish viper angler hatchet shark ribbon small squid isopod coel eel perch carp ray jelly olm armored gar",
    "abyss": "armored viper angler shark moray ray coel piranha squid isopod trilobite nautilus crab jelly ribbon hatchet lobster",
    "ash": "carp perch pike catfish small eel gar snail lobster bream piranha olm",
}

# =============================================================================================
# Wirkungen beim Essen
# =============================================================================================

# Wirkung -> Grunddauer in Sekunden (wird je nach Seltenheit vervielfacht), höchste Stufe
GOOD = {
    "speed": (30, 2), "haste": (30, 2), "strength": (20, 2), "jump_boost": (30, 2), "regeneration": (5, 1), "resistance": (25, 1),
    "fire_resistance": (45, 0), "water_breathing": (45, 0), "invisibility": (25, 0), "night_vision": (60, 0), "health_boost": (60, 2),
    "absorption": (40, 2), "luck": (60, 2), "slow_falling": (30, 0), "conduit_power": (30, 0), "dolphins_grace": (25, 0),
}
# Nebenwirkung -> (kürzeste, längste Dauer)
BAD = {
    "poison": (4, 8), "wither": (3, 5), "hunger": (10, 20), "nausea": (6, 10), "blindness": (4, 6), "darkness": (5, 8), "slowness": (10, 25),
    "weakness": (10, 25), "mining_fatigue": (10, 20), "glowing": (20, 60), "levitation": (2, 5),
}
THEMES = {
    "fire": ("fire_resistance strength haste speed absorption resistance", "hunger weakness"),
    "ice": ("resistance absorption health_boost water_breathing slow_falling night_vision", "slowness mining_fatigue"),
    "storm": ("speed jump_boost haste dolphins_grace strength slow_falling", "glowing nausea"),
    "forest": ("regeneration luck health_boost jump_boost speed haste night_vision", "hunger"),
    "earth": ("haste resistance strength absorption night_vision health_boost", "slowness mining_fatigue"),
    "sea": ("water_breathing dolphins_grace conduit_power speed strength regeneration luck", "nausea"),
    "deep": ("night_vision water_breathing conduit_power resistance strength invisibility", "darkness slowness glowing"),
    "swamp": ("regeneration night_vision jump_boost luck water_breathing resistance strength", "poison nausea hunger"),
    "dry": ("fire_resistance speed haste jump_boost strength luck", "hunger"),
    "cave": ("night_vision haste resistance invisibility absorption strength", "blindness mining_fatigue darkness"),
    "sulfur": ("fire_resistance haste strength night_vision resistance speed", "poison nausea hunger"),
    "shadow": ("invisibility night_vision strength speed resistance slow_falling", "blindness darkness weakness wither"),
    "soul": ("regeneration invisibility speed night_vision absorption slow_falling", "wither weakness darkness"),
    "void": ("slow_falling jump_boost luck absorption invisibility speed night_vision", "levitation nausea glowing"),
    "star": ("luck night_vision speed slow_falling absorption haste regeneration", "glowing levitation"),
    "fae": ("luck regeneration jump_boost speed health_boost slow_falling haste", "glowing nausea"),
    "mushroom": ("jump_boost regeneration luck health_boost night_vision resistance", "nausea poison hunger"),
    "moon": ("night_vision regeneration slow_falling luck invisibility health_boost water_breathing", "glowing"),
    "thorn": ("strength resistance speed haste regeneration absorption", "poison hunger"),
    "abyss": ("strength fire_resistance resistance speed absorption slow_falling invisibility", "wither levitation darkness hunger"),
    "chaos": ("strength speed jump_boost luck invisibility absorption haste", "levitation nausea wither blindness"),
    "royal": ("luck absorption strength regeneration health_boost resistance haste", "glowing"),
    "meadow": ("speed regeneration haste luck jump_boost health_boost", "hunger"),
    "mountain": ("jump_boost speed resistance haste slow_falling regeneration strength", "hunger"),
    "jungle": ("speed jump_boost strength regeneration health_boost night_vision luck", "poison hunger"),
    "crimson": ("strength fire_resistance health_boost regeneration resistance speed", "hunger"),
    "warped": ("fire_resistance invisibility jump_boost speed luck slow_falling", "nausea"),
    "nethersoul": ("speed fire_resistance regeneration night_vision invisibility absorption", "wither"),
    "basalt": ("resistance fire_resistance strength haste absorption health_boost", "slowness"),
    "end": ("slow_falling luck jump_boost invisibility absorption speed strength", "levitation"),
    "crystal": ("absorption resistance luck night_vision haste health_boost strength", "glowing slowness"),
    "sharp": ("strength speed haste jump_boost invisibility resistance", "poison wither hunger"),
}
# Themen, in denen einzelne Arten versetzen oder Leiden nehmen
TELEPORT = {"void": "teleport_far", "end": "teleport_far", "chaos": "teleport_far", "warped": "teleport_near", "star": "teleport_near"}
CLEANSE = ("soul", "moon", "fae", "royal", "forest")

RARITIES = ("COMMON", "UNCOMMON", "RARE", "EPIC")
SPLIT = {12: (4, 4, 3, 1), 7: (3, 2, 1, 1), 6: (2, 2, 1, 1)}

# =============================================================================================
# Die Biome
# =============================================================================================
# Kopfzeile: Biom | Kapitel | Arten | Medien (W Wasser, L Lava, V Leere) | vier Farben (Grund, dunkel, Akzent, hell) | Gewässer | Wesen
#            [| glow=Anteil leuchtender Arten] [| eyes (helle Augen)] [| fixed (keine Tageszeit)]
# danach:    Vorsilben "Deutsch/Englisch"
#            je Medium "W: wo (deutsch) | where (englisch)"

BIOME_TABLE = """
kingdomsatwar:ash_fields | rivers | 12 | WL | #6a5a52 #2a2226 #f0782a #e8d8c0 | ash | fire | glow=.2
Aschen/Ash Glut/Ember Schlacken/Slag Ruß/Soot Funken/Spark Rauch/Smoke Kohlen/Coal Zunder/Tinder Glimm/Smolder Brand/Scorch Bims/Pumice Schlot/Vent
W: in den warmen Tümpeln der Aschefelder | in the warm pools of the Ash Fields
L: in den Lavaadern der Aschefelder | in the lava veins of the Ash Fields

kingdomsatwar:frost_peaks | rivers | 12 | W | #7ab0d8 #2a4a7a #f0a8c8 #f4fbff | fresh_cold | ice
Frost/Frost Firn/Firn Gletscher/Glacier Eis/Ice Schnee/Snow Reif/Rime Gipfel/Summit Lawinen/Avalanche Kristall/Crystal Hagel/Hail Grat/Ridge Winter/Winter
W: in den Schmelzwasserseen der Frostgipfel | in the meltwater lakes of the Frost Peaks

kingdomsatwar:storm_coast | sea | 12 | W | #5a6a82 #1e2a44 #f4e04a #dce6f0 | sea_cold | storm
Sturm/Storm Brandungs/Surf Gischt/Spray Wetter/Tempest Böen/Gust Donner/Thunder Klippen/Cliff Wogen/Billow Orkan/Gale Blitz/Lightning Regen/Rain Wrack/Wreck
W: in der Brandung der Sturmküste | in the surf of the Storm Coast

kingdomsatwar:primeval_forest | rivers | 12 | W | #4a7a2e #2a3a1a #e8a82a #e8e8b0 | fresh | forest
Ur/Primeval Moos/Moss Farn/Fern Wurzel/Root Rinden/Bark Harz/Resin Riesen/Giant Laub/Leaf Dickicht/Thicket Bernstein/Amber Lianen/Vine Eichen/Oak
W: in den Waldseen des Urhains | in the forest lakes of the Primeval Forest

kingdomsatwar:rock_gorges | rivers | 12 | W | #b08a5a #4a3a2e #d8502a #f0e0c0 | mountain | earth
Fels/Rock Schlucht/Gorge Klamm/Ravine Kiesel/Pebble Geröll/Scree Stein/Stone Wildwasser/Whitewater Kluft/Cleft Schiefer/Slate Granit/Granite Schotter/Gravel Sturzbach/Torrent
W: in den Wildbächen der Felsschluchten | in the torrents of the Rock Gorges

kingdomsatwar:coral_coast | sea | 12 | W | #f0708a #2a6a8a #ffd24a #e0fff8 | sea_warm | sea
Korallen/Coral Riff/Reef Lagunen/Lagoon Perlmutt/Nacre Anemonen/Anemone Atoll/Atoll Muschel/Shell Tropen/Tropic Türkis/Turquoise Sonnen/Sun Schwamm/Sponge Fächer/Fan
W: zwischen den Riffen der Korallenküste | among the reefs of the Coral Coast

kingdomsatwar:royal_meadows | rivers | 12 | W | #3a5ac8 #1e2a6a #f0c02a #f8f0e0 | fresh | royal
Königs/King Kron/Crown Wappen/Crest Auen/Meadow Lilien/Lily Gold/Gold Herolds/Herald Zepter/Scepter Purpur/Purple Hof/Court Ritter/Knight Banner/Banner
W: in den klaren Teichen der Königsauen | in the clear ponds of the Royal Meadows

kingdomsatwar:mist_moor | rivers | 12 | W | #6a7a5a #2e3a2a #7ae8d0 #d8e0d0 | swamp | swamp | glow=.15
Nebel/Mist Moor/Bog Torf/Peat Irrlicht/Wisp Dunst/Haze Schlick/Silt Binsen/Rush Ried/Fen Wollgras/Cottongrass Schwaden/Vapor Tümpel/Pool Grau/Grey
W: in den Moorlöchern des Nebelmoors | in the bog holes of the Mist Moor

kingdomsatwar:ashlands | rivers | 12 | WL | #3a2a2a #141014 #ff5a1a #ffd890 | lava | fire | glow=.4 | eyes
Krater/Crater Lava/Lava Magma/Magma Obsidian/Obsidian Feuer/Fire Drachenglut/Dragonfire Aschland/Ashland Flammen/Flame Lohen/Blaze Vulkan/Volcano Schmelz/Molten Zinder/Cinder
W: in den heißen Quellen der Aschelande | in the hot springs of the Ashlands
L: in den Lavaseen der Aschelande | in the lava lakes of the Ashlands

kingdomsatwar:frostbasin | rivers | 12 | W | #5ad0e8 #1e4a7a #b48cf0 #f0feff | fresh_cold | ice | glow=.15
Dauerfrost/Permafrost Blaueis/Blue_Ice Polar/Polar Raureif/Hoarfrost Zapfen/Icicle Schollen/Floe Nordlicht/Aurora Kälte/Chill Glaseis/Glaze Spiegel/Mirror Winterherz/Winterheart Eisdrachen/Icedrake
W: unter dem Eis des Frostkessels | beneath the ice of the Frost Basin

kingdomsatwar:rugged_wilds | rivers | 12 | W | #8a6a4a #3a2e26 #3ac88a #e8dcc0 | mountain | earth
Erd/Earth Lehm/Clay Geoden/Geode Erz/Ore Spalten/Fissure Wild/Wild Smaragd/Emerald Topas/Topaz Karst/Karst Quarz/Quartz Krumen/Loam Schrunden/Crag
W: in den Spaltenseen der Zerklüfteten Weiten | in the fissure lakes of the Rugged Wilds

kingdomsatwar:ancient_grove | rivers | 12 | W | #2e8a4a #1a3a26 #f08ac8 #f0f8c8 | fresh | forest | glow=.15
Hain/Grove Knospen/Bud Ranken/Tendril Tau/Dew Lebens/Life Efeu/Ivy Jade/Jade Nektar/Nectar Samen/Seed Wipfel/Canopy Grünlicht/Greenlight Altholz/Elderwood
W: in den Quellteichen des Uralten Hains | in the spring ponds of the Ancient Grove

kingdomsatwar:storm_cliffs | rivers | 12 | W | #4a5a7a #1a2238 #5af0ff #f8f4a0 | mountain | storm | glow=.2
Gewitter/Thunderhead Wolken/Cloud Zacken/Jagged Volt/Volt Knister/Crackle Windbruch/Windfall Strom/Current Schlag/Strike Leucht/Flash Himmels/Sky Regenguss/Downpour Sturmhorn/Stormhorn
W: in den Regenbecken der Sturmklippen | in the rain basins of the Storm Cliffs

kingdomsatwar:abyssal_grotto | caves | 12 | W | #1e6a7a #0e2a3a #5affe0 #e8f8f0 | cave | deep | glow=.45 | eyes
Grotten/Grotto Gezeiten/Tide Perl/Pearl Nixen/Nixie Algen/Algae Tropf/Drip Hall/Chime Dämmer/Dusk Brunnen/Well Schleier/Veil Tang/Kelp Sirenen/Siren
W: in den stillen Wassern der Abgrundgrotte | in the still waters of the Abyssal Grotto

kingdomsatwar:shadow_forest | shadow | 12 | W | #4a2a6a #1a0e2a #b46cff #c8b8e0 | shadow | shadow | glow=.35 | eyes | fixed
Schatten/Shadow Dunkel/Dark Nacht/Night Zwielicht/Twilight Finster/Murk Raben/Raven Schwarzholz/Blackwood Umbra/Umbral Schemen/Wraith Trauer/Mourning Tinten/Ink Neumond/Newmoon
W: in den schwarzen Weihern des Schattenwalds | in the black ponds of the Shadow Forest

kingdomsatwar:pale_wastes | shadow | 12 | W | #c8c0b8 #5a5460 #8a6ad0 #f4f0ec | shadow | shadow | glow=.15 | fixed
Bleich/Pale Knochen/Bone Öd/Waste Staub/Dust Kalk/Chalk Geister/Ghost Fahl/Wan Gebein/Skeletal Salz/Salt Still/Silent Elfenbein/Ivory Schädel/Skull
W: in den Salztümpeln der Bleichen Öde | in the salt pools of the Pale Wastes

kingdomsatwar:crystal_chasms | shadow | 12 | W | #7a4ad8 #22104a #e8b0ff #f0e8ff | shadow | crystal | glow=.45 | eyes | fixed
Nachtkristall/Nightcrystal Amethyst/Amethyst Prismen/Prism Facetten/Facet Drusen/Druse Glanz/Gleam Spat/Spar Violett/Violet Schliff/Cut Funkel/Sparkle Kluftlicht/Chasmlight Juwelen/Jewel
W: in den spiegelnden Becken der Kristallschluchten | in the mirrored basins of the Crystal Chasms

kingdomsatwar:soul_marsh | shadow | 12 | W | #3a7a8a #12303a #8af8ff #d8fcff | swamp | soul | glow=.5 | eyes | fixed
Seelen/Soul Sumpflicht/Marshlight Klage/Lament Wiedergänger/Revenant Toten/Dead Spuk/Haunt Leichentuch/Shroud Flüster/Whisper Jenseits/Beyond Grab/Grave Tränen/Tear Raunen/Murmur
W: im trüben Wasser des Seelensumpfs | in the murky water of the Soul Marsh

kingdomsatwar:shadow_peaks | shadow | 12 | W | #5a5a7a #16162a #d8d8ff #a8a8d0 | mountain | shadow | glow=.2 | eyes | fixed
Schattengrat/Shadowridge Schwarzfels/Blackrock Onyx/Onyx Höhen/Highland Mondstein/Moonstone Kaltwind/Coldwind Nachtfrost/Nightfrost Dunkelfirn/Darkfirn Zinnen/Pinnacle Abhang/Scarp Steilwand/Bluff Dohlen/Jackdaw
W: in den kalten Karseen der Schattengipfel | in the cold tarns of the Shadow Peaks

kingdomsatwar:glimmer_meadows | shadow | 12 | W | #3ac8b0 #1a3a4a #ffb0e0 #e8fff8 | fae | fae | glow=.6 | fixed
Schimmer/Shimmer Glüh/Glow Sternblumen/Starflower Funkeltau/Sparkledew Lichtgras/Lightgrass Nachtblüten/Nightbloom Glanzlicht/Glint Leuchtkäfer/Firefly Mondtau/Moondew Flimmer/Flicker Phosphor/Phosphor Sanftlicht/Softlight
W: in den leuchtenden Bächen der Glimmerweiten | in the glowing brooks of the Glimmer Meadows

kingdomsatwar:abyssal_sea | shadow | 12 | W | #1e2a5a #080c20 #3ae8c8 #a8b8e8 | deep | deep | glow=.55 | eyes | fixed
Schwarzwasser/Blackwater Tiefnacht/Deepnight Tiefenlot/Fathomless Nachtmeer/Nightsea Dunkelflut/Darktide Tintenmeer/Inksea Leviathan/Leviathan Malstrom/Maelstrom Untiefen/Shoal Schweige/Hush Schwarzhimmel/Starless Kalttiefen/Coldfathom
W: in den lichtlosen Wassern des Abgrundmeers | in the lightless waters of the Abyssal Sea

kingdomsatwar:floating_isles | shadow | 12 | WV | #9a8ad0 #3a2a6a #f0e0a0 #f0ecff | fae | void | glow=.3 | fixed
Schwebe/Float Insel/Isle Himmelsriff/Skyreef Luft/Air Wind/Wind Feder/Feather Drift/Drift Äther/Aether Segelwind/Sailwind Auftrieb/Updraft Fallwind/Downdraft Saum/Fringe
W: in den Teichen der Schwebenden Inseln | in the ponds of the Floating Isles
V: im Nichts unter den Schwebenden Inseln | in the nothing beneath the Floating Isles

kingdomsatwar:shadow_depths | shadow | 12 | W | #2a1a3a #0a0612 #ff5ac8 #8a6ab0 | cave | cave | glow=.6 | eyes | fixed
Tiefen/Depth Stollen/Tunnel Blind/Blind Gruft/Crypt Ader/Vein Schacht/Shaft Kavernen/Cavern Pech/Pitch Unterwelt/Underworld Grubenlicht/Pitlight Stumm/Mute Kriech/Crawl
W: in den Höhlenseen der Schattentiefen | in the cave lakes of the Shadow Depths

kingdomsatwar:void_expanse | end | 12 | VW | #1a1a3a #06060f #6a8aff #c8d0ff | void | void | glow=.6 | eyes | fixed
Nichts/Null Leeren/Void Schwarzraum/Blackspace Fern/Far Weiten/Endless Vakuum/Vacuum Finsternis/Eclipse Außen/Outer Tiefraum/Deepspace Leerlicht/Voidlight Horizont/Horizon Niemands/Nowhere
V: im Sternennichts, weit unter den letzten Inseln | in the Starless Expanse, far below the last islands

kingdomsatwar:void_crystal_forest | end | 12 | VW | #5ad8f0 #123a5a #ffffff #e0fbff | void | crystal | glow=.5 | fixed
Klirr/Clink Quarzlicht/Quartzlight Spektral/Spectral Eisglas/Iceglass Lichtbruch/Refraction Diamant/Diamond Saphir/Sapphire Opal/Opal Klarstein/Clearstone Reinlicht/Purelight Strahl/Beam Glaslaub/Glassleaf
V: zwischen den Stämmen des Kristallwalds | among the trunks of the Crystal Forest

kingdomsatwar:void_ruins | end | 12 | VW | #8a7a9a #2a2238 #f0c84a #e0d8c8 | void | void | glow=.3 | fixed
Ruinen/Ruin Säulen/Pillar Trümmer/Rubble Vorzeit/Forgotten Leichtstein/Weightless Bogen/Arch Relikt/Relic Grabmal/Tomb Erbauer/Builder Ewigkeits/Timeless Fresken/Fresco Tempel/Temple
V: zwischen den treibenden Mauern der Schwerelosen Ruinen | among the drifting walls of the Weightless Ruins

kingdomsatwar:void_stardust_fields | end | 12 | VW | #3a3a8a #12123a #fff0a0 #fffbe0 | void | star | glow=.7 | eyes | fixed
Sternenstaub/Stardust Kometen/Comet Nebula/Nebula Stern/Star Funkelstaub/Glitterdust Milchstraßen/Galaxy Lichtkorn/Lightgrain Sternschnuppen/Meteor Plejaden/Pleiad Goldstaub/Golddust Nova/Nova Glitzer/Glitter
V: im Glitzern der Sternenstaubfelder | in the glitter of the Stardust Fields

kingdomsatwar:void_shard_cliffs | end | 12 | VW | #2a6a6a #0a1e22 #a8fff0 #d0f0e8 | void | sharp | glow=.4 | eyes | fixed
Splitter/Shard Scherben/Sherd Schneiden/Razor Glasklippen/Glasscliff Scharten/Notch Spitz/Spike Bruchglas/Brokenglass Säge/Saw Keil/Wedge Sichel/Sickle Stachel/Sting Spalt/Split
V: in den Rissen der Splitterklippen | in the cracks of the Shard Cliffs

kingdomsatwar:void_world_remnants | end | 12 | VW | #5a8a4a #2a2a3a #e8a04a #e0e8d0 | void | forest | glow=.2 | fixed
Welten/World Nachwelt/Afterworld Erinnerungs/Memory Treib/Flotsam Bruchstück/Fragment Heimweh/Homesick Erdschollen/Clod Altwelt/Oldworld Überrest/Vestige Strandgut/Jetsam Garten/Garden Eiland/Islet
V: zwischen den treibenden Brocken der Weltenreste | among the drifting chunks of the World Remnants

kingdomsatwar:fae_meadows | fae | 12 | W | #f08ac0 #8a3a7a #ffe04a #fff0f8 | fae | fae | glow=.3 | fixed
Blüten/Blossom Feen/Fairy Honig/Honey Klee/Clover Rosen/Rose Falter/Butterfly Pollen/Pollen Krokus/Crocus Tulpen/Tulip Zucker/Sugar Elfen/Elf Sommer/Summer
W: in den Bächen der Blütenwiesen | in the brooks of the Blossom Meadows

kingdomsatwar:glimmerwood | fae | 12 | W | #2ac8a8 #1a4a4a #ffe890 #e0fff0 | fae | fae | glow=.6 | fixed
Glimmer/Glimmer Laternen/Lantern Zauber/Spell Leuchtmoos/Glowmoss Waldgeist/Sprite Mondholz/Moonwood Wunder/Wonder Schein/Sheen Traum/Dream Sagen/Fable Märchen/Fairytale Kobold/Kobold
W: in den Weihern des Glimmerwalds | in the pools of the Glimmerwood

kingdomsatwar:fae_mushroom_grove | fae | 12 | W | #b04ad8 #4a1a6a #5af0d0 #fff0d8 | fae | mushroom | glow=.4 | fixed
Pilz/Mushroom Sporen/Spore Hut/Cap Lamellen/Gill Myzel/Mycel Fliegenpilz/Toadstool Morchel/Morel Trüffel/Truffle Hexenring/Fairyring Stäubling/Puffball Täubling/Russula Röhrling/Bolete
W: im Schatten der Riesenpilze des Pilzhains | in the shade of the giant mushrooms of the Mushroom Grove

kingdomsatwar:moonlit_lakes | fae | 12 | W | #8ab8f0 #2a3a7a #fff8d0 #f4f8ff | fae | moon | glow=.45 | fixed
Mond/Moon Silber/Silver Vollmond/Fullmoon Sichelmond/Crescent Nachtglanz/Nightgleam Spiegelsee/Mirrorlake Seerosen/Waterlily Luna/Luna Perlmond/Pearlmoon Lotus/Lotus Stillwasser/Stillwater Mondstaub/Moondust
W: im Silberwasser der Mondseen | in the silver water of the Moonlit Lakes

kingdomsatwar:thornwild | fae | 12 | W | #3a6a2a #1e2a16 #d83a6a #e8f0c0 | swamp | thorn | fixed
Dornen/Thorn Brombeer/Bramble Distel/Thistle Nessel/Nettle Hecken/Hedge Kratz/Scratch Hagebutten/Rosehip Schlehen/Sloe Wildrosen/Briar Gestrüpp/Scrub Gift/Venom Kletten/Burr
W: in den Tümpeln der Dornenwildnis | in the pools of the Thornwild

kingdomsatwar:abyss_fractured_lands | abyss | 12 | WLV | #4a3a4a #1a1218 #ff7a2a #c8b8c0 | abyss | abyss | glow=.4 | eyes | fixed
Bruch/Fracture Riss/Rift Narben/Scar Beben/Quake Verwerfungs/Fault Schutt/Debris Sprung/Crack Zerfall/Decay Grenz/Border Kipp/Tilt Schrund/Crevasse Malm/Grind
W: in den Pfützen der Bruchlande | in the puddles of the Fractured Lands
L: in den Glutrissen der Bruchlande | in the glowing cracks of the Fractured Lands
V: im Nichts zwischen den Schollen der Bruchlande | in the nothing between the slabs of the Fractured Lands

kingdomsatwar:abyss_ember_falls | abyss | 12 | WLV | #a83a1a #2a0c08 #ffd24a #ffc8a0 | abyss | fire | glow=.55 | eyes | fixed
Glutsturz/Emberfall Kaskaden/Cascade Sturzglut/Plungefire Lavafall/Lavafall Schmelzfluss/Meltflow Rotglut/Redheat Weißglut/Whiteheat Essen/Forge Gieß/Pour Tropfglut/Dripfire Hitze/Heat Glühfaden/Filament
W: in den dampfenden Becken des Glutsturzes | in the steaming basins of the Ember Falls
L: in den Lavafällen des Glutsturzes | in the lava falls of the Ember Falls
V: im Nichts unter dem Glutsturz | in the nothing beneath the Ember Falls

kingdomsatwar:abyss_star_wreck | abyss | 12 | WLV | #5a7ab8 #141e3a #ffe890 #e8f0ff | abyss | star | glow=.6 | eyes | fixed
Sternwrack/Starwreck Einschlag/Impact Sternmetall/Starmetal Himmelseisen/Skyiron Absturz/Crash Sternsplitter/Starshard Schweif/Comettail Fallstern/Fallenstar Sternenerz/Starore Weltraum/Cosmic Trabanten/Satellite Sternkern/Starcore
W: in den Kraterseen der Sternentrümmer | in the crater lakes of the Star Wreck
L: in der Schmelze der Sternentrümmer | in the melt of the Star Wreck
V: im Nichts zwischen den Sternentrümmern | in the nothing among the Star Wreck

kingdomsatwar:abyss_shadow_chasm | abyss | 12 | WLV | #3a2a5a #0c0818 #c05aff #a898c8 | abyss | shadow | glow=.5 | eyes | fixed
Schlund/Maw Rachen/Gullet Kehlen/Throat Schwärze/Blackness Höllen/Hell Tiefschatten/Deepshadow Fraß/Devourer Lichtfraß/Lighteater Dunkelsog/Darkpull Abgrund/Abyss Sog/Undertow Schattenfall/Shadowfall
W: in den schwarzen Lachen des Schattenschlunds | in the black pools of the Shadow Chasm
L: in der dunklen Glut des Schattenschlunds | in the dark embers of the Shadow Chasm
V: im Nichts des Schattenschlunds | in the nothing of the Shadow Chasm

kingdomsatwar:abyss_seam | abyss | 12 | WLV | #7a4ac8 #1c1026 #ff8a3a #e8d0ff | abyss | chaos | glow=.5 | eyes | fixed
Naht/Seam Chaos/Chaos Faden/Thread Flick/Patch Zwischen/Between Weltennaht/Worldseam Stich/Stitch Knoten/Knot Wirbel/Vortex Zwirn/Twine Wandel/Shift Paradox/Paradox
W: in den schillernden Lachen an der Naht | in the shimmering pools at the Seam
L: in der Glut an der Naht | in the embers at the Seam
V: im Nichts entlang der Naht | in the nothing along the Seam

minecraft:plains | rivers | 7 | W | #6aa84a #3a5a2a #e8c84a #f0f0c8 | fresh | meadow
Ebenen/Plains Gras/Grass Feld/Field Weiden/Pasture Halm/Stalk Brisen/Breeze Löwenzahn/Dandelion Heu/Hay
W: in den Bächen der Ebene | in the brooks of the plains

minecraft:sunflower_plains | rivers | 7 | W | #e8b82a #6a5a1a #f07a2a #fff4c0 | fresh | dry
Sonnenblumen/Sunflower Sommerfeld/Summerfield Gelb/Yellow Kern/Kernel Mittags/Noon Strahlen/Radiant Bienen/Bee Korn/Grain
W: zwischen den Sonnenblumenfeldern | among the sunflower fields

minecraft:forest | rivers | 7 | W | #3a7a3a #22401e #c87a3a #e0e8c0 | fresh | forest
Wald/Forest Busch/Bush Unterholz/Underbrush Lichtungs/Glade Eichel/Acorn Specht/Woodpecker Reisig/Twig Hasel/Hazel
W: in den Waldbächen | in the forest streams

minecraft:flower_forest | rivers | 7 | W | #d86aa8 #3a6a3a #f0d84a #fff0f4 | fresh | fae
Blumen/Flower Flieder/Lilac Pfingstrosen/Peony Mohn/Poppy Kornblumen/Cornflower Margeriten/Daisy Duft/Scent Bunt/Motley
W: in den Teichen des Blumenwalds | in the ponds of the flower forest

minecraft:birch_forest | rivers | 7 | W | #d8d8c8 #3a3a34 #7ab84a #f8f8f0 | fresh | forest
Birken/Birch Weißrinden/Whitebark Maigrün/Maygreen Lichtwald/Brightwood Kätzchen/Catkin Papier/Paper Silberblatt/Silverleaf Taglicht/Daylight
W: in den Bächen des Birkenwalds | in the streams of the birch forest

minecraft:old_growth_birch_forest | rivers | 6 | W | #c8c8b0 #2e3a2a #e8c84a #f4f4e0 | fresh | forest
Altbirken/Oldbirch Hochstamm/Tallstem Säulenwald/Pillarwood Kronen/Treetop Weißriesen/Whitegiant Stamm/Trunk Lichtdom/Lightdome
W: in den stillen Weihern des Birken-Urwalds | in the quiet ponds of the old growth birch forest

minecraft:dappled_forest | rivers | 7 | W | #c85a2a #4a2a1a #f0b83a #f4e4c0 | fresh | forest
Rotlaub/Redleaf Herbst/Autumn Scheck/Dappled Laubfall/Leaffall Kupferlaub/Copperleaf Sprenkel/Speckle Flecklicht/Sunfleck Ahorn/Maple
W: in den Bächen des Rotlaubwalds | in the streams of the dappled forest

minecraft:dark_forest | rivers | 7 | W | #2e4a2a #141e12 #b83a2a #a8b890 | fresh | cave
Düster/Gloom Dunkelwald/Darkwood Schattenlaub/Shadeleaf Finsterwald/Murkwood Rotpilz/Redcap Unheil/Bane Wolfs/Wolf Eulen/Owl Hexen/Witch
W: in den Tümpeln des Dunklen Walds | in the pools of the dark forest

minecraft:pale_garden | rivers | 7 | W | #b8b8b0 #4a4a48 #e8782a #ecece4 | fresh | shadow
Fahlgarten/Palegarden Knarz/Creaking Harzaugen/Resineye Bleichmoos/Palemoss Augenblüten/Eyeblossom Grauholz/Greywood Stillgarten/Stillgarden Welk/Wilted
W: in den reglosen Teichen des Blassen Gartens | in the motionless ponds of the pale garden

minecraft:taiga | rivers | 7 | W | #3a6a5a #1e3a34 #c8a86a #d8e8e0 | fresh_cold | forest
Taiga/Taiga Fichten/Spruce Nadelwald/Conifer Tannen/Fir Beeren/Berry Fuchs/Fox Moosbeeren/Cranberry Nordwald/Northwood
W: in den Bächen der Taiga | in the streams of the taiga

minecraft:old_growth_pine_taiga | rivers | 7 | W | #5a6a3a #2e2a1a #b86a3a #e0d8b8 | fresh_cold | earth
Kiefern/Pine Podsol/Podzol Urkiefer/Elderpine Findling/Boulder Riesenkiefer/Giantpine Kork/Cork Bärenwald/Bearwood Nadelstreu/Needlebed
W: zwischen den Findlingen der Kiefern-Urtaiga | among the boulders of the old growth pine taiga

minecraft:old_growth_spruce_taiga | rivers | 6 | W | #2e5a4a #16261e #8ab8c8 #d0e0d8 | fresh_cold | forest
Altfichten/Oldspruce Riesenfichten/Giantspruce Dunkeltann/Darkfir Moosstein/Mossrock Urtaiga/Eldertaiga Farnboden/Fernfloor Hochtann/Highfir
W: in den dunklen Weihern der Fichten-Urtaiga | in the dark ponds of the old growth spruce taiga

minecraft:snowy_taiga | rivers | 7 | W | #a8c8d8 #2e4a5a #f0f8ff #ffffff | fresh_cold | ice
Schneefichten/Snowspruce Winterwald/Winterwood Eisnadel/Icepine Frostwald/Frostwood Kaltmoos/Coldmoss Schneelast/Snowload Weißtann/Whitefir Schlitten/Sled
W: in den Eislöchern der Verschneiten Taiga | in the ice holes of the snowy taiga

minecraft:snowy_plains | rivers | 7 | W | #e0f0f8 #5a7a9a #8ad0f0 #ffffff | fresh_cold | ice
Schneefeld/Snowfield Tundra/Tundra Weiß/White Iglu/Igloo Harsch/Crust Pulverschnee/Powder Schneewehen/Snowdrift Eishasen/Snowhare
W: unter dem Eis der Verschneiten Ebene | beneath the ice of the snowy plains

minecraft:ice_spikes | rivers | 7 | W | #8ad8f0 #2a5a8a #ffffff #e8fbff | fresh_cold | crystal | glow=.15
Eisstachel/Icespike Packeis/Packice Eisturm/Icetower Blaufrost/Bluefrost Kristalleis/Crystalice Zinken/Tine Eisdorn/Icethorn Spiegeleis/Mirrorice
W: zwischen den Eisnadeln der Eiszapfentundra | among the ice needles of the ice spikes

minecraft:frozen_river | rivers | 7 | W | #7ab8e0 #2a4a7a #e8f4ff #f8fcff | fresh_cold | ice
Eisfluss/Iceriver Treibeis/Driftice Eisloch/Icehole Eisdecken/Icesheet Grundeis/Anchorice Winterfluss/Winterriver Eisrand/Icebrim Klareis/Clearice
W: unter dem Eis des Vereisten Flusses | beneath the ice of the frozen river

minecraft:river | rivers | 7 | W | #4a8ab8 #22466a #e8d06a #e0f0f8 | fresh | meadow
Fluss/River Ufer/Bank Kies/Shingle Furt/Ford Strudel/Eddy Mühlen/Mill Altarm/Oxbow Wehr/Weir
W: in der Strömung der Flüsse | in the current of the rivers

minecraft:swamp | rivers | 7 | W | #5a6a3a #2a3220 #a8c83a #d8dcb0 | swamp | swamp
Sumpf/Swamp Schlamm/Mud Teichrosen/Pondlily Morast/Mire Entengrütze/Duckweed Faulwasser/Stagnant Schleim/Slime Schilf/Reed Brack/Brackish
W: im trüben Wasser des Sumpfs | in the murky water of the swamp

minecraft:mangrove_swamp | rivers | 7 | W | #6a7a3a #3a2e1e #e8a04a #e0d8b0 | fresh_warm | swamp
Mangroven/Mangrove Stelzwurzel/Stiltroot Gezeitenwald/Tidewood Wurzeldickicht/Rootmaze Watt/Mudflat Propagel/Propagule Wurzelbogen/Rootarch Salzsumpf/Saltmarsh
W: zwischen den Stelzwurzeln des Mangrovensumpfs | among the stilt roots of the mangrove swamp

minecraft:jungle | rivers | 7 | W | #2a9a3a #164a22 #f0d02a #d8f0c0 | fresh_warm | jungle
Dschungel/Jungle Kakao/Cocoa Papageien/Parrot Regenwald/Rainforest Tiger/Tiger Orchideen/Orchid Tukan/Toucan Melonen/Melon
W: in den Flüssen des Dschungels | in the rivers of the jungle

minecraft:sparse_jungle | rivers | 6 | W | #5aa83a #2a5a22 #e8b83a #e0f0c8 | fresh_warm | jungle
Buschland/Bushland Dschungelrand/Jungleedge Wildfrucht/Wildfruit Ozelot/Ocelot Feucht/Humid Lichtlaub/Sparseleaf Waldsaum/Woodfringe
W: in den Wasserläufen des Lichten Dschungels | in the waterways of the sparse jungle

minecraft:bamboo_jungle | rivers | 7 | W | #7ab83a #2e5a1e #f0f0a0 #e8f4d0 | fresh_warm | jungle
Bambus/Bamboo Panda/Panda Halmwald/Cane Rohr/Culm Schößling/Shoot Grünrohr/Greencane Sprossen/Sprout Zen/Zen
W: zwischen den Halmen des Bambusdschungels | among the canes of the bamboo jungle

minecraft:savanna | rivers | 7 | W | #b8a84a #5a4a22 #e8782a #f0e8c0 | dry | dry
Savannen/Savanna Akazien/Acacia Steppen/Steppe Dürr/Drought Löwen/Lion Staubwind/Dustwind Trockengras/Drygrass Wasserloch/Waterhole
W: in den Wasserlöchern der Savanne | in the waterholes of the savanna

minecraft:savanna_plateau | rivers | 6 | W | #c8a85a #5a4226 #d8603a #f4e8c8 | dry | dry
Plateau/Plateau Hochsteppen/Highsteppe Geier/Vulture Trockenkamm/Dryridge Rotgras/Redgrass Fernblick/Farsight Weitblick/Vista
W: in den Wasserstellen der Savannenhochebene | in the watering places of the savanna plateau

minecraft:windswept_savanna | rivers | 6 | W | #a8983a #4a3e1e #f0a03a #ece4b8 | dry | storm
Sturmgras/Stormgrass Schiefwuchs/Windbent Überhang/Overhang Zaus/Tousled Wehwind/Howlwind Kliffgras/Cliffgrass Fallbach/Fallbrook
W: in den Sturzbächen der Zerzausten Savanne | in the cascades of the windswept savanna

minecraft:desert | rivers | 7 | W | #e0c88a #7a5a2a #e8703a #fff4d8 | dry | dry
Wüsten/Desert Oasen/Oasis Sand/Sand Dünen/Dune Kaktus/Cactus Skorpion/Scorpion Trug/Mirage Karawanen/Caravan Zisternen/Cistern
W: in den Oasen der Wüste | in the oases of the desert

minecraft:badlands | rivers | 7 | W | #c8703a #5a2a1a #f0c04a #f4d8b8 | dry | earth
Tafelberg/Mesa Ton/Terracotta Rotsand/Redsand Canyon/Canyon Ocker/Ochre Goldgräber/Prospector Rost/Rust Schicht/Strata
W: in den Wasserlöchern der Tafelberge | in the waterholes of the badlands

minecraft:eroded_badlands | rivers | 6 | W | #d8823a #6a3220 #ffd86a #f8e0c0 | dry | earth
Hoodoo/Hoodoo Erosions/Erosion Felsnadel/Rockspire Windschliff/Windcarved Rotzinnen/Redspire Säulenstein/Pillarstone Kerb/Groove
W: zwischen den Felsnadeln der Abgetragenen Tafelberge | among the rock spires of the eroded badlands

minecraft:wooded_badlands | rivers | 6 | W | #a86a3a #3a4a22 #6a9a3a #ecd8b0 | dry | forest
Tafelwald/Mesawood Roterde/Redearth Wurzelton/Rootclay Trockenwald/Drywood Kammwald/Ridgewood Eichenton/Oakclay Lehmwald/Claywood
W: in den Tümpeln der Bewaldeten Tafelberge | in the pools of the wooded badlands

minecraft:meadow | rivers | 7 | W | #7ac86a #3a6a4a #f08ad0 #f4fbe0 | mountain | meadow
Alm/Alp Enzian/Gentian Edelweiß/Edelweiss Glocken/Bell Kräuter/Herb Almrausch/Alpenrose Bienenweide/Beepasture Senn/Dairy
W: in den Bergbächen der Alm | in the mountain brooks of the meadow

minecraft:cherry_grove | rivers | 7 | W | #f8a8c8 #7a4a5a #ffffff #fff4f8 | mountain | fae
Kirsch/Cherry Sakura/Sakura Rosa/Pink Blütenschnee/Petalsnow Hanami/Hanami Lenz/Springtide Zartrosa/Blush Blütenregen/Petalrain
W: unter den Blüten des Kirschberghains | beneath the blossoms of the cherry grove

minecraft:grove | rivers | 7 | W | #b8d8c8 #2e4a4a #5a9ab8 #f4fcf8 | fresh_cold | mountain
Schneehain/Snowgrove Bergfichten/Alpspruce Hochwald/Highwood Stieb/Flurry Tannicht/Firstand Schneewald/Snowwood Waldgrenz/Treeline Kaltluft/Coldair
W: in den Quellen des Berghains | in the springs of the grove

minecraft:snowy_slopes | rivers | 7 | W | #d8ecf8 #4a6a8a #f0a04a #ffffff | fresh_cold | mountain
Hang/Slope Schneehang/Snowslope Firnfeld/Firnfield Rutsch/Slide Pisten/Piste Wechten/Cornice Ziegen/Goat Schneebrett/Slab
W: in den Schmelzwasserrinnen der Verschneiten Hänge | in the meltwater runnels of the snowy slopes

minecraft:frozen_peaks | rivers | 7 | W | #a8d0f0 #3a5a8a #ffffff #f0f8ff | fresh_cold | ice
Eisgipfel/Icepeak Hocheis/Highice Eiswand/Icewall Gipfeleis/Summitice Nordwand/Northface Eisgrat/Iceridge Frostkrone/Frostcrown Blankeis/Bareice
W: in den Gletscherseen der Vereisten Gipfel | in the glacier lakes of the frozen peaks

minecraft:jagged_peaks | rivers | 7 | W | #c8d0d8 #4a5462 #f0f8ff #ffffff | mountain | mountain
Zackengipfel/Jaggedpeak Felszahn/Rocktooth Schroffen/Steep Steinbock/Ibex Nadelgrat/Needleridge Scharfgrat/Sharpridge Hochgrat/Highridge Gipfelkreuz/Summitcross
W: in den Karseen der Zerklüfteten Gipfel | in the tarns of the jagged peaks

minecraft:stony_peaks | rivers | 7 | W | #a8a8a0 #4a4a44 #5ab8a8 #e8e8e0 | mountain | earth
Steingipfel/Stonepeak Kalkgipfel/Limepeak Kalzit/Calcite Geröllhang/Screeslope Karg/Barren Kahlfels/Barerock Sonnenfels/Sunrock Trockenfels/Dryrock
W: in den Felsbecken der Steinigen Gipfel | in the rock basins of the stony peaks

minecraft:windswept_hills | rivers | 7 | W | #7a9a7a #3a4a3e #c8c8c8 #e8f0e8 | mountain | mountain
Böenhügel/Gusthill Kuppen/Knoll Hügel/Hill Heide/Heath Zugwind/Draft Wetterseiten/Weatherside Grünstein/Greenstone Rauwind/Roughwind
W: in den Bächen der Zerzausten Hügel | in the brooks of the windswept hills

minecraft:windswept_gravelly_hills | rivers | 6 | W | #9a9a94 #44444a #c8b48a #e8e8e4 | mountain | earth
Kieshügel/Gravelhill Splitt/Grit Kieshalden/Gravelheap Steinschlag/Rockfall Grauhügel/Greyhill Flint/Flint Rollstein/Rollstone
W: in den Rinnsalen der Zerzausten Geröllhügel | in the trickles of the windswept gravelly hills

minecraft:windswept_forest | rivers | 6 | W | #4a7a5a #22382a #a8c8a8 #dcecdc | mountain | forest
Windwald/Windwood Krummholz/Krummholz Sturmholz/Stormwood Hangwald/Slopewood Rauschwald/Rustlewood Bergwald/Mountainwood Zerrholz/Gnarlwood
W: in den Bächen des Zerzausten Walds | in the brooks of the windswept forest

minecraft:mushroom_fields | rivers | 7 | W | #8a6a8a #4a2a3a #e84a4a #f4e8f0 | fresh | mushroom
Pilzland/Mushroomfield Mooshroom/Mooshroom Rotkappen/Scarletcap Braunkappen/Browncap Pilzinsel/Shroomisle Sporenwind/Sporewind Hutland/Capland Riesenpilz/Giantshroom
W: in den Tümpeln des Pilzlands | in the pools of the mushroom fields

minecraft:ocean | sea | 7 | W | #3a6aa8 #1a2e5a #e8e8f0 #dcecf8 | sea | sea
Meeres/Sea Hochsee/Highsea Wellen/Wave Seemanns/Sailor Küsten/Coast Blauwasser/Bluewater Seegras/Seagrass Dünung/Swell
W: im offenen Ozean | in the open ocean

minecraft:cold_ocean | sea | 7 | W | #3a5a8a #162a4a #a8d8e8 #d8e8f0 | sea_cold | ice
Kaltmeer/Coldsea Nordmeer/Northsea Fjord/Fjord Nebelbank/Fogbank Robben/Seal Grauwasser/Greywater Schären/Skerry Walfänger/Whaler
W: im Kalten Ozean | in the cold ocean

minecraft:frozen_ocean | sea | 7 | W | #7ab0d8 #2a4a6a #ffffff #e8f4fc | sea_cold | ice
Eismeer/Icesea Eisberg/Iceberg Polarmeer/Polarsea Eisbären/Polarbear Frostsee/Frostsea Eiskanten/Icebrink Walross/Walrus Robbenloch/Sealhole
W: zwischen den Eisbergen des Vereisten Ozeans | among the icebergs of the frozen ocean

minecraft:lukewarm_ocean | sea | 7 | W | #3a9ab8 #1a4a6a #f0d86a #e0f8f8 | sea | sea
Sandbank/Sandbar Seichtwasser/Shallows Blaugrün/Teal Sommersee/Summersea Delfin/Dolphin Tangwald/Kelpwood Küstenwind/Shorewind Warmstrom/Warmcurrent
W: im Lauwarmen Ozean | in the lukewarm ocean

minecraft:warm_ocean | sea | 7 | W | #2ab8c8 #1a5a7a #ff8a5a #e8fffc | sea_warm | sea
Warmmeer/Warmsea Südsee/Southsea Palmen/Palm Passat/Tradewind Schildkröten/Turtle Sonnenriff/Sunreef Blaulagunen/Bluelagoon Muschelbank/Shellbed
W: über den Korallen des Warmen Ozeans | above the corals of the warm ocean

minecraft:beach | sea | 7 | W | #e8d8a0 #8a7a4a #5ab8d8 #fff8e0 | beach | sea
Strand/Beach Flut/Hightide Ebbe/Lowtide Strandhafer/Marram Treibholz/Driftwood Priel/Tidecreek Sandburg/Sandcastle Möwen/Gull
W: in der Brandung am Strand | in the surf off the beach

minecraft:snowy_beach | sea | 6 | W | #e8f0f0 #6a8a9a #a8d8f0 #ffffff | beach | ice
Schneestrand/Snowbeach Eisstrand/Icebeach Frostküsten/Frostcoast Winterstrand/Winterbeach Eisschaum/Icefoam Kaltbrandung/Coldsurf Schneedünen/Snowdune
W: vor dem Verschneiten Strand | off the snowy beach

minecraft:stony_shore | sea | 7 | W | #8a8a8a #3a3a40 #5a8ab0 #e0e0e0 | beach | earth
Steinküsten/Stoneshore Klippenfuß/Cliffbase Brandungsfels/Surfrock Felsküsten/Rockshore Seepocken/Barnacle Spritzwasser/Splashzone Tangfels/Kelprock Gezeitentümpel/Tidepool
W: zwischen den Felsen der Steinigen Küste | among the rocks of the stony shore

minecraft:deep_ocean | deep | 7 | W | #1e3a6a #0a1430 #7af0ff #a8c8e8 | deep | deep | glow=.5 | eyes
Tiefsee/Deepsea Bathy/Bathyal Tiefenstrom/Deepcurrent Dunkelsee/Darksea Graben/Trench Lot/Sounding Druck/Pressure Wächter/Guardian
W: in der Tiefsee | in the deep ocean

minecraft:deep_cold_ocean | deep | 7 | W | #1e2e5a #080e26 #a8e0ff #b0c0e0 | deep | deep | glow=.5 | eyes
Eistiefen/Icedepth Nordtiefen/Northdeep Frostgraben/Frosttrench Kaltgrund/Coldbottom Dunkelfjord/Darkfjord Tiefkalt/Deepcold Walgrund/Whalefall Nachtfjord/Nightfjord
W: in der Kalten Tiefsee | in the deep cold ocean

minecraft:deep_frozen_ocean | deep | 6 | W | #3a5a8a #0e1a36 #ffffff #c8dcf0 | deep | ice | glow=.4 | eyes
Untereis/Underice Polartiefen/Polardeep Schelfeis/Iceshelf Eisgrund/Icebottom Ewigeis/Everice Dunkelschollen/Darkfloe Tiefeis/Deepice
W: unter dem Eis der Vereisten Tiefsee | beneath the ice of the deep frozen ocean

minecraft:deep_lukewarm_ocean | deep | 7 | W | #1e5a7a #0a2236 #ffc86a #b0e0e8 | deep | sea | glow=.4 | eyes
Blautiefen/Bluedeep Warmtiefen/Warmdeep Tiefriff/Deepreef Dämmerzonen/Twilightzone Tiefenlicht/Deeplight Steilabfall/Dropoff Blauloch/Bluehole Tiefstrom/Deepstream
W: in der Lauwarmen Tiefsee | in the deep lukewarm ocean

minecraft:dripstone_caves | caves | 7 | W | #a87a5a #4a3222 #e8c8a0 #f0e0cc | cave | cave
Tropfstein/Dripstone Stalaktiten/Stalactite Stalagmiten/Stalagmite Sinter/Sinter Tropfwasser/Dripwater Höhlenlehm/Caveclay Kupferader/Coppervein Spitzstein/Pointstone
W: in den Sinterbecken der Tropfsteinhöhlen | in the sinter pools of the dripstone caves

minecraft:lush_caves | caves | 7 | W | #5aa84a #22401e #f08ab8 #e8f8d0 | cave | forest | glow=.3
Wucher/Lush Azaleen/Azalea Tropfblatt/Dripleaf Leuchtbeeren/Glowberry Sporenblüten/Sporeblossom Lehmteich/Claypond Höhlenranken/Cavevine Axolotl/Axolotl Moosteppich/Mosscarpet
W: in den Lehmteichen der Üppigen Höhlen | in the clay ponds of the lush caves

minecraft:deep_dark | caves | 7 | W | #0e2a36 #04121a #1ad0d8 #3a6a78 | cave | shadow | glow=.7 | eyes
Sculk/Sculk Kreischer/Shrieker Sensor/Sensor Katalysator/Catalyst Lausch/Listening Wärter/Warden Echo/Echo Tiefdunkel/Deepdark Schallfänger/Soundcatcher
W: in den schwarzen Wassern des Tiefen Dunkels | in the black waters of the deep dark

minecraft:sulfur_caves | caves | 7 | W | #d8c83a #4a4212 #8af05a #f8f4b0 | cave | sulfur | glow=.2
Schwefel/Sulfur Fumarolen/Fumarole Geysir/Geyser Dampf/Steam Stinkquell/Stinkspring Säure/Acid Brodel/Bubbling Gelbdunst/Yellowhaze
W: in den dampfenden Tümpeln der Schwefelhöhlen | in the steaming pools of the sulfur caves

minecraft:nether_wastes | nether | 7 | L | #8a3a3a #3a1414 #ffb02a #e8a890 | lava | fire | glow=.4 | eyes | fixed
Nether/Nether Ödland/Wasteland Netherquarz/Netherquartz Ghast/Ghast Leuchtstein/Glowstone Feuermeer/Firesea Lavasee/Lavalake Rotnebel/Redfog
L: in den Lavaseen des Nether-Ödlands | in the lava seas of the nether wastes

minecraft:crimson_forest | nether | 7 | L | #a81e2a #3a060c #ffd24a #f09a8a | lava | crimson | glow=.35 | eyes | fixed
Karmesin/Crimson Blutrot/Bloodred Trauerranken/Weepingvine Hoglin/Hoglin Netherwarzen/Netherwart Pilzlicht/Shroomlight Rotstiel/Redstem Nylium/Nylium
L: in der Lava des Karmesinwalds | in the lava of the crimson forest

minecraft:warped_forest | nether | 7 | L | #16a08a #0a3a3a #e84a8a #7af0d0 | lava | warped | glow=.4 | eyes | fixed
Wirr/Warped Zwirbel/Twisting Türkisstiel/Tealstem Wirrpilz/Warpshroom Wirrwurzel/Warproot Blaunebel/Bluefog Wirrwarzen/Warpwart Zerrlicht/Warplight
L: in der Lava des Wirrwalds | in the lava of the warped forest

minecraft:soul_sand_valley | nether | 7 | L | #5a4a3e #1e1814 #5ae8f0 #c8f8ff | lava | nethersoul | glow=.55 | eyes | fixed
Seelensand/Soulsand Seelenfeuer/Soulfire Gebeintal/Bonevale Fossil/Fossil Klagetal/Wailvale Blauflammen/Blueflame Seelenerd/Soulsoil Geistertal/Ghostvale
L: in der Lava des Seelensandtals | in the lava of the soul sand valley

minecraft:basalt_deltas | nether | 7 | L | #4a4a54 #16161c #ff7a1a #a8a8b0 | lava | basalt | glow=.35 | eyes | fixed
Basalt/Basalt Schwarzstein/Blackstone Delta/Delta Magmawürfel/Magmacube Ascheregen/Ashfall Säulenbasalt/Columnbasalt Glutdelta/Emberdelta Graustein/Greystone Rauchsäulen/Smokepillar
L: in der Lava der Basaltdeltas | in the lava of the basalt deltas

minecraft:the_end | end | 7 | VW | #d8d8a0 #2a1a3a #c05af0 #f4f4c8 | void | end | glow=.4 | eyes | fixed
End/End Drachen/Dragon Obsidiansäulen/Obsidianpillar Endstein/Endstone Kristallturm/Crystaltower Hauptinsel/Mainisle Portal/Portal Ender/Ender
V: in der Leere um die Hauptinsel des Endes | in the void around the main island of the End

minecraft:end_highlands | end | 7 | VW | #c8c890 #3a2a4a #e88af8 #f0f0c0 | void | end | glow=.35 | eyes | fixed
Chorus/Chorus Endhöhen/Endheight Endstadt/Endcity Purpurblock/Purpur Shulker/Shulker Endschiff/Endship Chorusblüten/Chorusflower Hochinsel/Highisle
V: in der Leere unter dem End-Hochland | in the void beneath the End highlands

minecraft:end_midlands | end | 6 | VW | #b8b888 #2e2440 #a878e0 #e8e8b8 | void | end | glow=.3 | eyes | fixed
Zwischenland/Midland Endhang/Endslope Randland/Rimland Gelbstein/Yellowstone Endkanten/Endledge Leerblick/Voidview Endflanken/Endflank
V: in der Leere unter dem End-Mittelland | in the void beneath the End midlands

minecraft:small_end_islands | end | 6 | VW | #a8a880 #221a30 #70f0d0 #e0e0b0 | void | void | glow=.4 | eyes | fixed
Kleininsel/Smallisle Splitterinsel/Shardisle Endbrocken/Endchunk Inselchen/Isleling Leerstein/Voidstone Streu/Scattered Endkrümel/Endcrumb
V: in der Leere zwischen den Kleinen Endinseln | in the void between the small End islands

minecraft:end_barrens | end | 6 | VW | #989870 #1e1828 #f0f0f0 #d8d8a8 | void | void | glow=.3 | eyes | fixed
Endöd/Endbarren Kahlstein/Barestone Randöd/Rimbarren Endrand/Endrim Abbruch/Dropedge Ödstein/Barrenstone Letztland/Lastland
V: in der Leere am Rand des End-Karglands | in the void at the rim of the End barrens
"""


class Biome:
    def __init__(self, block: str):
        lines = [ln.strip() for ln in block.strip().split("\n") if ln.strip()]
        head = [p.strip() for p in lines[0].split("|")]
        self.id, self.chapter, self.n, self.media = head[0], head[1], int(head[2]), head[3]
        self.cols = [rgb(c) for c in head[4].split()]
        assert len(self.cols) == 4, head
        self.pool, self.theme = head[5], head[6]
        assert self.pool in POOLS and self.theme in THEMES, head
        self.glow, self.eyes, self.fixed = 0.0, False, False
        for opt in head[7:]:
            if opt.startswith("glow="):
                self.glow = float(opt[5:])
            elif opt == "eyes":
                self.eyes = True
            elif opt == "fixed":
                self.fixed = True
            else:
                raise ValueError(opt)
        self.prefixes = []
        for pair in lines[1].split():
            de, en = pair.split("/")
            self.prefixes.append((de, en.replace("_", " ")))
        self.where = {}
        for ln in lines[2:]:
            medium, rest = ln.split(":", 1)
            de, en = [p.strip() for p in rest.split("|")]
            self.where[medium.strip()] = (de, en)
        for m in self.media:
            assert m in self.where or (self.media == "VW" and "V" in self.where), (self.id, m)
        assert self.n in SPLIT, self.id
        assert len(self.prefixes) >= min(self.n, 7), (self.id, len(self.prefixes))
        self.own = self.id.startswith("kingdomsatwar:")


def biomes() -> list:
    return [Biome(block) for block in BIOME_TABLE.strip().split("\n\n")]


# =============================================================================================
# Was es schon gibt (die 84 von Hand benannten Arten)
# =============================================================================================

def existing_names() -> tuple:
    """Namen (deutsch, englisch) und Kennungen der Arten aus FishingLang.java – dazu die vier Fische von Minecraft."""
    src = open(os.path.join(JAVA, "datagen/FishingLang.java"), encoding="utf-8").read()
    de, en, ids = {"kabeljau", "lachs", "tropenfisch", "kugelfisch"}, {"cod", "salmon", "tropical fish", "pufferfish"}, {"cod", "salmon", "tropical_fish", "pufferfish"}
    for m in re.finditer(r'text\.add\("item\.kingdomsatwar\.fish_(\w+)",\s*"([^"]+)",\s*"([^"]+)"\)', src):
        ids.add(m.group(1))
        en.add(m.group(2).lower())
        de.add(m.group(3).lower())
    return de, en, ids


def existing_signatures() -> set:
    """Die Wirkungen der von Hand benannten Arten (als Schlüssel wie bei den erzeugten)."""
    src = open(os.path.join(JAVA, "fishing/FishSpecies.java"), encoding="utf-8").read()
    out = set()
    for m in re.finditer(r'=\s*add\(b\("(\w+)"(.*?)\)\);|^\s*[A-Z_]+\(b\("(\w+)"(.*?)\)\)[,;]', src, re.S | re.M):
        body = (m.group(2) or m.group(4) or "") + ")"       # die Klammer der letzten Wirkung gehört zum Ende der Konstante
        eff = [(e.lower(), int(a)) for e, a in re.findall(r'\.eat\(MobEffects\.(\w+),\s*\d+,\s*(\d+)\)', body)]
        special = re.search(r'\.special\(Special\.(\w+)\)', body)
        key = signature(eff, special.group(1).lower() if special else "none")
        if eff or special:
            out.add(key)
    return out


def signature(effects, special: str) -> frozenset:
    """Was eine Wirkung ausmacht: welche Effekte in welcher Stufe (die Dauer zählt nicht) und das Besondere."""
    return frozenset([(e, a) for (e, a) in effects] + [("special:" + special, 0)])


# =============================================================================================
# Eine Art
# =============================================================================================

class Species:
    def __init__(self):
        self.id = ""
        self.de = ""
        self.en = ""
        self.biome = None
        self.media = ""
        self.form = ""
        self.rarity = "COMMON"
        self.min = 0
        self.max = 0
        self.difficulty = 0.0
        self.behavior = "STEADY"
        self.time = "ANY"
        self.weather = "ANY"
        self.special = "none"
        self.effects = []          # (Wirkung, Sekunden, Stufe)
        self.note_de = ""
        self.note_en = ""
        self.image = None
        self.glows = False


def pick_forms(biome: Biome, rng: Rng) -> list:
    """Je Art (Medium, Gestalt): möglichst lauter verschiedene Gestalten."""
    if biome.media == "VW":
        groups = [("VW", biome.n, biome.pool)]
    elif len(biome.media) == 1:
        groups = [(biome.media, biome.n, biome.pool)]
    elif biome.media == "WL":
        groups = [("W", 7, biome.pool if biome.pool != "lava" else "ash"), ("L", 5, "lava")]
    elif biome.media == "WV":
        groups = [("W", 7, biome.pool), ("V", 5, "void")]
    elif biome.media == "WLV":
        groups = [("W", 4, "shadow"), ("L", 4, "lava"), ("V", 4, "void")]
    else:
        raise ValueError(biome.media)
    out, used = [], set()
    for medium, count, pool in groups:
        names = POOLS[pool].split()
        if biome.media == "WLV":
            # der Abgrund: seine eigenen Gestalten zuerst
            names = POOLS["abyss"].split() + names
        order = rng.shuffled(names)
        chosen = []
        for form in order:
            if form not in used and form not in chosen:
                chosen.append(form)
            if len(chosen) == count:
                break
        for form in order:                      # reicht die Auswahl nicht, darf sich eine Gestalt wiederholen
            if len(chosen) == count:
                break
            if form not in chosen:
                chosen.append(form)
        assert len(chosen) == count, (biome.id, medium)
        used.update(chosen)
        out += [(medium, form) for form in chosen]
    return out


def name_for(biome: Biome, form: str, prefixes: list, k: int, rng: Rng, used_de: set, used_en: set, used_ids: set):
    nouns = []
    for pair in FORMS[form][4].split():
        d, e = pair.split("/")
        nouns.append((d, e.replace("_", " ")))
    # die vorderen (geläufigen) Namen einer Gestalt werden bevorzugt
    order = sorted(range(len(nouns)), key=lambda i: i * 0.55 + rng.f() * 2.2)
    for attempt in range(len(prefixes) * 2):
        pde, pen = prefixes[(k + attempt) % len(prefixes)]
        for i in order:
            nde, nen = nouns[i]
            de = pde + nde.lower()
            en = pen + " " + nen
            ident = re.sub(r"[^a-z0-9]+", "_", en.lower()).strip("_")
            if len(de) > MAX_NAME_DE or de.lower() in used_de or en.lower() in used_en or ident in used_ids:
                continue
            if shares(pde, nde) or shares(pen, nen) or clashes(pde, nde):
                continue
            used_de.add(de.lower())
            used_en.add(en.lower())
            used_ids.add(ident)
            return ident, de, en
    raise RuntimeError("kein Name frei: %s %s" % (biome.id, form))


def clashes(prefix: str, noun: str) -> bool:
    """Stoßen an der Fuge gleiche Selbstlaute („Pandaaal“) oder drei gleiche Mitlaute („Blatttrilobit“) zusammen?"""
    a, b = prefix.lower(), noun.lower()
    if a[-1] == b[0] and a[-1] in "aeiouäöü":
        return True
    return len(a) >= 2 and a[-1] == a[-2] == b[0]


def shares(a: str, b: str) -> bool:
    """Teilen Vorsilbe und Name ein Wortstück (dann klänge der Name doppelt: „Nadelnadelfisch“)?"""
    a, b = a.lower().replace(" ", ""), b.lower().replace(" ", "")
    n = 4
    return any(a[i:i + n] in b for i in range(0, max(1, len(a) - n + 1))) if len(a) >= n else a in b


def assign_effects(sp: Species, biome: Biome, rng: Rng, used: set) -> None:
    good, bad = THEMES[biome.theme]
    good, bad = good.split(), bad.split()
    others = [e for e in GOOD if e not in good]
    r = RARITIES.index(sp.rarity)
    special_options = ["none"]
    if biome.theme in TELEPORT and r >= 1:
        special_options.append(TELEPORT[biome.theme])
    if biome.theme in CLEANSE and r >= 2:
        special_options.append("cleanse")

    def amp_for(effect: str, want: int) -> int:
        return min(want, GOOD[effect][1])

    def dur(effect: str, mult: float, amp: int) -> int:
        base = GOOD[effect][0] * mult / (1.0 + 0.6 * amp)
        return max(5, int(round(base / 5.0)) * 5)

    def harm(pool):
        b = rng.pick(pool)
        return (b, rng.pick(range(BAD[b][0], BAD[b][1] + 1)), 0)

    for attempt in range(600):
        wide = attempt >= 40            # erst im Thema bleiben, dann aus allen Wirkungen schöpfen
        p = rng.shuffled(good) + (rng.shuffled(others) if wide else [])
        first = p[0]
        rest = [e for e in (rng.shuffled(good) + rng.shuffled(others)) if e != first]
        bad_pool = bad if attempt < 60 else list(BAD)
        special = "none"
        effects = []
        if r == 0:
            mode = 0 if attempt < 10 else 1 + rng.below(3)
            if mode == 0:
                effects = [(first, dur(first, rng.pick([1.0, 1.5, 2.0]), 0), 0)]
            elif mode == 1:
                effects = [(first, dur(first, rng.pick([1.5, 2.0]), 0), 0), harm(bad_pool)]
            else:
                second = rest[0]
                effects = [(first, dur(first, rng.pick([1.0, 1.5]), 0), 0), (second, dur(second, rng.pick([0.5, 1.0]), 0), 0)]
                if attempt >= 120:
                    effects.append(harm(bad_pool))
        elif r == 1:
            mode = rng.below(5)
            second = rest[0]
            if mode == 0:
                a = amp_for(first, 1)
                effects = [(first, dur(first, 2.0, a), a)]
                if a == 0:
                    effects.append((second, dur(second, 1.0, 0), 0))
            elif mode == 1:
                a = amp_for(first, 1)
                effects = [(first, dur(first, 3.0, a), a), harm(bad_pool)]
            elif mode == 4:
                a = amp_for(first, 1)
                effects = [(first, dur(first, 2.0, a), a), (second, dur(second, 1.0, 0), 0)]
            else:
                effects = [(first, dur(first, rng.pick([2.0, 3.0]), 0), 0), (second, dur(second, rng.pick([1.0, 1.5]), 0), 0)]
            if attempt >= 150:
                effects.append(harm(bad_pool))
            if len(special_options) > 1 and rng.chance(0.22):
                special = rng.pick(special_options[1:])
        elif r == 2:
            second, third = rest[0], rest[1]
            a1 = amp_for(first, 1)
            effects = [(first, dur(first, rng.pick([3.0, 4.0, 5.0]), a1), a1), (second, dur(second, rng.pick([2.0, 3.0]), 0), 0)]
            mode = rng.below(3)
            if mode == 0:
                effects.append((third, dur(third, rng.pick([1.0, 2.0]), 0), 0))
            elif mode == 1:
                effects.append(harm(bad_pool))
            if len(special_options) > 1 and rng.chance(0.25):
                special = rng.pick(special_options[1:])
        else:
            second, third = rest[0], rest[1]
            a1 = amp_for(first, 2 if rng.chance(0.5) else 1)
            a2 = amp_for(second, 1 if rng.chance(0.6) else 0)
            effects = [(first, dur(first, rng.pick([5.0, 6.0, 8.0]), a1), a1), (second, dur(second, rng.pick([3.0, 4.0]), a2), a2),
                       (third, dur(third, rng.pick([2.0, 3.0]), 0), 0)]
            if rng.chance(0.3):
                effects.append(harm(bad_pool))
            if len(special_options) > 1 and rng.chance(0.3):
                special = rng.pick(special_options[1:])
        # Lavafische schützen immer ein wenig vor Feuer – es sei denn, sie tun es ohnehin
        if "L" in sp.media and all(e[0] != "fire_resistance" for e in effects) and rng.chance(0.5):
            effects.append(("fire_resistance", 20 + 10 * r, 0))
        key = signature([(e, a) for (e, _, a) in effects], special)
        if key in used:
            continue
        used.add(key)
        sp.effects = effects
        sp.special = special
        return
    raise RuntimeError("keine Wirkung frei: " + sp.id)


# =============================================================================================
# Bilder
# =============================================================================================

PATTERNS = ("spots", "bars", "line", "patches", "scales", "mottle", "tailfade", "headfade", "belly", "saddle", "stripes", "collar", "mask",
            "backline", "halves")
FISH_FORMS = set(ft.SHAPES)


def colors_for(biome: Biome, rng: Rng) -> dict:
    main, dark, accent, light = biome.cols
    main = shift(main, rng.uni(-14, 14), rng.uni(0.9, 1.12), rng.uni(0.9, 1.1))
    accent = shift(accent, rng.uni(-12, 12), rng.uni(0.9, 1.1), rng.uni(0.94, 1.06))
    dark = shift(dark, rng.uni(-10, 10), 1.0, rng.uni(0.9, 1.15))
    scheme = rng.pick("AAABBCCDEEF")
    if scheme == "A":
        back, side, belly, fin = dim(main, 0.62), main, light, blend(accent, main, 0.2)
    elif scheme == "B":
        back, side, belly, fin = dim(accent, 0.6), accent, light, main
    elif scheme == "C":
        back, side, belly, fin = dark, blend(dark, main, 0.6), blend(main, light, 0.55), accent
    elif scheme == "D":
        back, side, belly, fin = blend(main, light, 0.35), blend(main, light, 0.75), blend(light, WHITE, 0.5), blend(accent, light, 0.2)
    elif scheme == "E":
        back, side, belly, fin = dim(main, 0.7), blend(main, accent, 0.5), blend(accent, light, 0.65), dark
    else:
        back, side, belly, fin = dark, accent, light, blend(dark, main, 0.45)
    # der Rücken muss sich von der Flanke abheben, der Bauch auch
    if lum(side) - lum(back) < 0.1:
        back = dim(back, 0.72) if lum(back) > 0.12 else back
        if lum(side) - lum(back) < 0.08:
            side = blend(side, WHITE, 0.18)
    if lum(belly) - lum(side) < 0.08:
        belly = blend(belly, WHITE, 0.35)
    return dict(back=back, side=side, belly=belly, fin=fin, main=main, dark=dark, accent=accent, light=light, scheme=scheme)


def pattern_for(form: str, c: dict, rng: Rng):
    if rng.chance(0.14):
        return None
    kind = rng.pick(PATTERNS)
    if form in ("eel", "moray", "ribbon") and kind in ("collar", "halves", "mask"):
        kind = rng.pick(("bars", "spots", "saddle", "line", "mottle"))
    if form in ("flat", "sunfish", "round", "box") and kind in ("line", "stripes", "backline"):
        kind = rng.pick(("spots", "mottle", "patches", "bars"))
    if form in ("flat", "sunfish", "round", "box", "angler", "armored", "hatchet", "bream", "piranha", "coel") and kind == "scales":
        kind = rng.pick(("spots", "mottle", "saddle", "belly", "tailfade"))
    side = c["side"]
    options = [c["dark"], c["accent"], c["light"], dim(side, 0.55), blend(side, WHITE, 0.6), c["fin"]]
    good = [o for o in options if abs(lum(o) - lum(side)) >= 0.18 or dist(o, side) >= 110]
    col = rng.pick(good) if good else (blend(side, WHITE, 0.6) if lum(side) < 0.5 else dim(side, 0.5))
    if kind == "spots":
        return ("spots", hx(col), round(rng.uni(0.14, 0.3), 2))
    if kind == "bars":
        return ("bars", hx(col), rng.pick((3, 4, 5, 6, 7)))
    if kind == "saddle":
        return ("saddle", hx(col), rng.pick((3, 4, 5)))
    if kind == "patches":
        return ("patches", hx(col), round(rng.uni(0.25, 0.45), 2))
    if kind == "mottle":
        return ("mottle", hx(col), round(rng.uni(0.2, 0.38), 2))
    if kind == "collar":
        return ("collar", hx(col), round(rng.uni(0.4, 0.62), 2))
    return (kind, hx(col))


def dist(a, b) -> float:
    return abs(a[0] - b[0]) + abs(a[1] - b[1]) + abs(a[2] - b[2])


def bright(c) -> tuple:
    """Eine leuchtende Fassung der Farbe (für Leuchtpunkte und helle Augen)."""
    h, s, v = colorsys.rgb_to_hsv(c[0] / 255.0, c[1] / 255.0, c[2] / 255.0)
    r, g, b = colorsys.hsv_to_rgb(h, min(s, 0.55), 1.0)
    return r * 255.0, g * 255.0, b * 255.0


def body_touches_border(img: Image.Image) -> bool:
    """Ragt die Gestalt über den Rand (dann fehlt ihr dort die Umrisslinie)?"""
    px = img.load()
    line_pixels = 0
    for i in range(16):
        for (x, y) in ((i, 0), (i, 15), (0, i), (15, i)):
            if px[x, y][3]:
                line_pixels += 1
    return line_pixels > 6


def paint(sp: Species, biome: Biome, rng: Rng) -> Image.Image:
    form = sp.form
    c = colors_for(biome, rng)
    glow = rng.chance(biome.glow)
    glowcol = hx(bright(c["accent"]))
    eye = "#101018"
    if biome.eyes and rng.chance(0.6):
        eye = hx(bright(c["accent"]))
    elif lum(c["side"]) < 0.2:
        eye = hx(blend(c["light"], WHITE, 0.3))
    sp.glows = glow
    back, side, belly, fin = hx(c["back"]), hx(c["side"]), hx(c["belly"]), hx(c["fin"])
    line = hx(blend(c["back"], BLACK, 0.7))
    if form in FISH_FORMS:
        base = ft.SHAPES[form]
        e = {}
        if form == "catfish":
            e["barbels"] = rng.pick((2, 3, 3))
        if form == "carp" and rng.chance(0.5):
            e["barbels"] = 1
        if form == "angler":
            e["lure"] = glowcol
            e["teeth"] = 3
        if form == "viper":
            e["teeth"] = 3
        if form == "piranha":
            e["teeth"] = 2
        if form in ("shark", "moray", "pike") and rng.chance(0.35):
            e["teeth"] = 2
        if form == "sword":
            e["bill"] = round(rng.uni(4.4, 5.1), 1)
        if form == "sail":
            e["bill"] = round(rng.uni(3.8, 4.5), 1)
        if form == "gar":
            e["bill"] = round(rng.uni(2.6, 3.3), 1)
        if form == "armored":
            e["plates"] = hx(blend(c["dark"], BLACK, 0.3))
            if rng.chance(0.6):
                e["teeth"] = 3
        if form == "sturgeon":
            e["scutes"] = hx(blend(c["light"], WHITE, 0.3))
        if form in ("round", "box") and rng.chance(0.45):
            e["spines"] = hx(c["dark"])
        if form == "perch" and rng.chance(0.25):
            e["spines"] = fin
        if form in ("tuna", "mahi") and rng.chance(0.6):
            e["finlets"] = hx(c["accent"])
        if form == "ribbon":
            e["crest"] = hx(c["accent"])
        if form in ("small", "hatchet", "flyer", "skipper") and rng.chance(0.45):
            e["big_eye"] = True
        if rng.chance(0.24):
            e["fintip"] = hx(blend(c["light"], WHITE, 0.3) if lum(c["fin"]) < 0.55 else dim(c["fin"], 0.5))
        if rng.chance(0.18):
            e["tailcol"] = hx(c["accent"] if dist(c["accent"], c["fin"]) > 60 else c["light"])
        if glow:
            e["glow"] = glowcol
        if form == "angel":
            # die hohen Flossen brauchen eine eigene Farbe, sonst verschmilzt die Gestalt
            fin = hx(c["accent"] if dist(c["accent"], c["side"]) > 90 else c["dark"])
        pattern = pattern_for(form, c, rng)
        # die Gestalt leicht abwandeln
        widest = max(w for _, w in base["body"])
        ws = rng.uni(0.92, 1.1) if widest < 2.9 else rng.uni(0.94, 1.02)
        jitter = dict(
            body=[(u, round(w * ws, 2)) for (u, w) in base["body"]],
            len=round(base["len"] * (rng.uni(0.95, 1.01) if "bill" not in e else 1.0), 2),
            dorsal=(base["dorsal"][0], base["dorsal"][1], round(base["dorsal"][2] * rng.uni(0.85, 1.15), 2)),
        )
        t = base["tail"]
        if t[2] > 0:
            jitter["tail"] = (round(t[0] * rng.uni(0.92, 1.06), 2), round(t[1] * rng.uni(0.9, 1.1), 2), round(min(0.9, max(0.0, t[2] + rng.uni(-0.1, 0.1))), 2)) + tuple(t[3:])
        img = ft.fish(form, back, side, belly, fin, pattern, eye, **e, **jitter)
        if body_touches_border(img):
            img = ft.fish(form, back, side, belly, fin, pattern, eye, **e)
        return img
    # --- Wesen ohne Fischgestalt ---
    eyec = eye
    if form == "ray":
        spots = [(7, 9), (10, 8), (6, 5), (11, 10), (8, 6), (9, 10), (5, 7)]
        dots = tuple(rng.shuffled(spots)[:rng.pick((0, 3, 4, 5))])
        return ft.ray(side, belly if rng.chance(0.6) else hx(c["accent"]), back, dots=dots, dot=glowcol if glow else hx(blend(c["light"], WHITE, 0.2)),
                      eye=eyec, line=line)
    if form == "squid":
        return ft.squid(side, back, belly, eye=eyec, glow=glowcol if glow else None, fins=rng.chance(0.75),
                        cloak=hx(c["accent"]) if rng.chance(0.25) else None)
    if form == "octopus":
        img = ft.octopus(side, back, belly, eye=hx(blend(c["light"], WHITE, 0.4)))
        return img
    if form == "seahorse":
        return ft.seahorse(side, hx(blend(c["side"], c["belly"], 0.3)), back, belly, hx(c["accent"]), line, eyec)
    if form == "isopod":
        return ft.isopod(side, belly, hx(blend(c["side"], c["belly"], 0.45)), back, back, line, eyec if eyec != "#101018" else "#101018")
    if form == "crab":
        claw = hx(c["accent"]) if rng.chance(0.4) else None
        return ft.crab(side, back, belly, claw=claw, eye=eyec, line=line)
    if form in ("shrimp", "lobster"):
        return ft.shrimp(side, back, belly, claws=form == "lobster", eye=eyec, line=line, feeler=hx(c["accent"]) if rng.chance(0.4) else None)
    if form == "jelly":
        return ft.jelly(side, back, hx(blend(c["belly"], WHITE, 0.3)), glow=glowcol if (glow or rng.chance(0.4)) else None, line=line,
                        arms=rng.pick((4, 5, 5)))
    if form == "snail":
        return ft.snail(side, back, hx(blend(c["belly"], c["main"], 0.25)), hx(blend(c["belly"], WHITE, 0.3)), eye=eyec, line=line)
    if form == "starfish":
        return ft.starfish(side, back, belly, line=line)
    if form == "olm":
        return ft.olm(side, back, belly, hx(c["accent"]), eye=eyec, line=line)
    if form == "nautilus":
        return ft.nautilus(belly, side, hx(blend(c["side"], c["belly"], 0.55)), hx(blend(c["side"], c["belly"], 0.4)),
                           hx(blend(c["side"], c["belly"], 0.7)), line, eyec)
    if form == "trilobite":
        return ft.trilobite(side, belly, hx(blend(c["side"], c["back"], 0.35)), hx(blend(c["belly"], c["side"], 0.3)), back, line, eyec)
    raise ValueError(form)


# =============================================================================================
# Notizen im Fischbuch
# =============================================================================================

PLACE_NOTES = [
    ("Lebt {wo}.", "Lives {where}."),
    ("Haust {wo}.", "Dwells {where}."),
    ("Zu Hause {wo}.", "At home {where}."),
    ("Nur {wo} zu finden.", "Found only {where}."),
    ("Zieht {wo} Kreise.", "Circles {where}."),
    ("Wird {wo} gefangen – und nirgends sonst.", "Caught {where} – and nowhere else."),
]
BEHAVIOR_NOTES = {
    "CALM": [("Gemächlich im Drill – ein Fang für ruhige Hände.", "Unhurried on the line – a catch for calm hands."),
             ("Lässt sich fast ohne Gegenwehr einholen.", "Comes in almost without a fight.")],
    "STEADY": [("Zieht gleichmäßig, ohne Finten.", "Pulls evenly, without feints."),
               ("Ein ehrlicher Kämpfer: kein Trick, nur Ausdauer.", "An honest fighter: no tricks, only stamina.")],
    "FLOATER": [("Drängt im Drill immer wieder nach oben.", "Keeps pushing upward on the line."),
                ("Steigt an der Schnur wie eine Luftblase.", "Rises on the line like a bubble.")],
    "SINKER": [("Zieht stur in die Tiefe.", "Pulls stubbornly for the deep."),
               ("Legt sich mit ganzem Gewicht in die Schnur.", "Throws its whole weight into the line.")],
    "JITTER": [("Zappelt, dass die Rute zittert.", "Wriggles until the rod trembles."),
               ("Keinen Augenblick ruhig – die Schnur zuckt hin und her.", "Never still for a moment – the line twitches to and fro.")],
    "DARTER": [("Schießt in plötzlichen Fluchten davon.", "Shoots off in sudden runs."),
               ("Steht still – und rast dann los.", "Holds still – then bolts.")],
    "MIXED": [("Wechselt im Drill ständig die Laune.", "Changes its mood all through the fight."),
              ("Unberechenbar: mal träge, mal rasend.", "Unpredictable: sluggish one moment, frantic the next.")],
}
TIME_NOTES = {
    "NIGHT": [("Beißt nur nachts.", "Bites only at night."), ("Erst wenn es dunkel wird, kommt Leben an den Haken.", "Only after dark does the hook come alive.")],
    "DAY": [("Beißt nur bei Tageslicht.", "Bites only in daylight."), ("Sobald die Sonne sinkt, ist Ruhe.", "Once the sun sinks, all goes quiet.")],
}
WEATHER_NOTES = {
    "RAIN": [("Bei Regen besonders beißfreudig.", "Especially keen to bite in the rain.")],
    "RAIN_ONLY": [("Zeigt sich nur bei Regen.", "Shows itself only in the rain.")],
    "THUNDER_ONLY": [("Steigt nur im Gewitter auf.", "Rises only in a thunderstorm.")],
}
RARITY_NOTES = {
    "RARE": [("Ein seltener Fang.", "A rare catch."), ("Selten gesehen, noch seltener gelandet.", "Rarely seen, more rarely landed.")],
    "EPIC": [("Ein Prachtfang, von dem man am Lagerfeuer erzählt.", "A prize catch worth a campfire tale."),
             ("Wer so etwas landet, hat lange darauf gewartet.", "Whoever lands one has waited long for it.")],
}
BIG_NOTE = ("Wird länger als ein Ruderboot.", "Grows longer than a rowing boat.")
TINY_NOTE = ("Kaum länger als ein Finger.", "Barely longer than a finger.")
GLOW_NOTE = ("Leuchtet im Dunkeln.", "Glows in the dark.")
EFFECT_NOTES = {
    "speed": [("Macht flinke Beine.", "Makes for quick legs."), ("Wer davon isst, läuft wie der Wind.", "Eat it and you run like the wind.")],
    "haste": [("Lässt die Hände schneller arbeiten.", "Makes the hands work faster."),
              ("Bergleute und Holzfäller schwören darauf.", "Miners and woodcutters swear by it.")],
    "strength": [("Gibt Kraft in die Arme.", "Puts strength in the arms."), ("Ein Bissen, und jeder Schlag sitzt härter.", "One bite, and every blow lands harder.")],
    "jump_boost": [("Lässt höher springen.", "Lets you jump higher."), ("Danach nimmt man Zäune im Sprung.", "Afterwards you clear fences in a leap.")],
    "regeneration": [("Schließt Wunden.", "Closes wounds."), ("Heiler geben es den Verwundeten.", "Healers give it to the wounded.")],
    "resistance": [("Härtet ab.", "Toughens you."), ("Macht die Haut zäh wie Leder.", "Makes the skin tough as leather.")],
    "fire_resistance": [("Schützt vor Feuer.", "Shields from fire."), ("Danach schreckt keine Glut.", "No ember frightens you afterwards.")],
    "water_breathing": [("Lässt unter Wasser atmen.", "Lets you breathe under water."), ("Taucher essen es vor dem Sprung.", "Divers eat it before the plunge.")],
    "invisibility": [("Macht unsichtbar.", "Turns you invisible."), ("Wer davon isst, verschwindet für eine Weile.", "Eat it and you vanish for a while.")],
    "night_vision": [("Schärft die Augen für die Nacht.", "Sharpens the eyes for the night."), ("Danach braucht man keine Fackel.", "No torch needed afterwards.")],
    "health_boost": [("Schenkt zusätzliche Herzen.", "Grants extra hearts."), ("Macht für eine Weile zäher, als man ist.", "Makes you hardier than you are, for a while.")],
    "absorption": [("Legt einen goldenen Schutz ums Herz.", "Lays a golden guard around the heart."), ("Fängt die ersten Schläge ab.", "Soaks up the first blows.")],
    "luck": [("Bringt Glück.", "Brings luck."), ("Ein Glücksbringer – auch gegessen.", "A lucky charm – even eaten.")],
    "slow_falling": [("Lässt sanft fallen.", "Makes you fall gently."), ("Danach schwebt man zu Boden wie eine Feder.", "Afterwards you drift down like a feather.")],
    "conduit_power": [("Verleiht die Kraft des Meeres.", "Grants the power of the sea."),
                      ("Unter Wasser fühlt man sich danach zu Hause.", "Under water you feel at home afterwards.")],
    "dolphins_grace": [("Lässt schwimmen wie ein Delfin.", "Lets you swim like a dolphin."),
                       ("Im Wasser ist man danach kaum einzuholen.", "Hard to catch in the water afterwards.")],
}
BAD_NOTES = {
    "poison": ("Aber Vorsicht: giftig.", "But beware: poisonous."), "wither": ("Zehrt aber am Leben.", "But it gnaws at your life."),
    "hunger": ("Macht aber hungrig.", "But it leaves you hungry."), "nausea": ("Der Magen dreht sich dabei.", "The stomach turns, though."),
    "blindness": ("Nimmt kurz die Sicht.", "Briefly takes your sight."), "darkness": ("Danach wird es kurz finster.", "Darkness follows for a moment."),
    "slowness": ("Macht aber träge.", "But it makes you sluggish."), "weakness": ("Schwächt aber den Arm.", "But it weakens the arm."),
    "mining_fatigue": ("Lähmt aber die Hände.", "But it numbs the hands."), "glowing": ("Man leuchtet danach.", "You glow afterwards."),
    "levitation": ("Hebt kurz vom Boden.", "Lifts you off the ground for a moment."),
}
SPECIAL_NOTES = {
    "teleport_far": ("Versetzt beim Essen an einen anderen Ort.", "Carries you elsewhere when eaten."),
    "teleport_near": ("Versetzt ein paar Schritte.", "Shifts you a few steps."),
    "cleanse": ("Nimmt alle Leiden.", "Lifts all ailments."),
}


def note(sp: Species, rng: Rng) -> tuple:
    medium = "V" if sp.media == "VW" else sp.media[0]
    wo, where = sp.biome.where[medium]
    de, en = rng.pick(PLACE_NOTES)
    parts = [(de.replace("{wo}", wo), en.replace("{where}", where))]
    middle = []
    if sp.weather != "ANY":
        middle.append(rng.pick(WEATHER_NOTES[sp.weather]))
    elif sp.time != "ANY":
        middle.append(rng.pick(TIME_NOTES[sp.time]))
    else:
        options = [rng.pick(BEHAVIOR_NOTES[sp.behavior])]
        if sp.rarity in RARITY_NOTES:
            options.append(rng.pick(RARITY_NOTES[sp.rarity]))
        if sp.max >= 260:
            options.append(BIG_NOTE)
        if sp.max <= 12:
            options.append(TINY_NOTE)
        if sp.glows:
            options.append(GLOW_NOTE)
        middle.append(rng.pick(options))
    parts += middle
    if sp.special != "none":
        parts.append(SPECIAL_NOTES[sp.special])
    else:
        first = sp.effects[0][0]
        parts.append(rng.pick(EFFECT_NOTES[first]))
    for (e, _, _) in sp.effects:
        if e in BAD_NOTES:
            parts.append(BAD_NOTES[e])
            break
    return " ".join(p[0] for p in parts), " ".join(p[1] for p in parts)


# =============================================================================================
# Alles zusammen
# =============================================================================================

def generate() -> list:
    used_de, used_en, used_ids = existing_names()
    used_sig = existing_signatures()
    out = []
    images = set()
    for biome in biomes():
        rng = Rng("fish:" + biome.id)
        forms = pick_forms(biome, rng)
        # Seltenheit nach Rang der Gestalt (mit etwas Zufall): kleine Fische sind gewöhnlich, große Räuber selten
        ranked = sorted(range(len(forms)), key=lambda i: FORMS[forms[i][1]][3] + rng.uni(-1.3, 1.3))
        split = SPLIT[biome.n]
        rarity_of = {}
        pos = 0
        for r, count in enumerate(split):
            for i in ranked[pos:pos + count]:
                rarity_of[i] = RARITIES[r]
            pos += count
        prefixes = rng.shuffled(biome.prefixes)
        for k, (medium, form) in enumerate(forms):
            sp = Species()
            sp.biome, sp.media, sp.form, sp.rarity = biome, medium, form, rarity_of[k]
            r = RARITIES.index(sp.rarity)
            sp.id, sp.de, sp.en = name_for(biome, form, prefixes, k, rng, used_de, used_en, used_ids)
            lo, hi, behaviors, _, _ = FORMS[form]
            scale = rng.uni(0.75, 1.3) * (1.0 + 0.1 * r)
            sp.min = max(2, int(round(lo * scale)))
            sp.max = max(sp.min + 2, int(round(hi * scale)))
            sp.behavior = rng.pick(behaviors.split())
            bands = ((0.16, 0.34), (0.34, 0.54), (0.54, 0.72), (0.72, 0.86))[r]
            bonus = 0.04 if form in ("sword", "sail", "shark", "tuna", "mahi", "pike", "gar") else -0.05 if form in ("snail", "starfish", "jelly") else 0.0
            sp.difficulty = round(max(0.12, min(0.88, rng.uni(*bands) + bonus)), 2)
            if not biome.fixed:
                dark_place = biome.chapter in ("deep", "caves") or biome.theme in ("shadow", "cave", "swamp")
                roll = rng.f()
                if roll < (0.4 if dark_place else 0.22):
                    sp.time = "NIGHT"
                elif roll < (0.5 if dark_place else 0.42):
                    sp.time = "DAY"
                if biome.chapter in ("rivers", "sea") and medium == "W":
                    roll = rng.f()
                    if biome.theme == "storm" and sp.rarity == "EPIC":
                        sp.weather = "THUNDER_ONLY"
                        sp.time = "ANY"
                    elif roll < 0.03 and r >= 2:
                        sp.weather = "RAIN_ONLY"
                    elif roll < 0.12:
                        sp.weather = "RAIN"
            # eigener Zufall je Art: Wie oft hier gewürfelt wird, hängt davon ab, welche Wirkungen schon vergeben sind –
            # das darf die übrigen Eigenschaften (und die Arten danach) nicht verschieben
            assign_effects(sp, biome, Rng("effects:" + sp.id), used_sig)
            for attempt in range(8):
                img = paint(sp, biome, Rng("paint:%s:%d" % (sp.id, attempt)))
                digest = hashlib.md5(img.tobytes()).hexdigest()
                if digest not in images:
                    images.add(digest)
                    break
            else:
                raise RuntimeError("kein eigenes Bild: " + sp.id)
            sp.image = img
            sp.note_de, sp.note_en = note(sp, Rng("note:" + sp.id))
            out.append(sp)
    return out


def check(species: list) -> None:
    assert len(species) == 916, len(species)
    assert len(existing_signatures()) >= 70, len(existing_signatures())    # 80 Arten mit Wirkung; ohne die Dauer gleichen sich einige
    assert len({s.id for s in species}) == len(species)
    assert len({s.de for s in species}) == len(species)
    assert len({s.en for s in species}) == len(species)
    for s in species:
        assert re.fullmatch(r"[a-z0-9_]+", s.id), s.id
        assert s.effects, s.id
        assert "\t" not in s.note_de + s.note_en


def write(species: list) -> None:
    os.makedirs(DATA, exist_ok=True)
    os.makedirs(TEX, exist_ok=True)
    keep = set(ft.species())
    wanted = {s.id for s in species}
    for name in os.listdir(TEX):
        base = name[:-4]
        if name.endswith(".png") and base not in keep and base not in wanted:
            os.remove(os.path.join(TEX, name))
    with open(os.path.join(DATA, "species.tsv"), "w", encoding="utf-8", newline="\n") as f:
        f.write("# id\tSeltenheit\tcm von\tcm bis\tSchwierigkeit\tVerhalten\tBiom\tMedien\tKapitel\tZeit\tWetter\tBesonderes\tWirkungen (Name:Sekunden:Stufe)\n")
        for s in species:
            effects = ",".join("%s:%d:%d" % e for e in s.effects)
            f.write("\t".join([s.id, s.rarity, str(s.min), str(s.max), "%.2f" % s.difficulty, s.behavior, s.biome.id, s.media, s.biome.chapter.upper(),
                               s.time, s.weather, s.special.upper(), effects]) + "\n")
    with open(os.path.join(DATA, "species_lang.tsv"), "w", encoding="utf-8", newline="\n") as f:
        f.write("# id\tName (englisch)\tName (deutsch)\tNotiz (englisch)\tNotiz (deutsch)\n")
        for s in species:
            f.write("\t".join([s.id, s.en, s.de, s.note_en, s.note_de]) + "\n")
    for s in species:
        s.image.save(os.path.join(TEX, s.id + ".png"))
    print(len(species), "Arten geschrieben")


def preview(species: list, folder: str) -> None:
    os.makedirs(folder, exist_ok=True)
    by_chapter = {}
    for s in species:
        by_chapter.setdefault(s.biome.chapter, []).append(s)
    scale = 5
    cell = 16 * scale + 8
    for chapter, items in by_chapter.items():
        rows = []
        for s in items:
            if not rows or rows[-1][0] is not s.biome:
                rows.append((s.biome, []))
            rows[-1][1].append(s)
        per_sheet = 14
        for part in range(0, len(rows), per_sheet):
            chunk = rows[part:part + per_sheet]
            sheet = Image.new("RGBA", (12 * cell + 8, len(chunk) * (cell + 22) + 8), (36, 40, 48, 255))
            draw = ImageDraw.Draw(sheet)
            for r, (biome, fishes) in enumerate(chunk):
                y = 8 + r * (cell + 22)
                draw.text((8, y), biome.id.split(":")[1] + "  (" + biome.theme + ")", fill=(255, 240, 180, 255))
                for i, s in enumerate(fishes):
                    bg = {"W": (60, 96, 130, 255), "L": (120, 50, 24, 255), "V": (40, 28, 60, 255)}[s.media[0]]
                    tile = Image.new("RGBA", (16 * scale, 16 * scale), bg)
                    tile.alpha_composite(s.image.resize((16 * scale, 16 * scale), Image.NEAREST))
                    x = 8 + i * cell
                    sheet.alpha_composite(tile, (x, y + 12))
                    mark = {"COMMON": (255, 255, 255), "UNCOMMON": (90, 220, 90), "RARE": (88, 168, 255), "EPIC": (200, 120, 255)}[s.rarity]
                    draw.rectangle((x, y + 12, x + 5, y + 17), fill=mark + (255,))
            sheet.save(os.path.join(folder, "fish_%s_%d.png" % (chapter, part // per_sheet)))
    with open(os.path.join(folder, "names.txt"), "w", encoding="utf-8") as f:
        last = None
        for s in species:
            if s.biome is not last:
                f.write("\n== %s (%s) ==\n" % (s.biome.id, s.biome.chapter))
                last = s.biome
            effects = ", ".join("%s %d s%s" % (e, d, " " + "I" * (a + 1) if a else "") for (e, d, a) in s.effects)
            f.write("%-9s %s %-24s %-28s %3d-%3d cm  %.2f %-7s %-5s %-12s %s%s\n    %s\n" % (
                s.rarity, s.media.ljust(2), s.de, s.en, s.min, s.max, s.difficulty, s.behavior, s.time, s.weather,
                effects, "" if s.special == "none" else " + " + s.special, s.note_de))
    print("Übersicht in", folder)


def build(put) -> None:
    """Für tools/generate_textures.py."""
    for s in generate():
        put("item/fish/" + s.id, s.image)


def main() -> None:
    species = generate()
    check(species)
    if "--preview" in sys.argv:
        preview(species, sys.argv[sys.argv.index("--preview") + 1])
    elif "--check" in sys.argv:
        print(len(species), "Arten, alles eindeutig")
    else:
        write(species)


if __name__ == "__main__":
    main()
