"""
Schneemann (statt Schneegolem) auf dem Skelett des Vanilla-Schneegolems: gleiche Knochen, Drehpunkte und
Grundwinkel wie SnowGolemModel (head, left_arm, right_arm, upper_body, lower_body), damit Kopfdrehen, das Mitdrehen
des Oberkörpers und der geschnitzte Kürbis auf dem Kopf (SnowGolemHeadLayer) genauso funktionieren. Eigene Quader:

  * drei runde Schneekugeln (je drei gekreuzte Quader statt eines Würfels), unten die größte
  * Kohleknöpfe auf dem Bauch, ein gestrickter Schal mit herabhängendem Ende und Fransen
  * Arme aus Ästen mit Zweigen und Fingern (eigene Kind-Knochen mit festen Winkeln)
  * unter dem Kürbis (nach dem Scheren): Kohleaugen, ein Lächeln aus Kohlestückchen, eine Möhrennase und eine
    Strickmütze mit Bommel – Nase und Mütze sind eigene Knochen, die der Renderer nur ohne Kürbis zeigt

Vier Gestalten: Schal und Mütze in Rot, Grün, Blau oder Gelb (jeweils mit weißen Streifen).
"""
from __future__ import annotations

import math

from ..geometry import Model
from ..paint import Ctx, hash01, mix, noise3, pick, ramp, rel_y

_BASE = {
    "snow": ramp("#8da2bd", "#ffffff", steps=9, hue_shift=-0.02),
    "coal": ramp("#0a0a0c", "#4a4a52", steps=5),
    "carrot": ramp("#7a2a04", "#ffb04a", steps=6, hue_shift=-0.04),
    "bark": ramp("#2a180c", "#8c6440", steps=6),
    "white": ramp("#b8b4ae", "#ffffff", steps=5),
}

PALETTES = {
    "rot": {**_BASE, "wool": ramp("#4a0a0c", "#e8504a", steps=7, hue_shift=-0.03)},
    "gruen": {**_BASE, "wool": ramp("#0c2a12", "#5cbf4e", steps=7)},
    "blau": {**_BASE, "wool": ramp("#0c1a4a", "#5a8ef0", steps=7)},
    "gelb": {**_BASE, "wool": ramp("#5a3a04", "#ffd84a", steps=7, hue_shift=-0.03)},
}

LOOKS = tuple(PALETTES)
_ARM_Z = math.degrees(1.0)


def _ball(m: Model, bone: str, cy: float, r: float, mat: str = "snow"):
    """Runde Schneekugel aus drei gekreuzten Quadern: breit (x), tief (z) und hoch (y), Mittelpunkt (0, cy, 0)."""
    flat = r * 0.62
    m.cube(bone, (-r, cy - flat, -r * 0.72), (2 * r, 2 * flat, r * 1.44), mat)
    m.cube(bone, (-r * 0.72, cy - flat, -r), (r * 1.44, 2 * flat, 2 * r), mat)
    m.cube(bone, (-r * 0.84, cy - r, -r * 0.84), (r * 1.68, 2 * r, r * 1.68), mat)


