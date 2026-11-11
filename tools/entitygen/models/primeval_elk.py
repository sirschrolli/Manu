"""
Urelch des Urhains (0.85): ein riesiger Elch – hohe, buckelige Schultern mit fast schwarzem Zottelfell, ein dunkelbrauner
Leib auf langen, hellen Läufen, der lange Kopf mit der überhängenden Ramsnase und dem Kinnbart ("Glocke"), große,
seitlich abstehende Ohren. Die Bullen tragen gewaltige Schaufeln, bemoost wie die alten Stämme des Urhains; von ihren
Rändern hängen Ranken mit glühenden Leuchtbeeren. Auf Rücken und Buckel wächst Moos.

Knochen (für PrimevalElkModel in Java): body (mit hump, neck → head → jaw, bell, ear_l, ear_r, antler_l → antler_l_palm,
antler_r → antler_r_palm, bridle; tail, saddle, chest_l, chest_r), die Läufe left_front_leg → left_front_shin,
right_front_leg → right_front_shin, left_hind_leg → left_hind_shin, right_hind_leg → right_hind_shin.
Sattel, Zaumzeug und Truhen sind eigene Knochen, die das Modell nur zeigt, wenn der Elch sie trägt.
"""
from __future__ import annotations

import math

from ..geometry import Model
from ..paint import Ctx, hash01, hexc, mix, noise3, pick, rel_y

PALETTES = {
    "primeval_elk": {
        "fur": [hexc(c) for c in ("#1c130d", "#2a1d14", "#38281c", "#473325", "#583f2e", "#6b4e39", "#7f5f46")],
        "hump": [hexc(c) for c in ("#120c08", "#1c140e", "#271c14", "#33261b", "#413124", "#52402f")],
        "leg": [hexc(c) for c in ("#3e352d", "#524840", "#685d53", "#7e7367", "#958a7d", "#aca193", "#c2b8aa")],
        "muzzle": [hexc(c) for c in ("#2e2219", "#3e3024", "#503f30", "#64513f", "#78644f", "#8c7760")],
        "nose": [hexc(c) for c in ("#100b09", "#1c1411", "#2a1f1a", "#3a2c25", "#4c3b33")],
        "antler": [hexc(c) for c in ("#4e4030", "#6a5841", "#887355", "#a48e6c", "#bea886", "#d6c4a2", "#eadcbe")],
        "moss": [hexc(c) for c in ("#1a2e10", "#263f16", "#33531d", "#426926", "#548031", "#69983d", "#82b04c")],
        "vine": [hexc(c) for c in ("#16260d", "#213812", "#2d4a18", "#3a5e1f")],
        "berry": [hexc(c) for c in ("#9a4414", "#c8681e", "#ee9430", "#ffbc52", "#ffdc8e", "#fff2cc")],
        "eye": [hexc(c) for c in ("#0e0908", "#21150f", "#3a2618", "#5a3c22")],
        "leather": [hexc(c) for c in ("#2e1c10", "#422816", "#58361e", "#6e4628", "#865834", "#9c6a40")],
        "metal": [hexc(c) for c in ("#3c3c40", "#5a5a60", "#7c7c84", "#a2a2aa", "#c6c6ce")],
        "wood": [hexc(c) for c in ("#3e2814", "#52361c", "#684624", "#7e582e", "#946a3a", "#aa7e48")],
    },
}

_LIT = {"top": 0.1, "front": 0.03, "west": 0.0, "east": -0.03, "back": -0.05, "bottom": -0.16}


