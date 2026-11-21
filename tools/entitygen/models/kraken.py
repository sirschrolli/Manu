"""
Der Kraken (seit 0.90): der Herr des Urmeers, so groß wie ein Turm. Er liegt im Schlund in der Mitte des großen Strudels
und steigt empor, wenn jemand in seinen Felsenring kommt.

  * ein gewaltiger Kopf mit zwei goldenen Augen (waagrechte Schlitzpupillen), die im Dunkeln glühen, darüber Wülste
  * der Mantel: ein hoher, nach hinten geneigter Kegel, an der Spitze zwei Flossen; die Haut tief purpurrot mit dunklen
    Flecken und feinen Warzen, über Mantel und Arme verstreut leuchtende Punkte in Türkis
  * unten in der Mitte der Schnabel, schwarz und glänzend
  * acht Arme in sechs Gliedern, die sich nach außen und zur Spitze hin nach oben rollen – unten blasse Saugnäpfe
  * zwei lange Fangarme in acht Gliedern mit einer breiten Keule voller Saugnäpfe und Haken (die greifen und schlagen)

Knochen-Namen werden in KrakenModel (Java) für die Animationen verwendet (Wogen der Arme, Schlag, Griff, Brüllen). Im Spiel ×5.
"""
from __future__ import annotations

import math

from ..geometry import Model
from ..paint import Ctx, hash01, hexc, mix, noise3, pick

PALETTES = {
    "kraken": {
        "skin": [hexc(c) for c in ("#22060e", "#3a0c18", "#561422", "#721e2c", "#8e2a36", "#a83a40", "#c2524e")],
        "blotch": [hexc(c) for c in ("#1a040a", "#2a0812", "#3c0e1a")],
        "under": [hexc(c) for c in ("#9a4e5a", "#b66470", "#cc7e88", "#e0a0a6", "#f2c6c6")],
        "sucker": [hexc(c) for c in ("#5e2230", "#7e3442", "#f6d8d6", "#fff4f0")],
        "glow": [hexc(c) for c in ("#1a8aa0", "#3ad0e0", "#8af6ff", "#e8ffff")],
        "eye": [hexc(c) for c in ("#6a3a06", "#b0700e", "#e0a020", "#f8cc48", "#fff090")],
        "lid": [hexc(c) for c in ("#140306", "#240810", "#3a1018")],
        "beak": [hexc(c) for c in ("#0a0608", "#1a1014", "#2c1c22", "#463038")],
        "fin": [hexc(c) for c in ("#3a0c18", "#5a1626", "#7a2434", "#9c3a46", "#c45c62")],
        "hook": [hexc(c) for c in ("#1a1214", "#3a2e30", "#6a5a5a")],
    },
}

BODY = (0.0, 18.0, 0.0)
ARM_SEGMENTS = [(11.0, 7.0), (10.0, 6.0), (9.0, 5.0), (8.0, 4.1), (7.5, 3.2), (7.0, 2.4)]
# Ruhelage der Arme: das erste Glied schräg hinab ins Wasser, die übrigen rollen sich nach oben
ARM_BEND = [32.0, -8.0, -14.0, -18.0, -22.0, -26.0]
TENTACLE_SEGMENTS = 8
# der Mantel: so hoch (über dem Ansatz am Kopf) und in so vielen Lagen
MANTLE_HEIGHT = 39.0
MANTLE_LAYERS = 13
# Stufen einer Flosse (von unten nach oben): Versatz, Höhe, wie weit sie seitlich hinausreicht – eine Raute
FIN_STEPS = [(4.4 - 1.7 * (k + 1), 1.7, round(10.5 * math.sin(math.pi * (k + 0.5) / 10) ** 0.85 + 0.6, 1)) for k in range(10)]


def _mantle_radius(height: float) -> float:
    """Halbmesser des Mantels in dieser Höhe über dem Ansatz: unten bauchig (10), oben spitz zulaufend."""
    t = max(0.0, min(1.0, height / MANTLE_HEIGHT))
    return max(1.6, 10.2 * (1.0 - t ** 1.7) ** 0.75 + 0.6 * math.sin(t * math.pi) * (1.0 - t))
TENTACLE_LENGTH = 8.0


