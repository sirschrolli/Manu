"""
Glaciara, die Frostkönigin – der Eisdrachen-Boss.

Schlank und elegant: langer Schwanenhals (acht Glieder), langer, dünner Schwanz mit einem Fächer aus Eisklingen,
schmaler Kopf mit einer Krone aus Eiskristallen, Kristallzacken entlang des Rückens, lange, schmale Flügel mit
federartigen Eisschuppen an der Hinterkante. Schuppen weiß bis blassblau mit leuchtenden Frostadern, Augen und
Kristalle leuchten eisblau. Grundgröße ca. 11,5 Blöcke, im Spiel ×2,1.
"""
from __future__ import annotations

import math

from ..geometry import Model
from ..paint import Ctx, flat_painter, hash01, mix, noise3, pick, ramp, transparent_painter
from .boss_common import (arm_membrane, chain, cracked_scales, crystal_painter, glow_painter, glowing_eye, leg,
                          mouth_painter, plates_painter, wing)

PALETTES = {
    "glaciara": {
        "back": ramp("#9ab6d2", "#ffffff"), "belly": ramp("#b4cee4", "#ffffff"),
        "crack": ramp("#1a8ad8", "#bff0ff"), "membrane": ramp("#86b8de", "#f6fdff"),
        "vein": ramp("#2aa8f0", "#e0faff"), "horn": ramp("#7ac0e8", "#ffffff"),
        "spike": ramp("#6ab4e0", "#f4ffff"), "claw": ramp("#34475c", "#e4eef6"),
        "eye": ramp("#1aa0ff", "#f0ffff"), "mouth": ramp("#1e2a44", "#6a8ac0"),
        "crystal": ramp("#3aa4e4", "#f0ffff"), "feather": ramp("#a8d0ec", "#ffffff"),
    },
}

TEX = (512, 384)


