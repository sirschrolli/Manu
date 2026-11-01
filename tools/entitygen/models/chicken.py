"""
Huhn: rundlicher Leib, kleiner Kopf mit Kamm und Kehllappen, Schwanzfedern, Flügel (schlagen beim Flattern),
gelbe Beine. Hähne tragen einen großen Kamm und lange, gebogene Sichelfedern. Rassen:

  * Weiße Henne     – weiß mit rotem Kamm (legt weiße Eier)
  * Braune Henne    – rotbraun mit dunkleren Schwanzfedern (legt braune Eier)
  * Schwarze Henne  – schwarz mit grünem Schimmer
  * Sperberhenne    – grau-weiß gesperbert (Querstreifen), legt blaue Eier (kalte Länder)
  * Hahn            – goldrot mit grünschwarzen Sichelfedern

Knochen-Namen werden in ChickenModel (Java) verwendet; "sickle" (Sichelfedern) und "comb_big" nur beim Hahn.
"""
from __future__ import annotations

import math

from ..geometry import Model
from ..paint import Ctx, eye_painter, hash01, mix, pick, ramp, rel_y
from . import farm

BREEDS = ["white", "brown", "black", "barred", "rooster"]

_COMMON = {"eye": ramp("#1a0e04", "#c8781a"), "comb": ramp("#7a0e0e", "#f04a3a"), "beak": ramp("#8a6420", "#f4d27a"),
           "leg": ramp("#8a6420", "#f4c860")}

PALETTES = {
    "chicken_white": {**_COMMON, "feather": ramp("#cfc8bc", "#ffffff"), "dark": ramp("#b4ac9e", "#f4f0e8"),
                      "tail": ramp("#c4bcb0", "#ffffff"), "neck": ramp("#d8d2c8", "#ffffff")},
    "chicken_brown": {**_COMMON, "feather": ramp("#5a2810", "#d88a4a"), "dark": ramp("#2a120a", "#7a3a1a"),
                      "tail": ramp("#1a1210", "#5a3a28"), "neck": ramp("#7a3a14", "#f0a860")},
    "chicken_black": {**_COMMON, "feather": ramp("#0a0c10", "#3a4a4a"), "dark": ramp("#06080a", "#22302c"),
                      "tail": ramp("#060a08", "#2a4a3a"), "neck": ramp("#0a0c10", "#3e5048")},
    "chicken_barred": {**_COMMON, "feather": ramp("#8a8c94", "#f4f4f8"), "dark": ramp("#3a3c44", "#7a7c84"),
                       "tail": ramp("#5a5c64", "#e0e0e8"), "neck": ramp("#9a9ca4", "#f8f8fa")},
    "chicken_rooster": {**_COMMON, "feather": ramp("#5a1a08", "#e8823a"), "dark": ramp("#1a0a06", "#6a2a10"),
                        "tail": ramp("#04100c", "#2a6a4a"), "neck": ramp("#8a4a10", "#ffcc5a")},
}
for _p in PALETTES.values():
    _p["back"] = _p["feather"]


def build() -> Model:
    m = Model("chicken", 64, 64)
    m.bone("body", pivot=(0, 16, 0))
    m.cube("body", (-3, -3.5, -4), (6, 6, 8), "feather")
    m.cube("body", (-2.5, -1, -5), (5, 4, 2), "breast")                  # Brust

    m.bone("head", "body", pivot=(0, -3.5, -4))
    m.cube("head", (-2, -5, -2), (4, 5, 3.5), "neck")
    m.cube("head", (-1, -3.5, -3.6), (2, 1, 1.6), "beak")
    m.cube("head", (-0.75, -2.5, -3.0), (1.5, 1.8, 1), "wattle")         # Kehllappen
    m.cube("head", (2, -4, -1.2), (0.3, 1, 1), "eye")
    m.cube("head", (-2.3, -4, -1.2), (0.3, 1, 1), "eye")
    m.bone("comb", "head", pivot=(0, -5, -0.5))
    m.cube("comb", (-0.5, -1.5, -1.5), (1, 1.5, 3), "comb")
    m.bone("comb_big", "head", pivot=(0, -5, -0.5))
    m.cube("comb_big", (-0.5, -2.8, -2), (1, 2.8, 4.2), "comb")

    for side in (1, -1):
        sfx = "l" if side > 0 else "r"
        m.bone("wing_" + sfx, "body", pivot=(3 * side, -2.5, -2.5))
        m.cube("wing_" + sfx, (0 if side > 0 else -1, 0, 0), (1, 4.5, 6.5), "wing")
        m.bone("leg_" + sfx, pivot=(1.5 * side, 19, 0.5))
        m.cube("leg_" + sfx, (-0.5, 0, -0.5), (1, 5, 1), "leg")
        m.cube("leg_" + sfx, (-0.5, 4.6, -2.6), (1, 0.4, 2.1), "toe")     # Mittelzehe
        m.cube("leg_" + sfx, (-1.6, 4.6, -1.6), (1.1, 0.4, 1.1), "toe")   # Seitenzehen
        m.cube("leg_" + sfx, (0.5, 4.6, -1.6), (1.1, 0.4, 1.1), "toe")
        m.cube("leg_" + sfx, (-0.4, 4.6, 0.5), (0.8, 0.4, 0.9), "toe")    # Hinterzehe

    m.bone("tail", "body", pivot=(0, -2.5, 3.2), rotation=(-28, 0, 0))
    m.cube("tail", (-2, -4, -0.6), (4, 4.5, 1.6), "tail")              # Federfächer
    m.cube("tail", (-1.5, -5.5, -0.3), (3, 1.5, 1.1), "tail")
    m.bone("sickle", "tail", pivot=(0, -5, 0.6), rotation=(-30, 0, 0))  # Sichelfedern des Hahns: Bogen nach hinten
    m.cube("sickle", (-1.25, -4.2, -0.4), (2.5, 4.4, 0.8), "sickle")
    m.bone("sickle_tip", "sickle", pivot=(0, -4, 0), rotation=(-100, 0, 0))
    m.cube("sickle_tip", (-1.0, -4.2, -0.4), (2, 4.4, 0.8), "sickle")
    m.cube("sickle_tip", (-0.5, -5.6, -0.3), (1, 1.6, 0.6), "sickle")
    return m


