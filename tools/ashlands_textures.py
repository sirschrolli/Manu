#!/usr/bin/env python3
"""
Texturen der Aschelande (Biom um Pyrrhons Vulkankrater). Alle Blocktexturen sind nahtlos kachelbar.

  * block/ash_block              – feine, graue Asche mit hellen und dunklen Körnchen (auch für die Ascheschicht)
  * block/cinder_soil_(side|top|bottom) – Aschenerde: dunkle, verbrannte Erde, oben von Asche bedeckt (Seite mit Aschekante)
  * block/volcanic_rock          – poröses, fast schwarzes Vulkangestein mit Gasbläschen und rötlichem Schimmer
  * block/glowing_volcanic_rock  – Vulkangestein mit einem Netz glühender Risse (+ _glow: die selbstleuchtenden Risse)
  * block/charred_log_(side|top) – verkohlter Baumstamm: rissige schwarze Borke mit letzter Glut, Jahresringe aus Kohle
  * block/ember_vent(_top)       – Glutspalte: Vulkangestein mit glühendem Schlot oben (+ _top_glow)

Wird von tools/generate_textures.py aufgerufen (build(put)).
"""
from __future__ import annotations

import math
from typing import Callable

from PIL import Image

import pixelart as pa
from pixelart import hexc
from region_blocks import volcanic_rock

N = 16

ROCK = pa.palette("#1b1717", "#231e1e", "#2c2626", "#352e2d", "#3f3735", "#4a403d", "#574a46")
BARK = pa.palette("#121010", "#1a1616", "#231d1c", "#2d2524", "#382e2b", "#463934")
EMBER = [hexc("#5c1a0c"), hexc("#9a2e10"), hexc("#e0561a"), hexc("#ff9a2e"), hexc("#ffd36a")]


def _wrap(v: int) -> int:
    return v % N





def ember_vent_side() -> Image.Image:
    img = volcanic_rock()
    px = img.load()
    # senkrechter Riss bis oben, der nach oben hin glüht
    for y in range(N):
        x = 7 + (1 if y in (3, 4, 9, 10, 11) else 0) - (1 if y in (13, 14) else 0)
        px[x, y] = EMBER[0] if y > 5 else EMBER[1]
        if y < 4:
            px[_wrap(x + 1), y] = EMBER[0]
    return img


VENT_HOLE = {(x, y) for x in range(6, 10) for y in range(6, 10)} - {(6, 6), (9, 6), (6, 9), (9, 9)}


def ember_vent_top() -> Image.Image:
    img = volcanic_rock()
    px = img.load()
    for y in range(N):
        for x in range(N):
            d = math.hypot(x - 7.5, y - 7.5)
            if (x, y) in VENT_HOLE:
                px[x, y] = EMBER[2] if d < 1.2 else EMBER[1]
            elif d < 3.6:
                px[x, y] = pa.mix(px[x, y], EMBER[0], 0.6)
            elif d < 4.6:
                px[x, y] = pa.mix(px[x, y], ROCK[0], 0.5)
    # Risse vom Schlot nach außen
    for (x, y) in [(10, 5), (11, 4), (12, 4), (5, 10), (4, 11), (4, 12), (10, 10), (11, 11)]:
        px[x, y] = EMBER[0]
    return img


def ember_vent_top_glow() -> Image.Image:
    img = pa.new()
    px = img.load()
    for (x, y) in VENT_HOLE:
        d = math.hypot(x - 7.5, y - 7.5)
        px[x, y] = EMBER[4] if d < 1.0 else EMBER[3]
    return img


def build(put: Callable[[str, Image.Image], None]) -> None:
    put("block/ember_vent", ember_vent_side())
    put("block/ember_vent_top", ember_vent_top())
    put("block/ember_vent_top_glow", ember_vent_top_glow())


if __name__ == "__main__":
    import sys

    sheet: dict[str, Image.Image] = {}
    build(lambda key, img: sheet.__setitem__(key, img))
    keys = [k for k in sheet if not k.endswith("_glow")]
    zoom, pad = 6, 6
    cell = 48 * zoom + pad
    cols = 4
    rows = (len(keys) + cols - 1) // cols
    out = Image.new("RGBA", (cols * cell + pad, rows * cell + pad), (60, 64, 70, 255))
    for i, key in enumerate(keys):
        img = sheet[key]
        glow = sheet.get(key + "_glow")
        if glow is not None:
            img = pa.compose(img, glow)
        out.alpha_composite(pa.zoom(pa.tiled(img, 3), zoom), (pad + (i % cols) * cell, pad + (i // cols) * cell))
    target = sys.argv[1] if len(sys.argv) > 1 else "/tmp/claude-0/-home-claude/50786bb5-6cc3-5907-a25e-13bee43305bb/scratchpad/ashlands_tex.png"
    out.save(target)
    print(f"{len(sheet)} Texturen -> {target}")
