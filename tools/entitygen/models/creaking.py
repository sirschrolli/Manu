"""
Knorz (statt Knarz) auf dem Skelett des Vanilla-Knarzes: gleiche Knochen, Eltern und Drehpunkte wie CreakingModel
(root -> upper_body -> head/body/right_arm/left_arm, root -> right_leg/left_leg), damit die Keyframe-Animationen
(Gehen, Angriff, Unverwundbar-Zittern, Tod) und der Kopf-Blick genauso laufen. Die Quader sind eigene:

  * hagere, verdrehte Gestalt aus dunkler, knorriger Eichenrinde mit tiefen, gewundenen Furchen, Astknoten, Moos- und
    Flechtenflecken und ein paar eingeklemmten, verdorrten Blättern
  * schmales Rindengesicht: wulstige Stirn und vorspringender Kiefer, dazwischen zwei tiefe Augenhöhlen mit
    glimmenden Glutaugen (Leucht-Textur), ein gesprungener Mund mit Splitterzahn
  * der Kopf endet in einem hohlen, zersplitterten Stumpf; aus ihm wächst ein kahles Zweiggeweih (eigene Knochen)
  * lange Astarme mit Knotenellbogen und Zweigfingern, Wurzelbeine mit gespreizten Wurzeln auf dem Boden

Die Wurzel des Vanilla-Modells heißt "root" – genau wie die Wurzel des Python-Modells; der Knochen wird deshalb
von Hand eingehängt (siehe _vanilla_root).
"""
from __future__ import annotations

import math

from ..geometry import Bone, Model
from ..paint import Ctx, hash01, mix, noise3, pick, ramp, rel_y

PALETTES = {
    "knorz": {
        "bark": ramp("#150f0a", "#ab957c", steps=9, hue_shift=0.02),
        "inner": ramp("#0a0706", "#4a3524"),                    # morsches Holz im hohlen Stumpf
        "dead": ramp("#3e352c", "#cbbda6"),                     # kahles, ausgebleichtes Holz (Zweige, Splitter)
        "moss": ramp("#17220c", "#7d8c38"),
        "lichen": ramp("#5c6656", "#c9d0b6"),
        "leaf": ramp("#3a1608", "#cf7a2c"),
        "socket": ramp("#030202", "#1e140e"),
        "ember": ramp("#4a0800", "#ffe27a", steps=7),
    },
}


def _vanilla_root(m: Model, pivot) -> Bone:
    """Hängt den Vanilla-Knochen "root" unter die Modellwurzel (Model.bone() lehnt den Namen ab)."""
    b = Bone("root", m.root, tuple(float(v) for v in pivot))
    m.root.children.append(b)
    m.bones["root"] = b            # ab jetzt hängen neue Knochen ohne Elternangabe am Vanilla-"root"
    return b


