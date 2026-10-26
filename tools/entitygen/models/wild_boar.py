"""
Wildschwein: gedrungener Körper mit hohem Widerrist, Borstenkamm, Hauern und kurzem Ringelschwanz.
Frischlinge (Jungtiere) haben helle Längsstreifen.

Knochen-Namen werden in WildBoarModel (Java) für die Animationen verwendet.
"""
from __future__ import annotations

import math

from ..geometry import Model
from ..paint import Ctx, eye_painter, flat_painter, hash01, mix, pick, ramp, rel_y, soft_fur_painter

PALETTES = {
    "wild_boar": {
        "fur": ramp("#1f1712", "#7d6650"), "belly": ramp("#3a2c22", "#8f7a64"), "snout": ramp("#3b2a26", "#9a7a6e"),
        "tusk": ramp("#8f846c", "#fff8e6"), "hoof": ramp("#141010", "#4a403a"), "eye": ramp("#2a1408", "#c88a3a"),
        "bristle": ramp("#0e0b09", "#5a4a3c"), "stripe": ramp("#6e5a40", "#d8c49a"), "back": ramp("#1f1712", "#7d6650"),
    },
    "wild_boar_baby": {
        "fur": ramp("#4a3020", "#a8805a"), "belly": ramp("#6a5038", "#c8a880"), "snout": ramp("#5a3a30", "#c09080"),
        "tusk": ramp("#5a3a30", "#c09080"), "hoof": ramp("#201814", "#5a4a40"), "eye": ramp("#2a1408", "#c88a3a"),
        "bristle": ramp("#3a2818", "#8a6a48"), "stripe": ramp("#8a7050", "#f0dcae"), "back": ramp("#4a3020", "#a8805a"),
    },
}


def build() -> Model:
    m = Model("wild_boar", 128, 64)
    m.bone("body", pivot=(0, 12, 0))
    m.cube("body", (-4.5, -5, -9), (9, 9, 17), "fur")
    m.cube("body", (-3.5, -6.5, -9), (7, 2, 8), "fur")                 # Widerrist (hohe Schultern)
    m.cube("body", (0, -10, -9.5), (0, 4, 13), "bristle")              # Borstenkamm

    m.bone("head", "body", pivot=(0, -1, -9), rotation=(12, 0, 0))
    m.cube("head", (-3.5, -3.5, -6.5), (7, 7, 7), "fur")
    m.cube("head", (-2, 0, -9.5), (4, 3, 3), "snout")                  # Rüssel
    m.cube("head", (-2.6, 0.2, -9), (1, 2.5, 1), "tusk")               # Hauer
    m.cube("head", (1.6, 0.2, -9), (1, 2.5, 1), "tusk")
    m.cube("head", (3.4, -1.5, -5.5), (0.5, 1, 1), "eye")
    m.cube("head", (-3.9, -1.5, -5.5), (0.5, 1, 1), "eye")
    for side in (1, -1):
        name = "ear_l" if side > 0 else "ear_r"
        m.bone(name, "head", pivot=(2.6 * side, -3.2, -2), rotation=(-15, 0, 25 * side))
        m.cube(name, (-1, -3, -0.5), (2, 3, 1), "fur")

    for name, x, z in (("front_left_leg", 2.5, -6), ("front_right_leg", -2.5, -6),
                       ("hind_left_leg", 2.5, 5.5), ("hind_right_leg", -2.5, 5.5)):
        m.bone(name, pivot=(x, 16, z))
        m.cube(name, (-1.5, 0, -1.5), (3, 8, 3), "leg")

    m.bone("tail", "body", pivot=(0, -3.5, 8), rotation=(35, 0, 0))
    m.cube("tail", (-0.5, 0, 0), (1, 4, 1), "fur")
    return m


def _stripes(base):
    """Frischlinge: helle Längsstreifen auf Rücken und Flanken."""

    def paint(ctx: Ctx):
        col = base(ctx)
        if ctx.params.get("variant") != "wild_boar_baby" or ctx.face == "bottom":
            return col
        x, y, z = ctx.world
        coord = (y if ctx.face in ("west", "east") else x) + math.sin(z * 0.35) * 0.6
        if ctx.cube.size[2] > 12 and (coord % 3.0) < 1.0 and rel_y(ctx) < 0.75:
            return mix(col, pick(ctx.pal["stripe"], 0.7), 0.75)
        return col

    return paint


def _leg(ctx: Ctx):
    if rel_y(ctx) > 0.82 or ctx.face == "bottom":
        return pick(ctx.pal["hoof"], 0.4 + hash01(ctx.u, ctx.v) * 0.2)
    return soft_fur_painter("fur", None, top_dark=0.0, seed=21)(ctx)


def _snout(ctx: Ctx):
    if ctx.face == "front":
        # Rüsselscheibe mit zwei Nasenlöchern
        if ctx.t == 1 and ctx.s in (1, 2):
            return pick(ctx.pal["snout"], 0.08)
        return pick(ctx.pal["snout"], 0.75 if ctx.t == 0 else 0.62)
    return pick(ctx.pal["snout"], 0.45 + hash01(ctx.u, ctx.v) * 0.1)


def _bristle(ctx: Ctx):
    lx, ly, lz = ctx.local
    oy = ctx.cube.origin[1]
    h = ctx.cube.size[1]
    rel = (ly - oy) / h                      # 0 oben, 1 unten
    jag = 0.25 + 0.3 * abs(math.sin(lz * 1.7)) + hash01(int(lz * 2)) * 0.2
    if rel < jag:
        return None
    return pick(ctx.pal["bristle"], 0.25 + (1 - rel) * 0.5)


def materials(variant: str) -> dict:
    return {
        "fur": _stripes(soft_fur_painter("fur", "belly", belly_from=0.7, contrast=0.26, strokes=0.08, seed=7)),
        "leg": _leg,
        "snout": _snout,
        "tusk": flat_painter("tusk", 0.8, 0.1) if variant != "wild_boar_baby" else (lambda ctx: None),
        "eye": eye_painter("eye", glow=False),
        "bristle": _bristle,
    }
