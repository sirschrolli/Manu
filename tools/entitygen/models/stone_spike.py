"""
Steinstachel – Zauber des Erdelements: ein zerklüfteter Felsdorn bricht aus dem Boden (ca. 40 hoch, am Fuß 10 × 10).
Boden-Regel: y = 24 ist der Boden, oben ist -y.

Knochen:
  * spike    Drehpunkt (0, 24, 0) auf dem Boden: fünf gestapelte, gegeneinander versetzte und dünner werdende
             Felsblöcke (jeder aus zwei verschobenen Quadern, dadurch unregelmäßig) mit Felsnasen und eine scharfe
             Spitze (spitz zulaufend bemalt). Die Blöcke beginnen etwas unter dem Boden, damit beim Hochfahren
             keine Lücke bleibt.
  * rubble   Drehpunkt (0, 24, 0): sechs Brocken aufgebrochenen Gesteins rund um den Fuß, halb im Boden.

Bemalung (gemeinsam mit der Steinfaust, siehe ``stone``): geschichteter Tuff und Sandstein in Braun- und Grautönen
mit dem warmen Erd-Ton #C8A070, große ruhige Flecken statt Gesprenkel, dunkle Risse, oben/links hellere Kanten, unten
dunkler (Erde am Fuß), zur Spitze heller. Dünne bernsteinfarben glühende Risse laufen den Dorn hinauf (Leuchtbild).
"""
from __future__ import annotations

import math

from ..geometry import Model
from ..paint import Ctx, Voronoi3D, hash01, hexc, mix, noise3, pick

PALETTES = {
    "stone_spike": {
        "stone": [hexc(c) for c in ("#231d19", "#332a24", "#463a31", "#5a4b3f", "#6f5e4e", "#86735f", "#9d8a72", "#b4a085",
                                    "#cab699", "#ddcdb2")],
        "sand": [hexc(c) for c in ("#4a3622", "#6a4e30", "#8c6c44", "#ab8858", "#c8a070", "#dcbc8c", "#ecd4a8")],
        "dirt": [hexc(c) for c in ("#1e150e", "#2e2016", "#40301f", "#55402b")],
        "amber": [hexc(c) for c in ("#4a1c02", "#8a3a04", "#c8640c", "#f09420", "#ffc04a", "#ffe490", "#fff8dc")],
    },
}

# Felsblöcke von unten nach oben: (Breite x, Tiefe z, Mitte x, Mitte z, y unten, y oben)
SEGMENTS = [
    (10.0, 10.0, 0.0, 0.0, 1.6, -9.5),
    (8.4, 8.8, 0.9, -0.5, -8.6, -17.5),
    (6.8, 7.0, 1.9, -0.4, -16.6, -24.5),
    (5.2, 5.4, 2.4, 0.6, -23.6, -30.0),
    (3.8, 3.9, 1.9, 1.3, -29.2, -34.2),
]
# Spitze: schmale Stufen, die letzte läuft gemalt spitz zu: (Breite, Mitte x, Mitte z, y unten, y oben)
TIP = [(2.7, 1.5, 1.6, -33.6, -36.6), (1.9, 1.2, 1.8, -36.3, -38.8), (1.2, 1.0, 1.9, -38.5, -40.6),
       (0.7, 0.9, 2.0, -40.4, -42.6)]
HEIGHT = 42.6


