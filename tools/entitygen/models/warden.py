"""
Tiefenhüter (statt Wärter) auf dem Skelett des Vanilla-Wärters: gleiche Knochen, Eltern und Drehpunkte wie WardenModel
(bone -> body -> head -> right/left_tendril, right/left_arm, right/left_ribcage; bone -> right/left_leg), damit
Gehen, Schnüffeln, Brüllen, Graben, Auftauchen, Schlag und Schallstoß genauso laufen. Eigene Quader und Bilder:

  * ein blinder Riese aus der Tiefe: schiefergraue, steinartige Haut aus Platten mit dunklen Fugen, schwerer Brauenwulst
    über einem augenlosen Gesicht, breiter, vorspringender Kiefer mit Zähnen
  * ein Käfig aus bleichen Rippen über der Brust, dahinter eine dunkle Höhle mit einem leuchtenden Seelenherz
  * zwei große, gekrümmte Hörner (die Ranken-Ebenen) mit leuchtenden Spitzen
  * leuchtende Adern in den Fugen von Schultern und Armen, pulsierende Flecken, Rückenplatten, mächtige Unterarme mit
    Krallen, Zehenkrallen

Alles Leuchtende (Herz, Adern, Flecken, Hornspitzen) liegt in der Leucht-Textur; Herz und Adern sind getrennte Formen.
"""
from __future__ import annotations

import math

from ..geometry import Model
from ..paint import Ctx, Voronoi3D, hash01, mix, noise3, pick, ramp, rel_y

PALETTES = {
    "tiefenhueter": {
        "hide": ramp("#0b1217", "#6f8794", steps=9, hue_shift=0.02),
        "seam": ramp("#020405", "#18232b"),
        "cavity": ramp("#010203", "#0d1a22"),
        "bone": ramp("#4e4a40", "#f0e9d4", steps=7),
        "horn": ramp("#1a1e22", "#c9cdc6", steps=8),
        "sculk": ramp("#03141a", "#0f4250"),
        "vein": ramp("#06424a", "#8ffff0", steps=7),
        "heart": ramp("#0a4656", "#eaffff", steps=7),
        "tooth": ramp("#5a5648", "#f4eedc"),
    },
}



def build() -> Model:
    m = Model("warden", 128, 128)
    m.bone("bone", pivot=(0, 24, 0))

    # ---------------------------------------------------------------- Rumpf
    m.bone("body", "bone", pivot=(0, -21, 0))
    m.cube("body", (-9, -13, -4), (18, 21, 11), "torso")
    for i, (y, h) in enumerate(((-12, 3), (-7.5, 3), (-3, 2.5))):        # Rückenplatten
        m.cube("body", (-2.5 + i * 0.5, y, 7), (5 - i, h, 2 - i * 0.4), "dorsal")
    m.bone("right_ribcage", "body", pivot=(-7, -2, -4))
    m.cube("right_ribcage", (-2, -11, -0.1), (9, 21, 0), "ribs")
    m.bone("left_ribcage", "body", pivot=(7, -2, -4))
    m.cube("left_ribcage", (-7, -11, -0.1), (9, 21, 0), "ribs")

    # ---------------------------------------------------------------- Kopf
    m.bone("head", "body", pivot=(0, -13, 0))
    m.cube("head", (-8, -16, -5), (16, 16, 10), "skull")
    m.cube("head", (-8.5, -12, -6.2), (17, 2.5, 1.5), "brow")           # schwerer Brauenwulst
    m.cube("head", (-7, -5, -6), (14, 5, 1.2), "jaw")                    # breiter Kiefer mit Zähnen
    m.cube("head", (-9.6, -14, -1.5), (2, 4, 3), "horn_base")            # Hornansätze an den Schläfen
    m.cube("head", (7.6, -14, -1.5), (2, 4, 3), "horn_base")
    m.bone("right_tendril", "head", pivot=(-8, -12, 0))
    m.cube("right_tendril", (-16, -13, 0), (16, 16, 0), "horn")
    m.bone("left_tendril", "head", pivot=(8, -12, 0))
    m.cube("left_tendril", (0, -13, 0), (16, 16, 0), "horn")

    # ---------------------------------------------------------------- Arme (Drehpunkte wie Vanilla)
    for side, sx in (("right", -1), ("left", 1)):
        arm = f"{side}_arm"
        m.bone(arm, "body", pivot=(13 * sx, -13, 1))
        m.cube(arm, (-4, 0, -4), (8, 28, 8), "arm")
        m.cube(arm, (-4.6, 13, -4.6), (9.2, 12, 9.2), "forearm")         # mächtiger Unterarm
        for i, cx in enumerate((-3.4, -1.0, 1.4)):                        # drei Krallen vorn an der Faust
            m.cube(arm, (cx, 27, -4.2), (2, 4 - (i == 1) * -1, 2), "claw")
        m.cube(arm, (-0.8 + 2.6 * sx, 27, 1.2), (2, 3, 2), "claw")       # Daumenkralle seitlich

    # ---------------------------------------------------------------- Beine (Drehpunkte wie Vanilla)
    m.bone("right_leg", "bone", pivot=(-5.9, -13, 0))
    m.cube("right_leg", (-3.1, 0, -3), (6, 13, 6), "leg")
    m.cube("right_leg", (-3.4, 11, -5), (6.6, 2, 2), "toes")
    m.bone("left_leg", "bone", pivot=(5.9, -13, 0))
    m.cube("left_leg", (-2.9, 0, -3), (6, 13, 6), "leg")
    m.cube("left_leg", (-3.2, 11, -5), (6.6, 2, 2), "toes")
    return m


