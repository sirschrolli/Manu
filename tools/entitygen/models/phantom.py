"""
Nachtmahr (statt Phantom) auf dem Skelett des Vanilla-Phantoms – gleiche Teile und Drehpunkte, damit Flügelschlag und
Schwanzbewegung der Vanilla-Modellklasse passen. Knochiger Leib mit Wirbelkamm, Schädel mit Hörnern und Fangzähnen,
Flughäute mit Fingerknochen und zerfransten Hinterkanten, am Schwanzende ein Hautsegel. Die Augen leuchten
(Leucht-Textur), beim dunklen Nachtmahr glimmen auch die Fingerknochen auf der Unterseite der Flügel.

Arten:
  * Nachtmahr        – dunkles Indigo, türkis leuchtende Augen
  * bleicher Nachtmahr – fahles Grauviolett, bernsteinfarben leuchtende Augen
"""
from __future__ import annotations

import math

from ..geometry import Model
from ..paint import Ctx, hash01, mix, noise3, ramp

_RAD = 180.0 / math.pi

PALETTES = {
    "nachtmahr": {
        "skin": ramp("#0c0820", "#4c3c8e"), "bone": ramp("#4a4064", "#d6cce8"), "membrane": ramp("#0e0a22", "#46347a"),
        "vein": ramp("#1a1236", "#6a58a8"), "eye": ramp("#0a5a5e", "#a8fff2"), "mouth": ramp("#040208", "#2a1430"),
        "tooth": ramp("#8a8496", "#f4f0fa"), "glow": ramp("#120c3a", "#5a46c8"),
    },
    "nachtmahr_bleich": {
        "skin": ramp("#2e2a36", "#bab0c6"), "bone": ramp("#7a7084", "#f6f2fa"), "membrane": ramp("#28222e", "#8e8298"),
        "vein": ramp("#3e3646", "#a89cb2"), "eye": ramp("#6a3404", "#ffd860"), "mouth": ramp("#0a060a", "#3a2a30"),
        "tooth": ramp("#9a9488", "#fffaf0"), "glow": None,
    },
}
for _p in PALETTES.values():
    _p["back"] = _p["skin"]


def build() -> Model:
    m = Model("phantom", 64, 64)
    # Werte wie PhantomModel.createBodyLayer (Winkel dort im Bogenmaß)
    m.bone("body", rotation=(-0.1 * _RAD, 0, 0))
    m.cube("body", (-3, -2, -8), (5, 3, 9), "body")
    m.bone("crest", "body")                                                       # Wirbelkamm auf dem Rücken
    m.cube("crest", (-0.5, -4, -7), (0, 2, 7), "crest")
    m.bone("tail_base", "body", pivot=(0, -2, 1))
    m.cube("tail_base", (-2, 0, 0), (3, 2, 6), "tail")
    m.bone("tail_tip", "tail_base", pivot=(0, 0.5, 6))
    m.cube("tail_tip", (-1, 0, 0), (1, 1, 6), "tail")
    m.bone("tail_fin", "tail_tip", pivot=(-0.5, 0.5, 4))                        # Hautsegel am Schwanzende
    m.cube("tail_fin", (-2.5, 0, 0), (5, 0, 4), "fin")
    m.bone("left_wing_base", "body", pivot=(2, -2, -8), rotation=(0, 0, 0.1 * _RAD))
    m.cube("left_wing_base", (0, 0, 0), (6, 2, 9), "wing_base")
    m.bone("left_wing_tip", "left_wing_base", pivot=(6, 0, 0), rotation=(0, 0, 0.1 * _RAD))
    m.cube("left_wing_tip", (0, 0, 0), (13, 1, 9), "wing_tip")
    m.bone("right_wing_base", "body", pivot=(-3, -2, -8), rotation=(0, 0, -0.1 * _RAD))
    m.cube("right_wing_base", (-6, 0, 0), (6, 2, 9), "wing_base")
    m.bone("right_wing_tip", "right_wing_base", pivot=(-6, 0, 0), rotation=(0, 0, -0.1 * _RAD))
    m.cube("right_wing_tip", (-13, 0, 0), (13, 1, 9), "wing_tip")
    m.bone("head", "body", pivot=(0, 1, -7), rotation=(0.2 * _RAD, 0, 0))
    m.cube("head", (-4, -2, -5), (7, 3, 5), "head")
    m.bone("horns", "head", pivot=(-0.5, -2, -1.5), rotation=(28, 0, 0))         # zwei nach hinten geschwungene Hörner
    m.cube("horns", (-2.5, -1, 0), (1, 1, 4), "horn")
    m.cube("horns", (1.5, -1, 0), (1, 1, 4), "horn")
    return m


