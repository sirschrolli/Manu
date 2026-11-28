"""
Hausrind: kräftiger Körper mit Widerrist, hängende Wamme, Euter, langer Schwanz mit Quaste, seitlich abstehende Ohren.
Fünf Rassen mit eigenen Hörnern und Fellen:

  * Fleckvieh     – cremeweiß mit rotbraunen Platten, weißer Kopf, kurze Hörner
  * Braunvieh     – graubraun mit hellem Maulring und dunklem Flotzmaul, kurze Hörner mit schwarzen Spitzen
  * Hochlandrind  – zotteliges rostrotes Fell, Stirnschopf über den Augen, weit ausladende Hörner (kalte Länder)
  * Steppenrind   – silbergrau mit dunklen Schultern, hohe Leierhörner, ausgeprägter Buckel (Savanne, Steppe)
  * Pilzrind      – myzelgraues Fell mit hellen Flecken, auf dem Rücken wachsen Pilze (Pilzland)

Knochen-Namen werden in CattleModel (Java) verwendet: Hörner, Schopf und Pilze werden je Rasse ein- und ausgeblendet.
"""
from __future__ import annotations

from ..geometry import Model
from ..paint import Ctx, eye_painter, hash01, mix, noise3, pick, ramp, rel_y
from . import farm

BREEDS = ["fleckvieh", "braunvieh", "hochland", "steppe", "pilz"]

_COMMON = {
    "eye": ramp("#120a06", "#5a3a22"), "hoof": ramp("#16110e", "#4e443c"), "udder": ramp("#9a6458", "#f0b8a8"),
    "mush_stem": ramp("#b8ab98", "#f2ece0"), "mush_red": ramp("#7a1410", "#e8483a"), "mush_brown": ramp("#5a3a24", "#b88a5e"),
    "mush_dot": ramp("#d8d0c8", "#ffffff"),
}

PALETTES = {
    "fleckvieh": {**_COMMON,
                  "fur": ramp("#b4a48c", "#fcf8ee"), "belly": ramp("#c8bca8", "#ffffff"), "patch": ramp("#5a2610", "#c06a36"),
                  "nose": ramp("#6a463c", "#e8b8a6"), "horn": ramp("#8a7a62", "#f4ead2"), "horn_tip": ramp("#3a3026", "#8a7a62"),
                  "tuft": ramp("#5a2610", "#c06a36"), "face": ramp("#c8bca8", "#ffffff")},
    "braunvieh": {**_COMMON,
                  "fur": ramp("#3e322a", "#a89a8a"), "belly": ramp("#6a5e54", "#c8bcae"), "patch": ramp("#2e241e", "#7a6a5c"),
                  "nose": ramp("#16161a", "#56565e"), "ring": ramp("#b0a490", "#f2ead8"), "horn": ramp("#9a8c74", "#f6eedc"),
                  "horn_tip": ramp("#0e0c0a", "#3a342e"), "tuft": ramp("#16120e", "#4a3e34"), "face": ramp("#3e322a", "#a89a8a")},
    "hochland": {**_COMMON,
                 "fur": ramp("#3e1606", "#e08848"), "belly": ramp("#5a2a10", "#c87a44"), "patch": ramp("#3e1606", "#a85a2a"),
                 "nose": ramp("#2a1a16", "#7a5a50"), "horn": ramp("#a89a7a", "#fbf2dc"), "horn_tip": ramp("#2a221c", "#7a6a56"),
                 "tuft": ramp("#3e1606", "#b0602e"), "face": ramp("#3e1606", "#e08848")},
    "steppe": {**_COMMON,
               "fur": ramp("#78746c", "#f4f2ea"), "belly": ramp("#a8a49c", "#ffffff"), "patch": ramp("#3a3836", "#8a8680"),
               "nose": ramp("#121214", "#4a4a4e"), "horn": ramp("#9c9078", "#f8f2e2"), "horn_tip": ramp("#1a1612", "#5a5046"),
               "tuft": ramp("#1a1816", "#4a4642"), "face": ramp("#78746c", "#f4f2ea")},
    "pilz": {**_COMMON,
             "fur": ramp("#3e3038", "#a8929c"), "belly": ramp("#6a5a62", "#c8b8be"), "patch": ramp("#b8aeb0", "#f6f0f0"),
             "nose": ramp("#3a2a30", "#9a7a84"), "horn": ramp("#8a7e6c", "#ece2cc"), "horn_tip": ramp("#3a3228", "#7a6e5c"),
             "tuft": ramp("#2a2026", "#6a5a62"), "face": ramp("#3e3038", "#a8929c")},
}

