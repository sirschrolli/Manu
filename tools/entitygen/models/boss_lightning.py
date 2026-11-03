"""
Voltarion, der Sturmbringer – der Blitzdrachen-Boss.

Ein Wyvern: nur zwei kräftige Hinterbeine, die Flügel sind zugleich die Vorderglieder (mit Daumenkrallen am
Handgelenk). Schlanker, aufrecht getragener Leib, langer Hals mit einem Kamm aus leuchtenden Blitzflossen, ein
schmaler, kantiger Kopf mit einer Krone aus vier nach hinten geschwungenen Hörnern, ein peitschenlanger Schwanz
(zwölf Glieder) mit einer gezackten Klinge wie ein Blitz. Riesige Flügel mit drei langen Fingern. Schuppen
nachtblau, an den Flanken zieht sich je ein gleißender Zickzack-Streifen wie ein Blitz vom Hals bis zur
Schwanzspitze. Grundgröße ca. 12 Blöcke, im Spiel ×2,1.
"""
from __future__ import annotations

import math

from ..geometry import Model
from ..paint import Ctx, ramp, transparent_painter
from .boss_style import (arm_web, chain2, eye, facing_down, finger_web, horn_back, keratin, leg2, mouth, pick_smooth,
                         plates, rbox, scales3, seam_plates, solid, spike, teeth, wing2)

PALETTES = {
    "voltarion": {
        "back": ramp("#121a2e", "#5e7298"), "belly": ramp("#34425e", "#c4d2e6"), "glow": ramp("#1a8ac8", "#f0fcff"),
        "spark": ramp("#c8a010", "#fffce0"), "membrane": ramp("#10182e", "#46608e"), "vein": ramp("#1e5490", "#8ec8f0"),
        "horn": ramp("#1c2230", "#b4bcc8"), "spike": ramp("#1a2234", "#9aaac8"), "claw": ramp("#16181e", "#d8dce8"),
        "teeth": ramp("#8890a0", "#f4f8ff"), "eye": ramp("#4ac8ff", "#ffffff"), "mouth": ramp("#10162a", "#3c64a8"),
        "fin": ramp("#1a6ab0", "#e0faff"),
    },
}

TEX = (512, 384)
FINGERS = [(62, 8), (56, 34), (47, 62)]
NECK = [(11, 11, 7), (10, 10, 6.8), (9.2, 9.2, 6.5), (8.6, 8.6, 6.2), (8, 8, 6), (7.4, 7.4, 5.8)]
TAIL = [(10, 9, 9), (9, 8, 9), (8, 7.2, 8.5), (7, 6.4, 8.5), (6.2, 5.6, 8), (5.4, 5, 8), (4.8, 4.4, 7.5), (4.2, 3.8, 7.5),
        (3.6, 3.2, 7), (3, 2.8, 7), (2.6, 2.4, 6.5), (2.2, 2, 6)]


def _crawl_pose() -> dict:
    """Haltung am Boden wie im Spiel (VoltarionModel: Kriechhaltung auf den Handgelenken) – für die Vorschau."""
    d = math.degrees
    arm, fore, hand = (-1.6519, -0.8654, 0.4519), (-2.5316, -1.2171, 2.5612), (0.3782, 0.2775, -2.6978)
    pose = {}
    for side, sx in (("l", 1), ("r", -1)):
        pose[f"wing_{side}"] = (d(arm[0]), d(arm[1]) * sx, d(arm[2]) * sx)
        pose[f"wing_{side}_fore"] = (d(fore[0]), d(fore[1]) * sx, d(fore[2]) * sx)
        pose[f"wing_{side}_hand"] = (d(hand[0]), d(hand[1]) * sx, d(hand[2]) * sx)
        pose[f"wing_{side}_web"] = (0, 0, 0, 1, 1, 0.15)
        pose[f"wing_{side}_fore_web"] = (0, 0, 0, 1, 1, 0.15)
        for i, (_, ang) in enumerate(FINGERS, start=1):
            pose[f"wing_{side}_f{i}"] = (0, 0.92 * ang * sx, 0, 0.6, 1, 0.2)
    return pose


