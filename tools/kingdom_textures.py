#!/usr/bin/env python3
"""
Texturen rund um die Armee: Kommandostab, Rekrutierungstisch und das Anwerbe-Fenster
(Spawn-Eier der Soldaten und Banditen: tools/items_eggs.py). Wird von tools/generate_textures.py aufgerufen (build(put)).
"""
from __future__ import annotations

from PIL import Image

import pixelart as pa
from pixelart import CLEAR, hexc

BLUE = {"U": hexc("#121d44"), "u": hexc("#1f3373"), "V": hexc("#2a4796"), "v": hexc("#3a5fb8"), "w": hexc("#5a82d6")}
GOLD = {"g": hexc("#8c5f16"), "G": hexc("#d8a63a"), "y": hexc("#f7dc80")}


# =============================================================================
# Kommandostab: blau umwickelter Stab mit goldenen Beschlägen und Knauf
# =============================================================================

BATON = [
    "............ooo.",
    "...........oyyGo",
    "..........oyGGgo",
    ".........oGGGgo.",
    "........ovwgoo..",
    ".......ovwVuo...",
    "......oywVuo....",
    ".....oGgVuo.....",
    "....ovwVuo......",
    "...ovwVuo.......",
    "..oywguo........",
    ".oGGgGo.........",
    ".oyGgo..........",
    "..ogo...........",
    "................",
    "................",
]


def baton() -> Image.Image:
    colors = {"o": hexc("#16142a"), **BLUE, **GOLD}
    return pa.sprite(BATON, colors)


# =============================================================================
# Rekrutierungstisch: Tischläufer, Wappenbehang und Kleinkram auf dem Tisch
# =============================================================================

def table_cloth() -> Image.Image:
    """Blauer Läufer mit goldenen Kanten (oben aufliegend), 16x16 – genutzt wird die Mitte (x 4..12)."""
    img = pa.new()
    px = img.load()
    for y in range(16):
        for x in range(16):
            if x in (4, 11):
                c = GOLD["G"] if y % 4 else GOLD["g"]
            elif 4 < x < 11:
                c = BLUE["V"] if (x * 7 + y * 3) % 11 else BLUE["u"]
                if x == 5:
                    c = BLUE["v"]
            else:
                c = BLUE["u"]
            px[x, y] = c
    return img


TABLE_BANNER = [
    "gGGGGGGg",
    "uVVGGVVu",
    "uVVGGVVu",
    "GGGyyGGG",
    "uVVGGVVu",
    "uVVGGVVu",
    "uvVVVVvu",
    "GgGgGgGg",
]


def table_banner() -> Image.Image:
    """Behang vorn am Tisch (8x8 genutzt): Wappen mit goldenem Kreuz und Fransen."""
    img = pa.new()
    img.alpha_composite(pa.sprite(TABLE_BANNER, {**BLUE, **GOLD}, w=8, h=8), (4, 0))
    return img


TABLE_ITEMS = [
    # Buchdeckel (0..8 x 0..7), Seiten (0..8 x 8..13), Smaragd (9..12 x 0..3), Tinte (9..11 x 5..7), Feder (12..15 x 4..15)
    "qRRRRRRRq.ggg...",
    "RrRRRRRrR.gGyg..",
    "RRRRRRRRR.gyGg..",
    "RRRRRRRRR..gg...",
    "RRRRRRRRR...W...",
    "RrRRRRRrR.ZZ.Ww.",
    "qRRRRRRRq.Zz.Ww.",
    ".........ZZZ.Ww.",
    "PPPPpPPPP....wW.",
    "PlllpPllP....wW.",
    "PPPPpPPPP.....w.",
    "PlllpllPP.....w.",
    "PPPPpPPPP.....k.",
    "PllPplllP.....k.",
    ".........ee.....",
    ".........eE.....",
]


def table_items() -> Image.Image:
    colors = {"q": hexc("#3a0a0e"), "R": hexc("#7a161b"), "r": hexc("#a82428"),
              "P": hexc("#efe6cc"), "p": hexc("#c9bc98"), "l": hexc("#8a7c64"),
              "g": hexc("#0f6a3a"), "G": hexc("#2fbf6a"), "y": hexc("#b8f8d0"),
              "Z": hexc("#101014"), "z": hexc("#3a4a6a"),
              "W": hexc("#f4f2ea"), "w": hexc("#c8c4b8"), "k": hexc("#2a2a2e"),
              "e": hexc("#17a858"), "E": hexc("#6ff0a8")}
    return pa.sprite(TABLE_ITEMS, colors)


