"""
Mondsteinkristalle (0.96) – die Kristallspur des Steinhüters und des Hüterhammers: ein Büschel Mondsteinkristalle bricht
aus dem Boden, ein großer in der Mitte, vier kleinere schräg darum, am Fuß aufgeworfene Feensteinbrocken. Die Kristalle sind
sechskantig (zwei gegeneinander gedrehte Quader je Glied), milchig blau, zur Spitze heller; die obere Hälfte leuchtet.

Boden-Regel: y = 24 ist der Boden, oben ist -y.

Knochen:
  * crystals  Drehpunkt (0, 24, 0): die Kristalle (beginnen etwas unter dem Boden, damit beim Herauswachsen nichts fehlt)
  * rubble    Drehpunkt (0, 24, 0): Brocken rund um den Fuß, halb im Boden
"""
from __future__ import annotations

import math

from ..geometry import Model
from ..paint import Ctx, Voronoi3D, hash01, hexc, mix, noise3, pick, rel_y

PALETTES = {
    "moon_crystal": {
        "crystal": [hexc(c) for c in ("#22307a", "#2f44a0", "#3f5cc4", "#5a7ce0", "#7e9ef0", "#a6c0f8", "#cadbfc", "#eef4ff")],
        "glow": [hexc(c) for c in ("#2f62d0", "#4f8cf0", "#7cb4ff", "#a8d2ff", "#d4ecff", "#f4fbff")],
        "stone": [hexc(c) for c in ("#4e475e", "#645c76", "#7b738e", "#938aa6", "#aaa2bc", "#bfb8cf", "#d1cbdd")],
        "moss": [hexc(c) for c in ("#62489a", "#7b5eb8", "#9478cc", "#ae94dc")],
    },
}

# Kristalle: (x, z, Höhe, Breite, Neigung x, Neigung z, Drehung y)
CRYSTALS = [(0.0, 0.0, 30.0, 7.0, -4.0, 3.0, 10.0), (-5.6, 2.4, 17.0, 4.6, 14.0, -30.0, 35.0), (5.2, -2.2, 19.0, 4.8, -16.0, 28.0, -20.0),
            (1.6, 5.8, 14.0, 4.0, 34.0, 8.0, 60.0), (-2.2, -5.6, 12.0, 3.6, -32.0, -12.0, 15.0)]


def _crystal(m: Model, parent: str, name: str, x: float, z: float, height: float, width: float, rx: float, rz: float, ry: float):
    """Ein Kristall: Schaft, Spitze und Nadel – je zwei um 45° gegeneinander gedrehte Quader (achtkantiger Querschnitt)."""
    m.bone(name, parent, (x, 0.0, z), (rx, ry, rz))
    h1 = height * 0.66
    m.cube(name, (-width / 2, -h1, -width / 2), (width, h1 + 3.0, width), "crystal", tag="up:0.0:0.66")
    m.bone(f"{name}_b", name, (0.0, 0.0, 0.0), (0.0, 45.0, 0.0))
    w2 = width * 0.84
    m.cube(f"{name}_b", (-w2 / 2, -h1 + 0.5, -w2 / 2), (w2, h1 + 2.5, w2), "crystal_inner", tag="up:0.0:0.64")
    tip = f"{name}_tip"
    m.bone(tip, name, (0.0, -h1 + 0.4, 0.0), (0.0, 22.5, 0.0))
    w3 = width * 0.62
    h2 = height * 0.24
    m.cube(tip, (-w3 / 2, -h2, -w3 / 2), (w3, h2 + 0.4, w3), "crystal", tag="up:0.62:0.9")
    m.bone(f"{tip}_b", tip, (0.0, 0.0, 0.0), (0.0, 45.0, 0.0))
    m.cube(f"{tip}_b", (-w3 * 0.42, -h2 + 0.6, -w3 * 0.42), (w3 * 0.84, h2, w3 * 0.84), "crystal_inner", tag="up:0.62:0.88")
    point = f"{name}_point"
    m.bone(point, tip, (0.0, -h2 + 0.3, 0.0), (0.0, 22.5, 0.0))
    w4 = width * 0.32
    h3 = height * 0.12
    m.cube(point, (-w4 / 2, -h3, -w4 / 2), (w4, h3 + 0.3, w4), "crystal", tag="up:0.88:1.0")


def build() -> Model:
    m = Model("moon_crystal", 128, 96)
    m.tex_res = 2
    m.bone("crystals", pivot=(0, 24, 0))
    for i, (x, z, height, width, rx, rz, ry) in enumerate(CRYSTALS):
        _crystal(m, "crystals", f"crystal_{i}", x, z, height, width, rx, rz, ry)
    m.bone("rubble", pivot=(0, 24, 0))
    for (x, z, sw, sh, sd) in [(-9.0, -3.5, 4.0, 2.6, 3.4), (7.6, -6.0, 3.2, 2.0, 3.0), (8.2, 4.0, 4.2, 3.0, 3.6),
                               (-3.0, 8.6, 3.6, 2.0, 2.8), (-8.0, 6.4, 2.6, 1.6, 2.4), (2.4, -9.6, 3.2, 2.4, 2.6)]:
        m.cube("rubble", (x, -sh + 0.8, z), (sw, sh, sd), "rubble")
    return m


def _seg_t(ctx: Ctx) -> float:
    tag = ctx.cube.tag or ""
    parts = tag.split(":")
    if len(parts) != 3:
        return 0.5
    a, b = float(parts[1]), float(parts[2])
    oy, hgt = ctx.cube.origin[1], ctx.cube.size[1]
    tl = (oy + hgt - ctx.local[1]) / hgt if hgt > 0 else 0.0
    return a + (b - a) * max(0.0, min(1.0, tl))


def _crystal_paint(inner: bool):
    def paint(ctx: Ctx):
        t = _seg_t(ctx)
        facet = ((ctx.s + ctx.t * 2) // 3) % 3
        v = 0.22 + 0.62 * t + (0.1 if facet == 0 else -0.05 if facet == 1 else 0.0) + (0.06 if inner else 0.0)
        if ctx.face in ("west", "front"):
            v += 0.06
        # helle Kante an jeder Fläche (Schliff)
        if ctx.s == 0 or ctx.s == ctx.w - 1:
            v += 0.1
        col = pick(ctx.pal["crystal"], v)
        if t > 0.38:
            g = pick(ctx.pal["glow"], (t - 0.38) / 0.62)
            ctx.glow = g
            col = mix(col, g, 0.5 + 0.3 * (t - 0.38))
        return col
    return paint


_VOR = Voronoi3D(2.2, 307)


def _rubble(ctx: Ctx):
    x, y, z = ctx.world
    cell, edge = _VOR(x, y, z)
    v = 0.3 + 0.5 * cell - (0.25 if edge < 0.18 else 0.0) + (0.12 if ctx.face == "top" else 0.0)
    col = pick(ctx.pal["stone"], v)
    if ctx.face == "top" and noise3(x * 0.4, y * 0.4, z * 0.4, 311) > 0.62:
        col = pick(ctx.pal["moss"], hash01(ctx.u, ctx.v, 313))
    return col


def materials(variant: str) -> dict:
    return {"crystal": _crystal_paint(False), "crystal_inner": _crystal_paint(True), "rubble": _rubble}
