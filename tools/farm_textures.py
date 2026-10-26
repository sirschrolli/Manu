#!/usr/bin/env python3
"""
Texturen für "Mehr Essen & Pflanzen": Hopfen, Weintrauben und Knoblauch (je vier Wachstumsstufen), die
leuchtende Mondblume, Gegenstände (Knoblauch, Trauben, Hopfen, Ale, Wein, Met, Knoblauchbrot) und das
Festmahl (Gegenstand und Textur-Atlas für das Blockmodell).

Wird von tools/generate_textures.py aufgerufen (build(put)).
"""
from __future__ import annotations

from typing import Callable

from PIL import Image

import pixelart as pa
from pixelart import CLEAR, Color, hexc

N = 16


def sprite(rows: list[str], colors: dict[str, Color]) -> Image.Image:
    rows = [r if r else "." * N for r in rows]
    rows += ["." * N] * (N - len(rows))
    for i, r in enumerate(rows):
        if len(r) != N:
            raise ValueError(f"Zeile {i} hat {len(r)} Zeichen: {r!r}")
    return pa.sprite(rows, colors)


def pad_top(rows: list[str]) -> list[str]:
    """Pflanzen stehen unten im Bild: leere Zeilen oben auffüllen."""
    return ["." * N] * (N - len(rows)) + rows


LEAF = {
    "k": hexc("#1d4416"), "g": hexc("#2b6420"), "G": hexc("#3c842a"), "l": hexc("#56a238"), "L": hexc("#7cc24e"),
    "s": hexc("#4a3218"), "S": hexc("#7a5a32"),                                    # Stange / Rebstock
}

# =============================================================================
# Hopfen: rankt an einer Stange empor, reif mit hellgrünen Dolden
# =============================================================================



# =============================================================================
# Weintrauben: Rebe am Pfahl, erst grüne, dann blaue Trauben
# =============================================================================

GRAPE_COLORS = {**LEAF, "u": hexc("#6aa632"), "U": hexc("#a8d65e"),
                "m": hexc("#2a0e36"), "M": hexc("#5a2470"), "N": hexc("#8a44a6"), "x": hexc("#c890dc")}


# =============================================================================
# Knoblauch: flache, graugrüne Blätter, reif mit weißen Knollen
# =============================================================================



# =============================================================================
# Mondblume: blasse, bläulich-weiße Blüte, die nachts schwach leuchtet
# =============================================================================

MOONFLOWER = pad_top([
    "......b.b.......",
    ".....bwBwb..b...",
    "....bwwcwwbbwb..",
    ".....BwcwB.bcb..",
    "......bwb...B...",
    ".......G...lG...",
    "...l...G..lG....",
    "....l..G.lG.....",
    ".....l.GlG......",
    "......lGG.......",
    ".......G........",
    ".......g........"])

MOON_COLORS = {**LEAF, "w": hexc("#ffffff"), "b": hexc("#b4d0ff"), "B": hexc("#86a8ea"), "c": hexc("#fff4b0")}


def crops(put: Callable[[str, Image.Image], None]) -> None:
    put("block/moonflower", sprite(MOONFLOWER, MOON_COLORS))


# =============================================================================
# Gegenstände
# =============================================================================

GARLIC_ITEM = [
    "",
    "",
    ".......k........",
    ".......kV.......",
    "......oQo.......",
    ".....oqQqo......",
    "....oqwQqqo.....",
    "...oqwwQqxqo....",
    "...oqwqQqxqo....",
    "...oqqqQxqqo....",
    "...oxqqQqqxo....",
    "....oqqQqqo.....",
    ".....ooooo......",
    ".....r.r.r......",
]

GRAPES_ITEM = [
    "",
    ".........LL.....",
    "........LlLl....",
    ".......sLll.....",
    "......s.........",
    ".....mMmmMm.....",
    "....mNxMNxMm....",
    "....mMMmMMMm....",
    ".....mNxMNxMm...",
    ".....mMMmMMMm...",
    "......mNxMNm....",
    "......mMMmMm....",
    ".......mNxm.....",
    ".......mMMm.....",
    "........mm......",
]

