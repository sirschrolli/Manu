"""
Terragon, der Bergbrecher – der Erddrachen-Boss.

Flügellos, massig und gepanzert wie ein wandelnder Berg: breiter, tief getragener Rumpf unter einem Panzer aus
übereinandergeschobenen Felsplatten, aus denen leuchtende Bernsteinkristalle wachsen. Kurzer, dicker Hals, ein
schwerer Rammschädel mit Stirnplatte, zwei nach vorn gerichteten Hörnern, einem Nasenhorn und Hauern. Der Schwanz
endet in einer gewaltigen Felskeule mit Kristallzacken, die Beine sind dick wie Säulen. Durch die steingraue Haut
ziehen sich glimmende Bernsteinadern und goldene Erzsprenkel. Grundgröße ca. 11 Blöcke, im Spiel ×2,2.
"""
from __future__ import annotations

import math

from ..geometry import Model
from ..paint import Ctx, Voronoi3D, flat_painter, hash01, mix, noise3, pick, ramp, transparent_painter
from .boss_common import (belly_plate, chain, cracked_scales, crystal_painter, facing_ry, glow_painter, glowing_eye,
                          horn_painter, leg, mouth_painter, plates_painter)

PALETTES = {
    "terragon": {
        "back": ramp("#38322c", "#b4a898"), "belly": ramp("#3a3026", "#b09a7a"), "crack": ramp("#8a4a08", "#ffd27a"),
        "plate": ramp("#3a3630", "#c4bcac"), "crystal": ramp("#a8560e", "#fff2a8"), "horn": ramp("#26201c", "#dccfb8"),
        "claw": ramp("#1e1a16", "#c8bca8"), "eye": ramp("#ff8a10", "#fff4c0"), "mouth": ramp("#2a1610", "#9a5a3a"),
        "gold": ramp("#8a6a1a", "#ffe68a"), "lichen": ramp("#4e5a3a", "#b8c490"),
    },
}

TEX = (512, 512)