def build() -> Model:
    m = Model("snow_golem", 128, 128)

    # ---------------------------------------------------------------- Kopf (Drehpunkt wie Vanilla)
    m.bone("head", pivot=(0, 4, 0))
    _ball(m, "head", -4.0, 3.9, "head")
    m.bone("nose", "head", pivot=(0, -4.2, -3.6), rotation=(-8, 0, 0))
    m.cube("nose", (-0.5, -0.5, -3.6), (1, 1, 3.6), "carrot")
    m.cube("nose", (-0.75, -0.75, -0.8), (1.5, 1.5, 0.8), "carrot")
    m.bone("cap", "head", pivot=(0, -7.2, 0))
    m.cube("cap", (-3.6, -1.6, -3.6), (7.2, 2.0, 7.2), "cap_rim")            # umgeschlagener Rand
    m.cube("cap", (-3.1, -3.8, -3.1), (6.2, 2.4, 6.2), "cap")
    m.cube("cap", (-2.2, -5.0, -2.2), (4.4, 1.4, 4.4), "cap")
    m.cube("cap", (-1.2, -6.6, -1.2), (2.4, 1.8, 2.4), "pompom")

    # ---------------------------------------------------------------- Arme: Äste (Drehpunkte und Winkel wie Vanilla)
    for name, pivot, rot in (("left_arm", (5, 6, 1), (0, 0, _ARM_Z)), ("right_arm", (-5, 6, -1), (0, 180, -_ARM_Z))):
        m.bone(name, pivot=pivot, rotation=rot)
        m.cube(name, (-0.5, -0.5, -0.5), (11.5, 1, 1), "stick")
        m.bone(f"{name}_twig", name, pivot=(6.5, 0, 0), rotation=(0, 0, -38))
        m.cube(f"{name}_twig", (0, -0.45, -0.45), (3.2, 0.9, 0.9), "stick")
        m.bone(f"{name}_finger_a", name, pivot=(10.6, 0, 0), rotation=(0, 0, 32))
        m.cube(f"{name}_finger_a", (0, -0.4, -0.4), (2.2, 0.8, 0.8), "stick")
        m.bone(f"{name}_finger_b", name, pivot=(10.6, 0, 0), rotation=(0, 0, -28))
        m.cube(f"{name}_finger_b", (0, -0.4, -0.4), (2.0, 0.8, 0.8), "stick")

    # ---------------------------------------------------------------- Oberkörper mit Schal und Knöpfen
    m.bone("upper_body", pivot=(0, 13, 0))
    _ball(m, "upper_body", -5.0, 5.0, "middle")
    m.cube("upper_body", (-4.3, -10.6, -4.3), (8.6, 1.8, 8.6), "scarf", inflate=0.2)  # Schal um den Hals
    m.cube("upper_body", (1.4, -9.4, -4.9), (2.2, 5.6, 0.9), "scarf_end")              # herabhängendes Ende
    for y in (-7.6, -5.4, -3.2):
        m.cube("upper_body", (-0.6, y, -5.35), (1.2, 1.2, 0.6), "button")

    # ---------------------------------------------------------------- Unterkörper
    m.bone("lower_body", pivot=(0, 24, 0))
    _ball(m, "lower_body", -6.2, 6.2, "base")
    return m


# =============================================================================================
# Bemalen
# =============================================================================================

_LIT = {"top": 0.1, "front": 0.04, "west": 0.0, "east": -0.03, "back": -0.06, "bottom": -0.2}


def _snow(ctx: Ctx, base: float = 0.66, seed: int = 1):
    """
    Festgeklopfter Schnee: weiß mit kühlen, blaugrauen Schatten zur Unterseite und zu den Rändern hin (die Kugel
    wirkt rund), weiche Dellen, ein paar glitzernde Körner und kleine Klumpen.
    """
    x, y, z = ctx.world
    v = base + _LIT[ctx.face]
    if ctx.face not in ("top", "bottom"):
        ry = rel_y(ctx)
        v -= ry * ry * 0.32                                               # unten in den Schatten
        u = ctx.s / max(1, ctx.w - 1) * 2.0 - 1.0
        v -= u * u * 0.1                                                  # zu den Rändern hin runder
    else:
        u = ctx.s / max(1, ctx.w - 1) * 2.0 - 1.0
        w = ctx.t / max(1, ctx.h - 1) * 2.0 - 1.0
        v -= (u * u + w * w) * 0.06
    v += (noise3(x * 0.35, y * 0.35, z * 0.35, seed) - 0.5) * 0.12
    g = hash01(ctx.u, ctx.v, seed)
    if g > 0.965:
        v += 0.12                                                         # Glitzern
    elif g < 0.04:
        v -= 0.08                                                         # kleiner Klumpen
    return pick(ctx.pal["snow"], v)


def _middle(ctx: Ctx):
    return _snow(ctx, 0.8, seed=3)


def _base(ctx: Ctx):
    x, y, z = ctx.world
    col = _snow(ctx, 0.78, seed=5)
    if ctx.face in ("front", "back", "west", "east") and y > 21.5:
        n = noise3(x * 0.6, 0, z * 0.6, 7)
        if n > 0.55:
            return pick(ctx.pal["snow"], 0.3 + n * 0.2)                   # Matsch und Erde am Boden
    return col


def _head(ctx: Ctx):
    """Kopf: Kohleaugen, Kohle-Lächeln, Wangen etwas heller; die Nase ist ein eigener Knochen."""
    if ctx.face == "front":
        x, y, z = ctx.world
        lx, ly = x, y - 4.0                                               # Kopf-Koordinaten (Mitte 0, -4)
        for ex in (-1.5, 1.5):
            if abs(lx - ex) < 0.62 and abs(ly + 5.0) < 0.62:
                return pick(ctx.pal["coal"], 0.25 if lx < ex else 0.55)   # Kohleauge mit Glanz
        for mx in (-2.0, -1.0, 0.0, 1.0, 2.0):
            my = -2.15 + (0.0 if abs(mx) > 1.5 else 0.55 if abs(mx) > 0.5 else 0.8)
            if abs(lx - mx) < 0.45 and abs(ly - my) < 0.45:
                return pick(ctx.pal["coal"], 0.35)                        # Lächeln aus Kohlestückchen
        if abs(abs(lx) - 2.4) < 0.7 and abs(ly + 3.5) < 0.5:
            return pick(ctx.pal["snow"], 0.86)                            # helle Wangen
    return _snow(ctx, 0.82, seed=9)


