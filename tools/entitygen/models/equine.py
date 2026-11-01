"""
Gemeinsamer Bauplan der Pferdeartigen (Pferd, Esel, Maultier).

Das Skelett entspricht Knochen für Knochen dem Vanilla-Pferd (gleiche Namen, Drehpunkte und Grundquader), damit Sattel,
Rossharnisch und Packtaschen-Sattel von Vanilla exakt passen und die Vanilla-Animationen (Steigen, Grasen, Schweif)
greifen. Darüber liegen eigene Teile: volle Mähne, Stirnschopf, lange Seitenmähne, Fesselbehang, Schweifquaste,
Packtaschen. Die Bemalung ist vollständig eigen (Abzeichen, Apfelschimmel-Tupfen, Scheckung, Aalstrich, Fesseln).
"""
from __future__ import annotations

import math

from ..geometry import Model
from ..paint import Ctx, hash01, mix, noise3, pick, rel_y
from . import farm

LEGS = ("left_hind_leg", "right_hind_leg", "left_front_leg", "right_front_leg")


def skeleton(m: Model, *, donkey: bool = False) -> None:
    m.bone("body", pivot=(0, 11, 5))
    m.cube("body", (-5, -8, -17), (10, 10, 22), "coat", inflate=0.05)

    m.bone("head_parts", pivot=(0, 4, -12), rotation=(30, 0, 0))
    m.cube("head_parts", (-2.05, -6, -2), (4, 12, 7), "neck")
    m.bone("head", "head_parts")
    m.cube("head", (-3, -11, -2), (6, 5, 7), "head")
    m.bone("upper_mouth", "head_parts")
    m.cube("upper_mouth", (-2, -11, -7), (4, 5, 5), "mouth")
    m.bone("mane", "head_parts")
    if donkey:
        m.cube("mane", (-1, -11.2, 4.6), (2, 15.5, 2.2), "mane")              # kurze Stehmähne
    else:
        m.cube("mane", (-1.15, -11.6, 4.5), (2.3, 16.8, 2.7), "mane")         # volle Kammmähne
        m.bone("mane_drape", "head_parts")                                     # lange, seitlich fallende Mähne
        m.cube("mane_drape", (1.9, -10.6, 2.4), (1.0, 15.2, 4.6), "mane")
        m.bone("forelock", "head")                                             # Stirnschopf
        m.cube("forelock", (-1.5, -11.5, 1.2), (3, 0.6, 3.6), "mane")
        m.bone("forelock_big", "head")
        m.cube("forelock_big", (-2.0, -11.8, -0.6), (4, 0.9, 5.4), "mane")
        m.cube("forelock_big", (-1.5, -11.0, -1.4), (3, 1.6, 0.9), "mane")

    if donkey:
        for side, name in ((1, "left_ear"), (-1, "right_ear")):
            m.bone(name, "head", pivot=(1.25 * side, -10, 4), rotation=(15, 0, 15 * side))
            m.cube(name, (-1, -7, 0), (2, 7, 1), "ear")
            m.bone(name + "_mule", "head", pivot=(1.25 * side, -10, 4), rotation=(12, 0, 13 * side))
            m.cube(name + "_mule", (-1, -5, 0), (2, 5, 1), "ear")
    else:
        for side, name in ((1, "left_ear"), (-1, "right_ear")):
            m.bone(name, "head", pivot=(1.55 * side, -11, 4.5))
            m.cube(name, (-1, -2, -0.5), (2, 3, 1), "ear")

    for name, x, z, front in (("left_hind_leg", 4, 7, False), ("right_hind_leg", -4, 7, False),
                              ("left_front_leg", 4, -10, True), ("right_front_leg", -4, -10, True)):
        m.bone(name, pivot=(x, 14, z))
        ox = -3 if x > 0 else -1
        oz = -1.9 if front else -1
        m.cube(name, (ox, -1.01, oz), (4, 11, 4), "leg")
        if not donkey:
            m.bone("feather_" + name, name)                                    # Fesselbehang der Kaltblüter
            m.cube("feather_" + name, (ox - 0.45, 5.6, oz - 0.45), (4.9, 3.7, 4.9), "feather")

    m.bone("tail", "body", pivot=(0, -5, 2), rotation=(30, 0, 0))
    if donkey:
        m.cube("tail", (-0.8, 0, 1.2), (1.6, 10, 1.6), "tail_core")
        m.bone("tail_tuft", "tail")
        m.cube("tail_tuft", (-1.5, 8.5, 0.5), (3, 5, 3), "tail")
        m.bone("tail_hair", "tail")                                           # Maultier: Pferdeschweif
        m.cube("tail_hair", (-1.5, 0, 0), (3, 14, 4), "tail")
    else:
        m.cube("tail", (-1.5, 0, 0), (3, 14, 4), "tail")
        m.cube("tail", (-2.0, 9.0, -0.2), (4, 6.5, 4.2), "tail")              # ausfallende Schweifspitze

    if donkey:
        for side, name in ((1, "left_chest"), (-1, "right_chest")):
            m.bone(name, "body", pivot=(6 * side, -8, 0), rotation=(0, -90 * side, 0))
            m.cube(name, (-4, 0, -2), (8, 8, 3), "chest")


