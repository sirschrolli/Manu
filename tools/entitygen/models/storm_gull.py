"""
Sturmmöwe der Sturmküste (0.85): weißer Kopf, Leib und Schwanz, silbergraue Oberseite der Flügel (bei der Mantelmöwe
schiefergrau), schwarze Flügelspitzen mit weißen Spiegelflecken, ein gelber Schnabel mit rotem Fleck am Unterschnabel,
blassgelbe Augen mit rotem Lidring und rosa Beine mit Schwimmhäuten.

Knochen (für StormGullModel in Java): body (mit head → jaw; wing_l → wing_l_tip, wing_r → wing_r_tip; tail), die Beine
left_leg, right_leg. Ruhehaltung: Flügel ausgebreitet (Gleitflug); zum Stehen legt sie das Modell an.
"""
from __future__ import annotations

from ..geometry import Model
from ..paint import Ctx, hash01, hexc, pick, rel_y

_COMMON = {
    "white": [hexc(c) for c in ("#b8bcc2", "#cfd3d8", "#e2e5e8", "#eff1f3", "#f8f9fa", "#ffffff")],
    "black": [hexc(c) for c in ("#0e0e10", "#1a1a1e", "#28282e", "#3a3a42", "#4e4e56")],
    "beak": [hexc(c) for c in ("#a8761a", "#d49e26", "#eebd36", "#fcd862", "#fff0a4")],
    "red": [hexc(c) for c in ("#8a2018", "#b82e22", "#dc4230", "#f06450")],
    "leg": [hexc(c) for c in ("#9a5a4c", "#c27a6a", "#dc9a88", "#eebaaa", "#f8d4c8")],
    "eye": [hexc(c) for c in ("#0c0c0c", "#c8c070", "#ece49a", "#fff8c8")],
}

PALETTES = {
    "storm_gull": {**_COMMON,
                   "mantle": [hexc(c) for c in ("#56606a", "#6e7882", "#88929c", "#a0aab3", "#b6bfc7", "#cad2d9")]},
    "storm_gull_dark": {**_COMMON,
                        "mantle": [hexc(c) for c in ("#1c2026", "#282c33", "#353a42", "#444a52", "#555b63", "#686e76")]},
}

_LIT = {"top": 0.08, "front": 0.02, "west": 0.0, "east": -0.02, "back": -0.04, "bottom": -0.12}


def build() -> Model:
    m = Model("storm_gull", 64, 48)
    m.tex_res = 2
    # Leib: vorn die runde Brust, hinten schmaler zum Schwanz; der Kopf sitzt vorn oben
    m.bone("body", pivot=(0, 18.2, 0))
    m.cube("body", (-2.1, -2.0, -3.9), (4.2, 4.0, 7.6), "body")
    m.cube("body", (-1.8, -1.3, -4.7), (3.6, 3.2, 0.9), "body")                # die Brust
    m.cube("body", (-1.6, -1.6, 3.6), (3.2, 2.7, 1.8), "body")                 # zum Schwanz hin schmaler
    m.bone("head", "body", pivot=(0, -1.3, -3.6))
    m.cube("head", (-1.45, -3.3, -2.9), (2.9, 3.1, 3.2), "head")
    m.cube("head", (-0.45, -2.05, -5.4), (0.9, 0.75, 2.6), "beak")
    m.cube("head", (-0.45, -1.4, -5.45), (0.9, 0.4, 0.5), "beak_tip")              # der Haken
    m.cube("head", (1.4, -2.6, -2.0), (0.12, 0.7, 0.7), "eye")
    m.cube("head", (-1.52, -2.6, -2.0), (0.12, 0.7, 0.7), "eye")
    m.bone("jaw", "head", pivot=(0, -1.3, -2.9))
    m.cube("jaw", (-0.4, 0, -2.3), (0.8, 0.5, 2.3), "beak_lower")
    # Flügel: innen und Spitze; ausgebreitet in der Ruhehaltung
    m.bone("wing_l", "body", pivot=(2.0, -1.6, -2.2))
    m.cube("wing_l", (0, -0.3, -0.6), (5.0, 0.6, 4.4), "wing")
    m.bone("wing_l_tip", "wing_l", pivot=(5.0, 0, 0))
    m.cube("wing_l_tip", (0, -0.25, -0.4), (6.4, 0.5, 3.6), "wing_tip")
    m.bone("wing_r", "body", pivot=(-2.0, -1.6, -2.2))
    m.cube("wing_r", (-5.0, -0.3, -0.6), (5.0, 0.6, 4.4), "wing")
    m.bone("wing_r_tip", "wing_r", pivot=(-5.0, 0, 0))
    m.cube("wing_r_tip", (-6.4, -0.25, -0.4), (6.4, 0.5, 3.6), "wing_tip")
    # kurzer, gerader Schwanz
    m.bone("tail", "body", pivot=(0, -1.1, 5.0))
    m.cube("tail", (-1.6, -0.3, -0.4), (3.2, 0.6, 3.0), "tail")
    # Beine mit Schwimmfüßen
    for side, sx in (("left", 1), ("right", -1)):
        m.bone(f"{side}_leg", pivot=(0.9 * sx, 20.0, 0.6))
        m.cube(f"{side}_leg", (-0.35, 0, -0.35), (0.7, 3.8, 0.7), "leg")
        m.cube(f"{side}_leg", (-0.95, 3.8, -1.7), (1.9, 0.2, 2.1), "foot")
    return m


