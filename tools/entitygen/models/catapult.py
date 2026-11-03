"""
Katapult (Onager auf vier Rädern): ein langer Rahmen aus zwei Eichenbalken mit Querhäuptern, darauf vorn ein Prellbock
mit gepolstertem Prellbalken (Streben nach vorn und hinten), dazwischen das dicke Torsionsbündel aus Seil, in dem der
Wurfarm steckt. Am Arm sitzt der Becher (Holz, innen Leder) mit dem Stein, hinten die Winde mit vier Speichen je Seite,
rechts hinten ein Brettsitz für den Schützen. Eisenbeschläge, Unterlegscheiben und Spannhebel am Torsionsbündel.

Knochen (Namen und Drehpunkte sind ein fester Vertrag mit CatapultModel in Java):
  frame (0, 24, 0)          Rahmen; y = 24 ist der Boden
  wheel_fl/fr/bl/br         Räder (Kinder von root), Drehpunkt = Radmitte, rollen um X
  arm   (Kind von frame)    Wurfarm, gebaut SENKRECHT (abgefeuert); Drehpunkt auf der Achse des Torsionsbündels
  cup   (Kind von arm)      Becher an der Armspitze, öffnet nach vorn (-Z) = beim gespannten Arm nach oben
  stone (Kind von cup)      Stein im Becher (wird ohne Ladung ausgeblendet)
  winch (Kind von frame)    Winde hinten, dreht um X
Hilfsknochen brace_* sind fest gedrehte Streben (nicht animieren).
"""
from __future__ import annotations

import math

from ..geometry import Model
from ..paint import Ctx, pick
from . import siege_common as sc
from .siege_common import band, box, lbox

PALETTES = {"catapult": sc.PALETTE}

# Drehpunkte in Modell-Koordinaten (für den Bericht an die Java-Seite)
ARM_PIVOT = (0.0, 13.0, 7.0)
WINCH_PIVOT = (0.0, 14.5, 18.0)
WHEELS = {"wheel_fl": (14.0, 18.0, -12.0), "wheel_fr": (-14.0, 18.0, -12.0),
          "wheel_bl": (14.0, 18.0, 14.0), "wheel_br": (-14.0, 18.0, 14.0)}


