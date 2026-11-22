#!/usr/bin/env python3
"""
Wegkompass und Wegsteine (seit 1.12; eigene Pixelkunst, alles hier gezeichnet):

  * item/waystone_compass_00 .. _31   der Wegkompass: achteckiges Bronzegehäuse mit Öse, schieferblaues Zifferblatt,
                                      eine Nadel mit leuchtend türkiser Spitze in 32 Stellungen (wie beim Kompass von
                                      Minecraft: _16 zeigt nach oben, _24 nach rechts, _00 nach unten, _08 nach links)
  * block/waystone_*                  der Wegstein: Steinsockel, Schaft mit eingemeißelter Rune (leuchtet), Bronzeband,
                                      Spitze mit Kristallfassung; darüber schwebt ein Kristall (waystone_crystal)
  * gui/waystone_compass              das Kompassfenster (512 × 256): Rahmen mit Pergament und türkisem Band, das große
                                      Zifferblatt mit Bronzelünette, ein Edelstein (grau, wird im Spiel gefärbt), sein
                                      Schein und die Nabe der Nadel

    python3 tools/waystone_textures.py            # schreibt in die Ressourcen der Mod
    python3 tools/waystone_textures.py VORSCHAU   # schreibt nur eine vergrößerte Übersicht (PNG)

Wird auch von tools/generate_textures.py aufgerufen (build(put)). Alle Zufallsmuster haben feste Startwerte.
"""
from __future__ import annotations

import math
import os
import random
import sys
from typing import Callable

from PIL import Image

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import pixelart as pa  # noqa: E402
import town_textures as tt  # noqa: E402
from pixelart import CLEAR, Color, hexc, mix  # noqa: E402

N = 16

STONE = [hexc(c) for c in ("#202429", "#2e333a", "#3f454c", "#525960", "#686f76", "#828990", "#9fa6ab", "#bec4c8")]
BRONZE = [hexc(c) for c in ("#3a210e", "#5e3816", "#86521f", "#ad6f2b", "#cf9140", "#ecb862", "#fbdc96")]
CYAN = [hexc(c) for c in ("#0b3f4d", "#137085", "#22a3b8", "#4fd2e0", "#9af0f5", "#e2fdff")]


# =============================================================================
# Wegkompass (Gegenstand, 32 Bilder)
# =============================================================================

CX, CY = 7.5, 8.5            # Mitte des Zifferblatts (Pixelmitte von x=7, y=8)
CASE_R = 6.45                # Achteck außen
DIAL_R = 4.3                 # Zifferblatt (rund)
DIAL = [hexc("#121c21"), hexc("#1a2a30"), hexc("#243940")]
TICK = hexc("#5f858c")
OUTLINE = hexc("#26140a")
TIP = [CYAN[5], CYAN[4], CYAN[3], CYAN[2]]
TAIL = [hexc("#c9a46a"), hexc("#8a6234")]


def _octagon(dx: float, dy: float) -> float:
    """Abstand im regelmäßigen Achteck (Seiten waagerecht/senkrecht)."""
    return max(abs(dx), abs(dy), (abs(dx) + abs(dy)) * 0.7071)


