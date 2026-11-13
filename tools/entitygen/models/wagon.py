"""
Planwagen: ein Kastenwagen auf vier Speichen-Vollrädern mit einer Plane aus Leinen über drei Bögen, vorn ein Kutschbock
mit Lehne und eine Deichsel mit Ortscheit, hinten offen mit einer Ladeklappe. Im Wagen Kisten, Fässer und Säcke (nur zu
sehen, wenn etwas geladen ist), vorn am ersten Bogen eine Laterne.

Knochen (Namen und Drehpunkte sind ein fester Vertrag mit SiegeModels.Wagon in Java):
  frame (0, 24, 0)         Fahrgestell, Kasten, Plane, Bock, Deichsel; y = 24 ist der Boden
  cargo (Kind von frame)   die Ladung, Stufe 1 (Kiste und Fässer hinten)
  cargo_2, cargo_3         mehr Ladung (Kinder von cargo): Säcke und Stroh, dann bis unter die Plane
  wheel_fl/fr/bl/br        Räder (Kinder von root), Drehpunkt = Radmitte, rollen um X
Hilfsknochen cover_l/cover_r (die schrägen Planenteile) sind fest gedreht (nicht animieren).
"""
from __future__ import annotations

import math

from ..geometry import Model
from ..paint import Ctx, hash01, noise3, pick
from . import siege_common as sc
from .siege_common import box, lbox

PALETTES = {"wagon": sc.PALETTE}

HX = 13.0                    # halbe Breite des Kastens
FZ, BZ = -22.0, 26.0         # Vorder- und Hinterkante des Kastens
FLOOR_Y = 11.0               # Oberkante des Wagenbodens
SIDE_TOP = 3.0               # Oberkante der Seitenbretter
COVER_TOP = -17.0            # Scheitel der Plane
WHEELS = {"wheel_fl": (15.0, 18.0, -14.0), "wheel_fr": (-15.0, 18.0, -14.0),
          "wheel_bl": (15.0, 18.0, 17.0), "wheel_br": (-15.0, 18.0, 17.0)}
BOWS = (-19.0, 2.0, 23.0)    # Bögen der Plane


