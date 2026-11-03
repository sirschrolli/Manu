"""
Leerwandler (statt Enderman) auf dem Skelett des Vanilla-Endermans (Knochen und Drehpunkte wie EndermanModel, damit
Gehen, Greifen, der getragene Block und das aufgerissene Maul passen):

  * ein Leib wie aus dem Nachthimmel geschnitten – fast schwarz, darin Sterne, die schwach leuchten, und ein Hauch von
    Nebel; über der Brust klafft ein Riss, durch den man in die leuchtende Leere sieht
  * ein langer, glatter Kopf ohne Mund, nur zwei schmale, schräge, gleißende Augen; wird er wütend, hebt sich der Kopf
    vom Kiefer ("hat", wie bei Vanilla) und darunter klafft ein Schlund voller dünner Zähne und violettem Licht
  * über dem Kopf kreisen drei Splitter aus Purpurkristall (eigener Knochen "halo")
  * hinten hängen zerfaserte Schleier aus Schatten bis über die Hüften, über den Schultern ein dunkler Grat
  * dürre lange Glieder mit Gelenkknoten, an den Händen drei lange Finger, an den Beinen schmale Füße

Zwei Gestalten: Leerwandler (violett) und Wirrwandler (türkis, im Nether).
"""
from __future__ import annotations

import math

from ..geometry import Model
from ..paint import Ctx, hash01, hexc, mix, noise3, pick, ramp

PALETTES = {
    "leerwandler": {
        "void": ramp("#07040b", "#3a2a48", steps=8), "nebula": ramp("#1a0a2a", "#7a3aa8"),
        "star": [hexc(c) for c in ("#6a4a8a", "#9a7ac0", "#d0b8ff", "#f4ecff", "#ffffff")],
        "eye": [hexc(c) for c in ("#5a1070", "#a02ad0", "#d86aff", "#f2c0ff", "#ffffff")],
        "rift": [hexc(c) for c in ("#200838", "#4a1478", "#8a2ac0", "#c45af0", "#f0a8ff", "#ffffff")],
        "crystal": ramp("#2a1240", "#e8b8ff", steps=7), "tooth": ramp("#5a4a6a", "#e8dcf4"),
    },
    "wirrwandler": {
        "void": ramp("#030a0a", "#1e3a3a", steps=8), "nebula": ramp("#062222", "#1e8078"),
        "star": [hexc(c) for c in ("#2a6a66", "#4aa8a0", "#90e8de", "#d8fffa", "#ffffff")],
        "eye": [hexc(c) for c in ("#0a5048", "#14a090", "#3ee8d4", "#b0fff2", "#ffffff")],
        "rift": [hexc(c) for c in ("#04201e", "#0a4a44", "#148a80", "#2ccab8", "#a0fff0", "#ffffff")],
        "crystal": ramp("#06302c", "#a8fff2", steps=7), "tooth": ramp("#3a5a58", "#d8f4f0"),
    },
}


def build() -> Model:
    m = Model("enderman", 128, 64)
    m.tex_res = 2
    # Kopf mit Kiefer ("hat" wie bei Vanilla: steht still, wenn sich der Kopf im Zorn hebt) und Splitterkranz
    m.bone("head", pivot=(0, -13, 0))
    m.cube("head", (-3.5, -9.5, -3.5), (7, 9.5, 7), "head")
    m.bone("hat", "head")
    m.cube("hat", (-3, -7, -3), (6, 7, 6), "jaw")
    m.bone("halo", "head", pivot=(0, -10.5, 0))
    for i in range(3):
        a = math.radians(i * 120)
        m.bone(f"shard_{i}", "halo", pivot=(math.cos(a) * 5.0, (-1.0, 0.6, -0.2)[i], math.sin(a) * 5.0),
               rotation=(0, -i * 120, (18, -14, 24)[i]))
        m.cube(f"shard_{i}", (-0.6, -1.7, -0.6), (1.2, 3.4, 1.2), "shard")
    # Rumpf mit Schultergrat und Schattenschleiern
    m.bone("body", pivot=(0, -14, 0))
    m.cube("body", (-3.5, 0, -1.75), (7, 12, 3.5), "torso")
    m.cube("body", (-5, -0.6, -2), (10, 1.5, 4), "ridge")
    m.cube("body", (-4.5, -0.3, 1.75), (9, 17, 0.5), "veil")
    # lange Glieder (wie bei Vanilla 30 Einheiten), Knoten, Finger und Füße
    for side, sx in (("right", -1), ("left", 1)):
        arm = f"{side}_arm"
        m.bone(arm, pivot=(5 * sx, -12, 0))
        m.cube(arm, (-1, -2, -1), (2, 30, 2), "limb")
        m.cube(arm, (-1.25, 11, -1.25), (2.5, 1.5, 2.5), "knot")
        for fx, fz, length in ((-0.95, -0.95, 4.5), (0.35, -0.95, 4.5), (-0.3, 0.35, 3.8)):
            m.cube(arm, (fx, 27.5, fz), (0.6, length, 0.6), "finger")
        leg = f"{side}_leg"
        m.bone(leg, pivot=(2 * sx, -5, 0))
        m.cube(leg, (-1, 0, -1), (2, 30, 2), "limb")
        m.cube(leg, (-1.25, 13, -1.25), (2.5, 1.5, 2.5), "knot")
        m.cube(leg, (-1.1, 28.5, -2.4), (2.2, 1.5, 1.6), "foot")
    return m


