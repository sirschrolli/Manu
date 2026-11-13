"""
Schneeleopard der Frostgipfel (0.85): eine schlanke, tief gebaute Großkatze mit rauchgrauem, dichtem Fell, darauf
offene, dunkle Rosetten (am Kopf und an den Läufen kleine volle Tupfen), einem cremeweißen Bauch mit langer
Bauchfranse, großen, behaarten Pranken und einem Schwanz fast so lang wie der Leib – dick, buschig, quer gebändert,
mit dunkler Spitze. Die kleinen runden Ohren sind hinten schwarz mit hellem Fleck, die Augen blass graugrün.

Knochen (für SnowLeopardModel in Java): body (mit belly_fur, neck → head → jaw, ear_l, ear_r; tail1 → tail2 → tail3 →
tail4), die Läufe left_front_leg → left_front_shin → left_front_paw (ebenso rechts und hinten: *_hind_leg → *_hind_shin →
*_hind_paw). Varianten: snow_leopard (hellgrau, ein Hauch Beige) und snow_leopard_smoky (kühler,
dunkler Rauch).
"""
from __future__ import annotations

import math

from ..geometry import Model
from ..paint import Ctx, hash01, hexc, mix, noise3, pick, rel_y

_COMMON = {
    "belly": [hexc(c) for c in ("#a8a49c", "#c4c0b8", "#dad6ce", "#ebe8e1", "#f6f4ef", "#fdfcf9")],
    "nose": [hexc(c) for c in ("#2a2224", "#4a3a3c", "#6e5456", "#94706e", "#b8908c")],
    "eye": [hexc(c) for c in ("#1a221c", "#3c5240", "#6a8a6c", "#9ab89a", "#c8dcc4", "#e8f2e4")],
    "mouth": [hexc(c) for c in ("#1c1416", "#3a2628", "#5c3a3c", "#7e5052")],
    "claw": [hexc(c) for c in ("#2a2a2c", "#4e4c4a", "#7a7670", "#a8a49c")],
}

PALETTES = {
    "snow_leopard": {
        **_COMMON,
        "fur": [hexc(c) for c in ("#5c5852", "#77726a", "#928c83", "#ada69c", "#c7c0b5", "#ddd7cc", "#ece7de")],
        "spot": [hexc(c) for c in ("#1e1d21", "#29272c", "#343238", "#403d44", "#4d4a51", "#5c585f")],
    },
    "snow_leopard_smoky": {
        **_COMMON,
        "fur": [hexc(c) for c in ("#46474c", "#5d5e64", "#76777d", "#909197", "#aaabb0", "#c4c4c8", "#d8d8db")],
        "spot": [hexc(c) for c in ("#141418", "#1d1d22", "#27272d", "#323239", "#3e3e46", "#4b4b53")],
    },
}

class _Points:
    """Merkmalspunkte auf einem verwackelten Gitter in der Ebene einer Seite (Ringe bleiben so rund und gleich dick,
    wo eine Kugel um einen Raumpunkt nur Flecken zeichnen würde): der nächste Punkt (a, b, Zufallswert) und der Abstand."""

    def __init__(self, spacing: float, seed: int):
        self.spacing = spacing
        self.seed = seed
        self.cache: dict = {}

    def _point(self, ia: int, ib: int, plane: int):
        key = (ia, ib, plane)
        p = self.cache.get(key)
        if p is None:
            r = [hash01(ia, ib, plane, self.seed + k) for k in range(3)]
            p = ((ia + 0.15 + r[0] * 0.7) * self.spacing, (ib + 0.15 + r[1] * 0.7) * self.spacing, r[2])
            self.cache[key] = p
        return p

    def nearest(self, ctx: Ctx):
        a, b, plane = _coords(ctx)
        ca, cb = int(math.floor(a / self.spacing)), int(math.floor(b / self.spacing))
        best = None
        bd = 1e9
        for da in (-1, 0, 1):
            for db in (-1, 0, 1):
                p = self._point(ca + da, cb + db, plane)
                d = (p[0] - a) ** 2 + (p[1] - b) ** 2
                if d < bd:
                    bd, best = d, p
        return best, math.sqrt(bd), (a, b)


def _coords(ctx: Ctx):
    """Lage in der Ebene der Seite: Flanken (z, y), Rücken und Bauch (x, z), vorn und hinten (x, y)."""
    x, y, z = ctx.world
    if ctx.face in ("top", "bottom"):
        return x, z, 0
    if ctx.face in ("west", "east"):
        return z, y, 1
    return x, y, 2


