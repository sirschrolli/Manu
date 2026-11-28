"""
Phönix – das Geschoss des Feuerzaubers: ein Feuervogel, der nach vorn (-Z) fliegt. Als Geschoss gilt die Boden-Regel
nicht: die Mitte des Rumpfs liegt im Ursprung.

Knochen (Namen und Drehpunkte werden im Renderer für die Animation verwendet, alle ohne Grunddrehung):
  * body            Rumpf (6 × 5 × 10) mit weißglühender Brust, Drehpunkt (0, 0, 0)
  * head            Kopf vorn auf dem Rumpf, Drehpunkt (0, -1, -5): Hakenschnabel, glühende Augen, Federkrone aus
                    drei Flammenfedern, die nach hinten oben schwingen
  * left_wing       Armschwinge nach +X, Drehpunkt an der linken Schulter (3, -1, -1), 12 lang
    left_wing_tip   Handschwinge, Drehpunkt am Handgelenk (12, 0, 0), sechs gespreizte Handschwingen bis ca. 12 weit
  * right_wing / right_wing_tip   gespiegelt nach -X
  * tail            Schweif, Drehpunkt (0, 0, 5): drei lange Schmuckfedern (13–15) als Fächer nach +Z, darunter kurze
                    Flammenfedern, dazu eine senkrechte Schleppe (von der Seite sichtbar)

Das Spiel schlägt die Flügel über zRot (Schwinge und Handschwinge), wiegt den Schweif und lässt den Kopf nicken.

Alles glüht: dunkles Karmesin am Federansatz, über Orange und Gelb bis fast Weiß an den Federspitzen und an der Brust.
Jedes Texel steht auch im Leuchtbild (etwas gedämpft, damit Licht + Leuchten nicht zu reinem Weiß verschmelzen).
Schwungfedern, Schweif und Krone sind flache Flächen mit gemalten Umrissen: Deckfedern liegen über den Schwungfedern,
jede Feder wirft einen feinen Schatten auf die darunterliegende.
"""
from __future__ import annotations

import math

from ..geometry import Model
from ..paint import Ctx, hexc, mix, noise3, pick, shade

PALETTES = {
    "phoenix": {
        "fire": [hexc(c) for c in ("#3c040c", "#62081a", "#8c0e1c", "#b81c18", "#dc3a14", "#f2621a", "#ff8c24", "#ffb232",
                                   "#ffd250", "#ffe988", "#fff6c4", "#fffdf0")],
        "gold": [hexc(c) for c in ("#6a2a04", "#a24e08", "#d27c12", "#f2a824", "#ffd04e", "#ffec96", "#fffae0")],
        "eye": [hexc(c) for c in ("#1e0204", "#fff4d0", "#ffffff")],
    },
}

GLOW = 0.6                 # Anteil der Farbe im Leuchtbild
ARM = 12.0                 # Armschwinge (Schulter -> Handgelenk)


# =============================================================================================
# Federn als gemalte Umrisse auf flachen Flächen
# =============================================================================================

