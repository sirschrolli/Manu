"""
Ein Batzen Schlamm, den der Moorschlurfer wirft (1.12.1): ein nasser, klumpiger Ball (Knochen "ball", Mitte im Ursprung)
mit zwei Kieseln und einem Grasbüschel darin; hinten (+z) tropft er nach. Vorher flog ein Schlammblock aus Vanilla – ein
glatter Würfel, der nicht nach Wurf aussah.
"""
from __future__ import annotations

from ..geometry import Model
from ..paint import Ctx, Voronoi3D, hash01, hexc, pick

PALETTES = {"mud_ball": {
    "mud": [hexc(c) for c in ("#1c130b", "#2a1d11", "#382817", "#47331d", "#584025", "#6d5030", "#86673f")],
    "pebble": [hexc(c) for c in ("#3f3c37", "#57534b", "#706b60", "#8c8676", "#a8a18e")],
    "grass": [hexc(c) for c in ("#26400f", "#3a5a18", "#527a22", "#6c962e", "#8db63e")],
}}

_CLUMPS = Voronoi3D(1.6, 7101)


def build() -> Model:
    m = Model("mud_ball", 64, 32)
    m.tex_res = 2
    m.bone("ball", pivot=(0, 0, 0))
    m.cube("ball", (-2.5, -2.5, -2.5), (5, 5, 5), "mud")                 # Kern
    m.cube("ball", (-3.0, -1.7, -1.9), (6.0, 3.6, 3.6), "mud")           # Wulst quer
    m.cube("ball", (-1.6, -3.0, -1.4), (3.4, 6.0, 3.2), "mud")           # Wulst hoch
    m.cube("ball", (-1.3, -1.2, -3.0), (2.8, 2.6, 5.6), "mud")           # Wulst längs (etwas versetzt: klumpig)
    m.cube("ball", (1.6, 1.4, -2.6), (1.6, 1.6, 1.6), "pebble")          # Kiesel unten vorn
    m.cube("ball", (-3.3, -2.6, 0.4), (1.3, 1.2, 1.3), "pebble")         # Kiesel oben seitlich
    m.cube("ball", (-0.5, -4.0, -0.9), (0.8, 1.3, 0.6), "grass")         # Grasbüschel oben
    m.cube("ball", (0.1, -3.6, -0.2), (0.6, 0.9, 0.5), "grass")
    m.cube("ball", (-0.9, 0.0, 2.6), (1.8, 1.8, 2.2), "mud")             # Nachtropfen
    m.cube("ball", (-0.5, 0.7, 4.8), (1.0, 1.0, 1.4), "mud")
    return m


def _mud(ctx: Ctx):
    x, y, z = ctx.world
    cell, edge = _CLUMPS(x + 9.0, y + 9.0, z + 9.0)
    # Klümpchen: jede Zelle etwas anders getönt, an den Rändern dunkle Fugen
    v = 0.42 + (cell - 0.5) * 0.22 + (hash01(ctx.u, ctx.v, 811) - 0.5) * 0.14 - (1.0 - edge) * 0.16
    if ctx.face == "top":
        v += 0.12
    elif ctx.face == "bottom":
        v -= 0.16
    # nasser Glanz: einzelne helle Lichter, vor allem oben
    if ctx.face != "bottom" and hash01(ctx.u, ctx.v, 813) > (0.90 if ctx.face == "top" else 0.95):
        v = 0.99
    return pick(ctx.pal["mud"], v)


def _pebble(ctx: Ctx):
    v = 0.55 + (hash01(ctx.u, ctx.v, 821) - 0.5) * 0.35
    if ctx.face == "top":
        v += 0.2
    elif ctx.face == "bottom":
        v -= 0.25
    return pick(ctx.pal["pebble"], v)


def _grass(ctx: Ctx):
    # Halme: oben hell, unten im Schlamm dunkel
    v = 0.75 - (ctx.t + 0.5) / max(ctx.h, 1) * 0.5 + (hash01(ctx.u, ctx.v, 831) - 0.5) * 0.2
    return pick(ctx.pal["grass"], v)


def materials(variant: str) -> dict:
    return {"mud": _mud, "pebble": _pebble, "grass": _grass}
