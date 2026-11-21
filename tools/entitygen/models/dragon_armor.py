"""
Drachenpanzer: Panzerplatten, die über dem Drachen-Modell gezeichnet werden.

Die Geometrie übernimmt ALLE Knochen des Drachen-Modells mit denselben Drehpunkten und Winkeln
(siehe dragon.py). Dadurch kann DragonModel die Animationen unverändert auf den Panzer anwenden:
Hals, Kopf, Schwanz und Beine bewegen sich exakt mit. Die Platten sind etwas größer als die
Körperteile darunter (inflate), damit sie außen aufliegen.

Zwei Varianten:
  * steel_dragon_armor     – Stahlplatten mit Messingnieten und blauem Königswappen
  * draconium_dragon_armor – schwarzrotes Draconium mit Goldkanten, Glutadern und Stirnhorn
"""
from __future__ import annotations

import math

from ..geometry import Model
from ..paint import Ctx, hash01, pick, ramp, rel_y
from . import dragon

PALETTES = {
    "steel_dragon_armor": {
        "metal": ramp("#3a414d", "#e9eef5"), "trim": ramp("#5c3c0e", "#f0cf6a"),
        "strap": ramp("#24160c", "#8a5a32"), "accent": ramp("#14245a", "#5a86e0"), "emblem": ramp("#b08a2a", "#fff1a8"),
        "back": ramp("#3a414d", "#e9eef5"),
    },
    "draconium_dragon_armor": {
        "metal": ramp("#12080b", "#6e2630"), "trim": ramp("#6a4406", "#ffe384"),
        "strap": ramp("#140808", "#4a1616"), "accent": ramp("#8a1800", "#ffb24a"), "emblem": ramp("#c45a08", "#ffe7a0"),
        "back": ramp("#12080b", "#6e2630"),
    },
}

SIDES = ("top", "west", "east", "front", "back")


def build() -> Model:
    base = dragon.build()
    m = Model("dragon_armor", 256, 128)

    def copy(bone, parent: str):
        for child in bone.children:
            m.bone(child.name, parent, child.pivot, child.rotation)
            copy(child, child.name)

    copy(base.root, "root")

    # Rumpf: große Rückenplatte (deckt Rücken und obere Flanken) + Brustplatte
    m.cube("body", (-9, -10, -15), (18, 9, 28), "body_plate", inflate=0.7, faces=SIDES)
    m.cube("body", (-7, -7, -16), (14, 11, 1), "breast", inflate=0.6, faces=("front", "top", "west", "east"))
    # Hals: drei überlappende Schienen
    m.cube("neck1", (-5.5, -5.5, -11), (11, 7, 10), "neck_plate", inflate=0.6, faces=SIDES)
    m.cube("neck2", (-4.5, -4.5, -10), (9, 6, 9), "neck_plate", inflate=0.6, faces=SIDES)
    m.cube("neck3", (-4, -4, -9), (8, 5, 8), "neck_plate", inflate=0.6, faces=SIDES)
    # Kopf: Stirnplatte und Nasenschiene, bei Draconium mit Stirnhorn
    m.cube("head", (-5, -6, -12), (10, 3, 10), "head_plate", inflate=0.5, faces=SIDES)
    m.cube("head", (-3.5, -3, -21), (7, 1, 9), "snout_plate", inflate=0.45, faces=SIDES)
    m.cube("head", (-0.5, -5.5, -16.5), (1, 2.5, 3), "horn")
    m.cube("head", (-0.5, -7.5, -15.5), (1, 2, 1.5), "horn")
    # Schwanz: die ersten beiden Glieder
    m.cube("tail1", (-5, -5, 0), (10, 6, 13), "tail_plate", inflate=0.6, faces=SIDES)
    m.cube("tail2", (-4.5, -4.5, 0), (9, 5, 13), "tail_plate", inflate=0.5, faces=SIDES)
    # Beine: Schulter- und Schenkelplatten, Beinschienen
    for side in ("l", "r"):
        m.cube(f"front_leg_{side}", (-3, -2.5, -3), (6, 6, 6), "leg_plate", inflate=0.5, faces=SIDES)
        m.cube(f"front_shin_{side}", (-2.5, 1, -2.5), (5, 4, 5), "greave", inflate=0.45, faces=SIDES)
        m.cube(f"hind_leg_{side}", (-4, -3.5, -5), (8, 7, 10), "leg_plate", inflate=0.5, faces=SIDES)
        m.cube(f"hind_shin_{side}", (-3, 1, -3), (6, 4, 6), "greave", inflate=0.45, faces=SIDES)
    return m


# =============================================================================================
# Bemalung
# =============================================================================================

def _is_draconium(ctx: Ctx) -> bool:
    return ctx.params.get("variant", "").startswith("draconium")


def _rivet(ctx: Ctx) -> tuple[int, int, int, int]:
    return pick(ctx.pal["trim"], 0.9 if (ctx.s + ctx.t) % 2 == 0 else 0.75)


