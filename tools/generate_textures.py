#!/usr/bin/env python3
"""
Erzeugt alle 16x16-Texturen der Mod "Kingdoms at War" als eigene Pixelkunst im Vanilla-Stil.

Es werden KEINE Original-Minecraft-Texturen verwendet. Die Erze bestehen nur aus
"Erz-Kristallen" mit durchsichtigem Hintergrund – der Stein dahinter kommt zur Laufzeit
aus dem Spiel (siehe ModModelProvider, Vorlage ORE_OVERLAY). Für Aetherit und Draconium gibt es
zusätzlich "Leucht"-Ebenen (*_ore_glow.png) mit den hellsten Kristallpixeln.

Die Bausteine (Paletten, ASCII-Sprites, Rauschen, Ränder) liegen in tools/pixelart.py.

Aufruf (aus dem Projektordner):
    python3 tools/generate_textures.py                       # schreibt alle Texturen
    python3 tools/generate_textures.py --preview datei.png   # zusätzlich eine vergrößerte Übersicht

Benötigt: Pillow (pip install pillow)
"""
from __future__ import annotations

import math
import random
import sys
from pathlib import Path

from PIL import Image

sys.path.insert(0, str(Path(__file__).resolve().parent))
import pixelart as pa  # noqa: E402
from pixelart import CLEAR, Color, hexc, palette, pick  # noqa: E402

ROOT = Path(__file__).resolve().parent.parent
TEXTURES = ROOT / "src" / "main" / "resources" / "assets" / "kingdomsatwar" / "textures"

# Alle erzeugten Texturen: "block/name" bzw. "item/name" -> Bild
GENERATED: dict[str, Image.Image] = {}


def put(key: str, img: Image.Image) -> None:
    if key in GENERATED:
        raise ValueError(f"Textur doppelt erzeugt: {key}")
    GENERATED[key] = img


# =============================================================================
# 1. Steinblöcke für Burgen und Festungen
# =============================================================================

def masonry(layout, pal: list[Color], mortar: list[Color], seed: int, *, tone: float = 0.16,
            grain: float = 0.22, bevel: float = 0.22, shadow: float = 0.2, pits: int = 1,
            chips: float = 0.0, tint: float = 0.0) -> Image.Image:
    """
    Mauerwerk aus einzelnen Steinen (kachelbar).
    layout: siehe pixelart.bricks(); pal: Steinfarben dunkel->hell; mortar: [dunkel, hell].
    tone: Helligkeitsunterschied zwischen den Steinen, grain: feines Rauschen im Stein,
    bevel/shadow: Kante oben-links heller bzw. unten-rechts dunkler, pits: dunkle Poren je Stein,
    chips: Anteil abgeschlagener Ecken, tint: leichte Farbabweichung je Stein (warm/kühl).
    """
    rng = random.Random(seed)
    grid = pa.bricks(layout)
    dims = pa.stone_dims(grid)
    base = {sid: 0.5 + rng.uniform(-tone, tone) for sid in dims}
    hue = {sid: rng.choice([-1, 0, 0, 1]) * tint for sid in dims}
    noise = pa.fractal_noise(seed, ((4, 0.55), (2, 0.3), (8, 0.15)))
    img = pa.new()
    px = img.load()
    for y in range(16):
        for x in range(16):
            sid, dx, dy = grid[y][x]
            if sid < 0:
                # Fuge: Kreuzungen etwas dunkler
                vertical = grid[(y + 1) % 16][x][0] < 0 and grid[(y - 1) % 16][x][0] < 0
                horizontal = grid[y][(x + 1) % 16][0] < 0 and grid[y][(x - 1) % 16][0] < 0
                px[x, y] = mortar[0] if (vertical and horizontal) or noise[y][x] < 0.4 else mortar[1]
                continue
            w, h = dims[sid]
            v = base[sid] + (noise[y][x] - 0.5) * grain
            if dy == 0 or dx == 0:
                v += bevel
            if dy == h - 1 or dx == w - 1:
                v -= shadow
            c = pick(pal, v)
            if hue[sid]:
                # warm (Richtung Ocker) bzw. kühl (Richtung Blaugrau)
                c = pa.shift(c, hue=0.0, sat=abs(hue[sid]) * 0.6, light=0.0)
                c = pa.mix(c, hexc("#a08060") if hue[sid] > 0 else hexc("#607080"), abs(hue[sid]))
            px[x, y] = c
    # dunkle Poren / Kerben
    for sid, (w, h) in dims.items():
        cells = [(x, y) for y in range(16) for x in range(16) if grid[y][x][0] == sid
                 and 0 < grid[y][x][1] < w - 1 and 0 < grid[y][x][2] < h - 1]
        for _ in range(pits):
            if cells and rng.random() < 0.75:
                x, y = rng.choice(cells)
                px[x, y] = pal[max(0, pal.index(px[x, y]) - 2)] if px[x, y] in pal else pa.shift(px[x, y], light=-0.09)
                if rng.random() < 0.5:
                    x2 = (x + 1) % 16
                    if grid[y][x2][0] == sid:
                        px[x2, y] = pal[max(0, pal.index(px[x2, y]) - 1)] if px[x2, y] in pal else pa.shift(px[x2, y], light=-0.05)
    # abgeschlagene Ecken
    if chips > 0:
        for y in range(16):
            for x in range(16):
                sid, dx, dy = grid[y][x]
                if sid < 0:
                    continue
                w, h = dims[sid]
                corner = (dx in (0, w - 1)) and (dy in (0, h - 1))
                if corner and rng.random() < chips:
                    px[x, y] = mortar[1]
    return img


def add_cracks(img: Image.Image, seed: int, count: int, dark: Color, light: Color, length: int = 7) -> None:
    """Risse als Zufallspfade: dunkle Linie mit hellem Saum (Licht von oben links)."""
    rng = random.Random(seed)
    px = img.load()
    for _ in range(count):
        x, y = rng.randrange(16), rng.randrange(16)
        dx, dy = rng.choice([(1, 1), (1, 0), (0, 1), (1, -1)])
        for _ in range(length + rng.randrange(4)):
            px[x % 16, y % 16] = dark
            hx, hy = (x - 1) % 16, (y - 1) % 16
            if px[hx, hy] != dark and rng.random() < 0.5:
                px[hx, hy] = light
            if rng.random() < 0.45:
                dx, dy = rng.choice([(1, 1), (1, 0), (0, 1), (1, -1), (dx, dy)])
            x, y = x + dx, y + dy


def add_growth(img: Image.Image, seed: int, pal: list[Color], amount: float, near: set | None = None,
               bias_top: float = 0.0) -> None:
    """Moos oder Flechten in Büscheln (kachelbar). near: Pixel, an denen Bewuchs bevorzugt wird."""
    noise = pa.fractal_noise(seed, ((4, 0.6), (2, 0.4)))
    detail = pa.value_noise(seed + 99, 2)
    px = img.load()
    for y in range(16):
        for x in range(16):
            v = noise[y][x]
            if near is not None and (x, y) in near:
                v += 0.18
            v += bias_top * (1 - y / 15)
            if v > 1 - amount:
                strength = (v - (1 - amount)) / amount + (detail[y][x] - 0.5) * 0.5
                px[x, y] = pick(pal, 0.25 + strength * 0.75)


STONE = palette("#5f5c5b", "#6d6a67", "#7a7672", "#87837e", "#95908a", "#a5a097")
STONE_MORTAR = palette("#464343", "#545150")


