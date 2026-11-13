"""
Kristallkriecher: ein flacher, sechsbeiniger Panzerkäfer aus den Kristallschluchten. Auf seinem Rücken wachsen
Nachtkristalle, die im Dunkeln leuchten; vorn sitzen vier glühende Augen und zwei Beißzangen.

Knochen-Namen werden in CrystalCrawlerModel (Java) für die Animationen verwendet.
"""
from __future__ import annotations

from ..geometry import Model
from ..paint import Ctx, hash01, mix, noise3, pick, ramp, rel_y

PALETTES = {
    "crystal_crawler": {
        "shell": ramp("#08081a", "#3a3c74"), "joint": ramp("#14122a", "#56508a"), "crystal": ramp("#232270", "#b4a8ff", steps=7),
        "eye": ramp("#2aa8c8", "#e8ffff"), "back": ramp("#08081a", "#3a3c74"),
    },
}

# (Name, Neigung um X, Neigung um Z, Länge, Breite, Lage auf dem Rücken)
CRYSTALS = [
    ("crystal1", -8, 10, 8, 2, (-1.5, -3, -2)),
    ("crystal2", 14, -16, 6, 2, (2.0, -3, 1)),
    ("crystal3", -22, -8, 5, 2, (0.5, -3, -5)),
    ("crystal4", 20, 22, 4, 1, (-2.5, -3, 3)),
    ("crystal5", 30, -4, 3, 1, (1.0, -3, 5)),
]


def build() -> Model:
    m = Model("crystal_crawler", 64, 96)
    m.bone("body", pivot=(0, 17, 0))
    m.cube("body", (-5, -3, -7), (10, 5, 13), "shell")
    m.cube("body", (-4, 2, -6), (8, 1, 11), "joint")                 # Bauchplatten
    m.bone("abdomen", "body", pivot=(0, -1, 6), rotation=(-8, 0, 0))
    m.cube("abdomen", (-4, -2, 0), (8, 5, 7), "shell")
    m.cube("abdomen", (-2.5, -1, 7), (5, 3, 2), "shell")

    m.bone("head", "body", pivot=(0, 0, -7))
    m.cube("head", (-3.5, -2.5, -4), (7, 4, 4), "head")
    for k, (x, y) in enumerate(((-2.4, -1.6), (1.4, -1.6), (-1.6, -0.4), (0.6, -0.4))):
        m.cube("head", (x, y, -4.3), (1, 1, 0.5), "eye")
    for side in (1, -1):
        name = "mandible_l" if side > 0 else "mandible_r"
        m.bone(name, "head", pivot=(2.2 * side, 0.5, -4), rotation=(0, 22 * side, 0))
        m.cube(name, (-0.5 if side > 0 else -0.5, -0.5, -4), (1, 1, 4), "mandible")

    # sechs Beine, schräg nach außen und unten
    for i, z in enumerate((-4.5, 0.0, 4.5)):
        for side in (1, -1):
            name = f"leg_{'l' if side > 0 else 'r'}{i + 1}"
            yaw = (-20, 0, 20)[i] * side
            m.bone(name, "body", pivot=(4.5 * side, 0, z), rotation=(0, yaw, 42 * side))
            m.cube(name, (0 if side > 0 else -9, -0.75, -0.75), (9, 1.5, 1.5), "leg")
            foot = name + "_foot"
            m.bone(foot, name, pivot=(8.5 * side, 0, 0), rotation=(0, 0, 50 * side))
            m.cube(foot, (0 if side > 0 else -5, -0.5, -0.5), (5, 1, 1), "leg")

    # Nachtkristalle auf dem Rücken
    m.bone("crystals", "body", pivot=(0, 0, 0))
    for name, rx, rz, length, width, (x, y, z) in CRYSTALS:
        m.bone(name, "crystals", pivot=(x, y, z), rotation=(rx, 0, rz))
        m.cube(name, (-width / 2, -length, -width / 2), (width, length, width), "crystal")
    return m


def _shell(ctx: Ctx):
    x, y, z = ctx.world
    n = noise3(x * 0.6, y * 0.6, z * 0.6, 81)
    v = 0.35 + (n - 0.5) * 0.35
    if ctx.face == "top":
        v += 0.12
        # Panzerplatten: Fugen quer zum Körper
        if int(z + 40) % 4 == 0:
            v -= 0.18
    if ctx.face == "bottom":
        v -= 0.1
    col = pick(ctx.pal["shell"], v)
    # Glanz an den Kanten
    if ctx.face in ("west", "east") and rel_y(ctx) < 0.2:
        col = mix(col, pick(ctx.pal["crystal"], 0.3), 0.25)
    return col


def _head(ctx: Ctx):
    return pick(ctx.pal["shell"], 0.3 + hash01(ctx.u, ctx.v) * 0.12 + (0.1 if ctx.face == "top" else 0))


def _crystal(ctx: Ctx):
    a = 1.0 - rel_y(ctx)                       # 0 an der Basis … 1 an der Spitze (Kristall zeigt nach oben)
    edge = ctx.s == 0 or ctx.s == ctx.w - 1
    v = 0.14 + a * 0.6 + (0.12 if edge else 0.0) + (0.06 if ctx.face in ("top", "west") else 0.0)
    col = pick(ctx.pal["crystal"], v)
    # nur die Spitzen und hellen Kanten leuchten – blauviolett, nicht weiß
    if a > 0.6 or (edge and a > 0.35):
        ctx.glow = pick(ctx.pal["crystal"], min(0.8, v))
    return col


def _eye(ctx: Ctx):
    col = pick(ctx.pal["eye"], 0.8)
    ctx.glow = col
    return col


def materials(variant: str) -> dict:
    return {
        "shell": _shell,
        "head": _head,
        "joint": lambda ctx: pick(ctx.pal["joint"], 0.4 + hash01(ctx.u, ctx.v) * 0.15),
        "leg": lambda ctx: pick(ctx.pal["joint"], 0.3 + (0.15 if ctx.face == "top" else 0.0) + hash01(ctx.u, ctx.v) * 0.1),
        "mandible": lambda ctx: pick(ctx.pal["crystal"], 0.2 + rel_y(ctx) * 0.1),
        "crystal": _crystal,
        "eye": _eye,
    }
