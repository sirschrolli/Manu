#!/usr/bin/env python3
"""
Texturen der Schätze der Schattenwelt (Erze, Rohstoffe, Werkzeuge, Waffen, Rüstungen):

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
import random
from typing import Callable

from PIL import Image

import building_textures as bld
import equipment_textures as et
import pixelart as pa
from pixelart import CLEAR, Color, hexc

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


def ingot_like(art, colors: dict[str, Color]) -> Image.Image:
    return pa.sprite(art, colors)


def abyss_template() -> Image.Image:
    colors = {"o": hexc("#050307"), "s": hexc("#2a2230"), "t": hexc("#1a141e"), "b": hexc("#120e16"),
              "g": hexc("#3e1a44"), "y": hexc("#b01ea2"), "Y": hexc("#ff6ae8"), "x": hexc("#ffe4fb")}
    return pa.sprite(TEMPLATE, colors)


# =============================================================================
# Erz-Einschlüsse (durchsichtig; der Stein kommt im Modell darunter)
# =============================================================================

MOONSILVER_ORE = """
................
...dl...........
..dlhd......dl..
...dl......dlhd.
............dd..
.......dl.......
......dlhl......
.......dld......
.dl.............
dlhd.......dl...
.dd.......dlhd..
...........dd...
.....dl.........
....dlhd....dl..
.....dd....dlhd.
............d...
"""
MOONSILVER_COLORS = {"d": "#4c5a74", "l": "#9eaecc", "h": "#eef4ff"}

NIGHTSTEEL_ORE = """
................
..dlh...........
.dmlhl......dl..
.dmmld.....dmlh.
..ddd......dmmd.
............dd..
......dlh.......
.....dmlhl......
.....dmmld......
......ddd.......
..dl.......dlh..
.dmlh.....dmlhl.
.dmmd.....dmmld.
..dd.......ddd..
................
................
"""
NIGHTSTEEL_COLORS = {"d": "#05040b", "m": "#332c66", "l": "#7466c8", "h": "#d6ccff"}

SOUL_CRYSTAL_ORE = """
................
....c...........
...cl.......c...
...cld.....clh..
..dcld.....cld..
..ddd......dd...
................
........c.......
.......clh......
.c.....cld......
clh...dcldd.....
cld....ddd...c..
dd..........clh.
............cld.
...........ddd..
................
"""
SOUL_CRYSTAL_COLORS = {"d": "#0a3a3e", "c": "#28aaa6", "l": "#7af0e4", "h": "#ffffff"}

ABYSSITE_ORE = """
................
.v..............
.vV.......vv....
..vVv....vVVv...
...vVv..vVhVv...
....vVvvVVvv....
.....vVhVv......
......vVv.......
.....vVv........
....vVv.....v...
...vVhv....vVv..
..vVvv....vVhVv.
..vv.....vVvvv..
........vVv.....
........vv......
................
"""
ABYSSITE_COLORS = {"v": "#3a0a36", "V": "#c02ab0", "h": "#ffb4f4"}


def _ore(art: str, colors: dict[str, str]) -> Image.Image:
    rows = [r for r in art.strip("\n").split("\n")]
    img = pa.new()
    px = img.load()
    for y, row in enumerate(rows):
        for x, ch in enumerate(row):
            if ch in colors:
                px[x, y] = hexc(colors[ch])
    return img


def _glow_of(img: Image.Image, keep: set[Color]) -> Image.Image:
    out = pa.new()
    src = img.load()
    dst = out.load()
    for y in range(N):
        for x in range(N):
            if src[x, y] in keep:
                dst[x, y] = src[x, y]
    return out


# =============================================================================
# Speicherblöcke
# =============================================================================

def _plate_block(t: dict[str, Color], seed: int, rivets: bool) -> Image.Image:
    """Metallblock: vier Platten mit Fasen, feiner Bürstung und (optional) Nieten."""
    noise = pa.fractal_noise(seed, ((8, 0.5), (4, 0.5)))
    ramp = [t[k] for k in "abcdef"]
    img = pa.new()
    px = img.load()
    for y in range(N):
        for x in range(N):
            lx, ly = x % 8, y % 8
            if lx == 7 or ly == 7:
                c = t["o"] if (lx == 7 and ly == 7) else ramp[0]
            elif lx == 0 or ly == 0:
                c = ramp[4]
            elif lx == 6 or ly == 6:
                c = ramp[1]
            else:
                v = 0.45 + 0.2 * noise[y][x] + (0.08 if (x + 2 * y) % 7 == 0 else 0)
                c = ramp[max(0, min(5, int(v * 6)))]
            px[x, y] = c
    if rivets:
        for (x, y) in ((1, 1), (5, 1), (1, 5), (5, 5)):
            for ox in (0, 8):
                for oy in (0, 8):
                    px[x + ox, y + oy] = ramp[5]
                    px[x + ox + 1, y + oy + 1] = ramp[0]
    return img


def moonsilver_block() -> Image.Image:
    """Mondsilberblock: helle, glatte Platten mit einer eingelegten Mondsichel in der Mitte."""
    img = _plate_block(MOON_T, 41101, False)
    px = img.load()
    for (x, y) in [(6, 5), (5, 6), (5, 7), (5, 8), (5, 9), (6, 10), (7, 10), (7, 5)]:
        px[x, y] = MOON_T["o"]
    for (x, y) in [(7, 6), (6, 7), (6, 8), (6, 9), (8, 10)]:
        px[x, y] = MOON_T["f"]
    return img


def nightsteel_block() -> Image.Image:
    return _plate_block(NIGHT_T, 41201, True)


def soul_crystal_block() -> tuple[Image.Image, Image.Image]:
    """Seelenkristallblock: große, türkise Facetten mit hellen Kanten; die Kanten leuchten."""
    cells, border, pts = pa.voronoi(41301, 6)
    rng = random.Random(41302)
    pal = [SOUL["o"], SOUL["a"], SOUL["b"], SOUL["c"], SOUL["d"], SOUL["w"]]
    tone = {i: 1 + rng.randrange(3) for i in range(len(pts))}
    img = pa.new()
    glow = pa.new()
    px = img.load()
    gp = glow.load()
    for y in range(N):
        for x in range(N):
            if border[y][x] < 0.8:
                px[x, y] = pal[4]
                gp[x, y] = pal[4]
            else:
                cx, cy = pts[cells[y][x]]
                v = tone[cells[y][x]] + (1 if (x - cx) + (y - cy) < -2 else 0)
                px[x, y] = pal[v]
    for (x, y) in [(4, 3), (11, 9), (6, 12)]:
        px[x, y] = pal[5]
        gp[x, y] = pal[5]
    return img, glow


def abyssite_block() -> tuple[Image.Image, Image.Image]:
    """Abyssitblock: schwarze, schwere Blöcke; durch die Fugen glüht es magenta."""
    img = _plate_block(ABYSS_T, 41401, False)
    glow = pa.new()
    px = img.load()
    gp = glow.load()
    vein = [(1, 3), (2, 3), (3, 4), (4, 4), (5, 5), (5, 6), (6, 7), (7, 7), (8, 8), (9, 8), (10, 9), (11, 10), (12, 10),
            (13, 11), (14, 12), (9, 2), (10, 3), (10, 4), (3, 11), (4, 12), (5, 13), (5, 14)]
    for i, (x, y) in enumerate(vein):
        c = MAGENTA["y"] if i % 4 else MAGENTA["Y"]
        px[x, y] = c
        gp[x, y] = c
    return img, glow


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

    # Erze
    put("block/moonsilver_ore_overlay", _ore(MOONSILVER_ORE, MOONSILVER_COLORS))
    put("block/nightsteel_ore_overlay", _ore(NIGHTSTEEL_ORE, NIGHTSTEEL_COLORS))
    soul = _ore(SOUL_CRYSTAL_ORE, SOUL_CRYSTAL_COLORS)
    put("block/soul_crystal_ore_overlay", soul)
    put("block/soul_crystal_ore_glow", _glow_of(soul, {hexc(SOUL_CRYSTAL_COLORS["l"]), hexc(SOUL_CRYSTAL_COLORS["h"])}))
    abyss = _ore(ABYSSITE_ORE, ABYSSITE_COLORS)
    put("block/abyssite_ore_overlay", abyss)
    put("block/abyssite_ore_glow", _glow_of(abyss, {hexc(ABYSSITE_COLORS["V"]), hexc(ABYSSITE_COLORS["h"])}))

    # Speicherblöcke
    put("block/moonsilver_block", moonsilver_block())
    put("block/nightsteel_block", nightsteel_block())
    soul_block, soul_glow = soul_crystal_block()
    put("block/soul_crystal_block", soul_block)
    put("block/soul_crystal_block_glow", soul_glow)
    abyss_block, abyss_glow = abyssite_block()
    put("block/abyssite_block", abyss_block)
    put("block/abyssite_block_glow", abyss_glow)

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