_ROSETTE = _Points(3.5, 401)
_DOTS = _Points(1.5, 403)
_TAIL = _Points(3.6, 405)

_LIT = {"top": 0.1, "front": 0.03, "west": 0.0, "east": -0.03, "back": -0.05, "bottom": -0.16}


def build() -> Model:
    m = Model("snow_leopard", 128, 64)
    m.tex_res = 2
    m.world_faces = True
    # Leib: tiefe Brust, schlanke Lenden; darunter die lange Bauchfranse
    m.bone("body", pivot=(0, 13.0, 0))
    m.cube("body", (-3.4, -4.3, -9.6), (6.8, 7.9, 9.2), "fur")                 # Brust und Schultern
    m.cube("body", (-3.0, -3.7, -1.0), (6.0, 6.9, 9.6), "fur")                 # Lenden
    m.bone("belly_fur", "body", pivot=(0, 0, 0))
    m.cube("belly_fur", (-2.7, 2.4, -8.4), (5.4, 1.8, 15.6), "fringe")
    # Hals und Kopf: breiter Schädel, kurze Schnauze, kleine runde Ohren
    m.bone("neck", "body", pivot=(0, -1.6, -9.0), rotation=(-16, 0, 0))
    m.cube("neck", (-2.5, -2.9, -4.6), (5.0, 5.6, 5.2), "fur")
    m.bone("head", "neck", pivot=(0, -0.9, -4.2), rotation=(16, 0, 0))
    m.cube("head", (-3.2, -3.3, -5.6), (6.4, 5.6, 6.0), "skull")
    m.cube("head", (-2.0, -0.8, -7.9), (4.0, 2.9, 2.5), "muzzle")
    m.cube("head", (-0.75, -1.0, -8.05), (1.5, 0.8, 0.3), "nose")
    m.cube("head", (0.95, -2.45, -5.7), (1.5, 1.0, 0.15), "eye")
    m.cube("head", (-2.45, -2.45, -5.7), (1.5, 1.0, 0.15), "eye")
    m.cube("head", (-3.75, -1.4, -4.5), (0.7, 3.6, 3.6), "ruff")               # Backenbart
    m.cube("head", (3.05, -1.4, -4.5), (0.7, 3.6, 3.6), "ruff")
    m.bone("jaw", "head", pivot=(0, 2.0, -5.1))
    m.cube("jaw", (-1.6, 0, -2.6), (3.2, 1.0, 2.8), "jaw")
    m.bone("ear_l", "head", pivot=(2.2, -3.2, -1.9), rotation=(0, 0, 16))
    m.cube("ear_l", (-0.9, -1.7, -0.5), (1.9, 1.8, 1.0), "ear")
    m.bone("ear_r", "head", pivot=(-2.2, -3.2, -1.9), rotation=(0, 0, -16))
    m.cube("ear_r", (-1.0, -1.7, -0.5), (1.9, 1.8, 1.0), "ear")
    # der lange Schwanz: erst hängend, dann in einem Bogen nach oben; die Spitze buschig und dunkel
    m.bone("tail1", "body", pivot=(0, -2.5, 8.2), rotation=(-42, 0, 0))
    m.cube("tail1", (-1.35, -1.35, -0.4), (2.7, 2.7, 7.0), "tail")
    m.bone("tail2", "tail1", pivot=(0, 0, 6.4), rotation=(4, 0, 0))
    m.cube("tail2", (-1.3, -1.3, -0.4), (2.6, 2.6, 6.8), "tail")
    m.bone("tail3", "tail2", pivot=(0, 0, 6.2), rotation=(14, 0, 0))
    m.cube("tail3", (-1.32, -1.32, -0.4), (2.64, 2.64, 6.4), "tail")
    m.bone("tail4", "tail3", pivot=(0, 0, 5.9), rotation=(36, 0, 0))
    m.cube("tail4", (-1.5, -1.5, -0.4), (3.0, 3.0, 5.8), "tail")
    # Läufe in zwei Gliedern, große behaarte Pranken
    for side, sx in (("left", 1), ("right", -1)):
        m.bone(f"{side}_front_leg", pivot=(2.3 * sx, 12.4, -6.6))
        m.cube(f"{side}_front_leg", (-1.7, -1.2, -1.9), (3.4, 7.5, 3.8), "leg_upper")
        m.bone(f"{side}_front_shin", f"{side}_front_leg", pivot=(0, 6.1, -0.2))
        m.cube(f"{side}_front_shin", (-1.4, 0, -1.4), (2.8, 4.7, 2.8), "leg")
        m.bone(f"{side}_front_paw", f"{side}_front_shin", pivot=(0, 4.4, 0))
        m.cube(f"{side}_front_paw", (-1.8, 0, -2.6), (3.6, 1.1, 3.8), "paw")
        m.bone(f"{side}_hind_leg", pivot=(2.4 * sx, 12.0, 6.0))
        m.cube(f"{side}_hind_leg", (-1.9, -1.4, -2.5), (3.8, 7.4, 4.8), "thigh")
        m.bone(f"{side}_hind_shin", f"{side}_hind_leg", pivot=(0, 5.6, 1.0))
        m.cube(f"{side}_hind_shin", (-1.3, 0, -1.35), (2.6, 5.5, 2.7), "leg")
        m.bone(f"{side}_hind_paw", f"{side}_hind_shin", pivot=(0, 5.3, 0))
        m.cube(f"{side}_hind_paw", (-1.75, 0, -2.6), (3.5, 1.1, 3.7), "paw")
    return m