def _metal(ctx: Ctx, base: float, period: float, axis: int = 2, lame_shift: float = 0.0):
    """Metall mit Schienen (Lamellen) quer zur Achse: vordere Kante hell, hintere dunkel."""
    coord = ctx.world[axis] + lame_shift
    k = (coord / period) % 1.0
    v = base + (hash01(ctx.u, ctx.v, "m") - 0.5) * 0.06
    if k < 0.12:
        v += 0.16                       # aufliegende Vorderkante glänzt
    elif k > 0.88:
        v -= 0.26                       # Fuge zur nächsten Schiene
    elif k > 0.76:
        v -= 0.08
    return pick(ctx.pal["metal"], v)


def _veins(ctx: Ctx, col):
    """Glutadern im Draconium (leuchten im Dunkeln)."""
    if not _is_draconium(ctx):
        return col
    x, y, z = ctx.world
    n = math.sin(x * 0.9 + z * 0.35) + math.sin(z * 0.55 - y * 0.8) * 0.8
    if abs(n) < 0.07 and hash01(int(x * 2), int(z * 2), "vein") > 0.25:
        glow = pick(ctx.pal["accent"], 0.75)
        ctx.glow = glow
        return glow
    return col


def _trim_band(ctx: Ctx, ry: float, start: float) -> bool:
    return ctx.face in ("west", "east", "front", "back") and ry >= start


def body_plate(ctx: Ctx):
    ry = rel_y(ctx) if ctx.face != "top" else 0.0
    x, y, z = ctx.world
    # Goldene / messingfarbene Kante unten mit Nieten
    if _trim_band(ctx, ry, 0.8):
        if ctx.t == ctx.h - 1:
            return pick(ctx.pal["trim"], 0.15)
        if ctx.s % 4 == 1 and ctx.t == ctx.h - 2:
            return _rivet(ctx)
        return pick(ctx.pal["trim"], 0.55 + (0.12 if ctx.t == int(ctx.h * 0.8) else 0.0))
    # Rückgrat: Mittelgrat über die ganze Länge
    if ctx.face == "top" and abs(x) < 1.2:
        v = 0.62 if x < 0 else 0.42
        return pick(ctx.pal["trim"], v)
    # Wappen auf beiden Flanken (Raute mit Rand)
    if ctx.face in ("west", "east"):
        cz = -1.0
        cy = -5.2
        d = abs(z - cz) / 5.0 + abs(y - cy) / 3.2
        if d < 1.0:
            if d > 0.78:
                return pick(ctx.pal["trim"], 0.7)
            if _is_draconium(ctx):
                # Drachenauge: senkrechte Pupille in Glut
                if abs(z - cz) < 0.7:
                    return pick(ctx.pal["metal"], 0.05)
                col = pick(ctx.pal["emblem"], 0.75 - d * 0.4)
                ctx.glow = col
                return col
            # Königswappen: blaues Feld mit goldenem Kreuz
            if abs(z - cz) < 0.8 or abs(y - cy) < 0.6:
                return pick(ctx.pal["emblem"], 0.7)
            return pick(ctx.pal["accent"], 0.55 - d * 0.25)
    # Sattelgurt (Leder) quer über die Flanke hinter den Vorderbeinen
    if ctx.face in ("west", "east", "top") and -9.5 < z < -7.0:
        if ctx.face == "top" and abs(x) < 6:
            pass
        else:
            v = 0.5 + (0.1 if int(z * 2) % 2 == 0 else 0.0)
            if abs(z + 8.25) < 0.4 and ctx.t % 3 == 1 and ctx.face != "top":
                return pick(ctx.pal["trim"], 0.8)      # Schnalle / Nieten auf dem Gurt
            return pick(ctx.pal["strap"], v)
    col = _metal(ctx, 0.56 - ry * 0.16, period=6.5, lame_shift=0.5)
    # Nietenreihe entlang des Grats
    if ctx.face == "top" and 1.2 <= abs(x) < 2.2 and int(z + 100) % 3 == 0:
        return _rivet(ctx)
    return _veins(ctx, col)


def breast(ctx: Ctx):
    ry = rel_y(ctx) if ctx.face != "top" else 0.0
    x = ctx.world[0]
    if ctx.face == "front":
        # Brustpanzer mit Mittelgrat und Rand
        if ctx.s in (0, ctx.w - 1) or ctx.t in (0, ctx.h - 1):
            return pick(ctx.pal["trim"], 0.5 if ctx.t != ctx.h - 1 else 0.2)
        if abs(x) < 0.8:
            return pick(ctx.pal["metal"], 0.82 if x < 0 else 0.5)
        if ctx.t in (3, 6) and ctx.s % 3 == 1:
            return _rivet(ctx)
        v = 0.62 - ry * 0.2 - abs(x) * 0.015
        return _veins(ctx, pick(ctx.pal["metal"], v + (hash01(ctx.u, ctx.v) - 0.5) * 0.05))
    return pick(ctx.pal["trim"], 0.45)


