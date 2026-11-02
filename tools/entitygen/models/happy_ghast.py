"""
Wolkenqualle (statt glücklicher Ghast) auf dem Skelett des Vanilla-Ghasts: derselbe Körperwürfel (16 Einheiten, im
Spiel ×4) und dieselben neun Fangarme an denselben Stellen wie HappyGhastModel, damit Geschirr, Schutzbrille, Seile
und das Wehen der Fangarme passen. Eigenes Aussehen:

  * eine Haufenwolke mit doppelter Texturauflösung (wie Vanillas Ghast): die Ballen sind als Kugeln im Raum gedacht
    und werden über die Kanten hinweg schattiert – oben sonnig, unten kühl, zwischen den Ballen weiche Falten
  * um die untere Hälfte ein Kranz aus Wolkenballen und runde Ecken (nur dort, wo das Geschirr nicht sitzt), deren
    Umriss die Ballen nachzeichnet
  * die Fangarme sind Dunstfäden, die nach unten dünner werden; bei der Regenwolke hängt ein Tropfen daran
  * ein freundliches Gesicht: große, glänzende Augen (unter der Schutzbrille), rosige Wangen, ein offenes Lächeln

Drei Gestalten: Schönwetterwolke (weiß mit goldenem Sonnensaum), Abendwolke (rosig mit fliederfarbenen Schatten) und
Regenwölkchen (blaugrau, mit Tropfen). Das Jungtier (Wölkchen) hat dieselbe Gestalt, im Spiel ein knapper Block groß,
mit größeren Augen.
"""
from __future__ import annotations

import math

from ..geometry import Model
from ..paint import Ctx, hash01, hexc, mix, noise3

_EYE = {
    "eye": hexc("#141a33"), "eye2": hexc("#26305e"), "eye3": hexc("#41539a"), "eye4": hexc("#6a83c8"),
    "shine": hexc("#ffffff"), "shine2": hexc("#d2e2ff"),
    "mouth": hexc("#4a2b44"), "tongue": hexc("#e37b92"),
}

PALETTES = {
    "schoenwetter": {
        "tones": [hexc(c) for c in ("#8e9cc0", "#a8b4d0", "#bfc9e0", "#d2dbec", "#e2e8f4", "#eef2f9", "#f8fafd", "#ffffff")],
        "sun": hexc("#fff0c4"), "crease": hexc("#a3b0cf"), "cheek": hexc("#f4a6b8"), "drop": None, **_EYE},
    "abendrot": {
        "tones": [hexc(c) for c in ("#7f6f9e", "#9b86b3", "#b99dc3", "#d4b5cb", "#e9c9d0", "#f6dcd8", "#fdebe3", "#fff6ef")],
        "sun": hexc("#ffc890"), "crease": hexc("#a58fb8"), "cheek": hexc("#f28fa6"), "drop": None, **_EYE},
    "regenwolke": {
        "tones": [hexc(c) for c in ("#56637c", "#6c7a94", "#8491aa", "#9ca8bd", "#b3bdcf", "#c8d0de", "#dbe1eb", "#eaeef4")],
        "sun": hexc("#f2f5f9"), "crease": hexc("#6f7d96"), "cheek": hexc("#e19ab2"),
        "drop": (hexc("#3f74c4"), hexc("#7fb2f0"), hexc("#d6ecff")), **_EYE},
}
PALETTES.update({f"{k}_baby": v for k, v in list(PALETTES.items())})

LOOKS = ("schoenwetter", "abendrot", "regenwolke")

# (x, z, Länge) der neun Fangarme wie bei Vanilla
_TENTACLES = ((-3.75, -5.0, 5), (1.25, -5.0, 7), (6.25, -5.0, 4), (-6.25, 0.0, 5), (-1.25, 0.0, 5), (3.75, 0.0, 7),
              (-3.75, 5.0, 8), (1.25, 5.0, 8), (6.25, 5.0, 5))

# Das Geschirr bedeckt oben die Seiten bis 10 Einheiten unter der Oberkante (y = 2 im Körper) – darunter ist Platz für
# den Wolkenkranz und die runden Ecken.
_SKIRT_TOP = 3.0


