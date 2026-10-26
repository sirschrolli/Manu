"""
Verdantis, der Hüter des Hains – der Naturdrachen-Boss.

Ein uraltes, schweres Waldwesen: breiter, tief getragener Rumpf mit Borkenschuppen, auf dem Rücken ein kleiner
Garten aus Moos, Laub, Blüten und Baumpilzen, abgestorbene Äste ragen aus dem Rückgrat. Kurzer, dicker Hals mit
einer Mähne aus Moos und herabhängenden Ranken, breiter, stumpfer Schädel mit einem gewaltigen, verzweigten
Hirschgeweih (mit Laub und Blüten an den Enden) und einem Bart aus Moos. Blattflügel mit nur drei Fingern – die
Flughaut sieht aus wie riesige, geäderte Blätter. Der dicke Schwanz endet in einem Samenkolben mit einem Fächer
aus Farnwedeln, die Füße greifen mit Wurzelzehen in den Boden. Grundgröße ca. 12 Blöcke, im Spiel ×2,0.
"""
from __future__ import annotations

import math

from ..geometry import Model
from ..paint import Ctx, Voronoi3D, hash01, mix, noise3, pick, ramp, transparent_painter
from .boss_style import (ScaleCells, chain2, edge_ao, eye, facing_down, fold_pose, keratin, leg2, mouth, pick_smooth,
                         plate_color, plates, rbox, solid, spike, teeth, wing2)

PALETTES = {
    "verdantis": {
        "back": ramp("#2a2016", "#a08a66"), "belly": ramp("#46502a", "#c8d48e"),
        "moss": ramp("#1c3e16", "#8ccc4e"), "leaf": ramp("#14381a", "#84c64c"),
        "leaf_old": ramp("#4a3a12", "#d4a83a"), "vein": ramp("#2e5a10", "#d8ff8a"),
        "bark": ramp("#1e1610", "#7a6448"), "wood": ramp("#2a1e14", "#a08a64"),
        "lichen": ramp("#4a5a3a", "#c8d4a0"), "blossom": ramp("#c0407a", "#ffe0f0"),
        "blossom2": ramp("#d09a10", "#fff6b0"), "glow": ramp("#2a8a30", "#c8ff9a"),
        "mushroom": ramp("#1a6a6a", "#b0fff0"), "claw": ramp("#1a140e", "#8a7258"),
        "tusk": ramp("#7a6a50", "#f4ecd8"), "teeth": ramp("#8a8068", "#f0ead4"),
        "eye": ramp("#e09010", "#fffcc0"), "mouth": ramp("#2a1408", "#a0503a"),
    },
}

TEX = (512, 512)
FINGERS = [(46, 10), (43, 42), (36, 74)]
FOLD_POSE = fold_pose(FINGERS)


def bush(m: Model, bone: str, origin, size, mat: str = "foliage") -> None:
    """Laubbüschel: abgerundeter Klumpen (zwei überlappende Quader)."""
    rbox(m, bone, origin, size, mat, bevel=min(size) * 0.28)


