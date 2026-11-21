"""
Phasmor, der Leerwandlerkönig (seit 1.5): der älteste der Leerwandler, der Herr der Phasensprünge. Er wacht im
Phasenheiligtum auf den äußeren Inseln des Ends vor dem Leerenthron – reglos wie ein Standbild, die Hände auf dem Knauf
seiner Klinge, die mit der Spitze im Boden steckt.

Ein Leerwandler von königlicher Gestalt (im Spiel 1,35-fach vergrößert, mit der Krone fast fünf Blöcke hoch): ein Leib wie
aus dem Nachthimmel geschnitten, fast schwarz mit Sternen, über der Brust ein Riss voller türkisem Licht; ein langer Kopf
mit zwei schmalen, schrägen, türkis gleißenden Augen (seine Diener blicken violett – nur der König sieht mit dem Licht des
Phasits), darunter ein Kiefer, der sich im Zorn öffnet. Ein Stirnreif aus Leerenstahl mit einem Phasit, darüber schwebt
eine Krone aus fünf Phasitsplittern, die sich langsam dreht. Hinter dem Kopf ein hoher Stehkragen, innen mit türkisem
Strahlenkranz. Ein Brustpanzer aus Leerenstahl mit einem großen Phasit, ein Schultergrat, Schulterplatten mit schwebenden
Splittern (wie die Rüstung der Leerwandler), ein langer Königsmantel mit Purpursaum und leuchtenden Fäden, der unten in
Fetzen ausläuft, vorn ein Wappenrock aus Purpur mit dem Auge der Leerwandler. Lange, dürre Glieder mit Gelenken (Ellbogen,
Knie), Armschienen und Beinschienen mit leuchtenden Nähten, drei lange Finger. In der Rechten die Phasenklinge: ein
Zweihänder aus einem einzigen Phasitkristall, Parierstange aus Purpur, Griff mit violetter Wicklung, Phasit im Knauf.

Knochen-Namen werden in PhasmorModel (Java) für die Animationen verwendet.
"""
from __future__ import annotations

import math

from ..geometry import Model
from ..paint import Ctx, hash01, hexc, mix, noise3, pick, ramp, rel_y


def stops(*colors: str) -> list:
    """Handverlesene Tonleiter (Türkis und Phasit: der lineare Verlauf würde in der Mitte grau)."""
    return [hexc(c) for c in colors]


PALETTES = {
    "phasmor": {
        "void": ramp("#05040a", "#2e2642", steps=8),
        "nebula": ramp("#071c22", "#1a6e6c"),
        "star": stops("#2a6a66", "#4aa8a0", "#90e8de", "#d8fffa", "#ffffff"),
        "eye": stops("#0a6a5e", "#19c4ac", "#3ce0c8", "#a6faec", "#ffffff"),
        "rift": stops("#032a28", "#07524c", "#0c9888", "#28d4bc", "#8af6e4", "#ffffff"),
        "crystal": stops("#04403a", "#076a60", "#0c9888", "#19c4ac", "#3ce0c8", "#86f4e0", "#e0fff8"),
        "steel": ramp("#0b0912", "#5c5682", steps=8),
        "purpur": ramp("#2e1238", "#e8c0ee", steps=8),
        "cloth": ramp("#07050c", "#463a62", steps=8),
        "leather": ramp("#0a0610", "#4a3050", steps=6),
        "spark": stops("#5a1e9a", "#a24ee8", "#f0d8ff"),
        "tooth": ramp("#3a4a4a", "#d8f0ec"),
    },
}

_LIT = {"top": 0.08, "front": 0.03, "west": 0.0, "east": -0.03, "back": -0.06, "bottom": -0.14}

#: die Krone: fünf Splitter im Kreis (der vordere am höchsten) – Winkel (Grad, -90 = vorn) und Höhe
CROWN = ((-90.0, 6.6), (-18.0, 5.0), (54.0, 4.0), (126.0, 4.0), (198.0, 5.0))
CROWN_RADIUS = 3.3
#: die Klinge: so lang (Pixel, ohne Griff) und so breit
BLADE_LENGTH = 22.8
BLADE_WIDTH = 3.4