# =============================================================================================
# Maler
# =============================================================================================

def face_plane(ctx: Ctx) -> tuple[float, float]:
    x, y, z = ctx.world
    if ctx.face in ("front", "back"):
        return x, y
    if ctx.face in ("west", "east"):
        return z, y
    return x, z


def spots(ctx: Ctx, cell: float, seed: int) -> float:
    """Abstand (in Zellen) zum nächsten Tupfen-Mittelpunkt in der Ebene der Fläche – für runde Tupfen."""
    a, b = face_plane(ctx)
    best = 9.0
    ca, cb = math.floor(a / cell), math.floor(b / cell)
    for da in (-1, 0, 1):
        for db in (-1, 0, 1):
            ia, ib = ca + da, cb + db
            pa = (ia + 0.2 + hash01(ia, ib, seed) * 0.6) * cell
            pb = (ib + 0.2 + hash01(ib, ia, seed + 1) * 0.6) * cell
            best = min(best, math.hypot(a - pa, b - pb) / cell)
    return best


def coat(features: dict, belly: bool = True):
    """Fell des Rumpfs, Halses und Kopfes – mit Glanzlicht und je nach Rasse Tupfen, Platten oder Aalstrich."""
    base = farm.fur("fur", "belly" if belly else None, seed=81, contrast=features.get("contrast", 0.18), top_dark=0.04,
                    belly_from=0.8)
    if features.get("pinto"):
        base = farm.patched(base, "white", scale=0.15, threshold=0.5, seed=features.get("pinto_seed", 84), soft=0.03)

    def paint(ctx: Ctx):
        col = base(ctx)
        if col is None:
            return col
        x, y, z = ctx.world
        ry = rel_y(ctx)
        if ctx.face in ("west", "east") and 0.1 < ry < 0.3 and not features.get("pinto") and ctx.cube.bone.name == "body":
            col = mix(col, pick(ctx.pal["fur"], 0.86), 0.12)                     # Glanz auf dem Rücken
        if features.get("dapples") and ctx.cube.bone.name in ("body", "head_parts"):
            d = spots(ctx, 3.4, 85)
            if d < 0.24:
                col = mix(col, pick(ctx.pal["white"], 0.72), 0.8)                  # runde Apfel-Tupfen
            elif d < 0.36:
                col = mix(col, pick(ctx.pal["white"], 0.55), 0.35)
        if features.get("dorsal") and ctx.face == "top" and abs(x) < 1.05:
            col = pick(ctx.pal["mane"], 0.4 + hash01(ctx.u, ctx.v) * 0.12)       # Aalstrich
        if features.get("cross") and ctx.face in ("west", "east", "top") and -9.5 < z < -7.5 and ctx.cube.bone.name == "body":
            if ctx.face == "top" or ry < 0.45:
                col = mix(col, pick(ctx.pal["mane"], 0.4), 0.7)                   # Schulterkreuz des Esels
        if features.get("mealy") and ctx.face in ("bottom",):
            col = pick(ctx.pal["belly"], 0.7 + hash01(ctx.u, ctx.v) * 0.12)
        return col

    return paint


