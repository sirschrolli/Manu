#!/usr/bin/env python3
"""
Texturen zu MissMystik, der Feen-Zauberkünstlerin (0.66), im Vanilla-Stil (siehe tools/item_style.py):

  * die Zauberkarten (32 x 48): Rücken in Violett mit Harlekin-Rauten, Goldrand und einem Stern im Medaillon; Vorderseiten
    weiß mit einer großen Farbe (Herz, Pik, Karo, Kreuz) und kleinen Eckzeichen;
  * Konfetti: Streifen, Quadrat, Stern und Kreis (weiß, im Spiel eingefärbt);
  * der große Zauberzylinder auf der Bühne: Seidenfilz mit Glanzstreifen, Deckel, Krempe, Satinband mit goldenem Stern
    (leuchtend und erloschen);
  * der Samtvorhang: schwere Falten in tiefem Violett, dazu der Saum mit goldener Fransenborte;
  * das Kartendeck der Mystik (Bild und Atlas des 3D-Fächers in der Hand) und der Zylinder der Mystik (Bild und Atlas des
    3D-Huts auf dem Kopf).
"""
from __future__ import annotations

import math
from typing import Callable

from PIL import Image

import item_style as st

CLEAR = (0, 0, 0, 0)

VIOLET = st.ramp("#6a3ab0", dark=0.16, light=0.86, cool=290)
FELT = st.ramp("#30243e", dark=0.08, light=0.58, cool=285)
MAGENTA = st.ramp("#d0369a", dark=0.22, light=0.9, cool=330, warm=350)
GOLD = st.ramp("#e8bc4e", dark=0.3, light=0.98, cool=40, warm=75, chroma_light=0.6)
CARD = st.ramp("#f2ecf8", dark=0.55, light=1.0, cool=280)
RED = st.ramp("#d8243a", dark=0.25, light=0.85, cool=10)
INK = st.ramp("#2a2238", dark=0.06, light=0.45, cool=280)
WAND = st.ramp("#1a1820", dark=0.05, light=0.5, cool=270)
TEAL = st.ramp("#3ad0d8", dark=0.3, light=0.95, cool=200)
PINK = st.ramp("#ff8ad0", dark=0.4, light=0.97, cool=330)


def hexc(value: str, alpha: int = 255):
    v = value.lstrip("#")
    return int(v[0:2], 16), int(v[2:4], 16), int(v[4:6], 16), alpha


def mix(a, b, t: float):
    t = max(0.0, min(1.0, t))
    return tuple(int(round(a[i] + (b[i] - a[i]) * t)) for i in range(3)) + (255,)


def noise(x: int, y: int, seed: int) -> float:
    return st.noise(seed, x, y)


def col(r: st.Ramp, level: float):
    """Farbe einer Stufe (0..6, auch Zwischenwerte) aus einer Rampe."""
    level = max(0.0, min(6.0, level))
    lo = int(math.floor(level))
    hi = min(6, lo + 1)
    return mix(r[lo], r[hi], level - lo)


# =============================================================================
# Kartenfarben als Formen (u, v in -1..1, v nach unten)
# =============================================================================

def in_heart(u: float, v: float) -> bool:
    x, y = u * 1.15, -v * 1.15 + 0.15
    return (x * x + y * y - 1.0) ** 3 - x * x * y ** 3 <= 0.0


def in_spade(u: float, v: float) -> bool:
    # zwei Bögen unten, darüber eine Spitze, unten ein Stiel mit breitem Fuß
    for cx in (-0.4, 0.4):
        if (u - cx) ** 2 + (v - 0.12) ** 2 <= 0.42 ** 2:
            return True
    if -1.0 <= v <= 0.18 and abs(u) <= (v + 1.0) * 0.68:
        return True
    if 0.3 < v <= 1.0 and abs(u) <= 0.1 + (v - 0.3) * 0.5:
        return True
    return False


def in_diamond(u: float, v: float) -> bool:
    return abs(u) / 0.72 + abs(v) / 1.0 <= 1.0


def in_club(u: float, v: float) -> bool:
    for cx, cy in ((0.0, -0.45), (-0.45, 0.12), (0.45, 0.12)):
        if (u - cx) ** 2 + (v - cy) ** 2 <= 0.36 ** 2:
            return True
    if abs(u) <= 0.12 and -0.2 <= v <= 0.6:
        return True
    if 0.6 < v <= 1.0 and abs(u) <= 0.1 + (v - 0.6) * 0.8:
        return True
    return False