def build() -> Model:
    m = Model("wagon", 512, 512)
    m.bone("frame", pivot=(0, 24, 0))
    f = "frame"

    # ------------------------------------------------------------------ Fahrgestell
    for sx in (1, -1):
        box(m, f, 7 * sx, FLOOR_Y + 2, -27, 10.5 * sx, FLOOR_Y + 5.5, 28, "beam")         # Langbaum
        for z in (-14.0, 17.0):
            box(m, f, 9.5 * sx, FLOOR_Y + 5.5, z - 2, 12.5 * sx, 18.5, z + 2, "dark_beam")  # Achsstock
    for z in (-14.0, 17.0):
        box(m, f, -14, 17, z - 1, 14, 19, z + 1, "axle")
        box(m, f, -12.5, FLOOR_Y + 5.5, z - 1.5, 12.5, FLOOR_Y + 7.5, z + 1.5, "dark_beam")     # Querschemel

    # ------------------------------------------------------------------ Kasten
    box(m, f, -HX, FLOOR_Y, FZ, HX, FLOOR_Y + 2, BZ, "floor")                                   # Boden
    for sx in (1, -1):
        box(m, f, (HX - 1) * sx, SIDE_TOP, FZ, HX * sx, FLOOR_Y, BZ, "plank_z")                 # Seitenbretter
        box(m, f, (HX - 1.5) * sx, SIDE_TOP - 1.5, FZ - 0.5, (HX + 0.5) * sx, SIDE_TOP, BZ + 0.5, "beam_z")   # Oberleiste
        for z in (FZ + 1.0, -6.0, 10.0, BZ - 1.0):
            box(m, f, HX * sx, SIDE_TOP - 1.0, z - 1, (HX + 1.2) * sx, FLOOR_Y + 2, z + 1, "beam_y")       # Rungen
    box(m, f, -HX + 1, SIDE_TOP - 4, FZ, HX - 1, FLOOR_Y, FZ + 1, "plank_x")                    # Stirnbrett (vorn)
    box(m, f, -HX + 1, SIDE_TOP + 2, BZ - 1, HX - 1, FLOOR_Y, BZ, "plank_x")                    # Ladeklappe (hinten)
    for sx in (1, -1):
        box(m, f, (HX - 3) * sx, SIDE_TOP + 1, BZ, (HX - 1.5) * sx, SIDE_TOP + 3, BZ + 0.6, "iron_plain")   # Klappenhaken

    # ------------------------------------------------------------------ Kutschbock und Deichsel
    box(m, f, -HX + 0.5, SIDE_TOP - 3.5, FZ - 6, HX - 0.5, SIDE_TOP - 1.5, FZ + 1, "beam_x")     # Sitzbrett
    for sx in (1, -1):
        box(m, f, (HX - 2.5) * sx, SIDE_TOP - 1.5, FZ - 5.5, (HX - 0.5) * sx, FLOOR_Y, FZ - 3.5, "post")   # Stützen
    box(m, f, -HX + 1.5, SIDE_TOP - 9, FZ + 0.2, HX - 1.5, SIDE_TOP - 3.5, FZ + 1.4, "plank_x")      # Lehne
    box(m, f, -HX, SIDE_TOP - 5, FZ - 6.5, HX, SIDE_TOP - 3.5, FZ - 6, "beam_x")                  # Fußbrett vorn
    for sx in (1, -1):
        box(m, f, 2.0 * sx, 16.5, -50.0, 4.0 * sx, 18.5, -14.0, "shaft")                         # Deichselbäume
    box(m, f, -4.5, 16.0, -36.5, 4.5, 19.0, -34.5, "iron_plain")                                  # Eisenband
    box(m, f, -10, 16.5, -51.5, 10, 18.5, -49.5, "beam_x")                                        # Ortscheit
    for sx in (1, -1):
        box(m, f, 9 * sx, 16.0, -52, 10.5 * sx, 19.0, -49, "iron_plain")                         # Zughaken

    # ------------------------------------------------------------------ Plane über drei Bögen
    shoulder = SIDE_TOP - 9.0                     # bis hier stehen die Planenseiten senkrecht
    for z in BOWS:
        for sx in (1, -1):
            box(m, f, (HX - 0.5) * sx, shoulder, z - 0.75, (HX + 0.7) * sx, SIDE_TOP, z + 0.75, "bow")
        box(m, f, -HX + 4, COVER_TOP - 0.6, z - 0.75, HX - 4, COVER_TOP + 0.8, z + 0.75, "bow")
    for sx in (1, -1):
        box(m, f, (HX - 0.2) * sx, shoulder, FZ + 1.5, (HX + 0.5) * sx, SIDE_TOP - 1.4, BZ - 1.0, "canvas")   # Seite
    box(m, f, -HX + 4.5, COVER_TOP - 0.3, FZ + 1.5, HX - 4.5, COVER_TOP + 0.4, BZ - 1.0, "canvas")     # Scheitel
    # die schrägen Teile zwischen Seite und Scheitel als fest gedrehte Hilfsknochen
    dx = (HX - 4.5) - HX
    dy = COVER_TOP - shoulder
    length = math.sqrt(dx * dx + dy * dy) + 0.6
    angle = math.degrees(math.atan2(-dx, -dy))
    for sx, side in ((1, "l"), (-1, "r")):
        m.bone(f"cover_{side}", f, pivot=sc.abs_to_local(m, f, (HX + 0.3) * sx, shoulder, 0.0), rotation=(0.0, 0.0, -angle * sx))
        lbox(m, f"cover_{side}", -0.35, -length, FZ + 1.5, 0.35, 0.2, BZ - 1.0, "canvas")
        for z in BOWS:
            lbox(m, f"cover_{side}", -0.6, -length, z - 0.75, 0.6, 0.2, z + 0.75, "bow")
    # Laterne vorn am ersten Bogen
    box(m, f, -1.2, COVER_TOP + 0.8, BOWS[0] - 2.2, 1.2, COVER_TOP + 1.6, BOWS[0] - 0.8, "iron_plain")
    box(m, f, -1.6, COVER_TOP + 1.6, BOWS[0] - 2.6, 1.6, COVER_TOP + 5.0, BOWS[0] - 0.4, "lantern")

    # ------------------------------------------------------------------ Ladung (drei Stufen, je nachdem wie voll der Laderaum ist)
    m.bone("cargo", f, pivot=sc.abs_to_local(m, f, 0.0, FLOOR_Y, 4.0))
    m.bone("cargo_2", "cargo", pivot=(0.0, 0.0, 0.0))
    m.bone("cargo_3", "cargo", pivot=(0.0, 0.0, 0.0))
    cg, c2, c3 = "cargo", "cargo_2", "cargo_3"
    # Stufe 1: hinten eine große Kiste und zwei Fässer
    lbox(m, cg, -10, -8, 8, -2, 0, 16, "crate")                       # Kiste hinten links
    for (cx, cz) in ((6.0, 12.0), (6.0, 4.5)):                        # zwei Fässer rechts
        lbox(m, cg, cx - 3.5, -9, cz - 2.5, cx + 3.5, 0, cz + 2.5, "barrel")
        lbox(m, cg, cx - 2.5, -9, cz - 3.5, cx + 2.5, 0, cz + 3.5, "barrel")
    # Stufe 2: eine kleine Kiste obenauf, Säcke in der Mitte, vorn ein Strohballen
    lbox(m, c2, -9, -14, 9, -3, -8, 15, "crate")                      # kleine Kiste auf der großen
    for (cx, cz) in ((-6.0, -2.0), (-1.0, -8.0), (5.0, -6.0)):        # Säcke
        lbox(m, c2, cx - 3, -5, cz - 2.5, cx + 3, 0, cz + 2.5, "sack")
        lbox(m, c2, cx - 2, -6, cz - 1.5, cx + 2, -5, cz + 1.5, "sack")
    lbox(m, c2, -11, -7, -23, -2, 0, -14, "hay")                      # Strohballen vorn links
    # Stufe 3 (voll): eine Kiste quer über den Fässern, noch mehr Säcke, zwei weitere Ballen
    lbox(m, c3, 1.5, -17, 1.5, 10.5, -9, 15.0, "crate")
    for (cx, cz) in ((-4.0, -5.0), (2.5, -4.5)):
        lbox(m, c3, cx - 3, -11, cz - 2.5, cx + 3, -6, cz + 2.5, "sack")
        lbox(m, c3, cx - 2, -12, cz - 1.5, cx + 2, -11, cz + 1.5, "sack")
    lbox(m, c3, 2, -8, -23, 11, 0, -14, "hay")                        # Strohballen vorn rechts
    lbox(m, c3, -10, -14, -22, -3, -7, -15, "hay")                    # Ballen auf dem Ballen
    lbox(m, c3, -9, -19, 9.5, -3, -14, 14.5, "sack")                  # ein Sack auf der kleinen Kiste
    lbox(m, c3, -8, -20, 10.5, -4, -19, 13.5, "sack")

    # ------------------------------------------------------------------ Räder
    for name, (x, y, z) in WHEELS.items():
        m.bone(name, pivot=(x, y, z))
        sc.wheel(m, name, 12, -1.5, 1.5, "wheel12")
        sc.hub(m, name, 1 if x > 0 else -1, 1.5)
    return m


