"""
Kupferknecht (statt Kupfergolem) auf dem Skelett des Vanilla-Kupfergolems: gleiche Knochen und Drehpunkte wie
CopperGolemModel (body -> head, right_arm, left_arm; right_leg, left_leg; die Verschiebung des Vanilla-Netzes um 24
ist in die Drehpunkte eingerechnet), damit Gehen, Tragen, Kopfdrehen und das Öffnen der Truhen genauso laufen.
Eigene Quader:

  * ein kleiner Kupferdiener aus getriebenem Blech: tonnenförmiger Rumpf mit zwei Bändern und einem Zeigerwerk auf
    der Brust, auf dem Rücken ein Aufziehschlüssel (eigener Knochen, dreht sich im Spiel)
  * ein breiter Kopf wie ein Kessel mit umlaufendem Rand, zwei runden Glasaugen, die leuchten, einem Ventil als Nase
    und dem Blitzableiter obendrauf (Stange und Kugel wie bei Vanilla, damit eine Blume darauf Platz hat)
  * Kugelgelenke an den Schultern, Fäustlinge mit drei Fingern, kurze Beine mit Schuhen

Vier Bilder für die vier Stufen der Verwitterung: blankes Kupfer, angelaufen, verwittert (Grünspan breitet sich
aus) und oxidiert (ganz grün). Die Augen leuchten in jeder Stufe.
"""
from __future__ import annotations

import math

from ..geometry import Model
from ..paint import Ctx, hash01, mix, noise3, pick, ramp, rel_y

_BASE = {
    "glass": ramp("#2a1a04", "#fff3b0", steps=8, hue_shift=-0.05),
    "dark": ramp("#050403", "#2a2018"),
    "steel": ramp("#20242a", "#c8ccd2", steps=6),
}

PALETTES = {
    "kupfer": {**_BASE, "copper": ramp("#5e200c", "#ffd4ae", steps=10, hue_shift=-0.04),
               "patina": ramp("#204a3c", "#7fe0c0", steps=7), "stage": [0.0]},
    "angelaufen": {**_BASE, "copper": ramp("#46200f", "#e8a07c", steps=10, hue_shift=-0.03),
                   "patina": ramp("#2a4a3c", "#88c8a8", steps=7), "stage": [0.18]},
    "verwittert": {**_BASE, "copper": ramp("#3a2218", "#c79a82", steps=10, hue_shift=-0.03),
                   "patina": ramp("#1f4438", "#76c9a6", steps=7), "stage": [0.55]},
    "oxidiert": {**_BASE, "copper": ramp("#2c3a30", "#9cbfa8", steps=10),
                 "patina": ramp("#14362e", "#74dcbc", steps=8), "stage": [0.93]},
}

LOOKS = tuple(PALETTES)
_Y = 24.0                                                                  # Verschiebung des Vanilla-Netzes


