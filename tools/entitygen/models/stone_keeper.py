"""
Der Steinhüter (0.96): der Wächter des alten Mondsteinbruchs im Feenreich. Das Volk, das lange vor den Feen hier Mondstein
brach, schichtete ihn aus Feenstein und band ihn mit Mondstein – seither kniet er am Grund des Bruchs und wartet. Im Spiel
1,4-fach vergrößert (gut dreieinhalb Blöcke hoch).

  * ein gebeugter Riese aus gefügten Feensteinquadern (Mörtelfugen, Risse, Feenmoos auf allen Oberseiten), breite Brust,
    ein Buckel aus Fels auf dem Rücken
  * ein kleiner Kopf tief zwischen den Schultern: schwere Stirn, darunter zwei schmale, mondblaue Augen, ein Unterkiefer aus
    einem einzigen Block, auf der Stirn ein Mondstein, auf dem Scheitel zwei Kristalle
  * auf der Brust eine Platte mit eingemeißelten Runen des alten Volks (sie leuchten), darin das Hüterherz – ein großer
    Mondstein, der pulsiert
  * lange Arme bis fast auf den Boden: Schulterfelsen mit Kristallbüscheln, Unterarme dicker als die Oberarme, mit
    Mondsteinbändern, und Fäuste aus grobem Fels mit Knöcheln
  * kurze, säulenhafte Beine mit Knien (zum Knien und Aufstehen), breite Füße mit Zehensteinen
  * auf dem Buckel ein Kamm aus Mondsteinkristallen, die nach hinten oben wachsen; Leuchtranken hängen von den Schultern
  * der Kristallpanzer (unter halber Kraft): Kristallplatten wachsen über Brust, Unterarme, Schenkel und Kopf (Knochen
    armor_*, im Spiel nur dann sichtbar)

Boden-Regel: y = 24 ist der Boden, oben ist -y, vorn ist -z. Knochen-Namen werden in StoneKeeperModel (Java) für die
Animationen verwendet.
"""
from __future__ import annotations

import math

from ..geometry import Model
from ..paint import Ctx, Voronoi3D, hash01, hexc, mix, noise3, pick, ramp, rel_y


def stops(*colors: str) -> list:
    return [hexc(c) for c in colors]


PALETTES = {
    "stone_keeper": {
        "stone": stops("#3a3448", "#4e475e", "#645c76", "#7b738e", "#938aa6", "#aaa2bc", "#bfb8cf", "#d1cbdd", "#e1dce9",
                       "#efecf4"),
        "mortar": stops("#2a2534", "#3a3446", "#4c4558"),
        "moss": stops("#4a3678", "#62489a", "#7b5eb8", "#9478cc", "#ae94dc", "#c8b2ec"),
        "spore": stops("#ff7ccf", "#ffb0e4", "#7ce8ff", "#ffe07a"),
        "moon": stops("#1e3e9a", "#2f62d0", "#4f8cf0", "#7cb4ff", "#a8d2ff", "#d4ecff", "#f4fbff"),
        "gem": stops("#5a64a8", "#7a86c8", "#9eaae0", "#c4ccf0", "#e2e8fa", "#ffffff"),
        "crystal": stops("#2a3c8c", "#3e56b0", "#5a78d0", "#7e9ce6", "#a6bef4", "#cad8fb", "#eef4ff"),
        "vine": stops("#1a2a24", "#28403a", "#3a5a50", "#4e7468"),
        "bloom": stops("#ff9ad0", "#ffd0ec", "#9af2ff", "#e0fffc"),
    },
}

# Quaderfugen: Höhe der Steinreihen und Breite der Quader (Einheiten)
ROW = 3.6
BLOCK = 4.8