def build() -> Model:
    m = Model("happy_ghast", 128, 128)
    m.tex_res = 2
    m.bone("body", pivot=(0, 16, 0))
    m.cube("body", (-8, -8, -8), (16, 16, 16), "cloud")
    m.cube("body", (-8.75, _SKIRT_TOP, -8.75), (17.5, 9.0 - _SKIRT_TOP, 17.5), "puff")          # Ballenkranz unten
    for i, (x, z, length) in enumerate(_TENTACLES):
        name = f"tentacle{i}"
        m.bone(name, "body", pivot=(x, 7, z))
        m.cube(name, (-1, 0, -1), (2, length, 2), "wisp")
    return m


# =============================================================================================
# Wolkenballen als Kugeln im Raum (Körper-Koordinaten: Würfel -8..8, y nach unten)
# =============================================================================================

def _spheres() -> list[tuple[float, float, float, float, int]]:
    """
    Drei Lagen Wolkenballen (x, y, z, Radius, Lage): hinten große Ballen, die eine sanfte Kuppel bilden; davor
    mittlere, die als runde Ballen hervortreten; ganz vorn kleine Bäusche. Unten quellen kleine Ballen über den Rand
    (der krause Saum der Wolkenqualle).
    """
    import random
    rnd = random.Random(31)
    out: list[tuple[float, float, float, float, int]] = []

    def on_cube(a, y_range=None, faces=6):
        face = rnd.randrange(faces)
        u, v = rnd.uniform(-a, a), rnd.uniform(-a, a)
        if y_range is not None and face < 4:
            v = rnd.uniform(*y_range)
        return [(a, v, u), (-a, v, u), (u, v, a), (u, v, -a), (u, a, v), (u, -a, v)][face]

    def place(count, a, r_range, layer, spacing, y_range=None, faces=6, tag=0):
        added = 0
        for _ in range(8000):
            if added >= count:
                break
            x, y, z = on_cube(a, y_range, faces)
            r = rnd.uniform(*r_range)
            if any(lay == layer and (x - ox) ** 2 + (y - oy) ** 2 + (z - oz) ** 2 < (spacing * (r + orr)) ** 2
                   for ox, oy, oz, orr, lay in out):
                continue
            out.append((x, y, z, r, layer))
            added += 1

    for sx in (-1, 1):                                                    # hinten: acht große Ballen
        for sy in (-1, 1):
            for sz in (-1, 1):
                out.append((3.0 * sx, 3.0 * sy, 3.0 * sz, 7.2, 0))
    place(26, 6.0, (4.2, 5.2), 1, 0.62)                                   # mittlere Ballen
    place(14, 8.2, (2.4, 3.2), 2, 0.9, y_range=(-6.0, 2.0))               # kleine Bäusche
    place(24, 7.6, (2.9, 3.5), 3, 0.62, y_range=(5.2, 7.0), faces=4)      # Saum unten
    place(8, 7.6, (2.6, 3.2), 3, 0.62, faces=1)                           # … und unter dem Bauch
    return out


_SPHERES = _spheres()
_LIGHT = (lambda v: tuple(c / math.sqrt(sum(k * k for k in v)) for c in v))((-0.45, -1.0, -0.6))
_AXES = {"east": (0, 1.0), "west": (0, -1.0), "bottom": (1, 1.0), "top": (1, -1.0), "back": (2, 1.0), "front": (2, -1.0)}


