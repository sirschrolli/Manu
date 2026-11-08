#!/usr/bin/env python3
"""
Herz- und Manakristalle.

  * block/heart_crystal     – das geschliffene Herz von vorn, dazu Schliffkanten und die Flächen der Splitter
  * block/heart_crystal_tip – Dreieck für die Pyramidenspitzen der Splitter (außen durchsichtig)
  * block/mana_crystal      – Atlas der Kristallsäulen: helle, dunkle und mittlere Fläche, Kopf
  * block/mana_crystal_tip  – Dreieck für die Pyramidenspitzen der Manakristalle
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

RUBY = pa.palette("#3a0310", "#66081d", "#93112c", "#bd1b3d", "#e03454", "#ff6e86", "#ffc6cf")
AZURE = pa.palette("#0a1a40", "#0f316c", "#1955a3", "#2a80d3", "#53acf0", "#9adaff", "#e6f7ff")

HEART_RAMP = st.ramp("#d42a4f", dark=0.18, light=0.96, chroma_dark=1.1, chroma_light=0.35)
MANA_RAMP = st.ramp("#2e88d8", dark=0.18, light=0.97, chroma_dark=1.1, chroma_light=0.3)

# Das Herz von vorn, Ton 0 (dunkel) bis 6 (hell): helle Bögen, eine helle Tafel in der Mitte, zur Spitze hin dunkler.
# Es ist spiegelgleich – das Modell zeigt es vorn und hinten. Die Zeilen und Spannen passen genau zu den Schichten in
# datagen/CrystalModels.java (Pixelzeile r = Modellhöhe 15 - r bis 16 - r, das Herz steht also von Höhe 2 bis 13).
HEART_ART = [
    "................",
    "................",
    "................",
    "....34....43....",
    "...2564..4652...",
    "..245543345542..",
    "..244556655442..",
    "..134455554431..",
    "...1334554331...",
    "....12344321....",
    ".....123321.....",
    "......1221......",
    ".......11.......",
    ".......00.......",
]


def heart_crystal_block() -> Image.Image:
    """
    Das Herzbild in der Mitte; Zeile 0 die hellen Schliffkanten (Oberseiten), Zeile 15 die dunklen (Unterseiten),
    Spalte 1 und 14 die Seitenkanten, Spalten 0–1 und 14–15 die hellen und dunklen Flächen der Splitter.
    """
    img = pa.new(RUBY[3])
    px = img.load()
    for x in range(16):
        px[x, 0] = RUBY[5] if x % 5 else RUBY[6]
        px[x, 15] = RUBY[2] if x % 4 else RUBY[1]
    for r in range(1, 15):
        px[0, r] = RUBY[6] if r < 3 else RUBY[5] if r < 7 else RUBY[4]
        px[1, r] = RUBY[5] if r < 5 else RUBY[4] if r < 10 else RUBY[3]
        px[14, r] = RUBY[4] if r < 4 else RUBY[3] if r < 9 else RUBY[2]
        px[15, r] = RUBY[3] if r < 6 else RUBY[2] if r < 11 else RUBY[1]
    for r, row in enumerate(HEART_ART):
        for x, ch in enumerate(row):
            if ch != ".":
                px[x, r] = RUBY[int(ch)]
    return img


def mana_crystal_block() -> Image.Image:
    """
    Atlas der Kristallsäulen: Spalten 0–3 helle Fläche, 4–7 dunkle, 8–11 Kopf, 12–15 mittlere; jede oben hell und nach
    unten dunkler, links eine helle Schliffkante, rechts ein dunkler Saum, dazu ein paar Funken.
    """
    img = pa.new(AZURE[3])
    px = img.load()
    columns = {0: (5.2, 3.0), 4: (3.9, 1.2), 12: (4.6, 2.0)}   # (oben, unten)
    for r in range(16):
        t = r / 15.0
        for c0, (top, bottom) in columns.items():
            for i in range(4):
                v = top + (bottom - top) * t + (0.75 if i == 0 else -0.45 if i == 3 else 0.0)
                px[c0 + i, r] = AZURE[max(0, min(6, round(v)))]
        for i in range(4):
            v = 5.6 - t * 1.4 - (0.5 if i in (0, 3) else 0.0)
            px[8 + i, r] = AZURE[max(0, min(6, round(v)))]
    for (x, r) in ((1, 2), (9, 1), (13, 4), (2, 8), (5, 3)):
        px[x, r] = AZURE[6]
    return img


def crystal_tip(pal: list) -> Image.Image:
    """
    Eine Fläche der Pyramidenspitze: ein Dreieck (Spitze oben in der Mitte, Grundlinie unten über die ganze Breite),
    außen herum durchsichtig; oben hell, helle Kanten, ein Grat zur Spitze.
    """
    img = pa.new()
    px = img.load()
    for r in range(N):
        half = (r + 1) / 2.0
        for x in range(N):
            d = abs(x + 0.5 - 8.0)
            if d > half:
                continue
            v = 5.4 - r / 15.0 * 1.6
            if d > half - 1.0:
                v += 0.8
            if d < 1.0 and r < 9:
                v += 0.5
            px[x, r] = pal[max(0, min(6, round(v)))]
    return img


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
    put("block/heart_crystal_tip", crystal_tip(RUBY))
    put("block/mana_crystal", mana_crystal_block())
    put("block/mana_crystal_tip", crystal_tip(AZURE))
    put("item/heart_crystal", heart_crystal_item())
    put("item/mana_crystal", mana_crystal_item())


if __name__ == "__main__":
    import sys

    sheet: dict[str, Image.Image] = {}
    build(lambda key, img: sheet.__setitem__(key.replace("/", "_"), img))
    st.preview(sheet, sys.argv[1] if len(sys.argv) > 1 else "crystals.png", zoom=12, cols=4)
