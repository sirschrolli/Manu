"""
Wolf auf dem Skelett des Vanilla-Wolfs (gleiche Teile und Drehpunkte, damit Wolfspanzer und alle Vanilla-Bewegungen
passen – Sitzen, Schütteln, Betteln). Eigenes Fell, höhere Ohren, Halskrause, buschige Rute, eigenes Halsband (wird in
der Farbe des Halsbands eingefärbt). Rassen:

  * Grauwolf   – grau meliert mit dunklem Sattel (Wälder, Taiga)
  * Polarwolf  – weiß (Schnee)
  * Schwarzwolf – fast schwarz (alter Kiefernwald)
  * Timberwolf – graubraun (alter Fichtenwald)
  * Steppenwolf – sandfarben (Savanne, Tafelberge)
  * Rotwolf    – rostrot (Dschungel)

Jede Rasse gibt es zweimal: ruhig und wütend (rote Augen, Zähne).
"""
from __future__ import annotations

from ..geometry import Model
from ..paint import Ctx, hash01, mix, pick, ramp, rel_y
from . import farm, wild

BREEDS = ["grau", "polar", "schwarz", "timber", "steppe", "rot"]

_C = {"eye": ramp("#1a1004", "#e0a020"), "nose": ramp("#0a0808", "#3a3434"), "collar": ramp("#b0b0b0", "#ffffff"),
      "teeth": ramp("#b8b0a0", "#ffffff"), "angry": ramp("#5a0808", "#ff3a20"), "claw": ramp("#141210", "#4a4440")}

_P = {
    "grau": {"fur": ramp("#4a4846", "#b8b4ac"), "belly": ramp("#a8a49c", "#f2eee6"), "dark": ramp("#1e1c1a", "#5a5650")},
    "polar": {"fur": ramp("#b8b4ac", "#ffffff"), "belly": ramp("#c8c4bc", "#ffffff"), "dark": ramp("#a8a49c", "#e0dcd4")},
    "schwarz": {"fur": ramp("#0c0c0e", "#46464c"), "belly": ramp("#2a2a2e", "#6a6a70"), "dark": ramp("#060608", "#2a2a2e")},
    "timber": {"fur": ramp("#3e3022", "#a88a68"), "belly": ramp("#a8987e", "#efe2c8"), "dark": ramp("#1e160e", "#4e3e2e")},
    "steppe": {"fur": ramp("#6a4e2a", "#e0c08a"), "belly": ramp("#c8b48e", "#fbf0d8"), "dark": ramp("#3a2a16", "#8a6a40")},
    "rot": {"fur": ramp("#5a2410", "#d07a44"), "belly": ramp("#b8a088", "#f2e4d0"), "dark": ramp("#2a120a", "#6a3018")},
}

PALETTES = {}
for _b, _p in _P.items():
    for _mood in ("", "_angry"):
        PALETTES["wolf_" + _b + _mood] = {**_C, **_p, "back": _p["fur"]}


def build() -> Model:
    m = Model("wolf", 64, 64)
    m.world_faces = True
    m.bone("head", pivot=(-1, 13.5, -7))
    m.bone("real_head", "head")
    m.cube("real_head", (-2, -3, -2), (6, 6, 4), "skull")
    m.cube("real_head", (-0.5, -0.001, -5), (3, 3, 4), "snout")
    m.cube("real_head", (-2.6, -0.5, -1.6), (0.6, 3.5, 3.2), "ruff")            # Backenbart
    m.cube("real_head", (4.0, -0.5, -1.6), (0.6, 3.5, 3.2), "ruff")
    m.bone("ear_r", "real_head", pivot=(-1, -3, 0.5), rotation=(-8, 0, -6))
    m.cube("ear_r", (-1, -3, -0.5), (2, 3, 1), "ear")
    m.bone("ear_l", "real_head", pivot=(3, -3, 0.5), rotation=(-8, 0, 6))
    m.cube("ear_l", (-1, -3, -0.5), (2, 3, 1), "ear")

    m.bone("body", pivot=(0, 14, 2), rotation=(90, 0, 0))
    m.cube("body", (-3, -2, -3), (6, 9, 6), "coat")
    m.bone("upper_body", pivot=(-1, 14, -3), rotation=(90, 0, 0))
    m.cube("upper_body", (-3, -3, -3), (8, 6, 7), "mane")
    m.cube("upper_body", (-3.4, -3.5, -3.4), (8.8, 2.4, 7.8), "ruff")          # Halskrause
    m.bone("collar", "upper_body")
    m.cube("collar", (-3.6, -2.4, -3.6), (9.2, 1.2, 8.2), "collar")

    for name, x, z in (("right_hind_leg", -2.5, 7), ("left_hind_leg", 0.5, 7), ("right_front_leg", -2.5, -4),
                       ("left_front_leg", 0.5, -4)):
        m.bone(name, pivot=(x, 16, z))
        m.cube(name, (0, 0, -1), (2, 8, 2), "leg")

    m.bone("tail", pivot=(-1, 12, 8), rotation=(36, 0, 0))
    m.bone("real_tail", "tail")
    m.cube("real_tail", (-0.25, 0, -1.25), (2.5, 6, 2.5), "tail")
    m.cube("real_tail", (-0.5, 5.5, -1.5), (3, 3, 3), "tail")
    return m


