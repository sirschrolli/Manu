"""
Hausschwein: langer, tiefer Körper auf kurzen Beinen, großer Kopf mit Rüssel und Schlappohren über den Augen,
Ringelschwanz. Mit Sattel reitbar (der Sattel ist Teil des Modells). Vier Rassen:

  * Landschwein  – rosig mit hellen Borsten
  * Sattelschwein – schwarz mit weißem Gürtel über Schultern und Vorderbeinen
  * Buntes Schwein – rosig mit großen schwarzen Platten (Wälder, warmes Land)
  * Wollschwein  – dichtes, krauses blondes bis rötliches Fell (kalte Länder)

Knochen-Namen werden in PigModel (Java) verwendet; der Knochen "saddle" ist nur gesattelt sichtbar.
"""
from __future__ import annotations

import math

from ..geometry import Model
from ..paint import Ctx, eye_painter, hash01, mix, noise3, pick, ramp, rel_y
from . import farm

BREEDS = ["landschwein", "sattelschwein", "bunt", "wollschwein"]

_COMMON = {
    "eye": ramp("#100a08", "#4a3022"), "hoof": ramp("#2a201c", "#7a6a60"),
    "leather": ramp("#3a2010", "#a8683a"), "cloth": ramp("#4a0e0e", "#c83a2e"), "iron": ramp("#3a3a40", "#c8c8d0"),
}

PALETTES = {
    "landschwein": {**_COMMON, "fur": ramp("#b8706a", "#ffd2c8"), "belly": ramp("#c88a84", "#ffe2da"),
                    "snout": ramp("#9a4a48", "#f0a49a"), "patch": ramp("#b8706a", "#ffd2c8"), "band": ramp("#b8706a", "#ffd2c8")},
    "sattelschwein": {**_COMMON, "fur": ramp("#141012", "#4a4446"), "belly": ramp("#1a1618", "#504a4c"),
                      "snout": ramp("#2a2426", "#7a6e6e"), "patch": ramp("#141012", "#4a4446"), "band": ramp("#c8bcb4", "#fffaf2")},
    "bunt": {**_COMMON, "fur": ramp("#b87a70", "#ffdcd0"), "belly": ramp("#c8928a", "#ffe8e0"),
             "snout": ramp("#9a5450", "#f0aca2"), "patch": ramp("#141012", "#3e3638"), "band": ramp("#b87a70", "#ffdcd0")},
    "wollschwein": {**_COMMON, "fur": ramp("#8a5a2a", "#f8dca0"), "belly": ramp("#7a4e26", "#e0c08a"),
                    "snout": ramp("#3a2420", "#8a6460"), "patch": ramp("#8a5a2a", "#f8dca0"), "band": ramp("#8a5a2a", "#f8dca0")},
}
for _p in PALETTES.values():
    _p["back"] = _p["fur"]
    _p["wool"] = _p["fur"]


def build() -> Model:
    m = Model("pig", 128, 64)
    m.bone("body", pivot=(0, 13, 0))
    m.cube("body", (-5, -4.5, -8), (10, 9, 16), "fur")
    m.cube("body", (-4, -5.5, -6.5), (8, 1, 12), "fur")                  # gewölbter Rücken

    m.bone("head", "body", pivot=(0, -1, -8))
    m.cube("head", (-4, -4, -6.5), (8, 7.5, 6.5), "face")
    m.cube("head", (-2.5, -0.5, -9.5), (5, 3.5, 3), "snout")              # Rüssel
    m.cube("head", (4, -2, -5.5), (0.5, 1, 1), "eye")
    m.cube("head", (-4.5, -2, -5.5), (0.5, 1, 1), "eye")
    for side in (1, -1):
        sfx = "l" if side > 0 else "r"
        # Schlappohren: hängen nach vorne über die Augen
        m.bone("ear_" + sfx, "head", pivot=(2.4 * side, -4, -2.5), rotation=(38, -12 * side, 22 * side))
        m.cube("ear_" + sfx, (-1.75, -0.5, -4.5), (3.5, 1, 4.5), "ear")

    for name, x, z in (("front_left_leg", 2.8, -5.5), ("front_right_leg", -2.8, -5.5),
                       ("hind_left_leg", 2.8, 5.5), ("hind_right_leg", -2.8, 5.5)):
        m.bone(name, pivot=(x, 17.5, z))
        m.cube(name, (-1.5, 0, -1.5), (3, 6.5, 3), "leg")

    # Ringelschwanz
    m.bone("tail", "body", pivot=(0, -3, 8), rotation=(-30, 0, 0))
    m.cube("tail", (-0.5, -1.5, 0), (1, 1.5, 1), "tail")
    m.cube("tail", (-0.5, -2.5, 0.8), (1, 1, 1), "tail")
    m.cube("tail", (0.3, -2.0, 1.4), (1, 1, 1), "tail")

    # Sattel mit Satteldecke und Steigbügeln
    m.bone("saddle", "body", pivot=(0, -5.5, 0))
    m.cube("saddle", (-4.5, -0.6, -3.5), (9, 1, 7), "cloth")
    m.cube("saddle", (-3.5, -1.6, -2.5), (7, 1, 5), "leather")
    m.cube("saddle", (-2, -2.6, -3), (4, 1, 1), "leather")                 # Sattelknauf vorne
    m.cube("saddle", (-2.5, -2.4, 2), (5, 1, 1), "leather")                # Hinterzwiesel
    for side in (1, -1):
        m.cube("saddle", (4.6 if side > 0 else -5.1, 0.4, -2.5), (0.5, 4.5, 5), "cloth")
        m.cube("saddle", (4.9 if side > 0 else -5.4, 4.5, -0.5), (0.5, 1, 1), "iron")
    return m


