"""
Borstlinge (statt Piglins) auf dem Skelett des Vanilla-Piglins: dieselben Knochen und Drehpunkte wie AdultPiglinModel
(head mit hat und den beiden Ohren, body, Arme, Beine), damit Ohrenwackeln, Gold-Bewundern, Tanzen, Armbrust-Haltung,
Schlag und Rüstung genauso laufen. Eigene Quader und Bilder – ein stolzes Keilervolk des Nethers:

  * ein Keilerkopf mit schwerer Stirn, kleinen Knopfaugen, breitem Rüssel mit Nasenring, zwei Hauern, einem
    Borstenkamm über dem Schädel und einem geflochtenen Zopf im Nacken; Schlappohren mit Goldringen
  * ärmelloser Waffenrock mit Goldborte, ein Fellumhang über den Schultern, ein breiter Gürtel mit Goldschnalle und
    ein Lendenschurz; bloße, bemalte Arme mit Armreifen, Hufhände; Hosen mit Wickeln und Hufe

Dieselbe Geometrie tragen auch der Borstenhüne (statt Piglin-Barbar: gehörnter Helm und Brustpanzer, die nur bei ihm
bemalt sind) und der Moderborstling (statt Zombie-Piglin: fauliges Fleisch, offener Schädel, Rippen, zerrissenes
Tuch, ein glimmendes Auge).

Gestalten: Karmesinborstling (Karmesinwald), Aschenborstling (Netherödland und Seelensandtal), Goldborstling (selten,
reich geschmückt); Borstenhüne; Moderborstling (karmesin oder aschen).
"""
from __future__ import annotations

import math

from ..geometry import Model
from ..paint import Ctx, hash01, hexc, mix, noise3, pick, ramp, rel_y

_COMMON = {
    "gold": ramp("#5a3a08", "#fff2a0", steps=8, hue_shift=-0.03),
    "leather": ramp("#1e1008", "#9a6a3c", steps=7),
    "hoof": ramp("#120c0a", "#5a4a40", steps=5),
    "tusk": ramp("#7a6448", "#fff6dc", steps=6),
    "bone": ramp("#5a5040", "#f0e8d0", steps=7),
    "metal": ramp("#16181c", "#c8ccd2", steps=8, hue_shift=0.02),
    "eye_white": hexc("#f0e4d8"), "eye_dark": hexc("#1a0c08"),
}

PALETTES = {
    "karmesin": {**_COMMON, "skin": ramp("#5a2420", "#f0a088", steps=10, hue_shift=-0.03),
                 "bristle": ramp("#1a0806", "#8a3a24", steps=7), "cloth": ramp("#2a0606", "#d8402c", steps=8),
                 "fur": ramp("#1c100a", "#9a7050", steps=7), "paint": hexc("#1a0a0a")},
    "aschen": {**_COMMON, "skin": ramp("#5a3a36", "#e8c0ac", steps=10, hue_shift=-0.02),
               "bristle": ramp("#18141a", "#7a6e70", steps=7), "cloth": ramp("#1e1a16", "#8e7c62", steps=8),
               "fur": ramp("#16120e", "#8a8070", steps=7), "paint": hexc("#e8e0d0")},
    "gold": {**_COMMON, "skin": ramp("#5a2a26", "#f4b098", steps=10, hue_shift=-0.03),
             "bristle": ramp("#1e0c06", "#a0582a", steps=7), "cloth": ramp("#1a0a24", "#8a4ab0", steps=8),
             "fur": ramp("#20140c", "#c8a070", steps=7), "paint": hexc("#d8a020")},
    "borstenhuene": {**_COMMON, "skin": ramp("#3e1a18", "#d08470", steps=10, hue_shift=-0.03),
                     "bristle": ramp("#100606", "#5a2a1c", steps=7), "cloth": ramp("#141010", "#5a4a3a", steps=8),
                     "fur": ramp("#100a08", "#6a5040", steps=7), "paint": hexc("#7a1010")},
    "moder_karmesin": {**_COMMON, "skin": ramp("#26301e", "#a8b88a", steps=10, hue_shift=0.02),
                       "flesh": ramp("#3a1010", "#b0504a", steps=7), "bristle": ramp("#100c08", "#5a4a30", steps=7),
                       "cloth": ramp("#1a0a08", "#7a2a20", steps=8), "fur": ramp("#14100c", "#5a5040", steps=7),
                       "paint": None, "glow": hexc("#ffe060")},
    "moder_aschen": {**_COMMON, "skin": ramp("#2a2c26", "#b0b4a0", steps=10, hue_shift=0.02),
                     "flesh": ramp("#301818", "#9a5450", steps=7), "bristle": ramp("#0e0c0c", "#4a4440", steps=7),
                     "cloth": ramp("#141210", "#5e5446", steps=8), "fur": ramp("#12100e", "#4e4a42", steps=7),
                     "paint": None, "glow": hexc("#a0ff80")},
}