def build() -> Model:
    m = Model("primeval_elk", 192, 128)
    m.tex_res = 2
    m.world_faces = True
    # Leib mit hohem Buckel über den Schultern, die Kruppe tiefer
    m.bone("body", pivot=(0, 2.0, 1.0), rotation=(-3, 0, 0))
    m.cube("body", (-7.0, -5.0, -13.0), (14.0, 13.0, 26.0), "fur")
    m.bone("hump", "body", pivot=(0, 0, 0))
    m.cube("hump", (-6.2, -12.5, -13.8), (12.4, 8.5, 12.0), "hump")
    m.cube("hump", (-5.4, -8.8, -2.0), (10.8, 4.0, 5.0), "hump")                 # der Buckel läuft nach hinten aus
    m.cube("hump", (-6.6, -6.0, -14.4), (13.2, 10.0, 4.0), "hump")               # die Brust: zottig, tief
    # Hals: kurz und dick, aus der Mitte des Buckels nach vorn; der Kopf hängt tiefer
    m.bone("neck", "body", pivot=(0, -5.5, -13.0), rotation=(8, 0, 0))
    m.cube("neck", (-4.0, -5.0, -10.0), (8.0, 10.0, 11.0), "neck")
    # Kopf: Schädel, lange Ramsnase mit überhängender Oberlippe, Unterkiefer, die "Glocke"
    m.bone("head", "neck", pivot=(0, -0.5, -9.5), rotation=(40, 0, 0))
    m.cube("head", (-4.0, -4.6, -6.5), (8.0, 8.2, 7.5), "skull")
    m.cube("head", (-3.3, -3.8, -15.0), (6.6, 7.4, 9.0), "muzzle")
    m.cube("head", (-3.5, -4.0, -16.6), (7.0, 4.8, 2.2), "nose")                  # Oberlippe und Nüstern
    m.cube("head", (3.95, -2.9, -5.6), (0.25, 1.3, 1.8), "eye")
    m.cube("head", (-4.2, -2.9, -5.6), (0.25, 1.3, 1.8), "eye")
    m.bone("jaw", "head", pivot=(0, 3.4, -6.0))
    m.cube("jaw", (-2.7, 0, -9.6), (5.4, 2.0, 10.0), "jaw")
    m.bone("bell", "head", pivot=(0, 3.6, -3.8), rotation=(-40, 0, 0))
    m.cube("bell", (-1.0, 0, -1.4), (2.0, 7.5, 2.6), "bell")
    m.bone("ear_l", "head", pivot=(3.8, -4.2, -1.6), rotation=(-40, 0, -22))
    m.cube("ear_l", (0, -1.2, -0.7), (5.2, 2.6, 1.4), "ear")
    m.bone("ear_r", "head", pivot=(-3.8, -4.2, -1.6), rotation=(-40, 0, 22))
    m.cube("ear_r", (-5.2, -1.2, -0.7), (5.2, 2.6, 1.4), "ear")
    # Schaufeln (nur Bullen): der Stamm hebt die Neigung des Kopfs auf; dann seitlich ein kurzer Stamm und die breite,
    # leicht nach hinten geschwungene Schaufel mit Sprossen am Rand; von der Schaufel hängen Ranken mit Leuchtbeeren
    for side, sx in (("l", 1), ("r", -1)):
        m.bone(f"antler_{side}", "head", pivot=(3.2 * sx, -4.4, -2.4), rotation=(-48, 0, -10 * sx))
        bx = 0.0 if sx > 0 else -5.0
        m.cube(f"antler_{side}", (bx, -1.3, -1.3), (5.0, 2.6, 2.6), "beam")
        m.bone(f"antler_{side}_palm", f"antler_{side}", pivot=(4.6 * sx, -0.4, 0), rotation=(0, -16 * sx, -20 * sx))
        px = 0.0 if sx > 0 else -11.0
        m.cube(f"antler_{side}_palm", (px, -0.7, -5.4), (11.0, 1.4, 10.0), "palm")
        # Sprossen am Außenrand (nach oben) und vorn
        for i, (tx, tz, th) in enumerate(((9.8, -5.4, 3.6), (9.8, -2.6, 4.4), (9.8, 0.2, 4.0), (9.8, 3.0, 3.4), (5.6, -5.8, 2.8),
                                          (2.6, -5.8, 2.2))):
            x0 = tx if sx > 0 else -tx - 1.2
            m.cube(f"antler_{side}_palm", (x0, -0.7 - th, tz), (1.2, th, 1.2), "tine")
        # Ranken mit Leuchtbeeren, die vom Schaufelrand hängen
        for i, (vx, vz, vl) in enumerate(((3.6, 3.6, 5.5), (7.0, 3.8, 7.0), (8.6, -4.6, 4.0))):
            x0 = vx if sx > 0 else -vx - 0.6
            m.cube(f"antler_{side}_palm", (x0, 0.6, vz), (0.6, vl, 0.6), "vine")
    # Zaumzeug (sichtbar mit Sattel): Nasenriemen und Genickstück
    m.bone("bridle", "head", pivot=(0, 0, 0))
    m.cube("bridle", (-3.45, -3.95, -12.2), (6.9, 7.7, 1.0), "strap", inflate=0.06)
    m.cube("bridle", (-4.15, -4.75, -4.2), (8.3, 8.5, 1.0), "strap", inflate=0.06)
    m.cube("bridle", (-3.9, -1.0, -12.0), (0.6, 0.6, 7.6), "strap")
    m.cube("bridle", (3.3, -1.0, -12.0), (0.6, 0.6, 7.6), "strap")
    # kurzer Schwanz
    m.bone("tail", "body", pivot=(0, -3.8, 12.6), rotation=(10, 0, 0))
    m.cube("tail", (-1.4, 0, -0.6), (2.8, 4.0, 2.0), "tail")
    # Sattel mit Steigbügeln (nur gesattelt) und zwei Truhen (nur mit Truhe)
    m.bone("saddle", "body", pivot=(0, 0, 0))
    m.cube("saddle", (-5.2, -6.6, -3.2), (10.4, 1.8, 11.4), "saddle")
    m.cube("saddle", (-4.0, -7.6, -3.4), (8.0, 1.2, 2.2), "saddle")                # Sattelknauf vorn
    m.cube("saddle", (-4.4, -7.4, 6.4), (8.8, 1.0, 1.6), "saddle")                 # Hinterzwiesel
    m.cube("saddle", (-7.2, -5.2, -1.0), (0.4, 6.0, 7.0), "flap")
    m.cube("saddle", (6.8, -5.2, -1.0), (0.4, 6.0, 7.0), "flap")
    m.cube("saddle", (-7.6, 0.6, 2.0), (0.4, 6.4, 0.6), "strap")
    m.cube("saddle", (7.2, 0.6, 2.0), (0.4, 6.4, 0.6), "strap")
    m.cube("saddle", (-8.2, 7.0, 1.4), (1.6, 0.7, 1.8), "metal")
    m.cube("saddle", (6.6, 7.0, 1.4), (1.6, 0.7, 1.8), "metal")
    m.bone("chest_l", "body", pivot=(0, 0, 0))
    m.cube("chest_l", (7.0, -3.6, 4.4), (3.2, 7.0, 7.6), "chest")
    m.bone("chest_r", "body", pivot=(0, 0, 0))
    m.cube("chest_r", (-10.2, -3.6, 4.4), (3.2, 7.0, 7.6), "chest")
    # lange Läufe in zwei Gliedern, Hufe
    for side, sx in (("left", 1), ("right", -1)):
        m.bone(f"{side}_front_leg", pivot=(4.2 * sx, 8.0, -8.6))
        m.cube(f"{side}_front_leg", (-2.2, -2.0, -2.6), (4.4, 10.5, 5.2), "leg_upper")
        m.bone(f"{side}_front_shin", f"{side}_front_leg", pivot=(0, 8.5, -0.2))
        m.cube(f"{side}_front_shin", (-1.5, 0, -1.6), (3.0, 7.2, 3.2), "leg")
        m.cube(f"{side}_front_shin", (-1.8, 6.0, -2.2), (3.6, 1.5, 4.0), "hoof")
        m.bone(f"{side}_hind_leg", pivot=(4.4 * sx, 7.4, 10.0))
        m.cube(f"{side}_hind_leg", (-2.5, -2.0, -3.2), (5.0, 11.0, 6.4), "thigh")
        m.bone(f"{side}_hind_shin", f"{side}_hind_leg", pivot=(0, 9.0, 1.4))
        m.cube(f"{side}_hind_shin", (-1.5, 0, -1.5), (3.0, 7.2, 3.0), "leg")
        m.cube(f"{side}_hind_shin", (-1.8, 6.5, -2.2), (3.6, 1.5, 4.0), "hoof")
    return m


