"""
Chorusschlund: eine fleischfressende Choruspflanze der äußeren End-Inseln, gut zwei Blöcke hoch. Ein Stamm aus kantigen,
violetten Chorusgliedern mit Seitenästen und geschlossenen Knospen, unten flache Wurzeln; oben eine große, fahl-lila
Chorusblüte – geschlossen sieht sie aus wie jede andere. Kommt Beute heran, klappt die obere Hälfte auf: Zähne ringsum,
innen glüht es magenta, die Blütenblätter spreizen sich. Zwei Peitschenranken mit Knospen hängen seitlich herab.

Knochen (für ChorusMawModel in Java): stalk1 → stalk2 → stalk3 → head (mit lid: der Deckel des Schlunds, Scharnier
hinten; petal_left/right/front/back), Seitenäste branch_left (an stalk1) und branch_right (an stalk2) mit *_up, die Ranken
vine_left/vine_right (an stalk3) → *_mid → *_tip, und die Wurzeln roots.
"""
from __future__ import annotations

from ..geometry import Model
from ..paint import Ctx, hash01, hexc, mix, noise3, pick, rel_y

PALETTES = {
    "chorus_maw": {
        "stalk": [hexc(c) for c in ("#1e0e24", "#301838", "#46244e", "#5e3468", "#7a4a86", "#9a68a6", "#bc90c6")],
        "petal": [hexc(c) for c in ("#4a3660", "#6a5284", "#8e76a6", "#b29cc6", "#d2c2e0", "#ece2f4", "#faf6fe")],
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
    # die Blüte: unten der Kelch, oben der Deckel (Scharnier hinten); Zähne an beiden Rändern, innen das Glühen
    m.bone("head", "stalk3", pivot=(0, -4.8, 0))
    m.cube("head", (-3.5, -3.4, -3.5), (7.0, 3.4, 7.0), "cup")
    for x in (-2.5, -0.8, 0.9, 2.6):
        m.cube("head", (x - 0.35, -4.3, -3.2), (0.7, 1.0, 0.7), "tooth")
    for z in (-1.4, 0.6):
        m.cube("head", (-3.2, -4.3, z - 0.35), (0.7, 1.0, 0.7), "tooth")
        m.cube("head", (2.5, -4.3, z - 0.35), (0.7, 1.0, 0.7), "tooth")
    m.bone("lid", "head", pivot=(0, -3.4, 3.5))
    m.cube("lid", (-3.7, -4.4, -7.2), (7.4, 4.4, 7.4), "lid")
    m.cube("lid", (-2.3, -5.4, -5.8), (4.6, 1.0, 4.6), "crown")                    # die Krone der Blüte
    for x in (-2.3, -0.5, 1.3):
        m.cube("lid", (x - 0.35, -0.2, -6.8), (0.7, 1.1, 0.7), "tooth")
    for z in (-5.2, -3.0):
        m.cube("lid", (-3.4, -0.2, z - 0.35), (0.7, 1.1, 0.7), "tooth")
        m.cube("lid", (2.7, -0.2, z - 0.35), (0.7, 1.1, 0.7), "tooth")
    # vier große Blütenblätter um den Kelch (spreizen sich weiter, wenn der Schlund aufgeht)
    for name, pivot, origin, size, rot in (
            ("petal_left", (3.4, -0.6, 0), (0, -5.6, -3.2), (0.9, 6.0, 6.4), (0, 0, -24)),
            ("petal_right", (-3.4, -0.6, 0), (-0.9, -5.6, -3.2), (0.9, 6.0, 6.4), (0, 0, 24)),
            ("petal_front", (0, -0.6, -3.4), (-3.2, -5.6, -0.9), (6.4, 6.0, 0.9), (24, 0, 0)),
            ("petal_back", (0, -0.6, 3.4), (-3.2, -5.6, 0), (6.4, 6.0, 0.9), (-24, 0, 0))):
        m.bone(name, "head", pivot=pivot, rotation=rot)
        m.cube(name, origin, size, "petal")
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


def _petal(ctx: Ctx):
    return _petal_px(ctx)


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


def _lid(ctx: Ctx):
    if ctx.face == "bottom":
        return _maw(ctx)
    return _petal_px(ctx, 0.62)


def _crown(ctx: Ctx):
    """Die Krone oben: die Mitte der Blüte, dunkler violett mit hellem Rand."""
    rs = (ctx.s + 0.5) / max(ctx.w, 1)
    rt = (ctx.t + 0.5) / max(ctx.h, 1)
    d = max(abs(rs - 0.5), abs(rt - 0.5))
    return pick(ctx.pal["petal"], 0.15 + d * 0.9 + _LIT[ctx.face])


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
    return {"stalk": _stalk, "petal": _petal, "cup": _cup, "lid": _lid, "crown": _crown, "tooth": _tooth, "bud": _bud,
            "vine": _vine, "root": _root}
