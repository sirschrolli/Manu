"""
Aschengeist: ein schwebender Geist aus Asche und Seelenfeuer, gut zwei Blöcke groß – eine zerlumpte Kutte aus grauer
Asche mit tiefer Kapuze, darin ein rußgeschwärzter Schädel mit kalt glühenden, seelenblauen Augen; durch einen Riss in der
Brust sieht man die Rippen und dahinter eine blaue Seelenflamme. Dürre, knochige Arme ragen aus den zerfetzten Ärmeln und
enden in langen Krallen, deren Spitzen blau glimmen. Statt Beinen läuft die Kutte in Fetzen aus, die sich in Rauch
auflösen; um ihn kreisen zwei glimmende Aschebrocken.

Knochen-Namen werden in AshWraithModel (Java) für die Animationen verwendet (schweben, Seelenfeuer werfen, Aschewolke,
Verwehen).
"""
from __future__ import annotations

from ..geometry import Model
from ..paint import Ctx, hash01, hexc, mix, noise3, pick, rel_y

PALETTES = {
    "ash_wraith": {
        "ash": [hexc(c) for c in ("#151413", "#22201e", "#33302d", "#46423e", "#5c5752", "#77716a", "#958e86", "#b3aca3")],
        "char": [hexc(c) for c in ("#070707", "#100f0e", "#1b1918", "#282523")],
        "bone": [hexc(c) for c in ("#2c2722", "#463e36", "#62574c", "#807264", "#9e8f7e", "#bcae9a")],
        "soul": [hexc(c) for c in ("#0a3a52", "#13708e", "#1fa6c4", "#4fd6ec", "#a6f4ff", "#e8feff")],
        "ember": [hexc(c) for c in ("#3a0c02", "#7a2204", "#c24a0a", "#ff7a1a", "#ffb64a")],
    },
}


def build() -> Model:
    m = Model("ash_wraith", 128, 128)
    m.tex_res = 2
    # Rumpf: schwebt, leicht vorgebeugt
    m.bone("body", pivot=(0, 4.0, 0), rotation=(10, 0, 0))
    m.cube("body", (-4.5, -13.0, -2.8), (9, 13, 5.6), "robe")
    m.cube("body", (-5.8, -14.2, -3.4), (11.6, 3.6, 6.8), "mantle")          # Schulterumhang
    m.cube("body", (-2.6, -11.5, -3.05), (5.2, 6.0, 0.4), "ribs")             # Riss in der Brust: Rippen und Seelenflamme
    # Kapuze und Schädel
    m.bone("head", "body", pivot=(0, -14.0, -0.5), rotation=(-8, 0, 0))
    m.cube("head", (-4.4, -9.0, -4.6), (8.8, 9.0, 8.4), "hood")               # vorn offen
    m.cube("head", (-3.8, -8.5, -2.6), (7.6, 8.2, 6.6), "face")               # das Dunkel in der Kapuze
    m.cube("head", (-2.6, -7.0, -4.0), (5.2, 5.6, 3.6), "skull")
    m.cube("head", (-2.2, -5.6, -4.2), (1.6, 1.2, 0.3), "eye")
    m.cube("head", (0.6, -5.6, -4.2), (1.6, 1.2, 0.3), "eye")
    m.cube("head", (-1.5, -2.5, -4.25), (3.0, 1.4, 0.5), "teeth")
    m.cube("head", (-1.2, -10.4, 1.0), (2.4, 2.0, 3.6), "hood")              # Kapuzenzipfel
    m.bone("hood_tip", "head", pivot=(0, -9.5, 4.0), rotation=(38, 0, 0))
    m.cube("hood_tip", (-0.9, -0.8, 0), (1.8, 1.6, 4.5), "hood")
    # Arme: dürr und knochig, in zerfetzten Ärmeln, lange Krallen
    for side, sx in (("left", 1), ("right", -1)):
        arm = f"{side}_arm"
        m.bone(arm, "body", pivot=(5.4 * sx, -12.0, 0), rotation=(-18, 0, -10 * sx))
        m.cube(arm, (-2.2, -1.6, -2.2), (4.4, 7.5, 4.4), "sleeve")
        m.cube(arm, (-0.9, 0, -0.9), (1.8, 9.0, 1.8), "bone")
        fore = f"{side}_forearm"
        m.bone(fore, arm, pivot=(0, 8.6, 0), rotation=(-28, 0, 0))
        m.cube(fore, (-0.8, 0, -0.8), (1.6, 7.5, 1.6), "bone")
        m.cube(fore, (-1.3, 6.8, -1.3), (2.6, 1.6, 2.6), "bone")             # Hand
        for dx in (-0.9, 0.0, 0.9):
            m.cube(fore, (dx - 0.25, 8.2, -0.9), (0.5, 4.2, 0.5), "claw")
        m.cube(fore, (-0.25 + 0.9 * sx, 7.6, 0.4), (0.5, 2.6, 0.5), "claw")
    # die Kutte läuft in Fetzen aus und löst sich in Rauch auf
    m.bone("skirt", "body", pivot=(0, 0, 0))
    m.cube("skirt", (-5.0, 0, -3.2), (10, 6.5, 6.4), "rag")
    m.bone("skirt_low", "skirt", pivot=(0, 6.0, 0.4), rotation=(8, 0, 0))
    m.cube("skirt_low", (-4.0, 0, -2.6), (8, 6.5, 5.2), "rag")
    m.bone("skirt_tip", "skirt_low", pivot=(0, 6.0, 0.5), rotation=(12, 0, 0))
    m.cube("skirt_tip", (-2.6, 0, -1.8), (5.2, 6.0, 3.6), "rag_end")
    # zwei glimmende Aschebrocken kreisen um ihn
    m.bone("orbit", pivot=(0, -4.0, 0))
    m.cube("orbit", (8.0, -1.0, -1.0), (2.0, 2.0, 2.0), "cinder")
    m.cube("orbit", (-9.5, 3.0, 2.0), (1.6, 1.6, 1.6), "cinder")
    m.cube("orbit", (1.0, -6.0, 8.0), (1.4, 1.4, 1.4), "cinder")
    return m


