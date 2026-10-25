#!/usr/bin/env python3
"""
Gegenstands-Texturen rund um die wilden Kreaturen: Spawn-Eier (Eiform mit den Merkmalen der Kreatur),
Wildbret (roh und gebraten), Irrlichtstaub und die Trollkeule.

Wird von tools/generate_textures.py aufgerufen (build(put)).
"""
from __future__ import annotations

from typing import Callable

from PIL import Image

import pixelart as pa
from pixelart import CLEAR, Color, hexc


def pal(*values: str) -> list[Color]:
    return [hexc(v) for v in values]


def egg(base: list[Color], outline: Color, overlay: list[str] | None = None, colors: dict[str, Color] | None = None,
        spots: list[Color] | None = None, seed: int = 0) -> Image.Image:
    """Eiform (oben schmaler), Licht von oben links, Rand in einem dunklen Ton; darüber die Merkmale."""
    img = Image.new("RGBA", (16, 16), CLEAR)
    px = img.load()
    for y in range(16):
        for x in range(16):
            dx = (x + 0.5 - 8.0) / 5.6
            dy = (y + 0.5 - 9.0) / 6.6
            squeeze = 1.0 + 0.22 * max(0.0, -dy)
            r2 = (dx * squeeze) ** 2 + dy ** 2
            if r2 > 1.0:
                continue
            light = 0.62 - 0.38 * dy - 0.3 * dx * squeeze - 0.18 * r2
            col = pa.pick(base, light)
            if spots is not None and pa.value_noise(seed, 3)[y][x] > 0.68:
                col = pa.pick(spots, light)
            px[x, y] = col
    img = pa.outline(img, outline)
    if overlay:
        px = img.load()
        for y, row in enumerate(overlay):
            for x, ch in enumerate(row):
                if ch != ".":
                    px[x, y] = CLEAR if ch == " " else colors[ch]
    return img


# =============================================================================
# Spawn-Eier
# =============================================================================

WILD_BOAR_EGG = [
    "................",
    "...k........k...",
    "...kk.bbbb.kk...",
    "....k.bbbb.k....",
    "................",
    "................",
    "................",
    ".....e....e.....",
    "................",
    "......pppp......",
    ".....pPnnPp.....",
    "....wpPPPPpw....",
    "....w.pppp.w....",
    "................",
]

DEER_EGG = [
    "..a.a......a.a..",
    "..aaa......aaa..",
    "...aa.....aa....",
    "....a.....a.....",
    "...ee.....ee....",
    "................",
    "................",
    ".....k....k.....",
    "................",
    "................",
    "......wwww......",
    ".....wwnnww.....",
    "......wwww......",
]

GOOSE_EGG = [
    "................",
    "................",
    "................",
    "................",
    ".......nnn......",
    "......nnnnn.....",
    "......nkn.n.....",
    "......nnnbbbb...",
    ".......nnbBBBb..",
    "........nbbbb...",
]

WISP_EGG = [
    "................",
    "................",
    "................",
    "................",
    "................",
    "................",
    "......zzz.......",
    ".....zZZZz......",
    ".....zZWZz......",
    ".....zZZZz......",
    "......zzz.......",
    "...s........s...",
    "................",
    "...........s....",
]

TROLL_EGG = [
    "................",
    "................",
    "................",
    "................",
    "....hhhhhhh.....",
    "................",
    ".....bbb.bbb....",
    ".....eY...Ye....",
    ".......NN.......",
    "......NNNN......",
    "......NnnN......",
    "................",
    ".....mmmmmm.....",
    ".....w....w.....",
]

LINDWORM_EGG = [
    "..h..........h..",
    "...hh......hh...",
    ".....h....h.....",
    "................",
    "................",
    "....Y......Y....",
    "....yk....ky....",
    "................",
    "................",
    "...s.s.s.s.s....",
    "................",
    "....bbbbbbbb....",
    "....b.b.b.b.....",
]


