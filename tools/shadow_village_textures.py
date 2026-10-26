#!/usr/bin/env python3
"""
Texturen rund um die Dörfer der Umbrer:

  * block/shadow_door_top, _bottom, item/shadow_door – Tür aus Schattenholz mit Mondsilberbeschlägen und einem
    Fenster in Form einer Mondsichel
  * block/shadow_trapdoor              – Falltür mit Mondsichel-Fenster
  * block/shadow_farmland(_moist)      – Schattenackerland (Furchen), feucht dunkler und glänzend
  * block/shadow_path_top, _side       – festgetretener Schattenpfad
  * block/duskgrain_stage0..3          – Dämmerkorn vom Keimling bis zur reifen, violett-goldenen Ähre
  * item/duskgrain_seeds, item/duskgrain, item/dusk_bread

Wird von tools/generate_textures.py aufgerufen (build(put)).
"""
from __future__ import annotations

import math
import random
from typing import Callable

from PIL import Image

import pixelart as pa
import shadow_textures as st
from pixelart import CLEAR, hexc

N = 16
SILVER = pa.palette("#4a5470", "#8a96b4", "#c4d0ec", "#eef4ff")
GLASS = (150, 170, 230, 110)


# =============================================================================
# Tür und Falltür
# =============================================================================

def _boards(seed: int, vertical: bool = True) -> Image.Image:
    """Senkrechte (oder waagrechte) Bretter aus Schattenholz mit Maserung."""
    noise = pa.fractal_noise(seed, ((8, 0.4), (4, 0.4), (2, 0.2)))
    img = pa.new()
    px = img.load()
    for y in range(N):
        for x in range(N):
            a, b = (x, y) if vertical else (y, x)
            board = a // 4
            if a % 4 == 3:
                px[x, y] = st.WOOD[1]
                continue
            grain = math.sin(2 * math.pi * (b + board * 5) / N * 2 + a * 0.5 + noise[y][x] * 2.0)
            v = 0.42 + 0.15 * grain + 0.2 * (noise[y][x] - 0.5) + (0.08 if a % 4 == 0 else 0)
            px[x, y] = pa.pick_dither(st.WOOD[2:7], v, x, y, 0.5)
    return img


def _frame(img: Image.Image, top: bool, bottom: bool) -> None:
    px = img.load()
    for i in range(N):
        px[0, i] = st.WOOD[1]
        px[N - 1, i] = st.WOOD[1]
    if top:
        for i in range(N):
            px[i, 0] = st.WOOD[1]
    if bottom:
        for i in range(N):
            px[i, N - 1] = st.WOOD[1]


def _band(img: Image.Image, y: int) -> None:
    """Mondsilber-Beschlag quer über die Tür mit zwei Nieten."""
    px = img.load()
    for x in range(1, N - 1):
        px[x, y] = SILVER[1]
        px[x, y + 1] = SILVER[0]
    for x in (2, N - 3):
        px[x, y] = SILVER[3]


def _crescent(px, cx: float, cy: float, r: float, color, cut: float = 0.55) -> None:
    """Mondsichel: großer Kreis minus versetzter Kreis."""
    for y in range(N):
        for x in range(N):
            dx, dy = x + 0.5 - cx, y + 0.5 - cy
            if dx * dx + dy * dy <= r * r:
                ex, ey = dx - r * cut, dy - r * 0.15
                if ex * ex + ey * ey > (r * 0.82) ** 2:
                    px[x, y] = color


def shadow_door_top() -> Image.Image:
    img = _boards(41001)
    px = img.load()
    # Fenster: ein Rahmen aus Mondsilber mit einer Mondsichel aus Glas, der Rest durchsichtig
    for y in range(3, 11):
        for x in range(3, 13):
            edge = y in (3, 10) or x in (3, 12)
            px[x, y] = SILVER[1] if edge else CLEAR
    _crescent(px, 8.0, 7.0, 3.2, GLASS)
    for x, y in ((4, 4), (11, 4), (4, 9), (11, 9)):
        px[x, y] = SILVER[2]
    _band(img, 13)
    _frame(img, True, False)
    return img


def shadow_door_bottom() -> Image.Image:
    img = _boards(41002)
    px = img.load()
    _band(img, 2)
    _band(img, 11)
    # Griff
    px[12, 6] = SILVER[2]
    px[12, 7] = SILVER[3]
    px[11, 7] = SILVER[1]
    _frame(img, False, True)
    return img