def build() -> Model:
    m = Model("creaking", 128, 64)
    _vanilla_root(m, (0, 24, 0))
    m.bone("upper_body", "root", pivot=(-1, -19, 0))

    # ---------------------------------------------------------------- Kopf (Drehpunkt wie Vanilla)
    m.bone("head", "upper_body", pivot=(-3, -11, 0))
    m.cube("head", (-3.5, -10, -3), (7, 10, 6), "skull")
    m.cube("head", (-3.5, -7, -4), (7, 2, 1), "brow")                  # wulstige Stirn über den Augenhöhlen
    m.cube("head", (-3, -3, -4), (6, 3, 1), "jaw")                     # vorspringender Kiefer mit Mund
    m.cube("head", (-3.5, -13, -3), (7, 3, 6), "stump")                # hohler, zersplitterter Stumpf
    # kahles Zweiggeweih: Hauptast je Seite, zwei Sprossen, feine Spitzen
    for side, sx in (("r", -1), ("l", 1)):
        m.bone(f"antler_{side}", "head", pivot=(2.6 * sx, -12, 0.5), rotation=(-8, 0, 38 * sx))
        m.cube(f"antler_{side}", (-0.5, -8, -0.5), (1, 8, 1), "branch")
        m.bone(f"antler_{side}_tine", f"antler_{side}", pivot=(0, -3.5, 0), rotation=(14, 0, -42 * sx))
        m.cube(f"antler_{side}_tine", (-0.5, -5, -0.5), (1, 5, 1), "twig")
        m.bone(f"antler_{side}_tip", f"antler_{side}", pivot=(0, -7.5, 0), rotation=(-18, 0, 30 * sx))
        m.cube(f"antler_{side}_tip", (-0.5, -4, -0.5), (1, 4, 1), "twig")
        m.bone(f"antler_{side}_spur", f"antler_{side}", pivot=(0, -5.5, 0), rotation=(0, 0, 62 * sx))
        m.cube(f"antler_{side}_spur", (-0.5, -3, -0.5), (1, 3, 1), "twig")

    # ---------------------------------------------------------------- Rumpf (zwei versetzte Blöcke wie Vanilla)
    m.bone("body", "upper_body", pivot=(0, -7, 1))
    m.cube("body", (0, -3, -3), (6, 13, 5), "trunk")
    m.cube("body", (-6, -4, -3), (6, 7, 5), "trunk")
    m.cube("body", (-5.5, -5, -2.5), (4, 1, 4), "trunk_top")           # Schulterbuckel mit Moos
    m.cube("body", (1, 10, -2.5), (4, 1.5, 4), "trunk")                # Hüftknoten

    # ---------------------------------------------------------------- Arme (Drehpunkte wie Vanilla)
    m.bone("right_arm", "upper_body", pivot=(-7, -9.5, 1.5))
    m.cube("right_arm", (-2, -1.5, -1.5), (3, 21, 3), "limb")
    m.cube("right_arm", (-2.5, 7.5, -2), (4, 3, 4), "knot")             # Knotenellbogen
    m.cube("right_arm", (-2.3, 19.5, -1.8), (3.6, 3, 3.6), "hand")
    m.cube("right_arm", (-2.2, 22.5, -1.8), (1, 4, 1), "finger")
    m.cube("right_arm", (-0.6, 22.5, -1.8), (1, 5, 1), "finger")
    m.cube("right_arm", (0.3, 22.5, 0.2), (1, 3.5, 1), "finger")
    m.cube("right_arm", (-1.6, 22.5, 0.8), (1, 3, 1), "finger")

    m.bone("left_arm", "upper_body", pivot=(6, -9, 0.5))
    m.cube("left_arm", (0, -1, -1.5), (3, 16, 3), "limb")
    m.cube("left_arm", (0.5, -5, -1), (2, 4, 2), "spur")               # abgebrochener Aststumpf über der Schulter
    m.cube("left_arm", (-0.5, 5.5, -2), (4, 3, 4), "knot")
    m.cube("left_arm", (-0.3, 15, -1.8), (3.6, 3, 3.6), "hand")
    m.cube("left_arm", (2.2, 18, -1.8), (1, 4, 1), "finger")
    m.cube("left_arm", (0.6, 18, -1.8), (1, 5, 1), "finger")
    m.cube("left_arm", (-0.3, 18, 0.2), (1, 3.5, 1), "finger")
    m.cube("left_arm", (1.6, 18, 0.8), (1, 3, 1), "finger")

    # ---------------------------------------------------------------- Wurzelbeine (Drehpunkte wie Vanilla)
    m.bone("left_leg", "root", pivot=(1.5, -16, 0.5))
    m.cube("left_leg", (-1.5, 0, -1.5), (3, 16, 3), "limb")
    m.cube("left_leg", (-2, 11.5, -2), (4, 3, 4), "knot")
    m.cube("left_leg", (-1, 14.5, -5), (2, 1.5, 3.5), "root")          # Wurzeln: vorn, außen, hinten
    m.cube("left_leg", (1.5, 14.8, -1), (3, 1.2, 2), "root")
    m.cube("left_leg", (-1.2, 14.8, 1.5), (2, 1.2, 3), "root")
    m.cube("left_leg", (-2.5, 15.7, -5.5), (8, 0, 11), "rootlets")     # feine Wurzelfasern am Boden

    m.bone("right_leg", "root", pivot=(-1, -17.5, 0.5))
    m.cube("right_leg", (-3, -1.5, -1.5), (3, 19, 3), "limb")
    m.cube("right_leg", (-3, -4.5, -1.5), (3, 3, 3), "hip")
    m.cube("right_leg", (-3.5, 13, -2), (4, 3, 4), "knot")
    m.cube("right_leg", (-2.5, 16, -5), (2, 1.5, 3.5), "root")
    m.cube("right_leg", (-6, 16.3, -1), (3, 1.2, 2), "root")
    m.cube("right_leg", (-2.8, 16.3, 1.5), (2, 1.2, 3), "root")
    m.cube("right_leg", (-7, 17.2, -5.5), (8, 0, 11), "rootlets")
    return m


