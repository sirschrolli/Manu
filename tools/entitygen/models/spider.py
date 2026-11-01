"""
Spinnen auf dem Skelett der Vanilla-Spinne (Kopf, Taille "body0", Hinterleib "body1", acht Beine mit den
Vanilla-Namen, -Drehpunkten und -Grundwinkeln – so laufen und klettern sie wie bei Vanilla, und die Höhlenspinne ist
dieselbe Geometrie, nur kleiner gezeichnet).

Anders als bei Vanilla sind die Beine geknickt: Hüfte im Vanilla-Teil, Schenkel und Schiene als Kind-Knochen – die
Knie stehen über dem Körper, die Spitzen treten auf den Boden. Dazu Kieferklauen mit Giftzähnen, Taster und Spinnwarzen.
Acht Augen leuchten (eigene Leucht-Textur für die Augen-Ebene).

  * Waldspinne: dunkelbraun, behaart, Blattzeichnung auf dem Hinterleib, geringelte Beine
  * Wüstenspinne: sandfarbene Vogelspinne mit dunklen Winkeln, dicht behaart
  * Dschungelspinne: schwarz mit roten Querbinden, orange Knie, rote Sanduhr am Bauch
  * Schneespinne: weißgrau mit blaugrauer Rautenzeichnung
  * Höhlenspinne: dunkel blaugrün, giftgrün leuchtende Flecken und Augen
"""
from __future__ import annotations

import math

from ..geometry import Model
from ..paint import Ctx, hash01, mix, noise3, pick, ramp

PALETTES = {
    "spinne_wald": {"chitin": ramp("#140d08", "#6e5034", steps=7), "hair": ramp("#3e2c1c", "#b8986c"),
                    "mark": ramp("#5a4026", "#d2b07c"), "band": ramp("#6a5034", "#d8bc8a"), "eye": ramp("#5a0204", "#ff3a2a"),
                    "fang": ramp("#060404", "#5a3a2a"), "belly": ramp("#1e150e", "#6a5038")},
    "spinne_wueste": {"chitin": ramp("#3a2814", "#c8a46a", steps=7), "hair": ramp("#7a5a34", "#f6e2b4"),
                      "mark": ramp("#2a180c", "#7a5230"), "band": ramp("#8a6a40", "#f4dcaa"), "eye": ramp("#5a0204", "#ff3a2a"),
                      "fang": ramp("#0a0604", "#5a3a22"), "belly": ramp("#2e2012", "#8a6a40")},
    "spinne_dschungel": {"chitin": ramp("#070607", "#3c3438", steps=7), "hair": ramp("#1e1a1c", "#6a5e62"),
                         "mark": ramp("#6a1006", "#ff6a26"), "band": ramp("#8a2a06", "#ff9a3a"), "eye": ramp("#5a0204", "#ff3a2a"),
                         "fang": ramp("#040304", "#4a1a14"), "belly": ramp("#0a0809", "#2c2628")},
    "spinne_schnee": {"chitin": ramp("#5e646c", "#fafcfe", steps=7), "hair": ramp("#9aa2aa", "#ffffff"),
                      "mark": ramp("#3e4c5e", "#9cb2c6"), "band": ramp("#4a5664", "#a8b8c8"), "eye": ramp("#5a0204", "#ff3a2a"),
                      "fang": ramp("#14161a", "#5a626c"), "belly": ramp("#6a7078", "#d8dde2")},
    "hoehlenspinne": {"chitin": ramp("#040d10", "#1e4a4c", steps=7), "hair": ramp("#0e2426", "#3e7a74"),
                      "mark": ramp("#2a8a10", "#c8ff5a"), "band": ramp("#103a30", "#3a8a6a"), "eye": ramp("#1a6a04", "#d4ff6a"),
                      "fang": ramp("#020404", "#1a3a2a"), "belly": ramp("#061416", "#1c3c3c")},
}

# Vanilla-Beine: Name -> (Drehpunkt, Grundwinkel) und eigene Knicke (Schenkel hoch, Schiene runter, Längen)
_LEGS = {
    "right_hind_leg": ((-4, 15, 2), (0, 45, -45), 62, 100, 9),
    "left_hind_leg": ((4, 15, 2), (0, -45, 45), 62, 100, 9),
    "right_middle_hind_leg": ((-4, 15, 1), (0, 22.5, -33.3), 64, 108, 9),
    "left_middle_hind_leg": ((4, 15, 1), (0, -22.5, 33.3), 64, 108, 9),
    "right_middle_front_leg": ((-4, 15, 0), (0, -22.5, -33.3), 72, 118, 8),
    "left_middle_front_leg": ((4, 15, 0), (0, 22.5, 33.3), 72, 118, 8),
    "right_front_leg": ((-4, 15, -1), (0, -45, -45), 70, 126, 8),
    "left_front_leg": ((4, 15, -1), (0, 45, 45), 70, 126, 8),
}
_TIBIA = 13


