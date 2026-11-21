"""
Lamas auf dem Skelett des Vanilla-Lamas (Teppiche, Packtaschen und alle Bewegungen bleiben): dichtes Vlies an Hals und
Rumpf, Bananenohren, gespaltene Oberlippe, Zehen. Die Farben entsprechen den Vanilla-Lamas (creme, weiß, braun,
grau); dazu der Wanderhändler mit eigenem Lama.
"""
from __future__ import annotations

from ..geometry import Model
from ..paint import Ctx, hash01, mix, pick, ramp, rel_y
from . import farm

BREEDS = ["creme", "weiss", "braun", "grau"]

_C = {"eye": ramp("#000000", "#3a2a1a"), "hoof": ramp("#1a1612", "#5a5048"), "lip": ramp("#3a2a26", "#8a6a60")}

PALETTES = {
    "llama_creme": {**_C, "fur": ramp("#a88a5a", "#f8e8c0"), "dark": ramp("#7a5a30", "#c8a46a")},
    "llama_weiss": {**_C, "fur": ramp("#b8b4ac", "#ffffff"), "dark": ramp("#8a867e", "#d8d4cc")},
    "llama_braun": {**_C, "fur": ramp("#4a2e18", "#b07a48"), "dark": ramp("#2a1a0c", "#6a4626")},
    "llama_grau": {**_C, "fur": ramp("#4a4846", "#a8a49c"), "dark": ramp("#2a2826", "#6a6660")},
}
for _p in PALETTES.values():
    _p["back"] = _p["fur"]
    _p["belly"] = _p["fur"]


def build() -> Model:
    m = Model("llama", 128, 64)
    m.bone("head", pivot=(0, 7, -6))
    m.cube("head", (-2, -14, -10), (4, 4, 9), "face")
    m.cube("head", (-4, -16, -6), (8, 18, 6), "neck")
    m.cube("head", (-4.4, -9, -6.4), (8.8, 11, 6.8), "fleece")               # Halsvlies
    m.bone("ear_l", "head", pivot=(2.5, -16, -3), rotation=(-6, 0, 10))
    m.cube("ear_l", (-1.5, -3.5, -1), (3, 3.5, 2), "ear")
    m.bone("ear_r", "head", pivot=(-2.5, -16, -3), rotation=(-6, 0, -10))
    m.cube("ear_r", (-1.5, -3.5, -1), (3, 3.5, 2), "ear")
    m.bone("body", pivot=(0, 5, 2), rotation=(90, 0, 0))
    m.cube("body", (-6, -10, -7), (12, 18, 10), "fleece")
    m.cube("body", (-1.5, 7.5, -6), (3, 2.5, 3), "fleece")                    # kurzer Schwanz
    m.bone("right_chest", pivot=(-8.5, 3, 3), rotation=(0, 90, 0))
    m.cube("right_chest", (-3, 0, 0), (8, 8, 3), "chest")
    m.bone("left_chest", pivot=(5.5, 3, 3), rotation=(0, 90, 0))
    m.cube("left_chest", (-3, 0, 0), (8, 8, 3), "chest")
    for name, x, z in (("right_hind_leg", -3.5, 6), ("left_hind_leg", 3.5, 6), ("right_front_leg", -3.5, -5), ("left_front_leg", 3.5, -5)):
        m.bone(name, pivot=(x, 10, z))
        m.cube(name, (-2, 0, -2), (4, 14, 4), "leg")
        m.cube(name, (-2.4, -0.4, -2.4), (4.8, 6, 4.8), "fleece")             # Wollhosen
    return m


def materials(variant: str) -> dict:
    fleece = farm.wool("fur", seed=201)

    def face(ctx: Ctx):
        if ctx.face in ("west", "east") and ctx.t == 1 and ctx.s in (6, 7):
            return pick(ctx.pal["eye"], 0.05 if ctx.s == 7 else 0.35)
        if ctx.face == "front":
            if ctx.t == 2 and ctx.s == 1:
                return pick(ctx.pal["lip"], 0.2)                                      # gespaltene Lippe
            return pick(ctx.pal["fur"], 0.6)
        return farm.fur("fur", None, seed=203, contrast=0.12, top_dark=0.0)(ctx)

    def leg(ctx: Ctx):
        if ctx.face == "bottom" or rel_y(ctx) > 0.92:
            return pick(ctx.pal["hoof"], 0.4)
        return farm.fur("fur", None, seed=205, contrast=0.14, top_dark=0.0)(ctx)

    def ear(ctx: Ctx):
        if ctx.face == "front" and 0 < ctx.s < ctx.w - 1 and ctx.t > 0:
            return pick(ctx.pal["dark"], 0.45)
        return pick(ctx.pal["fur"], 0.55)

    return {
        "face": face,
        "neck": farm.fur("fur", None, seed=207, contrast=0.12, top_dark=0.0),
        "fleece": fleece,
        "ear": ear,
        "chest": lambda ctx: pick(ctx.pal["dark"], 0.4 + (0.1 if ctx.t % 3 == 0 else 0.0)),
        "leg": leg,
    }


def hidden(variant: str) -> list[str]:
    return []
