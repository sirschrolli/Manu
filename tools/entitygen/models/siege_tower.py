"""
Belagerungsturm: ein hoher Turm aus Balken auf vier Vollrädern. Vorn ist er mit genähten Rinderhäuten verkleidet (nasse
Häute brennen nicht), an den Seiten mit Brettern und schrägen Streben, hinten offen mit einer Leiter durch alle
Stockwerke. Oben eine Plattform mit Brustwehr und Zinnen aus Bohlen, an der hinteren Ecke ein Wimpel. Vorn oben hängt
die Fallbrücke: hochgezogen steht sie senkrecht vor der Öffnung, herabgelassen liegt sie waagerecht und reicht drei
Blöcke weit – auf der Unterseite Querleisten, oben Geländer, vorn eiserne Haken.

Der Turm wächst mit der Mauer, die er stürmen soll: Der Unterbau trägt die Plattform sieben Blöcke hoch, zwei
aufgesetzte Stockwerke (je drei Blöcke) heben sie auf zehn oder dreizehn Blöcke. Gebaut ist der höchste Turm; Java
blendet fehlende Stockwerke aus und setzt die Plattform je fehlendem Stockwerk 48 Pixel tiefer.

Knochen (Namen und Drehpunkte sind ein fester Vertrag mit SiegeModels.SiegeTower in Java):
  frame (0, 24, 0)         Fahrgestell und Unterbau (zwei Geschosse); y = 24 ist der Boden
  storey_a, storey_b       die aufgesetzten Stockwerke (Kinder von frame), sichtbar ab 10 bzw. 13 Blöcken Höhe
  top (0, TOP_Y, 0)        Plattform, Brustwehr, Wimpel (Kind von frame), gebaut für 13 Blöcke
  bridge (Kind von top)    Fallbrücke, Drehpunkt = Scharnier an der Vorderkante der Plattform; xRot 0 = hochgezogen,
                           +90° = herabgelassen (die Spitze zeigt nach vorn, -Z)
  wheel_fl/fr/bl/br        Räder (Kinder von root), Drehpunkt = Radmitte, rollen um X
Hilfsknochen brace_* (Streben) sind fest gedreht (nicht animieren).
"""
from __future__ import annotations

import math

from ..geometry import Model
from ..paint import Ctx, hash01, noise3, pick
from . import siege_common as sc
from .siege_common import box, lbox

PALETTES = {"siege_tower": sc.PALETTE}

HX = 22.0                            # halbe Breite (Außenkante der Eckständer)
FZ, BZ = -22.0, 22.0                 # Vorder- und Hinterkante
STOREY = 48.0                        # ein aufgesetztes Stockwerk (drei Blöcke)
MID_FLOOR = -40.0                    # Zwischenboden im Unterbau
BASE_TOP = -88.0                     # Oberkante der Plattform auf dem Unterbau (sieben Blöcke)
TOPS = (BASE_TOP, BASE_TOP - STOREY, BASE_TOP - 2 * STOREY)   # 7, 10, 13 Blöcke
TOP_Y = TOPS[-1]                     # so gebaut: der höchste Turm
PARAPET = 16.0                       # Höhe der Brustwehr
BRIDGE_LEN = 48.0                    # Länge der Fallbrücke (drei Blöcke)
BRIDGE_HALF = 14.0                   # halbe Breite der Brücke
HINGE = (0.0, TOP_Y, FZ - 1.0)       # Scharnier (Modell-Koordinaten)
WHEELS = {"wheel_fl": (24.0, 18.0, -15.0), "wheel_fr": (-24.0, 18.0, -15.0),
          "wheel_bl": (24.0, 18.0, 15.0), "wheel_br": (-24.0, 18.0, 15.0)}


