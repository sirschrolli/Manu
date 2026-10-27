"""
Nyxar, der Schattendrache – der Endgegner der Schattenwelt.

Größer und hagerer als die sechs Elementdrachen: ein langer, schlanker Leib, unter dessen fast schwarzen Schuppen
an den Flanken violettes Seelenlicht durch die Fugen dringt, als glühten Seelen in ihm. Ein langer Hals (sieben
Glieder) und ein noch längerer Schwanz (zehn Glieder), beide mit einem Kamm aus Nachtkristallen, der Schwanz endet in
einer gespenstischen Klinge. Der Kopf ist schmal und kantig, mit drei Augenpaaren und einer Krone aus sechs Hörnern
– zwei gewaltige, nach hinten geschwungene, zwei steil aufragende und zwei Wangenhörner. Riesige, zerfetzte Flügel
mit leuchtenden Adern. Grundgröße ca. 13 Blöcke, im Spiel ×2,3 (ca. 30 Blöcke).
"""
from __future__ import annotations

from ..geometry import Model
from ..paint import Ctx, hash01, noise3, ramp, transparent_painter
from .boss_style import (arm_web, chain2, crystal2, edge_ao, eye, finger_web, fold_pose, horn_back, keratin, leg2, mix,
                         mouth, pick_smooth, plate_color, plates, rbox, scales3, solid, spike, teeth, wing2)

PALETTES = {
    "nyxar": {
        "back": ramp("#0a0712", "#55457a"), "belly": ramp("#170f22", "#7e6c9c"),
        "glow": ramp("#4a1496", "#f6ecff"), "membrane": ramp("#07050c", "#33264f"),
        "vein": ramp("#2c1450", "#b884ff"), "horn": ramp("#0b0812", "#a698bc"),
        "spike": ramp("#0d0a14", "#7c6c98"), "claw": ramp("#0e0c14", "#d6d0e4"),
        "teeth": ramp("#5e586c", "#f2eefa"), "eye": ramp("#b06aff", "#ffffff"),
        "mouth": ramp("#100818", "#9c4cff"), "crystal": ramp("#1c0a3c", "#cfa8ff"),
    },
}

TEX = (512, 512)
FINGERS = [(60, 6), (55, 31), (48, 56), (39, 80)]
FOLD_POSE = fold_pose(FINGERS)

NECK = [(13.5, 13, 7.5), (12.6, 12.2, 7), (11.8, 11.4, 7), (11, 10.6, 6.5), (10.4, 10, 6.5), (9.8, 9.4, 6), (9.4, 9, 6)]
NECK_ROT = [-30, -12, -4, 3, 6, 8, 7]
TAIL = [(13.5, 12.5, 12.5), (12, 11, 12), (10.6, 9.8, 11.5), (9.4, 8.6, 11), (8.2, 7.6, 10.5), (7.2, 6.6, 10),
        (6.2, 5.8, 9.5), (5.3, 5.0, 9), (4.5, 4.2, 8.5), (3.8, 3.5, 8)]
TAIL_ROT = [-10, -8, -5, -2, 1, 3, 4, 4, 3, 2]


def _crystal_spike(m: Model, parent: str, name: str, pos, height: float, base: float, lean: float = -28.0,
                   yaw: float = 0.0, roll: float = 0.0) -> None:
    spike(m, parent, name, pos, height, base, lean=lean, mat="crystal", yaw=yaw, roll=roll)


