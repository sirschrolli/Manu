"""
Gürteltiere auf dem Skelett des Vanilla-Gürteltiers (Einrollen, Spähen und Aufrollen bleiben): Panzer mit Gürteln
und Schilden, spitze Schnauze, Löffelohren, beschuppter Schwanz. Arten: Neunbinden-Gürteltier (graurosa),
Kugelgürteltier (gelbbraun).
"""
from __future__ import annotations

from ..geometry import Model
from ..paint import Ctx, hash01, mix, pick, ramp, rel_y
from . import farm

BREEDS = ["neunbinden", "kugel"]

_C = {"eye": ramp("#000000", "#3a3a3a"), "claw": ramp("#1a1612", "#5a5048")}

PALETTES = {
    "armadillo_neunbinden": {**_C, "shell": ramp("#5a4648", "#c8a8a4"), "band": ramp("#3a2a2c", "#8a6a6a"), "skin": ramp("#8a6a68", "#e8c0b8"),
                             "hair": ramp("#8a8078", "#d8d0c8")},
    "armadillo_kugel": {**_C, "shell": ramp("#6a4e24", "#e0b86a"), "band": ramp("#4a3418", "#a07a44"), "skin": ramp("#8a6a48", "#e8c8a0"),
                        "hair": ramp("#8a7a60", "#d8c8a8")},
}
for _p in PALETTES.values():
    _p["back"] = _p["shell"]


def build() -> Model:
    m = Model("armadillo", 128, 64)
    m.bone("body", pivot=(0, 21, 4))
    m.cube("body", (-4, -7, -10), (8, 8, 12), "shell", inflate=0.3)
    m.cube("body", (-4, -7, -10), (8, 8, 12), "underside")
    m.bone("tail", "body", pivot=(0, -3, 1), rotation=(29, 0, 0))
    m.cube("tail", (-0.5, -0.0865, 0.0933), (1, 6, 1), "tail")
    m.bone("head", "body", pivot=(0, -2, -11))
    m.bone("head_cube", "head", rotation=(-22.5, 0, 0))
    m.cube("head_cube", (-1.5, -1, -1), (3, 5, 2), "head")
    m.bone("right_ear", "head", pivot=(-1, -1, 0))
    m.bone("right_ear_cube", "right_ear", pivot=(-0.5, 0, -0.6), rotation=(10.8, -22.1, -4.1))
    m.cube("right_ear_cube", (-2, -3, 0), (2, 5, 0), "ear")
    m.bone("left_ear", "head", pivot=(1, -2, 0))
    m.bone("left_ear_cube", "left_ear", pivot=(0.5, 1, -0.6), rotation=(10.8, 22.1, 4.1))
    m.cube("left_ear_cube", (0, -3, 0), (2, 5, 0), "ear")
    for name, x, z in (("right_hind_leg", -2, 4), ("left_hind_leg", 2, 4), ("right_front_leg", -2, -4), ("left_front_leg", 2, -4)):
        m.bone(name, pivot=(x, 21, z))
        m.cube(name, (-1, 0, -1), (2, 3, 2), "leg")
    m.bone("cube", pivot=(0, 24, 0))                                         # eingerollte Kugel
    m.cube("cube", (-5, -10, -6), (10, 10, 10), "ball")
    return m


# Aufteilung des Panzers entlang der Körperachse (Körper-Koordinaten, z von -10 vorn bis 2 hinten):
# Schulterschild | bewegliche Gürtel (Licht/Fuge im Wechsel) | Beckenschild
_SPLIT = {"neunbinden": (-7.0, 0.0), "kugel": (-6.0, -1.0)}
_LEGS_Z = (-8.0, 0.0)                                   # Beinpaare im Körper-Raum (Bögen im Panzerrand)


