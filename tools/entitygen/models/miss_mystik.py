"""
MissMystik, die Feen-Zauberkünstlerin: die größte Bühnenmagierin des Feenreichs, gut zweieinhalb Blöcke groß (mit dem
Zylinder fast drei). Sie trägt einen mitternachtsvioletten Frack mit Satin-Revers, goldenen Knöpfen und langen
Schößen, ein weißes Hemd mit magentafarbener Fliege, weiße Handschuhe, einen kurzen Faltenrock mit goldenem Saum,
dunkle Strümpfe mit Rautenmuster und spitze Stiefel. Auf dem Kopf sitzt keck ein hoher Zylinder mit Satinband,
goldener Schnalle, einer Feenrose und einer schillernden Feder; darunter fällt silberlila Haar mit rosa Spitzen zu
einem langen Pferdeschwanz. Vor den Augen eine Maske mit Goldrand, hinter der zwei Lichter glühen.

Aus dem Rücken wachsen zwei Paar schillernde Feenflügel (violett, türkis, rosa), durchzogen von goldenen Adern; auf
jedem Flügel leuchtet eine Spielkartenfarbe (Pik, Herz, Kreuz, Karo). In der rechten Hand der Zauberstab (schwarz mit
weißen Spitzen), in der linken – bei ihren Kartentricks – ein Fächer aus fünf Karten.

Knochen-Namen werden in MissMystikModel (Java) animiert: Schweben, Flügelschlag, Pferdeschwanz und Frackschöße wehen,
Gesten für jeden Trick; der Zylinder ("hat") fliegt beim Hut-Trick als eigenes Wesen davon.
"""
from __future__ import annotations

import math

from ..geometry import Model
from ..paint import Ctx, Voronoi3D, hash01, hexc, mix, noise3, pick, ramp, rel_y

PALETTES = {
    "miss_mystik": {
        "skin": ramp("#a07280", "#fff8f4", steps=7, hue_shift=-0.02),
        "hair": ramp("#40227a", "#f2dcff", steps=7, hue_shift=0.05),
        "hair_tip": ramp("#8a2a7a", "#ffc4ea", steps=6),
        "coat": ramp("#0e0818", "#7a56b8", steps=8, hue_shift=0.02),
        "satin": ramp("#2a1648", "#d4b8fa", steps=7),
        "gold": ramp("#5a3a0a", "#fff4c0", steps=7, hue_shift=-0.03),
        "white": ramp("#7a7090", "#ffffff", steps=6),
        "magenta": ramp("#4a0632", "#ff86d0", steps=7),
        "skirt": ramp("#2a0a3e", "#e88ae0", steps=7),
        "stocking": ramp("#0a0612", "#5a4876", steps=6),
        "boot": ramp("#08060c", "#6a5a84", steps=7),
        "hat": ramp("#06040a", "#5a4870", steps=7),
        "wand": ramp("#050508", "#4a4a58", steps=5),
        "eye": ramp("#5a1aa0", "#ffffff", steps=6),
        "rose": ramp("#7a0a4a", "#ffd0f0", steps=6),
        "leaf": ramp("#1a4a3a", "#6ad8a8", steps=5),
        "wing_base": [hexc("#5a2aa8"), hexc("#7a46d0")],
        "wing_mid": [hexc("#2aa8c8"), hexc("#5ae0e8")],
        "wing_tip": [hexc("#e85ab8"), hexc("#ffa8de")],
        "wing_rim": ramp("#1e0c3a", "#5a3a8a", steps=4),
        "vein": ramp("#c08a20", "#fff2b8", steps=5),
        "card_red": ramp("#7a0a1a", "#ff4a5a", steps=4),
        "card_black": ramp("#0a0a12", "#3a3a4a", steps=4),
    },
}

# Flügel: Spitze (ly, lz) relativ zum Ansatz und größte halbe Breite
WINGS = {"upper": (-14.5, 15.5, 7.2), "lower": (9.5, 10.5, 5.0)}


