"""
Das Drachen-Modell (ausgewachsene Größe; kleinere Wachstumsstufen werden im Spiel verkleinert).

Knochen-Namen werden im Java-Code (DragonModel) für die Animationen verwendet – beim Umbenennen
dort mit anpassen.
"""
from __future__ import annotations

import math

from ..geometry import Model
from ..paint import (Ctx, belly_plates, eye_painter, flat_painter, gradient_painter, hash01, membrane_painter, mix,
                     noise3, pick, ramp, rel_y, stripes_painter, transparent_painter)

# Farbpaletten je Drachenart (angelehnt an die Farbpaletten im Konzeptbild)
PALETTES = {
    "fire": {
        "back": ramp("#3a0a08", "#d8452a"), "belly": ramp("#8a3f12", "#ffcf6a"),
        "membrane": ramp("#4a0e0a", "#e2622c"), "horn": ramp("#1a1414", "#7a6a60"),
        "claw": ramp("#2a2420", "#d8d0c0"), "eye": ramp("#a03a00", "#ffe066"), "accent": ramp("#c43a00", "#ffd34a"),
    },
    "ice": {
        "back": ramp("#3a6f9a", "#eaf6ff"), "belly": ramp("#9ab8cf", "#ffffff"),
        "membrane": ramp("#5d9ccc", "#dff4ff"), "horn": ramp("#5a8fbf", "#f4fbff"),
        "claw": ramp("#557799", "#eef6ff"), "eye": ramp("#1aa3d6", "#c8f8ff"), "accent": ramp("#7fd6ff", "#ffffff"),
    },
    "lightning": {
        "back": ramp("#16141f", "#565269"), "belly": ramp("#4a4652", "#a49fae"),
        "membrane": ramp("#1c1a26", "#5a5570"), "horn": ramp("#7a6000", "#ffe14a"),
        "claw": ramp("#2a2830", "#c9c4d4"), "eye": ramp("#b38a00", "#fff59a"), "accent": ramp("#c99a00", "#fff27a"),
    },
    "nature": {
        "back": ramp("#123d1c", "#59b84a"), "belly": ramp("#5d7a1e", "#d8e88a"),
        "membrane": ramp("#1f5a24", "#7fd067"), "horn": ramp("#3b2715", "#9a7246"),
        "claw": ramp("#3a2a1a", "#d6c8a0"), "eye": ramp("#3d9e00", "#d6ff7a"), "accent": ramp("#2f8f2f", "#b6f07a"),
    },
    "earth": {
        "back": ramp("#2e2116", "#9c7a52"), "belly": ramp("#6a5a40", "#d8c6a0"),
        "membrane": ramp("#3a2a1c", "#8f6f4a"), "horn": ramp("#3e3f42", "#a9adb2"),
        "claw": ramp("#303030", "#c8c8c8"), "eye": ramp("#a35a00", "#ffc04a"), "accent": ramp("#6b6e72", "#c9ccd0"),
    },
    "water": {
        "back": ramp("#0a2e4a", "#2fa3d6"), "belly": ramp("#3f8f9f", "#bff4f0"),
        "membrane": ramp("#0e3f66", "#4fc3e8"), "horn": ramp("#8a6a5a", "#f4e6dc"),
        "claw": ramp("#2a4a5a", "#d6eef4"), "eye": ramp("#00a8a8", "#a8fff4"), "accent": ramp("#1fb5c9", "#bdfcff"),
    },
}


