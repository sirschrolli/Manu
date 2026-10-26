#!/usr/bin/env python3
"""
Restliche Texturen der Schattenwelt-Ausrüstung: Mondlaterne und Schattensetzling.

Rohstoffe, Waffen, Werkzeuge und Rüstungs-Symbole kommen aus tools/items_materials.py, items_gear.py und
items_armor.py, die getragenen Rüstungen aus tools/armor_worn.py, Erze und Speicherblöcke aus tools/shadow_blocks.py.

Wird von tools/generate_textures.py aufgerufen: build(put).
"""
from __future__ import annotations

import math
from typing import Callable

from PIL import Image

import building_textures as bld
import equipment_textures as et
import pixelart as pa
from pixelart import hexc

N = 16


# Mondsilber-Töne für den Laternenrahmen (o, a..f)
MOON_T = et.tones("#262c3a", "#4a566c", "#6e7c96", "#95a4bc", "#b9c6da", "#d9e3f0", "#f7fbff")
# =============================================================================
# Setzling
# =============================================================================

SAPLING = [
    "................",
    "................",
    "......l.l.......",
    ".....lLlLl..l...",
    "...l.lLLLl.lLl..",
    "..lLlLLdLLlLLl..",
    "..lLLLdLLLLLl...",
    "...lLLLLdLLl....",
    "....lLLdLLl.....",
    "......LbL.......",
    ".......b........",
    ".......bb.......",
    "......bb........",
    ".......b........",
    "......bbb.......",
    "................",
]


def shadow_sapling() -> Image.Image:
    return pa.sprite(SAPLING, {"l": hexc("#6c48b0"), "L": hexc("#4c2e88"), "d": hexc("#2e1a5a"), "b": hexc("#1c1428")})


# =============================================================================
# Ausgabe
# =============================================================================

def build(put: Callable[[str, Image.Image], None]) -> None:
    # Mondlaterne: Mondsilber-Rahmen, kaltes, weißblaues Licht
    frame = [MOON_T[k] for k in "oace"]
    light = [hexc("#4a7ac8"), hexc("#8ab8f0"), hexc("#c8e4ff"), hexc("#f4fbff")]
    put("block/moon_lantern", bld.lantern(frame, light))
    put("item/moon_lantern", bld.lantern_item(frame, light))

    # Setzling
    put("block/shadow_sapling", shadow_sapling())


if __name__ == "__main__":
    import sys
    out: dict[str, Image.Image] = {}
    build(lambda k, v: out.__setitem__(k, v))
    zoom, pad, cols = 8, 6, 8
    cell = 16 * zoom + pad
    rows = math.ceil(len(out) / cols)
    sheet = Image.new("RGBA", (cols * cell + pad, rows * cell + pad), (60, 62, 70, 255))
    for i, (key, img) in enumerate(out.items()):
        tile = pa.checker(16, 16, (150, 150, 150, 255), (128, 128, 128, 255))
        if key.endswith("_overlay") or key.endswith("_ore_glow"):
            base = Image.open("../src/main/resources/assets/kingdomsatwar/textures/block/"
                              + ("deep_shadow_stone" if "abyss" in key or "soul" in key else "shadow_stone") + ".png").convert("RGBA")
            tile = base
        img16 = img if img.size == (16, 16) else img.crop((0, 0, 16, 16))
        tile = tile.copy()
        tile.alpha_composite(img16)
        sheet.alpha_composite(pa.zoom(tile, zoom), (pad + (i % cols) * cell, pad + (i // cols) * cell))
    target = sys.argv[1] if len(sys.argv) > 1 else "shadow_gear.png"
    sheet.save(target)
    print(f"{len(out)} Texturen -> {target}")
