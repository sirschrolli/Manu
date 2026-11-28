"""
Rammbock (fahrbares Schutzdach, "Katze"): ein Gerüst aus Schwellen, Eck- und Mittelständern, Rähmen und Firstbalken
auf vier Vollrädern, darüber ein steiles Satteldach aus Bohlen, gedeckt mit sich überlappenden, vernähten und
genagelten Rinderhäuten (dunkle nasse Flecken – nasse Häute brennen nicht). Unten an den Seiten Bretterwände, vorn ein
verbretterter Giebel mit der Öffnung für den Widder, links und rechts davon Hautvorhänge.

Der Widder ist ein langer Stamm mit Eisenbändern, vorn eine eiserne Kappe mit Widderhörnern; er hängt an zwei
Ketten (V-förmig) an einem Ring unter dem Firstbalken und schwingt wie ein Pendel.

Knochen (Namen und Drehpunkte sind ein fester Vertrag mit BatteringRamModel in Java):
  frame (0, 24, 0)         Gerüst, Dach, Wände; y = 24 ist der Boden
  wheel_fl/fr/bl/br        Räder (Kinder von root), Drehpunkt = Radmitte, rollen um X
  log (Kind von frame)     Widder samt Ketten, Drehpunkt = Aufhängering unter dem First, schwingt um X
Hilfsknochen roof_l/roof_r (Dachflächen) und log_chain_front/rear (Ketten) sind fest gedreht (nicht animieren).
"""
from __future__ import annotations

import math

from ..geometry import Model
from ..paint import Ctx, hash01, noise3, pick
from . import siege_common as sc
from .siege_common import box, lbox

PALETTES = {"battering_ram": sc.PALETTE}

RIDGE_Y = -14.0                      # Oberkante des Firstbalkens – Drehpunkt der Dachflächen
PITCH = 45.0                         # Dachneigung
EAVE_X = 16.5                        # Traufe (halbe Breite)
LOG_PIVOT = (0.0, -11.0, -6.0)       # Aufhängering unter dem First (Modell-Koordinaten)
LOG_Y = (3.0, 9.0)                   # Ober- und Unterkante des Stamms (Modell-Koordinaten)
LOG_Z = (-30.0, 24.5)                # Stamm (ohne Kopf) von vorn nach hinten
CHAIN_DZ = 10.0                      # Ketten greifen so weit vor und hinter dem Ring am Stamm an
WHEELS = {"wheel_fl": (15.0, 18.0, -18.0), "wheel_fr": (-15.0, 18.0, -18.0),
          "wheel_bl": (15.0, 18.0, 18.0), "wheel_br": (-15.0, 18.0, 18.0)}
HIDES = 5                            # Häute je Dachseite
COLLAR_Y, COLLAR_HALF = -5.5, 8.0    # Unterkante und halbe Länge der Kehlbalken vorn/hinten