def build() -> Model:
    m = Model("miss_mystik", 256, 256)
    m.tex_res = 2

    # ---------------------------------------------------------------- Leib: Taille, Brust, Hals, Fliege
    m.bone("body", pivot=(0, 4.0, 0))
    m.cube("body", (-2.75, -4.0, -1.75), (5.5, 4.0, 3.5), "waist")
    m.cube("body", (-3.0, -1.25, -2.0), (6.0, 1.25, 4.0), "belt")
    m.bone("chest", "body", pivot=(0, -4.0, 0))
    m.cube("chest", (-3.5, -7.0, -2.0), (7, 7, 4), "coat")
    m.cube("chest", (-1.5, -8.0, -1.5), (3, 1.25, 3), "collar")
    m.cube("chest", (-1.0, -8.75, -1.0), (2, 1.75, 2), "skin")
    m.cube("chest", (-1.75, -7.75, -2.5), (3.5, 1.25, 0.75), "bowtie")
    m.cube("chest", (1.4, -5.6, -2.45), (1.0, 1.0, 0.6), "flower")              # Feenrose am Revers

    # ---------------------------------------------------------------- Kopf: Gesicht, Maske, Haar, Ohren
    m.bone("head", "chest", pivot=(0, -8.5, 0))
    m.cube("head", (-3.0, -7.0, -3.0), (6, 7, 6), "face")
    m.cube("head", (-3.25, -7.5, -3.0), (6.5, 1.0, 6.25), "hair")             # Scheitel (unter dem Zylinder)
    m.cube("head", (-3.25, -7.25, -3.3), (6.5, 2.0, 1.0), "bangs")            # Stirnfransen
    m.cube("head", (-3.25, -7.0, 1.5), (6.5, 8.0, 1.75), "hair_back")
    m.cube("head", (-3.4, -6.0, -2.6), (0.6, 6.5, 2.0), "lock")               # Strähnen, die das Gesicht rahmen
    m.cube("head", (2.8, -6.0, -2.6), (0.6, 6.5, 2.0), "lock")
    m.cube("head", (-3.6, -4.5, -0.5), (0.6, 2.5, 1.5), "ear")
    m.cube("head", (3.0, -4.5, -0.5), (0.6, 2.5, 1.5), "ear")
    m.cube("head", (-3.5, -5.0, -3.5), (7.0, 2.0, 0.5), "mask")

    # Pferdeschwanz (zwei Glieder, damit er weht)
    m.bone("ponytail", "head", pivot=(0, -6.25, 3.0), rotation=(30, 0, 0))
    m.cube("ponytail", (-1.5, -0.6, -0.9), (3.0, 1.25, 1.75), "gold")         # Haarband
    m.cube("ponytail", (-1.5, 0.5, -0.7), (3.0, 8.0, 2.0), "ponytail")
    m.bone("ponytail_tip", "ponytail", pivot=(0, 8.25, 0.25), rotation=(-20, 0, 0))
    m.cube("ponytail_tip", (-1.25, 0, -0.75), (2.5, 7.0, 1.5), "ponytail")
    m.cube("ponytail_tip", (-0.75, 6.75, -1.0), (1.5, 2.0, 1.5), "ponytail")

    # ---------------------------------------------------------------- Zylinder (keck geneigt)
    m.bone("hat", "head", pivot=(0, -7.25, 0), rotation=(-3, 0, -8))
    m.cube("hat", (-4.75, -0.75, -4.75), (9.5, 0.75, 9.5), "hat_brim")
    m.cube("hat", (-3.25, -7.5, -3.25), (6.5, 6.75, 6.5), "hat")
    m.cube("hat", (-3.5, -8.25, -3.5), (7.0, 0.75, 7.0), "hat_top")
    m.cube("hat", (-3.4, -2.5, -3.4), (6.8, 1.75, 6.8), "band")
    m.cube("hat", (-1.0, -2.75, -3.65), (2.0, 2.25, 0.5), "buckle")
    m.cube("hat", (2.9, -3.5, -1.75), (1.25, 2.25, 2.25), "flower")
    m.bone("feather", "hat", pivot=(3.4, -2.75, 0.5), rotation=(-26, 0, 16))
    m.cube("feather", (0, -9.0, -1.25), (0, 9.0, 2.5), "feather")

    # ---------------------------------------------------------------- Arme: Schulter, Ärmel, Manschette, Handschuh
    for side, sx in (("right", -1), ("left", 1)):
        arm = f"{side}_arm"
        m.bone(arm, "chest", pivot=(4.4 * sx, -6.25, 0), rotation=(0, 0, -6 * sx))
        m.cube(arm, (-1.5, -1.25, -1.6), (3.0, 2.75, 3.25), "shoulder")
        m.cube(arm, (-1.1, 0, -1.1), (2.2, 5.5, 2.2), "sleeve")
        fore = f"{side}_forearm"
        m.bone(fore, arm, pivot=(0, 5.25, 0), rotation=(-12, 0, 0))
        m.cube(fore, (-1.0, 0, -1.0), (2.0, 3.5, 2.0), "sleeve")
        m.cube(fore, (-1.25, 2.75, -1.25), (2.5, 0.75, 2.5), "cuff")
        m.cube(fore, (-1.1, 3.5, -1.1), (2.2, 0.75, 2.2), "collar")
        m.cube(fore, (-1.1, 4.25, -1.25), (2.2, 2.5, 2.5), "glove")
    # Zauberstab in der rechten Hand (zeigt nach vorn)
    m.bone("wand", "right_forearm", pivot=(0, 5.5, -0.5), rotation=(18, 0, 0))
    m.cube("wand", (-0.35, -0.35, -9.0), (0.7, 0.7, 10.5), "wand")
    m.cube("wand", (-0.45, -0.45, -10.5), (0.9, 0.9, 1.5), "wand_tip")
    m.cube("wand", (-0.45, -0.45, 1.5), (0.9, 0.9, 1.0), "wand_end")
    # Kartenfächer in der linken Hand (nur bei den Kartentricks sichtbar)
    m.bone("cards", "left_forearm", pivot=(0, 5.5, -1.0))
    for i, angle in enumerate((-40, -20, 0, 20, 40)):
        card = f"card{i}"
        m.bone(card, "cards", pivot=(0, 0, -0.05 * i), rotation=(0, 0, angle))
        m.cube(card, (-1.25, 0, 0), (2.5, 3.5, 0), "card", tag=str(i))

    # ---------------------------------------------------------------- Frackschöße, Rock, Beine
    m.bone("coattails", "body", pivot=(0, -1.5, 1.75), rotation=(14, 0, 0))
    m.cube("coattails", (-3.0, 0, 0), (6.0, 11.0, 0.5), "coattails")
    m.bone("skirt", "body", pivot=(0, -0.5, 0))
    m.cube("skirt", (-3.0, 0, -1.9), (6.0, 2.0, 3.8), "skirt")
    m.cube("skirt", (-3.75, 1.5, -2.6), (7.5, 3.0, 5.2), "skirt_flare")
    for side, sx in (("right", -1), ("left", 1)):
        leg = f"{side}_leg"
        m.bone(leg, "body", pivot=(1.45 * sx, 1.5, 0), rotation=(-10 if sx < 0 else 4, 0, 2 * -sx))
        m.cube(leg, (-1.4, 0, -1.4), (2.8, 8.5, 2.8), "stocking")
        shin = f"{side}_shin"
        m.bone(shin, leg, pivot=(0, 8.5, 0), rotation=(18 if sx < 0 else 8, 0, 0))
        m.cube(shin, (-1.25, 0, -1.25), (2.5, 3.0, 2.5), "stocking")
        m.cube(shin, (-1.4, 2.75, -1.4), (2.8, 6.0, 2.8), "boot")
        m.cube(shin, (-1.6, 2.5, -1.6), (3.2, 0.75, 3.2), "boot_cuff")
        m.cube(shin, (-1.3, 8.5, -3.75), (2.6, 1.25, 4.0), "boot")
        m.cube(shin, (-0.6, 8.75, 0.25), (1.2, 1.25, 1.0), "heel")

    # ---------------------------------------------------------------- Flügel: zwei Paar, schillernd
    for side, sx in (("right", -1), ("left", 1)):
        up = f"{side}_wing"
        m.bone(up, "chest", pivot=(1.0 * sx, -5.0, 2.0), rotation=(-10, 62 * sx, 16 * sx))
        m.cube(up, (0, -16.0, -0.5), (0, 17.0, 19.0), "wing", tag="upper")
        low = f"{side}_wing_lower"
        m.bone(low, "chest", pivot=(0.8 * sx, -2.5, 2.0), rotation=(8, 50 * sx, 32 * sx))
        m.cube(low, (0, -1.5, -0.5), (0, 12.5, 13.0), "wing", tag="lower")
    return m