SHAPES: dict[str, Callable[[float, float], bool]] = {"heart": in_heart, "spade": in_spade, "diamond": in_diamond, "club": in_club}


def suit_mask(suit: str, size: int) -> set[tuple[int, int]]:
    """Die Form einer Kartenfarbe in einem Quadrat der Kantenlänge size (mehrfach abgetastet)."""
    fn = SHAPES[suit]
    out = set()
    for y in range(size):
        for x in range(size):
            hits = 0
            for sy in (0.25, 0.75):
                for sx in (0.25, 0.75):
                    u = (x + sx) / size * 2.0 - 1.0
                    v = (y + sy) / size * 2.0 - 1.0
                    if fn(u, v):
                        hits += 1
            if hits >= 2:
                out.add((x, y))
    return out


# =============================================================================
# Die Zauberkarten
# =============================================================================

CARD_W, CARD_H = 32, 48


def _card_outline(img: Image.Image) -> None:
    """Abgerundete Ecken (durchsichtig) und ein dunkler Rand."""
    px = img.load()
    for y in range(CARD_H):
        for x in range(CARD_W):
            cx = min(x, CARD_W - 1 - x)
            cy = min(y, CARD_H - 1 - y)
            if cx + cy < 2:
                px[x, y] = CLEAR
            elif cx == 0 or cy == 0 or cx + cy == 2:
                px[x, y] = hexc("#2a1838")


def card_back() -> Image.Image:
    """Rücken: Goldrand, innen Harlekin-Rauten in Violett und Pflaume, in der Mitte ein Medaillon mit einem Stern."""
    img = Image.new("RGBA", (CARD_W, CARD_H), CLEAR)
    px = img.load()
    for y in range(CARD_H):
        for x in range(CARD_W):
            bx = min(x, CARD_W - 1 - x)
            by = min(y, CARD_H - 1 - y)
            if bx <= 2 or by <= 2:
                px[x, y] = col(GOLD, 4.2 + (0.8 if (x + y) % 5 == 0 else 0.0) - (0.8 if bx == 2 or by == 2 else 0.0))
                continue
            a = ((x - 16) / 4.0 + (y - 24) / 6.0) % 2.0
            b = ((x - 16) / 4.0 - (y - 24) / 6.0) % 2.0
            cell = (int(a) + int(b)) % 2
            edge = min(a % 1.0, 1.0 - a % 1.0, b % 1.0, 1.0 - b % 1.0) < 0.1
            level = 2.6 if cell else 1.6
            if edge:
                level = 3.6
            px[x, y] = col(VIOLET, level + noise(x, y, 7) * 0.3)
    # Medaillon mit Stern
    for y in range(CARD_H):
        for x in range(CARD_W):
            d = math.hypot(x + 0.5 - 16, y + 0.5 - 24)
            if d <= 7.5:
                px[x, y] = col(GOLD, 2.0) if d > 6.4 else col(VIOLET, 1.2)
            ang = math.atan2(y + 0.5 - 24, x + 0.5 - 16)
            r_star = 2.4 + 2.8 * max(0.0, math.cos(ang * 5 / 2.0) ** 2) if d > 0 else 0
            if d <= 5.4 and d <= r_star:
                px[x, y] = col(GOLD, 5.6 - d * 0.35)
    _card_outline(img)
    return img


