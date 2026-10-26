"""
Glaciara, die Frostkönigin – der Eisdrachen-Boss.

Schlank und elegant: langer Schwanenhals (acht Glieder), langer, dünner Schwanz mit einem Fächer aus Eisklingen,
schmaler Kopf mit einer Krone aus Eiskristallen, langen glatten Hörnern und Wangenfächern aus Eis, Kristallgruppen
entlang des Rückens, lange, schmale Flügel mit federartigen Eisschuppen an der Hinterkante. Rücken stahlblau, Flanken
und Bauch frostweiß (so hebt sie sich auch vom Schnee ab), Augen und Kristalle leuchten eisblau.
Grundgröße ca. 11,5 Blöcke, im Spiel ×2,1.
"""
from __future__ import annotations

import math

from ..geometry import Model
from ..paint import Ctx, mix, ramp, transparent_painter
from .boss_style import (arm_web, chain2, crystal2, eye, finger_web, fold_pose, horn_back, keratin, leg2, mouth,
                         pick_smooth, plates, rbox, scales3, seam_plates, solid, spike, teeth, wing2)

PALETTES = {
    "glaciara": {
        "back": ramp("#3a6290", "#eef8ff"), "belly": ramp("#a4c6e2", "#ffffff"),
        "glow": ramp("#1a7cd0", "#dcf8ff"), "membrane": ramp("#2f5f8e", "#cfeaff"),
        "vein": ramp("#5aa0d8", "#effbff"), "horn": ramp("#8cb6dc", "#ffffff"),
        "spike": ramp("#4e88c0", "#f4ffff"), "claw": ramp("#26364a", "#e4eef6"),
        "teeth": ramp("#9ab0c4", "#ffffff"), "eye": ramp("#1aa0ff", "#f0ffff"),
        "mouth": ramp("#141c30", "#4a68a0"), "crystal": ramp("#1f7ed0", "#f0ffff"),
        "feather": ramp("#8cc0e8", "#ffffff"),
    },
}

TEX = (512, 384)
FINGERS = [(54, 8), (49, 30), (43, 52), (35, 74)]
FOLD_POSE = fold_pose(FINGERS)
TAIL = [(10, 9.5, 9), (9, 8.5, 9), (8, 7.5, 8.5), (7, 6.6, 8.5), (6.2, 5.8, 8), (5.4, 5.0, 8), (4.6, 4.4, 7.5),
        (3.8, 3.6, 7.5), (3.2, 3.0, 7), (2.6, 2.4, 6.5)]


def crystal_cluster(m: Model, parent: str, name: str, pos, height: float, base: float = 2.4, lean: float = -22.0,
                    side: float = 0.55) -> None:
    """Kristallgruppe: ein großer Kristall, daneben zwei kleinere, die schräg zur Seite wachsen."""
    spike(m, parent, name, pos, height, base, lean=lean, mat="crystal", steps=3)
    if side > 0:
        x, y, z = pos
        for k, sx in enumerate((1, -1)):
            spike(m, parent, f"{name}_s{k}", (x + 0.9 * sx, y + 0.3, z + 0.8 * (1 if k else -1)), height * side,
                  base * 0.6, lean=lean - 8, roll=24 * sx, mat="crystal", steps=2)