def _crystal(m: Model, parent: str, name: str, pivot, rot, height: float, width: float) -> None:
    """Ein Mondsteinkristall: ein sechskantiges Prisma aus zwei Gliedern (breiter Fuß, schmale Spitze)."""
    m.bone(name, parent, pivot, rot)
    h1, h2 = height * 0.62, height * 0.48
    m.cube(name, (-width / 2, -h1, -width / 2), (width, h1, width), "crystal", tag="up:0.0:0.6")
    tip = f"{name}_tip"
    m.bone(tip, name, (0.0, -h1 + 0.3, 0.0), (0.0, 45.0, 0.0))
    w2 = width * 0.62
    m.cube(tip, (-w2 / 2, -h2, -w2 / 2), (w2, h2, w2), "crystal", tag="up:0.55:1.0")


def _cluster(m: Model, parent: str, name: str, base, crystals) -> None:
    """Ein Büschel Kristalle: (x, z, Höhe, Breite, Neigung x, Neigung z) je Kristall, alle aus einem Punkt."""
    for i, (dx, dz, height, width, rx, rz) in enumerate(crystals):
        _crystal(m, parent, f"{name}_{i}", (base[0] + dx, base[1], base[2] + dz), (rx, 0.0, rz), height, width)


def build() -> Model:
    m = Model("stone_keeper", 256, 256)

    # ------------------------------------------------------------------ Beine (mit Knie)
    for side, sx in (("left", 1), ("right", -1)):
        leg = f"{side}_leg"
        m.bone(leg, pivot=(5.6 * sx, 10.0, 0.5))
        m.cube(leg, (-3.6, -1.0, -3.6), (7.2, 7.8, 7.2), "stone")
        shin = f"{side}_shin"
        m.bone(shin, leg, pivot=(0.0, 6.6, -0.4))
        m.cube(shin, (-3.9, -0.6, -3.7), (7.8, 6.6, 7.8), "stone")
        m.cube(shin, (-4.4, 5.4, -5.8), (8.8, 2.0, 9.6), "stone_rough")                 # Fuß
        for tx in (-3.2, -0.2, 2.8):
            m.cube(shin, (tx - 0.2 if sx > 0 else -tx - 2.2, 5.6, -7.0), (2.4, 1.8, 1.6), "stone_rough")  # Zehensteine
        m.cube(shin, (-2.0, 0.6, -4.3), (4.0, 3.0, 0.8), "inlay")                       # Mondsteinband am Schienbein
        # Kristallpanzer: Splitter über Schenkel und Knie
        arm_bone = f"armor_{side}_leg"
        m.bone(arm_bone, leg, pivot=(0.0, 2.0, -3.6))
        _crystal(m, arm_bone, f"{arm_bone}_0", (0.0, 2.6, -0.6), (-58.0, 0.0, 12.0 * sx), 4.6, 2.2)
        _crystal(m, arm_bone, f"{arm_bone}_1", (1.6 * sx, 0.4, 0.4), (-40.0, 0.0, 36.0 * sx), 3.6, 1.8)
        m.cube(arm_bone, (-3.0, -1.6, -0.8), (6.0, 6.0, 1.2), "crystal_plate")

    # ------------------------------------------------------------------ Rumpf
    m.bone("body", pivot=(0.0, 10.0, 0.0), rotation=(10.0, 0.0, 0.0))
    m.cube("body", (-7.8, -4.4, -4.8), (15.6, 5.6, 9.6), "stone")                     # Becken
    m.cube("body", (-8.6, -11.6, -5.4), (17.2, 7.6, 10.8), "stone")                   # Bauch
    m.cube("body", (-8.9, -6.2, -5.7), (17.8, 1.7, 11.4), "inlay")                    # Mondsteingürtel
    m.cube("body", (-11.5, -23.0, -6.8), (23.0, 11.8, 13.6), "stone")                 # Brust
    m.cube("body", (-9.0, -22.0, 4.4), (18.0, 10.0, 4.6), "stone_rough")              # Buckel
    m.cube("body", (-6.4, -24.4, -2.0), (12.8, 2.2, 8.0), "stone_rough")              # Nacken
    m.cube("body", (-6.6, -20.4, -7.8), (13.2, 8.6, 1.2), "rune_plate")               # Runenplatte
    # das Hüterherz (pulsiert)
    m.bone("core", "body", pivot=(0.0, -15.8, -8.0))
    m.cube("core", (-2.3, -2.4, -0.9), (4.6, 4.8, 1.6), "core")
    m.cube("core", (-1.3, -3.4, -0.6), (2.6, 6.8, 1.2), "core")
    m.cube("core", (-3.3, -1.4, -0.6), (6.6, 2.8, 1.2), "core")
    # Kristallkamm auf dem Buckel
    _cluster(m, "body", "back_crystal", (0.0, -21.6, 6.8), [
        (0.0, 0.0, 9.0, 3.2, -34.0, 0.0), (-3.6, 0.8, 6.6, 2.6, -28.0, -24.0), (3.6, 0.6, 7.2, 2.6, -30.0, 22.0),
        (-6.4, 1.6, 4.4, 2.0, -20.0, -40.0), (6.2, 1.4, 4.8, 2.0, -24.0, 38.0), (1.6, 3.2, 4.6, 2.0, -58.0, 10.0),
        (-1.8, 3.0, 3.8, 1.8, -62.0, -14.0)])
    # Leuchtranken hängen vom Buckel
    for i, (x, length) in enumerate(((-7.6, 7.0), (-4.4, 4.2), (7.2, 5.6))):
        m.bone(f"vine_back_{i}", "body", pivot=(x, -14.0, 9.0))
        m.cube(f"vine_back_{i}", (-0.4, 0.0, -0.4), (0.8, length, 0.8), "vine")
    # Kristallpanzer der Brust: Platten und Splitter
    m.bone("armor_chest", "body", pivot=(0.0, -16.0, -8.2))
    m.cube("armor_chest", (-10.0, -6.8, -0.6), (6.8, 10.4, 1.4), "crystal_plate")
    m.cube("armor_chest", (3.2, -6.8, -0.6), (6.8, 10.4, 1.4), "crystal_plate")
    _crystal(m, "armor_chest", "armor_chest_0", (-6.6, -5.6, -0.4), (-62.0, 0.0, -22.0), 4.4, 2.2)
    _crystal(m, "armor_chest", "armor_chest_1", (6.6, -5.6, -0.4), (-62.0, 0.0, 22.0), 4.4, 2.2)
    _crystal(m, "armor_chest", "armor_chest_2", (-6.0, 2.8, -0.4), (-80.0, 0.0, -10.0), 3.2, 1.8)
    _crystal(m, "armor_chest", "armor_chest_3", (6.0, 2.8, -0.4), (-80.0, 0.0, 10.0), 3.2, 1.8)

    # der Felsbrocken, den er beim Felswurf aus dem Boden reißt (im Spiel nur dann sichtbar)
    m.bone("boulder", "body", pivot=(0.0, -44.0, -6.0))
    m.cube("boulder", (-6.8, -5.6, -6.2), (13.6, 11.2, 12.4), "boulder")
    m.cube("boulder", (-5.0, -7.2, -4.4), (10.0, 2.0, 8.8), "boulder")
    m.cube("boulder", (-5.4, 5.2, -4.6), (10.8, 1.8, 9.2), "boulder")
    _crystal(m, "boulder", "boulder_crystal_0", (3.4, -5.0, -2.0), (-20.0, 0.0, 30.0), 4.2, 2.0)
    _crystal(m, "boulder", "boulder_crystal_1", (-4.0, -4.8, 2.4), (24.0, 0.0, -26.0), 3.2, 1.6)

    # ------------------------------------------------------------------ Kopf
    m.bone("head", "body", pivot=(0.0, -22.6, -6.6), rotation=(-10.0, 0.0, 0.0))
    m.cube("head", (-4.8, -7.6, -5.4), (9.6, 8.2, 8.4), "stone")
    m.cube("head", (-5.6, -6.8, -7.0), (11.2, 3.0, 3.2), "stone_rough")               # Stirnwulst
    m.cube("head", (-1.1, -4.4, -7.2), (2.2, 2.8, 1.4), "stone")                      # Nasengrat
    m.cube("head", (-4.0, -3.7, -6.2), (2.8, 1.3, 0.8), "eye")
    m.cube("head", (1.2, -3.7, -6.2), (2.8, 1.3, 0.8), "eye")
    m.cube("head", (-1.3, -8.9, -6.1), (2.6, 2.4, 1.2), "gem")                        # Mondstein auf der Stirn
    _crystal(m, "head", "head_crystal_0", (-2.2, -7.2, 0.6), (-30.0, 0.0, -18.0), 4.4, 1.8)
    _crystal(m, "head", "head_crystal_1", (2.0, -7.2, 1.4), (-38.0, 0.0, 20.0), 3.4, 1.5)
    m.bone("jaw", "head", pivot=(0.0, -1.0, -2.6), rotation=(4.0, 0.0, 0.0))
    m.cube("jaw", (-4.2, -0.4, -4.6), (8.4, 3.2, 7.6), "stone_rough")
    m.cube("jaw", (-3.4, -0.9, -4.9), (6.8, 0.8, 0.8), "teeth")
    m.bone("armor_head", "head", pivot=(0.0, -7.6, -1.0))
    for i, (x, height, rz) in enumerate(((-3.6, 3.4, -24.0), (-1.2, 4.6, -8.0), (1.2, 4.6, 8.0), (3.6, 3.4, 24.0))):
        _crystal(m, "armor_head", f"armor_head_{i}", (x, 0.2, -3.4), (-12.0, 0.0, rz), height, 1.6)

    # ------------------------------------------------------------------ Arme
    for side, sx in (("left", 1), ("right", -1)):
        arm = f"{side}_arm"
        m.bone(arm, "body", pivot=(12.6 * sx, -19.6, 0.4), rotation=(0.0, 0.0, -9.0 * sx))
        m.cube(arm, (-5.0 if sx > 0 else -6.2, -6.0, -6.0), (11.2, 9.6, 12.0), "stone_rough")   # Schulterfels
        m.cube(arm, (-3.2, 2.6, -3.3), (6.4, 9.8, 6.6), "stone")                       # Oberarm
        _cluster(m, arm, f"{side}_shoulder_crystal", (1.4 * sx, -5.6, 0.2), [
            (0.0, 0.0, 6.4, 2.6, -8.0, 22.0 * sx), (1.6 * sx, 2.4, 4.6, 2.2, -32.0, 38.0 * sx), (-1.0 * sx, -2.6, 4.0, 2.0, 18.0, 14.0 * sx),
            (2.8 * sx, -1.2, 3.2, 1.6, 4.0, 56.0 * sx)])
        m.bone(f"vine_{side}", arm, pivot=(4.4 * sx, 3.2, -3.0))
        m.cube(f"vine_{side}", (-0.4, 0.0, -0.4), (0.8, 6.4, 0.8), "vine")
        fore = f"{side}_forearm"
        m.bone(fore, arm, pivot=(0.0, 11.8, 0.2), rotation=(-14.0, 0.0, 0.0))
        m.cube(fore, (-4.2, -0.8, -4.3), (8.4, 10.8, 8.6), "stone")
        m.cube(fore, (-4.6, 2.2, -4.7), (9.2, 1.8, 9.4), "inlay")                      # Mondsteinbänder
        m.cube(fore, (-4.6, 6.0, -4.7), (9.2, 1.4, 9.4), "inlay")
        m.cube(fore, (-4.9, 9.6, -5.0), (9.8, 6.8, 10.0), "stone_rough")               # Faust
        for k in range(4):
            m.cube(fore, (-4.4 + k * 2.3, 13.6, -5.9), (2.0, 2.4, 1.4), "stone_rough")  # Knöchel
        armor = f"armor_{side}_arm"
        m.bone(armor, fore, pivot=(0.0, 4.0, 0.0))
        m.cube(armor, (-5.0 if sx > 0 else -5.4, -3.0, -5.0), (10.4, 5.0, 10.0), "crystal_plate")
        _crystal(m, armor, f"{armor}_0", (4.6 * sx, -1.0, -1.6), (0.0, 0.0, 70.0 * sx), 4.8, 2.2)
        _crystal(m, armor, f"{armor}_1", (4.4 * sx, 1.0, 2.2), (14.0, 0.0, 78.0 * sx), 3.6, 1.8)
        _crystal(m, armor, f"{armor}_2", (0.0, -1.4, 5.0), (-70.0, 0.0, 0.0), 3.6, 1.8)
    return m


