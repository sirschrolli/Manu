#!/usr/bin/env python3
"""
Texturen zu Malleus, dem Glutschmied (0.64), im Vanilla-Stil (siehe tools/item_style.py):

  * der Glutspeer: Bild im Inventar (schräg, wie die Speere von Minecraft) und der Atlas für das 3D-Modell in der Hand
    (Schaft aus geschwärztem Eisenholz mit Glutadern, Lederwicklung, goldene Ringe, Parierstück mit Widderhörnern und
    einem Glutstein, eine breite, blattförmige Klinge aus rotglühendem Metall mit weißglühender Mittelader);
  * der Glutklumpen: ein Klecks geschmolzenen Metalls mit Schlackenkruste und Tropfen;
  * der Glutamboss: geschwärztes, geschmiedetes Eisen mit Nieten und glühenden Nähten (heiß und erkaltet), dazu je eine
    Leuchtmaske für die glühenden Stellen.
"""
from __future__ import annotations

import math
from typing import Callable

from PIL import Image

import item_style as st
from sea_textures import _diagonal_art

SHAFT = st.ramp("#3a2a24", dark=0.08, light=0.6, cool=300, warm=40)
GOLD = st.ramp("#d8a440", dark=0.25, light=0.98, cool=40, warm=70, chroma_light=0.6)
BLADE = st.ramp("#ff7418", dark=0.32, light=1.0, cool=25, warm=95, hue_dark=24, chroma_light=0.55)
GRIP = st.ramp("#4a2c1c", dark=0.1, light=0.62, cool=330)
EMBER = st.ramp("#ff5a14", dark=0.3, light=1.0, cool=20, warm=80)
CRUST = st.ramp("#3a302c", dark=0.1, light=0.55, cool=280)
MOLTEN = st.ramp("#ff8a1e", dark=0.34, light=1.0, cool=25, warm=95, hue_dark=24, chroma_light=0.5)


def render(art: str | st.Spr, ramps: dict[str, st.Ramp], prefer: tuple[str, ...] = ("A", "B", "C", "D", "E"),
           override: dict[str, str] | None = None) -> Image.Image:
    s = st.Spr.art(art) if isinstance(art, str) else art
    return s.outline(prefer=prefer, override=override).image(ramps)


# =============================================================================
# Der Glutspeer
# =============================================================================

def glut_spear_icon() -> Image.Image:
    """Schräg von unten links nach oben rechts: goldene Kappe, Lederwicklung, geschwärzter Schaft mit Goldring,
    Parierstück mit Widderhörnern und Glutstein, darüber die breite, rotglühende Klinge mit weißglühender Ader."""
    def profile(t: float, off: float):
        if t < 0.06:
            return [(0.95, "d" if off < 0 else "b")]
        if t < 0.21:
            band = int(t * 64) % 2 == 0
            return [(0.85, "s" if band else "q")]
        if t < 0.53:
            if abs(t - 0.37) < 0.02:
                return [(0.95, "e")]
            return [(0.72, "3" if off < 0 else "1")]
        if t < 0.585:
            if abs(off) < 0.62:
                return [(0.62, "F")]
            return [(2.5, "d" if off < 0 else "b")]
        if t < 0.64 and abs(off) > 1.45:
            return [(2.75, "f" if off < 0 else "c")]
        u = (t - 0.585) / 0.415
        if u < 0.3:
            w = 1.3 + 1.4 * (u / 0.3) ** 0.8
        else:
            w = 2.7 * ((1.0 - u) / 0.7) ** 0.85 + 0.15
        if abs(off) < 0.55 and u < 0.92:
            return [(w, "n")]
        if abs(off) > w - 0.75:
            return [(w, "l" if off < 0 else "j")]
        return [(w, "m" if off < 0 else "l")]
    art = _diagonal_art(profile)
    return render(st.Spr.art(art), {"A": SHAFT, "B": GOLD, "C": BLADE, "D": GRIP, "E": EMBER},
                  prefer=("C", "B", "E", "A", "D"))


