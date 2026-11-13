#!/usr/bin/env python3
"""
Das Rabenbanner der Raubritterburg (seit 1.8): der Bannerpfahl aus dunkler Eiche mit Eisenbändern und goldenem Knauf, am
Querholz das Banner derer von Rabenfels.

  * raven_banner_pole – dunkle Eiche, längs gemasert;
  * raven_banner_iron – geschwärztes Eisen mit Nieten (Fuß, Bänder, Querholz-Beschläge);
  * entity/raven_banner/raven_banner (128 x 128) – was der Block-Renderer zeichnet: das Banner (48 x 72, ein goldenes Feld
    mit sanften Falten und einem schwarzen Rand, darin der schwarze Rabe wie auf Kunos Wappenrock, unten ein schwarzer
    Streifen mit goldenen Zacken und der Schwalbenschwanz), das zerrissene Banner (Fetzen, versengt, ein Stück des Raben),
    Eichenholz, Gold und Eisen für Pfahl, Querholz, Knauf und Beschläge;
  * item/raven_banner – das Bild im Inventar: Pfahl, Querholz und Banner.

Wird von tools/generate_textures.py aufgerufen: build(put).
"""
from __future__ import annotations

import math
import os
import sys

from PIL import Image

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import item_style as st  # noqa: E402

CLEAR = (0, 0, 0, 0)

#: dunkle Eiche (von der Fuge zum Licht)
OAK = [(0x24, 0x17, 0x0b), (0x31, 0x20, 0x10), (0x40, 0x2b, 0x16), (0x50, 0x36, 0x1d), (0x60, 0x42, 0x25), (0x72, 0x50, 0x2e)]
IRON = [(0x14, 0x13, 0x17), (0x22, 0x21, 0x27), (0x33, 0x32, 0x3a), (0x47, 0x46, 0x50), (0x5e, 0x5d, 0x68), (0x7c, 0x7b, 0x86),
        (0xa4, 0xa3, 0xae)]
GOLD = [(0x5a, 0x3c, 0x0c), (0x8a, 0x64, 0x14), (0xb8, 0x8e, 0x26), (0xe0, 0xb6, 0x3c), (0xf2, 0xd0, 0x64), (0xff, 0xec, 0xa8),
        (0xff, 0xf8, 0xdc)]
#: das Tuch: Gold, etwas wärmer und matter als das Metall
CLOTH = [(0x6a, 0x46, 0x10), (0x94, 0x6a, 0x1a), (0xbc, 0x8e, 0x2a), (0xd8, 0xaa, 0x3c), (0xe8, 0xc2, 0x58), (0xf4, 0xd8, 0x80)]
BLACK = [(0x0e, 0x0d, 0x11), (0x18, 0x17, 0x1d), (0x24, 0x23, 0x2b), (0x34, 0x33, 0x3d), (0x48, 0x47, 0x53)]
SCORCH = [(0x1c, 0x12, 0x0a), (0x3a, 0x24, 0x10), (0x5e, 0x3c, 0x16)]

#: der Rabe (wie auf Kunos Wappenrock, Blick nach links), doppelt so groß gezeichnet; o = Auge
RAVEN = (
    "....XXX.......",
    "...XXXXX......",
    "XXXXoXXX......",
    "..XXXXXXX.....",
    "...XXXXXXXX...",
    "...XXXXXXXXXX.",
    "....XXXXXXXXXX",
    "....XXXXXXXXX.",
    ".....XXXXXXX..",
    "......XXXXX...",
    "......X...X...",
    ".....XX..XX...",
)


def _h(*a: int) -> float:
    """Ein fester Zufall je Pixel (0–1)."""
    h = 2166136261
    for v in a:
        h = ((h ^ (v & 0xFFFFFFFF)) * 16777619) & 0xFFFFFFFF
    h ^= h >> 13
    h = (h * 0x5BD1E995) & 0xFFFFFFFF
    h ^= h >> 15
    return (h & 0xFFFF) / 65535.0


