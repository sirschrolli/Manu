"""
Voltarion, der Sturmbringer – der Blitzdrachen-Boss.

Ein Wyvern: nur zwei kräftige Hinterbeine, die Flügel sind zugleich die Vorderglieder (mit Daumenkrallen am
Handgelenk). Schlanker, aufrecht getragener Leib, langer Hals mit einem Kamm aus leuchtenden Blitzflossen, ein
schmaler Kopf mit einer Krone aus vier nach hinten geschwungenen Hörnern, ein peitschenlanger Schwanz (zwölf Glieder)
mit einer gezackten Klinge wie ein Blitz. Riesige Flügel mit drei langen Fingern. Schuppen schiefergrau bis
nachtblau, durchzogen von gleißenden Blitzadern (Türkis und Weißgelb). Grundgröße ca. 12 Blöcke, im Spiel ×2,1.
"""
from __future__ import annotations

import math

from ..geometry import Model
from ..paint import Ctx, flat_painter, hash01, mix, noise3, pick, ramp, transparent_painter
from .boss_common import (arm_membrane, chain, cracked_scales, finger_membrane, glow_painter, glowing_eye, horn_painter,
                          leg, mouth_painter, plates_painter, wing)

PALETTES = {
    "voltarion": {
        "back": ramp("#1a2234", "#6c7fa0"), "belly": ramp("#3a4862", "#c4d2e6"), "crack": ramp("#1a8ac8", "#f0fcff"),
        "spark": ramp("#c8a010", "#fffce0"), "membrane": ramp("#141e36", "#4c6494"), "vein": ramp("#1a6ab0", "#c8f4ff"),
        "horn": ramp("#20242e", "#b4bcc8"), "spike": ramp("#1e2436", "#9aaac8"), "claw": ramp("#16181e", "#d8dce8"),
        "eye": ramp("#4ac8ff", "#ffffff"), "mouth": ramp("#141c34", "#4a7ac0"), "fin": ramp("#1a7ac0", "#e0faff"),
    },
}

TEX = (512, 448)


