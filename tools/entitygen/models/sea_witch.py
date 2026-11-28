"""
Die Meerhexe (seit 0.97): eine verstoßene Meerjungfrau, die in einer Grotte am Grund des Urmeers haust und dort ihre
Zauber braut. Oben eine hagere Frau mit fahler, graugrüner Haut, schmalem Gesicht, leuchtenden Augen mit schmalen Pupillen
und dunklen Lippen; eine Krone aus schwarzer Koralle mit einem leuchtenden Aquamarin; wildes Haar aus Tang, dessen
Strähnen im Wasser nach oben treiben und an den Spitzen leuchten; ein zerschlissener Umhang aus Tang, eine Kette aus
Haizähnen, Armreife aus Knochen, lange Krallen. Unten ein langer Aalschwanz in fünf Gliedern, gebändert wie ein
Feuerfisch, mit leuchtenden Punkten, giftigen Stacheln auf dem Rücken, zwei großen gefächerten Flossen an den Hüften und
einer zerrissenen Schwanzflosse. In der rechten Hand ihr Hexenstab aus Treibholz: oben halten drei Korallenzinken eine
leuchtende Kugel, darunter hängen Muscheln und Zähne an Schnüren.

Aufrecht (wassertretend) steht sie im Modell; beim Schwimmen kippt das Java-Modell den Knochen "swim" nach vorn. Im Spiel
1,5-fach vergrößert.
"""
from __future__ import annotations

import math

from ..geometry import Model
from ..paint import Ctx, hash01, mix, noise3, pick, ramp, rel_y

HIP_Y = 6.0

PALETTES = {
    "witch": {
        "skin": ramp("#3a5c54", "#e4f4ea", steps=7, hue_shift=-0.02),
        "hair": ramp("#04200f", "#58b070", steps=7),
        "hair_glow": ramp("#3affb0", "#e0fff4", steps=4),
        "tail": ramp("#03121c", "#2f6f80", steps=8),
        "band": ramp("#6a5a4c", "#f0e2c8", steps=6),
        "fin": ramp("#3a0a16", "#f09a84", steps=7),
        "spine": ramp("#1e080c", "#f4dcc8", steps=6),
        "spot": ramp("#2ad8c8", "#e8fffc", steps=4),
        "kelp": ramp("#06200f", "#5a9a52", steps=7),
        "coral": ramp("#060306", "#4e3448", steps=6),
        "polyp": ramp("#b03a8a", "#ffd0f0", steps=4),
        "orb": ramp("#0a4a6e", "#effffe", steps=7),
        "wood": ramp("#241a14", "#a89478", steps=7),
        "bone": ramp("#5e5040", "#f6eedc", steps=6),
        "shell": ramp("#5a3a4a", "#f4dcd8", steps=6),
        "lip": ramp("#140812", "#5a2a4a", steps=5),
        "lash": ramp("#05060a", "#2a2a3a", steps=4),
        "iris": ramp("#0a7a6a", "#d8fff0", steps=5),
        "white": ramp("#7a8c84", "#dceae2", steps=4),
    }
}

_LIT = {"top": 0.08, "front": 0.03, "west": 0.0, "east": -0.03, "back": -0.06, "bottom": -0.14}


