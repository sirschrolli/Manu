#!/usr/bin/env python3
"""
Die Texturen des Urmeers (0.89) im Vanilla-Stil (siehe tools/vanilla_style.py):

  * Gezeitenstein: grünblauer Fels mit welligen, dunkleren Schichten und hellen Muschelsplittern
  * Gezeitensteinziegel: große, flache Ziegel desselben Steins, die Kanten vom Wasser gerundet
  * gemeißelter Gezeitenstein: eine polierte Platte mit eingelassenem Feld, darin eine Welle, die sich zur Spirale
    einrollt – die Spirale leuchtet (eigene Ebene _glow)
  * der Schleier des Gezeitentors: wirbelndes, leuchtendes Wasser in Tiefblau, Türkis und Gischtweiß, durch das
    Bläschen aufsteigen (bewegt, leicht durchscheinend)

Wird von tools/generate_textures.py aufgerufen: build(put). Bildfolgen stehen in ANIMATIONS.
"""
from __future__ import annotations

import math
import random
from typing import Callable

from PIL import Image

import pixelart as pa
import vanilla_style as vs
from pixelart import Color, hexc

N = 16

#: Bildfolgen: Name -> (Bilder, Ticks je Bild, weich überblenden)
ANIMATIONS: dict[str, tuple[int, int, bool]] = {}

# Paletten (dunkel -> hell)
TIDE = vs.tones("#5e8b8a", 4, spread=0.15, cool=205, warm=165, hue=10, sat=0.05)
TIDE_DARK = vs.tones("#456f72", 2, spread=0.08, cool=210, warm=180, hue=6)
TIDE_BRICK = vs.tones("#5a8786", 7, spread=0.27, cool=205, warm=165, hue=12, sat=0.06)
TIDE_PLATE = vs.tones("#6a9896", 7, spread=0.22, cool=205, warm=165, hue=12, sat=0.06)
SHELL = [hexc("#d8d2bc"), hexc("#efe8d4")]
GLOW = [hexc("#2fc8c0"), hexc("#6ff2e2"), hexc("#c4fff6"), hexc("#ffffff")]
GATE = [hexc("#082c4a"), hexc("#0c4766"), hexc("#11657e"), hexc("#1a8a98"), hexc("#2bb2b2"), hexc("#5ad6cc"), hexc("#a8f2e6")]


def _px(img: Image.Image, x: int, y: int, c: Color) -> None:
    if 0 <= x < img.width and 0 <= y < img.height:
        img.putpixel((x, y), c)


def tide_stone() -> Image.Image:
    """Gezeitenstein: körniger, grünblauer Fels; zwei wellige, dunklere Schichten ziehen sich hindurch, dazu Muschelsplitter."""
    img = vs.paint(vs.stone(89101, shares=(0.12, 0.33, 0.37, 0.18)), TIDE)
    rnd = random.Random(89102)
    for base, amp, phase in ((4.5, 1.2, 0.3), (11.5, 1.0, 2.1)):
        for x in range(N):
            y = int(round(base + amp * math.sin(x / N * 2 * math.pi * 2 + phase)))
            c = img.getpixel((x, y % N))
            img.putpixel((x, y % N), pa.mix(c, TIDE_DARK[0], 0.42))
            if rnd.random() < 0.35:
                below = (y + 1) % N
                img.putpixel((x, below), pa.mix(img.getpixel((x, below)), TIDE_DARK[1], 0.35))
    for _ in range(5):
        x, y = rnd.randrange(N), rnd.randrange(N)
        img.putpixel((x, y), pa.mix(img.getpixel((x, y)), SHELL[rnd.randrange(2)], 0.7))
    return img


TIDE_BRICK_ROWS = [(5, [2, 10]), (5, [6, 14]), (6, [3, 11])]


def tide_stone_bricks() -> Image.Image:
    """Gezeitensteinziegel: drei Reihen großer Ziegel, auf einigen ein heller Muschelsplitter."""
    g = vs.bricks(89201, TIDE_BRICK_ROWS)
    img = vs.paint(g, TIDE_BRICK)
    rnd = random.Random(89202)
    for _ in range(3):
        x, y = rnd.randrange(N), rnd.randrange(N)
        if g[y][x] >= 3:
            img.putpixel((x, y), pa.mix(img.getpixel((x, y)), SHELL[rnd.randrange(2)], 0.55))
    return img


def _spiral_cells() -> list[tuple[int, int, float]]:
    """Die Spirale (eine Welle, die sich einrollt): Zellen mit ihrem Abstand vom Anfang (0 innen, 1 außen)."""
    cells: dict[tuple[int, int], float] = {}
    cx, cy = 7.5, 7.0
    steps = 900
    turns = 2.15
    for i in range(steps):
        t = i / (steps - 1)
        theta = t * turns * 2 * math.pi
        r = 0.6 + t * 5.1
        x = int(round(cx + r * math.cos(theta + math.pi)))
        y = int(round(cy + r * math.sin(theta + math.pi)))
        if 2 <= x <= 13 and 2 <= y <= 13:
            cells.setdefault((x, y), t)
    # der Fuß der Welle: vom Ende der Spirale unten nach rechts auslaufend
    for x in range(8, 14):
        cells.setdefault((x, 13), 1.0)
    return [(x, y, t) for (x, y), t in cells.items()]