def neck(features: dict):
    c = coat(features, belly=False)

    def paint(ctx: Ctx):
        if features.get("mealy") and ctx.face == "front":
            return mix(c(ctx), pick(ctx.pal["belly"], 0.6), 0.22)                  # etwas hellere Kehle
        return c(ctx)

    return paint


def head(features: dict):
    c = coat(features, belly=False)
    marking = features.get("face")        # None, "star", "blaze", "bald"

    def paint(ctx: Ctx):
        lx, ly, lz = ctx.local
        # Augen auf den Seiten: groß, dunkel, mit Glanzpunkt und Lid
        if ctx.face in ("west", "east") and 1.0 <= lz < 3.0:
            if -10.0 <= ly < -9.0:
                return pick(ctx.pal["eye"], 0.08) if lz >= 2.0 else pick(ctx.pal["eye"], 0.35)
            if -11.0 <= ly < -10.0 and lz >= 2.0 and features.get("eyelid", True):
                return mix(c(ctx), pick(ctx.pal["eye"], 0.1), 0.45)
        if features.get("mealy") and ctx.face in ("west", "east") and 1.0 <= lz < 3.0 and -9.0 <= ly < -8.0:
            return mix(c(ctx), pick(ctx.pal["belly"], 0.75), 0.5)                  # helle Unterlider (Esel)
        if ctx.face == "top" and marking:
            if marking == "star" and abs(lx) < 1.0 and 0.5 <= lz < 2.5:
                return pick(ctx.pal["white"], 0.8 + hash01(ctx.u, ctx.v) * 0.12)
            if marking == "blaze" and abs(lx) < 1.0 and lz < 3.5:
                return pick(ctx.pal["white"], 0.8 + hash01(ctx.u, ctx.v) * 0.12)
            if marking == "bald" and abs(lx) < 2.0 and lz < 4.0:
                return pick(ctx.pal["white"], 0.8 + hash01(ctx.u, ctx.v) * 0.12)
        if ctx.face == "bottom":
            return mix(c(ctx), pick(ctx.pal["fur"], 0.3), 0.3)
        return c(ctx)

    return paint


def mouth(features: dict):
    c = coat(features, belly=False)
    marking = features.get("face")

    def paint(ctx: Ctx):
        lx, ly, lz = ctx.local
        white = marking in ("blaze", "bald") and (abs(lx) < (0.9 if marking == "blaze" else 1.9))
        skin = "skin_pink" if white or features.get("pink_nose") else "skin"
        if ctx.face == "front":
            if -10.0 <= ly < -9.0 and (ctx.s == 0 or ctx.s == ctx.w - 1):
                return pick(ctx.pal["eye"], 0.06)                                  # Nüstern
            if -8.0 <= ly < -7.0:
                return pick(ctx.pal[skin], 0.2)                                    # Maulspalte
            if white and ly < -8.0:
                return pick(ctx.pal["white"], 0.78)
            return pick(ctx.pal[skin], 0.45 + hash01(ctx.u, ctx.v) * 0.12)
        if ctx.face == "top":
            if white:
                return pick(ctx.pal["white"], 0.8 + hash01(ctx.u, ctx.v) * 0.12)
            if lz < -5.5:
                return mix(c(ctx), pick(ctx.pal[skin], 0.45), 0.6)
            return c(ctx)
        if ctx.face in ("west", "east"):
            if -8.0 <= ly < -7.0 and lz < -4.0:
                return pick(ctx.pal[skin], 0.22)                                   # Maulwinkel
            if lz < -5.5:
                return mix(c(ctx), pick(ctx.pal[skin], 0.45), 0.7)
            if features.get("mealy") and ly > -9.0:
                return mix(c(ctx), pick(ctx.pal["belly"], 0.75), 0.7)
            return c(ctx)
        if ctx.face == "bottom":
            return mix(c(ctx), pick(ctx.pal[skin], 0.4), 0.5)
        return c(ctx)

    return paint


