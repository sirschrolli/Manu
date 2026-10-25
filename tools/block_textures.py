#!/usr/bin/env python3
"""
Blocktexturen der Mod als handgezeichnete Pixelkunst (16x16, Stil wie Original-Minecraft).

Statt Zufallsmustern legt jede Textur ihren Aufbau fest – Steinfugen, Balken, Ziegelreihen, Moos,
Risse, Erzkristalle – als Zeichen-Raster. Die Schattierung folgt festen Regeln wie bei den
Original-Texturen:
  * kleine, eng beieinander liegende Paletten (6-7 Töne), dunkle Töne kühler, helle wärmer
  * Licht von oben links: obere/linke Steinkante hell, untere Kante dunkel, Fuge darunter am dunkelsten
  * ruhige Flächen: nur weiche, 2-3 Pixel große Helligkeitsflecken statt Pixelrauschen
  * alles ist nahtlos kachelbar (Raster laufen "um die Ecke")

Es werden KEINE Original-Minecraft-Texturen verwendet oder kopiert.
"""
from __future__ import annotations

import math
import random
from typing import Callable

from PIL import Image

import pixelart as pa
from pixelart import CLEAR, Color, hexc

N = 16


# =============================================================================
# Grundbausteine
# =============================================================================

def pal(*hexes: str) -> list[Color]:
    return [hexc(h) for h in hexes]


def grid(ascii_map: str) -> list[str]:
    """16 Zeilen à 16 Zeichen (Leerzeichen am Zeilenrand werden ignoriert)."""
    rows = [r.strip() for r in ascii_map.strip("\n").splitlines()]
    rows = [r for r in rows if r]
    if len(rows) != N or any(len(r) != N for r in rows):
        raise ValueError(f"Raster muss 16x16 sein: {[len(r) for r in rows]}")
    return rows


def soft_noise(seed: int, smooth: float = 0.6) -> list[list[float]]:
    """Weiches, kachelbares Rauschen (0..1, gleichmäßig verteilt) mit kleinen Flecken statt Einzelpixeln."""
    rng = random.Random(seed)
    raw = [[rng.random() for _ in range(N)] for _ in range(N)]
    blur = [[sum(raw[(y + dy) % N][(x + dx) % N] for dy in (-1, 0, 1) for dx in (-1, 0, 1)) / 9
             for x in range(N)] for y in range(N)]
    mixed = [[blur[y][x] * smooth + raw[y][x] * (1 - smooth) for x in range(N)] for y in range(N)]
    order = sorted((mixed[y][x], y, x) for y in range(N) for x in range(N))
    out = [[0.0] * N for _ in range(N)]
    for i, (_, y, x) in enumerate(order):
        out[y][x] = i / (len(order) - 1)
    return out


def from_indices(rows: list[list[int]], colors: list[Color]) -> Image.Image:
    img = Image.new("RGBA", (N, N), CLEAR)
    px = img.load()
    for y in range(N):
        for x in range(N):
            v = rows[y][x]
            if v >= 0:
                px[x, y] = colors[max(0, min(len(colors) - 1, v))]
    return img


def from_ascii(ascii_map: str, colors: dict[str, Color]) -> Image.Image:
    """Zeichen -> Farbe; Zeichen ohne Farbe (z. B. '.') bleiben durchsichtig."""
    rows = grid(ascii_map)
    img = Image.new("RGBA", (N, N), CLEAR)
    px = img.load()
    for y, row in enumerate(rows):
        for x, ch in enumerate(row):
            if ch in colors:
                px[x, y] = colors[ch]
    return img


def overlay(base: Image.Image, ascii_map: str, colors: dict[str, Color]) -> Image.Image:
    out = base.copy()
    out.alpha_composite(from_ascii(ascii_map, colors))
    return out


def edit(indices: list[list[int]], ascii_map: str, mapping: dict[str, int]) -> list[list[int]]:
    """Überschreibt einzelne Pixel: Zeichen aus mapping -> Palettenindex, alle anderen bleiben."""
    rows = grid(ascii_map)
    out = [row[:] for row in indices]
    for y, row in enumerate(rows):
        for x, ch in enumerate(row):
            if ch in mapping:
                out[y][x] = mapping[ch]
    return out


def digits(ascii_map: str) -> list[list[int]]:
    """Raster aus Ziffern 0-9 (Palettenindex); '.' = durchsichtig (-1)."""
    return [[-1 if c == "." else int(c) for c in row] for row in grid(ascii_map)]


# =============================================================================
# Mauerwerk
# =============================================================================

def ashlar_indices(layout: str, seed: int, *, smooth: float = 0.6, tone: tuple[int, ...] = (-1, 0, 0, 1),
                   mortar: tuple[int, int] = (0, 1), body: tuple[int, int, int] = (3, 4, 5),
                   hi: int = 6, hi2: int = 5, lo: int = 2, lo_right: int = 3) -> list[list[int]]:
    """
    Schattiert ein Mauerwerk-Raster. Jedes Zeichen ist ein Stein (gleiches Zeichen = gleicher Stein,
    auch über den Rand hinweg), '.' ist Fuge. Ergebnis sind Palettenindizes (0 dunkel .. 6 hell):
    Fuge 0-1, untere Steinkante 2, rechte Kante 3, Fläche 3-5, obere/linke Kante 5-6.
    """
    rows = grid(layout)
    rng = random.Random(seed)
    noise = soft_noise(seed, smooth)

    def sid(x: int, y: int) -> str:
        return rows[y % N][x % N]

    stones = sorted({c for r in rows for c in r if c != "."})
    stone_tone = {s: rng.choice(tone) for s in stones}
    out = [[0] * N for _ in range(N)]
    for y in range(N):
        for x in range(N):
            s = sid(x, y)
            n = noise[y][x]
            if s == ".":
                out[y][x] = mortar[1] if n > 0.42 else mortar[0]
                continue
            up, down = sid(x, y - 1) != s, sid(x, y + 1) != s
            left, right = sid(x - 1, y) != s, sid(x + 1, y) != s
            if down:
                v = lo
            elif up:
                v = hi if n > 0.22 else hi2
                if right:
                    v = body[1]
            elif left:
                v = hi2 if n > 0.3 else body[2]
            elif right:
                v = lo_right
            else:
                q = -1 if n < 0.3 else (1 if n > 0.74 else 0)
                v = max(body[0], min(body[2], body[1] + stone_tone[s] + q))
            out[y][x] = v
    return out


WARM_TINT = hexc("#9a7a58")
COOL_TINT = hexc("#5d6f85")


