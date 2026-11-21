"""
Glutkeiler (statt Hoglin) und Moderkeiler (statt Zoglin) auf dem Skelett des Vanilla-Hoglins: dieselben Knochen und
Drehpunkte wie HoglinModel (body mit mane, head mit den Ohren und den Hauern, vier Beine), damit Laufen, Ohrenschlagen
und der Kopfstoß genauso laufen. Eigene Quader und Bilder – ein schwerer Keiler des Nethers:

  * ein hoher Schulterbuckel unter einem zotteligen Fellumhang, darunter ein abfallender Rücken mit kräftigem Hinterteil,
    ein Haarbehang am Bauch und ein Schwanz mit Quaste
  * eine Borstenmähne vom Nacken bis zur Rückenmitte: ein fester Kamm und darüber drei Fächer aus steifen Borsten
  * ein langer Keilerkopf: breiter Schädel mit Stirnwulst und kleinen, glühenden Augen, Backenbart, eine schmale Schnauze
    mit Rüsselscheibe und Unterkiefer, zwei lange, nach oben geschwungene Hauer und Schlappohren
  * kräftige Läufe mit Zotteln über den gespaltenen Hufen

Gestalten: Karmesinkeiler (Karmesinwald – rostrot, auf dem Rücken wachsen Karmesinpilze und -wurzeln), Aschenkeiler
(aschgrau, die Borstenspitzen glimmen wie Glut), Schlackenkeiler (selten – schwarz, Schultern aus Magmakruste mit
glühenden Rissen). Der Moderkeiler trägt dieselbe Gestalt verwest: fleckige, grünliche Haut, offene Stellen, freiliegende
Rippen und Stirnknochen, eine räudige Mähne, ein abgebrochener Hauer und ein glimmendes Auge.
"""
from __future__ import annotations

import math

from ..geometry import Model
from ..paint import Ctx, Voronoi3D, hash01, hexc, mix, noise3, pick, ramp, rel_y

_COMMON = {
    "tusk": ramp("#6e5a40", "#fff6dc", steps=7),
    "hoof": ramp("#0e0a08", "#5a4a40", steps=5),
    "bone": ramp("#4e4636", "#ece4cc", steps=7),
    "socket": hexc("#120806"),
}

PALETTES = {
    "karmesin": {**_COMMON,
                 "coat": ramp("#1e0806", "#c4603e", steps=10, hue_shift=-0.03),
                 "mane": ramp("#160404", "#b8301e", steps=8), "tip": ramp("#a8341c", "#ff9a64", steps=6),
                 "hide": ramp("#2e1410", "#d89a86", steps=8, hue_shift=-0.03),
                 "snout": ramp("#3a1a18", "#f0a898", steps=8, hue_shift=-0.03),
                 "eye": hexc("#ffb43c"), "tip_glow": None,
                 "cap": ramp("#3a0606", "#e83a2a", steps=7), "stem": ramp("#5a2a2a", "#e8b4a0", steps=5),
                 "root": ramp("#4a0a0a", "#d8402c", steps=6)},
    "aschen": {**_COMMON,
               "coat": ramp("#141212", "#a8a098", steps=10, hue_shift=-0.01),
               "mane": ramp("#0e0c0c", "#78706a", steps=8), "tip": ramp("#c0501c", "#ffc070", steps=6),
               "hide": ramp("#241e1e", "#b8a8a0", steps=8),
               "snout": ramp("#2e2424", "#d0b0a8", steps=8),
               "eye": hexc("#ff8a34"), "tip_glow": hexc("#ff8c3a")},
    "schlacke": {**_COMMON,
                 "coat": ramp("#0c0909", "#76605a", steps=10, hue_shift=-0.02),
                 "mane": ramp("#060404", "#4a3028", steps=8), "tip": ramp("#e06a1c", "#ffe08a", steps=6),
                 "hide": ramp("#1a1212", "#8a706a", steps=8),
                 "snout": ramp("#1e1414", "#a07870", steps=8),
                 "eye": hexc("#ffe066"), "tip_glow": hexc("#ffb040"),
                 "crust": ramp("#0a0606", "#5a4038", steps=6), "magma": ramp("#a8280a", "#ffe890", steps=6),
                 "magma_glow": hexc("#ff9a30")},
    "moder_karmesin": {**_COMMON,
                       "coat": ramp("#141810", "#7a8462", steps=10, hue_shift=0.02),
                       "mane": ramp("#0e0c08", "#5a4a34", steps=8), "tip": ramp("#5a4a34", "#9a8a68", steps=6),
                       "hide": ramp("#20281a", "#a8b88a", steps=8, hue_shift=0.02),
                       "snout": ramp("#2a2a1e", "#b0b08a", steps=8),
                       "flesh": ramp("#2a0c0c", "#94463e", steps=7), "glow": hexc("#ffe060"), "tip_glow": None},
    "moder_aschen": {**_COMMON,
                     "coat": ramp("#141412", "#86887a", steps=10, hue_shift=0.02),
                     "mane": ramp("#0c0c0a", "#4e4a42", steps=8), "tip": ramp("#4e4a42", "#8a8678", steps=6),
                     "hide": ramp("#22241e", "#b0b4a0", steps=8, hue_shift=0.02),
                     "snout": ramp("#28281e", "#b4b098", steps=8),
                     "flesh": ramp("#261212", "#8a4c46", steps=7), "glow": hexc("#a0ff80"), "tip_glow": None},
}