def build() -> Model:
    m = Model("battering_ram", 512, 512)
    m.bone("frame", pivot=(0, 24, 0))
    f = "frame"

    # ------------------------------------------------------------------ Gerüst
    for sx in (1, -1):
        x0, x1 = 10 * sx, 13 * sx
        box(m, f, x0, 14, -28, x1, 17, 28, "beam")                            # Schwelle
        box(m, f, x0, -1, -29, x1, 2, 29, "beam")                             # Rähm
        for z in (-26.5, 0.0, 26.5):                                          # Eck- und Mittelständer
            box(m, f, 10.25 * sx, 2, z - 1.25, 12.75 * sx, 14, z + 1.25, "post")
        for z in (-18.0, 18.0):                                               # Achsklötze
            box(m, f, x0, 17, z - 2, x1, 20.5, z + 2, "dark_beam")
        # Bretterwand unten außen an den Ständern
        box(m, f, 12.75 * sx, 8.5, -27.75, 13.3 * sx, 14.2, 27.75, "plank_z")
        for z in (-26.5, 0.0, 26.5):
            sc.band(m, f, 2, z, (min(x0, x1), 14, 0), (max(x0, x1), 17, 0), 1.0, 0.25)
    for z in (-18.0, 18.0):
        box(m, f, -14, 17, z - 1, 14, 19, z + 1, "axle")
    box(m, f, -10, 14.5, 25.5, 10, 16.5, 28, "beam")                          # Querschwelle hinten (vorn frei für den Widder)
    for z0, z1 in ((-28.0, -25.5), (25.5, 28.0)):                             # Kehlbalken vorn und hinten (hoch:
        box(m, f, -COLLAR_HALF, COLLAR_Y - 2, z0, COLLAR_HALF, COLLAR_Y, z1, "beam")  # der Widder schwingt darunter)
    box(m, f, -1.5, RIDGE_Y, -30, 1.5, RIDGE_Y + 3, 30, "beam")             # Firstbalken
    for z in (-26.5, 26.5):                                                   # Firstständer auf den Kehlbalken
        box(m, f, -1.25, RIDGE_Y + 3, z - 1.25, 1.25, COLLAR_Y - 2, z + 1.25, "post")
    sc.band(m, f, 2, LOG_PIVOT[2], (-1.5, RIDGE_Y, 0), (1.5, RIDGE_Y + 3, 0), 1.3, 0.3)   # Hängeeisen

    # Giebel vorn und hinten: gestufte Bretter über dem Bundbalken (unter der Dachfläche)
    for zf, zb in ((-28.6, -28.0), (28.0, 28.6)):
        y = COLLAR_Y - 2
        while True:
            top = y - 2.5
            half = (top - RIDGE_Y) / math.tan(math.radians(PITCH)) - 0.4
            if half < 1.5:
                break
            box(m, f, -half, top, zf, half, y, zb, "plank_x")
            y = top
    # Hautvorhänge links und rechts der Öffnung vorn
    for sx in (1, -1):
        box(m, f, 5.0 * sx, 1.0, -29.1, 10.6 * sx, 1.6, -28.5, "axle")         # Vorhangstange
        box(m, f, 5.5 * sx, 1.6, -28.9, 10.2 * sx, 11.5, -28.4, "curtain")

    # ------------------------------------------------------------------ Dach (zwei fest gedrehte Flächen)
    length = EAVE_X / math.cos(math.radians(PITCH))
    step = (62.0 + 1.5 * (HIDES - 1)) / HIDES
    for sx, side in ((1, "l"), (-1, "r")):
        roof = f"roof_{side}"
        m.bone(roof, f, pivot=sc.abs_to_local(m, f, 0, RIDGE_Y, 0), rotation=(0, 0, PITCH * sx))
        for k in range(HIDES):                                                # Dachbohlen (je Haut ein Feld)
            za = -30.5 + k * 61.0 / HIDES
            lbox(m, roof, 0, -1.2, za, length * sx, 0, za + 61.0 / HIDES, "roofboard")
        for z in (-27.4, -13.7, 0.0, 13.7, 27.4):                            # Sparren darunter
            lbox(m, roof, 1.0 * sx, 0, z - 1, (length - 1.5) * sx, 1.6, z + 1, "beam")
        for k in range(HIDES):
            z0 = -31.0 + k * (step - 1.5)
            y0, y1 = (-1.75, -1.2) if k % 2 == 0 else (-2.25, -1.7)
            lbox(m, roof, -0.3 * sx, y0, z0, (length + 1.6) * sx, y1, z0 + step, "hide")
    box(m, f, -2.6, RIDGE_Y - 3.6, -31.2, 2.6, RIDGE_Y - 0.6, 31.2, "ridgehide")   # Firsthaut

    # ------------------------------------------------------------------ Widder (Pendel)
    m.bone("log", f, pivot=sc.abs_to_local(m, f, *LOG_PIVOT))
    lg = "log"
    py, pz = LOG_PIVOT[1], LOG_PIVOT[2]
    ya, yb = LOG_Y[0] - py, LOG_Y[1] - py                                     # 14 .. 20 unter dem Ring
    za, zb = LOG_Z[0] - pz, LOG_Z[1] - pz
    yc = (ya + yb) / 2
    lbox(m, lg, -3, ya + 1, za, 3, yb - 1, zb, "log")                         # Stamm, achteckig
    lbox(m, lg, -2, ya, za, 2, yb, zb, "log")
    lbox(m, lg, -2.6, yc - 2.6, zb - 0.6, 2.6, yc + 2.6, zb + 0.5, "iron")     # Endplatte hinten
    for z in (-CHAIN_DZ, CHAIN_DZ, za + 6, zb - 6):
        lbox(m, lg, -3.3, ya - 0.3, z - 0.75, 3.3, yb + 0.3, z + 0.75, "band")
    for z in (-CHAIN_DZ, CHAIN_DZ):                                           # Ösen für die Ketten
        lbox(m, lg, -0.6, ya - 1.2, z - 0.6, 0.6, ya - 0.3, z + 0.6, "iron_plain")
    # eiserne Widderkappe: Hülse über dem Stammende, Schädel, Stirn, Maul; seitlich gerollte Hörner
    lbox(m, lg, -3.5, ya - 0.5, za - 1, 3.5, yb + 0.5, za + 3, "ramcap")        # Hülse
    lbox(m, lg, -3.0, ya - 0.3, za - 5, 3.0, yb + 0.1, za - 1, "ramskull")      # Schädel
    lbox(m, lg, -2.4, ya - 0.1, za - 7, 2.4, yb - 1.4, za - 5, "ramskull")      # Stirn (Stoßfläche)
    lbox(m, lg, -1.6, yb - 3.4, za - 7.6, 1.6, yb - 0.2, za - 5, "ramskull")    # Maul
    for sx in (1, -1):
        xa, xb = 3.0 * sx, 5.2 * sx
        lbox(m, lg, xa, ya - 1.2, za - 4.5, xb, ya + 0.8, za + 0.5, "horn")    # Horn: oben nach hinten,
        lbox(m, lg, xa, ya + 0.8, za + 0.5, xb, yb - 1.0, za + 2.4, "horn")    # hinten herab,
        lbox(m, lg, xa, yb - 2.6, za - 3.0, xb, yb - 0.6, za + 0.5, "horn")    # unten wieder nach vorn
        lbox(m, lg, xa, yb - 4.4, za - 3.0, xb - 0.6 * sx, yb - 2.6, za - 1.6, "horn")   # Hornspitze
    # Ring und Ketten
    lbox(m, lg, -0.8, -0.8, -0.8, 0.8, 0.8, 0.8, "iron_plain")
    for name, dz in (("log_chain_front", -CHAIN_DZ), ("log_chain_rear", CHAIN_DZ)):
        sc.strut(m, name, lg, LOG_PIVOT, (0.0, LOG_Y[0] - 1.0, pz + dz), 1.0, "chain", 0.2)

    # ------------------------------------------------------------------ Räder
    for name, (x, y, z) in WHEELS.items():
        m.bone(name, pivot=(x, y, z))
        sc.wheel(m, name, 12, -1.5, 1.5, "wheel12")
        sc.hub(m, name, 1 if x > 0 else -1, 1.5)
    return m


