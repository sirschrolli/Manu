"""
Thalassa, die Herrin der Tiefe – der Wasserdrachen-Boss.

Eine Seeschlange: ein mächtiger Vorderleib mit zwei breiten Flossen statt Beinen, ein langer, schlanker Hals und ein
aalartiger Kopf mit langer Schnauze, Fangzähnen, zwei hängenden Barteln und einem aufstellbaren Kragen aus sieben
Flossenstrahlen (wie eine Kragenechse). Der Schlangenleib (sechzehn Glieder) trägt einen durchgehenden Rückenkamm aus
Flossenhaut und endet in einer großen, gegabelten Schwanzflosse. Schuppen tief petrol bis nachtblau, der Bauch blass
türkis, entlang der Flanken Reihen leuchtender Punkte wie bei Tiefseewesen; die Flossen sind durchscheinend mit
leuchtenden Rändern. Grundgröße ca. 14 Blöcke, im Spiel ×2,0.
"""
from __future__ import annotations

import math

from ..geometry import Model
from ..paint import Ctx, Voronoi3D, flat_painter, hash01, mix, noise3, pick, ramp, transparent_painter
from .boss_common import belly_plate, chain, facing_ry, glowing_eye, mouth_painter, plates_painter

PALETTES = {
    "thalassa": {
        "back": ramp("#08222c", "#2c7c8a"), "belly": ramp("#2a6a70", "#c4ece4"), "glow": ramp("#1aa8c8", "#e8ffff"),
        "fin": ramp("#0e4a5a", "#7ae0dc"), "spine": ramp("#0a2a34", "#9ad8d4"), "claw": ramp("#1a2a2c", "#e8e4d0"),
        "eye": ramp("#3ad8c8", "#f4fff8"), "mouth": ramp("#14141e", "#5a3a4a"), "barbel": ramp("#1a5a64", "#8ad4d0"),
    },
}

TEX = (512, 512)