def _angry(ctx: Ctx) -> bool:
    return str(ctx.params.get("variant", "")).endswith("_angry")


def materials(variant: str) -> dict:
    body = wild.coat("fur", "belly", "dark", seed=111, contrast=0.2)

    def skull(ctx: Ctx):
        # Augen (bernstein, wütend rot), helle Brauen, dunkle Stirn
        if ctx.face == "front" and ctx.t == 2 and ctx.s in (1, 4):
            return pick(ctx.pal["angry" if _angry(ctx) else "eye"], 0.75)
        if ctx.face == "front" and ctx.t == 2 and ctx.s in (0, 5):
            return pick(ctx.pal["eye"], 0.05)
        if ctx.face == "front" and ctx.t == 1 and ctx.s in (1, 4):
            return pick(ctx.pal["belly"], 0.75)
        if ctx.face in ("front", "bottom") and rel_y(ctx) > 0.6:
            return pick(ctx.pal["belly"], 0.65 + hash01(ctx.u, ctx.v) * 0.12)
        return body(ctx)

    def snout(ctx: Ctx):
        if ctx.face == "front" and ctx.t == 0 and 0 < ctx.s < ctx.w - 1:
            return pick(ctx.pal["nose"], 0.3)
        if ctx.face == "top" and ctx.t == 0:
            return pick(ctx.pal["nose"], 0.35)
        if _angry(ctx) and ctx.face in ("front", "west", "east") and ctx.t == ctx.h - 1:
            return pick(ctx.pal["teeth"], 0.85 if ctx.s % 2 == 0 else 0.5)
        if ctx.face in ("bottom", "front", "west", "east") and rel_y(ctx) > 0.4:
            return pick(ctx.pal["belly"], 0.62 + hash01(ctx.u, ctx.v) * 0.12)
        return body(ctx)

    def ear(ctx: Ctx):
        if ctx.face == "front" and ctx.t > 0:
            return pick(ctx.pal["belly"], 0.45)
        if rel_y(ctx) < 0.34:
            return pick(ctx.pal["dark"], 0.35)
        return body(ctx)

    def leg(ctx: Ctx):
        if ctx.face == "bottom" or rel_y(ctx) > 0.9:
            return pick(ctx.pal["claw"], 0.4)
        if ctx.face in ("front", "west", "east") and rel_y(ctx) > 0.5:
            return mix(body(ctx), pick(ctx.pal["belly"], 0.7), 0.5)
        return body(ctx)

    def tail(ctx: Ctx):
        lx, ly, lz = ctx.local
        col = farm.shaggy("fur", seed=113, length=2.0, contrast=0.3)(ctx)
        if ly > 6.5:
            return pick(ctx.pal["dark"], 0.35 + hash01(ctx.u, ctx.v) * 0.12)       # dunkle Rutenspitze
        return col

    return {
        "skull": skull,
        "snout": snout,
        "ruff": farm.shaggy("belly", seed=115, length=1.4, contrast=0.25),
        "ear": ear,
        "coat": body,
        "mane": body,
        "collar": farm.solid("collar", 0.8, 0.1),
        "leg": leg,
        "tail": tail,
    }


def hidden(variant: str) -> list[str]:
    return ["collar"]
