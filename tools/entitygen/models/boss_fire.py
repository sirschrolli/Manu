"""
Pyrrhon, der Aschenfürst – der Feuerdrachen-Boss.

Klassischer, schwerer Drache: breite Brust mit glühendem Feuersack, vier kräftige Beine mit Krallen, große
Fledermausflügel mit vier Fingern, zwei mächtige nach hinten geschwungene Hörner (dazu kleinere Wangenhörner und
Kieferstacheln), ein Kamm spitzer Stacheln von der Stirn bis zur Schwanzklinge. Schuppen wie erkaltete Lava, nur
an wenigen Stellen ziehen sich glühende Adern hindurch. Grundgröße ca. 12 Blöcke, im Spiel ×2,1 (ca. 25 Blöcke).
"""
from __future__ import annotations

from ..geometry import Model
from ..paint import Ctx, ramp, transparent_painter
from .boss_style import (arm_web, chain2, eye, finger_web, fold_pose, horn_back, keratin, leg2, mix, mouth, pick_smooth,
                         plate_color, plates, rbox, scales3, solid, spike, teeth, wing2)

PALETTES = {
    "pyrrhon": {
        "back": ramp("#1c0b08", "#7e3626"), "belly": ramp("#5a2210", "#eeb070"),
        "glow": ramp("#b02800", "#fff3a8"), "membrane": ramp("#2a0b08", "#a4442a"),
        "vein": ramp("#4a1208", "#c8643a"), "horn": ramp("#2a1a12", "#e2c8a0"),
        "spike": ramp("#160d0a", "#a68468"), "claw": ramp("#241c18", "#efe4d2"),
        "teeth": ramp("#8a7a68", "#fbf4e6"), "eye": ramp("#ff6000", "#fff6c0"),
        "mouth": ramp("#3a0806", "#e85a1e"),
    },
}

TEX = (512, 512)
FINGERS = [(54, 6), (50, 30), (44, 54), (36, 78)]
FOLD_POSE = fold_pose(FINGERS)


