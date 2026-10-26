#!/usr/bin/env python3
"""
Texturen des Frostkessels (Biom um Glaciaras Gletscherkessel) und die Geschosse der Drachen-Bosse.

  * block/frost_crystal      – durchscheinender, facettierter Eiskristall (halbtransparent, leuchtet im Spiel)
  * block/glacier_ice        – uraltes Gletschereis in Schichten mit eingeschlossenen Luftblasen
  * block/rime_stone         – grauer Stein unter einer Kruste aus Raureif
  * block/frozen_soil_(side|top|bottom) – gefrorene Erde mit Reif obenauf
  * block/icicle             – Eiszapfen (hängend, durchsichtiger Hintergrund)
  * entity/boss/projectile_* – Eisspeer, Eiszapfen, Dornensaat, Felsbrocken, Blitzkugel, Wasserblase

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

ICE = pa.palette("#3c6c9c", "#4f84b6", "#6a9fcc", "#8ab8dd", "#a8cfe9", "#c6e2f4", "#e2f2fb", "#ffffff")
STONE = pa.palette("#4a4d55", "#575b63", "#656972", "#737882", "#838892", "#959aa3")

def frost_crystal() -> Image.Image:
    """Facettierter Kristall: schräge Flächen in Blautönen, helle Kanten, halbtransparent."""
    img = pa.new()
    px = img.load()
    noise = pa.fractal_noise(8101, ((8, 0.6), (4, 0.4)))
    for y in range(N):
        for x in range(N):
            # Facetten: drei Scharen schräger Linien
            f1 = (x + y * 0.5) % 8
            f2 = (x * 0.6 - y) % 7
            v = 0.35 + 0.35 * noise[y][x] + (0.15 if f1 < 1 else 0) + (0.12 if f2 < 1 else 0)
            c = pa.pick(ICE[2:], min(0.99, v))
            alpha = 170 + int(60 * min(1.0, v))
            if f1 < 1 or f2 < 1:
                alpha = 235
            px[x, y] = (c[0], c[1], c[2], alpha)
    # Rand etwas heller
    for i in range(N):
        for (x, y) in ((i, 0), (0, i)):
            c = ICE[6]
            px[x, y] = (c[0], c[1], c[2], 230)
    return img


def glacier_ice() -> Image.Image:
    """Gletschereis: waagerechte Schichten (wellig) in Blau, eingeschlossene Bläschen, ein paar Risse."""
    img = pa.new()
    px = img.load()
    rng = random.Random(8201)
    noise = pa.fractal_noise(8202, ((8, 0.5), (4, 0.3), (2, 0.2)))
    for y in range(N):
        for x in range(N):
            band = (y + 1.2 * math.sin(x * 0.7 + 0.5)) / 4.0
            layer = (math.floor(band) % 3)
            v = 0.3 + layer * 0.12 + noise[y][x] * 0.25
            px[x, y] = pa.pick_dither(ICE[1:6], v, x, y, 0.5)
    for _ in range(9):
        x, y = rng.randrange(N), rng.randrange(N)
        px[x, y] = ICE[7]
        if rng.random() < 0.5:
            px[(x + 1) % N, y] = ICE[6]
    # feiner Riss
    x, y = 3, 2
    for _ in range(9):
        px[x % N, y % N] = ICE[6]
        x += rng.choice((0, 1))
        y += 1
    return img



ICICLE = """
.....oKKKo......
.....oKwKo......
.....oKwKo......
......oKKo......
......oKwo......
......oKKo......
......oKo.......
......oKo.......
.......oKo......
.......oKo......
.......oo.......
.......oo.......
.......o........
................
................
................
"""


def icicle() -> Image.Image:
    return pa.sprite(ICICLE, {"o": hexc("#6aa4d4"), "K": hexc("#b8def4"), "w": hexc("#ffffff")})


# --------------------------------------------------------------------------- Geschosse

def _orb(core: str, mid: str, rim: str, seed: int, spikes: int = 0, rough: bool = False) -> Image.Image:
    img = pa.new()
    px = img.load()
    noise = pa.value_noise(seed, 4)
    c_core, c_mid, c_rim = hexc(core), hexc(mid), hexc(rim)
    for y in range(N):
        for x in range(N):
            dx, dy = (x - 7.5) / 7.5, (y - 7.5) / 7.5
            d = math.hypot(dx, dy)
            a = math.atan2(dy, dx)
            limit = 0.62
            if spikes:
                limit += 0.3 * max(0.0, math.cos(a * spikes)) ** 6
            if rough:
                limit += (noise[y][x] - 0.5) * 0.2
            if d > limit:
                continue
            t = 1.0 - d / limit
            col = pa.mix(c_rim, c_mid, min(1.0, t * 1.8)) if t < 0.55 else pa.mix(c_mid, c_core, (t - 0.55) / 0.45)
            px[x, y] = col
    return img


SPEAR = """
..............wo
.............wKo
............wKo.
...........wKo..
..........wKo...
.........wKo....
........wKKo....
.......wKKo.....
......wKKo......
.....oKKo.......
....oKKo........
...oKKo.........
..oKo...........
.oKo............
.oo.............
................
"""


def ice_spear() -> Image.Image:
    return pa.sprite(SPEAR, {"o": hexc("#3a8ad0"), "K": hexc("#a8dcf8"), "w": hexc("#ffffff")})


def icicle_projectile() -> Image.Image:
    img = pa.new()
    big = icicle().resize((16, 16), Image.NEAREST)
    img.alpha_composite(big)
    return img


THORN = """
................
.......o........
......oGo.......
.....oGgGo......
....oGgggGo.....
...oGggyggGo....
..ooGggggGoo....
.oKoGgggGoKo....
..o.oGggGo.o....
.....oGGo.......
......oo........
.......o........
................
................
................
................
"""


def thorn() -> Image.Image:
    return pa.sprite(THORN, {"o": hexc("#1c3a12"), "G": hexc("#3f7a24"), "g": hexc("#6aa83a"), "y": hexc("#d8f07a"),
                             "K": hexc("#8a6a3a")})


def boulder() -> Image.Image:
    img = _orb("#a0968a", "#7a7064", "#3e362e", 8501, rough=True)
    px = img.load()
    rng = random.Random(8502)
    for _ in range(12):
        x, y = rng.randrange(3, 13), rng.randrange(3, 13)
        if px[x, y][3] > 0:
            px[x, y] = hexc("#2c2620") if rng.random() < 0.6 else hexc("#c0b4a4")
    return img


def spark() -> Image.Image:
    img = _orb("#ffffff", "#fff27a", "#c8a010", 8601, spikes=5)
    px = img.load()
    for i in range(16):
        a = i * math.tau / 16
        x = int(7.5 + math.cos(a) * 7)
        y = int(7.5 + math.sin(a) * 7)
        if 0 <= x < N and 0 <= y < N and i % 3 == 0:
            px[x, y] = hexc("#fffbe0")
    return img


def bubble() -> Image.Image:
    img = pa.new()
    px = img.load()
    for y in range(N):
        for x in range(N):
            d = math.hypot(x - 7.5, y - 7.5)
            if d < 6.8:
                if d > 5.6:
                    px[x, y] = (150, 225, 255, 230)
                else:
                    px[x, y] = (70, 160, 220, 110)
    for (x, y) in ((5, 4), (4, 5), (5, 5), (6, 4)):
        px[x, y] = (255, 255, 255, 255)
    return img


def build(put: Callable[[str, Image.Image], None]) -> None:
    put("block/frost_crystal", frost_crystal())
    put("block/glacier_ice", glacier_ice())
    put("block/icicle", icicle())
    put("entity/boss/projectile_ice_spear", ice_spear())
    put("entity/boss/projectile_icicle", icicle_projectile())
    put("entity/boss/projectile_thorn", thorn())
    put("entity/boss/projectile_boulder", boulder())
    put("entity/boss/projectile_spark", spark())
    put("entity/boss/projectile_bubble", bubble())
