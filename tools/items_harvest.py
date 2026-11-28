#!/usr/bin/env python3
"""
Die Feldfrüchte der Welten (seit 2.0) im Vanilla-Stil (siehe tools/item_style.py): Reis, Milchreis und Sushi aus der
Oberwelt, Meertrauben und Meertraubensalat aus dem Urmeer, Schwebebeeren und Schwebebeerentörtchen aus dem End,
Schattenknollen (roh, geröstet, als Eintopf) aus der Schattenwelt, Sternfrucht und Sternfruchtsaft aus der Leere, Chaosbohnen
und ihr Eintopf aus dem Abgrund, Feenbeeren und Feenpfannkuchen aus dem Feenreich – dazu die Pflanzen in ihren Stufen:
die vier Sträucher, die Meertrauben unter Wasser, die Schattenknollen auf dem Acker und der Reis im seichten Wasser (die
beiden letzten Stufen doppelt hoch, mit eigenem oberen Bild).

Gegenstände: gewölbte Formen über Masken (st.shaded), Zeichnung darüber, Umriss in der dunkelsten Stufe des Materials.
Pflanzen: wie die Pflanzen von Minecraft ohne Umriss, mit festen Tonleitern und gesätem Zufall (immer gleich).
"""
from __future__ import annotations

import math
import random
from typing import Callable

from PIL import Image

import item_style as st
from items_cuisine import BOWL, HERB, FLOUR, JAR, CORK, bowl, render

# =============================================================================
# Tonleitern der Gegenstände
# =============================================================================

GRAIN = st.ramp("#f1e8cc", dark=0.5, light=1.0, cool=70, warm=95, chroma_light=0.3)
STRAW = st.ramp("#c9a447", dark=0.3, light=0.92, cool=60, warm=95, chroma_light=0.55)
RICE_LEAF = st.ramp("#6aa040", dark=0.24, light=0.9, cool=160, warm=105, chroma_light=0.6)
NORI = st.ramp("#2c3d28", dark=0.12, light=0.62, cool=170, warm=110)
SALMON = st.ramp("#f08850", dark=0.36, light=0.96, cool=20, hue_dark=12, warm=70, chroma_light=0.55)
CUCUMBER = st.ramp("#58b048", dark=0.3, light=0.92, cool=160, warm=110)
PUDDING = st.ramp("#f2ead6", dark=0.52, light=1.0, cool=70, warm=90, chroma_light=0.3)
CINNAMON = st.ramp("#8a4a22", dark=0.22, light=0.8, cool=20, warm=60)
SEA_GRAPE = st.ramp("#3fae4a", dark=0.2, light=0.9, cool=170, warm=115, chroma_light=0.7)
SEA_STEM = st.ramp("#2f6a3c", dark=0.16, light=0.78, cool=180, warm=120)
KELP_LEAF = st.ramp("#4a6a2a", dark=0.16, light=0.76, cool=160, warm=100)
FISH_WHITE = st.ramp("#f2e2d4", dark=0.5, light=1.0, cool=30, warm=60, chroma_light=0.3)
FLOAT = st.ramp("#c4a6ee", dark=0.32, light=1.0, cool=280, warm=310, chroma_light=0.35)
FLOAT_LEAF = st.ramp("#8a8aa0", dark=0.3, light=0.9, cool=270, warm=200)
SPARK = st.fixed("#b8c8ff", "#c8d4ff", "#d8e0ff", "#e6ecff", "#f2f5ff", "#fafbff", "#ffffff")
TART = st.ramp("#d8a050", dark=0.3, light=0.96, cool=40, hue_dark=12, warm=85, chroma_light=0.55)
TUBER = st.ramp("#6e5c86", dark=0.18, light=0.86, cool=285, warm=320, chroma_light=0.6)
TUBER_SPROUT = st.ramp("#c8b4e8", dark=0.4, light=0.99, cool=280, warm=300)
ROAST = st.ramp("#6a4250", dark=0.16, light=0.82, cool=300, hue_dark=10, warm=30, chroma_light=0.7)
FLESH = st.ramp("#eadcb8", dark=0.46, light=1.0, cool=290, warm=80, chroma_light=0.4)
BUTTER = st.ramp("#f4d050", dark=0.42, light=1.0, cool=60, warm=95)
STEW_BROTH = st.ramp("#7a4a3a", dark=0.18, light=0.8, cool=330, warm=40)
MEAT = st.ramp("#9a5a3a", dark=0.22, light=0.84, cool=10, warm=50)
STARFRUIT = st.ramp("#f2cf3c", dark=0.34, light=0.99, cool=55, hue_dark=10, warm=100, chroma_light=0.45)
STAR_TIP = st.ramp("#8ab43c", dark=0.24, light=0.9, cool=150, warm=100)
STAR_GLOW = st.fixed("#ffe080", "#ffe898", "#fff0b0", "#fff6c8", "#fffadc", "#fffdee", "#ffffff")
JUICE = st.ramp("#f6b830", dark=0.36, light=0.98, cool=50, hue_dark=10, warm=90, chroma_light=0.5)
BEAN_RED = st.ramp("#c8343a", dark=0.2, light=0.92, cool=340, warm=20, chroma_light=0.6)
BEAN_VIOLET = st.ramp("#8a46c8", dark=0.2, light=0.94, cool=290, warm=310, chroma_light=0.5)
BEAN_GREEN = st.ramp("#48a860", dark=0.22, light=0.94, cool=170, warm=110, chroma_light=0.55)
BEAN_GOLD = st.ramp("#e0a82c", dark=0.3, light=0.98, cool=50, warm=95)
CHAOS_SAUCE = st.ramp("#8a2a3a", dark=0.16, light=0.78, cool=320, warm=20)
FAE_BERRY = st.ramp("#f05aa6", dark=0.26, light=0.98, cool=320, hue_dark=12, warm=350, chroma_light=0.45)
FAE_LEAF = st.ramp("#5aae5c", dark=0.24, light=0.92, cool=170, warm=110, chroma_light=0.6)
FAE_SPARK = st.fixed("#a8c8ff", "#bcd6ff", "#d0e2ff", "#e2eeff", "#f0f6ff", "#f9fbff", "#ffffff")
PANCAKE = st.ramp("#e2aa56", dark=0.3, light=0.97, cool=40, hue_dark=12, warm=85, chroma_light=0.55)
SYRUP = st.ramp("#c4701c", dark=0.24, light=0.9, cool=30, hue_dark=10, warm=70, chroma_light=0.6)
PLATE = st.ramp("#d8dce4", dark=0.46, light=1.0, cool=240, warm=200, chroma_light=0.3)


