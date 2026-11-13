#!/usr/bin/env python3
"""
Texturen der Werkzeuge und Waffen in 3D (0.88), im Vanilla-Stil: je Stück ein Atlas (32x32) für das 3D-Modell in der Hand
und eine Leuchtmaske. Die Bereiche legt tools/toolset_models.py fest (SWORD, TOOL); hier werden sie je Werkstoff
ausgemalt. Das Bild im Inventar (16x16) bleibt das bisherige.

Aufruf:  python3 tools/toolset_textures.py     schreibt textures/item/<name>_model.png und <name>_model_glow.png
"""
from __future__ import annotations

import os
import sys
from dataclasses import dataclass

from PIL import Image

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import armory_textures as at  # noqa: E402
import item_style as st  # noqa: E402
import toolset_models as models  # noqa: E402
from armory_textures import _lv, h01, paint  # noqa: E402

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
OUT = os.path.join(ROOT, "src/main/resources/assets/kingdomsatwar/textures/item")

DARK_WOOD = st.ramp("#3e2c4a", dark=0.1, light=0.66, cool=290)
ABYSS_WOOD = st.ramp("#2a2036", dark=0.07, light=0.56, cool=290)
MOONSILVER = st.ramp("#c8d0ee", dark=0.32, light=1.0, chroma_dark=1.5, cool=250)
MIDNIGHT = st.ramp("#22306a", dark=0.1, light=0.62, cool=270)
CLUB_WOOD = st.ramp("#7a5230", dark=0.13, light=0.78, cool=20)
IRON = st.ramp("#7e848e", dark=0.18, light=0.9, chroma_dark=1.3)
BATON_BLUE = st.ramp("#2c4aa0", dark=0.14, light=0.82, cool=270)
ROD_RED = st.ramp("#b8282a", dark=0.18, light=0.85, cool=350)
ROD_WHITE = st.ramp("#e8e2d6", dark=0.4, light=1.0, chroma_dark=0.8, cool=60)
SAPPHIRE_GLOW = st.fixed("#0c1e5a", "#16348a", "#2452c0", "#3c78f0", "#78a8ff", "#bcd6ff", "#f0f6ff")


@dataclass
class Mat:
    """Die Farben eines Stücks (Tonleitern als Tupel)."""
    head: tuple                     # Klinge / Kopf
    edge: tuple | None = None       # Schneide (sonst wie der Kopf)
    handle: tuple = tuple(at.OAK)
    wrap: tuple = tuple(at.LEATHER)
    fitting: tuple = tuple(at.STEEL)   # Zwingen, Knauf, Parierstange
    glow: tuple | None = None       # Leuchten (Adern, Stein)
    guard: tuple | None = None      # Parierstange (sonst wie die Zwingen)

    def __post_init__(self):
        for f in ("head", "edge", "handle", "wrap", "fitting", "glow", "guard"):
            v = getattr(self, f)
            if isinstance(v, list):
                object.__setattr__(self, f, tuple(v))


MATS: dict[str, Mat] = {
    "steel_sword": Mat(at.STEEL, handle=at.OAK, wrap=at.LEATHER, fitting=at.GOLD),
    "steel_axe": Mat(at.STEEL),
    "steel_pickaxe": Mat(at.STEEL),
    "steel_shovel": Mat(at.STEEL),
    "steel_hoe": Mat(at.STEEL),
    "nightsteel_sword": Mat(at.NIGHTSTEEL, wrap=at.DARK_LEATHER, fitting=at.NIGHTSTEEL, glow=at.CYAN),
    "nightsteel_axe": Mat(at.NIGHTSTEEL, handle=DARK_WOOD, wrap=at.DARK_LEATHER, fitting=at.NIGHTSTEEL, glow=at.CYAN),
    "nightsteel_pickaxe": Mat(at.NIGHTSTEEL, handle=DARK_WOOD, wrap=at.DARK_LEATHER, fitting=at.NIGHTSTEEL, glow=at.CYAN),
    "nightsteel_shovel": Mat(at.NIGHTSTEEL, handle=DARK_WOOD, wrap=at.DARK_LEATHER, fitting=at.NIGHTSTEEL, glow=at.CYAN),
    "nightsteel_hoe": Mat(at.NIGHTSTEEL, handle=DARK_WOOD, wrap=at.DARK_LEATHER, fitting=at.NIGHTSTEEL, glow=at.CYAN),
    "abyssite_sword": Mat(at.ABYSS, wrap=at.DARK_LEATHER, fitting=at.ABYSS, glow=at.MAGENTA),
    "abyssite_axe": Mat(at.ABYSS, handle=ABYSS_WOOD, wrap=at.DARK_LEATHER, fitting=at.ABYSS, glow=at.MAGENTA),
    "abyssite_pickaxe": Mat(at.ABYSS, handle=ABYSS_WOOD, wrap=at.DARK_LEATHER, fitting=at.ABYSS, glow=at.MAGENTA),
    "abyssite_shovel": Mat(at.ABYSS, handle=ABYSS_WOOD, wrap=at.DARK_LEATHER, fitting=at.ABYSS, glow=at.MAGENTA),
    "abyssite_hoe": Mat(at.ABYSS, handle=ABYSS_WOOD, wrap=at.DARK_LEATHER, fitting=at.ABYSS, glow=at.MAGENTA),
    "draconium_sword": Mat(at.DRACONIUM, edge=at.GOLD, wrap=at.DARK_LEATHER, fitting=at.GOLD, glow=at.EMBER),
    "draconium_axe": Mat(at.DRACONIUM, edge=at.GOLD, handle=at.CHARRED, wrap=at.DARK_LEATHER, fitting=at.GOLD, glow=at.EMBER),
    "moonsilver_blade": Mat(MOONSILVER, wrap=MIDNIGHT, fitting=MOONSILVER, glow=at.MOON),
    "troll_club": Mat(CLUB_WOOD, handle=CLUB_WOOD, wrap=at.LEATHER, fitting=IRON),
    "command_baton": Mat(BATON_BLUE, handle=BATON_BLUE, fitting=at.GOLD, glow=SAPPHIRE_GLOW),
    "surveyors_rod": Mat(ROD_RED, handle=ROD_RED, fitting=at.BRASS),
}


