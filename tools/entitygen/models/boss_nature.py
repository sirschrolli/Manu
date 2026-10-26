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
from ..paint import Ctx, Voronoi3D, flat_painter, hash01, mix, noise3, pick, ramp, transparent_painter
from .boss_common import (belly_plate, chain, facing_ry, glow_painter, glowing_eye, leg, mouth_painter, plates_painter,
                          wing)

PALETTES = {
    "verdantis": {
        "back": ramp("#2e261c", "#a8967a"), "belly": ramp("#3e4a22", "#b4c47a"),
        "moss": ramp("#1c3a14", "#86c44a"), "leaf": ramp("#14361a", "#7cc24a"),
        "leaf_old": ramp("#4a3a12", "#d4a83a"), "vein": ramp("#2e5a10", "#d8ff8a"),
        "bark": ramp("#1e1610", "#7a6448"), "wood": ramp("#2a1e14", "#a08a64"),
        "lichen": ramp("#4a5a3a", "#c8d4a0"), "blossom": ramp("#c0407a", "#ffe0f0"),
        "blossom2": ramp("#d09a10", "#fff6b0"), "glow": ramp("#2a8a30", "#c8ff9a"),
        "mushroom": ramp("#1a6a6a", "#b0fff0"), "claw": ramp("#1a140e", "#8a7258"),
        "eye": ramp("#e0a010", "#fffcc0"), "mouth": ramp("#2a1408", "#a0503a"),
    },
}

TEX = (512, 512)


