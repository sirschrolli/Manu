"""
Lindwurm: ein flügelloser Drache mit langem Schlangenleib, zwei kräftigen Vorderbeinen, gehörntem Kopf
und Rückenstacheln. Der Leib besteht aus acht Gliedern, die sich beim Kriechen wellenförmig bewegen.

Knochen-Namen werden in LindwormModel (Java) für die Animationen verwendet.
"""
from __future__ import annotations

from ..geometry import Model
from ..paint import Ctx, eye_painter, flat_painter, gradient_painter, ramp, scales_painter

PALETTES = {
    "lindworm": {
        "back": ramp("#16200e", "#7f9446"), "belly": ramp("#5e4e20", "#e8d68c"), "horn": ramp("#241c14", "#c4b494"),
        "claw": ramp("#1c1814", "#d0c4ac"), "eye": ramp("#8a2a00", "#ffcc33"), "accent": ramp("#3a4a1a", "#b8c86a"),
    },
}

# (Breite, Höhe, Länge, Neigung) der Leibglieder vom Brustkorb bis zur Schwanzspitze
SEGMENTS = [(8, 7, 10, -14), (7.5, 6.5, 10, -8), (7, 6, 9, 6), (6, 5.5, 9, 6),
            (5.5, 5, 8, 5), (4.5, 4, 8, 4), (3.5, 3.5, 7, 3), (2.5, 2.5, 7, 2)]


def build() -> Model:
    m = Model("lindworm", 128, 128)
    m.bone("body", pivot=(0, 16, 0))
    m.cube("body", (-4.5, -4, -6), (9, 8, 12), "scales")
    for i, z in enumerate((-4, 1)):
        m.cube("body", (-0.75, -6.5 + i * 0.5, z), (1.5, 2.5 - i * 0.5, 2.5), "spike")

    # Hals und Kopf
    m.bone("neck", "body", pivot=(0, -2, -6), rotation=(-30, 0, 0))
    m.cube("neck", (-3, -3, -7), (6, 6, 8), "scales")
    m.cube("neck", (-0.6, -5, -5), (1.2, 2, 2.5), "spike")
    m.bone("head", "neck", pivot=(0, 0, -6.5), rotation=(30, 0, 0))
    m.cube("head", (-3.5, -3.5, -7), (7, 6, 7), "scales")
    m.cube("head", (-2.5, -2, -12), (5, 3.5, 5.5), "scales")
    m.cube("head", (-3.5, -4.2, -6), (7, 1, 4), "scales")             # Stirnwulst
    m.cube("head", (-2, 1.5, -11.5), (4, 1, 4.5), "teeth")
    m.cube("head", (3.2, -2.2, -5.5), (0.5, 1.2, 1.8), "eye")
    m.cube("head", (-3.7, -2.2, -5.5), (0.5, 1.2, 1.8), "eye")
    m.bone("jaw", "head", pivot=(0, 2.5, -6))
    m.cube("jaw", (-2.5, 0, -6), (5, 1.5, 6.5), "scales")
    m.cube("jaw", (-2, -0.8, -5.5), (4, 1, 4), "teeth")
    for side in (1, -1):
        name = "left_horn" if side > 0 else "right_horn"
        m.bone(name, "head", pivot=(2.4 * side, -3.6, -1.5), rotation=(25, 18 * side, 0))
        m.cube(name, (-1, -1, 0), (2, 2, 7), "horn")

    # Vorderbeine
    for side in (1, -1):
        name = "left_leg" if side > 0 else "right_leg"
        m.bone(name, "body", pivot=(4.8 * side, 2, -3))
        m.cube(name, (-1.5, -1.5, -1.5), (3, 6, 3), "scales")
        m.cube(name, (-2, 4.5, -3.5), (4, 1.5, 5), "scales")
        for cx in (-1.75, -0.25, 1.25):
            m.cube(name, (cx, 5, -4.5), (0.5, 1, 1.5), "claw")

    # Leib bis zur Schwanzspitze
    parent, pivot = "body", (0, 0.5, 5.5)
    for i, (w, h, length, tilt) in enumerate(SEGMENTS, start=1):
        name = f"segment{i}"
        m.bone(name, parent, pivot=pivot, rotation=(tilt, 0, 0))
        m.cube(name, (-w / 2, -h / 2, 0), (w, h, length), "scales")
        if i <= 6:
            m.cube(name, (-0.6, -h / 2 - 2, length * 0.4), (1.2, 2, 2.5), "spike")
        parent, pivot = name, (0, 0, length - 0.8)
    m.cube("segment8", (-2.5, -0.25, 6), (5, 0.5, 4), "tailtip")
    return m


def _tail_tip(ctx: Ctx):
    lx, ly, lz = ctx.local
    ox, oy, oz = ctx.cube.origin
    w, h, d = ctx.cube.size
    u = (lx - ox) / w * 2 - 1
    v = (lz - oz) / d
    if abs(u) > (1 - v) * 1.0:
        return None                          # Pfeilspitze
    return ctx.pal["horn"][3 if abs(u) < 0.3 else 2]


def materials(variant: str) -> dict:
    return {
        "scales": scales_painter(cell=2.0, seed=71, belly_from=0.68),
        "spike": gradient_painter("horn", 0.3, 0.85),
        "horn": gradient_painter("horn", 0.25, 0.9, rings=4),
        "claw": flat_painter("claw", 0.75),
        "teeth": flat_painter("claw", 0.92, 0.04),
        "eye": eye_painter("eye"),
        "tailtip": _tail_tip,
    }
