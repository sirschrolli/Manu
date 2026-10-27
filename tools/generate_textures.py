#!/usr/bin/env python3
"""
Erzeugt alle 16x16-Texturen der Mod "Kingdoms at War" als eigene Pixelkunst im Vanilla-Stil.

Es werden KEINE Original-Minecraft-Texturen verwendet. Die Erze bestehen nur aus
"Erz-Kristallen" mit durchsichtigem Hintergrund – der Stein dahinter kommt zur Laufzeit
aus dem Spiel (siehe ModModelProvider, Vorlage ORE_OVERLAY). Für Aetherit und Draconium gibt es
zusätzlich "Leucht"-Ebenen (*_ore_glow.png) mit den hellsten Kristallpixeln.

Die Bausteine (Paletten, ASCII-Sprites, Rauschen, Ränder) liegen in tools/pixelart.py.

Aufruf (aus dem Projektordner):
    python3 tools/generate_textures.py                       # schreibt alle Texturen
    python3 tools/generate_textures.py --preview datei.png   # zusätzlich eine vergrößerte Übersicht

Benötigt: Pillow (pip install pillow)
"""
from __future__ import annotations

import math
import sys
from pathlib import Path

from PIL import Image

sys.path.insert(0, str(Path(__file__).resolve().parent))
import armor_art  # noqa: E402
import ashlands_textures  # noqa: E402
import block_textures  # noqa: E402
import boss_textures  # noqa: E402
import building_textures  # noqa: E402
import creature_textures  # noqa: E402
import crops  # noqa: E402
import equipment_textures  # noqa: E402
import farm_textures  # noqa: E402
import frost_textures  # noqa: E402
import grove_textures  # noqa: E402
import earth_textures  # noqa: E402
import economy_textures  # noqa: E402
import town_textures  # noqa: E402
import magic_textures  # noqa: E402
import storm_textures  # noqa: E402
import deep_textures  # noqa: E402
import items_armor  # noqa: E402
import items_eggs  # noqa: E402
import items_food  # noqa: E402
import items_gear  # noqa: E402
import items_materials  # noqa: E402
import items_misc  # noqa: E402
import kingdom_textures  # noqa: E402
import lair_textures  # noqa: E402
import ores_art  # noqa: E402
import pixelart as pa  # noqa: E402
import region_blocks  # noqa: E402
import seal_textures  # noqa: E402
import shadow_textures  # noqa: E402
import shadow_gear_textures  # noqa: E402
import world_blocks  # noqa: E402
from pixelart import Color, hexc  # noqa: E402

ROOT = Path(__file__).resolve().parent.parent
TEXTURES = ROOT / "src" / "main" / "resources" / "assets" / "kingdomsatwar" / "textures"

# Alle erzeugten Texturen: "block/name" bzw. "item/name" -> Bild
GENERATED: dict[str, Image.Image] = {}


def put(key: str, img: Image.Image) -> None:
    if key in GENERATED:
        raise ValueError(f"Textur doppelt erzeugt: {key}")
    GENERATED[key] = img


# =============================================================================
# Blöcke: siehe tools/block_textures.py (handgezeichnete Pixelkunst)
# =============================================================================

# =============================================================================
# 7. Feldfrüchte (je 4 Wachstumsstufen) und Zierblumen
# =============================================================================

PLANT = {
    "k": hexc("#1d4416"), "g": hexc("#2b6420"), "G": hexc("#3c842a"), "l": hexc("#56a238"), "L": hexc("#7cc24e"),
    "s": hexc("#5a3c1e"), "S": hexc("#8a6236"),                                   # Stütz-Stab
    "r": hexc("#8c1a10"), "R": hexc("#d2361e"), "h": hexc("#ff9070"),             # Tomate
    "u": hexc("#6aa632"), "U": hexc("#9cd25a"),                                   # unreife Tomate
    "y": hexc("#f4d23a"), "Y": hexc("#c4960e"),                                   # Blüte
    "c": hexc("#7fbe52"), "C": hexc("#a8dc7c"), "w": hexc("#d6f0b8"), "v": hexc("#5e9a3e"),  # Kohlkopf
    "o": hexc("#7a4a16"), "O": hexc("#c0822e"), "p": hexc("#e6b458"), "q": hexc("#f8dc98"),  # Zwiebel
    "e": hexc("#b2b04a"),                                                         # vergilbtes Blatt
    "a": hexc("#94722a"), "A": hexc("#c4a044"), "b": hexc("#dcbc60"), "B": hexc("#f2da88"), "n": hexc("#cfb468"),  # Gerste
}