class Feather:
    """Eine Feder in Flächen-Koordinaten (a, b): Ansatz, Richtung (Grad, 0 = +a, 90 = +b), Länge, Breite.

    ``kind``:
      * "vane"   Schwungfeder: breite Fahne, runde Spitze mit kleinem Flammenschwung, heller Kiel
      * "covert" Deckfeder: kurz, rund
      * "flame"  Flammenfeder: nahe am Ansatz am breitesten, läuft geschwungen spitz zu
      * "plume"  Schmuckfeder: dünner Kiel, dann eine lange Fahne mit leuchtendem Auge
    ``curl``: Biegung der Achse zur Spitze hin (Anteil der Länge, + = nach links von der Richtung gesehen),
    ``wave``: S-Schwung der Achse, ``v0``/``v1``: Helligkeit am Ansatz / an der Spitze (0..1 der Feuer-Rampe)."""

    def __init__(self, root, ang, length, width, kind="vane", curl=0.0, v0=0.12, v1=0.92, wave=0.0):
        self.a0, self.b0 = root
        r = math.radians(ang)
        self.da, self.db = math.cos(r), math.sin(r)
        self.length = length
        self.width = width
        self.kind = kind
        self.curl = curl
        self.v0, self.v1 = v0, v1
        self.wave = wave

    def axis_offset(self, t: float) -> float:
        off = self.curl * self.length * t * t
        if self.wave:
            off += self.wave * math.sin(t * math.pi * 1.6) * t
        return off

    def half_width(self, t: float) -> float:
        w = self.width * 0.5
        if self.kind == "plume":
            shaft = 0.38
            if t < 0.34:
                return shaft
            k = (t - 0.34) / 0.66
            return max(shaft, w * math.sin(math.pi * min(1.0, k ** 0.72)) ** 0.85)
        if self.kind == "flame":
            if t < 0.28:
                return w * (0.55 + 0.45 * (t / 0.28) ** 0.6)
            k = (t - 0.28) / 0.72
            return w * (1.0 - k) ** 1.25
        if self.kind == "covert":
            if t < 0.12:
                return w * (0.7 + 0.3 * t / 0.12)
            if t < 0.5:
                return w
            k = (t - 0.5) / 0.5
            return w * math.sqrt(max(0.0, 1.0 - k * k))
        if t < 0.14:
            return w * (0.5 + 0.5 * (t / 0.14) ** 0.6)
        if t < 0.6:
            return w
        k = (t - 0.6) / 0.4
        return w * math.sqrt(max(0.0, 1.0 - k ** 2.2)) * (1.0 - 0.18 * k)

    def hit(self, a: float, b: float):
        """-> (t längs 0..1, s quer -1..1, Abstand zum Rand) oder None."""
        ra, rb = a - self.a0, b - self.b0
        t = (ra * self.da + rb * self.db) / self.length
        if t < -0.03 or t > 1.0:
            return None
        t = max(0.0, t)
        across = -ra * self.db + rb * self.da - self.axis_offset(t)
        half = self.half_width(t)
        if half <= 0.05 or abs(across) > half:
            return None
        return t, across / half, half - abs(across)


def _fan_color(ctx: Ctx, feathers: list[Feather], a: float, b: float):
    """Farbe einer Federfläche: oberste Feder am Punkt (die Liste geht von unten nach oben), Verlauf vom Ansatz zur
    Spitze, Kiel, dunkler Saum und Schlagschatten der darüberliegenden Feder. None = keine Feder (durchsichtig)."""
    hit = None
    idx = -1
    for i in range(len(feathers) - 1, -1, -1):
        h = feathers[i].hit(a, b)
        if h is not None:
            hit, idx = h, i
            break
    if hit is None:
        return None
    f = feathers[idx]
    t, s, margin = hit
    if f.kind == "plume":
        if t < 0.34:
            v = f.v0 + 0.3 * t / 0.34 + (0.06 if abs(s) < 0.5 else -0.06)
        else:
            k = (t - 0.34) / 0.66
            ring = math.hypot(s * 1.05, (k - 0.62) * 2.6)                # Auge kurz vor der Spitze
            v = f.v1 - 0.5 * min(1.0, ring) ** 1.2
            if ring < 0.3:
                v = 1.0
            elif ring > 0.95 and k > 0.62:
                v = f.v1 - 0.18                                          # Flammensaum hinter dem Auge
    elif f.kind == "flame":
        v = f.v0 + (f.v1 - f.v0) * t ** 0.8 + 0.12 * (1.0 - abs(s)) - 0.04
    else:
        v = f.v0 + (f.v1 - f.v0) * t ** 0.9 - 0.06 * s * s
        if f.kind == "vane" and abs(s) < 0.18 and t < 0.84:
            v += 0.06                                                    # heller Kiel
    soft = 0.55 if f.kind == "covert" else 1.0
    if margin < 0.3 and t > 0.04 and f.kind != "plume":
        v -= (0.11 if s > 0 else 0.06) * soft                            # Saum (Schattenseite kräftiger)
    # Schlagschatten der darüberliegenden Federn
    for j in range(idx + 1, len(feathers)):
        g = feathers[j]
        ra, rb = a - g.a0, b - g.b0
        tt = (ra * g.da + rb * g.db) / g.length
        if 0.0 <= tt <= 1.0:
            across = -ra * g.db + rb * g.da - g.axis_offset(tt)
            if abs(across) - g.half_width(tt) < 0.5:
                v -= 0.1 * (0.6 if g.kind == "covert" else 1.0)
                break
    return _lit(ctx, v)


def _lit(ctx: Ctx, v: float, key: str = "fire", glow: float = GLOW, smooth: bool = False):
    col = _pick_smooth(ctx.pal[key], v) if smooth else pick(ctx.pal[key], v)
    ctx.glow = shade(col, glow)
    return col


