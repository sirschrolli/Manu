"""
Hasen und Kaninchen: geduckter Körper, kräftige Hinterläufe mit langen Pfoten, lange Löffel, Blume (Schwanz).
Die Rassen ersetzen die Kaninchen-Varianten von Vanilla:

  * Feldhase      – graubraun meliert, schwarze Löffelspitzen, lange Löffel (Wiesen, Wälder)
  * Schneehase    – weiß mit schwarzen Löffelspitzen (Schneelandschaften)
  * Wüstenhase    – sandfarben, sehr große Löffel (Wüste, Savanne)
  * Wildkaninchen – graubraun, kürzere Löffel, weiße Blume (Wälder, Taiga)
  * Schecke       – Hauskaninchen, weiß mit schwarzen Flecken (Wiesen, Kirschhaine, Dörfer)
  * Mordshase     – der berüchtigte weiße Killerhase mit roten Augen (nur per Befehl, wie bei Vanilla)

Knochen-Namen werden in HareModel (Java) verwendet; "ear_long_*" bzw. "ear_short_*" je nach Rasse.
"""
from __future__ import annotations

import math

from ..geometry import Model
from ..paint import Ctx, eye_painter, hash01, mix, noise3, pick, ramp, rel_y
from . import farm

BREEDS = ["feldhase", "schneehase", "wuestenhase", "kaninchen", "schecke", "mordshase"]

_COMMON = {"eye": ramp("#100804", "#a8641a"), "nose": ramp("#5a3a38", "#d8a8a0"), "claw": ramp("#1a1612", "#5a5048"),
           "tip": ramp("#0a0808", "#3a3430"), "inner": ramp("#9a6a64", "#f0c8c0")}

PALETTES = {
    "hare_feldhase": {**_COMMON, "fur": ramp("#4a3624", "#c8a678"), "belly": ramp("#b8ac9c", "#fbf7f0"),
                      "dark": ramp("#2a1e14", "#6a5034")},
    "hare_schneehase": {**_COMMON, "fur": ramp("#c0bcb4", "#ffffff"), "belly": ramp("#d0ccc4", "#ffffff"),
                        "dark": ramp("#a8a49c", "#e8e4dc")},
    "hare_wuestenhase": {**_COMMON, "fur": ramp("#8a6a40", "#f0d8a8"), "belly": ramp("#d0c4ac", "#fffaf0"),
                         "dark": ramp("#5a4428", "#a88a5a")},
    "hare_kaninchen": {**_COMMON, "fur": ramp("#3e3830", "#a89c88"), "belly": ramp("#a8a094", "#f4f0e8"),
                       "dark": ramp("#22201c", "#5a5248")},
    "hare_schecke": {**_COMMON, "fur": ramp("#c4c0b8", "#ffffff"), "belly": ramp("#d0ccc4", "#ffffff"),
                     "dark": ramp("#0c0c0e", "#3a3a40")},
    "hare_mordshase": {**_COMMON, "fur": ramp("#c8c4bc", "#ffffff"), "belly": ramp("#d0ccc4", "#ffffff"),
                       "dark": ramp("#5a0c0c", "#c82a2a"), "eye": ramp("#3a0000", "#ff2a1a")},
}
for _p in PALETTES.values():
    _p["back"] = _p["fur"]


