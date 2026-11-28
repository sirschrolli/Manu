#!/usr/bin/env python3
"""
Die Küche der Welten (seit 1.16) im Vanilla-Stil (siehe tools/item_style.py): Mehl, Bergkäse und Käselaib, Käsespätzle,
Brezel, Bratwurst, Rum, Glutschote (mit den vier Stufen der Pflanze), Glutchili, Pilzspieß, Schiffszwieback,
Chorusmarmelade, Dämmerbrei, Seelenbeerkuchen, Sternenkandis, Sturzkuchen, Pilgerration, Fischsuppe, Fischbrötchen,
Lebkuchenherz, Feentee und Glimmerküchlein – dazu die drei Flächen des Käselaib-Blocks.

Gewölbtes wird über eine Maske schattiert (Licht oben links, st.shaded), danach kommt die Zeichnung darüber (Löcher,
Salz, Grillstreifen, Gitter); der Umriss ist immer die dunkelste Stufe des angrenzenden Materials.
"""
from __future__ import annotations

import math
from typing import Callable

from PIL import Image

import item_style as st

# =============================================================================
# Tonleitern
# =============================================================================

LINEN = st.ramp("#d9c79d", dark=0.32, light=0.97, cool=50, warm=90, chroma_light=0.5)
TWINE = st.ramp("#9c7444", dark=0.24, light=0.86, cool=30, warm=80)
FLOUR = st.ramp("#f2ede2", dark=0.52, light=1.0, base_level=4, cool=250, warm=90, chroma_light=0.3)
CHEESE = st.ramp("#f3c63e", dark=0.36, light=0.98, cool=45, hue_dark=10, warm=100, chroma_light=0.5)
CHEESE_PALE = st.ramp("#f6dc7c", dark=0.42, light=0.99, cool=50, warm=100, chroma_light=0.45)
RIND = st.ramp("#c98a2c", dark=0.26, light=0.9, cool=30, hue_dark=14, warm=80, chroma_light=0.6)
CRUST = st.ramp("#b86a28", dark=0.22, light=0.92, cool=20, hue_dark=12, warm=80, chroma_light=0.6)
PRETZEL = st.ramp("#8e4a1c", dark=0.2, light=0.86, cool=15, hue_dark=10, warm=70, chroma_light=0.75)
SALT = st.fixed("#9aa0a8", "#b8bcc4", "#d4d8de", "#e8eaee", "#f6f7f9", "#fdfdfd", "#ffffff")
RAW_MEAT = st.ramp("#e08c80", dark=0.3, light=0.96, cool=350, hue_dark=10, warm=40, chroma_light=0.5)
GRILLED = st.ramp("#a4542a", dark=0.2, light=0.86, cool=10, hue_dark=12, warm=60, chroma_light=0.7)
MUSTARD = st.ramp("#e0b020", dark=0.36, light=0.96, cool=50, warm=95)
WOOD = st.ramp("#8a5a32", dark=0.2, light=0.82, cool=30, warm=70)
BOWL = st.ramp("#8a5c34", dark=0.2, light=0.84, cool=30, warm=75, chroma_light=0.6)
SPAETZLE = st.ramp("#f0cc5a", dark=0.4, light=0.98, cool=50, warm=100, chroma_light=0.5)
ONION_FRIED = st.ramp("#a8641e", dark=0.26, light=0.86, cool=30, warm=70)
HERB = st.ramp("#4c9a34", dark=0.2, light=0.86, cool=170, warm=110, chroma_light=0.7)
GLASS_RUM = st.ramp("#5a3418", dark=0.14, light=0.7, cool=20, warm=60)
RUM = st.ramp("#c06a1a", dark=0.26, light=0.9, cool=20, hue_dark=10, warm=70, chroma_light=0.6)
LABEL = st.ramp("#e6dcc0", dark=0.5, light=0.98, cool=60, warm=90)
CORK = st.ramp("#a87848", dark=0.3, light=0.86)
PEPPER = st.ramp("#e0301c", dark=0.24, light=0.95, cool=350, hue_dark=10, warm=60, hue_light=24, chroma_light=0.75)
EMBER = st.ramp("#ff9a3a", dark=0.5, light=0.99, cool=40, warm=95, chroma_light=0.6)
PEPPER_STEM = st.ramp("#5c7a2a", dark=0.2, light=0.82, cool=150, warm=100)
CHILI = st.ramp("#a8341c", dark=0.2, light=0.86, cool=350, hue_dark=10, warm=50, chroma_light=0.75)
STICK = st.ramp("#8a6438", dark=0.24, light=0.84)
CRIMSON = st.ramp("#a8263a", dark=0.2, light=0.86, cool=330, warm=30, chroma_light=0.7)
WARPED = st.ramp("#1e8a7c", dark=0.2, light=0.88, cool=220, warm=150, chroma_light=0.6)
HARDTACK = st.ramp("#d8b072", dark=0.34, light=0.97, cool=40, warm=90, chroma_light=0.5)
JAR = st.ramp("#c8dcdc", dark=0.4, light=1.0, cool=210, warm=190, chroma_light=0.3)
CHORUS = st.ramp("#9a5aa8", dark=0.2, light=0.92, cool=290, hue_dark=10, warm=330, chroma_light=0.55)
CLOTH = st.ramp("#e8d0e8", dark=0.46, light=0.99, cool=300, warm=320)
DUSK_PORRIDGE = st.ramp("#b8a4d8", dark=0.36, light=0.98, cool=280, warm=300, chroma_light=0.45)
SOUL = st.ramp("#3fd6d0", dark=0.26, light=0.99, cool=240, chroma_light=0.35)
DUSK_CRUST = st.ramp("#7a5a84", dark=0.18, light=0.86, cool=300, warm=40, hue_light=25, chroma_light=0.6)
STAR = st.ramp("#a8d8ff", dark=0.42, light=1.0, cool=260, warm=200, chroma_light=0.35)
STAR_GOLD = st.ramp("#ffd860", dark=0.46, light=1.0, cool=50, warm=95)
CARAMEL = st.ramp("#c87a1c", dark=0.26, light=0.94, cool=30, hue_dark=12, warm=80, chroma_light=0.65)
APPLE_SLICE = st.ramp("#f0d488", dark=0.46, light=0.99, cool=60, warm=95)
CAKE = st.ramp("#e8b866", dark=0.34, light=0.97, cool=50, warm=90, chroma_light=0.5)
KELP = st.ramp("#3e5a26", dark=0.14, light=0.72, cool=160, warm=100)
JERKY = st.ramp("#7a3a24", dark=0.18, light=0.8, cool=10, warm=50)
BROTH = st.ramp("#e6a050", dark=0.36, light=0.96, cool=40, warm=85, chroma_light=0.55)
FISH = st.ramp("#f0c4b0", dark=0.4, light=0.99, cool=20, warm=60)
FISH_SKIN = st.ramp("#8a9aa8", dark=0.3, light=0.92, cool=240, warm=200)
LETTUCE = st.ramp("#7ac04a", dark=0.26, light=0.95, cool=150, warm=110, chroma_light=0.6)
GINGER = st.ramp("#9a5426", dark=0.2, light=0.86, cool=20, hue_dark=10, warm=60, chroma_light=0.7)
ICING = st.fixed("#a8b0bc", "#c4cad2", "#dde1e6", "#eef0f2", "#f8f9fa", "#fdfdfe", "#ffffff")
RIBBON = st.ramp("#d82a3a", dark=0.24, light=0.9, cool=340, warm=20)
TEA = st.ramp("#e48aa8", dark=0.34, light=0.97, cool=330, warm=20, chroma_light=0.5)
FAE_GLASS = st.ramp("#d8e8f0", dark=0.48, light=1.0, cool=230, warm=200, chroma_light=0.3)
MOONBELL = st.ramp("#b8c8ff", dark=0.42, light=1.0, cool=260, warm=220, chroma_light=0.4)
FROSTING = st.ramp("#f0a8d0", dark=0.42, light=0.99, cool=330, warm=350, chroma_light=0.45)
CUP = st.ramp("#8ac8e8", dark=0.36, light=0.97, cool=230, warm=190, chroma_light=0.5)
SPARK = st.fixed("#d8b8ff", "#e4ccff", "#eedcff", "#f6eaff", "#fbf4ff", "#fefbff", "#ffffff")
LEAF_DARK = st.ramp("#4a5e26", dark=0.16, light=0.74, cool=150, warm=95)
NETHER_STEM = st.ramp("#7a2a26", dark=0.18, light=0.74, cool=340, warm=20)


