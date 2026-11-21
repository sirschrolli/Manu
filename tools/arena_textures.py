#!/usr/bin/env python3
"""
Die Arena der Prüfungen und ihr Meister (seit 1.9): Ventor, der Gladiator der Winde.

  * block/arena_seal – das Arenasiegel: gemeißelter Sandstein, in der Mitte eine Scheibe aus Kupfer mit einer Windspirale aus
    Grünspan;
  * block/gladiator_net – das Netz, das jemanden gefangen hält: Rautenmaschen aus Hanfseil mit Knoten, unten Bleigewichte;
  * entity/ventor/sigil (32 x 16) – die Windsiegel über dem Podest: links matt (Kupfer und Grünspan), rechts leuchtend
    (die Spirale weiß und türkis);
  * entity/ventor/net (32 x 32) – das Wurfnetz im Flug: rund, mit Saumseil, Speichen, Ringen und Bleigewichten am Rand;
  * item/gladiator_net – das Wurfnetz im Inventar: ein Bündel aus Netz, Seil und Gewichten;
  * item/storm_trident – der Sturmdreizack im Inventar (schräg, die Spitzen leuchten);
  * item/storm_trident_model (+ _glow, je 32 x 32) – die Bereiche, die das 3D-Modell in der Hand belegt (siehe
    ArenaModels.TRIDENT: Schaft, Wicklung, Kupfer, Querstück, Windrune, Zinken, Spitzen, Widerhaken).

Wird von tools/generate_textures.py aufgerufen: build(put). Allein aufgerufen schreibt es die Bilder direkt und zeigt sie
vergrößert: python3 tools/arena_textures.py [vorschau.png]
"""
from __future__ import annotations

import math
import os
import sys
from pathlib import Path

from PIL import Image

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import item_style as st  # noqa: E402

CLEAR = (0, 0, 0, 0)
ROOT = Path(__file__).resolve().parent.parent
TEX = ROOT / "src/main/resources/assets/kingdomsatwar/textures"

SAND = [(0x7a, 0x62, 0x38), (0x9a, 0x80, 0x4c), (0xb8, 0x9c, 0x64), (0xd2, 0xb8, 0x80), (0xe6, 0xd2, 0x9c), (0xf4, 0xe6, 0xbc)]
COPPER = [(0x4a, 0x23, 0x12), (0x77, 0x39, 0x1b), (0xa2, 0x4f, 0x24), (0xc9, 0x6c, 0x33), (0xe3, 0x8f, 0x4f), (0xf5, 0xb9, 0x8a)]
PATINA = [(0x12, 0x36, 0x30), (0x1c, 0x50, 0x45), (0x28, 0x6b, 0x5a), (0x3a, 0x8a, 0x73), (0x58, 0xab, 0x8f), (0x86, 0xcf, 0xb2)]
BRONZE = [(0x2e, 0x1d, 0x0e), (0x46, 0x30, 0x15), (0x61, 0x44, 0x1c), (0x7f, 0x5b, 0x24), (0x9e, 0x75, 0x2f), (0xbc, 0x92, 0x41),
          (0xd7, 0xb2, 0x5e), (0xec, 0xd2, 0x8c)]
WIND = [(0x0b, 0x3c, 0x4a), (0x11, 0x60, 0x7a), (0x1a, 0x8c, 0xa6), (0x2f, 0xb9, 0xcf), (0x62, 0xdb, 0xe8), (0xa4, 0xf0, 0xf6),
        (0xdc, 0xfc, 0xff), (0xff, 0xff, 0xff)]
ROPE = [(0x2e, 0x24, 0x14), (0x4a, 0x3b, 0x22), (0x68, 0x55, 0x32), (0x8a, 0x73, 0x46), (0xab, 0x92, 0x5e), (0xcb, 0xb2, 0x7c)]
LEAD = [(0x15, 0x16, 0x1a), (0x24, 0x26, 0x2c), (0x37, 0x3a, 0x42), (0x4d, 0x51, 0x5b), (0x66, 0x6b, 0x76)]
WOOD = [(0x1a, 0x10, 0x0a), (0x2a, 0x1a, 0x10), (0x3c, 0x26, 0x17), (0x51, 0x34, 0x1f), (0x68, 0x44, 0x28)]
LEATHER = [(0x1d, 0x10, 0x09), (0x2f, 0x1b, 0x0e), (0x46, 0x29, 0x14), (0x5f, 0x39, 0x1d), (0x7a, 0x4b, 0x28), (0x98, 0x62, 0x36)]
STEEL = [(0x2c, 0x2a, 0x28), (0x4a, 0x46, 0x40), (0x6e, 0x67, 0x5c), (0x95, 0x8b, 0x7a), (0xbd, 0xb2, 0x9c), (0xe2, 0xd8, 0xc2),
         (0xfb, 0xf4, 0xe2)]


