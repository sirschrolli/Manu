#!/usr/bin/env python3
"""
Texturen der Schattenwelt – Pflanzen und Kristalldrusen (die vollen Blöcke kommen aus tools/shadow_blocks.py):

  * block/night_crystal_cluster(_emissive) – Druse aus spitzen Kristallen (Kreuzpflanze, leuchtend)
  * block/ghostcap(_emissive)           – Geisterpilze mit leuchtenden Hüten
  * block/shadow_sprouts                – fedrige Schattenhalme (grau, Farbe aus dem Biom)
  * block/pale_grass                    – dürre, fahle Halme der Bleichen Öde
  * block/soul_berry_bush_stage0..3 – Seelenbeerenstrauch (die Beeren als Gegenstand: tools/items_food.py)
  * block/glimmer_vines(_plant)         – Glimmerranken

Wird von tools/generate_textures.py aufgerufen (build(put)).
"""
from __future__ import annotations

import math
from typing import Callable

from PIL import Image

import pixelart as pa
import shadow_blocks
from pixelart import Color, hexc

N = 16

CRYSTAL = pa.palette("#17154a", "#232270", "#33339a", "#4846c4", "#635ee6", "#8a7ef8", "#b4a8ff", "#ddd6ff", "#f7f4ff")
GHOST = pa.palette("#5a6a8a", "#8a9cbc", "#b8c8e4", "#e2ecfa", "#ffffff")
GHOST_GLOW = pa.palette("#6ad0f0", "#a8ecff", "#e8fbff")
STEM = pa.palette("#8c8aa0", "#b4b2c6", "#d8d6e6")
PALE = pa.palette("#5c566a", "#7a7488", "#9c96a8", "#bdb8c6", "#dcd8e2")


def _gray(v: int, a: int = 255) -> Color:
    v = max(0, min(255, v))
    return (v, v, v, a)


# =============================================================================
# Kristalldrusen und Pflanzen
# =============================================================================

NIGHT_CLUSTER = [
    "................",
    ".......a........",
    "......aba.......",
    "......abc...a...",
    "..a...abc..aba..",
    ".aba..abc..abc..",
    ".abc..abcd.abc..",
    ".abcd.abcd.abcd.",
    "..bcd.abcd.abcd.",
    "..bcd.abcd..bcd.",
    "...cdabbcd..bcd.",
    "...cdabccdabcd..",
    "....abbccdabcd..",
    "....abbccdbccd..",
    "...aabbccddccd..",
    "..aaabbbcccddd..",
]


def night_crystal_cluster() -> tuple[Image.Image, Image.Image]:
    colors = {"a": CRYSTAL[6], "b": CRYSTAL[5], "c": CRYSTAL[3], "d": CRYSTAL[2]}
    img = pa.sprite(NIGHT_CLUSTER, colors)
    px = img.load()
    for (x, y) in [(7, 1), (12, 4), (2, 4), (7, 2)]:
        px[x, y] = CRYSTAL[8]
    glow = pa.new()
    gp = glow.load()
    for y in range(N):
        for x in range(N):
            c = px[x, y]
            if c[3] and c in (CRYSTAL[8], CRYSTAL[6]):
                gp[x, y] = c
    return img, glow


GHOSTCAP = [
    "................",
    "................",
    "........oCCCo...",
    ".......oCwwcCo..",
    ".......CccccCC..",
    "..oCCo..o.s..o..",
    ".oCwcCo...s.....",
    ".CccccC...s.....",
    "...s......s.....",
    "...s.....ss.....",
    "...s....oCCo....",
    "...ss..oCwcCo...",
    "....s..CcccC....",
    "....s....s......",
    "...gsg..gsg.....",
    "................",
]


