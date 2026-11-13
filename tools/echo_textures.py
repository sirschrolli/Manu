#!/usr/bin/env python3
"""
Hallkristall (seit 1.6): der Kristall der Tiefen Dunkelheit, sein Block und die Symbole der Rüstung der Lauscher.

  * Hallkristall – ein Büschel aus drei sechseckigen Prismen (ein hohes in der Mitte, zwei kleine zu den Seiten), blass wie
    Eis, die linke Schlifffläche im Licht, die rechte im Schatten; quer durch das hohe Prisma laufen helle Ringe wie Wellen
    auf einem Teich, oben links ein weißer Glanz.
  * Hallkristallblock – eine runde, geschliffene Kristallscheibe mit Ringen darin, gefasst in einen Rahmen aus Tiefenschiefer
    (vier kleine Kristalle in den Ecken). In der Leuchtebene läuft eine Welle von der Mitte nach außen, eine nach der anderen.
  * Die Rüstung der Lauscher: Platten aus Tiefenschiefer, Fühler aus Sculk, die türkis leuchten, Hallkristall an Ohren, Brust
    und Knien, ein pochendes Sculkherz auf der Brust, Stiefel mit dicken Sohlen aus grauer Wolle.

Wird von tools/generate_textures.py aufgerufen: build(put); Bildfolgen stehen in ANIMATIONS.
"""
from __future__ import annotations

import math
from typing import Callable

from PIL import Image

import item_style as st
import items_armor as icons
from phase_textures import _prism

N = 16
CLEAR = (0, 0, 0, 0)
ANIMATIONS: dict[str, tuple[int, int, bool]] = {}

#: der Kristall: blaugrau im Schatten, eisblau in der Mitte, fast weiß an den Spitzen
ECHO = st.ramp("#86c2e8", dark=0.3, light=0.98, cool=255, hue_dark=16, chroma_dark=1.0, warm=200, hue_light=6, chroma_light=0.35)
#: der Stein selbst (als Gegenstand) etwas heller
GEM = st.ramp("#94ccf0", dark=0.36, light=0.99, cool=255, hue_dark=16, chroma_dark=1.0, warm=200, hue_light=6, chroma_light=0.35)
#: das Leuchten des Sculk (Fühler, Herz, Adern) und der Wellen
WAVE = st.ramp("#2cc6da", dark=0.34, light=0.98, cool=230, hue_dark=12, chroma_dark=1.0, warm=180, hue_light=6, chroma_light=0.35)
#: Glanzlicht
SHINE = st.fixed("#dff4ff", "#e8f8ff", "#f2fbff", "#ffffff", "#ffffff", "#ffffff", "#ffffff")
#: Tiefenschiefer (die Platten der Rüstung, der Rahmen des Blocks) – dunkelgrau mit einem Hauch Blau
DEEPSLATE = st.ramp("#4c4c58", dark=0.14, light=0.74, cool=260, hue_dark=10, chroma_dark=1.0, warm=230, hue_light=4, chroma_light=0.3)
#: graue Wolle (die Sohlen)
WOOL = st.ramp("#8e8e96", dark=0.3, light=0.92, cool=260, hue_dark=6, warm=80, hue_light=4, chroma_light=0.3)

BLOCK_FRAMES = 16


# =============================================================================
# Der Kristall
# =============================================================================

def echo_gem_art() -> Image.Image:
    """Der Hallkristall: ein hohes Prisma in der Mitte, zwei kleine zu den Seiten; helle Ringe quer durch das hohe."""
    s = st.Spr()
    _prism(s, (4.6, 15.2), -34.0, 7.0, 1.5, 2.4, bright=-1)
    _prism(s, (11.0, 15.2), 36.0, 6.2, 1.4, 2.2, bright=-1)
    big = _prism(s, (7.6, 15.4), 8.0, 15.0, 2.7, 5.0)
    for x, y, t, u in big:
        # der Grat zwischen linker Fläche und Mitte fängt das Licht
        if -1.2 <= u <= -0.45 and 1.5 < t < 15.0 - 5.0:
            s.set(x, y, ("P", 6 if t > 5.0 else 5))
        # die Ringe: zwei helle Wellen quer durch den Kristall
        for ring in (4.6, 8.4):
            if abs(t - ring) < 0.55 and abs(u) < 2.4:
                s.set(x, y, ("P", 6 if u < 0.6 else 5))
    for x, y, lv in ((7, 2, 6), (7, 3, 3)):
        s.set(x, y, ("W", lv))
    s = s.outline(prefer=("P",), override={"W": "P"}, light=1, dark=0)
    return s.image({"P": GEM, "W": SHINE})


# =============================================================================
# Der Block: eine Kristallscheibe in Tiefenschiefer
# =============================================================================

CX, CY, R = 7.5, 7.5, 5.6