# =============================================================================================
# Bemalung
# =============================================================================================

_LIT = {"top": 0.1, "front": 0.04, "west": 0.0, "east": -0.03, "back": -0.06, "bottom": -0.16}


def _n(ctx: Ctx, scale: float = 1.0, seed: int = 0) -> float:
    x, y, z = ctx.world
    return noise3(x * scale, y * scale, z * scale, seed) - 0.5


def _skin(ctx: Ctx):
    return pick(ctx.pal["skin"], 0.7 + _LIT[ctx.face] - rel_y(ctx) * 0.04 + (hash01(ctx.u, ctx.v) - 0.5) * 0.03)


def _face(ctx: Ctx):
    """Gesicht (12×14): Brauen über der Maske, darunter Nase, Wangen mit Rouge, ein Schönheitsstern, Lippen."""
    face, s, t = ctx.face, ctx.s, ctx.t
    if face == "front":
        if t <= 3:
            return pick(ctx.pal["hair"], 0.5 + (s % 2) * 0.08)
        if 4 <= t <= 7:
            return pick(ctx.pal["skin"], 0.66)                          # unter der Maske
        if t == 8:
            return pick(ctx.pal["skin"], 0.74 if 3 <= s <= 8 else 0.7)   # Wangenknochen, knapp unter der Maske
        if t == 9 and s in (5, 6):
            return pick(ctx.pal["skin"], 0.72)                          # Nasenrücken
        if t == 10 and s in (5, 6):
            return pick(ctx.pal["skin"], 0.66)                          # Nasenschatten
        if t in (9, 10) and s in (1, 2, 9, 10):
            return mix(pick(ctx.pal["skin"], 0.8), pick(ctx.pal["magenta"], 0.8), 0.3 if t == 9 else 0.18)   # Rouge
        if t == 9 and s == 8:
            col = pick(ctx.pal["gold"], 0.95)                           # Schönheitsstern unter dem Auge
            ctx.glow = col
            return col
        # ein kleines, verschmitztes Lächeln: Mundwinkel eine Zeile höher
        if t == 11 and s in (4, 7):
            return pick(ctx.pal["magenta"], 0.4)
        if t == 12 and s in (5, 6):
            return pick(ctx.pal["magenta"], 0.5)
        if t == 13 and s in (5, 6):
            return mix(pick(ctx.pal["skin"], 0.74), pick(ctx.pal["magenta"], 0.7), 0.4)
        return pick(ctx.pal["skin"], 0.8 - (0.04 if s in (0, 11) else 0.0) + (hash01(ctx.u, ctx.v) - 0.5) * 0.02)
    if face in ("west", "east"):
        if ctx.t <= 4 or ctx.s >= 9:
            return _hair(ctx)
        return _skin(ctx)
    if face in ("top", "back"):
        return _hair(ctx)
    return pick(ctx.pal["skin"], 0.58)


def _hair(ctx: Ctx):
    strand = hash01(ctx.s, ctx.face, ctx.cube.origin, "strand")
    v = 0.5 + strand * 0.22 + _LIT[ctx.face] - rel_y(ctx) * 0.08
    if hash01(ctx.u, ctx.v, "shine") > 0.92:
        v += 0.18
    return pick(ctx.pal["hair"], v)


def _bangs(ctx: Ctx):
    """Stirnfransen: Strähnen mit Spitzen (unten gezackt, durchsichtig)."""
    if ctx.face == "front" and ctx.t == ctx.h - 1 and ctx.s % 3 == 1:
        return None
    if ctx.face == "bottom":
        return pick(ctx.pal["hair"], 0.3)
    return _hair(ctx)