# =============================================================================================
# Hilfen
# =============================================================================================

def _uv(ctx: Ctx) -> tuple[float, float]:
    return (ctx.s + 0.5) / max(ctx.w, 1), (ctx.t + 0.5) / max(ctx.h, 1)


def _lit(ctx: Ctx, key: str, v: float):
    pal = ctx.pal[key]
    col = pal[max(0, min(len(pal) - 1, int(v * len(pal))))]
    ctx.glow = col
    return col


def _void(ctx: Ctx, base: float = 0.3):
    """Haut wie Nachthimmel: fast schwarz, ein Hauch Nebel, darin leuchtende Sterne."""
    x, y, z = ctx.world
    v = base + (noise3(x * 0.35, y * 0.35, z * 0.35, 1101) - 0.5) * 0.12 + (hash01(ctx.u, ctx.v, 1102) - 0.5) * 0.05
    if ctx.face == "top":
        v += 0.06
    elif ctx.face == "bottom":
        v -= 0.08
    col = pick(ctx.pal["void"], v)
    neb = noise3(x * 0.22 + 40, y * 0.22, z * 0.22, 1103)
    if neb > 0.62:
        col = mix(col, pick(ctx.pal["nebula"], 0.3 + (neb - 0.62) * 1.5), min(0.55, (neb - 0.62) * 2.2))
    r = hash01(ctx.u, ctx.v, 1104)
    if r > 0.986:
        return _lit(ctx, "star", ((r - 0.986) / 0.014) ** 2)             # Sterne, die meisten schwach
    return col


# =============================================================================================
# Kopf
# =============================================================================================

def _head(ctx: Ctx):
    u, v = _uv(ctx)
    if ctx.face == "front":
        # zwei schmale, schräge Augen: innen tiefer als außen
        for cu, slant in ((0.28, 1.0), (0.72, -1.0)):
            du = u - cu
            dv = v - (0.5 + du * slant * 0.45)
            if abs(du) < 0.17 and abs(dv) < 0.05:
                core = 1.0 - max(abs(du) / 0.17, abs(dv) / 0.05)
                return _lit(ctx, "eye", 0.5 + core * 0.55)
            if abs(du) < 0.2 and abs(dv) < 0.1:
                return mix(pick(ctx.pal["void"], 0.05), pick(ctx.pal["eye"], 0.0), 0.5)    # Schein um die Augen
        if v > 0.74:
            return _void(ctx, 0.22)                                     # da sitzt der Kiefer (dunkler)
        return _void(ctx, 0.3)
    return _void(ctx, 0.3)


def _jaw(ctx: Ctx):
    """Kiefer: nur zu sehen, wenn der Kopf sich hebt – dann ein Schlund voller dünner Zähne und violettem Licht."""
    u, v = _uv(ctx)
    if ctx.face == "front":
        if 0.28 < v < 0.9 and 0.12 < u < 0.88:
            # Zähne oben und unten, lang und dünn
            col_i = int(u * 12)
            top = 0.28 + (0.18 if col_i % 2 else 0.1)
            bottom = 0.9 - (0.16 if col_i % 2 == 0 else 0.08)
            if col_i % 3 != 1 and (v < top or v > bottom):
                return pick(ctx.pal["tooth"], 0.4 + (1.0 - abs(v - 0.59) * 2.0) * 0.4)
            depth = 1.0 - min(1.0, math.hypot((u - 0.5) * 1.6, (v - 0.6) * 2.2))
            return _lit(ctx, "rift", 0.15 + depth * 0.8)
        return _void(ctx, 0.24)
    if ctx.face == "top":
        return _lit(ctx, "rift", 0.25)
    return _void(ctx, 0.26)


