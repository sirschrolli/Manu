"""
Mantarochen: ein riesiger, friedlicher Rochen der warmen Meere, der mit langsamen Flügelschlägen durch das Wasser
gleitet und manchmal aus dem Wasser springt.

  * ein rautenförmiger Leib mit weit ausladenden, spitz nach hinten gezogenen Brustflossen (in drei Gliedern, damit sie
    wellen) – deutlich schlanker und spitzer als der Sternenrochen der Leere
  * vorn das breite, offene Maul zwischen zwei langen, nach vorn gestreckten Kopfflossen; seitlich kleine dunkle Augen
  * oben fast schwarz mit den typischen weißen „Schulterflecken“ (Winkel hinter dem Kopf), feinen hellen Sprenkeln
  * unten weiß mit dunklen Tupfen (jeder Rochen ein eigenes Muster), dazu fünf Kiemenspalten je Seite
  * ein kurzer, dünner Peitschenschwanz mit einer kleinen Rückenflosse

Knochen-Namen werden in MantaRayModel (Java) für die Animationen verwendet.
"""
from __future__ import annotations

import math

from ..geometry import Model
from ..paint import Ctx, hash01, hexc, mix, noise3, pick

PALETTES = {
    "manta_ray": {
        "back": [hexc(c) for c in ("#0c0f16", "#141922", "#1c2230", "#252c3c", "#303849", "#3c4558")],
        "patch": [hexc(c) for c in ("#8a96a6", "#aeb8c4", "#ccd4dc", "#e4e9ee", "#f4f6f8")],
        "belly": [hexc(c) for c in ("#b8c0c8", "#ccd2d8", "#dce1e6", "#e8ecf0", "#f2f5f7", "#fafbfc")],
        "spot": [hexc(c) for c in ("#2a303c", "#3c4452", "#566070")],
        "mouth": [hexc(c) for c in ("#10131a", "#1e232c", "#2c323e")],
        "eye": [hexc(c) for c in ("#06070a", "#1a1e26", "#c8d0dc")],
    },
}

BODY = (0.0, 20.0, 0.0)


def build() -> Model:
    m = Model("manta_ray", 192, 128)
    m.tex_res = 2
    m.bone("body", pivot=BODY)
    m.cube("body", (-6.0, -2.0, -9.0), (12, 4, 18), "skin")
    m.cube("body", (-8.5, -1.5, -7.0), (17, 3, 13), "skin")
    m.cube("body", (-3.5, -2.6, -6.5), (7, 0.6, 10), "skin")                       # Rückenwölbung

    m.bone("head", "body", pivot=(0, 0.3, -9.0))
    m.cube("head", (-6.5, -1.6, -2.6), (13, 3.2, 2.6), "head")
    m.cube("head", (-5.0, -1.2, -3.2), (10, 2.4, 0.6), "mouth")                     # offenes Maul
    for side, sx in (("left", 1), ("right", -1)):
        eye_x = 6.5 if sx > 0 else -7.1
        m.cube("head", (eye_x, -0.9, -2.2), (0.6, 1.0, 1.0), "eye")
        fin = f"{side}_cephalic"
        m.bone(fin, "head", pivot=(6.0 * sx, 0.1, -2.4), rotation=(4, -8 * sx, 0))
        m.cube(fin, (-0.9 if sx > 0 else -1.1, -0.9, -5.4), (2.0, 1.8, 5.4), "skin")
        tip = f"{side}_cephalic_tip"
        m.bone(tip, fin, pivot=(0, 0.0, -5.4), rotation=(-10, 6 * sx, 0))
        m.cube(tip, (-0.8 if sx > 0 else -0.9, -0.7, -2.4), (1.7, 1.4, 2.4), "skin")

    # Brustflossen: die Vorderkante läuft als gerade, nach hinten gezogene Linie vom Kopf bis zur Spitze, die Flosse
    # wird nach außen immer schmaler (ein Dreieck in drei Gliedern)
    for side, sx in (("left", 1), ("right", -1)):
        w1 = f"{side}_wing"
        m.bone(w1, "body", pivot=(8.0 * sx, 0, -7.0), rotation=(0, -10 * sx, 0))
        m.cube(w1, (0 if sx > 0 else -12, -1.2, 0), (12, 2.4, 13), "skin")
        w2 = f"{side}_wing_mid"
        m.bone(w2, w1, pivot=(12 * sx, 0, 0), rotation=(0, -16 * sx, 0))
        m.cube(w2, (0 if sx > 0 else -11, -0.8, 0), (11, 1.6, 8), "skin")
        w3 = f"{side}_wing_tip"
        m.bone(w3, w2, pivot=(11 * sx, 0, 0), rotation=(0, -24 * sx, 0))
        m.cube(w3, (0 if sx > 0 else -10, -0.5, 0), (10, 1.0, 3.6), "skin")
        pel = f"{side}_pelvic"
        m.bone(pel, "body", pivot=(3.0 * sx, 0.4, 8.0), rotation=(0, 18 * sx, 0))
        m.cube(pel, (-1.4, -0.4, 0), (2.8, 0.8, 3.6), "skin")

    m.bone("tail", "body", pivot=(0, -0.2, 9.0))
    m.cube("tail", (-0.7, -0.6, 0), (1.4, 1.2, 6), "skin")
    m.cube("tail", (-0.3, -2.4, 0.6), (0.6, 1.8, 2.4), "skin")                       # kleine Rückenflosse
    m.bone("tail_tip", "tail", pivot=(0, 0, 6))
    m.cube("tail_tip", (-0.4, -0.4, 0), (0.8, 0.8, 7), "skin")
    return m


