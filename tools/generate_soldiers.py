#!/usr/bin/env python3
"""
Erzeugt die Texturen der Soldaten und Banditen (128x64, eigene Pixelkunst im Vanilla-Stil).

Aufbau einer Textur (siehe client/model/SoldierModel.java):
  * links (64x64) das gewohnte Spieler-Schema: Kopf, Körper, Arme, Beine – jeweils mit zweiter Schicht
    (Helm bzw. Kapuze, Rüstung/Wappenrock, Schulterstücke, Beinschienen)
  * rechts die Zusatzteile: Umhang (64,0), Helmfeder (64,20 / 72,20), Köcher (64,32), Pfeilfedern (64,48)

Soldatenarten: Schwertkämpfer, Bogenschütze, Ritter und Hauptmann, dazu Speerträger (Eisenhut, blauer Gambeson),
Armbrustschütze (Schaller oder Polsterhaube, Brigantine, Bolzenköcher), Schildträger (Spangenhelm, Kettenhemd,
Wappenrock), Waffenknecht (gebläuter Harnisch, Topfhelm bzw. Hundsgugel), Lanzenreiter (blanker Harnisch mit
Goldborten, Visierhelm, Umhang und Helmfeder) und Pionier (Lederschürze, Werkzeuggürtel, Filzkappe oder Gugel).

Jede Soldatenart hat mehrere Gesichter (Hautton, Haare, Bart); die Namen der Soldaten passen dazu
(die letzte Variante von Schwertkämpfer, Bogen- und Armbrustschütze, Speerträger und Pionier ist eine Frau;
Waffenknechte und Lanzenreiter tragen geschlossene Helme).

Aufruf (aus dem Projektordner):
    python3 tools/generate_soldiers.py            # schreibt alle Texturen
    python3 tools/generate_soldiers.py --preview  # zusätzlich eine Vorschau (Vorder- und Rückansicht)
"""
from __future__ import annotations

import hashlib
import sys
from pathlib import Path
from typing import Callable, Union

from PIL import Image

sys.path.insert(0, str(Path(__file__).resolve().parent))
from pixelart import CLEAR, Color, hexc, mix  # noqa: E402

ROOT = Path(__file__).resolve().parent.parent
TEX = ROOT / "src" / "main" / "resources" / "assets" / "kingdomsatwar" / "textures" / "entity"
SCRATCH = Path("/tmp/claude-0/-home-claude/50786bb5-6cc3-5907-a25e-13bee43305bb/scratchpad")

W, H = 128, 64

# (u, v, Breite, Höhe, Tiefe) der Quader im Textur-Schema
PARTS = {
    "head": (0, 0, 8, 8, 8), "hat": (32, 0, 8, 8, 8),
    "body": (16, 16, 8, 12, 4), "jacket": (16, 32, 8, 12, 4),
    "rarm": (40, 16, 4, 12, 4), "rsleeve": (40, 32, 4, 12, 4),
    "larm": (32, 48, 4, 12, 4), "lsleeve": (48, 48, 4, 12, 4),
    "rleg": (0, 16, 4, 12, 4), "rpants": (0, 32, 4, 12, 4),
    "lleg": (16, 48, 4, 12, 4), "lpants": (0, 48, 4, 12, 4),
    "cape": (64, 0, 10, 16, 1),
    "plume_holder": (64, 20, 2, 2, 2), "plume": (72, 20, 1, 6, 4),
    "quiver": (64, 32, 4, 11, 3), "fletching": (64, 48, 3, 3, 2),
}


def face_rect(part: str, face: str) -> tuple[int, int, int, int]:
    u, v, w, h, d = PARTS[part]
    return {
        "top": (u + d, v, w, d), "bottom": (u + d + w, v, w, d),
        "right": (u, v + d, d, h), "front": (u + d, v + d, w, h),
        "left": (u + d + w, v + d, d, h), "back": (u + d + w + d, v + d, w, h),
    }[face]


def h01(*args) -> float:
    """Fester Pseudo-Zufall 0..1 aus beliebigen Werten (gleiches Ergebnis bei jedem Aufruf)."""
    digest = hashlib.md5(repr(args).encode()).digest()
    return digest[0] / 255.0


Paint = Union[Color, Callable[[int, int, int, int], Color], None]


class Skin:
    def __init__(self):
        self.img = Image.new("RGBA", (W, H), CLEAR)
        self.px = self.img.load()

    def set(self, x: int, y: int, c: Color | None):
        if c is not None and 0 <= x < W and 0 <= y < H:
            self.px[x, y] = c

    def fill(self, part: str, face: str, paint: Paint):
        x0, y0, w, h = face_rect(part, face)
        for y in range(h):
            for x in range(w):
                self.set(x0 + x, y0 + y, paint(x, y, w, h) if callable(paint) else paint)

    def fill_all(self, part: str, paint: Paint, faces=("top", "bottom", "right", "front", "left", "back")):
        for f in faces:
            self.fill(part, f, paint)

    def art(self, part: str, face: str, rows: list[str], codes: dict[str, Paint], dy: int = 0):
        """ASCII-Kunst auf eine Fläche: '.' = nichts tun, ' ' = durchsichtig machen."""
        x0, y0, w, h = face_rect(part, face)
        for y, row in enumerate(rows):
            for x, ch in enumerate(row):
                if x >= w or y + dy >= h or ch == ".":
                    continue
                if ch == " ":
                    self.px[x0 + x, y0 + y + dy] = CLEAR
                    continue
                paint = codes[ch]
                self.set(x0 + x, y0 + y + dy, paint(x, y + dy, w, h) if callable(paint) else paint)

    def band(self, part: str, y: int, paint: Paint, faces=("right", "front", "left", "back")):
        """Eine waagerechte Linie rund um den Quader (z. B. ein Gürtel)."""
        for f in faces:
            x0, y0, w, h = face_rect(part, f)
            if 0 <= y < h:
                for x in range(w):
                    self.set(x0 + x, y0 + y, paint(x, y, w, h) if callable(paint) else paint)

    def rows(self, part: str, y_from: int, y_to: int, paint: Paint, faces=("right", "front", "left", "back")):
        for y in range(y_from, y_to + 1):
            self.band(part, y, paint, faces)

    def clear(self, part: str, faces=("top", "bottom", "right", "front", "left", "back")):
        self.fill_all(part, CLEAR, faces)


# =============================================================================
# Farben
# =============================================================================

def pal(*values: str) -> list[Color]:
    return [hexc(v) for v in values]


STEEL = pal("#272b33", "#3f454f", "#5c636e", "#7e8692", "#a3abb6", "#c9d0d8", "#eef2f6")
CHAIN = pal("#3a4049", "#535a64", "#6b737e", "#868e99")
BLUE = pal("#121d44", "#1a2b63", "#233c86", "#2f50a6", "#4469c2", "#6188d8")
GOLD = pal("#5a3c0c", "#8c5f16", "#bf8c24", "#e0b444", "#f7dc80")
RED = pal("#3a0a0e", "#601217", "#8a1c20", "#b0282a", "#cf4438", "#e8735c")
LEATHER = pal("#24170e", "#3c2617", "#583822", "#774d2f", "#94663f", "#b0845a")
LINEN = pal("#6e624a", "#8f8264", "#ad9f7c", "#c8bb96", "#ded3b2")
FOREST = pal("#13281a", "#1c3a22", "#28502c", "#376838", "#4a8248", "#649c58")
WOOL = pal("#26242a", "#36343c", "#48454f", "#5b5864", "#716e7a")
OLIVE = pal("#262a18", "#353b22", "#474e2e", "#5b633c", "#727b4c")
EYE_WHITE = hexc("#eeeeee")
BLACK = hexc("#0e0e12")

SKINS = {
    "light": pal("#a86e52", "#c98d6c", "#e3ae8b", "#f1c9a8", "#f8dcc2"),
    "medium": pal("#91603e", "#b07650", "#c99468", "#dbae84", "#e8c49e"),
    "tan": pal("#6e4329", "#8d5a39", "#a87251", "#bd8c69", "#cfa582"),
    "dark": pal("#3e2416", "#563522", "#6d4630", "#845a40", "#9a6e52"),
}
HAIRS = {
    "blond": pal("#6e5020", "#9a7632", "#c39c4a", "#ddc070", "#eedb9c"),
    "brown": pal("#2a180e", "#432818", "#5e3b22", "#7a5030", "#946a44"),
    "black": pal("#0f0c0d", "#1d1716", "#2d2522", "#40362f", "#54483f"),
    "red": pal("#4a160c", "#702614", "#963a1c", "#b45428", "#cc7240"),
    "gray": pal("#46464a", "#66666c", "#8a8a90", "#ababb0", "#cacace"),
    "chestnut": pal("#3a1c10", "#5a2e18", "#7a4426", "#965c38", "#ae7650"),
}
IRISES = {"blue": hexc("#3d5fae"), "brown": hexc("#4b2e1a"), "green": hexc("#3f7040"), "gray": hexc("#5c6878"),
          "hazel": hexc("#6b4f24")}


# =============================================================================
# Materialien (Muster) – f(x, y, Breite, Höhe) -> Farbe
# =============================================================================