# =============================================================================================
# Bemalen
# =============================================================================================

def _noise(ctx: Ctx, seed: int = 0) -> float:
    return hash01(ctx.u, ctx.v, seed)


def _moss_amount(ctx: Ctx) -> float:
    """Wie viel Feenmoos an dieser Stelle wächst: auf Oberseiten viel, oben an den Seiten etwas, sonst keins."""
    x, y, z = ctx.world
    n = noise3(x * 0.32, y * 0.32, z * 0.32, 17)
    if ctx.face == "top":
        return 0.45 + 0.7 * n
    if ctx.face == "bottom":
        return 0.0
    return max(0.0, (0.22 - rel_y(ctx)) * 2.6) * (0.4 + 0.8 * n) + (0.5 if n > 0.82 else 0.0) * 0.4


def _moss(ctx: Ctx, base):
    """Feenmoos mit leuchtenden Sporen über dem Stein."""
    amount = _moss_amount(ctx)
    if amount < 0.5:
        return base
    v = 0.3 + 0.45 * _noise(ctx, 19) + min(0.25, (amount - 0.5) * 0.5)
    col = pick(ctx.pal["moss"], v)
    if amount < 0.6:
        col = mix(base, pick(ctx.pal["moss"], 0.2), 0.6)       # dünner Rand
    if amount > 0.75 and _noise(ctx, 23) > 0.975:
        col = pick(ctx.pal["spore"], _noise(ctx, 29))
        ctx.glow = col
    return col