def build() -> Model:
    m = Model("terragon", *TEX)

    # ---------------------------------------------------------------- Rumpf (breit, tief) mit Felspanzer
    m.bone("body", pivot=(0, -6, 0))
    m.cube("body", (-16, -11, -22), (32, 20, 44), "scales")
    m.cube("body", (-14, 9, -20), (28, 4, 24), "plates")                        # Brust
    m.cube("body", (-13, 9, 4), (26, 3.5, 17), "plates")
    m.cube("body", (-18, -7, -20), (3, 13, 14), "scales")                       # Schultern
    m.cube("body", (15, -7, -20), (3, 13, 14), "scales")
    m.cube("body", (-17.5, -6, 7), (2.5, 14, 15), "scales")                     # Hüften
    m.cube("body", (15, -6, 7), (2.5, 14, 15), "scales")
    # Felsplatten: vier Reihen zu je drei Platten, leicht verkantet und übereinandergeschoben
    rows = [-19, -9, 1, 11]
    for r, z in enumerate(rows):
        for c, x in enumerate((-10.5, 0, 10.5)):
            name = f"plate_{r}_{c}"
            tilt = (-8 + r * 3) if c == 1 else (-6 + r * 2)
            roll = 0 if c == 1 else (14 if x > 0 else -14)
            m.bone(name, "body", pivot=(x, -11.5 - (1.5 if c == 1 else 0), z), rotation=(tilt, (r - 1.5) * 3 * (1 if c != 0 else -1), roll))
            w = 11 if c == 1 else 10
            m.cube(name, (-w / 2, -3.2, -1), (w, 3.5, 11), "plate")
            m.cube(name, (-w / 2 + 1.5, -4.2, 1), (w - 3, 1.2, 7), "plate")     # Grat
    # Bernsteinkristalle wachsen zwischen den Platten
    clusters = [(-6, -14, -14, 0), (7, -14.5, -4, 1), (-2, -15, 6, 2), (9, -13.5, 14, 3), (-10, -13, 15, 4)]
    for k, (x, y, z, seed) in enumerate(clusters):
        for i in range(3 + (k % 2)):
            name = f"crystal_{k}_{i}"
            lean_x = ((seed * 7 + i * 13) % 5 - 2) * 9
            lean_z = ((seed * 11 + i * 5) % 5 - 2) * 9
            h = 6 + ((seed * 3 + i * 7) % 5) * 1.6
            th = 2.0 + ((seed + i) % 3) * 0.6
            m.bone(name, "body", pivot=(x + (i - 1) * 1.6, y, z + ((i * 7) % 3 - 1) * 1.4), rotation=(lean_z, 0, lean_x))
            m.cube(name, (-th / 2, -h, -th / 2), (th, h + 1, th), "crystal")
            m.cube(name, (-th / 4, -h - 1.6, -th / 4), (th / 2, 1.8, th / 2), "crystal")

    # ---------------------------------------------------------------- Hals (kurz, dick) und Rammschädel
    neck_sizes = [(18, 17, 7), (17, 16, 6.5), (16, 15, 6)]
    neck = chain(m, "neck", "body", (0, -5, -21), neck_sizes, [-14, -4, 6], direction=-1, belly="plates")
    for name, (w, h, length) in zip(neck, neck_sizes):
        m.cube(name, (-w / 2 + 2, -h / 2 - 2.5, -length), (w - 4, 3, length), "plate")
    m.bone("head", neck[-1], pivot=(0, 0, -6), rotation=(12, 0, 0))
    m.cube("head", (-9, -8, -17), (18, 15, 17), "scales")                       # Schädel
    m.cube("head", (-10, -10.5, -17.5), (20, 4, 11), "plate")                   # Stirnplatte
    m.cube("head", (-7, -5, -29.5), (14, 10, 13), "scales")                     # Schnauze
    m.cube("head", (-5.5, -7, -28), (11, 2.5, 11), "plate")                     # Nasenplatte
    m.cube("head", (-5, -3.5, -29.8), (3, 2, 1.5), "nostril")
    m.cube("head", (2, -3.5, -29.8), (3, 2, 1.5), "nostril")
    m.cube("head", (-6.5, 4.5, -29), (13, 1.5, 12.5), "teeth")
    m.cube("head", (-6.5, 4.4, -28.9), (13, 0.2, 13.5), "mouth")
    m.cube("head", (8.6, -5.2, -15.5), (1, 2, 3.4), "eye")
    m.cube("head", (-9.6, -5.2, -15.5), (1, 2, 3.4), "eye")
    m.cube("head", (8.8, -2, -16), (1.8, 8, 11), "scales")                      # Wangenwülste
    m.cube("head", (-10.6, -2, -16), (1.8, 8, 11), "scales")
    # Nasenhorn (nach vorn oben)
    m.bone("nose_horn", "head", pivot=(0, -7.5, -25), rotation=(55, 0, 0))
    m.cube("nose_horn", (-2, -2, -0.5), (4, 4, 5), "horn")
    m.cube("nose_horn", (-1.2, -1.2, 4), (2.4, 2.4, 4), "horn")
    # zwei große Stirnhörner, nach vorn gerichtet
    for side, sx in (("l", 1), ("r", -1)):
        m.bone(f"horn_{side}1", "head", pivot=(6 * sx, -9, -10), rotation=(-20, 18 * sx, 0))
        m.cube(f"horn_{side}1", (-2.2, -2.2, -10), (4.4, 4.4, 10.5), "horn")
        m.bone(f"horn_{side}2", f"horn_{side}1", pivot=(0, 0, -10), rotation=(-12, -6 * sx, 0))
        m.cube(f"horn_{side}2", (-1.4, -1.4, -8), (2.8, 2.8, 8.5), "horn")
    m.bone("jaw", "head", pivot=(0, 4.5, -14), rotation=(3, 0, 0))
    m.cube("jaw", (-7, 0, -15.5), (14, 5, 15.5), "scales")
    m.cube("jaw", (-6, -1.3, -15), (12, 1.3, 10), "teeth")
    m.cube("jaw", (-5.4, -0.2, -14.5), (10.8, 0.4, 13), "mouth")
    for sx in (1, -1):                                                         # Hauer
        m.cube("jaw", (5.2 * sx - 0.9, -4.5, -14.5), (1.8, 5, 1.8), "horn")

    # ---------------------------------------------------------------- Schwanz mit Felskeule
    tail_sizes = [(16, 14, 12), (14, 12, 11), (12, 10, 10), (10, 8.5, 10), (8.5, 7, 9), (7, 6, 8)]
    tail = chain(m, "tail", "body", (0, -2, 21), tail_sizes, [-8, -4, 0, 2, 3, 2], direction=1, belly="plates")
    for name, (w, h, length) in zip(tail[:5], tail_sizes):
        m.cube(name, (-w / 2 + 1.5, -h / 2 - 2.5, 1), (w - 3, 3, length - 2), "plate")
    m.cube(tail[-1], (-8, -7, 4), (16, 13, 13), "club")                         # Felskeule
    m.cube(tail[-1], (-6, -9, 6), (12, 2.5, 9), "plate")
    for k, (x, y, z, rot) in enumerate([(-7, -6, 9, (0, 0, 60)), (7, -6, 9, (0, 0, -60)), (0, -9, 10, (-20, 0, 0)),
                                       (0, 4, 10, (200, 0, 0))]):
        b = f"club_spike_{k}"
        m.bone(b, tail[-1], pivot=(x, y, z), rotation=rot)
        m.cube(b, (-1.3, -5.5, -1.3), (2.6, 6, 2.6), "crystal")

    # ---------------------------------------------------------------- Beine (säulendick)
    for side, sx in (("l", 1), ("r", -1)):
        leg(m, side, sx, "front", "body", (12.5, 5, -14),
            [(13, 12, 13, 4), (11, 9, 11, -6), (13, 3, 14, 2)], toes=3, toe_len=3.0, claw_mat="claw", mat="scales")
        leg(m, side, sx, "hind", "body", (12.5, 4, 13),
            [(15, 13, 16, -8), (12, 9, 12, 16), (14, 3, 15, -8)], toes=3, toe_len=3.2, claw_mat="claw", mat="scales")
    return m


