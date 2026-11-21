"""
Frösche (statt Vanilla-Frosch) auf dem Skelett des Vanilla-Froschs: dieselben Teile und Drehpunkte wie FrogModel
(Wurzel "root" mit Leib, Kopf samt Augen, Schallblase "croaking_body", Zunge, Arme mit Händen, Beine mit Füßen), damit
Hüpfen, Quaken, Zunge, Schwimmen und Gehen genau wie bei Vanilla ablaufen. Eine Art je Vanilla-Variante (nach der
Wärme des Bioms):

  * teichfrosch  (statt "temperate"): sattgrün mit dunklen Flecken, hellgelber Rückenlinie und bronzenen
    Drüsenleisten, cremeweißer Bauch, goldene Augen; beim Quaken zwei weiße Schallblasen an den Mundwinkeln
  * feuerkroete  (statt "warm"): dunkel olivgrauer, warziger Rücken ohne Trommelfell, leuchtend orangeroter Bauch
    mit schwarzen Flecken, orange Zehenspitzen
  * grasfrosch   (statt "cold"): rostbraun mit dunklen Flecken, dem dunklen Winkel zwischen den Schultern und der
    dunklen Schläfenmaske über dem Trommelfell, helle Oberlippe, gelblich marmorierter Bauch

Wie bei Vanilla ist der Kopf der ganze Oberkiefer und der Leib der Unterkiefer; die Nahtzeile, in der sich beide
überdecken, wird für beide gleich gemalt (Maulspalte), damit nichts flimmert. Hände, Füße, Zunge und Maulflächen sind
flache Flächen, von denen nur die Oberseite bemalt ist (die Unterseite bleibt durchsichtig, so zeigt Minecraft von
beiden Seiten dieselbe Fläche). Alle Quader haben ganzzahlige Maße, damit die UVs in Minecraft genau passen.
"""
from __future__ import annotations

import math

from ..geometry import Bone, Model
from ..paint import Ctx, hash01, mix, noise3, pick, ramp


def _pal(**kw) -> dict:
    pal = {k: ramp(*v) if isinstance(v, tuple) else v for k, v in kw.items()}
    pal.setdefault("back", pal["skin"])
    return pal


PALETTES = {
    "teichfrosch": _pal(skin=("#1c4a0c", "#b6ec5a"), spot=("#0a1404", "#2c4a12"), stripe=("#86a826", "#f4f7a0"),
                        ridge=("#7a6a1e", "#e2c868"), belly=("#aca886", "#fffbe8"), lip=("#a6b06a", "#f4f6cc"),
                        iris=("#7a5208", "#ffdc50"), mouth=("#7a2a3c", "#f6a2b2"), sac=("#a8aa9c", "#fffff6"),
                        web=("#3a6418", "#c4ec78"), tymp=("#4e5a16", "#c8b44a")),
    "feuerkroete": _pal(skin=("#20221a", "#8a9264"), spot=("#060706", "#26281c"), stripe=("#4e5834", "#c4ca86"),
                        ridge=("#3a4026", "#a2a86a"), belly=("#9a1804", "#ff8432"), lip=("#2a2c1c", "#6a6c4a"),
                        iris=("#6a4c0e", "#eac444"), mouth=("#661818", "#e26050"), sac=("#a82808", "#ff9444"),
                        web=("#20221a", "#5a6044"), tymp=("#22241a", "#4a4e36")),
    "grasfrosch": _pal(skin=("#3c1c0a", "#d6a064"), spot=("#1a0c05", "#4c2a14"), stripe=("#7e5028", "#eec290"),
                       ridge=("#8a5a30", "#f2cc98"), belly=("#ae9e76", "#fcf2d4"), lip=("#b09068", "#f8e8c8"),
                       iris=("#5c3a12", "#ecbe66"), mouth=("#6a2a2c", "#e2828c"), sac=("#8e9aa6", "#f2f8fc"),
                       web=("#6a3c1c", "#dca272"), tymp=("#1e0e06", "#5a321a")),
}


def _vanilla_root(m: Model, pivot) -> Bone:
    """Hängt den Vanilla-Knochen "root" unter die Modellwurzel (Model.bone() lehnt den Namen ab)."""
    b = Bone("root", m.root, tuple(float(v) for v in pivot))
    m.root.children.append(b)
    m.bones["root"] = b
    return b


