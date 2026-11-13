"""
Höhlentroll: riesig, gebeugt, mit steinerner, moosiger Haut, langen Armen, Lendenschurz und einer
Holzkeule. Die Augen glühen schwach in der Dunkelheit.

Knochen-Namen werden in CaveTrollModel (Java) für die Animationen verwendet.
"""
from __future__ import annotations

import math

from ..geometry import Model
from ..paint import Ctx, Voronoi3D, flat_painter, hash01, noise3, pick, ramp, rel_y

PALETTES = {
    "cave_troll": {
        "skin": ramp("#262a24", "#8c9484"), "moss": ramp("#1c3014", "#6a8a3a"), "cloth": ramp("#2a1c10", "#7a5a38"),
        "wood": ramp("#2a1a0c", "#8a6236"), "nail": ramp("#2a2420", "#a09080"), "eye": ramp("#8a5a00", "#ffd84a"),
        "hair": ramp("#141210", "#4a4038"), "tusk": ramp("#8a8070", "#f4ecd8"), "iron": ramp("#2a2c30", "#9aa0a8"),
        "back": ramp("#262a24", "#8c9484"),
    },
}


def build() -> Model:
    m = Model("cave_troll", 128, 128)
    # Rumpf: gebeugt, mit Bauch
    m.bone("body", pivot=(0, 10, 0), rotation=(14, 0, 0))
    m.cube("body", (-8, -18, -5), (16, 18, 10), "torso")
    m.cube("body", (-6.5, -9, -7), (13, 8, 3), "torso")             # Bauch
    m.cube("body", (-8.5, -3, -5.5), (17, 4, 11), "cloth")           # Gürtel mit Lendenschurz
    m.cube("body", (-4, 1, -6), (8, 5, 1), "cloth")                  # Schurz vorne

    m.bone("head", "body", pivot=(0, -17, -3), rotation=(-10, 0, 0))
    m.cube("head", (-4.5, -8, -6.5), (9, 9, 9), "head")
    m.cube("head", (-1.5, -4.5, -9), (3, 4, 3), "head")             # Knollennase
    m.cube("head", (-4.5, -8.5, -7), (9, 2, 2), "head")              # Stirnwulst
    m.cube("head", (-3.5, 0, -7), (7, 2, 4), "head")                 # Unterkiefer
    m.cube("head", (-3, -1.5, -7.3), (1, 2, 1), "tusk")
    m.cube("head", (2, -1.5, -7.3), (1, 2, 1), "tusk")
    m.cube("head", (-2.5, -9.5, -3), (5, 2, 5), "hair")              # Haarbüschel
    m.cube("head", (-3.6, -5.5, -6.6), (2, 1, 0.5), "eye")
    m.cube("head", (1.6, -5.5, -6.6), (2, 1, 0.5), "eye")
    for side in (1, -1):
        name = "left_ear" if side > 0 else "right_ear"
        m.bone(name, "head", pivot=(4.5 * side, -5, -2), rotation=(0, -30 * side, 0))
        m.cube(name, (0 if side > 0 else -4, -1.5, -0.5), (4, 3, 1), "head")

    # Arme: lang, mit großen Händen; die rechte hält die Keule
    for side in (1, -1):
        name = "left_arm" if side > 0 else "right_arm"
        m.bone(name, "body", pivot=(10 * side, -15, -1), rotation=(-14, 0, 0))
        m.cube(name, (-3 if side > 0 else -2, -2, -3), (5, 18, 6), "arm")
        m.cube(name, (-3.5 if side > 0 else -2.5, 16, -3.5), (6, 5, 7), "hand")
    m.bone("club", "right_arm", pivot=(0.5, 19, 0), rotation=(75, 0, 0))
    m.cube("club", (-1.5, -2, -1.5), (3, 18, 3), "wood")
    m.cube("club", (-2.5, 13, -2.5), (5, 8, 5), "wood")
    m.cube("club", (-3, 15, -3), (6, 1, 6), "iron")                  # Eisenband
    m.cube("club", (-3, 18, -3), (6, 1, 6), "iron")

    for side in (1, -1):
        name = "left_leg" if side > 0 else "right_leg"
        m.bone(name, pivot=(4.5 * side, 10, 0))
        m.cube(name, (-3.5, 0, -3.5), (7, 14, 7), "leg")
    return m


_CRACKS = Voronoi3D(6.5, 69)


