"""
Sternenrochen: ein riesiger, friedlicher Rochen, der mit langsamen Flügelschlägen durch das Sternennichts gleitet.

  * ein flacher Leib mit weit ausladenden, nach hinten geschwungenen Schwingen (in drei Gliedern, damit sie wellen)
  * oben tiefes Nachtblau, darin funkelnde Sterne, die zu Sternbildern verbunden sind; an der Vorderkante der Schwingen
    ein Schleier aus violettem Nebel, die Spitzen laufen durchscheinend aus
  * unten bleich lavendelfarben mit Kiemenspalten
  * vorn zwei eingerollte Kopflappen, seitlich kleine, sanft leuchtende Augen
  * ein langer, peitschenartiger Schwanz mit leuchtender Spitze

Knochen-Namen werden in StarRayModel (Java) für die Animationen verwendet.
"""
from __future__ import annotations

import math

from ..geometry import Model
from ..paint import Ctx, hash01, hexc, mix, noise3, pick

PALETTES = {
    "star_ray": {
        "back": [hexc(c) for c in ("#060a22", "#0b1230", "#111a40", "#18244f", "#20305f", "#2a3d70")],
        "nebula": [hexc(c) for c in ("#2a1a6a", "#4a2a92", "#7a3ab4", "#a85ad0", "#d49af0")],
        "belly": [hexc(c) for c in ("#6a6a98", "#8a88b4", "#a8a6cc", "#c4c2e0", "#dcdaf0", "#eeecf8")],
        "star": [hexc(c) for c in ("#9ab8ff", "#c8d8ff", "#eef2ff", "#ffffff")],
        "line": [hexc(c) for c in ("#3a5aa0", "#5a7ac8")],
        "eye": [hexc(c) for c in ("#3a6ab8", "#8ab8ff", "#e0ecff")],
        "tip": [hexc(c) for c in ("#3ab8e8", "#9ae8ff", "#ffffff")],
    },
}

BODY = (0.0, 19.0, 0.0)


def build() -> Model:
    m = Model("star_ray", 192, 128)
    m.tex_res = 2
    m.bone("body", pivot=BODY)
    m.cube("body", (-5.5, -2.0, -8.5), (11, 4, 17), "body")
    m.cube("body", (-7.0, -1.5, -6.5), (14, 3, 13), "body")
    m.cube("body", (-3.5, -2.5, -6.0), (7, 0.5, 11), "body")                      # Rückenwölbung

    m.bone("head", "body", pivot=(0, 0.2, -8.5))
    m.cube("head", (-5.0, -1.4, -3.5), (10, 2.8, 3.5), "head")
    for side, sx in (("left", 1), ("right", -1)):
        eye_x = 5.0 if sx > 0 else -5.6
        m.cube("head", (eye_x, -1.0, -2.6), (0.6, 1.2, 1.2), "eye")
        fin = f"{side}_cephalic"
        m.bone(fin, "head", pivot=(4.0 * sx, 0.0, -3.2), rotation=(6, 6 * sx, 0))
        m.cube(fin, (-0.8, -0.6, -4.0), (1.6, 1.4, 4.0), "fin")
        curl = f"{side}_cephalic_curl"
        m.bone(curl, fin, pivot=(0, 0.6, -4.0), rotation=(-55, 0, 0))
        m.cube(curl, (-0.7, -0.5, -1.6), (1.4, 1.1, 1.6), "fin")

    for side, sx in (("left", 1), ("right", -1)):
        w1 = f"{side}_wing"
        m.bone(w1, "body", pivot=(6.5 * sx, 0, -1.5))
        m.cube(w1, (0 if sx > 0 else -14, -1.1, -6.5), (14, 2.2, 13), "wing")
        w2 = f"{side}_wing_mid"
        m.bone(w2, w1, pivot=(14 * sx, 0, -1.0), rotation=(0, -16 * sx, 0))
        m.cube(w2, (0 if sx > 0 else -12, -0.7, -5.0), (12, 1.4, 8.5), "wing")
        w3 = f"{side}_wing_tip"
        m.bone(w3, w2, pivot=(12 * sx, 0, -1.5), rotation=(0, -26 * sx, 0))
        m.cube(w3, (0 if sx > 0 else -8, -0.4, -2.6), (8, 0.8, 4.2), "wing")
        pel = f"{side}_pelvic"
        m.bone(pel, "body", pivot=(3.2 * sx, 0.5, 7.5), rotation=(0, 22 * sx, 0))
        m.cube(pel, (-1.5, -0.4, 0), (3, 0.8, 4.5), "wing")

    m.bone("tail", "body", pivot=(0, 0, 8.5))
    m.cube("tail", (-0.7, -0.6, 0), (1.4, 1.2, 9), "tail")
    m.bone("tail_mid", "tail", pivot=(0, 0, 9))
    m.cube("tail_mid", (-0.5, -0.45, 0), (1.0, 0.9, 10), "tail")
    m.bone("tail_tip", "tail_mid", pivot=(0, 0, 10))
    m.cube("tail_tip", (-0.35, -0.35, 0), (0.7, 0.7, 9), "tail")
    m.cube("tail_tip", (-0.6, -0.6, 8.5), (1.2, 1.2, 1.2), "glowtip")
    return m


