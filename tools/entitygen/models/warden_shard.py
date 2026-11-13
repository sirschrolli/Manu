"""
Kristallsplitter: das Geschoss des Splitterwächters – ein schlanker, geschliffener Leerenkristall mit rautenförmigem
Querschnitt, der von innen glüht. Zeigt entlang -Z (Flugrichtung), Mitte im Ursprung.
"""
from __future__ import annotations

from ..geometry import Model
from ..paint import Ctx, hash01, hexc, mix, pick

PALETTES = {
    "warden_shard": {
        "crystal": [hexc(c) for c in ("#3a1c78", "#5a30b0", "#7a44d0", "#a070ec", "#c8a8ff", "#e6d8ff", "#ffffff")],
    },
}


def build() -> Model:
    m = Model("warden_shard", 32, 32)
    m.tex_res = 2
    m.bone("shard", rotation=(0, 0, 45))
    m.cube("shard", (-1.1, -1.1, -3.5), (2.2, 2.2, 7.0), "crystal")
    m.bone("tip", "shard", pivot=(0, 0, -3.5))
    m.cube("tip", (-0.65, -0.65, -3.0), (1.3, 1.3, 3.0), "crystal")
    m.bone("tail", "shard", pivot=(0, 0, 3.5))
    m.cube("tail", (-0.55, -0.55, 0), (1.1, 1.1, 1.8), "crystal")
    return m


def _crystal(ctx: Ctx):
    z = ctx.world[2]
    a = max(0.0, min(1.0, (-z + 6.0) / 12.0))                    # vorn heller
    edge = ctx.s == 0 or ctx.s == ctx.w - 1 or ctx.t == 0 or ctx.t == ctx.h - 1
    v = 0.12 + a * 0.5 + (0.2 if edge else 0.0) + hash01(ctx.u, ctx.v) * 0.06
    col = pick(ctx.pal["crystal"], v)
    ctx.glow = mix((0, 0, 0, 255), col, 0.85)
    return col


def materials(variant: str) -> dict:
    return {"crystal": _crystal}