# =============================================================================================
# Bemalung
# =============================================================================================

_LIT = {"top": 0.12, "front": 0.04, "west": 0.0, "east": -0.04, "back": -0.06, "bottom": -0.2}


def _ash_value(ctx: Ctx, base: float = 0.45) -> float:
    x, y, z = ctx.world
    big = (noise3(x * 0.18, y * 0.18, z * 0.18, 201) - 0.5) * 0.3
    fine = (noise3(x * 0.9, y * 0.6, z * 0.9, 202) - 0.5) * 0.18
    return base + _LIT[ctx.face] + big + fine


def _robe(ctx: Ctx):
    """Asche, in Falten gelegt: senkrechte dunklere Bahnen, darin glimmende Glutadern."""
    x, y, z = ctx.world
    v = _ash_value(ctx) - rel_y(ctx) * 0.1
    if ctx.face in ("front", "back", "west", "east") and noise3(x * 1.2, y * 0.15, z * 1.2, 203) > 0.62:
        v -= 0.14                                                       # Falten
    if noise3(x * 0.7, y * 0.7, z * 0.7, 204) > 0.83:
        col = pick(ctx.pal["ember"], 0.55 + (noise3(x, y, z, 205) - 0.5) * 0.4)
        ctx.glow = mix((0, 0, 0, 255), col, 0.6)
        return col
    if hash01(ctx.u, ctx.v, 206) > 0.97:
        v += 0.2                                                        # helle Ascheflocken
    return pick(ctx.pal["ash"], v)


def _mantle(ctx: Ctx):
    v = _ash_value(ctx, 0.38)
    if ctx.face not in ("top", "bottom") and ctx.t >= ctx.h - 1 and hash01(ctx.u, 0, 207) > 0.4:
        return (0, 0, 0, 0)                                             # zerfranster Saum
    return pick(ctx.pal["ash"], v)


def _hood(ctx: Ctx):
    v = _ash_value(ctx, 0.36)
    if ctx.face == "front":
        rs = (ctx.s + 0.5) / max(ctx.w, 1)
        rt = (ctx.t + 0.5) / max(ctx.h, 1)
        if ctx.cube.size[2] > 6.0 and 0.14 < rs < 0.86 and rt > 0.13:
            return (0, 0, 0, 0)                                         # die Öffnung der Kapuze
        v -= 0.08
    return pick(ctx.pal["ash"], v)


def _face(ctx: Ctx):
    """Das Dunkel in der Kapuze, hinter dem Schädel."""
    return pick(ctx.pal["char"], 0.15 + (hash01(ctx.u, ctx.v, 218) - 0.5) * 0.1)


def _skull(ctx: Ctx):
    x, y, z = ctx.world
    v = 0.3 + _LIT[ctx.face] * 0.6 + (noise3(x * 1.5, y * 1.5, z * 1.5, 208) - 0.5) * 0.3
    if ctx.face == "front":
        rs = (ctx.s + 0.5) / max(ctx.w, 1)
        rt = (ctx.t + 0.5) / max(ctx.h, 1)
        if 0.38 < rt < 0.62 and abs(rs - 0.5) > 0.1:
            return pick(ctx.pal["char"], 0.0)                          # Augenhöhlen
        if rt > 0.7 and abs(rs - 0.5) < 0.08:
            return pick(ctx.pal["char"], 0.1)                          # Nasenloch
    return pick(ctx.pal["bone"], v)


