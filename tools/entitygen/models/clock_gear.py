"""
Das Zahnrad des Stundenwächters (1.01): ein Zahnrad aus Uhrbronze, das auf der Kante über das Zifferblatt rollt. Zwei
Scheiben (vorn und hinten, dazwischen die Nabe), zwölf Zähne, fünf Fenster zwischen den Speichen, in der Mitte ein Chronit.

Die Scheiben stehen quer zu x (sie rollen entlang -z, der Blickrichtung), die Mitte liegt im Ursprung. Knochen: gear
(dreht sich in Java um x).
"""
from __future__ import annotations

import math

from ..geometry import Model
from ..paint import Ctx, hexc, pick

PALETTES = {
    "clock_gear": {
        "bronze": [hexc(c) for c in ("#1e1006", "#331c0a", "#4e2d10", "#6c4016", "#8c561e", "#aa6e28", "#c38634", "#d8a046",
                                     "#e8bc62", "#f6d88e", "#fff2c8")],
        "brass": [hexc(c) for c in ("#3a2608", "#5a3c0e", "#7c5716", "#a07420", "#c4942e", "#deb44a", "#f0d27a", "#fff0b8")],
        "amber": [hexc(c) for c in ("#6a2c06", "#a2480c", "#d26a16", "#f08f24", "#ffb347", "#ffd27e", "#fff0c4")],
    }
}

SIZE = 20.8
TEETH = 12


def build() -> Model:
    m = Model("clock_gear", 128, 64)
    m.tex_res = 2
    m.bone("gear")
    h = SIZE / 2.0
    for x in (-1.0, 0.6):
        m.cube("gear", (x, -h, -h), (0.4, SIZE, SIZE), "disc", faces=("west", "east"))
    m.cube("gear", (-0.9, -2.0, -2.0), (1.8, 4.0, 4.0), "hub")
    return m


def _disc(ctx: Ctx):
    ox, oy, oz = ctx.cube.origin
    sx, sy, sz = ctx.cube.size
    u = ctx.local[2] - (oz + sz / 2.0)
    v = (oy + sy / 2.0) - ctx.local[1]
    R = SIZE / 2.0
    r = math.hypot(u, v)
    a = math.atan2(v, u)
    if r > R:
        return None
    root = R * 0.82
    if r > root:
        k = (a / (2.0 * math.pi) * TEETH) % 1.0
        width = 0.52 - (r - root) / (R - root) * 0.2
        if abs(k - 0.5) > width / 2.0:
            return None
    if R * 0.3 < r < R * 0.62:
        k = (a / (2.0 * math.pi) * 5) % 1.0
        if 0.16 < k < 0.84:
            return None
    if r < R * 0.16:
        col = pick(ctx.pal["amber"], 0.85 - r / (R * 0.16) * 0.3)
        ctx.glow = col
        return col
    light = 0.08 if v > 0 else -0.04
    if r > root:
        return pick(ctx.pal["brass"], 0.62 + light)
    if abs(r - root) < 0.6 or abs(r - R * 0.62) < 0.5 or abs(r - R * 0.3) < 0.45:
        return pick(ctx.pal["bronze"], 0.45 + light)                       # erhabene Ringe
    return pick(ctx.pal["bronze"], 0.62 + light)


def _hub(ctx: Ctx):
    if ctx.face in ("west", "east"):
        col = pick(ctx.pal["amber"], 0.7)
        ctx.glow = col
        return col
    return pick(ctx.pal["brass"], 0.6)


def materials(variant: str) -> dict:
    return {"disc": _disc, "hub": _hub}
