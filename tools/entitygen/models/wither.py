"""
Aschenkönig (statt Wither): ein schwebender König aus verkohlten Knochen, im Spiel doppelt so groß gezeichnet wie das
Modell (wie der Vanilla-Wither). Die drei Köpfe sitzen dort, wo das Spiel die Schädel abfeuert (Mitte oben, links und
rechts auf den Schultern), sonst ist alles eigen:

  * der Mittelkopf ist ein schwerer Schädel unter einer Krone aus angelaufenem Gold mit acht Zacken und einem
    Seelenstein; in der Krone lodert Seelenfeuer (zwei gekreuzte Flächen), Augen und Rachen glühen türkis, ein
    glühender Riss läuft über die Stirn
  * die Seitenköpfe tragen geschwungene Hörner aus schwarzem Horn, die nach hinten und außen ausgreifen
  * ein Joch aus verwachsenen Knochen mit Schulterplatten und Knochendornen, darüber der umgeschlagene Mantel
  * ein Brustkorb mit Rippen und Lücken, darin schlägt ein Herz aus Seelenfeuer; darunter Becken und ein Schwanz aus
    drei Wirbeln mit Rückendornen und einer Knochenklinge
  * zwei lange Knochenarme mit Ellbogenknauf und Krallenhänden
  * hinten ein zerschlissener Königsmantel in Netherrot mit Goldborte und der Dreizackkrone, unten verbrannt und
    ausgefranst, an den Brandrändern glimmt Seelenfeuer

Zwei Bilder: der Aschenkönig und "erwachen" (während er sich nach dem Beschwören auflädt: gebleichte Knochen, alles
glüht im Seelenfeuer). Dazu im Spiel der Seelenschild (eigene Hülle, wenn er nur noch halbe Kraft hat).
"""
from __future__ import annotations

import math

from ..geometry import Model
from ..paint import Ctx, Voronoi3D, hash01, hexc, mix, noise3, pick, ramp

PALETTES = {
    "aschenkoenig": {
        "bone": ramp("#120e0d", "#7e766f", steps=8), "ash": ramp("#3a3533", "#b4ada6"), "socket": ramp("#020101", "#18110f"),
        "soul": [hexc(c) for c in ("#062c2a", "#0b4c48", "#127a72", "#1ca498", "#33cdc0", "#6aece0", "#b4fff6", "#f0fffd")],
        "gold": ramp("#2a1d08", "#dcb052", steps=7),
        "cloth": ramp("#200606", "#9c2c1e"), "lining": ramp("#090303", "#3e100c"), "horn": ramp("#16121c", "#8a7f96"),
    },
    "aschenkoenig_erwachen": {
        "bone": ramp("#33403f", "#eefcfa", steps=8), "ash": ramp("#7a8c8c", "#f6fffe"), "socket": ramp("#05302c", "#127a72"),
        "soul": [hexc(c) for c in ("#0f6e68", "#18968c", "#22bcb0", "#3cd8cc", "#74f0e6", "#aefcf4", "#e0fffb", "#ffffff")],
        "gold": ramp("#3e5c58", "#eafffa", steps=7),
        "cloth": ramp("#0c3238", "#5ed8d0"), "lining": ramp("#062224", "#1c6c68"), "horn": ramp("#1a3438", "#ace8e2"),
    },
}

# Lage der Hörner der Seitenköpfe: (Name, Drehpunkt am Schädel, Spreizung)
_HORNS = (("inner", 2.2, 25.0), ("outer", -2.2, -25.0))


