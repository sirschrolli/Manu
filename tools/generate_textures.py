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
import random
import sys
from pathlib import Path

from PIL import Image

sys.path.insert(0, str(Path(__file__).resolve().parent))
import ashlands_textures  # noqa: E402
import block_textures  # noqa: E402
import boss_textures  # noqa: E402
import building_textures  # noqa: E402
import creature_textures  # noqa: E402
import equipment_textures  # noqa: E402
import farm_textures  # noqa: E402
import frost_textures  # noqa: E402
import grove_textures  # noqa: E402
import earth_textures  # noqa: E402
import storm_textures  # noqa: E402
import deep_textures  # noqa: E402
import kingdom_textures  # noqa: E402
import lair_textures  # noqa: E402
import magic_textures  # noqa: E402
import pixelart as pa  # noqa: E402
import seal_textures  # noqa: E402
import shadow_textures  # noqa: E402
import shadow_gear_textures  # noqa: E402
from pixelart import CLEAR, Color, hexc, palette, pick  # noqa: E402

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

# Farben der Spawn-Eier (dunkel -> hell)
SPAWN_EGG_PALETTES = {
    "fire": palette("#3d0805", "#7a1508", "#b02a0c", "#e0521a", "#ffa43a"),
    "ice": palette("#4a7ea6", "#7fb2d8", "#a8d2ee", "#cfe8f8", "#f6fcff"),
    "lightning": palette("#121318", "#24262e", "#363944", "#4c505d", "#6a6f7e"),
    "nature": palette("#15381a", "#22562a", "#33773a", "#4f9a48", "#8ccc62"),
    "earth": palette("#2e1d10", "#4c331d", "#6a4b2c", "#8b673f", "#b08a5a"),
    "water": palette("#062a4a", "#0c4a7a", "#1470a8", "#2a9ad0", "#7ad4f4"),
}


DRAGON_SPAWN_EGG = """
...h........h...
...hh......hh...
....hoooooooh...
....o4444443o...
...o443333333o..
..wo43e3333e3ow.
.wwo433333332oww
wwwo332bbbb22oww
.wwo32bbbbbb2ow.
..wo32bbbbbb2o..
...o22bbbbbb1o..
...o122bbbb21o..
....o11222211o..
.....oooooooo...
................
................
"""

SPAWN_EGG_DETAILS = {
    # Bauch, Augen, Hörner, Flügel
    "fire": ("#ffb347", "#ffe066", "#2a2220", "#5a1208"),
    "ice": ("#ffffff", "#2ab7e8", "#e8f6ff", "#5d9ccc"),
    "lightning": ("#9a96a8", "#fff27a", "#ffd23a", "#1c1a26"),
    "nature": ("#d8e88a", "#c6ff6a", "#6a4a28", "#1f5a24"),
    "earth": ("#d8c6a0", "#ffb84a", "#8a8e94", "#3a2a1c"),
    "water": ("#bff4f0", "#7afff0", "#f4e6dc", "#0e3f66"),
}


def dragon_spawn_egg(kind: str) -> Image.Image:
    pal = SPAWN_EGG_PALETTES[kind]
    belly, eye, horn, wing = (hexc(c) for c in SPAWN_EGG_DETAILS[kind])
    colors = {
        "o": pal[0], "1": pal[1], "2": pal[2], "3": pal[3], "4": pal[4],
        "b": belly, "e": eye, "h": horn, "w": wing,
    }
    img = pa.sprite(DRAGON_SPAWN_EGG, colors)
    if kind == "lightning":
        px = img.load()
        for x, y in ((6, 4), (7, 5), (7, 6), (8, 7)):
            px[x, y] = hexc("#ffd92a")
    return img


def dragon_spawn_eggs() -> None:
    for kind in SPAWN_EGG_PALETTES:
        put(f"item/{kind}_dragon_spawn_egg", dragon_spawn_egg(kind))


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