def _mask(text: str) -> set[tuple[int, int]]:
    rows = (text[1:] if text.startswith("\n") else text).rstrip().split("\n")
    return {(x, y) for y, row in enumerate(rows) for x, c in enumerate(row) if c == "#"}


def overlay(s: st.Spr, art: str, legend: dict[str, st.Px] | None = None) -> st.Spr:
    """Legt eine Zeichnung über das Sprite ('.' lässt durch)."""
    return s.over(st.Spr.art(art, legend=legend))


def dim(s: st.Spr, pts: set[tuple[int, int]], by: int) -> st.Spr:
    """Dunkelt (by < 0) oder hellt Pixel um {@code by} Stufen auf."""
    for x, y in pts:
        p = s.get(x, y)
        if p:
            s.set(x, y, (p[0], st.clamp(p[1] + by)))
    return s


def render(s: st.Spr, ramps: dict[str, st.Ramp], prefer: tuple[str, ...] = (), override: dict[str, str] | None = None) -> Image.Image:
    return s.outline(prefer=prefer, override=override).image(ramps)


# =============================================================================
# Mehl: ein Leinensack, oben offen mit einem Häufchen Mehl, mit Kordel gebunden, eine Ähre aufgedruckt
# =============================================================================

def flour() -> st.Spr:
    body = _mask("""
................
................
................
................
................
................
......####......
.....######.....
....########....
...##########...
...##########...
..############..
..############..
..############..
...##########...
....########....
""")
    s = st.shaded(body, "A", cuts=(-0.1, 0.35, 0.68, 0.9), grain=0.06, seed=3)
    s = overlay(s, """
................
................
.......5........
......555.......
.....55655......
....5566665.....
....bccccb......
................
................
.......d........
......ddd.......
.......d........
......d.d.......
.......d........
................
................
""", legend={"5": ("C", 5), "6": ("C", 6), "b": ("B", 3), "c": ("B", 4), "d": ("A", 2)})
    # Mehlstaub am Sack
    for x, y in ((4, 8), (5, 7), (10, 8)):
        s.set(x, y, ("C", 4))
    return s


# =============================================================================
# Bergkäse: ein Keil mit Löchern und Rinde
# =============================================================================

