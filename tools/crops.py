#!/usr/bin/env python3
"""
Feldfrüchte im Vanilla-Stil: dichte Pflanzen, die die ganze Kachel füllen (wie Weizen, Karotten oder Kartoffeln),
mit klarer Licht-Schatten-Seite an jedem Halm und Blatt.

  * Gerste und Dämmerkorn: viele Halme, in der letzten Stufe Ähren mit langen Grannen
  * Zwiebeln und Knoblauch: röhrige bzw. flache Blätter, in der letzten Stufe die Knollen am Boden
  * Kohl: von Hand gezeichnet, vom Keimling bis zum festen Kopf
  * Tomaten, Trauben, Hopfen: rankende Pflanzen an einem Stab, mit Früchten

Alle Stufen entstehen aus demselben Startwert, so wächst die Pflanze von Stufe zu Stufe sichtbar weiter.
Wird von tools/generate_textures.py aufgerufen: build(put).
"""
from __future__ import annotations

import random
from typing import Callable

from PIL import Image

import pixelart as pa
from pixelart import Color, hexc

N = 16
CLEAR = (0, 0, 0, 0)

STEM = [hexc("#2a5a1c"), hexc("#3f7a26"), hexc("#5a9a32"), hexc("#7cbc44")]
LEAF = [hexc("#24501a"), hexc("#356f22"), hexc("#4c8e2c"), hexc("#6cae3c"), hexc("#90cc56")]


class Canvas:
    def __init__(self) -> None:
        self.img = Image.new("RGBA", (N, N), CLEAR)

    def put(self, x: int, y: int, c: Color, over: bool = True) -> None:
        if 0 <= x < N and 0 <= y < N and (over or self.img.getpixel((x, y))[3] == 0):
            self.img.putpixel((x, y), c)

    def get(self, x: int, y: int) -> Color:
        return self.img.getpixel((x, y)) if 0 <= x < N and 0 <= y < N else CLEAR


# =============================================================================
# Getreide (Gerste, Dämmerkorn)
# =============================================================================