# =============================================================================================
# Bemalung
# =============================================================================================

def _hair(ctx: Ctx, scale: float = 1.0) -> float:
    """Feine Haarstriche: senkrechte Strähnen mit leichtem Helligkeitswechsel."""
    x, y, z = ctx.world
    across = z if ctx.face in ("west", "east") else x
    lane = math.floor(across * 2.0 + 0.001)
    return (hash01(lane, math.floor(y * 1.2 + hash01(lane, 409) * 3), 411) - 0.5) * 0.09 * scale


def _base(ctx: Ctx, v: float) -> tuple:
    """Rauchgraues Fell, oben etwas dunkler, an den Flanken heller; zum Bauch hin ins Cremeweiß."""
    x, y, z = ctx.world
    n = noise3(x * 0.22, y * 0.22, z * 0.22, 413)
    v = v + _LIT[ctx.face] + (n - 0.5) * 0.18 + _hair(ctx)
    col = pick(ctx.pal["fur"], v)
    ry = rel_y(ctx)
    if ctx.face == "bottom" or (ctx.face in ("west", "east", "front", "back") and ry > 0.68):
        t = 1.0 if ctx.face == "bottom" else min(1.0, (ry - 0.68) / 0.3 * 1.3)
        col = mix(col, pick(ctx.pal["belly"], 0.6 + (n - 0.5) * 0.2 + _hair(ctx)), t)
    return col


def _angle(p: tuple, ab: tuple) -> float:
    return math.atan2(ab[1] - p[1], ab[0] - p[0])


def _rosettes(ctx: Ctx, col: tuple, points: _Points, size: float = 1.0, centre: float = 0.3) -> tuple:
    """Offene Rosetten: ein dunkelgrauer Ring um jeden Punkt, zwei-, dreimal unterbrochen; die Mitte etwas dunkler.
    Ein Teil der Punkte sind kleine volle Flecken."""
    p, d, ab = points.nearest(ctx)
    r = 1.55 * size * (0.85 + p[2] * 0.35)
    width = 0.3
    if p[2] < 0.2:                                                       # ein voller Fleck
        if d < r * 0.42:
            return pick(ctx.pal["spot"], 0.35 + hash01(ctx.u, ctx.v, 417) * 0.15)
        return col
    if abs(d - r) < width:
        gaps = 2 + int(p[2] * 2.0)
        if math.sin(_angle(p, ab) * gaps + p[2] * 40.0) < 0.55:
            return pick(ctx.pal["spot"], 0.3 + (abs(d - r) / width) * 0.25 + hash01(ctx.u, ctx.v, 419) * 0.1)
    if d < r - width:
        return mix(col, pick(ctx.pal["fur"], 0.26), centre)
    return col


def _dots(ctx: Ctx, col: tuple, size: float = 1.0) -> tuple:
    """Kleine volle Tupfen (Kopf, Läufe)."""
    p, d, _ = _DOTS.nearest(ctx)
    if p[2] > 0.3 and d < (0.28 + p[2] * 0.2) * size:
        return pick(ctx.pal["spot"], 0.35 + hash01(ctx.u, ctx.v, 421) * 0.15)
    return col


def _fur(ctx: Ctx):
    col = _base(ctx, 0.56)
    if ctx.face == "bottom" or rel_y(ctx) > 0.8:
        return col
    return _rosettes(ctx, col, _ROSETTE)