def _ashlar(ctx: Ctx):
    """Gefügte Feensteinquader: Reihen mit versetzten Fugen, jeder Quader im eigenen Ton, feine Risse und Körnung."""
    x, y, z = ctx.world
    a = x if ctx.face in ("front", "back", "top", "bottom") else z
    row = math.floor(y / ROW)
    shift = (row % 2) * BLOCK * 0.5
    col_i = math.floor((a + shift) / BLOCK)
    fy = (y / ROW) % 1.0
    fa = ((a + shift) / BLOCK) % 1.0
    tone = hash01(row, col_i, 31)
    v = 0.56 + 0.2 * tone + (_noise(ctx, 37) - 0.5) * 0.045
    v += 0.06 * (1.0 - rel_y(ctx)) - 0.06
    if ctx.face == "top":
        v += 0.08
    joint = ctx.face != "top" and (fy < 0.12 or fa < 0.08)
    if joint:
        return pick(ctx.pal["mortar"], 0.4 + 0.4 * _noise(ctx, 41))
    # Kante des Quaders: oben/links ein heller Grat, unten dunkler
    if fy < 0.24:
        v += 0.07
    elif fy > 0.86:
        v -= 0.06
    crack = abs(noise3(x * 0.45, y * 0.8, z * 0.45, 43) - 0.5) < 0.016
    if crack:
        v -= 0.24
    return pick(ctx.pal["stone"], v)