def build() -> Model:
    m = Model("wither", 128, 128)
    m.tex_res = 2
    m.world_faces = True
    m.grow_layer = True

    # --- Mittelkopf mit Krone, Seelenfeuer und Unterkiefer
    m.bone("center_head")
    m.cube("center_head", (-4.5, -5, -4.5), (9, 9, 9), "skull")
    m.cube("center_head", (-4.5, -2.4, -4.9), (9, 1.1, 0.4), "brow")
    m.bone("center_jaw", "center_head", pivot=(0, 4, -0.5), rotation=(7, 0, 0))
    m.cube("center_jaw", (-3.25, 0, -3.9), (6.5, 2.2, 4.5), "jaw")
    m.bone("crown", "center_head", pivot=(0, -5, 0))
    m.cube("crown", (-5, -1.5, -5), (10, 2, 10), "crown_band")
    for origin, size in (((-0.75, -5.5, -5.3), (1.5, 4, 0.8)),                 # vorn die große Zacke
                         ((-3.75, -4, -5.25), (1, 2.5, 0.7)), ((2.75, -4, -5.25), (1, 2.5, 0.7)),
                         ((-5.3, -4.5, -0.5), (0.8, 3, 1)), ((4.5, -4.5, -0.5), (0.8, 3, 1)),
                         ((-0.5, -4, 4.5), (1, 2.5, 0.8)),
                         ((-3.75, -3, 4.45), (1, 1.5, 0.7)), ((2.75, -3, 4.45), (1, 1.5, 0.7))):
        m.cube("crown", origin, size, "crown_spike")
    m.bone("crown_fire", "crown", pivot=(0, -0.5, 0))
    m.bone("crown_fire_1", "crown_fire", rotation=(0, 45, 0))
    m.cube("crown_fire_1", (-4, -6, 0), (8, 6, 0), "soul_flame")
    m.bone("crown_fire_2", "crown_fire", rotation=(0, -45, 0))
    m.cube("crown_fire_2", (-4, -6, 0), (8, 6, 0), "soul_flame")

    # --- Seitenköpfe mit Hörnern (rechts = -x wie bei Vanilla)
    for side, sx in (("right", -1), ("left", 1)):
        head = f"{side}_head"
        m.bone(head, pivot=(9.5 * sx, 2.5, 0))
        m.cube(head, (-3.5, -3.5, -3.5), (7, 7, 7), "skull")
        m.cube(head, (-3.5, -1.6, -3.85), (7, 0.9, 0.35), "brow")
        m.bone(f"{side}_jaw", head, pivot=(0, 3.5, -0.5), rotation=(7, 0, 0))
        m.cube(f"{side}_jaw", (-2.4, 0, -2.9), (4.8, 1.8, 3.5), "jaw")
        for name, x, spread in _HORNS:
            horn = f"{side}_horn_{name}"
            # innen und außen zeigen beim rechten Kopf andersherum als beim linken
            m.bone(horn, head, pivot=(-x * sx, -3, 0.5), rotation=(40, -spread * sx, 0))
            m.cube(horn, (-0.75, -0.75, 0), (1.5, 1.5, 3.5), "horn")
            m.bone(horn + "_tip", horn, pivot=(0, 0, 3.5), rotation=(35, 0, 0))
            m.cube(horn + "_tip", (-0.5, -0.5, 0), (1, 1, 3), "horn_tip")

    # --- Joch, Mantelkragen, Schulterplatten, Dornen
    m.bone("shoulders")
    m.cube("shoulders", (-12, 6, -2), (24, 3, 4), "yoke")
    m.cube("shoulders", (-1.5, 4, -0.5), (3, 2.2, 3), "vertebra")
    m.cube("shoulders", (-10, 5.4, 0.2), (20, 1.2, 3.4), "mantle")
    m.cube("shoulders", (-14.5, 5.5, -3), (5.5, 2.5, 6), "pauldron")
    m.cube("shoulders", (9, 5.5, -3), (5.5, 2.5, 6), "pauldron")
    for side, sx in (("right", -1), ("left", 1)):
        m.bone(f"{side}_spike_1", "shoulders", pivot=(13.5 * sx, 5.8, -1.5), rotation=(-15, 0, 30 * sx))
        m.cube(f"{side}_spike_1", (-0.6, -4, -0.6), (1.2, 4, 1.2), "spike")
        m.bone(f"{side}_spike_2", "shoulders", pivot=(12.8 * sx, 5.8, 1.6), rotation=(22, 0, 24 * sx))
        m.cube(f"{side}_spike_2", (-0.5, -3.2, -0.5), (1, 3.2, 1), "spike")

    # --- Brustkorb (schwankt), Herz, Becken, Mantel, Schwanz
    m.bone("ribcage", pivot=(0, 8.5, 0.5), rotation=(12, 0, 0))
    m.cube("ribcage", (-5, 0, -3.5), (10, 8, 6), "ribs")
    m.cube("ribcage", (-1, -0.5, 1.7), (2, 10, 1.8), "spine")
    m.cube("ribcage", (-4.5, 8.5, -2), (9, 2.5, 4), "pelvis")
    m.bone("heart", "ribcage", pivot=(0, 3.5, -0.5))
    m.cube("heart", (-2, -2, -1.75), (4, 4, 3.5), "heart")
    m.bone("cape", "ribcage", pivot=(0, -2.5, 3.2), rotation=(4, 0, 0))
    m.cube("cape", (-9, 0, 0), (18, 17, 0.5), "cape")
    m.bone("tail", "ribcage", pivot=(0, 10.5, 0.8), rotation=(28, 0, 0))
    m.cube("tail", (-1.25, 0, -1.25), (2.5, 3.5, 2.5), "vertebra")
    m.cube("tail", (-0.4, 0.4, 1.25), (0.8, 2, 1.5), "fin")
    m.bone("tail2", "tail", pivot=(0, 3.5, 0), rotation=(18, 0, 0))
    m.cube("tail2", (-1, 0, -1), (2, 3, 2), "vertebra")
    m.cube("tail2", (-0.35, 0.3, 1), (0.7, 1.6, 1.2), "fin")
    m.bone("tail3", "tail2", pivot=(0, 3, 0), rotation=(18, 0, 0))
    m.cube("tail3", (-0.75, 0, -0.75), (1.5, 2.5, 1.5), "vertebra")
    m.cube("tail3", (-1.5, 2.5, -0.25), (3, 4, 0.5), "blade")

    # --- Arme mit Krallenhänden
    for side, sx in (("right", -1), ("left", 1)):
        arm = f"{side}_arm"
        m.bone(arm, pivot=(11 * sx, 8, 0), rotation=(-8, 0, -14 * sx))
        m.cube(arm, (-1, -0.5, -1), (2, 7, 2), "arm")
        m.cube(arm, (-1.25, 6, -1.25), (2.5, 1.5, 2.5), "knob")
        m.bone(f"{side}_forearm", arm, pivot=(0, 7, 0), rotation=(-38, 0, 0))
        m.cube(f"{side}_forearm", (-0.8, 0, -0.8), (1.6, 6.5, 1.6), "arm")
        m.bone(f"{side}_hand", f"{side}_forearm", pivot=(0, 6.5, 0), rotation=(-25, 0, 0))
        m.cube(f"{side}_hand", (-1.3, 0, -1), (2.6, 1.5, 2), "hand")
        for cx, length in ((-1.0, 3.2), (-0.3, 3.8), (0.4, 3.2)):
            m.cube(f"{side}_hand", (cx, 1.4, -0.95), (0.6, length, 0.6), "claw")
        m.cube(f"{side}_hand", (1.0 if sx < 0 else -1.6, 0.6, -1.2), (0.6, 2.2, 0.6), "claw")      # Daumen innen
    return m


