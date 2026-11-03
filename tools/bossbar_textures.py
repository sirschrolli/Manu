#!/usr/bin/env python3
"""
Bossleisten der Drachen-Bosse (Version 2): je Element ein eigener, verzierter Rahmen.

  * links ein Medaillon mit dem Bildnis des Bosses (aus dem 3D-Modell gerendert, doppelte Auflösung)
  * die Leiste selbst (182 x 9) in einem Metallrahmen mit Nieten, darüber zwei Fassungen für die Phasen-Edelsteine
    (60 % und 30 %), rechts eine Spitze in Form des Elements (Flamme, Kristall, Blitz, Blatt, Fels, Flosse)
  * Verzierungen oben und unten: Flammen, Eiskristalle und Eiszapfen, Blitze, Ranken mit Blättern, Felszacken mit
    Bernstein, Wellenkämme mit Gischt
  * Füllung mit wanderndem Muster (wird im Spiel verschoben), leere Leiste, Spur für erlittenen Schaden, Glanz,
    leuchtende Aura für die letzte Phase, Edelsteine (leuchtend / zersprungen)

Aufbau einer Textur (256 x 256), siehe BossBarRenderer.java:
  (0, 0)     240 x 44  Rahmen; Fenster der Leiste bei (48, 18), 182 x 9; Medaillon-Mitte (21, 22)
  (0, 48)    256 x 9   Füllung (nahtlos kachelbar, wird verschoben)
  (0, 58)    182 x 9   leere Leiste
  (0, 68)    182 x 9   Schadensspur
  (184, 58)   16 x 9   Glanz
  (204, 58)    9 x 9   Edelstein leuchtend      (214, 58) Edelstein zersprungen
  (0, 80)    240 x 44  Aura (letzte Phase)
  (0, 128)    64 x 64  Bildnis (wird auf 32 x 32 gezeichnet)
"""
from __future__ import annotations

import math
import sys
from pathlib import Path

from PIL import Image, ImageEnhance

sys.path.insert(0, str(Path(__file__).resolve().parent))

from pixelart import CLEAR, hexc, mix  # noqa: E402

FRAME_W, FRAME_H = 240, 44
BAR_X, BAR_Y, BAR_W, BAR_H = 48, 18, 182, 9
EMB_X, EMB_Y, EMB_R = 21, 22, 20.0
GEMS = (0.6, 0.3)

# Metall dunkel / mittel / hell, Akzent, Leuchten, dunkler Grund
ELEMENTS = {
    "fire": dict(m0="#1c1412", m1="#4a3a34", m2="#9a8478", acc="#ff6a1a", glow="#ffd27a", deep="#2a0806",
                 f0="#5a0c04", f1="#d8360a", f2="#ff9a2a", f3="#ffe9a0"),
    "ice": dict(m0="#1a2430", m1="#4e6478", m2="#b8d0e0", acc="#6ad0f8", glow="#e8fbff", deep="#0a1c2a",
                f0="#0e3a66", f1="#2a86d0", f2="#7ad0f8", f3="#eafcff"),
    "lightning": dict(m0="#16141e", m1="#3c3a4c", m2="#a8a4bc", acc="#ffd21a", glow="#fff6b0", deep="#100e20",
                      f0="#5a4206", f1="#c89a10", f2="#ffd84a", f3="#fffbe0"),
    "nature": dict(m0="#1e160e", m1="#5a4428", m2="#a88e60", acc="#5ac832", glow="#d8ffa0", deep="#0c1c0a",
                   f0="#14460e", f1="#2e8a1e", f2="#6cd03a", f3="#d4ff9a"),
    "earth": dict(m0="#1e1a16", m1="#4e463c", m2="#a49684", acc="#e09a3a", glow="#ffe2a0", deep="#20140a",
                  f0="#5a2e08", f1="#b0661a", f2="#e8a440", f3="#fff0c0"),
    "water": dict(m0="#0e1a22", m1="#2e4a5a", m2="#8ab4c4", acc="#28c0f0", glow="#c8f6ff", deep="#04182a",
                  f0="#063a5e", f1="#0e78b8", f2="#3cc4ec", f3="#d8faff"),
    "shadow": dict(m0="#0c0812", m1="#2e2442", m2="#8a7ca8", acc="#b46cff", glow="#f0e0ff", deep="#12061e",
                   f0="#2a0a4e", f1="#6420b0", f2="#a660ff", f3="#f2e4ff"),
}

OUTLINE = (12, 10, 12, 255)


def c(pal: dict, key: str):
    return hexc(pal[key])


def put(px, img, x, y, col):
    if 0 <= x < img.width and 0 <= y < img.height:
        px[x, y] = col


def hash01(*a) -> float:
    h = 2166136261
    for v in a:
        h = ((h ^ (int(v) & 0xFFFFFFFF)) * 16777619) & 0xFFFFFFFF
    h ^= h >> 13
    h = (h * 1274126177) & 0xFFFFFFFF
    return (h & 0xFFFF) / 0xFFFF


# =============================================================================================
# Rahmen
# =============================================================================================