def shadow_door_item() -> Image.Image:
    top = shadow_door_top()
    bottom = shadow_door_bottom()
    img = pa.new()
    # Zusammengestaucht: obere und untere Hälfte, je 8 Zeilen breit 10
    t = top.resize((10, 8), Image.NEAREST)
    b = bottom.resize((10, 8), Image.NEAREST)
    img.paste(t, (3, 0))
    img.paste(b, (3, 8))
    return img


def shadow_trapdoor() -> Image.Image:
    img = _boards(41003, vertical=False)
    px = img.load()
    for i in range(N):
        px[i, 0] = st.WOOD[1]
        px[i, N - 1] = st.WOOD[1]
        px[0, i] = st.WOOD[1]
        px[N - 1, i] = st.WOOD[1]
    # Fenster mit Mondsichel in der Mitte
    for y in range(4, 12):
        for x in range(4, 12):
            edge = y in (4, 11) or x in (4, 11)
            px[x, y] = SILVER[1] if edge else CLEAR
    _crescent(px, 8.0, 8.0, 2.6, GLASS)
    for x, y in ((2, 2), (13, 2), (2, 13), (13, 13)):
        px[x, y] = SILVER[2]
    return img


# =============================================================================
# Ackerland und Pfad
# =============================================================================

def _farmland(moist: bool) -> Image.Image:
    noise = pa.fractal_noise(41011, ((8, 0.4), (4, 0.4), (2, 0.2)))
    img = pa.new()
    px = img.load()
    pal = st.SOIL if not moist else [(round(c[0] * 0.72), round(c[1] * 0.72), round(c[2] * 0.8), c[3]) for c in st.SOIL]
    for y in range(N):
        for x in range(N):
            furrow = y % 4
            v = 0.3 + noise[y][x] * 0.4
            if furrow == 0:
                v -= 0.25                       # Rinne
            elif furrow == 2:
                v += 0.18                       # Kamm
            if moist and furrow == 0 and (x + y) % 5 == 0:
                px[x, y] = hexc("#4a3a6a")      # nasser Glanz
                continue
            px[x, y] = pa.pick_dither(pal[1:7], v, x, y, 0.6)
    return img


def shadow_farmland() -> Image.Image:
    return _farmland(False)


def shadow_farmland_moist() -> Image.Image:
    return _farmland(True)


def shadow_path_top() -> Image.Image:
    noise = pa.fractal_noise(41021, ((8, 0.5), (4, 0.3), (2, 0.2)))
    rng = random.Random(41022)
    img = pa.new()
    px = img.load()
    for y in range(N):
        for x in range(N):
            v = 0.45 + (noise[y][x] - 0.5) * 0.5
            px[x, y] = pa.pick_dither(st.SOIL[2:7], v, x, y, 0.5)
    for _ in range(9):                          # festgetretene Kiesel
        x, y = rng.randrange(N), rng.randrange(N)
        px[x, y] = st.PEBBLE[rng.randrange(3)]
    return img


def shadow_path_side() -> Image.Image:
    soil = st.shadow_soil()
    top = shadow_path_top()
    img = soil.copy()
    px, tp = img.load(), top.load()
    for x in range(N):
        depth = 2 + (1 if (x * 7) % 5 == 0 else 0)
        for y in range(depth):
            px[x, y] = tp[x, (y + 5) % N]
        px[x, depth] = st.SOIL[1]
    return img


# =============================================================================
# Dämmerkorn
# =============================================================================

GRAIN = {"s": hexc("#2e3a5a"), "S": hexc("#4a5e88"), "t": hexc("#5a4a8a"), "e": hexc("#8a6ac0"), "E": hexc("#c8a8f0"),
         "g": hexc("#b89a4a"), "G": hexc("#f0d890"), "w": hexc("#fff4d0")}