def card_front(suit: str) -> Image.Image:
    """Vorderseite: elfenbeinweiß mit feinem Goldrand, eine große Farbe in der Mitte (schattiert, mit Glanzlicht), oben
    links und unten rechts kleine Eckzeichen."""
    img = Image.new("RGBA", (CARD_W, CARD_H), CLEAR)
    px = img.load()
    red = suit in ("heart", "diamond")
    ramp = RED if red else INK
    for y in range(CARD_H):
        for x in range(CARD_W):
            bx = min(x, CARD_W - 1 - x)
            by = min(y, CARD_H - 1 - y)
            if bx == 2 or by == 2:
                px[x, y] = col(GOLD, 4.0)
                continue
            px[x, y] = col(CARD, 5.2 - y / CARD_H * 0.8 + noise(x, y, 11) * 0.25)
    big = suit_mask(suit, 18)
    ox, oy = 7, 15
    for x, y in big:
        # Schattierung: oben links hell, unten rechts dunkel, ein Glanzlicht
        k = 4.0 - (x + y) / 36.0 * 2.4
        if (x - 5) ** 2 + (y - 5) ** 2 < 5 and suit != "diamond":
            k += 1.4
        px[ox + x, oy + y] = col(ramp, k)
    for x, y in big:
        for dx, dy in ((1, 0), (0, 1), (-1, 0), (0, -1)):
            if (x + dx, y + dy) not in big:
                px[ox + x, oy + y] = col(ramp, 1.2)
                break
    small = suit_mask(suit, 6)
    for x, y in small:
        px[4 + x, 5 + y] = col(ramp, 3.4)
        px[CARD_W - 5 - x, CARD_H - 6 - y] = col(ramp, 3.4)
    _card_outline(img)
    return img


# =============================================================================
# Konfetti (weiß, wird eingefärbt)
# =============================================================================

def confetti(kind: int) -> Image.Image:
    img = Image.new("RGBA", (8, 8), CLEAR)
    px = img.load()
    white = (255, 255, 255, 255)
    shade = (220, 220, 230, 255)
    for y in range(8):
        for x in range(8):
            on = False
            if kind == 0:
                on = 3 <= x <= 4 and 1 <= y <= 6                      # Streifen
            elif kind == 1:
                on = 2 <= x <= 5 and 2 <= y <= 5                      # Quadrat
            elif kind == 2:
                dx, dy = abs(x - 3.5), abs(y - 3.5)
                on = (dx < 0.6 and dy < 3.2) or (dy < 0.6 and dx < 3.2) or (dx < 1.6 and dy < 1.6)   # Stern
            else:
                on = (x - 3.5) ** 2 + (y - 3.5) ** 2 <= 6.5           # Kreis
            if on:
                px[x, y] = shade if (x + y) % 3 == 0 else white
    return img


# =============================================================================
# Der große Zauberzylinder
# =============================================================================

def stage_hat_side() -> Image.Image:
    """Seidenfilz: fast schwarz mit violettem Hauch, ein weicher senkrechter Glanzstreifen, feine Fasern."""
    img = Image.new("RGBA", (16, 16), CLEAR)
    px = img.load()
    for y in range(16):
        for x in range(16):
            sheen = max(0.0, 1.6 - abs(x - 5.5) * 0.55)
            level = 1.4 + sheen + noise(x, y, 31) * 0.5 + (0.3 if (x * 7 + y * 3) % 11 == 0 else 0.0)
            px[x, y] = col(FELT, level)
    return img


def stage_hat_top() -> Image.Image:
    """Der Deckel: dunkel, ein hellerer Ring am Rand und eine feine Naht im Kreis."""
    img = Image.new("RGBA", (16, 16), CLEAR)
    px = img.load()
    for y in range(16):
        for x in range(16):
            d = math.hypot(x + 0.5 - 8, y + 0.5 - 8)
            level = 1.6 + noise(x, y, 33) * 0.4
            if d > 6.6:
                level += 1.2
            elif 4.2 < d < 4.9:
                level -= 0.7
            px[x, y] = col(FELT, level)
    return img


def stage_hat_brim() -> Image.Image:
    """Die Krempe: Filz, nach außen mit einem satinglänzenden Rand."""
    img = Image.new("RGBA", (16, 16), CLEAR)
    px = img.load()
    for y in range(16):
        for x in range(16):
            edge = min(x, y, 15 - x, 15 - y)
            level = 1.5 + noise(x, y, 35) * 0.4
            if edge == 0:
                level = 3.6
            elif edge == 1:
                level = 2.6
            px[x, y] = col(FELT, level)
    return img


