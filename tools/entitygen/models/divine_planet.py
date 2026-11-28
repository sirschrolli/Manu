"""
Welten der Schöpfung (Weltenwurf, Sternenregen, Weltkugel und Sonne Schrollis des Schöpfers): eine runde Welt aus
geschichteten Quadern (Knochen "planet", Mitte im Ursprung) und ein schräger Ring um sie (Knochen "ring", nur bei manchen
Welten sichtbar). Varianten: Erde, Glut, Eis, Leere, Sonne und die Weltkugel.
"""
from __future__ import annotations

import math

from ..geometry import Model
from ..paint import Ctx, hash01, hexc, mix, noise3, pick
from . import schrolli as base

_P = dict(base._BASE)
PALETTES = {
    "earth": dict(_P, sea=[hexc(c) for c in ("#0a2a6a", "#123c8a", "#1a56b0", "#2a74d0")],
                  land=[hexc(c) for c in ("#2a5a1a", "#3a7a22", "#5a9a30", "#8ab048", "#c8b878")],
                  cloud=[hexc("#e8f0ff"), hexc("#ffffff")]),
    "lava": dict(_P, sea=[hexc(c) for c in ("#2a0a06", "#401008", "#5a180a", "#2a0a06")],
                 land=[hexc(c) for c in ("#c02008", "#e84a10", "#ff8a20", "#ffc040", "#fff0a0")],
                 cloud=[hexc("#3a2a2a"), hexc("#5a4a48")]),
    "ice": dict(_P, sea=[hexc(c) for c in ("#5a8ab8", "#7aaad0", "#a0c8e8", "#c8e4f8")],
                land=[hexc(c) for c in ("#e8f4ff", "#ffffff", "#d0e8f8", "#b8dcf4", "#a0d0f0")],
                cloud=[hexc("#ffffff"), hexc("#f0f8ff")]),
    "void": dict(_P, sea=[hexc(c) for c in ("#06021a", "#0e0630", "#1a0c48", "#2a1260")],
                 land=[hexc(c) for c in ("#6a1e8a", "#a02a9e", "#d24aa8", "#5a8ae8", "#a0f0ff")],
                 cloud=[hexc("#c8a8ff"), hexc("#ffffff")]),
    "sun": dict(_P, sea=[hexc(c) for c in ("#ff9a20", "#ffc040", "#ffe080", "#fff4c0")],
                land=[hexc(c) for c in ("#fff0a0", "#fff8d0", "#ffffff", "#ffffff", "#ffffff")],
                cloud=[hexc("#ffffff"), hexc("#ffffff")]),
    "globe": dict(_P, sea=[hexc(c) for c in ("#0a1a4a", "#103074", "#1a4aa2", "#2c6ccc")],
                  land=[hexc(c) for c in ("#8a5e1c", "#b8862a", "#dcae3e", "#f2cf62", "#ffe8a0")],
                  cloud=[hexc("#fff2c4"), hexc("#ffffff")]),
}


def build() -> Model:
    m = Model("divine_planet", 128, 128)
    m.tex_res = 2
    m.bone("planet", pivot=(0, 0, 0))
    # eine Kugel aus geschichteten Quadern (Halbmesser 6)
    m.cube("planet", (-6, -4, -4), (12, 8, 8), "world")
    m.cube("planet", (-4, -6, -4), (8, 12, 8), "world")
    m.cube("planet", (-4, -4, -6), (8, 8, 12), "world")
    m.cube("planet", (-5.2, -5.2, -5.2), (10.4, 10.4, 10.4), "world")
    m.bone("ring", "planet", pivot=(0, 0, 0), rotation=(18, 0, 24))
    r = 10.5
    seg = 2 * math.pi * r / 20 + 0.6
    for k in range(20):
        a = 18.0 * k
        rad = math.radians(a)
        m.bone(f"ring_{k}", "ring", pivot=(math.cos(rad) * r, 0, math.sin(rad) * r), rotation=(0, -(a + 90.0), 0))
        m.cube(f"ring_{k}", (-seg / 2, -0.15, -1.6), (seg, 0.3, 3.2), "ring")
    return m


def _world(ctx: Ctx):
    x, y, z = ctx.world
    n = noise3(x * 0.42 + 2.0, y * 0.42, z * 0.42, 91)
    n2 = noise3(x * 1.1, y * 1.1, z * 1.1, 92)
    variant = (ctx.params or {}).get("variant")
    lit = base._LIT[ctx.face]
    if n > 0.55:
        col = pick(ctx.pal["land"], (n - 0.55) * 2.4 + n2 * 0.25 + lit)
        if variant in ("lava", "sun", "void", "globe"):
            ctx.glow = mix((0, 0, 0, 255), col, 0.9 if variant in ("lava", "sun") else 0.6)
    else:
        col = pick(ctx.pal["sea"], n * 1.5 + lit)
        if variant == "sun":
            ctx.glow = col
        elif variant == "void" and hash01(ctx.u, ctx.v, 93) > 0.94:
            col = pick(ctx.pal["light"], 0.95)
            ctx.glow = col
    if variant in ("earth", "ice") and noise3(x * 0.6, y * 0.6 + 7.0, z * 0.6, 94) > 0.68:
        col = mix(col, pick(ctx.pal["cloud"], 0.5), 0.8)                                 # Wolken
    return col


def _atmosphere(ctx: Ctx):
    variant = (ctx.params or {}).get("variant")
    if variant in ("sun",):
        col = pick(ctx.pal["light"], 0.9)
        ctx.glow = col
        return col[:3] + (90,)
    if variant in ("lava",):
        col = hexc("#ff6a20")
        ctx.glow = mix((0, 0, 0, 255), col, 0.5)
        return col[:3] + (50,)
    col = pick(ctx.pal["cloud"], 0.5)
    ctx.glow = mix((0, 0, 0, 255), col, 0.25)
    return col[:3] + (40,)


def _ring(ctx: Ctx):
    s = ctx.t if ctx.face in ("top", "bottom") else ctx.s
    v = 0.4 + (s % 3) * 0.2
    col = pick(ctx.pal["land"], v)
    ctx.glow = mix((0, 0, 0, 255), col, 0.4)
    return col[:3] + (200,)


def materials(variant: str) -> dict:
    return {"world": _world, "ring": _ring}
