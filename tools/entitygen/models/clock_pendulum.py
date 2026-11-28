"""
Das Pendel des Stundenwächters (1.01): eine lange Stange aus Uhrbronze, oben ein Zahnrad als Lager, unten eine
sichelförmige Klinge aus gebläutem Stahl um eine runde Pendelscheibe mit Chronit und zwölf Stundenmarken.

Alles hängt nach unten (+y): rod ist EIN Stück der Stange (ein Block lang, Java setzt es so oft wie nötig aneinander),
cap ist das Lager am Aufhängepunkt (Scheibe quer zu x, dreht sich mit), blade ist die Klinge (ihre Oberkante im Ursprung,
breit entlang x, dünn entlang z – sie schwingt entlang z). Knochen: rod, cap, blade (> blade_0..6).
"""
from __future__ import annotations

import math

from ..geometry import Model
from ..paint import Ctx, hexc, pick

PALETTES = {
    "clock_pendulum": {
        "bronze": [hexc(c) for c in ("#1e1006", "#331c0a", "#4e2d10", "#6c4016", "#8c561e", "#aa6e28", "#c38634", "#d8a046",
                                     "#e8bc62", "#f6d88e", "#fff2c8")],
        "brass": [hexc(c) for c in ("#3a2608", "#5a3c0e", "#7c5716", "#a07420", "#c4942e", "#deb44a", "#f0d27a", "#fff0b8")],
        "blued": [hexc(c) for c in ("#06081a", "#0b1030", "#121a4a", "#1b2766", "#283a86", "#3a52a6", "#5670c2", "#7e96dc",
                                    "#b4c6f2")],
        "amber": [hexc(c) for c in ("#6a2c06", "#a2480c", "#d26a16", "#f08f24", "#ffb347", "#ffd27e", "#fff0c4")],
        "enamel": [hexc(c) for c in ("#6e6250", "#8f826c", "#ad9f86", "#c7baa0", "#dcd0b6", "#ebe1ca", "#f6efdd", "#fffaf0")],
    }
}

BOB = 30.0
ARC = 26.0
SEGMENTS = 7


def build() -> Model:
    m = Model("clock_pendulum", 128, 128)
    m.tex_res = 1
    m.bone("rod")
    m.cube("rod", (-1.0, 0.0, -1.0), (2.0, 16.0, 2.0), "rod")
    m.bone("cap")
    m.cube("cap", (-1.5, -10.0, -10.0), (3.0, 20.0, 20.0), "cap", faces=("west", "east"))
    m.cube("cap", (-2.5, -2.0, -2.0), (5.0, 4.0, 4.0), "axle")
    m.bone("blade")
    # die Pendelscheibe: Bronzering, Email mit Stundenmarken, in der Mitte Chronit
    m.cube("blade", (-BOB / 2.0, 0.0, -1.5), (BOB, BOB, 3.0), "bob", faces=("front", "back"))
    # die Sichel darunter: sieben Stücke auf einem Bogen um die Mitte der Scheibe
    cy = BOB / 2.0
    for i in range(SEGMENTS):
        a = math.radians(-72.0 + i * 144.0 / (SEGMENTS - 1))
        b = f"blade_{i}"
        m.bone(b, "blade", pivot=(math.sin(a) * ARC, cy + math.cos(a) * ARC, 0.0), rotation=(0.0, 0.0, -math.degrees(a)))
        m.cube(b, (-6.6, -4.0, -1.0), (13.2, 7.0, 2.0), "sickle", faces=("front", "back", "bottom"))
    return m


def _rod(ctx: Ctx):
    v = 0.55 + (0.15 if ctx.s == 0 else 0.0) - (0.15 if ctx.s == ctx.w - 1 else 0.0)
    if ctx.t % 16 in (0, 15):
        return pick(ctx.pal["brass"], 0.75)
    return pick(ctx.pal["bronze"], v + (0.08 if ctx.face == "front" else 0.0))


def _cap(ctx: Ctx):
    ox, oy, oz = ctx.cube.origin
    sx, sy, sz = ctx.cube.size
    u = ctx.local[2] - (oz + sz / 2.0)
    v = (oy + sy / 2.0) - ctx.local[1]
    R = sy / 2.0
    r = math.hypot(u, v)
    a = math.atan2(v, u)
    if r > R:
        return None
    if r > R * 0.82:
        k = (a / (2.0 * math.pi) * 14) % 1.0
        if abs(k - 0.5) > 0.24:
            return None
        return pick(ctx.pal["brass"], 0.62)
    if R * 0.3 < r < R * 0.62 and 0.18 < (a / (2.0 * math.pi) * 6) % 1.0 < 0.82:
        return None
    return pick(ctx.pal["bronze"], 0.6 + (0.08 if v > 0 else -0.04))


def _axle(ctx: Ctx):
    return pick(ctx.pal["brass"], 0.7 if ctx.face in ("west", "east") else 0.5)


def _bob(ctx: Ctx):
    ox, oy, oz = ctx.cube.origin
    sx, sy = ctx.cube.size[0], ctx.cube.size[1]
    u = (ctx.local[0] - (ox + sx / 2.0)) * (1.0 if ctx.lface == "front" else -1.0)
    v = (oy + sy / 2.0) - ctx.local[1]
    R = sx / 2.0
    r = math.hypot(u, v)
    if r > R:
        return None
    k = r / R
    a = math.atan2(u, v)
    if k > 0.86:
        return pick(ctx.pal["brass"], 0.62 + (0.12 if v > 0 else -0.06))
    if k > 0.8:
        return pick(ctx.pal["bronze"], 0.3)
    hour = a / (2.0 * math.pi) * 12.0
    if k > 0.6 and abs(hour - round(hour)) * 2.0 * math.pi / 12.0 * r < (1.1 if round(hour) % 3 == 0 else 0.6):
        col = pick(ctx.pal["amber"], 0.75)
        ctx.glow = col
        return col
    if k > 0.3:
        return pick(ctx.pal["enamel"], 0.7 + (0.05 if v > 0 else 0.0))
    col = pick(ctx.pal["amber"], 0.9 - k)
    ctx.glow = col
    return col


def _sickle(ctx: Ctx):
    """Die Sichel: gebläuter Stahl, die Unterkante (die Schneide) hell, oben ein Messingsaum."""
    if ctx.face == "bottom":
        return pick(ctx.pal["blued"], 0.92)
    rel = ctx.t / max(1, ctx.h - 1)
    if rel < 0.18:
        return pick(ctx.pal["brass"], 0.66)
    if rel > 0.8:
        return pick(ctx.pal["blued"], 0.9 - (1.0 - rel) * 0.4)
    return pick(ctx.pal["blued"], 0.4 + rel * 0.25 + (0.08 if ctx.s % 6 == 0 else 0.0))


def materials(variant: str) -> dict:
    return {"rod": _rod, "cap": _cap, "axle": _axle, "bob": _bob, "sickle": _sickle}
