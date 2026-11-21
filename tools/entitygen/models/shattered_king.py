"""
Der Zerbrochene König: der letzte König der Alten. Als die Welten zerbrachen, wollte er sie mit seinem eigenen Herzen
zusammenhalten – es zersprang, und die Leere band ihn an seinen Thron.

  * ein Leib aus bleichem Sternenmarmor mit goldenen Säumen; durch Risse leuchtet das violette Licht der Leere
  * in der offenen Brust schwebt sein zersprungenes Herz: zwei Hälften aus Leerenkristall mit einem leuchtenden Spalt
  * ein strenges, steinernes Gesicht mit leuchtenden Augenschlitzen und einem langen Bart aus Marmor; über dem Kopf
    schwebt eine Krone aus sieben Kristallsplittern
  * mächtige Schulterstücke mit Kristallen, die Arme enden an den Ellbogen in gezackten, leuchtenden Bruchstellen –
    seine Hände schweben frei neben ihm: schwere Panzerhände aus Marmor und Gold mit kristallenen Fingerspitzen
  * unten ein Gewand aus Sternennacht, das in Fetzen ausläuft; um ihn kreisen Trümmer seines Leibes

Knochen-Namen werden in ShatteredKingModel (Java) für die Animationen verwendet. Im Spiel ×2,2.
"""
from __future__ import annotations

import math

from ..geometry import Model
from ..paint import Ctx, hash01, hexc, mix, noise3, pick

PALETTES = {
    "shattered_king": {
        "marble": [hexc(c) for c in ("#8a80a8", "#a49cc0", "#bcb4d4", "#cfc8e2", "#ddd6ec", "#e9e4f4", "#f6f3fb")],
        "vein": [hexc(c) for c in ("#6a5aa0", "#8a78c4", "#a898dc")],
        "gold": [hexc(c) for c in ("#6a4618", "#a8742a", "#d6a23c", "#f2cf62", "#fff0a8")],
        "cloth": [hexc(c) for c in ("#06041a", "#0c0828", "#140e38", "#1e164a", "#2a1e5e", "#382876")],
        "nebula": [hexc(c) for c in ("#3a1a6a", "#5a2a92", "#8a3ab4", "#c050c8", "#e890e0")],
        "star": [hexc(c) for c in ("#a8b8ff", "#d0d8ff", "#f4f0ff", "#ffffff")],
        "crystal": [hexc(c) for c in ("#2e1660", "#4c2a96", "#7a44d0", "#a070ec", "#c8a8ff", "#e6d8ff", "#ffffff")],
        "light": [hexc(c) for c in ("#5a2ab0", "#8a4ae8", "#b88aff", "#e4d0ff", "#ffffff")],
        "eye": [hexc(c) for c in ("#2a7ab8", "#46b6e6", "#9ae6ff", "#e8fbff")],
        "dark": [hexc(c) for c in ("#0e0b1e", "#161230", "#1f1a3e", "#2a244e", "#36305e")],
    },
}

BODY = (0.0, 2.0, 0.0)
CROWN_R = 5.6


