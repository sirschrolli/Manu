"""
Steinfaust – Zauber des Erdelements: eine riesige Faust aus Fels schlägt aus dem Boden nach oben (ca. 33 hoch).
Boden-Regel: y = 24 ist der Boden, oben ist -y.

Knochen:
  * arm    Drehpunkt (0, 24, 0) auf dem Boden: Unterarm als grobe Felssäule (ca. 10 × 19 × 10, zwei verschobene
           Quader), unten aufgeworfenes Gestein, ein paar Felsnasen, oben ein Armband aus glühenden Runen
  * fist   Kind von arm, Drehpunkt oben auf dem Unterarm (0, -19, 0): geballte Faust (ca. 16 breit × 12 hoch ×
           14 tief), die Knöchel zeigen nach oben. Vorn (-Z) die eingerollten Finger mit vier Knöchelwülsten oben und
           den Mittelgelenken an der Vorderkante, quer davor der Daumen, sein Ballen an der rechten Seite (-X);
           hinten (+Z) der Handrücken.

Bemalung wie beim Steinstachel (gemeinsamer Maler ``stone_spike.stone``): geschichteter Tuff und Sandstein mit
glühenden Bernstein-Rissen über die Knöchel und Runen im Armband (Leuchtbild).
"""
from __future__ import annotations

import math

from ..geometry import Model
from ..paint import Ctx, hash01, pick
from .stone_spike import PALETTES as _SPIKE_PALETTES
from .stone_spike import amber_crack, face_pos, stone

PALETTES = {"stone_fist": dict(_SPIKE_PALETTES["stone_spike"])}

ARM_TOP = -19.0
FINGER_X = (-7.4, -3.7, 0.0, 3.7)        # linke Kante jedes Fingers (je 3,4 breit)
HEIGHT = 33.0


def build() -> Model:
    m = Model("stone_fist", 128, 112)
    m.tex_res = 2

    # ---------------------------------------------------------------- Unterarm
    m.bone("arm", pivot=(0, 24, 0))
    # Säule in zwei Abschnitten (zum Ellbogen im Boden breiter), je zwei verschobene Quader; 1,2 tief im Boden
    m.cube("arm", (-5.5, -9.6, -5.1), (11, 10.8, 10.2), "arm")
    m.cube("arm", (-5.0, -9.2, -5.5), (10, 10.4, 11), "arm")
    m.cube("arm", (-4.6, ARM_TOP, -4.3), (9.2, 10.4, 8.6), "arm")
    m.cube("arm", (-4.2, ARM_TOP + 0.3, -4.6), (8.4, 10.0, 9.2), "arm")
    m.cube("arm", (-6.6, -3.4, -6.4), (13.2, 4.6, 12.8), "arm_base")              # aufgeworfenes Gestein
    m.cube("arm", (-5.6, -5.0, -7.4), (8.0, 2.4, 2.0), "arm_base")
    m.cube("arm", (2.0, -4.6, 5.6), (3.6, 2.2, 2.4), "arm_base")
    m.cube("arm", (-8.0, -1.6, 1.0), (2.4, 2.8, 3.6), "arm_base")
    for (x, y, z, w, h, d) in [(-6.6, -11.5, -2.0, 1.6, 3.0, 4.0), (5.0, -8.0, 0.6, 1.8, 2.6, 3.4),
                               (-2.0, -7.6, 5.0, 4.0, 2.2, 1.6), (1.4, -13.0, -5.4, 3.2, 2.4, 1.4)]:
        m.cube("arm", (x, y, z), (w, h, d), "arm")                               # Felsnasen
    m.cube("arm", (-5.3, ARM_TOP + 0.7, -5.3), (10.6, 2.6, 10.6), "band")         # Runen-Armband

    # ---------------------------------------------------------------- Faust (Knöchel oben)
    m.bone("fist", "arm", pivot=(0, ARM_TOP, 0))
    m.cube("fist", (-7.0, -10.6, -3.2), (14.0, 11.8, 8.4), "hand")                # Mittelhand
    m.cube("fist", (-6.2, -9.8, 4.6), (12.4, 9.0, 1.4), "hand")                   # Handrücken (gewölbt)
    for i, x0 in enumerate(FINGER_X):
        top = -11.8 + (0.4 if i in (0, 3) else 0.0)
        m.cube("fist", (x0, top, -7.2), (3.4, -1.4 - top, 4.6), "finger")         # eingerollter Finger
        m.cube("fist", (x0 + 0.25, top - 1.3, -3.9), (2.9, 1.6, 3.4), "knuckle")  # Grundknöchel (oben)
        m.cube("fist", (x0 + 0.3, top - 0.5, -7.7), (2.8, 2.3, 1.6), "knuckle")   # Mittelgelenk (Vorderkante)
    m.cube("fist", (-8.6, -7.6, -7.4), (3.6, 7.6, 6.8), "thumb")                  # Daumenballen (rechts, -X)
    m.cube("fist", (-6.0, -6.8, -10.0), (9.8, 3.6, 3.4), "thumb")                 # Daumen quer vor den Fingern
    m.cube("fist", (3.0, -6.4, -10.3), (1.6, 2.8, 1.0), "thumb_tip")              # Daumenkuppe
    return m


# =============================================================================================
# Bemalen
# =============================================================================================

def _hf(ctx: Ctx) -> float:
    """Höhe über dem Boden (0 Boden … 1 Knöchel) aus der Weltlage."""
    return max(0.0, min(1.0, (24.0 - ctx.world[1]) / HEIGHT))