def build() -> Model:
    m = Model("verdantis", *TEX)
    m.tex_res = 2

    # ---------------------------------------------------------------- Rumpf (breit und schwer)
    m.bone("body", pivot=(0, -8, 0))
    rbox(m, "body", (-14.5, -12, -23), (29, 22, 46), "scales", bevel=5.0, axis=2)
    rbox(m, "body", (-16, -13, -24), (32, 24, 17), "scales", bevel=5.5, axis=2)           # Brustkorb
    rbox(m, "body", (-15, -12.5, 9), (30, 21, 14), "scales", bevel=5.0, axis=2)           # Hüften
    rbox(m, "body", (-11.5, -16, -20), (23, 5, 37), "moss", bevel=2.0, axis=2)            # Moospolster
    rbox(m, "body", (-8, -18.2, -14), (16, 3, 24), "moss", bevel=1.2, axis=2)
    rbox(m, "body", (-12, 9.5, -23), (24, 2.6, 19), "plates", bevel=1.2, axis=2)          # Brust
    m.cube("body", (-11, 8.6, -4), (22, 2.5, 26), "plates")                               # Bauch
    # der Garten auf dem Rücken: Laubbüschel und Blüten
    for (x, y, z, w, h, d) in [(-10, -21.5, -17, 8, 5, 8), (2, -22.5, -11, 9, 6, 9), (-7, -21, -2, 7, 4.5, 7),
                               (4, -20.5, 5, 8, 5, 7), (-4, -19.5, 12, 7, 4, 6)]:
        bush(m, "body", (x, y, z), (w, h, d))
    for k, (x, y, z) in enumerate([(-7, -22.6, -15), (5, -24.3, -8), (-4.5, -22.4, 0), (7, -21.8, 7), (-1.5, -20.8, 14),
                                   (3, -24.3, -12)]):
        m.cube("body", (x, y, z), (1.4, 1.4, 1.4), "blossom" if k % 3 else "blossom2")
    # Baumpilze an den Flanken (wie an einem alten Stamm)
    for sx in (1, -1):
        for (y, z, w, d) in [(-4, -8, 3, 5), (-6, 9, 3, 6), (0, 14, 2, 3.5)]:
            x = 15.6 if sx > 0 else -15.6 - w
            m.cube("body", (x, y, z), (w, 1.2, d), "mushroom")
    # zwei junge Bäume wachsen auf dem Rücken, dazu ein abgestorbener Ast
    for i, (x, z, h, lean, yaw) in enumerate([(-4, -14, 11, -10, -20), (5, 2, 9, -16, 25)]):
        b = f"tree_{i}"
        m.bone(b, "body", pivot=(x, -18, z), rotation=(lean, yaw, 0))
        m.cube(b, (-1.5, -h * 0.5, -1.5), (3.0, h * 0.5 + 1, 3.0), "wood")
        m.cube(b, (-1.1, -h - 0.5, -1.1), (2.2, h * 0.55, 2.2), "wood")
        m.bone(f"{b}_bough", b, pivot=(0, -h * 0.55, 0), rotation=(0, 0, 42 if i == 0 else -42))
        m.cube(f"{b}_bough", (-0.8, -5.5, -0.8), (1.6, 6, 1.6), "wood")
        bush(m, f"{b}_bough", (-3.2, -9, -3.0), (6.4, 4.4, 6))
        bush(m, b, (-4.5, -h - 4.5, -4.2), (9, 5.2, 8.4))
        bush(m, b, (-3.0, -h - 7.6, -2.5), (6.4, 3.6, 5.6))
        bush(m, b, (1.5, -h - 3.0, -1.0), (5.0, 3.6, 5.0))
        m.cube(b, (-2.2, -h - 8.2, -0.6), (1.6, 1.6, 1.6), "blossom")
    m.bone("deadwood", "body", pivot=(-1, -17, 13), rotation=(-30, -8, 12))
    m.cube("deadwood", (-1.2, -5, -1.2), (2.4, 6, 2.4), "wood")
    m.cube("deadwood", (-0.9, -9.2, -0.9), (1.8, 4.6, 1.8), "wood")
    m.bone("deadwood_twig", "deadwood", pivot=(0, -6, 0), rotation=(10, 0, -45))
    m.cube("deadwood_twig", (-0.6, -5, -0.6), (1.2, 5.5, 1.2), "wood")

    # ---------------------------------------------------------------- Hals (kurz und dick) mit Moosmähne und Ranken
    neck_sizes = [(17, 16, 7.5), (15.5, 15, 7), (14, 14, 6.5), (13, 13, 6)]
    neck = chain2(m, "neck", "body", (0, -7, -22), neck_sizes, [-24, -10, 2, 10], direction=-1, bevel=0.3, mat="hide")
    for i, (name, (w, h, length)) in enumerate(zip(neck, neck_sizes)):
        rbox(m, name, (-w / 2 + 1.5, -h / 2 - 2.5, -length - 0.5), (w - 3, 3.2, length + 1), "moss", bevel=1.0, axis=2)
        for sx in (1, -1):
            x = (w / 2 + 0.3) * sx
            m.cube(name, (x, -h / 2 + 3, -length), (0, h + 2 - i, length), "vines")

    # ---------------------------------------------------------------- Kopf (breit, stumpf) mit Geweih, Hauern und Moosbart
    m.bone("head", neck[-1], pivot=(0, 0, -6), rotation=(28, 0, 0))
    rbox(m, "head", (-8, -7.5, -15.5), (16, 12.5, 16), "hide", bevel=3.0, axis=2)          # Schädel
    rbox(m, "head", (-8.6, -9, -15.5), (17.2, 2.6, 8), "bark", bevel=1.0, axis=2)          # Brauenwulst aus Borke
    rbox(m, "head", (-6.6, -5, -22), (13.2, 9.6, 8), "hide", bevel=2.2, axis=2)            # Schnauze hinten
    rbox(m, "head", (-6.0, -4.5, -28.5), (12, 9, 7), "hide", bevel=2.0, axis=2)            # Schnauze vorn
    rbox(m, "head", (-4.2, -6.6, -27), (8.4, 2.2, 11), "bark", bevel=0.8, axis=2)          # Nasenrücken
    rbox(m, "head", (-4.6, -4.0, -29.2), (9.2, 5.4, 1.0), "hide", bevel=1.0, axis=0)       # Nasenspitze
    rbox(m, "head", (-5, -9.4, -8), (10, 2.4, 7), "moss", bevel=0.9, axis=2)               # Moos auf der Stirn
    for sx in (1, -1):
        s = "l" if sx > 0 else "r"
        m.cube("head", ((1.6 if sx > 0 else -3.6), -3.6, -29.4), (2.0, 1.2, 0.5), "nostril")
        m.cube("head", ((6.3 if sx > 0 else -8.15), -4.6, -15.9), (1.85, 2.2, 3.9), "eye")
        rbox(m, "head", ((7.2 if sx > 0 else -9.2), -1.0, -15), (2, 6.5, 10), "hide", bevel=0.7, axis=2)    # Wangen
        teeth(m, "head", 5.4 * sx, 4.5, -28, -16, 6, height=1.2, size=1.1)
        m.bone(f"ear_{s}", "head", pivot=(7.5 * sx, -5, -5), rotation=(-20, 35 * sx, 0))
        m.cube(f"ear_{s}", (-0.1, -3, 0), (0, 6, 9), "earleaf")
    m.cube("head", (-5.4, 4.52, -28), (10.8, 0.3, 12), "mouth")                           # Gaumen

    m.bone("jaw", "head", pivot=(0, 4.8, -13), rotation=(3, 0, 0))
    rbox(m, "jaw", (-6, 0, -15.5), (12, 4.5, 15.5), "hide", bevel=1.6, axis=2)
    rbox(m, "jaw", (-7, -0.4, -3), (14, 5, 4.5), "hide", bevel=1.6, axis=2)
    for sx in (1, -1):
        teeth(m, "jaw", 5.0 * sx, 0.1, -14.5, -4, 5, height=1.1, down=False, size=1.0)
        # Hauer: krumm nach oben aus den Mundwinkeln vorn
        spike(m, "jaw", f"tusk_{'l' if sx > 0 else 'r'}", (6.3 * sx, 0.8, -12.5), 5.2, 1.9, lean=12, roll=16 * sx,
              mat="tusk", steps=3)
    m.cube("jaw", (-5.2, -0.15, -15.2), (10.4, 0.3, 15), "mouth")
    m.cube("jaw", (-3.6, -0.5, -13.5), (7.2, 0.6, 11), "mouth")
    m.cube("jaw", (-4.5, 4.5, -11), (9, 7, 0), "beard")                                    # Moosbart
    m.cube("jaw", (-3.2, 4.5, -7), (6.4, 5.5, 0), "beard")
    # Geweih: kräftige Stangen, die nach oben, außen und hinten wachsen; die Enden zeigen nach vorn und oben
    for side, sx in (("l", 1), ("r", -1)):
        head_rot = _abs_rot((28, 0, 0))
        beam = [(4.4, 12.5, (0.42 * sx, -0.86, 0.28)), (3.7, 11.0, (0.58 * sx, -0.55, 0.6)),
                (3.0, 10.0, (0.36 * sx, -0.78, 0.5)), (2.4, 8.0, (0.12 * sx, -0.96, 0.25))]
        parent, parent_rot, pivot = "head", head_rot, (4.8 * sx, -8.6, -9)
        names, rots = [], []
        for i, (th, length, d) in enumerate(beam, start=1):
            name = f"antler_{side}{i}"
            rot, absr = _aim(parent_rot, head_rot, d)
            m.bone(name, parent, pivot=pivot, rotation=rot)
            t2 = th * 0.82
            m.cube(name, (-th / 2, -th / 2, -0.4), (th, th, length * 0.55 + 0.4), "antler")
            m.cube(name, (-t2 / 2, -t2 / 2, length * 0.5), (t2, t2, length * 0.5 + 0.6), "antler")
            names.append(name)
            rots.append(absr)
            parent, parent_rot, pivot = name, absr, (0, 0, length)
        # Enden: Augspross, Eissprosse, Mittelsprosse und die Krone
        tines = [(0, 3.0, 9.0, (0.12 * sx, -0.4, -0.9)), (0, 9.0, 8.0, (0.3 * sx, -0.62, -0.72)),
                 (1, 7.0, 8.5, (0.35 * sx, -0.82, -0.45)), (2, 9.0, 6.5, (0.7 * sx, -0.68, -0.1)),
                 (3, 7.5, 6.0, (-0.15 * sx, -0.8, -0.58))]
        for k, (bi, at, length, d) in enumerate(tines):
            name = f"antler_{side}_t{k}"
            rot, _ = _aim(rots[bi], head_rot, d)
            m.bone(name, names[bi], pivot=(0, 0, at), rotation=rot)
            m.cube(name, (-1.15, -1.15, -0.3), (2.3, 2.3, length * 0.55 + 0.3), "antler")
            m.cube(name, (-0.8, -0.8, length * 0.5), (1.6, 1.6, length * 0.5 + 0.4), "antler")
            if k in (2, 4):
                bush(m, name, (-2.6, -2.6, length - 2.2), (5.2, 4.0, 5.0))
            if k == 3:
                m.cube(name, (-0.65, -0.65, length - 0.3), (1.3, 1.3, 1.3), "blossom")
        rbox(m, names[1], (-2.7, -2.7, 2), (5.4, 3.6, 4.5), "moss", bevel=0.9, axis=2)   # Moos an der Stange

    # ---------------------------------------------------------------- Schwanz mit Samenkolben und Farnwedeln
    tail_sizes = [(15, 13, 12), (13, 11, 11), (11, 9.5, 11), (9.5, 8, 10), (8, 7, 10), (6.5, 6, 9), (5.5, 5, 8)]
    tail = chain2(m, "tail", "body", (0, -4, 22), tail_sizes, [-12, -8, -4, 0, 3, 4, 3], direction=1, bevel=0.3)
    for i, (name, (w, h, length)) in enumerate(zip(tail[:5], tail_sizes)):
        rbox(m, name, (-w / 2 + 1.2, -h / 2 - 2.0, -0.4), (w - 2.4, 2.6, length + 0.4), "moss", bevel=0.9, axis=2)
    rbox(m, tail[-1], (-5.5, -5.5, 4.5), (11, 11, 10), "bark", bevel=2.6, axis=2)           # Samenkolben
    rbox(m, tail[-1], (-4.2, -6.8, 6), (8.4, 1.8, 7), "moss", bevel=0.8, axis=2)
    for (x, y, z) in [(-5.9, -2, 7), (4.3, 1, 9), (-1, -7.4, 10.5)]:
        m.cube(tail[-1], (x, y, z), (1.6, 1.6, 1.6), "blossom2")
    for k, (yaw, pitch, length) in enumerate([(-42, -12, 18), (-15, -28, 22), (15, -28, 22), (42, -12, 18), (0, 8, 20)]):
        b = f"tailfrond_{k}"
        m.bone(b, tail[-1], pivot=(0, 0, 13), rotation=(pitch, yaw, 0))
        m.cube(b, (-4.5, 0, 0), (9, 0, length), "frond")

    # ---------------------------------------------------------------- Beine (dick, Wurzelzehen)
    for side, sx in (("l", 1), ("r", -1)):
        leg2(m, side, sx, "front", "body", (12.5, 4, -15),
             [(11, 13, 11, 8), (8.5, 11.5, 8.5, -14), (11, 3.5, 12, 6)], toe_count=4, toe_len=4.5, claw=1.6,
             mat="limb", claw_mat="root")
        leg2(m, side, sx, "hind", "body", (12.5, 2, 14),
             [(13, 15, 14, -12), (9.5, 12, 9.5, 34), (11.5, 3.5, 13, -22)], toe_count=4, toe_len=5, claw=1.8,
             mat="limb", claw_mat="root")

    # ---------------------------------------------------------------- Blattflügel (drei Finger, breite Flughaut)
    for side, sx in (("l", 1), ("r", -1)):
        wing2(m, side, sx, "body", (13, -11, -12), arm=22, fore=26, fingers=FINGERS, arm_th=5.0, inner_chord=30,
              fore_chord=32, sag=0.05, finger_th=2.6,
              mats={"bone": "wingbone", "inner": "leaf_inner", "fore": "leaf_fore", "outer": "leaf_outer", "claw": "root"})
    return m