def build() -> Model:
    m = Model("sea_witch", 192, 192)
    m.tex_res = 2
    m.bone("swim", pivot=(0, HIP_Y, 0))

    # ------------------------------------------------------------------ Oberkörper (hager)
    m.bone("torso", "swim", pivot=(0, 0, 0))
    m.cube("torso", (-3.0, -9.5, -1.7), (6.0, 5.0, 3.4), "chest")
    m.cube("torso", (-2.2, -4.5, -1.3), (4.4, 4.5, 2.6), "belly")
    # der Umhang aus Tang: über den Schultern, vorn zwei Bahnen, hinten bis an die Hüften (in zwei Gliedern)
    m.bone("shawl", "torso", pivot=(0, 0, 0))
    m.cube("shawl", (-3.7, -10.1, -2.25), (7.4, 2.3, 4.5), "shawl")
    for side, sx in (("l", 1), ("r", -1)):
        strip = f"shawl_{side}"
        m.bone(strip, "shawl", pivot=(2.3 * sx, -8.0, -2.15), rotation=(-4, 0, 3 * sx))
        m.cube(strip, (-0.95, 0, -0.3), (1.9, 7.0, 0.5), "kelp_strip")
    m.bone("cape", "shawl", pivot=(0, -9.4, 2.15), rotation=(6, 0, 0))
    m.cube("cape", (-3.6, 0, -0.3), (7.2, 8.0, 0.6), "kelp_cape")
    m.bone("cape_tip", "cape", pivot=(0, 8.0, 0), rotation=(8, 0, 0))
    m.cube("cape_tip", (-3.4, 0, -0.25), (6.8, 7.0, 0.5), "kelp_cape_tip")
    # Kette aus Haizähnen, ein großer Zahn
    m.cube("torso", (-2.3, -9.65, -1.95), (4.6, 0.6, 0.3), "teeth")
    m.cube("torso", (-0.5, -9.3, -2.15), (1.0, 1.7, 0.5), "tooth")

    # ------------------------------------------------------------------ Kopf
    m.bone("head", "torso", pivot=(0, -9.5, 0))
    m.cube("head", (-3.2, -6.8, -3.2), (6.4, 6.8, 6.4), "face")
    m.cube("head", (-3.45, -7.3, -3.4), (6.9, 1.3, 6.9), "hair")                     # Scheitel
    m.cube("head", (-3.5, -6.4, -2.3), (0.6, 5.2, 5.6), "hair_side")                 # Haar über den Schläfen
    m.cube("head", (2.9, -6.4, -2.3), (0.6, 5.2, 5.6), "hair_side")
    for side, sx in (("l", 1), ("r", -1)):
        # spitze, zerrissene Flossenohren
        ear = f"ear_{side}"
        m.bone(ear, "head", pivot=(3.1 * sx, -3.8, 0.2), rotation=(0, 34 * sx, 0))
        m.cube(ear, (0, -2.2, -0.2), (0, 4.0, 3.6), "ear_fin")
        # lange Strähnen vor den Schultern
        lock = f"locks_{side}"
        m.bone(lock, "head", pivot=(3.0 * sx, -5.6, -1.4), rotation=(-6, 0, -6 * sx))
        m.cube(lock, (-0.55, 0, -0.9), (1.1, 10.0, 1.8), "lock")
    # langes Haar über den Rücken
    m.bone("hair_back", "head", pivot=(0, -6.2, 2.9), rotation=(10, 0, 0))
    m.cube("hair_back", (-3.4, 0, -0.5), (6.8, 9.5, 1.3), "hair_back")
    m.bone("hair_tip", "hair_back", pivot=(0, 9.5, 0), rotation=(12, 0, 0))
    m.cube("hair_tip", (-3.0, 0, -0.4), (6.0, 8.0, 1.0), "hair_tip")
    # wilde Strähnen, die nach oben treiben (je zwei Glieder, die Spitzen leuchten)
    for i, (x, z, rx, rz, length) in enumerate(((-2.4, 1.6, -28, -38, 7.5), (-0.9, 2.4, -38, -12, 8.5), (0.9, 2.3, -36, 14, 8.0),
                                                (2.5, 1.4, -26, 40, 7.0), (0.0, 0.4, -12, 4, 6.0))):
        s = f"strand_{i}"
        m.bone(s, "head", pivot=(x, -7.0, z), rotation=(rx, 0, rz))
        m.cube(s, (-0.5, -length, -0.5), (1.0, length, 1.0), "strand")
        tip = f"strand_{i}_tip"
        m.bone(tip, s, pivot=(0, -length + 0.2, 0), rotation=(-18 if i % 2 else 16, 0, 12 if i < 2 else -12))
        m.cube(tip, (-0.4, -length * 0.8, -0.4), (0.8, length * 0.8, 0.8), "strand_tip")
    # die Krone aus schwarzer Koralle: Zacken im Kreis, vorn ein leuchtender Aquamarin
    m.bone("crown", "head", pivot=(0, -7.2, 0))
    spikes = ((-2.9, -2.4, -28, -22, 2.6), (-1.5, -3.2, -26, -10, 3.6), (0.0, -3.4, -24, 0, 4.6), (1.5, -3.2, -26, 10, 3.6),
              (2.9, -2.4, -28, 22, 2.6), (-3.1, 0.2, 0, -30, 2.2), (3.1, 0.2, 0, 30, 2.2))
    for i, (x, z, rx, rz, h) in enumerate(spikes):
        b = f"crown_{i}"
        m.bone(b, "crown", pivot=(x, 0, z), rotation=(rx, 0, rz))
        m.cube(b, (-0.45, -h, -0.45), (0.9, h, 0.9), "black_coral")
        m.cube(b, (-0.95 if i % 2 else 0.15, -h * 0.6, -0.35), (0.8, 0.6, 0.7), "black_coral")
    m.cube("crown", (-3.5, -0.4, -3.45), (7.0, 0.8, 0.5), "black_coral")
    m.cube("crown", (-0.7, -1.3, -3.85), (1.4, 1.4, 0.6), "gem")

    # ------------------------------------------------------------------ Arme (lang, hager, mit Krallen)
    for side, sx in (("l", 1), ("r", -1)):
        arm = "left_arm" if sx > 0 else "right_arm"
        m.bone(arm, "torso", pivot=(3.6 * sx, -8.9, 0))
        m.cube(arm, (-0.8, -0.6, -0.8), (1.6, 8.6, 1.6), "arm")
        m.cube(arm, (-0.9, 7.8, -1.0), (1.8, 1.6, 2.0), "hand")
        for i, cz in enumerate((-0.9, -0.15, 0.6)):
            m.cube(arm, (-0.25 + (0.35 * sx if i == 1 else 0.0), 9.3, cz), (0.5, 1.6, 0.45), "claw")
        m.cube(arm, (0.8 if sx > 0 else -0.8, 2.6, -0.2), (0, 4.4, 2.6), "arm_fin")      # zerrissene Flosse am Unterarm
        m.cube(arm, (-0.95, 5.4, -0.95), (1.9, 0.8, 1.9), "bone")                        # Armreif aus Knochen

    # ------------------------------------------------------------------ der Hexenstab (rechte Hand)
    m.bone("staff", "right_arm", pivot=(0, 8.6, -0.1))
    m.cube("staff", (-0.45, -18.0, -0.45), (0.9, 29.0, 0.9), "wood")
    m.cube("staff", (-0.6, -8.0, -0.6), (1.2, 1.4, 1.2), "wood_knot")                # ein Knorren
    m.cube("staff", (-0.6, 2.0, -0.6), (1.2, 1.0, 1.2), "wood_knot")
    m.cube("staff", (-1.7, -22.2, -1.7), (3.4, 3.4, 3.4), "orb")                      # die leuchtende Kugel
    for i, (x, z, rz, rx) in enumerate(((-1.5, 0.0, -22, 0), (1.5, 0.0, 22, 0), (0.0, 1.5, 0, 22))):
        prong = f"staff_prong_{i}"
        m.bone(prong, "staff", pivot=(x * 0.5, -17.8, z * 0.5), rotation=(rx, 0, rz))
        m.cube(prong, (-0.35, -5.6, -0.35), (0.7, 5.6, 0.7), "black_coral")
    # Schnüre mit Muscheln und Zähnen
    for i, (x, z, length, charm) in enumerate(((0.5, -0.5, 3.2, "shell"), (-0.5, 0.4, 4.2, "tooth_charm"), (0.4, 0.5, 2.4, "shell"))):
        cord = f"staff_charm_{i}"
        m.bone(cord, "staff", pivot=(x, -17.2, z), rotation=(0, 0, 8 if i % 2 else -8))
        m.cube(cord, (-0.12, 0, -0.12), (0.24, length, 0.24), "cord")
        m.cube(cord, (-0.55, length, -0.4), (1.1, 1.0, 0.8), charm)

    # ------------------------------------------------------------------ der Aalschwanz
    m.bone("tail1", "swim", pivot=(0, 0, 0))
    m.cube("tail1", (-2.5, 0, -1.5), (5.0, 4.0, 3.0), "hips")
    for side, sx in (("l", 1), ("r", -1)):
        fan = f"fan_{side}"
        m.bone(fan, "tail1", pivot=(2.4 * sx, 1.0, 0.4), rotation=(0, 16 * sx, -34 * sx))
        m.cube(fan, (0, -1.0, -1.6), (0, 11.0, 8.0), "fan")
    sizes = ((4.2, 4.4, 2.6, 8), (3.6, 4.2, 2.2, 10), (3.0, 4.0, 1.9, 10), (2.4, 3.8, 1.6, 12))
    parent = "tail1"
    py = 4.0
    for i, (w, h, d, rot) in enumerate(sizes):
        b = f"tail{i + 2}"
        m.bone(b, parent, pivot=(0, py, 0), rotation=(rot, 0, 0))
        m.cube(b, (-w / 2, 0, -d / 2), (w, h, d), "scales")
        if i < 3:
            # giftige Stacheln auf dem Rücken, dazwischen Haut
            spine = f"spines_{i}"
            m.bone(spine, b, pivot=(0, 0.4, d / 2 - 0.1), rotation=(-58 + 6 * i, 0, 0))
            m.cube(spine, (0, -(5.2 - 0.8 * i), 0), (0, 5.2 - 0.8 * i, 3.2), "spine_fin")
        parent = b
        py = h
    m.bone("fluke", "tail5", pivot=(0, 3.4, 0.05), rotation=(8, 0, 0))
    m.cube("fluke", (-0.9, -0.4, -0.6), (1.8, 1.6, 1.2), "scales")
    for side, sx in (("l", 1), ("r", -1)):
        lobe = f"fluke_{side}"
        m.bone(lobe, "fluke", pivot=(0.4 * sx, 0.2, 0), rotation=(0, 0, 30 * sx))
        m.cube(lobe, (0 if sx > 0 else -8.0, 0, 0), (8.0, 6.4, 0), "fluke")
    return m


