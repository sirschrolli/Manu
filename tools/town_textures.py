#!/usr/bin/env python3
"""
Texturen der eigenen Siedlung (Wirtschaft): das Stadtbanner (Block und Gegenstand), der Bauplan und die Hintergründe
von Stadtfenster und Handelsfenster.

  * block/town_banner_cloth_upper / _lower – das Tuch des Stadtbanners (12x22, auf zwei Blöcke verteilt):
    königsblau mit Goldborte, goldener Krone und Schwalbenschwanz unten
  * item/town_banner – Banner am Mast auf dem Steinsockel
  * item/blueprint – Bauplan: Pergament mit blauer Tuschezeichnung eines Fachwerkhauses, unten eingerollt
  * gui/town – Stadtfenster (320x210): Eichenrahmen mit Goldbeschlägen, blaues Band für den Namen, Pergament
  * gui/trade – Handelsfenster (300x186), gleicher Stil

Wird von tools/generate_textures.py aufgerufen (build(put)). Alle Zufallsmuster haben feste Startwerte.
"""
from __future__ import annotations

import math
import random

from PIL import Image

import item_style as st
from pixelart import CLEAR, hexc, mix

# =============================================================================
# Farben
# =============================================================================

BLUE = st.ramp("#2a4796", dark=0.16, light=0.78, cool=275, warm=230, hue_dark=8, hue_light=6, chroma_light=0.8)
GOLD = st.ramp("#e2a623", dark=0.24, light=0.97, cool=30, warm=85, hue_dark=14, hue_light=12, chroma_light=0.5)
RED = st.ramp("#b0282a", dark=0.2, light=0.85, cool=320, warm=40)
WOOD = st.ramp("#6b4426", dark=0.18, light=0.82, cool=20, warm=70, hue_dark=10)
STONE = st.ramp("#8f8f8f", dark=0.22, light=0.92, cool=250, warm=80, chroma_dark=1.0)
PAPER = st.ramp("#dcc69a", dark=0.35, light=0.97, cool=40, warm=90, hue_dark=10, chroma_light=0.6)
INK = st.ramp("#2f5aa8", dark=0.2, light=0.8, cool=270, warm=220)


# =============================================================================
# Stadtbanner
# =============================================================================

CLOTH_W, CLOTH_H = 12, 22


def banner_cloth() -> Image.Image:
    """Das Tuch (12x22): blau mit Goldborte, goldene Krone, ein Kreuz darunter, unten ein Schwalbenschwanz."""
    img = Image.new("RGBA", (CLOTH_W, CLOTH_H), CLEAR)
    px = img.load()
    rnd = random.Random(4117)

    def notch(x: int, y: int) -> bool:
        """Liegt (x, y) im Ausschnitt des Schwalbenschwanzes?"""
        if y < 16:
            return False
        hw = y - 16  # 0..5
        return 5.5 - hw <= x + 0.5 <= 6.5 + hw and hw > 0 or (hw > 0 and abs(x + 0.5 - 6.0) < hw)

    # Grund: Gewebe mit zwei weichen Falten, Licht von links oben
    for y in range(CLOTH_H):
        for x in range(CLOTH_W):
            if notch(x, y):
                continue
            lvl = 3
            if x in (3, 8):
                lvl = 2  # Falte
            elif x in (2, 7):
                lvl = 4  # Glanz vor der Falte
            if y >= 18:
                lvl -= 1
            if rnd.random() < 0.08:
                lvl -= 1
            px[x, y] = BLUE[st.clamp(lvl)]
    # Borte: oben ein umgeschlagener Saum (über der Querstange), seitlich Goldkanten, am Ausschnitt eine Goldlinie
    for x in range(CLOTH_W):
        px[x, 0] = GOLD[4] if x % 3 else GOLD[5]
        px[x, 1] = GOLD[2]
        px[x, 2] = BLUE[1]
    for y in range(1, CLOTH_H):
        for x in (0, CLOTH_W - 1):
            if not notch(x, y):
                px[x, y] = GOLD[4] if x == 0 else GOLD[2]
    for y in range(16, CLOTH_H):
        for x in range(CLOTH_W):
            if notch(x, y):
                continue
            if notch(x + 1, y) or notch(x - 1, y) or notch(x, y + 1) or y == CLOTH_H - 1:
                px[x, y] = GOLD[3] if x < 6 else GOLD[2]
    # Krone (Mitte, Zeilen 5..10)
    crown = [
        "y..yy..y",
        "G..GG..G",
        "GG.GG.GG",
        "GGGGGGGG",
        "GrGGbGrG",
        "GGGGGGGG",
        "gggggggg",
    ]
    codes = {"y": GOLD[6], "G": GOLD[4], "g": GOLD[2], "r": RED[4], "b": hexc("#7fb2ff")}
    for dy, row in enumerate(crown):
        for dx, ch in enumerate(row):
            if ch != ".":
                c = codes[ch]
                if ch == "G" and dx >= 5:
                    c = GOLD[3]
                px[2 + dx, 5 + dy] = c
    # kleines Kreuz unter der Krone
    for (x, y, lvl) in ((5, 12, 4), (6, 12, 3), (5, 13, 4), (6, 13, 3), (4, 13, 4), (7, 13, 3), (5, 14, 3), (6, 14, 2)):
        px[x, y] = GOLD[lvl]
    return img