# =============================================================================================
# Ausrichten von Knochen (für das Geweih)
# =============================================================================================

def _rot_zyx(rx: float, ry: float, rz: float):
    import numpy as np
    cx, sxx = math.cos(rx), math.sin(rx)
    cy, sy = math.cos(ry), math.sin(ry)
    cz, sz = math.cos(rz), math.sin(rz)
    mx = np.array([[1, 0, 0], [0, cx, -sxx], [0, sxx, cx]])
    my = np.array([[cy, 0, sy], [0, 1, 0], [-sy, 0, cy]])
    mz = np.array([[cz, -sz, 0], [sz, cz, 0], [0, 0, 1]])
    return mz @ my @ mx


def _abs_rot(rot_deg):
    return _rot_zyx(*(math.radians(v) for v in rot_deg))


def _aim(parent_abs, frame_abs, direction):
    """Drehung (Grad) eines Knochens, dessen +Z in Richtung `direction` zeigt (angegeben im Raum von `frame_abs`,
    z. B. dem Kopf), unter einem Elternknochen mit der absoluten Drehung `parent_abs`. Gibt auch die neue absolute
    Drehung zurück."""
    import numpy as np
    d = np.array(direction, dtype=float)
    d /= np.linalg.norm(d)
    world = frame_abs @ d
    local = parent_abs.T @ world
    a = -math.asin(max(-1.0, min(1.0, local[1])))
    b = math.atan2(local[0], local[2])
    rot = (math.degrees(a), math.degrees(b), 0.0)
    return rot, parent_abs @ _rot_zyx(a, b, 0.0)


