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

import math
import random
from typing import Callable

from PIL import Image

import pixelart as pa
from pixelart import hexc

N = 16

STORM = pa.palette("#1a1e28", "#242a36", "#2e3544", "#394152", "#465062", "#566174")
SPARK = pa.palette("#1a8ac8", "#5ac8f0", "#b8f0ff", "#f4fdff")
GLASS = pa.palette("#24282a", "#34393a", "#474d4a", "#5e645c", "#7a7e70", "#9a9c88")
SHINE = hexc("#d4ece6")


def _wrap(v: int) -> int:
    return v % N


def stormstone() -> Image.Image:
    noise = pa.fractal_noise(14101, ((8, 0.5), (4, 0.3), (2, 0.2)))
    img = pa.new()
    px = img.load()
    for y in range(N):
        for x in range(N):
            streak = math.sin((x * 0.6 + y * 0.9) + noise[y][x] * 3.0)
            v = 0.3 + 0.4 * noise[y][x] + 0.12 * streak
            px[x, y] = pa.pick_dither(STORM, v, x, y, 0.6)
    rng = random.Random(14102)
    for _ in range(4):
        x, y = rng.randrange(N), rng.randrange(N)
        px[x, y] = STORM[0]
        px[_wrap(x + 1), _wrap(y + 1)] = STORM[0]
    return img


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


def fulgurite_side() -> Image.Image:
    """Glasige, knollige Röhre: geschmolzener Sand mit Blasen, verkohlten Körnern und glänzenden Glasstellen."""
    noise = pa.fractal_noise(14201, ((6, 0.45), (3, 0.35), (2, 0.2)))
    img = pa.new()
    px = img.load()
    for y in range(N):
        for x in range(N):
            lump = math.sin(x * 1.3 + noise[y][x] * 4.0) * math.cos(y * 0.9 - noise[y][x] * 3.0)
            v = 0.22 + 0.5 * noise[y][x] + 0.14 * lump
            px[x, y] = pa.pick_dither(GLASS, v, x, y, 0.5)
    rng = random.Random(14202)
    for _ in range(9):   # Blasen: dunkler Rand, heller Glanz
        x, y = rng.randrange(N), rng.randrange(N)
        px[x, y] = GLASS[0]
        px[_wrap(x - 1), _wrap(y - 1)] = SHINE
    for _ in range(6):
        px[rng.randrange(N), rng.randrange(N)] = GLASS[5]
    return img


def fulgurite_top() -> Image.Image:
    img = pa.new()
    px = img.load()
    for y in range(N):
        for x in range(N):
            d = math.hypot(x - 7.5, y - 7.5)
            if d < 2.6:
                px[x, y] = STORM[0]
            elif d < 4.2:
                px[x, y] = SHINE if (x + y) % 3 == 0 else GLASS[5]
            else:
                px[x, y] = GLASS[2] if (x * 3 + y) % 4 else GLASS[1]
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
    put("block/stormstone", stormstone())
    put("block/charged_stormstone", charged_stormstone())
    put("block/fulgurite", fulgurite_side())
    put("block/fulgurite_top", fulgurite_top())
    put("block/storm_crystal", storm_crystal())
