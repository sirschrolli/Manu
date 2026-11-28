"""
Chorusschlund: eine fleischfressende Choruspflanze der äußeren End-Inseln, gut zwei Blöcke hoch. Ein Stamm aus kantigen,
violetten Chorusgliedern mit Seitenästen und geschlossenen Knospen, unten flache Wurzeln; oben eine große lila
Chorusknospe aus vier spitzen Blütenblättern – geschlossen sieht sie aus wie jede andere. Kommt Beute heran, blüht sie
auf: die vier Blätter klappen weit auseinander, an ihren Innenseiten stehen Zähne, und aus dem Kelch glüht es magenta.
Zwei Peitschenranken mit Knospen hängen seitlich herab.

Knochen (für ChorusMawModel in Java): stalk1 → stalk2 → stalk3 → head (der Kelch, mit den vier Blütenblättern
petal_left/right/front/back – Scharnier jeweils an der Oberkante des Kelchs – und den Kelchblättern sepal_*), Seitenäste
branch_left (an stalk1) und branch_right (an stalk2) mit *_up, die Ranken vine_left/vine_right (an stalk3) → *_mid →
*_tip, und die Wurzeln roots.
"""
from __future__ import annotations

from ..geometry import Model
from ..paint import Ctx, hash01, hexc, mix, noise3, pick, rel_y

PALETTES = {
    "chorus_maw": {
        "stalk": [hexc(c) for c in ("#1e0e24", "#301838", "#46244e", "#5e3468", "#7a4a86", "#9a68a6", "#bc90c6")],
        "petal": [hexc(c) for c in ("#3a1e4e", "#5a3474", "#7e529a", "#a276ba", "#c29ad2", "#dcc0e6", "#f2e4f6")],
        "maw": [hexc(c) for c in ("#3a0630", "#6e1258", "#b02a8a", "#e852be", "#ff8ade", "#ffd0f2")],
        "tooth": [hexc(c) for c in ("#8a7a96", "#b4a6c0", "#dcd2e6", "#f6f0fa")],
        "root": [hexc(c) for c in ("#120812", "#1e1020", "#2c1a30", "#3e2844", "#54385c")],
    },
}