def spawn_eggs(put: Callable[[str, Image.Image], None]) -> None:
    put("item/wild_boar_spawn_egg", egg(
        pal("#1e1712", "#3a2c22", "#5a4636", "#7a6450", "#9a8268"), hexc("#120d0a"), WILD_BOAR_EGG,
        {"k": hexc("#1a120e"), "b": hexc("#0e0a08"), "e": hexc("#e8a040"), "p": hexc("#9a7268"),
         "P": hexc("#b48a80"), "n": hexc("#3a2420"), "w": hexc("#f4ecd8")}))
    put("item/deer_spawn_egg", egg(
        pal("#4a2a16", "#7a4622", "#a0623a", "#c0864e", "#dca878"), hexc("#2e1a0e"), DEER_EGG,
        {"a": hexc("#e8dcc0"), "e": hexc("#7a4a28"), "k": hexc("#120a08"), "w": hexc("#f0e6d4"),
         "n": hexc("#1a1210")}))
    put("item/goose_spawn_egg", egg(
        pal("#7a746c", "#9a948a", "#b8b2a8", "#d4d0c8", "#f0ece6"), hexc("#4a4640"), GOOSE_EGG,
        {"n": hexc("#4a4440"), "k": hexc("#0e0a08"), "b": hexc("#e0701a"), "B": hexc("#ffa040")}))
    put("item/will_o_wisp_spawn_egg", egg(
        pal("#0e5a58", "#1a8a84", "#2cb8ae", "#6ae0d4", "#b8fff6"), hexc("#06403e"), WISP_EGG,
        {"z": hexc("#8ff8ee"), "Z": hexc("#d4fffb"), "W": hexc("#ffffff"), "s": hexc("#e8fffd")}))
    put("item/cave_troll_spawn_egg", egg(
        pal("#262a24", "#3e443a", "#5a6252", "#76806c", "#949e88"), hexc("#181a16"), TROLL_EGG,
        {"h": hexc("#1a1612"), "b": hexc("#2a2e26"), "e": hexc("#1a1612"), "Y": hexc("#ffd84a"),
         "N": hexc("#6e7862"), "n": hexc("#2a2e26"), "m": hexc("#1a1a16"), "w": hexc("#f0e8d4")}))
    put("item/lindworm_spawn_egg", egg(
        pal("#16200e", "#2c3c18", "#46602a", "#62803a", "#86a050"), hexc("#0e1408"), LINDWORM_EGG,
        {"h": hexc("#c4b494"), "Y": hexc("#ffcc33"), "y": hexc("#d89a1a"), "k": hexc("#140c04"),
         "s": hexc("#1e2a12"), "b": hexc("#d8c87a")}))


# =============================================================================
# Wildbret, Irrlichtstaub, Trollkeule
# =============================================================================

HAUNCH = [
    "",
    "",
    "..........ww....",
    ".........wWWw...",
    ".........wWw....",
    "........owwo....",
    ".......orrro....",
    ".....oorRRrro...",
    "....orRRhRRrro..",
    "...orRhRRRRRrro.",
    "...orRRRRRhRrro.",
    "...orrRRRRRrrro.",
    "....orrrrrrrro..",
    ".....oorrrroo...",
    ".......oooo.....",
]

WISP_DUST = [
    "",
    "",
    "",
    "",
    ".......W........",
    "......WzW...W...",
    ".......W...WzW..",
    "...W........W...",
    "..WzW...........",
    "...W..oooo......",
    ".....oZzzZo.....",
    "....oZzZZzZo....",
    "...oZzZWZZzZo...",
    "...ozZZZZZZzo...",
    "....oooooooo....",
]

TROLL_CLUB = [
    "........oooo....",
    ".......oIIiIo...",
    "......oIgIiigo..",
    ".....oIIiiIIgo..",
    ".....ogIiIiigio.",
    "....oiIiiIiiio..",
    "....ojiiIigio...",
    "...ojjiiiioo....",
    "...ojjjooo......",
    "..ojjo..........",
    ".ojjo...........",
    ".olo............",
    "olo.............",
    "oo..............",
]


def items(put: Callable[[str, Image.Image], None]) -> None:
    put("item/venison", pa.sprite(HAUNCH, {
        "o": hexc("#3a0c10"), "r": hexc("#8a1c22"), "R": hexc("#b83038"), "h": hexc("#e8a0a0"),
        "w": hexc("#c8bca4"), "W": hexc("#f4ecdc")}))
    put("item/cooked_venison", pa.sprite(HAUNCH, {
        "o": hexc("#2e1608"), "r": hexc("#6a3a16"), "R": hexc("#9a5a26"), "h": hexc("#d89a52"),
        "w": hexc("#c8bca4"), "W": hexc("#f4ecdc")}))
    put("item/wisp_dust", pa.sprite(WISP_DUST, {
        "o": hexc("#0e5a58"), "z": hexc("#2cb8ae"), "Z": hexc("#6ae0d4"), "W": hexc("#e8fffd")}))
    put("item/troll_club", pa.sprite(TROLL_CLUB, {
        "o": hexc("#1e140a"), "i": hexc("#6a4624"), "I": hexc("#8a6236"), "g": hexc("#9aa0a8"),
        "j": hexc("#7a5430"), "l": hexc("#4a3018")}))


def build(put: Callable[[str, Image.Image], None]) -> None:
    spawn_eggs(put)
    items(put)


if __name__ == "__main__":
    import sys

    sheet: dict[str, Image.Image] = {}
    build(lambda key, img: sheet.__setitem__(key, img))
    zoom, pad, cols = 8, 4, 5
    cell = 16 * zoom + pad
    rows = (len(sheet) + cols - 1) // cols
    out = Image.new("RGBA", (cols * cell + pad, rows * cell + pad), (60, 64, 70, 255))
    for i, (key, img) in enumerate(sheet.items()):
        tile = pa.checker(16, 16, (150, 150, 150, 255), (128, 128, 128, 255))
        tile.alpha_composite(img)
        out.alpha_composite(pa.zoom(tile, zoom), (pad + (i % cols) * cell, pad + (i // cols) * cell))
    target = sys.argv[1] if len(sys.argv) > 1 else "/tmp/claude-0/-home-claude/50786bb5-6cc3-5907-a25e-13bee43305bb/scratchpad/creature_items.png"
    out.save(target)
    print(f"{len(sheet)} Symbole -> {target}")
