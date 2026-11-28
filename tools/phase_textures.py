#!/usr/bin/env python3
"""
Phasit (seit 1.4): der Edelstein der äußeren Endinseln, sein Block und die Symbole der Rüstung der Leerwandler.

  * Phasit – ein Büschel aus zwei sechseckigen Kristallprismen (ein großes, leicht nach links geneigt, daneben ein kleines,
    das nach rechts abknickt): türkis wie der Blick der Leerwandler, die linke Schlifffläche im Licht, die rechte im
    Schatten, an der Spitze heller; im großen Prisma wandert ein violetter Funke, oben links ein weißer Glanz.
  * Phasitblock – vier geschliffene Phasitsteine, in Purpur gefasst (ein Rahmen mit Nieten, dazwischen ein Kreuz aus
    Purpur). In der Leuchtebene springt ein Licht von Stein zu Stein, ein violetter Funke folgt ihm.
  * Die Rüstung der Leerwandler: schwarzer Leerenstahl mit Säumen aus Purpur, türkis leuchtende Nähte und Augenschlitze,
    Phasit an Stirn, Brust, Gürtel und Knien, die Stiefel mit leuchtenden, schwebenden Sohlen.

Wird von tools/generate_textures.py aufgerufen: build(put); Bildfolgen stehen in ANIMATIONS.
"""
from __future__ import annotations

import math
from typing import Callable

from PIL import Image

import item_style as st
import items_armor as icons

N = 16
CLEAR = (0, 0, 0, 0)
ANIMATIONS: dict[str, tuple[int, int, bool]] = {}

#: der Kristall: tief petrol im Schatten, türkis in der Mitte, fast weiß an den Spitzen
PHASIT = st.ramp("#22c2aa", dark=0.26, light=0.97, cool=255, hue_dark=24, chroma_dark=1.05, warm=165, hue_light=8,
                 chroma_light=0.45)
#: der Stein selbst (als Gegenstand) etwas heller, damit er auch im fahlen Licht des Ends türkis leuchtet
GEM = st.ramp("#2ad2b8", dark=0.34, light=0.98, cool=255, hue_dark=24, chroma_dark=1.05, warm=165, hue_light=8,
              chroma_light=0.45)
#: der violette Funke, der durch den Stein wandert
SPARK = st.ramp("#a24ee8", dark=0.36, light=0.97, cool=280, hue_dark=10, warm=320, hue_light=12, chroma_light=0.4)
#: Glanzlicht
SHINE = st.fixed("#c8fff4", "#dcfff8", "#eefffb", "#ffffff", "#ffffff", "#ffffff", "#ffffff")
#: schwarzer Leerenstahl (die Platten der Rüstung) – fast schwarz mit einem Hauch Violett
VOID_STEEL = st.ramp("#2a2436", dark=0.12, light=0.66, cool=280, hue_dark=8, warm=300, hue_light=10, chroma_light=0.7)
#: Purpur (Säume, Fassungen)
PURPUR = st.ramp("#a679a8", dark=0.24, light=0.94, cool=290, hue_dark=12, warm=330, hue_light=8, chroma_light=0.5)
#: das türkise Leuchten der Nähte und Augen
GLOW = st.ramp("#3ce8cc", dark=0.42, light=0.99, cool=200, hue_dark=10, warm=170, hue_light=6, chroma_light=0.3)

BLOCK_FRAMES = 16


# =============================================================================
# Der Kristall
# =============================================================================

