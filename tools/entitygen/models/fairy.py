"""
Fee: ein kleines, zierliches Wesen des Feenreichs mit großen, leuchtenden Augen, spitzen Ohren, langem Haar mit einem
Blütenkranz, einem Kleid aus Blütenblättern und zwei Paar großen Schmetterlingsflügeln, deren Augenflecken und
Randpunkte leuchten. Gebaut auf dem Skelett des Vanilla-Hilfsgeists (root -> head, body -> right/left_arm,
right/left_wing), damit Schweben und Flügelschlag mit AllayModel laufen.

Vier Gestalten: Rosenfee, Himmelsfee, Goldfee, Veilchenfee.
"""
from __future__ import annotations

import math

from ..geometry import Model
from ..paint import Ctx, hash01, mix, pick, ramp, rel_y

_SKIN = ramp("#a8786a", "#fff4ec", steps=7, hue_shift=-0.03)

PALETTES = {
    "rose": {"skin": _SKIN, "hair": ramp("#7a2a4a", "#ffc0dc", steps=7), "dress": ramp("#7a1e4e", "#ffb4d8", steps=7),
             "wing": ramp("#8a2068", "#ff9ccc", steps=7), "spot": ramp("#ff7ab8", "#fff6c8", steps=5),
             "eye": ramp("#3a1050", "#f4d8ff", steps=6), "accent": ramp("#d8a020", "#fff6c0", steps=5)},
    "azure": {"skin": _SKIN, "hair": ramp("#1a3a6a", "#c0ecff", steps=7), "dress": ramp("#14406e", "#a8e4ff", steps=7),
              "wing": ramp("#0e5a7e", "#7ae4f4", steps=7), "spot": ramp("#4ae0e8", "#f4fffe", steps=5),
              "eye": ramp("#0a2a50", "#d4f8ff", steps=6), "accent": ramp("#f0f0ff", "#ffffff", steps=5)},
    "gold": {"skin": _SKIN, "hair": ramp("#7a4a10", "#fff0a8", steps=7), "dress": ramp("#7a4a0a", "#ffe08a", steps=7),
             "wing": ramp("#9a5008", "#ffd070", steps=7), "spot": ramp("#ffb03a", "#fffbe0", steps=5),
             "eye": ramp("#3a2a08", "#fff2c8", steps=6), "accent": ramp("#ff7a9a", "#ffe0ea", steps=5)},
    "violet": {"skin": _SKIN, "hair": ramp("#2a1a5a", "#e0ccff", steps=7), "dress": ramp("#3a1a6e", "#d8b8ff", steps=7),
               "wing": ramp("#4a1a90", "#c8a0ff", steps=7), "spot": ramp("#b07aff", "#fff4ff", steps=5),
               "eye": ramp("#200a40", "#ecdcff", steps=6), "accent": ramp("#7ad8ff", "#e8fbff", steps=5)},
}

LOOKS = tuple(PALETTES)


def build() -> Model:
    m = Model("fairy", 64, 64)
    m.bone("fairy_root", pivot=(0, 23.5, 0))                                  # heißt im Spiel "root" (wie Vanilla)

    # ---------------------------------------------------------------- Kopf mit Haar und Blütenkranz
    m.bone("head", "fairy_root", pivot=(0, -3.99, 0))
    m.cube("head", (-2.5, -5, -2.5), (5, 5, 5), "face")
    m.cube("head", (-3.0, -3.6, -0.4), (0.6, 1.6, 1.0), "ear")
    m.cube("head", (2.4, -3.6, -0.4), (0.6, 1.6, 1.0), "ear")
    m.cube("head", (-2.7, -5.4, -2.0), (5.4, 1.0, 4.9), "hair")             # Scheitel
    m.cube("head", (-2.7, -5.0, 1.3), (5.4, 6.2, 1.5), "hair_back")         # langes Haar über den Rücken
    m.cube("head", (-2.85, -4.8, -1.6), (0.5, 4.4, 3.2), "hair")             # Strähnen an den Seiten
    m.cube("head", (2.35, -4.8, -1.6), (0.5, 4.4, 3.2), "hair")
    m.cube("head", (-2.9, -5.9, -2.8), (5.8, 0.9, 0.9), "crown")            # Blütenkranz vorn
    m.cube("head", (-2.9, -5.9, -2.8), (0.9, 0.9, 4.6), "crown")
    m.cube("head", (2.0, -5.9, -2.8), (0.9, 0.9, 4.6), "crown")

    # ---------------------------------------------------------------- Rumpf, Kleid, Arme, Flügel
    m.bone("body", "fairy_root", pivot=(0, -4, 0))
    m.cube("body", (-1.4, 0, -0.9), (2.8, 3.2, 1.8), "bodice")
    m.cube("body", (-2.0, 2.4, -1.5), (4.0, 2.2, 3.0), "dress")
    m.cube("body", (-2.6, 4.4, -2.0), (5.2, 1.6, 4.0), "hem")
    for side, sx in (("right", -1), ("left", 1)):
        arm = f"{side}_arm"
        m.bone(arm, "body", pivot=(1.75 * sx, 0.5, 0))
        m.cube(arm, (-0.6 if sx < 0 else -0.2, -0.5, -0.8), (0.8, 3.0, 1.6), "arm", inflate=-0.01)
        m.cube(arm, (-0.6 if sx < 0 else -0.2, 2.5, -0.7), (0.8, 0.8, 1.4), "hand", inflate=-0.01)
        wing = f"{side}_wing"
        m.bone(wing, "body", pivot=(0.5 * sx, 0, 0.6))
        m.cube(wing, (0, -2.4, 0), (0, 10, 12), "wing")
    m.bones["fairy_root"].name = "root"
    return m


