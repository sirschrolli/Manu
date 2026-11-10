#!/usr/bin/env python3
"""
Texturen des Abgrunds (im Vanilla-Stil, siehe tools/vanilla_style.py):

  * Chaosgestein – ein dunkles Trümmergestein, in dem Splitter aller Welten zusammengebacken sind (Netherrack, Endstein,
    Sternenmarmor, Kristall, Schattenstein): mit Ziegeln, poliert, rissig (aus den Rissen glüht es rot) und gemeißelt (ein
    Auge, durch das ein Riss läuft – es glimmt)
  * Weltennaht – eine zackige Fuge, die weißglühend durch dunklen Fels läuft, rot gesäumt (leuchtend)
  * Schwebekristall (eisblau) und Hebekristall (rosenrot) – Drusen mit leuchtenden Spitzen
  * der Abgrundriss (bewegt: ein roter Wirbel um einen schwarzen Kern, in den Splitter stürzen)
  * Gegenstand: der Rissschlüssel

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

STONE = vs.tones("#2a2228", 4, spread=0.22, cool=300, warm=350, hue=8, sat=0.05)
BRICK = vs.tones("#2e252c", 7, spread=0.40, cool=300, warm=350, hue=10, sat=0.06)
POLISHED = vs.tones("#2c242a", 6, spread=0.34, cool=300, warm=350, hue=8, sat=0.05)
# Splitter fremder Welten im Chaosgestein (je zwei Töne: dunkel, hell)
CHIPS = [
    (hexc("#6e2422"), hexc("#a83a30")),     # Netherrack
    (hexc("#b8b07a"), hexc("#e2dca2")),     # Endstein
    (hexc("#7a5aa8"), hexc("#b48cff")),     # Sternenmarmor / Leere
    (hexc("#2a5a64"), hexc("#5ab4c0")),     # Kristall
    (hexc("#3a4458"), hexc("#5a6884")),     # Schattenstein
    (hexc("#4a5a2a"), hexc("#6e8a3a")),     # Moos der Oberwelt
]
EMBER = [hexc("#5a0a0a"), hexc("#a8180e"), hexc("#e8481a"), hexc("#ff9a3a"), hexc("#ffe6a8"), hexc("#ffffff")]
ICE = [hexc("#1e3a6a"), hexc("#3a6ab8"), hexc("#7ab4f0"), hexc("#c4e6ff"), hexc("#f2fbff")]
ROSE = [hexc("#4a0e2a"), hexc("#8a1e4a"), hexc("#d03a6a"), hexc("#ff86a8"), hexc("#ffe0ea")]
DARK = hexc("#0c080c")


def _img(g: vs.Grid, pal: list[Color]) -> Image.Image:
    return vs.paint(g, pal)


def _chips(img: Image.Image, seed: int, count: int, mask: Callable[[int, int], bool] | None = None) -> Image.Image:
    """Eingebackene Splitter: kleine, kantige Brocken (1–3 Pixel) in den Farben anderer Welten, oben links heller."""
    rnd = random.Random(seed)
    px = img.load()
    for _ in range(count):
        x0, y0 = rnd.randrange(N), rnd.randrange(N)
        dark, light = rnd.choice(CHIPS)
        shape = rnd.choice([[(0, 0)], [(0, 0), (1, 0)], [(0, 0), (0, 1)], [(0, 0), (1, 0), (0, 1)], [(0, 0), (1, 0), (1, 1)],
                            [(0, 0), (1, 0), (0, 1), (1, 1)]])
        for (dx, dy) in shape:
            x, y = (x0 + dx) % N, (y0 + dy) % N
            if mask is not None and not mask(x, y):
                continue
            edge = (dx, dy) == (0, 0)
            px[x, y] = pa.mix(px[x, y], light if edge else dark, 0.85)
    return img


# =============================================================================
# Chaosgestein
# =============================================================================

BRICK_ROWS = [(4, [2, 10]), (4, [6, 14]), (4, [0, 9]), (4, [4, 12])]


def chaos_stone() -> Image.Image:
    return _chips(_img(vs.stone(71101, grain=0.8), STONE), 71102, 9)


def chaos_stone_bricks() -> Image.Image:
    g = vs.bricks(71201, BRICK_ROWS)
    # nur auf den Steinen (nicht in den Fugen)
    return _chips(_img(g, BRICK), 71202, 6, mask=lambda x, y: g[y][x] >= 2)


def cracked_chaos_stone_bricks() -> Image.Image:
    g = vs.bricks(71201, BRICK_ROWS, chips=6)
    g = vs.crack(g, 71211, count=4, length=(4, 8), level=0, only=lambda v: v >= 2)
    img = _chips(_img(g, BRICK), 71202, 5, mask=lambda x, y: g[y][x] >= 2)
    # aus den Rissen glüht es rot
    px = img.load()
    rnd = random.Random(71212)
    for y in range(N):
        for x in range(N):
            if g[y][x] == 0 and rnd.random() < 0.45:
                px[x, y] = pa.mix(px[x, y], EMBER[1], 0.6)
    return img


def polished_chaos_stone() -> Image.Image:
    return _chips(_img(vs.polished(71301), POLISHED), 71302, 4)


EYE = [
    "...######...",
    "..##....##..",
    ".##..##..##.",
    "##..####..##",
    ".##..##..##.",
    "..##....##..",
    "...######...",
]


def chiseled_chaos_stone() -> tuple[Image.Image, Image.Image]:
    """Gemeißeltes Chaosgestein: in einem eingelassenen Feld ein Auge, durch das ein Riss läuft (glimmend)."""
    g = vs.polished(71401)
    for y in range(1, 15):
        for x in range(1, 15):
            g[y][x] = 2
    vs.bevel_frame(g, 1, 1, 14, 14, light=4, dark=0)
    raised = {(2 + x, 4 + y) for y, row in enumerate(EYE) for x, c in enumerate(row) if c == "#"}
    for (x, y) in raised:
        g[y][x] = 5 if (x, y - 1) not in raised or (x - 1, y) not in raised else 4
    img = _img(g, POLISHED)
    px = img.load()
    glow = pa.new()
    gp = glow.load()
    # der Riss: von oben links nach unten rechts quer durch das Auge, weißglühend in der Mitte
    crack = [(3, 2), (4, 3), (5, 4), (5, 5), (6, 6), (7, 7), (8, 7), (9, 8), (9, 9), (10, 10), (11, 11), (11, 12), (12, 13)]
    for i, (x, y) in enumerate(crack):
        hot = 1.0 - abs(i - len(crack) / 2) / (len(crack) / 2)
        col = pa.mix(EMBER[2], EMBER[4], hot)
        px[x, y] = col
        gp[x, y] = col
    for (x, y) in ((7, 7), (8, 7)):
        px[x, y] = EMBER[5]
        gp[x, y] = EMBER[5]
    return img, glow


# =============================================================================
# Weltennaht
# =============================================================================

def world_seam() -> tuple[Image.Image, Image.Image]:
    """
    Weltennaht: links eine Welt, rechts eine andere (oben dunkles Chaosgestein, unten fremder Fels mit anderer Farbe),
    dazwischen eine zackige Fuge, die weißglühend leuchtet und zu den Rändern hin rot wird.
    """
    img = _img(vs.stone(71501, grain=0.8), STONE)
    other = _img(vs.stone(71502, grain=0.7), vs.tones("#3a2e46", 4, spread=0.22, cool=270, warm=320, hue=10, sat=0.05))
    px, op = img.load(), other.load()
    glow = pa.new()
    gp = glow.load()
    rnd = random.Random(71503)
    # die Fuge: ein zackiger Pfad von oben nach unten (an den Rändern kachelbar: oben und unten bei x = 7)
    path = []
    x = 7.0
    for y in range(N):
        path.append(x)
        x += rnd.choice([-1, 0, 0, 1])
        x = max(4.0, min(11.0, x))
    path[-1] = 7.0
    path[-2] = (path[-3] + 7.0) / 2
    for y in range(N):
        c = path[y]
        for xx in range(N):
            d = xx - c
            if d > 1.5:
                px[xx, y] = op[xx, y]               # die andere Welt rechts der Fuge
            ad = abs(d)
            if ad < 0.6:
                col = EMBER[5] if (y % 5) else EMBER[4]
            elif ad < 1.6:
                col = EMBER[3]
            elif ad < 2.6:
                col = pa.mix(px[xx, y], EMBER[1], 0.75)
            else:
                continue
            px[xx, y] = col
            gp[xx, y] = col if ad < 1.6 else pa.mix((0, 0, 0, 255), col, 0.55)
    # Funken: einzelne Glutpunkte in der Nähe der Fuge
    for _ in range(6):
        y = rnd.randrange(N)
        xx = int(path[y]) + rnd.choice([-3, 3, -4, 4])
        if 0 <= xx < N:
            px[xx, y] = EMBER[3]
            gp[xx, y] = EMBER[3]
    return img, glow


# =============================================================================
# Die Kristalle der verdrehten Schwerkraft
# =============================================================================

CLUSTER = [
    "................",
    ".......a........",
    "......aba.......",
    "..a...abc...a...",
    ".aba..abc..aba..",
    ".abc..abcd.abc..",
    ".abcd.abcd.abcd.",
    "..bcd.abcd..bcd.",
    "..bcdaabcd..bcd.",
    "a.bcdabbcd.abcd.",
    "abccdabbcdaabcd.",
    "abcdcdabccdabcd.",
    ".bcdcdabccdbccd.",
    ".bcddabbccdbccd.",
    "..cddabbccddcd..",
    ".aabbbbcccddddd.",
]


def _crystal(pal: list[Color]) -> tuple[Image.Image, Image.Image]:
    colors = {"a": pal[4], "b": pal[3], "c": pal[2], "d": pal[1]}
    img = pa.sprite(CLUSTER, colors)
    glow = pa.new()
    gp, px = glow.load(), img.load()
    for y in range(N):
        for x in range(N):
            c = px[x, y]
            if c[3] and c in (pal[4], pal[3]):
                gp[x, y] = c
    # schwebende Splitterchen über den Spitzen
    for (x, y) in ((7, 1), (12, 3), (2, 3)):
        px[x, y] = hexc("#ffffff")
        gp[x, y] = hexc("#ffffff")
    for (x, y) in ((10, 0), (4, 1)):
        px[x, y] = pal[3]
        gp[x, y] = pal[3]
    return img, glow


def float_crystal() -> tuple[Image.Image, Image.Image]:
    return _crystal(ICE)


def lift_crystal() -> tuple[Image.Image, Image.Image]:
    return _crystal(ROSE)


# =============================================================================
# Der Abgrundriss
# =============================================================================

def abyss_rift(frames: int = 24) -> Image.Image:
    """
    Der Riss als Bildfolge: ein roter Wirbel um einen schwarzen Kern; helle Splitter stürzen spiralförmig hinein, die
    Ränder glühen orange. Leicht durchscheinend.
    """
    sheet = Image.new("RGBA", (N, N * frames))
    rnd = random.Random(72101)
    shards = [(rnd.uniform(0, 2 * math.pi), rnd.uniform(1.5, 11.0), rnd.choice([EMBER[4], EMBER[3], hexc("#ffffff"), hexc("#ffb0c8")]))
              for _ in range(22)]
    for f in range(frames):
        t = f / frames
        frame = Image.new("RGBA", (N, N))
        px = frame.load()
        for y in range(N):
            for x in range(N):
                dx, dy = x - 7.5, y - 7.5
                r = math.hypot(dx, dy)
                a = math.atan2(dy, dx)
                swirl = math.sin(a * 3 - r * 0.8 + t * 2 * math.pi) * 0.5 + 0.5
                swirl2 = math.sin(a * 2 + r * 0.5 - t * 2 * math.pi * 2) * 0.5 + 0.5
                v = 0.2 + 0.5 * swirl + 0.2 * swirl2
                v *= 0.35 + 0.65 * min(1.0, r / 8.0)
                c = pa.mix(hexc("#080204"), hexc("#8a0e1a"), v)
                c = pa.mix(c, hexc("#ff6a2a"), max(0.0, swirl2 - 0.7) * 0.9)
                px[x, y] = (c[0], c[1], c[2], 220)
        for (a0, r0, col) in shards:
            r = (r0 - t * 9.0) % 11.0 + 0.8
            a = a0 - t * 2 * math.pi * 0.5 - 2.0 / (r + 0.5)
            x = int(round(7.5 + math.cos(a) * r))
            y = int(round(7.5 + math.sin(a) * r))
            if 0 <= x < N and 0 <= y < N:
                b = min(1.0, r / 4.0)
                px[x, y] = pa.mix(px[x, y], col, 0.9 * b)[:3] + (240,)
        sheet.paste(frame, (0, f * N))
    return sheet


# =============================================================================
# Gegenstand: der Rissschlüssel
# =============================================================================

KEY = [
    "................",
    "..........aab...",
    ".........abccb..",
    "........abcddcb.",
    "........acdeedc.",
    "........bcdeedc.",
    ".........bcddc..",
    ".......ffgbccb..",
    "......ffg..bb...",
    ".....ffg........",
    "....ffg.........",
    "...ffg..........",
    "..ffg.h.........",
    ".ffgfh..........",
    ".fgff...........",
    "..fh............",
]


def rift_key() -> Image.Image:
    """
    Der Rissschlüssel: ein Bart aus schwarzem, violett geädertem Metall, der Griff ein Ring aus Kronensplittern um einen
    glühenden roten Riss.
    """
    colors = {
        "a": hexc("#3a2a52"), "b": hexc("#5a4480"), "c": hexc("#8a6ac0"),     # Ring aus Kronensplittern (violett)
        "d": EMBER[2], "e": EMBER[4],                                          # der glühende Riss in der Mitte
        "f": hexc("#2a2232"), "g": hexc("#5a4a6a"), "h": hexc("#8a6ac0"),     # Schaft und Bart (dunkles Metall)
    }
    img = pa.sprite(KEY, colors)
    img = pa.outline(img, lambda c: pa.mix(c, hexc("#08040a"), 0.75))
    px = img.load()
    px[12, 4] = hexc("#ffffff")
    return img


def sky_rift() -> tuple[Image.Image, Image.Image]:
    """Himmelsriss: weißglühendes, zuckendes Licht mit roten und orangen Adern (Grundbild und Leuchtebene gleich)."""
    img = Image.new("RGBA", (N, N))
    px = img.load()
    rnd = random.Random(72201)
    field = vs.layered(72202, [(4, 4, 0.6), (8, 8, 0.4)])
    for y in range(N):
        for x in range(N):
            v = field[y][x]
            if v > 0.62:
                c = EMBER[5]
            elif v > 0.5:
                c = EMBER[4]
            elif v > 0.38:
                c = EMBER[3]
            elif v > 0.26:
                c = EMBER[2]
            else:
                c = EMBER[1]
            if rnd.random() < 0.06:
                c = hexc("#ffffff")
            px[x, y] = c
    return img, img.copy()


ANIMATIONS = {"block/abyss_rift": (24, 2, True)}


def build(put: Callable[[str, Image.Image], None]) -> None:
    put("block/chaos_stone", chaos_stone())
    put("block/chaos_stone_bricks", chaos_stone_bricks())
    put("block/cracked_chaos_stone_bricks", cracked_chaos_stone_bricks())
    put("block/polished_chaos_stone", polished_chaos_stone())
    chiseled, chiseled_glow = chiseled_chaos_stone()
    put("block/chiseled_chaos_stone", chiseled)
    put("block/chiseled_chaos_stone_glow", chiseled_glow)
    seam, seam_glow = world_seam()
    put("block/world_seam", seam)
    put("block/world_seam_glow", seam_glow)
    for name, fn in (("float_crystal", float_crystal), ("lift_crystal", lift_crystal)):
        img, glow = fn()
        put(f"block/{name}", img)
        put(f"block/{name}_emissive", glow)
    put("block/abyss_rift", abyss_rift())
    sky, sky_glow = sky_rift()
    put("block/sky_rift", sky)
    put("block/sky_rift_glow", sky_glow)
    put("item/rift_key", rift_key())


if __name__ == "__main__":
    import sys
    out = {}
    build(lambda k, v: out.__setitem__(k, v))
    vs.preview({k: v.crop((0, 0, 16, 16)) for k, v in out.items()}, sys.argv[1] if len(sys.argv) > 1 else "abyss_textures.png")
