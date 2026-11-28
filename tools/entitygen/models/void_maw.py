"""
Leerenschlund: ein schwebender Räuber der Leere, halb Qualle, halb Maul.

  * eine gewölbte Glocke wie aus dem Nachthimmel geschnitten – tiefes Indigo mit violetten und magentafarbenen
    Nebelschleiern, darin glimmende Sterne; von innen scheint das Licht seines Kerns durch die Glocke
  * ein Kranz aus sechs bleichen, glühenden Augen rund um die Glocke, drei Splitter aus Leerenkristall stecken in der
    Kuppel (als hätte ihn die Leere selbst aufgespießt)
  * unten ein Saum aus vier Hautlappen, die beim Pumpen ausschlagen, mit ausgefranstem, durchscheinendem Rand
  * darunter das Maul: vier Lippen, die wie ein Schnabel geschlossen hängen und beim Angriff weit aufklappen – innen
    Reihen dünner, bleicher Zähne und ein leuchtender Schlund
  * acht lange Fangfäden mit Leuchtpunkten und glühenden Spitzen, dazu vier durchscheinende, gekräuselte Mundarme

Knochen-Namen werden in VoidMawModel (Java) für die Animationen verwendet.
"""
from __future__ import annotations

import math

from ..geometry import Model
from ..paint import Ctx, hash01, hexc, mix, noise3, pick, ramp

PALETTES = {
    "void_maw": {
        "bell": [hexc(c) for c in ("#07051a", "#0e0a2a", "#171040", "#221852", "#2e2066", "#3c2a7c", "#4c3694")],
        "nebula": [hexc(c) for c in ("#3a1a6a", "#5a2a92", "#8a3ab4", "#c050c8", "#e890e0")],
        "teal": [hexc(c) for c in ("#123a5a", "#1e6a8a", "#3aa6c4", "#8ae6f4")],
        "star": [hexc(c) for c in ("#a8b8ff", "#d0d8ff", "#f4f0ff", "#ffffff")],
        "rim": [hexc(c) for c in ("#2a1a58", "#3e2878", "#58409c", "#7a5cc0", "#a688e0", "#d4c0ff")],
        "core": [hexc(c) for c in ("#3a1470", "#6a28b0", "#a04ae0", "#d08cff", "#f4dcff", "#ffffff")],
        "tooth": ramp("#6a5c8a", "#f4eeff"),
        "lip": [hexc(c) for c in ("#0c0822", "#160f34", "#22184a", "#2e2160", "#3c2c78")],
        "tentacle": [hexc(c) for c in ("#120c2c", "#1c1440", "#281c56", "#36266e")],
        "light": [hexc(c) for c in ("#3ab8e8", "#7ae0ff", "#c4f6ff", "#ffffff")],
        "eye": [hexc(c) for c in ("#2a90c0", "#6ad8ff", "#c8f8ff", "#ffffff")],
        "crystal": [hexc(c) for c in ("#2e1660", "#4c2a96", "#7a44d0", "#a070ec", "#c8a8ff", "#e6d8ff")],
    },
}

# Lage der Glocke (Drehpunkt) im Modell: ihr Boden liegt gut einen Block über den Füßen
BELL = (0.0, 4.0, 0.0)
# Fangfäden: acht rund um den Saum, abwechselnd lang und kurz
TENTACLES = [(i, 22.5 + 45.0 * i, (7.0, 7.0, 6.0) if i % 2 == 0 else (6.0, 5.5, 5.0)) for i in range(8)]
# Augen: (Seite, waagerechte Lage, Höhe) auf der Glocke
EYES = [("front", -3.2, -1.4), ("front", 3.2, -1.4), ("west", -3.8, -1.0), ("east", -3.8, -1.0), ("west", 3.6, -0.6),
        ("east", 3.6, -0.6)]
# Kristallsplitter in der Kuppel: (x, z, Neigung um X, Neigung um Z, Länge, Breite)
SHARDS = [(2.5, 1.5, -18, 22, 7.0, 1.8), (-3.0, 2.5, 24, -16, 5.0, 1.4), (0.5, -3.0, -26, -8, 4.0, 1.2)]


# Glocke in Lagen (von unten nach oben): je Lage drei überlappende Quader mit leicht versetzten Oberkanten – so wirkt die
# Glocke rund und gestuft gewölbt statt kastig. (halbe Breite x, halbe Tiefe z, Oberkante)
BELL_LAYERS = [
    [(8.0, 6.0, -3.5), (6.0, 8.0, -4.0), (7.2, 7.2, -4.5)],
    [(6.6, 5.0, -6.5), (5.0, 6.6, -7.0), (6.0, 6.0, -7.4)],
    [(4.8, 3.6, -8.8), (3.6, 4.8, -9.2), (4.3, 4.3, -9.6)],
    [(2.8, 2.0, -10.4), (2.0, 2.8, -10.8)],
]