def _pick_smooth(r, v: float):
    """Wie pick(), aber zwischen zwei Stufen gemischt (für weiche Verläufe am Rumpf)."""
    v = max(0.0, min(0.9999, v)) * (len(r) - 1)
    i = int(v)
    return mix(r[i], r[min(i + 1, len(r) - 1)], v - i)


def _side(ctx: Ctx) -> int:
    return -1 if ctx.cube.bone.name.startswith("right") else 1


# ---------------------------------------------------------------------------- Federlisten (unten -> oben)

def _arm_feathers() -> list[Feather]:
    """Armschwinge (a = nach außen, b = nach hinten): Armschwingen, darüber eine Reihe großer Deckfedern."""
    out = []
    specs = [(11.3, 82, 8.1, -0.06), (9.1, 85, 7.6, 0.04), (6.9, 88, 8.3, -0.05), (4.7, 90, 8.0, 0.05),
             (2.5, 92, 8.8, -0.04), (0.6, 94, 9.4, 0.03)]
    for x, ang, length, curl in specs:                                  # vom Handgelenk zur Schulter (innen oben)
        out.append(Feather((x, -0.3), ang, length, 3.8, curl=curl, v0=0.24, v1=0.9))
    for i in range(6):
        k = 1.0 - i / 5
        x = 0.6 + k * 11.0
        out.append(Feather((x, 0.0), 92 - 8 * k, 5.0 - 0.4 * k, 3.0, kind="covert", v0=0.2, v1=0.6))
    return out


def _hand_feathers() -> list[Feather]:
    """Handschwinge (a = nach außen ab dem Handgelenk, b = nach hinten): sechs gespreizte Handschwingen (innen nach
    außen, die äußeren oben), darüber die Handdecken."""
    out = []
    specs = [(81, 8.2), (67, 8.8), (54, 9.3), (41, 9.7), (29, 10.0), (18, 10.2)]
    for i, (ang, length) in enumerate(specs):
        out.append(Feather((0.2 + i * 0.3, -0.4 + i * 0.05), ang, length, 3.3 - i * 0.12, curl=-0.07,
                           v0=0.24, v1=0.93))
    for i, (ang, length) in enumerate([(78, 4.6), (62, 4.8), (47, 5.0), (33, 5.0)]):
        out.append(Feather((-0.2 + i * 1.3, -0.2), ang, length, 2.9, kind="covert", v0=0.2, v1=0.6))
    return out


def _tail_fan() -> list[Feather]:
    """Schweif-Fächer (waagrecht, a = x, b = z): kurze Flammenfedern am Ansatz, darüber drei Schmuckfedern."""
    out = []
    for sx in (1, -1):
        out.append(Feather((0.8 * sx, 0.0), 90 - 46 * sx, 6.4, 3.0, kind="flame", v0=0.2, v1=0.9, wave=0.5 * sx))
        out.append(Feather((0.5 * sx, 0.2), 90 - 16 * sx, 8.0, 3.2, kind="flame", v0=0.2, v1=0.92, wave=-0.6 * sx))
    for sx in (1, -1):
        out.append(Feather((0.4 * sx, 0.6), 90 - 25 * sx, 13.2, 3.6, kind="plume", v0=0.22, v1=0.95, curl=0.04 * sx))
    out.append(Feather((0.0, 0.6), 90, 15.0, 3.9, kind="plume", v0=0.24, v1=0.98))
    return out


def _tail_veil() -> list[Feather]:
    """Senkrechte Schleppe (a = z nach hinten, b = nach oben): eine Flammenfeder, darüber die mittlere Schmuckfeder
    von der Seite, leicht gewellt."""
    return [Feather((0.4, -0.2), -8, 9.5, 3.0, kind="flame", v0=0.2, v1=0.9, wave=0.6),
            Feather((0.6, 0.4), 4, 14.8, 3.6, kind="plume", v0=0.24, v1=0.98, wave=0.6)]


def _crest() -> list[Feather]:
    """Federkrone (a = z nach hinten, b = nach oben): drei Flammenfedern vom Scheitel nach hinten oben."""
    return [Feather((-0.4, 0.0), 22, 7.4, 2.4, kind="flame", v0=0.3, v1=1.0, curl=-0.08, wave=0.4),
            Feather((-1.2, 0.0), 44, 9.0, 2.8, kind="flame", v0=0.3, v1=1.0, curl=-0.12, wave=0.5),
            Feather((-2.0, 0.0), 68, 7.6, 2.4, kind="flame", v0=0.3, v1=1.0, curl=-0.16, wave=0.4)]