# =============================================================================================
# Bemalen
# =============================================================================================

def _glow(ctx: Ctx, group: str, col):
    """Setzt das Leuchten nur für erlaubte Gruppen (params["glow_groups"]: heart, veins, spots, horns; Standard: alle)."""
    groups = ctx.params.get("glow_groups")
    if groups is None or group in groups:
        ctx.glow = col


_LIT = {"top": 0.1, "front": 0.03, "west": 0.05, "east": -0.03, "back": -0.06, "bottom": -0.14}

# pulsierende Flecken (Welt-Punkte, Radius): Schultern, oberer Rücken, Oberarme, Schenkel
_SPOTS = [(-7.0, -8.0, 4.0, 1.3), (7.5, -7.5, 4.6, 1.1), (-3.5, -3.0, 8.0, 1.0), (4.5, 0.5, 8.0, 0.9),
          (-17.2, -4.0, 0.0, 1.0), (17.2, -2.0, 2.0, 1.0), (-9.2, 15.0, -1.0, 0.85), (9.2, 17.0, 0.0, 0.85),
          (0.0, -10.0, 8.0, 0.8), (-12.0, -10.0, -2.5, 0.9), (12.5, -10.0, 3.0, 0.9)]


def _spot(ctx: Ctx):
    x, y, z = ctx.world
    for sx, sy, sz, r in _SPOTS:
        d = math.sqrt((x - sx) ** 2 + (y - sy) ** 2 + (z - sz) ** 2)
        if d < r:
            col = pick(ctx.pal["vein"], 0.9 if d < r * 0.45 else 0.62)
            _glow(ctx, "spots", col)
            return col
        if d < r + 0.8:
            return pick(ctx.pal["sculk"], 0.45)                           # dunkler Sculk-Hof um den Fleck
    return None


_PLATES = Voronoi3D(6.5, 7)


def _hide(ctx: Ctx, base: float = 0.55, fold: float = 0.0, plates: bool = False, seed: int = 3):
    """
    Steinartige Haut: ruhige Grundfläche, oben heller; entweder waagrechte Hautfalten (Arme, Beine, Bauch – je Falte
    oben Licht, unten Schatten, dazwischen eine dunkle Kerbe) oder große, unregelmäßige Platten mit dunkleren Fugen
    (Schultern, Rücken, Schädel). Wenige feine Poren.
    """
    x, y, z = ctx.world
    face = ctx.face
    lit = _LIT[face]
    coord = x if face in ("front", "back", "top", "bottom") else z
    v = base + lit + (noise3(x * 0.09, y * 0.09, z * 0.09, seed) - 0.5) * 0.12
    if plates:
        val, edge = _PLATES(x, y, z)
        if edge < 0.1:
            return pick(ctx.pal["hide"], base - 0.24 + lit * 0.5)         # Fuge
        v += (val - 0.5) * 0.12 + (0.05 if edge > 0.6 else -0.03 if edge < 0.25 else 0.0)
    elif fold and face not in ("top", "bottom"):
        wave = 0.7 * math.sin(coord * 0.55 + seed)
        k = (y + wave) / fold
        f = k - math.floor(k)
        if f < 0.16:
            v -= 0.2                                                      # Kerbe
        else:
            v += 0.09 * (1.0 - 2.0 * (f - 0.16) / 0.84)                  # Wulst: oben Licht, unten Schatten
    if hash01(math.floor(x * 1.0), math.floor(y * 1.0), math.floor(z * 1.0), seed + 1) > 0.965:
        v -= 0.08                                                         # Pore
    return pick(ctx.pal["hide"], v)