CROPS: dict[str, list[list[str]]] = {
    "tomatoes": [
        ["", "", "", "", "", "", "", "", "", "", "",
         "....L.L.........",
         ".....G.....L.L..",
         ".....g......G...",
         "..L.Lg......g...",
         "...Gg.......g..."],
        ["", "", "", "", "", "",
         ".......L........",
         "......LGL....L..",
         "...L...g....LGl.",
         "..lGl..gL....g..",
         "...g..lGl.L..g..",
         "...gL..g.lG..gL.",
         "..lG...g..g.lG..",
         "...g...g..g..g..",
         "...g...g..g..g..",
         "...g...g..g..g.."],
        ["",
         ".......S........",
         ".....L.SL.......",
         "....LGlsGL......",
         "...lGgysGgL.....",
         "..lGgYyslGgl....",
         "..Gg..Ls.LGgL...",
         ".lGuUlGsgGuUg...",
         ".gGuugGslGuugl..",
         "..gg.lGsGg.gG...",
         "..lGl.gsg.lGgl..",
         ".lGgGLgsglGgGl..",
         "..g.uU.s.uU.g...",
         "..g.uu.s.uu.g...",
         "..g....s....g...",
         "..g....s....g..."],
        ["",
         ".......S........",
         ".....L.SL.......",
         "....LGlsGL......",
         "...lGgRhGgL.....",
         "..lGgrRslGgl....",
         "..Gg..Ls.LGgL...",
         ".lGRhlGsgGRhg...",
         ".gGrRgGslGrRgl..",
         "..gg.lGsGg.gG...",
         "..lGl.gsg.lGgl..",
         ".lGgGLgsglGgGl..",
         "..g.Rh.s.Rh.g...",
         "..g.rR.s.rR.g...",
         "..g....s....g...",
         "..g....s....g..."],
    ],
    "cabbages": [
        ["", "", "", "", "", "", "", "", "", "", "", "",
         "...L.L....L.L...",
         "....G......G....",
         "...lGl....lGl...",
         "....g......g...."],
        ["", "", "", "", "", "", "", "", "", "",
         "................",
         "..L.L.....L..L..",
         ".lGlGl...lGllGl.",
         "lGgGGGl.lGgGGgGl",
         ".GgCgG...GgCgGg.",
         "..ggg.....ggg..."],
        ["", "", "", "", "", "", "",
         "................",
         "..L..L.....L..L.",
         ".lGl.Gl...lGl.Gl",
         "lGgGlGGl.lGgGlGG",
         "GgGCCCgGlGgCCCgG",
         "gGCwCCcGgGCwCCcG",
         ".gGCCcgg.gGCCcgg",
         "..gGGgg...gGGgg.",
         "...ggg.....ggg.."],
        ["", "", "", "", "",
         "....L....L......",
         "...lGl..lGl.....",
         "..lGgGllGgGl....",
         ".lGgCCCCCgGgl...",
         ".GgCwwCCCcgGl...",
         "lGCwCCvCCCcgGl..",
         "GgCCvCCCvCcgGg..",
         "gGcCCCvCCCcgGg..",
         ".gGcCCCCCcggg...",
         "..gGgcccgGgg....",
         "...gggGggg......"],
    ],
    "onions": [
        ["", "", "", "", "", "", "", "", "", "", "", "",
         "...L.....L....L.",
         "...l.....l....l.",
         "...G.....G....G.",
         "...g.....g....g."],
        ["", "", "", "", "", "", "", "",
         "..L.......L.....",
         "..l..L....l...L.",
         "..G..l...LG...l.",
         "..G..G...lG...G.",
         ".lG..G...G....G.",
         ".G...g...G...gG.",
         ".g...g...g...g..",
         ".g...g...g...g.."],
        ["", "", "",
         "..L.........L...",
         "..l...L.....l...",
         "..G...l...L.G...",
         ".lG...G...l.G..L",
         ".G....G...G.G..l",
         ".G...lG...G.G..G",
         ".G...G....GlG..G",
         ".g...G...lG.g..G",
         ".g...g...G..g.lg",
         ".g...g...g..g.g.",
         ".pO..g...pO.g.g.",
         "pOOo.....OOo.pOo",
         ".oo.......o..oo."],
        ["", "", "",
         "..e.........e...",
         "..e...L.....l...",
         "..l...l...e.G...",
         ".lG...G...e.G..L",
         ".G....G...G.G..l",
         ".G...lG...G.G..G",
         ".G...G....GlG..G",
         ".g...G...lG.g..G",
         ".g..qg...G.qg.lg",
         "qpOOpO..qpOOpOpO",
         "pOOOOo..pOOOOopO",
         ".OOoOo..OOOoOoOo",
         "..oo......oo..o."],
    ],
    "barley": [
        ["", "", "", "", "", "", "", "", "", "", "", "", "",
         "..L....L...L..L.",
         ".lG..L.G..lG..G.",
         ".G..lG.g..G..lg."],
        ["", "", "", "", "", "", "", "", "",
         "..L......L......",
         "..l...L..l...L..",
         ".lG...l..G..lG..",
         ".G...lG..G..G..L",
         ".G...G..lG..G..l",
         ".g...G..G..lg..G",
         ".g...g..g..g...g"],
        ["", "",
         "..n.....n.......",
         "..ln...nl....n..",
         "..lL...Ll...nl..",
         "..LG...lL...Ll..",
         "..lL...GL...lL..",
         "..G...lG....LG..",
         ".lG...G....lG...",
         ".G...lG....G....",
         ".G...G....lG...l",
         ".G...G....G....G",
         ".g...G....G...lG",
         ".g...g...lg...G.",
         ".g...g...g....g.",
         ".g...g...g....g."],
        ["n.......n.......",
         ".n.n.....n.n....",
         "..nBn...nBn.....",
         "..BbB...BbBn....",
         "..bAb..nbAb.n.n.",
         "..Bb....Bb...nBn",
         "..bA....bA...BbB",
         "..A.....A....bAb",
         ".aA....aA....Bb.",
         ".A.....A.....bA.",
         ".A.....A....aA..",
         ".A....aA....A...",
         ".a....A.....A...",
         ".a....A....aA...",
         ".a....a....a....",
         ".a....a....a...."],
    ],
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
    for crop, stages in CROPS.items():
        for i, rows in enumerate(stages):
            put(f"block/{crop}_stage{i}", pa.sprite(rows, PLANT))
    for name, (rows, colors) in FLOWERS.items():
        put(f"block/{name}", pa.sprite(rows, {**PLANT, **colors}))


# =============================================================================
# 8. Gegenstände (handgezeichnete Sprites)
# =============================================================================
# Zeichen: o = Rand (dunkler Ton der Eigenfarbe), a/b/c/d = Töne dunkel -> hell, w = Glanzlicht.

def tones(o: str, a: str, b: str, c: str, d: str, w: str) -> dict[str, Color]:
    return {"o": hexc(o), "a": hexc(a), "b": hexc(b), "c": hexc(c), "d": hexc(d), "w": hexc(w)}


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

POWDER = [
    "", "", "", "", "", "",
    "......ooo.......",
    ".....ocwco......",
    "....ocdccbo.....",
    "...ocdcwcbbo....",
    "..ocdcccbbbao...",
    ".ocwccbbbbaaao..",
    ".obcbbbbaaaaao..",
    "..oabbaaaaaoo...",
    "...ooooooooo....",
]

CHUNK = [
    "", "", "",
    "......oooo......",
    "....oodwcbo.....",
    "...odwdcbbbo....",
    "..odccbbcbbao...",
    "..ocbbbbcbaaao..",
    ".odcbbbbbcaaao..",
    ".ocbbbbbbbaaao..",
    ".obbbbbbbaaaao..",
    ".oabbbaaaaaao...",
    "..oaaaaaaooo....",
    "...oooooo.......",
]

GEM_ITEM = [
    "", "",
    "........ooooo...",
    ".......ocwwdco..",
    "......ocwdddcbo.",
    "......odwdcccbo.",
    "......obddccbao.",
    "......obcccbbao.",
    ".......obcbbao..",
    "..qq....obbao...",
    ".qhgq....oao....",
    "qhggqq....o.....",
    "qgggkq..........",
    ".qgkq...........",
    "..qq............",
]

CRYSTAL_CLUSTER = [
    "", "",
    "..........oo....",
    ".........owdo...",
    "....oo...odco...",
    "...owdo..ocbo...",
    "...odcbo.ocbo.oo",
    "....ocbo.obaoowo",
    "....ocbaoobaodco",
    ".....obaoobaocbo",
    ".....obbaabaobao",
    "......obbbbaobo.",
    "......oaaaaaao..",
    ".......oooooo...",
]

TALL_CRYSTAL = [
    "",
    ".......oo.......",
    "......owdo......",
    "......owdo...w..",
    ".....owdcbo.wdw.",
    ".....owdcbo..w..",
    ".....odccbo.....",
    ".....odcbbo.....",
    ".....ocbbao.....",
    ".....ocbbao.....",
    ".w...obbaao.....",
    "wdw..obbaao.....",
    ".w...oabaao.....",
    "......oaao......",
    ".......oo.......",
]

FLAME_CRYSTAL = [
    "", "",
    "......o.........",
    ".....odo...o....",
    ".....odco.odo...",
    "....odcwbodco...",
    "....odwwcbcbo...",
    "...odcwwcccbo...",
    "...ocwwwccbbao..",
    "...ocwcwcbbbao..",
    "...obccccbbaao..",
    "....obccbbbao...",
    ".....obbbbao....",
    "......oaaao.....",
    ".......ooo......",
]

SHARD = [
    "", "",
    "..........ooo...",
    ".........owwdo..",
    "........owddco..",
    ".......odddcbo..",
    "......odddcbo...",
    ".....odddcbao...",
    "....odddcbao....",
    "...odddcbao.....",
    "..occccbao......",
    "..obcbbao.......",
    "...obbao........",
    "....ooo.........",
]

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

SCALE = [
    "",
    ".....oooooo.....",
    "....owwddcco....",
    "...owddddcccbo..",
    "...odddcccccbo..",
    "..odddccccbbbao.",
    "..odccccccbbbao.",
    "..occcccxbbbbao.",
    "..occcccxbbbaao.",
    "...occccxbbbao..",
    "...obcccxbbaao..",
    "....obbbxbaao...",
    ".....obbbaao....",
    "......obaao.....",
    ".......ooo......",
]

SCALE_COLORS = {
    "fire": tones("#3c0804", "#7a1508", "#b42a0c", "#e2541a", "#ff9a36", "#ffd88a"),
    "ice": tones("#2e5a80", "#5f94c0", "#8fbde2", "#bcdcf4", "#e2f2ff", "#ffffff"),
    "lightning": tones("#0c0d11", "#1e2027", "#30333d", "#454955", "#626877", "#ffe14a"),
    "nature": tones("#0f3014", "#1d4f24", "#2e7234", "#4a9646", "#7cc25e", "#c8ec8a"),
    "earth": tones("#26180c", "#47301a", "#664828", "#88643c", "#ae8a58", "#dcc08a"),
    "water": tones("#05223e", "#0a4072", "#1266a0", "#228fca", "#5ec4ee", "#c4f2ff"),
}
SCALE_RIDGE = {"fire": "#ffc04a", "ice": "#ffffff", "lightning": "#ffd42a", "nature": "#a8dc6a",
               "earth": "#c4a06a", "water": "#9ae4ff"}

FOOD = {
    "tomato": ([
        "", "", "",
        ".......kk.......",
        "......kgGk......",
        ".....oogkGoo....",
        "...ooRRgRRRRoo..",
        "..oRhhRRRRRRRro.",
        "..oRhRRRRRRRrro.",
        ".oRRRRRRRRRrrrro",
        ".oRRRRRRRRrrrrro",
        ".orRRRRRRrrrrrro",
        "..orRRRrrrrrrro.",
        "..oorrrrrrrrroo.",
        "....oooooooo....",
    ], {"o": hexc("#4e0a06"), "R": hexc("#c8301c"), "r": hexc("#9a2214"), "h": hexc("#ff8c6c"),
        "k": hexc("#1d4416"), "g": hexc("#3c842a"), "G": hexc("#6ab442")}),
    "cabbage": ([
        "", "",
        ".....oooooo.....",
        "...ooLlLLlGoo...",
        "..oLlCCCCClGgo..",
        ".oLlCwwCCCClGgo.",
        ".olCwCCvCCCcGgo.",
        "oLlCCvCCCvCclGgo",
        "olCCCCvCCCvclGgo",
        "olcCCCCvCCcclGgo",
        "oGlcCCCCvccllGgo",
        ".oGlcccccclGggo.",
        ".ogGllllllGggko.",
        "..okgGGGGggkko..",
        "....oooooooo....",
    ], {"o": hexc("#1c3d12"), "k": hexc("#2a5a1c"), "g": hexc("#3a7a28"), "G": hexc("#4c9434"),
        "l": hexc("#6eb04a"), "L": hexc("#8ccc5e"), "c": hexc("#9ad06c"), "C": hexc("#bfe496"),
        "w": hexc("#e2f6cc"), "v": hexc("#7cb858")}),
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

SEEDS = [
    "", "", "", "", "",
    ".........oo.....",
    "........odbo....",
    "...oo...obao....",
    "..odbo...oo.....",
    "..obao.....oo...",
    "...oo.....odbo..",
    ".....oo...obao..",
    "....odbo...oo...",
    "....obao........",
    ".....oo.........",
]


def items() -> None:
    put("item/steel_ingot", pa.sprite(INGOT, tones("#262d36", "#46515d", "#66737f", "#8e9ba7", "#c4cfd8", "#f2f6fa")))
    put("item/steel_blend", pa.sprite(POWDER, tones("#1e2026", "#33363e", "#4a4e58", "#686d79", "#8f95a2", "#d4dbe6")))
    put("item/carbon", pa.sprite(CHUNK, tones("#09090b", "#17181c", "#25272d", "#353840", "#525866", "#8a93a4")))
    gem = tones("#2a0f3d", "#4c1d6c", "#7230a2", "#9a50d2", "#c486f0", "#f4e0ff")
    put("item/gemstone", pa.sprite(GEM_ITEM, {**gem, "q": hexc("#0b3a20"), "k": hexc("#16683a"),
                                              "g": hexc("#2fae60"), "h": hexc("#9af2bc")}))
    put("item/konterit", pa.sprite(CRYSTAL_CLUSTER, tones("#33103a", "#5c1a62", "#8a2a90", "#b746bc", "#dc7ae0", "#ffd8fc")))
    put("item/aetherit", pa.sprite(TALL_CRYSTAL, tones("#0a3346", "#125a73", "#1c88a6", "#37bcd8", "#86e6f4", "#efffff")))
    put("item/draconium", pa.sprite(FLAME_CRYSTAL, tones("#1a0306", "#4a080e", "#7a1016", "#b4241c", "#e24a22", "#ffb04e")))
    put("item/wyvernit", pa.sprite(SHARD, tones("#34536f", "#5f88ad", "#8fb6d8", "#bcdaf2", "#e6f4ff", "#ffffff")))
    put("item/dragon_food", pa.sprite(MEAT_BONE, {
        "o": hexc("#4a0e0c"), "r": hexc("#9a2a22"), "R": hexc("#c8443a"), "h": hexc("#f08a7a"),
        "w": hexc("#f2ead8"), "k": hexc("#c8bca0"), "b": hexc("#3a0a1e"), "B": hexc("#c2304a"), "p": hexc("#8a1a34")}))
    for kind, colors in SCALE_COLORS.items():
        put(f"item/{kind}_dragon_scale", pa.sprite(SCALE, {**colors, "x": hexc(SCALE_RIDGE[kind])}))
    for name, (rows, colors) in FOOD.items():
        put(f"item/{name}", pa.sprite(rows, colors))
    put("item/tomato_seeds", pa.sprite(SEEDS, {"o": hexc("#6a5a1a"), "a": hexc("#c8b04a"), "b": hexc("#f0e08a"),
                                               "d": hexc("#fff4c0")}))
    put("item/cabbage_seeds", pa.sprite(SEEDS, {"o": hexc("#1e140a"), "a": hexc("#4a321e"), "b": hexc("#6e4c30"),
                                                "d": hexc("#9a7454")}))
    put("item/barley_seeds", pa.sprite(SEEDS, {"o": hexc("#5a4214"), "a": hexc("#b08a3a"), "b": hexc("#d8b862"),
                                               "d": hexc("#f2dc98")}))


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
    magic_textures.build(put)
    boss_textures.build(put)
    lair_textures.build(put)
    ashlands_textures.build(put)
    seal_textures.build(put)
    frost_textures.build(put)
    grove_textures.build(put)
    earth_textures.build(put)
    storm_textures.build(put)
    deep_textures.build(put)
    shadow_textures.build(put)
    shadow_gear_textures.build(put)
    equipment_textures.build_entity(put)
    shadow_gear_textures.build_entity(put)
    dragon_spawn_eggs()
    plants()
    items()
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
