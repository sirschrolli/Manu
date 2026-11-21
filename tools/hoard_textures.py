#!/usr/bin/env python3
"""
Texturen zum Schlund der Welten (0.73), im Vanilla-Stil (siehe tools/item_style.py):

  * der Weltenspeer: Bild im Inventar (schräg, wie die Speere von Minecraft) und der Atlas für das 3D-Modell in der Hand –
    goldener Knauf mit einem violetten Kronensplitter, Griff aus Chaosgestein mit Goldbändern, das Kreuz aus vier
    Bruchstücken der verschlungenen Welten (Wiese, Netherrack, Endstein, Eis), der Schaft aus Chaosgestein, durch den eine
    Weltennaht glüht, ein Goldkragen mit Kronenzacken und die Klinge: der Fangzahn des Weltenfressers (elfenbeinweiß, in der
    Mitte eine weißglühende Naht).

Wird von tools/generate_textures.py aufgerufen (build(put)).
"""
from __future__ import annotations

import math
from typing import Callable

from PIL import Image

import item_style as st
from sea_textures import _diagonal_art

GOLD = st.ramp("#e0b84e", dark=0.3, light=0.98, cool=40, warm=75, chroma_light=0.6)
CHAOS = st.ramp("#3c2c40", dark=0.1, light=0.6, cool=290, warm=330, chroma_light=0.6)
BONE = st.ramp("#e2d6bc", dark=0.38, light=1.0, cool=40, warm=80, chroma_light=0.35)
EMBER = st.ramp("#ff7a2a", dark=0.32, light=1.0, cool=20, warm=70, chroma_light=0.55)
CROWN = st.ramp("#9a5ae0", dark=0.28, light=0.98, cool=280, warm=300)
GRASS = st.ramp("#5c9a38", dark=0.26, light=0.86, cool=150)
ICE = st.ramp("#8ccaf2", dark=0.36, light=1.0, cool=225, warm=200)
CLEAR = (0, 0, 0, 0)


def render(art: str, ramps: dict[str, st.Ramp], prefer: tuple[str, ...]) -> Image.Image:
    return st.Spr.art(art).outline(prefer=prefer).image(ramps)


# =============================================================================
# Der Weltenspeer
# =============================================================================

def world_spear_icon() -> Image.Image:
    """Schräg von unten links nach oben rechts (wie die Speere von Minecraft)."""
    def profile(t: float, off: float):
        if t < 0.06:
            # goldener Knauf mit dem violetten Kronensplitter
            if abs(off) < 0.45 and 0.012 < t < 0.05:
                return [(1.0, "F")]
            return [(1.0, "5" if off < 0 else "3")]
        if t < 0.20:
            # Griff aus Chaosgestein mit Goldbändern
            if int(t * 50) % 3 == 0:
                return [(0.85, "4" if off < 0 else "2")]
            return [(0.8, "d" if off < 0 else "b")]
        if t < 0.262:
            # das Kreuz: vier Bruchstücke der Welten um einen goldenen Kern
            if abs(off) < 0.6:
                return [(0.6, "5")]
            if off < -1.65:
                return [(2.75, "L")]          # Wiese
            if off < 0:
                return [(1.65, "q")]          # Netherrack (dunkle Glut)
            if off < 1.65:
                return [(1.65, "6")]          # Endstein (helles Gold)
            return [(2.75, "R")]              # Eis
        if t < 0.655:
            # Schaft aus Chaosgestein; die Weltennaht glüht in kurzen Strichen hindurch
            if abs(off) < 0.42 and (t * 17.0) % 1.0 < 0.34:
                return [(0.75, "s")]
            return [(0.75, "e" if off < 0 else "c")]
        if t < 0.70:
            # Goldkragen mit zwei violetten Kronenzacken
            if 0.668 < t < 0.69 and 1.1 < abs(off) < 1.95:
                return [(1.95, "G")]
            return [(1.15, "5" if off < 0 else "3")]
        # die Klinge: der Fangzahn des Weltenfressers, in der Mitte die glühende Naht
        u = (t - 0.70) / 0.30
        if u < 0.18:
            w = 1.05 + u / 0.18 * 0.75
        else:
            w = 1.8 * (1.0 - (u - 0.18) / 0.82) ** 0.9 + 0.1
        if abs(off) < 0.5 and 0.04 < u < 0.74:
            return [(w, "t" if 0.28 < u < 0.52 else "s")]
        if u > 0.88:
            return [(w, "n")]
        return [(w, "m" if off < 0 else "k")]
    art = _diagonal_art(profile)
    ramps = {"A": GOLD, "B": CHAOS, "C": BONE, "D": EMBER, "E": CROWN, "F": GRASS, "G": ICE}
    return render(art, ramps, prefer=("C", "A", "D", "E", "F", "G", "B"))