def _compass_base() -> Image.Image:
    img = pa.new()
    px = img.load()
    for y in range(N):
        for x in range(N):
            dx, dy = x + 0.5 - CX, y + 0.5 - CY
            d = math.hypot(dx, dy)
            o = _octagon(dx, dy)
            if d < DIAL_R:
                px[x, y] = DIAL[2] if d < 1.9 else DIAL[1] if d < 3.3 else DIAL[0]
            elif o < CASE_R:
                # Facette des Achtecks: ihre Richtung bestimmt das Licht (von oben links)
                ax, ay, dg = abs(dx), abs(dy), (abs(dx) + abs(dy)) * 0.7071
                if dg >= ax and dg >= ay:
                    nx, ny = math.copysign(0.7071, dx), math.copysign(0.7071, dy)
                elif ax >= ay:
                    nx, ny = math.copysign(1.0, dx), 0.0
                else:
                    nx, ny = 0.0, math.copysign(1.0, dy)
                light = -(nx * 0.7071 + ny * 0.7071)
                if d < DIAL_R + 0.8:
                    light = -light * 0.8          # Innenkante: Vertiefung
                    idx = 3 + round(light * 1.6)
                else:
                    idx = 4 + round(light * 1.7)
                px[x, y] = BRONZE[max(1, min(6, idx))]
    img = pa.outline(img, OUTLINE)
    px = img.load()
    # Öse oben: ein kleiner Goldring
    for (x, y, c) in ((7, 0, BRONZE[6]), (8, 0, BRONZE[5]), (6, 1, BRONZE[5]), (9, 1, BRONZE[3])):
        px[x, y] = c
    for (x, y) in ((6, 0), (9, 0), (5, 1), (10, 1)):
        if px[x, y][3] == 0:
            px[x, y] = OUTLINE
    if px[7, 1][3] == 0:
        px[7, 1] = OUTLINE
        px[8, 1] = OUTLINE
    # vier Himmelsmarken
    for (x, y) in ((7, 5), (11, 8), (7, 12), (3, 8)):
        px[x, y] = TICK
    # zwei türkise Nieten auf den schrägen Facetten (die Wegsteine)
    for (x, y) in ((3, 4), (11, 12)):
        px[x, y] = CYAN[3]
    for (x, y) in ((11, 4), (3, 12)):
        px[x, y] = BRONZE[6] if y < 8 else BRONZE[2]
    return img


def _needle(img: Image.Image, frame: int) -> Image.Image:
    out = img.copy()
    px = out.load()
    a = (frame - 16) / 32.0 * math.tau
    sx, sy = math.sin(a), -math.cos(a)
    tail: dict[tuple[int, int], Color] = {}
    tip: dict[tuple[int, int], Color] = {}
    steps = 48
    for i in range(steps + 1):
        t = -2.6 + (3.6 + 2.6) * i / steps
        x = math.floor(CX + sx * t)
        y = math.floor(CY + sy * t)
        if t < -0.4:
            tail[(x, y)] = TAIL[0] if t > -1.6 else TAIL[1]
        elif t > 0.4:
            k = 3 if t < 1.3 else 2 if t < 2.2 else 1 if t < 3.0 else 0
            old = tip.get((x, y))
            if old is None or TIP.index(old) > k:
                tip[(x, y)] = TIP[k]
    for pos, c in tail.items():
        if pos not in tip:
            px[pos] = c
    for pos, c in tip.items():
        px[pos] = c
    px[7, 8] = BRONZE[5]
    return out


def compass_frames() -> list[Image.Image]:
    base = _compass_base()
    return [_needle(base, i) for i in range(32)]


# =============================================================================
# Wegstein (Block)
# =============================================================================

def _stone_noise(seed: int) -> list[list[float]]:
    return pa.fractal_noise(seed)


def _plain_stone(seed: int, lo: int = 3, hi: int = 4) -> Image.Image:
    """Gleichmäßiger, leicht fleckiger Stein (zwei Töne, ein paar dunkle Körner)."""
    img = pa.new()
    px = img.load()
    noise = _stone_noise(seed)
    rnd = random.Random(seed)
    for y in range(N):
        for x in range(N):
            v = noise[y][x]
            px[x, y] = STONE[hi] if v > 0.55 else STONE[lo]
            if rnd.random() < 0.06:
                px[x, y] = STONE[lo - 1]
    return img


def base_side() -> Image.Image:
    """Seiten von Sockel (Zeilen 14–15) und Stufe (Zeile 13); darüber Mauerwerk (für die Bruchstücke)."""
    img = pa.new()
    px = img.load()
    noise = _stone_noise(5101)
    # Mauerwerk: Quader 8 × 4, versetzt
    for y in range(N):
        for x in range(N):
            row = y // 4
            offset = 4 if row % 2 else 0
            v = noise[y][x]
            c = STONE[4] if v > 0.5 else STONE[3]
            if y % 4 == 0:
                c = STONE[5] if v > 0.4 else STONE[4]
            if y % 4 == 3 or (x + offset) % 8 == 7:
                c = STONE[1]
            px[x, y] = c
    # Stufe
    for x in range(N):
        px[x, 13] = STONE[6] if x % 5 else STONE[5]
    # Sockel: Oberkante hell, Fugen bei x=5 und x=10
    for x in range(N):
        px[x, 14] = STONE[5] if x not in (5, 10) else STONE[2]
        px[x, 15] = (STONE[3] if noise[15][x] > 0.45 else STONE[2]) if x not in (5, 10) else STONE[1]
    return img


