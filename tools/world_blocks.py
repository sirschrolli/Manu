#!/usr/bin/env python3
"""
Bausteine der Königreiche im Vanilla-Stil (siehe tools/vanilla_style.py): mittelalterliches Quadermauerwerk (auch
bemoost und gesprungen), Feldstein, behauener Sandstein, dunkle Steinziegel, Kalksteinziegel, verwitterte Ziegel,
Steinpfeiler und Putz.

Die Mauerwerks-Raster (welcher Stein wo liegt) kommen aus tools/block_textures.py; hier entsteht die Oberfläche:
Lichtkante oben/links, Schatten unten/rechts, Fugen, und in jedem Stein kurze Striche wie bei Vanilla-Stein.

Wird von tools/generate_textures.py aufgerufen: build(put).
"""
from __future__ import annotations

import math
import random
from typing import Callable

from PIL import Image

import block_textures as bt
import vanilla_style as vs
from pixelart import Color

N = 16

MEDIEVAL = vs.tones("#79736b", 7, spread=0.28, cool=230, warm=40, hue=6, sat=0.03)
DARK_BRICKS = vs.tones("#454852", 7, spread=0.34, cool=230, warm=220, hue=6, sat=0.03)
LIMESTONE = vs.tones("#cbc6b5", 7, spread=0.16, cool=220, warm=45, hue=6, sat=0.02)
WEATHERED = vs.tones("#6e7064", 7, spread=0.30, cool=200, warm=70, hue=6, sat=0.03)
SANDSTONE = vs.tones("#cbb084", 7, spread=0.18, cool=30, warm=48, hue=4, sat=-0.02)
PILLAR = vs.tones("#8f8a82", 7, spread=0.26, cool=230, warm=40, hue=6, sat=0.02)
FIELD = [vs.tones("#76726c", 6, spread=0.30), vs.tones("#7a6f62", 6, spread=0.30), vs.tones("#6f7377", 6, spread=0.30)]
FIELD_MORTAR = vs.tones("#4a4641", 2, spread=0.08)
MOSS = vs.tones("#5a7432", 4, spread=0.30, cool=150, warm=70, hue=8, sat=0.04)
LICHEN = vs.tones("#9a9d5e", 3, spread=0.18, cool=90, warm=60, hue=6, sat=0.02)
WHITE_PLASTER = vs.tones("#ddd6c6", 4, spread=0.06, cool=220, warm=45, hue=4, sat=0.01)
CLAY_PLASTER = vs.tones("#b9926a", 4, spread=0.08, cool=20, warm=40, hue=4, sat=0.02)


def _layout(ascii_map: str) -> list[str]:
    return bt.grid(ascii_map)


def _img(g: vs.Grid, pal: list[Color]) -> Image.Image:
    return vs.paint(g, pal)


# =============================================================================
# Mauerwerk
# =============================================================================

def medieval_stone() -> Image.Image:
    return _img(vs.masonry(_layout(bt.MEDIEVAL_LAYOUT), 41101), MEDIEVAL)


def cracked_stone_blocks() -> Image.Image:
    g = vs.masonry(_layout(bt.MEDIEVAL_LAYOUT), 41101)
    g = vs.crack(g, 41111, count=4, length=(4, 9), level=0, only=lambda v: v >= 2, edge=6)
    return _img(g, MEDIEVAL)


def mossy_wall_stones() -> Image.Image:
    """Mittelalterlicher Stein, Moos wächst aus den Fugen, hängt über die Kanten und bildet kleine Polster."""
    layout = _layout(bt.MEDIEVAL_LAYOUT)
    g = vs.masonry(layout, 41101)
    img = _img(g, MEDIEVAL)
    rnd = random.Random(41121)
    moss = vs.quantize(vs.layered(41122, ((4, 4, 0.6), (2, 2, 0.4))), (0.3, 0.3, 0.25, 0.15))
    cover = vs.layered(41123, ((8, 8, 0.6), (4, 4, 0.4)))
    for y in range(N):
        for x in range(N):
            joint = layout[y][x] == "."
            below_joint = layout[(y - 1) % N][x] == "."
            # Moos in den Fugen und in Flecken, die von den Fugen ausgehen
            if (joint and cover[y][x] > 0.35) or (below_joint and cover[y][x] > 0.5) or cover[y][x] > 0.78:
                img.putpixel((x, y), MOSS[min(3, moss[y][x] + (1 if not joint else 0))])
    # ein paar Moosfäden hängen über die Steinkanten
    for _ in range(4):
        x = rnd.randrange(N)
        for y in range(N):
            if layout[y][x] == "." and layout[(y + 1) % N][x] != ".":
                for d in range(1, rnd.randint(2, 3)):
                    img.putpixel((x, (y + d) % N), MOSS[1])
                break
    return img


def rough_fieldstone() -> Image.Image:
    """Grober Naturstein: runde Feldsteine in drei Farbtönen (grau, bräunlich, kühl) in dunklem Mörtel."""
    stones = bt.blob_layout(bt.FIELDSTONES)
    layout = ["".join("." if v < 0 else "ABCDEFGHIJKLMNOP"[v] for v in row) for row in stones]
    g = vs.masonry(layout, 41201, vary=0.25)
    rnd = random.Random(41202)
    family = [rnd.randrange(3) for _ in bt.FIELDSTONES]
    img = Image.new("RGBA", (N, N))
    for y in range(N):
        for x in range(N):
            v = g[y][x]
            if stones[y][x] < 0:
                img.putpixel((x, y), FIELD_MORTAR[min(1, v)])
            else:
                img.putpixel((x, y), FIELD[family[stones[y][x]]][max(0, min(5, v - 1))])
    return img


