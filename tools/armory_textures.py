#!/usr/bin/env python3
"""
Texturen der Waffenkammer (0.84), im Vanilla-Stil (siehe tools/item_style.py):

  * je Gegenstand das Bild im Inventar (16x16, schräg wie ein Schwert oder eine Spitzhacke),
  * je Gegenstand ein Atlas (32x32) für das 3D-Modell in der Hand und eine Leuchtmaske (die Bereiche, die leuchten).

Die Bereiche der Atlanten legt tools/armory_models.py fest (REGIONS); hier werden sie ausgemalt.
"""
from __future__ import annotations

import math
import os
import sys
from typing import Callable

from PIL import Image

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import item_style as st  # noqa: E402

# Tonleitern (0 = dunkler Umriss … 6 = Glanzlicht)
STEEL = st.ramp("#a9b2bc", dark=0.22, light=0.97, chroma_dark=1.3)
ASHWOOD = st.ramp("#9a6a3a", dark=0.16, light=0.84, cool=20)
OAK = st.ramp("#7a5228", dark=0.14, light=0.8, cool=20)
CHARRED = st.ramp("#4a3226", dark=0.1, light=0.66, cool=20)
LEATHER = st.ramp("#6a4428", dark=0.15, light=0.8, cool=20)
DARK_LEATHER = st.ramp("#3e2c3a", dark=0.1, light=0.7, cool=290)
DRACONIUM = st.ramp("#a8231e", dark=0.14, light=0.86, cool=320, warm=60, chroma_light=0.7)
GOLD = st.ramp("#e0a83a", dark=0.3, light=0.97, cool=20, warm=90)
NIGHTSTEEL = st.ramp("#5c5f8c", dark=0.12, light=0.93, cool=270, chroma_light=0.4)
ABYSS = st.ramp("#3a2a4e", dark=0.06, light=0.74, cool=280, chroma_light=0.6)
BRASS = st.ramp("#c89a3a", dark=0.28, light=0.95, warm=90)
TASSEL = st.ramp("#b8302a", dark=0.18, light=0.85, cool=330)
# leuchtende Farben (nur in den Leuchtmasken)
EMBER = st.fixed("#5a1606", "#8a2a08", "#c8400c", "#ff6a14", "#ff9a2e", "#ffc860", "#fff0b8")
VIOLET = st.fixed("#2a0c4a", "#4a1a7a", "#7a32b8", "#a85cf0", "#c88aff", "#e4c0ff", "#f8ecff")
MOON = st.fixed("#2a3a5a", "#4a6a9a", "#7aa0d0", "#a8c8f0", "#cfe4ff", "#ecf6ff", "#ffffff")
MAGENTA = st.fixed("#3a0632", "#6a0c5a", "#a01a8a", "#d02ab4", "#f05ad8", "#ff9cee", "#ffe0fa")
CYAN = st.fixed("#06303a", "#0c5a6a", "#16909e", "#2ac8d0", "#6af0f0", "#b8fffa", "#f0ffff")


# =============================================================================
# Malen
# =============================================================================

def _blank() -> tuple[Image.Image, Image.Image]:
    return Image.new("RGBA", (32, 32), (0, 0, 0, 0)), Image.new("RGBA", (32, 32), (0, 0, 0, 0))


def _lv(ramp, level: float) -> tuple:
    return ramp[max(0, min(len(ramp) - 1, int(round(level))))]


def paint(img: Image.Image, uv, color_at: Callable[[int, int, int, int], tuple | None]) -> None:
    """Malt einen Bereich (UV-Einheiten, je zwei Pixel); color_at(x, y, w, h) gibt die Farbe des Pixels."""
    px = img.load()
    x0, y0, x1, y1 = (round(c * 2) for c in uv)
    w, h = x1 - x0, y1 - y0
    for y in range(h):
        for x in range(w):
            c = color_at(x, y, w, h)
            if c is not None:
                px[x0 + x, y0 + y] = c


def h01(*a) -> float:
    h = 2166136261
    for v in a:
        h = ((h ^ (int(v) & 0xFFFFFFFF)) * 16777619) & 0xFFFFFFFF
    h ^= h >> 13
    h = (h * 1274126177) & 0xFFFFFFFF
    return (h & 0xFFFF) / 0xFFFF


