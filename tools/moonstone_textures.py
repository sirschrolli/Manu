#!/usr/bin/env python3
"""
Mondstein (0.96): der Mondsteinblock und der Mondstein selbst.

  * Mondsteinblock – eine glatt geschliffene Platte aus milchigem, bläulich-weißem Stein mit abgefaster Kante (oben links
    hell, unten rechts dunkel), innen wolkig wie echter Mondstein. Darüber wandert der blaue Schimmer (Adulareszenz): ein
    schräges Lichtband, das langsam über den Stein zieht (Leuchtebene, bewegt; nahtlos – nebeneinander gesetzte Blöcke
    schimmern wie eine Fläche).
  * Mondstein – ein gewölbter, ovaler Stein (Cabochon): milchig weiß, oben ein Glanzpunkt, quer darüber das blaue Band.

Wird von tools/generate_textures.py aufgerufen: build(put, animations).
"""
from __future__ import annotations

import math
from typing import Callable

from PIL import Image

import item_style as st
import pixelart as pa
import vanilla_style as vs
from pixelart import hexc

N = 16
CLEAR = (0, 0, 0, 0)

#: der Stein: milchig, kühl, ein Hauch Flieder in den Tiefen
STONE = vs.tones("#b9c5ec", 7, spread=0.27, cool=245, warm=285, hue=14, sat=0.08)
#: der wandernde Schimmer
SHEEN = [hexc("#5d8fe8"), hexc("#7fb2ff"), hexc("#a8d4ff"), hexc("#dcf0ff")]
#: Bilder im Lauf des Schimmers (je 4 Ticks)
SHEEN_FRAMES = 16


def _cloud() -> list[list[float]]:
    return vs.layered(96011, ((8, 8, 0.55), (4, 4, 0.3), (2, 2, 0.15)))


def moonstone_block() -> tuple[Image.Image, Image.Image]:
    """Der Block und sein wandernder Schimmer (Bildfolge, senkrecht gestapelt)."""
    cloud = _cloud()
    g = vs.grid(3)
    # geschliffen wie ein Edelstein: ein Rahmen, darin vier schräge Facetten um eine helle Tafel; das Licht kommt von oben links
    for y in range(N):
        for x in range(N):
            v = cloud[y][x]
            cx, cy = x - 7.5, y - 7.5
            if max(abs(cx), abs(cy)) >= 7.0:
                tone = 4 if (x == 0 or y == 0) else 1
            elif max(abs(cx), abs(cy)) <= 3.0:
                tone = 4 + (1 if v > 0.55 else 0)
            elif abs(cy) >= abs(cx):
                tone = 5 if cy < 0 else 2
            else:
                tone = 4 if cx < 0 else 3
            if 0 < max(abs(cx), abs(cy)) < 7.0 and v > 0.7:
                tone = min(6, tone + 1)
            elif 0 < max(abs(cx), abs(cy)) < 7.0 and v < 0.25:
                tone = max(1, tone - 1)
            g[y][x] = tone
    # die Kanten der Facetten: helle Grate zur Lichtseite
    for i in range(1, 4):
        g[i][i] = 6
        g[i][N - 1 - i] = 4
        g[N - 1 - i][i] = 3
        g[N - 1 - i][N - 1 - i] = 1
    for i in range(4, 12):
        g[4][i] = max(g[4][i], 5)
        g[i][4] = max(g[i][4], 5)
        g[11][i] = min(g[11][i], 2)
        g[i][11] = min(g[i][11], 2)
    # zwei feine Einschlüsse (dunkle Nadeln im Stein)
    for x, y in ((5, 9), (6, 10), (11, 4)):
        g[y][x] = 2
    img = vs.paint(g, STONE)
    # ein paar helle Glanzpunkte
    for x, y in ((3, 3), (4, 3), (3, 4), (12, 11)):
        img.putpixel((x, y), pa.mix(img.getpixel((x, y)), hexc("#ffffff"), 0.6))

    # der Schimmer: zwei schräge Bänder (halbe Breite versetzt), die über den Stein ziehen; auf der Diagonale nahtlos
    frames = Image.new("RGBA", (N, N * SHEEN_FRAMES), CLEAR)
    for f in range(SHEEN_FRAMES):
        centre = f * N / SHEEN_FRAMES
        for y in range(1, N - 1):
            for x in range(1, N - 1):
                d = (x + y) % N
                dist = min(abs(d - centre), N - abs(d - centre))
                dist2 = min(abs(d - (centre + N / 2) % N), N - abs(d - (centre + N / 2) % N))
                k = max(0.0, 1.0 - dist / 3.2) + 0.6 * max(0.0, 1.0 - dist2 / 2.0)
                k *= 0.45 + 0.8 * cloud[y][x]
                if k < 0.32:
                    continue
                level = min(len(SHEEN) - 1, int((k - 0.32) / 0.22))
                base = img.getpixel((x, y))
                frames.putpixel((x, f * N + y), pa.mix(base, SHEEN[level], 0.55 + 0.1 * level))
    return img, frames


MOONSTONE_GEM = """
................
................
................
......5666......
....55666665....
...4556666654...
..45ttuuuutt54..
..44rssttssr44..
..344qrrrrq443..
..334444444433..
...2334444332...
....22333322....
......2222......
................
................
................
"""

GEM = st.ramp("#d0d8f4", dark=0.46, light=1.0, cool=248, chroma_dark=1.7, hue_dark=18, chroma_light=0.25)
GEM_SHEEN = st.ramp("#4f9bff", dark=0.46, light=1.0, cool=220, chroma_light=0.6)


def moonstone_gem() -> Image.Image:
    return st.Spr.art(MOONSTONE_GEM).outline(prefer=("A", "D")).image({"A": GEM, "D": GEM_SHEEN})


