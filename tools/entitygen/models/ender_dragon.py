"""
Sternenfresser (statt Enderdrache) auf dem Skelett des Vanilla-Enderdrachen: Kopf mit Kiefer, fünf Halswirbel, Rumpf mit
Flügeln (je zwei Glieder mit Flughaut) und vier Beinen (je drei Glieder), zwölf Schwanzwirbel – Namen, Drehpunkte und
Abstände wie in EnderDragonModel, denn dessen Animation legt Hals und Schwanz entlang der Flugbahn aus und schlägt die
Flügel. Eigenes Aussehen:

  * Schuppen wie Nachthimmel – fast schwarz mit violettem Schimmer, darin Sterne, die schwach leuchten; der Bauch in
    helleren Platten; über Kehle und Brust läuft ein Riss in die leuchtende Leere (wie bei den Leerwandlern)
  * ein langer Kopf mit Nüstern, Brauenwülsten, Wangendornen, schmalen violett gleißenden Augen und Zähnen; zwei
    geschwungene Hörner aus Purpurkristall, dazu ein Kamm aus Kristall über Kopf, Hals, Rücken und Schwanz
  * Flughäute wie ein Nebel im All (Indigo, Violett, Magenta) mit leuchtenden Adern entlang der Fingerknochen und
    Sternen, am Hinterrand zwischen den Fingern ausgebuchtet; Daumenkrallen am Flügelgelenk
  * der Schwanz wird zum Ende schmaler und läuft in eine Klinge aus Kristall aus; Füße mit dunklen Krallen

Dazu ein eigenes Muster, in dem er beim Sterben zerfällt (sternfresser_zerfall.png).
"""
from __future__ import annotations

import math

from ..geometry import Model
from ..paint import Ctx, Voronoi3D, hash01, hexc, mix, noise3, pick, ramp

PALETTES = {
    "sternenfresser": {
        "scale": ramp("#06040a", "#3e2c52", steps=8), "belly": ramp("#140c1e", "#6a5484", steps=7),
        "star": [hexc(c) for c in ("#5a4a7a", "#8a74b0", "#c4b0f0", "#f0e8ff", "#ffffff")],
        "nebula": [hexc(c) for c in ("#0a0618", "#1c0c3a", "#32145e", "#4e1c86", "#7428a8", "#a03cc4", "#c85ad8")],
        "vein": [hexc(c) for c in ("#3a1460", "#7a2ab8", "#c070f0", "#f0c0ff", "#ffffff")],
        "rift": [hexc(c) for c in ("#200838", "#4a1478", "#8a2ac0", "#c45af0", "#f0a8ff", "#ffffff")],
        "crystal": ramp("#2a0c48", "#e4a8ff", steps=8), "eye": [hexc(c) for c in ("#6a10a0", "#c040ff", "#f0a0ff", "#ffffff")],
        "claw": ramp("#050308", "#4a3a5a"), "tooth": ramp("#6a6070", "#f2eee6"),
    },
}

_SCALES = Voronoi3D(3.2, 1401)
_D = math.degrees