def build() -> Model:
    m = Model("void_maw", 160, 128)
    m.tex_res = 2
    m.bone("bell", pivot=BELL)
    # innen zuerst: der leuchtende Kern, dessen Unterseite der Schlund ist
    m.cube("bell", (-4.4, -5.5, -4.4), (8.8, 6.4, 8.8), "core")
    bottom = 1.0
    for layer in BELL_LAYERS:
        for hx, hz, top in layer:
            mat = {"*": "bell", "bottom": "underside"} if bottom == 1.0 else "bell"
            m.cube("bell", (-hx, top, -hz), (2 * hx, bottom - top, 2 * hz), mat)
        bottom = max(t for _, _, t in layer)
    for side, x, y in EYES:
        if side == "front":
            m.cube("bell", (x - 0.9, y - 0.9, -8.5), (1.8, 1.8, 0.6), "eye")
        else:
            sx = -1 if side == "west" else 1
            m.cube("bell", (7.9 if sx > 0 else -8.5, y - 0.8, x - 0.8), (0.6, 1.6, 1.6), "eye")
    for k, (x, z, rx, rz, length, width) in enumerate(SHARDS):
        name = f"shard_{k}"
        m.bone(name, "bell", pivot=(x, -8.5, z), rotation=(rx, 37 * k, rz))
        m.cube(name, (-width / 2, -length, -width / 2), (width, length + 1.5, width), "crystal")

    # Saum: vier Hautlappen am unteren Rand der Glocke (schlagen beim Pumpen aus)
    for name, pivot, origin, size in (("skirt_n", (0, 0.6, -7.6), (-8.5, 0, -5), (17, 0.8, 5)),
                                      ("skirt_s", (0, 0.6, 7.6), (-8.5, 0, 0), (17, 0.8, 5)),
                                      ("skirt_w", (-7.6, 0.6, 0), (-5, 0, -8.5), (5, 0.8, 17)),
                                      ("skirt_e", (7.6, 0.6, 0), (0, 0, -8.5), (5, 0.8, 17))):
        m.bone(name, "bell", pivot=pivot)
        m.cube(name, origin, size, "skirt")

    # Maul: vier Lippen um die Öffnung (geschlossen schräg nach innen, beim Angriff weit offen)
    m.bone("maw", "bell", pivot=(0, 1, 0))
    for name, pivot, origin, size, teeth in (
            ("lip_n", (0, 0, -4.4), (-4.4, 0, -0.5), (8.8, 5, 1), [((tx - 0.4, 2.4, 0.5), (0.8, 2.6, 0.8)) for tx in (-3, -1, 1, 3)]),
            ("lip_s", (0, 0, 4.4), (-4.4, 0, -0.5), (8.8, 5, 1), [((tx - 0.4, 2.4, -1.3), (0.8, 2.6, 0.8)) for tx in (-3, -1, 1, 3)]),
            ("lip_w", (-4.4, 0, 0), (-0.5, 0, -4.4), (1, 5, 8.8), [((0.5, 2.4, tz - 0.4), (0.8, 2.6, 0.8)) for tz in (-2, 0, 2)]),
            ("lip_e", (4.4, 0, 0), (-0.5, 0, -4.4), (1, 5, 8.8), [((-1.3, 2.4, tz - 0.4), (0.8, 2.6, 0.8)) for tz in (-2, 0, 2)])):
        m.bone(name, "maw", pivot=pivot)
        m.cube(name, origin, size, "lip")
        for o, sz in teeth:
            m.cube(name, o, sz, "tooth")

    # vier gekräuselte Mundarme an den Ecken des Mauls
    for k, (x, z) in enumerate(((-3.4, -3.4), (3.4, -3.4), (3.4, 3.4), (-3.4, 3.4))):
        arm = f"arm_{k}"
        m.bone(arm, "maw", pivot=(x, 0.5, z), rotation=(0, 45 + 90 * k, 0))
        m.cube(arm, (-1.6, 0, -0.25), (3.2, 8, 0.5), "frill")
        tip = f"arm_{k}_tip"
        m.bone(tip, arm, pivot=(0, 8, 0))
        m.cube(tip, (-1.3, 0, -0.25), (2.6, 7, 0.5), "frill")

    # acht Fangfäden mit Leuchtpunkten und Leuchtspitzen
    for i, angle, (l1, l2, l3) in TENTACLES:
        a = math.radians(angle)
        name = f"tentacle_{i}"
        m.bone(name, "bell", pivot=(math.cos(a) * 6.6, 1.2, math.sin(a) * 6.6))
        m.cube(name, (-0.5, 0, -0.5), (1.0, l1, 1.0), "tentacle")
        mid = f"tentacle_{i}_mid"
        m.bone(mid, name, pivot=(0, l1, 0))
        m.cube(mid, (-0.4, 0, -0.4), (0.8, l2, 0.8), "tentacle")
        tip = f"tentacle_{i}_tip"
        m.bone(tip, mid, pivot=(0, l2, 0))
        m.cube(tip, (-0.3, 0, -0.3), (0.6, l3, 0.6), "tentacle")
        m.cube(tip, (-0.5, l3 - 0.3, -0.5), (1.0, 1.0, 1.0), "bulb")
    return m


