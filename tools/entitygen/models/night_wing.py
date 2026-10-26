"""
Nachtschwinge: ein schwarzvioletter Flugjäger mit Fledermausflügeln, spitzen Ohren, langem Schwanz mit Hautsegel und
rot glühenden Augen. Kreist hoch über den Schattengipfeln und stößt auf Beute herab.

Knochen-Namen werden in NightWingModel (Java) für die Animationen verwendet (wie das Phantom-Modell).
"""
from __future__ import annotations

from ..geometry import Model
from ..paint import Ctx, hash01, membrane_painter, mix, noise3, pick, ramp, rel_y

PALETTES = {
    "night_wing": {
        "skin": ramp("#0a0812", "#3e2e58"), "membrane": ramp("#140c22", "#5a3e86"), "back": ramp("#0a0812", "#3e2e58"),
        "eye": ramp("#d01030", "#ffd0d8"), "claw": ramp("#4a4458", "#c8c2d6"),
    },
}


def build() -> Model:
    m = Model("night_wing", 128, 64)
    m.bone("body", pivot=(0, 0, 0))
    m.cube("body", (-3, -2, -6), (6, 4, 11), "skin")
    m.cube("body", (-2, -3, -5), (4, 1, 8), "skin")                 # Rückenkamm

    m.bone("head", "body", pivot=(0, -0.5, -6), rotation=(8, 0, 0))
    m.cube("head", (-2.5, -2, -4.5), (5, 4, 5), "skin")
    m.cube("head", (-1.5, -0.5, -7.5), (3, 2, 3), "skin")            # Schnauze
    m.cube("head", (-2, -1, -4.8), (1, 1, 0.5), "eye")
    m.cube("head", (1, -1, -4.8), (1, 1, 0.5), "eye")
    m.cube("head", (-1.2, 1.5, -7.2), (0.5, 1, 0.5), "claw")         # Zähne
    m.cube("head", (0.7, 1.5, -7.2), (0.5, 1, 0.5), "claw")
    for side in (1, -1):
        ear = "ear_l" if side > 0 else "ear_r"
        m.bone(ear, "head", pivot=(1.8 * side, -2, -2), rotation=(-20, 0, 18 * side))
        m.cube(ear, (-0.5, -4, -0.5), (1, 4, 2), "skin")

    for side in (1, -1):
        wing = "left_wing_base" if side > 0 else "right_wing_base"
        tip = "left_wing_tip" if side > 0 else "right_wing_tip"
        m.bone(wing, "body", pivot=(3 * side, -1.5, -4))
        m.cube(wing, (0 if side > 0 else -11, 0, 0), (11, 1, 9), "wing")
        m.bone(tip, wing, pivot=(11 * side, 0, 0))
        m.cube(tip, (0 if side > 0 else -11, 0, 0), (11, 1, 7), "wing")

    m.bone("tail_base", "body", pivot=(0, -0.5, 5))
    m.cube("tail_base", (-1.5, -1, 0), (3, 2, 7), "skin")
    m.bone("tail_tip", "tail_base", pivot=(0, 0, 7))
    m.cube("tail_tip", (-1, -0.5, 0), (2, 1, 7), "skin")
    m.cube("tail_tip", (-3, 0, 4), (6, 0.5, 4), "wing")             # Hautsegel am Ende
    return m


def _skin(ctx: Ctx):
    x, y, z = ctx.world
    n = noise3(x * 0.5, y * 0.5, z * 0.5, 91)
    v = 0.3 + (n - 0.5) * 0.3 + (0.12 if ctx.face == "top" else 0.0) - (0.1 if ctx.face == "bottom" else 0.0)
    col = pick(ctx.pal["skin"], v)
    if ctx.face == "top" and hash01(ctx.u, ctx.v, 92) > 0.92:
        col = pick(ctx.pal["membrane"], 0.6)
    return col


def _wing(ctx: Ctx):
    base = membrane_painter("membrane", "skin", spacing=4.5)(ctx)
    if ctx.face == "bottom":
        base = mix(base, pick(ctx.pal["skin"], 0.2), 0.4)
    # Löcher und Risse an der Hinterkante
    d = (ctx.local[2] - ctx.cube.origin[2]) / max(ctx.cube.size[2], 1e-3)
    if d > 0.88 and hash01(ctx.u, 93) > 0.6:
        return None
    return base


def _eye(ctx: Ctx):
    col = pick(ctx.pal["eye"], 0.8)
    ctx.glow = col
    return col


def materials(variant: str) -> dict:
    return {
        "skin": _skin,
        "wing": _wing,
        "eye": _eye,
        "claw": lambda ctx: pick(ctx.pal["claw"], 0.7),
    }