LOOKS = ("karmesin", "aschen", "schlacke")

# Pilze und Wurzeln auf dem Rücken des Karmesinkeilers: (x, z, Stiel-Höhe) im Körper-Knochen
_FUNGI = ((3.2, 3.5, 2.2), (-2.6, 7.2, 1.6))
_ROOTS = ((-3.8, 1.2, 2.4), (2.4, 9.4, 2.0), (0.4, 5.6, 1.8))


def build() -> Model:
    m = Model("hoglin", 256, 128)
    m.world_faces = True

    # ---------------------------------------------------------------- Rumpf (Drehpunkt wie Vanilla: 0, 7, 0)
    m.bone("body", pivot=(0, 7, 0))
    m.cube("body", (-8, -7, -13), (16, 14, 13), "chest")                     # Brust und Schultern
    m.cube("body", (-7, -9.4, -12.2), (14, 2.6, 10.4), "hump")               # Widerrist
    m.cube("body", (-7.2, -6.0, -1.2), (14.4, 2.0, 5.0), "rump")             # Übergang zum Hinterteil
    m.cube("body", (-7.5, -4.6, 0), (15, 11.2, 12.5), "rump")                # Hinterteil, der Rücken fällt ab
    m.cube("body", (-8.6, -9.0, -13.6), (17.2, 12.8, 11.6), "shag",          # zotteliger Fellumhang über den Schultern
           faces=("west", "east", "front"))
    m.cube("body", (-8.1, 4.4, -11.5), (16.2, 3.4, 21), "fringe", faces=("west", "east"))   # Haarbehang am Bauch
    m.cube("body", (-0.75, -3.8, 12.4), (1.5, 5.5, 1), "tail")
    m.cube("body", (-1.3, 1.2, 12.1), (2.6, 2.8, 1.6), "tuft")              # Schwanzquaste
    m.cube("body", (-8.4, -9.8, -12.6), (16.8, 5.4, 9.8), "crust", faces=("top", "west", "east", "front"))  # Magmakruste
    for x, z, h in _FUNGI:                                                   # Karmesinpilze (nur der Karmesinkeiler)
        m.cube("body", (x - 0.5, -4.6 - h, z - 0.5), (1, h, 1), "stem")
        m.cube("body", (x - 2, -4.6 - h - 1.4, z - 2), (4, 1.4, 4), "cap")
    for x, z, h in _ROOTS:                                                   # Karmesinwurzeln (Kreuz aus zwei Ebenen)
        m.cube("body", (x - 1.2, -4.6 - h, z), (2.4, h, 0), "root")
        m.cube("body", (x, -4.6 - h, z - 1.2), (0, h, 2.4), "root")

    # Mähne (Drehpunkt wie Vanilla): fester Kamm auf dem Widerrist, darüber drei Borstenfächer
    m.bone("mane", "body", pivot=(0, -14, -7))
    m.cube("mane", (-2.0, 2.4, -8.6), (4.0, 5.6, 16.4), "crest")
    m.cube("mane", (0, -2.4, -9.2), (0, 10.4, 18.4), "spines")
    m.bone("mane_left", "mane", pivot=(1.2, 4.0, 0), rotation=(0, 0, 30))
    m.cube("mane_left", (0, -7.4, -8.6), (0, 8, 16.6), "spines")
    m.bone("mane_right", "mane", pivot=(-1.2, 4.0, 0), rotation=(0, 0, -30))
    m.cube("mane_right", (0, -7.4, -8.6), (0, 8, 16.6), "spines")

    # ---------------------------------------------------------------- Kopf (Drehpunkt und Neigung wie Vanilla)
    m.bone("head", pivot=(0, 2, -12), rotation=(50, 0, 0))
    m.cube("head", (-6.5, -3.5, -9), (13, 7, 9), "skull")
    m.cube("head", (-6.9, -4.6, -6.8), (13.8, 1.3, 2.6), "brow")             # Stirnwulst über den Augen
    m.cube("head", (-5, -3, -17), (10, 6, 8), "muzzle")
    m.cube("head", (-4.2, -2.6, -19), (8.4, 5.2, 2), "disk")                 # Rüsselscheibe
    m.cube("head", (-4.4, 3, -15.6), (8.8, 1.6, 9), "jaw")                   # Unterkiefer
    m.cube("head", (-7.6, -2.8, -8.2), (1.4, 5.8, 6.4), "cheek")             # Backenbart
    m.cube("head", (6.2, -2.8, -8.2), (1.4, 5.8, 6.4), "cheek")
    for side, x in (("right", -1), ("left", 1)):
        horn = f"{side}_horn"
        m.bone(horn, "head", pivot=(5.4 * x, 1.6, -14.2))
        m.cube(horn, (-1, -5.4, -1), (2, 5.4, 2), "tusk")
        tip = f"{side}_horn_tip"
        m.bone(tip, horn, pivot=(0, -5.2, 0), rotation=(-30, 0, 12 * x))
        m.cube(tip, (-0.75, -4.6, -0.75), (1.5, 4.6, 1.5), "tusk_tip")
        ear = f"{side}_ear"
        m.bone(ear, "head", pivot=(6.0 * x, -2.8, -3), rotation=(0, 0, 40 * x))
        ox = -5.5 if x < 0 else 0
        m.cube(ear, (ox, -0.5, -2.2), (5.5, 1, 4.4), "ear")

    # ---------------------------------------------------------------- Läufe (Drehpunkte wie Vanilla)
    for side, x in (("right", -4.0), ("left", 4.0)):
        leg = f"{side}_front_leg"
        m.bone(leg, pivot=(x, 10, -8.5))
        m.cube(leg, (-3, 0, -3), (6, 8.5, 6), "leg_fur")
        m.cube(leg, (-2.5, 8.5, -2.5), (5, 5.5, 5), "leg")
    for side, x in (("right", -5.0), ("left", 5.0)):
        leg = f"{side}_hind_leg"
        m.bone(leg, pivot=(x, 13, 10))
        m.cube(leg, (-3, 0, -3), (6, 6.2, 6), "leg_fur")
        m.cube(leg, (-2.2, 6.2, -2.2), (4.4, 4.8, 4.4), "leg")
    return m


