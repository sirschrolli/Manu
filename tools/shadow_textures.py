#!/usr/bin/env python3
"""
Texturen der Schattenwelt (Grundblöcke):

  * block/shadow_stone                  – dunkler, violettgrauer Fels mit fahlen Schlieren
  * block/shadow_stone_bricks, cracked_shadow_stone_bricks, polished_shadow_stone
  * block/chiseled_shadow_stone         – Mondsichel über einem wachenden Auge: das Zeichen der Umbrer
  * block/deep_shadow_stone(_top)       – fast schwarzes, geschichtetes Tiefengestein (Säule wie Tiefenschiefer)
  * block/shadow_soil                   – dunkle, violettbraune Erde mit fahlen Steinchen und Würzelchen
  * block/shadow_grass_top, _side, _side_overlay – Schattengras (grau, die Farbe kommt aus dem Biom)
  * block/shadow_sand                   – fahler, lavendelgrauer Sand mit Windriffeln
  * block/ash_snow_block                – grauer Schnee voller Ascheflocken
  * block/shadow_log(_top), stripped_shadow_log(_top), shadow_planks – Schattenholz (schwarze Borke, violettes Holz)
  * block/shadow_leaves                 – Schattenlaub (grau, die Farbe kommt aus dem Biom)
  * block/night_crystal                 – durchscheinender, facettierter Nachtkristall
  * block/night_crystal_cluster(_emissive) – Druse aus spitzen Kristallen (Kreuzpflanze, leuchtend)
  * block/glimmer_moss(_glow)           – Glimmmoos mit silbrig glimmenden Spitzen
  * block/ghostcap(_emissive)           – Geisterpilze mit leuchtenden Hüten
  * block/shadow_sprouts                – fedrige Schattenhalme (grau, Farbe aus dem Biom)
  * block/pale_grass                    – dürre, fahle Halme der Bleichen Öde

Alle vollen Blöcke kacheln nahtlos. Wird von tools/generate_textures.py aufgerufen (build(put)).
"""
from __future__ import annotations

import math
import random
from typing import Callable

from PIL import Image

import block_textures as bt
import pixelart as pa
from pixelart import CLEAR, Color, hexc

N = 16

STONE = pa.palette("#131018", "#19151f", "#201b27", "#27212f", "#2e2738", "#362e41", "#3f364b", "#4a4057", "#584d66")
STREAK = pa.palette("#625874", "#776c8a", "#9186a4")
DEEP = pa.palette("#08070c", "#0d0b12", "#121018", "#17141e", "#1d1925", "#231e2c", "#2b2535", "#352e40")
SOIL = pa.palette("#120c13", "#19111a", "#211722", "#2a1e2b", "#332534", "#3e2d3f", "#4a364b")
PEBBLE = pa.palette("#4c4656", "#5e5868", "#736c7e")
SAND = pa.palette("#4a4454", "#585262", "#666070", "#746e7f", "#827c8e", "#918b9d", "#a19bac", "#b2adbc")
ASH_SNOW = pa.palette("#6c6874", "#7b7783", "#8a8692", "#9995a1", "#a8a4b0", "#b7b3bf", "#c6c3cd", "#d6d3dc")
ASH_FLECK = pa.palette("#2e2a33", "#3e3a44", "#524d58")
BARK = pa.palette("#0b0910", "#110e17", "#18131f", "#1f1928", "#271f32", "#30263d", "#3b2e4a")
SHEEN = pa.palette("#4a3a60", "#5c4876")
WOOD = pa.palette("#221534", "#2c1b43", "#372252", "#432a61", "#4f3370", "#5d3d80", "#6c4791", "#7d53a3")
CRYSTAL = pa.palette("#17154a", "#232270", "#33339a", "#4846c4", "#635ee6", "#8a7ef8", "#b4a8ff", "#ddd6ff", "#f7f4ff")
MOSS = pa.palette("#11151f", "#161c29", "#1c2434", "#232d40", "#2b374d", "#33425b")
SILVER = pa.palette("#6c7c9c", "#97a8c8", "#c4d2ec", "#eef4ff")
GHOST = pa.palette("#5a6a8a", "#8a9cbc", "#b8c8e4", "#e2ecfa", "#ffffff")
GHOST_GLOW = pa.palette("#6ad0f0", "#a8ecff", "#e8fbff")
STEM = pa.palette("#8c8aa0", "#b4b2c6", "#d8d6e6")
PALE = pa.palette("#5c566a", "#7a7488", "#9c96a8", "#bdb8c6", "#dcd8e2")


def _wrap(v: int) -> int:
    return v % N


def _gray(v: int, a: int = 255) -> Color:
    v = max(0, min(255, v))
    return (v, v, v, a)


def _new() -> Image.Image:
    return pa.new()


# =============================================================================
# Schattenstein
# =============================================================================

