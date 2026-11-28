"""
Ein Klumpen Gift, den Quetzara spuckt (1.10): ein zäher, grün leuchtender Tropfen (Knochen "glob", Mitte im Ursprung), mit
einem Schweif aus kleineren Tropfen.
"""
from __future__ import annotations

from ..geometry import Model
from ..paint import Ctx, hash01, hexc, pick

PALETTES = {"venom_glob": {"venom": [hexc(c) for c in ("#1a4a08", "#2e7410", "#48a01c", "#6cc82e", "#9ce858", "#ccff94", "#f0ffd8")]}}


def build() -> Model:
    m = Model("venom_glob", 64, 32)
    m.tex_res = 2
    m.bone("glob", pivot=(0, 0, 0))
    m.cube("glob", (-2.5, -2.5, -2.5), (5, 5, 5), "venom")
    m.cube("glob", (-1.8, -3.1, -1.8), (3.6, 6.2, 3.6), "venom")
    m.cube("glob", (-3.1, -1.8, -1.8), (6.2, 3.6, 3.6), "venom")
    m.cube("glob", (-1.2, -1.2, 2.4), (2.4, 2.4, 2.4), "venom")
    m.cube("glob", (-0.7, -0.7, 4.8), (1.4, 1.4, 1.6), "venom")
    return m


def _venom(ctx: Ctx):
    s = (ctx.s + 0.5) / max(ctx.w, 1) - 0.5
    t = (ctx.t + 0.5) / max(ctx.h, 1) - 0.5
    d = (s * s + t * t) ** 0.5 * 2.0
    v = 0.85 - d * 0.45 + (hash01(ctx.u, ctx.v, 501) - 0.5) * 0.12
    if ctx.face == "top":
        v += 0.1
    col = pick(ctx.pal["venom"], v)
    ctx.glow = col
    return col[0], col[1], col[2], 215


def materials(variant: str) -> dict:
    return {"venom": _venom}