def build() -> Model:
    m = Model("shattered_king", 256, 256)
    m.tex_res = 2
    m.bone("body", pivot=BODY)
    # Brust: vorn offen, darin das Herz
    m.cube("body", (-8, -12, 1.5), (16, 12, 3.5), "marble")                       # Rücken
    m.cube("body", (-8, -12, -4.5), (5.2, 12, 6), "marble")                       # rechte Brust
    m.cube("body", (2.8, -12, -4.5), (5.2, 12, 6), "marble")                      # linke Brust
    m.cube("body", (-2.8, -12, -4.5), (5.6, 3.2, 6), "marble")                    # Kragen
    m.cube("body", (-2.8, -2.8, -4.2), (5.6, 2.8, 5.7), "marble")                 # Brustbein
    m.cube("body", (-2.8, -8.8, 1.0), (5.6, 6.0, 0.6), "hollow")                 # Rückwand der Höhlung
    m.cube("body", (-8.6, -12.8, -5.0), (17.2, 1.6, 10.4), "gold")                # Halsring
    m.cube("body", (-6.5, 0, -3.8), (13, 4, 7.6), "gold")                         # Gürtel
    for side in (-1, 1):
        m.cube("body", (3.1 * side - 0.3, -8.6, -4.8), (0.6, 5.6, 0.4), "rune")   # Runen an der Öffnung
    m.bone("heart", "body", pivot=(0, -5.8, -1.8))
    m.bone("heart_l", "heart", pivot=(-0.4, 0, 0), rotation=(0, 0, -8))
    m.cube("heart_l", (-2.2, -2.6, -1.8), (2.1, 5.2, 3.6), "heart")
    m.bone("heart_r", "heart", pivot=(0.4, 0, 0), rotation=(0, 0, 10))
    m.cube("heart_r", (0.1, -2.4, -1.8), (2.1, 4.8, 3.6), "heart")

    # Kopf mit Bart und schwebender Krone
    m.bone("head", "body", pivot=(0, -12.5, -0.5))
    m.cube("head", (-4.5, -9.5, -4.5), (9, 9.5, 9), "face")
    m.cube("head", (-5.0, -7.2, -5.0), (10, 1.4, 1.2), "gold")                     # Stirnreif
    m.bone("beard", "head", pivot=(0, -1.2, -4.4))
    m.cube("beard", (-3.6, 0, -0.9), (7.2, 4.2, 1.8), "beard")
    m.bone("beard_tip", "beard", pivot=(0, 4.2, 0))
    m.cube("beard_tip", (-2.4, 0, -0.7), (4.8, 2.4, 1.4), "beard")
    m.bone("crown", "head", pivot=(0, -12.5, 0))
    for k in range(7):
        a = math.radians(-90 + k * 360 / 7)
        name = f"crown_{k}"
        length = 7.5 if k == 0 else (5.5 if k in (1, 6) else 4.5)
        m.bone(name, "crown", pivot=(math.cos(a) * CROWN_R, 0, math.sin(a) * CROWN_R),
               rotation=(math.sin(a) * -14, 0, math.cos(a) * 14))
        m.cube(name, (-0.9, -length, -0.9), (1.8, length + 1.2, 1.8), "crystal")

    # Schultern mit Kristallen, Armstümpfe mit leuchtenden Bruchstellen
    for side, sx in (("left", 1), ("right", -1)):
        sh = f"{side}_shoulder"
        m.bone(sh, "body", pivot=(10.0 * sx, -10.5, 0))
        m.cube(sh, (-4.5, -2.5, -5.5), (9, 6, 11), "marble")
        m.cube(sh, (-4.9, 3.0, -5.9), (9.8, 1.4, 11.8), "gold")
        m.cube(sh, (-1.9, 4, -2.2), (3.8, 6.5, 4.4), "marble")                    # Oberarm
        m.cube(sh, (-1.5, 10.2, -1.8), (3.0, 2.6, 3.6), "crystal")                # Bruchstelle
        for k, (x, z, rx, rz, length) in enumerate(((0.5, -2.0, -16, 24, 7.0), (2.5, 2.0, 18, 34, 5.0), (-1.0, 2.5, 26, 10, 4.0))):
            name = f"{side}_shoulder_shard_{k}"
            m.bone(name, sh, pivot=(x * sx, -2.5, z), rotation=(rx, 0, rz * sx))
            m.cube(name, (-1.0, -length, -1.0), (2.0, length + 1.2, 2.0), "crystal")

    # Gewand aus Sternennacht: vier Bahnen in zwei Gliedern, die wehen
    m.bone("robe", "body", pivot=(0, 4, 0))
    m.cube("robe", (-6.2, 0, -3.8), (12.4, 5, 7.6), "cloth")
    for name, pivot, rot, origin, size, low in (
            ("robe_front", (0, 0, -4.3), (-14, 0, 0), (-8.4, 0, -0.6), (16.8, 9, 1.2), (16.8, 9, 1.2)),
            ("robe_back", (0, 0, 4.3), (12, 0, 0), (-8.4, 0, -0.6), (16.8, 9, 1.2), (16.8, 10, 1.2)),
            ("robe_left", (6.9, 0, 0), (0, 0, -14), (-0.6, 0, -5.0), (1.2, 9, 10.0), (1.2, 8, 10.0)),
            ("robe_right", (-6.9, 0, 0), (0, 0, 14), (-0.6, 0, -5.0), (1.2, 9, 10.0), (1.2, 8, 10.0))):
        m.bone(name, "robe", pivot=pivot, rotation=rot)
        m.cube(name, origin, size, "cloth")
        m.bone(name + "_low", name, pivot=(0, 9, 0))
        m.cube(name + "_low", origin, low, "tatter")

    # die schwebenden Hände
    for side, sx in (("left", 1), ("right", -1)):
        hand = f"{side}_hand"
        m.bone(hand, pivot=(19.0 * sx, 3.0, -5.0), rotation=(10, -12 * sx, 8 * sx))
        m.cube(hand, (-3.8, -2.0, -4.0), (7.6, 3.6, 7.8), "marble")               # Handrücken
        m.cube(hand, (-4.2, -2.6, 3.4), (8.4, 4.6, 3.2), "gold")                  # Stulpe
        m.cube(hand, (-2.2, -1.6, 6.4), (4.4, 3.4, 2.2), "crystal")               # Bruchstelle am Handgelenk
        for k, fx in enumerate((-2.7, -0.9, 0.9, 2.7)):
            f = f"{side}_finger_{k}"
            m.bone(f, hand, pivot=(fx, 0.2, -4.0), rotation=(18, 0, 0))
            m.cube(f, (-0.8, -0.8, -4.6 + (0.6 if k in (0, 3) else 0.0)), (1.6, 1.6, 4.6 - (0.6 if k in (0, 3) else 0.0)), "finger")
            tip = f + "_tip"
            m.bone(tip, f, pivot=(0, 0, -4.6 + (0.6 if k in (0, 3) else 0.0)), rotation=(32, 0, 0))
            m.cube(tip, (-0.7, -0.7, -3.2), (1.4, 1.4, 3.2), "finger")
            m.cube(tip, (-0.6, -0.6, -4.2), (1.2, 1.2, 1.1), "crystal")
        thumb = f"{side}_thumb"
        m.bone(thumb, hand, pivot=(-3.6 * sx, 0.8, -1.5), rotation=(10, 40 * sx, 0))
        m.cube(thumb, (-0.8, -0.8, -4.0), (1.6, 1.6, 4.0), "finger")
        m.cube(thumb, (-0.6, -0.6, -5.0), (1.2, 1.2, 1.1), "crystal")

    # Trümmer seines Leibes, die um ihn kreisen
    m.bone("orbit", pivot=(0, -4, 0))
    for k, (r, ang, y, size) in enumerate(((16.0, 20, -6.0, (3.6, 2.8, 3.0)), (17.5, 110, 2.0, (2.6, 3.4, 2.6)),
                                           (15.0, 200, -9.0, (2.2, 2.2, 3.0)), (18.0, 290, 4.0, (3.0, 2.2, 2.4)))):
        a = math.radians(ang)
        name = f"fragment_{k}"
        m.bone(name, "orbit", pivot=(math.cos(a) * r, y, math.sin(a) * r), rotation=(15 * k, 30 * k, 10))
        m.cube(name, (-size[0] / 2, -size[1] / 2, -size[2] / 2), size, "fragment")
    return m