def _vein_path(c: float, y: float, phase: float, amp: float = 1.3) -> bool:
    return abs(c - amp * math.sin(y * 0.3 + phase)) < 0.55


def _arm_vein(ctx: Ctx) -> bool:
    """Leuchtende Adern: je Arm eine geschlängelte Ader außen und eine vorn, mit Abzweigen; Arm-Koordinaten."""
    lx, ly, lz = ctx.local
    bone = ctx.cube.bone.name
    out = -1 if bone.startswith("right") else 1
    face = ctx.face
    outer = "west" if out < 0 else "east"
    if face == outer:
        if _vein_path(lz, ly, 1.0) and ly > 1.0:
            return True
        if 7.0 < ly < 11.0 and abs(lz - (1.3 * math.sin(7.0 * 0.3 + 1.0) - (ly - 7.0) * 0.9)) < 0.55:
            return True                                                   # Abzweig nach vorn
        if 18.0 < ly < 21.5 and abs(lz - (1.3 * math.sin(18.0 * 0.3 + 1.0) + (ly - 18.0) * 0.8)) < 0.55:
            return True                                                   # Abzweig nach hinten
    if face == "front" and ly > 4.0:
        if _vein_path(lx * out, ly, 2.6, 1.0) and ly < 24.0:
            return True
    return False


# ------------------------------------------------------------------------------- Rumpf

_HEART = ["..##.##..",
          ".#######.",
          "#########",
          "#########",
          ".#######.",
          "..#####..",
          "...###...",
          "....#...."]


def _heart(bx: float, by: float):
    """Seelenherz in der Brust (Rumpf-Koordinaten): Helligkeit 0..1 oder None."""
    ix = math.floor(bx + 4.5)
    iy = math.floor(by + 10.0)

    def inside(i, j):
        return 0 <= j < len(_HEART) and 0 <= i < 9 and _HEART[j][i] == "#"

    if not inside(ix, iy):
        return None
    if not all(inside(ix + dx, iy + dy) for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1))):
        return 0.42                                                       # dunklerer Rand: die Form bleibt lesbar
    core = abs(ix - 4) * 1.0 + abs(iy - 3.5) * 1.1
    return 0.98 if core <= 1.2 else 0.82 if core <= 2.6 else 0.66


def _torso(ctx: Ctx):
    x, y, z = ctx.world
    bx, by = x, y - 3.0                                                   # Rumpf-Koordinaten
    spot = _spot(ctx)
    if spot is not None:
        return spot
    if ctx.face == "front":
        # Brusthöhle: ein Oval hinter den Rippen, zum Rand hin in die Haut übergehend
        e = (bx / 6.8) ** 2 + ((by + 5.5) / 7.2) ** 2
        if e < 1.0:
            h = _heart(bx, by)
            if h is not None:
                col = pick(ctx.pal["heart"], h)
                _glow(ctx, "heart", col)
                return col
            hd = math.hypot(bx, (by + 6.0) * 1.1)
            t = max(0.0, 1.0 - hd / 6.0)
            col = mix(pick(ctx.pal["cavity"], 0.55 + (0.3 if e > 0.8 else 0.0)), pick(ctx.pal["heart"], 0.22), t * 0.85)
            if t > 0:
                _glow(ctx, "heart", mix((0, 0, 0, 255), pick(ctx.pal["heart"], 0.3), t * 0.7))
            return col
        if e < 1.25:
            return pick(ctx.pal["hide"], 0.3)                             # Rand der Höhle
        return _hide(ctx, 0.52, fold=3.5, seed=13)                        # Bauch mit Falten
    if ctx.face == "back" or ctx.face == "top" or by < -5.0:
        return _hide(ctx, 0.55, plates=True, seed=11)
    return _hide(ctx, 0.53, fold=4.0, seed=12)


def _dorsal(ctx: Ctx):
    """Knöcherne Rückenplatten: dunkel an der Wurzel, zur Kante hin heller."""
    if ctx.face == "bottom":
        return pick(ctx.pal["hide"], 0.2)
    if ctx.face == "front":
        return pick(ctx.pal["hide"], 0.3)
    v = 0.34 + _LIT[ctx.face] + (1.0 - rel_y(ctx)) * 0.12
    if ctx.face == "back":
        v += 0.06 if ctx.t == 0 else 0.0
    return pick(ctx.pal["horn"], v)