def _shard(ctx: Ctx):
    """Splitter aus Purpurkristall: oben und unten spitz, Kanten hell."""
    o, size, p = ctx.cube.origin, ctx.cube.size, ctx.local
    f = (p[1] - o[1]) / size[1]                                         # 0 oben … 1 unten
    taper = abs(f - 0.5) * 2.0                                          # 0 Mitte … 1 Spitzen
    if ctx.face in ("top", "bottom"):
        return None
    across = abs((p[0] - o[0]) / size[0] - 0.5) * 2.0 if ctx.face in ("front", "back") else abs((p[2] - o[2]) / size[2] - 0.5) * 2.0
    if across > 1.05 - taper ** 1.4:
        return None
    edge = across > 0.6 - taper * 0.4
    return _lit(ctx, "crystal", (0.8 if edge else 0.5) + (hash01(ctx.u, ctx.v) - 0.5) * 0.15) if ctx.pal.get("crystal") else None


# =============================================================================================
# Rumpf und Glieder
# =============================================================================================

def _torso(ctx: Ctx):
    u, v = _uv(ctx)
    if ctx.face == "front":
        # klaffender Riss über Brust und Bauch, darin die leuchtende Leere
        path = 0.5 + 0.08 * math.sin(v * 13.0) + 0.04 * math.sin(v * 31.0 + 1.0)
        width = 0.11 * math.sin(math.pi * min(1.0, max(0.0, (v - 0.12) / 0.7)))
        if 0.12 < v < 0.82 and abs(u - path) < width:
            k = 1.0 - abs(u - path) / max(width, 1e-6)
            return _lit(ctx, "rift", 0.3 + k * 0.7)
        if 0.12 < v < 0.82 and abs(u - path) < width + 0.07:
            return mix(_void(ctx, 0.2), pick(ctx.pal["rift"], 1), 0.4)       # glimmender Rand
    return _void(ctx, 0.32 if ctx.face != "back" else 0.26)


def _ridge(ctx: Ctx):
    if ctx.face == "bottom":
        return pick(ctx.pal["void"], 0.05)
    if ctx.face == "top" and int(ctx.s / 2) % 3 == 0:
        return pick(ctx.pal["void"], 0.55)                              # Grate
    return _void(ctx, 0.42)


def _veil(ctx: Ctx):
    """Schattenschleier: zerfasert, unten in Strähnen, Löcher."""
    x, y, z = ctx.world
    u, v = _uv(ctx)
    col_i = int(u * 18)
    end = 0.5 + hash01(col_i, 1121) * 0.45
    if ctx.face in ("top", "bottom") or v > end:
        return None
    if noise3(x * 0.8, y * 0.35, z, 1122) > 0.7 and v > 0.2:
        return None
    if v > end - 0.06 and hash01(ctx.u, ctx.v, 1123) > 0.5:
        return _lit(ctx, "nebula", 0.55)                                # Fransen glimmen
    return pick(ctx.pal["void"], 0.18 + (noise3(x * 1.2, y * 0.2, z, 1124) - 0.5) * 0.2)


def _limb(ctx: Ctx):
    return _void(ctx, 0.3)


def _knot(ctx: Ctx):
    if ctx.face not in ("top", "bottom") and ctx.t == ctx.h // 2:
        return _lit(ctx, "rift", 0.55)                                  # glimmender Ring
    return _void(ctx, 0.42)


def _finger(ctx: Ctx):
    f = (ctx.local[1] - ctx.cube.origin[1]) / ctx.cube.size[1]
    if f > 0.82:
        return _lit(ctx, "eye", 0.25)                                   # Fingerspitzen glimmen
    return _void(ctx, 0.36)


def _foot(ctx: Ctx):
    return _void(ctx, 0.34)


def materials(variant: str) -> dict:
    return {"head": _head, "jaw": _jaw, "shard": _shard, "torso": _torso, "ridge": _ridge, "veil": _veil,
            "limb": _limb, "knot": _knot, "finger": _finger, "foot": _foot}


def hidden(variant: str) -> list[str]:
    return []
