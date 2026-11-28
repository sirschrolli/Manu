#!/usr/bin/env python3
"""
Die Texturen der Meereshölzer (0.92) im Vanilla-Stil (siehe tools/vanilla_style.py):

  * Tangholz      – olivbraune, faserige Rinde mit goldenen Schwimmblasen, khakifarbene Bretter mit welliger Maserung,
                    Tangwedel in Grün und Gold;
  * Korallenholz  – rosarote, poröse Rinde voller Polypenpunkte, lachsfarbene Bretter, Korallenfächer als Gitter in
                    Orange und Rosa;
  * Perlholz      – cremeweiße, glatte Rinde mit Perlmuttschimmer (rosa, himmelblau, mint), helle Bretter mit
                    schillernden Lichtern, Perlblätter in Eisblau mit kleinen Perlen;
  * Tiefseeholz   – fast schwarze Rinde mit türkis leuchtenden Adern, nachtblaue Bretter mit türkiser Maserung,
                    Leuchtwedel in dunklem Petrol mit hellen Lichtpunkten.

Türen und Falltüren haben ein rundes Bullauge mit Messing-, Silber- oder Leuchtrand.

Wird von tools/generate_textures.py aufgerufen (build(put)); allein: python3 tools/sea_wood_textures.py [--preview DATEI]
schreibt nur diese Texturen.
"""
from __future__ import annotations

import math
import random
import sys
from pathlib import Path
from typing import Callable

from PIL import Image

sys.path.insert(0, str(Path(__file__).resolve().parent))

import pixelart as pa  # noqa: E402
import vanilla_style as vs  # noqa: E402
from pixelart import Color, hexc  # noqa: E402

N = 16
CLEAR = (0, 0, 0, 0)


class Wood:
    """Farben und Eigenheiten eines Meeresholzes."""

    def __init__(self, name: str, seed: int, bark: list[Color], wood: list[Color], stripped: list[Color], planks: list[Color],
                 leaf: list[Color], accent: list[Color], metal: list[Color], specks: list[Color]):
        self.name = name
        self.seed = seed
        self.bark = bark
        self.wood = wood
        self.stripped = stripped
        self.planks = planks
        self.leaf = leaf          # dunkel -> hell (5 Stufen)
        self.accent = accent      # Schwimmblasen, Polypen, Perlen, Lichtpunkte
        self.metal = metal        # Rand des Bullauges (dunkel, mittel, hell)
        self.specks = specks      # Punkte in Rinde und Brettern


KELP = Wood("kelpwood", 92100,
            bark=vs.tones("#6b6436", 6, spread=0.34, cool=110, warm=50, hue=10, sat=0.06),
            wood=vs.tones("#b8a35e", 6, spread=0.24, cool=70, warm=50, hue=8, sat=0.05),
            stripped=vs.tones("#a99a58", 5, spread=0.22, cool=80, warm=50, hue=8, sat=0.05),
            planks=vs.tones("#a8975a", 7, spread=0.27, cool=80, warm=45, hue=8, sat=0.05),
            leaf=[hexc("#2e5a22"), hexc("#3f7a2a"), hexc("#5a9a34"), hexc("#86bc48"), hexc("#c6e07a")],
            accent=[hexc("#9a7a1e"), hexc("#d8b03a"), hexc("#f6dc78")],
            metal=[hexc("#6a4a1a"), hexc("#b08a3a"), hexc("#f0d07a")],
            specks=[hexc("#c8a840"), hexc("#e8cc68")])

CORAL = Wood("coralwood", 92200,
             bark=vs.tones("#b4444e", 6, spread=0.32, cool=345, warm=15, hue=10, sat=0.05),
             wood=vs.tones("#ec9a8e", 6, spread=0.22, cool=355, warm=20, hue=8, sat=0.04),
             stripped=vs.tones("#e48a7e", 5, spread=0.22, cool=355, warm=20, hue=8, sat=0.04),
             planks=vs.tones("#de8478", 7, spread=0.27, cool=350, warm=20, hue=10, sat=0.05),
             leaf=[hexc("#b8402a"), hexc("#e0603a"), hexc("#f4864e"), hexc("#ffae78"), hexc("#ffdcb8")],
             accent=[hexc("#f6c0c0"), hexc("#fff0e8"), hexc("#ffffff")],
             metal=[hexc("#6a3a1a"), hexc("#b07a3a"), hexc("#f0c07a")],
             specks=[hexc("#ffd0c8"), hexc("#7a1e28")])

