"""
Fuchs auf dem Skelett des Vanilla-Fuchses (Schlafen, Lauern, Sprung, Sitzen und das Tragen von Dingen im Maul
bleiben, wie sie sind). Größere Ohren, weiße Backen, buschige Lunte. Rassen:

  * Rotfuchs   – orange mit weißer Brust, schwarzen Socken und weißer Luntenspitze (Taiga, Wälder)
  * Polarfuchs – weiß mit grauen Spitzen (Schnee)
  * Wüstenfuchs – sandfarben, riesige Ohren (Wüste, Tafelberge)
  * Silberfuchs – schwarz mit silbernem Reif und weißer Luntenspitze (selten in der Taiga)
"""
from __future__ import annotations

from ..geometry import Model
from ..paint import Ctx, hash01, mix, pick, ramp, rel_y
from . import farm, wild

BREEDS = ["rot", "polar", "wueste", "silber"]

_C = {"eye": ramp("#0a0604", "#e8b020"), "nose": ramp("#0a0808", "#3a3434"), "claw": ramp("#0e0c0a", "#3e3834"),
      "white": ramp("#c8c4bc", "#ffffff")}

PALETTES = {
    "fox_rot": {**_C, "fur": ramp("#7a2e08", "#f08a3a"), "belly": ramp("#c8c0b4", "#ffffff"), "dark": ramp("#0e0a08", "#3a2a22"),
                "tip": ramp("#c8c4bc", "#ffffff")},
    "fox_polar": {**_C, "fur": ramp("#b8b4ac", "#ffffff"), "belly": ramp("#c8c4bc", "#ffffff"), "dark": ramp("#6a6a70", "#b8b8c0"),
                  "tip": ramp("#c8c4bc", "#ffffff")},
    "fox_wueste": {**_C, "fur": ramp("#9a7a4a", "#f4dcaa"), "belly": ramp("#d8ccb4", "#fffaf0"), "dark": ramp("#5a4428", "#a88a5a"),
                   "tip": ramp("#2a2018", "#5a4a3a")},
    "fox_silber": {**_C, "fur": ramp("#0c0c0e", "#3e3e44"), "belly": ramp("#2a2a2e", "#5a5a60"), "dark": ramp("#060608", "#26262a"),
                   "tip": ramp("#c8c4bc", "#ffffff"), "frost": ramp("#8a8a90", "#e8e8f0")},
}
for _p in PALETTES.values():
    _p["back"] = _p["fur"]
    _p.setdefault("frost", _p["belly"])
# jede Art auch schlafend (geschlossene Augen)
PALETTES.update({f"{_k}_sleep": _v for _k, _v in list(PALETTES.items())})


def build() -> Model:
    m = Model("fox", 64, 64)
    m.world_faces = True
    m.bone("head", pivot=(-1, 16.5, -3))
    m.cube("head", (-3, -2, -5), (8, 6, 6), "skull")
    m.cube("head", (-3.6, 1.0, -4.6), (1.0, 3.0, 3.4), "cheek")                 # weiße Backen
    m.cube("head", (4.6, 1.0, -4.6), (1.0, 3.0, 3.4), "cheek")
    m.bone("right_ear", "head", pivot=(-2, -2, -4))
    m.cube("right_ear", (-1, -2.5, 0), (2, 2.5, 1), "ear")
    m.bone("left_ear", "head", pivot=(4, -2, -4))
    m.cube("left_ear", (-1, -2.5, 0), (2, 2.5, 1), "ear")
    m.bone("ear_big_r", "head", pivot=(-2, -2, -4), rotation=(0, 0, -14))      # Riesenohren des Wüstenfuchses
    m.cube("ear_big_r", (-1.5, -5.0, 0), (3, 5, 1), "ear")
    m.bone("ear_big_l", "head", pivot=(4, -2, -4), rotation=(0, 0, 14))
    m.cube("ear_big_l", (-1.5, -5.0, 0), (3, 5, 1), "ear")
    m.bone("nose", "head")
    m.cube("nose", (-1, 2.01, -8), (4, 2, 3), "snout")

    m.bone("body", pivot=(0, 16, -6), rotation=(90, 0, 0))
    m.cube("body", (-3, 3.999, -3.5), (6, 11, 6), "coat")
    m.cube("body", (-2.5, 3.6, -3.8), (5, 3.5, 2), "chest")                   # weißer Latz
    m.bone("tail", "body", pivot=(-4, 15, -1), rotation=(-3, 0, 0))
    m.cube("tail", (1.5, 0, -1.5), (5, 9.5, 6), "tail")

    for name, x, z in (("right_hind_leg", -5, 7), ("left_hind_leg", -1, 7), ("right_front_leg", -5, 0), ("left_front_leg", -1, 0)):
        m.bone(name, pivot=(x, 17.5, z))
        m.cube(name, (2, 0.5, -1), (2, 6, 2), "leg")
    return m


