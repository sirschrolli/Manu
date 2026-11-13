"""
Glutgeist (statt Lohe) auf dem Skelett der Vanilla-Lohe: Kopf (8 Einheiten) und zwölf Stäbe in drei Ringen, die
BlazeModel um den Kopf kreisen lässt – deshalb heißen die Knochen wie bei Vanilla (head, part0 … part11). Eigenes
Aussehen:

  * der Kopf ist eine Maske aus geschwärztem Erz mit glühenden Nähten, schmalen, gleißenden Augenschlitzen unter einer
    schweren Braue und einem Gitter vor dem Mund, aus dem es glüht; oben lodert eine Krone aus Flammen, unten ein Bart
    aus Glut
  * unter dem Kopf hängt ein glühender Kern mit schwarzer Kruste und einer Flammenzunge (steht still, die Stäbe kreisen
    darum)
  * die Stäbe sind Glutklingen: geschwärzte Erzstäbe mit leuchtenden Runen, auf jeder eine kleine Flamme

Zwei Gestalten: Glutgeist (goldenes Feuer) und Seelengeist (türkises Seelenfeuer, in Festungen im Seelensandtal).
"""
from __future__ import annotations

import math

from ..geometry import Model
from ..paint import Ctx, hash01, hexc, mix, noise3

PALETTES = {
    "glut": {
        "metal": [hexc(c) for c in ("#1f1712", "#33251b", "#4a3424", "#634631", "#7f5b3e", "#9c7350")],
        "fire": [hexc(c) for c in ("#7a1d06", "#b8380c", "#e8611a", "#ff8f2b", "#ffbd4a", "#ffe58f", "#fff6d6")],
        "rune": hexc("#ffb02e")},
    "seele": {
        "metal": [hexc(c) for c in ("#151c1f", "#233136", "#33464c", "#456067", "#5a7a82", "#72959d")],
        "fire": [hexc(c) for c in ("#0b4a55", "#107783", "#17a7ad", "#35d4cf", "#74efe6", "#b9fff7", "#ecfffd")],
        "rune": hexc("#4fe7df")},
}


def build() -> Model:
    m = Model("blaze", 128, 64)
    m.tex_res = 2
    m.bone("head", pivot=(0, 0, 0))
    m.cube("head", (-4, -4, -4), (8, 8, 8), "mask")
    m.cube("head", (-4.5, -4.75, -4.5), (9, 1.5, 9), "brow")                  # Stirnreif
    m.cube("head", (-3.5, -8.5, -3.5), (7, 4, 7), "crown")                    # Flammenkrone
    m.cube("head", (-2.5, 4, -4.25), (5, 2.5, 3), "beard")                    # Glutbart unter dem Kinn
    m.cube("head", (-4.75, -1.5, -1.5), (0.75, 3, 3), "cheek")                # Wangenplatten
    m.cube("head", (4, -1.5, -1.5), (0.75, 3, 3), "cheek")
    m.bone("core", pivot=(0, 5, 0))                                           # steht still unter dem Kopf
    m.cube("core", (-2, 0, -2), (4, 6, 4), "core")
    m.cube("core", (-1.5, 6, -1.5), (3, 3, 3), "core")
    m.cube("core", (-1, 9, -1), (2, 4, 2), "tongue")
    for i, pivot in enumerate(_rod_positions()):
        name = f"part{i}"
        m.bone(name, pivot=pivot)                                         # Lage wie bei Vanilla (das Spiel bewegt sie)
        m.cube(name, (0, 0, 0), (2, 8, 2), "rod")
        m.cube(name, (0.5, -2.5, 0.5), (1, 2.5, 1), "flame")
    return m


def _rod_positions():
    """Die Lage der zwölf Stäbe wie in BlazeModel.createBodyLayer (drei Ringe)."""
    out = []
    angle = 0.0
    for i in range(4):
        out.append((math.cos(angle) * 9.0, -2.0 + math.cos(i * 2 * 0.25), math.sin(angle) * 9.0))
        angle += math.pi / 2
    angle = math.pi / 4
    for i in range(4, 8):
        out.append((math.cos(angle) * 7.0, 2.0 + math.cos(i * 2 * 0.25), math.sin(angle) * 7.0))
        angle += math.pi / 2
    angle = 0.47123894
    for i in range(8, 12):
        out.append((math.cos(angle) * 5.0, 11.0 + math.cos(i * 1.5 * 0.5), math.sin(angle) * 5.0))
        angle += math.pi / 2
    return out


def _fire(ctx: Ctx, k: float):
    f = ctx.pal["fire"]
    k = max(0.0, min(0.999, k))
    return f[int(k * len(f))]


def _metal(ctx: Ctx, k: float):
    m = ctx.pal["metal"]
    k = max(0.0, min(0.999, k))
    return m[int(k * len(m))]