TOP = ("top",)


def build() -> Model:
    m = Model("frog", 128, 64)
    _vanilla_root(m, (0, 24, 0))
    # Leib = Unterkiefer und Bauch; oben drauf der Kopf (Oberkiefer = ganzer Rücken), dazwischen das Maul
    m.bone("body", "root", pivot=(0, -2, 4))
    m.cube("body", (-3.5, -2, -8), (7, 3, 9), "body")
    m.cube("body", (-3.5, -1, -8), (7, 0, 9), "mouth", faces=TOP)
    m.bone("head", "body", pivot=(0, -2, -1))
    m.cube("head", (-3.5, -1, -7), (7, 0, 9), "mouth", faces=TOP)
    m.cube("head", (-3.5, -2, -7), (7, 3, 9), "head")
    # Glubschaugen: 3 hoch, unten 0,4 im Kopf, seitlich 0,25 eingerückt (keine deckungsgleichen Flächen)
    m.bone("eyes", "head", pivot=(-0.5, 0, 2))
    m.bone("right_eye", "eyes", pivot=(-1.5, -3, -6.5))
    m.cube("right_eye", (-1.25, -1.6, -1.5), (3, 3, 3), "eye")
    m.bone("left_eye", "eyes", pivot=(2.5, -3, -6.5))
    m.cube("left_eye", (-1.75, -1.6, -1.5), (3, 3, 3), "eye")
    # Schallblase(n) unter dem Kinn – sichtbar nur beim Quaken (FrogModel blendet sie sonst aus)
    m.bone("croaking_body", "body", pivot=(0, -1, -5))
    m.cube("croaking_body", (-3.5, -0.1, -2.9), (7, 2, 3), "sac", inflate=-0.1)
    m.bone("tongue", "body", pivot=(0, -1.01, 1))
    m.cube("tongue", (-2, 0, -7.1), (4, 0, 7), "tongue", faces=TOP)
    # Arme mit flachen Händen
    m.bone("left_arm", "body", pivot=(4, -1, -6.5))
    m.cube("left_arm", (-1, 0, -1), (2, 3, 3), "arm")
    m.bone("left_hand", "left_arm", pivot=(0, 3, -1))
    m.cube("left_hand", (-4, 0.01, -4), (8, 0, 8), "hand", faces=TOP)
    m.bone("right_arm", "body", pivot=(-4, -1, -6.5))
    m.cube("right_arm", (-1, 0, -1), (2, 3, 3), "arm")
    m.bone("right_hand", "right_arm", pivot=(0, 3, 0))
    m.cube("right_hand", (-4, 0.01, -5), (8, 0, 8), "hand", faces=TOP)
    # Hinterbeine: Oberschenkel wie Vanilla, dazu der eingeklappte Unterschenkel an der Flanke; Schwimmfüße
    m.bone("left_leg", "root", pivot=(3.5, -3, 4))
    m.cube("left_leg", (-1, 0, -2), (3, 3, 4), "thigh")
    m.cube("left_leg", (-0.2, 0.9, -5.5), (2, 2, 4), "shin")
    m.bone("left_foot", "left_leg", pivot=(2, 3, 0))
    m.cube("left_foot", (-4, 0.01, -4), (8, 0, 8), "foot", faces=TOP)
    m.bone("right_leg", "root", pivot=(-3.5, -3, 4))
    m.cube("right_leg", (-2, 0, -2), (3, 3, 4), "thigh")
    m.cube("right_leg", (-1.8, 0.9, -5.5), (2, 2, 4), "shin")
    m.bone("right_foot", "right_leg", pivot=(-2, 3, 0))
    m.cube("right_foot", (-4, 0.01, -4), (8, 0, 8), "foot", faces=TOP)
    return m


def hidden(variant: str) -> list[str]:
    """Vorschau: die Schallblase ist nur beim Quaken zu sehen."""
    return ["croaking_body"]


# =============================================================================================
# Malen
# =============================================================================================

def _variant(ctx: Ctx) -> str:
    return ctx.params.get("variant")


def _lit(ctx: Ctx, v: float) -> float:
    if ctx.face == "top":
        return v + 0.05
    if ctx.face == "bottom":
        return v - 0.06
    if ctx.face in ("west", "back"):
        return v - 0.03
    return v