def tinted(colors: list[Color], tint: Color, amount: float) -> list[Color]:
    return [(round(c[0] + (tint[0] - c[0]) * amount), round(c[1] + (tint[1] - c[1]) * amount),
             round(c[2] + (tint[2] - c[2]) * amount), c[3]) for c in colors]


def masonry_image(layout: str, idx: list[list[int]], colors: list[Color], seed: int, strength: float = 0.1) -> Image.Image:
    """Wie from_indices, aber jeder Stein bekommt eine leichte eigene Tönung (warm, kühl oder neutral)."""
    rows = grid(layout)
    rng = random.Random(seed * 7 + 3)
    variants = [colors, tinted(colors, WARM_TINT, strength), tinted(colors, COOL_TINT, strength)]
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


# --- Mittelalterlicher Stein: unregelmäßige Quader (ein großer Stein, zwei kleine, dann zwei lange) ---
MEDIEVAL_LAYOUT = """
AAAAAAAAA.BBBBB.
AAAAAAAAA.BBBBB.
AAAAAAAAA.BBBBB.
AAAAAAAAA.......
AAAAAAAAA.CCCCC.
AAAAAAAAA.CCCCC.
AAAAAAAAA.CCCCC.
................
DDD.EEEEEEEE.DDD
DDD.EEEEEEEE.DDD
DDD.EEEEEEEE.DDD
DDD.EEEEEEEE.DDD
DDD.EEEEEEEE.DDD
DDD.EEEEEEEE.DDD
DDD.EEEEEEEE.DDD
................
"""
MEDIEVAL = pal("#4a4641", "#57524c", "#655f58", "#726c65", "#7f7971", "#8c867d", "#9e978d")


def medieval_indices() -> list[list[int]]:
    idx = ashlar_indices(MEDIEVAL_LAYOUT, 11)
    # ein paar Meißelspuren in den großen Steinen
    return edit(idx, """
................
................
................
....3...........
.....3..........
................
................
................
................
................
.......3........
........3.......
................
................
................
................
""", {"3": 3})


def medieval_stone() -> Image.Image:
    return masonry_image(MEDIEVAL_LAYOUT, medieval_indices(), MEDIEVAL, 11)


MOSS = {"d": hexc("#3c4f25"), "m": hexc("#4f672e"), "M": hexc("#647f39"), "h": hexc("#7d9846")}


def mossy_wall_stones() -> Image.Image:
    """Mittelalterlicher Stein, Moos wächst aus den Fugen und hängt über die Steinkanten."""
    return overlay(medieval_stone(), """
..mMm....dm.....
...m.....mm.....
.........m......
........dmMhm...
.........mMm....
.........dm.....
..........d.....
.dmMm...dmMMmd..
..mhm.....mMm...
...m.......m....
...d............
................
............d...
...........dmm..
...d.......mMm..
dmMhmd...dmMhMmd
""", MOSS)


def cracked_stone_blocks() -> Image.Image:
    """Mittelalterlicher Stein mit feinen Rissen (dunkle Rille, rechts/unten helle Bruchkante) und Abplatzern."""
    idx = edit(medieval_indices(), """
......kl........
......kl..m.....
.....kl.........
.....kl.kl......
....kkkkl.......
...kl.......k...
............kl..
................
...........k....
....kkkl...kl...
.......kkl.kl...
........kkkl....
...........kl...
...........kl...
...........m....
................
""", {"k": 0, "l": 6, "m": 1})
    return masonry_image(MEDIEVAL_LAYOUT, idx, MEDIEVAL, 11)


# --- Grober Naturstein: runde Feldsteine verschiedener Farbe in Mörtel ---
FIELDSTONES = [  # (x, y, Radius)
    (3.0, 2.5, 3.4), (10.0, 2.0, 3.0), (15.5, 6.0, 2.6), (6.5, 7.5, 3.0), (12.0, 9.0, 2.8),
    (2.0, 11.0, 2.9), (8.0, 13.0, 3.1), (14.0, 14.5, 2.4), (0.5, 6.5, 1.6),
]
FIELD_PALETTES = [
    pal("#4d4a46", "#5b5854", "#686561", "#75716c", "#827d77", "#8e8983", "#9e9891"),   # grau
    pal("#4d463e", "#5b5349", "#696054", "#766c5f", "#83786a", "#8f8475", "#9f9384"),   # bräunlich
    pal("#484a4c", "#56585b", "#636669", "#707376", "#7d8083", "#8a8d90", "#9a9da0"),   # kühl grau
]
FIELD_MORTAR = pal("#403d39", "#4b4843")


def blob_layout(stones: list[tuple[float, float, float]], gap: float = 1.15) -> list[list[int]]:
    """Ordnet jedem Pixel den nächsten Stein zu (Abstand minus Radius, kachelbar); -1 = Mörtel."""
    out = [[-1] * N for _ in range(N)]
    for y in range(N):
        for x in range(N):
            ds = []
            for i, (cx, cy, r) in enumerate(stones):
                dx = min(abs(x + 0.5 - cx), N - abs(x + 0.5 - cx))
                dy = min(abs(y + 0.5 - cy), N - abs(y + 0.5 - cy))
                ds.append((math.hypot(dx, dy) - r, i))
            ds.sort()
            if ds[1][0] - ds[0][0] >= gap and ds[0][0] < 0.6:
                out[y][x] = ds[0][1]
    return out


def rough_fieldstone() -> Image.Image:
    ids = blob_layout(FIELDSTONES)
    layout = "\n".join("".join("." if v < 0 else "ABCDEFGHIJKLMNOP"[v] for v in row) for row in ids)
    idx = ashlar_indices(layout, 23, tone=(-1, 0, 0), hi=5, hi2=5)
    rng = random.Random(23)
    family = [rng.randrange(3) for _ in FIELDSTONES]
    img = Image.new("RGBA", (N, N), CLEAR)
    px = img.load()
    for y in range(N):
        for x in range(N):
            if ids[y][x] < 0:
                px[x, y] = FIELD_MORTAR[min(1, idx[y][x])]
            else:
                px[x, y] = FIELD_PALETTES[family[ids[y][x]]][idx[y][x]]
    return img


# --- Behauener Sandstein: große Quader mit feinen waagerechten Schichtlinien ---
SANDSTONE_LAYOUT = """
AAAAAAAAAA.BBBB.
AAAAAAAAAA.BBBB.
AAAAAAAAAA.BBBB.
AAAAAAAAAA.BBBB.
AAAAAAAAAA.BBBB.
AAAAAAAAAA.BBBB.
AAAAAAAAAA.BBBB.
................
CCCCC.DDDDDD.CCC
CCCCC.DDDDDD.CCC
CCCCC.DDDDDD.CCC
CCCCC.DDDDDD.CCC
CCCCC.DDDDDD.CCC
CCCCC.DDDDDD.CCC
CCCCC.DDDDDD.CCC
................
"""
SANDSTONE = pal("#8a6c45", "#9e7f55", "#b08f62", "#bf9d6d", "#cbaa78", "#d6b684", "#e3c597")


