#!/usr/bin/env python3
"""
Texturen des Nebelmoors (eigenes Oberwelt-Biom):

  * block/peat          – Torf: dunkelbraune, faserige Moorerde mit hellen Pflanzenfasern und nassen, dunklen Stellen
  * block/reeds_bottom  – Schilf, untere Hälfte: Halme und lange, sich biegende Blätter (Kreuzpflanze)
  * block/reeds_top     – Schilf, obere Hälfte: die Halme mit braunen Kolben und hellen Spitzen

Wird von tools/generate_textures.py aufgerufen (build(put)).
"""
from __future__ import annotations

import random
from typing import Callable

from PIL import Image

import vanilla_style as vs
from pixelart import Color, hexc

N = 16

PEAT = vs.tones("#3d2c20", 5, spread=0.30, cool=10, warm=35, hue=6, sat=0.05)
PEAT_FIBER = [hexc("#5c4630"), hexc("#7a6040"), hexc("#94784e")]
PEAT_WET = hexc("#21170f")

STALK = [hexc("#3d5a26"), hexc("#55772f"), hexc("#6f9139")]
LEAF = [hexc("#3a5522"), hexc("#557530"), hexc("#7b9c45"), hexc("#a3b863")]
CATTAIL = [hexc("#3f2614"), hexc("#5e3a1d"), hexc("#7d5028"), hexc("#9a6a38")]
SPIKE = [hexc("#b8a878"), hexc("#d8cca0")]


def peat() -> Image.Image:
    """Torf: körnige, dunkle Erde; darin kurze, schräge Fasern aus halb vermoderten Halmen und ein paar nasse Flecken."""
    g = vs.soil(61001, pebbles=0)
    img = vs.paint([[min(4, v) for v in row] for row in g], PEAT)
    rnd = random.Random(61002)
    # Fasern: zwei bis vier Pixel lang, waagerecht oder leicht schräg, hell an einem Ende
    for _ in range(11):
        x, y = rnd.randrange(N), rnd.randrange(N)
        length = rnd.randint(2, 4)
        dy = rnd.choice((0, 0, 1, -1))
        for i in range(length):
            px, py = (x + i) % N, (y + (i * dy) // 2) % N
            img.putpixel((px, py), PEAT_FIBER[min(2, (i * 2) // length + rnd.randint(0, 1))])
    # nasse, dunkle Stellen
    for _ in range(4):
        x, y = rnd.randrange(N), rnd.randrange(N)
        img.putpixel((x, y), PEAT_WET)
        if rnd.random() < 0.6:
            img.putpixel(((x + 1) % N, y), PEAT[0])
    return img


def _draw_stalk(img: Image.Image, x0: float, lean: float, y_from: int, y_to: int, shade: int) -> None:
    """Ein Halm von unten (y_from) nach oben (y_to), leicht geneigt; zwei Töne wechseln sich ab (Knoten)."""
    for y in range(y_from, y_to - 1, -1):
        t = (y_from - y)
        x = int(round(x0 + lean * t))
        if 0 <= x < N:
            tone = shade if (t // 3) % 2 == 0 else max(0, shade - 1)
            img.putpixel((x, y), STALK[tone])


def _draw_leaf(img: Image.Image, x0: int, y0: int, direction: int, length: int, droop: int) -> None:
    """Ein langes, schmales Blatt: steigt vom Fuß schräg auf und biegt sich zum Ende hin zur Seite (droop: wie stark)."""
    x, y = float(x0), y0
    for i in range(length):
        t = i / max(1, length - 1)
        x += direction * (0.18 + 0.8 * t * t * droop / 3)
        if t < 0.88 or droop < 2:
            y -= 1
        px = int(round(x))
        if 0 <= px < N and 0 <= y < N:
            img.putpixel((px, y), LEAF[1] if t < 0.3 else (LEAF[2] if t < 0.8 else LEAF[3]))


# Halme: (x am Fuß, Neigung je Pixel, Farbton); gemeinsam für unten und oben, damit sie durchlaufen
STALKS = [(3.0, 0.05, 1), (6.0, -0.06, 2), (8.0, 0.03, 1), (11.0, 0.08, 2), (13.0, -0.04, 1)]


def reeds_bottom() -> Image.Image:
    img = Image.new("RGBA", (N, N), (0, 0, 0, 0))
    # zuerst die Blätter (hinten), dann die Halme davor
    _draw_leaf(img, 3, 15, -1, 13, 2)
    _draw_leaf(img, 12, 15, 1, 14, 2)
    _draw_leaf(img, 9, 15, -1, 15, 1)
    for x0, lean, shade in STALKS:
        _draw_stalk(img, x0, lean, 15, 0, shade)
    # am Fuß etwas dunkler (feucht)
    for x in range(N):
        if img.getpixel((x, 15))[3]:
            img.putpixel((x, 15), STALK[0])
    return img


def reeds_top() -> Image.Image:
    img = Image.new("RGBA", (N, N), (0, 0, 0, 0))
    # zwei Blattspitzen, die sich oben zur Seite biegen
    _draw_leaf(img, 2, 15, -1, 9, 3)
    _draw_leaf(img, 14, 15, 1, 7, 3)
    # die Halme setzen sich fort (jeder endet anders hoch)
    heights = [4, 7, 3, 6, 9]
    for (x0, lean, shade), top in zip(STALKS, heights):
        _draw_stalk(img, x0 + lean * 16, lean, 15, top, shade)
    # Kolben an drei Halmen: zwei Pixel breit, fünf, sechs hoch, darüber eine helle Spitze
    for index, y_top, length in ((0, 6, 5), (2, 4, 6), (4, 9, 5)):
        x0, lean, _ = STALKS[index]
        x = int(round(x0 + lean * 16 + lean * (15 - y_top)))
        x = min(x, N - 2)
        for i in range(length):
            y = y_top + i
            tone = 3 if i == 1 else (2 if i < length - 1 else 1)
            img.putpixel((x, y), CATTAIL[tone])
            img.putpixel((x + 1, y), CATTAIL[max(0, tone - 1)])
        for i, y in enumerate(range(y_top - 1, y_top - 4, -1)):
            if y >= 0:
                img.putpixel((x, y), SPIKE[1 if i == 0 else 0])
    return img


def build(put: Callable[[str, Image.Image], None]) -> None:
    put("block/peat", peat())
    put("block/reeds_bottom", reeds_bottom())
    put("block/reeds_top", reeds_top())