def build() -> Model:
    m = Model("glaciara", *TEX)
    m.tex_res = 2

    # ---------------------------------------------------------------- Rumpf (schmal, hoch getragen)
    m.bone("body", pivot=(0, -10, 0))
    rbox(m, "body", (-8.5, -9, -18), (17, 17, 36), "scales", bevel=3.8, axis=2)
    rbox(m, "body", (-9.5, -10, -19), (19, 19, 14), "scales", bevel=4.2, axis=2)          # Brustkorb
    rbox(m, "body", (-9, -9.5, 6), (18, 17, 11), "scales", bevel=3.8, axis=2)             # Hüften
    rbox(m, "body", (-4.5, -12, -16), (9, 3.5, 30), "scales", bevel=1.3, axis=2)          # Rückgrat
    for sx in (1, -1):
        rbox(m, "body", (6.5 if sx > 0 else -11, -11.5, -14), (4.5, 5.5, 9), "scales", bevel=1.6, axis=2)
    rbox(m, "body", (-7, 8, -18), (14, 2, 15), "chest", bevel=1.0, axis=2)                # Frostsack
    m.cube("body", (-6.5, 6.5, -4), (13, 2, 20), "plates")
    for i, (z, h) in enumerate(zip((-14, -8, -2, 4, 10), (6, 8, 9, 8, 6.5)), start=1):
        crystal_cluster(m, "body", f"back_crystal{i}", (0, -11.6, z), h, 2.6)

    # ---------------------------------------------------------------- Hals (acht Glieder, Schwanenhals)
    neck = chain2(m, "neck", "body", (0, -6, -17),
                  [(11, 11, 6.5), (10, 10, 6.2), (9.2, 9.2, 6), (8.5, 8.5, 6), (8, 8, 5.8), (7.5, 7.5, 5.6), (7, 7, 5.5),
                   (6.6, 6.6, 5.4)],
                  [-34, -16, -8, -2, 4, 9, 12, 9], direction=-1, bevel=0.3, mat="hide")
    for i, name in enumerate(neck):
        if i % 2 == 0:
            h = 3.6 - i * 0.28
            spike(m, name, f"{name}_crystal", (0, -neck_half(i) + 0.4, -3.0), h, 1.7, lean=-35, mat="crystal")

    # ---------------------------------------------------------------- Kopf (lang und schmal)
    m.bone("head", neck[-1], pivot=(0, 0, -5.5), rotation=(32, 0, 0))
    rbox(m, "head", (-5, -4.6, -11.5), (10, 8.6, 12.5), "hide", bevel=2.2, axis=2)         # Schädel
    rbox(m, "head", (-4, -4.4, -14), (8, 2.4, 5), "hide", bevel=1.0, axis=2)               # Stirn
    rbox(m, "head", (-3.8, -3.2, -18), (7.6, 6.8, 7.5), "hide", bevel=1.6, axis=2)        # Schnauze hinten
    rbox(m, "head", (-3.3, -2.8, -25), (6.6, 6.4, 7.5), "hide", bevel=1.4, axis=2)        # Schnauze vorn
    rbox(m, "head", (-1.8, -3.9, -23.5), (3.6, 1.4, 12), "hide", bevel=0.5, axis=2)       # Nasenrücken
    rbox(m, "head", (-2.6, -2.4, -25.8), (5.2, 3.4, 1.0), "hide", bevel=0.7, axis=0)      # Nasenspitze
    for sx in (1, -1):
        s = "l" if sx > 0 else "r"
        m.cube("head", ((0.9 if sx > 0 else -2.0), -2.1, -26.0), (1.1, 0.7, 0.5), "nostril")
        m.cube("head", ((3.6 if sx > 0 else -5.15), -2.8, -11.9), (1.55, 1.6, 3.4), "eye")
        m.bone(f"brow_{s}", "head", pivot=(3.4 * sx, -4.0, -11.6), rotation=(5, 0, -10 * sx))
        m.cube(f"brow_{s}", (-1.9, -1.1, -0.5), (3.8, 2.0, 7), "hide")
        teeth(m, "head", 3.0 * sx, 3.6, -24.5, -13, 7, height=1.1, size=0.75)
        m.cube("head", (2.8 * sx - 0.45, 3.6, -23.2), (0.9, 2.2, 0.9), "teeth")
        # Wangenfächer aus Eis
        for k, (y, z, h) in enumerate(((-0.5, -6.0, 4.6), (1.5, -4.5, 3.8), (3.2, -3.0, 2.8)), start=1):
            spike(m, "head", f"frill_{s}{k}", (4.6 * sx, y, z), h, 1.3, lean=-100, yaw=35 * sx, mat="crystal", steps=2)
        horn_back(m, "head", f"horn_{s}", (3.0 * sx, -4.2, -3.5),
                  [(6.5, 2.6, (18, 16 * sx, 0)), (6.0, 2.0, (-4, 4 * sx, 0)), (5.5, 1.5, (-4, 2 * sx, 0)),
                   (4.5, 1.0, (6, 0, 0))])
    for x in (-1.5, -0.5, 0.5, 1.5):
        m.cube("head", (x - 0.35, 3.6, -25.4), (0.7, 0.9, 0.7), "teeth")
    m.cube("head", (-2.7, 3.62, -24.3), (5.4, 0.3, 11.5), "mouth")                       # Gaumen
    # Kristallkrone: fünf Zacken, die mittlere am höchsten, fächerförmig nach hinten geneigt
    m.bone("crown", "head", pivot=(0, -4.4, -6), rotation=(-28, 0, 0))
    for k, (x, h, w) in enumerate([(-4.0, 7, 1.7), (-2.1, 10, 2.0), (0, 13, 2.4), (2.1, 10, 2.0), (4.0, 7, 1.7)]):
        spike(m, "crown", f"crown_{k}", (x, 0.3, 0), h, w, lean=0, roll=-x * 3.5, mat="crystal", steps=3)

    # ---------------------------------------------------------------- Unterkiefer
    m.bone("jaw", "head", pivot=(0, 3.6, -10), rotation=(3, 0, 0))
    rbox(m, "jaw", (-3.2, 0, -14.8), (6.4, 2.8, 15), "hide", bevel=1.0, axis=2)
    rbox(m, "jaw", (-4, -0.4, -3), (8, 3.6, 4.5), "hide", bevel=1.1, axis=2)
    for sx in (1, -1):
        teeth(m, "jaw", 2.7 * sx, 0.1, -14.4, -3.5, 7, height=1.0, down=False, size=0.7)
    m.cube("jaw", (-2.8, -0.15, -14.6), (5.6, 0.3, 14.5), "mouth")
    m.cube("jaw", (-1.9, -0.45, -13.5), (3.8, 0.5, 11), "mouth")                          # Zunge
    m.cube("jaw", (-2.4, 2.6, -10), (4.8, 0.7, 10), "plates")                             # Kehle
    spike(m, "jaw", "chin_crystal", (0, 2.6, -8), 2.6, 1.5, lean=-150, mat="crystal", steps=2)

    # ---------------------------------------------------------------- Schwanz (zehn Glieder) mit Eisfächer
    tail = chain2(m, "tail", "body", (0, -3, 17), TAIL, [-12, -8, -5, -2, 0, 2, 3, 3, 3, 2], direction=1, bevel=0.3)
    for i, name in enumerate(tail[:7]):
        h = 4.4 - i * 0.45
        spike(m, name, f"{name}_crystal", (0, -TAIL[i][1] / 2 + 0.4, TAIL[i][2] * 0.45), h, max(1.2, 2.0 - i * 0.12),
              lean=-38, mat="crystal")
    for k, (yaw, length) in enumerate([(-22, 11), (0, 14), (22, 11)]):
        m.bone(f"tailfan_{k}", tail[-1], pivot=(0, 0, 6.0), rotation=(0, yaw, 0))
        m.cube(f"tailfan_{k}", (-0.4, -2.5, 0), (0.8, 5, length), "tailfan")

    # ---------------------------------------------------------------- Beine (schlank)
    for side, sx in (("l", 1), ("r", -1)):
        leg2(m, side, sx, "front", "body", (8.0, 3, -12),
             [(6.5, 13, 6.5, 6), (5, 12.5, 5, -12), (6.5, 2.6, 9, 6)], toe_count=3, toe_len=3.2, claw=2.0, mat="limb")
        leg2(m, side, sx, "hind", "body", (8.5, 2, 11),
             [(8, 14.5, 9.5, -10), (5.6, 13, 5.6, 30), (7, 2.8, 10, -20)], toe_count=3, toe_len=3.6, claw=2.2, mat="limb")

    # ---------------------------------------------------------------- Flügel (lang und schmal)
    for side, sx in (("l", 1), ("r", -1)):
        wing2(m, side, sx, "body", (8.5, -9, -10), arm=26, fore=31, fingers=FINGERS, arm_th=3.6, inner_chord=28,
              fore_chord=30, sag=0.12, finger_th=2.0, mats={"bone": "limb"})
    return m