def cheese() -> st.Spr:
    """Ein Keil, auf der Schnittfläche liegend: die dreieckige Seite mit Löchern, oben die schräge Schnittkante, hinten Rinde."""
    s = st.Spr()
    for x in range(16):
        for y in range(16):
            slope = 12.6 - (x - 1.0) * (8.4 / 12.0)
            if 1 <= x <= 13 and slope <= y + 0.5 <= 13.0:
                lvl = 5 if y + 0.5 < slope + 1.0 else 3 if y >= 12 else 4
                s.set(x, y, ("A", lvl))
            elif 1 <= x <= 13 and slope - 2.2 <= y + 0.5 < slope:
                s.set(x, y, ("C", 6 if x < 8 else 5))
            elif x == 14 and slope - 2.0 <= y + 0.5 <= 13.0:
                s.set(x, y, ("B", 4 if y < 6 else 3))
    for x, y in ((4, 11), (5, 11), (8, 10), (11, 8), (11, 9), (10, 12), (7, 12), (12, 11)):
        if s.get(x, y) and s.get(x, y)[0] == "A":
            s.set(x, y, ("A", 2))
    for x, y in ((4, 10), (8, 9), (11, 7), (12, 10)):
        if s.get(x, y) and s.get(x, y)[0] == "A":
            s.set(x, y, ("A", 3))
    return s


def cheese_wheel_item() -> st.Spr:
    """Ein ganzer Laib, schräg von oben: heller Deckel mit Rindenrand, die Rinde rundum, vorn rechts fehlt ein Keil."""
    cx, cy, rx, ry, depth = 8.0, 6.2, 7.0, 3.9, 4

    def top(x: int, y: float) -> bool:
        return ((x + 0.5 - cx) / rx) ** 2 + ((y - cy) / ry) ** 2 <= 1.0

    def wedge(x: int, y: float) -> bool:
        a = math.degrees(math.atan2((y - cy) / ry, (x + 0.5 - cx) / rx))
        return -8.0 <= a <= 62.0

    s = st.Spr()
    for x in range(16):
        for y in range(16):
            yc = y + 0.5
            if top(x, yc) and not wedge(x, yc):
                edge = not all(top(x + dx, yc + dy) for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)))
                if edge:
                    s.set(x, y, ("B", 5 if yc < cy else 4))
                else:
                    s.set(x, y, ("A", 6 if x < 6 and yc < cy else 5))
                continue
            # die Rinde unter dem Deckel (vorn), der Keil schneidet auch sie weg
            for t in range(1, depth + 1):
                if top(x, yc - t) and yc - t > cy - 0.5 and not wedge(x, yc - t):
                    s.set(x, y, ("B", 4 if x < 7 else 3 if x < 12 else 2))
                    break
    # die Schnittflächen des Keils: zur Mitte hin senkrecht, gelb mit Löchern
    for y in range(6, 12):
        for x in range(8, 16):
            yc = y + 0.5
            if s.get(x, y) is None and top(x, cy) and x <= 14:
                inner_top = cy + (x + 0.5 - cx) * 0.18
                if inner_top - 0.5 <= yc <= cy + depth + 0.2 - (x - 8) * 0.25 and x < 8 + (depth + 6 - (y - 6)):
                    s.set(x, y, ("C", 5 if y < 8 else 4))
    for x, y in ((10, 9), (12, 8), (9, 10)):
        if s.get(x, y) and s.get(x, y)[0] == "C":
            s.set(x, y, ("C", 2))
    return s


# =============================================================================
# Käsespätzle: in der Schüssel, gelbe Spätzle, geschmolzener Käse, Röstzwiebeln, Schnittlauch
# =============================================================================

BOWL_ART = """
................
................
................
................
................
................
................
................
................
.555555555555556
.4444444444444445
..33333333333333.
..33333333333332.
...233333333322..
....2222222222...
......111111.....
"""


def bowl(fill: Callable[[st.Spr], None]) -> st.Spr:
    """Eine Holzschüssel (Slot D) mit Inhalt, den {@code fill} hineinzeichnet (Zeilen 5–9)."""
    s = st.Spr()
    rows = [
        "................",
        "................",
        "................",
        "................",
        "................",
        "................",
        "................",
        "................",
        "................",
        ".rrrrrrrrrrrrrr.",
        ".tsssssssssssssr",
        "..qqqqqqqqqqqqq.",
        "..qqqqqqqqqqqqp.",
        "...pqqqqqqqqqp..",
        "....pppppppppp..",
        "......pppppp....",
    ]
    s.draw("\n".join(rows))
    fill(s)
    return s


def spaetzle() -> st.Spr:
    def fill(s: st.Spr) -> None:
        s.draw("""
................
................
................
................
................
.....5456.......
...54565455.....
..5465456545r...
.565456545654r..
.45654565456545.
""", legend={"r": ("A", 3)})
        # geschmolzener Käse (heller Slot B) in Fäden
        for x, y in ((4, 6), (5, 6), (8, 7), (9, 7), (10, 7), (3, 8), (11, 8), (6, 9), (7, 9)):
            s.set(x, y, ("B", 6))
        for x, y in ((6, 5), (7, 6), (12, 8), (13, 9)):
            s.set(x, y, ("B", 5))
        # Röstzwiebeln und Schnittlauch obenauf
        for x, y in ((5, 5), (8, 6), (10, 8), (4, 8), (12, 9)):
            s.set(x, y, ("C", 4))
        for x, y in ((9, 6), (3, 9), (11, 7)):
            s.set(x, y, ("C", 3))
        for x, y in ((7, 5), (6, 8), (9, 9), (13, 8)):
            s.set(x, y, ("E", 4))
        s.set(2, 9, ("E", 3))
    return bowl(fill)


# =============================================================================
# Brezel: die geschlungene Form, glänzend braun, mit Salzkörnern
# =============================================================================

def pretzel() -> st.Spr:
    shape = _mask("""
................
................
....####..####..
...######.#####.
..###..#####.###
..##....###...##
..##...#####..##
..###.###.###.##
...######..####.
....####....##..
.....###...##...
......########..
.......######...
................
................
................
""")
    s = st.shaded(shape, "A", cuts=(-0.2, 0.25, 0.62, 0.9), radius=1.6)
    for x, y in ((5, 3), (10, 3), (3, 5), (13, 6), (7, 8), (12, 8), (9, 11), (6, 4), (14, 4)):
        if s.get(x, y):
            s.set(x, y, ("B", 6 if (x + y) % 2 else 5))
    return s


