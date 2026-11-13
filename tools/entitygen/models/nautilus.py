"""
Nautilus und Geisternautilus (statt Nautilus und Zombie-Nautilus, gleiche Geometrie) auf dem Skelett des
Vanilla-Nautilus: der Vanilla-Knochen "root" (von Hand eingehängt wie beim Knarz), darin "shell" und "body" mit
"upper_mouth", "inner_mouth" und "lower_mouth" – genau die Knochen, Eltern und Drehpunkte, die NautilusModel und
die Schwimm-Animation lesen. Unter "shell" hängt "corals" wie beim ZombieNautilusCoralModel (dort mit Rüstung
ausgeblendet): beim Geisternautilus Seepocken und Tang, beim Nautilus leer.

Die Schale bleibt in der Hülle der beiden Vanilla-Schalenquader (oben 14x10x16, unten 14x8x20), damit Sattel und
Rüstung (eigene Vanilla-Geometrie um genau diese Quader) passen: oben eine flache Sitzfläche, Ecken gerundet, hinten
unten die offene Mündung mit Perlmutt-Lippe, in der der Körper sitzt. Die Tigerstreifen laufen strahlenförmig vom
Nabel der Windung aus und verblassen zur jüngsten Windung an der Mündung hin, wie bei echten Schalen. Am Maul
fächern Fangarme als eigene Kindknochen von Ober- und Unterkiefer auf und bewegen sich mit.

  * nautilus:        cremeweiße Schale mit rostroten Tigerstreifen, Perlmutt-Mündung, orangebraune Kappe mit hellen
                     Warzen, graurosa Fangarme, dunkle Lochaugen
  * geisternautilus: ausgebleichte, rissige Schale mit verblassten Streifen, Seepocken und Tangsträhnen, stumpfes
                     Perlmutt, grüngraue Fangarme, leuchtend meergrüne Augen (Glow)
"""
from __future__ import annotations

import math

from ..geometry import Bone, Model
from ..paint import Ctx, hash01, mix, noise3, pick, ramp

PALETTES = {
    "nautilus": {
        "shell": ramp("#b4a286", "#fcf7ec", steps=8), "stripe": ramp("#6e200e", "#e0703e", steps=8),
        "nacre": ramp("#b2a6b4", "#fffafc", steps=8), "pink": ramp("#c49aa8", "#f8e0e8"),
        "teal": ramp("#94bcc2", "#e2f6f6"), "inside": ramp("#2e2024", "#7a5a60"),
        "hood": ramp("#4a2210", "#cc7c46", steps=8), "spot": ramp("#c8a688", "#f8e8d2"),
        "skin": ramp("#5e464e", "#dcb8c0", steps=8), "tip": ramp("#9a8088", "#f4dee4"),
        "eye": ramp("#020202", "#5a5458"), "beak": ramp("#120e0e", "#5a5050"),
    },
    "geisternautilus": {
        "shell": ramp("#7e7e74", "#eeede4", steps=8), "stripe": ramp("#6e685c", "#b6ae9e", steps=8),
        "crack": ramp("#1e1c18", "#4e4840"), "nacre": ramp("#72847e", "#d6e4de", steps=8),
        "pink": ramp("#86968e", "#c6d2cc"), "teal": ramp("#6e9e92", "#c4ece0"), "inside": ramp("#0e1412", "#36423e"),
        "hood": ramp("#1a221e", "#5a6a62", steps=8), "spot": ramp("#76887e", "#c2cec6"),
        "skin": ramp("#34403a", "#9cb0a4", steps=8), "tip": ramp("#5a6c64", "#bccec4"),
        "eye": ramp("#06483e", "#c4fff2"), "beak": ramp("#080a0a", "#363e3e"),
        "barnacle": ramp("#4e4e46", "#e6e4da"), "kelp": ramp("#16260e", "#62822e", steps=6),
    },
}

GHOST = "geisternautilus"

# Lage der Knochen im Modell (Drehpunkte aufsummiert), um Quader in Modellkoordinaten anzugeben
_SHELL = (0.0, 16.0, -1.0)
_BODY = (0.0, 20.5, 6.3)

# Nabel der Windung (Modell-y, -z) und Richtung zur Mündung – die Streifen laufen von dort aus
_UMB = (15.0, 0.5)
_A_AP = math.atan2(-(20.0 - 15.0), 10.0 - 0.5)