# =============================================================================================
# Bemalung
# =============================================================================================

def _bell_pos(ctx: Ctx) -> tuple[float, float, float]:
    x, y, z = ctx.world
    return x - BELL[0], y - BELL[1], z - BELL[2]


def _nebula(p, seed=0) -> float:
    """Schleier aus Nebel: 0..1, in Bändern um die Glocke gewunden."""
    x, y, z = p
    warp = noise3(x * 0.16, y * 0.16, z * 0.16, 31 + seed) * 5.0
    band = math.sin(math.atan2(z, x) * 2.0 + y * 0.32 + warp)
    n = noise3(x * 0.3 + 3, y * 0.3, z * 0.3, 32 + seed)
    return max(0.0, min(1.0, (band * 0.5 + 0.5) * 0.65 + (n - 0.5) * 0.9))


def _starfield(ctx: Ctx, col, density=0.035, glow=True):
    h = hash01(ctx.u, ctx.v, 41)
    if h < density:
        k = hash01(ctx.u, ctx.v, 42)
        star = pick(ctx.pal["star"], 0.2 + k * 0.8)
        col = mix(col, star, 0.35 + k * 0.6)
        if glow and k > 0.45:
            ctx.glow = mix((0, 0, 0, 255), star, 0.35 + k * 0.5)
    return col


_LIGHT = (lambda v: tuple(c / math.sqrt(sum(k * k for k in v)) for c in v))((-0.45, -1.0, -0.6))


def _bell(ctx: Ctx):
    p = _bell_pos(ctx)
    x, y, z = p
    # gewölbte Schattierung: eine gedachte Kugel um die Mitte der Glocke gibt jedem Texel seine Richtung
    nx, ny, nz = x, (y + 2.0) * 1.25, z
    ln = math.sqrt(nx * nx + ny * ny + nz * nz) or 1.0
    light = (nx * _LIGHT[0] + ny * _LIGHT[1] + nz * _LIGHT[2]) / ln
    # Grundton: oben dunkler (Nachthimmel), zum Rand hin aufgehellt
    v = 0.2 + max(0.0, (y + 11.0) / 12.0) * 0.26 + light * 0.16 + (noise3(x * 0.45, y * 0.45, z * 0.45, 11) - 0.5) * 0.14
    col = pick(ctx.pal["bell"], v)
    neb = _nebula(p)
    if neb > 0.45:
        col = mix(col, pick(ctx.pal["nebula"], (neb - 0.45) * 1.4 + 0.05 + light * 0.15), min(0.7, (neb - 0.45) * 1.6))
    teal = noise3(x * 0.22 + 9, y * 0.22, z * 0.22, 13)
    if teal > 0.66:
        col = mix(col, pick(ctx.pal["teal"], (teal - 0.66) * 2.5), min(0.45, (teal - 0.66) * 1.8))
    # das Licht des Kerns scheint von innen durch die Glocke (am stärksten unten nahe dem Maul)
    r = math.sqrt(x * x + (y + 1.5) ** 2 * 1.6 + z * z)
    inner = max(0.0, 1.0 - r / 10.0)
    if inner > 0.0:
        col = mix(col, pick(ctx.pal["core"], 0.3 + inner * 0.45), inner * 0.6)
        if inner > 0.3:
            ctx.glow = mix((0, 0, 0, 255), pick(ctx.pal["core"], 0.2 + inner * 0.4), (inner - 0.3) * 1.1)
    # Rippen: feine helle Linien von der Kuppel zum Rand
    ang = math.atan2(z, x)
    rib = abs(math.sin(ang * 8.0 + noise3(x * 0.2, y * 0.2, z * 0.2, 17) * 1.2))
    if rib < 0.09 and ctx.face != "top":
        col = mix(col, pick(ctx.pal["rim"], 0.5 + light * 0.2), 0.32)
    if ctx.face == "top":
        # Lichtkante an den Stufen der Kuppel
        edge = min(ctx.s, ctx.w - 1 - ctx.s, ctx.t, ctx.h - 1 - ctx.t)
        if edge == 0:
            col = mix(col, pick(ctx.pal["rim"], 0.45), 0.22)
    col = _starfield(ctx, col, 0.022 if ctx.glow is None else 0.008)
    return col