def ghostcap() -> tuple[Image.Image, Image.Image]:
    colors = {"o": GHOST[0], "C": GHOST[1], "c": GHOST[2], "w": GHOST[4], "s": STEM[1], "g": hexc("#3a3448")}
    img = pa.sprite(GHOSTCAP, colors)
    glow = pa.new()
    gp = glow.load()
    px = img.load()
    for y, row in enumerate(GHOSTCAP):
        for x, ch in enumerate(row):
            if ch == "w":
                gp[x, y] = GHOST_GLOW[2]
            elif ch == "c":
                gp[x, y] = GHOST_GLOW[1]
    _ = px
    return img, glow


SPROUTS = [
    "................",
    "................",
    "................",
    "...........a....",
    "..a.......ab....",
    "..ba...a..b.a...",
    "...b..ab.ab.b...",
    ".a.b.a.b.b.b..a.",
    ".ba.b..bb..b.ab.",
    "..b.b.a.b.ab.b..",
    "..bb.ab.b.b.bb..",
    "...bb.b.bb.bb...",
    "...cbbbcbbcbc...",
    "....cbcbcbbc....",
    "....ccccccccc...",
    "................",
]


def shadow_sprouts() -> Image.Image:
    return pa.sprite(SPROUTS, {"a": _gray(196), "b": _gray(160), "c": _gray(120)})


PALE_GRASS = [
    "................",
    "..........a.....",
    "...a......a.....",
    "...a.....ab.....",
    "...b..a..b......",
    "...b..a..b...a..",
    "...bb.b..b..ab..",
    "....b.b.bb..b...",
    "..a.b.bb.b..b...",
    "..ab.b.b.b.bb...",
    "...b.b.bb.bb....",
    "...bbcb.bcb.....",
    "....bcbcbcb.....",
    "....ccbccbc.....",
    "...cddcdcdcd....",
    "................",
]


def pale_grass() -> Image.Image:
    return pa.sprite(PALE_GRASS, {"a": PALE[4], "b": PALE[3], "c": PALE[2], "d": PALE[1]})


# =============================================================================
# Seelenbeeren und Glimmerranken
# =============================================================================

BUSH_LEAF = {"d": hexc("#0e2e2c"), "m": hexc("#174a46"), "l": hexc("#22665e"), "h": hexc("#348a7e"), "s": hexc("#1c1a24")}
BERRY_UNRIPE = {"b": hexc("#1e6e72"), "B": hexc("#3aa4a4")}
BERRY_RIPE = {"b": hexc("#2ad2cc"), "B": hexc("#c8fffa")}

SOUL_BUSH = [
    [
        "", "", "", "", "", "", "", "", "",
        "................",
        ".......l........",
        "......lhl..l....",
        "....l..m..lhl...",
        "...lhl.m...m....",
        "....m..ms.m.....",
        ".......ss.......",
    ],
    [
        "", "", "", "", "",
        "................",
        "......l..l......",
        ".....lhllhl.l...",
        "...l.mhmmhm.hl..",
        "..lhlmmlmmlmm...",
        "...mmdmmlmdmml..",
        "..lmdmldmmdmlh..",
        "...mdmmdmdmmm...",
        "....dmddsdmd....",
        ".....d.ss.d.....",
        ".......ss.......",
    ],
    [
        "", "", "",
        "................",
        ".......l..l.....",
        "....l.lhllhl....",
        "...lhlmhmmhm.l..",
        "..lmhmmbmlmmlhl.",
        "..mmlmmmmlbmmm..",
        ".lhmbmdmlmmmdml.",
        "..mmmmdmmdbmmh..",
        "..lmdmbdmmdmlm..",
        "...mdmmdmdmmm...",
        "....dmddsdmbd...",
        ".....d.ss.d.....",
        ".......ss.......",
    ],
    [
        "", "",
        "................",
        "......l...l.....",
        "...l.lhlBlhl.l..",
        "..lhlmhmbmhmlhl.",
        "..mBmmbmmlmmBm..",
        ".lbmlmmmBmmbmml.",
        ".mmmhbmlbmmmBmh.",
        ".lBmmmdmlmmbdml.",
        "..mmbmdmmdBmmh..",
        "..lmdmBdmmdmbm..",
        "...mbmmdmBmmm...",
        "....dmddsdmbd...",
        ".....d.ss.d.....",
        ".......ss.......",
    ],
]


