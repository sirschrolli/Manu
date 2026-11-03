"""
Fledermäuse auf dem Skelett der Vanilla-Fledermaus (Flug- und Ruhe-Bewegungen bleiben): Flughäute mit Fingerknochen,
große Ohren, Krallenfüße. Arten: Abendsegler (rotbraun), Mausohr (graubraun), Vampir (dunkel, rote Augen).
"""
from __future__ import annotations

from ..geometry import Model
from ..paint import Ctx, hash01, pick, ramp
from . import farm, wild

BREEDS = ["abendsegler", "mausohr", "vampir"]

_C = {"eye": ramp("#000000", "#3a3a3a"), "claw": ramp("#0a0806", "#3a3028")}

PALETTES = {
    "bat_abendsegler": {**_C, "fur": ramp("#3a1e0e", "#a8683a"), "wing": ramp("#1e120c", "#5a3e30"), "dark": ramp("#120a06", "#3a2618"),
                        "ear": ramp("#2a1a12", "#6a4a3a")},
    "bat_mausohr": {**_C, "fur": ramp("#3a342c", "#a89a88"), "wing": ramp("#1e1a18", "#5a524c"), "dark": ramp("#12100e", "#3a3430"),
                    "ear": ramp("#3a2e2a", "#8a6e64")},
    "bat_vampir": {**_C, "fur": ramp("#141012", "#4a3a40"), "wing": ramp("#0e0a0c", "#3a2a30"), "dark": ramp("#060406", "#241a1e"),
                   "ear": ramp("#1e1418", "#5a3a44"), "eye": ramp("#4a0404", "#ff3020")},
}
for _p in PALETTES.values():
    _p["back"] = _p["fur"]


def build() -> Model:
    m = Model("bat", 64, 32)
    m.bone("body", pivot=(0, 17, 0))
    m.cube("body", (-1.5, 0, -1), (3, 5, 2), "body")
    m.cube("body", (-1.8, 0.2, -1.3), (3.6, 2.4, 2.6), "fluff")                # pelziger Kragen
    m.bone("head", pivot=(0, 17, 0))
    m.cube("head", (-2, -3, -1), (4, 3, 2), "head")
    m.cube("head", (-0.75, -1.5, -1.6), (1.5, 1.2, 0.6), "snout")
    m.bone("right_ear", "head", pivot=(-1.5, -2, 0))
    m.cube("right_ear", (-2.5, -4, 0), (3, 5, 0), "ear")
    m.bone("left_ear", "head", pivot=(1.1, -3, 0))
    m.cube("left_ear", (-0.1, -3, 0), (3, 5, 0), "ear")
    m.bone("right_wing", "body", pivot=(-1.5, 0, 0))
    m.cube("right_wing", (-2, -2, 0), (2, 7, 0), "wing")
    m.bone("right_wing_tip", "right_wing", pivot=(-2, 0, 0))
    m.cube("right_wing_tip", (-6, -2, 0), (6, 8, 0), "wing")
    m.bone("left_wing", "body", pivot=(1.5, 0, 0))
    m.cube("left_wing", (0, -2, 0), (2, 7, 0), "wing")
    m.bone("left_wing_tip", "left_wing", pivot=(2, 0, 0))
    m.cube("left_wing_tip", (0, -2, 0), (6, 8, 0), "wing")
    m.bone("feet", "body", pivot=(0, 5, 0))
    m.cube("feet", (-1.5, 0, 0), (3, 2, 0), "feet")
    return m


def materials(variant: str) -> dict:
    fur = farm.fur("fur", None, seed=181, contrast=0.2, top_dark=0.0)

    def head(ctx: Ctx):
        if ctx.face == "front" and ctx.t == 1 and ctx.s in (0, 3):
            return pick(ctx.pal["eye"], 0.6 if variant == "bat_vampir" else 0.05)
        return fur(ctx)

    def ear(ctx: Ctx):
        if ctx.t > 1 and 0 < ctx.s < ctx.w - 1:
            return pick(ctx.pal["ear"], 0.6)
        return pick(ctx.pal["ear"], 0.35)

    def wing(ctx: Ctx):
        # Fingerknochen als dunkle Adern, am Rand ausgefranst
        if ctx.t == ctx.h - 1 and ctx.s % 2 == 1:
            return None
        if ctx.s % 3 == 0:
            return pick(ctx.pal["dark"], 0.45)
        return pick(ctx.pal["wing"], 0.5 + (hash01(ctx.u, ctx.v) - 0.5) * 0.12)

    def feet(ctx: Ctx):
        return pick(ctx.pal["claw"], 0.4) if ctx.s != 1 else None

    return {
        "body": fur,
        "fluff": farm.shaggy("fur", seed=183, length=1.0, contrast=0.25),
        "head": head,
        "snout": farm.solid("ear", 0.45, 0.1),
        "ear": ear,
        "wing": wing,
        "feet": feet,
    }


def hidden(variant: str) -> list[str]:
    return []