def _prism(s: st.Spr, base: tuple[float, float], angle: float, length: float, half: float, tip: float, bright: int = 0) -> list:
    """Ein sechseckiges Prisma mit Spitze: Grundmitte {@code base}, nach oben um {@code angle} Grad (im Uhrzeigersinn)
    geneigt. Linke Fläche hell, Mitte mittel, rechte dunkel, zur Spitze heller. Gibt die Pixel mit (t, u) zurück."""
    a = math.radians(angle)
    dx, dy = math.sin(a), -math.cos(a)            # die Achse (nach oben)
    nx, ny = math.cos(a), math.sin(a)             # quer dazu (nach rechts)
    out = []
    for y in range(N):
        for x in range(N):
            px, py = x + 0.5 - base[0], y + 0.5 - base[1]
            t = px * dx + py * dy
            u = px * nx + py * ny
            if t < 0 or t > length:
                continue
            width = half if t <= length - tip else half * (length - t) / tip
            if abs(u) > width + 0.15:
                continue
            k = t / length
            third = width / 3.0
            if t > length - tip:
                lvl = 5 if u < 0 else 4                 # die Spitze: zwei Flächen
            elif u < -third:
                lvl = 4 + (1 if k > 0.45 else 0)        # linke Fläche im Licht
            elif u <= third:
                lvl = 3 + (1 if k > 0.6 else 0)         # die Mitte
            else:
                lvl = 2 + (1 if k > 0.7 else 0)         # rechte Fläche im Schatten
            s.set(x, y, ("P", st.clamp(lvl + bright, 1, 6)))
            out.append((x, y, t, u))
    return out


def phasit_gem_art() -> Image.Image:
    """Der Phasit: ein großes Prisma, leicht nach links geneigt, rechts unten ein kleines, das nach rechts abknickt."""
    s = st.Spr()
    # zwei kleine Prismen am Fuß (dahinter), dann das große, das schräg nach rechts oben ragt
    _prism(s, (4.6, 14.6), -26.0, 5.2, 1.25, 1.8, bright=-1)
    _prism(s, (8.4, 15.0), 62.0, 4.8, 1.2, 1.7, bright=-1)
    big = _prism(s, (4.4, 13.6), 42.0, 14.8, 2.45, 5.0)
    # der Grat zwischen linker Fläche und Mitte fängt das Licht
    for x, y, t, u in big:
        if -1.15 <= u <= -0.4 and 1.5 < t < 14.8 - 5.0:
            s.set(x, y, ("P", 6 if t > 5.5 else 5))
    # der violette Funke im großen Prisma und sein Schweif (zur Spitze hin)
    for x, y, lv in ((8, 8, 6), (9, 7, 5), (8, 9, 3), (7, 9, 2)):
        s.set(x, y, ("V", lv))
    # der Glanz an der Spitze
    for x, y, lv in ((12, 3, 6), (11, 4, 3)):
        s.set(x, y, ("W", lv))
    s = s.outline(prefer=("P",), override={"V": "P", "W": "P"}, light=1, dark=0)
    return s.image({"P": GEM, "V": SPARK, "W": SHINE})


# =============================================================================
# Der Block: Phasit in Purpur gefasst
# =============================================================================

#: die vier Steine (x0, y0, x1, y1 einschließlich), in der Reihenfolge, in der das Licht springt
TILES = ((2, 2, 6, 6), (9, 2, 13, 6), (9, 9, 13, 13), (2, 9, 6, 13))


def _tile_level(x: int, y: int, tile: tuple[int, int, int, int]) -> int:
    """Ein geschliffener Stein: oben links hell, unten rechts dunkel, in der Mitte die Tafel."""
    x0, y0, x1, y1 = tile
    if x == x0 or y == y0:
        return 5 if not (x == x1 or y == y1) else 3
    if x == x1 or y == y1:
        return 2
    cx, cy = (x0 + x1) / 2, (y0 + y1) / 2
    d = abs(x - cx) + abs(y - cy)
    return 4 if d <= 1.0 else 3