# =============================================================================
# Bratwurst: roh (zwei rosa Würste, abgedreht) und gebraten (Grillstreifen, ein Klecks Senf)
# =============================================================================

def raw_sausage() -> st.Spr:
    a = st.polygon([(1.5, 9.5), (3.0, 7.0), (6.5, 5.2), (8.2, 5.6), (8.0, 7.6), (5.4, 9.0), (3.4, 11.4), (1.8, 11.4)])
    b = st.polygon([(8.4, 7.2), (11.0, 5.0), (14.0, 3.2), (15.0, 4.4), (14.4, 6.2), (11.6, 8.6), (9.4, 9.8), (8.2, 9.2)])
    s = st.shaded(a | b, "A", cuts=(-0.15, 0.3, 0.68, 0.92), radius=2.0)
    # die abgedrehte Stelle in der Mitte (Kordel)
    s.set(8, 7, ("B", 3))
    s.set(8, 8, ("B", 2))
    s.set(7, 7, ("A", 2))
    # Fettaugen
    for x, y in ((4, 9), (11, 6), (13, 5)):
        if s.get(x, y):
            s.set(x, y, ("A", 6))
    return s


def sausage() -> st.Spr:
    body = st.polygon([(2.0, 11.6), (3.4, 9.2), (11.0, 3.4), (13.6, 2.6), (14.6, 4.2), (13.4, 6.4), (5.4, 12.6), (3.0, 13.4)])
    s = st.shaded(body, "A", cuts=(-0.15, 0.3, 0.68, 0.92), radius=2.2)
    # Grillstreifen quer
    for x0, y0 in ((4, 9), (7, 7), (10, 5)):
        for k in range(3):
            x, y = x0 + k, y0 + k
            if s.get(x, y):
                s.set(x, y, ("A", 1))
    # ein Klecks Senf
    for x, y, l in ((9, 8, 5), (10, 8, 6), (8, 9, 4), (9, 9, 5)):
        if s.get(x, y):
            s.set(x, y, ("B", l))
    return s


# =============================================================================
# Getränke in Flaschen: Rum (bauchige dunkle Flasche) und Feentee (runde Flasche mit Mondglöckchen)
# =============================================================================

def rum() -> st.Spr:
    s = st.Spr.art("""
................
......kkk.......
......jkj.......
.......2........
......232.......
.....23332......
....2333332.....
...255555542....
...2BBBBBB42....
...2BCCCCB42....
...2BCDDCB42....
...2BCCCCB42....
...2BBBBBB42....
...255555542....
....2444442.....
.....22222......
""", legend={"k": ("C", 5), "j": ("C", 4)})
    # Glanzlicht auf dem Glas, das Etikett mit einem Anker (Slot E: Etikett, D: Zeichnung)
    s.set(4, 7, ("A", 6))
    s.set(4, 12, ("A", 6))
    for x, y in ((6, 9), (6, 10), (7, 11), (8, 11), (9, 10), (9, 9)):
        s.set(x, y, ("D", 2))
    s.set(7, 9, ("D", 2))
    s.set(8, 9, ("D", 2))
    s.set(7, 10, ("E", 5))
    s.set(8, 10, ("E", 5))
    return s


def fae_tea() -> st.Spr:
    body = st.ellipse(8.0, 10.6, 5.2, 4.8)
    s = st.shaded(body, "A", cuts=(-0.1, 0.35, 0.7, 0.9), radius=3.0)
    # Hals und Korken
    s.draw("""
................
.......kk.......
.......jj.......
.......66.......
......5665......
""", legend={"k": ("C", 5), "j": ("C", 4)})
    # der Tee (rosa) füllt die untere Hälfte, mit Spiegel
    for x, y in body:
        if y >= 9:
            lvl = s.get(x, y)[1]
            s.set(x, y, ("B", st.clamp(lvl)))
    for x in range(4, 13):
        if (x, 9) in body:
            s.set(x, 9, ("B", 6))
    # ein Mondglöckchen schwimmt im Tee
    for x, y, l in ((7, 11, 5), (8, 11, 6), (9, 11, 5), (8, 12, 4), (7, 12, 3), (9, 12, 3)):
        s.set(x, y, ("E", l))
    # Glanz auf dem Glas
    s.set(5, 8, ("A", 6))
    s.set(5, 9, ("A", 6))
    return s


# =============================================================================
# Glutschote: die Schote (rot, glühend an der Spitze) und ihre Pflanze in vier Stufen
# =============================================================================

def ember_pepper() -> st.Spr:
    pod = st.polygon([(5.0, 4.6), (8.4, 4.0), (10.8, 5.6), (11.6, 8.4), (10.6, 11.4), (8.0, 13.6), (5.4, 14.6), (4.8, 13.6),
                      (6.8, 11.6), (7.6, 9.0), (6.8, 6.6)])
    s = st.shaded(pod, "A", cuts=(-0.2, 0.3, 0.66, 0.9), radius=2.4)
    # die Spitze glüht
    for x, y in ((5, 14), (6, 13), (5, 13)):
        if s.get(x, y):
            s.set(x, y, ("B", 4 if (x, y) == (5, 14) else 5))
    # Kelch und Stiel
    s.draw("""
................
.........cc.....
........c.......
......dccd......
.....ddeedd.....
""", legend={"c": ("C", 3), "d": ("C", 4), "e": ("C", 5)})
    return s