def banner_textures() -> tuple[Image.Image, Image.Image]:
    """Oberer Block: Tuchzeilen 0..11 bei v 4..15; unterer Block: Zeilen 12..21 bei v 0..9 (jeweils u 2..13)."""
    cloth = banner_cloth()
    upper = Image.new("RGBA", (16, 16), CLEAR)
    lower = Image.new("RGBA", (16, 16), CLEAR)
    upper.alpha_composite(cloth.crop((0, 0, CLOTH_W, 12)), (2, 4))
    lower.alpha_composite(cloth.crop((0, 12, CLOTH_W, CLOTH_H)), (2, 0))
    return upper, lower


def _town_banner_sprite() -> Image.Image:
    """Gegenstand: das Banner am Mast mit Querstange, Goldknauf und Steinsockel."""
    s = st.Spr()
    # Mast
    for y in range(1, 15):
        s.set(7, y, ("D", 4))
        s.set(8, y, ("D", 2))
    # Knauf
    s.set(7, 0, ("G", 6))
    s.set(8, 0, ("G", 4))
    # Querstange mit Goldenden
    for x in range(2, 14):
        s.set(x, 2, ("D", 4 if x < 8 else 3))
    s.set(1, 2, ("G", 5))
    s.set(14, 2, ("G", 3))
    # Tuch (vor dem Mast): x 3..12, y 3..12, mit Schwalbenschwanz
    for y in range(3, 13):
        for x in range(3, 13):
            if y >= 10:
                hw = y - 9
                if abs(x + 0.5 - 8.0) < hw:
                    continue
            lvl = 3
            if x in (5, 10):
                lvl = 2
            elif x in (4, 9):
                lvl = 4
            s.set(x, y, ("B", lvl))
        s.set(3, y, ("A", 5))
        s.set(12, y, ("A", 3))
    for x in range(3, 13):
        s.set(x, 3, ("A", 5 if x % 2 else 4))
    # Krone
    crown = ["G.GG.G", "GGGGGG", "GrGGrG", "gggggg"]
    for dy, row in enumerate(crown):
        for dx, ch in enumerate(row):
            if ch == "G":
                s.set(5 + dx, 5 + dy, ("A", 5 if dx < 3 else 4))
            elif ch == "g":
                s.set(5 + dx, 5 + dy, ("A", 3))
            elif ch == "r":
                s.set(5 + dx, 5 + dy, ("E", 4))
    # Goldkante am Ausschnitt
    for y in range(10, 13):
        hw = y - 9
        for x in range(3, 13):
            if s.get(x, y) is not None and s.get(x, y)[0] == "B":
                if abs(x + 1.5 - 8.0) < hw or abs(x - 0.5 - 8.0) < hw:
                    s.set(x, y, ("A", 3))
    # Sockel
    for x in range(5, 11):
        s.set(x, 14, ("F", 5 if x < 8 else 4))
    for x in range(4, 12):
        s.set(x, 15, ("F", 3 if x < 8 else 2))
    return s.outline(prefer=("B", "D", "F")).image({"A": GOLD, "B": BLUE, "D": WOOD, "E": RED, "F": STONE, "G": GOLD})


