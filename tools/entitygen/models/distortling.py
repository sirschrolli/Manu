"""
Zerrling: ein Wesen, das die Leere beim Zerbrechen der Welten verzerrt hat – kaum noch eine Gestalt, eher ein Fehler im
Bild.

  * ein hagerer, zu langer Leib aus lauter Bruchstücken, zwischen denen schmale Lücken klaffen (Brust, Bauch, Becken,
    Glieder) – als hielte ihn nur noch die Leere zusammen
  * schwarz wie ein Loch im Bild, durchzogen von Rauschen und Bildfehler-Streifen; an den Rändern spalten sich die
    Farben in Türkis und Magenta auf
  * eine bleiche, zersprungene Maske mit einem leuchtenden Auge, das andere ist ein flimmernder Pixelhaufen
  * lange Arme mit Krallenfingern; um den Leib flirren lose Bildfehler-Splitter

Knochen-Namen werden in DistortlingModel (Java) für die Animationen verwendet.
"""
from __future__ import annotations

import math

from ..geometry import Model
from ..paint import Ctx, hash01, hexc, mix, noise3, pick

PALETTES = {
    "distortling": {
        "void": [hexc(c) for c in ("#040309", "#08060f", "#0d0a18", "#141022", "#1c172e", "#26203c")],
        "cyan": [hexc(c) for c in ("#0a6a7a", "#18b8c8", "#5af0ff", "#c8ffff")],
        "magenta": [hexc(c) for c in ("#7a0a6a", "#c818a8", "#ff5ae0", "#ffc8f4")],
        "mask": [hexc(c) for c in ("#5c5870", "#86829a", "#aeaabe", "#ccc8da", "#e4e0ee", "#f6f4fa")],
        "eye": [hexc(c) for c in ("#2ad8f0", "#8af4ff", "#e8ffff", "#ffffff")],
        "claw": [hexc(c) for c in ("#2a2440", "#4a4268", "#7a7098", "#b8b0d4")],
    },
}

GAP = 0.8


