"""
Moorschnapper des Nebelmoors (0.86): eine riesige Schnappschildkröte. Ein flacher, rauer Panzer mit drei Reihen
spitzer Höcker und einem gezackten Hinterrand, bewachsen mit Moos, von dem Algenfäden hängen; ein großer Kopf mit
hakigem Schnabel und kleinen gelben Augen, ein faltiger Hals, kräftige Beine mit Krallen und ein langer Schwanz mit
gezacktem Kamm. Haut graubraun und warzig, der Bauchpanzer schmutzig gelb.

Knochen (für MireSnapperModel in Java): body (Panzer, mit neck → head → jaw; tail1 → tail2), die Beine
left_front_leg, right_front_leg, left_hind_leg, right_hind_leg.
"""
from __future__ import annotations

import math

from ..geometry import Model
from ..paint import Ctx, hash01, hexc, mix, noise3, pick, rel_y

PALETTES = {
    "mire_snapper": {
        "shell": [hexc(c) for c in ("#1a1a10", "#262618", "#343320", "#43412a", "#535034", "#655f40", "#79714e")],
        "skin": [hexc(c) for c in ("#2a2620", "#3a342c", "#4a4338", "#5c5446", "#6e6554", "#827862")],
        "plastron": [hexc(c) for c in ("#5a4e2a", "#746634", "#8e7e40", "#a8964e", "#c0ae62")],
        "moss": [hexc(c) for c in ("#1a2a10", "#243a14", "#304c18", "#3e601e", "#4e7426", "#628a30")],
        "algae": [hexc(c) for c in ("#20341a", "#2c4422", "#3a562c", "#4a6a36")],
        "beak": [hexc(c) for c in ("#2a2622", "#3c3630", "#504840", "#665c52", "#7e7266")],
        "eye": [hexc(c) for c in ("#0a0806", "#a89020", "#d8bc40", "#f4dc70")],
        "claw": [hexc(c) for c in ("#141210", "#24201c", "#38322c", "#4e463e")],
    },
}

_LIT = {"top": 0.08, "front": 0.02, "west": 0.0, "east": -0.02, "back": -0.04, "bottom": -0.14}