def _art(text: str, legend: dict[str, st.Px] | None = None) -> st.Spr:
    return st.Spr.art(text, legend=legend)


def _ball(s: st.Spr, cx: float, cy: float, r: float, slot: str, cuts=(-0.15, 0.3, 0.7, 0.92)) -> None:
    """Eine gewölbte Kugel (Beere) über das Sprite legen."""
    ball = st.shaded(st.ellipse(cx, cy, r, r), slot, cuts=cuts, radius=max(1.2, r))
    s.over(ball)


# =============================================================================
# Oberwelt: Reis, Milchreis, Sushi
# =============================================================================

def rice() -> st.Spr:
    """Eine Reisrispe: der Halm steigt links auf, oben neigt sich die Rispe über und hängt rechts herab, voller Körner; ein Blatt."""
    return _art("""
................
........3455....
.......4....5...
......4...ab5b..
......4..ab.5ab.
.....4....ba5ba.
.....4....ab5ab.
....4......b5ba.
....4.....ab5b..
...4.......a5ab.
...4..ll....ba..
..34.ll.........
..3.ll..........
.33ll...........
.3l.............
.3..............
""", legend={"a": ("B", 4), "b": ("B", 6), "l": ("C", 4)})


def rice_pudding() -> st.Spr:
    def fill(s: st.Spr) -> None:
        s.draw("""
................
................
................
................
................
................
.......55.......
.....566665.....
...4566666654...
..456666666654..
""")
        for x, y in ((6, 7), (9, 8), (5, 9), (11, 9), (8, 9)):
            s.set(x, y, ("B", 3))
        for x, y in ((7, 8), (10, 9)):
            s.set(x, y, ("B", 4))
    return bowl(fill)


def sushi() -> st.Spr:
    """Zwei Maki-Röllchen, schräg von oben: Seetang außen, Reis, in der Mitte Lachs und Gurke."""
    s = st.Spr()
    for cx, cy, depth in ((5.6, 5.4, 3), (10.6, 9.8, 3)):
        rx, ry = 3.6, 2.9

        def top(x: float, y: float) -> float:
            return ((x - cx) / rx) ** 2 + ((y - cy) / ry) ** 2

        for x in range(16):
            for y in range(16):
                xc, yc = x + 0.5, y + 0.5
                v = top(xc, yc)
                if v <= 1.0:
                    if v > 0.62:
                        s.set(x, y, ("A", 3 if yc < cy else 2))
                    elif v > 0.2:
                        s.set(x, y, ("B", 6 if xc < cx and yc < cy else 5))
                    else:
                        s.set(x, y, ("C", 5 if xc < cx else 4))
                else:
                    for t in range(1, depth + 1):
                        if top(xc, yc - t) <= 1.0 and yc - t > cy:
                            s.set(x, y, ("A", 2 if t < depth else 1))
                            break
        # ein Streifen Gurke neben dem Lachs
        s.set(int(cx + 0.6), int(cy - 0.4), ("D", 5))
    return s


# =============================================================================
# Urmeer: Meertrauben und Meertraubensalat
# =============================================================================

def sea_grapes() -> st.Spr:
    """Ein Zweig Meertrauben: ein Stängel, dicht mit grünen Perlen besetzt, und ein kürzerer Seitenzweig."""
    s = _art("""
................
.......56.......
......5645......
......4656......
.....564565.....
.....456465.....
....5645654.....
....465b456.....
.....56b65..56..
.....45b54.5646.
......5b6..4656.
.......b...565..
.......b..5b4...
.......bb.b.....
........bbb.....
........b.......
""", legend={"b": ("B", 3)})
    # Glanzpunkte auf einzelnen Perlen
    for x, y in ((7, 1), (6, 4), (5, 6), (12, 9), (8, 5)):
        if s.get(x, y):
            s.set(x, y, ("A", 6))
    return s


def sea_grape_salad() -> st.Spr:
    def fill(s: st.Spr) -> None:
        s.draw("""
................
................
................
................
................
......5656......
....56465645....
...4565646564...
..456564656465..
.45646565646564.
""")
        # Tangstreifen (B) und Fischflocken (C)
        for x, y in ((4, 7), (5, 8), (10, 6), (11, 7), (12, 8)):
            s.set(x, y, ("B", 3))
        for x, y in ((7, 7), (8, 8), (13, 9), (3, 9)):
            s.set(x, y, ("C", 5))
        s.set(8, 7, ("C", 6))
    return bowl(fill)


# =============================================================================
# End: Schwebebeeren und Törtchen
# =============================================================================

def float_berries() -> st.Spr:
    """Drei blasslila Beeren – zwei liegen, die dritte schwebt ein Stück darüber; ein Funkeln, ein graues Blättchen."""
    s = st.Spr()
    _ball(s, 4.6, 11.2, 2.7, "A")
    _ball(s, 11.0, 11.8, 2.6, "A")
    _ball(s, 9.0, 4.4, 2.6, "A")
    # Kelchpunkte
    for x, y in ((5, 8), (10, 9), (9, 2)):
        s.set(x, y, ("A", 2))
    # das Blättchen (B) an der unteren Beere
    for x, y, l in ((2, 7, 4), (3, 7, 5), (3, 8, 4), (1, 6, 3)):
        s.set(x, y, ("B", l))
    # Funkeln unter der schwebenden Beere (C)
    for x, y in ((8, 8), (13, 3), (6, 2)):
        s.set(x, y, ("C", 6))
    s.set(13, 2, ("C", 4))
    s.set(13, 4, ("C", 4))
    s.set(12, 3, ("C", 4))
    s.set(14, 3, ("C", 4))
    return s