LOOKS = ("karmesin", "aschen", "gold")


def build() -> Model:
    m = Model("piglin", 128, 128)

    # ---------------------------------------------------------------- Kopf (Drehpunkt wie Vanilla)
    m.bone("head", pivot=(0, 0, 0))
    m.cube("head", (-5, -8, -4), (10, 8, 8), "head")
    m.cube("head", (-2.5, -3.9, -6), (5, 3.7, 2), "snout")                    # Rüssel
    m.cube("head", (-5.3, -7.3, -4.6), (10.6, 1.5, 1), "brow")                # schwere Stirn
    m.cube("head", (-3.6, -2.6, -5.6), (1, 2.2, 1), "tusk")                   # Hauer
    m.cube("head", (2.6, -2.6, -5.6), (1, 2.2, 1), "tusk")
    m.cube("head", (-1.0, -1.2, -6.3), (2, 1, 0.6), "ring")                   # Nasenring
    m.cube("head", (-1.5, -9.6, -3.4), (3, 2, 8), "crest")                    # Borstenkamm
    m.cube("head", (-1.0, -5.5, 3.6), (2, 7, 1.2), "braid")                   # Zopf im Nacken
    m.cube("head", (-5.6, -8.7, -4.6), (11.2, 4.4, 9.2), "helm")              # Helm (nur der Hüne)
    m.cube("head", (-7.4, -11.5, -1.0), (2, 5, 2), "horn")                    # Hörner (nur der Hüne)
    m.cube("head", (5.4, -11.5, -1.0), (2, 5, 2), "horn")
    m.bone("hat", "head", pivot=(0, 0, 0))
    m.bone("left_ear", "head", pivot=(4.5, -6, 0), rotation=(0, 0, -30))
    m.cube("left_ear", (0, 0, -2), (1, 5, 4), "ear")
    m.cube("left_ear", (0.2, 4.4, -0.6), (1.2, 1.2, 1.2), "earring")
    m.bone("right_ear", "head", pivot=(-4.5, -6, 0), rotation=(0, 0, 30))
    m.cube("right_ear", (-1, 0, -2), (1, 5, 4), "ear")
    m.cube("right_ear", (-1.4, 4.4, -0.6), (1.2, 1.2, 1.2), "earring")

    # ---------------------------------------------------------------- Rumpf
    m.bone("body", pivot=(0, 0, 0))
    m.cube("body", (-4, 0, -2), (8, 12, 4), "torso")
    m.cube("body", (-4.6, -0.4, -2.6), (9.2, 3.2, 5.2), "mantle")             # Fellumhang über den Schultern
    m.cube("body", (-4.4, 0.6, -2.5), (8.8, 7, 5), "plate")                   # Brustpanzer (nur der Hüne)
    m.cube("body", (-4.3, 7.4, -2.3), (8.6, 1.8, 4.6), "belt")
    m.cube("body", (-2.6, 9.2, -2.45), (5.2, 4.6, 0.4), "loin")               # Lendenschurz vorn …
    m.cube("body", (-2.6, 9.2, 2.05), (5.2, 4.6, 0.4), "loin")                # … und hinten

    # ---------------------------------------------------------------- Arme und Beine (Drehpunkte wie Vanilla)
    for side, x in (("right", -5.0), ("left", 5.0)):
        arm = f"{side}_arm"
        m.bone(arm, pivot=(x, 2, 0))
        ox = -3.0 if side == "right" else -1.0
        m.cube(arm, (ox, -2, -2), (4, 12, 4), "arm")
        m.cube(arm, (ox - 0.3, 3.4, -2.3), (4.6, 1.8, 4.6), "bracer")
        m.cube(arm, (ox - 0.5, -2.5, -2.5), (5, 3.6, 5), "pauldron")          # Schulterstück (nur der Hüne)
    for side, x in (("right", -1.9), ("left", 1.9)):
        leg = f"{side}_leg"
        m.bone(leg, pivot=(x, 12, 0))
        m.cube(leg, (-2, 0, -2), (4, 12, 4), "leg")
        m.cube(leg, (-2.25, 6.6, -2.25), (4.5, 3.6, 4.5), "wrap")
    return m