def blueprint_item() -> Image.Image:
    """Bauplan: Pergamentbogen mit blauer Tuschezeichnung (Fachwerkhaus), unten aufgerollt, mit rotem Band."""
    s = st.Spr()
    # Bogen x 2..13, y 1..11
    for y in range(1, 12):
        for x in range(2, 14):
            lvl = 4 if (x + y) % 7 else 3
            if x == 2 or y == 1:
                lvl = 5
            if x == 13:
                lvl = 3
            s.set(x, y, ("A", lvl))
    # Rolle unten (y 11..14)
    for x in range(1, 15):
        s.set(x, 11, ("A", 5))
        s.set(x, 12, ("A", 4))
        s.set(x, 13, ("A", 2))
    s.set(0, 12, ("A", 3))
    s.set(15, 12, ("A", 2))
    # rotes Band um die Rolle
    for y in (11, 12, 13):
        s.set(9, y, ("E", 4 if y == 11 else 3))
        s.set(10, y, ("E", 3 if y == 11 else 2))
    s.set(9, 14, ("E", 3))
    s.set(11, 14, ("E", 2))
    # Tuschezeichnung: Giebeldach, Wände, Fachwerk, Tür, Fenster
    ink = [
        "......B.....",
        ".....B.B....",
        "....B...B...",
        "...B.....B..",
        "..BBBBBBBBB.",
        "..B...B...B.",
        "..B.B.B.BBB.",
        "..BB..B.B.B.",
        "..BBBBBBBBB.",
    ]
    for dy, row in enumerate(ink):
        for dx, ch in enumerate(row):
            if ch == "B":
                s.set(1 + dx + 1, 1 + dy + 1, ("B", 3))
    return s.outline(prefer=("A", "E")).image({"A": PAPER, "B": INK, "E": RED})


# =============================================================================
# Fenster-Hintergründe
# =============================================================================

PARCH = {
    "base": hexc("#e6d3a8"), "light": hexc("#f2e4c0"), "dark": hexc("#d3bc8a"), "edge": hexc("#b89c69"),
    "deep": hexc("#9a7e50"), "fiber": hexc("#dcc69a"),
}
FRAME = [hexc("#1c120a"), hexc("#2e1d10"), hexc("#432b17"), hexc("#5a3a20"), hexc("#714a2a"), hexc("#8a5d36")]
RIBBON = [hexc("#0d1533"), hexc("#16244f"), hexc("#1f3373"), hexc("#2a4796"), hexc("#3a5fb8")]
TRIM = [hexc("#5a3c0c"), hexc("#8c5f16"), hexc("#c8932c"), hexc("#e8bd52"), hexc("#f7dc80")]


def _noise(seed: int, w: int, h: int, scale: float) -> list[list[float]]:
    """Weiches Rauschen (Wertrauschen, bilinear) 0..1."""
    rnd = random.Random(seed)
    gw, gh = int(w / scale) + 2, int(h / scale) + 2
    grid = [[rnd.random() for _ in range(gw)] for _ in range(gh)]
    out = [[0.0] * w for _ in range(h)]
    for y in range(h):
        for x in range(w):
            fx, fy = x / scale, y / scale
            ix, iy = int(fx), int(fy)
            tx, ty = fx - ix, fy - iy
            tx, ty = tx * tx * (3 - 2 * tx), ty * ty * (3 - 2 * ty)
            a = grid[iy][ix] * (1 - tx) + grid[iy][ix + 1] * tx
            b = grid[iy + 1][ix] * (1 - tx) + grid[iy + 1][ix + 1] * tx
            out[y][x] = a * (1 - ty) + b * ty
    return out


