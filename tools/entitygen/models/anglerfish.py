"""
Anglerfisch: ein Schrecken der Tiefsee – ein plumper, fast schwarzer Leib voller Warzen, ein riesiges Maul mit
vorgeschobenem Unterkiefer und langen, nadelspitzen, durchscheinenden Zähnen, winzige milchige Augen. Aus der Stirn
wächst eine Angel, an deren Spitze ein Köder blau-weiß leuchtet; in der Dunkelheit sieht man oft nur ihn.

Knochen-Namen werden in AnglerfishModel (Java) für Schwimmen, Schnappen und das Pendeln der Angel verwendet.
"""
from __future__ import annotations

import math

from ..geometry import Model
from ..paint import Ctx, hash01, hexc, mix, noise3, pick

PALETTES = {
    "anglerfish": {
        "skin": [hexc(c) for c in ("#120e10", "#1c1618", "#271e1f", "#342828", "#423332", "#54423e")],
        "belly": [hexc(c) for c in ("#3a3030", "#4a3e3c", "#5c4e4a", "#6e5e58")],
        "wart": [hexc(c) for c in ("#4a3a34", "#5e4a40", "#76604e")],
        "photo": [hexc(c) for c in ("#1a6a9a", "#3aa8e0", "#8ae0ff")],
        "mouth": [hexc(c) for c in ("#2a0e14", "#4a1822", "#6e2a34")],
        "tooth": [hexc(c) for c in ("#8a9aa4", "#c4d0d6", "#eef4f6")],
        "eye": [hexc(c) for c in ("#5a6a72", "#9aaab2", "#d4e0e4")],
        "lure": [hexc(c) for c in ("#2a8ad0", "#5ac4ff", "#a8ecff", "#f0ffff")],
        "fin": [hexc(c) for c in ("#1a1416", "#2a2224", "#3a3032")],
    },
}

BODY = (0.0, 17.0, 0.0)


def build() -> Model:
    m = Model("anglerfish", 128, 96)
    m.tex_res = 2
    m.bone("body", pivot=BODY)
    m.cube("body", (-5.0, -5.0, -4.0), (10, 9, 10), "skin")
    m.cube("body", (-5.8, -4.0, -3.0), (11.6, 7, 8), "skin")                        # runde Flanken
    m.cube("body", (-4.0, -6.0, -3.0), (8, 1, 8), "skin")                           # Buckel
    m.cube("body", (-3.0, -6.6, -1.5), (6, 0.6, 5), "skin")
    m.cube("body", (-4.0, 4.0, -2.0), (8, 0.8, 7), "belly")                         # Bauch
    m.cube("body", (-5.5, -4.0, -5.0), (11, 5.5, 2), "skin")                        # Oberkiefer (vorn)
    m.cube("body", (-4.6, 1.4, -4.4), (9.2, 2.4, 0.6), "throat")                    # Schlund hinter den Zähnen
    # obere Zahnreihe: lange Nadeln, die nach unten hängen
    for k, x in enumerate((-4.4, -2.6, -0.8, 1.0, 2.8, 4.4)):
        m.cube("body", (x - 0.3, 1.5, -4.9), (0.6, 2.4 if k % 2 == 0 else 1.6, 0.6), "tooth")
    for side, sx in (("left", 1), ("right", -1)):
        m.cube("body", ((5.3 if sx > 0 else -5.9), -3.4, -4.4), (0.6, 1.0, 1.0), "eye")
        pec = f"{side}_pectoral"
        m.bone(pec, "body", pivot=(5.6 * sx, 1.5, 1.0), rotation=(0, -20 * sx, 25 * sx))
        m.cube(pec, (0 if sx > 0 else -4, -0.4, -1.0), (4, 0.8, 3.5), "fin")

    m.bone("jaw", "body", pivot=(0, 3.5, 2.5))
    m.cube("jaw", (-5.5, 0, -8.5), (11, 2.6, 8.5), "jawskin")
    for k, x in enumerate((-4.8, -3.0, -1.2, 0.6, 2.4, 4.2)):
        m.cube("jaw", (x, -2.8 if k % 2 == 0 else -2.0, -8.3), (0.6, 2.8 if k % 2 == 0 else 2.0, 0.6), "tooth")
    m.cube("jaw", (-5.2, -1.6, -5.5), (0.6, 1.6, 0.6), "tooth")
    m.cube("jaw", (4.6, -1.6, -5.5), (0.6, 1.6, 0.6), "tooth")

    m.bone("rod", "body", pivot=(0, -6.0, -2.0), rotation=(-35, 0, 0))
    m.cube("rod", (-0.35, -6.5, -0.35), (0.7, 6.5, 0.7), "rod")
    m.bone("rod_tip", "rod", pivot=(0, -6.5, 0), rotation=(75, 0, 0))
    m.cube("rod_tip", (-0.3, -5.0, -0.3), (0.6, 5.0, 0.6), "rod")
    m.bone("lure", "rod_tip", pivot=(0, -5.0, 0))
    m.cube("lure", (-1.1, -1.1, -1.1), (2.2, 2.2, 2.2), "lure")
    m.cube("lure", (-0.7, -1.5, -0.7), (1.4, 0.4, 1.4), "lure")
    m.cube("lure", (-0.4, 1.1, -0.4), (0.8, 0.9, 0.8), "lure")
    m.cube("lure", (-0.6, 1.9, -0.15), (0.3, 1.6, 0.3), "filament")                # zwei leuchtende Fäden
    m.cube("lure", (0.3, 1.9, -0.15), (0.3, 1.2, 0.3), "filament")

    m.bone("dorsal", "body", pivot=(0, -5.5, 3.5), rotation=(-20, 0, 0))
    m.cube("dorsal", (-0.3, -2.5, -1.0), (0.6, 2.5, 3.5), "fin")
    m.bone("tail", "body", pivot=(0, -0.5, 6.0))
    m.cube("tail", (-2.8, -3.0, 0), (5.6, 6.0, 3.5), "skin")
    m.bone("tail_fin", "tail", pivot=(0, 0, 3.5))
    m.cube("tail_fin", (-0.4, -4.5, 0), (0.8, 9.0, 4.0), "fin")
    return m