def _glow(ctx: Ctx, c):
    ctx.glow = c
    return c


# =============================================================================================
# Kopf
# =============================================================================================

def _mask(ctx: Ctx):
    """Maske aus geschwärztem Erz: Platten mit glühenden Nähten, vorn Augenschlitze und das Mundgitter."""
    s, t, w, h = ctx.s, ctx.t, ctx.w, ctx.h                               # 16×16 Pixel je Seite
    face = ctx.face
    x, y, z = ctx.local
    n = noise3(x * 0.8, y * 0.8, z * 0.8, 3)
    base = _metal(ctx, 0.25 + n * 0.45 - (y + 4) * 0.02)
    if face == "front":
        # schmale, gleißende Augenschlitze, zur Mitte hin schräg nach unten (grimmig)
        for left, right, inner_right in ((2, 6, True), (9, 13, False)):
            if left <= s <= right:
                rel = (s - left) / (right - left)
                drop = rel if inner_right else 1.0 - rel
                row = 6 + round(drop * 1.2)
                if t == row:
                    return _glow(ctx, _fire(ctx, 0.85 + 0.1 * (1 - abs(rel - 0.5) * 2)))
                if t == row + 1 and 0.15 < rel < 0.85:
                    return _glow(ctx, _fire(ctx, 0.6))
                if t == row - 1:
                    return _metal(ctx, 0.05)                              # Schatten der Braue
        # Nasensteg
        if 7 <= s <= 8 and 6 <= t <= 10:
            return _metal(ctx, 0.75 if s == 7 else 0.55)
        # Mundgitter: vier glühende Schlitze
        if 4 <= s <= 11 and 11 <= t <= 13:
            if s in (4, 11) or t in (11, 13) and s % 2 == 0:
                return _metal(ctx, 0.1)
            if s % 2 == 1:
                return _glow(ctx, _fire(ctx, 0.55 + (t - 11) * 0.12))
            return _metal(ctx, 0.35)
        if 3 <= s <= 12 and t in (10, 14):
            return _metal(ctx, 0.12)
    # glühende Nähte zwischen den Platten
    seam = (face in ("front", "back") and s in (0, 15)) or (face in ("west", "east") and s in (0, 15) and t > 3)
    if face in ("west", "east") and t == 9 and 2 <= s <= 13:
        return _glow(ctx, _fire(ctx, 0.5 + hash01(s, t, face) * 0.2))
    if face == "back" and s in (5, 10) and t > 4:
        return _glow(ctx, _fire(ctx, 0.45))
    if face == "top" and (s == 7 or t == 7):
        return _glow(ctx, _fire(ctx, 0.55))
    if seam:
        return _metal(ctx, 0.08)
    # Nieten und Kanten
    if (s in (2, 13)) and (t in (2, 13)) and face != "top":
        return _metal(ctx, 0.95)
    if face == "bottom":
        return _glow(ctx, _fire(ctx, 0.35 + n * 0.3))                    # von unten glüht der Kern
    return base


