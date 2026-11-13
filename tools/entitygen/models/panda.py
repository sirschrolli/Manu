"""
Panda auf dem Skelett des Vanilla-Pandas (Sitzen, Rollen, Rückenlage, Niesen und Bambus im Maul bleiben). Rundere
Ohren, Schnauze mit Nase, Augenflecken – die Augenflecken verraten das Gemüt (die Gene):

  normal, faul (müde Lider), besorgt (hängende Flecken), verspielt (große runde Flecken), braun (braun-weiß),
  schwach (blass, rote Nase), aggressiv (schräge Flecken, Brauen)
"""
from __future__ import annotations

from ..geometry import Model
from ..paint import Ctx, hash01, mix, pick, ramp, rel_y
from . import farm

GENES = ["normal", "lazy", "worried", "playful", "brown", "weak", "aggressive"]

_C = {"eye": ramp("#000000", "#3a3a3a"), "nose": ramp("#08080a", "#3a3a40"), "red": ramp("#7a2020", "#e86060"),
      "claw": ramp("#101012", "#4a4a50")}

PALETTES = {}
for _g in GENES:
    _p = {**_C, "fur": ramp("#c8c4bc", "#ffffff"), "black": ramp("#0a0a0c", "#36363a")}
    if _g == "brown":
        _p["black"] = ramp("#2a1a10", "#7a5636")
        _p["fur"] = ramp("#c0b098", "#f8f0e0")
    if _g == "weak":
        _p["fur"] = ramp("#c8c8c4", "#ffffff")
        _p["black"] = ramp("#2a2a2e", "#6a6a70")
    _p["back"] = _p["fur"]
    PALETTES["panda_" + _g] = _p


def build() -> Model:
    m = Model("panda", 128, 64)
    m.world_faces = True
    m.bone("head", pivot=(0, 11.5, -17))
    m.cube("head", (-6.5, -5, -4), (13, 10, 9), "skull")
    m.cube("head", (-3.5, 0, -6), (7, 5, 2), "snout")
    m.cube("head", (-1.5, 0.2, -6.6), (3, 1.4, 0.6), "nose")
    m.bone("right_ear", "head", pivot=(-6, -5, 0.5))
    m.cube("right_ear", (-2.5, -3.5, -0.5), (4, 4, 1.5), "ear")
    m.bone("left_ear", "head", pivot=(6, -5, 0.5))
    m.cube("left_ear", (-1.5, -3.5, -0.5), (4, 4, 1.5), "ear")

    m.bone("body", pivot=(0, 10, 0), rotation=(90, 0, 0))
    m.cube("body", (-9.5, -13, -6.5), (19, 26, 13), "coat")
    m.bone("tail", "body", pivot=(0, 13, 3))
    m.cube("tail", (-1.5, -0.5, -1.5), (3, 1.5, 3), "coat")

    for name, x, z in (("right_hind_leg", -5.5, 9), ("left_hind_leg", 5.5, 9), ("right_front_leg", -5.5, -9), ("left_front_leg", 5.5, -9)):
        m.bone(name, pivot=(x, 15, z))
        m.cube(name, (-3, 0, -3), (6, 9, 6), "leg")
    return m


def _gene(ctx: Ctx) -> str:
    return str(ctx.params.get("variant", "panda_normal"))[6:]


def materials(variant: str) -> dict:
    white = farm.fur("fur", None, seed=141, contrast=0.12, top_dark=0.02)
    black = farm.fur("black", None, seed=142, contrast=0.16, top_dark=0.0)
    gene = variant[6:]

    def skull(ctx: Ctx):
        if ctx.face == "front":
            s, t = ctx.s, ctx.t
            # Augenflecken (Lage je Gemüt), darin die Augen
            patches = {
                "normal": {(2, 4), (3, 4), (2, 5), (3, 5), (9, 4), (10, 4), (9, 5), (10, 5), (2, 6), (10, 6)},
                "lazy": {(2, 4), (3, 4), (2, 5), (3, 5), (9, 4), (10, 4), (9, 5), (10, 5), (3, 6), (9, 6)},
                "worried": {(2, 4), (3, 4), (1, 5), (2, 5), (3, 5), (9, 4), (10, 4), (9, 5), (10, 5), (11, 5), (1, 6), (11, 6)},
                "playful": {(1, 3), (2, 3), (3, 3), (1, 4), (2, 4), (3, 4), (1, 5), (2, 5), (3, 5), (9, 3), (10, 3), (11, 3),
                            (9, 4), (10, 4), (11, 4), (9, 5), (10, 5), (11, 5)},
                "brown": {(2, 4), (3, 4), (2, 5), (3, 5), (9, 4), (10, 4), (9, 5), (10, 5), (2, 6), (10, 6)},
                "weak": {(2, 4), (3, 4), (2, 5), (3, 5), (9, 4), (10, 4), (9, 5), (10, 5)},
                "aggressive": {(3, 3), (2, 4), (3, 4), (4, 4), (2, 5), (3, 5), (9, 3), (8, 4), (9, 4), (10, 4), (9, 5), (10, 5)},
            }[gene]
            eyes = {"normal": {(3, 4), (9, 4)}, "lazy": {(3, 5), (9, 5)}, "worried": {(2, 5), (10, 5)},
                    "playful": {(2, 4), (10, 4)}, "brown": {(3, 4), (9, 4)}, "weak": {(3, 5), (9, 5)},
                    "aggressive": {(3, 4), (9, 4)}}[gene]
            if (s, t) in eyes:
                return pick(ctx.pal["eye"], 0.0)
            if (s, t) in patches:
                return black(ctx)
            if gene == "aggressive" and t == 2 and s in (2, 3, 9, 10):
                return black(ctx)                                                    # finstere Brauen
        return white(ctx)

    def snout(ctx: Ctx):
        if gene == "weak" and ctx.face == "front" and ctx.t >= 2:
            return mix(white(ctx), pick(ctx.pal["red"], 0.6), 0.35)                  # verschnupft
        return white(ctx)

    def coat(ctx: Ctx):
        x, y, z = ctx.world
        # schwarzes Schulterband um den Vorderleib
        if -10.5 < z < -3.0 and rel_y(ctx) < 0.95:
            return black(ctx)
        return white(ctx)

    def leg(ctx: Ctx):
        if ctx.face == "bottom":
            return pick(ctx.pal["claw"], 0.3)
        if ctx.face == "front" and rel_y(ctx) > 0.86 and ctx.s % 2 == 0:
            return pick(ctx.pal["claw"], 0.6)
        return black(ctx)

    return {
        "skull": skull,
        "snout": snout,
        "nose": farm.solid("nose", 0.3, 0.1),
        "ear": lambda ctx: black(ctx),
        "coat": coat,
        "leg": leg,
    }


def hidden(variant: str) -> list[str]:
    return []