def base_top() -> Image.Image:
    """Oberseiten von Sockel (Ring 1/14) und Stufe (Ring 2/13); innen schlichter Stein (auch für die Unterseite)."""
    img = _plain_stone(5202)
    px = img.load()
    for y in range(N):
        for x in range(N):
            if 1 <= x <= 14 and 1 <= y <= 14 and (x in (1, 14) or y in (1, 14)):
                px[x, y] = STONE[5] if (x == 1 or y == 1) else STONE[4]
            if 2 <= x <= 13 and 2 <= y <= 13 and (x in (2, 13) or y in (2, 13)):
                px[x, y] = STONE[6] if (x == 2 or y == 2) else STONE[5]
    return img


# Die Rune auf dem Schaft (x 4..11, Zeilen 5..11): eine Raute mit Punkt über einem Stab – ein Wegzeichen
RUNE = [
    "...XX...",
    "..X..X..",
    ".X.XX.X.",
    "..X..X..",
    "...XX...",
    "...XX...",
    "..XXXX..",
]
RUNE_X, RUNE_Y = 4, 5


def _rune_cells() -> list[tuple[int, int]]:
    cells = []
    for dy, row in enumerate(RUNE):
        for dx, ch in enumerate(row):
            if ch == "X":
                cells.append((RUNE_X + dx, RUNE_Y + dy))
    return cells


def shaft() -> Image.Image:
    """Schaft (x 3..12, Zeilen 4..12): glatter Stein mit Fase, die Rune eingemeißelt."""
    img = _plain_stone(5303, 3, 4)
    px = img.load()
    noise = _stone_noise(5304)
    for y in range(4, 13):
        for x in range(3, 13):
            v = noise[y][x]
            c = STONE[5] if v > 0.6 else STONE[4]
            if x == 3:
                c = STONE[6]
            elif x == 12:
                c = STONE[3]
            if y == 4:
                c = STONE[2]               # Schatten unter dem Bronzeband
            elif y == 12 and 3 < x < 12:
                c = STONE[5]
            px[x, y] = c
    for (x, y) in _rune_cells():
        px[x, y] = STONE[1]
    # Verwitterung: ein paar dunkle Körner
    for (x, y) in ((5, 11), (10, 6), (9, 12), (4, 9)):
        if (x, y) not in _rune_cells():
            px[x, y] = STONE[3]
    return img


def shaft_glow() -> Image.Image:
    img = pa.new()
    px = img.load()
    for (x, y) in _rune_cells():
        # oben heller (die Raute), unten etwas dunkler
        px[x, y] = CYAN[5] if (x, y) in ((7, 7), (8, 7)) else CYAN[4] if y <= 9 else CYAN[3]
    return img


def bronze() -> Image.Image:
    """Bronzeband und Krallen der Fassung: warme Bronze mit Nieten."""
    img = pa.new()
    px = img.load()
    for y in range(N):
        for x in range(N):
            c = BRONZE[4]
            if x % 3 == 1:
                c = BRONZE[6]
            elif x % 3 == 2:
                c = BRONZE[3]
            if y % 4 == 0:
                c = BRONZE[5] if x % 3 != 2 else BRONZE[4]
            px[x, y] = c
    return img


def cap_side() -> Image.Image:
    """Kappe (Zeile 2, x 3..12) und Spitze (Zeile 1, x 4..11); sonst heller Stein."""
    img = _plain_stone(5505, 4, 5)
    px = img.load()
    for x in range(N):
        px[x, 2] = STONE[6] if x not in (6, 9) else STONE[4]
        px[x, 1] = STONE[7] if x < 8 else STONE[6]
    return img


def cap_top() -> Image.Image:
    """Oberseiten von Kappe (Ring 3/12) und Spitze (4..11) mit der Kristallfassung in der Mitte."""
    img = _plain_stone(5606, 4, 5)
    px = img.load()
    for y in range(3, 13):
        for x in range(3, 13):
            ring = min(x - 3, y - 3, 12 - x, 12 - y)
            if ring == 0:
                px[x, y] = STONE[6] if (x == 3 or y == 3) else STONE[5]
            elif ring == 1:
                px[x, y] = STONE[7] if (x == 4 or y == 4) else STONE[6]
            elif ring == 2:
                px[x, y] = STONE[5]
            else:
                px[x, y] = STONE[1]          # die Mulde der Fassung
    return img


