"""
Leuchtqualle: eine durchscheinende Glocke, die im Wasser pulsiert; in ihr glimmen vier Keimdrüsen wie eine Blüte, der
Saum leuchtet, darunter wehen vier gekräuselte Mundarme und acht lange, feine Fangfäden.

Drei Farben: Meerblau, Rosa und Bernstein. Die Glocke und die Fäden sind durchscheinend, Kern, Saum und Fäden leuchten
(eigene Leuchttextur). Knochen-Namen werden in GlowJellyfishModel (Java) für das Pulsieren und Wehen verwendet.
"""
from __future__ import annotations

import math

from ..geometry import Model
from ..paint import Ctx, hash01, hexc, mix, noise3, pick

PALETTES = {
    "glow_jellyfish_blue": {
        "bell": [hexc(c) for c in ("#1a4a7a", "#2a6aa8", "#3a8ccc", "#5ab0e8", "#8ad0f8", "#c4ecff")],
        "glow": [hexc(c) for c in ("#2a8ae0", "#4ac0ff", "#8ae6ff", "#d4faff")],
        "core": [hexc(c) for c in ("#3a5ad0", "#6a8af0", "#a8c4ff", "#e4eeff")],
    },
    "glow_jellyfish_pink": {
        "bell": [hexc(c) for c in ("#6a1a5a", "#9a2a82", "#c444a8", "#e070c8", "#f4a4e0", "#ffd8f4")],
        "glow": [hexc(c) for c in ("#d03aa8", "#f06ad0", "#ffa8ec", "#ffe4fa")],
        "core": [hexc(c) for c in ("#c02a6a", "#e8509a", "#ff90c4", "#ffe0f0")],
    },
    "glow_jellyfish_gold": {
        "bell": [hexc(c) for c in ("#6a3a10", "#a0601c", "#d08a2a", "#ecb04a", "#f8d48a", "#fff0cc")],
        "glow": [hexc(c) for c in ("#e09a2a", "#ffc04a", "#ffe08a", "#fff6d4")],
        "core": [hexc(c) for c in ("#d0602a", "#f0884a", "#ffb87a", "#ffe8cc")],
    },
}

TENTACLES = 8
CENTER = (0.0, 12.0, 0.0)


def build() -> Model:
    m = Model("glow_jellyfish", 96, 64)
    m.tex_res = 2
    m.bone("bell", pivot=CENTER)
    # die Glocke als flache Kuppel in fünf Stufen
    for size, top, height in ((2.6, -6.4, 0.6), (4.6, -5.8, 0.8), (6.6, -5.0, 1.0), (8.2, -4.0, 1.4), (9.4, -2.6, 2.2)):
        m.cube("bell", (-size / 2, top, -size / 2), (size, height, size), "bell")
    m.cube("bell", (-2.0, -3.6, -2.0), (4, 1.8, 4), "core")
    m.bone("rim", "bell", pivot=(0, -0.4, 0))
    m.cube("rim", (-5.5, 0, -5.5), (11, 1.4, 11), "rim")
    # vier gekräuselte Mundarme
    for k in range(4):
        a = math.radians(45 + 90 * k)
        name = f"arm_{k}"
        m.bone(name, "bell", pivot=(math.cos(a) * 1.8, 0.8, math.sin(a) * 1.8))
        m.cube(name, (-0.6, 0, -0.6), (1.2, 4.5, 1.2), "arm")
        m.cube(name, (-0.9, 1.0, -0.2), (1.8, 2.6, 0.4), "arm")                     # Krause
        low = name + "_low"
        m.bone(low, name, pivot=(0, 4.5, 0))
        m.cube(low, (-0.5, 0, -0.5), (1.0, 4.0, 1.0), "arm")
        m.cube(low, (-0.2, 0.6, -0.8), (0.4, 2.4, 1.6), "arm")
    # acht feine Fangfäden am Saum
    for k in range(TENTACLES):
        a = math.radians(22.5 + 45 * k)
        name = f"tentacle_{k}"
        m.bone(name, "bell", pivot=(math.cos(a) * 4.8, 0.9, math.sin(a) * 4.8))
        m.cube(name, (-0.3, 0, -0.3), (0.6, 4.5, 0.6), "thread")
        mid = name + "_mid"
        m.bone(mid, name, pivot=(0, 4.5, 0))
        m.cube(mid, (-0.25, 0, -0.25), (0.5, 4.5, 0.5), "thread")
        tip = name + "_tip"
        m.bone(tip, mid, pivot=(0, 4.5, 0))
        m.cube(tip, (-0.2, 0, -0.2), (0.4, 3.5, 0.4), "thread")
    return m


