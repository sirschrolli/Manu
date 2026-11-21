"""
Lichttaube: eine kleine Taube aus weißem, sanft leuchtendem Licht, mit goldenem Schnabel und lila schimmernden
Federspitzen – MissMystik lässt sie mit einem Fingerschnippen aufflattern. Die Flügel ("left_wing", "right_wing") schlagen
im Java-Renderer, der Schwanz ("tail") wippt.
"""
from __future__ import annotations

from ..geometry import Model
from ..paint import Ctx, hash01, mix, pick, ramp

PALETTES = {
    "light_dove": {
        "white": ramp("#b8b0d8", "#ffffff", steps=6),
        "tip": ramp("#9a7ae0", "#f0dcff", steps=5),
        "gold": ramp("#c08a20", "#fff2b8", steps=5),
        "eye": ramp("#4a2a8a", "#c8a8ff", steps=4),
    },
}


def build() -> Model:
    m = Model("light_dove", 32, 32)
    m.tex_res = 2
    m.bone("dove", pivot=(0, 0, 0))
    m.cube("dove", (-1.25, -1.25, -2.5), (2.5, 2.5, 5.0), "body")
    m.bone("head", "dove", pivot=(0, -0.9, -2.3))
    m.cube("head", (-1.0, -1.75, -1.75), (2.0, 2.0, 2.0), "head")
    m.cube("head", (-0.4, -0.75, -2.5), (0.8, 0.6, 0.9), "beak")
    m.bone("tail", "dove", pivot=(0, -0.5, 2.4))
    m.cube("tail", (-1.5, -0.25, 0), (3.0, 0.5, 3.0), "tail")
    for side, sx in (("left", 1), ("right", -1)):
        wing = f"{side}_wing"
        m.bone(wing, "dove", pivot=(1.25 * sx, -0.9, -0.6))
        m.cube(wing, (0 if sx > 0 else -5.0, -0.15, -1.5), (5.0, 0.3, 4.0), "wing")
    return m


def _body(ctx: Ctx):
    v = 0.86 + (0.08 if ctx.face == "top" else -0.08 if ctx.face == "bottom" else 0.0) + (hash01(ctx.u, ctx.v) - 0.5) * 0.06
    col = pick(ctx.pal["white"], v)
    ctx.glow = mix((0, 0, 0, 255), col, 0.7)
    return col


def _head(ctx: Ctx):
    if ctx.face in ("west", "east") and ctx.t == 1 and ctx.s == 1:
        col = pick(ctx.pal["eye"], 0.3)
        return col
    return _body(ctx)


def _beak(ctx: Ctx):
    col = pick(ctx.pal["gold"], 0.75)
    ctx.glow = col
    return col


def _wing(ctx: Ctx):
    """Flügel: weiß, nach außen lila Federspitzen, der Rand gezackt (durchsichtig)."""
    if ctx.face not in ("top", "bottom"):
        return pick(ctx.pal["white"], 0.8)
    lx, _, lz = ctx.local
    out = abs(lx) / 5.0
    if out > 0.82 and int((lz + 1.5) * 2) % 2 == 0:
        return None
    if out > 0.6:
        col = pick(ctx.pal["tip"], 0.5 + (1.0 - out) * 0.8)
    else:
        col = pick(ctx.pal["white"], 0.9 - out * 0.2)
    ctx.glow = mix((0, 0, 0, 255), col, 0.7)
    return col


def _tail(ctx: Ctx):
    if ctx.face in ("top", "bottom"):
        _, _, lz = ctx.local
        lx = ctx.local[0]
        if lz > 2.2 and int((lx + 1.5) * 2) % 2 == 1:
            return None
        col = pick(ctx.pal["tip"] if lz > 1.8 else ctx.pal["white"], 0.7)
        ctx.glow = mix((0, 0, 0, 255), col, 0.7)
        return col
    return pick(ctx.pal["white"], 0.8)


def materials(variant: str) -> dict:
    return {"body": _body, "head": _head, "beak": _beak, "wing": _wing, "tail": _tail}