# =============================================================================================
# Bemalung
# =============================================================================================

def _strands(ctx: Ctx, scale: float = 1.0) -> float:
    """Lange, senkrechte Haarsträhnen."""
    x, y, z = ctx.world
    across = z if ctx.face in ("west", "east") else x
    lane = math.floor(across * 2.0 + 0.001)
    return (hash01(lane, math.floor(y * 0.6 + hash01(lane, 501) * 4), 503) - 0.5) * 0.12 * scale


def _moss(ctx: Ctx, col: tuple, amount: float, seed: int = 505) -> tuple:
    """Moospolster: weiche Flecken, mit hellen Spitzen und ein paar dunklen Tupfen."""
    x, y, z = ctx.world
    n = noise3(x * 0.32, y * 0.32, z * 0.32, seed)
    if n < 1.0 - amount:
        return col
    v = 0.45 + (n - (1.0 - amount)) * 1.2 + (hash01(ctx.u, ctx.v, seed + 1) - 0.5) * 0.24
    return pick(ctx.pal["moss"], v)


def _fur(ctx: Ctx):
    x, y, z = ctx.world
    v = 0.52 + _LIT[ctx.face] + (noise3(x * 0.2, y * 0.2, z * 0.2, 507) - 0.5) * 0.16 + _strands(ctx)
    ry = rel_y(ctx)
    if ctx.face in ("west", "east", "front", "back"):
        v -= 0.08 * max(0.0, ry - 0.6) / 0.4                                     # Bauch dunkler
    if ctx.face == "bottom":
        v -= 0.1
    col = pick(ctx.pal["fur"], v)
    if ctx.face == "top":
        col = _moss(ctx, col, 0.32)
    return col