def build() -> Model:
    m = Model("voltarion", *TEX)

    # ---------------------------------------------------------------- Rumpf (schlank, aufrecht)
    m.bone("body", pivot=(0, -10, 0))
    m.cube("body", (-10, -10, -18), (20, 19, 34), "scales")
    m.cube("body", (-8, -13, -15), (16, 3.5, 27), "scales")                     # Rückenlinie
    m.cube("body", (-8.5, 8, -17), (17, 4, 20), "plates")                       # Brust
    m.cube("body", (-7.5, 8, 3), (15, 3, 12), "plates")
    m.cube("body", (-11.5, -6, 4), (2, 12, 11), "scales")                       # Hüften
    m.cube("body", (9.5, -6, 4), (2, 12, 11), "scales")
    m.cube("body", (-12, -9, -16), (3, 9, 9), "scales")                         # Flügelansätze
    m.cube("body", (9, -9, -16), (3, 9, 9), "scales")
    # Blitzflossen entlang des Rückgrats
    for i, (z, h) in enumerate([(-13, 6), (-7, 7.5), (-1, 8), (5, 6.5), (11, 5)]):
        m.cube("body", (-0.4, -13 - h, z), (0.8, h + 0.5, 4.5), "fin")

    # ---------------------------------------------------------------- Hals (sechs Glieder) mit Flossenkamm und Kopf
    neck_sizes = [(11, 11, 7), (10, 10, 6.8), (9.2, 9.2, 6.5), (8.6, 8.6, 6.2), (8, 8, 6), (7.4, 7.4, 5.8)]
    neck = chain(m, "neck", "body", (0, -6, -17), neck_sizes, [-30, -14, -4, 4, 10, 10], direction=-1, belly="plates")
    for i, (name, (w, h, length)) in enumerate(zip(neck, neck_sizes)):
        fh = 4.5 - i * 0.45
        m.cube(name, (-0.35, -h / 2 - fh, -length + 0.5), (0.7, fh + 0.5, length - 1), "fin")
    m.bone("head", neck[-1], pivot=(0, 0, -5.8), rotation=(30, 0, 0))
    m.cube("head", (-5.5, -5, -13), (11, 10, 13), "scales")                     # Schädel
    m.cube("head", (-4, -3.2, -26), (8, 6.5, 13.5), "scales")                   # lange Schnauze
    m.cube("head", (-2.8, -4.5, -24.5), (5.6, 1.5, 11), "scales")               # Nasenrücken
    m.cube("head", (-3.2, -3.5, -26.3), (2, 1.2, 1.2), "nostril")
    m.cube("head", (1.2, -3.5, -26.3), (2, 1.2, 1.2), "nostril")
    m.cube("head", (-3.6, 3.3, -25.5), (7.2, 1.3, 12.5), "teeth")
    m.cube("head", (-3.6, 3.2, -25.4), (7.2, 0.2, 13), "mouth")
    m.cube("head", (5.1, -2.6, -12.5), (1, 1.8, 3.6), "eye")
    m.cube("head", (-6.1, -2.6, -12.5), (1, 1.8, 3.6), "eye")
    m.cube("head", (5.0, -5.4, -12.5), (1.6, 1.4, 6), "scales")                 # Brauen
    m.cube("head", (-6.6, -5.4, -12.5), (1.6, 1.4, 6), "scales")
    # Krone aus vier nach hinten geschwungenen Hörnern (je zwei Glieder)
    for side, sx in (("l", 1), ("r", -1)):
        for k, (x, y, z, pitch, yaw, l1, l2, th) in enumerate([(2.8, -5, -6, -28, 12, 10, 8, 2.6), (4.6, -3.5, -4, -10, 30, 8, 6, 2.0)]):
            b1 = f"horn_{side}{k}a"
            b2 = f"horn_{side}{k}b"
            m.bone(b1, "head", pivot=(x * sx, y, z), rotation=(pitch, yaw * sx, 0))
            m.cube(b1, (-th / 2, -th / 2, 0), (th, th, l1), "horn")
            m.bone(b2, b1, pivot=(0, 0, l1), rotation=(-18, 4 * sx, 0))
            m.cube(b2, (-th * 0.35, -th * 0.35, 0), (th * 0.7, th * 0.7, l2), "horntip")
    m.bone("jaw", "head", pivot=(0, 3.3, -11), rotation=(3, 0, 0))
    m.cube("jaw", (-3.8, 0, -14.5), (7.6, 3, 15), "scales")
    m.cube("jaw", (-3.2, -1.1, -14.2), (6.4, 1.1, 10), "teeth")
    m.cube("jaw", (-2.8, -0.2, -13.5), (5.6, 0.4, 12.5), "mouth")
    m.cube("jaw", (-0.5, 2.5, -10), (1, 3, 5), "fin")                           # Kinnflosse

    # ---------------------------------------------------------------- Peitschenschwanz (zwölf Glieder) mit Blitzklinge
    sizes = [(10, 9, 9), (9, 8, 9), (8, 7.2, 8.5), (7, 6.4, 8.5), (6.2, 5.6, 8), (5.4, 5, 8), (4.8, 4.4, 7.5), (4.2, 3.8, 7.5),
             (3.6, 3.2, 7), (3, 2.8, 7), (2.6, 2.4, 6.5), (2.2, 2, 6)]
    tail = chain(m, "tail", "body", (0, -3, 15), sizes, [-6, -4, -2, 0, 1, 2, 2, 2, 2, 2, 1, 1], direction=1, belly="plates")
    for i, (name, (w, h, length)) in enumerate(zip(tail[:9], sizes)):
        fh = 3.6 - i * 0.35
        m.cube(name, (-0.3, -h / 2 - fh, 1.5), (0.6, fh + 0.5, length - 3), "fin")
    m.cube(tail[-1], (-5.5, -0.5, 4.5), (11, 1, 13), "blade")                    # Blitzklinge (flach)

    # ---------------------------------------------------------------- Hinterbeine (kräftig, wie bei einem Raubsaurier)
    for side, sx in (("l", 1), ("r", -1)):
        leg(m, side, sx, "hind", "body", (9.5, 3, 8),
            [(9, 16, 11, -18), (6, 15, 6, 42), (8, 3, 12, -24)], toes=3, toe_len=4.2)

    # ---------------------------------------------------------------- Riesige Flügel (zugleich Vorderglieder)
    for side, sx in (("l", 1), ("r", -1)):
        wing(m, side, sx, "body", (10.5, -7, -12), arm=30, fore=36,
             fingers=[(62, 8), (56, 34), (47, 62)], arm_th=4.6, inner_chord=32, fore_chord=34, sag=0.18,
             mats={"claw": "claw"}, web_bones=True)
    return m


