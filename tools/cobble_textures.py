#!/usr/bin/env python3
"""
Bruchstein aus dem eigenen Gestein – wie Bruchstein aus Stein in Vanilla: runde, verschieden große Brocken mit dunklen
Fugen, jeder oben links heller und unten rechts im Schatten. Farben und Eigenheiten kommen von dem Fels, aus dem die
Brocken geschlagen wurden:

  * block/cobbled_stormstone        – Sturmbruchstein: blaugraue Brocken, einzelne mit hellem Windschliff
  * block/cobbled_volcanic_rock     – Vulkanbruchstein: schwarzbraune, porige Brocken
  * block/cobbled_rime_stone        – Raureifbruchstein: hellgraue, bläuliche Brocken mit Reifkörnern
  * block/cobbled_shale             – Schieferbruchstein: flache, geschichtete Platten
  * block/cobbled_abyssal_stone     – Abgrundbruchstein: blaugrüne Brocken mit Muschelsplittern
  * block/cobbled_shadow_stone      – Schattenbruchstein: violettgraue Brocken
  * block/cobbled_deep_shadow_stone – Tiefschattenbruchstein: kleine, kantige, fast schwarze Brocken (wie Bruchtiefenschiefer)

Wird von tools/generate_textures.py aufgerufen: build(put).
"""
from __future__ import annotations

import math
import random
from typing import Callable, Sequence

from PIL import Image

import vanilla_style as vs
from pixelart import Color, hexc

N = 16


# =============================================================================
# Brocken
# =============================================================================

def _torus(a: float, b: float) -> float:
    d = abs(a - b) % N
    return min(d, N - d)


def cobble(seed: int, stones: int = 9, sx: float = 0.9, sy: float = 1.1, angular: float = 0.0, joint: float = 0.62,
           min_gap: float = 3.4, grain: float = 0.45) -> tuple[vs.Grid, list[list[int]]]:
    """
    Brocken als Stufenraster (0 = Fuge, 1 = Schatten … 5 = Licht) und daneben, welcher Brocken wo liegt (−1 = Fuge).
    {@code sx}/{@code sy} strecken die Brocken (flach: sx klein), {@code angular} macht sie kantiger, {@code grain}
    gibt der Oberfläche Korn.
    """
    rnd = random.Random(seed)
    pts: list[tuple[float, float, float]] = []
    tries = 0
    while len(pts) < stones and tries < 4000:
        tries += 1
        x, y = rnd.uniform(0, N), rnd.uniform(0, N)
        if all(math.hypot(_torus(x, p[0]) * sx, _torus(y, p[1]) * sy) > min_gap for p in pts):
            pts.append((x, y, rnd.random()))
    noise = vs.white(seed + 7)
    g = vs.grid(0)
    owner_of = [[-1] * N for _ in range(N)]
    for y in range(N):
        for x in range(N):
            best, second, owner = 1e9, 1e9, -1
            for i, p in enumerate(pts):
                dx = _torus(x + 0.5, p[0]) * sx
                dy = _torus(y + 0.5, p[1]) * sy
                d = (1 - angular) * math.hypot(dx, dy) + angular * max(dx, dy) * 1.12
                if d < best:
                    best, second, owner = d, best, i
                elif d < second:
                    second = d
            edge = second - best
            if edge < joint:
                g[y][x] = 0
                continue
            p = pts[owner]
            ox = ((x + 0.5 - p[0] + N / 2) % N) - N / 2
            oy = ((y + 0.5 - p[1] + N / 2) % N) - N / 2
            length = math.hypot(ox, oy) or 1.0
            # jeder Brocken eine flache Kuppel: zum Rand hin geneigt, Licht von oben links
            tilt = max(0.0, min(1.0, (2.8 - edge) / 2.2)) ** 0.8
            shade = -(ox + oy) / (length * math.sqrt(2)) * tilt
            light = shade * 2.3 + (p[2] - 0.5) * 0.9 + (noise[y][x] - 0.5) * grain * 2
            level = 3 + round(light)
            if edge < joint + 0.55 and shade < -0.15:
                # Schattenfuge unten rechts
                level = min(level, 1)
            g[y][x] = max(1, min(5, level))
            owner_of[y][x] = owner
    return vs.despeckle(g), owner_of


def _palette(body: str, spread: float, cool: float, warm: float, hue: float = 6.0, sat: float = 0.03) -> list[Color]:
    """Sechs Stufen von der Fuge (dunkel) bis zum Licht."""
    return vs.tones(body, 6, spread=spread, cool=cool, warm=warm, hue=hue, sat=sat)


