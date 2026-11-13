"""
Balliste (Torsionsgeschütz): ein niedriger Fahrwagen mit vier kleinen Vollrädern, darauf ein Ständer mit Streben und
Drehscheibe, auf der die Gabel mit dem Schwenkbolzen sitzt. Darauf das Geschütz: die Gleitbahn mit Führungsleisten,
vorn der Spannrahmen mit zwei senkrechten Torsionsbündeln (eiserne Spannscheiben oben und unten), darin die beiden
Bogenarme mit Eisenspitzen und die Sehne, hinten die Kralle mit dem Abzug und die kleine Winde mit Speichen.

Knochen (Namen und Drehpunkte sind ein fester Vertrag mit BallistaModel in Java):
  stand (0, 24, 0)                Fahrwagen, Ständer, Drehscheibe, Gabel; y = 24 ist der Boden
  bow (Kind von stand)            das ganze Geschütz, neigt um X (Drehpunkt = Schwenkbolzen)
  left_arm / right_arm (Kind bow) Bogenarme, Drehpunkt in der Achse des Torsionsbündels, schwingen um Y
  string_left / string_right      Sehnenhälften (Kinder der Arme), Drehpunkt an der Armspitze, gebaut GESPANNT
  winch (Kind von bow)            Winde hinten, dreht um X
  bolt (Kind von bow)             eingelegter Bolzen (wird ohne Ladung ausgeblendet)
Hilfsknochen *_beam, *_cord, bolt_fin_*, brace_*, stand_wheel_* sind fest gedreht bzw. fest (nicht animieren).
"""
from __future__ import annotations

import math

from ..geometry import Model
from ..paint import Ctx, pick
from . import siege_common as sc
from .siege_common import box, lbox

PALETTES = {"ballista": sc.PALETTE}

BOW_PIVOT = (0.0, 10.0, 0.0)                 # Schwenkbolzen (Modell-Koordinaten)
SKEIN_X, SKEIN_Y, SKEIN_Z = 4.5, -2.5, -10.5  # Achse der Torsionsbündel (relativ zum Bogen; links +X)
ARM_LEN, ARM_BACK = 12.5, 28.0                # Länge der Bogenarme, Winkel nach hinten (gespannt)
CLAW = (0.0, -2.0, 6.5)                       # wo die Sehne in der Kralle hängt (relativ zum Bogen)
BOLT_CENTER = (0.0, -2.0, CLAW[2] - sc.BOLT_END)
WINCH = (0.0, -0.25, 11.0)
WHEELS = {"stand_wheel_fl": (9.5, 20.0, -7.0), "stand_wheel_fr": (-9.5, 20.0, -7.0),
          "stand_wheel_bl": (9.5, 20.0, 7.0), "stand_wheel_br": (-9.5, 20.0, 7.0)}


def arm_tip() -> tuple[float, float, float]:
    """Spitze des linken Arms relativ zum Drehpunkt von left_arm (gespannt)."""
    a = math.radians(ARM_BACK)
    return ARM_LEN * math.cos(a), 0.0, ARM_LEN * math.sin(a)