def build() -> Model:
    m = Model("chorus_maw", 128, 96)
    m.tex_res = 2
    # Wurzeln am Boden
    m.bone("roots", pivot=(0, 24.0, 0))
    for x, z, w, d in ((-6.0, -1.0, 4.0, 2.0), (2.0, -0.6, 4.2, 1.8), (-1.0, -6.0, 2.0, 4.2), (-0.8, 2.0, 1.8, 4.4)):
        m.cube("roots", (x, -1.2, z), (w, 1.2, d), "root")
    # Stamm aus drei Chorusgliedern, leicht gegeneinander verdreht
    m.bone("stalk1", pivot=(0, 24.0, 0))
    m.cube("stalk1", (-2.5, -6.2, -2.5), (5.0, 6.2, 5.0), "stalk")
    m.bone("stalk2", "stalk1", pivot=(0, -6.0, 0), rotation=(0, 12, 0))
    m.cube("stalk2", (-2.2, -5.8, -2.2), (4.4, 5.8, 4.4), "stalk")
    m.bone("stalk3", "stalk2", pivot=(0, -5.6, 0), rotation=(0, -10, 0))
    m.cube("stalk3", (-2.0, -5.0, -2.0), (4.0, 5.0, 4.0), "stalk")
    # Seitenäste mit geschlossenen Knospen
    m.bone("branch_left", "stalk1", pivot=(2.4, -4.4, 0))
    m.cube("branch_left", (0, -1.5, -1.5), (3.4, 3.0, 3.0), "stalk")
    m.bone("branch_left_up", "branch_left", pivot=(2.2, -1.4, 0))
    m.cube("branch_left_up", (-1.3, -4.0, -1.3), (2.6, 4.0, 2.6), "stalk")
    m.cube("branch_left_up", (-1.7, -6.2, -1.7), (3.4, 2.4, 3.4), "bud")
    m.bone("branch_right", "stalk2", pivot=(-2.1, -3.6, 0.6))
    m.cube("branch_right", (-3.0, -1.3, -1.3), (3.0, 2.6, 2.6), "stalk")
    m.bone("branch_right_up", "branch_right", pivot=(-2.0, -1.2, 0))
    m.cube("branch_right_up", (-1.1, -3.2, -1.1), (2.2, 3.2, 2.2), "stalk")
    m.cube("branch_right_up", (-1.5, -5.2, -1.5), (3.0, 2.2, 3.0), "bud")
    # die Blüte: unten der Kelch (innen der glühende Schlund, am Rand ein Zahnkranz), darunter vier hängende Kelchblätter
    m.bone("head", "stalk3", pivot=(0, -4.8, 0))
    m.cube("head", (-3.4, -2.8, -3.4), (6.8, 2.8, 6.8), "cup")
    for x in (-2.2, 0.0, 2.2):
        m.cube("head", (x - 0.35, -3.6, -2.9), (0.7, 0.9, 0.7), "tooth")
        m.cube("head", (x - 0.35, -3.6, 2.2), (0.7, 0.9, 0.7), "tooth")
    for z in (-1.1, 1.1):
        m.cube("head", (-2.9, -3.6, z - 0.35), (0.7, 0.9, 0.7), "tooth")
        m.cube("head", (2.2, -3.6, z - 0.35), (0.7, 0.9, 0.7), "tooth")
    for name, pivot, origin, size, rot in (
            ("sepal_left", (3.2, -0.6, 0), (0, 0, -2.2), (3.2, 0.6, 4.4), (0, 0, 38)),
            ("sepal_right", (-3.2, -0.6, 0), (-3.2, 0, -2.2), (3.2, 0.6, 4.4), (0, 0, -38)),
            ("sepal_front", (0, -0.6, -3.2), (-2.2, 0, -3.2), (4.4, 0.6, 3.2), (-38, 0, 0)),
            ("sepal_back", (0, -0.6, 3.2), (-2.2, 0, 0), (4.4, 0.6, 3.2), (38, 0, 0))):
        m.bone(name, "head", pivot=pivot, rotation=rot)
        m.cube(name, origin, size, "sepal")
    # vier große, spitze Blütenblätter, an der Oberkante des Kelchs angeschlagen: geschlossen neigen sie sich zur Knospe
    # zusammen, offen klappen sie weit auseinander; innen glühen sie, oben an der Innenseite sitzen Zähne
    for name, pivot, origin, size, rot, teeth in (
            ("petal_front", (0, -2.8, -3.4), (-3.4, -8.0, 0), (6.8, 8.0, 1.2), (-22, 0, 0),
             [((x - 0.35, -6.4, 1.2), (0.7, 1.0, 0.8)) for x in (-1.7, 0.0, 1.7)]),
            ("petal_back", (0, -2.8, 3.4), (-3.4, -8.0, -1.2), (6.8, 8.0, 1.2), (22, 0, 0),
             [((x - 0.35, -6.4, -2.0), (0.7, 1.0, 0.8)) for x in (-1.7, 0.0, 1.7)]),
            ("petal_left", (3.4, -2.8, 0), (-1.2, -8.0, -3.4), (1.2, 8.0, 6.8), (0, 0, -22),
             [((-2.0, -6.4, z - 0.35), (0.8, 1.0, 0.7)) for z in (-1.7, 0.0, 1.7)]),
            ("petal_right", (-3.4, -2.8, 0), (0, -8.0, -3.4), (1.2, 8.0, 6.8), (0, 0, 22),
             [((1.2, -6.4, z - 0.35), (0.8, 1.0, 0.7)) for z in (-1.7, 0.0, 1.7)])):
        m.bone(name, "head", pivot=pivot, rotation=rot)
        m.cube(name, origin, size, "petal")
        for o, sz in teeth:
            m.cube(name, o, sz, "fang")
    # Ranken: steigen schräg auf und rollen sich nach außen ein, an der Spitze eine Knospe
    for side, sx in (("left", 1), ("right", -1)):
        vine = f"vine_{side}"
        m.bone(vine, "stalk3", pivot=(1.8 * sx, -2.6, -0.4), rotation=(0, 0, -42 * sx))
        x0 = 0.0 if sx > 0 else -4.0
        m.cube(vine, (x0, -0.55, -0.55), (4.0, 1.1, 1.1), "vine")
        mid = f"vine_{side}_mid"
        m.bone(mid, vine, pivot=(3.8 * sx, 0, 0), rotation=(0, 0, 30 * sx))
        x1 = 0.0 if sx > 0 else -3.4
        m.cube(mid, (x1, -0.45, -0.45), (3.4, 0.9, 0.9), "vine")
        tip = f"vine_{side}_tip"
        m.bone(tip, mid, pivot=(3.2 * sx, 0, 0), rotation=(0, 0, 38 * sx))
        x2 = 0.0 if sx > 0 else -2.6
        m.cube(tip, (x2, -0.35, -0.35), (2.6, 0.7, 0.7), "vine")
        x3 = 2.2 if sx > 0 else -3.6
        m.cube(tip, (x3, -0.7, -0.7), (1.4, 1.4, 1.4), "bud")
    return m