def wood(ramp, seed: int):
    """Holz mit Längsmaserung: links im Licht, rechts im Schatten, dunkle Fasern."""
    def f(x, y, w, h):
        level = 4 if w == 1 else (5 if x == 0 else (3 if x == w - 1 else 4))
        if h01(x, y // 3, seed) < 0.22:
            level -= 1
        if h01(x, y, seed + 1) < 0.06:
            level -= 1
        return _lv(ramp, level)
    return f


def wrap(ramp):
    """Wicklung: schräge Bänder, jedes mit heller Kante."""
    def f(x, y, w, h):
        k = (y + x // 2) % 4
        if k == 0:
            return _lv(ramp, 1)
        if k == 1:
            return _lv(ramp, 5 if 0 < x < w - 1 else 4)
        return _lv(ramp, 3 if x == w - 1 else 4)
    return f


def metal(ramp, base: float = 4.0, rivets: bool = False):
    """Metall: oben ein Glanzstreif, unten Schatten, links heller als rechts."""
    def f(x, y, w, h):
        level = base
        if y == 0:
            level = base + 2
        elif y == h - 1:
            level = base - 2
        elif x == 0:
            level = base + 1
        elif x == w - 1:
            level = base - 1
        if rivets and 0 < y < h - 1 and (x, y) in ((1, 1), (w - 2, 1)):
            level = 6
        return _lv(ramp, level)
    return f


def top(ramp):
    """Deckel (oben/unten): heller Rand oben links, dunkler unten rechts."""
    def f(x, y, w, h):
        if x == 0 or y == 0:
            return _lv(ramp, 5)
        if x == w - 1 or y == h - 1:
            return _lv(ramp, 2)
        return _lv(ramp, 4)
    return f


def strand(ramp, tip: float = 6.0, base: float = 2.5, lateral: bool = True, seed: int = 0):
    """Ein Strang (Spitze oben hell, Fuß unten dunkel); quer: links heller, rechts dunkler."""
    def f(x, y, w, h):
        t = y / max(1, h - 1)
        level = tip + (base - tip) * t
        if lateral and w > 1:
            level += 0.6 if x == 0 else (-0.8 if x == w - 1 else 0.0)
        if h01(x, y, seed) < 0.08:
            level -= 0.8
        return _lv(ramp, level)
    return f


def blade(ramp, edge_left: bool = True, seed: int = 0):
    """Eine Klinge/ein Blatt von vorn: an der Schneide hell geschliffen, zur Mitte hin Grundfarbe, am Rücken dunkler."""
    def f(x, y, w, h):
        u = x / max(1, w - 1)
        if not edge_left:
            u = 1.0 - u
        level = 6 if u < 0.12 else 5 if u < 0.3 else 4 if u < 0.7 else 3
        if y == 0:
            level = max(level, 5)
        if h01(x, y, seed) < 0.05 and level < 6:
            level -= 1
        return _lv(ramp, level)
    return f


def glow_fill(ramp, core: float = 5.0):
    """Leuchtender Bereich: innen am hellsten."""
    def f(x, y, w, h):
        dx = abs(x - (w - 1) / 2) / max(1, w / 2)
        dy = abs(y - (h - 1) / 2) / max(1, h / 2)
        d = max(dx, dy)
        return _lv(ramp, core + 1 - d * 2.5)
    return f


def glow_strand(ramp):
    """Leuchtender Strang: an der Spitze am hellsten."""
    def f(x, y, w, h):
        t = y / max(1, h - 1)
        return _lv(ramp, 6 - t * 2.4 - (0.6 if x == w - 1 and w > 1 else 0.0))
    return f


# =============================================================================
# Atlanten
# =============================================================================

def _regions(name: str) -> dict:
    import armory_models as models
    return models.REGIONS[name]


def steel_halberd_atlas():
    R = _regions("steel_halberd")
    img, glow = _blank()
    paint(img, R["shaft"], wood(ASHWOOD, 3))
    paint(img, R["shaft_side"], wood(ASHWOOD, 5))
    paint(img, R["wrap"], wrap(LEATHER))
    paint(img, R["steel"], metal(STEEL))
    paint(img, R["steel_top"], top(STEEL))

    def axe(x, y, w, h):
        # die Schneide links, ein Loch und zwei Nieten nahe dem Schaft, feine Ätzlinien
        u = x / (w - 1)
        if (x, y) in ((w - 3, 4), (w - 3, h - 5)):
            return _lv(STEEL, 6)
        if (x, y) in ((w - 3, 5), (w - 3, h - 4)):
            return _lv(STEEL, 1)
        if w - 7 <= x <= w - 6 and h // 2 - 2 <= y <= h // 2 + 1:
            return _lv(STEEL, 1)                                           # das Durchbruchloch
        level = 6 if u < 0.08 else 5 if u < 0.22 else 4 if u < 0.62 else 3
        if 0.3 < u < 0.6 and (y == h // 2 - 4 or y == h // 2 + 4):
            level -= 1                                                     # Ätzlinien
        return _lv(STEEL, level)
    paint(img, R["blade"], axe)
    paint(img, R["edge"], lambda x, y, w, h: _lv(STEEL, 6 if x == 0 else 5))
    paint(img, R["blade_side"], lambda x, y, w, h: _lv(STEEL, 3 if x else 4))
    paint(img, R["point"], blade(STEEL, True, 7))
    paint(img, R["point_edge"], strand(STEEL, 5.5, 3.0, False))
    paint(img, R["hook"], strand(STEEL, 6.0, 3.0, True, 9))
    paint(img, R["hook_edge"], strand(STEEL, 4.5, 2.5, False))
    paint(img, R["tassel"], lambda x, y, w, h: _lv(TASSEL, (5 if x % 2 == 0 else 3) - (y / h) * 1.5 + (1 if y == 0 else 0)))
    paint(img, R["tassel_side"], lambda x, y, w, h: _lv(TASSEL, 3 - (y / h)))
    return img, glow


def draconium_warhammer_atlas():
    R = _regions("draconium_warhammer")
    img, glow = _blank()
    paint(img, R["handle"], wood(CHARRED, 11))
    paint(img, R["wrap"], wrap(DARK_LEATHER))
    paint(img, R["gold"], metal(GOLD))
    paint(img, R["gold_top"], top(GOLD))

    def head(x, y, w, h):
        # Draconium in Schuppenreihen, oben und unten ein goldener Rand, in der Mitte eine dunkle Fläche für die Rune
        if y == 0 or y == h - 1:
            return _lv(GOLD, 5 if y == 0 else 2)
        if x == 0 or x == w - 1:
            return _lv(GOLD, 4 if x == 0 else 2)
        cx = (w - 1) / 2
        if abs(x - cx) <= 3.5 and 1 <= y <= h - 2:
            return _lv(DRACONIUM, 2 if abs(x - cx) <= 2.5 else 3)
        row = (y - 1) // 2
        scale = (x + row * 2) % 4
        level = 5 if scale == 0 else 4 if scale in (1, 3) else 3
        if (y - 1) % 2 == 1:
            level -= 1
        return _lv(DRACONIUM, level)
    paint(img, R["head"], head)
    paint(img, R["head_side"], lambda x, y, w, h: _lv(DRACONIUM, 5 if y == 0 else 2 if y == h - 1 else (4 if (x + y) % 3 else 3)))

    def face(x, y, w, h):
        # Schlagfläche: Gold mit eingeschlagenem Kreuzmuster
        if x == 0 or y == 0:
            return _lv(GOLD, 6)
        if x == w - 1 or y == h - 1:
            return _lv(GOLD, 2)
        if (x + y) % 3 == 0 or (x - y) % 3 == 0:
            return _lv(GOLD, 3)
        return _lv(GOLD, 5)
    paint(img, R["face"], face)
    paint(glow, R["rune"], lambda x, y, w, h: _lv(EMBER, 6 if (x == w // 2 or y == h // 2) else (4 if (x + y) % 2 else 3)))
    paint(img, R["spike"], strand(GOLD, 6.0, 3.0, True, 13))
    paint(img, R["spike_edge"], strand(GOLD, 4.5, 2.0, False))
    paint(img, R["fang"], strand(st.ramp("#e8d6b0", dark=0.32, light=0.99, warm=70), 6.0, 2.5, True, 17))
    paint(img, R["fang_edge"], strand(st.ramp("#e8d6b0", dark=0.32, light=0.99, warm=70), 4.0, 2.0, False))
    paint(glow, R["ember"], glow_fill(EMBER))
    return img, glow


def nightsteel_dagger_atlas():
    R = _regions("nightsteel_dagger")
    img, glow = _blank()
    paint(img, R["grip"], wrap(DARK_LEATHER))
    paint(img, R["metal"], metal(NIGHTSTEEL))
    paint(img, R["metal_top"], top(NIGHTSTEEL))
    paint(glow, R["gem"], glow_fill(MOON))

    def guard(x, y, w, h):
        if y == 0:
            return _lv(NIGHTSTEEL, 6)
        if y == h - 1:
            return _lv(NIGHTSTEEL, 2)
        if abs(x - (w - 1) / 2) < 1:
            return _lv(VIOLET, 4)                                          # ein kleiner Stein in der Mitte
        return _lv(NIGHTSTEEL, 4 if x % 3 else 5)
    paint(img, R["guard"], guard)
    paint(img, R["guard_side"], metal(NIGHTSTEEL, 3.5))
    paint(img, R["blade"], blade(NIGHTSTEEL, True, 21))
    paint(img, R["blade_edge"], strand(NIGHTSTEEL, 5.0, 3.0, False))
    paint(glow, R["fuller"], glow_strand(VIOLET))
    paint(glow, R["fuller_side"], glow_strand(VIOLET))
    return img, glow


def abyssite_greatsword_atlas():
    R = _regions("abyssite_greatsword")
    img, glow = _blank()
    paint(img, R["grip"], wrap(DARK_LEATHER))
    paint(img, R["metal"], metal(ABYSS, 4.0))
    paint(img, R["metal_top"], top(ABYSS))
    paint(glow, R["gem"], glow_fill(MAGENTA))

    def guard(x, y, w, h):
        if y == 0:
            return _lv(ABYSS, 6)
        if y == h - 1:
            return _lv(ABYSS, 1)
        cx = (w - 1) / 2
        if abs(x - cx) <= 1:
            return _lv(MAGENTA, 3)
        return _lv(ABYSS, 4 if (x // 2) % 2 else 3)
    paint(img, R["guard"], guard)
    paint(img, R["guard_side"], metal(ABYSS, 3.0))

    def sword(x, y, w, h):
        u = x / (w - 1)
        level = 6 if u < 0.1 or u > 0.9 else 5 if u < 0.25 or u > 0.75 else 3
        if h01(x, y, 41) < 0.06:
            level -= 1
        return _lv(ABYSS, level)
    paint(img, R["blade"], sword)
    paint(img, R["blade_edge"], strand(ABYSS, 5.5, 3.0, False))
    paint(glow, R["rune"], lambda x, y, w, h: _lv(MAGENTA, 6 if y % 4 == 0 else (4 if y % 4 == 2 else 3)))
    paint(glow, R["crystal"], glow_strand(MAGENTA))
    paint(glow, R["crystal_edge"], glow_strand(MAGENTA))
    return img, glow


def steel_double_axe_atlas():
    R = _regions("steel_double_axe")
    img, glow = _blank()
    paint(img, R["shaft"], wood(OAK, 23))
    paint(img, R["shaft_side"], wood(OAK, 25))
    paint(img, R["wrap"], wrap(LEATHER))
    paint(img, R["steel"], metal(STEEL))
    paint(img, R["steel_top"], top(STEEL))

    def blades(x, y, w, h):
        # beide Blätter als ein Bild: außen die Schneiden hell, innen dunkler, in der Mitte (am Stiel) eine Fassung
        u = x / (w - 1)
        d = min(u, 1.0 - u)
        level = 6 if d < 0.05 else 5 if d < 0.13 else 4 if d < 0.32 else 3
        if 0.2 < d < 0.36 and y in (h // 2 - 3, h // 2 + 3):
            level -= 1
        if (x, y) in ((w // 2 - 4, h // 2), (w // 2 + 3, h // 2)):
            level = 6
        return _lv(STEEL, level)
    paint(img, R["blade"], blades)
    paint(img, R["blade_side"], lambda x, y, w, h: _lv(STEEL, 3 if x else 4))
    paint(img, R["edge"], lambda x, y, w, h: _lv(STEEL, 6 if x == 0 else 5))
    paint(img, R["spike"], strand(STEEL, 6.0, 3.0, True, 27))
    paint(img, R["spike_edge"], strand(STEEL, 4.5, 2.5, False))
    return img, glow


def miner_hammer_atlas():
    R = _regions("miner_hammer")
    img, glow = _blank()
    paint(img, R["handle"], wood(ASHWOOD, 31))
    paint(img, R["handle_side"], wood(ASHWOOD, 33))
    paint(img, R["wrap"], wrap(LEATHER))
    paint(img, R["steel"], metal(STEEL))
    paint(img, R["steel_top"], top(STEEL))

    def head(x, y, w, h):
        # ein schwerer Block: Kanten gefast, in der Mitte drei Querrillen (die Zahl der Reihen, die er bricht)
        if y == 0 or x == 0:
            return _lv(STEEL, 6 if y == 0 else 5)
        if y == h - 1 or x == w - 1:
            return _lv(STEEL, 2)
        if y == 1:
            return _lv(STEEL, 5)
        cx = (w - 1) / 2
        if abs(x - cx) <= 3.5 and y in (3, 5, 7):
            return _lv(STEEL, 2)
        return _lv(STEEL, 4 if (x + y * 3) % 7 else 3)
    paint(img, R["head"], head)
    paint(img, R["head_top"], top(STEEL))

    def face(x, y, w, h):
        if x == 0 or y == 0:
            return _lv(STEEL, 6)
        if x == w - 1 or y == h - 1:
            return _lv(STEEL, 2)
        return _lv(STEEL, 5 if (x + y) % 2 else 4)                          # aufgeraute Schlagfläche
    paint(img, R["face"], face)
    paint(img, R["pick"], strand(STEEL, 6.0, 3.0, True, 35))
    paint(img, R["pick_edge"], strand(STEEL, 4.5, 2.5, False))
    paint(img, R["brass"], top(BRASS))
    return img, glow


def _pick_atlas(name: str, handle, wrap_ramp, metal_ramp, pick_ramp, vein_ramp, core_detail):
    R = _regions(name)
    img, glow = _blank()
    paint(img, R["handle"], wood(handle, len(name)))
    paint(img, R["handle_side"], wood(handle, len(name) + 2))
    paint(img, R["wrap"], wrap(wrap_ramp))
    keys = [k for k in R if k in ("gold", "metal")]
    paint(img, R[keys[0]], metal(metal_ramp))
    paint(img, R[keys[0] + "_top"], top(metal_ramp))
    paint(img, R["pick"], strand(pick_ramp, 6.0, 2.6, True, len(name) * 3))
    paint(img, R["pick_edge"], strand(pick_ramp, 4.5, 2.0, False))
    paint(glow, R["vein"], glow_strand(vein_ramp))
    paint(glow, R["vein_edge"], glow_strand(vein_ramp))

    def core(x, y, w, h):
        if y == 0 or x == 0:
            return _lv(metal_ramp, 6)
        if y == h - 1 or x == w - 1:
            return _lv(metal_ramp, 2)
        return _lv(metal_ramp, 4 if (x + y) % 3 else 5)
    paint(img, R["core"], core)
    paint(img, R["core_side"], metal(metal_ramp, 3.5))
    core_detail(img, glow, R)
    return img, glow


def ember_pickaxe_atlas():
    return _pick_atlas("ember_pickaxe", CHARRED, DARK_LEATHER, GOLD, DRACONIUM, EMBER,
                       lambda img, glow, R: paint(glow, R["stone"], glow_fill(EMBER)))


def vein_breaker_atlas():
    def ore(img, glow, R):
        # Erzkristalle: grün, gold, blau
        cols = (st.fixed("#0a3a1a", "#16602e", "#2a9a4a", "#4ad86a", "#8af5a0", "#c8ffd4", "#f0fff4"), GOLD, CYAN)

        def f(x, y, w, h):
            # Kristalle in 2x2-Zellen: je Zelle eine Erzart, oben links ein Glanzpunkt, unten rechts Schatten
            bx, by = x // 2, y // 2
            ramp = cols[(bx + by * 2) % 3]
            lx, ly = x % 2, y % 2
            return _lv(ramp, 6 if (lx, ly) == (0, 0) else 3 if (lx, ly) == (1, 1) else 4)
        paint(glow, R["ore"], f)
    return _pick_atlas("vein_breaker", OAK, DARK_LEATHER, NIGHTSTEEL, NIGHTSTEEL, VIOLET, ore)


def ore_seeker_atlas():
    def eye(img, glow, R):
        def f(x, y, w, h):
            cx, cy = (w - 1) / 2, (h - 1) / 2
            d = math.hypot((x - cx) / (w / 2), (y - cy) / (h / 2))
            if d < 0.3:
                return _lv(CYAN, 6)
            if d < 0.62:
                return _lv(CYAN, 4)
            return _lv(MAGENTA, 3)
        paint(glow, R["eye"], f)
    return _pick_atlas("ore_seeker", CHARRED, DARK_LEATHER, ABYSS, ABYSS, CYAN, eye)


ATLASES = {
    "steel_halberd": steel_halberd_atlas, "draconium_warhammer": draconium_warhammer_atlas, "nightsteel_dagger": nightsteel_dagger_atlas,
    "abyssite_greatsword": abyssite_greatsword_atlas, "steel_double_axe": steel_double_axe_atlas, "miner_hammer": miner_hammer_atlas,
    "ember_pickaxe": ember_pickaxe_atlas, "vein_breaker": vein_breaker_atlas, "ore_seeker": ore_seeker_atlas,
}


def atlas(name: str) -> tuple[Image.Image, Image.Image]:
    return ATLASES[name]()


# =============================================================================
# Bilder im Inventar
# =============================================================================

# Gezeichnet im (p, s)-Raster der Diagonale (p = x + y quer zum Stiel, s = x - y entlang des Stiels), dann von Hand
# nachgebessert. Slots je Bild siehe ICON_RAMPS; der Umriss entsteht automatisch (item_style.Spr.outline).

ORE_GREEN = st.fixed("#0a3a1a", "#16602e", "#2a9a4a", "#4ad86a", "#8af5a0", "#c8ffd4", "#f0fff4")

STEEL_HALBERD = """
................
......65.....6..
.....6533...64..
....654433.643..
...6444433fd....
...544422fd43...
...54422lkt.2...
....54.lksr.....
......lk..q.....
.....lk.........
....lk..........
...lk...........
..lk............
.lk.............
.ec.............
................
"""

DRACONIUM_WARHAMMER = """
................
........n.......
.......nlu..n...
......nlttul....
......lsstFu....
.......rsFGEt...
........rsEssk..
.......42rrrki..
......l2..qki...
.....4j....i....
....42..........
...MK...........
..LJ............
.4K.............
.GE.............
................
"""

NIGHTSTEEL_DAGGER = """
................
................
............gg..
...........gtd..
..........gtd...
.....gf..gtd....
......gfgte.....
.......fed......
......MKedc.....
.....LJ...dc....
....MK..........
....GE..........
................
................
................
................
"""

ABYSSITE_GREATSWORD = """
................
.............g..
...........ggd..
..........gedb..
.........geDb...
........geFb....
..GF...geDb.....
...ff.geDb......
....ffeDb.......
.....ddb........
....LJdc........
...MK..cc.......
..LJ....ED......
.GK.............
.ED.............
................
"""

STEEL_DOUBLE_AXE = """
................
......ggf.......
.....ggfe...g...
....ggfee..fd...
....gfeeddcb....
....eddcccb.....
........cbccdd..
.......lbbbddef.
......lk..cdeff.
.....lk...ceff..
....Lk....dff...
...MK...........
..LJ............
.lk.............
.ec.............
................
"""

MINER_HAMMER = """
................
.......gf.......
......gffT......
.....gfeSfg.....
......cScefg....
.......cdceef...
........cdcdSf..
.......lkbcSdcb.
......lk..Qccb..
.....lk....cb...
....Lk..........
...MK...........
..LJ............
.lk.............
.ec.............
................
"""

EMBER_PICKAXE = """
................
................
.....ttt........
....urEEEtt.....
.......rFFGs....
........rljs....
........3qFFs...
.......31.qEs...
......31...qE...
.....31.....t...
....L1..........
...MK...........
..LJ............
.31.............
.Ej.............
................
"""

VEIN_BREAKER = """
................
................
....ffff........
...gsssstf......
.......dttRd....
........dScd....
........4ccd....
.......42..c....
......42........
.....42.........
....L2..........
...MK...........
..LJ............
.42.............
.ec.............
................
"""

ORE_SEEKER = """
................
................
................
....FdddEE......
.......ddSU.....
.........SdE....
........3.bE....
.......31..bE...
......31...bD...
.....31.....F...
....L1..........
...MK...........
..LJ............
.31.............
.ec.............
................
"""

ICON_ART = {
    "steel_halberd": (STEEL_HALBERD, {"A": STEEL, "B": STEEL, "C": ASHWOOD, "D": TASSEL}, ("A", "B", "D", "C")),
    "draconium_warhammer": (DRACONIUM_WARHAMMER, {"A": CHARRED, "C": GOLD, "D": DRACONIUM, "E": EMBER, "F": DARK_LEATHER},
                            ("E", "C", "D", "A", "F")),
    "nightsteel_dagger": (NIGHTSTEEL_DAGGER, {"B": NIGHTSTEEL, "D": VIOLET, "E": MOON, "F": DARK_LEATHER}, ("E", "D", "B", "F")),
    "abyssite_greatsword": (ABYSSITE_GREATSWORD, {"B": ABYSS, "E": MAGENTA, "F": DARK_LEATHER}, ("E", "B", "F")),
    "steel_double_axe": (STEEL_DOUBLE_AXE, {"B": STEEL, "C": OAK, "F": LEATHER}, ("B", "C", "F")),
    "miner_hammer": (MINER_HAMMER, {"B": STEEL, "C": ASHWOOD, "F": LEATHER, "G": BRASS}, ("B", "G", "C", "F")),
    "ember_pickaxe": (EMBER_PICKAXE, {"A": CHARRED, "C": GOLD, "D": DRACONIUM, "E": EMBER, "F": DARK_LEATHER}, ("E", "C", "D", "A", "F")),
    "vein_breaker": (VEIN_BREAKER, {"A": OAK, "B": NIGHTSTEEL, "D": VIOLET, "F": DARK_LEATHER, "G": ORE_GREEN}, ("G", "D", "B", "A", "F")),
    "ore_seeker": (ORE_SEEKER, {"A": CHARRED, "B": ABYSS, "E": CYAN, "F": DARK_LEATHER, "G": MAGENTA}, ("G", "E", "B", "A", "F")),
}


def icon(name: str) -> Image.Image:
    art, ramps, prefer = ICON_ART[name]
    return st.Spr.art(art).outline(prefer=prefer).image(ramps)


ICONS = {name: (lambda n=name: icon(n)) for name in ICON_ART}


def build(put) -> None:
    """Für tools/generate_textures.py."""
    for name in ICONS:
        put("item/" + name, ICONS[name]())
        img, glow = ATLASES[name]()
        put("item/" + name + "_model", img)
        put("item/" + name + "_model_glow", glow)


def preview(path: str) -> None:
    names = list(ICONS)
    scale = 12
    cell = 16 * scale + 16
    cols = 5
    rows = (len(names) + cols - 1) // cols
    sheet = Image.new("RGBA", (cols * cell + 16, rows * cell + 16), (40, 44, 52, 255))
    for i, n in enumerate(names):
        tile = Image.new("RGBA", (16 * scale, 16 * scale), (139, 139, 139, 255))
        tile.alpha_composite(ICONS[n]().resize((16 * scale, 16 * scale), Image.NEAREST))
        sheet.alpha_composite(tile, (16 + (i % cols) * cell, 16 + (i // cols) * cell))
    sheet.save(path)
    print("Vorschau:", path)


if __name__ == "__main__":
    if "--preview" in sys.argv:
        preview(sys.argv[sys.argv.index("--preview") + 1])