# =============================================================================================
# Bemalen
# =============================================================================================

def _skin(ctx: Ctx, base: float = 0.58):
    x, y, z = ctx.world
    n = noise3(x * 0.8, y * 0.8, z * 0.8, 1931)
    return pick(ctx.pal["skin"], base + (n - 0.5) * 0.09 + _LIT[ctx.face])


def _chest(ctx: Ctx):
    if ctx.face == "front":
        if ctx.t == 0:
            return _skin(ctx, 0.64)
        # Rippen (hager)
        if ctx.t in (4, 6, 8) and 1 <= ctx.s <= ctx.w - 2 and abs(ctx.s - (ctx.w - 1) / 2.0) > 1.2:
            return _skin(ctx, 0.46)
    if ctx.face in ("west", "east") and ctx.t <= 1:
        return _skin(ctx, 0.64)
    return _skin(ctx)


def _belly(ctx: Ctx):
    """Bauch und Taille: Haut, unten gehen die gebänderten Schuppen in einem V nach oben über."""
    if ctx.face == "bottom":
        return _scale_color(ctx, 0.2)
    rows = ctx.h
    if ctx.face in ("front", "back"):
        center = abs(ctx.s - (ctx.w - 1) / 2.0) / max(1.0, (ctx.w - 1) / 2.0)
        edge = rows - 3 + int(round(center * 2.0)) - 2
    else:
        edge = rows - 4
    if ctx.t >= edge:
        return _scales(ctx)
    if ctx.t == edge - 1 and hash01(ctx.u, ctx.v, 1932) > 0.4:
        return mix(_skin(ctx), _scale_color(ctx, 0.7), 0.5)
    return _skin(ctx, 0.55)