def world_spear_model() -> tuple[Image.Image, Image.Image]:
    """
    Atlas für das 3D-Modell (16x16): (0..2, 0..16) Schaft aus Chaosgestein, (2..4, 0..16) Griff mit Goldbändern, (4..6, 0..8)
    Gold, (4..6, 8..16) violetter Kronenkristall, (6..10, 0..12) Elfenbein des Fangzahns (zur Schneide dunkler),
    (10..12, 0..12) weißglühende Naht, (12..14, 0..4) Wiese, (14..16, 0..4) Netherrack, (12..14, 4..8) Endstein,
    (14..16, 4..8) Eis, (12..16, 8..12) Glut der Nahtringe, (6..16, 12..16) die Naht im Schaft (nur Leuchten, sonst klar).
    Dazu die Leuchtmaske (nur die leuchtenden Teile).
    """
    img = Image.new("RGBA", (16, 16), CLEAR)
    glow = Image.new("RGBA", (16, 16), CLEAR)
    px, gp = img.load(), glow.load()

    def put(x: int, y: int, c, lit: bool = False) -> None:
        px[x, y] = c
        if lit:
            gp[x, y] = c

    for y in range(16):
        # Schaft: Chaosgestein mit hellen Splittern fremder Welten
        for x in range(2):
            n = (x * 7 + y * 13) % 11
            c = CHAOS[3 if x == 0 else 2]
            if n == 0:
                c = CHAOS[4]
            elif n == 5:
                c = EMBER[1]
            put(x, y, c)
        # Griff: Chaosgestein, alle vier Pixel ein Goldband
        for x in range(2, 4):
            if y % 4 == 0:
                put(x, y, GOLD[5 if x == 2 else 3])
            else:
                put(x, y, CHAOS[2 if (x + y) % 2 else 3])
    for y in range(8):
        for x in range(4, 6):
            put(x, y, GOLD[5 if x == 4 and y % 3 != 2 else 3])
    for y in range(8, 16):
        for x in range(4, 6):
            put(x, y, CROWN[5 if (x + y) % 3 else 3] if y > 8 else CROWN[6], lit=True)
    # Elfenbein des Fangzahns: oben hell, zu den Rändern dunkler, feine Wachstumslinien
    for y in range(12):
        for x in range(6, 10):
            edge = x in (6, 9)
            level = 4 if edge else 5
            if y % 4 == 3:
                level -= 1
            if y < 2 and not edge:
                level = 6
            put(x, y, BONE[level])
        for x in range(10, 12):
            col = EMBER[6] if (x + y) % 3 else st.hexc("#ffffff")
            if y > 9:
                col = EMBER[5]
            put(x, y, col, lit=True)
    # vier Welten
    for y in range(4):
        for x in range(12, 14):
            put(x, y, GRASS[5 if y == 0 else 4] if y < 1 else st.hexc("#6a4a32") if (x + y) % 2 else st.hexc("#7a5a3a"))
        for x in range(14, 16):
            put(x, y, st.hexc("#7a2826") if (x + y) % 2 else st.hexc("#9a3430"))
    for y in range(4, 8):
        for x in range(12, 14):
            put(x, y, st.hexc("#d8d29a") if (x + y) % 2 else st.hexc("#ece6b0"))
        for x in range(14, 16):
            put(x, y, ICE[5] if (x + y) % 2 else ICE[4])
    # Glut der Nahtringe
    for y in range(8, 12):
        for x in range(12, 16):
            put(x, y, EMBER[5] if (x + y) % 2 else EMBER[4], lit=True)
    # die Naht im Schaft: eine zackige, weißglühende Linie, sonst durchsichtig
    for y in range(12, 16):
        for x in range(6, 16):
            px[x, y] = CLEAR
    for x in range(6, 16):
        y = 12 + [1, 2, 2, 1, 0, 1, 2, 3, 2, 1][x - 6]
        put(x, y, EMBER[6], lit=True)
        if x % 3 == 0:
            yy = min(15, y + 1)
            put(x, yy, EMBER[4], lit=True)
    return img, glow


