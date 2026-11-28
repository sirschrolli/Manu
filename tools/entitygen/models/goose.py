"""
Gans: rundlicher Körper, langer Hals, orangefarbener Schnabel und Füße mit Schwimmhäuten.
Zwei Arten: Graugans und weiße Hausgans.

Knochen-Namen werden in GooseModel (Java) für die Animationen verwendet.
"""
from __future__ import annotations

from ..geometry import Model
from ..paint import Ctx, eye_painter, feather_painter, flat_painter, hash01, pick, ramp, rel_y

PALETTES = {
    "goose_grey": {
        "feather": ramp("#3e3a36", "#c8c0b4"), "neck": ramp("#4a4440", "#b0a89c"), "wingtip": ramp("#1e1c1c", "#6a6460"),
        "belly": ramp("#a8a094", "#f0ebe2"), "beak": ramp("#a0400c", "#ffa040"), "leg": ramp("#9a4a3a", "#f0a090"),
        "eye": ramp("#100a08", "#3a2a20"), "back": ramp("#3e3a36", "#c8c0b4"),
    },
    "goose_white": {
        "feather": ramp("#a8acb4", "#ffffff"), "neck": ramp("#b4b8c0", "#ffffff"), "wingtip": ramp("#9aa0aa", "#e8ecf0"),
        "belly": ramp("#c0c4cc", "#ffffff"), "beak": ramp("#b8500c", "#ffb040"), "leg": ramp("#b05010", "#ffa040"),
        "eye": ramp("#100a08", "#3a5a8a"), "back": ramp("#a8acb4", "#ffffff"),
    },
}


def build() -> Model:
    m = Model("goose", 64, 64)
    m.bone("body", pivot=(0, 17, 0))
    m.cube("body", (-3, -3, -5), (6, 6, 10), "body")

    m.bone("neck", "body", pivot=(0, -2, -4), rotation=(12, 0, 0))
    m.cube("neck", (-1, -8, -1.5), (2, 8, 2), "neck")
    m.bone("head", "neck", pivot=(0, -7.5, -0.5), rotation=(-12, 0, 0))
    m.cube("head", (-1.5, -2.5, -2.5), (3, 3, 4), "neck")
    m.cube("head", (-1, -1.5, -5), (2, 1.5, 2.5), "beak")
    m.cube("head", (1.3, -2, -1.8), (0.5, 1, 1), "eye")
    m.cube("head", (-1.8, -2, -1.8), (0.5, 1, 1), "eye")

    for side in (1, -1):
        name = "left_wing" if side > 0 else "right_wing"
        m.bone(name, "body", pivot=(3 * side, -2, -3))
        m.cube(name, (0 if side > 0 else -1, 0, 0), (1, 4, 8), "wing")

    m.bone("tail", "body", pivot=(0, -2.5, 5), rotation=(-25, 0, 0))
    m.cube("tail", (-2, -1, 0), (4, 2, 3), "wing")

    for side in (1, -1):
        name = "left_leg" if side > 0 else "right_leg"
        m.bone(name, pivot=(1.5 * side, 20, 0.5))
        m.cube(name, (-0.5, 0, -0.5), (1, 4, 1), "leg")
        m.cube(name, (-1, 3.5, -2.5), (2, 0.5, 3), "leg")       # Fuß mit Schwimmhaut
    return m


def _body(ctx: Ctx):
    if ctx.face == "bottom" or (rel_y(ctx) > 0.6 and ctx.face in ("front", "west", "east")):
        return pick(ctx.pal["belly"], 0.55 + hash01(ctx.u, ctx.v) * 0.15)
    return feather_painter("feather", rows=1.5, seed=51)(ctx)


def _neck(ctx: Ctx):
    if ctx.face == "front" and ctx.cube.size[1] > 6:
        return pick(ctx.pal["belly"], 0.6)                       # heller Vorderhals
    return pick(ctx.pal["neck"], 0.45 + hash01(ctx.u, ctx.v) * 0.12 + (0.1 if ctx.face == "top" else 0.0))


def _wing(ctx: Ctx):
    return feather_painter("feather", rows=1.2, seed=52, tip="wingtip")(ctx)


def _beak(ctx: Ctx):
    if ctx.face == "front" or (ctx.face == "top" and ctx.t == ctx.h - 1):
        return pick(ctx.pal["beak"], 0.4)                        # Nagel an der Schnabelspitze
    return pick(ctx.pal["beak"], 0.7 if ctx.face == "top" else 0.55)


def materials(variant: str) -> dict:
    return {
        "body": _body,
        "neck": _neck,
        "wing": _wing,
        "beak": _beak,
        "eye": eye_painter("eye", glow=False),
        "leg": flat_painter("leg", 0.55, 0.1),
    }
