"""
Moorschlurfer: ein Haufen lebendig gewordener Moorerde, gut zwei Blöcke hoch – ein mächtiger, gebeugter Buckel aus
nassem Schlamm und Torf, über und über mit Moos bewachsen, aus dem ein toter Ast, Rohrkolben und ein paar Seerosenblätter
ragen und auf dem leuchtende Baumpilze wachsen; der kleine Kopf sitzt tief zwischen den Schultern, aus dem Maul hängen
Wurzeln, zwei grüne Lichter glimmen darin. Lange Arme, von denen Moosbärte hängen, schleifen über den Boden und enden in
Fingern aus Wurzeln, die Beine sind kurze, schwere Stümpfe. Überall tropft es.

Knochen-Namen werden in MireShamblerModel (Java) für die Animationen verwendet (schlurfen, Schlammwurf, Wurzelgriff,
Aufsteigen aus dem Schlamm).
"""
from __future__ import annotations

from ..geometry import Model
from ..paint import Ctx, hash01, hexc, mix, noise3, pick, rel_y

PALETTES = {
    "mire_shambler": {
        "mud": [hexc(c) for c in ("#140e0a", "#21170f", "#2f2216", "#3e2e1e", "#4e3a26", "#614a32", "#7a6044")],
        "peat": [hexc(c) for c in ("#0e0a08", "#1a120c", "#261a12", "#33241a")],
        "moss": [hexc(c) for c in ("#1c2a10", "#2c4216", "#3e5a1c", "#557424", "#6e8e30", "#8aaa44")],
        "root": [hexc(c) for c in ("#2a1a0e", "#3e2816", "#563a20", "#70502c", "#8c6a3c")],
        "wood": [hexc(c) for c in ("#2e2a26", "#46403a", "#605850", "#7c7268", "#9a9084")],
        "lily": [hexc(c) for c in ("#1e3a16", "#2e5420", "#40702a", "#589038")],
        "eye": [hexc(c) for c in ("#1a5a12", "#46b02a", "#9cf05a", "#e4ffb0")],
        "wet": [hexc(c) for c in ("#5a5a46", "#8a8a6c", "#b8b898")],
        "reed": [hexc(c) for c in ("#2a3416", "#3e4c1e", "#56662a", "#708238", "#8c9c4a")],
        "cattail": [hexc(c) for c in ("#2a170c", "#3e2412", "#56341a", "#704626", "#8a5a34")],
        "glowcap": [hexc(c) for c in ("#0e3a34", "#1a6a5a", "#2ea88a", "#6ae0b8", "#c4ffe4")],
    },
}


