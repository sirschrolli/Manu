"""
Goldene Feder (Federsturm Schrollis des Schöpfers): eine große, scharfe Schwungfeder aus Licht, die Spitze voraus (-Z),
Mitte im Ursprung (Knochen "feather"). Im Spiel leuchtend.
"""
from __future__ import annotations

from ..geometry import Model
from ..paint import Ctx, mix, pick
from . import schrolli as base

PALETTES = {"divine_feather": dict(base._BASE)}


def build() -> Model:
    m = Model("divine_feather", 32, 32)
    m.tex_res = 2
    m.bone("feather", pivot=(0, 0, 0))
    m.cube("feather", (-2.4, 0, -7.0), (4.8, 0, 14.0), "vane")
    m.cube("feather", (-0.3, -0.3, -6.0), (0.6, 0.6, 13.0), "quill")
    return m


def _vane(ctx: Ctx):
    s = (ctx.s + 0.5) / max(ctx.w, 1) - 0.5
    t = (ctx.t + 0.5) / max(ctx.h, 1)            # 0 = Spitze (vorn), 1 = Kiel
    width = 0.5 * min(1.0, t * 3.0) - max(0.0, t - 0.8) * 1.6
    if abs(s) > width:
        return None
    v = 0.8 - abs(s) * 0.4
    col = pick(ctx.pal["feather"], v)
    if t < 0.45:
        col = mix(col, pick(ctx.pal["gold"], 0.78), (0.45 - t) * 2.0)
    if abs(s) < 0.05:
        col = pick(ctx.pal["gold"], 0.6)
    if (ctx.t + int(abs(s) * 20)) % 5 == 0 and abs(s) > 0.1:
        col = mix(col, pick(ctx.pal["shadow"], 0.8), 0.25)                              # Fahnenäste
    ctx.glow = mix((0, 0, 0, 255), pick(ctx.pal["light"], 0.85), 0.75 - t * 0.35)
    return col


def _quill(ctx: Ctx):
    col = pick(ctx.pal["gold"], 0.72)
    ctx.glow = mix((0, 0, 0, 255), col, 0.6)
    return col


def materials(variant: str) -> dict:
    return {"vane": _vane, "quill": _quill}
