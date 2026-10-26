#!/usr/bin/env python3
"""
Restliche Texturen der Schattenwelt-Ausrüstung: Mondlaterne, Schattensetzling und die getragenen Rüstungen aus
Nachtstahl und Abyssit.

Rohstoffe, Waffen, Werkzeuge und Rüstungs-Symbole kommen aus tools/items_materials.py, items_gear.py und
items_armor.py, Erze und Speicherblöcke aus tools/shadow_blocks.py.

Wird von tools/generate_textures.py aufgerufen: build(put) für Laterne und Setzling, build_entity(save) für die
getragenen Rüstungen.
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


# Werkstoff-Töne für Werkzeuge und Rüstungen (o, a..f)
MOON_T = et.tones("#262c3a", "#4a566c", "#6e7c96", "#95a4bc", "#b9c6da", "#d9e3f0", "#f7fbff")
NIGHT_T = et.tones("#04030a", "#0f0d22", "#1b1838", "#2a2552", "#3d3570", "#594e98", "#8f80d6")
ABYSS_T = et.tones("#030105", "#0d0612", "#1a0c22", "#2a1234", "#3e1a4a", "#5a2666", "#86408e")

MOON_TRIM = {"g": hexc("#4a566c"), "G": hexc("#8796b0"), "y": hexc("#c4d2e6"), "Y": hexc("#f4f8ff")}
MAGENTA = {"g": hexc("#5a0a52"), "G": hexc("#b01ea2"), "y": hexc("#ff5ae6"), "Y": hexc("#ffc4f6")}
VIOLET_ACCENT = {"x": hexc("#2a2470"), "X": hexc("#7a6af0"), "z": hexc("#d4ccff")}
MAGENTA_ACCENT = {"x": hexc("#5a0a52"), "X": hexc("#e03ad0"), "z": hexc("#ffc4f6")}


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


NIGHTSTEEL_STYLE = et.Style(NIGHT_T, MOON_TRIM, VIOLET_ACCENT, "plate")
ABYSSITE_STYLE = et.Style(ABYSS_T, MAGENTA, MAGENTA_ACCENT, "plate")


def build_entity(save: Callable[[str, Image.Image], None]) -> None:
    for name, style in (("nightsteel", NIGHTSTEEL_STYLE), ("abyssite", ABYSSITE_STYLE)):
        outer, inner = et.armor_layers(style)
        save(f"entity/equipment/humanoid/{name}", outer)
        save(f"entity/equipment/humanoid_leggings/{name}", inner)


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