def materials(variant: str) -> dict:
    body = wild.coat("fur", "belly", None, seed=121, contrast=0.18, agouti=0.03)
    silver = variant.startswith("fox_silber")
    sleeping = variant.endswith("_sleep")

    def frosted(ctx: Ctx):
        col = body(ctx)
        # silberne Haarspitzen: nur auf Rücken und Flanken, dünn gestreut und weich eingemischt
        if silver and ctx.face != "bottom" and rel_y(ctx) < 0.7 and hash01(ctx.u, ctx.v, 9) > 0.955:
            return mix(col, pick(ctx.pal["frost"], 0.45), 0.4)
        return col

    def skull(ctx: Ctx):
        if sleeping and ctx.face == "front" and ctx.t == 2 and ctx.s in (1, 2, 5, 6):
            return pick(ctx.pal["dark"], 0.25)                                       # geschlossene Lider
        if ctx.face == "front" and ctx.t == 2 and ctx.s in (1, 6):
            return pick(ctx.pal["eye"], 0.75)
        if ctx.face == "front" and ctx.t == 2 and ctx.s in (2, 5):
            return pick(ctx.pal["eye"], 0.05)
        if ctx.face == "front" and ctx.t >= 4:
            return pick(ctx.pal["belly"], 0.7)
        return frosted(ctx)

    def snout(ctx: Ctx):
        if ctx.face == "front" and ctx.t == 0 and 0 < ctx.s < ctx.w - 1:
            return pick(ctx.pal["nose"], 0.3)
        if ctx.face in ("bottom", "west", "east", "front") and rel_y(ctx) > 0.45:
            return pick(ctx.pal["belly"], 0.7)
        return frosted(ctx)

    def ear(ctx: Ctx):
        if ctx.face == "front" and rel_y(ctx) > 0.3 and 0 < ctx.s < ctx.w - 1:
            return pick(ctx.pal["belly"], 0.55)
        if ctx.face == "back":
            return pick(ctx.pal["dark"], 0.4)                                        # dunkle Ohrrückseiten
        return frosted(ctx)

    def leg(ctx: Ctx):
        if rel_y(ctx) > 0.35 or ctx.face == "bottom":
            return pick(ctx.pal["dark"], 0.35 + hash01(ctx.u, ctx.v) * 0.12)       # schwarze Socken
        return frosted(ctx)

    def tail(ctx: Ctx):
        lx, ly, lz = ctx.local
        if ly > 7.2:
            return pick(ctx.pal["tip"], 0.75 + hash01(ctx.u, ctx.v) * 0.12)       # Luntenspitze
        col = farm.shaggy("fur", seed=123, length=2.2, contrast=0.25)(ctx)
        if silver and hash01(ctx.u, ctx.v, 3) > 0.95:
            return mix(col, pick(ctx.pal["frost"], 0.45), 0.4)
        return col

    return {
        "skull": skull,
        "cheek": farm.shaggy("belly", seed=125, length=1.2, contrast=0.2),
        "ear": ear,
        "snout": snout,
        "coat": frosted,
        "chest": farm.shaggy("belly", seed=127, length=1.2, contrast=0.2),
        "tail": tail,
        "leg": leg,
    }


def hidden(variant: str) -> list[str]:
    if variant.startswith("fox_wueste"):
        return ["right_ear", "left_ear"]
    return ["ear_big_r", "ear_big_l"]
