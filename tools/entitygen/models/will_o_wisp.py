"""
Irrlicht: ein schwebendes Licht aus einem hellen Kern, einem durchscheinenden Schein und drei Funken,
die um den Kern kreisen. Wird selbstleuchtend und durchscheinend gezeichnet (WillOWispRenderer).

Knochen-Namen werden in WillOWispModel (Java) für die Animationen verwendet.
"""
from __future__ import annotations

import math

from ..geometry import Model
from ..paint import Ctx, ramp

PALETTES = {
    "will_o_wisp": {
        "core": ramp("#58e6d8", "#f4fffe"), "halo": ramp("#1aa8a0", "#8ff8ee"), "spark": ramp("#9af8f0", "#ffffff"),
        "back": ramp("#1aa8a0", "#8ff8ee"),
    },
}


def build() -> Model:
    m = Model("will_o_wisp", 32, 32)
    m.bone("core", pivot=(0, 16, 0))
    m.cube("core", (-1.5, -1.5, -1.5), (3, 3, 3), "core")
    m.bone("halo", "core", pivot=(0, 0, 0))
    m.cube("halo", (-3, -3, -3), (6, 6, 6), "halo")
    for i in range(3):
        m.bone(f"spark{i + 1}", "core", pivot=(0, 0, 0))
        m.cube(f"spark{i + 1}", (3.5, -0.5, -0.5), (1, 1, 1), "spark")
    return m


def _core(ctx: Ctx):
    # zur Mitte jeder Fläche heller
    cx = (ctx.w - 1) / 2
    cy = (ctx.h - 1) / 2
    d = math.hypot(ctx.s - cx, ctx.t - cy) / max(1.0, math.hypot(cx, cy))
    col = ctx.pal["core"][min(5, max(0, int((1.0 - d) * 6)))]
    return col


def _halo(ctx: Ctx):
    cx = (ctx.w - 1) / 2
    cy = (ctx.h - 1) / 2
    d = math.hypot(ctx.s - cx, ctx.t - cy) / max(1.0, math.hypot(cx, cy))
    r, g, b, _ = ctx.pal["halo"][min(5, max(0, int((1.0 - d) * 5) + 1))]
    alpha = int(36 + (1.0 - d) * 80)
    if d > 0.95:
        return None                     # weiche, runde Kanten
    return r, g, b, alpha


def materials(variant: str) -> dict:
    return {
        "core": _core,
        "halo": _halo,
        "spark": lambda ctx: ctx.pal["spark"][5 if (ctx.s + ctx.t) % 2 == 0 else 4],
    }