# =============================================================================================
# Bemalen
# =============================================================================================

_LIT = {"top": 0.1, "front": 0.04, "west": 0.0, "east": -0.03, "back": -0.06, "bottom": -0.16}


def _face(ctx: Ctx):
    """Gesicht: große, leuchtende Augen mit Glanzpunkt, zarte Wangen, ein kleiner Mund; Stirnfransen."""
    face, s, t = ctx.face, ctx.s, ctx.t
    if face == "front":
        if t in (2, 3) and s in (0, 1, 3, 4):
            if t == 2:
                col = pick(ctx.pal["eye"], 0.98 if s in (0, 3) else 0.75)
            else:
                col = pick(ctx.pal["eye"], 0.5 if s in (1, 3) else 0.38)
            ctx.glow = col
            return col
        if t == 4 and s in (0, 4):
            return mix(pick(ctx.pal["skin"], 0.7), pick(ctx.pal["dress"], 0.75), 0.45)
        if t == 4 and s == 2:
            return pick(ctx.pal["dress"], 0.4)
        if t == 0:
            return pick(ctx.pal["hair"], 0.6 if s % 2 else 0.48)
        return pick(ctx.pal["skin"], 0.76 - t * 0.02 + (hash01(ctx.u, ctx.v) - 0.5) * 0.04)
    if face in ("west", "east", "back") and t <= 1:
        return pick(ctx.pal["hair"], 0.5 + hash01(s, face) * 0.2 + _LIT[face])
    return pick(ctx.pal["skin"], 0.7 + _LIT[face] + (hash01(ctx.u, ctx.v) - 0.5) * 0.04)


def _ear(ctx: Ctx):
    if ctx.face in ("west", "east") and ctx.t == 0 and ctx.s == 0:
        return None
    return pick(ctx.pal["skin"], 0.66 + _LIT[ctx.face] + (0.08 if ctx.t == 0 else 0.0))


def _hair(ctx: Ctx):
    strand = hash01(ctx.s, ctx.face, "strand")
    v = 0.48 + strand * 0.24 + _LIT[ctx.face] - rel_y(ctx) * 0.1
    if hash01(ctx.u, ctx.v, "shine") > 0.93:
        v += 0.18
    return pick(ctx.pal["hair"], v)


def _hair_back(ctx: Ctx):
    if ctx.face == "front" and ctx.t < 5:
        return pick(ctx.pal["hair"], 0.3)
    if ctx.t == ctx.h - 1 and ctx.s % 2 == 0 and ctx.face in ("back", "front"):
        return None                                                       # Haarspitzen
    return _hair(ctx)


def _crown(ctx: Ctx):
    """Blütenkranz: kleine Blüten im Wechsel mit Blättchen; die Blüten leuchten sanft."""
    if ctx.face == "bottom":
        return pick(ctx.pal["hair"], 0.3)
    flower = (ctx.s + ctx.t) % 3 == 0
    if flower:
        col = pick(ctx.pal["accent"], 0.85 if hash01(ctx.u, ctx.v) > 0.5 else 0.7)
        ctx.glow = col
        return col
    return mix(pick(ctx.pal["dress"], 0.5), (90, 170, 110, 255), 0.6)


def _bodice(ctx: Ctx):
    face, s, t = ctx.face, ctx.s, ctx.t
    if face == "front" and t == 0:
        return pick(ctx.pal["skin"], 0.7)                                 # Ausschnitt
    if face == "front" and s == 1 and t in (1, 2):
        col = pick(ctx.pal["accent"], 0.8)                                # eine kleine Brosche
        ctx.glow = col
        return col
    return pick(ctx.pal["dress"], 0.55 + _LIT[face] + (hash01(ctx.u, ctx.v) - 0.5) * 0.06)


