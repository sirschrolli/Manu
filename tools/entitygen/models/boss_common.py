"""
Gemeinsame Bausteine der Drachen-Bosse: Ketten (Hals, Schwanz, Schlangenkörper), Beine mit Zehen,
Flügel mit Fingern und Flughäuten, Stacheln – dazu Maler für Schuppen, Risse, Flughäute und Kristalle.

Die Bosse werden in „Grundgröße“ (etwa 10–12 Blöcke lang) gebaut und im Spiel vergrößert (ca. 25 Blöcke).
Knochen-Namen werden im Java-Code der Modelle für die Animationen verwendet.
"""
from __future__ import annotations

import math

from ..geometry import Model
from ..paint import Ctx, Voronoi3D, hash01, mix, noise3, pick


# =============================================================================================
# Geometrie
# =============================================================================================

def chain(m: Model, prefix: str, parent: str, start, sizes: list[tuple[float, float, float]], rots: list[float],
          material: str = "scales", direction: int = 1, spikes: list[float] | None = None, spike_mat: str = "spike",
          belly: str | None = None, yaw_rots: list[float] | None = None) -> list[str]:
    """Kette aus Segmenten (Breite, Höhe, Länge) entlang +Z (direction=1, Schwanz) oder -Z (direction=-1, Hals).

    Jedes Segment ist ein eigener Knochen; der Drehpunkt liegt am Anfang des Segments.
    spikes[i] = Höhe des Rückenstachels auf Segment i (0 = keiner). belly = Material der Bauchplatte."""
    names = []
    pivot = start
    for i, (w, h, length) in enumerate(sizes):
        name = f"{prefix}{i + 1}"
        yaw = yaw_rots[i] if yaw_rots else 0.0
        m.bone(name, parent, pivot=pivot, rotation=(rots[i], yaw, 0))
        z0 = 0 if direction > 0 else -length
        m.cube(name, (-w / 2, -h / 2, z0 - 0.5), (w, h, length + 1.0), material)
        if belly:
            m.cube(name, (-w / 2 + 1, h / 2 - 0.5, z0 + 0.5), (w - 2, 1.5, length - 1), belly)
        if spikes and i < len(spikes) and spikes[i] > 0:
            s = spikes[i]
            base = max(1.5, min(3.0, w * 0.22))
            m.cube(name, (-base / 2, -h / 2 - s, z0 + length * 0.3), (base, s + 0.5, base * 1.4), spike_mat)
        names.append(name)
        parent = name
        pivot = (0, 0, length * direction)
    return names


def leg(m: Model, side: str, sx: int, kind: str, parent: str, hip, segs: list[tuple], toes: int = 3,
        toe_len: float = 3.0, claw_mat: str = "claw", mat: str = "scales") -> None:
    """Bein aus drei Teilen: <kind>_leg_<side> (Ober), <kind>_shin_<side> (Unter), <kind>_foot_<side> (Fuß).

    segs = [(breite, länge, tiefe, winkel_x), (…), (…)]; das letzte Element ist der Fuß (flach, nach vorn)."""
    (w1, l1, d1, r1), (w2, l2, d2, r2), (w3, h3, d3, r3) = segs
    up = f"{kind}_leg_{side}"
    shin = f"{kind}_shin_{side}"
    foot = f"{kind}_foot_{side}"
    m.bone(up, parent, pivot=(hip[0] * sx, hip[1], hip[2]), rotation=(r1, 0, 0))
    m.cube(up, (-w1 / 2, -w1 * 0.35, -d1 / 2), (w1, l1 + w1 * 0.35, d1), mat)
    m.bone(shin, up, pivot=(0, l1 - 0.5, 0), rotation=(r2, 0, 0))
    m.cube(shin, (-w2 / 2, -1, -d2 / 2), (w2, l2 + 1, d2), mat)
    m.bone(foot, shin, pivot=(0, l2 - 0.5, 0), rotation=(r3, 0, 0))
    m.cube(foot, (-w3 / 2, 0, -d3 * 0.7), (w3, h3, d3), mat)
    spacing = w3 / toes
    for t in range(toes):
        cx = -w3 / 2 + spacing * (t + 0.5)
        m.cube(foot, (cx - 0.75, h3 * 0.35, -d3 * 0.7 - toe_len), (1.5, h3 * 0.65, toe_len), claw_mat)


