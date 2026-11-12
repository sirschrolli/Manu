#!/usr/bin/env python3
"""
Texturen der Angeln in 3D (0.90), im Stil von Minecraft: je Angel ein Atlas (32x32) für das Modell in der Hand und ein
Leuchtatlas (dieselben Bereiche; die leuchtenden Teile – Steine, Glutköder, Sternenschnur – nehmen ihre Farbe von dort). Die
Bereiche legt tools/rod_models.py fest; das Bild im Inventar (16x16) bleibt das bisherige.

Aufruf:  python3 tools/rod_textures.py     schreibt textures/item/<angel>_model.png und <angel>_model_glow.png
"""
from __future__ import annotations

import os
import sys
from dataclasses import dataclass

from PIL import Image

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import armory_textures as at  # noqa: E402
import item_style as st  # noqa: E402
import rod_models as models  # noqa: E402
from armory_textures import _lv, h01, paint  # noqa: E402

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
OUT = os.path.join(ROOT, "src/main/resources/assets/kingdomsatwar/textures/item")

STRING = st.ramp("#d8d4cc", dark=0.36, light=0.99, chroma_dark=0.6, cool=250)
IRON = st.ramp("#8a9098", dark=0.2, light=0.93, chroma_dark=1.2)
RED = st.ramp("#c0302a", dark=0.2, light=0.86, cool=350)
WHITE = st.ramp("#e8e6e0", dark=0.42, light=1.0, chroma_dark=0.7)
CORK = st.ramp("#b08a5a", dark=0.26, light=0.88, cool=30)
RED_LEATHER = st.ramp("#8a2228", dark=0.14, light=0.78, cool=340)
IVORY = st.ramp("#e8dcc0", dark=0.38, light=0.99, warm=80)
RUBY = st.fixed("#3a0610", "#6a0c1c", "#a01428", "#d02a3c", "#f05a64", "#ff9ca0", "#ffe4e4")
HORN = st.ramp("#3a3230", dark=0.1, light=0.82, cool=20, warm=60)
GOLD_THREAD = st.ramp("#d8a43a", dark=0.3, light=0.96, warm=90)
VIOLET_THREAD = st.ramp("#6a4aa0", dark=0.16, light=0.86, cool=280)
BASALT = st.ramp("#4a4448", dark=0.1, light=0.72, cool=280)
BLAZE = st.fixed("#7a3a06", "#b85a0a", "#e88a12", "#ffb81e", "#ffd84a", "#fff08a", "#fffbe0")
STARMETAL = st.ramp("#2a3a8a", dark=0.12, light=0.8, cool=270)
MIDNIGHT = st.ramp("#22306a", dark=0.1, light=0.62, cool=270)
STARLIGHT = st.fixed("#4a3a10", "#8a6a1a", "#c8a02a", "#f0cc4a", "#ffe680", "#fff4b8", "#fffdf0")
PALE_GOLD = st.fixed("#5a4a2a", "#8a7444", "#bca468", "#e4d08e", "#f4e6b0", "#fcf4d8", "#ffffff")
KRAKEN_SKIN = st.ramp("#8e2a36", dark=0.12, light=0.72, cool=330, warm=30)
SEA_LEATHER = st.ramp("#26404a", dark=0.08, light=0.6, cool=220)
BRONZE = st.ramp("#b07a3a", dark=0.25, light=0.92, warm=80)
TEAL = st.fixed("#0a3a44", "#126a78", "#1aa0b0", "#3ad0e0", "#8af6ff", "#d8ffff", "#ffffff")
SUCKER = st.fixed("#3a1018", "#6a2a32", "#a0605e", "#c88a82", "#e0b0a4", "#f0cec2", "#fff0e8")
BEAK = st.ramp("#2c1c22", dark=0.06, light=0.5, cool=330)