def _bevel(ctx: Ctx) -> float:
    """Gerundete Kanten: Oberseite zum Rand hin dunkler, Seiten oben heller, unten dunkler."""
    f, s, t, w, h = ctx.face, ctx.s, ctx.t, ctx.w, ctx.h
    if f == "top" and w > 2 and h > 2:
        return -0.035 if (s in (0, w - 1) or t in (0, h - 1)) else 0.015
    if f in ("west", "east", "front", "back") and h > 1:
        if t == 0:
            return 0.035
        if t == h - 1 and h > 2:
            return -0.04
    return 0.0


def _blotch(x: float, y: float, z: float, size: float, seed: int, chance: float, radius: float) -> float:
    """Runde Flecken im Raum: 0 = Fleckmitte, >= 1 = kein Fleck."""
    cx, cy, cz = math.floor(x / size), math.floor(y / size), math.floor(z / size)
    best = 9.0
    for dx in (-1, 0, 1):
        for dy in (-1, 0, 1):
            for dz in (-1, 0, 1):
                ix, iy, iz = cx + dx, cy + dy, cz + dz
                if hash01(ix, iy, iz, seed) > chance:
                    continue
                px = (ix + 0.2 + 0.6 * hash01(ix, iy, iz, seed + 1)) * size
                py = (iy + 0.2 + 0.6 * hash01(ix, iy, iz, seed + 2)) * size
                pz = (iz + 0.2 + 0.6 * hash01(ix, iy, iz, seed + 3)) * size
                r = radius * (0.7 + 0.6 * hash01(ix, iy, iz, seed + 4))
                best = min(best, math.sqrt((x - px) ** 2 + (y - py) ** 2 + (z - pz) ** 2) / r)
    return best


def _back(ctx: Ctx, base: float = 0.55, bevel: bool = True) -> tuple:
    """Rückenhaut mit dem Muster der Art (Flecken laufen über alle Flächen, weil sie im Raum liegen)."""
    v, (x, y, z) = _variant(ctx), ctx.world
    tone = base + (noise3(x * 0.45, y * 0.45, z * 0.45, 31) - 0.5) * 0.1 + (_bevel(ctx) if bevel else 0.0)
    col = pick(ctx.pal["skin"], _lit(ctx, tone))
    if v == "teichfrosch":
        b = _blotch(x, y, z, 2.5, 32, 0.5, 0.95)
        if b < 0.55:
            return pick(ctx.pal["spot"], _lit(ctx, 0.35))
        if b < 1.0:
            return mix(col, pick(ctx.pal["spot"], 0.55), 0.6)
    elif v == "feuerkroete":
        big = _blotch(x, y, z, 3.2, 38, 0.45, 1.2)
        if big < 1.0:
            col = mix(col, pick(ctx.pal["spot"], 0.6), 0.55)              # dunkle Flecken
        w = _blotch(x, y, z, 1.7, 33, 0.55, 0.6)
        if w < 0.4:
            return pick(ctx.pal["spot"], 0.3)                             # dunkle Warzenspitze
        if w < 1.0:
            return pick(ctx.pal["stripe"], _lit(ctx, 0.48 + (1.0 - w) * 0.2))   # Warze
    elif v == "grasfrosch":
        b = _blotch(x, y, z, 2.4, 34, 0.5, 0.85)
        if b < 0.55:
            return pick(ctx.pal["spot"], _lit(ctx, 0.4))
        if b < 1.0:
            return mix(col, pick(ctx.pal["spot"], 0.6), 0.55)
    return col


def _belly(ctx: Ctx, base: float = 0.55) -> tuple:
    v, (x, y, z) = _variant(ctx), ctx.world
    col = pick(ctx.pal["belly"], _lit(ctx, base + (noise3(x * 0.5, y * 0.5, z * 0.5, 35) - 0.5) * 0.08))
    if v == "feuerkroete":
        b = _blotch(x, y, z, 2.1, 36, 0.6, 0.85)
        if b < 0.6:
            return pick(ctx.pal["spot"], 0.25)                            # schwarze Flecken im roten Bauch
        if b < 1.0:
            return mix(col, pick(ctx.pal["spot"], 0.3), 0.6)
    elif v == "grasfrosch":
        if _blotch(x, y, z, 1.7, 37, 0.5, 0.65) < 1.0:
            return mix(col, (150, 120, 70, 255), 0.3)                     # gelblich graue Marmorierung
    elif v == "teichfrosch":
        if _blotch(x, y, z, 2.0, 39, 0.3, 0.55) < 1.0:
            return mix(col, pick(ctx.pal["skin"], 0.3), 0.18)            # zarte graugrüne Tupfen
    return col