def cap_glow() -> Image.Image:
    """In der Mulde (6..9) glimmt der Kristallgrund."""
    img = pa.new()
    px = img.load()
    for y in range(6, 10):
        for x in range(6, 10):
            edge = x in (6, 9) or y in (6, 9)
            px[x, y] = CYAN[2] if edge else CYAN[4]
    px[7, 7] = CYAN[5]
    return img


def crystal() -> Image.Image:
    """Der schwebende Kristall (für alle acht Flächen): türkis, zur Spitze hin heller, mit Lichtkante."""
    img = pa.new()
    px = img.load()
    for y in range(N):
        for x in range(N):
            # v = 0 ist die Spitze, v = 15 die Mitte des Kristalls
            t = y / 15.0
            c = mix(CYAN[4], CYAN[1], t)
            if abs(x - 7.5) < 1.0 + t * 1.5:
                c = mix(c, CYAN[5], 0.55 * (1.0 - t))       # Lichtkante in der Mitte der Fläche
            if x in (0, 15):
                c = mix(c, CYAN[0], 0.5)
            px[x, y] = c
    return img


# =============================================================================
# Kompassfenster
# =============================================================================

GUI_W, GUI_H = 304, 210
DIAL_SIZE = 97              # ungerade: die Mitte liegt auf einem Pixel (48, 48)
DIAL_U = 308
GEM_U, GEM_V, GEM = 408, 0, 9
HALO_U, HALO_V, HALO = 420, 0, 19
HUB_U, HUB_V, HUB = 408, 12, 7

TEAL = [hexc(c) for c in ("#081d21", "#0e2e33", "#144148", "#1b5860", "#27717b")]


def teal_ribbon(img: Image.Image, x0: int, y0: int, w: int, h: int) -> None:
    """Das Band oben in Türkis statt Königsblau – mit derselben Goldkante wie die anderen Fenster."""
    px = img.load()
    rnd = random.Random(1212)
    for y in range(h):
        for x in range(w):
            lvl = 2 if y < h // 2 else 1
            if (x + y * 2) % 5 == 0:
                lvl -= 1
            if rnd.random() < 0.05:
                lvl += 1
            c = TEAL[max(0, min(4, lvl))]
            if y == 1:
                c = TEAL[3]
            px[x0 + x, y0 + y] = c
    for x in range(w):
        px[x0 + x, y0] = tt.TRIM[3] if x % 4 else tt.TRIM[4]
        px[x0 + x, y0 + h - 1] = tt.TRIM[2] if x % 4 else tt.TRIM[1]
        px[x0 + x, y0 + h] = mix(tt.PARCH["deep"], tt.FRAME[1], 0.5)


def window() -> Image.Image:
    img = Image.new("RGBA", (GUI_W, GUI_H), CLEAR)
    t = 7
    tt.parchment(img, t, t, GUI_W - 2 * t, GUI_H - 2 * t, seed=1201)
    teal_ribbon(img, t, t, GUI_W - 2 * t, 22)
    tt.wood_frame(img, GUI_W, GUI_H, t, seed=1203)
    tt.gold_corner(img, 1, 1, False, False)
    tt.gold_corner(img, GUI_W - 10, 1, True, False)
    tt.gold_corner(img, 1, GUI_H - 10, False, True)
    tt.gold_corner(img, GUI_W - 10, GUI_H - 10, True, True)
    return img


