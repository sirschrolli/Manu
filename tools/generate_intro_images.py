#!/usr/bin/env python3
"""
Erzeugt die beiden Bilder für die Titelkarte am Ende des Erzähl-Intros:
  - textures/gui/intro_title.png:          das Kingdoms-at-War-Banner mit weich auslaufendem Rand
  - textures/gui/intro_dragon_warfare.png: der Dragon-Warfare-Schriftzug, freigestellt (durchsichtiger Hintergrund)

Quellen sind die Konzeptbilder des Projekts (kingdomsatwar_banner.png und docs/bilder/dragon-warfare-erze.jpg).

Aufruf (aus dem Projektordner): python3 tools/generate_intro_images.py   (benötigt: pip install pillow)
"""
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parent.parent
GUI = ROOT / "src" / "main" / "resources" / "assets" / "kingdomsatwar" / "textures" / "gui"


def smooth(a: float) -> float:
    a = max(0.0, min(1.0, a))
    return a * a * (3 - 2 * a)


def feathered_banner() -> Image.Image:
    banner = Image.open(ROOT / "src" / "main" / "resources" / "kingdomsatwar_banner.png").convert("RGBA")
    w, h = banner.size
    mask = Image.new("L", (w, h))
    px = mask.load()
    for y in range(h):
        for x in range(w):
            px[x, y] = int(smooth(min(min(x, w - 1 - x) / 70.0, min(y, h - 1 - y) / 26.0)) * 255)
    banner.putalpha(mask)
    return banner


def dragon_warfare_logo() -> Image.Image:
    """Goldener Schriftzug auf dunklem Grund: Helligkeit und Wärme bestimmen die Deckkraft."""
    src = Image.open(ROOT / "docs" / "bilder" / "dragon-warfare-erze.jpg").convert("RGB").crop((0, 5, 600, 115))
    out = Image.new("RGBA", src.size)
    sp, op = src.load(), out.load()
    for y in range(src.height):
        for x in range(src.width):
            r, g, b = sp[x, y]
            light = (0.299 * r + 0.587 * g + 0.114 * b) / 255
            warm = (r - b) / 255
            alpha = max(0.0, min(1.0, (light - 0.16) / 0.22)) * max(0.0, min(1.0, (warm + 0.05) / 0.15))
            op[x, y] = (r, g, b, int(alpha * 255))
    return out.crop(out.getbbox())


def main() -> None:
    GUI.mkdir(parents=True, exist_ok=True)
    feathered_banner().save(GUI / "intro_title.png")
    dragon_warfare_logo().save(GUI / "intro_dragon_warfare.png")
    for name in ("intro_title.png", "intro_dragon_warfare.png"):
        img = Image.open(GUI / name)
        print(f"{name}: {img.size[0]}x{img.size[1]}")


if __name__ == "__main__":
    main()
