"""
Pyrrhon, der Aschenfürst – der Feuerdrachen-Boss.

Klassischer, schwerer Drache: vier kräftige Beine, große Fledermausflügel mit vier Fingern, gebogene Widderhörner,
Stachelkamm von Kopf bis Schwanz, Schwanzklinge. Schuppen wie erkaltete Lava (fast schwarz) mit glühenden
Rissen, leuchtender Feuersack in der Brust. Grundgröße ca. 12 Blöcke, im Spiel ×2,1 (ca. 25 Blöcke).
"""
from __future__ import annotations

from ..geometry import Model
from ..paint import Ctx, flat_painter, gradient_painter, pick, ramp, transparent_painter
from .boss_common import (arm_membrane, chain, cracked_scales, finger_membrane, glow_painter, glowing_eye,
                          horn_painter, leg, mouth_painter, plates_painter, spike_row, wing)

PALETTES = {
    "pyrrhon": {
        "back": ramp("#120705", "#5c2016"), "belly": ramp("#4a1406", "#e88a30"),
        "crack": ramp("#b82800", "#fff08a"), "membrane": ramp("#240806", "#8a2a16"),
        "vein": ramp("#7a1a06", "#ff8a30"), "horn": ramp("#140e0c", "#9a8672"),
        "spike": ramp("#100a08", "#8a6a58"), "claw": ramp("#1c1512", "#e0d4c0"),
        "eye": ramp("#ff5a00", "#fff6b0"), "mouth": ramp("#3a0806", "#d0401c"),
    },
}

TEX = (512, 384)