# =============================================================================================
# Bemalen
# =============================================================================================

_LIT = {"top": 0.08, "front": 0.03, "west": 0.0, "east": -0.03, "back": -0.05, "bottom": -0.17}
_CRUST = Voronoi3D(4.2, 41)


def _v(ctx: Ctx) -> str:
    return ctx.params.get("variant", "karmesin")


def _rotten(ctx: Ctx) -> bool:
    return _v(ctx).startswith("moder")


def _lit(ctx: Ctx) -> float:
    return _LIT.get(ctx.face, 0.0)


def _streak(column: float, along: float, seed: int, length: float) -> float:
    """Kurze Borstenstriche: jede Spalte in ungleich lange Abschnitte geteilt, jeder etwas heller oder dunkler."""
    c = math.floor(column)
    seg = 2.0 + hash01(c, seed, "len") * length
    k = math.floor((along + hash01(c, seed, "off") * 9.0) / seg)
    return (hash01(c, k, seed, "st") - 0.5) * 0.16


def _hair(ctx: Ctx, base: float, key: str = "coat", seed: int = 1, length: float = 3.0) -> tuple:
    """
    Borstiges Fell: ruhige Grundfarbe mit großen, weichen Flecken, darin kurze Striche in Wuchsrichtung (an den
    Flanken nach unten, auf dem Rücken nach hinten) und vereinzelte hell gespitzte Grannen; der Rücken dunkler (Aalstrich),
    die Flanken in der Mitte am hellsten. Beim Moderkeiler räudig und fleckig, mit offenen Stellen.
    """
    x, y, z = ctx.world
    face = ctx.face
    v = base + _lit(ctx) + (noise3(x * 0.07, y * 0.07, z * 0.07, seed) - 0.5) * 0.1
    if face in ("top", "bottom"):
        v += _streak(x * 1.3, z, seed + 5, length) * 0.75
    else:
        v += _streak(z if face in ("west", "east") else x, y, seed, length) * 0.75
    if face == "top":
        v -= 0.07 * max(0.0, 1.0 - abs(x) / 4.0)                               # dunkler Aalstrich auf dem Rücken
    if hash01(ctx.u, ctx.v, seed, "gz") > 0.965:
        v += 0.13                                                                # hell gespitzte Granne
    if _rotten(ctx):
        n = noise3(x * 0.34, y * 0.34, z * 0.34, seed + 19)
        if n > 0.8:
            return pick(ctx.pal["flesh"], 0.28 + (n - 0.8) * 1.5)               # offene Stelle
        if n > 0.68:
            return pick(ctx.pal["hide"], 0.32 + (hash01(ctx.u, ctx.v, "sc") - 0.5) * 0.1)   # kahle, faulige Haut
    return pick(ctx.pal[key], v)