def _hair_back(ctx: Ctx):
    if ctx.face == "front":
        return pick(ctx.pal["hair"], 0.28)
    if ctx.face in ("back", "west", "east") and ctx.t == ctx.h - 1 and ctx.s % 2 == 0:
        return None
    return _hair(ctx)


def _lock(ctx: Ctx):
    if ctx.t == ctx.h - 1 and ctx.s % 2 == 1:
        return None
    r = rel_y(ctx)
    if r > 0.72:
        return pick(ctx.pal["hair_tip"], 0.5 + (r - 0.72) * 1.2 + _LIT[ctx.face])
    return _hair(ctx)


def _ponytail(ctx: Ctx):
    """Pferdeschwanz: lange Strähnen, die zur Spitze hin rosa werden."""
    r = rel_y(ctx)
    tip = ctx.cube.bone.name == "ponytail_tip"
    k = (0.5 + r * 0.5) if tip else r * 0.5
    strand = hash01(ctx.s, ctx.face, "pt")
    if tip and ctx.t == ctx.h - 1 and ctx.s % 2 == 0:
        return None
    if k > 0.55:
        return pick(ctx.pal["hair_tip"], 0.42 + strand * 0.3 + (k - 0.55) * 0.6 + _LIT[ctx.face])
    return pick(ctx.pal["hair"], 0.48 + strand * 0.26 + _LIT[ctx.face])


def _ear(ctx: Ctx):
    # spitz: oben hinten ausgespart
    if ctx.face in ("west", "east") and ctx.t == 0 and ctx.s in (0, 1, ctx.w - 1):
        return None
    if ctx.face == "top":
        return None
    return pick(ctx.pal["skin"], 0.68 + _LIT[ctx.face] + (0.06 if ctx.t <= 1 else 0.0))


def _mask(ctx: Ctx):
    """
    Maske (14×4): violett mit Goldrand und funkelnden Pailletten, zwei Augenöffnungen, aus denen helles Licht glüht; die
    äußeren Ecken schwingen nach oben (unten ausgespart), über der Nase eine Kerbe.
    """
    face, s, t = ctx.face, ctx.s, ctx.t
    if face != "front":
        if face == "back":
            return pick(ctx.pal["coat"], 0.2)
        return pick(ctx.pal["magenta"], 0.36 + _LIT[face])
    w = ctx.w
    if (t == 3 and (s <= 1 or s >= w - 2)) or (t == 2 and (s == 0 or s == w - 1)):
        return None                                                     # die Ecken schwingen nach oben
    if t == 3 and s in (5, 6, 7, 8):
        return None                                                     # Kerbe über der Nase
    # mandelförmige Augen mit hellem Kern
    if t == 1 and s in (2, 3, 4, 9, 10, 11):
        core = s in (3, 10)
        col = pick(ctx.pal["eye"], 0.99 if core else 0.62)
        ctx.glow = col
        return col
    if t == 2 and s in (3, 10):
        col = pick(ctx.pal["eye"], 0.45)
        ctx.glow = col
        return col
    # Goldspitzen an den nach oben geschwungenen Ecken, ein Juwel in der Mitte
    if (t == 0 and s in (0, 1, w - 2, w - 1)) or (t == 1 and s in (0, w - 1)):
        return pick(ctx.pal["gold"], 0.8 + (0.1 if t == 0 else 0.0))
    if t == 0 and s in (6, 7):
        col = pick(ctx.pal["rose"], 0.9)
        ctx.glow = col
        return col
    if (t == 2 and s in (2, 4, 9, 11)) or (t == 0 and s in (2, 3, 4, 9, 10, 11)):
        return pick(ctx.pal["coat"], 0.12)                              # dunkle Ränder um die Augen
    if hash01(ctx.u, ctx.v, "sequin") > 0.86:
        col = pick(ctx.pal["satin"], 0.95)
        ctx.glow = mix((0, 0, 0, 255), col, 0.6)
        return col
    return mix(pick(ctx.pal["satin"], 0.42), pick(ctx.pal["magenta"], 0.5), 0.25 + 0.15 * (t / 3.0))


def _hat(ctx: Ctx):
    """Zylinder: tiefschwarz-violetter Seidenfilz mit einem senkrechten Glanzstreifen."""
    face = ctx.face
    if face == "bottom":
        return pick(ctx.pal["hat"], 0.08)                              # innen (sieht man beim Hut-Trick)
    if face == "top":
        lx, _, lz = ctx.local
        rr = math.hypot(lx, lz)
        return pick(ctx.pal["hat"], 0.3 + (0.06 if 1.4 < rr < 2.0 else 0.0) + _n(ctx, 0.8, 21) * 0.08)
    v = 0.22 + _LIT[face] + _n(ctx, 0.5, 22) * 0.1
    # Glanz: ein senkrechter, weicher Streifen auf der Vorderseite und den Seiten
    rs = (ctx.s + 0.5) / max(ctx.w, 1)
    if face in ("front", "west"):
        v += max(0.0, 0.22 - abs(rs - 0.3) * 0.9)
    return pick(ctx.pal["hat"], v)


def _hat_top(ctx: Ctx):
    if ctx.face == "top":
        return _hat(ctx)
    return pick(ctx.pal["hat"], 0.42 + _LIT[ctx.face])


