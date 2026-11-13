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
FAESTONE = st.ramp("#b4abc6", dark=0.3, light=0.95, cool=270, hue_dark=12, chroma_light=0.4)
PETRIFIED = st.ramp("#8c8496", dark=0.2, light=0.86, cool=280)
MOONSTONE_GEM = st.fixed("#2f4ea8", "#4f78d8", "#7aa6f0", "#a8ccff", "#cfe6ff", "#eef6ff", "#ffffff")
# leuchtende Farben (nur in den Leuchtmasken)
EMBER = st.fixed("#5a1606", "#8a2a08", "#c8400c", "#ff6a14", "#ff9a2e", "#ffc860", "#fff0b8")
VIOLET = st.fixed("#2a0c4a", "#4a1a7a", "#7a32b8", "#a85cf0", "#c88aff", "#e4c0ff", "#f8ecff")
MOON = st.fixed("#2a3a5a", "#4a6a9a", "#7aa0d0", "#a8c8f0", "#cfe4ff", "#ecf6ff", "#ffffff")
MAGENTA = st.fixed("#3a0632", "#6a0c5a", "#a01a8a", "#d02ab4", "#f05ad8", "#ff9cee", "#ffe0fa")
CYAN = st.fixed("#06303a", "#0c5a6a", "#16909e", "#2ac8d0", "#6af0f0", "#b8fffa", "#f0ffff")
# die Phasenklinge (1.5): Phasit, Purpur, Leerenstahl, violettes Leder
PHASIT = st.ramp("#2ad2b8", dark=0.3, light=0.98, cool=255, hue_dark=24, chroma_dark=1.05, warm=165, hue_light=8, chroma_light=0.45)
PURPUR = st.ramp("#a679a8", dark=0.24, light=0.94, cool=290, hue_dark=12, warm=330, hue_light=8, chroma_light=0.5)
VOID_STEEL = st.ramp("#3a3450", dark=0.12, light=0.7, cool=280, hue_dark=8, warm=300, hue_light=10, chroma_light=0.7)
PHASIT_GLOW = st.fixed("#064a44", "#0c7a6e", "#14a898", "#2ad2bc", "#6af0dc", "#b8fff2", "#f0fffc")
# die Stimmgabel (1.7): grün angelaufenes Kupfer, seegrünes Leder, Hallkristall
OX_COPPER = st.fixed("#2a3a30", "#3b5a4a", "#4f7d66", "#6aa286", "#b0663a", "#d88a52", "#f2b98a")
SEA_LEATHER = st.ramp("#2d6a6e", dark=0.12, light=0.8, cool=190)
ECHO_CRYSTAL = st.fixed("#1e3e5c", "#2c5a80", "#4a86b0", "#6aa6d0", "#94c8ea", "#c4e6ff", "#f2fbff")
ECHO_GLOW = st.fixed("#0c3a52", "#16608a", "#2a90c0", "#5ec0ea", "#9cd8ff", "#d4f0ff", "#ffffff")
#: das Leuchten des Sculk (wie Sordins Augen und die Noten auf seinem Pult)
SCULK_GLOW = st.fixed("#06343a", "#0b6f7c", "#139fac", "#29dfeb", "#8af4fb", "#c8fcff", "#ffffff")
# der Minutenzeiger (1.01): gebläuter Stahl, Uhrmessing, Chronit
BLUED = st.fixed("#0a0e2a", "#141c48", "#20306e", "#2e4896", "#4566b8", "#7290dc", "#b4c8f6")
CLOCK_BRASS = st.ramp("#d0a040", dark=0.28, light=0.96, warm=90)
CHRONIT_GLOW = st.fixed("#6a2c06", "#a2480c", "#d26a16", "#f08f24", "#ffb347", "#ffd27e", "#fff0c4")


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