def _hide(ctx: Ctx, base: float, seed: int = 3):
    """Fast nackte Haut (Bauch, Schnauze): fein gesprenkelt."""
    x, y, z = ctx.world
    v = base + _lit(ctx) + (noise3(x * 0.3, y * 0.3, z * 0.3, seed) - 0.5) * 0.1
    if hash01(ctx.u, ctx.v, "pore") > 0.86:
        v -= 0.07
    if _rotten(ctx) and noise3(x * 0.25, y * 0.25, z * 0.25, seed + 9) > 0.68:
        return pick(ctx.pal["flesh"], 0.35)
    return pick(ctx.pal["hide"], v)


def _ragged(ctx: Ctx, seed: str, depth: int = 3) -> bool:
    """Unten ausgefranst: liegt das Texel unterhalb der Spitze seiner Haarsträhne?"""
    col = math.floor(ctx.world[2] if ctx.lface in ("west", "east") else ctx.world[0])
    cut = depth * (0.25 + 0.75 * hash01(col, seed))
    return ctx.t >= ctx.h - cut


# ---------------------------------------------------------------------------------- Rumpf

def _chest(ctx: Ctx):
    face = ctx.face
    if face == "bottom":
        return _hide(ctx, 0.42)
    if face == "front":
        return _hair(ctx, 0.36, seed=2)                                          # Hals unter dem Kinn
    return _hair(ctx, 0.46, seed=2)


