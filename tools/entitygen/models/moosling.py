"""
Mooslinge – der Arachnophobie-Modus: Statt Spinnen kugeln runde, freundliche Moosbälle mit großen Augen durch die Welt.
Sie haben das Skelett der Vanilla-Spinne (Kopf, Taille, Hinterleib und acht Beine mit den Vanilla-Namen), damit
Minecrafts Spinnenmodell sie bewegt – aber keine Beine: Der ganze Moosball ist der "Kopf" und dreht sich, wohin der
Moosling schaut; darunter tragen ihn vier Stummelfüße, oben wächst ein Blattschopf.

  * moosling: sattgrünes Moos mit hellen Polstern und winzigen Blüten, braune Füße, große braune Augen
  * moosling_hoehle: blaugrauer Stein mit türkis leuchtenden Moosflecken und glimmenden Augen
"""
from __future__ import annotations

import math

from ..geometry import Model
from ..paint import Ctx, hash01, mix, noise3, pick, ramp

PALETTES = {
    "moosling": {"moss": ramp("#1c3812", "#a6d45e", steps=7), "tuft": ramp("#3a6a1e", "#d4f08a"), "foot": ramp("#2e1e10", "#8a6440"),
                 "leaf": ramp("#245414", "#9ae25a"), "white": ramp("#d6dcc8", "#ffffff"), "iris": ramp("#2a1a0c", "#8a5a2a"),
                 "pupil": ramp("#060504", "#1a120a"), "mouth": ramp("#1e1008", "#5a3018"), "cheek": ramp("#b8604a", "#f4a08a"),
                 "flower": ramp("#e8e0a0", "#fff8e0"), "glow": ramp("#000000", "#000000")},
    "moosling_hoehle": {"moss": ramp("#161e24", "#7c8c98", steps=7), "tuft": ramp("#24343e", "#9aaab4"), "foot": ramp("#14181c", "#4e5a64"),
                        "leaf": ramp("#0e4a4a", "#5ae0c8"), "white": ramp("#a8f0e4", "#e8fffa"), "iris": ramp("#0a5a5a", "#5affe0"),
                        "pupil": ramp("#02100e", "#0a2a26"), "mouth": ramp("#0a1216", "#28343c"), "cheek": ramp("#2a6a6a", "#5ab4aa"),
                        "flower": ramp("#3affd8", "#c8fff4"), "glow": ramp("#0a6a5a", "#8affe8")},
}

_LEGS = {
    "right_hind_leg": ((-4, 15, 2), (0, 45, -45)),
    "left_hind_leg": ((4, 15, 2), (0, -45, 45)),
    "right_middle_hind_leg": ((-4, 15, 1), (0, 22.5, -33.3)),
    "left_middle_hind_leg": ((4, 15, 1), (0, -22.5, 33.3)),
    "right_middle_front_leg": ((-4, 15, 0), (0, -22.5, -33.3)),
    "left_middle_front_leg": ((4, 15, 0), (0, 22.5, 33.3)),
    "right_front_leg": ((-4, 15, -1), (0, -45, -45)),
    "left_front_leg": ((4, 15, -1), (0, 45, 45)),
}


def build() -> Model:
    m = Model("moosling", 128, 64)
    # der Moosball: Kern und abgerundete Kanten (Kappen oben/unten, Wülste an den Seiten), Mitte bei y=16
    m.bone("head", pivot=(0, 16, 0))
    m.cube("head", (-7, -6, -7), (14, 12, 14), "ball")
    m.cube("head", (-6, -7, -6), (12, 1, 12), "cap")
    m.cube("head", (-6, 6, -6), (12, 1, 12), "underside")
    m.cube("head", (-8, -5, -6), (1, 10, 12), "bulge")
    m.cube("head", (7, -5, -6), (1, 10, 12), "bulge")
    m.cube("head", (-6, -5, 7), (12, 10, 1), "bulge")
    # Gesicht etwas vorgewölbt (darauf Augen und Mund)
    m.cube("head", (-6, -5, -8), (12, 10, 1), "face")
    # Blattschopf: ein Stiel und zwei Blätter
    m.bone("sprout", "head", pivot=(0, -7, 0))
    m.cube("sprout", (-0.5, -3, -0.5), (1, 3, 1), "stem")
    m.bone("leaf_left", "sprout", pivot=(0, -3, 0), rotation=(0, 0, 32))
    m.cube("leaf_left", (0, -1, -1.5), (5, 1, 3), "leaf")
    m.bone("leaf_right", "sprout", pivot=(0, -3, 0), rotation=(0, 0, -32))
    m.cube("leaf_right", (-5, -1, -1.5), (5, 1, 3), "leaf")
    # Taille und Hinterleib der Spinne: hier die vier Stummelfüße (bewegen sich nicht)
    m.bone("body0", pivot=(0, 15, 0))
    m.cube("body0", (-5.5, 8, -5), (4, 1, 4), "foot")
    m.cube("body0", (1.5, 8, -5), (4, 1, 4), "foot")
    m.bone("body1", pivot=(0, 15, 0))
    m.cube("body1", (-5.5, 8, 1), (4, 1, 4), "foot")
    m.cube("body1", (1.5, 8, 1), (4, 1, 4), "foot")
    # die acht Beinknochen bleiben leer (Minecrafts Spinnenmodell braucht sie)
    for name, (pivot, rot) in _LEGS.items():
        m.bone(name, pivot=pivot, rotation=rot)
    return m