# =============================================================================================
# Hilfen
# =============================================================================================

def _awake(ctx: Ctx) -> bool:
    return ctx.params.get("variant") == "aschenkoenig_erwachen"


def _soul(ctx: Ctx, v: float):
    """Seelenfeuer (leuchtet)."""
    col = pick(ctx.pal["soul"], v)
    ctx.glow = col
    return col


_AXIS = {"west": 0, "east": 0, "top": 1, "bottom": 1, "front": 2, "back": 2}
_POSITIVE = {"east", "bottom", "back"}


def _uv(ctx: Ctx) -> tuple[float, float]:
    """Lage des Texels auf seiner Seite (0..1, Spalte und Zeile)."""
    return (ctx.s + 0.5) / max(ctx.w, 1), (ctx.t + 0.5) / max(ctx.h, 1)


def _pointed(ctx: Ctx, axis: int, sign: int, start: float = 0.3, sharp: float = 0.92) -> bool:
    """True, wenn das Texel wegfällt, damit der Quader zur Spitze hin schmal wird (Dornen, Zacken, Krallen, Hörner).

    axis: Längsachse (0 = x, 1 = y, 2 = z), sign: +1 Spitze am großen Ende, -1 am kleinen."""
    o, size, p = ctx.cube.origin, ctx.cube.size, ctx.local
    n = _AXIS[ctx.lface]
    if n == axis:
        return (ctx.lface in _POSITIVE) == (sign > 0)                     # die Stirnseite an der Spitze fehlt
    f = (p[axis] - o[axis]) / size[axis]
    if sign < 0:
        f = 1.0 - f
    if f <= start:
        return False
    m = 3 - axis - n
    d = abs(p[m] - (o[m] + size[m] / 2)) / (size[m] / 2)
    return d > 1.0 - (f - start) / (1.0 - start) * sharp


def _char(ctx: Ctx, base: float = 0.5, seed: int = 0):
    """Verkohlter Knochen: matt, fleckig, Asche auf den Oberseiten, selten ein Glutnest (beim Erwachen glüht alles mit)."""
    x, y, z = ctx.world
    n = noise3(x * 0.4, y * 0.4, z * 0.4, 801 + seed)
    v = base + (n - 0.5) * 0.18 + (hash01(ctx.u, ctx.v, 802) - 0.5) * 0.07
    if ctx.face == "top":
        v += 0.08
        if hash01(ctx.u, ctx.v, 803) > 0.35:
            col = pick(ctx.pal["ash"], 0.25 + hash01(ctx.u, ctx.v, 804) * 0.5)
            return _awake_glow(ctx, col)
    elif ctx.face == "bottom":
        v -= 0.16
    col = pick(ctx.pal["bone"], v)
    nest = noise3(x * 0.55 + 7, y * 0.55, z * 0.55, 805 + seed)
    if nest > 0.92 and ctx.face != "top":
        return _soul(ctx, 0.3 + (nest - 0.92) * 4.0)
    if nest > 0.86:
        col = mix(col, pick(ctx.pal["soul"], 0.1), 0.5)
    return _awake_glow(ctx, col)


def _awake_glow(ctx: Ctx, col):
    """Beim Erwachen leuchten auch die Knochen schwach mit."""
    if _awake(ctx):
        ctx.glow = mix(col, (0, 0, 0, 255), 0.55)
    return col


