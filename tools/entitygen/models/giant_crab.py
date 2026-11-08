"""
Riesenkrabbe: eine Strandkrabbe so groß wie ein Hund – ein breiter, gewölbter Panzer mit gezacktem Vorderrand und
kleinen Dornen, Augen auf Stielen, zwei Scheren (die linke ist die große Knackschere) mit beweglichem Finger und
schwarzen Spitzen, und sechs gelenkige Laufbeine. Sie läuft seitwärts.

Panzer rotorange mit dunkleren Flecken und hellem Rand, Unterseite cremefarben, die Beine orange mit hellen Gelenken.
Knochen-Namen werden in GiantCrabModel (Java) für den Seitwärtsgang, das Scherenklappern und den Angriff verwendet.
"""
from __future__ import annotations

from ..geometry import Model
from ..paint import Ctx, hash01, hexc, mix, noise3, pick

PALETTES = {
    "giant_crab": {
        "shell": [hexc(c) for c in ("#5a1a10", "#7e2614", "#a0381c", "#c04e26", "#d86a34", "#ec8a4c")],
        "spot": [hexc(c) for c in ("#4a1408", "#62200e")],
        "belly": [hexc(c) for c in ("#c8a888", "#dcc0a0", "#ecd6ba", "#f6e8d4")],
        "leg": [hexc(c) for c in ("#8a2e16", "#b04420", "#cc5e2c", "#e07a3c")],
        "tip": [hexc(c) for c in ("#0e0a0a", "#221a18", "#3a2c28")],
        "eye": [hexc(c) for c in ("#060606", "#1a1a1a", "#c8c8c8")],
    },
}

BODY = (0.0, 18.5, 0.0)


def build() -> Model:
    m = Model("giant_crab", 128, 96)
    m.tex_res = 2
    m.bone("body", pivot=BODY)
    # der Panzer ist vorn am breitesten und läuft nach hinten schmaler zu
    m.cube("body", (-7.0, -2.5, -5.0), (14, 4, 6.5), "shell")
    m.cube("body", (-5.8, -2.3, 1.5), (11.6, 3.7, 3.0), "shell")
    m.cube("body", (-4.2, -2.0, 4.5), (8.4, 3.2, 1.2), "shell")
    m.cube("body", (-6.2, -3.1, -4.4), (12.4, 0.6, 8.4), "shell")                   # Wölbung
    m.cube("body", (-4.8, -3.7, -3.4), (9.6, 0.6, 6.4), "shell")
    m.cube("body", (-2.8, -4.1, -2.4), (5.6, 0.4, 4.0), "shell")
    m.cube("body", (-6.0, 1.5, -4.0), (12, 1.2, 8), "belly")                        # Unterseite
    m.cube("body", (-2.6, -2.2, -5.6), (5.2, 1.4, 0.6), "brow")                     # Stirn zwischen den Augen
    for k, x in enumerate((-5.5, -3.0, 3.0, 5.5)):
        m.cube("body", (x - 0.4, -2.2, -5.6), (0.8, 1.0, 0.8), "spike")             # Zacken am Vorderrand
    for side, sx in (("left", 1), ("right", -1)):
        m.cube("body", (6.6 * sx - 0.4, -2.0, -2.0), (0.8, 1.0, 0.8), "spike")      # Seitendorn
        eye = f"{side}_eye"
        m.bone(eye, "body", pivot=(1.6 * sx, -2.6, -4.6))
        m.cube(eye, (-0.3, -2.2, -0.3), (0.6, 2.2, 0.6), "stalk")
        m.cube(eye, (-0.55, -3.2, -0.55), (1.1, 1.1, 1.1), "eye")

    # Scheren: Oberarm, Unterarm, Schere mit beweglichem Finger (links die große Knackschere)
    for side, sx, big in (("left", 1, 1.25), ("right", -1, 1.0)):
        arm = f"{side}_arm"
        m.bone(arm, "body", pivot=(5.5 * sx, 0.5, -4.0), rotation=(0, -35 * sx, 0))
        m.cube(arm, (0 if sx > 0 else -4.5, -1.0, -1.0), (4.5, 2.0, 2.0), "leg")
        fore = f"{side}_forearm"
        m.bone(fore, arm, pivot=(4.5 * sx, 0, 0), rotation=(0, 70 * sx, 0))
        m.cube(fore, (-1.2, -1.2, -4.0), (2.4, 2.4, 4.0), "leg")
        claw = f"{side}_claw"
        m.bone(claw, fore, pivot=(0, 0, -4.0))
        w, h, d = 3.4 * big, 3.0 * big, 4.2 * big
        m.cube(claw, (-w / 2, -h / 2, -d), (w, h, d), "claw")
        m.cube(claw, (-w / 2 + 0.3, -h / 2 + 0.2, -d - 2.4 * big), (w * 0.45, h * 0.55, 2.4 * big), "clawtip")   # fester Finger
        finger = f"{side}_claw_finger"
        m.bone(finger, claw, pivot=(w / 2 - 0.4, h / 2 - 0.4, -d), rotation=(0, -12 * sx, 0))
        m.cube(finger, (-w * 0.45, -h * 0.35, -2.6 * big), (w * 0.45, h * 0.5, 2.6 * big), "clawtip")

    # sechs Laufbeine (je drei Seiten), jeweils Oberglied und Unterglied mit Spitze
    for side, sx in (("left", 1), ("right", -1)):
        for k in range(3):
            leg = f"{side}_leg_{k}"
            m.bone(leg, "body", pivot=(6.5 * sx, 0.5, -1.8 + k * 2.6), rotation=(0, (k - 1) * 14 * sx, -30 * sx))
            m.cube(leg, (0 if sx > 0 else -5, -0.6, -0.6), (5, 1.2, 1.2), "leg")
            low = leg + "_low"
            m.bone(low, leg, pivot=(5.0 * sx, 0, 0), rotation=(0, 0, 75 * sx))
            m.cube(low, (0 if sx > 0 else -5, -0.5, -0.5), (5, 1.0, 1.0), "legtip")
    return m