def build() -> Model:
    m = Model("pyrrhon", *TEX)

    # ---------------------------------------------------------------- Rumpf
    m.bone("body", pivot=(0, -8, 0))
    m.cube("body", (-13, -12, -22), (26, 22, 44), "scales")                     # Rumpf
    m.cube("body", (-10.5, -15.5, -19), (21, 4, 34), "scales")                  # Rückenwölbung
    m.cube("body", (-8, -17.5, -14), (16, 2.5, 22), "scales")
    m.cube("body", (-11.5, 8, -21), (23, 5, 24), "chest")                       # Brust (Feuersack)
    m.cube("body", (-10.5, 8, 3), (21, 3.5, 17), "plates")                      # Bauch hinten
    m.cube("body", (-15, -8, -20), (3, 13, 13), "scales")                       # Schultern
    m.cube("body", (12, -8, -20), (3, 13, 13), "scales")
    m.cube("body", (-14.5, -6, 8), (2.5, 13, 14), "scales")                     # Hüften
    m.cube("body", (12, -6, 8), (2.5, 13, 14), "scales")
    spike_row(m, "body", [-19, -11, -3, 5, 13], -17, [7, 9, 10, 8.5, 6.5], width=3.4)

    # ---------------------------------------------------------------- Hals und Kopf
    # sechs kurze Halsglieder: der Hals biegt sich weich statt in wenigen Knicken
    neck = chain(m, "neck", "body", (0, -7, -20),
                 [(15, 15, 7.5), (14, 14, 7), (13, 13, 7), (12, 12, 6.5), (11, 11, 6), (10, 10, 6)],
                 [-28, -12, -2, 6, 9, 7], direction=-1, spikes=[5.5, 5.2, 4.9, 4.6, 4.3, 4], belly="plates")
    m.bone("head", neck[-1], pivot=(0, 0, -8.5), rotation=(30, 0, 0))
    m.cube("head", (-7, -6, -15), (14, 12, 15), "scales")                       # Schädel
    m.cube("head", (-7.5, -7.5, -14), (15, 2.5, 7), "scales")                   # Stirnwülste
    m.cube("head", (-5, -3.5, -29), (10, 8, 14), "scales")                      # Schnauze
    m.cube("head", (-3, -5, -27.5), (6, 2, 11), "scales")                       # Nasenrücken
    m.cube("head", (-4, -4, -29.3), (2.5, 1.5, 1.5), "nostril")
    m.cube("head", (1.5, -4, -29.3), (2.5, 1.5, 1.5), "nostril")
    m.cube("head", (-4.5, 4.5, -28.5), (9, 1.5, 12), "teeth")                   # obere Zahnreihe
    m.cube("head", (-4.5, 4.4, -28.4), (9, 0.2, 13.5), "mouth")                 # Gaumen
    m.cube("head", (6.2, -2, -14), (1.6, 7, 9), "scales")                       # Wangenplatten
    m.cube("head", (-7.8, -2, -14), (1.6, 7, 9), "scales")
    m.cube("head", (6.3, -4.6, -13.8), (1, 2.2, 3.6), "eye")
    m.cube("head", (-7.3, -4.6, -13.8), (1, 2.2, 3.6), "eye")
    spike_row(m, "head", [-12, -7, -2], -6, [3, 3.5, 3], width=1.8)
    m.bone("jaw", "head", pivot=(0, 4.5, -12), rotation=(3, 0, 0))
    m.cube("jaw", (-5, 0, -17), (10, 4, 17), "scales")
    m.cube("jaw", (-4.2, -1.3, -16.5), (8.4, 1.3, 11), "teeth")
    m.cube("jaw", (-3.6, -0.2, -15.5), (7.2, 0.4, 14), "mouth")                # Zunge/Maulboden
    m.cube("jaw", (-1, 3.5, -10), (2, 2.5, 3), "spike")                          # Kinnstachel
    # Widderhörner: vier Glieder, die sich hinter dem Ohr nach unten und vorn einrollen
    for side, sx in (("l", 1), ("r", -1)):
        sizes = [(4.6, 4.6, 8), (4, 4, 7), (3.3, 3.3, 6.5), (2.5, 2.5, 5.5)]
        rots = [(-12, 38 * sx, 0), (-55, 8 * sx, 0), (-62, 6 * sx, 0), (-55, -6 * sx, 0)]
        parent, pivot = "head", (5.6 * sx, -6.2, -7)
        for i, ((w, h, length), rot) in enumerate(zip(sizes, rots), start=1):
            name = f"horn_{side}{i}"
            m.bone(name, parent, pivot=pivot, rotation=rot)
            m.cube(name, (-w / 2, -h / 2, -0.5), (w, h, length + 0.8), "horn")
            parent, pivot = name, (0, 0, length)
        m.bone(f"horn_{side}_back", "head", pivot=(3 * sx, -6.5, -3), rotation=(-22, 12 * sx, 0))
        m.cube(f"horn_{side}_back", (-1.2, -1.2, 0), (2.4, 2.4, 8), "horn")

    # ---------------------------------------------------------------- Schwanz
    tail = chain(m, "tail", "body", (0, -4, 21),
                 [(14, 13, 13), (12, 11, 12), (10.5, 9.5, 12), (9, 8, 11), (7.5, 7, 11), (6, 6, 10), (5, 5, 9),
                  (4, 4, 8)],
                 [-10, -8, -4, 0, 3, 4, 4, 3], direction=1, spikes=[5, 4.5, 4, 3.5, 3, 2.5, 0, 0], belly="plates")
    m.cube(tail[-1], (-5.5, -0.5, 5), (11, 1, 10), "tailblade")                 # Schwanzklinge (flach)
    m.cube(tail[-1], (-0.5, -5, 4), (1, 4.5, 9), "tailfin")                      # senkrechte Zacke

    # ---------------------------------------------------------------- Beine
    for side, sx in (("l", 1), ("r", -1)):
        leg(m, side, sx, "front", "body", (11.5, 4, -14),
            [(10, 13.5, 10, 8), (7.5, 12, 7.5, -14), (9.5, 3.5, 12, 6)], toes=3, toe_len=3.5)
        leg(m, side, sx, "hind", "body", (12, 2, 14),
            [(12, 15, 14, -12), (8.5, 12.5, 8.5, 34), (10.5, 3.5, 13, -22)], toes=3, toe_len=4)

    # ---------------------------------------------------------------- Flügel
    for side, sx in (("l", 1), ("r", -1)):
        wing(m, side, sx, "body", (11, -11, -12), arm=28, fore=32,
             fingers=[(54, 6), (50, 30), (44, 54), (36, 78)], arm_th=4.5, inner_chord=33, fore_chord=34)
    return m


# =============================================================================================
# Bemalung
# =============================================================================================

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
    col = pick(ctx.pal["spike"], 0.35 + 0.4 * (1 - abs(u)))
    if abs(u) < 0.1:
        col = pick(ctx.pal["crack"], 0.6)
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
    return pick(ctx.pal["spike"], 0.3 + 0.4 * rh)


def materials(variant: str) -> dict:
    return {
        "scales": cracked_scales(cell=4.0, seed=31),
        "plates": plates_painter("belly"),
        "chest": plates_painter("belly", glow_from=6.0),
        "spike": gradient_painter("spike", 0.25, 0.85),
        "horn": horn_painter("horn", rings=5),
        "claw": flat_painter("claw", 0.78),
        "teeth": flat_painter("claw", 0.93, 0.05),
        "eye": glowing_eye("eye"),
        "nostril": glow_painter("crack", 0.75),
        "mouth": mouth_painter(),
        "membrane_inner": arm_membrane("membrane", "vein", glow_veins=True),
        "membrane_fore": arm_membrane("membrane", "vein", fore=True, glow_veins=True),
        "membrane_outer": finger_membrane("membrane", "vein", tatter=0.22, glow_veins=True, seed=5),
        "tailblade": _tail_blade,
        "tailfin": _tail_fin,
        "none": transparent_painter(),
    }
