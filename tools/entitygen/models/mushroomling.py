"""
Pilzling: ein knapp kniehohes Pilzwesen aus dem Pilzhain – ein rundlicher, heller Stiel mit einem freundlichen Gesicht
(große, leuchtende Augen), zwei Stummelbeinchen, zwei Ärmchen und ein großer, gepunkteter Hut in Rosa, Himmelblau oder
Violett, dessen Unterseite leuchtende Lamellen zeigt.

Knochen-Namen werden in MushroomlingModel (Java) für die Animationen verwendet.
"""
from __future__ import annotations

import math

from ..geometry import Model
from ..paint import Ctx, hash01, mix, pick, ramp, rel_y

_STEM = ramp("#a08a78", "#fff6ea", steps=7)

PALETTES = {
    "rose": {"stem": _STEM, "cap": ramp("#7a1a48", "#ff8cc4", steps=7), "spot": ramp("#f8d8e8", "#ffffff", steps=4),
             "gill": ramp("#e090b8", "#fff0f8", steps=5), "eye": ramp("#3a0a2a", "#ffe0f4", steps=6)},
    "azure": {"stem": _STEM, "cap": ramp("#123a7a", "#6ac4ff", steps=7), "spot": ramp("#d8f0ff", "#ffffff", steps=4),
              "gill": ramp("#80c0e8", "#f0fbff", steps=5), "eye": ramp("#0a1a3a", "#d8f6ff", steps=6)},
    "violet": {"stem": _STEM, "cap": ramp("#3a1478", "#b88cff", steps=7), "spot": ramp("#ecdcff", "#ffffff", steps=4),
               "gill": ramp("#b090e8", "#f6f0ff", steps=5), "eye": ramp("#1a0a3a", "#eee0ff", steps=6)},
}


def build() -> Model:
    m = Model("mushroomling", 64, 64)
    m.bone("body", pivot=(0, 22, 0))
    m.cube("body", (-2.5, -6, -2), (5, 6, 4), "stem")
    for side, sx in (("left", 1), ("right", -1)):
        leg = f"{side}_leg"
        m.bone(leg, "body", pivot=(1.3 * sx, 0, 0))
        m.cube(leg, (-1, 0, -1.2), (2, 2, 2.4), "foot")
        arm = f"{side}_arm"
        m.bone(arm, "body", pivot=(2.5 * sx, -4.5, 0))
        m.cube(arm, (0 if sx > 0 else -1, 0, -0.75), (1, 2.6, 1.5), "arm")
    m.bone("cap", "body", pivot=(0, -6, 0))
    m.cube("cap", (-5, -3, -5), (10, 3, 10), "cap_low")
    m.cube("cap", (-3.5, -5, -3.5), (7, 2, 7), "cap_top")
    m.cube("cap", (-2, -6, -2), (4, 1, 4), "cap_top")
    return m


_LIT = {"top": 0.1, "front": 0.04, "west": 0.0, "east": -0.03, "back": -0.06, "bottom": -0.18}


def _stem(ctx: Ctx):
    """Stiel mit Gesicht: zwei große, leuchtende Augen, rosige Wangen und ein kleines Lächeln."""
    face, s, t = ctx.face, ctx.s, ctx.t
    if face == "front":
        if t in (1, 2) and s in (0, 1, 3, 4):
            col = pick(ctx.pal["eye"], 0.95 if (t == 1 and s in (0, 3)) else 0.45 if s in (1, 4) else 0.3)
            ctx.glow = col
            return col
        if t == 3 and s in (0, 4):
            return mix(pick(ctx.pal["stem"], 0.7), pick(ctx.pal["cap"], 0.7), 0.4)
        if t == 3 and s == 2:
            return pick(ctx.pal["cap"], 0.25)
    v = 0.66 + _LIT[face] - rel_y(ctx) * 0.08 + (hash01(ctx.u, ctx.v) - 0.5) * 0.05
    if face in ("west", "east", "back") and hash01(ctx.u, ctx.v, "fleck") > 0.9:
        v -= 0.12
    return pick(ctx.pal["stem"], v)


def _foot(ctx: Ctx):
    return pick(ctx.pal["stem"], 0.5 + _LIT[ctx.face] + (hash01(ctx.u, ctx.v) - 0.5) * 0.05)


def _arm(ctx: Ctx):
    return pick(ctx.pal["stem"], 0.6 + _LIT[ctx.face])


def _cap_paint(ctx: Ctx, base: float):
    x, y, z = ctx.world
    cell = (math.floor(x * 0.55 + 0.3), math.floor(y * 0.55), math.floor(z * 0.55 + 0.5))
    if hash01(*cell, 7) > 0.74 and ctx.face != "bottom":
        return pick(ctx.pal["spot"], 0.7 + hash01(ctx.u, ctx.v) * 0.2)
    return pick(ctx.pal["cap"], base + _LIT[ctx.face] + (hash01(ctx.u, ctx.v) - 0.5) * 0.06)


def _cap_low(ctx: Ctx):
    if ctx.face == "bottom":
        # Lamellen: strahlenförmig, leuchtend
        lx, ly, lz = ctx.local
        a = math.atan2(lz, lx)
        r = math.hypot(lz, lx)
        if r < 2.4:
            return pick(ctx.pal["stem"], 0.55)
        stripe = math.sin(a * 14.0) > 0.2
        col = pick(ctx.pal["gill"], 0.75 if stripe else 0.45)
        ctx.glow = col if stripe else None
        return col
    if ctx.face == "top":
        return _cap_paint(ctx, 0.62)
    return _cap_paint(ctx, 0.5 - ctx.t * 0.08)


def _cap_top(ctx: Ctx):
    if ctx.face == "bottom":
        return pick(ctx.pal["cap"], 0.3)
    return _cap_paint(ctx, 0.62 if ctx.face == "top" else 0.56)


def materials(variant: str) -> dict:
    return {"stem": _stem, "foot": _foot, "arm": _arm, "cap_low": _cap_low, "cap_top": _cap_top}