def _build_arm(m: Model, name: str, angle: float, radius: float, y: float) -> None:
    """Ein Arm aus sechs Gliedern, am Kopf auf dem Kreis {@code radius} unter dem Winkel {@code angle} (Grad, 0 = vorn)."""
    a = math.radians(angle)
    px, pz = -math.sin(a) * radius, -math.cos(a) * radius
    m.bone(name, "body", pivot=(px, y, pz), rotation=(ARM_BEND[0], angle, 0.0))
    parent = name
    for k, (length, width) in enumerate(ARM_SEGMENTS):
        bone = parent if k == 0 else f"{name}_{k}"
        if k > 0:
            prev_len = ARM_SEGMENTS[k - 1][0]
            m.bone(bone, parent, pivot=(0.0, 0.0, -prev_len + 0.4), rotation=(ARM_BEND[k], 0.0, 0.0))
        h = width * 0.9
        m.cube(bone, (-width / 2, -h / 2, -length), (width, h, length), {"bottom": "under", "*": "arm"})
        parent = bone


def _build_tentacle(m: Model, name: str, angle: float) -> None:
    """Ein Fangarm aus acht dünnen Gliedern mit einer Keule am Ende."""
    a = math.radians(angle)
    m.bone(name, "body", pivot=(-math.sin(a) * 6.0, 4.0, -math.cos(a) * 6.0 - 3.0), rotation=(18.0, angle, 0.0))
    parent = name
    for k in range(TENTACLE_SEGMENTS):
        bone = parent if k == 0 else f"{name}_{k}"
        if k > 0:
            m.bone(bone, parent, pivot=(0.0, 0.0, -TENTACLE_LENGTH + 0.3), rotation=(-6.0 if k < 5 else -10.0, 0.0, 0.0))
        w = 3.3 - k * 0.15
        m.cube(bone, (-w / 2, -w / 2, -TENTACLE_LENGTH), (w, w, TENTACLE_LENGTH), {"bottom": "under", "*": "arm"})
        parent = bone
    club = f"{name}_club"
    m.bone(club, parent, pivot=(0.0, 0.0, -TENTACLE_LENGTH + 0.3), rotation=(-12.0, 0.0, 0.0))
    # die Keule: eine Schaufel, in der Mitte am breitesten, zur Spitze hin schmal (unten Saugnäpfe und Haken)
    for (z0, length, width, thick) in ((0.0, 3.0, 4.2, 2.8), (-3.0, 3.5, 5.8, 3.2), (-6.5, 3.5, 6.6, 3.2), (-10.0, 3.0, 5.4, 3.0),
                                       (-13.0, 2.4, 3.8, 2.6), (-15.4, 2.0, 2.2, 2.0)):
        m.cube(club, (-width / 2, -thick / 2, z0 - length), (width, thick, length), {"bottom": "club", "*": "arm"})