FLOWERS: dict[str, tuple[list[str], dict[str, Color]]] = {
    "lavender": ([
        "", "",
        "......p...p.....",
        ".....pPp.pPp....",
        "..p..pPp.pPp....",
        ".pPp.PpP.PpP.p..",
        ".pPp.pPp.pPp.Pp.",
        ".PpP..P...P.pPp.",
        ".pPp..l...l.PpP.",
        "..P...G..lG..P..",
        "..l...G..G...l..",
        "..G...G.lG...G..",
        "..lG..GlG...Gl..",
        "...G..GG...lG...",
        "....GgGg..gG....",
        ".....gg.gg......",
    ], {"p": hexc("#c4a0f2"), "P": hexc("#8a5ccc")}),
    "garden_rose": ([
        "", "", "",
        "......oRRo......",
        ".....oRhrRo.....",
        ".....RrRRrR.....",
        ".....oRrrRo.....",
        "......oRRo......",
        ".......G........",
        ".......G.lL.....",
        "...Ll..GlGl.....",
        "...lGl.G........",
        ".....lGG........",
        ".......G..lL....",
        ".......GlGl.....",
        ".......g........",
    ], {"o": hexc("#5a0814"), "R": hexc("#b4162a"), "r": hexc("#e2364a"), "h": hexc("#ff8a96")}),
    "fire_lily": ([
        "", "",
        "...o.......o....",
        "...oR.....Ro....",
        "....RR.y.RR.....",
        ".....RrYyrR.....",
        "...oRrryrrRRo...",
        "..oR..RrrR..Ro..",
        "......oRRo......",
        ".......G........",
        ".......G........",
        "....lL.G........",
        ".....lGG..Ll....",
        ".......G.lG.....",
        ".......GGl......",
        ".......g........",
    ], {"o": hexc("#7a1806"), "R": hexc("#e04a12"), "r": hexc("#ff9a2a"), "y": hexc("#ffe25a"),
        "Y": hexc("#c8840e")}),
    "frost_flower": ([
        "", "",
        ".......w........",
        "...b...B...b....",
        "....B..b..B.....",
        ".....BbwbB......",
        "..wbbwcCcwbbw...",
        ".....BbwbB......",
        "....B..b..B.....",
        "...b...t...b....",
        ".......t........",
        "....T..t..T.....",
        ".....TtT.tT.....",
        ".......tTt......",
        ".......t........",
        ".......u........",
    ], {"w": hexc("#ffffff"), "b": hexc("#c4e8ff"), "B": hexc("#7ab6e6"), "c": hexc("#5ad8f2"),
        "C": hexc("#2a9cc4"), "t": hexc("#6a9eb0"), "T": hexc("#94c4d4"), "u": hexc("#3e6c7c")}),
}


def plants() -> None:
    for name, (rows, colors) in FLOWERS.items():
        put(f"block/{name}", pa.sprite(rows, {**PLANT, **colors}))


# =============================================================================
# 8. Gegenstände (handgezeichnete Sprites)
# =============================================================================
# Zeichen: o = Rand (dunkler Ton der Eigenfarbe), a/b/c/d = Töne dunkel -> hell, w = Glanzlicht.

def tones(o: str, a: str, b: str, c: str, d: str, w: str) -> dict[str, Color]:
    return {"o": hexc(o), "a": hexc(a), "b": hexc(b), "c": hexc(c), "d": hexc(d), "w": hexc(w)}


MEAT_BONE = [
    "", "",
    "............ww..",
    "...........wkkw.",
    "......oooo.wkw..",
    "....oorRRroww...",
    "...orRhRRRro....",
    "..orRhhRRrrro...",
    "..oRRRRRrrrro...",
    "..orRRRrrrro....",
    "...orrrrrro.....",
    ".bb.oooooo......",
    "bBpb............",
    "bppb.bb.........",
    ".bb.bBpb........",
    "....bppb........",
]