def build() -> Model:
    m = Model("mire_shambler", 128, 128)
    m.tex_res = 2
    # Leib: ein gebeugter Buckel aus Schlamm
    m.bone("body", pivot=(0, 12.0, 1.0), rotation=(22, 0, 0))
    m.cube("body", (-7.0, -14.0, -5.0), (14, 14, 10), "mud")
    m.cube("body", (-6.0, -17.5, -3.5), (12, 4.0, 9), "mud")                 # Buckel
    m.cube("body", (-7.6, -15.0, -4.5), (15.2, 6.0, 9.6), "moss_mud")        # Moospolster auf den Schultern
    m.cube("body", (-5.0, -2.0, -5.8), (10, 6.0, 9), "mud")                  # Bauch, schwer hängend
    m.cube("body", (-3.0, -19.5, 0.0), (6, 2.5, 4), "moss_mud")
    # toter Ast und Seerosenblätter auf dem Rücken
    m.bone("branch", "body", pivot=(2.5, -17.0, 2.0), rotation=(-25, 20, 18))
    m.cube("branch", (-0.6, -9.0, -0.6), (1.2, 9.0, 1.2), "wood")
    m.cube("branch", (-0.4, -6.0, -0.4), (3.0, 0.8, 0.8), "wood")
    m.cube("branch", (-2.6, -8.0, -0.4), (2.6, 0.7, 0.7), "wood")
    m.cube("body", (-6.5, -17.8, -2.0), (4.0, 0.4, 4.0), "lily")
    m.cube("body", (3.0, -16.0, 2.5), (3.5, 0.4, 3.5), "lily")
    # Rohrkolben wachsen aus dem Rücken
    m.bone("reeds", "body", pivot=(-3.6, -16.0, 2.4), rotation=(-16, -12, -12))
    for dx, dz, h in ((0.0, 0.0, 10.0), (1.7, 1.1, 8.2), (-1.5, 0.9, 7.0)):
        m.cube("reeds", (dx - 0.3, -h, dz - 0.3), (0.6, h, 0.6), "reed")
        m.cube("reeds", (dx - 0.6, -h + 0.9, dz - 0.6), (1.2, 3.2, 1.2), "cattail")
        m.cube("reeds", (dx - 0.15, -h - 1.4, dz - 0.15), (0.3, 1.6, 0.3), "reed")
    # leuchtende Baumpilze an Schulter und Flanke
    m.cube("body", (-8.4, -12.6, -2.4), (1.8, 0.6, 2.6), "glowcap")
    m.cube("body", (-8.1, -10.4, 0.2), (1.4, 0.5, 2.0), "glowcap")
    m.cube("body", (-7.8, -8.6, -1.6), (1.1, 0.4, 1.5), "glowcap")
    m.cube("body", (5.6, -16.4, 3.2), (2.0, 0.5, 2.0), "glowcap")
    # Kopf: klein, tief zwischen den Schultern
    m.bone("head", "body", pivot=(0, -10.0, -5.0), rotation=(-18, 0, 0))
    m.cube("head", (-3.5, -4.0, -5.0), (7, 6, 5.5), "mud")
    m.cube("head", (-3.9, -4.6, -4.0), (7.8, 2.0, 4.0), "moss_mud")          # Moosbrauen
    m.cube("head", (-2.8, -2.4, -5.3), (1.8, 1.2, 0.4), "eye")
    m.cube("head", (1.0, -2.4, -5.3), (1.8, 1.2, 0.4), "eye")
    m.bone("jaw", "head", pivot=(0, 1.0, -2.0), rotation=(12, 0, 0))
    m.cube("jaw", (-3.0, 0, -3.2), (6, 2.6, 4.0), "mud")
    m.cube("jaw", (-2.2, 2.6, -3.0), (0.8, 3.0, 0.8), "root")                # Wurzeln hängen aus dem Maul
    m.cube("jaw", (0.4, 2.6, -2.6), (0.8, 4.0, 0.8), "root")
    m.cube("jaw", (1.6, 2.6, -3.1), (0.7, 2.2, 0.7), "root")
    # Arme: lang, schleifen über den Boden, Finger aus Wurzeln
    for side, sx in (("left", 1), ("right", -1)):
        arm = f"{side}_arm"
        m.bone(arm, "body", pivot=(7.5 * sx, -12.0, -1.0), rotation=(-26, 0, -6 * sx))
        m.cube(arm, (-2.8, -2.0, -3.0), (5.6, 11.0, 6.0), "mud")
        m.cube(arm, (-3.2, -2.6, -3.3), (6.4, 4.4, 6.6), "moss_mud")             # Schulter
        fore = f"{side}_forearm"
        m.bone(fore, arm, pivot=(0, 8.6, 0), rotation=(-12, 0, 0))
        m.cube(fore, (-2.6, 0, -2.6), (5.2, 10.0, 5.2), "mud")
        m.cube(fore, (-3.0, 8.5, -3.0), (6.0, 3.0, 6.0), "peat")                # Hand
        m.cube(fore, (-3.1, 1.0, -3.1), (6.2, 7.5, 6.2), "strands")             # Moosbart, der vom Arm hängt
        for k, (dx, dz, h) in enumerate(((-1.8, -1.8, 4.2), (0.0, -2.2, 5.0), (1.8, -1.8, 4.4), (-1.6, 1.4, 3.4), (1.6, 1.4, 3.6))):
            m.cube(fore, (dx - 0.45, 11.0, dz - 0.45), (0.9, h, 0.9), "root")
    # Beine: kurze, schwere Stümpfe
    for side, sx in (("left", 1), ("right", -1)):
        leg = f"{side}_leg"
        m.bone(leg, pivot=(3.8 * sx, 12.5, 1.0))
        m.cube(leg, (-3.0, 0, -3.0), (6.0, 9.5, 6.0), "mud")
        m.cube(leg, (-3.4, 8.5, -3.8), (6.8, 3.0, 7.2), "peat")
    return m