# =============================================================================================
# Bemalen
# =============================================================================================

_LIT = {"top": 0.07, "front": 0.03, "west": 0.0, "east": -0.03, "back": -0.05, "bottom": -0.16}


def _v(ctx: Ctx) -> str:
    return ctx.params.get("variant", "karmesin")


def _rotten(ctx: Ctx) -> bool:
    return _v(ctx).startswith("moder")


def _brute(ctx: Ctx) -> bool:
    return _v(ctx) == "borstenhuene"


def _skin(ctx: Ctx, base: float = 0.58, seed: int = 1):
    """Haut: ruhige Fläche mit feinen Borsten-Pünktchen; beim Moderborstling faulig, fleckig, stellenweise rohes Fleisch."""
    x, y, z = ctx.world
    v = base + _LIT[ctx.face] + (noise3(x * 0.25, y * 0.25, z * 0.25, seed) - 0.5) * 0.12
    if hash01(ctx.u, ctx.v, "stubble") > 0.88:
        v -= 0.07                                                         # Borstenstoppeln
    if _rotten(ctx):
        n = noise3(x * 0.3, y * 0.3, z * 0.3, seed + 7)
        if n > 0.68:
            return pick(ctx.pal["flesh"], 0.35 + (n - 0.68) * 1.2)         # offene Stelle
        v -= 0.05 if n > 0.6 else 0.0
    return pick(ctx.pal["skin"], v)


def _bristle(ctx: Ctx, v: float = 0.45):
    """Borsten: kurze, steife Strähnen in Wuchsrichtung, jede etwas anders getönt."""
    x, y, z = ctx.world
    col = math.floor(x + z * 0.7)
    seg = math.floor((y + hash01(col, "o") * 3.0) / 2.0)
    return pick(ctx.pal["bristle"], v + _LIT[ctx.face] + (hash01(col, seg, "br") - 0.5) * 0.14)


def _head(ctx: Ctx):
    face, s, t = ctx.face, ctx.s, ctx.t
    rotten = _rotten(ctx)
    if face == "top":
        return _bristle(ctx, 0.4)
    if face == "back":
        return _bristle(ctx, 0.42 - t * 0.02)
    if face in ("west", "east"):
        back = s if face == "east" else ctx.w - 1 - s                      # 0 = hinten
        if t <= 1 or back <= 1 or (t <= 2 and back <= 3):
            return _bristle(ctx, 0.44)                                    # Borsten oben und am Hinterkopf
        if rotten and face == "east" and 2 <= t <= 6 and 3 <= back <= 6:
            return pick(ctx.pal["bone"], 0.75 - t * 0.04 + (hash01(s, t) - 0.5) * 0.1)   # offener Schädel
        return _skin(ctx, 0.55)
    if face == "bottom":
        return _skin(ctx, 0.42)
    # Vorderseite (10 x 8): Haaransatz, Kriegsbemalung, Knopfaugen, Wangen, Maul
    if t == 0:
        return _bristle(ctx, 0.46)
    if t == 3 and s in (2, 3, 6, 7):
        pupil = s in (3, 6)
        if rotten and s == 6:
            ctx.glow = ctx.pal["glow"]
            return ctx.pal["glow"]                                        # ein glimmendes Auge
        if rotten and s == 3:
            return pick(ctx.pal["skin"], 0.18)                            # das andere ist leer
        return ctx.pal["eye_dark"] if pupil else ctx.pal["eye_white"]
    if t == 7 and 1 <= s <= 8:
        return pick(ctx.pal["skin"], 0.22)                                # Maulspalt
    paint = ctx.pal.get("paint")
    if paint is not None and t in (1, 2, 4) and s in (1, 8):
        return mix(_skin(ctx, 0.58), paint, 0.75)                         # Kriegsbemalung
    if t in (5, 6) and s in (1, 8):
        return _skin(ctx, 0.66)                                           # Wangen
    return _skin(ctx, 0.6)