def build() -> Model:
    m = Model("thalassa", *TEX)

    # ---------------------------------------------------------------- Vorderleib mit Flossen
    m.bone("body", pivot=(0, 14, 0))
    m.cube("body", (-8, -7, -14), (16, 14, 27), "scales")
    m.cube("body", (-6.5, -9, -12), (13, 2.5, 22), "scales")                    # Rückenwulst
    m.cube("body", (-7, 6, -13), (14, 2.5, 25), "plates")                       # Bauchplatten
    m.cube("body", (-0.4, -15, -11), (0.8, 6.5, 20), "dorsal")                  # Rückenkamm
    for side, sx in (("l", 1), ("r", -1)):
        flipper(m, side, sx)

    # ---------------------------------------------------------------- Hals (fünf Glieder) und Kopf
    neck_sizes = [(12, 12, 7.5), (10.6, 10.6, 7.2), (9.6, 9.6, 7), (8.8, 8.8, 6.6), (8.2, 8.2, 6.2)]
    neck = chain(m, "neck", "body", (0, -2, -13.5), neck_sizes, [-28, -16, -6, 6, 12], direction=-1, belly="plates")
    for i, (name, (w, h, length)) in enumerate(zip(neck, neck_sizes)):
        fh = 4.0 - i * 0.4
        m.cube(name, (-0.35, -h / 2 - fh, -length + 0.5), (0.7, fh + 0.5, length - 1), "dorsal")
    m.bone("head", neck[-1], pivot=(0, 0, -6), rotation=(32, 0, 0))
    m.cube("head", (-6, -5, -12), (12, 10, 12), "scales")                       # Schädel
    m.cube("head", (-4.5, -3.2, -25), (9, 6.6, 13.5), "scales")                 # lange Schnauze
    m.cube("head", (-3.2, -4.6, -23.5), (6.4, 1.5, 12), "scales")               # Nasenrücken
    m.cube("head", (-3.4, -3.6, -25.3), (2, 1.2, 1.2), "nostril")
    m.cube("head", (1.4, -3.6, -25.3), (2, 1.2, 1.2), "nostril")
    m.cube("head", (-4.1, 3.3, -24.6), (8.2, 1.2, 12), "teeth")
    m.cube("head", (-4.1, 3.2, -24.5), (8.2, 0.2, 12.5), "mouth")
    for fx in (-3.4, 2.4):                                                       # obere Fangzähne
        m.cube("head", (fx, 3.6, -23.5), (1, 3.2, 1.2), "teeth")
    m.cube("head", (5.6, -2.8, -12.0), (1, 2.2, 4), "eye")
    m.cube("head", (-6.6, -2.8, -12.0), (1, 2.2, 4), "eye")
    m.cube("head", (5.2, -5.6, -12.5), (1.6, 1.4, 6), "scales")                 # Brauenwülste
    m.cube("head", (-6.8, -5.6, -12.5), (1.6, 1.4, 6), "scales")
    m.cube("head", (-0.4, -9.5, -11), (0.8, 4.8, 9), "dorsal")                  # Scheitelkamm
    # Barteln: zwei lange, hängende Fäden an der Schnauze
    for side, sx in (("l", 1), ("r", -1)):
        b1 = f"barbel_{side}1"
        b2 = f"barbel_{side}2"
        m.bone(b1, "head", pivot=(3.8 * sx, 1.5, -23), rotation=(20, 0, 25 * sx))
        m.cube(b1, (-0.5, 0, -0.5), (1, 8, 1), "barbel")
        m.bone(b2, b1, pivot=(0, 7.6, 0), rotation=(15, 0, -10 * sx))
        m.cube(b2, (-0.4, 0, -0.4), (0.8, 7, 0.8), "barbel")
    # Kragen: sieben Flossenstrahlen fächern hinter dem Schädel auf (oben einer, je drei zur Seite)
    for k, (ang, length, width) in enumerate([(0, 15, 11), (40, 16, 12), (80, 14, 11), (116, 11, 9)]):
        sides = [("c", 1)] if ang == 0 else [("l", 1), ("r", -1)]
        for side, sx in sides:
            name = f"frill_{side}{k}"
            m.bone(name, "head", pivot=(0, -0.5, -2.5), rotation=(-24, 0, ang * sx))
            m.cube(name, (-width / 2, -length, -0.5), (width, length, 1), "frill")
    m.bone("jaw", "head", pivot=(0, 3.4, -10), rotation=(3, 0, 0))
    m.cube("jaw", (-4.2, 0, -14.5), (8.4, 3.2, 15), "scales")
    m.cube("jaw", (-3.8, -1.1, -14.2), (7.6, 1.1, 11), "teeth")
    m.cube("jaw", (-3.4, -0.2, -13.6), (6.8, 0.4, 12.6), "mouth")
    for fx in (-3.0, 2.0):                                                       # untere Fangzähne
        m.cube("jaw", (fx, -3.2, -13.2), (1, 3.2, 1.2), "teeth")
    m.cube("jaw", (-0.4, 3.0, -11), (0.8, 3.5, 7), "dorsal")                    # Kehlflosse

    # ---------------------------------------------------------------- Schlangenleib (sechzehn Glieder) mit Rückenkamm
    sizes = []
    for i in range(16):
        t = i / 15
        w = 15.0 - 11.5 * t ** 1.15
        h = 13.0 - 10.0 * t ** 1.15
        length = 9.5 - 3.0 * t
        sizes.append((round(w, 2), round(h, 2), round(length, 2)))
    tail = chain(m, "tail", "body", (0, 0, 12.5), sizes, [-2] + [0] * 15, direction=1, belly="plates")
    for i, (name, (w, h, length)) in enumerate(zip(tail, sizes)):
        if i < 14:
            fh = 5.2 - i * 0.3
            m.cube(name, (-0.35, -h / 2 - fh, 0.5), (0.7, fh + 0.5, length), "dorsal")
        if 3 <= i <= 6:
            # kleine Bauchflossen in der Mitte des Leibes
            for sx in (1, -1):
                m.cube(name, ((w / 2 - 0.5) if sx > 0 else (-w / 2 - 3.5), h / 2 - 2, 2), (4, 0.8, 5), "fin")
    # große, gegabelte Schwanzflosse (senkrecht)
    m.cube(tail[-1], (-0.6, -11, 1.5), (1.2, 22, 14), "fluke")
    return m


