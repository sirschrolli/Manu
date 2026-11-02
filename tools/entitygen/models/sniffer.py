"""
Urwühler (statt Schnüffler) auf dem Skelett des Vanilla-Schnüfflers: gleiche Knochen und Drehpunkte wie SnifferModel
(bone -> body -> head -> left/right_ear, nose, lower_beak; bone -> sechs Beine), damit Gehen, Schnuppern, Graben,
Aufstehen und Freudentanz genauso laufen. Eigene Quader:

  * ein uraltes, zotteliges Grabtier wie ein Moschusochse: das lange Fell fällt in breiten Strähnen über die Flanken,
    darunter hängt ein langer Fellbehang bis über die Knie (er wird von der äußeren Fellschicht verdeckt und schaut
    unter ihr hervor), auf dem Schulterbuckel steht ein Kamm aus dunklerem Haar
  * auf dem hinteren Rücken eine Moosmatte mit Farnen und Blüten – Samen aus alter Zeit wachsen auf ihm
  * ein Stirnschopf über den kleinen, glänzenden Augen, lange Schlappohren, eine breite, ledrige Rüsselplatte mit
    Nüstern (sie bläht sich beim Schnuppern) und eine Schaufelschnauze mit zwei Hauern
  * kurze, kräftige Grabbeine mit Krallen

Drei Gestalten nach dem Land, in dem er schlüpft: Zottelwühler (dunkles Kastanienfell), Steppenwühler (ockerfarben
mit dunklen Rückenstreifen) und Frostwühler (langes, graues Winterfell).
"""
from __future__ import annotations

import math

from ..geometry import Model
from ..paint import Ctx, hash01, noise3, pick, ramp, rel_y

_BASE = {
    "leather": ramp("#3a2420", "#d8a090", steps=7, hue_shift=-0.03),
    "tusk": ramp("#6a5a40", "#fff4dc", steps=6),
    "claw": ramp("#1a1410", "#8a7a68", steps=5),
    "eye": ramp("#020202", "#3a3028", steps=4),
    "moss": ramp("#1a3010", "#8ac04a", steps=7),
    "fern": ramp("#173a12", "#5aa83a", steps=6),
    "bloom": ramp("#b06ab0", "#ffd8f0", steps=5),
    "bloom2": ramp("#c89a20", "#fff2a0", steps=5),
}

PALETTES = {
    "zottelwuehler": {**_BASE, "fur": ramp("#2c1006", "#ee9c5a", steps=10, hue_shift=-0.03),
                      "mane": ramp("#1e0c05", "#9a5630", steps=7), "tip": ramp("#b06c36", "#ffdcaa", steps=6)},
    "steppenwuehler": {**_BASE, "fur": ramp("#2e2010", "#ecd096", steps=10, hue_shift=-0.02),
                       "mane": ramp("#1a1008", "#6e4c26", steps=7), "tip": ramp("#a8884e", "#fff4d4", steps=6)},
    "frostwuehler": {**_BASE, "fur": ramp("#23272e", "#e8edf2", steps=10, hue_shift=0.02),
                     "mane": ramp("#3a4048", "#a4aeb8", steps=7), "tip": ramp("#a6b0ba", "#ffffff", steps=6),
                     "leather": ramp("#2a2a34", "#a8aabe", steps=7)},
}

LOOKS = tuple(PALETTES)