def build() -> Model:
    m = Model("dragon", 256, 256)

    # ---------------------------------------------------------------- Rumpf
    m.bone("body", pivot=(0, 7, 0))
    m.cube("body", (-9, -9, -16), (18, 15, 30), "scales")
    m.cube("body", (-7, -10.5, -13), (14, 2, 22), "scales")                  # Rückenwölbung
    m.cube("body", (-7.5, 5, -14), (15, 2, 22), "scales")                    # Bauch
    for i, z in enumerate((-12, -5, 2, 9)):
        m.cube("body", (-1, -14.5 + (i % 2), z), (2, 4.5 - (i % 2), 3), "spike")
    m.cube("body", (0, -16, -14), (0, 6, 26), "crest")                        # Kamm (je nach Art sichtbar)

    # ---------------------------------------------------------------- Hals und Kopf
    m.bone("neck1", "body", pivot=(0, -3.5, -14), rotation=(-38, 0, 0))
    m.cube("neck1", (-5.5, -5.5, -11), (11, 11, 11), "scales")
    m.cube("neck1", (-1, -8.5, -8), (2, 3, 3), "spike")
    m.bone("neck2", "neck1", pivot=(0, 0, -10), rotation=(-8, 0, 0))
    m.cube("neck2", (-4.5, -4.5, -10), (9, 9, 10), "scales")
    m.cube("neck2", (-1, -7, -7), (2, 2.5, 3), "spike")
    m.bone("neck3", "neck2", pivot=(0, 0, -9), rotation=(18, 0, 0))
    m.cube("neck3", (-4, -4, -9), (8, 8, 9), "scales")
    m.bone("head", "neck3", pivot=(0, 0, -8), rotation=(28, 0, 0))
    m.cube("head", (-5, -5, -12), (10, 9, 12), "scales")                      # Schädel
    m.cube("head", (-3.5, -2.5, -22), (7, 5, 10), "scales")                   # Schnauze
    m.cube("head", (-4, -6, -9), (8, 1.5, 6), "scales")                       # Stirnwulst
    m.cube("head", (-3, 2.5, -21.5), (6, 1, 9), "teeth")                      # obere Zähne
    m.cube("head", (-2.5, -3.5, -21), (1.5, 1, 2), "horn")                    # Nüstern-Höcker
    m.cube("head", (1, -3.5, -21), (1.5, 1, 2), "horn")
    m.cube("head", (4.4, -2.5, -10.5), (1, 2, 3), "eye")
    m.cube("head", (-5.4, -2.5, -10.5), (1, 2, 3), "eye")
    m.bone("jaw", "head", pivot=(0, 3, -10), rotation=(4, 0, 0))
    m.cube("jaw", (-3.5, 0, -11.5), (7, 2.5, 11.5), "scales")
    m.cube("jaw", (-3, -1, -11), (6, 1, 8), "teeth")
    # Hörner (zwei Paare)
    for side in (1, -1):
        name = "horn_l" if side > 0 else "horn_r"
        m.bone(name, "head", pivot=(3.2 * side, -4.5, -2), rotation=(30, 14 * side, 0))
        m.cube(name, (-1.25, -1.25, 0), (2.5, 2.5, 10), "horn")
        m.bone(name + "2", "head", pivot=(4.6 * side, -1.5, -1), rotation=(10, 32 * side, 0))
        m.cube(name + "2", (-0.75, -0.75, 0), (1.5, 1.5, 6), "horn")
    m.cube("head", (0, -9, -10), (0, 5, 12), "crest")                         # Kopfkamm

    # ---------------------------------------------------------------- Schwanz
    sizes = [(10, 14), (8.5, 14), (7, 13), (5.5, 12), (4.5, 11), (3.5, 10)]
    rots = [-14, -6, 4, 6, 6, 6]
    parent = "body"
    pivot = (0, -2.5, 13.5)
    for i, ((s, length), rot) in enumerate(zip(sizes, rots), start=1):
        name = f"tail{i}"
        m.bone(name, parent, pivot=pivot, rotation=(rot, 0, 0))
        m.cube(name, (-s / 2, -s / 2, 0), (s, s, length), "scales")
        if i <= 4:
            m.cube(name, (-0.75, -s / 2 - 2.5, length * 0.35), (1.5, 2.5, 2.5), "spike")
        parent = name
        pivot = (0, 0, length - 1)
    m.cube("tail6", (-6, 0, 7), (12, 0, 10), "tailtip")                        # Schwanzspitze (je nach Art)
    m.cube("tail4", (0, -6, 0), (0, 4, 12), "crest")

    # ---------------------------------------------------------------- Beine
    # Rumpf-Drehpunkt liegt auf Höhe 7, der Boden auf Höhe 24.
    for side, sx in (("l", 1), ("r", -1)):
        # Vorderbein: Schulter auf 10, Unterarm ab 15.5, Fuß von 21.5 bis 24
        m.bone(f"front_leg_{side}", "body", pivot=(7.5 * sx, 3, -10), rotation=(4, 0, 0))
        m.cube(f"front_leg_{side}", (-3, -2.5, -3), (6, 8, 6), "scales")
        m.bone(f"front_shin_{side}", f"front_leg_{side}", pivot=(0, 5.5, 0), rotation=(-6, 0, 0))
        m.cube(f"front_shin_{side}", (-2.5, 0, -2.5), (5, 6.5, 5), "scales")
        m.bone(f"front_foot_{side}", f"front_shin_{side}", pivot=(0, 6, 0), rotation=(2, 0, 0))
        m.cube(f"front_foot_{side}", (-3, 0, -5.5), (6, 2.5, 8), "scales")
        for cx in (-2.5, -0.5, 1.5):
            m.cube(f"front_foot_{side}", (cx, 1, -7), (1, 1.5, 2), "claw")
        # Hinterbein: Hüfte auf 8, Unterschenkel ab 15, Fuß von 21.5 bis 24
        m.bone(f"hind_leg_{side}", "body", pivot=(8 * sx, 1, 9), rotation=(-6, 0, 0))
        m.cube(f"hind_leg_{side}", (-4, -3.5, -5), (8, 11, 10), "scales")
        m.bone(f"hind_shin_{side}", f"hind_leg_{side}", pivot=(0, 7, 1.5), rotation=(10, 0, 0))
        m.cube(f"hind_shin_{side}", (-3, 0, -3), (6, 6.5, 6), "scales")
        m.bone(f"hind_foot_{side}", f"hind_shin_{side}", pivot=(0, 6.5, 0), rotation=(-4, 0, 0))
        m.cube(f"hind_foot_{side}", (-3.5, 0, -6.5), (7, 2.5, 9), "scales")
        for cx in (-3, -0.5, 2):
            m.cube(f"hind_foot_{side}", (cx, 1, -8), (1, 1.5, 2), "claw")

    # ---------------------------------------------------------------- Flügel
    for side, sx in (("l", 1), ("r", -1)):
        arm = f"wing_{side}"
        fore = f"wing_tip_{side}"
        m.bone(arm, "body", pivot=(8 * sx, -8, -9), rotation=(0, 0, 0))
        x0 = 0 if sx > 0 else -22
        m.cube(arm, (x0, -1.5, -1.5), (22, 3, 3), "scales")
        m.cube(arm, (x0, 0, 1), (22, 0, 24), "membrane_inner")
        m.bone(fore, arm, pivot=(21 * sx, 0, 0))
        x1 = 0 if sx > 0 else -26
        m.cube(fore, (x1, -1, -1), (26, 2, 2), "scales")
        m.cube(fore, (0 if sx > 0 else -40, 0, 1), (40, 0, 34), "membrane_outer")
        # Fingerknochen (auf der Flughaut aufliegend)
        m.cube(fore, ((25 if sx > 0 else -27), -2, -2), (2, 3, 3), "claw")    # Daumenkralle
    return m