# Fangarme: (x, y, Länge, Neigung, Einrollen der Spitze)
UPPER_ARMS = ((-4.5, 1.5, 5, 14, 30), (-3, 0.5, 7, 8, 22), (-1.5, 1.5, 6, 12, -18), (0, 0.5, 8, 6, 26),
              (1.5, 1.5, 6, 11, -20), (3, 0.5, 7, 9, 24), (4.5, 1.5, 5, 15, 32))
LOWER_ARMS = ((-3.75, -0.5, 5, -10, -28), (-2.25, 0.5, 7, -16, -20), (-0.75, -0.5, 6, -12, 18),
              (0.75, 0.5, 6, -13, -22), (2.25, -0.5, 7, -15, 20), (3.75, 0.5, 5, -9, -30))


def _vanilla_root(m: Model, pivot) -> Bone:
    """Der Vanilla-Knochen heißt "root" – Model.bone() reserviert den Namen, also von Hand einhängen."""
    b = Bone("root", m.root, tuple(float(v) for v in pivot))
    m.root.children.append(b)
    m.bones["root"] = b
    return b


def _box(m: Model, bone: str, off, x0, x1, ya, yb, za, zb, material: str):
    """Quader in Modellkoordinaten (y nach unten, z nach hinten) in einen Knochen mit Lage ``off`` setzen."""
    m.cube(bone, (x0 - off[0], ya - off[1], za - off[2]), (x1 - x0, yb - ya, zb - za), material)


def build() -> Model:
    m = Model("nautilus", 128, 128)
    m.world_faces = True
    _vanilla_root(m, (0, 29, -6))
    m.bone("shell", "root", pivot=(0, -13, 5))
    _box(m, "shell", _SHELL, -6, 6, 6, 7, -6, 6, "shell")             # flache Sitzfläche (Sattel)
    _box(m, "shell", _SHELL, -7, 7, 7, 8, -7, 7, "shell")
    _box(m, "shell", _SHELL, -7, 7, 8, 22, -7, 8, "shell")             # Windung
    _box(m, "shell", _SHELL, -6, 6, 9, 21, -8, -7, "shell")             # gerundete Stirn
    _box(m, "shell", _SHELL, -7, 7, 22, 23, -7, 8, "shell")
    _box(m, "shell", _SHELL, -6, 6, 23, 24, -6, 8, "shell")
    _box(m, "shell", _SHELL, 6, 7, 16, 23, 8, 12, "wall")              # Mündung: Seitenwände und Unterlippe
    _box(m, "shell", _SHELL, -7, -6, 16, 23, 8, 12, "wall")
    _box(m, "shell", _SHELL, -7, 7, 23, 24, 8, 12, "lip")
    m.bone("corals", "shell")
    # Seepocken nur in den gerundeten Ecken der Hülle – nichts ragt über die Vanilla-Schalenquader hinaus
    for x0, x1, ya, yb, za, zb in ((-3, -1, 8, 9, -8, -7), (-4, -3, 8, 9, -8, -7), (2, 4, 6, 7, -7, -6),
                                   (6, 7, 13, 15, -8, -7), (-7, -6, 17, 18, -8, -7), (1, 3, 21, 22, -8, -7),
                                   (-4, -3, 23, 24, -7, -6), (4, 5, 8, 9, -8, -7)):
        _box(m, "corals", _SHELL, x0, x1, ya, yb, za, zb, "barnacle")
    _box(m, "corals", _SHELL, 2.5, 2.5, 24, 28, -3, 2, "kelp")         # Tangsträhnen unter der Schale
    _box(m, "corals", _SHELL, -3.5, -3.5, 24, 27, 1, 6, "kelp")
    m.bone("body", "root", pivot=(0, -8.5, 12.3))
    m.cube("body", (-5, -4.51, -3), (10, 8, 10), "body")
    m.cube("body", (5, -2.5, 5.7), (1, 2, 2), "eye")
    m.cube("body", (-6, -2.5, 5.7), (1, 2, 2), "eye")
    m.bone("upper_mouth", "body", pivot=(0, -2.51, 7))
    m.cube("upper_mouth", (-5, -2, 0), (10, 4, 4), "sheath", inflate=-0.001)
    m.bone("inner_mouth", "body", pivot=(0, -0.51, 7.5))
    m.cube("inner_mouth", (-3, -2, -0.5), (6, 4, 4), "beak")
    m.bone("lower_mouth", "body", pivot=(0, 1.49, 7))
    m.cube("lower_mouth", (-5, -1.98, 0), (10, 4, 4), "sheath", inflate=-0.001)
    for prefix, parent, arms in (("tentacle_u", "upper_mouth", UPPER_ARMS), ("tentacle_l", "lower_mouth", LOWER_ARMS)):
        for i, (x, y, length, pitch, curl) in enumerate(arms):
            name = f"{prefix}{i}"
            m.bone(name, parent, pivot=(x, y, 3.5), rotation=(pitch, x * 4.0, 0))
            m.cube(name, (-0.5, -0.5, 0), (1, 1, length), "arm")
            m.bone(f"{name}_tip", name, pivot=(0, 0, length), rotation=(curl, 0, 0))
            m.cube(f"{name}_tip", (-0.5, -0.5, -0.2), (1, 1, 2), "arm")
    return m