def build() -> Model:
    m = Model("sniffer", 272, 256)
    m.bone("bone", pivot=(0, 5, 0))

    # ---------------------------------------------------------------- Rumpf
    m.bone("body", "bone", pivot=(0, 0, 0))
    m.cube("body", (-12.5, -14, -20), (25, 29, 40), "body")
    # langer Fellbehang bis über die Knie: zwischen Körper und äußerer Fellschicht, schaut unten hervor
    m.cube("body", (-12.5, 5, -20), (25, 12.5, 40), "skirt", inflate=0.3, faces=("front", "back", "west", "east"))
    m.cube("body", (-12.5, -14, -20), (25, 26, 40), "shag", inflate=0.65)         # äußere Fellschicht, unten ausgefranst
    m.cube("body", (-12.5, -14, -20), (25, 13, 40), "mantle", inflate=1.1, faces=("front", "back", "west", "east"))  # Mantel
    m.cube("body", (-11.5, -18.0, -20.4), (23, 4.2, 15), "hump")                   # Schulterbuckel mit langem Fell
    m.cube("body", (-10.5, -15.9, -4), (21, 1.4, 22), "moss")                      # Moosmatte auf dem hinteren Rücken
    for i, (x, z, h) in enumerate(((-6.0, 0.0, 4.0), (5.0, 4.0, 3.5), (-3.5, 10.0, 3.0), (6.5, 13.5, 4.5))):
        m.cube("body", (x - 1.5, -15.9 - h, z), (3, h, 0), "plant")             # Farne und Blüten (Kreuz aus zwei Ebenen)
        m.cube("body", (x, -15.9 - h, z - 1.5), (0, h, 3), "plant")

    # ---------------------------------------------------------------- Kopf (Drehpunkte wie Vanilla)
    m.bone("head", "body", pivot=(0, 6.5, -19.48))
    m.cube("head", (-6.5, -7.5, -11.5), (13, 18, 11), "head")
    m.cube("head", (-7.0, -8.4, -12.3), (14, 5.2, 3.2), "forelock")                # Stirnschopf
    m.cube("head", (-7.1, 3.0, -9.0), (14.2, 7.5, 8.0), "beard", faces=("west", "east"))           # Backenbart
    m.bone("left_ear", "head", pivot=(6.51, -7.5, -4.51))
    m.cube("left_ear", (0, 0, -3), (1, 19, 7), "ear")
    m.bone("right_ear", "head", pivot=(-6.51, -7.5, -4.51))
    m.cube("right_ear", (-1, 0, -3), (1, 19, 7), "ear")
    m.bone("nose", "head", pivot=(0, -4.5, -11.5))
    m.cube("nose", (-6.5, -2, -9), (13, 2, 9), "nose")
    m.bone("lower_beak", "head", pivot=(0, 2.5, -12.5))
    m.cube("lower_beak", (-6.5, -7, -8), (13, 12, 9), "snout")
    m.cube("lower_beak", (-6.2, 3.0, -8.6), (1.6, 4.2, 1.6), "tusk")
    m.cube("lower_beak", (4.6, 3.0, -8.6), (1.6, 4.2, 1.6), "tusk")

    # ---------------------------------------------------------------- Beine (Drehpunkte wie Vanilla)
    for side, x in (("right", -7.5), ("left", 7.5)):
        for name, z in (("front", -15.0), ("mid", 0.0), ("hind", 15.0)):
            leg = f"{side}_{name}_leg"
            m.bone(leg, "bone", pivot=(x, 10, z))
            m.cube(leg, (-3.5, -1, -4), (7, 10, 8), "leg")
            m.cube(leg, (-3.7, 7.4, -5.3), (7.4, 1.6, 1.6), "claw")
    return m


# =============================================================================================
# Bemalen
# =============================================================================================

_LIT = {"top": 0.08, "front": 0.03, "west": 0.0, "east": -0.03, "back": -0.05, "bottom": -0.18}


def _look(ctx: Ctx) -> str:
    return ctx.params.get("variant", "zottelwuehler")


def _across(ctx: Ctx) -> float:
    """Koordinate quer über die Seite (für die Strähnen)."""
    x, y, z = ctx.world
    return x if ctx.face in ("front", "back", "top", "bottom") else z


def _lock(ctx: Ctx, width: float) -> tuple[int, float]:
    """(Nummer der Strähne, Lage in der Strähne 0..1)."""
    a = _across(ctx) / width
    i = math.floor(a)
    return i, a - i


def _streak(column: float, y: float, seed: int, length: float) -> float:
    """
    Kurze Haarsträhnen: jede Pixelspalte ist in unterschiedlich lange Abschnitte geteilt (2 bis 2+length Pixel), jeder
    etwas heller oder dunkler – wie einzelne Haare in einem Fell. Gibt den Helligkeitsversatz zurück.
    """
    c = math.floor(column)
    seg_len = 2.0 + hash01(c, seed, "len") * length
    k = math.floor((y + hash01(c, seed, "off") * 9.0) / seg_len)
    return (hash01(c, k, seed) - 0.5) * 0.13


