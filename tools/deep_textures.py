#!/usr/bin/env python3
"""
Texturen der Abgrundgrotte (Biom um Thalassas Versunkene Grotte).

  * block/abyssal_stone – Abgrundstein: dunkler, blaugrüner Fels mit hellen Muschel- und Fossilsplittern
  * block/abyssal_sand  – Abgrundsand: feiner, dunkelgrauer Tiefseesand mit Glimmerkörnern
  * block/glow_coral    – Leuchtkoralle: verzweigte, türkis leuchtende Koralle (Kreuzpflanze)
  * block/nacre_block   – Perlmutt: schimmernde Schichten in Weiß, Rosa und Hellblau

Wird von tools/generate_textures.py aufgerufen (build(put)).
"""
from __future__ import annotations

import math
from typing import Callable

from PIL import Image

import pixelart as pa
from pixelart import hexc

N = 16

ABYSS = pa.palette("#0e1a1e", "#152429", "#1c2f34", "#243a40", "#2e474d", "#3a565c")
CORAL = pa.palette("#0e5a6a", "#1a8c9a", "#3ac8c8", "#8af4e8", "#e0fff8")
NACRE = [hexc("#c8c4d0"), hexc("#dcd4e0"), hexc("#ecdce4"), hexc("#d4e4ec"), hexc("#e8f0f4"), hexc("#f8f4f8"), hexc("#c4d8e4")]



GLOW_CORAL = [
    "",
    "",
    "....w......w....",
    "....cw....wc..w.",
    ".w...c...cc..wc.",
    ".cw..cc.cc..cc..",
    "..c...ccc..cc...",
    "..cc...c..cc..w.",
    "...cc..c.cc..cc.",
    ".w..cc.ccc..cc..",
    ".cc..cccc..cc...",
    "..cc..cc..cc....",
    "...cc.oc.cc.....",
    "....ccooocc.....",
    ".....oooo.......",
    "......oo........",
]


def glow_coral() -> Image.Image:
    return pa.sprite(GLOW_CORAL, {"o": CORAL[0], "c": CORAL[2], "w": CORAL[4]})


def nacre_block() -> Image.Image:
    """Perlmutt: wellige, schimmernde Schichten."""
    noise = pa.fractal_noise(15301, ((8, 0.6), (4, 0.4)))
    img = pa.new()
    px = img.load()
    for y in range(N):
        for x in range(N):
            band = math.sin(y * 0.9 + math.sin(x * 0.5) * 1.5 + noise[y][x] * 3.0)
            i = int((band * 0.5 + 0.5) * (len(NACRE) - 1) + noise[y][x] * 1.5) % len(NACRE)
            px[x, y] = NACRE[i]
    return img


def build(put: Callable[[str, Image.Image], None]) -> None:
    put("block/glow_coral", glow_coral())
    put("block/nacre_block", nacre_block())