def grain(stage: int, seed: int, stem: list[Color], ear: list[Color], awn: Color, ripe_stem: list[Color]) -> Image.Image:
    """
    Getreide wie Vanilla-Weizen: viele dünne Halme (1 Pixel) in unregelmäßigem Abstand, die sich leicht neigen, dazu
    schmale Blätter; ab Stufe 2 Ähren aus hellen und dunklen Körnern im Wechsel, in der letzten Stufe reif mit Grannen.
    """
    rnd = random.Random(seed)
    c = Canvas()
    count = [4, 7, 9, 9][stage]
    heights = [(2, 4), (6, 9), (10, 13), (11, 15)][stage]
    colors = ripe_stem if stage == 3 else stem
    xs = sorted(rnd.sample(range(0, N), count))
    for i, x0 in enumerate(xs):
        h = rnd.randint(*heights)
        lean = rnd.choice((-1, 1)) if rnd.random() < 0.6 else 0
        bend = rnd.randint(h // 3, h - 2) if lean else h
        bend2 = rnd.randint(bend + 1, h) if lean and bend + 1 < h else h
        x = x0
        top = N - h
        for s_ in range(h):
            y = N - 1 - s_
            if s_ == bend or s_ == bend2:
                x += lean
            c.put(x, y, colors[2] if (y + i) % 3 else colors[1])
        if stage in (1, 2) and rnd.random() < 0.75:
            ly = N - 1 - rnd.randint(2, max(3, h // 2 + 1))
            d = rnd.choice((-1, 1))
            for k in range(1, 3 + (1 if stage == 2 else 0)):
                c.put(x0 + d * k, ly - k // 2, LEAF[2] if k < 2 else LEAF[3], over=False)
        if stage == 0:
            c.put(x, top, colors[3])
        elif stage >= 2:
            length = 4 if stage == 3 else 3
            for k in range(length):
                y = top + k
                c.put(x, y, ear[2] if k % 2 == 0 else ear[1])
                if k % 2 == 0:
                    c.put(x + 1, y, ear[1], over=False)
                else:
                    c.put(x - 1, y, ear[0], over=False)
            c.put(x, top - 1, ear[3])
            if stage == 3:
                c.put(x, top - 2, awn, over=False)
                c.put(x + 1, top - 3, awn, over=False)
    return c.img


BARLEY_STEM = [hexc("#3d6e22"), hexc("#4f8a2a"), hexc("#6aa83a"), hexc("#8cc85a")]
BARLEY_RIPE = [hexc("#5e4c1c"), hexc("#7c6828"), hexc("#9a8436"), hexc("#bba24e")]
BARLEY_EAR_GREEN = [hexc("#4a7a2a"), hexc("#6e9e3a"), hexc("#9aca5a"), hexc("#c4e08a")]
BARLEY_EAR = [hexc("#8a6a22"), hexc("#c09a3a"), hexc("#e6c45e"), hexc("#fbe8a0")]
BARLEY_AWN = hexc("#e8d496")

DUSK_STEM = [hexc("#2e3a5a"), hexc("#3f4e78"), hexc("#566a98"), hexc("#7a8cbc")]
DUSK_RIPE = [hexc("#2c2650"), hexc("#3e366a"), hexc("#524888"), hexc("#6c60a6")]
DUSK_EAR_GREEN = [hexc("#3a3a6a"), hexc("#5a5a92"), hexc("#7c7cba"), hexc("#a8a8dc")]
DUSK_EAR = [hexc("#5a3a8a"), hexc("#8a5ac0"), hexc("#c49af0"), hexc("#f2e0ff")]
DUSK_AWN = hexc("#e6d2ff")


def barley(stage: int) -> Image.Image:
    return grain(stage, 61001, BARLEY_STEM, BARLEY_EAR_GREEN if stage < 3 else BARLEY_EAR, BARLEY_AWN, BARLEY_RIPE)


def duskgrain(stage: int) -> Image.Image:
    return grain(stage, 61101, DUSK_STEM, DUSK_EAR_GREEN if stage < 3 else DUSK_EAR, DUSK_AWN, DUSK_RIPE)


# =============================================================================
# Zwiebeln und Knoblauch
# =============================================================================

def bulbs(stage: int, seed: int, leaf: list[Color], bulb: list[Color], flat: bool) -> Image.Image:
    """
    Zwiebelgewächse: aus jeder Knolle fächern schlanke Blätter auf (röhrig: dünn und gerade, flach: etwas breiter und
    stärker gebogen), Licht von links; in der letzten Stufe schauen die Knollen am Boden heraus.
    """
    rnd = random.Random(seed)
    c = Canvas()
    centers = [3, 8, 13] if stage >= 1 else [4, 11]
    heights = [(3, 4), (6, 8), (9, 11), (10, 12)][stage]
    ground = N - 3 if stage == 3 else N - 1
    for cx in centers:
        n = 2 if stage == 0 else 3 if stage == 1 else 4
        for k in range(n):
            spread = (k - (n - 1) / 2)
            h = rnd.randint(*heights) - abs(round(spread))
            curve = spread * (0.9 if flat else 0.6)
            for sidx in range(h):
                t = sidx / max(1, h - 1)
                y = ground - sidx
                x = round(cx + curve * t * t * 2.2)
                c.put(x, y, leaf[3] if spread <= 0 else leaf[2])
                if flat and t < 0.75:
                    c.put(x + (1 if spread >= 0 else -1), y, leaf[1], over=False)
            tx = round(cx + curve * 2.2)
            c.put(tx, ground - h, leaf[4])
        if stage == 3:
            shape = ((0, 1, 1, 1, 0), (1, 1, 1, 1, 1), (0, 1, 1, 1, 0))
            for dy, row in enumerate(shape):
                for dx, v in enumerate(row):
                    if not v:
                        continue
                    x, y = cx - 2 + dx, N - 3 + dy
                    shade = bulb[1]
                    if dy == 0 or dx <= 1:
                        shade = bulb[2]
                    if dy == 2 or dx == 4:
                        shade = bulb[0]
                    c.put(x, y, shade)
            c.put(cx - 1, N - 3, bulb[3])
            c.put(cx, N - 4, leaf[1])
    return c.img


ONION_LEAF = [hexc("#24501a"), hexc("#2f6a22"), hexc("#3f8a2c"), hexc("#5aaa3c"), hexc("#86cc5a")]
ONION_BULB = [hexc("#7a4a1a"), hexc("#b07a32"), hexc("#d8a24a"), hexc("#f6d488")]
GARLIC_LEAF = [hexc("#2a5a26"), hexc("#3a7432"), hexc("#4e9442"), hexc("#6cb05a"), hexc("#9ad084")]
GARLIC_BULB = [hexc("#8a7a9a"), hexc("#c8c0d2"), hexc("#ece8f2"), hexc("#ffffff")]


def onions(stage: int) -> Image.Image:
    return bulbs(stage, 62001, ONION_LEAF, ONION_BULB, flat=False)


def garlic(stage: int) -> Image.Image:
    return bulbs(stage, 62101, GARLIC_LEAF, GARLIC_BULB, flat=True)


# =============================================================================
# Kohl
# =============================================================================

# Kohl ist von Hand gezeichnet (die runde Kopfform liest sich so am besten)
CABBAGE_STAGES = [
    [
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "...L.L....L.L...",
        "....G......G....",
        "...lGl....lGl...",
        "....g......g....",
    ],
    [
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "................",
        "..L.L.....L..L..",
        ".lGlGl...lGllGl.",
        "lGgGGGl.lGgGGgGl",
        ".GgCgG...GgCgGg.",
        "..ggg.....ggg...",
    ],
    [
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "................",
        "..L..L.....L..L.",
        ".lGl.Gl...lGl.Gl",
        "lGgGlGGl.lGgGlGG",
        "GgGCCCgGlGgCCCgG",
        "gGCwCCcGgGCwCCcG",
        ".gGCCcgg.gGCCcgg",
        "..gGGgg...gGGgg.",
        "...ggg.....ggg..",
    ],
    [
        "",
        "",
        "",
        "",
        "",
        "....L....L......",
        "...lGl..lGl.....",
        "..lGgGllGgGl....",
        ".lGgCCCCCgGgl...",
        ".GgCwwCCCcgGl...",
        "lGCwCCvCCCcgGl..",
        "GgCCvCCCvCcgGg..",
        "gGcCCCvCCCcgGg..",
        ".gGcCCCCCcggg...",
        "..gGgcccgGgg....",
        "...gggGggg......",
    ],
]
CABBAGE_COLORS = {"C": hexc("#a8dc7c"), "G": hexc("#3c842a"), "L": hexc("#7cc24e"), "c": hexc("#7fbe52"), "g": hexc("#2b6420"), "l": hexc("#56a238"), "v": hexc("#5e9a3e"), "w": hexc("#d6f0b8")}


def cabbage(stage: int) -> Image.Image:
    rows = [r if r else "." * N for r in CABBAGE_STAGES[stage]]
    return pa.sprite(rows, CABBAGE_COLORS)


# =============================================================================
# Rankende Pflanzen an einem Stab (Tomaten, Trauben, Hopfen)
# =============================================================================

STAKE = [hexc("#4a3218"), hexc("#6e4c26"), hexc("#94703e")]


def _stake(c: Canvas, x: int, top: int) -> None:
    for y in range(top, N):
        c.put(x, y, STAKE[1] if y % 3 else STAKE[0])
        c.put(x + 1, y, STAKE[0])
    c.put(x, top, STAKE[2])


def _leaf(c: Canvas, x: int, y: int, d: int, size: int, pal: list[Color]) -> None:
    """Ein Blatt: von (x, y) aus schräg nach außen, in der Mitte 2 Pixel breit, Spitze hell."""
    for k in range(size):
        lx = x + d * (k + 1)
        ly = y - (k // 2)
        c.put(lx, ly, pal[2])
        if 0 < k < size - 1:
            c.put(lx, ly + 1, pal[1])
    c.put(x + d * size, y - ((size - 1) // 2), pal[3])


def vine(stage: int, seed: int, fruit: Callable[[Canvas, int, int, random.Random], None] | None, leaf: list[Color]) -> Image.Image:
    """
    Rankpflanze: zwei Stäbe, an denen sich je eine Ranke im Zickzack hochwindet, mit wechselständigen Blättern;
    ab Stufe 2 Blüten bzw. Früchte (über {@code fruit}).
    """
    rnd = random.Random(seed)
    c = Canvas()
    heights = [5, 10, 14, 15][stage]
    for sx in (3, 11):
        if stage >= 1:
            _stake(c, sx, N - heights - 1 if stage >= 2 else N - heights)
        x = sx + 2
        for s in range(heights):
            y = N - 1 - s
            x = sx + 2 + (1 if (s // 2) % 2 else 0) - (3 if stage >= 1 and (s // 4) % 2 else 0)
            x = max(sx - 1, min(sx + 3, x))
            c.put(x, y, STEM[1] if s % 3 else STEM[2])
            if s % 3 == 1 and s > 1:
                d = 1 if (s // 3) % 2 else -1
                _leaf(c, x, y, d, 2 + (1 if stage >= 2 else 0), leaf)
        if stage >= 2 and fruit is not None:
            for k in range(2 if stage == 2 else 3):
                fy = N - 4 - k * 4 - rnd.randint(0, 1)
                fx = sx + (-2 if k % 2 else 3)
                fruit(c, fx, fy, rnd)
    return c.img


TOMATO_LEAF = [hexc("#245a1c"), hexc("#357a26"), hexc("#4c9a34"), hexc("#74c050")]
TOMATO = [hexc("#7a1210"), hexc("#c02a1c"), hexc("#ec5232"), hexc("#ffb08a")]
TOMATO_GREEN = [hexc("#4a7a22"), hexc("#7aa83a"), hexc("#a8cc5a"), hexc("#d4ec9a")]


def _tomato(ripe: bool) -> Callable[[Canvas, int, int, random.Random], None]:
    pal = TOMATO if ripe else TOMATO_GREEN

    def draw(c: Canvas, x: int, y: int, rnd: random.Random) -> None:
        for dx, dy, v in ((0, 0, 2), (1, 0, 1), (0, 1, 1), (1, 1, 0), (-1, 1, 1), (-1, 0, 2)):
            c.put(x + dx, y + dy, pal[v])
        c.put(x - 1, y, pal[3])
        c.put(x, y - 1, STEM[2])
    return draw


GRAPE = [hexc("#2a0f3a"), hexc("#4a1c62"), hexc("#6e2e8e"), hexc("#a464c8")]


def _grapes(c: Canvas, x: int, y: int, rnd: random.Random) -> None:
    rows = ((-1, 0, 1), (-1, 0, 1), (0, 1), (0,))
    for dy, xs in enumerate(rows):
        for dx in xs:
            v = 2 if (dx + dy) % 2 == 0 else 1
            c.put(x + dx, y + dy, GRAPE[v])
    c.put(x - 1, y, GRAPE[3])
    c.put(x, y + 3, GRAPE[0])
    c.put(x, y - 1, STEM[1])


HOP = [hexc("#5a7a2a"), hexc("#86a83c"), hexc("#b4d264"), hexc("#e2f2a8")]


def _hop(c: Canvas, x: int, y: int, rnd: random.Random) -> None:
    for dy, (xs, v) in enumerate((((0,), 3), ((-1, 0), 2), ((-1, 0), 2), ((0,), 1))):
        for dx in xs:
            c.put(x + dx, y + dy, HOP[v if dx == 0 else v - 1])
    c.put(x, y - 1, STEM[1])


def tomatoes(stage: int) -> Image.Image:
    return vine(stage, 63001, _tomato(stage == 3), TOMATO_LEAF)


def grapes(stage: int) -> Image.Image:
    return vine(stage, 63101, _grapes if stage == 3 else None, LEAF)


def hops(stage: int) -> Image.Image:
    return vine(stage, 63201, _hop if stage >= 2 else None, LEAF)


# =============================================================================
# Alles zusammen
# =============================================================================

def build(put: Callable[[str, Image.Image], None]) -> None:
    for stage in range(4):
        put(f"block/barley_stage{stage}", barley(stage))
        put(f"block/onions_stage{stage}", onions(stage))
        put(f"block/garlic_crop_stage{stage}", garlic(stage))
        put(f"block/cabbages_stage{stage}", cabbage(stage))
        put(f"block/tomatoes_stage{stage}", tomatoes(stage))
        put(f"block/grapes_stage{stage}", grapes(stage))
        put(f"block/hops_stage{stage}", hops(stage))
        put(f"block/duskgrain_stage{stage}", duskgrain(stage))


if __name__ == "__main__":
    import sys

    import vanilla_style as vs

    sheet: dict[str, Image.Image] = {}
    build(lambda key, img: sheet.__setitem__(key.split("/")[-1], img))
    vs.preview(sheet, sys.argv[1] if len(sys.argv) > 1 else "crops.png", zoom=6, tile=1, cols=8)
