#!/usr/bin/env python3
"""
Besondere Gegenstände im Vanilla-Stil (siehe tools/item_style.py): die Chronik des Siegels.

Die Chronik ist ein dicker, in rotbraunes Leder gebundener Band: Rücken mit Goldbünden links, Seitenschnitt rechts
und unten, Goldbeschläge an den Ecken, auf dem Deckel das goldene Siegel mit Edelstein, unten hängt das Lesebändchen.
"""
from __future__ import annotations

from typing import Callable

from PIL import Image

import item_style as st

LEATHER = st.ramp("#7a2420", dark=0.12, light=0.7, cool=350, warm=40, hue_light=20)
GOLD = st.ramp("#d6a838", dark=0.3, light=0.97, cool=30, hue_dark=25, warm=100)
PAGES = st.ramp("#e6dcc2", dark=0.4, light=0.99, chroma_dark=1.6, cool=60)
RIBBON = st.ramp("#d02a3a", dark=0.2, light=0.95, chroma_light=0.6)
GEM = st.ramp("#3fb8e0", dark=0.3, light=0.98)

# A = Leder, B = Gold, C = Seiten, D = Lesebändchen, G = Edelstein
CHRONICLE = """
................
................
..2555555555fe..
..34444444443m..
..3444fffe443l..
..e44f3333e43m..
..d4f33US3d43l..
..34f3UTS3d33m..
..34e3333d433l..
..344edddc433m..
..d4444444333l..
..c4433333332m..
..23333333322l..
...lmlmlmlmlmm..
.......p.....3..
.......q........
"""


def build(put: Callable[[str, Image.Image], None]) -> None:
    book = st.Spr.art(CHRONICLE).outline(prefer=("A", "B", "C", "D", "G"))
    put("item/chronicle", book.image({"A": LEATHER, "B": GOLD, "C": PAGES, "D": RIBBON, "G": GEM}))


if __name__ == "__main__":
    import sys

    sheet: dict[str, Image.Image] = {}
    build(lambda key, img: sheet.__setitem__(key.split("/")[-1], img))
    st.preview(sheet, sys.argv[1] if len(sys.argv) > 1 else "items_misc.png", zoom=10, cols=4)