def _h(*a: int) -> float:
    """Ein fester Zufall je Pixel (0–1)."""
    h = 2166136261
    for v in a:
        h = ((h ^ (v & 0xFFFFFFFF)) * 16777619) & 0xFFFFFFFF
    h ^= h >> 13
    h = (h * 0x5BD1E995) & 0xFFFFFFFF
    return (h & 0xFFFF) / 0xFFFF


def _c(ramp: list, i: float) -> tuple:
    return ramp[max(0, min(len(ramp) - 1, int(round(i))))] + (255,)


def _spiral(u: float, v: float, turns: float = 1.5, width: float = 0.17) -> bool:
    """Eine Windspirale um die Mitte (u, v in −1..1)."""
    r = math.hypot(u, v)
    if r > 1.0 or r < 0.1:
        return False
    a = (math.atan2(v, u) / math.tau) % 1.0
    phase = (r * turns - a) % 1.0
    return phase < width * (1.4 - r * 0.5)


# =============================================================================================
# Blöcke
# =============================================================================================

def seal_block() -> Image.Image:
    """Das Arenasiegel: gemeißelter Sandstein (ein Rahmen aus Quadern), in der Mitte die Kupferscheibe mit der Windspirale."""
    img = Image.new("RGBA", (16, 16), CLEAR)
    px = img.load()
    for y in range(16):
        for x in range(16):
            n = _h(x, y, 901)
            edge = x in (0, 15) or y in (0, 15)
            frame = x in (1, 14) or y in (1, 14)
            if edge:
                col = _c(SAND, 1.6 + n * 0.8)
            elif frame:
                col = _c(SAND, 4.2 + n * 0.6) if (x == 1 or y == 1) else _c(SAND, 2.6 + n * 0.5)
            else:
                col = _c(SAND, 3.0 + n * 0.9 + (0.4 if (x + y) % 7 == 0 else 0.0))
            du, dv = (x - 7.5) / 5.6, (y - 7.5) / 5.6
            r = math.hypot(du, dv)
            if r <= 1.0:
                if r > 0.82:
                    col = _c(COPPER, 4 if du + dv < 0 else 2)
                elif _spiral(du / 0.82, dv / 0.82):
                    col = _c(PATINA, 5 if du + dv < 0 else 4)
                else:
                    col = _c(COPPER, 3.2 - r * 1.4 + _h(x, y, 903) * 0.6)
            elif r <= 1.18:
                col = _c(SAND, 1.2)                                    # die Fuge um die Scheibe
            px[x, y] = col
    return img


def net_block() -> Image.Image:
    """Das Netz (gekreuzt wie ein Spinnennetz): Rautenmaschen aus Hanfseil mit Knoten; unten Bleigewichte."""
    img = Image.new("RGBA", (16, 16), CLEAR)
    px = img.load()
    for y in range(16):
        for x in range(16):
            a = (x + y) % 5
            b = (x - y) % 5
            if a == 0 and b == 0:
                px[x, y] = _c(ROPE, 5)
            elif a == 0 or b == 0:
                if _h(x, y, 911) > 0.93:
                    continue                                           # eine gerissene Masche
                px[x, y] = _c(ROPE, 3.4 if a == 0 else 2.6)
    for x in range(1, 15):
        px[x, 14] = _c(ROPE, 3 if x % 2 else 4)
    for gx in (2, 7, 12):
        for dx, dy, lv in ((0, 0, 3), (1, 0, 2), (0, 1, 2), (1, 1, 1)):
            px[gx + dx, 13 + dy + 1 if dy else 15] = _c(LEAD, lv + 1)
    return img


