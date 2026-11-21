#!/usr/bin/env python3
"""Die Herzen der Heldenherzen-Leiste (seit 0.77) und die Panzer der Rüstungsleiste in Schichten (seit 0.78): eine Herzfüllung in Grautönen, die das Spiel in der Farbe ihrer
Schicht tönt (Orange, Rosa, Violett, Blau, Türkis, Smaragd, Silber), dieselbe als halbes Herz und ein weißer Glanz, der
ungetönt darüberliegt. Je 9 × 9 Pixel, passend in den Herzrahmen von Minecraft (die Füllung liegt in seinem Inneren).

Die Panzer (seit 0.78): ein Harnisch in Grautönen mit Umriss, Grat und Glanz, ebenso getönt; dazu der halbe.

Aufruf: python3 tools/hero_hearts.py  (schreibt nach src/main/resources/assets/kingdomsatwar/textures/gui/sprites/hero/heart
und …/hero/armor)
"""
import os

from PIL import Image

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
OUT = os.path.join(ROOT, "src/main/resources/assets/kingdomsatwar/textures/gui/sprites/hero/heart")

# das Innere eines Herzens (7 Pixel breit), Zeile 1 bis 7
SHAPE = [
    ".........",
    "..XX.XX..",
    ".XXXXXXX.",
    ".XXXXXXX.",
    ".XXXXXXX.",
    "..XXXXX..",
    "...XXX...",
    "....X....",
    ".........",
]


def inside(x, y):
    return SHAPE[y][x] == "X"


def shade(x, y):
    """Helligkeit (0–255) der Füllung: oben hell, zum unteren Rand und rechts dunkler, unten ein Schattensaum."""
    if not inside(x, y):
        return None
    value = 242
    if y == 1:
        value = 252
    if y >= 5:
        value = 226
    if x == 7 and y in (2, 3, 4):
        value = 212
    # Schattensaum: die Pixel am unteren Rand der Form
    below = y + 1 < 9 and inside(x, y + 1)
    if not below and y >= 3:
        value = 176
    elif (not inside(x + 1, y) or not inside(x - 1, y)) and y >= 4:
        value = min(value, 196)
    return value


def fill(half):
    image = Image.new("RGBA", (9, 9), (0, 0, 0, 0))
    for y in range(9):
        for x in range(9):
            if half and x > 4:
                continue
            value = shade(x, y)
            if value is not None:
                image.putpixel((x, y), (value, value, value, 255))
    return image


def shine():
    """Der Glanz oben links: ein heller Punkt mit weichem Saum."""
    image = Image.new("RGBA", (9, 9), (0, 0, 0, 0))
    for (x, y), alpha in {(2, 2): 235, (3, 2): 120, (2, 3): 120, (3, 1): 70, (6, 2): 60}.items():
        image.putpixel((x, y), (255, 255, 255, alpha))
    return image


ARMOR = [
    ".##...##.",
    "#ab#.#ab#",
    "#abb#abb#",
    "#abbcbbd#",
    ".#bbcbd#.",
    ".#abcbd#.",
    ".#abcbd#.",
    ".#bbcdd#.",
    "..#####..",
]
ARMOR_SHADE = {"#": 70, "a": 252, "b": 222, "c": 240, "d": 186}


def armor(half):
    """Ein Harnisch (9×9) in Grautönen: dunkler Umriss, hell links oben, ein Grat in der Mitte, rechts dunkler."""
    image = Image.new("RGBA", (9, 9), (0, 0, 0, 0))
    for y, row in enumerate(ARMOR):
        for x, ch in enumerate(row):
            if ch == ".":
                continue
            v = ARMOR_SHADE[ch]
            if half and x > 4 and ch != "#":
                continue
            if half and x == 4 and ch != "#":
                v = 70
            image.putpixel((x, y), (v, v, v, 255))
    if half:
        # die rechte Hälfte nur als Umriss (darunter liegt die volle Schicht darunter)
        for y, row in enumerate(ARMOR):
            for x, ch in enumerate(row):
                if x > 4 and ch == "#":
                    image.putpixel((x, y), (0, 0, 0, 0))
    return image


def armor_shine():
    image = Image.new("RGBA", (9, 9), (0, 0, 0, 0))
    for (x, y), alpha in {(1, 1): 230, (2, 2): 130, (1, 3): 90, (6, 1): 110}.items():
        image.putpixel((x, y), (255, 255, 255, alpha))
    return image


def main():
    os.makedirs(OUT, exist_ok=True)
    fill(False).save(os.path.join(OUT, "full.png"))
    fill(True).save(os.path.join(OUT, "half.png"))
    shine().save(os.path.join(OUT, "shine.png"))
    armor_out = os.path.join(os.path.dirname(OUT), "armor")
    os.makedirs(armor_out, exist_ok=True)
    armor(False).save(os.path.join(armor_out, "full.png"))
    armor(True).save(os.path.join(armor_out, "half.png"))
    armor_shine().save(os.path.join(armor_out, "shine.png"))
    print("Herzen und Panzer geschrieben nach", os.path.dirname(OUT))


if __name__ == "__main__":
    main()
