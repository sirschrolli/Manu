"""
Kamele auf dem Skelett des Vanilla-Kamels (Sitzen, Aufstehen, Sprint und Vanilla-Sattel bleiben): zotteliger Hals und
Höcker, Schwielen an den Knien, Wimpern, Quastenschwanz. Arten: Dromedar (sandfarben, ein Höcker, Wüste) und
Trampeltier (dunkelbraun, zwei Höcker mit Wollmähne, Tafelberge und Steppen).
"""
from __future__ import annotations

from ..geometry import Model
from ..paint import Ctx, hash01, mix, pick, ramp, rel_y
from . import farm, wild

BREEDS = ["dromedar", "trampeltier"]

_C = {"eye": ramp("#000000", "#3a2a1a"), "hoof": ramp("#2a2420", "#7a6a5e"), "nose": ramp("#3a2a20", "#8a6a54")}

PALETTES = {
    "camel_dromedar": {**_C, "fur": ramp("#8a6a3a", "#f0d098"), "belly": ramp("#b8a07a", "#fbecc8"), "dark": ramp("#6a4e2a", "#c8a06a"),
                       "shag": ramp("#7a5a30", "#e0b878")},
    "camel_trampeltier": {**_C, "fur": ramp("#4a3018", "#b08050"), "belly": ramp("#6a4a2a", "#c09868"), "dark": ramp("#2a1a0c", "#6a4626"),
                          "shag": ramp("#3a2410", "#9a6a3a")},
}
for _p in PALETTES.values():
    _p["back"] = _p["fur"]


def build() -> Model:
    m = Model("camel", 128, 128)
    m.bone("body", pivot=(0, 4, 9.5))
    m.cube("body", (-7.5, -12, -23.5), (15, 12, 27), "coat")
    m.bone("hump", "body", pivot=(0, -12, -10))
    m.cube("hump", (-4.5, -5, -5.5), (9, 5, 11), "hump")
    m.bone("hump2", "body", pivot=(0, -12, -10))                            # zweiter Höcker des Trampeltiers
    m.cube("hump2", (-4.0, -5.5, -11.5), (8, 5.5, 6), "hump")
    m.cube("hump2", (-4.0, -5.5, 2.5), (8, 5.5, 6), "hump")
    m.bone("tail", "body", pivot=(0, -9, 3.5))
    m.cube("tail", (-1.5, 0, 0), (3, 14, 0), "tail")
    m.bone("head", "body", pivot=(0, -3, -19.5))
    m.cube("head", (-3.5, -7, -15), (7, 8, 19), "neck")
    m.cube("head", (-3.5, -21, -15), (7, 14, 7), "neck_up")
    m.cube("head", (-2.5, -21, -21), (5, 5, 6), "face")
    m.cube("head", (-3.9, -12, -15.3), (7.8, 6, 6.6), "shag")               # Halsmähne
    m.bone("left_ear", "head", pivot=(2.5, -21, -9.5))
    m.cube("left_ear", (-0.5, 0.5, -1), (3, 1, 2), "ear")
    m.bone("right_ear", "head", pivot=(-2.5, -21, -9.5))
    m.cube("right_ear", (-2.5, 0.5, -1), (3, 1, 2), "ear")
    for name, x, z in (("left_hind_leg", 4.9, 9.5), ("right_hind_leg", -4.9, 9.5), ("left_front_leg", 4.9, -10.5),
                       ("right_front_leg", -4.9, -10.5)):
        m.bone(name, pivot=(x, 1, z))
        m.cube(name, (-2.5, 2, -2.5), (5, 21, 5), "leg")
    return m


def materials(variant: str) -> dict:
    coat = farm.fur("fur", "belly", seed=191, contrast=0.16, belly_from=0.8, top_dark=0.04)
    shag = farm.shaggy("shag", seed=193, length=1.6, contrast=0.28)

    def face(ctx: Ctx):
        if ctx.face in ("west", "east") and ctx.t == 1 and ctx.s in (3, 2):
            return pick(ctx.pal["eye"], 0.05 if ctx.s == 3 else 0.3)
        if ctx.face == "front" and ctx.t == 3 and ctx.s in (1, 3):
            return pick(ctx.pal["nose"], 0.2)                                         # Nüstern
        if ctx.face == "front" and ctx.t == 4:
            return pick(ctx.pal["nose"], 0.45)
        return coat(ctx)

    def leg(ctx: Ctx):
        ry = rel_y(ctx)
        if ctx.face == "bottom" or ry > 0.92:
            return pick(ctx.pal["hoof"], 0.4 + hash01(ctx.u, ctx.v) * 0.15)
        if ctx.face == "front" and 0.45 < ry < 0.55:
            return pick(ctx.pal["dark"], 0.3)                                          # Kniestwiele
        return coat(ctx)

    def tail(ctx: Ctx):
        if ctx.t > ctx.h - 4:
            return pick(ctx.pal["dark"], 0.3 + hash01(ctx.u, ctx.v) * 0.2) if ctx.s % 2 == 0 or ctx.t < ctx.h - 1 else None
        return pick(ctx.pal["fur"], 0.45) if ctx.s == 1 else None

    def hump(ctx: Ctx):
        if variant == "camel_trampeltier" and ctx.face == "top":
            return shag(ctx)
        return coat(ctx)

    return {
        "coat": coat,
        "hump": hump,
        "tail": tail,
        "neck": coat,
        "neck_up": coat,
        "face": face,
        "shag": shag,
        "ear": farm.solid("dark", 0.45, 0.1),
        "leg": leg,
    }


def hidden(variant: str) -> list[str]:
    return ["hump"] if variant == "camel_trampeltier" else ["hump2"]