FOOD = {
    "onion": ([
        "",
        ".......g.g......",
        ".......GgG......",
        "........G.......",
        "......oooo......",
        "....ooqpOOoo....",
        "...oqpppOOOOo...",
        "..oqppOOOOOOOo..",
        "..opppOOOOOOOo..",
        ".oqppOOOOOOOOOo.",
        ".opOOOOOOOOOOOo.",
        ".opOOOOOOOOOOro.",
        "..oOOOOOOOOOro..",
        "...ooOOOOOroo...",
        ".....oooooo.....",
        "......n.n.n.....",
    ], {"o": hexc("#5a3410"), "O": hexc("#c0822e"), "p": hexc("#dea250"), "q": hexc("#f6d690"),
        "r": hexc("#96601e"), "g": hexc("#3c842a"), "G": hexc("#6ab442"), "n": hexc("#d8c8a0")}),
    "barley": ([
        "...........n....",
        "..........nBn.n.",
        "........n.BbBnBn",
        ".........nbAbBbB",
        "......n..BbAbbAb",
        ".....nBn.bAbAAb.",
        ".....BbB.AbAbA..",
        ".....bAb.bAAa...",
        ".....BbA.AAa....",
        ".....bAAaAa.....",
        "......bAAa......",
        ".....aAAa.......",
        "....aAa.........",
        "...aAa..........",
        "..aAa...........",
        "..aa............",
    ], {"a": hexc("#8a6a22"), "A": hexc("#c49e40"), "b": hexc("#dcbc5e"), "B": hexc("#f4dc8c"),
        "n": hexc("#d4ba6e")}),
    "bread_roll": ([
        "", "", "", "",
        ".....oooooo.....",
        "...oohwwhhcoo...",
        "..ohwhhhhhcccbo.",
        ".ohwhhcccccccbbo",
        ".ohhccmcccmccbbo",
        "ohhcccmmcmmcccbo",
        "ohcccccmmmcccbbo",
        "occcccccccccbbao",
        ".obccccccccbbaao",
        "..oabbbbbbbbaao.",
        "...ooaaaaaaaoo..",
        ".....oooooooo...",
    ], {"o": hexc("#4a2a0c"), "a": hexc("#8a521c"), "b": hexc("#b06e28"), "c": hexc("#d4923a"),
        "h": hexc("#ecb860"), "w": hexc("#f8dc98"), "m": hexc("#f2d090")}),
    "vegetable_stew": ([
        "", "", "", "", "",
        "...oooooooooo...",
        "..oSSSSSSSSSSo..",
        ".oSRsRgSsTRSsSo.",
        ".osSsTSRgsSsRso.",
        ".oWsSsgSsSTsSWo.",
        "..oWWWWWWWWWWo..",
        "..owwWWWWWWwwo..",
        "...owwwwwwwwo...",
        "....oowwwwoo....",
        "......oooo......",
    ], {"o": hexc("#2e1a0c"), "W": hexc("#8a5a32"), "w": hexc("#6a4224"), "S": hexc("#c4501c"),
        "s": hexc("#9a3612"), "R": hexc("#e8442a"), "g": hexc("#5aa83a"), "T": hexc("#f0a030")}),
    "meat_pie": ([
        "", "", "", "",
        "....oooooooo....",
        "..oohwwhhhhhoo..",
        ".ohwhhcccchhcco.",
        ".ohhcchmmhccccbo",
        "ohccchmeemhcccbo",
        "ohcccchmmhcccbbo",
        "ocbccccccccccbao",
        "oTTTTTTTTTTTTTTo",
        "otTtTtTtTtTtTtto",
        ".ottttttttttttto",
        "..oooooooooooo..",
    ], {"o": hexc("#4a280c"), "a": hexc("#7a4618"), "b": hexc("#a8682a"), "c": hexc("#cc8c3c"), "h": hexc("#e6b064"),
        "w": hexc("#f8d896"), "m": hexc("#6a2e14"), "e": hexc("#3a1608"), "T": hexc("#9a6a3e"),
        "t": hexc("#74482a")}),
    "apple_pie": ([
        "", "", "", "",
        "....oooooooo....",
        "..oohwhrhwhhoo..",
        ".ohwRhRhRhRhcco.",
        ".ohRhRhRhRhRhcbo",
        "ohRhRhRhRhRhRhbo",
        "ohcRhRhRhRhRhcbo",
        "ocbcRcRcRcRcbbao",
        "oTTTTTTTTTTTTTTo",
        "otTtTtTtTtTtTtto",
        ".ottttttttttttto",
        "..oooooooooooo..",
    ], {"o": hexc("#4a280c"), "a": hexc("#7a4618"), "b": hexc("#a8682a"), "c": hexc("#cc8c3c"), "h": hexc("#e6b064"),
        "w": hexc("#f8d896"), "R": hexc("#b42a1c"), "r": hexc("#e2503a"), "T": hexc("#9a6a3e"),
        "t": hexc("#74482a")}),
}

