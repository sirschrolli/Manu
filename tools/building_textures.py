#!/usr/bin/env python3
"""
Texturen der zusätzlichen Baublöcke: Putz, Bleiglasfenster, Burgtür, Fensterläden, Laternen und
Kronleuchter (eigene Pixelkunst im Vanilla-Stil, 16x16). Möbel und Zinnen verwenden vorhandene
Holz-/Stein-Texturen und brauchen hier nichts.

Wird von tools/generate_textures.py aufgerufen (build(put)).
"""
from __future__ import annotations

import random
from typing import Callable

from PIL import Image

import pixelart as pa
from block_textures import PLASTER, soft_noise
from pixelart import CLEAR, Color, hexc

N = 16


def pal(*values: str) -> list[Color]:
    return [hexc(v) for v in values]


def sprite(rows: list[str], colors: dict[str, Color]) -> Image.Image:
    for i, r in enumerate(rows):
        if len(r) != N:
            raise ValueError(f"Zeile {i} hat {len(r)} Zeichen: {r!r}")
    return pa.sprite(rows, colors)


# =============================================================================
# Putz
# =============================================================================

def white_plaster() -> Image.Image:
    """Kalkputz: ruhige, leicht wolkige Fläche mit Kellenspuren und feinen Haarrissen."""
    noise = soft_noise(201, 0.75)
    img = Image.new("RGBA", (N, N))
    px = img.load()
    rng = random.Random(202)
    cracks = {(x, y) for x, y in ((3, 4), (4, 5), (4, 6), (5, 7), (11, 11), (12, 11), (13, 12))}
    for y in range(N):
        for x in range(N):
            n = noise[y][x]
            v = 3 if n < 0.3 else 4 if n < 0.82 else 5
            if (x + 2 * y) % 11 == 0 and rng.random() < 0.5:
                v -= 1                                   # Kellenspur
            if (x, y) in cracks:
                v = 1
            px[x, y] = PLASTER[v]
    return img


CLAY = pal("#6e4c2c", "#80593a", "#936848", "#a27654", "#b08562", "#bf9672")


