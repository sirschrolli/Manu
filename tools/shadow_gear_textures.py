#!/usr/bin/env python3
"""
Texturen der Schätze der Schattenwelt (Rohstoffe, Werkzeuge, Waffen, Rüstungen; Erze und Blöcke: tools/shadow_blocks.py):

  * Mondsilber   – fahles, bläuliches Silber (Erz in Schatten- und Tiefschattenstein, Rohmondsilber, Barren, Klümpchen,
                   Block, Mondlaterne, die Mondsilberklinge)
  * Nachtstahl   – tiefblauschwarzer Stahl mit violettem Schimmer (Erz, Rohnachtstahl, Barren, Block, Werkzeuge, Rüstung)
  * Seelenkristall – geisterhaft türkiser Kristall aus der Tiefe (Erz mit Leuchtebene, Kristall, Block)
  * Abyssit      – schwarzes Metall aus dem Abgrund mit glühenden magentafarbenen Adern (Erz, Splitter, Barren, Block,
                   Schmiedevorlage, Werkzeuge, Rüstung)
  * Nachtkristallsplitter, Schattensetzling

Wird von tools/generate_textures.py aufgerufen: build(put) für Blöcke und Gegenstände, build_entity(save) für die
getragenen Rüstungen.
"""
from __future__ import annotations

import math
from typing import Callable

from PIL import Image

import building_textures as bld
import equipment_textures as et
import pixelart as pa
from pixelart import Color, hexc

N = 16


def tones6(o: str, a: str, b: str, c: str, d: str, w: str) -> dict[str, Color]:
    """Töne für die Rohstoff-Symbole: o Umriss, a..d dunkel -> hell, w Glanz."""
    return {"o": hexc(o), "a": hexc(a), "b": hexc(b), "c": hexc(c), "d": hexc(d), "w": hexc(w)}


# Rohstoff-Farben
MOON = tones6("#2e3648", "#5c6a84", "#8796b0", "#b2c0d6", "#d8e4f2", "#ffffff")
NIGHT = tones6("#05040c", "#14122c", "#232048", "#363066", "#54498e", "#a294e6")
SOUL = tones6("#073236", "#10686c", "#22a4a2", "#52d8cc", "#a2fbef", "#ffffff")
ABYSS = tones6("#040206", "#120818", "#22102a", "#381640", "#5c2460", "#ff6ae8")
NCRYSTAL = tones6("#141244", "#2a2878", "#4442b4", "#6a62e2", "#a49cff", "#efeaff")

# Werkstoff-Töne für Werkzeuge und Rüstungen (o, a..f)
MOON_T = et.tones("#262c3a", "#4a566c", "#6e7c96", "#95a4bc", "#b9c6da", "#d9e3f0", "#f7fbff")
NIGHT_T = et.tones("#04030a", "#0f0d22", "#1b1838", "#2a2552", "#3d3570", "#594e98", "#8f80d6")
ABYSS_T = et.tones("#030105", "#0d0612", "#1a0c22", "#2a1234", "#3e1a4a", "#5a2666", "#86408e")

SHADOW_WOOD = {"h": hexc("#0c0812"), "i": hexc("#1c1428"), "j": hexc("#2e2140"), "k": hexc("#45325e")}
MOON_TRIM = {"g": hexc("#4a566c"), "G": hexc("#8796b0"), "y": hexc("#c4d2e6"), "Y": hexc("#f4f8ff")}
MAGENTA = {"g": hexc("#5a0a52"), "G": hexc("#b01ea2"), "y": hexc("#ff5ae6"), "Y": hexc("#ffc4f6")}
VIOLET_ACCENT = {"x": hexc("#2a2470"), "X": hexc("#7a6af0"), "z": hexc("#d4ccff")}
MAGENTA_ACCENT = {"x": hexc("#5a0a52"), "X": hexc("#e03ad0"), "z": hexc("#ffc4f6")}


# =============================================================================
# Rohstoffe (16×16)
# =============================================================================

RAW = [
    "", "", "",
    "......oooo......",
    "....oodwcbo.....",
    "...odwdcbbbo....",
    "..odccbbcbbao...",
    "..ocbbbbcbaaao..",
    ".odcbbwbbcaaao..",
    ".ocbbbcbbbaaao..",
    ".obbbbbbbaaaao..",
    ".oabbbaaaaaao...",
    "..oaaaaaaooo....",
    "...oooooo.......",
]

INGOT = [
    "", "", "",
    "..........ooo...",
    ".......ooowddo..",
    "....ooowddddcbo.",
    "..oowdddddcdcbo.",
    ".owdddcddddcbbao",
    ".ocddddddccbbaao",
    ".obcccddcbbaaao.",
    ".obbbccbbaaaoo..",
    ".oabbbbaaaoo....",
    "..oaaaaaoo......",
    "...ooooo........",
]