def float_berry_tart() -> st.Spr:
    """Ein rundes Törtchen: goldener Mürbeteigrand mit Wellen, darin lila Beeren, einzelne funkeln."""
    cx, cy, rx, ry, depth = 8.0, 7.6, 6.6, 3.6, 3

    def top(x: float, y: float) -> bool:
        return ((x - cx) / rx) ** 2 + ((y - cy) / ry) ** 2 <= 1.0

    s = st.Spr()
    for x in range(16):
        for y in range(16):
            xc, yc = x + 0.5, y + 0.5
            if top(xc, yc):
                rim = not all(top(xc + dx, yc + dy) for dx, dy in ((1.3, 0), (-1.3, 0), (0, 1.0), (0, -1.0)))
                if rim:
                    s.set(x, y, ("A", 6 if yc < cy and xc < cx else 5 if yc < cy else 4))
                else:
                    s.set(x, y, ("B", 3))
            else:
                for t in range(1, depth + 1):
                    if top(xc, yc - t) and yc - t > cy:
                        # gewellter Rand: abwechselnd heller und dunkler
                        s.set(x, y, ("A", (4 if x % 2 else 3) if t == 1 else 3 if x < 11 else 2))
                        break
    # die Beeren: kleine Kugeln (oben links hell, unten rechts dunkel)
    for bx, by in ((4, 6), (7, 5), (10, 5), (6, 8), (9, 8), (12, 7), (3, 8)):
        for dx, dy, l in ((0, 0, 6), (1, 0, 5), (0, 1, 5), (1, 1, 4)):
            if s.get(bx + dx, by + dy) and s.get(bx + dx, by + dy)[0] == "B":
                s.set(bx + dx, by + dy, ("B", l))
    s.set(8, 4, ("C", 6))
    return s


# =============================================================================
# Schattenwelt: Schattenknolle, geröstet, Eintopf
# =============================================================================

def _tuber_mask() -> set[tuple[int, int]]:
    return st.polygon([(3.0, 7.4), (5.6, 4.4), (9.4, 3.6), (12.6, 5.0), (13.6, 8.2), (12.4, 11.6), (8.8, 13.4), (5.0, 13.0), (2.6, 10.6)])


def shadow_tuber() -> st.Spr:
    s = st.shaded(_tuber_mask(), "A", cuts=(-0.15, 0.3, 0.66, 0.9), grain=0.08, seed=11, radius=3.2)
    # Augen (dunkle Pünktchen) und ein blasser Keim
    for x, y in ((6, 6), (10, 9), (5, 10), (11, 5)):
        if s.get(x, y):
            s.set(x, y, ("A", 2))
    for x, y, l in ((11, 4, 5), (12, 3, 6), (12, 2, 5)):
        s.set(x, y, ("B", l))
    return s


def roasted_shadow_tuber() -> st.Spr:
    s = st.shaded(_tuber_mask(), "A", cuts=(-0.15, 0.3, 0.66, 0.9), grain=0.1, seed=12, radius=3.2)
    # oben kreuzweise aufgeschnitten: helles Fleisch, darin ein Stück Butter
    for x, y, l in ((5, 5, 5), (6, 5, 6), (7, 6, 6), (8, 7, 5), (9, 8, 4), (10, 9, 4), (10, 5, 5), (9, 6, 6), (7, 8, 5), (6, 9, 4),
                    (6, 6, 5), (9, 7, 5), (8, 5, 6), (7, 7, 6), (8, 8, 4)):
        if s.get(x, y):
            s.set(x, y, ("B", l))
    for x, y, l in ((7, 6, 6), (8, 6, 6), (7, 7, 5), (8, 7, 5)):
        s.set(x, y, ("C", l))
    # Röstspuren
    for x, y in ((4, 9), (11, 10), (6, 11), (12, 7)):
        if s.get(x, y):
            s.set(x, y, ("A", 1))
    return s


def shadow_tuber_stew() -> st.Spr:
    def fill(s: st.Spr) -> None:
        s.draw("""
................
................
................
................
................
................
................
.....4454454....
..344544545443..
.34454454454543.
""")
        for x, y, l in ((5, 8, 5), (6, 8, 4), (9, 9, 5), (12, 8, 5), (3, 9, 4)):
            s.set(x, y, ("B", l))
        for x, y in ((8, 8), (11, 9), (6, 9)):
            s.set(x, y, ("C", 4))
        for x, y in ((7, 7), (10, 8), (13, 9)):
            s.set(x, y, ("E", 4))
    return bowl(fill)


# =============================================================================
# Leere: Sternfrucht und Saft
# =============================================================================