PEARL = Wood("pearlwood", 92300,
             bark=vs.tones("#cfc6b6", 6, spread=0.18, cool=230, warm=40, hue=10, sat=0.03),
             wood=vs.tones("#efe8dc", 6, spread=0.12, cool=220, warm=40, hue=8, sat=0.03),
             stripped=vs.tones("#e8e0d4", 5, spread=0.12, cool=220, warm=40, hue=8, sat=0.03),
             planks=vs.tones("#e2dacb", 7, spread=0.15, cool=230, warm=40, hue=8, sat=0.03),
             leaf=[hexc("#8a80b0"), hexc("#b0aad6"), hexc("#d6d2ee"), hexc("#f2eefa"), hexc("#ffffff")],
             accent=[hexc("#f8bede"), hexc("#b6e6f8"), hexc("#f6e4bc")],
             metal=[hexc("#6a7080"), hexc("#b8c0d0"), hexc("#f4f8ff")],
             specks=[hexc("#f6c8e4"), hexc("#c4e4f8"), hexc("#ccf4dc")])

ABYSS = Wood("abysswood", 92400,
             bark=vs.tones("#1e2638", 6, spread=0.40, cool=230, warm=200, hue=10, sat=0.06),
             wood=vs.tones("#2e4a66", 6, spread=0.30, cool=225, warm=195, hue=8, sat=0.05),
             stripped=vs.tones("#2a3e5c", 5, spread=0.30, cool=225, warm=195, hue=8, sat=0.05),
             planks=vs.tones("#2c3a54", 7, spread=0.32, cool=230, warm=200, hue=10, sat=0.05),
             leaf=[hexc("#06333a"), hexc("#0a4e56"), hexc("#14727a"), hexc("#2aa8a8"), hexc("#7af6e4")],
             accent=[hexc("#2ad8d0"), hexc("#7affec"), hexc("#e0fffa")],
             metal=[hexc("#0e5a60"), hexc("#3ac8c0"), hexc("#b0fff4")],
             specks=[hexc("#3ae0d8"), hexc("#8af8ec")])

WOODS = (KELP, CORAL, PEARL, ABYSS)


def _px(img: Image.Image, x: int, y: int, c: Color) -> None:
    if 0 <= x < img.width and 0 <= y < img.height:
        img.putpixel((x, y), c)


# =============================================================================
# Stamm, Stirnseite, entrindet, Bretter
# =============================================================================

