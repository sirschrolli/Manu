"""
Lavastelzer (statt Schreiter) auf dem Skelett des Vanilla-Schreiters: dieselben Knochen und Drehpunkte wie
AdultStriderModel (body mit den sechs Haarlappen, zwei Beine), damit Stelzen, Wippen und das Wehen der Haare genauso
laufen und der Vanilla-Sattel passt (der Rumpf hat die Maße des Vanilla-Rumpfs). Eigene Quader und Bilder:

  * ein runder, warziger Rumpf aus warmer Lavahaut – Buckel mit dunklen Rändern, dazwischen glimmende Poren
  * ein Gesicht mit Stirnwulst, großen, glühenden Augen und einem breiten Maul mit kleinen Zähnen, oben vier Höcker
  * sechs Lappen aus langem, feurigem Haar, deren Spitzen glimmen
  * lange Stelzbeine mit Kniebuckel und breiten Füßen, unten glühend heiß vom Waten in der Lava

Gestalten: Glutstelzer (feuerrot) und Seelenstelzer (im Seelensandtal, türkis wie Seelenfeuer). Außerhalb der Lava
friert er: Haut fahl violett- bzw. schiefergrau, nichts glimmt mehr, die Augen halb geschlossen (eigene „_kalt“-Bilder).
"""
from __future__ import annotations

import math

from ..geometry import Model
from ..paint import Ctx, Voronoi3D, hash01, hexc, mix, noise3, pick, ramp

_COMMON = {
    "mouth": hexc("#160404"), "teeth": ramp("#8a7a68", "#fff6e0", steps=4), "pupil": hexc("#1a0604"),
}

PALETTES = {
    "glut": {**_COMMON, "skin": ramp("#3a0604", "#ff7454", steps=10, hue_shift=-0.03),
             "hair": ramp("#3a0604", "#e8442a", steps=8), "tip": ramp("#ff7a2a", "#ffe08a", steps=5),
             "tip_glow": hexc("#ff8a30"), "pore": hexc("#ffb050"), "eye": ramp("#e89a20", "#fff4a0", steps=4),
             "eye_glow": hexc("#ffd060"), "heat": ramp("#a82a08", "#ffe070", steps=6), "heat_glow": hexc("#ff7020"),
             "foot": ramp("#2a0804", "#8a2a14", steps=5)},
    "glut_kalt": {**_COMMON, "skin": ramp("#1e1220", "#a88aac", steps=10, hue_shift=0.02),
                  "hair": ramp("#1c1420", "#8a7090", steps=8), "tip": ramp("#9a88a0", "#d8ccdc", steps=5),
                  "tip_glow": None, "pore": None, "eye": ramp("#8a7a50", "#c8bc90", steps=4), "eye_glow": None,
                  "heat": ramp("#3a2a3c", "#7a6a7e", steps=6), "heat_glow": None,
                  "foot": ramp("#120c14", "#4a3a4e", steps=5), "cold": True},
    "seele": {**_COMMON, "skin": ramp("#041a20", "#6ae8e8", steps=10, hue_shift=0.02),
              "hair": ramp("#02242a", "#38c8d8", steps=8), "tip": ramp("#5ae8f0", "#e0ffff", steps=5),
              "tip_glow": hexc("#50e0f0"), "pore": hexc("#80f8ff"), "eye": ramp("#40c8d8", "#e8ffff", steps=4),
              "eye_glow": hexc("#80f0ff"), "heat": ramp("#0a6a78", "#a0ffff", steps=6), "heat_glow": hexc("#30c0d0"),
              "foot": ramp("#021014", "#145058", steps=5)},
    "seele_kalt": {**_COMMON, "skin": ramp("#14161e", "#8a94ac", steps=10, hue_shift=0.02),
                   "hair": ramp("#12141a", "#6a7488", steps=8), "tip": ramp("#7a8498", "#c8d0dc", steps=5),
                   "tip_glow": None, "pore": None, "eye": ramp("#6a7a80", "#b0bcc0", steps=4), "eye_glow": None,
                   "heat": ramp("#262a34", "#5a6070", steps=6), "heat_glow": None,
                   "foot": ramp("#0c0e12", "#3a4050", steps=5), "cold": True},
}

LOOKS = ("glut", "seele")

