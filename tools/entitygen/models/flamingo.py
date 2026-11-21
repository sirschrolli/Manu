"""
Flamingo der Korallenküste (0.86): rosa Gefieder (beim Kubaflamingo kräftig rotrosa), lange dünne Läufe mit dem nach hinten
knickenden Fersengelenk und Schwimmfüßen, ein langer S-Hals, der kleine Kopf mit blassgelbem Auge und dem geknickten
Schnabel – hell am Grund, schwarz an der Spitze. Die Flügel: rote Deckfedern, schwarze Schwungfedern (angelegt nur als
schwarzer Streif über dem Schwanz zu sehen).

Knochen (für FlamingoModel in Java): body (mit neck1 → neck2 → neck3 → head → beak_tip; wing_l → wing_l_tip,
wing_r → wing_r_tip; tail), die Läufe left_leg → left_shin → left_foot, right_leg → right_shin → right_foot.
Ruhehaltung: stehend, der Hals im S, Flügel ausgebreitet (Flug); zum Stehen legt das Modell sie an.
"""
from __future__ import annotations

import math

from ..geometry import Model
from ..paint import Ctx, hash01, hexc, mix, pick, rel_y

_COMMON = {
    "white": [hexc(c) for c in ("#c8b8bc", "#ddd0d2", "#ece2e3", "#f6f0f0", "#fffafa")],
    "black": [hexc(c) for c in ("#0c0a0c", "#18141a", "#241e26", "#342c36", "#463c48")],
    "eye": [hexc(c) for c in ("#0c0a08", "#d8c060", "#f0dc88", "#fff4c0")],
    "beak": [hexc(c) for c in ("#c89aa0", "#dcb4b8", "#ecccd0", "#f6e2e4", "#fff2f2")],
}

PALETTES = {
    "flamingo": {**_COMMON,
                 "plume": [hexc(c) for c in ("#c46c80", "#d8849a", "#e89eb2", "#f2b6c6", "#f8cad6", "#fcdce4", "#ffeef2")],
                 "covert": [hexc(c) for c in ("#a8203e", "#c43452", "#dc4c68", "#ec6a84", "#f68aa0", "#faa8ba")],
                 "leg": [hexc(c) for c in ("#a8485a", "#c45e70", "#da7686", "#ea909e", "#f4aab6", "#fcc4cc")]},
    "flamingo_crimson": {**_COMMON,
                         "plume": [hexc(c) for c in ("#b03c58", "#c84c68", "#dc6280", "#ea7a96", "#f494ac", "#faaec0", "#fecad6")],
                         "covert": [hexc(c) for c in ("#8c1430", "#a81e3e", "#c22c4c", "#d8405e", "#e85c76", "#f47e94")],
                         "leg": [hexc(c) for c in ("#9c3448", "#b8485c", "#d06072", "#e47a8a", "#f096a4", "#f8b4be")]},
}

_LIT = {"top": 0.08, "front": 0.02, "west": 0.0, "east": -0.02, "back": -0.04, "bottom": -0.12}


