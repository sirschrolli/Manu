#!/usr/bin/env python3
"""
Texturen rund um den Siegelkreis und die Geschichte „Das Siegel“:

  * block/sealstone, sealstone_bricks, cracked_/mossy_sealstone_bricks, polished_sealstone, chiseled_sealstone
    – uralter, dunkler Stein mit kühlem Blaustich
  * block/seal_monolith(_top)     – Monolithenstein mit senkrechten Meißelspuren
  * block/seal_rune_<element>(_lit, _glow) – Rune des Elements am Monolithen (dunkel / leuchtend + Leuchtebene)
  * block/seal_inlay_<element>(_lit, _glow) – eingelegtes Zeichen im Boden
  * block/seal_socket_<element>(_filled, _filled_glow) – Siegelsockel mit Nische (leer / mit Splitter)
  * block/seal_plate_00..24 (+ _glow) – das Siegel: ein großes Bild aus 5×5 Platten (Kreise, Hexagramm, sechs
    Edelsteine der Elemente, in der Mitte das versiegelte Dunkel)
  * block/shadow_portal          – das Schattentor (32 Bilder Animation, nahtlos)
  * item/<element>_seal_shard, item/chronicle
  * gui/chronicle                – das aufgeschlagene Buch der Chronik mit Symbolen (512×256)

Wird von tools/generate_textures.py aufgerufen (build(put)).
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
ELEMENTS = ["fire", "ice", "lightning", "nature", "earth", "water"]

STONE = pa.palette("#171b21", "#1e232a", "#262c34", "#2f363f", "#38404a", "#434c57", "#505a66", "#5e6975")
GOLD = [hexc("#4a3512"), hexc("#7a5a22"), hexc("#a8813a"), hexc("#d2ac5c"), hexc("#f0d58e")]
VOID = [hexc("#07040c"), hexc("#140a22"), hexc("#26113f"), hexc("#3d1b63"), hexc("#6a3aa0")]

# Farben der Elemente: (dunkel, mittel, hell, Kern) – für Runen, Edelsteine und Splitter
ELEMENT_COLORS: dict[str, list[Color]] = {
    "fire": [hexc("#5c1408"), hexc("#b8380e"), hexc("#ff7a24"), hexc("#ffd27a")],
    "ice": [hexc("#1e4e70"), hexc("#4fa6d8"), hexc("#a8e6ff"), hexc("#f0fcff")],
    "lightning": [hexc("#5a4a0a"), hexc("#c8a81a"), hexc("#ffe94a"), hexc("#fffbc8")],
    "nature": [hexc("#1c4a12"), hexc("#3f9a2a"), hexc("#8ee05a"), hexc("#e2ffb8")],
    "earth": [hexc("#4a2e12"), hexc("#9a6a32"), hexc("#d8a868"), hexc("#ffe6b8")],
    "water": [hexc("#0a3a5a"), hexc("#1a86b8"), hexc("#5ad6f0"), hexc("#d4fbff")],
}

# Runenzeichen der Elemente (10×10, mittig gesetzt). '#' = gemeißelt.
GLYPHS = {
    "fire": """
....#.....
....##....
...###....
...####.#.
..#####.#.
.#######..
.###.####.
.##...###.
..#...##..
...####...
""",
    "ice": """
....##....
.#..##..#.
..#.##.#..
...####...
##########
##########
...####...
..#.##.#..
.#..##..#.
....##....
""",
    "lightning": """
.....###..
....###...
...###....
..###.....
.#######..
.....###..
....###...
...###....
..##......
.##.......
""",
    "nature": """
.......##.
.....####.
....#####.
...######.
..####.##.
..###.###.
.###.####.
.##.####..
.#.####...
#.........
""",
    "earth": """
..........
....##....
...####...
..##..##..
..#.##.#..
.#.####.#.
.########.
##..##..##
##########
..........
""",
    "water": """
..........
.##....##.
#..#..#..#
....##....
..........
.##....##.
#..#..#..#
....##....
..........
..........
""",
}


def glyph_mask(element: str, size: int = 10) -> list[list[bool]]:
    rows = [r for r in GLYPHS[element].strip("\n").split("\n")]
    return [[c == "#" for c in row.ljust(size, ".")[:size]] for row in rows[:size]]


def _clamp(v: int, lo: int, hi: int) -> int:
    return max(lo, min(hi, v))


# =============================================================================
# Siegelstein
# =============================================================================

def sealstone() -> Image.Image:
    """Natürlicher Siegelstein: dunkles, feinkörniges Gestein mit hellen Adern und kleinen Einschlüssen."""
    noise = pa.fractal_noise(7101, ((8, 0.45), (4, 0.35), (2, 0.2)))
    vein = pa.fractal_noise(7102, ((8, 0.6), (4, 0.4)))
    rng = random.Random(7103)
    img = pa.new()
    px = img.load()
    for y in range(N):
        for x in range(N):
            v = noise[y][x]
            c = pa.pick_dither(STONE[1:6], 0.1 + v * 0.8, x, y, 0.8)
            if abs(vein[y][x] - 0.5) < 0.035:
                c = STONE[6]
            px[x, y] = c
    for _ in range(7):
        x, y = rng.randrange(N), rng.randrange(N)
        px[x, y] = STONE[0]
    for _ in range(4):
        x, y = rng.randrange(N), rng.randrange(N)
        px[x, y] = STONE[7]
    return img


SEAL_BRICKS_LAYOUT = bt.brick_layout(4, [[8, 8], [5, 8, 3], [8, 8], [3, 8, 5]], [0, 3, 4, 7])


def sealstone_bricks() -> Image.Image:
    idx = bt.ashlar_indices(SEAL_BRICKS_LAYOUT, 7201, tone=(-1, 0, 0, 1), hi=6, hi2=5)
    return bt.masonry_image(SEAL_BRICKS_LAYOUT, idx, STONE[:7], 7201, 0.06)


def cracked_sealstone_bricks() -> Image.Image:
    idx = bt.ashlar_indices(SEAL_BRICKS_LAYOUT, 7201, tone=(-1, 0, 0, 1), hi=6, hi2=5)
    idx = bt.edit(idx, """