_BRISTLES = (("right_bottom_bristle", (-8, 4, -8), -70), ("right_middle_bristle", (-8, -1, -8), -65),
             ("right_top_bristle", (-8, -5, -8), -50), ("left_top_bristle", (8, -6, -8), 50),
             ("left_middle_bristle", (8, -2, -8), 65), ("left_bottom_bristle", (8, 3, -8), 70))


def build() -> Model:
    m = Model("strider", 128, 128)
    m.bone("body", pivot=(0, 1, 0))
    m.cube("body", (-8, -6, -8), (16, 14, 16), "body")                       # Maße wie Vanilla (Sattel!)
    m.cube("body", (-7.4, -4.4, -8.9), (14.8, 1.4, 1.2), "brow")             # Stirnwulst
    for x, z in ((-6.8, -6.8), (4.4, -6.8), (-6.8, 4.4), (4.4, 4.4)):        # vier Höcker an den Ecken
        m.cube("body", (x, -7.6, z), (2.4, 1.6, 2.4), "bump")
    for name, pivot, rz in _BRISTLES:
        m.bone(name, "body", pivot=pivot, rotation=(0, 0, rz))
        ox = -12 if rz < 0 else 0
        m.cube(name, (ox, 0, 0), (12, 0, 16), "hair")
    for side, x in (("right", -4.0), ("left", 4.0)):
        leg = f"{side}_leg"
        m.bone(leg, pivot=(x, 8, 0))
        m.cube(leg, (-2, 0, -2), (4, 16, 4), "leg")
        m.cube(leg, (-2.5, 5.6, -2.5), (5, 2.4, 5), "knee")
        m.cube(leg, (-2.8, 14.6, -2.8), (5.6, 1.4, 5.6), "foot")
    return m


# =============================================================================================
# Bemalen
# =============================================================================================

_LIT = {"top": 0.08, "front": 0.03, "west": 0.0, "east": -0.03, "back": -0.05, "bottom": -0.18}
_WARTS = Voronoi3D(3.2, 77)


def _cold(ctx: Ctx) -> bool:
    return bool(ctx.pal.get("cold"))


def _skin(ctx: Ctx, base: float = 0.55, pores: bool = True):
    """Lavahaut: runde Buckel (in der Mitte heller, am Rand dunkel), dazwischen vereinzelt glimmende Poren."""
    x, y, z = ctx.world
    cell, edge = _WARTS(x, y, z)
    v = base + _LIT[ctx.face] + (cell - 0.5) * 0.12 + (edge - 0.5) * 0.22
    v += (noise3(x * 0.2, y * 0.2, z * 0.2, 5) - 0.5) * 0.08
    if edge < 0.12:
        v -= 0.12                                                                # Furche zwischen den Buckeln
        pore = ctx.pal.get("pore")
        if pores and pore is not None and hash01(math.floor(x * 2), math.floor(y * 2), math.floor(z * 2), "pore") > 0.86:
            ctx.glow = mix(hexc("#000000"), pore, 0.8)
            return pore
    return pick(ctx.pal["skin"], v)


def _face(ctx: Ctx):
    """Gesicht (Vorderseite 16 x 14): große Augen unter dem Wulst, breites Maul mit kleinen Zähnen."""
    s, t = ctx.s, ctx.t
    cold = _cold(ctx)
    for cx in (4, 11):
        if 2 <= t <= 4 and cx - 1 <= s <= cx + 1:
            if cold and t == 2:
                return _skin(ctx, 0.42, pores=False)                                          # halb geschlossen
            if s == cx and t == (3 if not cold else 4):
                return ctx.pal["pupil"]
            col = pick(ctx.pal["eye"], 0.8 if t <= 3 else 0.5)
            glow = ctx.pal.get("eye_glow")
            if glow is not None:
                ctx.glow = glow
            return col
        if t == 5 and cx - 1 <= s <= cx + 1:
            return _skin(ctx, 0.36, pores=False)                                              # Schatten unter dem Auge
    if 8 <= t <= 10 and 3 <= s <= 12:
        edge = s in (3, 12)
        if t == 8 and not edge:
            if s % 2 == 0:
                return pick(ctx.pal["teeth"], 0.85)                              # obere Zähnchen
            return ctx.pal["mouth"]
        if t == 10 and (edge or s in (4, 11)):
            return _skin(ctx, 0.38, pores=False)                                              # Mundwinkel
        if t == 10 and s % 3 == 1 and not cold:
            return pick(ctx.pal["teeth"], 0.6)                                   # untere Zähnchen
        return ctx.pal["mouth"]
    if t == 11 and 4 <= s <= 11:
        return _skin(ctx, 0.62, pores=False)                                                  # Unterlippe
    return _skin(ctx, 0.56, pores=False)


