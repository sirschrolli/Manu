"""
Glut-Frischling (statt Hoglin-Jungtier) auf dem Skelett des Vanilla-Hoglin-Jungtiers: dieselben Knochen und Drehpunkte
wie BabyHoglinModel (head mit den Ohren, body, vier Beine). Ein großer, runder Kopf mit Kulleraugen, kurzer Schnauze
und zwei Hauer-Stummeln, ein Borstenschopf auf Stirn und Rücken, ein Ringelschwanz – und wie bei Frischlingen helle
Längsstreifen, beim Schlackenkeiler glimmend. Auch das Junge des Moderkeilers trägt diese Gestalt (verwest, ohne
Streifen).
"""
from __future__ import annotations

import math

from ..geometry import Model
from ..paint import Ctx, hash01, hexc, mix, pick, ramp, rel_y
from . import hoglin

PALETTES = {
    "karmesin": {**hoglin.PALETTES["karmesin"], "stripe": ramp("#7a3a1c", "#f0b07a", steps=6)},
    "aschen": {**hoglin.PALETTES["aschen"], "stripe": ramp("#5a5450", "#e8e0d4", steps=6)},
    "schlacke": {**hoglin.PALETTES["schlacke"], "stripe": ramp("#4a1a08", "#c8682a", steps=6),
                 "stripe_glow": hexc("#5a2008")},
    "moder_karmesin": hoglin.PALETTES["moder_karmesin"],
    "moder_aschen": hoglin.PALETTES["moder_aschen"],
}

LOOKS = hoglin.LOOKS


def build() -> Model:
    m = Model("hoglin_baby", 64, 128)
    m.world_faces = True
    m.bone("head", pivot=(0, 13, -7), rotation=(50, 0, 0))
    m.cube("head", (-4.5, -2.6, -6.5), (9, 5.2, 6.5), "skull")
    m.cube("head", (-3.2, -2.0, -10.5), (6.4, 4, 4), "muzzle")
    m.cube("head", (-2.6, -1.6, -11.5), (5.2, 3.2, 1), "disk")
    m.cube("head", (-1.3, -3.7, -5.2), (2.6, 1.2, 4.6), "tuft")              # Stirnschopf
    m.cube("head", (-3.7, -0.9, -9.8), (1, 1.6, 1), "nub")                    # Hauer-Stummel
    m.cube("head", (2.7, -0.9, -9.8), (1, 1.6, 1), "nub")
    m.bone("right_ear", "head", pivot=(-4.3, -1.8, -1.6), rotation=(0, 0, -50))
    m.cube("right_ear", (-4.4, -0.5, -1.8), (4.4, 1, 3.6), "ear")
    m.bone("left_ear", "head", pivot=(4.3, -1.8, -1.6), rotation=(0, 0, 50))
    m.cube("left_ear", (0, -0.5, -1.8), (4.4, 1, 3.6), "ear")

    m.bone("body", pivot=(0, 24, 0))
    m.cube("body", (-4, -14, -7), (8, 8, 14), "trunk")
    m.cube("body", (-1.1, -15.3, -7.4), (2.2, 1.6, 9.4), "crest")             # Borstenschopf auf dem Rücken
    m.cube("body", (0, -17.8, -7.8), (0, 3.8, 10.4), "spines")
    m.cube("body", (-0.5, -13.4, 6.9), (1, 2.6, 1), "tail")
    m.cube("body", (-0.9, -11.2, 6.7), (1.8, 1.6, 1.4), "tuft")

    for name, x, z in (("right_hind_leg", -2.5, 4.5), ("left_hind_leg", 2.5, 4.5),
                       ("right_front_leg", -2.5, -4.5), ("left_front_leg", 2.5, -4.5)):
        m.bone(name, pivot=(x, 18, z))
        m.cube(name, (-1.5, 0, -1.5), (3, 6, 3), "leg")
    return m


def _rotten(ctx: Ctx) -> bool:
    return hoglin._rotten(ctx)


def _trunk(ctx: Ctx):
    """Rumpf mit den Frischlingsstreifen (Längsstreifen an Rücken und Flanken)."""
    face = ctx.face
    if face == "bottom":
        return hoglin._hide(ctx, 0.46)
    col = hoglin._hair(ctx, 0.46 if face != "back" else 0.4, seed=40, length=2.0)
    if _rotten(ctx) or face in ("front", "back"):
        return col
    x, y, z = ctx.world
    across = y if face in ("west", "east") else abs(x) + 10.0                   # Flanken: waagrecht, Rücken: längs
    phase = (across + math.sin(z * 0.45) * 0.4) % 2.6
    if phase < 0.95 and (face != "west" and face != "east" or y < 16.4):
        glow = ctx.pal.get("stripe_glow")
        if glow is not None:
            ctx.glow = glow
        return mix(col, pick(ctx.pal["stripe"], 0.62 + (hash01(ctx.u, ctx.v) - 0.5) * 0.1), 0.62)
    return col