def build() -> Model:
    m = Model("spider", 128, 64)
    m.bone("head", pivot=(0, 15, -3))
    m.cube("head", (-4, -4, -8), (8, 8, 8), "head")
    m.bone("chelicerae", "head")
    m.cube("chelicerae", (-2.6, 1.5, -9.2), (2, 3, 2), "chelicera")
    m.cube("chelicerae", (0.6, 1.5, -9.2), (2, 3, 2), "chelicera")
    m.cube("chelicerae", (-1.8, 4.5, -9.4), (1, 1, 1), "fang")
    m.cube("chelicerae", (0.8, 4.5, -9.4), (1, 1, 1), "fang")
    m.bone("right_palp", "head", pivot=(-3, 2.5, -7.5), rotation=(28, 14, 0))
    m.cube("right_palp", (-0.5, -0.5, -4), (1, 1, 4), "palp")
    m.bone("left_palp", "head", pivot=(3, 2.5, -7.5), rotation=(28, -14, 0))
    m.cube("left_palp", (-0.5, -0.5, -4), (1, 1, 4), "palp")

    m.bone("body0", pivot=(0, 15, 0))
    m.cube("body0", (-3, -3, -3), (6, 6, 6), "waist")
    m.bone("body1", pivot=(0, 15, 9))
    m.cube("body1", (-5, -4, -6), (10, 8, 12), "abdomen")
    m.bone("spinnerets", "body1")
    m.cube("spinnerets", (-1, 0.5, 5.6), (2, 2, 1), "spinneret")

    for name, (pivot, rot, femur_up, knee_down, femur_len) in _LEGS.items():
        right = name.startswith("right")
        side = 1 if right else -1                       # rechte Beine zeigen nach -X
        prefix = name[:-4]
        m.bone(name, pivot=pivot, rotation=rot)
        m.cube(name, (-3 if right else 0, -1, -1), (3, 2, 2), "coxa")
        m.bone(prefix + "_femur", name, pivot=(-2.5 * side, 0, 0), rotation=(0, 0, femur_up * side))
        m.cube(prefix + "_femur", (-femur_len if right else 0, -1, -1), (femur_len, 2, 2), "femur")
        m.bone(prefix + "_tibia", prefix + "_femur", pivot=(-femur_len * side, 0, 0), rotation=(0, 0, -knee_down * side))
        m.cube(prefix + "_tibia", (-_TIBIA if right else 0, -0.75, -0.75), (_TIBIA, 1.5, 1.5), "tibia")
    return m


# =============================================================================================
# Malen
# =============================================================================================

def _variant(ctx: Ctx) -> str:
    return ctx.params.get("variant")


def _hairy(ctx: Ctx, base: float = 0.42, key: str = "chitin", streak_axis: int = 1):
    """Behaarter Chitinpanzer: feine helle Haarstriche über dunklem Grund."""
    x, y, z = ctx.world
    coords = [x, y, z]
    coords[streak_axis] *= 0.35                                                            # Striche längs einer Achse
    n = noise3(coords[0] * 1.1, coords[1] * 1.1, coords[2] * 1.1, 901)
    v = base + (n - 0.5) * 0.1 + (hash01(ctx.u, ctx.v, 902) - 0.5) * 0.05
    if ctx.face == "top":
        v += 0.05
    elif ctx.face == "bottom":
        v -= 0.1
    col = pick(ctx.pal[key], v)
    h = hash01(ctx.u, ctx.v, 903)
    if h > 0.92:
        col = mix(col, pick(ctx.pal["hair"], 0.5 + (h - 0.92) * 2.5), 0.45)                 # helle Haarspitzen
    return col


def _eye(ctx: Ctx, level: float):
    col = pick(ctx.pal["eye"], level)
    ctx.glow = col
    return col


_FACE = ["........",
         "........",
         "...ee...",
         "eHM..HMe",
         ".MM..MM.",
         "e......e",
         "..c..c..",
         ".cc..cc."]


