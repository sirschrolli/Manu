"""
Tintenfisch und Tiefseekrake (statt Tintenfisch und Leuchttintenfisch) auf dem Skelett des Vanilla-Tintenfischs: gleiche
Knochen und Drehpunkte ("body" bei (0, 8, 0), acht Fangarme "tentacle0" … "tentacle7" im Kreis mit Radius 5 auf
Höhe 15, je um die eigene Achse gedreht), damit SquidModel die Arme genauso schwingen lässt und der SquidRenderer den
Leib in Schwimmrichtung neigt.

Anders als Vanillas Würfel ist der Leib ein echter Kalmar: ein spitz zulaufender Mantel mit zwei dreieckigen
Flossen an der Spitze, ein abgesetzter Kopf mit großen, vorstehenden Augen an beiden Seiten und ein Kranz aus
Fangarmen mit Saugnäpfen auf der Innenseite.

  * tintenfisch:   ziegelrot bis umbra, dicht mit dunklen Farbzellen getupft, heller Bauch, große goldene Augen mit
                   schwarzer Pupille, blassrosa Saugnäpfe
  * tiefseekrake:  tiefes Indigo mit grünblauem Schimmer, Reihen leuchtender Lichtpunkte entlang des Mantels,
                   leuchtende Ringe an den Armen und leuchtende Flossensäume, kalt leuchtende Augen
"""
from __future__ import annotations

import math

from ..geometry import Model
from ..paint import Ctx, hash01, mix, noise3, pick, ramp

PALETTES = {
    "tintenfisch": {
        "skin": ramp("#3a0e0a", "#d8704a", steps=8), "belly": ramp("#8a4a3a", "#f4d4c0"), "spot": ramp("#2a0806", "#7a2014"),
        "fin": ramp("#5a1a12", "#e8885a"), "iris": ramp("#5a3a06", "#ffd25a"), "pupil": ramp("#020202", "#141010"),
        "sucker": ramp("#a85a50", "#f8d8cc"), "tip": ramp("#2a0806", "#8a3020"), "back": ramp("#3a0e0a", "#d8704a"),
    },
    "tiefseekrake": {
        "skin": ramp("#06081e", "#3a5a9a", steps=8), "belly": ramp("#121a3a", "#5a7ab0"), "spot": ramp("#03040e", "#141c3a"),
        "fin": ramp("#0a1030", "#4a6ab0"), "iris": ramp("#0a6a7a", "#d8ffff"), "pupil": ramp("#020208", "#0a1020"),
        "sucker": ramp("#2a3a6a", "#8ab0e0"), "tip": ramp("#04061a", "#1a2a5a"), "light": ramp("#1ac8d8", "#e0ffff"),
        "back": ramp("#06081e", "#3a5a9a"),
    },
}


def build() -> Model:
    m = Model("squid", 128, 64)
    m.world_faces = True
    # Mantel mit Spitze, Kopf mit Augen, zwei Flossen (Drehpunkt wie Vanilla: der Leib reicht von y 0 bis 16)
    m.bone("body", pivot=(0, 8, 0))
    m.cube("body", (-5, -6, -5), (10, 10, 10), "mantle", inflate=0.02)        # Mantel (spitz zulaufend nach oben)
    m.cube("body", (-4, -10, -4), (8, 4, 8), "mantle_mid")
    m.cube("body", (-2.5, -13, -2.5), (5, 3, 5), "mantle_tip")
    m.cube("body", (-4.5, 4, -4.5), (9, 4, 9), "head", inflate=0.02)          # Kopf mit Augen
    m.cube("body", (-5.5, 4.5, -2), (1, 3, 4), "eye")
    m.cube("body", (4.5, 4.5, -2), (1, 3, 4), "eye")
    m.cube("body", (-11, -14, -0.5), (7, 11, 1), "fin")                       # zwei Rautenflossen an der Mantelspitze
    m.cube("body", (4, -14, -0.5), (7, 11, 1), "fin")
    # acht Fangarme (Lage und Drehung wie Vanilla)
    for i in range(8):
        angle = i * math.pi * 2.0 / 8.0
        x = math.cos(angle) * 5.0
        z = math.sin(angle) * 5.0
        y_rot = math.degrees(i * math.pi * -2.0 / 8.0 + math.pi / 2.0)
        name = f"tentacle{i}"
        m.bone(name, pivot=(x, 15, z), rotation=(0, y_rot, 0))
        m.cube(name, (-1, 0, -1), (2, 18, 2), "arm")
    return m