# =============================================================================
# Schwerter
# =============================================================================

def _sword(name: str, m: Mat) -> tuple[Image.Image, Image.Image]:
    R = models.SWORD
    img, glow = at._blank()
    edge = m.edge or m.head

    def blade(x, y, w, h):
        # zweischneidig: beide Schneiden hell geschliffen, zur Mitte hin dunkler (der Grat wirft einen Schatten)
        u = x / max(1, w - 1)
        d = min(u, 1 - u)
        if d < 0.1:
            return _lv(edge, 6 if m.edge is None else 5)
        if d < 0.26:
            return _lv(edge if m.edge else m.head, 5 if m.edge is None else 4)
        level = 4 if d < 0.4 else 3
        if h01(x, y, 61) < 0.05:
            level -= 1
        return _lv(m.head, level)
    paint(img, R["blade"], blade)
    # die Spitze liegt gedreht: gleichmäßig hell, zur Mitte etwas dunkler
    paint(img, R["tip"], lambda x, y, w, h: _lv(edge, 5 if 0 < x < w - 1 and 0 < y < h - 1 else 6))
    paint(img, R["blade_edge"], lambda x, y, w, h: _lv(edge, 5 if y % 5 else 6))
    paint(img, R["fuller"], lambda x, y, w, h: _lv(m.head, 2 if x == 0 else 3))
    guard = m.guard or m.fitting

    def guard_front(x, y, w, h):
        if y == 0:
            return _lv(guard, 6)
        if y == h - 1:
            return _lv(guard, 2)
        cx = (w - 1) / 2
        if abs(x - cx) <= 0.5 and m.glow is not None:
            return _lv(guard, 1)                                           # die Fassung des Steins
        return _lv(guard, 5 if x % 4 == 0 else 4)
    paint(img, R["guard"], guard_front)
    paint(img, R["guard_side"], at.metal(guard, 3.5))
    paint(img, R["quillon"], at.strand(guard, 6.0, 3.0, True, 5))
    paint(img, R["quillon_edge"], at.strand(guard, 4.5, 2.5, False))
    paint(img, R["grip"], at.wrap(m.wrap))
    paint(img, R["metal"], at.metal(m.fitting))
    paint(img, R["metal_top"], at.top(m.fitting))
    if m.glow is not None:
        paint(glow, R["gem"], at.glow_fill(m.glow))
        paint(glow, R["glow"], lambda x, y, w, h: _lv(m.glow, 6 if y % 6 == 0 else (5 if y % 3 == 0 else 4)))
        paint(glow, R["glow_edge"], at.glow_strand(m.glow))
    else:
        paint(img, R["gem"], at.metal(m.fitting))
    return img, glow


# =============================================================================
# Werkzeuge
# =============================================================================