def wing(m: Model, side: str, sx: int, parent: str, shoulder, arm: float, fore: float,
         fingers: list[tuple[float, float]], arm_th: float = 4.0, inner_chord: float = 30.0,
         fore_chord: float = 30.0, mats: dict | None = None, thumb: bool = True, sag: float = 0.16) -> None:
    """Flügel: wing_<s> (Oberarm) -> wing_<s>_fore (Unterarm) -> wing_<s>_hand (Handgelenk) -> wing_<s>_f<n> (Finger).

    fingers = [(länge, winkel_y_grad), …] von vorn nach hinten (Winkel 0 = gerade nach außen, 90 = nach hinten).
    Jeder Finger trägt die Flughaut bis zum nächsten Finger: ein Keil vom Handgelenk zur Hinterkante zwischen den
    beiden Fingerspitzen (leicht eingebuchtet). Der letzte Finger trägt die Haut zurück bis zum Unterarm.
    Die Form steht im Tag des Quaders und wird von finger_membrane() gemalt."""
    mats = mats or {}
    bone_mat = mats.get("bone", "scales")
    x0 = lambda length: 0 if sx > 0 else -length  # noqa: E731
    w = f"wing_{side}"
    m.bone(w, parent, pivot=(shoulder[0] * sx, shoulder[1], shoulder[2]))
    m.cube(w, (x0(arm), -arm_th / 2, -arm_th / 2), (arm, arm_th, arm_th), bone_mat)
    m.cube(w, (x0(arm), 0, arm_th / 2 - 0.5), (arm, 0, inner_chord), mats.get("inner", "membrane_inner"))
    f = f"{w}_fore"
    m.bone(f, w, pivot=(arm * sx, 0, 0))
    th = arm_th * 0.75
    m.cube(f, (x0(fore), -th / 2, -th / 2), (fore, th, th), bone_mat)
    m.cube(f, (x0(fore), 0, th / 2 - 0.5), (fore, 0, fore_chord), mats.get("fore", "membrane_fore"))
    h = f"{w}_hand"
    m.bone(h, f, pivot=(fore * sx, 0, 0))
    m.cube(h, (-2.5, -2.5, -2.5), (5, 5, 5), bone_mat)
    if thumb:
        m.cube(h, (-1, -1, -6), (2, 2, 4), mats.get("claw", "claw"))
    for i, (length, ang) in enumerate(fingers, start=1):
        fn = f"{w}_f{i}"
        m.bone(fn, h, pivot=(0, 0, 0), rotation=(0, -ang * sx, 0))
        fth = max(1.0, 2.2 - i * 0.35)
        m.cube(fn, (x0(length), -fth / 2, -fth / 2), (length, fth, fth), bone_mat)
        if i < len(fingers):
            gap = fingers[i][1] - ang
            nxt = fingers[i][0]
        else:
            gap = min(90.0, 175.0 - ang)
            nxt = fore_chord * 1.05
        gap = max(8.0, gap)
        # Ausdehnung des Keils im Finger-Raum (x entlang, z nach hinten)
        xmax = zmax = 0.0
        for k in range(41):
            a = math.radians(gap * k / 40)
            r = (length + (nxt - length) * k / 40) * (1 - sag * math.sin(math.pi * k / 40))
            xmax = max(xmax, r * math.cos(a))
            zmax = max(zmax, r * math.sin(a))
        z_off = fth / 2 - 0.4
        cube = m.cube(fn, (x0(xmax), 0, z_off), (math.ceil(xmax), 0, math.ceil(zmax) + 1), mats.get("outer", "membrane_outer"))
        cube.tag = f"finger:{i}:{len(fingers)}:{gap:.2f}:{length:.2f}:{nxt:.2f}:{sag:.3f}:{z_off:.2f}"


def spike_row(m: Model, bone: str, zs: list[float], y: float, heights: list[float], width: float = 2.5,
              mat: str = "spike", lean: float = 0.0) -> None:
    for z, h in zip(zs, heights):
        m.cube(bone, (-width / 2, y - h, z), (width, h + 0.5, width * 1.5 + lean), mat)


# =============================================================================================
# Maler
# =============================================================================================

def facing_ry(ctx: Ctx) -> float:
    """0 = Oberseite des Quaders, 1 = Unterseite (Ober-/Unterfläche: 0 bzw. 1)."""
    if ctx.face == "top":
        return 0.0
    if ctx.face == "bottom":
        return 1.0
    oy, h = ctx.cube.origin[1], ctx.cube.size[1]
    return 0.0 if h <= 0 else (ctx.local[1] - oy) / h