def frame_border(img: Image.Image, pal: dict) -> None:
    """Metallrahmen um die Leiste: dunkle Außenlinie, Fase (oben/links hell, unten/rechts dunkel), Innenschatten."""
    px = img.load()
    m0, m1, m2 = c(pal, "m0"), c(pal, "m1"), c(pal, "m2")
    x0, x1, y0, y1 = BAR_X - 3, BAR_X + BAR_W + 2, BAR_Y - 3, BAR_Y + BAR_H + 2
    for y in range(y0, y1 + 1):
        for x in range(x0, x1 + 1):
            if BAR_X <= x < BAR_X + BAR_W and BAR_Y <= y < BAR_Y + BAR_H:
                continue
            ring = min(x - x0, x1 - x, y - y0, y1 - y)
            if ring == 0:
                col = OUTLINE
            elif ring == 1:
                col = mix(m2, m1, 0.15) if (y - y0 <= 1 or x - x0 <= 1) and not (y1 - y <= 1) else mix(m1, m0, 0.35)
            else:
                col = mix(m0, OUTLINE, 0.3)
            px[x, y] = col
    # Nieten oben und unten
    for x in range(x0 + 9, x1 - 4, 22):
        if any(abs(x - (BAR_X + round(BAR_W * f))) < 7 for f in GEMS):
            continue
        for y in (y0 + 1, y1 - 1):
            put(px, img, x, y, m2)
            put(px, img, x + 1, y, mix(m1, m0, 0.4))
    # Fassungen der Edelsteine (oberhalb des Rahmens)
    for f in GEMS:
        gx = BAR_X + round(BAR_W * f)
        for dy in range(-6, 1):
            half = 5 - abs(dy + 3) + 1
            for dx in range(-half, half + 1):
                x, y = gx + dx, y0 + dy
                edge = abs(dx) == half or dy in (-6, 0)
                put(px, img, x, y, OUTLINE if edge else (mix(m1, m0, 0.2) if dx < 0 else mix(m0, OUTLINE, 0.2)))


def emblem(img: Image.Image, pal: dict) -> None:
    """Medaillon: Ring aus Metall (Licht von oben links), dunkler Grund mit Schimmer in der Farbe des Elements."""
    px = img.load()
    m0, m1, m2 = c(pal, "m0"), c(pal, "m1"), c(pal, "m2")
    deep, acc = c(pal, "deep"), c(pal, "acc")
    # Brücke zum Rahmen
    for y in range(BAR_Y - 1, BAR_Y + BAR_H + 1):
        for x in range(int(EMB_X + EMB_R) - 2, BAR_X - 2):
            edge = y in (BAR_Y - 1, BAR_Y + BAR_H)
            px[x, y] = OUTLINE if edge else mix(m1, m0, 0.3 + 0.4 * (y - BAR_Y) / BAR_H)
    for y in range(FRAME_H):
        for x in range(0, int(EMB_X + EMB_R) + 2):
            d = math.hypot(x - EMB_X + 0.5, y - EMB_Y + 0.5)
            if d > EMB_R:
                continue
            ang = math.atan2(y - EMB_Y, x - EMB_X)
            if d > EMB_R - 1.0:
                px[x, y] = OUTLINE
            elif d > EMB_R - 4.0:
                lit = 0.5 + 0.5 * math.cos(ang + 2.35)          # oben links hell
                col = mix(m0, m2, 0.15 + 0.7 * lit)
                if d > EMB_R - 1.8:
                    col = mix(col, m0, 0.35)
                px[x, y] = col
            elif d > EMB_R - 5.0:
                px[x, y] = OUTLINE
            else:
                t = d / (EMB_R - 5.0)
                px[x, y] = mix(mix(acc, deep, 0.25), mix(deep, OUTLINE, 0.2), t ** 1.3)


def gem(pal: dict, broken: bool) -> Image.Image:
    img = Image.new("RGBA", (9, 9), CLEAR)
    px = img.load()
    acc, glow = c(pal, "acc"), c(pal, "glow")
    for y in range(9):
        for x in range(9):
            d = abs(x - 4) + abs(y - 4)
            if d > 4:
                continue
            if d == 4:
                px[x, y] = OUTLINE
                continue
            if broken:
                col = (58, 54, 60, 255) if (x + y) % 3 else (40, 38, 44, 255)
                if (x == 3 and y in (2, 3)) or (x == 4 and y in (4, 5)) or (x == 5 and y == 6):
                    col = (20, 18, 22, 255)
            else:
                t = (x + y) / 8
                col = mix(glow, acc, min(1.0, t * 1.2))
                if (x, y) in ((3, 2), (2, 3), (3, 3)):
                    col = (255, 255, 255, 255)
            px[x, y] = col
    return img


# =============================================================================================
# Verzierungen je Element
# =============================================================================================