def shadow_stone() -> Image.Image:
    """Feinkörniger, violettgrauer Fels mit weichen Flecken, kurzen fahlen Schlieren, Poren und hellen Körnern."""
    noise = pa.fractal_noise(31101, ((8, 0.3), (4, 0.4), (2, 0.3)))
    rng = random.Random(31103)
    img = _new()
    px = img.load()
    for y in range(N):
        for x in range(N):
            px[x, y] = pa.pick_dither(STONE[3:7], 0.06 + noise[y][x] * 0.88, x, y, 0.8)
    # kurze Schlieren, schräg nach rechts unten (helle Kante oben, Schatten darunter)
    for (x0, y0, length) in ((1, 2, 4), (9, 5, 3), (4, 10, 5), (12, 12, 3), (13, 0, 2)):
        x, y = x0, y0
        for k in range(length):
            px[_wrap(x), _wrap(y)] = STREAK[0] if k % 2 == 0 else STONE[7]
            px[_wrap(x), _wrap(y + 1)] = STONE[3]
            x += 1
            if rng.random() < 0.45:
                y += 1
    for _ in range(6):   # Poren
        x, y = rng.randrange(N), rng.randrange(N)
        px[x, y] = STONE[1]
        px[_wrap(x + 1), y] = STONE[2] if rng.random() < 0.5 else px[_wrap(x + 1), y]
    for _ in range(4):   # helle Körner
        x, y = rng.randrange(N), rng.randrange(N)
        px[x, y] = STONE[8]
    return img


SHADOW_BRICKS_LAYOUT = bt.brick_layout(4, [[7, 9], [10, 6], [6, 10], [9, 7]], [0, 4, 9, 2])
VIOLET_TINT = hexc("#6a4a9a")
BLUE_TINT = hexc("#3a4a8a")


def _masonry(layout: str, idx: list[list[int]], colors: list[Color], seed: int, strength: float = 0.1) -> Image.Image:
    """Mauerwerk aus Palettenindizes; jeder Stein bekommt eine leichte eigene Tönung (violett, blau oder neutral)."""
    rows = bt.grid(layout)
    rng = random.Random(seed * 7 + 3)
    variants = [colors, bt.tinted(colors, VIOLET_TINT, strength), bt.tinted(colors, BLUE_TINT, strength)]
    stones = sorted({c for r in rows for c in r if c != "."})
    choice = {st: rng.choice([0, 0, 1, 2]) for st in stones}
    img = Image.new("RGBA", (N, N), CLEAR)
    px = img.load()
    for y in range(N):
        for x in range(N):
            v = max(0, min(len(colors) - 1, idx[y][x]))
            st = rows[y][x]
            px[x, y] = (colors if st == "." else variants[choice[st]])[v]
    return img


def _brick_indices() -> list[list[int]]:
    return bt.ashlar_indices(SHADOW_BRICKS_LAYOUT, 31201, tone=(-1, 0, 0, 1), hi=6, hi2=5)


def shadow_stone_bricks() -> Image.Image:
    idx = bt.edit(_brick_indices(), """
................
.....2..........
................
................
................
................
...........2....
................
................
..2.............
................
................
................
.........2......
................
................
""", {"2": 2})
    return _masonry(SHADOW_BRICKS_LAYOUT, idx, STONE[1:8], 31201, 0.08)


def cracked_shadow_stone_bricks() -> Image.Image:
    idx = bt.edit(_brick_indices(), """
...0............
....0...........
....00..........
.....0..........
..........0.....
..........00....
...........0....
............0...
..1.............
..00............
...0........0...
...0.......00...
....0.......0...
.............0..
.............00.
..............0.
""", {"0": 0, "1": 1})
    return _masonry(SHADOW_BRICKS_LAYOUT, idx, STONE[1:8], 31201, 0.08)


def polished_shadow_stone() -> Image.Image:
    """Polierter Schattenstein: glatte Fläche mit fein abgesetztem Rand und einem matten Glanzstreifen."""
    noise = pa.fractal_noise(31301, ((8, 0.6), (4, 0.4)))
    img = _new()
    px = img.load()
    for y in range(N):
        for x in range(N):
            if x == N - 1 or y == N - 1:
                c = STONE[2]
            elif x == 0 or y == 0:
                c = STONE[6]
            else:
                c = pa.pick_dither(STONE[4:7], 0.15 + noise[y][x] * 0.6, x, y, 0.5)
            px[x, y] = c
    for i in range(2, 9):
        if 0 < i - 1 < N - 1:
            px[i, i - 1] = STONE[7]
        if 0 < i - 2 < N - 1 and i % 2 == 0:
            px[i + 1, i - 2] = STONE[7]
    return img


# Mondsichel über dem Auge (gemeißelt): '#' Rille, '+' Grund im Auge (heller), 'o' Pupille
UMBRA_SIGN = [
    "................",
    "................",
    "......####......",
    "....##....#.....",
    "...##...........",
    "...#............",
    "...##...........",
    "....##....#.....",
    "......####......",
    "................",
    "....########....",
    "...#++++++++#...",
    "..#+++#oo#+++#..",
    "...#++++++++#...",
    "....########....",
    "................",
]


