#!/usr/bin/env python3
"""
Blöcke der Drachengebiete im Vanilla-Stil (siehe tools/vanilla_style.py):

  * Glutland:   Asche, Schlackenerde, Vulkangestein (auch glühend), verkohlter Stamm
  * Eisland:    Raureifstein, gefrorene Erde
  * Urhain:     Urholz (Stamm, Bretter), Urlaub, Leuchtmoos
  * Sturmland:  Fulgurit
  * Tiefsee:    Abgrundstein, Abgrundsand
  (die Erz-Einschlüsse kommen aus tools/ores_art.py)

Wird von tools/generate_textures.py aufgerufen: build(put).
"""
from __future__ import annotations

import math
import random
from typing import Callable

from PIL import Image

import vanilla_style as vs
from pixelart import Color, hexc

N = 16

ASH = vs.tones("#7b7774", 4, spread=0.12, cool=230, warm=30, hue=4, sat=0.02)
CINDER = vs.tones("#4b3b35", 5, spread=0.28, cool=0, warm=25, hue=6, sat=0.04)
CINDER_PEBBLE = vs.tones("#2b2628", 2, spread=0.12)
EMBER = [hexc("#b43a10"), hexc("#ff8a2a")]
BASALT = vs.tones("#3f3a3d", 4, spread=0.26, cool=250, warm=20, hue=6, sat=0.03)
LAVA = [hexc("#7a1e08"), hexc("#d24a10"), hexc("#ff9a2a"), hexc("#ffe08a")]
CHAR_BARK = vs.tones("#2e2728", 6, spread=0.40, cool=250, warm=20, hue=6, sat=0.03)
CHAR_WOOD = vs.tones("#3a2e2a", 6, spread=0.30)
RIME = vs.tones("#b9c4cf", 4, spread=0.12, cool=215, warm=200, hue=4, sat=0.02)
FROST = [hexc("#e7f2fb"), hexc("#ffffff")]
FROZEN_SOIL = vs.tones("#5c524c", 5, spread=0.26, cool=220, warm=30, hue=6, sat=0.03)
FROZEN_PEBBLE = vs.tones("#7c8288", 2, spread=0.1)
SNOW = vs.tones("#e8f0f6", 3, spread=0.04, cool=210, warm=200, hue=4, sat=0.01)
ANCIENT_BARK = vs.tones("#4e3a26", 6, spread=0.40, cool=20, warm=40, hue=6, sat=0.04)
ANCIENT_WOOD = vs.tones("#8a6740", 6, spread=0.28, cool=25, warm=45, hue=6, sat=0.04)
ANCIENT_PLANKS = vs.tones("#7a5836", 7, spread=0.32, cool=20, warm=40, hue=6, sat=0.04)
MOSS_GREEN = vs.tones("#4f6e2c", 4, spread=0.30, cool=150, warm=70, hue=8, sat=0.04)
LEAF_GREEN = vs.tones("#3f6b2a", 4, spread=0.42, cool=150, warm=75, hue=10, sat=0.04)
GLOW_MOSS = vs.tones("#3f7a3a", 6, spread=0.30, cool=150, warm=90, hue=8, sat=0.04)
GLOW_TIPS = [hexc("#8af0b0"), hexc("#e0fff0")]
FULGURITE = vs.tones("#9a9a86", 5, spread=0.26, cool=200, warm=60, hue=6, sat=0.03)
STORM = vs.tones("#3a4252", 4, spread=0.26, cool=225, warm=200, hue=6, sat=0.03)
ABYSS_STONE = vs.tones("#2e4547", 4, spread=0.24, cool=210, warm=170, hue=6, sat=0.03)
ABYSS_SAND = vs.tones("#4c5f5c", 5, spread=0.18, cool=200, warm=160, hue=4, sat=0.02)


def _img(g: vs.Grid, pal: list[Color]) -> Image.Image:
    return vs.paint(g, pal)


def _fringe(seed: int, deep: float = 0.45) -> list[int]:
    rnd = random.Random(seed)
    depth = [2 + (1 if rnd.random() < deep else 0) + (1 if rnd.random() < 0.18 else 0) for _ in range(N)]
    for x in range(N):
        if depth[x] >= 4 and depth[(x - 1) % N] <= 2 and depth[(x + 1) % N] <= 2:
            depth[x] = 3
    return depth


# =============================================================================
# Glutland
# =============================================================================