def build() -> Model:
    m = Model("glaciara", *TEX)

    # ---------------------------------------------------------------- Rumpf (schmal, hoch getragen)
    m.bone("body", pivot=(0, -10, 0))
    m.cube("body", (-9, -9, -18), (18, 17, 36), "scales")                         # Rumpf
    m.cube("body", (-7.5, -12, -15), (15, 3.5, 28), "scales")                     # Rückenlinie
    m.cube("body", (-8, 7, -17), (16, 4, 18), "chest")                            # Brust (Frostsack)
    m.cube("body", (-7, 7, 1), (14, 3, 14), "plates")                             # Bauch hinten
    m.cube("body", (-11, -6, -16), (2.5, 11, 10), "scales")                       # Schultern
    m.cube("body", (8.5, -6, -16), (2.5, 11, 10), "scales")
    m.cube("body", (-10.5, -5, 6), (2, 11, 11), "scales")                         # Hüften
    m.cube("body", (8.5, -5, 6), (2, 11, 11), "scales")
    # Kristallzacken entlang des Rückens (schräg nach hinten)
    for i, (z, h) in enumerate([(-15, 6), (-9, 8), (-3, 9), (3, 8), (9, 6.5)]):
        m.cube("body", (-1.2, -12 - h, z), (2.4, h + 0.5, 3.2), "crystal")
        m.cube("body", (-0.6, -12 - h - 2.0, z + 0.6), (1.2, 2.2, 1.8), "crystal")

    # ---------------------------------------------------------------- Hals (acht Glieder, Schwanenhals) und Kopf
    neck = chain(m, "neck", "body", (0, -6, -17),
                 [(11, 11, 6.5), (10, 10, 6.2), (9.2, 9.2, 6), (8.5, 8.5, 6), (8, 8, 5.8), (7.5, 7.5, 5.6), (7, 7, 5.5),
                  (6.6, 6.6, 5.4)],
                 [-34, -16, -8, -2, 4, 9, 12, 9], direction=-1, spikes=[0, 0, 0, 0, 0, 0, 0, 0], belly="plates")
    for i, name in enumerate(neck):
        if i % 2 == 0:
            h = 3.4 - i * 0.25
            m.cube(name, (-0.8, -5.4 + i * 0.25 - h, -4.0), (1.6, h + 0.5, 2.4), "crystal")
    m.bone("head", neck[-1], pivot=(0, 0, -5.5), rotation=(32, 0, 0))
    m.cube("head", (-5, -4.5, -12), (10, 9, 12), "scales")                        # Schädel
    m.cube("head", (-3.6, -2.8, -25), (7.2, 6, 13.5), "scales")                   # lange, schmale Schnauze
    m.cube("head", (-2.4, -4, -23.5), (4.8, 1.5, 10), "scales")                   # Nasenrücken
    m.cube("head", (-3, -3.3, -25.3), (2, 1.2, 1.2), "nostril")
    m.cube("head", (1, -3.3, -25.3), (2, 1.2, 1.2), "nostril")
    m.cube("head", (-3.2, 3.0, -24.6), (6.4, 1.2, 11.5), "teeth")
    m.cube("head", (-3.2, 2.9, -24.5), (6.4, 0.2, 12.5), "mouth")
    m.cube("head", (4.6, -2.4, -11.5), (1, 1.8, 3.4), "eye")
    m.cube("head", (-5.6, -2.4, -11.5), (1, 1.8, 3.4), "eye")
    m.cube("head", (4.6, -4.8, -11), (1.4, 1.2, 5), "scales")                     # Brauenwülste
    m.cube("head", (-6.0, -4.8, -11), (1.4, 1.2, 5), "scales")
    # Wangenfächer aus Eis
    for sx in (1, -1):
        m.cube("head", (5.0 if sx > 0 else -6.4, -1.5, -8), (1.4, 6, 7), "crystal")
    # Kristallkrone: fünf Zacken, die mittlere am höchsten, leicht nach hinten geneigt
    m.bone("crown", "head", pivot=(0, -4.5, -6), rotation=(-28, 0, 0))
    for x, h, w in [(-4.2, 7, 1.6), (-2.2, 10, 1.9), (0, 13, 2.2), (2.2, 10, 1.9), (4.2, 7, 1.6)]:
        m.cube("crown", (x - w / 2, -h, -w / 2), (w, h, w * 1.2), "crystal")
        m.cube("crown", (x - w / 4, -h - 2.2, -w / 4), (w / 2, 2.4, w / 2), "crystal")
    m.bone("jaw", "head", pivot=(0, 3.2, -10), rotation=(3, 0, 0))
    m.cube("jaw", (-3.4, 0, -14.5), (6.8, 3, 15), "scales")
    m.cube("jaw", (-2.8, -1.1, -14.2), (5.6, 1.1, 10), "teeth")
    m.cube("jaw", (-2.4, -0.2, -13.5), (4.8, 0.4, 12.5), "mouth")
    m.cube("jaw", (-0.7, 2.6, -8), (1.4, 2.2, 2.4), "crystal")                    # Kinnkristall
    # Hörner: zwei lange, glatte Hörner nach hinten
    for side, sx in (("l", 1), ("r", -1)):
        m.bone(f"horn_{side}1", "head", pivot=(3.0 * sx, -4.2, -4), rotation=(-18, 14 * sx, 0))
        m.cube(f"horn_{side}1", (-1.2, -1.2, 0), (2.4, 2.4, 9), "horn")
        m.bone(f"horn_{side}2", f"horn_{side}1", pivot=(0, 0, 9), rotation=(10, 4 * sx, 0))
        m.cube(f"horn_{side}2", (-0.8, -0.8, 0), (1.6, 1.6, 7), "horn")

    # ---------------------------------------------------------------- Schwanz (zehn Glieder) mit Eisfächer
    tail = chain(m, "tail", "body", (0, -3, 17),
                 [(10, 9.5, 9), (9, 8.5, 9), (8, 7.5, 8.5), (7, 6.6, 8.5), (6.2, 5.8, 8), (5.4, 5.0, 8), (4.6, 4.4, 7.5),
                  (3.8, 3.6, 7.5), (3.2, 3.0, 7), (2.6, 2.4, 6.5)],
                 [-12, -8, -5, -2, 0, 2, 3, 3, 3, 2], direction=1, spikes=None, belly="plates")
    for i, name in enumerate(tail[:7]):
        h = 4.2 - i * 0.45
        m.cube(name, (-0.7, -tail_half_height(i) - h, 2.0), (1.4, h + 0.5, 2.6), "crystal")
    for k, (yaw, length) in enumerate([(-22, 11), (0, 14), (22, 11)]):
        m.bone(f"tailfan_{k}", tail[-1], pivot=(0, 0, 6.0), rotation=(0, yaw, 0))
        m.cube(f"tailfan_{k}", (-0.4, -2.5, 0), (0.8, 5, length), "tailfan")

    # ---------------------------------------------------------------- Beine (schlank)
    for side, sx in (("l", 1), ("r", -1)):
        leg(m, side, sx, "front", "body", (8.0, 3, -12),
            [(6.5, 13, 6.5, 6), (5, 12.5, 5, -12), (6.5, 2.6, 9, 6)], toes=3, toe_len=3.2)
        leg(m, side, sx, "hind", "body", (8.5, 2, 11),
            [(8, 14.5, 9.5, -10), (5.6, 13, 5.6, 30), (7, 2.8, 10, -20)], toes=3, toe_len=3.6)

    # ---------------------------------------------------------------- Flügel (lang und schmal)
    for side, sx in (("l", 1), ("r", -1)):
        wing(m, side, sx, "body", (8.5, -9, -10), arm=26, fore=31,
             fingers=[(54, 8), (49, 30), (43, 52), (35, 74)], arm_th=3.6, inner_chord=28, fore_chord=30, sag=0.12)
    return m