def neck_half(i: int) -> float:
    return [11, 10, 9.2, 8.5, 8, 7.5, 7, 6.6][i] / 2


# =============================================================================================
# Bemalung
# =============================================================================================

def _feather(ctx: Ctx, col):
    """Federreihen aus Eisschuppen: versetzte Bögen, helle Federkanten."""
    lx, ly, lz = ctx.local
    row = math.floor(lz / 3.2)
    shift = 1.6 if row % 2 else 0.0
    fx = ((lx + shift) % 3.2) / 3.2
    fz = (lz % 3.2) / 3.2
    if fz > 0.66 + 0.25 * abs(fx - 0.5):
        return mix(col, pick_smooth(ctx.pal["feather"], 0.3), 0.55)
    if fz < 0.18:
        return mix(col, pick_smooth(ctx.pal["feather"], 0.9), 0.35)
    return col


def _feathered(base, outer: bool = False):
    def paint(ctx: Ctx):
        col = base(ctx)
        if col is None:
            return None
        if outer and math.hypot(ctx.local[0], ctx.local[2]) <= 22:
            return col                                  # nur am äußeren Rand Eisfedern, innen glatte Haut
        return _feather(ctx, col)

    return paint


def _tail_fan(ctx: Ctx):
    lx, ly, lz = ctx.local
    oy, oz = ctx.cube.origin[1], ctx.cube.origin[2]
    h, d = ctx.cube.size[1], ctx.cube.size[2]
    v = (lz - oz) / max(d, 1e-3)                # 0 am Ansatz, 1 an der Spitze
    u = (ly - oy) / max(h, 1e-3) * 2 - 1        # -1 oben, 1 unten
    if abs(u) > (1.0 - v) * 1.05:               # Klinge läuft spitz zu
        return None
    col = pick_smooth(ctx.pal["crystal"], 0.5 + 0.35 * v + 0.1 * (1 - abs(u)))
    if abs(u) < 0.14:
        col = pick_smooth(ctx.pal["glow"], 0.85)
    ctx.glow = mix(col, pick_smooth(ctx.pal["crystal"], 0.9), 0.3)
    return col