# =============================================================================================
# Bemalung
# =============================================================================================

_LIT = {"top": 0.12, "front": 0.04, "west": 0.0, "east": -0.04, "back": -0.06, "bottom": -0.2}


def _stalk(ctx: Ctx):
    """Chorusglied: Längsrillen, an den Kanten heller, dazwischen helle Markpunkte (wie die Chorus-Pflanze)."""
    rs = (ctx.s + 0.5) / max(ctx.w, 1)
    groove = int(rs * 5) % 2 == 1
    edge = rs < 0.12 or rs > 0.88
    x, y, z = ctx.world
    v = 0.5 + _LIT[ctx.face] + (0.16 if edge else 0.0) - (0.1 if groove else 0.0) + (noise3(x * 0.6, y * 0.6, z * 0.6, 81) - 0.5) * 0.12
    if ctx.face in ("top", "bottom"):
        rt = (ctx.t + 0.5) / max(ctx.h, 1)
        d = max(abs(rs - 0.5), abs(rt - 0.5))
        return pick(ctx.pal["stalk"], 0.62 - d * 0.5 + _LIT[ctx.face])
    if hash01(ctx.u, ctx.v, 82) > 0.975:
        return pick(ctx.pal["petal"], 0.35)
    return pick(ctx.pal["stalk"], v)


def _petal_px(ctx: Ctx, v0: float = 0.6):
    """Blütenblatt: ein dunklerer Saum, innen hell, zur Mitte eine weiche Falte; leicht gesprenkelt."""
    rs = (ctx.s + 0.5) / max(ctx.w, 1)
    rt = (ctx.t + 0.5) / max(ctx.h, 1)
    d = max(abs(rs - 0.5), abs(rt - 0.5))
    if d > 0.42:
        return pick(ctx.pal["petal"], v0 - 0.32 + _LIT[ctx.face])
    crease = abs(rs - 0.5) < 0.06
    v = v0 + _LIT[ctx.face] - d * 0.25 - (0.1 if crease else 0.0) + (hash01(ctx.u, ctx.v, 86) - 0.5) * 0.08
    return pick(ctx.pal["petal"], v)


_NRM = {"west": (-1.0, 0.0, 0.0), "east": (1.0, 0.0, 0.0), "front": (0.0, 0.0, -1.0), "back": (0.0, 0.0, 1.0), "top": (0.0, -1.0, 0.0),
        "bottom": (0.0, 1.0, 0.0)}


def _inner(ctx: Ctx) -> bool:
    """Zeigt diese Seite eines Blütenblatts zur Mitte der Blüte (nach innen)?"""
    x, _, z = ctx.world
    n = _NRM[ctx.face]
    return n[0] * -x + n[2] * -z > 0.3


