"""
Wichtel (statt Hilfsgeist) auf dem Skelett des Vanilla-Hilfsgeists: gleiche Knochen und Drehpunkte wie AllayModel
(root -> head, body -> right/left_arm, right/left_wing), damit Schweben, Flügelschlag, Tanzen, Drehen und das Tragen
von Gegenständen genauso laufen. Eigene Quader:

  * ein kleiner Hausgeist mit großen, leuchtenden Augen, Stupsnase, roten Wangen und spitzen Ohren
  * eine Zipfelmütze (der Zipfel ist ein eigener, nach hinten geknickter Knochen) – je Gestalt anders
  * ein Kittel mit Gürtel und ein Rock aus Blättern mit gezacktem Saum
  * zarte, durchscheinende Flügel wie bei einer Libelle mit Adern und farbigen Spitzen

Vier Gestalten: Mooswichtel (Moosmütze mit Blüten), Pilzwichtel (Fliegenpilz-Mütze), Laubwichtel (Herbstlaub) und
Sternwichtel (nachtblau mit goldenen Sternen).
"""
from __future__ import annotations

import math

from ..geometry import Model
from ..paint import Ctx, hash01, mix, noise3, pick, ramp, rel_y

_BASE = {
    "skin": ramp("#9a6a58", "#fff0e4", steps=7, hue_shift=-0.03),
    "cheek": ramp("#b84a4a", "#ff9a8a", steps=4),
    "eye": ramp("#0a3a5a", "#d8fbff", steps=7, hue_shift=0.04),
    "white": ramp("#b8b4ae", "#ffffff", steps=5),
    "hair": ramp("#3a1e0c", "#c88a4a", steps=6),
}

PALETTES = {
    "mooswichtel": {**_BASE, "cap": ramp("#18300c", "#8fcf4a", steps=7), "tunic": ramp("#2a3a14", "#9ab85a", steps=6),
                    "leaf": ramp("#1a3a10", "#7ac04a", steps=6), "wing": ramp("#6aa07a", "#f0fff0", steps=6),
                    "accent": ramp("#d8b828", "#fff8b8", steps=4)},
    "pilzwichtel": {**_BASE, "cap": ramp("#5a0a08", "#f2483a", steps=7, hue_shift=-0.03),
                    "tunic": ramp("#3a2414", "#b08458", steps=6), "leaf": ramp("#3a3010", "#b8a050", steps=6),
                    "wing": ramp("#a0a0b0", "#ffffff", steps=6), "accent": ramp("#d8d4cc", "#ffffff", steps=4)},
    "laubwichtel": {**_BASE, "cap": ramp("#5a1e06", "#f09a30", steps=7, hue_shift=-0.03),
                    "tunic": ramp("#4a2a10", "#c88a48", steps=6), "leaf": ramp("#5a2008", "#e8a038", steps=6),
                    "wing": ramp("#b09060", "#fff4e0", steps=6), "accent": ramp("#c8301a", "#ffb060", steps=4)},
    "sternwichtel": {**_BASE, "cap": ramp("#0a0e30", "#4a62c8", steps=7), "tunic": ramp("#141a40", "#5a6ab8", steps=6),
                     "leaf": ramp("#101a3a", "#4a5aa8", steps=6), "wing": ramp("#7a8ad0", "#f4f6ff", steps=6),
                     "accent": ramp("#c89a20", "#fff2a0", steps=4)},
}

LOOKS = tuple(PALETTES)