# =============================================================================================
# Bemalung
# =============================================================================================

def _pos(ctx: Ctx):
    x, y, z = ctx.world
    return x - BODY[0], y - BODY[1], z - BODY[2]


def _stars(ctx: Ctx, col, x, z, density=0.03):
    """Sterne und ihre Verbindungslinien (Sternbilder) auf dem Rücken."""
    # Sternbilder: feste Punkte in einem groben Raster, benachbarte durch schwache Linien verbunden
    cell = 7.0
    cx, cz = math.floor(x / cell), math.floor(z / cell)
    best = None
    for dx in (-1, 0, 1):
        for dz in (-1, 0, 1):
            gx, gz = cx + dx, cz + dz
            px = (gx + 0.2 + hash01(gx, gz, 1) * 0.6) * cell
            pz = (gz + 0.2 + hash01(gx, gz, 2) * 0.6) * cell
            d = math.hypot(x - px, z - pz)
            if best is None or d < best[0]:
                best = (d, px, pz, gx, gz)
    d, px, pz, gx, gz = best
    if hash01(gx, gz, 3) > 0.35:
        # Linie zum Nachbarstern rechts
        nx = (gx + 1 + 0.2 + hash01(gx + 1, gz, 1) * 0.6) * cell
        nz = (gz + 0.2 + hash01(gx + 1, gz, 2) * 0.6) * cell
        vx, vz = nx - px, nz - pz
        lv = math.hypot(vx, vz) or 1.0
        tt = ((x - px) * vx + (z - pz) * vz) / (lv * lv)
        if 0.0 < tt < 1.0:
            dist = abs((x - px) * vz - (z - pz) * vx) / lv
            if dist < 0.22:
                col = mix(col, pick(ctx.pal["line"], 0.6), 0.55)
                ctx.glow = mix((0, 0, 0, 255), pick(ctx.pal["line"], 0.9), 0.45)
        if d < 0.55:
            col = pick(ctx.pal["star"], 0.95)
            ctx.glow = col
            return col
        if d < 0.95:
            col = mix(col, pick(ctx.pal["star"], 0.5), 0.5)
            ctx.glow = mix((0, 0, 0, 255), pick(ctx.pal["star"], 0.4), 0.5)
            return col
    h = hash01(ctx.u, ctx.v, 4)
    if h < density:
        k = hash01(ctx.u, ctx.v, 5)
        star = pick(ctx.pal["star"], 0.3 + k * 0.7)
        col = mix(col, star, 0.4 + k * 0.5)
        if k > 0.5:
            ctx.glow = mix((0, 0, 0, 255), star, 0.5 + k * 0.4)
    return col


def _back_col(ctx: Ctx, x, y, z):
    v = 0.35 + (noise3(x * 0.15, y * 0.15, z * 0.15, 7) - 0.5) * 0.4 - abs(x) * 0.006
    col = pick(ctx.pal["back"], v)
    # Nebelschleier entlang der Vorderkante der Schwingen
    lead = -z - abs(x) * 0.22
    neb = noise3(x * 0.12, 0.0, z * 0.12, 8) + max(0.0, lead + 2.0) * 0.06
    if neb > 0.62:
        col = mix(col, pick(ctx.pal["nebula"], (neb - 0.62) * 2.2), min(0.65, (neb - 0.62) * 2.0))
    return col