# =============================================================================================
# Windsiegel und das Netz im Flug
# =============================================================================================

def sigil() -> Image.Image:
    """Zwei Windsiegel (je 16 x 16): links matt – ein Kupferring, darin die Spirale aus Grünspan; rechts leuchtend – der Ring
    hell, die Spirale weiß und türkis, dazwischen ein Schimmer."""
    img = Image.new("RGBA", (32, 16), CLEAR)
    px = img.load()
    for lit in (False, True):
        ox = 16 if lit else 0
        for y in range(16):
            for x in range(16):
                du, dv = (x - 7.5) / 7.2, (y - 7.5) / 7.2
                r = math.hypot(du, dv)
                if r > 1.0:
                    continue
                if r > 0.8:
                    col = _c(WIND, 6 if du + dv < 0 else 5) if lit else _c(COPPER, 4 if du + dv < 0 else 2)
                elif _spiral(du / 0.8, dv / 0.8, 1.6, 0.2):
                    col = _c(WIND, 7 if r < 0.4 else 6) if lit else _c(PATINA, 4 if du + dv < 0 else 3)
                else:
                    col = _c(WIND, 3.2 + (0.8 - r) * 2.0) if lit else _c(COPPER, 1.6 + (0.8 - r) * 1.2 + _h(x, y, 921) * 0.5)
                px[ox + x, y] = col
    return img


def net_entity() -> Image.Image:
    """Das Wurfnetz im Flug (von vorn): rund, ein Saumseil, acht Speichen, drei Ringe, am Rand acht Bleigewichte."""
    size = 32
    img = Image.new("RGBA", (size, size), CLEAR)
    px = img.load()
    c = (size - 1) / 2.0
    for y in range(size):
        for x in range(size):
            dx, dy = x - c, y - c
            r = math.hypot(dx, dy)
            if r > 15.6:
                continue
            a = (math.atan2(dy, dx) / math.tau) % 1.0
            spoke = min((a * 8) % 1.0, 1.0 - (a * 8) % 1.0) * r * math.tau / 8 < 0.55
            ring = any(abs(r - rr) < 0.55 for rr in (4.0, 8.0, 11.8))
            rim = r > 14.4
            mesh = (round(dx + dy) % 4 == 0 or round(dx - dy) % 4 == 0) and r < 14.4
            if rim:
                col = _c(ROPE, 4 if dy < 0 else 2)
            elif spoke or ring:
                col = _c(ROPE, 4.4 if spoke else 3.6)
            elif mesh and _h(x, y, 931) > 0.08:
                col = _c(ROPE, 2.6)
            else:
                continue
            px[x, y] = col
    for k in range(8):
        a = k * math.tau / 8 + math.tau / 16
        gx, gy = c + math.cos(a) * 14.6, c + math.sin(a) * 14.6
        for ddx in (-1, 0, 1):
            for ddy in (-1, 0, 1):
                if abs(ddx) + abs(ddy) <= 1:
                    xx, yy = int(round(gx + ddx)), int(round(gy + ddy))
                    if 0 <= xx < size and 0 <= yy < size:
                        px[xx, yy] = _c(LEAD, 4 if ddx + ddy < 0 else 2)
    return img


# =============================================================================================
# Gegenstände
# =============================================================================================

def net_icon() -> Image.Image:
    """Das Wurfnetz im Inventar: oben mit einer Lederschlaufe gebunden, darunter fällt es weit auseinander (Rautenmaschen aus
    Hanfseil, ein Saumseil), unten hängen vier Bleigewichte."""
    s = st.Spr(16, 16)
    for (x, y, lv) in ((7, 1, 3), (8, 1, 2), (6, 2, 4), (9, 2, 2), (7, 2, 5), (8, 2, 4)):   # die Schlaufe
        s.set(x, y, ("B", lv))
    for y in range(3, 13):
        half = 0.9 + (y - 3) * 0.62
        x0, x1 = int(round(7.5 - half)), int(round(7.5 + half))
        for x in range(x0, x1 + 1):
            border = x in (x0, x1)
            mesh = (x + y) % 3 == 0 or (x - y) % 3 == 0
            if y == 12 or border:
                s.set(x, y, ("C", 4 if x < 8 else 3))
            elif mesh:
                s.set(x, y, ("C", 5 if (x + y) % 3 == 0 else 3))
    for gx in (2, 6, 10, 13):                                          # die Bleigewichte am Saum
        s.set(gx, 13, ("D", 4))
        s.set(gx, 14, ("D", 2))
    return s.outline(prefer=("C", "B", "D")).image({"B": st.ramp("#7a4b28", dark=0.15, light=0.85, cool=30),
                                                     "C": st.ramp("#ab925e", dark=0.2, light=0.92, cool=60),
                                                     "D": st.ramp("#4d515b", dark=0.1, light=0.75, cool=240)})