def build() -> Model:
    m = Model("ender_dragon", 512, 512)
    # --- Kopf mit Kiefer, Hörnern und Kamm (Drehpunkt wie bei Vanilla; die Animation setzt ihn ans Halsende)
    m.bone("head", pivot=(0, 20, -62))
    m.cube("head", (-7.5, -8, -10), (15, 15, 16), "skull")
    m.cube("head", (-5.5, -2, -25), (11, 6, 15), "snout")
    m.cube("head", (-4.5, -3.2, -23.5), (2, 1.5, 3), "nostril")
    m.cube("head", (2.5, -3.2, -23.5), (2, 1.5, 3), "nostril")
    m.cube("head", (-7.8, -9.2, -11), (5, 2, 7), "brow")
    m.cube("head", (2.8, -9.2, -11), (5, 2, 7), "brow")
    for side in (-1, 1):
        m.cube("head", (7.2 if side > 0 else -9.2, -1, -6), (2, 2, 6), "cheek")
        for k in range(3):
            m.cube("head", (side * 5.2 - 0.5, 4, -23 + k * 4), (1, 1.6, 1), "tooth")
    for k, (z, h) in enumerate(((-6, 3.2), (-2, 4.2), (2, 3.4))):
        m.cube("head", (-0.75, -8 - h, z), (1.5, h, 2.4), "crest")
    for side, name in ((-1, "right"), (1, "left")):
        m.bone(f"{name}_horn", "head", pivot=(side * 5, -7.5, -1), rotation=(32, side * 14, 0))
        m.cube(f"{name}_horn", (-1.6, -1.6, 0), (3.2, 3.2, 13), "horn")
        m.bone(f"{name}_horn_tip", f"{name}_horn", pivot=(0, 0, 13), rotation=(24, side * 6, 0))
        m.cube(f"{name}_horn_tip", (-1.1, -1.1, 0), (2.2, 2.2, 10), "horn_tip")
    m.bone("jaw", "head", pivot=(0, 4, -8))
    m.cube("jaw", (-5.5, 0, -17), (11, 4, 17), "jaw")
    for side in (-1, 1):
        for k in range(3):
            m.cube("jaw", (side * 4.4 - 0.5, -1.5, -16 + k * 4), (1, 1.5, 1), "tooth")

    # --- Hals: fünf Wirbel im Abstand von 10 (wie bei Vanilla), mit Kristalldorn
    for i in range(5):
        name = f"neck{i}"
        m.bone(name, pivot=(0, 20, -12 - i * 10))
        m.cube(name, (-4.5, -4.5, -5), (9, 9, 10), "neck")
        m.cube(name, (-0.75, -8.5 + i * 0.1, -2.5), (1.5, 4, 5), "dorsal")

    # --- Schwanz: zwölf Wirbel, zum Ende schmaler, am Ende eine Kristallklinge
    for i in range(12):
        name = f"tail{i}"
        s = 10.0 - i * 0.42
        m.bone(name, pivot=(0, 10, 60 + i * 10))
        m.cube(name, (-s / 2, -s / 2, -5), (s, s, 10), "tail")
        if i < 11:
            h = 4.0 - i * 0.22
            m.cube(name, (-0.7, -s / 2 - h, -2.5), (1.4, h, 5), "dorsal")
    m.cube("tail11", (-0.6, -9, -4.5), (1.2, 18, 9), "tail_blade")

    # --- Rumpf mit großen Rückenkristallen
    m.bone("body", pivot=(0, 20, 8))
    m.cube("body", (-12, -16, -16), (24, 24, 64), "body")
    for k, (z, h) in enumerate(((-12, 6), (0, 8), (12, 9), (24, 8), (36, 6))):
        m.cube("body", (-1.2, -16 - h, z), (2.4, h, 8), "dorsal_big")

    # --- Flügel: Knochen und Flughaut, je zwei Glieder (Drehpunkte wie bei Vanilla); Daumenkralle am Gelenk
    for side, name in ((1, "left"), (-1, "right")):
        wing = f"{name}_wing"
        m.bone(wing, "body", pivot=(12 * side, -15, -6))
        x0 = 0 if side > 0 else -56
        m.cube(wing, (x0, -3, -3), (56, 6, 6), "wing_bone")
        m.cube(wing, (x0, 0, 2), (56, 0, 56), "membrane")
        m.cube(wing, ((54 if side > 0 else -56), -7, -2.5), (2, 4, 2), "claw")
        tip = f"{name}_wing_tip"
        m.bone(tip, wing, pivot=(56 * side, 0, 0))
        m.cube(tip, (x0, -2, -2), (56, 4, 4), "wing_bone")
        m.cube(tip, (x0, 0, 2), (56, 0, 56), "membrane_tip")

    # --- Beine (Drehpunkte und Grundhaltung wie bei Vanilla), Füße mit Krallen
    for side, name in ((1, "left"), (-1, "right")):
        front = f"{name}_front_leg"
        m.bone(front, "body", pivot=(12 * side, 0, -6), rotation=(_D(1.3), 0, 0))
        m.cube(front, (-4, -4, -4), (8, 24, 8), "leg")
        m.bone(front + "_tip", front, pivot=(0, 20, -1), rotation=(_D(-0.5), 0, 0))
        m.cube(front + "_tip", (-3, -1, -3), (6, 24, 6), "leg")
        m.bone(f"{name}_front_foot", front + "_tip", pivot=(0, 23, 0), rotation=(_D(0.75), 0, 0))
        m.cube(f"{name}_front_foot", (-4, 0, -12), (8, 4, 16), "foot")
        for cx in (-3.5, -0.75, 2):
            m.cube(f"{name}_front_foot", (cx, 1.5, -15), (1.5, 2.5, 3.5), "claw")
        hind = f"{name}_hind_leg"
        m.bone(hind, "body", pivot=(16 * side, -4, 34), rotation=(_D(1.0), 0, 0))
        m.cube(hind, (-8, -4, -8), (16, 32, 16), "leg")
        m.bone(hind + "_tip", hind, pivot=(0, 32, -4), rotation=(_D(0.5), 0, 0))
        m.cube(hind + "_tip", (-6, -2, 0), (12, 32, 12), "leg")
        m.bone(f"{name}_hind_foot", hind + "_tip", pivot=(0, 31, 4), rotation=(_D(0.75), 0, 0))
        m.cube(f"{name}_hind_foot", (-9, 0, -20), (18, 6, 24), "foot")
        for cx in (-8, -3.25, 1.5, 6):
            m.cube(f"{name}_hind_foot", (cx, 3, -24), (2.5, 3, 4.5), "claw")
    return m