# =============================================================================================
# Malen
# =============================================================================================

def _c(r: list, i: float):
    return r[max(0, min(len(r) - 1, int(round(i))))]


def _dark(ctx: Ctx) -> bool:
    return ctx.params.get("variant") == "nachtmahr"


def _body(ctx: Ctx):
    """Leib: oben Wirbel (helle Höcker in der Mitte), seitlich Rippen, unten eine fahle Bauchhaut."""
    pal = ctx.pal
    f, s, t = ctx.face, ctx.s, ctx.t
    lx, ly, lz = ctx.local
    if f == "top":
        if s == 2:
            return _c(pal["bone"], 4 if t % 2 == 0 else 2.6)                     # Wirbel
        if s in (1, 3) and t % 2 == 0:
            return _c(pal["bone"], 1.6)
        return _c(pal["skin"], 2.4 + (s in (0, 4)) * -0.8 + (hash01(ctx.u, ctx.v, 501) - 0.5) * 0.6)
    if f == "bottom":
        return _c(pal["vein" if t % 2 else "membrane"], 3.4 if t % 2 == 0 else 2.6)
    if f in ("west", "east"):
        # Rippen: helle Bögen alle zwei Einheiten, unten auslaufend
        rib = int(math.floor(lz)) % 2 == 0 and t < 2
        if rib:
            return _c(pal["bone"], 3.2 - t * 0.8)
        return _c(pal["skin"], 2.2 - t * 0.5)
    if f == "front":
        return _c(pal["skin"], 1.6)
    return _c(pal["skin"], 1.2)


def _head(ctx: Ctx):
    """Schädel: Brauenwülste, leuchtende Augen, Maul mit Fangzähnen."""
    pal = ctx.pal
    f, s, t = ctx.face, ctx.s, ctx.t
    if f == "front":
        if t == 1 and s in (1, 2, 4, 5):
            inner = s in (2, 4)
            col = _c(pal["eye"], 5 if inner else 4)
            ctx.glow = _c(pal["eye"], 4 if inner else 3)
            return col
        if t == 0:
            return _c(pal["bone"], 3.4 if s in (1, 2, 4, 5) else 2.2)            # Brauenwülste
        if t == 2:
            if s in (2, 4):
                return _c(pal["tooth"], 4)                                         # Fangzähne
            if 1 <= s <= 5:
                return _c(pal["mouth"], 0 if s == 3 else 1)
            return _c(pal["skin"], 1.4)
        return _c(pal["skin"], 2.0)
    if f == "top":
        # zwei helle Grate laufen von den Brauen nach hinten, dazwischen dunkle Haut
        if s in (1, 5):
            return _c(pal["bone"], 3.6 - (4 - t) * 0.25)
        return _c(pal["skin"], 2.4 + (hash01(ctx.u, ctx.v, 511) - 0.5) * 0.6)
    if f == "bottom":
        return _c(pal["bone"], 2.4 if s % 2 == 0 else 1.8)                       # Unterkiefer
    if f in ("west", "east"):
        if t == 2 and (s >= 3 if f == "west" else s <= 1):
            return _c(pal["mouth"], 1)                                             # Maulspalte
        if t == 1 and (s == 4 if f == "west" else s == 0):
            return _c(pal["bone"], 3)                                              # Wangenknochen
        return _c(pal["skin"], 1.8)
    return _c(pal["skin"], 1.4)


def _horn(ctx: Ctx):
    pal = ctx.pal
    lz = ctx.local[2]
    return _c(pal["bone"], 4.2 - lz * 0.7 + (0.4 if ctx.face == "top" else 0.0))


def _crest(ctx: Ctx):
    """Wirbelkamm: Zacken über jedem Wirbel, dazwischen durchsichtig."""
    if ctx.face not in ("west", "east"):
        return None
    pal = ctx.pal
    lz = ctx.local[2]
    k = int(math.floor(lz)) % 2
    if ctx.t == 0 and k == 1:
        return None
    return _c(pal["bone"], 3.6 - ctx.t * 1.2 if k == 0 else 2.4)