def build() -> Model:
    m = Model("siege_tower", 1024, 1024)
    m.bone("frame", pivot=(0, 24, 0))
    f = "frame"

    # ------------------------------------------------------------------ Fahrgestell
    for sx in (1, -1):
        box(m, f, 18 * sx, 14, -25, 22 * sx, 17.5, 25, "beam")                        # Längsschwelle
        for z in (-15.0, 15.0):
            box(m, f, 18.5 * sx, 17.5, z - 2, 21.5 * sx, 20.5, z + 2, "dark_beam")    # Achsklotz
    for z0, z1 in ((-25.0, -21.5), (21.5, 25.0)):
        box(m, f, -22, 14, z0, 22, 17.5, z1, "beam")                                  # Querschwelle
    for z in (-15.0, 15.0):
        box(m, f, -23, 17, z - 1, 23, 19, z + 1, "axle")

    # ------------------------------------------------------------------ Unterbau und aufgesetzte Stockwerke
    _storey(m, f, 14.0, MID_FLOOR, 0)
    _storey(m, f, MID_FLOOR, BASE_TOP, 1)
    for k, name in enumerate(("storey_a", "storey_b")):
        ya, yb = TOPS[k], TOPS[k + 1]
        m.bone(name, f, pivot=sc.abs_to_local(m, f, 0.0, ya, 0.0))
        _storey(m, name, ya, yb, k + 2)

    # ------------------------------------------------------------------ Plattform (gebaut für den höchsten Turm)
    m.bone("top", f, pivot=sc.abs_to_local(m, f, 0.0, TOP_Y, 0.0))
    t = "top"
    # der Boden reicht bis an die Außenkante (die Brücke liegt davor)
    box(m, t, -HX, TOP_Y - 0.01, FZ - 0.5, HX, TOP_Y + 1.0, BZ, "floor")
    for sx in (1, -1):
        for z0, z1 in ((FZ, FZ + 3), (BZ - 3, BZ)):
            box(m, t, (HX - 3) * sx, TOP_Y - PARAPET, z0, HX * sx, TOP_Y, z1, "post")   # Eckständer in der Brustwehr
    # Brustwehr mit Zinnen (seitlich und hinten)
    for sx in (1, -1):
        box(m, t, (HX - 1.5) * sx, TOP_Y - PARAPET + 4, FZ + 3, HX * sx, TOP_Y, BZ - 3, "plank_z")
        z = FZ + 4.0
        while z + 4 <= BZ - 3:
            box(m, t, (HX - 1.5) * sx, TOP_Y - PARAPET, z, HX * sx, TOP_Y - PARAPET + 4, z + 4, "plank_z")
            z += 8.0
    box(m, t, -HX + 3, TOP_Y - PARAPET + 4, BZ - 1.5, HX - 3, TOP_Y, BZ, "plank_x")
    x = -HX + 4.0
    while x + 4 <= HX - 3:
        box(m, t, x, TOP_Y - PARAPET, BZ - 1.5, x + 4, TOP_Y - PARAPET + 4, BZ, "plank_x")
        x += 8.0
    # vorn: niedrige Wangen neben der Brücke, Seilrollen oben an den Eckständern
    for sx in (1, -1):
        box(m, t, BRIDGE_HALF * sx + 0.5 * sx, TOP_Y - 6, FZ, (HX - 3) * sx, TOP_Y, FZ + 1.5, "plank_x")
        box(m, t, (HX - 3.5) * sx, TOP_Y - PARAPET - 3, FZ - 1, (HX + 0.5) * sx, TOP_Y - PARAPET, FZ + 3.5, "dark_beam")
        box(m, t, (HX - 4.5) * sx, TOP_Y - PARAPET - 2.5, FZ - 2.5, (HX - 3.5) * sx, TOP_Y - PARAPET - 0.5, FZ - 0.5, "iron_plain")
    # Ausstieg der Leiter hinten
    for x in (-5.0, 5.0):
        box(m, t, x - 1, TOP_Y - 10, BZ - 4.5, x + 1, TOP_Y, BZ - 2.5, "shaft")
    # Wimpel an der hinteren linken Ecke
    box(m, t, HX - 2.2, TOP_Y - PARAPET - 30, BZ - 2.2, HX - 0.8, TOP_Y - PARAPET, BZ - 0.8, "shaft")
    box(m, t, HX - 1.5, TOP_Y - PARAPET - 29, BZ - 1.75, HX + 13, TOP_Y - PARAPET - 21, BZ - 1.25, "flag")

    # ------------------------------------------------------------------ Fallbrücke (hochgezogen gebaut)
    m.bone("bridge", t, pivot=sc.abs_to_local(m, t, *HINGE))
    br = "bridge"
    lbox(m, br, -BRIDGE_HALF, -BRIDGE_LEN, -2.5, BRIDGE_HALF, 0, 0, "deck")             # Bohlen
    for y in (-6.0, -22.0, -38.0):
        lbox(m, br, -BRIDGE_HALF + 1, y - 2, -4.0, BRIDGE_HALF - 1, y, -2.5, "beam_x")  # Querleisten (außen/unten)
    for sx in (1, -1):
        lbox(m, br, (BRIDGE_HALF - 1.5) * sx, -BRIDGE_LEN + 1, 0, BRIDGE_HALF * sx, -1, 4, "beam_y")   # Geländer
        for y in (-12.0, -36.0):
            lbox(m, br, (BRIDGE_HALF - 1.2) * sx, y - 1, 0, (BRIDGE_HALF - 0.3) * sx, y + 1, 4, "iron_plain")
    for x in (-10.0, -5.0, 0.0, 5.0, 10.0):
        lbox(m, br, x - 1, -BRIDGE_LEN - 3, -2.0, x + 1, -BRIDGE_LEN, -0.5, "iron")       # Haken vorn
    lbox(m, br, -BRIDGE_HALF, -2, -2.8, BRIDGE_HALF, 0, 0.3, "iron_plain")               # Scharnierband

    # ------------------------------------------------------------------ Räder
    for name, (x, y, z) in WHEELS.items():
        m.bone(name, pivot=(x, y, z))
        sc.wheel(m, name, 12, -1.5, 1.5, "wheel12")
        sc.hub(m, name, 1 if x > 0 else -1, 1.5)
    return m


