#!/usr/bin/env python3
"""
Texturen rund um die Dörfer der Umbrer (Tür, Falltür, Acker und Pfad: tools/shadow_blocks.py):

  * block/duskgrain_stage0..3          – Dämmerkorn vom Keimling bis zur reifen, violett-goldenen Ähre
  * item/duskgrain_seeds, item/duskgrain, item/dusk_bread

Wird von tools/generate_textures.py aufgerufen (build(put)).
"""
from __future__ import annotations

import math
from typing import Callable

from PIL import Image

import pixelart as pa
from pixelart import hexc

N = 16


# =============================================================================
# Tür und Falltür
# =============================================================================

# =============================================================================
# Ackerland und Pfad
# =============================================================================

# =============================================================================
# Dämmerkorn
# =============================================================================

GRAIN = {"s": hexc("#2e3a5a"), "S": hexc("#4a5e88"), "t": hexc("#5a4a8a"), "e": hexc("#8a6ac0"), "E": hexc("#c8a8f0"),
         "g": hexc("#b89a4a"), "G": hexc("#f0d890"), "w": hexc("#fff4d0")}


SEEDS = [
    "", "", "", "", "",
    "......e.........",
    "....e..E...e....",
    "...eE....e.Ee...",
    "......eE...e....",
    "....e....Ee.....",
    "...Ee..e....e...",
    "......eE..eE....",
    "........e.......",
]

GRAIN_ITEM = [
    "..........w.....",
    ".........gGw....",
    "........gGEg....",
    ".......EgGg.....",
    "......gEgE......",
    ".....EgGg.......",
    "....gEgE........",
    "...eSgE.........",
    "..eS.e..........",
    ".sS.............",
    "sS..............",
    "s...............",
]

BREAD = [
    "", "", "", "",
    "....oooooooo....",
    "..oobbBbbBbboo..",
    ".obBbbbBbbbBbbo.",
    "obbbBbbbBbbbbBbo",
    "obbbbbBbbbbBbbbo",
    "odbbbbbbbbbbbbdo",
    ".oddbbbbbbbbddo.",
    "..ooddddddddoo..",
    "....oooooooo....",
]


def build(put: Callable[[str, Image.Image], None]) -> None:
    # Tür, Falltür, Acker und Pfad kommen aus tools/shadow_blocks.py
    put("item/duskgrain_seeds", pa.sprite(SEEDS, GRAIN))
    put("item/duskgrain", pa.sprite(GRAIN_ITEM, GRAIN))
    put("item/dusk_bread", pa.sprite(BREAD, {"o": hexc("#2a1626"), "b": hexc("#6a3e5a"), "B": hexc("#a8709a"),
                                             "d": hexc("#4a2a40")}))


if __name__ == "__main__":
    import sys

    out: dict[str, Image.Image] = {}
    build(lambda k, v: out.__setitem__(k, v))
    zoom, pad, cols = 8, 6, 6
    cell = N * zoom + pad
    rows = math.ceil(len(out) / cols)
    sheet = Image.new("RGBA", (cols * cell + pad, rows * cell + pad), (60, 62, 70, 255))
    for i, img in enumerate(out.values()):
        sheet.alpha_composite(img.resize((N * zoom, N * zoom), Image.NEAREST), (pad + (i % cols) * cell, pad + (i // cols) * cell))
    sheet.save(sys.argv[1] if len(sys.argv) > 1 else "shadow_village_preview.png")
    print(len(out), "Texturen")