def build() -> Model:
    m = Model("phasmor", 128, 128)
    m.tex_res = 2
    # ------------------------------------------------------------------ Rumpf (Drehpunkt an der Hüfte)
    m.bone("body", pivot=(0, -3, 0))
    m.cube("body", (-4, -11.5, -2), (8, 12, 4), "torso")
    m.cube("body", (-5.6, -12.3, -2.4), (11.2, 1.6, 4.8), "yoke")                     # der Schultergrat
    m.cube("body", (-4.4, -10.9, -2.5), (8.8, 5.4, 0.8), "plate")                     # der Brustpanzer
    m.cube("body", (-1.15, -9.75, -3.0), (2.3, 2.3, 0.6), "gem")                      # der große Phasit
    m.cube("body", (-4.3, -1.6, -2.3), (8.6, 1.4, 4.6), "belt")
    m.cube("body", (-0.8, -1.8, -2.75), (1.6, 1.8, 0.5), "gem")                       # die Gürtelschnalle
    # der Stehkragen hinter dem Kopf: Mitte und zwei ausgestellte Flügel, innen ein türkiser Strahlenkranz
    m.bone("collar", "body", pivot=(0, -12.0, 1.5), rotation=(-14, 0, 0))
    m.cube("collar", (-3.8, -7.6, 0), (7.6, 7.6, 0.7), "collar")
    for side, sx in (("l", 1), ("r", -1)):
        wing = f"collar_{side}"
        m.bone(wing, "collar", pivot=(3.7 * sx, 0, 0.2), rotation=(0, -30 * sx, -8 * sx))
        m.cube(wing, (0 if sx > 0 else -3.4, -6.4, -0.1), (3.4, 6.4, 0.6), "collar_wing")
    # der Königsmantel (zwei Glieder, damit er weht) und vorn der Wappenrock
    m.bone("mantle", "body", pivot=(0, -12.2, 2.4), rotation=(5, 0, 0))
    m.cube("mantle", (-5.6, 0, 0), (11.2, 14, 0.7), "mantle")
    m.bone("mantle_low", "mantle", pivot=(0, 14, 0.05), rotation=(5, 0, 0))
    m.cube("mantle_low", (-5.4, 0, 0), (10.8, 13, 0.6), "mantle_low")
    m.bone("tabard", "body", pivot=(0, -0.9, -2.35))
    m.cube("tabard", (-2.3, 0, -0.3), (4.6, 11, 0.5), "tabard")

    # ------------------------------------------------------------------ Kopf, Kiefer, Stirnreif, Krone
    m.bone("head", "body", pivot=(0, -11.5, 0))
    m.cube("head", (-3.5, -9.5, -3.5), (7, 9.5, 7), "head")
    m.cube("head", (-3.9, -10.0, -3.9), (7.8, 1.4, 7.8), "circlet")
    m.cube("head", (-0.9, -9.85, -4.2), (1.8, 1.8, 0.5), "gem")
    m.bone("jaw", "body", pivot=(0, -11.5, 0))                                         # bleibt stehen, wenn sich der Kopf hebt
    m.cube("jaw", (-3, -7, -3), (6, 7, 6), "jaw")
    m.bone("crown", "head", pivot=(0, -12.2, 0))
    for i, (deg, height) in enumerate(CROWN):
        a = math.radians(deg)
        spike = f"crown_{i}"
        m.bone(spike, "crown", pivot=(math.cos(a) * CROWN_RADIUS, 0, math.sin(a) * CROWN_RADIUS),
               rotation=(-16 * math.sin(a), 0, 16 * math.cos(a)))
        m.cube(spike, (-0.75, -height, -0.75), (1.5, height, 1.5), "spike")

    # ------------------------------------------------------------------ Arme (mit Ellbogen), Schulterplatten, Klinge
    for side, sx in (("left", 1), ("right", -1)):
        arm = f"{side}_arm"
        m.bone(arm, "body", pivot=(5.6 * sx, -10.0, 0))
        m.cube(arm, (-1, -1.5, -1), (2, 15.5, 2), "limb")
        m.cube(arm, (-1.3, 12.9, -1.3), (2.6, 1.6, 2.6), "knot")                         # der Ellbogen
        pauldron = f"{side}_pauldron"
        m.bone(pauldron, arm, pivot=(0.6 * sx, -1.2, 0), rotation=(0, 0, -12 * sx))
        m.cube(pauldron, (-3.0, -1.4, -2.9), (6.0, 2.2, 5.8), "plate")
        m.cube(pauldron, (-2.6, 0.8, -3.0), (5.2, 1.6, 6.0), "plate")
        for k, (dx, dz, h, rx, rz) in enumerate(((0.9, -1.0, 3.0, 10, 14), (-0.6, 1.3, 2.2, -12, 6))):
            shard = f"{side}_float_{k}"
            m.bone(shard, pauldron, pivot=(dx * sx, -3.2 - k * 0.5, dz), rotation=(rx, 0, rz * sx))
            m.cube(shard, (-0.55, -h, -0.55), (1.1, h, 1.1), "spike")
        fore = f"{side}_forearm"
        m.bone(fore, arm, pivot=(0, 14, 0))
        m.cube(fore, (-1, 0, -1), (2, 14.5, 2), "limb")
        m.cube(fore, (-1.45, 5.6, -1.45), (2.9, 6.8, 2.9), "bracer")
        for fx, fz, length in ((-0.95, -0.95, 4.6), (0.35, -0.95, 4.6), (-0.3, 0.35, 3.9)):
            m.cube(fore, (fx, 14.0, fz), (0.6, length, 0.6), "finger")
    # in der Rechten die Phasenklinge (zeigt nach vorn, wenn der Arm hängt; die Haltungen drehen sie)
    m.bone("blade", "right_forearm", pivot=(0, 15.6, -0.2), rotation=(90, 0, 0))
    m.cube("blade", (-0.65, -2.4, -0.65), (1.3, 4.8, 1.3), "grip")
    m.cube("blade", (-0.95, 2.4, -0.95), (1.9, 1.9, 1.9), "gem")                         # der Knauf
    m.cube("blade", (-3.3, -3.7, -1.0), (6.6, 1.3, 2.0), "guard")                        # die Parierstange
    m.cube("blade", (-0.85, -4.9, -1.3), (1.7, 1.2, 2.6), "guard")
    m.cube("blade", (-0.55, -4.9 - BLADE_LENGTH, -BLADE_WIDTH / 2), (1.1, BLADE_LENGTH, BLADE_WIDTH), "blade")

    # ------------------------------------------------------------------ Beine (mit Knien)
    for side, sx in (("left", 1), ("right", -1)):
        leg = f"{side}_leg"
        m.bone(leg, "body", pivot=(2.2 * sx, 0.5, 0))
        m.cube(leg, (-1.1, 0, -1.1), (2.2, 14.5, 2.2), "limb")
        m.cube(leg, (-1.35, 13.2, -1.35), (2.7, 1.6, 2.7), "knot")                       # das Knie
        shin = f"{side}_shin"
        m.bone(shin, leg, pivot=(0, 14.5, 0))
        m.cube(shin, (-1, 0, -1), (2, 12, 2), "limb")
        m.cube(shin, (-1.45, 1.2, -1.45), (2.9, 7.4, 2.9), "greave")
        m.cube(shin, (-1.3, 10.5, -3.0), (2.6, 1.5, 3.6), "foot")
    return m