def deco_fire(img: Image.Image, pal: dict) -> None:
    """Flammenzungen oben auf dem Rahmen (am Medaillon und am Ende höher), Glutfunken darunter, Flamme als Spitze."""
    px = img.load()
    f0, f1, f2, f3 = (c(pal, k) for k in ("f0", "f1", "f2", "f3"))
    top = BAR_Y - 3

    def flame(cx: int, base: int, h: int, w: int):
        for k in range(h):
            t = k / max(1, h - 1)
            half = max(0, round((w / 2) * (1 - t) ** 0.7 * (0.9 + 0.2 * math.sin(k * 1.7 + cx))))
            sway = round(math.sin(k * 0.8 + cx * 0.3) * t * 1.2)
            for dx in range(-half, half + 1):
                inner = abs(dx) < half
                col = f1 if t < 0.35 else (f2 if t < 0.7 else f3)
                if not inner:
                    col = mix(f0, f1, 0.4) if t < 0.5 else f1
                elif abs(dx) <= max(0, half - 2) and t < 0.6:
                    col = f2 if t < 0.3 else f3
                put(px, img, cx + dx + sway, base - k, col)
    for x in range(BAR_X - 2, BAR_X + BAR_W + 2, 5):
        rel = (x - BAR_X) / BAR_W
        hgt = 3 + round(4 * (abs(rel - 0.5) * 2) ** 2 + 2 * hash01(x, 7))
        flame(x, top - 1, hgt, 4)
    # Krone aus Flammen auf dem Medaillon
    for k, ang in enumerate(range(-150, -20, 26)):
        a = math.radians(ang)
        x = round(EMB_X + math.cos(a) * (EMB_R - 1))
        y = round(EMB_Y + math.sin(a) * (EMB_R - 1))
        flame(x, y, 4 + (k % 2) * 2, 4)
    # Glutfunken unterhalb
    for x in range(BAR_X + 4, BAR_X + BAR_W, 9):
        if hash01(x, 3) > 0.55:
            put(px, img, x, BAR_Y + BAR_H + 4 + round(hash01(x, 5) * 4), f2 if hash01(x, 9) > 0.5 else f1)
    # Spitze: liegende Flamme nach rechts
    ex, ey = BAR_X + BAR_W + 3, BAR_Y + BAR_H // 2
    for k in range(10):
        half = round(5 * (1 - k / 10) ** 0.8)
        for dy in range(-half, half + 1):
            col = OUTLINE if abs(dy) == half else (f1 if abs(dy) > half - 2 else (f2 if k < 6 else f3))
            put(px, img, ex + k, ey + dy + round(math.sin(k * 0.9) * k / 8), col)


def deco_ice(img: Image.Image, pal: dict) -> None:
    """Eiskristalle oben, Eiszapfen unten, Kristallspitzen am Medaillon, Kristall als Spitze."""
    px = img.load()
    f1, f2, f3 = c(pal, "f1"), c(pal, "f2"), c(pal, "f3")
    m0 = c(pal, "m0")
    top, bottom = BAR_Y - 3, BAR_Y + BAR_H + 2

    def crystal(cx: int, base: int, h: int, w: int, up: bool = True):
        for k in range(h):
            half = max(0, round((w / 2) * (1 - k / h)))
            for dx in range(-half, half + 1):
                if abs(dx) == half:
                    col = mix(f1, m0, 0.3)
                else:
                    col = f3 if dx < 0 else (f2 if dx == 0 else f1)
                put(px, img, cx + dx, base - k if up else base + k, col)
    for x in range(BAR_X + 3, BAR_X + BAR_W, 11):
        h = 4 + round(5 * hash01(x, 2))
        crystal(x, top - 1, h, 4)
        if hash01(x, 4) > 0.4:
            crystal(x + 3, top - 1, max(2, h - 3), 2)
    for x in range(BAR_X + 6, BAR_X + BAR_W - 2, 7):
        crystal(x, bottom + 1, 3 + round(6 * hash01(x, 6)), 2, up=False)
    for ang in (-90, -135, -45, 180):
        a = math.radians(ang)
        crystal(round(EMB_X + math.cos(a) * (EMB_R - 2)), round(EMB_Y + math.sin(a) * (EMB_R - 2)), 6, 4,
                up=ang != 180)
    ex, ey = BAR_X + BAR_W + 3, BAR_Y + BAR_H // 2
    for k in range(10):
        half = round(4 * (1 - abs(k - 3) / 7)) if k > 3 else k + 1
        for dy in range(-half, half + 1):
            col = OUTLINE if abs(dy) == half else (f3 if dy < 0 else f1)
            put(px, img, ex + k, ey + dy, col)