def _billow(p: tuple[float, float, float], face: str):
    """
    Blick von außen senkrecht auf die Seite: welcher Ballen ist zuoberst? Gibt (Höhe über dem Mittelpunkt, Normale,
    Umrisslinie) zurück. Die Umrisslinie liegt auf dem tieferen Ballen dicht um einen höheren – oben fein und hell,
    unten (im Schatten des höheren Ballens) breiter und dunkler: 0 = keine, 1 = fein, 2 = Schatten.
    """
    axis, sign = _AXES[face]
    best = second = -1e9
    owner = other = None
    for s in _SPHERES:
        c = s[:3]
        r = s[3]
        d2 = sum((p[i] - c[i]) ** 2 for i in range(3) if i != axis)
        if d2 >= r * r:
            continue
        h = c[axis] * sign + math.sqrt(r * r - d2)
        if h > best:
            second, other = best, owner
            best, owner = h, s
        elif h > second:
            second, other = h, s
    if owner is None:
        return -1e9, (0.0, -1.0, 0.0), 0
    c = owner[:3]
    q = list(p)
    q[axis] = best * sign
    normal = tuple((q[i] - c[i]) / owner[3] for i in range(3))
    line = 0
    if other is not None and other[4] > owner[4]:
        gap = best - second
        below = p[1] > other[1] + other[3] * 0.2                          # unter der Mitte des höheren Ballens
        if gap < (0.75 if below else 0.3):
            line = 2 if below else 1
    return best, normal, line


def _body_pos(ctx: Ctx) -> tuple[float, float, float]:
    x, y, z = ctx.world
    return x, y - 16.0, z


def _tone(ctx: Ctx, b: float):
    tones = ctx.pal["tones"]
    b = max(0.0, min(0.999, b))
    return tones[int(b * len(tones))]


def _is_baby(ctx: Ctx) -> bool:
    return ctx.params.get("variant", "").endswith("_baby")


def _look(ctx: Ctx) -> str:
    return ctx.params.get("variant", "schoenwetter").removesuffix("_baby")


def _cloud_color(ctx: Ctx, p, n, line: int, face_zone: bool = False):
    lam = n[0] * _LIGHT[0] + n[1] * _LIGHT[1] + n[2] * _LIGHT[2]
    b = 0.8 + 0.17 * lam - p[1] * 0.014
    b += (noise3(p[0] * 0.3, p[1] * 0.3, p[2] * 0.3, 5) - 0.5) * 0.03
    if _is_baby(ctx):
        b += 0.04
    if line and not face_zone:
        b -= 0.15 if line == 2 else 0.08                                  # Umriss bzw. Schatten unter dem Ballen
    if ctx.face == "bottom":
        b += 0.12                                                         # die Unterseite sieht man oft (sie schwebt über einem)
    col = _tone(ctx, (b - 0.35) / 0.65)
    up = -n[1]
    if up > 0.3 and p[1] < 1.0:
        col = mix(col, ctx.pal["sun"], min(0.32, (up - 0.3) * 0.6))     # Sonnensaum oben
    if _look(ctx) == "abendrot" and n[0] < -0.2:
        col = mix(col, ctx.pal["sun"], min(0.3, -n[0] * 0.3))            # Abendlicht von der Seite
    return col


# =============================================================================================
# Gesicht (Vorderseite, 32×32 Pixel)
# =============================================================================================

_EYE_MASK = (".####.", "######", "######", "######", "######", "######", "######", ".####.")
_EYE_MASK_BABY = (".#####.", "#######", "#######", "#######", "#######", "#######", "#######", "#######", ".#####.")
_MOUTH = ("######", ".#tt#.", "..##..")
_MOUTH_BABY = ("####", ".tt.")


def _face(ctx: Ctx):
    """Augen, Wangen und Mund; None, wenn hier nichts vom Gesicht ist."""
    s, t = ctx.s, ctx.t
    pal = ctx.pal
    baby = _is_baby(ctx)
    mask = _EYE_MASK_BABY if baby else _EYE_MASK
    ew, eh = len(mask[0]), len(mask)
    top = 10 if baby else 9
    for left in ((6, 19) if baby else (7, 19)):
        es, et = s - left, t - top
        if 0 <= es < ew and 0 <= et < eh and mask[et][es] == "#":
            if (es, et) in ((1, 1), (2, 1), (1, 2), (2, 2)):
                return pal["shine"]                                       # großes Glanzlicht
            if (es, et) == (ew - 2, eh - 3):
                return pal["shine2"]                                      # kleines Glanzlicht
            if et >= eh - 2:
                return pal["eye4"] if 1 <= es <= ew - 2 else pal["eye3"]  # Spiegelung unten im Auge
            if et >= eh - 4:
                return pal["eye3"] if 1 <= es <= ew - 2 else pal["eye2"]
            return pal["eye"] if et < 2 or es in (0, ew - 1) else pal["eye2"]
    mouth = _MOUTH_BABY if baby else _MOUTH
    ml = 14 if baby else 13
    mt = 20 if baby else 18
    ms, mt_ = s - ml, t - mt
    if 0 <= mt_ < len(mouth) and 0 <= ms < len(mouth[0]):
        c = mouth[mt_][ms]
        if c == "#":
            return pal["mouth"]
        if c == "t":
            return pal["tongue"]
    return None