for _p in PALETTES.values():
    _p["back"] = _p["fur"]


def build() -> Model:
    m = Model("cattle", 128, 128)
    m.bone("body", pivot=(0, 10, 0))
    m.cube("body", (-6, -5, -10), (12, 10, 20), "fur")
    m.cube("body", (-4.5, -6.5, -9.5), (9, 2, 7), "hump")                # Widerrist / Buckel
    m.cube("body", (-4, -4.5, -13), (8, 7.5, 3.5), "neck")               # kräftiger Hals
    m.cube("body", (-1.5, 3, -13.5), (3, 4, 5), "dewlap")                # Wamme unter Hals und Brust
    m.cube("body", (-2.5, 5, 2.5), (5, 1.5, 5), "udder")                 # Euter

    m.bone("head", "body", pivot=(0, -1, -12.5), rotation=(14, 0, 0))
    m.cube("head", (-4, -4.5, -6.5), (8, 7.5, 6.5), "face")              # Stirn und Schädel
    m.cube("head", (-3.25, 0, -10.5), (6.5, 5, 4), "muzzle")             # Maul
    m.cube("head", (4, -1, -5.5), (0.5, 1, 1), "eye")
    m.cube("head", (-4.5, -1, -5.5), (0.5, 1, 1), "eye")
    for side in (1, -1):
        sfx = "l" if side > 0 else "r"
        m.bone("ear_" + sfx, "head", pivot=(4.0 * side, -3.0, -1.8), rotation=(-10, 0, 12 * side))
        m.cube("ear_" + sfx, (0 if side > 0 else -3, -0.75, -1), (3, 1.5, 2), "ear")
        # kurze Hörner (Fleckvieh, Braunvieh)
        m.bone("horn_short_" + sfx, "head", pivot=(3.6 * side, -4.0, -3.5), rotation=(0, 0, -20 * side))
        m.cube("horn_short_" + sfx, (0 if side > 0 else -2.5, -0.75, -0.75), (2.5, 1.5, 1.5), "horn")
        m.bone("horn_short_tip_" + sfx, "horn_short_" + sfx, pivot=(2.2 * side, 0, 0), rotation=(0, 0, -50 * side))
        m.cube("horn_short_tip_" + sfx, (0 if side > 0 else -1.5, -0.5, -0.5), (1.5, 1, 1), "horn", tag="tip")
        # ausladende Hörner (Hochlandrind): weit zur Seite, dann nach vorne oben
        m.bone("horn_long_" + sfx, "head", pivot=(3.6 * side, -3.8, -3.5), rotation=(0, 0, 8 * side))
        m.cube("horn_long_" + sfx, (0 if side > 0 else -4.5, -0.9, -0.9), (4.5, 1.8, 1.8), "horn")
        m.bone("horn_long_mid_" + sfx, "horn_long_" + sfx, pivot=(4.2 * side, 0, 0), rotation=(-20, 25 * side, -40 * side))
        m.cube("horn_long_mid_" + sfx, (0 if side > 0 else -3.5, -0.7, -0.7), (3.5, 1.4, 1.4), "horn")
        m.bone("horn_long_tip_" + sfx, "horn_long_mid_" + sfx, pivot=(3.3 * side, 0, 0), rotation=(0, 15 * side, -35 * side))
        m.cube("horn_long_tip_" + sfx, (0 if side > 0 else -2.5, -0.5, -0.5), (2.5, 1, 1), "horn", tag="tip")
        # Leierhörner (Steppenrind): steil nach oben, außen geschwungen
        m.bone("horn_lyre_" + sfx, "head", pivot=(3.4 * side, -4.0, -3.5), rotation=(-10, 0, 35 * side))
        m.cube("horn_lyre_" + sfx, (-0.8, -4, -0.8), (1.6, 4, 1.6), "horn")
        m.bone("horn_lyre_mid_" + sfx, "horn_lyre_" + sfx, pivot=(0, -3.8, 0), rotation=(0, 0, -45 * side))
        m.cube("horn_lyre_mid_" + sfx, (-0.65, -3.5, -0.65), (1.3, 3.5, 1.3), "horn")
        m.bone("horn_lyre_tip_" + sfx, "horn_lyre_mid_" + sfx, pivot=(0, -3.3, 0), rotation=(0, 0, 40 * side))
        m.cube("horn_lyre_tip_" + sfx, (-0.5, -2.5, -0.5), (1, 2.5, 1), "horn", tag="tip")
    # Stirnschopf des Hochlandrinds (verdeckt die Augen halb)
    m.bone("fringe", "head", pivot=(0, -4.5, -6.5))
    m.cube("fringe", (-4.25, 0, -0.6), (8.5, 3.5, 1), "shag")
    m.cube("fringe", (-3.0, -0.8, -0.2), (6, 1.5, 3), "shag")

    # Pilze auf dem Rücken des Pilzrinds
    m.bone("mushrooms", "body", pivot=(0, -5, 0))
    for i, (x, z, red) in enumerate(((-2.5, -4.5, True), (2.0, 1.0, False), (-1.0, 6.0, True), (3.0, -7.0, True))):
        m.cube("mushrooms", (x - 0.4, -1.6, z - 0.4), (0.8, 1.6, 0.8), "mush_stem")
        m.cube("mushrooms", (x - 1.2, -2.6, z - 1.2), (2.4, 1, 2.4), "mush_red" if red else "mush_brown")

    for name, x, z in (("front_left_leg", 3.5, -7), ("front_right_leg", -3.5, -7),
                       ("hind_left_leg", 3.5, 7), ("hind_right_leg", -3.5, 7)):
        m.bone(name, pivot=(x, 15, z))
        m.cube(name, (-2, 0, -2), (4, 9, 4), "leg")

    m.bone("tail", "body", pivot=(0, -4, 10), rotation=(12, 0, 0))
    m.cube("tail", (-0.5, 0, -0.5), (1, 9, 1), "tail")
    m.cube("tail", (-1, 8, -1), (2, 3, 2), "tuft")
    return m


