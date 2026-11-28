"""
Korallenspieße (0.97) – die Spur der Meerhexe: dieselbe Form wie die Mondsteinkristalle (moon_crystal.py, gleiche
Geometrie und Textur-Anordnung), aber als Büschel aus Korallen: dunkelrot am Fuß, zu den Spitzen leuchtend korallenrot und
rosa, mit hellen Polypen; am Fuß Brocken aus Gezeitenstein mit Algen. Die Spitzen leuchten.
"""
from __future__ import annotations

from ..paint import Ctx, Voronoi3D, hash01, hexc, mix, noise3, pick
from . import moon_crystal

PALETTES = {
    "coral_spike": {
        "coral": [hexc(c) for c in ("#3a0612", "#5a0c1c", "#7e1428", "#a82238", "#d03a4a", "#f06a6a", "#ffa08a", "#ffd4c0")],
        "glow": [hexc(c) for c in ("#d02a4a", "#f05a6a", "#ff8a8a", "#ffb8a8", "#ffe0d4", "#fff4ee")],
        "polyp": [hexc(c) for c in ("#ffd0e0", "#ffffff")],
        "stone": [hexc(c) for c in ("#2a4644", "#365a56", "#46706a", "#58867e", "#6c9a90", "#80aea2", "#94c0b4")],
        "algae": [hexc(c) for c in ("#1a5a2a", "#2a7a3a", "#3a9a4a", "#5ab86a")],
    },
}


def build():
    return moon_crystal.build()


def _coral_paint(inner: bool):
    def paint(ctx: Ctx):
        t = moon_crystal._seg_t(ctx)
        knob = ((ctx.s * 2 + ctx.t) // 2) % 4 == 0
        v = 0.18 + 0.66 * t + (0.08 if knob else 0.0) + (0.05 if inner else 0.0)
        if ctx.face in ("west", "front"):
            v += 0.05
        col = pick(ctx.pal["coral"], v)
        if hash01(ctx.u, ctx.v, 911) > 0.9:
            col = pick(ctx.pal["polyp"], 0.5 + 0.5 * t)               # helle Polypen
        if t > 0.55:
            g = pick(ctx.pal["glow"], (t - 0.55) / 0.45)
            ctx.glow = g
            col = mix(col, g, 0.35 + 0.4 * (t - 0.55))
        return col
    return paint


_VOR = Voronoi3D(2.2, 917)


def _rubble(ctx: Ctx):
    x, y, z = ctx.world
    cell, edge = _VOR(x, y, z)
    v = 0.3 + 0.5 * cell - (0.25 if edge < 0.18 else 0.0) + (0.12 if ctx.face == "top" else 0.0)
    col = pick(ctx.pal["stone"], v)
    if ctx.face == "top" and noise3(x * 0.4, y * 0.4, z * 0.4, 919) > 0.58:
        col = pick(ctx.pal["algae"], hash01(ctx.u, ctx.v, 923))
    return col


def materials(variant: str) -> dict:
    return {"crystal": _coral_paint(False), "crystal_inner": _coral_paint(True), "rubble": _rubble}
