"""
Katzen und Wildkatzen auf dem Skelett der Vanilla-Katze (Sitzen, Liegen, Schleichen, Schnurren bleiben).
Größerer Kopf mit Backen, spitze Ohren, Schwanz in zwei Teilen; Halsband (eingefärbt), Ohrpinsel und Stummelschwanz
des Luchses, Backenbart und dichtes Fell von Perser und Luchs.

Hauskatzen: getigert, Kater (rot), Siam, Kartäuser, Glückskatze, Perser, Smoking (schwarz-weiß), Hexenkatze (schwarz)
Wildkatzen: Ozelot (Dschungel), Luchs (Taiga, Schnee), Waldkatze (Wälder)
"""
from __future__ import annotations

import math

from ..geometry import Model
from ..paint import Ctx, hash01, mix, noise3, pick, ramp, rel_y
from . import farm, wild

CATS = ["getigert", "rot", "siam", "kartaeuser", "glueck", "perser", "smoking", "hexe"]
WILD = ["ozelot", "luchs", "waldkatze"]

_C = {"eye": ramp("#0a1004", "#a8e040"), "nose": ramp("#5a3a3a", "#e8a0a0"), "collar": ramp("#b0b0b0", "#ffffff"),
      "claw": ramp("#1a1612", "#5a5048"), "white": ramp("#c8c4bc", "#ffffff")}

PALETTES = {
    "cat_getigert": {**_C, "fur": ramp("#5a4a3a", "#c8b090"), "belly": ramp("#b0a490", "#f0e8d8"), "dark": ramp("#2a1e14", "#5a4630")},
    "cat_rot": {**_C, "fur": ramp("#8a3e10", "#f8a85a"), "belly": ramp("#d8b890", "#fff0d8"), "dark": ramp("#6a2a08", "#b8601e")},
    "cat_siam": {**_C, "fur": ramp("#a89880", "#fbf2e2"), "belly": ramp("#c8bca8", "#ffffff"), "dark": ramp("#2a1c14", "#6a4a36"),
                 "eye": ramp("#04102a", "#5a9aff")},
    "cat_kartaeuser": {**_C, "fur": ramp("#4a4e58", "#a8b0bc"), "belly": ramp("#6a6e78", "#b8c0ca"), "dark": ramp("#2a2e36", "#5a606a"),
                       "eye": ramp("#2a1a04", "#f0a020")},
    "cat_glueck": {**_C, "fur": ramp("#b8b0a4", "#ffffff"), "belly": ramp("#c8c0b4", "#ffffff"), "dark": ramp("#0e0c0a", "#3a3430"),
                   "orange": ramp("#7a3a10", "#e88a3a")},
    "cat_perser": {**_C, "fur": ramp("#a89a84", "#fbf2e0"), "belly": ramp("#c8bca8", "#ffffff"), "dark": ramp("#8a7a62", "#d8c8a8"),
                   "eye": ramp("#2a1a04", "#e8a030")},
    "cat_smoking": {**_C, "fur": ramp("#0c0c0e", "#3a3a40"), "belly": ramp("#c8c4bc", "#ffffff"), "dark": ramp("#060608", "#26262a")},
    "cat_hexe": {**_C, "fur": ramp("#060608", "#2e2e34"), "belly": ramp("#0c0c0e", "#36363c"), "dark": ramp("#040406", "#1e1e22"),
                 "eye": ramp("#2a2004", "#f8e040")},
    "wildcat_ozelot": {**_C, "fur": ramp("#8a6420", "#f0c86a"), "belly": ramp("#c8bca8", "#fffaf0"), "dark": ramp("#140c06", "#4a3218")},
    "wildcat_luchs": {**_C, "fur": ramp("#6a5a46", "#d8c4a4"), "belly": ramp("#c0b6a4", "#f8f2e8"), "dark": ramp("#1e1610", "#5a4836"),
                      "eye": ramp("#2a2004", "#e0c040")},
    "wildcat_waldkatze": {**_C, "fur": ramp("#4e4a40", "#b0a890"), "belly": ramp("#a8a090", "#ece4d4"), "dark": ramp("#1a1814", "#4a463c")},
}
for _p in PALETTES.values():
    _p["back"] = _p["fur"]
    _p.setdefault("orange", _p["fur"])

FLUFFY = {"cat_perser", "wildcat_luchs"}