def build() -> Model:
    m = Model("ballista", 256, 256)
    m.bone("stand", pivot=(0, 24, 0))
    st = "stand"

    # ------------------------------------------------------------------ Fahrwagen
    for sx in (1, -1):
        box(m, st, 5 * sx, 16, -10, 8 * sx, 19, 10, "beam")                   # Wangen
        for z in (-7.0, 7.0):
            box(m, st, 5 * sx, 19, z - 1.5, 8 * sx, 21.5, z + 1.5, "dark_beam")  # Achsklötze
            box(m, st, 8 * sx, 19.4, z - 0.6, 8.5 * sx, 20.6, z + 0.6, "axle")
        sc.band(m, st, 2, -8.5, (min(5 * sx, 8 * sx), 16, 0), (max(5 * sx, 8 * sx), 19, 0), 0.8, 0.2)
        sc.band(m, st, 2, 8.5, (min(5 * sx, 8 * sx), 16, 0), (max(5 * sx, 8 * sx), 19, 0), 0.8, 0.2)
    box(m, st, -5, 16.5, -10, 5, 18.5, -7, "beam")                             # Querhäupter
    box(m, st, -5, 16.5, 7, 5, 18.5, 10, "beam")
    box(m, st, -5, 16.5, -2, 5, 19, 2, "beam")
    box(m, st, -2, 14, -2, 2, 16.5, 2, "post")                                 # Ständer
    for sx, side in ((1, "l"), (-1, "r")):
        sc.strut(m, f"brace_{side}", st, (1.9 * sx, 14.6, 0), (6.4 * sx, 16.4, 0), 1.5, "beam", 0.5)
    for sz, side in ((1, "b"), (-1, "f")):
        sc.strut(m, f"brace_{side}", st, (0, 14.6, 1.9 * sz), (0, 16.6, 8.5 * sz), 1.5, "beam", 0.5)
    # Drehscheibe (Pixelkreis Ø8) und Gabel mit Schwenkbolzen
    box(m, st, -4, 12.5, -2, 4, 14, 2, "turntable")
    for sz in (1, -1):
        box(m, st, -3, 12.5, 2 * sz, 3, 14, 3 * sz, "turntable")
        box(m, st, -2, 12.5, 3 * sz, 2, 14, 4 * sz, "turntable")
    for sx in (1, -1):
        box(m, st, 2.25 * sx, 8.5, -2, 3.25 * sx, 12.5, 2, "beam_y")            # Gabelwangen
        box(m, st, 3.25 * sx, 9.25, -0.75, 3.75 * sx, 10.75, 0.75, "cap")
    box(m, st, -3.25, 9.4, -0.6, 3.25, 10.6, 0.6, "axle")                      # Schwenkbolzen

    for name, (x, y, z) in WHEELS.items():
        m.bone(name, st, pivot=sc.abs_to_local(m, st, x, y, z))
        sc.wheel(m, name, 8, -1.0, 1.0, "wheel8")
        sc.hub(m, name, 1 if x > 0 else -1, 1.0, 0.6, 0.4)

    # ------------------------------------------------------------------ Geschütz
    m.bone("bow", st, pivot=sc.abs_to_local(m, st, *BOW_PIVOT))
    bw = "bow"
    lbox(m, bw, -2, -1, -15, 2, 1, 9, "beam_z")                                # Gleitbahn (mittig auf dem Bolzen)
    for sx in (1, -1):
        lbox(m, bw, 1 * sx, -1.75, -15, 2 * sx, -1, -1, "beam_z")             # Führungsleisten
        lbox(m, bw, 2 * sx, -2, 7, 3 * sx, 2, 13, "beam_z")                   # Wangen der Winde
    lbox(m, bw, -2.3, -1.3, -14.6, 2.3, 1.3, -13.6, "band")                    # Eisenband vorn
    lbox(m, bw, -2.3, -1.3, 5.0, 2.3, 1.3, 6.0, "band")
    # Spannrahmen vorn
    z0, z1 = SKEIN_Z - 1.5, SKEIN_Z + 1.5
    lbox(m, bw, -7.5, -8, z0, 7.5, -6, z1, "beam_x")                           # oberes Rahmenholz
    lbox(m, bw, -7.5, 1, z0, 7.5, 3, z1, "beam_x")                             # unteres Rahmenholz
    for sx in (1, -1):
        lbox(m, bw, (SKEIN_X - 1) * sx, -6, SKEIN_Z - 1, (SKEIN_X + 1) * sx, 1, SKEIN_Z + 1, "skein")
        lbox(m, bw, 2.0 * sx, -6, SKEIN_Z - 0.75, 3.0 * sx, 1, SKEIN_Z + 0.75, "post")       # innere Ständer
        for y0, y1 in ((-8.75, -8.0), (3.0, 3.75)):                                          # Spannscheiben
            lbox(m, bw, (SKEIN_X - 2) * sx, y0, SKEIN_Z - 1, (SKEIN_X + 2) * sx, y1, SKEIN_Z + 1, "washer")
            lbox(m, bw, (SKEIN_X - 1) * sx, y0, SKEIN_Z - 2, (SKEIN_X + 1) * sx, y1, SKEIN_Z + 2, "washer")
        lbox(m, bw, (SKEIN_X - 0.5) * sx, -9.4, SKEIN_Z - 3, (SKEIN_X + 0.5) * sx, -8.75, SKEIN_Z + 3, "iron_plain")
        for y0, y1 in ((-8.0, -6.0), (1.0, 3.0)):                                            # Bänder an den Enden
            lbox(m, bw, 6.4 * sx, y0 - 0.25, z0 - 0.25, 7.2 * sx, y1 + 0.25, z1 + 0.25, "band")
    # Kralle mit Abzug, Seil zur Winde
    lbox(m, bw, -1.25, -2.5, 6.5, 1.25, -1.0, 9.5, "iron")
    for sx in (1, -1):
        lbox(m, bw, 0.95 * sx, -3.5, 5.9, 1.45 * sx, -1.9, 7.1, "iron_plain")  # Krallenzinken
    lbox(m, bw, -0.4, 1.0, 7.5, 0.4, 3.0, 8.3, "iron_plain")                   # Abzugshebel
    lbox(m, bw, -0.25, -2.0, 9.5, 0.25, -1.5, 10.2, "rope")

    # Bogenarme, darin fest gedrehte Hilfsknochen mit den Quadern; Sehnen an den Spitzen
    tip = arm_tip()
    claw_rel = (CLAW[0] - SKEIN_X, CLAW[1] - SKEIN_Y, CLAW[2] - SKEIN_Z)
    for sx, side, sname in ((1, "left", "string_left"), (-1, "right", "string_right")):
        arm = f"{side}_arm"
        m.bone(arm, bw, pivot=(SKEIN_X * sx, SKEIN_Y, SKEIN_Z))
        beam = f"{side}_arm_beam"
        m.bone(beam, arm, pivot=(0, 0, 0), rotation=(0, ARM_BACK * -sx, 0))
        lbox(m, beam, 0, -1.5, -1.25, 7 * sx, 1.5, 1.25, "beam_x")
        lbox(m, beam, 7 * sx, -1.25, -1, (ARM_LEN - 1.4) * sx, 1.25, 1, "beam_x")
        lbox(m, beam, (ARM_LEN - 1.4) * sx, -1.45, -1.2, (ARM_LEN + 0.4) * sx, 1.45, 1.2, "iron")
        lbox(m, beam, 2.6 * sx, -1.8, -1.55, 3.4 * sx, 1.8, 1.55, "band")
        lbox(m, beam, 5.9 * sx, -1.8, -1.55, 6.7 * sx, 1.8, 1.55, "band")
        # Sehne: von der Spitze zur Kralle
        tx, ty, tz = tip[0] * sx, tip[1], tip[2]
        m.bone(sname, arm, pivot=(tx, ty, tz))
        dx, dy, dz = claw_rel[0] * sx - tx, claw_rel[1] - ty, claw_rel[2] - tz
        length = math.sqrt(dx * dx + dy * dy + dz * dz)
        yaw = math.degrees(math.atan2(dx, dz))
        pitch = -math.degrees(math.asin(dy / length))
        cord = f"{sname}_cord"
        m.bone(cord, sname, pivot=(0, 0, 0), rotation=(pitch, yaw, 0))
        lbox(m, cord, -0.25, -0.25, 0, 0.25, 0.25, length, "string")

    # Winde hinten
    m.bone("winch", bw, pivot=WINCH)
    w = "winch"
    lbox(m, w, -3.5, -1, -1, 3.5, 1, 1, "beam_x")
    lbox(m, w, -1.8, -1.4, -1.4, 1.8, 1.4, 1.4, "coil")
    for sx in (1, -1):
        lbox(m, w, 3.5 * sx, -2.8, -0.4, 4.3 * sx, 2.8, 0.4, "beam_y")
        lbox(m, w, 3.5 * sx, -0.4, -2.8, 4.3 * sx, 0.4, 2.8, "beam_z")
        lbox(m, w, 3.5 * sx, -0.75, -0.75, 4.6 * sx, 0.75, 0.75, "cap")

    # eingelegter Bolzen
    m.bone("bolt", bw, pivot=BOLT_CENTER)
    sc.bolt(m, "bolt")
    return m