@dataclass
class Mat:
    """Die Werkstoffe einer Angel (Tonleitern)."""
    blank: tuple                    # die Rute
    grip: tuple                     # der Griff
    grip_band: tuple                # Wicklung am Griff
    fitting: tuple                  # Metall: Kappe, Rollenhalter, Ringe, Rolle
    fore: tuple                     # Vordergriff
    wrap: tuple                     # Wicklung an den Ringen
    spool_line: tuple               # die aufgewickelte Schnur
    line: tuple                     # die Schnur
    hook: tuple
    knob: tuple
    lure: str = "bobber"
    lure_a: tuple | None = None     # Köder: Hauptfarbe
    lure_b: tuple | None = None     # Köder: zweite Farbe
    gem: tuple | None = None
    spike: tuple | None = None
    tip: tuple | None = None
    accent: tuple | None = None     # Zierde (Saugnäpfe)
    blank_pattern: str = "plain"    # plain, wood, scales, sparkle, blaze, veins, blotch
    grip_pattern: str = "wrap"      # wrap, cork, string


MATS: dict[str, Mat] = {
    "fishing_rod": Mat(at.OAK, at.OAK, STRING, at.OAK, at.OAK, STRING, STRING, STRING, IRON, at.OAK, "bobber", RED, WHITE,
                       blank_pattern="wood", grip_pattern="string"),
    "iron_fishing_rod": Mat(IRON, at.DARK_LEATHER, at.LEATHER, IRON, CORK, RED, WHITE, WHITE, IRON, CORK, "bobber", RED, WHITE,
                            grip_pattern="wrap"),
    "gold_fishing_rod": Mat(at.GOLD, RED_LEATHER, at.GOLD, at.GOLD, RED_LEATHER, IVORY, WHITE, WHITE, at.GOLD, IVORY, "spoon",
                            at.GOLD, RUBY, gem=RUBY),
    "dragon_fishing_rod": Mat(at.DRACONIUM, at.DARK_LEATHER, at.DRACONIUM, at.GOLD, at.DARK_LEATHER, GOLD_THREAD, GOLD_THREAD,
                              GOLD_THREAD, at.GOLD, HORN, "scale", at.EMBER, at.DRACONIUM, gem=at.EMBER, spike=HORN,
                              blank_pattern="scales"),
    "shadow_fishing_rod": Mat(at.NIGHTSTEEL, at.DARK_LEATHER, at.NIGHTSTEEL, at.NIGHTSTEEL, at.DARK_LEATHER, at.CYAN, VIOLET_THREAD,
                              VIOLET_THREAD, at.NIGHTSTEEL, at.NIGHTSTEEL, "crystal", at.CYAN, at.NIGHTSTEEL, gem=at.CYAN,
                              blank_pattern="veins"),
    "glut_fishing_rod": Mat(BLAZE, at.CHARRED, at.EMBER, BASALT, at.CHARRED, at.EMBER, at.EMBER, at.EMBER, BASALT, at.EMBER, "ember",
                            at.EMBER, BLAZE, spike=at.EMBER, blank_pattern="blaze", grip_pattern="wrap"),
    "star_fishing_rod": Mat(STARMETAL, MIDNIGHT, at.GOLD, at.GOLD, MIDNIGHT, at.GOLD, PALE_GOLD, PALE_GOLD, at.GOLD, PALE_GOLD, "star",
                            STARLIGHT, PALE_GOLD, gem=at.MOON, tip=STARLIGHT, blank_pattern="sparkle"),
    "kraken_fishing_rod": Mat(KRAKEN_SKIN, SEA_LEATHER, BRONZE, BRONZE, SEA_LEATHER, BRONZE, TEAL, TEAL, BEAK, BRONZE, "eye",
                              TEAL, BEAK, gem=TEAL, accent=SUCKER, blank_pattern="blotch"),
}


# =============================================================================
# Malen
# =============================================================================

