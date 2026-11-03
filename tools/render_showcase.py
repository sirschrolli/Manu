#!/usr/bin/env python3
"""
Zeichnet eine Übersicht aller neuen Blöcke (als 3D-Würfel) und Gegenstände
nach docs/bilder/inhalte-phase-1.png.

Hinweis: Die Erze werden hier auf einem nachgemachten grauen Stein gezeigt.
Im Spiel liegt darunter die echte Stein- bzw. Tiefenschiefer-Textur.

Aufruf: python3 tools/render_showcase.py   (benötigt: pip install pillow)
"""
from __future__ import annotations

import sys
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont

sys.path.insert(0, str(Path(__file__).resolve().parent))
import pixelart as pa  # noqa: E402

ROOT = Path(__file__).resolve().parent.parent
TEX = ROOT / "src" / "main" / "resources" / "assets" / "kingdomsatwar" / "textures"
OUT = ROOT / "docs" / "bilder" / "inhalte-phase-1.png"

S = 48  # Kantenlänge einer Würfelfläche in Pixeln
BG = (34, 38, 44, 255)
TEXT = (235, 225, 200, 255)
HEAD = (230, 190, 100, 255)


def font(size: int, bold: bool = False):
    name = "DejaVuSans-Bold.ttf" if bold else "DejaVuSans.ttf"
    try:
        return ImageFont.truetype(f"/usr/share/fonts/truetype/dejavu/{name}", size)
    except OSError:
        return ImageFont.load_default()


def tex(kind: str, name: str) -> Image.Image:
    return Image.open(TEX / kind / f"{name}.png").convert("RGBA")


def fake_rock(dark: bool) -> Image.Image:
    """Nachgemachter Stein bzw. Tiefenschiefer (eigene Pixel, keine Vanilla-Datei)."""
    pal = pa.palette("#3a3a41", "#46464d", "#55555b", "#626267") if dark else \
        pa.palette("#6a6a6a", "#767676", "#818181", "#8e8e8e")
    noise = pa.fractal_noise(7 if dark else 3, ((4, 0.5), (2, 0.35), (8, 0.15)))
    img = Image.new("RGBA", (16, 16))
    for y in range(16):
        for x in range(16):
            img.putpixel((x, y), pa.pick(pal, noise[y][x]))
    return img


def shade(img: Image.Image, factor: float) -> Image.Image:
    r, g, b, a = img.split()
    r = r.point(lambda v: int(v * factor))
    g = g.point(lambda v: int(v * factor))
    b = b.point(lambda v: int(v * factor))
    return Image.merge("RGBA", (r, g, b, a))


def cube(top: Image.Image, side: Image.Image | None = None) -> Image.Image:
    side = side or top
    t = top.resize((S, S), Image.NEAREST)
    sd = side.resize((S, S), Image.NEAREST)
    out = Image.new("RGBA", (2 * S, 2 * S), (0, 0, 0, 0))
    clear = (0, 0, 0, 0)
    top_face = t.transform((2 * S, 2 * S), Image.AFFINE, (0.5, -1, S / 2, 0.5, 1, -S / 2), Image.NEAREST, fillcolor=clear)
    left = shade(sd, 0.82).transform((2 * S, 2 * S), Image.AFFINE, (1, 0, 0, -0.5, 1, -S / 2), Image.NEAREST, fillcolor=clear)
    right = shade(sd, 0.62).transform((2 * S, 2 * S), Image.AFFINE, (1, 0, -S, 0.5, 1, -1.5 * S), Image.NEAREST, fillcolor=clear)
    out.alpha_composite(right)
    out.alpha_composite(left)
    out.alpha_composite(top_face)
    return out