def _tool(name: str, m: Mat) -> tuple[Image.Image, Image.Image]:
    R = models.TOOL
    img, glow = at._blank()
    kind = name.rsplit("_", 1)[-1]
    edge = m.edge or m.head
    paint(img, R["handle"], at.wood(m.handle, 3 + len(name)))
    paint(img, R["handle_side"], at.wood(m.handle, 7 + len(name)))
    paint(img, R["wrap"], at.wrap(m.wrap))
    paint(img, R["metal"], at.metal(m.fitting))
    paint(img, R["metal_top"], at.top(m.fitting))

    def head(x, y, w, h):
        u = x / max(1, w - 1)
        v = y / max(1, h - 1)
        if kind == "axe":
            # die Schneide links; zum Auge hin dunkler, zwei Nieten am Auge
            if (x, y) in ((w - 2, 2), (w - 2, h - 3)):
                return _lv(m.fitting, 6)
            level = 6 if u < 0.08 else 5 if u < 0.24 else 4 if u < 0.62 else 3
        elif kind == "shovel":
            # gewölbtes Blatt: oben die Schneide hell, in der Mitte ein Glanz, unten zur Tülle hin dunkler
            level = 6 if v < 0.08 else 5 if v < 0.3 and abs(u - 0.4) < 0.3 else 4 if v < 0.75 else 3
            if abs(u - 0.5) < 0.08 and v > 0.25:
                level -= 1                                                 # der Grat in der Mitte
        elif kind == "hoe":
            # das Blatt: unten die Schneide hell, oben zum Arm hin dunkler
            level = 6 if v > 0.9 else 5 if v > 0.7 else 4 if v > 0.3 else 3
            if u < 0.15:
                level += 1
        else:
            level = 4
        if h01(x, y, 71 + len(name)) < 0.05 and level < 6:
            level -= 1
        return _lv(m.head, level)
    paint(img, R["head"], head)
    paint(img, R["head_side"], at.metal(m.head, 3.5))
    paint(img, R["edge"], lambda x, y, w, h: _lv(edge, 6 if x == 0 else 5))
    paint(img, R["edge_side"], lambda x, y, w, h: _lv(edge, 4))
    paint(img, R["pick"], at.strand(m.head, 6.0, 3.0, True, 31 + len(name)))
    paint(img, R["pick_edge"], at.strand(m.head, 4.5, 2.5, False))
    paint(img, R["socket"], at.metal(m.fitting if kind in ("baton", "rod") else m.head, 3.5))
    paint(img, R["socket_top"], at.top(m.head))
    paint(img, R["spike"], at.strand(m.fitting, 6.0, 2.5, True, 41))
    paint(img, R["spike_edge"], at.strand(m.fitting, 4.5, 2.0, False))
    if m.glow is not None:
        paint(glow, R["gem"], at.glow_fill(m.glow))
        paint(glow, R["glow"], at.glow_strand(m.glow))
        paint(glow, R["glow_edge"], at.glow_strand(m.glow))
    return img, glow


def _club(name: str, m: Mat) -> tuple[Image.Image, Image.Image]:
    """Die Trollkeule: knorriges Holz mit Astlöchern, Wicklung, Eisen."""
    img, glow = _tool(name, m)
    R = models.TOOL

    def wood(x, y, w, h):
        level = 4 if x % 3 else 3
        if x == 0:
            level = 5
        if h01(x // 2, y // 3, 5) < 0.12:
            level = 2                                                      # Astlöcher und Kerben
        if h01(x, y, 9) < 0.08:
            level -= 1
        return _lv(m.head, level)
    paint(img, R["head"], wood)
    paint(img, R["head_side"], wood)
    paint(img, R["spike"], at.strand(IRON, 6.0, 2.0, True, 43))
    paint(img, R["spike_edge"], at.strand(IRON, 4.0, 2.0, False))
    return img, glow


def _baton(name: str, m: Mat) -> tuple[Image.Image, Image.Image]:
    """Kommandostab: blau mit feinen goldenen Spirallinien."""
    img, glow = _tool(name, m)
    R = models.ROD

    def band(x, y, w, h):
        if (y + x) % 7 == 0:
            return _lv(at.GOLD, 5)
        return _lv(m.head, 5 if x == 0 else 3 if x == w - 1 else 4)
    paint(img, R["band"], band)
    return img, glow


def _rod(name: str, m: Mat) -> tuple[Image.Image, Image.Image]:
    """Messlatte: abwechselnd rote und weiße Felder, je vier Pixel, mit feinem Rand."""
    img, glow = _tool(name, m)
    R = models.ROD

    def band(x, y, w, h):
        ramp = ROD_RED if (y // 4) % 2 == 0 else ROD_WHITE
        level = 5 if x == 0 else 3 if x == w - 1 else 4
        if y % 4 == 3:
            level -= 1
        return _lv(ramp, level)
    paint(img, R["band"], band)
    return img, glow


def atlas(name: str) -> tuple[Image.Image, Image.Image]:
    m = MATS[name]
    if name.endswith("_sword") or name == "moonsilver_blade":
        return _sword(name, m)
    if name == "troll_club":
        return _club(name, m)
    if name == "command_baton":
        return _baton(name, m)
    if name == "surveyors_rod":
        return _rod(name, m)
    return _tool(name, m)


def build(put) -> None:
    """Für tools/generate_textures.py: die Atlanten aller Stücke."""
    for const, (name, _fn) in models.ITEMS.items():
        img, glow = atlas(name)
        put("item/" + name + "_model", img)
        put("item/" + name + "_model_glow", glow)


def main() -> None:
    for const, (name, _fn) in models.ITEMS.items():
        img, glow = atlas(name)
        img.save(os.path.join(OUT, name + "_model.png"))
        glow.save(os.path.join(OUT, name + "_model_glow.png"))
    print(f"{len(models.ITEMS)} Atlanten geschrieben")


if __name__ == "__main__":
    main()