def keeper_maul_atlas():
    R = _regions("keeper_maul")
    img, glow = _blank()
    paint(img, R["handle"], wood(PETRIFIED, 23))
    paint(img, R["handle_side"], wood(PETRIFIED, 29))
    paint(img, R["wrap"], wrap(LEATHER))
    paint(img, R["pommel"], metal(FAESTONE, 3.5))
    paint(img, R["pommel_top"], top(FAESTONE))
    band = lambda x, y, w, h: _lv(MOONSTONE_GEM, 5 if y == 0 else 3 if y == h - 1 else 4)
    paint(img, R["band"], band)
    paint(glow, R["band"], band)
    paint(img, R["band_top"], top(FAESTONE))

    def head(x, y, w, h):
        # gefügte Feensteinquader: Reihen mit versetzten Fugen, die Mitte frei für den Mondstein
        if y == 0:
            return _lv(FAESTONE, 6)
        if y == h - 1:
            return _lv(FAESTONE, 1)
        if x == 0 or x == w - 1:
            return _lv(FAESTONE, 5 if x == 0 else 2)
        row = (y - 1) // 4
        joint = (y - 1) % 4 == 3 or (x + (row % 2) * 3) % 6 == 0
        if joint:
            return _lv(FAESTONE, 1)
        return _lv(FAESTONE, 4 if (x * 7 + y * 3) % 5 else 5)
    paint(img, R["head"], head)
    paint(img, R["head_side"], lambda x, y, w, h: _lv(FAESTONE, 5 if y == 0 else 2 if y == h - 1 else (4 if (x + y) % 3 else 3)))

    def face(x, y, w, h):
        # Schlagfläche: grober, behauener Fels
        if x == 0 or y == 0:
            return _lv(FAESTONE, 5)
        if x == w - 1 or y == h - 1:
            return _lv(FAESTONE, 1)
        return _lv(FAESTONE, 3 + ((x * 5 + y * 3) % 4 == 0) - ((x + y * 2) % 5 == 0))
    paint(img, R["face"], face)
    gem = lambda x, y, w, h: _lv(MOONSTONE_GEM, 6 if (x, y) == (1, 1) else 5 if y < h / 2 else 3)
    paint(img, R["gem"], gem)
    paint(glow, R["gem"], gem)
    paint(img, R["crystal"], strand(MOONSTONE_GEM, 6.0, 2.0, True, 31))
    paint(img, R["crystal_edge"], strand(MOONSTONE_GEM, 5.0, 2.0, False))
    paint(glow, R["crystal"], glow_strand(MOONSTONE_GEM))
    paint(glow, R["crystal_edge"], glow_strand(MOONSTONE_GEM))
    return img, glow


DRIFTWOOD = st.ramp("#a89a84", dark=0.22, light=0.94, cool=30, hue_dark=10, chroma_light=0.4)
BLACK_CORAL = st.fixed("#060406", "#140c12", "#24161f", "#38242f", "#4e3443", "#6a4a5c", "#8a6a7c")
KELP = st.ramp("#3a6a3a", dark=0.12, light=0.8, cool=170)
SEA_ORB = st.fixed("#0a3a5a", "#126a8a", "#1e9cb4", "#3ccad8", "#7aeef0", "#c4fff8", "#ffffff")
SHELL = st.ramp("#e0b8b0", dark=0.3, light=0.98, cool=340)
BONE = st.ramp("#e8dcc4", dark=0.35, light=0.99, cool=40)