def _skin(ctx: Ctx):
    x, y, z = ctx.world
    yr, zr = y - BODY[1], z - BODY[2]
    if ctx.face == "front" and 1.3 < yr < 4.1 and abs(x) < 5.0 and zr < -3.9:
        return pick(ctx.pal["mouth"], 0.25)                                          # in den Schlund
    v = 0.4 + (noise3(x * 0.35, y * 0.35, z * 0.35, 3) - 0.5) * 0.4 + (0.1 if ctx.face == "top" else 0.0)
    # samtige, fleckige Haut: hellere Wolken über dem dunklen Grund
    v += max(0.0, noise3(x * 0.18, y * 0.18, z * 0.18, 21) - 0.58) * 1.2
    col = pick(ctx.pal["skin"], v)
    if ctx.face == "bottom" or (ctx.face in ("west", "east") and yr > 2.5):
        col = mix(col, pick(ctx.pal["belly"], 0.4), 0.5)
    # Warzen und Hautlappen
    if hash01(ctx.u // 2, ctx.v // 2, 4) < 0.07:
        col = pick(ctx.pal["wart"], 0.3 + hash01(ctx.u, ctx.v, 5) * 0.6)
    # eine Reihe schwach leuchtender Punkte entlang der Flanken
    if ctx.face in ("west", "east") and 0.0 <= yr < 0.5 and (zr + 20.0) % 2.5 < 0.5 and -3.0 < zr < 6.0:
        col = pick(ctx.pal["photo"], 0.6)
        ctx.glow = mix((0, 0, 0, 255), col, 0.7)
    return col


def _belly(ctx: Ctx):
    x, y, z = ctx.world
    return pick(ctx.pal["belly"], 0.35 + (noise3(x * 0.4, y, z * 0.4, 22) - 0.5) * 0.3)


def _throat(ctx: Ctx):
    t = (ctx.t + 0.5) / max(ctx.h, 1)
    return pick(ctx.pal["mouth"], 0.15 + t * 0.25)


def _filament(ctx: Ctx):
    col = pick(ctx.pal["lure"], 0.7)
    ctx.glow = mix((0, 0, 0, 255), col, 0.85)
    return col


def _jawskin(ctx: Ctx):
    if ctx.face == "top":
        # das Innere des Mauls
        return pick(ctx.pal["mouth"], 0.3 + 0.4 * hash01(ctx.u, ctx.v, 6))
    return _skin(ctx)


def _tooth(ctx: Ctx):
    t = (ctx.t + 0.5) / max(ctx.h, 1)
    col = pick(ctx.pal["tooth"], 0.4 + t * 0.5)
    return col[:3] + (235,)


def _eye(ctx: Ctx):
    if ctx.s == 0 and ctx.t == 0:
        return pick(ctx.pal["eye"], 1.0)
    return pick(ctx.pal["eye"], 0.4)


def _rod(ctx: Ctx):
    x, y, z = ctx.world
    return pick(ctx.pal["fin"], 0.4 + 0.3 * (int(y * 2) % 2))


def _lure(ctx: Ctx):
    s = (ctx.s + 0.5) / max(ctx.w, 1)
    t = (ctx.t + 0.5) / max(ctx.h, 1)
    v = 0.95 - math.hypot(s - 0.4, t - 0.4) * 0.9
    col = pick(ctx.pal["lure"], v)
    ctx.glow = col
    return col


def _fin(ctx: Ctx):
    x, y, z = ctx.world
    col = pick(ctx.pal["fin"], 0.3 + (noise3(x, y, z, 9) - 0.5) * 0.3)
    # Flossenstrahlen
    if ctx.face in ("west", "east") and ctx.s % 3 == 0:
        col = pick(ctx.pal["wart"], 0.2)
    return col[:3] + (230,)


def materials(variant: str) -> dict:
    return {"skin": _skin, "belly": _belly, "throat": _throat, "jawskin": _jawskin, "tooth": _tooth, "eye": _eye,
            "rod": _rod, "lure": _lure, "filament": _filament, "fin": _fin}
