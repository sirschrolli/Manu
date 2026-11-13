"""
Dornwicht: der Hüter der Dornenwildnis – eine hagere, über zwei Blöcke hohe Gestalt aus schwarzen, verdrehten Ranken
und Rinde, besetzt mit violetten Dornen. Der Kopf ist eine hohle Rindenmaske mit einer Krone aus Dornen, in der zwei
violette Augen glühen; die langen Arme enden in Dornenklauen, die Beine in Wurzelfüßen. Zwischen den Ranken glimmt
an Brust und Rücken ein violettes Licht.

Knochen-Namen werden in ThornWightModel (Java) für die Animationen verwendet.
"""
from __future__ import annotations

import math

from ..geometry import Model
from ..paint import Ctx, hash01, mix, noise3, pick, ramp, rel_y

PALETTES = {
    "thorn_wight": {
        "bark": ramp("#120a14", "#5a4250", steps=7), "vine": ramp("#1a0e22", "#5a3070", steps=7),
        "thorn": ramp("#4a1a6a", "#f0b8ff", steps=6), "glow": ramp("#8a2ad8", "#f4d0ff", steps=5),
        "mask": ramp("#2a1e24", "#9a8478", steps=7),
    },
}


def build() -> Model:
    m = Model("thorn_wight", 64, 64)
    m.bone("wight", pivot=(0, 24, 0))
    # Beine (Hüfte 15 Pixel über dem Boden)
    for side, sx in (("left", 1), ("right", -1)):
        leg = f"{side}_leg"
        m.bone(leg, "wight", pivot=(1.6 * sx, -15, 0))
        m.cube(leg, (-1, 0, -1), (2, 14, 2), "vine")
        m.cube(leg, (-1.5, 13, -2.2), (3, 2, 3.4), "bark")              # Wurzelfuß
    # Rumpf
    m.bone("body", "wight", pivot=(0, -15, 0))
    m.cube("body", (-2.5, -11, -1.5), (5, 11, 3), "torso")
    m.cube("body", (-3.2, -12, -1.8), (6.4, 3, 3.6), "bark")             # Schultern
    for i, (x, y, rz) in enumerate(((-1.4, -9, -35), (1.2, -6, 30), (-0.6, -3, -20), (1.6, -10, 40))):
        m.bone(f"spine{i}", "body", pivot=(x, y, 1.5), rotation=(-55, 0, rz))
        m.cube(f"spine{i}", (-0.4, -3.2, -0.4), (0.8, 3.2, 0.8), "thorn")
    # Kopf mit Dornenkrone
    m.bone("head", "body", pivot=(0, -12, 0))
    m.cube("head", (-2.4, -6, -2.6), (4.8, 6, 5), "mask")
    m.cube("head", (-1.2, -1.2, -3.1), (2.4, 1.6, 0.6), "jaw")
    for i, (x, rz, rx) in enumerate(((-2.0, -25, -10), (-0.8, -8, -18), (0.8, 8, -18), (2.0, 25, -10), (0.0, 0, 15))):
        m.bone(f"horn{i}", "head", pivot=(x, -5.6, -0.5 + (2.0 if i == 4 else 0.0)), rotation=(rx, 0, rz))
        m.cube(f"horn{i}", (-0.45, -4.0 + (1 if i in (0, 3) else 0), -0.45), (0.9, 4.0 - (1 if i in (0, 3) else 0), 0.9), "thorn")
    # Arme mit Unterarmen und Klauen
    for side, sx in (("left", 1), ("right", -1)):
        arm = f"{side}_arm"
        m.bone(arm, "body", pivot=(3.9 * sx, -11, 0))
        m.cube(arm, (-1 if sx > 0 else -1, 0, -1), (2, 9, 2), "vine")
        fore = f"{side}_forearm"
        m.bone(fore, arm, pivot=(0, 9, 0))
        m.cube(fore, (-0.9, 0, -0.9), (1.8, 8, 1.8), "vine")
        for k, (dx, dz) in enumerate(((-0.7, -0.6), (0.0, -0.8), (0.7, -0.6))):
            claw = f"{side}_claw{k}"
            m.bone(claw, fore, pivot=(dx, 8, dz), rotation=(-18, 0, (k - 1) * 12))
            m.cube(claw, (-0.3, 0, -0.3), (0.6, 3.2, 0.6), "thorn")
    return m