ARM_FEATHERS = _arm_feathers()
HAND_FEATHERS = _hand_feathers()
TAIL_FAN = _tail_fan()
TAIL_VEIL = _tail_veil()
CREST = _crest()
CREST_Y = -3.5             # Scheitel (Kopf-Knochen), Ansatz der Krone


# =============================================================================================
# Geometrie
# =============================================================================================

def build() -> Model:
    m = Model("phoenix", 128, 96)
    m.tex_res = 2

    # ---------------------------------------------------------------- Rumpf
    m.bone("body", pivot=(0, 0, 0))
    m.cube("body", (-3, -2.1, -5), (6, 4.3, 10), "torso")
    m.cube("body", (-2.2, -2.75, -4.4), (4.4, 0.8, 8.6), "torso")              # Rückenwölbung
    m.cube("body", (-2.3, 2.0, -4.2), (4.6, 0.8, 7.4), "belly")               # Bauchwölbung
    m.cube("body", (-2.5, -1.3, -6.6), (5, 4.3, 2.6), "breast")               # weißglühende Brust
    m.cube("body", (-2.2, -1.8, 4.6), (4.4, 3.2, 2.4), "torso")               # Bürzel

    # ---------------------------------------------------------------- Kopf
    m.bone("head", "body", pivot=(0, -1, -5))
    m.cube("head", (-1.8, -2.4, -1.6), (3.6, 3.4, 2.4), "neck")               # Halskrause
    m.cube("head", (-2, -3.5, -4.4), (4, 4, 4), "head")
    m.cube("head", (-0.85, -2.3, -6.4), (1.7, 1.4, 2.2), "beak")              # Oberschnabel
    m.cube("head", (-0.55, -1.3, -7.0), (1.1, 1.3, 0.9), "beak")              # Haken
    m.cube("head", (-0.65, -0.95, -5.8), (1.3, 0.65, 1.5), "beak")            # Unterschnabel
    m.cube("head", (0, -12.4, -3.2), (0, 9.0, 12), "crest")                   # Federkrone (senkrecht)

    # ---------------------------------------------------------------- Flügel
    for side, sx in (("left", 1), ("right", -1)):
        w = f"{side}_wing"
        m.bone(w, "body", pivot=(3 * sx, -1, -1))
        _box(m, w, sx, -0.8, 5.2, (-1.15, 2.1), (-2.3, 3.6), "arm")            # Oberarm
        _box(m, w, sx, 4.8, ARM + 0.6, (-0.85, 1.6), (-1.9, 2.8), "arm")       # Unterarm
        _box(m, w, sx, 0.0, ARM, (-0.45, 0.9), (0.2, 2.6), "covert")           # kleine Deckfedern
        # Armschwingen (flach) – endet genau am Handgelenk, damit sie die Handschwingen nicht in derselben Ebene
        # überlappt (sonst Flimmern); die innersten Handschwingen decken den Rand
        _box(m, w, sx, -0.6, ARM, (0, 0), (-0.6, 10.2), "wing_plane")
        t = f"{side}_wing_tip"
        m.bone(t, w, pivot=(ARM * sx, 0, 0))
        _box(m, t, sx, -0.6, 4.6, (-0.75, 1.4), (-1.6, 2.4), "arm")            # Hand
        _box(m, t, sx, 4.2, 7.0, (-0.5, 1.0), (-1.25, 1.7), "arm")             # Daumenfittich
        _box(m, t, sx, 0.0, 12.2, (0, 0), (-1.8, 10.0), "hand_plane")          # Handschwingen (flach)

    # ---------------------------------------------------------------- Schweif
    m.bone("tail", "body", pivot=(0, 0, 5))
    m.cube("tail", (-1.6, -1.3, -0.4), (3.2, 2.1, 3.0), "rump")
    m.cube("tail", (-8.8, 0, 0), (17.6, 0, 16), "tail_fan")                    # Fächer (waagrecht)
    m.cube("tail", (0, -4.6, 0.4), (0, 7.4, 15.6), "tail_veil")                # Schleppe (senkrecht)
    return m


def _box(m: Model, bone: str, sx: int, x0: float, x1: float, yh, zd, mat: str) -> None:
    """Quader von x0 bis x1 nach außen (links +X, rechts -X gespiegelt); yh = (y, Höhe), zd = (z, Tiefe)."""
    ox = x0 if sx > 0 else -x1
    m.cube(bone, (ox, yh[0], zd[0]), (x1 - x0, yh[1], zd[1]), mat)