def _hump(ctx: Ctx):
    """Buckel und Brust: fast schwarzes, zottiges Fell, oben bemoost; an der Brust lange Fransen."""
    x, y, z = ctx.world
    v = 0.48 + _LIT[ctx.face] + _strands(ctx, 1.6) + (noise3(x * 0.25, y * 0.25, z * 0.25, 509) - 0.5) * 0.2
    col = pick(ctx.pal["hump"], v)
    if ctx.face == "top" or (ctx.face != "bottom" and rel_y(ctx) < 0.12):
        col = _moss(ctx, col, 0.45, 511)
    if ctx.face in ("front", "west", "east") and ctx.t >= ctx.h - 2 and hash01(ctx.u, 0, 513) > 0.5:
        return None
    return col


def _neck(ctx: Ctx):
    """Hals: dunkelbraun, die Unterseite mit langer, fast schwarzer Mähne."""
    x, y, z = ctx.world
    v = 0.46 + _LIT[ctx.face] + _strands(ctx, 1.3) + (noise3(x * 0.2, y * 0.2, z * 0.2, 515) - 0.5) * 0.14
    if ctx.face == "bottom":
        return pick(ctx.pal["hump"], 0.4 + _strands(ctx, 1.5))
    col = pick(ctx.pal["fur"], v)
    if ctx.face == "top":
        col = _moss(ctx, pick(ctx.pal["hump"], v), 0.3, 517)
    return col


def _skull(ctx: Ctx):
    x, y, z = ctx.world
    v = 0.5 + _LIT[ctx.face] + (noise3(x * 0.3, y * 0.3, z * 0.3, 519) - 0.5) * 0.12 + _strands(ctx, 0.6)
    return pick(ctx.pal["fur"], v)


def _muzzle(ctx: Ctx):
    """Die lange Ramsnase: oben heller graubraun, an den Seiten zur Lippe hin dunkler; unten die Maulspalte."""
    x, y, z = ctx.world
    ry = rel_y(ctx)
    v = 0.52 + _LIT[ctx.face] + (noise3(x * 0.35, y * 0.35, z * 0.35, 521) - 0.5) * 0.12
    if ctx.face in ("west", "east"):
        v -= ry * 0.18
        if ry > 0.86:
            return pick(ctx.pal["nose"], 0.3)
    if ctx.face == "bottom":
        return pick(ctx.pal["nose"], 0.45)
    return pick(ctx.pal["muzzle"], v)


def _nose(ctx: Ctx):
    """Die überhängende Oberlippe mit den großen, schräg stehenden Nüstern."""
    rs = (ctx.s + 0.5) / max(ctx.w, 1)
    rt = (ctx.t + 0.5) / max(ctx.h, 1)
    if ctx.face == "front":
        if 0.15 < abs(rs - 0.5) < 0.33 and 0.25 < rt < 0.62:
            return pick(ctx.pal["nose"], 0.0)
        return pick(ctx.pal["muzzle"], 0.42 - rt * 0.12 + (hash01(ctx.u, ctx.v, 523) - 0.5) * 0.08)
    if ctx.face == "top":
        return pick(ctx.pal["muzzle"], 0.62)
    return pick(ctx.pal["muzzle"], 0.38 + _LIT[ctx.face])