# =============================================================================================
# Malen
# =============================================================================================

def _canvas(ctx: Ctx):
    """Plane aus Leinen: hell, leicht fleckig, mit Nähten quer und Staub unten."""
    x, y, z = ctx.local
    v = 0.66 + (noise3(x * 0.25, y * 0.25, z * 0.25, 51) - 0.5) * 0.14
    v += (hash01(ctx.u, ctx.v, 3) - 0.5) * 0.05
    if math.floor(z + 60) % 12 == 0:
        v -= 0.12                                                      # Naht
    if y > 0:
        v -= 0.06 * min(1.0, y / 3.0)                                  # Staub unten
    return pick(sc.PALETTE["linen"], v)


_BOW = sc.wood(1, base=0.5, knots=False)


def _bow(ctx: Ctx):
    return _BOW(ctx)


_FLOOR = sc.planks(2, 4.0, 0.5)


def _floor(ctx: Ctx):
    return _FLOOR(ctx)


def _barrel(ctx: Ctx):
    """Fass: Dauben senkrecht, zwei Eisenreifen, Deckel mit Ringen."""
    r = sc.rel(ctx)
    size = ctx.cube.size
    if ctx.face in ("top", "bottom"):
        cx, cz = size[0] / 2, size[2] / 2
        d = max(abs(r[0] - cx), abs(r[2] - cz))
        return pick(sc.PALETTE["end"], 0.42 + (0.08 if math.floor(d) % 2 == 0 else 0.0))
    if abs(r[1] - 1.5) < 0.6 or abs(r[1] - (size[1] - 1.5)) < 0.6:
        return pick(sc.PALETTE["iron"], 0.35 + sc.LIT_IRON[ctx.face])
    v = 0.5 + (0.08 if math.floor(ctx.u * 0.5) % 2 == 0 else -0.04)
    return pick(sc.PALETTE["oak"], v + (hash01(ctx.u, ctx.v, 21) - 0.5) * 0.06)


