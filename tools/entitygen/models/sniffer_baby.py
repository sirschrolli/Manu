"""
Junger Urwühler (statt Schnüffler-Junges) auf dem Skelett des Vanilla-Schnüfflerjungen: gleiche Knochen und Drehpunkte
wie SniffletModel (bone -> body -> head -> Ohren, nose, lower_beak; bone -> sechs Beine), damit alle Bewegungen des
Schnüfflers greifen. Ein weiches Fellknäuel mit großen, glänzenden Augen, Stirnbüschel, Schlappohren, kleiner
Rüsselplatte und einem ersten Farnsprössling auf dem Rücken; Farben wie bei den Großen, nur heller und flauschiger.
"""
from __future__ import annotations

import math

from ..geometry import Model
from ..paint import Ctx, hash01, noise3, pick, rel_y
from . import sniffer

PALETTES = sniffer.PALETTES
LOOKS = sniffer.LOOKS


def build() -> Model:
    m = Model("sniffer_baby", 128, 128)
    m.bone("bone", pivot=(0, 24, 0))
    m.bone("body", "bone", pivot=(6, -3, -9.5))
    m.cube("body", (-13, -14, -0.5), (14, 15, 20), "body")
    m.cube("body", (-13, -14, -0.5), (14, 13, 20), "fluff", inflate=0.5)
    m.cube("body", (-7.5, -16.5, 8.5), (3, 2.5, 0), "sprout")
    m.cube("body", (-6, -16.5, 7), (0, 2.5, 3), "sprout")

    m.bone("head", "body", pivot=(-6, -4.75, 0))
    m.cube("head", (-5, -4.25, -7.5), (10, 9, 9), "head")
    m.cube("head", (-5.4, -4.9, -8.1), (10.8, 2.6, 2.4), "forelock")
    m.cube("head", (-2.0, -6.4, -6.0), (4, 2.2, 4), "tuft")                       # Stirnbüschel
    m.bone("left_ear", "head", pivot=(5, -4.25, -1.5))
    m.cube("left_ear", (0, 0, -2), (1, 11, 3), "ear")
    m.bone("right_ear", "head", pivot=(-5, -4.25, -1.5))
    m.cube("right_ear", (-1, 0, -2), (1, 11, 3), "ear")
    m.bone("nose", "head", pivot=(0, -1.25, -9.5))
    m.cube("nose", (-5, -3, -2), (10, 3, 4), "nose")
    m.bone("lower_beak", "head", pivot=(0, 1.25, -9.5))
    m.cube("lower_beak", (-5, -2.5, -2), (10, 5, 4), "snout")

    for side, x in (("right", -4.0), ("left", 4.0)):
        for name, z in (("front", -7.0), ("mid", 0.0), ("hind", 7.0)):
            leg = f"{side}_{name}_leg"
            m.bone(leg, "bone", pivot=(x, -4, z))
            m.cube(leg, (-2, -1, -2), (4, 5, 4), "leg")
    return m


def _soft(ctx: Ctx, base: float, seed: int):
    """Weicher Flaum: ruhige Fläche mit kurzen, feinen Strähnen, nach unten dunkler."""
    x, y, z = ctx.world
    face = ctx.face
    v = base + sniffer._LIT[face]
    v += (noise3(x * 0.12, y * 0.12, z * 0.12, seed) - 0.5) * 0.06
    if face in ("top", "bottom"):
        v += sniffer._streak(z, abs(x - 6.0), seed + 5, 2.0) * 0.7
    else:
        v += sniffer._streak(x if face in ("front", "back") else z, y, seed, 2.0) * 0.8
    return pick(ctx.pal["fur"], v)


def _fluff(ctx: Ctx):
    """Flaumiges Fell: dicht, nach unten in kurzen, spitzen Büscheln ausgefranst, heller als bei den Großen."""
    if ctx.face == "bottom":
        return None
    if ctx.face == "top":
        return _soft(ctx, 0.7, 41)
    r = sniffer._ragged(ctx, 2.0, 0.7, 1.0, "fluff")
    if r is None:
        return None
    if r == "tip":
        return pick(ctx.pal["tip"], 0.6)
    return _soft(ctx, 0.68 - rel_y(ctx) * 0.14, 41)


def _body(ctx: Ctx):
    if ctx.face == "bottom":
        return pick(ctx.pal["fur"], 0.34)
    return _soft(ctx, 0.56 - rel_y(ctx) * 0.12, 43)


def _head(ctx: Ctx):
    """Kopf mit großen, glänzenden Augen seitlich vorn (zwei mal zwei Pixel, Glanzpunkt, heller Ring)."""
    face, s, t = ctx.face, ctx.s, ctx.t
    if face in ("west", "east"):
        col = ctx.w - 1 - s if face == "west" else s                      # 0 = vorderste Spalte
        if col in (1, 2) and t in (2, 3):
            if t == 2 and col == 2:
                return pick(ctx.pal["eye"], 0.95)                         # Glanzpunkt
            return pick(ctx.pal["eye"], 0.15)
        if col in (0, 1, 2, 3) and t in (1, 2, 3, 4):
            return pick(ctx.pal["tip"], 0.7 if t in (1, 4) or col in (0, 3) else 0.55)
    return _soft(ctx, 0.66 - rel_y(ctx) * 0.1 if face != "top" else 0.72, 47)


def _tuft(ctx: Ctx):
    """Stirnbüschel: ein paar helle, aufgestellte Haare."""
    face, s, t = ctx.face, ctx.s, ctx.t
    if face == "bottom":
        return None
    if face == "top":
        return pick(ctx.pal["tip"], 0.75) if hash01(s, t, "tuft") > 0.3 else None
    if t == 0 and hash01(s, face, "tuft") < 0.5:
        return None
    return pick(ctx.pal["tip"], 0.62 - t * 0.12)


def _sprout(ctx: Ctx):
    if ctx.face not in ("front", "back", "west", "east"):
        return None
    lx, ly, lz = ctx.local
    ox, oy, oz = ctx.cube.origin
    w, h, d = ctx.cube.size
    u = ((lx - ox) / w if w > 0 else (lz - oz) / d) - 0.5
    t = (ly - oy) / h
    if abs(u) < 0.17:
        return pick(ctx.pal["fern"], 0.5)
    if t < 0.5 and abs(u) < 0.45:
        return pick(ctx.pal["fern"], 0.7 - abs(u))                       # zwei junge Blätter
    return None


def materials(variant: str) -> dict:
    base = sniffer.materials(variant)
    return {
        "body": _body, "fluff": _fluff, "sprout": _sprout, "head": _head, "tuft": _tuft, "forelock": base["forelock"],
        "ear": base["ear"], "nose": base["nose"], "snout": base["snout"], "leg": base["leg"],
    }


def hidden(variant: str) -> list[str]:
    return []