# =============================================================================================
# Hilfen
# =============================================================================================

def _uv(ctx: Ctx) -> tuple[float, float]:
    return (ctx.s + 0.5) / max(ctx.w, 1), (ctx.t + 0.5) / max(ctx.h, 1)


def _lit(ctx: Ctx, key: str, v: float, strength: float = 1.0):
    pal = ctx.pal[key]
    col = pal[max(0, min(len(pal) - 1, int(v * len(pal))))]
    ctx.glow = col if strength >= 1.0 else mix((0, 0, 0, 255), col, strength)
    return col


def _void(ctx: Ctx, base: float = 0.3):
    """Haut wie Nachthimmel: fast schwarz, ein Hauch türkiser Nebel, darin Sterne, die schwach leuchten."""
    x, y, z = ctx.world
    v = base + (noise3(x * 0.35, y * 0.35, z * 0.35, 1501) - 0.5) * 0.12 + (hash01(ctx.u, ctx.v, 1502) - 0.5) * 0.05
    v += _LIT.get(ctx.face, 0.0) * 0.6
    col = pick(ctx.pal["void"], v)
    neb = noise3(x * 0.22 + 40, y * 0.22, z * 0.22, 1503)
    if neb > 0.64:
        col = mix(col, pick(ctx.pal["nebula"], 0.3 + (neb - 0.64) * 1.5), min(0.5, (neb - 0.64) * 2.2))
    r = hash01(ctx.u, ctx.v, 1504)
    if r > 0.985:
        return _lit(ctx, "star", ((r - 0.985) / 0.015) ** 2)
    return col


