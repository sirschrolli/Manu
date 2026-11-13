"""
Papageien auf dem Skelett des Vanilla-Papageis (Fliegen, Sitzen, Tanzen und die Schulter bleiben): kräftiger Krummschnabel,
langer gestufter Schwanz, Federhaube, Augenring. Die fünf Arten entsprechen den fünf Vanilla-Farben:

  * Hellroter Ara (rot-blau)  * Hyazinthara (blau)  * Amazone (grün)  * Gelbbrustara (blau-gelb)  * Graupapagei (grau)
"""
from __future__ import annotations

import math

from ..geometry import Model
from ..paint import Ctx, hash01, mix, pick, ramp, rel_y
from . import farm

BREEDS = ["ara", "hyazinth", "amazone", "gelbbrust", "grau"]

_C = {"eye": ramp("#000000", "#3a3a3a"), "beak": ramp("#1a1a1c", "#5a5a60"), "beak_light": ramp("#8a8070", "#f8f0e0"),
      "skin": ramp("#c8c0b0", "#ffffff"), "leg": ramp("#3a3a40", "#8a8a90")}

PALETTES = {
    "parrot_ara": {**_C, "body": ramp("#7a0808", "#f03a2a"), "wing": ramp("#0a2a8a", "#3a8aff"), "wing2": ramp("#8a6a08", "#f8d830"),
                   "tail": ramp("#7a0808", "#e83a2a"), "tail2": ramp("#0a2a8a", "#4a9aff"), "beak": ramp("#1a1a1c", "#5a5a60"),
                   "beak_light": ramp("#a89a8a", "#fff8f0")},
    "parrot_hyazinth": {**_C, "body": ramp("#0a1a6a", "#3a5ae0"), "wing": ramp("#081450", "#3050c8"), "wing2": ramp("#0a1a6a", "#4a6af0"),
                        "tail": ramp("#081450", "#3050c8"), "tail2": ramp("#081450", "#2a40a0"), "skin": ramp("#a88a08", "#ffe040"),
                        "beak_light": ramp("#1a1a1c", "#4a4a50")},
    "parrot_amazone": {**_C, "body": ramp("#0a4a10", "#4ac83a"), "wing": ramp("#063808", "#3aa82a"), "wing2": ramp("#8a0a0a", "#e83a2a"),
                       "tail": ramp("#0a4a10", "#5ad84a"), "tail2": ramp("#8a8a08", "#e8e040"), "head": ramp("#8a8a08", "#f8f040"),
                       "beak_light": ramp("#8a8070", "#e8dcc8")},
    "parrot_gelbbrust": {**_C, "body": ramp("#8a6a04", "#ffd030"), "wing": ramp("#063a7a", "#30a0f0"), "wing2": ramp("#0a2a6a", "#2a7ad0"),
                         "tail": ramp("#063a7a", "#30a0f0"), "tail2": ramp("#0a2a6a", "#2060b0"), "head": ramp("#0a5a2a", "#40c070"),
                         "beak_light": ramp("#1a1a1c", "#4a4a50")},
    "parrot_grau": {**_C, "body": ramp("#3a3c42", "#a8acb4"), "wing": ramp("#2a2c32", "#8a8e96"), "wing2": ramp("#1a1c22", "#5a5e66"),
                    "tail": ramp("#6a0808", "#e02a2a"), "tail2": ramp("#5a0808", "#c02020"), "beak_light": ramp("#1a1a1c", "#4a4a50")},
}
for _p in PALETTES.values():
    _p["back"] = _p["body"]
    _p.setdefault("head", _p["body"])