def materials(variant: str) -> dict:
    return {
        # Rücken dunkler (stahlblau), Flanken heller, Bauch frostweiß
        "scales": scales3(cell=2.6, seed=47, belly_from=0.66, top_light=-0.12, var=0.1, bias=0.12, line=0.1, line_depth=0.1),
        "hide": scales3(cell=2.4, seed=49, belly_from=0.7, top_light=-0.1, var=0.08, bias=0.12, line=0.1, line_depth=0.1),
        "limb": scales3(cell=2.2, seed=51, belly_from=2.0, top_light=-0.04, foot_dark=0.1, bias=0.1, line=0.1, line_depth=0.1),
        "plates": plates("belly"),
        "chest": seam_plates("belly", "glow", tint=0.12),
        "crystal": crystal2("crystal", glow=0.55),
        "horn": keratin("horn", base=0.4, span=0.58),
        "spike": crystal2("spike", glow=0.4),
        "claw": keratin("claw", base=0.35, span=0.6),
        "teeth": solid("teeth", 0.85, noise=0.06),
        "eye": eye("eye"),
        "nostril": solid("back", 0.04, noise=0.0, ao=0.0),
        "mouth": mouth("mouth"),
        "membrane_inner": arm_web("membrane", "vein"),
        "membrane_fore": arm_web("membrane", "vein", fore=True),
        "membrane_outer": _feathered(finger_web("membrane", "vein", seed=9, edge_key="feather"), outer=True),
        "tailfan": _tail_fan,
        "none": transparent_painter(),
    }