def pepper_stage(stage: int) -> Image.Image:
    """Die Pflanze der Glutschote (Kreuzmodell): Stufe 0 Keimlinge, 1 Busch, 2 grüne Schoten, 3 reife, glühende Schoten."""
    arts = {
        0: """
................
................
................
................
................
................
................
................
................
................
.......d........
....d..cd..d....
.....c.c..cd....
.....cdc.dc.....
......ccc.c.....
.......c........
""",
        1: """
................
................
................
................
................
................
.......d........
....dd.cd.dd....
.....dcc.cdd....
..dd..cc.c...d..
...ddc.cdc..dd..
....cc.cc.ccd...
.....cdcc.c.....
......ccc.c.....
.......cc.......
.......c........
""",
        2: """
................
................
................
.......d........
...dd..cd.dd....
....dccc.cdd....
.dd..cc.c...dd..
..ddc.cddcc.d...
...c.ac.cc.cc...
...cbac.ca.a....
....bb.cba.b....
.....cdcbbcb....
......ccc.c.....
.......cc.......
.......cc.......
.......c........
""",
        3: """
................
.......d........
...dd..cd.dd....
....dccc.cdd....
.dd..cc.c...dd..
..ddc.cdac..d...
..ac.cc.aac.cc..
..aac.c.bacca...
..bac.cc.b.cab..
...b..cca..cbb..
...a.cdcba.b....
..ba..ccbb.ca...
..b...ccc.cbb...
.......cc..b....
.......cc.......
.......c........
""",
    }
    s = st.Spr.art(arts[stage], legend={"a": ("A", 5), "b": ("A", 3), "c": ("C", 3), "d": ("E", 4)})
    # die Blätter heller oben, dunkler unten
    s = s.map(lambda x, y, p: ("E", 5 if y < 6 else 4 if y < 11 else 3) if p[0] == "E" else p)
    if stage == 3:
        # die Spitzen glühen
        for x, y, p in list(s.cells()):
            if p[0] == "A" and s.get(x, y + 1) is None:
                s.set(x, y, ("B", 5))
    if stage == 2:
        s = s.map(lambda x, y, p: ("D", p[1]) if p[0] == "A" else p)
    return s.image({"A": PEPPER, "B": EMBER, "C": NETHER_STEM, "D": PEPPER_STEM, "E": LEAF_DARK})


# =============================================================================
# Glutchili (Schüssel), Pilzspieß
# =============================================================================

def ember_chili() -> st.Spr:
    def fill(s: st.Spr) -> None:
        s.draw("""
................
................
................
................
................
................
................
....3434343.....
..343454543433..
.34345454543433.
""")
        # Bohnen und Fleisch (dunkler), Schoten obenauf, Dampf
        for x, y in ((5, 8), (9, 7), (12, 8), (3, 9), (8, 9)):
            s.set(x, y, ("A", 2))
        for x, y, l in ((6, 7, 5), (7, 7, 5), (7, 6, 4), (10, 8, 5), (11, 8, 4)):
            s.set(x, y, ("B", l))
        s.set(7, 5, ("C", 4))
        for x, y in ((5, 4), (6, 3), (10, 4), (9, 2)):
            s.set(x, y, ("E", 5))
    return bowl(fill)


def fungus_skewer() -> st.Spr:
    s = st.Spr()
    for x, y in st.line(2, 14, 13, 3):
        s.set(x, y, ("C", 4))
    for x, y in st.line(3, 14, 14, 3):
        s.set(x, y, ("C", 3))
    # drei Pilzhüte: Karmesin, Wirr, Karmesin (rund, geröstet)
    for cx, cy, slot in ((5.0, 11.4, "A"), (8.4, 8.0, "B"), (11.8, 4.6, "A")):
        cap = st.ellipse(cx, cy, 2.3, 2.3)
        sh = st.shaded(cap, slot, cuts=(-0.1, 0.35, 0.7, 0.92), radius=1.6)
        s.over(sh)
    # Röststellen
    for x, y in ((5, 12), (12, 5), (8, 9)):
        p = s.get(x, y)
        if p:
            s.set(x, y, (p[0], 1))
    return s


# =============================================================================
# Schiffszwieback (viereckig mit Stichlöchern), Chorusmarmelade (Glas mit Tuch)
# =============================================================================

def hardtack() -> st.Spr:
    s = st.Spr.art("""
................
................
................
..5666666666665.
..6555555555554.
..6555555555554.
..6555555555554.
..6555555555554.
..6555555555554.
..6555555555554.
..6555555555554.
..6555555555554.
..6555555555554.
..5444444444443.
................
................
""")
    for x in (4, 7, 10, 13):
        for y in (5, 8, 11):
            if x < 14:
                s.set(x, y, ("A", 2))
    # Bräunung an den Kanten
    for x, y in ((3, 4), (13, 12), (12, 13), (3, 12)):
        s.set(x, y, ("A", 3))
    return s


def chorus_jam() -> st.Spr:
    s = st.Spr.art("""
................
................
....hijkkjih....
...hjkllllkjh...
....aaaaaaaa....
...5BBBBBBBB4...
...5CDDDDDDC4...
...5CDEEEEDC4...
...5CDEEEEDC4...
...5CDDEEDDC4...
...5CCDDDDCC4...
...5CCCCCCCC4...
....33333333....
................
................
................
""", legend={"h": ("C", 3), "i": ("C", 4), "j": ("C", 5), "k": ("C", 6), "l": ("C", 6), "a": ("D", 3),
            "B": ("B", 5), "C": ("B", 3), "D": ("B", 4), "E": ("B", 5)})
    # Glanz auf dem Glas
    s.set(4, 6, ("A", 6))
    s.set(4, 7, ("A", 6))
    # Chorusstückchen in der Marmelade
    for x, y in ((6, 8), (9, 9), (8, 7)):
        s.set(x, y, ("B", 6))
    return s