def ash_block() -> Image.Image:
    """Asche: feines, graues Korn (wie Vanilla-Sand, nur grau) mit ein paar dunklen Schlackenkrümeln."""
    g = vs.sand(51001)
    img = _img(g, ASH)
    rnd = random.Random(51002)
    for _ in range(5):
        x, y = rnd.randrange(N), rnd.randrange(N)
        img.putpixel((x, y), CINDER_PEBBLE[0])
        if rnd.random() < 0.5:
            img.putpixel(((x + 1) % N, y), CINDER_PEBBLE[1])
    return img


def cinder_soil_bottom() -> Image.Image:
    return _img(vs.soil(51101, pebbles=5), CINDER + CINDER_PEBBLE)


def cinder_soil_top() -> Image.Image:
    """Schlackenerde von oben: Asche, in der hier und da Glut glimmt."""
    img = ash_block()
    rnd = random.Random(51111)
    for _ in range(3):
        x, y = rnd.randrange(N), rnd.randrange(N)
        img.putpixel((x, y), EMBER[1])
        img.putpixel(((x + 1) % N, y), EMBER[0])
    return img


def cinder_soil_side() -> Image.Image:
    img = cinder_soil_bottom()
    top = vs.sand(51001)
    for x, d in enumerate(_fringe(51121)):
        for y in range(d):
            img.putpixel((x, y), ASH[min(3, top[y][x])] if y < d - 1 else ASH[0])
    return img


def volcanic_rock() -> Image.Image:
    """Vulkangestein: dunkler Basalt aus kurzen Strichen, mit ein paar Poren."""
    g = vs.stone(51201, shares=(0.16, 0.34, 0.34, 0.16))
    rnd = random.Random(51202)
    for _ in range(6):
        x, y = rnd.randrange(N), rnd.randrange(N)
        g[y][x] = 0
    return _img(g, BASALT)


def _lava_cracks(seed: int, cells: int = 7) -> vs.Grid:
    """
    Rissnetz wie bei Vanilla-Magma: die Grenzen unregelmäßiger Schollen, ein Pixel breit, hier und da unterbrochen;
    wo Risse zusammenlaufen, ist es am heißesten (1 = Riss, 2 = heiß).
    """
    rnd = random.Random(seed)
    pts = [(rnd.uniform(0, N), rnd.uniform(0, N)) for _ in range(cells)]

    def nearest(x: float, y: float) -> int:
        best, idx = 1e9, 0
        for i, (px, py) in enumerate(pts):
            dx = min(abs(x - px), N - abs(x - px))
            dy = min(abs(y - py), N - abs(y - py))
            d = dx * dx + dy * dy
            if d < best:
                best, idx = d, i
        return idx

    own = [[nearest(x + 0.5, y + 0.5) for x in range(N)] for y in range(N)]
    g = vs.grid(0)
    for y in range(N):
        for x in range(N):
            if (own[y][(x + 1) % N] != own[y][x] or own[(y + 1) % N][x] != own[y][x]) and rnd.random() < 0.9:
                g[y][x] = 1
    for y in range(N):
        for x in range(N):
            if g[y][x] and sum(g[(y + dy) % N][(x + dx) % N] > 0 for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1))) >= 3:
                g[y][x] = 2
    return g


def glowing_volcanic_rock() -> tuple[Image.Image, Image.Image]:
    """Glühendes Vulkangestein: Basalt, durchzogen von einem Netz glühender Risse (wie Vanilla-Magma)."""
    img = volcanic_rock()
    glow = Image.new("RGBA", (N, N), (0, 0, 0, 0))
    cracks = _lava_cracks(51301)
    for y in range(N):
        for x in range(N):
            v = cracks[y][x]
            if v:
                c = LAVA[3] if v == 2 else LAVA[1] if (x + y) % 3 else LAVA[2]
                img.putpixel((x, y), c)
                glow.putpixel((x, y), c)
    return img, glow


def charred_log_side() -> Image.Image:
    """Verkohlter Stamm: schwarze, rissige Borke mit wenigen glimmenden Fasern."""
    img = _img(vs.bark(51401, grooves=6), CHAR_BARK)
    rnd = random.Random(51402)
    for _ in range(3):
        x, y = rnd.randrange(N), rnd.randrange(N)
        img.putpixel((x, y), EMBER[0])
        img.putpixel((x, (y + 1) % N), EMBER[1] if rnd.random() < 0.4 else EMBER[0])
    return img


