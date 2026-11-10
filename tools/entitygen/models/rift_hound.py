"""
Rissjäger: ein hagerer Jagdhund des Ends, schwarz wie die Leere zwischen den Inseln, mit violetten Sternsprenkeln auf der
Haut. Wo sein Leib gerissen ist, glüht es magenta – entlang des Rückgrats, über die Rippen und quer über das augenlose
Gesicht; das gespaltene Maul glüht innen violett. Lange, dünne Läufe, ein Peitschenschwanz mit einem Risssplitter an der
Spitze und drei Kristallsplitter, die über seinem Rücken schweben.

Knochen (für RiftHoundModel in Java): body (mit spine), head (mit jaw, left_spike/right_spike), tail → tail2 → tail3,
die Beine left_front_leg (→ left_front_shin), right_front_leg, left_hind_leg (→ left_hind_shin), right_hind_leg und die
schwebenden Splitter shard1..shard3 (an der Wurzel, damit sie für sich kreisen).
"""
from __future__ import annotations

from ..geometry import Model
from ..paint import Ctx, hash01, hexc, mix, noise3, pick, rel_y

PALETTES = {
    "rift_hound": {
        "skin": [hexc(c) for c in ("#040308", "#0a0812", "#12101e", "#1c172c", "#28203e", "#362c54", "#483c6c")],
        "star": [hexc(c) for c in ("#6a4aa8", "#9a7ad8", "#c8b0ff", "#f0e8ff")],
        "crack": [hexc(c) for c in ("#4a0858", "#8a1898", "#c832d8", "#f45cec", "#ffa8f8", "#ffe4fc")],
        "shard": [hexc(c) for c in ("#22093e", "#3e1468", "#62309a", "#9058cc", "#bc90ec", "#e6d0ff")],
    },
}


def build() -> Model:
    m = Model("rift_hound", 96, 96)
    m.tex_res = 2
    # Leib: tiefer Brustkorb, schmale Lende; über dem Rückgrat ein glühender Riss
    m.bone("body", pivot=(0, 9.5, 0), rotation=(-4, 0, 0))
    m.cube("body", (-2.8, -3.2, -6.4), (5.6, 6.2, 7.4), "chest")
    m.cube("body", (-2.0, -2.8, 0.8), (4.0, 4.4, 6.4), "skin")
    m.bone("spine", "body", pivot=(0, -3.2, 0))
    m.cube("spine", (-0.5, -0.6, -6.0), (1.0, 0.8, 12.6), "spine")
    # Kopf: Hals, augenloser Schädel, lange Schnauze; das Maul glüht innen; zwei Kristalldornen nach hinten
    m.bone("head", "body", pivot=(0, -1.0, -6.2), rotation=(-14, 0, 0))
    m.cube("head", (-1.7, -2.2, -3.6), (3.4, 3.6, 4.0), "skin")
    m.cube("head", (-2.1, -3.4, -7.6), (4.2, 3.9, 4.4), "skull")
    m.cube("head", (-1.45, -2.5, -12.0), (2.9, 2.3, 4.6), "snout")
    m.bone("jaw", "head", pivot=(0, -0.3, -7.4), rotation=(8, 0, 0))
    m.cube("jaw", (-1.3, 0, -4.6), (2.6, 1.0, 4.6), "jaw")
    for side, sx in (("left", 1), ("right", -1)):
        spike = f"{side}_spike"
        m.bone(spike, "head", pivot=(1.3 * sx, -3.2, -4.4), rotation=(62, 14 * sx, 0))
        m.cube(spike, (-0.5, -0.5, 0), (1.0, 1.0, 3.6), "shard")
    # Peitschenschwanz mit Risssplitter
    m.bone("tail", "body", pivot=(0, -1.6, 7.0), rotation=(-24, 0, 0))
    m.cube("tail", (-0.55, -0.55, 0), (1.1, 1.1, 4.4), "skin")
    m.bone("tail2", "tail", pivot=(0, 0, 4.2), rotation=(22, 0, 0))
    m.cube("tail2", (-0.45, -0.45, 0), (0.9, 0.9, 4.2), "skin")
    m.bone("tail3", "tail2", pivot=(0, 0, 4.0), rotation=(18, 0, 0))
    m.cube("tail3", (-0.35, -0.35, 0), (0.7, 0.7, 3.0), "skin")
    m.cube("tail3", (-0.7, -0.7, 2.8), (1.4, 1.4, 2.2), "shard")
    # lange, dünne Läufe
    for side, sx in (("left", 1), ("right", -1)):
        front = f"{side}_front_leg"
        m.bone(front, pivot=(1.8 * sx, 11.0, -4.4))
        m.cube(front, (-0.9, -1.0, -1.0), (1.8, 6.6, 2.0), "skin")
        shin = f"{side}_front_shin"
        m.bone(shin, front, pivot=(0, 5.4, 0), rotation=(-4, 0, 0))
        m.cube(shin, (-0.65, 0, -0.65), (1.3, 7.0, 1.3), "skin")
        m.cube(shin, (-0.85, 6.6, -1.7), (1.7, 0.9, 2.5), "paw")
        hind = f"{side}_hind_leg"
        m.bone(hind, pivot=(1.6 * sx, 11.0, 5.6), rotation=(-12, 0, 0))
        m.cube(hind, (-1.0, -1.2, -1.4), (2.0, 6.2, 2.8), "skin")
        hshin = f"{side}_hind_shin"
        m.bone(hshin, hind, pivot=(0, 4.8, 0.4), rotation=(14, 0, 0))
        m.cube(hshin, (-0.6, 0, -0.6), (1.2, 7.6, 1.2), "skin")
        m.cube(hshin, (-0.85, 7.0, -1.9), (1.7, 0.9, 2.5), "paw")
    # drei schwebende Kristallsplitter über dem Rücken
    for i, (x, y, z, rx, rz) in enumerate(((0.0, 2.4, -3.2, 20, 30), (1.0, 1.6, 0.6, -15, -25), (-0.8, 2.0, 4.0, 30, 15))):
        shard = f"shard{i + 1}"
        m.bone(shard, pivot=(x, y, z), rotation=(rx, 0, rz))
        m.cube(shard, (-0.6, -1.5, -0.6), (1.2, 3.0, 1.2), "shard")
    return m