def build() -> Model:
    m = Model("stone_spike", 128, 80)
    m.tex_res = 2
    m.bone("spike", pivot=(0, 24, 0))
    for i, (w, d, cx, cz, y0, y1) in enumerate(SEGMENTS):
        h = y0 - y1
        # zwei gegeneinander verschobene Quader: unregelmäßiger, kantiger Querschnitt
        m.cube("spike", (cx - w / 2, y1, cz - d / 2 + 0.4), (w, h, d - 0.8), "rock")
        m.cube("spike", (cx - w / 2 + 0.5 + 0.2 * (i % 2), y1 - 0.4 * (i % 2 == 0), cz - d / 2),
               (w - 1.0, h - 0.6, d), "rock")
    for i, (w, cx, cz, y0, y1) in enumerate(TIP):
        m.cube("spike", (cx - w / 2, y1, cz - w / 2), (w, y0 - y1, w), "tip" if i == len(TIP) - 1 else "rock")
    # Felsnasen und Absätze (rau, schief)
    for (x, y, z, sw, sh, sd) in [(-6.0, -6.5, -2.0, 1.6, 3.0, 4.2), (4.6, -14.5, 1.0, 1.4, 2.6, 3.4),
                                  (-2.6, -20.6, 2.0, 1.6, 2.4, 2.8), (2.0, -12.8, -5.6, 3.2, 2.2, 1.2),
                                  (4.4, -27.0, -0.6, 1.2, 2.0, 2.2), (-1.4, -4.0, 5.0, 3.6, 2.6, 1.4),
                                  (0.4, -31.4, 2.6, 1.4, 1.8, 1.4)]:
        m.cube("spike", (x, y, z), (sw, sh, sd), "rock")

    m.bone("rubble", pivot=(0, 24, 0))
    for (x, z, sw, sh, sd, top) in [(-9.5, -3.0, 3.4, 2.4, 3.0, 1.6), (7.0, -7.0, 2.6, 1.8, 2.8, 0.0),
                                    (8.4, 3.5, 3.8, 2.8, 3.2, 1.4), (-2.5, 8.2, 3.0, 1.6, 2.4, 0.0),
                                    (-7.6, 6.4, 2.2, 1.4, 2.0, 0.0), (2.6, -9.6, 2.8, 2.2, 2.2, 1.0)]:
        m.cube("rubble", (x, -sh + 0.6, z), (sw, sh, sd), "rubble")
        if top:
            m.cube("rubble", (x + 0.5, -sh - top + 0.7, z + 0.4), (sw - 1.2, top, sd - 1.0), "rubble")
    return m


# =============================================================================================
# Bemalen (gemeinsam mit der Steinfaust)
# =============================================================================================

_CRACKS = Voronoi3D(5.5, 211)
_LIT = {"top": 0.14, "west": 0.04, "front": 0.02, "east": -0.04, "back": -0.06, "bottom": -0.16}


def face_pos(ctx: Ctx) -> tuple[float, float]:
    """(0..1 waagrecht über die Fläche, 0..1 von oben nach unten) bezogen auf die Pixelmitte."""
    return (ctx.s + 0.5) / ctx.w, (ctx.t + 0.5) / ctx.h


def stone(ctx: Ctx, height_frac: float, foot: float = 0.0, seed: int = 0, warm: float = 1.0, edge: float = 1.0):
    """Geschichtetes Gestein: Schichten aus grauem Tuff und warmem Sandstein (leicht gewellt), große ruhige Flecken,
    wenige dunkle Risse, helle Ober-/Linkskanten und dunklere Unterkanten jeder Fläche, oben (Spitze) heller,
    am Fuß (``foot`` 0..1) dunkel und erdig. -> Farbe."""
    x, y, z = ctx.world
    face = ctx.lface or ctx.face
    n = noise3(x * 0.18, y * 0.18, z * 0.18, 201 + seed)
    # Schichten: wellige Bänder entlang der Höhe
    wob = noise3(x * 0.12, 0.0, z * 0.12, 203 + seed) * 2.6
    band = math.floor((y + wob) / 2.4)
    kind = hash01(band, 205 + seed)
    v = 0.42 + _LIT[face] + (n - 0.5) * 0.18 + 0.2 * height_frac + (kind - 0.5) * 0.1
    use_sand = kind > 0.62 and warm > 0
    # Kanten: oben/links hell, unten/rechts dunkler (Licht von oben links)
    if edge and face not in ("top", "bottom"):
        if ctx.t == 0:
            v += 0.1
        elif ctx.t == ctx.h - 1:
            v -= 0.1
        if ctx.s == 0:
            v += 0.04
        elif ctx.s == ctx.w - 1:
            v -= 0.06
    elif edge and face == "top" and (ctx.s == 0 or ctx.t == 0 or ctx.s == ctx.w - 1 or ctx.t == ctx.h - 1):
        v -= 0.05
    # Schichtfuge: feine dunkle Linie an der Bandgrenze
    frac = ((y + wob) / 2.4) % 1.0
    if face not in ("top", "bottom") and frac < 0.09:
        v -= 0.1
    # Risse (Zellränder), nur wenige und fein
    val, cedge = _CRACKS(x + seed * 2.0, y * 0.8, z)
    if cedge < 0.06 and face != "bottom":
        v -= 0.17
    # Fuß: dunkler, erdig
    v -= 0.22 * foot
    col = pick(ctx.pal["sand"], v + 0.05) if use_sand else pick(ctx.pal["stone"], v)
    if foot > 0.35:
        col = mix(col, pick(ctx.pal["dirt"], 0.3 + n * 0.5), min(1.0, (foot - 0.35) * 1.6))
    return col