def _fur(ctx: Ctx, base: float = 0.5, seed: int = 1, width: float = 4.0, layer: float = 5.0, ramp_key: str = "fur"):
    """
    Langes, zotteliges Fell wie bei Vanilla-Tieren: ruhige Grundfarbe mit großen, weichen Flecken, darin kurze helle und
    dunkle Strähnen (in Fallrichtung des Haars), oben etwas heller. Beim Steppenwühler dunkle Querstreifen über dem
    Rücken. ({@code width}/{@code layer}: wie lang die Strähnen sind.)
    """
    x, y, z = ctx.world
    face = ctx.face
    v = base + _LIT[face]
    v += (noise3(x * 0.09, y * 0.09, z * 0.09, seed) - 0.5) * 0.16
    if face in ("top", "bottom"):
        # oben fällt das Haar vom Rückgrat nach beiden Seiten: Strähnen quer zum Körper
        v += _streak(z, abs(x), seed + 11, layer * 0.8)
    else:
        v += _streak(_across(ctx), y, seed, layer * 0.8)
    if _look(ctx) == "steppenwuehler" and ramp_key == "fur" and face in ("top", "west", "east") and y < 6:
        if (z * 0.3 + noise3(x * 0.12, 0, z * 0.12, 7) * 1.6) % 2.4 < 0.5:
            v -= 0.16                                                     # Rückenstreifen
    return pick(ctx.pal[ramp_key], v)


def _ragged(ctx: Ctx, width: float, lo: float, hi: float, key: str):
    """
    Ausgefranster unterer Rand: jede Strähne endet in einer anderen Zeile (zwischen lo und hi der Höhe), die letzte
    Zeile nur in der Mitte der Strähne (spitz). Gibt (None | 'tip' | 'hair') zurück.
    """
    i, w = _lock(ctx, width)
    end = math.floor((lo + hash01(i, ctx.face, key) * (hi - lo)) * ctx.h)
    if ctx.t > end:
        return None
    if ctx.t == end:
        return "tip" if 0.25 < w < 0.75 else None
    return "hair"


def _body(ctx: Ctx):
    """Unter der Fellschicht: dunkleres, dichtes Unterfell; der Bauch noch dunkler."""
    if ctx.face == "bottom":
        return pick(ctx.pal["fur"], 0.16 + (hash01(ctx.u, ctx.v) - 0.5) * 0.06)
    ry = rel_y(ctx) if ctx.face != "top" else 0.0
    return _fur(ctx, 0.4 - ry * 0.16, seed=3)


def _skirt(ctx: Ctx):
    """Langer Behang über den Knien: oben dicht (unter der Fellschicht), unten in spitzen Strähnen ausgefranst."""
    r = _ragged(ctx, 3.0, 0.62, 1.0, "skirt")
    if r is None:
        return None
    if r == "tip":
        return pick(ctx.pal["tip"], 0.2)
    return _fur(ctx, 0.36 - rel_y(ctx) * 0.1, seed=7, layer=6.0)


def _shag(ctx: Ctx):
    """Äußere Fellschicht: Rücken und Flanken dicht, unten in breiten, spitzen Strähnen ausgefranst."""
    face = ctx.face
    if face == "bottom":
        return None
    if face == "top":
        x, y, z = ctx.world
        if z < -4.0:
            return _mane(ctx) or _fur(ctx, 0.6, seed=5)
        return _fur(ctx, 0.6, seed=5)
    r = _ragged(ctx, 3.0, 0.6, 0.98, "shag")
    if r is None:
        return None
    if r == "tip":
        return pick(ctx.pal["tip"], 0.45)
    return _fur(ctx, 0.6 - rel_y(ctx) * 0.16, seed=5)


def _mane(ctx: Ctx):
    """Etwas dunklerer Aalstrich entlang des Rückgrats (oben auf Buckel und Rücken), zur Seite hin ausgefranst."""
    x, y, z = ctx.world
    edge = 1.6 + hash01(math.floor(z / 1.0), "mane") * 1.8
    if abs(x) > edge:
        return None
    return _fur(ctx, 0.44 - abs(x) / edge * 0.06, seed=33)


def _mantle(ctx: Ctx):
    """Mantel aus langem, hellerem Deckhaar über Schultern und Rücken, unten in spitzen Strähnen ausgefranst."""
    r = _ragged(ctx, 3.0, 0.55, 1.0, "mantle")
    if r is None:
        return None
    if r == "tip":
        return pick(ctx.pal["tip"], 0.6)
    return _fur(ctx, 0.66 - rel_y(ctx) * 0.08, seed=9)


def _hump(ctx: Ctx):
    """Schulterbuckel: dichtes, langes Fell, an den Seiten und vorn in Strähnen ausgefranst; oben die Mähne."""
    face = ctx.face
    if face == "bottom":
        return None
    if face == "top":
        return _mane(ctx) or _fur(ctx, 0.62, seed=31)
    r = _ragged(ctx, 2.0, 0.55, 1.0, "hump")
    if r is None:
        return None
    if r == "tip":
        return pick(ctx.pal["tip"], 0.5)
    return _fur(ctx, 0.58 - rel_y(ctx) * 0.1, seed=31, width=3.0, layer=4.0)