# =============================================================================
# Der Hüterstein (am Grund des Mondsteinbruchs)
# =============================================================================

#: Feenstein, dunkler und kühler (behauener Block)
KEEPER = vs.tones("#a79fbb", 7, spread=0.28, cool=255, warm=320, hue=10, sat=0.05)
RUNE = [hexc("#2f62d0"), hexc("#5a96f4"), hexc("#9cc8ff"), hexc("#e2f2ff")]

# Das Zeichen des Hüters: ein Auge über einer Mondsichel (12 x 12, mittig)
EYE = [
    "............",
    "...######...",
    "..#......#..",
    ".#..####..#.",
    "#..##..##..#",
    ".#..####..#.",
    "..#......#..",
    "...######...",
    "............",
    ".#........#.",
    "..##....##..",
    "....####....",
]


def keeper_stone() -> dict[str, Image.Image]:
    """Der Hüterstein: oben eine Platte mit eingemeißeltem Rahmen und dem Zeichen des Hüters (Auge über Mondsichel), an den
    Seiten behauener Feenstein mit einem Runenband; die Rillen leuchten (eigene Ebenen)."""
    out: dict[str, Image.Image] = {}
    # oben
    g = vs.polished(96101, shares=(0.2, 0.45, 0.35))
    for y in range(N):
        for x in range(N):
            g[y][x] = 3 + g[y][x] // 2
    vs.bevel_frame(g, 1, 1, 14, 14, light=5, dark=1)
    img = vs.paint(g, KEEPER)
    glow = Image.new("RGBA", (N, N), CLEAR)
    for y, row in enumerate(EYE):
        for x, ch in enumerate(row):
            if ch == "#":
                c = RUNE[2] if y < 8 else RUNE[1]
                if (x, y) in ((5, 4), (6, 4)):
                    c = RUNE[3]
                img.putpixel((2 + x, 2 + y), pa.mix(c, KEEPER[1], 0.25))
                glow.putpixel((2 + x, 2 + y), c)
    # die Pupille
    for x, y in ((7, 6), (8, 6)):
        img.putpixel((x, y), RUNE[3])
        glow.putpixel((x, y), RUNE[3])
    out["block/keeper_stone_top"] = img
    out["block/keeper_stone_top_glow"] = glow
    # Seiten: zwei Reihen behauener Quader, dazwischen ein Band mit Runen
    g = vs.grid(3)
    stone = vs.stone(96111)
    for y in range(N):
        for x in range(N):
            g[y][x] = 2 + stone[y][x] // 1
    for x in range(N):
        g[0][x] = 5
        g[N - 1][x] = 0
        g[5][x] = 1
        g[10][x] = 1
        g[6][x] = 4
    for y in range(N):
        if y < 5 or y > 10:
            g[y][0] = max(g[y][0], 4)
            g[y][15] = min(g[y][15], 1)
    for (x, y0, y1) in ((7, 1, 4), (3, 11, 14), (11, 11, 14)):
        for y in range(y0, y1 + 1):
            g[y][x] = 1
    side = vs.paint(g, KEEPER)
    side_glow = Image.new("RGBA", (N, N), CLEAR)
    runes = ["#.#..##.#.##.#.#", "##.#..#.##..#.##", "#.#.##..#.#.##.#"]
    for dy, row in enumerate(runes):
        for x, ch in enumerate(row):
            if ch == "#":
                c = RUNE[2] if dy == 1 else RUNE[1]
                side.putpixel((x, 7 + dy), pa.mix(c, KEEPER[1], 0.3))
                side_glow.putpixel((x, 7 + dy), c)
    out["block/keeper_stone_side"] = side
    out["block/keeper_stone_side_glow"] = side_glow
    # unten
    out["block/keeper_stone_bottom"] = vs.paint(vs.stone(96121), KEEPER[1:5])
    return out


def build(put: Callable[[str, Image.Image], None], animations: dict) -> None:
    block, sheen = moonstone_block()
    put("block/moonstone_block", block)
    put("block/moonstone_block_glow", sheen)
    animations["block/moonstone_block_glow"] = (SHEEN_FRAMES, 4, True)
    put("item/moonstone", moonstone_gem())
    for key, img in keeper_stone().items():
        put(key, img)


if __name__ == "__main__":
    import sys

    out: dict[str, Image.Image] = {}
    build(lambda k, i: out.__setitem__(k, i), {})
    tile = Image.new("RGBA", (N * 2, N * 2))
    for dx in (0, N):
        for dy in (0, N):
            tile.alpha_composite(out["block/moonstone_block"], (dx, dy))
            tile.alpha_composite(out["block/moonstone_block_glow"].crop((0, 5 * N, N, 6 * N)), (dx, dy))
    tiles = [tile.resize((N, N), Image.NEAREST), out["block/moonstone_block"], out["item/moonstone"]]
    sheet = Image.new("RGBA", (len(tiles) * N * 10 + 10 * (len(tiles) - 1), N * 10), (40, 40, 48, 255))
    for i, t in enumerate(tiles):
        sheet.alpha_composite(t.resize((N * 10, N * 10), Image.NEAREST), (i * (N * 10 + 10), 0))
    big = Image.new("RGBA", (N * 2, N * 2))
    for dx in (0, N):
        for dy in (0, N):
            big.alpha_composite(out["block/moonstone_block"], (dx, dy))
            big.alpha_composite(out["block/moonstone_block_glow"].crop((0, 5 * N, N, 6 * N)), (dx, dy))
    sheet.alpha_composite(big.resize((N * 5, N * 5), Image.NEAREST), (0, 0))
    sheet.save(sys.argv[1] if len(sys.argv) > 1 else "moonstone.png")