def _face(ctx: Ctx):
    """Das Gesicht: schmale, leuchtende Augen mit dunklen Wimpern, scharfe Brauen, eingefallene Wangen, dunkle Lippen."""
    face, s, t, w = ctx.face, ctx.s, ctx.t, ctx.w
    if face == "front":
        m = min(s, w - 1 - s)
        if t <= 2:
            return _hair(ctx)
        if t == 4 and m in (2, 3, 4, 5):
            return pick(ctx.pal["hair"], 0.12) if m != 5 else _skin(ctx, 0.5)  # scharfe Brauen (nach außen ansteigend)
        if t == 3 and m == 2:
            return pick(ctx.pal["hair"], 0.12)
        if t == 5 and m in (2, 3, 4, 5):
            return pick(ctx.pal["lash"], 0.1)                               # dunkler Lidstrich
        if t == 6 and m in (2, 3, 4, 5):
            col = pick(ctx.pal["iris"], 0.95) if m in (3, 4) else pick(ctx.pal["iris"], 0.55)
            if m == 4:
                col = pick(ctx.pal["lash"], 0.05)                           # schmale Pupille
            ctx.glow = col if m != 4 else None
            return col
        if t == 7 and m in (2, 3, 4, 5):
            return _skin(ctx, 0.4)                                          # Schatten unter den Augen
        if t in (9, 10) and m in (1, 2):
            return _skin(ctx, 0.44)                                         # eingefallene Wangen
        if t == 9 and m == 6:
            return _skin(ctx, 0.5)                                          # Nasenschatten
        if t == 11 and m in (5, 6):
            return pick(ctx.pal["lip"], 0.6 if m == 6 else 0.35)            # dunkle Lippen
        if t == 12 and m == 6:
            return pick(ctx.pal["lip"], 0.85)
        if t >= 12 and m <= 1:
            return _skin(ctx, 0.48)
        return _skin(ctx, 0.62 - (0.05 if m == 0 else 0.0))
    if face in ("west", "east"):
        if t <= 3 or ctx.s >= ctx.w // 2:
            return _hair(ctx)
        return _skin(ctx, 0.52)
    if face in ("top", "back"):
        return _hair(ctx)
    return _skin(ctx, 0.48)


