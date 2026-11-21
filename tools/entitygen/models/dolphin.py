"""
Tümmler und Schwertwal (statt Delfin) auf dem Skelett des Vanilla-Delfins: gleiche Knochen, Eltern und Drehpunkte
(Leib "body", darin Kopf "head" mit Schnabel "nose", Schwanz "tail" mit Fluke "tail_fin", Rückenflosse "back_fin"
und die Brustflossen "left_fin"/"right_fin"), damit DolphinModel Leib, Schwanz und Fluke beim Schwimmen genauso
bewegt und der Gegenstand im Maul (fester Versatz im Renderer) an der Schnabelspitze sitzt.

Statt des Vanilla-Quaders ein stromlinienförmiger Körper: Rumpf, schmalerer Hinterleib, ein sich verjüngender,
seitlich abgeflachter Schwanzstiel, ein in Stufen zur Schnauze abfallender Kopf (Melone) und ein kurzer, kräftiger
Schnabel. Die Rückenflossen hängen in Kindknochen von back_fin, die dessen Kippung aufheben, damit ihre Stufen
waagrecht liegen: "dorsal_low" (Tümmler, sichelförmig) und "dorsal_tall" (Schwertwal, hohes Schwert); die jeweils
andere bleibt in der Textur durchsichtig. Brustflossen und Fluke sind pixelgenau ausgeschnitten.

  * tuemmler:   schiefergrauer Umhang auf dem Rücken, der unter der Rückenflosse abfällt, hellgraue Flanken mit
                heller Blesse, dunkler Augen-Flossen-Streif, rosig-weißer Bauch und Unterkiefer
  * schwertwal: glänzend schwarz mit weißem Augenfleck, weißem Kinn und Kehle, weißem Flankenlappen hinter der
                Rückenflosse, grauem Sattel, Fluke unten weiß mit schwarzem Rand; Paddel-Brustflossen
"""
from __future__ import annotations

import math

from ..geometry import Model
from ..paint import Ctx, mix, noise3, pick, ramp

PALETTES = {
    "tuemmler": {
        "back": ramp("#1a232b", "#5e6e7c", steps=10), "flank": ramp("#475663", "#b8c6d0", steps=10),
        "belly": ramp("#a8999a", "#f7eeea", steps=8), "eye": ramp("#020304", "#1e242a"),
        "line": ramp("#151b22", "#36414c"),
    },
    "schwertwal": {
        "back": ramp("#040506", "#3a4148", steps=10), "flank": ramp("#040506", "#3a4148", steps=10),
        "belly": ramp("#b2b3b0", "#fdfdf9", steps=8), "eye": ramp("#000000", "#101214"),
        "line": ramp("#000000", "#16181c"), "saddle": ramp("#4a525a", "#a6aeb6", steps=8),
    },
}

GEAR = {"tuemmler": {"dorsal_low"}, "schwertwal": {"dorsal_tall"}}

# Brustflosse: Zeilen vom Ansatz zur Spitze, Spalten von der Vorder- zur Hinterkante
FLIPPER = {
    "tuemmler": ("####", "####", "####", ".###", ".###", "..##", "...#"),
    "schwertwal": ("####", "####", "####", "####", "####", "####", ".##."),
}
# Fluke von oben: Zeilen vom Schwanzstiel zur Hinterkante, Spalten von links nach rechts
FLUKE = ("...####...", ".########.", "##########", "####..####", "###....###", "#........#")

# Lage der Knochen-Ursprünge im Modell (ohne die leichte Neigung des Schwanzes), für Zeichnungen am Körper
_OFFSET = {"body": (0.0, 22.0, -5.0), "dorsal_low": (0.0, 22.0, -5.0), "dorsal_tall": (0.0, 22.0, -5.0),
           "head": (0.0, 18.0, -8.0), "nose": (0.0, 18.0, -8.0), "tail": (0.0, 19.5, 6.0)}

