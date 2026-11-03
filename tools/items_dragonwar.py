#!/usr/bin/env python3
"""
Gegenstände für "Drachen im Krieg" im Vanilla-Stil (siehe tools/item_style.py): das Drachenhorn.

Das Drachenhorn ist ein Kriegshorn aus dem Horn eines Drachen: ein langes, sich verjüngendes, leicht geripptes Horn in
warmem, dunklem Elfenbein, das zur Spitze hin nachdunkelt und kurz vor dem Mundstück ins Drachenrot/Kohlschwarz
übergeht. Es schwingt in einem Bogen vom goldenen Mundstück unten links zum Schalltrichter oben rechts, der sich (wie
beim Ziegenhorn, nur gespiegelt) nach oben öffnet und einen breiten Goldrand trägt. Zwei Goldringe fassen das Horn ein,
am oberen hängt eine rote Kordel mit Goldperle und Quaste. Licht von oben links, die Rippen zeigen sich als Stufen im
Glanzlicht.

Materialien: A = Horn (Elfenbein), B = Gold (Mundstück, Ringe, Rand des Schalltrichters), C = dunkle Hornspitze,
D = das Innere des Schalltrichters, E = rote Kordel und Quaste.
"""
from __future__ import annotations

from typing import Callable

from PIL import Image

import item_style as st

HORN = st.ramp("#cdb68a", dark=0.24, light=0.95, chroma_dark=1.0, cool=25, hue_dark=22, warm=80)
GOLD = st.ramp("#d6a838", dark=0.3, light=0.97, cool=30, hue_dark=25, warm=100)
HORN_TIP = st.ramp("#5a2a28", dark=0.12, light=0.62, cool=330, chroma_dark=0.9)
BELL = st.ramp("#2e1c1a", dark=0.08, light=0.45, cool=330)
CORD = st.ramp("#b4392d", dark=0.2, light=0.86, chroma_light=0.5)

RAMPS = {"A": HORN, "B": GOLD, "C": HORN_TIP, "D": BELL, "E": CORD}

# Drachenhorn: oben der Schalltrichter (Goldrand um die dunkle Öffnung), darunter der obere Goldring, dann der Bogen nach
# unten links, der untere Goldring, die dunkle Spitze und das Mundstück; rechts hängt die Quaste am oberen Ring
DRAGON_HORN = """
................
.........efffd..
........eoopqrc.
........efqrrdc.
.........fggec..
.........56443..
.........45433..
........efgedc..
........45432C..
.......44322.D..
.....e44322..f..
.e..lf3322..ED..
.ffkke322...DC..
.cdiid2.....CB..
.....c..........
................
"""


def render(art: str, prefer: tuple[str, ...] = ("B", "A", "C", "D", "E")) -> Image.Image:
    return st.Spr.art(art).outline(prefer=prefer).image(RAMPS)


def build(put: Callable[[str, Image.Image], None]) -> None:
    put("item/dragon_horn", render(DRAGON_HORN))


if __name__ == "__main__":
    import sys

    sheet: dict[str, Image.Image] = {}
    build(lambda key, img: sheet.__setitem__(key.split("/")[-1], img))
    st.preview(sheet, sys.argv[1] if len(sys.argv) > 1 else "items_dragonwar.png", zoom=12, cols=4)