def hidden(variant: str) -> list[str]:
    return [] if variant == GHOST else ["corals"]


def preview_pose(variant: str) -> dict:
    return {"upper_mouth": (6, 0, 0), "lower_mouth": (-6, 0, 0)}


# =============================================================================================

def _ghost(ctx: Ctx) -> bool:
    return ctx.params.get("variant") == GHOST


def _clamp(v: float) -> float:
    return max(0.0, min(1.0, v))


def _smooth(a: float, b: float, v: float) -> float:
    t = _clamp((v - a) / (b - a))
    return t * t * (3 - 2 * t)


def _shade(ctx: Ctx) -> float:
    return {"top": 0.05, "bottom": -0.1, "front": -0.03, "back": -0.03}.get(ctx.face, 0.0)


def _glow(ctx: Ctx, col, dim: float = 0.2):
    ctx.glow = mix(col, (0, 0, 0, 255), dim)
    return col


def _stripe(y: float, z: float) -> float:
    """Stärke der Tigerstreifen (0..1): strahlenförmig um den Nabel, zur Mündung hin verblassend."""
    dy, dz = y - _UMB[0], z - _UMB[1]
    r = math.hypot(dy, dz)
    a = math.atan2(-dy, dz)
    phi = (a - _A_AP) % (2 * math.pi)
    inten = _smooth(0.6, 2.3, phi) * _smooth(1.8, 3.6, r)
    band = math.sin(10 * a + 0.6 * math.sin(r * 0.8) + r * 0.22)
    return inten * _clamp((band - 0.1) / 0.6)


def _worley(x: float, y: float, z: float, size: float = 7.0, seed: int = 91):
    """Abstand zum nächsten und zweitnächsten Zellpunkt (3D) – Zellgrenzen ergeben ein Rissnetz."""
    gx, gy, gz = math.floor(x / size), math.floor(y / size), math.floor(z / size)
    best = []
    for dx in (-1, 0, 1):
        for dy in (-1, 0, 1):
            for dz in (-1, 0, 1):
                c = (gx + dx, gy + dy, gz + dz)
                px = (c[0] + hash01(c[0], c[1], c[2], seed)) * size
                py = (c[1] + hash01(c[0], c[1], c[2], seed + 1)) * size
                pz = (c[2] + hash01(c[0], c[1], c[2], seed + 2)) * size
                best.append((math.dist((x, y, z), (px, py, pz)), c))
    best.sort()
    return best[0][0], best[1][0], best[0][1], best[1][1]


def _crack(x: float, y: float, z: float) -> float:
    """0 = kein Riss, 1 = Rissrand, 2 = Riss."""
    d1, d2, c1, c2 = _worley(x, y, z)
    if hash01(*sorted((c1, c2)), 97) < 0.55:
        return 0
    gap = d2 - d1
    return 2 if gap < 0.45 else (1 if gap < 0.9 else 0)


def _plane(ctx: Ctx) -> tuple[int, int]:
    x, y, z = ctx.world
    if ctx.face in ("west", "east"):
        return math.floor(z), math.floor(y)
    if ctx.face in ("top", "bottom"):
        return math.floor(x), math.floor(z)
    return math.floor(x), math.floor(y)