# Tümmler: Unterkante des dunklen Umhangs und Oberkante des hellen Bauchs (Modell-y, unten größer) über z
CAPE = ((-15, 19.6), (-10.5, 18.6), (-8, 17.6), (-5, 17.1), (-1, 17.4), (2.5, 18.7), (6, 17.9), (10, 18.6),
        (17, 19.2))
BELLY = ((-15, 21.0), (4, 21.0), (6, 20.6), (10, 20.6), (14, 20.1), (17, 20.0))
# Schwertwal: Oberkante des weißen Flankenlappens
LOBE = ((-0.6, 99.0), (0.0, 21.6), (4.5, 17.6), (11.5, 21.7), (12.0, 99.0))


def build() -> Model:
    m = Model("dolphin", 64, 64)
    m.world_faces = True
    m.bone("body", pivot=(0, 22, -5))
    m.cube("body", (-4, -7, 0), (8, 7, 9), "skin")                      # Rumpf
    m.cube("body", (-3.5, -6.5, 9), (7, 6, 3), "skin")                  # schmalerer Hinterleib
    # back_fin bleibt wie im Original um 60° gekippt; die Flossen hängen in Kindknochen, die das aufheben
    m.bone("back_fin", "body", rotation=(60, 0, 0))
    m.bone("dorsal_low", "back_fin", rotation=(-60, 0, 0))
    for y, h, z0, z1 in ((-8, 2, 3, 9), (-9, 1, 4, 9), (-10, 1, 5.5, 9.5), (-11, 1, 7.5, 9.5)):
        m.cube("dorsal_low", (-0.5, y, z0), (1, h, z1 - z0), "fin")
    m.bone("dorsal_tall", "back_fin", rotation=(-60, 0, 0))
    for y, h, z0, z1 in ((-8, 2, 3, 7), (-10, 2, 4, 7), (-12, 2, 4.5, 6.5), (-14, 2, 5, 6)):
        m.cube("dorsal_tall", (-0.5, y, z0), (1, h, z1 - z0), "fin")
    m.bone("left_fin", "body", pivot=(2, -2, 4), rotation=(60, 0, 120))
    m.cube("left_fin", (-0.5, -4, 0), (1, 4, 7), "flipper")
    m.bone("right_fin", "body", pivot=(-2, -2, 4), rotation=(60, 0, -120))
    m.cube("right_fin", (-0.5, -4, 0), (1, 4, 7), "flipper")
    m.bone("tail", "body", pivot=(0, -2.5, 11), rotation=(-6, 0, 0))
    m.cube("tail", (-2.5, -3, 0), (5, 5, 4), "skin")
    m.cube("tail", (-1.5, -2.5, 4), (3, 4, 4), "skin")                   # seitlich abgeflachter Schwanzstiel
    m.cube("tail", (-1, -1.5, 8), (2, 3, 3), "skin")
    m.bone("tail_fin", "tail", pivot=(0, 0, 9))
    m.cube("tail_fin", (-5, -0.5, 0), (10, 1, 6), "fluke")
    m.bone("head", "body", pivot=(0, -4, -3))
    m.cube("head", (-3.5, -2, 0), (7, 6, 3), "skin")                     # Nacken
    m.cube("head", (-3, -1, -2), (6, 5, 2), "skin")                      # Melone
    m.cube("head", (-2.5, 0, -3), (5, 4, 1), "skin")                     # Stirn über dem Schnabel
    m.bone("nose", "head")
    m.cube("nose", (-1.5, 2, -7), (3, 2, 4), "beak")
    return m


def hidden(variant: str) -> list[str]:
    return [b for b in ("dorsal_low", "dorsal_tall") if b not in GEAR[variant]]


def preview_pose(variant: str) -> dict:
    return {"tail": (-4, 0, 0), "tail_fin": (-8, 0, 0)}


# =============================================================================================