def chiseled_tide_stone() -> tuple[Image.Image, Image.Image]:
    """
    Gemeißelter Gezeitenstein: eine polierte Platte mit eingelassenem Feld; darin rollt sich eine Welle zur Spirale ein,
    in den Ecken vier Perlen. Spirale und Perlen leuchten (eigene Ebene).
    """
    g = vs.polished(89301, shares=(0.2, 0.45, 0.35))
    for y in range(N):
        for x in range(N):
            g[y][x] = 4 + g[y][x]
    for y in range(1, 15):
        for x in range(1, 15):
            g[y][x] = 2 + (1 if (x + y) % 7 == 0 else 0)
    vs.bevel_frame(g, 1, 1, 14, 14, light=6, dark=0)
    img = vs.paint(g, TIDE_PLATE)
    glow = Image.new("RGBA", (N, N), (0, 0, 0, 0))
    for (x, y, t) in _spiral_cells():
        c = GLOW[3] if t < 0.12 else GLOW[2] if t < 0.4 else GLOW[1]
        img.putpixel((x, y), c)
        glow.putpixel((x, y), c)
        # ein dunkler Schatten unten rechts neben der erhabenen Linie
        if (x + 1, y + 1) not in [(a, b) for a, b, _ in _spiral_cells()] and 2 <= x + 1 <= 13 and 2 <= y + 1 <= 13:
            if img.getpixel((x + 1, y + 1))[:3] not in [c2[:3] for c2 in GLOW]:
                img.putpixel((x + 1, y + 1), pa.mix(img.getpixel((x + 1, y + 1)), TIDE_DARK[0], 0.5))
    for (x, y) in ((3, 3), (12, 3), (3, 12)):
        img.putpixel((x, y), GLOW[2])
        glow.putpixel((x, y), GLOW[2])
    return img, glow


def tide_gate(frames: int = 32) -> Image.Image:
    """
    Der Schleier des Gezeitentors als Bildfolge: Strömungen in Tiefblau und Türkis wogen gegeneinander, auf den Kämmen
    schäumt es gischtweiß, dazwischen steigen Bläschen auf. Leicht durchscheinend.
    """
    sheet = Image.new("RGBA", (N, N * frames))
    rnd = random.Random(89401)
    bubbles = [(rnd.uniform(0, N), rnd.uniform(0, N), rnd.choice((1, 1, 2)), rnd.random()) for _ in range(10)]
    for f in range(frames):
        t = f / frames
        frame = Image.new("RGBA", (N, N))
        px = frame.load()
        for y in range(N):
            for x in range(N):
                u = x / N * 2 * math.pi
                v = y / N * 2 * math.pi
                w1 = math.sin(u + v + t * 2 * math.pi) * 0.5 + 0.5
                w2 = math.sin(2 * u - v - t * 2 * math.pi * 2 + 0.7) * 0.5 + 0.5
                w3 = math.sin(v * 2 + t * 2 * math.pi + math.sin(u + t * 2 * math.pi) * 1.4) * 0.5 + 0.5
                val = 0.12 + 0.5 * w1 * w3 + 0.38 * w2 * w2
                i = val * (len(GATE) - 1.001)
                lo = int(i)
                c = pa.mix(GATE[lo], GATE[min(lo + 1, len(GATE) - 1)], i - lo)
                # Gischt auf den Kämmen
                if w1 * w3 > 0.82 and w2 > 0.55:
                    c = pa.mix(c, hexc("#e8fffb"), 0.6)
                px[x, y] = (c[0], c[1], c[2], 205)
        for (bx, by, size, phase) in bubbles:
            yy = (by - t * N * 1.0) % N
            xx = (bx + math.sin((t * 2 + phase) * 2 * math.pi) * 0.8) % N
            ix, iy = int(xx), int(yy)
            px[ix, iy] = pa.mix(px[ix, iy], hexc("#ffffff"), 0.75)[:3] + (235,)
            if size == 2:
                jx = (ix + 1) % N
                px[jx, iy] = pa.mix(px[jx, iy], hexc("#d8fffa"), 0.5)[:3] + (225,)
        sheet.paste(frame, (0, f * N))
    return sheet


def build(put: Callable[[str, Image.Image], None]) -> None:
    put("block/tide_stone", tide_stone())
    put("block/tide_stone_bricks", tide_stone_bricks())
    chiseled, glow = chiseled_tide_stone()
    put("block/chiseled_tide_stone", chiseled)
    put("block/chiseled_tide_stone_glow", glow)
    put("block/tide_gate", tide_gate())
    ANIMATIONS["block/tide_gate"] = (32, 2, True)