def _snout(ctx: Ctx):
    face, s, t = ctx.face, ctx.s, ctx.t
    if face == "front":
        if t in (1, 2) and s in (1, 3):
            return pick(ctx.pal["skin"], 0.12)                            # Nüstern
        if t == 0:
            return _skin(ctx, 0.72)
        if t == ctx.h - 1:
            return _skin(ctx, 0.4)
        return _skin(ctx, 0.66)
    if face == "top":
        return _skin(ctx, 0.7 - (ctx.t % 2) * 0.04)                       # Falten auf dem Rüssel
    return _skin(ctx, 0.52)


def _brow(ctx: Ctx):
    if ctx.face == "top" or (ctx.face == "front" and ctx.t == 0):
        return _bristle(ctx, 0.5)                                         # buschige Brauen
    return _skin(ctx, 0.46)


def _tusk(ctx: Ctx):
    if _rotten(ctx):
        return pick(ctx.pal["bone"], 0.5 - rel_y(ctx) * 0.2)
    return pick(ctx.pal["tusk"], 0.85 - rel_y(ctx) * 0.4 + _LIT[ctx.face])


def _gold(ctx: Ctx, v: float = 0.6):
    tarnish = 0.25 if _rotten(ctx) else 0.0
    return pick(ctx.pal["gold"], v - tarnish + _LIT[ctx.face] + (hash01(ctx.u, ctx.v, "g") - 0.5) * 0.1)


def _ring(ctx: Ctx):
    if _rotten(ctx) or _brute(ctx):
        return None
    return _gold(ctx, 0.7)


def _crest(ctx: Ctx):
    """Borstenkamm: steife Borsten, oben ausgefranst und an den Spitzen heller."""
    face, s, t = ctx.face, ctx.s, ctx.t
    if face == "bottom":
        return None
    if face == "top":
        return _bristle(ctx, 0.55) if hash01(s, t, "ct") > 0.25 else None
    if t == 0 and hash01(s, face, "cr") > 0.5:
        return None
    return _bristle(ctx, 0.5 - t * 0.1)


def _braid(ctx: Ctx):
    face, t = ctx.face, ctx.t
    if face in ("top", "bottom"):
        return _bristle(ctx, 0.4)
    if t == ctx.h - 2 and not _rotten(ctx):
        return _gold(ctx, 0.75)                                           # Goldperle am Zopf
    if t == ctx.h - 1:
        return _bristle(ctx, 0.55) if ctx.s % 2 == 0 else None            # Quaste
    return _bristle(ctx, 0.36 + (0.1 if (t + ctx.s) % 2 else 0.0))        # Flechtmuster


def _helm(ctx: Ctx):
    """Helm des Borstenhünen: Eisen mit Goldnieten, vorn ein Nasenschutz; die Augenpartie bleibt frei."""
    if not _brute(ctx):
        return None
    face, s, t = ctx.face, ctx.s, ctx.t
    if face == "bottom":
        return None
    if face == "front":
        if t >= 3 and not (4 <= s <= 6):
            return None                                                   # Gesicht frei, nur der Nasenschutz
        if t >= 3:
            return pick(ctx.pal["metal"], 0.55 - (t - 3) * 0.05)
    if face in ("west", "east") and t >= 3 and (s <= 2 if face == "west" else s >= ctx.w - 3):
        return None                                                       # Wangen frei
    if t == ctx.h - 1 or (face != "top" and t == 0):
        return _gold(ctx, 0.62) if (ctx.s % 3 == 1) else pick(ctx.pal["metal"], 0.35)   # Rand mit Nieten
    return pick(ctx.pal["metal"], 0.58 + _LIT[face] + (hash01(ctx.u, ctx.v, "m") - 0.5) * 0.08)