# =============================================================================================
# Bemalen
# =============================================================================================

# verdorrte Blätter, die in Ritzen stecken (Welt-Punkte ohne Drehungen)
_LEAVES = [(-5.6, -4.9, -1.4), (3.2, 1.0, -2.1), (-3.0, -16.2, -1.0), (4.0, 7.5, 3.6), (-7.8, -5.5, 2.4)]


def _leaf(ctx: Ctx):
    x, y, z = ctx.world
    for i, (lx, ly, lz) in enumerate(_LEAVES):
        d = math.sqrt((x - lx) ** 2 + (y - ly) * (y - ly) * 0.7 + (z - lz) ** 2)
        if d < 1.15:
            v = 0.35 if d < 0.45 else 0.6 + hash01(i, ctx.u, ctx.v) * 0.25
            return pick(ctx.pal["leaf"], v)
    return None


def _bark(ctx: Ctx, base: float = 0.55, seed: int = 11, moss: float = 0.7, twist: float = 9.0, knots: bool = True):
    """
    Knorrige Rinde als ruhige Pixelkunst: senkrechte Furchen im Dreierraster (Furche, Grat, Mitte), die sich alle
    paar Einheiten um eine Spalte versetzen (die Gestalt wirkt verdreht), unterbrochene Furchen (Rindenplatten),
    seltene Astknoten, Moos auf den Oberseiten, wenige Flechten und ein paar verdorrte Blätter.
    """
    x, y, z = ctx.world
    face = ctx.face
    leaf = _leaf(ctx)
    if leaf is not None:
        return leaf
    big = noise3(x * 0.1, y * 0.07, z * 0.1, seed + 2)
    m = noise3(x * 0.22, y * 0.2, z * 0.22, seed + 5)
    if face == "bottom":
        return pick(ctx.pal["bark"], base - 0.22 + (big - 0.5) * 0.08)
    if face == "top":
        if m > moss - 0.06:
            return pick(ctx.pal["moss"], 0.45 + (m - moss) * 2.0 + (0.12 if hash01(ctx.u, ctx.v, seed) > 0.7 else 0.0))
        ring = (math.floor(x) + math.floor(z)) % 2
        return pick(ctx.pal["bark"], base + 0.1 + (big - 0.5) * 0.1 + ring * 0.04)
    coord = x if face in ("front", "back") else z
    c = math.floor(coord + 1e-6)
    seg = math.floor((y + 9.0 * hash01(math.floor(c / 3), seed)) / twist)
    cc = c + seg
    phase = cc % 3
    seg_y = math.floor(y / 3.0)
    v = base + (big - 0.5) * 0.12
    if phase == 0:
        bridge = hash01(cc, seg_y, seed + 3) > 0.88                      # Rindenplatte überbrückt die Furche
        deep = hash01(cc, math.floor(y / 6.0), seed + 4) > 0.3
        v -= 0.06 if bridge else (0.24 if deep else 0.14)
    elif phase == 1:
        v += 0.09 if face in ("front", "west") else 0.06                 # heller Grat (Licht von vorn links)
    ry = rel_y(ctx)
    v += (0.5 - ry) * 0.06
    # Astknoten: dunkler Kern, heller Wulst darum
    if knots:
        cell = 7.0
        kx, ky, kz = math.floor(x / cell), math.floor(y / cell), math.floor(z / cell)
        if hash01(kx, ky, kz, seed + 6) > 0.8:
            px = (kx + 0.5) * cell
            py = (ky + 0.3 + hash01(kx, ky, kz, seed + 7) * 0.4) * cell
            pz = (kz + 0.5) * cell
            hx = (x - px) if face in ("front", "back") else (z - pz)
            d = math.hypot(hx, (y - py) * 0.8)
            if d < 0.8:
                v = base - 0.3
            elif d < 1.7:
                v = base + 0.12
    col = pick(ctx.pal["bark"], v)
    # Moos sitzt oben auf den Formen und am Rücken, Flechten in kleinen Flecken
    if m > moss and (face == "back" or ry < 0.4):
        return pick(ctx.pal["moss"], 0.35 + (m - moss) * 2.2 + (0.1 if phase == 1 else 0.0))
    lc = (math.floor(x / 2.0), math.floor(y / 2.0), math.floor(z / 2.0))
    if phase != 0 and hash01(*lc, seed + 8) > 0.965:
        return pick(ctx.pal["lichen"], 0.55 + (0.25 if (math.floor(x) + math.floor(y)) % 2 else 0.0))
    return col