def starfruit() -> st.Spr:
    """
    Eine Sternfrucht: oben rechts die ganze Frucht von der Seite (länglich, schräg, mit einer Rippe als Grat und grünen
    Spitzen), unten links eine Scheibe – ein fünfzackiger Stern mit grünlichem Rand und hellem Fruchtfleisch.
    """
    s = st.Spr()
    ax, ay, bx, by = 8.0, 8.6, 14.6, 1.6
    length = math.hypot(bx - ax, by - ay)
    ux, uy = (bx - ax) / length, (by - ay) / length
    nx, ny = uy, -ux                       # quer, nach oben links
    for x in range(16):
        for y in range(16):
            px, py = x + 0.5 - ax, y + 0.5 - ay
            t = (px * ux + py * uy) / length
            d = px * nx + py * ny
            if not 0.0 <= t <= 1.0:
                continue
            half = 2.6 * math.sin(math.pi * t) ** 0.55
            if abs(d) > half or half < 0.5:
                continue
            r = d / half
            if t < 0.08 or t > 0.93:
                s.set(x, y, ("B", 4 if t > 0.5 else 3))
            elif abs(r - 0.05) < 0.2:
                s.set(x, y, ("A", 6))           # der Grat im Licht
            elif r > 0.05:
                s.set(x, y, ("A", 5))
            else:
                s.set(x, y, ("A", 3 if r < -0.6 else 4))
    s.set(15, 0, ("B", 3))
    # die Scheibe: ein Stern (Rand und Fruchtfleisch), die Kerne in der Mitte
    cx, cy = 4.4, 11.6
    star = []
    for k in range(10):
        a = -math.pi / 2 + k * math.pi / 5
        rr = 4.0 if k % 2 == 0 else 1.9
        star.append((cx + rr * math.cos(a), cy + rr * math.sin(a)))
    mask = st.polygon(star)
    for x, y in mask:
        inner = all((x + dx, y + dy) in mask for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)))
        s.set(x, y, ("C", 5 if x + y < 16 else 4) if inner else ("D", 5 if x + y < 16 else 4))
    s.set(int(cx), int(cy), ("D", 2))
    return s


def starfruit_juice() -> st.Spr:
    body = st.ellipse(8.0, 10.6, 5.2, 4.8)
    s = st.shaded(body, "A", cuts=(-0.1, 0.35, 0.7, 0.9), radius=3.0)
    s.draw("""
................
.......kk.......
.......jj.......
.......66.......
......5665......
""", legend={"k": ("C", 5), "j": ("C", 4)})
    for x, y in body:
        if y >= 8:
            lvl = s.get(x, y)[1]
            s.set(x, y, ("B", st.clamp(lvl)))
    for x in range(4, 13):
        if (x, 8) in body:
            s.set(x, 8, ("B", 6))
    # eine Scheibe Sternfrucht schwimmt im Saft (ein kleiner Stern)
    for x, y, l in ((8, 10, 6), (7, 11, 5), (8, 11, 6), (9, 11, 5), (8, 12, 4), (7, 12, 4), (9, 12, 4), (6, 11, 4), (10, 11, 4)):
        s.set(x, y, ("E", l))
    s.set(5, 7, ("A", 6))
    s.set(5, 8, ("A", 6))
    return s


# =============================================================================
# Abgrund: Chaosbohnen und Eintopf
# =============================================================================

def _bean(s: st.Spr, cx: float, cy: float, slot: str, flip: bool = False) -> None:
    """Eine Bohne (nierenförmig, liegend) mit Glanz und Nabel."""
    mask = st.ellipse(cx, cy, 3.0, 2.0)
    # die Kerbe: oben in der Mitte (oder unten) ein Pixel weg
    notch = (int(cx), int(cy - 1.6)) if not flip else (int(cx), int(cy + 1.4))
    mask.discard(notch)
    s.over(st.shaded(mask, slot, cuts=(-0.2, 0.3, 0.7, 0.92), radius=1.8))
    s.set(int(cx), int(cy) + (1 if not flip else -1), (slot, 2))


def chaos_beans() -> st.Spr:
    s = st.Spr()
    _bean(s, 5.2, 10.4, "A")
    _bean(s, 10.8, 11.2, "C", flip=True)
    _bean(s, 8.2, 6.0, "B")
    # ein Funkeln Chaos (D, golden)
    for x, y, l in ((12, 4, 6), (12, 3, 4), (12, 5, 4), (11, 4, 4), (13, 4, 4), (3, 4, 5)):
        s.set(x, y, ("D", l))
    return s


def chaos_bean_stew() -> st.Spr:
    def fill(s: st.Spr) -> None:
        s.draw("""
................
................
................
................
................
................
................
.....4454454....
..344544545443..
.34454454454543.
""")
        for x, y, slot in ((5, 8, "B"), (6, 8, "B"), (9, 9, "C"), (10, 9, "C"), (12, 8, "F"), (13, 8, "F"), (3, 9, "C"), (7, 7, "F"), (8, 7, "F"),
                           (10, 7, "B")):
            s.set(x, y, (slot, 5))
        for x, y in ((6, 9), (11, 9), (8, 8)):
            s.set(x, y, ("E", 5))
    return bowl(fill)


# =============================================================================
# Feenreich: Feenbeeren und Pfannkuchen
# =============================================================================

def fae_berries() -> st.Spr:
    s = st.Spr()
    _ball(s, 5.4, 10.6, 2.8, "A")
    _ball(s, 10.4, 11.0, 2.8, "A")
    _ball(s, 7.8, 6.6, 2.7, "A")
    _ball(s, 11.6, 6.4, 2.0, "A")
    # Blatt (B) oben links
    for x, y, l in ((3, 4, 4), (4, 4, 5), (4, 3, 5), (5, 3, 4), (2, 5, 3), (3, 5, 4), (5, 4, 3), (6, 4, 3)):
        s.set(x, y, ("B", l))
    # funkelnder Feenstaub (C)
    for x, y, l in ((13, 2, 6), (13, 1, 4), (13, 3, 4), (12, 2, 4), (14, 2, 4), (2, 13, 5), (9, 3, 5)):
        s.set(x, y, ("C", l))
    return s