def trident_icon() -> Image.Image:
    """Der Sturmdreizack im Inventar: schräg (Griff unten links, Zinken oben rechts) – der Knauf aus Kupfer, der Schaft aus
    dunklem Holz mit Kupferbändern, unten die rote Lederwicklung; ein Querstück aus Bronze mit der Windrune; drei Zinken aus
    hellem Erz, weit genug auseinander, dass man sie zählt, deren Spitzen leuchten. Gezeichnet längs der Diagonale: u läuft vom
    Knauf zur Spitze, v quer dazu (beides in Pixeln)."""
    s = st.Spr(16, 16)
    root2 = math.sqrt(2.0)
    for y in range(16):
        for x in range(16):
            cx, cy = x + 0.5, y + 0.5
            u = ((cx - 0.5) - (cy - 15.5)) / root2
            v = ((cx - 0.5) + (cy - 15.5)) / root2
            px = None
            if u < 1.9 and abs(v) < 1.1:
                px = ("D", 5 if v < 0 else 3)                          # der Knauf
            elif u < 11.0 and abs(v) < 0.75:
                lit = v < 0
                if 2.5 < u < 6.4:
                    px = ("E", 4 if lit else 2)                        # die Lederwicklung
                elif abs(u - 7.6) < 0.6 or abs(u - 10.4) < 0.7:
                    px = ("D", 5 if lit else 3)                        # ein Kupferband, die Tülle
                else:
                    px = ("B", 4 if lit else 2)
            elif 11.0 <= u < 12.5 and abs(v) < 3.9:
                px = ("G", 5) if abs(v) < 0.75 else ("F", 5 if v < 0 else 3)   # das Querstück mit der Windrune
            elif u >= 12.5:
                for cv, top in ((-3.1, 17.9), (0.0, 19.7), (3.1, 17.9)):
                    if abs(v - cv) < 0.75 and u < top:
                        px = ("G", 6 if v - cv < 0 else 4) if u > top - 1.7 else ("C", 5 if v - cv < 0 else 3)
            if px is not None:
                s.set(x, y, px)
    return s.outline(prefer=("C", "F", "D", "B", "E", "G")).image({
        "B": st.ramp("#51341f", dark=0.1, light=0.7, cool=30),
        "C": st.ramp("#bdb29c", dark=0.25, light=0.97, cool=220),
        "D": st.ramp("#c96c33", dark=0.2, light=0.92, warm=60),
        "E": st.ramp("#a42620", dark=0.15, light=0.8, cool=30),
        "F": st.ramp("#bc9241", dark=0.2, light=0.95, warm=70),
        "G": st.fixed("#0b3c4a", "#11607a", "#1a8ca6", "#2fb9cf", "#62dbe8", "#a4f0f6", "#e6ffff")})


def _box(px, u0: int, v0: int, u1: int, v1: int, paint) -> None:
    """Füllt den Bereich (UV-Einheiten, je zwei Pixel) mit paint(x, y, w, h) – x, y relativ, w, h in Pixeln."""
    w, h = (u1 - u0) * 2, (v1 - v0) * 2
    for y in range(h):
        for x in range(w):
            col = paint(x, y, w, h)
            if col is not None:
                px[u0 * 2 + x, v0 * 2 + y] = col