def build() -> Model:
    m = Model("verdantis", *TEX)

    # ---------------------------------------------------------------- Rumpf (breit und schwer)
    m.bone("body", pivot=(0, -8, 0))
    m.cube("body", (-15, -12, -23), (30, 22, 46), "scales")                     # Rumpf
    m.cube("body", (-12.5, -16, -20), (25, 4.5, 37), "moss")                    # Moospolster auf dem Rücken
    m.cube("body", (-9, -18.5, -14), (18, 3, 23), "moss")
    m.cube("body", (-13, 8, -22), (26, 5, 26), "plates")                        # Brust
    m.cube("body", (-12, 8, 4), (24, 3.5, 17), "plates")                        # Bauch hinten
    m.cube("body", (-17, -9, -21), (3, 14, 14), "scales")                       # Schultern
    m.cube("body", (14, -9, -21), (3, 14, 14), "scales")
    m.cube("body", (-16.5, -7, 8), (2.5, 14, 15), "scales")                     # Hüften
    m.cube("body", (14, -7, 8), (2.5, 14, 15), "scales")
    # der Garten auf dem Rücken: Laubbüschel, Blüten, Farne
    for (x, y, z, w, h, d) in [(-10, -22, -17, 8, 5, 8), (2, -23.5, -11, 9, 6, 9), (-7, -22, -2, 7, 4.5, 7),
                               (4, -21, 5, 8, 5, 7), (-4, -20, 12, 7, 4, 6), (8, -18.5, -19, 5, 3.5, 5)]:
        m.cube("body", (x, y, z), (w, h, d), "foliage")
    for (x, y, z) in [(-7, -23, -15), (5, -24.8, -8), (-4.5, -23, 0), (7, -22.2, 7), (-1.5, -21.2, 14), (-9, -23, -12),
                      (3, -24.8, -12), (9.5, -19.8, -17)]:
        m.cube("body", (x, y, z), (1.6, 1.6, 1.6), "blossom")
    for (x, y, z) in [(-2, -22.6, -6), (10, -22.2, 3), (-8, -21.5, 6)]:
        m.cube("body", (x, y, z), (1.6, 1.6, 1.6), "blossom2")
    # Baumpilze an den Flanken (wie an einem alten Stamm)
    for sx in (1, -1):
        for (y, z, w, d) in [(-4, -8, 3, 5), (-1, -3, 2.5, 4), (-6, 9, 3, 6), (0, 14, 2, 3.5)]:
            x = 15 if sx > 0 else -15 - w
            m.cube("body", (x, y, z), (w, 1.2, d), "mushroom")
    # zwei junge Bäume wachsen auf dem Rücken (Kronen aus mehreren Laubbüscheln), dazu ein abgestorbener Ast
    for i, (x, z, h, lean, yaw) in enumerate([(-4, -14, 11, -10, -20), (5, 2, 9, -16, 25)]):
        b = f"tree_{i}"
        m.bone(b, "body", pivot=(x, -18, z), rotation=(lean, yaw, 0))
        m.cube(b, (-1.3, -h, -1.3), (2.6, h + 1, 2.6), "wood")
        m.bone(f"{b}_bough", b, pivot=(0, -h * 0.55, 0), rotation=(0, 0, 42 if i == 0 else -42))
        m.cube(f"{b}_bough", (-0.8, -5.5, -0.8), (1.6, 6, 1.6), "wood")
        m.cube(f"{b}_bough", (-3.2, -9, -3.0), (6.4, 4.2, 6), "foliage")
        m.cube(b, (-4.5, -h - 4.5, -4.2), (9, 5, 8.4), "foliage")
        m.cube(b, (-3.0, -h - 7.5, -2.5), (6.4, 3.4, 5.6), "foliage")
        m.cube(b, (1.5, -h - 3.0, -1.0), (5.0, 3.5, 5.0), "foliage")
        m.cube(b, (-2.2, -h - 8.2, -0.6), (1.6, 1.6, 1.6), "blossom")
    m.bone("deadwood", "body", pivot=(-1, -17, 13), rotation=(-30, -8, 12))
    m.cube("deadwood", (-1.1, -9, -1.1), (2.2, 10, 2.2), "wood")
    m.bone("deadwood_twig", "deadwood", pivot=(0, -6, 0), rotation=(10, 0, -45))
    m.cube("deadwood_twig", (-0.6, -5, -0.6), (1.2, 5.5, 1.2), "wood")

    # ---------------------------------------------------------------- Hals (kurz und dick) mit Moosmähne und Ranken
    neck_sizes = [(17, 16, 7.5), (15.5, 15, 7), (14, 14, 6.5), (13, 13, 6)]
    neck = chain(m, "neck", "body", (0, -7, -22), neck_sizes, [-24, -10, 2, 10], direction=-1, belly="plates")
    for i, (name, (w, h, length)) in enumerate(zip(neck, neck_sizes)):
        m.cube(name, (-w / 2 + 1.5, -h / 2 - 2.5, -length - 0.5), (w - 3, 3, length + 1), "moss")
        for sx in (1, -1):
            x = (w / 2 + 0.3) * sx
            m.cube(name, (x, -h / 2 + 3, -length), (0, h + 4 - i, length), "vines")

    # ---------------------------------------------------------------- Kopf (breit, stumpf) mit Geweih und Moosbart
    m.bone("head", neck[-1], pivot=(0, 0, -6), rotation=(28, 0, 0))
    m.cube("head", (-8, -7.5, -16), (16, 13, 16), "scales")                     # Schädel
    m.cube("head", (-8.6, -9, -15.5), (17.2, 2.5, 8), "bark")                   # Brauenwulst aus Borke
    m.cube("head", (-6.5, -4.5, -28.5), (13, 9, 13), "scales")                  # stumpfe Schnauze
    m.cube("head", (-4.5, -6.5, -27), (9, 2.5, 10.5), "bark")                   # Nasenrücken
    m.cube("head", (-5, -4.2, -28.8), (3, 2, 1.5), "nostril")
    m.cube("head", (2, -4.2, -28.8), (3, 2, 1.5), "nostril")
    m.cube("head", (-6, 4.0, -28), (12, 1.5, 12), "teeth")
    m.cube("head", (-6, 3.9, -27.9), (12, 0.2, 13), "mouth")
    m.cube("head", (7.6, -4.2, -14.5), (1, 2.4, 4), "eye")
    m.cube("head", (-8.6, -4.2, -14.5), (1, 2.4, 4), "eye")
    m.cube("head", (7.8, -1, -15), (1.6, 7, 10), "scales")                      # Wangen
    m.cube("head", (-9.4, -1, -15), (1.6, 7, 10), "scales")
    m.cube("head", (-5, -8.8, -8), (10, 2, 7), "moss")                          # Moos auf der Stirn
    # Ohrwedel (Blätter)
    for sx in (1, -1):
        m.bone(f"ear_{'l' if sx > 0 else 'r'}", "head", pivot=(7.5 * sx, -5, -5), rotation=(-20, 35 * sx, 0))
        m.cube(f"ear_{'l' if sx > 0 else 'r'}", (-0.1, -3, 0), (0, 6, 9), "earleaf")
    m.bone("jaw", "head", pivot=(0, 4.5, -13), rotation=(3, 0, 0))
    m.cube("jaw", (-6, 0, -15.5), (12, 4.5, 15.5), "scales")
    m.cube("jaw", (-5.2, -1.3, -15), (10.4, 1.3, 10), "teeth")
    m.cube("jaw", (-4.6, -0.2, -14.5), (9.2, 0.4, 13), "mouth")
    m.cube("jaw", (-4.5, 4.5, -11), (9, 7, 0), "beard")                         # Moosbart
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
            m.cube(name, (-th / 2, -th / 2, -0.4), (th, th, length + 0.8), "antler")
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
            m.cube(name, (-1.1, -1.1, -0.3), (2.2, 2.2, length + 0.5), "antler")
            if k in (2, 4):
                m.cube(name, (-2.4, -2.4, length - 2.0), (4.8, 3.6, 4.6), "foliage")
                m.cube(name, (-1.6, -3.6, length - 0.6), (3.2, 2.0, 3.2), "foliage")
            if k == 3:
                m.cube(name, (-0.9, -0.9, length - 0.4), (1.8, 1.8, 1.8), "blossom")
        m.cube(names[1], (-2.7, -2.7, 2), (5.4, 3.6, 4.5), "moss")              # Moos an der Stange

    # ---------------------------------------------------------------- Schwanz mit Samenkolben und Farnwedeln
    tail_sizes = [(15, 13, 12), (13, 11, 11), (11, 9.5, 11), (9.5, 8, 10), (8, 7, 10), (6.5, 6, 9), (5.5, 5, 8)]
    tail = chain(m, "tail", "body", (0, -4, 22), tail_sizes, [-12, -8, -4, 0, 3, 4, 3], direction=1, belly="plates")
    for i, (name, (w, h, length)) in enumerate(zip(tail[:5], tail_sizes)):
        m.cube(name, (-w / 2 + 1.2, -h / 2 - 2.0, 0), (w - 2.4, 2.5, length), "moss")
    m.cube(tail[-1], (-5.5, -5.5, 4.5), (11, 11, 10), "bark")                    # Samenkolben
    m.cube(tail[-1], (-4.2, -6.8, 6), (8.4, 1.5, 7), "moss")
    for (x, y, z) in [(-5.9, -2, 7), (4.3, 1, 9), (-1, -7, 10.5)]:
        m.cube(tail[-1], (x, y, z), (1.6, 1.6, 1.6), "blossom2")
    for k, (yaw, pitch, length) in enumerate([(-42, -12, 18), (-15, -28, 22), (15, -28, 22), (42, -12, 18), (0, 8, 20)]):
        b = f"tailfrond_{k}"
        m.bone(b, tail[-1], pivot=(0, 0, 13), rotation=(pitch, yaw, 0))
        m.cube(b, (-4.5, 0, 0), (9, 0, length), "frond")

    # ---------------------------------------------------------------- Beine (dick, Wurzelzehen)
    for side, sx in (("l", 1), ("r", -1)):
        leg(m, side, sx, "front", "body", (12.5, 4, -15),
            [(11, 13, 11, 8), (8.5, 11.5, 8.5, -14), (11, 3.5, 12, 6)], toes=4, toe_len=4.5, claw_mat="root")
        leg(m, side, sx, "hind", "body", (12.5, 2, 14),
            [(13, 15, 14, -12), (9.5, 12, 9.5, 34), (11.5, 3.5, 13, -22)], toes=4, toe_len=5, claw_mat="root")

    # ---------------------------------------------------------------- Blattflügel (drei Finger, breite Flughaut)
    for side, sx in (("l", 1), ("r", -1)):
        wing(m, side, sx, "body", (13, -11, -12), arm=22, fore=26,
             fingers=[(46, 10), (43, 42), (36, 74)], arm_th=5.0, inner_chord=30, fore_chord=32, sag=0.05,
             mats={"bone": "wingbone", "inner": "leaf_inner", "fore": "leaf_fore", "outer": "leaf_outer",
                   "claw": "root"})
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