def _ribs(ctx: Ctx):
    """
    Rippenkäfig: kurzes Brustbein oben, fünf Rippenpaare, die nach außen absinken; um das Herz weichen die Rippen
    auseinander (der Käfig öffnet sich); dazwischen durchsichtig.
    """
    x, y, _ = ctx.world
    bx, by = x, y - 3.0
    ax = abs(bx)
    if ctx.face not in ("front", "back"):
        return None
    if ax < 1.0 and -12.5 <= by < -10.0:
        return pick(ctx.pal["bone"], 0.72 if ctx.face == "front" else 0.3)  # Brustbein-Ansatz
    hit = _rib_hit(ax, by)
    if hit is None:
        return None
    return pick(ctx.pal["bone"], hit if ctx.face == "front" else 0.25)


def _rib_hit(ax: float, by: float):
    for i in range(5):
        top = -11.8 + i * 3.0
        reach = 8.8 - i * 0.5
        inner = 1.0 if i == 0 else 3.6 + (i - 1) * 0.4 if i < 4 else 5.0   # Öffnung um das Herz
        if ax < inner or ax > reach:
            continue
        drop = ((ax - inner) / 8.8) ** 1.4 * 2.4
        rt = top + drop
        if rt <= by < rt + 1.6:
            upper = by < rt + 0.8
            tip = ax > reach - 0.9
            return 0.92 if upper and not tip else 0.66 if not tip else 0.5
    return None


# ------------------------------------------------------------------------------- Kopf

def _skull(ctx: Ctx):
    face, s, t = ctx.face, ctx.s, ctx.t
    if face == "front" and 6 <= t <= 10:
        # augenloses Gesicht unter dem Wulst: glatte Haut, wo Augen wären nur zugewachsene Narben, Wangenknochen,
        # Nasenschlitze
        v = 0.52 + (0.04 if 6 <= s <= 9 else 0.0)
        if t == 6:
            v -= 0.14                                                     # Schatten unter dem Wulst
        if t == 7 and s in (3, 4, 5, 10, 11, 12):
            return pick(ctx.pal["seam"], 0.55 if s in (4, 11) else 0.75)  # zugewachsene Augenschlitze
        if t == 8 and s in (2, 3, 4, 11, 12, 13):
            v += 0.1                                                      # Wangenknochen
        if t == 9 and s in (7, 8):
            return pick(ctx.pal["seam"], 0.3)                             # Nasenschlitze
        return pick(ctx.pal["hide"], v)
    if face == "top":
        x, y, z = ctx.world
        n = noise3(x * 0.32, 0, z * 0.32, 21)
        if n > 0.64:
            return pick(ctx.pal["sculk"], 0.4 + (n - 0.64) * 2.0)         # Sculk-Bewuchs auf dem Schädel
    return _hide(ctx, 0.55, plates=True, seed=23)


def _brow(ctx: Ctx):
    face = ctx.face
    if face == "bottom":
        return pick(ctx.pal["seam"], 0.5)
    v = 0.6 + _LIT[face]
    if face == "front":
        v -= rel_y(ctx) * 0.16
        if ctx.s in (0, ctx.w - 1):
            v -= 0.1
        if ctx.s in (4, 5, 11, 12) and ctx.t == 0:
            v += 0.08                                                     # Wülste über den leeren Augen
    return pick(ctx.pal["hide"], v)


def _jaw(ctx: Ctx):
    face, s, t = ctx.face, ctx.s, ctx.t
    if face == "front":
        if t == 0:
            if s % 2 == 1 and 1 <= s <= 12:
                return pick(ctx.pal["tooth"], 0.78)                       # obere Zähne
            return pick(ctx.pal["hide"], 0.5)
        if t == 1:
            if s % 2 == 0 and 2 <= s <= 11:
                return pick(ctx.pal["tooth"], 0.58)                       # untere Zähne
            return pick(ctx.pal["cavity"], 0.35)                          # Maulspalt
        v = 0.54 - (t - 2) * 0.06 - (0.08 if s in (0, ctx.w - 1) else 0.0)
        return pick(ctx.pal["hide"], v)
    if face == "bottom":
        return pick(ctx.pal["hide"], 0.32)
    return pick(ctx.pal["hide"], 0.52 + _LIT[face])


def _horn_base(ctx: Ctx):
    v = 0.5 + _LIT[ctx.face] - rel_y(ctx) * 0.1
    if ctx.face != "top" and ctx.t % 2 == 0:
        v -= 0.08
    return pick(ctx.pal["horn"], v)


def _bezier(p0, p1, p2, u):
    a = (1 - u) * (1 - u)
    b = 2 * (1 - u) * u
    c = u * u
    return a * p0[0] + b * p1[0] + c * p2[0], a * p0[1] + b * p1[1] + c * p2[1]