def build() -> Model:
    m = Model("parrot", 64, 32)
    m.world_faces = True
    m.bone("body", pivot=(0, 16.5, -3), rotation=(28.29, 0, 0))
    m.cube("body", (-1.5, 0, -1.5), (3, 6, 3), "body")
    m.cube("body", (-1.7, 0.2, -1.9), (3.4, 3.2, 1), "chest")
    m.bone("tail", pivot=(0, 21.07, 1.16), rotation=(58.15, 0, 0))
    m.cube("tail", (-1.5, -1, -1), (3, 5, 1), "tail")
    m.cube("tail", (-1.0, 3.9, -0.9), (2, 3.5, 0.8), "tail")                 # lange Steuerfedern
    m.cube("tail", (-0.5, 7.3, -0.85), (1, 1.6, 0.7), "tail")
    for side, name in ((1, "left_wing"), (-1, "right_wing")):
        m.bone(name, pivot=(1.5 * side, 16.94, -2.76), rotation=(-40, -180, 0))
        m.cube(name, (-0.5, 0, -1.5), (1, 5, 3), "wing")
        m.cube(name, (-0.45, 4.6, -1.2), (0.9, 2, 2.2), "wing")             # Schwungfedern
    m.bone("head", pivot=(0, 15.69, -2.76))
    m.cube("head", (-1, -1.5, -1), (2, 3, 2), "head")
    m.cube("head", (-1.2, -1.3, -0.8), (2.4, 1.2, 1.4), "cheek")            # Augenring / Wangen
    m.bone("head2", "head", pivot=(0, -2, -1))
    m.cube("head2", (-1, -0.5, -2), (2, 1, 4), "head")
    m.bone("beak1", "head", pivot=(0, -0.5, -1.5))
    m.cube("beak1", (-0.5, -1, -0.5), (1, 2, 1), "beak_low")
    m.bone("beak2", "head", pivot=(0, -1.75, -2.45))
    m.cube("beak2", (-0.5, 0, -0.5), (1, 2, 1), "beak_up")
    m.cube("beak2", (-0.4, 1.8, -0.45), (0.8, 0.7, 0.8), "beak_up")         # Hakenspitze
    m.bone("feather", "head", pivot=(0, -2.15, 0.15), rotation=(-12.69, 0, 0))
    m.cube("feather", (0, -4, -2), (0, 5, 4), "crest")
    for side, name in ((1, "left_leg"), (-1, "right_leg")):
        m.bone(name, pivot=(1.0 * side, 22, -1.05), rotation=(-1.71, 0, 0))
        m.cube(name, (-0.5, 0, -0.5), (1, 2, 1), "leg")
    return m


def materials(variant: str) -> dict:
    feathers = farm.fur("body", None, seed=161, contrast=0.16, top_dark=0.0)

    def head(ctx: Ctx):
        return pick(ctx.pal["head"], 0.55 + (hash01(ctx.u, ctx.v) - 0.5) * 0.15)

    def cheek(ctx: Ctx):
        if ctx.face in ("west", "east") and ctx.t == 0 and ctx.s == 1:
            return pick(ctx.pal["eye"], 0.0)
        # Aras: nackte weiße Wangen mit Federlinien; Hyazinthara: gelber Augenring
        if variant in ("parrot_ara", "parrot_gelbbrust"):
            if ctx.s % 2 == 0 and ctx.t == 1:
                return pick(ctx.pal["eye"], 0.2)
            return pick(ctx.pal["skin"], 0.75)
        if variant == "parrot_hyazinth" and ctx.face in ("west", "east"):
            return pick(ctx.pal["skin"], 0.7)
        return head(ctx)

    def wing(ctx: Ctx):
        lx, ly, lz = ctx.local
        rows = (ly * 1.4) % 2.0 < 0.6
        key = "wing" if ly < 3.0 else "wing2"
        v = 0.5 + (0.15 if rows else 0.0) + (hash01(ctx.u, ctx.v) - 0.5) * 0.1
        return pick(ctx.pal[key], v)

    def tail(ctx: Ctx):
        lx, ly, lz = ctx.local
        key = "tail" if ly < 3.5 else "tail2"
        v = 0.5 + (0.12 if int(lx * 2) % 2 == 0 else -0.04) + (hash01(ctx.u, ctx.v) - 0.5) * 0.08
        return pick(ctx.pal[key], v)

    def chest(ctx: Ctx):
        if variant == "parrot_ara":
            return pick(ctx.pal["body"], 0.55)
        return mix(feathers(ctx), pick(ctx.pal["body"], 0.8), 0.4)

    def crest(ctx: Ctx):
        if variant == "parrot_amazone":
            return pick(ctx.pal["head"], 0.6) if hash01(ctx.u, ctx.v, 2) > 0.4 else None
        return pick(ctx.pal["head"], 0.45) if (hash01(ctx.u, ctx.v, 2) > 0.65 and ctx.t > 2) else None

    return {
        "body": feathers,
        "chest": chest,
        "tail": tail,
        "wing": wing,
        "head": head,
        "cheek": cheek,
        "beak_low": farm.solid("beak", 0.45, 0.1),
        "beak_up": farm.solid("beak_light", 0.6, 0.12),
        "crest": crest,
        "leg": farm.solid("leg", 0.5, 0.15),
    }


def hidden(variant: str) -> list[str]:
    return []