def _crest(ctx: Ctx):
    """Haarkamm: dunklere, aufgestellte Strähnen mit ausgefranster Oberkante und hellen Spitzen."""
    face, s, t = ctx.face, ctx.s, ctx.t
    if face == "bottom":
        return None
    if face == "top":
        return pick(ctx.pal["mane"], 0.5) if 1 <= s <= 2 else None
    i, w = _lock(ctx, 2.0)
    start = math.floor(hash01(i, face, "crest") * 0.6 * ctx.h)           # wo die Strähne oben endet
    if t < start:
        return None
    if t == start:
        return pick(ctx.pal["tip"], 0.3)
    v = 0.62 - (t - start) / max(ctx.h, 1) * 0.35 + (hash01(i, "c") - 0.5) * 0.12 + _LIT[face]
    return pick(ctx.pal["mane"], v)


def _moss(ctx: Ctx):
    """Moosmatte: unregelmäßiger Rand (dort durchsichtig), Moos mit Blüten oben, an den Seiten herabhängende Fransen."""
    x, y, z = ctx.world
    n = noise3(x * 0.3, 0, z * 0.3, 11)
    edge = max(abs(x) - 10.5, abs(z - 7) - 11)
    if n < 0.26 + (0.22 if edge > -2.0 else 0.0):
        return None
    if ctx.face == "bottom":
        return None
    if ctx.face == "top":
        h = hash01(math.floor(x), math.floor(z), "bloom")
        if h > 0.96:
            return pick(ctx.pal["bloom"], 0.7)
        if h > 0.93:
            return pick(ctx.pal["bloom2"], 0.75)
        return pick(ctx.pal["moss"], 0.35 + n * 0.55 + (hash01(ctx.u, ctx.v) - 0.5) * 0.15)
    return pick(ctx.pal["moss"], 0.3 + n * 0.3)