def _sack(ctx: Ctx):
    """Sack aus Rupfen: grob gewebt, oben zugebunden."""
    v = 0.5 + (0.06 if (math.floor(ctx.u) + math.floor(ctx.v)) % 2 == 0 else -0.04)
    v += (noise3(ctx.local[0] * 0.4, ctx.local[1] * 0.4, ctx.local[2] * 0.4, 61) - 0.5) * 0.12
    return pick(sc.PALETTE["straw"], v)


def _lantern(ctx: Ctx):
    """Laterne: Eisenrahmen, dazwischen warmes Licht."""
    u0, u1, v0, v1 = sc.face_edge(ctx)
    if ctx.face in ("top", "bottom") or min(u0, u1) < 0.6 or min(v0, v1) < 0.6:
        return pick(sc.PALETTE["iron"], 0.3 + sc.LIT_IRON[ctx.face])
    return pick(sc.PALETTE["straw"], 0.92 + (hash01(ctx.u, ctx.v, 31) - 0.5) * 0.08)


def _hay(ctx: Ctx):
    """Strohballen: Halme längs (in x), zwei Schnüre herum; an den Enden die Schnittflächen."""
    r = sc.rel(ctx)
    size = ctx.cube.size
    if ctx.face not in ("west", "east"):
        for t in (0.28, 0.72):
            if abs(r[0] - size[0] * t) < 0.6:
                return pick(sc.PALETTE["rope"], 0.35)
        stripe = hash01(0, math.floor(ctx.v), 41)
        v = 0.55 + (stripe - 0.5) * 0.3 + (hash01(math.floor(ctx.u * 0.5), math.floor(ctx.v), 43) - 0.5) * 0.1
        return pick(sc.PALETTE["straw"], v)
    v = 0.48 + (hash01(ctx.u, ctx.v, 47) - 0.5) * 0.3
    return pick(sc.PALETTE["straw"], v)


def materials(variant: str) -> dict:
    mats = sc.materials()
    mats.update({"canvas": _canvas, "bow": _bow, "floor": _floor, "barrel": _barrel, "sack": _sack, "lantern": _lantern, "hay": _hay})
    return mats
