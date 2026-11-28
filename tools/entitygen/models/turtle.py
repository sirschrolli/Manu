"""
Suppenschildkröte und Karettschildkröte (statt Schildkröte) auf dem Skelett der Vanilla-Schildkröte (AdultTurtleModel):
gleiche Knochen, Eltern und Drehpunkte – Kopf "head", um 90° gekippter Rumpf "body", "egg_belly" (nur sichtbar, wenn
sie ein Ei trägt) und die vier Flossen "right_hind_leg", "left_hind_leg", "right_front_leg", "left_front_leg" –,
damit TurtleModel Kopf und Flossen an Land und im Wasser genauso bewegt.

Statt der flachen Kiste ein gewölbter Panzer in drei Stufen (Randschilde, Rippenschilde, Wirbelschilde) mit
abgerundeten Ecken; die Schilde sind als echte Schildfolge gezeichnet: fünf Wirbelschilde in der Mitte, je vier
Rippenschilde mit versetzten Nähten, ein Kranz aus Randschilden mit Nackenschild vorn. Flossen pixelgenau
ausgeschnitten (lange, nach hinten gezogene Vorderflossen mit Krallen, breite Paddel hinten), Kopf mit Hornschnabel.

  * suppenschildkroete: olivbrauner Panzer mit feinen, vom Zentrum ausstrahlenden Streifen und hellen Nähten,
                        grüngraue gesprenkelte Haut mit hellem Schuppensaum, cremefarbener Bauchpanzer, stumpfe Schnauze
  * karettschildkroete: bernsteinfarbener Schildpatt-Panzer mit dunklen Flammen und dunklen Nähten, dunkelbraune
                        Schuppen mit Bernsteinsaum, gelber Bauchpanzer, spitzer, gebogener Falkenschnabel
"""
from __future__ import annotations

import math

from ..geometry import Model
from ..paint import Ctx, hash01, noise3, pick, ramp

PALETTES = {
    "suppenschildkroete": {
        "shell": ramp("#2a2410", "#a8964e", steps=10), "seam": ramp("#857a4c", "#cdc08e"),
        "flame": ramp("#1e180a", "#4a3e1c"), "plastron": ramp("#9c9060", "#f4ecd2", steps=8),
        "skin": ramp("#222c20", "#9caa8c", steps=8), "margin": ramp("#8c8e70", "#e2e2c4"),
        "plate": ramp("#24261a", "#6e6c40"), "horn": ramp("#4e4636", "#cabc94"), "eye": ramp("#040404", "#2a2018"),
    },
    "karettschildkroete": {
        "shell": ramp("#4a1e06", "#f2ac4c", steps=10), "seam": ramp("#140904", "#341a0a"),
        "flame": ramp("#1a0904", "#5a2810"), "plastron": ramp("#a07a2c", "#f8e09c", steps=8),
        "skin": ramp("#1a120c", "#6e4e2e", steps=8), "margin": ramp("#8e5c1e", "#ecbc64"),
        "plate": ramp("#1c120a", "#5a3a1c"), "horn": ramp("#5e3e14", "#eec474"), "eye": ramp("#040302", "#24180c"),
    },
}

# Vorderflosse: Zeilen vom Rumpf zur Spitze, Spalten von der Vorder- zur Hinterkante
FRONT = ("#####", "#####", "#####", "#####", "#####", ".####", ".####", ".####", "..###", "..###", "...##", "...##",
         "....#")
# Hinterflosse: Zeilen vom Rumpf nach hinten, Spalten quer
HIND = (".##.", ".##.", "####", "####", "####", "####", "####", "####", "####", ".##.")
CLAWS = {"suppenschildkroete": (6,), "karettschildkroete": (6, 8)}

VSEAMS = (-0.5, 2.5, 5.5, 8.5)          # Nähte zwischen den Wirbelschilden (Panzer-z)
CSEAMS = (1.5, 4.5, 7.5)                # Nähte zwischen den Rippenschilden, versetzt dazu

# Kopf: Schuppenplatten (P), Nähte (s), Haut (K), Kehle (T), Auge (E), Ober-/Unterkiefer (J/L)
HEAD_SIDE = {   # Spalten: z von vorn nach hinten, Zeilen: y von oben nach unten
    "head_b": (-11.5, 18.5, ("PP", "PE", "JJ", "LL")),
    "head_a": (-9.5, 18.5, ("PsP", "sPP", "KKK", "KKK", "TTT")),
}
HEAD_TOP = {-12.5: "PsP", -11.5: "PPsPP", -10.5: "PPsPP", -9.5: "sssss", -8.5: "sPPPs", -7.5: "sPPPs"}