# =============================================================================================
# Hilfen
# =============================================================================================

def _uv(ctx: Ctx) -> tuple[float, float]:
    return (ctx.s + 0.5) / max(ctx.w, 1), (ctx.t + 0.5) / max(ctx.h, 1)


def _lit(ctx: Ctx, key: str, v: float):
    pal = ctx.pal[key]
    col = pal[max(0, min(len(pal) - 1, int(v * len(pal))))]
    ctx.glow = col
    return col


def _star(ctx: Ctx, rate: float = 0.012):
    r = hash01(ctx.u, ctx.v, 1402)
    if r > 1.0 - rate:
        return _lit(ctx, "star", ((r - (1.0 - rate)) / rate) ** 2)
    return None


def _scales(ctx: Ctx, base: float = 0.38):
    """Schuppen wie Nachthimmel: Zellen mit dunklen Fugen, oben ein violetter Schimmer, darin Sterne."""
    x, y, z = ctx.world
    cell, edge = _SCALES(x, y, z)
    star = _star(ctx)
    if star is not None:
        return star
    if ctx.face == "bottom":
        return _belly(ctx)
    v = base + (cell - 0.5) * 0.14 + (hash01(ctx.u, ctx.v, 1403) - 0.5) * 0.04
    if edge < 0.18:
        v -= 0.18                                                       # Fugen
    elif edge > 0.6:
        v += 0.06                                                       # Schuppenmitte glänzt
    if ctx.face == "top":
        v += 0.08
    return pick(ctx.pal["scale"], v)


def _belly(ctx: Ctx):
    """Bauchplatten: quer, heller."""
    x, y, z = ctx.world
    band = (z * 0.5) % 1.0
    v = 0.48 + (0.1 if band < 0.75 else -0.2) + (noise3(x * 0.3, y * 0.3, z * 0.3, 1404) - 0.5) * 0.1
    return pick(ctx.pal["belly"], v)


def _crystal(ctx: Ctx, axis: int, sign: int, start: float = 0.2):
    """Purpurkristall, der zur Spitze schmal wird (axis/sign: wie in wither._pointed)."""
    o, size, p = ctx.cube.origin, ctx.cube.size, ctx.local
    n = {"west": 0, "east": 0, "top": 1, "bottom": 1, "front": 2, "back": 2}[ctx.lface]
    if n == axis:
        if (ctx.lface in ("east", "bottom", "back")) == (sign > 0):
            return None
        return _lit(ctx, "crystal", 0.5)
    f = (p[axis] - o[axis]) / size[axis]
    if sign < 0:
        f = 1.0 - f
    m = 3 - axis - n
    d = abs(p[m] - (o[m] + size[m] / 2)) / (size[m] / 2)
    if f > start and d > 1.0 - (f - start) / (1.0 - start) * 0.92:
        return None
    edge = d > 0.6
    return _lit(ctx, "crystal", 0.22 + f * 0.42 + (0.14 if edge else 0.0))


# =============================================================================================
# Kopf
# =============================================================================================

def _skull(ctx: Ctx):
    face = ctx.lface
    u, v = _uv(ctx)
    if face in ("west", "east"):
        front = 1.0 - u if face == "west" else u                       # 1 = vorn
        # schmale, schräge Augen weit vorn an den Seiten
        du, dv = front - 0.84, v - (0.42 - (front - 0.84) * 0.6)
        if abs(du) < 0.1 and abs(dv) < 0.05:
            return _lit(ctx, "eye", 0.55 + (1.0 - max(abs(du) / 0.1, abs(dv) / 0.05)) * 0.5)
        if abs(du) < 0.13 and abs(dv) < 0.09:
            return pick(ctx.pal["scale"], 0.06)
    if face == "front":
        if v > 0.45 and abs(u - 0.5) < 0.38:
            return _scales(ctx, 0.3)                                    # da sitzt die Schnauze
    return _scales(ctx, 0.36)