def leg(features: dict):
    c = coat(features, belly=False)
    socks = features.get("socks", {})
    points = features.get("points", False)
    stripes = features.get("leg_stripes", False)

    def paint(ctx: Ctx):
        ry = rel_y(ctx)
        name = ctx.cube.bone.name
        sock = socks.get(name, 0.0)
        if ctx.face == "bottom" or ry > 0.885:
            key = "hoof_light" if sock > 0 else "hoof"
            v = 0.35 + hash01(ctx.u, ctx.v) * 0.15
            if ctx.face != "bottom" and (ctx.s % 2 == 0):
                v += 0.08                                                          # Hornrillen
            return pick(ctx.pal[key], v)
        if sock > 0 and ry > 1.0 - sock - 0.115:
            edge = (ry - (1.0 - sock - 0.115)) < 0.06 and hash01(ctx.u, ctx.v, 3) > 0.5
            return pick(ctx.pal["white"], 0.62 if edge else 0.78 + hash01(ctx.u, ctx.v) * 0.12)
        if points and ry > 0.42:
            t = min(1.0, (ry - 0.42) / 0.12)
            return mix(c(ctx), pick(ctx.pal["points"], 0.4 + hash01(ctx.u, ctx.v) * 0.12), t)
        if stripes and 0.45 < ry < 0.85 and math.floor(ctx.world[1] * 0.9) % 3 == 0:
            return mix(c(ctx), pick(ctx.pal["mane"], 0.4), 0.55)                   # Zebrastreifen (Falbe, Esel)
        if ry < 0.08 and ctx.face != "top":
            return mix(c(ctx), pick(ctx.pal["fur"], 0.35), 0.3)                    # Schatten unter dem Rumpf
        return c(ctx)

    return paint


def ear(features: dict):
    c = coat(features, belly=False)

    def paint(ctx: Ctx):
        ry = rel_y(ctx)
        if features.get("ear_tips") and ry < 0.3:
            return pick(ctx.pal["mane"], 0.35)
        if ctx.face == "front" and 0 < ctx.s < ctx.w - 1 and ry > 0.25:
            inner = "belly" if features.get("mealy") else "fur"
            return pick(ctx.pal[inner], 0.3 + hash01(ctx.u, ctx.v) * 0.12)
        if ctx.face in ("west", "east") and features.get("ear_rims"):
            return pick(ctx.pal["mane"], 0.38)
        return c(ctx)

    return paint


def hair(key: str = "mane", seed: int = 91, highlights: bool = True):
    """Langhaar (Mähne, Schweif): durchgehende Strähnen mit Glanzlichtern und dunkleren Spitzen."""
    sh = farm.shaggy(key, seed=seed, length=3.2, contrast=0.36)

    def paint(ctx: Ctx):
        col = sh(ctx)
        if highlights and ctx.face in ("west", "east", "back", "front"):
            x, y, z = ctx.world
            across = z if ctx.face in ("west", "east") else x
            lane = math.floor(across + 0.001)
            if hash01(lane, seed + 7) > 0.78:
                col = mix(col, pick(ctx.pal[key], 0.9), 0.4)
        return col

    return paint


def chest(ctx: Ctx):
    """Packtaschen aus Leder: Deckelklappe, mittiger Riemen mit Messingschnalle, abgesetzte Nähte."""
    v = 0.52 + (hash01(ctx.u, ctx.v) - 0.5) * 0.1
    big = ctx.face in ("front", "back")
    if big:
        mid = ctx.w // 2
        if ctx.s in (mid - 1, mid) and ctx.t <= 5:
            if ctx.t == 4:
                return pick(ctx.pal["brass"], 0.8 if ctx.s == mid - 1 else 0.6)    # Schnalle
            return pick(ctx.pal["strap"], 0.42 + (ctx.t % 2) * 0.05)               # Riemen
        if ctx.t <= 2:
            if ctx.t == 2 and 0 < ctx.s < ctx.w - 1:
                return pick(ctx.pal["leather"], 0.3)                               # Klappenkante
            return pick(ctx.pal["leather"], 0.4 + (hash01(ctx.u, ctx.v) - 0.5) * 0.1)
        if ctx.t == ctx.h - 1 or ctx.s == 0 or ctx.s == ctx.w - 1:
            return pick(ctx.pal["leather"], 0.34)
        if ctx.t == ctx.h - 2 and ctx.s % 2 == 1:
            return pick(ctx.pal["leather"], 0.7)                                   # Ziernaht
    elif ctx.face == "top":
        return pick(ctx.pal["leather"], 0.4)
    return pick(ctx.pal["leather"], v)