def _horn(ctx: Ctx):
    if not _brute(ctx):
        return None
    k = rel_y(ctx)
    return pick(ctx.pal["tusk"], 0.35 + (1.0 - k) * 0.5 + _LIT[ctx.face])


def _ear(ctx: Ctx):
    face = ctx.face
    inner = face == "west" if ctx.cube.bone.name.startswith("left_ear") else face == "east"
    if inner:
        return pick(ctx.pal["skin"], 0.72 - rel_y(ctx) * 0.1)                # rosige Innenseite
    if rel_y(ctx) < 0.25:
        return _bristle(ctx, 0.42)
    return _skin(ctx, 0.56)


def _earring(ctx: Ctx):
    if _brute(ctx):
        return pick(ctx.pal["metal"], 0.6)
    return _gold(ctx, 0.66)


def _torso(ctx: Ctx):
    """Ärmelloser Waffenrock mit Goldborte; beim Moderborstling zerrissen, die Rippen schauen hervor."""
    face, s, t = ctx.face, ctx.s, ctx.t
    cloth = ctx.pal["cloth"]
    if face == "top":
        return _skin(ctx, 0.5)
    if face == "bottom":
        return pick(cloth, 0.3)
    v = 0.55 + _LIT[face] + (hash01(math.floor(ctx.world[0] / 2.0), "fold") - 0.5) * 0.08 - t * 0.012
    if face == "front":
        if t <= 2 and 3 <= s <= 4:
            return _skin(ctx, 0.56)                                       # Halsausschnitt
        if t <= 3 and s in (2, 5):
            return _gold(ctx, 0.62) if not _rotten(ctx) else pick(cloth, 0.3)
        if _rotten(ctx) and 3 <= t <= 6 and 1 <= s <= 6:
            if t % 2 == 1:
                return pick(ctx.pal["bone"], 0.7 - abs(s - 3.5) * 0.06)    # Rippen
            return pick(ctx.pal["flesh"], 0.25)
    if t == ctx.h - 1:
        return _gold(ctx, 0.55) if not _rotten(ctx) else None             # Saum
    if _rotten(ctx) and hash01(s, t, face, "torn") > 0.86:
        return pick(ctx.pal["flesh"], 0.3)                                # Riss im Tuch
    return pick(cloth, v)


def _mantle(ctx: Ctx):
    """Fellumhang: zottig, unten in Strähnen ausgefranst."""
    face, s, t = ctx.face, ctx.s, ctx.t
    if face == "bottom":
        return None
    if _brute(ctx):
        return None
    if face != "top" and t == ctx.h - 1 and hash01(s, face, "fr") > 0.55:
        return None
    x, y, z = ctx.world
    v = 0.5 + _LIT[face] + (hash01(math.floor(x * 0.8 + z), t, "fur") - 0.5) * 0.22
    if _rotten(ctx) and hash01(s, t, face, "mange") > 0.7:
        return None                                                       # räudig
    return pick(ctx.pal["fur"], v)


def _plate(ctx: Ctx):
    """Brustpanzer des Borstenhünen: zwei Platten mit Goldnieten und einem Keilerkopf-Zeichen."""
    if not _brute(ctx):
        return None
    face, s, t = ctx.face, ctx.s, ctx.t
    if face in ("top", "bottom"):
        return pick(ctx.pal["metal"], 0.4)
    if face == "front" and (s, t) in ((3, 2), (4, 2), (2, 3), (5, 3), (3, 4), (4, 4)):
        return _gold(ctx, 0.66)                                           # Wappenzeichen
    if t in (0, ctx.h - 1) or s in (0, ctx.w - 1):
        return _gold(ctx, 0.52) if (s + t) % 3 == 0 else pick(ctx.pal["metal"], 0.38)
    return pick(ctx.pal["metal"], 0.6 + _LIT[face] - t * 0.02 + (hash01(ctx.u, ctx.v) - 0.5) * 0.06)