# =============================================================================================
# Malen
# =============================================================================================

def _hide_value(ctx: Ctx, seed: int) -> float:
    """Rinderhaut: weich gefleckt, je Haut ein eigener Ton, große dunkle nasse Flecken, kurze Haarstriche."""
    x, y, z = ctx.local
    c = sc.cid(ctx.cube)
    v = 0.6 + ((c % 13) / 12 - 0.5) * 0.16
    v += (noise3(x * 0.3, y * 0.3, z * 0.3, seed) - 0.5) * 0.18
    wet = noise3(x * 0.13 + c * 0.01, y * 0.13, z * 0.13, seed + 1)
    if wet > 0.6:
        v -= 0.12 + (wet - 0.6) * 0.5
    v += (hash01(math.floor(x), math.floor(z * 0.5), c, 7) - 0.5) * 0.06
    return v


def _hide(ctx: Ctx):
    """
    Dachhaut (im gedrehten Dachknochen; lokal x = von First zur Traufe, z = entlang des Firsts): Außenseite mit
    nassen Flecken, Nähten an den Stößen zu den Nachbarhäuten, Nagelreihen an First und Traufe, ausgefranster
    unterer Rand; Kanten dunkel (eingerollt).
    """
    r = sc.rel(ctx)
    size = ctx.cube.size
    face = ctx.face
    if face not in ("top", "bottom"):
        if face in ("west", "east") and hash01(math.floor(r[2]), sc.cid(ctx.cube), 9) > 0.7:
            return None                                                    # ausgefranste Kante
        return pick(sc.PALETTE["hide"], 0.24)
    if face == "bottom":
        return pick(sc.PALETTE["hide"], 0.3 + (hash01(ctx.u, ctx.v, 8) - 0.5) * 0.06)
    x_out = ctx.local[0] if ctx.cube.origin[0] + size[0] / 2 > 0 else -ctx.local[0]   # Abstand vom First
    lx = size[0]
    dz = min(r[2], size[2] - r[2])
    # ausgefranster Rand an der Traufe
    if x_out > lx - 1.6 - 0.3 and hash01(math.floor(r[2]), sc.cid(ctx.cube), 9) > 0.62:
        return None
    v = _hide_value(ctx, 91)
    upper = ctx.cube.origin[1] < -2.0                                      # obere Lage: an die Nachbarn genäht
    if dz < 1.0:
        v -= 0.12                                                          # eingerollte Kante
    elif upper and dz < 2.0 and math.floor(x_out) % 2 == 0:
        return pick(sc.PALETTE["hide"], 0.78)                              # Naht (Lederriemen)
    if (abs(x_out - 2.0) < 0.5 or abs(x_out - (lx - 3.4)) < 0.5) and math.floor(r[2]) % 3 == 1:
        return sc.rivet(ctx, 0.66)                                         # Nagelreihe
    return pick(sc.PALETTE["hide"], v)


