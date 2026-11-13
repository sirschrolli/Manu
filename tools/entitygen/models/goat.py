"""
Ziegen der Berge: schlanker Körper auf langen Beinen, aufrechter Hals, Bart, kurzer Stummelschwanz. Vier Arten mit
eigenen Hörnern und Fellen (Hörner können wie bei Vanilla einzeln abbrechen, das Java-Modell blendet sie aus):

  * Steinbock    – graubraun, mächtige, nach hinten geschwungene Hörner mit Knoten (Felsgipfel)
  * Gämse        – dunkelbraun, weißes Gesicht mit schwarzem Zügelstreif, kurze Hakenhörner
  * Schneeziege  – langes weißes Zottelfell mit Bart und "Hosen", kurze schwarze Dolchhörner (Schneehänge)
  * Hausziege    – weiß mit braunen Platten, Bart, mittellange Säbelhörner (Wiesen)

Knochen-Namen werden in GoatModel (Java) verwendet.
"""
from __future__ import annotations

import math

from ..geometry import Model
from ..paint import Ctx, eye_painter, hash01, mix, noise3, pick, ramp, rel_y
from . import farm

BREEDS = ["steinbock", "gaemse", "schnee", "haus"]

_COMMON = {"eye": ramp("#1a1004", "#c89a2a"), "hoof": ramp("#121010", "#4a4440")}

PALETTES = {
    "goat_steinbock": {**_COMMON, "fur": ramp("#4a3e30", "#b8a888"), "belly": ramp("#8a7c66", "#e8dcc4"),
                       "dark": ramp("#2a2218", "#6a5a46"), "face": ramp("#4a3e30", "#b0a080"),
                       "nose": ramp("#1a1612", "#5a4e44"), "horn": ramp("#3e3428", "#a8987a"),
                       "horn_tip": ramp("#2a241c", "#6a5e4c"), "beard": ramp("#2a2218", "#6a5a46")},
    "goat_gaemse": {**_COMMON, "fur": ramp("#2a1a10", "#8a6440"), "belly": ramp("#6a5038", "#c8a880"),
                    "dark": ramp("#0e0a08", "#3a2a1e"), "face": ramp("#c8c0b0", "#fffaf0"),
                    "nose": ramp("#0e0c0a", "#3a3430"), "horn": ramp("#060608", "#3a3a40"),
                    "horn_tip": ramp("#060608", "#2a2a30"), "beard": ramp("#2a1a10", "#6a4a30")},
    "goat_schnee": {**_COMMON, "fur": ramp("#b8b0a0", "#fffcf2"), "belly": ramp("#c8c0b0", "#ffffff"),
                    "dark": ramp("#a8a090", "#e8e0d0"), "face": ramp("#c0b8a8", "#ffffff"),
                    "nose": ramp("#141214", "#4a4448"), "horn": ramp("#060608", "#34343a"),
                    "horn_tip": ramp("#060608", "#2a2a30"), "beard": ramp("#c0b8a8", "#ffffff")},
    "goat_haus": {**_COMMON, "fur": ramp("#bcb4a6", "#fffcf4"), "belly": ramp("#ccc4b6", "#ffffff"),
                  "dark": ramp("#4a2a14", "#a8683a"), "face": ramp("#bcb4a6", "#fffcf4"),
                  "nose": ramp("#6a4e48", "#d8aaa0"), "horn": ramp("#7a6e5c", "#e8dcc4"),
                  "horn_tip": ramp("#4a4236", "#9a8e78"), "beard": ramp("#bcb4a6", "#fffcf4")},
}
for _p in PALETTES.values():
    _p["back"] = _p["fur"]
    _p["patch"] = _p["dark"]