def build() -> Model:
    m = Model("allay", 64, 64)
    m.bone("allay_root", pivot=(0, 23.5, 0))                                  # heißt im Spiel "root" (wie Vanilla)

    # ---------------------------------------------------------------- Kopf (Drehpunkt wie Vanilla)
    m.bone("head", "allay_root", pivot=(0, -3.99, 0))
    m.cube("head", (-2.5, -5, -2.5), (5, 5, 5), "face")
    m.cube("head", (-0.4, -2.4, -2.95), (0.8, 0.8, 0.6), "nose")
    m.cube("head", (-3.1, -3.4, -0.6), (0.7, 1.4, 1.2), "ear")
    m.cube("head", (2.4, -3.4, -0.6), (0.7, 1.4, 1.2), "ear")
    m.cube("head", (-3.0, -5.7, -3.0), (6, 1.3, 6), "cap_brim")
    m.bone("cap_cone", "head", pivot=(0, 0, 0))                               # Zipfelmütze (nicht beim Pilzwichtel)
    m.cube("cap_cone", (-2.3, -7.2, -2.1), (4.6, 1.6, 4.6), "cap")
    m.cube("cap_cone", (-1.5, -8.6, -1.0), (3.0, 1.5, 3.0), "cap")
    m.bone("toadstool", "head", pivot=(0, 0, 0))                              # Fliegenpilz-Hut (nur beim Pilzwichtel)
    m.cube("toadstool", (-3.7, -6.9, -3.7), (7.4, 1.4, 7.4), "cap")
    m.cube("toadstool", (-2.8, -8.0, -2.8), (5.6, 1.2, 5.6), "cap")
    m.cube("toadstool", (-1.6, -8.7, -1.6), (3.2, 0.8, 3.2), "cap")
    m.bone("cap_tip", "cap_cone", pivot=(0, -8.4, 0.5), rotation=(-48, 0, 0))
    m.cube("cap_tip", (-0.75, -2.2, -0.75), (1.5, 2.3, 1.5), "cap")
    m.cube("cap_tip", (-0.6, -3.1, -0.6), (1.2, 1.0, 1.2), "tassel")

    # ---------------------------------------------------------------- Rumpf mit Armen und Flügeln
    m.bone("body", "allay_root", pivot=(0, -4, 0))
    m.cube("body", (-1.5, 0, -1), (3, 4, 2), "tunic")
    m.cube("body", (-1.9, 2.6, -1.4), (3.8, 2.9, 2.8), "skirt")
    for side, sx in (("right", -1), ("left", 1)):
        arm = f"{side}_arm"
        m.bone(arm, "body", pivot=(1.75 * sx, 0.5, 0))
        m.cube(arm, (-0.75 if sx < 0 else -0.25, -0.5, -1), (1, 3.2, 2), "sleeve", inflate=-0.01)
        m.cube(arm, (-0.75 if sx < 0 else -0.25, 2.7, -0.9), (1, 0.9, 1.8), "hand", inflate=-0.01)
        wing = f"{side}_wing"
        m.bone(wing, "body", pivot=(0.5 * sx, 0, 0.6))
        m.cube(wing, (0, 0.6, 0), (0, 6, 9), "wing")
    m.bones["allay_root"].name = "root"
    return m


# =============================================================================================
# Bemalen
# =============================================================================================

_LIT = {"top": 0.1, "front": 0.04, "west": 0.0, "east": -0.03, "back": -0.06, "bottom": -0.16}


def _look(ctx: Ctx) -> str:
    return ctx.params.get("variant", "mooswichtel")


def _face(ctx: Ctx):
    """Gesicht: zwei große leuchtende Augen mit Glanzpunkt, rote Wangen, kleiner Mund; Haarbüschel an Seiten und Hinterkopf."""
    face, s, t = ctx.face, ctx.s, ctx.t
    if face == "front":
        if t in (2, 3) and s in (0, 1, 3, 4):
            inner = s in (1, 3)
            if t == 2:
                col = pick(ctx.pal["eye"], 0.98 if (s in (0, 3)) else 0.8)  # Glanzpunkt oben außen
            else:
                col = pick(ctx.pal["eye"], 0.55 if inner else 0.42)
            ctx.glow = col
            return col
        if t == 4 and s in (0, 4):
            return pick(ctx.pal["cheek"], 0.6)                            # Wangen
        if t == 4 and s == 2:
            return pick(ctx.pal["cheek"], 0.2)                            # Mund
        if t == 0:
            return pick(ctx.pal["hair"], 0.55 if s % 2 else 0.4)          # Stirnfransen unter der Mütze
        return pick(ctx.pal["skin"], 0.74 - t * 0.02 + (hash01(ctx.u, ctx.v) - 0.5) * 0.04)
    if face in ("west", "east", "back") and (t <= 1 or (face == "back" and t <= 3)):
        strand = hash01(s, face, "hair")
        return pick(ctx.pal["hair"], 0.4 + strand * 0.22 - t * 0.04 + _LIT[face])
    v = 0.66 + _LIT[face] - (rel_y(ctx) * 0.08 if face not in ("top", "bottom") else 0.0)
    return pick(ctx.pal["skin"], v + (hash01(ctx.u, ctx.v) - 0.5) * 0.04)


