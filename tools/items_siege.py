#!/usr/bin/env python3
"""
Gegenstände der Belagerungswaffen im Vanilla-Stil (siehe tools/item_style.py): Steinkugel, Ballistenbolzen und die
Bausätze von Katapult, Tribok, Balliste und Rammbock (in Seitenansicht, nach rechts gewandt).

Materialien: A = Eichenholz (Rahmen, Arm), B = dunkles Holz (Räder, Balken), C = Eisen (Beschläge, Naben, Spitzen),
D = Seil und Sehne, E = Stein, F = rot gefärbte Federn, G = dunkles Gusseisen (Gegengewicht).
"""
from __future__ import annotations

from typing import Callable

from PIL import Image

import item_style as st

OAK = st.ramp("#9a7243", dark=0.22, light=0.86, chroma_dark=0.7)
DARK = st.ramp("#6a4a2c", dark=0.16, light=0.7, chroma_dark=0.7)
IRON = st.ramp("#a4adb7", dark=0.24, light=0.97, chroma_dark=1.5, hue_dark=10)
ROPE = st.ramp("#c8ae76", dark=0.3, light=0.93, chroma_dark=0.9, cool=40)
STONE = st.ramp("#8d8c86", dark=0.2, light=0.9, chroma_dark=0.6, cool=250, hue_dark=12)
FLETCH = st.ramp("#b4392d", dark=0.2, light=0.86, chroma_light=0.5)
CAST = st.ramp("#5a5c63", dark=0.14, light=0.72, chroma_dark=1.2)

RAMPS = {"A": OAK, "B": DARK, "C": IRON, "D": ROPE, "E": STONE, "F": FLETCH, "G": CAST}

# Katapult: Wurfarm halb ausgeholt, Steinkugel im Löffel, vorn der gepolsterte Prellbalken, zwei Räder
CATAPULT = """
................
..FGF...........
.FGFED..........
.EFEDC.....3554.
.5444443....42..
..2333321...42..
......4432..42..
.......4432.42..
........4432422.
.........44l42..
.55555555555555.
.33jkj3333jkj32.
..jdcdi..jdcdi..
..kcmci..kcmci..
..jdcdi..jdcdi..
...iji....iji...
"""

# Tribok: der lange Arm hoch erhoben mit herabhängender Schlinge und Stein, am kurzen Ende hängt das Gegengewicht
TREBUCHET = """
................
..55............
..3355..........
..p.3355........
..p...3355......
..p.....3355....
.qEq......m355..
.pDp.....43.33..
.........43.p...
........43.SSTR.
........43.RlkP.
.......43..RQQP.
.....4343..QPPP.
..555555555555..
..222222222222..
...ijki....ijki.
"""

# Balliste: ein riesiger Armbrustbogen auf einem Dreibein, der Bolzen liegt gespannt auf der Bahn
BALLISTA = """
................
.............mn.
.......dddd..ln.
......ce...ekl..
.....j.r....l...
.....k.r...45d..
.......r..453e..
.......r.453.ed.
.......r453..e..
.......453rrre..
......453.4..kj.
.....453..4.....
....453...4.....
...43.....4.....
.........434....
........3...3...
"""

# Rammbock: Schutzdach auf Pfosten, darunter der an Ketten hängende Stamm mit Eisenkopf, zwei Räder
RAM = """
................
................
.....555555.....
....54444443....
...5444444443...
..b2222222222b..
..4..k....k.4...
..4..j....j.4...
.r55555555555lm.
.s44444444444mnl
.r33333333333lm.
.55555555555553.
.33jkj3333jkj32.
..jdcdi..jdcdi..
..kcmci..kcmci..
...iji....iji...
"""

# Ballistenbolzen: breite Eisenspitze oben rechts, dicker Schaft, rote Federn unten links
BOLT = """
................
.............mn.
............lnm.
...........kll..
..........4kj...
.........453....
........453.....
.......453......
......453.......
.....453........
..JK453.........
..IJ53..........
...45JK.........
..43.IJ.........
................
................
"""


def stone_ball() -> st.Spr:
    """
    Behauene Steinkugel: eine Kugel aus ebenen Meißelflächen (jede Fläche einheitlich beleuchtet, an den Kanten etwas
    dunkler) mit einer abgeplatzten Stelle unten rechts.
    """
    cx, cy, r = 7.5, 7.5, 5.75
    mask = st.ellipse(cx, cy, r, r)
    rng = st.seeded(11)
    centers: list[tuple[float, float]] = []
    for _ in range(4000):
        if len(centers) >= 12:
            break
        x, y = rng.uniform(cx - r, cx + r), rng.uniform(cy - r, cy + r)
        if (x - cx) ** 2 + (y - cy) ** 2 <= (r - 0.6) ** 2 and all((x - a) ** 2 + (y - b) ** 2 > 6.0 for a, b in centers):
            centers.append((x, y))
    lx, ly, lz = -0.55, -0.65, 0.75
    ln = (lx * lx + ly * ly + lz * lz) ** 0.5

    def facet(px: int, py: int) -> int:
        return min(range(len(centers)), key=lambda i: (px + 0.5 - centers[i][0]) ** 2 + (py + 0.5 - centers[i][1]) ** 2)

    ball = st.Spr()
    for x, y in mask:
        i = facet(x, y)
        fx, fy = (centers[i][0] - cx) / r, (centers[i][1] - cy) / r
        fz = max(0.0, 1.0 - fx * fx - fy * fy) ** 0.5
        light = (fx * lx + fy * ly + fz * lz) / ln
        level = 2 + sum(light >= c for c in (0.2, 0.55, 0.8, 0.94))
        # Kanten zwischen den Flächen: eine Stufe dunkler (nur nach unten/rechts, damit die Kanten nicht flimmern)
        if any((x + dx, y + dy) in mask and facet(x + dx, y + dy) != i for dx, dy in ((1, 0), (0, 1))):
            level -= 1
        ball.set(x, y, ("E", st.clamp(level, 1, 6)))
    # abgeplatzte Stelle unten rechts
    for x, y, lvl in [(10, 11, 1), (11, 10, 1), (10, 10, 2)]:
        if (x, y) in mask:
            ball.set(x, y, ("E", lvl))
    return st.despeckle(ball, ("E",))


def render(art: str, prefer: tuple[str, ...] = ("B", "A", "C", "D", "E", "F", "G")) -> Image.Image:
    return st.Spr.art(art).outline(prefer=prefer).image(RAMPS)


def build(put: Callable[[str, Image.Image], None]) -> None:
    put("item/siege_stone", stone_ball().outline().image(RAMPS))
    put("item/ballista_bolt", render(BOLT, prefer=("C", "A", "F")))
    put("item/catapult", render(CATAPULT))
    put("item/trebuchet", render(TREBUCHET))
    put("item/ballista", render(BALLISTA))
    put("item/battering_ram", render(RAM))


if __name__ == "__main__":
    import sys

    sheet: dict[str, Image.Image] = {}
    build(lambda key, img: sheet.__setitem__(key.split("/")[-1], img))
    st.preview(sheet, sys.argv[1] if len(sys.argv) > 1 else "items_siege.png", zoom=12, cols=6)