def _hat_brim(ctx: Ctx):
    if ctx.face == "top":
        return pick(ctx.pal["hat"], 0.26 + _n(ctx, 0.6, 23) * 0.08)
    if ctx.face == "bottom":
        return pick(ctx.pal["satin"], 0.22)                             # Unterseite mit violettem Futter
    return pick(ctx.pal["hat"], 0.4 + _LIT[ctx.face])


def _band(ctx: Ctx):
    """Satinband in Magenta mit zwei feinen Goldlinien und funkelnden Sternchen."""
    if ctx.face in ("top", "bottom"):
        return pick(ctx.pal["magenta"], 0.3)
    if ctx.t in (0, ctx.h - 1):
        return pick(ctx.pal["gold"], 0.7)
    if hash01(ctx.u, ctx.v, "band") > 0.9:
        col = pick(ctx.pal["gold"], 0.95)
        ctx.glow = col
        return col
    return pick(ctx.pal["magenta"], 0.62 + _LIT[ctx.face] + (0.12 if ctx.t == 1 else 0.0))


def _buckle(ctx: Ctx):
    if ctx.face == "front":
        inner = 0 < ctx.s < ctx.w - 1 and 0 < ctx.t < ctx.h - 1
        if inner:
            return pick(ctx.pal["magenta"], 0.45)
    return _gold(ctx)


def _gold(ctx: Ctx):
    r = rel_y(ctx)
    v = 0.62 + _LIT[ctx.face] - r * 0.15 + _n(ctx, 1.5, 24) * 0.12
    if hash01(ctx.u, ctx.v, "glint") > 0.93:
        v += 0.25
    return pick(ctx.pal["gold"], v)


def _flower(ctx: Ctx):
    """Feenrose: Blütenblätter in Ringen, das Herz leuchtet."""
    if ctx.face == "back" or ctx.face == "bottom":
        return pick(ctx.pal["leaf"], 0.4)
    cx = (ctx.s + 0.5) / max(ctx.w, 1) - 0.5
    cy = (ctx.t + 0.5) / max(ctx.h, 1) - 0.5
    r = math.hypot(cx, cy)
    a = math.atan2(cy, cx)
    if r < 0.14:
        col = pick(ctx.pal["rose"], 0.98)
        ctx.glow = col
        return col
    if r > 0.5:
        if (cx > 0) == (cy > 0):
            return pick(ctx.pal["leaf"], 0.5 + _LIT[ctx.face])            # zwei Blättchen an den Ecken
        return pick(ctx.pal["rose"], 0.3)
    # gewundene Blütenblätter: Spirale aus hellen und dunklen Bändern
    swirl = math.sin(a * 2.0 + r * 14.0)
    col = pick(ctx.pal["rose"], 0.55 + swirl * 0.18 - r * 0.2)
    ctx.glow = mix((0, 0, 0, 255), col, 0.3)
    return col


def _feather(ctx: Ctx):
    """Feder: weißer Kiel, Fahnen von Rosa zu Türkis, gezackte Ränder (durchsichtig)."""
    if ctx.face not in ("west", "east"):
        return None
    _, ly, lz = ctx.local
    k = -ly / 9.0                                                       # 0 = Kiel unten, 1 = Spitze oben
    half = 1.25 * math.sin(math.pi * min(1.0, max(0.0, k)) ** 0.7)
    if abs(lz) > half or k < 0.05:
        if abs(lz) < 0.18 and k >= -0.02:
            return pick(ctx.pal["white"], 0.9)
        return None
    if abs(lz) < 0.18:
        col = pick(ctx.pal["white"], 0.95)
        ctx.glow = mix((0, 0, 0, 255), col, 0.4)
        return col
    if abs(lz) > half - 0.25 and hash01(ctx.u, ctx.v, "barb") > 0.5:
        return None
    col = mix(pick(ctx.pal["wing_tip"], 0.6), pick(ctx.pal["wing_mid"], 0.7), k)
    ctx.glow = mix((0, 0, 0, 255), col, 0.5)
    return col


def _coat_value(ctx: Ctx) -> float:
    return 0.36 + _LIT[ctx.face] - rel_y(ctx) * 0.06 + _n(ctx, 0.6, 25) * 0.08


def _coat(ctx: Ctx):
    """
    Frack (Brust, 14×14 vorn): ein V aus weißem Hemd mit Knopfleiste, Revers aus hellem Satin, zwei Reihen goldener Knöpfe;
    hinten eine Mittelnaht, an den Seiten feine Satin-Paspeln.
    """
    face, s, t = ctx.face, ctx.s, ctx.t
    if face == "front":
        d = abs(s - 6.5)
        shirt = 2.6 - t * 0.26
        if t < 10 and d < shirt:
            if d < 0.6:
                return pick(ctx.pal["white"], 0.62)                     # Knopfleiste
            return pick(ctx.pal["white"], 0.86 - t * 0.012 + ((s + t) % 2) * 0.03)
        if t < 11 and d < shirt + 1.7:
            edge = d > shirt + 1.2
            return pick(ctx.pal["satin"], 0.58 + (0.14 if edge else 0.0) - t * 0.012)
        if (s in (3, 10)) and t in (10, 12):
            col = pick(ctx.pal["gold"], 0.9)
            ctx.glow = mix((0, 0, 0, 255), col, 0.35)
            return col
        return pick(ctx.pal["coat"], _coat_value(ctx))
    if face == "back" and s in (6, 7):
        return pick(ctx.pal["coat"], _coat_value(ctx) - 0.1)
    if face in ("west", "east") and s in (0, ctx.w - 1):
        return pick(ctx.pal["satin"], 0.4)
    return pick(ctx.pal["coat"], _coat_value(ctx))