def _underside(ctx: Ctx):
    """Unterseite der Glocke: in der Mitte die Öffnung des Mauls (durchsichtig), außen Rippen und Saum."""
    x, _, z = _bell_pos(ctx)
    r = math.hypot(x, z)
    if max(abs(x), abs(z)) < 4.4:
        return None
    col = pick(ctx.pal["lip"], 0.35 + (noise3(x * 0.5, 1.0, z * 0.5, 21) - 0.5) * 0.3)
    ang = math.atan2(z, x)
    if abs(math.sin(ang * 8.0)) < 0.14:
        col = mix(col, pick(ctx.pal["rim"], 0.4), 0.4)
    if r < 6.2:
        col = mix(col, pick(ctx.pal["core"], 0.2), (6.2 - r) * 0.18)
    return col


def _core(ctx: Ctx):
    x, y, z = _bell_pos(ctx)
    if ctx.face == "bottom":
        # der Schlund: Ringe, die in die leuchtende Tiefe führen
        r = math.hypot(x, z)
        ring = 0.5 + 0.5 * math.sin(r * 2.4 - noise3(x * 0.4, 0, z * 0.4, 23) * 2.0)
        v = max(0.0, 1.0 - r / 6.5) * 0.75 + ring * 0.2
        col = pick(ctx.pal["core"], v)
        ctx.glow = mix((0, 0, 0, 255), col, 0.55 + v * 0.45)
        return col
    col = pick(ctx.pal["core"], 0.2 + noise3(x * 0.5, y * 0.5, z * 0.5, 24) * 0.3)
    return col


def _skirt(ctx: Ctx):
    x, y, z = _bell_pos(ctx)
    r = max(abs(x), abs(z))
    d = max(0.0, (r - 7.6) / 5.0)                              # 0 am Ansatz … 1 am Rand
    # ausgefranster Rand: Zacken und Löcher
    along = x if abs(z) > abs(x) else z
    scallop = 0.8 + 0.14 * math.sin(along * 1.4) + (hash01(ctx.u, 51) - 0.5) * 0.1
    if d > scallop:
        return None
    col = pick(ctx.pal["rim"], 0.18 + d * 0.6 + (0.1 if ctx.face == "top" else 0.0))
    col = mix(col, pick(ctx.pal["nebula"], 0.5), 0.2 + 0.2 * math.sin(along * 0.7))
    if ctx.face == "bottom":
        col = mix(col, pick(ctx.pal["bell"], 0.35), 0.35)
    # Adern im Lappen
    if abs(math.sin(along * 1.1)) < 0.1:
        col = mix(col, pick(ctx.pal["rim"], 0.85), 0.35)
    alpha = round(250 - d * 120)
    if d > scallop - 0.18:
        ctx.glow = mix((0, 0, 0, 255), pick(ctx.pal["rim"], 0.85), 0.7)
    return col[:3] + (alpha,)


def _lip(ctx: Ctx):
    inner = ctx.face in ("back", "front", "west", "east")
    v = 0.25 + (noise3(*(c * 0.6 for c in ctx.world), 61) - 0.5) * 0.25
    t = (ctx.t + 0.5) / max(ctx.h, 1)
    col = pick(ctx.pal["lip"], v + t * 0.25)
    # Saum der Lippe: leuchtende Kante unten
    if t > 0.82 and inner:
        col = mix(col, pick(ctx.pal["core"], 0.5), 0.6)
        ctx.glow = mix((0, 0, 0, 255), pick(ctx.pal["core"], 0.45), 0.8)
    return _starfield(ctx, col, 0.02, glow=False)


def _tooth(ctx: Ctx):
    t = (ctx.t + 0.5) / max(ctx.h, 1)
    return pick(ctx.pal["tooth"], 0.45 + t * 0.5 + (0.1 if ctx.face in ("front", "west") else 0.0))