def flat(img: Image.Image) -> Image.Image:
    out = Image.new("RGBA", (2 * S, 2 * S), (0, 0, 0, 0))
    big = img.resize((int(1.5 * S), int(1.5 * S)), Image.NEAREST)
    out.alpha_composite(big, ((2 * S - big.width) // 2, (2 * S - big.height) // 2))
    return out


def egg(name: str) -> Image.Image:
    pattern = tex("block", name).resize((S * 2, S * 2), Image.NEAREST)
    mask = Image.new("L", (2 * S, 2 * S), 0)
    d = ImageDraw.Draw(mask)
    d.ellipse((S * 0.45, S * 0.15, S * 1.55, S * 1.95), fill=255)
    out = Image.new("RGBA", (2 * S, 2 * S), (0, 0, 0, 0))
    out.paste(pattern, (0, 0), mask)
    shadow = Image.new("L", (2 * S, 2 * S), 0)
    ImageDraw.Draw(shadow).ellipse((S * 0.95, S * 0.3, S * 1.6, S * 1.95), fill=90)
    out.paste(Image.new("RGBA", (2 * S, 2 * S), (0, 0, 0, 255)), (0, 0), Image.composite(shadow, Image.new("L", shadow.size, 0), mask))
    return out


def main() -> None:
    stone = fake_rock(False)
    deep = fake_rock(True)

    def ore(name: str, base: Image.Image) -> Image.Image:
        img = base.copy()
        img.alpha_composite(tex("block", f"{name}_ore_overlay"))
        glow = TEX / "block" / f"{name}_ore_glow.png"
        if glow.exists():  # Leuchtebene (im Spiel ohne Schatten gezeichnet)
            img.alpha_composite(Image.open(glow).convert("RGBA"))
        return cube(img)

    sections: list[tuple[str, list[tuple[str, Image.Image]]]] = [
        ("Kingdoms at War – Burgen & Bauen", [
            ("Mittelalterl. Stein", cube(tex("block", "medieval_stone"))),
            ("Grober Naturstein", cube(tex("block", "rough_fieldstone"))),
            ("Behauener Sandstein", cube(tex("block", "hewn_sandstone"))),
            ("Dunkle Steinziegel", cube(tex("block", "dark_stone_bricks"))),
            ("Kalksteinziegel", cube(tex("block", "limestone_bricks"))),
            ("Verwitterte Ziegel", cube(tex("block", "weathered_stone_bricks"))),
            ("Moosige Mauersteine", cube(tex("block", "mossy_wall_stones"))),
            ("Gerissene Steine", cube(tex("block", "cracked_stone_blocks"))),
            ("Steinpfeiler", cube(tex("block", "stone_pillar_top"), tex("block", "stone_pillar_side"))),
            ("Rote Dachziegel", cube(tex("block", "red_roof_tiles"))),
            ("Braune Dachziegel", cube(tex("block", "brown_roof_tiles"))),
            ("Helle Dachziegel", cube(tex("block", "light_roof_tiles"))),
            ("Schieferplatten", cube(tex("block", "dark_slate_shingles"))),
            ("Holzschindeln", cube(tex("block", "wooden_shingles"))),
            ("Kupferdach", cube(tex("block", "copper_roof"))),
            ("Holzbalken", cube(tex("block", "timber_beam_top"), tex("block", "timber_beam_side"))),
            ("Fachwerk", cube(tex("block", "timber_frame"))),
            ("Fachwerk diagonal", cube(tex("block", "timber_frame_diagonal"))),
            ("Fachwerk Kreuz", cube(tex("block", "timber_frame_cross"))),
        ]),
        ("Dragon Warfare – Erze & Rohstoffe", [
            ("Kohlenstofferz", ore("carbon", stone)),
            ("Edelstein-Erz", ore("gemstone", deep)),
            ("Konteriterz", ore("konterit", stone)),
            ("Aetheriterz", ore("aetherit", stone)),
            ("Draconiumerz", ore("draconium", deep)),
            ("Wyverniterz", ore("wyvernit", stone)),
            ("Kohlenstoffblock", cube(tex("block", "carbon_block"))),
            ("Edelsteinblock", cube(tex("block", "gemstone_block"))),
            ("Konteritblock", cube(tex("block", "konterit_block"))),
            ("Aetheritblock", cube(tex("block", "aetherit_block"))),
            ("Draconiumblock", cube(tex("block", "draconium_block"))),
            ("Wyvernitblock", cube(tex("block", "wyvernit_block"))),
            ("Stahlblock", cube(tex("block", "steel_block"))),
            ("Kohlenstoff", flat(tex("item", "carbon"))),
            ("Edelstein", flat(tex("item", "gemstone"))),
            ("Konterit", flat(tex("item", "konterit"))),
            ("Aetherit", flat(tex("item", "aetherit"))),
            ("Draconium", flat(tex("item", "draconium"))),
            ("Wyvernit", flat(tex("item", "wyvernit"))),
            ("Stahlmischung", flat(tex("item", "steel_blend"))),
            ("Stahlbarren", flat(tex("item", "steel_ingot"))),
        ]),
        ("Dragon Warfare – Drachen", [
            ("Feuerdrachen-Ei", egg("fire_dragon_egg")),
            ("Eisdrachen-Ei", egg("ice_dragon_egg")),
            ("Blitzdrachen-Ei", egg("lightning_dragon_egg")),
            ("Naturdrachen-Ei", egg("nature_dragon_egg")),
            ("Erddrachen-Ei", egg("earth_dragon_egg")),
            ("Wasserdrachen-Ei", egg("water_dragon_egg")),
            ("Feuer-Schuppe", flat(tex("item", "fire_dragon_scale"))),
            ("Eis-Schuppe", flat(tex("item", "ice_dragon_scale"))),
            ("Blitz-Schuppe", flat(tex("item", "lightning_dragon_scale"))),
            ("Natur-Schuppe", flat(tex("item", "nature_dragon_scale"))),
            ("Erd-Schuppe", flat(tex("item", "earth_dragon_scale"))),
            ("Wasser-Schuppe", flat(tex("item", "water_dragon_scale"))),
            ("Drachenfutter", flat(tex("item", "dragon_food"))),
        ]),
        ("Kingdoms at War – Landwirtschaft & Nahrung", [
            ("Tomaten (reif)", flat(tex("block", "tomatoes_stage3"))),
            ("Kohl (reif)", flat(tex("block", "cabbages_stage3"))),
            ("Zwiebeln (reif)", flat(tex("block", "onions_stage3"))),
            ("Gerste (reif)", flat(tex("block", "barley_stage3"))),
            ("Tomate", flat(tex("item", "tomato"))),
            ("Kohl", flat(tex("item", "cabbage"))),
            ("Zwiebel", flat(tex("item", "onion"))),
            ("Gerste", flat(tex("item", "barley"))),
            ("Tomatensamen", flat(tex("item", "tomato_seeds"))),
            ("Kohlsamen", flat(tex("item", "cabbage_seeds"))),
            ("Gerstensamen", flat(tex("item", "barley_seeds"))),
            ("Brötchen", flat(tex("item", "bread_roll"))),
            ("Gemüseeintopf", flat(tex("item", "vegetable_stew"))),
            ("Fleischpastete", flat(tex("item", "meat_pie"))),
            ("Apfelkuchen", flat(tex("item", "apple_pie"))),
            ("Lavendel", flat(tex("block", "lavender"))),
            ("Gartenrose", flat(tex("block", "garden_rose"))),
            ("Feuerlilie", flat(tex("block", "fire_lily"))),
            ("Frostblume", flat(tex("block", "frost_flower"))),
        ]),
    ]

    cols = 11
    cell_w, cell_h = 2 * S + 30, 2 * S + 26
    head_h = 40
    pad = 20
    title_h = 70
    height = title_h + pad
    for _, entries in sections:
        rows = (len(entries) + cols - 1) // cols
        height += head_h + rows * cell_h + pad
    width = pad * 2 + cols * cell_w
    sheet = Image.new("RGBA", (width, height), BG)
    draw = ImageDraw.Draw(sheet)
    draw.text((pad, 18), "Kingdoms at War – Inhalte der Testversion 0.1.0", font=font(30, True), fill=HEAD)

    y = title_h + pad
    label_font = font(12)
    for title, entries in sections:
        draw.text((pad, y + 8), title, font=font(20, True), fill=HEAD)
        y += head_h
        for i, (label, img) in enumerate(entries):
            cx = pad + (i % cols) * cell_w
            cy = y + (i // cols) * cell_h
            sheet.alpha_composite(img, (cx + (cell_w - img.width) // 2, cy))
            tw = draw.textlength(label, font=label_font)
            draw.text((cx + (cell_w - tw) / 2, cy + 2 * S + 6), label, font=label_font, fill=TEXT)
        y += ((len(entries) + cols - 1) // cols) * cell_h + pad
    OUT.parent.mkdir(parents=True, exist_ok=True)
    sheet.convert("RGB").save(OUT, optimize=True)
    print(f"Geschrieben: {OUT}")


if __name__ == "__main__":
    main()