def build() -> Model:
    m = Model("flamingo", 64, 64)
    m.tex_res = 2
    m.world_faces = True
    # Leib: rundlich, vorn die Brust, hinten der kurze, spitze Schwanz
    m.bone("body", pivot=(0, 10.0, 0.5))
    m.cube("body", (-2.2, -2.4, -3.4), (4.4, 4.6, 7.4), "body")
    m.cube("body", (-1.9, -2.0, -4.2), (3.8, 3.8, 1.0), "body")                 # die Brust
    m.cube("body", (-1.8, -2.9, -2.6), (3.6, 0.6, 5.4), "body")                 # gerundeter Rücken
    m.cube("body", (-1.8, 2.1, -2.4), (3.6, 0.6, 5.0), "body")                  # gerundeter Bauch
    m.bone("tail", "body", pivot=(0, -1.6, 3.8), rotation=(12, 0, 0))
    m.cube("tail", (-1.4, -0.6, -0.4), (2.8, 1.4, 2.6), "tail")
    # Hals im S: aus der Brust steil hinauf, oben nach vorn, der Kopf waagerecht
    m.bone("neck1", "body", pivot=(0, -1.4, -3.4), rotation=(-12, 0, 0))
    m.cube("neck1", (-0.75, -4.4, -0.75), (1.5, 4.6, 1.5), "neck")
    m.bone("neck2", "neck1", pivot=(0, -4.2, 0), rotation=(-22, 0, 0))
    m.cube("neck2", (-0.7, -4.2, -0.7), (1.4, 4.4, 1.4), "neck")
    m.bone("neck3", "neck2", pivot=(0, -4.0, 0), rotation=(48, 0, 0))
    m.cube("neck3", (-0.65, -2.8, -0.65), (1.3, 3.0, 1.3), "neck")
    m.bone("head", "neck3", pivot=(0, -2.6, 0), rotation=(-14, 0, 0))
    m.cube("head", (-1.0, -1.7, -1.9), (2.0, 2.0, 2.8), "head")
    m.cube("head", (0.98, -1.1, -1.2), (0.1, 0.6, 0.6), "eye")
    m.cube("head", (-1.08, -1.1, -1.2), (0.1, 0.6, 0.6), "eye")
    m.cube("head", (-0.6, -1.3, -4.0), (1.2, 1.2, 2.2), "beak")                # der dicke Schnabelgrund
    m.bone("beak_tip", "head", pivot=(0, -0.5, -3.8), rotation=(52, 0, 0))     # der Knick
    m.cube("beak_tip", (-0.5, -0.5, -1.6), (1.0, 1.1, 1.8), "beak_tip")
    # Flügel: in der Ruhehaltung ausgebreitet (Flug)
    for side, sx in (("l", 1), ("r", -1)):
        lo = (lambda a, w: a if sx > 0 else -a - w)
        m.bone(f"wing_{side}", "body", pivot=(1.9 * sx, -1.9, -2.4))
        m.cube(f"wing_{side}", (lo(0, 6.5), -0.3, -0.4), (6.5, 0.6, 4.8), "wing")
        m.bone(f"wing_{side}_tip", f"wing_{side}", pivot=(6.5 * sx, 0, 0))
        m.cube(f"wing_{side}_tip", (lo(0, 5.0), -0.25, -0.3), (5.0, 0.5, 4.2), "wing_tip")
    # Läufe: Unterschenkel bis zum Fersengelenk, Lauf bis zum Fuß, Schwimmfuß
    for side, sx in (("left", 1), ("right", -1)):
        m.bone(f"{side}_leg", pivot=(1.0 * sx, 12.0, 1.0))
        m.cube(f"{side}_leg", (-0.3, 0, -0.3), (0.6, 6.0, 0.6), "leg")
        m.bone(f"{side}_shin", f"{side}_leg", pivot=(0, 6.0, 0))
        m.cube(f"{side}_shin", (-0.35, -0.3, -0.35), (0.7, 0.8, 0.7), "knee")
        m.cube(f"{side}_shin", (-0.28, 0, -0.28), (0.56, 5.8, 0.56), "leg")
        m.bone(f"{side}_foot", f"{side}_shin", pivot=(0, 5.8, 0))
        m.cube(f"{side}_foot", (-0.9, 0, -1.7), (1.8, 0.2, 2.1), "foot")
    return m


# =============================================================================================
# Bemalung
# =============================================================================================

def _soft(ctx: Ctx, key: str, v: float) -> tuple:
    x, y, z = ctx.world
    n = hash01(int(math.floor(x * 2)), int(math.floor(y * 1.5)), int(math.floor(z * 2)), 901)
    return pick(ctx.pal[key], v + _LIT[ctx.face] + (n - 0.5) * 0.06)


def _body(ctx: Ctx):
    """Rosa, oben (unter den angelegten Flügeln) kräftiger, der Bauch blasser; feine Federreihen."""
    x, y, z = ctx.world
    ry = rel_y(ctx) if ctx.face not in ("top", "bottom") else (0.0 if ctx.face == "top" else 1.0)
    v = 0.58 - (1.0 - ry) * 0.08 + ry * 0.1
    row = (y * 0.9 + (math.floor(z / 1.6) % 2) * 0.5) % 1.0 if ctx.face in ("west", "east", "front", "back") else (z * 0.7) % 1.0
    if row > 0.82:
        v -= 0.06
    return _soft(ctx, "plume", v)


def _tail(ctx: Ctx):
    lz = ctx.local[2]
    if ctx.face == "top" and lz > 1.2:
        return _soft(ctx, "black", 0.3)                                      # die schwarzen Schwungfedern über dem Schwanz
    return _soft(ctx, "plume", 0.5)


def _neck(ctx: Ctx):
    return _soft(ctx, "plume", 0.66 + (0.04 if (ctx.world[1] * 1.2) % 1.0 > 0.8 else 0.0))


