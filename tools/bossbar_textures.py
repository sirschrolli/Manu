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
    # der Zerbrochene König (Boss der Leere): Sternenmarmor mit Goldsaum, Füllung wie ein Sternennebel
    "void": dict(m0="#16121f", m1="#4a4262", m2="#d2cae6", acc="#d6a23c", glow="#f4ecff", deep="#0a0618",
                 f0="#1a0c40", f1="#4c26a0", f2="#9a6ae8", f3="#f4eeff", gold="#e2b04a", gold_dark="#7a5420"),
    # Karkinos, der Riffkoloss: ausgebleichter Muschelkalk mit Goldsaum, darauf ein Korallenriff; Füllung wie das
    # Licht einer Lagune (Wellennetz und Blasen)
    "reef": dict(m0="#2a2219", m1="#7a6850", m2="#eadfc4", acc="#2ad8c8", glow="#d4fff4", deep="#03222a",
                 f0="#043e48", f1="#0a8486", f2="#38d4c4", f3="#e4fff6", gold="#e8b84a", gold_dark="#7a5a1e",
                 coral="#ee5a40", coral_dark="#7e2420", coral_light="#ffb08a", fan="#c25ab8", fan_dark="#5e2058",
                 sponge="#f0a428", sponge_dark="#7a4a10", brain="#d8c060", brain_dark="#7a6420",
                 anemone="#7ae8a8", anemone_dark="#1e6a4c", star="#ff8a3a"),
    # Noctua, die Mottenkönigin: Mondsilber mit Goldsaum, darauf Mottenflügel, Seidenfäden und Mondsicheln; Füllung wie
    # schimmernder Flügelstaub (Schuppenreihen, Glitzer)
    "moth": dict(m0="#1a1428", m1="#5a4e78", m2="#e2dcf2", acc="#ff8ad0", glow="#fff0fa", deep="#140c24",
                 f0="#3a1458", f1="#8a3aa8", f2="#e47ac8", f3="#fff0fa", gold="#ffd878", gold_dark="#8a6420",
                 wing="#9ee8c8", wing_dark="#3a8a74", wing_light="#e4fff2", spot="#ff7ab8", spot_dark="#5a1e4e",
                 silk="#f4eefc", silk_dark="#a898c4"),
    # Malleus, der Glutschmied: geschwärztes Eisen mit Nieten und glühenden Nähten, dahinter Hammer und Speer gekreuzt;
    # Füllung wie fließendes, glühendes Metall mit hellen Rissen
    "forge": dict(m0="#141012", m1="#4a4246", m2="#a89c98", acc="#ff7a1a", glow="#fff0c0", deep="#1c0804",
                  f0="#3e0a02", f1="#a8300a", f2="#ff8a24", f3="#fff2b8", gold="#e6ac48", gold_dark="#7a5218",
                  iron="#2e2c32", iron_light="#7c7880", ember="#ff9a30", ember_dark="#8a2406", wood="#4a2e1e"),
    # Asteria, die Sternenreiterin: poliertes Silber mit Goldsaum, dahinter die Schwingen ihres Sternenrochens und die
    # Sternenlanze; Füllung wie der Nachthimmel mit Nebelschleiern und funkelnden Sternen
    "star": dict(m0="#121628", m1="#5a6488", m2="#dce4f6", acc="#9ad8ff", glow="#f2f8ff", deep="#060a22",
                 f0="#0b1236", f1="#2a3d8a", f2="#7ab0ff", f3="#eef6ff", gold="#e8c25a", gold_dark="#8a6018",
                 night="#141c48", night_dark="#070b22", nebula="#7a3ab4", nebula_light="#c08ae8", silver="#c8d0e4",
                 silver_dark="#5a6488", crystal="#9ad0ff"),
    # MissMystik, die Feen-Zauberkünstlerin: violetter Samt mit Goldsaum, dahinter ein Fächer aus Zauberkarten und ihr
    # Zauberstab; Füllung wie ein Harlekin-Rautenmuster aus Samt, in dem Pailletten funkeln
    "mystic": dict(m0="#140c1e", m1="#4e3470", m2="#dcc8f4", acc="#ff7ac8", glow="#fff0fa", deep="#10061a",
                   f0="#2e0a44", f1="#7a2496", f2="#e46ad0", f3="#fff0fa", gold="#ffd86a", gold_dark="#8a6020",
                   card="#fbf7ff", card_dark="#a898c0", red="#e8344a", black="#1c1428", wand="#121016",
                   confetti="#4ae0e8"),
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