def _eye(ctx: Ctx):
    rt = (ctx.t + 0.5) / max(ctx.h, 1)
    rs = (ctx.s + 0.5) / max(ctx.w, 1)
    if ctx.face in ("west", "east"):
        if rt < 0.3:
            return pick(ctx.pal["hump"], 0.2)
        return pick(ctx.pal["eye"], 0.85 if (0.3 < rs < 0.55 and rt < 0.6) else 0.25)
    return pick(ctx.pal["eye"], 0.1)


def _jaw(ctx: Ctx):
    if ctx.face == "top":
        return pick(ctx.pal["nose"], 0.6)
    return pick(ctx.pal["muzzle"], 0.38 + _LIT[ctx.face] + _strands(ctx, 0.5))


def _bell(ctx: Ctx):
    """Die "Glocke": ein behaarter Hautlappen unter der Kehle, unten in Fransen."""
    ry = rel_y(ctx)
    if ry > 0.82 and hash01(ctx.u, 525) > 0.5:
        return None
    return pick(ctx.pal["hump"], 0.38 + (1 - ry) * 0.15 + _strands(ctx, 1.4))


def _ear(ctx: Ctx):
    rs = (ctx.s + 0.5) / max(ctx.w, 1)
    if ctx.face == "front" and 0.2 < (ctx.t + 0.5) / max(ctx.h, 1) < 0.8 and rs > 0.18:
        return pick(ctx.pal["leg"], 0.45)                                      # die hellere Innenseite
    return pick(ctx.pal["fur"], 0.45 + _LIT[ctx.face] + _strands(ctx, 0.5))


def _beam(ctx: Ctx):
    """Der kurze Stamm der Schaufel: rau, ganz bemoost."""
    col = pick(ctx.pal["antler"], 0.4 + _LIT[ctx.face] + (hash01(ctx.u, ctx.v, 527) - 0.5) * 0.15)
    return _moss(ctx, col, 0.75, 529)


def _palm(ctx: Ctx):
    """Die Schaufel: knochenhell, oben mit Rillen zum Rand hin; zur Mitte hin Moospolster; an der Kante dunkler."""
    x, y, z = ctx.world
    lx = abs(ctx.local[0])
    v = 0.55 + _LIT[ctx.face] + (noise3(x * 0.6, y * 0.6, z * 0.6, 531) - 0.5) * 0.18
    if ctx.face in ("top", "bottom"):
        v += 0.06 * math.sin(lx * 2.1)                                         # Rillen
    if ctx.face not in ("top", "bottom"):
        v -= 0.12
    col = pick(ctx.pal["antler"], v)
    amount = max(0.0, 0.55 - lx * 0.055)
    if ctx.face == "bottom":
        amount *= 0.5
    return _moss(ctx, col, amount, 533)


def _tine(ctx: Ctx):
    """Die Sprossen: zur Spitze hin heller, glatt."""
    ry = rel_y(ctx)
    return pick(ctx.pal["antler"], 0.78 - ry * 0.3 + _LIT[ctx.face])