def build() -> Model:
    m = Model("distortling", 128, 64)
    m.tex_res = 2
    # --- Beine: Oberschenkel, Schienbein und Fuß als getrennte Bruchstücke ---
    for side, sx in (("right", -1), ("left", 1)):
        leg = f"{side}_leg"
        m.bone(leg, pivot=(2.1 * sx, 6.5, 0))
        m.cube(leg, (-1.4, 0, -1.4), (2.8, 7.2, 2.8), "void")
        shin = f"{side}_shin"
        m.bone(shin, leg, pivot=(0, 7.2 + GAP, 0))
        m.cube(shin, (-1.2, 0, -1.2), (2.4, 7.0, 2.4), "void")
        m.cube(shin, (-1.5, 7.0 + GAP, -2.9), (3.0, 1.5, 4.2), "void")            # Fuß (schwebt knapp darunter)

    # --- Rumpf: Becken, Bauch und Brust mit Lücken dazwischen ---
    m.bone("body", pivot=(0, 6.5, 0))
    m.cube("body", (-3.0, -2.2, -1.8), (6.0, 2.6, 3.6), "void")                  # Becken
    m.bone("belly", "body", pivot=(0, -2.2 - GAP, 0))
    m.cube("belly", (-2.3, -3.2, -1.5), (4.6, 3.2, 3.0), "void")
    m.bone("chest", "belly", pivot=(0, -3.2 - GAP, 0))
    m.cube("chest", (-4.2, -6.8, -2.2), (8.4, 6.8, 4.4), "void")
    m.cube("chest", (-4.8, -7.4, -2.5), (2.6, 1.4, 5.0), "void")                 # Schultergrate
    m.cube("chest", (2.2, -7.4, -2.5), (2.6, 1.4, 5.0), "void")
    # verrutschte Bildzeilen: Scheiben der Brust, seitlich versetzt wie ein Fehler im Bild
    m.bone("chest_slice", "chest", pivot=(1.3, -3.6, 0))
    m.cube("chest_slice", (-4.2, -0.6, -2.3), (8.4, 1.2, 4.6), "void")
    m.bone("chest_slice2", "chest", pivot=(-0.9, -5.7, 0))
    m.cube("chest_slice2", (-4.2, -0.4, -2.25), (8.4, 0.8, 4.5), "void")

    # --- Kopf: zersprungene Maske, darunter ein abgelöstes Kieferstück ---
    m.bone("head", "chest", pivot=(0, -7.0 - GAP, -0.3))
    m.cube("head", (-3.0, -7.0, -3.0), (6.0, 7.0, 6.0), "head")
    m.cube("head", (-3.2, -6.4, -3.35), (6.4, 5.2, 0.5), "mask")
    m.bone("head_slice", "head", pivot=(-1.1, -2.2, 0))
    m.cube("head_slice", (-3.0, -0.5, -3.05), (6.0, 1.0, 6.1), "head")
    m.bone("jaw", "head", pivot=(0, 0.6, -0.4))
    m.cube("jaw", (-2.4, 0, -2.4), (4.8, 1.6, 4.4), "void")
    # lose Splitter am Hinterkopf
    m.bone("head_shards", "head", pivot=(0, -4, 2))
    for k, (x, y, z, size) in enumerate(((2.8, -2.6, 1.6, (1.4, 1.4, 1.4)), (-3.4, -0.8, 1.0, (1.0, 1.8, 1.0)),
                                         (1.2, -4.2, 2.4, (1.8, 0.8, 0.8)))):
        m.cube("head_shards", (x, y, z), size, "glitch")

    # --- Arme: Oberarm, Unterarm, Hand mit drei Krallen ---
    for side, sx in (("right", -1), ("left", 1)):
        arm = f"{side}_arm"
        m.bone(arm, "chest", pivot=(5.4 * sx, -6.0, 0))
        m.cube(arm, (-1.0, -1.0, -1.0), (2.0, 8.0, 2.0), "void")
        fore = f"{side}_forearm"
        m.bone(fore, arm, pivot=(0, 7.0 + GAP, 0))
        m.cube(fore, (-0.9, 0, -0.9), (1.8, 8.0, 1.8), "void")
        hand = f"{side}_hand"
        m.bone(hand, fore, pivot=(0, 8.0 + GAP, 0))
        m.cube(hand, (-1.2, 0, -1.2), (2.4, 2.2, 2.4), "void")
        for k, (cx, cz) in enumerate(((-0.8, -0.8), (0.4, -0.9), (-0.2, 0.6))):
            m.cube(hand, (cx - 0.2, 2.0, cz - 0.2), (0.45, 4.6 - k * 0.6, 0.45), "claw")

    # --- flirrende Bildfehler-Splitter um den Leib ---
    m.bone("glitch", "body", pivot=(0, -6, 0))
    for k, (x, y, z, size) in enumerate(((5.6, -4.0, -1.0, (1.2, 1.2, 1.2)), (-6.2, 0.5, 1.0, (0.8, 2.2, 0.8)),
                                         (4.4, 3.5, 2.6, (2.0, 0.6, 0.6)), (-4.6, -6.5, -2.4, (1.0, 1.0, 1.0)),
                                         (0.5, -9.5, 3.4, (1.6, 0.6, 1.6)), (-2.0, 4.6, -3.2, (0.7, 0.7, 2.2)))):
        name = f"bit_{k}"
        m.bone(name, "glitch", pivot=(x, y, z))
        m.cube(name, (-size[0] / 2, -size[1] / 2, -size[2] / 2), size, "glitch")
    return m


# =============================================================================================
# Bemalung
# =============================================================================================

def _static(ctx: Ctx) -> float:
    """Bildrauschen: grobkörnig, in waagerechten Zeilen leicht verrutscht."""
    row = ctx.v // 2
    shift = int(hash01(row, 3) * 6) if hash01(row, 4) > 0.8 else 0
    return hash01(ctx.u + shift, ctx.v, 5)