# =============================================================================
# Dämmerbrei (Schüssel), Seelenbeerkuchen
# =============================================================================

def dusk_porridge() -> st.Spr:
    def fill(s: st.Spr) -> None:
        s.draw("""
................
................
................
................
................
................
................
.....556655.....
..455665566554..
.45566556655654.
""")
        for x, y in ((6, 7), (9, 8), (4, 9), (12, 9)):
            s.set(x, y, ("B", 5))
        for x, y in ((7, 7), (10, 8), (5, 9)):
            s.set(x, y, ("B", 4))
        s.set(11, 8, ("C", 4))
    return bowl(fill)


def soul_berry_pie() -> st.Spr:
    """Ein runder Kuchen aus Dämmerkorn: oben ein Teiggitter, in den Lücken leuchten Seelenbeeren, der Rand gewellt."""
    cx, cy, rx, ry, depth = 8.0, 7.0, 7.0, 3.8, 3

    def top(x: float, y: float) -> bool:
        return ((x - cx) / rx) ** 2 + ((y - cy) / ry) ** 2 <= 1.0

    s = st.Spr()
    for x in range(16):
        for y in range(16):
            xc, yc = x + 0.5, y + 0.5
            if top(xc, yc):
                rim = not all(top(xc + dx, yc + dy) for dx, dy in ((1.2, 0), (-1.2, 0), (0, 1.0), (0, -1.0)))
                if rim:
                    s.set(x, y, ("A", 6 if yc < cy and xc < cx else 5 if yc < cy else 4))
                else:
                    strip = (x + y) % 3 == 0 or (x - y) % 3 == 0
                    s.set(x, y, ("A", 6 if xc + yc < 14 else 5) if strip else ("B", 6 if (x + y) % 2 else 5))
            else:
                for t in range(1, depth + 1):
                    if top(xc, yc - t) and yc - t > cy:
                        s.set(x, y, ("A", (3 if x % 2 else 4) if t == 1 else 3 if x < 11 else 2))
                        break
    return s


# =============================================================================
# Sternenkandis: ein Stern aus Zuckerkristall mit goldener Mitte und Funkeln
# =============================================================================

def star_candy() -> st.Spr:
    pts = []
    for k in range(10):
        a = -math.pi / 2 + k * math.pi / 5
        r = 6.6 if k % 2 == 0 else 3.0
        pts.append((8.0 + math.cos(a) * r, 8.6 + math.sin(a) * r))
    star = st.polygon(pts)
    s = st.shaded(star, "A", cuts=(-0.3, 0.2, 0.6, 0.88), radius=2.0)
    for x, y, l in ((7, 8, 5), (8, 8, 6), (8, 9, 5), (7, 9, 4)):
        s.set(x, y, ("B", l))
    # Funkeln
    for x, y in ((2, 2), (13, 13)):
        s.set(x, y, ("A", 6))
    return s


# =============================================================================
# Sturzkuchen: runder Kuchen, obenauf glänzender Karamell mit Apfelringen
# =============================================================================

def upside_down_cake() -> st.Spr:
    """Ein runder Kuchen: obenauf glänzender Karamell mit Apfelringen, an der Seite der helle Teig, Karamell läuft herab."""
    cx, cy, rx, ry, depth = 8.0, 6.0, 7.0, 3.6, 5

    def top(x: float, y: float) -> bool:
        return ((x - cx) / rx) ** 2 + ((y - cy) / ry) ** 2 <= 1.0

    s = st.Spr()
    for x in range(16):
        for y in range(16):
            xc, yc = x + 0.5, y + 0.5
            if top(xc, yc):
                s.set(x, y, ("A", 5 if (xc - cx) + (yc - cy) * 1.6 < 1.0 else 4))
            else:
                for t in range(1, depth + 1):
                    if top(xc, yc - t) and yc - t > cy:
                        drip = t <= 1 or (t == 2 and x % 3 == 1)
                        s.set(x, y, ("A", 3) if drip else ("B", 5 if x < 6 else 4 if x < 11 else 3))
                        break
    # Apfelringe: helle Ringe mit dunkler Mitte
    for ax, ay in ((5, 5), (9, 4), (11, 7), (7, 8), (3, 7)):
        for dx, dy in ((-1, 0), (1, 0), (0, -1), (0, 1), (-1, -1), (1, -1), (-1, 1), (1, 1)):
            q = s.get(ax + dx, ay + dy)
            if q and q[0] == "A":
                s.set(ax + dx, ay + dy, ("C", 6 if dy < 0 or dx < 0 else 5))
        s.set(ax, ay, ("A", 3))
    s.set(6, 3, ("A", 6))
    s.set(7, 3, ("A", 6))
    return s


# =============================================================================
# Pilgerration: ein Bündel in Tang gewickelt, mit Kordel gebunden, ein Stück Dörrfleisch schaut heraus
# =============================================================================

def pilgrim_ration() -> st.Spr:
    body = st.polygon([(2.4, 8.0), (6.0, 4.4), (10.0, 4.0), (13.6, 6.6), (13.8, 10.4), (10.6, 13.4), (5.4, 13.6), (2.4, 11.6)])
    s = st.shaded(body, "A", cuts=(-0.1, 0.3, 0.68, 0.92), radius=2.6, grain=0.12, seed=11)
    # Kordel kreuzweise
    for x, y in st.line(3, 9, 13, 9):
        if s.get(x, y):
            s.set(x, y, ("B", 4 if x < 8 else 3))
    for x, y in st.line(8, 4, 8, 13):
        if s.get(x, y):
            s.set(x, y, ("B", 4 if y < 9 else 3))
    s.set(8, 9, ("B", 5))
    # Dörrfleisch oben rechts
    for x, y, l in ((11, 3, 4), (12, 3, 3), (12, 4, 4), (13, 4, 3), (13, 5, 2), (11, 4, 3)):
        s.set(x, y, ("C", l))
    return s