def clay_plaster() -> Image.Image:
    """Lehmputz: warmes Ocker mit eingearbeiteten Strohfasern."""
    noise = soft_noise(211, 0.7)
    img = Image.new("RGBA", (N, N))
    px = img.load()
    for y in range(N):
        for x in range(N):
            n = noise[y][x]
            v = 2 if n < 0.18 else 3 if n < 0.55 else 4 if n < 0.9 else 5
            px[x, y] = CLAY[v]
    straw = hexc("#d8bc7c")
    straw_dark = hexc("#a88a4e")
    for x0, y0, dx, length in ((2, 3, 1, 4), (9, 1, 1, 3), (11, 7, -1, 4), (4, 11, 1, 3), (12, 13, 1, 3), (6, 7, 1, 2)):
        for k in range(length):
            px[(x0 + k * dx) % N, (y0 + (k // 2)) % N] = straw if k % 3 else straw_dark
    return img


# =============================================================================
# Bleiglas
# =============================================================================

LEAD = hexc("#33373e")
LEAD_HI = hexc("#666b74")


def leaded_glass() -> Image.Image:
    """Bleiglasfenster: Rautenmuster aus Bleiruten, leicht grünlich-klares Glas mit Lichtreflexen."""
    img = Image.new("RGBA", (N, N), CLEAR)
    px = img.load()
    for y in range(N):
        for x in range(N):
            if x in (0, 15) or y in (0, 15):
                px[x, y] = LEAD_HI if (x == 0 or y == 0) else LEAD
                continue
            d1 = (x + y) % 8
            d2 = (x - y) % 8
            if d1 == 0 or d2 == 0:
                px[x, y] = LEAD
                continue
            if d1 == 1 or d2 == 1:
                px[x, y] = (88, 94, 102, 255) if (x + y) % 2 else LEAD_HI
                continue
            # Glas: Raute erkennen (für leichte Farbunterschiede)
            cell = ((x + y) // 8, (x - y) // 8)
            tint = (cell[0] * 3 + cell[1]) % 3
            base = [(198, 226, 222), (206, 230, 236), (214, 228, 214)][tint]
            alpha = 92
            if d1 in (2, 3) and d2 in (5, 6):
                base = (240, 250, 250)                 # Reflex
                alpha = 150
            px[x, y] = (*base, alpha)
    return img


def leaded_glass_pane_top() -> Image.Image:
    img = Image.new("RGBA", (N, N), CLEAR)
    px = img.load()
    for y in range(N):
        for x in range(N):
            px[x, y] = LEAD_HI if x in (7,) else LEAD
    return img


# =============================================================================
# Burgtür
# =============================================================================

DOOR_COLORS = {
    "o": hexc("#1c120a"), "a": hexc("#3a2414"), "b": hexc("#4e321c"), "c": hexc("#5e3e24"), "d": hexc("#704c2e"),
    "i": hexc("#26282c"), "I": hexc("#4a4e55"), "j": hexc("#7a8088"), "J": hexc("#a8aeb6"),
}

CASTLE_DOOR_TOP = [
    "IjjjjjjjjjjjjjjI",
    "icbaodcbaodcbaoi",
    "icbaodcbaodcbaoi",
    "icbaoiiiiiicbaoi",
    "icbaoi  J  ibaoi",
    "icbaoi  J  ibaoi",
    "icbaoiJJjJJibaoi",
    "icbaoi  J  ibaoi",
    "icbaoiiiiiicbaoi",
    "icbaodcbaodcbaoi",
    "IjjJjjjJjjjjJjjI",
    "iiiiiiiiiiiiiiii",
    "icbaodcbaodcbaoi",
    "icbaodcbaodcbaoi",
    "icbaodcbaodcbaoi",
    "icbaodcbaodcbaoi",
]

CASTLE_DOOR_BOTTOM = [
    "icbaodcbaodcbaoi",
    "IjjJjjjJjjjjJjjI",
    "iiiiiiiiiiiiiiii",
    "icbaodcbaodcbaoi",
    "icbaodcbaodcbaoi",
    "icbaodcbaodIjjIi",
    "icbaodcbaodjbaJi",
    "icbaodcbaodjbaJi",
    "icbaodcbaodIJJIi",
    "icbaodcbaodcbaoi",
    "icbaodcbaodcbaoi",
    "IjjJjjjJjjjjJjjI",
    "iiiiiiiiiiiiiiii",
    "icbaodcbaodcbaoi",
    "icbaodcbaodcbaoi",
    "iiiiiiiiiiiiiiii",
]

CASTLE_DOOR_ITEM = [
    "....IjjjjjjI....",
    "....icbacbai....",
    "....icIiiiai....",
    "....ic JJ ai....",
    "....icIiiiai....",
    "....IjjJjjjI....",
    "....icbacbai....",
    "....icbacbai....",
    "....IjjJjjjI....",
    "....icbacbIi....",
    "....icbacbJi....",
    "....icbacbIi....",
    "....IjjJjjjI....",
    "....icbacbai....",
    "....iiiiiiii....",
    "................",
]

# =============================================================================
# Fensterläden (Falltüren)
# =============================================================================

HEART = {(6, 4), (7, 4), (9, 4), (10, 4), (6, 5), (7, 5), (8, 5), (9, 5), (10, 5),
         (6, 6), (7, 6), (8, 6), (9, 6), (10, 6), (7, 7), (8, 7), (9, 7), (8, 8)}


def shutter(main: str, light: str, dark: str, shade: str) -> Image.Image:
    """Fensterladen: senkrechte Bretter, zwei Querleisten, ausgesägtes Herz."""
    img = Image.new("RGBA", (N, N), CLEAR)
    px = img.load()
    c_main, c_light, c_dark, c_shade = hexc(main), hexc(light), hexc(dark), hexc(shade)
    for y in range(N):
        for x in range(N):
            if x in (0, 15) or y in (0, 15):
                px[x, y] = c_dark
            elif y in (1, 12):
                px[x, y] = c_light                      # Querleisten, oben hell
            elif y in (2, 13):
                px[x, y] = c_main
            elif y in (3, 14):
                px[x, y] = c_shade                      # Schatten unter der Leiste
            elif x in (4, 8, 12):
                px[x, y] = c_dark                       # Fugen zwischen den Brettern
            else:
                px[x, y] = c_light if x in (1, 5, 9, 13) else c_main
    for x, y in HEART:
        px[x, y] = CLEAR
    for x, y in ((5, 4), (5, 5), (5, 6), (6, 7), (7, 8)):
        px[x, y] = c_shade                              # Kante des Ausschnitts
    return img


# =============================================================================
# Laternen (Aufteilung wie die Vorlage template_lantern)
# =============================================================================

def lantern(frame: list[Color], glow: list[Color]) -> Image.Image:
    """frame: 4 Töne dunkel->hell, glow: 4 Töne (Rand->Mitte)."""
    img = Image.new("RGBA", (N, N), CLEAR)
    px = img.load()
    f0, f1, f2, f3 = frame
    g0, g1, g2, g3 = glow
    # Kappe (u1-5, v0-2)
    for x in range(1, 5):
        px[x, 0] = f3 if x in (1, 2) else f2
        px[x, 1] = f1
    # Körper-Seiten (u0-6, v2-9): Rahmen und Glas mit hellem Kern
    for y in range(2, 9):
        for x in range(0, 6):
            if x in (0, 5) or y in (2, 8):
                px[x, y] = f2 if (x == 0 or y == 2) else f0
            else:
                core = (x in (2, 3)) and (4 <= y <= 6)
                px[x, y] = g3 if core else (g2 if 3 <= y <= 7 and x in (1, 4) else g1)
                if y == 3 and x in (1, 2):
                    px[x, y] = g2
    # Deckel/Boden (u0-6, v9-15), Kappe oben (u1-5, v10-14)
    for y in range(9, 15):
        for x in range(0, 6):
            edge = x in (0, 5) or y in (9, 14)
            px[x, y] = f0 if edge else (f2 if (x + y) % 3 else f1)
    for y in range(10, 14):
        for x in range(1, 5):
            px[x, y] = f3 if (x, y) in ((1, 10), (2, 10), (1, 11)) else f2
    # Henkel (u11-14, v1-3) und Kette (u11-14, v6-12)
    for x in range(11, 14):
        px[x, 1] = f2
    px[11, 2] = f1
    px[13, 2] = f1
    for y in range(6, 12):
        px[12, y] = f2 if y % 2 == 0 else f1
        if y % 2 == 1:
            px[11, y] = f0
            px[13, y] = f0
    return img


LANTERN_ITEM = [
    "................",
    "......jJj.......",
    ".....j...j......",
    "......jjj.......",
    ".....iIIIi......",
    "....iIjjjIi.....",
    "....IggYggI.....",
    "....IgYWYgI.....",
    "....IgYWYgI.....",
    "....IggYggI.....",
    "....IgggggI.....",
    "....iIjjjIi.....",
    ".....iiiii......",
    "................",
    "................",
    "................",
]


def lantern_item(frame: list[Color], glow: list[Color]) -> Image.Image:
    f0, f1, f2, f3 = frame
    g0, g1, g2, g3 = glow
    return sprite(LANTERN_ITEM, {"i": f0, "I": f1, "j": f2, "J": f3, "g": g1, "Y": g2, "W": g3})


# =============================================================================
# Kronleuchter (Atlas für das eigene Modell)
# =============================================================================

def chandelier() -> Image.Image:
    """Bereiche: Eisen (0-8, 0-8), Kerzenwachs (8-12, 0-8), Flamme (12-16, 0-4), Gold (12-16, 4-8)."""
    img = Image.new("RGBA", (N, N), CLEAR)
    px = img.load()
    iron = pal("#1e2024", "#2e3136", "#43474e", "#5c616a")
    for y in range(8):
        for x in range(8):
            px[x, y] = iron[2] if (x + y) % 5 else iron[3]
            if y in (0,):
                px[x, y] = iron[3]
            if y == 7 or x == 7:
                px[x, y] = iron[1]
    wax = pal("#cbb88e", "#e2d3ae", "#f0e6c8", "#fbf5e2")
    for y in range(8):
        for x in range(8, 12):
            px[x, y] = wax[3] if x == 8 else wax[2] if x == 9 else wax[1]
            if y == 0:
                px[x, y] = wax[3]
    px[10, 2] = wax[3]
    px[10, 3] = wax[3]                                  # Wachstropfen
    flame = {(13, 0): "#fff6c0", (14, 0): "#fff6c0", (13, 1): "#ffd24a", (14, 1): "#ffe680",
             (12, 2): "#ff9a22", (13, 2): "#ffc23a", (14, 2): "#ffd24a", (15, 2): "#ff9a22",
             (12, 3): "#e06010", (13, 3): "#ff9a22", (14, 3): "#ff9a22", (15, 3): "#e06010"}
    for (x, y), c in flame.items():
        px[x, y] = hexc(c)
    gold = pal("#7a5212", "#b8862a", "#e6b84a", "#fbe08a")
    for y in range(4, 8):
        for x in range(12, 16):
            px[x, y] = gold[3] if (x, y) == (12, 4) else gold[2] if (x + y) % 2 else gold[1]
    return img


CHANDELIER_ITEM = [
    "................",
    ".......ii.......",
    ".......ii.......",
    "..Y....ii....Y..",
    "..y....ii....y..",
    "..w...iggi...w..",
    "..w..iiGGii..w..",
    ".iIiiiiGGiiiiIi.",
    ".iiIIIIiiIIIIii.",
    "..i..........i..",
    "................",
    "................",
    "................",
    "................",
    "................",
    "................",
]


def build(put: Callable[[str, Image.Image], None]) -> None:
    put("block/white_plaster", white_plaster())
    put("block/clay_plaster", clay_plaster())
    put("block/leaded_glass", leaded_glass())
    put("block/leaded_glass_pane_top", leaded_glass_pane_top())
    put("block/castle_door_top", sprite(CASTLE_DOOR_TOP, DOOR_COLORS))
    put("block/castle_door_bottom", sprite(CASTLE_DOOR_BOTTOM, DOOR_COLORS))
    put("item/castle_door", sprite(CASTLE_DOOR_ITEM, DOOR_COLORS))
    put("block/green_shutters", shutter("#2f6a3a", "#3f8a4c", "#163a1e", "#24502c"))
    put("block/red_shutters", shutter("#8e2a22", "#b23a2e", "#4a120e", "#6e1e18"))
    steel = pal("#23272e", "#3e454f", "#6a7380", "#a3adba")
    warm = pal("#8a4a10", "#e0901e", "#ffc84a", "#fff2b0")
    dark_iron = pal("#141518", "#25272c", "#3c3f46", "#5e626b")
    cyan = pal("#0e5a6a", "#1ca8c0", "#5ee6f4", "#e0ffff")
    put("block/steel_lantern", lantern(steel, warm))
    put("item/steel_lantern", lantern_item(steel, warm))
    put("block/aetherit_lantern", lantern(dark_iron, cyan))
    put("item/aetherit_lantern", lantern_item(dark_iron, cyan))
    put("block/chandelier", chandelier())
    put("item/chandelier", sprite(CHANDELIER_ITEM, {
        "i": hexc("#2e3136"), "I": hexc("#5c616a"), "g": hexc("#b8862a"), "G": hexc("#e6b84a"),
        "w": hexc("#f0e6c8"), "y": hexc("#ffc23a"), "Y": hexc("#fff6c0")}))


if __name__ == "__main__":
    import sys

    sheet: dict[str, Image.Image] = {}
    build(lambda key, img: sheet.__setitem__(key, img))
    zoom, pad, cols = 8, 4, 6
    cell = 16 * zoom + pad
    rows = (len(sheet) + cols - 1) // cols
    out = Image.new("RGBA", (cols * cell + pad, rows * cell + pad), (60, 64, 70, 255))
    for i, (key, img) in enumerate(sheet.items()):
        tile = pa.checker(16, 16, (150, 150, 150, 255), (128, 128, 128, 255))
        tile.alpha_composite(img)
        out.alpha_composite(pa.zoom(tile, zoom), (pad + (i % cols) * cell, pad + (i // cols) * cell))
    target = sys.argv[1] if len(sys.argv) > 1 else "/tmp/claude-0/-home-claude/50786bb5-6cc3-5907-a25e-13bee43305bb/scratchpad/building_tex.png"
    out.save(target)
    print(f"{len(sheet)} Texturen -> {target}")