def build() -> Model:
    m = Model("feline", 64, 64)
    m.world_faces = True
    m.bone("head", pivot=(0, 15, -9))
    m.cube("head", (-2.5, -2, -3), (5, 4, 5), "skull")
    m.cube("head", (-1.5, -0.001, -4), (3, 2, 2), "snout")
    m.bone("ear_l", "head", pivot=(1.5, -2, 0.5))
    m.cube("ear_l", (-0.5, -1.5, -0.5), (1.2, 1.5, 1.6), "ear")
    m.bone("ear_r", "head", pivot=(-1.5, -2, 0.5))
    m.cube("ear_r", (-0.7, -1.5, -0.5), (1.2, 1.5, 1.6), "ear")
    m.bone("tuft_l", "ear_l")                                                   # Ohrpinsel des Luchses
    m.cube("tuft_l", (0.0, -3.2, 0.1), (0.4, 1.8, 0.4), "tuft")
    m.bone("tuft_r", "ear_r")
    m.cube("tuft_r", (-0.2, -3.2, 0.1), (0.4, 1.8, 0.4), "tuft")
    m.bone("cheeks", "head")                                                    # Backenbart (Perser, Luchs)
    m.cube("cheeks", (-3.3, -0.5, -2.2), (0.8, 2.8, 3), "fluff")
    m.cube("cheeks", (2.5, -0.5, -2.2), (0.8, 2.8, 3), "fluff")

    m.bone("body", pivot=(0, 12, -10), rotation=(90, 0, 0))
    m.cube("body", (-2, 3, -8), (4, 16, 6), "coat")
    m.bone("fluff_body", "body")                                                # dichtes Fell
    m.cube("fluff_body", (-2.4, 3.5, -8.4), (4.8, 15, 6.8), "fluff")
    m.bone("collar", "body")
    m.cube("collar", (-2.3, 3.4, -8.3), (4.6, 1.0, 6.6), "collar")
    m.bone("collar_fluffy", "body")                                             # weiteres Halsband über dem dichten Fell
    m.cube("collar_fluffy", (-2.7, 3.4, -8.7), (5.4, 1.0, 7.4), "collar")

    m.bone("tail1", pivot=(0, 15, 8), rotation=(51.6, 0, 0))
    m.cube("tail1", (-0.5, 0, 0), (1, 8, 1), "tail")
    m.bone("tail2", pivot=(0, 20, 14))
    m.cube("tail2", (-0.5, 0, 0), (1, 8, 1), "tail")
    m.bone("tail_stub", "tail1")                                                # Stummelschwanz des Luchses
    m.cube("tail_stub", (-0.7, -0.2, -0.2), (1.4, 4, 1.4), "tail")

    for name, x, y, z, h, oz in (("left_hind_leg", 1.1, 18, 5, 6, 1), ("right_hind_leg", -1.1, 18, 5, 6, 1),
                                 ("left_front_leg", 1.2, 14.1, -5, 10, 0), ("right_front_leg", -1.2, 14.1, -5, 10, 0)):
        m.bone(name, pivot=(x, y, z))
        m.cube(name, (-1, 0, oz), (2, h, 2), "leg")
    return m


def _v(ctx: Ctx) -> str:
    return str(ctx.params.get("variant", "cat_getigert"))


def _pattern(variant: str):
    base = farm.fur("fur", "belly", seed=151, contrast=0.16, belly_from=0.75, top_dark=0.05)
    if variant in ("cat_getigert", "cat_rot", "wildcat_waldkatze"):
        return wild.stripes_across(base, "dark", period=2.6, width=1.0, axis=2, wobble=0.6, strength=0.7)
    if variant == "wildcat_ozelot":
        def rosettes(ctx: Ctx):
            col = base(ctx)
            if rel_y(ctx) > 0.75:
                return col
            d = wild.face_dot  # noqa: F841 (nur zur Lesbarkeit)
            x, y, z = ctx.world
            n = noise3(x * 0.9, y * 0.9, z * 0.9, 157)
            if n > 0.66:
                return pick(ctx.pal["dark"], 0.4)
            if n > 0.58:
                return mix(col, pick(ctx.pal["dark"], 0.5), 0.5)
            return col
        return rosettes
    if variant == "wildcat_luchs":
        def spots(ctx: Ctx):
            col = farm.shaggy("fur", seed=158, length=1.6, contrast=0.2)(ctx)
            if hash01(math.floor(ctx.world[0] * 0.7), math.floor(ctx.world[2] * 0.7), 3) > 0.85 and rel_y(ctx) < 0.8:
                return mix(col, pick(ctx.pal["dark"], 0.4), 0.6)
            if rel_y(ctx) > 0.75:
                return pick(ctx.pal["belly"], 0.65)
            return col
        return spots
    if variant == "cat_glueck":
        def calico(ctx: Ctx):
            col = pick(ctx.pal["fur"], 0.8 + hash01(ctx.u, ctx.v) * 0.1)
            x, y, z = ctx.world
            n = noise3(x * 0.3, y * 0.3, z * 0.3, 159)
            if n > 0.62:
                return pick(ctx.pal["orange"], 0.6)
            if n < 0.34:
                return pick(ctx.pal["dark"], 0.35)
            return col
        return calico
    if variant == "cat_siam":
        def points(ctx: Ctx):
            return base(ctx)
        return points
    if variant == "cat_smoking":
        def tux(ctx: Ctx):
            if ctx.face in ("bottom",) or (ctx.face == "front" and ctx.cube.bone.name in ("body", "fluff_body")):
                return pick(ctx.pal["belly"], 0.75)
            return pick(ctx.pal["fur"], 0.45 + hash01(ctx.u, ctx.v) * 0.1)
        return tux
    return base


