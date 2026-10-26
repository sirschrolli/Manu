#!/usr/bin/env python3
"""
Gegenstands-Texturen rund um die Kreaturen der Schattenwelt: die Spawn-Eier (Eiform mit den Merkmalen der Kreatur)
und die Schattenessenz, die ein besiegter Schattenpirscher zurücklässt.

Wird von tools/generate_textures.py aufgerufen (build(put)).
"""
from __future__ import annotations

from typing import Callable

from PIL import Image

import pixelart as pa
from creature_textures import egg, pal
from pixelart import hexc

# Schattenpirscher: schräge, violett glühende Augen, ein dunkles Maul, Rauchfäden
SHADE_STALKER_EGG = [
    "................",
    "................",
    "................",
    "................",
    "................",
    "....E......E....",
    "....eE....Ee....",
    "................",
    "................",
    "......mmmm......",
    ".....m.mm.m.....",
    "................",
    "....s......s....",
    "...s........s...",
]

# Kristallkriecher: Nachtkristalle auf dem Rücken, vier Augen, zwei Zangen
CRYSTAL_CRAWLER_EGG = [
    "................",
    "......C...C.....",
    ".....Cc..Cc.....",
    ".....cc..cc.....",
    "........C.......",
    "........c.......",
    "................",
    "................",
    ".....y.y..y.y...",
    "................",
    "......mm..mm....",
    ".......m..m.....",
]

# Nachtschwinge: spitze Ohren, rot glühende Augen, Reißzähne, Flügelhaut an den Seiten
NIGHT_WING_EGG = [
    "................",
    "....k......k....",
    "....kk....kk....",
    ".....k....k.....",
    "................",
    "................",
    ".....R....R.....",
    ".....r....r.....",
    "................",
    "......w..w......",
    "................",
    "...m........m...",
    "...mm......mm...",
    "....m......m....",
]

# Mondhirsch: Kristallgeweih, leuchtende Augen, helle Schnauze
MOON_STAG_EGG = [
    "..A.A......A.A..",
    "..aaa......aaa..",
    "...aa.....aa....",
    "....a.....a.....",
    "...ee.....ee....",
    "................",
    "................",
    ".....k....k.....",
    "................",
    "................",
    "......wwww......",
    ".....wwnnww.....",
    "......wwww......",
]

# Glimmerfalter: Fühler und silberblau glimmende Augenflecken
GLIMMER_MOTH_EGG = [
    "................",
    "....a......a....",
    ".....a....a.....",
    "......a..a......",
    "................",
    "................",
    "....zz....zz....",
    "...zZZz..zZZz...",
    "...zZZz..zZZz...",
    "....zz....zz....",
    "................",
    "....z......z....",
    "......z..z......",
]

# Umbrer: Kapuze, spitze Ohren, silbrig leuchtende Augen, Mondsilber-Saum
UMBRER_EGG = [
    "................",
    "................",
    "......hhhh......",
    ".....h....h.....",
    "..ee.h....h.ee..",
    "...eeh.ss.hee...",
    ".....h....h.....",
    "......hhhh......",
    "................",
    "................",
    "................",
    "................",
    "...tttttttttt...",
    "....tttttttt....",
]

# Umbrer-Wächter: Helmkamm, Visier, Mondsilber-Rüstung
UMBRER_GUARD_EGG = [
    ".......pp.......",
    "......pPPp......",
    ".......pp.......",
    "....mmmmmmmm....",
    "....mMMMMMMm....",
    "....m.s..s.m....",
    "....mmmmmmmm....",
    "................",
    "...mmmmmmmmmm...",
    "...mMMMMMMMMm...",
    "....mmmmmmmm....",
]

SHADOW_ESSENCE = [
    "................",
    ".......o........",
    "......oso.......",
    ".....osSo.......",
    ".....oSso..o....",
    "....osSSso.so...",
    "....oSSWSsoso...",
    "...osSWWWSso....",
    "...oSWWWWSso....",
    "...osSWWWSso....",
    "....osSSSso.....",
    ".....ossso......",
    "......ooo.......",
]