def deco_lightning(img: Image.Image, pal: dict) -> None:
    """Goldene Zickzack-Einlage im Rahmen, kleine Blitze oben, Funken, Blitz als Spitze."""
    px = img.load()
    f2, f3, acc = c(pal, "f2"), c(pal, "f3"), c(pal, "acc")
    top, bottom = BAR_Y - 3, BAR_Y + BAR_H + 2
    for x in range(BAR_X - 2, BAR_X + BAR_W + 2):
        zig = (x // 3) % 2
        put(px, img, x, top + 1 if zig else top + 2, acc)
        put(px, img, x, bottom - 1 if not zig else bottom - 2, mix(acc, (0, 0, 0, 255), 0.35))

    def bolt(x: int, y: int, length: int, seed: int):
        cx = x
        for k in range(length):
            if k % 3 == 2:
                cx += 1 if hash01(seed, k) > 0.5 else -1
            put(px, img, cx, y - k, f3 if k % 2 else f2)
            put(px, img, cx + 1, y - k, OUTLINE)
    for i, x in enumerate(range(BAR_X + 10, BAR_X + BAR_W, 26)):
        bolt(x, top - 1, 4 + (i % 3) * 2, x)
    for x in range(BAR_X, BAR_X + BAR_W, 6):
        if hash01(x, 11) > 0.7:
            put(px, img, x, top - 3 - round(hash01(x, 12) * 5), f3)
        if hash01(x, 13) > 0.75:
            put(px, img, x, bottom + 2 + round(hash01(x, 14) * 4), f2)
    for ang in range(-160, 170, 40):
        a = math.radians(ang)
        x = round(EMB_X + math.cos(a) * (EMB_R - 2.5))
        y = round(EMB_Y + math.sin(a) * (EMB_R - 2.5))
        put(px, img, x, y, f3)
    # Blitz nach rechts
    ex, ey = BAR_X + BAR_W + 3, BAR_Y - 1
    shape = ["xx.......", ".xxx.....", "..xxxx...", "...xxxxx.", "....xxx..", "...xxxx..", "....xxxxx", ".....xxx.",
             "......xx.", ".......x."]
    for dy, row in enumerate(shape):
        for dx, ch in enumerate(row):
            if ch == "x":
                put(px, img, ex + dx, ey + dy, f3 if dx < 4 else f2)
    for dy, row in enumerate(shape):
        for dx, ch in enumerate(row):
            if ch == "x":
                for ox, oy in ((1, 0), (0, 1), (-1, 0), (0, -1)):
                    nx, ny = dx + ox, dy + oy
                    inside = 0 <= ny < len(shape) and 0 <= nx < len(shape[ny]) and shape[ny][nx] == "x"
                    tx, ty = ex + nx, ey + ny
                    if not inside and 0 <= tx < img.width and 0 <= ty < img.height and px[tx, ty][3] == 0:
                        put(px, img, tx, ty, OUTLINE)


def deco_nature(img: Image.Image, pal: dict) -> None:
    """Ranken winden sich um den Rahmen, mit Blättern und ein paar Blüten; ein Blatt als Spitze."""
    px = img.load()
    f0, f1, f2, f3 = (c(pal, k) for k in ("f0", "f1", "f2", "f3"))
    blossom = hexc("#f07ab0")
    top, bottom = BAR_Y - 3, BAR_Y + BAR_H + 2

    def leaf(x: int, y: int, d: int):
        cells = [(0, 0), (d, 0), (d, -1), (2 * d, -1), (d, 1), (0, -1)]
        for i, (dx, dy) in enumerate(cells):
            put(px, img, x + dx, y + dy, f2 if i < 3 else f1)
        put(px, img, x + 2 * d, y - 1, f3)
    for x in range(BAR_X - 3, BAR_X + BAR_W + 3):
        yt = top - 1 + round(1.6 * math.sin(x * 0.21))
        yb = bottom + 1 + round(1.6 * math.sin(x * 0.21 + 2.0))
        put(px, img, x, yt, f0)
        put(px, img, x, yt - 1, f1)
        put(px, img, x, yb, f0)
        put(px, img, x, yb + 1, mix(f0, f1, 0.4))
        if x % 9 == 0:
            leaf(x, yt - 2, 1 if (x // 9) % 2 else -1)
        if x % 11 == 5:
            leaf(x, yb + 2, -1 if (x // 11) % 2 else 1)
        if x % 31 == 13:
            put(px, img, x, yt - 3, blossom)
            put(px, img, x + 1, yt - 3, hexc("#ffd0e4"))
    for ang in range(-170, 180, 34):
        a = math.radians(ang)
        leaf(round(EMB_X + math.cos(a) * (EMB_R + 0.5)), round(EMB_Y + math.sin(a) * (EMB_R + 0.5)), 1 if ang < 0 else -1)
    ex, ey = BAR_X + BAR_W + 3, BAR_Y + BAR_H // 2
    for k in range(10):
        half = round(4.5 * math.sin(math.pi * (k + 0.5) / 10))
        for dy in range(-half, half + 1):
            col = OUTLINE if abs(dy) == half else (f3 if dy == 0 else (f2 if dy < 0 else f1))
            put(px, img, ex + k, ey + dy, col)


def deco_earth(img: Image.Image, pal: dict) -> None:
    """Zackige Felsen auf dem Rahmen, Bernsteinkristalle, Geröll darunter, Felsspitze am Ende."""
    px = img.load()
    m0, m1, m2 = c(pal, "m0"), c(pal, "m1"), c(pal, "m2")
    f2, f3 = c(pal, "f2"), c(pal, "f3")
    top, bottom = BAR_Y - 3, BAR_Y + BAR_H + 2
    x = BAR_X - 3
    while x < BAR_X + BAR_W + 2:
        w = 5 + round(5 * hash01(x, 1))
        h = 2 + round(4 * hash01(x, 2))
        for dx in range(w):
            t = dx / max(1, w - 1)
            colh = round(h * (1 - abs(t - 0.4) * 1.4))
            for k in range(max(1, colh)):
                col = m2 if dx < w * 0.4 else (m1 if dx < w * 0.8 else mix(m1, m0, 0.5))
                if k == colh - 1:
                    col = mix(col, m2, 0.5)
                put(px, img, x + dx, top - 1 - k, col)
        x += w
    for i, cx in enumerate((BAR_X + 30, BAR_X + 85, BAR_X + 140)):
        for j, (dx, h) in enumerate(((0, 6), (2, 4), (-2, 3))):
            for k in range(h):
                put(px, img, cx + dx, top - 2 - k, f3 if k > h - 3 else f2)
                put(px, img, cx + dx + 1, top - 2 - k, mix(f2, m0, 0.4))
    for gx in range(BAR_X + 3, BAR_X + BAR_W, 7):
        if hash01(gx, 5) > 0.5:
            put(px, img, gx, bottom + 1, m1)
            put(px, img, gx + 1, bottom + 1, m0)
    ex, ey = BAR_X + BAR_W + 3, BAR_Y + BAR_H // 2
    for k in range(9):
        half = round(5 * (1 - k / 9))
        for dy in range(-half, half + 1):
            col = OUTLINE if abs(dy) == half else (m2 if dy < 0 else m1)
            put(px, img, ex + k, ey + dy, col)


def deco_water(img: Image.Image, pal: dict) -> None:
    """Wellenkämme mit Gischt oben, Luftblasen unten, Flosse als Spitze."""
    px = img.load()
    f1, f2, f3 = c(pal, "f1"), c(pal, "f2"), c(pal, "f3")
    top, bottom = BAR_Y - 3, BAR_Y + BAR_H + 2
    period = 14
    for x in range(BAR_X - 3, BAR_X + BAR_W + 3):
        u = ((x - BAR_X) % period) / period
        crest = round(4 * math.sin(math.pi * u) ** 3)
        for k in range(crest + 1):
            col = f1 if k < crest - 1 else f2
            put(px, img, x, top - 1 - k, col)
        if crest >= 3:
            put(px, img, x, top - 2 - crest, f3)
        if 0.6 < u < 0.75:
            put(px, img, x, top - 1 - crest, f3)
    for x in range(BAR_X + 5, BAR_X + BAR_W, 13):
        y = bottom + 3 + round(hash01(x, 2) * 4)
        r = 1 if hash01(x, 3) > 0.5 else 0
        for dx in range(-r, r + 1):
            for dy in range(-r, r + 1):
                put(px, img, x + dx, y + dy, f2 if (dx, dy) != (-r, -r) else f3)
        put(px, img, x - r, y - r, f3)
    ex, ey = BAR_X + BAR_W + 3, BAR_Y + BAR_H // 2
    for k in range(10):
        up = round(6 * (k / 9) ** 1.4)
        down = round(4 * (k / 9) ** 1.4)
        for dy in range(-up, down + 1):
            if k < 9 and abs(dy) < 2 + k // 3:
                col = OUTLINE if dy in (-up, down) else (f3 if dy < 0 else f1)
                put(px, img, ex + k, ey + dy, col)


def deco_shadow(img: Image.Image, pal: dict) -> None:
    """Dornige Nachtkristalle oben (zum Medaillon hin höher), Seelenfunken darunter, eine Krone aus Kristallen am
    Medaillon und ein wachendes Auge als Spitze."""
    px = img.load()
    f0, f1, f2, f3 = (c(pal, k) for k in ("f0", "f1", "f2", "f3"))
    m0 = c(pal, "m0")
    top, bottom = BAR_Y - 3, BAR_Y + BAR_H + 2

    def shard(cx: int, base: int, h: int, w: int, lean: float, up: bool = True):
        for k in range(h):
            t = k / max(1, h - 1)
            half = max(0, round((w / 2) * (1 - t)))
            off = round(lean * k)
            for dx in range(-half, half + 1):
                if abs(dx) == half:
                    col = mix(f0, m0, 0.35)
                else:
                    col = f3 if (dx < 0 and t > 0.4) else (f2 if dx <= 0 else f1)
                put(px, img, cx + dx + off, base - k if up else base + k, col)
    for x in range(BAR_X - 1, BAR_X + BAR_W + 1, 6):
        rel = (x - BAR_X) / BAR_W
        h = 3 + round(5 * (1 - rel) ** 2 + 3 * hash01(x, 11))
        shard(x, top - 1, h, 3 if h < 6 else 4, (hash01(x, 13) - 0.5) * 0.5)
    # Seelenfunken unter der Leiste, wie aufsteigende Irrlichter
    for x in range(BAR_X + 3, BAR_X + BAR_W, 8):
        if hash01(x, 17) > 0.45:
            y = bottom + 2 + round(hash01(x, 19) * 5)
            put(px, img, x, y, f3)
            put(px, img, x, y + 1, f2)
            put(px, img, x + (1 if hash01(x, 23) > 0.5 else -1), y + 2, f1)
    # Kristallkrone am Medaillon
    for k, ang in enumerate(range(-160, -10, 22)):
        a = math.radians(ang)
        x = round(EMB_X + math.cos(a) * (EMB_R - 1))
        y = round(EMB_Y + math.sin(a) * (EMB_R - 1))
        shard(x, y, 5 + (k % 2) * 3, 3, math.cos(a) * 0.5)
    # Spitze: ein wachendes Auge (Mandelform, leuchtende Pupille)
    ex, ey = BAR_X + BAR_W + 3, BAR_Y + BAR_H // 2
    for k in range(11):
        half = round(4.2 * math.sin(math.pi * k / 10))
        for dy in range(-half, half + 1):
            if abs(dy) == half:
                col = OUTLINE
            elif abs(k - 5) <= 1 and abs(dy) <= 1:
                col = f3 if (k, dy) == (5, 0) else f2
            else:
                col = mix(m0, f0, 0.6)
            put(px, img, ex + k, ey + dy, col)


DECOS = {"shadow": deco_shadow, "fire": deco_fire, "ice": deco_ice, "lightning": deco_lightning, "nature": deco_nature, "earth": deco_earth,
         "water": deco_water}


def frame(element: str) -> Image.Image:
    pal = ELEMENTS[element]
    img = Image.new("RGBA", (FRAME_W, FRAME_H), CLEAR)
    DECOS[element](img, pal)
    frame_border(img, pal)
    emblem(img, pal)
    return img


# =============================================================================================
# Füllung, leere Leiste, Spur, Glanz, Aura
# =============================================================================================

def fill_value(element: str, x: int, y: int) -> float:
    tau = math.tau
    if element == "fire":
        return 0.5 + 0.5 * math.sin(tau * x / 32 + 1.6 * math.sin(tau * y / 9 + tau * x / 64))
    if element == "ice":
        band = ((x + 2 * y) // 6) % 4
        return (0.75 if band == 0 else 0.3) + 0.15 * math.sin(tau * x / 64)
    if element == "lightning":
        u = (x / 8.0) % 2.0
        tri = u if u < 1.0 else 2.0 - u                     # Zickzack-Bogen quer durch die Leiste
        arc = 1.5 + 5.0 * tri + (1.0 if (x // 16) % 2 else 0.0)
        d = abs(y - arc)
        return 1.0 if d < 0.6 else (0.72 if d < 1.6 else 0.32 + 0.08 * math.sin(tau * x / 32))
    if element == "nature":
        vein = ((x - 2 * y) % 16) < 2
        return 0.75 if vein else 0.4 + 0.15 * math.sin(tau * x / 32) * math.cos(tau * y / 9)
    if element == "earth":
        return 0.35 + 0.4 * hash01(x // 2, y // 2, 7) * (0.5 + 0.5 * math.sin(tau * x / 64))
    if element == "water":
        return 0.5 + 0.5 * math.sin(tau * (x / 32 - y / 9))
    if element == "shadow":
        # wabernder Rauch, durch den einzelne Seelen ziehen
        smoke = 0.42 + 0.22 * math.sin(tau * x / 64 + 2.0 * math.sin(tau * y / 9 + tau * x / 32))
        return 1.0 if hash01(x // 2, y // 3, 29) > 0.93 else smoke
    return 0.5


def fill_strip(element: str) -> Image.Image:
    pal = ELEMENTS[element]
    f0, f1, f2, f3 = (c(pal, k) for k in ("f0", "f1", "f2", "f3"))
    img = Image.new("RGBA", (256, BAR_H), CLEAR)
    px = img.load()
    for y in range(BAR_H):
        top = 1.0 - y / (BAR_H - 1)
        for x in range(256):
            v = fill_value(element, x, y)
            base = mix(f0, f1, 0.35 + 0.65 * top)
            col = mix(base, f2, max(0.0, v - 0.35) * 1.2)
            if v > 0.85:
                col = mix(col, f3, (v - 0.85) / 0.15 * 0.8)
            if y == 1:
                col = mix(col, f3, 0.35)
            if y == BAR_H - 1:
                col = mix(col, f0, 0.5)
            px[x, y] = col
    return img


def empty_strip(element: str) -> Image.Image:
    pal = ELEMENTS[element]
    deep = c(pal, "deep")
    img = Image.new("RGBA", (BAR_W, BAR_H), CLEAR)
    px = img.load()
    for y in range(BAR_H):
        for x in range(BAR_W):
            v = 0.25 + 0.1 * fill_value(element, x, y)
            col = mix(OUTLINE, deep, v * 1.6)
            if y == 0:
                col = mix(col, OUTLINE, 0.6)
            elif y == BAR_H - 1:
                col = mix(col, deep, 0.5)
            px[x, y] = col
    return img


def trail_strip(element: str) -> Image.Image:
    pal = ELEMENTS[element]
    glow = c(pal, "glow")
    img = Image.new("RGBA", (BAR_W, BAR_H), CLEAR)
    px = img.load()
    for y in range(BAR_H):
        for x in range(BAR_W):
            a = 210 if 0 < y < BAR_H - 1 else 150
            px[x, y] = glow[:3] + (a,)
    return img


def shine_strip() -> Image.Image:
    img = Image.new("RGBA", (16, BAR_H), CLEAR)
    px = img.load()
    for y in range(BAR_H):
        for x in range(16):
            d = abs((x - y * 0.7) - 6.0)
            if d < 3.5:
                px[x, y] = (255, 255, 255, int(255 * (1 - d / 3.5) * 0.8))
    return img


def aura(element: str, frame_img: Image.Image) -> Image.Image:
    """Weicher Schein rund um Rahmen und Medaillon (für die letzte Phase)."""
    pal = ELEMENTS[element]
    glow, acc = c(pal, "glow"), c(pal, "acc")
    src = frame_img.load()
    img = Image.new("RGBA", (FRAME_W, FRAME_H), CLEAR)
    px = img.load()
    solid = [[src[x, y][3] > 0 for y in range(FRAME_H)] for x in range(FRAME_W)]
    for y in range(FRAME_H):
        for x in range(FRAME_W):
            if solid[x][y]:
                continue
            best = 99.0
            for dy in range(-4, 5):
                for dx in range(-4, 5):
                    nx, ny = x + dx, y + dy
                    if 0 <= nx < FRAME_W and 0 <= ny < FRAME_H and solid[nx][ny]:
                        best = min(best, math.hypot(dx, dy))
            if best < 4.5:
                t = 1.0 - best / 4.5
                px[x, y] = mix(acc, glow, t)[:3] + (int(230 * t ** 1.5),)
    return img


# =============================================================================================
# Bildnis aus dem 3D-Modell
# =============================================================================================

def portrait(element: str) -> Image.Image:
    """Kopf des Bosses schräg von vorn, aus dem Modell gerendert und auf 64 x 64 verkleinert, kreisrund beschnitten,
    mit dunkler Kontur (2 Pixel = 1 Pixel im Spiel), damit er sich vom Grund des Medaillons abhebt."""
    from entitygen.models import boss_earth, boss_fire, boss_ice, boss_lightning, boss_nature, boss_shadow, boss_water
    from entitygen.paint import paint_model
    from entitygen.preview import render
    mod = {"fire": boss_fire, "ice": boss_ice, "lightning": boss_lightning, "nature": boss_nature, "earth": boss_earth,
           "water": boss_water, "shadow": boss_shadow}[element]
    m = mod.build()
    m.pack()
    variant = next(iter(mod.PALETTES))
    tex, glow = paint_model(m, mod.materials(variant), mod.PALETTES[variant], seed=100)
    zoom = {"nature": 0.82, "earth": 1.15, "water": 1.1}.get(element, 1.2)
    yaw = {"water": -50, "earth": -38}.get(element, -42)
    big = render(m, tex, glow, yaw, 6, 448, pose=getattr(mod, "FOLD_POSE", None), background=(0, 0, 0, 0), zoom=zoom,
                 focus=["head"])
    img = big.resize((64, 64), Image.LANCZOS)
    img = ImageEnhance.Brightness(img).enhance({"fire": 1.9, "earth": 1.5, "lightning": 1.9, "nature": 1.6, "shadow": 2.4}.get(element, 1.4))
    img = ImageEnhance.Color(img).enhance(1.15)
    img = ImageEnhance.Contrast(img).enhance(1.12)
    px = img.load()
    solid = [[False] * 64 for _ in range(64)]
    for y in range(64):
        for x in range(64):
            r, g, b, a = px[x, y]
            d = math.hypot(x - 31.5, y - 31.5)
            on = a > 140 and d <= 29.5
            solid[x][y] = on
            px[x, y] = (r, g, b, 255) if on else (0, 0, 0, 0)
    out = Image.new("RGBA", (64, 64), CLEAR)
    op = out.load()
    for y in range(64):
        for x in range(64):
            if solid[x][y]:
                op[x, y] = px[x, y]
                continue
            near = any(0 <= x + dx < 64 and 0 <= y + dy < 64 and solid[x + dx][y + dy]
                       for dx in (-2, -1, 0, 1, 2) for dy in (-2, -1, 0, 1, 2) if abs(dx) + abs(dy) <= 3)
            if near and math.hypot(x - 31.5, y - 31.5) <= 31.5:
                op[x, y] = (10, 8, 12, 255)
    return out


def mage_portrait(element: str) -> Image.Image:
    """Bildnis eines Erzmagiers: Kopf mit Kopfschmuck schräg von vorn (Zubehör anderer Elemente entfernt)."""
    from entitygen.models import mage
    from entitygen.paint import paint_model
    from entitygen.preview import render
    m = mage.build()
    m.pack()
    hidden = (mage.ALL_GEAR - mage.GEAR[element]) | {"left_arm", "right_arm"}

    def strip(bone):
        if bone.name in hidden:
            bone.cubes.clear()
            for child in bone.children:
                strip_all(child)
            return
        for child in bone.children:
            strip(child)

    def strip_all(bone):
        bone.cubes.clear()
        for child in bone.children:
            strip_all(child)

    tex, glow = paint_model(m, mage.materials(element), mage.PALETTES[element], seed=100 + mage.ELEMENTS.index(element),
                            params={"variant": element})
    strip(m.root)
    big = render(m, tex, glow, -28, 4, 448, background=(0, 0, 0, 0), zoom=1.08, focus=["head"])
    img = big.resize((64, 64), Image.LANCZOS)
    img = ImageEnhance.Brightness(img).enhance({"fire": 1.2, "earth": 1.25, "lightning": 1.5}.get(element, 1.4))
    img = ImageEnhance.Color(img).enhance(1.15)
    img = ImageEnhance.Contrast(img).enhance(1.12)
    px = img.load()
    solid = [[False] * 64 for _ in range(64)]
    for y in range(64):
        for x in range(64):
            r, g, b, a = px[x, y]
            d = math.hypot(x - 31.5, y - 31.5)
            on = a > 140 and d <= 29.5
            solid[x][y] = on
            px[x, y] = (r, g, b, 255) if on else (0, 0, 0, 0)
    out = Image.new("RGBA", (64, 64), CLEAR)
    op = out.load()
    for y in range(64):
        for x in range(64):
            if solid[x][y]:
                op[x, y] = px[x, y]
                continue
            near = any(0 <= x + dx < 64 and 0 <= y + dy < 64 and solid[x + dx][y + dy]
                       for dx in (-2, -1, 0, 1, 2) for dy in (-2, -1, 0, 1, 2) if abs(dx) + abs(dy) <= 3)
            if near and math.hypot(x - 31.5, y - 31.5) <= 31.5:
                op[x, y] = (10, 8, 12, 255)
    return out


def mage_bossbar(element: str) -> Image.Image:
    """Bossleiste eines Erzmagiers: Rahmen und Füllung seines Elements, im Medaillon sein Bildnis."""
    img = bossbar(element)
    img.paste((0, 0, 0, 0), (0, 128, 64, 192))
    img.alpha_composite(mage_portrait(element), (0, 128))
    return img


def bossbar(element: str) -> Image.Image:
    img = Image.new("RGBA", (256, 256), CLEAR)
    fr = frame(element)
    img.alpha_composite(fr, (0, 0))
    img.alpha_composite(fill_strip(element), (0, 48))
    img.alpha_composite(empty_strip(element), (0, 58))
    img.alpha_composite(trail_strip(element), (0, 68))
    img.alpha_composite(shine_strip(), (184, 58))
    img.alpha_composite(gem(ELEMENTS[element], False), (204, 58))
    img.alpha_composite(gem(ELEMENTS[element], True), (214, 58))
    img.alpha_composite(aura(element, fr), (0, 80))
    img.alpha_composite(portrait(element), (0, 128))
    return img


def preview(target: str) -> None:
    """Vorschau: alle Leisten zusammengesetzt (wie im Spiel, bei 70 % und 25 % Leben), dreifach vergrößert."""
    out = Image.new("RGBA", (FRAME_W * 3 * 2 + 30, len(ELEMENTS) * 3 * (FRAME_H + 10) + 20), (46, 52, 60, 255))
    for i, element in enumerate(ELEMENTS):
        tex = bossbar(element)
        for j, hp in enumerate((0.7, 0.25)):
            scene = Image.new("RGBA", (FRAME_W, FRAME_H + 8), (0, 0, 0, 0))
            scene.alpha_composite(tex.crop((0, 58, BAR_W, 58 + BAR_H)), (BAR_X, BAR_Y + 8))
            fill = round(BAR_W * hp)
            trail = min(BAR_W, fill + 18)
            scene.alpha_composite(tex.crop((0, 68, trail, 68 + BAR_H)), (BAR_X, BAR_Y + 8))
            scene.alpha_composite(tex.crop((20, 48, 20 + fill, 48 + BAR_H)), (BAR_X, BAR_Y + 8))
            if hp < 0.3:
                scene.alpha_composite(tex.crop((0, 80, FRAME_W, 80 + FRAME_H)), (0, 8))
            scene.alpha_composite(tex.crop((0, 0, FRAME_W, FRAME_H)), (0, 8))
            p = tex.crop((0, 128, 64, 192)).resize((32, 32), Image.NEAREST)
            scene.alpha_composite(p, (EMB_X - 16, EMB_Y - 16 + 8))
            for f in GEMS:
                gx = BAR_X + round(BAR_W * f)
                g = tex.crop((214, 58, 223, 67)) if hp < f else tex.crop((204, 58, 213, 67))
                scene.alpha_composite(g, (gx - 4, BAR_Y - 3 - 7 + 8))
            big = scene.resize((scene.width * 3, scene.height * 3), Image.NEAREST)
            out.alpha_composite(big, (10 + j * (FRAME_W * 3 + 10), 10 + i * 3 * (FRAME_H + 10)))
    out.save(target)


if __name__ == "__main__":
    preview(sys.argv[1] if len(sys.argv) > 1 else "/tmp/claude-0/-home-claude/50786bb5-6cc3-5907-a25e-13bee43305bb/scratchpad/v2/bars_new.png")
