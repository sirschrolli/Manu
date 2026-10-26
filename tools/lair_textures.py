#!/usr/bin/env python3
"""
Texturen rund um die Drachennester:

  * block/dragon_lair_core (+ _glow): der Nestkern – uralter, fast schwarzer Siegelstein mit eingemeißeltem Siegelkreis,
    dessen Runen glimmen.
  * item/dragon_compass_00 .. _31: der Drachenkompass – Goldfassung, nachtblaues Zifferblatt, eine glühende Nadel in
    32 Stellungen (wie beim Vanilla-Kompass: _16 zeigt nach oben, _24 nach rechts, _00 nach unten, _08 nach links).
  * item/dragon_compass_gem: der Edelstein oben in der Fassung, in Grautönen – das Spiel färbt ihn in der Farbe des
    Elements, dessen Nest der Kompass sucht (Tönung über custom_model_data).

Wird von tools/generate_textures.py aufgerufen (build(put)).
"""
from __future__ import annotations

import math
from typing import Callable

from PIL import Image

import pixelart as pa
from pixelart import Color, hexc

N = 16

# --------------------------------------------------------------------------- Drachenkompass

CX, CY = 7.5, 8.5          # Mitte des Zifferblatts (Pixelmitte von x=7, y=8)
DIAL_R = 4.35
RIM_R = 5.75

GOLD = [hexc("#5a3a10"), hexc("#8c5e1a"), hexc("#c08a2a"), hexc("#e8b84a"), hexc("#fbe39a")]
OUTLINE = hexc("#25160a")
DIAL = [hexc("#0f0d1c"), hexc("#18152b"), hexc("#221e3a")]
TICK = hexc("#6c6488")
NEEDLE_TIP = [hexc("#fff2a8"), hexc("#ffc444"), hexc("#ff7c2a"), hexc("#ea4a1e")]
NEEDLE_TAIL = [hexc("#c4c4dc"), hexc("#9090b0")]
PIVOT = hexc("#e8b84a")

# Edelstein oben in der Fassung (Raute) – (x, y) -> Grauwert für die Tönung
GEM = {(7, 1): 255, (6, 2): 205, (7, 2): 238, (8, 2): 150, (7, 3): 118}


def _compass_base() -> Image.Image:
    img = pa.new()
    px = img.load()
    for y in range(N):
        for x in range(N):
            dx, dy = x + 0.5 - CX, y + 0.5 - CY
            d = math.hypot(dx, dy)
            if d < DIAL_R:
                # Zifferblatt: zur Fassung hin dunkler
                px[x, y] = DIAL[2] if d < 2.2 else DIAL[1] if d < 3.6 else DIAL[0]
            elif d < RIM_R:
                # Goldfassung, Licht von oben links
                nx, ny = dx / max(d, 1e-6), dy / max(d, 1e-6)
                light = -(nx * -0.7071 + ny * -0.7071)   # Außenseite: oben links hell
                inner = d < DIAL_R + 0.75
                if inner:
                    light = -light * 0.8                   # Innenkante umgekehrt (Vertiefung)
                idx = 2 + round(light * 1.6)
                px[x, y] = GOLD[max(0, min(4, idx))]
    img = pa.outline(img, OUTLINE)
    px = img.load()
    # Fassung für den Edelstein
    for (x, y) in GEM:
        px[x, y] = GOLD[1]
    for (x, y) in [(7, 0), (6, 1), (8, 1), (5, 2), (9, 2), (6, 3), (8, 3)]:
        if px[x, y][3] == 0 or px[x, y] == OUTLINE or (x, y) in [(6, 3), (8, 3)]:
            px[x, y] = OUTLINE if (x, y) not in [(6, 3), (8, 3)] else GOLD[3]
    # Gravur: vier Himmelsmarken auf dem Zifferblatt
    for (x, y) in [(7, 5), (11, 8), (7, 12), (3, 8)]:
        px[x, y] = TICK
    # kleine Rautenzeichen auf der Fassung (eingravierte Runen)
    for (x, y) in [(3, 4), (11, 4), (3, 12), (11, 12)]:
        if px[x, y] in GOLD:
            px[x, y] = GOLD[1]
    return img


def _needle(img: Image.Image, frame: int) -> Image.Image:
    out = img.copy()
    px = out.load()
    # Winkel im Uhrzeigersinn ab „oben“: _16 = oben, _24 = rechts, _00 = unten
    a = (frame - 16) / 32.0 * math.tau
    sx, sy = math.sin(a), -math.cos(a)
    tail: dict[tuple[int, int], Color] = {}
    tip: dict[tuple[int, int], Color] = {}
    steps = 40
    for i in range(steps + 1):
        t = -2.5 + (3.55 + 2.5) * i / steps
        x = math.floor(CX + sx * t)
        y = math.floor(CY + sy * t)
        if t < -0.4:
            tail[(x, y)] = NEEDLE_TAIL[0] if t > -1.6 else NEEDLE_TAIL[1]
        elif t > 0.4:
            k = 3 if t < 1.3 else 2 if t < 2.3 else 1 if t < 3.0 else 0
            # das hellste Stück gewinnt (Spitze glüht)
            old = tip.get((x, y))
            if old is None or NEEDLE_TIP.index(old) > k:
                tip[(x, y)] = NEEDLE_TIP[k]
    for pos, c in tail.items():
        if pos not in tip:
            px[pos] = c
    for pos, c in tip.items():
        px[pos] = c
    px[7, 8] = PIVOT
    return out