def _painted_barnacle(ctx: Ctx):
    """Aufgemalte Seepocken: kleine Kreuze mit dunkler Öffnung, verstreut auf einem Raster."""
    p, q = _plane(ctx)
    cp, cq = p // 5, q // 5
    h = hash01(cp, cq, ctx.face, 61)
    if h < 0.8:
        return None
    ci = cp * 5 + 2 + int(hash01(cp, cq, 62) * 3) - 1
    cj = cq * 5 + 2 + int(hash01(cp, cq, 63) * 3) - 1
    di, dj = abs(p - ci), abs(q - cj)
    pal = ctx.pal
    if di + dj == 0:
        return pick(pal["crack"], 0.3)
    if di + dj == 1:
        return pick(pal["barnacle"], 0.95)
    if di == 1 and dj == 1:
        return pick(pal["barnacle"], 0.5)
    return None


def _nacre(ctx: Ctx, v: float = 0.75):
    x, y, z = ctx.world
    pal = ctx.pal
    t = noise3(x * 0.55, y * 0.55, z * 0.55, 53)
    if t > 0.6:
        return pick(pal["pink"], v)
    if t < 0.38:
        return pick(pal["teal"], v)
    return pick(pal["nacre"], v + 0.05)


def _exterior(ctx: Ctx):
    x, y, z = ctx.world
    pal = ctx.pal
    sh = _shade(ctx)
    n = (noise3(x * 0.4, y * 0.4, z * 0.4, 7) - 0.5) * 0.12
    s = _stripe(y, z)
    if ctx.face in ("west", "east"):
        r = math.hypot(y - _UMB[0], z - _UMB[1])
        if r < 0.8:
            return pick(pal["inside"], 0.4)                          # Nabel
        if r < 2.2:
            return pick(pal["shell"], 0.6 + sh)                       # Schwiele um den Nabel
    if not _ghost(ctx):
        if s > 0.62:
            return pick(pal["stripe"], 0.42 + (s - 0.62) * 0.5 + sh)
        if s > 0.3:
            return pick(pal["stripe"], 0.8 + sh)
        return pick(pal["shell"], 0.74 + n + sh)
    b = _painted_barnacle(ctx)
    if b is not None:
        return b
    if ctx.face in ("west", "east", "back") and y > 16:
        k = noise3(z * 0.8, y * 0.05, x * 0.1, 47)
        if k > 0.7 - (y - 16) * 0.012:
            return pick(pal["kelp"], 0.35 + (k - 0.6) + sh)          # Tangsträhnen hängen herab
    crack = _crack(x, y, z)
    if crack == 2:
        return pick(pal["crack"], 0.75)
    if crack == 1:
        return pick(pal["shell"], 0.56 + sh)                         # Rissrand
    if s > 0.6:
        return pick(pal["stripe"], 0.55 + n + sh)
    return pick(pal["shell"], 0.76 + n + sh)


def _shell(ctx: Ctx):
    x, y, z = ctx.world
    if ctx.face == "back":
        if y > 15.9:
            return pick(ctx.pal["inside"], 0.35)                     # Inneres der Mündung
        if abs(x) < 5 - max(0.0, 11.5 - y) * 1.2 and y > 9.5:
            # die ältere Windung ragt in die Mündung: dunkle Schicht, auf der die Kappe ruht
            return pick(ctx.pal["inside"], 0.2 + (y - 9.5) * 0.05 + (0.08 if abs(x) > 3.9 else 0.0))
        if abs(x) < 6 and y > 8.5:
            return _nacre(ctx, 0.7)                                  # Perlmuttrand
    return _exterior(ctx)


def _wall(ctx: Ctx):
    x, y, z = ctx.world
    face = ctx.face
    inner = (face == "west" and x > 0) or (face == "east" and x < 0)
    if inner:
        return _nacre(ctx, 0.62 - max(0.0, 11.0 - z) * 0.08)
    if face in ("back", "top"):
        return _nacre(ctx, 0.85)
    if face == "front":
        return pick(ctx.pal["inside"], 0.3)
    return _exterior(ctx)


def _lip(ctx: Ctx):
    face = ctx.face
    if face in ("top", "back"):
        return _nacre(ctx, 0.8 if face == "back" else 0.66)
    if face == "front":
        return pick(ctx.pal["inside"], 0.3)
    return _exterior(ctx)


# ---------------------------------------------------------------------------------------------
# Weichkörper