def charred_log_top() -> Image.Image:
    g = vs.log_top(51411)
    return _img(g, [CHAR_BARK[0], CHAR_BARK[1], CHAR_WOOD[1], CHAR_WOOD[2], CHAR_WOOD[3], CHAR_WOOD[4]])


# =============================================================================
# Eisland
# =============================================================================

def rime_stone() -> Image.Image:
    """Raureifstein: hellgrauer, bläulicher Fels, auf dem feine Reifkristalle in Strichen sitzen."""
    g = vs.stone(52001, shares=(0.12, 0.33, 0.37, 0.18))
    img = _img(g, RIME)
    rnd = random.Random(52002)
    for _ in range(6):
        x, y = rnd.randrange(N), rnd.randrange(N)
        for i in range(rnd.randint(2, 3)):
            img.putpixel(((x + i) % N, y), FROST[0] if i else FROST[1])
    return img


def frozen_soil_bottom() -> Image.Image:
    return _img(vs.soil(52101, pebbles=5), FROZEN_SOIL + FROZEN_PEBBLE)


def frozen_soil_top() -> Image.Image:
    """Gefrorene Erde von oben: verharschter Schnee mit ein paar dunklen Erdkrümeln, die durchschauen."""
    f = vs.layered(52111, ((4, 4, 0.5), (2, 2, 0.5)))
    g = vs.quantize(f, (0.25, 0.5, 0.25))
    img = _img(g, SNOW)
    rnd = random.Random(52112)
    for _ in range(4):
        x, y = rnd.randrange(N), rnd.randrange(N)
        img.putpixel((x, y), FROZEN_SOIL[2])
        if rnd.random() < 0.5:
            img.putpixel(((x + 1) % N, y), FROZEN_SOIL[3])
    return img


def frozen_soil_side() -> Image.Image:
    img = frozen_soil_bottom()
    top = vs.quantize(vs.layered(52111, ((4, 4, 0.5), (2, 2, 0.5))), (0.25, 0.5, 0.25))
    for x, d in enumerate(_fringe(52121, 0.5)):
        for y in range(d):
            img.putpixel((x, y), SNOW[top[y][x]] if y < d - 1 else SNOW[0])
    return img


# =============================================================================
# Urhain
# =============================================================================

def ancient_log() -> Image.Image:
    """Urholz-Stamm: tief gefurchte braune Borke, in den Furchen hier und da Moos."""
    g = vs.bark(53001, grooves=6)
    img = _img(g, ANCIENT_BARK)
    moss = vs.layered(53002, ((4, 8, 0.6), (2, 4, 0.4)))
    for y in range(N):
        for x in range(N):
            if g[y][x] <= 1 and moss[y][x] > 0.6:
                img.putpixel((x, y), MOSS_GREEN[1 if g[y][x] == 0 else 2])
            elif moss[y][x] > 0.82 and g[y][x] >= 3:
                img.putpixel((x, y), MOSS_GREEN[2])
    return img


def ancient_log_top() -> Image.Image:
    g = vs.log_top(53011)
    return _img(g, [ANCIENT_BARK[0], ANCIENT_BARK[2], ANCIENT_WOOD[1], ANCIENT_WOOD[2], ANCIENT_WOOD[3], ANCIENT_WOOD[4]])


def ancient_planks() -> Image.Image:
    return _img(vs.planks(53021, joints=(None, 6, 11, 2)), ANCIENT_PLANKS)


def ancient_leaves() -> Image.Image:
    """Urlaub: dichtes, tiefgrünes Laub mit hellen Blattspitzen."""
    return vs.paint(vs.leaves(53031, holes=0.24), LEAF_GREEN)


def glowing_moss() -> tuple[Image.Image, Image.Image]:
    """Leuchtmoos: weiche grüne Moospolster, dazwischen einzelne leuchtende Sporen."""
    f = vs.mix_fields((vs.strokes(53041, (1, 2, 1), (1, 2, 1)), 0.6), (vs.layered(53042, ((4, 4, 0.6), (8, 8, 0.4))), 0.4))
    g = vs.despeckle(vs.quantize(f, (0.1, 0.2, 0.28, 0.24, 0.12, 0.06)), passes=1)
    img = _img(g, GLOW_MOSS)
    glow = Image.new("RGBA", (N, N), (0, 0, 0, 0))
    rnd = random.Random(53043)
    spots = 0
    while spots < 8:
        x, y = rnd.randrange(N), rnd.randrange(N)
        if g[y][x] < 3 or glow.getpixel((x, y))[3]:
            continue
        c = GLOW_TIPS[1] if spots % 3 == 0 else GLOW_TIPS[0]
        img.putpixel((x, y), c)
        glow.putpixel((x, y), c)
        spots += 1
    return img, glow