def _brow(ctx: Ctx):
    """Stirnreif: dunkles Band mit Runen, vorn eine schwere Braue."""
    s, t = ctx.s, ctx.t
    if ctx.face in ("top", "bottom"):
        return _metal(ctx, 0.15 if ctx.face == "bottom" else 0.4)
    if t == 0:
        return _metal(ctx, 0.85)
    if hash01(s // 3, ctx.face, "rune") < 0.45 and s % 3 == 1:
        return _glow(ctx, ctx.pal["rune"])
    return _metal(ctx, 0.45 + (0.1 if s % 6 == 0 else 0.0))


def _flame_color(ctx: Ctx, k: float, x: float, z: float, seed: int):
    """Flamme: k = 0 unten (heiß) … 1 oben (Spitze); zerzauste Ränder, oben einzelne Zungen."""
    flick = noise3(x * 1.3, k * 3.0, z * 1.3, seed)
    edge = k + (flick - 0.5) * 0.55
    if edge > 0.92:
        return None
    return _glow(ctx, _fire(ctx, 0.98 - edge * 0.85))


def _crown(ctx: Ctx):
    """Flammenkrone: Zungen verschiedener Höhe lodern nach oben; von oben sieht man nur ihre Spitzen."""
    if ctx.face == "bottom":
        return None
    x, y, z = ctx.local
    if ctx.face == "top":
        r = math.sqrt(x * x + z * z) / 3.5
        if r < 0.45 and hash01(round(x * 2), round(z * 2), "krone") < 0.7:
            return _glow(ctx, _fire(ctx, 0.75 - r * 0.6))
        return None
    k = (y - (-4.5)) / -4.0                                              # 0 = Stirn, 1 = Oberkante
    along = x + z if ctx.face in ("front", "back") else z - x
    tongue = 0.5 + 0.5 * math.sin(along * 1.6 + (1.3 if ctx.face in ("east", "back") else 0.0))
    limit = 0.3 + 0.7 * tongue * (0.75 + noise3(x * 0.9, 0.0, z * 0.9, 9) * 0.5)
    if k > limit:
        return None
    return _glow(ctx, _fire(ctx, 0.97 - (k / max(limit, 0.05)) * 0.75))


def _beard(ctx: Ctx):
    """Glutbart: hängende Glut, unten in Strähnen."""
    x, y, z = ctx.local
    if ctx.face == "top":
        return None
    k = (y - 4) / 2.5
    if ctx.face == "bottom":
        return _glow(ctx, _fire(ctx, 0.4)) if hash01(round(x * 2), round(z * 2), "bart") < 0.6 else None
    strand = math.sin(x * 2.4) * 0.5 + 0.5
    if k > 0.55 and strand < 0.4:
        return None
    return _glow(ctx, _fire(ctx, 0.85 - k * 0.55 - strand * 0.1))


def _cheek(ctx: Ctx):
    s, t = ctx.s, ctx.t
    if ctx.face in ("east", "west") and (s in (0, ctx.w - 1) or t in (0, ctx.h - 1)):
        return _metal(ctx, 0.75)
    if ctx.face in ("east", "west") and hash01(s, t, "wange") < 0.15:
        return _glow(ctx, ctx.pal["rune"])
    return _metal(ctx, 0.3)


# =============================================================================================
# Kern und Stäbe
# =============================================================================================

def _core(ctx: Ctx):
    """Glühender Kern mit schwarzer Kruste: Krustenschollen auf weißglühendem Grund."""
    x, y, z = ctx.local
    n = noise3(x * 1.6 + 3, y * 1.6, z * 1.6, 11)
    if n > 0.66:
        return _metal(ctx, 0.12 + (n - 0.66) * 0.9)                       # Krustenscholle
    if n > 0.6:
        return _glow(ctx, _fire(ctx, 0.3))                                # glühender Rand der Scholle
    depth = (y - 0.0) / 9.0                                               # unten kühler
    return _glow(ctx, _fire(ctx, 0.92 - n * 0.45 - depth * 0.25))


def _tongue(ctx: Ctx):
    """Flammenzunge unter dem Kern, nach unten schmaler werdend."""
    x, y, z = ctx.local
    k = (y - 9) / 4.0
    if ctx.face == "top":
        return _glow(ctx, _fire(ctx, 0.8))
    if ctx.face == "bottom" or (k > 0.6 and ctx.s % 2 == (1 if k > 0.8 else 0)):
        return None
    return _glow(ctx, _fire(ctx, 0.8 - k * 0.55))


def _rod(ctx: Ctx):
    """Glutklinge: geschwärzter Erzstab mit einer feinen, glühenden Rune je Seite; unten glüht er aus."""
    s, t, h = ctx.s, ctx.t, ctx.h                                         # 4 × 16 Pixel je Seite
    if ctx.face == "top":
        return _metal(ctx, 0.7)
    if ctx.face == "bottom":
        return _glow(ctx, _fire(ctx, 0.6))
    i = int(ctx.cube.bone.name.removeprefix("part"))
    k = (t + 0.5) / h
    if k > 0.88:
        return _glow(ctx, _fire(ctx, 0.3 + (k - 0.88) * 4.0))            # ausglühendes Ende
    # Rune: eine glühende Linie mit Kerben, in der Mitte der Seite
    if 3 <= t <= h - 4:
        if s == 2 and hash01(i, ctx.face, t, "riss") < 0.82:
            return _glow(ctx, mix(ctx.pal["rune"], _fire(ctx, 0.3), 0.3 if t % 3 else 0.0))
        if s == 1 and t % 5 == 2:
            return _glow(ctx, mix(ctx.pal["rune"], _fire(ctx, 0.3), 0.4))
    if s == 0:
        return _metal(ctx, 0.7)                                           # Kante fängt das Licht
    if s == 3:
        return _metal(ctx, 0.12)
    return _metal(ctx, 0.3 + noise3(i, t * 0.4, s, 5) * 0.2)


def _flame(ctx: Ctx):
    x, y, z = ctx.local
    if ctx.face == "bottom":
        return None
    i = int(ctx.cube.bone.name.removeprefix("part"))
    k = (y - 0.0) / -2.5
    if ctx.face == "top":
        return _glow(ctx, _fire(ctx, 0.7)) if hash01(i, "spitze") < 0.7 else None
    return _flame_color(ctx, k, x + i, z, 31)


def materials(variant: str) -> dict:
    return {"mask": _mask, "brow": _brow, "crown": _crown, "beard": _beard, "cheek": _cheek, "core": _core,
            "tongue": _tongue, "rod": _rod, "flame": _flame}


def hidden(variant: str) -> list[str]:
    return []