def _skull(ctx: Ctx):
    face, s, t = ctx.face, ctx.s, ctx.t
    if face == "top":
        # Boden des hohlen Stumpfs: morsches, dunkles Holz mit Fasern
        return pick(ctx.pal["inner"], 0.3 + hash01(ctx.u, ctx.v, 21) * 0.35 + (0.15 if s % 3 == 1 else 0.0))
    if face != "front":
        return _bark(ctx, 0.5, 31)
    # Vorderseite: nur Stirn (Zeilen 0-2) und das zurückliegende Augenband (Zeilen 5-6) sind zu sehen
    if t in (5, 6):
        eye = {(1, 5): 0.97, (2, 5): 0.5, (1, 6): 0.32, (5, 5): 0.97, (4, 5): 0.5, (5, 6): 0.32}.get((s, t))
        if eye is not None:
            col = pick(ctx.pal["ember"], eye)
            ctx.glow = col
            return col
        if s == 3:
            return pick(ctx.pal["bark"], 0.5 if t == 5 else 0.4)         # Nasenrücken zwischen den Höhlen
        return pick(ctx.pal["socket"], 0.2 if t == 6 else 0.45)
    if t in (3, 4):
        return pick(ctx.pal["socket"], 0.6)
    return _bark(ctx, 0.48, 32)


def _brow(ctx: Ctx):
    face = ctx.face
    if face == "bottom":
        return pick(ctx.pal["bark"], 0.12)                              # Schatten über den Augen
    if face == "front":
        col = _bark(ctx, 0.58, 41, moss=0.9)
        if ctx.t == 1 and ctx.s in (0, 6):
            return pick(ctx.pal["bark"], 0.3)
        if ctx.t == 0 and ctx.s in (2, 4):
            return pick(ctx.pal["bark"], 0.72)                          # Knubbel über den Augen
        return col
    return _bark(ctx, 0.55, 42, moss=0.9)


_MOUTH = {(1, 0): 0.2, (0, 1): 0.25, (1, 1): 0.05, (2, 1): 0.0, (4, 1): 0.05, (5, 1): 0.2, (4, 2): 0.15}


def _jaw(ctx: Ctx):
    face, s, t = ctx.face, ctx.s, ctx.t
    if face == "front":
        if (s, t) == (3, 1):
            return pick(ctx.pal["dead"], 0.78)                          # Splitterzahn im Riss
        dark = _MOUTH.get((s, t))
        if dark is not None:
            return pick(ctx.pal["socket"], dark * 3.0)
        return pick(ctx.pal["bark"], (0.62 if t == 0 else 0.5 if t == 1 else 0.42) - (0.06 if s in (0, 5) else 0.0))
    if face == "top":
        return pick(ctx.pal["bark"], 0.55)
    return _bark(ctx, 0.46, 52, moss=0.95, knots=False)