def build() -> Model:
    m = Model("goat", 128, 64)
    m.bone("body", pivot=(0, 12, 0))
    m.cube("body", (-4, -4, -8), (8, 8, 15), "fur")
    m.cube("body", (-3.5, -4.8, -7.5), (7, 1, 6), "withers")             # Widerrist
    # Zottelfell der Schneeziege: Behang an Bauch und Flanken
    m.bone("skirt", "body", pivot=(0, 0, 0))
    m.cube("skirt", (-4.5, 0.5, -8.2), (9, 5.5, 15.5), "skirt")

    m.bone("head", "body", pivot=(0, -3, -7))
    m.cube("head", (-2, -5.5, -3), (4, 6.5, 4.5), "neck")                # aufrechter Hals
    m.cube("head", (-2.5, -8.5, -5.5), (5, 4.5, 6), "face")              # Schädel
    m.cube("head", (-1.75, -7.2, -8.6), (3.5, 3.2, 3.2), "muzzle")       # Maul
    m.cube("head", (2.5, -7.5, -4.2), (0.3, 1, 1.5), "eye")
    m.cube("head", (-2.8, -7.5, -4.2), (0.3, 1, 1.5), "eye")
    m.bone("beard", "head", pivot=(0, -4.3, -5.4), rotation=(-8, 0, 0))   # Kinnbart (hinter dem Maul)
    m.cube("beard", (-0.75, 0, -1.0), (1.5, 2.6, 1.5), "beard")
    m.bone("beard_long", "head", pivot=(0, -4.3, -5.2), rotation=(-8, 0, 0))
    m.cube("beard_long", (-1.25, 0, -1.3), (2.5, 3.6, 2.1), "beard")

    for side in (1, -1):
        sfx = "l" if side > 0 else "r"
        m.bone("ear_" + sfx, "head", pivot=(2.4 * side, -7.6, -1.8), rotation=(0, -20 * side, 28 * side))
        m.cube("ear_" + sfx, (0 if side > 0 else -3.2, -0.5, -0.8), (3.2, 1, 1.8), "ear")
        x = 1.3 * side
        # Steinbock: mächtige Bögen nach hinten
        m.bone("horn_ibex_" + sfx, "head", pivot=(x, -8.4, -3.6), rotation=(-30, 0, 6 * side))
        m.cube("horn_ibex_" + sfx, (-1.0, -4.6, -1.1), (2, 4.6, 2.2), "horn")
        m.bone("horn_ibex_mid_" + sfx, "horn_ibex_" + sfx, pivot=(0, -4.4, 0), rotation=(-42, 0, 4 * side))
        m.cube("horn_ibex_mid_" + sfx, (-0.85, -4.2, -0.95), (1.7, 4.2, 1.9), "horn")
        m.bone("horn_ibex_tip_" + sfx, "horn_ibex_mid_" + sfx, pivot=(0, -4.0, 0), rotation=(-48, 0, 4 * side))
        m.cube("horn_ibex_tip_" + sfx, (-0.65, -3.6, -0.75), (1.3, 3.6, 1.5), "horn", tag="tip")
        # Gämse: gerade hoch, Spitze hakenförmig nach hinten
        m.bone("horn_chamois_" + sfx, "head", pivot=(x * 0.9, -8.4, -3.8), rotation=(-6, 0, 4 * side))
        m.cube("horn_chamois_" + sfx, (-0.5, -3.4, -0.5), (1, 3.4, 1), "horn")
        m.bone("horn_chamois_tip_" + sfx, "horn_chamois_" + sfx, pivot=(0, -3.3, 0.1), rotation=(-120, 0, 0))
        m.cube("horn_chamois_tip_" + sfx, (-0.45, -1.8, -0.45), (0.9, 1.8, 0.9), "horn", tag="tip")
        # Schneeziege: kurze, leicht gebogene Dolche
        m.bone("horn_snow_" + sfx, "head", pivot=(x, -8.4, -3.6), rotation=(-18, 0, 5 * side))
        m.cube("horn_snow_" + sfx, (-0.6, -3.0, -0.6), (1.2, 3, 1.2), "horn")
        m.bone("horn_snow_tip_" + sfx, "horn_snow_" + sfx, pivot=(0, -2.9, 0), rotation=(-26, 0, 0))
        m.cube("horn_snow_tip_" + sfx, (-0.45, -2.0, -0.45), (0.9, 2, 0.9), "horn", tag="tip")
        # Hausziege: Säbelhörner, nach hinten und etwas nach außen
        m.bone("horn_domestic_" + sfx, "head", pivot=(x, -8.4, -3.6), rotation=(-32, 0, 10 * side))
        m.cube("horn_domestic_" + sfx, (-0.8, -3.6, -0.8), (1.6, 3.6, 1.6), "horn")
        m.bone("horn_domestic_tip_" + sfx, "horn_domestic_" + sfx, pivot=(0, -3.5, 0), rotation=(-42, 0, 8 * side))
        m.cube("horn_domestic_tip_" + sfx, (-0.6, -2.8, -0.6), (1.2, 2.8, 1.2), "horn", tag="tip")

    for name, x, z in (("front_left_leg", 2.5, -5.5), ("front_right_leg", -2.5, -5.5),
                       ("hind_left_leg", 2.5, 4.8), ("hind_right_leg", -2.5, 4.8)):
        m.bone(name, pivot=(x, 16, z))
        m.cube(name, (-1.5, 0, -1.5), (3, 8, 3), "leg")
        m.bone(name + "_pants", name, pivot=(0, 0, 0))                  # Zottelhosen der Schneeziege
        m.cube(name + "_pants", (-1.9, -0.5, -1.9), (3.8, 4.5, 3.8), "pants")

    m.bone("tail", "body", pivot=(0, -3.2, 7), rotation=(-38, 0, 0))
    m.cube("tail", (-1, -3, -0.4), (2, 3.2, 1.4), "tail")
    return m