# =============================================================================================
# Bemalung
# =============================================================================================

def _membrane_shape(outer: bool, variant: str):
    base = membrane_painter("membrane", "back", spacing=11.0 if outer else 9.0)

    def paint(ctx: Ctx):
        lx, ly, lz = ctx.local
        ox, oy, oz = ctx.cube.origin
        w, h, d = ctx.cube.size
        span = abs(lx - ox) / w if ox >= 0 else abs(lx - (ox + w)) / w   # 0 am Körper, 1 außen
        chord = (lz - oz) / d
        if outer:
            limit = 0.98 - 0.85 * span ** 1.4
            scallop = 0.08 * abs(math.sin(span * math.pi * 3.0))
            limit -= scallop
            # Fingerknochen als dunkle Linien von der Handwurzel aus
            for ang in (0.12, 0.42, 0.75):
                fx = span
                fz = chord
                if abs(fz - fx * ang * 1.25) < 0.035 and fx > 0.05:
                    return pick(ctx.pal["back"], 0.3)
        else:
            limit = 0.95 - 0.25 * span
            limit -= 0.07 * abs(math.sin(span * math.pi * 1.5))
        if chord > limit:
            return None
        col = base(ctx)
        if chord > limit - 0.04:
            col = pick(ctx.pal["membrane"], 0.2)
        return col

    return paint


def _crest(variant: str):
    show = variant in ("water", "nature", "fire")

    def paint(ctx: Ctx):
        if not show:
            return None
        lx, ly, lz = ctx.local
        ox, oy, oz = ctx.cube.origin
        w, h, d = ctx.cube.size
        rel_h = (ly - oy) / max(h, 1e-3)          # 0 oben, 1 unten
        rel_z = (lz - oz) / max(d, 1e-3)
        wave = 0.35 + 0.35 * abs(math.sin(rel_z * math.pi * (5 if variant != "fire" else 7)))
        if rel_h < wave:
            return None
        key = "accent" if variant != "nature" else "membrane"
        v = 0.35 + 0.4 * (1.0 - rel_h)
        if variant == "water" and int(rel_z * 24) % 3 == 0:
            v -= 0.12
        return pick(ctx.pal[key], v)

    return paint