def _bell(ctx: Ctx):
    x, y, z = ctx.world
    r = math.hypot(x - CENTER[0], z - CENTER[2])
    v = 0.55 + (noise3(x * 0.4, y * 0.4, z * 0.4, 3) - 0.5) * 0.2 + (0.15 if ctx.face == "top" else 0.0)
    col = pick(ctx.pal["bell"], v)
    # feine, strahlenförmige Kanäle in der Glocke
    a = math.atan2(z - CENTER[2], x - CENTER[0])
    if abs(math.sin(a * 8)) < 0.12 and r > 1.2:
        col = mix(col, pick(ctx.pal["glow"], 0.6), 0.5)
        ctx.glow = mix((0, 0, 0, 255), pick(ctx.pal["glow"], 0.5), 0.45)
    alpha = 150 if ctx.face != "top" else 175
    return col[:3] + (alpha,)


def _core(ctx: Ctx):
    s = (ctx.s + 0.5) / max(ctx.w, 1)
    t = (ctx.t + 0.5) / max(ctx.h, 1)
    # vier Keimdrüsen wie ein Kleeblatt
    d = min(math.hypot(s - 0.28, t - 0.28), math.hypot(s - 0.72, t - 0.28), math.hypot(s - 0.28, t - 0.72),
            math.hypot(s - 0.72, t - 0.72))
    v = 0.9 - d * 1.6
    col = pick(ctx.pal["core"], v)
    ctx.glow = mix((0, 0, 0, 255), col, 0.95)
    return col[:3] + (230,)


def _rim(ctx: Ctx):
    # der Saum: zart durchscheinend, nur seine Unterkante leuchtet als feine Linie
    col = pick(ctx.pal["bell"], 0.6)
    if ctx.face == "bottom":
        edge = min(ctx.s, ctx.w - 1 - ctx.s, ctx.t, ctx.h - 1 - ctx.t)
        if edge == 0:
            col = pick(ctx.pal["glow"], 0.55)
            ctx.glow = mix((0, 0, 0, 255), col, 0.8)
            return col[:3] + (210,)
        return col[:3] + (110,)
    if ctx.face not in ("top",) and ctx.t == ctx.h - 1:
        col = pick(ctx.pal["glow"], 0.5)
        ctx.glow = mix((0, 0, 0, 255), col, 0.7)
        return col[:3] + (200,)
    return col[:3] + (135,)


def _arm(ctx: Ctx):
    x, y, z = ctx.world
    # die Mundarme: zart, oben kräftiger, nach unten blasser, mit hellen, gekräuselten Rändern
    fade = min(1.0, max(0.0, (y - 12.0) / 10.0))
    col = pick(ctx.pal["bell"], 0.75 - fade * 0.25)
    alpha = round(165 - fade * 45)
    edge = ctx.face in ("west", "east", "front", "back") and (ctx.s == 0 or ctx.s == ctx.w - 1)
    if edge or hash01(ctx.u, ctx.v, 31) < 0.05:
        col = pick(ctx.pal["glow"], 0.6)
        ctx.glow = mix((0, 0, 0, 255), col, 0.45 - fade * 0.2)
        alpha = 185
    return col[:3] + (alpha,)


def _thread(ctx: Ctx):
    x, y, z = ctx.world
    # ein zarter Faden, der nach unten blasser wird, mit leuchtenden Nesselknoten in Abständen
    fade = min(1.0, max(0.0, (y - 12.0) / 16.0))
    col = pick(ctx.pal["glow"], 0.45 - fade * 0.25)
    alpha = round(185 - fade * 70)
    if int(y * 2) % 7 == 0:
        col = pick(ctx.pal["glow"], 0.8)
        alpha = 220
    ctx.glow = mix((0, 0, 0, 255), col, 0.6 - fade * 0.3)
    return col[:3] + (alpha,)


def materials(variant: str) -> dict:
    return {"bell": _bell, "core": _core, "rim": _rim, "arm": _arm, "thread": _thread}