def sea_witch_staff_atlas():
    R = _regions("sea_witch_staff")
    img, glow = _blank()
    paint(img, R["shaft"], wood(DRIFTWOOD, 41))
    paint(img, R["shaft_side"], wood(DRIFTWOOD, 43))
    paint(img, R["knot"], metal(DRIFTWOOD, 3.0))
    paint(img, R["knot_top"], top(DRIFTWOOD))
    paint(img, R["wrap"], wrap(KELP))
    paint(img, R["coral"], strand(BLACK_CORAL, 5.5, 1.5, True, 47))
    paint(img, R["coral_edge"], strand(BLACK_CORAL, 4.5, 1.0, False))

    def orb(x, y, w, h):
        dx, dy = x - (w - 1) / 2, y - (h - 1) / 2
        r = math.hypot(dx, dy) / max(1.0, w / 2)
        swirl = math.sin(math.atan2(dy, dx) * 2.0 + r * 4.0)
        level = 5.2 - r * 2.0 + swirl * 0.6
        if (x, y) in ((1, 1), (2, 1), (1, 2)):
            level = 6
        return _lv(SEA_ORB, level)
    paint(img, R["orb"], orb)
    paint(glow, R["orb"], orb)
    paint(img, R["orb_side"], orb)
    paint(glow, R["orb_side"], orb)
    paint(img, R["cord"], lambda x, y, w, h: _lv(DRIFTWOOD, 1))
    paint(img, R["shell"], lambda x, y, w, h: _lv(SHELL, 5 if (x + y) % 2 == 0 else 3))
    paint(img, R["tooth"], strand(BONE, 6.0, 3.0, True))
    return img, glow


SWAMP_OAK = st.ramp("#4e3826", dark=0.12, light=0.72, cool=20)
ROPE = st.ramp("#8a7650", dark=0.2, light=0.86, cool=30)
DARK_IRON = st.ramp("#5e636e", dark=0.1, light=0.84, chroma_dark=1.2)
SOUL_FIRE = st.fixed("#06302e", "#0c5a54", "#16908a", "#2ac8bc", "#6af0e0", "#b8fff2", "#f0fffc")


def shepherd_crook_atlas():
    R = _regions("shepherd_crook")
    img, glow = _blank()
    paint(img, R["shaft"], wood(SWAMP_OAK, 53))
    paint(img, R["shaft_side"], wood(SWAMP_OAK, 55))
    paint(img, R["wrap"], wrap(ROPE))
    paint(img, R["iron"], metal(DARK_IRON))
    paint(img, R["iron_top"], top(DARK_IRON))
    paint(img, R["hook"], strand(DARK_IRON, 5.5, 2.5, True, 57))
    paint(img, R["hook_edge"], strand(DARK_IRON, 4.5, 2.0, False))
    paint(img, R["roof"], metal(DARK_IRON, 3.5))
    paint(img, R["roof_top"], top(DARK_IRON))

    def flame(x, y, w, h):
        # Seelenfeuer hinter dem Glas: unten in der Mitte am hellsten, die Ränder sind eiserne Streben
        if w > 2 and x in (0, w - 1):
            return _lv(DARK_IRON, 2)
        dx = abs(x - (w - 1) / 2) / max(1.0, w / 2)
        t = y / max(1, h - 1)
        return _lv(SOUL_FIRE, 6.2 - dx * 2.4 - abs(t - 0.62) * 2.6)
    paint(img, R["glass"], flame)
    paint(glow, R["glass"], flame)
    paint(img, R["glass_top"], lambda x, y, w, h: _lv(SOUL_FIRE, 4))
    paint(glow, R["glass_top"], lambda x, y, w, h: _lv(SOUL_FIRE, 4))
    paint(img, R["chain"], lambda x, y, w, h: _lv(DARK_IRON, 4 if y % 2 == 0 else 2))
    paint(img, R["cord"], lambda x, y, w, h: _lv(ROPE, 2))
    paint(img, R["bone"], strand(BONE, 5.5, 3.5, True))
    return img, glow