def build() -> Model:
    m = Model("pyrrhon", *TEX)
    m.tex_res = 2

    # ---------------------------------------------------------------- Rumpf
    m.bone("body", pivot=(0, -8, 0))
    rbox(m, "body", (-12.5, -12, -21), (25, 20, 42), "scales", bevel=4.5, axis=2)        # Rumpf
    rbox(m, "body", (-14, -13, -23), (28, 22, 16), "scales", bevel=5.0, axis=2)          # Brustkorb
    rbox(m, "body", (-13.2, -12.5, 7), (26.4, 19.5, 13), "scales", bevel=4.5, axis=2)     # Hüften
    rbox(m, "body", (-6, -15, -20), (12, 4, 36), "scales", bevel=1.8, axis=2)              # Rückgrat
    for sx in (1, -1):                                                                     # Flügelansatz
        rbox(m, "body", (9 if sx > 0 else -15, -14.5, -17), (6, 7, 11), "scales", bevel=2.0, axis=2)
    rbox(m, "body", (-9.5, 8, -22), (19, 2.2, 15), "chest", bevel=1.2, axis=2)            # Feuersack
    m.cube("body", (-9, 6.8, -7), (18, 2, 27), "plates")                                   # Bauchplatten
    for i, (z, h) in enumerate(zip((-17, -9, -1, 7, 15), (7.5, 9.5, 10, 8.5, 6.5)), start=1):
        spike(m, "body", f"back_spike{i}", (0, -14.6, z), h, 3.6, lean=-30)

    # ---------------------------------------------------------------- Hals
    neck = chain2(m, "neck", "body", (0, -6, -21),
                  [(14.5, 14, 8), (13.5, 13, 7.5), (12.5, 12, 7), (11.5, 11, 6.5), (10.5, 10, 6.5), (10, 9.5, 6)],
                  [-30, -12, -2, 6, 9, 8], direction=-1, spikes=[4.6, 4.3, 4.0, 3.7, 3.4, 3.0], bevel=0.3, mat="hide")

    # ---------------------------------------------------------------- Kopf
    m.bone("head", neck[-1], pivot=(0, 0, -7), rotation=(30, 0, 0))
    rbox(m, "head", (-6.5, -6.2, -13), (13, 10.5, 14.5), "hide", bevel=2.6, axis=2)      # Schädel
    rbox(m, "head", (-5.2, -6.0, -16), (10.4, 3, 6), "hide", bevel=1.2, axis=2)           # Stirn
    rbox(m, "head", (-4.9, -4.4, -21), (9.8, 8.4, 9), "hide", bevel=2.0, axis=2)           # Schnauze hinten
    rbox(m, "head", (-4.4, -3.9, -28), (8.8, 7.9, 8), "hide", bevel=1.8, axis=2)           # Schnauze vorn
    rbox(m, "head", (-2.8, -5.4, -26.5), (5.6, 1.8, 13), "hide", bevel=0.7, axis=2)       # Nasenrücken
    rbox(m, "head", (-3.4, -3.5, -28.9), (6.8, 4.2, 1.2), "hide", bevel=0.9, axis=0)      # Nasenspitze
    for sx in (1, -1):
        m.cube("head", ((1.4 if sx > 0 else -2.8), -3.2, -29.1), (1.4, 0.9, 0.6), "nostril")
        # Brauenwulst (schräg: innen und vorn tiefer – grimmiger Blick)
        m.bone(f"brow_{'l' if sx > 0 else 'r'}", "head", pivot=(4.4 * sx, -5.4, -12.8), rotation=(6, 0, -10 * sx))
        m.cube(f"brow_{'l' if sx > 0 else 'r'}", (-2.4, -1.4, -0.6), (4.8, 2.6, 9), "hide")
        rbox(m, "head", ((5.0 if sx > 0 else -7.0), -0.6, -14), (2.0, 5.0, 8), "hide", bevel=0.7, axis=2)  # Wangen
        # Auge an der Kante des Schädels: von vorn und von der Seite zu sehen
        m.cube("head", ((4.9 if sx > 0 else -6.75), -3.9, -13.4), (1.85, 2.0, 3.8), "eye")
        # Zähne oben: Reihe, Fangzahn
        teeth(m, "head", 4.0 * sx, 4.0, -27, -15, 6, height=1.5, size=0.9)
        m.cube("head", (3.6 * sx - 0.6, 4.0, -26.2), (1.2, 3.0, 1.2), "teeth")
    for x in (-2.2, -0.75, 0.75, 2.2):
        m.cube("head", (x - 0.4, 4.0, -27.8), (0.8, 1.2, 0.8), "teeth")
    m.cube("head", (-3.6, 4.02, -27.2), (7.2, 0.3, 13.5), "mouth")                          # Gaumen
    for i, (z, h) in enumerate(((-9, 2.6), (-4.5, 3.2), (0, 2.8)), start=1):                 # Stirnkamm
        spike(m, "head", f"crest{i}", (0, -6.0, z), h, 2.2, lean=-55)
    for side, sx in (("l", 1), ("r", -1)):
        horn_back(m, "head", f"horn_{side}", (4.6 * sx, -5.6, -3.5),
                  [(8, 4.4, (26, 24 * sx, 0)), (7.5, 3.4, (-9, 6 * sx, 0)), (6.5, 2.6, (-6, 2 * sx, 0)),
                   (5.5, 1.8, (10, -4 * sx, 0))])
        horn_back(m, "head", f"horn_{side}_low", (6.0 * sx, -2.6, -2.5),
                  [(5.5, 2.6, (6, 42 * sx, 0)), (4.5, 1.9, (-8, 6 * sx, 0)), (3.5, 1.3, (-6, 0, 0))])
        spike(m, "head", f"frill_{side}1", (7.0 * sx, 1.5, -7), 3.4, 1.6, lean=-100, yaw=30 * sx)
        spike(m, "head", f"frill_{side}2", (6.8 * sx, 3.6, -4.5), 2.8, 1.4, lean=-110, yaw=26 * sx)

    # ---------------------------------------------------------------- Unterkiefer
    m.bone("jaw", "head", pivot=(0, 4.0, -11.5), rotation=(4, 0, 0))
    rbox(m, "jaw", (-4.5, 0, -16.2), (9, 3.6, 16.5), "hide", bevel=1.3, axis=2)
    rbox(m, "jaw", (-5.2, -0.5, -3), (10.4, 4.4, 5), "hide", bevel=1.4, axis=2)
    rbox(m, "jaw", (-3.4, 2.6, -15.8), (6.8, 1.6, 7), "hide", bevel=0.7, axis=2)        # Kinn
    for sx in (1, -1):
        teeth(m, "jaw", 3.7 * sx, 0.1, -15.6, -4, 6, height=1.3, down=False, size=0.85)
    m.cube("jaw", (-3.8, -0.15, -15.8), (7.6, 0.3, 16), "mouth")
    m.cube("jaw", (-2.6, -0.5, -14.5), (5.2, 0.6, 12), "mouth")                             # Zunge
    m.cube("jaw", (-3.2, 3.4, -11), (6.4, 0.8, 11), "plates")                               # Kehle
    spike(m, "jaw", "beard1", (0, 3.8, -6), 2.6, 1.6, lean=-150)
    for sx in (1, -1):
        spike(m, "jaw", f"beard_{'l' if sx > 0 else 'r'}", (2.6 * sx, 3.6, -3), 2.2, 1.4, lean=-150, yaw=-10 * sx)

    # ---------------------------------------------------------------- Schwanz
    tail = chain2(m, "tail", "body", (0, -4, 20),
                  [(14, 13, 13), (12.2, 11.2, 12), (10.6, 9.6, 12), (9.2, 8.2, 11), (7.8, 7, 11), (6.4, 5.8, 10),
                   (5.2, 4.8, 9), (4.2, 3.8, 8)],
                  [-10, -8, -4, 0, 3, 4, 4, 3], direction=1, spikes=[5, 4.6, 4.2, 3.7, 3.2, 2.7, 2.2, 0], bevel=0.3)
    m.cube(tail[-1], (-5.5, -0.5, 4), (11, 1, 11), "tailblade")                             # Schwanzklinge
    m.cube(tail[-1], (-0.5, -5, 3), (1, 4.5, 9), "tailfin")

    # ---------------------------------------------------------------- Beine
    for side, sx in (("l", 1), ("r", -1)):
        leg2(m, side, sx, "front", "body", (11.5, 4, -14),
             [(10, 13.5, 10, 8), (7.5, 12, 7.5, -14), (9.5, 3.5, 12, 6)], toe_count=3, toe_len=3.5, claw=2.4, mat="limb")
        leg2(m, side, sx, "hind", "body", (12, 2, 14),
             [(12, 15, 14, -12), (8.5, 12.5, 8.5, 34), (10.5, 3.5, 13, -22)], toe_count=3, toe_len=4, claw=2.6, mat="limb")

    # ---------------------------------------------------------------- Flügel
    for side, sx in (("l", 1), ("r", -1)):
        wing2(m, side, sx, "body", (11, -11, -12), arm=28, fore=32, fingers=FINGERS, arm_th=4.5, inner_chord=33,
              fore_chord=34, mats={"bone": "limb"})
    return m