_HORN_P = ((0.4, 0.4), (11.0, 2.4), (13.8, -11.8))
_HORN_CURVE = [_bezier(*_HORN_P, i / 80) for i in range(81)]


def _horn(ctx: Ctx):
    """Gekrümmtes Horn auf der Ranken-Ebene: vom Schläfenansatz nach außen und hinauf, Ringe, leuchtende Spitze."""
    if ctx.face not in ("front", "back"):
        return None
    lx, ly, _ = ctx.local
    px = -lx if ctx.cube.origin[0] < -1 else lx                           # rechte Ebene gespiegelt
    best, bu = 99.0, 0.0
    for i, (cx, cy) in enumerate(_HORN_CURVE):
        d = (px - cx) ** 2 + (ly - cy) ** 2
        if d < best:
            best, bu = d, i / 80
    dist = math.sqrt(best)
    half = 2.9 * (1 - bu) ** 0.8 + 0.5
    if dist > half:
        return None
    if bu > 0.8:
        col = pick(ctx.pal["vein"], 0.5 + (bu - 0.8) * 2.4)
        _glow(ctx, "horns", col)
        return col
    cx, cy = _bezier(*_HORN_P, bu)
    tx, ty = _bezier(*_HORN_P, min(1.0, bu + 0.01))
    nx, ny = ty - cy, -(tx - cx)                                          # Normale der Mittellinie
    nl = math.hypot(nx, ny) or 1.0
    side = ((px - cx) * nx + (ly - cy) * ny) / nl / half                  # -1 .. 1 quer über das Horn
    v = 0.5 + bu * 0.2 + side * 0.16
    if (bu * 16.0) % 1.0 < 0.22:
        v -= 0.14                                                         # Ringe
    if abs(side) > 0.78:
        v -= 0.1                                                          # Kontur
    if ctx.face == "back":
        v -= 0.06
    return pick(ctx.pal["horn"], v)


# ------------------------------------------------------------------------------- Glieder

def _arm(ctx: Ctx):
    if ctx.face == "bottom":
        return pick(ctx.pal["hide"], 0.32 if (ctx.s % 3) else 0.2)         # Knöchel der Faust
    spot = _spot(ctx)
    if spot is not None:
        return spot
    if _arm_vein(ctx):
        col = pick(ctx.pal["vein"], 0.62)
        _glow(ctx, "veins", col)
        return col
    return _hide(ctx, 0.55, fold=4.5, seed=31)


def _forearm(ctx: Ctx):
    face = ctx.face
    if face == "bottom":
        return pick(ctx.pal["hide"], 0.26)
    if face == "top":
        return pick(ctx.pal["hide"], 0.62 if (ctx.s + ctx.t) % 6 else 0.54)
    if ctx.t == 0:
        return pick(ctx.pal["hide"], 0.66 + _LIT[face])                    # Wulst an der Oberkante
    spot = _spot(ctx)
    if spot is not None:
        return spot
    if _arm_vein(ctx):
        col = pick(ctx.pal["vein"], 0.7)
        _glow(ctx, "veins", col)
        return col
    return _hide(ctx, 0.52, fold=3.0, seed=41)


def _claw(ctx: Ctx):
    ry = rel_y(ctx)
    if ctx.face == "bottom":
        return pick(ctx.pal["bone"], 0.15)
    v = 0.78 - ry * 0.45 + _LIT[ctx.face]
    return pick(ctx.pal["bone"], v)


def _leg(ctx: Ctx):
    if ctx.face == "bottom":
        return pick(ctx.pal["hide"], 0.2)
    spot = _spot(ctx)
    if spot is not None:
        return spot
    return _hide(ctx, 0.5, fold=3.5, seed=51)


def _toes(ctx: Ctx):
    if ctx.face == "bottom":
        return pick(ctx.pal["hide"], 0.2)
    if ctx.face == "front" and ctx.s % 2 == 1:
        return pick(ctx.pal["bone"], 0.66 - ctx.t * 0.22)                  # Zehenkrallen
    return pick(ctx.pal["hide"], 0.46 + _LIT[ctx.face])


def materials(variant: str) -> dict:
    return {
        "torso": _torso, "dorsal": _dorsal, "ribs": _ribs, "skull": _skull, "brow": _brow, "jaw": _jaw,
        "horn_base": _horn_base, "horn": _horn, "arm": _arm, "forearm": _forearm, "claw": _claw, "leg": _leg,
        "toes": _toes,
    }


def hidden(variant: str) -> list[str]:
    return []