def blank(m: Mat):
    """Die Rute (zwei Pixel breit): links im Licht, rechts im Schatten, mit dem Muster ihres Werkstoffs."""
    def f(x, y, w, h):
        level = 5 if x == 0 else 3
        if m.blank_pattern == "wood":
            if h01(x, y // 3, 7) < 0.25:
                level -= 1
        elif m.blank_pattern == "scales":
            # Schuppen: alle drei Zeilen eine dunkle Kante, versetzt
            if (y + x) % 3 == 0:
                level -= 2
            elif (y + x) % 3 == 1 and x == 0:
                level += 1
        elif m.blank_pattern == "sparkle":
            if h01(x, y, 31) < 0.12:
                return _lv(STARLIGHT, 6)
        elif m.blank_pattern == "veins":
            if x == 0 and y % 6 in (2, 3):
                return _lv(at.CYAN, 5)
        elif m.blank_pattern == "blotch":
            # dunkle Flecken auf der Haut des Kraken
            if h01(x // 2, y // 3, 71) < 0.3:
                level -= 1
        elif m.blank_pattern == "blaze":
            level = 5 if x == 0 else 4
            if (y // 2) % 3 == 0:
                level -= 1
        return _lv(m.blank, level)
    return f


def grip(m: Mat):
    def f(x, y, w, h):
        if m.grip_pattern == "string":
            # Holz, mit Bindfaden umwickelt
            if y % 3 == 0:
                return _lv(m.grip_band, 5 if x < w - 1 else 3)
            return _lv(m.grip, 4 if x < w - 1 else 3)
        if m.grip_pattern == "cork":
            level = 4 + (1 if h01(x, y, 3) < 0.25 else 0) - (1 if h01(x, y, 4) < 0.2 else 0)
            return _lv(m.grip, level - (1 if x == w - 1 else 0))
        # Lederwicklung mit Bändern oben und unten
        if y < 2 or y >= h - 2:
            return _lv(m.grip_band, 5 if y in (0, h - 2) else 3)
        k = (y + x) % 3
        if k == 0:
            return _lv(m.grip, 1)
        return _lv(m.grip, (5 if k == 1 else 4) - (1 if x == w - 1 else 0))
    return f


def ring_metal(r):
    """Ein Ring von vorn: heller Rand, dunkles Loch."""
    def f(x, y, w, h):
        if 0 < x < w - 1 and 0 < y < h - 1:
            return _lv(r, 1)
        return _lv(r, 6 if (x == 0 or y == 0) else 4)
    return f


def spool(m: Mat):
    """Die Rolle von vorn: Rand aus Metall, innen die aufgewickelte Schnur in Ringen, in der Mitte die Achse."""
    def f(x, y, w, h):
        cx, cy = (w - 1) / 2, (h - 1) / 2
        d = max(abs(x - cx), abs(y - cy))
        if d > cx - 0.6:
            return _lv(m.fitting, 5 if (x == 0 or y == 0) else 2)
        if d < 0.8:
            return _lv(m.fitting, 6)
        return _lv(m.spool_line, 5 if int(d * 2) % 2 == 0 else 3)
    return f


def lure(m: Mat):
    a, b = m.lure_a, m.lure_b

    def f(x, y, w, h):
        cx = (w - 1) / 2
        if m.lure == "bobber":
            # oben rot, unten weiß, dazwischen ein dunkler Ring; Glanz links oben
            if y == h // 2:
                return _lv(b, 2)
            if y < h // 2:
                return _lv(a, 6 if (x == 1 and y == 1) else (5 if x < cx else 4))
            return _lv(b, 5 if x < cx else 4)
        if m.lure == "spoon":
            # goldener Löffel mit rotem Auge
            if abs(x - cx) < 0.6 and y == h // 2:
                return _lv(b, 4)
            return _lv(a, 6 if x == 0 or y == 0 else 5 if x < cx else 3)
        if m.lure == "scale":
            # eine glühende Drachenschuppe: heller Rand, dunkle Rippe
            if abs(x - cx) < 0.6:
                return _lv(a, 3)
            return _lv(a, 6 if y == 0 or x in (0, w - 1) else 5)
        if m.lure == "crystal":
            d = abs(x - cx) + abs(y - (h - 1) / 2)
            return _lv(a, 6 if d < 1.2 else 5 if d < 2.6 else 3)
        if m.lure == "ember":
            d = max(abs(x - cx), abs(y - (h - 1) / 2))
            return _lv(a, 6 if d < 1 else 5 if d < 2 else 4)
        if m.lure == "star":
            d = max(abs(x - cx), abs(y - (h - 1) / 2))
            return _lv(a, 6 if d < 1.2 else 5)
        if m.lure == "eye":
            # ein leuchtendes Auge mit waagrechter, dunkler Schlitzpupille
            cy = (h - 1) / 2
            if abs(y - cy) < 0.6 and abs(x - cx) < 2.1:
                return _lv(b, 1)
            d = max(abs(x - cx), abs(y - cy))
            return _lv(a, 6 if d < 1.2 else 5 if d < 2.2 else 3)
        return _lv(a, 4)
    return f


def lure_top(m: Mat):
    def f(x, y, w, h):
        if m.lure == "bobber":
            return _lv(m.lure_a, 5)
        return _lv(m.lure_b if m.lure in ("spoon",) else m.lure_a, 5 if x < w / 2 else 4)
    return f


def atlas(name: str) -> tuple[Image.Image, Image.Image]:
    m = MATS[name]
    R = models.REGIONS
    img, _ = at._blank()
    paint(img, R["butt"], at.metal(m.fitting, 4.0))
    paint(img, R["butt_top"], at.top(m.fitting))
    paint(img, R["grip"], grip(m))
    paint(img, R["grip_side"], grip(m))
    paint(img, R["seat"], at.metal(m.fitting, 4.0, rivets=True))
    paint(img, R["seat_top"], at.top(m.fitting))
    if m.grip_pattern == "string":
        paint(img, R["fore"], at.wood(m.fore, 5))
    else:
        paint(img, R["fore"], lambda x, y, w, h: _lv(m.fore, (5 if x == 0 else 4 if x < w - 1 else 3) - (1 if y % 4 == 3 else 0)))
    paint(img, R["blank"], blank(m))
    paint(img, R["blank_side"], blank(m))
    paint(img, R["wrap"], lambda x, y, w, h: _lv(m.wrap, 5 if x == 0 else 4 if y % 2 else 3))
    paint(img, R["guide"], ring_metal(m.fitting))
    paint(img, R["spool"], spool(m))
    paint(img, R["spool_side"], lambda x, y, w, h: _lv(m.spool_line, 4 if x % 2 else 5) if 0 < y < h - 1 else _lv(m.fitting, 3))
    paint(img, R["reel"], at.metal(m.fitting, 4.0))
    paint(img, R["reel_side"], at.metal(m.fitting, 3.0))
    paint(img, R["crank"], at.metal(m.fitting, 4.5))
    paint(img, R["knob"], lambda x, y, w, h: _lv(m.knob, 5 if x == 0 or y == 0 else 3 if x == w - 1 or y == h - 1 else 4))
    paint(img, R["knob_top"], at.top(m.knob))
    paint(img, R["line"], lambda x, y, w, h: _lv(m.line, 5 if x == 0 else 4))
    paint(img, R["hook"], lambda x, y, w, h: _lv(m.hook, 5 if x == 0 else 3))
    paint(img, R["lure"], lure(m))
    paint(img, R["lure_top"], lure_top(m))
    if m.gem:
        paint(img, R["gem"], at.glow_fill(m.gem, 5.0))
    if m.spike:
        paint(img, R["spike"], at.strand(m.spike, 6.0, 2.5, True, 11))
        paint(img, R["spike_edge"], at.strand(m.spike, 5.0, 2.0, False, 12))
    if m.accent:
        paint(img, R["accent"], lambda x, y, w, h: _lv(m.accent, 6 if (x + y) % 3 == 0 else 4 if x < w - 1 else 3))
    if m.tip:
        paint(img, R["tip"], at.glow_fill(m.tip, 5.0))
        paint(img, R["tip_side"], lambda x, y, w, h: _lv(m.tip, 5))
    # die leuchtenden Teile nehmen ihre Farbe aus dem Leuchtatlas – hier derselbe Atlas
    return img, img.copy()


def build(put) -> None:
    """Für tools/generate_textures.py: die Atlanten aller Angeln."""
    for spec in models.SPECS.values():
        img, glow = atlas(spec.name)
        put("item/" + spec.name + "_model", img)
        put("item/" + spec.name + "_model_glow", glow)


def main() -> None:
    for spec in models.SPECS.values():
        img, glow = atlas(spec.name)
        img.save(os.path.join(OUT, spec.name + "_model.png"))
        glow.save(os.path.join(OUT, spec.name + "_model_glow.png"))
    print(f"{len(models.SPECS)} Atlanten geschrieben")


if __name__ == "__main__":
    main()