def _ridgehide(ctx: Ctx):
    """Haut über dem First: eine Naht in der Mitte, Nägel seitlich."""
    r = sc.rel(ctx)
    if ctx.face in ("front", "back"):
        return pick(sc.PALETTE["hide"], 0.22)
    v = _hide_value(ctx, 93)
    if ctx.face == "top" and abs(r[0] - ctx.cube.size[0] / 2) < 0.5:
        return pick(sc.PALETTE["linen"], 0.5) if math.floor(r[2]) % 2 == 0 else pick(sc.PALETTE["hide"], v - 0.1)
    if ctx.face in ("west", "east") and math.floor(r[2]) % 4 == 1 and abs(r[1] - 1.5) < 0.6:
        return sc.rivet(ctx, 0.66)
    return pick(sc.PALETTE["hide"], v)


def _curtain(ctx: Ctx):
    """Hautvorhang: hängt in Falten, unten ausgefranst, oben angenagelt."""
    r = sc.rel(ctx)
    size = ctx.cube.size
    if r[1] > size[1] - 1.0 and hash01(math.floor(ctx.local[0] * 1.0), 11) > 0.5:
        return None
    v = _hide_value(ctx, 95) + (0.06 if math.floor(ctx.local[0] + 50) % 3 == 0 else -0.03)
    if r[1] < 1.0:
        if math.floor(ctx.local[0] + 50) % 3 == 1:
            return sc.rivet(ctx, 0.62)
        v -= 0.08
    return pick(sc.PALETTE["hide"], v)


_ROOFBOARD = sc.planks(2, 3.0, 0.5, nails=False)


def _roofboard(ctx: Ctx):
    """Dachbohlen (Unterseite sichtbar), Bohlenstöße quer zur Neigung."""
    return _ROOFBOARD(ctx)


_LOG = sc.wood(2, base=0.6)


def _log(ctx: Ctx):
    """Geschälter Stamm: helle Maserung längs, Hirnholz an den Enden."""
    return _LOG(ctx)


def _ramcap(ctx: Ctx):
    """Eiserne Widderkappe: dunkles, schweres Eisen, vorn blank geschlagen, Nietreihen an den Kanten."""
    r = sc.rel(ctx)
    v = sc.iron_value(ctx, 0.36)
    if ctx.face == "front":
        v += 0.12 + (hash01(ctx.u, ctx.v, 12) - 0.5) * 0.12                 # blank geschlagen
    u0, u1, w0, w1 = sc.face_edge(ctx)
    if min(u0 + u1, w0 + w1) >= 5 and ctx.face != "front":
        if (0.6 < min(u0, u1) < 1.6 or 0.6 < min(w0, w1) < 1.6) and (math.floor(r[2]) + math.floor(r[0] + r[1])) % 2 == 0:
            return sc.rivet(ctx, 0.7)
    return pick(sc.PALETTE["iron"], v)


def _ramskull(ctx: Ctx):
    """Schädel der Widderkappe: dunkles Eisen, Stirn blank geschlagen, seitlich schmale Augenschlitze."""
    x, y, z = ctx.local
    r = sc.rel(ctx)
    size = ctx.cube.size
    ya = LOG_Y[0] - LOG_PIVOT[1]
    za = LOG_Z[0] - LOG_PIVOT[2]
    if ctx.face in ("west", "east") and size[2] >= 3.9 and abs(y - (ya + 2.0)) < 0.6 and za - 4.0 < z < za - 2.0:
        return pick(sc.PALETTE["dye"], 0.2) if z < za - 3.0 else pick(sc.PALETTE["iron"], 0.06)   # Auge
    v = sc.iron_value(ctx, 0.27)
    if ctx.face == "front":
        v += 0.2 + (hash01(ctx.u, ctx.v, 12) - 0.5) * 0.14                  # blank geschlagene Stoßfläche
    elif ctx.face == "top":
        v += 0.05
    if ctx.face in ("west", "east", "top") and r[2] < 0.75:
        v += 0.08                                                          # Kante zur Stirn
    return sc._rust(ctx, pick(sc.PALETTE["iron"], v), 0.7)


def _horn(ctx: Ctx):
    """Gewundene Widderhörner aus blank geschmiedetem Eisen (nur nach Lage gemalt): helle Wülste, dunkle Rillen."""
    x, y, z = ctx.local
    k = math.floor(y * 0.8 + z * 0.8 + 40)
    v = (0.64 if k % 2 == 0 else 0.34) + sc.LIT_IRON[ctx.face]
    return pick(sc.PALETTE["iron"], v)


def materials(variant: str) -> dict:
    mats = sc.materials()
    mats.update({"hide": _hide, "ridgehide": _ridgehide, "curtain": _curtain, "roofboard": _roofboard,
                 "log": _log, "ramcap": _ramcap, "ramskull": _ramskull, "horn": _horn})
    return mats