def build() -> Model:
    m = Model("catapult", 256, 256)
    m.bone("frame", pivot=(0, 24, 0))
    f = "frame"

    # ------------------------------------------------------------------ Rahmen
    for sx in (1, -1):
        x0, x1 = 9 * sx, 12 * sx
        box(m, f, x0, 11, -19, x1, 15, 24, "beam")                         # Längsbalken
        box(m, f, x0, 15, -14, x1, 19.5, -10, "dark_beam")                 # Achsklötze
        box(m, f, x0, 15, 12, x1, 19.5, 16, "dark_beam")
        box(m, f, x0, -1, 1.5, x1, 11, 4.5, "post")                        # Ständer des Prellbocks
        for z in (-17.5, 21.2):
            band(m, f, 2, z, (x0 if sx > 0 else x1, 11, 0), (x1 if sx > 0 else x0, 15, 0), 1.0, 0.25)
        band(m, f, 1, 9.5, (min(x0, x1), 0, 1.5), (max(x0, x1), 0, 4.5), 1.0, 0.25)   # Band unten am Ständer
        # Eisenwinkel am Fuß des Ständers (außen)
        box(m, f, 12 * sx, 6.5, 0.5, 12.4 * sx, 12.5, 4.0, "iron")
        # Unterlegscheibe und Spannhebel des Torsionsbündels (außen am Längsbalken)
        box(m, f, 12 * sx, 10.5, 5, 12.5 * sx, 15.5, 9, "washer")
        box(m, f, 12 * sx, 11, 4.5, 12.5 * sx, 15, 9.5, "washer")
        box(m, f, 12.5 * sx, 8.5, 6.5, 13.1 * sx, 17.5, 7.5, "iron_plain")
    box(m, f, -9, 11, -19, 9, 14, -16, "beam")                               # vorderes Querhaupt
    box(m, f, -9, 12, 1.5, 9, 14.5, 4.5, "beam")                             # Querhaupt unter dem Prellbock
    box(m, f, -9, 12, 21.5, 9, 14.5, 23.5, "beam")                           # hinteres Querhaupt (Armauflage)
    box(m, f, -2.5, 11.6, 21.8, 2.5, 12.05, 23.2, "leather_plain")           # Lederpolster für den gespannten Arm
    box(m, f, -13, 17, -13, 13, 19, -11, "axle")                             # Achsen
    box(m, f, -13, 17, 13, 13, 19, 15, "axle")

    # Prellbalken mit Polster, Eisenkappen an den Enden
    box(m, f, -12.5, -4, 1, 12.5, -1, 5, "beam")
    for sx in (1, -1):
        band(m, f, 0, 11.6 * sx, (0, -4, 1), (0, -1, 5), 1.4, 0.25)
    box(m, f, -3.5, -4.3, 5, 3.5, -0.7, 5.5, "cushion")

    # Streben: nach vorn zum Längsbalken, nach hinten über das Torsionsbündel
    for sx, side in ((1, "l"), (-1, "r")):
        sc.strut(m, f"brace_front_{side}", f, (10.5 * sx, 0.5, 1.5), (10.5 * sx, 11.3, -12.5), 2.0, "beam", 0.6)
        sc.strut(m, f"brace_rear_{side}", f, (10.5 * sx, 3.0, 4.5), (10.5 * sx, 11.3, 12.0), 2.0, "beam", 0.6)

    # Torsionsbündel (Achse des Arms) – reicht in die Längsbalken hinein
    box(m, f, -10, 10.5, 4.5, 10, 15.5, 9.5, "skein")
    box(m, f, -9.5, 10.2, 5.6, -7.5, 15.8, 8.4, "band")                      # Bündelschellen innen an den Balken
    box(m, f, 7.5, 10.2, 5.6, 9.5, 15.8, 8.4, "band")

    # Sitz rechts hinten (rechts = -X)
    box(m, f, -15, 8, 16.5, -8, 9, 22.5, "plank_x")
    box(m, f, -11.5, 9, 17.5, -9.5, 11, 18.5, "post")
    box(m, f, -11.5, 9, 20.5, -9.5, 11, 21.5, "post")

    # ------------------------------------------------------------------ Wurfarm (senkrecht = abgefeuert)
    m.bone("arm", f, pivot=sc.abs_to_local(m, f, *ARM_PIVOT))
    lbox(m, "arm", -1.5, -26, -1.5, 1.5, 2, 1.5, "beam_y")
    lbox(m, "arm", -2, -9, -2, 2, -3, 2, "beam_y")                          # verstärkter Fuß
    for y in (-7.5, -4.5, -14.5, -19.5):
        g = 2.3 if y > -9 else 1.8
        lbox(m, "arm", -g, y - 0.5, -g, g, y + 0.5, g, "band")
    lbox(m, "arm", -1.8, -26.4, -1.8, 1.8, -25.4, 1.8, "band")              # Kappe oben

    # Becher (Holz, innen Leder), öffnet nach vorn
    m.bone("cup", "arm", pivot=(0, -22, -1.5))
    lbox(m, "cup", -4, -4, -1, 4, 4, 0, "cup")
    lbox(m, "cup", 3, -4, -4, 4, 4, -1, "cup")
    lbox(m, "cup", -4, -4, -4, -3, 4, -1, "cup")
    lbox(m, "cup", -3, -4, -4, 3, -3, -1, "cup")
    lbox(m, "cup", -3, 3, -4, 3, 4, -1, "cup")
    lbox(m, "cup", -2.1, -3.4, -0.4, 2.1, -2.6, 3.4, "band")                # Schellen um den Arm
    lbox(m, "cup", -2.1, 2.6, -0.4, 2.1, 3.4, 3.4, "band")

    # Stein im Becher
    m.bone("stone", "cup", pivot=(0, 0, -4))
    sc.boulder(m, "stone", 0, 0, 0, 6, 6, 6)
    lbox(m, "stone", -1.5, -3.6, -1, 1.0, -2.8, 1.5, "stone")               # kleine Buckel
    lbox(m, "stone", 2.6, -0.5, -2.2, 3.4, 1.8, 0.5, "stone")

    # ------------------------------------------------------------------ Winde
    m.bone("winch", f, pivot=sc.abs_to_local(m, f, *WINCH_PIVOT))
    w = "winch"
    lbox(m, w, -9.5, -1.5, -1.5, 9.5, 1.5, 1.5, "beam_x")
    lbox(m, w, -4, -2, -2, 4, 2, 2, "coil")                                 # aufgewickeltes Zugseil
    for sx in (1, -1):
        lbox(m, w, 8.0 * sx, -1.8, -1.8, 8.8 * sx, 1.8, 1.8, "band")
        lbox(m, w, 6.5 * sx, -3.8, -0.5, 7.5 * sx, 3.8, 0.5, "beam_y")      # Speichen
        lbox(m, w, 6.5 * sx, -0.5, -3.8, 7.5 * sx, 0.5, 3.8, "beam_z")

    # ------------------------------------------------------------------ Räder
    for name, (x, y, z) in WHEELS.items():
        m.bone(name, pivot=(x, y, z))
        sc.wheel(m, name, 12, -1.5, 1.5, "wheel12")
        sc.hub(m, name, 1 if x > 0 else -1, 1.5)
    return m


