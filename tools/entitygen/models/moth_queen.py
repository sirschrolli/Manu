"""
Noctua, die Mottenkönigin: ein riesiger Mondfalter (im Spiel 2,5-fach vergrößert, Spannweite gut sechs Blöcke). Ein
dicht befellter, lavendelweißer Leib mit einem gestreiften Hinterleib, ein Kopf mit großen, violett glühenden
Facettenaugen, gefiederten Fühlern und einer goldenen Mondsichel-Krone; vier gewaltige Flügel in Mondgrün und Flieder
mit leuchtenden Augenflecken und langen, geschwungenen Schwänzen an den Hinterflügeln; sechs zarte Beine.

Knochen-Namen werden in MothQueenModel (Java) für die Animationen verwendet.
"""
from __future__ import annotations

import math

from ..geometry import Model
from ..paint import Ctx, hash01, mix, noise3, pick, ramp, rel_y

PALETTES = {
    "moth_queen": {
        "fur": ramp("#8a78b0", "#ffffff", steps=8), "stripe": ramp("#6a4a9a", "#e2d0ff", steps=6),
        "wing": ramp("#2a8a6a", "#f2fff9", steps=9), "wing2": ramp("#8058c0", "#fbf2ff", steps=9),
        "vein": ramp("#1e5a4c", "#7ab8a4", steps=5), "vein2": ramp("#4a2a7a", "#a888d0", steps=5),
        "spot": ramp("#c02a78", "#ffd0ec", steps=6),
        "eye": ramp("#3a0a6a", "#f0d8ff", steps=6), "gold": ramp("#9a6a18", "#fff4c0", steps=6),
        "leg": ramp("#4a3e66", "#c0b0d8", steps=5),
    },
}


def build() -> Model:
    m = Model("moth_queen", 128, 128)
    m.bone("body", pivot=(0, 12, 0))
    m.cube("body", (-3, -3, -4), (6, 6, 7), "fur")                         # Brust
    m.cube("body", (-3.5, -3.6, -4.4), (7, 2.2, 7.6), "fur")               # Fellkragen
    # Hinterleib in zwei Gliedern
    m.bone("abdomen", "body", pivot=(0, 0.5, 3))
    m.cube("abdomen", (-2.5, -2.4, 0), (5, 5, 5), "abdomen")
    m.bone("tail", "abdomen", pivot=(0, 0.2, 5))
    m.cube("tail", (-1.8, -1.8, 0), (3.6, 3.6, 4.5), "abdomen")
    m.cube("tail", (-1.0, -1.0, 4.4), (2, 2, 1.6), "abdomen")
    # Kopf mit Augen, Krone und Fühlern
    m.bone("head", "body", pivot=(0, -0.5, -4))
    m.cube("head", (-2.2, -2.2, -3.4), (4.4, 4.4, 3.6), "face")
    m.cube("head", (-3.3, -1.9, -3.4), (1.7, 3.0, 3.0), "eye")
    m.cube("head", (1.6, -1.9, -3.4), (1.7, 3.0, 3.0), "eye")
    m.cube("head", (-1.8, -3.4, -2.8), (3.6, 1.2, 2.2), "crown")
    m.cube("head", (-0.6, -5.0, -2.6), (1.2, 1.6, 1.4), "crown")
    m.cube("head", (-1.9, -4.4, -2.4), (0.9, 1.0, 1.0), "crown")
    m.cube("head", (1.0, -4.4, -2.4), (0.9, 1.0, 1.0), "crown")
    for side, sx in (("left", 1), ("right", -1)):
        ant = f"{side}_antenna"
        m.bone(ant, "head", pivot=(1.0 * sx, -2.2, -2.8), rotation=(-25, -18 * sx, 22 * sx))
        m.cube(ant, (-0.5 if sx > 0 else -0.5, -9, -2.5), (1, 9, 5), "antenna", inflate=-0.01)
    # Flügel: waagerechte Flächen, die nach außen reichen
    for side, sx in (("left", 1), ("right", -1)):
        fore = f"{side}_wing"
        m.bone(fore, "body", pivot=(2.6 * sx, -2.0, -1.5))
        m.cube(fore, (0 if sx > 0 else -18, 0, -7), (18, 0, 13), "forewing")
        hind = f"{side}_hind_wing"
        m.bone(hind, "body", pivot=(2.4 * sx, -1.0, 1.5))
        m.cube(hind, (0 if sx > 0 else -13, 0, 0), (13, 0, 17), "hindwing")
        for i, z in enumerate((-2.5, 0.0, 2.5)):
            leg = f"{side}_leg{i}"
            m.bone(leg, "body", pivot=(2.4 * sx, 2.6, z), rotation=(0, 0, 35 * sx))
            m.cube(leg, (-0.4, 0, -0.4), (0.8, 5.5, 0.8), "leg")
    return m


_LIT = {"top": 0.1, "front": 0.04, "west": 0.0, "east": -0.03, "back": -0.06, "bottom": -0.16}


def _fur(ctx: Ctx):
    x, y, z = ctx.world
    tuft = noise3(x * 1.3, y * 1.3, z * 1.3, 4)
    v = 0.74 + tuft * 0.22 + _LIT[ctx.face] - rel_y(ctx) * 0.08
    return pick(ctx.pal["fur"], v + (hash01(ctx.u, ctx.v) - 0.5) * 0.08)


def _abdomen(ctx: Ctx):
    """Gestreifter Hinterleib: helle Fellringe, dazwischen dunklere Bänder; an der Unterseite heller."""
    x, y, z = ctx.world
    band = math.sin(z * 1.9) > 0.35
    if band and ctx.face != "bottom":
        return pick(ctx.pal["stripe"], 0.45 + _LIT[ctx.face] + (hash01(ctx.u, ctx.v) - 0.5) * 0.08)
    return pick(ctx.pal["fur"], 0.72 + _LIT[ctx.face] + (hash01(ctx.u, ctx.v) - 0.5) * 0.08)