def hewn_sandstone() -> Image.Image:
    """Behauener Sandstein: große Quader mit flachen, waagerechten Schichtlinien in feinem Korn."""
    layout = _layout(bt.SANDSTONE_LAYOUT)
    g = vs.masonry(layout, 41301, vary=0.2)
    for y in range(N):
        for x in range(N):
            inner = layout[y][x] != "." and layout[(y - 1) % N][x] != "." and layout[(y + 1) % N][x] != "."
            if inner and 3 <= g[y][x] <= 5 and (y + (x // 6)) % 4 == 0:
                g[y][x] = max(3, g[y][x] - 1)
    return _img(g, SANDSTONE)


DARK_BRICK_ROWS = [(4, [7, 15]), (4, [3, 11]), (4, [7, 15]), (4, [3, 11])]


def dark_stone_bricks() -> Image.Image:
    return _img(vs.bricks(41401, DARK_BRICK_ROWS, chips=4), DARK_BRICKS)


LIMESTONE_ROWS = [(4, [9, 15]), (4, [2, 12]), (4, [7, 13]), (4, [4, 12])]


def limestone_bricks() -> Image.Image:
    return _img(vs.bricks(41501, LIMESTONE_ROWS, chips=2), LIMESTONE)


def weathered_stone_bricks() -> Image.Image:
    """Verwitterte Ziegel: ausgebrochene Ecken, ein paar Löcher, Flechten in kleinen Polstern."""
    layout = _layout(bt.WEATHERED_LAYOUT)
    g = vs.masonry(layout, 41601, rounded=True)
    g = vs.crack(g, 41602, count=2, length=(3, 5), level=1)
    img = _img(g, WEATHERED)
    rnd = random.Random(41603)
    for _ in range(4):
        x, y = rnd.randrange(N), rnd.randrange(N)
        if layout[y][x] == ".":
            continue
        for dx, dy, c in ((0, 0, 1), (1, 0, 2), (0, 1, 0), (1, 1, 1)):
            if layout[(y + dy) % N][(x + dx) % N] != ".":
                img.putpixel(((x + dx) % N, (y + dy) % N), LICHEN[c])
    return img


# =============================================================================
# Steinpfeiler
# =============================================================================

def stone_pillar_side() -> Image.Image:
    """Säulenschaft: drei Kanneluren (Rillen) im runden Schaft, links im Licht, rechts im Schatten, feine Striche."""
    profile = [3, 5, 5, 4, 2, 5, 5, 4, 4, 2, 4, 4, 3, 2, 3, 1]
    grain = vs.quantize(vs.strokes(41701, (1,), (1, 2, 3, 2, 1)), (0.2, 0.6, 0.2))
    g = vs.grid(0)
    for y in range(N):
        for x in range(N):
            v = profile[x]
            if v >= 3:
                v = max(3, min(6, v + grain[y][x] - 1))
            g[y][x] = v
    return _img(g, PILLAR)


def stone_pillar_top() -> Image.Image:
    """Säulenkopf: runde Säule mit Rille auf einer quadratischen Platte."""
    g = vs.polished(41711, shares=(0.25, 0.5, 0.25))
    for y in range(1, N - 1):
        for x in range(1, N - 1):
            d = math.hypot(x - 7.5, y - 7.5)
            if d <= 6.6:
                if d > 5.6:
                    g[y][x] = 5 if x + y < 15 else 2
                elif d > 4.6:
                    g[y][x] = 1
                elif d > 1.2:
                    g[y][x] = 4 if (x + y) % 7 else 3
                else:
                    g[y][x] = 5
    return _img(g, PILLAR)


# =============================================================================
# Putz
# =============================================================================

def _plaster(pal: list[Color], seed: int) -> Image.Image:
    """Glatter Kalkputz wie Vanilla-Terrakotta: ruhige, weiche Flecken in vier eng beieinander liegenden Tönen."""
    f = vs.mix_fields((vs.strokes(seed, (1, 2, 1), (1, 2, 1)), 0.5), (vs.layered(seed + 1, ((8, 8, 0.5), (4, 4, 0.5))), 0.5))
    g = vs.despeckle(vs.quantize(f, (0.12, 0.38, 0.36, 0.14)))
    return _img(g, pal)


def white_plaster() -> Image.Image:
    return _plaster(WHITE_PLASTER, 41801)


def clay_plaster() -> Image.Image:
    return _plaster(CLAY_PLASTER, 41811)


# =============================================================================
# Alles zusammen
# =============================================================================

def build(put: Callable[[str, Image.Image], None]) -> None:
    put("block/medieval_stone", medieval_stone())
    put("block/cracked_stone_blocks", cracked_stone_blocks())
    put("block/mossy_wall_stones", mossy_wall_stones())
    put("block/rough_fieldstone", rough_fieldstone())
    put("block/hewn_sandstone", hewn_sandstone())
    put("block/dark_stone_bricks", dark_stone_bricks())
    put("block/limestone_bricks", limestone_bricks())
    put("block/weathered_stone_bricks", weathered_stone_bricks())
    put("block/white_plaster", white_plaster())
    put("block/clay_plaster", clay_plaster())


if __name__ == "__main__":
    import sys

    sheet: dict[str, Image.Image] = {}
    build(lambda key, img: sheet.__setitem__(key.split("/")[-1], img))
    vs.preview(sheet, sys.argv[1] if len(sys.argv) > 1 else "world_blocks.png", zoom=6, tile=3, cols=4)
