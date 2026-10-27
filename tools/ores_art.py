#!/usr/bin/env python3
"""
Erz-Einschlüsse, von Hand gepixelt nach dem Vorbild der Vanilla-Erze: unregelmäßige, verschieden große Klumpen
(2-6 Pixel), die mit mehreren Tönen ins Gestein übergehen – oben links Glanz, unten rechts die dunkelste Erzfarbe.
Keine runden „Perlen" mit dunklem Rand.

Die Einschlüsse sind durchsichtig und liegen im Modell über dem Gestein (Stein, Tiefenschiefer, Schattenstein,
Tiefschattenstein); leuchtende Erze bekommen dazu eine Leuchtebene mit ihren hellsten Pixeln.

Zeichen: '.' = Gestein, 1..4 = Erzton (dunkel -> hell), 5..8 = zweiter Erzton (Edelstein: violette Steine).
Wird von tools/generate_textures.py aufgerufen: build(put).
"""
from __future__ import annotations

from typing import Callable

from PIL import Image

from pixelart import Color, hexc

N = 16

# Metallische Erze: flache, unregelmäßige Klumpen
METAL_A = [
    "................",
    "...34...........",
    "..34432.....3...",
    ".344321....343..",
    "..2321......21..",
    "...1............",
    "..........34....",
    ".......3.34432..",
    "......34343221..",
    ".......2221.1...",
    "..3.............",
    ".343.......34...",
    "..221.....3442..",
    "...........221..",
    ".....34.........",
    "......21........",
]

METAL_B = [
    "................",
    "..........34....",
    "...3.....34432..",
    "..3443...343221.",
    "..34421...221...",
    "...221..........",
    "................",
    ".34.......3.....",
    "3443.....343....",
    ".2321...34421...",
    "..21.....2221...",
    "...........1....",
    "......34........",
    ".....34432...3..",
    "......3221..32..",
    ".......1........",
]

# Kristall-Erze: kleine, spitze Kristalle mit hellem Glanzpunkt
GEM_C = [
    "................",
    "..4.............",
    ".434....43......",
    ".3432..4432.....",
    "..321..3321..4..",
    "........21..343.",
    "............32..",
    "....43..........",
    "...4432....4....",
    "...33421..434...",
    "....2321..321...",
    "......1.........",
    ".4..........43..",
    ".32.....4..4432.",
    "........32..321.",
    "................",
]

# Edelstein-Erz: grüne (1-4) und violette (5-8) Steine
GEM_MIXED = [
    "................",
    "..8.............",
    ".878....43......",
    ".7876..4432.....",
    "..765..3321..8..",
    "........21..787.",
    "............76..",
    "....43..........",
    "...4432....8....",
    "...33421..878...",
    "....2321..765...",
    "......1.........",
    ".4..........87..",
    ".32.....8..8876.",
    "........76..765.",
    "................",
]


def mirror(art: list[str]) -> list[str]:
    return [row[::-1] for row in art]


def flip(art: list[str]) -> list[str]:
    """Senkrecht gespiegelt – Licht bleibt oben links, indem die Töne der Klumpen erhalten bleiben."""
    return list(reversed(art))


def overlay(art: list[str], tones: list[Color], second: list[Color] | None = None) -> Image.Image:
    img = Image.new("RGBA", (N, N), (0, 0, 0, 0))
    for y, row in enumerate(art):
        for x, ch in enumerate(row):
            if ch == ".":
                continue
            v = int(ch)
            pal = tones if v <= 4 else (second or tones)
            img.putpixel((x, y), pal[(v - 1) % 4])
    return img


def glow_of(img: Image.Image, bright: list[Color]) -> Image.Image:
    glow = Image.new("RGBA", (N, N), (0, 0, 0, 0))
    for y in range(N):
        for x in range(N):
            if img.getpixel((x, y)) in bright:
                glow.putpixel((x, y), img.getpixel((x, y)))
    return glow


def tones(*hex_values: str) -> list[Color]:
    return [hexc(v) for v in hex_values]