def _skull(ctx: Ctx):
    lface = ctx.lface
    lx, ly, lz = ctx.local
    if lface == "top":
        ax = abs(lx)
        if 2.0 <= ax <= 4.0 and -6.5 <= lz <= -4.4:                              # große Kulleraugen
            if 2.5 <= ax <= 3.5 and -6.0 <= lz <= -5.0:
                if _rotten(ctx):
                    if lx > 0:
                        ctx.glow = ctx.pal["glow"]
                        return ctx.pal["glow"]
                    return ctx.pal["socket"]
                ctx.glow = ctx.pal["eye"]
                return ctx.pal["eye"]
            return ctx.pal["socket"]
        return hoglin._hair(ctx, 0.48, seed=42, length=2.0)
    if lface in ("west", "east"):
        return hoglin._hair(ctx, 0.46, seed=43, length=2.0)
    if lface == "front":
        return hoglin._hide(ctx, 0.42)
    return hoglin._hair(ctx, 0.38, seed=44)


def _muzzle(ctx: Ctx):
    if ctx.lface == "top":
        return hoglin._hide(ctx, 0.56 - (0.05 if ctx.t % 2 else 0.0))
    if ctx.lface in ("west", "east") and ctx.local[1] > 1.2:
        return hoglin._hide(ctx, 0.3)
    return hoglin._hide(ctx, 0.46)


def _disk(ctx: Ctx):
    if ctx.lface == "front":
        lx, ly, lz = ctx.local
        if abs(abs(lx) - 1.1) < 0.5 and abs(ly) < 0.6:
            return pick(ctx.pal["snout"], 0.08)
        return pick(ctx.pal["snout"], 0.66)
    return pick(ctx.pal["snout"], 0.5)


def _tuft(ctx: Ctx):
    if ctx.face == "bottom" or ctx.lface == "bottom":
        return None
    if ctx.face != "top" and ctx.t == ctx.h - 1 and hash01(ctx.s, ctx.face, "bt") > 0.5:
        return None
    return hoglin._hair(ctx, 0.34, key="mane", seed=46, length=2.0)


def _nub(ctx: Ctx):
    if _rotten(ctx):
        return pick(ctx.pal["bone"], 0.6)
    return pick(ctx.pal["tusk"], 0.7 - rel_y(ctx) * 0.2)


def _ear(ctx: Ctx):
    if ctx.lface == "bottom":
        return pick(ctx.pal["snout"], 0.42)
    return hoglin._hair(ctx, 0.44, seed=48, length=2.0)


def _crest(ctx: Ctx):
    if ctx.face == "bottom":
        return None
    if ctx.face == "top" and hash01(ctx.s, ctx.t, "bc") > 0.7:
        return None
    return hoglin._hair(ctx, 0.36, key="mane", seed=50, length=2.0)


def _spines(ctx: Ctx):
    x, y, z = ctx.world
    col = math.floor(z * 1.4)
    if col % 2 == 1 or _rotten(ctx) and hash01(col, "bb") > 0.5:
        return None                                                              # einzelne Borsten mit Lücken
    length = 0.45 + 0.55 * hash01(col, "bl")
    k = ctx.t / max(ctx.h - 1, 1)
    if k < 1.0 - length:
        return None
    tip = (k - (1.0 - length)) / max(length, 1e-3)
    if tip < 0.3:
        glow = ctx.pal.get("tip_glow")
        if glow is not None:
            ctx.glow = mix(hexc("#000000"), glow, 0.7)
        return pick(ctx.pal["tip"], 0.7)
    return pick(ctx.pal["mane"], 0.4 + (hash01(col, "bm") - 0.5) * 0.14)


def _tail(ctx: Ctx):
    return hoglin._hair(ctx, 0.38, seed=52)


def _leg(ctx: Ctx):
    face, t = ctx.face, ctx.t
    if face == "bottom" or (face != "top" and t >= ctx.h - 1):
        return pick(ctx.pal["hoof"], 0.5 + hoglin._lit(ctx))
    return hoglin._hair(ctx, 0.38 - t * 0.02, seed=54, length=2.0)


def materials(variant: str) -> dict:
    return {"trunk": _trunk, "skull": _skull, "muzzle": _muzzle, "disk": _disk, "tuft": _tuft, "nub": _nub, "ear": _ear,
            "crest": _crest, "spines": _spines, "tail": _tail, "leg": _leg}


def hidden(variant: str) -> list[str]:
    return []