def _variant(ctx: Ctx) -> str:
    return str(ctx.params.get("variant", "goat_steinbock"))


def _coat(variant: str):
    if variant == "goat_schnee":
        return farm.shaggy("fur", seed=41, length=2.2, contrast=0.24)
    base = farm.fur("fur", "belly", seed=43, contrast=0.22, belly_from=0.7)
    if variant == "goat_haus":
        return farm.patched(base, "dark", scale=0.17, threshold=0.6, seed=44)
    if variant == "goat_gaemse":
        # dunkler Aalstrich auf dem Rücken und dunkle Flanken unten
        def paint(ctx: Ctx):
            col = base(ctx)
            x, y, z = ctx.world
            if ctx.face == "top" and abs(x) < 1.1:
                return pick(ctx.pal["dark"], 0.45 + hash01(ctx.u, ctx.v) * 0.15)
            if ctx.face in ("west", "east") and 0.55 < rel_y(ctx) < 0.75:
                return mix(col, pick(ctx.pal["dark"], 0.5), 0.55)
            return col
        return paint
    if variant == "goat_steinbock":
        # dunkler Brust- und Flankenstreif
        def paint(ctx: Ctx):
            col = base(ctx)
            x, y, z = ctx.world
            if ctx.face in ("west", "east") and 0.6 < rel_y(ctx) < 0.8:
                return mix(col, pick(ctx.pal["dark"], 0.5), 0.5)
            if ctx.face == "front" and rel_y(ctx) > 0.3:
                return mix(col, pick(ctx.pal["dark"], 0.5), 0.6)
            return col
        return paint
    return base


def _face(variant: str):
    fur = farm.fur("face", None, seed=45, contrast=0.16, top_dark=0.0)

    def paint(ctx: Ctx):
        x, y, z = ctx.world
        ry = rel_y(ctx)
        if variant == "goat_gaemse":
            # weißes Gesicht, schwarzer Zügelstreif vom Horn über das Auge zum Maul
            if ctx.face in ("west", "east"):
                d = ctx.cube.size[2]
                rz = (ctx.local[2] - ctx.cube.origin[2]) / max(1e-3, d)       # 0 vorn .. 1 hinten
                band = 0.32 + (1.0 - rz) * 0.42
                if abs(ry - band) < 0.2:
                    return pick(ctx.pal["dark"], 0.35 + hash01(ctx.u, ctx.v) * 0.15)
            if ctx.face == "front" and (ctx.s == 0 or ctx.s == ctx.w - 1) and ry > 0.25:
                return pick(ctx.pal["dark"], 0.4)
            return pick(ctx.pal["face"], 0.62 + (hash01(ctx.u, ctx.v) - 0.5) * 0.12)
        if variant == "goat_steinbock" and ctx.face == "front" and 1 <= ctx.s <= ctx.w - 2:
            return mix(fur(ctx), pick(ctx.pal["dark"], 0.5), 0.55)          # dunkle Stirn
        if variant == "goat_haus":
            return farm.patched(fur, "dark", scale=0.3, threshold=0.66, seed=47)(ctx)
        return fur(ctx)

    return paint


