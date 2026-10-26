#!/usr/bin/env python3
"""
Texturen des Uralten Hains (Biom um Verdantis' Nest).

  * block/ancient_log, ancient_log_top – graugrüne, tief gefurchte Borke mit Moos und Flechten; Schnittfläche mit Ringen
  * block/ancient_planks              – dunkle, olivbraune Bretter mit Astlöchern
  * block/ancient_leaves              – tiefgrünes Laub mit goldenen Blättchen (Lücken durchsichtig)
  * block/glowing_moss                – Leuchtmoos: dichtes Moos mit leuchtenden Sporen
  * block/glowcap                     – Leuchtkappe: Grüppchen leuchtender Pilze (Kreuzpflanze)
  * block/bramble                     – Dornengestrüpp: verschlungene, dornige Ranken mit Beeren (Kreuzpflanze)
  * block/gnarled_roots, gnarled_roots_top – knorrige Wurzeln, durch die man hindurchsieht
  * block/grove_bell                  – Hainglocke: hohe Blume mit blassblauen, leuchtenden Glocken

Wird von tools/generate_textures.py aufgerufen (build(put)).
"""
from __future__ import annotations

import math
import random
from typing import Callable

from PIL import Image

import pixelart as pa
from pixelart import hexc

N = 16

BARK = pa.palette("#1d1b15", "#2a271f", "#38342a", "#474236", "#575143", "#6a6350")
MOSS = pa.palette("#1b3615", "#26491b", "#335d21", "#417328", "#528a31", "#6aa53e")
LICHEN = pa.palette("#76866a", "#9aab84", "#c0cfa0")
WOOD = pa.palette("#2f2417", "#3d301f", "#4b3c27", "#5b4a31", "#6c593b", "#806b48")
LEAF = pa.palette("#0f2a12", "#163a19", "#1f4c22", "#295f2b", "#357436", "#468c42")
GOLD = pa.palette("#8a6a1a", "#c09a2a", "#e8c84a")
GLOW = pa.palette("#1f8a6a", "#3ac090", "#7af0c4", "#c8fff0")
THORN = pa.palette("#1e140e", "#2e2016", "#40301e", "#56422a")


def _wrap(v: int) -> int:
    return v % N


