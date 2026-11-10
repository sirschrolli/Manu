"""
Nachtwidder: ein mächtiger Widder der Schattengipfel und der Bleichen Öde – zottiges, fast schwarzes Vlies mit violettem
Schimmer und frostbleichen Spitzen, ein Buckel über den Schultern, ein langer Frostbart. Seine gewaltigen Hörner sind aus
Nachtkristall: blauviolett, geriffelt, von glimmenden Adern durchzogen, in einer Spirale um die Ohren gedreht. Die Augen
leuchten blassblau.

Knochen (für NightRamModel in Java): body (mit shag: Zottelvlies, tail), head (mit jaw, den Hörnern left_horn/right_horn
und ihren Gliedern *_horn2 … *_horn5, beard), die Beine left_front_leg, right_front_leg, left_hind_leg, right_hind_leg.
"""
from __future__ import annotations

from ..geometry import Model
from ..paint import Ctx, Voronoi3D, hash01, hexc, mix, noise3, pick, rel_y

PALETTES = {
    "night_ram": {
        "fleece": [hexc(c) for c in ("#0c0a10", "#16121c", "#221c2a", "#30283a", "#40364c", "#544a62", "#6e6680")],
        "frost": [hexc(c) for c in ("#5a5e7a", "#7a82a0", "#9ea8c4", "#c4ccdf", "#e4eaf6")],
        "face": [hexc(c) for c in ("#100c14", "#1c1622", "#2a2232", "#3a3044", "#4c4058", "#60546e", "#786c88")],
        "horn": [hexc(c) for c in ("#0c0a20", "#181838", "#262a56", "#363e7a", "#4a56a0", "#6474c4", "#8ea2e6")],
        "vein": [hexc(c) for c in ("#2a4a9a", "#4a7ad8", "#7aaaf8", "#b4d4ff", "#e8f4ff")],
        "eye": [hexc(c) for c in ("#2a5a9a", "#5a9ad8", "#a8d8ff", "#e8f8ff")],
        "hoof": [hexc(c) for c in ("#060508", "#0e0c12", "#1a1620", "#2a2432", "#3c3446")],
    },
}

_CURL = Voronoi3D(1.6, 51)


def build() -> Model:
    m = Model("night_ram", 128, 128)
    m.tex_res = 2
    # Rumpf mit Buckel; darüber das Zottelvlies (unten ausgefranst)
    m.bone("body", pivot=(0, 9.0, 1.0))
    m.cube("body", (-5.0, -5.0, -8.0), (10, 9, 16), "fleece")
    m.cube("body", (-4.4, -6.8, -8.2), (8.8, 2.4, 7.5), "fleece")                  # Buckel
    m.bone("shag", "body", pivot=(0, 0, 0))
    m.cube("shag", (-5.6, -5.4, -8.5), (11.2, 10.6, 17.0), "shag")
    m.bone("tail", "body", pivot=(0, -3.0, 8.4), rotation=(38, 0, 0))
    m.cube("tail", (-1.6, -0.5, -0.5), (3.2, 4.2, 2.2), "fleece")
    # Kopf: Hals, Schädel, Schnauze, Nase, Augen; Unterkiefer und Bart
    m.bone("head", "body", pivot=(0, -3.2, -8.0), rotation=(18, 0, 0))
    m.cube("head", (-2.8, -2.8, -4.6), (5.6, 6.4, 5.4), "fleece")                  # Hals
    m.cube("head", (-3.2, -5.9, -9.8), (6.4, 6.4, 6.8), "face")                   # Schädel
    m.cube("head", (-2.4, -4.0, -13.8), (4.8, 4.4, 4.4), "muzzle")                # Schnauze (Ramsnase)
    m.cube("head", (-1.5, -3.2, -14.1), (3.0, 1.4, 0.4), "nose")
    m.cube("head", (3.15, -4.4, -9.4), (0.25, 0.9, 1.5), "eye")
    m.cube("head", (-3.4, -4.4, -9.4), (0.25, 0.9, 1.5), "eye")
    m.bone("jaw", "head", pivot=(0, 0.2, -9.4), rotation=(4, 0, 0))
    m.cube("jaw", (-1.9, 0, -3.6), (3.8, 1.2, 3.6), "muzzle")
    m.bone("beard", "jaw", pivot=(0, 1.0, -2.0), rotation=(-8, 0, 0))
    m.cube("beard", (-1.2, 0, -1.0), (2.4, 4.6, 2.0), "beard")
    # Hörner: je fünf Glieder, erst nach hinten oben, dann in einer Spirale herab und um das Ohr nach vorn
    sizes = [(2.9, 3.4), (2.6, 3.2), (2.3, 3.0), (1.9, 2.7), (1.4, 2.4)]
    turns = [(48, 26, 0), (-58, 10, 0), (-58, 10, 0), (-56, 8, 0), (-50, 6, 0)]
    for side, sx in (("left", 1), ("right", -1)):
        parent = "head"
        pivot = (2.6 * sx, -5.2, -6.4)
        for i, ((w, length), (rx, ry, rz)) in enumerate(zip(sizes, turns)):
            name = f"{side}_horn" if i == 0 else f"{side}_horn{i + 1}"
            m.bone(name, parent, pivot=pivot, rotation=(rx, ry * sx, rz))
            m.cube(name, (-w / 2, -w / 2, 0), (w, w, length + 0.4), "horn")
            parent = name
            pivot = (0, 0, length)
    # Beine: oben zottig, unten schlank, schwarze Hufe
    for name, x, z in (("left_front_leg", 3.1, -5.6), ("right_front_leg", -3.1, -5.6), ("left_hind_leg", 3.1, 6.0),
                       ("right_hind_leg", -3.1, 6.0)):
        m.bone(name, pivot=(x, 12.0, z))
        m.cube(name, (-1.8, -0.5, -1.8), (3.6, 5.6, 3.6), "shag")
        m.cube(name, (-1.2, 4.5, -1.2), (2.4, 6.2, 2.4), "leg")
        m.cube(name, (-1.4, 10.4, -1.5), (2.8, 1.6, 2.9), "hoof")
    return m