def _belt(ctx: Ctx):
    face, s, t = ctx.face, ctx.s, ctx.t
    if face == "front" and 3 <= s <= 5:
        return _gold(ctx, 0.72 if t == 0 else 0.55)                       # Schnalle
    return pick(ctx.pal["leather"], 0.45 + _LIT[face] - t * 0.08)


def _loin(ctx: Ctx):
    face, s, t = ctx.face, ctx.s, ctx.t
    if t == ctx.h - 1 and s % 2 == 1:
        return None                                                       # Fransen
    if _rotten(ctx) and hash01(s, t, "lt") > 0.75:
        return None
    if t == ctx.h - 2 and not _rotten(ctx):
        return _gold(ctx, 0.5)
    return pick(ctx.pal["cloth"], 0.48 + _LIT[face] - t * 0.03)


def _arm(ctx: Ctx):
    """Bloße Arme mit Bemalung, unten die Hufhand; beim Hünen Kettenärmel."""
    face, s, t = ctx.face, ctx.s, ctx.t
    if face == "bottom" or (face != "top" and t >= ctx.h - 2):
        return pick(ctx.pal["hoof"], 0.5 + _LIT[face] - (0.1 if s == 2 else 0.0))   # Hufhand mit Spalt
    if _brute(ctx) and t <= 6:
        return pick(ctx.pal["metal"], 0.45 + (0.12 if (s + t) % 2 else 0.0))   # Kettenärmel
    paint = ctx.pal.get("paint")
    if paint is not None and face in ("front", "west", "east") and t in (6, 7) and s % 2 == 0:
        return mix(_skin(ctx, 0.56), paint, 0.7)
    if face == "top":
        return _skin(ctx, 0.6)
    return _skin(ctx, 0.56 - t * 0.008)


def _bracer(ctx: Ctx):
    if _brute(ctx):
        return pick(ctx.pal["metal"], 0.55 + _LIT[ctx.face])
    if _rotten(ctx):
        return pick(ctx.pal["leather"], 0.3) if hash01(ctx.u, ctx.v) > 0.3 else None
    return _gold(ctx, 0.62 - ctx.t * 0.12)


def _pauldron(ctx: Ctx):
    if not _brute(ctx):
        return None
    face, t = ctx.face, ctx.t
    if face == "bottom":
        return None
    if t == ctx.h - 1:
        return _gold(ctx, 0.55)
    return pick(ctx.pal["metal"], 0.62 + _LIT[face] - t * 0.05)


def _leg(ctx: Ctx):
    """Hosen aus Leder, unten die Hufe."""
    face, s, t = ctx.face, ctx.s, ctx.t
    if face == "bottom" or (face != "top" and t >= ctx.h - 2):
        return pick(ctx.pal["hoof"], 0.45 + _LIT[face])
    if face == "top":
        return pick(ctx.pal["leather"], 0.4)
    leather = ctx.pal["leather"] if not _brute(ctx) else ctx.pal["cloth"]
    if _rotten(ctx) and hash01(s, t, face, "lg") > 0.8:
        return _skin(ctx, 0.45)
    return pick(leather, 0.48 + _LIT[face] - t * 0.015 + (hash01(math.floor(ctx.world[0]), "seam") - 0.5) * 0.06)


def _wrap(ctx: Ctx):
    face, t = ctx.face, ctx.t
    if face in ("top", "bottom"):
        return None
    if _rotten(ctx) and hash01(ctx.s, t, face, "wr") > 0.6:
        return None
    return pick(ctx.pal["leather"], 0.6 - (t % 2) * 0.14 + _LIT[face])     # gewickelte Lederstreifen


def materials(variant: str) -> dict:
    return {
        "head": _head, "snout": _snout, "brow": _brow, "tusk": _tusk, "ring": _ring, "crest": _crest, "braid": _braid,
        "helm": _helm, "horn": _horn, "ear": _ear, "earring": _earring, "torso": _torso, "mantle": _mantle,
        "plate": _plate, "belt": _belt, "loin": _loin, "arm": _arm, "bracer": _bracer, "pauldron": _pauldron,
        "leg": _leg, "wrap": _wrap,
    }


def hidden(variant: str) -> list[str]:
    return []
