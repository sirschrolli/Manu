"""
Tribok (Gegengewichts-Blide): ein langer Grundrahmen aus zwei Schwellen und drei Querhäuptern auf vier kleinen
Vollrädern mit Eisenreifen. Darauf links und rechts je ein Bock aus zwei schrägen Beinen, einem Mittelständer, zwei
Riegeln und zwei Kopfbändern; oben auf jedem Bock ein Lagerklotz mit der eisernen Achse. Auf der Achse der lange
Wurfbalken (verjüngt, mit Eisenbändern), vorn am kurzen Ende der Kasten mit Bruchsteinen als Gegengewicht, hinten an
der Spitze die Schlinge mit Ledertasche und Stein. Hinten zwischen den Schwellen die Winde zum Niederziehen des
Balkens, rechts dahinter eine Bank für den Schützen.

Knochen (Namen und Drehpunkte sind ein fester Vertrag mit TrebuchetModel in Java):
  base (0, 24, 0)            Grundrahmen, Böcke, Achse, Räder (fest); y = 24 ist der Boden
  winch (Kind von base)      Winde, dreht um X
  arm (Kind von base)        Wurfbalken, gebaut WAAGERECHT, Drehpunkt auf der Achse oben
  counterweight (Kind arm)   Gegengewicht, hängt unter seinem Gelenk am kurzen (vorderen) Ende
  sling (Kind von arm)       Schlinge an der Spitze des langen (hinteren) Endes, hängt nach unten
  stone (Kind von sling)     Stein in der Tasche (wird ohne Ladung ausgeblendet)
Hilfsknochen leg_*, brace_*, base_wheel_* sind fest gedreht bzw. fest (nicht animieren).
"""
from __future__ import annotations

from ..geometry import Model
from . import siege_common as sc
from .siege_common import box, lbox

PALETTES = {"trebuchet": sc.PALETTE}

AXLE = (0.0, -44.0, 0.0)          # Drehpunkt des Wurfbalkens (Modell-Koordinaten)
SHORT, LONG = 16.0, 56.0          # Länge des kurzen (vorn, -Z) und des langen Endes (hinten, +Z)
CW_DEPTH = 12.0                   # Tiefe des Gegengewichts: so bleibt es gespannt (bis etwa -64°) vor der Achse
CW_DROP = 17.0                    # Oberkante des Kastens unter dem Gelenk: gespannt läuft der Balken hinter ihm vorbei
WINCH = (0.0, 9.0, 21.5)
WHEELS = {"base_wheel_fl": (16.25, 20.0, -18.0), "base_wheel_fr": (-16.25, 20.0, -18.0),
          "base_wheel_bl": (16.25, 20.0, 18.0), "base_wheel_br": (-16.25, 20.0, 18.0)}