def build() -> Model:
    m = Model("nyxar", *TEX)
    m.tex_res = 2

    # ---------------------------------------------------------------- Rumpf (lang und hager)
    m.bone("body", pivot=(0, -8, 0))
    rbox(m, "body", (-11.5, -12, -22), (23, 19, 44), "scales", bevel=4.5, axis=2)        # Rumpf
    rbox(m, "body", (-13, -13, -24), (26, 21, 16), "scales", bevel=5.0, axis=2)          # Brustkorb
    rbox(m, "body", (-12.2, -12.5, 8), (24.4, 18.5, 13), "scales", bevel=4.5, axis=2)     # Hüften
    rbox(m, "body", (-5.5, -15, -21), (11, 4, 38), "scales", bevel=1.8, axis=2)            # Rückgrat
    for sx in (1, -1):                                                                     # Flügelansatz
        rbox(m, "body", (8.5 if sx > 0 else -14.5, -14.5, -18), (6, 7, 11), "scales", bevel=2.0, axis=2)
        # Rippenbögen an den Flanken: zwischen ihnen glimmt das Seelenlicht
        for i, z in enumerate((-15, -9.5, -4, 1.5)):
            m.cube("body", ((11.0 if sx > 0 else -12.4), -6 + i * 0.4, z), (1.4, 10.5 - i * 0.6, 2.2), "rib")
    rbox(m, "body", (-9, 7.5, -23), (18, 2.2, 15), "chest", bevel=1.2, axis=2)            # Seelenkern in der Brust
    m.cube("body", (-8.5, 6.4, -8), (17, 2, 28), "plates")                                 # Bauchplatten
    for i, (z, h) in enumerate(zip((-18, -11, -4, 3, 10, 16), (8.5, 10.5, 11.5, 10.5, 8.5, 6.5)), start=1):
        _crystal_spike(m, "body", f"back_spike{i}", (0, -14.6, z), h, 3.4, lean=-26)

    # ---------------------------------------------------------------- Hals (sieben Glieder, Kristallkamm)
    neck = chain2(m, "neck", "body", (0, -6, -22), NECK, NECK_ROT, direction=-1, spikes=None, bevel=0.3, mat="hide")
    for i, name in enumerate(neck):
        w, h, length = NECK[i]
        _crystal_spike(m, name, f"{name}_spike", (0, -h / 2 + 0.4, -length * 0.45), 5.2 - i * 0.35, 2.4, lean=-36)

    # ---------------------------------------------------------------- Kopf (schmal, kantig, drei Augenpaare)
    m.bone("head", neck[-1], pivot=(0, 0, -6.5), rotation=(30, 0, 0))
    rbox(m, "head", (-6.2, -6.0, -13), (12.4, 10.0, 14.5), "hide", bevel=2.6, axis=2)    # Schädel
    rbox(m, "head", (-5.0, -6.2, -17), (10.0, 3, 7), "hide", bevel=1.2, axis=2)           # Stirn
    rbox(m, "head", (-4.6, -4.4, -22), (9.2, 8.0, 10), "hide", bevel=2.0, axis=2)          # Schnauze hinten
    rbox(m, "head", (-3.9, -3.9, -30), (7.8, 7.4, 9), "hide", bevel=1.8, axis=2)           # Schnauze vorn (lang)
    rbox(m, "head", (-2.4, -5.5, -28.5), (4.8, 1.8, 15), "hide", bevel=0.7, axis=2)       # Nasenrücken
    rbox(m, "head", (-3.0, -3.4, -30.9), (6.0, 4.0, 1.2), "hide", bevel=0.8, axis=0)      # Nasenspitze
    for sx in (1, -1):
        side = "l" if sx > 0 else "r"
        m.cube("head", ((1.2 if sx > 0 else -2.6), -3.2, -31.1), (1.4, 0.9, 0.6), "nostril")
        m.bone(f"brow_{side}", "head", pivot=(4.2 * sx, -5.3, -12.8), rotation=(8, 0, -12 * sx))
        m.cube(f"brow_{side}", (-2.3, -1.4, -0.6), (4.6, 2.4, 9), "hide")
        rbox(m, "head", ((4.8 if sx > 0 else -6.8), -0.8, -14), (2.0, 4.8, 8), "hide", bevel=0.7, axis=2)  # Wangen
        # drei Augen hintereinander an der Schädelkante (das vordere am größten)
        m.cube("head", ((4.75 if sx > 0 else -6.55), -3.8, -13.6), (1.8, 2.0, 3.6), "eye")
        m.cube("head", ((4.95 if sx > 0 else -6.45), -4.2, -9.0), (1.5, 1.5, 2.4), "eye")
        m.cube("head", ((5.05 if sx > 0 else -6.35), -4.4, -5.6), (1.3, 1.2, 1.8), "eye")
        teeth(m, "head", 3.7 * sx, 3.7, -29, -15, 7, height=1.6, size=0.85)
        m.cube("head", (3.3 * sx - 0.6, 3.7, -28.2), (1.2, 3.2, 1.2), "teeth")
    for x in (-2.0, -0.7, 0.7, 2.0):
        m.cube("head", (x - 0.4, 3.7, -29.8), (0.8, 1.2, 0.8), "teeth")
    m.cube("head", (-3.4, 3.72, -29.2), (6.8, 0.3, 15.5), "mouth")                          # Gaumen
    for i, (z, h) in enumerate(((-10, 2.4), (-5.5, 3.0), (-1, 3.4)), start=1):              # Stirnkamm
        _crystal_spike(m, "head", f"crest{i}", (0, -6.0, z), h, 2.0, lean=-55)
    for side, sx in (("l", 1), ("r", -1)):
        # die zwei gewaltigen, nach hinten geschwungenen Hörner
        horn_back(m, "head", f"horn_{side}", (4.4 * sx, -5.4, -3.5),
                  [(9, 4.2, (22, 22 * sx, 0)), (8.5, 3.3, (-10, 7 * sx, 0)), (7.5, 2.5, (-9, 3 * sx, 0)),
                   (6.5, 1.8, (-4, -2 * sx, 0)), (5.0, 1.2, (12, -6 * sx, 0))])
        # zwei steil aufragende Hörner (Krone)
        horn_back(m, "head", f"horn_{side}_up", (2.6 * sx, -6.0, -6.5),
                  [(6.5, 2.8, (62, 10 * sx, 0)), (6.0, 2.0, (14, 4 * sx, 0)), (5.0, 1.4, (16, 0, 0))])
        # Wangenhörner
        horn_back(m, "head", f"horn_{side}_low", (5.8 * sx, -2.2, -2.5),
                  [(5.5, 2.4, (4, 44 * sx, 0)), (4.5, 1.8, (-8, 8 * sx, 0)), (3.5, 1.2, (-6, 0, 0))])
        spike(m, "head", f"frill_{side}1", (6.6 * sx, 1.4, -7), 3.6, 1.5, lean=-100, yaw=30 * sx)
        spike(m, "head", f"frill_{side}2", (6.4 * sx, 3.4, -4.5), 3.0, 1.3, lean=-110, yaw=26 * sx)

    # ---------------------------------------------------------------- Unterkiefer
    m.bone("jaw", "head", pivot=(0, 3.7, -11.5), rotation=(4, 0, 0))
    rbox(m, "jaw", (-4.2, 0, -18.2), (8.4, 3.4, 18.5), "hide", bevel=1.3, axis=2)
    rbox(m, "jaw", (-5.0, -0.5, -3), (10.0, 4.2, 5), "hide", bevel=1.4, axis=2)
    rbox(m, "jaw", (-3.0, 2.4, -17.8), (6.0, 1.6, 7), "hide", bevel=0.7, axis=2)        # Kinn
    for sx in (1, -1):
        teeth(m, "jaw", 3.4 * sx, 0.1, -17.6, -4, 7, height=1.4, down=False, size=0.8)
    m.cube("jaw", (-3.5, -0.15, -17.8), (7.0, 0.3, 18), "mouth")
    m.cube("jaw", (-2.4, -0.5, -16.5), (4.8, 0.6, 14), "mouth")                             # Zunge
    m.cube("jaw", (-3.0, 3.2, -12), (6.0, 0.8, 12), "plates")                               # Kehle
    for sx in (1, -1):
        spike(m, "jaw", f"beard_{'l' if sx > 0 else 'r'}", (2.4 * sx, 3.4, -4), 3.0, 1.4, lean=-150, yaw=-10 * sx)
    spike(m, "jaw", "beard1", (0, 3.6, -8), 3.2, 1.6, lean=-150)

    # ---------------------------------------------------------------- Schwanz (zehn Glieder, Seelenklinge)
    tail = chain2(m, "tail", "body", (0, -4, 21), TAIL, TAIL_ROT, direction=1, spikes=None, bevel=0.3)
    for i, name in enumerate(tail[:-1]):
        w, h, length = TAIL[i]
        _crystal_spike(m, name, f"{name}_spike", (0, -h / 2 + 0.4, length * 0.45), max(1.6, 5.4 - i * 0.45), max(1.3, 2.8 - i * 0.16),
                       lean=-34)
    m.cube(tail[-1], (-6.5, -0.5, 4), (13, 1, 13), "tailblade")                             # Seelenklinge
    m.cube(tail[-1], (-0.5, -6, 2), (1, 5.5, 11), "tailfin")

    # ---------------------------------------------------------------- Beine (lang, mit großen Krallen)
    for side, sx in (("l", 1), ("r", -1)):
        leg2(m, side, sx, "front", "body", (10.5, 4, -15),
             [(9.5, 14, 9.5, 8), (7, 12.5, 7, -14), (9, 3.4, 12, 6)], toe_count=3, toe_len=4.0, claw=3.0, mat="limb",
             knee_spike=3.0)
        leg2(m, side, sx, "hind", "body", (11, 2, 15),
             [(11.5, 15.5, 13.5, -12), (8, 13, 8, 34), (10, 3.4, 13, -22)], toe_count=3, toe_len=4.4, claw=3.2, mat="limb",
             knee_spike=3.4)

    # ---------------------------------------------------------------- Flügel (riesig, zerfetzt)
    for side, sx in (("l", 1), ("r", -1)):
        wing2(m, side, sx, "body", (10.5, -11, -13), arm=30, fore=34, fingers=FINGERS, arm_th=4.4, inner_chord=34,
              fore_chord=35, mats={"bone": "limb"})
    return m