def _inside(g: vs.Grid, x: int, y: int) -> bool:
    """Liegt das Pixel mitten in einem Brocken (keine Fuge daneben)?"""
    return all(g[(y + dy) % N][(x + dx) % N] > 0 for dx, dy in ((0, 0), (1, 0), (-1, 0), (0, 1), (0, -1)))


def _spots(g: vs.Grid, seed: int, count: int, levels: Sequence[int] | None = None) -> list[tuple[int, int]]:
    rnd = random.Random(seed)
    out: list[tuple[int, int]] = []
    tries = 0
    while len(out) < count and tries < 500:
        tries += 1
        x, y = rnd.randrange(N), rnd.randrange(N)
        if _inside(g, x, y) and (levels is None or g[y][x] in levels) and all(abs(x - a) + abs(y - b) > 2 for a, b in out):
            out.append((x, y))
    return out


# =============================================================================
# Die Bruchsteine
# =============================================================================

def cobbled_stormstone() -> Image.Image:
    g, _ = cobble(61101, stones=9)
    img = vs.paint(g, _palette("#434c5e", 0.40, 225, 205))
    # Windschliff: kurze helle Schrägstriche auf ein paar Brocken
    for x, y in _spots(g, 61102, 3, (3, 4)):
        for i in range(2):
            xx, yy = (x + i) % N, (y - i) % N
            if g[yy][xx] > 0:
                img.putpixel((xx, yy), hexc("#8ea4c4") if i else hexc("#6d83a3"))
    return img


def cobbled_volcanic_rock() -> Image.Image:
    g, _ = cobble(61201, stones=10)
    img = vs.paint(g, _palette("#443d3c", 0.42, 250, 20))
    # Poren
    for x, y in _spots(g, 61202, 4, (3, 4)):
        img.putpixel((x, y), hexc("#26211f"))
    return img


def cobbled_rime_stone() -> Image.Image:
    g, _ = cobble(61301, stones=9)
    img = vs.paint(g, _palette("#a7b3c0", 0.22, 215, 200, hue=4, sat=0.02))
    # Reifkörner auf der Lichtseite
    for x, y in _spots(g, 61302, 7, (4, 5)):
        img.putpixel((x, y), hexc("#eef6fc"))
    for x, y in _spots(g, 61303, 3, (3,)):
        img.putpixel((x, y), hexc("#ffffff"))
    return img


def cobbled_shale() -> Image.Image:
    g, owner = cobble(61401, stones=10, sx=0.55, sy=1.75, joint=0.7, min_gap=3.0, grain=0.3)
    # Schichtung: in jedem Brocken jede dritte Zeile eine Stufe dunkler
    for y in range(N):
        for x in range(N):
            if g[y][x] > 1 and owner[y][x] >= 0 and (y + owner[y][x]) % 3 == 0:
                g[y][x] -= 1
    return vs.paint(g, [hexc("#2e2b28"), hexc("#3d3a36"), hexc("#4a4641"), hexc("#58534c"), hexc("#686158"), hexc("#7b7367")])


def cobbled_abyssal_stone() -> Image.Image:
    g, _ = cobble(61501, stones=9)
    img = vs.paint(g, _palette("#334d4d", 0.40, 210, 170))
    # Muschelsplitter
    for x, y in _spots(g, 61502, 4, (2, 3, 4)):
        img.putpixel((x, y), hexc("#c9d8d0"))
        if g[y][(x + 1) % N] > 0:
            img.putpixel(((x + 1) % N, y), hexc("#9fb7ae"))
    return img


def cobbled_shadow_stone() -> Image.Image:
    g, _ = cobble(61601, stones=9)
    return vs.paint(g, _palette("#6a637d", 0.32, 250, 280, hue=10, sat=0.04))


def cobbled_deep_shadow_stone() -> Image.Image:
    g, _ = cobble(61701, stones=14, angular=0.55, joint=0.55, min_gap=2.8, grain=0.3)
    return vs.paint(g, _palette("#3c3848", 0.42, 250, 280, hue=10, sat=0.04))


def build(put: Callable[[str, Image.Image], None]) -> None:
    put("block/cobbled_stormstone", cobbled_stormstone())
    put("block/cobbled_volcanic_rock", cobbled_volcanic_rock())
    put("block/cobbled_rime_stone", cobbled_rime_stone())
    put("block/cobbled_shale", cobbled_shale())
    put("block/cobbled_abyssal_stone", cobbled_abyssal_stone())
    put("block/cobbled_shadow_stone", cobbled_shadow_stone())
    put("block/cobbled_deep_shadow_stone", cobbled_deep_shadow_stone())