# =============================================================================
# Anwerbe-Fenster (306x176) im Stil der Minecraft-Fenster
# =============================================================================

PANEL = hexc("#c6c6c6")
LIGHT = hexc("#ffffff")
SHADOW = hexc("#555555")
OUTLINE = hexc("#000000")
INSET_DARK = hexc("#373737")


def container_panel(w: int, h: int) -> Image.Image:
    """Grauer Fensterrahmen wie bei Truhen und Öfen: schwarzer Rand, abgerundete Ecken, helle/dunkle Kante."""
    img = Image.new("RGBA", (w, h), CLEAR)
    px = img.load()
    for y in range(h):
        for x in range(w):
            px[x, y] = PANEL
    # Außenrand mit runden Ecken
    for x in range(w):
        px[x, 0] = OUTLINE
        px[x, h - 1] = OUTLINE
    for y in range(h):
        px[0, y] = OUTLINE
        px[w - 1, y] = OUTLINE
    # helle Kante oben/links (2 px), dunkle Kante unten/rechts (2 px)
    for x in range(1, w - 1):
        px[x, 1] = LIGHT
        px[x, 2] = LIGHT
        px[x, h - 2] = SHADOW
        px[x, h - 3] = SHADOW
    for y in range(1, h - 1):
        px[1, y] = LIGHT
        px[2, y] = LIGHT
        px[w - 2, y] = SHADOW
        px[w - 3, y] = SHADOW
    # Ecken: oben rechts und unten links gemischt, außen abgerundet
    for (cx, cy) in ((0, 0), (w - 1, 0), (0, h - 1), (w - 1, h - 1)):
        px[cx, cy] = CLEAR
    for (x, y) in ((1, 0), (0, 1), (w - 2, 0), (w - 1, 1), (0, h - 2), (1, h - 1), (w - 1, h - 2), (w - 2, h - 1)):
        px[x, y] = CLEAR
    for (x, y) in ((1, 1), (w - 2, 1), (1, h - 2), (w - 2, h - 2)):
        px[x, y] = OUTLINE
    px[w - 3, 1] = PANEL
    px[w - 3, 2] = PANEL
    px[w - 2, 2] = SHADOW
    px[1, h - 3] = PANEL
    px[2, h - 3] = PANEL
    px[2, h - 2] = SHADOW
    px[w - 3, h - 3] = SHADOW
    px[2, 2] = LIGHT
    return img


def inset(img: Image.Image, x0: int, y0: int, w: int, h: int, fill=hexc("#000000")):
    """Vertiefte Fläche wie das Spielerbild im Inventar: dunkle Kante oben/links, helle unten/rechts."""
    px = img.load()
    for y in range(y0, y0 + h):
        for x in range(x0, x0 + w):
            if y == y0 or x == x0:
                px[x, y] = INSET_DARK
            elif y == y0 + h - 1 or x == x0 + w - 1:
                px[x, y] = LIGHT
            else:
                px[x, y] = fill
    px[x0 + w - 1, y0] = PANEL
    px[x0, y0 + h - 1] = PANEL


def separator(img: Image.Image, x0: int, y0: int, w: int):
    px = img.load()
    for x in range(x0, x0 + w):
        px[x, y0] = hexc("#8b8b8b")
        px[x, y0 + 1] = LIGHT


def recruit_gui() -> Image.Image:
    img = container_panel(306, 176)
    inset(img, 128, 18, 68, 96)                                  # Bild des Soldaten
    separator(img, 128, 116, 170)                                # Linie über der Beschreibung
    # dezente Rahmenlinie um die Liste
    px = img.load()
    for y in range(17, 162):
        px[125, y] = hexc("#8b8b8b")
        px[126, y] = LIGHT
    return img


def build(put) -> None:
    put("item/command_baton", baton())
    put("block/recruitment_table_cloth", table_cloth())
    put("block/recruitment_table_banner", table_banner())
    put("block/recruitment_table_items", table_items())
    put("gui/recruitment_table", recruit_gui())