def _bark_scales(seed: int = 61):
    """Borkenschuppen: grobe, längs gefurchte Rinde (Furchen folgen der Körperachse), auf den Oberseiten Moos,
    vereinzelt Flechten und glimmende Lichtpunkte (Glühwürmchen-Moos)."""
    vor = Voronoi3D(3.6, seed)

    def paint(ctx: Ctx):
        x, y, z = ctx.world
        if ctx.face == "bottom":
            return belly_plate(ctx, "belly")
        ry = facing_ry(ctx)
        val, edge = vor(x * 1.4, y, z * 0.55)            # längliche Borkenplatten
        v = 0.38 + (val - 0.5) * 0.3 + (1.0 - ry) * 0.08
        if edge < 0.2:
            v = 0.12 + edge * 0.6                          # tiefe Furchen
        col = pick(ctx.pal["back"], v)
        # Moos wächst oben (Oberseiten und obere Flanken)
        top = 1.0 if ctx.face == "top" else max(0.0, 0.55 - ry) * 1.8
        mossy = noise3(x * 0.18, y * 0.18, z * 0.18, seed + 3)
        if top > 0 and mossy + top * 0.35 > 0.72 and edge > 0.12:
            mv = 0.35 + 0.4 * noise3(x * 0.7, y * 0.7, z * 0.7, seed + 4)
            col = mix(col, pick(ctx.pal["moss"], mv), 0.85)
            if hash01(round(x * 2), round(y * 2), round(z * 2), seed) > 0.985:
                g = pick(ctx.pal["glow"], 0.9)
                ctx.glow = g
                return g
        elif noise3(x * 0.4 + 9, y * 0.4, z * 0.4, seed + 5) > 0.8 and edge > 0.25:
            col = mix(col, pick(ctx.pal["lichen"], 0.55), 0.6)
        if ry > 0.78 and ctx.face in ("west", "east", "front", "back"):
            col = mix(col, pick(ctx.pal["belly"], 0.45), (ry - 0.78) / 0.22 * 0.7)
        return col

    return paint