CARBON = tones("#25262b", "#34363c", "#474a52", "#61656f")
GEMSTONE = tones("#1c6a3c", "#2c9c58", "#5fd88a", "#d2ffe2")
GEMSTONE_VIOLET = tones("#4a1a72", "#7a34b0", "#ad6ae4", "#f0d8ff")
KONTERIT = tones("#4c1c6c", "#7e30ac", "#b562e4", "#ecc8ff")
AETHERIT = tones("#0f5c6c", "#1aa2b6", "#5fdcec", "#dcfcff")
DRACONIUM = tones("#6e1210", "#b4261e", "#ec6232", "#ffcc9c")
WYVERNIT = tones("#3c6088", "#6c9cc8", "#a8d0f0", "#f2faff")
MOONSILVER = tones("#6a7592", "#96a2bc", "#c2cde0", "#eef3fb")
NIGHTSTEEL = tones("#22204a", "#34346e", "#4c4e98", "#7c80cc")
SOUL = tones("#0e5c58", "#18a69c", "#58e4d6", "#dafff8")
ABYSSITE = tones("#4c0c48", "#8c1a82", "#e04ad2", "#ffc4f4")


def build(put: Callable[[str, Image.Image], None]) -> None:
    put("block/carbon_ore_overlay", overlay(METAL_A, CARBON))
    put("block/gemstone_ore_overlay", overlay(GEM_MIXED, GEMSTONE, GEMSTONE_VIOLET))
    put("block/konterit_ore_overlay", overlay(GEM_C, KONTERIT))
    aetherit = overlay(mirror(GEM_C), AETHERIT)
    put("block/aetherit_ore_overlay", aetherit)
    put("block/aetherit_ore_glow", glow_of(aetherit, AETHERIT[2:]))
    draconium = overlay(METAL_B, DRACONIUM)
    put("block/draconium_ore_overlay", draconium)
    put("block/draconium_ore_glow", glow_of(draconium, DRACONIUM[2:]))
    put("block/wyvernit_ore_overlay", overlay(mirror(METAL_A), WYVERNIT))
    put("block/moonsilver_ore_overlay", overlay(METAL_B, MOONSILVER))
    put("block/nightsteel_ore_overlay", overlay(mirror(METAL_B), NIGHTSTEEL))
    soul = overlay(GEM_C, SOUL)
    put("block/soul_crystal_ore_overlay", soul)
    put("block/soul_crystal_ore_glow", glow_of(soul, SOUL[2:]))
    abyss = overlay(mirror(METAL_A), ABYSSITE)
    put("block/abyssite_ore_overlay", abyss)
    put("block/abyssite_ore_glow", glow_of(abyss, ABYSSITE[2:]))


if __name__ == "__main__":
    import sys
    from pathlib import Path

    tex = Path(__file__).resolve().parent.parent / "src/main/resources/assets/kingdomsatwar/textures/block"
    ref = Path(sys.argv[2]) if len(sys.argv) > 2 else None
    bases = {"carbon": "stone", "gemstone": "stone", "konterit": "stone", "aetherit": "stone", "draconium": "stone",
             "wyvernit": "stone", "moonsilver": "shadow_stone", "nightsteel": "shadow_stone",
             "soul_crystal": "deep_shadow_stone_side", "abyssite": "deep_shadow_stone_side"}
    out: dict[str, Image.Image] = {}
    build(lambda k, v: out.__setitem__(k.split("/")[-1], v))
    zoom = 6
    sheet = Image.new("RGB", (5 * (48 * zoom // 2 + 8), 2 * (48 * zoom // 2 + 8)), (40, 40, 40))
    for i, (mat, base) in enumerate(bases.items()):
        if base == "stone" and ref is not None:
            b = Image.open(ref / "stone.png").convert("RGBA")
        else:
            b = Image.open(tex / f"{base}.png").convert("RGBA").crop((0, 0, 16, 16))
        t = b.copy()
        t.alpha_composite(out[f"{mat}_ore_overlay"])
        tile = Image.new("RGBA", (32, 32))
        for k in range(4):
            tile.paste(t, ((k % 2) * 16, (k // 2) * 16))
        tile = tile.resize((32 * zoom // 2 * 3 // 2, 32 * zoom // 2 * 3 // 2), Image.NEAREST)
        sheet.paste(tile, ((i % 5) * (48 * zoom // 2 + 8), (i // 5) * (48 * zoom // 2 + 8)))
    sheet.save(sys.argv[1] if len(sys.argv) > 1 else "ores_art.png")