def _slit(ctx: Ctx) -> tuple:
    return mix(pick(ctx.pal["spot"], 0.4), pick(ctx.pal["skin"], 0.3), 0.5)


def _band(ctx: Ctx) -> tuple:
    """Die Nahtzeile zwischen Ober- und Unterkiefer (Kopf unten = Leib oben): vorn und an den Seiten bis unter das
    Trommelfell die dunkle Maulspalte, dahinter Haut. Hängt nur von der Weltlage ab -> Kopf und Leib malen gleich."""
    face, z = ctx.face, ctx.world[2]
    if face == "front":
        return _slit(ctx)
    if face in ("west", "east") and z < 1.0:
        return _slit(ctx) if z < 0.0 else mix(_slit(ctx), _back(ctx, 0.5, bevel=False), 0.45)   # Mundwinkel
    return _back(ctx, 0.5, bevel=False)


def _dorsal(ctx: Ctx, col: tuple) -> tuple:
    """Oberseite des Kopfs (= Rücken): Rückenlinie, Drüsenleisten und beim Grasfrosch der Schulterwinkel."""
    x, z = ctx.world[0], ctx.world[2]
    v = _variant(ctx)
    if v == "teichfrosch":
        if abs(x) < 0.5 and z > -3.0:
            return pick(ctx.pal["stripe"], 0.72 if z < 3.5 else 0.6)      # hellgelbe Rückenlinie
        if abs(abs(x) - 3.0) < 0.5 and z > -0.5:
            return pick(ctx.pal["ridge"], 0.58)                           # bronzene Drüsenleisten
    elif v == "grasfrosch":
        if abs(abs(x) - 3.0) < 0.5 and z > -0.5:
            return pick(ctx.pal["ridge"], 0.52)
        if (round(abs(x)), round(z - 0.5)) in ((0, 0), (1, 1), (2, 2)):
            return pick(ctx.pal["spot"], 0.32)                            # dunkler Winkel zwischen den Schultern
    return col


def _head(ctx: Ctx):
    v, face, s, t, w, h = _variant(ctx), ctx.face, ctx.s, ctx.t, ctx.w, ctx.h
    x, y, z = ctx.world
    if face == "bottom":
        return pick(ctx.pal["mouth"], 0.5 if abs(x) > 0.5 else 0.38)     # Gaumen mit Mittelfurche
    if face in ("west", "east", "front", "back") and t == h - 1:
        return _band(ctx)
    if face == "top":
        if z < -3.0 and abs(abs(x) - 1.0) < 0.3:
            return pick(ctx.pal["spot"], 0.18)                            # Nasenlöcher
        return _dorsal(ctx, _back(ctx, 0.6))
    if face == "front":
        return _back(ctx, 0.6 if t == 0 else 0.54)
    if face in ("west", "east"):
        tymp = 0.0 <= z < 2.0
        if v == "grasfrosch":
            if t == 0 and -1.0 <= z < 2.0:
                return pick(ctx.pal["tymp"], 0.42 if tymp else 0.5)      # Schläfenmaske hinter dem Auge
            if t == 1 and tymp:
                return pick(ctx.pal["tymp"], 0.62 if z < 1.0 else 0.4)   # Trommelfell in der Maske
            if t == 1 and z < 0.0:
                return mix(_back(ctx, 0.56), pick(ctx.pal["lip"], 0.5), 0.6)   # helle Oberlippe
        elif v == "teichfrosch" and tymp:
            return pick(ctx.pal["tymp"], 0.68 if (t == 0 and z < 1.0) else 0.5)   # Trommelfell mit Glanzpunkt
        elif v == "teichfrosch" and t == 1 and z < 0.0:
            return mix(_back(ctx, 0.56), pick(ctx.pal["lip"], 0.5), 0.35)  # hellere Oberlippe
        return _back(ctx, 0.54)
    return _back(ctx, 0.5)