def spawn_eggs(put: Callable[[str, Image.Image], None]) -> None:
    put("item/shade_stalker_spawn_egg", egg(
        pal("#0a0812", "#1a1428", "#2c2240", "#3e3058", "#544274"), hexc("#050308"), SHADE_STALKER_EGG,
        {"E": hexc("#f2e4ff"), "e": hexc("#9a5aff"), "m": hexc("#050308"), "s": hexc("#7a68a6")}))
    put("item/crystal_crawler_spawn_egg", egg(
        pal("#08081a", "#141634", "#22264c", "#30366a", "#424a86"), hexc("#04040e"), CRYSTAL_CRAWLER_EGG,
        {"C": hexc("#f0ecff"), "c": hexc("#8a82e6"), "y": hexc("#8ff6ff"), "m": hexc("#c4bcf4")}))
    put("item/night_wing_spawn_egg", egg(
        pal("#0a0812", "#16101e", "#241a32", "#322446", "#42305a"), hexc("#050308"), NIGHT_WING_EGG,
        {"k": hexc("#1e1630"), "R": hexc("#ffd0d8"), "r": hexc("#d01030"), "w": hexc("#e8e2f0"), "m": hexc("#5a3e86")}))
    put("item/moon_stag_spawn_egg", egg(
        pal("#4a4672", "#6a6694", "#8a86b4", "#aeaad2", "#d4d0f0"), hexc("#2a2848"), MOON_STAG_EGG,
        {"a": hexc("#9ad8ff"), "A": hexc("#f4fcff"), "e": hexc("#6a66a0"), "k": hexc("#5ac8ff"), "w": hexc("#eeecfc"),
         "n": hexc("#2a2848")}))
    put("item/umbrer_spawn_egg", egg(
        pal("#3a2c5a", "#4e3c78", "#644e96", "#7c66b0", "#9682c8"), hexc("#1e1630"), UMBRER_EGG,
        {"h": hexc("#2a1e44"), "e": hexc("#b8b0cc"), "s": hexc("#dff0ff"), "t": hexc("#c4d0ec")}))
    put("item/umbrer_guard_spawn_egg", egg(
        pal("#1a2240", "#26305a", "#343f72", "#465290", "#5a6aa0"), hexc("#0e1226"), UMBRER_GUARD_EGG,
        {"p": hexc("#6a3aa0"), "P": hexc("#a070e0"), "m": hexc("#8a96b4"), "M": hexc("#dfe6f8"), "s": hexc("#dff0ff")}))
    put("item/glimmer_moth_spawn_egg", egg(
        pal("#141a30", "#222c4a", "#323e64", "#46547e", "#5a6a9a"), hexc("#0a0e1c"), GLIMMER_MOTH_EGG,
        {"a": hexc("#9adcff"), "z": hexc("#5ac8ff"), "Z": hexc("#f0fbff")}))


def items(put: Callable[[str, Image.Image], None]) -> None:
    put("item/shadow_essence", pa.sprite(SHADOW_ESSENCE, {
        "o": hexc("#140c22"), "s": hexc("#4a2a7a"), "S": hexc("#8a5ad0"), "W": hexc("#ecdcff")}))


def build(put: Callable[[str, Image.Image], None]) -> None:
    spawn_eggs(put)
    items(put)


if __name__ == "__main__":
    import sys

    sheet: dict[str, Image.Image] = {}
    build(lambda key, img: sheet.__setitem__(key, img))
    zoom, pad, cols = 8, 4, 6
    cell = 16 * zoom + pad
    rows = (len(sheet) + cols - 1) // cols
    out = Image.new("RGBA", (cols * cell + pad, rows * cell + pad), (60, 60, 70, 255))
    for i, img in enumerate(sheet.values()):
        out.alpha_composite(img.resize((16 * zoom, 16 * zoom), Image.NEAREST), (pad + (i % cols) * cell, pad + (i // cols) * cell))
    out.save(sys.argv[1] if len(sys.argv) > 1 else "shadow_creature_items.png")
