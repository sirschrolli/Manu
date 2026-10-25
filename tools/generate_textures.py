#!/usr/bin/env python3
"""
Erzeugt alle 16x16-Texturen der Mod "Kingdoms at War" als eigene Pixelkunst.

Es werden KEINE Original-Minecraft-Texturen verwendet. Die Erze bestehen nur aus
"Erz-Flecken" mit transparentem Hintergrund – der Stein dahinter kommt zur Laufzeit
aus dem Spiel (siehe ModModelProvider, Vorlage ORE_OVERLAY).

Aufruf (aus dem Projektordner):
    python3 tools/generate_textures.py            # schreibt alle Texturen
    python3 tools/generate_textures.py --preview  # zusätzlich eine vergrößerte Übersicht

Benötigt: Pillow (pip install pillow)
"""
from __future__ import annotations

import math
import random
import sys
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parent.parent
TEXTURES = ROOT / "src" / "main" / "resources" / "assets" / "kingdomsatwar" / "textures"
BLOCK = TEXTURES / "block"
ITEM = TEXTURES / "item"

Color = tuple[int, int, int, int]
CLEAR: Color = (0, 0, 0, 0)


# =============================================================================
# Farb-Hilfen
# =============================================================================

def hexc(value: str) -> Color:
    value = value.lstrip("#")
    return (int(value[0:2], 16), int(value[2:4], 16), int(value[4:6], 16), 255)


def mix(a: Color, b: Color, t: float) -> Color:
    return tuple(round(a[i] + (b[i] - a[i]) * t) for i in range(3)) + (255,)


def scale(c: Color, factor: float) -> Color:
    return tuple(max(0, min(255, round(c[i] * factor))) for i in range(3)) + (c[3],)


def ramp(*colors: str) -> list[Color]:
    return [hexc(c) for c in colors]


def pick(palette: list[Color], value: float) -> Color:
    """value 0..1 -> Farbe aus der Palette (dunkel -> hell)."""
    value = max(0.0, min(0.999, value))
    return palette[int(value * len(palette))]


def new(fill: Color = CLEAR) -> Image.Image:
    return Image.new("RGBA", (16, 16), fill)


def save(img: Image.Image, folder: Path, name: str) -> None:
    folder.mkdir(parents=True, exist_ok=True)
    img.save(folder / f"{name}.png")
    GENERATED.append((folder.name, name, img))


GENERATED: list[tuple[str, str, Image.Image]] = []


def value_noise(rng: random.Random, cell: int = 4) -> list[list[float]]:
    """Weiches, kachelbares Rauschen (0..1) für 16x16."""
    n = 16 // cell
    grid = [[rng.random() for _ in range(n)] for _ in range(n)]
    out = [[0.0] * 16 for _ in range(16)]
    for y in range(16):
        for x in range(16):
            gx, gy = x / cell, y / cell
            x0, y0 = int(gx) % n, int(gy) % n
            x1, y1 = (x0 + 1) % n, (y0 + 1) % n
            tx, ty = gx - int(gx), gy - int(gy)
            tx, ty = tx * tx * (3 - 2 * tx), ty * ty * (3 - 2 * ty)
            a = grid[y0][x0] + (grid[y0][x1] - grid[y0][x0]) * tx
            b = grid[y1][x0] + (grid[y1][x1] - grid[y1][x0]) * tx
            out[y][x] = a + (b - a) * ty
    return out


# =============================================================================
# Mauerwerk-Muster (kachelbar)
# =============================================================================

def masonry(rows: list[tuple[int, int, int]], palette: list[Color], mortar: Color, seed: int,
            grain: float = 0.18, bevel: bool = True, stains: list[Color] | None = None,
            stain_chance: float = 0.0) -> Image.Image:
    """
    rows: Liste (Zeilenhöhe, Steinbreite, Versatz) von oben nach unten (Summe der Höhen = 16).
    Fugen liegen oben und links jedes Steins, damit sich das Muster nahtlos wiederholt.
    """
    rng = random.Random(seed)
    img = new()
    px = img.load()
    y0 = 0
    for height, width, offset in rows:
        x = -offset
        while x < 16:
            base = 0.30 + rng.random() * 0.45
            for dy in range(height):
                for dx in range(width):
                    gx, gy = (x + dx) % 16, y0 + dy
                    if dy == 0 or dx == 0:
                        px[gx, gy] = mortar
                        continue
                    v = base + (rng.random() - 0.5) * grain
                    if bevel:
                        if dy == 1 or dx == 1:
                            v += 0.16
                        if dy == height - 1 or dx == width - 1:
                            v -= 0.14
                    color = pick(palette, v)
                    if stains and rng.random() < stain_chance:
                        color = rng.choice(stains)
                    px[gx, gy] = color
            x += width
        y0 += height
    return img


def cobble(palette: list[Color], gap: Color, seed: int, cells: int = 11) -> Image.Image:
    """Unregelmäßige, runde Feldsteine (Voronoi, kachelbar)."""
    rng = random.Random(seed)
    points = [(rng.random() * 16, rng.random() * 16, 0.25 + rng.random() * 0.5) for _ in range(cells)]
    img = new()
    px = img.load()

    def dist(ax, ay, bx, by):
        dx = min(abs(ax - bx), 16 - abs(ax - bx))
        dy = min(abs(ay - by), 16 - abs(ay - by))
        return math.hypot(dx, dy)

    for y in range(16):
        for x in range(16):
            ds = sorted((dist(x + 0.5, y + 0.5, p[0], p[1]), i) for i, p in enumerate(points))
            (d1, i1), (d2, _) = ds[0], ds[1]
            if d2 - d1 < 0.9:
                px[x, y] = gap
                continue
            cx, cy, base = points[i1]
            # Licht von oben links
            dx = ((x + 0.5 - cx + 8) % 16) - 8
            dy = ((y + 0.5 - cy + 8) % 16) - 8
            light = -(dx + dy) / 10.0
            v = base + light * 0.35 + (rng.random() - 0.5) * 0.15
            px[x, y] = pick(palette, v)
    return img


def cracked(img: Image.Image, crack: Color, seed: int, count: int = 3) -> Image.Image:
    rng = random.Random(seed)
    px = img.load()
    for _ in range(count):
        x, y = rng.randrange(16), rng.randrange(16)
        for _ in range(rng.randrange(5, 9)):
            px[x % 16, y % 16] = crack
            x += rng.choice([-1, 0, 1, 1])
            y += rng.choice([0, 1, 1])
    return img