def _moss(seed: int = 71):
    """Dichtes Moospolster mit kleinen Blüten und Glühpunkten."""

    def paint(ctx: Ctx):
        x, y, z = ctx.world
        n = noise3(x * 0.5, y * 0.5, z * 0.5, seed)
        fine = hash01(ctx.u, ctx.v, seed)
        col = pick(ctx.pal["moss"], 0.3 + 0.45 * n + (fine - 0.5) * 0.18)
        if ctx.face not in ("top",) and facing_ry(ctx) > 0.75:
            col = pick(ctx.pal["moss"], 0.15 + 0.2 * n)
        r = hash01(round(x * 1.5), round(y * 1.5), round(z * 1.5), seed + 1)
        if r > 0.975:
            return pick(ctx.pal["blossom" if r > 0.988 else "blossom2"], 0.7)
        if r < 0.012:
            g = pick(ctx.pal["glow"], 0.85)
            ctx.glow = g
            return g
        return col

    return paint


def _foliage(seed: int = 81):
    """Laubbüschel: einzelne Blätter in verschiedenen Grüntönen, dazwischen Schatten, ein paar Blätter schon gelb."""
    vor = Voronoi3D(1.3, seed)

    def paint(ctx: Ctx):
        x, y, z = ctx.world
        val, edge = vor(x, y, z)
        if edge < 0.16:
            return pick(ctx.pal["leaf"], 0.08)
        v = 0.25 + 0.6 * val
        if ctx.face == "top":
            v += 0.1
        elif ctx.face == "bottom":
            v -= 0.15
        col = pick(ctx.pal["leaf"], v)
        if val > 0.93:
            col = pick(ctx.pal["leaf_old"], 0.6)
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
        length = 0.45 + 0.55 * hash01(strand, seed)
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
        col = pick(ctx.pal["leaf"], 0.3 + 0.3 * hash01(strand, round(ly * 2), seed))
        if rel > length - 0.04 and hash01(strand, seed + 7) > 0.6:
            g = pick(ctx.pal["glow"], 0.8)                 # leuchtende Rankenenden
            ctx.glow = g
            return g
        return col

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


def materials(variant: str) -> dict:
    return {
        "scales": _bark_scales(),
        "plates": plates_painter("belly"),
        "moss": _moss(),
        "foliage": _foliage(),
        "vines": _vines(),
        "beard": _beard(),
        "bark": _bark(),
        "wood": _wood(),
        "antler": _antler(),
        "wingbone": _wing_bone(),
        "blossom": glow_painter("blossom", 0.75),
        "blossom2": glow_painter("blossom2", 0.75),
        "mushroom": _mushroom,
        "root": flat_painter("claw", 0.45, 0.1),
        "claw": flat_painter("claw", 0.6),
        "teeth": flat_painter("lichen", 0.9, 0.05),
        "eye": glowing_eye("eye"),
        "nostril": flat_painter("bark", 0.05, 0.02),
        "mouth": mouth_painter(),
        "earleaf": _ear_leaf,
        "frond": _frond(),
        "leaf_inner": _leaf_membrane("inner"),
        "leaf_fore": _leaf_membrane("fore"),
        "leaf_outer": _leaf_membrane("outer"),
        "none": transparent_painter(),
    }