# =============================================================================
# Fischsuppe (Schüssel), Fischbrötchen
# =============================================================================

def fish_soup() -> st.Spr:
    def fill(s: st.Spr) -> None:
        s.draw("""
................
................
................
................
................
................
................
.....5565565....
..455565556554..
.45565556555654.
""")
        # Fischstücke (hell), Kartoffel, Kräuter; eine Schwanzflosse ragt heraus
        for x, y in ((5, 8), (9, 9), (11, 8)):
            s.set(x, y, ("B", 6))
        for x, y in ((7, 9), (3, 9)):
            s.set(x, y, ("C", 5))
        for x, y in ((6, 7), (10, 8), (12, 9), (4, 8)):
            s.set(x, y, ("E", 4))
        for x, y, l in ((11, 6, 4), (12, 5, 5), (13, 5, 4), (12, 6, 3), (13, 4, 5)):
            s.set(x, y, ("F", l))
    return bowl(fill)


def fish_roll() -> st.Spr:
    s = st.Spr.art("""
................
................
................
................
....555555555...
..5566666666554.
.566666666666654
.CCCCDDCCCDDCC..
.EEEEEEEEEEEEEE.
.BBBBBBBBBBBBBBB
.555555555555554
.444444444444443
..3333333333333.
................
................
................
""", legend={"B": ("B", 5), "C": ("C", 4), "D": ("C", 5), "E": ("D", 5)})
    # Sesam auf dem Brötchen, Fischhaut-Streifen
    for x, y in ((4, 5), (8, 4), (11, 5), (6, 6), (13, 6)):
        s.set(x, y, ("A", 6) if s.get(x, y) else None)
    for x in (3, 7, 11):
        s.set(x, 9, ("B", 3))
    return s


# =============================================================================
# Lebkuchenherz mit Zuckerguss und Band
# =============================================================================

def gingerbread() -> st.Spr:
    heart = set()
    for y in range(16):
        for x in range(16):
            px, py = (x + 0.5 - 8.0) / 6.4, (y + 0.5 - 7.6) / 6.4
            # Herzkurve
            v = (px * px + py * py - 0.6) ** 3 - px * px * (py * -1) ** 3 * 1.0
            if v <= 0:
                heart.add((x, y))
    s = st.shaded(heart, "A", cuts=(-0.2, 0.3, 0.68, 0.92), radius=2.4)
    # Zuckerguss: ein Rand eine Reihe innen, Tupfen in der Mitte
    inner = {(x, y) for x, y in heart if all((x + dx, y + dy) in heart for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)))}
    ring = {(x, y) for x, y in inner if any((x + dx, y + dy) not in inner for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)))}
    for x, y in ring:
        if (x + y) % 2 == 0:
            s.set(x, y, ("B", 5 if x < 8 else 4))
    for x, y in ((6, 8), (8, 9), (10, 8), (8, 7)):
        if s.get(x, y):
            s.set(x, y, ("B", 6))
    # das Band oben
    for x, y, l in ((7, 1, 4), (8, 1, 4), (6, 0, 5), (9, 0, 5), (7, 2, 3), (8, 2, 3)):
        s.set(x, y, ("C", l))
    return s


# =============================================================================
# Glimmerküchlein: rosa Haube, Funkeln, ein blaues Papierförmchen
# =============================================================================

def glimmer_cupcake() -> st.Spr:
    s = st.Spr.art("""
................
................
.......6........
......565.......
....5566655.....
...566666655....
..56666666665...
..55666666654...
..445555555443..
...hijkjkjkji...
...hijkjkjkji...
...hijkjkjkji...
....hjkjkjkjh...
....hiiiiiiih...
................
................
""", legend={"h": ("C", 2), "i": ("C", 3), "j": ("C", 4), "k": ("C", 5)})
    for x, y in ((5, 5), (8, 4), (10, 6), (6, 7), (9, 7), (4, 6)):
        if s.get(x, y):
            s.set(x, y, ("B", 6 if (x + y) % 2 else 5))
    # eine Beere obenauf
    s.set(7, 2, ("D", 5))
    s.set(7, 1, ("D", 6))
    s.set(6, 2, ("D", 4))
    return s


# =============================================================================
# Die Flächen des Käselaib-Blocks: Deckel (16x16, das Achteck mit Rindenrand), Rinde, Schnittfläche
# =============================================================================

WHEEL_BOXES = [(1, 4, 2, 12), (2, 2, 4, 14), (4, 1, 12, 15), (12, 2, 14, 14), (14, 4, 15, 12)]


def _wheel_footprint() -> set[tuple[int, int]]:
    out = set()
    for x1, z1, x2, z2 in WHEEL_BOXES:
        for x in range(x1, x2):
            for z in range(z1, z2):
                out.add((x, z))
    return out


def wheel_top() -> Image.Image:
    fp = _wheel_footprint()
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    px = img.load()
    for x in range(16):
        for y in range(16):
            n = st.smooth_noise(41, x, y, 3.0)
            px[x, y] = CHEESE_PALE[4 if n < 0.3 else 5]
    for (x, y) in fp:
        edge = any((x + dx, y + dy) not in fp for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)))
        if edge:
            px[x, y] = RIND[4 if (x + y) % 3 else 3]
    # eine eingeprägte Marke in der Mitte (Kreis)
    for x, y in ((7, 6), (8, 6), (6, 7), (9, 7), (6, 8), (9, 8), (7, 9), (8, 9)):
        px[x, y] = CHEESE_PALE[3]
    return img


