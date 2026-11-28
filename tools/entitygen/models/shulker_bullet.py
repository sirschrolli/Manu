"""
Schreinfunke (statt Shulker-Geschoss): ein Stern aus drei gekreuzten Kristallnadeln um einen glühenden Kern (Knochen
"main" wie beim Vanilla-Geschoss, das Spiel dreht ihn). Die Nadeln laufen spitz zu und leuchten violett.
"""
from __future__ import annotations

from ..geometry import Model
from ..paint import Ctx, hash01, hexc

PALETTES = {
    "schreinfunke": {"crystal": [hexc(c) for c in ("#3a1460", "#6a24a8", "#a040e0", "#d080ff", "#f4d0ff", "#ffffff")]},
}


def build() -> Model:
    m = Model("shulker_bullet", 32, 32)
    m.tex_res = 2
    m.bone("main")
    m.cube("main", (-1.5, -1.5, -1.5), (3, 3, 3), "core")
    m.cube("main", (-0.75, -4.5, -0.75), (1.5, 9, 1.5), "needle")
    m.cube("main", (-4.5, -0.75, -0.75), (9, 1.5, 1.5), "needle")
    m.cube("main", (-0.75, -0.75, -4.5), (1.5, 1.5, 9), "needle")
    return m


def _lit(ctx: Ctx, v: float):
    pal = ctx.pal["crystal"]
    col = pal[max(0, min(len(pal) - 1, int(v * len(pal))))]
    ctx.glow = col
    return col


def _core(ctx: Ctx):
    return _lit(ctx, 0.9 + hash01(ctx.u, ctx.v) * 0.1)


def _needle(ctx: Ctx):
    """Kristallnadel: zu beiden Enden spitz, innen hell."""
    size = ctx.cube.size
    axis = max(range(3), key=lambda i: size[i])
    o, p = ctx.cube.origin, ctx.local
    f = (p[axis] - o[axis]) / size[axis]
    end = abs(f - 0.5) * 2.0                                            # 0 Mitte … 1 Spitze
    face_axis = {"west": 0, "east": 0, "top": 1, "bottom": 1, "front": 2, "back": 2}[ctx.face]
    if face_axis == axis:
        return None                                                     # die Stirnseiten fehlen: spitze Enden
    m = 3 - axis - face_axis
    across = abs((p[m] - o[m]) / size[m] - 0.5) * 2.0
    if across > 1.05 - end ** 1.3:
        return None
    return _lit(ctx, 0.62 - end * 0.45 - across * 0.12)


def materials(variant: str) -> dict:
    return {"core": _core, "needle": _needle}


def hidden(variant: str) -> list[str]:
    return []