def _petal(ctx: Ctx):
    """
    Blütenblatt: spitz zulaufend (die Ecken oben bleiben leer). Außen lila mit dunklem Saum, heller Spitze und rosa
    Mittelader; innen glüht es magenta, von dunklen Adern durchzogen, zum Kelch hin am hellsten.
    """
    rs = (ctx.s + 0.5) / max(ctx.w, 1)
    rt = (ctx.t + 0.5) / max(ctx.h, 1)
    big = ctx.w > 4 and ctx.h > 4
    if ctx.face == "top":
        across = rs if ctx.w >= ctx.h else rt
        if abs(across - 0.5) > 0.12:
            return None
    elif big:
        if abs(rs - 0.5) > min(0.5, 0.12 + rt * 1.3):
            return None
    elif ctx.face != "bottom" and rt < 0.29:
        return None
    if not big:
        return pick(ctx.pal["petal"], 0.32 + _LIT[ctx.face])
    half = min(0.5, 0.12 + rt * 1.3)
    if _inner(ctx):
        vein = abs(rs - 0.5) < 0.05 or abs(abs(rs - 0.5) - half * 0.55) < 0.04 and rt > 0.3
        col = pick(ctx.pal["maw"], 0.25 + rt * 0.55 - (0.25 if vein else 0.0) + (hash01(ctx.u, ctx.v, 89) - 0.5) * 0.08)
        ctx.glow = mix((0, 0, 0, 255), col, 0.45 + rt * 0.4)
        return col
    if abs(rs - 0.5) > half - 0.09:
        return pick(ctx.pal["petal"], 0.26 + _LIT[ctx.face])            # der Saum
    if abs(rs - 0.5) < 0.05 and rt > 0.2:
        return mix(pick(ctx.pal["petal"], 0.44), pick(ctx.pal["maw"], 0.6), 0.3 + rt * 0.35)   # die Mittelader
    v = 0.74 - rt * 0.34 + _LIT[ctx.face] + (hash01(ctx.u, ctx.v, 86) - 0.5) * 0.08
    return pick(ctx.pal["petal"], v)


def _sepal(ctx: Ctx):
    """Kelchblatt: dunkelviolett, zur Spitze heller, mit einer feinen Ader."""
    x, y, z = ctx.world
    along = min(1.0, (abs(x) + abs(z)) / 6.5)
    if ctx.face in ("top", "bottom") and abs(x) > 0.1 and abs(z) > 0.1 and min(abs(x), abs(z)) < 0.35:
        return pick(ctx.pal["stalk"], 0.62)
    return pick(ctx.pal["stalk"], 0.42 + along * 0.2 + _LIT[ctx.face])


def _fang(ctx: Ctx):
    """Zähne innen an den Blütenblättern: bleich, zur Spitze heller."""
    return pick(ctx.pal["tooth"], 0.8 + _LIT[ctx.face] + (hash01(ctx.u, ctx.v, 90) - 0.5) * 0.1)


def _maw(ctx: Ctx):
    """Das Innere des Schlunds: Ringe aus glühendem Magenta, in der Mitte am hellsten."""
    rs = (ctx.s + 0.5) / max(ctx.w, 1)
    rt = (ctx.t + 0.5) / max(ctx.h, 1)
    d = ((rs - 0.5) ** 2 + (rt - 0.5) ** 2) ** 0.5
    ring = int(d * 9) % 2 == 0
    col = pick(ctx.pal["maw"], 0.95 - d * 1.1 - (0.12 if ring else 0.0))
    ctx.glow = mix((0, 0, 0, 255), col, 0.85)
    return col


def _cup(ctx: Ctx):
    if ctx.face == "top":
        return _maw(ctx)
    return _petal_px(ctx, 0.55)


def _tooth(ctx: Ctx):
    return pick(ctx.pal["tooth"], 0.75 - rel_y(ctx) * 0.2 + _LIT[ctx.face])


def _bud(ctx: Ctx):
    """Geschlossene Knospe: violett, oben ein glühender Spalt."""
    if ctx.face == "top":
        rs = (ctx.s + 0.5) / max(ctx.w, 1)
        if abs(rs - 0.5) < 0.18:
            col = pick(ctx.pal["maw"], 0.7)
            ctx.glow = col
            return col
    return pick(ctx.pal["petal"], 0.32 + _LIT[ctx.face] + (1 - rel_y(ctx)) * 0.15)


def _vine(ctx: Ctx):
    x, y, z = ctx.world
    v = 0.36 + _LIT[ctx.face] + (noise3(x, y, z, 83) - 0.5) * 0.14
    if hash01(ctx.u, ctx.v, 84) > 0.93:
        v += 0.25                                                  # kleine Dornen
    return pick(ctx.pal["stalk"], v)


def _root(ctx: Ctx):
    x, y, z = ctx.world
    return pick(ctx.pal["root"], 0.5 + _LIT[ctx.face] + (noise3(x, y, z, 85) - 0.5) * 0.25)


def materials(variant: str) -> dict:
    return {"stalk": _stalk, "petal": _petal, "cup": _cup, "sepal": _sepal, "fang": _fang, "tooth": _tooth, "bud": _bud,
            "vine": _vine, "root": _root}