def dial() -> Image.Image:
    """Das große Zifferblatt: Bronzelünette (Achteck) mit acht türkisen Steinen, schieferblaues Blatt mit Teilung."""
    s = DIAL_SIZE
    c = (s - 1) / 2.0 + 0.5        # 48.5: Mitte des Pixels (48, 48)
    img = Image.new("RGBA", (s, s), CLEAR)
    px = img.load()
    outer, inner = 47.4, 38.5
    rnd = random.Random(1300)
    face = [hexc("#0f181c"), hexc("#152126"), hexc("#1b2b31"), hexc("#22363d"), hexc("#2a4148")]
    for y in range(s):
        for x in range(s):
            dx, dy = x + 0.5 - c, y + 0.5 - c
            d = math.hypot(dx, dy)
            o = _octagon(dx, dy)
            if d < inner:
                # Blatt: zur Mitte heller, feine Körnung
                k = 1.0 - d / inner
                idx = 1 + k * 3.2 + (rnd.random() - 0.5) * 0.5
                px[x, y] = face[max(0, min(4, int(idx)))]
                if inner - 1.2 < d:
                    px[x, y] = face[0]       # Schatten an der Lünette
            elif o < outer:
                ax, ay, dg = abs(dx), abs(dy), (abs(dx) + abs(dy)) * 0.7071
                if dg >= ax and dg >= ay:
                    nx, ny = math.copysign(0.7071, dx), math.copysign(0.7071, dy)
                elif ax >= ay:
                    nx, ny = math.copysign(1.0, dx), 0.0
                else:
                    nx, ny = 0.0, math.copysign(1.0, dy)
                light = -(nx * 0.7071 + ny * 0.7071)
                if d < inner + 2.2:
                    idx = 3 - round(light * 1.4)         # innere Kante: Vertiefung, umgekehrtes Licht
                elif o > outer - 1.6:
                    idx = 4 + round(light * 2.0)         # äußere Fase
                else:
                    idx = 4 + round(light * 1.2)
                    if (x * 7 + y * 13) % 11 == 0:
                        idx -= 1                          # gehämmerte Bronze
                px[x, y] = BRONZE[max(1, min(6, idx))]
    # dunkler Rand außen
    out = pa.outline(img, OUTLINE)
    px = out.load()
    # Teilung: 32 Striche, alle 45° lang und hell
    for i in range(32):
        a = i / 32.0 * math.tau
        major = i % 4 == 0
        r0 = inner - (6.5 if major else 3.5)
        for k in range(12):
            r = r0 + (inner - 1.6 - r0) * k / 11
            x = math.floor(c + math.sin(a) * r)
            y = math.floor(c - math.cos(a) * r)
            px[x, y] = hexc("#7fa6ab") if major else hexc("#3f5a61")
    # innerer Kreis (graviert)
    for i in range(240):
        a = i / 240.0 * math.tau
        x = math.floor(c + math.sin(a) * 19.5)
        y = math.floor(c - math.cos(a) * 19.5)
        px[x, y] = hexc("#2f4a52")
    # eingeschlagene Punkte in der Lünette (auf den Ecken des Achtecks) – die Steine der Wegsteine setzt das Spiel
    for i in range(8):
        a = (i + 0.5) / 8.0 * math.tau
        r = (outer + inner) / 2.0 + 0.6
        gx = math.floor(c + math.sin(a) * r)
        gy = math.floor(c - math.cos(a) * r)
        px[gx, gy] = BRONZE[2]
        px[gx - 1, gy - 1] = BRONZE[6]
    # Steuerstrich oben: eine kleine Bronzezunge zeigt „vorne“
    top = math.floor(c - inner) + 1
    for k, half in enumerate((3, 2, 1, 0)):
        for ox in range(-half, half + 1):
            px[48 + ox, top + k] = BRONZE[6] if ox <= 0 else BRONZE[4]
        px[48 - half - 1, top + k] = OUTLINE
        px[48 + half + 1, top + k] = OUTLINE
    px[48, top + 4] = OUTLINE
    return out


def gem() -> Image.Image:
    """Ein Edelstein (9 × 9) in Grautönen – im Spiel in der Farbe seines Wegsteins gefärbt."""
    art = [
        "....o....",
        "...oWo...",
        "..oWLMo..",
        ".oWLLMMo.",
        "oLLLMMMDo",
        ".oMMMMDo.",
        "..oMMDo..",
        "...oDo...",
        "....o....",
    ]
    codes = {"o": (46, 46, 46, 255), "W": (255, 255, 255, 255), "L": (226, 226, 226, 255), "M": (176, 176, 176, 255),
             "D": (118, 118, 118, 255)}
    img = Image.new("RGBA", (GEM, GEM), CLEAR)
    px = img.load()
    for y, row in enumerate(art):
        for x, ch in enumerate(row):
            if ch != ".":
                px[x, y] = codes[ch]
    return img