def _head(ctx: Ctx):
    face, s, t = ctx.face, ctx.s, ctx.t
    if face == "front":
        c = _FACE[t][s]
        if c == "e":
            return _eye(ctx, 0.66)
        if c == "H":
            return _eye(ctx, 1.0)                                                          # Glanzpunkt
        if c == "M":
            return _eye(ctx, 0.84)
        if c == "c":
            return pick(ctx.pal["fang"], 0.3)
        col = _hairy(ctx, 0.4)
        if t >= 2 and 2 <= s <= 5 and c == ".":
            col = mix(col, pick(ctx.pal["chitin"], 0.15), 0.35)                            # dunkles Augenfeld
        return col
    if face == "top":
        # Kopfbrust: Mittelgrube und strahlenförmige Furchen
        cx, cz = ctx.s - (ctx.w - 1) / 2, ctx.t - (ctx.h - 1) / 2
        r = math.hypot(cx, cz)
        col = _hairy(ctx, 0.46, streak_axis=2)
        if r < 0.8:
            return pick(ctx.pal["chitin"], 0.12)
        ang = math.atan2(cz, cx)
        if r > 1.4 and abs(math.sin(ang * 4)) < 0.22:
            col = mix(col, pick(ctx.pal["chitin"], 0.18), 0.5)
        if _variant(ctx) == "spinne_wald" and abs(cx) < 1.0 and cz > 0.5:
            col = mix(col, pick(ctx.pal["mark"], 0.6), 0.5)                                 # heller Mittelstreif
        if _variant(ctx) == "hoehlenspinne" and r > 2.6 and abs(cx) > 2.6 and hash01(ctx.s, ctx.t, 904) > 0.5:
            glow = pick(ctx.pal["mark"], 0.7)
            ctx.glow = glow
            return glow
        return col
    if face == "bottom":
        return pick(ctx.pal["belly"], 0.45 + hash01(ctx.u, ctx.v) * 0.1)                     # Brustschild
    return _hairy(ctx, 0.38)


def _waist(ctx: Ctx):
    return _hairy(ctx, 0.3)


def _abdomen_pattern(ctx: Ctx, xr: float, zr: float):
    """Zeichnung auf dem Hinterleib. xr: -1..1 quer, zr: 0 vorn (Taille) .. 1 hinten."""
    v = _variant(ctx)
    if v == "spinne_wald":
        # Blattzeichnung (Folium): dunkles Mittelfeld mit hellem gewelltem Rand und Winkeln nach hinten
        width = 0.55 * (1.0 - 0.55 * zr) + 0.08 * math.sin(zr * 18)
        if abs(xr) < width - 0.16:
            chevron = abs((zr * 5) % 1.0 - abs(xr) * 0.9) < 0.12 and zr > 0.3
            return ("mark", 0.75) if chevron else ("chitin", 0.12)
        if abs(xr) < width:
            return ("mark", 0.62)
        return None
    if v == "spinne_wueste":
        if abs(xr) < 0.18 and zr < 0.5:
            return ("mark", 0.45)                                                          # dunkler Herzfleck
        if zr > 0.3 and abs((zr * 3.0) % 1.0 - abs(xr) * 0.8) < 0.2 and abs(xr) < 0.8:
            return ("mark", 0.22)                                                          # Winkel
        return None
    if v == "spinne_dschungel":
        band = (zr * 3.0) % 1.0
        if 0.12 < band < 0.5 and abs(xr) < 0.92 - zr * 0.25 and zr > 0.2:
            return ("mark", 0.78 - band * 0.3)                                              # rote Querbinden
        return None
    if v == "spinne_schnee":
        # Rauten längs der Mitte
        cell = (zr * 4.0) % 1.0
        dist = abs(xr) * 1.6 + abs(cell - 0.5)
        if dist < 0.42 and zr > 0.12:
            return ("mark", 0.3 if dist < 0.25 else 0.55)
        return None
    if v == "hoehlenspinne":
        # drei Paar giftgrün leuchtender Flecken, dazu ein Mittelfleck vorn
        for zc in (0.32, 0.56, 0.8):
            if abs(zr - zc) < 0.085 and 0.28 < abs(xr) < 0.64:
                return ("glow", 0.75)
        if abs(xr) < 0.12 and abs(zr - 0.14) < 0.06:
            return ("glow", 0.55)
        return None
    return None