# =============================================================================================
# Malen
# =============================================================================================

def _washer(ctx: Ctx):
    """Runde Unterlegscheibe des Torsionsbündels (nur nach Lage gemalt: zwei Quader decken sich gleich)."""
    if ctx.face not in ("west", "east"):
        return pick(sc.PALETTE["iron"], 0.3 + sc.LIT_IRON[ctx.face])
    _, y, z = ctx.world                       # Modell-Koordinaten (Rahmen ungedreht); Mitte = Achse des Arms
    dy, dz = y - ARM_PIVOT[1], z - ARM_PIVOT[2]
    r = math.hypot(dy, dz)
    if r < 1.3:
        return pick(sc.PALETTE["rope"], 0.42)                              # Seilende in der Mitte
    if abs(abs(dy) - abs(dz)) < 0.6 and r > 2.0:
        return pick(sc.PALETTE["iron"], 0.74)                              # Niete
    v = 0.46 - (0.08 if r > 2.4 else 0.0) + (0.06 if dy < -1 else 0.0)
    return pick(sc.PALETTE["iron"], v)


def _cup(ctx: Ctx):
    """Becher: außen Holzbohlen, die Ränder vorn Eisen, innen vernähtes Leder."""
    n = {"west": (-1, 0, 0), "east": (1, 0, 0), "top": (0, -1, 0), "bottom": (0, 1, 0),
         "front": (0, 0, -1), "back": (0, 0, 1)}[ctx.face]
    x, y, z = ctx.local
    inner = n[0] * (0 - x) + n[1] * (0 - y) + n[2] * (-2.5 - z) > 0.05
    if inner:
        return _CUP_LEATHER(ctx)
    if ctx.face == "front" and ctx.cube.size[2] >= 2.5:
        return pick(sc.PALETTE["end"], sc.end_value(ctx, 2, 0.62))         # Rand: Hirnholz der Wände
    r = sc.rel(ctx)
    if ctx.face in ("west", "east", "top", "bottom") and ctx.cube.size[2] >= 2.5 and 1.0 <= r[2] < 2.0:
        along = x if ctx.face in ("top", "bottom") else y                  # Eisenband unter dem Rand, Niete
        if math.floor(along + 20) % 3 == 1:
            return sc.rivet(ctx, 0.74)
        return pick(sc.PALETTE["iron"], 0.42 + sc.LIT_IRON[ctx.face])
    return _CUP_WOOD_V(ctx) if ctx.face in ("west", "east") else _CUP_WOOD_H(ctx)


_CUP_LEATHER = sc.leather_paint(0.36)
_CUP_WOOD_V = sc.planks(axis=1, width=2.7, nails=False)
_CUP_WOOD_H = sc.planks(axis=0, width=2.7, nails=False)


def materials(variant: str) -> dict:
    mats = sc.materials()
    mats.update({"washer": _washer, "cup": _cup})
    return mats