def _ribs(ctx: Ctx):
    """Moderkeiler: an der Flanke hinter dem Umhang liegen drei Rippen frei, umrandet von zerrissener Haut."""
    x, y, z = ctx.world
    if not (0.6 <= z <= 6.6 and 5.6 <= y <= 10.6):
        return None
    edge = min(z - 0.6, 6.6 - z, y - 5.6, 10.6 - y)
    if edge < 0.9 and hash01(math.floor(z), math.floor(y), "rim") > 0.3:
        return pick(ctx.pal["hide"], 0.22)                                       # zerrissener Rand
    if math.floor(y - 5.6) % 2 == 1:
        return pick(ctx.pal["bone"], 0.62 + (1.0 - abs(z - 3.6) / 3.0) * 0.12)   # Rippe
    return pick(ctx.pal["flesh"], 0.12)


def _rump(ctx: Ctx):
    face, s, t = ctx.face, ctx.s, ctx.t
    if face == "bottom":
        return _hide(ctx, 0.4)
    if _rotten(ctx) and face in ("west", "east"):
        rib = _ribs(ctx)
        if rib is not None:
            return rib
    if face == "back":
        cx = abs(s - (ctx.w - 1) / 2.0)
        if cx < 2.5 and t > ctx.h * 0.55:
            return _hide(ctx, 0.3)                                               # unter dem Schwanz
        return _hair(ctx, 0.4 - (0.06 if cx < 1.5 else 0.0), seed=4)
    return _hair(ctx, 0.48, seed=4)


def _shag(ctx: Ctx):
    """Fellumhang: lange Zotteln von den Schultern, unten ausgefranst; dunkler als das Fell darunter."""
    if _ragged(ctx, "shag", 4):
        return None
    if _rotten(ctx) and hash01(math.floor(ctx.world[2]), math.floor(ctx.world[0]), "mange") > 0.72:
        return None                                                              # räudig
    v = 0.4 - ctx.t * 0.012
    return _hair(ctx, v, key="mane" if ctx.t < 2 else "coat", seed=6, length=5.0)


def _fringe(ctx: Ctx):
    if ctx.t < 1 or _ragged(ctx, "fringe", 3):
        return None if ctx.t >= 1 else _hair(ctx, 0.34, seed=8)
    if _rotten(ctx) and hash01(math.floor(ctx.world[2]), "fr") > 0.6:
        return None
    return _hair(ctx, 0.36 - ctx.t * 0.03, seed=8, length=4.0)


def _tail(ctx: Ctx):
    return _hair(ctx, 0.36, seed=10)


def _tuft(ctx: Ctx):
    if ctx.face != "top" and ctx.t == ctx.h - 1 and hash01(ctx.s, ctx.face, "tq") > 0.5:
        return None
    return _hair(ctx, 0.3 + rel_y(ctx) * 0.1, key="mane", seed=12)


def _crust(ctx: Ctx):
    """Magmakruste des Schlackenkeilers: dunkle Schollen, dazwischen glühende Risse."""
    if _v(ctx) != "schlacke":
        return None
    face = ctx.face
    if face in ("west", "east") and _ragged(ctx, "crust", 2):
        return None
    x, y, z = ctx.world
    cell, edge = _CRUST(x, y, z)
    if edge < 0.11:
        heat = 1.0 - edge / 0.11
        ctx.glow = mix(hexc("#000000"), ctx.pal["magma_glow"], 0.45 + heat * 0.5)
        return pick(ctx.pal["magma"], 0.3 + heat * 0.6)
    if edge < 0.22:
        return pick(ctx.pal["magma"], 0.06)                                      # verkohlter Rand am Riss
    return pick(ctx.pal["crust"], 0.3 + cell * 0.25 + _lit(ctx) + edge * 0.15
                + (hash01(math.floor(x * 1.5), math.floor(y * 1.5), math.floor(z * 1.5), "cr") - 0.5) * 0.12)


def _stem(ctx: Ctx):
    if _v(ctx) != "karmesin":
        return None
    return pick(ctx.pal["stem"], 0.55 + _lit(ctx) - rel_y(ctx) * 0.2)