def _disc_level(x: int, y: int) -> int | None:
    """Die Kristallscheibe: Ringe (abwechselnd hell und dunkel), oben links im Licht. None außerhalb."""
    d = math.hypot(x + 0.5 - (CX + 0.5), y + 0.5 - (CY + 0.5))
    if d > R:
        return None
    ring = int(d / 1.4)
    light = -0.5 * ((x - CX) + (y - CY)) / R                 # oben links hell
    base = 4 if ring % 2 == 0 else 2
    lvl = base + (1 if light > 0.25 else 0) - (1 if light < -0.35 else 0)
    if d < 1.0:
        lvl = 6
    return st.clamp(lvl, 1, 6)


def echo_block() -> tuple[Image.Image, Image.Image]:
    """Der Block und seine Leuchtebene (Bildfolge: eine Welle läuft von der Mitte nach außen)."""
    s = st.Spr()
    for y in range(N):
        for x in range(N):
            lvl = _disc_level(x, y)
            if lvl is not None:
                s.set(x, y, ("E", lvl))
                continue
            # der Rahmen: Tiefenschiefer in Platten, außen eine Fase (oben/links im Licht), in den Ecken je ein Kristall
            outer = x in (0, N - 1) or y in (0, N - 1)
            if outer:
                tone = 4 if (x == 0 or y == 0) and not (x == N - 1 or y == N - 1) else 1
            else:
                tone = 2 + ((x // 4 + y // 4) % 2)
                if x in (1,) or y in (1,):
                    tone = 3
            s.set(x, y, ("A", tone))
    for cx, cy in ((1, 1), (14, 1), (1, 14), (14, 14)):
        s.set(cx, cy, ("E", 6 if (cx, cy) == (1, 1) else 5))
    for x, y in ((5, 4), (4, 5)):
        s.set(x, y, ("W", 2))                                # Glanz
    img = s.image({"E": ECHO, "A": DEEPSLATE, "W": SHINE})

    frames = Image.new("RGBA", (N, N * BLOCK_FRAMES), CLEAR)
    for fr in range(BLOCK_FRAMES):
        wave = fr / BLOCK_FRAMES * (R + 1.5)                # der Halbmesser der Welle
        for y in range(N):
            for x in range(N):
                lvl = _disc_level(x, y)
                d = math.hypot(x + 0.5 - (CX + 0.5), y + 0.5 - (CY + 0.5))
                a = 0.0
                if lvl is not None:
                    a = 0.18 + 0.1 * (lvl - 2)                   # der ruhige Schimmer der Ringe
                    k = 1.0 - abs(d - wave) / 1.3                # die Welle
                    if k > 0.0:
                        a = max(a, k * (1.0 - wave / (R + 2.0)) * 1.1)
                elif (x, y) in ((1, 1), (14, 1), (1, 14), (14, 14)):
                    a = 0.45 if fr < 3 or fr > 13 else 0.25      # die Eckkristalle glimmen, wenn die Welle sie erreicht
                if a <= 0.04:
                    continue
                col = WAVE[6] if a > 0.75 else (WAVE[5] if a > 0.4 else ECHO[5])
                frames.putpixel((x, fr * N + y), (col[0], col[1], col[2], int(40 + 200 * min(1.0, a))))
    return img, frames


# =============================================================================
# Die Rüstung der Lauscher
# =============================================================================

def listener_icons(put: Callable[[str, Image.Image], None]) -> None:
    ramps = {"A": DEEPSLATE, "B": WOOL, "D": WAVE, "E": ECHO, "C": SHINE}
    override = {"D": "A", "C": "A", "E": "A"}

    # --- der Helm: geschlossen, ein schmaler Sehschlitz mit türkisem Schimmer, an den Ohren Scheiben aus Hallkristall, oben
    # zwei Fühler aus Sculk mit leuchtenden Spitzen
    s = icons.base(icons.CLOSED_HELMET)
    for x in range(5, 11):
        s.set(x, 7, ("D", 2 if x in (5, 10) else 3))           # der Sehschlitz
    for x, y, lv in ((3, 7, 5), (4, 7, 6), (3, 8, 3), (4, 8, 4), (11, 7, 4), (12, 7, 5), (11, 8, 2), (12, 8, 3)):
        s.set(x, y, ("E", lv))                                  # die Ohrscheiben
    for x, y, lv in ((5, 2, 2), (4, 1, 4), (4, 0, 6), (10, 2, 2), (11, 1, 4), (11, 0, 6), (7, 2, 3), (8, 2, 2)):
        s.set(x, y, ("D", lv))                                  # die Fühler (zwei lange außen, zwei kurze innen)
    s.set(7, 4, ("E", 6))
    s.set(8, 4, ("E", 4))                                       # ein Kristall über der Stirn
    put("item/listener_helmet", s.outline(prefer=("A", "E"), override=override).image(ramps))

    # --- der Harnisch: ein pochendes Sculkherz auf der Brust, ein Ring aus Hallkristall darum, Sculkadern, die davon
    # ausgehen; die Schultern dunkler
    s = icons.base(icons.CHESTPLATE)
    for x in range(2, 14):
        for y in (2, 3):
            if s.opaque(x, y) and (x <= 5 or x >= 10):
                s.set(x, y, ("A", 5 if y == 2 else 4))
    for x, y in ((7, 5), (8, 5), (6, 6), (9, 6), (6, 7), (9, 7), (7, 8), (8, 8)):
        s.set(x, y, ("E", 5 if y <= 6 else 3))                  # der Ring
    for x, y, lv in ((7, 6, 6), (8, 6, 5), (7, 7, 5), (8, 7, 4)):
        s.set(x, y, ("D", lv))                                  # das Herz
    for x, y, lv in ((5, 9, 3), (4, 10, 2), (10, 9, 3), (11, 10, 2), (7, 10, 3), (8, 11, 2), (6, 12, 2)):
        if s.opaque(x, y):
            s.set(x, y, ("D", lv))                              # die Adern
    put("item/listener_chestplate", s.outline(prefer=("A", "E"), override=override).image(ramps))

    # --- die Beinschienen: ein Gürtel mit Kristallschnalle, Hallkristall an den Knien, eine Sculkader außen
    s = icons.base(icons.LEGGINGS)
    for x in range(2, 14):
        s.set(x, 2, ("A", 5 if x < 8 else 4))
        s.set(x, 3, ("A", 3))
    s.set(7, 2, ("E", 6))
    s.set(8, 2, ("E", 4))
    s.set(7, 3, ("E", 4))
    s.set(8, 3, ("E", 2))
    for x0 in (2, 10):
        s.set(x0 + 1, 8, ("E", 6))
        s.set(x0 + 2, 8, ("E", 4))
        s.set(x0 + 1, 9, ("E", 4))
        s.set(x0 + 2, 9, ("E", 2))
    for y in range(9, 13):
        s.set(2, y, ("D", 3 if y % 2 else 2))
        s.set(13, y, ("D", 2 if y % 2 else 1))
    put("item/listener_leggings", s.outline(prefer=("A", "E"), override=override).image(ramps))

    # --- die Stiefel: dicke Sohlen aus grauer Wolle, Hallkristall an den Knöcheln
    s = icons.base(icons.BOOTS)
    for x in list(range(0, 7)) + list(range(9, 16)):
        for y, lv in ((10, 6), (11, 4)):
            if s.opaque(x, y):
                s.set(x, y, ("B", lv if (x + y) % 3 else lv - 1))   # die Wollsohlen
    for x0 in (4, 11):
        s.set(x0, 6, ("E", 6))
        s.set(x0 - 1, 6, ("E", 4))
    put("item/listener_boots", s.outline(prefer=("A", "B"), override=override).image(ramps))


# =============================================================================
# Ausgabe
# =============================================================================

def build(put: Callable[[str, Image.Image], None]) -> None:
    block, glow = echo_block()
    put("block/echo_crystal_block", block)
    put("block/echo_crystal_block_glow", glow)
    ANIMATIONS["block/echo_crystal_block_glow"] = (BLOCK_FRAMES, 2, True)
    put("item/echo_crystal", echo_gem_art())
    listener_icons(put)


if __name__ == "__main__":
    import sys

    out: dict[str, Image.Image] = {}
    build(lambda k, i: out.__setitem__(k, i))
    tiles = []
    plain = Image.new("RGBA", (N * 2, N * 2))
    for dx in (0, N):
        for dy in (0, N):
            plain.alpha_composite(out["block/echo_crystal_block"], (dx, dy))
    tiles.append(plain)
    for f in (0, 4, 8, 12):
        fr = out["block/echo_crystal_block_glow"].crop((0, f * N, N, (f + 1) * N))
        big = plain.copy()
        for dx in (0, N):
            for dy in (0, N):
                big.alpha_composite(fr, (dx, dy))
        tiles.append(big)
    for key in sorted(k for k in out if k.startswith("item/")):
        t = Image.new("RGBA", (N * 2, N * 2), (0, 0, 0, 0))
        t.alpha_composite(out[key].resize((N * 2, N * 2), Image.NEAREST))
        tiles.append(t)
    z = 5
    sheet = Image.new("RGBA", (len(tiles) * (N * 2 * z + 10), N * 2 * z), (52, 50, 62, 255))
    for i, t in enumerate(tiles):
        sheet.alpha_composite(t.resize((N * 2 * z, N * 2 * z), Image.NEAREST), (i * (N * 2 * z + 10), 0))
    sheet.save(sys.argv[1] if len(sys.argv) > 1 else "echo.png")