def log(w: Wood) -> Image.Image:
    g = vs.bark(w.seed + 1, grooves=6 if w is KELP else 4)
    img = vs.paint(g, w.bark)
    rnd = random.Random(w.seed + 2)
    if w is KELP:
        # goldene Schwimmblasen, je zwei Pixel übereinander, und durchgehende helle Fasern
        for _ in range(4):
            x, y = rnd.randrange(N), rnd.randrange(N)
            _px(img, x, y, w.accent[2])
            _px(img, x, (y + 1) % N, w.accent[1])
        for _ in range(2):
            x = rnd.randrange(N)
            for y in range(N):
                if g[y][x] >= 3 and rnd.random() < 0.6:
                    _px(img, x, y, w.bark[5])
    elif w is CORAL:
        # Polypen: helle Punkte mit dunklem Rand, wie bei einem Korallenstock
        for _ in range(9):
            x, y = rnd.randrange(1, N - 1), rnd.randrange(N)
            _px(img, x, y, w.accent[1])
            _px(img, x, (y + 1) % N, w.specks[1])
    elif w is PEARL:
        # Perlmuttschimmer: kurze, schräge Streifen in Rosa, Himmelblau und Mint
        for k in range(7):
            x, y = rnd.randrange(N), rnd.randrange(N)
            c = w.specks[k % 3]
            for i in range(rnd.randint(2, 4)):
                _px(img, (x + i // 2) % N, (y + i) % N, c)
    else:
        # leuchtende Adern: zwei feine Linien, die sich den Stamm hinaufwinden (oben und unten nahtlos, damit sie über
        # gestapelte Stämme weiterlaufen), eine kurze Abzweigung und helle Knoten
        def vein(x0: int, phase: float, k: int, rows: range) -> list[tuple[int, int]]:
            points = []
            prev = None
            for y in rows:
                x = (x0 + round(1.5 * math.sin(2.0 * math.pi * k * (y % N) / N + phase))) % N
                if prev is not None and x != prev:
                    points.append((prev, y % N))  # Block an Block: erst seitlich, dann weiter hinauf
                points.append((x, y % N))
                prev = x
            return points
        first = rnd.randrange(N)
        lines = [vein(first, rnd.random() * 6.283, 1, range(N)),
                 vein((first + 7 + rnd.randrange(3)) % N, rnd.random() * 6.283, 2, range(N))]
        start = rnd.randrange(N)
        lines.append(vein(rnd.randrange(N), rnd.random() * 6.283, 1, range(start, start + 5)))
        for points in lines:
            for x, y in points:
                _px(img, x, y, w.accent[0])
        for points in lines[:2]:
            for _ in range(2):
                x, y = points[rnd.randrange(len(points))]
                _px(img, x, y, w.accent[1])
    return img


def log_top(w: Wood) -> Image.Image:
    g = vs.log_top(w.seed + 11)
    pal = [w.bark[0], w.bark[2], w.wood[1], w.wood[2], w.wood[3], w.wood[4]]
    img = vs.paint(g, pal)
    rnd = random.Random(w.seed + 12)
    if w is CORAL:
        for _ in range(10):
            x, y = rnd.randrange(2, N - 2), rnd.randrange(2, N - 2)
            _px(img, x, y, w.specks[1])
    elif w is PEARL:
        # die Ringe schillern
        for y in range(N):
            for x in range(N):
                if g[y][x] == 5:
                    _px(img, x, y, w.specks[(x + y) % 3])
    elif w is ABYSS:
        for y in range(N):
            for x in range(N):
                if g[y][x] == 5 or g[y][x] == 2:
                    _px(img, x, y, w.accent[0] if g[y][x] == 5 else w.accent[1])
    return img


def stripped(w: Wood) -> Image.Image:
    g = vs.stripped(w.seed + 21)
    img = vs.paint(g, w.stripped)
    rnd = random.Random(w.seed + 22)
    if w is PEARL:
        for k in range(5):
            x, y = rnd.randrange(N), rnd.randrange(N)
            for i in range(3):
                _px(img, x, (y + i) % N, w.specks[k % 3])
    elif w is ABYSS:
        for _ in range(3):
            x = rnd.randrange(N)
            y0 = rnd.randrange(N)
            for i in range(rnd.randint(4, 9)):
                _px(img, x, (y0 + i) % N, w.accent[0])
    elif w is CORAL:
        for _ in range(6):
            _px(img, rnd.randrange(N), rnd.randrange(N), w.specks[0])
    return img


def stripped_top(w: Wood) -> Image.Image:
    g = vs.log_top(w.seed + 31)
    pal = [w.stripped[0], w.stripped[1], w.wood[1], w.wood[2], w.wood[3], w.wood[4]]
    return vs.paint(g, pal)


def planks(w: Wood) -> Image.Image:
    g = vs.planks(w.seed + 41, joints=(None, 10, 4, 13))
    img = vs.paint(g, w.planks)
    rnd = random.Random(w.seed + 42)
    if w is KELP:
        # wellige Maserung: hier und da ein heller Strich, der eine Zeile springt
        for _ in range(5):
            x, b = rnd.randrange(N - 3), rnd.randrange(4)
            y = b * 4 + 1 + rnd.randrange(2)
            for i in range(3):
                _px(img, x + i, y + (1 if i == 1 else 0), w.planks[6])
    elif w is CORAL:
        for _ in range(8):
            x, y = rnd.randrange(N), rnd.randrange(N)
            if y % 4 not in (0, 3):
                _px(img, x, y, w.specks[1] if rnd.random() < 0.6 else w.specks[0])
    elif w is PEARL:
        for k in range(8):
            x, y = rnd.randrange(N), rnd.randrange(N)
            if y % 4 not in (0, 3):
                _px(img, x, y, w.specks[k % 3])
    else:
        for b in range(4):
            if rnd.random() < 0.8:
                y = b * 4 + 1 + rnd.randrange(2)
                x0 = rnd.randrange(N)
                for i in range(rnd.randint(3, 6)):
                    _px(img, (x0 + i) % N, y, w.accent[0])
    return img


# =============================================================================
# Laub
# =============================================================================

def leaves(w: Wood) -> Image.Image:
    if w is CORAL:
        return coral_fans(w)
    g = vs.leaves(w.seed + 51, holes=0.3 if w is not PEARL else 0.24)
    img = vs.paint(g, w.leaf[:4])
    rnd = random.Random(w.seed + 52)
    count = 0
    tips = 6 if w is not ABYSS else 9
    while count < tips:
        x, y = rnd.randrange(N), rnd.randrange(N)
        if g[y][x] == 3:
            img.putpixel((x, y), w.leaf[4])
            count += 1
    if w is KELP:
        # Wedel: senkrechte Bänder, dazu goldene Schwimmblasen
        for _ in range(4):
            x, y = rnd.randrange(N), rnd.randrange(N - 1)
            if g[y][x] >= 0:
                img.putpixel((x, y), w.accent[2])
                if g[y + 1][x] >= 0:
                    img.putpixel((x, y + 1), w.accent[1])
    elif w is PEARL:
        for k in range(7):
            x, y = rnd.randrange(N), rnd.randrange(N)
            if g[y][x] >= 0:
                img.putpixel((x, y), w.accent[k % 3])
    else:
        for _ in range(6):
            x, y = rnd.randrange(N), rnd.randrange(N)
            if g[y][x] >= 1:
                img.putpixel((x, y), w.accent[1] if rnd.random() < 0.6 else w.accent[2])
    return img


def coral_fans(w: Wood) -> Image.Image:
    """Korallenfächer: ein Gitter aus feinen Ästchen (Netz), dazwischen Lücken; Spitzen hell."""
    rnd = random.Random(w.seed + 61)
    img = Image.new("RGBA", (N, N), CLEAR)
    shade = vs.noise(w.seed + 62, 4, 4)
    for y in range(N):
        for x in range(N):
            # Netz: zwei schräge Scharen von Ästchen, leicht verwackelt
            a = (x + y + int(shade[y][x] * 3)) % 4 == 0
            b = (x - y + 16 + int(shade[y][x] * 3)) % 5 == 0
            if a or b or rnd.random() < 0.18:
                v = 1 + int(shade[y][x] * 2.5)
                if rnd.random() < 0.15:
                    v = 3
                img.putpixel((x, y), w.leaf[max(0, min(4, v))])
    # Polypen: helle Punkte
    for _ in range(8):
        x, y = rnd.randrange(N), rnd.randrange(N)
        if img.getpixel((x, y))[3]:
            img.putpixel((x, y), w.leaf[4])
    return img


# =============================================================================
# Setzlinge
# =============================================================================

KELP_SPROUT = [
    "................",
    "........g.......",
    ".......gG..f....",
    "......gG..fF....",
    ".....bgG..fFf...",
    "...f.gGg.fF.....",
    "..fF.gG..Ff.....",
    "..fFf.gG.f..o...",
    "...Ff.gGfF..oO..",
    "....fFgGF.......",
    "......gGo.......",
    ".......gOo......",
    ".......gG.......",
    ".......sS.......",
    "......sSs.......",
    "................",
]

CORAL_SPROUT = [
    "................",
    "...a........a...",
    "...ab......ab...",
    "....b..a..bb....",
    "....bb.ab.b.....",
    ".a...b.bb.b..a..",
    ".ab..bbbbbb.ab..",
    "..bb..bbbb..b...",
    "...bb.bbbb.bb...",
    "....bbbbbbbb....",
    "......bbbb......",
    ".......bb.......",
    ".......bb.......",
    "......cbbc......",
    ".....cc..cc.....",
    "................",
]

PEARL_SPROUT = [
    "................",
    "......lLl.......",
    ".....lLwLl......",
    "....lLLwLLl.....",
    "...lLLLLLLLl....",
    "...lLLpLLLLl....",
    "....lLLLLLl.....",
    ".....llsLl......",
    ".......s........",
    ".......s..l.....",
    ".......s.lL.....",
    ".......sl.......",
    ".......s........",
    ".......s........",
    "......sSs.......",
    "................",
]

ABYSS_SPROUT = [
    "................",
    ".......x........",
    "......xXx.......",
    ".....xX*Xx......",
    "......xXx.......",
    ".......s........",
    "...t...s...t....",
    "..tT..ss..Tt....",
    "..tT..s...Tt....",
    "...tT.s..Tt.....",
    "....tTs.Tt......",
    "......sS........",
    "......sS........",
    "......sS........",
    ".....sSSs.......",
    "................",
]


def sapling(w: Wood) -> Image.Image:
    if w is KELP:
        colors = {"g": w.leaf[1], "G": w.leaf[3], "f": w.leaf[2], "F": w.leaf[4], "b": w.accent[1], "o": w.accent[1], "O": w.accent[2],
                  "s": w.bark[1], "S": w.bark[3]}
        return pa.sprite(KELP_SPROUT, colors)
    if w is CORAL:
        colors = {"a": w.leaf[4], "b": w.leaf[2], "c": w.bark[2]}
        img = pa.sprite(CORAL_SPROUT, colors)
        # Schattierung: die rechte Seite jedes Ästchens dunkler
        for y in range(N):
            for x in range(N - 1):
                if img.getpixel((x, y))[3] and not img.getpixel((x + 1, y))[3] and CORAL_SPROUT[y][x] == "b":
                    img.putpixel((x, y), w.leaf[1])
        return img
    if w is PEARL:
        colors = {"l": w.leaf[1], "L": w.leaf[2], "w": w.leaf[4], "p": w.accent[1], "s": w.bark[2], "S": w.bark[4]}
        return pa.sprite(PEARL_SPROUT, colors)
    colors = {"x": w.leaf[3], "X": w.accent[1], "*": w.accent[2], "t": w.leaf[2], "T": w.leaf[4], "s": w.bark[2], "S": w.bark[4]}
    return pa.sprite(ABYSS_SPROUT, colors)


# =============================================================================
# Tür und Falltür (mit Bullauge)
# =============================================================================

def _door_body(w: Wood, seed: int) -> vs.Grid:
    grain = vs.quantize(vs.strokes(seed, (1,), (1, 2, 3, 2, 1)), (0.25, 0.5, 0.25))
    g = vs.grid(3)
    for y in range(N):
        for x in range(N):
            if x in (0, 15):
                v = 1
            elif x == 1:
                v = 5
            elif x == 14:
                v = 2
            else:
                col = (x - 2) % 4
                v = 5 if col == 0 else 1 if col == 3 else 2 + grain[y][x]
            g[y][x] = v
    return g


def _porthole(img: Image.Image, w: Wood, cx: float, cy: float, r: float, glass: bool) -> None:
    """Ein rundes Bullauge: Rand aus Metall (oben links hell), innen offen oder getönt."""
    for y in range(N):
        for x in range(N):
            d = math.hypot(x + 0.5 - cx, y + 0.5 - cy)
            if d <= r - 1.0:
                img.putpixel((x, y), CLEAR if not glass else w.accent[0])
            elif d <= r + 0.15:
                light = (x + 0.5 - cx) + (y + 0.5 - cy) < 0
                img.putpixel((x, y), w.metal[2] if light else w.metal[1])
            elif d <= r + 0.75:
                img.putpixel((x, y), w.metal[0])


def door(w: Wood) -> tuple[Image.Image, Image.Image, Image.Image]:
    pal = w.planks
    g = _door_body(w, w.seed + 71)
    for x in range(N):
        g[0][x] = 1 if 0 < x < 15 else 0
        g[1][x] = 5 if 0 < x < 15 else 4
    top = vs.paint(g, pal)
    _porthole(top, w, 8.0, 8.5, 4.6, False)
    g = _door_body(w, w.seed + 72)
    for x in range(N):
        g[14][x] = 5 if 0 < x < 15 else 4
        g[15][x] = 1 if 0 < x < 15 else 0
    bottom = vs.paint(g, pal)
    # Beschläge und Griff aus dem Metall des Bullauges
    for y in (3, 11):
        for x in range(1, 7):
            bottom.putpixel((x, y), w.metal[1])
            bottom.putpixel((x, y + 1), w.metal[0])
        bottom.putpixel((1, y), w.metal[2])
    for (x, y, c) in ((12, 6, w.metal[2]), (12, 7, w.metal[1]), (13, 7, w.metal[0])):
        bottom.putpixel((x, y), c)
    item = pa.new()
    item.alpha_composite(top.resize((8, 8), Image.NEAREST), (4, 0))
    item.alpha_composite(bottom.resize((8, 8), Image.NEAREST), (4, 8))
    return top, bottom, item


def trapdoor(w: Wood) -> Image.Image:
    g = vs.planks(w.seed + 81, joints=(None, None, None, None))
    img = vs.paint(g, w.planks)
    for y in range(N):
        for x in range(N):
            if x in (0, 15) or y in (0, 15):
                img.putpixel((x, y), w.planks[1])
            elif x in (1, 14) or y in (1, 14):
                img.putpixel((x, y), w.planks[5])
    _porthole(img, w, 8.0, 8.0, 4.4, False)
    return img


# =============================================================================
# Alles zusammen
# =============================================================================

def build(put: Callable[[str, Image.Image], None]) -> None:
    for w in WOODS:
        n = w.name
        put(f"block/{n}_log", log(w))
        put(f"block/{n}_log_top", log_top(w))
        put(f"block/stripped_{n}_log", stripped(w))
        put(f"block/stripped_{n}_log_top", stripped_top(w))
        put(f"block/{n}_planks", planks(w))
        put(f"block/{n}_leaves", leaves(w))
        put(f"block/{n}_sapling", sapling(w))
        top, bottom, item = door(w)
        put(f"block/{n}_door_top", top)
        put(f"block/{n}_door_bottom", bottom)
        put(f"item/{n}_door", item)
        put(f"block/{n}_trapdoor", trapdoor(w))


def main() -> None:
    root = Path(__file__).resolve().parent.parent / "src" / "main" / "resources" / "assets" / "kingdomsatwar" / "textures"
    images: dict[str, Image.Image] = {}

    def put(key: str, img: Image.Image) -> None:
        images[key] = img

    build(put)
    for key, img in images.items():
        target = root / f"{key}.png"
        target.parent.mkdir(parents=True, exist_ok=True)
        img.save(target)
    print(f"{len(images)} Texturen geschrieben")
    if "--preview" in sys.argv:
        out = Path(sys.argv[sys.argv.index("--preview") + 1])
        keys = list(images)
        cols = 12
        zoom = 6
        rows = (len(keys) + cols - 1) // cols
        sheet = Image.new("RGBA", (cols * (N * zoom + 6), rows * (N * zoom + 6)), (60, 70, 90, 255))
        for i, key in enumerate(keys):
            img = images[key].resize((N * zoom, N * zoom), Image.NEAREST)
            sheet.alpha_composite(img, ((i % cols) * (N * zoom + 6) + 3, (i // cols) * (N * zoom + 6) + 3))
        sheet.save(out)
        print("Vorschau:", out)


if __name__ == "__main__":
    main()