# =============================================================================================
# Bemalung
# =============================================================================================

def _chest(ctx: Ctx):
    """Feuersack: zwischen den Brustplatten dringt Glut nach außen, die Platten selbst sind warm getönt."""
    col = plate_color(ctx, "belly")
    ox, w = ctx.cube.origin[0], ctx.cube.size[0]
    xr = abs((ctx.local[0] - ox) / w * 2 - 1)
    heat = max(0.0, 1.0 - xr * 1.1)
    if ctx.face in ("bottom", "west", "east"):
        k = (ctx.world[2] / 2.4) % 1.0
        if k > 0.7 and heat > 0.08:
            g = pick_smooth(ctx.pal["glow"], 0.35 + 0.6 * heat * (1 - abs(k - 0.85) / 0.15))
            ctx.glow = g
            return g
    if ctx.face == "front":
        # von vorn: Glut scheint durch die dünne Haut der Kehle (Mitte hell, Ränder dunkel)
        ry = (ctx.local[1] - ctx.cube.origin[1]) / max(ctx.cube.size[1], 1e-3)
        g = heat * (0.4 + 0.6 * ry)
        if g > 0.25:
            c = pick_smooth(ctx.pal["glow"], 0.2 + 0.6 * g)
            ctx.glow = c
            return c
    return mix(col, pick_smooth(ctx.pal["glow"], 0.1), 0.35 + heat * 0.35)


def _tail_blade(ctx: Ctx):
    lx, ly, lz = ctx.local
    ox, oy, oz = ctx.cube.origin
    w, h, d = ctx.cube.size
    u = (lx - ox) / w * 2 - 1
    v = (lz - oz) / d
    # Pfeilspitze: hinten spitz, vorn mit zwei Widerhaken
    limit = (1.0 - v) * 1.05 if v > 0.25 else 0.55 + v * 1.6
    if abs(u) > limit:
        return None
    col = pick_smooth(ctx.pal["spike"], 0.3 + 0.45 * (1 - abs(u) / max(limit, 1e-3)))
    if abs(u) < 0.07 and ctx.face in ("top", "bottom"):
        col = pick_smooth(ctx.pal["glow"], 0.55)
        ctx.glow = col
    return col


def _tail_fin(ctx: Ctx):
    lx, ly, lz = ctx.local
    oy, oz = ctx.cube.origin[1], ctx.cube.origin[2]
    h, d = ctx.cube.size[1], ctx.cube.size[2]
    rh = (ly - oy) / max(h, 1e-3)          # 0 oben, 1 unten
    rz = (lz - oz) / max(d, 1e-3)
    if rh < 1.0 - (1.0 - abs(rz * 2 - 1)) * 1.1:
        return None
    return pick_smooth(ctx.pal["spike"], 0.25 + 0.45 * rh)


def materials(variant: str) -> dict:
    return {
        "scales": scales3(cell=2.6, seed=31, belly_from=0.7),
        "hide": scales3(cell=2.0, seed=33, belly_from=0.74),
        "limb": scales3(cell=2.2, seed=35, belly_from=2.0, top_light=0.08, foot_dark=0.14),
        "plates": plates("belly"),
        "chest": _chest,
        "spike": keratin("spike"),
        "horn": keratin("horn", rings=7, base=0.12, span=0.8),
        "claw": keratin("claw", base=0.35, span=0.6),
        "teeth": solid("teeth", 0.82, noise=0.08),
        "eye": eye("eye"),
        "nostril": solid("back", 0.02, noise=0.0, ao=0.0),
        "mouth": mouth("mouth", glow=True),
        "membrane_inner": arm_web("membrane", "vein"),
        "membrane_fore": arm_web("membrane", "vein", fore=True),
        "membrane_outer": finger_web("membrane", "vein", tatter=0.18, seed=5, edge_key="back"),
        "tailblade": _tail_blade,
        "tailfin": _tail_fin,
        "none": transparent_painter(),
    }