# =============================================================================
# Sturmland und Tiefsee
# =============================================================================

def fulgurite() -> Image.Image:
    """Fulgurit: vom Blitz verglaster Sand – helles, körniges Gestein mit dunklen, verglasten Poren und Rinnen."""
    g = vs.stone(54001, shares=(0.1, 0.3, 0.38, 0.22))
    g = [[v + 1 for v in row] for row in g]
    rnd = random.Random(54002)
    for _ in range(5):
        x, y = rnd.randrange(N), rnd.randrange(N)
        g[y][x] = 0
        g[(y + 1) % N][x] = 0 if rnd.random() < 0.6 else 1
    return _img(g, FULGURITE)


def fulgurite_top() -> Image.Image:
    """Fulgurit von oben: eine glasige Röhre (dunkles Loch mit hellem, glänzendem Rand) im körnigen Stein."""
    img = fulgurite()
    for y in range(N):
        for x in range(N):
            d = math.hypot(x - 7.5, y - 7.5)
            if d < 2.2:
                img.putpixel((x, y), FULGURITE[0])
            elif d < 3.2:
                light = (x - 7.5) + (y - 7.5) > 0
                img.putpixel((x, y), FULGURITE[4] if light else FULGURITE[1])
    return img


def stormstone() -> Image.Image:
    """Sturmstein: dunkler, blaugrauer Fels mit schräg verlaufenden Strichen (vom Wind geschliffen)."""
    rnd = random.Random(54101)
    f = vs.mix_fields((vs.strokes(54102, (1, 2, 3, 2, 1)), 0.6), (vs.layered(54103, ((8, 4, 0.6), (4, 2, 0.4))), 0.4))
    g = vs.quantize(f, (0.15, 0.33, 0.35, 0.17))
    # Striche leicht schräg: jede Zeile um ein Viertel der Zeilennummer verschoben
    g = [[g[y][(x + y // 4) % N] for x in range(N)] for y in range(N)]
    g = vs.despeckle(vs.streaks(g, rnd, 0.5))
    return _img(g, STORM)


def abyssal_stone() -> Image.Image:
    return _img(vs.stone(55001, shares=(0.15, 0.33, 0.35, 0.17)), ABYSS_STONE)


def abyssal_sand() -> Image.Image:
    return _img(vs.sand(55011), ABYSS_SAND[:4])


# =============================================================================
# Alles zusammen
# =============================================================================

def build(put: Callable[[str, Image.Image], None]) -> None:
    put("block/ash_block", ash_block())
    put("block/cinder_soil_top", cinder_soil_top())
    put("block/cinder_soil_side", cinder_soil_side())
    put("block/cinder_soil_bottom", cinder_soil_bottom())
    put("block/volcanic_rock", volcanic_rock())
    rock, rock_glow = glowing_volcanic_rock()
    put("block/glowing_volcanic_rock", rock)
    put("block/glowing_volcanic_rock_glow", rock_glow)
    put("block/charred_log_side", charred_log_side())
    put("block/charred_log_top", charred_log_top())
    put("block/rime_stone", rime_stone())
    put("block/frozen_soil_top", frozen_soil_top())
    put("block/frozen_soil_side", frozen_soil_side())
    put("block/frozen_soil_bottom", frozen_soil_bottom())
    put("block/ancient_log", ancient_log())
    put("block/ancient_log_top", ancient_log_top())
    put("block/ancient_planks", ancient_planks())
    put("block/ancient_leaves", ancient_leaves())
    put("block/glowing_moss", glowing_moss()[0])
    put("block/stormstone", stormstone())
    put("block/fulgurite", fulgurite())
    put("block/fulgurite_top", fulgurite_top())
    put("block/abyssal_stone", abyssal_stone())
    put("block/abyssal_sand", abyssal_sand())


if __name__ == "__main__":
    import sys

    sheet: dict[str, Image.Image] = {}
    build(lambda key, img: sheet.__setitem__(key.split("/")[-1], img))
    vs.preview(sheet, sys.argv[1] if len(sys.argv) > 1 else "region_blocks.png", zoom=5, tile=3, cols=6)