def stone_blocks() -> None:
    # Mittelalterlicher Stein: unregelmäßige Quader in drei Lagen
    put("block/medieval_stone", masonry(
        [(6, [7, 9], 0), (5, [5, 6, 5], 3), (5, [9, 7], 6)], STONE, STONE_MORTAR, 11, pits=2, tint=0.07))

    # Grober Naturstein: runde Feldsteine verschiedener Farben in dunklem Mörtel
    put("block/rough_fieldstone", fieldstone(21))

    # Behauener Sandstein: große glatte Blöcke mit feinen Sedimentlinien
    sand = palette("#a3824f", "#b38f5b", "#c29d67", "#cfab74", "#dbb982", "#e6c793")
    img = masonry([(8, [10, 6], 2), (8, [7, 9], 9)], sand, palette("#87683f", "#94764a"), 31,
                  tone=0.1, grain=0.12, bevel=0.2, shadow=0.18, pits=0)
    px = img.load()
    for y in (2, 5, 10, 13):
        for x in range(16):
            c = px[x, y]
            if c in sand and (x * 7 + y * 3) % 5 != 0:
                px[x, y] = sand[max(0, sand.index(c) - 1)]
    put("block/hewn_sandstone", img)

    # Dunkle Steinziegel
    dark = palette("#2b2c33", "#353741", "#40424c", "#4b4e58", "#575a65", "#666a75")
    put("block/dark_stone_bricks", masonry(
        [(4, [8, 8], 0), (4, [8, 8], 4), (4, [8, 8], 0), (4, [8, 8], 4)], dark, palette("#1a1b20", "#22232a"), 41,
        tone=0.12, grain=0.16, bevel=0.26, shadow=0.16))

    # Helle Kalksteinziegel
    lime = palette("#b3ab9b", "#c2baa9", "#cfc8b7", "#dad4c4", "#e4dfd1", "#eeeae0")
    put("block/limestone_bricks", masonry(
        [(4, [8, 8], 2), (4, [8, 8], 6), (4, [8, 8], 2), (4, [8, 8], 6)], lime, palette("#9a9385", "#a69f91"), 51,
        tone=0.1, grain=0.12, bevel=0.18, shadow=0.2, pits=0))

    # Verwitterte Steinziegel: Flechten, abgeschlagene Ecken, kleiner Riss
    weathered = palette("#5e5c55", "#6b6861", "#78746c", "#858077", "#918c82", "#9f998e")
    img = masonry([(5, [9, 7], 0), (5, [7, 9], 5), (6, [8, 8], 2)], weathered, palette("#45433e", "#504d47"), 61,
                  tone=0.2, grain=0.26, pits=2, chips=0.35, tint=0.06)
    add_growth(img, 64, palette("#7d7b55", "#8f8c5e", "#a3a06b", "#b6b27a"), 0.16)
    add_cracks(img, 62, 1, hexc("#3f3d39"), hexc("#a39d92"), 5)
    put("block/weathered_stone_bricks", img)

    # Moosige Mauersteine: Moos wächst bevorzugt in den Fugen und auf den Oberkanten
    layout = [(6, [7, 9], 0), (5, [5, 6, 5], 3), (5, [9, 7], 6)]
    img = masonry(layout, STONE, STONE_MORTAR, 71, pits=1, tint=0.06)
    grid = pa.bricks(layout)
    near = {(x, y) for y in range(16) for x in range(16)
            if grid[y][x][0] < 0 or grid[(y - 1) % 16][x][0] < 0 or grid[y][(x - 1) % 16][0] < 0}
    add_growth(img, 72, palette("#3a4f22", "#455e28", "#526e2e", "#617f36", "#70903f"), 0.27, near)
    put("block/mossy_wall_stones", img)

    # Gerissene Steinblöcke: große Blöcke mit durchgehenden Rissen
    cool = palette("#5c5b5c", "#69686a", "#767578", "#838286", "#919094", "#a09fa3")
    img = masonry([(8, [8, 8], 0), (8, [8, 8], 0)], cool, palette("#444346", "#504f52"), 81,
                  tone=0.14, grain=0.24, pits=2, chips=0.2)
    add_cracks(img, 82, 3, hexc("#38373a"), hexc("#a8a7aa"), 7)
    put("block/cracked_stone_blocks", img)

    # Steinpfeiler
    pillar_side, pillar_top = stone_pillar()
    put("block/stone_pillar_side", pillar_side)
    put("block/stone_pillar_top", pillar_top)


def fieldstone(seed: int) -> Image.Image:
    """Feldsteine: Voronoi-Zellen mit runder Schattierung (Licht oben links)."""
    rng = random.Random(seed)
    cells, border, pts = pa.voronoi(seed, 9, metric="euclid")
    families = [
        palette("#55524e", "#64605b", "#736f69", "#827d76", "#928c84"),   # warmes Grau
        palette("#4d4d52", "#5b5b61", "#69696f", "#78787e", "#88888e"),   # kühles Grau
        palette("#5f574c", "#6e6559", "#7e7466", "#8e8474", "#9e9383"),   # beigegrau
        palette("#564c43", "#655a4f", "#75695c", "#857869", "#958877"),   # braungrau
    ]
    weights = [0.4, 0.27, 0.18, 0.15]
    choice = [rng.choices(families, weights)[0] for _ in pts]
    gap = palette("#302b26", "#3c3630")
    noise = pa.value_noise(seed + 5, 2)
    img = pa.new()
    px = img.load()
    for y in range(16):
        for x in range(16):
            i = cells[y][x]
            if border[y][x] < 0.75:
                px[x, y] = gap[0] if noise[y][x] < 0.5 else gap[1]
                continue
            cx, cy = pts[i]
            ox = ((x + 0.5 - cx + 8) % 16) - 8
            oy = ((y + 0.5 - cy + 8) % 16) - 8
            v = 0.6 - (ox + oy) * 0.075 + (noise[y][x] - 0.5) * 0.16
            if border[y][x] < 1.4:
                v -= 0.2
            px[x, y] = pick(choice[i], v)
    return img


def stone_pillar() -> tuple[Image.Image, Image.Image]:
    """Kannelierter Pfeiler (Seite) und runde Deckplatte (oben)."""
    pal = palette("#5b5856", "#6b6865", "#7b7773", "#8b8782", "#9b968f", "#aca69d")
    noise = pa.fractal_noise(91, ((4, 0.6), (2, 0.4)))
    side = pa.new()
    px = side.load()
    # Profil quer zum Pfeiler: Kanten dunkel, Kanneluren (Rillen) im Wechsel hell/dunkel
    profile = [0.12, 0.62, 0.78, 0.55, 0.35, 0.7, 0.82, 0.58, 0.36, 0.68, 0.8, 0.56, 0.34, 0.64, 0.5, 0.2]
    for y in range(16):
        for x in range(16):
            v = profile[x] + (noise[y][x] - 0.5) * 0.14
            px[x, y] = pick(pal, v)
    top = pa.new()
    px = top.load()
    for y in range(16):
        for x in range(16):
            v = 0.56 + (noise[y][x] - 0.5) * 0.12
            d = math.hypot(x - 7.5, y - 7.5)
            if x in (0, 15) or y in (0, 15):
                v = 0.14
            elif x == 1 or y == 1:
                v = 0.8
            elif x == 14 or y == 14:
                v = 0.3
            elif 5.4 < d < 6.4:
                # eingemeißelter Kreis: oben links Schatten, unten rechts Licht (Vertiefung)
                v = 0.28 if (x + y) < 15 else 0.78
            elif d <= 5.4:
                v = 0.62 - (x + y - 15) * 0.015 + (noise[y][x] - 0.5) * 0.1
            px[x, y] = pick(pal, v)
    return side, top


# =============================================================================
# 2. Holzblöcke und Fachwerk
# =============================================================================

WOOD = palette("#2f1f12", "#3e2918", "#4f3520", "#614229", "#735033", "#865f3d")
PLASTER = palette("#c9bfa8", "#d6cdb8", "#e1d9c6", "#ebe5d5", "#f3eee3")


def wood_grain(seed: int, vertical: bool = True, knots: int = 1) -> Image.Image:
    """Holzmaserung entlang der Faser (kachelbar in Faserrichtung)."""
    rng = random.Random(seed)
    lines = pa.value_noise(seed, 4)
    img = pa.new()
    px = img.load()
    stripe = [rng.uniform(0.25, 0.75) for _ in range(16)]
    for y in range(16):
        for x in range(16):
            a, b = (x, y) if vertical else (y, x)
            wobble = math.sin((b + seed) * 0.8 + a) * 0.08
            v = stripe[a] + (lines[y][x] - 0.5) * 0.3 + wobble
            if a % 4 == 3:
                v -= 0.18
            px[x, y] = pick(WOOD, v)
    for _ in range(knots):
        kx, ky = rng.randrange(16), rng.randrange(16)
        pa.stamp(img, [".a.", "aba", ".a."], {"a": WOOD[1], "b": WOOD[0]}, kx, ky)
    return img


def timber_blocks() -> None:
    put("block/timber_beam_side", wood_grain(101, vertical=True, knots=1))
    # Stirnseite: Jahresringe in einem quadratischen Balken
    top = pa.new()
    px = top.load()
    noise = pa.value_noise(103, 2)
    for y in range(16):
        for x in range(16):
            if x in (0, 15) or y in (0, 15):
                px[x, y] = WOOD[1] if (x + y) % 3 else WOOD[0]
                continue
            d = math.hypot(x - 7.3, y - 7.8) + (noise[y][x] - 0.5) * 0.8
            ring = int(d) % 3
            v = (0.78, 0.6, 0.38)[ring] - d * 0.02
            px[x, y] = pick(WOOD, v)
    put("block/timber_beam_top", top)

    for name, diagonals in (("timber_frame", []), ("timber_frame_diagonal", ["/"]), ("timber_frame_cross", ["/", "\\"])):
        put(f"block/{name}", timber_frame(111 + len(diagonals), diagonals))