def phasit_block() -> tuple[Image.Image, Image.Image]:
    """Der Block und seine Leuchtebene (Bildfolge: das Licht springt reihum von Stein zu Stein)."""
    s = st.Spr()
    for y in range(N):
        for x in range(N):
            tile = next((t for t in TILES if t[0] <= x <= t[2] and t[1] <= y <= t[3]), None)
            if tile is not None:
                s.set(x, y, ("P", _tile_level(x, y, tile)))
                continue
            # die Fassung: Purpur, abgeschrägt (außen oben/links im Licht), Nieten in den Ecken und in der Mitte
            outer = x in (0, N - 1) or y in (0, N - 1)
            if outer:
                tone = 4 if (x == 0 or y == 0) and not (x == N - 1 or y == N - 1) else 1
            else:
                tone = 3 if (x in (1, 7) or y in (1, 7)) else 2
            if (x, y) in ((1, 1), (N - 2, 1), (1, N - 2), (N - 2, N - 2), (7, 7), (8, 8)):
                tone = 5
            s.set(x, y, ("B", tone))
    for x, y in ((4, 4), (11, 4), (11, 11), (4, 11)):
        s.set(x, y, ("V", 4))                                # in jedem Stein ein Funke
    for x, y in ((3, 3), (10, 3), (10, 10), (3, 10)):
        s.set(x, y, ("W", 2))                                # Glanz
    img = s.image({"P": PHASIT, "B": PURPUR, "V": SPARK, "W": SHINE})

    frames = Image.new("RGBA", (N, N * BLOCK_FRAMES), CLEAR)
    for fr in range(BLOCK_FRAMES):
        for i, tile in enumerate(TILES):
            # das Licht: jeder Stein leuchtet vier Bilder lang auf, der vorige verglimmt noch
            age = (fr - i * 4) % BLOCK_FRAMES
            if age < 4:
                k = 1.0 - age / 5.0
            elif age >= BLOCK_FRAMES - 2:
                k = 0.35 * (age - BLOCK_FRAMES + 3) / 2.0    # Vorglühen
            else:
                k = 0.0
            x0, y0, x1, y1 = tile
            for y in range(y0, y1 + 1):
                for x in range(x0, x1 + 1):
                    base = 0.25 + 0.15 * (_tile_level(x, y, tile) - 2)   # der ruhige Schimmer
                    a = max(base * 0.55, k)
                    if a <= 0.05:
                        continue
                    col = PHASIT[6] if k > 0.6 and _tile_level(x, y, tile) >= 4 else PHASIT[5]
                    frames.putpixel((x, fr * N + y), (col[0], col[1], col[2], int(60 + 170 * min(1.0, a))))
            # der Funke folgt dem Licht
            if age < 4:
                sx, sy = ((4, 4), (11, 4), (11, 11), (4, 11))[i]
                frames.putpixel((sx, fr * N + sy), SPARK[6])
    return img, frames


# =============================================================================
# Die Rüstung der Leerwandler
# =============================================================================