def fae_pancakes() -> st.Spr:
    """Drei Pfannkuchen übereinander (jeder mit hellem Deckel und dunklerem Rand), obenauf Feenbeeren, Sirup läuft herab."""
    s = st.Spr()
    cx, rx, ry = 8.0, 6.7, 2.1
    for cy in (12.2, 9.4, 6.6):
        for x in range(16):
            for y in range(16):
                xc, yc = x + 0.5, y + 0.5
                top = ((xc - cx) / rx) ** 2 + ((yc - cy) / ry) ** 2 <= 1.0
                side = ((xc - cx) / rx) ** 2 + ((yc - 1.7 - cy) / ry) ** 2 <= 1.0 and yc > cy
                if top:
                    s.set(x, y, ("A", 6 if yc < cy - 0.6 and xc < cx else 5))
                elif side:
                    under = ((xc - cx) / rx) ** 2 + ((yc - 2.7 - cy) / ry) ** 2 > 1.0
                    s.set(x, y, ("A", 2 if under else 3 if xc > cx + 2 else 4))
    # Sirup: auf dem obersten Pfannkuchen ein Spiegel, rechts läuft er über die Ränder herab
    for x, y, l in ((6, 6, 5), (7, 6, 6), (8, 6, 6), (9, 6, 5), (10, 6, 5), (7, 7, 4), (8, 7, 5), (9, 7, 4), (10, 7, 4), (11, 7, 4), (12, 8, 4),
                    (12, 9, 3), (13, 10, 3), (13, 11, 3), (13, 12, 2)):
        s.set(x, y, ("B", l))
    # Feenbeeren obenauf (C) und ein Funkeln (D)
    for x, y, l in ((5, 4, 5), (6, 4, 6), (5, 5, 4), (6, 5, 5), (8, 4, 5), (9, 4, 6), (8, 5, 4), (9, 5, 5), (7, 3, 5), (7, 4, 4)):
        s.set(x, y, ("C", l))
    s.set(11, 3, ("D", 6))
    return s


# =============================================================================
# Die Pflanzen (Blockbilder ohne Umriss, wie die Pflanzen von Minecraft)
# =============================================================================

N = 16


def _new() -> Image.Image:
    return Image.new("RGBA", (N, N), (0, 0, 0, 0))


def _hex(v: str) -> tuple[int, int, int, int]:
    return st.hexc(v)


def bush(stage: int, seed: int, leaves: list[str], fruit: Callable[[object, int, int, int, random.Random], None] | None,
         spots: list[tuple[int, int]], airy: float = 0.1, extra: Callable[[object, int, random.Random], None] | None = None) -> Image.Image:
    """
    Ein Strauch wie die Beerensträucher von Minecraft: dichtes Laub bis zum Boden (Stufe 0 ein kleiner Schössling), ab Stufe 2
    Früchte an den Stellen {@code spots} (halbreif die ersten vier). {@code leaves}: Laubtöne dunkel → hell; {@code fruit}
    zeichnet eine Frucht (px, x, y, Stufe, Zufall); {@code airy}: Anteil der Lücken am Rand.
    """
    rnd = random.Random(seed * 10 + stage)
    img = _new()
    px = img.load()
    tones = [_hex(c) for c in leaves]
    top = (11, 6, 3, 2)[stage]
    half = (3.2, 5.6, 6.8, 7.4)[stage]
    for y in range(top, N):
        for x in range(N):
            v = (y - top) / max(1, N - 1 - top)
            width = half * (0.55 + 0.45 * min(1.0, v * 1.8))
            edge = width + rnd.uniform(-1.2, 0.8)
            if abs(x - 7.5) > edge:
                continue
            if rnd.random() < airy and abs(x - 7.5) > width - 2:
                continue
            shade = 0.25 + 0.55 * (1.0 - v) + rnd.uniform(-0.28, 0.28)
            if abs(x - 7.5) > width - 1.5:
                shade -= 0.18
            px[x, y] = tones[max(0, min(len(tones) - 1, int(shade * len(tones))))]
    for _ in range(4 + stage * 2):
        x = rnd.randrange(1, N - 1)
        y = rnd.randrange(max(0, top - 2), N - 3)
        if px[x, y][3] == 0 and any(0 <= x + dx < N and px[x + dx, y + 1][3] for dx in (-1, 0, 1)):
            px[x, y] = tones[rnd.randrange(2, len(tones) - 1)]
    if extra:
        extra(px, stage, rnd)
    if stage >= 2 and fruit:
        for bx, by in spots[: 4 if stage == 2 else len(spots)]:
            by = max(by, top + 1)
            if by + 1 < N:
                fruit(px, bx, by, stage, rnd)
    return img


def _put(px, x: int, y: int, c) -> None:
    if 0 <= x < N and 0 <= y < N:
        px[x, y] = c


# --- Schwebebeere (End): blasses, graulila Laub, lila leuchtende Beeren, reif schweben einzelne über dem Strauch ---

FLOAT_LEAVES = ["#2e2838", "#3e3650", "#4f4666", "#62587c", "#776c92", "#8e84a8"]
FLOAT_UNRIPE = (_hex("#5c5a8a"), _hex("#7a78a8"), _hex("#a09ec8"))
FLOAT_RIPE = (_hex("#9a78d8"), _hex("#c8a8ff"), _hex("#f2e8ff"))


def _float_fruit(px, x, y, stage, rnd) -> None:
    c = FLOAT_RIPE if stage == 3 else FLOAT_UNRIPE
    _put(px, x, y, c[1])
    _put(px, x + 1, y, c[2])
    _put(px, x, y + 1, c[0])
    _put(px, x + 1, y + 1, c[1])


def float_berry_bush(stage: int) -> Image.Image:
    img = bush(stage, 71, FLOAT_LEAVES, _float_fruit, [(4, 7), (10, 6), (7, 10), (2, 11), (12, 11), (8, 4), (5, 13)], airy=0.16)
    if stage == 3:
        # zwei reife Beeren schweben über dem Strauch, mit einem Schimmer darunter
        px = img.load()
        for x, y in ((3, 1), (11, 0)):
            for dx, dy, i in ((0, 0, 1), (1, 0, 2), (0, 1, 0), (1, 1, 1)):
                _put(px, x + dx, y + dy, FLOAT_RIPE[i])
            _put(px, x, y + 3, _hex("#b8a0e8"))
    return img