def _waist(ctx: Ctx):
    face, s, t = ctx.face, ctx.s, ctx.t
    if face == "front":
        if s in (5, 6):
            return pick(ctx.pal["coat"], _coat_value(ctx) - 0.12)        # die Kante, an der der Frack schließt
        if s in (2, 9) and t == 1:
            col = pick(ctx.pal["gold"], 0.9)
            ctx.glow = mix((0, 0, 0, 255), col, 0.35)
            return col
    return pick(ctx.pal["coat"], _coat_value(ctx))


def _belt(ctx: Ctx):
    if ctx.face == "front" and 5 <= ctx.s <= 6:
        return pick(ctx.pal["gold"], 0.85)
    if ctx.face in ("top", "bottom"):
        return pick(ctx.pal["magenta"], 0.32)
    return pick(ctx.pal["magenta"], 0.48 + _LIT[ctx.face] + (0.08 if ctx.t == 0 else 0.0))


def _collar(ctx: Ctx):
    return pick(ctx.pal["white"], 0.86 + _LIT[ctx.face] - rel_y(ctx) * 0.1)


def _bowtie(ctx: Ctx):
    """Fliege: zwei Schlaufen, der Knoten in der Mitte dunkler, Glanz auf den Schlaufen."""
    if ctx.face == "front":
        rs = (ctx.s + 0.5) / max(ctx.w, 1)
        if 0.4 < rs < 0.6:
            return pick(ctx.pal["magenta"], 0.36)
        pinch = abs(rs - 0.5) * 2.0
        if ctx.t in (0, ctx.h - 1) and pinch < 0.45:
            return None
        return pick(ctx.pal["magenta"], 0.56 + (0.16 if ctx.t == 0 else 0.0))
    return pick(ctx.pal["magenta"], 0.42 + _LIT[ctx.face])


def _shoulder(ctx: Ctx):
    if ctx.face == "top":
        return pick(ctx.pal["satin"], 0.42 + _n(ctx, 1.0, 26) * 0.08)
    return pick(ctx.pal["coat"], _coat_value(ctx) + 0.04)


def _sleeve(ctx: Ctx):
    v = _coat_value(ctx)
    if ctx.face in ("west", "east") and ctx.s == ctx.w // 2:
        v -= 0.08                                                       # Naht
    return pick(ctx.pal["coat"], v)


def _cuff(ctx: Ctx):
    if ctx.face in ("top", "bottom"):
        return pick(ctx.pal["gold"], 0.5)
    return pick(ctx.pal["gold"], 0.7 + _LIT[ctx.face] + (0.12 if ctx.s % 3 == 0 else 0.0))


def _glove(ctx: Ctx):
    """Weiße Handschuhe: Fingerrillen vorn und unten, die Fingerspitzen etwas dunkler."""
    v = 0.86 + _LIT[ctx.face] - rel_y(ctx) * 0.08
    if ctx.face in ("front", "bottom") and ctx.s % 2 == 1 and rel_y(ctx) > 0.45:
        v -= 0.12
    if ctx.face == "bottom":
        v -= 0.06
    return pick(ctx.pal["white"], v)


def _wand(ctx: Ctx):
    v = 0.3 + _LIT[ctx.face] + (0.25 if ctx.face == "top" and ctx.s == 0 else 0.0)
    return pick(ctx.pal["wand"], v)


def _wand_tip(ctx: Ctx):
    col = pick(ctx.pal["white"], 0.95 + _LIT[ctx.face] * 0.5)
    ctx.glow = mix((0, 0, 0, 255), col, 0.7)
    return col


def _wand_end(ctx: Ctx):
    return pick(ctx.pal["white"], 0.88 + _LIT[ctx.face])


# Kartenfarben (7×7): Pik, Herz, Kreuz, Karo
SUITS = {
    "spade": [
        "...#...",
        "..###..",
        ".#####.",
        "#######",
        "#######",
        ".#.#.#.",
        "..###..",
    ],
    "heart": [
        ".##.##.",
        "#######",
        "#######",
        "#######",
        ".#####.",
        "..###..",
        "...#...",
    ],
    "club": [
        "..###..",
        "..###..",
        "##.#.##",
        "#######",
        "##.#.##",
        "...#...",
        "..###..",
    ],
    "diamond": [
        "...#...",
        "..###..",
        ".#####.",
        "#######",
        ".#####.",
        "..###..",
        "...#...",
    ],
}

# kleine Kartenfarben (3×3) für die Karten in der Hand
_SMALL = {
    "heart": ["#.#", "###", ".#."],
    "spade": [".#.", "###", "#.#"],
    "diamond": [".#.", "###", ".#."],
    "club": ["###", "###", ".#."],
}