def _body(ctx: Ctx):
    if ctx.face == "front":
        return _face(ctx)
    if ctx.face == "bottom":
        return _skin(ctx, 0.4)
    return _skin(ctx, 0.55)


def _brow(ctx: Ctx):
    if ctx.face == "bottom":
        return _skin(ctx, 0.3)
    return _skin(ctx, 0.5)


def _bump(ctx: Ctx):
    if ctx.face == "bottom":
        return None
    return _skin(ctx, 0.62 if ctx.face == "top" else 0.5)


def _hair(ctx: Ctx):
    """
    Haarlappen: lange, feurige Strähnen vom Rumpf nach außen (eine je Zeile), ungleich lang, die Spitzen hell und
    glimmend; beim frierenden Lavastelzer fahl und ohne Glut.
    """
    lx, ly, lz = ctx.local
    out = abs(lx) / 12.0                                                         # 0 am Rumpf, 1 an der Spitze
    strand = math.floor(lz + 8.0)
    name = ctx.cube.bone.name
    length = 0.72 + 0.28 * hash01(strand, name, "len")
    if hash01(strand, name, "gap") > 0.9:
        length *= 0.6
    if out > length:
        return None
    tip = (length - out) / max(length, 1e-3)                                     # 0 an der Spitze
    shade = (hash01(strand, name, "sh") - 0.5) * 0.18 + (0.06 if strand % 2 else 0.0)
    if tip < 0.16:
        glow = ctx.pal.get("tip_glow")
        if glow is not None:
            ctx.glow = mix(hexc("#000000"), glow, 0.85 - tip * 2.5)
        return pick(ctx.pal["tip"], 0.72 - tip * 2.0 + shade)
    return pick(ctx.pal["hair"], 0.3 + (1.0 - tip) * 0.42 + shade - (0.08 if ctx.face == "bottom" else 0.0))


def _heat(ctx: Ctx, k: float):
    """Unten am Bein: glühend heiß vom Waten in der Lava (k = 0 oben … 1 unten)."""
    col = pick(ctx.pal["heat"], 0.25 + k * 0.7 + (hash01(ctx.u, ctx.v, "ht") - 0.5) * 0.1)
    glow = ctx.pal.get("heat_glow")
    if glow is not None and k > 0.35:
        ctx.glow = mix(hexc("#000000"), glow, min(1.0, (k - 0.35) * 1.6))
    return col


def _leg(ctx: Ctx):
    t, h = ctx.t, ctx.h
    k = t / max(h - 1, 1)
    if ctx.face in ("top", "bottom"):
        return _skin(ctx, 0.4)
    if k > 0.62:
        return _heat(ctx, (k - 0.62) / 0.38)
    rings = 0.05 if t % 3 == 0 else 0.0
    return _skin(ctx, 0.5 - k * 0.12 - rings)


def _knee(ctx: Ctx):
    if ctx.face == "bottom":
        return _skin(ctx, 0.3)
    return _skin(ctx, 0.6 if ctx.face == "top" else 0.5)


def _foot(ctx: Ctx):
    if ctx.face == "top":
        return _heat(ctx, 0.75)
    if ctx.face == "bottom":
        return pick(ctx.pal["foot"], 0.35)
    split = ctx.face in ("front", "back") and ctx.s in (ctx.w // 3, 2 * ctx.w // 3)
    return pick(ctx.pal["foot"], 0.55 - (0.25 if split else 0.0) + _LIT[ctx.face])   # dreizehige Füße


def materials(variant: str) -> dict:
    return {"body": _body, "brow": _brow, "bump": _bump, "hair": _hair, "leg": _leg, "knee": _knee, "foot": _foot}


def hidden(variant: str) -> list[str]:
    return []