def stage_hat_band(lit: bool) -> Image.Image:
    """Das Satinband (Zeilen 5–10): Magenta mit zwei Goldlinien, vorn ein goldener Stern – leuchtend oder erloschen.
    Darüber und darunter Filz (für die Seiten des Bandes)."""
    img = stage_hat_side()
    px = img.load()
    for y in range(5, 11):
        for x in range(16):
            if y in (5, 10):
                px[x, y] = col(GOLD, 4.4 if lit else 2.6)
            else:
                level = (3.6 if lit else 2.2) + (0.8 if y == 6 else 0.0) + noise(x, y, 37) * 0.3
                px[x, y] = col(MAGENTA, level)
    # der Stern
    for y in range(5, 11):
        for x in range(4, 12):
            dx, dy = abs(x + 0.5 - 8), abs(y + 0.5 - 8)
            if (dx < 0.6 and dy < 3.0) or (dy < 0.6 and dx < 3.4) or (dx + dy < 1.6):
                px[x, y] = col(GOLD, 6.0 if lit else 3.0) if dx + dy < 1.0 else col(GOLD, 5.0 if lit else 2.6)
    return img


# =============================================================================
# Der Samtvorhang
# =============================================================================

def velvet(fringe: bool) -> Image.Image:
    """Samt in Falten: vier weiche Falten je Block (helle Grate, dunkle Täler), ein feiner Flor. Mit Fransenborte: unten eine
    goldene Borte und Fransen mit kleinen Quasten."""
    img = Image.new("RGBA", (16, 16), CLEAR)
    px = img.load()
    for y in range(16):
        for x in range(16):
            fold = math.sin((x + 0.5) / 16.0 * math.tau * 2.0 - 0.6)
            level = 2.3 + fold * 1.1 + noise(x, y, 41) * 0.35 - (0.4 if y % 7 == 3 and fold < -0.5 else 0.0)
            px[x, y] = col(VIOLET, level) if fold > -0.2 else mix(col(VIOLET, level), col(MAGENTA, 1.2), 0.35)
    if fringe:
        for x in range(16):
            px[x, 9] = col(GOLD, 2.4)
            px[x, 10] = col(GOLD, 4.6 if x % 3 else 5.6)
            px[x, 11] = col(GOLD, 3.2)
            for y in range(12, 16):
                fold = math.sin((x + 0.5) / 16.0 * math.tau * 2.0 - 0.6)
                strand = x % 2 == 0
                if strand:
                    px[x, y] = col(GOLD, 4.0 - (y - 12) * 0.5 + (0.6 if x % 4 == 0 and y == 15 else 0.0))
                else:
                    px[x, y] = mix(col(VIOLET, 1.0 + fold * 0.6), (10, 6, 16, 255), 0.4)
    return img


# =============================================================================
# Das Kartendeck der Mystik
# =============================================================================

def mystic_deck_icon() -> Image.Image:
    """Drei aufgefächerte Karten: hinten eine mit violettem Rücken, in der Mitte eine mit rotem Herz, vorn eine große mit
    schwarzem Pik; Goldränder, oben rechts ein Funkelstern."""
    s = st.Spr()
    spade5 = ("..#..", ".###.", "#####", "#####", "..#..")
    heart3 = ("#.#", "###", ".#.")

    def frame(x0: int, y0: int, w: int, h: int, inner):
        for y in range(y0, y0 + h):
            for x in range(x0, x0 + w):
                edge = x in (x0, x0 + w - 1) or y in (y0, y0 + h - 1)
                s.set(x, y, ("C", 4 if (x == x0 or y == y0) else 3) if edge else inner(x - x0, y - y0))

    frame(0, 4, 7, 10, lambda x, y: ("E", 3 if (x + y) % 2 == 0 else 2))
    frame(3, 1, 7, 10, lambda x, y: ("A", 6 if y < 5 else 5))
    for gy, row in enumerate(heart3):
        for gx, ch in enumerate(row):
            if ch == "#":
                s.set(4 + 1 + gx, 1 + 2 + gy, ("B", 4))
    frame(7, 4, 8, 12, lambda x, y: ("A", 6 if y < 6 else 5))
    for gy, row in enumerate(spade5):
        for gx, ch in enumerate(row):
            if ch == "#":
                s.set(7 + 1 + 1 + gx, 4 + 3 + gy, ("D", 2 if gy > 1 else 3))
    for x, y, lvl in ((14, 1, 6), (13, 1, 4), (15, 1, 4), (14, 0, 4), (14, 2, 4)):
        s.set(x, y, ("C", lvl))
    return s.outline(prefer=("A", "C", "B", "D", "E")).image({"A": CARD, "B": RED, "C": GOLD, "D": INK, "E": VIOLET})