..0.............
...0............
...00...........
....0......0....
...........0....
..........00....
...........0....
............0...
.....0......0...
.....00.........
......0.........
......0.........
.......0........
..............0.
.............00.
.............0..
""", {"0": 0})
    return bt.masonry_image(SEAL_BRICKS_LAYOUT, idx, STONE[:7], 7201, 0.06)


MOSS = [hexc("#2c4a1e"), hexc("#3d6228"), hexc("#517a32"), hexc("#6a933e")]


def mossy_sealstone_bricks() -> Image.Image:
    img = sealstone_bricks()
    px = img.load()
    noise = pa.fractal_noise(7301, ((8, 0.5), (4, 0.3), (2, 0.2)))
    rows = bt.grid(SEAL_BRICKS_LAYOUT)
    for y in range(N):
        for x in range(N):
            n = noise[y][x]
            mortar = rows[y][x] == "."
            if n > (0.52 if mortar else 0.66):
                px[x, y] = MOSS[_clamp(int((n - 0.5) * 8), 0, 3)]
    return img


def polished_sealstone() -> Image.Image:
    """Polierter Siegelstein: glatte Fläche mit leichtem Glanz und feinem Rand."""
    noise = pa.fractal_noise(7401, ((8, 0.6), (4, 0.4)))
    img = pa.new()
    px = img.load()
    for y in range(N):
        for x in range(N):
            edge = x == 0 or y == 0 or x == N - 1 or y == N - 1
            v = noise[y][x]
            if edge:
                c = STONE[2] if (x == N - 1 or y == N - 1) else STONE[5]
            else:
                c = pa.pick_dither(STONE[3:6], 0.2 + v * 0.6, x, y, 0.5)
            px[x, y] = c
    # Glanzstreifen
    for i in range(3, 8):
        px[i, i - 2] = STONE[6]
    return img


def chiseled_sealstone() -> Image.Image:
    """Gemeißelter Siegelstein: ein Kreis mit eingeschriebenem Hexagramm, wie ein kleines Abbild des Siegels."""
    img = polished_sealstone()
    px = img.load()
    cx, cy = 7.5, 7.5
    for y in range(1, N - 1):
        for x in range(1, N - 1):
            d = math.hypot(x - cx, y - cy)
            if 5.6 < d < 6.6:
                px[x, y] = STONE[1]
                if y + 1 < N - 1 and math.hypot(x - cx, y + 1 - cy) > 6.6:
                    pass
    # Hexagramm
    pts = []
    for k in range(6):
        a = math.radians(-90 + 60 * k)
        pts.append((cx + math.cos(a) * 5.0, cy + math.sin(a) * 5.0))
    for tri in ((0, 2, 4), (1, 3, 5)):
        for i in range(3):
            ax, ay = pts[tri[i]]
            bx, by = pts[tri[(i + 1) % 3]]
            for s in range(24):
                t = s / 23
                x, y = round(ax + (bx - ax) * t), round(ay + (by - ay) * t)
                px[x, y] = STONE[1]
    px[7, 7] = STONE[0]
    px[8, 8] = STONE[0]
    px[7, 8] = STONE[0]
    px[8, 7] = STONE[0]
    # Lichtkanten unter den Rillen (Tiefe)
    out = img.copy()
    po = out.load()
    for y in range(1, N - 1):
        for x in range(1, N - 1):
            if px[x, y] in (STONE[1], STONE[0]) and px[x, y + 1] not in (STONE[1], STONE[0]) and y + 1 < N - 1:
                po[x, y + 1] = STONE[6]
    return out


# =============================================================================
# Monolithen, Runen, Einlagen, Sockel
# =============================================================================

def seal_monolith() -> Image.Image:
    """Monolithenstein: große, glatte Fläche mit senkrechten Meißelspuren, die Kanten leicht abgerundet."""
    noise = pa.fractal_noise(7501, ((8, 0.5), (4, 0.3), (2, 0.2)))
    rng = random.Random(7502)
    img = pa.new()
    px = img.load()
    for y in range(N):
        for x in range(N):
            v = noise[y][x]
            c = pa.pick_dither(STONE[2:6], 0.15 + v * 0.7, x, y, 0.6)
            px[x, y] = c
    # wenige, feine senkrechte Meißelspuren (nicht durchgehend, damit kein Streifenmuster entsteht)
    for x, y0, length in ((3, 2, 6), (10, 7, 7), (13, 0, 4)):
        for y in range(y0, min(N, y0 + length)):
            px[x, y] = STONE[2]
            if rng.random() < 0.7:
                px[(x + 1) % N, y] = STONE[5]
    # ein paar Kerben und helle Körner
    for _ in range(5):
        x, y = rng.randrange(N), rng.randrange(N)
        px[x, y] = STONE[1]
    for _ in range(4):
        x, y = rng.randrange(N), rng.randrange(N)
        px[x, y] = STONE[6]
    return img


def seal_monolith_top() -> Image.Image:
    noise = pa.fractal_noise(7511, ((8, 0.5), (4, 0.5)))
    img = pa.new()
    px = img.load()
    for y in range(N):
        for x in range(N):
            d = max(abs(x - 7.5), abs(y - 7.5))
            c = pa.pick_dither(STONE[3:6], 0.2 + noise[y][x] * 0.6, x, y, 0.5)
            if 5.5 < d < 6.5:
                c = STONE[2]
            px[x, y] = c
    return img


def _carve(img: Image.Image, mask: list[list[bool]], x0: int, y0: int, deep: Color, edge: Color, tint: Color | None = None) -> None:
    px = img.load()
    h = len(mask)
    w = len(mask[0])
    for y in range(h):
        for x in range(w):
            if mask[y][x]:
                px[x0 + x, y0 + y] = tint if tint is not None else deep
    # Lichtkante unten rechts der Rillen
    for y in range(h):
        for x in range(w):
            if mask[y][x]:
                for dx, dy in ((1, 1), (0, 1)):
                    nx, ny = x + dx, y + dy
                    inside = 0 <= nx < w and 0 <= ny < h and mask[ny][nx]
                    if not inside and 0 <= x0 + nx < N and 0 <= y0 + ny < N:
                        px[x0 + nx, y0 + ny] = edge


def _glow_layer(mask: list[list[bool]], x0: int, y0: int, colors: list[Color]) -> Image.Image:
    img = pa.new()
    px = img.load()
    h = len(mask)
    w = len(mask[0])
    for y in range(h):
        for x in range(w):
            if mask[y][x]:
                inner = all(0 <= x + dx < w and 0 <= y + dy < h and mask[y + dy][x + dx] for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)))
                px[x0 + x, y0 + y] = colors[3] if inner else colors[2]
    return img


def _dull(c: Color, amount: float = 0.65) -> Color:
    return pa.mix(c, STONE[1], amount)


def seal_rune(element: str) -> tuple[Image.Image, Image.Image, Image.Image]:
    """Rune am Monolithen: (dunkel, leuchtend, Leuchtebene)."""
    mask = glyph_mask(element)
    col = ELEMENT_COLORS[element]
    dark = seal_monolith()
    _carve(dark, mask, 3, 3, STONE[0], STONE[5], tint=_dull(col[0], 0.55))
    # Rahmen um die Rune (eingemeißeltes Viereck)
    px = dark.load()
    for i in range(1, 15):
        for (x, y) in ((i, 1), (i, 14), (1, i), (14, i)):
            px[x, y] = STONE[1]
    lit = dark.copy()
    _carve(lit, mask, 3, 3, col[1], col[2], tint=col[1])
    glow = _glow_layer(mask, 3, 3, col)
    gpx = glow.load()
    lpx = lit.load()
    for i in range(1, 15):
        for (x, y) in ((i, 1), (i, 14), (1, i), (14, i)):
            gpx[x, y] = pa.mix(col[1], col[2], 0.3)
            lpx[x, y] = col[0]
    return dark, lit, glow


def seal_inlay(element: str) -> tuple[Image.Image, Image.Image, Image.Image]:
    """Bodeneinlage: polierter Stein mit einer Raute aus Bronze und dem Edelstein des Elements."""
    col = ELEMENT_COLORS[element]
    base = polished_sealstone()
    px = base.load()
    cx, cy = 7.5, 7.5
    frame: list[tuple[int, int]] = []
    gem: list[tuple[int, int, float]] = []
    for y in range(N):
        for x in range(N):
            d = abs(x - cx) + abs(y - cy)
            if 5.0 <= d < 6.0:
                frame.append((x, y))
            elif d < 3.0:
                gem.append((x, y, d))
    for (x, y) in frame:
        px[x, y] = GOLD[1] if (x + y) % 3 else GOLD[2]
    dark = base.copy()
    dpx = dark.load()
    for (x, y, d) in gem:
        dpx[x, y] = _dull(col[1], 0.6) if d > 1.2 else _dull(col[2], 0.55)
    lit = base.copy()
    lpx = lit.load()
    glow = pa.new()
    gpx = glow.load()
    for (x, y) in frame:
        lpx[x, y] = GOLD[3]
        gpx[x, y] = pa.mix(GOLD[3], col[2], 0.35)
    for (x, y, d) in gem:
        c = col[3] if d < 1.2 else col[2] if d < 2.2 else col[1]
        lpx[x, y] = c
        gpx[x, y] = c
    return dark, lit, glow


SHARD = """
................
...........o....
..........oho...
.........okwo...
.........okho...
........okkwo...
........okhho...
.......okkhwho..
.......okkhhho..
......okkkhhko..
......okkhhko...
.....okkkhhko...
.....okkhhko....
....okkkhko.....
....okkkko......
.....oooo.......
"""


def seal_socket(element: str) -> tuple[Image.Image, Image.Image, Image.Image]:
    """Vorderseite des Siegelsockels: (leer, mit Splitter, Leuchtebene)."""
    col = ELEMENT_COLORS[element]
    base = seal_monolith()
    px = base.load()
    # Nische 6×9 mit Bronzerahmen
    x0, y0, w, h = 5, 4, 6, 10
    for y in range(y0 - 1, y0 + h + 1):
        for x in range(x0 - 1, x0 + w + 1):
            if y in (y0 - 1, y0 + h) or x in (x0 - 1, x0 + w):
                px[x, y] = GOLD[2] if (y == y0 - 1 or x == x0 - 1) else GOLD[1]
            else:
                depth = min(x - x0, x0 + w - 1 - x, y - y0, y0 + h - 1 - y)
                px[x, y] = VOID[0] if depth >= 1 else VOID[1]
    # kleines Elementzeichen über der Nische
    for x in range(6, 10):
        px[x, 2] = _dull(col[1], 0.4)
    px[7, 1] = _dull(col[2], 0.4)
    px[8, 1] = _dull(col[2], 0.4)
    empty = base.copy()
    filled = base.copy()
    fpx = filled.load()
    glow = pa.new()
    gpx = glow.load()
    # Splitter in die Nische (verkleinert: nur der mittlere Teil)
    shard = pa.sprite(SHARD, {"o": col[0], "k": col[1], "h": col[2], "w": col[3]})
    small = shard.resize((8, 8), Image.NEAREST)
    spx = small.load()
    for y in range(8):
        for x in range(8):
            c = spx[x, y]
            if c[3] > 0:
                fx, fy = x0 - 1 + x, y0 + 1 + y
                if x0 <= fx < x0 + w and y0 <= fy < y0 + h:
                    fpx[fx, fy] = c
                    gpx[fx, fy] = c
    for x in range(6, 10):
        fpx[x, 2] = col[2]
        gpx[x, 2] = col[2]
    for (x, y) in ((7, 1), (8, 1)):
        fpx[x, y] = col[3]
        gpx[x, y] = col[3]
    return empty, filled, glow


# =============================================================================
# Das Siegel (5×5 Platten)
# =============================================================================

def seal_emblem() -> tuple[Image.Image, Image.Image]:
    """Das große Siegelbild (80×80) und seine Leuchtebene."""
    S = 80
    c = (S - 1) / 2
    noise = pa.fractal_noise(7601, ((8, 0.5), (4, 0.3), (2, 0.2)), S, S)
    img = Image.new("RGBA", (S, S), CLEAR)
    glow = Image.new("RGBA", (S, S), CLEAR)
    px = img.load()
    gpx = glow.load()
    base = [hexc("#12151b"), hexc("#171b22"), hexc("#1d222a"), hexc("#232932")]
    for y in range(S):
        for x in range(S):
            px[x, y] = pa.pick_dither(base, 0.15 + noise[y][x] * 0.7, x, y, 0.6)
            # feine Fugen zwischen den Platten
            if x % 16 == 15 or y % 16 == 15:
                px[x, y] = hexc("#0d1014")
            elif x % 16 == 0 or y % 16 == 0:
                px[x, y] = pa.mix(px[x, y], hexc("#2b313b"), 0.5)

    def plot(x: float, y: float, color: Color, glow_color: Color | None = None) -> None:
        ix, iy = math.floor(x + 0.5), math.floor(y + 0.5)
        if 0 <= ix < S and 0 <= iy < S:
            px[ix, iy] = color
            if glow_color is not None:
                gpx[ix, iy] = glow_color

    # zwei Goldringe mit Runenmarken dazwischen
    for y in range(S):
        for x in range(S):
            d = math.hypot(x - c, y - c)
            if 37.0 <= d < 38.3:
                px[x, y] = GOLD[3] if y < c else GOLD[2]
            elif 35.2 <= d < 36.0:
                px[x, y] = GOLD[1]
            elif 28.6 <= d < 29.6:
                px[x, y] = GOLD[2]
    for k in range(36):
        a = k * math.tau / 36
        if k % 6 == 0:
            continue
        for r in (31.0, 32.0, 33.0):
            plot(c + math.cos(a) * r, c + math.sin(a) * r, GOLD[1] if r != 32.0 else GOLD[2])
    # Hexagramm aus zwei Dreiecken
    pts = []
    for k in range(6):
        a = math.radians(-90 + 60 * k)
        pts.append((c + math.cos(a) * 28.0, c + math.sin(a) * 28.0))
    for tri in ((0, 2, 4), (1, 3, 5)):
        for i in range(3):
            ax, ay = pts[tri[i]]
            bx, by = pts[tri[(i + 1) % 3]]
            steps = int(math.hypot(bx - ax, by - ay) * 2)
            for s in range(steps + 1):
                t = s / steps
                plot(ax + (bx - ax) * t, ay + (by - ay) * t, GOLD[3])
                plot(ax + (bx - ax) * t, ay + (by - ay) * t + 1, GOLD[1])
    # sechs Edelsteine an den Spitzen – gleiche Reihenfolge wie die Monolithen (Norden = Feuer, im Uhrzeigersinn)
    order = ["fire", "lightning", "nature", "ice", "water", "earth"]
    for k, element in enumerate(order):
        a = math.radians(-90 + 60 * k)
        gx, gy = c + math.cos(a) * 32.5, c + math.sin(a) * 32.5
        col = ELEMENT_COLORS[element]
        for y in range(-4, 5):
            for x in range(-4, 5):
                d = math.hypot(x, y)
                if d < 3.2:
                    color = col[3] if d < 1.0 else col[2] if d < 2.0 else col[1]
                    plot(gx + x, gy + y, color, color)
                elif d < 4.2:
                    plot(gx + x, gy + y, GOLD[2])
    # die Mitte: das versiegelte Dunkel, ein Auge aus Schatten
    for y in range(S):
        for x in range(S):
            d = math.hypot(x - c, y - c)
            if d < 9.0:
                v = d / 9.0
                color = VOID[4] if d < 1.6 else VOID[3] if d < 3.2 else VOID[2] if d < 5.5 else VOID[1]
                px[x, y] = color
                if d < 3.2:
                    gpx[x, y] = color
            elif d < 10.4:
                px[x, y] = GOLD[3] if (x + y) % 2 else GOLD[2]
    # sechs Strahlen vom Dunkel zu den Ringen
    for k in range(6):
        a = math.radians(-60 + 60 * k)
        for r in range(11, 16):
            plot(c + math.cos(a) * r, c + math.sin(a) * r, GOLD[1])
    return img, glow


def seal_plates() -> list[tuple[Image.Image, Image.Image]]:
    emblem, glow = seal_emblem()
    tiles = []
    for row in range(5):
        for col in range(5):
            box = (col * 16, row * 16, col * 16 + 16, row * 16 + 16)
            tiles.append((emblem.crop(box), glow.crop(box)))
    return tiles


# =============================================================================
# Schattentor (Animation)
# =============================================================================

def _periodic_noise(seed: int, cell: int, period: int) -> Callable[[float, float], float]:
    rng = random.Random(seed)
    n = period // cell
    grid = [[rng.random() for _ in range(n)] for _ in range(n)]

    def sample(x: float, y: float) -> float:
        gx, gy = x / cell, y / cell
        x0, y0 = math.floor(gx), math.floor(gy)
        tx, ty = gx - x0, gy - y0
        tx, ty = tx * tx * (3 - 2 * tx), ty * ty * (3 - 2 * ty)
        a = grid[y0 % n][x0 % n]
        b = grid[y0 % n][(x0 + 1) % n]
        cc = grid[(y0 + 1) % n][x0 % n]
        d = grid[(y0 + 1) % n][(x0 + 1) % n]
        return (a + (b - a) * tx) + ((cc + (d - cc) * tx) - (a + (b - a) * tx)) * ty

    return sample


def shadow_portal(frames: int = 32) -> Image.Image:
    """Ein wogender Schattenstrudel: zwei gegeneinander laufende Rauschebenen, nahtlos in Raum und Zeit."""
    n1 = _periodic_noise(7701, 4, 16)
    n2 = _periodic_noise(7702, 8, 16)
    n3 = _periodic_noise(7703, 2, 16)
    pal = [hexc("#05030a"), hexc("#0e0619"), hexc("#1a0b2e"), hexc("#2b1248"), hexc("#43206b"), hexc("#6a3aa0"), hexc("#a77ae0"),
           hexc("#e2cfff")]
    sheet = Image.new("RGBA", (N, N * frames), CLEAR)
    px = sheet.load()
    for f in range(frames):
        t = f / frames
        for y in range(N):
            for x in range(N):
                # Ebenen verschieben sich in einem Durchlauf um genau eine Kachel → nahtlose Schleife
                a = n1(x + t * 16, y + t * 16)
                b = n2(x - t * 16, y + t * 32)
                w = n3(x + math.sin(t * math.tau) * 2 + t * 32, y)
                v = a * 0.55 + b * 0.35 + w * 0.1
                v = (v - 0.2) / 0.6
                v = max(0.0, min(0.999, v))
                idx = int(v ** 1.6 * 7)
                c = pal[idx]
                # vereinzelte Funken
                if v > 0.86 and (x * 7 + y * 13 + f * 5) % 11 == 0:
                    c = pal[7]
                px[x, f * N + y] = c
    return sheet


# =============================================================================
# Gegenstände
# =============================================================================

def seal_shard(element: str) -> Image.Image:
    col = ELEMENT_COLORS[element]
    img = pa.sprite(SHARD, {"o": col[0], "k": col[1], "h": col[2], "w": col[3]})
    px = img.load()
    # Glanzpunkt und dunkle Kante unten
    px[8, 3] = col[3]
    px[7, 2] = col[3]
    return img


CHRONICLE = """
................
..oooooooooooo..
.oRRRRRRRRRRRRpo
.oRgRRRRRRRRgRpo
.oRRRRggggRRRRpo
.oRRRgyyyygRRRpo
.oRRgyRggRygRRpo
.oRRgyggggygRRpo
.oRRgyRggRygRRpo
.oRRRgyyyygRRRpo
.oRRRRggggRRRRpo
.oRgRRRRRRRRgRpo
.oRRRRRRRRRRRRpo
.oddddddddddddpo
..oooooooooooo..
................
"""


def chronicle_item() -> Image.Image:
    colors = {
        "o": hexc("#1e0c08"), "R": hexc("#6e2418"), "d": hexc("#4a160e"), "p": hexc("#e8dcb8"),
        "g": hexc("#c8a050"), "y": hexc("#f0d58e"),
    }
    img = pa.sprite(CHRONICLE, colors)
    px = img.load()
    # Lederstruktur: einige hellere Flecken
    rng = random.Random(7801)
    for _ in range(10):
        x, y = rng.randrange(2, 14), rng.randrange(2, 13)
        if px[x, y] == colors["R"]:
            px[x, y] = hexc("#7e2c1e")
    # Seitenränder (Papierlagen)
    for y in range(2, 14):
        px[14, y] = hexc("#d8caa0") if y % 2 else colors["p"]
    return img


# =============================================================================
# Oberfläche: die Chronik als Buch
# =============================================================================

PARCHMENT = [hexc("#cdb88e"), hexc("#d9c69e"), hexc("#e3d3ae"), hexc("#ebdfbf"), hexc("#f2e8cf")]
LEATHER = [hexc("#2a0e09"), hexc("#3d150d"), hexc("#521d12"), hexc("#672718"), hexc("#7d3220")]
INK = hexc("#3a2414")


def chronicle_gui() -> Image.Image:
    W, H = 512, 256
    img = Image.new("RGBA", (W, H), CLEAR)
    book = _book()
    img.alpha_composite(book, (0, 0))
    _icons(img)
    # Hervorhebungen der Kapitel (gewählt / unter der Maus)
    sel = _highlight(136, 18, (122, 30, 18), 0.20)
    hov = _highlight(136, 18, (90, 60, 30), 0.10)
    img.alpha_composite(sel, (0, 200))
    img.alpha_composite(hov, (0, 220))
    return img


def _book() -> Image.Image:
    W, H = 320, 200
    img = Image.new("RGBA", (W, H), CLEAR)
    px = img.load()
    rng = random.Random(7901)
    lnoise = pa.fractal_noise(7902, ((16, 0.5), (8, 0.3), (4, 0.2)), 320, 208)
    pnoise = pa.fractal_noise(7903, ((32, 0.4), (16, 0.3), (8, 0.2), (4, 0.1)), 320, 208)
    # Einband (Leder) mit abgerundeten Ecken
    for y in range(H):
        for x in range(W):
            r = 6
            cx = min(x, W - 1 - x)
            cy = min(y, H - 1 - y)
            if cx < r and cy < r and math.hypot(r - cx - 0.5, r - cy - 0.5) > r:
                continue
            v = lnoise[y][x]
            c = pa.pick_dither(LEATHER[1:5], 0.15 + v * 0.7, x, y, 0.7)
            if cx == 0 or cy == 0:
                c = LEATHER[0]
            elif cx == 1 or cy == 1:
                c = LEATHER[4] if (x < W / 2 and y < H / 2) else LEATHER[2]
            px[x, y] = c
    # Goldbeschläge an den Ecken
    for (ox, oy, sx, sy) in ((3, 3, 1, 1), (W - 4, 3, -1, 1), (3, H - 4, 1, -1), (W - 4, H - 4, -1, -1)):
        for i in range(14):
            for j in range(3):
                for (x, y) in ((ox + sx * i, oy + sy * j), (ox + sx * j, oy + sy * i)):
                    if 0 <= x < W and 0 <= y < H:
                        px[x, y] = GOLD[3] if j == 0 else GOLD[2] if j == 1 else GOLD[1]
        for (x, y) in ((ox + sx * 4, oy + sy * 4), (ox + sx * 5, oy + sy * 4), (ox + sx * 4, oy + sy * 5)):
            px[x, y] = GOLD[3]
    # Papierlagen am Rand (unten und außen)
    for y in range(9, H - 7):
        for x in (9, 10, W - 11, W - 10):
            px[x, y] = PARCHMENT[0] if (y + x) % 3 else PARCHMENT[1]
    for x in range(10, W - 10):
        for y in (H - 9, H - 8):
            px[x, y] = PARCHMENT[0] if (x + y) % 3 else PARCHMENT[1]
    # Seiten
    spine = W // 2
    for y in range(8, H - 9):
        for x in range(11, W - 11):
            if abs(x - spine) < 2:
                continue
            v = pnoise[y][x]
            # zum Bund hin dunkler (Wölbung), an der Außenkante leicht dunkler
            dist = abs(x - spine)
            curve = max(0.0, 1.0 - dist / 22.0)
            outer = max(0.0, 1.0 - (min(x - 11, W - 12 - x)) / 6.0)
            shade = 0.25 + v * 0.6 - curve * 0.55 - outer * 0.25
            px[x, y] = pa.pick_dither(PARCHMENT, max(0.0, min(0.999, shade + 0.15)), x, y, 0.6)
    # Bund
    for y in range(8, H - 9):
        px[spine - 1, y] = hexc("#8a7650")
        px[spine, y] = hexc("#5a4a30")
        px[spine + 1, y] = hexc("#8a7650")
    # Flecken und Alterung
    for _ in range(14):
        cx, cy = rng.randrange(20, W - 20), rng.randrange(16, H - 16)
        rad = rng.uniform(2.0, 5.5)
        for y in range(int(cy - rad), int(cy + rad) + 1):
            for x in range(int(cx - rad), int(cx + rad) + 1):
                if math.hypot(x - cx, y - cy) < rad and abs(x - spine) > 3 and 12 < x < W - 12 and 9 < y < H - 10:
                    if rng.random() < 0.55:
                        px[x, y] = pa.mix(px[x, y], PARCHMENT[0], 0.45)
    # Lesebändchen
    for y in range(H - 12, H):
        for x in range(spine + 30, spine + 34):
            if y < H - 1 or x in (spine + 30, spine + 33):
                px[x, y] = hexc("#8e1c1c") if x in (spine + 31, spine + 32) else hexc("#5e1010")
    px[spine + 31, H - 1] = CLEAR
    px[spine + 32, H - 1] = CLEAR
    return img


def _icon(img: Image.Image, x0: int, y0: int, art: str, colors: dict[str, Color]) -> None:
    rows = [r for r in art.strip("\n").split("\n")]
    px = img.load()
    for y, row in enumerate(rows):
        for x, ch in enumerate(row):
            if ch in colors:
                px[x0 + x, y0 + y] = colors[ch]


def _icons(img: Image.Image) -> None:
    green = {"g": hexc("#2e6b2e"), "G": hexc("#4f9a3f")}
    _icon(img, 320, 0, """
