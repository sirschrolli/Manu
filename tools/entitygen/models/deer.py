"""
Rothirsch: schlanker Körper, langer Hals, große Ohren, weißer Spiegel am Hinterteil.
Hirsche tragen ein Geweih (im Java-Modell nur bei männlichen Tieren sichtbar), Kitze haben weiße Tupfen.

Knochen-Namen werden in DeerModel (Java) für die Animationen verwendet.
"""
from __future__ import annotations

from ..geometry import Model
from ..paint import Ctx, eye_painter, fur_painter, hash01, mix, noise3, pick, ramp, rel_y

PALETTES = {
    "deer": {
        "fur": ramp("#4a2a16", "#c0864e"), "belly": ramp("#a88a66", "#f0e2c8"), "white": ramp("#c8c0b0", "#ffffff"),
        "nose": ramp("#0e0a0a", "#4a3a34"), "hoof": ramp("#141010", "#4a403a"), "eye": ramp("#100806", "#5a3a20"),
        "antler": ramp("#5a4630", "#efe2c4"), "back": ramp("#4a2a16", "#c0864e"),
    },
    "deer_fawn": {
        "fur": ramp("#5a321a", "#d29a5e"), "belly": ramp("#b09070", "#f6e8d0"), "white": ramp("#d8d0c0", "#ffffff"),
        "nose": ramp("#0e0a0a", "#4a3a34"), "hoof": ramp("#201814", "#5a4a40"), "eye": ramp("#100806", "#5a3a20"),
        "antler": ramp("#5a4630", "#efe2c4"), "back": ramp("#5a321a", "#d29a5e"),
    },
}


def build() -> Model:
    m = Model("deer", 64, 64)
    m.bone("body", pivot=(0, 10, 0))
    m.cube("body", (-3.5, -4, -8), (7, 8, 15), "fur")

    m.bone("neck", "body", pivot=(0, -2, -7), rotation=(-50, 0, 0))
    m.cube("neck", (-1.5, -2, -7), (3, 4, 8), "neck")
    m.bone("head", "neck", pivot=(0, 0, -6.5), rotation=(50, 0, 0))
    m.cube("head", (-2.5, -3, -5), (5, 4, 5), "fur")
    m.cube("head", (-1.5, -2, -8.5), (3, 3, 3.5), "muzzle")
    m.cube("head", (2.3, -1.8, -4), (0.5, 1, 1), "eye")
    m.cube("head", (-2.8, -1.8, -4), (0.5, 1, 1), "eye")
    for side in (1, -1):
        name = "ear_l" if side > 0 else "ear_r"
        m.bone(name, "head", pivot=(2.0 * side, -2.8, -1.2), rotation=(-15, -15 * side, 38 * side))
        m.cube(name, (-1, -3.5, -0.5), (2, 3.5, 1), "ear")
        # Geweih: Stange mit drei Enden
        antler = "antler_l" if side > 0 else "antler_r"
        m.bone(antler, "head", pivot=(1.3 * side, -3, -2.5), rotation=(-15, 0, 22 * side))
        m.cube(antler, (-0.5, -7, -0.5), (1, 7, 1), "antler")
        m.bone(antler + "_tine1", antler, pivot=(0, -2.5, 0), rotation=(-55, 0, 0))
        m.cube(antler + "_tine1", (-0.5, -3, -0.5), (1, 3, 1), "antler")
        m.bone(antler + "_tine2", antler, pivot=(0, -5, 0), rotation=(-45, 0, 0))
        m.cube(antler + "_tine2", (-0.5, -2.5, -0.5), (1, 2.5, 1), "antler")
        m.bone(antler + "_crown", antler, pivot=(0, -6.5, 0), rotation=(25, 0, -15 * side))
        m.cube(antler + "_crown", (-0.5, -2.5, -0.5), (1, 2.5, 1), "antler")

    for name, x, z in (("front_left_leg", 2, -6), ("front_right_leg", -2, -6),
                       ("hind_left_leg", 2, 5.5), ("hind_right_leg", -2, 5.5)):
        m.bone(name, pivot=(x, 13, z))
        m.cube(name, (-1, 0, -1), (2, 11, 2), "leg")

    m.bone("tail", "body", pivot=(0, -3, 7), rotation=(30, 0, 0))
    m.cube("tail", (-1, 0, -0.5), (2, 3, 1), "tail")
    return m