def moss(img: Image.Image, palette: list[Color], seed: int, amount: float = 0.45) -> Image.Image:
    rng = random.Random(seed)
    noise = value_noise(rng, 4)
    px = img.load()
    for y in range(16):
        for x in range(16):
            # Moos sammelt sich oben und in den Fugen
            weight = noise[y][x] + (0.25 if y < 5 else 0.0)
            if weight > 1.0 - amount:
                px[x, y] = pick(palette, rng.random())
    return img


# =============================================================================
# Baublöcke
# =============================================================================

def building_blocks() -> None:
    stone = ramp("#5c5c5c", "#696969", "#767676", "#838383", "#919191")
    save(masonry([(4, 8, 0), (4, 6, 3), (4, 8, 4), (4, 6, 1)], stone, hexc("#474747"), 11),
         BLOCK, "medieval_stone")

    field = ramp("#4f4a44", "#5e5850", "#6d665d", "#7d756a", "#8c8377")
    save(cobble(field, hexc("#34302b"), 21), BLOCK, "rough_fieldstone")

    sand = ramp("#b09862", "#c2aa72", "#cfb882", "#dcc792", "#e7d5a3")
    save(masonry([(8, 16, 0), (8, 8, 4)], sand, hexc("#9a8250"), 31, grain=0.10), BLOCK, "hewn_sandstone")

    dark = ramp("#2c2e35", "#363840", "#40434c", "#4b4e58", "#575a65")
    save(masonry([(4, 8, 0), (4, 8, 4), (4, 8, 0), (4, 8, 4)], dark, hexc("#1d1e23"), 41), BLOCK, "dark_stone_bricks")

    lime = ramp("#c8c1b0", "#d5cfc0", "#e0dbcd", "#eae6da", "#f3f0e7")
    save(masonry([(4, 8, 2), (4, 8, 6), (4, 8, 2), (4, 8, 6)], lime, hexc("#a59e8d"), 51, grain=0.12),
         BLOCK, "limestone_bricks")

    weathered = ramp("#5a5751", "#67645d", "#74716a", "#817d75", "#8d8980")
    img = masonry([(4, 8, 0), (4, 8, 4), (4, 8, 0), (4, 8, 4)], weathered, hexc("#46433e"), 61,
                  grain=0.30, stains=ramp("#5d5c45", "#4f4a3e", "#6a6656"), stain_chance=0.10)
    save(cracked(img, hexc("#3e3b36"), 62, 2), BLOCK, "weathered_stone_bricks")

    mossy_stone = ramp("#5b5b5b", "#686868", "#757575", "#828282")
    img = masonry([(5, 8, 0), (5, 8, 4), (6, 8, 2)], mossy_stone, hexc("#444444"), 71)
    save(moss(img, ramp("#3c5f22", "#4a7529", "#5a8b32", "#6c9f3d"), 72, amount=0.28), BLOCK, "mossy_wall_stones")

    crack_stone = ramp("#6a6a6a", "#777777", "#848484", "#919191", "#9d9d9d")
    img = masonry([(8, 8, 0), (8, 8, 4)], crack_stone, hexc("#525252"), 81, grain=0.12)
    save(cracked(img, hexc("#3b3b3b"), 82, 4), BLOCK, "cracked_stone_blocks")

    # Steinpfeiler: Seite mit Rillen, oben ein Quadrat
    pillar = ramp("#6f6f6f", "#7c7c7c", "#898989", "#969696", "#a3a3a3")
    rng = random.Random(91)
    side = new()
    px = side.load()
    for y in range(16):
        for x in range(16):
            band = x % 4
            v = {0: 0.15, 1: 0.75, 2: 0.6, 3: 0.4}[band] + (rng.random() - 0.5) * 0.12
            if y in (0, 15):
                v = 0.2
            if y in (1, 14):
                v = 0.85
            px[x, y] = pick(pillar, v)
    save(side, BLOCK, "stone_pillar_side")
    top = new()
    px = top.load()
    for y in range(16):
        for x in range(16):
            d = max(abs(x - 7.5), abs(y - 7.5))
            v = 0.55 + (rng.random() - 0.5) * 0.1
            if d > 6.5:
                v = 0.25
            elif d > 5.5:
                v = 0.8
            elif d < 2.5:
                v = 0.7
            elif d < 3.5:
                v = 0.35
            px[x, y] = pick(pillar, v)
    save(top, BLOCK, "stone_pillar_top")


def timber() -> None:
    wood = ramp("#3d2616", "#4a2f1c", "#573822", "#654229", "#754d31")
    rng = random.Random(101)
    # Holzbalken – Seite mit Maserung
    side = new()
    px = side.load()
    lines = [rng.random() for _ in range(16)]
    for y in range(16):
        for x in range(16):
            v = 0.35 + lines[x] * 0.4 + math.sin((y + x * 3) * 0.7) * 0.05 + (rng.random() - 0.5) * 0.08
            if x in (0, 15):
                v -= 0.25
            px[x, y] = pick(wood, v)
    save(side, BLOCK, "timber_beam_side")
    # Hirnholz mit Jahresringen
    top = new()
    px = top.load()
    end_grain = ramp("#5e3b22", "#6e472a", "#7d5434", "#8d613d", "#9c6d46")
    for y in range(16):
        for x in range(16):
            d = math.hypot(x - 7.5, y - 7.5)
            v = 0.5 + 0.3 * math.sin(d * 1.6) + (rng.random() - 0.5) * 0.1
            if max(abs(x - 7.5), abs(y - 7.5)) > 6.5:
                px[x, y] = pick(wood, 0.25 + rng.random() * 0.2)
            else:
                px[x, y] = pick(end_grain, v)
    save(top, BLOCK, "timber_beam_top")

    # Fachwerk: heller Putz mit dunklen Balken
    plaster = ramp("#d6cfbd", "#e0d9c8", "#e9e3d4", "#f1ecdf")
    beam = ramp("#3b2416", "#4a2f1c", "#573822", "#64422a")

    def frame(name: str, mask) -> None:
        r = random.Random(hash(name) & 0xFFFF)
        img = new()
        p = img.load()
        for y in range(16):
            for x in range(16):
                if x == 0 or y == 0 or x == 15 or y == 15 or mask(x, y):
                    p[x, y] = pick(beam, 0.3 + r.random() * 0.6)
                else:
                    p[x, y] = pick(plaster, 0.3 + r.random() * 0.7)
        save(img, BLOCK, name)

    frame("timber_frame", lambda x, y: x in (7, 8))
    frame("timber_frame_diagonal", lambda x, y: abs(x - (15 - y)) <= 1)
    frame("timber_frame_cross", lambda x, y: abs(x - (15 - y)) <= 0 or abs(x - y) <= 0 or
          abs(x - (15 - y)) == 1 and x < 15 or abs(x - y) == 1 and x < 15)