# =============================================================================================
# Bemalung
# =============================================================================================

def _fin(ctx: Ctx):
    """Blitzflossen: dünne, gezackte Flossen, die zur Kante hin gleißend leuchten."""
    oy, oz = ctx.cube.origin[1], ctx.cube.origin[2]
    h, d = ctx.cube.size[1], ctx.cube.size[2]
    rel = (ctx.local[1] - oy) / max(h, 1e-3)            # 0 oben, 1 unten
    along = (ctx.local[2] - oz) / max(d, 1e-3)
    # gezackter oberer Rand
    saw = (along * 3.0) % 1.0
    if rel < 0.35 * saw:
        return None
    col = pick(ctx.pal["fin"], 0.85 - 0.6 * rel)
    if rel < 0.5:
        ctx.glow = col
    return col


def _blade(ctx: Ctx):
    """Blitzklinge: Zickzack-Form wie ein Blitz, leuchtend."""
    lx, ly, lz = ctx.local
    ox, oz = ctx.cube.origin[0], ctx.cube.origin[2]
    w, d = ctx.cube.size[0], ctx.cube.size[2]
    u = (lx - ox) / max(w, 1e-3) * 2 - 1
    v = (lz - oz) / max(d, 1e-3)
    zig = 0.25 * math.sin(v * math.pi * 4.0)
    half = (1.0 - v) * 0.8 + 0.12
    if abs(u - zig) > half:
        return None
    col = pick(ctx.pal["spark"], 0.6 + 0.35 * (1 - abs(u - zig) / half))
    ctx.glow = col
    return col


def _horntip(ctx: Ctx):
    rel = 0.0
    oz, d = ctx.cube.origin[2], ctx.cube.size[2]
    if d > 0:
        rel = (ctx.local[2] - oz) / d
    col = pick(ctx.pal["horn"], 0.4 + 0.3 * rel)
    if rel > 0.6:
        col = pick(ctx.pal["crack"], 0.5 + 0.5 * rel)
        ctx.glow = col
    return col


def materials(variant: str) -> dict:
    return {
        "scales": cracked_scales(cell=3.6, seed=67, crack_width=0.012, heat=0.3, branch=0.7, freq=0.08),
        "plates": plates_painter("belly"),
        "fin": _fin,
        "blade": _blade,
        "horn": horn_painter("horn", rings=6),
        "horntip": _horntip,
        "spike": horn_painter("spike", rings=3),
        "claw": flat_painter("claw", 0.8),
        "teeth": flat_painter("claw", 0.95, 0.04),
        "eye": glowing_eye("eye"),
        "nostril": glow_painter("crack", 0.8),
        "mouth": mouth_painter(),
        "membrane_inner": arm_membrane("membrane", "vein", glow_veins=True),
        "membrane_fore": arm_membrane("membrane", "vein", fore=True, glow_veins=True),
        "membrane_outer": finger_membrane("membrane", "vein", tatter=0.12, glow_veins=True, seed=11),
        "none": transparent_painter(),
    }