# =============================================================================================
# Malen
# =============================================================================================

def _variant(ctx: Ctx) -> str:
    return ctx.params.get("variant")


def _cave(ctx: Ctx) -> bool:
    return _variant(ctx) == "moosling_hoehle"


def _moss(ctx: Ctx, base: float = 0.5):
    """Moospolster: weich gefleckt, helle Spitzen, dazwischen winzige Blüten (in der Höhle leuchtende Flecken)."""
    x, y, z = ctx.world
    n = noise3(x * 0.45, y * 0.45, z * 0.45, 611)
    fine = noise3(x * 1.3, y * 1.3, z * 1.3, 612)
    v = base + (n - 0.5) * 0.34 + (fine - 0.5) * 0.14 + (hash01(ctx.u, ctx.v, 613) - 0.5) * 0.06
    if ctx.face == "top":
        v += 0.08
    elif ctx.face == "bottom":
        v -= 0.18
    col = pick(ctx.pal["moss"], v)
    h = hash01(ctx.u, ctx.v, 614)
    if fine > 0.62 and h > 0.55:
        col = mix(col, pick(ctx.pal["tuft"], 0.4 + fine * 0.5), 0.55)                     # helle Moosspitzen
    if ctx.face != "bottom" and h > 0.985:
        col = pick(ctx.pal["flower"], 0.4 + hash01(ctx.v, ctx.u, 615) * 0.6)               # winzige Blüte
        if _cave(ctx):
            ctx.glow = col
    if _cave(ctx) and n > 0.66 and fine > 0.5 and ctx.face != "bottom":
        glow = pick(ctx.pal["glow"], 0.45 + (n - 0.66) * 1.5)                               # leuchtende Moosflecken
        ctx.glow = glow
        return mix(col, glow, 0.8)
    return col


def _ball(ctx: Ctx):
    return _moss(ctx, 0.5)


def _cap(ctx: Ctx):
    return _moss(ctx, 0.58)


def _underside(ctx: Ctx):
    return _moss(ctx, 0.3)


def _bulge(ctx: Ctx):
    return _moss(ctx, 0.48)


# Gesicht (12 x 10): große Kulleraugen mit Glanzpunkt, rosige Wangen, kleines Lächeln
_FACE = ["............",
         ".WWWW..WWWW.",
         ".WPHW..WPHW.",
         ".WPPW..WPPW.",
         ".WWWW..WWWW.",
         "............",
         "cc........cc",
         "....m..m....",
         ".....mm.....",
         "............"]


def _face(ctx: Ctx):
    if ctx.face != "front":
        return _moss(ctx, 0.5)
    c = _FACE[ctx.t][ctx.s] if ctx.t < len(_FACE) and ctx.s < len(_FACE[0]) else "."
    if c == "W":
        col = pick(ctx.pal["white"], 0.75 + hash01(ctx.s, ctx.t, 621) * 0.2)
        if _cave(ctx):
            ctx.glow = mix(col, pick(ctx.pal["glow"], 0.3), 0.6)
        return col
    if c == "I":
        col = pick(ctx.pal["iris"], 0.45 + (ctx.t - 2) * 0.12)
        if _cave(ctx):
            ctx.glow = col
        return col
    if c == "P":
        return pick(ctx.pal["pupil"], 0.3)
    if c == "H":
        col = pick(ctx.pal["white"], 1.0)                                                    # Glanzpunkt
        if _cave(ctx):
            ctx.glow = col
        return col
    if c == "c":
        return mix(_moss(ctx, 0.55), pick(ctx.pal["cheek"], 0.6), 0.55)                     # Wangen
    if c == "m":
        return pick(ctx.pal["mouth"], 0.4)
    return _moss(ctx, 0.52)


def _foot(ctx: Ctx):
    v = 0.45 + (hash01(ctx.u, ctx.v, 631) - 0.5) * 0.12
    if ctx.face == "top":
        v -= 0.15
    return pick(ctx.pal["foot"], v)


def _stem(ctx: Ctx):
    return pick(ctx.pal["leaf"], 0.3 + hash01(ctx.u, ctx.v, 641) * 0.1)


def _leaf(ctx: Ctx):
    # Blatt mit heller Mittelrippe
    lx, ly, lz = ctx.local
    oz = ctx.cube.origin[2]
    d = ctx.cube.size[2]
    zr = abs((lz - oz) / d * 2 - 1)
    v = 0.55 + (hash01(ctx.u, ctx.v, 651) - 0.5) * 0.12 - zr * 0.12
    if ctx.face in ("top", "bottom") and zr < 0.3:
        v += 0.22
    col = pick(ctx.pal["leaf"], v)
    if _cave(ctx) and ctx.face == "top":
        ctx.glow = pick(ctx.pal["glow"], 0.5 + v * 0.3)
    return col


def materials(variant: str) -> dict:
    return {
        "ball": _ball,
        "cap": _cap,
        "underside": _underside,
        "bulge": _bulge,
        "face": _face,
        "foot": _foot,
        "stem": _stem,
        "leaf": _leaf,
    }
