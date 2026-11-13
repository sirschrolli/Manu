#!/usr/bin/env python3
"""
Das Rabenbanner der Raubritterburg (seit 1.8): der Bannerpfahl aus dunkler Eiche mit Eisenbändern und goldenem Knauf, am
Querholz das Banner derer von Rabenfels.

  * raven_banner_pole – dunkle Eiche, längs gemasert;
  * raven_banner_iron – geschwärztes Eisen mit Nieten (Fuß, Bänder, Querholz-Beschläge);
  * raven_banner_gold – Gold (Knauf, Spitzen des Querholzes);
  * raven_banner_cloth (32 x 48) – das Banner: ein goldenes Feld mit sanften Falten und einem schwarzen Saum, darin der
    schwarze Rabe (wie auf Kunos Wappenrock), unten ein schwarzer Streifen mit goldenen Zacken und der Schwalbenschwanz;
  * raven_banner_torn (32 x 48) – das zerrissene Banner: Fetzen hängen vom Querholz, versengt, ein Stück des Raben;
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


def _fold(x: int, y: int) -> float:
    """Die Falten des Tuchs: senkrechte Wellen, die nach unten etwas weiter werden."""
    return 0.7 * math.sin(x * 0.55 + 0.4 + y * 0.02) + 0.35 * math.sin(x * 1.3 + 1.7)


def _raven_at(x: int, y: int, x0: int, y0: int) -> str | None:
    """Das Zeichen des (doppelt großen) Raben bei (x, y), wenn er bei (x0, y0) beginnt."""
    rx, ry = (x - x0) // 2, (y - y0) // 2
    if 0 <= ry < len(RAVEN) and 0 <= rx < len(RAVEN[0]) and x >= x0 and y >= y0:
        ch = RAVEN[ry][rx]
        return None if ch == "." else ch
    return None


def _raven_color(x: int, y: int, x0: int, y0: int, ch: str) -> tuple:
    """Der Rabe: schwarz, Federn mit dunkelgrauen Kanten, das Auge golden, der Schnabel etwas heller."""
    if ch == "o":
        return GOLD[5] + (255,) if (x - x0) % 2 == 0 and (y - y0) % 2 == 0 else BLACK[0] + (255,)
    rx, ry = (x - x0) / 2.0, (y - y0) / 2.0
    if rx < 2.5 and 2 <= ry < 3:
        return BLACK[3] + (255,) if (y - y0) % 2 == 0 else BLACK[2] + (255,)   # der Schnabel
    # Flügelfedern: schräge, hellere Linien auf dem Rumpf
    if 5 <= ry <= 8 and (int(x - x0) + int(y - y0)) % 5 == 0 and rx > 4:
        return BLACK[3] + (255,)
    left = _raven_at(x - 1, y, x0, y0) is None
    top = _raven_at(x, y - 1, x0, y0) is None
    return BLACK[2] + (255,) if left or top else BLACK[1] + (255,)


def _swallowtail(x: int, y: int) -> bool:
    """Ist (x, y) im Banner (32 x 48)? Unten der Schwalbenschwanz: ein Keil in der Mitte, die Ecken schräg."""
    if y >= 38 and abs(x - 15.5) < (y - 37.0) * 1.05:
        return False
    if y >= 42 and (x < (y - 42) * 0.7 or x > 31 - (y - 42) * 0.7):
        return False
    return True


def cloth() -> Image.Image:
    img = Image.new("RGBA", (32, 48), CLEAR)
    px = img.load()
    x0, y0 = 2, 8
    for y in range(48):
        for x in range(32):
            if not _swallowtail(x, y):
                continue
            fold = _fold(x, y)
            col = _c(CLOTH, 3.2 + fold + (0.4 if _h(x, y, 21) < 0.12 else 0.0) - (0.5 if _h(x, y, 23) < 0.08 else 0.0))
            if y <= 2:
                col = _c(CLOTH, 1.6 + (0.8 if y == 0 else 0.0) + 0.4 * math.sin(x * 0.8))      # der Saum um das Querholz
            elif x in (0, 31) or y == 3:
                col = BLACK[1] + (255,)                                                        # schwarzer Rand
            elif x in (1, 30):
                col = _c(CLOTH, 1.6 + fold)
            ch = _raven_at(x, y, x0, y0)
            if ch is not None:
                col = _raven_color(x, y, x0, y0, ch)
            # unten ein schwarzer Streifen mit goldenen Zacken (wie der gezackte Saum seiner Schabracke)
            if 34 <= y <= 37:
                zig = (x % 4) if (x // 4) % 2 == 0 else 3 - (x % 4)
                col = BLACK[1] + (255,) if y - 34 > zig else _c(CLOTH, 3.6 + fold)
                if y == 37:
                    col = BLACK[1] + (255,)
            if y >= 38 and not _swallowtail(x, y + 1) or y == 47:
                col = BLACK[2] + (255,)                                                        # der Rand der Zipfel
            px[x, y] = col
    return img


def torn() -> Image.Image:
    """Das zerrissene Banner: oben ein Rest des Saums, drei Fetzen hängen herab, ihre Ränder versengt; auf dem längsten ist
    noch ein Stück des Raben zu sehen."""
    full = cloth().load()
    img = Image.new("RGBA", (32, 48), CLEAR)
    px = img.load()
    strips = [(2, 9, 27), (12, 16, 17), (19, 27, 36)]               # (von x, bis x, Länge)
    for y in range(48):
        for x in range(32):
            keep = y <= 2 and 1 <= x <= 28 and not (x > 22 and y == 2)
            for (a, b, ln) in strips:
                ragged = ln - 4 + int(_h(x, 31) * 6) - abs(x - (a + b) / 2) * 0.6
                if a <= x <= b and y < ragged:
                    keep = True
            if not keep:
                continue
            col = full[x, y]
            if col[3] == 0:
                continue
            # versengte Ränder: wo das Tuch endet, ist es braun und dunkel
            edge = any(not _kept(px2x, y2, strips) for px2x, y2 in ((x - 1, y), (x + 1, y), (x, y + 1), (x, y + 2)))
            if edge and y > 2:
                col = SCORCH[int(_h(x, y, 37) * 2.99)] + (255,)
            px[x, y] = col
    return img


def _kept(x: int, y: int, strips) -> bool:
    if x < 0 or x > 31 or y > 47:
        return False
    if y <= 2 and 1 <= x <= 28 and not (x > 22 and y == 2):
        return True
    for (a, b, ln) in strips:
        ragged = ln - 4 + int(_h(x, 31) * 6) - abs(x - (a + b) / 2) * 0.6
        if a <= x <= b and y < ragged:
            return True
    return False


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
    put("block/raven_banner_gold", gold())
    put("block/raven_banner_cloth", cloth())
    put("block/raven_banner_torn", torn())
    put("item/raven_banner", icon())


if __name__ == "__main__":
    out = sys.argv[1] if len(sys.argv) > 1 else "/tmp/raven_banner.png"
    tiles = [pole(), iron(), gold(), cloth(), torn(), icon()]
    sheet = Image.new("RGBA", (sum(t.width * 8 + 16 for t in tiles), 48 * 8), (96, 104, 112, 255))
    x = 0
    for t in tiles:
        sheet.alpha_composite(t.resize((t.width * 8, t.height * 8), Image.NEAREST), (x, 0))
        x += t.width * 8 + 16
    sheet.save(out)
    print(out)