def build() -> Model:
    m = Model("copper_golem", 128, 64)

    # ---------------------------------------------------------------- Rumpf (Drehpunkt wie Vanilla)
    m.bone("body", pivot=(0, _Y - 5, 0))
    m.cube("body", (-4, -6, -3), (8, 6, 6), "torso")
    m.cube("body", (-4.3, -5.4, -3.3), (8.6, 1, 6.6), "band")
    m.cube("body", (-4.3, -1.6, -3.3), (8.6, 1, 6.6), "band")
    m.cube("body", (-1.6, -4.3, -3.6), (3.2, 2.6, 0.6), "gauge")              # Zeigerwerk auf der Brust
    m.bone("key", "body", pivot=(0, -3.2, 3))                                 # Aufziehschlüssel (dreht sich)
    m.cube("key", (-0.5, -0.5, 0), (1, 1, 1.6), "key")
    m.cube("key", (-2.2, -1.6, 1.6), (4.4, 3.2, 0.6), "key_bow")

    # ---------------------------------------------------------------- Kopf: Kessel mit Rand, Glasaugen, Ventil
    m.bone("head", "body", pivot=(0, -6, 0))
    m.cube("head", (-4, -5, -5), (8, 5, 10), "kettle")
    m.cube("head", (-4.6, -1.6, -5.6), (9.2, 1.4, 11.2), "rim")
    m.cube("head", (-3.4, -4.1, -5.5), (2.6, 2.6, 0.6), "lens")
    m.cube("head", (0.8, -4.1, -5.5), (2.6, 2.6, 0.6), "lens")
    m.cube("head", (-1, -2, -6), (2, 3, 2), "valve")                          # Nase: Ventil
    m.cube("head", (-1, -9, -1), (2, 4, 2), "rod")                            # Blitzableiter wie bei Vanilla
    m.cube("head", (-2, -13, -2), (4, 4, 4), "knob")
    m.cube("head", (-0.5, -14.6, -0.5), (1, 1.6, 1), "tip")

    # ---------------------------------------------------------------- Arme (Drehpunkte wie Vanilla)
    for side, sx in (("right", -1), ("left", 1)):
        arm = f"{side}_arm"
        m.bone(arm, "body", pivot=(4 * sx, -6, 0))
        x0 = -3.0 if sx < 0 else 0.0
        m.cube(arm, (x0 - 0.3, -1.6, -2.2), (3.6, 3.0, 4.4), "shoulder")
        m.cube(arm, (x0 + 0.2, 1.2, -1.5), (2.6, 4.2, 3.0), "arm")
        m.cube(arm, (x0 - 0.2, 5.2, -2.1), (3.4, 4.2, 4.2), "hand")

    # ---------------------------------------------------------------- Beine (Drehpunkte wie Vanilla)
    for side, x0 in (("right", -4.0), ("left", 0.0)):
        leg = f"{side}_leg"
        m.bone(leg, pivot=(0, _Y - 5, 0))
        m.cube(leg, (x0 + 0.5, 0, -1.6), (3.0, 3.4, 3.2), "leg")
        m.cube(leg, (x0 + 0.1, 3.2, -2.6), (3.8, 1.8, 4.4), "foot")
    return m


# =============================================================================================
# Bemalen
# =============================================================================================

_LIT = {"top": 0.09, "front": 0.04, "west": 0.0, "east": -0.03, "back": -0.06, "bottom": -0.16}


def _stage(ctx: Ctx) -> float:
    return ctx.pal["stage"][0]


def _metal(ctx: Ctx, base: float = 0.55, seed: int = 1, dome: float = 0.08):
    """
    Getriebenes Kupferblech: glänzende Mitte, dunklere Ränder, feine Treibspuren; je nach Stufe breitet sich der
    Grünspan von Kanten, Nieten und Vertiefungen her aus (und als Flecken auf den Flächen).
    """
    x, y, z = ctx.world
    v = base + _LIT[ctx.face]
    if ctx.face not in ("top", "bottom"):
        if ctx.t == 0:
            v += 0.1
        elif ctx.t == ctx.h - 1:
            v -= 0.12
        if ctx.w > 2:
            u = ctx.s / (ctx.w - 1) * 2.0 - 1.0
            v += dome * (1.0 - u * u) - dome * 0.4
    v += (noise3(x * 0.5, y * 0.5, z * 0.5, seed) - 0.5) * 0.1
    if hash01(ctx.u, ctx.v, seed) > 0.92:
        v += 0.08                                                         # Glanzpunkt
    col = pick(ctx.pal["copper"], v)
    return _patina(ctx, col, v)


def _patina(ctx: Ctx, col, v: float, edge_bias: float = 0.0):
    stage = _stage(ctx)
    if stage <= 0.0:
        return col
    x, y, z = ctx.world
    n = noise3(x * 0.42, y * 0.42, z * 0.42, 77)
    edge = 0.0
    if ctx.face not in ("top", "bottom") and (ctx.s in (0, ctx.w - 1) or ctx.t in (0, ctx.h - 1)):
        edge = 0.18                                                       # an Kanten setzt sich der Grünspan zuerst an
    edge += edge_bias
    cover = n + edge - (1.0 - stage) * 1.05
    if cover > 0.0:
        p = pick(ctx.pal["patina"], 0.38 + min(0.5, cover) * 0.6 + (v - 0.5) * 0.6 + (hash01(ctx.u, ctx.v, "p") - 0.5) * 0.12)
        if cover < 0.08:
            return mix(col, p, 0.5)
        return p
    return col