# =============================================================================================
# Bemalung
# =============================================================================================

def _feather(ctx: Ctx, key: str, v: float) -> tuple:
    return pick(ctx.pal[key], v + _LIT[ctx.face] + (hash01(ctx.u, ctx.v // 2, 601) - 0.5) * 0.06)


def _body(ctx: Ctx):
    """Weiß, der Rücken silbergrau (dort liegen die Flügel auf)."""
    if ctx.face == "top":
        return _feather(ctx, "mantle", 0.6)
    if ctx.face in ("west", "east") and rel_y(ctx) < 0.18:
        return _feather(ctx, "mantle", 0.62)
    return _feather(ctx, "white", 0.72)


def _head(ctx: Ctx):
    return _feather(ctx, "white", 0.8)


def _beak(ctx: Ctx):
    rs = (ctx.s + 0.5) / max(ctx.w, 1)
    if ctx.face == "front":
        return pick(ctx.pal["beak"], 0.75)
    v = 0.62 + _LIT[ctx.face]
    if ctx.face in ("west", "east") and rs < 0.25:
        v += 0.12                                                            # die Spitze heller
    return pick(ctx.pal["beak"], v)


def _beak_tip(ctx: Ctx):
    return pick(ctx.pal["beak"], 0.82 + _LIT[ctx.face])


def _beak_lower(ctx: Ctx):
    """Unterschnabel mit dem roten Fleck kurz vor der Spitze."""
    lz = ctx.local[2]
    if -1.5 < lz < -1.0 and ctx.face in ("west", "east", "bottom") and (ctx.face == "bottom" or rel_y(ctx) > 0.4):
        return pick(ctx.pal["red"], 0.6)
    return pick(ctx.pal["beak"], 0.55 + _LIT[ctx.face])


def _eye(ctx: Ctx):
    rs = (ctx.s + 0.5) / max(ctx.w, 1)
    rt = (ctx.t + 0.5) / max(ctx.h, 1)
    if ctx.face not in ("west", "east"):
        return pick(ctx.pal["white"], 0.7)
    if abs(rs - 0.5) < 0.2 and abs(rt - 0.5) < 0.2:
        return pick(ctx.pal["eye"], 0.0)                                     # Pupille
    if abs(rs - 0.5) > 0.38 or abs(rt - 0.5) > 0.38:
        return pick(ctx.pal["red"], 0.55)                                    # der rote Lidring
    return pick(ctx.pal["eye"], 0.7)


def _wing(ctx: Ctx):
    """Innenflügel: oben grau mit weißer Hinterkante, unten weiß."""
    lz = ctx.local[2]
    if ctx.face == "bottom":
        return _feather(ctx, "white", 0.62)
    if ctx.face == "top":
        if lz > 3.0:
            return _feather(ctx, "white", 0.76)                             # die weiße Hinterkante
        return _feather(ctx, "mantle", 0.6 + (0.04 if int(ctx.local[0] * 2) % 3 == 0 else 0.0))
    return _feather(ctx, "mantle", 0.45)


def _wing_tip(ctx: Ctx):
    """Handschwingen: grau, außen schwarz mit zwei weißen Spiegelflecken; unten hell, die Spitze schwarz."""
    lx = abs(ctx.local[0])
    lz = ctx.local[2]
    black = lx > 3.4 + (lz + 0.4) * 0.2
    if ctx.face in ("top", "bottom", "west", "east", "front", "back"):
        if black:
            # Spiegel: weiße Flecken nah an der Spitze
            if (5.0 < lx < 5.8 and 0.6 < lz < 1.6) or (4.0 < lx < 4.6 and 1.6 < lz < 2.4):
                return _feather(ctx, "white", 0.78)
            return _feather(ctx, "black", 0.4)
        if ctx.face == "bottom":
            return _feather(ctx, "white", 0.6)
        if ctx.face == "top" and lz > 2.6:
            return _feather(ctx, "white", 0.72)
    return _feather(ctx, "mantle", 0.62)


def _tail(ctx: Ctx):
    return _feather(ctx, "white", 0.74)


def _leg(ctx: Ctx):
    return pick(ctx.pal["leg"], 0.55 + _LIT[ctx.face])


def _foot(ctx: Ctx):
    rs = (ctx.s + 0.5) / max(ctx.w, 1)
    if ctx.face == "top" and any(abs(rs - f) < 0.08 for f in (0.2, 0.5, 0.8)):
        return pick(ctx.pal["leg"], 0.3)                                     # die Zehen über der Schwimmhaut
    return pick(ctx.pal["leg"], 0.62 + _LIT[ctx.face])


def materials(variant: str) -> dict:
    return {"body": _body, "head": _head, "beak": _beak, "beak_tip": _beak_tip, "beak_lower": _beak_lower, "eye": _eye, "wing": _wing,
            "wing_tip": _wing_tip, "tail": _tail, "leg": _leg, "foot": _foot}
