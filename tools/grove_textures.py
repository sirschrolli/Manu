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
from typing import Callable

from PIL import Image

import pixelart as pa
from pixelart import hexc

N = 16

BARK = pa.palette("#1d1b15", "#2a271f", "#38342a", "#474236", "#575143", "#6a6350")
MOSS = pa.palette("#1b3615", "#26491b", "#335d21", "#417328", "#528a31", "#6aa53e")
LICHEN = pa.palette("#76866a", "#9aab84", "#c0cfa0")
GOLD = pa.palette("#8a6a1a", "#c09a2a", "#e8c84a")
GLOW = pa.palette("#1f8a6a", "#3ac090", "#7af0c4", "#c8fff0")
THORN = pa.palette("#1e140e", "#2e2016", "#40301e", "#56422a")



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
    put("block/glowcap", glowcap())
    put("block/bramble", bramble())
    put("block/gnarled_roots_side", gnarled_roots())
    put("block/gnarled_roots_top", gnarled_roots_top())
    put("block/grove_bell", grove_bell())
    put("block/life_blossom", life_blossom())
    put("entity/boss/projectile_seed", seed_projectile())
