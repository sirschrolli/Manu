"""
Gemeinsame Maler der Wildtiere (E3b): Wildfell mit dunklem Sattel und heller Unterseite, Gesichter mit Augen und
Nase auf beliebigen Quaderflächen, Krallen, Zottelfell. Baut auf farm.py auf.
"""
from __future__ import annotations

import math

from ..paint import Ctx, hash01, mix, noise3, pick, rel_y
from . import farm


def coat(back: str = "fur", belly: str = "belly", saddle: str | None = "dark", seed: int = 101, contrast: float = 0.22,
         belly_from: float = 0.72, agouti: float = 0.06):
    """Wildfell: Rücken mit dunklerem Sattel, Bauch hell, einzelne dunkle und helle Haarspitzen."""
    base = farm.fur(back, belly, seed=seed, contrast=contrast, belly_from=belly_from, top_dark=0.06)

    def paint(ctx: Ctx):
        col = base(ctx)
        if col is None:
            return col
        if saddle is not None and (ctx.face == "top" or (ctx.face in ("west", "east") and rel_y(ctx) < 0.28)):
            x, y, z = ctx.world
            n = noise3(x * 0.35, y * 0.35, z * 0.35, seed + 4)
            col = mix(col, pick(ctx.pal[saddle], 0.45 + n * 0.2), 0.55 if ctx.face == "top" else 0.3)
        h = hash01(ctx.u, ctx.v, seed)
        if h > 1.0 - agouti:
            col = mix(col, pick(ctx.pal[saddle or back], 0.3), 0.5)
        elif h < agouti:
            col = mix(col, pick(ctx.pal[belly], 0.85), 0.4)
        return col

    return paint


def face_dot(ctx: Ctx, points: dict, face: str = "front"):
    """Malt einzelne Texel einer Fläche (s, t) -> (Schlüssel, Helligkeit); sonst None."""
    if ctx.face != face:
        return None
    key = points.get((ctx.s, ctx.t))
    if key is None:
        return None
    return pick(ctx.pal[key[0]], key[1])


def claws(base, key: str = "claw", from_rel: float = 0.85):
    """Pfoten: unten dunkle Krallen an der Vorderseite."""

    def paint(ctx: Ctx):
        if ctx.face == "front" and rel_y(ctx) > from_rel and ctx.s % 2 == 0:
            return pick(ctx.pal[key], 0.35)
        if ctx.face == "bottom":
            return pick(ctx.pal[key], 0.25)
        return base(ctx)

    return paint


def stripes_across(base, key: str, period: float = 3.0, width: float = 1.0, axis: int = 2, phase: float = 0.0,
                   strength: float = 0.75, wobble: float = 0.0):
    """Querstreifen entlang einer Achse (Tigerung, Bänder) mit leichter Welligkeit."""

    def paint(ctx: Ctx):
        col = base(ctx)
        if col is None:
            return col
        x, y, z = ctx.world
        c = (x, y, z)[axis] + math.sin((x + y + z) * 0.7) * wobble + phase
        if (c % period) < width:
            return mix(col, pick(ctx.pal[key], 0.4 + hash01(ctx.u, ctx.v) * 0.12), strength)
        return col

    return paint


def membrane(key: str = "wing", vein: str = "dark", spacing: float = 2.0):
    """Flughaut: dünn, mit Fingerknochen als Adern."""

    def paint(ctx: Ctx):
        x, y, z = ctx.world
        v = 0.45 + (hash01(ctx.u, ctx.v) - 0.5) * 0.12
        if abs(((x * 0.8 + y * 0.6) % spacing) - spacing / 2) < 0.35:
            return pick(ctx.pal[vein], 0.4)
        return pick(ctx.pal[key], v)

    return paint