def parchment(img: Image.Image, x0: int, y0: int, w: int, h: int, seed: int) -> None:
    """Pergament: warme Grundfarbe, wolkige Flecken, feine Fasern, zu den Rändern hin dunkler."""
    px = img.load()
    big = _noise(seed, w, h, 22.0)
    small = _noise(seed + 1, w, h, 5.0)
    rnd = random.Random(seed + 2)
    for y in range(h):
        for x in range(w):
            n = big[y][x] * 0.65 + small[y][x] * 0.35
            c = mix(PARCH["dark"], PARCH["light"], min(1.0, max(0.0, (n - 0.2) * 1.25)))
            # Vignette: an den Rändern dunkler
            d = min(x, y, w - 1 - x, h - 1 - y)
            if d < 10:
                c = mix(c, PARCH["edge"], (10 - d) / 10 * 0.55)
            px[x0 + x, y0 + y] = c
    # Fasern: kurze, helle und dunkle Striche
    for _ in range(w * h // 90):
        x, y = rnd.randrange(w - 4), rnd.randrange(h)
        length = rnd.randint(2, 4)
        tone = PARCH["fiber"] if rnd.random() < 0.6 else PARCH["light"]
        for i in range(length):
            old = px[x0 + x + i, y0 + y]
            px[x0 + x + i, y0 + y] = mix(old, tone, 0.45)
    # Innenkante: dunkle Linie oben/links (der Rahmen wirft Schatten)
    for x in range(w):
        px[x0 + x, y0] = mix(px[x0 + x, y0], PARCH["deep"], 0.7)
        px[x0 + x, y0 + 1] = mix(px[x0 + x, y0 + 1], PARCH["deep"], 0.3)
    for y in range(h):
        px[x0, y0 + y] = mix(px[x0, y0 + y], PARCH["deep"], 0.7)
        px[x0 + 1, y0 + y] = mix(px[x0 + 1, y0 + y], PARCH["deep"], 0.3)


def wood_frame(img: Image.Image, w: int, h: int, t: int, seed: int) -> None:
    """Eichenrahmen mit Maserung (waagerecht oben/unten, senkrecht seitlich), Fase innen, runde Außenecken."""
    px = img.load()
    rnd = random.Random(seed)
    grain_h = [rnd.random() for _ in range(w + h)]
    for y in range(h):
        for x in range(w):
            d = min(x, y, w - 1 - x, h - 1 - y)
            if d >= t:
                continue
            horizontal = (y < t or y >= h - t) and t <= x < w - t or (y < t or y >= h - t) and min(x, w - 1 - x) >= min(y, h - 1 - y)
            g = grain_h[(y * 3 + x // 9) % len(grain_h)] if horizontal else grain_h[(x * 3 + y // 9) % len(grain_h)]
            lvl = 3 + (1 if g > 0.75 else -1 if g < 0.2 else 0)
            # Fase: außen hell oben/links, innen dunkel
            if d == 0:
                lvl = 0
            elif d == 1:
                lvl = 5 if (x <= y and x < w // 2) or (y < x and y < h // 2) else 2
            elif d == t - 1:
                lvl = 1
            elif d == t - 2:
                lvl = min(lvl, 2)
            px[x, y] = FRAME[max(0, min(5, lvl))]
    # runde Außenecken
    for (cx, cy) in ((0, 0), (w - 1, 0), (0, h - 1), (w - 1, h - 1)):
        px[cx, cy] = CLEAR
    for (cx, cy, dx, dy) in ((1, 0, 0, 1), (w - 2, 0, w - 1, 1), (1, h - 1, 0, h - 2), (w - 2, h - 1, w - 1, h - 2)):
        px[cx, cy] = FRAME[0]
        px[dx, dy] = FRAME[0]


def gold_corner(img: Image.Image, x0: int, y0: int, flip_x: bool, flip_y: bool) -> None:
    """Goldbeschlag an einer Ecke (9x9), mit Niete."""
    art = [
        "yyyyyyy..",
        "yGGGGGgo.",
        "yGggggo..",
        "yGgo.....",
        "yGgo.....",
        "yGo......",
        "ygo......",
        ".o.......",
        ".........",
    ]
    codes = {"y": TRIM[4], "G": TRIM[3], "g": TRIM[2], "o": TRIM[0]}
    px = img.load()
    for y, row in enumerate(art):
        for x, ch in enumerate(row):
            if ch == ".":
                continue
            X = x0 + (8 - x if flip_x else x)
            Y = y0 + (8 - y if flip_y else y)
            px[X, Y] = codes[ch]
    # Niete
    nx = x0 + (6 if flip_x else 2)
    ny = y0 + (6 if flip_y else 2)
    px[nx, ny] = TRIM[4]


def ribbon(img: Image.Image, x0: int, y0: int, w: int, h: int) -> None:
    """Blaues Band mit Goldkante oben und unten und feinem Stoffmuster."""
    px = img.load()
    rnd = random.Random(77)
    for y in range(h):
        for x in range(w):
            lvl = 2 if y < h // 2 else 1
            if (x + y * 2) % 5 == 0:
                lvl -= 1
            if rnd.random() < 0.05:
                lvl += 1
            c = RIBBON[max(0, min(4, lvl))]
            if y == 1:
                c = RIBBON[3]
            px[x0 + x, y0 + y] = c
    for x in range(w):
        px[x0 + x, y0] = TRIM[3] if x % 4 else TRIM[4]
        px[x0 + x, y0 + h - 1] = TRIM[2] if x % 4 else TRIM[1]
        px[x0 + x, y0 + h] = mix(PARCH["deep"], FRAME[1], 0.5)


def town_gui() -> Image.Image:
    w, h = 320, 210
    img = Image.new("RGBA", (w, h), CLEAR)
    t = 7
    parchment(img, t, t, w - 2 * t, h - 2 * t, seed=811)
    ribbon(img, t, t, w - 2 * t, 22)
    wood_frame(img, w, h, t, seed=31)
    gold_corner(img, 1, 1, False, False)
    gold_corner(img, w - 10, 1, True, False)
    gold_corner(img, 1, h - 10, False, True)
    gold_corner(img, w - 10, h - 10, True, True)
    return img


def trade_gui() -> Image.Image:
    w, h = 300, 186
    img = Image.new("RGBA", (w, h), CLEAR)
    t = 7
    parchment(img, t, t, w - 2 * t, h - 2 * t, seed=977)
    ribbon(img, t, t, w - 2 * t, 22)
    wood_frame(img, w, h, t, seed=53)
    gold_corner(img, 1, 1, False, False)
    gold_corner(img, w - 10, 1, True, False)
    gold_corner(img, 1, h - 10, False, True)
    gold_corner(img, w - 10, h - 10, True, True)
    return img


def build(put) -> None:
    upper, lower = banner_textures()
    put("block/town_banner_cloth_upper", upper)
    put("block/town_banner_cloth_lower", lower)
    put("item/town_banner", _town_banner_sprite())
    put("item/blueprint", blueprint_item())
    put("gui/town", town_gui())
    put("gui/trade", trade_gui())


if __name__ == "__main__":
    import sys

    out: dict[str, Image.Image] = {}
    build(lambda key, img: out.__setitem__(key, img))
    target = sys.argv[1] if len(sys.argv) > 1 else "town_textures.png"
    sheet = Image.new("RGBA", (700, 520), (40, 44, 52, 255))
    sheet.alpha_composite(out["gui/town"].resize((640, 420), Image.NEAREST), (0, 0))
    x = 0
    for key in ("block/town_banner_cloth_upper", "block/town_banner_cloth_lower", "item/town_banner", "item/blueprint"):
        sheet.alpha_composite(out[key].resize((96, 96), Image.NEAREST), (x, 422))
        x += 100
    sheet.save(target)
    print("Vorschau:", target)