# =============================================================================================
# Bemalung
# =============================================================================================

_LIT = {"top": 0.12, "front": 0.04, "west": 0.0, "east": -0.04, "back": -0.06, "bottom": -0.18}


def _mud_value(ctx: Ctx) -> float:
    x, y, z = ctx.world
    big = (noise3(x * 0.14, y * 0.14, z * 0.14, 91) - 0.5) * 0.3
    clump = (noise3(x * 0.55, y * 0.55, z * 0.55, 92) - 0.5) * 0.22
    return 0.48 + _LIT[ctx.face] - rel_y(ctx) * 0.12 + big + clump


def _mud(ctx: Ctx):
    x, y, z = ctx.world
    v = _mud_value(ctx)
    # nasse Glanzlichter
    if hash01(ctx.u, ctx.v, 93) > 0.992 and ctx.face in ("top", "front", "west", "east"):
        return mix(pick(ctx.pal["mud"], v), pick(ctx.pal["wet"], 0.5), 0.5)
    # Tropfspuren: senkrechte, dunklere Bahnen
    if ctx.face in ("front", "back", "west", "east") and noise3(x * 1.4, 0.0, z * 1.4, 94) > 0.74:
        v -= 0.12
    # einzelne Wurzelfasern im Schlamm
    if noise3(x * 0.9, y * 0.3, z * 0.9, 95) > 0.8:
        return pick(ctx.pal["root"], 0.4 + (v - 0.4) * 0.5)
    return pick(ctx.pal["mud"], v)


def _moss_mud(ctx: Ctx):
    """Oben Moos (in weichen Polstern, mit dunklem Rand), darunter Schlamm."""
    x, y, z = ctx.world
    m = noise3(x * 0.3, y * 0.3, z * 0.3, 96) + (0.5 - rel_y(ctx)) * 0.5 + (0.25 if ctx.face == "top" else 0.0)
    if m > 0.62:
        v = 0.45 + _LIT[ctx.face] + (noise3(x * 0.9, y * 0.9, z * 0.9, 97) - 0.5) * 0.35
        if hash01(ctx.u, ctx.v, 98) > 0.94:
            v += 0.25                                                  # Moossporen
        return pick(ctx.pal["moss"], v)
    if m > 0.56:
        return pick(ctx.pal["moss"], 0.12)
    return _mud(ctx)


def _peat(ctx: Ctx):
    v = 0.5 + _LIT[ctx.face] + (noise3(*(c * 0.5 for c in ctx.world), 99) - 0.5) * 0.4
    return pick(ctx.pal["peat"], v)


def _root(ctx: Ctx):
    x, y, z = ctx.world
    v = 0.45 + _LIT[ctx.face] * 0.8 + (noise3(x * 2.0, y * 0.3, z * 2.0, 100) - 0.5) * 0.4
    if ctx.face not in ("top", "bottom") and int(y * 2 + x) % 4 == 0:
        v -= 0.12                                                      # Rindenrillen
    return pick(ctx.pal["root"], v)


def _wood(ctx: Ctx):
    x, y, z = ctx.world
    v = 0.5 + _LIT[ctx.face] + (noise3(x * 2.5, y * 0.4, z * 2.5, 101) - 0.5) * 0.35
    return pick(ctx.pal["wood"], v)


