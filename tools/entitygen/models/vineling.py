"""
Rankling: ein Diener des Hains, den Verdantis aus dem Boden ruft. Ein Leib aus verdrehten Ranken auf zwei
Wurzelbeinen, lange Rankenpeitschen als Arme und als Kopf eine geschlossene Knospe mit leuchtenden Augen, deren
Blütenblätter beim Zuschnappen aufklappen und ein Maul voller Dornen freigeben (ca. 1,6 Blöcke hoch).

Knochen-Namen werden in VinelingModel (Java) für die Animationen verwendet.
"""
from __future__ import annotations

import math

from ..geometry import Model
from ..paint import Ctx, eye_painter, flat_painter, hash01, mix, noise3, pick, ramp

PALETTES = {
    "vineling": {
        "vine": ramp("#14300e", "#6aa83a"), "root": ramp("#1e150c", "#7a5c3a"), "bud": ramp("#173a14", "#5a9a3a"),
        "back": ramp("#173a14", "#5a9a3a"),
        "petal": ramp("#5a1440", "#f070b0"), "eye": ramp("#a07000", "#fff2a0"), "maw": ramp("#200408", "#8a2030"),
        "thorn": ramp("#4a3a20", "#e8dcb0"), "leaf": ramp("#173d14", "#7cc24a"),
    },
}


def build() -> Model:
    m = Model("vineling", 64, 64)
    # Leib aus Ranken
    m.bone("body", pivot=(0, 14, 0))
    m.cube("body", (-3, -9, -2), (6, 10, 4), "vine")
    m.cube("body", (-3.5, -9.5, -2.5), (7, 2.5, 5), "leaf")             # Blattkragen
    m.cube("body", (-2.5, 0, -1.5), (5, 1.5, 3), "root")
    # Kopf: Knospe mit Augen, darin das Maul
    m.bone("head", "body", pivot=(0, -9, 0))
    m.cube("head", (-3, -6, -3), (6, 6, 6), "bud")
    m.cube("head", (-2.2, -4.2, -3.3), (1.4, 1.2, 0.5), "eye")
    m.cube("head", (0.8, -4.2, -3.3), (1.4, 1.2, 0.5), "eye")
    m.cube("head", (-2, -8.5, -2), (4, 2.6, 4), "maw")
    # Blütenblätter (klappen beim Zuschnappen auf)
    for name, pivot, rot, origin, size in [
            ("petal_front", (0, -6, -3), (-12, 0, 0), (-2.5, -5, -0.5), (5, 5, 1)),
            ("petal_back", (0, -6, 3), (12, 0, 0), (-2.5, -5, -0.5), (5, 5, 1)),
            ("petal_left", (3, -6, 0), (0, 0, 12), (-0.5, -5, -2.5), (1, 5, 5)),
            ("petal_right", (-3, -6, 0), (0, 0, -12), (-0.5, -5, -2.5), (1, 5, 5))]:
        m.bone(name, "head", pivot=pivot, rotation=rot)
        m.cube(name, origin, size, "petal")
    # Rankenarme (zwei Glieder, mit Dornen und einem Blatt an der Spitze)
    for side, sx in (("left", 1), ("right", -1)):
        up = f"{side}_arm"
        m.bone(up, "body", pivot=(3.2 * sx, -8, 0), rotation=(0, 0, -12 * sx))
        m.cube(up, (-1 if sx > 0 else -1, -0.5, -1), (2, 7, 2), "vine")
        m.cube(up, (0.6 * sx - 0.4, 2, -1.3), (0.8, 0.8, 0.6), "thorn")
        low = f"{side}_forearm"
        m.bone(low, up, pivot=(0, 6, 0), rotation=(-10, 0, 6 * sx))
        m.cube(low, (-0.75, -0.3, -0.75), (1.5, 7, 1.5), "vine")
        m.cube(low, (-0.3, 3, -1.2), (0.6, 0.8, 0.6), "thorn")
        m.cube(low, (-1.8, 6, -0.25), (3.6, 3, 0.5), "leaf")
    # Wurzelbeine
    for side, sx in (("left", 1), ("right", -1)):
        name = f"{side}_leg"
        m.bone(name, "body", pivot=(1.9 * sx, 1, 0))
        m.cube(name, (-1.0, 0, -1.0), (2.0, 8, 2.0), "root")
        m.cube(name, (-1.6, 7.5, -2.6), (3.2, 1.5, 4.0), "root")
        m.cube(name, (-0.4, 8.0, -4.2), (0.8, 1, 1.6), "root")
    return m


