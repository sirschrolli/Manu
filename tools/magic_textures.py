#!/usr/bin/env python3
"""
Texturen der Magie: der Aetherit-Stab (Holzschaft mit Goldbändern und leuchtendem Aetherit-Kristall) und die sechs
Runen der Drachen-Elemente – Steintafeln mit einem leuchtenden Runenzeichen in der Farbe des Elements.

Wird von tools/generate_textures.py aufgerufen (build(put)).
"""
from __future__ import annotations

from typing import Callable

from PIL import Image

import pixelart as pa
from pixelart import CLEAR, Color, hexc

N = 16

STAFF = [
    "...........kkk..",
    "..........kCwCk.",
    ".........kCwCCck",
    "........gkCCcck.",
    "........ogkcck..",
    ".......oGgokk...",
    "......oSso......",
    ".....oSso.......",
    "....oSso........",
    "...oGgo.........",
    "...Sso..........",
    "..oSo...........",
    ".oSso...........",
    "oGgo............",
    "ogo.............",
    ".o..............",
]

STAFF_COLORS = {
    "o": hexc("#1e140a"), "S": hexc("#8a6236"), "s": hexc("#5e3e1e"), "G": hexc("#f2cc5a"), "g": hexc("#a87a1e"),
    "C": hexc("#5ee6f4"), "c": hexc("#1ca8c0"), "w": hexc("#e8ffff"), "k": hexc("#0b4a58"),
}

TABLET = [
    "",
    "....oooooooo....",
    "...oSSSSSSSSo...",
    "..oSWSSSSSSsSo..",
    "..oSSSSSSSSSso..",
    "..oSSSSSSSSSso..",
    "..oSSSSSSSSSso..",
    "..oSSSSSSSSSso..",
    "..oSSSSSSSSSso..",
    "..oSSSSSSSSSso..",
    "..oSSSSSSSSSso..",
    "..oSSSSSSSSSso..",
    "..osSSSSSSSssso.",
    "...ossssssssoo..",
    "....oooooooo....",
    "",
]

# Runenzeichen (6 breit, 9 hoch), angelehnt an das ältere Futhark
GLYPHS = {
    "fire": [          # Kaun, als Flamme
        "..x...",
        "..xx..",
        ".x.x..",
        ".x..x.",
        "x...x.",
        "x..x..",
        ".xx...",
        "..x...",
        "......"],
    "ice": [           # Isa mit Eiskristall
        "..x...",
        "x.x.x.",
        ".xxx..",
        "..x...",
        "..x...",
        ".xxx..",
        "x.x.x.",
        "..x...",
        "......"],
    "lightning": [     # Sowilo – der Blitz
        "...xx.",
        "..xx..",
        ".xx...",
        "xxxxx.",
        "...xx.",
        "..xx..",
        ".xx...",
        ".x....",
        "......"],
    "nature": [        # Algiz – der Spross
        "x.x.x.",
        "x.x.x.",
        ".xxx..",
        "..x...",
        "..x...",
        "..x...",
        "..x...",
        ".xxx..",
        "......"],
    "earth": [         # Ingwaz – die Raute (Berg und Saat)
        "..x...",
        ".x.x..",
        "x...x.",
        ".x.x..",
        "..x...",
        ".x.x..",
        "x...x.",
        ".xxx..",
        "......"],
    "water": [         # Laguz – die Welle
        "..x...",
        "..xx..",
        "..x.x.",
        "..x..x",
        "..x...",
        "..x...",
        "..x...",
        "..x...",
        "......"],
}

ELEMENT_COLORS = {
    "fire": ("#ff6a1a", "#ffd080"), "ice": ("#7fd8ff", "#e8fbff"), "lightning": ("#ffd21a", "#fff6b0"),
    "nature": ("#56c832", "#c8ff9a"), "earth": ("#c8904a", "#f4d8a0"), "water": ("#28b8f0", "#b8f2ff"),
}


def rune(element: str) -> Image.Image:
    """Dunkle Schiefertafel; das Runenzeichen leuchtet in der Farbe des Elements und strahlt leicht in den Stein."""
    stone, stone_dark, stone_light, edge = hexc("#575b63"), hexc("#3b3f46"), hexc("#7b8089"), hexc("#16171a")
    img = pa.sprite([r if r else "." * N for r in TABLET], {"o": edge, "S": stone, "s": stone_dark, "W": stone_light})
    px = img.load()
    stone_noise = pa.value_noise(sum(map(ord, element)), 2)   # deterministisch (kein hash())
    for y in range(N):
        for x in range(N):
            if px[x, y] == stone and stone_noise[y][x] > 0.7:
                px[x, y] = hexc("#4d5159")
    glow, core = (hexc(c) for c in ELEMENT_COLORS[element])
    glyph = GLYPHS[element]
    ox, oy = 5, 3
    # Schein um das Zeichen: Elementfarbe halb in den Stein gemischt
    for gy in range(-1, len(glyph) + 1):
        for gx in range(-1, 7):
            if glyph_at(glyph, gx, gy):
                continue
            near = any(glyph_at(glyph, gx + dx, gy + dy) for dx in (-1, 0, 1) for dy in (-1, 0, 1))
            tx, ty = ox + gx, oy + gy
            if near and 3 <= tx <= 12 and 3 <= ty <= 12 and px[tx, ty][3] == 255 and px[tx, ty] != edge:
                base = px[tx, ty]
                px[tx, ty] = tuple((base[i] * 3 + glow[i] * 2) // 5 for i in range(3)) + (255,)
    for gy, row in enumerate(glyph):
        for gx, ch in enumerate(row):
            if ch == "x":
                px[ox + gx, oy + gy] = core if (gx + gy) % 3 else glow
    return img


def glyph_at(glyph: list[str], x: int, y: int) -> bool:
    return 0 <= y < len(glyph) and 0 <= x < len(glyph[y]) and glyph[y][x] == "x"


def build(put: Callable[[str, Image.Image], None]) -> None:
    put("item/aetherit_staff", pa.sprite(STAFF, STAFF_COLORS))
    for element in GLYPHS:
        put(f"item/{element}_rune", rune(element))


if __name__ == "__main__":
    import sys

    sheet: dict[str, Image.Image] = {}
    build(lambda key, img: sheet.__setitem__(key, img))
    zoom, pad, cols = 8, 4, 7
    cell = 16 * zoom + pad
    rows = (len(sheet) + cols - 1) // cols
    out = Image.new("RGBA", (cols * cell + pad, rows * cell + pad), (60, 64, 70, 255))
    for i, (key, img) in enumerate(sheet.items()):
        tile = pa.checker(16, 16, (150, 150, 150, 255), (128, 128, 128, 255))
        tile.alpha_composite(img)
        out.alpha_composite(pa.zoom(tile, zoom), (pad + (i % cols) * cell, pad + (i // cols) * cell))
    target = sys.argv[1] if len(sys.argv) > 1 else "/tmp/claude-0/-home-claude/50786bb5-6cc3-5907-a25e-13bee43305bb/scratchpad/magic_tex.png"
    out.save(target)
    print(f"{len(sheet)} Texturen -> {target}")