def build() -> Model:
    m = Model("hare", 64, 64)
    m.bone("body", pivot=(0, 19, 2), rotation=(-18, 0, 0))
    m.cube("body", (-2.5, -2.5, -4.5), (5, 5, 8), "fur")
    m.cube("body", (-2.0, -3.0, -1.5), (4, 1, 5), "fur")                # gerundeter Rücken
    m.bone("tail", "body", pivot=(0, -1.2, 3.4), rotation=(-30, 0, 0))
    m.cube("tail", (-1.0, -1.0, -0.5), (2, 2, 1.5), "tail")

    m.bone("head", pivot=(0, 17.1, -1.5))
    m.cube("head", (-2.0, -3.5, -4.0), (4, 3.8, 4.2), "face")
    m.cube("head", (-1.25, -2.0, -5.0), (2.5, 2.0, 1.2), "snout")
    m.cube("head", (2.0, -2.8, -3.0), (0.4, 1.2, 1.4), "eye")
    m.cube("head", (-2.4, -2.8, -3.0), (0.4, 1.2, 1.4), "eye")
    for side in (1, -1):
        sfx = "l" if side > 0 else "r"
        m.bone("ear_long_" + sfx, "head", pivot=(1.0 * side, -3.3, -1.2), rotation=(-22, -8 * side, 10 * side))
        m.cube("ear_long_" + sfx, (-0.9, -7.0, -0.5), (1.8, 7.0, 0.9), "ear")
        m.bone("ear_short_" + sfx, "head", pivot=(1.0 * side, -3.3, -1.2), rotation=(-18, -8 * side, 12 * side))
        m.cube("ear_short_" + sfx, (-0.9, -5.0, -0.5), (1.8, 5.0, 0.9), "ear")

        m.bone("front_leg_" + sfx, pivot=(1.4 * side, 19.5, -2.0))
        m.cube("front_leg_" + sfx, (-0.6, 0, -0.6), (1.2, 4.5, 1.2), "leg")
        m.cube("front_leg_" + sfx, (-0.7, 3.9, -1.4), (1.4, 0.6, 1.6), "paw")
        m.bone("haunch_" + sfx, pivot=(2.4 * side, 19.5, 3.0))
        m.cube("haunch_" + sfx, (-1.0 if side > 0 else -1.0, -2.0, -2.5), (2, 4, 5), "haunch")
        m.bone("hind_foot_" + sfx, "haunch_" + sfx, pivot=(0, 2.0, 1.5))
        m.cube("hind_foot_" + sfx, (-1.0, 2.0, -5.0), (2, 0.9, 6.0), "foot")
    return m


def _variant(ctx: Ctx) -> str:
    return str(ctx.params.get("variant", "hare_feldhase"))


def _coat(variant: str):
    if variant in ("hare_feldhase", "hare_wuestenhase", "hare_kaninchen"):
        base = farm.fur("fur", "belly", seed=61, contrast=0.3, belly_from=0.72)

        def agouti(ctx: Ctx):
            # meliertes Wildfell: einzelne dunkle und helle Haarspitzen
            col = base(ctx)
            if col is None or rel_y(ctx) > 0.7 and ctx.face not in ("top",):
                return col
            h = hash01(ctx.u, ctx.v, 7)
            if h > 0.93:
                return mix(col, pick(ctx.pal["dark"], 0.45), 0.6)
            if h < 0.08:
                return mix(col, pick(ctx.pal["belly"], 0.7), 0.45)
            return col
        return agouti
    base = farm.fur("fur", "belly", seed=62, contrast=0.16, belly_from=0.75, top_dark=0.04)
    if variant == "hare_schecke":
        return farm.patched(base, "dark", scale=0.36, threshold=0.6, seed=63)
    return base


def _face(variant: str):
    coat = _coat(variant)

    def paint(ctx: Ctx):
        if variant == "hare_schecke":
            ry = rel_y(ctx)
            # Schmetterlingszeichnung: dunkle Flügel neben der Nase und dunkle Augenringe, sonst weiß
            if ctx.face == "front" and 0.45 < ry < 0.9 and (ctx.s <= 0 or ctx.s >= ctx.w - 1):
                return pick(ctx.pal["dark"], 0.45 + hash01(ctx.u, ctx.v) * 0.15)
            if ctx.face in ("west", "east"):
                rz = (ctx.local[2] - ctx.cube.origin[2]) / max(1e-3, ctx.cube.size[2])
                if abs(ry - 0.38) < 0.22 and rz < 0.55:
                    return pick(ctx.pal["dark"], 0.45 + hash01(ctx.u, ctx.v) * 0.15)
            return pick(ctx.pal["fur"], 0.62 + (hash01(ctx.u, ctx.v) - 0.5) * 0.12)
        if variant == "hare_mordshase" and ctx.face == "front" and rel_y(ctx) > 0.8:
            return pick(ctx.pal["dark"], 0.55)                              # blutverschmiertes Maul
        if variant in ("hare_feldhase", "hare_wuestenhase") and ctx.face in ("west", "east"):
            ry = rel_y(ctx)
            if 0.25 < ry < 0.45:
                return mix(coat(ctx), pick(ctx.pal["belly"], 0.75), 0.6)     # heller Augenring
        return coat(ctx)

    return paint