# =============================================================================================
# Bemalung
# =============================================================================================

def _bark_scales(seed: int = 61, moss_cap: bool = True, belly_from: float = 0.74, cell: float = 3.0, cap: float = 0.3):
    """Borkenschuppen: längliche Rindenplatten (entlang der Körperachse) mit dunklen Furchen und leichter Wölbung;
    oben trägt der Drache einen Mantel aus Moos mit sauberem, leicht welligem Rand (statt Tarnflecken)."""
    cells = ScaleCells(cell, seed, stretch=1.9)

    def paint(ctx: Ctx):
        x, y, z = ctx.world
        down = facing_down(ctx)
        if moss_cap and ctx.face != "bottom":
            limit = cap + 0.1 * (noise3(x * 0.12, y * 0.12, z * 0.12, seed + 3) - 0.5) * 2
            if ctx.face == "top" or down < limit:
                col = _moss_color(ctx, seed + 4)
                if ctx.face != "top" and down > limit - 0.05:
                    col = mix(col, pick_smooth(ctx.pal["moss"], 0.15), 0.6)      # Schattenkante am Moosrand
                return col
        val, edge, dx, dy, dz = cells(x, y, z)
        rel = -dy * 0.8 + dz * 0.3 if ctx.face in ("west", "east") else dz * 0.6
        v = 0.42 + (val - 0.5) * 0.14 + max(-0.5, min(0.5, rel)) * 0.14
        if edge < 0.16:
            v = 0.16 + edge * 1.2                      # Furchen
        col = pick_smooth(ctx.pal["back"], v)
        if ctx.face == "bottom" or (down > belly_from and ctx.face in ("west", "east", "front", "back")):
            t = 1.0 if ctx.face == "bottom" else min(1.0, (down - belly_from) / max(1e-3, 1.0 - belly_from) * 1.5)
            col = mix(col, plate_color(ctx, "belly"), t)
        return edge_ao(ctx, col, 0.1)

    return paint