def _variant(ctx: Ctx) -> str:
    return str(ctx.params.get("variant", "landschwein"))


def _coat(variant: str):
    if variant == "wollschwein":
        return farm.wool("wool", seed=41)
    base = farm.fur("fur", "belly", seed=42, contrast=0.16, top_dark=0.06, belly_from=0.7)
    if variant == "bunt":
        return farm.patched(base, "patch", scale=0.2, threshold=0.6, seed=43)
    if variant == "sattelschwein":
        def paint(ctx: Ctx):
            x, y, z = ctx.world
            # weißer Gürtel über Schultern und Vorderbeine (mit leicht welligem Rand)
            edge = math.sin(y * 0.9) * 0.6
            if -7.5 + edge < z < -2.5 + edge and ctx.cube.bone.name in ("body", "front_left_leg", "front_right_leg"):
                return pick(ctx.pal["band"], 0.55 + (hash01(ctx.u, ctx.v) - 0.5) * 0.12)
            return base(ctx)
        return paint
    return base


def _face(variant: str):
    if variant == "wollschwein":
        return farm.wool("wool", seed=44)
    return farm.fur("fur", None, seed=45, contrast=0.14, top_dark=0.04)


def _snout(variant: str):
    def paint(ctx: Ctx):
        if ctx.face == "front":
            v = 0.62 if ctx.s not in (0, ctx.w - 1) and ctx.t not in (0, ctx.h - 1) else 0.48
            if ctx.t == ctx.h // 2 and ctx.s in (1, ctx.w - 2):
                v = 0.2                                        # Nasenlöcher
            return pick(ctx.pal["snout"], v)
        return pick(ctx.pal["snout"], 0.5 + hash01(ctx.u, ctx.v) * 0.12)

    return paint


def _ear(variant: str):
    def paint(ctx: Ctx):
        if variant == "wollschwein":
            return farm.wool("wool", seed=46)(ctx)
        if ctx.face == "bottom":
            return pick(ctx.pal["snout"], 0.6)                  # rosiges Innenohr
        key = "fur"
        if variant == "bunt" and hash01(5) > 0.0 and ctx.s < ctx.w // 2:
            key = "patch"
        return pick(ctx.pal[key], 0.5 + hash01(ctx.u, ctx.v) * 0.1)

    return paint


def _tail(variant: str):
    def paint(ctx: Ctx):
        return pick(ctx.pal["fur"], 0.45 + hash01(ctx.u, ctx.v) * 0.15)

    return paint


def _cloth(ctx: Ctx):
    v = 0.5 + (hash01(ctx.u, ctx.v) - 0.5) * 0.12
    if ctx.s == 0 or ctx.s == ctx.w - 1 or ctx.t == ctx.h - 1:
        v = 0.85                                                # helle Borte
    return pick(ctx.pal["cloth"], v)


def _leather(ctx: Ctx):
    v = 0.48 + (hash01(ctx.u, ctx.v) - 0.5) * 0.1 + (0.12 if ctx.face == "top" else 0.0)
    if ctx.face == "top" and (ctx.s in (0, ctx.w - 1) or ctx.t in (0, ctx.h - 1)):
        v -= 0.15                                               # Naht am Rand
    return pick(ctx.pal["leather"], v)


def materials(variant: str) -> dict:
    return {
        "fur": _coat(variant),
        "face": _face(variant),
        "snout": _snout(variant),
        "eye": eye_painter("eye", glow=False),
        "ear": _ear(variant),
        "leg": _leg(variant),
        "tail": _tail(variant),
        "cloth": _cloth,
        "leather": _leather,
        "iron": farm.solid("iron", 0.6, 0.2),
    }


def _leg(variant: str):
    coat = _coat(variant)

    def paint(ctx: Ctx):
        if ctx.face == "bottom" or rel_y(ctx) > 0.84:
            return pick(ctx.pal["hoof"], 0.35 + hash01(ctx.u, ctx.v) * 0.2)
        return coat(ctx)

    return paint


def hidden(variant: str) -> list[str]:
    return ["saddle"] if variant != "landschwein" else []
