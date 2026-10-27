"""
Glimmerfalter: ein handgroßer Nachtfalter mit samtigem, dunkelblauem Leib, gefiederten Fühlern und vier weichen
Flügeln, auf denen silberblaue Augenflecken glimmen. Flattert in Schwärmen über die Glimmerweiten.

Knochen-Namen werden in GlimmerMothModel (Java) für die Animationen verwendet.
"""
from __future__ import annotations

import math

from ..geometry import Model
from ..paint import Ctx, hash01, mix, pick, ramp

PALETTES = {
    "glimmer_moth": {
        "body": ramp("#141a30", "#5a6a9a"), "wing": ramp("#26325e", "#a8b8ec"), "spot": ramp("#5ac8ff", "#f0fbff"),
        "back": ramp("#141a30", "#5a6a9a"),
    },
}


def build() -> Model:
    m = Model("glimmer_moth", 64, 32)
    m.bone("body", pivot=(0, 20, 0))
    m.cube("body", (-1, -1, -2), (2, 2, 5), "body")
    m.cube("body", (-1.5, -1.5, -4), (3, 3, 2), "body")              # Kopf
    for side in (1, -1):
        ant = "antenna_l" if side > 0 else "antenna_r"
        m.bone(ant, "body", pivot=(0.8 * side, -1.5, -3.5), rotation=(-50, 20 * side, 0))
        m.cube(ant, (-0.5, 0, -4), (1, 0, 4), "antenna")
        front = "left_wing" if side > 0 else "right_wing"
        back = "left_hind_wing" if side > 0 else "right_hind_wing"
        m.bone(front, "body", pivot=(1 * side, -1, -1))
        m.cube(front, (0 if side > 0 else -9, 0, -4), (9, 0, 7), "wing")
        m.bone(back, "body", pivot=(1 * side, -1, 1))
        m.cube(back, (0 if side > 0 else -7, 0, 0), (7, 0, 5), "hind")
    return m


def _wing(ctx: Ctx, cx: float, cz: float, r: float):
    lx = abs(ctx.local[0])
    lz = ctx.local[2]
    w = ctx.cube.size[0]
    # runder Flügelrand: Ecken weglassen
    ox = ctx.cube.origin[2]
    rel_z = (lz - ox) / max(ctx.cube.size[2], 1e-3)
    rel_x = lx / max(w, 1e-3)
    if rel_x > 0.85 and (rel_z < 0.15 or rel_z > 0.85):
        return None
    d = math.hypot(lx - cx, lz - cz)
    v = 0.3 + rel_x * 0.25 + (hash01(ctx.u, ctx.v) - 0.5) * 0.08
    col = pick(ctx.pal["wing"], v)
    if d < r:
        spot = pick(ctx.pal["spot"], 0.9 if d < r * 0.45 else 0.5)
        ctx.glow = spot
        return spot
    if rel_x > 0.9:
        col = mix(col, pick(ctx.pal["spot"], 0.2), 0.3)          # helle Flügelkante
    return col


def materials(variant: str) -> dict:
    return {
        "body": lambda ctx: pick(ctx.pal["body"], 0.3 + hash01(ctx.u, ctx.v) * 0.25 + (0.1 if ctx.face == "top" else 0)),
        "antenna": lambda ctx: pick(ctx.pal["spot"], 0.4),
        "wing": lambda ctx: _wing(ctx, 5.5, -1.0, 1.9),
        "hind": lambda ctx: _wing(ctx, 4.0, 2.5, 1.3),
    }