def chainmail(dark_bottom: bool = True) -> Callable:
    """Kettengeflecht: Reihen kleiner Ringe, versetzt; oben etwas heller, am unteren Rand dunkler."""
    def f(x, y, w, h):
        ring = (x + (y // 1)) % 2 == 0
        tile = 2 if ring else 1
        if y % 2 == 0 and ring and h01("chain", x, y) > 0.6:
            tile = 3
        if dark_bottom and y >= h - 1:
            tile = max(0, tile - 1)
        return CHAIN[tile]
    return f


def cloth(ramp: list[Color], base: int = 2, seed: str = "", folds: tuple[int, ...] = (), shade_bottom: bool = True) -> Callable:
    """Stoff: Grundton mit wenigen dunkleren Flecken, senkrechten Falten und dunklerem Saum."""
    def f(x, y, w, h):
        i = base
        r = h01(seed, x, y)
        if r < 0.12:
            i -= 1
        elif r > 0.93:
            i += 1
        if x in folds:
            i -= 1
        if shade_bottom and y == h - 1:
            i -= 1
        return ramp[max(0, min(len(ramp) - 1, i))]
    return f


def plate(ramp: list[Color] = STEEL, base: int = 3, light_col: int | None = None, seed: str = "") -> Callable:
    """Glatte Metallplatte: oben heller, unten dunkler, eine Glanzspalte."""
    def f(x, y, w, h):
        t = 1.0 - y / max(1, h - 1)
        i = base + round((t - 0.5) * 2)
        if light_col is not None and x == light_col:
            i += 1
        if x == 0 or x == w - 1:
            i -= 1
        if h01(seed, x, y) < 0.06:
            i -= 1
        return ramp[max(0, min(len(ramp) - 1, i))]
    return f


def leather(ramp: list[Color] = LEATHER, base: int = 3, seed: str = "") -> Callable:
    def f(x, y, w, h):
        r = h01("leather", seed, x, y)
        i = base + (-1 if r < 0.18 else 1 if r > 0.9 else 0)
        if y == h - 1:
            i -= 1
        return ramp[max(0, min(len(ramp) - 1, i))]
    return f


def solid(c: Color) -> Callable:
    return lambda x, y, w, h: c


# =============================================================================
# Gesichter
# =============================================================================

def face_codes(skin: list[Color], hair: list[Color], iris: Color) -> dict[str, Paint]:
    return {
        "1": skin[0], "2": skin[1], "3": skin[2], "4": skin[3], "5": skin[4],
        "a": hair[0], "b": hair[1], "c": hair[2], "d": hair[3], "e": hair[4],
        "W": EYE_WHITE, "I": iris, "M": mix(skin[0], hexc("#5a1e1e"), 0.55), "m": mix(skin[1], hexc("#a8433a"), 0.45),
        "k": BLACK,
    }


# Vorderseite des Kopfes (8x8). Augen in Zeile 4, Nase Zeile 5, Mund Zeile 6.
FACES = {
    "plain": ["cccddccc", "cbcccccb", "b443344b", "3bb33bb3", "3WI33IW3", "33322333", "333mm332", "23333332"],
    "beard": ["cccddccc", "cbcccccb", "b443344b", "3bb33bb3", "3WI33IW3", "c332233c", "cbMMMMbc", "bcbccbcb"],
    "mustache": ["cccddccc", "cbcccccb", "b443344b", "3bb33bb3", "3WI33IW3", "33322333", "3cbMMbc3", "23333332"],
    "stubble": ["cccddccc", "cbcccccb", "b443344b", "3bb33bb3", "3WI33IW3", "33322333", "2b3MM3b2", "22b22b22"],
    "fullbeard": ["cccddccc", "cbcccccb", "b443344b", "3bb33bb3", "3WI33IW3", "b332233b", "bcMMMMcb", "abcbbcba"],
    "woman": ["dcccccdd", "cdccccdc", "c443344c", "3bb33bb3", "3kI33Ik3", "3332233c", "333mm33c", "2333333c"],
    "freckles": ["dcccccdd", "cdccccdc", "c443344c", "3bb33bb3", "3kI33Ik3", "32322323", "333mm33c", "2333333c"],
}


def paint_head(s: Skin, skin: list[Color], hair: list[Color], iris: Color, face: str, long_hair: bool = False):
    codes = face_codes(skin, hair, iris)
    s.art("head", "front", FACES[face], codes)
    side = ["ccccdccc", "bccccccb", "bccccc33", "bcccc233", "bccc3223", "bbcc3233", "bbc33332", "abb22222"]
    s.art("head", "right", side, codes)
    s.art("head", "left", [r[::-1] for r in side], codes)
    back = ["cccddccc", "ccdccdcc", "cccccccc", "bccccccb", "bcbccbcb", "bbbccbbb", "abbbbbba" if long_hair else "2bbbbbb2",
            "abbaabba" if long_hair else "22222222"]
    s.art("head", "back", back, codes)
    s.fill("head", "top", lambda x, y, w, h: hair[3 if h01("hairtop", x, y) > 0.7 else 2])
    s.fill("head", "bottom", skin[1])


# =============================================================================
# Soldaten
# =============================================================================

def tabard_codes(extra: dict[str, Paint] | None = None) -> dict[str, Paint]:
    codes: dict[str, Paint] = {
        "C": chainmail(), "V": cloth(BLUE, 3, "tabard"), "v": BLUE[2], "u": BLUE[1], "U": BLUE[0], "w": BLUE[4],
        "G": GOLD[3], "g": GOLD[2], "y": GOLD[4], "o": GOLD[1],
        "L": leather(), "l": LEATHER[1], "n": LEATHER[4], "N": LEATHER[5],
        "a": STEEL[0], "b": STEEL[1], "c": STEEL[2], "d": STEEL[3], "e": STEEL[4], "f": STEEL[5], "F": STEEL[6],
        "R": cloth(RED, 3, "red"), "r": RED[2], "q": RED[1], "Q": RED[0], "s": RED[4],
        "k": BLACK, "x": cloth(LINEN, 2, "linen"),
    }
    if extra:
        codes.update(extra)
    return codes


def boots(s: Skin, part: str, top_row: int, ramp: list[Color] = LEATHER, cuff: bool = True):
    def f(x, y, w, h):
        if y == top_row and cuff:
            return ramp[4]
        if y == h - 1:
            return ramp[0]
        return leather(ramp, 2, part)(x, y, w, h)
    s.rows(part, top_row, 11, f)
    s.fill(part, "bottom", ramp[0])


def paint_swordsman(skin_name: str, hair_name: str, iris: str, face: str, feminine: bool) -> Skin:
    s = Skin()
    skin, hair = SKINS[skin_name], HAIRS[hair_name]
    paint_head(s, skin, hair, IRISES[iris], face, long_hair=feminine)
    t = tabard_codes()

    # --- Helm (Normannenhelm mit Nasenschutz) und Kettenhaube ---
    s.art("hat", "top", ["bccddccb", "bcdeedcb", "cddefddc", "cdeffedc", "cdeffedc", "cddefddc", "bcdeedcb", "bccddccb"], t)
    s.art("hat", "front", ["bcdeedcb", "abccccba", "C  db  C", "C  cb  C", "C  cb  C", "C      C", "C      C", "CC    CC"], t)
    side = ["cddeeddc", "abbccbba", "CCCCCCCC", "CCCCCCCC", "CCCCCCCC", "CCCCCCCC", "CCCCCCCC", "CCCCCCCC"]
    s.art("hat", "right", side, t)
    s.art("hat", "left", side, t)
    s.art("hat", "back", ["cddeeddc", "abbccbba", "CCCCCCCC", "CCCCCCCC", "CCCCCCCC", "CCCCCCCC", "CCCCCCCC", "CCCCCCCC"], t)
    if feminine:  # geflochtener Zopf über der Kettenhaube
        braid = face_codes(skin, hair, IRISES[iris])
        s.art("hat", "back", ["........", "........", "........", "...cd...", "...bc...", "...cd...", "...bc...", "...cd..."], braid)
    s.clear("hat", ("bottom",))

    # --- Körper: Kettenhemd, blauer Wappenrock mit goldenem Kreuz, Gürtel ---
    front = ["CVVwwVVC", "CVVGGVVC", "CVVGGVVC", "CGGyyGGC", "CVVGGVVC", "CVVGGVVC", "CVVggVVC", "lLLGyLLl",
             "CVVuuVVC", "CVVuuVVC", "CVVUUVVC", "CgGUUGgC"]
    s.art("body", "front", front, t)
    s.art("jacket", "front", front, t)
    back = ["CVVVVVVC", "CVVVVVVC", "CVVVVVVC", "CVVVVVVC", "CVVVVVVC", "CVVVVVVC", "CVVuuVVC", "lLLLLLLl",
            "CVVVVVVC", "CVVVVVVC", "CVVVVVVC", "CgGggGgC"]
    s.art("body", "back", back, t)
    s.art("jacket", "back", back, t)
    for part in ("body", "jacket"):
        s.fill(part, "right", chainmail())
        s.fill(part, "left", chainmail())
        s.band(part, 7, t["L"], ("right", "left"))
        s.art(part, "top", ["CVVVVVVC", "CVVVVVVC", "CVVVVVVC", "CVVVVVVC"], t)
        s.fill(part, "bottom", chainmail())

    # --- Arme: Kettenärmel, Lederhandschuhe; Schulterplatten und Stulpen als zweite Schicht ---
    for arm, sleeve in (("rarm", "rsleeve"), ("larm", "lsleeve")):
        s.fill_all(arm, chainmail(), ("top", "right", "front", "left", "back"))
        s.rows(arm, 9, 11, leather(LEATHER, 3, arm))
        s.fill(arm, "bottom", LEATHER[2])
        s.clear(sleeve)
        s.fill(sleeve, "top", plate(STEEL, 4, seed=sleeve))
        s.rows(sleeve, 0, 2, plate(STEEL, 4, seed=sleeve + "p"))
        s.band(sleeve, 2, STEEL[1])
        s.rows(sleeve, 8, 8, LEATHER[4])
        s.rows(sleeve, 9, 9, LEATHER[3])

    # --- Beine: Wollhose, Stiefel; der Wappenrock reicht bis über die Knie ---
    for leg, pants in (("rleg", "rpants"), ("lleg", "lpants")):
        s.fill_all(leg, cloth(WOOL, 2, leg, shade_bottom=False), ("top", "right", "front", "left", "back"))
        boots(s, leg, 7)
        s.clear(pants)
        s.rows(pants, 0, 1, cloth(BLUE, 3, pants))
        s.band(pants, 2, GOLD[2])
        s.band(pants, 2, BLUE[1], ("left",) if leg == "rleg" else ("right",))
    return s


def paint_archer(skin_name: str, hair_name: str, iris: str, face: str, feminine: bool) -> Skin:
    s = Skin()
    skin, hair = SKINS[skin_name], HAIRS[hair_name]
    paint_head(s, skin, hair, IRISES[iris], face, long_hair=feminine)
    fc = face_codes(skin, hair, IRISES[iris])
    # Haarsträhnen unter der Kapuze
    s.art("head", "front", ["........", "........", ".cddcdc." if not feminine else ".dcddcd.", "........"], fc)
    t = tabard_codes({
        "H": cloth(FOREST, 3, "hood"), "h": FOREST[2], "j": FOREST[1], "J": FOREST[4], "i": FOREST[0],
        "T": cloth(LEATHER, 3, "jerkin"), "t": LEATHER[2], "p": LEATHER[5],
    })

    # --- Kapuze ---
    s.art("hat", "top", ["hHHHHHHh", "HHHJJHHH", "HHJJJJHH", "HHHJJHHH", "HHHHHHHH", "hHHHHHHh", "hhHHHHhh", "hhhhhhhh"], t)
    s.art("hat", "front", ["HJJJJJJH", "hiiiiiih", "H      H", "H      H", "H      H", "Hh    hH", "HH    HH", "HHh  hHH"], t)
    hood_side = ["HHHHHHHH", "HHHHHJHH", "HHHHHHHH", "HHHHHHhH", "HHHHHhHH", "hHHHHHHH", "hhHHHHHh", "hhhhhhhh"]
    s.art("hat", "right", hood_side, t)
    s.art("hat", "left", [r[::-1] for r in hood_side], t)
    s.art("hat", "back", ["HHHHHHHH", "HHHhhHHH", "HHHhhHHH", "HHHJhHHH", "HHHhhHHH", "HHHHHHHH", "hHHHHHHh", "hhhhhhhh"], t)
    if feminine:
        s.art("hat", "back", ["........", "........", "........", "........", "........", "...cd...", "...bc...", "...cd..."], fc)
    s.clear("hat", ("bottom",))

    # --- Körper: Lederwams mit Schnürung, Köchergurt, kleines Wappen auf der Brust ---
    front = ["ltTxxTTT", "TlTlxTvT", "TTlxlTGT", "TTTlxTTT", "TTTxlTTT", "TTTlxlTT", "TTTTTTlT", "TTTTTTTl",
             "lLLgyLLl", "TTTTTTTT", "TtTTTTtT", "tttttttt"]
    s.art("body", "front", front, t)
    s.art("jacket", "front", front, t)
    back = ["TTTTTTTT", "lTTTTTTT", "TlTTTTTT", "TTlTTTTT", "TTTlTTTT", "TTTTlTTT", "TTTTTlTT", "TTTTTTlT",
            "lLLLLLLl", "TTTTTTTT", "TtTTTTtT", "tttttttt"]
    s.art("body", "back", back, t)
    s.art("jacket", "back", back, t)
    for part in ("body", "jacket"):
        s.fill(part, "right", cloth(LEATHER, 3, part + "r"))
        s.fill(part, "left", cloth(LEATHER, 3, part + "l"))
        s.band(part, 8, t["L"], ("right", "left"))
        s.art(part, "top", ["xTTxxTTx", "TTTxxTTT", "TTTTTTTT", "TTTTTTTT"], t)
        s.fill(part, "bottom", LEATHER[2])
    s.art("jacket", "left", ["....", "....", "....", "....", "....", "....", "....", "....", ".nN."], t)  # Beutel

    # --- Arme: Leinenärmel, Armschienen, bloße Hände ---
    for arm, sleeve in (("rarm", "rsleeve"), ("larm", "lsleeve")):
        s.fill_all(arm, cloth(LINEN, 3, arm, folds=(1,)), ("top", "right", "front", "left", "back"))
        s.rows(arm, 7, 10, leather(LEATHER, 2, arm))
        s.band(arm, 7, LEATHER[4])
        s.rows(arm, 11, 11, skin[2])
        s.fill(arm, "bottom", skin[1])
        s.clear(sleeve)
        s.fill(sleeve, "top", cloth(LEATHER, 3, sleeve))
        s.rows(sleeve, 0, 1, cloth(LEATHER, 3, sleeve + "c"))
        s.band(sleeve, 2, LEATHER[1])

    # --- Beine: Hose in Olivgrün, hohe weiche Stiefel ---
    for leg, pants in (("rleg", "rpants"), ("lleg", "lpants")):
        s.fill_all(leg, cloth(OLIVE, 2, leg, folds=(2,), shade_bottom=False), ("top", "right", "front", "left", "back"))
        boots(s, leg, 6)
        s.clear(pants)

    # --- Köcher aus rotbraunem Leder mit Goldrand, Nähten und Pfeilfedern ---
    quiver_leather = pal("#2a0f0a", "#4a1c12", "#6a2c1a", "#8a4026", "#a65838")
    q = tabard_codes({"Q": leather(quiver_leather, 3, "quiver"), "j": quiver_leather[1], "J": quiver_leather[4]})
    s.fill_all("quiver", leather(quiver_leather, 3, "quiver"))
    s.band("quiver", 0, GOLD[3])
    s.band("quiver", 1, GOLD[1])
    s.band("quiver", 10, quiver_leather[1])
    for f_ in ("back", "front"):
        s.art("quiver", f_, ["GGGG", "oooo", "QJQQ", "QjQQ", "QJQG", "QjQQ", "QJQQ", "QjQG", "QJQQ", "QjQQ", "jjjj"], q)
    s.fill("quiver", "top", hexc("#1a0c08"))
    feather = {"W": hexc("#f2efe6"), "w": hexc("#c4bfb2"), "R": RED[3], "r": RED[2], "s": hexc("#6b4a2a")}
    for f_ in ("front", "back"):
        s.art("fletching", f_, ["WRW", "wWR", "sss"], feather)
    for f_ in ("right", "left"):
        s.art("fletching", f_, ["RW", "Ww", "ss"], feather)
    s.fill("fletching", "top", feather["W"])
    s.fill("fletching", "bottom", feather["s"])
    return s


def paint_knight(variant: int) -> Skin:
    s = Skin()
    # Gesicht hinter dem Visier (für den Fall, dass die zweite Schicht ausgeblendet ist)
    paint_head(s, SKINS["light"], HAIRS["brown"], IRISES["gray"], "stubble")
    accent = variant == 1
    t = tabard_codes({"Z": hexc("#08080b"), "T": GOLD[3]})

    # --- Topfhelm ---
    s.art("hat", "top", ["bccccccb", "cdeeeedc", "cdeffedc", "cefFfedc", "cefFfedc", "cdeffedc", "cdeeeedc", "bccccccb"], t)
    s.art("hat", "front", ["TTTTTTTT" if accent else "bcdeedcb", "cdeffedc", "cdeffedc", "bZZddZZb",
                           "cdeFfedc", "cdeFfZdZ", "bcdFfdZc", "aTTddTTa" if accent else "abbddbba"], t)
    hs = ["TTTTTTTT" if accent else "bcddeedc", "cddeedcc", "cdeeddcc", "bZddddcb", "cdeeddcb", "cdedddcb", "bcddddcb",
          "aTTTTTTa" if accent else "abbbbbba"]
    s.art("hat", "right", hs, t)
    s.art("hat", "left", [r[::-1] for r in hs], t)
    s.art("hat", "back", ["TTTTTTTT" if accent else "bcddddcb", "cdeddecc", "cdeddecc", "cddedccb", "cddedccb", "cdeddccb",
                          "bcddddcb", "aTTTTTTa" if accent else "abbbbbba"], t)
    s.fill("hat", "bottom", STEEL[0])

    # --- Körper: Wappenrock über dem Kettenhemd ---
    front = ["CwVVVVwC", "CVVGGVVC", "CVVGGVVC", "CGGyyGGC", "CVVGGVVC", "CVVGGVVC", "CVVGGVVC", "lGlLLlGl",
             "CVVuuVVC", "CVVuuVVC", "CVVUUVVC", "CGgUUgGC"]
    back = ["CVVVVVVC", "CVVGGVVC", "CVVGGVVC", "CGGyyGGC", "CVVGGVVC", "CVVGGVVC", "CVVVVVVC", "lGlLLlGl",
            "CVVVVVVC", "CVVVVVVC", "CVVVVVVC", "CGggggGC"]
    for part in ("body", "jacket"):
        s.art(part, "front", front, t)
        s.art(part, "back", back, t)
        s.fill(part, "right", chainmail())
        s.fill(part, "left", chainmail())
        s.band(part, 7, t["L"], ("right", "left"))
        s.art(part, "top", ["CVVVVVVC", "CVVVVVVC", "CVVVVVVC", "CVVVVVVC"], t)
        s.fill(part, "bottom", chainmail())

    # --- Arme: volle Plattenrüstung ---
    for arm, sleeve in (("rarm", "rsleeve"), ("larm", "lsleeve")):
        s.fill_all(arm, chainmail())
        s.clear(sleeve)
        arm_plate = ["deed", "cddc", "bccb", "abba", "cdcc", "bccb", "cefc", "bddb", "cddc", "bccb", "cddc", "abba"]
        for f_ in ("right", "front", "left", "back"):
            s.art(sleeve, f_, arm_plate, t)
        s.art(sleeve, "front", ["....", "....", "....", "TTTT" if accent else "....", "....", "....", "....", "....",
                                "....", "dcdc", "cbcb", "...."], t)
        s.fill(sleeve, "top", plate(STEEL, 4, seed="ktop" + sleeve))
        s.fill(sleeve, "bottom", STEEL[1])

    # --- Beine: Kettenbeinlinge, Kniebuckel, Beinschienen, Eisenschuhe ---
    for leg, pants in (("rleg", "rpants"), ("lleg", "lpants")):
        s.fill_all(leg, chainmail())
        s.clear(pants)
        s.rows(pants, 0, 1, cloth(BLUE, 3, pants))
        s.band(pants, 2, GOLD[2])
        greave = ["....", "....", "....", "cddc", "defd", "bccb", "cddc", "cddc", "bddc", "bccb", "cddc", "abba"]
        for f_ in ("front", "right", "left"):
            s.art(pants, f_, greave, t)
        s.art(pants, "back", ["....", "....", "....", "....", "....", "....", "bccb", "bccb", "bccb", "abba", "cddc", "abba"], t)
        s.fill(pants, "bottom", STEEL[1])

    # --- Umhang: blau mit goldenem Saum ---
    cape_out = []
    for y in range(16):
        row = ""
        for x in range(10):
            if y == 0:
                row += "o"
            elif x in (0, 9) or y == 15:
                row += "g" if (x + y) % 2 else "G"
            elif y == 14:
                row += "u"
            else:
                row += "V"
        cape_out.append(row)
    cape_out[1] = "G" + "wwwwwwww" + "G"
    s.art("cape", "back", cape_out, t)
    s.art("cape", "front", ["o" * 10] + ["u" + "v" * 8 + "u" for _ in range(14)] + ["g" * 10], t)
    s.fill("cape", "right", GOLD[2])
    s.fill("cape", "left", GOLD[2])
    s.fill("cape", "top", GOLD[1])
    s.fill("cape", "bottom", GOLD[2])

    # --- Helmfeder (nur Variante 2): blau mit goldenem Halter ---
    paint_plume(s, BLUE[3], BLUE[5], BLUE[1])
    return s


def paint_plume(s: Skin, base: Color, light: Color, dark: Color):
    s.fill_all("plume_holder", GOLD[3])
    s.fill("plume_holder", "top", GOLD[4])
    s.fill("plume_holder", "bottom", GOLD[1])

    def feather(x, y, w, h):
        if y == 0:
            return light
        if (x + y) % 3 == 0:
            return dark
        return base if y < h - 1 else dark
    s.fill_all("plume", feather)
    s.fill("plume", "top", light)


def paint_captain(skin_name: str, hair_name: str, iris: str, face: str) -> Skin:
    s = Skin()
    skin, hair = SKINS[skin_name], HAIRS[hair_name]
    paint_head(s, skin, hair, IRISES[iris], face)
    t = tabard_codes()

    # --- offener Helm mit goldenem Stirnband und Wangenklappen ---
    s.art("hat", "top", ["bccddccb", "bcdeedcb", "cddefddc", "cdeffedc", "cdeffedc", "cddefddc", "bcdeedcb", "bccddccb"], t)
    s.art("hat", "front", ["bcdeedcb", "oGGyyGGo", "dc    cd", "ec    ce", "dc    cd", "cb    bc", "b      b", "        "], t)
    side = ["cddeeddc", "oGGGGGGo", "cddeeddc", "cdeedfdc", "cddedddc", "bccddccb", "abbccbba", "    abba"]
    s.art("hat", "right", side, t)
    s.art("hat", "left", [r[::-1] for r in side], t)
    s.art("hat", "back", ["cddeeddc", "oGGGGGGo", "cddeeddc", "cddeeddc", "cddddddc", "bccddccb", "abbccbba", "abbbbbba"], t)
    s.clear("hat", ("bottom",))

    # --- Körper: Brustpanzer mit Goldrand, rote Offiziersschärpe, roter Waffenrock ---
    front = ["oGGyyGGo", "cRcefdcc", "ccRefdcc", "cdcRfdcc", "cdeRRdcc", "bcdefRcb", "bcddeeRb", "lLLGyLLR",
             "RRRRRRRR", "RRRqqRRR", "RRRqqRRR", "gGgggGgg"]
    back = ["oGGGGGGo", "ccdeedcR", "ccdeedRc", "ccdeeRcc", "ccdeRdcc", "bcdRedcb", "bcRddecb", "RLLLLLLl",
            "RRRRRRRR", "RRRRRRRR", "RRRRRRRR", "gGggggGg"]
    for part in ("body", "jacket"):
        s.art(part, "front", front, t)
        s.art(part, "back", back, t)
        s.fill(part, "right", plate(STEEL, 3, seed=part + "r"))
        s.fill(part, "left", plate(STEEL, 3, seed=part + "l"))
        s.rows(part, 8, 10, t["R"], ("right", "left"))
        s.band(part, 11, GOLD[2], ("right", "left"))
        s.band(part, 7, t["L"], ("right", "left"))
        s.band(part, 0, GOLD[3], ("right", "left"))
        s.art(part, "top", ["oGGyyGGo", "cddeeddc", "cddeeddc", "oGGGGGGo"], t)
        s.fill(part, "bottom", RED[1])

    # --- Arme: rote Ärmel, Schulterstücke mit Goldrand, Armschienen, Lederhandschuhe ---
    for arm, sleeve in (("rarm", "rsleeve"), ("larm", "lsleeve")):
        s.fill_all(arm, cloth(RED, 3, arm, folds=(2,)), ("top", "right", "front", "left", "back"))
        s.rows(arm, 9, 11, leather(LEATHER, 2, arm))
        s.band(arm, 9, LEATHER[4])
        s.fill(arm, "bottom", LEATHER[1])
        s.clear(sleeve)
        s.fill(sleeve, "top", plate(STEEL, 4, seed="ctop" + sleeve))
        pauldron = ["deed", "cdec", "bccb", "GyGG", "....", "....", "....", "cddc", "cedc", "bccb", "....", "...."]
        for f_ in ("right", "front", "left", "back"):
            s.art(sleeve, f_, pauldron, t)

    # --- Beine: dunkle Hose, Kniebuckel, hohe schwarze Stiefel ---
    for leg, pants in (("rleg", "rpants"), ("lleg", "lpants")):
        s.fill_all(leg, cloth(WOOL, 1, leg, shade_bottom=False), ("top", "right", "front", "left", "back"))
        boots(s, leg, 6, pal("#120e0c", "#1e1814", "#2c241e", "#3c3229", "#5a4a3c"))
        s.clear(pants)
        s.rows(pants, 0, 1, cloth(RED, 3, pants))
        s.band(pants, 1, GOLD[2])
        s.art(pants, "front", ["....", "....", "....", "....", "cddc", "defd", "bccb", "....", "....", "....", "....", "...."], t)

    # --- roter Umhang mit Goldsaum ---
    cape_out = []
    for y in range(16):
        row = ""
        for x in range(10):
            if y == 0:
                row += "o"
            elif x in (0, 9) or y == 15:
                row += "G" if (x + y) % 2 else "g"
            elif y == 14:
                row += "q"
            else:
                row += "R"
        cape_out.append(row)
    cape_out[1] = "G" + "ssssssss" + "G"
    s.art("cape", "back", cape_out, t)
    s.art("cape", "front", ["o" * 10] + ["q" + "r" * 8 + "q" for _ in range(14)] + ["g" * 10], t)
    s.fill("cape", "right", GOLD[2])
    s.fill("cape", "left", GOLD[2])
    s.fill("cape", "top", GOLD[1])
    s.fill("cape", "bottom", GOLD[2])
    paint_plume(s, RED[3], hexc("#f2ece0"), RED[1])
    return s


# =============================================================================
# Weitere Soldatenarten: Speerträger, Armbrustschütze, Schildträger, Waffenknecht, Lanzenreiter, Pionier
# =============================================================================

# gebläuter Stahl der Waffenknechte: fast schwarz, mit kühlem blauem Glanz
BLUED = pal("#11141f", "#1c2133", "#29304a", "#394362", "#4d5a7e", "#6878a0", "#93a1c6")
# braune Wollhose der Speerträger
UMBER = pal("#291f18", "#3a2d23", "#4e3e30", "#64503f", "#7b6551")
# rotbrauner Loden (Kapuze der Pioniere)
RUSSET = pal("#2a1610", "#422219", "#5a3022", "#74412e", "#8e563c")
# dunkles Stiefelleder
DARK_LEATHER = pal("#120e0c", "#1e1814", "#2c241e", "#3c3229", "#5a4a3c")
# helles Holz (Hammerstiel)
WOOD = pal("#7e5c34", "#9c7848", "#b48e5a")


def quilted(ramp: list[Color], base: int = 3, seed: str = "") -> Callable:
    """Steppstoff (Gambeson): senkrechte Steppnähte im Abstand von 2 Pixeln, dazwischen gepolsterte Bahnen."""
    def f(x, y, w, h):
        i = base if x % 2 == 0 else base - 1
        if x % 2 == 0 and y == 0:
            i += 1
        if x % 2 == 0 and h01("quilt", seed, x, y) < 0.04:
            i -= 1
        return ramp[max(0, min(len(ramp) - 1, i))]
    return f


def paint_spearman(skin_name: str, hair_name: str, iris: str, face: str, feminine: bool) -> Skin:
    """Speerträger: Eisenhut über der Polsterhaube, blauer Gambeson mit Goldborte, Lederarmschienen, braune Hose."""
    s = Skin()
    skin, hair = SKINS[skin_name], HAIRS[hair_name]
    paint_head(s, skin, hair, IRISES[iris], face, long_hair=feminine)
    t = tabard_codes({
        "S": quilted(BLUE, 3, "gambeson"), "B": BLUE[3], "W": BLUE[5],
        "X": quilted(LINEN, 2, "cap"), "i": LINEN[0], "I": LINEN[1], "j": LINEN[3],
    })

    # --- Eisenhut: Glocke mit breiter, blanker Krempe (von oben ein heller Ring), darunter die gesteppte Polsterhaube ---
    s.art("hat", "top", ["deeeeeed", "ebbbbbbe", "ebcddcbe", "ebdeedbe", "ebdeedbe", "ebcddcbe", "ebbbbbbe", "efffffff"], t)
    s.art("hat", "front", ["abcddcba", "bcdeedcb", "eFFFFFFe", "abbbbbba", "I      I", "X      X", "X      X", "Xi    iX"], t)
    side = ["abcddcba", "bcdeedcb", "efFFFFfe", "abbbbbba", "IIIIIIII", "XXXXXXXX", "XXXXXXXX", " XXXXXiX"]
    s.art("hat", "right", side, t)
    s.art("hat", "left", [r[::-1] for r in side], t)
    s.art("hat", "back", ["abcddcba", "bcdeedcb", "deffffed", "abbbbbba", "IIIIIIII", "XXXXXXXX", "XXXXXXXX", "        "], t)
    if feminine:  # Zopf unter der Haube
        braid = face_codes(skin, hair, IRISES[iris])
        s.art("hat", "back", ["........", "........", "........", "........", "........", "...cd...", "...bc...", "...cd..."], braid)
    s.clear("hat", ("bottom",))

    # --- Körper: blauer Gambeson mit senkrechten Steppnähten, Goldborte und Ledergürtel ---
    front = ["oGGyyGGo", "SSSgSSSS", "SSSySSSS", "SSSgSSSS", "SSSySSSS", "SSSgSSSS", "SSSySSSS", "lLLGyLLl",
             "SSSgSSSS", "SSSgSSSS", "SSSgSSSS", "SSSgSSSS"]
    back = ["oGGGGGGo"] + ["SSSSSSSS"] * 6 + ["lLLLLLLl"] + ["SSSSSSSS"] * 4
    for part in ("body", "jacket"):
        s.art(part, "front", front, t)
        s.art(part, "back", back, t)
        s.fill(part, "right", quilted(BLUE, 3, part + "r"))
        s.fill(part, "left", quilted(BLUE, 3, part + "l"))
        s.band(part, 0, GOLD[2], ("right", "left"))
        s.band(part, 7, t["L"], ("right", "left"))
        s.art(part, "top", ["SSSSSSSS", "SGGGGGGS", "SGGGGGGS", "SSSSSSSS"], t)
        s.fill(part, "bottom", BLUE[2])

    # --- Arme: gesteppte Ärmel, gepolsterte Schulter mit Goldnaht, Armschienen aus Leder, bloße Hände ---
    for arm, sleeve in (("rarm", "rsleeve"), ("larm", "lsleeve")):
        s.fill_all(arm, quilted(BLUE, 3, arm), ("top", "right", "front", "left", "back"))
        s.rows(arm, 11, 11, skin[2])
        s.fill(arm, "bottom", skin[1])
        s.clear(sleeve)
        s.fill(sleeve, "top", quilted(BLUE, 4, sleeve))
        s.rows(sleeve, 0, 1, quilted(BLUE, 4, sleeve + "s"))
        s.band(sleeve, 2, GOLD[2])
        s.band(sleeve, 7, LEATHER[4])
        s.rows(sleeve, 8, 9, leather(LEATHER, 3, sleeve))
        s.band(sleeve, 10, LEATHER[1])
        s.art(sleeve, "right" if arm == "rarm" else "left", ["....", "....", "....", "....", "....", "....", "....", "....",
                                                             ".nN.", ".Nn."], t)

    # --- Beine: braune Wollhose, Lederstiefel; der Gambeson reicht bis über die Oberschenkel ---
    for leg, pants in (("rleg", "rpants"), ("lleg", "lpants")):
        s.fill_all(leg, cloth(UMBER, 2, leg, folds=(1,), shade_bottom=False), ("top", "right", "front", "left", "back"))
        boots(s, leg, 7)
        s.clear(pants)
        s.rows(pants, 0, 1, quilted(BLUE, 3, pants))
        s.band(pants, 2, GOLD[2])
        s.band(pants, 2, BLUE[1], ("left",) if leg == "rleg" else ("right",))
        # goldbesetzte Vorderkante des geschlitzten Schoßes
        s.art(pants, "front", ["...g", "...g"] if leg == "rleg" else ["g...", "g..."], t)
    return s


def paint_crossbowman(skin_name: str, hair_name: str, iris: str, face: str, feminine: bool, helmet: str) -> Skin:
    """Armbrustschütze: Schaller ("sallet") oder Polsterhaube ("cap"), dunkelblaue Brigantine, Bolzenköcher."""
    s = Skin()
    skin, hair = SKINS[skin_name], HAIRS[hair_name]
    paint_head(s, skin, hair, IRISES[iris], face, long_hair=feminine)
    t = tabard_codes({"B": BLUE[3], "K": hexc("#120c08")})

    if helmet == "sallet":
        # --- Schaller: runde Glocke mit Grat, hinten der lange, ausgestellte Nackenschirm; das Gesicht bleibt frei ---
        s.art("hat", "top", ["abcddcba", "bcdeedcb", "cdeFfedc", "cdeFfedc", "cdeFfedc", "cdeffedc", "bcdeedcb", "abcddcba"], t)
        s.art("hat", "front", ["bcdeedcb", "cdeffedc", "abbccbba", "c      c", "b      b", "a      a", "        ", "        "], t)
        side = ["bcdeedcb", "cdeffedc", "cdeeeddb", "bcdddcb ", "bcddcb  ", "bccbb   ", "abb     ", "efd     "]
        s.art("hat", "right", side, t)
        s.art("hat", "left", [r[::-1] for r in side], t)
        s.art("hat", "back", ["bcdeedcb", "cdeffedc", "cdeeeedc", "bcddddcb", "bccddccb", "bbcccbbb", "abbbbbba", "deffffed"], t)
    else:
        # --- Polsterhaube: in Wülsten gesteppt, oben ein Messingknopf, Wangenklappen mit Lederriemen ---
        s.art("hat", "top", ["uvvvvvvu", "vBBBBBBv", "vBwwBBBv", "vBwGgBBv", "vBBggBBv", "vBBBBBBv", "vBBBBBBv", "uvvvvvvu"], t)
        s.art("hat", "front", ["vBBwwBBv", "uuuuuuuu", "vwwwwwwv", "B      B", "v      v", "u      u", "l      l", "        "], t)
        side = ["vBBwwBBv", "uuuuuuuu", "vBBBBBBw", "uuuuuuuv", "vBBBBBBv", "uvvvvvvu", " uuuuuul", "      l "]
        s.art("hat", "right", side, t)
        s.art("hat", "left", [r[::-1] for r in side], t)
        s.art("hat", "back", ["vBBwwBBv", "uuuuuuuu", "vBBBBBBv", "uuuuuuuu", "vBBBBBBv", "uvvvvvvu", "        ", "        "], t)
    s.clear("hat", ("bottom",))

    # --- Körper: dunkelblaue Brigantine mit Messingnieten, vorn verschnürt; Gürtel ---
    front = ["uvvxxvvu", "vGvuuvGv", "vvvuuvvv", "vGvuuvGv", "vvvuuvvv", "vGvuuvGv", "vvvuuvvv", "lLLGyLLl",
             "vGvvvvGv", "uuuuuuuu", "vvGvvGvv", "UUUUUUUU"]
    back = ["uvvvvvvu", "vGvvvvGv", "vvvvvvvv", "vGvvvvGv", "vvvvvvvv", "vGvvvvGv", "vvvvvvvv", "lLLLLLLl",
            "vGvvvvGv", "uuuuuuuu", "vvGvvGvv", "UUUUUUUU"]
    side = ["vvvv", "vvGv", "vvvv", "vvGv", "vvvv", "vvGv", "vvvv", "LLLL", "vvGv", "uuuu", "vGvv", "UUUU"]
    for part in ("body", "jacket"):
        s.art(part, "front", front, t)
        s.art(part, "back", back, t)
        s.art(part, "right", side, t)
        s.art(part, "left", [r[::-1] for r in side], t)
        s.art(part, "top", ["vvvvvvvv", "vvxxxxvv", "vvxxxxvv", "vvvvvvvv"], t)
        s.fill(part, "bottom", BLUE[0])
    if feminine:  # Zopf über dem Rücken
        braid = face_codes(skin, hair, IRISES[iris])
        s.art("jacket", "back", ["...cd...", "...bc...", "...cd...", "...ab..."], braid)

    # --- Arme: Kettenärmel, Lederhandschuhe mit Stulpen ---
    for arm, sleeve in (("rarm", "rsleeve"), ("larm", "lsleeve")):
        s.fill_all(arm, chainmail(), ("top", "right", "front", "left", "back"))
        s.rows(arm, 9, 11, leather(LEATHER, 2, arm))
        s.fill(arm, "bottom", LEATHER[1])
        s.clear(sleeve)
        s.band(sleeve, 8, LEATHER[4])
        s.band(sleeve, 9, LEATHER[3])

    # --- Beine: olivgrüne Beinlinge wie bei den Bogenschützen, hohe Stiefel; der Brigantinenschurz über den Hüften ---
    for leg, pants in (("rleg", "rpants"), ("lleg", "lpants")):
        s.fill_all(leg, cloth(OLIVE, 2, leg, folds=(2,), shade_bottom=False), ("top", "right", "front", "left", "back"))
        boots(s, leg, 6)
        s.clear(pants)
        s.art(pants, "front", ["GvvG", "uuuu"], t)
        s.art(pants, "back", ["GvvG", "uuuu"], t)
        s.art(pants, "right", ["vGvv", "uuuu"], t)
        s.art(pants, "left", ["vvGv", "uuuu"], t)

    # --- Bolzenköcher: lederbezogener Holzkasten mit Messingbeschlägen, kurze Bolzen mit Holzfiedern ---
    q = tabard_codes({"Q": leather(LEATHER, 2, "bolts"), "j": LEATHER[1], "J": LEATHER[3]})
    s.fill_all("quiver", leather(LEATHER, 2, "bolts"))
    for f_ in ("front", "back"):
        s.art("quiver", f_, ["GyGG", "oooo", "QJQQ", "QJQQ", "gGGg", "QJQQ", "QJQQ", "QJQQ", "gGGg", "QJQQ", "jjjj"], q)
    for f_ in ("right", "left"):
        s.art("quiver", f_, ["GGG", "ooo", "QQQ", "QQQ", "gGg", "QQQ", "QQQ", "QQQ", "gGg", "QQQ", "jjj"], q)
    s.fill("quiver", "top", t["K"])
    s.fill("quiver", "bottom", LEATHER[0])
    bolt = {"W": hexc("#e9e2cf"), "w": hexc("#b9b09a"), "B": BLUE[3], "b": BLUE[1], "s": hexc("#5a3e24"), "S": hexc("#3a2716")}
    for f_ in ("front", "back"):
        s.art("fletching", f_, ["W B", "wWb", "sSs"], bolt)
    for f_ in ("right", "left"):
        s.art("fletching", f_, ["B ", "bW", "Ss"], bolt)
    s.art("fletching", "top", ["W B", " W "], bolt)
    s.fill("fletching", "bottom", bolt["S"])
    return s


def paint_shieldbearer(skin_name: str, hair_name: str, iris: str, face: str) -> Skin:
    """Schildträger: Spangenhelm mit Nasenschutz über der Kettenhaube, langes Kettenhemd, blauer Wappenrock."""
    s = Skin()
    skin, hair = SKINS[skin_name], HAIRS[hair_name]
    paint_head(s, skin, hair, IRISES[iris], face)
    t = tabard_codes()

    # --- Spangenhelm: spitze Glocke mit Messingspangen und -reif, Nasenschutz; darunter die Kettenhaube ---
    s.art("hat", "top", ["bcdgocbb", "cdegodcb", "deegoddc", "gggyGgoo", "oooGGooo", "cddgoccb", "bcdgocbb", "bccgocba"], t)
    s.art("hat", "front", ["bcdgocbb", "cdegodcb", "oGGyyGGo", "C  fd  C", "C  ec  C", "C  db  C", "C      C", "CC    CC"], t)
    side = ["bcdgocbb", "cdegodcb", "oGGGGGGo", "CCCCCCCC", "CCCCCCCC", "CCCCCCCC", "CCCCCCCC", "CCCCCCCC"]
    for f_ in ("right", "left", "back"):
        s.art("hat", f_, side, t)
    s.clear("hat", ("bottom",))

    # --- Körper: langes Kettenhemd, Kettenkragen, blauer Wappenrock mit breitem goldenem Kreuz ---
    front = ["CCCCCCCC", "CVVwwVVC", "CVVGGVVC", "CGGGGGGC", "CggyyggC", "CVVGGVVC", "CVVggVVC", "lLLGyLLl",
             "CVVVVVVC", "CVVVVVVC", "CVVuuVVC", "CVVUUVVC"]
    back = ["CCCCCCCC", "ClVVVVVC", "CVlVVVVC", "CVVlVVVC", "CVVVlVVC", "CVVVVlVC", "CVVVVVlC", "lLLLLLLl",
            "CVVVVVVC", "CVVVVVVC", "CVVVVVVC", "CVVVVVVC"]
    for part in ("body", "jacket"):
        s.art(part, "front", front, t)
        s.art(part, "back", back, t)
        s.fill(part, "right", chainmail())
        s.fill(part, "left", chainmail())
        s.band(part, 7, t["L"], ("right", "left"))
        s.art(part, "top", ["CCCCCCCC", "CCCCCCCC", "CCCCCCCC", "CCCCCCCC"], t)
        s.fill(part, "bottom", chainmail())

    # --- Arme: Kettenärmel, Lederhandschuhe ---
    for arm, sleeve in (("rarm", "rsleeve"), ("larm", "lsleeve")):
        s.fill_all(arm, chainmail(), ("top", "right", "front", "left", "back"))
        s.rows(arm, 9, 11, leather(LEATHER, 3, arm))
        s.fill(arm, "bottom", LEATHER[2])
        s.clear(sleeve)
        s.band(sleeve, 8, LEATHER[4])
        s.band(sleeve, 9, LEATHER[3])

    # --- Beine: das Kettenhemd reicht bis über die Knie, darüber der lange Wappenrock; Stiefel ---
    for leg, pants in (("rleg", "rpants"), ("lleg", "lpants")):
        s.fill_all(leg, chainmail(), ("top", "right", "front", "left", "back"))
        boots(s, leg, 6)
        s.clear(pants)
        s.rows(pants, 0, 2, cloth(BLUE, 3, pants))
        s.band(pants, 3, GOLD[2])
        s.band(pants, 3, BLUE[1], ("left",) if leg == "rleg" else ("right",))
    return s


def paint_man_at_arms(variant: int) -> Skin:
    """Waffenknecht: gebläuter Plattenharnisch, Topfhelm (Variante 1) bzw. Hundsgugel (Variante 2)."""
    s = Skin()
    looks = [("light", "brown", "gray", "stubble"), ("tan", "black", "brown", "beard")]
    skin_name, hair_name, iris, face = looks[variant]
    paint_head(s, SKINS[skin_name], HAIRS[hair_name], IRISES[iris], face)
    t = tabard_codes({"Z": hexc("#07080c"), "Y": cloth(GOLD, 3, "mgold"), "7": hexc("#bec8e4")})
    t.update({str(i): c for i, c in enumerate(BLUED)})

    if variant == 0:
        # --- Topfhelm mit flachem Scheitel, Sehschlitzen und Atemlöchern, Messingkreuz auf dem Gesicht ---
        s.art("hat", "top", ["23333332", "34444443", "34555443", "34565443", "34555443", "34444443", "34444443", "23333332"], t)
        s.art("hat", "front", ["23444432", "34555543", "oggGGggo", "3ZZgGZZ3", "456gG654", "3Z4gG4Z3", "34ZgGZ43", "122gG221"], t)
        side = ["23444432", "34555543", "oggggggo", "34555543", "34555543", "23444432", "23444432", "12333321"]
        s.art("hat", "right", side, t)
        s.art("hat", "left", side, t)
        s.art("hat", "back", ["23444432", "34555543", "oggggggo", "34544543", "34544543", "23433432", "23433432", "12333321"], t)
        s.fill("hat", "bottom", BLUED[0])
    else:
        # --- Beckenhaube mit spitzem Klappvisier ("Hundsgugel"), darunter der Kettenkragen ---
        s.art("hat", "top", ["12333321", "23455432", "34566543", "34577543", "34566543", "23455432", "23444432", "12333321"], t)
        s.art("hat", "front", ["23455432", "12333321", "2ZZ67ZZ2", "23467432", "12357321", "1Z2562Z1", "C1Z55Z1C", "CC0230CC"], t)
        side = ["23455432", "34566543", "3455542Z", "345g5543", "23455542", "CCC34442", "CCCC2331", "CCCCC110"]
        s.art("hat", "right", side, t)
        s.art("hat", "left", [r[::-1] for r in side], t)
        s.art("hat", "back", ["23455432", "34566543", "34555543", "23444432", "12333321", "CCCCCCCC", "CCCCCCCC", "CCCCCCCC"], t)
        s.clear("hat", ("bottom",))

    # --- Körper: Plattenharnisch aus gebläutem Stahl, darüber ein geviertelter Wappenrock (blau und gold) ---
    front = ["4wwwyyy4", "3VVVYYY3", "4VVVYYY4", "3VVVYYY3", "3VVVYYY3", "2VVVYYY2", "2VVVYYY2", "lLLGyLLl",
             "3YYYVVV3", "2YYYVVV2", "2YYYVVV2", "1ggguuu1"]
    back = ["4yyywww4", "3YYYVVV3", "4YYYVVV4", "3YYYVVV3", "3YYYVVV3", "2YYYVVV2", "2YYYVVV2", "lLLLLLLl",
            "3VVVYYY3", "2VVVYYY2", "2VVVYYY2", "1uuuggg1"]
    side = ["4554", "3443", "3443", "3453", "3443", "2342", "2332", "LLLL", "3443", "1221", "3443", "1221"]
    for part in ("body", "jacket"):
        s.art(part, "front", front, t)
        s.art(part, "back", back, t)
        s.art(part, "right", side, t)
        s.art(part, "left", [r[::-1] for r in side], t)
        s.art(part, "top", ["45555554", "45wwyy54", "45wwyy54", "45555554"], t)
        s.fill(part, "bottom", BLUED[1])

    # --- Arme: Schulterstücke, Ellbogenkacheln, Armröhren und Plattenhandschuhe ---
    arm_front = ["4664", "3553", "2442", "1oo1", "2542", "2442", "3673", "1221", "2542", "2442", "3553", "1331"]
    arm_side = ["4554", "3443", "2332", "1oo1", "2332", "2332", "3443", "1221", "2332", "2332", "3443", "1221"]
    for arm, sleeve in (("rarm", "rsleeve"), ("larm", "lsleeve")):
        s.fill_all(arm, chainmail())
        s.clear(sleeve)
        s.art(sleeve, "front", arm_front, t)
        for f_ in ("right", "left", "back"):
            s.art(sleeve, f_, arm_side, t)
        s.fill(sleeve, "top", plate(BLUED, 4, seed="mtop" + sleeve))
        s.fill(sleeve, "bottom", BLUED[1])

    # --- Beine: Beinzeug mit Kniebuckeln, Beinröhren und Eisenschuhen ---
    leg_front = ["2552", "2442", "2442", "1331", "3673", "1gg1", "2552", "2542", "2442", "1331", "0110", "3553"]
    leg_side = ["2332", "2332", "2332", "1221", "3443", "1221", "2442", "2332", "2332", "1221", "0110", "2442"]
    for leg, pants in (("rleg", "rpants"), ("lleg", "lpants")):
        s.fill_all(leg, chainmail())
        s.clear(pants)
        s.art(pants, "front", leg_front, t)
        for f_ in ("right", "left", "back"):
            s.art(pants, f_, leg_side, t)
        s.fill(pants, "bottom", BLUED[1])
    return s


def paint_lancer(variant: int) -> Skin:
    """Lanzenreiter: blanker Harnisch mit Goldborten, Visierhelm, Waffenrock mit Sparren, Umhang und Helmfeder."""
    s = Skin()
    looks = [("light", "blond", "blue", "plain"), ("medium", "brown", "hazel", "mustache")]
    skin_name, hair_name, iris, face = looks[variant]
    paint_head(s, SKINS[skin_name], HAIRS[hair_name], IRISES[iris], face)
    t = tabard_codes({"Z": hexc("#08080b")})

    # --- Visierhelm mit goldenem Kamm und Goldborten ---
    s.art("hat", "top", ["bcdeedcb", "cdeGyedc", "deeGyeed", "deeGyeed", "deeGyeed", "deeGyeed", "cdeGyedc", "bcdeedcb"], t)
    s.art("hat", "front", ["cdeGyedc", "deeffeed", "oGGyyGGo", "aZZZZZZa", "cdeffedc", "cdefeZdZ", "bcdfedZb", "oGGyyGGo"], t)
    side = ["bcdeeddc", "cdeffeed", "ooGGGGGG", "cdddddZZ", "cdeeefff", "cdeGeeed", "bcddddcc", "oGGGGGGo"]
    s.art("hat", "right", side, t)
    s.art("hat", "left", [r[::-1] for r in side], t)
    s.art("hat", "back", ["bcdGydcb", "cdefeedc", "cdeeeedc", "cdeeeedc", "bcddddcb", "bcddddcb", "abccccba", "oGGGGGGo"], t)
    s.fill("hat", "bottom", STEEL[1])

    # --- Körper: blanker Harnisch mit Goldborten, darüber der blaue Waffenrock mit goldenem Sparren ---
    front = ["doGwwGod", "dVVVVVVd", "dVVyyVVd", "dVyggyVd", "dygVVgyd", "dgVVVVgd", "dVVVVVVd", "lLLGyLLl",
             "dVVVVVVd", "dVVVVVVd", "dVVuuVVd", "dGgGGgGd"]
    back = ["doGGGGod", "dVVVVVVd", "dVVyyVVd", "dVyggyVd", "dygVVgyd", "dgVVVVgd", "dVVVVVVd", "lLLLLLLl",
            "dVVVVVVd", "dVVVVVVd", "dVVVVVVd", "dGgGGgGd"]
    for part in ("body", "jacket"):
        s.art(part, "front", front, t)
        s.art(part, "back", back, t)
        s.fill(part, "right", plate(STEEL, 4, seed=part + "r"))
        s.fill(part, "left", plate(STEEL, 4, seed=part + "l"))
        s.band(part, 0, GOLD[3], ("right", "left"))
        s.band(part, 7, t["L"], ("right", "left"))
        s.band(part, 11, GOLD[2], ("right", "left"))
        s.art(part, "top", ["oGGGGGGo", "deeffeed", "deeffeed", "oGGGGGGo"], t)
        s.fill(part, "bottom", BLUE[1])

    # --- Arme: blanke Platten mit Goldborten ---
    arm_plate = ["deed", "cefc", "bccb", "GyGG", "cddc", "bccb", "deed", "abba", "cedc", "oGGo", "deed", "bccb"]
    for arm, sleeve in (("rarm", "rsleeve"), ("larm", "lsleeve")):
        s.fill_all(arm, chainmail())
        s.clear(sleeve)
        for f_ in ("right", "front", "left", "back"):
            s.art(sleeve, f_, arm_plate, t)
        s.fill(sleeve, "top", plate(STEEL, 5, seed="ltop" + sleeve))
        s.fill(sleeve, "bottom", STEEL[1])

    # --- Beine: der Waffenrock reicht bis an die Knie (für den Sattel geschlitzt), darunter blankes Beinzeug ---
    leg_front = ["....", "....", "....", "....", "GyGG", "deed", "cGdc", "cddc", "bddc", "bccb", "abba", "cddc"]
    leg_side = ["....", "....", "....", "....", "GGGG", "cddc", "cddc", "cddc", "bccb", "bccb", "abba", "cddc"]
    for leg, pants in (("rleg", "rpants"), ("lleg", "lpants")):
        s.fill_all(leg, chainmail())
        s.clear(pants)
        s.rows(pants, 0, 3, cloth(BLUE, 3, pants))
        s.art(pants, "front", leg_front, t)
        for f_ in ("right", "left", "back"):
            s.art(pants, f_, leg_side, t)
        s.fill(pants, "bottom", STEEL[1])

    # --- Umhang: blau mit Goldsaum und goldener Krone ---
    cape_out = []
    for y in range(16):
        row = ""
        for x in range(10):
            if y == 0:
                row += "o"
            elif x in (0, 9) or y == 15:
                row += "g" if (x + y) % 2 else "G"
            elif y == 14:
                row += "u"
            else:
                row += "V"
        cape_out.append(row)
    cape_out[1] = "G" + "wwwwwwww" + "G"
    cape_out[3] = "GVyVyyVyVG"
    cape_out[4] = "GVyyyyyyVG"
    cape_out[5] = "GVggggggVG"
    s.art("cape", "back", cape_out, t)
    s.art("cape", "front", ["o" * 10] + ["u" + "v" * 8 + "u" for _ in range(14)] + ["g" * 10], t)
    s.fill("cape", "right", GOLD[2])
    s.fill("cape", "left", GOLD[2])
    s.fill("cape", "top", GOLD[1])
    s.fill("cape", "bottom", GOLD[2])

    # --- Helmfeder: gold-blau bzw. rot-weiß ---
    if variant == 0:
        paint_plume(s, GOLD[3], GOLD[4], BLUE[2])
    else:
        paint_plume(s, hexc("#ece6da"), hexc("#ffffff"), RED[3])
    return s


def paint_engineer(skin_name: str, hair_name: str, iris: str, face: str, feminine: bool, headgear: str) -> Skin:
    """Pionier (Belagerungsknecht): Filzkappe ("cap") oder Gugel ("hood"), Lederschürze, Werkzeuggürtel mit Hammer."""
    s = Skin()
    skin, hair = SKINS[skin_name], HAIRS[hair_name]
    paint_head(s, skin, hair, IRISES[iris], face, long_hair=feminine)
    soot = {"s": mix(skin[1], hexc("#2a2624"), 0.3), "S": mix(skin[2], hexc("#2a2624"), 0.22)}
    s.art("head", "front", ["........", "........", "........", "........", "........", "......s.", "........", "........"], soot)

    def shirt(x, y, w, h):
        """Leinenhemd: ruhiger Grundton, ein paar Falten, wenig Flecken."""
        i = 3
        if (x + 2 * (y // 4)) % 5 == 1 or h01("shirt", x, y) < 0.05:
            i -= 1
        if y == h - 1:
            i -= 1
        return LINEN[i]

    def apron(x, y, w, h):
        """Lederschürze: glattes Leder, kaum Schrammen (die Rußflecken sind von Hand gesetzt)."""
        return LEATHER[2] if h01("apron", x, y) < 0.04 else LEATHER[3]
    t = tabard_codes({
        "x": shirt, "X": LINEN[2], "A": apron, "a": LEATHER[2],
        "P": DARK_LEATHER[3], "p": DARK_LEATHER[2], "Q": LEATHER[5],
        "B": DARK_LEATHER[2], "b": DARK_LEATHER[1],
        "h": WOOD[1], "H": WOOD[2], "s": mix(LEATHER[2], hexc("#1c1a1a"), 0.45), "S": soot["S"],
        "D": mix(DARK_LEATHER[3], LINEN[2], 0.45),
    })

    if headgear == "cap":
        # --- Filzkappe in Blau mit umgeschlagener Krempe ---
        felt = {"F": cloth(BLUE, 2, "felt"), "f": BLUE[1], "h": BLUE[3], "H": BLUE[4], "u": BLUE[0]}
        s.art("hat", "top", ["fFFFFFFf", "FFFFFFFF", "FFhHFFFF", "FFHhFFFF", "FFFFFFFF", "FFFFFFFF", "FFFFFFFF", "fFFFFFFf"], felt)
        s.art("hat", "front", ["fFFhhFFf", "FFFFFFFF", "hHHHHHHh"], felt)
        for f_ in ("right", "left", "back"):
            s.art("hat", f_, ["fFFFFFFf", "FFFFFFFF", "hHHHHHHh"], felt)
        if feminine:  # Zopf
            braid = face_codes(skin, hair, IRISES[iris])
            s.art("hat", "back", ["........", "........", "........", "........", "...cd...", "...bc...", "...cd...", "...bc..."], braid)
    else:
        # --- Gugel aus rotbraunem Loden ---
        hood = {"H": cloth(RUSSET, 3, "ehood"), "h": RUSSET[2], "j": RUSSET[1], "J": RUSSET[4], "i": RUSSET[0]}
        s.art("hat", "top", ["hHHHHHHh", "HHHJJHHH", "HHJJJJHH", "HHHJJHHH", "HHHHHHHH", "hHHHHHHh", "hhHHHHhh", "hhhhhhhh"], hood)
        s.art("hat", "front", ["HJJJJJJH", "hiiiiiih", "H      H", "H      H", "H      H", "Hh    hH", "HH    HH", "HHh  hHH"], hood)
        hood_side = ["HHHHHHHH", "HHHHHJHH", "HHHHHHHH", "HHHHHHhH", "HHHHHhHH", "hHHHHHHH", "hhHHHHHh", "hhhhhhhh"]
        s.art("hat", "right", hood_side, hood)
        s.art("hat", "left", [r[::-1] for r in hood_side], hood)
        s.art("hat", "back", ["HHHHHHHH", "HHHhhHHH", "HHHhhHHH", "HHHJhHHH", "HHHhhHHH", "HHHHHHHH", "hHHHHHHh", "hhhhhhhh"], hood)
    s.clear("hat", ("bottom",))

    # --- Körper: Leinenhemd, Lederschürze mit Latz und Brusttasche, Werkzeuggürtel mit Beutel und Hammer ---
    front = ["xxlxxlxx", "xxlxxlxx", "xxAAAAxx", "xxAnnAxx", "xxAaaAxx", "xxAAsAxx", "xAAAAAAx", "bBBGBBBb",
             "QPAAAdef", "pPAAAAHA", "xAAsAAhA", "xAAAAAAx"]
    back = ["xxxxxxxx", "xxxxxxxx", "xxxxxxxx", "xxxxxxxx", "xxxxxxxx", "xxxxxxxx", "xxxxxxxx", "bBBBBBBb",
            "xxxQQxxx", "xxxQlxxx", "xxxxxxxx", "xxxxxxxx"]
    for part in ("body", "jacket"):
        s.art(part, "front", front, t)
        s.art(part, "back", back, t)
        s.fill(part, "right", shirt)
        s.fill(part, "left", shirt)
        s.band(part, 7, DARK_LEATHER[2], ("right", "left"))
        s.art(part, "top", ["xxxxxxxx", "xxxxxxxx", "xxxxxxxx", "xxxxxxxx"], t)
        s.fill(part, "bottom", LINEN[2])
    s.art("jacket", "right", ["....", "....", "....", "....", "....", "....", "....", "....", ".QP.", ".pP."], t)  # Beutel

    # --- Arme: hochgekrempelte Ärmel, nackte Unterarme mit etwas Ruß ---
    for arm, sleeve in (("rarm", "rsleeve"), ("larm", "lsleeve")):
        s.fill_all(arm, shirt, ("top", "right", "front", "left", "back"))
        s.rows(arm, 5, 11, lambda x, y, w, h: skin[1] if y == 11 else skin[2])
        s.art(arm, "front", ["....", "....", "....", "....", "....", "....", "....", "..S.", "....", ".S..", "....", "...."], t)
        s.fill(arm, "bottom", skin[1])
        s.clear(sleeve)
        s.band(sleeve, 4, LINEN[4])
        s.band(sleeve, 5, LINEN[2])

    # --- Beine: graue Hose, Schürze bis an die Knie, derbe Stiefel mit Staub ---
    for leg, pants in (("rleg", "rpants"), ("lleg", "lpants")):
        s.fill_all(leg, cloth(WOOL, 2, leg, folds=(1,), shade_bottom=False), ("top", "right", "front", "left", "back"))
        boots(s, leg, 7, DARK_LEATHER)
        s.art(leg, "front", ["....", "....", "....", "....", "....", "....", "....", "....", "....", "....", ".D..", "D..D"], t)
        s.clear(pants)
        s.art(pants, "front", ["AAAA", "AAAA", "AAsA", "AAAA", "aaaa"], t)
    return s


# =============================================================================
# Banditen
# =============================================================================

HOODS = {
    "brown": pal("#17110d", "#241a13", "#33261b", "#453425", "#584432"),
    "charcoal": pal("#111214", "#1b1d20", "#272a2e", "#363a40", "#484d54"),
    "moss": pal("#141810", "#20271a", "#2d3624", "#3c4730", "#4e5b3e"),
}
MASKS = {
    "red": pal("#300c0c", "#4c1614", "#6a201c", "#88302a"),
    "black": pal("#0e0e0f", "#19191b", "#252528", "#333337"),
    "ochre": pal("#3e2c0e", "#5e4416", "#80601f", "#a07c30"),
}
DIRTY_LINEN = pal("#4e463a", "#665c4a", "#80755e", "#998d72")
TROUSERS = pal("#211e1a", "#2e2a24", "#3d3830", "#4e483e", "#615a4e")


def paint_bandit(variant: int, archer: bool) -> Skin:
    s = Skin()
    looks = [("tan", "black", "brown"), ("light", "red", "gray"), ("dark", "black", "brown")]
    skin_name, hair_name, iris = looks[variant]
    skin, hair = SKINS[skin_name], HAIRS[hair_name]
    paint_head(s, skin, hair, IRISES[iris], "stubble")
    hood_name = (["moss", "brown", "charcoal"] if archer else ["brown", "charcoal", "moss"])[variant]
    mask_name = (["black", "ochre", "red"] if archer else ["red", "black", "ochre"])[variant]
    hood, mask = HOODS[hood_name], MASKS[mask_name]
    t = tabard_codes({
        "H": cloth(hood, 3, "bhood" + hood_name), "h": hood[2], "j": hood[1], "J": hood[4], "i": hood[0],
        "S": cloth(mask, 2, "mask"), "s": mask[1], "z": mask[3],
        "T": cloth(LEATHER, 2, "vest"), "t": LEATHER[1], "P": LEATHER[4],
        "X": cloth(DIRTY_LINEN, 2, "shirt"), "O": cloth(TROUSERS, 2, "trousers"), "Y": TROUSERS[4],
    })

    # --- Kapuze, Schatten über den Augen, Tuch vor Mund und Nase ---
    s.art("hat", "top", ["hHHHHHHh", "HHHJHHHH", "HHHHHHHH", "HHHHHHJH", "HHHHHHHH", "hHHHHHHh", "hhHHHHhh", "hhhhhhhh"], t)
    s.art("hat", "front", ["HJJJJJJH", "hiiiiiih", "Hj    jH", "H      H", "H      H", "HSSzSSSH", "HSSSSsSH", "HsSSSSsH"], t)
    hood_side = ["HHHHHHHH", "HHHHHJHH", "HHHHHHHH", "HHHHHHhH", "HHHHHhHH", "SSSSSSHH", "sSSSSSHh", "ssSSSShh"]
    s.art("hat", "right", hood_side, t)
    s.art("hat", "left", [r[::-1] for r in hood_side], t)
    s.art("hat", "back", ["HHHHHHHH", "HHHhhHHH", "HHHhhHHH", "HHHJhHHH", "HHHhhHHH", "HHSzSSHH", "hHHSSHHh", "hhSsSShh"], t)
    s.clear("hat", ("bottom",))

    # --- Körper: geflickte Lederweste über schmutzigem Hemd, Gürtel mit Beutel ---
    if archer:
        front = ["XTTXXTTX", "TtTXXlTT", "TTTXlTTT", "TPTlXTTT", "TTlXXTTT", "TlTXXTPT", "lTTXXTTT", "TTTXXTTT",
                 "lLLnnLLl", "TTTXXTTT", "TTtXXtTT", "tttXXttt"]
    else:
        front = ["XTTXXTTX", "TtTXXTTT", "TTTXXTPT", "TPPXXTPT", "TTTXXTTT", "TTTXXTTT", "TtTXXTtT", "TTTXXTTT",
                 "lLLnnLLl", "TTTXXTTT", "TTtXXtTT", "tttXXttt"]
    back = ["TTTTTTTT", "TTTTTTTT", "TTPPTTTT", "TTPPTTTT", "TTTTTTTT", "TTTTTTTT", "TtTTTTtT", "TTTTTTTT",
            "lLLLLLLl", "TTTTTTTT", "TTtTTtTT", "tttttttt"]
    if archer:  # Köchergurt quer über den Rücken
        back = ["TTTTTTlT", "TTTTTlTT", "TTPPlTTT", "TTPlTTTT", "TTlTTTTT", "TlTTTTTT", "lTTTTTtT", "TTTTTTTT",
                "lLLLLLLl", "TTTTTTTT", "TTtTTtTT", "tttttttt"]
    for part in ("body", "jacket"):
        s.art(part, "front", front, t)
        s.art(part, "back", back, t)
        s.fill(part, "right", cloth(LEATHER, 2, part + "br"))
        s.fill(part, "left", cloth(LEATHER, 2, part + "bl"))
        s.band(part, 8, t["L"], ("right", "left"))
        s.art(part, "top", ["XTTXXTTX", "TTTXXTTT", "TTTTTTTT", "TTTTTTTT"], t)
        s.fill(part, "bottom", LEATHER[1])
    s.art("jacket", "right", ["....", "....", "....", "....", "....", "....", "....", "....", ".PN.", ".PP."], t)

    # --- Arme: hochgekrempelte Ärmel, Lederbinden um die Unterarme ---
    for arm, sleeve in (("rarm", "rsleeve"), ("larm", "lsleeve")):
        s.fill_all(arm, cloth(DIRTY_LINEN, 2, arm, folds=(1,)), ("top", "right", "front", "left", "back"))
        s.band(arm, 5, DIRTY_LINEN[3])
        s.rows(arm, 6, 11, lambda x, y, w, h: skin[1] if y == 11 else skin[2])
        s.rows(arm, 8, 10, lambda x, y, w, h: LEATHER[2] if y == 9 else LEATHER[3])
        s.fill(arm, "bottom", skin[1])
        s.clear(sleeve)
    # ein einzelnes Lederschulterstück (rechts)
    s.fill("rsleeve", "top", leather(LEATHER, 3, "pad"))
    s.rows("rsleeve", 0, 2, leather(LEATHER, 3, "pad2"))
    s.band("rsleeve", 2, LEATHER[1])

    # --- Beine: geflickte Hose, abgelaufene Stiefel mit Stoffwickeln ---
    for leg, pants in (("rleg", "rpants"), ("lleg", "lpants")):
        s.fill_all(leg, cloth(TROUSERS, 2, leg, folds=(1,), shade_bottom=False), ("top", "right", "front", "left", "back"))
        boots(s, leg, 8, pal("#1a120c", "#2a1d13", "#3a2a1c", "#4c3826", "#5e4632"), cuff=False)
        s.band(leg, 8, DIRTY_LINEN[1])
        s.band(leg, 9, DIRTY_LINEN[2])
        s.clear(pants)
    s.art("lleg", "front", ["....", "....", "....", ".YY.", ".YY.", "....", "....", "...."], t)  # Flicken am Knie

    if archer:
        s.fill_all("quiver", leather(LEATHER, 2, "bquiver"))
        s.band("quiver", 0, LEATHER[4])
        s.fill("quiver", "top", LEATHER[0])
        feather = {"W": hexc("#9a948a"), "w": hexc("#6e6a62"), "R": hexc("#2a2a2a"), "r": hexc("#1a1a1a"), "s": hexc("#4a3a2a")}
        for f_ in ("front", "back"):
            s.art("fletching", f_, ["WRW", "wWR", "sss"], feather)
        for f_ in ("right", "left"):
            s.art("fletching", f_, ["RW", "Ww", "ss"], feather)
        s.fill("fletching", "top", feather["W"])
        s.fill("fletching", "bottom", feather["s"])
    return s


# =============================================================================
# Alle Texturen
# =============================================================================

def all_skins() -> dict[str, Image.Image]:
    out: dict[str, Image.Image] = {}
    swordsmen = [("light", "brown", "blue", "beard", False), ("tan", "black", "brown", "plain", False),
                 ("medium", "blond", "green", "mustache", False), ("light", "red", "green", "woman", True)]
    for i, args in enumerate(swordsmen):
        out[f"soldier/swordsman_{i + 1}"] = paint_swordsman(*args).img
    archers = [("light", "blond", "blue", "plain", False), ("dark", "black", "brown", "plain", False),
               ("medium", "brown", "hazel", "stubble", False), ("light", "chestnut", "green", "freckles", True)]
    for i, args in enumerate(archers):
        out[f"soldier/archer_{i + 1}"] = paint_archer(*args).img
    for i in range(2):
        out[f"soldier/knight_{i + 1}"] = paint_knight(i).img
    captains = [("light", "brown", "blue", "fullbeard"), ("medium", "gray", "gray", "fullbeard"), ("dark", "black", "brown", "mustache")]
    for i, args in enumerate(captains):
        out[f"soldier/captain_{i + 1}"] = paint_captain(*args).img
    spearmen = [("tan", "brown", "brown", "stubble", False), ("light", "red", "blue", "fullbeard", False),
                ("dark", "black", "brown", "woman", True)]
    for i, args in enumerate(spearmen):
        out[f"soldier/spearman_{i + 1}"] = paint_spearman(*args).img
    crossbowmen = [("light", "blond", "gray", "mustache", False, "sallet"), ("medium", "black", "brown", "stubble", False, "cap"),
                   ("tan", "chestnut", "hazel", "freckles", True, "sallet")]
    for i, args in enumerate(crossbowmen):
        out[f"soldier/crossbowman_{i + 1}"] = paint_crossbowman(*args).img
    shieldbearers = [("medium", "brown", "green", "beard"), ("light", "gray", "blue", "fullbeard"), ("dark", "black", "brown", "mustache")]
    for i, args in enumerate(shieldbearers):
        out[f"soldier/shieldbearer_{i + 1}"] = paint_shieldbearer(*args).img
    for i in range(2):
        out[f"soldier/man_at_arms_{i + 1}"] = paint_man_at_arms(i).img
    for i in range(2):
        out[f"soldier/lancer_{i + 1}"] = paint_lancer(i).img
    engineers = [("light", "brown", "blue", "fullbeard", False, "cap"), ("tan", "black", "brown", "stubble", False, "hood"),
                 ("medium", "red", "green", "freckles", True, "cap")]
    for i, args in enumerate(engineers):
        out[f"soldier/engineer_{i + 1}"] = paint_engineer(*args).img
    for i in range(3):
        out[f"bandit/bandit_{i + 1}"] = paint_bandit(i, False).img
        out[f"bandit/bandit_archer_{i + 1}"] = paint_bandit(i, True).img
    return out


def front_view(img: Image.Image, back: bool = False) -> Image.Image:
    """Flache Vorder- bzw. Rückansicht (16x32) zum schnellen Anschauen."""
    view = Image.new("RGBA", (16, 32), CLEAR)
    face = "back" if back else "front"

    def blit(part, dx, dy):
        x0, y0, w, h = face_rect(part, face)
        region = img.crop((x0, y0, x0 + w, y0 + h))
        view.alpha_composite(region, (dx, dy))
    arms = [("rarm", "rsleeve", 0), ("larm", "lsleeve", 12)] if not back else [("larm", "lsleeve", 0), ("rarm", "rsleeve", 12)]
    legs = [("rleg", "rpants", 4), ("lleg", "lpants", 8)] if not back else [("lleg", "lpants", 4), ("rleg", "rpants", 8)]
    for base, over in (("head", "hat"),):
        blit(base, 4, 0)
        blit(over, 4, 0)
    blit("body", 4, 8)
    blit("jacket", 4, 8)
    for a, sl, x in arms:
        blit(a, x, 8)
        blit(sl, x, 8)
    for l, p, x in legs:
        blit(l, x, 20)
        blit(p, x, 20)
    return view


def main():
    skins = all_skins()
    for key, img in skins.items():
        path = TEX / f"{key}.png"
        path.parent.mkdir(parents=True, exist_ok=True)
        img.save(path)
    print(f"{len(skins)} Texturen geschrieben")
    if "--preview" in sys.argv:
        scale = 8
        keys = list(skins)
        sheet = Image.new("RGBA", (len(keys) * 36 * scale // 2, 2 * 34 * scale // 2 + 128 * 2), (90, 110, 130, 255))
        for i, key in enumerate(keys):
            for j, back in enumerate((False, True)):
                v = front_view(skins[key], back).resize((16 * scale // 2, 32 * scale // 2), Image.NEAREST)
                sheet.alpha_composite(v, (i * 36 * scale // 2 + 8, j * 34 * scale // 2 + 8))
        sheet.save(SCRATCH / "soldier_preview.png")
        big = Image.new("RGBA", (W * 6, H * 6 * 2), (60, 60, 70, 255))
        big.alpha_composite(skins["soldier/swordsman_1"].resize((W * 6, H * 6), Image.NEAREST), (0, 0))
        big.alpha_composite(skins["soldier/knight_2"].resize((W * 6, H * 6), Image.NEAREST), (0, H * 6))
        big.save(SCRATCH / "soldier_sheet.png")
        print("Vorschau:", SCRATCH / "soldier_preview.png")


if __name__ == "__main__":
    main()