def timber_frame(seed: int, diagonals: list[str]) -> Image.Image:
    """Weißer Putz mit dunklen Holzbalken am Rand (2 px) und optionalen Diagonalen."""
    noise = pa.fractal_noise(seed, ((4, 0.5), (2, 0.5)))
    grain = wood_grain(seed + 7, vertical=True, knots=0)
    grain_h = wood_grain(seed + 8, vertical=False, knots=0)
    img = pa.new()
    px = img.load()
    beam = [[False] * 16 for _ in range(16)]
    for y in range(16):
        for x in range(16):
            if x < 2 or x > 13 or y < 2 or y > 13:
                beam[y][x] = True
            for d in diagonals:
                t = x - (15 - y) if d == "/" else x - y
                if -1 <= t <= 0 and 1 < x < 14 and 1 < y < 14:
                    beam[y][x] = True
    for y in range(16):
        for x in range(16):
            if beam[y][x]:
                horizontal = (y < 2 or y > 13) and 1 < x < 14
                src = grain_h if horizontal else grain
                c = src.getpixel((x, y))
                # Balkenkante zum Putz hin etwas heller (Licht oben links)
                if (x == 1 and 1 < y < 14) or (y == 1 and 1 < x < 14):
                    c = pa.shift(c, light=0.05)
                px[x, y] = c
            else:
                v = 0.55 + (noise[y][x] - 0.5) * 0.45
                # Schatten der Balken auf dem Putz (unten/rechts eines Balkens)
                if beam[y - 1][x] or beam[y][x - 1]:
                    v -= 0.35
                px[x, y] = pick(PLASTER, v)
    return img


# =============================================================================
# 3. Dachblöcke
# =============================================================================