def _tile(i: int, j: int) -> float:
    """Schuppen-Kacheln (2x2, jede Reihe um ein Texel versetzt): helle Mitte, etwas dunklere Kanten und Fugen;
    jede Kachel bekommt ihre eigene, leicht schwankende Helligkeit."""
    jr, jt = j // 2, j % 2
    shift = jr % 2
    io = (i + shift) % 2
    tone = (hash01((i + shift) // 2, jr, 21) - 0.5) * 0.12
    if jt == 1 and io == 1:
        return 0.34 + tone
    if jt == 1:
        return 0.5 + tone
    if io == 1:
        return 0.56 + tone
    return 0.7 + tone


def materials(variant: str) -> dict:
    breed = variant.split("_", 1)[1]
    front_end, rear_start = _SPLIT[breed]

    def shield(ctx: Ctx, i: int, j: int, dark: float = 0.0):
        v = _tile(i, j) + (hash01(i, j, 7) - 0.5) * 0.05 - dark
        return pick(ctx.pal["shell"], v) if v > 0.4 else pick(ctx.pal["band"], v + 0.22)

    def belt(ctx: Ctx, lz: float, across: int):
        k = int(lz - front_end)                               # 0, 1, 2, … ab dem Schulterschild
        if k % 2 == 1:
            return pick(ctx.pal["band"], 0.28 + hash01(across, k) * 0.08)        # Fuge zwischen zwei Gürteln
        v = 0.6 + (0.1 if across % 2 == 0 else -0.06) + (hash01(across, k, 3) - 0.5) * 0.08
        return pick(ctx.pal["shell"], v)

    def shell(ctx: Ctx):
        lx, ly, lz = ctx.local
        if ctx.face == "bottom":
            return None                                       # Bauch des inneren Quaders scheint durch
        if ctx.face in ("west", "east"):
            ry = rel_y(ctx)
            for lzc in _LEGS_Z:                                # Bogen über jedem Bein
                d = abs(lz - lzc)
                if (ctx.t == ctx.h - 1 and d < 1.6) or (ctx.t == ctx.h - 2 and d < 0.6):
                    return None
            if ctx.t == ctx.h - 1:                            # dunkler Panzerrand
                return pick(ctx.pal["band"], 0.34 + hash01(ctx.s, 5) * 0.06)
            if ctx.t == ctx.h - 2:
                return pick(ctx.pal["band"], 0.56 + hash01(ctx.s, 6) * 0.06)
            across = int(ly + 7)
            if lz < front_end:
                return shield(ctx, across, int(lz + 10), dark=ry * 0.08)
            if lz > rear_start:
                return shield(ctx, across, int(lz - rear_start), dark=ry * 0.08)
            return belt(ctx, lz, across)
        if ctx.face == "top":
            across = int(lx + 4)
            if lz < front_end:
                return shield(ctx, across, int(lz + 10))
            if lz > rear_start:
                return shield(ctx, across, int(lz - rear_start))
            return belt(ctx, lz, across)
        # Stirn- und Heckseite: Schild mit dunklem Rand und der Öffnung für Kopf bzw. Schwanz
        edge = ctx.s in (0, ctx.w - 1) or ctx.t == 0
        if ctx.face == "front" and 2 <= ctx.s <= 5 and ctx.t >= 4:
            return pick(ctx.pal["skin"], 0.18 + (ctx.t - 4) * 0.05)
        if ctx.face == "back" and 3 <= ctx.s <= 4 and 2 <= ctx.t <= 4:
            return pick(ctx.pal["skin"], 0.2)
        if ctx.t >= ctx.h - 1:
            return pick(ctx.pal["band"], 0.34)
        return shield(ctx, ctx.s, ctx.t, dark=0.08 if edge else 0.0)

    def under(ctx: Ctx):
        if ctx.face == "bottom":
            hair = hash01(ctx.u, ctx.v, 11) > 0.72
            return pick(ctx.pal["hair" if hair else "skin"], 0.62 + hash01(ctx.u, ctx.v) * 0.12)
        ry = rel_y(ctx)
        return pick(ctx.pal["skin"], 0.3 + ry * 0.14 + hash01(ctx.u, ctx.v) * 0.05)

    def head(ctx: Ctx):
        lx, ly, lz = ctx.local
        front_col = lz - ctx.cube.origin[2] < 1.0
        if ctx.face == "top":
            return shield(ctx, ctx.s, ctx.t + 1)
        if ctx.face == "front":
            if ctx.t == 0:
                return pick(ctx.pal["shell"], 0.62 if ctx.s == 1 else 0.5)
            if ctx.t == 1:
                return pick(ctx.pal["eye"], 0.08) if ctx.s != 1 else pick(ctx.pal["shell"], 0.48)
            if ctx.t == ctx.h - 1:
                return pick(ctx.pal["skin"], 0.22 if ctx.s == 1 else 0.38)          # Nasenspitze
            return pick(ctx.pal["skin"], 0.56 + (0.08 if ctx.s == 1 else 0.0) - ctx.t * 0.03)
        if ctx.face in ("west", "east"):
            if ctx.t == 0:
                return pick(ctx.pal["shell"], 0.44)
            if ctx.t == 1 and front_col:
                return pick(ctx.pal["eye"], 0.12)
            return pick(ctx.pal["skin"], 0.46 - ctx.t * 0.03 + hash01(ctx.u, ctx.v) * 0.06)
        if ctx.face == "bottom":
            nostril = ctx.s != 1 and front_col
            return pick(ctx.pal["skin"], 0.08 if nostril else 0.28)
        return pick(ctx.pal["skin"], 0.3)

    def ear(ctx: Ctx):
        rim = ctx.s in (0, ctx.w - 1) or ctx.t == 0
        if ctx.face == "front":
            return pick(ctx.pal["skin"], 0.5 if rim else 0.26 + ctx.t * 0.02)            # Innenseite
        return pick(ctx.pal["skin"], 0.48 + (0.08 if rim else 0.0) + hash01(ctx.u, ctx.v) * 0.05)

    def tail(ctx: Ctx):
        lx, ly, lz = ctx.local
        k = int(ly - ctx.cube.origin[1])
        if k >= 5:
            return pick(ctx.pal["skin"], 0.3)
        v = 0.6 - k * 0.04 + (0.06 if ctx.face in ("top", "west") else 0.0)
        return pick(ctx.pal["band"], 0.3) if k % 2 == 1 else pick(ctx.pal["shell"], v)

    def leg(ctx: Ctx):
        ry = rel_y(ctx)
        if ctx.face == "bottom":
            return pick(ctx.pal["skin"], 0.22)
        if ry > 0.66:
            if ctx.face == "front":
                return pick(ctx.pal["claw"], 0.3 + (0.25 if ctx.s % 2 == 0 else 0.0))   # zwei Krallen
            return pick(ctx.pal["skin"], 0.3)
        if ry < 0.34 and ctx.face in ("west", "east", "front", "back"):
            return pick(ctx.pal["shell"], 0.42 + hash01(ctx.u, ctx.v) * 0.1)          # beschuppter Oberschenkel
        return pick(ctx.pal["skin"], 0.42 + (0.08 if (ctx.s + ctx.t) % 2 == 0 else 0.0))

    def ball(ctx: Ctx):
        # Eingerollt: der Rücken läuft vom Stirnrand oben über die Rückseite bis unten zum Schwanz (Weg p = 0…30);
        # die Seiten zeigen die Ränder wie einen Bilderrahmen, vorn treffen Kopfschild und Schwanz aufeinander.
        lx, ly, lz = ctx.local
        across = int(lx + 5)

        def along_spine(p: float, a: int, dark: float):
            if p < 9.0:
                return shield(ctx, a, int(p), dark)
            if p < 19.0:
                k = int(p - 9.0)
                if k % 2 == 1:
                    return pick(ctx.pal["band"], 0.28 + hash01(a, k) * 0.08 - dark * 0.5)
                return pick(ctx.pal["shell"], 0.62 - dark + (0.08 if a % 2 == 0 else -0.04))
            return shield(ctx, a, int(p - 19.0), dark + 0.04)

        if ctx.face == "top":
            return along_spine(lz + 6.0, across, 0.0)
        if ctx.face == "back":
            return along_spine(10.0 + (ly + 10.0), across, 0.04)
        if ctx.face == "bottom":
            return along_spine(20.0 + (4.0 - lz), across, 0.1)
        if ctx.face in ("west", "east"):
            d_top, d_back, d_bottom = ly + 10.0, 4.0 - lz, -ly
            depth = int(min(d_top, d_back, d_bottom))
            if depth >= 4:
                return pick(ctx.pal["band"], 0.3 + hash01(ctx.u, ctx.v) * 0.06)        # Seitenrand des Panzers
            dark = 0.05 * depth
            if d_top <= min(d_back, d_bottom):
                return along_spine(lz + 6.0, depth, dark)
            if d_back <= d_bottom:
                return along_spine(10.0 + (ly + 10.0), depth, dark)
            return along_spine(20.0 + (4.0 - lz), depth, dark)
        # Stirnseite: oben der Kopfschild, unten der eingeklappte Schwanz zwischen den Beckenschild-Kacheln
        if ctx.t == 5:
            return pick(ctx.pal["band"], 0.2)
        if ctx.t < 5:
            if ctx.s in (0, ctx.w - 1) or ctx.t == 0:
                return shield(ctx, ctx.s, ctx.t, 0.1)
            return shield(ctx, ctx.s, ctx.t + 1, -0.06)
        if 4 <= ctx.s <= 5 and ctx.t < ctx.h - 1:
            return pick(ctx.pal["band"], 0.42) if ctx.t % 2 == 0 else pick(ctx.pal["shell"], 0.66)
        return shield(ctx, ctx.s, ctx.t, 0.04)

    return {
        "shell": shell,
        "underside": under,
        "head": head,
        "ear": ear,
        "tail": tail,
        "leg": leg,
        "ball": ball,
    }


def hidden(variant: str) -> list[str]:
    return ["cube"]