def build() -> Model:
    m = Model("mire_snapper", 128, 64)
    m.tex_res = 2
    m.world_faces = True
    m.bone("body", pivot=(0, 20.4, 0))
    m.cube("body", (-5.5, -2.0, -6.5), (11.0, 3.4, 13.0), "shell_rim")       # Rand des Panzers
    m.cube("body", (-4.6, -3.8, -5.6), (9.2, 1.9, 11.2), "shell")            # die flache Kuppel
    m.cube("body", (-3.4, -4.5, -4.4), (6.8, 0.8, 8.8), "shell")
    m.cube("body", (-4.9, 1.3, -5.9), (9.8, 0.9, 11.8), "plastron")           # Bauchpanzer
    # drei Reihen Höcker
    for i, z in enumerate((-3.8, -1.6, 0.6, 2.8)):
        m.cube("body", (-0.7, -5.6, z), (1.4, 1.2, 1.6), "spike")
    for sx in (-1, 1):
        for z in (-3.0, -0.7, 1.6):
            m.cube("body", (2.4 * sx - 0.6, -4.8, z), (1.2, 1.0, 1.4), "spike")
    # gezackter Hinterrand
    for x in (-4.0, -1.6, 0.8, 3.2):
        m.cube("body", (x, -1.4, 6.4), (0.9, 1.6, 1.0), "shell_rim")
    # Moospolster und Algenfäden
    m.cube("body", (-4.0, -4.3, -4.8), (3.0, 0.6, 2.4), "moss")
    m.cube("body", (1.6, -4.3, 2.0), (2.6, 0.6, 3.0), "moss")
    m.cube("body", (-3.6, -4.9, 2.6), (2.0, 0.5, 2.0), "moss")
    for x, z, h in ((-5.6, -2.0, 2.6), (5.5, 1.0, 3.0), (-5.6, 3.6, 2.2), (2.4, 6.6, 2.4), (-2.2, 6.6, 1.8)):
        m.cube("body", (x, 1.2, z), (0.1, h, 0.5), "algae")
    # Hals, Kopf, Kiefer
    m.bone("neck", "body", pivot=(0, -0.2, -6.0))
    m.cube("neck", (-1.9, -1.6, -3.2), (3.8, 3.0, 3.6), "neck")
    m.bone("head", "neck", pivot=(0, -0.2, -3.0))
    m.cube("head", (-2.5, -2.2, -4.6), (5.0, 3.4, 4.8), "head")
    m.cube("head", (-1.5, -1.8, -6.0), (3.0, 2.1, 1.5), "beak")
    m.cube("head", (-0.9, 0.1, -6.3), (1.8, 0.9, 0.8), "beak")                  # der Haken
    m.cube("head", (2.35, -1.4, -3.4), (0.2, 0.8, 0.8), "eye")
    m.cube("head", (-2.55, -1.4, -3.4), (0.2, 0.8, 0.8), "eye")
    m.bone("jaw", "head", pivot=(0, 0.9, -1.0))
    m.cube("jaw", (-2.1, 0, -4.6), (4.2, 1.1, 4.4), "jaw")
    # Schwanz mit Kamm
    m.bone("tail1", "body", pivot=(0, -0.4, 6.4))
    m.cube("tail1", (-1.1, -0.9, 0), (2.2, 1.8, 4.6), "neck")
    for z in (0.6, 2.0, 3.4):
        m.cube("tail1", (-0.4, -1.8, z), (0.8, 1.0, 0.9), "spike")
    m.bone("tail2", "tail1", pivot=(0, 0.1, 4.5))
    m.cube("tail2", (-0.7, -0.6, 0), (1.4, 1.2, 4.2), "neck")
    for z in (0.5, 1.9, 3.2):
        m.cube("tail2", (-0.3, -1.3, z), (0.6, 0.8, 0.8), "spike")
    # Beine: seitlich abgespreizt (Oberschenkel waagrecht unter dem Panzerrand), der Fuß stützt nach unten, Krallen vorn
    for side, sx in (("left", 1), ("right", -1)):
        lo = (lambda a, w: a if sx > 0 else -a - w)
        for part, pz in (("front", -3.8), ("hind", 3.9)):
            m.bone(f"{side}_{part}_leg", pivot=(4.4 * sx, 21.2, pz))
            m.cube(f"{side}_{part}_leg", (lo(-0.6, 3.2), -0.9, -1.4), (3.2, 2.2, 2.8), "leg")
            m.cube(f"{side}_{part}_leg", (lo(1.1, 1.8), 0.4, -1.6), (1.8, 2.4, 3.0), "leg")
            for dx in (0.0, 0.6, 1.2):
                m.cube(f"{side}_{part}_leg", (lo(1.2 + dx, 0.45), 2.15, -2.15), (0.45, 0.65, 0.6), "claw")
    return m


# =============================================================================================
# Bemalung
# =============================================================================================

def _scutes(ctx: Ctx) -> float:
    """Die Schilde des Panzers: Abstand zur nächsten Fuge (0 auf der Fuge)."""
    x, _, z = ctx.world
    cx, cz = x / 3.0, z / 3.2
    fx = abs((cx % 1.0) - 0.5)
    fz = abs(((cz + (math.floor(cx) % 2) * 0.5) % 1.0) - 0.5)
    return min(0.5 - fx, 0.5 - fz)


def _shell(ctx: Ctx):
    x, y, z = ctx.world
    n = noise3(x * 0.5, y * 0.5, z * 0.5, 961)
    v = 0.48 + (n - 0.5) * 0.3 + _LIT[ctx.face]
    if ctx.face == "top":
        d = _scutes(ctx)
        if d < 0.05:
            v -= 0.22                                                        # Fuge zwischen den Schilden
        else:
            v += (0.5 - d) * 0.2                                             # Wachstumsringe zum Rand hin heller
            if (d * 10.0) % 1.0 < 0.15:
                v -= 0.06
        if noise3(x * 0.35, 0, z * 0.35, 962) > 0.68:
            return pick(ctx.pal["moss"], 0.4 + (n - 0.5) * 0.4)            # Moosflecken
    return pick(ctx.pal["shell"], v)


def _shell_rim(ctx: Ctx):
    if ctx.face == "bottom":
        return pick(ctx.pal["plastron"], 0.3)
    x, y, z = ctx.world
    v = 0.4 + (noise3(x * 0.6, y * 0.6, z * 0.6, 963) - 0.5) * 0.3 + _LIT[ctx.face]
    if ctx.face in ("west", "east", "front", "back") and int((x + z) * 0.75) % 2 == 0:
        v -= 0.06                                                            # die Randschilde
    if ctx.face in ("west", "east", "front", "back") and rel_y(ctx) > 0.75:
        return pick(ctx.pal["algae"], 0.35)                                  # unten verschlammt
    return _shell(ctx) if ctx.face == "top" else pick(ctx.pal["shell"], v)


