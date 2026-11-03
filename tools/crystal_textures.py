#!/usr/bin/env python3
"""
Herz- und Manakristalle.

  * block/heart_crystal – geschliffener roter Kristall (Flächen des Herzens im Höhlenkristall, leuchtet im Spiel)
  * block/mana_crystal  – geschliffener blauer Kristall mit kleinen Sternfunken (Spitzen des Manakristalls)
  * item/heart_crystal  – ein geschliffenes Herz aus rotem Kristall, oben links ein Glanzlicht
  * item/mana_crystal   – ein vierzackiger Stern aus blauem Kristall mit hellem Kern

Wird von tools/generate_textures.py aufgerufen (build(put)); allein aufgerufen schreibt es eine Vorschau.
"""
from __future__ import annotations

import math
from typing import Callable

from PIL import Image

import item_style as st
import pixelart as pa

N = 16

RUBY = pa.palette("#4a0614", "#7a0d24", "#a8163a", "#d42a4f", "#ef5470", "#ff8ea0", "#ffd2da")
AZURE = pa.palette("#0b1f4a", "#123a7a", "#1b5fae", "#2e88d8", "#58b2f2", "#9fdcff", "#e4f6ff")

HEART_RAMP = st.ramp("#d42a4f", dark=0.18, light=0.96, chroma_dark=1.1, chroma_light=0.35)
MANA_RAMP = st.ramp("#2e88d8", dark=0.18, light=0.97, chroma_dark=1.1, chroma_light=0.3)


def _gem_surface(pal: list, seed: int, sparkles: bool) -> Image.Image:
    """Geschliffene Kristallfläche: schräge Facetten mit hellen Kanten, im Innern ein sanftes Leuchten."""
    img = pa.new()
    px = img.load()
    noise = pa.fractal_noise(seed, ((8, 0.55), (4, 0.3), (2, 0.15)))
    cells, borders, _ = pa.voronoi(seed + 7, 7, jitter=0.9)
    for y in range(N):
        for x in range(N):
            cell, edge = cells[y][x], borders[y][x]
            # jede Facette hat ihren eigenen Ton, das Innere leuchtet etwas heller
            v = 0.2 + 0.24 * ((cell * 37 % 11) / 10.0) + 0.2 * noise[y][x]
            if edge < 0.9:
                v += 0.26          # helle Schliffkante
            elif edge < 1.6:
                v -= 0.07          # dunkler Saum neben der Kante
            px[x, y] = pa.pick(pal[1:], min(0.99, max(0.0, v)))
    if sparkles:
        for (x, y) in ((3, 4), (11, 2), (12, 11), (6, 13)):
            px[x, y] = pal[6]
            for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                if 0 <= x + dx < N and 0 <= y + dy < N:
                    px[x + dx, y + dy] = pal[5]
    else:
        for (x, y) in ((4, 3), (12, 9)):
            px[x, y] = pal[6]
    return img


def heart_crystal_block() -> Image.Image:
    return _gem_surface(RUBY, 4711, False)


def mana_crystal_block() -> Image.Image:
    return _gem_surface(AZURE, 2903, True)


def _shade(mask: set[tuple[int, int]], cx: float, cy: float, facets: Callable[[float, float], float]) -> st.Spr:
    """Füllt die Maske mit Stufen 1–6 nach Facetten (Licht von oben links), dunkler zum Rand."""
    spr = st.Spr()
    for (x, y) in mask:
        dx, dy = x + 0.5 - cx, y + 0.5 - cy
        level = facets(dx, dy)
        # Rand: eine Stufe dunkler, wenn ein Nachbar außerhalb liegt (unten/rechts zwei)
        if (x + 1, y) not in mask or (x, y + 1) not in mask:
            level -= 1.2
        elif (x - 1, y) not in mask or (x, y - 1) not in mask:
            level -= 0.4
        spr.set(x, y, ("K", st.clamp(round(level), 1, 6)))
    return spr


def heart_crystal_item() -> Image.Image:
    """Ein geschliffenes Herz: oben links hell, unten rechts dunkel, in der Mitte eine helle Tafel, ein Glanzpunkt."""
    cx, cy = 7.5, 7.6
    mask = set()
    for y in range(N):
        for x in range(N):
            u = (x + 0.5 - cx) / 6.6
            v = -(y + 0.5 - cy) / 6.4 + 0.18
            if (u * u + v * v - 1) ** 3 - u * u * v ** 3 <= 0:
                mask.add((x, y))

    def facets(dx: float, dy: float) -> float:
        a = math.atan2(dy, dx)
        r = math.hypot(dx, dy)
        if r < 2.2 and dy < 1.5:
            return 5.0                      # Tafel
        light = math.cos(a + math.pi * 0.75)    # oben links = 1
        return 3.6 + 1.6 * light - 0.12 * r

    spr = _shade(mask, cx, cy, facets)
    # Glanzlicht oben links auf der linken Wölbung, ein kleiner Funke rechts
    for (x, y, lvl) in ((4, 4, 6), (5, 4, 6), (4, 5, 6), (3, 5, 5), (10, 5, 6)):
        if (x, y) in mask:
            spr.set(x, y, ("K", lvl))
    return spr.outline(prefer=("K",)).image({"K": HEART_RAMP})


def mana_crystal_item() -> Image.Image:
    """
    Ein vierzackiger Stern aus blauem Kristall: Jede Zacke hat eine helle und eine dunkle Flanke (Licht von oben links),
    in der Mitte ein heller Kern, ein Funke an der oberen Zacke.
    """
    cx, cy = 7.5, 7.5
    mask = set()
    for y in range(N):
        for x in range(N):
            dx, dy = abs(x + 0.5 - cx), abs(y + 0.5 - cy)
            if dx ** 0.72 + dy ** 0.72 <= 7.4 ** 0.72 and max(dx, dy) <= 7.4:
                mask.add((x, y))

    def facets(dx: float, dy: float) -> float:
        r = math.hypot(dx, dy)
        if r < 1.8:
            return 6.0                      # Kern
        if abs(dx) >= abs(dy):              # waagerechte Zacke: obere Flanke hell
            lit = dy < 0
        else:                               # senkrechte Zacke: linke Flanke hell
            lit = dx < 0
        return (4.7 if lit else 2.6) - 0.08 * r

    spr = _shade(mask, cx, cy, facets)
    for (x, y, lvl) in ((7, 2, 6), (6, 3, 5), (3, 6, 5)):
        if (x, y) in mask:
            spr.set(x, y, ("K", lvl))
    return spr.outline(prefer=("K",)).image({"K": MANA_RAMP})


def build(put: Callable[[str, Image.Image], None]) -> None:
    put("block/heart_crystal", heart_crystal_block())
    put("block/mana_crystal", mana_crystal_block())
    put("item/heart_crystal", heart_crystal_item())
    put("item/mana_crystal", mana_crystal_item())


if __name__ == "__main__":
    import sys

    sheet: dict[str, Image.Image] = {}
    build(lambda key, img: sheet.__setitem__(key.replace("/", "_"), img))
    st.preview(sheet, sys.argv[1] if len(sys.argv) > 1 else "crystals.png", zoom=12, cols=4)