def _variant(ctx: Ctx) -> str:
    return str(ctx.params.get("variant", "fleckvieh"))


def _body_base(variant: str):
    if variant == "hochland":
        return farm.shaggy("fur", seed=17)
    base = farm.fur("fur", "belly", seed=51, contrast=0.22)
    if variant == "fleckvieh":
        return farm.patched(base, "patch", scale=0.15, threshold=0.55, seed=12)
    if variant == "pilz":
        return farm.patched(base, "patch", scale=0.22, threshold=0.62, seed=19)
    if variant == "steppe":
        # dunklere Schultern und Hals wie bei den alten Steppenrindern
        def paint(ctx: Ctx):
            col = base(ctx)
            x, y, z = ctx.world
            if z < -2:
                t = min(1.0, (-2 - z) / 8.0) * (0.85 if rel_y(ctx) < 0.7 else 0.4)
                col = mix(col, pick(ctx.pal["patch"], 0.5 + noise3(x * 0.4, y * 0.4, z * 0.4, 5) * 0.3), t)
            return col
        return paint
    return base


def _face(variant: str):
    if variant == "hochland":
        return farm.shaggy("fur", seed=18, length=2.0)
    base = farm.fur("face", None, seed=52, contrast=0.18, top_dark=0.06)

    def paint(ctx: Ctx):
        col = base(ctx)
        x, y, z = ctx.world
        if variant == "fleckvieh" and ctx.face in ("west", "east") and abs(y - 9.5) < 2.2 and -16 < z < -12:
            # rotbraune Flecken um die Augen, sonst ein weißer Kopf
            if noise3(x, y * 0.8, z * 0.8, 9) > 0.35:
                return pick(ctx.pal["patch"], 0.55)
        if variant == "steppe" and ctx.face in ("west", "east") and abs(y - 9.0) < 1.6 and -16 < z < -13:
            return pick(ctx.pal["patch"], 0.35)              # dunkle Augenringe
        if variant == "braunvieh" and ctx.face == "top":
            return mix(col, pick(ctx.pal["ring"], 0.5), 0.35)  # heller Schopf
        return col

    return paint


def _hump(variant: str):
    body = _body_base(variant)

    def paint(ctx: Ctx):
        col = body(ctx)
        if variant == "steppe":
            return mix(col, pick(ctx.pal["patch"], 0.45), 0.5)
        return col

    return paint


