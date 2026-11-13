"""
Silberfischchen auf dem Skelett des Vanilla-Silberfischchens (sieben Glieder "segment0".."segment6" und die drei
Teile "layer0".."layer2" mit den Vanilla-Drehpunkten – so schlängeln sie sich wie bei Vanilla).

Statt der struppigen Vanilla-Borsten tragen die drei "layer"-Teile hier die Beinpaare (je ein dünner Quader quer
unter dem Leib, der links und rechts herausschaut); dazu Fühler am Kopf und drei Schwanzfäden. Die Glieder haben
schuppige Rückenplatten mit Metallglanz.

  * Silberfischchen: silbergrau mit bläulichem Glanz
  * Tiefenfischchen: dunkles Tiefenschiefer-Grau mit blauem Schimmer
"""
from __future__ import annotations

from ..geometry import Model
from ..paint import Ctx, hash01, mix, pick, ramp

PALETTES = {
    "silberfischchen": {"shell": ramp("#2e323a", "#eef3f8", steps=8), "sheen": ramp("#6a7c96", "#dbe8ff"),
                        "leg": ramp("#22252a", "#8a929c"), "eye": ramp("#020203", "#3a3e48"), "belly": ramp("#6a6e74", "#d4d6d8")},
    "tiefenfischchen": {"shell": ramp("#0e0f13", "#7a8090", steps=8), "sheen": ramp("#1e2e5a", "#6a94e0"),
                        "leg": ramp("#0a0b0e", "#4a4e58"), "eye": ramp("#000000", "#2a2c36"), "belly": ramp("#2a2c32", "#6a6e78")},
}

_SIZES = [(3, 2, 2), (4, 3, 2), (6, 4, 3), (3, 3, 3), (2, 2, 3), (2, 1, 2), (1, 1, 2)]


def _placements() -> list[float]:
    z = -3.5
    out = []
    for i, (_, _, d) in enumerate(_SIZES):
        out.append(z)
        if i < len(_SIZES) - 1:
            z += (d + _SIZES[i + 1][2]) * 0.5
    return out


def build() -> Model:
    m = Model("silverfish", 64, 32)
    zs = _placements()
    for i, (w, h, d) in enumerate(_SIZES):
        name = f"segment{i}"
        m.bone(name, pivot=(0, 24 - h, zs[i]))
        m.cube(name, (w * -0.5, 0, d * -0.5), (w, h, d), "head" if i == 0 else "segment")
    # Fühler und Schwanzfäden
    m.bone("antenna_right", "segment0", pivot=(-0.8, 0.3, -1), rotation=(-18, 28, 0))
    m.cube("antenna_right", (-0.3, -0.3, -5), (0.6, 0.6, 5), "antenna")
    m.bone("antenna_left", "segment0", pivot=(0.8, 0.3, -1), rotation=(-18, -28, 0))
    m.cube("antenna_left", (-0.3, -0.3, -5), (0.6, 0.6, 5), "antenna")
    for name, yaw in (("cercus_right", -30), ("cercus_middle", 0), ("cercus_left", 30)):
        m.bone(name, "segment6", pivot=(0, 0.4, 1), rotation=(8, yaw, 0))
        m.cube(name, (-0.25, -0.25, 0), (0.5, 0.5, 4 if yaw == 0 else 3.5), "cercus")
    # Beinpaare in den Vanilla-"layer"-Teilen (gleiche Drehpunkte wie bei Vanilla)
    m.bone("layer0", pivot=(0, 16, zs[2]))
    m.cube("layer0", (-4.6, 7.1, -1.1), (9.2, 0.8, 0.6), "leg")
    m.cube("layer0", (-4.2, 7.1, 0.6), (8.4, 0.8, 0.6), "leg")
    m.bone("layer1", pivot=(0, 20, zs[4]))
    m.cube("layer1", (-2.8, 3.1, -0.3), (5.6, 0.8, 0.6), "leg")
    m.bone("layer2", pivot=(0, 19, zs[1]))
    m.cube("layer2", (-3.8, 4.1, -0.3), (7.6, 0.8, 0.6), "leg")
    return m


# =============================================================================================
# Malen
# =============================================================================================

def _shell(ctx: Ctx, base: float):
    """Schuppenplatte: Metallglanz längs der Mitte, dunkle Fuge am Vorderrand, Schuppenreihen."""
    face, s, t, w, h = ctx.face, ctx.s, ctx.t, ctx.w, ctx.h
    v = base + (hash01(ctx.u, ctx.v, 951) - 0.5) * 0.06
    if face == "top":
        mid = abs(s - (w - 1) / 2) / max(1.0, w / 2)
        v += 0.18 * (1 - mid)                                                            # Glanz in der Mitte
        if t == h - 1:
            v -= 0.22                                                                    # Fuge zum vorderen Glied
        col = pick(ctx.pal["shell"], v)
        if (s + t) % 3 == 0 and w > 2:
            col = mix(col, pick(ctx.pal["sheen"], 0.6), 0.35)                            # bläulicher Schimmer
        return col
    if face == "bottom":
        return pick(ctx.pal["belly"], 0.45 + hash01(ctx.u, ctx.v) * 0.1)
    if face in ("west", "east"):
        v += 0.1 - t * 0.12                                                              # oben hell, unten dunkel
        col = pick(ctx.pal["shell"], v)
        if t == 0 and (s % 2 == 0):
            col = mix(col, pick(ctx.pal["sheen"], 0.7), 0.4)
        return col
    if face == "front":
        return pick(ctx.pal["shell"], v - 0.12 - t * 0.08)
    return pick(ctx.pal["shell"], v - 0.05 - t * 0.08)


def _segment(ctx: Ctx):
    return _shell(ctx, 0.56)


def _head(ctx: Ctx):
    face, s, t = ctx.face, ctx.s, ctx.t
    if face == "front":
        if t == 0 and s in (0, ctx.w - 1):
            return pick(ctx.pal["eye"], 0.2)                                              # Augen
        if t == ctx.h - 1 and 0 < s < ctx.w - 1:
            return pick(ctx.pal["leg"], 0.3)                                              # Mundwerkzeug
        return pick(ctx.pal["shell"], 0.5)
    if face in ("west", "east") and t == 0:
        front = ctx.w - 1 if face == "west" else 0
        if s == front:
            return pick(ctx.pal["eye"], 0.25)
    return _shell(ctx, 0.5)


def _antenna(ctx: Ctx):
    lz = ctx.local[2]
    tip = (-lz) / 5.0                                                                     # 0 am Kopf .. 1 an der Spitze
    return pick(ctx.pal["leg"], 0.35 + tip * 0.45)


def _cercus(ctx: Ctx):
    lz = ctx.local[2]
    return pick(ctx.pal["leg"], 0.4 + lz / 4.0 * 0.4)


def _leg(ctx: Ctx):
    lx = abs(ctx.local[0])
    if lx < 2.6:
        return pick(ctx.pal["leg"], 0.2)                                                  # unter dem Leib
    joint = int(lx * 2) % 3 == 0
    return pick(ctx.pal["leg"], 0.55 + (0.15 if joint else 0.0) + (0.1 if ctx.face == "top" else 0.0))


def materials(variant: str) -> dict:
    return {
        "head": _head,
        "segment": _segment,
        "antenna": _antenna,
        "cercus": _cercus,
        "leg": _leg,
    }