def hewn_sandstone() -> Image.Image:
    """Sandstein-Quader: helle Oberkante, waagerechte Schichtbänder (wellig) und feine Körnung."""
    idx = ashlar_indices(SANDSTONE_LAYOUT, 31, tone=(0, 0, 1), smooth=0.75)
    rows = grid(SANDSTONE_LAYOUT)
    rng = random.Random(31)
    for y in range(N):
        for x in range(N):
            if rows[y][x] == "." or rows[(y - 1) % N][x] == "." or rows[(y + 1) % N][x] == ".":
                continue
            band = (y + (1 if (x // 5) % 2 else 0)) % 3     # leicht wellige Schichten
            if band == 0:
                idx[y][x] = max(3, idx[y][x] - 1)
            elif band == 2 and rng.random() < 0.35:
                idx[y][x] = min(5, idx[y][x] + 1)
    return masonry_image(SANDSTONE_LAYOUT, idx, SANDSTONE, 31, 0.07)


# --- Ziegel-Mauerwerk (Dunkle Steinziegel, Helle Kalksteinziegel) ---
def brick_layout(course_height: int, lengths: list[list[int]], offsets: list[int]) -> str:
    """Erzeugt ein Raster aus Ziegelreihen: je Reihe Ziegellängen (inkl. Fuge) und Versatz."""
    rows = []
    sid = 0
    letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"
    for course, (lens, off) in enumerate(zip(lengths, offsets)):
        line = [""] * N
        x = off
        ids = []
        for length in lens:
            ids.append((x, length, letters[sid % len(letters)]))
            sid += 1
            x += length
        cells = ["."] * N
        for start, length, ch in ids:
            for k in range(length - 1):
                cells[(start + k) % N] = ch
        for _ in range(course_height - 1):
            rows.append("".join(cells))
        rows.append("." * N)
    return "\n".join(rows)


DARK_BRICKS = pal("#22232a", "#2b2d34", "#35373f", "#3f424b", "#494c56", "#545864", "#626774")
DARK_BRICKS_LAYOUT = brick_layout(4, [[8, 8], [8, 8], [8, 8], [8, 8]], [0, 4, 0, 4])


def dark_stone_bricks() -> Image.Image:
    idx = ashlar_indices(DARK_BRICKS_LAYOUT, 41)
    # zwei abgeschlagene Ziegelecken
    idx = edit(idx, """
................
................
.......1........
................
................
................
................
...............1
................
................
................
................
....1...........
................
................
................
""", {"1": 1})
    return masonry_image(DARK_BRICKS_LAYOUT, idx, DARK_BRICKS, 41, 0.12)


LIMESTONE = pal("#9b9484", "#ada796", "#bdb7a7", "#cac5b5", "#d5d0c1", "#dfdbcd", "#ebe8dc")
LIMESTONE_LAYOUT = brick_layout(4, [[10, 6], [6, 10], [10, 6], [6, 10]], [0, 3, 8, 13])


def limestone_bricks() -> Image.Image:
    idx = ashlar_indices(LIMESTONE_LAYOUT, 51, tone=(0, 0, 1, -1))
    # kleine Poren im Kalkstein
    idx = edit(idx, """
................
....2...........
...........2....
................
................
.......2........
................
................
.............2..
................
..2.............
................
................
.........2......
................
................
""", {"2": 2})
    return masonry_image(LIMESTONE_LAYOUT, idx, LIMESTONE, 51, 0.08)


# --- Verwitterte Steinziegel: abgerundete, ausgebrochene Kanten, Flechten ---
WEATHERED_LAYOUT = """
AAAAAAA.BBBBBBB.
AAAAAAA.BBBBBBB.
AAAAAAA.BBBBBBB.
AAAAAAA.BBBBBBB.
AAAAAAA.BBBBBBB.
AAAAAAA.BBBBBBB.
AAAAAAA.BBBBBBB.
................
CCC.DDDDDDD.CCCC
CCC.DDDDDDD.CCCC
CCC.DDDDDDD.CCCC
CCC.DDDDDDD.CCCC
CCC.DDDDDDD.CCCC
CCC.DDDDDDD.CCCC
CCC.DDDDDDD.CCCC
................
"""
WEATHERED = pal("#3e3f39", "#4a4c44", "#575950", "#64665c", "#717368", "#7e8074", "#8d8f82")
LICHEN = {"y": hexc("#8c935a"), "Y": hexc("#a3a96c"), "o": hexc("#6e7246")}


def weathered_stone_bricks() -> Image.Image:
    idx = ashlar_indices(WEATHERED_LAYOUT, 61, tone=(-1, 0, 0))
    # ausgebrochene Ecken und Kanten (Fugenfarbe), Löcher
    idx = edit(idx, """
0.....00......00
0..............0
.........1......
................
.....1..........
...............1
0......00......0
................
0..0........0..0
...........1...0
.......1........
................
..1.............
................
0..00......00..0
................
""", {"0": 1, "1": 2})
    img = masonry_image(WEATHERED_LAYOUT, idx, WEATHERED, 61, 0.1)
    return overlay(img, """
................
...........yY...
..........oy....
................
................
................
................
................
................
....Yy..........
....oy..........
................
.............y..
............oyY.
................
................
""", LICHEN)


# =============================================================================
# Steinpfeiler
# =============================================================================

PILLAR = pal("#5c5852", "#6b6760", "#7a756e", "#87827a", "#948f86", "#a19b92", "#b1aba1")


def stone_pillar_side() -> Image.Image:
    """Säulenschaft: rund schattiert (links hell, rechts dunkel) mit drei Kanneluren (Rillen)."""
    profile = [2, 4, 5, 5, 6, 5, 5, 5, 5, 4, 4, 4, 3, 3, 3, 1]
    flutes = {4: 3, 8: 2, 12: 1}
    noise = soft_noise(71, 0.8)
    rows = []
    for y in range(N):
        row = []
        for x in range(N):
            v = profile[x]
            if x in flutes:
                v = flutes[x]
            elif x - 1 in flutes:
                v = min(6, profile[x] + 1)  # Kante rechts der Rille im Licht
            elif noise[y][x] > 0.85 and 0 < x < 15:
                v = max(1, v - 1)
            row.append(v)
        rows.append(row)
    return from_indices(rows, PILLAR)


def stone_pillar_top() -> Image.Image:
    """Säulenkopf: runde Säule (Ring mit Rille) auf quadratischer Platte."""
    rows = []
    for y in range(N):
        row = []
        for x in range(N):
            d = math.hypot(x - 7.5, y - 7.5)
            if x in (0, 15) or y in (0, 15):
                v = 2 if (x == 15 or y == 15) else 5
            elif d > 7.2:
                v = 3
            elif d > 6.2:
                v = 2  # Fuge um die Säule
            elif d > 5.2:
                v = 5 if (x + y) < 15 else 4
            elif d > 4.2:
                v = 3  # Rille
            else:
                v = 4 if d > 1.5 else 5
            row.append(v)
        rows.append(row)
    return from_indices(rows, PILLAR)


# =============================================================================
# Holz und Fachwerk
# =============================================================================

BEAM = pal("#2a1b11", "#372316", "#452d1c", "#533722", "#614129", "#704c31", "#80583a")
PLASTER = pal("#a59c8a", "#bab19f", "#cbc3b1", "#d8d1c0", "#e2dccd", "#ebe6da")


def wood_grain(seed: int, along_x: bool) -> list[list[int]]:
    """Maserung in Längsrichtung: Werte -1..1, Streifen quer zur Faser, längliche Flecken entlang der Faser."""
    rng = random.Random(seed)
    across = [rng.choice([-1, 0, 0, 0, 1]) for _ in range(N)]
    out = [[0] * N for _ in range(N)]
    for i in range(N):  # i = Position quer zur Faser
        j = 0
        while j < N:
            length = rng.randint(4, 9)
            val = across[i] + rng.choice([-1, 0, 0, 0, 0, 1])
            for k in range(length):
                jj = (j + k) % N
                if along_x:
                    out[i][jj] = max(-1, min(1, val))
                else:
                    out[jj][i] = max(-1, min(1, val))
            j += length
    return out


def timber_frame(diagonals: str, seed: int) -> Image.Image:
    """
    Fachwerk-Feld: 2 Pixel breiter Balkenrahmen um ein Putzfeld, optional mit Streben.
    diagonals: "" (leer), "/" (eine Strebe) oder "x" (Andreaskreuz).
    """
    grain_h = wood_grain(seed, along_x=True)
    grain_v = wood_grain(seed + 1, along_x=False)
    grain_d = wood_grain(seed + 2, along_x=True)
    noise = soft_noise(seed + 3, 0.7)

    def beam_kind(x: int, y: int) -> str | None:
        if y in (0, 1) or y in (14, 15):
            return "h"
        if x in (0, 1) or x in (14, 15):
            return "v"
        if diagonals in ("/", "x") and x + y in (14, 15, 16):
            return "/"
        if diagonals == "x" and x - y in (-1, 0, 1):
            return "\\"
        return None

    is_beam = [[beam_kind(x, y) is not None for x in range(N)] for y in range(N)]
    img = Image.new("RGBA", (N, N), CLEAR)
    px = img.load()
    for y in range(N):
        for x in range(N):
            kind = beam_kind(x, y)
            if kind == "h":
                v = {0: 5, 1: 3, 14: 5, 15: 3}[y] + grain_h[y][x]
            elif kind == "v":
                v = {0: 5, 1: 3, 14: 5, 15: 3}[x] + grain_v[y][x]
            elif kind == "/":
                edge = {14: 5, 15: 4, 16: 2}[x + y]
                v = edge + (0 if edge == 2 else grain_d[(x - y) % N][(x + y) % N])
            elif kind == "\\":
                edge = {-1: 5, 0: 4, 1: 2}[x - y]
                v = edge + (0 if edge == 2 else grain_d[(x + y) % N][(x - y) % N])
            else:
                v = None
            if v is not None:
                px[x, y] = BEAM[max(1, min(6, v))]
                continue
            # Putz: Schatten unter und rechts neben Balken
            n = noise[y][x]
            if is_beam[y - 1][x]:
                v = 1
            elif is_beam[y][x - 1] or is_beam[y - 1][x - 1]:
                v = 2
            else:
                v = 4 if n > 0.32 else 3
                if n > 0.86:
                    v = 5
            px[x, y] = PLASTER[v]
    return img


def timber_beam_side() -> Image.Image:
    """Eichenbalken längs: ruhige, leicht gewellte Maserung mit zwei dunklen Faserlinien und einem Haarriss."""
    base = [4, 4, 5, 4, 3, 4, 4, 4, 5, 5, 4, 4, 3, 4, 4, 5]

    def shift(y: int) -> int:
        return 1 if 6 <= y < 11 else 0

    rows = [[base[(x + shift(y)) % N] for x in range(N)] for y in range(N)]
    return from_indices(edit(rows, """
................
.............2..
.......6.....2..
.......6........
................
....2...........
....2...........
................
..............6.
..............6.
................
..........2.....
..........2.....
..........2.....
.2..............
.2..............
""", {"2": 2, "6": 6}), BEAM)


def timber_beam_top() -> Image.Image:
    """Hirnholz: Jahresringe in einem kantigen Balken, Mark in der Mitte, ein Trocknungsriss."""
    rows = []
    for y in range(N):
        row = []
        for x in range(N):
            d = max(abs(x - 7.5), abs(y - 7.5)) * 0.65 + math.hypot(x - 7.5, y - 7.5) * 0.35
            if x in (0, 15) or y in (0, 15):
                v = 2
            else:
                v = (5, 4, 3)[int(d * 1.15) % 3]
                if d < 1.2:
                    v = 2
            row.append(v)
        rows.append(row)
    rows = edit(rows, """
................
................
................
................
................
................
................
........11......
..........1111..
..............1.
................
................
................
................
................
................
""", {"1": 1})
    return from_indices(rows, BEAM)


# =============================================================================
# Dächer
# =============================================================================

def roof_tiles(colors: list[Color], seed: int) -> Image.Image:
    """
    Rundziegel in Reihen (4 Pixel hoch, 8 breit, jede Reihe um einen halben Ziegel versetzt):
    oben der Schatten der Reihe darüber, quer gewölbt (Mitte hell, Ränder dunkel), runde Unterkante;
    jeder Ziegel leicht anders getönt.
    """
    tile = [
        [1, 0, 0, 0, 0, 0, 0, 1],
        [2, 3, 4, 5, 5, 4, 3, 2],
        [2, 4, 5, 6, 5, 4, 3, 2],
        [1, 3, 4, 5, 5, 4, 3, 1],
    ]
    rng = random.Random(seed)
    tone = [[rng.choice([-1, 0, 0, 0, 1]) for _ in range(2)] for _ in range(4)]
    rows = []
    for y in range(N):
        r, ty = y // 4, y % 4
        off = 4 * (r % 2)
        row = []
        for x in range(N):
            v = tile[ty][(x + off) % 8]
            if v >= 2:
                v = max(2, min(6, v + tone[r][((x + off) // 8) % 2]))
            row.append(v)
        rows.append(row)
    return from_indices(rows, colors)


RED_TILES = pal("#4a1a12", "#6a2417", "#86301d", "#9e3b23", "#b4492b", "#c55a35", "#d66d45")
BROWN_TILES = pal("#2e1d14", "#432a1c", "#563624", "#68422c", "#7a4f35", "#8c5d3f", "#a06d4b")
LIGHT_TILES = pal("#74472a", "#8f5d39", "#a87246", "#bc8654", "#cb9763", "#d8a975", "#e6bd8d")


SLATE = pal("#1d2027", "#262a33", "#30353f", "#3b414c", "#474e5a", "#545c69", "#657080")


def dark_slate_shingles() -> Image.Image:
    """Schieferplatten (8x4), Reihen um eine halbe Platte versetzt, unten schräg abgeschnittene Ecken."""
    plate = [  # Spalte 0 = Spalt; Zeile 0 = Schatten der Reihe darüber
        [0, 1, 1, 1, 1, 1, 1, 1],
        [1, 4, 5, 4, 4, 4, 4, 3],
        [1, 4, 4, 4, 4, 3, 4, 3],
        [0, 2, 3, 3, 3, 3, 2, 1],
    ]
    rng = random.Random(81)
    tone = [[rng.choice([-1, 0, 0, 1]) for _ in range(2)] for _ in range(4)]
    rows = []
    for y in range(N):
        r, ty = y // 4, y % 4
        off = 4 * (r % 2)
        row = []
        for x in range(N):
            tx = (x + off) % 8
            v = plate[ty][tx]
            if v >= 2:
                v = max(2, min(6, v + tone[r][((x + off) // 8) % 2]))
            row.append(v)
        rows.append(row)
    return from_indices(rows, SLATE)


def wooden_shingles() -> Image.Image:
    """Holzschindeln: Reihen à 4 Pixel, Schindeln 3-5 Pixel breit, Spalt dazwischen, helle Unterkante."""
    widths = [[4, 5, 3, 4], [5, 3, 4, 4], [3, 4, 5, 4], [4, 4, 3, 5]]
    offsets = [0, 2, 1, 3]
    rng = random.Random(91)
    rows = [[0] * N for _ in range(N)]
    for r in range(4):
        x = offsets[r]
        for w in widths[r]:
            tone = rng.choice([-1, 0, 0, 1])
            grain = rng.randrange(1, w)
            for k in range(w):
                col = (x + k) % N
                for ty in range(4):
                    if k == 0:
                        v = 1 if ty < 3 else 2          # Spalt zwischen den Schindeln
                    elif ty == 0:
                        v = 2                           # Schatten der Reihe darüber
                    elif ty == 3:
                        v = 5                           # Unterkante im Licht
                    else:
                        v = 3 if k == grain else 4      # eine Faserlinie je Schindel
                    rows[r * 4 + ty][col] = max(1, min(6, v + (tone if v > 2 else 0)))
            x += w
    return from_indices(rows, pal("#3b2d21", "#4c3a2a", "#5d4834", "#6e573f", "#80674b", "#917757", "#a58a69"))


def copper_roof() -> Image.Image:
    """Kupferdach mit Stehfalz: zwei Bahnen je Block, erhabene Naht (hell) mit Schatten daneben."""
    noise = soft_noise(97, 0.7)
    rows = []
    for y in range(N):
        row = []
        for x in range(N):
            tx = x % 8
            if tx == 0:
                v = 6                      # Falz im Licht
            elif tx == 1:
                v = 2                      # Schatten rechts der Naht
            elif tx == 7:
                v = 3
            else:
                v = 4 if noise[y][x] > 0.3 else 3
                if noise[y][x] > 0.9:
                    v = 5
            row.append(v)
        rows.append(row)
    return from_indices(rows, pal("#6b3220", "#83402a", "#9c4f34", "#b35f3e", "#c7704a", "#d8835a", "#e99c74"))


# =============================================================================
# Erze (nur die Kristalle; der Stein dahinter kommt aus dem Spiel)
# =============================================================================

def ore(ascii_map: str, colors: dict[str, str]) -> Image.Image:
    return from_ascii(ascii_map, {k: hexc(v) for k, v in colors.items()})


CARBON_ORE = """
................
..dd............
.dmmd......dd...
.dmhmd....dmmd..
..dmd.....dmld..
...........dd...
.....dd.........
....dmmd........
....dmlmd...dd..
.....dmd...dmmd.
...........dmhd.
.dd.........dd..
dmmd............
dmld.....dd.....
.dd.....dmmd....
.........dd.....
"""
CARBON_COLORS = {"d": "#141417", "m": "#26272c", "l": "#3e3f47", "h": "#8a8d98"}

GEMSTONE_ORE = """
................
..ab............
.abce......12...
.bcc......1234..
..b........23...
......ab........
.....abce.......
.....bcc........
......b.....ab..
...........abce.
.12........bcc..
1234........b...
.23.....56......
.......5677.....
........67......
................
"""
GEMSTONE_COLORS = {"a": "#3d1556", "b": "#6d2694", "c": "#9d4ccb", "e": "#e0b5ff",
                   "1": "#0d4a2a", "2": "#1c8a4a", "3": "#3fd17a", "4": "#c2ffd9",
                   "5": "#0e4a5c", "6": "#1aa3b8", "7": "#9ef3ff"}

KONTERIT_ORE = """
................
.....h......h...
....cl.....cl...
...dcl....dc....
...dc.....d.....
...d............
..........h.....
.........cl.....
....h...dcl.....
...cl...dc......
..dcl...d.......
..dc.........h..
..d.........cl..
...........dcl..
...........dc...
...........d....
"""
KONTERIT_COLORS = {"d": "#3b1458", "c": "#8a2fc0", "l": "#b75ae6", "h": "#f0c8ff"}

AETHERIT_ORE = """
................
..h.........l...
.lm........lhd..
.md........md...
..d.........d...
......l.........
.....lhm........
.....mmd....l...
......d....lhm..
...........mmd..
..l.........d...
.lhm............
.mmd.....l......
..d.....lhm.....
........mmd.....
.........d......
"""
AETHERIT_COLORS = {"d": "#0b3d4f", "m": "#138a9e", "l": "#3fd3e3", "h": "#d4fbff"}
AETHERIT_GLOW = {"l": "#3fd3e3", "h": "#d4fbff"}

DRACONIUM_ORE = """
................
...l............
..lhm......l....
..mmd.....lhm...
...d......mmd...
...........d....
......l.........
.....lhm........
.....mmd.....l..
......d.....lhm.
............mmd.
..l..........d..
.lhm.....l......
.mmd....lhm.....
..d.....mmd.....
.........d......
"""
DRACONIUM_COLORS = {"d": "#3a0707", "m": "#86121a", "l": "#d6302c", "h": "#ffb08a"}
DRACONIUM_GLOW = {"l": "#d6302c", "h": "#ffb08a"}

WYVERNIT_ORE = """
................
...h............
...l........h...
..lm........l...
..md.......lm...
...d.......md...
............d...
.......h........
.......l........
......lm........
......md........
.......d....h...
............l...
..h........lm...
..l........md...
.lm.........d...
"""
WYVERNIT_COLORS = {"d": "#2b4b6b", "m": "#5d8dba", "l": "#a2cdf0", "h": "#effaff"}


def ore_glow(ascii_map: str, colors: dict[str, str]) -> Image.Image:
    return from_ascii(ascii_map, {k: hexc(v) for k, v in colors.items()})


# =============================================================================
# Rohstoffblöcke
# =============================================================================

def bevel_block(colors: list[Color], inner: Callable[[int, int], int], frame: bool = True) -> Image.Image:
    """Block mit abgefaster Kante (oben/links hell, unten/rechts dunkel) und Innenmuster."""
    rows = []
    for y in range(N):
        row = []
        for x in range(N):
            if frame and (y == 0 or x == 0):
                v = 5 if not (x == 15 or y == 15) else 3
            elif frame and (y == 15 or x == 15):
                v = 1
            elif frame and (y == 1 or x == 1):
                v = 4
            elif frame and (y == 14 or x == 14):
                v = 2
            else:
                v = inner(x, y)
            row.append(v)
        rows.append(row)
    return from_indices(rows, colors)


def gemstone_block() -> Image.Image:
    """Edelsteinblock – der Schatz der Drachen: vier geschliffene lila Steine in einer Goldfassung."""
    return from_ascii("""
FGGGGGGGGGGGGGGf
G.1111.GG.1111.f
G165541GG165541f
G164431GG164431f
G154331GG154331f
G143321GG143321f
G.1111.Gg.1111.f
GGGGGGGggggggggf
GGGGGGGgggggggfF
G.1111.gg.1111.f
G165541gg165541f
G164431gg164431f
G154331gg154331f
G143321gg143321f
G.1111.gg.1111.f
fffffffffffffffF
""".replace(".", "G"), {
        "F": hexc("#5a3d0e"), "f": hexc("#7d5a18"), "G": hexc("#b48a2c"), "g": hexc("#9a7424"),
        "1": hexc("#2e0f47"), "2": hexc("#4a1a6e"), "3": hexc("#652a91"), "4": hexc("#8240b6"),
        "5": hexc("#a564d6"), "6": hexc("#e2bcff"),
    })


def konterit_block() -> Image.Image:
    """Leichtes Lüfte-Metall (silbrig-lila): glatte Platte mit erhabener Raute in der Mitte."""
    def inner(x: int, y: int) -> int:
        d = abs(x - 7.5) + abs(y - 7.5)
        if 4.5 <= d < 5.5:
            return 5 if (x < 8 and y < 8) or (x < 8 <= y and x + 3 < y) else 2
        if d < 4.5:
            return 4 if (x + y) < 15 else 3
        return 3
    return bevel_block(pal("#3d3352", "#4f4468", "#62577e", "#776b94", "#8c80a9", "#a497bf", "#c2b6da"), inner)


AETHERIT_BLOCK = pal("#06303e", "#0b4558", "#115e74", "#18798f", "#2196aa", "#3cb7c6", "#86e3ea")


def aetherit_block() -> Image.Image:
    """Magischer Kristall, wie ein geschliffener Stein von oben: vier Facetten und ein heller Kern."""
    def inner(x: int, y: int) -> int:
        cx, cy = x - 7.5, y - 7.5
        if abs(cx) <= 2 and abs(cy) <= 2:
            return 6 if cx + cy < -1.5 else 5        # Tafel in der Mitte
        if abs(cx - cy) < 0.6:
            return 6 if cx < 0 else 1                 # Kante oben links hell, unten rechts dunkel
        if abs(cx + cy) < 0.6:
            return 4
        if cy < -abs(cx):
            return 5                                  # obere Facette
        if cx < -abs(cy):
            return 4                                  # linke Facette
        if cx > abs(cy):
            return 3                                  # rechte Facette
        return 2                                      # untere Facette
    return bevel_block(AETHERIT_BLOCK, inner)


AETHERIT_BLOCK_GLOW = """
................
................
................
................
................
................
......hhhh......
......hhhh......
......hhhh......
......hhhh......
................
................
................
................
................
................
"""

DRACONIUM_VEINS = [  # Glutadern als Linienzüge (kachelbar: was rechts/unten hinausläuft, kommt links/oben wieder)
    [(2, 0), (3, 2), (5, 4), (7, 6), (9, 9), (12, 10), (15, 13)],
    [(0, 14), (2, 15)],
    [(7, 6), (9, 4), (11, 2), (13, 0)],
    [(13, 15), (12, 14)],
    [(9, 9), (8, 12), (6, 13)],
]
DRACONIUM_BLOCK = pal("#1a0303", "#260505", "#350808", "#460b0b", "#5a1010", "#721616", "#932020")


def draconium_veins() -> tuple[set[tuple[int, int]], set[tuple[int, int]]]:
    """Kern- und Randpixel der Glutadern."""
    core: set[tuple[int, int]] = set()
    for path in DRACONIUM_VEINS:
        for (x0, y0), (x1, y1) in zip(path, path[1:]):
            steps = max(abs(x1 - x0), abs(y1 - y0))
            for i in range(steps + 1):
                core.add((round(x0 + (x1 - x0) * i / steps) % N, round(y0 + (y1 - y0) * i / steps) % N))
    rim = {((x + 1) % N, y) for x, y in core} | {(x, (y + 1) % N) for x, y in core}
    return core, rim - core


def draconium_block() -> Image.Image:
    """Drachenessenz: dunkelroter Kristall mit unregelmäßigen, glühenden Adern."""
    noise = soft_noise(111, 0.6)
    rows = [[3 if noise[y][x] > 0.66 else (2 if noise[y][x] > 0.25 else 1) for x in range(N)] for y in range(N)]
    img = from_indices(rows, DRACONIUM_BLOCK)
    px = img.load()
    core, rim = draconium_veins()
    for x, y in rim:
        px[x, y] = hexc("#8e1a14")
    for x, y in core:
        px[x, y] = hexc("#ff6a3a") if (x + y) % 3 else hexc("#ffb38a")
    return img


def draconium_block_glow() -> Image.Image:
    img = Image.new("RGBA", (N, N), CLEAR)
    px = img.load()
    core, _ = draconium_veins()
    for x, y in core:
        px[x, y] = hexc("#ff6a3a") if (x + y) % 3 else hexc("#ffb38a")
    return img


def wyvernit_block() -> Image.Image:
    """Höhenkristall: eisblaue Kristallsäulen nebeneinander, in verschiedener Höhe abgebrochen."""
    breaks = {0: 5, 1: 11, 2: 2, 3: 8}
    profile = [5, 4, 4, 2]
    rows = []
    for y in range(N):
        row = []
        for x in range(N):
            col, tx = x // 4, x % 4
            b = breaks[col]
            if y == b:
                v = 1                              # Fuge zwischen zwei Säulenstücken
            elif y == (b + 1) % N:
                v = 6 if tx < 3 else 4             # helle Oberseite des unteren Stücks
            else:
                v = profile[tx]
                if tx in (1, 2) and (y - b) % N in (3, 4) and col % 2 == 0:
                    v = 5                          # Glanz
            row.append(v)
        rows.append(row)
    return from_indices(rows, pal("#456a8f", "#567ea6", "#6a93bb", "#82aad0", "#9cc1e2", "#bad7f0", "#e3f2fb"))


def carbon_block() -> Image.Image:
    """Gepresster Kohlenstoff: dunkle, geschichtete Platten mit silbrig glänzender Oberkante."""
    return from_indices(digits("""
4544444454444445
3333233333332333
3322222332222233
2222112222211222
1111111111111111
4444445444444654
3332333333333333
2222233222222333
2211222221122222
1111111111111111
4464444445444444
3333332333333233
2223333222333322
1122222111222211
1111111111111111
0000000000000000
"""), pal("#121215", "#1a1a1f", "#232329", "#2d2e35", "#3a3b44", "#4c4d58", "#7c7e8c"))


def steel_block() -> Image.Image:
    """Vier Stahlplatten mit abgefasten Kanten, Nieten an den Ecken und feinen Schleifspuren."""
    return from_indices(digits("""
5555555355555553
5644464256444642
5424442254244422
5443344254443342
5444434254434442
5644464256444642
5424442254244422
3222222132222221
5555555355555553
5644464256444642
5424442254244422
5444334254334442
5443444254444342
5644464256444642
5424442254244422
3222222132222221
"""), pal("#2a2f36", "#383e46", "#4a515a", "#5b636d", "#6d7681", "#838c97", "#a2abb5"))


# =============================================================================
# Dracheneier
# =============================================================================
# Form und Machart wie beim echten Drachenei aus dem Ende (siehe ModModelProvider): mehrere ineinander
# geschobene Quader ergeben die runde Eiform, jede Seite zeigt den Teil der Textur, der ihrer Lage
# entspricht. Die Textur ist eine gesprenkelte Schale ohne eingemalte Licht-Kanten – die Rundung
# kommt allein von der Form. Jedes Element hat seine eigenen Farben und Sprenkel.

EGG_STYLES = {
    # Grundtöne der Schale (dunkel -> hell), Sprenkel (Rand, Mitte, Glanz), Funkel-Pixel
    "fire": (pal("#1c0504", "#2a0806", "#3a0c08", "#4c120b"), pal("#8a2209", "#d1521a", "#ffb347"), "#ffe9a0"),
    "ice": (pal("#8fb7d4", "#a7cbe3", "#bcdaee", "#d3e8f6"), pal("#4c7ea8", "#6e9cc2", "#e9f6ff"), "#ffffff"),
    "lightning": (pal("#121319", "#191b23", "#21242e", "#2a2e3b"), pal("#8a6a08", "#e6bf1c", "#fff1a0"), "#bfe8ff"),
    "nature": (pal("#0d220d", "#143014", "#1c3e1b", "#244d22"), pal("#3e7426", "#6faa3e", "#b4e07a"), "#f29ac2"),
    "earth": (pal("#1f140b", "#2b1c0f", "#382514", "#462f1a"), pal("#7a5418", "#c08e34", "#ecc76c"), "#9c9ca4"),
    "water": (pal("#051c33", "#082743", "#0d3456", "#13436c"), pal("#1c6fa0", "#3ea6d6", "#b0ecff"), "#e6fbff"),
}
EGG_SEEDS = {"fire": 311, "ice": 523, "lightning": 727, "nature": 919, "earth": 1103, "water": 1307}


def dragon_egg(kind: str) -> Image.Image:
    shell, spots, sparkle = EGG_STYLES[kind]
    seed = EGG_SEEDS[kind]
    noise = pa.fractal_noise(seed, ((8, 0.45), (4, 0.35), (2, 0.2)))
    img = Image.new("RGBA", (N, N), CLEAR)
    px = img.load()
    for y in range(N):
        for x in range(N):
            # gesprenkelte Schale, unten minimal dunkler (wie beim echten Ei: keine harten Kanten)
            v = noise[y][x] * 0.9 + (0.08 if y < 8 else 0.0)
            px[x, y] = shell[min(3, int(v * 4))]
    rng = random.Random(seed)
    # Sprenkel: kleine Flecken aus 1-4 Pixeln, mit hellem Kern oben links
    for _ in range(12):
        cx, cy = rng.randrange(N), rng.randrange(N)
        size = rng.choice((1, 1, 1, 2, 2, 3))
        cells = [(cx, cy)]
        for _ in range(size):
            bx, by = rng.choice(cells)
            nx, ny = bx + rng.choice((-1, 0, 1)), by + rng.choice((-1, 0, 1))
            if 0 <= nx < N and 0 <= ny < N:
                cells.append((nx, ny))
        for i, (bx, by) in enumerate(cells):
            px[bx, by] = spots[2] if i == 0 and size >= 2 else spots[1] if i < 3 else spots[0]
        # dunkler Saum unten rechts lässt den Fleck leicht vertieft wirken
        bx, by = max(cells, key=lambda c: c[0] + c[1])
        if bx + 1 < N and by + 1 < N and (bx + 1, by + 1) not in cells:
            px[bx + 1, by + 1] = shell[0]
    for _ in range(3):
        px[rng.randrange(N), rng.randrange(N)] = hexc(sparkle)
    return img


# Risse beim Brüten (Blockzustand hatch=1..3). Jede Stufe hat zwei Ebenen über dem Ei:
#  - block/dragon_egg_cracks_N: dunkler Rand neben den Rissen (für alle Eier gleich)
#  - block/<art>_dragon_egg_glow_N: das leuchtende Innere in der Farbe des Elements (selbstleuchtend)
# Die Seiten des Eis zeigen die Texturspalten 2..14; ein Riss über die ganze Breite läuft also
# einmal rund um das Ei.
EGG_CRACK_PATHS = [
    # (ab Stufe, Pixel) – Hauptriss im Zickzack um das obere Drittel, dann Verästelungen
    (1, [(6, 6), (7, 5), (8, 6), (9, 7), (10, 6)]),
    (2, [(4, 6), (5, 7)]),
    (2, [(11, 5), (12, 6)]),
    (2, [(9, 8), (10, 9), (10, 10)]),
    (3, [(2, 6), (3, 5)]),
    (3, [(13, 6), (14, 6)]),
    (3, [(5, 8), (4, 9), (4, 10)]),
    (3, [(7, 4), (8, 3)]),
    (3, [(10, 11), (11, 12)]),
]
EGG_CRACK_HOLES = {3: [(8, 6), (8, 7), (7, 6)]}  # ausgebrochene Schalenstücke
EGG_GLOW = {
    # (Kern, Rand des Leuchtens)
    "fire": ("#ffe28a", "#ff7a1a"),
    "ice": ("#f2fdff", "#8fdcff"),
    "lightning": ("#fffbd0", "#ffd92a"),
    "nature": ("#eeffb0", "#7fe05a"),
    "earth": ("#ffe0a0", "#e09a48"),
    "water": ("#e0fbff", "#4fd0f0"),
}


def egg_crack_pixels(stage: int) -> tuple[list[tuple[int, int]], set[tuple[int, int]]]:
    """Alle Risspixel bis zur Stufe (in Zeichenreihenfolge) und die Endpunkte der Risse."""
    pixels: list[tuple[int, int]] = []
    ends: set[tuple[int, int]] = set()
    for first, path in EGG_CRACK_PATHS:
        if first <= stage:
            pixels.extend(p for p in path if p not in pixels)
            ends.add(path[-1])
            if first == stage == 1:
                ends.add(path[0])
    return pixels, ends


def dragon_egg_cracks(put: Callable[[str, Image.Image], None]) -> None:
    rim_color = hexc("#1b1210")
    for stage in (1, 2, 3):
        cracks, ends = egg_crack_pixels(stage)
        holes = EGG_CRACK_HOLES.get(stage, [])
        core = set(cracks) | set(holes)
        rim = Image.new("RGBA", (N, N), CLEAR)
        rpx = rim.load()
        for x, y in core:
            for nx, ny in ((x, y + 1), (x + 1, y), (x - 1, y + 1)):
                if (nx, ny) not in core and 0 <= nx < N and 0 <= ny < N:
                    rpx[nx, ny] = rim_color
        for x, y in holes:
            # um die Löcher herum ist die Schale abgeplatzt: dunkler Rand ringsum
            for nx, ny in ((x, y - 1), (x - 1, y), (x + 1, y), (x, y + 1)):
                if (nx, ny) not in core:
                    rpx[nx, ny] = rim_color
        put(f"block/dragon_egg_cracks_{stage}", rim)
        for kind, (light, dark) in EGG_GLOW.items():
            glow = Image.new("RGBA", (N, N), CLEAR)
            gpx = glow.load()
            for x, y in cracks:
                gpx[x, y] = hexc(dark) if (x, y) in ends else hexc(light)
            for x, y in holes:
                gpx[x, y] = hexc(light)
            if holes:
                c = hexc(light)
                gpx[holes[0]] = (round(c[0] + (255 - c[0]) * 0.6), round(c[1] + (255 - c[1]) * 0.6),
                                 round(c[2] + (255 - c[2]) * 0.6), 255)
            put(f"block/{kind}_dragon_egg_glow_{stage}", glow)


# =============================================================================
# Sammlung
# =============================================================================

def build(put: Callable[[str, Image.Image], None]) -> None:
    put("block/medieval_stone", medieval_stone())
    put("block/rough_fieldstone", rough_fieldstone())
    put("block/hewn_sandstone", hewn_sandstone())
    put("block/dark_stone_bricks", dark_stone_bricks())
    put("block/limestone_bricks", limestone_bricks())
    put("block/weathered_stone_bricks", weathered_stone_bricks())
    put("block/mossy_wall_stones", mossy_wall_stones())
    put("block/cracked_stone_blocks", cracked_stone_blocks())
    put("block/stone_pillar_side", stone_pillar_side())
    put("block/stone_pillar_top", stone_pillar_top())
    put("block/timber_frame", timber_frame("", 61))
    put("block/timber_frame_diagonal", timber_frame("/", 62))
    put("block/timber_frame_cross", timber_frame("x", 63))
    put("block/timber_beam_side", timber_beam_side())
    put("block/timber_beam_top", timber_beam_top())
    put("block/red_roof_tiles", roof_tiles(RED_TILES, 21))
    put("block/brown_roof_tiles", roof_tiles(BROWN_TILES, 22))
    put("block/light_roof_tiles", roof_tiles(LIGHT_TILES, 23))
    put("block/dark_slate_shingles", dark_slate_shingles())
    put("block/wooden_shingles", wooden_shingles())
    put("block/copper_roof", copper_roof())
    put("block/carbon_ore_overlay", ore(CARBON_ORE, CARBON_COLORS))
    put("block/gemstone_ore_overlay", ore(GEMSTONE_ORE, GEMSTONE_COLORS))
    put("block/konterit_ore_overlay", ore(KONTERIT_ORE, KONTERIT_COLORS))
    put("block/aetherit_ore_overlay", ore(AETHERIT_ORE, AETHERIT_COLORS))
    put("block/aetherit_ore_glow", ore_glow(AETHERIT_ORE, AETHERIT_GLOW))
    put("block/draconium_ore_overlay", ore(DRACONIUM_ORE, DRACONIUM_COLORS))
    put("block/draconium_ore_glow", ore_glow(DRACONIUM_ORE, DRACONIUM_GLOW))
    put("block/wyvernit_ore_overlay", ore(WYVERNIT_ORE, WYVERNIT_COLORS))
    put("block/carbon_block", carbon_block())
    put("block/gemstone_block", gemstone_block())
    put("block/konterit_block", konterit_block())
    put("block/aetherit_block", aetherit_block())
    put("block/draconium_block", draconium_block())
    put("block/draconium_block_glow", draconium_block_glow())
    put("block/aetherit_block_glow", from_ascii(AETHERIT_BLOCK_GLOW, {"h": AETHERIT_BLOCK[6]}))
    put("block/wyvernit_block", wyvernit_block())
    put("block/steel_block", steel_block())
    for kind in EGG_STYLES:
        put(f"block/{kind}_dragon_egg", dragon_egg(kind))
    dragon_egg_cracks(put)