def preview_pose(variant: str) -> dict:
    """Die Arme leicht gespreizt, wie beim Schwimmen."""
    return {f"tentacle{i}": (16, 0, 0) for i in range(8)}


# =============================================================================================

def _variant(ctx: Ctx) -> str:
    return ctx.params.get("variant")


def _deep(ctx: Ctx) -> bool:
    return _variant(ctx) == "tiefseekrake"


def _glow(ctx: Ctx, col, dim: float = 0.25):
    ctx.glow = mix(col, (0, 0, 0, 255), dim)
    return col


def _shade(ctx: Ctx, v: float) -> float:
    if ctx.face == "top":
        return v + 0.06
    if ctx.face == "bottom":
        return v - 0.1
    if ctx.face == "back":
        return v - 0.04
    return v


def _cells(ctx: Ctx, density: float, seed: int) -> bool:
    """Farbzellen: auf einem verschobenen Zweier-Raster, nicht als Rauschen."""
    s, t = ctx.s, ctx.t
    if (s + (t // 2)) % 2 or t % 2:
        return False
    return hash01(ctx.u // 2, ctx.v // 2, seed) < density


def _skin(ctx: Ctx, base: float, seed: int = 11) -> tuple:
    x, y, z = ctx.world
    v = base + (noise3(x * 0.25, y * 0.25, z * 0.25, seed) - 0.5) * 0.12
    return pick(ctx.pal["skin"], _shade(ctx, v))


def _mantle(ctx: Ctx):
    """Mantel: oben (zur Spitze hin) dunkler, an der Unterseite heller; Farbzellen bzw. Lichtpunkt-Reihen."""
    x, y, z = ctx.world
    up = max(0.0, min(1.0, (12 - y) / 16.0))                 # 0 am Kopf, 1 an der Spitze
    v = 0.52 - up * 0.12
    col = _skin(ctx, v + (0.06 if ctx.face == "front" else 0.0))   # Bauchseite nur wenig heller
    if _deep(ctx):
        # Lichtpunkte in senkrechten Reihen auf jeder Seite, dazwischen ein feiner Schimmer
        if ctx.face in ("front", "back", "west", "east") and ctx.s % 3 == 1 and ctx.t % 3 == 1:
            if hash01(ctx.u, ctx.v, 21) < 0.75:
                return _glow(ctx, pick(ctx.pal["light"], 0.55 + hash01(ctx.u, 22) * 0.4), 0.2)
        if ctx.face in ("west", "east") and ctx.s % 4 == 0:
            return pick(ctx.pal["skin"], _shade(ctx, v + 0.08))
        return col
    if _cells(ctx, 0.5, 23):
        return pick(ctx.pal["spot"], 0.35 + hash01(ctx.u, ctx.v, 24) * 0.45)
    if hash01(ctx.u, ctx.v, 26) < 0.06:
        return pick(ctx.pal["spot"], 0.75)                     # kleine, helle Farbzellen dazwischen
    if ctx.face == "top":
        return _skin(ctx, 0.4)
    return col


def _mantle_mid(ctx: Ctx):
    if ctx.face == "bottom":
        return None
    return _mantle(ctx)


def _mantle_tip(ctx: Ctx):
    if ctx.face == "bottom":
        return None
    if _deep(ctx):
        if ctx.face == "top" and (ctx.s + ctx.t) % 4 == 0:
            return _glow(ctx, pick(ctx.pal["light"], 0.6), 0.3)
        return _skin(ctx, 0.42)
    return _skin(ctx, 0.36) if not _cells(ctx, 0.5, 25) else pick(ctx.pal["spot"], 0.5)


def _head(ctx: Ctx):
    """Kopf zwischen Mantel und Armen: etwas schmaler, mit Hautfalte zum Mantel hin."""
    if ctx.face == "top":
        return _skin(ctx, 0.3)
    if ctx.t == 0:
        return _skin(ctx, 0.34)                              # Mantelrand
    if ctx.face == "bottom":
        # Mundöffnung in der Mitte, Kranz um den Armansatz
        x, _, z = ctx.local
        r = math.hypot(x, z)
        if r < 1.5:
            return pick(ctx.pal["spot"], 0.2)
        if r < 2.6:
            return pick(ctx.pal["sucker"], 0.35)
        return _skin(ctx, 0.4)
    if ctx.face == "front":
        return _skin(ctx, 0.54)
    if _deep(ctx) and ctx.s % 3 == 1 and ctx.t == 2:
        return _glow(ctx, pick(ctx.pal["light"], 0.7), 0.25)
    return _skin(ctx, 0.48)


def _eye(ctx: Ctx):
    """Großes, vorstehendes Auge: Iris im Kreis, schwarze Pupille, ein heller Glanzpunkt."""
    if ctx.lface not in ("west", "east"):
        return _skin(ctx, 0.36)
    s, t = ctx.s, ctx.t                                       # 4 breit, 3 hoch
    cx, cy = 1.5, 1.0
    d = math.hypot(s - cx, (t - cy) * 1.2)
    if d < 0.9:
        col = pick(ctx.pal["pupil"], 0.5)
        return col
    if (s, t) == (1, 0):
        hl = (250, 248, 236, 255)
        return _glow(ctx, hl, 0.1) if _deep(ctx) else hl
    col = pick(ctx.pal["iris"], 0.62 + (0.2 if t == 0 else 0.0) - (0.18 if t == 2 else 0.0))
    if _deep(ctx):
        return _glow(ctx, col, 0.3)
    return col


def _fin(ctx: Ctx):
    """Dreieckige Flosse: breit am Mantel, spitz nach außen; Saum heller (bzw. leuchtend)."""
    if ctx.lface in ("top", "bottom"):
        return None
    x, y, _ = ctx.local
    ox = ctx.cube.origin[0]
    w = ctx.cube.size[0]
    oy = ctx.cube.origin[1]
    h = ctx.cube.size[1]
    out = (x - ox) / w if ox < 0 else 1 - (x - ox) / w          # 0 am Mantel, 1 außen
    out = 1 - out
    rel = (y - oy) / h                                        # 0 oben (Spitze), 1 unten
    # Dreieck: außen nur in der Mitte der Höhe
    half = 0.5 * (1.0 - out)
    if abs(rel - 0.42) > half + 0.06:
        return None
    edge = abs(abs(rel - 0.42) - half) < 0.16 or out > 0.85
    if edge:
        if _deep(ctx):
            return _glow(ctx, pick(ctx.pal["light"], 0.62), 0.3)
        return pick(ctx.pal["fin"], 0.78)
    # feine Strahlen in der Flosse
    v = 0.48 + (0.08 if int(rel * 8) % 2 == 0 else 0.0) - out * 0.06
    return pick(ctx.pal["fin"], v)


def _arm(ctx: Ctx):
    """Fangarm: zur Spitze dunkler und dünner wirkend; innen zwei Reihen Saugnäpfe (bzw. Lichtringe)."""
    rel = (ctx.local[1] - ctx.cube.origin[1]) / ctx.cube.size[1]   # 0 am Kopf, 1 an der Spitze
    lf = ctx.lface
    if lf == "top":
        return _skin(ctx, 0.4)
    if lf == "bottom":
        return pick(ctx.pal["tip"], 0.45)
    if _deep(ctx) and ctx.t % 6 == 3 and rel < 0.9 and lf != "front":
        return _glow(ctx, pick(ctx.pal["light"], 0.35 + (1 - rel) * 0.35), 0.45)    # schwache Lichtringe
    if lf == "front":                                          # Innenseite (zur Mitte)
        if ctx.t % 2 == (ctx.s % 2) and rel < 0.95:
            sz = 0.75 - rel * 0.3
            return pick(ctx.pal["sucker"], sz)                 # Saugnäpfe, versetzt in zwei Reihen
        return pick(ctx.pal["belly"], 0.42 - rel * 0.18)
    v = 0.5 - rel * 0.3
    if rel > 0.82:
        return pick(ctx.pal["tip"], 0.5 - (rel - 0.82) * 1.5)
    col = pick(ctx.pal["skin"], v + (0.05 if lf in ("west",) else 0.0))
    if not _deep(ctx) and _cells(ctx, 0.35, 31):
        return pick(ctx.pal["spot"], 0.5)                      # vereinzelte Farbzellen auch an den Armen
    return col


def materials(variant: str) -> dict:
    return {"mantle": _mantle, "mantle_mid": _mantle_mid, "mantle_tip": _mantle_tip, "head": _head, "eye": _eye, "fin": _fin, "arm": _arm}