def _variant(ctx: Ctx) -> str:
    return ctx.params.get("variant")


def _orca(ctx: Ctx) -> bool:
    return _variant(ctx) == "schwertwal"


def _pos(ctx: Ctx) -> tuple[float, float, float]:
    off = _OFFSET.get(ctx.cube.bone.name)
    if off is None:
        return ctx.world
    lx, ly, lz = ctx.local
    return lx + off[0], ly + off[1], lz + off[2]


def _curve(z: float, pts) -> float:
    if z <= pts[0][0]:
        return pts[0][1]
    for (z0, v0), (z1, v1) in zip(pts, pts[1:]):
        if z <= z1:
            return v0 + (v1 - v0) * (z - z0) / (z1 - z0)
    return pts[-1][1]


def _soft(ctx: Ctx, amp: float = 0.05) -> float:
    x, y, z = _pos(ctx)
    return (noise3(x * 0.35, y * 0.35, z * 0.35, 11) - 0.5) * amp * 2


def _side(ctx: Ctx) -> bool:
    return ctx.face in ("west", "east")


def _at(z0: float, y0: float, z: float, y: float) -> bool:
    return abs(z - z0) < 0.5 and abs(y - y0) < 0.5


# ---------------------------------------------------------------------------------------------
# Tümmler

def _tuemmler(ctx: Ctx, x: float, y: float, z: float):
    pal = ctx.pal
    n = _soft(ctx)
    cape, belly = _curve(z, CAPE), _curve(z, BELLY)
    if ctx.face == "bottom":
        return pick(pal["belly"], 0.6 + n)
    if ctx.face == "top":
        return pick(pal["back"], 0.5 + n)
    if y > belly:
        return pick(pal["belly"], 0.72 + n)
    back = pick(pal["back"], 0.42 + n)
    if y < cape - 0.5:
        return back
    f = max(0.0, (y - cape) / max(belly - cape, 0.5))
    v = 0.36 + f * 0.3
    if -3 < z < 6.5 and y < cape + 1.7:                     # helle Blesse unter der Rückenflosse
        v += 0.12
    if -7.6 < z < -2 and abs(y - (19.6 + (z + 7.5) * 0.15)) < 0.5:   # Augen-Flossen-Streif
        v -= 0.14
    col = pick(pal["flank"], v + n)
    if y < cape + 0.5:                                       # weicher Übergang am Umhang
        return mix(back, col, 0.5)
    return col


# ---------------------------------------------------------------------------------------------
# Schwertwal

def _orca_skin(ctx: Ctx, x: float, y: float, z: float):
    pal = ctx.pal
    n = _soft(ctx, 0.03)
    face = ctx.face
    white = pick(pal["belly"], (0.62 if face == "bottom" else 0.8) + n)
    black = pick(pal["back"], (0.46 if face == "top" else 0.32 if face in ("west", "east") else 0.28) + n)
    # grauer Sattel hinter der Rückenflosse (auch über die halbe Stufe zum Hinterleib hinweg)
    if (face == "top" or (face == "back" and y < 15.6)) and ((z - 4.8) / 2.6) ** 2 + (x / 3.8) ** 2 < 1.0:
        return pick(pal["saddle"], 0.36 + n)
    # Augenfleck: schräg nach hinten ansteigendes Oval über und hinter dem Auge
    if _side(ctx):
        dz = z + 5.5
        dy = y - 18.0 + dz * 0.25
        if (dz / 2.4) ** 2 + (dy / 0.75) ** 2 < 1.0:
            return pick(pal["belly"], 0.86 + n)
    # Kinn und Kehle
    if z < -3 and (face == "bottom" or y > 21):
        return white
    if face == "bottom":
        if z < -1 and abs(x) < 3.2:
            return white
        if -1 <= z < 1 and abs(x) < 2.2:
            return white
        if 1 <= z < 11:
            return white
        return black
    # weißer Flankenlappen
    if y > _curve(z, LOBE):
        return white
    return black