def _stump(ctx: Ctx):
    """Hohler, zersplitterter Stumpf über dem Kopf: Ränder mit Spänen, innen leer."""
    face, s, t = ctx.face, ctx.s, ctx.t
    if face == "bottom":
        return None
    if face == "top":
        return None                                                     # hohl: man sieht hinab auf morsches Holz
    # zackige Oberkante: je Spalte unterschiedlich hohe Splitter
    col_key = (s, face)
    cut = int(hash01(*col_key, 62) * 3.2)
    if t < cut:
        return None
    if t == cut:
        return pick(ctx.pal["dead"], 0.55 + hash01(ctx.u, 63) * 0.3)
    return _bark(ctx, 0.5, 64, moss=0.7)


def _branch(ctx: Ctx):
    a = 1.0 - rel_y(ctx)                                                # 0 = Ansatz, 1 = Spitze (Ast zeigt nach -Y)
    lit = {"front": 0.06, "west": 0.08, "top": 0.12, "east": -0.04, "back": -0.06, "bottom": -0.16}[ctx.face]
    if a < 0.45:
        v = 0.42 + a * 0.3 + lit
        if math.floor(a * 8) % 3 == 0:
            v -= 0.1                                                    # Rindenringe am Ansatz
        return pick(ctx.pal["bark"], v)
    return pick(ctx.pal["dead"], 0.34 + (a - 0.45) * 0.6 + lit)


def _twig(ctx: Ctx):
    a = 1.0 - rel_y(ctx)
    lit = {"front": 0.06, "west": 0.08, "top": 0.12, "east": -0.04, "back": -0.06, "bottom": -0.16}[ctx.face]
    return pick(ctx.pal["dead"], 0.3 + a * 0.45 + lit)


def _trunk(ctx: Ctx):
    x, y, z = ctx.world
    if ctx.face == "front" and 0.0 <= x < 3.0 and -2.0 <= y < 2.0:
        # Astloch in der Brust: dunkle Höhlung, heller Wulst am Rand
        hx, hy = x - 1.5, y - 0.0
        if abs(hx) < 1.0 and abs(hy) < 1.6 and not (abs(hx) > 0.5 and abs(hy) > 1.0):
            return pick(ctx.pal["inner"], 0.12 if hy > -0.5 else 0.3)
        return pick(ctx.pal["bark"], 0.7 if hy < 0 else 0.6)
    return _bark(ctx, 0.55, 81)


def _trunk_top(ctx: Ctx):
    if ctx.face == "top":
        x, y, z = ctx.world
        n = noise3(x * 0.5, 0, z * 0.5, 82)
        return pick(ctx.pal["moss"], 0.35 + n * 0.5)
    if ctx.face == "bottom":
        return None
    if ctx.t == 0:
        return pick(ctx.pal["moss"], 0.3 + hash01(ctx.u, 83) * 0.3)
    return _bark(ctx, 0.5, 84)


def _limb(ctx: Ctx):
    return _bark(ctx, 0.55, 91, knots=False)


def _knot(ctx: Ctx):
    face, s_, t = ctx.face, ctx.s, ctx.t
    if face in ("top", "bottom"):
        if face == "top" and noise3(ctx.world[0] * 0.4, 0, ctx.world[2] * 0.4, 102) > 0.55:
            return pick(ctx.pal["moss"], 0.5)
        return pick(ctx.pal["bark"], 0.62 if face == "top" else 0.2)
    ry = rel_y(ctx)
    v = 0.6 - abs(ry - 0.45) * 0.5                                      # Wulst: Mitte hell, oben und unten dunkler
    if s_ in (0, ctx.w - 1):
        v -= 0.1
    if face == "front" and t == 1 and s_ in (1, 2):
        v = 0.12 if s_ == 1 else 0.24                                   # Astloch
    elif face in ("west", "east") and (s_ + t) % 4 == 0:
        v -= 0.12                                                       # schräg verlaufende Faser
    return pick(ctx.pal["bark"], v)