def _stone(ctx: Ctx):
    return _moss(ctx, _ashlar(ctx))


_VOR = Voronoi3D(3.2, 47)


def _stone_rough(ctx: Ctx):
    """Grober Fels (Schulterfelsen, Fäuste, Buckel, Füße): Brocken mit dunklen Spalten."""
    x, y, z = ctx.world
    cell, edge = _VOR(x, y, z)
    v = 0.5 + 0.24 * cell + 0.08 * (1.0 - rel_y(ctx)) + (_noise(ctx, 53) - 0.5) * 0.05
    if edge < 0.18:
        v -= 0.3 * (1.0 - edge / 0.18)
    if ctx.face == "top":
        v += 0.07
    return _moss(ctx, pick(ctx.pal["stone"], v))


def _boulder(ctx: Ctx):
    """Der herausgerissene Felsbrocken: roher, dunkler Feenstein (nicht behauen wie der Hüter selbst) mit Rissen und
    Sprenkeln aus Mondstein, die leuchten; unten Erde mit Wurzeln, wo er aus dem Boden gerissen wurde, oben Moos."""
    x, y, z = ctx.world
    cell, edge = _VOR(x * 1.2, y * 1.2, z * 1.2)
    v = 0.16 + 0.24 * cell + (_noise(ctx, 89) - 0.5) * 0.08
    if edge < 0.16:
        v -= 0.14
    col = pick(ctx.pal["stone"], v)
    n = _noise(ctx, 91)
    if n > 0.955 and edge > 0.2:
        col = pick(ctx.pal["moon"], 0.5 + 0.4 * _noise(ctx, 93))         # Sprenkel aus Mondstein
        ctx.glow = col
        return col
    low = rel_y(ctx)
    if low > 0.62 or ctx.face == "bottom":
        earth = hexc("#4e3a2c") if _noise(ctx, 95) > 0.35 else hexc("#3a2a22")
        col = mix(col, earth, 0.55 + 0.4 * min(1.0, (low - 0.62) * 3.0 if ctx.face != "bottom" else 1.0))
        if _noise(ctx, 99) > 0.88:
            col = hexc("#6a5038")                                            # Wurzeln
        return col
    return _moss(ctx, col)