def mystic_deck_model() -> Image.Image:
    """Atlas des 3D-Fächers (16 x 16): links (0–8) die Vorderseite einer Karte (weiß, Goldrand, rotes Herz), rechts (8–16)
    der Rücken (violett mit Goldrand und Rauten)."""
    img = Image.new("RGBA", (16, 16), CLEAR)
    px = img.load()
    heart = suit_mask("heart", 4)
    for y in range(16):
        for x in range(8):
            edge = x in (0, 7) or y in (0, 15)
            px[x, y] = col(GOLD, 4.2) if edge else col(CARD, 5.4 - y * 0.05)
            px[x + 8, y] = col(GOLD, 4.0) if edge else col(VIOLET, 2.8 if (x + y) % 2 == 0 else 1.8)
    for x, y in heart:
        px[2 + x, 6 + y] = col(RED, 3.8)
    px[1, 1] = col(RED, 3.0)
    px[6, 14] = col(RED, 3.0)
    return img


# =============================================================================
# Der Zylinder der Mystik
# =============================================================================

def mystic_top_hat_icon() -> Image.Image:
    """Ein Zylinder von schräg vorn: schwarzviolett mit Glanzstreifen, ein magentafarbenes Band mit goldener Schnalle, eine
    schillernde Feder und eine kleine Feenrose an der Seite, breite Krempe."""
    art = """
................
..........e.....
.....aaaaaae....
....aAAaaaaee...
....aAAaaaa.e...
....aAAaaaa.....
....aAAaaaa.....
....bbbcbbbr....
....bbcCcbbr....
....aAAaaaa.....
....aAAaaaa.....
.aaaaaaaaaaaaa..
..aaaaaaaaaaa...
................
................
................
"""
    s = st.Spr.art(art, legend={"a": ("A", 2), "A": ("A", 4), "b": ("B", 3), "c": ("C", 4), "C": ("C", 6),
                                "e": ("D", 5), "r": ("B", 5)})
    return s.outline(prefer=("A", "B", "C", "D")).image({"A": FELT, "B": MAGENTA, "C": GOLD, "D": TEAL})