# =============================================================================
# Die Kronensplitter (der schwebende Kristall über der Druse)
# =============================================================================

def seal_crystal() -> Image.Image:
    """
    Textur des schwebenden Kristalls (24x32): sechs Flächen nebeneinander (je 4 Pixel), oben (v 0..0.62) die lange Spitze –
    zur Spitze weiß, sonst violett, die linke Kante jeder Fläche hell, die rechte dunkel –, unten (v 0.62..1) der kurze
    Fuß, der ins tiefe Purpur geht. Licht von links oben: die ersten Flächen heller.
    """
    w, h = 24, 32
    img = Image.new("RGBA", (w, h), CLEAR)
    px = img.load()
    split = int(round(h * 0.62))
    for y in range(h):
        for x in range(w):
            face = x // 4
            fx = x % 4
            light = [1.0, 0.86, 0.7, 0.62, 0.74, 0.9][face]
            if y < split:
                t = y / max(1, split - 1)                 # 0 an der Spitze, 1 am Gürtel
                level = 6.0 - 3.2 * t
            else:
                t = (y - split) / max(1, h - split - 1)    # 0 am Gürtel, 1 am Fuß
                level = 2.8 - 2.4 * t
            level *= light
            if fx == 0:
                level += 0.9
            elif fx == 3:
                level -= 0.8
            if y == split or y == split - 1:
                level += 0.6                              # der Gürtel glänzt
            i = max(0, min(6, int(round(level))))
            px[x, y] = CROWN[i]
    # ein paar helle Einschlüsse
    for (x, y) in ((1, 4), (5, 9), (13, 6), (17, 12), (9, 14), (21, 3)):
        px[x, y] = st.hexc("#ffffff")
    return img


def soft_glow(size: int = 64) -> Image.Image:
    """Ein weicher, runder Schein (für additive Lichthöfe): weiß, zum Rand hin ganz ausklingend, mit hellerem Kern."""
    img = Image.new("RGBA", (size, size), CLEAR)
    px = img.load()
    c = (size - 1) / 2.0
    for y in range(size):
        for x in range(size):
            r = math.hypot(x - c, y - c) / (size / 2.0)
            if r >= 1.0:
                continue
            f = 0.72 * (1.0 - r) ** 2.4 + 0.28 * math.exp(-r * r * 18.0)
            v = max(0, min(255, round(255 * f)))
            px[x, y] = (v, v, v, v)
    return img


def build(put: Callable[[str, Image.Image], None]) -> None:
    put("entity/seal_crystal", seal_crystal())
    put("entity/soft_glow", soft_glow())
    put("item/world_spear", world_spear_icon())
    model, model_glow = world_spear_model()
    put("item/world_spear_model", model)
    put("item/world_spear_model_glow", model_glow)


if __name__ == "__main__":
    import sys
    out: dict[str, Image.Image] = {}
    build(lambda k, v: out.__setitem__(k, v))
    sheet = Image.new("RGBA", (len(out) * 200, 192), (48, 48, 52, 255))
    for i, (k, v) in enumerate(out.items()):
        big = v.resize((192, 192), Image.NEAREST)
        sheet.paste(big, (i * 200, 0), big)
    sheet.save(sys.argv[1] if len(sys.argv) > 1 else "hoard_textures.png")