def chiseled_shadow_stone() -> Image.Image:
    """Gemeißelter Schattenstein: das Zeichen der Umbrer – eine Mondsichel über einem wachenden Auge."""
    img = polished_shadow_stone()
    px = img.load()
    carved = set()
    for y, row in enumerate(UMBRA_SIGN):
        for x, ch in enumerate(row):
            if ch == "#":
                px[x, y] = STONE[1]
                carved.add((x, y))
            elif ch == "+":
                px[x, y] = STONE[3]
            elif ch == "o":
                px[x, y] = hexc("#6a5ab8")
    # Lichtkanten unter den Rillen (Tiefe)
    for (x, y) in carved:
        below = (x, y + 1)
        if below not in carved and 0 < y + 1 < N - 1 and UMBRA_SIGN[y + 1][x] == ".":
            px[below] = STONE[7]
    px[8, 12] = hexc("#b0a4f0")   # Glanzpunkt in der Pupille
    return img


# =============================================================================
# Tiefschattenstein
# =============================================================================

def deep_shadow_stone() -> Image.Image:
    """Seite: fast schwarze, waagrechte Schichten mit feinen, helleren Bändern und Abplatzungen."""
    noise = pa.fractal_noise(31401, ((8, 0.5), (4, 0.3), (2, 0.2)))
    rng = random.Random(31402)
    img = _new()
    px = img.load()
    bands = [0, 3, 4, 7, 11, 12]    # Schichtfugen (dunkel) bzw. helle Bänder
    for y in range(N):
        for x in range(N):
            wave = int(round(math.sin(2 * math.pi * x / N + 1.3) * 0.8))
            yy = (y + wave) % N
            v = 0.2 + noise[y][x] * 0.55
            if yy in (0, 7):
                c = DEEP[1]
            elif yy in (11,):
                c = DEEP[2] if (x + y) % 3 else DEEP[1]
            elif yy in (3, 12):
                c = DEEP[5] if noise[y][x] > 0.35 else DEEP[4]
            else:
                c = pa.pick_dither(DEEP[2:6], v, x, y, 0.7)
            px[x, y] = c
    _ = bands
    for _ in range(6):
        x, y = rng.randrange(N), rng.randrange(N)
        px[x, y] = DEEP[6]
        px[_wrap(x + 1), y] = DEEP[5]
    for _ in range(5):
        x, y = rng.randrange(N), rng.randrange(N)
        px[x, y] = DEEP[0]
    return img


def deep_shadow_stone_top() -> Image.Image:
    """Oben (Querschnitt): kantige Bruchstücke, dazwischen dunkle Fugen."""
    cells, border, _ = pa.voronoi(31411, 9, metric="cheby")
    noise = pa.fractal_noise(31412, ((4, 0.6), (2, 0.4)))
    img = _new()
    px = img.load()
    for y in range(N):
        for x in range(N):
            if border[y][x] < 0.9:
                px[x, y] = DEEP[1]
                continue
            tone = 2 + cells[y][x] % 3
            v = tone + (1 if noise[y][x] > 0.7 else 0) - (1 if noise[y][x] < 0.25 else 0)
            if border[y][x] < 1.9 and (x + y) % 2 == 0:
                v += 1
            px[x, y] = DEEP[max(1, min(6, v))]
    return img


# =============================================================================
# Böden
# =============================================================================