def _steel(ctx: Ctx, base: float = 0.36):
    """Leerenstahl: fast schwarz mit einem Hauch Violett, oben heller, gebürstet."""
    v = base + _LIT.get(ctx.face, 0.0) - rel_y(ctx) * 0.08 + (hash01(ctx.s // 2, ctx.t, 1511) - 0.5) * 0.05
    return pick(ctx.pal["steel"], v)


def _crack(ctx: Ctx, seed: int, width: float = 0.018):
    """Feine türkise Risse (Linien im Raumrauschen); None, wo keiner ist."""
    x, y, z = ctx.world
    n = noise3(x * 0.9, y * 0.9, z * 0.9, seed)
    if abs(n - 0.5) < width:
        return _lit(ctx, "rift", 0.55 + (width - abs(n - 0.5)) / width * 0.35)
    return None


# =============================================================================================
# Kopf und Krone
# =============================================================================================

def _head(ctx: Ctx):
    u, v = _uv(ctx)
    if ctx.face == "front":
        # zwei schmale, schräge Augen (innen tiefer als außen), türkis wie der Phasit
        for cu, slant in ((0.28, 1.0), (0.72, -1.0)):
            du = u - cu
            dv = v - (0.52 + du * slant * 0.45)
            if abs(du) < 0.17 and abs(dv) < 0.05:
                core = 1.0 - max(abs(du) / 0.17, abs(dv) / 0.05)
                return _lit(ctx, "eye", 0.5 + core * 0.55)
            if abs(du) < 0.2 and abs(dv) < 0.1:
                return mix(pick(ctx.pal["void"], 0.05), pick(ctx.pal["eye"], 0), 0.45)
        if v > 0.74:
            return _void(ctx, 0.22)
        return _void(ctx, 0.3)
    return _void(ctx, 0.3)


def _jaw(ctx: Ctx):
    """Der Kiefer: nur zu sehen, wenn sich der Kopf hebt – ein Schlund voller dünner Zähne und türkisem Licht."""
    u, v = _uv(ctx)
    if ctx.face == "front":
        if 0.28 < v < 0.9 and 0.12 < u < 0.88:
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


def _circlet(ctx: Ctx):
    """Der Stirnreif: Leerenstahl, oben und unten ein Purpursaum, dazwischen kleine Phasite."""
    if ctx.face in ("top", "bottom"):
        return _steel(ctx, 0.42 if ctx.face == "top" else 0.2)
    if ctx.t == 0 or ctx.t == ctx.h - 1:
        return pick(ctx.pal["purpur"], 0.5 + (0.1 if ctx.t == 0 else -0.1))
    if ctx.s % 5 == 2:
        return _lit(ctx, "crystal", 0.62)
    return _steel(ctx, 0.4)


def _spike(ctx: Ctx):
    """Ein Phasitsplitter (Krone, Schultern): oben spitz, unten stumpf, die Kanten hell, die Spitze leuchtet."""
    o, size, p = ctx.cube.origin, ctx.cube.size, ctx.local
    f = (p[1] - o[1]) / size[1]                                                         # 0 oben … 1 unten
    if ctx.face == "top":
        return None
    if ctx.face == "bottom":
        return pick(ctx.pal["crystal"], 0.3)
    across = abs((p[0] - o[0]) / size[0] - 0.5) * 2.0 if ctx.face in ("front", "back") else abs((p[2] - o[2]) / size[2] - 0.5) * 2.0
    taper = max(0.0, 1.0 - f * 2.2)                                                    # nur das obere Stück läuft spitz zu
    if across > 1.05 - taper:
        return None
    edge = across > 0.55 - taper * 0.35
    v = 0.42 + (1.0 - f) * 0.38 + (0.14 if edge else 0.0) + (hash01(ctx.u, ctx.v, 1521) - 0.5) * 0.1
    if f < 0.18:
        return _lit(ctx, "crystal", min(0.99, v + 0.2))
    return _lit(ctx, "crystal", v, 0.75)


def _gem(ctx: Ctx):
    """Ein gefasster Phasit: geschliffen, oben links ein Glanz, innen ein violetter Funke."""
    u, v = _uv(ctx)
    if ctx.face == "front" and abs(u - 0.6) < 0.18 and abs(v - 0.6) < 0.18:
        return _lit(ctx, "spark", 1.0 if abs(u - 0.6) < 0.08 and abs(v - 0.6) < 0.08 else 0.5)
    if ctx.face == "front" and u < 0.3 and v < 0.3:
        return _lit(ctx, "crystal", 0.99)
    val = 0.5 + (1.0 - v) * 0.25 + _LIT.get(ctx.face, 0.0)
    return _lit(ctx, "crystal", val)


# =============================================================================================
# Rumpf
# =============================================================================================

def _torso(ctx: Ctx):
    u, v = _uv(ctx)
    if ctx.face == "front":
        # der Riss über dem Bauch (der Panzer deckt die Brust): senkrecht, gezackt, darin die türkis leuchtende Leere
        path = 0.5 + 0.035 * math.sin(v * 41.0) + 0.02 * math.sin(v * 97.0 + 1.0)
        width = 0.09 * math.sin(math.pi * min(1.0, max(0.0, (v - 0.4) / 0.5)))
        if 0.4 < v < 0.9 and abs(u - path) < width:
            k = 1.0 - abs(u - path) / max(width, 1e-6)
            return _lit(ctx, "rift", 0.3 + k * 0.7)
        if 0.4 < v < 0.9 and abs(u - path) < width + 0.07:
            return mix(_void(ctx, 0.2), pick(ctx.pal["rift"], 1), 0.4)
    return _void(ctx, 0.32 if ctx.face != "back" else 0.26)


def _yoke(ctx: Ctx):
    """Der Schultergrat: Leerenstahl mit Graten, oben ein Purpursaum."""
    if ctx.face == "bottom":
        return _steel(ctx, 0.12)
    if ctx.face == "top":
        return _steel(ctx, 0.5) if (ctx.s // 2) % 3 else pick(ctx.pal["purpur"], 0.4)
    if ctx.t == 0:
        return pick(ctx.pal["purpur"], 0.55)
    return _steel(ctx, 0.38)


def _plate(ctx: Ctx):
    """Platten aus Leerenstahl: unten ein Purpursaum, darüber feine türkis leuchtende Risse."""
    ry = rel_y(ctx)
    if ctx.face == "bottom":
        return _steel(ctx, 0.12)
    if ry > 0.82 and ctx.face != "top":
        return pick(ctx.pal["purpur"], 0.45 + (0.1 if ctx.t == ctx.h - 1 else 0.0))
    crack = _crack(ctx, 1531) if ctx.face != "top" else None
    if crack is not None:
        return crack
    v = 0.4 + _LIT.get(ctx.face, 0.0) - ry * 0.12
    if ctx.s == 0 or ctx.s == ctx.w - 1:
        v += 0.08                                                                      # die Kanten fangen das Licht
    return pick(ctx.pal["steel"], v + (hash01(ctx.s, ctx.t, 1532) - 0.5) * 0.04)


def _belt(ctx: Ctx):
    if ctx.face in ("top", "bottom"):
        return _steel(ctx, 0.25)
    if ctx.t == 0 or ctx.t == ctx.h - 1:
        return pick(ctx.pal["purpur"], 0.38)
    return _steel(ctx, 0.34) if ctx.s % 4 else pick(ctx.pal["purpur"], 0.3)


def _collar(ctx: Ctx, wing: bool = False):
    """Der Stehkragen: außen Leerentuch mit Purpursaum, innen (zum Kopf hin) ein türkiser Strahlenkranz."""
    u, v = _uv(ctx)
    if ctx.face == "front":
        # Strahlen von unten Mitte (dort sitzt der Kopf) nach außen
        cx, cy = (0.5, 1.05) if not wing else ((0.0 if ctx.cube.origin[0] >= 0 else 1.0), 1.05)
        ang = math.atan2(cy - v, u - cx)
        ray = abs(math.sin(ang * 7.0))
        d = math.hypot(u - cx, (cy - v) * 0.9)
        if ray > 0.86 and d > 0.25:
            return _lit(ctx, "rift", 0.45 + (1.0 - min(1.0, d)) * 0.35, 0.85)
        return pick(ctx.pal["cloth"], 0.16 + (1.0 - v) * 0.05)
    if ctx.face == "top" or (ctx.face in ("back", "west", "east") and ctx.t <= 1):
        return pick(ctx.pal["purpur"], 0.52)
    if ctx.face == "back" and (ctx.s == 0 or ctx.s == ctx.w - 1):
        return pick(ctx.pal["purpur"], 0.4)
    return pick(ctx.pal["cloth"], 0.3 + _LIT.get(ctx.face, 0.0) + (noise3(*ctx.world, 1541) - 0.5) * 0.08)


def _collar_main(ctx: Ctx):
    return _collar(ctx)


def _collar_wing(ctx: Ctx):
    return _collar(ctx, True)


def _mantle(ctx: Ctx, low: bool = False):
    """Der Königsmantel: Leerentuch mit Falten und Sternenstaub, die Ränder in Purpur, türkis leuchtende Fäden; innen dunkel;
    unten (low) in Fetzen, deren Fransen glimmen."""
    x, y, z = ctx.world
    if ctx.face == "front":
        return pick(ctx.pal["cloth"], 0.1 + (noise3(x * 0.4, y * 0.2, z, 1551) - 0.5) * 0.06)       # innen
    if ctx.face == "top":
        return pick(ctx.pal["purpur"], 0.5)
    ry = rel_y(ctx)
    if low:
        col_i = int(_uv(ctx)[0] * 16)
        end = 0.62 + hash01(col_i, 1552) * 0.36
        if ry > end:
            return None
        if ry > end - 0.07:
            return _lit(ctx, "rift", 0.42, 0.8)
        if noise3(x * 0.8, y * 0.35, z, 1553) > 0.74 and ry > 0.3:
            return None
    edge = min(ctx.s, ctx.w - 1 - ctx.s)
    if ctx.face == "back" and edge <= 1:
        return pick(ctx.pal["purpur"], 0.42 + (0.12 if edge == 0 else 0.0))
    if ctx.face == "back" and not low and ctx.t <= 1:
        return pick(ctx.pal["purpur"], 0.5)
    if ctx.face == "back" and ctx.s % 9 == 4 and hash01(ctx.s, ctx.t // 4, 1554) > 0.4:
        return _lit(ctx, "rift", 0.5, 0.7)                                             # leuchtende Fäden
    fold = 0.5 + 0.5 * math.sin(x * 1.5)
    v = 0.24 + 0.22 * fold + _LIT.get(ctx.face, 0.0) - ry * 0.08 + (hash01(ctx.u, ctx.v, 1555) - 0.5) * 0.04
    if hash01(ctx.u, ctx.v, 1556) > 0.992:
        return _lit(ctx, "star", 0.6)                                                  # Sternenstaub
    return pick(ctx.pal["cloth"], v)


def _mantle_top(ctx: Ctx):
    return _mantle(ctx)


def _mantle_low(ctx: Ctx):
    return _mantle(ctx, True)


def _tabard(ctx: Ctx):
    """Der Wappenrock: Purpur mit dunklem Rand, darauf das Auge der Leerwandler (türkis), unten ausgefranst."""
    u, v = _uv(ctx)
    if ctx.face in ("top", "bottom"):
        return pick(ctx.pal["purpur"], 0.2)
    if v > 0.93 and hash01(ctx.s, 1561) > 0.5:
        return None
    if ctx.face == "front":
        if u < 0.14 or u > 0.86 or v < 0.06:
            return pick(ctx.pal["cloth"], 0.35)
        # das Auge: eine schmale Mandel mit Pupille, darunter drei Tropfen (die Sprünge)
        du, dv = (u - 0.5) / 0.3, (v - 0.32) / 0.11
        if du * du + dv * dv < 1.0:
            if abs(u - 0.5) < 0.07:
                return _lit(ctx, "crystal", 0.95)
            return _lit(ctx, "rift", 0.5, 0.8)
        for k, ty in enumerate((0.55, 0.68, 0.8)):
            if abs(u - 0.5) < 0.07 - k * 0.012 and abs(v - ty) < 0.035:
                return _lit(ctx, "rift", 0.45 - k * 0.08, 0.7)
        return pick(ctx.pal["purpur"], 0.32 + (hash01(ctx.s, ctx.t, 1562) - 0.5) * 0.06 - v * 0.08)
    return pick(ctx.pal["purpur"], 0.25)


# =============================================================================================
# Glieder
# =============================================================================================

def _limb(ctx: Ctx):
    return _void(ctx, 0.3)


def _knot(ctx: Ctx):
    if ctx.face not in ("top", "bottom") and ctx.t == ctx.h // 2:
        return _lit(ctx, "rift", 0.55)
    return _void(ctx, 0.42)


def _bracer(ctx: Ctx):
    """Arm- und Beinschienen: Leerenstahl, Purpur an den Rändern, in der Mitte eine leuchtende Naht."""
    if ctx.face in ("top", "bottom"):
        return pick(ctx.pal["purpur"], 0.4)
    if ctx.t == 0 or ctx.t == ctx.h - 1:
        return pick(ctx.pal["purpur"], 0.48)
    if ctx.t == ctx.h // 2:
        return _lit(ctx, "rift", 0.62)
    return _steel(ctx, 0.36 + (0.06 if ctx.s == 0 else 0.0))


def _finger(ctx: Ctx):
    f = (ctx.local[1] - ctx.cube.origin[1]) / ctx.cube.size[1]
    if f > 0.82:
        return _lit(ctx, "eye", 0.3)
    return _void(ctx, 0.36)


def _foot(ctx: Ctx):
    if ctx.face == "top":
        return _steel(ctx, 0.4)
    return _steel(ctx, 0.28)


# =============================================================================================
# Die Phasenklinge
# =============================================================================================

def _grip(ctx: Ctx):
    """Der Griff: in violettes Leder gewickelt (schräge Bänder)."""
    band = (ctx.t + ctx.s) % 3
    return pick(ctx.pal["leather"], 0.32 + (0.16 if band == 0 else 0.0) + _LIT.get(ctx.face, 0.0))


def _guard(ctx: Ctx):
    """Die Parierstange: Purpur, geschliffen wie Gold, in der Mitte ein türkiser Funke."""
    u, v = _uv(ctx)
    if ctx.face in ("front", "back") and abs(u - 0.5) < 0.09 and ctx.cube.size[0] > 4:
        return _lit(ctx, "crystal", 0.7)
    val = 0.5 + _LIT.get(ctx.face, 0.0) + (0.12 if ctx.t == 0 else 0.0) - (0.1 if ctx.t == ctx.h - 1 else 0.0)
    if (ctx.s + ctx.t) % 5 == 0:
        val += 0.08
    return pick(ctx.pal["purpur"], val)


def _blade(ctx: Ctx):
    """Die Klinge: ein einziger Phasitkristall – Mittelgrat hell, Schneiden gleißend, zur Spitze hin spitz zulaufend; im
    Inneren treiben violette Funken."""
    o, size, p = ctx.cube.origin, ctx.cube.size, ctx.local
    f = (p[1] - o[1]) / size[1]                                                         # 0 Spitze … 1 Heft
    if ctx.face in ("top",):
        return None
    if ctx.face == "bottom":
        return pick(ctx.pal["crystal"], 0.35)
    if ctx.face in ("west", "east"):
        # Breitseite: Spitze als Dreieck, Schneiden hell, Mittelgrat
        across = abs((p[2] - o[2]) / size[2] - 0.5) * 2.0
        tip = min(1.0, f / 0.16)
        if across > tip * 1.02:
            return None
        if across > tip * 0.78:
            return _lit(ctx, "crystal", 0.9)                                            # die Schneiden
        if across < 0.14:
            return _lit(ctx, "crystal", 0.78, 0.8)                                      # der Mittelgrat
        if hash01(ctx.s, ctx.t, ctx.face, 1571) > 0.965:
            return _lit(ctx, "spark", 0.6, 0.8)
        band = noise3(p[0] * 0.5, p[1] * 0.35, p[2] * 0.5, 1572)
        return _lit(ctx, "crystal", 0.42 + band * 0.22 + (1.0 - f) * 0.08, 0.6)
    # Schmalseite (Schneide): hell, an der Spitze fort
    if f < 0.16 * (1.0 - abs((p[0] - o[0]) / size[0] - 0.5) * 2.0):
        return None
    return _lit(ctx, "crystal", 0.86)


def materials(variant: str) -> dict:
    return {"head": _head, "jaw": _jaw, "circlet": _circlet, "spike": _spike, "gem": _gem, "torso": _torso, "yoke": _yoke,
            "plate": _plate, "belt": _belt, "collar": _collar_main, "collar_wing": _collar_wing, "mantle": _mantle_top,
            "mantle_low": _mantle_low, "tabard": _tabard, "limb": _limb, "knot": _knot, "bracer": _bracer, "greave": _bracer,
            "finger": _finger, "foot": _foot, "grip": _grip, "guard": _guard, "blade": _blade}


def hidden(variant: str) -> list[str]:
    return []
