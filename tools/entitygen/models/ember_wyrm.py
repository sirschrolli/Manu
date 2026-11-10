"""
Glutwurm: eine gewaltige Schlange aus den Lavaseen des Nethers, gut sieben Blöcke lang – gepanzert mit Platten aus
schwarzem Basalt, zwischen denen geschmolzenes Gestein glüht; ein kantiger Schädel mit zwei nach hinten geschwungenen
Hörnern, glühenden Augen und einem Maul voller Zähne, in dem Feuer schwelt; über den Rücken läuft ein Kamm aus
Basaltstacheln, der Schwanz endet in einer Spitze.

Der Schädel ist der Ursprung, sechs Glieder folgen nach hinten (jedes ein Kind des vorigen) – so kann WyrmModel (Java)
den Leib schlängeln lassen, ihn beim Auftauchen wie eine Kobra aufrichten und zum Biss vorschnellen lassen.
"""
from __future__ import annotations

from ..geometry import Model
from ..paint import Ctx, hash01, hexc, mix, noise3, pick, rel_y

PALETTES = {
    "ember_wyrm": {
        "basalt": [hexc(c) for c in ("#0e0c0e", "#1a1718", "#262224", "#343032", "#46403f", "#5a5350", "#726a64")],
        "magma": [hexc(c) for c in ("#5a0e02", "#9a2a04", "#d85206", "#ff8a1a", "#ffc24a", "#fff0a8")],
        "horn": [hexc(c) for c in ("#140e0c", "#2a201c", "#46382e", "#6a5646", "#8e7660")],
        "eye": [hexc(c) for c in ("#a03a00", "#ff8a12", "#ffd23a", "#fff6c0")],
        "tooth": [hexc(c) for c in ("#6a5e50", "#a89880", "#e0d4bc")],
    },
}

# Glieder: (Breite, Höhe, Länge) – nach hinten schlanker
SEGMENTS = [(9.0, 7.0, 8.0), (8.4, 6.6, 8.0), (7.6, 6.0, 8.0), (6.6, 5.4, 8.0), (5.6, 4.6, 8.0), (4.4, 3.8, 8.0)]


def build() -> Model:
    m = Model("ember_wyrm", 192, 128)
    m.tex_res = 2
    # Schädel (Ursprung): der Kopf blickt nach vorn (−z)
    m.bone("head", pivot=(0, 16.0, -4.0))
    m.cube("head", (-4.5, -4.0, -9.0), (9, 6, 9), "plate")
    m.cube("head", (-3.6, -2.4, -13.0), (7.2, 4.4, 4.2), "plate")              # Schnauze
    m.cube("head", (-4.9, -4.6, -8.0), (9.8, 1.6, 5.0), "plate")               # Brauenwülste
    m.cube("head", (-4.2, -3.4, -8.3), (1.8, 1.2, 0.4), "eye")
    m.cube("head", (2.4, -3.4, -8.3), (1.8, 1.2, 0.4), "eye")
    m.cube("head", (-3.0, 1.6, -12.6), (0.8, 1.4, 0.8), "tooth")
    m.cube("head", (2.2, 1.6, -12.6), (0.8, 1.4, 0.8), "tooth")
    m.cube("head", (-1.2, -2.6, -13.3), (2.4, 0.8, 0.5), "magma")             # glühende Nüstern
    for side, sx in (("left", 1), ("right", -1)):
        horn = f"{side}_horn"
        m.bone(horn, "head", pivot=(3.4 * sx, -4.0, -3.0), rotation=(40, 18 * sx, 12 * sx))
        m.cube(horn, (-1.2, -1.2, 0), (2.4, 2.4, 5.0), "horn")
        tip = f"{side}_horn_tip"
        m.bone(tip, horn, pivot=(0, 0, 5.0), rotation=(-30, 0, 0))
        m.cube(tip, (-0.8, -0.8, 0), (1.6, 1.6, 4.0), "horn")
        m.cube(tip, (-0.4, -0.4, 4.0), (0.8, 0.8, 2.0), "horn")
    # Unterkiefer
    m.bone("jaw", "head", pivot=(0, 2.0, -4.0), rotation=(6, 0, 0))
    m.cube("jaw", (-3.8, 0, -8.6), (7.6, 2.4, 8.6), "jaw")
    m.cube("jaw", (-3.0, -1.0, -8.2), (0.8, 1.0, 0.8), "tooth")
    m.cube("jaw", (2.2, -1.0, -8.2), (0.8, 1.0, 0.8), "tooth")
    m.cube("jaw", (-0.4, -0.9, -8.4), (0.8, 0.9, 0.8), "tooth")
    # der Leib: sechs Glieder, jedes am Ende des vorigen
    parent = "head"
    z0 = 0.0
    for i, (w, h, length) in enumerate(SEGMENTS):
        name = f"segment{i + 1}"
        m.bone(name, parent, pivot=(0, 0, z0))
        m.cube(name, (-w / 2, -h / 2, 0), (w, h, length), "plate")
        # Rückenstachel
        sh = 3.2 - i * 0.35
        m.cube(name, (-0.6, -h / 2 - sh, 1.5), (1.2, sh, 2.6), "horn")
        m.cube(name, (-0.4, -h / 2 - sh * 0.6, 5.0), (0.8, sh * 0.6, 1.6), "horn")
        parent = name
        z0 = length
    # Schwanzspitze
    m.bone("tail", parent, pivot=(0, 0, z0))
    m.cube("tail", (-1.6, -1.4, 0), (3.2, 2.8, 5.0), "plate")
    m.cube("tail", (-0.8, -0.8, 5.0), (1.6, 1.6, 4.0), "horn")
    m.cube("tail", (-0.3, -0.3, 9.0), (0.6, 0.6, 2.0), "horn")
    return m


