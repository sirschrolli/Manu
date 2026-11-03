#!/usr/bin/env python3
"""
Texturen der Schattenwelt – Pflanzen und Kristalldrusen (die vollen Blöcke kommen aus tools/shadow_blocks.py):

  * block/night_crystal_cluster(_emissive) – Druse aus spitzen Kristallen (Kreuzpflanze, leuchtend)
  * block/ghostcap(_emissive)           – Geisterpilze mit leuchtenden Hüten
  * block/shadow_sprouts                – fedrige Schattenhalme (grau, Farbe aus dem Biom)
  * block/pale_grass                    – dürre, fahle Halme der Bleichen Öde
  * block/soul_berry_bush_stage0..3 – Seelenbeerenstrauch (die Beeren als Gegenstand: tools/items_food.py)
  * block/glimmer_vines(_plant)         – Glimmerranken

Wird von tools/generate_textures.py aufgerufen (build(put)).
"""
from __future__ import annotations

import math
from typing import Callable

from PIL import Image

import pixelart as pa
import shadow_blocks
from pixelart import Color, hexc

N = 16

CRYSTAL = pa.palette("#17154a", "#232270", "#33339a", "#4846c4", "#635ee6", "#8a7ef8", "#b4a8ff", "#ddd6ff", "#f7f4ff")
GHOST = pa.palette("#5a6a8a", "#8a9cbc", "#b8c8e4", "#e2ecfa", "#ffffff")
GHOST_GLOW = pa.palette("#6ad0f0", "#a8ecff", "#e8fbff")
STEM = pa.palette("#8c8aa0", "#b4b2c6", "#d8d6e6")
PALE = pa.palette("#5c566a", "#7a7488", "#9c96a8", "#bdb8c6", "#dcd8e2")


def _gray(v: int, a: int = 255) -> Color:
    v = max(0, min(255, v))
    return (v, v, v, a)


# =============================================================================
# Kristalldrusen und Pflanzen
# =============================================================================

NIGHT_CLUSTER = [
    "................",
    ".......a........",
    "......aba.......",
    "......abc...a...",
    "..a...abc..aba..",
    ".aba..abc..abc..",
    ".abc..abcd.abc..",
    ".abcd.abcd.abcd.",
    "..bcd.abcd.abcd.",
    "..bcd.abcd..bcd.",
    "...cdabbcd..bcd.",
    "...cdabccdabcd..",
    "....abbccdabcd..",
    "....abbccdbccd..",
    "...aabbccddccd..",
    "..aaabbbcccddd..",
]


def night_crystal_cluster() -> tuple[Image.Image, Image.Image]:
    colors = {"a": CRYSTAL[6], "b": CRYSTAL[5], "c": CRYSTAL[3], "d": CRYSTAL[2]}
    img = pa.sprite(NIGHT_CLUSTER, colors)
    px = img.load()
    for (x, y) in [(7, 1), (12, 4), (2, 4), (7, 2)]:
        px[x, y] = CRYSTAL[8]
    glow = pa.new()
    gp = glow.load()
    for y in range(N):
        for x in range(N):
            c = px[x, y]
            if c[3] and c in (CRYSTAL[8], CRYSTAL[6]):
                gp[x, y] = c
    return img, glow


GHOSTCAP = [
    "................",
    "................",
    "........oCCCo...",
    ".......oCwwcCo..",
    ".......CccccCC..",
    "..oCCo..o.s..o..",
    ".oCwcCo...s.....",
    ".CccccC...s.....",
    "...s......s.....",
    "...s.....ss.....",
    "...s....oCCo....",
    "...ss..oCwcCo...",
    "....s..CcccC....",
    "....s....s......",
    "...gsg..gsg.....",
    "................",
]


def ghostcap() -> tuple[Image.Image, Image.Image]:
    colors = {"o": GHOST[0], "C": GHOST[1], "c": GHOST[2], "w": GHOST[4], "s": STEM[1], "g": hexc("#3a3448")}
    img = pa.sprite(GHOSTCAP, colors)
    glow = pa.new()
    gp = glow.load()
    px = img.load()
    for y, row in enumerate(GHOSTCAP):
        for x, ch in enumerate(row):
            if ch == "w":
                gp[x, y] = GHOST_GLOW[2]
            elif ch == "c":
                gp[x, y] = GHOST_GLOW[1]
    _ = px
    return img, glow


