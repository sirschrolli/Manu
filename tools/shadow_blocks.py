#!/usr/bin/env python3
"""
Die vollen Blöcke der Schattenwelt im Vanilla-Stil (siehe tools/vanilla_style.py): Schattenstein in allen Formen,
Tiefschattenstein, Erde, Gras, Sand, Aschschnee, Schattenholz, Laub, Nachtkristall, Glimmmoos, Ackerboden, Pfad, Tür
und Falltür, die Metall- und Kristallblöcke (die Erz-Einschlüsse kommen aus tools/ores_art.py).

Farbwelt: kühles, gedämpftes Violettgrau für Stein, ein tiefes Pflaumenviolett für das Holz, blasses Lavendel für Sand
und Asche – überall wenig Sättigung (die Schattenwelt färbt selbst schon violett), dafür klare Formen.

Wird von tools/generate_textures.py (über shadow_textures.build) aufgerufen: build(put).
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

# =============================================================================
# Paletten (dunkel -> hell)
# =============================================================================

STONE = vs.tones("#6f6880", 4, spread=0.16, hue=10, sat=0.04)
BRICK = vs.tones("#6a6379", 7, spread=0.30, hue=12, sat=0.05)
POLISHED = vs.tones("#6c657c", 6, spread=0.26, hue=10, sat=0.04)
DEEP = vs.tones("#3d3949", 4, spread=0.24, hue=10, sat=0.04)
DEEP_TOP = vs.tones("#45404f", 6, spread=0.36, hue=10, sat=0.04)
SOIL = vs.tones("#4e4157", 5, spread=0.24, hue=10, sat=0.05)
PEBBLE = vs.tones("#6e6a78", 2, spread=0.10)
SAND = vs.tones("#a59eb2", 5, spread=0.10, hue=6, sat=0.03)
ASH_SNOW = vs.tones("#d3cfdb", 3, spread=0.04, hue=4, sat=0.02)
ASH = vs.tones("#8a8594", 2, spread=0.12)
BARK = vs.tones("#3f3249", 6, spread=0.40, hue=10, sat=0.05)
WOOD = vs.tones("#5f4874", 6, spread=0.32, hue=10, sat=0.06)
STRIPPED = vs.tones("#674f80", 5, spread=0.22, hue=8, sat=0.05)
PLANKS = vs.tones("#584468", 7, spread=0.34, hue=10, sat=0.05)
CRYSTAL = vs.tones("#6a60c8", 6, spread=0.30, cool=250, warm=290, hue=14, sat=0.02) + [hexc("#cfc8ff")]
MOSS = vs.tones("#4c5a70", 6, spread=0.26, cool=230, warm=200, hue=8, sat=0.03)
MOSS_GLOW = [hexc("#9fd8ff"), hexc("#e8f8ff")]
PATH = vs.tones("#6a5e72", 4, spread=0.13, hue=6, sat=0.03)
FARM_DRY = vs.tones("#54465d", 5, spread=0.30)
FARM_WET = vs.tones("#3d3149", 5, spread=0.32, hue=12, sat=0.08)
GREY = [(v, v, v, 255) for v in (0x6c, 0x76, 0x80, 0x8a, 0x96, 0xa2)]
SILVER = vs.tones("#bfc7da", 6, spread=0.24, cool=230, warm=210, hue=6, sat=0.03)
NIGHTSTEEL = vs.tones("#4d5269", 6, spread=0.34, cool=235, warm=220, hue=6, sat=0.04)
SOUL = vs.tones("#3fa9ba", 6, spread=0.34, cool=205, warm=170, hue=10, sat=0.03) + [hexc("#d6fbff")]
ABYSS = vs.tones("#2c2236", 6, spread=0.40, cool=270, warm=300, hue=8, sat=0.06)
ABYSS_GLOW = [hexc("#c050ff"), hexc("#ff9cff")]


def _img(g: vs.Grid, pal: list[Color]) -> Image.Image:
    return vs.paint(g, pal)


# =============================================================================
# Schattenstein
# =============================================================================

SHADOW_BRICK_ROWS = [(4, [2, 10]), (4, [6, 14]), (4, [0, 9]), (4, [4, 13])]


def shadow_stone() -> Image.Image:
    return _img(vs.stone(31101), STONE)


def shadow_stone_bricks() -> Image.Image:
    return _img(vs.bricks(31201, SHADOW_BRICK_ROWS), BRICK)


def cracked_shadow_stone_bricks() -> Image.Image:
    g = vs.bricks(31201, SHADOW_BRICK_ROWS, chips=6)
    g = vs.crack(g, 31211, count=4, length=(4, 8), level=0, only=lambda v: v >= 2)
    return _img(g, BRICK)


def polished_shadow_stone() -> Image.Image:
    return _img(vs.polished(31301), POLISHED)


CHISELED_EMBLEM = [
    # Mondsichel mit einem Kristall in ihrer Öffnung (Feld 3..12)
    "...####...",
    "..######..",
    ".###..###.",
    "###.......",
    "###....#..",
    "###...###.",
    "###....#..",
    ".###..###.",
    "..######..",
    "...####...",
]


def chiseled_shadow_stone() -> Image.Image:
    """
    Gemeißelter Schattenstein: eine polierte Platte mit eingelassenem Feld, darin erhaben das Zeichen der Umbrer – eine
    Mondsichel, in deren Öffnung ein Kristall funkelt. Erhabenes hat oben links eine Lichtkante, unten rechts Schatten.
    """
    g = vs.polished(31401)
    for y in range(2, 14):
        for x in range(2, 14):
            g[y][x] = 2
    vs.bevel_frame(g, 2, 2, 13, 13, light=4, dark=0)
    raised = {(3 + x, 3 + y) for y, row in enumerate(CHISELED_EMBLEM) for x, c in enumerate(row) if c == "#"}
    for (x, y) in raised:
        for sx, sy in ((x + 1, y + 1), (x, y + 1), (x + 1, y)):
            if (sx, sy) not in raised and 3 <= sx <= 12 and 3 <= sy <= 12:
                g[sy][sx] = 1
    for (x, y) in raised:
        up = (x, y - 1) not in raised
        left = (x - 1, y) not in raised
        down = (x, y + 1) not in raised
        right = (x + 1, y) not in raised
        if up or left:
            g[y][x] = 5
        elif down or right:
            g[y][x] = 3
        else:
            g[y][x] = 4
    return _img(g, POLISHED)


def deep_shadow_stone() -> Image.Image:
    return _img(vs.layered_stone(31501), DEEP)


def deep_shadow_stone_top() -> Image.Image:
    return _img(vs.cobble(31511, stones=14), DEEP_TOP)


# =============================================================================
# Böden
# =============================================================================

def shadow_soil() -> Image.Image:
    return _img(vs.soil(31601), SOIL + PEBBLE)


def shadow_grass_top() -> Image.Image:
    """Grau (die Farbe kommt aus dem Biom): fein gemusterte Halmspitzen wie beim Vanilla-Gras."""
    return _img(vs.grass_top(31701), GREY[:5])


def _grass_fringe(seed: int) -> list[int]:
    """Wie tief das Gras an der Seite herabhängt (je Spalte 1-4 Pixel)."""
    rnd = random.Random(seed)
    depth = []
    for x in range(N):
        d = 2 + (1 if rnd.random() < 0.45 else 0) + (1 if rnd.random() < 0.2 else 0)
        depth.append(d)
    # keine allein stehenden tiefen Zacken
    for x in range(N):
        if depth[x] >= 4 and depth[(x - 1) % N] <= 2 and depth[(x + 1) % N] <= 2:
            depth[x] = 3
    return depth


def shadow_grass_side() -> Image.Image:
    """Erde mit einem Saum aus Gras oben (der Saum selbst ist grau, darüber liegt die gefärbte Auflage)."""
    img = shadow_soil()
    fringe = _grass_fringe(31711)
    top = vs.grass_top(31712)
    for x in range(N):
        for y in range(fringe[x]):
            img.putpixel((x, y), GREY[min(4, top[y][x])])
    return img


def shadow_grass_side_overlay() -> Image.Image:
    img = Image.new("RGBA", (N, N), (0, 0, 0, 0))
    fringe = _grass_fringe(31711)
    top = vs.grass_top(31712)
    for x in range(N):
        for y in range(fringe[x]):
            img.putpixel((x, y), GREY[min(5, top[y][x] + 1)])
    return img


def shadow_sand() -> Image.Image:
    """Fahler Lavendelsand: kontrastarm, mit ein paar dunkleren Körnchen und flachen Windriffeln."""
    g = vs.sand(31801)
    rnd = random.Random(31802)
    for _ in range(3):
        y = rnd.randrange(N)
        x = rnd.randrange(N)
        for i in range(rnd.randint(3, 6)):
            g[y][(x + i) % N] = max(0, g[y][(x + i) % N] - 1)
    return _img(g, SAND[:4])


def ash_snow_block() -> Image.Image:
    """Grauer Schnee (fast wie Vanilla-Schnee, nur kühler), darin ein paar Ascheflocken aus 1-2 Pixeln."""
    f = vs.layered(31901, ((4, 4, 0.5), (2, 2, 0.5)))
    g = vs.quantize(f, (0.25, 0.5, 0.25))
    img = _img(g, ASH_SNOW)
    rnd = random.Random(31902)
    for _ in range(5):
        x, y = rnd.randrange(N), rnd.randrange(N)
        img.putpixel((x, y), ASH[0])
        if rnd.random() < 0.5:
            img.putpixel(((x + 1) % N, y), ASH[1])
    return img


# =============================================================================
# Schattenholz
# =============================================================================

def shadow_log() -> Image.Image:
    return _img(vs.bark(32001), BARK)


def shadow_log_top() -> Image.Image:
    g = vs.log_top(32011)
    return _img(g, [BARK[0], BARK[2], WOOD[1], WOOD[2], WOOD[3], WOOD[4]])


def stripped_shadow_log() -> Image.Image:
    return _img(vs.stripped(32021), STRIPPED)


def stripped_shadow_log_top() -> Image.Image:
    g = vs.log_top(32031)
    return _img(g, [STRIPPED[0], STRIPPED[1], WOOD[1], WOOD[2], WOOD[3], WOOD[4]])


def shadow_planks() -> Image.Image:
    return _img(vs.planks(32101, joints=(None, 9, 3, 12)), PLANKS)


def shadow_leaves() -> Image.Image:
    """Laub (grau, die Farbe kommt aus dem Biom): kleine Blattbüschel mit heller Spitze, dazwischen Lücken."""
    pal = [(0x6c, 0x69, 0x6c, 255), (0x7d, 0x7b, 0x7d, 255), (0x98, 0x99, 0x98, 255), (0xb6, 0xb8, 0xb6, 255)]
    return vs.paint(vs.leaves(32201), pal)


# =============================================================================
# Kristall und Moos
# =============================================================================

def _crystal_grid(seed: int) -> vs.Grid:
    """Kristallblock wie Vanilla-Amethyst: körnige Flecken in sechs Stufen, dazu wenige helle Funkelpunkte (Stufe 6)."""
    rnd = random.Random(seed)
    f = vs.mix_fields((vs.strokes(seed, (1, 2, 1), (1, 2, 1)), 0.55), (vs.layered(seed + 1, ((4, 4, 0.6), (8, 8, 0.4))), 0.45))
    g = vs.despeckle(vs.quantize(f, (0.08, 0.2, 0.28, 0.24, 0.14, 0.06)), passes=1)
    sparkles = 0
    while sparkles < 4:
        x, y = rnd.randrange(N), rnd.randrange(N)
        if g[y][x] == 5:
            g[y][x] = 6
            sparkles += 1
    return g


def night_crystal() -> Image.Image:
    return _img(_crystal_grid(32301), CRYSTAL)


def glimmer_moss() -> tuple[Image.Image, Image.Image]:
    """Glimmmoos wie Vanilla-Moos (weiche, körnige Polster), dazu einzelne glimmende Spitzen auf einer eigenen Textur."""
    f = vs.mix_fields((vs.strokes(32401, (1, 2, 1), (1, 2, 1)), 0.6), (vs.layered(32402, ((4, 4, 0.6), (8, 8, 0.4))), 0.4))
    g = vs.despeckle(vs.quantize(f, (0.1, 0.2, 0.28, 0.24, 0.12, 0.06)), passes=1)
    img = _img(g, MOSS)
    glow = Image.new("RGBA", (N, N), (0, 0, 0, 0))
    rnd = random.Random(32403)
    spots = 0
    while spots < 9:
        x, y = rnd.randrange(N), rnd.randrange(N)
        if g[y][x] < 4 or glow.getpixel((x, y))[3]:
            continue
        c = MOSS_GLOW[1] if spots % 3 == 0 else MOSS_GLOW[0]
        glow.putpixel((x, y), c)
        img.putpixel((x, y), c)
        spots += 1
    return img, glow


# =============================================================================
# Acker und Pfad
# =============================================================================

def _farmland(pal: list[Color], seed: int) -> Image.Image:
    """
    Ackerboden wie Vanilla: Erde mit zwei senkrechten Furchen (dunkle Rinne mit heller Kante links daneben), dazwischen
    erhabene, hellere Beete; ein paar Steinchen.
    """
    rnd = random.Random(seed)
    g = vs.soil(seed, pebbles=0)
    for y in range(N):
        for x in range(N):
            col = x % 8
            v = g[y][x]
            if col == 4:
                v = 0 if rnd.random() < 0.7 else 1                        # Rinne
            elif col == 3:
                v = 1 if rnd.random() < 0.7 else 2                        # Schattenseite der Rinne
            elif col == 5:
                v = max(v, 3)                                             # Lichtkante hinter der Rinne
            else:
                v = max(1, min(3, v))                                     # Beet: ruhiger
            g[y][x] = v
    for _ in range(4):
        x, y = rnd.randrange(N), rnd.randrange(N)
        if x % 8 not in (3, 4, 5):
            g[y][x] = 6
            if rnd.random() < 0.5 and (x + 1) % 8 not in (3, 4, 5):
                g[y][(x + 1) % N] = 5
    return _img(g, pal + PEBBLE)


def shadow_farmland() -> Image.Image:
    return _farmland(FARM_DRY, 32501)


def shadow_farmland_moist() -> Image.Image:
    return _farmland(FARM_WET, 32501)


def shadow_path_top() -> Image.Image:
    """Festgetretener Pfad wie Vanilla: feines Korn aus vier eng beieinander liegenden Tönen."""
    f = vs.mix_fields((vs.white(32602), 0.55), (vs.layered(32603, ((4, 4, 0.6), (2, 2, 0.4))), 0.45))
    g = vs.quantize(f, (0.1, 0.45, 0.33, 0.12))
    return _img(g, PATH)


def shadow_path_side() -> Image.Image:
    """Seite des Pfads: oben eine Zeile Pfad (der Block ist 15/16 hoch, die oberste Zeile bleibt leer), darunter Erde."""
    soil = shadow_soil()
    top = shadow_path_top()
    img = Image.new("RGBA", (N, N), (0, 0, 0, 0))
    for y in range(1, N):
        for x in range(N):
            img.putpixel((x, y), top.getpixel((x, y)) if y <= 2 else soil.getpixel((x, y)))
    return img


# =============================================================================
# Tür und Falltür
# =============================================================================

MOONSILVER_FITTING = [hexc("#59607a"), hexc("#9aa3bd"), hexc("#d9dfee")]


def _door_wood(seed: int) -> vs.Grid:
    """
    Grundgerüst einer Tür (Stufen der Bohlen-Palette): links und rechts ein Rahmenholz, dazwischen drei senkrechte
    Bretter zu je vier Pixeln (links eine Lichtkante, rechts die dunkle Fuge, dazwischen senkrechte Maserung).
    """
    grain = vs.quantize(vs.strokes(seed, (1,), (1, 2, 3, 2, 1)), (0.25, 0.5, 0.25))
    g = vs.grid(3)
    for y in range(N):
        for x in range(N):
            if x == 0:
                v = 1
            elif x == 1:
                v = 5
            elif x == 14:
                v = 2
            elif x == 15:
                v = 1
            else:
                col = (x - 2) % 4
                v = 5 if col == 0 else 1 if col == 3 else 2 + grain[y][x]
            g[y][x] = v
    return g


def _rail(g: vs.Grid, y: int, light: bool) -> None:
    """Ein Querholz über die ganze Breite: Zeile y hell (oben) oder dunkel (unten)."""
    for x in range(N):
        g[y][x] = (6 if 0 < x < 15 else 4) if light else (1 if 0 < x < 15 else 0)


def shadow_door() -> tuple[Image.Image, Image.Image]:
    """
    Tür aus Schattenholz: Rahmen und drei senkrechte Bretter; oben ein rundes Mondfenster (durchsichtig) mit einem
    Ring und einem Sprossenkreuz aus Mondsilber, unten zwei Bänder aus Mondsilber und ein Griff.
    """
    # oben
    g = _door_wood(32701)
    for x in range(N):
        g[0][x] = 1 if 0 < x < 15 else 0
        g[1][x] = 5 if 0 < x < 15 else 4
        g[2][x] = 3 if 0 < x < 15 else g[2][x]
    top = _img(g, PLANKS)
    cx, cy = 7.5, 8.5
    for y in range(N):
        for x in range(N):
            d = math.hypot(x - cx, y - cy)
            if d <= 3.9:
                top.putpixel((x, y), (0, 0, 0, 0))
            elif d <= 4.9:
                # Ring aus Mondsilber, oben links im Licht, unten rechts im Schatten
                light = (x - cx) + (y - cy) < -1.0
                dark = (x - cx) + (y - cy) > 1.5
                top.putpixel((x, y), MOONSILVER_FITTING[2] if light else MOONSILVER_FITTING[0] if dark else MOONSILVER_FITTING[1])
    for i in range(N):
        if math.hypot(7 - cx, i - cy) <= 3.9:
            top.putpixel((7, i), MOONSILVER_FITTING[1])
        if math.hypot(i - cx, 8 - cy) <= 3.9:
            top.putpixel((i, 8), MOONSILVER_FITTING[1])
    top.putpixel((7, 8), MOONSILVER_FITTING[2])

    # unten
    g = _door_wood(32702)
    _rail(g, 6, True)
    _rail(g, 7, False)
    for x in range(N):
        g[14][x] = 5 if 0 < x < 15 else 4
        g[15][x] = 1 if 0 < x < 15 else 0
    bottom = _img(g, PLANKS)
    for y in (3, 11):
        for x in range(0, 7):
            bottom.putpixel((x, y), MOONSILVER_FITTING[1] if x < 6 else MOONSILVER_FITTING[0])
            bottom.putpixel((x, y + 1), MOONSILVER_FITTING[0])
        bottom.putpixel((1, y), MOONSILVER_FITTING[2])
        bottom.putpixel((5, y), MOONSILVER_FITTING[2])
    # Griff rechts über dem Querholz
    bottom.putpixel((12, 4), MOONSILVER_FITTING[2])
    bottom.putpixel((12, 5), MOONSILVER_FITTING[1])
    bottom.putpixel((13, 5), MOONSILVER_FITTING[0])
    return top, bottom


DOOR_ITEM = [
    "...oooooooooo...",
    "...oBBBBBBBBo...",
    "...obbkSSkbbo...",
    "...obkSwwSkbo...",
    "...obSWwwwSbo...",
    "...obSwwwwSbo...",
    "...obkSwwSkbo...",
    "...obbkSSkbbo...",
    "...oBBBBBBBBo...",
    "...oddddddddo...",
    "...obbbdbbbbo...",
    "...oSssdbbbHo...",
    "...obbbdbbbko...",
    "...oSssdbbbbo...",
    "...obbbdbbbbo...",
    "...oooooooooo...",
]


def shadow_door_item() -> Image.Image:
    """Die Tür als Gegenstand: Rahmen, rundes Fenster mit Mondsilberring, Bänder und Griff."""
    return pa.sprite(DOOR_ITEM, {
        "o": PLANKS[0], "B": PLANKS[5], "b": PLANKS[3], "d": PLANKS[1],
        "S": MOONSILVER_FITTING[2], "s": MOONSILVER_FITTING[1], "k": MOONSILVER_FITTING[0], "H": MOONSILVER_FITTING[2],
        "w": hexc("#2b3150"), "W": hexc("#5d6a9c"),
    })


def shadow_trapdoor() -> Image.Image:
    """Falltür: Rahmen, waagerechte Bretter, in der Mitte eine durchbrochene Mondsichel, Nieten aus Mondsilber."""
    g = vs.planks(32801, boards=4, joints=(None,))
    for i in range(N):
        g[0][i] = 5 if i < 15 else 3
        g[i][0] = 5 if i < 15 else 3
        g[15][i] = 1
        g[i][15] = 1
        if 0 < i < 15:
            g[1][i] = max(g[1][i], 4) if i < 14 else g[1][i]
            g[14][i] = 2
    img = _img(g, PLANKS)
    mask = set()
    for y in range(4, 12):
        for x in range(4, 12):
            px, py = x + 0.5, y + 0.5
            outer = math.hypot(px - 8.0, py - 8.0) <= 3.9
            inner = math.hypot(px - 9.7, py - 6.8) <= 3.0
            if outer and not inner:
                mask.add((x, y))
    for (x, y) in mask:
        img.putpixel((x, y), (0, 0, 0, 0))
    for y in range(1, N - 1):
        for x in range(1, N - 1):
            if (x, y) in mask:
                continue
            if (x, y + 1) in mask or (x + 1, y) in mask:
                img.putpixel((x, y), PLANKS[1])
            elif (x, y - 1) in mask or (x - 1, y) in mask:
                img.putpixel((x, y), PLANKS[6])
    for (x, y) in ((2, 2), (13, 2), (2, 13), (13, 13)):
        img.putpixel((x, y), MOONSILVER_FITTING[2])
    return img


# =============================================================================
# Metall und Kristall
# =============================================================================

def moonsilver_block() -> Image.Image:
    return _img(vs.metal_block(33001), SILVER)


def nightsteel_block() -> Image.Image:
    """Nachtstahl: dunkles, bläuliches Metall in vier Platten mit Nieten."""
    g = vs.metal_block(33101, panels=4)
    img = _img(g, NIGHTSTEEL)
    for (x, y) in ((2, 2), (13, 2), (2, 13), (13, 13), (5, 5), (10, 10)):
        pass
    for (x, y) in ((2, 2), (12, 2), (2, 12), (12, 12)):
        img.putpixel((x, y), NIGHTSTEEL[5])
        img.putpixel((x + 1, y + 1), NIGHTSTEEL[1])
    return img


def soul_crystal_block() -> tuple[Image.Image, Image.Image]:
    g = _crystal_grid(33201)
    img = _img(g, SOUL)
    glow = Image.new("RGBA", (N, N), (0, 0, 0, 0))
    for y in range(N):
        for x in range(N):
            if g[y][x] >= 5:
                glow.putpixel((x, y), SOUL[g[y][x]])
    return img, glow


def abyssite_block() -> tuple[Image.Image, Image.Image]:
    """Abyssit: fast schwarzer, polierter Stein, von zwei, drei leuchtenden Adern durchzogen."""
    g = vs.stone(33301, shares=(0.2, 0.35, 0.3, 0.15))
    img = _img(g, ABYSS[1:5])
    glow = Image.new("RGBA", (N, N), (0, 0, 0, 0))
    rnd = random.Random(33302)
    for start in ((1, 3), (9, 0), (4, 10)):
        x, y = start
        for i in range(rnd.randint(7, 11)):
            c = ABYSS_GLOW[1] if i % 4 == 0 else ABYSS_GLOW[0]
            img.putpixel((x % N, y % N), c)
            glow.putpixel((x % N, y % N), c)
            img.putpixel(((x + 1) % N, (y + 1) % N), ABYSS[0])
            if rnd.random() < 0.55:
                x += 1
            else:
                y += 1
    return img, glow


# =============================================================================
# Alles zusammen
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
    moss, moss_glow = glimmer_moss()
    put("block/glimmer_moss", moss)
    put("block/glimmer_moss_glow", moss_glow)
    put("block/shadow_farmland", shadow_farmland())
    put("block/shadow_farmland_moist", shadow_farmland_moist())
    put("block/shadow_path_top", shadow_path_top())
    put("block/shadow_path_side", shadow_path_side())
    door_top, door_bottom = shadow_door()
    put("block/shadow_door_top", door_top)
    put("block/shadow_door_bottom", door_bottom)
    put("block/shadow_trapdoor", shadow_trapdoor())
    put("item/shadow_door", shadow_door_item())
    put("block/moonsilver_block", moonsilver_block())
    put("block/nightsteel_block", nightsteel_block())
    soul, soul_glow = soul_crystal_block()
    put("block/soul_crystal_block", soul)
    put("block/soul_crystal_block_glow", soul_glow)
    abyss, abyss_glow = abyssite_block()
    put("block/abyssite_block", abyss)
    put("block/abyssite_block_glow", abyss_glow)


if __name__ == "__main__":
    import sys

    sheet: dict[str, Image.Image] = {}
    build(lambda key, img: sheet.__setitem__(key.split("/")[-1], img))
    vs.preview(sheet, sys.argv[1] if len(sys.argv) > 1 else "shadow_blocks.png", zoom=6, tile=3, cols=5)