def _hide(ctx: Ctx, moss: bool, seed: int) -> tuple:
    """
    Steinige Trollhaut wie bei Vanilla-Wesen: ruhige Grundfläche mit großen, weichen Helligkeitsflächen,
    dunkleren Kanten der Quader (wirkt gerundet), wenigen Rissen und kleinen Warzen. Moos nur oben auf
    Schultern, Rücken und Kopf als zusammenhängende Flecken mit dunklerem Rand.
    """
    x, y, z = ctx.world
    ry = rel_y(ctx) if ctx.face not in ("top", "bottom") else (0.0 if ctx.face == "top" else 1.0)
    v = 0.54 + (0.5 - ry) * 0.16
    v += (noise3(x * 0.16, y * 0.16, z * 0.16, seed) - 0.5) * 0.18
    # Kanten der Fläche dunkler (Umgebungsverdeckung)
    if ctx.face not in ("top",):
        if ctx.s == 0 or ctx.s == ctx.w - 1:
            v -= 0.07
        if ctx.t == ctx.h - 1:
            v -= 0.08
    val, edge = _CRACKS(x, y, z)
    if edge < 0.09:
        v -= 0.13
    # vereinzelte Warzen: heller Punkt mit Schatten darunter
    if hash01(ctx.u, ctx.v, seed) > 0.975:
        v += 0.14
    elif hash01(ctx.u, ctx.v - 1, seed) > 0.975:
        v -= 0.1
    col = pick(ctx.pal["skin"], v)
    if moss and ctx.face in ("top", "back", "west", "east") and ry < 0.45:
        m = noise3(x * 0.22, y * 0.22, z * 0.22, seed + 7) + (0.45 - ry) * 0.35
        if m > 0.7:
            return pick(ctx.pal["moss"], 0.5 + (noise3(x * 0.6, y * 0.6, z * 0.6, seed + 8) - 0.5) * 0.3)
        if m > 0.66:
            return pick(ctx.pal["moss"], 0.2)
    return col


def _skin(ctx: Ctx):
    return _hide(ctx, True, 61)


def _head(ctx: Ctx):
    # Gesicht: dunkle Mundlinie über dem Unterkiefer, Nase etwas heller
    if ctx.cube.size == (3, 4, 3):
        return pick(ctx.pal["skin"], 0.62 - (0.1 if ctx.t == ctx.h - 1 else 0.0))
    if ctx.cube.size == (7, 2, 4) and ctx.face == "front" and ctx.t == 0:
        return pick(ctx.pal["skin"], 0.18)
    if ctx.cube.size == (9, 2, 2) and ctx.face in ("bottom",):
        return pick(ctx.pal["skin"], 0.22)
    return _hide(ctx, ctx.face == "top", 62)


def _arm(ctx: Ctx):
    return _hide(ctx, True, 63)


def _hand(ctx: Ctx):
    if ctx.face == "bottom" or (ctx.face == "front" and rel_y(ctx) > 0.6):
        # Finger: dunkle Linien, Fingernägel unten
        if ctx.face == "front" and ctx.s % 2 == 1:
            return pick(ctx.pal["skin"], 0.25)
        if ctx.face == "front" and ctx.t == ctx.h - 1:
            return pick(ctx.pal["nail"], 0.6)
    return _hide(ctx, False, 64)


def _leg(ctx: Ctx):
    if rel_y(ctx) > 0.86 or ctx.face == "bottom":
        # Zehen
        if ctx.face == "front" and ctx.s % 2 == 0:
            return pick(ctx.pal["skin"], 0.28)
        return pick(ctx.pal["skin"], 0.4)
    return _hide(ctx, False, 65)


def _cloth(ctx: Ctx):
    x, y, z = ctx.world
    n = noise3(x * 0.8, y * 0.8, z * 0.8, 66)
    v = 0.45 + (n - 0.5) * 0.4
    if ctx.face in ("front", "west", "east", "back") and ctx.cube.size[1] >= 4 and ctx.t == 0:
        v -= 0.2                         # Gürtelkante
    if ctx.cube.size[1] == 5 and ctx.t == ctx.h - 1 and ctx.s % 2 == 0:
        return None                      # ausgefranster Saum
    return pick(ctx.pal["cloth"], v)


def _wood(ctx: Ctx):
    x, y, z = ctx.world
    grain = math.sin(y * 1.6 + noise3(x, y * 0.3, z, 67) * 3)
    v = 0.45 + grain * 0.12 + (hash01(ctx.u, ctx.v) - 0.5) * 0.08
    if ctx.cube.size[1] == 8 and hash01(int(x), int(y), int(z), 68) > 0.9:
        return pick(ctx.pal["iron"], 0.75)  # Nagelköpfe
    return pick(ctx.pal["wood"], v)


def _eye(ctx: Ctx):
    col = pick(ctx.pal["eye"], 0.7 if ctx.s == 0 else 0.55)
    if ctx.face == "front":
        ctx.glow = col
    return col


def materials(variant: str) -> dict:
    return {
        "torso": _skin,
        "head": _head,
        "arm": _arm,
        "hand": _hand,
        "leg": _leg,
        "cloth": _cloth,
        "wood": _wood,
        "iron": flat_painter("iron", 0.55, 0.12),
        "tusk": flat_painter("tusk", 0.8, 0.1),
        "hair": flat_painter("hair", 0.4, 0.2),
        "eye": _eye,
    }