def _vine(seed: int = 211):
    """Verdrehte Ranken: breite, weich schattierte Stränge mit einer dunklen Furche dazwischen (keine Flimmerpixel)."""

    def paint(ctx: Ctx):
        x, y, z = ctx.world
        phase = (y * 0.55 + (x + z) * 0.7) % (2 * math.pi)
        twist = math.sin(phase)
        v = 0.42 + 0.2 * twist + 0.08 * (noise3(x * 0.2, y * 0.2, z * 0.2, seed) - 0.5)
        if ctx.face != "top" and (ctx.s == 0 or ctx.s == ctx.w - 1 or ctx.t == ctx.h - 1):
            v -= 0.06
        col = pick(ctx.pal["vine"], v)
        if twist < -0.85:
            col = pick(ctx.pal["vine"], 0.14)
        return col

    return paint


def _root(seed: int = 221):
    def paint(ctx: Ctx):
        groove = abs(math.sin(ctx.s * 1.8 + ctx.t * 0.2))
        return pick(ctx.pal["root"], 0.3 + 0.3 * groove + 0.1 * hash01(ctx.u, ctx.v, seed))

    return paint


def _bud(ctx: Ctx):
    x, y, z = ctx.world
    stripe = abs(math.sin((x + z) * 1.1)) < 0.25
    v = 0.4 + 0.2 * noise3(x * 0.6, y * 0.6, z * 0.6, 7)
    col = pick(ctx.pal["bud"], v - (0.15 if stripe else 0))
    if ctx.face == "top":
        col = mix(col, pick(ctx.pal["petal"], 0.4), 0.4)
    return col


def _petal(ctx: Ctx):
    rel = (ctx.local[1] - ctx.cube.origin[1]) / max(ctx.cube.size[1], 1e-3)   # 0 Spitze, 1 Ansatz
    col = pick(ctx.pal["petal"], 0.85 - 0.5 * rel)
    if rel > 0.7:
        col = mix(col, pick(ctx.pal["bud"], 0.45), (rel - 0.7) / 0.3)
    return col


def _maw(ctx: Ctx):
    if ctx.face == "top" and (ctx.s + ctx.t) % 2 == 0 and (ctx.s in (0, ctx.w - 1) or ctx.t in (0, ctx.h - 1)):
        return pick(ctx.pal["thorn"], 0.85)
    return pick(ctx.pal["maw"], 0.3 + 0.3 * hash01(ctx.u, ctx.v))


def _leaf(ctx: Ctx):
    # Blatt: hellere Spitze, dunkle Mittelrippe, ruhige Fläche
    t = ctx.t / max(1, ctx.h - 1)
    v = 0.4 + 0.3 * (1.0 - t) + 0.06 * (hash01(ctx.u // 2, ctx.v // 2) - 0.5)
    if ctx.s == ctx.w // 2:
        v -= 0.18
    return pick(ctx.pal["leaf"], v)


def materials(variant: str) -> dict:
    return {
        "vine": _vine(),
        "root": _root(),
        "bud": _bud,
        "petal": _petal,
        "maw": _maw,
        "leaf": _leaf,
        "thorn": flat_painter("thorn", 0.7),
        "eye": eye_painter("eye", glow=True),
    }