def _head(ctx: Ctx):
    lz = ctx.local[2]
    v = 0.7
    if lz < -1.2 and ctx.lface in ("west", "east", "front", "bottom"):
        return _soft(ctx, "white", 0.6)                                      # das helle Gesicht am Schnabelgrund
    return _soft(ctx, "plume", v)


def _eye(ctx: Ctx):
    if ctx.lface not in ("west", "east"):
        return pick(ctx.pal["plume"], 0.6)
    rs = (ctx.s + 0.5) / max(ctx.w, 1)
    rt = (ctx.t + 0.5) / max(ctx.h, 1)
    if abs(rs - 0.5) < 0.22 and abs(rt - 0.5) < 0.22:
        return pick(ctx.pal["eye"], 0.0)
    return pick(ctx.pal["eye"], 0.66)


def _beak(ctx: Ctx):
    """Schnabelgrund: hell rosa-weiß, vorn schon dunkler."""
    lz = ctx.local[2]
    if lz < -3.4:
        return pick(ctx.pal["black"], 0.35 + _LIT[ctx.face])
    return pick(ctx.pal["beak"], 0.62 + _LIT[ctx.face] + (lz + 4.0) * 0.04)


def _beak_tip(ctx: Ctx):
    return pick(ctx.pal["black"], 0.3 + _LIT[ctx.face])


def _wing(ctx: Ctx):
    """Armflügel: oben rote Deckfedern, hinten die schwarzen Schwungfedern; unten rosa, hinten schwarz."""
    lz = ctx.local[2]
    span = abs(ctx.local[0])
    chord = lz + 0.4
    if ctx.lface == "front":
        return _soft(ctx, "covert", 0.6)
    if chord > 3.2:
        k = (span / 1.3) % 1.0
        return pick(ctx.pal["black"], 0.25 + (0.1 if k < 0.15 else 0.0) + _LIT[ctx.face])
    if ctx.lface == "bottom":
        return _soft(ctx, "plume", 0.62)
    row = math.floor(chord / 1.1)
    ap = span / 1.4 + (row % 2) * 0.5
    fb = chord / 1.1 - row
    v = 0.5 + (hash01(int(ap), row, 902) - 0.5) * 0.14 + (0.08 if fb > 0.75 else 0.0)
    return pick(ctx.pal["covert"], v + _LIT[ctx.face])


def _wing_tip(ctx: Ctx):
    """Handflügel: vorn ein Saum roter Deckfedern, sonst schwarz; die Spitze gefingert."""
    lz = ctx.local[2]
    span = abs(ctx.local[0])
    chord = lz + 0.3
    if span > 3.0:
        fan = chord / 4.2 * 4.0
        f = min(3, int(math.floor(fan)))
        ff = fan - f
        reach = 5.0 - abs(f - 1.0) * 0.5
        if ff < 0.16 + (span - 3.0) * 0.1 or span > reach:
            return None
    if chord < 1.0 and span < 2.5 and ctx.lface in ("top", "front"):
        return _soft(ctx, "covert", 0.55)
    if ctx.lface == "front":
        return pick(ctx.pal["black"], 0.3)
    k = (span / 1.2) % 1.0
    return pick(ctx.pal["black"], 0.24 + (0.1 if k < 0.14 else 0.0) + _LIT[ctx.face])


def _leg(ctx: Ctx):
    x, y, z = ctx.world
    return pick(ctx.pal["leg"], 0.55 + _LIT[ctx.face] + (0.06 if (y * 2.0) % 1.0 > 0.8 else 0.0))


def _knee(ctx: Ctx):
    return pick(ctx.pal["leg"], 0.32 + _LIT[ctx.face])


def _foot(ctx: Ctx):
    rs = (ctx.s + 0.5) / max(ctx.w, 1)
    if ctx.face == "top" and any(abs(rs - f) < 0.07 for f in (0.2, 0.5, 0.8)):
        return pick(ctx.pal["leg"], 0.28)
    return pick(ctx.pal["leg"], 0.5 + _LIT[ctx.face])


def materials(variant: str) -> dict:
    return {"body": _body, "tail": _tail, "neck": _neck, "head": _head, "eye": _eye, "beak": _beak, "beak_tip": _beak_tip,
            "wing": _wing, "wing_tip": _wing_tip, "leg": _leg, "knee": _knee, "foot": _foot}
