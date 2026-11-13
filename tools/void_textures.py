#!/usr/bin/env python3
"""
Texturen der Leere (im Vanilla-Stil, siehe tools/vanilla_style.py):

  * Leerenstein (nachtschwarz-bläulich mit eingeschlossenen Sternen), Ziegel, poliert, rissig, gemeißelt (Auge über Riss)
  * Sternenmarmor (hell, violett und golden geädert), Ziegel, Säule, gemeißelt (goldener Stern, leuchtend)
  * Leerenmoos, Sternenstaub (funkelnd, leuchtend), Leerenkristall (durchscheinend), Drusen, Kristalllaub
  * Leerenblüte, Sternenranke
  * der Leerenriss (bewegt: kreisende Sterne in einem violetten Wirbel)
  * Gegenstand: Leerensplitter

Wird von tools/generate_textures.py aufgerufen (build(put)).
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

STONE = vs.tones("#1f1c33", 4, spread=0.22, cool=250, warm=275, hue=10, sat=0.05)
BRICK = vs.tones("#25213d", 7, spread=0.40, cool=250, warm=275, hue=12, sat=0.06)
POLISHED = vs.tones("#231f3a", 6, spread=0.34, cool=250, warm=270, hue=10, sat=0.05)
STAR_COLORS = [hexc("#cfeeff"), hexc("#b7d4ff"), hexc("#e2c8ff"), hexc("#fff6d8")]
MARBLE = vs.tones("#ddd6ea", 5, spread=0.10, cool=255, warm=40, hue=6, sat=0.03)
MARBLE_BRICK = vs.tones("#d8d0e6", 7, spread=0.16, cool=255, warm=40, hue=6, sat=0.03)
VEIN = [hexc("#9a8ac8"), hexc("#b4a6dc")]
GOLD = [hexc("#a8742a"), hexc("#d6a23c"), hexc("#f2cf62"), hexc("#fff0a8")]
MOSS = vs.tones("#2c3d5e", 6, spread=0.34, cool=270, warm=185, hue=22, sat=0.05)
SPORE = [hexc("#8ff4ff"), hexc("#d8fbff")]
DUST = vs.tones("#8ea4dc", 5, spread=0.16, cool=230, warm=210, hue=6, sat=0.03)
CRYSTAL = vs.tones("#7a44d0", 6, spread=0.45, cool=270, warm=200, hue=24, sat=0.04) + [hexc("#e6d8ff")]
CYAN = [hexc("#2a7ab8"), hexc("#46b6e6"), hexc("#9ae6ff"), hexc("#e8fbff")]
TEAL_STEM = [hexc("#1c3a46"), hexc("#2a5664"), hexc("#3e7a86")]


def _img(g: vs.Grid, pal: list[Color]) -> Image.Image:
    return vs.paint(g, pal)


def _stars(img: Image.Image, seed: int, count: int, bright: float = 1.0) -> Image.Image:
    """Eingeschlossene Sterne: einzelne helle Punkte, manche mit schwachem Schein ringsum."""
    rnd = random.Random(seed)
    px = img.load()
    for _ in range(count):
        x, y = rnd.randrange(N), rnd.randrange(N)
        c = rnd.choice(STAR_COLORS)
        base = px[x, y]
        px[x, y] = pa.mix(base, c, 0.85 * bright)
        if rnd.random() < 0.2:
            for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                xx, yy = (x + dx) % N, (y + dy) % N
                px[xx, yy] = pa.mix(px[xx, yy], c, 0.22 * bright)
    return img


# =============================================================================
# Leerenstein
# =============================================================================

VOID_BRICK_ROWS = [(4, [3, 11]), (4, [7, 15]), (4, [1, 10]), (4, [5, 13])]


def void_stone() -> Image.Image:
    return _stars(_img(vs.stone(51101, grain=0.7), STONE), 51102, 3, 0.85)


def void_stone_bricks() -> Image.Image:
    return _stars(_img(vs.bricks(51201, VOID_BRICK_ROWS), BRICK), 51202, 3, 0.8)


def cracked_void_stone_bricks() -> Image.Image:
    g = vs.bricks(51201, VOID_BRICK_ROWS, chips=6)
    g = vs.crack(g, 51211, count=4, length=(4, 8), level=0, only=lambda v: v >= 2)
    img = _img(g, BRICK)
    # aus den Rissen schimmert es violett
    px = img.load()
    for y in range(N):
        for x in range(N):
            if g[y][x] == 0 and random.Random(x * 31 + y).random() < 0.35:
                px[x, y] = pa.mix(px[x, y], hexc("#7a4ad0"), 0.45)
    return img


def polished_void_stone() -> Image.Image:
    return _stars(_img(vs.polished(51301), POLISHED), 51302, 2, 0.6)


EYE = [
    "...######...",
    "..##....##..",
    ".##..##..##.",
    "##..####..##",
    ".##..##..##.",
    "..##....##..",
    "...######...",
    ".....##.....",
    ".....##.....",
    "....#..#....",
    "...#....#...",
]


def chiseled_void_stone() -> Image.Image:
    """Gemeißelter Leerenstein: in einem eingelassenen Feld das Zeichen der Alten – ein Auge über einem Riss."""
    g = vs.polished(51401)
    for y in range(1, 15):
        for x in range(1, 15):
            g[y][x] = 2
    vs.bevel_frame(g, 1, 1, 14, 14, light=4, dark=0)
    raised = {(2 + x, 3 + y) for y, row in enumerate(EYE) for x, c in enumerate(row) if c == "#"}
    for (x, y) in raised:
        g[y][x] = 5 if (x, y - 1) not in raised or (x - 1, y) not in raised else 4
        if (x + 1, y + 1) not in raised and 1 < x + 1 < 14 and 1 < y + 1 < 14:
            g[y + 1][x + 1] = min(g[y + 1][x + 1], 1)
    img = _img(g, POLISHED)
    px = img.load()
    # die Pupille glimmt violett
    for (x, y) in ((7, 6), (8, 6), (7, 7), (8, 7)):
        px[x, y] = hexc("#b48cff")
    return img


# =============================================================================
# Sternenmarmor
# =============================================================================

def _veins(img: Image.Image, seed: int, gold_share: float = 0.18, strength: float = 1.0) -> Image.Image:
    """Marmoradern: geschwungene, feine Linien in Violett, ein paar davon golden."""
    rnd = random.Random(seed)
    px = img.load()
    field = pa.fractal_noise(seed, ((8, 0.55), (4, 0.3), (2, 0.15)))
    a = rnd.uniform(0.3, 0.9)
    for y in range(N):
        for x in range(N):
            v = math.sin((x * math.cos(a) + y * math.sin(a)) * 0.55 + field[y][x] * 7.0)
            if abs(v) < 0.11 * strength:
                gold = random.Random(seed + x * 17 + y * 5).random() < gold_share
                col = GOLD[2] if gold else VEIN[0]
                px[x, y] = pa.mix(px[x, y], col, 0.8 if gold else 0.7)
            elif abs(v) < 0.24 * strength:
                px[x, y] = pa.mix(px[x, y], VEIN[1], 0.3)
    return img


def _periodic_field(seed: int) -> list[list[float]]:
    """Kachelbares, weiches Rauschen (Summe von Wellen mit ganzzahligen Perioden über 16 Pixel)."""
    rnd = random.Random(seed)
    waves = [(rnd.randint(-3, 3), rnd.randint(-3, 3), rnd.uniform(0.4, 1.0), rnd.uniform(0, 2 * math.pi)) for _ in range(7)]
    field = [[0.0] * N for _ in range(N)]
    for y in range(N):
        for x in range(N):
            field[y][x] = sum(a * math.sin(2 * math.pi * (kx * x + ky * y) / N + p) for kx, ky, a, p in waves if kx or ky)
    return field


def _vein_paths(img: Image.Image, seed: int, count: int, gold: int) -> Image.Image:
    """
    Marmoradern: schräge, gewellte Linien, die genau eine Kachel weit wandern (Steigung 1 bzw. -1, Wellen mit ganzzahliger
    Periode) – so schließen sie an allen Rändern nahtlos an. Violett mit hellem Saum, eine golden.
    """
    rnd = random.Random(seed)
    px = img.load()
    for k in range(count):
        is_gold = k >= count - gold
        direction = 1 if k % 2 == 0 else -1
        x0 = rnd.uniform(0, N)
        amp = rnd.uniform(1.0, 2.2)
        phase = rnd.uniform(0, 2 * math.pi)
        waves = rnd.choice((1, 2))
        prev = None
        for y in range(N):
            x = x0 + direction * y + amp * math.sin(2 * math.pi * waves * y / N + phase)
            xi = int(math.floor(x)) % N
            cells = [xi]
            if prev is not None:
                d = ((xi - prev + N // 2) % N) - N // 2
                step = 1 if d > 0 else -1
                for j in range(1, abs(d)):
                    cells.append((prev + step * j) % N)
            for cx in cells:
                if is_gold:
                    px[cx, y] = pa.mix(px[cx, y], GOLD[2], 0.85)
                else:
                    px[cx, y] = pa.mix(px[cx, y], hexc("#8a74c4"), 0.6)
                    for dx in (-1, 1):
                        xx = (cx + dx) % N
                        px[xx, y] = pa.mix(px[xx, y], hexc("#c4b8e4"), 0.28)
            prev = xi
    return img


def astral_marble() -> Image.Image:
    g = vs.stone(52101, shares=(0.06, 0.24, 0.45, 0.25), grain=0.35)
    img = _img([[min(4, v + 1) for v in row] for row in g], MARBLE)
    return _vein_paths(img, 52103, 3, 1)


MARBLE_ROWS = [(8, [4]), (8, [12])]


def astral_marble_bricks() -> Image.Image:
    img = _img(vs.bricks(52201, MARBLE_ROWS, body=(0.2, 0.5, 0.3)), MARBLE_BRICK)
    return _veins(img, 52202, gold_share=0.25, strength=0.7)


def astral_pillar_side() -> Image.Image:
    """Säule: senkrechte Kanneluren, oben und unten ein goldenes Band."""
    img = Image.new("RGBA", (N, N))
    px = img.load()
    rnd = random.Random(52301)
    for y in range(N):
        for x in range(N):
            flute = x % 4
            level = (3, 4, 3, 1)[flute]
            if y in (0, N - 1):
                px[x, y] = GOLD[2 if x % 3 else 1]
                continue
            if y in (1, N - 2):
                px[x, y] = MARBLE[0]
                continue
            col = MARBLE[min(4, level + (1 if rnd.random() < 0.08 else 0))]
            px[x, y] = col
    return _veins(img, 52302, gold_share=0.1, strength=0.5)


def astral_pillar_top() -> Image.Image:
    img = Image.new("RGBA", (N, N))
    px = img.load()
    for y in range(N):
        for x in range(N):
            d = math.hypot(x - 7.5, y - 7.5)
            if d > 7.2:
                px[x, y] = MARBLE[1]
            elif d > 6.2:
                px[x, y] = GOLD[1]
            elif d > 3.5:
                px[x, y] = MARBLE[3 if (int(d * 2) % 2) else 2]
            elif d > 2.5:
                px[x, y] = MARBLE[1]
            else:
                px[x, y] = MARBLE[4]
    return img


STAR8 = [
    ".......#.......",
    ".......#.......",
    "......###......",
    "..#...###...#..",
    "...##.###.##...",
    "....#######....",
    "...#########...",
    "###############",
    "...#########...",
    "....#######....",
    "...##.###.##...",
    "..#...###...#..",
    "......###......",
    ".......#.......",
    ".......#.......",
]


def chiseled_astral_marble() -> tuple[Image.Image, Image.Image]:
    """Gemeißelter Sternenmarmor: ein goldener, achtzackiger Stern in einem eingelassenen Kreis (der Stern leuchtet)."""
    img = astral_marble_bricks()
    px = img.load()
    glow = pa.new()
    gp = glow.load()
    for y in range(N):
        for x in range(N):
            d = math.hypot(x - 7.5, y - 7.5)
            if 6.4 < d < 7.4:
                px[x, y] = pa.mix(px[x, y], MARBLE[0], 0.7)
            elif d <= 6.4:
                px[x, y] = MARBLE[3]
    for y, row in enumerate(STAR8):
        for x, c in enumerate(row):
            if c == "#" and 0 <= x < N and y < N:
                xx, yy = x + 1 if x < N - 1 else x, y + 1 if y < N - 1 else y
                d = math.hypot(xx - 7.5, yy - 7.5)
                col = GOLD[3] if d < 2.0 else (GOLD[2] if d < 4.5 else GOLD[1])
                px[xx, yy] = col
                if d < 4.5:
                    gp[xx, yy] = col
    return img, glow


# =============================================================================
# Boden: Leerenmoos, Sternenstaub
# =============================================================================

def void_moss_top() -> Image.Image:
    g = vs.grass_top(53101)
    img = _img(g, MOSS)
    rnd = random.Random(53102)
    px = img.load()
    # violetter Schimmer in den Senken, vereinzelte türkise Moosspitzen auf den Polstern (zufällig, kein Raster)
    field = pa.fractal_noise(53103, ((8, 0.6), (4, 0.4)))
    for y in range(N):
        for x in range(N):
            if field[y][x] < 0.3:
                px[x, y] = pa.mix(px[x, y], hexc("#4a2e8a"), 0.22)
            elif field[y][x] > 0.64 and rnd.random() < 0.3:
                px[x, y] = pa.mix(px[x, y], hexc("#4aa8a0"), 0.35)
    for _ in range(4):
        x, y = rnd.randrange(N), rnd.randrange(N)
        px[x, y] = pa.mix(px[x, y], rnd.choice(SPORE), 0.75)
    return img


def void_moss_side() -> Image.Image:
    img = void_stone()
    top = void_moss_top()
    px, tp = img.load(), top.load()
    rnd = random.Random(53201)
    fringe = [rnd.choice((2, 3, 3, 4)) for _ in range(N)]
    for x in range(N):
        for y in range(fringe[x]):
            px[x, y] = tp[x, y]
        if rnd.random() < 0.35:
            px[x, fringe[x]] = pa.mix(px[x, fringe[x]], MOSS[1], 0.6)
    return img


def stardust() -> tuple[Image.Image, Image.Image]:
    g = vs.sand(54101)
    img = _img(g, DUST)
    px = img.load()
    glow = pa.new()
    gp = glow.load()
    rnd = random.Random(54102)
    for _ in range(9):
        x, y = rnd.randrange(N), rnd.randrange(N)
        c = rnd.choice([hexc("#ffffff"), hexc("#d8f4ff"), hexc("#ffd8f4"), hexc("#fff4c8")])
        px[x, y] = c
        gp[x, y] = c
        if rnd.random() < 0.3:
            for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                xx, yy = (x + dx) % N, (y + dy) % N
                px[xx, yy] = pa.mix(px[xx, yy], c, 0.4)
    for _ in range(14):
        x, y = rnd.randrange(N), rnd.randrange(N)
        px[x, y] = pa.mix(px[x, y], hexc("#4a5a9a"), 0.5)
    return img, glow


# =============================================================================
# Kristalle und Pflanzen
# =============================================================================

def void_crystal() -> Image.Image:
    g = vs.crystal_block(55107, facets=5)
    img = _img(g, [CRYSTAL[0], CRYSTAL[1], CRYSTAL[2], CRYSTAL[3], CRYSTAL[5], CRYSTAL[6]])
    px = img.load()
    # Farbspiel: oben links türkis, unten rechts violett; leicht durchscheinend
    for y in range(N):
        for x in range(N):
            c = px[x, y]
            t = (x + y) / 30.0
            c = pa.mix(c, CYAN[2], 0.35 * (1 - t) if g[y][x] >= 4 else 0.12 * (1 - t))
            px[x, y] = (c[0], c[1], c[2], 225 if g[y][x] < 4 else 245)
    return img


CLUSTER = [
    "................",
    "........a.......",
    ".......aba..a...",
    "...a...abc.aba..",
    "..aba..abc.abc..",
    "..abc..abcdabc..",
    "..abcd.abcdabcd.",
    "...bcd.abcd.bcd.",
    ".a.bcdaabcd.bcd.",
    "aba.cdabbcd.bcd.",
    "abc.cdabbcdabcd.",
    "abcdcdabccdabcd.",
    ".bcdcdabccdbccd.",
    ".bcddabbccdbccd.",
    "..cddabbccddcd..",
    ".aabbbbcccddddd.",
]


def void_crystal_cluster() -> tuple[Image.Image, Image.Image]:
    colors = {"a": CYAN[3], "b": CRYSTAL[5], "c": CRYSTAL[3], "d": CRYSTAL[1]}
    img = pa.sprite(CLUSTER, colors)
    glow = pa.new()
    gp, px = glow.load(), img.load()
    for y in range(N):
        for x in range(N):
            c = px[x, y]
            if c[3] and c in (CYAN[3], CRYSTAL[5]):
                gp[x, y] = c
    for (x, y) in ((8, 1), (12, 2), (3, 3), (1, 8)):
        px[x, y] = hexc("#ffffff")
        gp[x, y] = hexc("#ffffff")
    return img, glow


def crystal_foliage() -> tuple[Image.Image, Image.Image]:
    """Kristalllaub: Büschel kleiner, schräger Splitter mit Lücken dazwischen (türkis bis violett), einige leuchten."""
    img = pa.new()
    glow = pa.new()
    px, gp = img.load(), glow.load()
    rnd = random.Random(56101)
    for _ in range(26):
        x, y = rnd.randrange(N), rnd.randrange(N)
        length = rnd.randint(2, 4)
        dx, dy = rnd.choice(((1, -1), (1, 1), (0, -1), (1, 0)))
        base = rnd.choice((CYAN[1], CYAN[2], CRYSTAL[4], CRYSTAL[5]))
        for i in range(length):
            xx, yy = (x + dx * i) % N, (y + dy * i) % N
            c = pa.mix(base, hexc("#ffffff"), 0.12 * i)
            px[xx, yy] = c
            if i == length - 1 and rnd.random() < 0.5:
                gp[xx, yy] = pa.mix(c, hexc("#ffffff"), 0.4)
    # dunklere Füllung, damit es nicht zu löchrig wird
    for y in range(N):
        for x in range(N):
            if px[x, y][3] == 0 and rnd.random() < 0.38:
                px[x, y] = pa.mix(CRYSTAL[1], CYAN[0], rnd.random() * 0.5)
    return img, glow


BLOOM = [
    "................",
    "................",
    "....a.....a.....",
    "....ab...ba.....",
    "....abb.bba.....",
    ".....abbba......",
    "...a.abcba.a....",
    "...abbbcbbba....",
    "....abbcbba.....",
    ".....aabaa......",
    ".......s........",
    ".......s..l.....",
    "...l...s.l......",
    "....l..sl.......",
    ".....l.s........",
    "......ss........",
]


def void_bloom() -> tuple[Image.Image, Image.Image]:
    colors = {"a": CRYSTAL[3], "b": CYAN[2], "c": hexc("#ffffff"), "s": TEAL_STEM[1], "l": TEAL_STEM[2]}
    img = pa.sprite(BLOOM, colors)
    glow = pa.new()
    gp, px = glow.load(), img.load()
    for y in range(N):
        for x in range(N):
            c = px[x, y]
            if c[3] and c in (CYAN[2], hexc("#ffffff")):
                gp[x, y] = c
    return img, glow


def star_vines(plant: bool) -> tuple[Image.Image, Image.Image]:
    """Sternenranke: dünne, hängende Strähnen; die Spitze trägt einen kleinen, leuchtenden Stern."""
    img = pa.new()
    glow = pa.new()
    px, gp = img.load(), glow.load()
    rnd = random.Random(57101 if plant else 57102)
    for strand_x in (4, 8, 11):
        x = strand_x
        end = N if plant else rnd.randint(9, 12)
        for y in range(end):
            if rnd.random() < 0.25:
                x += rnd.choice((-1, 1))
                x = max(2, min(13, x))
            c = TEAL_STEM[rnd.randrange(3)]
            px[x, y] = c
            if rnd.random() < 0.12:
                px[x, y] = CYAN[2]
                gp[x, y] = CYAN[2]
        if not plant:
            for dx, dy in ((0, 0), (1, 0), (-1, 0), (0, 1), (0, -1)):
                xx, yy = x + dx, end + dy
                if 0 <= xx < N and 0 <= yy < N:
                    c = hexc("#ffffff") if (dx, dy) == (0, 0) else CYAN[3]
                    px[xx, yy] = c
                    gp[xx, yy] = c
    return img, glow


# =============================================================================
# Der Leerenriss (bewegt)
# =============================================================================

def void_rift(frames: int = 24) -> Image.Image:
    """
    Der Riss als Bildfolge: Ein violetter Wirbel dreht sich langsam um eine dunkle Mitte, in ihm treiben Sterne nach
    innen; die Ränder leuchten heller. Leicht durchscheinend.
    """
    sheet = Image.new("RGBA", (N, N * frames))
    rnd = random.Random(58101)
    stars = [(rnd.uniform(0, 2 * math.pi), rnd.uniform(1.5, 11.0), rnd.choice(STAR_COLORS + [hexc("#ffffff")])) for _ in range(26)]
    for f in range(frames):
        t = f / frames
        frame = Image.new("RGBA", (N, N))
        px = frame.load()
        for y in range(N):
            for x in range(N):
                dx, dy = x - 7.5, y - 7.5
                r = math.hypot(dx, dy)
                a = math.atan2(dy, dx)
                swirl = math.sin(a * 3 + r * 0.7 - t * 2 * math.pi) * 0.5 + 0.5
                swirl2 = math.sin(a * 2 - r * 0.45 + t * 2 * math.pi * 2) * 0.5 + 0.5
                v = 0.25 + 0.45 * swirl + 0.2 * swirl2
                v *= 0.55 + 0.45 * min(1.0, r / 8.0)
                c = pa.mix(hexc("#0a0618"), hexc("#5a2ab0"), v)
                c = pa.mix(c, hexc("#2a8ad0"), max(0.0, swirl2 - 0.6) * 0.8)
                px[x, y] = (c[0], c[1], c[2], 215)
        for (a0, r0, col) in stars:
            # Sterne kreisen und treiben nach innen, außen tauchen sie wieder auf
            r = (r0 - t * 9.0) % 11.0 + 0.8
            a = a0 + t * 2 * math.pi * 0.5 + 2.0 / (r + 0.5)
            x = int(round(7.5 + math.cos(a) * r))
            y = int(round(7.5 + math.sin(a) * r))
            if 0 <= x < N and 0 <= y < N:
                b = min(1.0, r / 4.0)
                px[x, y] = pa.mix(px[x, y], col, 0.9 * b)[:3] + (240,)
        sheet.paste(frame, (0, f * N))
    return sheet


# =============================================================================
# Gegenstand
# =============================================================================

SHARD = [
    "................",
    "..........a.....",
    ".........ab.....",
    "........abc.....",
    ".......abbc.....",
    "......abbcd.....",
    ".....abbccd.....",
    ".....abccdd.....",
    "....abbccd......",
    "....abccdd......",
    "...abbcdd.......",
    "...abccd........",
    "..abcdd.........",
    "..bcdd..........",
    "..cd............",
    "................",
]


def void_shard() -> Image.Image:
    colors = {"a": CYAN[3], "b": CYAN[2], "c": CRYSTAL[4], "d": CRYSTAL[2]}
    img = pa.sprite(SHARD, colors)
    img = pa.outline(img, lambda c: pa.mix(c, hexc("#120a24"), 0.7))
    px = img.load()
    for (x, y) in ((10, 1), (6, 6)):
        px[x, y] = hexc("#ffffff")
    return img


def throne_seal() -> tuple[Image.Image, Image.Image]:
    """Thronsiegel: polierter Leerenstein, darin ein goldener Ring mit einer Krone aus sieben Splittern (leuchtend)."""
    img = polished_void_stone()
    px = img.load()
    glow = pa.new()
    gp = glow.load()
    for y in range(N):
        for x in range(N):
            d = math.hypot(x - 7.5, y - 7.5)
            if 5.6 < d < 6.6:
                col = GOLD[2] if (x + y) % 3 else GOLD[1]
                px[x, y] = col
                gp[x, y] = pa.mix((0, 0, 0, 255), col, 0.8)
    # die Krone: sieben Splitter über einem Reif
    for x in range(5, 11):
        px[x, 9] = GOLD[2]
        px[x, 10] = GOLD[1]
        gp[x, 9] = GOLD[2]
    for (x, top) in ((5, 7), (6, 6), (7, 4), (8, 4), (9, 6), (10, 7)):
        for y in range(top, 9):
            col = CRYSTAL[5] if y == top else CRYSTAL[3]
            px[x, y] = col
            gp[x, y] = col
    return img, glow


CROWN = [
    "................",
    ".......5........",
    ".......53.......",
    "...5...43...5...",
    ".5.43..43..43.5.",
    ".4.43..42..42.4.",
    ".3.32..42..32.3.",
    ".2.21..21..21.2.",
    "................",
    "................",
    "..y..........y..",
    "..YWYYYYYYYYWY..",
    "..yGyGyVvyGyGy..",
    "..GgGgGgGgGgGg..",
    "................",
    "................",
]


def shattered_crown() -> Image.Image:
    """Krone des Zerbrochenen Königs: ein goldener Reif mit Leerenkristall, darüber schweben fünf Kristallsplitter."""
    colors = {"1": hexc("#3a1f78"), "2": hexc("#5a32a8"), "3": hexc("#8a5ae0"), "4": hexc("#b896f4"), "5": hexc("#efe6ff"),
              "g": hexc("#7a5420"), "G": hexc("#b07a2c"), "y": hexc("#dca640"), "Y": hexc("#f4d066"), "W": hexc("#fff4b8"),
              "V": hexc("#c49cff"), "v": hexc("#7040c8")}
    img = pa.sprite(CROWN, colors)
    return pa.outline(img, hexc("#1a0f2e"))


def build(put: Callable[[str, Image.Image], None]) -> None:
    put("block/void_stone", void_stone())
    put("block/void_stone_bricks", void_stone_bricks())
    put("block/cracked_void_stone_bricks", cracked_void_stone_bricks())
    put("block/polished_void_stone", polished_void_stone())
    put("block/chiseled_void_stone", chiseled_void_stone())
    put("block/astral_marble", astral_marble())
    put("block/astral_marble_bricks", astral_marble_bricks())
    put("block/astral_pillar_side", astral_pillar_side())
    put("block/astral_pillar_top", astral_pillar_top())
    chiseled, chiseled_glow = chiseled_astral_marble()
    put("block/chiseled_astral_marble", chiseled)
    put("block/chiseled_astral_marble_glow", chiseled_glow)
    put("block/void_moss_top", void_moss_top())
    put("block/void_moss_side", void_moss_side())
    put("block/void_moss_bottom", void_stone())
    dust, dust_glow = stardust()
    put("block/stardust", dust)
    put("block/stardust_glow", dust_glow)
    put("block/void_crystal", void_crystal())
    cluster, cluster_glow = void_crystal_cluster()
    put("block/void_crystal_cluster", cluster)
    put("block/void_crystal_cluster_emissive", cluster_glow)
    foliage, foliage_glow = crystal_foliage()
    put("block/crystal_foliage", foliage)
    put("block/crystal_foliage_glow", foliage_glow)
    bloom, bloom_glow = void_bloom()
    put("block/void_bloom", bloom)
    put("block/void_bloom_emissive", bloom_glow)
    vines, vines_glow = star_vines(False)
    put("block/star_vines", vines)
    put("block/star_vines_emissive", vines_glow)
    plant, plant_glow = star_vines(True)
    put("block/star_vines_plant", plant)
    put("block/star_vines_plant_emissive", plant_glow)
    put("block/void_rift", void_rift())
    put("item/void_shard", void_shard())
    put("item/shattered_crown", shattered_crown())
    seal, seal_glow = throne_seal()
    put("block/throne_seal", seal)
    put("block/throne_seal_glow", seal_glow)