_CLUMPS = {}


def _moss_color(ctx: Ctx, seed: int):
    """Polstermoos: kleine, gewölbte Büschel (hell in der Mitte, dunkle Lücken), großflächig leicht wechselnde Töne."""
    vor = _CLUMPS.setdefault(seed, Voronoi3D(1.5, seed))
    x, y, z = ctx.world
    val, edge = vor(x, y, z)
    n = noise3(x * 0.3, y * 0.3, z * 0.3, seed)
    v = 0.3 + 0.28 * n + 0.1 * (val - 0.5) + 0.12 * min(1.0, edge * 1.6)
    if edge < 0.1:
        v -= 0.12
    if ctx.face == "top":
        v += 0.06
    elif ctx.face == "bottom":
        v -= 0.18
    elif facing_down(ctx) > 0.7:
        v -= 0.1
    return pick_smooth(ctx.pal["moss"], v)


def _moss(seed: int = 71):
    """Dichtes Moospolster."""

    def paint(ctx: Ctx):
        return _moss_color(ctx, seed)

    return paint


def _foliage(seed: int = 81):
    """Laubbüschel: einzelne Blätter in wenigen Grüntönen, dazwischen Schatten, vereinzelt ein gelbes Blatt."""
    vor = Voronoi3D(1.4, seed)

    def paint(ctx: Ctx):
        x, y, z = ctx.world
        val, edge = vor(x, y, z)
        if edge < 0.12:
            return pick_smooth(ctx.pal["leaf"], 0.14)
        v = 0.34 + 0.4 * val
        if ctx.face == "top":
            v += 0.1
        elif ctx.face == "bottom":
            v -= 0.15
        col = pick_smooth(ctx.pal["leaf"], v)
        if val > 0.95:
            col = pick_smooth(ctx.pal["leaf_old"], 0.6)
        return col

    return paint


