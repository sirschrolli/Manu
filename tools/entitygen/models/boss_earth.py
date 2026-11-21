"""
Terragon, der Bergbrecher – der Erddrachen-Boss.

Flügellos, massig und gepanzert wie ein wandelnder Berg: breiter, tief getragener Rumpf unter einem Panzer aus
übereinandergeschobenen Felsplatten, aus denen leuchtende Bernsteinkristalle wachsen. Kurzer, dicker Hals, ein
schwerer Rammschädel mit Stirnplatte, zwei nach vorn geschwungenen Hörnern, einem Nasenhorn und Hauern. Der
Schwanz endet in einer gewaltigen Felskeule mit Kristallzacken, die Beine sind dick wie Säulen. Die Haut ist
steingrau mit warmem Unterton, die Platten verwittert hell, nur Kristalle, Augen und die Risse der Keule glühen.
Grundgröße ca. 11 Blöcke, im Spiel ×2,2.
"""
from __future__ import annotations

import math

from ..geometry import Model
from ..paint import Ctx, Voronoi3D, hash01, mix, noise3, ramp, transparent_painter
from .boss_style import (chain2, crystal2, edge_ao, eye, horn, keratin, leg2, mouth, pick_smooth, plates, rbox, scales3,
                         solid, spike, teeth)

PALETTES = {
    "terragon": {
        "back": ramp("#38322c", "#b8ac9c"), "belly": ramp("#3a3026", "#b8a282"), "glow": ramp("#a8500a", "#ffe08a"),
        "plate": ramp("#34302a", "#cfc6b4"), "crystal": ramp("#a8560e", "#fff2a8"), "horn": ramp("#26201c", "#e4d6bc"),
        "claw": ramp("#1e1a16", "#c8bca8"), "teeth": ramp("#8a8070", "#f2ead8"), "eye": ramp("#ff8a10", "#fff4c0"),
        "mouth": ramp("#2a1610", "#9a5a3a"), "gold": ramp("#8a6a1a", "#ffe68a"), "lichen": ramp("#4e5a3a", "#b8c490"),
    },
}

TEX = (512, 384)


def armor_plate(m: Model, parent: str, name: str, pivot, rot, w: float, d: float, h: float = 3.6) -> None:
    """Felsplatte mit abgeschrägten Kanten und einem Grat in der Mitte (eigener Knochen, verkantet)."""
    m.bone(name, parent, pivot=pivot, rotation=rot)
    rbox(m, name, (-w / 2, -h + 0.3, -1), (w, h, d), "plate", bevel=1.2, axis=2)
    rbox(m, name, (-w / 2 + 2.0, -h - 0.9, 0.6), (w - 4.0, 1.5, d - 3.2), "plate", bevel=0.6, axis=2)


def crystal_spikes(m: Model, parent: str, prefix: str, pos, count: int, seed: int) -> None:
    """Gruppe von Bernsteinkristallen in verschiedenen Neigungen."""
    x, y, z = pos
    for i in range(count):
        lean_x = ((seed * 7 + i * 13) % 5 - 2) * 10
        lean_z = ((seed * 11 + i * 5) % 5 - 2) * 10
        h = 6 + ((seed * 3 + i * 7) % 5) * 1.6
        th = 2.2 + ((seed + i) % 3) * 0.6
        spike(m, parent, f"{prefix}_{i}", (x + (i - 1) * 1.7, y, z + ((i * 7) % 3 - 1) * 1.5), h, th, lean=lean_z,
              roll=lean_x, mat="crystal", steps=3)