def _face(ctx: Ctx):
    v = 0.78 + _LIT[ctx.face] + (hash01(ctx.u, ctx.v) - 0.5) * 0.1
    if ctx.face == "front" and ctx.t >= ctx.h - 1:
        return pick(ctx.pal["stripe"], 0.3)                               # Mundwerkzeuge
    return pick(ctx.pal["fur"], v)


def _eye(ctx: Ctx):
    """Facettenaugen: ein Raster aus leuchtenden Punkten auf dunklem Grund."""
    shine = (ctx.s + ctx.t) % 2 == 0
    col = pick(ctx.pal["eye"], 0.75 if shine else 0.42)
    if ctx.s == 0 and ctx.t == 0:
        col = pick(ctx.pal["eye"], 0.98)
    ctx.glow = col
    return col


def _crown(ctx: Ctx):
    col = pick(ctx.pal["gold"], 0.6 + _LIT[ctx.face] + (0.25 if (ctx.s + ctx.t) % 3 == 0 else 0.0))
    if (ctx.s + ctx.t) % 3 == 0:
        ctx.glow = col
    return col


def _antenna(ctx: Ctx):
    """Gefiederte Fühler: ein Schaft mit feinen Fiedern nach beiden Seiten (durchsichtige Lücken)."""
    if ctx.face not in ("west", "east"):
        return pick(ctx.pal["fur"], 0.5) if ctx.face in ("top", "bottom") else None
    lx, ly, lz = ctx.local
    row = int(math.floor(-ly))
    col = lz + 2.5
    shaft = abs(col - 2.5) < 0.6
    if shaft:
        return pick(ctx.pal["fur"], 0.55 + row * 0.02)
    width = 2.4 * (1.0 - row / 10.0)
    if abs(col - 2.5) < width and row % 2 == 0:
        c = pick(ctx.pal["fur"], 0.7)
        return c[0], c[1], c[2], 230
    return None


_FORE = [
    # 18 Spalten (vom Leib nach außen) × 13 Zeilen (vorn nach hinten)
    "######............",
    "###########.......",
    "################..",
    "##################",
    "##################",
    "##################",
    "#################.",
    "################..",
    "###############...",
    "#############.....",
    "###########.......",
    "#########.........",
    "######............",
]

_HIND = [
    # 13 Spalten × 17 Zeilen (vorn nach hinten), mit langem, geschwungenem Schwanz
    "########.....",
    "###########..",
    "#############",
    "#############",
    "############.",
    "###########..",
    "##########...",
    "########.....",
    "......####...",
    ".......####..",
    "........###..",
    "........###..",
    "........####.",
    ".........###.",
    ".........###.",
    "........####.",
    "........###..",
]


def _wing_cell(ctx: Ctx, shape: list[str], spot: tuple[float, float], spot_r: float, pal: str):
    """
    Flügel eines Mondfalters: mondgrün (Vorderflügel) bzw. flieder (Hinterflügel), zum Rand hin heller; dunkle Adern
    vom Ansatz aus; ein großer, leuchtender Augenfleck mit dunklem Ring; ein heller Saum mit leuchtenden Punkten.
    """
    if ctx.face not in ("top", "bottom"):
        return None
    lx, ly, lz = ctx.local
    w = len(shape[0])
    h = len(shape)
    col = min(w - 1, max(0, int(math.floor(abs(lx)))))
    row = min(h - 1, max(0, int(math.floor(lz - ctx.cube.origin[2]))))
    if shape[row][col] != "#":
        return None
    edge = col == w - 1 or shape[row][min(w - 1, col + 1)] != "#" or row == 0 or shape[row - 1][col] != "#" \
        or row == h - 1 or shape[min(h - 1, row + 1)][col] != "#"
    d = math.hypot(col - spot[0], row - spot[1])
    if d < spot_r * 0.42:
        c = pick(ctx.pal["spot"], 0.45 if d < spot_r * 0.2 else 0.62)
        ctx.glow = c
        return c
    if d < spot_r * 0.72:
        c = pick(ctx.pal["gold"], 0.85)
        ctx.glow = c
        return c
    if d < spot_r:
        return pick(ctx.pal["vein2" if pal == "wing2" else "vein"], 0.1)
    if edge:
        if (col + row) % 3 == 0:
            c = pick(ctx.pal["gold"], 0.85)
            ctx.glow = c
            return c
        return pick(ctx.pal[pal], 0.92)
    angle = math.atan2(row - h / 2.0, col + 1.0)
    vein = abs(math.sin(angle * 6.0)) < 0.13 and col > 1
    if vein:
        return pick(ctx.pal["vein2" if pal == "wing2" else "vein"], 0.6)
    v = 0.5 + col / w * 0.36 + (hash01(ctx.u, ctx.v) - 0.5) * 0.06 + (0.05 if ctx.face == "top" else -0.03)
    return pick(ctx.pal[pal], v)


def _forewing(ctx: Ctx):
    return _wing_cell(ctx, _FORE, (11.5, 6.0), 2.6, "wing")


def _hindwing(ctx: Ctx):
    return _wing_cell(ctx, _HIND, (7.0, 3.5), 2.0, "wing2")


def _leg(ctx: Ctx):
    return pick(ctx.pal["leg"], 0.5 + _LIT[ctx.face] - rel_y(ctx) * 0.2)


def materials(variant: str) -> dict:
    return {"fur": _fur, "abdomen": _abdomen, "face": _face, "eye": _eye, "crown": _crown, "antenna": _antenna,
            "forewing": _forewing, "hindwing": _hindwing, "leg": _leg}