_LIT = {"top": 0.1, "front": 0.04, "west": 0.0, "east": -0.03, "back": -0.06, "bottom": -0.16}


def _vine(ctx: Ctx):
    """Verdrehte Ranken: dunkle, gewundene Stränge, an manchen Stellen ein heller Dorn."""
    x, y, z = ctx.world
    twist = math.sin(y * 0.9 + x * 1.7 + z * 1.3) * 0.5 + 0.5
    v = 0.3 + twist * 0.3 + _LIT[ctx.face] - rel_y(ctx) * 0.05
    if hash01(ctx.u, ctx.v, "thorn") > 0.94:
        return pick(ctx.pal["thorn"], 0.7)
    return pick(ctx.pal["vine"], v + (hash01(ctx.u, ctx.v) - 0.5) * 0.06)


def _bark(ctx: Ctx):
    x, y, z = ctx.world
    v = 0.3 + noise3(x * 0.6, y * 1.4, z * 0.6, 5) * 0.35 + _LIT[ctx.face]
    if ctx.s % 3 == 0:
        v -= 0.1
    return pick(ctx.pal["bark"], v)


def _torso(ctx: Ctx):
    """Rumpf aus Ranken; durch die Lücken glimmt in der Brust ein violettes Licht."""
    face, s, t = ctx.face, ctx.s, ctx.t
    if face in ("front", "back"):
        cx, cy = ctx.w / 2 - 0.5, 3.5
        d = math.hypot(s - cx, (t - cy) * 0.8)
        gap = math.sin((s + t * 0.6) * 1.9) > 0.35
        if d < 2.2 and gap:
            col = pick(ctx.pal["glow"], 0.85 if d < 1.0 else 0.55)
            ctx.glow = col
            return col
    return _vine(ctx)


def _mask(ctx: Ctx):
    """Rindenmaske: hohle Augenhöhlen mit glühenden Augen, tiefe Kerben, oben dunkler."""
    face, s, t = ctx.face, ctx.s, ctx.t
    if face == "front":
        if t == 2 and s in (1, 3):
            col = pick(ctx.pal["glow"], 0.95)
            ctx.glow = col
            return col
        if t in (2, 3) and s in (0, 1, 3, 4) and not (t == 3 and s in (0, 4)):
            return pick(ctx.pal["bark"], 0.05)                          # Augenhöhlen
        if s == 2 and t >= 3:
            return pick(ctx.pal["mask"], 0.25)                          # Kerbe über der Nase
    v = 0.42 + _LIT[face] - rel_y(ctx) * 0.1 + (hash01(ctx.u, ctx.v) - 0.5) * 0.08
    if (ctx.s * 3 + ctx.t) % 7 == 0:
        v -= 0.15
    return pick(ctx.pal["mask"], v)


def _jaw(ctx: Ctx):
    if ctx.face == "front" and ctx.s % 2 == 1:
        return pick(ctx.pal["thorn"], 0.6)                              # Zähne aus Dornen
    return pick(ctx.pal["mask"], 0.3)


def _thorn(ctx: Ctx):
    v = 0.35 + (1.0 - rel_y(ctx)) * 0.5 + _LIT[ctx.face]
    return pick(ctx.pal["thorn"], v)


def materials(variant: str) -> dict:
    return {"vine": _vine, "bark": _bark, "torso": _torso, "mask": _mask, "jaw": _jaw, "thorn": _thorn}