def _nose(ctx: Ctx):
    return pick(ctx.pal["skin"], 0.8 if ctx.face in ("front", "top") else 0.6)


def _ear(ctx: Ctx):
    if ctx.face in ("west", "east") and ctx.t == 0 and ctx.s == 0:
        return None                                                       # spitz zulaufend
    return pick(ctx.pal["skin"], 0.62 + _LIT[ctx.face] + (0.1 if ctx.t == 0 else 0.0))


def _cap_paint(ctx: Ctx, base: float):
    look = _look(ctx)
    x, y, z = ctx.world
    v = base + _LIT[ctx.face] + (noise3(x * 0.8, y * 0.8, z * 0.8, 3) - 0.5) * 0.12
    if look == "pilzwichtel":
        # Fliegenpilz: weiße Tupfen auf Rot
        cell = (math.floor(x * 0.9 + 0.3), math.floor(y * 0.9), math.floor(z * 0.9 + 0.6))
        if hash01(*cell, 11) > 0.72 and ctx.face != "bottom":
            return pick(ctx.pal["accent"], 0.75)
    elif look == "mooswichtel":
        if hash01(ctx.u, ctx.v, "bloom") > 0.93 and ctx.face in ("top", "front", "west", "east"):
            return pick(ctx.pal["accent"], 0.8)                           # kleine Blüten im Moos
        v += (hash01(ctx.u, ctx.v, "moss") - 0.5) * 0.16
    elif look == "laubwichtel":
        if (ctx.s + ctx.t) % 3 == 0:
            v -= 0.1                                                      # Blattadern
        if hash01(ctx.u, ctx.v, "leaf") > 0.85:
            return pick(ctx.pal["accent"], 0.5)
    elif look == "sternwichtel":
        if hash01(ctx.u, ctx.v, "star") > 0.92 and ctx.face != "bottom":
            col = pick(ctx.pal["accent"], 0.85)
            ctx.glow = col
            return col
    return pick(ctx.pal["cap"], v)


def _cap(ctx: Ctx):
    if ctx.face == "bottom":
        return pick(ctx.pal["cap"], 0.25)
    return _cap_paint(ctx, 0.56 - rel_y(ctx) * 0.08 if ctx.face not in ("top",) else 0.62)


def _cap_brim(ctx: Ctx):
    if ctx.face == "bottom":
        if 0 < ctx.s < ctx.w - 1 and 0 < ctx.t < ctx.h - 1:
            return None                                                   # der Kopf steckt darin
        return pick(ctx.pal["cap"], 0.25)
    if ctx.face == "top":
        return _cap_paint(ctx, 0.58)
    if _look(ctx) == "pilzwichtel" and ctx.t == ctx.h - 1:
        return pick(ctx.pal["accent"], 0.55)                              # helle Lamellen unter dem Hutrand
    return _cap_paint(ctx, 0.5 - ctx.t * 0.08)


def _tassel(ctx: Ctx):
    look = _look(ctx)
    if look == "sternwichtel":
        col = pick(ctx.pal["accent"], 0.9)
        ctx.glow = col
        return col                                                        # Stern an der Spitze
    if look == "pilzwichtel":
        return pick(ctx.pal["accent"], 0.7)
    return pick(ctx.pal["leaf"], 0.66 + _LIT[ctx.face])