_CRACKS = Voronoi3D(2.8, 77)


def _crack(ctx: Ctx, col, seed: int, rate: float = 0.045):
    """Risse, in denen Seelenfeuer glimmt: ein Netz aus Zellrändern, nur stellenweise aufgebrochen (beim Erwachen überall).
    rate: wie viel davon aufbricht (0,045 ≈ ein gutes Drittel)."""
    x, y, z = ctx.world
    _, edge = _CRACKS(x + seed * 0.37, y - seed * 0.11, z)
    open_ = noise3(x * 0.32 + seed, y * 0.32, z * 0.32, 905) > (0.45 if _awake(ctx) else 0.74 - rate * 1.5)
    if edge < 0.24 and open_:
        return _soul(ctx, 0.42 + (1.0 - edge / 0.24) * 0.3)
    if edge < 0.4 and open_:
        return mix(col, pick(ctx.pal["soul"], 0.08), 0.6)                  # angeglühter Rand
    return col


# =============================================================================================
# Köpfe
# =============================================================================================

def _center(ctx: Ctx) -> bool:
    return ctx.cube.size[0] > 8


def _socket(u: float, v: float, cu: float, cv: float, rx: float, ry: float, tilt: float) -> float:
    """Abstand (1 = Rand) in einer schräg gestellten Augenhöhle."""
    du, dv = u - cu, v - cv
    c, s = math.cos(tilt), math.sin(tilt)
    a, b = du * c + dv * s, -du * s + dv * c
    return math.sqrt((a / rx) ** 2 + (b / ry) ** 2)


def _skull(ctx: Ctx):
    face = ctx.lface
    center = _center(ctx)
    u, v = _uv(ctx)
    if face == "front":
        # zornig schräge Augenhöhlen, darin Seelenfeuer (beim Mittelkopf größer und heller)
        rx, ry = (0.155, 0.115) if center else (0.18, 0.135)
        for cu, tilt in ((0.29, 0.28), (0.71, -0.28)):
            d = _socket(u, v, cu, 0.47, rx, ry, tilt)
            if d < 1.0:
                if d < (0.5 if center else 0.42):
                    return _soul(ctx, 0.99 - d * 0.6)
                if d < 0.7:
                    return _soul(ctx, 0.3)
                return pick(ctx.pal["socket"], 0.2 + d * 0.3)
            if d < 1.25:
                return pick(ctx.pal["bone"], 0.18)                        # Schattenrand der Höhle
        # Nasenhöhle (umgedrehtes Herz) mit Steg
        if 0.6 <= v <= 0.74:
            half = 0.035 + (v - 0.6) * 0.55
            if abs(u - 0.5) < half:
                if abs(u - 0.5) < 0.02 and v > 0.66:
                    return pick(ctx.pal["bone"], 0.3)
                return pick(ctx.pal["socket"], 0.25)
        # obere Zahnreihe; die Eckzähne reichen tiefer
        if v > 0.8:
            col_i = int(u * ctx.cube.size[0])
            edge = (ctx.s % 2 == 1) if ctx.w // ctx.cube.size[0] >= 2 else False
            if u < 0.12 or u > 0.88:
                return pick(ctx.pal["bone"], 0.2)
            if v > 0.93 and col_i not in (2, ctx.cube.size[0] - 3):
                return pick(ctx.pal["socket"], 0.15)
            col = pick(ctx.pal["ash"], 0.45 + (0.25 if col_i % 2 else 0.12))
            return mix(col, pick(ctx.pal["bone"], 0.2), 0.5) if edge else col
        # Wangenknochen
        for cu in (0.2, 0.8):
            if (u - cu) ** 2 + ((v - 0.67) * 1.4) ** 2 < 0.012:
                return _char(ctx, 0.66)
        # glühender Riss über die Stirn (beim Mittelkopf bis ins rechte Auge): zwei Texel breit, mit angeglühtem Rand
        if center and v < 0.38:
            path = 0.6 + v * 0.22 + (0.04 if int(v * 9) % 2 else -0.01)
            if abs(u - path) < 0.06:
                return _soul(ctx, 0.62 + (0.06 - abs(u - path)) * 4.0)
            if abs(u - path) < 0.1:
                return mix(_char(ctx, 0.45), pick(ctx.pal["soul"], 0.15), 0.6)
        return _crack(ctx, _char(ctx, 0.5 - abs(u - 0.5) * 0.25), 811)
    if face == "top":
        if center:
            # in der Krone: Asche, in der Mitte (wo das Seelenfeuer steht) Glut
            r = math.hypot(u - 0.5, v - 0.5)
            if r < 0.24:
                return _soul(ctx, 0.85 - r * 1.6 + hash01(ctx.u, ctx.v) * 0.1)
            if r < 0.34 and hash01(ctx.u, ctx.v, 812) > 0.5:
                return _soul(ctx, 0.35)
            return pick(ctx.pal["ash"], 0.2 + hash01(ctx.u, ctx.v, 813) * 0.45)
        # Schädelnähte
        if abs(u - 0.5) < 0.04 + 0.02 * math.sin(v * 30) or abs(v - 0.62) < 0.035:
            return _soul(ctx, 0.45) if hash01(ctx.u, ctx.v, 814) > 0.4 else pick(ctx.pal["bone"], 0.15)
        return _char(ctx, 0.56)
    if face == "bottom":
        # vorn der Gaumen: glüht im offenen Maul
        if v > 0.45 and 0.15 < u < 0.85:
            return _soul(ctx, 0.25 + (v - 0.45) * 0.9)
        return pick(ctx.pal["bone"], 0.12 + hash01(ctx.u, ctx.v) * 0.06)
    if face in ("west", "east"):
        front = 1.0 - u if face == "west" else u                          # 1 = vorn
        if front > 0.78 and 0.35 < v < 0.62:
            return mix(_char(ctx, 0.4), pick(ctx.pal["bone"], 0.1), 0.5)    # Schläfengrube
        if v > 0.9:
            return pick(ctx.pal["bone"], 0.16)
        return _crack(ctx, _char(ctx, 0.46), 815)
    return _crack(ctx, _char(ctx, 0.42), 816)


