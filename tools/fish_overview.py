#!/usr/bin/env python3
"""
Übersicht aller eigenen Fischbilder (docs/bilder/tausend_fische.jpg): erst die von Hand gestalteten, dann die Arten der Biome
in der Reihenfolge des Fischbuchs, jede auf der Farbe ihres Kapitels.

Aufruf: python3 tools/fish_overview.py ausgabe.jpg [Breite]
"""
import os, sys
from PIL import Image, ImageDraw, ImageFont
ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__))) + "/"
TEX = ROOT + "src/main/resources/assets/kingdomsatwar/textures/item/fish/"
out = sys.argv[1]
width = int(sys.argv[2]) if len(sys.argv) > 2 else 1936
order = []
# erst die von Hand gestalteten (in der Reihenfolge der Klasse), dann die der Biome (Tabelle)
import re
src = open(ROOT + "src/main/java/io/github/sirschrolli/kingdomsatwar/fishing/FishSpecies.java", encoding="utf-8").read()
for m in re.finditer(r'= add\(b\("([a-z_]+)"', src):
    if os.path.exists(TEX + m.group(1) + ".png"):
        order.append((m.group(1), "named"))
for line in open(ROOT + "src/main/resources/data/kingdomsatwar/fishing/species.tsv", encoding="utf-8"):
    if line.startswith("#") or not line.strip():
        continue
    f = line.split("\t")
    order.append((f[0], f[8]))
BG = {"named": (58, 46, 30), "RIVERS": (38, 74, 104), "SEA": (24, 58, 110), "DEEP": (12, 26, 62), "CAVES": (44, 40, 46), "NETHER": (96, 36, 20),
      "END": (30, 20, 50), "FAE": (70, 40, 84), "SHADOW": (30, 22, 48), "ABYSS": (40, 24, 30)}
scale = 2
cell = 16 * scale + 4
cols = (width - 8) // cell
rows = (len(order) + cols - 1) // cols
img = Image.new("RGB", (cols * cell + 8, rows * cell + 8), (22, 26, 34))
for i, (name, chapter) in enumerate(order):
    x = 4 + (i % cols) * cell
    y = 4 + (i // cols) * cell
    tile = Image.new("RGBA", (cell - 2, cell - 2), BG[chapter] + (255,))
    sprite = Image.open(TEX + name + ".png").convert("RGBA").resize((16 * scale, 16 * scale), Image.NEAREST)
    tile.alpha_composite(sprite, (1, 1))
    img.paste(tile.convert("RGB"), (x + 1, y + 1))
img.save(out, quality=92)
print(len(order), img.size)