def _vines(seed: int = 91):
    """Herabhängende Ranken: senkrechte Stränge mit Blättchen, unten unterschiedlich lang, dazwischen durchsichtig."""

    def paint(ctx: Ctx):
        lx, ly, lz = ctx.local
        oy, oz = ctx.cube.origin[1], ctx.cube.origin[2]
        h = ctx.cube.size[1]
        rel = (ly - oy) / max(h, 1e-3)
        strand = math.floor((lz - oz) / 1.6)
        offset = ((lz - oz) / 1.6) % 1.0
        length = 0.35 + 0.45 * hash01(strand, seed)
        if rel > length:
            return None
        wiggle = 0.5 + 0.18 * math.sin(ly * 0.9 + strand)
        if abs(offset - wiggle) > 0.22:
            # Blättchen an den Strängen
            if abs(offset - wiggle) < 0.5 and hash01(strand, round(ly * 1.3), seed + 1) > 0.62:
                return pick(ctx.pal["leaf"], 0.35 + 0.4 * hash01(strand, round(ly), seed + 2))
            return None
        if rel < 0.15:
            return pick(ctx.pal["moss"], 0.4)
        return pick(ctx.pal["leaf"], 0.3 + 0.3 * hash01(strand, round(ly * 2), seed))

    return paint


def _beard(seed: int = 95):
    """Bart aus hängendem Moos: Fransen, unten ungleich lang."""

    def paint(ctx: Ctx):
        lx, ly, lz = ctx.local
        ox, oy = ctx.cube.origin[0], ctx.cube.origin[1]
        w, h = ctx.cube.size[0], ctx.cube.size[1]
        rel = (ly - oy) / max(h, 1e-3)
        u = (lx - ox) / max(w, 1e-3)
        strand = math.floor((lx - ox) / 0.9)
        limit = (0.55 + 0.45 * hash01(strand, seed)) * (1.0 - 0.6 * abs(u - 0.5) ** 1.5)
        if rel > limit:
            return None
        return pick(ctx.pal["moss"], 0.25 + 0.4 * hash01(strand, round(ly * 2), seed + 1) - rel * 0.15)

    return paint


