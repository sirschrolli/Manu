#!/usr/bin/env python3
"""
Der Schlangentempel und seine Hüterin (seit 1.10): der Schlangenaltar und die Beute Quetzaras.

  * serpent_altar_stone – der Altarstein: bemooster, gemeißelter Fels, ringsum eine Federschlange als Relief (Jade mit
    goldenen Schuppen, die Federn rot und gold);
  * serpent_altar_top – die Deckplatte: Fels mit einem eingelegten Ring aus Gold und vier Federn;
  * serpent_altar_plinth – der Sockel: grober, bemooster Stein;
  * serpent_altar_gold – das Schlangenbild: getriebenes Gold mit Schuppen;
  * serpent_altar_feather – die Federn des Schlangenbilds: Smaragd mit Goldkiel;
  * serpent_altar_eye – die Smaragdaugen (leuchten, wenn sie wacht);
  * item/serpent_altar – das Bild im Inventar: der Altar mit dem goldenen Schlangenbild;
  * item/quetzal_feather – eine Quetzalfeder: smaragdgrün mit türkisem Glanz, goldener Kiel, rote Spitze.

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

STONE = [(0x2c, 0x30, 0x2a), (0x3c, 0x41, 0x39), (0x4d, 0x53, 0x4a), (0x60, 0x66, 0x5b), (0x74, 0x7a, 0x6e), (0x88, 0x8d, 0x80),
         (0x9c, 0xa0, 0x92)]
MOSS = [(0x1e, 0x36, 0x14), (0x2c, 0x4a, 0x1c), (0x3c, 0x60, 0x24), (0x4e, 0x76, 0x2e), (0x62, 0x8c, 0x3a)]
JADE = [(0x0c, 0x34, 0x22), (0x13, 0x4a, 0x30), (0x1c, 0x63, 0x40), (0x27, 0x7e, 0x52), (0x36, 0x9a, 0x66), (0x52, 0xb8, 0x80),
        (0x7c, 0xd4, 0xa0)]
GOLD = [(0x5a, 0x3a, 0x08), (0x86, 0x5a, 0x10), (0xb2, 0x80, 0x1c), (0xd8, 0xa4, 0x30), (0xf0, 0xc6, 0x4c), (0xfc, 0xe2, 0x80),
        (0xff, 0xf6, 0xc8)]
RED = [(0x4a, 0x0a, 0x0c), (0x78, 0x12, 0x14), (0xa8, 0x1e, 0x1c), (0xd0, 0x3a, 0x28), (0xec, 0x64, 0x3a)]
EMERALD = [(0x06, 0x30, 0x1a), (0x0c, 0x5a, 0x30), (0x14, 0x90, 0x4a), (0x24, 0xc8, 0x66), (0x5c, 0xf0, 0x96), (0xa8, 0xff, 0xcc),
           (0xea, 0xff, 0xf2)]


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


def _stone_level(x: int, y: int, seed: int) -> float:
    """Gemeißelter Fels: Quader mit dunklen Fugen, leicht fleckig."""
    level = 3.2 + (_h(x // 3, y // 2, seed) - 0.5) * 1.2 + (_h(x, y, seed + 1) - 0.5) * 0.6
    row = y // 4
    if y % 4 == 0 or (x + (row % 2) * 4) % 8 == 0:
        level = 1.4
    elif y % 4 == 1:
        level += 0.6
    return level


def plinth() -> Image.Image:
    img = Image.new("RGBA", (16, 16), CLEAR)
    px = img.load()
    for y in range(16):
        for x in range(16):
            px[x, y] = _c(STONE, _stone_level(x, y, 3))
            if _h(x // 2, y // 2, 9) < 0.28 and y > 6:
                px[x, y] = _c(MOSS, 2 + _h(x, y, 10) * 2)
    return img


def _snake_y(x: float) -> float:
    """Die Mittellinie der Reliefschlange (in Zeilen der Textur)."""
    return 9.5 + 2.0 * math.sin(x / 16.0 * 2.0 * math.pi)


def stone() -> Image.Image:
    """Der Altarstein: Fels mit einer Federschlange im Relief (sie windet sich um alle vier Seiten)."""
    img = Image.new("RGBA", (16, 16), CLEAR)
    px = img.load()
    for y in range(16):
        for x in range(16):
            level = _stone_level(x, y, 5)
            col = _c(STONE, level)
            cy = _snake_y(x + 0.5)
            d = abs(y + 0.5 - cy)
            if d < 1.6:
                # der Leib: Jade, oben hell, mit goldenen Schuppen
                lvl = 4.5 - (y + 0.5 - cy) * 0.9
                col = _c(JADE, lvl)
                if (x + y) % 3 == 0:
                    col = _c(GOLD, 3.5)
            elif d < 2.4:
                col = _c(STONE, 1.2)                                  # die Kerbe um das Relief
            # Federn über dem Leib (alle vier Pixel eine)
            if y + 0.5 < cy - 1.6 and y + 0.5 > cy - 4.2 and x % 4 == 1:
                col = _c(RED, 3) if y + 0.5 < cy - 3.2 else _c(GOLD, 4)
            if y < 2 and _h(x, 0, 13) < 0.5:
                col = _c(MOSS, 2 + _h(x, y, 14) * 2)                   # Moos an der Kante
            px[x, y] = col
    return img


def top() -> Image.Image:
    """Die Deckplatte: Fels, darin ein Ring aus Gold mit vier Federn (nach den Himmelsrichtungen)."""
    img = Image.new("RGBA", (16, 16), CLEAR)
    px = img.load()
    for y in range(16):
        for x in range(16):
            dx = x - 7.5
            dy = y - 7.5
            r = math.hypot(dx, dy)
            col = _c(STONE, 3.4 + (_h(x // 2, y // 2, 21) - 0.5) * 1.0)
            if 5.2 < r < 6.6:
                col = _c(GOLD, 4.2 - (dx + dy) * 0.08)
            elif 6.6 <= r < 7.2 or 4.6 < r <= 5.2:
                col = _c(STONE, 1.5)
            if (abs(dx) < 0.6 or abs(dy) < 0.6) and 5.0 < r < 7.5:
                col = _c(RED, 3) if r > 6.6 else _c(JADE, 5)
            if x in (0, 15) or y in (0, 15):
                col = _c(STONE, 2.0)
            px[x, y] = col
    return img


def gold() -> Image.Image:
    """Das Schlangenbild: getriebenes Gold, schuppig."""
    img = Image.new("RGBA", (16, 16), CLEAR)
    px = img.load()
    for y in range(16):
        for x in range(16):
            sx = (x + (y // 2 % 2) * 2) % 4
            level = 4.2 - (x + y) / 30.0 * 1.4
            if sx == 0:
                level -= 1.3
            elif sx == 1:
                level += 0.8
            if y % 2 == 1 and sx in (2, 3):
                level -= 0.4
            px[x, y] = _c(GOLD, level)
    return img


def feather() -> Image.Image:
    """Die Federn des Schlangenbilds: Smaragd, der Kiel aus Gold, die Spitze rot."""
    img = Image.new("RGBA", (16, 16), CLEAR)
    px = img.load()
    for y in range(16):
        for x in range(16):
            if x in (7, 8):
                px[x, y] = _c(GOLD, 4 if x == 7 else 3)
            elif y < 3:
                px[x, y] = _c(RED, 3 + (1 if x < 8 else 0))
            else:
                px[x, y] = _c(JADE, 5 - y / 16.0 * 2.5 - (0.6 if (x + y) % 3 == 0 else 0.0))
    return img


def eye() -> Image.Image:
    img = Image.new("RGBA", (16, 16), CLEAR)
    px = img.load()
    for y in range(16):
        for x in range(16):
            r = math.hypot(x - 7.5, y - 7.5) / 8.0
            px[x, y] = _c(EMERALD, 5.6 - r * 3.0)
    return img


def altar_icon() -> Image.Image:
    """Der Altar im Inventar: der Stein mit der Reliefschlange, obenauf das goldene Schlangenbild mit Smaragdauge."""
    s = st.Spr(16, 16)
    s.draw("""