def _cap(ctx: Ctx):
    if _v(ctx) != "karmesin":
        return None
    face = ctx.face
    if face == "bottom":
        return pick(ctx.pal["cap"], 0.22)
    if face == "top" and hash01(ctx.u, ctx.v, "spot") > 0.8:
        return pick(ctx.pal["stem"], 0.85)                                       # helle Flecken
    return pick(ctx.pal["cap"], 0.62 + _lit(ctx) + (hash01(ctx.u, ctx.v, "c") - 0.5) * 0.1)


def _root(ctx: Ctx):
    """Karmesinwurzeln: ein paar dünne, rote Halme."""
    if _v(ctx) != "karmesin":
        return None
    col = ctx.s
    height = 0.35 + 0.65 * hash01(col, ctx.cube.origin[0], ctx.cube.origin[2], "rt")
    if col % 2 == 1 or ctx.t < ctx.h * (1.0 - height):
        return None
    return pick(ctx.pal["root"], 0.4 + (1.0 - rel_y(ctx)) * 0.4)


# ---------------------------------------------------------------------------------- Mähne

def _crest(ctx: Ctx):
    """Fester Borstenkamm: oben ausgefranst, zu den Spitzen heller."""
    face = ctx.face
    if face == "bottom":
        return None
    if face == "top":
        if _rotten(ctx) and hash01(ctx.s, ctx.t, "cm") > 0.55:
            return None
        return pick(ctx.pal["mane"], 0.5 + (hash01(ctx.s, ctx.t, "ct") - 0.5) * 0.2)
    if _rotten(ctx) and hash01(ctx.s, ctx.t, face, "cm") > 0.75:
        return None
    return pick(ctx.pal["mane"], 0.42 + (1.0 - rel_y(ctx)) * 0.12 + _lit(ctx) + (hash01(ctx.u, "cs") - 0.5) * 0.12)


def _spines(ctx: Ctx):
    """
    Borstenfächer: einzelne, steife Borsten (jede Spalte eine), ungleich lang, vorn am Nacken am längsten; die Spitzen
    heller – beim Aschen- und Schlackenkeiler glimmen sie wie Glut. Beim Moderkeiler fehlen viele.
    """
    x, y, z = ctx.world
    side = ctx.cube.bone.name != "mane"
    col = math.floor(z)
    if side and hash01(col, ctx.cube.bone.name, "gap") > 0.75:
        return None                                                              # die Seitenfächer sind etwas lichter
    if _rotten(ctx) and hash01(col, ctx.cube.bone.name, "bald") > 0.45:
        return None
    # vorn (z klein) lang, nach hinten kürzer, jede Borste etwas anders
    front = min(1.0, max(0.0, (z + 16.0) / 18.0))
    length = (1.0 - front * 0.5) * (0.6 + 0.4 * hash01(col, ctx.cube.bone.name, "len"))
    k = ctx.t / max(ctx.h - 1, 1)                                               # 0 oben, 1 unten
    if k < 1.0 - length:
        return None
    tip = (k - (1.0 - length)) / max(length, 1e-3)                               # 0 an der Spitze
    if tip < 0.2:
        glow = ctx.pal.get("tip_glow")
        if glow is not None:
            ctx.glow = mix(hexc("#000000"), glow, 0.9 - tip * 2.0)
        return pick(ctx.pal["tip"], 0.78 - tip * 1.6)
    return pick(ctx.pal["mane"], 0.28 + (1.0 - tip) * 0.3 + (hash01(col, ctx.cube.bone.name, "mv") - 0.5) * 0.16)


def _hump(ctx: Ctx):
    """Widerrist: dichtes, dunkles Mähnenhaar."""
    if ctx.face == "bottom":
        return None
    return _hair(ctx, 0.36, key="mane", seed=7, length=4.0)


# ---------------------------------------------------------------------------------- Kopf

def _eye(ctx: Ctx, lx: float, lz: float):
    """Auge auf der Stirnseite (lokal oben): eine dunkle Höhle mit kleinem, glühendem Augenpunkt."""
    side = 1 if lx > 0 else -1
    ax = abs(lx)
    if 3.0 <= ax <= 6.0 and -9.0 <= lz <= -6.6:
        if 4.0 <= ax <= 5.6 and -8.6 <= lz <= -7.6:
            if _rotten(ctx):
                if side > 0:
                    ctx.glow = ctx.pal["glow"]
                    return ctx.pal["glow"]                                       # ein glimmendes Auge
                return ctx.pal["socket"]                                         # das andere ist leer
            ctx.glow = ctx.pal["eye"]
            return ctx.pal["eye"]
        return mix(ctx.pal["socket"], pick(ctx.pal["hide"], 0.3), 0.35)
    return None