def segment_plate(period: float, trim_from: float = 0.72):
    def paint(ctx: Ctx):
        ry = rel_y(ctx) if ctx.face != "top" else 0.0
        x = ctx.local[0] - (ctx.cube.origin[0] + ctx.cube.size[0] / 2)
        if _trim_band(ctx, ry, trim_from):
            if ctx.t == ctx.h - 1:
                return pick(ctx.pal["trim"], 0.15)
            if ctx.s % 3 == 1 and ctx.t == ctx.h - 2:
                return _rivet(ctx)
            return pick(ctx.pal["trim"], 0.55)
        if ctx.face == "top" and abs(x) < 1.0:
            return pick(ctx.pal["trim"], 0.62 if x < 0 else 0.4)
        col = _metal(ctx, 0.56 - ry * 0.18, period=period)
        return _veins(ctx, col)

    return paint


def head_plate(ctx: Ctx):
    ry = rel_y(ctx) if ctx.face != "top" else 0.0
    x = ctx.local[0]
    if ctx.face == "top":
        if abs(x) < 0.9:
            return pick(ctx.pal["trim"], 0.62 if x < 0 else 0.42)
        if ctx.s in (0, ctx.w - 1) or ctx.t in (0, ctx.h - 1):
            return pick(ctx.pal["trim"], 0.45)
        if (ctx.s, ctx.t) in ((2, 2), (ctx.w - 3, 2), (2, ctx.h - 3), (ctx.w - 3, ctx.h - 3)):
            return _rivet(ctx)
        return _veins(ctx, pick(ctx.pal["metal"], 0.66 + (hash01(ctx.u, ctx.v) - 0.5) * 0.05))
    if ctx.face == "front":
        # Stirnseite über der Schnauze: zwei Augenbrauen-Bögen
        if ctx.t == ctx.h - 1:
            return pick(ctx.pal["trim"], 0.2)
        return pick(ctx.pal["trim"], 0.55 if ctx.t == 0 else 0.45)
    if ctx.t == ctx.h - 1:
        return pick(ctx.pal["trim"], 0.2)
    return _veins(ctx, pick(ctx.pal["metal"], 0.52 - ry * 0.15))


def snout_plate(ctx: Ctx):
    x = ctx.local[0]
    if ctx.face == "top":
        if abs(x) < 0.8:
            return pick(ctx.pal["trim"], 0.6 if x < 0 else 0.4)
        if ctx.t % 3 == 0 and ctx.s in (1, ctx.w - 2):
            return _rivet(ctx)
        return _veins(ctx, pick(ctx.pal["metal"], 0.64 - (ctx.t / max(ctx.h, 1)) * 0.1))
    return pick(ctx.pal["trim"], 0.4)


def leg_plate(ctx: Ctx):
    ry = rel_y(ctx) if ctx.face != "top" else 0.0
    if _trim_band(ctx, ry, 0.75):
        if ctx.t == ctx.h - 1:
            return pick(ctx.pal["trim"], 0.15)
        if ctx.s % 3 == 1:
            return _rivet(ctx)
        return pick(ctx.pal["trim"], 0.55)
    if ctx.face == "top":
        return pick(ctx.pal["metal"], 0.7 + (hash01(ctx.u, ctx.v) - 0.5) * 0.05)
    # Schulterbuckel: runder Glanzpunkt
    v = 0.58 - ry * 0.25
    if ctx.t == 0:
        v += 0.12
    return _veins(ctx, pick(ctx.pal["metal"], v + (hash01(ctx.u, ctx.v) - 0.5) * 0.06))


def greave(ctx: Ctx):
    ry = rel_y(ctx) if ctx.face != "top" else 0.0
    if ctx.face == "top":
        return pick(ctx.pal["trim"], 0.4)
    if ctx.t in (0, ctx.h - 1):
        return pick(ctx.pal["trim"], 0.5 if ctx.t == 0 else 0.2)
    if ctx.face == "front" and ctx.s == ctx.w // 2:
        return pick(ctx.pal["metal"], 0.85)
    return _veins(ctx, pick(ctx.pal["metal"], 0.6 - ry * 0.2 + (hash01(ctx.u, ctx.v) - 0.5) * 0.05))


def horn(ctx: Ctx):
    if not _is_draconium(ctx):
        return None
    a = rel_y(ctx)
    return pick(ctx.pal["trim"], 0.85 - a * 0.55 + (0.08 if ctx.face in ("west", "front") else 0.0))


def materials(variant: str) -> dict:
    return {
        "body_plate": body_plate,
        "breast": breast,
        "neck_plate": segment_plate(3.5),
        "tail_plate": segment_plate(4.0),
        "head_plate": head_plate,
        "snout_plate": snout_plate,
        "leg_plate": leg_plate,
        "greave": greave,
        "horn": horn,
    }