def _plant(ctx: Ctx):
    """Farnwedel bzw. Blütenstängel auf einer Ebene: Mittelrippe, Fiedern nach außen, Blüte oben."""
    if ctx.face not in ("front", "back", "west", "east"):
        return None
    lx, ly, lz = ctx.local
    ox, oy, oz = ctx.cube.origin
    w, h, d = ctx.cube.size
    u = ((lx - ox) / w if w > 0 else (lz - oz) / d) - 0.5               # -0.5 … 0.5 quer
    t = (ly - oy) / h                                                     # 0 oben … 1 unten
    i = ctx.cube.bone.cubes.index(ctx.cube)
    flower = i % 4 in (2, 3) and (i // 2) % 2 == 1
    if abs(u) < 0.12:
        return pick(ctx.pal["fern"], 0.4 - t * 0.15)                      # Stängel
    if flower:
        if t < 0.34 and abs(u) < 0.34:
            return pick(ctx.pal["bloom"] if i % 3 else ctx.pal["bloom2"], 0.8 - t)
        return None
    reach = 0.5 * (1.0 - t * 0.2) * (0.6 + 0.4 * math.sin(t * 9.0) ** 2)
    if abs(u) < reach and int(t * 8) % 2 == 0:
        return pick(ctx.pal["fern"], 0.55 - abs(u))                      # Fiedern
    return None


def _head(ctx: Ctx):
    """
    Kopf: zotteliges Fell; die kleinen, glänzenden Augen sitzen seitlich vorn unter dem Stirnschopf, von einem Ring
    aus kurzem, hellem Fell umgeben.
    """
    face, s, t = ctx.face, ctx.s, ctx.t
    if face in ("west", "east"):
        col = ctx.w - 1 - s if face == "west" else s                      # 0 = vorderste Spalte
        if col in (1, 2) and t in (5, 6):
            if t == 5 and col == 2:
                return pick(ctx.pal["eye"], 0.95)                         # Glanzpunkt
            return pick(ctx.pal["eye"], 0.2)
        if col in (0, 1, 2, 3) and t in (4, 5, 6, 7):
            return pick(ctx.pal["tip"], 0.3 if t in (4, 7) or col in (0, 3) else 0.15)   # heller Augenring
    return _fur(ctx, 0.5 - rel_y(ctx) * 0.1 if face != "top" else 0.56, seed=13, width=3.0, layer=4.0)


def _forelock(ctx: Ctx):
    if ctx.face == "bottom":
        return None
    if ctx.face in ("front", "west", "east"):
        r = _ragged(ctx, 2.0, 0.55, 1.0, "fl")
        if r is None:
            return None
        if r == "tip":
            return pick(ctx.pal["tip"], 0.55)
    return _fur(ctx, 0.58, seed=17, width=2.0, layer=3.0)


def _beard(ctx: Ctx):
    """Backenbart: lange Strähnen an den Wangen, unten spitz ausgefranst."""
    r = _ragged(ctx, 2.0, 0.5, 1.0, "beard")
    if r is None:
        return None
    if r == "tip":
        return pick(ctx.pal["tip"], 0.4)
    return _fur(ctx, 0.5 - rel_y(ctx) * 0.12, seed=37, width=2.0, layer=4.0)


def _ear(ctx: Ctx):
    ry = rel_y(ctx)
    if ctx.face in ("west", "east"):
        r = _ragged(ctx, 2.0, 0.8, 1.0, "ear")
        if r is None:
            return None
        if r == "tip" or ry > 0.8:
            return pick(ctx.pal["tip"], 0.5)
    return _fur(ctx, 0.44 - ry * 0.1, seed=19, width=2.0)


def _nose(ctx: Ctx):
    """Rüsselplatte: ledrig, mit Querfalten, vorn zwei große Nüstern."""
    face, s, t = ctx.face, ctx.s, ctx.t
    if face == "front":
        if s in (3, 4, 8, 9):
            return pick(ctx.pal["leather"], 0.12)                         # Nüstern
        return pick(ctx.pal["leather"], 0.55 - t * 0.15)
    if face == "top":
        v = 0.6 - (ctx.t % 3 == 0) * 0.08                                 # Querfalten
        if ctx.t >= ctx.h - 2:
            v += 0.05
        return pick(ctx.pal["leather"], v + (hash01(ctx.u, ctx.v) - 0.5) * 0.05)
    if face == "bottom":
        return pick(ctx.pal["leather"], 0.3)
    return pick(ctx.pal["leather"], 0.48 + _LIT[face])


def _snout(ctx: Ctx):
    """Schaufelschnauze: oben und an den Seiten kurzes Fell, vorn ledrig mit dem Maulspalt unten."""
    face, s, t = ctx.face, ctx.s, ctx.t
    if face == "front":
        if t == ctx.h - 3:
            return pick(ctx.pal["leather"], 0.1)                          # Maulspalt
        if t > ctx.h - 3:
            return pick(ctx.pal["leather"], 0.4)
        return pick(ctx.pal["leather"], 0.62 - t * 0.02 + (hash01(ctx.u, ctx.v) - 0.5) * 0.06)
    if face == "bottom":
        return pick(ctx.pal["fur"], 0.25)
    if face in ("west", "east", "top"):
        return pick(ctx.pal["tip"], 0.32 - rel_y(ctx) * 0.18 + _LIT[face] + (hash01(ctx.u, ctx.v) - 0.5) * 0.12)   # helle Schnauze
    return _fur(ctx, 0.48, seed=23, width=2.0, layer=3.0)


def _tusk(ctx: Ctx):
    return pick(ctx.pal["tusk"], 0.85 - rel_y(ctx) * 0.35 + _LIT[ctx.face])


def _leg(ctx: Ctx):
    if ctx.face == "bottom":
        return pick(ctx.pal["claw"], 0.3)
    ry = rel_y(ctx)
    if _look(ctx) == "steppenwuehler":
        return _fur(ctx, 0.2 - ry * 0.08, seed=29, width=3.0, layer=3.5, ramp_key="mane")   # dunkle Beine wie bei einem Falben
    return _fur(ctx, 0.32 - ry * 0.14, seed=29, width=3.0, layer=3.5)


def _claw(ctx: Ctx):
    if ctx.face == "front" and ctx.s % 2 == 1:
        return pick(ctx.pal["fur"], 0.15)                                 # Lücke zwischen den Krallen
    return pick(ctx.pal["claw"], 0.7 - rel_y(ctx) * 0.3 + _LIT[ctx.face])


def materials(variant: str) -> dict:
    return {
        "body": _body, "skirt": _skirt, "shag": _shag, "mantle": _mantle, "hump": _hump, "moss": _moss, "plant": _plant,
        "head": _head, "forelock": _forelock, "beard": _beard, "ear": _ear, "nose": _nose, "snout": _snout, "tusk": _tusk,
        "leg": _leg, "claw": _claw,
    }


def hidden(variant: str) -> list[str]:
    return []