def _belly(ctx: Ctx, x, z):
    v = 0.68 + (noise3(x * 0.25, 0.0, z * 0.25, 9) - 0.5) * 0.16 - abs(x) * 0.008
    col = pick(ctx.pal["belly"], v)
    # Kiemenspalten (fünf je Seite) und ein paar dunkle Flecken
    if 1.5 < abs(x) < 4.6 and -6.5 < z < -1.0 and int((z + 6.5) * 1.1) % 2 == 0 and abs(abs(x) - 3.0) < 1.2:
        col = pick(ctx.pal["belly"], 0.25)
    if noise3(x * 0.6, 3.0, z * 0.6, 10) > 0.74:
        col = mix(col, pick(ctx.pal["back"], 0.5), 0.35)
    return col


def _body(ctx: Ctx):
    x, y, z = _pos(ctx)
    if ctx.face == "bottom":
        return _belly(ctx, x, z)
    col = _back_col(ctx, x, y, z)
    if ctx.face == "top":
        col = _stars(ctx, col, x, z)
    elif ctx.face in ("west", "east", "front", "back"):
        t = (ctx.t + 0.5) / max(ctx.h, 1)
        col = mix(col, _belly(ctx, x, z), max(0.0, t - 0.5) * 1.4)
    return col


def _head(ctx: Ctx):
    x, y, z = _pos(ctx)
    if ctx.face == "front":
        # breites Maul unten an der Stirn
        t = (ctx.t + 0.5) / max(ctx.h, 1)
        if t > 0.62 and abs(x) < 3.5:
            return pick(ctx.pal["back"], 0.05)
        return mix(_back_col(ctx, x, y, z), _belly(ctx, x, z), t * 0.8)
    return _body(ctx)


def _wing(ctx: Ctx):
    x, y, z = _pos(ctx)
    span = min(1.0, abs(x) / 40.0)
    if ctx.face == "bottom":
        col = _belly(ctx, x, z)
    else:
        col = _back_col(ctx, x, y, z)
        if ctx.face == "top":
            col = _stars(ctx, col, x, z, 0.035)
        else:
            # Kante: heller Saum
            col = mix(col, pick(ctx.pal["nebula"], 0.7), 0.4)
    # die Spitzen laufen durchscheinend aus und leuchten am Rand schwach
    alpha = 255 if span < 0.55 else round(255 - (span - 0.55) * 300)
    if span > 0.75 and ctx.face in ("front", "back", "west", "east"):
        ctx.glow = mix((0, 0, 0, 255), pick(ctx.pal["nebula"], 0.8), 0.6)
    return col[:3] + (max(130, alpha),)


def _fin(ctx: Ctx):
    x, y, z = _pos(ctx)
    t = (ctx.t + 0.5) / max(ctx.h, 1)
    under = 0.85 if ctx.face == "bottom" else 0.0 if ctx.face == "top" else t * 0.6
    col = mix(_back_col(ctx, x, y, z), _belly(ctx, x, z), under)
    if ctx.face in ("front", "back") and ctx.t == ctx.h - 1:
        col = mix(col, pick(ctx.pal["nebula"], 0.7), 0.5)
    return col


def _tail(ctx: Ctx):
    x, y, z = _pos(ctx)
    v = 0.4 + (noise3(0.0, y * 0.5, z * 0.5, 11) - 0.5) * 0.3
    col = pick(ctx.pal["back"], v)
    if hash01(ctx.u, ctx.v, 12) < 0.06:
        col = pick(ctx.pal["star"], 0.6)
        ctx.glow = col
    return col


def _glowtip(ctx: Ctx):
    col = pick(ctx.pal["tip"], 0.4 + hash01(ctx.u, ctx.v, 13) * 0.5)
    ctx.glow = col
    return col


def _eye(ctx: Ctx):
    t = (ctx.t + 0.5) / max(ctx.h, 1)
    col = pick(ctx.pal["eye"], 0.9 - abs(t - 0.5))
    ctx.glow = mix((0, 0, 0, 255), col, 0.7)
    return col


def materials(variant: str) -> dict:
    return {"body": _body, "head": _head, "wing": _wing, "fin": _fin, "tail": _tail, "glowtip": _glowtip, "eye": _eye}


def preview_pose(variant: str) -> dict:
    return {"left_wing": (0, 0, -8), "right_wing": (0, 0, 8), "left_wing_mid": (0, 0, -10), "right_wing_mid": (0, 0, 10)}