def ancient_log() -> Image.Image:
    """Borke: breite, senkrechte Platten mit tiefen Furchen, Moos in den Furchen, Flechtenflecken."""
    rng = random.Random(12101)
    noise = pa.fractal_noise(12102, ((8, 0.5), (4, 0.3), (2, 0.2)))
    img = pa.new()
    px = img.load()
    # Furchen: wandern leicht hin und her
    furrows = [1, 6, 11]
    for y in range(N):
        for x in range(N):
            d = min(min(abs(x - (f + (1 if (y // 4 + i) % 2 else 0))) for i, f in enumerate(furrows)), 3)
            if d == 0:
                c = BARK[0]
                if noise[y][x] > 0.55:
                    c = MOSS[1]
            elif d == 1:
                c = BARK[2] if noise[y][x] > 0.4 else BARK[1]
            else:
                v = 0.35 + 0.5 * noise[y][x]
                c = pa.pick_dither(BARK[2:], v, x, y, 0.6)
            px[x, y] = c
    # Querrisse in den Platten
    for _ in range(6):
        x, y = rng.randrange(N), rng.randrange(N)
        px[x, y] = BARK[1]
        px[_wrap(x + 1), y] = BARK[1]
    # Moospolster und Flechten
    for (x, y, r) in [(3, 2, 1.6), (13, 9, 1.8), (8, 14, 1.3)]:
        for yy in range(-2, 3):
            for xx in range(-2, 3):
                if xx * xx + yy * yy <= r * r:
                    px[_wrap(x + xx), _wrap(y + yy)] = MOSS[3 + (1 if (xx + yy) % 2 else 0) - (1 if yy > 0 else 0)]
    for (x, y) in [(9, 4), (4, 9), (14, 13), (10, 1)]:
        px[x, y] = LICHEN[1]
        px[_wrap(x + 1), y] = LICHEN[2] if (x + y) % 2 else LICHEN[0]
    return img


def ancient_log_top() -> Image.Image:
    """Schnittfläche: helle Ringe, dunkle Borke außen, Moos am Rand."""
    img = pa.new()
    px = img.load()
    for y in range(N):
        for x in range(N):
            dx, dy = x - 7.5, y - 7.5
            d = max(abs(dx), abs(dy)) * 0.55 + math.hypot(dx, dy) * 0.45
            if d > 6.9:
                px[x, y] = MOSS[2] if (x * 3 + y) % 5 == 0 else BARK[2 if (x + y) % 3 else 1]
            else:
                ring = int(d * 1.15) % 2
                px[x, y] = WOOD[4] if ring else WOOD[3]
                if d < 1.4:
                    px[x, y] = WOOD[5]
    for (x, y) in [(7, 2), (8, 3), (12, 7), (11, 8), (4, 11), (5, 10)]:
        px[x, y] = WOOD[1]
    return img


def ancient_planks() -> Image.Image:
    """Vier Bretter, olivbraun, mit Maserung und zwei Astlöchern."""
    noise = pa.fractal_noise(12201, ((8, 0.4), (4, 0.4), (2, 0.2)))
    img = pa.new()
    px = img.load()
    for y in range(N):
        board = y // 4
        offset = (0, 7, 3, 11)[board]
        for x in range(N):
            if y % 4 == 3:
                px[x, y] = WOOD[0]
                continue
            grain = math.sin((x + offset) * 0.9 + y * 0.3 + noise[y][x] * 2.0)
            v = 0.45 + 0.18 * grain + 0.2 * (noise[y][x] - 0.5) + (0.08 if y % 4 == 0 else 0)
            px[x, y] = pa.pick_dither(WOOD[1:], v, x, y, 0.5)
        # Brettfuge
        px[(offset + 5) % N, y] = WOOD[0] if y % 4 != 3 else WOOD[0]
    for (x, y) in [(4, 5), (12, 9)]:
        px[x, y] = WOOD[0]
        px[x + 1, y] = WOOD[1]
    return img


def ancient_leaves() -> Image.Image:
    """Dichtes, tiefgrünes Laub aus kleinen Blattgruppen, einzelne goldene Blätter, ein paar Lücken."""
    rng = random.Random(12301)
    cells, border, _ = pa.voronoi(12302, 14)
    img = pa.new()
    px = img.load()
    for y in range(N):
        for x in range(N):
            if border[y][x] < 0.55:
                px[x, y] = LEAF[0]
                continue
            v = 0.3 + 0.12 * (cells[y][x] % 4) + min(0.3, border[y][x] * 0.08)
            px[x, y] = pa.pick(LEAF[1:], v)
    for _ in range(9):
        x, y = rng.randrange(N), rng.randrange(N)
        px[x, y] = pa.CLEAR
    for (x, y) in [(3, 4), (11, 2), (13, 12), (6, 13)]:
        px[x, y] = GOLD[1]
        px[_wrap(x + 1), y] = GOLD[2]
    return img


def glowing_moss() -> Image.Image:
    """Leuchtmoos: weiches Moos mit leuchtenden Sporenpunkten (die Punkte leuchten im Dunkeln)."""
    noise = pa.fractal_noise(12401, ((8, 0.4), (4, 0.35), (2, 0.25)))
    img = pa.new()
    px = img.load()
    for y in range(N):
        for x in range(N):
            v = 0.25 + 0.65 * noise[y][x]
            px[x, y] = pa.pick_dither(MOSS, v, x, y, 0.8)
    for (x, y) in [(2, 3), (9, 1), (13, 6), (5, 9), (11, 12), (1, 14), (7, 6), (14, 15)]:
        px[x, y] = GLOW[3]
        for (dx, dy) in ((1, 0), (0, 1), (-1, 0), (0, -1)):
            if (x + dx + y + dy) % 2 == 0:
                px[_wrap(x + dx), _wrap(y + dy)] = GLOW[1]
    return img


GLOWCAP = [
    "",
    "",
    "",
    "..........oCCo..",
    ".........oCwcCo.",
    "...oCCo..ocCCco.",
    "..oCwcCo...ss...",
    "..occCCo...ss...",
    "....ss.....ss...",
    "....ss...oCo.s..",
    "....ss..oCwCos..",
    "....ss...ss.ss..",
    "....ss...ss.ss..",
    "...gssg.gssggs..",
    "..gGGGggGGGGGg..",
    "................",
]


def glowcap() -> Image.Image:
    return pa.sprite(GLOWCAP, {"o": GLOW[0], "C": GLOW[1], "c": GLOW[2], "w": GLOW[3], "s": hexc("#c8d8c0"),
                               "g": MOSS[3], "G": MOSS[2]})


BRAMBLE = [
    "................",
    "...t......t.....",
    "..tbt..r.tbt....",
    "...b..rRr.b..t..",
    "...bb..r..b.tbt.",
    "..t.b....bb..b..",
    ".tbtbb..bb..bb..",
    "..t..bbbb..bb.t.",
    "...r..bb..bb.tbt",
    "..rRr.bb.bb...b.",
    "...r..bbbb...bb.",
    ".t.....bb...bb..",
    "tbt...bbbbbbb...",
    ".bbb.bb.bb......",
    "...bbb...bb.....",
    "....bb....b.....",
]


def bramble() -> Image.Image:
    return pa.sprite(BRAMBLE, {"b": THORN[2], "t": THORN[3], "r": hexc("#8a1a2a"), "R": hexc("#d0405a")})


def gnarled_roots() -> Image.Image:
    """Verschlungene, dicke Wurzeln mit Lücken (durchsichtig), etwas Moos und Erde darin."""
    img = pa.new()
    px = img.load()
    noise = pa.value_noise(12501, 4)
    strands = [
        lambda t: (t, 3 + 2.0 * math.sin(t * 0.5)),
        lambda t: (t, 11 + 1.6 * math.sin(t * 0.45 + 2.0)),
        lambda t: (4 + 2.2 * math.sin(t * 0.5 + 1.0), t),
        lambda t: (12 + 1.8 * math.sin(t * 0.4 + 3.0), t),
    ]
    for k, f in enumerate(strands):
        for i in range(N * 4):
            t = i / 4.0
            x, y = f(t)
            for w in (-1, 0, 1):
                if k < 2:
                    xx, yy = int(x) % N, int(round(y + w)) % N
                else:
                    xx, yy = int(round(x + w)) % N, int(y) % N
                shade = 4 if w == -1 else (2 if w == 1 else 3)
                if noise[yy][xx] > 0.72 and w == -1:
                    px[xx, yy] = MOSS[3]
                else:
                    px[xx, yy] = BARK[shade]
    return img


def gnarled_roots_top() -> Image.Image:
    img = gnarled_roots().rotate(90)
    return img


GROVE_BELL = [
    "........l.......",
    ".......lGl......",
    "......bBGl......",
    ".....bwbBG.l....",
    ".....bBbBGlG....",
    "......b.bG..bB..",
    "........G..bwbB.",
    "...bB...G..bBbB.",
    "..bwbB..G...b.b.",
    "..bBbB..G..G....",
    "...b.b..G.G.....",
    ".....lG.GG......",
    "......lGG.......",
    ".......G........",
    ".......G........",
    ".......g........",
]


def grove_bell() -> Image.Image:
    return pa.sprite(GROVE_BELL, {"G": hexc("#3e7a2e"), "g": hexc("#2a5420"), "l": hexc("#5a9a3a"),
                                  "b": hexc("#8ab4f0"), "B": hexc("#5a7ed0"), "w": hexc("#e8f4ff")})


LIFE_BLOSSOM = [
    "",
    "",
    "......p.p.......",
    ".....pPwPp......",
    "....pPwYwPp.....",
    "...pPwYyYwPp....",
    "....pPwYwPp.....",
    ".....pPwPp......",
    "......pGp.......",
    ".......G..lL....",
    "...Ll..G.lL.....",
    "....lL.GlL......",
    ".....lLGG.......",
    ".......G........",
    ".......G........",
    ".......g........",
]


def life_blossom() -> Image.Image:
    return pa.sprite(LIFE_BLOSSOM, {"G": hexc("#3e7a2e"), "g": hexc("#2a5420"), "l": hexc("#4a8a34"), "L": hexc("#6aaa44"),
                                    "p": hexc("#c4407a"), "P": hexc("#f07ab0"), "w": hexc("#ffd8ec"), "Y": hexc("#fff0a0"),
                                    "y": hexc("#ffffff")})


def seed_projectile() -> Image.Image:
    """Dornensaat: stachelige, grünbraune Samenkapsel mit leuchtendem Kern."""
    img = pa.new()
    px = img.load()
    core, mid, rim, spike = hexc("#d8ff8a"), hexc("#5a8a2a"), hexc("#2e3a14"), hexc("#a08a5a")
    for y in range(N):
        for x in range(N):
            dx, dy = (x - 7.5) / 7.5, (y - 7.5) / 7.5
            d = math.hypot(dx, dy)
            a = math.atan2(dy, dx)
            limit = 0.5 + 0.38 * max(0.0, math.cos(a * 7)) ** 8
            if d > limit:
                continue
            if d > 0.5:
                px[x, y] = spike
            elif d > 0.38:
                px[x, y] = rim
            elif d > 0.16:
                px[x, y] = mid if (x + y) % 3 else rim
            else:
                px[x, y] = core
    return img


def build(put: Callable[[str, Image.Image], None]) -> None:
    put("block/ancient_log", ancient_log())
    put("block/ancient_log_top", ancient_log_top())
    put("block/ancient_planks", ancient_planks())
    put("block/ancient_leaves", ancient_leaves())
    put("block/glowing_moss", glowing_moss())
    put("block/glowcap", glowcap())
    put("block/bramble", bramble())
    put("block/gnarled_roots", gnarled_roots())
    put("block/gnarled_roots_top", gnarled_roots_top())
    put("block/grove_bell", grove_bell())
    put("block/life_blossom", life_blossom())
    put("entity/boss/projectile_seed", seed_projectile())