def _pos(ctx: Ctx):
    x, y, z = ctx.world
    return x - BODY[0], y - BODY[1], z - BODY[2]


def _shell(ctx: Ctx):
    x, y, z = _pos(ctx)
    v = 0.55 + (noise3(x * 0.3, y * 0.3, z * 0.3, 3) - 0.5) * 0.3 + (0.12 if ctx.face == "top" else -0.08)
    col = pick(ctx.pal["shell"], v)
    if ctx.face == "top":
        # dunkle Flecken und ein heller Rand
        if noise3(x * 0.5, 0.0, z * 0.5, 4) > 0.68:
            col = pick(ctx.pal["spot"], 0.5)
        edge = min(ctx.s, ctx.w - 1 - ctx.s, ctx.t, ctx.h - 1 - ctx.t)
        if edge == 0:
            col = mix(col, pick(ctx.pal["shell"], 0.95), 0.6)
        # die zwei typischen Furchen
        if abs(abs(x) - 2.0) < 0.3 and z < 1.0:
            col = pick(ctx.pal["shell"], v - 0.35)
    if ctx.face == "bottom":
        return pick(ctx.pal["belly"], 0.5)
    return col


def _belly(ctx: Ctx):
    x, y, z = _pos(ctx)
    col = pick(ctx.pal["belly"], 0.5 + (noise3(x * 0.4, 1.0, z * 0.4, 5) - 0.5) * 0.25)
    if ctx.face == "bottom" and abs(x) < 2.5 and abs(z) < 3:
        col = pick(ctx.pal["belly"], 0.25)                                         # Hinterleib
    return col


def _leg(ctx: Ctx):
    x, y, z = ctx.world
    v = 0.5 + (noise3(x * 0.5, y * 0.5, z * 0.5, 6) - 0.5) * 0.3 + (0.15 if ctx.face == "top" else 0.0)
    col = pick(ctx.pal["leg"], v)
    if ctx.face == "bottom":
        col = mix(col, pick(ctx.pal["belly"], 0.5), 0.5)
    # helle Gelenke an den Enden
    if ctx.face not in ("west", "east") and (ctx.s == 0 or ctx.s == ctx.w - 1):
        col = mix(col, pick(ctx.pal["belly"], 0.6), 0.5)
    return col


def _legtip(ctx: Ctx):
    col = _leg(ctx)
    s = (ctx.s + 0.5) / max(ctx.w, 1)
    return mix(col, pick(ctx.pal["tip"], 0.5), max(0.0, s - 0.7) * 2.5) if ctx.face in ("top", "bottom", "front", "back") else col


def _claw(ctx: Ctx):
    x, y, z = ctx.world
    v = 0.55 + (noise3(x * 0.4, y * 0.4, z * 0.4, 7) - 0.5) * 0.3 + (0.15 if ctx.face == "top" else 0.0)
    col = pick(ctx.pal["shell"], v)
    if hash01(ctx.u, ctx.v, 8) < 0.06:
        col = pick(ctx.pal["shell"], v + 0.25)                                       # Höcker
    if ctx.face == "bottom":
        col = mix(col, pick(ctx.pal["belly"], 0.5), 0.6)
    return col


def _clawtip(ctx: Ctx):
    t = (ctx.t + 0.5) / max(ctx.h, 1)
    col = _claw(ctx)
    if ctx.face == "front":
        return pick(ctx.pal["tip"], 0.4)
    s = (ctx.s + 0.5) / max(ctx.w, 1)
    return mix(col, pick(ctx.pal["tip"], 0.4), min(1.0, max(0.0, 1.0 - s * 1.4)))


def _brow(ctx: Ctx):
    # die Stirn: gezackt wie eine kleine Säge
    col = _shell(ctx)
    if ctx.face == "front" and ctx.t == 0 and ctx.s % 2 == 1:
        return (0, 0, 0, 0)
    return col


def _spike(ctx: Ctx):
    return pick(ctx.pal["shell"], 0.9 if ctx.face == "top" else 0.6)


def _stalk(ctx: Ctx):
    return pick(ctx.pal["leg"], 0.6)


def _eye(ctx: Ctx):
    # glänzend schwarz mit einem Lichtpunkt; zur Basis hin rötlich wie der Stiel
    if ctx.face in ("front", "west", "east") and ctx.s == 0 and ctx.t == 0:
        return pick(ctx.pal["eye"], 1.0)
    if ctx.face not in ("top", "bottom") and ctx.t == ctx.h - 1:
        return pick(ctx.pal["leg"], 0.3)
    return pick(ctx.pal["eye"], 0.15 if ctx.face != "top" else 0.4)


def materials(variant: str) -> dict:
    return {"shell": _shell, "brow": _brow, "belly": _belly, "leg": _leg, "legtip": _legtip, "claw": _claw, "clawtip": _clawtip,
            "spike": _spike, "stalk": _stalk, "eye": _eye}