def _box(m: Model, bone: str, x0, x1, ya, yb, za, zb, material: str):
    """Quader in Modellkoordinaten (y nach unten, z nach hinten) in den um 90° gekippten Rumpf setzen."""
    m.cube(bone, (x0, za + 10, 11 - yb), (x1 - x0, zb - za, yb - ya), material)


def build() -> Model:
    m = Model("turtle", 128, 64)
    m.world_faces = True
    m.bone("head", pivot=(0, 19, -10))
    m.cube("head", (-2.5, -1, 0), (5, 5, 3), "head")                    # Hinterkopf
    m.cube("head", (-2.5, -1, -2), (5, 4, 2), "head")                   # Stirn mit Augen
    m.cube("head", (-1.5, 0, -3), (3, 3, 1), "snout")
    m.cube("head", (-0.5, 1, -4), (1, 2, 1), "beak")                    # Falkenschnabel (nur Karettschildkröte)
    m.bone("body", pivot=(0, 11, -10), rotation=(90, 0, 0))
    _box(m, "body", -9.5, 9.5, 19, 21, -6, 12, "shell")                 # Randschilde
    _box(m, "body", -7.5, 7.5, 19, 21, -7, -6, "shell")
    _box(m, "body", -6.5, 6.5, 19, 21, 12, 13, "shell")
    _box(m, "body", -8.5, 8.5, 17, 19, -5, 11, "shell")                 # Rippenschilde
    _box(m, "body", -6.5, 6.5, 15, 17, -3, 9, "shell")                  # Wirbelschilde
    _box(m, "body", -5.5, 5.5, 21, 24, -7, 11, "plastron")              # Bauchpanzer (wie Vanilla "belly")
    m.bone("egg_belly", pivot=(0, 11, -10), rotation=(90, 0, 0))
    _box(m, "egg_belly", -4.5, 4.5, 24, 25, -7, 11, "egg")
    m.bone("right_hind_leg", pivot=(-3.5, 22, 11))
    m.cube("right_hind_leg", (-2, 0, 0), (4, 1, 10), "hind")
    m.bone("left_hind_leg", pivot=(3.5, 22, 11))
    m.cube("left_hind_leg", (-2, 0, 0), (4, 1, 10), "hind")
    m.bone("right_front_leg", pivot=(-5, 21, -4))
    m.cube("right_front_leg", (-13, 0, -2), (13, 1, 5), "front")
    m.bone("left_front_leg", pivot=(5, 21, -4))
    m.cube("left_front_leg", (0, 0, -2), (13, 1, 5), "front")
    return m


def hidden(variant: str) -> list[str]:
    return ["egg_belly"]


def preview_pose(variant: str) -> dict:
    return {"left_front_leg": (0, -18, 8), "right_front_leg": (0, 18, -8), "head": (-8, 0, 0)}


# =============================================================================================

def _hawk(ctx: Ctx) -> bool:
    return ctx.params.get("variant") == "karettschildkroete"


def _shade(ctx: Ctx) -> float:
    return {"top": 0.06, "bottom": -0.08}.get(ctx.face, -0.03)


def _near(v: float, targets, eps: float = 0.01) -> bool:
    return any(abs(v - t) < eps for t in targets)


def _scute(x: float, y: float, z: float, face: str) -> tuple[str, bool]:
    """Welcher Schild liegt hier – und liegt der Texel auf einer Naht?"""
    ax = abs(x)
    side = "L" if x > 0 else "R"
    if y > 18.99:                                           # Randschilde
        if ax >= 7.5:
            k = z + 6.5
            return f"M{side}{int(math.floor(k / 3.0))}", abs(k - round(k)) < 0.01 and round(k) % 3 == 0
        end = "F" if z < 3 else "B"
        idx = 0 if ax < 2 else (1 if ax < 5 else 2)
        return f"M{end}{side if idx else ''}{idx}", _near(ax, (2.0, 5.0))
    if face in ("top", "front", "back") and _near(ax, (3.0,)):
        return "S", True                                    # Naht zwischen Wirbel- und Rippenschilden
    if ax < 2.5:
        i = sum(1 for s in VSEAMS if z > s + 0.01)
        return f"V{i}", _near(z, VSEAMS)
    i = sum(1 for s in CSEAMS if z > s + 0.01)
    return f"C{side}{i}", _near(z, CSEAMS)


TIER_TOP = {"C": 15.0, "B": 17.0, "A": 19.0}


def _tier(y: float) -> str:
    return "C" if y < 16.99 else ("B" if y < 18.99 else "A")