def _snout(ctx: Ctx):
    if ctx.lface == "bottom":
        return pick(ctx.pal["scale"], 0.12)                              # Gaumen
    return _scales(ctx, 0.34)


def _nostril(ctx: Ctx):
    if ctx.lface == "top":
        return _lit(ctx, "rift", 0.45)                                  # glimmende Nüstern
    return _scales(ctx, 0.3)


def _brow(ctx: Ctx):
    return _scales(ctx, 0.44)


def _cheek(ctx: Ctx):
    return _crystal(ctx, 2, 1, 0.3)


def _tooth(ctx: Ctx):
    return pick(ctx.pal["tooth"], 0.6 + (0.2 if ctx.lface == "top" else 0.0))


def _crest(ctx: Ctx):
    return _crystal(ctx, 1, -1, 0.15)


def _horn(ctx: Ctx):
    o, size, p = ctx.cube.origin, ctx.cube.size, ctx.local
    f = (p[2] - o[2]) / size[2]
    if ctx.lface == "front":
        return pick(ctx.pal["scale"], 0.2)                              # Ansatz
    ring = int(f * 9) % 2
    return _lit(ctx, "crystal", 0.3 + ring * 0.08 + f * 0.25)


def _horn_tip(ctx: Ctx):
    return _crystal(ctx, 2, 1, 0.15)


def _jaw(ctx: Ctx):
    if ctx.lface == "top":
        u, v = _uv(ctx)
        if 0.15 < u < 0.85:
            return _lit(ctx, "rift", 0.2 + v * 0.35)                    # Rachen glimmt
    return _scales(ctx, 0.3)


# =============================================================================================
# Rumpf, Hals, Schwanz, Beine
# =============================================================================================

def _neck(ctx: Ctx):
    u, v = _uv(ctx)
    if ctx.lface == "bottom" and abs(u - 0.5) < 0.18:
        return _lit(ctx, "rift", 0.35 + (1.0 - abs(u - 0.5) / 0.18) * 0.5)   # Riss über die Kehle
    return _scales(ctx, 0.36)


def _body(ctx: Ctx):
    u, v = _uv(ctx)
    if ctx.lface == "bottom":
        x, y, z = ctx.local
        # Riss über die Brust (vorn), dahinter Bauchplatten
        zz = (z + 16) / 64
        if zz < 0.42:
            path = 0.5 + 0.06 * math.sin(zz * 30.0)
            width = 0.07 * math.sin(math.pi * min(1.0, zz / 0.42))
            if abs(u - path) < width:
                return _lit(ctx, "rift", 0.4 + (1.0 - abs(u - path) / max(width, 1e-6)) * 0.6)
        return _belly(ctx)
    return _scales(ctx, 0.38)


def _dorsal(ctx: Ctx):
    return _crystal(ctx, 1, -1, 0.1)


def _tail(ctx: Ctx):
    return _scales(ctx, 0.36)


def _tail_blade(ctx: Ctx):
    """Kristallklinge am Schwanzende: rautenförmig."""
    if ctx.lface in ("west", "east"):
        u, v = _uv(ctx)
        if abs(u - 0.5) * 2.0 + abs(v - 0.5) * 2.0 > 1.0:
            return None
        return _lit(ctx, "crystal", 0.85 - (abs(u - 0.5) + abs(v - 0.5)) * 0.6)
    return None


def _leg(ctx: Ctx):
    return _scales(ctx, 0.36)


def _foot(ctx: Ctx):
    return _scales(ctx, 0.32)


def _claw(ctx: Ctx):
    o, size, p = ctx.cube.origin, ctx.cube.size, ctx.local
    axis = max(range(3), key=lambda i: size[i])
    f = (p[axis] - o[axis]) / size[axis]
    return pick(ctx.pal["claw"], 0.35 + f * 0.4) if f > 0.5 else _lit(ctx, "crystal", 0.25) if hash01(ctx.u, ctx.v) > 0.8 \
        else pick(ctx.pal["claw"], 0.5)


# =============================================================================================
# Flügel
# =============================================================================================

