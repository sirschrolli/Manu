#!/usr/bin/env python3
"""
Grafiken des Angelns (seit 0.79; eigene Pixelkunst, alles hier gezeichnet):

  gui/fishing/minigame   (512 × 256) der Drill: drei Säulen-Rahmen (Wasser: Treibholz mit Messing · Lava: Schwarzstein mit Gold und
                         Glutrissen · Leere: Endstein mit Purpur), ihr Inneres, die ziehenden Schleier (Blasen, Funken,
                         Sterne), die Fangzone, die Leiste des Fortschritts, die Truhe, die Karte des Fischs und das Band zum
                         Abschluss
  gui/fishing/book       (512 × 256) das Fischbuch: der aufgeschlagene Band in Seeleder, Fächer für die Arten, Reiter der
                         Kapitel mit ihren Zeichen, Sterne, Symbole

    python3 tools/fishing_gui.py            # schreibt nach src/main/resources/assets/kingdomsatwar/textures/gui/fishing
    python3 tools/fishing_gui.py ORDNER     # schreibt stattdessen nach ORDNER (zum Ansehen)
"""
from __future__ import annotations

import math
import os
import random
import sys

from PIL import Image

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import pixelart as pa  # noqa: E402
from pixelart import hexc, mix  # noqa: E402

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
OUT = os.path.join(ROOT, "src/main/resources/assets/kingdomsatwar/textures/gui/fishing")
CLEAR = (0, 0, 0, 0)

# Maße des Drills (auch in client/fishing/FishingScreen.java)
FRAME_W, FRAME_H = 46, 182
COL_X, COL_Y, COL_W, COL_H = 10, 22, 26, 150


def pal(*values: str):
    return [hexc(v) for v in values]


THEMES = {
    # Material des Rahmens (dunkel -> hell), Beschläge (dunkel -> hell), Glut in Fugen/Rissen (oder None)
    "water": dict(body=pal("#1c120b", "#2e1e12", "#44301c", "#5c4226", "#765832", "#8e6e40"),
                  trim=pal("#4a3410", "#7a5a1c", "#b08a2c", "#dcb84a", "#f8e08a"), glow=None, wood=True),
    "lava": dict(body=pal("#0c0a0e", "#18151c", "#26222c", "#36313e", "#474252", "#5a5466"),
                 trim=pal("#5a2c08", "#96500e", "#d08618", "#f4bc3a", "#ffe890"), glow=pal("#7a1804", "#d84a0c", "#ff9a1e", "#ffe070"), wood=False),
    "void": dict(body=pal("#3a3a26", "#5a5a3c", "#7c7c54", "#a0a070", "#c2c28e", "#dedeb0"),
                 trim=pal("#2a1040", "#4c2274", "#7c3cb0", "#b06ae0", "#e0b0ff"), glow=pal("#3a1466", "#8a3cd0", "#c88aff", "#f0d8ff"), wood=False),
}


def h01(*a) -> float:
    h = 2166136261
    for v in a:
        h = ((h ^ (int(v) & 0xFFFFFFFF)) * 16777619) & 0xFFFFFFFF
    h ^= h >> 13
    h = (h * 1274126177) & 0xFFFFFFFF
    return (h & 0xFFFF) / 0xFFFF


# =============================================================================================
# Rahmen der Säule
# =============================================================================================