def _spots(base):
    """Kitze: weiße Tupfen in Reihen entlang des Rückens."""

    def paint(ctx: Ctx):
        col = base(ctx)
        if ctx.params.get("variant") != "deer_fawn" or ctx.face in ("bottom", "front", "back"):
            return col
        x, y, z = ctx.world
        if ctx.cube.size[2] >= 14 and rel_y(ctx) < 0.6:
            row = round(y / 2.0)
            if (int(z + row * 1.5) % 3 == 0) and hash01(int(z), row, 3) > 0.35 and abs((y / 2.0) - row) < 0.3:
                return pick(ctx.pal["white"], 0.8)
        return col

    return paint


def _body(ctx: Ctx):
    base = _spots(fur_painter("fur", "belly", belly_from=0.72, contrast=0.35, streaks=0.12, top_dark=0.16, seed=31))
    col = base(ctx)
    # weißer Spiegel am Hinterteil
    if ctx.face == "back" and ctx.cube.size[2] >= 14 and rel_y(ctx) < 0.7:
        return pick(ctx.pal["white"], 0.75 + hash01(ctx.u, ctx.v) * 0.2)
    return col


def _neck(ctx: Ctx):
    col = fur_painter("fur", "belly", belly_from=0.55, contrast=0.3, streaks=0.1, top_dark=0.1, seed=32)(ctx)
    # helle Kehle
    if ctx.face == "bottom":
        return pick(ctx.pal["white"], 0.6)
    return col


def _muzzle(ctx: Ctx):
    if ctx.face == "front" and ctx.t <= 1:
        return pick(ctx.pal["nose"], 0.25 if ctx.t == 0 else 0.4)
    if ctx.face == "bottom":
        return pick(ctx.pal["white"], 0.6)
    return fur_painter("fur", None, contrast=0.2, top_dark=0.0, seed=33)(ctx)


def _ear(ctx: Ctx):
    if ctx.face == "front" and 0 < ctx.t < ctx.h - 1:
        return pick(ctx.pal["belly"], 0.5)        # helles Ohr-Inneres
    return pick(ctx.pal["fur"], 0.5 + hash01(ctx.u, ctx.v) * 0.1)


def _leg(ctx: Ctx):
    ry = rel_y(ctx)
    if ry > 0.88 or ctx.face == "bottom":
        return pick(ctx.pal["hoof"], 0.35)
    if ry > 0.55:
        return pick(ctx.pal["fur"], 0.42 + hash01(ctx.u, ctx.v) * 0.08)
    return fur_painter("fur", None, contrast=0.25, top_dark=0.0, seed=34)(ctx)


def _antler(ctx: Ctx):
    x, y, z = ctx.world
    n = noise3(x * 1.2, y * 1.2, z * 1.2, 41)
    return pick(ctx.pal["antler"], 0.45 + n * 0.35 + (0.15 if ctx.face in ("top", "west") else 0.0))


def _tail(ctx: Ctx):
    if ctx.face in ("back", "bottom"):
        return pick(ctx.pal["white"], 0.8)
    return pick(ctx.pal["fur"], 0.35)


def materials(variant: str) -> dict:
    return {
        "fur": _body,
        "neck": _neck,
        "muzzle": _muzzle,
        "ear": _ear,
        "eye": eye_painter("eye", glow=False),
        "leg": _leg,
        "antler": _antler,
        "tail": _tail,
    }