NUGGET = [
    "", "", "", "", "",
    ".......oo.......",
    "......owdo......",
    ".....odccbo.....",
    "....odcwcbao....",
    "....ocbbbbao....",
    ".....obbaao.....",
    "......oooo......",
]

# Seelenkristall: Tropfen mit hellem Kern und einer Windung
SOUL_GEM = [
    "",
    ".......oo.......",
    "......odco......",
    "......odco......",
    ".....odcbbo.....",
    ".....odwcbo.....",
    "....odwwcbbo....",
    "....odwcccbo....",
    "...odccdcbbao...",
    "...ocdcccbbbao..",
    "...ocbdccbbbao..",
    "...obbbdcbbaao..",
    "....obbbbbaao...",
    ".....oabbaao....",
    "......oooo......",
]

ABYSS_SHARD = [
    "", "",
    "...........oo...",
    "..........owdo..",
    ".........owdbo..",
    "........odwbao..",
    ".......oddcbo...",
    "......odcwbao...",
    ".....odcbbao....",
    "....odcbwao.....",
    "...occbbao......",
    "..obcbaao.......",
    "..obbaoo........",
    "...ooo..........",
]

CRYSTAL_SHARD = [
    "", "",
    "........oo......",
    ".......owdo.....",
    "......owddo.....",
    "......odcco..oo.",
    ".....odccbo.owo.",
    ".....odcbbooddo.",
    "....odcbbaodcbo.",
    "....ocbbaoocbo..",
    "....obbaao.obo..",
    ".....obao...o...",
    "......oo........",
]

# Schmiedevorlage: Steintafel mit dem Zeichen des Abgrunds (Auge im Strudel)
TEMPLATE = [
    "",
    "...oooooooooo...",
    "..ossssssssssbo.",
    "..osttttttttsbo.",
    "..ostggggggtsbo.",
    "..ostgyYYygtsbo.",
    "..ostgYxxYgtsbo.",
    "..ostgYxxYgtsbo.",
    "..ostgyYYygtsbo.",
    "..ostggggggtsbo.",
    "..osttttttttsbo.",
    "..ostttyyttttbo.",
    "..ossssssssssbo.",
    "..obbbbbbbbbbbo.",
    "...oooooooooo...",
]



def abyss_template() -> Image.Image:
    colors = {"o": hexc("#050307"), "s": hexc("#2a2230"), "t": hexc("#1a141e"), "b": hexc("#120e16"),
              "g": hexc("#3e1a44"), "y": hexc("#b01ea2"), "Y": hexc("#ff6ae8"), "x": hexc("#ffe4fb")}
    return pa.sprite(TEMPLATE, colors)


# =============================================================================
# Erz-Einschlüsse (durchsichtig; der Stein kommt im Modell darunter)
# =============================================================================







# =============================================================================
# Speicherblöcke
# =============================================================================


# =============================================================================
# Setzling
# =============================================================================

SAPLING = [
    "................",
    "................",
    "......l.l.......",
    ".....lLlLl..l...",
    "...l.lLLLl.lLl..",
    "..lLlLLdLLlLLl..",
    "..lLLLdLLLLLl...",
    "...lLLLLdLLl....",
    "....lLLdLLl.....",
    "......LbL.......",
    ".......b........",
    ".......bb.......",
    "......bb........",
    ".......b........",
    "......bbb.......",
    "................",
]


def shadow_sapling() -> Image.Image:
    return pa.sprite(SAPLING, {"l": hexc("#6c48b0"), "L": hexc("#4c2e88"), "d": hexc("#2e1a5a"), "b": hexc("#1c1428")})


# =============================================================================
# Mondsilberklinge
# =============================================================================

MOON_BLADE = [
    "..............oo",
    ".............owo",
    "............owfo",
    "...........owfeo",
    "..........owfeo.",
    ".........owfeo..",
    "........owfedo..",
    ".......owfedo...",
    "..y...owfedo....",
    ".yY..owfedo.....",
    ".Y..owfedo......",
    ".Y.yGdooo.......",
    "..YGhyy.........",
    "...hjY..........",
    "..hjh...........",
    ".hkh............",
]


def moonsilver_blade() -> Image.Image:
    colors = et.material_colors(MOON_T, SHADOW_WOOD, {"y": hexc("#c4d2e6"), "Y": hexc("#f4f8ff"), "G": hexc("#8796b0"),
                                                       "w": hexc("#ffffff")})
    return pa.sprite(MOON_BLADE, colors)


# =============================================================================
# Ausgabe
# =============================================================================