# =============================================================================================
# Malen
# =============================================================================================

def _turntable(ctx: Ctx):
    """Drehscheibe (fünf Quader, nur nach Lage gemalt): oben Bohlen mit eisernem Rand und Bolzen, Rand Eisen."""
    x, y, z = ctx.local
    if ctx.face == "top":
        if not all(sc.disc_inside(8, x + a, z + b) for a, b in ((1, 0), (-1, 0), (0, 1), (0, -1))):
            ang = math.atan2(z, x)
            if int(round((ang + math.pi) / (2 * math.pi) * 16)) % 4 == 0:
                return sc.rivet(ctx, 0.74)
            return pick(sc.PALETTE["iron"], 0.5)
        if abs(x) > 0.5 and abs(x) < 1.5 and abs(z) > 0.5:
            return pick(sc.PALETTE["oak"], 0.2)                               # Fugen
        return pick(sc.PALETTE["oak"], sc.wood_value(ctx, 2, 0.6, knots=False, edges=False, seed=77))
    if ctx.face == "bottom":
        return pick(sc.PALETTE["oak"], 0.3)
    return pick(sc.PALETTE["iron"], 0.4 + sc.LIT_IRON[ctx.face] + (0.06 if sc.rel(ctx)[1] < 0.6 else 0.0))


def _washer(ctx: Ctx):
    """Runde eiserne Spannscheibe auf dem Torsionsbündel (zwei Quader, nur nach Lage gemalt)."""
    x, y, z = ctx.local
    sx = 1 if x > 0 else -1
    dx, dz = x - SKEIN_X * sx, z - SKEIN_Z
    if ctx.face in ("top", "bottom"):
        r = math.hypot(dx, dz)
        if r < 0.9:
            return pick(sc.PALETTE["rope"], 0.4)
        if abs(abs(dx) - abs(dz)) < 0.6 and r > 1.2:
            return sc.rivet(ctx, 0.74)
        return pick(sc.PALETTE["iron"], 0.48 if ctx.face == "top" else 0.3)
    return pick(sc.PALETTE["iron"], 0.38 + sc.LIT_IRON[ctx.face])


def _string(ctx: Ctx):
    """Sehne aus gedrehtem Darm/Hanf: hell, mit dunklen Drallstrichen."""
    r = sc.rel(ctx)
    k = math.floor(r[2] * 1.0 + (0.5 if ctx.face in ("west", "top") else 0.0))
    return pick(sc.PALETTE["linen"], 0.62 if k % 2 else 0.42)


def materials(variant: str) -> dict:
    mats = sc.materials()
    mats.update({"turntable": _turntable, "washer": _washer, "string": _string})
    return mats