def cracked_scales(cell: float = 4.0, seed: int = 21, crack_key: str = "crack", crack_width: float = 0.017,
                   freq: float = 0.085, belly_from: float = 0.74, glow_cracks: bool = True, branch: float = 0.6,
                   heat: float = 0.45) -> callable:
    """Dunkle Schuppen mit leuchtenden Adern (Lava, Blitz, Erz). Die Adern sind Höhenlinien eines großräumigen
    Rauschens – so entstehen zusammenhängende, gewundene Risse statt verstreuter Punkte; feinere Seitenäste zweigen
    nur in der Nähe der Hauptadern ab. Um die Adern herum glimmen die Schuppen leicht (heat)."""
    vor = Voronoi3D(cell, seed)

    def paint(ctx: Ctx):
        x, y, z = ctx.world
        if ctx.face == "bottom":
            return belly_plate(ctx, "belly")
        ry = facing_ry(ctx)
        val, edge = vor(x, y, z)
        d1 = abs(noise3(x * freq, y * freq, z * freq, seed) - 0.5)
        d2 = abs(noise3(x * freq * 2.4 + 7.1, y * freq * 2.4, z * freq * 2.4 - 3.3, seed + 5) - 0.5)
        main = d1 < crack_width
        side = branch > 0 and d2 < crack_width * branch and d1 < crack_width * 4.5
        if main or side:
            strength = 1.0 - (d1 / crack_width if main else d2 / (crack_width * branch))
            col = pick(ctx.pal[crack_key], 0.5 + 0.45 * strength)
            if glow_cracks:
                ctx.glow = col
            return col
        v = 0.36 + (val - 0.5) * 0.24 + (1.0 - ry) * 0.1
        if edge < 0.22:
            v -= 0.17 * (1.0 - edge / 0.22)
        elif edge > 0.75:
            v += 0.07
        col = pick(ctx.pal["back"], v)
        warm = max(0.0, 1.0 - d1 / (crack_width * 3.2))
        if warm > 0 and heat > 0:
            col = mix(col, pick(ctx.pal[crack_key], 0.15), warm * heat)
        if ry > belly_from and ctx.face in ("west", "east", "front", "back"):
            t = (ry - belly_from) / (1.0 - belly_from)
            col = mix(col, pick(ctx.pal["belly"], 0.5 + (val - 0.5) * 0.2), t * 0.8)
        return col

    return paint


def belly_plate(ctx: Ctx, key: str = "belly", plate: float = 2.5, glow_from: float | None = None,
                glow_key: str = "crack") -> tuple:
    z = ctx.world[2]
    k = (z / plate) % 1.0
    v = 0.6 + (0.1 if k < 0.5 else 0.0)
    if k > 0.85:
        v = 0.32
    x = ctx.local[0] - ctx.cube.origin[0]
    w = max(ctx.cube.size[0], 1e-3)
    edge = min(x, w - x) / w
    if edge < 0.12:
        v -= 0.14
    col = pick(ctx.pal[key], v)
    if glow_from is not None and ctx.world[2] < glow_from and k <= 0.85 and edge > 0.12:
        g = pick(ctx.pal[glow_key], 0.55 + 0.3 * (1 - k))
        ctx.glow = g
        return mix(col, g, 0.65)
    return col


def plates_painter(key: str = "belly", glow_from: float | None = None) -> callable:
    def paint(ctx: Ctx):
        if ctx.face in ("bottom", "top", "west", "east"):
            return belly_plate(ctx, key, glow_from=glow_from)
        return pick(ctx.pal[key], 0.45)

    return paint


def finger_membrane(key: str = "membrane", vein_key: str = "vein", tatter: float = 0.0, glow_veins: bool = False,
                    seed: int = 3) -> callable:
    """Flughaut eines Fingers (siehe wing()): Keil vom Handgelenk bis zur eingebuchteten Hinterkante zwischen
    dieser und der nächsten Fingerspitze, mit fächerförmigen Adern und leicht ausgefranstem Rand."""

    def paint(ctx: Ctx):
        lx, ly, lz = ctx.local
        _, idx, count, gap, length, nxt, sag, z_off = ctx.cube.tag.split(":")
        idx, count = int(idx), int(count)
        gap, length, nxt, sag, z_off = (float(v) for v in (gap, length, nxt, sag, z_off))
        x = abs(lx)
        z = lz - z_off
        a = math.degrees(math.atan2(z, x))
        if a < 0 or a > gap:
            return None
        k = a / gap
        r_edge = (length + (nxt - length) * k) * (1 - sag * math.sin(math.pi * k))
        r = math.hypot(x, z)
        if r > r_edge:
            return None
        rel = r / max(r_edge, 1e-3)
        if tatter > 0 and rel > 0.9:
            if noise3(lx * 0.45, lz * 0.45, idx * 3.1, seed) < tatter:
                return None
        v = 0.42 + 0.1 * math.sin(lz * 0.3 + lx * 0.12) + (1 - rel) * 0.12
        col = pick(ctx.pal[key], v)
        # Adern fächern vom Handgelenk aus (gleichmäßig über den Keil verteilt)
        veins = max(2, int(gap / 14))
        fan = k * veins
        if abs(fan - round(fan)) < 0.05 + 0.08 * (1 - rel) and rel > 0.08 and 0 < round(fan) < veins:
            col = pick(ctx.pal[vein_key], 0.5)
            if glow_veins:
                ctx.glow = col
        if rel > 0.965:
            col = pick(ctx.pal[key], 0.2)
        return col

    return paint