def _carrot(ctx: Ctx):
    if ctx.face in ("top", "bottom", "west", "east"):
        z = ctx.local[2]
        v = 0.55 + (0.12 if ctx.face == "top" else -0.1 if ctx.face == "bottom" else 0.0)
        if abs(z * 2.2) % 2.0 < 0.5:
            v -= 0.14                                                     # Rillen der Möhre
        v += (-z / 3.6) * 0.1
        return pick(ctx.pal["carrot"], v)
    if ctx.face == "front":
        return pick(ctx.pal["carrot"], 0.72)
    return pick(ctx.pal["carrot"], 0.4)


def _knit(ctx: Ctx, base: float, stripes: bool = True):
    """Strickmuster: kleine V-Maschen (abwechselnd hell und dunkel), weiße Streifen im Abstand."""
    x, y, z = ctx.world
    v = base + _LIT[ctx.face] * 0.6
    if (ctx.s + (ctx.t % 2)) % 2 == 0:
        v += 0.06
    else:
        v -= 0.03
    if stripes and ctx.face not in ("top", "bottom") and int(math.floor(y)) % 3 == 0:
        return pick(ctx.pal["white"], 0.62 + (0.15 if (ctx.s + ctx.t) % 2 else 0.0))
    return pick(ctx.pal["wool"], v)


def _scarf(ctx: Ctx):
    if ctx.face == "top":
        if 0 < ctx.s < ctx.w - 1 and 0 < ctx.t < ctx.h - 1:
            return None                                                   # oben offen (der Kopf sitzt darin)
        return _knit(ctx, 0.62, stripes=False)
    if ctx.face == "bottom":
        return pick(ctx.pal["wool"], 0.25)
    if ctx.t == 0:
        return _knit(ctx, 0.7, stripes=False)
    return _knit(ctx, 0.52, stripes=False)


def _scarf_end(ctx: Ctx):
    if ctx.face in ("top", "bottom"):
        return pick(ctx.pal["wool"], 0.3)
    if ctx.t >= ctx.h - 1:
        return pick(ctx.pal["wool"], 0.62) if ctx.s % 2 == 0 else None  # Fransen
    if ctx.t in (2, 4):
        return pick(ctx.pal["white"], 0.72)
    return _knit(ctx, 0.55 - rel_y(ctx) * 0.1, stripes=False)


def _cap(ctx: Ctx):
    if ctx.face == "bottom":
        return pick(ctx.pal["wool"], 0.2)
    if ctx.face == "top":
        return _knit(ctx, 0.66, stripes=False)
    return _knit(ctx, 0.58 - rel_y(ctx) * 0.1)


def _cap_rim(ctx: Ctx):
    if ctx.face == "bottom":
        return pick(ctx.pal["wool"], 0.2)
    if ctx.face == "top":
        return pick(ctx.pal["white"], 0.7)
    v = 0.66 if (ctx.s % 2 == 0) else 0.5                                 # Rippenstrick am Rand
    return pick(ctx.pal["white"], v - rel_y(ctx) * 0.15)


def _pompom(ctx: Ctx):
    v = 0.7 + _LIT[ctx.face] + (hash01(ctx.u, ctx.v, "pom") - 0.5) * 0.3
    return pick(ctx.pal["white"], v)


def _stick(ctx: Ctx):
    v = 0.48 + _LIT[ctx.face] * 1.4
    lx = ctx.local[0]
    if hash01(int(lx * 2), ctx.face, "knot") > 0.85:
        v -= 0.14                                                         # Astknoten
    if ctx.face in ("top",) and hash01(int(lx * 3), "snow") > 0.6:
        return pick(ctx.pal["snow"], 0.8)                                 # etwas Schnee auf dem Ast
    return pick(ctx.pal["bark"], v + (hash01(ctx.u, ctx.v) - 0.5) * 0.12)


def _button(ctx: Ctx):
    if ctx.face == "front":
        return pick(ctx.pal["coal"], 0.62 if (ctx.s == 0 and ctx.t == 0) else 0.3)
    return pick(ctx.pal["coal"], 0.15)


def materials(variant: str) -> dict:
    return {
        "head": _head, "middle": _middle, "base": _base, "snow": _middle, "carrot": _carrot, "cap": _cap,
        "cap_rim": _cap_rim, "pompom": _pompom, "stick": _stick, "scarf": _scarf, "scarf_end": _scarf_end,
        "button": _button,
    }


def hidden(variant: str) -> list[str]:
    return []
