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
import random
from typing import Callable

from PIL import Image

import pixelart as pa
from pixelart import CLEAR, Color, hexc

N = 16

ASH = pa.palette("#2c2927", "#363230", "#403c39", "#4a4643", "#55504c", "#605a55", "#6c6660", "#7a736c")
SOIL = pa.palette("#1d1714", "#261e1a", "#2f2520", "#392d26", "#44362d", "#504035")
ROCK = pa.palette("#1b1717", "#231e1e", "#2c2626", "#352e2d", "#3f3735", "#4a403d", "#574a46")
BARK = pa.palette("#121010", "#1a1616", "#231d1c", "#2d2524", "#382e2b", "#463934")
EMBER = [hexc("#5c1a0c"), hexc("#9a2e10"), hexc("#e0561a"), hexc("#ff9a2e"), hexc("#ffd36a")]


def _wrap(v: int) -> int:
    return v % N


def ash_block() -> Image.Image:
    """Feine Asche: weiches Rauschen in vielen Grautönen, dazu einzelne helle und dunkle Körnchen."""
    noise = pa.fractal_noise(9001, ((4, 0.5), (2, 0.5)))
    rng = random.Random(9002)
    img = pa.new()
    px = img.load()
    for y in range(N):
        for x in range(N):
            px[x, y] = pa.pick_dither(ASH[3:7], 0.1 + noise[y][x] * 0.8, x, y, 0.9)
    for _ in range(12):
        x, y = rng.randrange(N), rng.randrange(N)
        px[x, y] = ASH[7]
    for _ in range(9):
        x, y = rng.randrange(N), rng.randrange(N)
        px[x, y] = ASH[1] if rng.random() < 0.6 else ASH[0]
    return img


def _soil_base(seed: int) -> Image.Image:
    noise = pa.fractal_noise(seed, ((8, 0.4), (4, 0.4), (2, 0.2)))
    rng = random.Random(seed + 1)
    img = pa.new()
    px = img.load()
    for y in range(N):
        for x in range(N):
            px[x, y] = pa.pick_dither(SOIL[1:6], 0.1 + noise[y][x] * 0.8, x, y, 0.7)
    # Kohlestückchen und kleine Steinchen
    for _ in range(9):
        x, y = rng.randrange(N), rng.randrange(N)
        px[x, y] = SOIL[0]
        if rng.random() < 0.5:
            px[_wrap(x + 1), y] = SOIL[0]
    for _ in range(6):
        x, y = rng.randrange(N), rng.randrange(N)
        px[x, y] = ASH[3]
    return img


def cinder_soil_bottom() -> Image.Image:
    return _soil_base(9101)


def cinder_soil_top() -> Image.Image:
    """Oben: Asche über der Erde, an manchen Stellen schaut die dunkle Erde durch."""
    soil = _soil_base(9102)
    ash = ash_block()
    noise = pa.fractal_noise(9103, ((4, 0.55), (2, 0.45)))
    img = soil.copy()
    px = img.load()
    apx = ash.load()
    for y in range(N):
        for x in range(N):
            if noise[y][x] > 0.55:
                px[x, y] = pa.mix(apx[x, y], SOIL[3], 0.3)
            elif noise[y][x] > 0.46:
                px[x, y] = ASH[2]
    return img


def cinder_soil_side() -> Image.Image:
    """Seite: Erde mit einer unregelmäßigen Aschekante oben (wie der Grasrand bei Erde)."""
    img = _soil_base(9104)
    px = img.load()
    apx = ash_block().load()
    edge = [3, 2, 3, 4, 3, 2, 2, 3, 4, 5, 3, 2, 3, 3, 2, 3]   # Tiefe der Aschekante je Spalte
    for x in range(N):
        for y in range(edge[x]):
            px[x, y] = apx[x, y]
        px[x, edge[x]] = ASH[1] if x % 3 else SOIL[0]
    return img


def _rock_pixels(seed: int) -> list[list[Color]]:
    noise = pa.fractal_noise(seed, ((8, 0.35), (4, 0.4), (2, 0.25)))
    out = [[ROCK[0]] * N for _ in range(N)]
    for y in range(N):
        for x in range(N):
            out[y][x] = pa.pick_dither(ROCK[1:7], 0.08 + noise[y][x] * 0.86, x, y, 0.9)
    return out