def _unroll(x: float, y: float, z: float, face: str) -> tuple[float, float]:
    """Lage auf dem 'abgerollten' Panzer: Seitenflächen der Stufen zählen nach außen weiter."""
    drop = y - TIER_TOP[_tier(y)]
    if face == "west":
        return x - drop, z
    if face == "east":
        return x + drop, z
    if face == "front":
        return x, z - drop
    if face == "back":
        return x, z + drop
    return x, z


def _umbo(sid: str) -> tuple[float, float]:
    """Wachstumszentrum eines Schilds (von dort laufen Streifen und Flammen aus)."""
    if sid[0] == "V":
        return 0.0, (-2.5, 1.0, 4.0, 7.0, 9.75)[int(sid[1])]
    if sid[0] == "C":
        return (7.0 if sid[1] == "L" else -7.0), (-1.75, 3.0, 6.0, 9.25)[int(sid[2])]
    if sid[1] in "LR":
        return (9.5 if sid[1] == "L" else -9.5), 3 * int(sid[2:]) - 5.0
    idx = int(sid[-1])
    sign = -1.0 if "R" in sid[2:] else 1.0
    return sign * (0.0, 3.5, 6.5)[idx], (-7.0 if sid[1] == "F" else 13.0)


def _rim(x: float, y: float, z: float, face: str) -> bool:
    """Liegt der Texel direkt an einer Naht (dunklerer Schildrand)?"""
    if face == "top":
        nbs = ((x + 1, y, z), (x - 1, y, z), (x, y, z + 1), (x, y, z - 1))
    elif face in ("west", "east"):
        nbs = ((x, y, z + 1), (x, y, z - 1), (x, y + 1, z), (x, y - 1, z))
    else:
        nbs = ((x + 1, y, z), (x - 1, y, z), (x, y + 1, z), (x, y - 1, z))
    tier = _tier(y)
    return any(_scute(nx, ny, nz, face)[1] for nx, ny, nz in nbs if _tier(ny) == tier)


def _shell(ctx: Ctx):
    x, y, z = ctx.world
    pal = ctx.pal
    if ctx.face == "bottom":
        return pick(pal["plastron"], 0.42)                 # Unterseite der Randschilde
    sid, seam = _scute(x, y, z, ctx.face)
    sh = _shade(ctx)
    if seam:
        return pick(pal["seam"], 0.45 + sh)
    h = hash01(sid, 7)
    u, w = _unroll(x, y, z, ctx.face)
    u0, w0 = _umbo(sid)
    th = math.atan2(w - w0, u - u0)
    d = math.hypot(u - u0, w - w0)
    g = noise3(math.cos(th) * 1.7 + h * 9.0, math.sin(th) * 1.7, d * 0.22 + h * 4.0, 19)
    rim = 0.09 if _rim(x, y, z, ctx.face) else 0.0
    if _hawk(ctx):
        if g > 0.6 and d > 0.7:
            return pick(pal["flame"], 0.3 + (g - 0.58) * 1.5 + sh * 0.5)
        if g < 0.34 and not rim:
            return pick(pal["shell"], 0.84 + sh)
        return pick(pal["shell"], 0.56 + (0.5 - g) * 0.3 + (h - 0.5) * 0.12 - rim * 1.6 + sh)
    if g > 0.64 and d > 0.8 and not rim:
        return pick(pal["flame"], 0.72 + sh)                 # dunkle, ausstrahlende Zeichnung
    v = 0.5 + (g - 0.5) * 0.4 + (h - 0.5) * 0.14 - rim + sh + (0.06 if d < 1.3 else 0.0)
    return pick(pal["shell"], v)


def _plastron(ctx: Ctx):
    x, y, z = ctx.world
    pal = ctx.pal
    sh = _shade(ctx)
    if ctx.face != "bottom":
        if y < 23:
            return pick(pal["skin"], 0.1 + (y - 21.5) * 0.08)   # im Schatten unter dem Rückenpanzer
        return pick(pal["plastron"], 0.28 + sh)
    if _near(z, (-3.5, 0.5, 4.5, 7.5)) or _near(x, (0.0,)):
        return pick(pal["plastron"], 0.3 + sh)
    return pick(pal["plastron"], 0.62 + (noise3(x * 0.4, y * 0.4, z * 0.4, 5) - 0.5) * 0.2 + sh)


def _egg(ctx: Ctx):
    x, y, z = ctx.world
    return pick(ctx.pal["plastron"], 0.72 + _shade(ctx) - abs(z - 2) * 0.012)


# ---------------------------------------------------------------------------------------------
# Kopf