def _lily(ctx: Ctx):
    if ctx.face in ("top", "bottom"):
        cx = (ctx.s + 0.5) / max(ctx.w, 1) - 0.5
        cy = (ctx.t + 0.5) / max(ctx.h, 1) - 0.5
        if cx * cx + cy * cy > 0.25 or (cx > 0 and abs(cy) < 0.06):
            return (0, 0, 0, 0)                                        # rund, mit einer Kerbe
        v = 0.6 - (cx * cx + cy * cy) + (0.1 if ctx.face == "top" else -0.2)
        if abs(cx) < 0.04 or abs(cy) < 0.04:
            v -= 0.12                                                  # Blattadern
        return pick(ctx.pal["lily"], v)
    return pick(ctx.pal["lily"], 0.2)


def _eye(ctx: Ctx):
    rs = (ctx.s + 0.5) / max(ctx.w, 1)
    col = pick(ctx.pal["eye"], 0.85 if 0.25 < rs < 0.75 else 0.55)
    ctx.glow = col
    return col


def _reed(ctx: Ctx):
    x, y, z = ctx.world
    v = 0.5 + _LIT[ctx.face] * 0.8 + (noise3(x * 2.0, y * 0.4, z * 2.0, 102) - 0.5) * 0.3
    if int(y * 2) % 5 == 0:
        v -= 0.15                                                      # Knoten am Halm
    return pick(ctx.pal["reed"], v)


def _cattail(ctx: Ctx):
    x, y, z = ctx.world
    v = 0.5 + _LIT[ctx.face] + (noise3(x * 3.0, y * 3.0, z * 3.0, 103) - 0.5) * 0.3 + (hash01(ctx.u, ctx.v, 104) - 0.5) * 0.12
    return pick(ctx.pal["cattail"], v)


def _glowcap(ctx: Ctx):
    """Baumpilz: oben eine leuchtende Kappe mit hellem Rand, unten dunklere Lamellen, die schwach glimmen."""
    rs = (ctx.s + 0.5) / max(ctx.w, 1) - 0.5
    rt = (ctx.t + 0.5) / max(ctx.h, 1) - 0.5
    if ctx.face == "top":
        v = 0.55 + (0.3 if max(abs(rs), abs(rt)) > 0.36 else 0.0) + (hash01(ctx.u, ctx.v, 105) - 0.5) * 0.15
        col = pick(ctx.pal["glowcap"], v)
        ctx.glow = mix((0, 0, 0, 255), col, 0.85)
        return col
    if ctx.face == "bottom":
        col = pick(ctx.pal["glowcap"], 0.3 + (0.12 if int(ctx.s) % 2 == 0 else 0.0))
        ctx.glow = mix((0, 0, 0, 255), col, 0.5)
        return col
    col = pick(ctx.pal["glowcap"], 0.75)
    ctx.glow = mix((0, 0, 0, 255), col, 0.8)
    return col


def _strands(ctx: Ctx):
    """Moosbart: an den Seiten hängen einzelne, verschieden lange Strähnen, oben und unten offen."""
    if ctx.face in ("top", "bottom"):
        return (0, 0, 0, 0)
    col = int(ctx.s)
    length = 0.25 + hash01(col, 0, 106 + len(ctx.face)) * 0.75
    if hash01(col, 1, 107 + len(ctx.face)) < 0.45 or rel_y(ctx) > length:
        return (0, 0, 0, 0)
    x, y, z = ctx.world
    v = 0.38 + _LIT[ctx.face] + (noise3(x * 1.5, y * 0.6, z * 1.5, 108) - 0.5) * 0.3 - rel_y(ctx) * 0.2
    return pick(ctx.pal["moss"], v)


def materials(variant: str) -> dict:
    return {"mud": _mud, "moss_mud": _moss_mud, "peat": _peat, "root": _root, "wood": _wood, "lily": _lily, "eye": _eye,
            "reed": _reed, "cattail": _cattail, "glowcap": _glowcap, "strands": _strands}