def _leaf_membrane(kind: str, seed: int = 101):
    """Flughaut wie ein riesiges Blatt: kräftiges Grün, entlang der Knochen heller (dort sitzt die Mittelrippe),
    schräg zur Spitze laufende Seitenadern, ein gezackter Rand, zum Rand hin stellenweise herbstlich verfärbt und
    hier und da ein Fraßloch."""
    from .boss_common import arm_membrane
    shape = None if kind == "outer" else arm_membrane("leaf", "vein", fore=(kind == "fore"), glow_veins=False)

    def leaf_color(ctx: Ctx, along: float, across: float, rel: float, edge_phase: float):
        n = noise3(ctx.local[0] * 0.09, ctx.local[2] * 0.09, seed * 0.13, seed)
        v = 0.42 + 0.18 * n + 0.14 * max(0.0, 1.0 - across / 6.0)
        col = pick(ctx.pal["leaf"], v)
        # Seitenadern: vom Knochen schräg nach außen zur Spitze
        phase = ((along - 1.15 * across) / 7.5) % 1.0
        if abs(phase - 0.5) < 0.05 and across > 0.8 and rel < 0.93:
            col = pick(ctx.pal["vein"], 0.5 + 0.2 * (1 - rel))
        elif abs(phase - 0.5) < 0.11 and across > 0.8 and rel < 0.93:
            col = mix(col, pick(ctx.pal["vein"], 0.45), 0.3)
        # Herbstflecken am Rand
        if rel > 0.78 and n > 0.52:
            col = mix(col, pick(ctx.pal["leaf_old"], 0.5 + 0.3 * n), min(1.0, (rel - 0.78) / 0.2) * 0.85)
        # dunkler Saum
        if rel > 0.95 - 0.05 * edge_phase:
            col = pick(ctx.pal["leaf"], 0.18)
        return col

    def holes(ctx: Ctx) -> bool:
        return noise3(ctx.local[0] * 0.3 + 3.0, ctx.local[2] * 0.3, 1.7, seed + 2) > 0.88

    def paint(ctx: Ctx):
        lx, ly, lz = ctx.local
        if kind == "outer":
            _, idx, count, gap, length, nxt, sag, z_off = ctx.cube.tag.split(":")
            gap, length, nxt, sag, z_off = (float(v) for v in (gap, length, nxt, sag, z_off))
            x = abs(lx)
            z = lz - z_off
            a = math.degrees(math.atan2(z, x))
            if a < 0 or a > gap:
                return None
            k = a / gap
            r_edge = (length + (nxt - length) * k) * (1 - sag * math.sin(math.pi * k))
            r = math.hypot(x, z)
            teeth = max(4, int(gap / 5))
            tooth = (k * teeth) % 1.0
            if r > r_edge * (1.0 - 0.07 * tooth):
                return None
            rel = r / max(r_edge, 1e-3)
            if holes(ctx):
                return None
            return leaf_color(ctx, x, z, rel, tooth)
        if shape(ctx) is None:
            return None
        ox, oy, oz = ctx.cube.origin
        w, h, d = ctx.cube.size
        span = (lx - ox) if ox >= 0 else ((ox + w) - lx)
        across = lz - oz
        chord = across / max(d, 1e-3)
        tooth = (span / 4.0) % 1.0
        if chord > 0.93 - 0.06 * tooth and chord > 0.6:
            return None
        if holes(ctx):
            return None
        return leaf_color(ctx, span, across, chord, tooth)

    return paint


def _frond(seed: int = 121):
    """Farnwedel: Mittelrippe mit gefiederten Blättchen, zur Spitze schmaler."""

    def paint(ctx: Ctx):
        lx, ly, lz = ctx.local
        ox, oz = ctx.cube.origin[0], ctx.cube.origin[2]
        w, d = ctx.cube.size[0], ctx.cube.size[2]
        u = (lx - ox) / max(w, 1e-3) * 2 - 1             # -1 .. 1 quer
        v = (lz - oz) / max(d, 1e-3)                      # 0 Ansatz, 1 Spitze
        if abs(u) < 0.1:
            return pick(ctx.pal["wood"], 0.45)
        half = (1.0 - v) * 0.95 + 0.05
        pinna = ((lz - oz) / 1.6 + abs(u) * 0.8) % 1.0
        if abs(u) > half or pinna < 0.18:
            return None
        col = pick(ctx.pal["leaf"], 0.35 + 0.35 * (1 - abs(u)) + 0.1 * v)
        if v > 0.85:
            col = mix(col, pick(ctx.pal["leaf_old"], 0.6), 0.4)
        return col

    return paint


def _ear_leaf(ctx: Ctx):
    lx, ly, lz = ctx.local
    oy, oz = ctx.cube.origin[1], ctx.cube.origin[2]
    h, d = ctx.cube.size[1], ctx.cube.size[2]
    u = (ly - oy) / max(h, 1e-3) * 2 - 1
    v = (lz - oz) / max(d, 1e-3)
    if abs(u) > math.sin(math.pi * min(1.0, v * 1.05)) * 0.95:
        return None
    if abs(u) < 0.08:
        return pick(ctx.pal["vein"], 0.4)
    return pick(ctx.pal["leaf"], 0.4 + 0.3 * (1 - abs(u)))


def _antler(seed: int = 131):
    """Geweih aus altem Holz: Längsrillen, Ringe, Flechten, zur Spitze heller."""

    def paint(ctx: Ctx):
        x, y, z = ctx.world
        groove = abs(math.sin((ctx.s + ctx.t * 0.15) * 1.7))
        v = 0.3 + 0.25 * groove + 0.1 * noise3(x * 0.4, y * 0.4, z * 0.4, seed)
        col = pick(ctx.pal["wood"], v)
        if noise3(x * 0.6 + 5, y * 0.6, z * 0.6, seed + 1) > 0.74:
            col = mix(col, pick(ctx.pal["lichen"], 0.6), 0.7)
        return col

    return paint