def soul_berry_bush(stage: int) -> Image.Image:
    colors = dict(BUSH_LEAF)
    colors.update(BERRY_RIPE if stage == 3 else BERRY_UNRIPE)
    return pa.sprite(SOUL_BUSH[stage], colors)


VINE_COLORS = {"d": hexc("#2a3450"), "m": hexc("#44587e"), "l": hexc("#6a86b0"), "h": hexc("#9cb6dc"),
               "g": hexc("#c8e0ff"), "G": hexc("#f2f8ff")}

GLIMMER_VINES_PLANT = [
    ".....m....m.....",
    ".....ml...m.....",
    "......m..lm.....",
    "......m..m......",
    ".....lm..m......",
    "......m..ml.....",
    "......dm.m......",
    ".......m.m......",
    ".......m.mh.....",
    "......hm.m......",
    ".......m.m......",
    ".......mdm......",
    "........m.......",
    "........ml......",
    ".......lm.......",
    "........m.......",
]

GLIMMER_VINES = [
    ".....m....m.....",
    ".....ml...m.....",
    "......m..lm.....",
    "......m..m......",
    ".....lm..m......",
    "......m..ml.....",
    "......dm.m......",
    ".......m.m......",
    ".......mhm......",
    "......lmmh......",
    "......hggh......",
    ".....hgGGgh.....",
    ".....gGGGGg.....",
    ".....hgGGgh.....",
    "......hggh......",
    ".......hh.......",
]


def glimmer_vines() -> tuple[Image.Image, Image.Image]:
    return pa.sprite(GLIMMER_VINES, VINE_COLORS), pa.sprite(GLIMMER_VINES_PLANT, VINE_COLORS)


# =============================================================================
# Ausgabe
# =============================================================================

def build(put: Callable[[str, Image.Image], None]) -> None:
    # die vollen Blöcke (Stein, Erde, Holz, Kristall, Moos …) kommen aus tools/shadow_blocks.py
    shadow_blocks.build(put)
    cluster, cluster_glow = night_crystal_cluster()
    put("block/night_crystal_cluster", cluster)
    put("block/night_crystal_cluster_emissive", cluster_glow)
    cap, cap_glow = ghostcap()
    put("block/ghostcap", cap)
    put("block/ghostcap_emissive", cap_glow)
    put("block/shadow_sprouts", shadow_sprouts())
    put("block/pale_grass", pale_grass())
    for stage in range(4):
        put(f"block/soul_berry_bush_stage{stage}", soul_berry_bush(stage))
    head, plant = glimmer_vines()
    put("block/glimmer_vines", head)
    put("block/glimmer_vines_plant", plant)


if __name__ == "__main__":
    import sys
    out: dict[str, Image.Image] = {}
    build(lambda k, v: out.__setitem__(k, v))
    target = sys.argv[1] if len(sys.argv) > 1 else "shadow_preview.png"
    zoom, pad, cols = 8, 6, 6
    cell_w = 16 * 3 * zoom // 2 + pad
    rows = math.ceil(len(out) / cols)
    sheet = Image.new("RGBA", (cols * cell_w + pad, rows * cell_w + pad), (40, 42, 48, 255))
    for i, (key, img) in enumerate(out.items()):
        tile = pa.checker(48, 48, cell=4)
        tile.alpha_composite(pa.tiled(img, 3))
        tile = tile.resize((48 * zoom // 2, 48 * zoom // 2), Image.NEAREST)
        sheet.alpha_composite(tile, (pad + (i % cols) * cell_w, pad + (i // cols) * cell_w))
    sheet.save(target)
    print(f"{len(out)} Texturen, Vorschau: {target}")