def _arm(ctx: Ctx):
    x, y, z = ctx.local
    foot = max(0.0, min(1.0, (y + 3.0) / 4.0))
    face = ctx.lface
    # zwei glühende Risse laufen vom Boden zum Armband
    if face in ("front", "east") and ctx.cube.size[1] > 8:
        u, w = face_pos(ctx)
        base = 0.4 if face == "front" else 0.6
        c = amber_crack(ctx, u, base + 0.14 * math.sin(y * 0.4 + (1.3 if face == "east" else 0.0))
                        + 0.06 * math.sin(y * 1.2), 0.08)
        if c is not None and y < 0.5:
            return c
    return stone(ctx, _hf(ctx) * 0.8, foot)


def _arm_base(ctx: Ctx):
    x, y, z = ctx.local
    foot = max(0.0, min(1.0, (y + 1.6) / 2.4))
    return stone(ctx, 0.05, 0.4 + 0.6 * foot, seed=2)


# Runen (3 × 4 Pixel), werden im Armband nebeneinander gesetzt
_RUNES = [
    ["#..", "##.", "#.#", "#.."],
    [".#.", "###", ".#.", ".#."],
    ["#.#", "#.#", ".#.", ".#."],
    ["##.", "#.#", "##.", "#.."],
    ["#..", "#.#", "##.", "#.#"],
    [".#.", "#.#", ".#.", "#.#"],
]


def _band(ctx: Ctx):
    """Armband: dunkler, glatter Stein mit eingeschnittenen, glühenden Runen; oben und unten eine helle Kante."""
    face = ctx.lface
    hf = _hf(ctx)
    if face in ("top", "bottom"):
        return stone(ctx, hf, 0.0, seed=4, warm=0.0)
    if ctx.t == 0 or ctx.t == ctx.h - 1:
        return pick(ctx.pal["stone"], 0.62 if ctx.t == 0 else 0.3)
    col = pick(ctx.pal["stone"], 0.3 + (hash01(ctx.u // 3, 215) - 0.5) * 0.06)
    # Runen: 3 Pixel breit, 2 Pixel Abstand, Zeile 1 … 4
    period = 5
    k = ctx.s % period
    idx = (ctx.s // period + {"front": 0, "east": 2, "back": 4, "west": 1}.get(face, 0)) % len(_RUNES)
    row = ctx.t - (ctx.h - 4) // 2
    if 0 <= row < 4 and k < 3 and _RUNES[idx][row][k] == "#":
        g = pick(ctx.pal["amber"], 0.72 + 0.2 * (row == 1))
        ctx.glow = g
        return g
    return col


def _hand(ctx: Ctx):
    face = ctx.lface
    x, y, z = ctx.local
    if face == "top":
        # Knöchelfläche oben: glühende Risse
        u, w = face_pos(ctx)
        c = amber_crack(ctx, u, 0.5 + 0.2 * math.sin(z * 0.8) + 0.08 * math.sin(z * 2.1), 0.05)
        if c is not None:
            return c
    if face == "back":
        # Handrücken: Sehnen als flache Furchen
        u, w = face_pos(ctx)
        if any(abs(u - c) < 0.03 for c in (0.2, 0.4, 0.6, 0.8)) and w < 0.75:
            return stone(ctx, _hf(ctx) - 0.12, 0.0, seed=5)
    return stone(ctx, _hf(ctx), 0.0, seed=5)


def _finger(ctx: Ctx):
    """Eingerollter Finger: Gelenkfalten quer über die Vorderseite, Lücken zwischen den Fingern dunkel."""
    face = ctx.lface
    x, y, z = ctx.local
    hf = _hf(ctx)
    if face in ("west", "east"):
        return stone(ctx, hf - 0.22, 0.0, seed=6, edge=0.0)                  # Spalt zum Nachbarfinger
    if face == "front":
        oy, h = ctx.cube.origin[1], ctx.cube.size[1]
        rel = (y - oy) / h
        if abs(rel - 0.46) < 0.035 or abs(rel - 0.8) < 0.03:
            return stone(ctx, hf - 0.16, 0.0, seed=6)                         # Gelenkfalte
    if face == "top":
        u, w = face_pos(ctx)
        c = amber_crack(ctx, u, 0.45 + 0.25 * math.sin(z * 1.4 + x), 0.12)
        if c is not None:
            return c
    return stone(ctx, hf, 0.0, seed=6)


def _knuckle(ctx: Ctx):
    """Knöchelwulst: hell verwittert, ein glühender Riss über die Kuppe."""
    face = ctx.lface
    x, y, z = ctx.local
    if face in ("top", "front", "back"):
        u, w = face_pos(ctx)
        c = amber_crack(ctx, u, 0.5 + 0.18 * math.sin((z + y) * 1.6 + x * 0.7), 0.16)
        if c is not None:
            return c
    return stone(ctx, _hf(ctx) + 0.12, 0.0, seed=7)


def _thumb(ctx: Ctx):
    face = ctx.lface
    x, y, z = ctx.local
    if face == "front" and ctx.cube.size[0] > 8:
        u, w = face_pos(ctx)
        if abs(u - 0.55) < 0.02:
            return stone(ctx, _hf(ctx) - 0.16, 0.0, seed=8)                   # Gelenkfalte
    return stone(ctx, _hf(ctx) + 0.04, 0.0, seed=8)


def _thumb_tip(ctx: Ctx):
    col = stone(ctx, _hf(ctx) + 0.14, 0.0, seed=9, edge=0.0)
    if ctx.lface == "front" and 0 < ctx.t < ctx.h - 1 and ctx.s < ctx.w - 1:
        return pick(ctx.pal["sand"], 0.62)                                    # Kuppe hell verwittert
    return col


def materials(variant: str) -> dict:
    return {"arm": _arm, "arm_base": _arm_base, "band": _band, "hand": _hand, "finger": _finger, "knuckle": _knuckle,
            "thumb": _thumb, "thumb_tip": _thumb_tip}