# ---------------------------------------------------------------------------------------------

def _skin(ctx: Ctx):
    x, y, z = _pos(ctx)
    side = _side(ctx)
    # Auge hinter dem Mundwinkel
    if side and abs(abs(x) - 3.0) < 0.05 and _at(-8.5, 19.5, z, y):
        return pick(ctx.pal["eye"], 0.2)
    if side and abs(abs(x) - 3.0) < 0.05 and _at(-9.5, 20.5, z, y):
        return pick(ctx.pal["line"], 0.3)                   # Mundwinkel
    if ctx.face == "top" and abs(x) < 0.5 and abs(z + 6.5) < 0.5:
        return pick(ctx.pal["line"], 0.2)                   # Blasloch
    if _orca(ctx):
        return _orca_skin(ctx, x, y, z)
    return _tuemmler(ctx, x, y, z)


def _beak(ctx: Ctx):
    """Kurzer Schnabel: oben wie die Schnauze, unten der helle Unterkiefer; die Kante dazwischen ist das Maul."""
    x, y, z = _pos(ctx)
    pal = ctx.pal
    n = _soft(ctx, 0.03)
    if ctx.face == "bottom" or y > 21:
        return pick(pal["belly"], (0.6 if ctx.face == "bottom" else 0.74) + n)
    if _orca(ctx):
        return pick(pal["back"], (0.46 if ctx.face == "top" else 0.32) + n)
    return pick(pal["flank"], (0.44 if ctx.face == "top" else 0.4) + n)


def _fin(ctx: Ctx):
    if ctx.cube.bone.name not in GEAR[_variant(ctx)]:
        return None
    _, y, _ = _pos(ctx)
    pal = ctx.pal
    if _orca(ctx):
        v = 0.32 + (0.1 if ctx.face in ("front", "top") else 0.0)
        return pick(pal["back"], v)
    v = 0.36 + (0.06 if ctx.face in ("front", "top") else 0.0) - max(0.0, 12.5 - y) * 0.03
    return pick(pal["back"], v)


def _flipper(ctx: Ctx):
    _, ly, lz = ctx.local
    rows = FLIPPER[_variant(ctx)]
    r = min(max(int(math.floor(lz)), 0), len(rows) - 1)
    c = min(max(int(math.floor(ly + 4)), 0), 3)
    if rows[r][c] != "#":
        return None
    pal = ctx.pal
    if _orca(ctx):
        return pick(pal["back"], 0.42 if ctx.face == "top" else 0.3)
    if ctx.face == "bottom":
        return pick(pal["flank"], 0.4 - r * 0.02)
    return pick(pal["back"], 0.46 - r * 0.02)


def _fluke_cell(r: int, c: int) -> bool:
    return 0 <= r < len(FLUKE) and 0 <= c < len(FLUKE[0]) and FLUKE[r][c] == "#"


def _fluke(ctx: Ctx):
    lx, _, lz = ctx.local
    c = min(max(int(math.floor(lx + 5)), 0), 9)
    r = min(max(int(math.floor(lz)), 0), 5)
    if not _fluke_cell(r, c):
        return None
    pal = ctx.pal
    edge = not all(_fluke_cell(r + dr, c + dc) for dr, dc in ((1, 0), (-1, 0), (0, 1), (0, -1)))
    if _orca(ctx):
        if ctx.face == "bottom" and not edge and r > 0:
            return pick(pal["belly"], 0.66)
        return pick(pal["back"], 0.42 if ctx.face == "top" else 0.3)
    if ctx.face == "bottom":
        return pick(pal["flank"], 0.46 if not edge else 0.34)
    return pick(pal["back"], (0.44 if not edge else 0.36) - r * 0.015)


def materials(variant: str) -> dict:
    return {"skin": _skin, "beak": _beak, "fin": _fin, "flipper": _flipper, "fluke": _fluke}