# =============================================================================================
# Bemalung
# =============================================================================================

def _plate(seed: int = 171):
    """Felsplatte: Gesteinsschichten, raue, dunklere Kanten, Flechten und goldene Erzsprenkel."""

    def paint(ctx: Ctx):
        x, y, z = ctx.world
        strata = math.sin((y + noise3(x * 0.15, 0, z * 0.15, seed) * 6.0) * 1.1)
        v = 0.42 + 0.14 * strata + 0.16 * (noise3(x * 0.5, y * 0.5, z * 0.5, seed + 1) - 0.5)
        if ctx.face == "top":
            v += 0.08
        edge = ctx.s == 0 or ctx.t == 0 or ctx.s == ctx.w - 1 or ctx.t == ctx.h - 1
        if edge:
            v -= 0.14
        col = pick(ctx.pal["plate"], v)
        r = hash01(round(x * 2), round(y * 2), round(z * 2), seed)
        if r > 0.985:
            g = pick(ctx.pal["gold"], 0.8)
            ctx.glow = mix(g, (0, 0, 0, 255), 0.5)
            return g
        if noise3(x * 0.35 + 4, y * 0.35, z * 0.35, seed + 2) > 0.76 and ctx.face in ("top", "west", "east"):
            col = mix(col, pick(ctx.pal["lichen"], 0.5), 0.6)
        return col

    return paint


def _club(seed: int = 181):
    """Felskeule: grober Fels mit Rissen, in denen Bernstein glimmt."""
    vor = Voronoi3D(2.6, seed)

    def paint(ctx: Ctx):
        x, y, z = ctx.world
        val, edge = vor(x, y, z)
        if edge < 0.12:
            col = pick(ctx.pal["crack"], 0.6)
            ctx.glow = col
            return col
        return pick(ctx.pal["plate"], 0.3 + 0.4 * val - (0.1 if edge < 0.25 else 0))

    return paint


def materials(variant: str) -> dict:
    return {
        "scales": cracked_scales(cell=4.6, seed=57, crack_width=0.009, heat=0.18, branch=0.4, freq=0.06),
        "plates": plates_painter("belly"),
        "plate": _plate(),
        "club": _club(),
        "crystal": crystal_painter("crystal", glow=True, facets=2.6),
        "horn": horn_painter("horn", rings=4),
        "claw": flat_painter("claw", 0.55),
        "teeth": flat_painter("claw", 0.9, 0.05),
        "eye": glowing_eye("eye"),
        "nostril": glow_painter("crack", 0.6),
        "mouth": mouth_painter(),
        "none": transparent_painter(),
    }