# =============================================================================================
# Bemalen
# =============================================================================================

def _plumage(ctx: Ctx, base: float, ca: float = 2.2, cb: float = 2.4, rim: float = 0.08, span: float = 0.06) -> float:
    """Gefieder wie Dachziegel: versetzte Reihen runder Federn, deren Spitzen nach hinten (+z) bzw. unten zeigen; am
    Federende ein heller Rand, dahinter ein feiner Schatten. Ergibt eine Helligkeit (0..1 der Feuer-Rampe)."""
    x, y, z = ctx.local
    face = ctx.face
    if face in ("top", "bottom"):
        a, b = x, z
    elif face in ("west", "east"):
        a, b = y, z
    else:
        a, b = x, y
    row = math.floor(b / cb)
    u = (a / ca + (0.5 if row % 2 else 0.0)) % 1.0
    w = (b / cb) % 1.0
    edge = 0.45 + 0.55 * math.sqrt(max(0.0, 1.0 - (2 * u - 1) ** 2))
    if w > edge:
        v = base - 0.05                                                  # Ansatz der nächsten Reihe (im Schatten)
    else:
        v = base + span * (w / edge) ** 2
        if edge - w < 0.2:
            v += rim                                                     # leuchtender Federrand
    return v + (noise3(x * 0.3, y * 0.3, z * 0.3, 7) - 0.5) * 0.05


def _torso(ctx: Ctx):
    """Rumpf: Rücken dunkles Karmesin (zur Mittellinie am dunkelsten), an den Flanken nach unten über Orange ins
    Goldgelbe."""
    x, y, z = ctx.local
    down = max(0.0, min(1.0, (y + 2.75) / 5.0))                          # 0 Rücken … 1 Bauch
    if ctx.face == "top":
        down = min(1.0, abs(x) / 3.0) * 0.18
    elif ctx.face == "bottom":
        down = 1.0
    base = 0.2 + 0.44 * down ** 1.2 + 0.05 * max(0.0, -z / 5.0) - 0.05 * max(0.0, z / 6.0)
    if ctx.face == "back":
        base = 0.24
    return _lit(ctx, _plumage(ctx, base), smooth=True)


def _belly(ctx: Ctx):
    return _lit(ctx, _plumage(ctx, 0.64 if ctx.face != "top" else 0.5), smooth=True)


def _breast(ctx: Ctx):
    """Weißglühende Brust: in der Mitte fast weiß, zu den Rändern goldgelb."""
    x, y, z = ctx.local
    r = min(1.0, math.hypot(x / 2.8, (y - 1.0) / 2.8))
    v = 0.98 - 0.32 * r ** 1.5
    if ctx.face == "top":
        v = 0.6
    elif ctx.face in ("west", "east"):
        v -= 0.08 + 0.1 * max(0.0, (z + 4.0) / 2.6)
    return _lit(ctx, _plumage(ctx, v - 0.04, ca=2.0, cb=1.8, rim=0.04, span=0.03), smooth=True)


def _neck(ctx: Ctx):
    """Halskrause: kleine Federn, oben karmesin, unten zur Brust hin hell."""
    x, y, z = ctx.local
    down = max(0.0, min(1.0, (y + 2.4) / 3.4))
    if ctx.face == "top":
        down = 0.0
    elif ctx.face == "bottom":
        down = 1.0
    return _lit(ctx, _plumage(ctx, 0.26 + 0.5 * down ** 1.2, ca=1.8, cb=1.6), smooth=True)