# =============================================================================================
# Bemalung
# =============================================================================================

def _chest(ctx: Ctx):
    """Seelenkern: zwischen den Brustplatten pulst violettes Licht, von vorn scheint es durch die Kehle."""
    col = plate_color(ctx, "belly")
    ox, w = ctx.cube.origin[0], ctx.cube.size[0]
    xr = abs((ctx.local[0] - ox) / w * 2 - 1)
    heat = max(0.0, 1.0 - xr * 1.1)
    if ctx.face in ("bottom", "west", "east"):
        k = (ctx.world[2] / 2.6) % 1.0
        if k > 0.72 and heat > 0.08:
            g = pick_smooth(ctx.pal["glow"], 0.35 + 0.55 * heat * (1 - abs(k - 0.86) / 0.14))
            ctx.glow = g
            return g
    if ctx.face == "front":
        ry = (ctx.local[1] - ctx.cube.origin[1]) / max(ctx.cube.size[1], 1e-3)
        g = heat * (0.4 + 0.6 * ry)
        if g > 0.25:
            c = pick_smooth(ctx.pal["glow"], 0.2 + 0.55 * g)
            ctx.glow = c
            return c
    return mix(col, pick_smooth(ctx.pal["glow"], 0.08), 0.25 + heat * 0.3)


