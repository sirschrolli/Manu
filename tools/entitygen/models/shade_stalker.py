"""
Schattenpirscher: ein hagerer, übermannsgroßer Schatten mit gebeugtem Rücken, langen Armen und drei Krallen an jeder
Hand. Ein zerfetzter Umhang aus Schatten hängt ihm vom Rücken, aus dem länglichen Schädel glühen zwei violette Augen.

Knochen-Namen werden in ShadeStalkerModel (Java) für die Animationen verwendet.
"""
from __future__ import annotations

import math

from ..geometry import Model
from ..paint import Ctx, hash01, mix, noise3, pick, ramp, rel_y

PALETTES = {
    "shade_stalker": {
        "shade": ramp("#08060e", "#4a3870"), "smoke": ramp("#120c1e", "#6a5896"), "bone": ramp("#5a5468", "#d8d2e6"),
        "eye": ramp("#6a2ad8", "#f2e4ff"), "back": ramp("#07050c", "#3c2c5c"),
    },
}


def build() -> Model:
    m = Model("shade_stalker", 64, 64)
    # Beine: lang und dünn, Füße mit Krallenzehen
    for side in (1, -1):
        name = "left_leg" if side > 0 else "right_leg"
        m.bone(name, pivot=(2.3 * side, 4, 0.5))
        m.cube(name, (-1, 0, -1), (2, 19, 2), "limb")
        m.cube(name, (-1.5, 18, -3), (3, 2, 4), "limb")
        m.cube(name, (-1.5, 19.5, -4), (3, 0.5, 1), "claw")

    # Rumpf: schmal, nach vorn gebeugt, Rippen und Rückengrat
    m.bone("body", pivot=(0, 4, 0.5), rotation=(16, 0, 0))
    m.cube("body", (-3.5, -14, -2), (7, 14, 4), "torso")
    m.cube("body", (-2.5, -6, -2.6), (5, 4, 1), "torso")              # Bauchgrube
    for i in range(3):
        m.cube("body", (-0.5, -12.5 + i * 4, 1.6), (1, 2, 1.2), "bone")   # Wirbel
    # Umhang aus Schatten, unten zerfetzt
    m.bone("cloak", "body", pivot=(0, -13.5, 2.1), rotation=(-12, 0, 0))
    m.cube("cloak", (-4, 0, 0), (8, 18, 1), "cloak")

    m.bone("head", "body", pivot=(0, -14, -0.5), rotation=(-14, 0, 0))
    m.cube("head", (-2.5, -6, -3.5), (5, 6, 5), "skull")
    m.cube("head", (-2, -2, -6), (4, 2, 3), "skull")                 # lange Schnauze
    m.cube("head", (-1.5, 0, -5.5), (3, 1, 3), "skull")              # Unterkiefer
    m.cube("head", (-1.8, -4.2, -3.8), (1.2, 1, 0.5), "eye")
    m.cube("head", (0.6, -4.2, -3.8), (1.2, 1, 0.5), "eye")
    # Schattenfäden am Hinterkopf
    for side in (1, -1):
        name = "wisp_l" if side > 0 else "wisp_r"
        m.bone(name, "head", pivot=(1.5 * side, -5, 1.5), rotation=(-35, 14 * side, 0))
        m.cube(name, (-0.5, 0, 0), (1, 1, 7), "smoke")

    # Arme: lang, mit Unterarm und drei Krallen
    for side in (1, -1):
        arm = "left_arm" if side > 0 else "right_arm"
        fore = arm.replace("arm", "forearm")
        m.bone(arm, "body", pivot=(4.5 * side, -13, 0), rotation=(-10, 0, -6 * side))
        m.cube(arm, (-1 if side > 0 else -1, -1, -1), (2, 10, 2), "limb")
        m.bone(fore, arm, pivot=(0, 9, 0), rotation=(-18, 0, 0))
        m.cube(fore, (-1, 0, -1), (2, 10, 2), "limb")
        for k, (dx, rz) in enumerate(((-0.8, 14), (0, 0), (0.8, -14))):
            claw = f"{fore}_claw{k + 1}"
            m.bone(claw, fore, pivot=(dx, 10, -0.5), rotation=(-12, 0, rz))
            m.cube(claw, (-0.25, 0, -0.25), (0.5, 5, 0.5), "claw")
    return m


def _shade(ctx: Ctx, base: float = 0.35, contrast: float = 0.4):
    x, y, z = ctx.world
    n = noise3(x * 0.4, y * 0.4, z * 0.4, 71)
    swirl = math.sin(y * 0.7 + n * 4.0 + x * 0.3)
    v = base + (n - 0.5) * contrast + swirl * 0.06
    col = pick(ctx.pal["shade"], v)
    # Nebelschleier: hellere Schlieren auf den Seiten
    if ctx.face in ("west", "east", "back") and swirl > 0.82:
        col = mix(col, pick(ctx.pal["smoke"], 0.5), 0.5)
    return col


def _torso(ctx: Ctx):
    col = _shade(ctx)
    lx = ctx.local[0] - ctx.cube.origin[0]
    if ctx.face == "front" and ctx.cube.size[1] >= 14:
        # Rippen
        ry = rel_y(ctx)
        if 0.12 < ry < 0.55 and (ctx.t % 3 == 0) and 0.8 < lx < ctx.cube.size[0] - 0.8:
            return pick(ctx.pal["bone"], 0.25)
    return col


def _limb(ctx: Ctx):
    col = _shade(ctx, 0.3, 0.3)
    if ctx.face == "bottom":
        return pick(ctx.pal["shade"], 0.1)
    return col


def _skull(ctx: Ctx):
    x, y, z = ctx.world
    n = noise3(x * 0.8, y * 0.8, z * 0.8, 72)
    v = 0.28 + n * 0.25
    if ctx.face == "top":
        v += 0.1
    return pick(ctx.pal["shade"], v)


def _cloak(ctx: Ctx):
    ry = rel_y(ctx)
    # ausgefranster Saum: unten Lücken, je weiter unten, desto mehr
    lx = ctx.local[0] - ctx.cube.origin[0]
    tear = hash01(int(lx * 2), 73) * 0.35
    if ry > 0.62 + tear:
        return None
    if ry > 0.5 + tear and hash01(ctx.u, ctx.v, 74) > 0.55:
        return None
    x, y, z = ctx.world
    n = noise3(x * 0.5, y * 0.3, z * 0.5, 75)
    v = 0.18 + n * 0.3 + ry * 0.25
    col = pick(ctx.pal["smoke"], v)
    if ctx.face == "front":
        col = pick(ctx.pal["shade"], 0.2 + n * 0.15)
    return col


def _smoke(ctx: Ctx):
    a = (ctx.local[2] - ctx.cube.origin[2]) / max(ctx.cube.size[2], 1e-3)
    if a > 0.75 and hash01(ctx.u, ctx.v, 76) > 0.5:
        return None
    return pick(ctx.pal["smoke"], 0.25 + a * 0.45)


def _eye(ctx: Ctx):
    col = pick(ctx.pal["eye"], 0.85 if ctx.face == "front" else 0.6)
    ctx.glow = col
    return col


def materials(variant: str) -> dict:
    return {
        "torso": _torso,
        "limb": _limb,
        "skull": _skull,
        "cloak": _cloak,
        "smoke": _smoke,
        "eye": _eye,
        "bone": lambda ctx: pick(ctx.pal["bone"], 0.45 + hash01(ctx.u, ctx.v) * 0.2),
        "claw": lambda ctx: pick(ctx.pal["bone"], 0.6 + (0.2 if ctx.face == "west" else 0.0)),
    }