def _snout(variant: str):
    coat = _coat(variant)

    def paint(ctx: Ctx):
        if ctx.face == "front" and ctx.t <= 0 and 0 < ctx.s < ctx.w - 1:
            return pick(ctx.pal["nose"], 0.5)
        if ctx.face == "front" and ctx.t == ctx.h - 1 and variant == "hare_mordshase":
            return pick(ctx.pal["dark"], 0.6)
        if variant == "hare_schecke" and ctx.face == "top":
            return pick(ctx.pal["dark"], 0.45)                              # Körper des "Schmetterlings"
        if ctx.face in ("front", "bottom", "west", "east"):
            return pick(ctx.pal["belly"], 0.6 + hash01(ctx.u, ctx.v) * 0.12)
        return coat(ctx)

    return paint


def _ear(variant: str):
    coat = farm.fur("fur", None, seed=64, contrast=0.16, top_dark=0.0)

    def paint(ctx: Ctx):
        ry = rel_y(ctx)
        tipped = variant in ("hare_feldhase", "hare_schneehase", "hare_wuestenhase")
        if tipped and ry < 0.2:
            return pick(ctx.pal["tip"], 0.4 + hash01(ctx.u, ctx.v) * 0.2)          # schwarze Löffelspitze
        if variant == "hare_schecke":
            return pick(ctx.pal["dark"], 0.45 + hash01(ctx.u, ctx.v) * 0.12)
        if ctx.face == "front" and 0.25 < ry < 0.92 and 0 < ctx.s < ctx.w - 1:
            return pick(ctx.pal["inner"], 0.5 + hash01(ctx.u, ctx.v) * 0.15)        # rosa Innenseite
        return coat(ctx)

    return paint


def _tail(variant: str):
    def paint(ctx: Ctx):
        if variant in ("hare_feldhase", "hare_wuestenhase") and ctx.face == "top":
            return pick(ctx.pal["dark"], 0.5 + hash01(ctx.u, ctx.v) * 0.15)         # oben dunkle Blume
        return pick(ctx.pal["belly"], 0.65 + hash01(ctx.u, ctx.v) * 0.15)

    return paint


def _foot(variant: str):
    def paint(ctx: Ctx):
        if ctx.face == "front" and ctx.t == ctx.h - 1:
            return pick(ctx.pal["claw"], 0.5)
        if ctx.face == "bottom":
            return pick(ctx.pal["fur"], 0.35)
        return pick(ctx.pal["belly" if variant != "hare_kaninchen" else "fur"], 0.55 + hash01(ctx.u, ctx.v) * 0.15)

    return paint


def materials(variant: str) -> dict:
    coat = _coat(variant)
    return {
        "fur": coat,
        "haunch": coat,
        "face": _face(variant),
        "snout": _snout(variant),
        "eye": eye_painter("eye", glow=variant == "hare_mordshase"),
        "ear": _ear(variant),
        "tail": _tail(variant),
        "leg": coat,
        "paw": _foot(variant),
        "foot": _foot(variant),
    }


def hidden(variant: str) -> list[str]:
    if variant in ("hare_kaninchen", "hare_schecke", "hare_mordshase"):
        return ["ear_long_l", "ear_long_r"]
    return ["ear_short_l", "ear_short_r"]