# =============================================================================================
# Bemalung
# =============================================================================================

def _edge(ctx: Ctx) -> int:
    return min(ctx.s, ctx.w - 1 - ctx.s, ctx.t, ctx.h - 1 - ctx.t)


def _veins(x, y, z) -> float:
    w = noise3(x * 0.1, y * 0.1, z * 0.1, 5) * 7.0
    a = abs(math.sin(x * 0.3 + y * 0.2 - z * 0.28 + w))
    b = abs(math.sin(-x * 0.16 + y * 0.37 + z * 0.23 + w * 0.7 + 1.7))
    return max(0.0, 1.0 - min(a, b * 1.3) / 0.08)


def _crack(x, y, z) -> float:
    """Leuchtende Risse: vom Herzen aus durch die Brust und vereinzelt über den ganzen Leib."""
    n = noise3(x * 0.28, y * 0.28, z * 0.28, 9)
    fine = max(0.0, 1.0 - abs(n - 0.5) / 0.02) if noise3(x * 0.07, y * 0.07, z * 0.07, 10) > 0.58 else 0.0
    hx, hy = x - BODY[0], y - (BODY[1] - 5.8)
    rc = math.hypot(hx, hy)
    if 3.2 < rc < 12.0 and z < -3.0:
        ang = math.atan2(hy, hx)
        ray = abs(math.sin(ang * 3.5 + noise3(x * 0.2, y * 0.2, 0, 19) * 2.4))
        if ray < 0.06 + 0.06 * (1.0 - rc / 12.0):
            fine = max(fine, 1.0 - rc / 14.0)
    return fine