FOLD_POSE = _crawl_pose()


def build() -> Model:
    m = Model("voltarion", *TEX)
    m.tex_res = 2

    # ---------------------------------------------------------------- Rumpf (schlank, aufrecht)
    m.bone("body", pivot=(0, -10, 0))
    rbox(m, "body", (-9.5, -10, -18), (19, 19, 34), "scales", bevel=4.0, axis=2)
    rbox(m, "body", (-10.5, -11, -19), (21, 21, 14), "scales", bevel=4.5, axis=2)         # Brustkorb
    rbox(m, "body", (-10, -10, 3), (20, 18, 12), "scales", bevel=4.0, axis=2)             # Hüften
    rbox(m, "body", (-4.5, -13, -15), (9, 3.5, 27), "scales", bevel=1.3, axis=2)          # Rückgrat
    for sx in (1, -1):
        rbox(m, "body", (8 if sx > 0 else -13, -11, -17), (5, 9, 10), "scales", bevel=2.0, axis=2)   # Flügelansätze
    rbox(m, "body", (-8, 8.5, -18), (16, 2.2, 17), "chest", bevel=1.2, axis=2)             # Blitzsack
    m.cube("body", (-7, 7.2, -1), (14, 2, 15), "plates")
    for i, (z, h) in enumerate(zip((-13, -7, -1, 5, 11), (6, 7.5, 8, 6.5, 5))):
        m.cube("body", (-0.4, -12.6 - h, z), (0.8, h + 0.5, 4.5), "fin")

    # ---------------------------------------------------------------- Hals (sechs Glieder) mit Flossenkamm
    neck = chain2(m, "neck", "body", (0, -6, -17), NECK, [-30, -14, -4, 4, 10, 10], direction=-1, bevel=0.3, mat="neck")
    for i, (name, (w, h, length)) in enumerate(zip(neck, NECK)):
        fh = 4.5 - i * 0.45
        m.cube(name, (-0.35, -h / 2 - fh + 0.4, -length + 0.5), (0.7, fh + 0.5, length - 1), "fin")

    # ---------------------------------------------------------------- Kopf (schmal und kantig)
    m.bone("head", neck[-1], pivot=(0, 0, -5.8), rotation=(30, 0, 0))
    rbox(m, "head", (-5.5, -5, -12.5), (11, 9.4, 13), "hide", bevel=2.2, axis=2)          # Schädel
    rbox(m, "head", (-4.4, -4.8, -15), (8.8, 2.6, 5), "hide", bevel=1.0, axis=2)          # Stirn
    rbox(m, "head", (-4.2, -3.6, -19.5), (8.4, 7.2, 8), "hide", bevel=1.7, axis=2)        # Schnauze hinten
    rbox(m, "head", (-3.6, -3.1, -26.5), (7.2, 6.7, 7.5), "hide", bevel=1.4, axis=2)      # Schnauze vorn
    rbox(m, "head", (-2.0, -4.4, -25), (4.0, 1.6, 12.5), "hide", bevel=0.6, axis=2)       # Nasenrücken
    rbox(m, "head", (-2.8, -2.6, -27.3), (5.6, 3.6, 1.0), "hide", bevel=0.7, axis=0)      # Nasenspitze
    for sx in (1, -1):
        s = "l" if sx > 0 else "r"
        m.cube("head", ((1.0 if sx > 0 else -2.2), -2.3, -27.5), (1.2, 0.8, 0.5), "nostril")
        m.cube("head", ((3.9 if sx > 0 else -5.6), -3.1, -12.9), (1.7, 1.7, 3.6), "eye")
        m.bone(f"brow_{s}", "head", pivot=(3.8 * sx, -4.4, -12.6), rotation=(8, 0, -16 * sx))
        m.cube(f"brow_{s}", (-2.1, -1.2, -0.6), (4.2, 2.2, 8), "hide")
        teeth(m, "head", 3.3 * sx, 3.6, -26, -14, 7, height=1.3, size=0.8)
        m.cube("head", (3.0 * sx - 0.5, 3.6, -24.6), (1.0, 2.5, 1.0), "teeth")
        # Krone aus vier nach hinten geschwungenen Hörnern
        horn_back(m, "head", f"horn_{s}0", (2.8 * sx, -4.8, -6),
                  [(6.5, 2.7, (24, 12 * sx, 0)), (6.0, 2.1, (-8, 4 * sx, 0)), (5.0, 1.5, (-10, 2 * sx, 0)),
                   (4.0, 1.0, (-6, 0, 0))], mat="horn")
        horn_back(m, "head", f"horn_{s}1", (4.6 * sx, -3.2, -4),
                  [(5.0, 2.1, (8, 32 * sx, 0)), (4.5, 1.6, (-8, 6 * sx, 0)), (3.5, 1.1, (-8, 2 * sx, 0))], mat="horn")
    for x in (-1.6, -0.55, 0.55, 1.6):
        m.cube("head", (x - 0.35, 3.6, -26.9), (0.7, 1.0, 0.7), "teeth")
    m.cube("head", (-3.0, 3.62, -25.8), (6.0, 0.3, 12), "mouth")                          # Gaumen

    # ---------------------------------------------------------------- Unterkiefer
    m.bone("jaw", "head", pivot=(0, 3.6, -11), rotation=(3, 0, 0))
    rbox(m, "jaw", (-3.5, 0, -15.6), (7, 3, 16), "hide", bevel=1.0, axis=2)
    rbox(m, "jaw", (-4.4, -0.4, -3), (8.8, 3.8, 4.5), "hide", bevel=1.2, axis=2)
    for sx in (1, -1):
        teeth(m, "jaw", 3.0 * sx, 0.1, -15.2, -3.5, 7, height=1.1, down=False, size=0.75)
    m.cube("jaw", (-3.1, -0.15, -15.4), (6.2, 0.3, 15), "mouth")
    m.cube("jaw", (-2.1, -0.45, -14), (4.2, 0.5, 11.5), "mouth")
    m.cube("jaw", (-2.6, 2.8, -11), (5.2, 0.7, 11), "plates")
    spike(m, "jaw", "beard1", (0, 2.9, -7), 2.4, 1.4, lean=-150, mat="horn")
    for sx in (1, -1):
        spike(m, "jaw", f"beard_{'l' if sx > 0 else 'r'}", (2.3 * sx, 2.8, -4), 2.0, 1.2, lean=-150, yaw=-10 * sx, mat="horn")

    # ---------------------------------------------------------------- Peitschenschwanz (zwölf Glieder) mit Blitzklinge
    tail = chain2(m, "tail", "body", (0, -3, 15), TAIL, [-6, -4, -2, 0, 1, 2, 2, 2, 2, 2, 1, 1], direction=1, bevel=0.3)
    for i, (name, (w, h, length)) in enumerate(zip(tail[:9], TAIL)):
        fh = 3.6 - i * 0.35
        m.cube(name, (-0.3, -h / 2 - fh + 0.4, 1.5), (0.6, fh + 0.5, length - 3), "fin")
    m.cube(tail[-1], (-5.5, -0.5, 4.5), (11, 1, 13), "blade")                             # Blitzklinge (flach)

    # ---------------------------------------------------------------- Hinterbeine (kräftig, wie bei einem Raubsaurier)
    for side, sx in (("l", 1), ("r", -1)):
        leg2(m, side, sx, "hind", "body", (9.5, 3, 8),
             [(9, 16, 11, -18), (6, 15, 6, 42), (8, 3, 12, -24)], toe_count=3, toe_len=4.2, claw=2.6, mat="limb")

    # ---------------------------------------------------------------- Riesige Flügel (zugleich Vorderglieder)
    for side, sx in (("l", 1), ("r", -1)):
        wing2(m, side, sx, "body", (10.5, -7, -12), arm=30, fore=36, fingers=FINGERS, arm_th=4.6, inner_chord=32,
              fore_chord=34, sag=0.18, web_bones=True, finger_th=2.4, mats={"bone": "limb", "claw": "claw"})
    return m