def _storey(m: Model, bone: str, ya: float, yb: float, k: int) -> None:
    """
    Ein Geschoss von {@code ya} (unten) bis {@code yb} (oben, kleiner): Ständer, Rähm und Boden oben, seitlich Bretter
    mit Schießscharte und Strebe, vorn die Häute, hinten die Leiter.
    """
    span = ya - yb
    # Ständer
    for sx in (1, -1):
        for z0, z1 in ((FZ, FZ + 3), (BZ - 3, BZ)):
            box(m, bone, (HX - 3) * sx, yb, z0, HX * sx, ya, z1, "post")               # Eckständer
        box(m, bone, (HX - 3) * sx, yb, -1.5, HX * sx, ya, 1.5, "post")                # Mittelständer der Seiten
    for x0, x1 in ((-9.5, -6.5), (6.5, 9.5)):
        box(m, bone, x0, yb, BZ - 3, x1, ya, BZ, "post")                               # Ständer hinten (Leiterfeld)
    # Rähm und Boden oben
    for sx in (1, -1):
        box(m, bone, (HX - 3) * sx, yb, FZ, HX * sx, yb + 3.5, BZ, "beam_z")
    for z0, z1 in ((FZ, FZ + 3), (BZ - 3, BZ)):
        box(m, bone, -HX + 3, yb, z0, HX - 3, yb + 3.5, z1, "beam_x")
    box(m, bone, -HX + 3, yb, FZ + 3, HX - 3, yb + 2, BZ - 3, "floor")
    # Seiten: Bretter, Schießscharte, Strebe
    for sx, side in ((1, "l"), (-1, "r")):
        box(m, bone, HX * sx, yb, FZ + 0.5, (HX + 0.6) * sx, ya, BZ - 0.5, "plank_y")
        for z in (-10.0, 10.0):
            box(m, bone, (HX + 0.6) * sx, (ya + yb) / 2 - 4, z - 0.75, (HX + 0.7) * sx, (ya + yb) / 2 + 4, z + 0.75, "slit")
        a = ((HX + 1.3) * sx, ya - 1.0, FZ + 3.0 if k % 2 == 0 else BZ - 3.0)
        b = ((HX + 1.3) * sx, yb + 3.0, BZ - 3.0 if k % 2 == 0 else FZ + 3.0)
        sc.strut(m, f"brace_{side}{k}", bone, a, b, 2.0, "beam", 0.5)
    # vorn: Rinderhäute in Reihen (leicht versetzt), Querleisten über den Stößen
    rows = max(2, round(span / 25.0))
    for r in range(rows):
        y1 = ya - r * span / rows
        y0 = max(yb, y1 - span / rows - 1.2)
        for sx in (1, -1):
            xa, xb = (0.0, HX) if sx > 0 else (-HX, 0.0)
            depth = 0.6 if (r + k + (sx > 0)) % 2 == 0 else 1.1
            box(m, bone, xa - 0.4, y0, FZ - depth, xb + 0.4, y1, FZ - depth + 0.6, "hide")
    for r in range(1, rows):
        y = ya - r * span / rows
        box(m, bone, -HX - 0.5, y - 1.0, FZ - 1.7, HX + 0.5, y + 0.5, FZ - 1.1, "beam_x")
    box(m, bone, -HX - 0.5, yb, FZ - 1.7, HX + 0.5, yb + 1.5, FZ - 1.1, "beam_x")
    # hinten: Leiter
    for x in (-5.0, 5.0):
        box(m, bone, x - 1, yb, BZ - 4.5, x + 1, ya, BZ - 2.5, "shaft")
    y = ya - 2.0
    while y > yb + 2:
        box(m, bone, -4, y - 1, BZ - 4.2, 4, y, BZ - 2.8, "rung")
        y -= 6.0