def _muzzle(variant: str):
    base = farm.muzzle("nose", "face", nostrils=True)

    def paint(ctx: Ctx):
        if variant == "goat_gaemse" and ctx.face in ("west", "east") and rel_y(ctx) < 0.45:
            # der Zügelstreif läuft bis zum Mundwinkel aus
            rz = (ctx.local[2] - ctx.cube.origin[2]) / max(1e-3, ctx.cube.size[2])
            if rz > 0.45:
                return pick(ctx.pal["dark"], 0.4 + hash01(ctx.u, ctx.v) * 0.15)
        return base(ctx)

    return paint


def _neck(variant: str):
    coat = _coat(variant)

    def paint(ctx: Ctx):
        if variant == "goat_gaemse" and ctx.face == "front":
            return pick(ctx.pal["face"], 0.5 + hash01(ctx.u, ctx.v) * 0.12)        # helle Kehle
        if variant == "goat_steinbock" and ctx.face == "front":
            return pick(ctx.pal["dark"], 0.45 + hash01(ctx.u, ctx.v) * 0.12)
        return coat(ctx)

    return paint


def _leg(variant: str):
    if variant == "goat_schnee":
        return farm.leg("fur", "hoof", seed=48, hoof_from=0.88)
    if variant == "goat_gaemse":
        return farm.leg("dark", "hoof", seed=48)
    if variant == "goat_steinbock":
        base = farm.leg("fur", "hoof", seed=48)

        def paint(ctx: Ctx):
            if ctx.face == "front" and rel_y(ctx) < 0.86:
                return pick(ctx.pal["dark"], 0.4 + hash01(ctx.u, ctx.v) * 0.12)       # dunkle Laufstreifen
            return base(ctx)
        return paint
    return farm.leg("fur", "hoof", seed=48)


def _ear(variant: str):
    def paint(ctx: Ctx):
        if ctx.face == "bottom":
            return pick(ctx.pal["nose" if variant == "goat_haus" else "belly"], 0.55)
        key = "dark" if variant == "goat_gaemse" else "face"
        return pick(ctx.pal[key], 0.5 + hash01(ctx.u, ctx.v) * 0.12)

    return paint


def materials(variant: str) -> dict:
    coat = _coat(variant)
    return {
        "fur": coat,
        "withers": coat,
        "skirt": farm.shaggy("fur", seed=42, length=1.8, contrast=0.26),
        "pants": farm.shaggy("fur", seed=49, length=1.4, contrast=0.24),
        "neck": _neck(variant),
        "face": _face(variant),
        "muzzle": _muzzle(variant),
        "eye": eye_painter("eye", glow=False),
        "beard": farm.shaggy("beard", seed=46, length=1.2, contrast=0.2),
        "ear": _ear(variant),
        "horn": farm.horn("horn", "horn_tip", rings=0.9 if variant == "goat_steinbock" else 1.4),
        "leg": _leg(variant),
        "tail": farm.solid("dark" if variant != "goat_schnee" else "fur", 0.5, 0.12),
    }


HORNS = {"goat_steinbock": "ibex", "goat_gaemse": "chamois", "goat_schnee": "snow", "goat_haus": "domestic"}


def hidden(variant: str) -> list[str]:
    horns = HORNS[variant]
    out = [f"horn_{kind}_{side}" for kind in ("ibex", "chamois", "snow", "domestic") if kind != horns
           for side in ("l", "r")]
    if variant != "goat_schnee":
        out += ["skirt", "beard_long"] + [n + "_pants" for n in ("front_left_leg", "front_right_leg", "hind_left_leg",
                                                                  "hind_right_leg")]
    else:
        out.append("beard")
    if variant == "goat_gaemse":
        out.append("beard")
    return out