def material(theme: dict, x: int, y: int, seed: int, base: float) -> tuple:
    """Farbe des Rahmenmaterials an einer Stelle (base: Helligkeit 0..1 aus Form und Licht)."""
    body = theme["body"]
    if theme["wood"]:
        # Maserung: lange senkrechte Fasern, hin und wieder ein Astloch
        grain = h01(x * 7 + seed, (y + int(h01(x, seed) * 9)) // 7) * 0.22 - 0.11
        streak = -0.12 if h01(x, seed + 5) < 0.22 else 0.0
        v = base + grain + streak
    else:
        v = base + (h01(x, y, seed) - 0.5) * 0.16
    return pa.pick_dither(body, max(0.0, min(0.999, v)), x, y, 0.55)


def post(img: Image.Image, theme: dict, x0: int, y0: int, w: int, h: int, seed: int, joints: int) -> None:
    """Ein senkrechter Balken (oder Pfeiler aus Steinen) mit Kante im Licht (links oben) und im Schatten."""
    px = img.load()
    for y in range(y0, y0 + h):
        for x in range(x0, x0 + w):
            rx = x - x0
            base = 0.52
            if rx == 0:
                base = 0.82
            elif rx == 1:
                base = 0.66
            elif rx == w - 1:
                base = 0.16
            elif rx == w - 2:
                base = 0.34
            if not theme["wood"] and joints:
                jy = (y - y0) % joints
                if jy == 0:
                    base = 0.12
                elif jy == 1:
                    base += 0.2
                elif jy == joints - 1:
                    base -= 0.14
            px[x, y] = material(theme, x, y, seed, base)
    # Risse mit Glut (Schwarzstein, Endstein): kurze Zickzacklinien
    if theme["glow"]:
        rng = random.Random(seed * 13 + 1)
        for _ in range(max(1, h // 26)):
            cx, cy = x0 + rng.randrange(2, w - 2), y0 + rng.randrange(6, h - 8)
            for k in range(rng.randrange(3, 6)):
                if x0 + 1 <= cx < x0 + w - 1 and y0 <= cy < y0 + h:
                    px[cx, cy] = theme["glow"][2 if k % 2 else 1]
                cy += 1
                cx += rng.choice((-1, 0, 1))


def beam(img: Image.Image, theme: dict, x0: int, y0: int, w: int, h: int, seed: int) -> None:
    """Ein waagerechter Balken."""
    px = img.load()
    for y in range(y0, y0 + h):
        for x in range(x0, x0 + w):
            ry = y - y0
            base = 0.52
            if ry == 0:
                base = 0.84
            elif ry == 1:
                base = 0.66
            elif ry == h - 1:
                base = 0.14
            elif ry == h - 2:
                base = 0.34
            if x == x0:
                base = max(base, 0.74)
            elif x == x0 + w - 1:
                base = min(base, 0.2)
            if theme["wood"]:
                grain = h01((x + int(h01(y, seed) * 9)) // 7, y * 5 + seed) * 0.22 - 0.11
                v = base + grain
                c = pa.pick_dither(theme["body"], max(0.0, min(0.999, v)), x, y, 0.55)
            else:
                if (x - x0) % 11 == (5 if ry < h // 2 else 0):
                    base = 0.14
                c = material(theme, x, y, seed, base)
            px[x, y] = c


def band(img: Image.Image, theme: dict, x0: int, y0: int, w: int, h: int = 3, rivets: bool = True) -> None:
    """Ein Beschlag aus Metall mit Nieten."""
    px = img.load()
    trim = theme["trim"]
    for y in range(y0, y0 + h):
        for x in range(x0, x0 + w):
            ry = y - y0
            c = trim[3] if ry == 0 else trim[1] if ry == h - 1 else trim[2]
            if x == x0:
                c = trim[4]
            elif x == x0 + w - 1:
                c = trim[0]
            px[x, y] = c
    if rivets and h >= 3:
        for x in range(x0 + 2, x0 + w - 1, 4):
            px[x, y0 + 1] = trim[4]
            if x + 1 < x0 + w:
                px[x + 1, y0 + 1] = trim[0]


def outline_shape(img: Image.Image, color: tuple) -> None:
    """Dunkle Linie um alles Gezeichnete (außen)."""
    src = img.copy().load()
    px = img.load()
    w, h = img.size
    for y in range(h):
        for x in range(w):
            if src[x, y][3]:
                continue
            for nx, ny in ((x - 1, y), (x + 1, y), (x, y - 1), (x, y + 1)):
                if 0 <= nx < w and 0 <= ny < h and src[nx, ny][3]:
                    px[x, y] = color
                    break


def crest(img: Image.Image, name: str, theme: dict, cx: int, y0: int) -> None:
    """Das Zeichen auf dem Kopfbalken: Welle mit Muschel · Flamme · Stern der Leere."""
    px = img.load()
    trim = theme["trim"]
    if name == "water":
        rows = [
            "......LLLL......",
            "....LLHHHMLL....",
            "...LHHMHMHMML...",
            "..LHMHMHMHMHDL..",
            "..LHMHMHMHMHDL..",
            ".LMHMHMHMHMHMDL.",
            ".LMHMHMHMHMHMDL.",
            ".LMDMDMDMDMDMDL.",
            "..LDDDDDDDDDDL..",
            "...LLDDDDDDLL...",
            ".....LMMMML.....",
        ]
        colors = {"L": trim[0], "D": trim[1], "M": trim[2], "H": trim[3]}
    elif name == "lava":
        rows = [
            ".......H........",
            "......HY........",
            "......YYH.......",
            ".....HYOY..H....",
            "..H..YOOYH.Y....",
            "..YH.YORYYHY....",
            "..YYHOORROYYH...",
            ".HYOYORRROOYY...",
            ".YOORRRDRROOY...",
            ".YORRDDDDRROY...",
            "..ORRDDDDRRO....",
        ]
        g = theme["glow"]
        colors = {"H": g[3], "Y": g[2], "O": g[1], "R": g[0], "D": hexc("#3a0a02")}
    else:
        rows = [
            ".......H........",
            ".......H........",
            "......HWH.......",
            "..H...HWH...H...",
            "...HH.MWM.HH....",
            "....MMWWWMM.....",
            ".HHWWWWDWWWWHH..",
            "....MMWWWMM.....",
            "...HH.MWM.HH....",
            "..H...HWH...H...",
            "......HWH.......",
        ]
        g = theme["glow"]
        colors = {"H": g[1], "M": g[2], "W": g[3], "D": hexc("#1a0630")}
    x0 = cx - 8
    for y, row in enumerate(rows):
        for x, ch in enumerate(row):
            if ch in colors:
                px[x0 + x, y0 + y] = colors[ch]


def frame(name: str) -> Image.Image:
    theme = THEMES[name]
    img = Image.new("RGBA", (FRAME_W, FRAME_H), CLEAR)
    seed = sum(map(ord, name))
    left, right = 1, FRAME_W - 9
    # Pfosten
    post(img, theme, left, 14, 8, FRAME_H - 20, seed + 1, 9)
    post(img, theme, right, 14, 8, FRAME_H - 20, seed + 2, 9)
    # Kopf- und Fußbalken (etwas breiter als die Pfosten)
    beam(img, theme, 0, 9, FRAME_W, 11, seed + 3)
    beam(img, theme, 0, FRAME_H - 9, FRAME_W, 9, seed + 4)
    # Beschläge
    band(img, theme, 0, 18, FRAME_W, 3)
    band(img, theme, 0, FRAME_H - 10, FRAME_W, 3)
    for y in (46, 96, 146):
        band(img, theme, left, y, 8, 3)
        band(img, theme, right, y, 8, 3)
    # innere Laibung: eine dunkle Linie um die Säule, darin ein schmaler Lichtstreif rechts unten
    px = img.load()
    dark = mix(theme["body"][0], (0, 0, 0, 255), 0.4)
    for y in range(COL_Y - 1, COL_Y + COL_H + 1):
        px[COL_X - 1, y] = dark
        px[COL_X + COL_W, y] = mix(theme["body"][2], dark, 0.3)
    for x in range(COL_X - 1, COL_X + COL_W + 1):
        px[x, COL_Y - 1] = dark
        px[x, COL_Y + COL_H] = mix(theme["body"][2], dark, 0.3)
    # Zeichen auf dem Kopfbalken, auf einer kleinen Platte
    plate = theme["trim"]
    for y in range(4, 17):
        for x in range(FRAME_W // 2 - 10, FRAME_W // 2 + 10):
            dx = abs(x - (FRAME_W // 2 - 0.5))
            if dx + max(0, 6 - y) * 1.6 > 10.5:
                continue
            c = theme["body"][1]
            if dx + max(0, 6 - y) * 1.6 > 9.2 or y == 16:
                c = plate[2] if y < 10 else plate[1]
            px[x, y] = c
    crest(img, name, theme, FRAME_W // 2, 3)
    outline_shape(img, mix(theme["body"][0], (0, 0, 0, 255), 0.55))
    # die Säule selbst bleibt frei
    for y in range(COL_Y, COL_Y + COL_H):
        for x in range(COL_X, COL_X + COL_W):
            px[x, y] = CLEAR
    return img


# =============================================================================================
# Inneres der Säule und die ziehenden Schleier
# =============================================================================================

def interior(name: str) -> Image.Image:
    img = Image.new("RGBA", (COL_W, COL_H), CLEAR)
    px = img.load()
    noise = pa.fractal_noise(4100 + len(name), ((16, 0.5), (8, 0.3), (4, 0.2)), 32, 160)
    if name == "water":
        ramp = pal("#06122c", "#0a1e44", "#0e2e5e", "#134478", "#1a5e92", "#2478a8", "#3496bc", "#52b4cc")
        for y in range(COL_H):
            depth = y / (COL_H - 1)
            for x in range(COL_W):
                # Lichtbahnen von oben: schräge helle Streifen, die nach unten verblassen
                shaft = max(0.0, math.sin((x + y * 0.22) * 0.55) * 0.5 + 0.2) * (1.0 - depth) ** 1.6 * 0.22
                v = (1.0 - depth) ** 1.15 * 0.88 + shaft + (noise[y][x] - 0.5) * 0.10
                px[x, y] = pa.pick_dither(ramp, max(0.0, min(0.999, v)), x, y, 0.8)
        # Wasserspiegel: helle, leicht gewellte Linie
        for x in range(COL_W):
            wave = 1 if (x // 3) % 2 == 0 else 0
            px[x, wave] = hexc("#d8f4ff")
            px[x, wave + 1] = hexc("#8ad4ec")
            if wave:
                px[x, 0] = hexc("#8ad4ec")
        # Grund: Sand und ein paar Halme
        sand = pal("#6a5c34", "#8e7c48", "#b09c5e", "#c8b672")
        for x in range(COL_W):
            hgt = 4 + int(noise[5][x] * 4)
            for k in range(hgt):
                c = sand[min(3, (hgt - 1 - k) // 2 + (1 if h01(x, k, 3) < 0.3 else 0))]
                px[x, COL_H - 1 - k] = mix(c, hexc("#0e2e5e"), 0.28)
        for (wx, wh) in ((3, 12), (4, 8), (20, 15), (22, 9), (12, 6)):
            for k in range(wh):
                sway = int(math.sin(k * 0.6 + wx) * 1.2)
                c = hexc("#1e6a3a") if k % 3 else hexc("#2e8a4a")
                x = wx + sway
                if 0 <= x < COL_W:
                    px[x, COL_H - 4 - k] = mix(c, hexc("#06122c"), 0.25)
    elif name == "lava":
        ramp = pal("#2a0402", "#4a0a04", "#701206", "#9a2208", "#c43c0a", "#e45e10", "#f48a1c", "#fcb432", "#ffd860")
        cells, border, _ = pa.voronoi(4202, 26, 32, 160)
        for y in range(COL_H):
            depth = y / (COL_H - 1)
            for x in range(COL_W):
                v = (1.0 - depth) ** 0.9 * 0.86 + (noise[y][x] - 0.5) * 0.22
                # dunkle Schollen, dazwischen helle Adern
                if border[y][x] < 1.3:
                    v += 0.14
                elif h01(cells[y][x], 7) < 0.35:
                    v -= 0.16
                px[x, y] = pa.pick_dither(ramp, max(0.0, min(0.999, v)), x, y, 0.8)
        for x in range(COL_W):
            wave = 1 if (x // 4) % 2 == 0 else 0
            px[x, wave] = hexc("#fff4b0")
            px[x, wave + 1] = hexc("#ffd860")
            if wave:
                px[x, 0] = hexc("#ffd860")
        rock = pal("#1a1216", "#2c2226", "#40343a", "#564850")
        for x in range(COL_W):
            hgt = 4 + int(noise[9][x] * 5)
            for k in range(hgt):
                px[x, COL_H - 1 - k] = rock[min(3, (hgt - 1 - k) // 2)] if k < hgt - 1 else hexc("#ff9a1e")
    else:
        ramp = pal("#04020a", "#080414", "#0e0820", "#160c30", "#201242", "#2c1a58")
        neb = pa.fractal_noise(4303, ((32, 0.5), (16, 0.3), (8, 0.2)), 32, 160)
        for y in range(COL_H):
            for x in range(COL_W):
                v = neb[y][x] * 0.75 + (noise[y][x] - 0.5) * 0.2
                c = pa.pick_dither(ramp, max(0.0, min(0.999, v)), x, y, 0.8)
                # ein Hauch Türkis in den dichtesten Wolken
                if neb[y][x] > 0.74:
                    c = mix(c, hexc("#1c5a6a"), (neb[y][x] - 0.74) * 1.6)
                px[x, y] = c
        rng = random.Random(4304)
        for _ in range(34):
            x, y = rng.randrange(COL_W), rng.randrange(COL_H)
            px[x, y] = rng.choice((hexc("#6a5a9a"), hexc("#9a8ad0"), hexc("#4a3a7a")))
        # die Kante der Insel oben: ein paar Brocken Endstein
        stone = pal("#7c7c54", "#a0a070", "#c2c28e")
        for x in range(COL_W):
            hgt = 1 + int(noise[3][x] * 3.2)
            for k in range(hgt):
                px[x, k] = stone[max(0, 2 - k)]
            px[x, hgt] = hexc("#3a3a26")
    return img


def overlay(name: str) -> Image.Image:
    """Ein Schleier, der durch die Säule zieht (kachelbar in der Höhe): Blasen, Funken, Sternenstaub."""
    W, H = COL_W, 64
    img = Image.new("RGBA", (W, H), CLEAR)
    px = img.load()
    rng = random.Random(4400 + len(name))

    def put(x, y, c):
        px[x % W, y % H] = c

    if name == "water":
        for _ in range(9):
            x, y, r = rng.randrange(2, W - 2), rng.randrange(H), rng.choice((1, 1, 2, 2, 3))
            ring, shine = (214, 240, 255, 150), (255, 255, 255, 230)
            if r == 1:
                put(x, y, (200, 232, 255, 140))
            elif r == 2:
                for dx, dy in ((0, 0), (1, 0), (0, 1), (1, 1)):
                    put(x + dx, y + dy, ring)
                put(x, y, shine)
            else:
                for dx, dy in ((1, 0), (2, 0), (0, 1), (3, 1), (0, 2), (3, 2), (1, 3), (2, 3)):
                    put(x + dx, y + dy, ring)
                put(x + 1, y + 1, shine)
    elif name == "lava":
        for _ in range(12):
            x, y = rng.randrange(1, W - 1), rng.randrange(H)
            hot = rng.random() < 0.4
            put(x, y, (255, 244, 170, 240) if hot else (255, 190, 70, 210))
            put(x, y + 1, (255, 140, 30, 130))
            if hot:
                put(x, y - 1, (255, 255, 220, 150))
                put(x - 1, y, (255, 170, 50, 110))
                put(x + 1, y, (255, 170, 50, 110))
    else:
        for _ in range(13):
            x, y = rng.randrange(1, W - 1), rng.randrange(H)
            big = rng.random() < 0.3
            col = rng.choice(((236, 220, 255, 235), (190, 150, 255, 220), (150, 230, 240, 215)))
            put(x, y, col)
            if big:
                dim = (col[0], col[1], col[2], 110)
                for dx, dy in ((-1, 0), (1, 0), (0, -1), (0, 1)):
                    put(x + dx, y + dy, dim)
    return img


# =============================================================================================
# Fangzone, Leiste, Truhe, Karte, Band
# =============================================================================================

def zone(active: bool) -> Image.Image:
    """Die Fangzone, 24 breit: Kappe oben (5), kachelbare Mitte (16), Kappe unten (5) untereinander."""
    W, H = 24, 26
    img = Image.new("RGBA", (W, H), CLEAR)
    px = img.load()
    edge = hexc("#f6ffb0") if active else hexc("#b8f07a")
    mid = hexc("#8cf05a") if active else hexc("#62c84a")
    fill_a = 104 if active else 64

    def body(y, top=False, bottom=False):
        for x in range(W):
            if x in (0, W - 1):
                c = (edge[0], edge[1], edge[2], 235)
            elif x in (1, W - 2):
                c = (mid[0], mid[1], mid[2], 170)
            else:
                shine = 26 if 3 <= x <= 6 else 0
                c = (mid[0], mid[1], mid[2], fill_a + shine)
            px[x, y] = c

    for y in range(5):                       # Kappe oben
        body(y)
    for x in range(W):
        px[x, 0] = (edge[0], edge[1], edge[2], 245) if 2 <= x < W - 2 else CLEAR
        px[x, 1] = (edge[0], edge[1], edge[2], 235) if x in (1, W - 2) else px[x, 1]
    px[0, 0] = px[W - 1, 0] = CLEAR
    px[0, 1] = px[W - 1, 1] = CLEAR
    px[1, 1] = px[W - 2, 1] = (edge[0], edge[1], edge[2], 235)
    for y in range(5, 21):                   # Mitte
        body(y)
        if (y - 5) % 8 == 3:                 # feine Teilstriche an den Rändern
            px[2, y] = px[W - 3, y] = (edge[0], edge[1], edge[2], 190)
    for y in range(21, 26):                  # Kappe unten
        body(y)
    for x in range(W):
        px[x, 25] = (edge[0], edge[1], edge[2], 245) if 2 <= x < W - 2 else CLEAR
    px[0, 24] = px[W - 1, 24] = CLEAR
    px[1, 24] = px[W - 2, 24] = (edge[0], edge[1], edge[2], 235)
    return img


def meter_frame() -> Image.Image:
    """Rahmen der Fortschrittsleiste: Messing, innen ein dunkler Schacht 6 × 150."""
    W, H = 14, 160
    img = Image.new("RGBA", (W, H), CLEAR)
    px = img.load()
    trim = THEMES["water"]["trim"]
    for y in range(H):
        for x in range(W):
            rx, ry = min(x, W - 1 - x), min(y, H - 1 - y)
            if rx + ry < 2:
                continue
            c = trim[2]
            if x <= 1 or y <= 1:
                c = trim[3]
            if x >= W - 2 or y >= H - 2:
                c = trim[1]
            if rx == 0 or ry == 0:
                c = trim[0]
            if 4 <= x < 10 and 5 <= y < 155:
                c = hexc("#0c0a12")
            elif 3 <= x < 11 and 4 <= y < 156:
                c = trim[0]
            px[x, y] = c
    # Marken: Viertel
    for k in (1, 2, 3):
        y = 5 + 150 - int(150 * k / 4)
        px[2, y] = trim[4]
        px[11, y] = trim[4]
    # Nieten
    for y in (2, H - 3):
        px[6, y] = trim[4]
        px[7, y] = trim[0]
    return img


def meter_fill() -> Image.Image:
    """Füllung der Leiste 6 × 150: unten rot, über Bernstein nach oben grün – mit Glanzstreif."""
    W, H = 6, 150
    img = Image.new("RGBA", (W, H), CLEAR)
    px = img.load()
    stops = [(0.0, hexc("#c22a1c")), (0.3, hexc("#e8641a")), (0.55, hexc("#f4c02a")), (0.8, hexc("#9adc3a")), (1.0, hexc("#48d05a"))]
    for y in range(H):
        t = 1.0 - y / (H - 1)
        for (t0, c0), (t1, c1) in zip(stops, stops[1:]):
            if t <= t1:
                c = mix(c0, c1, (t - t0) / (t1 - t0))
                break
        for x in range(W):
            cc = c
            if x == 1:
                cc = mix(c, (255, 255, 255, 255), 0.45)
            elif x == 0:
                cc = mix(c, (255, 255, 255, 255), 0.15)
            elif x == W - 1:
                cc = mix(c, (0, 0, 0, 255), 0.35)
            if y % 15 == 14:
                cc = mix(cc, (0, 0, 0, 255), 0.18)
            px[x, y] = cc
    return img


def chest(open_: bool) -> Image.Image:
    rows_closed = [
        "..OOOOOOOOOO..",
        ".OWWWWWWWWWWO.",
        "OWLLLLLLLLLLDO",
        "OLLLLLLLLLLLDO",
        "OGGGGGYYGGGGGO",
        "OMMMMMYHMMMMDO",
        "OMMMMMYYMMMMDO",
        "OMMMMMMMMMMMDO",
        "OMDMMMMMMMMDDO",
        "OGGGGGGGGGGGGO",
        ".OOOOOOOOOOOO.",
        "..............",
    ]
    rows_open = [
        ".OOOOOOOOOOOO.",
        "OLLLLLLLLLLLDO",
        "OWLLLLLLLLLLDO",
        ".OOOOOOOOOOOO.",
        "OYHYHYYHYHYYHO",
        "OGGGGGYYGGGGGO",
        "OMMMMMYHMMMMDO",
        "OMMMMMYYMMMMDO",
        "OMDMMMMMMMMDDO",
        "OGGGGGGGGGGGGO",
        ".OOOOOOOOOOOO.",
        "..............",
    ]
    colors = {"O": hexc("#2a1608"), "W": hexc("#d8a060"), "L": hexc("#b47a3a"), "M": hexc("#8e5a26"), "D": hexc("#5e3814"),
              "G": hexc("#c89a2a"), "Y": hexc("#f4d44a"), "H": hexc("#fff6b0")}
    img = Image.new("RGBA", (14, 12), CLEAR)
    px = img.load()
    for y, row in enumerate(rows_open if open_ else rows_closed):
        for x, ch in enumerate(row):
            if ch in colors:
                px[x, y] = colors[ch]
    return img


def plate(w: int, h: int, seed: int, wood=None) -> Image.Image:
    """Eine Tafel: Rahmen aus dunklem Holz mit Messingecken, darin Pergament."""
    theme = THEMES["water"]
    img = Image.new("RGBA", (w, h), CLEAR)
    px = img.load()
    noise = pa.fractal_noise(seed, ((16, 0.5), (8, 0.3), (4, 0.2)), 160, 96)
    parch = pal("#b89a68", "#c8ac7a", "#d8c090", "#e4d0a4", "#eedcb6")
    for y in range(h):
        for x in range(w):
            rx, ry = min(x, w - 1 - x), min(y, h - 1 - y)
            if rx + ry < 2:
                continue
            if rx < 4 or ry < 4:
                base = 0.5
                if x == 1 or y == 1:
                    base = 0.8
                elif x == w - 2 or y == h - 2:
                    base = 0.2
                elif rx == 3 or ry == 3:
                    base = 0.22
                if rx == 0 or ry == 0:
                    px[x, y] = mix(theme["body"][0], (0, 0, 0, 255), 0.5)
                else:
                    grain = h01((x + int(h01(y, seed) * 9)) // 6, y * 3 + seed) * 0.2 - 0.1 if ry < 4 else h01(x * 3 + seed, (y + int(h01(x, seed) * 9)) // 6) * 0.2 - 0.1
                    px[x, y] = pa.pick_dither(theme["body"], max(0.0, min(0.999, base + grain)), x, y, 0.5)
            else:
                v = 0.35 + noise[y % 96][x % 160] * 0.6
                edge = min(rx - 4, ry - 4)
                if edge < 3:
                    v -= (3 - edge) * 0.12
                px[x, y] = pa.pick_dither(parch, max(0.0, min(0.999, v)), x, y, 0.6)
    # Messingecken
    trim = theme["trim"]
    for (ox, oy, sx, sy) in ((1, 1, 1, 1), (w - 2, 1, -1, 1), (1, h - 2, 1, -1), (w - 2, h - 2, -1, -1)):
        for i in range(7):
            for j in range(7 - i):
                x, y = ox + sx * i, oy + sy * j
                if i + j <= 1 and (i == 0 and j == 0):
                    continue
                c = trim[3] if (i == 0 or j == 0) else trim[2]
                if i + j == 6:
                    c = trim[0]
                px[x, y] = c
        px[ox + sx * 2, oy + sy * 2] = trim[4]
    return img


def inset(w: int, h: int, dark=False) -> Image.Image:
    """Ein vertieftes Feld (für das Bild des Fischs)."""
    img = Image.new("RGBA", (w, h), CLEAR)
    px = img.load()
    for y in range(h):
        for x in range(w):
            if x == 0 or y == 0:
                c = hexc("#6a5230")
            elif x == w - 1 or y == h - 1:
                c = hexc("#f4e6c4")
            elif x == 1 or y == 1:
                c = hexc("#8a7046") if not dark else hexc("#0e1e3a")
            else:
                # ein kleines Stück Wasser, in dem der Fisch liegt
                t = y / (h - 1)
                c = mix(hexc("#3c86a8"), hexc("#12305a"), t) if dark else mix(hexc("#cbb486"), hexc("#bfa676"), t)
                if dark and (x + y * 2) % 11 == 0 and y < h * 0.5:
                    c = mix(c, (255, 255, 255, 255), 0.12)
            px[x, y] = c
    return img


def ribbon(w: int, h: int) -> Image.Image:
    """Das Band zum Abschluss: ein breites, an den Enden eingeschnittenes Tuch mit Goldrand."""
    img = Image.new("RGBA", (w, h), CLEAR)
    px = img.load()
    cloth = pal("#0e2a4a", "#16406c", "#1e568c", "#2a6eaa", "#3c88c4")
    gold = THEMES["water"]["trim"]
    noise = pa.fractal_noise(4700, ((16, 0.6), (8, 0.4)), 176, 64)
    for y in range(h):
        for x in range(w):
            notch = abs(y - (h - 1) / 2) * 0.55            # Schwalbenschwanz an beiden Enden
            if x < (h / 2 * 0.55) - notch or x > w - 1 - ((h / 2 * 0.55) - notch):
                continue
            fold = math.sin(x / w * math.pi * 3.0) * 0.08
            v = 0.5 + fold + (noise[y % 64][x % 176] - 0.5) * 0.2 - (y / h - 0.5) * 0.3
            c = pa.pick_dither(cloth, max(0.0, min(0.999, v)), x, y, 0.7)
            if y in (2, 3) or y in (h - 4, h - 3):
                c = gold[3] if y in (2, h - 4) else gold[1]
            if y in (0, h - 1):
                c = hexc("#06101e")
            px[x, y] = c
    outline_shape(img, hexc("#06101e"))
    return img


def star(filled: bool) -> Image.Image:
    rows = ["...O...", "..OHO..", "OOOHOOO", "OHHHHMO", ".OHHMO.", ".OHMHO.", "OMO.OMO"] if filled else \
        ["...O...", "..O.O..", "OOO.OOO", "O.....O", ".O...O.", ".O.O.O.", "OO...OO"]
    colors = {"O": hexc("#6a4a0c") if filled else hexc("#8a7650"), "H": hexc("#ffe27a"), "M": hexc("#e8b02a")}
    img = Image.new("RGBA", (7, 7), CLEAR)
    px = img.load()
    for y, row in enumerate(rows):
        for x, ch in enumerate(row):
            if ch in colors:
                px[x, y] = colors[ch]
    return img


def bang() -> Image.Image:
    """Das Ausrufezeichen beim Biss."""
    rows = [
        "..OOOOOO..",
        ".OHHHHHYO.",
        ".OHYYYYYO.",
        ".OHYYYYDO.",
        ".OHYYYYDO.",
        "..OHYYDO..",
        "..OHYYDO..",
        "..OHYYDO..",
        "...OYDO...",
        "...OYDO...",
        "....OO....",
        "...OOOO...",
        "..OHYYDO..",
        "..OYYYDO..",
        "...OOOO...",
        "..........",
    ]
    colors = {"O": hexc("#3a1a04"), "H": hexc("#fff8c0"), "Y": hexc("#ffd23a"), "D": hexc("#e08a12")}
    img = Image.new("RGBA", (10, 16), CLEAR)
    px = img.load()
    for y, row in enumerate(rows):
        for x, ch in enumerate(row):
            if ch in colors:
                px[x, y] = colors[ch]
    return img


def sparkle() -> Image.Image:
    rows = ["....H....", "....H....", "...HWH...", "..HWWWH..", "HHWWWWWHH", "..HWWWH..", "...HWH...", "....H....", "....H...."]
    colors = {"H": (255, 240, 170, 200), "W": (255, 255, 255, 255)}
    img = Image.new("RGBA", (9, 9), CLEAR)
    px = img.load()
    for y, row in enumerate(rows):
        for x, ch in enumerate(row):
            if ch in colors:
                px[x, y] = colors[ch]
    return img


def minigame() -> Image.Image:
    atlas = Image.new("RGBA", (512, 256), CLEAR)
    for i, name in enumerate(("water", "lava", "void")):
        atlas.alpha_composite(frame(name), (i * 48, 0))
        atlas.alpha_composite(interior(name), (144 + i * 28, 0))
        atlas.alpha_composite(overlay(name), (144 + i * 28, 152))
    atlas.alpha_composite(meter_frame(), (230, 0))
    atlas.alpha_composite(meter_fill(), (246, 0))
    atlas.alpha_composite(plate(118, 74, 4601), (256, 0))
    atlas.alpha_composite(inset(36, 36, dark=True), (376, 0))
    atlas.alpha_composite(ribbon(176, 34), (256, 78))
    atlas.alpha_composite(zone(False), (256, 116))
    atlas.alpha_composite(zone(True), (282, 116))
    atlas.alpha_composite(chest(False), (310, 116))
    atlas.alpha_composite(chest(True), (326, 116))
    atlas.alpha_composite(star(True), (342, 116))
    atlas.alpha_composite(star(False), (350, 116))
    atlas.alpha_composite(bang(), (360, 116))
    atlas.alpha_composite(sparkle(), (372, 116))
    return atlas


# =============================================================================================
# Das Fischbuch
# =============================================================================================

def book_cover() -> Image.Image:
    """Der aufgeschlagene Band – gebaut wie die Chronik (tools/seal_textures.py), aber in Seeleder mit Silberbeschlag."""
    import seal_textures as st
    old = (st.LEATHER, st.GOLD)
    st.LEATHER = pal("#06171c", "#0a262e", "#0f3842", "#154c58", "#1c6270")
    st.GOLD = pal("#4a5660", "#76848e", "#a4b2ba", "#d2dee4", "#f4fafc")
    try:
        img = st._book()
    finally:
        st.LEATHER, st.GOLD = old
    px = img.load()
    swap = {hexc("#8e1c1c"): hexc("#d8a62a"), hexc("#5e1010"): hexc("#8a6212")}
    for y in range(img.height):
        for x in range(img.width):
            if px[x, y] in swap:
                px[x, y] = swap[px[x, y]]
    return img


def slot(kind: str) -> Image.Image:
    """Ein Fach für eine Art (22 × 22): gewöhnlich, unter der Maus, gewählt, Legende."""
    S = 22
    img = Image.new("RGBA", (S, S), CLEAR)
    px = img.load()
    edge_dark, edge_light = hexc("#7a6444"), hexc("#f6ead0")
    if kind == "legend":
        edge_dark, edge_light = hexc("#8a5a0c"), hexc("#ffe27a")
    for y in range(S):
        for x in range(S):
            rx, ry = min(x, S - 1 - x), min(y, S - 1 - y)
            if rx == 0 and ry == 0:
                continue
            if rx == 0 or ry == 0:
                c = edge_dark if (x == 0 or y == 0) else edge_light
                if kind == "legend":
                    c = hexc("#c8961e") if (x + y) % 4 < 2 else hexc("#f4d04a")
            elif rx == 1 or ry == 1:
                c = hexc("#9c8660") if (x == 1 or y == 1) else hexc("#d8c8a0")
            else:
                t = y / (S - 1)
                c = mix(hexc("#d2be92"), hexc("#c4ae80"), t)
                if (x * 3 + y * 5) % 17 == 0:
                    c = mix(c, hexc("#a89468"), 0.35)
            px[x, y] = c
    if kind in ("hover", "selected"):
        col = hexc("#fff2b0") if kind == "hover" else hexc("#c8281c")
        for y in range(S):
            for x in range(S):
                rx, ry = min(x, S - 1 - x), min(y, S - 1 - y)
                if (rx == 0 and ry == 0) or min(rx, ry) > (0 if kind == "hover" else 1):
                    continue
                px[x, y] = col if min(rx, ry) == 0 else mix(col, hexc("#5a0e08"), 0.45)
        if kind == "selected":
            for (cx, cy) in ((1, 1), (S - 2, 1), (1, S - 2), (S - 2, S - 2)):
                px[cx, cy] = hexc("#ffd24a")
    return img


def tab(selected: bool) -> Image.Image:
    """Ein Lesezeichen am linken Buchrand (ragt nach links heraus)."""
    W, H = 28, 18
    img = Image.new("RGBA", (W, H), CLEAR)
    px = img.load()
    cloth = pal("#5a4210", "#86641a", "#b48a26", "#dcb23a", "#f4d466") if selected else pal("#0c2a34", "#123c48", "#1a5260", "#246a7a", "#3284946".replace("3284946", "328494"))
    x0 = 0 if selected else 6
    for y in range(H):
        for x in range(x0, W):
            notch = abs(y - (H - 1) / 2)
            if x - x0 < 4 - notch * 0.5:
                continue
            v = 0.55 - (y / H - 0.5) * 0.35 + (h01(x, y, 9) - 0.5) * 0.12
            c = pa.pick_dither(cloth, max(0.0, min(0.999, v)), x, y, 0.6)
            if y in (1, H - 2):
                c = cloth[4] if y == 1 else cloth[1]
            if y in (0, H - 1):
                c = mix(cloth[0], (0, 0, 0, 255), 0.5)
            px[x, y] = c
    outline_shape(img, mix(cloth[0], (0, 0, 0, 255), 0.5))
    for y in range(H):                      # rechts läuft das Band unter den Einband: dort keine Linie
        px[W - 1, y] = px[W - 2, y] if px[W - 2, y][3] else CLEAR
    return img


ICONS = {
    # Zeichen der Kapitel, 12 × 12
    "rivers": (["............", "....G.......", "...GG..G....", "...G..GG....", "...G..G.....", ".LL...G..LL.", "LHHL.LLLLHHL", "HMMHLHHHHMMH", "MMMMHMMMMMMM", ".DD.MMDD..DD", "DMMD.DMMDDMM", "MMMMDMMMMMMM"],
               {"G": "#3c8a3c", "L": "#bfe8f8", "H": "#6ac0e4", "M": "#2e8cc0", "D": "#1c5e94"}),
    "sea": (["............", "......LL....", "....LLHHL...", "...LHHMMHL..", "..LHMM..MH..", ".LHM.....M..", "LHMM....LL..", "HMMML..LHHL.", "MMMMHLLHMMHL", "MMDMMHHMMMMH", "DDMDDMMMDDMM", "MMMMDDMDMMDD"],
            {"L": "#e4f6ff", "H": "#7ccaf0", "M": "#2a7cc4", "D": "#164e94"}),
    "deep": (["............", "..YY........", ".YWWY.......", ".YWWY..DDD..", "..YY..DMMMD.", "...D.DMMEMMD", "...DDMMMMMMD", "....DMMMTTD.", "..D.DMMMMMD.", "..DDDMMMMD..", "...D.DDDD...", "............"],
             {"Y": "#7af0ff", "W": "#f0ffff", "D": "#12203c", "M": "#2a4474", "E": "#fff4a0", "T": "#e8eef8"}),
    "nether": (["......H.....", ".....HY.....", ".....YYH....", "..H.HYOY....", "..YHYOOYH.H.", ".HYYOORYYHY.", ".YOYORROYYY.", ".YOORRRROOY.", ".YORRDDRROY.", "..ORRDDRRO..", "...ORRRRO...", "............"],
               {"H": "#ffe890", "Y": "#ffb42a", "O": "#e85a10", "R": "#a82208", "D": "#4a0a04"}),
    "end": (["............", "....DDDD....", "..DDMMMMDD..", ".DMMGGGGMMD.", ".DMGGLLGGMD.", "DMGGLDDLGGMD", "DMGGLDDLGGMD", ".DMGGLLGGMD.", ".DMMGGGGMMD.", "..DDMMMMDD..", "....DDDD....", "............"],
            {"D": "#0e2a22", "M": "#1c6a54", "G": "#3ac89a", "L": "#c8ffe8"}),
    "fae": (["............", "....PP.PP...", "...PLLPLLP..", "...PLLPLLP..", ".PP.PPYPP.PP", "PLLPPYWYPLLP", "PLLPPYYYPLLP", ".PP.PPYPP.PP", "...PLLPLLP..", "...PLLPLLP..", "....PP.PP...", "............"],
            {"P": "#c84a9a", "L": "#ffb4e0", "Y": "#ffd84a", "W": "#fff8d0"}),
    "shadow": (["............", "....LLLL....", "..LLHHHL....", ".LHHHL......", ".LHHL.....S.", "LHHHL...S...", "LHHHL.......", "LHHHHL....S.", ".LHHHHLLLL..", ".LLHHHHHHL..", "...LLLLLL...", "............"],
               {"L": "#5a3a9a", "H": "#b48cf0", "S": "#e8d8ff"}),
    "abyss": (["............", ".DDD....DDD.", "DMMMD..DMMMD", "DMMMMDDMMMMD", ".DMMOYYOMMD.", "..DOYWWYOD..", "..DOYWWYOD..", ".DMMOYYOMMD.", "DMMMMDDMMMMD", "DMMMD..DMMMD", ".DDD....DDD.", "............"],
              {"D": "#1c1420", "M": "#4a3458", "O": "#e8541a", "Y": "#ffb02a", "W": "#fff4b0"}),
    "legends": (["............", "............", ".Y...YY...Y.", ".YH..YH..YH.", ".YHY.YH.YHY.", ".YHHYYHYYHD.", ".YHHHHHHHHD.", ".YHRHHGHHBD.", ".YHHHHHHHDD.", ".DDDDDDDDDD.", "............", "............"],
                {"Y": "#f4c22a", "H": "#ffe684", "D": "#a8700a", "R": "#e83a2a", "G": "#3ac85a", "B": "#3a8ae8"}),
}
ICON_ORDER = ("rivers", "sea", "deep", "nether", "end", "fae", "shadow", "abyss", "legends")

SYMBOLS = {
    # kleine Zeichen der Angaben, 9 × 9: Ort, Zeit, Wetter, Größe, Rekord, Anzahl, Wirkung
    "place": (["...OOO...", "..OMMMO..", ".OMLLLMO.", ".OMLOLMO.", ".OMLLLMO.", "..OMMMO..", "...OMO...", "...OMO...", "....O...."], {"O": "#3a2414", "M": "#c8382a", "L": "#f4b0a0"}),
    "time": (["..OOOOO..", ".OLLLLLO.", "OLLLOLLLO", "OLLLOLLLO", "OLLLOOOLO", "OLLLLLLLO", "OLLLLLLLO", ".OLLLLLO.", "..OOOOO.."], {"O": "#3a2414", "L": "#f4ecd8"}),
    "weather": (["...OOO...", "..OLLLOO.", ".OLLLLLLO", "OLLLLLLLO", "OLLLLLLLO", ".OOOOOOO.", "..B..B...", ".B..B..B.", "...B..B.."], {"O": "#3a2414", "L": "#e8eef4", "B": "#3a7ac8"}),
    "size": (["OOOOOOOOO", "OLOLOLOLO", "OLLLLLLLO", "OOOOOOOOO", ".........", "O.......O", "OOOOOOOOO", "O.......O", "........."], {"O": "#3a2414", "L": "#f4d86a"}),
    "record": (["OOOOOOOOO", "OYYYYYYYO", ".OYYYYYO.", ".OYHYYYO.", "..OYYYO..", "...OYO...", "...OYO...", "..OOYOO..", "..OOOOO.."], {"O": "#3a2414", "Y": "#f4c22a", "H": "#fff4b0"}),
    "count": ([".........", "O...OOO..", "OO.OMMMO.", "OMOMMMEMO", "OMMMMMMMO", "OMOMMMMMO", "OO.OMMMO.", "O...OOO..", "........."], {"O": "#3a2414", "M": "#4a9ad0", "E": "#101018"}),
    "food": (["O.O.O..OO", "O.O.O.OLO", "O.O.O.OLO", "OOOOO.OLO", ".OLO..OLO", ".OLO...OO", ".OLO...OO", ".OLO...OO", ".OOO...OO"], {"O": "#3a2414", "L": "#d8dce4"}),
}
SYMBOL_ORDER = ("place", "time", "weather", "size", "record", "count", "food")


def art(rows, colors) -> Image.Image:
    img = Image.new("RGBA", (len(rows[0]), len(rows)), CLEAR)
    px = img.load()
    for y, row in enumerate(rows):
        assert len(row) == len(rows[0]), row
        for x, ch in enumerate(row):
            if ch in colors:
                px[x, y] = hexc(colors[ch])
    return img


def bar(w: int, h: int, fill: bool) -> Image.Image:
    img = Image.new("RGBA", (w, h), CLEAR)
    px = img.load()
    for y in range(h):
        for x in range(w):
            if fill:
                c = mix(hexc("#3aa0c8"), hexc("#1c5e94"), y / max(1, h - 1))
                if y == 0:
                    c = hexc("#9adcf4")
                if x % 10 == 9:
                    c = mix(c, (0, 0, 0, 255), 0.2)
            else:
                rx, ry = min(x, w - 1 - x), min(y, h - 1 - y)
                if rx == 0 and ry == 0:
                    continue
                c = hexc("#6a5230") if (rx == 0 or ry == 0) else hexc("#a8946c")
            px[x, y] = c
    return img


def book() -> Image.Image:
    import seal_textures as st
    atlas = Image.new("RGBA", (512, 256), CLEAR)
    atlas.alpha_composite(book_cover(), (0, 0))
    for i, kind in enumerate(("normal", "hover", "selected", "legend")):
        atlas.alpha_composite(slot(kind), (320 + i * 24, 0))
    atlas.alpha_composite(inset(44, 44, dark=True), (320, 24))
    atlas.alpha_composite(tab(False), (368, 24))
    atlas.alpha_composite(tab(True), (400, 24))
    for i, name in enumerate(ICON_ORDER):
        atlas.alpha_composite(art(*ICONS[name]), (320 + i * 14, 70))
    atlas.alpha_composite(star(True), (320, 84))
    atlas.alpha_composite(star(False), (328, 84))
    atlas.alpha_composite(st._divider(110, 7), (320, 94))
    atlas.alpha_composite(bar(102, 8, False), (320, 104))
    atlas.alpha_composite(bar(100, 6, True), (320, 114))
    for i, name in enumerate(SYMBOL_ORDER):
        atlas.alpha_composite(art(*SYMBOLS[name]), (320 + i * 11, 124))
    return atlas


def build(put) -> None:
    """Für tools/generate_textures.py."""
    put("gui/fishing/minigame", minigame())
    put("gui/fishing/book", book())


def main() -> None:
    out = sys.argv[1] if len(sys.argv) > 1 else OUT
    os.makedirs(out, exist_ok=True)
    minigame().save(os.path.join(out, "minigame.png"))
    book().save(os.path.join(out, "book.png"))
    print("geschrieben nach", out)


if __name__ == "__main__":
    main()