def _rib(ctx: Ctx):
    """Rippenbogen: knochig-dunkel, die Kante zum Leib hin glimmt (Seelenlicht dringt zwischen den Rippen hervor)."""
    oy, h = ctx.cube.origin[1], ctx.cube.size[1]
    ry = (ctx.local[1] - oy) / max(h, 1e-3)
    if ctx.face in ("front", "back"):
        g = pick_smooth(ctx.pal["glow"], 0.3 + 0.35 * (1.0 - abs(ry - 0.55) * 1.6))
        ctx.glow = g
        return g
    col = pick_smooth(ctx.pal["spike"], 0.32 + 0.22 * (1.0 - ry) + (hash01(ctx.u, ctx.v, 7) - 0.5) * 0.04)
    return edge_ao(ctx, col, 0.14)


def _tail_blade(ctx: Ctx):
    lx, ly, lz = ctx.local
    ox, oy, oz = ctx.cube.origin
    w, h, d = ctx.cube.size
    u = (lx - ox) / w * 2 - 1
    v = (lz - oz) / d
    # Blatt mit zwei gebogenen Haken, zur Spitze hin durchscheinend leuchtend
    limit = (1.0 - v) * 1.05 if v > 0.3 else 0.5 + v * 1.7
    if abs(u) > limit:
        return None
    t = 1.0 - abs(u) / max(limit, 1e-3)
    col = pick_smooth(ctx.pal["crystal"], 0.25 + 0.4 * t + 0.25 * v)
    if t > 0.75 or v > 0.75:
        ctx.glow = pick_smooth(ctx.pal["glow"], 0.4 + 0.4 * t)
    return col