def _soil(seed: int) -> Image.Image:
    noise = pa.fractal_noise(seed, ((8, 0.4), (4, 0.4), (2, 0.2)))
    rng = random.Random(seed + 1)
    img = _new()
    px = img.load()
    for y in range(N):
        for x in range(N):
            px[x, y] = pa.pick_dither(SOIL[1:6], 0.1 + noise[y][x] * 0.8, x, y, 0.75)
    for _ in range(6):   # fahle Steinchen mit Schatten
        x, y = rng.randrange(N), rng.randrange(N)
        px[x, y] = PEBBLE[rng.randrange(2) + 1]
        px[x, _wrap(y + 1)] = SOIL[0]
    for _ in range(3):   # Würzelchen
        x, y = rng.randrange(N), rng.randrange(N)
        for k in range(3):
            px[_wrap(x + k), _wrap(y + (k // 2))] = SOIL[6] if k == 1 else SOIL[5]
    for _ in range(6):
        x, y = rng.randrange(N), rng.randrange(N)
        px[x, y] = SOIL[0]
    return img


def shadow_soil() -> Image.Image:
    return _soil(31501)


def _grass_values(seed: int) -> list[list[int]]:
    """Grauwerte (110..200) für die Grasoberseite: Büschel aus hellen Spitzen mit dunklem Fuß."""
    noise = pa.fractal_noise(seed, ((8, 0.35), (4, 0.35), (2, 0.3)))
    rng = random.Random(seed + 1)
    vals = [[0] * N for _ in range(N)]
    for y in range(N):
        for x in range(N):
            vals[y][x] = int(128 + noise[y][x] * 52)
    for _ in range(22):   # Halmspitzen: hell, darunter Schatten
        x, y = rng.randrange(N), rng.randrange(N)
        vals[y][x] = min(205, vals[y][x] + 26)
        vals[_wrap(y + 1)][x] = max(108, vals[_wrap(y + 1)][x] - 22)
    for _ in range(10):
        x, y = rng.randrange(N), rng.randrange(N)
        vals[y][x] = max(108, vals[y][x] - 18)
    # auf wenige Stufen runden (Pixelkunst statt Rauschen)
    return [[(v // 8) * 8 for v in row] for row in vals]


def shadow_grass_top() -> Image.Image:
    img = _new()
    px = img.load()
    for y, row in enumerate(_grass_values(31601)):
        for x, v in enumerate(row):
            px[x, y] = _gray(v)
    return img


# Tiefe des Grasrands je Spalte und herabhängende Spitzen
GRASS_EDGE = [3, 4, 3, 2, 3, 5, 4, 3, 3, 2, 3, 4, 6, 4, 3, 2]
DEFAULT_GRASS = hexc("#5a3e8e")


def _grass_fringe() -> dict[tuple[int, int], int]:
    vals = _grass_values(31602)
    fringe = {}
    for x in range(N):
        depth = GRASS_EDGE[x]
        for y in range(depth):
            v = vals[y][x]
            if y == depth - 1:
                v = max(108, v - 24)   # dunkle Unterkante
            fringe[(x, y)] = v
    return fringe


def shadow_grass_side_overlay() -> Image.Image:
    img = _new()
    px = img.load()
    for (x, y), v in _grass_fringe().items():
        px[x, y] = _gray(v)
    return img


def shadow_grass_side() -> Image.Image:
    """Seite ohne Biomfarbe: Erde, oben schon mit der Grundfarbe des Schattengrases (für Partikel und Ferne)."""
    img = _soil(31603)
    px = img.load()
    for (x, y), v in _grass_fringe().items():
        px[x, y] = (DEFAULT_GRASS[0] * v // 255, DEFAULT_GRASS[1] * v // 255, DEFAULT_GRASS[2] * v // 255, 255)
    for x in range(N):   # Schatten unter dem Rand
        y = GRASS_EDGE[x]
        if y < N:
            px[x, y] = SOIL[0] if x % 3 else SOIL[1]
    return img


def shadow_sand() -> Image.Image:
    """Fahler Sand: feine Körnung und flache Windriffel, dazu helle und dunkle Körnchen."""
    noise = pa.fractal_noise(31701, ((4, 0.5), (2, 0.5)))
    rng = random.Random(31702)
    img = _new()
    px = img.load()
    for y in range(N):
        for x in range(N):
            ripple = math.sin(2 * math.pi * (y * 2 + x * 0.0) / N + 2 * math.pi * noise[y][x] * 0.5)
            v = 0.3 + noise[y][x] * 0.45 + ripple * 0.12
            px[x, y] = pa.pick_dither(SAND[2:7], v, x, y, 0.9)
    for _ in range(10):
        x, y = rng.randrange(N), rng.randrange(N)
        px[x, y] = SAND[7]
    for _ in range(8):
        x, y = rng.randrange(N), rng.randrange(N)
        px[x, y] = SAND[1] if rng.random() < 0.7 else SAND[0]
    return img


def ash_snow_block() -> Image.Image:
    """Aschschnee: weicher, grauer Schnee, durchsetzt mit dunklen Ascheflocken."""
    noise = pa.fractal_noise(31801, ((8, 0.4), (4, 0.4), (2, 0.2)))
    rng = random.Random(31802)
    img = _new()
    px = img.load()
    for y in range(N):
        for x in range(N):
            px[x, y] = pa.pick_dither(ASH_SNOW[2:8], 0.12 + noise[y][x] * 0.8, x, y, 0.8)
    for _ in range(11):   # Flocken (einzeln oder schräg zu zweit)
        x, y = rng.randrange(N), rng.randrange(N)
        px[x, y] = ASH_FLECK[rng.randrange(3)]
        if rng.random() < 0.35:
            px[_wrap(x + 1), _wrap(y + 1)] = ASH_FLECK[2]
    for _ in range(6):
        x, y = rng.randrange(N), rng.randrange(N)
        px[x, y] = ASH_SNOW[1]
    return img


# =============================================================================
# Schattenholz
# =============================================================================

def shadow_log() -> Image.Image:
    """Borke: schwarzviolette, gewellte Längsrippen mit tiefen Furchen; auf den Graten ein violetter Schimmer."""
    noise = pa.fractal_noise(31901, ((8, 0.5), (4, 0.3), (2, 0.2)))
    wobble = pa.fractal_noise(31903, ((8, 0.6), (4, 0.4)))
    rng = random.Random(31902)
    img = _new()
    px = img.load()
    for y in range(N):
        for x in range(N):
            phase = 2 * math.pi * (x + (wobble[y][x] - 0.5) * 3.0) * 3 / N
            ridge = math.sin(phase)
            if ridge < -0.72:
                c = BARK[0]
            elif ridge < -0.35:
                c = BARK[1] if noise[y][x] < 0.6 else BARK[2]
            else:
                v = 0.25 + 0.4 * (ridge + 0.35) / 1.35 + 0.35 * noise[y][x]
                c = pa.pick_dither(BARK[2:7], v, x, y, 0.7)
                if ridge > 0.8 and noise[y][x] > 0.6:
                    c = SHEEN[0]
            px[x, y] = c
    for _ in range(6):   # kurze Querrisse auf den Rippen
        x, y = rng.randrange(N), rng.randrange(N)
        px[x, y] = BARK[1]
        px[_wrap(x + 1), y] = BARK[1]
        px[x, _wrap(y + 1)] = BARK[5]
    for (x, y) in [(4, 3), (10, 9), (14, 14), (5, 12)]:
        if px[x, y] not in (BARK[0], BARK[1]):
            px[x, y] = SHEEN[1]
    return img


def _rings(bark: bool) -> Image.Image:
    img = _new()
    px = img.load()
    for y in range(N):
        for x in range(N):
            dx, dy = x - 7.5, y - 7.5
            d = max(abs(dx), abs(dy)) * 0.6 + math.hypot(dx, dy) * 0.4
            if d > 6.9:
                if bark:
                    px[x, y] = BARK[2 if (x + y) % 3 else 1]
                else:
                    px[x, y] = WOOD[3] if (x + y) % 3 else WOOD[2]
            else:
                ring = int(d * 1.2) % 2
                px[x, y] = WOOD[5] if ring else WOOD[4]
                if d < 1.3:
                    px[x, y] = WOOD[2]
                elif d < 2.0:
                    px[x, y] = WOOD[6]
    for (x, y) in [(7, 2), (8, 3), (12, 7), (11, 8), (4, 11), (5, 10)]:
        px[x, y] = WOOD[2]
    return img


def shadow_log_top() -> Image.Image:
    return _rings(True)


def stripped_shadow_log_top() -> Image.Image:
    return _rings(False)


def stripped_shadow_log() -> Image.Image:
    """Geschältes Holz: glatt und violett, mit feiner, senkrechter Maserung."""
    noise = pa.fractal_noise(32001, ((8, 0.5), (4, 0.3), (2, 0.2)))
    img = _new()
    px = img.load()
    for y in range(N):
        for x in range(N):
            grain = math.sin(2 * math.pi * x / N * 3 + noise[y][x] * 2.2)
            v = 0.45 + 0.2 * grain + 0.18 * (noise[y][x] - 0.5)
            px[x, y] = pa.pick_dither(WOOD[2:7], v, x, y, 0.6)
    for (x, y0, length) in ((3, 1, 5), (9, 8, 6), (13, 3, 4)):
        for y in range(y0, y0 + length):
            px[x, _wrap(y)] = WOOD[2]
    return img


def shadow_planks() -> Image.Image:
    """Vier violette Bretter mit Maserung, versetzten Stößen und Nagellöchern."""
    noise = pa.fractal_noise(32101, ((8, 0.4), (4, 0.4), (2, 0.2)))
    img = _new()
    px = img.load()
    seams = (5, 12, 1, 9)
    for y in range(N):
        board = y // 4
        for x in range(N):
            if y % 4 == 3:
                px[x, y] = WOOD[0]
                continue
            grain = math.sin(2 * math.pi * (x + board * 5) / N * 2 + y * 0.4 + noise[y][x] * 2.0)
            v = 0.42 + 0.16 * grain + 0.2 * (noise[y][x] - 0.5) + (0.1 if y % 4 == 0 else 0)
            px[x, y] = pa.pick_dither(WOOD[2:7], v, x, y, 0.5)
        if y % 4 != 3:
            px[seams[board], y] = WOOD[1]
    for board, sx in enumerate(seams):   # Nagellöcher neben den Stößen
        y = board * 4 + 1
        px[_wrap(sx + 1), y] = WOOD[0]
        px[_wrap(sx - 2), y] = WOOD[0]
    return img


def shadow_leaves() -> Image.Image:
    """Dichtes Laub aus kleinen, spitzen Blattgruppen (grau – die Farbe kommt aus dem Biom), mit Lücken."""
    rng = random.Random(32201)
    cells, border, _ = pa.voronoi(32202, 15)
    img = _new()
    px = img.load()
    tones = [104, 128, 152, 176, 196]
    for y in range(N):
        for x in range(N):
            if border[y][x] < 0.5:
                px[x, y] = _gray(tones[0])
                continue
            k = 1 + cells[y][x] % 3 + (1 if border[y][x] > 2.4 else 0)
            px[x, y] = _gray(tones[min(4, k)])
    for _ in range(12):
        x, y = rng.randrange(N), rng.randrange(N)
        px[x, y] = CLEAR
    return img


# =============================================================================
# Kristalle, Moos und Pflanzen
# =============================================================================

def night_crystal() -> Image.Image:
    """Nachtkristall: Facetten in Blauviolett, hellere Kanten, leicht durchscheinend, einzelne Funken."""
    cells, border, pts = pa.voronoi(32301, 7)
    rng = random.Random(32302)
    img = _new()
    px = img.load()
    facet = {i: 1 + rng.randrange(4) for i in range(len(pts))}
    for y in range(N):
        for x in range(N):
            v = facet[cells[y][x]]
            alpha = 196
            if border[y][x] < 0.7:
                v, alpha = 5, 228
            elif border[y][x] < 1.5:
                v += 1
            else:
                # Verlauf je Facette: oben links heller
                cx, cy = pts[cells[y][x]]
                if (x - cx) + (y - cy) < -1.5:
                    v += 1
            c = CRYSTAL[max(0, min(8, v))]
            px[x, y] = (c[0], c[1], c[2], alpha)
    for (x, y) in [(3, 4), (11, 2), (12, 11), (6, 13)]:
        px[x, y] = CRYSTAL[8]
    return img


NIGHT_CLUSTER = [
    "................",
    ".......a........",
    "......aba.......",
    "......abc...a...",
    "..a...abc..aba..",
    ".aba..abc..abc..",
    ".abc..abcd.abc..",
    ".abcd.abcd.abcd.",
    "..bcd.abcd.abcd.",
    "..bcd.abcd..bcd.",
    "...cdabbcd..bcd.",
    "...cdabccdabcd..",
    "....abbccdabcd..",
    "....abbccdbccd..",
    "...aabbccddccd..",
    "..aaabbbcccddd..",
]


def night_crystal_cluster() -> tuple[Image.Image, Image.Image]:
    colors = {"a": CRYSTAL[6], "b": CRYSTAL[5], "c": CRYSTAL[3], "d": CRYSTAL[2]}
    img = pa.sprite(NIGHT_CLUSTER, colors)
    px = img.load()
    for (x, y) in [(7, 1), (12, 4), (2, 4), (7, 2)]:
        px[x, y] = CRYSTAL[8]
    glow = pa.new()
    gp = glow.load()
    for y in range(N):
        for x in range(N):
            c = px[x, y]
            if c[3] and c in (CRYSTAL[8], CRYSTAL[6]):
                gp[x, y] = c
    return img, glow


def glimmer_moss() -> tuple[Image.Image, Image.Image]:
    """Glimmmoos: dichte, blaugraue Moospolster; einzelne Spitzen glimmen silbrig (eigene Leuchtebene)."""
    noise = pa.fractal_noise(32401, ((8, 0.4), (4, 0.35), (2, 0.25)))
    cells, border, _ = pa.voronoi(32403, 9)
    img = _new()
    px = img.load()
    for y in range(N):
        for x in range(N):
            v = 0.2 + 0.6 * noise[y][x] + (0.12 if border[y][x] > 2.0 else 0) - (0.18 if border[y][x] < 0.6 else 0)
            px[x, y] = pa.pick_dither(MOSS, v, x, y, 0.8)
    glow = pa.new()
    gp = glow.load()
    tips = [(2, 2), (9, 1), (14, 6), (5, 8), (11, 11), (2, 13), (8, 14)]
    for i, (x, y) in enumerate(tips):
        bright = SILVER[3] if i % 3 == 0 else SILVER[2]
        for (dx, dy, c) in ((0, 0, bright), (1, 0, SILVER[1]), (0, -1, SILVER[0])):
            if (dx, dy) != (0, 0) and (i + dx + dy) % 2:
                continue
            px[_wrap(x + dx), _wrap(y + dy)] = c
            gp[_wrap(x + dx), _wrap(y + dy)] = c
        px[x, _wrap(y + 1)] = MOSS[0]
    return img, glow


GHOSTCAP = [
    "................",
    "................",
    "........oCCCo...",
    ".......oCwwcCo..",
    ".......CccccCC..",
    "..oCCo..o.s..o..",
    ".oCwcCo...s.....",
    ".CccccC...s.....",
    "...s......s.....",
    "...s.....ss.....",
    "...s....oCCo....",
    "...ss..oCwcCo...",
    "....s..CcccC....",
    "....s....s......",
    "...gsg..gsg.....",
    "................",
]


def ghostcap() -> tuple[Image.Image, Image.Image]:
    colors = {"o": GHOST[0], "C": GHOST[1], "c": GHOST[2], "w": GHOST[4], "s": STEM[1], "g": hexc("#3a3448")}
    img = pa.sprite(GHOSTCAP, colors)
    glow = pa.new()
    gp = glow.load()
    px = img.load()
    for y, row in enumerate(GHOSTCAP):
        for x, ch in enumerate(row):
            if ch == "w":
                gp[x, y] = GHOST_GLOW[2]
            elif ch == "c":
                gp[x, y] = GHOST_GLOW[1]
    _ = px
    return img, glow


SPROUTS = [
    "................",
    "................",
    "................",
    "...........a....",
    "..a.......ab....",
    "..ba...a..b.a...",
    "...b..ab.ab.b...",
    ".a.b.a.b.b.b..a.",
    ".ba.b..bb..b.ab.",
    "..b.b.a.b.ab.b..",
    "..bb.ab.b.b.bb..",
    "...bb.b.bb.bb...",
    "...cbbbcbbcbc...",
    "....cbcbcbbc....",
    "....ccccccccc...",
    "................",
]


def shadow_sprouts() -> Image.Image:
    return pa.sprite(SPROUTS, {"a": _gray(196), "b": _gray(160), "c": _gray(120)})


PALE_GRASS = [
    "................",
    "..........a.....",
    "...a......a.....",
    "...a.....ab.....",
    "...b..a..b......",
    "...b..a..b...a..",
    "...bb.b..b..ab..",
    "....b.b.bb..b...",
    "..a.b.bb.b..b...",
    "..ab.b.b.b.bb...",
    "...b.b.bb.bb....",
    "...bbcb.bcb.....",
    "....bcbcbcb.....",
    "....ccbccbc.....",
    "...cddcdcdcd....",
    "................",
]


def pale_grass() -> Image.Image:
    return pa.sprite(PALE_GRASS, {"a": PALE[4], "b": PALE[3], "c": PALE[2], "d": PALE[1]})


# =============================================================================
# Seelenbeeren und Glimmerranken
# =============================================================================

BUSH_LEAF = {"d": hexc("#0e2e2c"), "m": hexc("#174a46"), "l": hexc("#22665e"), "h": hexc("#348a7e"), "s": hexc("#1c1a24")}
BERRY_UNRIPE = {"b": hexc("#1e6e72"), "B": hexc("#3aa4a4")}
BERRY_RIPE = {"b": hexc("#2ad2cc"), "B": hexc("#c8fffa")}

SOUL_BUSH = [
    [
        "", "", "", "", "", "", "", "", "",
        "................",
        ".......l........",
        "......lhl..l....",
        "....l..m..lhl...",
        "...lhl.m...m....",
        "....m..ms.m.....",
        ".......ss.......",
    ],
    [
        "", "", "", "", "",
        "................",
        "......l..l......",
        ".....lhllhl.l...",
        "...l.mhmmhm.hl..",
        "..lhlmmlmmlmm...",
        "...mmdmmlmdmml..",
        "..lmdmldmmdmlh..",
        "...mdmmdmdmmm...",
        "....dmddsdmd....",
        ".....d.ss.d.....",
        ".......ss.......",
    ],
    [
        "", "", "",
        "................",
        ".......l..l.....",
        "....l.lhllhl....",
        "...lhlmhmmhm.l..",
        "..lmhmmbmlmmlhl.",
        "..mmlmmmmlbmmm..",
        ".lhmbmdmlmmmdml.",
        "..mmmmdmmdbmmh..",
        "..lmdmbdmmdmlm..",
        "...mdmmdmdmmm...",
        "....dmddsdmbd...",
        ".....d.ss.d.....",
        ".......ss.......",
    ],
    [
        "", "",
        "................",
        "......l...l.....",
        "...l.lhlBlhl.l..",
        "..lhlmhmbmhmlhl.",
        "..mBmmbmmlmmBm..",
        ".lbmlmmmBmmbmml.",
        ".mmmhbmlbmmmBmh.",
        ".lBmmmdmlmmbdml.",
        "..mmbmdmmdBmmh..",
        "..lmdmBdmmdmbm..",
        "...mbmmdmBmmm...",
        "....dmddsdmbd...",
        ".....d.ss.d.....",
        ".......ss.......",
    ],
]


def soul_berry_bush(stage: int) -> Image.Image:
    colors = dict(BUSH_LEAF)
    colors.update(BERRY_RIPE if stage == 3 else BERRY_UNRIPE)
    return pa.sprite(SOUL_BUSH[stage], colors)


SOUL_BERRIES_ITEM = [
    "", "", "", "",
    ".........ll.....",
    "........lhl.....",
    "......m.l.......",
    ".....m..m.......",
    "....bBb.bBb.....",
    "...bBwbbBwbb....",
    "...bbbbbbbbb....",
    "....bbb.bbb.....",
    "......bBb.......",
    ".....bBwbb......",
    ".....bbbbb......",
    "......bbb.......",
]


def soul_berries_item() -> Image.Image:
    return pa.sprite(SOUL_BERRIES_ITEM, {"l": BUSH_LEAF["l"], "h": BUSH_LEAF["h"], "m": BUSH_LEAF["m"],
                                         "b": hexc("#1aa8a4"), "B": hexc("#4ae8e0"), "w": hexc("#e8fffd")})


VINE_COLORS = {"d": hexc("#2a3450"), "m": hexc("#44587e"), "l": hexc("#6a86b0"), "h": hexc("#9cb6dc"),
               "g": hexc("#c8e0ff"), "G": hexc("#f2f8ff")}

GLIMMER_VINES_PLANT = [
    ".....m....m.....",
    ".....ml...m.....",
    "......m..lm.....",
    "......m..m......",
    ".....lm..m......",
    "......m..ml.....",
    "......dm.m......",
    ".......m.m......",
    ".......m.mh.....",
    "......hm.m......",
    ".......m.m......",
    ".......mdm......",
    "........m.......",
    "........ml......",
    ".......lm.......",
    "........m.......",
]

GLIMMER_VINES = [
    ".....m....m.....",
    ".....ml...m.....",
    "......m..lm.....",
    "......m..m......",
    ".....lm..m......",
    "......m..ml.....",
    "......dm.m......",
    ".......m.m......",
    ".......mhm......",
    "......lmmh......",
    "......hggh......",
    ".....hgGGgh.....",
    ".....gGGGGg.....",
    ".....hgGGgh.....",
    "......hggh......",
    ".......hh.......",
]


def glimmer_vines() -> tuple[Image.Image, Image.Image]:
    return pa.sprite(GLIMMER_VINES, VINE_COLORS), pa.sprite(GLIMMER_VINES_PLANT, VINE_COLORS)


# =============================================================================
# Ausgabe
# =============================================================================

def build(put: Callable[[str, Image.Image], None]) -> None:
    put("block/shadow_stone", shadow_stone())
    put("block/shadow_stone_bricks", shadow_stone_bricks())
    put("block/cracked_shadow_stone_bricks", cracked_shadow_stone_bricks())
    put("block/polished_shadow_stone", polished_shadow_stone())
    put("block/chiseled_shadow_stone", chiseled_shadow_stone())
    put("block/deep_shadow_stone_side", deep_shadow_stone())
    put("block/deep_shadow_stone_top", deep_shadow_stone_top())
    put("block/shadow_soil", shadow_soil())
    put("block/shadow_grass_top", shadow_grass_top())
    put("block/shadow_grass_side", shadow_grass_side())
    put("block/shadow_grass_side_overlay", shadow_grass_side_overlay())
    put("block/shadow_sand", shadow_sand())
    put("block/ash_snow_block", ash_snow_block())
    put("block/shadow_log", shadow_log())
    put("block/shadow_log_top", shadow_log_top())
    put("block/stripped_shadow_log", stripped_shadow_log())
    put("block/stripped_shadow_log_top", stripped_shadow_log_top())
    put("block/shadow_planks", shadow_planks())
    put("block/shadow_leaves", shadow_leaves())
    put("block/night_crystal", night_crystal())
    cluster, cluster_glow = night_crystal_cluster()
    put("block/night_crystal_cluster", cluster)
    put("block/night_crystal_cluster_emissive", cluster_glow)
    moss, moss_glow = glimmer_moss()
    put("block/glimmer_moss", moss)
    put("block/glimmer_moss_glow", moss_glow)
    cap, cap_glow = ghostcap()
    put("block/ghostcap", cap)
    put("block/ghostcap_emissive", cap_glow)
    put("block/shadow_sprouts", shadow_sprouts())
    put("block/pale_grass", pale_grass())
    for stage in range(4):
        put(f"block/soul_berry_bush_stage{stage}", soul_berry_bush(stage))
    put("item/soul_berries", soul_berries_item())
    head, plant = glimmer_vines()
    put("block/glimmer_vines", head)
    put("block/glimmer_vines_plant", plant)


if __name__ == "__main__":
    import sys
    out: dict[str, Image.Image] = {}
    build(lambda k, v: out.__setitem__(k, v))
    target = sys.argv[1] if len(sys.argv) > 1 else "shadow_preview.png"
    zoom, pad, cols = 8, 6, 6
    cell_w = 16 * 3 * zoom // 2 + pad
    rows = math.ceil(len(out) / cols)
    sheet = Image.new("RGBA", (cols * cell_w + pad, rows * cell_w + pad), (40, 42, 48, 255))
    for i, (key, img) in enumerate(out.items()):
        tile = pa.checker(48, 48, cell=4)
        tile.alpha_composite(pa.tiled(img, 3))
        tile = tile.resize((48 * zoom // 2, 48 * zoom // 2), Image.NEAREST)
        sheet.alpha_composite(tile, (pad + (i % cols) * cell_w, pad + (i // cols) * cell_w))
    sheet.save(target)
    print(f"{len(out)} Texturen, Vorschau: {target}")