def trident_atlas() -> tuple[Image.Image, Image.Image]:
    """Die Bereiche des 3D-Modells (wie in ArenaModels.TRIDENT) und ihr Leuchtbild."""
    img = Image.new("RGBA", (32, 32), CLEAR)
    glow = Image.new("RGBA", (32, 32), CLEAR)
    px, gx = img.load(), glow.load()
    # der Schaft: dunkles Eschenholz, längs gemasert, dazu die Kupferspirale (schräge Bänder)
    _box(px, 0, 0, 1, 16, lambda x, y, w, h: _c(COPPER, 4) if (y + x * 3) % 9 == 0 else _c(WOOD, 2.2 + _h(x, y // 3, 941) * 1.6))
    _box(px, 1, 0, 2, 16, lambda x, y, w, h: _c(COPPER, 3) if (y + x * 3 + 4) % 9 == 0 else _c(WOOD, 1.6 + _h(x, y // 3, 943) * 1.4))
    # die Lederwicklung (schräge Bahnen)
    _box(px, 2, 0, 3, 8, lambda x, y, w, h: _c(LEATHER, 4.4 if (y + x) % 4 == 0 else 2.6 + _h(x, y, 945) * 0.6))
    # Kupfer (Knauf, Bänder, Tülle): oben hell, unten dunkel; die Kappe
    _box(px, 3, 0, 5, 2, lambda x, y, w, h: _c(COPPER, 4.6 - y * 0.8 + (0.6 if x == 0 else 0.0)))
    _box(px, 3, 2, 5, 4, lambda x, y, w, h: _c(COPPER, 4 if (x + y) % 3 else 5))
    # das Querstück aus Bronze (von vorn: oben im Licht, die Enden dunkler) und seine Seiten
    _box(px, 5, 0, 11, 2, lambda x, y, w, h: _c(BRONZE, 6.2 - y * 0.9 - abs(x - w / 2) / w * 1.6 + (0.6 if y == 0 else 0.0)))
    _box(px, 5, 2, 6, 4, lambda x, y, w, h: _c(BRONZE, 4.4 - y * 0.6))
    _box(px, 5, 4, 11, 6, lambda x, y, w, h: _c(BRONZE, 5.6 - y * 0.6))
    # die Windrune (leuchtet)
    rune = lambda x, y, w, h: _c(WIND, 7 if (x + y) % 3 == 0 else 5)  # noqa: E731
    _box(px, 6, 2, 8, 4, rune)
    _box(gx, 6, 2, 8, 4, rune)
    # die Zinken: helles Erz, an der Kante heller; ihre Seiten
    _box(px, 11, 0, 12, 10, lambda x, y, w, h: _c(STEEL, 5.6 - y / h * 1.4 + (0.6 if x == 0 else 0.0)))
    _box(px, 12, 0, 13, 10, lambda x, y, w, h: _c(STEEL, 4.4 - y / h * 1.2))
    # die Spitzen (leuchten) und die Widerhaken
    tip = lambda x, y, w, h: _c(WIND, 7 - y * 0.5)  # noqa: E731
    _box(px, 13, 0, 14, 3, tip)
    _box(gx, 13, 0, 14, 3, tip)
    _box(px, 13, 3, 14, 6, lambda x, y, w, h: _c(STEEL, 4.8 - y * 0.5))
    return img, glow


def build(put) -> None:
    """Für tools/generate_textures.py."""
    put("block/arena_seal", seal_block())
    put("block/gladiator_net", net_block())
    put("entity/ventor/sigil", sigil())
    put("entity/ventor/net", net_entity())
    put("item/gladiator_net", net_icon())
    put("item/storm_trident", trident_icon())
    atlas, glow = trident_atlas()
    put("item/storm_trident_model", atlas)
    put("item/storm_trident_model_glow", glow)


if __name__ == "__main__":
    written: dict[str, Image.Image] = {}
    build(lambda key, img: written.__setitem__(key, img))
    for key, img in written.items():
        out = TEX / f"{key}.png"
        out.parent.mkdir(parents=True, exist_ok=True)
        img.save(out)
    target = sys.argv[1] if len(sys.argv) > 1 else "/tmp/arena_textures.png"
    tiles = list(written.values())
    sheet = Image.new("RGBA", (sum(t.width * 8 + 16 for t in tiles), 32 * 8), (96, 104, 112, 255))
    x = 0
    for t in tiles:
        sheet.alpha_composite(t.resize((t.width * 8, t.height * 8), Image.NEAREST), (x, 0))
        x += t.width * 8 + 16
    sheet.save(target)
    print(target)