def _rune_plate(ctx: Ctx):
    """Die Runenplatte: glatt behauener Stein, darin die Zeichen des alten Volks – eine Mondsichel über drei Strichen
    und zwei Säulen aus Kerben; die Rillen leuchten."""
    base = pick(ctx.pal["stone"], 0.62 + (_noise(ctx, 59) - 0.5) * 0.06 - 0.1 * rel_y(ctx))
    if ctx.face != "front":
        return base
    w, h = ctx.w, ctx.h
    if ctx.s == 0 or ctx.t == 0 or ctx.s == w - 1 or ctx.t == h - 1:
        return pick(ctx.pal["stone"], 0.74 if (ctx.s == 0 or ctx.t == 0) else 0.42)
    u = (ctx.s + 0.5) / w - 0.5          # -0.5 .. 0.5 (links .. rechts im Bild)
    v = (ctx.t + 0.5) / h                # 0 oben .. 1 unten
    lit = False
    # Säulen aus Kerben links und rechts
    if abs(abs(u) - 0.37) < 0.07 and 0.14 < v < 0.9 and int(v * 7) % 2 == 0:
        lit = True
    # Zackenband oben: ein Zickzack über die ganze Breite
    zig = 0.1 + 0.05 * abs(((u + 0.5) * 8.0) % 2.0 - 1.0)
    if abs(v - zig) < 0.045 and abs(u) < 0.44:
        lit = True
    if lit:
        col = pick(ctx.pal["moon"], 0.55 + 0.3 * (1.0 - v))
        ctx.glow = col
        return col
    return base


def _inlay(ctx: Ctx):
    """Eingelegtes Mondsteinband: milchig, mit einer leuchtenden Mittellinie und hellen Flecken."""
    ry = rel_y(ctx)
    col = pick(ctx.pal["gem"], 0.45 + 0.4 * (1.0 - ry) + (_noise(ctx, 61) - 0.5) * 0.1)
    if ctx.face in ("top", "bottom"):
        return col
    if abs(ry - 0.5) < 0.22 or _noise(ctx, 67) > 0.9:
        g = pick(ctx.pal["moon"], 0.55 + 0.3 * _noise(ctx, 71))
        ctx.glow = g
        return mix(col, g, 0.6)
    return col