DUSKGRAIN = [
    [   # Keimling
        "", "", "", "", "", "", "", "", "", "", "",
        "....s.....s.....",
        "....s..s..s..s..",
        "...sS..s.sS..s..",
        "...s..sS.s...sS.",
        "...s..s..s...s..",
    ],
    [   # junge Halme
        "", "", "", "", "", "",
        "...s......s.....",
        "...s..s...s..s..",
        "..sS..s..sS..s..",
        "..s..sS..s...sS.",
        "..s..s...s...s..",
        "..s..s..sS..s...",
        ".sS..s..s...s...",
        ".s..sS..s..sS...",
        ".s..s...s..s....",
        ".s..s...s..s....",
    ],
    [   # Ähren schieben (noch violett)
        "", "",
        "...t......t.....",
        "...e..t...e..t..",
        "...e..e...e..e..",
        "..sS..e..sS..e..",
        "..s..sS..s...sS.",
        "..s..s...s...s..",
        "..s..s..sS..s...",
        ".sS..s..s...s...",
        ".s..sS..s..sS...",
        ".s..s...s..s....",
        ".s..s...s..s....",
        ".s..s...s..s....",
        ".s..s...s..s....",
        ".s..s...s..s....",
    ],
    [   # reif: violett-goldene, schimmernde Ähren
        "...w......w.....",
        "..gG..w..gG..w..",
        "..Eg.gG..Eg.gG..",
        "..gE.Eg..gE.Eg..",
        "..Eg.gE..Eg.gE..",
        "..eS.Eg.eeS.gE..",
        "..s..eS..s..eS..",
        "..s..s...s...s..",
        "..s..s..sS..s...",
        ".sS..s..s...s...",
        ".s..sS..s..sS...",
        ".s..s...s..s....",
        ".s..s...s..s....",
        ".s..s...s..s....",
        ".s..s...s..s....",
        ".s..s...s..s....",
    ],
]


def duskgrain_stage(stage: int) -> Image.Image:
    rows = DUSKGRAIN[stage]
    rows = [""] * (N - len(rows)) + rows if len(rows) < N else rows
    return pa.sprite(rows, GRAIN)


SEEDS = [
    "", "", "", "", "",
    "......e.........",
    "....e..E...e....",
    "...eE....e.Ee...",
    "......eE...e....",
    "....e....Ee.....",
    "...Ee..e....e...",
    "......eE..eE....",
    "........e.......",
]

GRAIN_ITEM = [
    "..........w.....",
    ".........gGw....",
    "........gGEg....",
    ".......EgGg.....",
    "......gEgE......",
    ".....EgGg.......",
    "....gEgE........",
    "...eSgE.........",
    "..eS.e..........",
    ".sS.............",
    "sS..............",
    "s...............",
]

BREAD = [
    "", "", "", "",
    "....oooooooo....",
    "..oobbBbbBbboo..",
    ".obBbbbBbbbBbbo.",
    "obbbBbbbBbbbbBbo",
    "obbbbbBbbbbBbbbo",
    "odbbbbbbbbbbbbdo",
    ".oddbbbbbbbbddo.",
    "..ooddddddddoo..",
    "....oooooooo....",
]


def build(put: Callable[[str, Image.Image], None]) -> None:
    put("block/shadow_door_top", shadow_door_top())
    put("block/shadow_door_bottom", shadow_door_bottom())
    put("item/shadow_door", shadow_door_item())
    put("block/shadow_trapdoor", shadow_trapdoor())
    put("block/shadow_farmland", shadow_farmland())
    put("block/shadow_farmland_moist", shadow_farmland_moist())
    put("block/shadow_path_top", shadow_path_top())
    put("block/shadow_path_side", shadow_path_side())
    for stage in range(4):
        put(f"block/duskgrain_stage{stage}", duskgrain_stage(stage))
    put("item/duskgrain_seeds", pa.sprite(SEEDS, GRAIN))
    put("item/duskgrain", pa.sprite(GRAIN_ITEM, GRAIN))
    put("item/dusk_bread", pa.sprite(BREAD, {"o": hexc("#2a1626"), "b": hexc("#6a3e5a"), "B": hexc("#a8709a"),
                                             "d": hexc("#4a2a40")}))


if __name__ == "__main__":
    import sys

    out: dict[str, Image.Image] = {}
    build(lambda k, v: out.__setitem__(k, v))
    zoom, pad, cols = 8, 6, 6
    cell = N * zoom + pad
    rows = math.ceil(len(out) / cols)
    sheet = Image.new("RGBA", (cols * cell + pad, rows * cell + pad), (60, 62, 70, 255))
    for i, img in enumerate(out.values()):
        sheet.alpha_composite(img.resize((N * zoom, N * zoom), Image.NEAREST), (pad + (i % cols) * cell, pad + (i // cols) * cell))
    sheet.save(sys.argv[1] if len(sys.argv) > 1 else "shadow_village_preview.png")
    print(len(out), "Texturen")