def wheel_side() -> Image.Image:
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    px = img.load()
    for x in range(16):
        for y in range(16):
            n = st.smooth_noise(77, x, y, 2.5)
            lvl = 4 + (1 if n > 0.35 else 0) - (1 if n < -0.4 else 0)
            if y in (9, 10):
                lvl = 5            # die gewölbte Oberkante
            if y == 15:
                lvl = 2
            if y == 14:
                lvl = 3
            px[x, y] = RIND[st.clamp(lvl)]
    return img


def wheel_inner() -> Image.Image:
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    px = img.load()
    for x in range(16):
        for y in range(16):
            px[x, y] = CHEESE[5 if (x * 3 + y) % 7 else 4]
    for x, y in ((2, 11), (3, 11), (2, 12), (8, 12), (12, 10), (13, 10), (6, 14), (11, 14), (14, 13)):
        px[x, y] = CHEESE[3]
    for y in range(16):
        px[0, y] = RIND[4]
        px[15, y] = RIND[3]
    for x in range(16):
        px[x, 9] = RIND[5]
        px[x, 15] = RIND[2]
    return img


# =============================================================================
# Ausgabe
# =============================================================================

def build(put: Callable[[str, Image.Image], None]) -> None:
    put("item/flour", render(flour(), {"A": LINEN, "B": TWINE, "C": FLOUR}, prefer=("A", "B", "C")))
    put("item/cheese", render(cheese(), {"A": CHEESE, "B": RIND, "C": CHEESE_PALE}, prefer=("A", "B")))
    put("item/cheese_wheel", render(cheese_wheel_item(), {"A": CHEESE_PALE, "B": RIND, "C": CHEESE}, prefer=("B", "A", "C")))
    put("item/cheese_spaetzle", render(spaetzle(), {"A": SPAETZLE, "B": CHEESE_PALE, "C": ONION_FRIED, "D": BOWL, "E": HERB},
                                       prefer=("D", "A")))
    put("item/pretzel", render(pretzel(), {"A": PRETZEL, "B": SALT}))
    put("item/raw_sausage", render(raw_sausage(), {"A": RAW_MEAT, "B": TWINE}))
    put("item/sausage", render(sausage(), {"A": GRILLED, "B": MUSTARD}, prefer=("A",)))
    put("item/rum", render(rum(), {"A": GLASS_RUM, "B": RUM, "C": CORK, "D": GLASS_RUM, "E": LABEL}, prefer=("A",)))
    put("item/ember_pepper", render(ember_pepper(), {"A": PEPPER, "B": EMBER, "C": PEPPER_STEM}, prefer=("A", "C")))
    put("item/ember_chili", render(ember_chili(), {"A": CHILI, "B": PEPPER, "C": PEPPER_STEM, "D": BOWL, "E": FLOUR},
                                   prefer=("D", "A")))
    put("item/fungus_skewer", render(fungus_skewer(), {"A": CRIMSON, "B": WARPED, "C": STICK}, prefer=("A", "B")))
    put("item/hardtack", render(hardtack(), {"A": HARDTACK}))
    put("item/chorus_jam", render(chorus_jam(), {"A": JAR, "B": CHORUS, "C": CLOTH, "D": TWINE}, prefer=("A", "C")))
    put("item/dusk_porridge", render(dusk_porridge(), {"A": DUSK_PORRIDGE, "B": SOUL, "C": DUSK_CRUST, "D": BOWL}, prefer=("D", "A")))
    put("item/soul_berry_pie", render(soul_berry_pie(), {"A": DUSK_CRUST, "B": SOUL}, prefer=("A",)))
    put("item/star_candy", render(star_candy(), {"A": STAR, "B": STAR_GOLD}, prefer=("A",)))
    put("item/upside_down_cake", render(upside_down_cake(), {"A": CARAMEL, "B": CAKE, "C": APPLE_SLICE}, prefer=("B", "A")))
    put("item/pilgrim_ration", render(pilgrim_ration(), {"A": KELP, "B": TWINE, "C": JERKY}, prefer=("A",)))
    put("item/fish_soup", render(fish_soup(), {"A": BROTH, "B": FISH, "C": LINEN, "D": BOWL, "E": HERB, "F": FISH_SKIN},
                                 prefer=("D", "A")))
    put("item/fish_roll", render(fish_roll(), {"A": CRUST, "B": FISH, "C": LETTUCE, "D": FISH_SKIN}, prefer=("A",)))
    put("item/gingerbread", render(gingerbread(), {"A": GINGER, "B": ICING, "C": RIBBON}, prefer=("A",)))
    put("item/fae_tea", render(fae_tea(), {"A": FAE_GLASS, "B": TEA, "C": CORK, "E": MOONBELL}, prefer=("A",)))
    put("item/glimmer_cupcake", render(glimmer_cupcake(), {"A": FROSTING, "B": SPARK, "C": CUP, "D": PEPPER}, prefer=("A", "C")))
    for stage in range(4):
        put(f"block/ember_peppers_stage{stage}", pepper_stage(stage))
    put("block/cheese_wheel_top", wheel_top())
    put("block/cheese_wheel_side", wheel_side())
    put("block/cheese_wheel_inner", wheel_inner())


if __name__ == "__main__":
    import sys

    sheet: dict[str, Image.Image] = {}
    build(lambda key, img: sheet.__setitem__(key.split("/")[-1], img))
    st.preview(sheet, sys.argv[1] if len(sys.argv) > 1 else "items_cuisine.png", zoom=8, cols=7)
