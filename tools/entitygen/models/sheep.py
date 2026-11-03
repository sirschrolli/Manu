"""
Schaf: Körper mit dichtem Vlies (eigener Wollmantel, vom Renderer in der Farbe der Wolle eingefärbt), schmaler
Kopf mit seitlich hängenden Ohren, dünne Beine, Stummelschwanz. Böcke der Heidschnucke tragen Schneckenhörner.
Drei Rassen:

  * Merinoschaf   – weißer Kopf mit rosiger Nase, besonders dichtes Vlies bis über die Stirn
  * Heidschnucke  – schwarzer Kopf und schwarze Beine, graues Vlies, gewundene Hörner
  * Schwarzkopf   – schwarzer Kopf und schwarze Beine, weißes Vlies, Hängeohren

Die Wolle steckt in eigenen Knochen ("wool_*") – geschoren werden sie ausgeblendet.
"""
from __future__ import annotations

from ..geometry import Model
from ..paint import Ctx, eye_painter, hash01, pick, ramp, rel_y
from . import farm

BREEDS = ["merino", "heidschnucke", "schwarzkopf"]

_COMMON = {"eye": ramp("#1a1208", "#8a6a2a"), "hoof": ramp("#16110e", "#4e443c"), "wool": ramp("#bcb8b2", "#ffffff"),
           "horn": ramp("#6a5a46", "#e8dcc2"), "horn_tip": ramp("#3a3026", "#8a7a62")}

PALETTES = {
    "merino": {**_COMMON, "fur": ramp("#a89c90", "#f6efe6"), "skin": ramp("#9a6a64", "#f2c4b8"), "leg": ramp("#a89c90", "#f2ece4")},
    "heidschnucke": {**_COMMON, "fur": ramp("#141214", "#4a4648"), "skin": ramp("#1a1618", "#5a5254"),
                     "leg": ramp("#141214", "#4a4648")},
    "schwarzkopf": {**_COMMON, "fur": ramp("#0e0c0e", "#403c3e"), "skin": ramp("#161214", "#4e4648"),
                    "leg": ramp("#0e0c0e", "#403c3e")},
}
for _p in PALETTES.values():
    _p["back"] = _p["fur"]


def build() -> Model:
    m = Model("sheep", 128, 64)
    m.bone("body", pivot=(0, 12, 0))
    m.cube("body", (-4, -4, -7.5), (8, 8, 15), "skin_body")              # Körper unter der Wolle (geschoren)
    m.bone("wool_body", "body", pivot=(0, 0, 0))
    m.cube("wool_body", (-5.5, -6, -8.5), (11, 10.5, 17), "wool")
    m.cube("wool_body", (-4.5, -7, -7), (9, 1.5, 13), "wool")            # aufgewölbter Rücken

    m.bone("head", "body", pivot=(0, -3, -8.5), rotation=(12, 0, 0))
    m.cube("head", (-2.5, -3.5, -6.5), (5, 5.5, 6.5), "face")
    m.cube("head", (-2, -1, -8.5), (4, 3.5, 2.5), "nose")
    m.cube("head", (2.5, -2.2, -5.5), (0.5, 1, 1), "eye")
    m.cube("head", (-3, -2.2, -5.5), (0.5, 1, 1), "eye")
    m.bone("wool_head", "head", pivot=(0, 0, 0))
    m.cube("wool_head", (-3, -4.5, -5), (6, 2.5, 5), "wool")             # Wollschopf auf dem Kopf
    for side in (1, -1):
        sfx = "l" if side > 0 else "r"
        m.bone("ear_" + sfx, "head", pivot=(2.4 * side, -2.6, -2.5), rotation=(0, 0, 50 * side))
        m.cube("ear_" + sfx, (0 if side > 0 else -3, -0.5, -0.75), (3, 1, 1.5), "ear")
        # Schneckenhorn der Heidschnucke: nach hinten, außen herum und nach vorne
        m.bone("horn_" + sfx, "head", pivot=(2.0 * side, -3.6, -2.5), rotation=(-30, 0, 30 * side))
        m.cube("horn_" + sfx, (-0.8, -2.5, -0.8), (1.6, 2.5, 1.6), "horn")
        m.bone("horn_mid_" + sfx, "horn_" + sfx, pivot=(0, -2.3, 0), rotation=(-95, 0, 0))
        m.cube("horn_mid_" + sfx, (-0.7, -2.6, -0.7), (1.4, 2.6, 1.4), "horn")
        m.bone("horn_low_" + sfx, "horn_mid_" + sfx, pivot=(0, -2.4, 0), rotation=(-80, 0, -10 * side))
        m.cube("horn_low_" + sfx, (-0.6, -2.4, -0.6), (1.2, 2.4, 1.2), "horn")
        m.bone("horn_tip_" + sfx, "horn_low_" + sfx, pivot=(0, -2.2, 0), rotation=(-60, 0, 0))
        m.cube("horn_tip_" + sfx, (-0.5, -1.8, -0.5), (1, 1.8, 1), "horn", tag="tip")

    for name, x, z in (("front_left_leg", 2.2, -5), ("front_right_leg", -2.2, -5),
                       ("hind_left_leg", 2.2, 5), ("hind_right_leg", -2.2, 5)):
        m.bone(name, pivot=(x, 16, z))
        m.cube(name, (-1, 0, -1), (2, 8, 2), "leg")
        m.bone("wool_" + name, name, pivot=(0, 0, 0))
        m.cube("wool_" + name, (-1.5, -0.5, -1.5), (3, 3.5, 3), "wool")  # Wollhosen

    m.bone("tail", "body", pivot=(0, -3, 7.5), rotation=(25, 0, 0))
    m.cube("tail", (-1, 0, -0.5), (2, 3.5, 1.5), "wool")
    return m


def _face(variant: str):
    base = farm.fur("fur", None, seed=61, contrast=0.16, top_dark=0.04)
    return base


def _nose(variant: str):
    def paint(ctx: Ctx):
        if ctx.face == "front":
            v = 0.5 + hash01(ctx.u, ctx.v) * 0.1
            if ctx.t == 1 and ctx.s in (0, ctx.w - 1):
                v = 0.15
            return pick(ctx.pal["skin"], v)
        return pick(ctx.pal["fur"], 0.5 + hash01(ctx.u, ctx.v) * 0.1)

    return paint


def _skin_body(ctx: Ctx):
    # geschorener Körper: kurze Stoppeln, heller als das Vlies
    return pick(ctx.pal["wool"], 0.55 + (hash01(ctx.u, ctx.v) - 0.5) * 0.18)


def _ear(ctx: Ctx):
    if ctx.face == "bottom":
        return pick(ctx.pal["skin"], 0.6)
    return pick(ctx.pal["fur"], 0.5 + hash01(ctx.u, ctx.v) * 0.1)


def materials(variant: str) -> dict:
    return {
        "skin_body": _skin_body,
        "wool": farm.wool("wool", seed=62),
        "face": _face(variant),
        "nose": _nose(variant),
        "eye": eye_painter("eye", glow=False),
        "ear": _ear,
        "horn": farm.horn("horn", "horn_tip", rings=1.0),
        "leg": farm.leg("leg", "hoof", seed=63),
    }


def hidden(variant: str) -> list[str]:
    out = []
    if variant != "heidschnucke":
        out += ["horn_l", "horn_r"]
    if variant != "merino":
        out.append("wool_head")
    return out
