#!/usr/bin/env python3
"""
Gegenstands-Texturen rund um die wilden Kreaturen: Wildbret (roh und gebraten) und die Trollkeule.
Spawn-Eier: tools/items_eggs.py, Irrlichtstaub: tools/items_materials.py.

Wird von tools/generate_textures.py aufgerufen (build(put)).
"""
from __future__ import annotations

from typing import Callable

from PIL import Image

import pixelart as pa
from pixelart import hexc


# =============================================================================
# Wildbret, Trollkeule
# =============================================================================

HAUNCH = [
    "",
    "",
    "..........ww....",
    ".........wWWw...",
    ".........wWw....",
    "........owwo....",
    ".......orrro....",
    ".....oorRRrro...",
    "....orRRhRRrro..",
    "...orRhRRRRRrro.",
    "...orRRRRRhRrro.",
    "...orrRRRRRrrro.",
    "....orrrrrrrro..",
    ".....oorrrroo...",
    ".......oooo.....",
]

TROLL_CLUB = [
    "........oooo....",
    ".......oIIiIo...",
    "......oIgIiigo..",
    ".....oIIiiIIgo..",
    ".....ogIiIiigio.",
    "....oiIiiIiiio..",
    "....ojiiIigio...",
    "...ojjiiiioo....",
    "...ojjjooo......",
    "..ojjo..........",
    ".ojjo...........",
    ".olo............",
    "olo.............",
    "oo..............",
]


def items(put: Callable[[str, Image.Image], None]) -> None:
    put("item/venison", pa.sprite(HAUNCH, {
        "o": hexc("#3a0c10"), "r": hexc("#8a1c22"), "R": hexc("#b83038"), "h": hexc("#e8a0a0"),
        "w": hexc("#c8bca4"), "W": hexc("#f4ecdc")}))
    put("item/cooked_venison", pa.sprite(HAUNCH, {
        "o": hexc("#2e1608"), "r": hexc("#6a3a16"), "R": hexc("#9a5a26"), "h": hexc("#d89a52"),
        "w": hexc("#c8bca4"), "W": hexc("#f4ecdc")}))
    put("item/troll_club", pa.sprite(TROLL_CLUB, {
        "o": hexc("#1e140a"), "i": hexc("#6a4624"), "I": hexc("#8a6236"), "g": hexc("#9aa0a8"),
        "j": hexc("#7a5430"), "l": hexc("#4a3018")}))


def build(put: Callable[[str, Image.Image], None]) -> None:
    items(put)


if __name__ == "__main__":
    import sys

    sheet: dict[str, Image.Image] = {}
    build(lambda key, img: sheet.__setitem__(key, img))
    zoom, pad, cols = 8, 4, 5
    cell = 16 * zoom + pad
    rows = (len(sheet) + cols - 1) // cols
    out = Image.new("RGBA", (cols * cell + pad, rows * cell + pad), (60, 64, 70, 255))
    for i, (key, img) in enumerate(sheet.items()):
        tile = pa.checker(16, 16, (150, 150, 150, 255), (128, 128, 128, 255))
        tile.alpha_composite(img)
        out.alpha_composite(pa.zoom(tile, zoom), (pad + (i % cols) * cell, pad + (i // cols) * cell))
    target = sys.argv[1] if len(sys.argv) > 1 else "/tmp/claude-0/-home-claude/50786bb5-6cc3-5907-a25e-13bee43305bb/scratchpad/creature_items.png"
    out.save(target)
    print(f"{len(sheet)} Symbole -> {target}")
