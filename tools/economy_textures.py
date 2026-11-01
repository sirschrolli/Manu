#!/usr/bin/env python3
"""
Wirtschaft: die drei Münzen (Bronze, Silber, Gold) im Vanilla-Stil (siehe tools/item_style.py).

Jede Münze ist eine einzelne Münze von vorn, mit Prägung (Bronze: Schild, Silber: Turm, Gold: Krone) – auch im Stapel
(die Anzahl steht ja daneben).

Licht von oben links: der Rand ist oben links hell, unten rechts dunkel; die Prägung steht erhaben (helle Kante oben,
Schatten darunter); die Dicke einer Münze zeigt sich als dunkles Band unter ihrer Oberseite.
"""
from __future__ import annotations

import math
from typing import Callable

from PIL import Image

import item_style as st

BRONZE = st.ramp("#b5733a", dark=0.2, light=0.95, cool=20, warm=60, hue_dark=12, chroma_light=0.55)
SILVER = st.ramp("#b9c0cc", dark=0.26, light=0.995, chroma_dark=1.6, cool=255, hue_dark=10)
GOLD = st.ramp("#e2a623", dark=0.22, light=0.98, cool=30, warm=85, hue_dark=14, hue_light=12, chroma_light=0.5)
RAMPS = {"bronze": BRONZE, "silver": SILVER, "gold": GOLD}

# Prägungen (x nach rechts, y nach unten), um die Mitte der Münze – ohne Löcher, damit sie nicht wie Gesichter wirken
EMBLEMS = {
    # Schild mit Spitze
    "bronze": """
#####
#####
.###.
..#..
""",
    # Turm mit Zinnen
    "silver": """
#.#.#
#####
.###.
.###.
""",
    # Krone mit drei Zacken
    "gold": """
#.#.#
#####
#####
""",
}


def _emblem_points(kind: str) -> list[tuple[int, int]]:
    rows = EMBLEMS[kind].strip("\n").split("\n")
    h = len(rows)
    w = max(len(r) for r in rows)
    pts = []
    for y, row in enumerate(rows):
        for x, ch in enumerate(row):
            if ch == "#":
                pts.append((x - w // 2, y - h // 2))
    return pts


def single(kind: str) -> st.Spr:
    """Eine Münze von vorn: heller Rand oben links, dunkler unten rechts, die Dicke als Sichel unten rechts."""
    s = st.Spr()
    cx, cy, r = 7.6, 7.4, 5.7
    face = st.ellipse(cx, cy, r, r)
    body = st.ellipse(cx + 0.7, cy + 0.9, r, r)
    for (x, y) in body - face:
        s.set(x, y, ("A", 1))
    for (x, y) in face:
        nx = (x + 0.5 - cx) / r
        ny = (y + 0.5 - cy) / r
        d = math.hypot(nx, ny)
        diag = (nx + ny) / math.sqrt(2)
        if d > 0.74:
            lvl = 6 if diag < -0.55 else 5 if diag < 0.0 else 3 if diag < 0.55 else 2
        else:
            lvl = 3 if diag < 0.35 else 2
        s.set(x, y, ("A", lvl))
    ox, oy = round(cx - 0.5), round(cy - 0.5)
    pts = {(ox + dx, oy + dy) for dx, dy in _emblem_points(kind)}
    for (x, y) in pts:
        s.set(x, y, ("A", 6 if (x, y - 1) not in pts else 5))
    for (x, y) in pts:
        for q in ((x, y + 1), (x + 1, y + 1)):
            if q not in pts and q in face and math.hypot((q[0] + 0.5 - cx) / r, (q[1] + 0.5 - cy) / r) <= 0.74:
                s.set(q[0], q[1], ("A", 1))
    return s


def render(spr: st.Spr, ramp: st.Ramp) -> Image.Image:
    return spr.outline().image({"A": ramp})


def build(put: Callable[[str, Image.Image], None]) -> None:
    for kind, ramp in RAMPS.items():
        put(f"item/{kind}_coin", render(single(kind), ramp))


if __name__ == "__main__":
    import sys

    sheet: dict[str, Image.Image] = {}
    build(lambda key, img: sheet.__setitem__(key.split("/")[-1], img))
    st.preview(sheet, sys.argv[1] if len(sys.argv) > 1 else "economy_items.png", zoom=10, cols=3)