def _skull(ctx: Ctx):
    lface = ctx.lface
    lx, ly, lz = ctx.local
    if lface == "top":
        eye = _eye(ctx, lx, lz)
        if eye is not None:
            return eye
        if _rotten(ctx) and abs(lx) < 2.6 and -5.5 < lz < -1.0:
            return pick(ctx.pal["bone"], 0.72 - abs(lx) * 0.05 + (hash01(ctx.s, ctx.t) - 0.5) * 0.1)   # Stirnknochen
        return _hair(ctx, 0.44 + min(lz + 9.0, 4.0) * -0.01, seed=14)
    if lface == "bottom":
        return _hair(ctx, 0.32, seed=15)                                         # Kehle
    if lface == "back":
        return _hair(ctx, 0.36, seed=16)
    if lface == "front":
        return _hide(ctx, 0.36)                                                  # nur seitlich der Schnauze sichtbar
    return _hair(ctx, 0.42, seed=17)                                             # Wangen


def _brow(ctx: Ctx):
    if ctx.lface == "bottom":
        return _hide(ctx, 0.25)
    return _hair(ctx, 0.36, key="mane", seed=18, length=2.0)


def _muzzle(ctx: Ctx):
    """Schnauze: oben borstig zur Stirn hin, nach vorn faltige Haut; an den Seiten die Lefzen."""
    lface = ctx.lface
    lx, ly, lz = ctx.local
    k = (lz + 17.0) / 8.0                                                        # 0 vorn an der Scheibe, 1 an der Stirn
    if lface == "top":
        if k > 0.55:
            return _hair(ctx, 0.42, seed=20)
        wrinkle = 0.05 if math.floor((lz + 17.0) * 1.0) % 2 == 0 else 0.0
        return _hide(ctx, 0.5 + wrinkle - (0.06 if abs(lx) > 3.6 else 0.0))
    if lface in ("west", "east"):
        if ly > 2.0:
            return _hide(ctx, 0.3)                                               # Lefzen
        if k > 0.6:
            return _hair(ctx, 0.38, seed=21)
        return _hide(ctx, 0.44)
    if lface == "bottom":
        return _hide(ctx, 0.3)
    return _hide(ctx, 0.4)


def _disk(ctx: Ctx):
    """Rüsselscheibe mit zwei Nasenlöchern und dunklem Rand."""
    if ctx.lface == "front":
        lx, ly, lz = ctx.local
        nx = abs(abs(lx) - 1.7)
        if nx < 0.7 and -0.9 < ly < 0.9:
            return pick(ctx.pal["snout"], 0.06)                                  # Nasenlöcher
        if ctx.s in (0, ctx.w - 1) or ctx.t in (0, ctx.h - 1):
            return pick(ctx.pal["snout"], 0.38)
        return pick(ctx.pal["snout"], 0.62 + (hash01(ctx.u, ctx.v, "sn") - 0.5) * 0.08)
    return pick(ctx.pal["snout"], 0.45 + _lit(ctx) * 0.5 - (0.08 if ctx.lface == "bottom" else 0.0))


def _jaw(ctx: Ctx):
    lface = ctx.lface
    if lface == "top":
        return pick(ctx.pal["snout"], 0.2)
    if lface in ("west", "east", "front"):
        if ctx.t == 0:
            return pick(ctx.pal["socket"], 1.0) if lface != "front" else pick(ctx.pal["snout"], 0.18)   # Maulspalt
        if lface != "front" and ctx.s == (ctx.w - 3 if lface == "west" else 2) and not _rotten(ctx):
            return pick(ctx.pal["tusk"], 0.7)                                    # ein Zahn blitzt hervor
        return _hide(ctx, 0.34)
    return _hair(ctx, 0.3, seed=24)                                              # Kinnbart