# --- Sternfrucht (Leere): tiefblaues Laub mit hellen Adern, gelbe Sternfrüchte (Kreuzform), reif leuchtend ---

STAR_LEAVES = ["#0e1630", "#152244", "#1d3058", "#28426e", "#355a86", "#4a76a0"]
STAR_UNRIPE = (_hex("#5a8a4a"), _hex("#8ab85a"), _hex("#c0e080"))
STAR_RIPE = (_hex("#e0a020"), _hex("#ffd84a"), _hex("#fffbe0"))


def _star_fruit(px, x, y, stage, rnd) -> None:
    c = STAR_RIPE if stage == 3 else STAR_UNRIPE
    _put(px, x, y - 1, c[1])
    _put(px, x - 1, y, c[1])
    _put(px, x, y, c[2])
    _put(px, x + 1, y, c[1])
    _put(px, x, y + 1, c[0])


def _star_veins(px, stage, rnd) -> None:
    # einzelne funkelnde Blätter (Sternenlicht im Laub)
    for _ in range(stage + 1):
        x, y = rnd.randrange(3, 13), rnd.randrange(6, 15)
        if px[x, y][3]:
            px[x, y] = _hex("#7ec8e8")


def starfruit_bush(stage: int) -> Image.Image:
    return bush(stage, 83, STAR_LEAVES, _star_fruit, [(5, 7), (11, 6), (8, 11), (3, 12), (12, 12), (8, 4), (6, 14)], airy=0.12, extra=_star_veins)


# --- Chaosbohne (Abgrund): dunkles Laub mit Flecken in fremden Farben, reif lange Hülsen in Rot, Violett, Grün und Gold ---

CHAOS_LEAVES = ["#182218", "#223020", "#2e4028", "#3c5030", "#4c6238", "#5e7442"]
CHAOS_PODS = [(_hex("#6a1420"), _hex("#c8343a"), _hex("#f8a0a8")), (_hex("#3c1870"), _hex("#8a46c8"), _hex("#d8b0ff")),
              (_hex("#145028"), _hex("#3ca858"), _hex("#b0f0c0")), (_hex("#7a4c0c"), _hex("#e0a82c"), _hex("#fff4b0"))]
CHAOS_UNRIPE = (_hex("#38542a"), _hex("#5a7a3a"), _hex("#8eac64"))


def _chaos_pod(px, x, y, stage, rnd) -> None:
    c = CHAOS_PODS[(x * 7 + y * 3) % 4] if stage == 3 else CHAOS_UNRIPE
    # eine hängende Hülse, zwei Pixel breit und vier lang, leicht gebogen, oben das Licht
    for dy, (left, right) in enumerate(((2, 1), (1, 1), (1, 0), (0, 0))):
        _put(px, x, y + dy, c[left])
        _put(px, x + 1, y + dy + (1 if dy == 3 else 0), c[right])


def _chaos_tint(px, stage, rnd) -> None:
    for _ in range(3 + stage * 2):
        x, y = rnd.randrange(2, 14), rnd.randrange(5, 15)
        if px[x, y][3]:
            px[x, y] = _hex(rnd.choice(("#5a2a3a", "#3a2a5a", "#6a4a1a")))


def chaos_bean_bush(stage: int) -> Image.Image:
    return bush(stage, 97, CHAOS_LEAVES, _chaos_pod, [(4, 5), (10, 4), (7, 9), (2, 10), (12, 9), (8, 2), (5, 11)], airy=0.14, extra=_chaos_tint)


# --- Feenbeere (Feenreich): frisches Grün mit rosa Blüten, rosa Beeren, reif mit hellem Funkeln ---

FAE_LEAVES = ["#173a24", "#21502e", "#2c6638", "#3a7e44", "#4e9852", "#6ab266"]
FAE_UNRIPE = (_hex("#a85a7a"), _hex("#d08aa8"), _hex("#f0c0d8"))
FAE_RIPE = (_hex("#c0306e"), _hex("#f05aa6"), _hex("#ffe0f4"))


def _fae_fruit(px, x, y, stage, rnd) -> None:
    c = FAE_RIPE if stage == 3 else FAE_UNRIPE
    _put(px, x, y, c[1])
    _put(px, x + 1, y, c[2])
    _put(px, x, y + 1, c[0])
    _put(px, x + 1, y + 1, c[1])


def _fae_blossoms(px, stage, rnd) -> None:
    for _ in range(1 + stage):
        x, y = rnd.randrange(2, 14), rnd.randrange(4, 14)
        if px[x, y][3]:
            px[x, y] = _hex("#ffd0ea")
    if stage == 3:
        for x, y in ((2, 3), (13, 2)):
            _put(px, x, y, _hex("#e0f0ff"))


def fae_berry_bush(stage: int) -> Image.Image:
    return bush(stage, 59, FAE_LEAVES, _fae_fruit, [(4, 7), (10, 6), (7, 10), (2, 11), (12, 11), (8, 4), (5, 13)], airy=0.1, extra=_fae_blossoms)


# --- Meertrauben (Urmeer): wellige Stängel, dicht mit Perlen besetzt, unten ein kriechender Ausläufer ---

GRAPE_STEM = [_hex("#173e24"), _hex("#1f5430"), _hex("#296a3a")]
GRAPE_BEAD = [_hex("#236e34"), _hex("#2f8a3e"), _hex("#45a64a"), _hex("#66c05a"), _hex("#8ed874")]
GRAPE_GLOW = [_hex("#3cc070"), _hex("#7aecb0"), _hex("#d0fff0")]