def _neck(variant: str):
    body = _body_base(variant)

    def paint(ctx: Ctx):
        col = body(ctx)
        if variant == "steppe":
            return mix(col, pick(ctx.pal["patch"], 0.5), 0.55)
        if ctx.face == "bottom":
            return pick(ctx.pal["belly"], 0.5)
        return col

    return paint


def _dewlap(variant: str):
    if variant == "hochland":
        return farm.shaggy("fur", seed=19, length=1.8)
    return farm.fur("belly", None, seed=53, contrast=0.16, top_dark=0.0)


def _udder(ctx: Ctx):
    if ctx.face == "top":
        return None
    v = 0.55 + (hash01(ctx.u, ctx.v) - 0.5) * 0.1
    if ctx.face == "bottom" and (ctx.s % 3 == 1) and (ctx.t % 3 == 1):
        v = 0.3                                              # Zitzen
    return pick(ctx.pal["udder"], v)


def _ear(variant: str):
    def paint(ctx: Ctx):
        if variant == "hochland":
            return farm.shaggy("fur", seed=21, length=1.5)(ctx)
        if ctx.face == "front":
            return pick(ctx.pal["udder"], 0.55)               # rosiges Ohr-Inneres
        key = "patch" if variant == "fleckvieh" else "fur"
        return pick(ctx.pal[key], 0.5 + hash01(ctx.u, ctx.v) * 0.1)

    return paint


def _tail(variant: str):
    def paint(ctx: Ctx):
        if variant == "hochland":
            return farm.shaggy("fur", seed=22, length=1.4)(ctx)
        return pick(ctx.pal["fur" if variant != "fleckvieh" else "patch"], 0.48 + hash01(ctx.u, ctx.v) * 0.1)

    return paint


def _mush_cap(key: str):
    def paint(ctx: Ctx):
        if key == "mush_red" and ctx.face == "top" and (ctx.s + ctx.t) % 3 == 0 and 0 < ctx.s < ctx.w - 1:
            return pick(ctx.pal["mush_dot"], 0.7)            # weiße Tupfen auf dem roten Hut
        if ctx.face == "bottom":
            return pick(ctx.pal["mush_stem"], 0.4)
        return pick(ctx.pal[key], 0.5 + hash01(ctx.u, ctx.v) * 0.12 + (0.12 if ctx.face == "top" else 0.0))

    return paint


def materials(variant: str) -> dict:
    ring = "ring" if variant == "braunvieh" else None
    return {
        "fur": _body_base(variant),
        "hump": _hump(variant),
        "face": _face(variant),
        "dewlap": _dewlap(variant),
        "neck": _neck(variant),
        "udder": _udder,
        "muzzle": farm.muzzle("nose", "face", ring_key=ring),
        "eye": eye_painter("eye", glow=False),
        "ear": _ear(variant),
        "horn": farm.horn("horn", "horn_tip"),
        "shag": farm.shaggy("fur", seed=23, length=1.6),
        "leg": farm.leg("fur" if variant != "fleckvieh" else "belly", "hoof", seed=24)
        if variant not in ("hochland",) else _shaggy_leg(),
        "tail": _tail(variant),
        "tuft": farm.shaggy("tuft", seed=25, length=1.2),
        "mush_stem": farm.solid("mush_stem", 0.6),
        "mush_red": _mush_cap("mush_red"),
        "mush_brown": _mush_cap("mush_brown"),
    }


def _shaggy_leg():
    shag = farm.shaggy("fur", seed=26, length=1.8)

    def paint(ctx: Ctx):
        if ctx.face == "bottom" or rel_y(ctx) > 0.86:
            return pick(ctx.pal["hoof"], 0.35 + hash01(ctx.u, ctx.v) * 0.2)
        return shag(ctx)

    return paint


def hidden(variant: str) -> list[str]:
    """Welche Knochen bei einer Rasse unsichtbar sind (das Java-Modell blendet sie genauso aus)."""
    horns = {"fleckvieh": "short", "braunvieh": "short", "hochland": "long", "steppe": "lyre", "pilz": "short"}[variant]
    out = [f"horn_{kind}_{side}" for kind in ("short", "long", "lyre") if kind != horns for side in ("l", "r")]
    if variant != "hochland":
        out.append("fringe")
    if variant != "pilz":
        out.append("mushrooms")
    return out