def materials(variant: str) -> dict:
    coat = _pattern(variant)
    pointed = variant == "cat_siam"

    def skull(ctx: Ctx):
        s, t = ctx.s, ctx.t
        if ctx.face == "front" and t == 1 and s in (0, 4):
            return pick(ctx.pal["eye"], 0.75)
        if ctx.face == "front" and t == 1 and s in (1, 3):
            return pick(ctx.pal["eye"], 0.05)
        if pointed and ctx.face == "front":
            return pick(ctx.pal["dark"], 0.4)                                       # dunkle Maske der Siam
        if variant == "cat_smoking" and ctx.face == "front" and s == 2:
            return pick(ctx.pal["belly"], 0.75)                                      # weiße Blesse
        if variant in ("cat_getigert", "cat_rot", "wildcat_waldkatze") and ctx.face == "top" and s % 2 == 0:
            return pick(ctx.pal["dark"], 0.45)                                       # "M" auf der Stirn
        return coat(ctx)

    def snout(ctx: Ctx):
        if ctx.face == "front" and ctx.t == 0 and ctx.s == 1:
            return pick(ctx.pal["nose"], 0.55)
        if pointed:
            return pick(ctx.pal["dark"], 0.4)
        return pick(ctx.pal["belly"], 0.7 + hash01(ctx.u, ctx.v) * 0.1)

    def ear(ctx: Ctx):
        if ctx.face == "front":
            return pick(ctx.pal["nose"], 0.65) if variant not in ("cat_hexe", "cat_smoking") else pick(ctx.pal["dark"], 0.3)
        if pointed or variant.startswith("wildcat"):
            return pick(ctx.pal["dark"], 0.35)
        return coat(ctx)

    def leg(ctx: Ctx):
        if ctx.face == "bottom":
            return pick(ctx.pal["nose"], 0.35)
        if pointed and rel_y(ctx) > 0.3:
            return pick(ctx.pal["dark"], 0.4)
        if variant == "cat_smoking" and rel_y(ctx) > 0.75:
            return pick(ctx.pal["belly"], 0.75)                                      # weiße Pfoten
        return coat(ctx)

    def tail(ctx: Ctx):
        lx, ly, lz = ctx.local
        if pointed:
            return pick(ctx.pal["dark"], 0.4)
        if variant in ("cat_getigert", "cat_rot", "wildcat_waldkatze", "wildcat_ozelot") and int(ly * 0.9) % 2 == 0:
            return pick(ctx.pal["dark"], 0.4)                                        # geringelter Schwanz
        if variant == "wildcat_luchs" and ly > 2.6 and ctx.cube.bone.name == "tail_stub":
            return pick(ctx.pal["dark"], 0.25)                                       # schwarze Schwanzspitze
        return coat(ctx)

    return {
        "skull": skull,
        "snout": snout,
        "ear": ear,
        "tuft": farm.solid("dark", 0.2, 0.1),
        "fluff": farm.shaggy("fur", seed=153, length=1.2, contrast=0.22),
        "coat": coat,
        "collar": farm.solid("collar", 0.8, 0.1),
        "tail": tail,
        "leg": leg,
    }


def hidden(variant: str) -> list[str]:
    out = ["collar", "collar_fluffy"]
    if variant != "wildcat_luchs":
        out += ["tuft_l", "tuft_r", "tail_stub"]
    else:
        out += ["tail2"]
    if variant not in FLUFFY:
        out += ["cheeks", "fluff_body"]
    return out