def flipper(m: Model, side: str, sx: int) -> None:
    """Brustflosse statt eines Vorderbeins: kräftiger Ansatz, Unterarm und breites Flossenblatt (für die Animation
    heißen die Knochen wie die eines Vorderbeins)."""
    up = f"front_leg_{side}"
    shin = f"front_shin_{side}"
    foot = f"front_foot_{side}"
    m.bone(up, "body", pivot=(7.5 * sx, 3.5, -7.5), rotation=(25, 0, -58 * sx))
    m.cube(up, (-2.6, -1, -3.6), (5.2, 9.5, 7.2), "scales")
    m.bone(shin, up, pivot=(0, 8.5, 0), rotation=(28, 0, 0))
    m.cube(shin, (-2.1, -0.5, -3), (4.2, 8.5, 6), "scales")
    m.bone(foot, shin, pivot=(0, 7.8, 0), rotation=(22, 0, 0))
    m.cube(foot, (-0.7, -0.5, -5.5), (1.4, 15, 11), "flipper")


# =============================================================================================
# Bemalung
# =============================================================================================

def spotted_scales(cell: float = 3.2, seed: int = 81) -> callable:
    """Glatte Aalschuppen: oben tief petrol, zum Bauch hin heller; an den Flanken Reihen leuchtender Punkte, dazu
    feine helle Linien zwischen den Schuppen und ein leichter Schimmer."""
    vor = Voronoi3D(cell, seed)

    def paint(ctx: Ctx):
        x, y, z = ctx.world
        if ctx.face == "bottom":
            return belly_plate(ctx, "belly")
        ry = facing_ry(ctx)
        val, edge = vor(x, y, z)
        sheen = noise3(x * 0.12, y * 0.12, z * 0.12, seed + 3)
        v = 0.34 + (val - 0.5) * 0.2 + (1.0 - ry) * 0.06 + (sheen - 0.5) * 0.18
        if edge < 0.2:
            v -= 0.12 * (1.0 - edge / 0.2)
        col = pick(ctx.pal["back"], v)
        if ctx.face in ("west", "east", "front", "back"):
            # Leuchtpunkte in zwei lockeren Reihen entlang der Flanke (rundlich, nicht jeder Platz besetzt)
            h = max(ctx.cube.size[1], 1e-3)
            for band, spacing, salt in ((0.42, 4.6, 11), (0.66, 5.4, 23)):
                idx = math.floor((z + band * 13.0) / spacing)
                if hash01(idx, salt) < 0.3:
                    continue
                dz = (z + band * 13.0) - (idx + 0.5) * spacing
                dy = (ry - band) * h
                if dz * dz + dy * dy * 1.6 < 0.55:
                    g = pick(ctx.pal["glow"], 0.75 + 0.2 * hash01(idx, salt + 1))
                    ctx.glow = g
                    return g
            if ry > 0.72:
                t = (ry - 0.72) / 0.28
                col = mix(col, pick(ctx.pal["belly"], 0.55 + (val - 0.5) * 0.2), t * 0.85)
        return col

    return paint


def _fin_base(ctx: Ctx, u: float, rel: float, spines: float, key: str = "fin"):
    """Flossenhaut mit Strahlen; zum Rand hin heller und leuchtend."""
    ray = abs(((u * spines) % 1.0) - 0.5) < 0.1
    v = 0.3 + 0.5 * rel
    if ray:
        col = pick(ctx.pal["spine"], 0.35 + 0.5 * rel)
    else:
        col = pick(ctx.pal[key], v + (hash01(ctx.u, ctx.v) - 0.5) * 0.06)
    if rel > 0.8:
        g = pick(ctx.pal["glow"], 0.55 + 0.4 * (rel - 0.8) / 0.2)
        ctx.glow = g
        return g
    return col


def _dorsal(ctx: Ctx):
    """Rückenkamm: dünne Flossenhaut mit Strahlen, oben gewellt ausgeschnitten, leuchtender Saum."""
    oy, oz = ctx.cube.origin[1], ctx.cube.origin[2]
    h, d = ctx.cube.size[1], ctx.cube.size[2]
    rel = 1.0 - (ctx.local[1] - oy) / max(h, 1e-3)          # 1 oben, 0 unten
    along = (ctx.local[2] - oz) / max(d, 1e-3)
    wave = 0.12 + 0.12 * math.sin(along * math.pi * 3.0 + ctx.world[2] * 0.2)
    if rel > 1.0 - wave:
        return None
    return _fin_base(ctx, ctx.world[2] * 0.25, rel / (1.0 - wave), 1.0)


