"""
Eine vergiftete Feder aus Quetzaras Krause (1.10): die Spitze voraus (−z), Mitte im Ursprung (Knochen "feather"); smaragdgrün mit
türkisem Glanz, goldener Kiel, die Spitze rot und in Gift getaucht (glimmt grün).
"""
from __future__ import annotations

from ..geometry import Model
from ..paint import Ctx, mix, pick
from .quetzara import PALETTES as _P

PALETTES = {"feather": dict(_P["quetzara"], venom=[(40, 120, 20, 255), (90, 200, 40, 255), (150, 255, 90, 255), (210, 255, 170, 255)])}


def build() -> Model:
    m = Model("quetzara_feather", 32, 32)
    m.tex_res = 2
    m.bone("feather", pivot=(0, 0, 0))
    m.cube("feather", (-2.2, 0, -7.0), (4.4, 0, 14.0), "vane")
    m.cube("feather", (-0.3, -0.3, -7.6), (0.6, 0.6, 14.2), "quill")
    return m


def _vane(ctx: Ctx):
    s = (ctx.s + 0.5) / max(ctx.w, 1) - 0.5
    t = (ctx.t + 0.5) / max(ctx.h, 1)            # 0 = Spitze (vorn), 1 = Kiel
    width = 0.5 * min(1.0, t * 2.6) - max(0.0, t - 0.82) * 1.8
    if abs(s) > width:
        return None
    v = 0.7 - abs(s) * 0.5 - t * 0.2
    col = pick(ctx.pal["feather"], v)
    if t < 0.5:
        col = mix(col, pick(ctx.pal["feather_sheen"], 0.6), (0.5 - t) * 1.2)
    if t < 0.16:
        col = pick(ctx.pal["crimson"], 0.55 + (0.16 - t) * 2.0)
    if t < 0.07:
        col = pick(ctx.pal["venom"], 0.6)
        ctx.glow = col
    if abs(s) < 0.06:
        col = pick(ctx.pal["gold"], 0.62)
    if (ctx.t + int(abs(s) * 20)) % 5 == 0 and abs(s) > 0.1:
        col = mix(col, pick(ctx.pal["dark"], 0.5), 0.25)
    return col


def _quill(ctx: Ctx):
    return pick(ctx.pal["gold"], 0.7)


def materials(variant: str) -> dict:
    return {"vane": _vane, "quill": _quill}