def arm_membrane(key: str = "membrane", vein_key: str = "vein", fore: bool = False, glow_veins: bool = False) -> callable:
    """Innere Flughaut am Ober-/Unterarm: zur Körperseite hin voll, Hinterkante leicht geschwungen."""

    def paint(ctx: Ctx):
        lx, ly, lz = ctx.local
        ox, oy, oz = ctx.cube.origin
        w, h, d = ctx.cube.size
        span = (lx - ox) / w if ox >= 0 else ((ox + w) - lx) / w     # 0 innen, 1 außen
        chord = (lz - oz) / max(d, 1e-3)
        limit = (0.98 - 0.12 * span) if not fore else (0.95 - 0.35 * span ** 1.3)
        limit -= 0.05 * abs(math.sin(span * math.pi * 1.5))
        if chord > limit:
            return None
        v = 0.44 + 0.1 * math.sin(lz * 0.25 + lx * 0.2) + (1 - chord) * 0.1
        col = pick(ctx.pal[key], v)
        for vs in ((0.38, 0.62) if not fore else (0.3, 0.55, 0.8)):
            vein_span = vs + 0.12 * chord * chord
            if abs(span - vein_span) < 0.018 and chord < limit - 0.06:
                col = pick(ctx.pal[vein_key], 0.45)
                if glow_veins:
                    ctx.glow = col
        if chord > limit - 0.04:
            col = pick(ctx.pal[key], 0.22)
        return col

    return paint


def horn_painter(key: str = "horn", rings: float = 6.0, tip_key: str | None = None) -> callable:
    """Gerillte Hörner: dunkle Ringe quer zur Längsachse, zur Spitze heller (oder in tip_key)."""

    def paint(ctx: Ctx):
        w, h, d = ctx.cube.size
        ox, oy, oz = ctx.cube.origin
        axis = max(range(3), key=lambda i: (w, h, d)[i])
        size = (w, h, d)[axis]
        a = 0.0 if size <= 0 else (ctx.local[axis] - (ox, oy, oz)[axis]) / size
        if (ox, oy, oz)[axis] < 0 and axis == 2:
            a = 1.0 - a
        v = 0.3 + 0.5 * a
        if int(a * rings * 2) % 2 == 0:
            v -= 0.1
        col = pick(ctx.pal[key], v + (hash01(ctx.u, ctx.v) - 0.5) * 0.06)
        if tip_key and a > 0.82:
            col = pick(ctx.pal[tip_key], 0.6 + (a - 0.82))
        return col

    return paint


def mouth_painter() -> callable:
    def paint(ctx: Ctx):
        return pick(ctx.pal["mouth"], 0.35 + 0.3 * hash01(ctx.u, ctx.v))

    return paint


def glowing_eye(key: str = "eye", slit: bool = True) -> callable:
    """Reptilienauge: leuchtende Iris mit senkrechter Schlitzpupille."""

    def paint(ctx: Ctx):
        if ctx.face in ("top", "bottom", "back"):
            return pick(ctx.pal["back"], 0.12)
        cx = (ctx.w - 1) / 2
        pupil = slit and ctx.w >= 3 and abs(ctx.s - cx) < 0.55
        if pupil:
            return 16, 8, 6, 255
        col = pick(ctx.pal[key], 0.85 - 0.25 * abs(ctx.s - cx) / max(cx, 1))
        ctx.glow = col
        return col

    return paint


def crystal_painter(key: str = "crystal", glow: bool = True, facets: float = 3.0) -> callable:
    """Kristalle (Eis, Erz): Facetten-Streifen, helle Kanten, leuchtend."""

    def paint(ctx: Ctx):
        lx, ly, lz = ctx.local
        k = (lx * 0.7 + ly * 0.3 + lz * 0.5) / facets
        v = 0.45 + 0.35 * (k % 1.0)
        if ctx.s == 0 or ctx.t == 0 or ctx.s == ctx.w - 1:
            v += 0.15
        col = pick(ctx.pal[key], v)
        if glow:
            ctx.glow = pick(ctx.pal[key], min(0.99, v + 0.1))
        return col

    return paint


def glow_painter(key: str, value: float = 0.8) -> callable:
    def paint(ctx: Ctx):
        col = pick(ctx.pal[key], value + (hash01(ctx.u, ctx.v) - 0.5) * 0.1)
        ctx.glow = col
        return col

    return paint