# =============================================================================================
# Bemalung
# =============================================================================================

_LIT = {"top": 0.14, "front": 0.04, "west": 0.0, "east": -0.04, "back": -0.06, "bottom": -0.2}


def _plate(ctx: Ctx):
    """Basaltplatten: Querfugen alle zwei Einheiten, in den Fugen glüht Magma (unten stärker als oben)."""
    lx, ly, lz = ctx.local
    x, y, z = ctx.world
    seam = (lz % 4.0) < 0.5 and ctx.face in ("west", "east", "bottom")
    ry = rel_y(ctx)
    if seam and ry > 0.25:
        col = pick(ctx.pal["magma"], 0.35 + ry * 0.3 + (noise3(x, y, z, 103) - 0.5) * 0.2)
        ctx.glow = mix((0, 0, 0, 255), col, 0.85)
        return col
    # Risse in den Platten
    if noise3(x * 0.8, y * 0.8, z * 0.8, 104) > 0.84:
        col = pick(ctx.pal["magma"], 0.4)
        ctx.glow = mix((0, 0, 0, 255), col, 0.6)
        return col
    v = 0.45 + _LIT[ctx.face] - ry * 0.15 + (noise3(x * 0.4, y * 0.4, z * 0.4, 105) - 0.5) * 0.28
    # Kanten der Platten heller (abgeschliffen)
    if (lz % 4.0) < 0.9 and ctx.face == "top":
        v += 0.1
    if hash01(ctx.u, ctx.v, 106) > 0.95:
        v += 0.12
    return pick(ctx.pal["basalt"], v)


def _jaw(ctx: Ctx):
    if ctx.face == "top":
        # das Innere des Mauls: glühend
        rs = (ctx.s + 0.5) / max(ctx.w, 1)
        col = pick(ctx.pal["magma"], 0.75 - abs(rs - 0.5) * 0.6)
        ctx.glow = col
        return col
    return _plate(ctx)


def _horn(ctx: Ctx):
    v = 0.45 + _LIT[ctx.face] + (noise3(*(c * 1.2 for c in ctx.world), 107) - 0.5) * 0.3
    return pick(ctx.pal["horn"], v)


def _eye(ctx: Ctx):
    rs = (ctx.s + 0.5) / max(ctx.w, 1)
    col = pick(ctx.pal["eye"], 0.95 if 0.3 < rs < 0.7 else 0.6)
    ctx.glow = col
    return col


def _magma(ctx: Ctx):
    col = pick(ctx.pal["magma"], 0.8)
    ctx.glow = col
    return col


def _tooth(ctx: Ctx):
    return pick(ctx.pal["tooth"], 0.7 - rel_y(ctx) * 0.4)


def materials(variant: str) -> dict:
    return {"plate": _plate, "jaw": _jaw, "horn": _horn, "eye": _eye, "magma": _magma, "tooth": _tooth}
