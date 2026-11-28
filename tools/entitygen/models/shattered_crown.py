"""
Die Krone des Zerbrochenen Königs, getragen: ein schmaler goldener Reif um den Kopf, vorn ein Leerenkristall, darüber
schweben sieben Kristallsplitter im Kreis – wie über dem Haupt des Königs, die Zacke über der Stirn am höchsten.

Knochen wie HumanoidModel (head mit hat, body, Arme, Beine – nur der Kopf trägt Quader), damit NeoForge die Haltung des
Spielers übertragen kann. Die Textur wird als Rüstungsschicht gezeichnet:
textures/entity/equipment/humanoid/shattered_crown.png (ohne Leuchten – Rüstungen werden nicht leuchtend gezeichnet).
"""
from __future__ import annotations

import math

from ..geometry import Model
from ..paint import Ctx, hash01, pick
from .shattered_king import PALETTES as KING, _crystal, _gold

PALETTES = {"shattered_crown": KING["shattered_king"]}


def build() -> Model:
    m = Model("shattered_crown", 64, 32)
    m.tex_res = 2
    m.bone("head", pivot=(0.0, 0.0, 0.0))
    m.bone("hat", "head", pivot=(0.0, 0.0, 0.0))
    # der Reif
    m.cube("head", (-4.6, -8.9, -4.7), (9.2, 1.8, 0.6), "gold")
    m.cube("head", (-4.6, -8.9, 4.1), (9.2, 1.8, 0.6), "gold")
    m.cube("head", (4.1, -8.9, -4.1), (0.6, 1.8, 8.2), "gold")
    m.cube("head", (-4.7, -8.9, -4.1), (0.6, 1.8, 8.2), "gold")
    m.cube("head", (-1.0, -9.6, -5.1), (2.0, 2.6, 0.6), "gem")                       # Kristall über der Stirn
    for sx in (-1, 1):
        for sz in (-1, 1):
            m.cube("head", (4.0 * sx - 0.5, -10.0, 4.0 * sz - 0.5), (1.0, 1.4, 1.0), "gold")   # Zacken an den Ecken
    # sieben schwebende Splitter
    m.bone("crown", "head", pivot=(0.0, -10.6, 0.0))
    for k in range(7):
        a = math.radians(-90 + k * 360 / 7)
        length = 5.4 if k == 0 else (4.2 if k in (1, 6) else 3.3)
        name = f"crown_{k}"
        m.bone(name, "crown", pivot=(math.cos(a) * 4.0, 0.0, math.sin(a) * 4.0), rotation=(math.sin(a) * -16, 0, math.cos(a) * 16))
        m.cube(name, (-0.6, -length, -0.6), (1.2, length + 0.8, 1.2), "crystal")
    # die übrigen Teile des Spielermodells (leer)
    m.bone("body", pivot=(0.0, 0.0, 0.0))
    m.bone("right_arm", pivot=(-5.0, 2.0, 0.0))
    m.bone("left_arm", pivot=(5.0, 2.0, 0.0))
    m.bone("right_leg", pivot=(-1.9, 12.0, 0.0))
    m.bone("left_leg", pivot=(1.9, 12.0, 0.0))
    return m


def _no_glow(painter):
    def paint(ctx: Ctx):
        col = painter(ctx)
        ctx.glow = None
        return col
    return paint


def _gem(ctx: Ctx):
    s = (ctx.s + 0.5) / max(ctx.w, 1)
    t = (ctx.t + 0.5) / max(ctx.h, 1)
    v = 0.45 + max(0.0, 0.35 - math.hypot(s - 0.35, t - 0.3)) * 1.6 + hash01(ctx.u, ctx.v, 5) * 0.06
    if ctx.s in (0, ctx.w - 1) or ctx.t in (0, ctx.h - 1):
        v -= 0.2
    return pick(ctx.pal["crystal"], v)


def materials(variant: str) -> dict:
    return {"gold": _no_glow(_gold), "crystal": _no_glow(_crystal), "gem": _gem}