def minute_hand_atlas():
    R = _regions("minute_hand")
    img, glow = _blank()
    paint(img, R["grip"], wrap(DARK_LEATHER))
    paint(img, R["brass"], metal(CLOCK_BRASS, 4.0))
    paint(img, R["brass_top"], top(CLOCK_BRASS))
    paint(img, R["gem"], glow_fill(CHRONIT_GLOW))
    paint(glow, R["gem"], glow_fill(CHRONIT_GLOW))

    def guard(x, y, w, h):
        if y == 0:
            return _lv(CLOCK_BRASS, 6)
        if y == h - 1:
            return _lv(CLOCK_BRASS, 2)
        cx = (w - 1) / 2
        if abs(x - cx) < 1.0:
            return _lv(CHRONIT_GLOW, 4)                                      # ein Chronit in der Mitte
        return _lv(CLOCK_BRASS, 4 if (x // 2) % 2 else 5)                    # graviert
    paint(img, R["guard"], guard)

    def gear(x, y, w, h):
        dx, dy = x - (w - 1) / 2, y - (h - 1) / 2
        r = math.hypot(dx, dy) / (w / 2)
        a = math.atan2(dy, dx)
        if r > 1.0 or (r > 0.72 and math.cos(a * 6) < 0.0):
            return None
        return _lv(CLOCK_BRASS, 5 if dy < 0 else 3) if r > 0.3 else _lv(CHRONIT_GLOW, 5)
    paint(img, R["gear"], gear)

    def blued(x, y, w, h):
        u = x / max(1, w - 1)
        if abs(u - 0.5) < 0.2:
            return _lv(CLOCK_BRASS, 4)                                       # die Messingseele
        return _lv(BLUED, 6 if u < 0.15 or u > 0.85 else 4 if u < 0.5 else 3)
    paint(img, R["blade"], blued)
    paint(img, R["blade_side"], lambda x, y, w, h: _lv(BLUED, 5))
    paint(img, R["narrow"], lambda x, y, w, h: _lv(BLUED, 5 if x == 0 else 3))

    def ring(x, y, w, h):
        dx, dy = x - (w - 1) / 2, y - (h - 1) / 2
        r = math.hypot(dx, dy) / (w / 2)
        if r > 1.0 or 0.42 < r < 0.62:
            return None                                                      # durchbrochen
        if r <= 0.42:
            return _lv(CHRONIT_GLOW, 5)
        return _lv(CLOCK_BRASS, 5 if dy < 0 else 3)
    paint(img, R["ring"], ring)
    paint(glow, R["ring"], lambda x, y, w, h: ring(x, y, w, h) if math.hypot(x - (w - 1) / 2, y - (h - 1) / 2) / (w / 2) <= 0.42 else None)

    def tip(x, y, w, h):
        # eine Raute (Spitze oben), innen durchbrochen
        t = y / max(1, h - 1)
        half = (w / 2) * (t / 0.55 if t < 0.55 else 1.0 - (t - 0.55) / 0.45 * 0.7)
        d = abs(x - (w - 1) / 2)
        if d > half:
            return None
        if d < half - 1.6 and 0.3 < t < 0.75:
            return None
        return _lv(BLUED, 6 if d > half - 0.8 else 4)
    paint(img, R["tip"], tip)
    paint(img, R["core"], glow_strand(CHRONIT_GLOW))
    paint(glow, R["core"], glow_strand(CHRONIT_GLOW))
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


def phase_blade_atlas():
    R = _regions("phase_blade")
    img, glow = _blank()
    paint(img, R["grip"], wrap(DARK_LEATHER))
    paint(img, R["metal"], metal(VOID_STEEL, 4.0))
    paint(img, R["metal_top"], top(VOID_STEEL))
    paint(glow, R["gem"], glow_fill(PHASIT_GLOW))

    def guard(x, y, w, h):
        if y == 0:
            return _lv(PURPUR, 6)
        if y == h - 1:
            return _lv(PURPUR, 1)
        cx = (w - 1) / 2
        if abs(x - cx) <= 1:
            return _lv(PHASIT_GLOW, 4)
        return _lv(PURPUR, 4 if (x // 2) % 2 else 3)
    paint(img, R["guard"], guard)
    paint(img, R["guard_side"], metal(PURPUR, 3.0))

    def crystal(x, y, w, h):
        # Phasit: an den Schneiden hell, zur Mitte tiefer, schräge Facetten; hier und da ein violetter Funke
        u = x / (w - 1)
        level = 6 if u < 0.1 or u > 0.9 else 5 if u < 0.25 or u > 0.75 else 3 + ((x + y) // 3) % 2
        if h01(x, y, 47) < 0.05:
            level -= 1
        return _lv(PHASIT, level)
    paint(img, R["blade"], crystal)
    paint(img, R["blade_edge"], strand(PHASIT, 6.0, 4.0, False))
    paint(glow, R["ridge"], lambda x, y, w, h: _lv(PHASIT_GLOW, 6 if y % 5 == 0 else 5 if y % 5 in (1, 4) else 4))
    paint(glow, R["shard"], glow_strand(PHASIT_GLOW))
    paint(glow, R["shard_edge"], glow_strand(PHASIT_GLOW))
    return img, glow


def tuning_fork_atlas():
    R = _regions("tuning_fork")
    img, glow = _blank()
    paint(img, R["grip"], wrap(SEA_LEATHER))

    def copper(x, y, w, h):
        # Grünspan in Flecken, wo er fehlt, glänzt das Kupfer
        if h01(x, y, 61) < 0.3:
            return _lv(OX_COPPER, 5 if y == 0 else 4)
        return _lv(OX_COPPER, 3 if y == 0 else 2 + (x + y) % 2)
    paint(img, R["copper"], copper)
    paint(img, R["copper_top"], top(OX_COPPER))
    paint(glow, R["gem"], glow_fill(ECHO_GLOW))
    paint(img, R["yoke"], copper)
    paint(img, R["yoke_side"], copper)

    def prong(x, y, w, h):
        # Hallkristall: an den Kanten hell, innen tiefer; Ringe wie Wellen auf einem Teich
        level = 6 if x == 0 else 5 if x == w - 1 else 3 + (1 if (y // 3) % 2 else 0)
        if y % 6 == 0:
            level = min(6, level + 1)
        return _lv(ECHO_CRYSTAL, level)
    paint(img, R["prong"], prong)
    paint(img, R["prong_edge"], strand(ECHO_CRYSTAL, 6.0, 4.0, False))
    paint(glow, R["ring"], lambda x, y, w, h: _lv(SCULK_GLOW, 4 if y % 3 == 0 else 3))   # Ringe wie Wellen: türkis
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
    "phase_blade": phase_blade_atlas,
    "tuning_fork": tuning_fork_atlas,
    "ember_pickaxe": ember_pickaxe_atlas, "vein_breaker": vein_breaker_atlas, "ore_seeker": ore_seeker_atlas,
    "keeper_maul": keeper_maul_atlas,
    "sea_witch_staff": sea_witch_staff_atlas,
    "shepherd_crook": shepherd_crook_atlas,
    "minute_hand": minute_hand_atlas,
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

PHASE_BLADE = """
................
.............g..
...........ggf..
..........gfEb..
.........gfEb...
........gfEb....
..nm...gfEb.....
...lk.gfEb......
....kkfEb.......
.....jjb........
....LJjk........
...MK..ki.......
..LJ....nm......
.UK.............
.SQ.............
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
    "phase_blade": (PHASE_BLADE, {"B": PHASIT, "C": PURPUR, "E": PHASIT_GLOW, "F": DARK_LEATHER, "G": PHASIT_GLOW},
                    ("E", "G", "B", "C", "F")),
    "steel_double_axe": (STEEL_DOUBLE_AXE, {"B": STEEL, "C": OAK, "F": LEATHER}, ("B", "C", "F")),
    "miner_hammer": (MINER_HAMMER, {"B": STEEL, "C": ASHWOOD, "F": LEATHER, "G": BRASS}, ("B", "G", "C", "F")),
    "ember_pickaxe": (EMBER_PICKAXE, {"A": CHARRED, "C": GOLD, "D": DRACONIUM, "E": EMBER, "F": DARK_LEATHER}, ("E", "C", "D", "A", "F")),
    "vein_breaker": (VEIN_BREAKER, {"A": OAK, "B": NIGHTSTEEL, "D": VIOLET, "F": DARK_LEATHER, "G": ORE_GREEN}, ("G", "D", "B", "A", "F")),
    "ore_seeker": (ORE_SEEKER, {"A": CHARRED, "B": ABYSS, "E": CYAN, "F": DARK_LEATHER, "G": MAGENTA}, ("G", "E", "B", "A", "F")),
}


def icon(name: str) -> Image.Image:
    art, ramps, prefer = ICON_ART[name]
    return st.Spr.art(art).outline(prefer=prefer).image(ramps)


def keeper_maul_icon() -> Image.Image:
    """Der Hüterhammer im Inventar: schräg wie die Kriegshämmer – der Stiel von unten links nach oben rechts, quer darauf der
    Kopf aus Feenstein (gefügt, mit Mondstein in der Mitte), obenauf zwei kleine Kristalle, unten der Knaufstein."""
    s = st.Spr(16, 16)
    # Stiel (A: versteinertes Holz), Wicklung (F: Leder), Knaufstein (B)
    for k in range(10):
        x, y = 2 + k, 13 - k
        if k < 2:
            continue
        lvl = 4 if k % 3 else 3
        s.set(x, y, ("F", 3 + (k % 2)) if 3 <= k <= 5 else ("A", lvl))
        s.set(x - 1, y, ("A", 2) if not 3 <= k <= 5 else ("F", 2))
    s.set(1, 14, ("B", 3))
    s.set(2, 14, ("B", 2))
    s.set(1, 13, ("B", 5))
    s.set(2, 13, ("B", 4))
    # der Kopf: ein Quader quer zum Stiel, Mitte bei (10, 5)
    cx, cy = 10.0, 5.0
    for y in range(16):
        for x in range(16):
            u = ((x - cx) + (y - cy)) / 1.4142          # entlang des Kopfs (unten rechts = +)
            v = ((x - cx) - (y - cy)) / 1.4142          # quer (nach oben rechts = +)
            if abs(u) <= 4.6 and abs(v) <= 2.25:
                lvl = 4 + (1 if v > 0.8 else 0) - (1 if v < -1.2 else 0)
                if abs(u) > 3.6:
                    lvl -= 1                             # die Schlagflächen etwas dunkler
                if int((u + 4.6) * 1.1) % 3 == 0 and abs(u) <= 3.6:
                    lvl -= 1                             # Fugen zwischen den Quadern
                s.set(x, y, ("B", max(1, min(6, lvl))))
                if abs(u) <= 0.8 and abs(v) <= 1.0:
                    s.set(x, y, ("E", 5 if v > 0 else 4))
    s.set(10, 5, ("E", 6))
    # zwei Kristalle obenauf (D)
    for (x, y, lvl) in ((8, 2, 4), (7, 1, 5), (12, 4, 4), (12, 3, 5), (13, 2, 6)):
        s.set(x, y, ("D", lvl))
    return s.outline(prefer=("E", "D", "B", "F", "A")).image({"A": PETRIFIED, "B": FAESTONE, "D": MOONSTONE_GEM, "E": MOONSTONE_GEM,
                                                               "F": LEATHER})


def sea_witch_staff_icon() -> Image.Image:
    """Der Hexenstab im Inventar: schräg von unten links nach oben rechts, gebleichtes Treibholz (am Griff Tang), oben die
    leuchtende Kugel zwischen drei schwarzen Korallenzinken, darunter eine Muschel an einer Schnur."""
    s = st.Spr(16, 16)
    for k in range(12):
        x, y = 1 + k, 14 - k
        lvl = 4 if k % 3 else 3
        if 2 <= k <= 4:
            s.set(x, y, ("F", 4 if k % 2 else 3))                # Tang am Griff
            s.set(x - 1, y, ("F", 2))
            continue
        s.set(x, y, ("A", lvl))
        if k > 0:
            s.set(x - 1, y, ("A", 2))
    s.set(0, 15, ("A", 3))
    # die Korallenzinken um die Kugel
    for (x, y, lvl) in ((10, 2, 3), (9, 1, 4), (14, 6, 3), (15, 5, 4), (13, 3, 2), (11, 6, 2)):
        s.set(x, y, ("B", lvl))
    # die Kugel (E), Mitte bei (12, 3.5)
    for (x, y, lvl) in ((11, 3, 5), (12, 3, 6), (13, 4, 4), (12, 4, 5), (11, 4, 4), (12, 2, 5), (13, 3, 5), (12, 5, 3), (11, 2, 4)):
        s.set(x, y, ("E", lvl))
    # eine Muschel an einer Schnur
    s.set(9, 7, ("C", 2))
    s.set(9, 8, ("C", 2))
    s.set(8, 9, ("D", 5))
    s.set(9, 9, ("D", 4))
    return s.outline(prefer=("E", "B", "A", "F", "D")).image({"A": DRIFTWOOD, "B": BLACK_CORAL, "C": DRIFTWOOD, "D": SHELL, "E": SEA_ORB,
                                                               "F": KELP})


def shepherd_crook_icon() -> Image.Image:
    """Der Hirtenstab im Inventar: schräg von unten links nach oben rechts, Sumpfeiche (am Griff Strick), oben der eiserne Haken,
    der sich nach rechts herumkrümmt, an seiner Spitze die Laterne mit Seelenfeuer."""
    s = st.Spr(16, 16)
    for k in range(10):
        x, y = 1 + k, 14 - k
        if 2 <= k <= 4:
            s.set(x, y, ("F", 4 if k % 2 else 3))                # Strick am Griff
            s.set(x - 1, y, ("F", 2))
            continue
        s.set(x, y, ("A", 4 if k % 3 else 3))
        if k > 0:
            s.set(x - 1, y, ("A", 2))
    s.set(0, 15, ("C", 3))
    s.set(1, 15, ("C", 2))
    # die Zwinge und der Haken (Eisen)
    s.set(10, 4, ("C", 5))
    s.set(9, 4, ("C", 3))
    for (x, y, lvl) in ((10, 3, 4), (11, 2, 5), (12, 1, 5), (13, 1, 4), (14, 2, 4), (14, 3, 3), (14, 4, 3), (13, 5, 2)):
        s.set(x, y, ("C", lvl))
    for (x, y, lvl) in ((11, 3, 2), (12, 2, 3), (13, 2, 2)):
        s.set(x, y, ("C", lvl))
    # die Kette und die Laterne
    s.set(13, 6, ("C", 4))
    for (x, y, lvl) in ((12, 7, 3), (13, 7, 4), (14, 7, 3)):
        s.set(x, y, ("C", lvl))
    for (x, y, lvl) in ((12, 8, 4), (13, 8, 6), (14, 8, 4), (12, 9, 3), (13, 9, 5), (14, 9, 3)):
        s.set(x, y, ("E", lvl))
    for (x, y, lvl) in ((12, 10, 2), (13, 10, 3), (14, 10, 2)):
        s.set(x, y, ("C", lvl))
    return s.outline(prefer=("E", "C", "A", "F")).image({"A": SWAMP_OAK, "C": DARK_IRON, "E": SOUL_FIRE, "F": ROPE})


def minute_hand_icon() -> Image.Image:
    """Der Minutenzeiger im Inventar: schräg wie ein Schwert, unten links der Messingknauf mit Chronit und der Griff, die
    Parierstange mit zwei Zahnrädern, die gebläute Klinge mit leuchtender Ader, kurz vor der Spitze die durchbrochene Raute."""
    s = st.Spr(16, 16)
    # Griff (F) und Knauf (C mit E)
    s.set(1, 14, ("C", 4))
    s.set(2, 14, ("C", 3))
    s.set(1, 13, ("C", 5))
    s.set(2, 13, ("E", 6))
    for k in range(3):
        s.set(3 + k, 12 - k, ("F", 4 if k % 2 else 3))
    # Parierstange (C) quer
    for (x, y, lvl) in ((3, 9, 5), (4, 10, 4), (6, 12, 3), (7, 13, 2), (5, 11, 6)):
        s.set(x, y, ("C", lvl))
    s.set(2, 8, ("C", 5))
    s.set(8, 14, ("C", 3))
    # die Klinge (B gebläut) mit Ader (E)
    for k in range(9):
        x, y = 6 + k, 9 - k
        if y < 0:
            break
        s.set(x, y, ("E", 5 if k % 2 else 4) if k < 6 else ("B", 4))
        s.set(x - 1, y, ("B", 5))
        if k < 3:
            s.set(x + 1, y + 1, ("B", 3))
    # die Raute vor der Spitze
    for (x, y, lvl) in ((12, 1, 5), (13, 2, 3), (11, 2, 4), (12, 3, 4), (14, 0, 6), (13, 1, 5)):
        s.set(x, y, ("B", lvl))
    s.set(12, 2, None)
    return s.outline(prefer=("E", "C", "B", "F")).image({"B": BLUED, "C": CLOCK_BRASS, "E": CHRONIT_GLOW, "F": DARK_LEATHER})


def tuning_fork_icon() -> Image.Image:
    """Die Stimmgabel im Inventar: schräg von unten links nach oben rechts – der Knauf mit Hallkristall, der Griff in seegrünem
    Leder, Kragen und Hals aus grün angelaufenem Kupfer, der Bügel und die beiden Zinken aus Hallkristall mit leuchtenden
    Ringen (zwischen ihnen der Spalt). Gezeichnet entlang der Diagonale: u längs der Gabel, v quer dazu."""
    s = st.Spr(16, 16)
    r2 = math.sqrt(2.0)
    for y in range(16):
        for x in range(16):
            dx, dy = x + 0.5 - 1.6, y + 0.5 - 14.4
            u = (dx - dy) / r2                      # vom Knauf zur Spitze
            v = (dx + dy) / r2                      # quer (nach unten rechts = +)
            av = abs(v)
            cell = None
            if u < -0.2 or u > 18.6:
                continue
            if u < 1.3 and av < 1.0:
                cell = ("E", 6 if v < 0 else 5)
            elif u < 6.3 and av < 0.8:
                cell = ("F", 4 if int(u * 1.2) % 2 else 3) if v < 0.2 else ("F", 2)
            elif u < 7.3 and av < 1.05:
                cell = ("C", 6 if v < 0 else 4)
            elif u < 9.0 and av < 0.62:
                cell = ("C", 5 if v < 0 else 3)
            elif 9.0 <= u < 10.3 and av < 3.3:
                cell = ("C", 5 if v < -0.5 else 4 if v < 1.0 else 3)
            elif u >= 10.3 and 2.0 < av < 3.3:
                ring = int(u) in (12, 15)
                inner = (av - 2.0) < 0.6
                cell = ("E", 5) if ring else ("B", (5 if inner else 3) + (1 if u > 17.4 else 0))
            if cell is not None:
                s.set(x, y, cell)
    return s.outline(prefer=("E", "B", "C", "F")).image({"B": ECHO_CRYSTAL, "C": OX_COPPER, "E": ECHO_GLOW, "F": SEA_LEATHER})


ICONS = {name: (lambda n=name: icon(n)) for name in ICON_ART}
ICONS["tuning_fork"] = tuning_fork_icon
ICONS["keeper_maul"] = keeper_maul_icon
ICONS["sea_witch_staff"] = lambda: sea_witch_staff_icon()
ICONS["shepherd_crook"] = lambda: shepherd_crook_icon()
ICONS["minute_hand"] = lambda: minute_hand_icon()


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