def mystic_top_hat_model() -> Image.Image:
    """Atlas des 3D-Huts (32 x 32, im Modell 16 x 16): Filz mit Glanz und Seidenstreifen (0–16, 0–16), Krempe oben mit
    Goldrand (16–32, 0–16), Satinband mit Goldkante und Karo-Rauten (0–16, 16–21), Deckel (0–16, 21–32), Schnalle (16–22,
    16–22), Feenrose (22–28, 16–22), Herz-Ass (16–23, 22–32), Blatt (23–28, 22–27), Feder (28–32, 16–32)."""
    img = Image.new("RGBA", (32, 32), CLEAR)
    px = img.load()
    # Filz: senkrechter Glanzstreifen, feine Seidenstreifen, unten (am Band) dunkler, oben ein heller Saum
    for y in range(16):
        for x in range(16):
            sheen = max(0.0, 2.0 - abs(x - 4.5) * 0.4) + max(0.0, 0.8 - abs(x - 12.0) * 0.35)
            stripe = 0.3 if x % 3 == 1 else 0.0
            shade = -0.6 * (y / 15.0) + (0.7 if y == 0 else 0.0)
            px[x, y] = col(FELT, 1.9 + sheen + stripe + shade + noise(x, y, 51) * 0.3)
    # Krempe oben: Filz, außen ein Goldrand, innen ein heller Saum
    for y in range(16):
        for x in range(16):
            edge = min(x, y, 15 - x, 15 - y)
            if edge == 0:
                px[16 + x, y] = col(GOLD, 4.4 if (x + y) % 2 else 3.6)
            else:
                px[16 + x, y] = col(FELT, 1.5 + (0.9 if edge == 1 else 0.0) + noise(x, y, 52) * 0.3)
    # Band: Goldkante oben und unten, dazwischen Satin mit kleinen goldenen Karo-Rauten
    for y in range(5):
        for x in range(16):
            if y in (0, 4):
                px[x, 16 + y] = col(GOLD, 4.6 if x % 2 == 0 else 3.8)
            else:
                cx = (x // 4) * 4 + 2
                diamond = abs(x - cx) + abs(y - 2) <= 1
                px[x, 16 + y] = col(GOLD, 4.8 if x == cx and y == 2 else 3.9) if diamond \
                    else col(MAGENTA, 3.9 if y == 1 else 3.3 if y == 2 else 2.8)
    # Deckel: Filz, außen ein heller Saum
    for y in range(11):
        for x in range(16):
            edge = min(x, y, 15 - x, 10 - y)
            px[x, 21 + y] = col(FELT, 1.4 + (1.1 if edge == 0 else 0.4 if edge == 1 else 0.0) + noise(x, y, 53) * 0.25)
    # Schnalle: Goldrahmen um ein dunkles Fenster
    for y in range(6):
        for x in range(6):
            frame = x in (0, 5) or y in (0, 5) or (x in (1, 4) and y in (1, 4))
            px[16 + x, 16 + y] = col(GOLD, 5.0 - y * 0.25 if frame else 3.0) if frame or x in (1, 4) or y in (1, 4) else col(FELT, 1.2)
    # Feenrose: eine Spirale aus Blütenblättern
    rose = ["..pPp.",
            ".pRrPp",
            "pRdDrP",
            "PrDdRp",
            "pPrRp.",
            ".ppP.."]
    roses = {"p": col(PINK, 2.6), "P": col(PINK, 4.2), "R": col(MAGENTA, 4.4), "r": col(MAGENTA, 3.2), "d": col(MAGENTA, 1.8),
             "D": col(MAGENTA, 2.6)}
    for y, row in enumerate(rose):
        for x, c in enumerate(row):
            if c != ".":
                px[22 + x, 16 + y] = roses[c]
    # Herz-Ass: weiße Karte mit zartem Rand, einem roten Herzen in der Mitte und roten Eckzeichen
    for y in range(10):
        for x in range(7):
            border = x in (0, 6) or y in (0, 9)
            px[16 + x, 22 + y] = col(CARD, 3.6 if border else 5.6)
    heart = {(1, 3), (2, 3), (4, 3), (5, 3), (1, 4), (2, 4), (3, 4), (4, 4), (5, 4), (2, 5), (3, 5), (4, 5), (3, 6)}
    for x, y in heart:
        px[16 + x, 22 + y] = col(RED, 4.6 if y == 3 else 3.9 if y == 4 else 3.2)
    px[16 + 1, 22 + 1] = col(RED, 3.6)
    px[16 + 5, 22 + 8] = col(RED, 3.6)
    # Blatt
    leaf = [".gG..",
            "gGLg.",
            "GLlGg",
            ".gLGg",
            "..gg."]
    leaves = {"g": (52, 112, 66, 255), "G": (74, 150, 84, 255), "L": (126, 200, 120, 255), "l": (160, 222, 140, 255)}
    for y, row in enumerate(leaf):
        for x, c in enumerate(row):
            if c != ".":
                px[23 + x, 22 + y] = leaves[c]
    # Feder: unten rosa, zur Spitze türkis, in der Mitte der helle Kiel, die Fahne zur Spitze hin zerzaust
    for y in range(16):
        k = y / 15.0
        for x in range(4):
            if y >= 14 and x in (0, 3):
                continue
            if x == 0 and y % 3 == 2:
                continue
            quill = x in (1, 2) and (x == 1 or y < 3)
            base = mix(col(PINK, 4.8 if quill else 3.6 + (x % 2) * 0.4), col(TEAL, 4.8 if quill else 3.4 + (x % 2) * 0.4), 1.0 - k)
            px[28 + x, 16 + y] = base
    return img


def build(put: Callable[[str, Image.Image], None]) -> None:
    put("entity/miss_mystik/card_back", card_back())
    for suit in ("heart", "spade", "diamond", "club"):
        put(f"entity/miss_mystik/card_{suit}", card_front(suit))
    for k in range(4):
        put(f"particle/confetti_{k}", confetti(k))
    put("block/stage_hat_side", stage_hat_side())
    put("block/stage_hat_top", stage_hat_top())
    put("block/stage_hat_brim", stage_hat_brim())
    put("block/stage_hat_band", stage_hat_band(True))
    put("block/stage_hat_band_dim", stage_hat_band(False))
    put("block/velvet_curtain", velvet(False))
    put("block/velvet_curtain_fringe", velvet(True))
    put("item/mystic_deck", mystic_deck_icon())
    put("item/mystic_deck_model", mystic_deck_model())
    put("item/mystic_top_hat", mystic_top_hat_icon())
    put("item/mystic_top_hat_model", mystic_top_hat_model())
