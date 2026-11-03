#!/usr/bin/env python3
"""
Mana-Leiste (Anzeige über der Schnellleiste, solange man einen Aetherit-Stab hält): links ein Mana-Kristall, daneben
eine Rinne mit Rahmen; die Füllung ist ein kühles Blau mit hellem Rand oben und ein paar Funken.

  textures/gui/sprites/mana/bar_background.png   82 x 9   Kristall, Rahmen, leere Rinne
  textures/gui/sprites/mana/bar_fill.png         82 x 9   Füllung (nur die Rinne, x 12..79, y 2..6)

Aufruf:  python3 tools/mana_hud.py [vorschau.png]
"""
from __future__ import annotations

import sys
from pathlib import Path

from PIL import Image

W, H = 82, 9
OUTLINE = (20, 24, 58, 255)
FRAME = (44, 50, 98, 255)
FRAME_LIGHT = (70, 80, 140, 255)
TROUGH = (11, 16, 40, 255)
TROUGH_SHADOW = (6, 9, 26, 255)

# Mana-Kristall (9 x 9): o Umriss, 1..5 von hell nach dunkel, w Glanz
CRYSTAL = [
    "....o....",
    "...o1o...",
    "..o1w2o..",
    ".o12233o.",
    "o1223344o",
    ".o23344o.",
    "..o3445o.",
    "...o45o..",
    "....oo...",
]
CRYSTAL_COLORS = {
    "o": OUTLINE,
    "w": (255, 255, 255, 255),
    "1": (196, 244, 255, 255),
    "2": (120, 210, 255, 255),
    "3": (70, 150, 250, 255),
    "4": (40, 96, 214, 255),
    "5": (26, 60, 160, 255),
}
FILL_ROWS = [
    (182, 240, 255),
    (111, 208, 255),
    (63, 155, 255),
    (42, 114, 232),
    (31, 84, 196),
]
SPARKS = [(17, 3), (29, 4), (40, 3), (52, 5), (63, 3), (74, 4)]


def background() -> Image.Image:
    img = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    px = img.load()
    for y, row in enumerate(CRYSTAL):
        for x, c in enumerate(row):
            if c != ".":
                px[x, y] = CRYSTAL_COLORS[c]
    x0, x1 = 10, W - 1
    for x in range(x0 + 1, x1):
        px[x, 0] = OUTLINE
        px[x, H - 1] = OUTLINE
    for y in range(1, H - 1):
        px[x0, y] = OUTLINE
        px[x1, y] = OUTLINE
    for x in range(x0 + 1, x1):
        px[x, 1] = FRAME_LIGHT
        px[x, H - 2] = FRAME
    for y in range(1, H - 1):
        px[x0 + 1, y] = FRAME
        px[x1 - 1, y] = FRAME
    for x in range(x0 + 2, x1 - 1):
        for y in range(2, H - 2):
            px[x, y] = TROUGH_SHADOW if y == 2 else TROUGH
    # kleine Nieten an den Enden des Rahmens
    px[x0 + 1, 1] = (150, 160, 210, 255)
    px[x1 - 1, 1] = (150, 160, 210, 255)
    return img


def fill() -> Image.Image:
    img = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    px = img.load()
    for x in range(12, W - 2):
        for i, y in enumerate(range(2, H - 2)):
            r, g, b = FILL_ROWS[i]
            # ganz leichte Wellen in der Füllung
            wave = ((x * 7 + y * 3) % 11) - 5
            px[x, y] = (max(0, min(255, r + wave)), max(0, min(255, g + wave)), max(0, min(255, b + wave)), 255)
    for x, y in SPARKS:
        px[x, y] = (235, 250, 255, 255)
        if x + 1 < W - 2:
            px[x + 1, y] = (170, 230, 255, 255)
    return img


def main() -> None:
    root = Path(__file__).resolve().parent.parent / "src/main/resources/assets/kingdomsatwar/textures/gui/sprites/mana"
    root.mkdir(parents=True, exist_ok=True)
    bg, fl = background(), fill()
    bg.save(root / "bar_background.png")
    fl.save(root / "bar_fill.png")
    print("Mana-Leiste geschrieben")
    if len(sys.argv) > 1:
        # Vorschau: leer, halb, voll – zehnfach vergrößert
        sheet = Image.new("RGBA", (W, H * 3 + 4), (60, 60, 60, 255))
        for i, part in enumerate((0.0, 0.55, 1.0)):
            row = bg.copy()
            width = round(68 * part)
            if width > 0:
                row.alpha_composite(fl.crop((12, 0, 12 + width, H)), (12, 0))
            sheet.alpha_composite(row, (0, i * (H + 2)))
        sheet.resize((sheet.width * 10, sheet.height * 10), Image.NEAREST).save(sys.argv[1])
        print(f"Vorschau: {sys.argv[1]}")


if __name__ == "__main__":
    main()