HOPS_ITEM = [
    "",
    "........s.......",
    ".......sLl......",
    "......oCCo......",
    ".....oCwCCo.....",
    ".....ocCCcCo....",
    "....oCwCcCCo....",
    "....ocCCcCwCo...",
    "....oCcwCCcCo...",
    ".....ocCCcCo....",
    ".....oCcCwCo....",
    "......ocCCo.....",
    ".......oCo......",
    "........o.......",
]

BOTTLE = [
    "",
    "......oooo......",
    ".....okKKko.....",
    "......okko......",
    "......oNNo......",
    "......oNNo......",
    ".....oNNNNo.....",
    "....oNLLLLNo....",
    "...oNwLLLLLNo...",
    "...oNwttttLLo...",
    "...oNLtTTtLLo...",
    "...oNLttttLLo...",
    "...oNLLLLLLDo...",
    "...oNDLLLLDDo...",
    "....oNDDDDDo....",
    ".....ooooooo....",
]


def bottle(glass: str, glass_edge: str, liquid: str, liquid_dark: str, label: str, emblem: str) -> Image.Image:
    return sprite(BOTTLE, {
        "o": hexc(glass_edge), "N": hexc(glass), "L": hexc(liquid), "D": hexc(liquid_dark), "w": hexc("#f4f8f0"),
        "t": hexc(label), "T": hexc(emblem), "k": hexc("#6a4624"), "K": hexc("#9a7040")})


GARLIC_BREAD = [
    "",
    "",
    "",
    "..........ooo...",
    "........oobbao..",
    "......oobcgcbao.",
    ".....obcgcbcgbo.",
    "....obcbcgcbbao.",
    "...obgcbcbcgbo..",
    "..obcbcgcbcbao..",
    "..oacgcbcbgao...",
    "..oaabcbcbao....",
    "...oaaabbao.....",
    "....oooooo......",
]

FEAST_ITEM = [
    "",
    "",
    "",
    "..........ww....",
    ".........wWw....",
    "........oWo.....",
    ".....oooRro.....",
    "....orRRRRro....",
    "...orRhRRRRro...",
    "...orRRRRRRro...",
    "..corRRRRRrocp..",
    ".cCporrrrrropCc.",
    "PPPPPPPPPPPPPPPP",
    ".BPPPPPPPPPPPPB.",
    "..BBBBBBBBBBBB..",
]


def items(put: Callable[[str, Image.Image], None]) -> None:
    put("item/garlic", sprite(GARLIC_ITEM, {
        "o": hexc("#8a7a68"), "q": hexc("#ece4d4"), "Q": hexc("#cfc2ac"), "w": hexc("#ffffff"),
        "x": hexc("#b08ab8"), "k": hexc("#5a7a32"), "V": hexc("#8ab866"), "r": hexc("#c8b898")}))
    put("item/grapes", sprite(GRAPES_ITEM, {**GRAPE_COLORS}))
    put("item/hops", sprite(HOPS_ITEM, {
        "o": hexc("#3e5a1e"), "c": hexc("#7fae3e"), "C": hexc("#b4d86a"), "w": hexc("#e6f4b0"),
        "s": hexc("#5a4a22"), "L": hexc("#7cc24e"), "l": hexc("#56a238")}))
    # Ale: Bernsteinglas, goldenes Bier; Wein: dunkelgrünes Glas, tiefrot; Met: braunes Glas, honiggelb
    put("item/ale", bottle("#7a4a1e", "#33200c", "#c47a22", "#8a4e12", "#e8dcc0", "#3a7a2a"))
    put("item/wine", bottle("#2e5a3a", "#14281a", "#8a1424", "#5a0a16", "#efe6d0", "#8a1424"))
    put("item/mead", bottle("#cfd8c4", "#5e6656", "#f6c02a", "#c8880e", "#fff4d0", "#2a2418"))
    put("item/garlic_bread", sprite(GARLIC_BREAD, {
        "o": hexc("#5a3410"), "a": hexc("#a8682a"), "b": hexc("#d8a050"), "c": hexc("#f0cc84"), "g": hexc("#4a8a2e")}))
    put("item/feast", sprite(FEAST_ITEM, {
        "o": hexc("#2e1608"), "r": hexc("#6a3a16"), "R": hexc("#9a5a26"), "h": hexc("#d89a52"),
        "w": hexc("#e8e0cc"), "W": hexc("#fffaf0"), "c": hexc("#d86a1a"), "C": hexc("#f0a040"),
        "p": hexc("#d8b04a"), "P": hexc("#8a6236"), "B": hexc("#5a3c1e")}))


