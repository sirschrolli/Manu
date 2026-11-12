#!/usr/bin/env python3
"""
Die Texturen des Feenreichs im Vanilla-Stil (siehe tools/vanilla_style.py): Feenstein, Mondstein, Feenerde, Feengras,
Feenmoos, Glimmerholz und sein leuchtendes Laub, Riesenpilze, die Blumen und Pilze, Leuchtranken, Mondlilie,
Dornenranke und Dornenhecke, das Feenportal (bewegt), Feenstaub und das Feenfunkeln.

Farbwelt: perlmuttblasse Steine mit einem Hauch Lavendel, silbrige Rinde über rosa Holz, Laub in leuchtendem Rosa und
Türkis, Pilzhüte in Rosa, Himmelblau und Violett – kräftig, aber mit weichen Tonleitern, damit es im goldenen
Abendlicht nicht grell wirkt. Leuchtendes liegt auf eigenen Ebenen (_emissive / _glow).

Wird von tools/generate_textures.py aufgerufen: build(put). Bildfolgen (Portal) stehen in ANIMATIONS.
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
CLEAR = (0, 0, 0, 0)

#: Bildfolgen: Name -> (Bilder, Ticks je Bild, weich überblenden)
ANIMATIONS: dict[str, tuple[int, int, bool]] = {}

# =============================================================================
# Paletten (dunkel -> hell)
# =============================================================================

FAESTONE = vs.tones("#b7aec8", 4, spread=0.13, cool=255, warm=320, hue=10, sat=0.05)
FAE_BRICK = vs.tones("#b3a9c4", 7, spread=0.26, cool=255, warm=320, hue=12, sat=0.06)
MOONSTONE = vs.tones("#d8dff0", 7, spread=0.17, cool=225, warm=290, hue=14, sat=0.08)
SHEEN = [hexc("#bfe6ff"), hexc("#f6d4ff"), hexc("#d4fff4")]
SOIL = vs.tones("#735a76", 5, spread=0.24, cool=280, warm=330, hue=10, sat=0.06)
PEBBLE = vs.tones("#9a8cab", 2, spread=0.12)
GREY = [(v, v, v, 255) for v in (0x86, 0x92, 0x9e, 0xaa, 0xb6, 0xc2)]
MOSS = vs.tones("#9a78c8", 6, spread=0.28, cool=265, warm=310, hue=10, sat=0.04)
MOSS_GLOW = [hexc("#ffd6f4"), hexc("#d6fbff")]
BARK = vs.tones("#9d99ae", 6, spread=0.30, cool=250, warm=300, hue=10, sat=0.04)
BARK_SHINE = hexc("#e6e8f4")
WOOD = vs.tones("#d9a1c2", 6, spread=0.22, cool=320, warm=345, hue=8, sat=0.04)
STRIPPED = vs.tones("#d79abf", 5, spread=0.20, cool=315, warm=345, hue=8, sat=0.04)
PLANKS = vs.tones("#d6a2c1", 7, spread=0.27, cool=310, warm=350, hue=10, sat=0.05)
ROSE_LEAF = [hexc("#b8487e"), hexc("#d9649a"), hexc("#f08cbc"), hexc("#ffbfdc"), hexc("#fff0f8")]
AZURE_LEAF = [hexc("#1e7f94"), hexc("#2fa8b8"), hexc("#55d0d6"), hexc("#a4f2ec"), hexc("#f0fffc")]
STEM = vs.tones("#e6ddcf", 5, spread=0.16, cool=260, warm=40, hue=8, sat=0.04)
STEM_RING = [hexc("#9fe8f0"), hexc("#e0fffb")]
CAPS = {
    "rose": (vs.tones("#e0609e", 6, spread=0.30, cool=320, warm=350, hue=10, sat=0.04), hexc("#fff0f6"), hexc("#ffc4e0")),
    "azure": (vs.tones("#3fa6e0", 6, spread=0.30, cool=215, warm=190, hue=10, sat=0.04), hexc("#f2fbff"), hexc("#bfeaff")),
    "violet": (vs.tones("#9258d8", 6, spread=0.30, cool=270, warm=295, hue=10, sat=0.04), hexc("#f6eeff"), hexc("#dcc4ff")),
}
GILLS = {
    "rose": vs.tones("#f4c4d8", 4, spread=0.14),
    "azure": vs.tones("#c4e2f4", 4, spread=0.14),
    "violet": vs.tones("#dccaf2", 4, spread=0.14),
}
PLANT = [hexc("#2f6b52"), hexc("#3e8a64"), hexc("#58a878"), hexc("#82c896")]
DARK_PLANT = [hexc("#2a3f4e"), hexc("#3a5466"), hexc("#4f6e80")]
THORN = [hexc("#1e1028"), hexc("#34183f"), hexc("#4f2463"), hexc("#6e3488"), hexc("#e8a0ff")]


def _img(g: vs.Grid, pal: list[Color]) -> Image.Image:
    return vs.paint(g, pal)


def _only(art: list[str], colors: dict[str, Color]) -> Image.Image:
    """Nur die genannten Zeichen der Vorlage (für Leuchtebenen); alles andere bleibt durchsichtig."""
    rows = ["".join(ch if ch in colors else "." for ch in row) for row in art]
    return pa.sprite(rows, colors)


def _px(img: Image.Image, x: int, y: int, c: Color) -> None:
    if 0 <= x < img.width and 0 <= y < img.height:
        img.putpixel((x, y), c)


# =============================================================================
# Feenstein und Mondstein
# =============================================================================

FAE_BRICK_ROWS = [(4, [3, 11]), (4, [7, 15]), (4, [1, 10]), (4, [5, 13])]


def faestone() -> Image.Image:
    """Feenstein: heller, körniger Fels mit lila Hauch, hier und da ein perlmutt schimmerndes Korn."""
    img = _img(vs.stone(63101, shares=(0.12, 0.33, 0.37, 0.18)), FAESTONE)
    rnd = random.Random(63102)
    for _ in range(4):
        x, y = rnd.randrange(N), rnd.randrange(N)
        img.putpixel((x, y), pa.mix(img.getpixel((x, y)), SHEEN[rnd.randrange(3)], 0.55))
    return img


def faestone_bricks() -> Image.Image:
    return _img(vs.bricks(63201, FAE_BRICK_ROWS), FAE_BRICK)


def mossy_faestone_bricks() -> Image.Image:
    """Feensteinziegel, in deren Fugen und auf deren Oberkanten Polster aus lavendelfarbenem Feenmoos sitzen."""
    g = vs.bricks(63201, FAE_BRICK_ROWS)
    img = _img(g, FAE_BRICK)
    f = vs.layered(63211, ((8, 8, 0.5), (4, 4, 0.3), (2, 2, 0.2)))
    for y in range(N):
        for x in range(N):
            near_joint = g[y][x] <= 2 or (y + 1 < N and g[y + 1][x] <= 1)
            v = f[y][x] + (0.2 if near_joint else 0.0)
            if v > 0.6:
                level = 4 if v > 0.82 else 3 if v > 0.7 else 2
                if (x + y) % 5 == 0 and level < 4:
                    level -= 1
                img.putpixel((x, y), MOSS[max(0, level)])
    return img


def moonstone_bricks() -> Image.Image:
    """
    Mondsteinziegel: große, glatte, milchweiße Blöcke mit abgefasten Kanten (zwei Reihen, versetzt), auf der Fläche ein
    bläulich-rosa Schimmer wie bei echtem Mondstein.
    """
    g = vs.grid(4)
    rows = [(0, 8, [4, 12]), (8, 8, [0, 8])]
    shine = vs.layered(63301, ((8, 4, 0.6), (4, 2, 0.4)))
    for y0, height, joints in rows:
        for yy in range(height):
            y = y0 + yy
            for x in range(N):
                rel = [(x - j) % N for j in joints]
                at_joint = any(r == 0 for r in rel)
                left = min(rel)                      # Abstand zur Fuge links
                right = min((j - x) % N for j in joints)
                if yy == height - 1 or at_joint:
                    g[y][x] = 0
                elif yy == 0:
                    g[y][x] = 6 if left > 1 else 5
                elif yy == height - 2:
                    g[y][x] = 2
                elif left == 1:
                    g[y][x] = 5
                elif right == 1:
                    g[y][x] = 2
                else:
                    g[y][x] = 3 + (1 if shine[y][x] > 0.55 else 0) + (1 if shine[y][x] > 0.8 else 0)
    img = _img(g, MOONSTONE)
    rnd = random.Random(63302)
    for _ in range(9):
        x, y = rnd.randrange(N), rnd.randrange(N)
        if g[y][x] >= 3:
            img.putpixel((x, y), pa.mix(img.getpixel((x, y)), SHEEN[rnd.randrange(3)], 0.65))
    return img


CRESCENT = [
    "....####....",
    "..######....",
    ".#####......",
    "#####.......",
    "####........",
    "####........",
    "####........",
    "####........",
    "#####.......",
    ".#####......",
    "..######....",
    "....####....",
]


def chiseled_moonstone() -> tuple[Image.Image, Image.Image]:
    """
    Gemeißelter Mondstein: eine polierte Platte mit eingelassenem Feld, darin eine erhabene Mondsichel mit drei kleinen
    Sternen. Sichel und Sterne leuchten (eigene Ebene).
    """
    g = vs.polished(63401, shares=(0.2, 0.45, 0.35))
    for y in range(N):
        for x in range(N):
            g[y][x] = 4 + g[y][x]
    for y in range(1, 15):
        for x in range(1, 15):
            g[y][x] = 3
    vs.bevel_frame(g, 1, 1, 14, 14, light=6, dark=1)
    img = _img(g, MOONSTONE)
    glow = pa.new()
    moon = [hexc("#9fd8ff"), hexc("#d6f0ff"), hexc("#ffffff")]
    cells = {(3 + x, 2 + y) for y, row in enumerate(CRESCENT) for x, ch in enumerate(row) if ch == "#"}
    for (x, y) in cells:
        edge_light = (x - 1, y) not in cells or (x, y - 1) not in cells
        edge_dark = (x + 1, y) not in cells or (x, y + 1) not in cells
        c = moon[2] if edge_light else moon[0] if edge_dark else moon[1]
        img.putpixel((x, y), c)
        glow.putpixel((x, y), c)
    for (sx, sy) in ((11, 4), (12, 9), (9, 12)):
        for dx, dy, c in ((0, 0, hexc("#ffffff")), (1, 0, hexc("#ffe8a8")), (-1, 0, hexc("#ffe8a8")), (0, 1, hexc("#ffe8a8")),
                          (0, -1, hexc("#ffe8a8"))):
            if (sx, sy) == (9, 12) and (dx or dy):
                continue
            img.putpixel((sx + dx, sy + dy), c)
            glow.putpixel((sx + dx, sy + dy), c)
    return img, glow


# =============================================================================
# Böden
# =============================================================================

def fae_soil() -> Image.Image:
    return _img(vs.soil(63501), SOIL + PEBBLE)


def fae_grass_top() -> Image.Image:
    """Grau (die Farbe kommt aus dem Biom): weiche, feine Halmspitzen."""
    return _img(vs.grass_top(63601), GREY[:5])


def _grass_fringe(seed: int) -> list[int]:
    rnd = random.Random(seed)
    depth = []
    for x in range(N):
        depth.append(2 + (1 if rnd.random() < 0.5 else 0) + (1 if rnd.random() < 0.25 else 0))
    for x in range(N):
        if depth[x] >= 4 and depth[(x - 1) % N] <= 2 and depth[(x + 1) % N] <= 2:
            depth[x] = 3
    return depth


def fae_grass_side() -> Image.Image:
    img = fae_soil()
    fringe = _grass_fringe(63611)
    top = vs.grass_top(63612)
    for x in range(N):
        for y in range(fringe[x]):
            img.putpixel((x, y), GREY[min(4, top[y][x])])
    return img


def fae_grass_side_overlay() -> Image.Image:
    img = pa.new()
    fringe = _grass_fringe(63611)
    top = vs.grass_top(63612)
    for x in range(N):
        for y in range(fringe[x]):
            img.putpixel((x, y), GREY[min(5, top[y][x] + 1)])
    return img


def fae_moss() -> tuple[Image.Image, Image.Image]:
    """Feenmoos: weiche, körnige Polster in Lavendel; darin sitzen glimmende Sporen (eigene Ebene)."""
    f = vs.mix_fields((vs.strokes(63701, (1, 2, 1), (1, 2, 1)), 0.6), (vs.layered(63702, ((4, 4, 0.6), (8, 8, 0.4))), 0.4))
    g = vs.despeckle(vs.quantize(f, (0.1, 0.2, 0.28, 0.24, 0.12, 0.06)), passes=1)
    img = _img(g, MOSS)
    glow = pa.new()
    rnd = random.Random(63703)
    spots = 0
    while spots < 7:
        x, y = rnd.randrange(N), rnd.randrange(N)
        if g[y][x] < 4 or glow.getpixel((x, y))[3]:
            continue
        c = MOSS_GLOW[spots % 2]
        glow.putpixel((x, y), c)
        img.putpixel((x, y), c)
        spots += 1
    return img, glow


# =============================================================================
# Glimmerholz
# =============================================================================

def glimmerwood_log() -> Image.Image:
    """Silbrige Rinde mit feinen, fast weißen Graten, die im Licht glänzen – wie Birke, nur kühler und glatter."""
    g = vs.bark(63801, grooves=4)
    img = _img(g, BARK)
    rnd = random.Random(63802)
    for _ in range(6):
        x = rnd.randrange(N)
        y = rnd.randrange(N)
        for k in range(rnd.randint(2, 4)):
            if g[(y + k) % N][x] >= 3:
                img.putpixel((x, (y + k) % N), BARK_SHINE)
    return img


def glimmerwood_log_top() -> Image.Image:
    g = vs.log_top(63811)
    return _img(g, [BARK[0], BARK[2], WOOD[1], WOOD[2], WOOD[3], WOOD[4]])


def stripped_glimmerwood_log() -> Image.Image:
    return _img(vs.stripped(63821), STRIPPED)


def stripped_glimmerwood_log_top() -> Image.Image:
    g = vs.log_top(63831)
    return _img(g, [STRIPPED[0], STRIPPED[1], WOOD[1], WOOD[2], WOOD[3], WOOD[4]])


def glimmerwood_planks() -> Image.Image:
    return _img(vs.planks(63901, joints=(None, 10, 4, 13)), PLANKS)


def _leaves(seed: int, pal: list[Color], blossoms: Color | None) -> Image.Image:
    """Laub wie Vanilla (Büschel mit hellen Spitzen, Lücken), dazu ein paar fast weiße, funkelnde Blattspitzen."""
    g = vs.leaves(seed, holes=0.26)
    img = vs.paint(g, pal[:4])
    rnd = random.Random(seed + 7)
    count = 0
    while count < 6:
        x, y = rnd.randrange(N), rnd.randrange(N)
        if g[y][x] == 3:
            img.putpixel((x, y), pal[4])
            count += 1
    if blossoms is not None:
        for _ in range(4):
            x, y = rnd.randrange(N), rnd.randrange(N)
            if g[y][x] >= 1:
                img.putpixel((x, y), blossoms)
    return img


def rose_glimmer_leaves() -> Image.Image:
    return _leaves(64001, ROSE_LEAF, hexc("#fff6d8"))


def azure_glimmer_leaves() -> Image.Image:
    return _leaves(64011, AZURE_LEAF, None)


SAPLING = [
    "................",
    "......aab.......",
    "....aabbbca.....",
    "...abbccbbca....",
    "...bbcddcbcb....",
    "....bccbccb.a...",
    "..abbcb.bbcbbb..",
    "..bbccbbccddcb..",
    "...bcdcb.bccb...",
    ".....bbs.bb.....",
    "........s.......",
    "......s.s.......",
    ".......ss.......",
    ".......s........",
    ".......s........",
    ".......s........",
]


def glimmerwood_sapling() -> Image.Image:
    colors = {"a": ROSE_LEAF[0], "b": ROSE_LEAF[1], "c": ROSE_LEAF[2], "d": ROSE_LEAF[3], "s": BARK[1]}
    return pa.sprite(SAPLING, colors)


# =============================================================================
# Riesenpilze
# =============================================================================

def fae_mushroom_stem_side() -> Image.Image:
    """Pilzstiel: blasse, faserige Haut, dazu zwei schmale, türkis glimmende Ringe."""
    img = _img(vs.stripped(64101), STEM)
    for ring_y in (4, 11):
        for x in range(N):
            wave = 1 if (x * 3 + ring_y) % 7 == 0 else 0
            img.putpixel((x, ring_y + wave), STEM_RING[0])
            if (x + ring_y) % 4 == 0:
                img.putpixel((x, ring_y + wave), STEM_RING[1])
    return img


def fae_mushroom_stem_top() -> Image.Image:
    g = vs.log_top(64111)
    return _img(g, [STEM[0], STEM[1], STEM[1], STEM[2], STEM[3], STEM[4]])


def _cap_spots(seed: int, img: Image.Image, spot: Color, spot_shade: Color, count: int) -> None:
    rnd = random.Random(seed)
    placed: list[tuple[int, int]] = []
    tries = 0
    while len(placed) < count and tries < 400:
        tries += 1
        x, y = rnd.randrange(N), rnd.randrange(N)
        if any(abs(x - px) < 4 and abs(y - py) < 4 for px, py in placed):
            continue
        placed.append((x, y))
        big = rnd.random() < 0.55
        cells = [(0, 0), (1, 0), (0, 1), (1, 1)] if big else [(0, 0), (1, 0)]
        for dx, dy in cells:
            img.putpixel(((x + dx) % N, (y + dy) % N), spot)
        img.putpixel(((x + 1) % N, (y + (2 if big else 1)) % N), spot_shade)


def mushroom_cap_top(kind: str) -> Image.Image:
    """Hut: samtig glänzende Haut in der Farbe der Art, mit hellen, runden Tupfen (wie beim Fliegenpilz)."""
    pal, spot, shade = CAPS[kind]
    f = vs.mix_fields((vs.layered(64201 + len(kind), ((8, 8, 0.5), (4, 4, 0.3), (2, 2, 0.2))), 1.0))
    g = vs.despeckle(vs.quantize(f, (0.08, 0.17, 0.3, 0.27, 0.13, 0.05)), passes=1)
    img = _img(g, pal)
    _cap_spots(64211 + len(kind), img, spot, shade, 5)
    return img


def mushroom_cap_side(kind: str) -> Image.Image:
    """Hutseite: wie oben, nach unten etwas dunkler, mit einem helleren Saum ganz unten."""
    pal, spot, shade = CAPS[kind]
    f = vs.layered(64221 + len(kind), ((8, 4, 0.5), (4, 2, 0.3), (2, 2, 0.2)))
    g = vs.grid(0)
    for y in range(N):
        for x in range(N):
            v = f[y][x] - y * 0.025
            g[y][x] = max(0, min(5, int(v * 5.2 + 0.8)))
    img = _img(g, pal)
    _cap_spots(64231 + len(kind), img, spot, shade, 4)
    for x in range(N):
        img.putpixel((x, N - 1), pal[4] if x % 3 else pal[5])
    return img


def mushroom_cap_bottom(kind: str) -> Image.Image:
    """Unterseite: feine, strahlenförmige Lamellen in einem hellen Ton."""
    pal = GILLS[kind]
    img = pa.new(pal[2])
    for y in range(N):
        for x in range(N):
            a = math.atan2(y - 7.5, x - 7.5)
            r = math.hypot(x - 7.5, y - 7.5)
            stripe = math.sin(a * 22.0 + r * 0.15)
            c = pal[3] if stripe > 0.55 else pal[1] if stripe < -0.5 else pal[2]
            if r < 1.6:
                c = STEM[1]
            img.putpixel((x, y), c)
    return img


# =============================================================================
# Pflanzen
# =============================================================================

SPROUTS = [
    "................",
    "................",
    "....a......a....",
    "....a...a..a....",
    "...ba...a..ab...",
    "...b.a..ba.a....",
    "..b..a.b.a.b..a.",
    ".ab..ba..a.b.ab.",
    ".a.b.b.a.bab.b..",
    ".b.b.ab.bba..b..",
    "..bc.bc.b.bcb.c.",
    "..c.cbc.cbc.bc..",
    "..cc.cdc.cdcc...",
    "...cdcdddcdd.c..",
    "...ddcdddddd....",
    "....dddd.ddd....",
]


def fae_sprouts() -> Image.Image:
    """Feenhalme: weiche, hohe Halme (grau – die Farbe kommt aus dem Biom)."""
    colors = {"a": GREY[5], "b": GREY[4], "c": GREY[2], "d": GREY[0]}
    return pa.sprite(SPROUTS, colors)


MOONBELL = [
    "................",
    "......ss........",
    ".....s..s.......",
    "....s....s......",
    "...abba..abba...",
    "..abccba.bccb...",
    "..bcddcb.bddcb..",
    "..b.ee.b.b.ee.b.",
    "....ee..s..ee...",
    "...l....s.......",
    "..lll..abba.....",
    "...ll.abccba..l.",
    "....l.bcddcb.ll.",
    ".....lb.ee.bll..",
    "......s.ee.s....",
    ".......ss.......",
]


def moonbell() -> tuple[Image.Image, Image.Image]:
    """Mondglocke: drei himmelblaue Glöckchen an einem gebogenen Stiel, ihre Klöppel leuchten weiß."""
    bell = [hexc("#3e7fc8"), hexc("#68a8e8"), hexc("#9cd0ff"), hexc("#d4ecff")]
    colors = {"a": bell[0], "b": bell[1], "c": bell[2], "d": bell[3], "e": hexc("#fff8d0"), "s": PLANT[1], "l": PLANT[2]}
    img = pa.sprite(MOONBELL, colors)
    glow = _only(MOONBELL, {"c": bell[2], "d": bell[3], "e": hexc("#fff8d0")})
    return img, glow


FAE_ROSE = [
    "................",
    "................",
    ".....abbba......",
    "....abccccb.....",
    "...abcdedccb....",
    "...bcdeffdcb....",
    "...bcdfgfecb....",
    "...abcdeedcba...",
    "....abccccba....",
    ".....aabbaa.....",
    ".......s..ll....",
    "..ll...s.lll....",
    "..lll..s.ll.....",
    "...lll.s........",
    ".......s........",
    ".......s........",
]


def fae_rose() -> tuple[Image.Image, Image.Image]:
    """Feenrose: zartrosa, dicht gefüllte Blüte; auf den inneren Blättern glitzern Tautropfen (leuchten)."""
    petal = [hexc("#a8386e"), hexc("#d05a92"), hexc("#ec86b4"), hexc("#ffaed0"), hexc("#ffd4e6"), hexc("#fff0f6")]
    colors = {"a": petal[0], "b": petal[1], "c": petal[2], "d": petal[3], "e": petal[4], "f": petal[5], "g": hexc("#ffffff"),
              "s": PLANT[0], "l": PLANT[2]}
    img = pa.sprite(FAE_ROSE, colors)
    glow = _only(FAE_ROSE, {"f": petal[5], "g": hexc("#ffffff"), "e": petal[4]})
    return img, glow


STARLACE_TOP = [
    "................",
    "...w.w....w.w...",
    "..wpwpw..wpwpw..",
    "...wpw.w..wpw...",
    "..w.s.wpw..s.w..",
    "....s..w...s....",
    ".....s..s.s.....",
    ".w.w..s.ss..w.w.",
    "wpwpw..ss..wpwpw",
    ".wpw...s....wpw.",
    "...s...s...s....",
    "....s..s..s.....",
    ".....s.s.s......",
    "......sss.......",
    ".......s........",
    ".......s........",
]

STARLACE_BOTTOM = [
    ".......s........",
    ".......s........",
    ".......s........",
    "......ls........",
    ".....lls........",
    "....lll.s.......",
    ".......s.ll.....",
    ".......s.lll....",
    ".......s..lll...",
    "...ll..s........",
    "..llll.s........",
    "...lll.s..l.....",
    ".......s.lll....",
    ".......s.ll.....",
    ".......s........",
    ".......s........",
]


def starlace() -> tuple[Image.Image, Image.Image, Image.Image]:
    """Sternspitze: unten Stiel und Fiederblätter, oben weiße, spitzenzarte Dolden mit leuchtenden Mitten."""
    colors = {"w": hexc("#f4f0ff"), "p": hexc("#ffe6a0"), "s": PLANT[1], "l": PLANT[2]}
    bottom = pa.sprite(STARLACE_BOTTOM, colors)
    top = pa.sprite(STARLACE_TOP, colors)
    glow = _only(STARLACE_TOP, {"p": hexc("#ffe6a0"), "w": hexc("#f4f0ff")})
    return bottom, top, glow


LANTERN = [
    "................",
    "................",
    "..........aa....",
    ".........abba...",
    ".........bccb...",
    "...aaa...bddb...",
    "..abbba...ss....",
    "..bcccb...s.....",
    "..bdddb...s.....",
    "..bdddb..s......",
    "...sss...s..aa..",
    "....s...s..bccb.",
    "....s...s..bddb.",
    ".....s.s....ss..",
    ".....s.s....s...",
    "......s....s....",
]


def lantern_mushroom() -> tuple[Image.Image, Image.Image]:
    """Laternenpilze: drei kleine Pilze mit glühenden, laternenförmigen Hüten in Orange und Gold."""
    cap = [hexc("#b84a1c"), hexc("#ff8a2a"), hexc("#ffc04a"), hexc("#fff0a0")]
    colors = {"a": cap[0], "b": cap[1], "c": cap[2], "d": cap[3], "s": STEM[2]}
    img = pa.sprite(LANTERN, colors)
    glow = _only(LANTERN, {"b": cap[1], "c": cap[2], "d": cap[3]})
    return img, glow


def _vine(seed: int, tip: bool) -> tuple[Image.Image, Image.Image]:
    """Leuchtranke: zwei, drei verschlungene Stränge mit kleinen Blättern und glimmenden Knospen (an der Spitze mehr)."""
    rnd = random.Random(seed)
    img = pa.new()
    glow = pa.new()
    vine = [hexc("#24606a"), hexc("#348a90"), hexc("#4cb4b0")]
    bud = [hexc("#7ff0ff"), hexc("#e0fffb")]
    for strand, x0 in enumerate((4, 8, 11)):
        phase = rnd.uniform(0, 2 * math.pi)
        end = N - (rnd.randint(2, 5) if tip else 0)
        for y in range(end):
            x = int(round(x0 + math.sin(y * 0.55 + phase) * 1.6))
            _px(img, x, y, vine[1 + (y + strand) % 2])
            if y % 4 == strand % 4:
                side = 1 if (y // 4) % 2 == 0 else -1
                _px(img, x + side, y, vine[2])
                _px(img, x + side * 2, y + 1, vine[0])
            if rnd.random() < (0.16 if not tip else 0.22):
                _px(img, x, y, bud[0])
                _px(glow, x, y, bud[0])
        if tip:
            x = int(round(x0 + math.sin(end * 0.55 + phase) * 1.6))
            for dx, dy, c in ((0, 0, bud[1]), (-1, 0, bud[0]), (1, 0, bud[0]), (0, 1, bud[0])):
                _px(img, x + dx, end + dy, c)
                _px(glow, x + dx, end + dy, c)
    return img, glow


LILY_FLOWER = [
    "................",
    "................",
    "................",
    "................",
    "................",
    ".......w........",
    "......wpw.......",
    "...w..wpw..w....",
    "...ww.wpw.ww....",
    "....wwpppww.....",
    "..wwwppyppwww...",
    "...wwpyyypww....",
    "....wwpppww.....",
    ".....lllll......",
    "....ll...ll.....",
    "................",
]


def moon_lily() -> tuple[Image.Image, Image.Image, Image.Image, Image.Image]:
    """Mondlilie: ein dunkles, rundes Blatt mit Einschnitt (von oben), darauf eine weiße Blüte mit goldener Mitte."""
    pad = pa.new()
    leaf = [hexc("#1f4a46"), hexc("#2c6a5e"), hexc("#3f8c72"), hexc("#62b088")]
    for y in range(N):
        for x in range(N):
            dx, dy = x - 7.5, y - 7.5
            r = math.hypot(dx, dy)
            a = math.atan2(dy, dx)
            if r > 7.2 or (abs(a - 0.6) < 0.22 and r > 1.5):
                continue
            vein = abs(math.sin(a * 5.0)) > 0.94 and r > 2
            v = 2 if r < 6.2 else 1
            if vein:
                v = 3
            if r > 6.6:
                v = 0
            pad.putpixel((x, y), leaf[v])
    colors = {"w": hexc("#f6f4ff"), "p": hexc("#e0e4ff"), "y": hexc("#ffe08a"), "l": leaf[2]}
    flower = pa.sprite(LILY_FLOWER, colors)
    glow = _only(LILY_FLOWER, {"w": hexc("#f6f4ff"), "p": hexc("#e0e4ff"), "y": hexc("#ffe08a")})
    # Gegenstand: Blatt mit Blüte von oben
    item = pad.copy()
    for (x, y, c) in ((7, 6, "w"), (8, 6, "w"), (6, 7, "w"), (9, 7, "w"), (7, 7, "y"), (8, 7, "y"), (7, 8, "y"), (8, 8, "y"),
                      (6, 8, "w"), (9, 8, "w"), (7, 9, "w"), (8, 9, "w"), (5, 7, "p"), (10, 8, "p"), (7, 5, "p"), (8, 10, "p")):
        item.putpixel((x, y), colors[c])
    return pad, flower, glow, item


BRIAR = [
    "................",
    ".....t....t.....",
    "....tb...bt.....",
    "...b.bt.tb..t...",
    "..tb..bb.b.bt...",
    "...bb.tbbt.b....",
    "..t.bbb.bbbt..t.",
    ".tb..cbt.cb..bt.",
    "..bbc.cbbc.bbb..",
    "...tcb.ccbcb.t..",
    "..t.bccdccc.bt..",
    ".tbb.cdcdcdbb...",
    "...bbcdddddcbt..",
    "..t.bcddddcb....",
    "....bbcdddcb....",
    ".....bbdddb.....",
]


def thornbriar() -> Image.Image:
    """Dornenranke: violette, verschlungene Ranken mit hellen Dornenspitzen."""
    colors = {"t": THORN[4], "b": THORN[3], "c": THORN[2], "d": THORN[1]}
    return pa.sprite(BRIAR, colors)


def thorn_hedge() -> Image.Image:
    """
    Dornenhecke: ein dichtes Geflecht dunkler, violetter Ranken (mit Lücken wie Laub), auf dem hier und da helle
    Dornenspitzen sitzen.
    """
    g = vs.leaves(64601, holes=0.28)
    img = vs.paint(g, THORN[:4])
    rnd = random.Random(64602)
    count = 0
    while count < 9:
        x, y = rnd.randrange(N), rnd.randrange(N)
        if g[y][x] >= 1 and g[(y - 1) % N][x] < 0:
            # eine Dornenspitze, die aus dem Geflecht nach oben ragt
            img.putpixel((x, y), THORN[4])
            img.putpixel((x, (y - 1) % N), (THORN[4][0], THORN[4][1], THORN[4][2], 255))
            count += 1
    # durchgehende Ranken quer durch
    for k in range(3):
        y = rnd.randrange(N)
        slope = rnd.choice((-1, 1))
        for x in range(N):
            yy = (y + (x * slope) // 3) % N
            if g[yy][x] >= 0:
                img.putpixel((x, yy), THORN[1] if (x + k) % 3 else THORN[0])
    return img


# =============================================================================
# Das Feenportal (bewegt)
# =============================================================================

PORTAL_COLORS = [hexc("#5a2a8a"), hexc("#b04ac8"), hexc("#ff7ac8"), hexc("#ffc488"), hexc("#fff4d8")]


def fae_portal(frames: int = 32) -> Image.Image:
    """
    Der Schleier des Feenportals als Bildfolge: Schlieren in Violett, Rosa und Gold wogen sanft aufwärts, dazwischen
    steigen funkelnde Sternchen empor. Leicht durchscheinend.
    """
    sheet = Image.new("RGBA", (N, N * frames))
    rnd = random.Random(64701)
    motes = [(rnd.uniform(0, N), rnd.uniform(0, N), rnd.uniform(0.6, 1.4), rnd.random()) for _ in range(14)]
    for f in range(frames):
        t = f / frames
        frame = Image.new("RGBA", (N, N))
        px = frame.load()
        for y in range(N):
            for x in range(N):
                u = x / N * 2 * math.pi
                v = y / N * 2 * math.pi
                w1 = math.sin(u + v * 1.0 + t * 2 * math.pi) * 0.5 + 0.5
                w2 = math.sin(u * 2 - v * 1.0 + t * 2 * math.pi * 2 + 1.3) * 0.5 + 0.5
                w3 = math.sin(v * 2 + t * 2 * math.pi + math.sin(u) * 1.5) * 0.5 + 0.5
                val = 0.2 + 0.45 * w1 * w3 + 0.35 * w2 * w2
                i = val * (len(PORTAL_COLORS) - 1.001)
                lo = int(i)
                c = pa.mix(PORTAL_COLORS[lo], PORTAL_COLORS[min(lo + 1, len(PORTAL_COLORS) - 1)], i - lo)
                px[x, y] = (c[0], c[1], c[2], 200)
        for (mx, my, speed, phase) in motes:
            yy = (my - t * N * speed) % N
            xx = (mx + math.sin((t + phase) * 2 * math.pi) * 1.2) % N
            ix, iy = int(xx), int(yy)
            b = 0.5 + 0.5 * math.sin((t * 3 + phase) * 2 * math.pi)
            px[ix, iy] = pa.mix(px[ix, iy], hexc("#ffffff"), 0.55 + 0.45 * b)[:3] + (240,)
        sheet.paste(frame, (0, f * N))
    return sheet


# =============================================================================
# Gegenstände und Partikel
# =============================================================================

DUST = [
    "................",
    "........w.......",
    ".......wyw......",
    "........w...p...",
    "...p...........",
    "..........w.....",
    ".....aa..wyw....",
    "...aabbaa.w.....",
    "..abbccbba......",
    ".abccddccba.p...",
    ".abcdeedcbba....",
    "abcddeeddccba...",
    "abccddddccbba...",
    ".aabbccbbbaa....",
    "...aaaaaaa......",
    "................",
]


def fae_dust() -> Image.Image:
    """Feenstaub: ein Häufchen glitzernden, rosagoldenen Staubs, über dem funkelnde Sternchen schweben."""
    colors = {"a": hexc("#a84a8a"), "b": hexc("#e070b0"), "c": hexc("#ff9ec8"), "d": hexc("#ffcfa8"), "e": hexc("#fff4d8"),
              "w": hexc("#ffffff"), "y": hexc("#ffe68a"), "p": hexc("#ffb8e8")}
    img = pa.sprite(DUST, colors)
    rnd = random.Random(64801)
    for _ in range(7):
        x, y = rnd.randrange(1, 13), rnd.randrange(8, 14)
        if img.getpixel((x, y))[3]:
            img.putpixel((x, y), hexc("#ffffff") if rnd.random() < 0.5 else hexc("#ffe68a"))
    return img


def glimmerwood_door() -> tuple[Image.Image, Image.Image, Image.Image]:
    """Tür aus Glimmerholz: helle Bretter im Rahmen, oben ein spitzbogiges Fenster, unten silberne Beschläge."""
    pal = PLANKS
    silver = [hexc("#7a7f96"), hexc("#bfc6da"), hexc("#f0f4ff")]

    def body(seed: int) -> vs.Grid:
        grain = vs.quantize(vs.strokes(seed, (1,), (1, 2, 3, 2, 1)), (0.25, 0.5, 0.25))
        g = vs.grid(3)
        for y in range(N):
            for x in range(N):
                if x in (0, 15):
                    v = 1
                elif x == 1:
                    v = 5
                elif x == 14:
                    v = 2
                else:
                    col = (x - 2) % 4
                    v = 5 if col == 0 else 1 if col == 3 else 2 + grain[y][x]
                g[y][x] = v
        return g

    g = body(64901)
    for x in range(N):
        g[0][x] = 1 if 0 < x < 15 else 0
        g[1][x] = 5 if 0 < x < 15 else 4
    top = _img(g, pal)
    # Spitzbogenfenster
    for y in range(3, 14):
        for x in range(4, 12):
            dx = abs(x - 7.5)
            limit = 3.5 if y >= 7 else 3.5 - (7 - y) * 0.75
            if dx <= limit:
                top.putpixel((x, y), CLEAR)
            elif dx <= limit + 1.0:
                top.putpixel((x, y), silver[2] if x < 8 else silver[1])
    for y in range(4, 14):
        top.putpixel((7, y), silver[1]) if top.getpixel((7, y))[3] == 0 else None
    g = body(64902)
    for x in range(N):
        g[14][x] = 5 if 0 < x < 15 else 4
        g[15][x] = 1 if 0 < x < 15 else 0
    bottom = _img(g, pal)
    for y in (3, 11):
        for x in range(1, 7):
            bottom.putpixel((x, y), silver[1])
            bottom.putpixel((x, y + 1), silver[0])
        bottom.putpixel((1, y), silver[2])
    for (x, y, c) in ((12, 6, silver[2]), (12, 7, silver[1]), (13, 7, silver[0])):
        bottom.putpixel((x, y), c)
    # Gegenstand: beide Hälften zusammengeschoben
    item = pa.new()
    t = top.resize((8, 8), Image.NEAREST)
    b = bottom.resize((8, 8), Image.NEAREST)
    item.alpha_composite(t, (4, 0))
    item.alpha_composite(b, (4, 8))
    return top, bottom, item


def glimmerwood_trapdoor() -> Image.Image:
    """Falltür: ein Rahmen aus Glimmerholz um ein Gitter aus vier spitzbogigen Öffnungen."""
    g = vs.planks(64911, joints=(None, None, None, None))
    img = _img(g, PLANKS)
    for y in range(N):
        for x in range(N):
            if x in (0, 15) or y in (0, 15):
                img.putpixel((x, y), PLANKS[1])
            elif x in (1, 14) or y in (1, 14):
                img.putpixel((x, y), PLANKS[5])
    for (x0, y0) in ((3, 3), (9, 3), (3, 9), (9, 9)):
        for y in range(y0, y0 + 4):
            for x in range(x0, x0 + 4):
                if not (y == y0 and x in (x0, x0 + 3)):
                    img.putpixel((x, y), CLEAR)
    return img


def thorn_projectile() -> Image.Image:
    """
    Der geschleuderte Dorn (Aufteilung wie der Vanilla-Pfeil, 32 × 32): oben die Seitenansicht – links die helle, scharfe
    Spitze, nach rechts ein dicker werdender, violetter Dorn mit dunklem Ende; darunter (5 × 5) der Querschnitt von hinten.
    """
    img = Image.new("RGBA", (32, 32), CLEAR)
    tip, light, mid, dark, deep = hexc("#fbe8ff"), THORN[4], THORN[3], THORN[2], THORN[1]
    rows = [
        "................",
        "..tlmmmmmmmdd...",
        "tllmmmmmddddddk.",
        "..tlmmmmmmmdd...",
        "................",
    ]
    colors = {"t": tip, "l": light, "m": mid, "d": dark, "k": deep}
    for y, row in enumerate(rows):
        for x, ch in enumerate(row):
            if ch in colors:
                img.putpixel((x, y), colors[ch])
    for y in range(5):
        for x in range(5):
            if abs(x - 2) + abs(y - 2) <= 2:
                img.putpixel((x, 5 + y), dark if abs(x - 2) + abs(y - 2) == 2 else mid)
    return img


def fae_spark(frame: int) -> Image.Image:
    """Feenfunkeln (4 Bilder): ein kleiner, vierstrahliger Stern in Gold und Rosa, der zu einem Punkt verglimmt."""
    img = pa.new()
    core = [hexc("#ffffff"), hexc("#fff4c8"), hexc("#ffd0e8"), hexc("#f0a0d0")][frame]
    ray = [hexc("#ffe08a"), hexc("#ffb8e0"), hexc("#e890c8"), None][frame]
    reach = [3, 2, 1, 0][frame]
    halo = [hexc("#ffb0d8", 150), hexc("#ff9ed0", 110), hexc("#e890c8", 80), hexc("#e890c8", 60)][frame]
    for dx in (-1, 0, 1):
        for dy in (-1, 0, 1):
            if dx or dy:
                img.putpixel((7 + dx, 7 + dy), halo)
    img.putpixel((7, 7), core)
    if ray is not None:
        for k in range(1, reach + 1):
            a = 255 if k < reach else 170
            for (x, y) in ((7 + k, 7), (7 - k, 7), (7, 7 + k), (7, 7 - k)):
                img.putpixel((x, y), (ray[0], ray[1], ray[2], a))
    return img


# =============================================================================
# Ausgabe
# =============================================================================

def build(put: Callable[[str, Image.Image], None]) -> None:
    put("block/faestone", faestone())
    put("block/faestone_bricks", faestone_bricks())
    put("block/mossy_faestone_bricks", mossy_faestone_bricks())
    put("block/moonstone_bricks", moonstone_bricks())
    chiseled, chiseled_glow = chiseled_moonstone()
    put("block/chiseled_moonstone", chiseled)
    put("block/chiseled_moonstone_glow", chiseled_glow)
    put("block/fae_soil", fae_soil())
    put("block/fae_grass_top", fae_grass_top())
    put("block/fae_grass_side", fae_grass_side())
    put("block/fae_grass_side_overlay", fae_grass_side_overlay())
    moss, moss_glow = fae_moss()
    put("block/fae_moss", moss)
    put("block/fae_moss_glow", moss_glow)
    put("block/glimmerwood_log", glimmerwood_log())
    put("block/glimmerwood_log_top", glimmerwood_log_top())
    put("block/stripped_glimmerwood_log", stripped_glimmerwood_log())
    put("block/stripped_glimmerwood_log_top", stripped_glimmerwood_log_top())
    put("block/glimmerwood_planks", glimmerwood_planks())
    put("block/rose_glimmer_leaves", rose_glimmer_leaves())
    put("block/azure_glimmer_leaves", azure_glimmer_leaves())
    put("block/glimmerwood_sapling", glimmerwood_sapling())
    door_top, door_bottom, door_item = glimmerwood_door()
    put("block/glimmerwood_door_top", door_top)
    put("block/glimmerwood_door_bottom", door_bottom)
    put("item/glimmerwood_door", door_item)
    put("block/glimmerwood_trapdoor", glimmerwood_trapdoor())
    put("block/fae_mushroom_stem_side", fae_mushroom_stem_side())
    put("block/fae_mushroom_stem_top", fae_mushroom_stem_top())
    for kind in CAPS:
        put(f"block/{kind}_mushroom_cap_top", mushroom_cap_top(kind))
        put(f"block/{kind}_mushroom_cap_side", mushroom_cap_side(kind))
        put(f"block/{kind}_mushroom_cap_bottom", mushroom_cap_bottom(kind))
    put("block/fae_sprouts", fae_sprouts())
    bell, bell_glow = moonbell()
    put("block/moonbell", bell)
    put("block/moonbell_emissive", bell_glow)
    rose, rose_glow = fae_rose()
    put("block/fae_rose", rose)
    put("block/fae_rose_emissive", rose_glow)
    lace_bottom, lace_top, lace_glow = starlace()
    put("block/starlace_bottom", lace_bottom)
    put("block/starlace_top", lace_top)
    put("block/starlace_top_emissive", lace_glow)
    lantern, lantern_glow = lantern_mushroom()
    put("block/lantern_mushroom", lantern)
    put("block/lantern_mushroom_emissive", lantern_glow)
    vine_tip, vine_tip_glow = _vine(65001, True)
    vine_body, vine_body_glow = _vine(65001, False)
    put("block/glow_vines", vine_tip)
    put("block/glow_vines_emissive", vine_tip_glow)
    put("block/glow_vines_plant", vine_body)
    put("block/glow_vines_plant_emissive", vine_body_glow)
    pad, flower, flower_glow, lily_item = moon_lily()
    put("block/moon_lily", pad)
    put("block/moon_lily_flower", flower)
    put("block/moon_lily_flower_emissive", flower_glow)
    put("item/moon_lily", lily_item)
    put("block/thornbriar", thornbriar())
    put("block/thorn_hedge", thorn_hedge())
    put("block/fae_portal", fae_portal())
    ANIMATIONS["block/fae_portal"] = (32, 2, True)
    put("item/fae_dust", fae_dust())
    put("entity/projectiles/thorn", thorn_projectile())
    for frame in range(4):
        put(f"particle/fae_spark_{frame}", fae_spark(frame))
    # der Mondhain, der Kokon und die Feen-Elytren
    import fae_boss_textures
    fae_boss_textures.build(put, ANIMATIONS)
    # Mondstein (0.96): Block mit wanderndem Schimmer und der Stein
    import moonstone_textures
    moonstone_textures.build(put, ANIMATIONS)


if __name__ == "__main__":
    import sys
    out: dict[str, Image.Image] = {}
    build(lambda name, img: out.__setitem__(name, img))
    tiles = []
    for name, img in out.items():
        frame = img.crop((0, 0, N, N))
        bg = Image.new("RGBA", (N, N), (120, 96, 140, 255))
        bg.alpha_composite(frame)
        tiles.append((name, bg))
    cols = 10
    rows = (len(tiles) + cols - 1) // cols
    zoom = 8
    sheet = Image.new("RGBA", (cols * (N * zoom + 6), rows * (N * zoom + 6)), (40, 32, 48, 255))
    for i, (name, img) in enumerate(tiles):
        x = (i % cols) * (N * zoom + 6)
        y = (i // cols) * (N * zoom + 6)
        sheet.alpha_composite(img.resize((N * zoom, N * zoom), Image.NEAREST), (x, y))
    sheet.save(sys.argv[1] if len(sys.argv) > 1 else "fae_preview.png")
    print(f"{len(tiles)} Texturen")