def _core(ctx: Ctx):
    """Das Hüterherz: ein großer Mondstein – innen weiß, nach außen tiefblau; leuchtet ganz."""
    cx = (ctx.s + 0.5) / ctx.w - 0.5
    cy = (ctx.t + 0.5) / ctx.h - 0.5
    d = min(1.0, math.hypot(cx, cy) * 2.0)
    col = pick(ctx.pal["moon"], 0.95 - 0.75 * d + (_noise(ctx, 73) - 0.5) * 0.06)
    ctx.glow = col
    return col


def _eye(ctx: Ctx):
    col = pick(ctx.pal["moon"], 0.9 if ctx.t == 0 else 0.7)
    ctx.glow = col
    return col


def _gem(ctx: Ctx):
    v = 0.62 + 0.3 * (1.0 - rel_y(ctx))
    if ctx.s == 0 and ctx.t == 0:
        v = 1.0
    col = pick(ctx.pal["gem"], v)
    if abs(rel_y(ctx) - 0.5) < 0.2:
        col = mix(col, pick(ctx.pal["moon"], 0.7), 0.6)
        ctx.glow = col
    return col


def _seg_t(ctx: Ctx) -> float:
    tag = ctx.cube.tag or ""
    parts = tag.split(":")
    if len(parts) != 3:
        return 0.5
    a, b = float(parts[1]), float(parts[2])
    oy, hgt = ctx.cube.origin[1], ctx.cube.size[1]
    tl = (oy + hgt - ctx.local[1]) / hgt if hgt > 0 else 0.0
    return a + (b - a) * max(0.0, min(1.0, tl))


def _crystal_paint(ctx: Ctx):
    """Mondsteinkristall: Facetten in schrägen Bändern, zur Spitze heller; die obere Hälfte leuchtet."""
    t = _seg_t(ctx)
    facet = ((ctx.s + ctx.t) // 2) % 3
    v = 0.25 + 0.6 * t + (0.12 if facet == 0 else -0.05 if facet == 1 else 0.0)
    if ctx.face in ("west", "front"):
        v += 0.07
    col = pick(ctx.pal["crystal"], v)
    if t > 0.45:
        g = pick(ctx.pal["moon"], 0.35 + 0.65 * (t - 0.45) / 0.55)
        ctx.glow = g
        col = mix(col, g, 0.55)
    return col


def _crystal_plate(ctx: Ctx):
    """Kristallplatten des Panzers: kantige Splitter, hell gerandet, innen tiefblau schimmernd (leuchtet schwach)."""
    x, y, z = ctx.world
    cell, edge = _VOR(x * 1.6, y * 1.6, z * 1.6)
    v = 0.35 + 0.4 * cell + (0.25 if edge < 0.2 else 0.0)
    col = pick(ctx.pal["crystal"], v)
    if edge < 0.2 or cell > 0.7:
        g = pick(ctx.pal["moon"], 0.5 + 0.4 * cell)
        ctx.glow = g
        col = mix(col, g, 0.5)
    return col


def _vine(ctx: Ctx):
    """Leuchtranke: dunkler Trieb, unten eine leuchtende Blüte."""
    ry = rel_y(ctx)
    if ry > 0.82:
        col = pick(ctx.pal["bloom"], 0.2 + 0.7 * _noise(ctx, 79))
        ctx.glow = col
        return col
    return pick(ctx.pal["vine"], 0.3 + 0.5 * _noise(ctx, 83))


def _teeth(ctx: Ctx):
    return pick(ctx.pal["stone"], 0.82 if ctx.s % 2 == 0 else 0.45)


def materials(variant: str) -> dict:
    return {"stone": _stone, "stone_rough": _stone_rough, "boulder": _boulder, "rune_plate": _rune_plate, "inlay": _inlay, "core": _core, "eye": _eye,
            "gem": _gem, "crystal": _crystal_paint, "crystal_plate": _crystal_plate, "vine": _vine, "teeth": _teeth}