# =============================================================================================
# Bemalung
# =============================================================================================

_LIT = {"top": 0.12, "front": 0.04, "west": 0.0, "east": -0.04, "back": -0.06, "bottom": -0.2}


def _curls(ctx: Ctx, base: float) -> tuple:
    """Wollige Locken: Zellen mit hellem Rand oben, dunklem Kern; die Spitzen oben bereift."""
    x, y, z = ctx.world
    cell, inner = _CURL(x, y * 1.2, z)                     # inner: 0 am Rand der Locke, 1 in ihrer Mitte
    v = base + _LIT[ctx.face] + (inner - 0.45) * 0.26 + (noise3(x * 0.3, y * 0.3, z * 0.3, 52) - 0.5) * 0.12
    # Reif nur oben auf den Locken (Rücken, Buckel), an den Seiten nur in der obersten Reihe
    if (ctx.face == "top" or (rel_y(ctx) < 0.06 and ctx.face != "bottom")) and inner > 0.62 \
            and hash01(int(cell * 1000), 53) > 0.55:
        return pick(ctx.pal["frost"], 0.15 + (inner - 0.62) * 0.9)
    return pick(ctx.pal["fleece"], v)


def _fleece(ctx: Ctx):
    return _curls(ctx, 0.48)


def _shag(ctx: Ctx):
    """Zottelvlies: wie das Vlies, an der Unterkante in langen Fransen (Lücken durchsichtig)."""
    if ctx.face == "bottom":
        return pick(ctx.pal["fleece"], 0.2)
    ry = rel_y(ctx)
    if ry > 0.86 and ctx.face != "top":
        strand = hash01(ctx.u // 2, 55)
        if ry > 0.86 + strand * 0.13:
            return None
        return pick(ctx.pal["fleece"], 0.3 + strand * 0.2)
    return _curls(ctx, 0.5)


def _face(ctx: Ctx):
    x, y, z = ctx.world
    v = 0.5 + _LIT[ctx.face] + (noise3(x * 0.9, y * 0.9, z * 0.9, 56) - 0.5) * 0.14
    # Stirnlocke oben
    if ctx.face == "top" or ctx.local[1] < -4.6:
        return _curls(ctx, 0.5)
    return pick(ctx.pal["face"], v)


def _muzzle(ctx: Ctx):
    x, y, z = ctx.world
    v = 0.7 + _LIT[ctx.face] + (noise3(x, y, z, 57) - 0.5) * 0.1 - (0.15 if ctx.face == "bottom" else 0.0)
    return pick(ctx.pal["face"], v)


def _nose(ctx: Ctx):
    return pick(ctx.pal["hoof"], 0.15)


def _eye(ctx: Ctx):
    rs = (ctx.s + 0.5) / max(ctx.w, 1)
    col = pick(ctx.pal["eye"], 0.95 if 0.3 < rs < 0.7 else 0.55)
    ctx.glow = col
    return col


def _beard(ctx: Ctx):
    ry = rel_y(ctx)
    if ry > 0.8 and hash01(ctx.u, 58) > 0.55:
        return None
    return pick(ctx.pal["frost"], 0.2 + (1 - ry) * 0.3 + (hash01(ctx.u, 59) - 0.5) * 0.2)


def _horn(ctx: Ctx):
    """Nachtkristall: geriffelt (Ringe quer), oben eine helle Facette, dazwischen glimmende Adern."""
    lx, ly, lz = ctx.local
    x, y, z = ctx.world
    ridge = (lz * 1.25) % 1.0
    v = 0.42 + _LIT[ctx.face] + (0.16 if ridge < 0.28 else 0.0) - (0.12 if ridge > 0.82 else 0.0)
    if ctx.face == "top":
        v += 0.12
    vein = abs(noise3(x * 0.7, y * 0.7, z * 0.7, 60) - 0.5)
    if vein < 0.022 and ctx.face != "bottom":
        col = pick(ctx.pal["vein"], 0.5 + (0.022 - vein) * 14)
        ctx.glow = mix((0, 0, 0, 255), col, 0.85)
        return col
    return pick(ctx.pal["horn"], v + (hash01(ctx.u, ctx.v, 61) - 0.5) * 0.08)


def _leg(ctx: Ctx):
    x, y, z = ctx.world
    return pick(ctx.pal["face"], 0.42 + _LIT[ctx.face] + (noise3(x, y, z, 62) - 0.5) * 0.12)


def _hoof(ctx: Ctx):
    ry = rel_y(ctx)
    return pick(ctx.pal["hoof"], 0.55 - ry * 0.3 + _LIT[ctx.face])


def materials(variant: str) -> dict:
    return {"fleece": _fleece, "shag": _shag, "face": _face, "muzzle": _muzzle, "nose": _nose, "eye": _eye, "beard": _beard,
            "horn": _horn, "leg": _leg, "hoof": _hoof}