def _void(ctx: Ctx):
    x, y, z = ctx.world
    n = _static(ctx)
    v = 0.18 + n * 0.32 + (noise3(x * 0.3, y * 0.3, z * 0.3, 6) - 0.5) * 0.2 + (0.1 if ctx.face == "top" else 0.0)
    col = pick(ctx.pal["void"], v)
    # Farbsäume: links Türkis, rechts Magenta – nur stückweise, wie flackernde Farbspaltung an den Rändern
    s = ctx.s
    if ctx.face in ("front", "back", "west", "east"):
        seg = hash01(ctx.v // 3, ctx.cube.bone.name, ctx.face, 21)
        if s == 0 and seg > 0.45:
            col = mix(col, pick(ctx.pal["cyan"], 0.45), 0.6)
            ctx.glow = mix((0, 0, 0, 255), pick(ctx.pal["cyan"], 0.4), 0.45)
        elif s == ctx.w - 1 and seg < 0.55:
            col = mix(col, pick(ctx.pal["magenta"], 0.45), 0.6)
            ctx.glow = mix((0, 0, 0, 255), pick(ctx.pal["magenta"], 0.4), 0.45)
    # Bildfehler-Streifen: kurze, grelle Zeilen quer über den Leib
    row = (ctx.v + int(y * 2)) // 1
    if hash01(row, ctx.cube.bone.name, 7) > 0.93:
        k = hash01(row, 8)
        start = int(k * ctx.w)
        if start <= ctx.s < start + 2 + int(k * 5):
            pal = ctx.pal["cyan"] if k < 0.45 else ctx.pal["magenta"] if k < 0.9 else [hexc("#ffffff")]
            col = pick(pal, 0.7)
            ctx.glow = mix((0, 0, 0, 255), col, 0.9)
    # die Lücken im Leib glimmen innen schwach violett (Unter- und Oberseiten der Bruchstücke)
    if ctx.face in ("top", "bottom") and hash01(ctx.u, ctx.v, 9) > 0.55:
        col = mix(col, pick(ctx.pal["magenta"], 0.2), 0.35)
    return col


def _head(ctx: Ctx):
    if ctx.face == "front":
        return pick(ctx.pal["void"], 0.2)
    return _void(ctx)


def _mask(ctx: Ctx):
    s = (ctx.s + 0.5) / max(ctx.w, 1)
    t = (ctx.t + 0.5) / max(ctx.h, 1)
    if ctx.face != "front":
        return pick(ctx.pal["mask"], 0.3)
    # Sprung quer über die Maske: dahinter ist nichts
    crack = abs((s - 0.62) - (t - 0.1) * 0.55 + math.sin(t * 9.0) * 0.04)
    if crack < 0.035 and t < 0.92:
        return pick(ctx.pal["void"], 0.05)
    v = 0.95 - abs(s - 0.5) * 0.35 - t * 0.15 + (hash01(ctx.u, ctx.v, 11) - 0.5) * 0.08
    col = pick(ctx.pal["mask"], v)
    # rechtes Auge (vom Betrachter links): ein leuchtendes, rundes Loch
    d = math.hypot((s - 0.3) * 1.2, t - 0.42)
    if d < 0.12:
        col = pick(ctx.pal["eye"], 0.9 - d * 3.0)
        ctx.glow = col
    elif d < 0.17:
        col = pick(ctx.pal["void"], 0.1)
    # linkes Auge: ein flimmernder Haufen aus Pixeln
    if abs(s - 0.72) < 0.11 and abs(t - 0.42) < 0.1 and hash01(ctx.u, ctx.v, 12) > 0.35:
        k = hash01(ctx.u, ctx.v, 13)
        col = pick(ctx.pal["magenta"] if k < 0.5 else ctx.pal["cyan"], 0.6 + k * 0.3)
        ctx.glow = col
    # schmaler, schiefer Mundschlitz
    if 0.72 < t < 0.78 and 0.3 < s < 0.7 - (t - 0.72) * 2:
        col = pick(ctx.pal["void"], 0.1)
    return col


def _glitch(ctx: Ctx):
    k = hash01(ctx.cube.origin, 14)
    pal = ctx.pal["cyan"] if k < 0.4 else ctx.pal["magenta"] if k < 0.8 else ctx.pal["mask"]
    col = pick(pal, 0.45 + hash01(ctx.u, ctx.v, 15) * 0.5)
    ctx.glow = mix((0, 0, 0, 255), col, 0.85)
    return col


def _claw(ctx: Ctx):
    t = (ctx.t + 0.5) / max(ctx.h, 1)
    col = pick(ctx.pal["claw"], 0.3 + t * 0.65)
    if t > 0.8:
        ctx.glow = mix((0, 0, 0, 255), pick(ctx.pal["cyan"], 0.6), 0.5)
    return col


def materials(variant: str) -> dict:
    return {"void": _void, "head": _head, "mask": _mask, "glitch": _glitch, "claw": _claw}