def _torso(ctx: Ctx):
    face, s, t = ctx.face, ctx.s, ctx.t
    if face == "front":
        if t in (1, 4) and s in (0, ctx.w - 1):
            return _rivet(ctx)
        if s in (1, 6) and t in (2, 3):
            return _metal(ctx, 0.4, seed=3, dome=0.0)                     # Nähte neben dem Zeigerwerk
    if face == "back":
        if s in (3, 4) and t in (2, 3):
            return pick(ctx.pal["dark"], 0.6)                             # Loch für den Schlüssel
        if s in (0, ctx.w - 1) and t in (1, 4):
            return _rivet(ctx)
    if face in ("west", "east") and t in (2, 3) and s in (2, 3):
        return _metal(ctx, 0.66, seed=4, dome=0.0)                        # Beule
    return _metal(ctx, 0.56, seed=5, dome=0.1)


def _rivet(ctx: Ctx):
    col = pick(ctx.pal["copper"], 0.86)
    return _patina(ctx, col, 0.86, edge_bias=0.2)


def _band(ctx: Ctx):
    if ctx.face in ("top", "bottom"):
        if 0 < ctx.s < ctx.w - 1 and 0 < ctx.t < ctx.h - 1:
            return None                                                   # nur der Ring ist sichtbar
        return _metal(ctx, 0.5, seed=7, dome=0.0)
    if ctx.s % 3 == 1:
        return _rivet(ctx)
    return _metal(ctx, 0.66, seed=6, dome=0.0)


def _gauge(ctx: Ctx):
    """Zeigerwerk: Messingrand, helles Zifferblatt mit Strichen und einem Zeiger."""
    face, s, t = ctx.face, ctx.s, ctx.t
    if face != "front":
        return _metal(ctx, 0.45, seed=8, dome=0.0)
    if s in (0, ctx.w - 1) or t in (0, ctx.h - 1):
        return _metal(ctx, 0.72, seed=8, dome=0.0)
    if (s, t) == (1, 1):
        return pick(ctx.pal["dark"], 0.7)                                 # Zeiger
    return pick(ctx.pal["glass"], 0.72 if (s + t) % 2 else 0.66)


def _key(ctx: Ctx):
    return pick(ctx.pal["steel"], 0.55 + _LIT[ctx.face])


def _key_bow(ctx: Ctx):
    """Griff des Schlüssels: zwei Ösen (in der Mitte durchsichtig)."""
    if ctx.face in ("front", "back"):
        cx = (ctx.w - 1) / 2
        dx = abs(ctx.s - cx)
        if dx < 0.6:
            return pick(ctx.pal["steel"], 0.5)
        if ctx.t == 1 and 0.6 < dx < ctx.w / 2 - 1:
            return None                                                   # Ösen
        return pick(ctx.pal["steel"], 0.66 if ctx.t == 0 else 0.5)
    return pick(ctx.pal["steel"], 0.4 + _LIT[ctx.face])


def _kettle(ctx: Ctx):
    """Kesselkopf: oben gewölbt (Treibspuren in Ringen um die Mitte), vorn glatt für die Augen, hinten eine Naht."""
    face, s, t = ctx.face, ctx.s, ctx.t
    if face == "top":
        x, y, z = ctx.world
        r = math.hypot(x, (z + 0.0) * 0.8)
        v = 0.62 - r * 0.02 + (0.05 if int(r * 1.2) % 2 == 0 else 0.0)
        return _patina(ctx, pick(ctx.pal["copper"], v + (hash01(ctx.u, ctx.v) - 0.5) * 0.05), v)
    if face == "front" and t == ctx.h - 1:
        return _metal(ctx, 0.42, seed=9, dome=0.0)
    if face == "back" and s == ctx.w // 2:
        return _metal(ctx, 0.4, seed=10, dome=0.0)                        # Naht
    if face in ("west", "east") and t == 2 and s in (2, 7):
        return _rivet(ctx)
    return _metal(ctx, 0.57, seed=11, dome=0.1)


def _rim(ctx: Ctx):
    if ctx.face == "top":
        if 0 < ctx.s < ctx.w - 1 and 0 < ctx.t < ctx.h - 1:
            return None
        return _metal(ctx, 0.7, seed=12, dome=0.0)
    if ctx.face == "bottom":
        return pick(ctx.pal["copper"], 0.25)
    return _metal(ctx, 0.6 - ctx.t * 0.15, seed=13, dome=0.0)