def deco_void(img: Image.Image, pal: dict) -> None:
    """Schwebende Bruchstücke aus Sternenmarmor über der Leiste (am Medaillon dichter), dazwischen Sterne; darunter
    Sternenstaub; am Medaillon die Krone aus sieben Kristallsplittern des Königs; als Spitze sein zersprungenes Herz."""
    px = img.load()
    f0, f1, f2, f3 = (c(pal, k) for k in ("f0", "f1", "f2", "f3"))
    m0, m1, m2 = c(pal, "m0"), c(pal, "m1"), c(pal, "m2")
    gold, gold_dark = c(pal, "gold"), c(pal, "gold_dark")
    top, bottom = BAR_Y - 3, BAR_Y + BAR_H + 2

    def chunk(cx: int, cy: int, w: int, h: int, seed: int):
        """Ein kleines Bruchstück: unregelmäßiger Umriss, oben hell, eine goldene Kante."""
        for dy in range(h):
            for dx in range(w):
                corner = (dx in (0, w - 1)) and (dy in (0, h - 1)) and hash01(seed, dx, dy) > 0.35
                if corner:
                    continue
                edge = dx in (0, w - 1) or dy in (0, h - 1)
                if edge:
                    col = OUTLINE
                elif dy == 1:
                    col = m2
                elif dy == h - 2 and hash01(seed, 3) > 0.4:
                    col = gold
                else:
                    col = mix(m1, m2, 0.35 + 0.3 * hash01(seed, dx, dy))
                put(px, img, cx + dx, cy + dy, col)

    def star(x: int, y: int, big: bool):
        put(px, img, x, y, f3)
        if big:
            for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                put(px, img, x + dx, y + dy, f2)

    # ein zerbrochener Fries: am Medaillon liegen die Stücke noch auf dem Rahmen, nach rechts lösen sie sich, steigen
    # und werden kleiner, bis nur noch Sterne treiben
    x = BAR_X + 1
    while x < BAR_X + BAR_W - 6:
        rel = (x - BAR_X) / BAR_W
        w = max(3, round(7 - 4 * rel + hash01(x, 31)))
        h = 4 if rel < 0.55 else 3
        lift = round(rel * 8 + (hash01(x, 41) - 0.5) * 2.0 * min(1.0, rel * 3))
        near_gem = any(abs(x + w // 2 - (BAR_X + round(BAR_W * f))) < 8 for f in GEMS)
        if not near_gem and rel < 0.9:
            chunk(x, top - lift - h + 1, w, h, x)
            if hash01(x, 89) > 0.55 and w >= 4:
                put(px, img, x + 1 + round(hash01(x, 97) * (w - 3)), top - lift - h + 3, f2)   # leuchtender Riss
        gap = 2 + round(rel * 6 + hash01(x, 59) * 2)
        if rel > 0.25 and hash01(x, 43) > 0.4:
            star(x + w + gap // 2, top - lift - 3 - round(hash01(x, 47) * 5), hash01(x, 53) > 0.75)
        x += w + gap
    # Sternenstaub unter der Leiste
    for x in range(BAR_X + 2, BAR_X + BAR_W, 6):
        if hash01(x, 61) > 0.6:
            y = bottom + 2 + round(hash01(x, 67) * 4)
            put(px, img, x, y, f3 if hash01(x, 71) > 0.5 else f2)
            if hash01(x, 73) > 0.8:
                put(px, img, x + 1, y + 1, f1)
    # die Krone aus sieben Kristallsplittern über dem Medaillon (die mittlere am höchsten)

    def shard(cx: int, base: int, h: int, w: int, lean: float):
        for k2 in range(h):
            t = k2 / max(1, h - 1)
            half = max(0, round((w / 2) * (1 - t)))
            off = round(lean * k2)
            for dx in range(-half, half + 1):
                if abs(dx) == half:
                    col = mix(f0, m0, 0.35)
                else:
                    col = f3 if (dx < 0 and t > 0.35) else (f2 if dx <= 0 else f1)
                put(px, img, cx + dx + off, base - k2, col)
    angles = [-90 + (i - 3) * 22 for i in range(7)]
    for i, ang in enumerate(angles):
        a = math.radians(ang)
        x = round(EMB_X + math.cos(a) * (EMB_R - 1))
        y = round(EMB_Y + math.sin(a) * (EMB_R - 1))
        h = 10 if i == 3 else (8 if i in (2, 4) else 6)
        shard(x, y, h, 3 if i != 3 else 4, math.cos(a) * 0.55)
    # goldener Reif um das Medaillon (oben)
    for ang in range(-170, -9, 4):
        a = math.radians(ang)
        put(px, img, round(EMB_X + math.cos(a) * (EMB_R + 0.2)), round(EMB_Y + math.sin(a) * (EMB_R + 0.2)),
            gold if ang < -90 else gold_dark)
    # Spitze: das zersprungene Herz – zwei Kristallhälften, dazwischen ein leuchtender Spalt
    ex, ey = BAR_X + BAR_W + 3, BAR_Y + BAR_H // 2
    for kx in range(12):
        half = round(4.6 * math.sin(math.pi * (kx + 0.5) / 12) + (1.2 if kx in (2, 3, 8, 9) else 0.0))
        for dy in range(-half, half + 1):
            gap = abs(kx - 6 + (1 if dy > 0 else 0) * (dy % 2)) <= 0
            if abs(dy) == half:
                col = OUTLINE
            elif gap:
                col = f3
            else:
                t = (dy + half) / max(1, 2 * half)
                col = mix(f2, f0, t) if kx < 6 else mix(f1, f0, t * 0.8)
                if kx in (2, 3) and dy == -half + 1:
                    col = f3
            put(px, img, ex + kx, ey + dy, col)


def deco_reef(img: Image.Image, pal: dict) -> None:
    """Ein Korallenriff wächst auf dem Rahmen (am Medaillon hoch und dicht, nach rechts niedriger, am Ende nur noch
    Muscheln und ein Seestern): Geweihkorallen, Gorgonienfächer, Tischkorallen, Hirnkorallen, Röhrenschwämme, Seegras
    und Anemonen, dazwischen zwei kleine Fische. Unter der Leiste hängen Seepocken und Rotalgen, Blasen steigen auf.
    Um das Medaillon ein Kranz aus Korallenästen mit Goldreif, als Spitze die geöffnete Schere des Karkinos."""
    px = img.load()
    f1, f2, f3 = (c(pal, k) for k in ("f1", "f2", "f3"))
    m0, m1, m2 = c(pal, "m0"), c(pal, "m1"), c(pal, "m2")
    gold, gold_dark = c(pal, "gold"), c(pal, "gold_dark")
    coral, coral_dark, coral_light = c(pal, "coral"), c(pal, "coral_dark"), c(pal, "coral_light")
    fan, fan_dark = c(pal, "fan"), c(pal, "fan_dark")
    sponge, sponge_dark = c(pal, "sponge"), c(pal, "sponge_dark")
    brain, brain_dark = c(pal, "brain"), c(pal, "brain_dark")
    anemone, anemone_dark = c(pal, "anemone"), c(pal, "anemone_dark")
    star_col = c(pal, "star")
    white = (255, 250, 236, 255)
    top, bottom = BAR_Y - 3, BAR_Y + BAR_H + 2
    base = top - 1                                   # unterste Zeile über dem Rahmen

    def line(x0: float, y0: float, x1: float, y1: float, col0, col1, width: int = 1):
        n = max(1, round(max(abs(x1 - x0), abs(y1 - y0))))
        for k in range(n + 1):
            t = k / n
            x, y = round(x0 + (x1 - x0) * t), round(y0 + (y1 - y0) * t)
            col = mix(col0, col1, t)
            put(px, img, x, y, col)
            if width > 1:
                put(px, img, x + 1, y, mix(col, OUTLINE, 0.35))

    def staghorn(x: float, y: float, ang: float, length: float, depth: int, seed: int, palette=None):
        """Geweihkoralle: dicker Stamm, Äste verzweigen sich, an den Spitzen helle Polypen."""
        dark, mid, light = palette or (coral_dark, coral, coral_light)
        x1, y1 = x + math.cos(ang) * length, y + math.sin(ang) * length
        line(x, y, x1, y1, mix(dark, mid, 0.4) if depth >= 2 else mid, mid if depth else light, 2 if depth >= 2 else 1)
        if depth == 0:
            put(px, img, round(x1), round(y1), light)
            put(px, img, round(x1), round(y1) - 1 if math.sin(ang) < 0 else round(y1) + 1, white)
            return
        spread = 0.5 + 0.25 * hash01(seed, 1)
        staghorn(x1, y1, ang - spread, length * 0.7, depth - 1, seed * 3 + 1, palette)
        staghorn(x1, y1, ang + spread * 0.9, length * 0.62, depth - 1, seed * 3 + 2, palette)
        if depth >= 2 and hash01(seed, 4) > 0.4:
            staghorn((x + x1) / 2, (y + y1) / 2, ang + (spread + 0.3) * (1 if hash01(seed, 5) > 0.5 else -1),
                     length * 0.45, 0, seed * 3 + 3, palette)

    def gorgonian(cx: int, r: int, seed: int):
        """Gorgonienfächer, von vorn gesehen: fünf feine Adern strahlen vom Fuß aus und verzweigen sich, am Rand
        hellere Polypen – dazwischen bleibt der Fächer offen."""
        for k in range(5):
            a = math.radians(-150 + k * 30 + (hash01(seed, k) - 0.5) * 10)
            mx, my = cx + math.cos(a) * r * 0.55, base + math.sin(a) * r * 0.5
            line(cx, base, mx, my, fan_dark, mix(fan_dark, fan, 0.6))
            for side in (-0.28, 0.28):
                ex, ey = cx + math.cos(a + side) * r, base + math.sin(a + side) * r * 0.92
                line(mx, my, ex, ey, mix(fan_dark, fan, 0.6), fan)
                put(px, img, round(ex), round(ey), mix(fan, white, 0.5))
        put(px, img, cx, base, fan_dark)

    def table_coral(cx: int, h: int, w: int, seed: int):
        """Tischkoralle: dünner Fuß, oben eine flache, gewellte Platte."""
        for k in range(h):
            put(px, img, cx, base - k, mix(f1, OUTLINE, 0.35))
        for dx in range(-w, w + 1):
            y = base - h - (1 if abs(dx) < w - 1 and hash01(seed, dx) > 0.7 else 0)
            put(px, img, cx + dx, y, mix(f2, white, 0.25) if dx < 0 else f2)
            put(px, img, cx + dx, y + 1, mix(f1, OUTLINE, 0.25) if abs(dx) < w else OUTLINE)
        put(px, img, cx - w, base - h - 1, mix(f2, white, 0.5))

    def brain_coral(cx: int, w: int, h: int, seed: int):
        """Hirnkoralle: Kuppel mit gewundenen Furchen, oben links im Licht."""
        for dy in range(h + 1):
            for dx in range(-w, w + 1):
                e = (dx / (w + 0.5)) ** 2 + (dy / (h + 0.4)) ** 2
                if e > 1.0:
                    continue
                if e > 0.78:
                    col = mix(brain_dark, OUTLINE, 0.25)
                elif (dx + round(1.6 * math.sin(dy * 1.9 + seed))) % 3 == 0:
                    col = brain_dark
                else:
                    col = mix(brain, white, 0.35) if (dx < 0 and dy > h // 2) else brain
                put(px, img, cx + dx, base - dy, col)

    def sponge_tubes(cx: int, heights):
        """Röhrenschwämme: je zwei Pixel breit, oben eine dunkle Öffnung."""
        for i, h in enumerate(heights):
            x = cx + i * 3
            for k in range(h):
                t = k / max(1, h - 1)
                put(px, img, x, base - k, mix(sponge, (255, 236, 170, 255), 0.4 * t))
                put(px, img, x + 1, base - k, sponge_dark if k < h - 1 else sponge)
            put(px, img, x, base - h, mix(sponge, white, 0.4))
            put(px, img, x + 1, base - h, sponge)
            put(px, img, x, base - h + 1, OUTLINE)
            put(px, img, x + 1, base - h + 1, mix(sponge_dark, OUTLINE, 0.5))

    def anemone_tuft(cx: int, h: int, seed: int, pink: bool = False):
        """Anemone: kurzer Fuß, darüber ein Büschel geschwungener Tentakel mit leuchtenden Spitzen."""
        light = (255, 150, 200, 255) if pink else anemone
        dark = (120, 30, 80, 255) if pink else anemone_dark
        put(px, img, cx, base, dark)
        put(px, img, cx + 1, base, dark)
        put(px, img, cx, base - 1, mix(dark, light, 0.4))
        put(px, img, cx + 1, base - 1, mix(dark, light, 0.4))
        for i, lean in enumerate((-1.4, -0.8, -0.25, 0.25, 0.8, 1.4)):
            x = cx + 0.5 + lean * 0.5
            hh = h - abs(round(lean))
            for k in range(hh):
                x += lean * (0.18 + 0.12 * k) + 0.3 * math.sin(seed + k * 1.3 + i)
                put(px, img, round(x), base - 2 - k, mix(dark, light, 0.3 + 0.7 * k / max(1, hh - 1)))
            put(px, img, round(x), base - 2 - hh, f3)

    def seagrass(cx: int, h: int, seed: int):
        """Seegras: zwei, drei schmale Halme, die in der Strömung schwanken."""
        for b in range(2 + (1 if hash01(seed, 9) > 0.5 else 0)):
            x = cx + b * 2
            for k in range(h - b * 2):
                sway = round(1.2 * math.sin(k * 0.7 + seed + b))
                col = mix((40, 96, 40, 255), (150, 210, 90, 255), k / max(1, h - 1))
                put(px, img, x + sway, base - k, col)

    def scallop(cx: int):
        for dy in range(3):
            half = 2 if dy < 2 else 1
            for dx in range(-half, half + 1):
                col = mix(m2, coral_light, 0.4) if dx % 2 == 0 else mix(m1, coral, 0.35)
                put(px, img, cx + dx, base - dy, col)
        put(px, img, cx - 2, base - 2, OUTLINE if False else mix(m2, coral_light, 0.2))

    def starfish(cx: int, cy: int):
        for k in range(5):
            a = -math.pi / 2 + k * math.tau / 5
            for r in range(1, 3):
                put(px, img, round(cx + math.cos(a) * r), round(cy + math.sin(a) * r), star_col if r < 2 else coral)
        put(px, img, cx, cy, mix(star_col, (255, 230, 160, 255), 0.5))

    def fish(x: int, y: int, body, stripe, facing: int):
        """Ein kleiner Rifffisch (4 x 3 Pixel) mit Schwanzflosse."""
        for dx in range(4):
            for dy in (-1, 0, 1):
                if abs(dy) == 1 and dx in (0, 3):
                    continue
                col = stripe if dx == 2 else body
                put(px, img, x + dx * facing, y + dy, col)
        put(px, img, x + (3 if facing > 0 else -3), y - 1 if False else y, OUTLINE)        # Auge vorn
        put(px, img, x - facing, y - 1, body)
        put(px, img, x - facing, y + 1, body)

    # Fuß des Riffs: Geröll aus Kalk und Korallenbruch auf dem Rahmen (Lücken zwischen den Gruppen)
    def rubble(x0: int, x1: int):
        for x in range(x0, x1):
            col = mix(m1, m2, 0.25 + 0.5 * hash01(x, 3)) if hash01(x, 2) > 0.25 else mix(coral_dark, coral, 0.5)
            put(px, img, x, base, col)
            if hash01(x, 4) > 0.55:
                put(px, img, x, base - 1, mix(m1, m2, 0.5 * hash01(x, 6)))

    # das Riff: Gruppen von zwei, drei Korallen, nach rechts niedriger und lichter
    groups = []
    x = BAR_X + 1
    i = 0
    while x < BAR_X + BAR_W - 6:
        rel = (x - BAR_X) / BAR_W
        width = 13 + round(hash01(x, 1) * 6) - round(rel * 5)
        if any(abs(x + width / 2 - (BAR_X + round(BAR_W * f))) < width / 2 + 6 for f in GEMS):
            x += 4
            continue
        groups.append((x, width, rel, i))
        x += width + 2 + round(rel * 6)
        i += 1
    for gx, width, rel, i in groups:
        if rel < 0.86:
            rubble(gx, gx + width)
    for gx, width, rel, i in groups:
        scale = 1.0 - 0.55 * rel
        kind = i % 5
        seed = gx * 7 + i
        if rel >= 0.86:
            scallop(gx + 2)
            if i % 2 == 0:
                anemone_tuft(gx + 7, 4, seed, pink=True)
            continue
        if kind == 0:
            staghorn(gx + 4, base, -math.pi / 2 - 0.15, 4.2 * scale + 1.0, 2, seed)
            anemone_tuft(gx + 9, max(3, round(5 * scale)), seed)
        elif kind == 1:
            gorgonian(gx + 6, max(4, round(7 * scale)), seed)
            seagrass(gx + 12, max(4, round(8 * scale)), seed)
        elif kind == 2:
            brain_coral(gx + 4, 4 if scale > 0.7 else 3, 3, seed)
            sponge_tubes(gx + 9, [max(2, round(h * scale)) for h in (7, 9, 5)][: 3 if scale > 0.6 else 2])
        elif kind == 3:
            table_coral(gx + 5, max(3, round(6 * scale)), 5 if scale > 0.7 else 4, seed)
            staghorn(gx + 12, base, -math.pi / 2 + 0.2, 3.2 * scale + 1.0, 1, seed, (fan_dark, fan, (255, 190, 240, 255)))
        else:
            anemone_tuft(gx + 3, max(3, round(6 * scale)), seed, pink=True)
            seagrass(gx + 8, max(4, round(9 * scale)), seed)
            brain_coral(gx + 13, 2, 2, seed)
    starfish(BAR_X + BAR_W - 14, base - 2)
    # zwei kleine Fische: ein Anemonenfisch nahe dem Medaillon, ein blauer Doktorfisch weiter rechts
    fish(BAR_X + round(BAR_W * 0.42), top - 11, (255, 128, 30, 255), white, 1)
    fish(BAR_X + round(BAR_W * 0.74), top - 8, (60, 120, 230, 255), (255, 214, 60, 255), -1)
    # Seepocken und Rotalgen unter der Leiste, aufsteigende Blasen
    for x in range(BAR_X + 3, BAR_X + BAR_W - 3, 7):
        h = hash01(x, 19)
        if h > 0.62:
            for dx in range(-1, 2):
                put(px, img, x + dx, bottom + 1, mix(m2, m1, 0.2 + 0.25 * (dx + 1)))
            put(px, img, x, bottom + 2, mix(m1, m0, 0.3))
            put(px, img, x - 1, bottom + 2, mix(m2, m1, 0.5))
            put(px, img, x, bottom + 1, mix(m0, OUTLINE, 0.5))                 # Öffnung
        elif h > 0.4:
            length = 3 + round(hash01(x, 21) * 4)
            for k in range(length):
                sway = round(0.8 * math.sin(k * 0.9 + x))
                put(px, img, x + sway, bottom + 1 + k, mix(coral_dark, coral, k / max(1, length - 1)))
    for x in range(BAR_X + 9, BAR_X + BAR_W, 14):
        y = bottom + 6 + round(hash01(x, 23) * 6)
        if hash01(x, 29) > 0.5:
            for dx, dy in ((0, -1), (-1, 0), (1, 0), (0, 1)):
                put(px, img, x + dx, y + dy, f2)
            put(px, img, x - 1, y - 1, f3)
            put(px, img, x + 2, y - 4, f2)
        else:
            put(px, img, x, y, f2)
            put(px, img, x + 1, y - 2, f3)
    # Kranz aus Korallenästen um das Medaillon, darüber ein Goldreif
    for k, ang in enumerate((-158, -136, -112, -68, -44, -22)):
        a = math.radians(ang)
        bx = EMB_X + math.cos(a) * (EMB_R - 0.5)
        by = EMB_Y + math.sin(a) * (EMB_R - 0.5)
        staghorn(bx, by, a, 3.6, 1, 50 + k)
    for ang in range(-178, -1, 3):
        a = math.radians(ang)
        put(px, img, round(EMB_X + math.cos(a) * (EMB_R + 0.3)), round(EMB_Y + math.sin(a) * (EMB_R + 0.3)),
            gold if ang < -90 else gold_dark)
    # Spitze: die geöffnete Schere des Karkinos
    claw = ["OO.....",
            "LLOO...",
            "HLLLOO.",
            "LLLLLLO",
            "CCCCCLO",
            "CCOOOWO",
            "CO.....",
            "CCOOWO.",
            "DCCCCO.",
            "DDDDO..",
            "OOOO..."]
    cols = {"O": OUTLINE, "L": coral_light, "H": white, "C": coral, "D": coral_dark, "W": (60, 30, 28, 255)}
    ex, ey = BAR_X + BAR_W + 3, BAR_Y + BAR_H // 2 - 6
    for dy, row in enumerate(claw):
        for dx, ch in enumerate(row):
            if ch != ".":
                put(px, img, ex + dx, ey + dy, cols[ch])


def deco_moth(img: Image.Image, pal: dict) -> None:
    """Hinter dem Medaillon breitet die Mottenkönigin ihre Flügel aus (Vorderflügel nach oben, Hinterflügel mit langen
    Schwänzen nach unten, mit Augenflecken); oben auf dem Rahmen sitzen gefiederte Fühler und kleine Mondsicheln,
    unter der Leiste hängen Seidenfäden mit Tautropfen; zwei kleine Falter schwirren über der Leiste. Als Spitze eine
    liegende Mondsichel mit Stern."""
    px = img.load()
    f2, f3 = c(pal, "f2"), c(pal, "f3")
    gold, gold_dark = c(pal, "gold"), c(pal, "gold_dark")
    wing, wing_dark, wing_light = c(pal, "wing"), c(pal, "wing_dark"), c(pal, "wing_light")
    spot, spot_dark = c(pal, "spot"), c(pal, "spot_dark")
    silk, silk_dark = c(pal, "silk"), c(pal, "silk_dark")
    m1 = c(pal, "m1")
    top, bottom = BAR_Y - 3, BAR_Y + BAR_H + 2

    def wing_shape(cx: float, cy: float, rx: float, ry: float, ang: float, tail: float):
        """Ein Flügel als gedrehte Ellipse, der Rand hell, innen Adern, ein Augenfleck; optional ein Schwanz."""
        ca, sa = math.cos(ang), math.sin(ang)
        for y in range(FRAME_H):
            for x in range(FRAME_W):
                dx, dy = x + 0.5 - cx, y + 0.5 - cy
                u = (dx * ca + dy * sa) / rx
                v = (-dx * sa + dy * ca) / ry
                d = u * u + v * v
                if d > 1.0 or u < 0.0:
                    continue
                col = mix(wing_dark, wing, 0.35 + 0.65 * u)
                if d > 0.72:
                    col = wing_light if d < 0.86 else mix(wing_dark, OUTLINE, 0.3)
                elif abs(math.sin(math.atan2(v, u) * 5.0)) < 0.12:
                    col = mix(wing_dark, wing, 0.3)
                es = (u - 0.62) ** 2 + (v * 1.2) ** 2
                if es < 0.012:
                    col = f3
                elif es < 0.03:
                    col = spot
                elif es < 0.05:
                    col = gold
                elif es < 0.07:
                    col = spot_dark
                put(px, img, x, y, col)
        if tail > 0:
            tx, ty = cx + ca * rx * 0.9, cy + sa * rx * 0.9
            for k in range(int(tail)):
                t = k / max(1, tail - 1)
                put(px, img, round(tx + ca * k * 0.8 + t * t * 3.0), round(ty + sa * k * 0.8), wing if k < tail - 1 else wing_light)

    # Flügel, die hinter dem Medaillon hervorkommen: oben der Vorderflügel, unten der Hinterflügel mit Schwanz
    wing_shape(EMB_X + 13, EMB_Y - 8, 27, 8.5, math.radians(-17), 0)
    wing_shape(EMB_X + 12, EMB_Y + 8, 19, 6.0, math.radians(20), 8)
    # gefiederte Fühler über dem Medaillon
    for side in (-1, 1):
        x0, y0 = EMB_X + side * 4, EMB_Y - EMB_R + 1
        for k in range(9):
            x = round(x0 + side * k * 0.6)
            y = y0 - k
            put(px, img, x, y, m1 if k < 8 else gold)
            if 1 < k < 8 and k % 2 == 0:
                put(px, img, x - side, y, mix(m1, silk, 0.4))
                put(px, img, x + side, y + 1, mix(m1, silk, 0.25))
    # kleine Mondsicheln auf dem Rahmen (nach rechts offen), daneben ein Stern
    crescent = (".gg.", "g..o", "g...", "g..o", ".gg.")
    for x0 in range(BAR_X + 34, BAR_X + BAR_W - 10, 38):
        for dy, row in enumerate(crescent):
            for dx, ch in enumerate(row):
                if ch == "g":
                    put(px, img, x0 + dx, top - 7 + dy, gold if dy < 3 else gold_dark)
                elif ch == "o":
                    put(px, img, x0 + dx, top - 7 + dy, mix(gold, f3, 0.5))
        put(px, img, x0 + 6, top - 8, f3)
    # Seidenfäden mit Tautropfen unter der Leiste
    for x in range(BAR_X + 11, BAR_X + BAR_W - 4, 17):
        length = 3 + round(hash01(x, 41) * 7)
        for k in range(length):
            put(px, img, x, bottom + 1 + k, silk if k % 4 else silk_dark)
        put(px, img, x, bottom + 1 + length, f3)
        put(px, img, x, bottom + 2 + length, f2)
    # zwei kleine Falter über der Leiste
    for (mx, my) in ((BAR_X + 70, top - 9), (BAR_X + 140, top - 7)):
        for dx, dy, col in ((-2, -1, wing), (-1, -1, wing_light), (-1, 0, wing), (0, 0, silk_dark), (1, 0, wing), (1, -1, wing_light),
                            (2, -1, wing), (-1, 1, wing_dark), (1, 1, wing_dark), (0, -1, silk_dark)):
            put(px, img, mx + dx, my + dy, col)
    # Spitze: eine liegende Mondsichel mit Stern
    ex, ey = BAR_X + BAR_W + 2, BAR_Y + BAR_H // 2
    for k in range(9):
        a = math.radians(-120 + k * 30)
        put(px, img, round(ex + 3 + math.cos(a) * 4), round(ey + math.sin(a) * 4), gold if 1 <= k <= 7 else gold_dark)
        put(px, img, round(ex + 3 + math.cos(a) * 3), round(ey + math.sin(a) * 3), mix(gold, f3, 0.4) if 2 <= k <= 6 else gold_dark)
    put(px, img, ex + 7, ey - 1, f3)
    put(px, img, ex + 7, ey, f2)


def deco_forge(img: Image.Image, pal: dict) -> None:
    """Hinter dem Medaillon gekreuzt: der Schmiedehammer (Kopf oben links) und der Glutspeer (glühende Klinge oben
    rechts); oben auf dem Rahmen kleine Glutzungen mit aufstiebenden Funken, unter der Leiste tropft geschmolzenes Metall
    (die Tropfen glühen); als Spitze die Klinge des Glutspeers."""
    px = img.load()
    f1, f2, f3 = c(pal, "f1"), c(pal, "f2"), c(pal, "f3")
    gold, gold_dark = c(pal, "gold"), c(pal, "gold_dark")
    iron, iron_light = c(pal, "iron"), c(pal, "iron_light")
    ember, ember_dark, wood = c(pal, "ember"), c(pal, "ember_dark"), c(pal, "wood")
    m1 = c(pal, "m1")
    top, bottom = BAR_Y - 3, BAR_Y + BAR_H + 2

    def stick(x0: float, y0: float, x1: float, y1: float, width: float, col_fn):
        length = math.hypot(x1 - x0, y1 - y0)
        ax, ay = (x1 - x0) / length, (y1 - y0) / length
        for y in range(FRAME_H):
            for x in range(FRAME_W):
                dx, dy = x + 0.5 - x0, y + 0.5 - y0
                t = dx * ax + dy * ay
                off = -dx * ay + dy * ax
                if 0 <= t <= length:
                    w = width(t / length) if callable(width) else width
                    if abs(off) <= w:
                        put(px, img, x, y, col_fn(t / length, off, w))

    # der Speer: von unten links nach oben rechts, die Klinge glüht
    def spear_col(t, off, w):
        if t < 0.72:
            return OUTLINE if abs(off) > w - 0.6 else (wood if off < 0 else mix(wood, OUTLINE, 0.4))
        if t < 0.76:
            return gold if off < 0 else gold_dark
        if abs(off) > w - 0.6:
            return ember_dark
        return f3 if abs(off) < 0.6 else (f2 if off < 0 else ember)
    stick(EMB_X - 16, EMB_Y + 22, EMB_X + 40, EMB_Y - 21, lambda u: 1.1 if u < 0.72 else (2.6 if u < 0.76 else 2.6 * (1 - (u - 0.76) / 0.24) ** 0.8 + 0.3),
          spear_col)
    # der Hammer: von unten rechts nach oben links, oben der Kopf quer
    def handle_col(t, off, w):
        return OUTLINE if abs(off) > w - 0.6 else (mix(wood, iron_light, 0.15) if off < 0 else wood)
    stick(EMB_X + 17, EMB_Y + 21, EMB_X - 13, EMB_Y - 17, 1.1, handle_col)
    hx, hy = EMB_X - 13, EMB_Y - 17
    for y in range(-6, 7):
        for x in range(-6, 7):
            u = (x - y) / 1.414
            v = (x + y) / 1.414
            if abs(u) <= 5.2 and abs(v) <= 2.6:
                edge = abs(u) > 4.4 or abs(v) > 1.9
                face = abs(u) > 4.0
                col = OUTLINE if edge and not face else (ember if face else (iron_light if v < -0.6 else iron))
                put(px, img, hx + x, hy + y, col)
    # Glutzungen und Funken oben auf dem Rahmen
    for x in range(BAR_X + 6, BAR_X + BAR_W - 2, 11):
        hgt = 2 + round(hash01(x, 13) * 3)
        for k in range(hgt):
            t = k / max(1, hgt - 1)
            col = f1 if t < 0.4 else (f2 if t < 0.8 else f3)
            put(px, img, x, top - 1 - k, col)
            if k == 0:
                put(px, img, x - 1, top - 1, ember_dark)
                put(px, img, x + 1, top - 1, ember_dark)
        if hash01(x, 17) > 0.45:
            put(px, img, x + round(hash01(x, 19) * 4) - 2, top - hgt - 2 - round(hash01(x, 23) * 3), f2 if hash01(x, 29) > 0.5 else f3)
    # geschmolzenes Metall tropft von der Leiste
    for x in range(BAR_X + 9, BAR_X + BAR_W - 4, 13):
        length = 2 + round(hash01(x, 31) * 4)
        for k in range(length):
            put(px, img, x, bottom + 1 + k, ember_dark if k < length - 2 else ember)
        put(px, img, x, bottom + 1 + length, f2)
        put(px, img, x, bottom + 2 + length, f3)
        put(px, img, x + 1, bottom + 1, ember_dark)
    # Spitze: die Klinge des Glutspeers nach rechts
    ex, ey = BAR_X + BAR_W + 3, BAR_Y + BAR_H // 2
    for k in range(12):
        half = round(4.2 * (1 - k / 12) ** 0.75) if k > 1 else 2
        for dy in range(-half, half + 1):
            if abs(dy) == half:
                col = OUTLINE if k > 1 else gold_dark
            elif k <= 1:
                col = gold
            else:
                col = f3 if dy == 0 else (f2 if dy < 0 else ember)
            put(px, img, ex + k, ey + dy, col)
    put(px, img, ex + 12, ey, ember_dark)
    _ = m1


def deco_star(img: Image.Image, pal: dict) -> None:
    """Hinter dem Medaillon breitet der Sternenrochen seine Schwingen aus (Nachthimmel mit Sternen, violette Vorderkante,
    nach außen spitz zulaufend); schräg dahinter die Sternenlanze mit leuchtender Kristallspitze. Oben auf dem Rahmen
    funkeln kleine vierzackige Sterne, unter der Leiste fallen Sternschnuppen; als Spitze ein Sternkristall."""
    px = img.load()
    f2, f3 = c(pal, "f2"), c(pal, "f3")
    gold, gold_dark = c(pal, "gold"), c(pal, "gold_dark")
    night, night_dark = c(pal, "night"), c(pal, "night_dark")
    nebula, nebula_light = c(pal, "nebula"), c(pal, "nebula_light")
    silver, silver_dark, crystal = c(pal, "silver"), c(pal, "silver_dark"), c(pal, "crystal")
    top, bottom = BAR_Y - 3, BAR_Y + BAR_H + 2

    def wing(cx: float, cy: float, length: float, lead: float, trail: float, ang: float):
        """Ein Rochenflügel ab (cx, cy) nach rechts: vorn die Vorderkante (Neigung lead), hinten geschwungen zur Spitze."""
        ca, sa = math.cos(ang), math.sin(ang)
        for y in range(FRAME_H):
            for x in range(FRAME_W):
                dx, dy = x + 0.5 - cx, y + 0.5 - cy
                u = dx * ca + dy * sa
                v = -dx * sa + dy * ca
                if u < 0.0 or u > length:
                    continue
                k = u / length
                v_lead = -lead * (1.0 - k)
                v_trail = trail * (1.0 - k) ** 1.4 - 1.2 * math.sin(math.pi * k)
                if v < v_lead or v > v_trail:
                    continue
                edge = v < v_lead + 1.0
                rim = v > v_trail - 0.9 or u > length - 1.2
                col = mix(night_dark, night, 0.4 + 0.6 * (1.0 - k))
                if hash01(x, y, 131) > 0.93:
                    col = f3 if hash01(x, y, 137) > 0.5 else crystal
                if edge:
                    col = nebula_light if v < v_lead + 0.5 else nebula
                elif rim:
                    col = mix(nebula, OUTLINE, 0.35)
                put(px, img, x, y, col)

    wing(EMB_X + 10, EMB_Y - 9, 34, 6.0, 9.0, math.radians(-24))
    # die Lanze: von unten links nach oben rechts hinter dem Medaillon, die Kristallspitze leuchtet
    x0, y0, x1, y1 = EMB_X - 17, EMB_Y + 20, EMB_X + 30, EMB_Y - 21
    length = math.hypot(x1 - x0, y1 - y0)
    ax, ay = (x1 - x0) / length, (y1 - y0) / length
    for y in range(FRAME_H):
        for x in range(FRAME_W):
            dx, dy = x + 0.5 - x0, y + 0.5 - y0
            t = (dx * ax + dy * ay) / length
            off = -dx * ay + dy * ax
            if not 0.0 <= t <= 1.0:
                continue
            if t < 0.8:
                w = 1.1
                if abs(off) > w:
                    continue
                col = OUTLINE if abs(off) > w - 0.55 else ((gold if off < 0 else gold_dark) if (t * 26) % 1.0 < 0.22 else
                                                          (silver if off < 0 else silver_dark))
            elif t < 0.84:
                w = 2.7
                if abs(off) > w:
                    continue
                col = OUTLINE if abs(off) > w - 0.6 else (gold if off < 0 else gold_dark)
            else:
                u = (t - 0.84) / 0.16
                w = 2.4 * max(0.0, 1.0 - u) ** 0.8 + 0.3
                if abs(off) > w:
                    continue
                col = mix(crystal, OUTLINE, 0.4) if abs(off) > w - 0.6 else (f3 if abs(off) < 0.7 else crystal)
            put(px, img, x, y, col)

    def star(sx: int, sy: int, r: int, col, core):
        for k in range(-r, r + 1):
            shade = col if abs(k) < r else mix(col, OUTLINE, 0.3)
            put(px, img, sx + k, sy, shade)
            put(px, img, sx, sy + k, shade)
        put(px, img, sx, sy, core)

    # kleine Sterne oben auf dem Rahmen
    for x in range(BAR_X + 14, BAR_X + BAR_W - 6, 27):
        big = hash01(x, 151) > 0.5
        star(x, top - 4 - round(hash01(x, 157) * 2), 2 if big else 1, crystal, f3)
    # Sternschnuppen unter der Leiste (schräg fallend, mit Schweif)
    for x in range(BAR_X + 18, BAR_X + BAR_W - 6, 31):
        length = 4 + round(hash01(x, 163) * 4)
        for k in range(length):
            t = k / max(1, length - 1)
            put(px, img, x - k, bottom + 2 + k // 2, mix(f2, nebula, t * 0.6) if k else f3)
        put(px, img, x + 1, bottom + 2, crystal)
    # Spitze: ein Sternkristall nach rechts
    ex, ey = BAR_X + BAR_W + 3, BAR_Y + BAR_H // 2
    for k in range(11):
        half = round(3.6 * (1 - k / 11) ** 0.75) if k > 1 else 2
        for dy in range(-half, half + 1):
            if abs(dy) == half:
                col = OUTLINE if k > 1 else gold_dark
            elif k <= 1:
                col = gold
            else:
                col = f3 if dy == 0 else (crystal if dy < 0 else f2)
            put(px, img, ex + k, ey + dy, col)
    for dy in (-5, -4, 4, 5):
        put(px, img, ex + 4, ey + dy, crystal if abs(dy) == 4 else OUTLINE)
    put(px, img, ex + 11, ey, f2)


def deco_mystic(img: Image.Image, pal: dict) -> None:
    """Hinter dem Medaillon ein Fächer aus fünf Zauberkarten (weiß mit Goldrand, rote und schwarze Farben), schräg dahinter
    ihr Zauberstab (schwarz mit weißen Spitzen); oben auf dem Rahmen die vier Kartenfarben, unter der Leiste rieselt
    Konfetti. Als Spitze die weiße Spitze des Zauberstabs mit einem Funkelstern."""
    px = img.load()
    f2, f3 = c(pal, "f2"), c(pal, "f3")
    gold, gold_dark = c(pal, "gold"), c(pal, "gold_dark")
    card, card_dark = c(pal, "card"), c(pal, "card_dark")
    red, black, wand = c(pal, "red"), c(pal, "black"), c(pal, "wand")
    confetti = [c(pal, "acc"), gold, c(pal, "confetti"), f2, card]
    top, bottom = BAR_Y - 3, BAR_Y + BAR_H + 2
    pips = {"heart": (".#.#.", "#####", "#####", ".###.", "..#.."),
            "spade": ("..#..", ".###.", "#####", "#####", "..#.."),
            "diamond": ("..#..", ".###.", "#####", ".###.", "..#.."),
            "club": (".###.", ".###.", "#####", "#.#.#", "..#..")}

    # der Zauberstab: von unten links nach oben rechts hinter dem Medaillon
    x0, y0, x1, y1 = EMB_X - 15, EMB_Y + 19, EMB_X + 34, EMB_Y - 20
    length = math.hypot(x1 - x0, y1 - y0)
    for k in range(int(length) + 1):
        t = k / length
        x = x0 + (x1 - x0) * t
        y = y0 + (y1 - y0) * t
        tip = t < 0.08 or t > 0.9
        for o in (-1, 0, 1):
            col = OUTLINE if o != 0 else (card if tip else wand)
            put(px, img, round(x + o * 0.7), round(y + o * 0.7), col)

    def card_at(cx: float, cy: float, ang: float, suit: str):
        """Eine kleine Karte (8 x 11) um (cx, cy) gedreht: Goldrand, weiße Fläche, die Farbe in der Mitte – nie über der
        Leiste (die bleibt frei)."""
        ca, sa = math.cos(ang), math.sin(ang)
        w, h = 4.0, 5.6
        red_suit = suit in ("heart", "diamond")
        for y in range(FRAME_H):
            for x in range(FRAME_W):
                if x >= BAR_X - 3 and y >= BAR_Y - 3:
                    continue
                dx, dy = x + 0.5 - cx, y + 0.5 - cy
                u = dx * ca + dy * sa
                v = -dx * sa + dy * ca
                if abs(u) > w or abs(v) > h:
                    continue
                if abs(u) > w - 0.9 or abs(v) > h - 0.9:
                    col = OUTLINE
                elif abs(u) > w - 1.8 or abs(v) > h - 1.8:
                    col = gold if (u + v) < 0 else gold_dark
                else:
                    col = mix(card, card_dark, 0.1 + 0.25 * (v + h) / (2 * h))
                    gx, gy = int(math.floor(u + 1.5)), int(math.floor(v + 1.5))
                    small_pip = {"heart": ("#.#", "###", ".#."), "spade": (".#.", "###", "#.#"),
                                 "diamond": (".#.", "###", ".#."), "club": (".#.", "###", ".#.")}[suit]
                    if 0 <= gx < 3 and 0 <= gy < 3 and small_pip[gy][gx] == "#":
                        col = red if red_suit else black
                put(px, img, x, y, col)

    # ein Fächer aus vier Karten steigt hinter dem Medaillon nach rechts oben auf
    for i, suit in enumerate(("club", "diamond", "spade", "heart")):
        ang = math.radians(-35 + i * 20)
        card_at(EMB_X + 17 + i * 6.0, EMB_Y - 12 + abs(i - 1.5) * 1.2, ang, suit)

    # die vier Kartenfarben auf dem Rahmen
    small = {"heart": ("#.#", "###", ".#."), "spade": (".#.", "###", "#.#"), "diamond": (".#.", "###", ".#."),
             "club": (".#.", "###", ".#.")}
    order = ("spade", "heart", "club", "diamond")
    for k, x in enumerate(range(BAR_X + 12, BAR_X + BAR_W - 6, 22)):
        suit = order[k % 4]
        col = red if suit in ("heart", "diamond") else card
        for gy, row in enumerate(small[suit]):
            for gx, ch in enumerate(row):
                if ch == "#":
                    put(px, img, x + gx - 1, top - 3 + gy, col)
        for gx, gy in ((-2, -1), (2, -1), (-2, 3), (2, 3)):
            put(px, img, x + gx, top - 3 + gy, gold_dark)
    # Konfetti unter der Leiste
    for x in range(BAR_X + 4, BAR_X + BAR_W - 4):
        h = hash01(x, 977)
        if h > 0.82:
            y = bottom + 1 + int(hash01(x, 979) * 5)
            put(px, img, x, y, confetti[int(hash01(x, 983) * len(confetti)) % len(confetti)])
            if h > 0.93:
                put(px, img, x + 1, y, confetti[int(hash01(x, 991) * len(confetti)) % len(confetti)])
    # Spitze: die weiße Spitze des Zauberstabs mit Funkelstern
    ex, ey = BAR_X + BAR_W + 2, BAR_Y + BAR_H // 2
    for k in range(9):
        for dy in (-1, 0, 1):
            col = OUTLINE if abs(dy) == 1 else (card if k > 4 else wand)
            put(px, img, ex + k, ey + dy, col)
    sx, sy = ex + 11, ey
    for d in range(-3, 4):
        put(px, img, sx + d, sy, f3 if abs(d) < 2 else gold)
        put(px, img, sx, sy + d, f3 if abs(d) < 2 else gold)
    put(px, img, sx + 1, sy + 1, gold)
    put(px, img, sx - 1, sy - 1, gold)
    put(px, img, sx + 1, sy - 1, gold)
    put(px, img, sx - 1, sy + 1, gold)


DECOS = {"mystic": deco_mystic, "star": deco_star, "forge": deco_forge, "moth": deco_moth, "void": deco_void, "reef": deco_reef, "shadow": deco_shadow, "fire": deco_fire, "ice": deco_ice, "lightning": deco_lightning, "nature": deco_nature, "earth": deco_earth,
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
    if element == "void":
        # ein Sternennebel: weiche Schleier, darin einzelne helle Sterne
        neb = 0.4 + 0.2 * math.sin(tau * x / 64 + 1.4 * math.sin(tau * y / 9)) + 0.12 * math.sin(tau * x / 32 - tau * y / 18)
        return 1.0 if hash01(x, y, 83) > 0.965 else neb
    if element == "reef":
        # Licht einer Lagune: ein schwankendes Netz heller Linien, dazwischen einzelne Blasen
        line1 = abs(math.sin(math.pi * (x / 16 + 0.35 * math.sin(tau * (y / 18 + x / 64)))))
        line2 = abs(math.sin(math.pi * (-x / 16 + y / 4.5 + 0.3 * math.sin(tau * x / 32))))
        near = min(line1, line2)
        if hash01(x, y, 91) > 0.975:
            return 1.0
        return 0.92 if near < 0.14 else (0.66 if near < 0.3 else 0.36 + 0.1 * math.sin(tau * x / 64))
    if element == "star":
        # Nachthimmel: Nebelschleier, die langsam ziehen, darin funkelnde Sterne und ab und zu ein heller Stern
        neb = 0.32 + 0.18 * math.sin(tau * x / 64 + 1.2 * math.sin(tau * y / 9)) + 0.1 * math.sin(tau * x / 32 + tau * y / 18)
        h = hash01(x, y, 59)
        if h > 0.97:
            return 1.0
        if h > 0.94:
            return 0.8
        return neb
    if element == "forge":
        # fließendes, glühendes Metall: langsame Wellen, darin helle Risse und einzelne Funken
        flow = 0.46 + 0.2 * math.sin(tau * x / 64 + 1.3 * math.sin(tau * y / 9 + tau * x / 128))
        crack = abs(math.sin(math.pi * (x / 13 + 0.45 * math.sin(tau * (y / 9 + x / 48)))))
        if hash01(x, y, 47) > 0.975:
            return 1.0
        return 0.9 if crack < 0.1 else (0.7 if crack < 0.22 else flow)
    if element == "moth":
        # Flügelstaub: versetzte Reihen kleiner Schuppen (Bögen), dazwischen Glitzer
        row = y // 3
        sx = (x + (row % 2) * 3) % 6
        arc = abs(sx - 2.5) / 2.5
        scale = 0.42 + 0.3 * (1.0 - arc) * (1.0 if y % 3 else 0.6)
        if hash01(x, y, 61) > 0.97:
            return 1.0
        return scale + 0.08 * math.sin(tau * x / 64)
    if element == "mystic":
        # Harlekin-Rauten aus Samt im Wechsel hell und dunkel, darin funkelnde Pailletten
        a = (x / 8.0 + y / 4.5) % 2.0
        b = (x / 8.0 - y / 4.5) % 2.0
        cell = (int(a) + int(b)) % 2
        edge = min(a % 1.0, 1.0 - a % 1.0, b % 1.0, 1.0 - b % 1.0)
        if hash01(x, y, 67) > 0.972:
            return 1.0
        if edge < 0.07:
            return 0.82
        return 0.62 if cell else 0.36
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
    from entitygen.models import (asteria, boss_earth, boss_fire, boss_ice, boss_lightning, boss_nature, boss_shadow, boss_water,
                                  glutschmied, miss_mystik, moth_queen, reef_colossus, shattered_king)
    from entitygen.paint import paint_model
    from entitygen.preview import render
    mod = {"fire": boss_fire, "ice": boss_ice, "lightning": boss_lightning, "nature": boss_nature, "earth": boss_earth,
           "water": boss_water, "shadow": boss_shadow, "void": shattered_king, "reef": reef_colossus, "moth": moth_queen,
           "forge": glutschmied, "star": asteria, "mystic": miss_mystik}[element]
    m = mod.build()
    m.pack()
    variant = next(iter(mod.PALETTES))
    tex, glow = paint_model(m, mod.materials(variant), mod.PALETTES[variant], seed=100)
    if element == "void":
        # nur Kopf, Bart und Krone (Schultern, Hände und Trümmer würden das kleine Bildnis überladen)
        keep = {"body", "head", "beard", "beard_tip", "crown"} | {f"crown_{k}" for k in range(7)}

        def strip(bone):
            if bone.name not in keep:
                bone.cubes.clear()
            for child in bone.children:
                strip(child)
        strip(m.root)
    if element == "reef":
        # Karkinos von vorn: Panzer mit Riff, Stielaugen, Stirn (ohne Beine, Tangsträhnen und Speer)
        def strip_extra(bone):
            if bone.name.startswith(("kelp", "spear")) or "leg" in bone.name:
                bone.cubes.clear()
            for child in bone.children:
                strip_extra(child)
        strip_extra(m.root)
    if element == "moth":
        # nur Kopf mit Krone und Fühlern, Brust und der Ansatz der Flügel
        def strip_moth(bone):
            if bone.name.startswith(("left_leg", "right_leg", "abdomen", "tail", "left_wing", "right_wing", "left_hind", "right_hind")):
                bone.cubes.clear()
            for child in bone.children:
                strip_moth(child)
        strip_moth(m.root)
    if element == "forge":
        # der Widderschädel mit Hörnern, Flammen und Kiefer (ohne Arme, Waffen und Beine)
        def strip_forge(bone):
            if bone.name.startswith(("left_arm", "right_arm", "left_forearm", "right_forearm", "hammer", "spear", "left_leg",
                                     "right_leg", "apron")):
                bone.cubes.clear()
            for child in bone.children:
                strip_forge(child)
        strip_forge(m.root)
    if element == "star":
        # Asterias Helm mit Krone und Schweif, die Schultern (ohne Rochen, Lanze, Schild und Umhang)
        def strip_star(bone):
            if bone.name not in ("torso", "helm", "crown", "plume", "plume_mid", "plume_tip", "rider"):
                bone.cubes.clear()
            for child in bone.children:
                strip_star(child)
        strip_star(m.root)
    if element == "mystic":
        # Kopf mit Maske, Zylinder, Feder und Pferdeschwanz (ohne Leib, Arme und Flügel)
        def strip_mystic(bone):
            if bone.name not in ("head", "hat", "feather", "ponytail", "ponytail_tip"):
                bone.cubes.clear()
            for child in bone.children:
                strip_mystic(child)
        strip_mystic(m.root)
    zoom = {"nature": 0.82, "earth": 1.15, "water": 1.1, "void": 1.05, "reef": 1.25, "moth": 0.8, "forge": 0.86, "star": 0.92, "mystic": 0.95}.get(element, 1.2)
    yaw = {"water": -50, "earth": -38, "void": -26, "reef": -20, "moth": -8, "forge": -14, "star": -24, "mystic": -22}.get(element, -42)
    pitch = {"reef": 8, "moth": 4, "forge": 4, "star": 4, "mystic": 2}.get(element, 6)
    big = render(m, tex, glow, yaw, pitch, 448, pose=getattr(mod, "FOLD_POSE", None), background=(0, 0, 0, 0), zoom=zoom,
                 focus=["body"] if element == "reef" else ["helm"] if element == "star" else ["head"])
    img = big.resize((64, 64), Image.LANCZOS)
    img = ImageEnhance.Brightness(img).enhance({"fire": 1.9, "earth": 1.5, "lightning": 1.9, "nature": 1.6, "shadow": 2.4,
                                                 "void": 1.0, "reef": 1.35, "moth": 1.25, "forge": 1.65, "star": 1.15, "mystic": 1.2}.get(element, 1.4))
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