def build() -> Model:
    m = Model("kraken", 256, 256)
    m.tex_res = 2
    m.world_faces = False
    m.bone("body", pivot=BODY)
    # der Kopf: breit, vorn etwas schmaler, unten der Kranz, an dem die Arme sitzen
    m.cube("body", (-11.0, -9.0, -10.0), (22, 15, 20), "skin")
    m.cube("body", (-12.0, -6.0, -8.0), (24, 10, 16), "skin")                         # Wangen
    m.cube("body", (-9.0, 6.0, -9.0), (18, 4, 18), "skin")                            # Kranz unten
    m.cube("body", (-8.0, -11.0, -8.0), (16, 2, 15), "skin")                          # Übergang zum Mantel
    for sx in (-1, 1):
        # Augen: groß, weit seitlich vorn; darüber ein Wulst, darunter eine Falte
        ex = 10.2 if sx > 0 else -15.2
        m.cube("body", (ex, -6.8, -9.4), (5.0, 7.6, 8.4), {"west" if sx > 0 else "east": "lid", "*": "eye"})
        m.cube("body", ((9.4 if sx > 0 else -15.9), -9.4, -10.2), (6.5, 2.6, 9.8), "brow")
        m.cube("body", ((9.6 if sx > 0 else -15.4), 0.8, -9.6), (5.8, 1.6, 8.8), "brow")
    # der Trichter (rechts hinten)
    m.cube("body", (7.0, 0.0, 6.0), (4, 4, 6), "skin")

    m.bone("mantle", "body", pivot=(0.0, -10.0, 1.0), rotation=(-14.0, 0.0, 0.0))
    # der Mantel als glatter Kegel: viele flache Lagen, unten bauchig, oben spitz
    for k in range(MANTLE_LAYERS):
        y1 = -MANTLE_HEIGHT * k / MANTLE_LAYERS
        y0 = -MANTLE_HEIGHT * (k + 1) / MANTLE_LAYERS
        r = _mantle_radius(-(y0 + y1) / 2.0)
        m.cube("mantle", (-r, y0, -r), (2 * r, y1 - y0, 2 * r), "skin")
    # Rippen am Mantel (eine kräftige Naht in der Mitte vorn und hinten)
    for sz in (-1, 1):
        for k in range(4):
            y0 = -6.0 - k * 6.0
            r = _mantle_radius(-(y0 - 3.0))
            m.cube("mantle", (-1.2, y0 - 6.0, (r - 0.2) * sz - (0.8 if sz > 0 else 0.0) + (0.0 if sz > 0 else -0.6)), (2.4, 6.0, 1.4), "skin")
    # die Flossen: zwei Rauten an der Spitze des Mantels, gestuft aus dünnen Platten
    for side, sx in (("left", 1), ("right", -1)):
        fin = f"fin_{side}"
        m.bone(fin, "mantle", pivot=(_mantle_radius(29.0) * sx, -29.0, 0.0), rotation=(0.0, 0.0, -6.0 * sx))
        for (dy, h, reach) in FIN_STEPS:
            m.cube(fin, ((0.0 if sx > 0 else -reach), dy, -0.8), (reach, h, 1.6), "fin")

    m.bone("beak", "body", pivot=(0.0, 9.0, -1.0))
    m.cube("beak", (-3.0, 0.0, -3.0), (6, 4, 6), "beak")
    m.cube("beak", (-1.5, 3.5, -1.5), (3, 2, 3), "beak")

    for i in range(8):
        _build_arm(m, f"arm{i}", 22.5 + 45.0 * i, 8.0, 8.0)
    # (im Modell zeigt +X zur linken Seite des Kraken: der linke Fangarm sitzt bei negativem Winkel)
    _build_tentacle(m, "tentacle_left", -28.0)
    _build_tentacle(m, "tentacle_right", 28.0)
    return m


# =============================================================================================
# Bemalen
# =============================================================================================

def _glow_dot(ctx: Ctx, density: float) -> bool:
    """Leuchtpunkte: in wenigen Zellen der Haut je ein einzelner Texel (die Mitte der Zelle)."""
    x, y, z = ctx.world
    cx, cy, cz = int(math.floor(x / 2.5)), int(math.floor(y / 2.5)), int(math.floor(z / 2.5))
    if hash01(cx, cy, cz, 77) >= density:
        return False
    return hash01(ctx.u, ctx.v, 78) < 0.3