def build() -> Model:
    m = Model("terragon", *TEX)
    m.tex_res = 2

    # ---------------------------------------------------------------- Rumpf (breit, tief) mit Felspanzer
    m.bone("body", pivot=(0, -6, 0))
    rbox(m, "body", (-16, -11, -22), (32, 20, 44), "scales", bevel=5.5, axis=2)
    rbox(m, "body", (-17, -12, -22.5), (34, 22, 16), "scales", bevel=6.0, axis=2)         # Brustkorb
    rbox(m, "body", (-16.5, -11.5, 8), (33, 20, 13), "scales", bevel=5.5, axis=2)         # Hüften
    rbox(m, "body", (-13.5, 9, -20), (27, 3, 24), "plates", bevel=1.4, axis=2)
    m.cube("body", (-12.5, 8.4, 4), (25, 3, 17), "plates")
    # Felsplatten: vier Reihen zu je drei Platten, leicht verkantet und übereinandergeschoben
    for r, z in enumerate((-19, -9, 1, 11)):
        for c, x in enumerate((-10.5, 0, 10.5)):
            tilt = (-8 + r * 3) if c == 1 else (-6 + r * 2)
            roll = 0 if c == 1 else (16 if x > 0 else -16)
            yaw = (r - 1.5) * 3 * (1 if c != 0 else -1)
            armor_plate(m, "body", f"plate_{r}_{c}", (x, -11.5 - (1.5 if c == 1 else 0), z), (tilt, yaw, roll),
                        11 if c == 1 else 10, 11)
    # Bernsteinkristalle wachsen zwischen den Platten
    for k, (x, y, z) in enumerate([(-6, -14, -14), (7, -14.5, -4), (-2, -15, 6), (9, -13.5, 14), (-10, -13, 15)]):
        crystal_spikes(m, "body", f"crystal_{k}", (x, y, z), 3 + (k % 2), k)

    # ---------------------------------------------------------------- Hals (kurz, dick)
    neck_sizes = [(18, 17, 7), (17, 16, 6.5), (16, 15, 6)]
    neck = chain2(m, "neck", "body", (0, -5, -21), neck_sizes, [-14, -4, 6], direction=-1, bevel=0.3, mat="hide")
    for i, (name, (w, h, length)) in enumerate(zip(neck, neck_sizes)):
        rbox(m, name, (-w / 2 + 2, -h / 2 - 2.6, -length - 0.3), (w - 4, 3.2, length + 0.6), "plate", bevel=1.0, axis=2)

    # ---------------------------------------------------------------- Rammschädel
    m.bone("head", neck[-1], pivot=(0, 0, -6), rotation=(12, 0, 0))
    rbox(m, "head", (-9, -8, -17), (18, 15, 17), "hide", bevel=3.4, axis=2)                # Schädel
    rbox(m, "head", (-10, -10.5, -17.5), (20, 4, 11), "plate", bevel=1.4, axis=2)          # Stirnplatte
    rbox(m, "head", (-8.5, -11.6, -16.5), (17, 1.6, 8), "plate", bevel=0.6, axis=2)
    rbox(m, "head", (-7.4, -5.4, -23), (14.8, 10.6, 7), "hide", bevel=2.4, axis=2)        # Schnauze hinten
    rbox(m, "head", (-6.6, -4.8, -29.5), (13.2, 9.8, 7), "hide", bevel=2.2, axis=2)       # Schnauze vorn
    rbox(m, "head", (-5.5, -7.2, -28), (11, 2.6, 11), "plate", bevel=1.0, axis=2)         # Nasenplatte
    rbox(m, "head", (-5, -3.6, -30.2), (10, 5.6, 1.0), "hide", bevel=1.0, axis=0)
    for sx in (1, -1):
        s = "l" if sx > 0 else "r"
        m.cube("head", ((1.8 if sx > 0 else -4.0), -3.0, -30.4), (2.2, 1.3, 0.5), "nostril")
        m.cube("head", ((7.0 if sx > 0 else -9.0), -5.6, -17.4), (2.0, 2.2, 3.6), "eye")
        rbox(m, "head", ((8.4 if sx > 0 else -10.6), -2, -16), (2.2, 8, 11), "hide", bevel=0.8, axis=2)   # Wangenwülste
        teeth(m, "head", 6.0 * sx, 5.0, -29, -17, 6, height=1.4, size=1.2)
        # zwei große Stirnhörner, nach vorn geschwungen
        horn(m, "head", f"horn_{s}", (6 * sx, -9, -10),
             [(10, 4.6, (-16, -24 * sx, 0)), (8.5, 3.4, (-12, 8 * sx, 0)), (6, 2.2, (-14, 8 * sx, 0))], taper_to=0.6)
    for x in (-3.0, -1.0, 1.0, 3.0):
        m.cube("head", (x - 0.5, 5.0, -29.6), (1.0, 1.2, 1.0), "teeth")
    m.cube("head", (-6.0, 5.02, -29), (12, 0.3, 12.5), "mouth")
    # Nasenhorn (nach vorn oben)
    horn(m, "head", "nose_horn", (0, -7.6, -25), [(4.5, 3.6, (-120, 0, 0)), (4.0, 2.4, (-18, 0, 0)), (3, 1.3, (-18, 0, 0))],
         mat="nosehorn", taper_to=0.6)

    m.bone("jaw", "head", pivot=(0, 4.9, -14), rotation=(3, 0, 0))
    rbox(m, "jaw", (-7, 0, -15.5), (14, 5, 15.5), "hide", bevel=1.8, axis=2)
    rbox(m, "jaw", (-8, -0.4, -3), (16, 5.6, 4.5), "hide", bevel=1.8, axis=2)
    for sx in (1, -1):
        teeth(m, "jaw", 5.6 * sx, 0.1, -15, -4, 5, height=1.2, down=False, size=1.1)
        spike(m, "jaw", f"tusk_{'l' if sx > 0 else 'r'}", (6.8 * sx, 0.6, -13.5), 5.0, 2.0, lean=10, roll=14 * sx,
              mat="horn", steps=3)
    m.cube("jaw", (-6.2, -0.15, -15.2), (12.4, 0.3, 15), "mouth")
    m.cube("jaw", (-4.4, -0.5, -13.5), (8.8, 0.6, 11), "mouth")
    m.cube("jaw", (-5, 4.6, -12), (10, 0.9, 12), "plates")

    # ---------------------------------------------------------------- Schwanz mit Felskeule
    tail_sizes = [(16, 14, 12), (14, 12, 11), (12, 10, 10), (10, 8.5, 10), (8.5, 7, 9), (7, 6, 8)]
    tail = chain2(m, "tail", "body", (0, -2, 21), tail_sizes, [-8, -4, 0, 2, 3, 2], direction=1, bevel=0.3)
    for name, (w, h, length) in zip(tail[:5], tail_sizes):
        rbox(m, name, (-w / 2 + 1.5, -h / 2 - 2.6, 0.6), (w - 3, 3.2, length - 1.2), "plate", bevel=1.0, axis=2)
    rbox(m, tail[-1], (-8, -7, 4), (16, 13, 13), "club", bevel=3.4, axis=2)                 # Felskeule
    rbox(m, tail[-1], (-6, -9.2, 6), (12, 2.8, 9), "plate", bevel=1.0, axis=2)
    for k, (x, y, z, rot) in enumerate([(-7.5, -5, 10, (0, 0, -60)), (7.5, -5, 10, (0, 0, 60)), (0, -9, 10, (-20, 0, 0)),
                                       (0, 5.5, 10, (180, 0, 0))]):
        spike(m, tail[-1], f"club_spike_{k}", (x, y, z), 5.6, 2.8, lean=rot[0], roll=rot[2], mat="crystal", steps=3)

    # ---------------------------------------------------------------- Beine (säulendick)
    for side, sx in (("l", 1), ("r", -1)):
        leg2(m, side, sx, "front", "body", (12.5, 5, -14),
             [(13, 12, 13, 4), (11, 9, 11, -6), (13, 3, 14, 2)], toe_count=3, toe_len=3.0, claw=2.0, mat="limb")
        leg2(m, side, sx, "hind", "body", (12.5, 4, 13),
             [(15, 13, 16, -8), (12, 9, 12, 16), (14, 3, 15, -8)], toe_count=3, toe_len=3.2, claw=2.2, mat="limb")
    return m