def _lens(ctx: Ctx):
    """Rundes Glasauge: dunkle Fassung in den Ecken, hell leuchtende Mitte mit Glanzpunkt."""
    if ctx.face != "front":
        return _metal(ctx, 0.5, seed=14, dome=0.0)
    corner = ctx.s in (0, ctx.w - 1) and ctx.t in (0, ctx.h - 1)
    edge = ctx.s in (0, ctx.w - 1) or ctx.t in (0, ctx.h - 1)
    if corner:
        col = pick(ctx.pal["glass"], 0.5)                                 # weiche, runde Ecken statt eines Kreuzes
        ctx.glow = pick(ctx.pal["glass"], 0.46)
        return col
    v = 0.66 if edge else 0.98
    if edge and ctx.t == 0:
        v = 0.78
    col = pick(ctx.pal["glass"], v)
    ctx.glow = col
    return col


def _valve(ctx: Ctx):
    if ctx.face == "front":
        if (ctx.s, ctx.t) in ((0, 1), (1, 1)):
            return pick(ctx.pal["dark"], 0.55)                            # Öffnung
        return _metal(ctx, 0.66, seed=15, dome=0.0)
    if ctx.face == "bottom":
        return pick(ctx.pal["copper"], 0.3)
    return _metal(ctx, 0.5 + (0.1 if ctx.t == 0 else 0.0), seed=15, dome=0.0)


def _rod(ctx: Ctx):
    v = 0.5 + _LIT[ctx.face] + (0.08 if ctx.t % 2 == 0 else -0.04)
    return _patina(ctx, pick(ctx.pal["copper"], v), v)


def _knob(ctx: Ctx):
    """Kugel des Blitzableiters: rund schattiert mit Glanzpunkt oben links."""
    if ctx.face == "top":
        return _metal(ctx, 0.7, seed=16, dome=0.0)
    if ctx.face == "bottom":
        return pick(ctx.pal["copper"], 0.3)
    v = 0.6 - abs(ctx.s - 1.5) * 0.06 - rel_y(ctx) * 0.2
    if (ctx.s, ctx.t) == (1, 1):
        v += 0.25
    return _patina(ctx, pick(ctx.pal["copper"], v + _LIT[ctx.face]), v)


def _tip(ctx: Ctx):
    return pick(ctx.pal["steel"], 0.7 + _LIT[ctx.face])


def _shoulder(ctx: Ctx):
    return _metal(ctx, 0.58, seed=17, dome=0.12)


def _arm(ctx: Ctx):
    if ctx.face in ("top", "bottom"):
        return pick(ctx.pal["copper"], 0.35)
    v = 0.48 + (0.08 if ctx.t % 2 == 0 else 0.0)                         # Ringe wie ein Balg
    return _patina(ctx, pick(ctx.pal["copper"], v + _LIT[ctx.face]), v)


def _hand(ctx: Ctx):
    """Fäustling mit drei Fingern (Kerben vorn und unten), Daumen innen."""
    face, s, t = ctx.face, ctx.s, ctx.t
    if face == "bottom":
        return pick(ctx.pal["copper"], 0.42 if s % 2 == 0 else 0.26)
    if face == "front" and t >= 2 and s in (1, 2):
        return _metal(ctx, 0.38, seed=18, dome=0.0)                       # Kerben zwischen den Fingern
    if t == 0:
        return _metal(ctx, 0.7, seed=18, dome=0.0)
    return _metal(ctx, 0.55, seed=19, dome=0.1)


def _leg(ctx: Ctx):
    if ctx.face in ("top", "bottom"):
        return pick(ctx.pal["copper"], 0.35)
    return _metal(ctx, 0.5 - ctx.t * 0.03, seed=20, dome=0.1)


def _foot(ctx: Ctx):
    if ctx.face == "bottom":
        return _metal(ctx, 0.3, seed=23, dome=0.0)                      # Sohle (bei der sitzenden Statue zu sehen)
    if ctx.face == "top":
        return _metal(ctx, 0.6, seed=21, dome=0.0)
    if ctx.t == ctx.h - 1:
        return pick(ctx.pal["copper"], 0.22)
    return _metal(ctx, 0.52, seed=22, dome=0.0)


def materials(variant: str) -> dict:
    return {
        "torso": _torso, "band": _band, "gauge": _gauge, "key": _key, "key_bow": _key_bow, "kettle": _kettle, "rim": _rim,
        "lens": _lens, "valve": _valve, "rod": _rod, "knob": _knob, "tip": _tip, "shoulder": _shoulder, "arm": _arm,
        "hand": _hand, "leg": _leg, "foot": _foot,
    }


def hidden(variant: str) -> list[str]:
    return []


def preview_pose(variant: str) -> dict:
    return {}