def _tunic(ctx: Ctx):
    face, s, t = ctx.face, ctx.s, ctx.t
    if t == 2 and face not in ("top", "bottom"):
        if face == "front" and s == 1:
            return pick(ctx.pal["accent"], 0.7)                           # Gürtelschnalle
        return pick(ctx.pal["tunic"], 0.22)                               # Gürtel
    if face == "front" and s == 1 and t in (0, 1):
        return pick(ctx.pal["tunic"], 0.36)                               # Knopfleiste
    return pick(ctx.pal["tunic"], 0.56 + _LIT[face] + (hash01(ctx.u, ctx.v) - 0.5) * 0.08)


def _skirt(ctx: Ctx):
    """Rock aus Blättern: überlappende Blattspitzen, gezackter (durchsichtiger) Saum."""
    face, s, t = ctx.face, ctx.s, ctx.t
    if face == "top":
        return pick(ctx.pal["leaf"], 0.35)
    if face == "bottom":
        return None
    if t == ctx.h - 1 and s % 2 == 1:
        return None                                                       # Zacken
    v = 0.56 + _LIT[face] - t * 0.06 + (0.1 if (s + t) % 2 == 0 else 0.0)
    if t == 0:
        v -= 0.12
    return pick(ctx.pal["leaf"], v + (hash01(ctx.u, ctx.v) - 0.5) * 0.06)


def _sleeve(ctx: Ctx):
    return pick(ctx.pal["tunic"], 0.5 + _LIT[ctx.face] - rel_y(ctx) * 0.1)


def _hand(ctx: Ctx):
    return pick(ctx.pal["skin"], 0.66 + _LIT[ctx.face])


_WING = ["..######.",
         "#########",
         ".#######.",
         ".#######.",
         "########.",
         "..####..."]


def _wing(ctx: Ctx):
    """
    Libellenflügel, oben der schmale Vorderflügel, darunter der Hinterflügel: durchscheinend, mit dunkler Vorderkante,
    Adern am Ansatz, feinem Zellnetz und einem farbigen Flügelmal an der Spitze des Vorderflügels.
    """
    if ctx.face not in ("west", "east"):
        return None
    lx, ly, lz = ctx.local
    col_i = min(8, max(0, int(math.floor(lz))))                           # 0 am Ansatz … 8 an der Spitze
    row = min(5, max(0, int(math.floor(ly - 0.6))))
    if _WING[row][col_i] != "#":
        return None
    lead = row in (0, 3) or (row == 1 and _WING[0][col_i] != "#") or (row == 4 and _WING[3][col_i] != "#")
    if col_i == 0 or (lead and col_i < 7):
        c = pick(ctx.pal["wing"], 0.22)
        return c[0], c[1], c[2], 225                                      # Adern
    if row == 1 and col_i >= 6:
        c = pick(ctx.pal["cap"], 0.62)
        return c[0], c[1], c[2], 230                                      # Flügelmal
    if row in (2, 5) and col_i >= 5:
        c = pick(ctx.pal["cap"], 0.7)
        return c[0], c[1], c[2], 150                                      # farbiger Saum
    cell = (col_i + row) % 2
    c = pick(ctx.pal["wing"], 0.72 + cell * 0.1 + (hash01(ctx.u, ctx.v) - 0.5) * 0.06)
    return c[0], c[1], c[2], 135


def materials(variant: str) -> dict:
    return {
        "face": _face, "nose": _nose, "ear": _ear, "cap": _cap, "cap_brim": _cap_brim, "tassel": _tassel,
        "tunic": _tunic, "skirt": _skirt, "sleeve": _sleeve, "hand": _hand, "wing": _wing,
    }


def hidden(variant: str) -> list[str]:
    return ["cap_cone"] if variant == "pilzwichtel" else ["toadstool"]


def preview_pose(variant: str) -> dict:
    return {"right_wing": (25, -45, 0), "left_wing": (25, 45, 0)}