def _head(ctx: Ctx):
    """Kopf: Scheitel karmesin, Gesicht orange-golden, schmales weißglühendes Auge unter einer dunklen Braue, dahinter
    ein leuchtender Flammenstreif."""
    x, y, z = ctx.local
    face = ctx.face
    if face in ("west", "east"):
        ez, ey = z + 3.05, y + 1.95                                      # Augenmitte
        if abs(ey) < 0.5 and -0.75 < ez < 0.75:
            if abs(ez) < 0.26:
                ctx.glow = (70, 10, 12, 255)
                return pick(ctx.pal["eye"], 0.0)                         # Pupille
            col = pick(ctx.pal["eye"], 0.9 if abs(ez) < 0.5 else 0.5)
            ctx.glow = col
            return col
        if -0.95 < ey < -0.5 and -1.0 < ez < 1.2:
            return _lit(ctx, 0.08)                                       # Braue
        if 0.75 < ez < 3.4 and abs(ey + 0.15 * (ez - 0.75)) < 0.48 - 0.12 * (ez - 0.75):
            return _lit(ctx, 0.92 - 0.06 * ez)                           # Flammenstreif hinter dem Auge
        if abs(ey) < 0.65 and -1.0 < ez < 1.0:
            return _lit(ctx, 0.3)                                        # Augenring
    down = max(0.0, min(1.0, (y + 3.5) / 4.0))
    if face == "top":
        down = 0.0
    elif face == "bottom":
        down = 1.0
    base = 0.2 + 0.46 * down ** 1.2
    if face == "front":
        base = 0.46 + 0.26 * down
    return _lit(ctx, _plumage(ctx, base, ca=1.8, cb=1.6, rim=0.07), smooth=True)


def _beak(ctx: Ctx):
    """Hakenschnabel aus glühendem Gold: oben hell, zur Spitze heller, unten dunkler."""
    x, y, z = ctx.local
    tip = max(0.0, min(1.0, (-z - 4.4) / 2.6))
    v = 0.42 + 0.42 * tip
    if ctx.face == "top":
        v += 0.12
    elif ctx.face == "bottom":
        v -= 0.2
    if ctx.face in ("west", "east") and ctx.t == ctx.h - 1:
        v -= 0.15                                                        # Schnabelspalt
    return _lit(ctx, v, "gold", 0.8)


def _arm(ctx: Ctx):
    """Flügelbug: kleine karmesinrote Federn, oben zur Vorderkante hin heller (Lichtkante), Stirnseiten schlicht."""
    x, y, z = ctx.local
    oz, d = ctx.cube.origin[2], ctx.cube.size[2]
    front = 1.0 - (z - oz) / d                                           # 1 vorn … 0 hinten
    face = ctx.face
    if face in ("west", "east"):
        oy, h = ctx.cube.origin[1], ctx.cube.size[1]
        return _lit(ctx, 0.34 - 0.1 * (y - oy) / h + 0.06 * front, smooth=True)
    if face == "top":
        base = 0.26 + 0.18 * front
    elif face == "front":
        base = 0.4
    elif face == "bottom":
        base = 0.5
    else:
        base = 0.2
    return _lit(ctx, _plumage(ctx, base, ca=1.6, cb=1.6, rim=0.07), smooth=True)


def _covert(ctx: Ctx):
    """Kleine Deckfedern: Reihen kleiner Federn, nach hinten heller."""
    x, y, z = ctx.local
    oz, d = ctx.cube.origin[2], ctx.cube.size[2]
    k = max(0.0, min(1.0, (z - oz) / d))
    base = 0.26 + 0.14 * k
    if ctx.face == "bottom":
        base += 0.16
    elif ctx.face in ("back", "west", "east"):
        base = 0.36
    return _lit(ctx, _plumage(ctx, base, ca=1.6, cb=1.4, rim=0.08), smooth=True)


def _wing_plane(ctx: Ctx):
    x, y, z = ctx.local
    return _fan_color(ctx, ARM_FEATHERS, x * _side(ctx), z)


def _hand_plane(ctx: Ctx):
    x, y, z = ctx.local
    return _fan_color(ctx, HAND_FEATHERS, x * _side(ctx), z)


def _tail_fan_paint(ctx: Ctx):
    x, y, z = ctx.local
    return _fan_color(ctx, TAIL_FAN, x, z)


def _tail_veil_paint(ctx: Ctx):
    x, y, z = ctx.local
    return _fan_color(ctx, TAIL_VEIL, z, -y)


def _crest_paint(ctx: Ctx):
    x, y, z = ctx.local
    return _fan_color(ctx, CREST, z, CREST_Y - y)


def _rump(ctx: Ctx):
    return _lit(ctx, _plumage(ctx, 0.24 + (0.16 if ctx.face == "bottom" else 0.0)), smooth=True)


def materials(variant: str) -> dict:
    return {
        "torso": _torso,
        "belly": _belly,
        "breast": _breast,
        "neck": _neck,
        "head": _head,
        "beak": _beak,
        "crest": _crest_paint,
        "arm": _arm,
        "covert": _covert,
        "wing_plane": _wing_plane,
        "hand_plane": _hand_plane,
        "rump": _rump,
        "tail_fan": _tail_fan_paint,
        "tail_veil": _tail_veil_paint,
    }
