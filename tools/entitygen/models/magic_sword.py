"""
Schwert der Schwerterkiste: eine schmale Klinge aus violett-weißem Licht mit hellem Grat, eine goldene Parierstange mit
einem rosa Edelstein, ein Griff mit violetter Wicklung und ein goldener Knauf. Die Spitze zeigt nach vorn (−z).
"""
from __future__ import annotations

from ..geometry import Model
from ..paint import Ctx, hash01, mix, pick, ramp, rel_y

PALETTES = {
    "magic_sword": {
        "blade": ramp("#6a3ab0", "#ffffff", steps=7),
        "gold": ramp("#6a4a10", "#fff2b8", steps=6),
        "grip": ramp("#2a1448", "#9a6ad8", steps=5),
        "gem": ramp("#8a1a5a", "#ffc8ec", steps=5),
    },
}


def build() -> Model:
    m = Model("magic_sword", 64, 32)
    m.tex_res = 2
    m.bone("sword", pivot=(0, 0, 0))
    m.cube("sword", (-0.2, -0.65, -13.0), (0.4, 1.3, 11.0), "blade")
    m.cube("sword", (-0.15, -0.45, -14.5), (0.3, 0.9, 1.5), "blade")
    m.cube("sword", (-0.1, -0.2, -15.5), (0.2, 0.4, 1.0), "blade")
    m.cube("sword", (-0.45, -2.25, -2.4), (0.9, 4.5, 0.9), "gold")
    m.cube("sword", (-0.55, -0.55, -2.5), (1.1, 1.1, 1.1), "gem")
    m.cube("sword", (-0.35, -0.35, -1.5), (0.7, 0.7, 3.0), "grip")
    m.cube("sword", (-0.6, -0.6, 1.5), (1.2, 1.2, 1.2), "gold")
    return m


def _blade(ctx: Ctx):
    """Klinge aus Licht: zum Grat hin weiß, zu den Schneiden violett."""
    _, ly, _ = ctx.local
    k = 1.0 - min(1.0, abs(ly) / 0.65)
    col = pick(ctx.pal["blade"], 0.45 + k * 0.5 + (hash01(ctx.u, ctx.v) - 0.5) * 0.06)
    ctx.glow = col
    return col


def _gold(ctx: Ctx):
    return pick(ctx.pal["gold"], 0.65 - rel_y(ctx) * 0.2 + (hash01(ctx.u, ctx.v) - 0.5) * 0.1)


def _grip(ctx: Ctx):
    return pick(ctx.pal["grip"], 0.5 + (0.15 if (ctx.t + ctx.s) % 3 == 0 else 0.0))


def _gem(ctx: Ctx):
    col = pick(ctx.pal["gem"], 0.8)
    ctx.glow = col
    return col


def materials(variant: str) -> dict:
    return {"blade": _blade, "gold": _gold, "grip": _grip, "gem": _gem}
