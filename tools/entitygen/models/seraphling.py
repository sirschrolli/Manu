"""
Seraphling: ein kleiner Lichtbote Schrollis des Schöpfers – ein goldenes Antlitz mit leuchtenden Augen unter einem
Heiligenschein, ein kurzes Gewand, das in Licht vergeht, und zwei Paar weiße Schwingen mit goldenen Spitzen. Er schwebt und
schießt Lichtpfeile. Knochen werden in SeraphlingModel (Java) animiert.
"""
from __future__ import annotations

import math

from ..geometry import Model
from ..paint import Ctx, hash01, hexc, mix, pick
from . import schrolli as base

PALETTES = {"seraphling": dict(base._BASE)}

WINGS = (
    ("upper", -1.0, 1.4, (-4.0, -20.0, -36.0), (4.0, 4.5), 0.5),
    ("lower", 1.6, 1.2, (4.0, -16.0, 22.0), (3.2, 3.6), 0.38),
)
FEATHERS = (
    (3, 4.0, 5.5, 4.0, 14.0, 2.6),
    (4, 7.0, 10.0, 20.0, 56.0, 2.4),
)


def build() -> Model:
    m = Model("seraphling", 64, 64)
    m.tex_res = 2
    m.bone("body", pivot=(0, 12, 0))
    m.cube("body", (-2.5, 0, -1.6), (5, 5, 3.2), "robe")
    m.cube("body", (-2.7, 0, -1.8), (5.4, 1.0, 3.6), "gold")                       # Kragen
    m.bone("robe_low", "body", pivot=(0, 5, 0))
    m.cube("robe_low", (-3.0, 0, -2.0), (6.0, 6.0, 4.0), "robe_light")
    # Kopf: ein goldenes Antlitz, darüber der Heiligenschein
    m.bone("head", "body", pivot=(0, 0, 0))
    m.cube("head", (-3, -6, -3), (6, 6, 6), "mask")
    m.cube("head", (-3.3, -6.6, -3.3), (6.6, 1.4, 6.6), "hair")
    m.bone("halo", "head", pivot=(0, -8.2, 0))
    r = 3.6
    seg = 2 * math.pi * r / 10 + 0.4
    for k in range(10):
        a = 36.0 * k
        rad = math.radians(a)
        name = f"halo_{k}"
        m.bone(name, "halo", pivot=(math.cos(rad) * r, 0, math.sin(rad) * r), rotation=(0, -(a + 90.0), 0))
        m.cube(name, (-seg / 2, -0.4, -0.4), (seg, 0.8, 0.8), "halo")
    # Ärmchen
    for side, sx in (("left", 1), ("right", -1)):
        m.bone(f"{side}_arm", "body", pivot=(2.9 * sx, 0.6, 0), rotation=(-20, 0, -10 * sx))
        m.cube(f"{side}_arm", (-0.8, 0, -0.8), (1.6, 3.6, 1.6), "sleeve")
        m.cube(f"{side}_arm", (-0.6, 3.6, -0.6), (1.2, 1.0, 1.2), "skin")
    # zwei Paar Schwingen
    for pos, y, z, rot, segs, size in WINGS:
        for side, sx in (("left", 1), ("right", -1)):
            parent = "body"
            pivot = (1.2 * sx, y, z)
            rotation = (rot[0], rot[1] * sx, rot[2] * sx)
            for i, length in enumerate(segs):
                name = f"wing_{pos}_{side}" + ("" if i == 0 else "_tip")
                m.bone(name, parent, pivot=pivot, rotation=rotation)
                x0 = 0.0 if sx > 0 else -length
                m.cube(name, (x0, -0.5, -0.4), (length, 1.0, 0.8), "wing_arm")
                count, l0, l1, a0, a1, w = FEATHERS[i]
                for j in range(count):
                    k = j / max(1, count - 1)
                    fname = f"{name}_f{j}"
                    m.bone(fname, name, pivot=((length * (0.15 + 0.8 * k)) * sx, 0.4, 0.05 + 0.1 * j),
                           rotation=(0, 0, -(a0 + (a1 - a0) * k) * sx))
                    m.cube(fname, (-w * size, 0, 0), (w * size * 2, (l0 + (l1 - l0) * k) * size * 2, 0), "primary",
                           tag=f"s{pos}{side}{i}{j}")
                parent = name
                pivot = (length * sx, 0, 0)
                rotation = (0, -8 * sx, 10 * sx)
    return m


def _mask(ctx: Ctx):
    """Ein goldenes, glattes Antlitz mit leuchtenden Augen (12×12 Texel vorn)."""
    face, s, t = ctx.face, ctx.s, ctx.t
    if face == "front":
        if t in (5, 6) and (2 <= s <= 4 or 7 <= s <= 9):
            col = pick(ctx.pal["eye"], 0.9 if s in (3, 8) else 0.6)
            ctx.glow = col
            return col
        if t == 4 and (2 <= s <= 4 or 7 <= s <= 9):
            return pick(ctx.pal["gold"], 0.45)                                        # Brauenbogen
        if 6 <= t <= 8 and s in (5, 6):
            return pick(ctx.pal["gold"], 0.82 if t < 8 else 0.55)                     # Nase
        if t == 10 and 4 <= s <= 7:
            return pick(ctx.pal["gold"], 0.42 if s in (4, 7) else 0.5)                # Mund
        v = 0.66 + (0.06 if 2 <= t <= 8 else 0.0) - abs(s - 5.5) * 0.015
        return pick(ctx.pal["gold"], v + (hash01(ctx.u, ctx.v) - 0.5) * 0.04)
    if face in ("top", "back"):
        return base._hair(ctx)
    if face in ("west", "east") and (t <= 3 or s >= 8):
        return base._hair(ctx)
    return pick(ctx.pal["gold"], 0.58 + base._LIT[face])


def _halo(ctx: Ctx):
    col = pick(ctx.pal["light"], 0.9)
    ctx.glow = col
    return col


def materials(variant: str) -> dict:
    mats = base.materials("schrolli")
    mats.update({"mask": _mask, "halo": _halo})
    return mats
