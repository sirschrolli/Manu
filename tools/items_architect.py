#!/usr/bin/env python3
"""
Gegenstände des Dragon Architect im Vanilla-Stil (siehe tools/item_style.py):

* item/surveyors_rod – der Vermessungsstab: eine schräg gehaltene Messlatte, rot-weiß gestreift wie eine echte
  Vermessungslatte, unten ein Messingschuh mit Spitze, oben eine Messingkappe mit kleinem Knauf. Licht von oben links:
  die obere linke Kante jedes Streifens glänzt, die untere rechte liegt im Schatten.
* item/blueprint_custom – ein eigener Bauplan: eine echte Blaupause (tiefblaues Papier mit feinem, hellerem Raster),
  darauf in weißen Linien die Ansicht einer kleinen Burg mit zwei Zinnentürmen und Tor; unten eingerollt, mit einem
  goldenen Siegel an rotem Band.

Materialien: A = Rot (Streifen, Siegelband), B = Weiß (Streifen, Zeichnung), C = Messing/Gold, D = Blaupause.
"""
from __future__ import annotations

from typing import Callable

from PIL import Image

import item_style as st

RED = st.ramp("#b8302a", dark=0.2, light=0.86, cool=320, warm=40, chroma_light=0.55)
WHITE = st.ramp("#dcd8cc", dark=0.42, light=1.0, cool=250, warm=90, chroma_dark=0.6)
BRASS = st.ramp("#c99a35", dark=0.26, light=0.97, cool=30, warm=95, hue_dark=20, chroma_light=0.45)
CYAN = st.ramp("#2c5ea8", dark=0.18, light=0.8, cool=270, warm=215, hue_dark=10, chroma_light=0.7)

RAMPS = {"A": RED, "B": WHITE, "C": BRASS, "D": CYAN}


def surveyors_rod() -> Image.Image:
    """Messlatte schräg von unten links nach oben rechts, zwei Bildpunkte dick, rot-weiß gestreift."""
    s = st.Spr()
    # Mittellinie: (1 + i, 14 - i); oben links liegt die helle, unten rechts die dunkle Hälfte
    length = 13
    for i in range(length):
        x, y = 1 + i, 14 - i
        if i <= 1:
            slot, hi, lo = "C", 5, 3          # Messingschuh
        elif i >= length - 2:
            slot, hi, lo = "C", 6, 4          # Messingkappe
        else:
            band = (i - 2) // 3                # je drei Bildpunkte ein Streifen
            slot = "A" if band % 2 == 0 else "B"
            hi, lo = (5, 3) if slot == "A" else (6, 4)
        s.set(x, y, (slot, hi))
        s.set(x + 1, y, (slot, lo))
        # dünne Kante oben auf jedem Streifen (Lackglanz)
    # Spitze unten (einzelner Messingpunkt) und Knauf oben
    s.set(0, 15, ("C", 2))
    s.set(14, 1, ("C", 5))
    s.set(15, 1, ("C", 3))
    s.set(14, 0, ("C", 4))
    # Glanzpunkte auf den roten Streifen
    for i in (2, 8):
        s.set(1 + i, 14 - i, ("A", 6))
    for i in (5, 11):
        s.set(1 + i, 14 - i, ("B", 6))
    return s.outline(prefer=("C", "A", "B")).image(RAMPS)


def blueprint_custom() -> Image.Image:
    """Blaupause: blaues Papier mit Raster, weiße Burgansicht, unten eingerollt, goldenes Siegel am roten Band."""
    s = st.Spr()
    # Bogen x 2..13, y 1..11, mit feinem hellerem Raster
    for y in range(1, 12):
        for x in range(2, 14):
            lvl = 3
            if (x - 2) % 3 == 0 or (y - 1) % 3 == 0:
                lvl = 4
            if x == 2 or y == 1:
                lvl = 5
            if x == 13:
                lvl = 2
            s.set(x, y, ("D", lvl))
    # Rolle unten
    for x in range(1, 15):
        s.set(x, 11, ("D", 5))
        s.set(x, 12, ("D", 4))
        s.set(x, 13, ("D", 2))
    s.set(0, 12, ("D", 3))
    s.set(15, 12, ("D", 2))
    # weiße Zeichnung: zwei Zinnentürme, Mauer mit Zinnen, Tor
    ink = [
        "W.W.W.W.W",
        "WWWWWWWWW",
        "W.......W",
        "W..W.W..W",
        "W.......W",
        "W..WWW..W",
        "W..W.W..W",
        "WWWW.WWWW",
    ]
    for dy, row in enumerate(ink):
        for dx, ch in enumerate(row):
            if ch == "W":
                s.set(3 + dx, 2 + dy, ("B", 6 if dy < 2 else 5))
    # rotes Band über die Rolle mit goldenem Siegel
    for y in (11, 12, 13):
        s.set(10, y, ("A", 4 if y == 11 else 3))
    s.set(9, 14, ("A", 3))
    s.set(11, 14, ("A", 2))
    s.set(10, 12, ("C", 5))
    s.set(11, 12, ("C", 3))
    s.set(10, 13, ("C", 4))
    s.set(11, 13, ("C", 2))
    return s.outline(prefer=("D", "C", "A")).image(RAMPS)


def build(put: Callable[[str, Image.Image], None]) -> None:
    put("item/surveyors_rod", surveyors_rod())
    put("item/blueprint_custom", blueprint_custom())


if __name__ == "__main__":
    import sys

    out = sys.argv[1] if len(sys.argv) > 1 else "."
    for name, img in (("surveyors_rod", surveyors_rod()), ("blueprint_custom", blueprint_custom())):
        img.resize((256, 256), Image.NEAREST).save(f"{out}/{name}_big.png")