def glut_spear_model() -> tuple[Image.Image, Image.Image]:
    """
    Atlas für das 3D-Modell: (0..2, 0..16) Schaft (geschwärztes Eisenholz, feine Glutfäden), (2..4, 0..16)
    Lederwicklung, (4..6, 0..8) Gold, (6..10, 0..12) Klinge (rotglühend, zum Rand dunkler), (10..12, 0..12) weißglühende
    Ader, (12..14, 0..4) Glutstein, (14..16, 0..8) geschwärztes Eisen (Parierstück). Dazu die Leuchtmaske.
    """
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    glow = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    px, gp = img.load(), glow.load()
    rnd = st.seeded(64001)
    for y in range(16):
        for x in range(2):
            v = 2 + (1 if x == 0 else 0) - (1 if rnd.random() < 0.3 else 0)
            px[x, y] = SHAFT[v]
        if y % 5 == 2:
            px[0, y] = EMBER[4]
            gp[0, y] = EMBER[4]
        for x in range(2, 4):
            band = y % 3 == 0
            px[x, y] = GRIP[1] if band else GRIP[3 if x == 2 else 2]
    for y in range(8):
        for x in range(4, 6):
            px[x, y] = GOLD[5 if x == 4 and y % 3 != 2 else 3]
    for y in range(12):
        for x in range(6, 10):
            edge = x in (6, 9)
            v = 3 if edge else 5
            if y < 2:
                v += 1
            col = BLADE[min(6, v)]
            px[x, y] = col
            gp[x, y] = col if not edge else (int(col[0] * 0.7), int(col[1] * 0.7), int(col[2] * 0.7), 255)
        for x in range(10, 12):
            col = BLADE[6] if (x + y) % 3 else st.hexc("#fff8e0")
            px[x, y] = col
            gp[x, y] = col
    for y in range(4):
        for x in range(12, 14):
            col = EMBER[6 if (x + y) % 2 == 0 else 4]
            px[x, y] = col
            gp[x, y] = col
    for y in range(8):
        for x in range(14, 16):
            px[x, y] = SHAFT[3 if x == 14 else 1] if y % 4 != 3 else GOLD[4]
    return img, glow


# =============================================================================
# Der Glutklumpen
# =============================================================================

def molten_glob_icon() -> Image.Image:
    """Ein Klecks geschmolzenen Metalls: innen gelbweiß, nach außen orange und rot, oben rechts eine dunkle
    Schlackenkruste, unten zwei Tropfen."""
    s = st.Spr()
    cx, cy, r = 7.6, 7.8, 5.6
    for y in range(16):
        for x in range(16):
            dx, dy = x + 0.5 - cx, y + 0.5 - cy
            a = math.atan2(dy, dx)
            rr = r * (1.0 + 0.09 * math.sin(a * 3.0 + 0.6) + 0.05 * math.sin(a * 5.0 + 1.3))
            d = math.hypot(dx, dy)
            if d > rr:
                continue
            # Licht von oben links: der heiße Kern liegt etwas versetzt
            hot = math.hypot(dx + 1.2, dy + 1.4) / rr
            lvl = 6 if hot < 0.32 else 5 if hot < 0.55 else 4 if hot < 0.78 else 3 if hot < 0.98 else 2
            crust = st.smooth_noise(71, x, y, 2.6) > 0.5 and d > rr * 0.45 and dx - dy * 0.4 > 0.2
            s.set(x, y, ("A", 2 if st.noise(5, x, y) < 0.5 else 3) if crust else ("C", lvl))
    # Tropfen
    for x, y0, n in ((6, 13, 2), (10, 12, 3)):
        for k in range(n):
            s.set(x, y0 + k, ("C", 4 if k < n - 1 else 5))
    s.set(9, 14, ("C", 3))
    return render(s, {"A": CRUST, "C": MOLTEN}, prefer=("C", "A"))


# =============================================================================
# Der Glutamboss
# =============================================================================