def _cheek(ctx: Ctx):
    if ctx.lface in ("front", "back") and hash01(ctx.s, ctx.t, ctx.lface, "ck") > 0.6:
        return None
    if ctx.lface == "bottom" and hash01(ctx.s, ctx.t, "ckb") > 0.4:
        return None
    if _rotten(ctx) and hash01(ctx.s, ctx.t, ctx.lface, "ckr") > 0.6:
        return None
    return _hair(ctx, 0.34 + rel_y(ctx) * 0.06, key="mane", seed=26, length=4.0)


def _ear(ctx: Ctx):
    lface = ctx.lface
    if lface == "bottom":
        return pick(ctx.pal["snout"], 0.34 + (hash01(ctx.u, ctx.v) - 0.5) * 0.06)   # Innenseite
    if _rotten(ctx) and lface == "top" and hash01(ctx.s, ctx.t, "ear") > 0.82:
        return pick(ctx.pal["flesh"], 0.3)
    return _hair(ctx, 0.4, seed=28, length=2.0)


def _tusk_color(ctx: Ctx, k: float):
    key = "bone" if _rotten(ctx) else "tusk"
    ring = 0.05 if math.floor(k * 9.0) % 2 == 0 else 0.0
    return pick(ctx.pal[key], 0.42 + k * 0.42 + ring + _lit(ctx) * 0.6)


def _tusk(ctx: Ctx):
    k = 1.0 - rel_y(ctx) if ctx.wrel is None else 1.0 - ctx.wrel
    if ctx.lface == "bottom":
        return pick(ctx.pal["snout"], 0.2)                                       # im Kiefer
    if k < 0.18:
        return pick(ctx.pal["snout"], 0.32)                                      # Zahnfleisch
    return _tusk_color(ctx, k * 0.6)


def _tusk_tip(ctx: Ctx):
    if _rotten(ctx) and ctx.cube.bone.name.startswith("right"):
        return None                                                              # abgebrochen
    k = 1.0 - ctx.t / max(ctx.h - 1, 1)
    if ctx.lface == "top":
        k = 1.0
    return _tusk_color(ctx, 0.6 + k * 0.4)


# ---------------------------------------------------------------------------------- Läufe

def _leg_fur(ctx: Ctx):
    if ctx.face == "bottom":
        return _hide(ctx, 0.3)
    return _hair(ctx, 0.4 - ctx.t * 0.012, seed=30, length=4.0)


def _leg(ctx: Ctx):
    """Unterlauf mit Fesselhaar und gespaltenem Huf."""
    face, s, t = ctx.face, ctx.s, ctx.t
    if face == "bottom" or (face != "top" and t >= ctx.h - 2):
        split = face in ("front", "back") and s == ctx.w // 2
        return pick(ctx.pal["hoof"], 0.45 + _lit(ctx) - (0.25 if split else 0.0) - (0.1 if t == ctx.h - 1 else 0.0))
    if face == "top":
        return _hide(ctx, 0.3)
    if t <= 1 and hash01(s, face, "fet") > 0.4:
        return _hair(ctx, 0.3, key="mane", seed=32)                              # Zotteln über dem Huf
    return _hair(ctx, 0.34 - t * 0.02, seed=33)


def materials(variant: str) -> dict:
    return {
        "chest": _chest, "hump": _hump, "rump": _rump, "shag": _shag, "fringe": _fringe, "tail": _tail, "tuft": _tuft,
        "crust": _crust, "stem": _stem, "cap": _cap, "root": _root, "crest": _crest, "spines": _spines,
        "skull": _skull, "brow": _brow, "muzzle": _muzzle, "disk": _disk, "jaw": _jaw, "cheek": _cheek, "ear": _ear,
        "tusk": _tusk, "tusk_tip": _tusk_tip, "leg_fur": _leg_fur, "leg": _leg,
    }


def hidden(variant: str) -> list[str]:
    return []