def _frill(ctx: Ctx):
    """Kragenstrahl: spitz zulaufend, mit Mittelstrahl und leuchtender Spitze."""
    ox, oy = ctx.cube.origin[0], ctx.cube.origin[1]
    w, h = ctx.cube.size[0], ctx.cube.size[1]
    u = (ctx.local[0] - ox) / max(w, 1e-3) * 2 - 1          # -1..1 quer
    rel = 1.0 - (ctx.local[1] - oy) / max(h, 1e-3)          # 0 am Kopf, 1 an der Spitze
    half = 1.0 - 0.15 * rel if rel < 0.7 else 0.895 * (1.0 - rel) / 0.3
    if abs(u) > half:
        return None
    if abs(u) < 0.12:
        col = pick(ctx.pal["spine"], 0.4 + 0.5 * rel)
        if rel > 0.75:
            ctx.glow = col
        return col
    return _fin_base(ctx, u * 2.0, rel, 3.0)


def _flipper(ctx: Ctx):
    """Flossenblatt: breit, abgerundet, mit fünf Strahlen und leuchtendem Rand."""
    oy, oz = ctx.cube.origin[1], ctx.cube.origin[2]
    h, d = ctx.cube.size[1], ctx.cube.size[2]
    rel = (ctx.local[1] - oy) / max(h, 1e-3)                # 0 am Ansatz, 1 außen
    v = (ctx.local[2] - oz) / max(d, 1e-3)                  # 0 vorn, 1 hinten
    width = 0.55 + 0.45 * math.sin(min(1.0, rel * 1.15) * math.pi * 0.85)
    if abs(v - 0.45) > width * 0.5 or rel > 0.98 - 0.25 * (v - 0.2) ** 2:
        return None
    edge = 1.0 - min(1.0, (width * 0.5 - abs(v - 0.45)) / 0.12)
    return _fin_base(ctx, v * 1.0, max(rel * 0.85, edge), 5.0)


def _fluke(ctx: Ctx):
    """Gegabelte Schwanzflosse: zwei Lappen nach oben und unten, Strahlen fächerförmig vom Ansatz."""
    oy, oz = ctx.cube.origin[1], ctx.cube.origin[2]
    h, d = ctx.cube.size[1], ctx.cube.size[2]
    u = (ctx.local[1] - oy) / max(h, 1e-3) * 2 - 1          # -1 oben … 1 unten
    v = (ctx.local[2] - oz) / max(d, 1e-3)                  # 0 am Ansatz, 1 hinten
    reach = 0.18 + 0.82 * v                                 # wird nach hinten breiter
    notch = 0.45 * max(0.0, v - 0.45) / 0.55                # Gabel in der Mitte hinten
    if abs(u) > reach or abs(u) < notch:
        return None
    rel = max(v, abs(u))
    angle = math.atan2(u, v + 0.05)
    return _fin_base(ctx, angle / math.pi * 2.5, rel, 3.0)


def _small_fin(ctx: Ctx):
    oz, d = ctx.cube.origin[2], ctx.cube.size[2]
    v = (ctx.local[2] - oz) / max(d, 1e-3)
    return _fin_base(ctx, v, 0.4 + 0.5 * v, 2.0)


def materials(variant: str) -> dict:
    return {
        "scales": spotted_scales(),
        "plates": plates_painter("belly"),
        "dorsal": _dorsal,
        "frill": _frill,
        "flipper": _flipper,
        "fluke": _fluke,
        "fin": _small_fin,
        "barbel": flat_painter("barbel", 0.55),
        "teeth": flat_painter("claw", 0.95, 0.04),
        "claw": flat_painter("claw", 0.8),
        "eye": glowing_eye("eye"),
        "nostril": flat_painter("mouth", 0.2),
        "mouth": mouth_painter(),
        "none": transparent_painter(),
    }