def _hand(ctx: Ctx):
    if ctx.face == "bottom":
        return pick(ctx.pal["bark"], 0.18)
    v = 0.48 + (0.1 if ctx.t == 0 else 0.0) - (0.1 if ctx.s in (0, ctx.w - 1) else 0.0)
    if ctx.face in ("front", "west", "east", "back") and ctx.t == ctx.h - 1 and ctx.s % 2 == 1:
        v -= 0.18                                                       # Fingeransätze
    return pick(ctx.pal["bark"], v)


def _finger(ctx: Ctx):
    ry = rel_y(ctx)
    if ctx.face == "bottom":
        return pick(ctx.pal["dead"], 0.2)
    v = 0.4 - ry * 0.22 + (0.08 if ctx.face in ("front", "top") else 0.0)
    return pick(ctx.pal["bark"] if ry < 0.55 else ctx.pal["dead"], v + (0.1 if ry >= 0.55 else 0.0))


def _spur(ctx: Ctx):
    face = ctx.face
    if face == "top":
        # Bruchstelle des abgebrochenen Asts: helles Holz mit Jahresringen
        cx, cz = (ctx.w - 1) / 2, (ctx.h - 1) / 2
        r = math.hypot(ctx.s - cx, ctx.t - cz)
        return pick(ctx.pal["dead"], 0.75 - r * 0.25)
    return _bark(ctx, 0.48, 121)


def _hip(ctx: Ctx):
    return _bark(ctx, 0.46, 131)


def _root(ctx: Ctx):
    face = ctx.face
    if face == "bottom":
        return pick(ctx.pal["bark"], 0.1)
    x, y, z = ctx.world
    m = noise3(x * 0.4, y * 0.4, z * 0.4, 141)
    if face == "top" and m > 0.6:
        return pick(ctx.pal["moss"], 0.3 + (m - 0.6) * 1.8)
    v = 0.36 + (0.12 if face == "top" else 0.0) + (hash01(ctx.u, ctx.v, 142) - 0.5) * 0.12
    return pick(ctx.pal["bark"], v)


def _rootlets(ctx: Ctx):
    """Feine Wurzelfasern auf einer Bodenebene: gewundene Linien vom Fuß nach außen, sonst durchsichtig."""
    if ctx.face not in ("top", "bottom"):
        return None
    lx, _, lz = ctx.local
    ox, _, oz = ctx.cube.origin
    w, _, d = ctx.cube.size
    cx, cz = ox + w / 2, oz + d / 2
    dx, dz = lx - cx, lz - cz
    r = math.hypot(dx, dz)
    if r < 1.6 or r > 5.6:
        return None
    ang = math.atan2(dz, dx)
    seed = 151 if ox > -3 else 152
    for i in range(5):
        a0 = hash01(i, seed) * math.tau
        bend = (hash01(i, seed + 1) - 0.5) * 0.9
        a = a0 + bend * (r - 1.6) / 4.0
        diff = (ang - a + math.pi) % math.tau - math.pi
        reach = 3.6 + hash01(i, seed + 2) * 2.0
        if abs(diff) * r < 0.55 and r < reach:
            col = pick(ctx.pal["bark"], 0.32 + (r / 6.0) * 0.18)
            return col if ctx.face == "top" else pick(ctx.pal["bark"], 0.15)
    return None


def materials(variant: str) -> dict:
    return {
        "skull": _skull, "brow": _brow, "jaw": _jaw, "stump": _stump, "branch": _branch, "twig": _twig,
        "trunk": _trunk, "trunk_top": _trunk_top, "limb": _limb, "knot": _knot, "hand": _hand, "finger": _finger,
        "spur": _spur, "hip": _hip, "root": _root, "rootlets": _rootlets,
    }


def hidden(variant: str) -> list[str]:
    return []