def _barred(base, variant: str):
    """Sperberung: schmale, leicht gewellte Querbinden (jede zweite Texelreihe dunkel)."""

    def paint(ctx: Ctx):
        col = base(ctx)
        if variant != "chicken_barred" or ctx.face in ("top", "bottom"):
            return col
        x, y, z = ctx.world
        across = z if ctx.face in ("west", "east") else x
        row = math.floor(y + 0.5 * math.sin(across * 0.9) + 0.001)
        if row % 2 == 0 and hash01(math.floor(across), row, 5) > 0.12:
            return pick(ctx.pal["dark"], 0.45 + hash01(ctx.u, ctx.v) * 0.2)
        return col

    return paint


def materials(variant: str) -> dict:
    feather = _barred(farm.fur("feather", None, seed=71, contrast=0.2, top_dark=0.08), variant)

    def breast(ctx: Ctx):
        col = feather(ctx)
        if variant == "chicken_rooster":
            # dunkle, geschuppte Brust des Hahns
            x, y, z = ctx.world
            scale = (math.floor(x) + math.floor(y) * 2) % 3 == 0
            return pick(ctx.pal["dark"], (0.62 if scale else 0.42) + hash01(ctx.u, ctx.v) * 0.1)
        return col

    def neck(ctx: Ctx):
        # Halsbehang: hellere, schmale Federn
        x, y, z = ctx.world
        v = 0.55 + (hash01(int(x * 2), int(y), int(z * 2)) - 0.5) * 0.25
        if variant == "chicken_barred":
            return _barred(lambda c: pick(c.pal["neck"], v), variant)(ctx)
        return pick(ctx.pal["neck"], v)

    def wing(ctx: Ctx):
        x, y, z = ctx.world
        rows = (z - y * 0.4) % 2.0
        v = 0.5 + (0.18 if rows < 0.4 else 0.0) + (hash01(int(y), int(z)) - 0.5) * 0.1
        col = pick(ctx.pal["feather"], v)
        if rel_y(ctx) > 0.7:
            col = mix(col, pick(ctx.pal["dark"], 0.5), 0.6)         # dunklere Schwungfedern
        return _barred(lambda c: col, variant)(ctx)

    def tail(ctx: Ctx):
        # einzelne Steuerfedern: helle Schäfte, dunklere Ränder
        x, y, z = ctx.world
        lane = math.floor(x + 0.001)
        v = 0.58 + (0.12 if lane % 2 == 0 else -0.04) + (hash01(lane, int(y)) - 0.5) * 0.1
        if ctx.face != "top" and rel_y(ctx) < 0.2:
            v += 0.06
        return _barred(lambda c: pick(c.pal["tail"], v), variant)(ctx)

    def sickle(ctx: Ctx):
        # grünschwarz schillernde Sichelfedern mit hellem Glanzstreif
        v = 0.4 + (hash01(ctx.u, ctx.v) - 0.5) * 0.15
        if ctx.face in ("west", "east") and ctx.s % 3 == 1:
            v += 0.3
        if ctx.face == "top":
            v += 0.2
        return pick(ctx.pal["tail"], v)

    def comb(ctx: Ctx):
        v = 0.55 + (hash01(ctx.u, ctx.v) - 0.5) * 0.15
        if ctx.face in ("top",) or (ctx.face in ("west", "east") and ctx.t == 0 and ctx.s % 2 == 0):
            v += 0.15                                                 # gezackter Rand
        return pick(ctx.pal["comb"], v)

    return {
        "feather": feather,
        "breast": breast,
        "neck": neck,
        "wing": wing,
        "tail": tail,
        "sickle": sickle,
        "comb": comb,
        "wattle": comb,
        "beak": farm.solid("beak", 0.6, 0.1),
        "eye": eye_painter("eye", glow=False),
        "leg": farm.solid("leg", 0.55, 0.15),
        "toe": farm.solid("leg", 0.45, 0.15),
    }


def hidden(variant: str) -> list[str]:
    return ["comb"] if variant == "chicken_rooster" else ["comb_big", "sickle", "sickle_tip"]