def _frill(ctx: Ctx):
    s = (ctx.s + 0.5) / max(ctx.w, 1)
    t = (ctx.t + 0.5) / max(ctx.h, 1)
    edge = min(s, 1.0 - s)
    wave = 0.5 + 0.5 * math.sin(ctx.world[1] * 1.3 + (0 if s < 0.5 else 1.6))
    if edge < 0.12 and wave > 0.55:
        return None                                              # gekräuselter Rand
    col = pick(ctx.pal["rim"], 0.35 + wave * 0.35)
    col = mix(col, pick(ctx.pal["nebula"], 0.6), 0.25)
    if edge < 0.22:
        ctx.glow = mix((0, 0, 0, 255), pick(ctx.pal["nebula"], 0.7), 0.6)
    return col[:3] + (round(150 + 60 * (1.0 - t)),)


def _tentacle(ctx: Ctx):
    x, y, z = ctx.world
    seg = hash01(ctx.cube.bone.name)
    t = max(0.0, min(1.0, (y - 5.0) / 20.0))                   # 0 an der Glocke … 1 an der Spitze
    v = 0.45 - t * 0.2 + (noise3(x * 0.8, y * 0.35, z * 0.8, 71) - 0.5) * 0.35 + (0.12 if ctx.face == "top" else 0.0)
    col = pick(ctx.pal["tentacle"], v)
    col = mix(col, pick(ctx.pal["nebula"], 0.3), t * 0.35)
    # einzelne Leuchtpunkte, nach unten dichter
    if hash01(ctx.u, ctx.v, 72) < 0.06 + t * 0.12 and ctx.face in ("front", "back", "west", "east"):
        light = pick(ctx.pal["light"], 0.3 + hash01(ctx.u, ctx.v, 73) * 0.5)
        col = mix(col, light, 0.8)
        ctx.glow = mix((0, 0, 0, 255), light, 0.8)
    return col[:3] + (round(255 - t * 70 - seg * 10),)


def _bulb(ctx: Ctx):
    col = pick(ctx.pal["light"], 0.25 + hash01(ctx.u, ctx.v, 81) * 0.45)
    col = mix(col, pick(ctx.pal["nebula"], 0.8), 0.3)
    ctx.glow = col
    return col


def _eye(ctx: Ctx):
    s_ = (ctx.s + 0.5) / max(ctx.w, 1)
    t = (ctx.t + 0.5) / max(ctx.h, 1)
    big = ctx.w >= 3 and ctx.h >= 3
    if big and ctx.face not in ("top", "bottom"):
        across = s_ if ctx.face in ("front", "back") else s_
        if abs(across - 0.5) < 0.2 and 0.15 < t < 0.85:
            col = hexc("#1a0a3a")                                 # senkrechter, schmaler Schlitz
            ctx.glow = hexc("#140a30")
            return col
    d = math.hypot(s_ - 0.5, t - 0.5)
    col = pick(ctx.pal["eye"], 0.9 - d * 0.9)
    ctx.glow = col
    return col


def _crystal(ctx: Ctx):
    t = (ctx.t + 0.5) / max(ctx.h, 1)
    a = 1.0 - t if ctx.face not in ("top", "bottom") else (1.0 if ctx.face == "top" else 0.0)
    edge = ctx.s == 0 or ctx.s == ctx.w - 1
    v = 0.2 + a * 0.6 + (0.14 if edge else 0.0) + (0.06 if ctx.face in ("front", "west") else 0.0)
    col = pick(ctx.pal["crystal"], v)
    if a > 0.55 or edge:
        ctx.glow = mix((0, 0, 0, 255), pick(ctx.pal["crystal"], min(0.95, v + 0.1)), 0.75)
    return col


def materials(variant: str) -> dict:
    return {
        "bell": _bell, "underside": _underside, "core": _core, "skirt": _skirt, "lip": _lip, "tooth": _tooth,
        "frill": _frill, "tentacle": _tentacle, "bulb": _bulb, "eye": _eye, "crystal": _crystal,
    }


def preview_pose(variant: str) -> dict:
    return {"lip_n": (28, 0, 0), "lip_s": (-28, 0, 0), "lip_w": (0, 0, -28), "lip_e": (0, 0, 28),
            "skirt_n": (-30, 0, 0), "skirt_s": (30, 0, 0), "skirt_w": (0, 0, 30), "skirt_e": (0, 0, -30)}
