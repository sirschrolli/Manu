"""
Junger Lavastelzer (statt Schreiter-Jungtier) auf dem Skelett des Vanilla-Jungtiers: dieselben Knochen und Drehpunkte
wie BabyStriderModel (body mit drei Haarkämmen, zwei Beine). Ein runder Körper mit großen Kulleraugen und kleinem Maul,
oben drei feurige Haarkämme, kurze Beinchen mit breiten Füßen; dieselben Farben wie die Großen, auch frierend.
"""
from __future__ import annotations

import math

from ..geometry import Model
from ..paint import Ctx, hash01, hexc, mix, pick
from . import strider

PALETTES = strider.PALETTES
LOOKS = strider.LOOKS


def build() -> Model:
    m = Model("strider_baby", 64, 64)
    m.bone("body", pivot=(0, 16.75, 0))
    m.cube("body", (-3.5, -3.75, -4), (7, 7, 8), "body")
    for name, z in (("bristle0", 2), ("bristle1", 0), ("bristle2", -2)):
        m.bone(name, "body", pivot=(0, -4.25, z))
        m.cube(name, (-3.5, -2.5, 0), (7, 3, 0), "hair")
    for side, x in (("right", -1.5), ("left", 1.5)):
        leg = f"{side}_leg"
        m.bone(leg, pivot=(x, 20, 0))
        m.cube(leg, (-1, 0, -1), (2, 4, 2), "leg")
        m.cube(leg, (-1.4, 3.2, -1.6), (2.8, 0.8, 3), "foot")
    return m


def _face(ctx: Ctx):
    """Gesicht (7 x 7): große Kulleraugen, ein kleines Maul."""
    s, t = ctx.s, ctx.t
    cold = strider._cold(ctx)
    if t in (2, 3) and s in (1, 2, 4, 5):
        inner = s in (2, 4)
        if cold and t == 2:
            return strider._skin(ctx, 0.42, pores=False)                                     # halb geschlossen
        if t == 3 and inner:
            return ctx.pal["pupil"]                                             # Pupille unten innen
        if t == 2 and not inner and not cold:
            return hexc("#fffbe8")                                              # Glanzpunkt
        glow = ctx.pal.get("eye_glow")
        if glow is not None:
            ctx.glow = glow
        return pick(ctx.pal["eye"], 0.85)
    if t == 5 and 2 <= s <= 4:
        return ctx.pal["mouth"] if s != 3 or cold else pick(ctx.pal["teeth"], 0.8)   # kleines Maul mit Zähnchen
    if t == 4 and s in (1, 5):
        return strider._skin(ctx, 0.66, pores=False)                                         # Bäckchen
    return strider._skin(ctx, 0.58, pores=False)


def _body(ctx: Ctx):
    if ctx.face == "front":
        return _face(ctx)
    return strider._skin(ctx, 0.4 if ctx.face == "bottom" else 0.56)


def _hair(ctx: Ctx):
    """Haarkamm: Strähnen nach oben, die Spitzen glimmen."""
    col = ctx.s
    name = ctx.cube.bone.name
    length = 0.6 + 0.4 * hash01(col, name, "bl")
    k = 1.0 - ctx.t / max(ctx.h - 1, 1)                                         # 0 unten, 1 oben
    if k > length:
        return None
    tip = (length - k) / max(length, 1e-3)
    if tip < 0.3:
        glow = ctx.pal.get("tip_glow")
        if glow is not None:
            ctx.glow = mix(hexc("#000000"), glow, 0.8)
        return pick(ctx.pal["tip"], 0.7)
    return pick(ctx.pal["hair"], 0.45 + (hash01(col, name, "bs") - 0.5) * 0.16 + (0.06 if col % 2 else 0.0))


def _leg(ctx: Ctx):
    k = ctx.t / max(ctx.h - 1, 1)
    if ctx.face in ("top", "bottom"):
        return strider._skin(ctx, 0.4)
    if k > 0.5:
        return strider._heat(ctx, (k - 0.5) * 2.0)
    return strider._skin(ctx, 0.5)


def _foot(ctx: Ctx):
    if ctx.face == "top":
        return strider._heat(ctx, 0.7)
    return pick(ctx.pal["foot"], 0.5 if ctx.face != "bottom" else 0.3)


def materials(variant: str) -> dict:
    return {"body": _body, "hair": _hair, "leg": _leg, "foot": _foot}


def hidden(variant: str) -> list[str]:
    return []