................
.......kl.......
......klmk......
.....Ulmmmk.....
......llmk......
.......lk.......
......kllmk.....
.....klmmmlk....
...4555555554...
...3KLMK3KLM3...
...3MLK3MLKK3...
...3333333333...
..444444444444..
..222222222222..
................
................
""")
    return s.outline(prefer=("C", "G", "F", "A")).image({
        "A": st.ramp("#7c8272", dark=0.2, light=0.9, cool=120),
        "C": st.ramp("#e8b440", light=0.97, cool=40),
        "F": st.ramp("#2f9a62", dark=0.15, light=0.88, cool=200),
        "G": st.fixed("#06301a", "#0c5a30", "#14904a", "#24c866", "#5cf096", "#a8ffcc", "#eafff2"),
    })


def feather_icon() -> Image.Image:
    """Eine Quetzalfeder im Inventar: schräg, der Kiel golden, die Fahne smaragdgrün mit türkisem Glanz, die Spitze rot."""
    s = st.Spr(16, 16)
    ax, ay, bx, by = 1.5, 14.5, 13.5, 1.5                  # vom Kiel zur Spitze
    lx, ly = bx - ax, by - ay
    length = math.hypot(lx, ly)
    for y in range(16):
        for x in range(16):
            px, py = x + 0.5 - ax, y + 0.5 - ay
            u = (px * lx + py * ly) / (length * length)       # 0 = Kiel, 1 = Spitze
            v = (px * ly - py * lx) / length                   # quer (Abstand von der Achse)
            if u < 0.0 or u > 1.02:
                continue
            width = 0.0 if u < 0.22 else 2.4 * math.sin(math.pi * min(1.0, (u - 0.22) / 0.82)) ** 0.6
            if abs(v) <= 0.55:
                s.set(x, y, ("C", 5 if v < 0 else 4))          # der Kiel (Gold)
            elif abs(v) <= width:
                if u > 0.86:
                    s.set(x, y, ("C", 3 if v < 0 else 2))       # die rote Spitze
                else:
                    lvl = 3 + (1 if v < 0 else 0) + (1 if u > 0.55 else 0)
                    s.set(x, y, ("G", lvl))
    return s.outline(prefer=("C", "G")).image({
        "C": st.fixed("#4a0a0c", "#7a1214", "#b8281e", "#e04a2c", "#e8b440", "#f6d270", "#fff0b8"),
        "G": st.fixed("#0a3020", "#125234", "#1b7448", "#28985c", "#3cbc78", "#6ed6c0", "#a8f0e0"),
    })


def build(put) -> None:
    """Für tools/generate_textures.py."""
    put("block/serpent_altar_stone", stone())
    put("block/serpent_altar_top", top())
    put("block/serpent_altar_plinth", plinth())
    put("block/serpent_altar_gold", gold())
    put("block/serpent_altar_feather", feather())
    put("block/serpent_altar_eye", eye())
    put("item/serpent_altar", altar_icon())
    put("item/quetzal_feather", feather_icon())


if __name__ == "__main__":
    out = sys.argv[1] if len(sys.argv) > 1 else "/tmp/serpent.png"
    tiles = [stone(), top(), plinth(), gold(), feather(), eye(), altar_icon(), feather_icon()]
    sheet = Image.new("RGBA", (len(tiles) * (16 * 10 + 10), 160), (139, 139, 139, 255))
    for i, t in enumerate(tiles):
        sheet.alpha_composite(t.resize((160, 160), Image.NEAREST), (i * 170, 0))
    sheet.save(out)
    print(out)