# =============================================================================
# Festmahl: Textur-Atlas für das Blockmodell
#   Braten-Kruste (u0-8, v0-8), Anschnitt (u8-12, v0-4), Knochen (u12-16, v0-2), Möhre (u8-12, v4-6),
#   Kartoffel (u12-16, v4-6), Kräuter (u8-10, v6-8), Apfel (u10-12, v6-8), Brotlaib (u12-16, v6-10)
# =============================================================================

def feast_atlas() -> Image.Image:
    img = Image.new("RGBA", (N, N), CLEAR)
    px = img.load()
    crust = [hexc(c) for c in ("#4a2410", "#6a3a16", "#8a4e1e", "#a86428", "#c8843a")]
    noise = pa.value_noise(331, 3)
    for y in range(8):
        for x in range(8):
            v = noise[y][x]
            px[x, y] = crust[1 + min(3, int(v * 4))]
            if (x + y * 3) % 7 == 0:
                px[x, y] = crust[4]                       # Fettglanz
            if y >= 6 and (x + y) % 3 == 0:
                px[x, y] = crust[0]                       # Röstspuren unten
    meat = [hexc(c) for c in ("#7a2a20", "#b84a3a", "#d8746a", "#f0b0a0", "#fff0e0")]
    for y in range(4):
        for x in range(8, 12):
            edge = x in (8, 11) or y in (0, 3)
            px[x, y] = crust[1] if edge else (meat[2] if (x + y) % 2 else meat[1])
    px[9, 1] = meat[3]
    px[10, 2] = meat[3]
    bone = [hexc(c) for c in ("#c8bca4", "#f4ecdc", "#fffaf0")]
    for y in range(2):
        for x in range(12, 16):
            px[x, y] = bone[2] if y == 0 else bone[1]
    for x in range(8, 12):
        px[x, 4] = hexc("#f09030")
        px[x, 5] = hexc("#c86418")
    for x in range(12, 16):
        px[x, 4] = hexc("#e8c060")
        px[x, 5] = hexc("#b88a30")
    for y in range(6, 8):
        for x in range(8, 10):
            px[x, y] = hexc("#4a9a2e") if (x + y) % 2 else hexc("#2e6a1e")
        for x in range(10, 12):
            px[x, y] = hexc("#d8301e") if y == 7 else hexc("#f05a3a")
    for y in range(6, 10):
        for x in range(12, 16):
            px[x, y] = hexc("#e0a450") if y == 6 else hexc("#c8842e") if y < 9 else hexc("#8a5418")
    px[13, 7] = hexc("#f4cc84")
    return img


def build(put: Callable[[str, Image.Image], None]) -> None:
    crops(put)
    items(put)
    put("block/feast", feast_atlas())


if __name__ == "__main__":
    import sys

    sheet: dict[str, Image.Image] = {}
    build(lambda key, img: sheet.__setitem__(key, img))
    zoom, pad, cols = 8, 4, 8
    cell = 16 * zoom + pad
    rows = (len(sheet) + cols - 1) // cols
    out = Image.new("RGBA", (cols * cell + pad, rows * cell + pad), (60, 64, 70, 255))
    for i, (key, img) in enumerate(sheet.items()):
        tile = pa.checker(16, 16, (150, 150, 150, 255), (128, 128, 128, 255))
        tile.alpha_composite(img)
        out.alpha_composite(pa.zoom(tile, zoom), (pad + (i % cols) * cell, pad + (i // cols) * cell))
    target = sys.argv[1] if len(sys.argv) > 1 else "/tmp/claude-0/-home-claude/50786bb5-6cc3-5907-a25e-13bee43305bb/scratchpad/farm_tex.png"
    out.save(target)
    print(f"{len(sheet)} Texturen -> {target}")