def _blades(seed: int, count: int, heights: tuple[int, int], tones: list[Color], bend: float, base: int = 2) -> Image.Image:
    """Ein Büschel aus einzelnen, ein Pixel breiten Halmen wie bei Vanilla-Gras: verschieden hoch, unten dunkel, zur
    Spitze hin heller; manche Spitzen biegen sich zur Seite. tones: dunkel → hell."""
    import random
    rnd = random.Random(seed)
    img = pa.new()
    px = img.load()
    columns = list(range(1, N - 1))
    rnd.shuffle(columns)
    columns = sorted(columns[:count])
    for x in columns:
        h = rnd.randint(*heights)
        side = rnd.choice((-1, 1))
        bends = rnd.random() < bend
        for k in range(h):
            y = N - 1 - k
            xx = x + (side if bends and k >= h - max(2, h // 3) else 0)
            if not 0 <= xx < N:
                continue
            t = k / max(1, h - 1)
            level = min(len(tones) - 1, max(0, int(t * (len(tones) - 1) + rnd.uniform(-0.35, 0.45))))
            if k == h - 1:
                level = len(tones) - 1 if rnd.random() < 0.6 else len(tones) - 2
            if px[xx, y][3] == 0 or level > tones.index(px[xx, y]):
                px[xx, y] = tones[level]
    for x in range(1, N - 1):
        for k in range(base):
            y = N - 1 - k
            if px[x, y][3] == 0 and rnd.random() < 0.75 - 0.25 * k:
                px[x, y] = tones[rnd.randrange(0, 2)]
    return img


def shadow_sprouts() -> Image.Image:
    """Fedrige Schattenhalme (grau – die Farbe kommt aus dem Biom)."""
    tones = [_gray(92), _gray(122), _gray(150), _gray(178), _gray(206)]
    return _blades(31, 12, (5, 12), tones, 0.45, base=2)


def pale_grass() -> Image.Image:
    """Dürre, fahle Halme der Bleichen Öde: höher, dünner, mit hellen Spitzen."""
    tones = [PALE[0], PALE[1], PALE[2], PALE[3], PALE[4]]
    return _blades(57, 10, (7, 15), tones, 0.7, base=1)


# =============================================================================
# Seelenbeeren und Glimmerranken
# =============================================================================

BUSH_TONES = [hexc("#0b2624"), hexc("#123a37"), hexc("#1a4f4a"), hexc("#23665e"), hexc("#2f8072"), hexc("#46a08e")]
BERRY_UNRIPE = (hexc("#155a5e"), hexc("#2a8486"), hexc("#5cb4b0"))
BERRY_RIPE = (hexc("#1b9c9c"), hexc("#3fe0d6"), hexc("#d8fffb"))


def soul_berry_bush(stage: int) -> Image.Image:
    """Seelenbeerenstrauch wie ein Vanilla-Beerenstrauch: dichtes Laub bis zum Boden (Stufe 0 ein kleiner Schössling),
    ab Stufe 2 Beeren als 2×2-Trauben mit Glanzpunkt, reif leuchtend türkis."""
    import random
    rnd = random.Random(400 + stage)
    img = pa.new()
    px = img.load()
    top = (11, 6, 3, 2)[stage]          # wie hoch das Laub reicht
    half = (3.2, 5.6, 6.8, 7.4)[stage]  # halbe Breite
    for y in range(top, N):
        for x in range(N):
            # Umriss: nach oben runder und schmaler, unten breit; ausgefranster Rand
            v = (y - top) / max(1, N - 1 - top)
            width = half * (0.55 + 0.45 * min(1.0, v * 1.8))
            edge = width + rnd.uniform(-1.2, 0.8)
            if abs(x - 7.5) > edge:
                continue
            if rnd.random() < 0.1 and abs(x - 7.5) > width - 2:
                continue                  # Lücken am Rand
            shade = 0.25 + 0.55 * (1.0 - v) + rnd.uniform(-0.28, 0.28)
            if abs(x - 7.5) > width - 1.5:
                shade -= 0.18             # Rand dunkler (Tiefe)
            level = max(0, min(len(BUSH_TONES) - 1, int(shade * len(BUSH_TONES))))
            px[x, y] = BUSH_TONES[level]
    # einzelne Blätter über den Rand hinaus
    for _ in range(4 + stage * 2):
        x = rnd.randrange(1, N - 1)
        y = rnd.randrange(max(0, top - 2), N - 3)
        if px[x, y][3] == 0 and any(0 <= x + dx < N and px[x + dx, y + 1][3] for dx in (-1, 0, 1)):
            px[x, y] = BUSH_TONES[rnd.randrange(2, 5)]
    if stage >= 2:
        berry = BERRY_RIPE if stage == 3 else BERRY_UNRIPE
        spots = [(4, 7), (10, 6), (7, 10), (2, 11), (12, 11), (8, 4), (5, 13)][: 4 if stage == 2 else 7]
        for bx, by in spots:
            by = max(by, top + 1)
            if by + 1 >= N:
                continue
            px[bx, by] = berry[1]
            px[bx + 1, by] = berry[2]
            px[bx, by + 1] = berry[0]
            px[bx + 1, by + 1] = berry[1]
    return img


VINE_COLORS = {"d": hexc("#2a3450"), "m": hexc("#44587e"), "l": hexc("#6a86b0"), "h": hexc("#9cb6dc"),
               "g": hexc("#c8e0ff"), "G": hexc("#f2f8ff")}

GLIMMER_VINES_PLANT = [
    ".....m....m.....",
    ".....ml...m.....",
    "......m..lm.....",
    "......m..m......",
    ".....lm..m......",
    "......m..ml.....",
    "......dm.m......",
    ".......m.m......",
    ".......m.mh.....",
    "......hm.m......",
    ".......m.m......",
    ".......mdm......",
    "........m.......",
    "........ml......",
    ".......lm.......",
    "........m.......",
]

GLIMMER_VINES = [
    ".....m....m.....",
    ".....ml...m.....",
    "......m..lm.....",
    "......m..m......",
    ".....lm..m......",
    "......m..ml.....",
    "......dm.m......",
    ".......m.m......",
    ".......mhm......",
    "......lmmh......",
    "......hggh......",
    ".....hgGGgh.....",
    ".....gGGGGg.....",
    ".....hgGGgh.....",
    "......hggh......",
    ".......hh.......",
]


def glimmer_vines() -> tuple[Image.Image, Image.Image]:
    return pa.sprite(GLIMMER_VINES, VINE_COLORS), pa.sprite(GLIMMER_VINES_PLANT, VINE_COLORS)


# =============================================================================
# Ausgabe
# =============================================================================

def build(put: Callable[[str, Image.Image], None]) -> None:
    # die vollen Blöcke (Stein, Erde, Holz, Kristall, Moos …) kommen aus tools/shadow_blocks.py
    shadow_blocks.build(put)
    cluster, cluster_glow = night_crystal_cluster()
    put("block/night_crystal_cluster", cluster)
    put("block/night_crystal_cluster_emissive", cluster_glow)
    cap, cap_glow = ghostcap()
    put("block/ghostcap", cap)
    put("block/ghostcap_emissive", cap_glow)
    put("block/shadow_sprouts", shadow_sprouts())
    put("block/pale_grass", pale_grass())
    for stage in range(4):
        put(f"block/soul_berry_bush_stage{stage}", soul_berry_bush(stage))
    head, plant = glimmer_vines()
    put("block/glimmer_vines", head)
    put("block/glimmer_vines_plant", plant)


if __name__ == "__main__":
    import sys
    out: dict[str, Image.Image] = {}
    build(lambda k, v: out.__setitem__(k, v))
    target = sys.argv[1] if len(sys.argv) > 1 else "shadow_preview.png"
    zoom, pad, cols = 8, 6, 6
    cell_w = 16 * 3 * zoom // 2 + pad
    rows = math.ceil(len(out) / cols)
    sheet = Image.new("RGBA", (cols * cell_w + pad, rows * cell_w + pad), (40, 42, 48, 255))
    for i, (key, img) in enumerate(out.items()):
        tile = pa.checker(48, 48, cell=4)
        tile.alpha_composite(pa.tiled(img, 3))
        tile = tile.resize((48 * zoom // 2, 48 * zoom // 2), Image.NEAREST)
        sheet.alpha_composite(tile, (pad + (i % cols) * cell_w, pad + (i // cols) * cell_w))
    sheet.save(target)
    print(f"{len(out)} Texturen, Vorschau: {target}")