def _tail(ctx: Ctx):
    pal = ctx.pal
    lz = ctx.local[2]
    seg = int(math.floor(lz)) % 2 == 0
    if ctx.face == "top" or ctx.face in ("west", "east"):
        return _c(pal["bone"], 3.2) if seg else _c(pal["skin"], 2.0)
    if ctx.face == "bottom":
        return _c(pal["membrane"], 3)
    return _c(pal["skin"], 1.4)


def _fin(ctx: Ctx):
    """Hautsegel am Schwanzende: zwei Strahlen, Hinterkante gezackt."""
    pal = ctx.pal
    if ctx.face not in ("top", "bottom"):
        return None
    s, t, w, h = ctx.s, ctx.t, ctx.w, ctx.h
    # Zeile 0 = hinten (Spitze des Segels)
    if t == 0 and s % 2 == 1:
        return None
    if s in (0, w - 1) and t <= 1:
        return None
    if s in (1, 3):
        return _c(pal["bone"], 3)
    return _c(pal["membrane"], 3.2 if ctx.face == "top" else 2.6)


# Fingerknochen der Flügel: Lage (z von vorn, 0..9) an der Flügelwurzel und an der Spitze
_FINGERS = [(0.5, 0.5), (2.6, 4.0), (4.6, 7.6)]


def _wing(ctx: Ctx, tip: bool):
    """Flughaut: vorn die kräftige Vorderkante, dazwischen zwei Fingerknochen, die sich zur Spitze hin auffächern; die
    Hinterkante schwingt zwischen den Fingern in Bögen ein und ist zur Spitze hin zerfranst. Unten heller (dünne Haut),
    beim dunklen Nachtmahr glimmen dort die Knochen."""
    pal = ctx.pal
    f = ctx.face
    lx, ly, lz = ctx.local
    ax = abs(lx)
    along = ax / 6.0 * 0.32 if not tip else 0.32 + ax / 13.0 * 0.68     # 0 an der Wurzel ... 1 an der Spitze
    if f in ("top", "bottom"):
        # Hinterkante: Bögen (Periode 4) – nur an der Spitze, zur Spitze hin tiefer, dazu einzelne Risse
        if tip:
            scallop = (0.5 - 0.5 * math.cos(2 * math.pi * (ax + 1.0) / 4.0)) * (0.8 + along * 1.6)
            if lz > 9.0 - scallop:
                return None
            if lz > 7.0 and along > 0.55 and hash01(int(math.floor(ax)), int(math.floor(lz)), 521) < 0.18:
                return None
        for k, (z0, z1) in enumerate(_FINGERS):
            zz = z0 + (z1 - z0) * along
            if abs(lz - zz) < 0.55:
                joint = tip and ax < 1.0
                col = _c(pal["bone"], (3.8 if f == "top" else 4.4) - along * 1.4 + (0.6 if joint else 0.0))
                if f == "bottom" and _dark(ctx) and k > 0:
                    ctx.glow = _c(pal["glow"], 2.2 - along * 1.4)
                return col
        n = noise3(lx * 0.3, 0, lz * 0.3, 523)
        if f == "top":
            i = 2.0 + along * 0.8 + (n - 0.5) * 0.6
        else:
            i = 3.0 + along * 0.6 + (n - 0.5) * 0.6
        # zarte Adern nahe der Knochen (eine Stufe, nur auf der dünnen Unterseite)
        for z0, z1 in (_FINGERS[1:] if f == "bottom" else ()):
            zz = z0 + (z1 - z0) * along
            if 0.55 <= abs(lz - zz) < 1.3 and int(math.floor(ax)) % 3 == 0:
                return _c(pal["vein"], i + 0.4)
        return _c(pal["membrane"], i)
    if f == "front":
        return _c(pal["bone"], 3.4 - along)                                       # Vorderkante
    if f == "back":
        return _c(pal["membrane"], 1.6) if not tip else None
    return _c(pal["skin"], 1.6)


def materials(variant: str) -> dict:
    return {
        "body": _body,
        "head": _head,
        "horn": _horn,
        "crest": _crest,
        "tail": _tail,
        "fin": _fin,
        "wing_base": lambda ctx: _wing(ctx, False),
        "wing_tip": lambda ctx: _wing(ctx, True),
    }


def hidden(variant: str) -> list[str]:
    return []


def preview_pose(variant: str) -> dict:
    return {}
