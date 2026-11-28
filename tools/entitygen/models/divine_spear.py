"""
Lichtspeer Schrollis des Schöpfers: ein Speer aus gehärtetem Licht – goldener Schaft, eine lange vierkantige Spitze aus
weißem Licht, eine Parierstange, am Ende Schwingen aus Licht. Spitze bei -Z, Mitte im Ursprung (Knochen "spear").
Im Spiel ×1,6 und ganz leuchtend.
"""
from __future__ import annotations

from ..geometry import Model
from ..paint import Ctx, hexc, mix, pick
from . import schrolli as base

PALETTES = {"divine_spear": dict(base._BASE)}


def build() -> Model:
    m = Model("divine_spear", 64, 64)
    m.tex_res = 2
    m.bone("spear", pivot=(0, 0, 0))
    m.cube("spear", (-0.6, -0.6, -6.0), (1.2, 1.2, 18.0), "shaft")
    m.cube("spear", (-1.6, -1.6, -7.2), (3.2, 3.2, 1.6), "guard_ring")
    m.cube("spear", (-3.6, -0.5, -7.0), (7.2, 1.0, 1.0), "guard")                     # Parierstange
    m.cube("spear", (-1.3, -1.3, -11.0), (2.6, 2.6, 4.0), "blade")
    m.cube("spear", (-0.9, -0.9, -14.0), (1.8, 1.8, 3.2), "blade")
    m.cube("spear", (-0.45, -0.45, -16.6), (0.9, 0.9, 2.8), "blade_tip")
    m.cube("spear", (-1.0, -1.0, 11.6), (2.0, 2.0, 1.4), "gem")                       # Knauf
    for name, rot in (("vane_l", (0, 0, 0)), ("vane_r", (0, 0, 90))):
        m.bone(name, "spear", pivot=(0, 0, 8.0), rotation=rot)
        m.cube(name, (-3.2, 0, 0), (6.4, 0, 4.6), "vane")                             # Schwingen aus Licht
    return m


def _light(v: float):
    def paint(ctx: Ctx):
        col = pick(ctx.pal["light"], v + base._LIT[ctx.face] * 0.5)
        ctx.glow = col
        return col
    return paint


def _shaft(ctx: Ctx):
    col = pick(ctx.pal["gold"], 0.7 + base._LIT[ctx.face])
    if ctx.face not in ("front", "back") and ctx.t % 6 in (0, 1):
        col = pick(ctx.pal["light"], 0.9)
    ctx.glow = mix((0, 0, 0, 255), col, 0.75)
    return col


def _vane(ctx: Ctx):
    s = (ctx.s + 0.5) / max(ctx.w, 1) - 0.5
    t = (ctx.t + 0.5) / max(ctx.h, 1)
    if abs(s) > 0.5 - t * 0.35:
        return None
    col = pick(ctx.pal["light"], 0.95 - t * 0.3)
    ctx.glow = col
    return col[:3] + (round(240 - t * 110),)


def materials(variant: str) -> dict:
    mats = base.materials("schrolli")
    mats.update({"shaft": _shaft, "guard_ring": _light(0.75), "guard": _light(0.7), "blade": _light(0.82),
                 "blade_tip": _light(0.98), "vane": _vane})
    return mats