def _vine(ctx: Ctx):
    """Ranke mit Leuchtbeeren: dunkles Grün, unten eine glühende Beere, dazwischen einzelne kleinere Beeren."""
    ry = rel_y(ctx)
    if ry > 0.82:
        col = pick(ctx.pal["berry"], 0.95 - (ry - 0.82) * 1.5)
        ctx.glow = col
        return col
    if hash01(ctx.u // 2, ctx.v // 3, 535) > 0.86 and ry > 0.3:
        col = pick(ctx.pal["berry"], 0.6)
        ctx.glow = col
        return col
    return pick(ctx.pal["vine"], 0.4 + (hash01(ctx.u, ctx.v, 537) - 0.5) * 0.4)


def _tail(ctx: Ctx):
    return pick(ctx.pal["hump"], 0.42 + _LIT[ctx.face] + _strands(ctx, 1.2))


def _leg_upper(ctx: Ctx):
    """Oben noch braun wie der Leib, nach unten ins helle Graubraun der Läufe."""
    ry = rel_y(ctx)
    base = _fur(ctx) if ctx.face != "top" else pick(ctx.pal["fur"], 0.5)
    if ry > 0.72:
        # kurz über dem Knie wird das Fell hell (in zwei Stufen, nicht verwaschen)
        light = pick(ctx.pal["leg"], 0.42 + _LIT[ctx.face] + _strands(ctx, 0.6))
        return light if ry > 0.84 or hash01(ctx.u, 545) > 0.5 else mix(base, light, 0.5)
    return base


def _thigh(ctx: Ctx):
    return _leg_upper(ctx)


def _leg(ctx: Ctx):
    """Die hellen Läufe der Elche, vorn ein dunklerer Streif."""
    rs = (ctx.s + 0.5) / max(ctx.w, 1)
    v = 0.56 + _LIT[ctx.face] + _strands(ctx, 0.7)
    if ctx.face == "front" and 0.35 < rs < 0.65:
        v -= 0.16
    return pick(ctx.pal["leg"], v)


def _hoof(ctx: Ctx):
    ry = rel_y(ctx)
    rs = (ctx.s + 0.5) / max(ctx.w, 1)
    if ctx.face == "front" and abs(rs - 0.5) < 0.07:
        return pick(ctx.pal["nose"], 0.0)                                        # der gespaltene Huf
    return pick(ctx.pal["nose"], 0.55 - ry * 0.3 + _LIT[ctx.face])


def _saddle(ctx: Ctx):
    """Sattel aus braunem Leder mit hellen Nähten am Rand."""
    v = 0.5 + _LIT[ctx.face] + (hash01(ctx.u, ctx.v, 539) - 0.5) * 0.08
    if ctx.face == "top" and (ctx.s in (1, ctx.w - 2) or ctx.t in (1, ctx.h - 2)):
        return pick(ctx.pal["leather"], 0.92)
    return pick(ctx.pal["leather"], v)


def _flap(ctx: Ctx):
    return pick(ctx.pal["leather"], 0.42 + _LIT[ctx.face] + (hash01(ctx.u, ctx.v, 541) - 0.5) * 0.08)


def _strap(ctx: Ctx):
    rs = (ctx.s + 0.5) / max(ctx.w, 1)
    if ctx.face in ("west", "east") and abs(rs - 0.5) < 0.12 and ctx.cube.size[2] < 2:
        return pick(ctx.pal["metal"], 0.75)                                       # Schnalle
    return pick(ctx.pal["leather"], 0.32 + _LIT[ctx.face])


def _metal(ctx: Ctx):
    return pick(ctx.pal["metal"], 0.6 + _LIT[ctx.face])


def _chest(ctx: Ctx):
    """Truhe: Bretter mit dunklen Fugen, eiserne Beschläge an den Kanten, ein Schloss vorn."""
    rt = (ctx.t + 0.5) / max(ctx.h, 1)
    rs = (ctx.s + 0.5) / max(ctx.w, 1)
    if ctx.s == 0 or ctx.s == ctx.w - 1 or ctx.t == 0 or ctx.t == ctx.h - 1:
        return pick(ctx.pal["metal"], 0.45 + _LIT[ctx.face])
    if ctx.face in ("west", "east") and abs(rs - 0.5) < 0.08 and 0.3 < rt < 0.45:
        return pick(ctx.pal["metal"], 0.85)
    if int(rt * 4.0) != int((rt - 0.5 / max(ctx.h, 1)) * 4.0):
        return pick(ctx.pal["wood"], 0.15)
    return pick(ctx.pal["wood"], 0.5 + _LIT[ctx.face] + (hash01(ctx.u // 3, ctx.v, 543) - 0.5) * 0.15)


def materials(variant: str) -> dict:
    return {"fur": _fur, "hump": _hump, "neck": _neck, "skull": _skull, "muzzle": _muzzle, "nose": _nose, "eye": _eye, "jaw": _jaw,
            "bell": _bell, "ear": _ear, "beam": _beam, "palm": _palm, "tine": _tine, "vine": _vine, "tail": _tail,
            "leg_upper": _leg_upper, "thigh": _thigh, "leg": _leg, "hoof": _hoof, "saddle": _saddle, "flap": _flap, "strap": _strap,
            "metal": _metal, "chest": _chest}