def halo() -> Image.Image:
    """Weicher Schein (19 × 19) hinter einem Edelstein – weiß, im Spiel gefärbt."""
    img = Image.new("RGBA", (HALO, HALO), CLEAR)
    px = img.load()
    c = (HALO - 1) / 2.0
    for y in range(HALO):
        for x in range(HALO):
            d = math.hypot(x - c, y - c) / (HALO / 2.0)
            if d < 1.0:
                a = (1.0 - d) ** 1.6
                px[x, y] = (255, 255, 255, round(200 * a))
    return img


def hub() -> Image.Image:
    """Die Nabe der Nadel (7 × 7): Bronzeknopf mit Glanzpunkt."""
    art = [
        "..ooo..",
        ".oHLLo.",
        "oHWLMMo",
        "oLLMMDo",
        "oLMMDDo",
        ".oMDDo.",
        "..ooo..",
    ]
    codes = {"o": OUTLINE, "W": BRONZE[6], "H": BRONZE[6], "L": BRONZE[5], "M": BRONZE[4], "D": BRONZE[2]}
    img = Image.new("RGBA", (HUB, HUB), CLEAR)
    px = img.load()
    for y, row in enumerate(art):
        for x, ch in enumerate(row):
            if ch != ".":
                px[x, y] = codes[ch]
    return img


def gui_sheet() -> Image.Image:
    img = Image.new("RGBA", (512, 256), CLEAR)
    img.alpha_composite(window(), (0, 0))
    img.alpha_composite(dial(), (DIAL_U, 0))
    img.alpha_composite(gem(), (GEM_U, GEM_V))
    img.alpha_composite(halo(), (HALO_U, HALO_V))
    img.alpha_composite(hub(), (HUB_U, HUB_V))
    return img


# =============================================================================

def build(put: Callable[[str, Image.Image], None]) -> None:
    for i, frame in enumerate(compass_frames()):
        put(f"item/waystone_compass_{i:02d}", frame)
    put("block/waystone_base_side", base_side())
    put("block/waystone_base_top", base_top())
    put("block/waystone_shaft", shaft())
    put("block/waystone_shaft_glow", shaft_glow())
    put("block/waystone_bronze", bronze())
    put("block/waystone_cap_side", cap_side())
    put("block/waystone_cap_top", cap_top())
    put("block/waystone_cap_glow", cap_glow())
    put("block/waystone_crystal", crystal())
    put("gui/waystone_compass", gui_sheet())


def _preview(sheet: dict[str, Image.Image], target: str) -> None:
    zoom, pad = 8, 6
    items = [sheet[f"item/waystone_compass_{i:02d}"] for i in (16, 20, 24, 28, 0, 8)]
    blocks = [sheet[k] for k in ("block/waystone_base_side", "block/waystone_base_top", "block/waystone_shaft",
                                 "block/waystone_shaft_glow", "block/waystone_bronze", "block/waystone_cap_side",
                                 "block/waystone_cap_top", "block/waystone_cap_glow", "block/waystone_crystal")]
    cell = 16 * zoom + pad
    gui = sheet["gui/waystone_compass"]
    w = max(len(blocks) * cell + pad, gui.width * 2 + pad * 2)
    h = cell * 2 + pad + gui.height * 2 + pad
    out = Image.new("RGBA", (w, h), (52, 56, 62, 255))
    for i, img in enumerate(items):
        tile = pa.checker(16, 16, (150, 150, 150, 255), (128, 128, 128, 255))
        tile.alpha_composite(img)
        out.alpha_composite(pa.zoom(tile, zoom), (pad + i * cell, pad))
    for i, img in enumerate(blocks):
        tile = pa.checker(16, 16, (90, 90, 90, 255), (70, 70, 70, 255))
        tile.alpha_composite(img)
        out.alpha_composite(pa.zoom(tile, zoom), (pad + i * cell, pad + cell))
    out.alpha_composite(gui.resize((gui.width * 2, gui.height * 2), Image.NEAREST), (pad, pad + cell * 2))
    out.save(target)
    print("Vorschau:", target)


if __name__ == "__main__":
    generated: dict[str, Image.Image] = {}
    build(lambda key, img: generated.__setitem__(key, img))
    if len(sys.argv) > 1:
        _preview(generated, sys.argv[1])
    else:
        root = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))), "src/main/resources/assets/kingdomsatwar/textures")
        for key, img in generated.items():
            path = os.path.join(root, key + ".png")
            os.makedirs(os.path.dirname(path), exist_ok=True)
            img.save(path)
        print(len(generated), "Texturen geschrieben")