def _marble(ctx: Ctx):
    x, y, z = ctx.world
    v = 0.62 + (noise3(x * 0.2, y * 0.2, z * 0.2, 3) - 0.5) * 0.28 + (noise3(x * 0.9, y * 0.9, z * 0.9, 4) - 0.5) * 0.1
    v += 0.1 if ctx.face == "top" else -0.14 if ctx.face == "bottom" else 0.0
    col = pick(ctx.pal["marble"], v)
    vein = _veins(x, y, z)
    if vein > 0:
        col = mix(col, pick(ctx.pal["vein"], 0.4 + vein * 0.5), vein * 0.65)
    crack = _crack(x, y, z)
    if crack > 0.3:
        col = mix(hexc("#1a1236"), pick(ctx.pal["light"], 0.6), crack)
        ctx.glow = mix((0, 0, 0, 255), pick(ctx.pal["light"], 0.55), crack * 0.9)
    e = _edge(ctx)
    if e == 0:
        col = mix(col, pick(ctx.pal["marble"], v - 0.3), 0.55)
    elif e == 1 and ctx.face != "bottom":
        col = mix(col, pick(ctx.pal["marble"], v + 0.2), 0.35)
    return col


def _gold(ctx: Ctx):
    x, y, z = ctx.world
    v = 0.55 + (noise3(x * 0.6, y * 0.6, z * 0.6, 14) - 0.5) * 0.16 + (0.15 if ctx.face == "top" else 0.0)
    col = pick(ctx.pal["gold"], v)
    if ctx.face in ("front", "back", "west", "east") and ctx.h >= 3:
        k = (ctx.s + abs(ctx.t - ctx.h // 2) * 2) % 10
        if k == 0 and 0 < ctx.t < ctx.h - 1:
            col = pick(ctx.pal["gold"], v - 0.25)
    if _edge(ctx) == 0:
        col = pick(ctx.pal["gold"], v - 0.15)
    elif ctx.t == 1:
        col = pick(ctx.pal["gold"], v + 0.25)
    return col


def _rune(ctx: Ctx):
    k = ctx.t % 5
    on = k in (0, 1, 3) and not (k == 3 and ctx.s % 2 == 0)
    col = pick(ctx.pal["light"], 0.75) if on else pick(ctx.pal["gold"], 0.5)
    if on:
        ctx.glow = pick(ctx.pal["light"], 0.65)
    return col


def _hollow(ctx: Ctx):
    x, y, _ = ctx.world
    d = math.hypot(x, y - (BODY[1] - 5.8))
    col = mix(pick(ctx.pal["dark"], 0.2), pick(ctx.pal["light"], 0.5), max(0.0, 1.0 - d / 5.0) * 0.8)
    ctx.glow = mix((0, 0, 0, 255), pick(ctx.pal["light"], 0.4), max(0.0, 1.0 - d / 5.5) * 0.9)
    return col


def _heart(ctx: Ctx):
    s = (ctx.s + 0.5) / max(ctx.w, 1)
    t = (ctx.t + 0.5) / max(ctx.h, 1)
    e = _edge(ctx)
    v = 0.5 + (0.35 if e == 0 else 0.0) + max(0.0, 0.3 - math.hypot(s - 0.4, t - 0.35)) * 1.4 + hash01(ctx.u, ctx.v, 18) * 0.08
    col = pick(ctx.pal["crystal"], v)
    ctx.glow = col
    return col


def _crystal(ctx: Ctx):
    t = (ctx.t + 0.5) / max(ctx.h, 1)
    a = 1.0 - t if ctx.face not in ("top", "bottom") else (1.0 if ctx.face == "top" else 0.0)
    edge = ctx.s == 0 or ctx.s == ctx.w - 1
    v = 0.18 + a * 0.62 + (0.12 if edge else 0.0) + (0.06 if ctx.face in ("front", "west") else 0.0)
    col = pick(ctx.pal["crystal"], v)
    if abs((ctx.s + 0.5) / max(ctx.w, 1) - t) < 0.08:
        col = mix(col, pick(ctx.pal["crystal"], v + 0.2), 0.5)
    if a > 0.45 or edge:
        ctx.glow = mix((0, 0, 0, 255), pick(ctx.pal["crystal"], min(0.95, v + 0.1)), 0.75)
    return col


def _face(ctx: Ctx):
    col = _marble(ctx)
    if ctx.face != "front":
        return col
    s = (ctx.s + 0.5) / max(ctx.w, 1)
    t = (ctx.t + 0.5) / max(ctx.h, 1)
    # glatte, strenge Züge: Brauen als Schatten, schmale leuchtende Augenschlitze, eine gerade Nase
    col = mix(col, pick(ctx.pal["marble"], 0.82 - abs(s - 0.5) * 0.3), 0.5)
    if 0.36 < t < 0.42 and (0.16 < s < 0.42 or 0.58 < s < 0.84):
        col = pick(ctx.pal["marble"], 0.2)                                         # Brauen
    if 0.44 < t < 0.5 and (0.2 < s < 0.4 or 0.6 < s < 0.8):
        col = pick(ctx.pal["eye"], 0.9 - abs((s if s < 0.5 else 1 - s) - 0.3) * 2)
        ctx.glow = col
    if 0.47 < t < 0.68 and 0.46 < s < 0.54:
        col = mix(col, pick(ctx.pal["marble"], 0.45), 0.5)                         # Nasenrücken
    if 0.78 < t < 0.82 and 0.34 < s < 0.66:
        col = pick(ctx.pal["marble"], 0.25)                                        # schmaler Mund
    return col


def _beard(ctx: Ctx):
    x, y, z = ctx.world
    v = 0.58 + (noise3(x * 0.4, y * 0.1, z * 0.4, 31) - 0.5) * 0.3
    col = pick(ctx.pal["marble"], v)
    # Strähnen: senkrechte Rillen
    if ctx.face in ("front", "back") and ctx.s % 3 == 0:
        col = pick(ctx.pal["marble"], v - 0.22)
    if ctx.face in ("front",) and hash01(ctx.u, ctx.v, 33) > 0.97:
        col = pick(ctx.pal["gold"], 0.6)                                           # goldene Bartringe
    return col


def _cloth(ctx: Ctx):
    x, y, z = ctx.world
    v = 0.3 + (noise3(x * 0.15, y * 0.15, z * 0.15, 41) - 0.5) * 0.35 + max(0.0, (y - 6.0) / 30.0)
    col = pick(ctx.pal["cloth"], v)
    neb = noise3(x * 0.1 + 4, y * 0.1, z * 0.1, 42)
    if neb > 0.6:
        col = mix(col, pick(ctx.pal["nebula"], (neb - 0.6) * 2.5), min(0.6, (neb - 0.6) * 2.0))
    h = hash01(ctx.u, ctx.v, 43)
    if h < 0.03:
        k = hash01(ctx.u, ctx.v, 44)
        star = pick(ctx.pal["star"], 0.3 + k * 0.7)
        col = mix(col, star, 0.5 + k * 0.5)
        if k > 0.4:
            ctx.glow = mix((0, 0, 0, 255), star, 0.5 + k * 0.4)
    if ctx.t <= 1 and ctx.face in ("front", "back", "west", "east"):
        col = pick(ctx.pal["gold"], 0.5)                                           # goldener Saum oben
    return col


def _tatter(ctx: Ctx):
    t = (ctx.t + 0.5) / max(ctx.h, 1)
    along = ctx.s if ctx.face in ("front", "back") else ctx.s
    cut = 0.55 + 0.35 * abs(math.sin(along * 0.9 + hash01(ctx.cube.bone.name) * 6)) + (hash01(along, ctx.cube.bone.name, 45) - 0.5) * 0.2
    if t > cut:
        return None
    col = _cloth(ctx)
    alpha = 255 if t < cut - 0.25 else round(255 - (t - (cut - 0.25)) / 0.25 * 120)
    if t > cut - 0.08:
        ctx.glow = mix((0, 0, 0, 255), pick(ctx.pal["nebula"], 0.8), 0.55)
    return col[:3] + (max(120, alpha),)


def _finger(ctx: Ctx):
    col = _marble(ctx)
    if ctx.face in ("front", "back") and ctx.t % 4 == 1:
        col = pick(ctx.pal["gold"], 0.6)                                           # Ringe
    return col


def _fragment(ctx: Ctx):
    col = _marble(ctx)
    if ctx.face == "bottom" or (ctx.face in ("west", "east") and ctx.t > ctx.h * 0.6):
        col = mix(col, pick(ctx.pal["dark"], 0.5), 0.6)
        if hash01(ctx.u, ctx.v, 51) > 0.85:
            ctx.glow = pick(ctx.pal["light"], 0.5)
    return col


def materials(variant: str) -> dict:
    return {"marble": _marble, "gold": _gold, "rune": _rune, "hollow": _hollow, "heart": _heart, "crystal": _crystal,
            "face": _face, "beard": _beard, "cloth": _cloth, "tatter": _tatter, "finger": _finger, "fragment": _fragment}
