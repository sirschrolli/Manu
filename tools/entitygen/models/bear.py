"""
Bär auf dem Skelett des Vanilla-Eisbären (Aufrichten zum Angriff bleibt): massiger Körper mit Schulterbuckel,
zotteliges Fell, runde Ohren, Krallen. Rassen:

  * Eisbär    – gelblich weiß, schwarze Nase (Schnee, Eis)
  * Braunbär  – dunkelbraun mit hellerem Rücken und hohem Buckel (alte Wälder, Taiga)
  * Schwarzbär – schwarz mit hellbraunem Maul (Wälder)
"""
from __future__ import annotations

from ..geometry import Model
from ..paint import Ctx, hash01, mix, pick, ramp, rel_y
from . import farm, wild

BREEDS = ["eis", "braun", "schwarz"]

_C = {"eye": ramp("#0a0604", "#3a2a1a"), "nose": ramp("#08080a", "#38383c"), "claw": ramp("#1a1612", "#6a6058")}

PALETTES = {
    "bear_eis": {**_C, "fur": ramp("#b8b098", "#fffcec"), "belly": ramp("#c8c0a8", "#fffcf0"), "dark": ramp("#a8a088", "#e8e0c8"),
                 "muzzle": ramp("#b8b098", "#fffcec")},
    "bear_braun": {**_C, "fur": ramp("#2a1a0e", "#9a6a3e"), "belly": ramp("#3a2614", "#8a5e36"), "dark": ramp("#5a4026", "#c09a6a"),
                   "muzzle": ramp("#4a3220", "#a88058")},
    "bear_schwarz": {**_C, "fur": ramp("#0a0a0c", "#3a3a40"), "belly": ramp("#121214", "#3a3a40"), "dark": ramp("#1a1a1e", "#4a4a50"),
                     "muzzle": ramp("#6a4a2a", "#c89a6a")},
}
for _p in PALETTES.values():
    _p["back"] = _p["fur"]


def build() -> Model:
    m = Model("bear", 128, 128)
    m.world_faces = True
    m.bone("head", pivot=(0, 10, -16))
    m.cube("head", (-3.5, -3, -3), (7, 7, 7), "skull")
    m.cube("head", (-2.5, 1, -6), (5, 3, 3), "muzzle")
    m.cube("head", (-4.3, -0.5, -2.5), (0.8, 4.5, 4.5), "shag")                 # Backenfell
    m.cube("head", (3.5, -0.5, -2.5), (0.8, 4.5, 4.5), "shag")
    m.bone("right_ear", "head", pivot=(-3.5, -3, 0))
    m.cube("right_ear", (-1, -1.5, -0.5), (2, 2, 1), "ear")
    m.bone("left_ear", "head", pivot=(3.5, -3, 0))
    m.cube("left_ear", (-1, -1.5, -0.5), (2, 2, 1), "ear")

    m.bone("body", pivot=(-2, 9, 12), rotation=(90, 0, 0))
    m.cube("body", (-5, -13, -7), (14, 14, 11), "coat")
    m.cube("body", (-4, -25, -7), (12, 12, 10), "coat")
    m.cube("body", (-5.5, -14, 3.5), (15, 15, 1.5), "belly_shag")              # Bauchzotteln
    m.bone("hump", "body")                                                     # Schulterbuckel des Braunbären
    m.cube("hump", (-3, -22, -8.5), (10, 8, 2.5), "coat")

    for name, x, z, d in (("right_hind_leg", -4.5, 6, 8), ("left_hind_leg", 4.5, 6, 8), ("right_front_leg", -3.5, -8, 6),
                          ("left_front_leg", 3.5, -8, 6)):
        m.bone(name, pivot=(x, 14, z))
        m.cube(name, (-2, 0, -2), (4, 10, d), "leg")
    return m


def materials(variant: str) -> dict:
    body = farm.shaggy("fur", seed=131, length=2.4, contrast=0.24)
    brown = variant == "bear_braun"

    def coat(ctx: Ctx):
        col = body(ctx)
        if brown and (ctx.face == "top" or rel_y(ctx) < 0.25):
            col = mix(col, pick(ctx.pal["dark"], 0.55), 0.45)                      # hellerer, silbriger Rücken
        if rel_y(ctx) > 0.8 and ctx.face != "top":
            col = mix(col, pick(ctx.pal["belly"], 0.4), 0.5)
        return col

    def skull(ctx: Ctx):
        if ctx.face == "front" and ctx.t == 2 and ctx.s in (1, 5):
            return pick(ctx.pal["eye"], 0.2)
        return coat(ctx)

    def muzzle(ctx: Ctx):
        if ctx.face == "front" and ctx.t == 0 and 1 <= ctx.s <= 3:
            return pick(ctx.pal["nose"], 0.3)
        if ctx.face == "front" and ctx.t == 2 and 1 <= ctx.s <= 3:
            return pick(ctx.pal["nose"], 0.15)
        return pick(ctx.pal["muzzle"], 0.55 + hash01(ctx.u, ctx.v) * 0.12)

    def ear(ctx: Ctx):
        if ctx.face == "front" and ctx.t > 0:
            return pick(ctx.pal["dark"], 0.3)
        return coat(ctx)

    leg = wild.claws(farm.shaggy("fur", seed=133, length=1.8, contrast=0.22), "claw", from_rel=0.88)

    def legs(ctx: Ctx):
        col = leg(ctx)
        if variant == "bear_schwarz" or brown:
            return col
        return col

    return {
        "skull": skull,
        "muzzle": muzzle,
        "shag": farm.shaggy("fur", seed=135, length=1.4, contrast=0.25),
        "ear": ear,
        "coat": coat,
        "belly_shag": farm.shaggy("belly", seed=137, length=1.2, contrast=0.3),
        "leg": legs,
    }


def hidden(variant: str) -> list[str]:
    return [] if variant == "bear_braun" else ["hump"]