def _card(ctx: Ctx):
    """
    Karte im Fächer (5×7): vorn weiß mit rotem Rand und einer Farbe in der Mitte (abwechselnd Herz, Pik, Karo, Kreuz,
    Herz), hinten das violett-goldene Rautenmuster der Zauberkarten.
    """
    i = int(ctx.cube.tag or 0)
    s, t = ctx.s, ctx.t
    if ctx.face == "back":
        if s in (0, ctx.w - 1) or t in (0, ctx.h - 1):
            return pick(ctx.pal["gold"], 0.75)
        if (s + t) % 2 == 0:
            return pick(ctx.pal["satin"], 0.35)
        return pick(ctx.pal["coat"], 0.42)
    if ctx.face != "front":
        return pick(ctx.pal["white"], 0.8)
    suit = ("heart", "spade", "diamond", "club", "heart")[i % 5]
    red = suit in ("heart", "diamond")
    grid = _SMALL[suit]
    gx, gy = s - 1, t - 2
    if 0 <= gx < 3 and 0 <= gy < 3 and grid[gy][gx] == "#":
        return pick(ctx.pal["card_red" if red else "card_black"], 0.7)
    if (s, t) in ((0, 0), (ctx.w - 1, ctx.h - 1)):
        return pick(ctx.pal["card_red" if red else "card_black"], 0.6)   # Eck-Index
    return pick(ctx.pal["white"], 0.95 - t * 0.01)


def _coattails(ctx: Ctx):
    """Zwei lange Schöße mit Schwalbenschwanz-Spitzen und goldener Paspel."""
    lx, ly, _ = ctx.local
    r = ly / 11.0
    ax = abs(lx)
    if ax < 0.4 and r > 0.22:
        return None                                                     # der Schlitz zwischen den Schößen
    u = (ax - 0.4) / 2.6                                                # 0 innen, 1 außen
    cut = 1.0 - 0.3 * u
    if r > cut:
        return None
    edge = r > cut - 0.06 or ax > 2.75 or (ax < 0.65 and r > 0.22)
    if edge and ctx.face in ("front", "back", "west", "east"):
        return pick(ctx.pal["gold"], 0.62)
    if ctx.face == "front":
        return pick(ctx.pal["satin"], 0.3)                              # Futter
    return pick(ctx.pal["coat"], _coat_value(ctx) + 0.02)