# =============================================================================================
# Bemalung
# =============================================================================================

def _bolt_stripe(ctx: Ctx) -> float:
    """Zickzack-Streifen an den Flanken (wie ein Blitz): 0..1 Stärke, 0 = kein Streifen."""
    if ctx.face not in ("west", "east"):
        return 0.0
    z = ctx.world[2]
    period = 7.0
    u = (z / period) % 1.0
    tri = 1.0 - abs(u * 2.0 - 1.0)            # 0..1..0
    centre = 0.42 + 0.16 * (tri - 0.5)
    d = abs(facing_down(ctx) - centre)
    width = 0.045
    return max(0.0, 1.0 - d / width) if d < width else 0.0


def _scales_with_stripe(base):
    def paint(ctx: Ctx):
        g = _bolt_stripe(ctx)
        if g > 0:
            col = pick_smooth(ctx.pal["glow"], 0.45 + 0.5 * g)
            ctx.glow = col
            return col
        return base(ctx)

    return paint


def _fin(ctx: Ctx):
    """Blitzflossen: dünne, gezackte Flossen, die zur Kante hin gleißend leuchten."""
    oy, oz = ctx.cube.origin[1], ctx.cube.origin[2]
    h, d = ctx.cube.size[1], ctx.cube.size[2]
    rel = (ctx.local[1] - oy) / max(h, 1e-3)            # 0 oben, 1 unten
    along = (ctx.local[2] - oz) / max(d, 1e-3)
    saw = (along * 3.0) % 1.0
    if rel < 0.35 * saw:
        return None
    col = pick_smooth(ctx.pal["fin"], 0.88 - 0.62 * rel)
    if rel < 0.45:
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
    col = pick_smooth(ctx.pal["spark"], 0.6 + 0.35 * (1 - abs(u - zig) / half))
    ctx.glow = col
    return col


