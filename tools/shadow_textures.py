#!/usr/bin/env python3
"""
Texturen der Schattenwelt – Pflanzen und Kristalldrusen (die vollen Blöcke kommen aus tools/shadow_blocks.py):

  * block/night_crystal_cluster(_emissive) – Druse aus spitzen Kristallen (Kreuzpflanze, leuchtend)
  * block/ghostcap(_emissive)           – Geisterpilze mit leuchtenden Hüten
  * block/shadow_sprouts                – fedrige Schattenhalme (grau, Farbe aus dem Biom)
  * block/pale_grass                    – dürre, fahle Halme der Bleichen Öde
  * block/soul_berry_bush_stage0..3 – Seelenbeerenstrauch (die Beeren als Gegenstand: tools/items_food.py)
  * block/glimmer_vines(_plant)         – Glimmerranken
  * block/moon_fern_bottom/top(_emissive) – Mondfarn (zwei Blöcke hoch, leuchtende Sporen an den Wedelspitzen)
  * block/star_blossom(_emissive)       – Sternblüte (sternförmige, leuchtende Blüten)
  * block/soul_reeds_bottom/top(_emissive) – Seelenschilf (leuchtende Samenstände)

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
# Mondfarn, Sternblüte und Seelenschilf
# =============================================================================

FERN = pa.palette("#18203a", "#24345a", "#344b7a", "#4a679c", "#6886ba", "#90aed6", "#c2d6ee")
FERN_GLOW = pa.palette("#78cfff", "#bdeeff", "#f2fcff")


def _plot(px, w: int, h: int, x: float, y: float, color: Color, glow=None, glow_color: Color | None = None) -> None:
    ix, iy = int(round(x)), int(round(y))
    if 0 <= ix < w and 0 <= iy < h:
        px[ix, iy] = color
        if glow is not None and glow_color is not None:
            glow[ix, iy] = glow_color


def _line(points: list[tuple[float, float]]) -> list[tuple[int, int]]:
    """Die Pixel entlang eines Linienzugs (ohne Doppelte), vom ersten zum letzten Punkt."""
    out: list[tuple[int, int]] = []
    for (x0, y0), (x1, y1) in zip(points, points[1:]):
        steps = max(1, int(math.ceil(max(abs(x1 - x0), abs(y1 - y0)))))
        for i in range(steps + 1):
            t = i / steps
            p = (int(round(x0 + (x1 - x0) * t)), int(round(y0 + (y1 - y0) * t)))
            if not out or out[-1] != p:
                out.append(p)
    return out


# Wedel des Mondfarns (16 × 32, Fuß unten in der Mitte): Linienzug vom Fuß zur Spitze, Tiefe (0 hinten … 2 vorn),
# eingerollte, leuchtende Spitze (Richtung der Einrollung) oder keine
MOON_FERN_FRONDS = [
    ([(8, 31), (8, 25), (7, 19), (7, 13), (8, 8), (9, 5), (10, 4)], 0, (1, 1)),
    ([(7, 31), (6, 27), (4, 22), (2, 17), (1, 13), (1, 10), (2, 8)], 1, (1, 1)),
    ([(8, 31), (10, 27), (12, 22), (14, 17), (15, 14), (15, 11), (14, 9)], 1, (-1, 1)),
    ([(7, 31), (5, 30), (3, 28), (1, 27), (0, 28)], 2, None),
    ([(8, 31), (10, 30), (12, 28), (14, 27), (15, 28)], 2, None),
]

def moon_fern() -> tuple[Image.Image, Image.Image, Image.Image]:
    """Mondfarn, zwei Blöcke hoch: ein Fächer aus silbrig blauen Wedeln – drei hohe, die sich auseinanderbiegen und
    deren Spitzen sich einrollen und glimmen (eigene Leuchtschicht), dazu kurze, überhängende Wedel am Fuß. Die Fiedern
    stehen paarweise schräg zur Spitze (Fischgrät), unten zwei Pixel lang, oben eins und heller.
    Rückgabe: unten, oben, oben leuchtend."""
    w, h = N, 2 * N
    img = pa.new(w=w, h=h)
    glow = pa.new(w=w, h=h)
    px = img.load()
    gp = glow.load()

    def put(x: int, y: int, color: Color, glowing: Color | None = None) -> None:
        if 0 <= x < w and 0 <= y < h:
            px[x, y] = color
            if glowing is not None:
                gp[x, y] = glowing

    for points, depth, curl in MOON_FERN_FRONDS:
        line = _line([(float(x), float(y)) for x, y in points])
        n = len(line)
        # Fiedern (hinter der Rippe gezeichnet)
        for i in range(2, n - 1, 2):
            x, y = line[i]
            nx, ny = line[i + 1]
            dx, dy = nx - x, ny - y
            t = i / max(1, n - 1)
            for side in (-1, 1):
                pxs, pys = -dy * side, dx * side
                # außen (vom Fächer weg) im unteren Teil zwei Pixel, sonst eins – so bleiben Lücken zwischen den Wedeln
                outward = (pxs > 0) == (line[-1][0] > 7.5) or abs(line[-1][0] - 7.5) < 3
                length = 2 if (t < 0.5 and outward) else 1
                for k in range(1, length + 1):
                    tone = 3 + (depth + 1) // 2 + (1 if k == length else 0) + (1 if t > 0.7 else 0)
                    put(x + (pxs + dx) * k, y + (pys + dy) * k, FERN[min(6, tone)])
        # Mittelrippe
        for i, (x, y) in enumerate(line):
            t = i / max(1, n - 1)
            put(x, y, FERN[min(5, (1 if t < 0.4 else 2 if t < 0.75 else 3) + depth // 2)])
        tx, ty = line[-1]
        if curl is not None:
            put(tx, ty, FERN_GLOW[2], FERN_GLOW[2])
            put(tx + curl[0], ty, FERN_GLOW[1], FERN_GLOW[1])
            put(tx + curl[0], ty + curl[1], FERN_GLOW[0], FERN_GLOW[0])
        else:
            put(tx, ty, FERN[6])
    # am Fuß ein dunkles Büschel
    for x in range(5, 11):
        if px[x, h - 1][3] == 0:
            px[x, h - 1] = FERN[0]
    top = img.crop((0, 0, w, N))
    bottom = img.crop((0, N, w, h))
    top_glow = glow.crop((0, 0, w, N))
    return bottom, top, top_glow


STAR = {"p": hexc("#78b8e0"), "W": hexc("#d6f4ff"), "w": hexc("#a8dcf6"), "y": hexc("#f6e2a0"),
        "s": hexc("#3a4e70"), "S": hexc("#4c6690"), "l": hexc("#5a7aa6"), "L": hexc("#7c9cc8"), "d": hexc("#26324e")}

STAR_BLOSSOM = [
    "..........p.....",
    ".........pWp....",
    "...p....pwyWp...",
    "..pWp....pWp....",
    ".pwyWp..p.s.p...",
    "..pWp.....s.....",
    ".p.s.p...s......",
    "...s......s..p..",
    "...s...l..s.pWp.",
    "....s..L..spwyWp",
    "....s.lL.s..pWp.",
    ".L..s.l..s.p.s.p",
    ".Ll.s.l.s....s..",
    "..lls.ls.S..sl..",
    "...lsSlsS.lsL...",
    "..dddsdssddsd...",
]


def star_blossom() -> tuple[Image.Image, Image.Image]:
    """Sternblüte: drei sternförmige, fahlblaue Blüten mit goldener Mitte auf dünnen Stielen; die Blütenblätter leuchten
    (eigene Leuchtschicht)."""
    img = pa.sprite(STAR_BLOSSOM, STAR)
    glow = pa.new()
    gp = glow.load()
    for y, row in enumerate(STAR_BLOSSOM):
        for x, ch in enumerate(row):
            if ch in "Wwy":
                gp[x, y] = STAR[ch]
            elif ch == "p":
                gp[x, y] = hexc("#5aa6d6")
    return img, glow


SOUL_STALK = pa.palette("#12282e", "#1c3c44", "#285458", "#3a6e6e", "#56908a")
SOUL_LEAF = pa.palette("#173236", "#22484a", "#2f605e", "#457c76", "#62a096")
SOUL_HEAD = pa.palette("#1f8c88", "#36c2b8", "#7af0e2", "#dafff9")
SOUL_STALKS = [(3.0, 0.05, 2), (6.0, -0.06, 3), (8.5, 0.03, 2), (11.0, 0.08, 3), (13.0, -0.05, 2)]


def _soul_stalk(img: Image.Image, x0: float, lean: float, y_from: int, y_to: int, shade: int) -> None:
    for y in range(y_from, y_to - 1, -1):
        x = int(round(x0 + lean * (y_from - y)))
        if 0 <= x < N:
            tone = shade if ((y_from - y) // 3) % 2 == 0 else max(0, shade - 1)
            img.putpixel((x, y), SOUL_STALK[tone])


def _soul_leaf(img: Image.Image, x0: int, y0: int, direction: int, length: int, droop: int) -> None:
    x, y = float(x0), y0
    for i in range(length):
        t = i / max(1, length - 1)
        x += direction * (0.18 + 0.8 * t * t * droop / 3)
        if t < 0.88 or droop < 2:
            y -= 1
        ix = int(round(x))
        if 0 <= ix < N and 0 <= y < N:
            img.putpixel((ix, y), SOUL_LEAF[1] if t < 0.3 else (SOUL_LEAF[2] if t < 0.8 else SOUL_LEAF[3]))


def soul_reeds() -> tuple[Image.Image, Image.Image, Image.Image]:
    """Seelenschilf: dunkle, blaugrüne Halme mit langen Blättern; oben tragen drei Halme längliche Samenstände, die
    türkis leuchten (eigene Leuchtschicht). Rückgabe: unten, oben, oben leuchtend."""
    bottom = pa.new()
    _soul_leaf(bottom, 3, 15, -1, 13, 2)
    _soul_leaf(bottom, 12, 15, 1, 14, 2)
    _soul_leaf(bottom, 9, 15, -1, 15, 1)
    for x0, lean, shade in SOUL_STALKS:
        _soul_stalk(bottom, x0, lean, 15, 0, shade)
    for x in range(N):
        if bottom.getpixel((x, 15))[3]:
            bottom.putpixel((x, 15), SOUL_STALK[0])
    top = pa.new()
    glow = pa.new()
    _soul_leaf(top, 2, 15, -1, 9, 3)
    _soul_leaf(top, 14, 15, 1, 7, 3)
    heights = [5, 8, 3, 6, 10]
    for (x0, lean, shade), y_top in zip(SOUL_STALKS, heights):
        _soul_stalk(top, x0 + lean * 16, lean, 15, y_top, shade)
    for index, y_top, length in ((0, 6, 5), (2, 3, 6), (4, 10, 4)):
        x0, lean, _ = SOUL_STALKS[index]
        x = min(int(round(x0 + lean * 16 + lean * (15 - y_top))), N - 2)
        for i in range(length):
            y = y_top + i
            tone = 2 if i <= 1 else (1 if i < length - 1 else 0)
            for dx, t in ((0, tone), (1, max(0, tone - 1))):
                top.putpixel((x + dx, y), SOUL_HEAD[t])
                glow.putpixel((x + dx, y), SOUL_HEAD[min(3, t + 1)])
        for i, y in enumerate(range(y_top - 1, y_top - 3, -1)):
            if y >= 0:
                top.putpixel((x, y), SOUL_HEAD[3] if i == 0 else SOUL_HEAD[2])
                glow.putpixel((x, y), SOUL_HEAD[3])
    return bottom, top, glow


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
    fern_bottom, fern_top, fern_glow = moon_fern()
    put("block/moon_fern_bottom", fern_bottom)
    put("block/moon_fern_top", fern_top)
    put("block/moon_fern_top_emissive", fern_glow)
    star, star_glow = star_blossom()
    put("block/star_blossom", star)
    put("block/star_blossom_emissive", star_glow)
    reeds_bottom, reeds_top, reeds_glow = soul_reeds()
    put("block/soul_reeds_bottom", reeds_bottom)
    put("block/soul_reeds_top", reeds_top)
    put("block/soul_reeds_top_emissive", reeds_glow)


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