def _skirt(ctx: Ctx):
    """Faltenrock: Falten im Wechsel hell und dunkel, nach unten heller."""
    if ctx.face == "top":
        return pick(ctx.pal["skirt"], 0.3)
    if ctx.face == "bottom":
        return pick(ctx.pal["skirt"], 0.18)
    pleat = (ctx.s // 2) % 2
    v = 0.42 + _LIT[ctx.face] + rel_y(ctx) * 0.12 + pleat * 0.1
    return pick(ctx.pal["skirt"], v)


def _skirt_flare(ctx: Ctx):
    if ctx.face == "bottom":
        lx, _, lz = ctx.local
        if abs(lx) < 3.0 and abs(lz) < 1.9:
            return pick(ctx.pal["stocking"], 0.2)                       # innen dunkel
        return pick(ctx.pal["skirt"], 0.22)
    if ctx.face != "top" and ctx.t == ctx.h - 1:
        # goldener Saum mit kleinen leuchtenden Sternen
        if ctx.s % 4 == 1:
            col = pick(ctx.pal["gold"], 0.98)
            ctx.glow = col
            return col
        return pick(ctx.pal["gold"], 0.66)
    return _skirt(ctx)


def _stocking(ctx: Ctx):
    """Dunkle Strümpfe mit zartem Rautenmuster in Flieder."""
    x, y, z = ctx.world
    a = (y + x * 1.0 + z * 0.6) % 2.0
    b = (y - x * 1.0 - z * 0.6) % 2.0
    v = 0.3 + _LIT[ctx.face] * 0.8
    if a < 0.32 or b < 0.32:
        v += 0.16
    return pick(ctx.pal["stocking"], v)


def _boot(ctx: Ctx):
    """Lackstiefel: dunkel mit einem hellen Glanzstreifen vorn, die Sohle fast schwarz."""
    if ctx.face == "bottom":
        return pick(ctx.pal["boot"], 0.05)
    v = 0.24 + _LIT[ctx.face]
    if ctx.face == "front" and ctx.s == ctx.w // 2 - 1:
        v += 0.32
    if ctx.face in ("west", "east") and ctx.s == 1:
        v += 0.14
    return pick(ctx.pal["boot"], v)


def _boot_cuff(ctx: Ctx):
    if ctx.face in ("top", "bottom"):
        return pick(ctx.pal["boot"], 0.3)
    if ctx.face == "front" and ctx.s in (2, 3):
        return pick(ctx.pal["gold"], 0.9)                               # Schnalle
    return pick(ctx.pal["satin"], 0.34 + _LIT[ctx.face])


def _heel(ctx: Ctx):
    return pick(ctx.pal["gold"] if ctx.face != "bottom" else ctx.pal["boot"], 0.5)


_VOR = Voronoi3D(2.2, 31)


def _wing(ctx: Ctx):
    """
    Feenflügel: eine Tropfenform vom Ansatz zur Spitze. Die Haut schillert vom Violett am Ansatz über Türkis zu Rosa an der
    Spitze, unterteilt in Zellen wie ein Kirchenfenster; goldene Adern laufen vom Ansatz zum Rand, der Rand ist dunkel mit
    leuchtenden Punkten, hier und da funkelt es. Auf jedem Flügel leuchtet eine Spielkartenfarbe.
    """
    if ctx.face not in ("west", "east"):
        return None
    kind = ctx.cube.tag
    ty, tz, wmax = WINGS[kind]
    _, ly, lz = ctx.local
    length = math.hypot(ty, tz)
    ay, az = ty / length, tz / length
    u = (ly * ay + lz * az) / length                                    # 0 Ansatz … 1 Spitze
    v = (ly * -az + lz * ay)                                            # quer dazu (Einheiten)
    if u <= 0.0 or u >= 1.0:
        return None
    half = wmax * (u ** 0.55) * math.sqrt(max(0.0, 1.0 - u ** 2.6))
    # die Hinterkante ist etwas voller als die Vorderkante
    half *= 1.08 if v > 0 else 0.92
    if abs(v) > half:
        return None
    left = ctx.cube.bone.name.startswith("left")
    # Kartenfarbe nahe der Spitze
    suit = {("upper", True): "spade", ("upper", False): "heart", ("lower", True): "club", ("lower", False): "diamond"}[(kind, left)]
    cu = 0.64 if kind == "upper" else 0.6
    cy, cz = ay * cu * length, az * cu * length
    col = int(math.floor((lz - cz) / 0.5 + 3.5))
    row = int(math.floor((ly - cy) / 0.5 + 3.5))
    in_suit_box = -1 <= col <= 7 and -1 <= row <= 7
    if in_suit_box:
        grid = SUITS[suit]

        def filled(r: int, c: int) -> bool:
            return 0 <= r < 7 and 0 <= c < 7 and grid[r][c] == "#"

        if filled(row, col):
            red = suit in ("heart", "diamond")
            c = pick(ctx.pal["card_red"] if red else ctx.pal["vein"], 0.92 if red else 0.62)
            if (row + col) % 4 == 0:
                c = mix(c, (255, 255, 255, 255), 0.25)                 # ein wenig Glanz
            ctx.glow = mix((0, 0, 0, 255), c, 0.45)
            return c[0], c[1], c[2], 250
        if any(filled(row + dr, col + dc) for dr in (-1, 0, 1) for dc in (-1, 0, 1)):
            c = pick(ctx.pal["wing_rim"], 0.15)                         # dunkler Umriss: die Farbe hebt sich ab
            return c[0], c[1], c[2], 240
    edge = half - abs(v)
    if edge < 0.55:
        if edge < 0.3 and hash01(ctx.u, ctx.v, "rimdot") > 0.9:
            c = pick(ctx.pal["vein"], 0.85)
            ctx.glow = c
            return c[0], c[1], c[2], 245
        c = pick(ctx.pal["wing_rim"], 0.5)
        return c[0], c[1], c[2], 235
    # Adern: Hauptader entlang der Achse, schräge Rippen zum Rand (nicht durch die Kartenfarbe)
    along_units = u * length
    rib = ((along_units * 0.8 + abs(v) * 1.4) % 4.4) < 0.4 and abs(v) > 0.7
    if (abs(v) < 0.28 or rib) and not in_suit_box:
        c = pick(ctx.pal["vein"], 0.5 + u * 0.2)
        ctx.glow = mix((0, 0, 0, 255), c, 0.45)
        return c[0], c[1], c[2], 225
    # Haut: Farbverlauf und Zellen
    if u < 0.5:
        base = mix(ctx.pal["wing_base"][0], ctx.pal["wing_mid"][0], u / 0.5)
        light = mix(ctx.pal["wing_base"][1], ctx.pal["wing_mid"][1], u / 0.5)
    else:
        base = mix(ctx.pal["wing_mid"][0], ctx.pal["wing_tip"][0], (u - 0.5) / 0.5)
        light = mix(ctx.pal["wing_mid"][1], ctx.pal["wing_tip"][1], (u - 0.5) / 0.5)
    d1, d2 = _VOR(0.0, ly, lz + (100.0 if left else 0.0) + (50.0 if kind == "lower" else 0.0))
    cell_edge = (d2 - d1) < 0.18
    k = 0.35 + 0.5 * (abs(v) / max(half, 0.01)) + (hash01(ctx.u, ctx.v) - 0.5) * 0.08
    c = mix(base, light, min(1.0, k))
    if cell_edge:
        c = mix(c, (30, 12, 60, 255), 0.35)
        return c[0], c[1], c[2], 215
    if hash01(ctx.u, ctx.v, "sparkle") > 0.992:
        c = (255, 250, 255, 255)
        ctx.glow = c
        return c[0], c[1], c[2], 240
    # zarter Schimmer: die Mitte der Zellen leuchtet ein wenig
    ctx.glow = mix((0, 0, 0, 255), c, 0.22)
    return c[0], c[1], c[2], 175 + int(u * 30)


def materials(variant: str) -> dict:
    return {
        "skin": _skin, "face": _face, "hair": _hair, "bangs": _bangs, "hair_back": _hair_back, "lock": _lock,
        "ponytail": _ponytail, "ear": _ear, "mask": _mask, "hat": _hat, "hat_top": _hat_top, "hat_brim": _hat_brim,
        "band": _band, "buckle": _buckle, "gold": _gold, "flower": _flower, "feather": _feather, "coat": _coat,
        "waist": _waist, "belt": _belt, "collar": _collar, "bowtie": _bowtie, "shoulder": _shoulder, "sleeve": _sleeve,
        "cuff": _cuff, "glove": _glove, "wand": _wand, "wand_tip": _wand_tip, "wand_end": _wand_end, "card": _card,
        "coattails": _coattails, "skirt": _skirt, "skirt_flare": _skirt_flare, "stocking": _stocking, "boot": _boot,
        "boot_cuff": _boot_cuff, "heel": _heel, "wing": _wing,
    }