def _eye(ctx: Ctx):
    rs = (ctx.s + 0.5) / max(ctx.w, 1)
    col = pick(ctx.pal["soul"], 0.95 if 0.25 < rs < 0.75 else 0.7)
    ctx.glow = col
    return col


def _teeth(ctx: Ctx):
    if ctx.face == "front":
        if int(ctx.s) % 2 == 1:
            return pick(ctx.pal["char"], 0.1)
        return pick(ctx.pal["bone"], 0.75)
    return pick(ctx.pal["bone"], 0.4)


def _ribs(ctx: Ctx):
    """Der Riss in der Brust: Rippen (Querstreben) und dahinter die blaue Seelenflamme."""
    if ctx.face != "front":
        return _robe(ctx)
    rs = (ctx.s + 0.5) / max(ctx.w, 1)
    rt = (ctx.t + 0.5) / max(ctx.h, 1)
    edge = abs(rs - 0.5) > 0.42 - rt * 0.12
    if edge:
        return pick(ctx.pal["char"], 0.25)                             # verkohlter Rand des Risses
    if int(ctx.t) % 3 == 0 and abs(rs - 0.5) > 0.06:
        return pick(ctx.pal["bone"], 0.55 - rt * 0.2)                   # Rippe
    if abs(rs - 0.5) < 0.06:
        return pick(ctx.pal["bone"], 0.4)                              # Brustbein
    flame = 0.95 - abs(rs - 0.5) * 1.2 - rt * 0.25 + (hash01(ctx.u, ctx.v, 209) - 0.5) * 0.15
    col = pick(ctx.pal["soul"], flame)
    ctx.glow = col
    return col


def _sleeve(ctx: Ctx):
    v = _ash_value(ctx, 0.4)
    if ctx.face not in ("top", "bottom") and rel_y(ctx) > 0.55 and hash01(ctx.u, 1, 210) > 0.45:
        return (0, 0, 0, 0)                                             # Fetzen
    if ctx.face == "bottom":
        return (0, 0, 0, 0)
    return pick(ctx.pal["ash"], v)


def _bone(ctx: Ctx):
    x, y, z = ctx.world
    v = 0.42 + _LIT[ctx.face] + (noise3(x * 2.0, y * 0.6, z * 2.0, 211) - 0.5) * 0.3
    if noise3(x * 1.1, y * 1.1, z * 1.1, 212) > 0.78:
        v -= 0.25                                                       # Ruß
    return pick(ctx.pal["bone"], v)


def _claw(ctx: Ctx):
    ry = rel_y(ctx)
    if ry > 0.6:
        col = pick(ctx.pal["soul"], 0.55 + (ry - 0.6) * 1.0)
        ctx.glow = col
        return col
    return pick(ctx.pal["char"], 0.5 + _LIT[ctx.face])


def _rag(ctx: Ctx):
    if ctx.face == "bottom":
        return (0, 0, 0, 0)
    if ctx.face != "top" and ctx.t >= ctx.h - 2 and hash01(ctx.u, 2, 213) > 0.35:
        return (0, 0, 0, 0)
    return pick(ctx.pal["ash"], _ash_value(ctx, 0.36) - rel_y(ctx) * 0.12)


def _rag_end(ctx: Ctx):
    """Das Ende der Kutte: lange Fetzen, die nach unten dunkler werden (dort beginnt der Rauch)."""
    if ctx.face in ("top", "bottom"):
        return (0, 0, 0, 0)
    col_len = 0.3 + hash01(int(ctx.s), 3, 214 + len(ctx.face)) * 0.7
    if rel_y(ctx) > col_len or hash01(int(ctx.s), 4, 215 + len(ctx.face)) < 0.3:
        return (0, 0, 0, 0)
    return pick(ctx.pal["ash"], _ash_value(ctx, 0.3) - rel_y(ctx) * 0.25)


def _cinder(ctx: Ctx):
    x, y, z = ctx.world
    if noise3(x * 2.0, y * 2.0, z * 2.0, 216) > 0.45:
        col = pick(ctx.pal["ember"], 0.6 + (hash01(ctx.u, ctx.v, 217) - 0.5) * 0.4)
        ctx.glow = col
        return col
    return pick(ctx.pal["char"], 0.6)


def materials(variant: str) -> dict:
    return {"robe": _robe, "mantle": _mantle, "hood": _hood, "face": _face, "skull": _skull, "eye": _eye, "teeth": _teeth,
            "ribs": _ribs, "sleeve": _sleeve, "bone": _bone, "claw": _claw, "rag": _rag, "rag_end": _rag_end, "cinder": _cinder}