def _horn(ctx: Ctx):
    """Hörner: dunkel, die Spitzen glühen wie geladen."""
    base = keratin("horn", base=0.2, span=0.5)
    col = base(ctx)
    tag = ctx.cube.tag
    if tag.startswith("hn:"):
        _, i, n, length, _d = tag.split(":")
        prog = (int(i) - 1 + max(0.0, min(1.0, ctx.local[2] / float(length)))) / int(n)
        if prog > 0.72:
            col = pick_smooth(ctx.pal["glow"], 0.5 + 0.5 * (prog - 0.72) / 0.28)
            ctx.glow = col
    return col


def materials(variant: str) -> dict:
    return {
        "scales": _scales_with_stripe(scales3(cell=2.5, seed=67, belly_from=0.7, top_light=0.12, var=0.12)),
        "neck": _scales_with_stripe(scales3(cell=2.1, seed=69, belly_from=0.72, top_light=0.12, var=0.1)),
        "hide": scales3(cell=2.1, seed=73, belly_from=0.72, top_light=0.12, var=0.1),
        "limb": scales3(cell=2.2, seed=71, belly_from=2.0, top_light=0.08, foot_dark=0.12),
        "plates": plates("belly"),
        "chest": seam_plates("belly", "glow", tint=0.1),
        "fin": _fin,
        "blade": _blade,
        "horn": _horn,
        "spike": keratin("spike"),
        "claw": keratin("claw", base=0.35, span=0.6),
        "teeth": solid("teeth", 0.85, noise=0.06),
        "eye": eye("eye"),
        "nostril": solid("back", 0.02, noise=0.0, ao=0.0),
        "mouth": mouth("mouth"),
        "membrane_inner": arm_web("membrane", "vein"),
        "membrane_fore": arm_web("membrane", "vein", fore=True),
        "membrane_outer": finger_web("membrane", "vein", tatter=0.12, seed=11, edge_key="back"),
        "none": transparent_painter(),
    }