def _brow(ctx: Ctx):
    if ctx.lface == "bottom":
        return pick(ctx.pal["bone"], 0.08)
    if ctx.lface == "front":
        return _crack(ctx, _char(ctx, 0.62 if ctx.t < ctx.h // 2 else 0.5), 821, 0.03)
    return _char(ctx, 0.58)


def _jaw(ctx: Ctx):
    face = ctx.lface
    u, v = _uv(ctx)
    if face == "front":
        if v < 0.4:                                                       # untere Zahnreihe
            if u < 0.1 or u > 0.9:
                return pick(ctx.pal["bone"], 0.2)
            col_i = int(u * ctx.cube.size[0] * 2)
            return pick(ctx.pal["ash"], 0.42 + (0.22 if col_i % 2 else 0.08))
        return _char(ctx, 0.46)
    if face == "top":
        # Mundboden: Seelenfeuer, vorn die Zahnreihe
        if v > 0.82:
            return pick(ctx.pal["ash"], 0.5)
        if 0.12 < u < 0.88:
            return _soul(ctx, 0.3 + v * 0.55)
        return pick(ctx.pal["socket"], 0.2)
    if face == "bottom":
        return _char(ctx, 0.3)
    return _crack(ctx, _char(ctx, 0.44), 822, 0.03)


# --- Krone ------------------------------------------------------------------------------------

def _gold(ctx: Ctx, base: float):
    x, y, z = ctx.world
    v = base + (noise3(x * 0.8, y * 0.8, z * 0.8, 831) - 0.5) * 0.22 + (hash01(ctx.u, ctx.v, 832) - 0.5) * 0.08
    col = pick(ctx.pal["gold"], v)
    if noise3(x * 0.6 + 4, y * 0.6, z * 0.6, 833) > 0.68:
        col = mix(col, pick(ctx.pal["bone"], 0.1), 0.55)                    # angelaufen, verrußt
    return _awake_glow(ctx, col)


def _crown_band(ctx: Ctx):
    face = ctx.lface
    if face in ("top", "bottom"):
        return None
    u, v = _uv(ctx)
    if v < 0.18 or v > 0.82:
        return _gold(ctx, 0.72 if v < 0.18 else 0.5)                       # Ränder
    # Steine: vorn ein großer Seelenstein, an den Seiten kleine
    big = face == "front"
    r = math.hypot((u - 0.5) * (1.0 if big else 1.4), (v - 0.5) * 0.9)
    if r < (0.13 if big else 0.08):
        return _soul(ctx, 0.95 - r * 3.0)
    if r < (0.19 if big else 0.12):
        return _gold(ctx, 0.85)                                            # Fassung
    # eingravierte Rauten
    k = (u * 10.0) % 1.0
    if abs(k - 0.5) + abs(v - 0.5) * 1.6 < 0.28:
        return _gold(ctx, 0.62)
    return _gold(ctx, 0.36)


def _crown_spike(ctx: Ctx):
    if ctx.lface == "bottom" or _pointed(ctx, 1, -1, start=0.15, sharp=0.95):
        return None
    u, v = _uv(ctx)
    return _gold(ctx, 0.75 - v * 0.3 + (0.12 if u < 0.5 else 0.0))


_FLAME_TONGUES = ((0.5, 1.0, 0.24), (0.25, 0.68, 0.17), (0.76, 0.75, 0.16), (0.12, 0.42, 0.12), (0.9, 0.46, 0.11),
                  (0.38, 0.84, 0.13), (0.63, 0.9, 0.12))


def _flame_shape(u: float, v: float, seed: int) -> float | None:
    """Flammenbild: 0 am Rand … 1 im Kern; None außerhalb. v = 0 oben, 1 unten."""
    height = 0.0
    for i, (cu, h, w) in enumerate(_FLAME_TONGUES):
        cu += (hash01(i, seed) - 0.5) * 0.05
        q = 1.0 - ((u - cu) / w) ** 2
        if q > 0:
            height = max(height, h * math.sqrt(q))
    rise = 1.0 - v
    if rise >= height or height <= 0:
        return None
    return 1.0 - rise / height


def _soul_flame(ctx: Ctx):
    lx, ly, _ = ctx.local
    ox, oy, _ = ctx.cube.origin
    u = (lx - ox) / ctx.cube.size[0]
    v = (ly - oy) / ctx.cube.size[1]
    k = _flame_shape(u, v, 840 + (1 if ctx.cube.bone.name.endswith("2") else 0))
    if k is None:
        return None
    return _soul(ctx, 0.2 + k * 0.8)


# --- Hörner -----------------------------------------------------------------------------------

def _horn(ctx: Ctx, tip: bool):
    if tip and _pointed(ctx, 2, 1, start=0.2, sharp=0.9):
        return None
    if ctx.lface == "front" and not tip:
        return pick(ctx.pal["horn"], 0.2)                                   # Ansatz am Schädel
    z = ctx.local[2] - ctx.cube.origin[2]
    ring = int((z + (3.5 if tip else 0.0)) * 1.4) % 2
    v = 0.36 + ring * 0.14 + (hash01(ctx.u, ctx.v, 851) - 0.5) * 0.08
    if ctx.face == "top":
        v += 0.12
    elif ctx.face == "bottom":
        v -= 0.12
    col = pick(ctx.pal["horn"], v)
    if tip:
        f = z / ctx.cube.size[2]
        col = mix(col, pick(ctx.pal["ash"], 0.55), max(0.0, f - 0.45) * 1.6)  # helle Spitze
    return _awake_glow(ctx, col)


# =============================================================================================
# Rumpf
# =============================================================================================

def _yoke(ctx: Ctx):
    x = ctx.world[0]
    groove = abs(((x + 12.0) % 3.0) - 1.5) < 0.25                          # verwachsene Wirbel
    if ctx.face in ("front", "back") and groove:
        return pick(ctx.pal["bone"], 0.12)
    col = _char(ctx, 0.56 if not groove else 0.4, seed=1)
    if ctx.face == "front" and abs(((x + 12.0) % 3.0) - 0.0) < 0.6 and abs(ctx.local[1] - 7.5) < 0.6:
        col = _char(ctx, 0.72, seed=1)                                      # Knauf
    return _crack(ctx, col, 861)


def _vertebra(ctx: Ctx):
    y = ctx.local[1] - ctx.cube.origin[1]
    if ctx.lface not in ("top", "bottom") and (y % 1.25) < 0.3:
        return pick(ctx.pal["bone"], 0.1)
    return _crack(ctx, _char(ctx, 0.55, seed=2), 862, 0.03)


def _fin(ctx: Ctx):
    if _pointed(ctx, 2, 1, start=0.1, sharp=0.95):
        return None
    return _char(ctx, 0.6, seed=3)


def _pauldron(ctx: Ctx):
    y = ctx.local[1] - ctx.cube.origin[1]
    if ctx.lface not in ("top", "bottom"):
        if abs(y - 0.85) < 0.18 or abs(y - 1.7) < 0.18:
            return pick(ctx.pal["bone"], 0.1)                               # Plattenränder
        return _crack(ctx, _char(ctx, 0.62 - y * 0.08, seed=4), 863)
    return _crack(ctx, _char(ctx, 0.6, seed=4), 864)


def _spike(ctx: Ctx):
    if _pointed(ctx, 1, -1, start=0.1, sharp=0.95):
        return None
    f = 1.0 - (ctx.local[1] - ctx.cube.origin[1]) / ctx.cube.size[1]     # 1 = Spitze
    return mix(_char(ctx, 0.5, seed=5), pick(ctx.pal["ash"], 0.6), max(0.0, f - 0.5))


def _mantle(ctx: Ctx):
    """Der über das Joch geschlagene Mantel: Netherrot, unten mit Goldsaum."""
    if ctx.lface == "bottom":
        return pick(ctx.pal["lining"], 0.3)
    if ctx.lface != "top" and ctx.t >= ctx.h - 1:
        return _gold(ctx, 0.62)
    return _cloth(ctx, 0.55)


def _cloth(ctx: Ctx, base: float, key: str = "cloth"):
    x, y, z = ctx.world
    n = noise3(x * 0.5, y * 0.25, z * 0.5, 871)
    v = base + math.sin(x * 1.1 + n * 3.0) * 0.08 + (n - 0.5) * 0.14 + (hash01(ctx.u, ctx.v, 872) - 0.5) * 0.06
    return _awake_glow(ctx, pick(ctx.pal[key], v))


def _ribs(ctx: Ctx):
    """Brustkorb: Schlüsselbeine, Brustbein, Rippenbögen mit Lücken, hinten die Wirbelsäule."""
    face = ctx.lface
    u, v = _uv(ctx)
    y = ctx.local[1] - ctx.cube.origin[1]                                 # 0 … 8 von oben
    if face == "bottom":
        return None
    if face == "top":
        if v < 0.15 or v > 0.85 or u < 0.08 or u > 0.92:
            return _char(ctx, 0.62, seed=6)
        return None
    if face == "front" and abs(u - 0.5) < 0.1 and y < 6.5:
        return _crack(ctx, _char(ctx, 0.66 if y < 6 else 0.5, seed=6), 873, 0.03)   # Brustbein
    if face == "back" and abs(u - 0.5) < 0.12:
        return _char(ctx, 0.6 if (y % 1.25) > 0.3 else 0.2, seed=6)                  # Wirbel
    if y < 0.8:
        return _char(ctx, 0.62, seed=6)                                                 # Schlüsselbeine
    # Rippen: Bögen, die zur Seite hin abfallen
    side = abs(u - 0.5) * 2.0 if face in ("front", "back") else 1.0
    yy = y - side * 0.6
    if yy % 1.8 < 0.95 and yy < 7.4:
        return _crack(ctx, _char(ctx, 0.58 - side * 0.1, seed=6), 874, 0.035)
    return None


def _spine(ctx: Ctx):
    y = ctx.local[1] - ctx.cube.origin[1]
    if ctx.lface not in ("top", "bottom") and (y % 1.25) < 0.3:
        return pick(ctx.pal["bone"], 0.1)
    return _char(ctx, 0.58, seed=7)


def _heart(ctx: Ctx):
    """Herz aus Seelenfeuer mit dunklen Adern, innen weißglühend."""
    x, y, z = ctx.local
    n = noise3(x * 1.3, y * 1.3, z * 1.3, 881)
    if 0.46 < n < 0.53:
        return pick(ctx.pal["soul"], 0.12)                                  # Adern
    u, v = _uv(ctx)
    core = 1.0 - min(1.0, math.hypot(u - 0.5, v - 0.5) * 2.2)
    return _soul(ctx, 0.4 + core * 0.45 + (hash01(ctx.u, ctx.v) - 0.5) * 0.08)


def _pelvis(ctx: Ctx):
    """Becken: Beckenschaufeln, vorn der Schambogen, an den Seiten die Hüftpfannen, hinten das Kreuzbein."""
    u, v = _uv(ctx)
    face = ctx.lface
    if face == "front":
        if abs(u - 0.5) < 0.09 and v > 0.35:
            return None                                                     # Schambogen
        if v < 0.3:
            return _char(ctx, 0.66, seed=8)                                 # Kamm der Schaufeln
        return _char(ctx, 0.52 - abs(u - 0.5) * 0.2, seed=8)
    if face in ("west", "east"):
        if (u - 0.5) ** 2 + ((v - 0.6) * 0.9) ** 2 < 0.05:
            return pick(ctx.pal["socket"], 0.3)                             # Hüftpfanne
    if face == "back" and abs(u - 0.5) < 0.12:
        return _char(ctx, 0.7, seed=8)                                      # Kreuzbein
    return _crack(ctx, _char(ctx, 0.56, seed=8), 882)


def _crown_mark(u: float, v: float) -> bool:
    """Die Dreizackkrone auf dem Mantel (u, v im Feld 0..1)."""
    if not (0.0 <= u <= 1.0 and 0.0 <= v <= 1.0):
        return False
    if v > 0.62:
        return v < 0.95                                                    # Reif
    for cu, top in ((0.12, 0.25), (0.5, 0.0), (0.88, 0.25)):
        half = 0.11 * (v - top) / (0.62 - top) if v >= top else -1.0
        if abs(u - cu) <= half + 0.02:
            return True
    return False


def _cape(ctx: Ctx):
    """Königsmantel: außen Netherrot mit Goldborte und der Dreizackkrone, innen dunkles Futter; unten verbrannt."""
    face = ctx.lface
    x, y, z = ctx.local
    ox, oy, _ = ctx.cube.origin
    u = (x - ox) / ctx.cube.size[0]
    v = (y - oy) / ctx.cube.size[1]
    if face == "top":
        return _gold(ctx, 0.6)
    # ausgefranster, verbrannter Saum
    col_i = int(u * 18)
    hem = 0.72 + hash01(col_i, 891) * 0.22 + (0.06 if col_i % 3 == 0 else 0.0)
    if v > hem or face == "bottom":
        return None
    burn = noise3(x * 0.45 + 13, y * 0.45, 2.0, 892)
    if v > 0.45 and burn > 0.77:
        return None                                                         # Brandloch
    if v > hem - 0.035 or (v > 0.45 and burn > 0.73):
        return _soul(ctx, 0.35 + hash01(ctx.u, ctx.v, 893) * 0.4)           # glimmender Brandrand
    if v > hem - 0.08 or (v > 0.45 and burn > 0.7):
        return pick(ctx.pal["lining"], 0.05)                                # verkohlt
    if face == "front":                                                     # innen (zum Körper hin)
        return _cloth(ctx, 0.4, "lining")
    if face in ("west", "east"):
        return _gold(ctx, 0.5)
    # außen (hinten): Goldborte, Krone
    if u < 0.05 or u > 0.95 or v < 0.04:
        return _gold(ctx, 0.66)
    if u < 0.08 or u > 0.92 or v < 0.065:
        return _cloth(ctx, 0.2)
    if _crown_mark((u - 0.3) / 0.4, (v - 0.12) / 0.2):
        return _gold(ctx, 0.62 + hash01(ctx.u, ctx.v) * 0.1)
    return _cloth(ctx, 0.5)


def _blade(ctx: Ctx):
    """Knochenklinge am Schwanzende: gezahnte Ränder, Spitze unten, eine glimmende Hohlkehle."""
    if _pointed(ctx, 1, 1, start=0.45, sharp=0.97):
        return None
    u, v = _uv(ctx)
    y = ctx.local[1] - ctx.cube.origin[1]
    if ctx.lface in ("front", "back"):
        if (u < 0.12 or u > 0.88) and (y % 1.0) < 0.35:
            return None                                                     # Zähne
        if abs(u - 0.5) < 0.07 and v < 0.75:
            return _soul(ctx, 0.5)                                          # Hohlkehle
    return _char(ctx, 0.62 - v * 0.1, seed=9)


def _arm(ctx: Ctx):
    return _crack(ctx, _char(ctx, 0.52, seed=10), 901, 0.04)


def _knob(ctx: Ctx):
    return _crack(ctx, _char(ctx, 0.66, seed=11), 902, 0.05)


def _hand(ctx: Ctx):
    if ctx.lface == "bottom":
        return pick(ctx.pal["bone"], 0.15)
    return _char(ctx, 0.56, seed=12)


def _claw(ctx: Ctx):
    if _pointed(ctx, 1, 1, start=0.35, sharp=0.9):
        return None
    f = (ctx.local[1] - ctx.cube.origin[1]) / ctx.cube.size[1]            # 1 = Spitze
    col = _char(ctx, 0.55, seed=13)
    return mix(col, pick(ctx.pal["horn"], 0.15), max(0.0, f - 0.4) * 1.6)   # dunkle Krallenspitzen


def materials(variant: str) -> dict:
    return {
        "skull": _skull, "brow": _brow, "jaw": _jaw,
        "crown_band": _crown_band, "crown_spike": _crown_spike, "soul_flame": _soul_flame,
        "horn": lambda ctx: _horn(ctx, False), "horn_tip": lambda ctx: _horn(ctx, True),
        "yoke": _yoke, "vertebra": _vertebra, "fin": _fin, "mantle": _mantle, "pauldron": _pauldron, "spike": _spike,
        "ribs": _ribs, "spine": _spine, "heart": _heart, "pelvis": _pelvis, "cape": _cape, "blade": _blade,
        "arm": _arm, "knob": _knob, "hand": _hand, "claw": _claw,
    }


def hidden(variant: str) -> list[str]:
    return []


def shield_texture(size: int = 64):
    """Seelenschild (wenn der Aschenkönig nur noch halbe Kraft hat): kachelbare Schlieren aus Seelenfeuer auf Schwarz.
    Das Spiel zeichnet die Hülle additiv und lässt das Bild über sie wandern; Schwarz bleibt unsichtbar."""
    from PIL import Image

    img = Image.new("RGBA", (size, size), (0, 0, 0, 255))
    px = img.load()
    soul = [hexc(c) for c in ("#000000", "#021412", "#052a27", "#09463f", "#0f6b62", "#1c9488", "#3bbcb0", "#86e0d8")]
    for y in range(size):
        for x in range(size):
            a = 2.0 * math.pi * x / size
            b = 2.0 * math.pi * y / size
            # zwei verschlungene Schlierenfelder (ganzzahlige Frequenzen: das Bild kachelt nahtlos)
            w1 = math.sin(2 * a + 1.4 * math.sin(b) + 0.8 * math.sin(3 * b + a))
            w2 = math.sin(3 * b - 1.2 * math.sin(2 * a) + 0.6 * math.cos(a + 2 * b))
            v = max(0.0, w1) ** 3 * 0.75 + max(0.0, w2) ** 4 * 0.6
            v += 0.08 * (0.5 + 0.5 * math.sin(5 * a + 4 * b))
            px[x, y] = soul[max(0, min(len(soul) - 1, int(v * len(soul))))]
    return img