def amber_crack(ctx: Ctx, u: float, center: float, width: float = 0.07):
    """Glühende Risslinie bei waagrechter Lage ``center`` (0..1 über die Fläche): hell in der Mitte, orange am Rand.
    -> Farbe (und Leuchten) oder None."""
    d = abs(u - center)
    px = width * max(1, ctx.w) / 2                                       # halbe Breite in Pixeln
    dpx = d * ctx.w
    if dpx > px + 0.5:
        return None
    k = 1.0 - min(1.0, dpx / (px + 0.5))
    col = pick(ctx.pal["amber"], 0.4 + 0.55 * k)
    ctx.glow = col
    return col


def _crack_center(y: float, phase: float, base: float) -> float:
    """Verlauf eines Risses über die Höhe (gezackt, 0..1 über die Fläche)."""
    return base + 0.16 * math.sin(y * 0.33 + phase) + 0.07 * math.sin(y * 1.1 + phase * 2.3) + \
        0.05 * (1 if math.floor(y / 1.5 + phase) % 2 else -1)


# Rissbahnen: (Fläche, Lage, Phase, bis Höhe)
_GLOW_CRACKS = [("front", 0.42, 0.0, 36.0), ("east", 0.55, 1.7, 31.0), ("west", 0.38, 3.1, 26.0), ("back", 0.6, 4.4, 38.0)]


def _height_frac(ctx: Ctx) -> float:
    return max(0.0, min(1.0, -ctx.local[1] / HEIGHT))


def _rock(ctx: Ctx):
    x, y, z = ctx.local
    hf = _height_frac(ctx)
    foot = max(0.0, min(1.0, (y + 3.5) / 4.0))
    face = ctx.lface
    if face not in ("top", "bottom") and ctx.cube.size[1] > 5:
        u, w = face_pos(ctx)
        for f, base, phase, top in _GLOW_CRACKS:
            if f == face and -y < top and y < 0.2:
                c = amber_crack(ctx, u, _crack_center(y, phase, base), 0.07 + 0.05 * (1 - hf))
                if c is not None:
                    return c
    if face == "top":
        return stone(ctx, hf + 0.15, 0.0)                                # Absätze: verwittert hell
    return stone(ctx, hf, foot)


def _tip(ctx: Ctx):
    """Spitze: läuft gemalt spitz zu (wie die Kristallnadeln), hell verwittert, ein Riss glüht bis fast zur Spitze."""
    x, y, z = ctx.local
    w, h, d = ctx.cube.size
    ox, oy, oz = ctx.cube.origin
    f = max(0.0, min(1.0, (oy + h - y) / h))                             # 0 unten … 1 oben
    face = ctx.lface
    if face in ("top",):
        return None
    if face in ("west", "east"):
        across = abs((z - oz) / d - 0.5) * 2
    elif face in ("front", "back"):
        across = abs((x - ox) / w - 0.5) * 2
    else:
        across = 0.0
    if across > (1.0 - f) ** 0.85 + 0.04:
        return None
    return stone(ctx, _height_frac(ctx), 0.0, edge=0.0)


def _rubble(ctx: Ctx):
    x, y, z = ctx.local
    foot = max(0.0, min(1.0, (y + 0.4) / 1.2))
    col = stone(ctx, 0.12, foot * 0.8, seed=3)
    # vereinzelt glimmt ein Splitter
    if ctx.lface in ("top", "west", "front") and hash01(round(x * 2), round(y * 2), round(z * 2), 209) > 0.965:
        col = pick(ctx.pal["amber"], 0.6)
        ctx.glow = col
    return col


def materials(variant: str) -> dict:
    return {"rock": _rock, "tip": _tip, "rubble": _rubble}