def build() -> Model:
    m = Model("trebuchet", 512, 256)
    m.bone("base", pivot=(0, 24, 0))
    b = "base"

    # ------------------------------------------------------------------ Grundrahmen
    for sx in (1, -1):
        box(m, b, 11 * sx, 14, -24, 15 * sx, 18, 24, "beam")                # Schwellen
        box(m, b, 11 * sx, 18, -20.5, 15 * sx, 21, -15.5, "dark_beam")      # Achsklötze
        box(m, b, 11 * sx, 18, 15.5, 15 * sx, 21, 20.5, "dark_beam")
        box(m, b, 13 * sx, 19.25, -18.75, 14.9 * sx, 20.75, -17.25, "axle")  # Achsstummel
        box(m, b, 13 * sx, 19.25, 17.25, 14.9 * sx, 20.75, 18.75, "axle")
        for z in (-22.0, 22.0):
            sc.band(m, b, 2, z, (11 * sx if sx > 0 else 15 * sx, 14, 0), (15 * sx if sx > 0 else 11 * sx, 18, 0),
                    1.0, 0.25)
    box(m, b, -11, 14.5, -23, 11, 17.5, -19.5, "beam")                       # Querhäupter
    box(m, b, -21, 14.5, -2, 21, 17.5, 2, "beam")                             # mittleres Querhaupt mit Auslegern
    for sx in (1, -1):
        sc.band(m, b, 0, 19.5 * sx, (0, 14.5, -2), (0, 17.5, 2), 1.0, 0.25)
    box(m, b, -11, 14.5, 19.5, 11, 17.5, 23, "beam")

    # ------------------------------------------------------------------ Böcke links und rechts
    apex_y = AXLE[1]
    for sx, side in ((1, "l"), (-1, "r")):
        x = 13 * sx
        sc.strut(m, f"leg_{side}_front", b, (x, apex_y, 0), (x, 14.5, -20), 3.5, "beam", 1.0)
        sc.strut(m, f"leg_{side}_rear", b, (x, apex_y, 0), (x, 14.5, 20), 3.5, "beam", 1.0)
        box(m, b, 11.5 * sx, -38, -1.5, 14.5 * sx, 14, 1.5, "post")          # Mittelständer
        sc.strut(m, f"brace_{side}_out", b, (14.0 * sx, -9.0, 0), (19.5 * sx, 14.6, 0), 2.5, "beam", 0.8)  # Stützstrebe
        box(m, b, 11.75 * sx, -14, -11.2, 14.25 * sx, -11, 11.2, "beam")      # oberer Riegel
        box(m, b, 11.75 * sx, 0, -15.6, 14.25 * sx, 3, 15.6, "beam")          # unterer Riegel
        for sz in (1, -1):
            sc.strut(m, f"brace_{side}_{'rear' if sz > 0 else 'front'}", b, (x, 0.5, 12.0 * sz), (x, -12.5, 1.6 * sz),
                     2.0, "beam", 0.6)
        # Lagerklotz oben mit Eisenbändern, darin die Achse
        box(m, b, 10.5 * sx, -48, -3.5, 15.5 * sx, -38, 3.5, "beam_y")
        for y in (-46.5, -40.0):
            sc.band(m, b, 1, y, (min(10.5 * sx, 15.5 * sx), 0, -3.5), (max(10.5 * sx, 15.5 * sx), 0, 3.5), 1.0, 0.25)
        box(m, b, 15.5 * sx, -45.5, -1.5, 16.3 * sx, -42.5, 1.5, "cap")      # Achskappe außen
    box(m, b, -16, -45, -1, 16, -43, 1, "axle")                               # Achse
    for sx in (1, -1):
        box(m, b, 9.4 * sx, -45.6, -1.6, 10.5 * sx, -42.4, 1.6, "iron_plain")  # Bunde an den Lagerklötzen

    # ------------------------------------------------------------------ Winde hinten, Lagerständer, Bank
    for sx in (1, -1):
        box(m, b, 9 * sx, 6, 19.5, 11 * sx, 14.5, 23.5, "post")
        box(m, b, 8.8 * sx, 12.5, 19.3, 11.2 * sx, 13.5, 23.7, "band")
    box(m, b, -11, 12, 25.5, -3, 13, 28.5, "plank_x")                        # Bank rechts hinten
    for x in (-10.5, -4.5):
        box(m, b, x, 13, 26.2, x + 1.5, 24, 27.8, "post")
    box(m, b, -10, 18, 26.5, -4, 19.2, 27.5, "beam")                          # Steg zwischen den Beinen
    box(m, b, -10.5, 15.5, 23, -9, 16.5, 26.2, "beam")                        # Anschluss an das Querhaupt

    m.bone("winch", b, pivot=sc.abs_to_local(m, b, *WINCH))
    w = "winch"
    lbox(m, w, -9.5, -1.5, -1.5, 9.5, 1.5, 1.5, "beam_x")
    lbox(m, w, -4, -2, -2, 4, 2, 2, "coil")
    for sx in (1, -1):
        lbox(m, w, 6.4 * sx, -1.8, -1.8, 5.6 * sx, 1.8, 1.8, "band")
        lbox(m, w, 7 * sx, -3.6, -0.5, 8 * sx, 3.6, 0.5, "beam_y")
        lbox(m, w, 7 * sx, -0.5, -3.6, 8 * sx, 0.5, 3.6, "beam_z")

    # ------------------------------------------------------------------ Räder (fest am Rahmen)
    for name, (x, y, z) in WHEELS.items():
        m.bone(name, b, pivot=sc.abs_to_local(m, b, x, y, z))
        sc.wheel(m, name, 8, -1.25, 1.25, "wheel8")
        sc.hub(m, name, 1 if x > 0 else -1, 1.25, 0.8, 0.5)

    # ------------------------------------------------------------------ Wurfbalken (waagerecht)
    m.bone("arm", b, pivot=sc.abs_to_local(m, b, *AXLE))
    a = "arm"
    lbox(m, a, -3, -3.5, -4, 3, 3.5, 4, "beam_z")                             # Nabe auf der Achse
    lbox(m, a, -2.5, -3, -SHORT, 2.5, 3, -4, "beam_z")                        # kurzes Ende
    lbox(m, a, -2.5, -2.5, 4, 2.5, 2.5, 20, "beam_z")                         # langes Ende, verjüngt
    lbox(m, a, -2, -2, 20, 2, 2, 38, "beam_z")
    lbox(m, a, -1.5, -1.5, 38, 1.5, 1.5, LONG - 2, "beam_z")
    lbox(m, a, -1.8, -1.8, LONG - 2, 1.8, 1.8, LONG, "iron")                  # Eisenschuh an der Spitze
    lbox(m, a, -4.3, -0.6, -SHORT - 0.6, 4.3, 0.6, -SHORT + 0.6, "axle")       # Gelenkbolzen des Gegengewichts
    for z0, gx, gy in ((-13.0, 2.8, 3.3), (-8.0, 2.8, 3.3), (-2.5, 3.15, 3.8), (2.5, 3.15, 3.8), (9.0, 2.8, 2.8),
                       (17.0, 2.8, 2.8), (26.0, 2.3, 2.3), (34.0, 2.3, 2.3), (46.0, 1.8, 1.8)):
        lbox(m, a, -gx, -gy, z0 - 0.5, gx, gy, z0 + 0.5, "band")

    # Gegengewicht: Kasten mit Bruchsteinen, hängt am Bolzen
    m.bone("counterweight", a, pivot=(0, 0, -SHORT))
    c = "counterweight"
    for sx in (1, -1):
        lbox(m, c, 2.6 * sx, -1.6, -1.25, 3.6 * sx, CW_DROP - 0.5, 1.25, "iron")   # lange Hängeeisen (Bügel)
        lbox(m, c, 2.5 * sx, CW_DROP * 0.5 - 0.6, -1.4, 3.7 * sx, CW_DROP * 0.5 + 0.6, 1.4, "band")
    lbox(m, c, -9.6, CW_DROP - 2, -1.6, 9.6, CW_DROP, 1.6, "beam_x")         # Joch
    d = CW_DEPTH / 2
    top, bot = CW_DROP, CW_DROP + 15
    lbox(m, c, -9, top, -d, 9, bot, d, "crate")
    for y in (top + 2.5, top + 12.0):
        lbox(m, c, -9.3, y, -d - 0.3, 9.3, y + 1.2, d + 0.3, "band")
    for sx in (1, -1):
        for sz in (1, -1):
            lbox(m, c, 9.45 * sx, top - 0.2, (d + 0.45) * sz, 8.45 * sx, bot + 0.3, (d - 0.55) * sz, "iron_plain")   # Eckwinkel
    sc.boulder(m, c, -4.2, top - 0.8, -3.4, 4, 3, 3)
    sc.boulder(m, c, 3.8, top - 0.6, 3.3, 5, 3, 3)
    sc.boulder(m, c, 4.6, top - 0.4, -3.4, 3, 2, 3)

    # Schlinge an der Spitze: Querriegel, zwei Seile, Ledertasche
    m.bone("sling", a, pivot=(0, 0, LONG - 1))
    s = "sling"
    lbox(m, s, -2.7, 1.8, -0.5, 2.7, 2.4, 0.5, "iron_plain")
    rope = 14.0                                                               # Länge der Schlingenseile
    for sx in (1, -1):
        lbox(m, s, 2.0 * sx, 2.4, -0.25, 2.5 * sx, 2.4 + rope, 0.25, "rope")
        lbox(m, s, 2.0 * sx, 2.4 + rope, -3, 2.75 * sx, 4.4 + rope, 3, "pouch")
    lbox(m, s, -2.75, 4.4 + rope, -3, 2.75, 5.4 + rope, 3, "pouch")
    m.bone("stone", s, pivot=(0, 2.4 + rope, 0))
    sc.boulder(m, "stone", 0, 0, 0, 4, 4, 4)
    return m


def materials(variant: str) -> dict:
    mats = sc.materials()
    mats["pouch"] = sc.leather_paint(0.56, stitch=False)
    return mats