def volcanic_rock() -> Image.Image:
    """Poröses Gestein: Bläschen (dunkel, mit hellem unteren Rand), dazu ein leicht rötlicher Schimmer."""
    rows = _rock_pixels(9201)
    rng = random.Random(9202)
    img = pa.new()
    px = img.load()
    for y in range(N):
        for x in range(N):
            px[x, y] = rows[y][x]
    # Gasbläschen
    for _ in range(11):
        x, y = rng.randrange(N), rng.randrange(N)
        big = rng.random() < 0.35
        px[x, y] = hexc("#110e0e")
        if big:
            px[_wrap(x + 1), y] = hexc("#110e0e")
            px[x, _wrap(y + 1)] = ROCK[0]
            px[_wrap(x + 1), _wrap(y + 1)] = ROCK[5]
        else:
            px[x, _wrap(y + 1)] = ROCK[5]
    # rötliche Flecken (Eisenoxid)
    tint = hexc("#5e403a")
    noise = pa.value_noise(9203, 8)
    for y in range(N):
        for x in range(N):
            if noise[y][x] > 0.72 and px[x, y] not in (hexc("#110e0e"),):
                px[x, y] = pa.mix(px[x, y], tint, 0.45)
    return img


def _crack_network(seed: int) -> tuple[set[tuple[int, int]], set[tuple[int, int]]]:
    """Kachelbares Rissnetz aus Voronoi-Grenzen: (Kern der Risse, schmaler Rand daneben)."""
    _, border, _ = pa.voronoi(seed, 4, jitter=1.0)
    core = {(x, y) for y in range(N) for x in range(N) if border[y][x] < 0.6}
    rim = {(x, y) for y in range(N) for x in range(N) if 0.6 <= border[y][x] < 1.5}
    return core, rim


def glowing_volcanic_rock() -> Image.Image:
    img = volcanic_rock()
    px = img.load()
    core, rim = _crack_network(9301)
    for x, y in rim:
        px[x, y] = pa.mix(px[x, y], EMBER[0], 0.55)
    for x, y in core:
        px[x, y] = EMBER[2]
    return img


def glowing_volcanic_rock_glow() -> Image.Image:
    img = pa.new()
    px = img.load()
    core, _ = _crack_network(9301)
    for x, y in core:
        px[x, y] = EMBER[4] if (x * 7 + y * 3) % 7 == 0 else (EMBER[3] if (x + 2 * y) % 3 else EMBER[2])
    return img


def charred_log() -> Image.Image:
    """Borke in senkrechten Streifen, tief gerissen; in einigen Rissen glimmt noch Glut."""
    rng = random.Random(9401)
    noise = pa.value_noise(9402, 4)
    img = pa.new()
    px = img.load()
    # Streifen: Breite 2-3, jede Spalte eigener Grundton
    col_tone = []
    x = 0
    while x < N:
        w = rng.choice((2, 3))
        tone = rng.randrange(2, 5)
        for i in range(w):
            if x + i < N:
                col_tone.append(tone if i < w - 1 else 1)
        x += w
    for y in range(N):
        for x in range(N):
            t = col_tone[x] + (1 if noise[y][x] > 0.68 else 0) - (1 if noise[y][x] < 0.25 else 0)
            px[x, y] = BARK[max(0, min(5, t))]
    # Querrisse (Holzkohle-Muster)
    for _ in range(7):
        x, y = rng.randrange(N), rng.randrange(N)
        for i in range(rng.choice((2, 3))):
            px[_wrap(x + i), y] = BARK[0]
    # Glut in einzelnen Rissen
    for x, y in [(4, 3), (4, 4), (11, 9), (11, 10), (11, 11), (7, 14)]:
        px[x, y] = EMBER[1] if (x + y) % 2 else EMBER[0]
    return img


def charred_log_top() -> Image.Image:
    """Schnittfläche: Ringe aus Kohle, außen die Borke, Risse vom Rand zur Mitte, ein Hauch Asche."""
    img = pa.new()
    px = img.load()
    for y in range(N):
        for x in range(N):
            dx, dy = x - 7.5, y - 7.5
            d = max(abs(dx), abs(dy)) * 0.6 + math.hypot(dx, dy) * 0.4
            if d > 6.9:
                px[x, y] = BARK[2] if (x + y) % 3 else BARK[1]
            else:
                ring = int(d * 1.2) % 2
                px[x, y] = BARK[4] if ring else BARK[3]
                if d < 1.6:
                    px[x, y] = ASH[3]
    for (x, y) in [(7, 1), (7, 2), (8, 3), (13, 8), (12, 8), (11, 9), (3, 11), (4, 10), (5, 10)]:
        px[x, y] = BARK[0]
    for (x, y) in [(6, 6), (9, 9), (9, 6)]:
        px[x, y] = ASH[5]
    return img


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
    put("block/ash_block", ash_block())
    put("block/cinder_soil_side", cinder_soil_side())
    put("block/cinder_soil_top", cinder_soil_top())
    put("block/cinder_soil_bottom", cinder_soil_bottom())
    put("block/volcanic_rock", volcanic_rock())
    put("block/glowing_volcanic_rock", glowing_volcanic_rock())
    put("block/glowing_volcanic_rock_glow", glowing_volcanic_rock_glow())
    put("block/charred_log_side", charred_log())
    put("block/charred_log_top", charred_log_top())
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