# =============================================================================================
# Malen
# =============================================================================================

def _hide(ctx: Ctx):
    """Rinderhaut: weich gefleckt, je Haut ein eigener Ton, dunkle nasse Flecken, Nähte und Nägel am Rand."""
    x, y, z = ctx.local
    c = sc.cid(ctx.cube)
    u0, u1, v0, v1 = sc.face_edge(ctx)
    v = 0.58 + ((c % 11) / 10 - 0.5) * 0.18
    v += (noise3(x * 0.3, y * 0.3, z * 0.3, 41) - 0.5) * 0.18
    wet = noise3(x * 0.12 + c * 0.01, y * 0.12, z * 0.12, 42)
    if wet > 0.62:
        v -= 0.12 + (wet - 0.62) * 0.5
    edge = min(u0, u1, v0, v1)
    if edge < 0.8:
        v -= 0.1
    elif edge < 1.8 and ctx.face in ("front", "back") and math.floor(u0 + v0) % 3 == 0:
        return sc.rivet(ctx, 0.64)
    return pick(sc.PALETTE["hide"], v)


_FLOOR = sc.planks(0, 4.0, 0.52)
_DECK = sc.planks(1, 4.0, 0.56)


def _floor(ctx: Ctx):
    return _FLOOR(ctx)


def _deck(ctx: Ctx):
    """Brückenbohlen: längs, hell abgetreten auf der Lauffläche (Innenseite, im hochgezogenen Zustand +Z)."""
    col = _DECK(ctx)
    return col


def _slit(ctx: Ctx):
    """Schießscharte: ein dunkler Spalt."""
    return pick(sc.PALETTE["end"], 0.05)


_RUNG = sc.wood(0, base=0.6, knots=False)


def _rung(ctx: Ctx):
    return _RUNG(ctx)


def _flag(ctx: Ctx):
    """Wimpel: rot gefärbtes Tuch mit hellem Saum, leicht gewellt."""
    x, y, z = ctx.local
    u0, u1, v0, v1 = sc.face_edge(ctx)
    if min(v0, v1) < 0.8:
        return pick(sc.PALETTE["linen"], 0.7)
    wave = math.sin(x * 0.7) * 0.08
    v = 0.55 + wave + (hash01(ctx.u, ctx.v, 5) - 0.5) * 0.06
    return pick(sc.PALETTE["dye"], v)


def materials(variant: str) -> dict:
    mats = sc.materials()
    mats.update({"hide": _hide, "floor": _floor, "deck": _deck, "slit": _slit, "rung": _rung, "flag": _flag})
    return mats