def build(put: Callable[[str, Image.Image], None]) -> None:
    # Rohstoffe
    put("item/raw_moonsilver", pa.sprite(RAW, MOON))
    put("item/moonsilver_ingot", pa.sprite(INGOT, MOON))
    put("item/moonsilver_nugget", pa.sprite(NUGGET, MOON))
    put("item/raw_nightsteel", pa.sprite(RAW, NIGHT))
    put("item/nightsteel_ingot", pa.sprite(INGOT, NIGHT))
    put("item/soul_crystal", pa.sprite(SOUL_GEM, SOUL))
    put("item/abyssite_shard", pa.sprite(ABYSS_SHARD, ABYSS))
    put("item/abyssite_ingot", pa.sprite(INGOT, ABYSS))
    put("item/night_crystal_shard", pa.sprite(CRYSTAL_SHARD, NCRYSTAL))
    put("item/abyssite_upgrade_smithing_template", abyss_template())

    # Erze und Speicherblöcke kommen aus tools/shadow_blocks.py

    # Mondlaterne: Mondsilber-Rahmen, kaltes, weißblaues Licht
    frame = [MOON_T[k] for k in "oace"]
    light = [hexc("#4a7ac8"), hexc("#8ab8f0"), hexc("#c8e4ff"), hexc("#f4fbff")]
    put("block/moon_lantern", bld.lantern(frame, light))
    put("item/moon_lantern", bld.lantern_item(frame, light))

    # Setzling
    put("block/shadow_sapling", shadow_sapling())

    # Waffen und Werkzeuge
    night_tools = et.material_colors(NIGHT_T, SHADOW_WOOD, MOON_TRIM)
    abyss_tools = et.material_colors(ABYSS_T, SHADOW_WOOD, MAGENTA)
    for prefix, colors in (("nightsteel", night_tools), ("abyssite", abyss_tools)):
        put(f"item/{prefix}_sword", pa.sprite(et.SWORD, colors))
        put(f"item/{prefix}_pickaxe", pa.sprite(et.PICKAXE, colors))
        put(f"item/{prefix}_axe", pa.sprite(et.AXE, colors))
        put(f"item/{prefix}_shovel", pa.sprite(et.SHOVEL, colors))
        put(f"item/{prefix}_hoe", pa.sprite(et.HOE, colors))
    put("item/moonsilver_blade", moonsilver_blade())

    # Rüstungen
    night_armor = et.material_colors(NIGHT_T, MOON_TRIM, VIOLET_ACCENT)
    abyss_armor = et.material_colors(ABYSS_T, MAGENTA, MAGENTA_ACCENT)
    for prefix, colors in (("nightsteel", night_armor), ("abyssite", abyss_armor)):
        put(f"item/{prefix}_helmet", pa.sprite(et.HELMET, colors))
        put(f"item/{prefix}_chestplate", pa.sprite(et.CHESTPLATE, colors))
        put(f"item/{prefix}_leggings", pa.sprite(et.LEGGINGS, colors))
        put(f"item/{prefix}_boots", pa.sprite(et.BOOTS, colors))


NIGHTSTEEL_STYLE = et.Style(NIGHT_T, MOON_TRIM, VIOLET_ACCENT, "plate")
ABYSSITE_STYLE = et.Style(ABYSS_T, MAGENTA, MAGENTA_ACCENT, "plate")


def build_entity(save: Callable[[str, Image.Image], None]) -> None:
    for name, style in (("nightsteel", NIGHTSTEEL_STYLE), ("abyssite", ABYSSITE_STYLE)):
        outer, inner = et.armor_layers(style)
        save(f"entity/equipment/humanoid/{name}", outer)
        save(f"entity/equipment/humanoid_leggings/{name}", inner)


if __name__ == "__main__":
    import sys
    out: dict[str, Image.Image] = {}
    build(lambda k, v: out.__setitem__(k, v))
    zoom, pad, cols = 8, 6, 8
    cell = 16 * zoom + pad
    rows = math.ceil(len(out) / cols)
    sheet = Image.new("RGBA", (cols * cell + pad, rows * cell + pad), (60, 62, 70, 255))
    for i, (key, img) in enumerate(out.items()):
        tile = pa.checker(16, 16, (150, 150, 150, 255), (128, 128, 128, 255))
        if key.endswith("_overlay") or key.endswith("_ore_glow"):
            base = Image.open("../src/main/resources/assets/kingdomsatwar/textures/block/"
                              + ("deep_shadow_stone" if "abyss" in key or "soul" in key else "shadow_stone") + ".png").convert("RGBA")
            tile = base
        img16 = img if img.size == (16, 16) else img.crop((0, 0, 16, 16))
        tile = tile.copy()
        tile.alpha_composite(img16)
        sheet.alpha_composite(pa.zoom(tile, zoom), (pad + (i % cols) * cell, pad + (i // cols) * cell))
    target = sys.argv[1] if len(sys.argv) > 1 else "shadow_gear.png"
    sheet.save(target)
    print(f"{len(out)} Texturen -> {target}")