def _wing_bone(ctx: Ctx):
    x = abs(ctx.local[0] - (ctx.cube.origin[0] if ctx.cube.origin[0] >= 0 else ctx.cube.origin[0] + ctx.cube.size[0]))
    if (x % 14.0) < 1.2:
        return pick(ctx.pal["scale"], 0.55)                             # Knöchelringe
    return _scales(ctx, 0.34)


def _membrane_at(ctx: Ctx, outer: bool):
    """Flughaut wie ein Nebel im All: Adern entlang der Fingerknochen, Sterne, ausgebuchteter Hinterrand.

    Lage auf der Haut: a = 0 am Rumpf … 1 an der Flügelspitze (über beide Glieder), b = 0 an der Vorderkante … 1 hinten."""
    o, size, p = ctx.cube.origin, ctx.cube.size, ctx.local
    a_local = (p[0] - o[0]) / size[0]
    if o[0] < 0:
        a_local = 1.0 - a_local                                          # rechter Flügel: gespiegelt
    a = (a_local + (1.0 if outer else 0.0)) / 2.0
    b = (p[2] - o[2]) / size[2]
    # Finger: vom Gelenk (a = 0.5, b = 0) zur Hinterkante; der Hinterrand buchtet sich zwischen den Fingern ein
    fingers = (0.08, 0.32, 0.52, 0.72, 0.92)
    nearest = min(fingers, key=lambda f: abs(a - f))
    gap = min(abs(a - f) for f in fingers)
    span = 0.1
    scallop = 0.92 - (min(gap, span) / span) ** 1.6 * 0.3 - (0.12 if a > 0.94 else 0.0)
    if b > scallop or (a > 0.97 and b > 0.25):
        return None
    if a < 0.02 and b > 0.6:
        return None
    # Adern entlang der Finger und vom Knochen aus verzweigt
    vein_dist = gap * 56.0 * 2.0                                         # in Einheiten
    if vein_dist < 0.55:
        return _lit(ctx, "vein", 0.3 + (1.0 - vein_dist / 0.55) * 0.3)
    branch = abs(math.sin(a * 23.0 + b * 9.0 + noise3(a * 4, b * 4, 0.0, 1405) * 4.0))
    if branch < 0.04 and 0.2 < b < 0.8 and noise3(a * 3, b * 3, 5.0, 1409) > 0.55:
        return mix(pick(ctx.pal["nebula"], 0.6), pick(ctx.pal["vein"], 0.2), 0.5)      # feine Seitenadern
    star = _star(ctx, 0.02)
    if star is not None:
        return star
    # Nebel: innen tiefes Indigo, zu den Rändern Violett und Magenta, dazwischen Wolken
    neb = noise3(a * 6.0, b * 6.0, 3.0 if outer else 1.0, 1406)
    k = 0.22 + b * 0.35 + neb * 0.35 + (0.08 if outer else 0.0)
    if b > scallop - 0.03:
        k += 0.25                                                       # heller Saum
    return pick(ctx.pal["nebula"], k)


def _membrane(ctx: Ctx):
    return _membrane_at(ctx, False)


def _membrane_tip(ctx: Ctx):
    return _membrane_at(ctx, True)


def materials(variant: str) -> dict:
    return {"skull": _skull, "snout": _snout, "nostril": _nostril, "brow": _brow, "cheek": _cheek, "tooth": _tooth,
            "crest": _crest, "horn": _horn, "horn_tip": _horn_tip, "jaw": _jaw, "neck": _neck, "dorsal": _dorsal,
            "tail": _tail, "tail_blade": _tail_blade, "body": _body, "dorsal_big": _dorsal, "wing_bone": _wing_bone,
            "membrane": _membrane, "membrane_tip": _membrane_tip, "leg": _leg, "foot": _foot, "claw": _claw}


def hidden(variant: str) -> list[str]:
    return []


def dissolve_mask(width: int, height: int):
    """Zerfall-Muster für den Tod (wie vanillas dragon_exploding: Grauwerte, bei denen er zuerst zerfällt, sind dunkel)."""
    from PIL import Image

    img = Image.new("RGBA", (width, height))
    px = img.load()
    for y in range(height):
        for x in range(width):
            n = noise3(x * 0.09, y * 0.09, 0.0, 1407) * 0.7 + hash01(x, y, 1408) * 0.3
            g = max(0, min(255, int(n * 255)))
            px[x, y] = (g, g, g, 255)
    return img