def _anvil(top: bool, hot: bool) -> tuple[Image.Image, Image.Image]:
    """Geschmiedetes Eisen: Hammerspuren, Nieten an den Kanten, glühende Nähte (nur heiß). Die Bahn (oben) ist blank
    geschlagen, mit einer rotglühenden Stelle in der Mitte, wo das Werkstück lag."""
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 255))
    glow = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    px, gp = img.load(), glow.load()
    iron = st.ramp("#3c3a40", dark=0.12, light=0.78, cool=270, warm=60) if hot else st.ramp("#4a4846", dark=0.16, light=0.74, cool=250,
                                                                                            warm=60, chroma_light=0.2)
    ash = st.ramp("#8a847e", dark=0.4, light=0.86)
    for y in range(16):
        for x in range(16):
            n = st.smooth_noise(311, x, y, 2.4)
            v = 2 + round(n * 2.2)
            # Hammerspuren: kleine, dunklere Mulden
            if st.smooth_noise(97, x, y, 1.6) > 0.78:
                v -= 1
            if top:
                v += 1
                if y in (0, 15) or x in (0, 15):
                    v = 4 if (x == 0 or y == 0) else 1
            else:
                if y == 0:
                    v = 5
                elif y == 15:
                    v = 0
            px[x, y] = iron[max(0, min(6, v))]
    if not top:
        # Nieten in den Ecken
        for rx, ry in ((2, 2), (13, 2), (2, 13), (13, 13)):
            px[rx, ry] = iron[6]
            px[rx + 1, ry] = iron[4]
            px[rx, ry + 1] = iron[3]
            px[rx + 1, ry + 1] = iron[1]
    if hot:
        # glühende Nähte und Risse: ein paar gezackte Linien, die zur Mitte heller werden
        seams = [((4, 15), (6, 11), (5, 8), (7, 5)), ((11, 15), (10, 12), (12, 9)), ((15, 6), (12, 7), (10, 6))] if not top else \
            [((0, 8), (3, 7), (6, 8)), ((16, 7), (13, 8), (10, 7))]
        for path in seams:
            for (x0, y0), (x1, y1) in zip(path, path[1:]):
                for x, y in st.line(x0, y0, x1, y1):
                    if 0 <= x < 16 and 0 <= y < 16:
                        k = 4 + (1 if (x + y) % 3 == 0 else 0)
                        px[x, y] = EMBER[k]
                        gp[x, y] = EMBER[k]
        if top:
            # die glühende Stelle in der Mitte der Bahn
            for y in range(16):
                for x in range(16):
                    d = math.hypot((x + 0.5 - 8.0) / 1.6, (y + 0.5 - 8.0) / 1.0)
                    if d < 2.9:
                        k = 6 if d < 1.0 else 5 if d < 1.9 else 3
                        col = BLADE[k]
                        px[x, y] = col
                        gp[x, y] = col if k > 3 else (col[0], col[1], col[2], 160)
    else:
        # erkaltet: graue Asche in den Mulden, die Nähte schwarz
        for y in range(16):
            for x in range(16):
                if st.noise(41, x, y) > 0.9:
                    px[x, y] = ash[2]
    return img, glow


def build(put: Callable[[str, Image.Image], None]) -> None:
    put("item/glut_spear", glut_spear_icon())
    model, model_glow = glut_spear_model()
    put("item/glut_spear_model", model)
    put("item/glut_spear_model_glow", model_glow)
    # der Speer auf dem Amboss (Blockmodelle sehen nur den Block-Atlas)
    put("block/glut_anvil_spear", model)
    put("block/glut_anvil_spear_glow", model_glow)
    put("item/molten_glob", molten_glob_icon())
    for top in (False, True):
        name = "block/glut_anvil" + ("_top" if top else "")
        hot, hot_glow = _anvil(top, True)
        cold, _ = _anvil(top, False)
        put(name, hot)
        put(name + "_glow", hot_glow)
        put(name + "_cold", cold)


if __name__ == "__main__":
    import sys
    out: dict[str, Image.Image] = {}
    build(lambda k, im: out.__setitem__(k, im))
    st.preview(out, sys.argv[1] if len(sys.argv) > 1 else "forge_preview.png", zoom=12, cols=6)