def _c(ramp: list, i: float) -> tuple:
    return ramp[max(0, min(len(ramp) - 1, int(round(i))))] + (255,)


def pole() -> Image.Image:
    """Dunkle Eiche, längs gemasert: helle und dunkle Fasern, hier und da ein Ast."""
    img = Image.new("RGBA", (16, 16), CLEAR)
    px = img.load()
    for y in range(16):
        for x in range(16):
            level = 3.0 + 0.9 * math.sin(x * 1.9 + math.sin(y * 0.35 + x) * 0.8)
            if _h(x, y // 3, 5) < 0.2:
                level -= 1.0
            if _h(x, y, 7) < 0.05:
                level += 1.0
            if (x - 11) ** 2 + ((y - 5) * 0.6) ** 2 < 2.2:
                level = 1.0 if (x - 11) ** 2 + ((y - 5) * 0.6) ** 2 < 0.8 else 2.0
            px[x, y] = _c(OAK, level)
    return img


def iron() -> Image.Image:
    """Geschwärztes Eisen, gehämmert, mit Nieten an den Rändern."""
    img = Image.new("RGBA", (16, 16), CLEAR)
    px = img.load()
    for y in range(16):
        for x in range(16):
            level = 3.0 - 0.6 * (x + y) / 30.0
            r = _h(x // 2, y // 2, 11)
            if r < 0.2:
                level -= 0.9
            elif r > 0.85:
                level += 0.8
            if x in (0, 15) or y in (0, 15):
                level = 1.2
            if (x, y) in ((2, 2), (13, 2), (2, 13), (13, 13), (7, 2), (7, 13)):
                level = 5.6
            if (x, y) in ((3, 3), (14, 3), (3, 14), (14, 14), (8, 3), (8, 14)):
                level = 1.0
            px[x, y] = _c(IRON, level)
    return img


def gold() -> Image.Image:
    """Gold, getrieben: hell oben links, ein Glanzstreif, dunkle Ränder."""
    img = Image.new("RGBA", (16, 16), CLEAR)
    px = img.load()
    for y in range(16):
        for x in range(16):
            level = 4.2 - 1.6 * (x + y) / 30.0
            if x == y or x == y + 1:
                level += 1.2
            if x in (0, 15) or y in (0, 15):
                level -= 1.4
            if _h(x, y, 13) < 0.08:
                level -= 0.8
            px[x, y] = _c(GOLD, level)
    return img


#: das Tuch (in der Textur des Banners: 2 Pixel je Modelleinheit, 24 x 36 Einheiten)
CW, CH = 48, 72
#: so groß wird der Rabe gezeichnet (je Bildpunkt der Vorlage)
RS = 3
RX, RY = 3, 10


def _fold(x: int, y: int) -> float:
    """Die Falten des Tuchs: senkrechte Wellen, die nach unten etwas weiter werden."""
    return 0.7 * math.sin(x * 0.37 + 0.4 + y * 0.015) + 0.35 * math.sin(x * 0.87 + 1.7)


def _raven_at(x: int, y: int) -> str | None:
    """Das Zeichen des (vergrößerten) Raben bei (x, y)."""
    if x < RX or y < RY:
        return None
    rx, ry = (x - RX) // RS, (y - RY) // RS
    if 0 <= ry < len(RAVEN) and 0 <= rx < len(RAVEN[0]):
        ch = RAVEN[ry][rx]
        return None if ch == "." else ch
    return None


def _raven_color(x: int, y: int, ch: str) -> tuple:
    """Der Rabe: schwarz, Federn mit dunkelgrauen Kanten, das Auge golden, der Schnabel etwas heller."""
    rx, ry = (x - RX) / RS, (y - RY) / RS
    if ch == "o":
        cx, cy = (x - RX) % RS, (y - RY) % RS
        return GOLD[5] + (255,) if (cx, cy) in ((1, 1), (1, 0), (0, 1)) else BLACK[0] + (255,)
    if rx < 2.6 and 2 <= ry < 3:
        return (BLACK[3] if (y - RY) % RS == 0 else BLACK[2]) + (255,)                 # der Schnabel
    # Flügelfedern: schräge, hellere Linien auf dem Rumpf
    if 5 <= ry <= 8.5 and rx > 4 and (x - RX + y - RY) % 6 == 0:
        return BLACK[3] + (255,)
    left = _raven_at(x - 1, y) is None
    top = _raven_at(x, y - 1) is None
    return BLACK[2] + (255,) if left or top else BLACK[1] + (255,)


def _inside(x: int, y: int) -> bool:
    """Ist (x, y) im Banner? Unten der Schwalbenschwanz: ein Keil in der Mitte, die äußeren Ecken schräg."""
    if not (0 <= x < CW and 0 <= y < CH):
        return False
    if y >= 58 and abs(x - (CW - 1) / 2) < (y - 57) * 0.75:
        return False
    if y >= 63 and (x < (y - 63) * 0.6 or x > CW - 1 - (y - 63) * 0.6):
        return False
    return True


def cloth() -> Image.Image:
    """Das Banner derer von Rabenfels (48 x 72): oben der Saum um das Querholz, ein goldenes Feld mit Falten und schwarzem
    Rand, darin der Rabe; unten ein schwarzer Streifen mit goldenen Zacken und der Schwalbenschwanz."""
    img = Image.new("RGBA", (CW, CH), CLEAR)
    px = img.load()
    for y in range(CH):
        for x in range(CW):
            if not _inside(x, y):
                continue
            fold = _fold(x, y)
            col = _c(CLOTH, 3.2 + fold + (0.4 if _h(x, y, 21) < 0.1 else 0.0) - (0.5 if _h(x, y, 23) < 0.07 else 0.0))
            if y <= 3:
                col = _c(CLOTH, 1.5 + (0.9 if y == 0 else 0.4 if y == 1 else 0.0) + 0.4 * math.sin(x * 0.6))   # der Saum
            elif x in (0, CW - 1) or y == 4:
                col = BLACK[1] + (255,)                                                                 # schwarzer Rand
            elif x in (1, CW - 2) or y == 5:
                col = _c(CLOTH, 1.7 + fold)
            ch = _raven_at(x, y)
            if ch is not None:
                col = _raven_color(x, y, ch)
            # unten ein schwarzer Streifen mit goldenen Zacken (wie der gezackte Saum seiner Schabracke)
            if 49 <= y <= 55:
                k = x % 8
                zig = k if k < 4 else 7 - k
                col = BLACK[1] + (255,) if y - 49 > zig + 1 or y == 55 else _c(CLOTH, 3.6 + fold)
            if y >= 56 and (not _inside(x - 1, y) or not _inside(x + 1, y) or not _inside(x, y + 1)):
                col = BLACK[2] + (255,)                                                                 # der Rand der Zipfel
            px[x, y] = col
    return img


#: die Fetzen des zerrissenen Banners: (von x, bis x, Länge)
STRIPS = [(3, 14, 41), (18, 24, 27), (29, 42, 58)]


def _kept(x: int, y: int) -> bool:
    if x < 0 or x >= CW or y >= CH or y < 0:
        return False
    if y <= 3 and 2 <= x <= 43 and not (x > 34 and y == 3):
        return True
    for (a, b, ln) in STRIPS:
        ragged = ln - 6 + int(_h(x, 31) * 9) - abs(x - (a + b) / 2) * 0.7
        if a <= x <= b and y < ragged:
            return True
    return False


def torn() -> Image.Image:
    """Das zerrissene Banner: oben ein Rest des Saums, drei Fetzen hängen herab, ihre Ränder versengt; auf dem längsten ist
    noch ein Stück des Raben zu sehen."""
    full = cloth().load()
    img = Image.new("RGBA", (CW, CH), CLEAR)
    px = img.load()
    for y in range(CH):
        for x in range(CW):
            if not _kept(x, y):
                continue
            col = full[x, y]
            if col[3] == 0:
                continue
            edge = any(not _kept(nx, ny) for nx, ny in ((x - 1, y), (x + 1, y), (x, y + 1), (x, y + 2)))
            if edge and y > 3:
                col = SCORCH[int(_h(x, y, 37) * 2.99)] + (255,)
            px[x, y] = col
    return img


def banner_texture() -> Image.Image:
    """Die Textur des Banners am Querholz (gezeichnet vom Block-Renderer): das Tuch, das zerrissene Tuch, Eichenholz für Pfahl
    und Querholz, Gold für Knauf und Spitzen, Eisen für Beschläge."""
    img = Image.new("RGBA", (128, 128), CLEAR)
    img.alpha_composite(cloth(), (0, 0))
    img.alpha_composite(torn(), (48, 0))
    wood = pole()
    for k in range(4):
        img.alpha_composite(wood, (96, k * 16))
    img.alpha_composite(gold(), (112, 0))
    img.alpha_composite(iron(), (112, 16))
    return img


def icon() -> Image.Image:
    """Das Bild im Inventar: der Pfahl mit goldenem Knauf, das Querholz und das goldene Banner mit dem Raben, unten der
    Schwalbenschwanz."""
    s = st.Spr(16, 16)
    for y in range(1, 16):
        s.set(7, y, ("D", 4))
        s.set(8, y, ("D", 2))
    s.set(7, 0, ("C", 6))
    s.set(8, 0, ("C", 4))
    s.set(7, 15, ("B", 3))
    s.set(8, 15, ("B", 2))
    for x in range(2, 14):
        s.set(x, 2, ("D", 4 if x < 7 else 3))
    s.set(1, 2, ("C", 5))
    s.set(14, 2, ("C", 4))
    raven = {(6, 5), (7, 5), (5, 6), (6, 6), (7, 6), (8, 6), (4, 6), (6, 7), (7, 7), (8, 7), (9, 7), (7, 8), (8, 8), (9, 8),
             (7, 9), (8, 9), (7, 10), (9, 10)}
    for y in range(3, 14):
        for x in range(3, 13):
            if y >= 11 and abs(x - 7.5) < (y - 10) * 1.0:
                continue
            if raven.__contains__((x, y)):
                s.set(x, y, ("B", 1 if (x, y) != (5, 6) else 3))
            elif (x, y) == (6, 5):
                s.set(x, y, ("C", 6))
            else:
                s.set(x, y, ("C", 5 if x < 5 else 4 if x < 10 else 3))
    s.set(6, 6, ("C", 6))                                         # das Auge
    return s.outline(prefer=("B", "C", "D")).image({"B": st.ramp("#3a3842", dark=0.05, light=0.6, cool=240),
                                                     "C": st.ramp("#e0b444", light=0.97, cool=40),
                                                     "D": st.ramp("#6a4a2a", dark=0.12, light=0.8, cool=20)})


def build(put) -> None:
    """Für tools/generate_textures.py."""
    put("block/raven_banner_pole", pole())
    put("block/raven_banner_iron", iron())
    put("entity/raven_banner/raven_banner", banner_texture())
    put("item/raven_banner", icon())


if __name__ == "__main__":
    out = sys.argv[1] if len(sys.argv) > 1 else "/tmp/raven_banner.png"
    tiles = [pole(), iron(), gold(), cloth(), torn(), icon(), banner_texture()]
    sheet = Image.new("RGBA", (sum(t.width * 6 + 16 for t in tiles), 128 * 6), (96, 104, 112, 255))
    x = 0
    for t in tiles:
        sheet.alpha_composite(t.resize((t.width * 6, t.height * 6), Image.NEAREST), (x, 0))
        x += t.width * 6 + 16
    sheet.save(out)
    print(out)
