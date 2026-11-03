#!/usr/bin/env python3
"""
Texturen der Sturmklippen (Biom um Voltarions Sturmgipfel).

  * block/stormstone            – Sturmstein: dunkler, schiefergrauer Fels mit bläulichem Schimmer und hellen Schlieren
  * block/charged_stormstone    – geladener Sturmstein: mit gleißenden, türkisfarbenen Blitzadern (leuchtet im Spiel)
  * block/fulgurite, _top        – Fulgurit: vom Blitz geschmolzener, glasiger Sand (Röhre mit hohlem Kern)
  * block/storm_crystal         – Sturmkristall: Grüppchen spitzer, eisblau leuchtender Kristalle (Kreuzpflanze)

Wird von tools/generate_textures.py aufgerufen (build(put)).
"""
from __future__ import annotations

from typing import Callable

from PIL import Image

import pixelart as pa
from region_blocks import stormstone
from pixelart import hexc

N = 16

SPARK = pa.palette("#1a8ac8", "#5ac8f0", "#b8f0ff", "#f4fdff")

def _wrap(v: int) -> int:
    return v % N


def charged_stormstone() -> Image.Image:
    img = stormstone()
    px = img.load()
    # verzweigte Blitzader
    path = [(0, 5), (1, 5), (2, 6), (3, 6), (4, 7), (5, 7), (6, 6), (7, 7), (8, 8), (9, 8), (10, 9), (11, 9), (12, 8), (13, 9),
            (14, 10), (15, 10), (6, 8), (6, 9), (5, 10), (5, 11), (4, 12), (10, 7), (10, 6), (11, 5), (11, 4), (12, 3)]
    for i, (x, y) in enumerate(path):
        px[x, y] = SPARK[2] if i % 4 else SPARK[3]
        if px[x, _wrap(y + 1)] not in SPARK:
            px[x, _wrap(y + 1)] = SPARK[0]
    return img


STORM_CRYSTAL = [
    "",
    "",
    ".......w........",
    "......wB.....w..",
    "......cB....wB..",
    "..w...cB....cB..",
    "..wB..cBw...cB..",
    "..cB..ccB..ccB..",
    "..cB.occB..ccB.w",
    "..ccBoccBoccBowB",
    "..ccBoccBoccBocB",
    ".occBoccBocccocB",
    ".occcoccco cccoo",
    ".oooooooooooooo.",
    "",
    "",
]


def storm_crystal() -> Image.Image:
    rows = [r.replace(" ", "c") for r in STORM_CRYSTAL]
    return pa.sprite(rows, {"o": hexc("#1a4a7a"), "c": SPARK[1], "B": SPARK[2], "w": SPARK[3]})


def build(put: Callable[[str, Image.Image], None]) -> None:
    put("block/charged_stormstone", charged_stormstone())
    put("block/storm_crystal", storm_crystal())