def roof_tiles(pal: list[Color], seed: int, tile_w: int = 4, row_h: int = 4) -> Image.Image:
    """
    Gewölbte Dachziegel in versetzten Reihen. Jede Reihe überlappt die darunterliegende:
    oben Schattenkante, Mitte hell, unten gerundete Lippe.
    pal: [Fuge, dunkel, mittel, hell, Glanz]
    """
    rng = random.Random(seed)
    shape = [  # Werte je Ziegel (Breite 4, Höhe 4): 0 Fuge, 1 dunkel, 2 mittel, 3 hell, 4 Glanz
        [1, 1, 1, 0],
        [3, 4, 3, 1],
        [3, 3, 2, 1],
        [0, 2, 2, 0],
    ]
    img = pa.new()
    px = img.load()
    tones = {}
    for y in range(16):
        row = y // row_h
        offset = (row % 2) * (tile_w // 2)
        for x in range(16):
            col = ((x + offset) // tile_w) % (16 // tile_w)
            key = (row, col)
            if key not in tones:
                tones[key] = rng.choice([0, 0, 0, 1, -1])
            v = shape[y % row_h][(x + offset) % tile_w]
            if v > 0:
                v = max(1, min(4, v + tones[key] if v > 1 else v))
            px[x, y] = pal[v]
    return img


def roofs() -> None:
    put("block/red_roof_tiles", roof_tiles(palette("#4e1a12", "#7a2919", "#9a3a22", "#b64c2c", "#d06a3e"), 201))
    put("block/brown_roof_tiles", roof_tiles(palette("#35220f", "#56381f", "#6c4829", "#835a34", "#9c7044"), 202))
    put("block/light_roof_tiles", roof_tiles(palette("#7b6648", "#9d8562", "#b39a74", "#c7b089", "#dccaa3"), 203))

    # Dunkle Schieferplatten: rechteckig, versetzt, kühles Blaugrau
    slate = palette("#16181f", "#22252e", "#2c303a", "#373c47", "#434855", "#525866")
    rng = random.Random(204)
    img = pa.new()
    px = img.load()
    noise = pa.value_noise(205, 2)
    for y in range(16):
        row = y // 4
        offset = (row % 2) * 3
        for x in range(16):
            col = ((x + offset) // 6)
            local_x = (x + offset) % 6
            local_y = y % 4
            if local_y == 3 or local_x == 5 and col < 2 or (x + offset) % 16 == 15:
                px[x, y] = slate[0]
                continue
            v = 0.5 + (noise[y][x] - 0.5) * 0.25 + ((row * 3 + col) % 3 - 1) * 0.08
            if local_y == 0:
                v += 0.25
            if local_y == 2:
                v -= 0.12
            px[x, y] = pick(slate[1:], v)
    put("block/dark_slate_shingles", img)

    # Holzschindeln: schmale Brettchen mit Maserung
    shingle = palette("#2f1e10", "#47301a", "#5b3e22", "#6f4d2b", "#835d35", "#966d40")
    rng = random.Random(206)
    img = pa.new()
    px = img.load()
    widths_per_row = [[4, 3, 5, 4], [5, 4, 3, 4], [3, 5, 4, 4], [4, 4, 5, 3]]
    for row in range(4):
        widths = widths_per_row[row]
        offset = (row * 5) % 16
        x = offset
        for w in widths:
            tone = rng.uniform(-0.12, 0.12)
            for dx in range(w):
                for dy in range(4):
                    gx, gy = (x + dx) % 16, row * 4 + dy
                    if dx == w - 1 or dy == 3:
                        px[gx, gy] = shingle[0]
                        continue
                    v = 0.55 + tone + (0.2 if dy == 0 else 0) - (0.12 if dy == 2 else 0)
                    if (gx * 5 + dy * 2 + row) % 7 == 0:
                        v -= 0.18
                    px[gx, gy] = pick(shingle[1:], v)
            x += w
    put("block/wooden_shingles", img)

    # Kupferdach: Stehfalz-Bahnen (zwei Bahnen je Block) mit Querfalz und leichtem Glanzverlauf
    copper = palette("#5a2812", "#7a391e", "#964a27", "#b05a30", "#c66e3c", "#d9874e", "#eda872")
    img = pa.new()
    px = img.load()
    noise = pa.fractal_noise(207, ((4, 0.6), (2, 0.4)))
    for y in range(16):
        for x in range(16):
            lx = x % 8
            if lx == 0:
                c = copper[6] if y % 8 != 7 else copper[4]   # Falz (Glanzkante)
            elif lx == 7:
                c = copper[1]                                 # Schatten neben dem Falz
            elif y % 8 == 7:
                c = copper[2]                                 # Querfalz
            elif y % 8 == 0:
                c = copper[5] if lx < 5 else copper[4]
            else:
                v = 0.78 - lx * 0.075 + (noise[y][x] - 0.5) * 0.22
                c = pick(copper[2:6], v)
            px[x, y] = c
    put("block/copper_roof", img)


# =============================================================================
# 4. Erze (durchsichtige Ebenen über Vanilla-Stein bzw. -Tiefenschiefer)
# =============================================================================
# Jedes Erz besteht aus handgezeichneten Kristall-Stempeln. Zeichen:
#   o = dunkler Rand, a/b/c/d = Töne dunkel -> hell, w = Glanz bzw. Leuchtkern
#   weitere Buchstaben = Akzentfarben (siehe jeweilige Farbtabelle)

ORE_STAMPS: dict[str, dict[str, list[str]]] = {
    "carbon": {
        "big": [
            ".oooo..",
            "odcbbo.",
            "odccbao",
            "obcbbao",
            ".oabaao",
            "..oooo.",
        ],
        "mid": [
            ".ooo..",
            "odcbo.",
            "ocbbao",
            ".obaao",
            "..ooo.",
        ],
        "small": [
            "ooo.",
            "odbo",
            "obao",
            ".ooo",
        ],
    },
    "gemstone": {
        "big": [
            ".ooo.",
            "ocwco",
            "odcbo",
            "obcao",
            ".oao.",
            "..o..",
        ],
        "mid": [
            ".oo.",
            "owco",
            "ocao",
            ".oo.",
        ],
        "green": [
            ".qq.",
            "qhgq",
            "qggq",
            ".qq.",
        ],
        "cyan": [
            ".r.",
            "rur",
            "rtr",
            ".r.",
        ],
    },
    "konterit": {
        "shard": [
            "...oo",
            "..owo",
            ".odbo",
            "ocbao",
            "obao.",
            ".oo..",
        ],
        "tall": [
            ".o.",
            "owo",
            "odo",
            "oco",
            "obo",
            "oao",
            ".o.",
        ],
        "pair": [
            ".o...",
            "owo.o",
            "odoow",
            "ocodo",
            "obobo",
            ".oao.",
            "..o..",
        ],
    },
    "aetherit": {
        "big": [
            ".oo.",
            "odwo",
            "ocwo",
            "ocwo",
            "obco",
            "oabo",
            ".oo.",
        ],
        "twin": [
            ".o...",
            "owo..",
            "owo.o",
            "ocoow",
            "oboco",
            ".oabo",
            "..oo.",
        ],
        "small": [
            ".o.",
            "owo",
            "oco",
            ".o.",
        ],
    },
    "draconium": {
        "big": [
            ".o..o.",
            "odo.oo",
            "owcodo",
            "ocwwco",
            "obcwbo",
            "oabbao",
            ".oooo.",
        ],
        "mid": [
            ".oo.",
            "odwo",
            "owco",
            "obao",
            ".oo.",
        ],
        "small": [
            ".o.",
            "owo",
            "obo",
            ".o.",
        ],
    },
    "wyvernit": {
        "shard": [
            "oo...",
            "owo..",
            "odco.",
            ".obco",
            "..obo",
            "...oo",
        ],
        "tall": [
            "..o.",
            ".owo",
            ".odo",
            "ocbo",
            "obao",
            ".oo.",
        ],
        "small": [
            ".o.",
            "owo",
            "obo",
            ".o.",
        ],
    },
}

# (Stempel, x, y, gespiegelt) – von Hand verteilt wie bei Vanilla-Erzen
ORE_LAYOUT: dict[str, list[tuple[str, int, int, bool]]] = {
    "carbon": [("mid", 1, 1, False), ("small", 11, 1, True), ("big", 5, 6, False), ("mid", 11, 9, True),
               ("small", 1, 11, False)],
    "gemstone": [("big", 2, 1, False), ("cyan", 11, 1, False), ("mid", 10, 6, False), ("green", 1, 9, False),
                 ("big", 6, 9, False), ("green", 12, 12, False)],
    "konterit": [("shard", 1, 1, False), ("tall", 12, 0, False), ("pair", 6, 5, False), ("shard", 11, 9, True),
                 ("tall", 2, 9, False)],
    "aetherit": [("big", 2, 1, False), ("small", 11, 2, False), ("twin", 9, 7, False), ("small", 6, 11, False),
                 ("big", 1, 9, False)],
    "draconium": [("big", 2, 2, False), ("small", 12, 1, False), ("mid", 10, 8, False), ("mid", 3, 10, True),
                  ("small", 13, 13, False)],
    "wyvernit": [("shard", 1, 1, False), ("small", 12, 2, False), ("tall", 7, 5, False), ("shard", 10, 9, True),
                 ("small", 2, 11, False)],
}

ORE_COLORS: dict[str, dict[str, Color]] = {
    "carbon": {"o": hexc("#101013"), "a": hexc("#1f2025"), "b": hexc("#2d2f35"), "c": hexc("#474c57"),
               "d": hexc("#727a89")},
    "gemstone": {"o": hexc("#2a0f3d"), "a": hexc("#4c1d6c"), "b": hexc("#7230a2"), "c": hexc("#9a50d2"),
                 "d": hexc("#bf82ef"), "w": hexc("#f2dcff"),
                 "q": hexc("#0b3a20"), "g": hexc("#239a52"), "h": hexc("#86eeac"),
                 "r": hexc("#0a3946"), "t": hexc("#2aa4bc"), "u": hexc("#a8f4ff")},
    "konterit": {"o": hexc("#33103a"), "a": hexc("#5a1a60"), "b": hexc("#87298e"), "c": hexc("#b443b8"),
                 "d": hexc("#da76de"), "w": hexc("#ffd2fb")},
    "aetherit": {"o": hexc("#0a3346"), "a": hexc("#125a73"), "b": hexc("#1c88a6"), "c": hexc("#37bcd8"),
                 "d": hexc("#86e6f4"), "w": hexc("#e8ffff")},
    "draconium": {"o": hexc("#1a0306"), "a": hexc("#3b070c"), "b": hexc("#650c14"), "c": hexc("#981a1d"),
                  "d": hexc("#cf3a20"), "w": hexc("#ffa24a")},
    "wyvernit": {"o": hexc("#34536f"), "a": hexc("#5f88ad"), "b": hexc("#8fb6d8"), "c": hexc("#bcdaf2"),
                 "d": hexc("#e6f4ff"), "w": hexc("#ffffff")},
}

# Diese Zeichen bilden die Leuchtebene (werden im Spiel ohne Schatten, also "leuchtend" gezeichnet)
ORE_GLOW_KEYS = {"aetherit": "wd", "draconium": "wd"}


def ore_overlay(name: str) -> Image.Image:
    img = pa.new()
    for stamp_name, x, y, mirrored in ORE_LAYOUT[name]:
        rows = ORE_STAMPS[name][stamp_name]
        if mirrored:
            rows = [r[::-1] for r in rows]
        pa.stamp(img, rows, ORE_COLORS[name], x, y, wrap=False)
    return img


def ore_glow(name: str) -> Image.Image:
    """Nur die hellsten Kristallpixel (gleiche Positionen wie in der Erz-Ebene)."""
    keys = ORE_GLOW_KEYS[name]
    colors = {k: (v if k in keys else CLEAR) for k, v in ORE_COLORS[name].items()}
    img = pa.new()
    for stamp_name, x, y, mirrored in ORE_LAYOUT[name]:
        rows = ORE_STAMPS[name][stamp_name]
        if mirrored:
            rows = [r[::-1] for r in rows]
        rows = ["".join(ch if ch in keys else "." for ch in r) for r in rows]
        pa.stamp(img, rows, colors, x, y, wrap=False)
    return img


def ores() -> None:
    for name in ORE_LAYOUT:
        put(f"block/{name}_ore_overlay", ore_overlay(name))
    for name in ORE_GLOW_KEYS:
        put(f"block/{name}_ore_glow", ore_glow(name))


# =============================================================================
# 5. Speicherblöcke (veredelte Blöcke wie Diamant-/Eisenblock)
# =============================================================================

def frame_value(x: int, y: int) -> float | None:
    """Rahmen eines veredelten Blocks (Licht oben links); None = Innenfläche."""
    if x == 15 or y == 15:
        return 0.12
    if x == 0 or y == 0:
        return 0.45
    if x == 14 or y == 14:
        return 0.3
    if x == 1 or y == 1:
        return 0.92
    return None


def polished(pal: list[Color], seed: int, shine: float = 0.22, grain: float = 0.12) -> Image.Image:
    """Polierte Fläche mit diagonalen Lichtbändern (wie Vanilla-Diamantblock) und Rahmen."""
    noise = pa.fractal_noise(seed, ((4, 0.6), (2, 0.4)))
    img = pa.new()
    px = img.load()
    for y in range(16):
        for x in range(16):
            f = frame_value(x, y)
            if f is not None:
                px[x, y] = pick(pal, f)
                continue
            v = 0.5 + shine * math.sin((x - y) * 0.55 + 1.0) + (noise[y][x] - 0.5) * grain * 2
            v += (15 - x - y) * 0.012  # oben links etwas heller
            px[x, y] = pick(pal, v)
    return img


def faceted(pal: list[Color], seed: int, points: int = 7, glints: int = 4) -> Image.Image:
    """Kristallmasse aus Facetten: jede Facette eigener Ton, helle Kanten oben links, Glanzpunkte."""
    rng = random.Random(seed)
    cells, border, pts = pa.voronoi(seed, points)
    tone = [rng.uniform(0.35, 0.68) for _ in pts]
    img = pa.new()
    px = img.load()
    for y in range(16):
        for x in range(16):
            f = frame_value(x, y)
            if f is not None:
                px[x, y] = pick(pal, f)
                continue
            i = cells[y][x]
            v = tone[i] + (15 - x - y) * 0.01
            if border[y][x] < 0.8:
                # Facettenkante: oben/links an einer Kante heller, unten/rechts dunkler
                up = cells[y - 1][x] != i or cells[y][x - 1] != i
                v += 0.2 if up else -0.2
            px[x, y] = pick(pal, v)
    for _ in range(glints):
        x, y = rng.randrange(3, 13), rng.randrange(3, 13)
        px[x, y] = pal[-1]
    return img


GEM_PURPLE = palette("#2c0f40", "#47196a", "#65259a", "#8338c4", "#a157df", "#c385f0", "#ead2ff")
KONTERIT = palette("#3a0f40", "#5a1862", "#7d238a", "#a233b0", "#c552cf", "#e07fe6", "#ffd0fb")
AETHERIT = palette("#0a3448", "#11546e", "#19799a", "#25a4c4", "#47c9e2", "#8de8f4", "#e8ffff")
DRACONIUM = palette("#1c0306", "#360609", "#560b10", "#7a1216", "#a11d1c", "#c9381f", "#ff9a42")
WYVERNIT = palette("#3a5878", "#5a80a6", "#7fa3c6", "#a4c4e0", "#c4dcf0", "#e2f0fb", "#ffffff")
CARBON = palette("#0e0e11", "#18191d", "#232429", "#2f3137", "#3c3f47", "#555a66", "#7c8494")
STEEL = palette("#2b323b", "#3b444f", "#4c5763", "#5e6a77", "#717e8b", "#8895a1", "#aab6c1")


def storage_blocks() -> None:
    # Edelsteinblock: polierte violette Fläche mit eingesetztem Schliff-Edelstein und grünen/türkisen Akzenten
    img = polished(GEM_PURPLE, 401)
    pa.stamp(img, [
        "..oooo..",
        ".ocwwco.",
        "ocwccbbo",
        "ocdccbao",
        "obcbbaao",
        ".obbaao.",
        "..oaao..",
        "...oo...",
    ], {"o": GEM_PURPLE[0], "a": GEM_PURPLE[2], "b": GEM_PURPLE[3], "c": GEM_PURPLE[4], "d": GEM_PURPLE[5],
        "w": GEM_PURPLE[6]}, 4, 4, wrap=False)
    accents = {"q": hexc("#0b3a20"), "g": hexc("#2fb060"), "r": hexc("#0a3946"), "t": hexc("#45c8de")}
    for (x, y), (rim, fill) in zip([(3, 3), (11, 3), (3, 11), (11, 11)], [("q", "g"), ("r", "t"), ("r", "t"), ("q", "g")]):
        pa.stamp(img, [fill + fill, fill + rim], accents, x, y, wrap=False)
    put("block/gemstone_block", img)

    put("block/konterit_block", faceted(KONTERIT, 402, points=6, glints=4))

    # Aetheritblock: leuchtend türkis mit eingravierter Rune
    img = polished(AETHERIT, 403, shine=0.16)
    pa.stamp(img, [
        "...w...",
        "w..w..w",
        ".w.w.w.",
        "..www..",
        "...w...",
        "...w...",
        "...w...",
        "..d.d..",
    ], {"w": AETHERIT[6], "d": AETHERIT[5]}, 4, 4, wrap=False)
    put("block/aetherit_block", img)

    # Draconiumblock: dunkles Blutrot mit glühenden, verzweigten Adern
    img = polished(DRACONIUM[:6], 404, shine=0.12, grain=0.14)
    pa.stamp(img, [
        "....e.......",
        "...ev.......",
        "...ev...e...",
        "....ve..ve..",
        ".....vvev...",
        "......vv....",
        ".....ev.....",
        "....ev......",
        "...ev..e....",
        "..ev...ve...",
        ".ev.....ve..",
        ".e.......e..",
    ], {"v": DRACONIUM[6], "e": DRACONIUM[5]}, 2, 2, wrap=False)
    put("block/draconium_block", img)

    put("block/wyvernit_block", faceted(WYVERNIT, 406, points=7, glints=5))

    # Kohlenstoffblock: gepresste Graphitplatten im Verband mit Glanzkanten
    img = pa.new()
    px = img.load()
    noise = pa.value_noise(407, 2)
    for y in range(16):
        row = y // 4
        for x in range(16):
            ly = y % 4
            lx = (x + (row % 2) * 4) % 8
            v = (0.66, 0.44, 0.36, 0.1)[ly] + (noise[y][x] - 0.5) * 0.16
            if lx == 7:
                v = 0.06
            elif lx == 0 and ly < 3:
                v += 0.12
            if ly == 0 and (x * 5 + row * 3) % 11 == 0:
                v = 0.98
            px[x, y] = pick(CARBON, v)
    put("block/carbon_block", img)

    # Stahlblock: vier genietete Platten
    img = pa.new()
    px = img.load()
    noise = pa.value_noise(408, 4)
    for y in range(16):
        for x in range(16):
            lx, ly = x % 8, y % 8
            if lx == 7 or ly == 7:
                v = 0.08
            elif lx == 0 or ly == 0:
                v = 0.86
            elif lx == 6 or ly == 6:
                v = 0.3
            else:
                v = 0.52 + (noise[y][x] - 0.5) * 0.14 + (5 - lx - ly) * 0.015
            if (lx, ly) in ((1, 1), (5, 1), (1, 5), (5, 5)):
                v = 0.98  # Nietkopf
            elif (lx, ly) in ((2, 2), (6, 2), (2, 6)):
                v = min(v, 0.25)  # Schatten der Niete
            px[x, y] = pick(STEEL, v)
    put("block/steel_block", img)


# =============================================================================
# 6. Dracheneier (Schuppenmuster je Element)
# =============================================================================
# Das Ei-Modell (siehe ModModelProvider) schneidet aus dieser Textur die Seiten der
# übereinandergestapelten Quader aus. Die ganze Fläche ist daher ein durchgehendes Schuppenmuster,
# oben etwas heller als unten.

EGG_SCALE = [  # 4x4-Schuppe: 0 Fuge/Schatten .. 4 Glanz
    [0, 3, 3, 0],
    [2, 4, 3, 2],
    [2, 3, 2, 1],
    [0, 1, 1, 0],
]

EGG_PALETTES = {
    "fire": palette("#3d0805", "#7a1508", "#b02a0c", "#e0521a", "#ffa43a"),
    "ice": palette("#4a7ea6", "#7fb2d8", "#a8d2ee", "#cfe8f8", "#f6fcff"),
    "lightning": palette("#121318", "#24262e", "#363944", "#4c505d", "#6a6f7e"),
    "nature": palette("#15381a", "#22562a", "#33773a", "#4f9a48", "#8ccc62"),
    "earth": palette("#2e1d10", "#4c331d", "#6a4b2c", "#8b673f", "#b08a5a"),
    "water": palette("#062a4a", "#0c4a7a", "#1470a8", "#2a9ad0", "#7ad4f4"),
}


def dragon_egg(kind: str, seed: int) -> Image.Image:
    pal = EGG_PALETTES[kind]
    rng = random.Random(seed)
    img = pa.new()
    px = img.load()
    tones = {}
    for y in range(16):
        row = y // 4
        offset = (row % 2) * 2
        for x in range(16):
            key = (row, (x + offset) // 4 % 4)
            if key not in tones:
                tones[key] = rng.choice([0, 0, 0, 1, -1])
            v = EGG_SCALE[y % 4][(x + offset) % 4]
            if v > 0:
                v += tones[key]
            # oben heller, unten dunkler (runde Eiform)
            if y < 4 and v > 0:
                v += 1
            if y > 11 and v > 1:
                v -= 1
            px[x, y] = pal[max(0, min(4, v))]

    accent = {
        "fire": [(hexc("#ffd36a"), 7)],                     # Glut in einzelnen Schuppen
        "ice": [(hexc("#ffffff"), 6)],                      # Funkeln
        "lightning": [(hexc("#ffe43a"), 0), (hexc("#fff8b0"), 0)],
        "nature": [(hexc("#c6e86a"), 6), (hexc("#e86aa6"), 2)],  # helle Blätter, kleine Blüte
        "earth": [(hexc("#d8b45a"), 5), (hexc("#9a9aa2"), 4)],    # Gold- und Eisenkörner
        "water": [(hexc("#c8f4ff"), 6)],
    }[kind]
    for color, count in accent:
        for _ in range(count):
            x, y = rng.randrange(16), rng.randrange(2, 14)
            if EGG_SCALE[y % 4][(x + (y // 4 % 2) * 2) % 4] >= 2:
                px[x, y] = color
    if kind == "lightning":
        # gelbe Blitz-Adern im Zickzack über das Ei
        bolt = [(3, 0), (4, 1), (4, 2), (3, 3), (4, 4), (5, 5), (5, 6), (4, 7), (5, 8), (6, 9), (6, 10), (5, 11),
                (6, 12), (7, 13), (7, 14), (6, 15)]
        for bx, by in bolt:
            px[bx, by] = hexc("#ffd92a")
            px[(bx + 1) % 16, by] = hexc("#b88a10")
            px[(bx + 8) % 16, (by + 5) % 16] = hexc("#ffd92a")
            px[(bx + 9) % 16, (by + 5) % 16] = hexc("#b88a10")
    if kind == "water":
        # helle Wellenlinien
        for x in range(16):
            y = 6 + round(math.sin(x * 0.8) * 1.2)
            if px[x, y] != pal[0]:
                px[x, y] = pal[4]
    return img


def dragon_eggs() -> None:
    for i, kind in enumerate(EGG_PALETTES):
        put(f"block/{kind}_dragon_egg", dragon_egg(kind, 501 + i))


# =============================================================================
# 7. Feldfrüchte (je 4 Wachstumsstufen) und Zierblumen
# =============================================================================

PLANT = {
    "k": hexc("#1d4416"), "g": hexc("#2b6420"), "G": hexc("#3c842a"), "l": hexc("#56a238"), "L": hexc("#7cc24e"),
    "s": hexc("#5a3c1e"), "S": hexc("#8a6236"),                                   # Stütz-Stab
    "r": hexc("#8c1a10"), "R": hexc("#d2361e"), "h": hexc("#ff9070"),             # Tomate
    "u": hexc("#6aa632"), "U": hexc("#9cd25a"),                                   # unreife Tomate
    "y": hexc("#f4d23a"), "Y": hexc("#c4960e"),                                   # Blüte
    "c": hexc("#7fbe52"), "C": hexc("#a8dc7c"), "w": hexc("#d6f0b8"), "v": hexc("#5e9a3e"),  # Kohlkopf
    "o": hexc("#7a4a16"), "O": hexc("#c0822e"), "p": hexc("#e6b458"), "q": hexc("#f8dc98"),  # Zwiebel
    "e": hexc("#b2b04a"),                                                         # vergilbtes Blatt
    "a": hexc("#94722a"), "A": hexc("#c4a044"), "b": hexc("#dcbc60"), "B": hexc("#f2da88"), "n": hexc("#cfb468"),  # Gerste
}

CROPS: dict[str, list[list[str]]] = {
    "tomatoes": [
        ["", "", "", "", "", "", "", "", "", "", "",
         "....L.L.........",
         ".....G.....L.L..",
         ".....g......G...",
         "..L.Lg......g...",
         "...Gg.......g..."],
        ["", "", "", "", "", "",
         ".......L........",
         "......LGL....L..",
         "...L...g....LGl.",
         "..lGl..gL....g..",
         "...g..lGl.L..g..",
         "...gL..g.lG..gL.",
         "..lG...g..g.lG..",
         "...g...g..g..g..",
         "...g...g..g..g..",
         "...g...g..g..g.."],
        ["",
         ".......S........",
         ".....L.SL.......",
         "....LGlsGL......",
         "...lGgysGgL.....",
         "..lGgYyslGgl....",
         "..Gg..Ls.LGgL...",
         ".lGuUlGsgGuUg...",
         ".gGuugGslGuugl..",
         "..gg.lGsGg.gG...",
         "..lGl.gsg.lGgl..",
         ".lGgGLgsglGgGl..",
         "..g.uU.s.uU.g...",
         "..g.uu.s.uu.g...",
         "..g....s....g...",
         "..g....s....g..."],
        ["",
         ".......S........",
         ".....L.SL.......",
         "....LGlsGL......",
         "...lGgRhGgL.....",
         "..lGgrRslGgl....",
         "..Gg..Ls.LGgL...",
         ".lGRhlGsgGRhg...",
         ".gGrRgGslGrRgl..",
         "..gg.lGsGg.gG...",
         "..lGl.gsg.lGgl..",
         ".lGgGLgsglGgGl..",
         "..g.Rh.s.Rh.g...",
         "..g.rR.s.rR.g...",
         "..g....s....g...",
         "..g....s....g..."],
    ],
    "cabbages": [
        ["", "", "", "", "", "", "", "", "", "", "", "",
         "...L.L....L.L...",
         "....G......G....",
         "...lGl....lGl...",
         "....g......g...."],
        ["", "", "", "", "", "", "", "", "", "",
         "................",
         "..L.L.....L..L..",
         ".lGlGl...lGllGl.",
         "lGgGGGl.lGgGGgGl",
         ".GgCgG...GgCgGg.",
         "..ggg.....ggg..."],
        ["", "", "", "", "", "", "",
         "................",
         "..L..L.....L..L.",
         ".lGl.Gl...lGl.Gl",
         "lGgGlGGl.lGgGlGG",
         "GgGCCCgGlGgCCCgG",
         "gGCwCCcGgGCwCCcG",
         ".gGCCcgg.gGCCcgg",
         "..gGGgg...gGGgg.",
         "...ggg.....ggg.."],
        ["", "", "", "", "",
         "....L....L......",
         "...lGl..lGl.....",
         "..lGgGllGgGl....",
         ".lGgCCCCCgGgl...",
         ".GgCwwCCCcgGl...",
         "lGCwCCvCCCcgGl..",
         "GgCCvCCCvCcgGg..",
         "gGcCCCvCCCcgGg..",
         ".gGcCCCCCcggg...",
         "..gGgcccgGgg....",
         "...gggGggg......"],
    ],
    "onions": [
        ["", "", "", "", "", "", "", "", "", "", "", "",
         "...L.....L....L.",
         "...l.....l....l.",
         "...G.....G....G.",
         "...g.....g....g."],
        ["", "", "", "", "", "", "", "",
         "..L.......L.....",
         "..l..L....l...L.",
         "..G..l...LG...l.",
         "..G..G...lG...G.",
         ".lG..G...G....G.",
         ".G...g...G...gG.",
         ".g...g...g...g..",
         ".g...g...g...g.."],
        ["", "", "",
         "..L.........L...",
         "..l...L.....l...",
         "..G...l...L.G...",
         ".lG...G...l.G..L",
         ".G....G...G.G..l",
         ".G...lG...G.G..G",
         ".G...G....GlG..G",
         ".g...G...lG.g..G",
         ".g...g...G..g.lg",
         ".g...g...g..g.g.",
         ".pO..g...pO.g.g.",
         "pOOo.....OOo.pOo",
         ".oo.......o..oo."],
        ["", "", "",
         "..e.........e...",
         "..e...L.....l...",
         "..l...l...e.G...",
         ".lG...G...e.G..L",
         ".G....G...G.G..l",
         ".G...lG...G.G..G",
         ".G...G....GlG..G",
         ".g...G...lG.g..G",
         ".g..qg...G.qg.lg",
         "qpOOpO..qpOOpOpO",
         "pOOOOo..pOOOOopO",
         ".OOoOo..OOOoOoOo",
         "..oo......oo..o."],
    ],
    "barley": [
        ["", "", "", "", "", "", "", "", "", "", "", "", "",
         "..L....L...L..L.",
         ".lG..L.G..lG..G.",
         ".G..lG.g..G..lg."],
        ["", "", "", "", "", "", "", "", "",
         "..L......L......",
         "..l...L..l...L..",
         ".lG...l..G..lG..",
         ".G...lG..G..G..L",
         ".G...G..lG..G..l",
         ".g...G..G..lg..G",
         ".g...g..g..g...g"],
        ["", "",
         "..n.....n.......",
         "..ln...nl....n..",
         "..lL...Ll...nl..",
         "..LG...lL...Ll..",
         "..lL...GL...lL..",
         "..G...lG....LG..",
         ".lG...G....lG...",
         ".G...lG....G....",
         ".G...G....lG...l",
         ".G...G....G....G",
         ".g...G....G...lG",
         ".g...g...lg...G.",
         ".g...g...g....g.",
         ".g...g...g....g."],
        ["n.......n.......",
         ".n.n.....n.n....",
         "..nBn...nBn.....",
         "..BbB...BbBn....",
         "..bAb..nbAb.n.n.",
         "..Bb....Bb...nBn",
         "..bA....bA...BbB",
         "..A.....A....bAb",
         ".aA....aA....Bb.",
         ".A.....A.....bA.",
         ".A.....A....aA..",
         ".A....aA....A...",
         ".a....A.....A...",
         ".a....A....aA...",
         ".a....a....a....",
         ".a....a....a...."],
    ],
}

FLOWERS: dict[str, tuple[list[str], dict[str, Color]]] = {
    "lavender": ([
        "", "",
        "......p...p.....",
        ".....pPp.pPp....",
        "..p..pPp.pPp....",
        ".pPp.PpP.PpP.p..",
        ".pPp.pPp.pPp.Pp.",
        ".PpP..P...P.pPp.",
        ".pPp..l...l.PpP.",
        "..P...G..lG..P..",
        "..l...G..G...l..",
        "..G...G.lG...G..",
        "..lG..GlG...Gl..",
        "...G..GG...lG...",
        "....GgGg..gG....",
        ".....gg.gg......",
    ], {"p": hexc("#c4a0f2"), "P": hexc("#8a5ccc")}),
    "garden_rose": ([
        "", "", "",
        "......oRRo......",
        ".....oRhrRo.....",
        ".....RrRRrR.....",
        ".....oRrrRo.....",
        "......oRRo......",
        ".......G........",
        ".......G.lL.....",
        "...Ll..GlGl.....",
        "...lGl.G........",
        ".....lGG........",
        ".......G..lL....",
        ".......GlGl.....",
        ".......g........",
    ], {"o": hexc("#5a0814"), "R": hexc("#b4162a"), "r": hexc("#e2364a"), "h": hexc("#ff8a96")}),
    "fire_lily": ([
        "", "",
        "...o.......o....",
        "...oR.....Ro....",
        "....RR.y.RR.....",
        ".....RrYyrR.....",
        "...oRrryrrRRo...",
        "..oR..RrrR..Ro..",
        "......oRRo......",
        ".......G........",
        ".......G........",
        "....lL.G........",
        ".....lGG..Ll....",
        ".......G.lG.....",
        ".......GGl......",
        ".......g........",
    ], {"o": hexc("#7a1806"), "R": hexc("#e04a12"), "r": hexc("#ff9a2a"), "y": hexc("#ffe25a"),
        "Y": hexc("#c8840e")}),
    "frost_flower": ([
        "", "",
        ".......w........",
        "...b...B...b....",
        "....B..b..B.....",
        ".....BbwbB......",
        "..wbbwcCcwbbw...",
        ".....BbwbB......",
        "....B..b..B.....",
        "...b...t...b....",
        ".......t........",
        "....T..t..T.....",
        ".....TtT.tT.....",
        ".......tTt......",
        ".......t........",
        ".......u........",
    ], {"w": hexc("#ffffff"), "b": hexc("#c4e8ff"), "B": hexc("#7ab6e6"), "c": hexc("#5ad8f2"),
        "C": hexc("#2a9cc4"), "t": hexc("#6a9eb0"), "T": hexc("#94c4d4"), "u": hexc("#3e6c7c")}),
}


def plants() -> None:
    for crop, stages in CROPS.items():
        for i, rows in enumerate(stages):
            put(f"block/{crop}_stage{i}", pa.sprite(rows, PLANT))
    for name, (rows, colors) in FLOWERS.items():
        put(f"block/{name}", pa.sprite(rows, {**PLANT, **colors}))


# =============================================================================
# 8. Gegenstände (handgezeichnete Sprites)
# =============================================================================
# Zeichen: o = Rand (dunkler Ton der Eigenfarbe), a/b/c/d = Töne dunkel -> hell, w = Glanzlicht.

def tones(o: str, a: str, b: str, c: str, d: str, w: str) -> dict[str, Color]:
    return {"o": hexc(o), "a": hexc(a), "b": hexc(b), "c": hexc(c), "d": hexc(d), "w": hexc(w)}


INGOT = [
    "", "", "",
    "..........ooo...",
    ".......ooowddo..",
    "....ooowddddcbo.",
    "..oowdddddcdcbo.",
    ".owdddcddddcbbao",
    ".ocddddddccbbaao",
    ".obcccddcbbaaao.",
    ".obbbccbbaaaoo..",
    ".oabbbbaaaoo....",
    "..oaaaaaoo......",
    "...ooooo........",
]

POWDER = [
    "", "", "", "", "", "",
    "......ooo.......",
    ".....ocwco......",
    "....ocdccbo.....",
    "...ocdcwcbbo....",
    "..ocdcccbbbao...",
    ".ocwccbbbbaaao..",
    ".obcbbbbaaaaao..",
    "..oabbaaaaaoo...",
    "...ooooooooo....",
]

CHUNK = [
    "", "", "",
    "......oooo......",
    "....oodwcbo.....",
    "...odwdcbbbo....",
    "..odccbbcbbao...",
    "..ocbbbbcbaaao..",
    ".odcbbbbbcaaao..",
    ".ocbbbbbbbaaao..",
    ".obbbbbbbaaaao..",
    ".oabbbaaaaaao...",
    "..oaaaaaaooo....",
    "...oooooo.......",
]

GEM_ITEM = [
    "", "",
    "........ooooo...",
    ".......ocwwdco..",
    "......ocwdddcbo.",
    "......odwdcccbo.",
    "......obddccbao.",
    "......obcccbbao.",
    ".......obcbbao..",
    "..qq....obbao...",
    ".qhgq....oao....",
    "qhggqq....o.....",
    "qgggkq..........",
    ".qgkq...........",
    "..qq............",
]

CRYSTAL_CLUSTER = [
    "", "",
    "..........oo....",
    ".........owdo...",
    "....oo...odco...",
    "...owdo..ocbo...",
    "...odcbo.ocbo.oo",
    "....ocbo.obaoowo",
    "....ocbaoobaodco",
    ".....obaoobaocbo",
    ".....obbaabaobao",
    "......obbbbaobo.",
    "......oaaaaaao..",
    ".......oooooo...",
]

TALL_CRYSTAL = [
    "",
    ".......oo.......",
    "......owdo......",
    "......owdo...w..",
    ".....owdcbo.wdw.",
    ".....owdcbo..w..",
    ".....odccbo.....",
    ".....odcbbo.....",
    ".....ocbbao.....",
    ".....ocbbao.....",
    ".w...obbaao.....",
    "wdw..obbaao.....",
    ".w...oabaao.....",
    "......oaao......",
    ".......oo.......",
]

FLAME_CRYSTAL = [
    "", "",
    "......o.........",
    ".....odo...o....",
    ".....odco.odo...",
    "....odcwbodco...",
    "....odwwcbcbo...",
    "...odcwwcccbo...",
    "...ocwwwccbbao..",
    "...ocwcwcbbbao..",
    "...obccccbbaao..",
    "....obccbbbao...",
    ".....obbbbao....",
    "......oaaao.....",
    ".......ooo......",
]

SHARD = [
    "", "",
    "..........ooo...",
    ".........owwdo..",
    "........owddco..",
    ".......odddcbo..",
    "......odddcbo...",
    ".....odddcbao...",
    "....odddcbao....",
    "...odddcbao.....",
    "..occccbao......",
    "..obcbbao.......",
    "...obbao........",
    "....ooo.........",
]

EGG_FRAGMENT = [
    "", "", "",
    "..........oo....",
    "........oodwo...",
    ".....oo.odwdco..",
    "...oodwoodcccbo.",
    "..odwdcdcccbbbo.",
    ".odwccccccbbbao.",
    ".odcccbbbbbbao..",
    ".ocbbbiiiiiiio..",
    "..oiiiiiiiioo...",
    "...oooooooo.....",
]

MEAT_BONE = [
    "", "",
    "............ww..",
    "...........wkkw.",
    "......oooo.wkw..",
    "....oorRRroww...",
    "...orRhRRRro....",
    "..orRhhRRrrro...",
    "..oRRRRRrrrro...",
    "..orRRRrrrro....",
    "...orrrrrro.....",
    ".bb.oooooo......",
    "bBpb............",
    "bppb.bb.........",
    ".bb.bBpb........",
    "....bppb........",
]

SCALE = [
    "",
    ".....oooooo.....",
    "....owwddcco....",
    "...owddddcccbo..",
    "...odddcccccbo..",
    "..odddccccbbbao.",
    "..odccccccbbbao.",
    "..occcccxbbbbao.",
    "..occcccxbbbaao.",
    "...occccxbbbao..",
    "...obcccxbbaao..",
    "....obbbxbaao...",
    ".....obbbaao....",
    "......obaao.....",
    ".......ooo......",
]

SCALE_COLORS = {
    "fire": tones("#3c0804", "#7a1508", "#b42a0c", "#e2541a", "#ff9a36", "#ffd88a"),
    "ice": tones("#2e5a80", "#5f94c0", "#8fbde2", "#bcdcf4", "#e2f2ff", "#ffffff"),
    "lightning": tones("#0c0d11", "#1e2027", "#30333d", "#454955", "#626877", "#ffe14a"),
    "nature": tones("#0f3014", "#1d4f24", "#2e7234", "#4a9646", "#7cc25e", "#c8ec8a"),
    "earth": tones("#26180c", "#47301a", "#664828", "#88643c", "#ae8a58", "#dcc08a"),
    "water": tones("#05223e", "#0a4072", "#1266a0", "#228fca", "#5ec4ee", "#c4f2ff"),
}
SCALE_RIDGE = {"fire": "#ffc04a", "ice": "#ffffff", "lightning": "#ffd42a", "nature": "#a8dc6a",
               "earth": "#c4a06a", "water": "#9ae4ff"}

FOOD = {
    "tomato": ([
        "", "", "",
        ".......kk.......",
        "......kgGk......",
        ".....oogkGoo....",
        "...ooRRgRRRRoo..",
        "..oRhhRRRRRRRro.",
        "..oRhRRRRRRRrro.",
        ".oRRRRRRRRRrrrro",
        ".oRRRRRRRRrrrrro",
        ".orRRRRRRrrrrrro",
        "..orRRRrrrrrrro.",
        "..oorrrrrrrrroo.",
        "....oooooooo....",
    ], {"o": hexc("#4e0a06"), "R": hexc("#c8301c"), "r": hexc("#9a2214"), "h": hexc("#ff8c6c"),
        "k": hexc("#1d4416"), "g": hexc("#3c842a"), "G": hexc("#6ab442")}),
    "cabbage": ([
        "", "",
        ".....oooooo.....",
        "...ooLlLLlGoo...",
        "..oLlCCCCClGgo..",
        ".oLlCwwCCCClGgo.",
        ".olCwCCvCCCcGgo.",
        "oLlCCvCCCvCclGgo",
        "olCCCCvCCCvclGgo",
        "olcCCCCvCCcclGgo",
        "oGlcCCCCvccllGgo",
        ".oGlcccccclGggo.",
        ".ogGllllllGggko.",
        "..okgGGGGggkko..",
        "....oooooooo....",
    ], {"o": hexc("#1c3d12"), "k": hexc("#2a5a1c"), "g": hexc("#3a7a28"), "G": hexc("#4c9434"),
        "l": hexc("#6eb04a"), "L": hexc("#8ccc5e"), "c": hexc("#9ad06c"), "C": hexc("#bfe496"),
        "w": hexc("#e2f6cc"), "v": hexc("#7cb858")}),
    "onion": ([
        "",
        ".......g.g......",
        ".......GgG......",
        "........G.......",
        "......oooo......",
        "....ooqpOOoo....",
        "...oqpppOOOOo...",
        "..oqppOOOOOOOo..",
        "..opppOOOOOOOo..",
        ".oqppOOOOOOOOOo.",
        ".opOOOOOOOOOOOo.",
        ".opOOOOOOOOOOro.",
        "..oOOOOOOOOOro..",
        "...ooOOOOOroo...",
        ".....oooooo.....",
        "......n.n.n.....",
    ], {"o": hexc("#5a3410"), "O": hexc("#c0822e"), "p": hexc("#dea250"), "q": hexc("#f6d690"),
        "r": hexc("#96601e"), "g": hexc("#3c842a"), "G": hexc("#6ab442"), "n": hexc("#d8c8a0")}),
    "barley": ([
        "...........n....",
        "..........nBn.n.",
        "........n.BbBnBn",
        ".........nbAbBbB",
        "......n..BbAbbAb",
        ".....nBn.bAbAAb.",
        ".....BbB.AbAbA..",
        ".....bAb.bAAa...",
        ".....BbA.AAa....",
        ".....bAAaAa.....",
        "......bAAa......",
        ".....aAAa.......",
        "....aAa.........",
        "...aAa..........",
        "..aAa...........",
        "..aa............",
    ], {"a": hexc("#8a6a22"), "A": hexc("#c49e40"), "b": hexc("#dcbc5e"), "B": hexc("#f4dc8c"),
        "n": hexc("#d4ba6e")}),
    "bread_roll": ([
        "", "", "", "",
        ".....oooooo.....",
        "...oohwwhhcoo...",
        "..ohwhhhhhcccbo.",
        ".ohwhhcccccccbbo",
        ".ohhccmcccmccbbo",
        "ohhcccmmcmmcccbo",
        "ohcccccmmmcccbbo",
        "occcccccccccbbao",
        ".obccccccccbbaao",
        "..oabbbbbbbbaao.",
        "...ooaaaaaaaoo..",
        ".....oooooooo...",
    ], {"o": hexc("#4a2a0c"), "a": hexc("#8a521c"), "b": hexc("#b06e28"), "c": hexc("#d4923a"),
        "h": hexc("#ecb860"), "w": hexc("#f8dc98"), "m": hexc("#f2d090")}),
    "vegetable_stew": ([
        "", "", "", "", "",
        "...oooooooooo...",
        "..oSSSSSSSSSSo..",
        ".oSRsRgSsTRSsSo.",
        ".osSsTSRgsSsRso.",
        ".oWsSsgSsSTsSWo.",
        "..oWWWWWWWWWWo..",
        "..owwWWWWWWwwo..",
        "...owwwwwwwwo...",
        "....oowwwwoo....",
        "......oooo......",
    ], {"o": hexc("#2e1a0c"), "W": hexc("#8a5a32"), "w": hexc("#6a4224"), "S": hexc("#c4501c"),
        "s": hexc("#9a3612"), "R": hexc("#e8442a"), "g": hexc("#5aa83a"), "T": hexc("#f0a030")}),
    "meat_pie": ([
        "", "", "", "",
        "....oooooooo....",
        "..oohwwhhhhhoo..",
        ".ohwhhcccchhcco.",
        ".ohhcchmmhccccbo",
        "ohccchmeemhcccbo",
        "ohcccchmmhcccbbo",
        "ocbccccccccccbao",
        "oTTTTTTTTTTTTTTo",
        "otTtTtTtTtTtTtto",
        ".ottttttttttttto",
        "..oooooooooooo..",
    ], {"o": hexc("#4a280c"), "a": hexc("#7a4618"), "b": hexc("#a8682a"), "c": hexc("#cc8c3c"), "h": hexc("#e6b064"),
        "w": hexc("#f8d896"), "m": hexc("#6a2e14"), "e": hexc("#3a1608"), "T": hexc("#9a6a3e"),
        "t": hexc("#74482a")}),
    "apple_pie": ([
        "", "", "", "",
        "....oooooooo....",
        "..oohwhrhwhhoo..",
        ".ohwRhRhRhRhcco.",
        ".ohRhRhRhRhRhcbo",
        "ohRhRhRhRhRhRhbo",
        "ohcRhRhRhRhRhcbo",
        "ocbcRcRcRcRcbbao",
        "oTTTTTTTTTTTTTTo",
        "otTtTtTtTtTtTtto",
        ".ottttttttttttto",
        "..oooooooooooo..",
    ], {"o": hexc("#4a280c"), "a": hexc("#7a4618"), "b": hexc("#a8682a"), "c": hexc("#cc8c3c"), "h": hexc("#e6b064"),
        "w": hexc("#f8d896"), "R": hexc("#b42a1c"), "r": hexc("#e2503a"), "T": hexc("#9a6a3e"),
        "t": hexc("#74482a")}),
}

SEEDS = [
    "", "", "", "", "",
    ".........oo.....",
    "........odbo....",
    "...oo...obao....",
    "..odbo...oo.....",
    "..obao.....oo...",
    "...oo.....odbo..",
    ".....oo...obao..",
    "....odbo...oo...",
    "....obao........",
    ".....oo.........",
]


def items() -> None:
    put("item/steel_ingot", pa.sprite(INGOT, tones("#262d36", "#46515d", "#66737f", "#8e9ba7", "#c4cfd8", "#f2f6fa")))
    put("item/steel_blend", pa.sprite(POWDER, tones("#1e2026", "#33363e", "#4a4e58", "#686d79", "#8f95a2", "#d4dbe6")))
    put("item/carbon", pa.sprite(CHUNK, tones("#09090b", "#17181c", "#25272d", "#353840", "#525866", "#8a93a4")))
    gem = tones("#2a0f3d", "#4c1d6c", "#7230a2", "#9a50d2", "#c486f0", "#f4e0ff")
    put("item/gemstone", pa.sprite(GEM_ITEM, {**gem, "q": hexc("#0b3a20"), "k": hexc("#16683a"),
                                              "g": hexc("#2fae60"), "h": hexc("#9af2bc")}))
    put("item/konterit", pa.sprite(CRYSTAL_CLUSTER, tones("#33103a", "#5c1a62", "#8a2a90", "#b746bc", "#dc7ae0", "#ffd8fc")))
    put("item/aetherit", pa.sprite(TALL_CRYSTAL, tones("#0a3346", "#125a73", "#1c88a6", "#37bcd8", "#86e6f4", "#efffff")))
    put("item/draconium", pa.sprite(FLAME_CRYSTAL, tones("#1a0306", "#4a080e", "#7a1016", "#b4241c", "#e24a22", "#ffb04e")))
    put("item/wyvernit", pa.sprite(SHARD, tones("#34536f", "#5f88ad", "#8fb6d8", "#bcdaf2", "#e6f4ff", "#ffffff")))
    put("item/dragon_egg_fragment", pa.sprite(EGG_FRAGMENT, {
        **tones("#2a1238", "#4a2266", "#6e3896", "#8e5cbc", "#b48ee0", "#e4d0fa"), "i": hexc("#efe6d6")}))
    put("item/dragon_food", pa.sprite(MEAT_BONE, {
        "o": hexc("#4a0e0c"), "r": hexc("#9a2a22"), "R": hexc("#c8443a"), "h": hexc("#f08a7a"),
        "w": hexc("#f2ead8"), "k": hexc("#c8bca0"), "b": hexc("#3a0a1e"), "B": hexc("#c2304a"), "p": hexc("#8a1a34")}))
    for kind, colors in SCALE_COLORS.items():
        put(f"item/{kind}_dragon_scale", pa.sprite(SCALE, {**colors, "x": hexc(SCALE_RIDGE[kind])}))
    for name, (rows, colors) in FOOD.items():
        put(f"item/{name}", pa.sprite(rows, colors))
    put("item/tomato_seeds", pa.sprite(SEEDS, {"o": hexc("#6a5a1a"), "a": hexc("#c8b04a"), "b": hexc("#f0e08a"),
                                               "d": hexc("#fff4c0")}))
    put("item/cabbage_seeds", pa.sprite(SEEDS, {"o": hexc("#1e140a"), "a": hexc("#4a321e"), "b": hexc("#6e4c30"),
                                                "d": hexc("#9a7454")}))
    put("item/barley_seeds", pa.sprite(SEEDS, {"o": hexc("#5a4214"), "a": hexc("#b08a3a"), "b": hexc("#d8b862"),
                                               "d": hexc("#f2dc98")}))


# =============================================================================
# Ausgabe
# =============================================================================

def preview(path: Path) -> None:
    """Übersicht aller Texturen, 6-fach vergrößert auf Schachbrett-Hintergrund."""
    zoom, pad, cols = 6, 4, 12
    cell = 16 * zoom + pad
    entries = list(GENERATED.items())
    rows = math.ceil(len(entries) / cols)
    sheet = Image.new("RGBA", (cols * cell + pad, rows * cell + pad), (52, 56, 60, 255))
    for i, (_, img) in enumerate(entries):
        x = pad + (i % cols) * cell
        y = pad + (i // cols) * cell
        tile = pa.checker(16, 16)
        tile.alpha_composite(img)
        sheet.alpha_composite(pa.zoom(tile, zoom), (x, y))
    sheet.save(path)


def build_all() -> dict[str, Image.Image]:
    GENERATED.clear()
    stone_blocks()
    timber_blocks()
    roofs()
    ores()
    storage_blocks()
    dragon_eggs()
    plants()
    items()
    return GENERATED


def main() -> None:
    build_all()
    for key, img in GENERATED.items():
        target = TEXTURES / f"{key}.png"
        target.parent.mkdir(parents=True, exist_ok=True)
        img.save(target)
    print(f"{len(GENERATED)} Texturen geschrieben nach {TEXTURES}")
    if "--preview" in sys.argv:
        i = sys.argv.index("--preview")
        target = Path(sys.argv[i + 1]) if len(sys.argv) > i + 1 else ROOT / "build" / "texture_preview.png"
        target.parent.mkdir(parents=True, exist_ok=True)
        preview(target)
        print(f"Vorschau: {target}")


if __name__ == "__main__":
    main()