def _fringe(ctx: Ctx):
    """Bauchfranse: cremeweiß, unten in langen Strähnen ausgefranst (Lücken durchsichtig)."""
    if ctx.face == "top":
        return None
    if ctx.face == "bottom":
        return pick(ctx.pal["belly"], 0.45)
    ry = rel_y(ctx)
    strand = hash01(ctx.u // 2, 423)
    if ry > 0.45 + strand * 0.5:
        return None
    return pick(ctx.pal["belly"], 0.62 - ry * 0.25 + _hair(ctx, 1.5))


def _skull(ctx: Ctx):
    """Stirn und Wangen rauchgrau mit feinen Tupfen; um die Augen etwas heller, unter den Augen ein feiner dunkler Strich,
    die Wangen zum Kinn hin hell."""
    rs = (ctx.s + 0.5) / max(ctx.w, 1)
    ry = rel_y(ctx)
    col = _base(ctx, 0.6)
    if ctx.face == "front":
        if ry > 0.62 and 0.17 < abs(rs - 0.5) < 0.22:
            return pick(ctx.pal["spot"], 0.45)                               # der Tränenstrich
        if 0.3 < ry < 0.6:
            col = mix(col, pick(ctx.pal["belly"], 0.65), 0.28)
        elif ry >= 0.6:
            col = mix(col, pick(ctx.pal["belly"], 0.7), 0.5)
        return _dots(ctx, col, 0.85) if ry < 0.42 or abs(rs - 0.5) > 0.3 else col
    if ctx.face == "bottom":
        return pick(ctx.pal["belly"], 0.55)
    if ctx.face in ("west", "east") and ry > 0.55:
        col = mix(col, pick(ctx.pal["belly"], 0.66), 0.4)
    return _dots(ctx, col)


def _muzzle(ctx: Ctx):
    """Helle Schnauze: unter der Nase die dunkle Furche, die in einen kleinen Mund mündet; Schnurrhaarpolster mit
    feinen Punktreihen."""
    ry = rel_y(ctx)
    rs = (ctx.s + 0.5) / max(ctx.w, 1)
    if ctx.face == "front":
        if abs(rs - 0.5) < 0.07 and 0.35 < ry < 0.7:
            return mix(pick(ctx.pal["belly"], 0.6), pick(ctx.pal["nose"], 0.9), 0.3)   # die Furche unter der Nase, zart
        if ry > 0.84 and 0.1 < abs(rs - 0.5) < 0.25:
            return pick(ctx.pal["nose"], 0.3)                               # der kleine Mund: zwei Bögen
        col = pick(ctx.pal["belly"], 0.68 + _hair(ctx))
        if 0.35 < ry < 0.8 and abs(rs - 0.5) > 0.16 and (ctx.s + ctx.t) % 3 == 0:
            return pick(ctx.pal["fur"], 0.4)                                # Schnurrhaarpunkte
        return col
    if ctx.face in ("west", "east"):
        return mix(_base(ctx, 0.6), pick(ctx.pal["belly"], 0.62), 0.6)
    if ctx.face == "top":
        return mix(_base(ctx, 0.6), pick(ctx.pal["belly"], 0.6), 0.25)
    return pick(ctx.pal["belly"], 0.45)


def _nose(ctx: Ctx):
    """Kleine rosagraue Nase, unten zwei dunkle Nasenlöcher an den Ecken."""
    if ctx.face == "front":
        rs = (ctx.s + 0.5) / max(ctx.w, 1)
        rt = (ctx.t + 0.5) / max(ctx.h, 1)
        if rt > 0.5 and abs(rs - 0.5) > 0.3:
            return pick(ctx.pal["nose"], 0.15)
        return pick(ctx.pal["nose"], 0.85 - rt * 0.2)
    return pick(ctx.pal["nose"], 0.6 + _LIT[ctx.face])


def _eye(ctx: Ctx):
    rs = (ctx.s + 0.5) / max(ctx.w, 1)
    rt = (ctx.t + 0.5) / max(ctx.h, 1)
    if ctx.face != "front":
        return pick(ctx.pal["spot"], 0.2)
    if abs(rs - 0.5) < 0.09:
        return pick(ctx.pal["spot"], 0.0)                                    # schmale Pupille
    if rt < 0.3:
        return pick(ctx.pal["spot"], 0.15)                                   # dunkler Lidrand oben
    return pick(ctx.pal["eye"], 0.92 - abs(rs - 0.5) * 0.7)


def _ruff(ctx: Ctx):
    """Backenbart: hell, in Strähnen nach hinten; einzelne Strähnen durchsichtig am Rand."""
    if ctx.face in ("top", "bottom"):
        return pick(ctx.pal["belly"], 0.55)
    ry = rel_y(ctx)
    strand = hash01(ctx.v // 2, 425)
    if ctx.face == "back" or (ctx.face in ("west", "east") and (ctx.s + 0.5) / max(ctx.w, 1) > 0.78 + strand * 0.2):
        return None if strand > 0.6 else pick(ctx.pal["belly"], 0.5)
    col = mix(_base(ctx, 0.62), pick(ctx.pal["belly"], 0.66), 0.35 + ry * 0.5)
    return _dots(ctx, col, 0.8)


def _jaw(ctx: Ctx):
    if ctx.face == "top":
        return pick(ctx.pal["mouth"], 0.55 + (hash01(ctx.u, ctx.v, 427) - 0.5) * 0.2)
    return pick(ctx.pal["belly"], 0.62 + _LIT[ctx.face] * 0.5)


def _ear(ctx: Ctx):
    """Ohren: vorn hell behaart mit rosa Innenseite, hinten schwarz mit einem hellen Fleck."""
    rs = (ctx.s + 0.5) / max(ctx.w, 1)
    ry = rel_y(ctx)
    if ctx.face == "front":
        if 0.25 < rs < 0.75 and ry > 0.35:
            return pick(ctx.pal["nose"], 0.72)
        return pick(ctx.pal["belly"], 0.6)
    if ctx.face == "back":
        if 0.3 < rs < 0.7 and 0.35 < ry < 0.75:
            return pick(ctx.pal["belly"], 0.7)
        return pick(ctx.pal["spot"], 0.2)
    if ctx.face == "top":
        return pick(ctx.pal["spot"], 0.3)
    return pick(ctx.pal["spot"], 0.3 + _LIT[ctx.face])


def _tail(ctx: Ctx):
    """Dick und buschig, mit großen, fast geschlossenen Ringen; die Spitze dunkel, die Unterseite hell."""
    bone = ctx.cube.bone.name
    if bone == "tail4" and ctx.local[2] > 2.6:
        return pick(ctx.pal["spot"], 0.3 + _hair(ctx, 1.5) + _LIT[ctx.face])
    if ctx.face == "bottom":
        return pick(ctx.pal["belly"], 0.55 + _hair(ctx))
    col = _base(ctx, 0.58)
    p, d, ab = _TAIL.nearest(ctx)
    r = 1.35 * (0.85 + p[2] * 0.3)
    if abs(d - r) < 0.34 and math.sin(_angle(p, ab) * 2 + p[2] * 30.0) < 0.75:
        return pick(ctx.pal["spot"], 0.3 + hash01(ctx.u, ctx.v, 431) * 0.12)
    if d < r - 0.34:
        return mix(col, pick(ctx.pal["fur"], 0.26), 0.3)
    return col


def _leg_upper(ctx: Ctx):
    col = _base(ctx, 0.58)
    if ctx.face == "bottom":
        return col
    return _rosettes(ctx, col, _ROSETTE, size=0.85)


def _thigh(ctx: Ctx):
    return _leg_upper(ctx)


def _leg(ctx: Ctx):
    col = _base(ctx, 0.62)
    if ctx.face in ("back", "west", "east") and rel_y(ctx) > 0.5:
        col = mix(col, pick(ctx.pal["belly"], 0.6), 0.4)
    return _dots(ctx, col, 0.9)


def _paw(ctx: Ctx):
    """Große, behaarte Pranke: hell, vorn drei dunkle Zehenfugen und kurze Krallenspitzen; unten dunkle Ballen."""
    rs = (ctx.s + 0.5) / max(ctx.w, 1)
    if ctx.face == "bottom":
        return pick(ctx.pal["nose"], 0.2 if hash01(ctx.u, ctx.v, 433) > 0.4 else 0.35)
    if ctx.face == "front":
        if any(abs(rs - f) < 0.05 for f in (0.3, 0.5, 0.7)):
            return pick(ctx.pal["fur"], 0.25)
        if rel_y(ctx) > 0.7 and any(abs(rs - f) < 0.06 for f in (0.2, 0.4, 0.6, 0.8)):
            return pick(ctx.pal["claw"], 0.7)
    return pick(ctx.pal["belly"], 0.55 + _LIT[ctx.face] + _hair(ctx))


def materials(variant: str) -> dict:
    return {"fur": _fur, "fringe": _fringe, "skull": _skull, "muzzle": _muzzle, "nose": _nose, "eye": _eye, "ruff": _ruff,
            "jaw": _jaw, "ear": _ear, "tail": _tail, "leg_upper": _leg_upper, "thigh": _thigh, "leg": _leg, "paw": _paw}