def voidwalker_icons(put: Callable[[str, Image.Image], None]) -> None:
    ramps = {"A": VOID_STEEL, "B": PURPUR, "D": GLOW, "E": PHASIT, "C": SHINE}
    override = {"D": "A", "C": "A", "E": "A"}

    # --- die Kapuze: geschlossen, zwei türkis leuchtende Augenschlitze, ein Purpursaum über der Stirn, darüber eine Krone
    # aus drei Phasitsplittern
    s = icons.base(icons.CLOSED_HELMET)
    for x in (5, 6, 9, 10):
        s.set(x, 8, ("D", 6 if x in (5, 10) else 5))           # die Augen der Leerwandler
    for x in (5, 10):
        s.set(x, 9, ("D", 3))
    for x in range(3, 13):
        if s.opaque(x, 6):
            s.set(x, 6, ("B", 5 if x < 8 else 4))               # der Purpursaum
    for x, y, lv in ((7, 1, 6), (7, 2, 5), (8, 2, 4), (7, 3, 4), (8, 3, 3),     # der mittlere Splitter
                     (4, 2, 5), (5, 3, 4), (4, 3, 3),                          # links, nach außen geneigt
                     (11, 2, 4), (10, 3, 4), (11, 3, 2)):                      # rechts
        s.set(x, y, ("E", lv))
    s.set(7, 5, ("E", 6))
    s.set(8, 5, ("E", 4))                                       # der Stirnstein
    put("item/voidwalker_helmet", s.outline(prefer=("A", "B"), override=override).image(ramps))

    # --- der Mantel: Purpur an den Schultern, ein Phasit auf der Brust, leuchtende Nähte, die von ihm ausgehen; unten lang
    s = icons.base(icons.CHESTPLATE)
    for x in range(2, 14):
        for y in (2, 3):
            if s.opaque(x, y) and (x <= 5 or x >= 10):
                s.set(x, y, ("B", 5 if y == 2 else 3))
    for x, y, lv in ((7, 6, 6), (8, 6, 4), (7, 7, 4), (8, 7, 3)):
        s.set(x, y, ("E", lv))
    for x, y in ((7, 5), (8, 5), (6, 6), (9, 6), (6, 7), (9, 7), (7, 8), (8, 8)):
        s.set(x, y, ("B", 4 if y <= 6 else 2))                  # die Fassung
    for x, y, lv in ((5, 9, 4), (4, 10, 3), (10, 9, 4), (11, 10, 3), (7, 10, 4), (8, 11, 3), (7, 12, 2)):
        if s.opaque(x, y):
            s.set(x, y, ("D", lv))                              # die leuchtenden Nähte
    for x in range(5, 11):
        if s.opaque(x, 13):
            s.set(x, 13, ("B", 3))
    put("item/voidwalker_chestplate", s.outline(prefer=("A", "B"), override=override).image(ramps))

    # --- die Beinlinge: ein Purpurgürtel mit Phasitschnalle, Phasitknoten an den Knien, eine leuchtende Naht außen
    s = icons.base(icons.LEGGINGS)
    for x in range(2, 14):
        s.set(x, 2, ("B", 5 if x < 8 else 4))
        s.set(x, 3, ("B", 3))
    s.set(7, 2, ("E", 6))
    s.set(8, 2, ("E", 4))
    s.set(7, 3, ("E", 4))
    s.set(8, 3, ("E", 2))
    for x0 in (2, 10):
        s.set(x0 + 1, 8, ("E", 6))
        s.set(x0 + 2, 8, ("E", 4))
        s.set(x0 + 1, 7, ("B", 4))
        s.set(x0 + 2, 7, ("B", 4))
    for y in range(9, 13):
        s.set(2, y, ("D", 4 if y % 2 else 3))
        s.set(13, y, ("D", 3 if y % 2 else 2))
    put("item/voidwalker_leggings", s.outline(prefer=("A", "B"), override=override).image(ramps))

    # --- die Stiefel: ein Purpurband, Phasit an den Knöcheln und leuchtende Sohlen, die über dem Boden schweben
    s = icons.base(icons.BOOTS)
    for x in list(range(1, 6)) + list(range(10, 15)):
        if s.opaque(x, 11):
            s.set(x, 11, ("D", 6 if x in (2, 3, 11, 12) else 5))
        if s.opaque(x, 8):
            s.set(x, 8, ("B", 5 if x < 8 else 4))
    for x0 in (4, 11):
        s.set(x0, 6, ("E", 6))
        s.set(x0 - 1, 6, ("E", 4))
    img = s.outline(prefer=("A", "B"), override=override).image(ramps)
    # unter den Sohlen ein Hauch Licht (der Schweberaum)
    for x in list(range(2, 6)) + list(range(10, 14)):
        c = GLOW[4]
        img.putpixel((x, 13), (c[0], c[1], c[2], 110))
    put("item/voidwalker_boots", img)


# =============================================================================
# Ausgabe
# =============================================================================

def build(put: Callable[[str, Image.Image], None]) -> None:
    block, glow = phasit_block()
    put("block/phasit_block", block)
    put("block/phasit_block_glow", glow)
    ANIMATIONS["block/phasit_block_glow"] = (BLOCK_FRAMES, 3, True)
    put("item/phasit", phasit_gem_art())
    voidwalker_icons(put)


if __name__ == "__main__":
    import sys

    out: dict[str, Image.Image] = {}
    build(lambda k, i: out.__setitem__(k, i))
    tiles = []
    plain = Image.new("RGBA", (N * 2, N * 2))
    for dx in (0, N):
        for dy in (0, N):
            plain.alpha_composite(out["block/phasit_block"], (dx, dy))
    tiles.append(plain)
    for f in (0, 3, 6, 10):
        fr = out["block/phasit_block_glow"].crop((0, f * N, N, (f + 1) * N))
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
    sheet.save(sys.argv[1] if len(sys.argv) > 1 else "phasit.png")