def _body(ctx: Ctx):
    v, face, s, t, w, h = _variant(ctx), ctx.face, ctx.s, ctx.t, ctx.w, ctx.h
    if face == "top":                                                     # Mundboden (bei offenem Maul)
        edge = s in (0, w - 1) or t in (0, h - 1)
        return pick(ctx.pal["mouth"], 0.32 if edge else 0.46)
    if face == "bottom":
        return _belly(ctx)
    if t == 0:
        return _band(ctx)
    if face == "front":
        if t == 1:
            return pick(ctx.pal["lip"], 0.55 + (0.04 if 0 < s < w - 1 else -0.04))   # Unterlippe
        return _belly(ctx, 0.62)                                          # Kehle
    if face == "back":
        return _back(ctx, 0.46) if t == 1 else mix(_back(ctx, 0.44), _belly(ctx, 0.5), 0.5)
    # Flanken: Mitte Haut (bei der Unke mit roten Flecken), unten der helle bzw. rote Bauch mit Sprenkeln
    x, y, z = ctx.world
    if t == 1:
        col = _back(ctx, 0.5)
        if v == "feuerkroete" and _blotch(x, y, z, 1.9, 40, 0.45, 0.7) < 1.0:
            return pick(ctx.pal["belly"], 0.45)
        return col
    col = _belly(ctx, 0.5)
    if v != "feuerkroete" and _blotch(x, y, z, 2.2, 41, 0.45, 0.75) < 1.0:
        return mix(col, pick(ctx.pal["spot"], 0.5), 0.45)
    return col


def _mouth(ctx: Ctx):
    return pick(ctx.pal["mouth"], 0.42)


def _eye(ctx: Ctx):
    """Glubschauge: oben das gewölbte Lid, darunter die goldene Iris mit waagerechter Pupille."""
    face, s, t = ctx.face, ctx.s, ctx.t
    if face == "top":
        return pick(ctx.pal["skin"], 0.68 if (s, t) == (1, 1) else 0.58)
    if face == "bottom":
        return pick(ctx.pal["skin"], 0.35)
    if face == "back":
        return pick(ctx.pal["skin"], 0.56 - t * 0.04)
    if t == 0:
        return pick(ctx.pal["skin"], 0.62)                                # Lid
    if t == 1:
        if s == 1:
            return (12, 10, 8, 255)                                       # Pupille
        return pick(ctx.pal["iris"], 0.82 if s == 0 else 0.72)
    return pick(ctx.pal["iris"], 0.46 if s == 1 else 0.4)


def _sac(ctx: Ctx):
    """Schallblase: beim Teichfrosch zwei Blasen an den Mundwinkeln, sonst eine Kehlblase."""
    v, face, s, t, w = _variant(ctx), ctx.face, ctx.s, ctx.t, ctx.w
    x = ctx.local[0]
    if v == "teichfrosch" and face in ("front", "back", "top", "bottom") and abs(x) < 1.6:
        return None
    if face in ("west", "east"):                                          # pralle Blase: oben Glanz, unten Schatten
        shade = (0.84 if t == 0 else 0.62) + (0.05 if s == 1 else 0.0)
    elif face == "front":
        shade = 0.78 if t == 0 else 0.6
    elif face == "top":
        shade = 0.72
    else:
        shade = 0.52
    col = pick(ctx.pal["sac"], shade)
    wx, wy, wz = ctx.world
    if v == "feuerkroete" and _blotch(wx, wy, wz, 1.6, 42, 0.45, 0.55) < 1.0:
        return pick(ctx.pal["spot"], 0.3)
    if v == "grasfrosch" and _blotch(wx, wy, wz, 1.4, 43, 0.4, 0.5) < 1.0:
        return mix(col, (150, 130, 90, 255), 0.3)
    return col


_TONGUE = (     # Zeile 0 = Spitze, Spalte 0 = links (minX)
    ".TT.",
    "TTTT",
    "tTTt",
    "tggt",
    ".gg.",
    ".tt.",
    ".tt.",
)