def _hair(ctx: Ctx, extra: float = 0.0):
    """Tanghaar: dunkelgrüne Bänder mit hellen Rippen."""
    strand = hash01(ctx.s // 2, ctx.face, ctx.cube.origin, 1933)
    v = 0.42 + strand * 0.22 + _LIT[ctx.face] - rel_y(ctx) * 0.08 + extra
    if hash01(ctx.u, ctx.v, 1934) > 0.92:
        v += 0.2
    return pick(ctx.pal["hair"], v)


def _hair_side(ctx: Ctx):
    if ctx.face in ("west", "east") and ctx.t >= ctx.h - 2 and ctx.s % 2 == 0:
        return None
    return _hair(ctx)


def _lock(ctx: Ctx):
    if ctx.t >= ctx.h - 2 and (ctx.s + ctx.t) % 2 == 1:
        return None
    r = rel_y(ctx)
    col = _hair(ctx, 0.03)
    if r > 0.82:
        col = pick(ctx.pal["hair_glow"], 0.4 + (r - 0.82) * 3.0)
        ctx.glow = col
    return col


def _hair_back(ctx: Ctx):
    if ctx.face == "front":
        return pick(ctx.pal["hair"], 0.22)
    return _hair(ctx)


def _hair_tip(ctx: Ctx):
    r = rel_y(ctx)
    if ctx.face in ("back", "front", "west", "east") and ctx.t >= ctx.h - 4:
        if hash01(ctx.s, ctx.cube.origin, 1935) < (ctx.t - (ctx.h - 5)) * 0.28:
            return None
    col = _hair(ctx, 0.04)
    if r > 0.78 and hash01(ctx.u, ctx.v, 1936) > 0.5:
        col = pick(ctx.pal["hair_glow"], 0.5)
        ctx.glow = col
    return col


def _strand(ctx: Ctx):
    return _hair(ctx, 0.05 + (1.0 - rel_y(ctx)) * 0.05)


def _strand_tip(ctx: Ctx):
    """Die Spitze einer treibenden Strähne: zum Ende heller, ganz außen leuchtend."""
    up = 1.0 - rel_y(ctx)                                                   # 1 an der Spitze (oben)
    if up > 0.55:
        col = pick(ctx.pal["hair_glow"], 0.3 + (up - 0.55) * 1.6)
        ctx.glow = col
        return col
    return _hair(ctx, 0.08 + up * 0.2)


def _fin_paint(ctx: Ctx, along_axis: int, rays_axis: int, ragged: float, start: float = 0.0, bands: bool = True):
    """Flosse eines Feuerfischs: Strahlen, quer dazu Bänder aus Rot und Creme; der Rand zerrissen (durchsichtig)."""
    lo = ctx.cube.origin
    size = ctx.cube.size
    a = (ctx.local[along_axis] - lo[along_axis]) / max(0.01, size[along_axis])
    b = (ctx.local[rays_axis] - lo[rays_axis]) / max(0.01, size[rays_axis])
    wave = math.sin(b * 11.0) * 0.08
    if a > 0.8 + wave and hash01(ctx.u, ctx.v, 1938) < ragged + (a - 0.8) * 3.2:
        return None
    ray = (int(b * 8.0 + a * 1.2) % 2) == 0
    if ray:
        return pick(ctx.pal["spine"], 0.25 + a * 0.6)
    band = bands and int(a * 6.0 + b * 0.8) % 2 == 0
    v = 0.3 + a * 0.45 + start
    return pick(ctx.pal["band"] if band else ctx.pal["fin"], v + (0.15 if band else 0.0))


def _ear_fin(ctx: Ctx):
    return _fin_paint(ctx, along_axis=2, rays_axis=1, ragged=0.45, bands=False)


def _arm_fin(ctx: Ctx):
    return _fin_paint(ctx, along_axis=2, rays_axis=1, ragged=0.42)


def _spine_fin(ctx: Ctx):
    """Die Rückenstacheln: lange, gebänderte Stacheln mit Haut dazwischen, die Spitzen frei."""
    return _fin_paint(ctx, along_axis=1, rays_axis=2, ragged=0.5) if ctx.local[1] > ctx.cube.origin[1] else None


def _fan(ctx: Ctx):
    """Die gefächerten Hüftflossen: Strahlen fächern vom Ansatz, rot und creme gebändert, außen zerrissen."""
    lo = ctx.cube.origin
    size = ctx.cube.size
    a = (ctx.local[1] - lo[1]) / max(0.01, size[1])                         # entlang (nach unten)
    b = (ctx.local[2] - lo[2]) / max(0.01, size[2])                         # quer
    ang = math.atan2(b - 0.2, a + 0.05)
    r = math.hypot(a, b - 0.2)
    if r > 0.95 + math.sin(ang * 9.0) * 0.06 or (r > 0.8 and hash01(ctx.u, ctx.v, 1939) < (r - 0.8) * 3.5):
        return None
    if int(ang * 7.0) % 2 == 0 and r > 0.12:
        return pick(ctx.pal["spine"], 0.3 + r * 0.55)
    band = int(r * 5.5) % 2 == 0
    return pick(ctx.pal["band"] if band else ctx.pal["fin"], 0.35 + r * 0.4 + (0.12 if band else 0.0))


def _scale_color(ctx: Ctx, v: float):
    y = ctx.world[1]
    k = min(1.0, max(0.0, (y - HIP_Y) / 20.0))
    return pick(ctx.pal["tail"], v * 0.72 + 0.12 + k * 0.1)


def _scales(ctx: Ctx):
    """Aalhaut mit Feuerfischbändern: dunkles Meerblau, helle Bänder quer, dazu leuchtende Punkte."""
    x, y, z = ctx.world
    band = (y * 0.55 + math.sin(x * 0.9 + z * 0.4) * 0.35) % 2.0
    v = 0.5 + _LIT[ctx.face] + (hash01(ctx.u, ctx.v, 1940) - 0.5) * 0.06
    if ctx.face == "front":
        v += 0.1
    if band < 0.55:
        return pick(ctx.pal["band"], 0.45 + v * 0.45)
    col = _scale_color(ctx, v)
    if ctx.face in ("west", "east", "back") and hash01(int(y * 1.3), ctx.s // 2, ctx.face, 1941) > 0.86 and ctx.t % 2 == 0:
        col = pick(ctx.pal["spot"], 0.6)
        ctx.glow = col
    return col


def _hips(ctx: Ctx):
    if ctx.face == "top":
        return _skin(ctx, 0.48)
    return _scales(ctx)


def _fluke(ctx: Ctx):
    """Die zerrissene Schwanzflosse: am Ansatz breit, die Hinterkante in Fetzen."""
    w, h = ctx.cube.size[0], ctx.cube.size[1]
    u = min(1.0, abs(ctx.local[0]) / w)
    v = (ctx.local[1] - ctx.cube.origin[1]) / h
    bottom = 1.0 - 0.8 * u ** 1.3
    notch = 0.55 + 0.45 * min(1.0, u * 3.5)
    tear = 0.18 if int(u * 7.0) % 3 == 1 else 0.0                           # Risse
    if v > bottom * notch - tear or u > 0.97:
        return None
    ang = math.atan2(v, u + 0.05)
    if int(ang * 10.0) % 2 == 0:
        return pick(ctx.pal["spine"], 0.3 + u * 0.5)
    band = int(u * 5.0) % 2 == 0
    return pick(ctx.pal["band"] if band else ctx.pal["fin"], 0.3 + u * 0.45 + (0.1 if band else 0.0))


def _arm(ctx: Ctx):
    if ctx.face == "bottom":
        return _skin(ctx, 0.46)
    return _skin(ctx, 0.56 - rel_y(ctx) * 0.06)


def _hand(ctx: Ctx):
    if ctx.face == "front" and ctx.t == ctx.h - 1:
        return _skin(ctx, 0.42)
    return _skin(ctx, 0.54)


def _claw(ctx: Ctx):
    return pick(ctx.pal["lash"], 0.25 + (1.0 - rel_y(ctx)) * 0.5 + _LIT[ctx.face])


def _kelp(ctx: Ctx, extra: float = 0.0):
    """Tang: lange Bahnen mit einer hellen Mittelrippe und dunklen Rändern."""
    x, y, z = ctx.world
    n = noise3(x * 0.5, y * 0.25, z * 0.5, 1942)
    v = 0.38 + n * 0.25 + _LIT[ctx.face] + extra
    if ctx.face in ("front", "back") and ctx.s % 4 == 1:
        v += 0.14                                                           # Rippe
    if ctx.face in ("front", "back") and ctx.s % 4 == 3:
        v -= 0.1
    return pick(ctx.pal["kelp"], v)


def _shawl(ctx: Ctx):
    if ctx.face in ("front", "back", "west", "east") and ctx.t == ctx.h - 1 and hash01(ctx.s, ctx.cube.origin, 1943) < 0.45:
        return None
    return _kelp(ctx, 0.05)


def _kelp_strip(ctx: Ctx):
    if ctx.t >= ctx.h - 2 and hash01(ctx.s, ctx.t, ctx.cube.origin, 1944) < 0.5:
        return None
    return _kelp(ctx)


def _kelp_cape(ctx: Ctx):
    if ctx.face == "front":
        return _kelp(ctx, -0.12)                                            # Innenseite
    return _kelp(ctx)


def _kelp_cape_tip(ctx: Ctx):
    if ctx.face in ("front", "back", "west", "east") and ctx.t >= ctx.h - 5:
        if hash01(ctx.s, ctx.cube.origin, 1945) < (ctx.t - (ctx.h - 6)) * 0.22:
            return None
    return _kelp_cape(ctx)


def _teeth(ctx: Ctx):
    if ctx.face == "front":
        return pick(ctx.pal["bone"], 0.85 if ctx.s % 2 == 0 else 0.35)
    return pick(ctx.pal["bone"], 0.4)


def _tooth(ctx: Ctx):
    if ctx.face == "front" and ctx.t >= ctx.h - 1 and ctx.s != (ctx.w - 1) // 2:
        return None                                                         # spitz
    return pick(ctx.pal["bone"], 0.9 - rel_y(ctx) * 0.25 + _LIT[ctx.face])


def _black_coral(ctx: Ctx):
    """Schwarze Koralle: fast schwarz, rau, mit kleinen leuchtenden Polypen."""
    v = 0.35 + (1.0 - rel_y(ctx)) * 0.35 + _LIT[ctx.face]
    if hash01(ctx.u, ctx.v, 1946) > 0.88:
        col = pick(ctx.pal["polyp"], 0.6)
        ctx.glow = col
        return col
    return pick(ctx.pal["coral"], v)


def _gem(ctx: Ctx):
    v = 0.6 + 0.3 * (1.0 - rel_y(ctx))
    if ctx.s == 0 and ctx.t == 0:
        v = 1.0
    col = pick(ctx.pal["orb"], v)
    ctx.glow = col
    return col


def _orb(ctx: Ctx):
    """Die Kugel auf dem Stab: meerblaues Licht mit einem hellen Wirbel, ganz leuchtend."""
    cx, cy = (ctx.w - 1) / 2.0, (ctx.h - 1) / 2.0
    dx, dy = ctx.s - cx, ctx.t - cy
    r = math.hypot(dx, dy) / max(1.0, cx)
    ang = math.atan2(dy, dx)
    swirl = math.sin(ang * 2.0 + r * 5.0)
    v = 0.85 - r * 0.35 + swirl * 0.1
    if dx < -0.5 and dy < -0.5 and r < 0.7:
        v += 0.12
    col = pick(ctx.pal["orb"], v)
    ctx.glow = col
    return col


def _wood(ctx: Ctx):
    """Treibholz: gebleicht, mit Längsfasern und dunklen Rissen."""
    x, y, z = ctx.world
    fiber = math.sin(ctx.s * 2.1 + y * 0.3) * 0.08
    v = 0.52 + fiber + _LIT[ctx.face] + (noise3(x * 0.4, y * 0.6, z * 0.4, 1947) - 0.5) * 0.2
    if hash01(ctx.s, int(y * 2), ctx.face, 1948) > 0.93:
        v -= 0.28
    return pick(ctx.pal["wood"], v)


def _wood_knot(ctx: Ctx):
    return pick(ctx.pal["wood"], 0.32 + _LIT[ctx.face] + (0.1 if ctx.t == 0 else 0.0))


def _bone(ctx: Ctx):
    v = 0.6 + (0.2 if ctx.t == 0 else 0.0) + _LIT[ctx.face] + (hash01(ctx.u, ctx.v, 1949) - 0.5) * 0.12
    if ctx.face in ("front", "back", "west", "east") and ctx.s % 3 == 2:
        v -= 0.2                                                            # Fugen zwischen den Wirbeln
    return pick(ctx.pal["bone"], v)


def _cord(ctx: Ctx):
    return pick(ctx.pal["wood"], 0.25)


def _shell(ctx: Ctx):
    return pick(ctx.pal["shell"], 0.55 + (0.2 if (ctx.s + ctx.t) % 2 == 0 else -0.05) + _LIT[ctx.face])


def _tooth_charm(ctx: Ctx):
    return pick(ctx.pal["bone"], 0.75 + _LIT[ctx.face] - rel_y(ctx) * 0.2)


def materials(variant: str) -> dict:
    return {
        "chest": _chest, "belly": _belly, "face": _face, "hair": _hair, "hair_side": _hair_side, "lock": _lock,
        "hair_back": _hair_back, "hair_tip": _hair_tip, "strand": _strand, "strand_tip": _strand_tip, "ear_fin": _ear_fin,
        "arm": _arm, "hand": _hand, "claw": _claw, "arm_fin": _arm_fin, "bone": _bone,
        "hips": _hips, "scales": _scales, "fan": _fan, "spine_fin": _spine_fin, "fluke": _fluke,
        "shawl": _shawl, "kelp_strip": _kelp_strip, "kelp_cape": _kelp_cape, "kelp_cape_tip": _kelp_cape_tip,
        "teeth": _teeth, "tooth": _tooth, "black_coral": _black_coral, "gem": _gem, "orb": _orb,
        "wood": _wood, "wood_knot": _wood_knot, "cord": _cord, "shell": _shell, "tooth_charm": _tooth_charm,
    }
