"""
Glutsalamander der Aschefelder (0.86): ein flacher, langer Salamander mit breitem Kopf und kleinen, glänzenden
Knopfaugen, gespreizten Beinchen mit vier Zehen und einem langen Schwanz mit niedrigem Kamm. Die Haut ist schwarz wie
erkaltete Lava und rissig; in den Rissen, in zwei Reihen Flecken auf dem Rücken und im Schwanzkamm glüht es (beim
Glutsalamander orangerot, beim Goldsalamander gelb). Der Bauch glimmt dunkelrot.

Knochen (für EmberSalamanderModel in Java): body (mit head → jaw; body2 → tail1 → tail2 → tail3; die Beine
left_front_leg → left_front_foot, right_front_leg → right_front_foot an body, left_hind_leg → left_hind_foot,
right_hind_leg → right_hind_foot an body2). Ruhehaltung: gestreckt, die Beine gespreizt.
"""
from __future__ import annotations

import math

from ..geometry import Model
from ..paint import Ctx, hash01, hexc, mix, noise3, pick

_COMMON = {
    "skin": [hexc(c) for c in ("#0a0807", "#141010", "#1e1817", "#2a2220", "#382e2a", "#4a3e38")],
    "eye": [hexc(c) for c in ("#050404", "#16100e", "#2c2420", "#fff0d0")],
}

PALETTES = {
    "ember_salamander": {**_COMMON,
                         "glow": [hexc(c) for c in ("#7a1606", "#b42c0a", "#e04a10", "#ff7420", "#ffa040", "#ffd080")],
                         "belly": [hexc(c) for c in ("#2a0c08", "#44140c", "#601c10", "#7c2814", "#983a1c")]},
    "ember_salamander_gold": {**_COMMON,
                              "glow": [hexc(c) for c in ("#6a4006", "#a0660a", "#d49412", "#f8c030", "#ffdc68", "#fff0b0")],
                              "belly": [hexc(c) for c in ("#2a1c08", "#44300c", "#604410", "#7c5a14", "#98701c")]},
}

_LIT = {"top": 0.08, "front": 0.02, "west": 0.0, "east": -0.02, "back": -0.04, "bottom": -0.12}


def build() -> Model:
    m = Model("ember_salamander", 64, 32)
    m.tex_res = 2
    m.world_faces = True
    # vorderer Leib mit den Vorderbeinen, hinterer Leib mit den Hinterbeinen
    m.bone("body", pivot=(0, 21.6, -2.0))
    m.cube("body", (-1.7, -1.15, -2.6), (3.4, 2.3, 5.0), "skin")
    m.bone("head", "body", pivot=(0, -0.1, -2.6))
    m.cube("head", (-1.8, -1.0, -3.6), (3.6, 1.9, 3.8), "head")
    m.cube("head", (-1.45, -1.55, -2.7), (0.9, 0.7, 0.9), "eye")
    m.cube("head", (0.55, -1.55, -2.7), (0.9, 0.7, 0.9), "eye")
    m.bone("jaw", "head", pivot=(0, 0.45, -0.4))
    m.cube("jaw", (-1.7, 0, -3.1), (3.4, 0.5, 3.2), "jaw")
    m.bone("body2", "body", pivot=(0, 0.05, 2.3))
    m.cube("body2", (-1.6, -1.1, 0), (3.2, 2.1, 4.4), "skin")
    m.bone("tail1", "body2", pivot=(0, -0.15, 4.3))
    m.cube("tail1", (-1.2, -0.85, 0), (2.4, 1.7, 3.6), "tail")
    m.cube("tail1", (-0.15, -1.45, 0.2), (0.3, 0.6, 3.4), "crest")
    m.bone("tail2", "tail1", pivot=(0, 0.05, 3.5))
    m.cube("tail2", (-0.85, -0.65, 0), (1.7, 1.3, 3.6), "tail")
    m.cube("tail2", (-0.13, -1.15, 0), (0.26, 0.5, 3.6), "crest")
    m.bone("tail3", "tail2", pivot=(0, 0.05, 3.5))
    m.cube("tail3", (-0.55, -0.45, 0), (1.1, 0.9, 3.4), "tail")
    m.cube("tail3", (-0.1, -0.85, 0), (0.2, 0.4, 3.0), "crest")
    # Beine: Oberarm seitlich abgespreizt, Unterarm hinab, Fuß mit vier Zehen
    for side, sx in (("left", 1), ("right", -1)):
        lo = (lambda a, w: a if sx > 0 else -a - w)
        for part, bone, pz in (("front", "body", -1.0), ("hind", "body2", 2.6)):
            m.bone(f"{side}_{part}_leg", bone, pivot=(1.5 * sx, 0.3, pz))
            m.cube(f"{side}_{part}_leg", (lo(0, 1.9), -0.45, -0.5), (1.9, 0.9, 1.0), "leg")
            m.bone(f"{side}_{part}_foot", f"{side}_{part}_leg", pivot=(1.8 * sx, 0, 0))
            m.cube(f"{side}_{part}_foot", (-0.45, -0.3, -0.45), (0.9, 1.9, 0.9), "leg")
            m.cube(f"{side}_{part}_foot", (-0.95, 1.45, -1.25), (1.9, 0.25, 1.6), "toes")
    return m