def items() -> None:
    put("item/dragon_food", pa.sprite(MEAT_BONE, {
        "o": hexc("#4a0e0c"), "r": hexc("#9a2a22"), "R": hexc("#c8443a"), "h": hexc("#f08a7a"),
        "w": hexc("#f2ead8"), "k": hexc("#c8bca0"), "b": hexc("#3a0a1e"), "B": hexc("#c2304a"), "p": hexc("#8a1a34")}))
    for name, (rows, colors) in FOOD.items():
        put(f"item/{name}", pa.sprite(rows, colors))


# =============================================================================
# Ausgabe
# =============================================================================

def preview(path: Path) -> None:
    """Übersicht aller Texturen, 6-fach vergrößert auf Schachbrett-Hintergrund."""
    zoom, pad, cols = 6, 4, 12
    cell = 16 * zoom + pad
    entries = [(k, v) for k, v in GENERATED.items() if v.size == (16, 16)]
    rows = math.ceil(len(entries) / cols)
    sheet = Image.new("RGBA", (cols * cell + pad, rows * cell + pad), (52, 56, 60, 255))
    for i, (_, img) in enumerate(entries):
        x = pad + (i % cols) * cell
        y = pad + (i // cols) * cell
        tile = pa.checker(16, 16)
        tile.alpha_composite(img)
        sheet.alpha_composite(pa.zoom(tile, zoom), (x, y))
    sheet.save(path)


def build_all() -> dict[str, Image.Image]:
    GENERATED.clear()
    block_textures.build(put)
    kingdom_textures.build(put)
    equipment_textures.build(put)
    creature_textures.build(put)
    building_textures.build(put)
    farm_textures.build(put)
    boss_textures.build(put)
    lair_textures.build(put)
    ashlands_textures.build(put)
    seal_textures.build(put)
    frost_textures.build(put)
    grove_textures.build(put)
    earth_textures.build(put)
    storm_textures.build(put)
    deep_textures.build(put)
    world_blocks.build(put)
    region_blocks.build(put)
    ores_art.build(put)
    crops.build(put)
    shadow_textures.build(put)
    shadow_gear_textures.build(put)
    equipment_textures.build_entity(put)
    armor_art.build(put)
    plants()
    items()
    items_gear.build(put)
    items_armor.build(put)
    items_materials.build(put)
    items_food.build(put)
    items_eggs.build(put)
    items_misc.build(put)
    economy_textures.build(put)
    town_textures.build(put)
    magic_textures.build(put)
    return GENERATED


def main() -> None:
    build_all()
    for key, img in GENERATED.items():
        target = TEXTURES / f"{key}.png"
        target.parent.mkdir(parents=True, exist_ok=True)
        img.save(target)
    print(f"{len(GENERATED)} Texturen geschrieben nach {TEXTURES}")
    if "--preview" in sys.argv:
        i = sys.argv.index("--preview")
        target = Path(sys.argv[i + 1]) if len(sys.argv) > i + 1 else ROOT / "build" / "texture_preview.png"
        target.parent.mkdir(parents=True, exist_ok=True)
        preview(target)
        print(f"Vorschau: {target}")


if __name__ == "__main__":
    main()
