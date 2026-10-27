#!/usr/bin/env python3
"""
Texturen der Zerklüfteten Weiten (Biom um Terragons Felsschlucht).

  * block/amber_crystal – durchscheinender, facettierter Bernsteinkristall (leuchtet im Spiel)
  * block/amber_cluster – Bernsteindruse: Grüppchen spitzer Kristalle (Kreuzpflanze)
  * block/shale         – Schiefer: dünne, graubraune Schichten mit feinen Rissen
  * block/rubble        – Geröll: grobe, kantige Steinbrocken in Grau und Braun
  * block/rift_stone    – Spaltgestein: dunkler Fels mit glimmenden Bernsteinrissen

Wird von tools/generate_textures.py aufgerufen (build(put)).
"""
from __future__ import annotations

import math
import random
from typing import Callable

from PIL import Image

import pixelart as pa

N = 16

AMBER = pa.palette("#7a3c06", "#a8560e", "#cf7a18", "#eba030", "#ffc65a", "#ffe08a", "#fff2c0")
SHALE = pa.palette("#2c2a28", "#3a3734", "#48443f", "#57524b", "#676057", "#787064")
STONE = pa.palette("#3e3a36", "#4e4943", "#5f5951", "#716a61", "#847c71", "#978e82")
DIRT = pa.palette("#3a2c20", "#4a3a2a", "#5c4a36")


def _wrap(v: int) -> int:
    return v % N


def amber_crystal() -> Image.Image:
    """Facetten in Bernsteintönen, helle Kanten, halbtransparent."""
    img = pa.new()
    px = img.load()
    noise = pa.fractal_noise(13101, ((8, 0.6), (4, 0.4)))
    for y in range(N):
        for x in range(N):
            f1 = (x * 0.8 + y * 0.6) % 7
            f2 = (x * 0.5 - y) % 6
            v = 0.32 + 0.38 * noise[y][x] + (0.16 if f1 < 1 else 0) + (0.12 if f2 < 1 else 0)
            c = pa.pick(AMBER[1:], min(0.99, v))
            alpha = 185 + int(55 * min(1.0, v))
            if f1 < 1 or f2 < 1:
                alpha = 240
            px[x, y] = (c[0], c[1], c[2], alpha)
    for i in range(N):
        for (x, y) in ((i, 0), (0, i)):
            c = AMBER[5]
            px[x, y] = (c[0], c[1], c[2], 235)
    return img


AMBER_CLUSTER = [
    "",
    "",
    "......w.........",
    "......Yw....w...",
    "......aY...wY...",
    "..w...aY...aY...",
    "..Yw..aYw..aY...",
    "..aY..aaY.aaY...",
    "..aY.oaaY.aaY.w.",
    "..aaYoaaYoaaYwY.",
    "..aaYoaaYoaaYaY.",
    ".oaaYoaaYoaaaaY.",
    ".oaaaoaaaoaaaaYo",
    ".oooooooooooooo.",
    "",
    "",
]


def amber_cluster() -> Image.Image:
    return pa.sprite(AMBER_CLUSTER, {"o": AMBER[0], "a": AMBER[2], "Y": AMBER[4], "w": AMBER[6]})


def shale() -> Image.Image:
    """Dünne, leicht wellige Schichten; einzelne Lagen heller, feine senkrechte Risse."""
    rng = random.Random(13201)
    noise = pa.fractal_noise(13202, ((8, 0.5), (4, 0.3), (2, 0.2)))
    img = pa.new()
    px = img.load()
    tones = [rng.randrange(1, 5) for _ in range(N)]
    for y in range(N):
        for x in range(N):
            layer = (y + int(round(math.sin(x * 0.4) * 0.8))) % N
            t = tones[layer] + (1 if noise[y][x] > 0.7 else 0) - (1 if noise[y][x] < 0.25 else 0)
            if layer % 4 == 3:
                t = 0
            px[x, y] = SHALE[max(0, min(5, t))]
    for _ in range(5):
        x, y = rng.randrange(N), rng.randrange(N)
        for k in range(rng.choice((2, 3))):
            px[x, _wrap(y + k)] = SHALE[0]
    return img


def rubble() -> Image.Image:
    """Kantige Brocken (Voronoi-Zellen) mit Licht oben links, Erde in den Fugen."""
    cells, border, pts = pa.voronoi(13301, 11)
    img = pa.new()
    px = img.load()
    tone = [random.Random(13302 + i).randrange(1, 5) for i in range(11)]
    for y in range(N):
        for x in range(N):
            b = border[y][x]
            c = cells[y][x]
            if b < 0.7:
                px[x, y] = DIRT[1] if (x + y) % 3 else DIRT[0]
                continue
            cx, cy = pts[c]
            light = 1 if (x - cx) + (y - cy) < -1.5 else (-1 if (x - cx) + (y - cy) > 2 else 0)
            px[x, y] = STONE[max(0, min(5, tone[c] + light))]
    return img


def rift_stone() -> Image.Image:
    """Dunkler Fels, durchzogen von verzweigten, glimmenden Bernsteinrissen."""
    noise = pa.fractal_noise(13401, ((8, 0.5), (4, 0.3), (2, 0.2)))
    img = pa.new()
    px = img.load()
    for y in range(N):
        for x in range(N):
            v = 0.2 + 0.5 * noise[y][x]
            px[x, y] = pa.pick_dither(SHALE[:5], v, x, y, 0.6)
    crack = [(2, 0), (3, 1), (3, 2), (4, 3), (5, 3), (6, 4), (6, 5), (7, 6), (8, 6), (9, 7), (9, 8), (10, 9), (11, 9), (12, 10),
             (12, 11), (13, 12), (13, 13), (14, 14), (14, 15), (6, 6), (5, 7), (4, 8), (4, 9), (3, 10), (10, 2), (11, 2), (12, 1),
             (9, 3), (9, 4), (8, 5)]
    for i, (x, y) in enumerate(crack):
        px[x, y] = AMBER[4] if i % 3 else AMBER[5]
        nx = _wrap(x + 1)
        if px[nx, y] not in (AMBER[4], AMBER[5]):
            px[nx, y] = AMBER[1]
    return img


def build(put: Callable[[str, Image.Image], None]) -> None:
    put("block/amber_crystal", amber_crystal())
    put("block/amber_cluster", amber_cluster())
    put("block/shale", shale())
    put("block/rubble", rubble())
    put("block/rift_stone", rift_stone())