.........
.......gG
......gG.
.....gG..
g...gG...
Gg.gG....
.GgG.....
..G......
.........
""", green)
    red = {"r": hexc("#7a1e12"), "R": hexc("#a8402a")}
    _icon(img, 330, 0, """
.........
..r......
..rR.....
..rRR....
..rRRR...
..rRR....
..rR.....
..r......
.........
""", red)
    gray = {"k": hexc("#6a5a46"), "K": hexc("#8a7860")}
    _icon(img, 340, 0, """
...kkk...
..k...k..
..k...k..
.kkkkkkk.
.kKKKKKk.
.kKKkKKk.
.kKKkKKk.
.kKKKKKk.
.kkkkkkk.
""", gray)
    box = {"b": hexc("#5a4630"), "w": hexc("#efe4c8")}
    _icon(img, 350, 0, """
bbbbbbbbb
bwwwwwwwb
bwwwwwwwb
bwwwwwwwb
bwwwwwwwb
bwwwwwwwb
bwwwwwwwb
bwwwwwwwb
bbbbbbbbb
""", box)
    _icon(img, 360, 0, """
bbbbbbbbb
bwwwwwwgG
bwwwwwgGb
bwwwwgGwb
bgwwgGwwb
bGggGwwwb
bwGGwwwwb
bwwGwwwwb
bbbbbbbbb
""", {**box, **green})
    # Zeichen der Elemente (12×12): leuchtend und gedämpft – Reihenfolge wie DragonType (Feuer, Eis, Blitz, Natur, Erde, Wasser)
    for i, element in enumerate(ELEMENTS):
        for lit, y0 in ((True, 12), (False, 25)):
            icon = _element_icon(element, lit)
            img.alpha_composite(icon, (320 + i * 13, y0))
    # Siegel 32×32 (ganz) und zerbrochen
    emblem, _ = seal_emblem()
    small = emblem.resize((32, 32), Image.LANCZOS)
    img.alpha_composite(_round_mask(small), (320, 40))
    # Zierlinien: für die Titelkarte (je 46×5) und die Buchseiten (110×7)
    img.alpha_composite(_ornament(46, 5, mirror=False), (400, 80))
    img.alpha_composite(_ornament(46, 5, mirror=True), (400, 86))
    img.alpha_composite(_divider(110, 7), (320, 94))


def _element_icon(element: str, lit: bool) -> Image.Image:
    col = ELEMENT_COLORS[element]
    img = Image.new("RGBA", (12, 12), CLEAR)
    px = img.load()
    c = 5.5
    for y in range(12):
        for x in range(12):
            d = math.hypot(x - c, y - c)
            if d < 5.6:
                if d > 4.6:
                    px[x, y] = GOLD[2] if lit else hexc("#7a6248")
                else:
                    px[x, y] = (col[0] if lit else hexc("#4a3e30"))
    mask = glyph_mask(element)
    # 10×10 → 8×8 (jede 5. Zeile/Spalte weglassen)
    keep = [0, 1, 2, 3, 5, 6, 7, 8]
    for yy, sy in enumerate(keep):
        for xx, sx in enumerate(keep):
            if mask[sy][sx]:
                px[2 + xx, 2 + yy] = (col[3] if lit else hexc("#a89878"))
    return img


def _round_mask(img: Image.Image) -> Image.Image:
    out = img.copy()
    px = out.load()
    c = (img.width - 1) / 2
    for y in range(img.height):
        for x in range(img.width):
            if math.hypot(x - c, y - c) > img.width / 2:
                px[x, y] = CLEAR
    return out


def _ornament(w: int, h: int, mirror: bool) -> Image.Image:
    img = Image.new("RGBA", (w, h), CLEAR)
    px = img.load()
    mid = h // 2
    for x in range(w):
        t = x / (w - 1)
        if mirror:
            t = 1 - t
        alpha = int(255 * min(1.0, t * 1.6))
        c = GOLD[3]
        px[x, mid] = (c[0], c[1], c[2], alpha)
    # Raute am inneren Ende
    ex = w - 3 if not mirror else 2
    for dy in range(-2, 3):
        for dx in range(-2, 3):
            if abs(dx) + abs(dy) <= 2:
                px[ex + dx, mid + dy] = GOLD[3] if abs(dx) + abs(dy) < 2 else GOLD[2]
    return img


def _divider(w: int, h: int) -> Image.Image:
    img = Image.new("RGBA", (w, h), CLEAR)
    px = img.load()
    mid = h // 2
    cx = w // 2
    ink = hexc("#7a5a32")
    for x in range(w):
        d = abs(x - cx) / cx
        if d < 0.95:
            a = int(255 * (1 - d ** 3))
            px[x, mid] = (ink[0], ink[1], ink[2], a)
    for dy in range(-3, 4):
        for dx in range(-3, 4):
            if abs(dx) + abs(dy) <= 3:
                px[cx + dx, mid + dy] = hexc("#7a1e12") if abs(dx) + abs(dy) <= 1 else ink
    for s in (-1, 1):
        px[cx + s * 6, mid] = ink
        px[cx + s * 7, mid - 1] = ink
        px[cx + s * 7, mid + 1] = ink
    return img


def _highlight(w: int, h: int, rgb: tuple[int, int, int], strength: float) -> Image.Image:
    img = Image.new("RGBA", (w, h), CLEAR)
    px = img.load()
    for y in range(h):
        for x in range(w):
            fx = min(x, w - 1 - x) / 10.0
            a = strength * min(1.0, fx)
            if y in (0, h - 1):
                a *= 0.5
            px[x, y] = (rgb[0], rgb[1], rgb[2], int(255 * a))
    return img


# =============================================================================

def seal_glow(size: int = 64) -> Image.Image:
    """Weicher, runder Lichtfleck (weiß, nach außen durchsichtig) für Lichtkugel und Blitz beim Siegelbruch."""
    img = Image.new("RGBA", (size, size), CLEAR)
    px = img.load()
    c = (size - 1) / 2
    for y in range(size):
        for x in range(size):
            d = math.hypot(x - c, y - c) / (size / 2)
            if d < 1.0:
                a = (1.0 - d) ** 2.2
                core = max(0.0, 1.0 - d * 3.0)
                v = int(255 * min(1.0, a + core * 0.5))
                px[x, y] = (255, 255, 255, v)
    return img


def build(put: Callable[[str, Image.Image], None]) -> None:
    put("entity/seal_glow", seal_glow())
    put("block/sealstone", sealstone())
    put("block/sealstone_bricks", sealstone_bricks())
    put("block/cracked_sealstone_bricks", cracked_sealstone_bricks())
    put("block/mossy_sealstone_bricks", mossy_sealstone_bricks())
    put("block/polished_sealstone", polished_sealstone())
    put("block/chiseled_sealstone", chiseled_sealstone())
    put("block/seal_monolith", seal_monolith())
    put("block/seal_monolith_top", seal_monolith_top())
    for element in ELEMENTS:
        dark, lit, glow = seal_rune(element)
        put(f"block/seal_rune_{element}", dark)
        put(f"block/seal_rune_{element}_lit", lit)
        put(f"block/seal_rune_{element}_glow", glow)
        dark, lit, glow = seal_inlay(element)
        put(f"block/seal_inlay_{element}", dark)
        put(f"block/seal_inlay_{element}_lit", lit)
        put(f"block/seal_inlay_{element}_glow", glow)
        empty, filled, glow = seal_socket(element)
        put(f"block/seal_socket_{element}", empty)
        put(f"block/seal_socket_{element}_filled", filled)
        put(f"block/seal_socket_{element}_filled_glow", glow)
        put(f"item/{element}_seal_shard", seal_shard(element))
    for i, (tile, glow) in enumerate(seal_plates()):
        put(f"block/seal_plate_{i:02d}", tile)
        put(f"block/seal_plate_{i:02d}_glow", glow)
    put("block/shadow_portal", shadow_portal())
    put("item/chronicle", chronicle_item())
    put("gui/chronicle", chronicle_gui())