def roof_tiles(name: str, palette: list[Color], gap: Color, seed: int, tile_w: int = 4,
               row_h: int = 4, rounded: bool = True) -> None:
    rng = random.Random(seed)
    img = new()
    px = img.load()
    for row in range(16 // row_h):
        offset = (row % 2) * (tile_w // 2)
        for tile in range(16 // tile_w + 1):
            base = 0.35 + rng.random() * 0.35
            for dy in range(row_h):
                for dx in range(tile_w):
                    x = (tile * tile_w + dx - offset) % 16
                    y = row * row_h + dy
                    v = base + (0.25 if dy == 0 else 0) - (0.1 * dy / row_h) + (rng.random() - 0.5) * 0.12
                    color = pick(palette, v)
                    if dx == tile_w - 1:
                        color = gap
                    if dy == row_h - 1:
                        color = scale(pick(palette, base), 0.72)
                        if rounded and dx in (0, tile_w - 2):
                            color = gap
                    px[x, y] = color
    save(img, BLOCK, name)


def roofs() -> None:
    roof_tiles("red_roof_tiles", ramp("#8a2c20", "#a23827", "#b9442f", "#cd5139", "#df6347"), hexc("#561a12"), 201)
    roof_tiles("brown_roof_tiles", ramp("#5a3a24", "#6b462c", "#7f5436", "#926240", "#a3714b"), hexc("#37210f"), 202)
    roof_tiles("light_roof_tiles", ramp("#ad9d84", "#bdae96", "#ccbea7", "#dacdb8", "#e6dbc9"), hexc("#857862"), 203)
    roof_tiles("dark_slate_shingles", ramp("#272c32", "#30363e", "#3a4049", "#454c56", "#515a65"), hexc("#16191d"), 204,
               tile_w=8, rounded=False)
    roof_tiles("wooden_shingles", ramp("#5e4026", "#6f4d2e", "#825b36", "#946840", "#a6764b"), hexc("#3c2a18"), 205,
               rounded=False)

    # Kupferdach mit Stehfalz
    copper = ramp("#8a4626", "#a5562f", "#bd6739", "#d27a46", "#e59358")
    rng = random.Random(206)
    img = new()
    px = img.load()
    for y in range(16):
        for x in range(16):
            band = x % 8
            if band == 0:
                v = 0.95
            elif band == 1:
                v = 0.15
            else:
                v = 0.45 + math.sin(y * 0.9 + x) * 0.08 + (rng.random() - 0.5) * 0.12
            if y % 8 == 7 and band not in (0, 1):
                v -= 0.15
            px[x, y] = pick(copper, v)
    save(img, BLOCK, "copper_roof")


# =============================================================================
# Erze, Rohstoffe, Speicherblöcke
# =============================================================================

ORE_PALETTES = {
    "carbon": ramp("#0c0c0e", "#1b1c20", "#2a2c32", "#40434c", "#666a76"),
    "konterit": ramp("#3a0e58", "#65229a", "#9343d2", "#bf76f2", "#e7cdff"),
    "aetherit": ramp("#0a4864", "#1477a4", "#21acd8", "#6cdef6", "#d6fbff"),
    "draconium": ramp("#470606", "#861010", "#c01f1f", "#ee4838", "#ffb0a0"),
    "wyvernit": ramp("#3b6896", "#5d90c6", "#8bb9e6", "#bddffe", "#f2faff"),
}
GEM_COLORS = [
    ramp("#4c1878", "#7c2fb8", "#a956e8", "#d8a6ff", "#f3e3ff"),
    ramp("#12522a", "#1f8c44", "#36c463", "#9cf0b6", "#e6fff0"),
    ramp("#0d5262", "#1b97ae", "#3fd2e8", "#a9f3ff", "#ecfdff"),
]

# Kleine Erz-Formen: 0 = dunkler Rand, 1-3 = Farbstufen, 4 = Glanzlicht
ORE_SHAPES = [
    ["0330", "3443", "0320"],
    ["032", "343", "220"],
    ["03", "42", "30"],
    ["0330", "3442", "2320", "0200"],
    ["30", "43", "02"],
]


def ore_overlay(name: str, palettes: list[list[Color]], seed: int, clusters: int = 5) -> None:
    rng = random.Random(seed)
    img = new()
    px = img.load()
    spots = [(2, 2), (9, 1), (5, 6), (12, 7), (1, 10), (8, 11), (13, 13), (4, 13)]
    rng.shuffle(spots)
    for i in range(clusters):
        sx, sy = spots[i]
        sx += rng.randint(-1, 1)
        sy += rng.randint(-1, 1)
        shape = rng.choice(ORE_SHAPES)
        palette = palettes[i % len(palettes)]
        for dy, row in enumerate(shape):
            for dx, ch in enumerate(row):
                x, y = sx + dx, sy + dy
                if 0 <= x < 16 and 0 <= y < 16:
                    px[x, y] = palette[int(ch)]
    save(img, BLOCK, f"{name}_ore_overlay")


def gem_block(name: str, palette: list[Color], seed: int) -> None:
    rng = random.Random(seed)
    img = new()
    px = img.load()
    for y in range(16):
        for x in range(16):
            if x in (0, 15) or y in (0, 15):
                v = 0
            elif x == 1 or y == 1:
                v = 3
            elif x == 14 or y == 14:
                v = 1
            else:
                # Facetten: diagonales Rautenmuster
                d = (x + y) % 6
                e = (x - y) % 6
                v = 2
                if d == 0 or e == 0:
                    v = 3
                if (x + y) % 6 == 3 and (x - y) % 6 == 3:
                    v = 4
                if rng.random() < 0.06:
                    v = 1
            px[x, y] = palette[v]
    save(img, BLOCK, f"{name}_block")


def multicolor_gem_block() -> None:
    img = new()
    px = img.load()
    rng = random.Random(311)
    for y in range(16):
        for x in range(16):
            palette = GEM_COLORS[((x // 4) + (y // 4)) % 3]
            if x in (0, 15) or y in (0, 15):
                v = 0
            elif x % 4 == 0 or y % 4 == 0:
                v = 1
            elif x % 4 == 1 and y % 4 == 1:
                v = 4
            else:
                v = 2 + (rng.random() < 0.3)
            px[x, y] = palette[v]
    save(img, BLOCK, "gemstone_block")


def resources_blocks() -> None:
    ore_overlay("carbon", [ORE_PALETTES["carbon"]], 301, clusters=6)
    ore_overlay("gemstone", GEM_COLORS, 302, clusters=5)
    ore_overlay("konterit", [ORE_PALETTES["konterit"]], 303, clusters=5)
    ore_overlay("aetherit", [ORE_PALETTES["aetherit"]], 304, clusters=5)
    ore_overlay("draconium", [ORE_PALETTES["draconium"]], 305, clusters=4)
    ore_overlay("wyvernit", [ORE_PALETTES["wyvernit"]], 306, clusters=5)

    multicolor_gem_block()
    gem_block("konterit", ORE_PALETTES["konterit"], 312)
    gem_block("aetherit", ORE_PALETTES["aetherit"], 313)
    gem_block("draconium", ORE_PALETTES["draconium"], 314)
    gem_block("wyvernit", ORE_PALETTES["wyvernit"], 315)

    # Kohlenstoffblock: dunkel mit glänzenden Bruchkanten
    carbon = ORE_PALETTES["carbon"]
    rng = random.Random(316)
    img = new()
    px = img.load()
    noise = value_noise(rng, 4)
    for y in range(16):
        for x in range(16):
            v = 1 + (noise[y][x] > 0.55) + (rng.random() < 0.08) * 2
            if x in (0, 15) or y in (0, 15):
                v = 0
            px[x, y] = carbon[min(4, v)]
    save(img, BLOCK, "carbon_block")

    # Stahlblock: vier Platten mit Nieten
    steel = ramp("#454c56", "#5f6873", "#79838e", "#949ea9", "#bcc5cf")
    img = new()
    px = img.load()
    for y in range(16):
        for x in range(16):
            lx, ly = x % 8, y % 8
            if lx == 0 or ly == 0:
                v = 0
            elif lx == 1 or ly == 1:
                v = 4
            elif lx == 7 or ly == 7:
                v = 1
            else:
                v = 2 + ((x + y) % 7 == 0)
            if (lx, ly) in ((2, 2), (5, 2), (2, 5), (5, 5)):
                v = 4
            px[x, y] = steel[v]
    save(img, BLOCK, "steel_block")


# =============================================================================
# Dracheneier (Schuppenmuster in den Farben aus dem Konzeptbild)
# =============================================================================

DRAGON_PALETTES = {
    # dunkel, Grundfarbe, Schuppe, hell, Glanz
    "fire": ramp("#2a0d0b", "#7a1714", "#c4301a", "#f07a1e", "#ffc35a"),
    "ice": ramp("#1d3c74", "#3f74b4", "#8fbfe8", "#dcefff", "#ffffff"),
    "lightning": ramp("#141119", "#2c2838", "#4a4458", "#f2d21e", "#fff7b0"),
    "nature": ramp("#1b3413", "#2f5a22", "#4f8a32", "#8ac84a", "#d2f59a"),
    "earth": ramp("#33271b", "#5a4430", "#7a5c3e", "#a88a62", "#d0d0c8"),
    "water": ramp("#0c3462", "#1a5a9a", "#2a8ac8", "#4ac4e0", "#b0f2fa"),
}


EGG_SCALE = [
    "1331",
    "2432",
    "2322",
    "0110",
]


def dragon_egg(kind: str, seed: int) -> None:
    """Überlappende Drachenschuppen; jede zweite Reihe ist um eine halbe Schuppe versetzt."""
    pal = DRAGON_PALETTES[kind]
    rng = random.Random(seed)
    img = new()
    px = img.load()
    for y in range(16):
        offset = 2 if (y // 4) % 2 else 0
        for x in range(16):
            v = int(EGG_SCALE[y % 4][(x + offset) % 4])
            if v == 2 and rng.random() < 0.12:
                v = 3
            px[x, y] = pal[v]
    # Akzente je Art
    if kind == "lightning":
        for x, y in [(3, 1), (4, 2), (3, 3), (4, 4), (11, 9), (12, 10), (11, 11), (12, 12)]:
            px[x, y] = pal[4]
    elif kind == "fire":
        for x, y in [(6, 5), (13, 13), (2, 10)]:
            px[x, y] = pal[4]
    save(img, BLOCK, f"{kind}_dragon_egg")


def dragon_eggs() -> None:
    for i, kind in enumerate(DRAGON_PALETTES):
        dragon_egg(kind, 400 + i)


# =============================================================================
# Pixel-Sprites (Gegenstände, Pflanzen) – als Zeichenraster
# =============================================================================

def sprite(rows: list[str], colors: dict[str, Color]) -> Image.Image:
    assert len(rows) == 16, f"16 Zeilen erwartet, nicht {len(rows)}"
    img = new()
    px = img.load()
    for y, row in enumerate(rows):
        assert len(row) == 16, f"Zeile {y} hat {len(row)} Zeichen: {row!r}"
        for x, ch in enumerate(row):
            if ch != ".":
                px[x, y] = colors[ch]
    return img


def tone(palette: list[Color]) -> dict[str, Color]:
    """Palette (5 Stufen) -> Zeichen o (Rand), d, m, l, h."""
    return {"o": palette[0], "d": palette[1], "m": palette[2], "l": palette[3], "h": palette[4]}


INGOT = [
    "................",
    "................",
    "................",
    "................",
    "................",
    ".....ooooooooo..",
    "....ohhhhhhhhlo.",
    "...ohlllllllldo.",
    "..ooooooooooldo.",
    "..ommmmmmmmmodo.",
    "..ommmmmmmmmoo..",
    "..oddddddddddo..",
    "...oooooooooo...",
    "................",
    "................",
    "................",
]

CRYSTAL_CLUSTER = [
    "................",
    ".......o........",
    "......ohd.......",
    "......ohmo......",
    "..o...olmo..o...",
    ".ohd..olmo.ohd..",
    ".ohmo.olmd.olmo.",
    ".olmo.olmd.olmd.",
    ".olmd.ollmoolmd.",
    ".ollmoolmmdolmd.",
    "..olmdolmmdolmo.",
    "..olmmolmmdlmdo.",
    "...ollmmmmmmdo..",
    "....oddmmmddo...",
    ".....oooooooo...",
    "................",
]

SHARD = [
    "................",
    "..........oo....",
    ".........ohho...",
    "........ohllmo..",
    ".......ohllmmo..",
    "......ohllmmdo..",
    ".....ohllmmdo...",
    "....ohllmmdo....",
    "...ohllmmdo.....",
    "..ohllmmdo......",
    "..olmmmdo.......",
    "..ommddo........",
    "...oddo.........",
    "....oo..........",
    "................",
    "................",
]

TALL_CRYSTAL = [
    "................",
    ".......oo.......",
    "......ohdo......",
    "......ohmo......",
    ".....ohlmdo.....",
    ".....ohlmdo..h..",
    ".....ohlmdo.hhh.",
    ".h...olmmdo..h..",
    "hhh..olmmdo.....",
    ".h...olmmdo.....",
    ".....olmmdo.....",
    ".....olmmdo.....",
    "......olmo......",
    "......odmo......",
    ".......oo.......",
    "................",
]

TWIN_CRYSTAL = [
    "................",
    "...oo...........",
    "...ohoo.........",
    "...olhdo........",
    "....olmdo...oo..",
    "....olmmdo.ohdo.",
    ".....olmmdohmdo.",
    ".....olmmdolmdo.",
    "......olmmlmmdo.",
    "......olmmmmdo..",
    ".......olmmmdo..",
    ".......olmmdo...",
    "......oddmmdo...",
    ".....ooooooo....",
    "................",
    "................",
]

LUMP = [
    "................",
    "................",
    "................",
    ".....oooo.......",
    "....ohhlmoo.....",
    "...ohllmmmdo....",
    "..ohlhmmmmddo...",
    "..olmmmlmmmdo...",
    ".olmmmhlmmddo...",
    ".olmmmmmmdmdo...",
    ".odmmmmmddddo...",
    "..odddmmdddo....",
    "...oodddddo.....",
    ".....ooooo......",
    "................",
    "................",
]

GEM_CLUSTER = [
    "................",
    "......PPPP......",
    ".....PpwqqP.....",
    "....PpwwqqrP....",
    "....PPPPPPPP....",
    ".....PqqqrrP....",
    "......PqrrP.....",
    "..GGGG.PrP.CCCC.",
    ".GgwhhG.P.CcwhhC",
    ".GGGGGG...CCCCCC",
    "..GhhjG....ChhkC",
    "...GjG......CkC.",
    "....G........C..",
    "................",
    "................",
    "................",
]

DUST = [
    "................",
    "................",
    "................",
    "................",
    "................",
    "................",
    ".......hl.......",
    ".....lmhmld.....",
    "....lmmlmxmd....",
    "...lmxmmlmmxd...",
    "..lmmmlmmxmmmd..",
    "..dmxmmmmmmxmd..",
    "...ddmmxmmmdd...",
    ".....dddddd.....",
    "................",
    "................",
]

SCALE = [
    "................",
    "................",
    "...oooooooooo...",
    "..ohhhhllllldo..",
    "..ohhlllhlmmdo..",
    "..ohllllhmmmdo..",
    "..ollllmhmmmdo..",
    "..ollllmhmmddo..",
    "...ollmmhmmdo...",
    "...ollmmhmddo...",
    "....olmmhmdo....",
    "....olmmhddo....",
    ".....olmhdo.....",
    "......olmo......",
    ".......oo.......",
    "................",
]

EGG_FRAGMENT = [
    "................",
    "................",
    "..........o.....",
    "......o..oho....",
    ".....ohooolmo...",
    "....ohlllmmdo...",
    "...ohlhlmmdmo...",
    "...olllmdmmdo...",
    "..ohlhmmmdmdo...",
    "..olllmdmmmdio..",
    "..olmmmmdmdiio..",
    "..olmdmmmdiiio..",
    "...oommdiiiioo..",
    ".....ooooooo....",
    "................",
    "................",
]

MEAT = [
    "................",
    "................",
    "...........ww...",
    "..........wbbw..",
    "...oooo..wbbw...",
    "..oRRRRo.wbw....",
    ".oRrrrrRowbw....",
    ".oRrhrrrRbw.....",
    ".oRrrrrrrRo.....",
    ".oRrrrfrrRo..ee.",
    ".oRrrrrrRRo.eEEe",
    "..oRRrrRRo..eEEe",
    "...ooRRRo....ee.",
    ".....ooo........",
    "................",
    "................",
]

TOMATO = [
    "................",
    "................",
    "......g..g......",
    ".......gg.......",
    ".....ogGGgo.....",
    "....oRgrrgRo....",
    "...oRrhrrrrRo...",
    "...oRhrrrrrRo...",
    "...oRrrrrrrRo...",
    "...oRrrrrrrRo...",
    "...oRrrrrrRRo...",
    "....oRRrrRRo....",
    ".....oRRRRo.....",
    "......oooo......",
    "................",
    "................",
]

CABBAGE = [
    "................",
    "................",
    "................",
    ".....oooooo.....",
    "....oGgglggo....",
    "...oGglhhlggo...",
    "..oGglhlhhlgo...",
    "..oGglhlhlggGo..",
    "..oGgglhlggGGo..",
    "..oGGgglgggGGo..",
    "...oGGgggggGo...",
    "...oGGGgGGGGo...",
    "....ooGGGGoo....",
    "......oooo......",
    "................",
    "................",
]

ONION = [
    "................",
    "........g.......",
    ".......gG.......",
    ".......gG.......",
    "......oyyo......",
    ".....oyhyYo.....",
    "....oyhyyyYo....",
    "...oyhyyyyyYo...",
    "...oyyyyyyyYo...",
    "...oYyyyyyYYo...",
    "....oYyyyYYo....",
    ".....oYYYYo.....",
    "......obbo......",
    ".......bb.......",
    "................",
    "................",
]

SEEDS = [
    "................",
    "................",
    "................",
    "................",
    "................",
    "........oo......",
    ".......ohlo.....",
    "...oo..olmo.....",
    "..ohlo..oo..oo..",
    "..olmo.....ohlo.",
    "...oo...oo.olmo.",
    "........ohlo.oo.",
    "........olmo....",
    ".........oo.....",
    "................",
    "................",
]

BARLEY = [
    "................",
    "..y...y...y.....",
    "...y..y..y......",
    "..oyo.yoy.oyo...",
    "..oYyoyYyoYyo...",
    "...oYyoYyoYo....",
    "...oYYoYYoYo....",
    "....oYoYYoo.....",
    ".....oSSSo......",
    "......SSS.......",
    "......SSS.......",
    ".....sSSSs......",
    "......SSS.......",
    "......SSS.......",
    "......sss.......",
    "................",
]

BREAD_ROLL = [
    "................",
    "................",
    "................",
    "................",
    ".....oooooo.....",
    "...oohhllllo....",
    "..ohlllcllllo...",
    ".ohlllcllcllmo..",
    ".olllcllcllmmo..",
    ".olmllllcllmmo..",
    ".ommmmmmmmmmdo..",
    "..odmmmmmmmddo..",
    "...ooddddddoo...",
    ".....oooooo.....",
    "................",
    "................",
]

STEW = [
    "................",
    "................",
    "................",
    "................",
    "................",
    "...ssssssssss...",
    "..sRrrgrrrkrrs..",
    ".sRrkrrrrgrrrrs.",
    ".obrrrrkrrrrgro.",
    ".obbbbbbbbbbbbo.",
    "..obbhbbbbbbbo..",
    "..obbbbbbbbbdo..",
    "...obbbbbbbdo...",
    "....oddddddo....",
    ".....oooooo.....",
    "................",
]

PIE = [
    "................",
    "................",
    "................",
    "................",
    "......oooo......",
    "...oooccccooo...",
    "..occcccccccco..",
    ".occfcccccfccco.",
    ".occcccfccccccc.",
    ".oCcccccccccCCo.",
    ".oFFFFFFFFFFFFo.",
    ".oCCCCCCCCCCCCo.",
    "..oCCCCCCCCCCo..",
    "...oooooooooo...",
    "................",
    "................",
]

LATTICE_PIE = [
    "................",
    "................",
    "................",
    "................",
    "......oooo......",
    "...oooffffooo...",
    "..occfccfccfco..",
    ".occccffcccffco.",
    ".offcccffccccco.",
    ".oCcffcccffcCCo.",
    ".oFFFFFFFFFFFFo.",
    ".oCCCCCCCCCCCCo.",
    "..oCCCCCCCCCCo..",
    "...oooooooooo...",
    "................",
    "................",
]


def item_sprites() -> None:
    steel = ramp("#2e333a", "#5d6670", "#7f8994", "#a6b0bb", "#d5dde6")
    save(sprite(INGOT, tone(steel)), ITEM, "steel_ingot")
    dust = {"h": hexc("#c4ccd4"), "l": hexc("#9aa3ad"), "m": hexc("#727b86"), "d": hexc("#4f5660"),
            "x": hexc("#1a1b1e")}
    save(sprite(DUST, dust), ITEM, "steel_blend")

    save(sprite(LUMP, tone(ORE_PALETTES["carbon"])), ITEM, "carbon")
    save(sprite(CRYSTAL_CLUSTER, tone(ORE_PALETTES["konterit"])), ITEM, "konterit")
    save(sprite(TALL_CRYSTAL, tone(ORE_PALETTES["aetherit"])), ITEM, "aetherit")
    save(sprite(TWIN_CRYSTAL, tone(ORE_PALETTES["draconium"])), ITEM, "draconium")
    save(sprite(SHARD, tone(ORE_PALETTES["wyvernit"])), ITEM, "wyvernit")

    purple, green, cyan = GEM_COLORS
    gems = {
        "P": purple[0], "p": purple[3], "w": purple[4], "q": purple[2], "r": purple[1],
        "G": green[0], "g": green[3], "h": green[2], "j": green[1],
        "C": cyan[0], "c": cyan[3], "k": cyan[1],
    }
    # Das "w" wird für die Glanzpunkte aller Steine verwendet
    save(sprite(GEM_CLUSTER, gems), ITEM, "gemstone")

    fragment = ramp("#2e0f44", "#5b2384", "#8a3cc0", "#b874ee", "#e6c8ff")
    save(sprite(EGG_FRAGMENT, dict(tone(fragment), i=hexc("#f2e6d0"))), ITEM, "dragon_egg_fragment")

    for kind, pal in DRAGON_PALETTES.items():
        colors = {"o": pal[0], "d": pal[1], "m": pal[2], "l": pal[3], "h": pal[4]}
        save(sprite(SCALE, colors), ITEM, f"{kind}_dragon_scale")

    meat = {"o": hexc("#4a1010"), "R": hexc("#9a2020"), "r": hexc("#c8403a"), "h": hexc("#e8706a"),
            "f": hexc("#f0d0c0"), "w": hexc("#5a4a3a"), "b": hexc("#e8e0cc"),
            "e": hexc("#2a0a3a"), "E": hexc("#6a1a7a")}
    save(sprite(MEAT, meat), ITEM, "dragon_food")

    save(sprite(TOMATO, {"o": hexc("#5a0e08"), "R": hexc("#a8200f"), "r": hexc("#d8341e"), "h": hexc("#ff8a6a"),
                         "g": hexc("#2f6a1a"), "G": hexc("#4f9a2a")}), ITEM, "tomato")
    save(sprite(CABBAGE, {"o": hexc("#1f4a12"), "G": hexc("#3f7a24"), "g": hexc("#6aae44"), "l": hexc("#94d06a"),
                          "h": hexc("#c4ef9e")}), ITEM, "cabbage")
    save(sprite(ONION, {"o": hexc("#5a3812"), "y": hexc("#d8a84e"), "Y": hexc("#a8762a"), "h": hexc("#f4d48a"),
                        "g": hexc("#4f9a2a"), "G": hexc("#2f6a1a"), "b": hexc("#8a6a4a")}), ITEM, "onion")

    save(sprite(SEEDS, {"o": hexc("#6a5a1a"), "h": hexc("#f0e08a"), "l": hexc("#d8c25a"), "m": hexc("#b49a3a")}),
         ITEM, "tomato_seeds")
    save(sprite(SEEDS, {"o": hexc("#2a1a0e"), "h": hexc("#8a6a4a"), "l": hexc("#6a4a2e"), "m": hexc("#4a321e")}),
         ITEM, "cabbage_seeds")
    save(sprite(SEEDS, {"o": hexc("#5a4214"), "h": hexc("#e8cf8a"), "l": hexc("#c8a65a"), "m": hexc("#a0803a")}),
         ITEM, "barley_seeds")
    save(sprite(BARLEY, {"o": hexc("#7a5a14"), "y": hexc("#f0d878"), "Y": hexc("#d4ac3e"), "S": hexc("#b89a3e"),
                         "s": hexc("#8a7228")}), ITEM, "barley")

    save(sprite(BREAD_ROLL, {"o": hexc("#5a320e"), "h": hexc("#f2c47a"), "l": hexc("#dc9c4a"), "m": hexc("#b87a32"),
                             "d": hexc("#8a5420"), "c": hexc("#f8e0b0")}), ITEM, "bread_roll")
    save(sprite(STEW, {"o": hexc("#3a2414"), "b": hexc("#7a5030"), "h": hexc("#a0703e"), "d": hexc("#5a3a20"),
                       "s": hexc("#4a2e18"), "R": hexc("#a83a1a"), "r": hexc("#d0602a"), "g": hexc("#5aa02a"),
                       "k": hexc("#f0a030")}), ITEM, "vegetable_stew")
    save(sprite(PIE, {"o": hexc("#5a3010"), "c": hexc("#e0a85a"), "f": hexc("#7a3a1a"), "C": hexc("#c08440"),
                      "F": hexc("#8a3420")}), ITEM, "meat_pie")
    save(sprite(LATTICE_PIE, {"o": hexc("#5a3010"), "c": hexc("#c83a2a"), "f": hexc("#f0c070"), "C": hexc("#c08440"),
                              "F": hexc("#e8d070")}), ITEM, "apple_pie")


# =============================================================================
# Feldfrüchte (vier Wachstumsstufen) und Zierblumen
# =============================================================================

PLANT = {"s": hexc("#2f6a18"), "S": hexc("#3f8a22"), "L": hexc("#5aa82e"), "l": hexc("#7ac84a"),
         "k": hexc("#8a6a3a")}

TOMATO_STAGES = [
    ["................"] * 12 + [
        "................",
        "....l.....l.....",
        "....S..l..S..l..",
        "....s..S..s..S..",
    ],
    ["................"] * 8 + [
        "......k.........",
        "....l.k.l.......",
        "...lLLkLl.......",
        "....LSkS..l.....",
        ".....SkS.lL.....",
        "......k..S......",
        "......k..s......",
        "......k.........",
    ],
    ["................"] * 4 + [
        "......k.........",
        "....l.k.l.......",
        "...lLLkLLl......",
        "..lLSSkSSLl.....",
        "...LSakSaL......",
        "....SSkSS..l....",
        "..lLLSkSLLlL....",
        "...LSakSaSL.....",
        "....SSkSSs......",
        ".....SkS........",
        "......k.........",
        "......k.........",
    ],
    ["................"] * 3 + [
        "......k.........",
        "....l.k.l.......",
        "...lLLkLLl......",
        "..lLSrkSrLl.....",
        "...LrRkSRrL.....",
        "....rRkSrr.l....",
        "..lLLSkSLLlL....",
        "...LrrkrRSL.....",
        "...rRRkRRr......",
        "....rSkSr.......",
        ".....SkS........",
        "......k.........",
        "......k.........",
    ],
]

CABBAGE_STAGES = [
    ["................"] * 13 + [
        "................",
        "......l..l......",
        "......S..S......",
    ],
    ["................"] * 11 + [
        "................",
        "....l......l....",
        "....LL.ll.LL....",
        ".....LLSSLL.....",
        "......SSSS......",
    ],
    ["................"] * 9 + [
        "................",
        "...l........l...",
        "...LL.llll.LL...",
        "....LLllllLL....",
        "...lLLggggLLl...",
        "....LLgGGgLL....",
        ".....SSSSSS.....",
    ],
    ["................"] * 7 + [
        "................",
        "..l..........l..",
        "..LL..gggg..LL..",
        "...LLghhhhgLL...",
        "..lLghhGhhhgLl..",
        "...LghGhhGhgL...",
        "...LgghhGhhgL...",
        "....LggggggL....",
        ".....SSSSSS.....",
    ],
]

ONION_STAGES = [
    ["................"] * 13 + [
        "................",
        "....l....l......",
        "....S....S...l..",
    ],
    ["................"] * 10 + [
        "....l.....l.....",
        "....L..l..L.....",
        "....L..L..L..l..",
        "...lS..L..S..L..",
        "....S..S..S..S..",
        "....s..s..s..s..",
    ],
    ["................"] * 6 + [
        "....l.....l.....",
        "....L..l..L..l..",
        "...lL..L..L..L..",
        "....L..L.lL..L..",
        "....L.lL..L.lL..",
        "...lS..L..S..L..",
        "....S..S..S..S..",
        "....S..S..S..S..",
        "....S..S..S..S..",
        "....s..s..s..s..",
    ],
    ["................"] * 5 + [
        "....l.....l.....",
        "....L..l..L..l..",
        "...lL..L..L..L..",
        "....L..L.lL..L..",
        "....L.lL..L.lL..",
        "...lS..L..S..L..",
        "....S..S..S..S..",
        "....S..S..S..S..",
        "...yYy.S.yYy.S..",
        "..yhYYy.yhYYy...",
        "..yYYYy.yYYYy...",
    ],
]

BARLEY_STAGES = [
    ["................"] * 13 + [
        "................",
        "..l...l...l..l..",
        "..L...L...L..L..",
    ],
    ["................"] * 10 + [
        "..l...l....l....",
        "..L...L..l.L..l.",
        "..L.l.L..L.L..L.",
        "..L.L.L..L.L..L.",
        "..S.L.S..L.S..L.",
        "..S.S.S..S.S..S.",
    ],
    ["................"] * 6 + [
        "..L...L....L....",
        ".lLl.lLl..lLl...",
        "..L...L....L..L.",
        "..L.L.L..L.L.lLl",
        "..L.L.L.lLlL..L.",
        "..L.L.L..L.L..L.",
        "..S.L.S..L.S..L.",
        "..S.L.S..L.S..L.",
        "..S.S.S..S.S..S.",
        "..S.S.S..S.S..S.",
    ],
    ["................"] * 3 + [
        ".y..y...y...y...",
        "..y.y..y...y....",
        "..Y...Y....Y....",
        ".yYy.yYy..yYy...",
        "..Y...Y....Y..Y.",
        ".yYy.yYy..yYy.Y.",
        "..Y.Y.Y..Y.Y.yYy",
        "..b.Y.b.yYyb..Y.",
        "..b.b.b..Y.b.yYy",
        "..b.b.b..b.b..b.",
        "..b.b.b..b.b..b.",
        "..b.b.b..b.b..b.",
        "..B.b.B..b.B..b.",
    ],
]


def crop_sprites() -> None:
    tomato = dict(PLANT, r=hexc("#d8341e"), R=hexc("#a8200f"), a=hexc("#8ac04a"))
    cabbage = dict(PLANT, g=hexc("#7ac050"), G=hexc("#5aa03a"), h=hexc("#b8e88e"))
    onion = dict(PLANT, y=hexc("#d8a84e"), Y=hexc("#a8762a"), h=hexc("#f4d48a"))
    barley = dict(PLANT, y=hexc("#f0d878"), Y=hexc("#d4ac3e"), b=hexc("#c8a648"), B=hexc("#a0822e"))
    for stages, name, colors in [(TOMATO_STAGES, "tomatoes", tomato), (CABBAGE_STAGES, "cabbages", cabbage),
                                 (ONION_STAGES, "onions", onion), (BARLEY_STAGES, "barley", barley)]:
        for i, rows in enumerate(stages):
            save(sprite(rows, colors), BLOCK, f"{name}_stage{i}")


LAVENDER = [
    "................",
    "...p......p.....",
    "..pPp....pPp....",
    "...P..p...P.....",
    "..pPp.Pp.pPp....",
    "...P.pPp..P..p..",
    "..pPp.P..pPp.P..",
    "...S.pPp..S.pPp.",
    "...S..S...S..P..",
    "...S..S..lS..S..",
    "..lS..S..LS..S..",
    "..LS..S...S.lS..",
    "...S.lS...S.LS..",
    "...S.LS...S..S..",
    "...S..S...S..S..",
    "...s..s...s..s..",
]

ROSE = [
    "................",
    "................",
    "......oooo......",
    ".....oRrrRo.....",
    "....oRrhrrRo....",
    "....oRrRrrRo....",
    ".....oRRrRo.....",
    "......oSSo......",
    ".......S...ll...",
    ".......S..lLL...",
    "...ll..S.lL.....",
    "..LLLl.SS.......",
    ".....LlS........",
    ".......S........",
    ".......S........",
    ".......s........",
]

LILY = [
    "................",
    "..o.........o...",
    "..oRo.....oRo...",
    "...oRo...oRo....",
    "....oRrorRo.....",
    ".oRRrryyrrRRo...",
    "..oorryYyrroo...",
    "....oRrSrRo.....",
    "...oRo.S.oRo....",
    "..oo...S...oo...",
    ".......S..ll....",
    "...ll..S.lL.....",
    "....LL.S.L......",
    ".....LlS........",
    ".......S........",
    ".......s........",
]

FROST = [
    "................",
    "................",
    ".......w........",
    "....w..b..w.....",
    ".....bwBwb......",
    "...wbBcCcBbw....",
    ".....bwBwb......",
    "....w..b..w.....",
    ".......t........",
    ".......t..tt....",
    "...tt..t.tT.....",
    "....TT.t.T......",
    ".....TtT........",
    ".......t........",
    ".......t........",
    ".......u........",
]


def flower_sprites() -> None:
    save(sprite(LAVENDER, dict(PLANT, p=hexc("#b48ae8"), P=hexc("#7a48b8"))), BLOCK, "lavender")
    save(sprite(ROSE, dict(PLANT, o=hexc("#5a0a14"), R=hexc("#a01224"), r=hexc("#d8283a"), h=hexc("#ff7a86"))),
         BLOCK, "garden_rose")
    save(sprite(LILY, dict(PLANT, o=hexc("#8a1e0a"), R=hexc("#e0501a"), r=hexc("#ff9a2a"), y=hexc("#ffe04a"),
                           Y=hexc("#c88a10"))), BLOCK, "fire_lily")
    save(sprite(FROST, {"w": hexc("#ffffff"), "b": hexc("#bfe6ff"), "B": hexc("#7ab8e8"), "c": hexc("#5ad8f0"),
                        "C": hexc("#2a98c0"), "t": hexc("#5a8a9a"), "T": hexc("#7aaab8"), "u": hexc("#3a6a7a")}),
         BLOCK, "frost_flower")


# =============================================================================
# Vorschau
# =============================================================================

def preview(path: Path) -> None:
    zoom, pad = 6, 4
    cols = 12
    cell = 16 * zoom + pad
    rows = math.ceil(len(GENERATED) / cols)
    sheet = Image.new("RGBA", (cols * cell + pad, rows * cell + pad), (60, 70, 60, 255))
    checker = Image.new("RGBA", (16 * zoom, 16 * zoom))
    for y in range(16):
        for x in range(16):
            c = (120, 120, 120, 255) if (x + y) % 2 else (90, 90, 90, 255)
            for yy in range(zoom):
                for xx in range(zoom):
                    checker.putpixel((x * zoom + xx, y * zoom + yy), c)
    for i, (_, _, img) in enumerate(GENERATED):
        x = pad + (i % cols) * cell
        y = pad + (i // cols) * cell
        sheet.alpha_composite(checker, (x, y))
        sheet.alpha_composite(img.resize((16 * zoom, 16 * zoom), Image.NEAREST), (x, y))
    sheet.save(path)


def main() -> None:
    building_blocks()
    timber()
    roofs()
    resources_blocks()
    dragon_eggs()
    item_sprites()
    crop_sprites()
    flower_sprites()
    print(f"{len(GENERATED)} Texturen geschrieben nach {TEXTURES}")
    if "--preview" in sys.argv:
        target = Path(sys.argv[sys.argv.index("--preview") + 1]) if len(sys.argv) > sys.argv.index("--preview") + 1 \
            else ROOT / "build" / "texture_preview.png"
        target.parent.mkdir(parents=True, exist_ok=True)
        preview(target)
        print(f"Vorschau: {target}")


if __name__ == "__main__":
    main()