def _abdomen(ctx: Ctx):
    face = ctx.face
    lx, ly, lz = ctx.local
    ox, oy, oz = ctx.cube.origin
    w, h, d = ctx.cube.size
    xr = (lx - ox) / w * 2 - 1
    zr = (lz - oz) / d                                     # 0 = vorn (Taille), 1 = hinten
    col = _hairy(ctx, 0.42, streak_axis=2)
    if face == "bottom":
        col = pick(ctx.pal["belly"], 0.42 + hash01(ctx.u, ctx.v) * 0.1)
        if _variant(ctx) == "spinne_dschungel":
            # rote Sanduhr am Bauch
            dz = abs(zr - 0.5)
            if abs(xr) < 0.08 + dz * 0.9 and dz < 0.3 and abs(xr) < 0.4:
                return pick(ctx.pal["mark"], 0.75)
        if abs(xr) < 0.15 and zr < 0.2:
            return pick(ctx.pal["belly"], 0.7)                                              # Buchlungen
        return col
    if face == "front":
        return _hairy(ctx, 0.32)
    if face == "back":
        # Hinterende: dunkler, bei der Höhlenspinne ein leuchtender Ring
        col = _hairy(ctx, 0.36)
        if _variant(ctx) == "hoehlenspinne" and abs(xr) < 0.25 and ctx.t in (2, 3):
            glow = pick(ctx.pal["mark"], 0.6)
            ctx.glow = glow
            return glow
        return col
    # Oberseite und Flanken: Zeichnung (auf den Flanken nach außen auslaufend)
    if face in ("west", "east"):
        ry = (ly - oy) / h                                    # 0 oben .. 1 unten
        if ry > 0.75:
            return pick(ctx.pal["belly"], 0.4 + hash01(ctx.u, ctx.v) * 0.1)
        if _variant(ctx) == "spinne_dschungel" and 0.12 < (zr * 3.0) % 1.0 < 0.5 and zr > 0.2 and ry < 0.55:
            return pick(ctx.pal["mark"], 0.6)
        if _variant(ctx) == "hoehlenspinne" and 0.2 < ry < 0.45 and any(abs(zr - zc) < 0.05 for zc in (0.44, 0.68)):
            glow = pick(ctx.pal["mark"], 0.55)
            ctx.glow = glow
            return glow
        return col
    pat = _abdomen_pattern(ctx, xr, zr)
    if pat is None:
        return col
    key, level = pat
    if key == "glow":
        glow = pick(ctx.pal["mark"], level)
        ctx.glow = glow
        return glow
    return mix(pick(ctx.pal[key], level), col, 0.22)


def _spinneret(ctx: Ctx):
    return pick(ctx.pal["chitin"], 0.3 + hash01(ctx.u, ctx.v) * 0.1)


def _chelicera(ctx: Ctx):
    if ctx.face == "front" and ctx.t == 0:
        return pick(ctx.pal["fang"], 0.75)                                                  # Glanzkante
    return _hairy(ctx, 0.28, key="chitin") if ctx.face in ("west", "east") else pick(ctx.pal["fang"], 0.45 + hash01(ctx.u, ctx.v) * 0.1)


def _fang(ctx: Ctx):
    return pick(ctx.pal["fang"], 0.9 if ctx.face in ("front", "top") else 0.55)


def _palp(ctx: Ctx):
    return _hairy(ctx, 0.36, streak_axis=2)


def _leg_part(ctx: Ctx, part: str):
    """Beinglieder: behaart, mit Ringen und Gelenken je Art."""
    v = _variant(ctx)
    lx = ctx.local[0]
    ox = ctx.cube.origin[0]
    length = ctx.cube.size[0]
    right = ox < 0
    along = (ox + length - lx) / length if right else (lx - ox) / length     # 0 = körpernah, 1 = Spitze
    col = _hairy(ctx, 0.38 if part != "coxa" else 0.3, streak_axis=0)
    if part == "femur":
        if along > 0.85:
            if v == "spinne_dschungel":
                return pick(ctx.pal["band"], 0.7)                                            # orange Knie
            return pick(ctx.pal["band"], 0.45) if v in ("spinne_wald", "spinne_schnee") else col
        return col
    if part == "tibia":
        if along > 0.88:
            return pick(ctx.pal["fang"], 0.4 if ctx.face != "top" else 0.6)                  # Krallenglied
        if along < 0.1 and v == "spinne_dschungel":
            return pick(ctx.pal["band"], 0.6)
        if v in ("spinne_wald", "spinne_schnee") and int(along * 13) % 4 == 1:
            return pick(ctx.pal["band"], 0.55)                                               # Ringel
        if v == "spinne_wueste":
            return mix(col, pick(ctx.pal["hair"], 0.6), 0.35)                                # dichte helle Haare
        return col
    return col


def materials(variant: str) -> dict:
    return {
        "head": _head,
        "chelicera": _chelicera,
        "fang": _fang,
        "palp": _palp,
        "waist": _waist,
        "abdomen": _abdomen,
        "spinneret": _spinneret,
        "coxa": lambda ctx: _leg_part(ctx, "coxa"),
        "femur": lambda ctx: _leg_part(ctx, "femur"),
        "tibia": lambda ctx: _leg_part(ctx, "tibia"),
    }