def _tail_tip(variant: str):
    def paint(ctx: Ctx):
        lx, ly, lz = ctx.local
        ox, oy, oz = ctx.cube.origin
        w, h, d = ctx.cube.size
        u = (lx - ox) / w * 2 - 1          # -1..1 quer
        v = (lz - oz) / d                  # 0 vorne, 1 hinten
        if variant == "fire":              # Flammenzunge
            limit = (1 - v) * 0.9 * (0.6 + 0.4 * math.sin(v * 9))
            if abs(u) > limit:
                return None
            return pick(ctx.pal["accent"], 0.9 - v * 0.6 - abs(u) * 0.3)
        if variant == "ice":               # Kristallspitze
            if abs(u) > (1 - v) * 0.8:
                return None
            return pick(ctx.pal["accent"], 0.4 + (1 - abs(u)) * 0.5)
        if variant == "lightning":         # Pfeilspitze
            if abs(u) > (1 - v) * 1.0 or (v < 0.25 and abs(u) > 0.25):
                return None
            return pick(ctx.pal["accent"], 0.75)
        if variant == "nature":            # Blatt
            if abs(u) > math.sin(v * math.pi) * 0.85:
                return None
            if abs(u) < 0.08:
                return pick(ctx.pal["horn"], 0.6)
            return pick(ctx.pal["accent"], 0.45 + 0.3 * (1 - abs(u)))
        if variant == "earth":             # Steinkeule (breite Platte)
            if abs(u) > 0.55 or v > 0.75:
                return None
            return pick(ctx.pal["horn"], 0.35 + 0.3 * hash01(int(lx), int(lz)))
        if variant == "water":             # Flosse
            if abs(u) > 0.15 + v * 0.85 or v < 0.15:
                return None
            return pick(ctx.pal["accent"], 0.4 + 0.25 * math.sin(u * 8))
        return None

    return paint


def _calm_scales(variant: str):
    """
    Schuppenkleid im Vanilla-Stil statt Pixelrauschen: ruhige Grundfarbe mit großen, weichen Helligkeitsflächen,
    feine Schuppenreihen (versetzt, jede Schuppe unten eine Stufe heller), dunklere Flächenkanten, etwas dunklere
    Rückenlinie und Bauchplatten bzw. hellerer Bauch an den Flanken.
    """
    seed = 11 + len(variant)

    def paint(ctx: Ctx):
        x, y, z = ctx.world
        if ctx.face == "bottom":
            return belly_plates(ctx, "belly")
        ry = rel_y(ctx) if ctx.face != "top" else 0.0
        v = 0.47 + (noise3(x * 0.12, y * 0.12, z * 0.12, seed) - 0.5) * 0.14 + (0.5 - ry) * 0.1
        if ctx.face == "top":
            row_pos = z / 3.0
            across = x / 4.0
        else:
            row_pos = y / 3.0
            across = (z if ctx.face in ("west", "east") else x) / 4.0
        row = math.floor(row_pos)
        fy = row_pos - row
        shifted = across + (row % 2) * 0.5
        fx = shifted - math.floor(shifted)
        if fy > 0.66 and 0.18 < fx < 0.82:
            v += 0.14                         # heller Bogen am unteren Rand jeder Schuppe
        if ctx.face != "top" and (ctx.s == 0 or ctx.s == ctx.w - 1 or ctx.t == ctx.h - 1):
            v -= 0.06
        if ctx.face == "top" and abs(x) < 1.2:
            v -= 0.06                         # Rückenlinie
        col = pick(ctx.pal["back"], v)
        if ry > 0.72 and ctx.face in ("west", "east", "front", "back"):
            t = (ry - 0.72) / 0.28
            col = mix(col, pick(ctx.pal["belly"], 0.58), t * 0.85)
        return col

    return paint


def materials(variant: str) -> dict:
    body = _calm_scales(variant)
    if variant == "lightning":
        body = stripes_painter(body, "accent", axis=2, period=9.0, width=1.1, glow=True)
    return {
        "scales": body,
        "spike": gradient_painter("horn", 0.3, 0.85),
        "horn": gradient_painter("horn", 0.25, 0.9, rings=4),
        "claw": flat_painter("claw", 0.75),
        "teeth": flat_painter("claw", 0.92, 0.04),
        "eye": eye_painter("eye"),
        "membrane_inner": _membrane_shape(False, variant),
        "membrane_outer": _membrane_shape(True, variant),
        "crest": _crest(variant),
        "tailtip": _tail_tip(variant),
        "none": transparent_painter(),
    }