def tail_half_height(i: int) -> float:
    sizes = [9.5, 8.5, 7.5, 6.6, 5.8, 5.0, 4.4, 3.6, 3.0, 2.4]
    return sizes[i] / 2


# =============================================================================================
# Bemalung
# =============================================================================================

def _feathered_membrane(fore: bool | None):
    """Flughaut wie gefrorene Federn: zur Hinterkante hin Reihen schuppiger Eisfedern, leuchtende Frostadern."""
    base = arm_membrane("membrane", "vein", fore=bool(fore), glow_veins=True) if fore is not None else None

    def feather(ctx: Ctx, col):
        lx, ly, lz = ctx.local
        # Federreihen: versetzte Bögen entlang der Hinterkante
        row = math.floor(lz / 3.2)
        shift = 1.6 if row % 2 else 0.0
        fx = ((lx + shift) % 3.2) / 3.2
        fz = (lz % 3.2) / 3.2
        if fz > 0.65 + 0.25 * abs(fx - 0.5):
            col = mix(col, pick(ctx.pal["feather"], 0.35), 0.6)
        elif fz < 0.2:
            col = mix(col, pick(ctx.pal["feather"], 0.85), 0.35)
        return col

    def paint(ctx: Ctx):
        col = base(ctx) if base is not None else None
        if col is None:
            return None
        return feather(ctx, col)

    return paint, feather


def _finger_feathers():
    from .boss_common import finger_membrane
    inner = finger_membrane("membrane", "vein", tatter=0.0, glow_veins=True, seed=9)
    _, feather = _feathered_membrane(None)

    def paint(ctx: Ctx):
        col = inner(ctx)
        if col is None:
            return None
        lx, ly, lz = ctx.local
        # nur im äußeren Teil federartig, nahe der Hand glatte Haut
        r = math.hypot(lx, lz)
        if r > 12:
            col = feather(ctx, col)
        return col

    return paint


def _tail_fan(ctx: Ctx):
    lx, ly, lz = ctx.local
    oy, oz = ctx.cube.origin[1], ctx.cube.origin[2]
    h, d = ctx.cube.size[1], ctx.cube.size[2]
    v = (lz - oz) / max(d, 1e-3)                # 0 am Ansatz, 1 an der Spitze
    u = (ly - oy) / max(h, 1e-3) * 2 - 1        # -1 oben, 1 unten
    if abs(u) > (1.0 - v) * 1.05:               # Klinge läuft spitz zu
        return None
    col = pick(ctx.pal["crystal"], 0.55 + 0.35 * v + 0.1 * (1 - abs(u)))
    if abs(u) < 0.15:
        col = pick(ctx.pal["crack"], 0.85)
    ctx.glow = mix(col, pick(ctx.pal["crystal"], 0.95), 0.3)
    return col


def materials(variant: str) -> dict:
    inner, _ = _feathered_membrane(False)
    fore, _ = _feathered_membrane(True)
    return {
        "scales": cracked_scales(cell=3.2, seed=47, crack_width=0.011, heat=0.25, branch=0.5, freq=0.07),
        "plates": plates_painter("belly"),
        "chest": plates_painter("belly", glow_from=2.0),
        "crystal": crystal_painter("crystal", glow=True, facets=2.5),
        "horn": crystal_painter("horn", glow=False, facets=3.5),
        "spike": crystal_painter("spike", glow=True),
        "claw": flat_painter("claw", 0.8),
        "teeth": flat_painter("claw", 0.95, 0.04),
        "eye": glowing_eye("eye"),
        "nostril": glow_painter("crack", 0.7),
        "mouth": mouth_painter(),
        "membrane_inner": inner,
        "membrane_fore": fore,
        "membrane_outer": _finger_feathers(),
        "tailfan": _tail_fan,
        "none": transparent_painter(),
    }