def compass_frames() -> list[Image.Image]:
    base = _compass_base()
    return [_needle(base, i) for i in range(32)]


def compass_gem() -> Image.Image:
    img = pa.new()
    px = img.load()
    for (x, y), v in GEM.items():
        px[x, y] = (v, v, v, 255)
    return img


# --------------------------------------------------------------------------- Nestkern (Siegelstein)

CORE_STONE = [hexc("#0e0b14"), hexc("#16121f"), hexc("#1e1929"), hexc("#282236"), hexc("#342c44")]
CORE_CARVED = hexc("#3d2a1a")      # eingemeißelte Linien (ohne Leuchten)
CORE_GLOW = [hexc("#ff9a2a"), hexc("#ffc24a"), hexc("#fff0b0")]


def _seal_pixels() -> tuple[set[tuple[int, int]], set[tuple[int, int]]]:
    """Siegelkreis (Ring) und Runenzeichen (Dreieck mit Punkt) – nahtlos am Blockrand vorbei nicht nötig."""
    ring: set[tuple[int, int]] = set()
    marks: set[tuple[int, int]] = set()
    cx, cy = 7.5, 7.5
    for y in range(N):
        for x in range(N):
            d = math.hypot(x + 0.5 - cx - 0.5, y + 0.5 - cy - 0.5)
            if 5.6 <= d < 6.5:
                ring.add((x, y))
    # Dreieck (Spitze oben), drei Punkte auf dem Ring, Mittelpunkt
    tri = [(7, 4), (8, 4), (6, 5), (9, 5), (6, 6), (9, 6), (5, 7), (10, 7), (5, 8), (10, 8), (4, 9), (11, 9)]
    tri += [(x, 10) for x in range(4, 12)]
    marks.update(tri)
    marks.update({(7, 7), (8, 7), (7, 8), (8, 8)})
    return ring, marks


def lair_core() -> Image.Image:
    noise = pa.fractal_noise(4711)
    img = pa.new()
    px = img.load()
    for y in range(N):
        for x in range(N):
            v = noise[y][x]
            px[x, y] = pa.pick(CORE_STONE, 0.15 + v * 0.75)
    # Rand der Steinplatte (leichte Fase): oben/links heller, unten/rechts dunkler
    for i in range(N):
        px[i, 0] = pa.mix(px[i, 0], CORE_STONE[4], 0.5)
        px[0, i] = pa.mix(px[0, i], CORE_STONE[4], 0.4)
        px[i, N - 1] = pa.mix(px[i, N - 1], CORE_STONE[0], 0.6)
        px[N - 1, i] = pa.mix(px[N - 1, i], CORE_STONE[0], 0.5)
    ring, marks = _seal_pixels()
    for pos in ring | marks:
        px[pos] = CORE_CARVED
    return img


def lair_core_glow() -> Image.Image:
    img = pa.new()
    px = img.load()
    ring, marks = _seal_pixels()
    for (x, y) in ring:
        # der Ring glimmt ungleichmäßig (alle paar Pixel heller)
        px[x, y] = CORE_GLOW[1] if (x * 3 + y * 5) % 7 < 2 else CORE_GLOW[0]
    for (x, y) in marks:
        px[x, y] = CORE_GLOW[2] if (x, y) in ((7, 7), (8, 8)) or (x + y) % 4 == 0 else CORE_GLOW[1]
    return img


def build(put: Callable[[str, Image.Image], None]) -> None:
    for i, frame in enumerate(compass_frames()):
        put(f"item/dragon_compass_{i:02d}", frame)
    put("item/dragon_compass_gem", compass_gem())
    put("block/dragon_lair_core", lair_core())
    put("block/dragon_lair_core_glow", lair_core_glow())


if __name__ == "__main__":
    import sys

    sheet: dict[str, Image.Image] = {}
    build(lambda key, img: sheet.__setitem__(key, img))
    zoom, pad, cols = 8, 4, 9
    cell = 16 * zoom + pad
    gem = sheet["item/dragon_compass_gem"]
    tint = hexc("#ff5a1e")
    tinted = gem.copy()
    tp = tinted.load()
    for y in range(N):
        for x in range(N):
            c = tp[x, y]
            if c[3]:
                tp[x, y] = (c[0] * tint[0] // 255, c[1] * tint[1] // 255, c[2] * tint[2] // 255, 255)
    show = []
    for key in [f"item/dragon_compass_{i:02d}" for i in (16, 18, 20, 24, 28, 0, 4, 8, 12)]:
        show.append(pa.compose(sheet[key], tinted))
    show.append(sheet["block/dragon_lair_core"])
    show.append(pa.compose(sheet["block/dragon_lair_core"], sheet["block/dragon_lair_core_glow"]))
    show.append(pa.tiled(pa.compose(sheet["block/dragon_lair_core"], sheet["block/dragon_lair_core_glow"]), 1))
    rows = (len(show) + cols - 1) // cols
    out = Image.new("RGBA", (cols * cell + pad, rows * cell + pad), (60, 64, 70, 255))
    for i, img in enumerate(show):
        tile = pa.checker(16, 16, (150, 150, 150, 255), (128, 128, 128, 255))
        tile.alpha_composite(img)
        out.alpha_composite(pa.zoom(tile, zoom), (pad + (i % cols) * cell, pad + (i // cols) * cell))
    target = sys.argv[1] if len(sys.argv) > 1 else "/tmp/claude-0/-home-claude/50786bb5-6cc3-5907-a25e-13bee43305bb/scratchpad/lair_tex.png"
    out.save(target)
    print(f"{len(sheet)} Texturen -> {target}")