def _cheek(ctx: Ctx) -> float:
    """Wie stark die Wangenröte hier ist (0 = gar nicht)."""
    s, t = ctx.s, ctx.t
    baby = _is_baby(ctx)
    row = 19 if baby else 18
    for cs in ((4, 27) if baby else (4, 27)):
        dx = (s + 0.5 - cs) / (2.6 if baby else 2.3)
        dy = (t + 0.5 - row) / 1.3
        d = dx * dx + dy * dy
        if d < 1.0:
            return 0.75 if d < 0.45 else 0.4
    return 0.0


# =============================================================================================
# Materialien
# =============================================================================================

def _cloud(ctx: Ctx):
    p = _body_pos(ctx)
    depth, n, line = _billow(p, ctx.face)
    face_zone = ctx.face == "front" and 3 <= ctx.s <= 28 and 6 <= ctx.t <= 23
    if ctx.face == "front":
        f = _face(ctx)
        if f is not None:
            return f
    col = _cloud_color(ctx, p, n, line, face_zone)
    if ctx.face == "front":
        k = _cheek(ctx)
        if k > 0:
            col = mix(col, ctx.pal["cheek"], k)
    return col


def _puff(ctx: Ctx):
    """Ballenkranz und Ecken: nur, wo eine Wolkenkugel hinreicht – so zeichnet der Umriss die Ballen nach."""
    if ctx.face == "top":
        return None                                                       # kein Sims: von oben sieht man die Ballen selbst
    p = _body_pos(ctx)
    height, n, line = _billow(p, ctx.face)
    axis, sign = _AXES[ctx.face]
    if height < p[axis] * sign:
        return None                                                       # kein Ballen reicht bis hierher
    return _cloud_color(ctx, p, n, line)


def _wisp(ctx: Ctx):
    """
    Dunstfaden: oben so breit wie der Arm und in der Farbe der Wolkenunterseite, darunter ein schmaler, leicht
    gewellter Faden, der nach unten heller wird und sich in einzelne Fetzen auflöst; bei Regen hängt ein Tropfen daran.
    """
    face = ctx.face
    s, t, w, h = ctx.s, ctx.t, ctx.w, ctx.h
    i = ctx.cube.bone.name
    seed = int(i.removeprefix("tentacle")) if i.startswith("tentacle") else 0
    tones = ctx.pal["tones"]
    if face == "top":
        return tones[4]
    if face == "bottom":
        return None
    k = (t + 0.5) / h
    drop = ctx.pal.get("drop")
    if drop is not None and t >= h - 3:
        if t == h - 1:
            return drop[0] if s in (1, 2) else None                       # Tropfen unten
        if t == h - 2:
            return (drop[2] if s == 1 else drop[1]) if s in (1, 2) else None
        return drop[1] if s == 1 + seed % 2 else None
    if k < 0.3:
        b = 0.62 + k * 0.4 + (0.06 if s in (1, 2) else -0.05)
        return tones[max(0, min(len(tones) - 1, int(b * len(tones))))]
    # gewellter Faden, zwei Pixel breit, ganz unten einer, der sich in Fetzen auflöst
    wave = 1 if math.sin(t * 0.55 + seed * 1.7) > 0.35 else 0
    left = wave
    if k > 0.84:
        if t == h - 1 and hash01(seed, face, "fetzen") < 0.5:
            return None
        return tones[7] if s == left + 1 - wave else None
    if s not in (left, left + 1):
        return None
    return tones[7] if s == left else tones[6]


def materials(variant: str) -> dict:
    return {"cloud": _cloud, "puff": _puff, "wisp": _wisp}


def hidden(variant: str) -> list[str]:
    return []
