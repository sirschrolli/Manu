"""
Junger Borstling (statt Piglin-Kind) auf dem Skelett des Vanilla-Piglin-Kindes: dieselben Knochen und Drehpunkte wie
BabyPiglinModel (head mit hat und den Ohren samt ihrer schrägen Unterknochen, body, Arme, Beine). Großer Kopf mit
Kulleraugen, rundem Rüssel und einem Borstenbüschel, ein kleiner Kittel mit Gürtel; dieselben Farben wie die Großen.
Auch das Kind des Moderborstlings trägt diese Gestalt.
"""
from __future__ import annotations

from ..geometry import Model
from ..paint import Ctx, hash01, pick, rel_y
from . import piglin

PALETTES = piglin.PALETTES
LOOKS = piglin.LOOKS


def build() -> Model:
    m = Model("piglin_baby", 64, 64)
    m.bone("body", pivot=(0, 18, -0.5))
    m.cube("body", (-3, -3, -1), (6, 5, 3), "torso")
    m.cube("body", (-3.3, 0.6, -1.3), (6.6, 1.2, 3.6), "belt")
    m.bone("head", pivot=(0, 15, 0))
    m.cube("head", (-4.5, -6, -3.5), (9, 6, 7), "head")
    m.cube("head", (-1.5, -3, -4.5), (3, 3, 1), "snout")
    m.cube("head", (-1, -7.6, -2), (2, 1.6, 4), "tuft")                       # Borstenbüschel
    m.bone("hat", "head", pivot=(0, 0, 0))
    m.bone("left_ear", "head", pivot=(4.2, -4, 0))
    m.bone("left_ear_r1", "left_ear", pivot=(1, 1.75, 0), rotation=(0, 0, -35))
    m.cube("left_ear_r1", (-0.5, -3, -2), (1, 6, 4), "ear")
    m.bone("right_ear", "head", pivot=(-4.2, -4, 0))
    m.bone("right_ear_r1", "right_ear", pivot=(-1, 1.75, 0), rotation=(0, 0, 35))
    m.cube("right_ear_r1", (-0.5, -3, -2), (1, 6, 4), "ear")
    m.bone("left_arm", pivot=(4, 15, 0))
    m.cube("left_arm", (-1, 0, -1.5), (2, 5, 3), "arm")
    m.bone("right_arm", pivot=(-4, 15, 0))
    m.cube("right_arm", (-1, 0, -1.5), (2, 5, 3), "arm")
    m.bone("right_leg", pivot=(-1.5, 20, 0))
    m.cube("right_leg", (-1.5, 0, -1.5), (3, 4, 3), "leg")
    m.bone("left_leg", pivot=(1.5, 20, 0))
    m.cube("left_leg", (-1.5, 0, -1.5), (3, 4, 3), "leg")
    return m


def _head(ctx: Ctx):
    """Kopf (Vorderseite 9 x 6): Haaransatz, große Kulleraugen, Wangen; die übrigen Seiten wie bei den Großen."""
    face, s, t = ctx.face, ctx.s, ctx.t
    if face != "front":
        return piglin._head(ctx)
    rotten = piglin._rotten(ctx)
    if t == 0:
        return piglin._bristle(ctx, 0.5)
    if t in (1, 2) and s in (1, 2, 6, 7):
        if rotten and s in (6, 7):
            ctx.glow = ctx.pal["glow"]
            return ctx.pal["glow"]                                       # ein glimmendes Auge
        if (s, t) in ((1, 1), (6, 1)):
            return ctx.pal["eye_white"]                                  # Glanzpunkt
        return ctx.pal["eye_dark"]                                       # große, dunkle Kulleraugen
    if t in (3, 4) and s in (1, 7):
        return piglin._skin(ctx, 0.68)                                   # rosige Wangen
    return piglin._skin(ctx, 0.62)


def _tuft(ctx: Ctx):
    if ctx.face == "bottom":
        return None
    if ctx.face != "top" and ctx.t == 0 and hash01(ctx.s, ctx.face) > 0.5:
        return None
    return piglin._bristle(ctx, 0.5)


def _torso(ctx: Ctx):
    face, t = ctx.face, ctx.t
    if face == "top":
        return piglin._skin(ctx, 0.55)
    if piglin._rotten(ctx) and hash01(ctx.s, t, face) > 0.8:
        return pick(ctx.pal["flesh"], 0.3)
    return pick(ctx.pal["cloth"], 0.58 + piglin._LIT[face] - t * 0.03)


def _arm(ctx: Ctx):
    face, t = ctx.face, ctx.t
    if face == "bottom" or (face != "top" and t >= ctx.h - 1):
        return pick(ctx.pal["hoof"], 0.5)
    return piglin._skin(ctx, 0.6)


def _leg(ctx: Ctx):
    face, t = ctx.face, ctx.t
    if face == "bottom" or (face != "top" and t >= ctx.h - 1):
        return pick(ctx.pal["hoof"], 0.45)
    return pick(ctx.pal["leather"], 0.5 + piglin._LIT[face] - t * 0.04)


def materials(variant: str) -> dict:
    base = piglin.materials(variant)
    return {"torso": _torso, "belt": base["belt"], "head": _head, "snout": base["snout"], "tuft": _tuft, "ear": base["ear"],
            "arm": _arm, "leg": _leg}


def hidden(variant: str) -> list[str]:
    return []