def sea_grape_plant(stage: int) -> Image.Image:
    rnd = random.Random(500 + stage)
    img = _new()
    px = img.load()
    for x in range(1, 15):
        if rnd.random() < 0.8:
            px[x, 15] = GRAPE_STEM[rnd.randrange(0, 2)]
    heights = {0: [4, 3], 1: [7, 5, 6], 2: [11, 8, 12, 9], 3: [15, 12, 14, 11, 13]}[stage]
    xs = {0: [6, 10], 1: [3, 8, 12], 2: [3, 6, 10, 13], 3: [2, 5, 8, 11, 13]}[stage]
    for x0, h in zip(xs, heights):
        phase = rnd.random() * 6.28
        for k in range(h):
            y = 14 - k
            x = x0 + int(round(0.7 * math.sin(phase + k * 0.55)))
            t = k / max(1, h - 1)
            _put(px, x, y, GRAPE_STEM[min(2, k * 3 // max(1, h))])
            if k < 1:
                continue
            # Perlen beiderseits, versetzt: wie kleine Trauben am Stängel
            for side in (-1, 1):
                if (k + (side > 0)) % 2 == 0 or k >= h - 2:
                    if stage == 3 and rnd.random() < 0.5:
                        c = GRAPE_GLOW[min(2, int(t * 2.6 + rnd.uniform(-0.4, 0.3)))]
                    else:
                        c = GRAPE_BEAD[min(4, max(0, int(1 + t * 3.4 + rnd.uniform(-0.6, 0.5))))]
                    _put(px, x + side, y, c)
        top_x = x0 + int(round(0.7 * math.sin(phase + (h - 1) * 0.55)))
        _put(px, top_x, 14 - h, GRAPE_GLOW[2] if stage == 3 else GRAPE_BEAD[4])
    return img


# --- Schattenknollen (Schattenwelt): buschiges, violettgraues Kraut wie Kartoffeln, reif mit blassen Blüten und Knollen ---

TUBER_LEAVES = [_hex("#221c30"), _hex("#30283f"), _hex("#3f3552"), _hex("#524768"), _hex("#685c80"), _hex("#80769a"), _hex("#9a90b2")]
TUBER_KNOBS = [_hex("#33283f"), _hex("#54466a"), _hex("#7a6a94")]
TUBER_FLOWER = [_hex("#a890d8"), _hex("#e8dcff")]


def shadow_tubers(stage: int) -> Image.Image:
    """Wie Kartoffeln: Stufe 0 Keimlinge, 1 kleine Büsche, 2 dichtes Kraut, 3 dazu blasse Blüten und Knollen am Grund."""
    rnd = random.Random(620 + stage)
    img = _new()
    px = img.load()
    plants = {0: [(4, 3, 1.2), (11, 4, 1.4)], 1: [(4, 7, 2.4), (11, 6, 2.2)], 2: [(3, 10, 3.0), (8, 12, 3.2), (13, 10, 2.8)],
              3: [(3, 11, 3.2), (8, 13, 3.4), (13, 11, 3.0)]}[stage]
    for cx, h, r in plants:
        # Stängel
        for k in range(h - 1):
            _put(px, cx, 15 - k, TUBER_LEAVES[1 if k < h // 2 else 2])
        # das Kraut: drei Blätterbüschel – zwei seitlich tief am Stängel, eines oben –, oben und links heller
        crown_y = 15 - h + r * 0.8
        for ox, oy, rr in ((-1.4, 1.6, 0.7), (1.5, 2.2, 0.7), (0.0, 0.0, 0.75)):
            bx, by, br = cx + ox * r * 0.6, crown_y + oy * r * 0.5, max(1.0, r * rr)
            for _ in range(int(9 * br * br)):
                ang = rnd.uniform(0, 6.283)
                dist = br * math.sqrt(rnd.random())
                x = int(round(bx + math.cos(ang) * dist * 1.2))
                y = int(round(by + math.sin(ang) * dist))
                if y > 15:
                    continue
                light = 0.5 - (y - by) / (2.0 * br) - (x - bx) / (5.0 * br) - (0.15 if oy > 0 else 0.0) + rnd.uniform(-0.18, 0.18)
                _put(px, x, y, TUBER_LEAVES[max(1, min(6, int(2 + light * 5)))])
        if stage == 3:
            for fx, fy in ((cx, 15 - h), (cx + 2, 16 - h)):
                _put(px, fx, fy, TUBER_FLOWER[1])
                _put(px, fx + 1, fy, TUBER_FLOWER[0])
    if stage == 3:
        for x0 in (1, 5, 10, 13):
            _put(px, x0, 15, TUBER_KNOBS[1])
            _put(px, x0 + 1, 15, TUBER_KNOBS[0])
            _put(px, x0, 14, TUBER_KNOBS[2])
            _put(px, x0 + 1, 14, TUBER_KNOBS[1])
    return img


# --- Reis (Oberwelt): Horste aus Halmen im Wasser, später heraus, reif goldene, nickende Rispen ---
#
# Gezeichnet auf einer 16 x 32 hohen Fläche (oben das Bild, das aus dem Wasser ragt, unten das im Wasserblock), damit die
# Halme an der Naht genau aneinanderstoßen.

RICE_GREEN = [_hex("#24481e"), _hex("#2f5e26"), _hex("#3c7630"), _hex("#4e8e3c"), _hex("#66a84a"), _hex("#84c262")]
RICE_RIPE = [_hex("#4e5a20"), _hex("#66722a"), _hex("#808c34"), _hex("#9ca23e"), _hex("#b6b44c"), _hex("#ccc462")]
RICE_GOLD = [_hex("#8a6e22"), _hex("#b08e30"), _hex("#d0ac44"), _hex("#e8c866"), _hex("#f6e4a0")]


def rice_plant(stage: int) -> tuple[Image.Image, Image.Image | None]:
    """Unteres Bild (im Wasserblock) und – ab Stufe 2 – oberes (ragt aus dem Wasser)."""
    rnd = random.Random(700 + stage)
    canvas = Image.new("RGBA", (N, 2 * N), (0, 0, 0, 0))
    cp = canvas.load()

    def put(x: int, y: int, c) -> None:
        if 0 <= x < N and 0 <= y < 2 * N:
            cp[x, y] = c

    tones = RICE_RIPE if stage == 3 else RICE_GREEN
    tall = {0: (4, 6), 1: (10, 14), 2: (22, 26), 3: (20, 23)}[stage]
    # drei Horste, je drei bis vier Halme, die sich nach oben auffächern
    for hx in (3, 8, 13):
        for i in range(3 if stage == 0 else 4):
            spread = (i - 1.5) * (0.9 if stage < 2 else 1.3) + rnd.uniform(-0.4, 0.4)
            h = rnd.randint(*tall)
            top = None
            for k in range(h):
                y = 31 - k
                f = k / max(1, h - 1)
                x = hx + int(round(spread * f * f * 2.2))
                put(x, y, tones[min(5, int(f * 5.99))] if stage < 3 or k < h - 1 else tones[5])
                top = (x, y)
            if stage == 3 and top is not None:
                # die Rispe: nickt zur Seite, Körner beiderseits, die Spitze hängt
                side = 1 if spread >= 0 else -1
                x, y = top
                path = [(x, y - 1), (x + side, y - 2), (x + 2 * side, y - 2), (x + 3 * side, y - 1), (x + 3 * side, y), (x + 4 * side, y + 1)]
                for j, (px_, py_) in enumerate(path):
                    put(px_, py_, RICE_GOLD[2])
                    put(px_, py_ + 1, RICE_GOLD[3 + (j % 2)])
                    if j % 2 == 0:
                        put(px_, py_ - 1, RICE_GOLD[4])
    # einzelne Blätter biegen sich aus dem Horst (ab Stufe 1)
    if stage >= 1:
        for hx in (3, 8, 13):
            side = rnd.choice((-1, 1))
            base = 31 - rnd.randint(4, 8) if stage >= 2 else 31 - rnd.randint(2, 4)
            for k in range(5):
                put(hx + side * (k // 2 + 1), base - k + (k * k) // 6, tones[min(5, 3 + k // 2)])
    lower = canvas.crop((0, N, N, 2 * N))
    upper = canvas.crop((0, 0, N, N)) if stage >= 2 else None
    return lower, upper


# =============================================================================
# Ausgabe
# =============================================================================

def build(put: Callable[[str, Image.Image], None]) -> None:
    put("item/rice", render(rice(), {"A": STRAW, "B": GRAIN, "C": RICE_LEAF}, prefer=("A", "B")))
    put("item/rice_pudding", render(rice_pudding(), {"A": PUDDING, "B": CINNAMON, "D": BOWL}, prefer=("D", "A")))
    put("item/sushi", render(sushi(), {"A": NORI, "B": GRAIN, "C": SALMON, "D": CUCUMBER}, prefer=("A",)))
    put("item/sea_grapes", render(sea_grapes(), {"A": SEA_GRAPE, "B": SEA_STEM}, prefer=("A", "B")))
    put("item/sea_grape_salad", render(sea_grape_salad(), {"A": SEA_GRAPE, "B": KELP_LEAF, "C": FISH_WHITE, "D": BOWL}, prefer=("D", "A")))
    put("item/float_berries", render(float_berries(), {"A": FLOAT, "B": FLOAT_LEAF, "C": SPARK}, prefer=("A", "B")))
    put("item/float_berry_tart", render(float_berry_tart(), {"A": TART, "B": FLOAT, "C": SPARK}, prefer=("A",)))
    put("item/shadow_tuber", render(shadow_tuber(), {"A": TUBER, "B": TUBER_SPROUT}, prefer=("A",)))
    put("item/roasted_shadow_tuber", render(roasted_shadow_tuber(), {"A": ROAST, "B": FLESH, "C": BUTTER}, prefer=("A",)))
    put("item/shadow_tuber_stew", render(shadow_tuber_stew(), {"A": STEW_BROTH, "B": TUBER, "C": MEAT, "D": BOWL, "E": HERB}, prefer=("D", "A")))
    put("item/starfruit", render(starfruit(), {"A": STARFRUIT, "B": STAR_TIP, "C": STAR_GLOW, "D": STARFRUIT}, prefer=("D", "A", "B")))
    put("item/starfruit_juice", render(starfruit_juice(), {"A": JAR, "B": JUICE, "C": CORK, "E": STARFRUIT}, prefer=("A",)))
    put("item/chaos_beans", render(chaos_beans(), {"A": BEAN_RED, "B": BEAN_VIOLET, "C": BEAN_GREEN, "D": BEAN_GOLD}, prefer=("A", "B", "C")))
    put("item/chaos_bean_stew", render(chaos_bean_stew(), {"A": CHAOS_SAUCE, "B": BEAN_RED, "C": BEAN_GREEN, "D": BOWL, "E": BEAN_VIOLET,
                                                           "F": BEAN_GOLD}, prefer=("D", "A")))
    put("item/fae_berries", render(fae_berries(), {"A": FAE_BERRY, "B": FAE_LEAF, "C": FAE_SPARK}, prefer=("A", "B")))
    put("item/fae_pancakes", render(fae_pancakes(), {"A": PANCAKE, "B": SYRUP, "C": FAE_BERRY, "D": FAE_SPARK}, prefer=("A",)))
    for stage in range(4):
        put(f"block/float_berry_bush_stage{stage}", float_berry_bush(stage))
        put(f"block/starfruit_bush_stage{stage}", starfruit_bush(stage))
        put(f"block/chaos_bean_bush_stage{stage}", chaos_bean_bush(stage))
        put(f"block/fae_berry_bush_stage{stage}", fae_berry_bush(stage))
        put(f"block/sea_grape_plant_stage{stage}", sea_grape_plant(stage))
        put(f"block/shadow_tubers_stage{stage}", shadow_tubers(stage))
        lower, upper = rice_plant(stage)
        put(f"block/rice_stage{stage}", lower)
        if upper is not None:
            put(f"block/rice_stage{stage}_top", upper)