# =============================================================================================
# Bemalung
# =============================================================================================

def _plate(seed: int = 171):
    """Felsplatte: Gesteinsschichten, hellere verwitterte Oberseite, dunkle Kanten, Flechten, selten Golderz."""

    def paint(ctx: Ctx):
        x, y, z = ctx.world
        strata = math.sin((y + noise3(x * 0.15, 0, z * 0.15, seed) * 6.0) * 1.1)
        v = 0.46 + 0.08 * strata + 0.1 * (noise3(x * 0.5, y * 0.5, z * 0.5, seed + 1) - 0.5)
        if ctx.face == "top":
            v += 0.1
        elif ctx.face == "bottom":
            v -= 0.15
        col = pick_smooth(ctx.pal["plate"], v)
        r = hash01(round(x * 1.5), round(y * 1.5), round(z * 1.5), seed)
        if r > 0.992:
            return pick_smooth(ctx.pal["gold"], 0.75)
        if noise3(x * 0.3 + 4, y * 0.3, z * 0.3, seed + 2) > 0.74 and ctx.face in ("top", "west", "east"):
            col = mix(col, pick_smooth(ctx.pal["lichen"], 0.5), 0.5)
        return edge_ao(ctx, col, 0.16)

    return paint


def _club(seed: int = 181):
    """Felskeule: grobe Felsbrocken, zwischen denen Bernstein glimmt (feine Fugen)."""
    vor = Voronoi3D(3.2, seed)

    def paint(ctx: Ctx):
        x, y, z = ctx.world
        val, edge = vor(x, y, z)
        if edge < 0.07:
            col = pick_smooth(ctx.pal["glow"], 0.55 + 0.4 * (1 - edge / 0.07))
            ctx.glow = col
            return col
        v = 0.32 + 0.26 * val + 0.1 * min(1.0, edge * 2) - (0.08 if edge < 0.16 else 0)
        if ctx.face == "top":
            v += 0.08
        return pick_smooth(ctx.pal["plate"], v)

    return paint


def materials(variant: str) -> dict:
    return {
        "scales": scales3(cell=3.2, seed=57, belly_from=0.7, top_light=0.1, var=0.12, relief=0.12, line=0.12),
        "hide": scales3(cell=2.6, seed=59, belly_from=0.72, top_light=0.1, var=0.1, relief=0.12, line=0.12),
        "limb": scales3(cell=2.8, seed=61, belly_from=2.0, top_light=0.06, foot_dark=0.12, relief=0.12, line=0.12),
        "plates": plates("belly"),
        "plate": _plate(),
        "club": _club(),
        "crystal": crystal2("crystal", glow=0.6),
        "horn": keratin("horn", rings=5, base=0.2, span=0.7),
        "nosehorn": keratin("horn", rings=4, base=0.38, span=0.55),
        "claw": keratin("claw", base=0.3, span=0.55),
        "teeth": solid("teeth", 0.8, noise=0.06),
        "eye": eye("eye"),
        "nostril": solid("back", 0.04, noise=0.0, ao=0.0),
        "mouth": mouth("mouth"),
        "none": transparent_painter(),
    }