# =============================================================================================
# Bemalung
# =============================================================================================

def _pos(ctx: Ctx):
    x, y, z = ctx.world
    return x - BODY[0], y - BODY[1], z - BODY[2]


def _top(ctx: Ctx, x, z):
    v = 0.3 + (noise3(x * 0.16, 0.0, z * 0.16, 11) - 0.5) * 0.3
    col = pick(ctx.pal["back"], v)
    # weiße Schulterflecken: ein Winkel hinter dem Kopf auf jeder Seite
    ax = abs(x)
    if 4.0 < ax < 17.0:
        # der Fleck folgt einer schrägen Linie vom Kopf nach außen und hinten
        line = (z + 6.5) - (ax - 4.0) * 0.55
        width = 2.6 - (ax - 4.0) * 0.13
        wobble = (noise3(x * 0.3, 1.0, z * 0.3, 12) - 0.5) * 1.6
        if abs(line + wobble) < width and z < 4.0:
            t = 1.0 - abs(line + wobble) / max(width, 0.1)
            col = mix(col, pick(ctx.pal["patch"], 0.4 + t * 0.5), min(1.0, 0.45 + t * 0.8))
    # feine helle Sprenkel
    if hash01(ctx.u, ctx.v, 13) < 0.014:
        col = mix(col, pick(ctx.pal["patch"], 0.3), 0.5)
    return col


def _belly(ctx: Ctx, x, z):
    v = 0.6 + (noise3(x * 0.2, 2.0, z * 0.2, 14) - 0.5) * 0.18 + max(0.0, abs(x) - 10) * 0.012
    col = pick(ctx.pal["belly"], v)
    # dunkle Tupfen (jeder Rochen anders – fest nach Lage)
    cell = 3.6
    cx, cz = math.floor(x / cell), math.floor(z / cell)
    if abs(x) < 9 and -6 < z < 8:
        for dx in (-1, 0, 1):
            for dz in (-1, 0, 1):
                gx, gz = cx + dx, cz + dz
                if hash01(gx, gz, 15) < 0.45:
                    px = (gx + 0.5 + (hash01(gx, gz, 16) - 0.5) * 0.6) * cell
                    pz = (gz + 0.5 + (hash01(gx, gz, 17) - 0.5) * 0.6) * cell
                    r = 0.5 + hash01(gx, gz, 18) * 0.7
                    if math.hypot(x - px, z - pz) < r:
                        col = pick(ctx.pal["spot"], 0.4 + hash01(gx, gz, 19) * 0.5)
    # zu den Flossenspitzen hin ein zarter, grauer Saum
    if abs(x) > 14:
        col = mix(col, pick(ctx.pal["spot"], 0.9), min(0.45, (abs(x) - 14) * 0.03))
    # Kiemenspalten: fünf schräge Linien je Seite hinter dem Kopf
    ax = abs(x)
    if 3.0 < ax < 6.5 and -8.0 < z < -2.0:
        k = (z + 8.0) / 1.25
        if abs(k - round(k)) < 0.18 and 1 <= round(k) <= 5:
            col = pick(ctx.pal["spot"], 0.2)
    return col


def _skin(ctx: Ctx):
    x, y, z = _pos(ctx)
    if ctx.face == "top":
        return _top(ctx, x, z)
    if ctx.face == "bottom":
        return _belly(ctx, x, z)
    # Seiten: oben dunkel, unten hell (ein schmaler Rand)
    t = (ctx.t + 0.5) / max(ctx.h, 1)
    return mix(_top(ctx, x, z), _belly(ctx, x, z), min(1.0, t * 1.2))


def _head(ctx: Ctx):
    col = _skin(ctx)
    if ctx.face == "front":
        x, y, z = _pos(ctx)
        # ein heller Saum um das Maul
        col = pick(ctx.pal["belly"], 0.55)
    return col


def _mouth(ctx: Ctx):
    s = (ctx.s + 0.5) / max(ctx.w, 1)
    t = (ctx.t + 0.5) / max(ctx.h, 1)
    if ctx.face == "front":
        edge = min(s, 1 - s, t, 1 - t)
        if edge < 0.14:
            return pick(ctx.pal["belly"], 0.35)
        # Kiemenreusen im Schlund: feine, helle senkrechte Linien
        if ctx.s % 3 == 1 and 0.3 < t < 0.8:
            return pick(ctx.pal["mouth"], 0.9)
        return pick(ctx.pal["mouth"], 0.2 + t * 0.3)
    return pick(ctx.pal["belly"], 0.4)


def _eye(ctx: Ctx):
    if ctx.face in ("west", "east") and ctx.s == 0 and ctx.t == 0:
        return pick(ctx.pal["eye"], 1.0)
    return pick(ctx.pal["eye"], 0.1)


def materials(variant: str) -> dict:
    return {"skin": _skin, "head": _head, "mouth": _mouth, "eye": _eye}