# =============================================================================================
# Bemalung
# =============================================================================================

_LIT = {"top": 0.12, "front": 0.04, "west": 0.0, "east": -0.04, "back": -0.06, "bottom": -0.2}


def _stars(ctx: Ctx, v: float) -> tuple:
    """Leerenhaut mit Sternsprenkeln (die hellsten leuchten schwach)."""
    h = hash01(ctx.u, ctx.v, 71)
    if h > 0.986:
        col = pick(ctx.pal["star"], 0.6 + (h - 0.986) * 25)
        ctx.glow = mix((0, 0, 0, 255), col, 0.55)
        return col
    if h > 0.965:
        return pick(ctx.pal["star"], 0.15)
    return pick(ctx.pal["skin"], v)


def _skin(ctx: Ctx):
    x, y, z = ctx.world
    v = 0.42 + _LIT[ctx.face] + (noise3(x * 0.5, y * 0.5, z * 0.5, 72) - 0.5) * 0.16
    return _stars(ctx, v)


def _crack_col(ctx: Ctx, strength: float):
    col = pick(ctx.pal["crack"], 0.45 + strength * 0.5)
    ctx.glow = mix((0, 0, 0, 255), col, 0.65 + strength * 0.35)
    return col


def _chest(ctx: Ctx):
    """Brustkorb: an den Seiten glühen die Rippen als Risse durch."""
    if ctx.face in ("west", "east"):
        lz = ctx.local[2]
        ry = rel_y(ctx)
        # gezackte Risse statt gerader Streifen: jede Rippe springt um einen Texel hin und her
        jag = 0.18 if int(ry * 7) % 2 else -0.18
        rib = (lz + 6.4 + jag) % 1.85
        if rib < 0.5 and 0.22 < ry < 0.84:
            return _crack_col(ctx, 0.3 + (0.5 - rib) * 0.6)
    return _skin(ctx)


def _crack(ctx: Ctx):
    return _crack_col(ctx, 0.7 + (hash01(ctx.u, ctx.v, 73) - 0.5) * 0.3)


def _spine(ctx: Ctx):
    """Rückgrat: ein gezackter Riss in der Mitte, daneben dunkle Haut (aufgesprungene Ränder)."""
    if ctx.face == "top":
        lz = ctx.local[2]
        center = (ctx.w - 1) / 2 + (0.5 if int((lz + 6.0) * 1.2) % 2 else -0.5)
        if abs(ctx.s - center) < 0.7:
            return _crack_col(ctx, 0.45 + (hash01(ctx.u, ctx.v, 76) - 0.5) * 0.3)
        return pick(ctx.pal["skin"], 0.25)
    return pick(ctx.pal["skin"], 0.2 + _LIT[ctx.face])


def _skull(ctx: Ctx):
    """Augenloser Schädel: ein gezackter Riss läuft von der Stirn über die Mitte bis zur Schnauze."""
    x, y, z = ctx.world
    if ctx.face in ("top", "front"):
        center = (ctx.w - 1) / 2 + (1 if int(abs(z) * 1.3) % 2 else -1) * 0.6
        if abs(ctx.s - center) < 0.9:
            return _crack_col(ctx, 0.75)
    if ctx.face in ("west", "east") and abs(rel_y(ctx) - 0.42) < 0.08:
        return pick(ctx.pal["skin"], 0.15)            # wo Augen sein müssten: eine dunkle Narbe
    return _skin(ctx)


def _snout(ctx: Ctx):
    if ctx.face == "bottom":
        return _crack_col(ctx, 0.3)                    # das Maul glüht innen
    if ctx.face == "top":
        center = (ctx.w - 1) / 2
        if abs(ctx.s - center) < 0.7:
            return _crack_col(ctx, 0.55)
    return _skin(ctx)


def _jaw(ctx: Ctx):
    if ctx.face == "top":
        return _crack_col(ctx, 0.35)
    return _skin(ctx)


def _paw(ctx: Ctx):
    # Krallen vorn hell
    if ctx.face == "front" and ctx.s % 2 == 0:
        return pick(ctx.pal["shard"], 0.75)
    return pick(ctx.pal["skin"], 0.3 + _LIT[ctx.face])


def _shard(ctx: Ctx):
    """Kristallsplitter: je Seite eine helle Schliffkante, zur Mitte dunkler; leuchtet schwach von innen."""
    rs = (ctx.s + 0.5) / max(ctx.w, 1)
    rt = (ctx.t + 0.5) / max(ctx.h, 1)
    edge = rs < 0.3
    v = 0.42 + _LIT[ctx.face] + (0.3 if edge else 0.0) + (0.5 - abs(rt - 0.5)) * 0.2
    col = pick(ctx.pal["shard"], v)
    ctx.glow = mix((0, 0, 0, 255), col, 0.4 + (0.35 if edge else 0.0))
    return col


def materials(variant: str) -> dict:
    return {"skin": _skin, "chest": _chest, "crack": _crack, "spine": _spine, "skull": _skull, "snout": _snout, "jaw": _jaw,
            "paw": _paw, "shard": _shard}