def _tongue(ctx: Ctx):
    lx, lz = ctx.local[0], ctx.local[2]
    r, c = int(math.floor(lz + 7.1)), int(math.floor(lx + 2))
    if not (0 <= r < 7 and 0 <= c < 4):
        return None
    k = _TONGUE[r][c]
    if k == ".":
        return None
    return pick(ctx.pal["mouth"], {"T": 0.86, "t": 0.66, "g": 0.56}[k])


# Hände und Füße von oben (Spalte 0 = innen unter dem Leib, Zeile 0 = vorn); rechts gespiegelt.
_HAND = (
    ".o..o...",
    "..f.f...",
    "...ff.o.",
    "...ppf..",
    "...pppfo",
    "........",
    "........",
    "........",
)
_FOOT = (
    "....o...",
    "....t...",
    "....t...",
    "....t..o",
    "....twt.",
    "....ttw.",
    "...hhtto",
    ".....o..",
)


def _digit_color(ctx: Ctx, k: str):
    v = _variant(ctx)
    if k == ".":
        return None
    if k == "o":                                                          # Haftballen / Zehenspitze
        if v == "feuerkroete":
            return pick(ctx.pal["belly"], 0.6)
        return pick(ctx.pal["web"], 0.64) if v == "teichfrosch" else pick(ctx.pal["lip"], 0.42)
    if k == "w":                                                          # Schwimmhaut
        return pick(ctx.pal["web"], 0.5)
    jitter = (hash01(ctx.u, ctx.v, 3, 77) - 0.5) * 0.06
    return pick(ctx.pal["skin"], (0.56 if k in "ph" else 0.62) + jitter)


def _hand(ctx: Ctx):
    lx, lz = ctx.local[0], ctx.local[2]
    col = int(math.floor(lx + 4))
    if ctx.cube.bone.name == "right_hand":
        col, row = 7 - col, int(math.floor(lz + 5))
    else:
        row = int(math.floor(lz + 4))
    if not (0 <= row < 8 and 0 <= col < 8):
        return None
    return _digit_color(ctx, _HAND[row][col])


def _foot(ctx: Ctx):
    lx, lz = ctx.local[0], ctx.local[2]
    col, row = int(math.floor(lx + 4)), int(math.floor(lz + 4))
    if ctx.cube.bone.name == "right_foot":
        col = 7 - col
    if not (0 <= row < 8 and 0 <= col < 8):
        return None
    return _digit_color(ctx, _FOOT[row][col])


def _arm(ctx: Ctx):
    face, t, h = ctx.face, ctx.t, ctx.h
    if face == "bottom":
        return _belly(ctx, 0.5)
    col = _back(ctx, 0.52)
    if face in ("west", "east", "front", "back") and t == h - 1:
        col = mix(col, pick(ctx.pal["belly"], 0.5), 0.4)
    elif face in ("west", "east") and t == 1 and _variant(ctx) != "feuerkroete":
        col = mix(col, pick(ctx.pal["spot"], 0.45), 0.45)                 # Querband
    return col


def _bands(ctx: Ctx, col: tuple) -> tuple:
    """Dunkle Querbänder auf Ober- und Unterschenkel (nicht bei der Unke)."""
    if ctx.face in ("top", "west", "east") and _variant(ctx) != "feuerkroete":
        if math.floor(ctx.world[2]) % 2 == 0:
            return mix(col, pick(ctx.pal["spot"], 0.45), 0.4)
    return col


def _thigh(ctx: Ctx):
    if ctx.face == "bottom":
        return _belly(ctx, 0.5)
    col = _back(ctx, 0.56)
    if ctx.face in ("west", "east", "front", "back") and ctx.t == ctx.h - 1:
        col = mix(col, pick(ctx.pal["belly"], 0.5), 0.3)
    return _bands(ctx, col)


def _shin(ctx: Ctx):
    if ctx.face == "bottom":
        return _belly(ctx, 0.48)
    col = _back(ctx, 0.5)
    if ctx.face in ("west", "east", "front", "back") and ctx.t == ctx.h - 1:
        col = mix(col, pick(ctx.pal["belly"], 0.48), 0.3)
    return _bands(ctx, col)


def materials(variant: str) -> dict:
    return {
        "body": _body, "mouth": _mouth, "head": _head, "eye": _eye, "sac": _sac, "tongue": _tongue, "arm": _arm,
        "hand": _hand, "thigh": _thigh, "shin": _shin, "foot": _foot,
    }