def _skin_color(ctx: Ctx, base: float) -> tuple:
    x, y, z = ctx.world
    v = base + (noise3(x * 0.12, y * 0.12, z * 0.12, 3) - 0.5) * 0.34 + (hash01(ctx.u, ctx.v, 4) - 0.5) * 0.07
    col = pick(ctx.pal["skin"], max(0.0, min(1.0, v)))
    if noise3(x * 0.22 + 5.0, y * 0.22, z * 0.22, 6) > 0.72:
        col = pick(ctx.pal["blotch"], 0.4 + hash01(ctx.u, ctx.v, 7) * 0.5)
    elif hash01(ctx.u // 2, ctx.v // 2, 8) < 0.06:
        col = mix(col, pick(ctx.pal["skin"], 0.95), 0.5)                                 # Warzen
    return col


def _skin(ctx: Ctx):
    base = 0.62 if ctx.face == "top" else 0.36 if ctx.face == "bottom" else 0.5
    col = _skin_color(ctx, base)
    if ctx.face != "bottom" and _glow_dot(ctx, 0.035):
        col = pick(ctx.pal["glow"], 0.6 + hash01(ctx.u, ctx.v, 9) * 0.4)
        ctx.glow = col
    return col


def _arm(ctx: Ctx):
    col = _skin_color(ctx, 0.56 if ctx.face == "top" else 0.44)
    if ctx.face == "top" and _glow_dot(ctx, 0.05):
        col = pick(ctx.pal["glow"], 0.55 + hash01(ctx.u, ctx.v, 10) * 0.45)
        ctx.glow = col
    return col


def _suckers(ctx: Ctx, spacing: float, key: str = "under") -> tuple:
    """Blasse Unterseite mit zwei Reihen runder Saugnäpfe (dunkler Rand, helle Mitte)."""
    w, h = ctx.w, ctx.h
    res = 2.0
    # entlang des Arms läuft t (Länge), quer s (Breite)
    s = (ctx.s + 0.5) / res
    t = (ctx.t + 0.5) / res
    across = w / res
    rows = (across * 0.3, across * 0.7) if across > 2.4 else (across * 0.5,)
    best = 9.0
    for r in rows:
        k = round((t - spacing / 2) / spacing)
        cy = k * spacing + spacing / 2 + (0.5 * spacing if r != rows[0] else 0.0)
        for c in (cy - spacing, cy, cy + spacing):
            d = math.hypot(s - r, t - c)
            best = min(best, d)
    radius = min(spacing * 0.38, across * 0.22)
    if best < radius * 0.45:
        return pick(ctx.pal["sucker"], 0.85 + hash01(ctx.u, ctx.v, 12) * 0.15)
    if best < radius:
        return pick(ctx.pal["sucker"], 0.15 + hash01(ctx.u, ctx.v, 13) * 0.2)
    return pick(ctx.pal[key], 0.55 + (noise3(ctx.world[0] * 0.4, ctx.world[1] * 0.4, ctx.world[2] * 0.4, 14) - 0.5) * 0.4)


def _under(ctx: Ctx):
    across = ctx.w / 2.0
    return _suckers(ctx, max(1.2, across * 0.9))


def _club(ctx: Ctx):
    col = _suckers(ctx, 2.6)
    if hash01(ctx.u, ctx.v, 15) < 0.04:
        col = pick(ctx.pal["hook"], 0.3 + hash01(ctx.u, ctx.v, 16) * 0.6)               # Haken zwischen den Näpfen
    return col


def _eye(ctx: Ctx):
    # goldene Iris mit waagrechter, dunkler Schlitzpupille, nach außen dunkler; leuchtet
    cx = (ctx.w - 1) / 2.0
    cy = (ctx.h - 1) / 2.0
    dx = (ctx.s - cx) / max(ctx.w / 2.0, 1.0)
    dy = (ctx.t - cy) / max(ctx.h / 2.0, 1.0)
    r = math.hypot(dx, dy)
    if abs(dy) < 0.16 and abs(dx) < 0.62:
        col = (10, 6, 4, 255)
    elif r > 0.92:
        col = pick(ctx.pal["lid"], 0.6)
        return col
    else:
        col = pick(ctx.pal["eye"], 0.9 - r * 0.6 + (hash01(ctx.u, ctx.v, 17) - 0.5) * 0.12)
        if abs(dy) < 0.3 and abs(dx) < 0.7:
            col = mix(col, pick(ctx.pal["eye"], 0.2), 0.35)                              # Schatten um die Pupille
    ctx.glow = col
    return col


def _lid(ctx: Ctx):
    return pick(ctx.pal["lid"], 0.3 + hash01(ctx.u, ctx.v, 18) * 0.5)


def _brow(ctx: Ctx):
    col = _skin_color(ctx, 0.3 if ctx.face != "top" else 0.55)
    return col


def _beak(ctx: Ctx):
    v = 0.4 + (1.0 - (ctx.t + 0.5) / max(ctx.h, 1)) * 0.4 + (hash01(ctx.u, ctx.v, 19) - 0.5) * 0.15
    return pick(ctx.pal["beak"], v)


def _fin(ctx: Ctx):
    """Die Flosse: zum Rand hin heller und dünner, feine dunkle Strahlen nach außen, am Saum ein leuchtender Streif."""
    x, y, z = ctx.world
    out = abs(x) / 16.0
    v = 0.4 + (noise3(x * 0.25, y * 0.25, z * 0.25, 20) - 0.5) * 0.2 + min(0.35, out * 0.35)
    col = pick(ctx.pal["fin"], v)
    if int(round(y * 2.0 + abs(x) * 0.6)) % 4 == 0:
        col = mix(col, pick(ctx.pal["fin"], 0.1), 0.45)                                    # Strahlen
    if ctx.face in ("east", "west", "up", "down") and hash01(ctx.u, ctx.v, 21) < 0.35:
        col = pick(ctx.pal["glow"], 0.5 + hash01(ctx.u, ctx.v, 22) * 0.4)                 # leuchtender Saum
        ctx.glow = col
    return col


def materials(variant: str) -> dict:
    return {"skin": _skin, "arm": _arm, "under": _under, "club": _club, "eye": _eye, "lid": _lid, "brow": _brow, "beak": _beak,
            "fin": _fin}