def _wood(seed: int = 141):
    def paint(ctx: Ctx):
        x, y, z = ctx.world
        groove = abs(math.sin(ctx.s * 1.9 + noise3(x * 0.2, y * 0.2, z * 0.2, seed) * 3))
        col = pick(ctx.pal["bark"], 0.25 + 0.3 * groove)
        if ctx.face in ("top", "bottom"):
            ring = math.hypot(ctx.s - ctx.w / 2, ctx.t - ctx.h / 2)
            col = pick(ctx.pal["wood"], 0.45 + 0.2 * (int(ring) % 2))
        return col

    return paint


def _bark(seed: int = 151):
    """Grobe Borke (Brauen, Nasenrücken, Samenkolben): tiefe Furchen, Moosflecken."""
    vor = Voronoi3D(2.2, seed)

    def paint(ctx: Ctx):
        x, y, z = ctx.world
        val, edge = vor(x * 1.6, y, z * 0.7)
        if edge < 0.22:
            return pick(ctx.pal["bark"], 0.08 + edge * 0.5)
        col = pick(ctx.pal["bark"], 0.35 + 0.35 * val)
        if noise3(x * 0.3, y * 0.3, z * 0.3, seed + 1) > 0.66:
            col = mix(col, pick(ctx.pal["moss"], 0.45), 0.7)
        return col

    return paint


def _mushroom(ctx: Ctx):
    """Leuchtende Baumpilze: türkis, mit hellem Rand und Lamellen an der Unterseite."""
    if ctx.face == "bottom":
        stripe = (ctx.s % 2 == 0)
        return pick(ctx.pal["mushroom"], 0.3 if stripe else 0.45)
    edge = ctx.s == 0 or ctx.s == ctx.w - 1 or ctx.t == 0 or ctx.t == ctx.h - 1
    col = pick(ctx.pal["mushroom"], 0.85 if edge else 0.55 + 0.2 * hash01(ctx.u, ctx.v))
    ctx.glow = col
    return col


def _wing_bone(seed: int = 161):
    """Flügelknochen wie junge Zweige: grünlich-braune Rinde."""

    def paint(ctx: Ctx):
        v = 0.35 + 0.2 * abs(math.sin(ctx.s * 1.3)) + 0.1 * hash01(ctx.u, ctx.v, seed)
        return mix(pick(ctx.pal["wood"], v), pick(ctx.pal["moss"], 0.4), 0.25)

    return paint


def _blossom(key: str):
    def paint(ctx: Ctx):
        edge = ctx.s == 0 or ctx.t == 0 or ctx.s == ctx.w - 1 or ctx.t == ctx.h - 1
        col = pick_smooth(ctx.pal[key], 0.55 if edge else 0.85)
        ctx.glow = pick_smooth(ctx.pal[key], 0.3)
        return col

    return paint


def materials(variant: str) -> dict:
    return {
        "scales": _bark_scales(),
        "hide": _bark_scales(seed=63, cell=2.4, cap=0.12),
        "limb": _bark_scales(seed=65, moss_cap=False, belly_from=2.0, cell=2.6),
        "plates": plates("belly"),
        "moss": _moss(),
        "foliage": _foliage(),
        "vines": _vines(),
        "beard": _beard(),
        "bark": _bark(),
        "wood": _wood(),
        "antler": _antler(),
        "wingbone": _wing_bone(),
        "blossom": _blossom("blossom"),
        "blossom2": _blossom("blossom2"),
        "mushroom": _mushroom,
        "root": keratin("claw", base=0.25, span=0.4),
        "claw": keratin("claw", base=0.35, span=0.5),
        "tusk": keratin("tusk", base=0.35, span=0.6),
        "teeth": solid("teeth", 0.8, noise=0.06),
        "eye": eye("eye"),
        "nostril": solid("bark", 0.04, noise=0.0, ao=0.0),
        "mouth": mouth("mouth"),
        "earleaf": _ear_leaf,
        "frond": _frond(),
        "leaf_inner": _leaf_membrane("inner"),
        "leaf_fore": _leaf_membrane("fore"),
        "leaf_outer": _leaf_membrane("outer"),
        "none": transparent_painter(),
    }