def _plastron(ctx: Ctx):
    x, y, z = ctx.world
    if ctx.face != "bottom" and ctx.face != "top":
        return pick(ctx.pal["plastron"], 0.25)
    v = 0.5 + (noise3(x * 0.7, y, z * 0.7, 964) - 0.5) * 0.3
    if abs(x) < 0.15 or abs((z / 3.0) % 1.0 - 0.5) > 0.47:
        v -= 0.2
    return pick(ctx.pal["plastron"], v)


def _spike(ctx: Ctx):
    rt = rel_y(ctx)
    v = 0.35 + (1.0 - rt) * 0.25 + _LIT[ctx.face]
    return pick(ctx.pal["shell"], v)


def _moss(ctx: Ctx):
    x, y, z = ctx.world
    v = 0.5 + (hash01(int(x * 2), int(z * 2), 965) - 0.5) * 0.5 + _LIT[ctx.face]
    return pick(ctx.pal["moss"], v)


def _algae(ctx: Ctx):
    return pick(ctx.pal["algae"], 0.4 + (hash01(ctx.t, 966) - 0.5) * 0.3)


def _skin(ctx: Ctx):
    """Warzige, graubraune Haut mit dunklen Falten."""
    x, y, z = ctx.world
    n = noise3(x * 1.1, y * 1.1, z * 1.1, 967)
    v = 0.45 + (n - 0.5) * 0.35 + _LIT[ctx.face]
    if hash01(int(x * 2), int(y * 2), int(z * 2), 968) > 0.82:
        v += 0.14                                                            # Warzen
    if ctx.face == "bottom":
        v += 0.12
    return pick(ctx.pal["skin"], v)


def _neck(ctx: Ctx):
    x, y, z = ctx.world
    col = _skin(ctx)
    if ctx.face in ("west", "east", "top") and (z * 1.3) % 1.0 < 0.18:
        return mix(col, pick(ctx.pal["skin"], 0.1), 0.6)                     # Falten
    return col


def _head(ctx: Ctx):
    lx, ly, lz = ctx.local
    if ctx.lface == "top" and abs(lx) < 1.6 and -4.2 < lz < -1.0:
        x, y, z = ctx.world
        return pick(ctx.pal["skin"], 0.3 + hash01(int(x * 2), int(z * 2), 969) * 0.3)  # grobe Kopfschilde
    return _skin(ctx)


def _beak(ctx: Ctx):
    rt = rel_y(ctx)
    return pick(ctx.pal["beak"], 0.5 - rt * 0.2 + _LIT[ctx.face])


def _jaw(ctx: Ctx):
    if ctx.lface == "top":
        return pick(ctx.pal["skin"], 0.75)                                   # das blasse Innere des Mauls
    lz = ctx.local[2]
    if lz < -3.6:
        return pick(ctx.pal["beak"], 0.4 + _LIT[ctx.face])
    return _skin(ctx)


def _eye(ctx: Ctx):
    if ctx.lface not in ("west", "east"):
        return pick(ctx.pal["skin"], 0.3)
    rs = (ctx.s + 0.5) / max(ctx.w, 1)
    rt = (ctx.t + 0.5) / max(ctx.h, 1)
    if abs(rs - 0.5) < 0.15:
        return pick(ctx.pal["eye"], 0.0)                                     # die waagrechte Pupille (Strich)
    if abs(rs - 0.5) > 0.4 or abs(rt - 0.5) > 0.4:
        return pick(ctx.pal["skin"], 0.2)
    return pick(ctx.pal["eye"], 0.6)


def _leg(ctx: Ctx):
    x, y, z = ctx.world
    col = _skin(ctx)
    if ctx.face in ("west", "east", "front", "back") and (y * 1.2) % 1.0 < 0.2:
        return mix(col, pick(ctx.pal["skin"], 0.15), 0.5)                    # Schuppenringe
    return col


def _claw(ctx: Ctx):
    return pick(ctx.pal["claw"], 0.4 + _LIT[ctx.face])


def materials(variant: str) -> dict:
    return {"shell": _shell, "shell_rim": _shell_rim, "plastron": _plastron, "spike": _spike, "moss": _moss, "algae": _algae,
            "neck": _neck, "head": _head, "beak": _beak, "jaw": _jaw, "eye": _eye, "leg": _leg, "claw": _claw}