def _tail_fin(ctx: Ctx):
    lx, ly, lz = ctx.local
    oy, oz = ctx.cube.origin[1], ctx.cube.origin[2]
    h, d = ctx.cube.size[1], ctx.cube.size[2]
    rh = (ly - oy) / max(h, 1e-3)
    rz = (lz - oz) / max(d, 1e-3)
    if rh < 1.0 - (1.0 - abs(rz * 2 - 1)) * 1.1:
        return None
    col = pick_smooth(ctx.pal["crystal"], 0.3 + 0.4 * (1 - rh))
    ctx.glow = pick_smooth(ctx.pal["glow"], 0.3 + 0.3 * (1 - rh))
    return col


def _soul_scales(**kw):
    """Schuppen fast schwarz mit violettem Schimmer; an den unteren Flanken glimmen die Fugen (Seelen im Leib)."""
    base = scales3(**kw)

    def paint(ctx: Ctx):
        col = base(ctx)
        if col is None or ctx.glow is not None:
            return col
        # zarter, wandernder violetter Schimmer auf der Oberseite (wie Öl im Mondlicht)
        x, y, z = ctx.world
        if ctx.face in ("top", "west", "east"):
            n = noise3(x * 0.09, y * 0.09, z * 0.09, 17)
            if n > 0.62:
                col = mix(col, pick_smooth(ctx.pal["vein"], 0.42), min(0.35, (n - 0.62) * 1.6))
        return col

    return paint


def materials(variant: str) -> dict:
    return {
        "scales": _soul_scales(cell=2.6, seed=41, belly_from=0.72, hot=(0.5, 0.11), top_light=0.1),
        "hide": _soul_scales(cell=2.0, seed=43, belly_from=0.76, top_light=0.1),
        "limb": _soul_scales(cell=2.2, seed=45, belly_from=2.0, top_light=0.08, foot_dark=0.14),
        "plates": plates("belly"),
        "chest": _chest,
        "rib": _rib,
        "spike": keratin("spike"),
        "crystal": crystal2("crystal", glow=0.6),
        "horn": keratin("horn", rings=8, base=0.1, span=0.82),
        "claw": keratin("claw", base=0.3, span=0.65),
        "teeth": solid("teeth", 0.82, noise=0.08),
        "eye": eye("eye"),
        "nostril": solid("back", 0.02, noise=0.0, ao=0.0),
        "mouth": mouth("mouth", glow=True),
        "membrane_inner": arm_web("membrane", "vein", glow_veins=True),
        "membrane_fore": arm_web("membrane", "vein", fore=True, glow_veins=True),
        "membrane_outer": finger_web("membrane", "vein", tatter=0.3, seed=9, edge_key="back", glow_veins=True),
        "tailblade": _tail_blade,
        "tailfin": _tail_fin,
        "none": transparent_painter(),
    }
