"""
Bienen auf dem Skelett der Vanilla-Biene (Flügelschlag, Stachel, Nektar und Zorn bleiben): pelzige Brust,
gestreifter Hinterleib, große Facettenaugen, gefiederte Fühler. Zwei Arten – Honigbiene und Hummel – jede in vier
Bildern: ruhig, wütend (rote Augen), mit Nektar (Pollenhöschen), wütend mit Nektar.
"""
from __future__ import annotations

from ..geometry import Model
from ..paint import Ctx, hash01, mix, pick, ramp, rel_y
from . import farm

BREEDS = ["honig", "hummel"]

_C = {"eye": ramp("#04040a", "#3a3a4a"), "angry": ramp("#3a0404", "#e83020"), "wing": ramp("#a8c0d8", "#ffffff"),
      "vein": ramp("#6a7a8a", "#a8b8c8"), "pollen": ramp("#a86a08", "#ffd040"), "leg": ramp("#0a0806", "#3a3028"),
      "sting": ramp("#2a2420", "#8a8070")}

PALETTES = {}
for _b, _p in {
    "honig": {"fur": ramp("#6a4a10", "#e8b040"), "band": ramp("#1a1208", "#4a3418"), "fluff": ramp("#8a6a30", "#f0d090"),
              "tail": ramp("#6a4a10", "#e8b040")},
    "hummel": {"fur": ramp("#8a6a04", "#ffd830"), "band": ramp("#08080a", "#2e2e34"), "fluff": ramp("#0e0e10", "#3a3a40"),
               "tail": ramp("#b8b4ac", "#ffffff")},
}.items():
    for _s in ("", "_angry", "_nectar", "_angry_nectar"):
        PALETTES["bee_" + _b + _s] = {**_C, **_p, "back": _p["fur"]}


def build() -> Model:
    m = Model("bee", 64, 64)
    m.bone("bone", pivot=(0, 19, 0))
    m.bone("body", "bone")
    m.cube("body", (-3.5, -4, -5), (7, 7, 10), "abdomen")
    m.cube("body", (-3.8, -4.3, -5.2), (7.6, 4.0, 3.2), "thorax")              # pelziger Kragen
    m.bone("stinger", "body")
    m.cube("stinger", (0, -1, 5), (0, 1, 2), "sting")
    m.bone("left_antenna", "body", pivot=(0, -2, -5))
    m.cube("left_antenna", (1.5, -2, -3), (1, 2, 3), "antenna")
    m.bone("right_antenna", "body", pivot=(0, -2, -5))
    m.cube("right_antenna", (-2.5, -2, -3), (1, 2, 3), "antenna")
    m.bone("right_wing", "bone", pivot=(-1.5, -4, -3), rotation=(0, -15, 0))
    m.cube("right_wing", (-9, 0, 0), (9, 0, 6), "wing")
    m.bone("left_wing", "bone", pivot=(1.5, -4, -3), rotation=(0, 15, 0))
    m.cube("left_wing", (0, 0, 0), (9, 0, 6), "wing")
    for name, z in (("front_legs", -2), ("middle_legs", 0), ("back_legs", 2)):
        m.bone(name, "bone", pivot=(1.5, 3, z))
        m.cube(name, (-5, 0, 0), (7, 2, 0), "legs")
    return m


def materials(variant: str) -> dict:
    angry = "angry" in variant
    nectar = "nectar" in variant

    def abdomen(ctx: Ctx):
        lx, ly, lz = ctx.local
        # Facettenaugen vorn an den Seiten
        if ctx.face == "front":
            if ctx.t in (2, 3) and ctx.s in (0, 1, 5, 6):
                return pick(ctx.pal["angry" if angry else "eye"], 0.45 + (0.25 if (ctx.s + ctx.t) % 2 else 0.0))
            if ctx.t == 5 and 2 <= ctx.s <= 4:
                return pick(ctx.pal["band"], 0.35)                                    # Mundwerkzeuge
            return pick(ctx.pal["fluff"], 0.55 + hash01(ctx.u, ctx.v) * 0.12)
        # Querbänder am Hinterleib, hinten die helle Hummel-Spitze
        if lz > 3.2 and variant.startswith("bee_hummel"):
            return pick(ctx.pal["tail"], 0.65 + hash01(ctx.u, ctx.v) * 0.12)
        band = (lz + 0.5) % 3.0 < 1.0 and lz > -1.0
        key = "band" if band else "fur"
        v = 0.5 + (hash01(ctx.u, ctx.v) - 0.5) * 0.15
        if ctx.face == "top":
            v += 0.08
        return pick(ctx.pal[key], v)

    def thorax(ctx: Ctx):
        return farm.shaggy("fluff", seed=171, length=1.0, contrast=0.3)(ctx)

    def legs(ctx: Ctx):
        lx, ly, lz = ctx.local
        if nectar and ctx.cube.bone.name in ("middle_legs", "back_legs") and ly > 0.8 and (lx < -3.5 or lx > 0.5):
            return pick(ctx.pal["pollen"], 0.6 + hash01(ctx.u, ctx.v) * 0.2)          # Pollenhöschen
        if ctx.s % 3 == 2:
            return None
        return pick(ctx.pal["leg"], 0.4)

    def wing(ctx: Ctx):
        # durchscheinend wirkende Flügel: helle Fläche, Adern, ein paar Lücken
        if (ctx.s + ctx.t) % 5 == 0 and ctx.t > 0:
            return None
        if ctx.s % 3 == 0 or ctx.t == 0:
            return pick(ctx.pal["vein"], 0.5)
        return pick(ctx.pal["wing"], 0.7 + hash01(ctx.u, ctx.v) * 0.15)

    def antenna(ctx: Ctx):
        return pick(ctx.pal["band"], 0.3 + (0.2 if ctx.t == 0 else 0.0))

    return {
        "abdomen": abdomen,
        "thorax": thorax,
        "sting": farm.solid("sting", 0.4, 0.1),
        "antenna": antenna,
        "wing": wing,
        "legs": legs,
    }


def hidden(variant: str) -> list[str]:
    return []