def _dress(ctx: Ctx):
    """Kleid aus Blütenblättern: überlappende Blätter, die nach unten heller werden."""
    face, s, t = ctx.face, ctx.s, ctx.t
    if face == "top":
        return pick(ctx.pal["dress"], 0.42)
    if face == "bottom":
        return None
    v = 0.5 + _LIT[face] + t * 0.08 + (0.08 if (s + t) % 2 == 0 else 0.0)
    return pick(ctx.pal["dress"], v + (hash01(ctx.u, ctx.v) - 0.5) * 0.05)


def _hem(ctx: Ctx):
    """Der Saum: spitze Blütenblätter mit hellen Rändern, unten gezackt (durchsichtig)."""
    face, s, t = ctx.face, ctx.s, ctx.t
    if face == "top":
        return pick(ctx.pal["dress"], 0.48)
    if face == "bottom":
        if 0 < s < ctx.w - 1 and 0 < t < ctx.h - 1:
            return pick(ctx.pal["dress"], 0.3)
        return None
    if t == ctx.h - 1 and s % 2 == 1:
        return None
    v = 0.66 + _LIT[face] + (0.12 if s % 2 == 0 else 0.0)
    if t == ctx.h - 1:
        col = pick(ctx.pal["spot"], 0.75)
        ctx.glow = col
        return col
    return pick(ctx.pal["dress"], v)


def _arm(ctx: Ctx):
    return pick(ctx.pal["skin"], 0.68 + _LIT[ctx.face] - rel_y(ctx) * 0.06)


def _hand(ctx: Ctx):
    return pick(ctx.pal["skin"], 0.72 + _LIT[ctx.face])


# Schmetterlingsflügel: 12 Spalten (vom Ansatz nach hinten) × 10 Zeilen (oben nach unten)
_WING = [
    "...######...",
    "..#########.",
    ".###########",
    "############",
    "###########.",
    "#########...",
    "########....",
    ".#######....",
    "..######....",
    "...####.....",
]


def _wing(ctx: Ctx):
    """
    Schmetterlingsflügel: oben der große Vorderflügel, unten der kleinere Hinterflügel; dunkle Adern strahlen vom Ansatz
    aus, die Fläche wird nach außen heller, ein leuchtender Augenfleck sitzt am Vorderflügel, leuchtende Punkte säumen
    den Rand.
    """
    if ctx.face not in ("west", "east"):
        return None
    lx, ly, lz = ctx.local
    col = min(11, max(0, int(math.floor(lz))))
    row = min(9, max(0, int(math.floor(ly + 2.4))))
    if _WING[row][col] != "#":
        return None
    edge = (col + 1 > 11 or _WING[row][col + 1] != "#") or (row == 0 or _WING[row - 1][col] != "#") \
        or (row == 9 or _WING[row + 1][col] != "#")
    # Augenfleck im Vorderflügel: ein dunkler Ring um einen leuchtenden Kern
    d = math.hypot(col - 7.5, row - 2.5)
    if d < 0.75:
        c = pick(ctx.pal["spot"], 0.9)
        ctx.glow = c
        return c[0], c[1], c[2], 250
    if d < 1.7:
        c = pick(ctx.pal["wing"], 0.18)
        return c[0], c[1], c[2], 240
    if edge:
        if (col + row) % 3 == 0:
            c = pick(ctx.pal["spot"], 0.7)
            ctx.glow = c
            return c[0], c[1], c[2], 240
        c = pick(ctx.pal["wing"], 0.22)
        return c[0], c[1], c[2], 235
    # Adern vom Ansatz aus
    angle = math.atan2(row - 4.5, col + 0.5)
    vein = abs(math.sin(angle * 5.0)) < 0.16 and col > 1
    if vein or col == 0:
        c = pick(ctx.pal["wing"], 0.32)
        return c[0], c[1], c[2], 220
    v = 0.38 + col * 0.035 + (hash01(ctx.u, ctx.v) - 0.5) * 0.06
    if row >= 5:
        v += 0.06
    c = pick(ctx.pal["wing"], min(0.82, v))
    return c[0], c[1], c[2], 200


def materials(variant: str) -> dict:
    return {
        "face": _face, "ear": _ear, "hair": _hair, "hair_back": _hair_back, "crown": _crown, "bodice": _bodice,
        "dress": _dress, "hem": _hem, "arm": _arm, "hand": _hand, "wing": _wing,
    }