def _head_cell(ctx: Ctx) -> str:
    x, y, z = ctx.world
    face = ctx.face
    if face in ("west", "east"):
        for z0, y0, rows in HEAD_SIDE.values():
            col = int(round(z - z0))
            row = int(round(y - y0))
            if 0 <= row < len(rows) and 0 <= col < len(rows[0]) and abs(z - (z0 + col)) < 0.01:
                return rows[row][col]
        return "K"
    if face == "top":
        line = HEAD_TOP.get(round(z * 2) / 2)
        if line:
            col = int(round(x + (len(line) - 1) / 2))
            if 0 <= col < len(line):
                return line[col]
        return "P"
    if face == "bottom":
        return "T"
    # Vorderseite der Stirn (um die Schnauze herum)
    if y < 19:
        return "P"
    return "J" if y < 21 else "L"


def _head(ctx: Ctx):
    pal = ctx.pal
    x, y, z = ctx.world
    cell = _head_cell(ctx)
    sh = _shade(ctx)
    n = noise3(x * 0.9, y * 0.9, z * 0.9, 31)
    if cell == "E":
        return pick(pal["eye"], 0.2)
    if cell == "s":
        return pick(pal["margin"], 0.55 + sh)
    if cell == "P":
        return pick(pal["plate"], 0.45 + (n - 0.5) * 0.3 + sh)
    if cell == "J":
        return pick(pal["horn"], 0.66 + sh)
    if cell == "L":
        return pick(pal["horn"], 0.38 + sh)
    if cell == "T":
        return pick(pal["margin"], 0.7 + sh)
    # gesprenkelte Haut
    v = 0.42 + (n - 0.5) * 0.5 + sh
    if hash01(round(x * 2), round(y * 2), round(z * 2), 3) > 0.82:
        return pick(pal["margin"], 0.45 + sh)
    return pick(pal["skin"], v)


def _snout(ctx: Ctx):
    x, y, z = ctx.world
    pal = ctx.pal
    sh = _shade(ctx)
    if ctx.face == "top":
        return pick(pal["plate"], 0.5 + sh) if abs(x) > 0.5 else pick(pal["margin"], 0.5 + sh)
    if y < 20:
        if ctx.face == "front" and abs(x) > 0.5:
            return pick(pal["eye"], 0.3)                    # Nasenlöcher
        return pick(pal["plate"], 0.5 + sh)
    if y < 21:
        return pick(pal["horn"], 0.66 + sh)
    return pick(pal["horn"], 0.38 + sh)


def _beak(ctx: Ctx):
    if not _hawk(ctx):
        return None
    x, y, z = ctx.world
    pal = ctx.pal
    if y < 21 and ctx.face != "bottom":
        return pick(pal["horn"], 0.74 + _shade(ctx))
    return pick(pal["horn"], 0.3)


# ---------------------------------------------------------------------------------------------
# Flossen

def _in(rows, a: int, c: int) -> bool:
    return 0 <= a < len(rows) and 0 <= c < len(rows[0]) and rows[a][c] == "#"


def _flipper(ctx: Ctx, rows, a: int, c: int, claws=()):
    if not _in(rows, a, c):
        return None
    pal = ctx.pal
    edge = not all(_in(rows, a + da, c + dc) for da, dc in ((1, 0), (-1, 0), (0, 1), (0, -1)))
    if ctx.face == "bottom":
        return pick(pal["plastron"], 0.5 if not edge else 0.36)
    if ctx.face != "top":
        return pick(pal["margin"], 0.42)
    lead = rows[a].index("#")
    if a in claws and c == lead:
        return pick(pal["horn"], 0.8)                       # Kralle an der Vorderkante
    if edge and a > 0:
        return pick(pal["margin"], 0.55)
    row = a // 2
    cell_c = (c + row % 2) // 2
    h = hash01(row, cell_c, 13)
    if a % 2 == 0 and (c + row % 2) % 2 == 0:
        return pick(pal["margin"], 0.3 + h * 0.2)            # Glanz an der Vorderecke jeder Schuppe
    return pick(pal["skin"], 0.3 + (h - 0.5) * 0.36 - (0.06 if a % 2 else 0.0))


def _front(ctx: Ctx):
    lx, _, lz = ctx.local
    a = min(int(abs(lx)), len(FRONT) - 1)
    c = min(max(int(math.floor(lz + 2)), 0), 4)
    return _flipper(ctx, FRONT, a, c, CLAWS[ctx.params.get("variant")])


def _hind(ctx: Ctx):
    lx, _, lz = ctx.local
    a = min(max(int(math.floor(lz)), 0), len(HIND) - 1)
    c = min(max(int(math.floor(lx + 2)), 0), 3)
    return _flipper(ctx, HIND, a, c)


def materials(variant: str) -> dict:
    return {"shell": _shell, "plastron": _plastron, "egg": _egg, "head": _head, "snout": _snout, "beak": _beak,
            "front": _front, "hind": _hind}