def _hood(ctx: Ctx):
    x, y, z = ctx.world
    pal = ctx.pal
    ix, iz = math.floor(x), math.floor(z)
    if hash01(ix, iz, 71) > 0.72 and (ix + iz) % 2 == 0:
        return pick(pal["spot"], 0.6)
    n = noise3(x * 0.5, y * 0.5, z * 0.5, 73)
    return pick(pal["hood"], 0.5 + (n - 0.5) * 0.4 + _shade(ctx))


def _skin(ctx: Ctx, v: float):
    x, y, z = ctx.world
    n = noise3(x * 0.3, y * 0.5, z * 0.3, 77)
    return pick(ctx.pal["skin"], v + (n - 0.5) * 0.14 + _shade(ctx))


def _body(ctx: Ctx):
    if ctx.face == "top":
        return _hood(ctx)
    if ctx.face == "bottom":
        return _skin(ctx, 0.62)
    return _skin(ctx, 0.46)


def _eye(ctx: Ctx):
    x, y, z = ctx.world
    outer = (ctx.face == "east" and x > 0) or (ctx.face == "west" and x < 0)
    pal = ctx.pal
    if not outer:
        return _skin(ctx, 0.4)
    glint = y < 19 and z < 13
    if _ghost(ctx):
        return _glow(ctx, pick(pal["eye"], 0.95 if glint else 0.62), 0.1 if glint else 0.25)
    return pick(pal["eye"], 0.75 if glint else 0.05)


def _sheath(ctx: Ctx):
    x, y, z = ctx.world
    face = ctx.face
    if face == "top":
        return _hood(ctx)
    if face == "back":
        # Ansätze der Fangarme
        if (math.floor(x) + math.floor(y)) % 2 == 0:
            return _skin(ctx, 0.3)
        return _skin(ctx, 0.52)
    if face == "bottom":
        return _skin(ctx, 0.6)
    return _skin(ctx, 0.5 - max(0.0, y - 21.5) * 0.06 - max(0.0, 17.5 - y) * 0.04)


def _beak(ctx: Ctx):
    x, y, z = ctx.world
    if ctx.face == "back" and abs(x) < 1.6 and 18.9 < y < 21.1:
        return pick(ctx.pal["beak"], 0.6 if y < 20 else 0.2)          # Papageienschnabel
    return _skin(ctx, 0.66)


def _arm(ctx: Ctx):
    _, _, lz = ctx.local
    pal = ctx.pal
    if ctx.cube.bone.name.endswith("_tip"):
        return pick(pal["tip"], 0.45 + lz * 0.2 + _shade(ctx))
    length = ctx.cube.size[2]
    t = _clamp(lz / max(length, 1.0))
    v = 0.38 + t * 0.24 - (0.05 if math.floor(lz) % 2 else 0.0)
    return pick(pal["skin"], v + _shade(ctx))


# ---------------------------------------------------------------------------------------------
# Bewuchs (nur Geisternautilus)

def _barnacle(ctx: Ctx):
    if not _ghost(ctx):
        return None
    x, y, z = ctx.world
    pal = ctx.pal
    if ctx.face in ("top", "front") and hash01(math.floor(x), math.floor(y), math.floor(z), 81) > 0.55:
        return pick(pal["crack"], 0.3)                               # Öffnung zwischen den Kalkplatten
    return pick(pal["barnacle"], 0.82 + _shade(ctx) - (0.14 if math.floor(x + y + z) % 2 else 0.0))


def _kelp(ctx: Ctx):
    if not _ghost(ctx):
        return None
    x, y, z = ctx.world
    z0 = ctx.cube.origin[2] + _SHELL[2]
    mid = z0 + 2.5 + math.sin(y * 1.3 + x) * 1.2
    if abs(z - mid) > 1.1 + (28 - y) * 0.08:
        return None
    pal = ctx.pal
    if abs(z - mid) < 0.5:
        return pick(pal["kelp"], 0.3)
    return pick(pal["kelp"], 0.62 + (noise3(y, z, x, 83) - 0.5) * 0.3)


def materials(variant: str) -> dict:
    return {"shell": _shell, "wall": _wall, "lip": _lip, "body": _body, "eye": _eye, "sheath": _sheath,
            "beak": _beak, "arm": _arm, "barnacle": _barnacle, "kelp": _kelp}