# =============================================================================================
# Bemalung
# =============================================================================================

def _cracks(ctx: Ctx, scale: float = 0.8) -> float:
    """0..1: wie nah an einem glühenden Riss (Zellränder eines groben Musters)."""
    x, y, z = ctx.world
    n1 = noise3(x * scale, y * scale, z * scale, 951)
    n2 = noise3(x * scale * 2.1, y * scale * 2.1, z * scale * 2.1, 952)
    v = abs(n1 - 0.5) * 2.0 * 0.7 + abs(n2 - 0.5) * 2.0 * 0.3
    return 1.0 - min(1.0, v * 3.2)


def _spots(ctx: Ctx) -> bool:
    """Zwei Reihen runder Glutflecken links und rechts der Rückenmitte."""
    x, y, z = ctx.world
    if ctx.face != "top":
        return False
    for sx in (-1, 1):
        cx = sx * 0.95
        k = round((z - 0.4 * sx) / 1.8)
        cz = k * 1.8 + 0.4 * sx
        if math.hypot(x - cx, z - cz) < 0.55 + hash01(k, sx, 953) * 0.2:
            return True
    return False


def _glow(ctx: Ctx, v: float) -> tuple:
    col = pick(ctx.pal["glow"], v)
    ctx.glow = col
    return col


def _skin(ctx: Ctx):
    """Schwarze, rissige Haut; in den Rissen und Flecken glüht es; die Flanken unten dunkelrot."""
    if _spots(ctx):
        return _glow(ctx, 0.72)
    c = _cracks(ctx)
    if c > 0.93 and ctx.face in ("top", "west", "east"):
        return _glow(ctx, 0.4 + (c - 0.93) * 3.0)
    if ctx.face == "bottom":
        return pick(ctx.pal["belly"], 0.5)
    x, y, z = ctx.world
    v = 0.35 + (noise3(x * 0.9, y * 0.9, z * 0.9, 954) - 0.5) * 0.3 + _LIT[ctx.face]
    if c > 0.7:
        v -= 0.12
    return pick(ctx.pal["skin"], v)


def _head(ctx: Ctx):
    lx, ly, lz = ctx.local
    if ctx.lface == "front" and ly > -0.1 and ly < 0.2:
        return pick(ctx.pal["glow"], 0.25)                                   # das glimmende Maul
    if ctx.lface == "top" and abs(lx) < 0.4 and lz < -1.2:
        return _glow(ctx, 0.55)                                              # ein Glutstreif zwischen den Augen
    return _skin(ctx)


def _jaw(ctx: Ctx):
    if ctx.lface == "top":
        return pick(ctx.pal["glow"], 0.3)
    return pick(ctx.pal["belly"], 0.45 + _LIT[ctx.face])


def _eye(ctx: Ctx):
    rs = (ctx.s + 0.5) / max(ctx.w, 1)
    rt = (ctx.t + 0.5) / max(ctx.h, 1)
    if ctx.lface in ("top", "west", "east", "front") and rs > 0.55 and rt < 0.4:
        return pick(ctx.pal["eye"], 1.0)                                     # Glanzpunkt
    if ctx.lface == "bottom":
        return pick(ctx.pal["skin"], 0.3)
    return pick(ctx.pal["eye"], 0.2)


def _tail(ctx: Ctx):
    return _skin(ctx)


def _crest(ctx: Ctx):
    """Der niedrige Kamm auf dem Schwanz glüht von unten nach oben heller."""
    lz = ctx.local[2]
    return _glow(ctx, 0.55 + 0.25 * math.sin(lz * 1.4) ** 2)


def _leg(ctx: Ctx):
    x, y, z = ctx.world
    if ctx.face == "top" and hash01(int(x * 2), int(z * 2), 955) > 0.75:
        return _glow(ctx, 0.4)
    return pick(ctx.pal["skin"], 0.4 + _LIT[ctx.face])


def _toes(ctx: Ctx):
    rs = (ctx.s + 0.5) / max(ctx.w, 1)
    if ctx.lface in ("top", "front") and any(abs(rs - f) < 0.07 for f in (0.25, 0.5, 0.75)):
        return pick(ctx.pal["skin"], 0.1)                                    # zwischen den Zehen
    if ctx.lface == "front":
        return _glow(ctx, 0.35)                                              # glühende Zehenspitzen
    return pick(ctx.pal["skin"], 0.45 + _LIT[ctx.face])


def materials(variant: str) -> dict:
    return {"skin": _skin, "head": _head, "jaw": _jaw, "eye": _eye, "tail": _tail, "crest": _crest, "leg": _leg, "toes": _toes}
