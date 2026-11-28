"""
Ventor, der Gladiator der Winde (seit 1.9): der letzte Meister der Arena der Prüfungen. Als das vergessene Volk sein
Kolosseum verließ, blieb sein Standbild aus Erz auf dem Podest zurück – und der Wind, der durch die Ränge fuhr, füllte es
mit Leben. Er schläft im Spawner der Böen, bis alle sechs Käfige bezwungen sind.

Eine hohe Gestalt (im Spiel 1,1-fach vergrößert, mit dem Helmbusch gut dreieinhalb Blöcke hoch):

* **Der Leib** – ein Standbild aus Bronze mit grünem Grünspan in den Falten: breite Brust, gemeißelte Bauchmuskeln, ein
  Lederriemen schräg über der Brust. Durch Risse im Erz leuchtet der Wind, der ihn füllt.
* **Der Helm** – eine Galea aus poliertem Erz: eine breite Krempe auf Stirnhöhe (hinten tief als Nackenschutz), darunter das
  Visier mit zwei runden, vergitterten Augenfenstern, hinter denen die Augen leuchten; obenauf ein Kamm aus rotem Rosshaar
  (hinten fällt ein Schweif herab), an den Seiten zwei weiße Federn.
* **Die Rüstung** – ein breiter Kupfergürtel mit einer runden Schnalle (darin glimmt eine Windspirale), darunter ein roter
  Schurz; am linken Arm die Manica aus Kupferschienen, auf der linken Schulter der hohe Galerus; der rechte Arm ist frei, der
  Unterarm mit Lederbinden umwickelt.
* **Die Waffen** – in der Rechten der **Sturmdreizack** (ein Schaft aus dunklem Eschenholz mit Kupferspirale, ein Querstück
  mit leuchtender Windrune, drei Zinken mit Widerhaken, deren Spitzen leuchten), in der Linken das **Wurfnetz** (gebündelt,
  zwei Bahnen hängen herab, unten Bleigewichte).
* **Der Wirbelwind** – statt der Beine: vier Ringe aus hellen Windfäden, oben breit, unten spitz, die sich drehen; darin
  wirbeln Sand und Kiesel. (Den durchscheinenden Dunst im Wirbel zeichnet eine zweite Ebene: ventor_wind.py, gleiche
  Knochen, eigenes rollendes Bild.)

Der Wirbel ist ein eigener Knochen unter der Wurzel (er kippt nicht mit, wenn sich der Leib neigt). Knochen-Namen werden in
VentorModel (Java) für die Animationen verwendet.
"""
from __future__ import annotations

import math

from ..geometry import Model
from ..paint import Ctx, hash01, hexc, mix, noise3, pick, rel_y


def stops(*colors: str) -> list:
    """Handverlesene Tonleiter."""
    return [hexc(c) for c in colors]


PALETTES = {
    "ventor": {
        # Bronze des Standbilds (warm, golden im Licht)
        "bronze": stops("#1a1009", "#2e1d0e", "#463015", "#61441c", "#7f5b24", "#9e752f", "#bc9241", "#d7b25e", "#ecd28c", "#fbeec4"),
        # Grünspan in Falten und Ritzen
        "patina": stops("#123630", "#1c5045", "#286b5a", "#3a8a73", "#58ab8f", "#86cfb2"),
        # Kupfer der Rüstung (rötlicher, heller poliert)
        "copper": stops("#3a1b0e", "#5e2b15", "#86401f", "#ad5a2c", "#cc7840", "#e49a5e", "#f6c194", "#fde3c8"),
        "red": stops("#220405", "#3d0809", "#5e0f10", "#821a17", "#a42620", "#c63c2c", "#e2604a"),
        "leather": stops("#1d1009", "#2f1b0e", "#462914", "#5f391d", "#7a4b28", "#986236"),
        "wood": stops("#1a100a", "#2a1a10", "#3c2617", "#51341f", "#684428"),
        "steel": stops("#2c2a28", "#4a4640", "#6e675c", "#958b7a", "#bdb29c", "#e2d8c2", "#fbf4e2"),
        "rope": stops("#2e2414", "#4a3b22", "#685532", "#8a7346", "#ab925e", "#cbb27c"),
        "lead": stops("#15161a", "#24262c", "#373a42", "#4d515b", "#666b76"),
        "feather": stops("#5c6670", "#8a96a2", "#b2bec8", "#d4dde4", "#eef3f6", "#ffffff"),
        "sand": stops("#6e5a34", "#927a4a", "#b49a62", "#d4bc84", "#ecdcac"),
        # der Wind: die Fäden des Wirbels und was in ihm leuchtet (Augen, Spitzen, Runen)
        "wind": stops("#0b3c4a", "#11607a", "#1a8ca6", "#2fb9cf", "#62dbe8", "#a4f0f6", "#dcfcff", "#ffffff"),
        "gust": stops("#7fb6c4", "#a2d2de", "#c2e6ee", "#dcf4f8", "#f2fcff"),
    },
}

_LIT = {"top": 0.08, "front": 0.03, "west": 0.0, "east": -0.03, "back": -0.06, "bottom": -0.14}

#: der Sturmdreizack (in der Hand, Spitze nach vorn): so weit hinter der Hand der Knauf, so weit vorn die Spitze
TRIDENT_BUTT = 8.3
TRIDENT_TIP = 29.6
#: die Ringe des Wirbels: (halbe Breite, Höhe) von oben nach unten
RINGS = ((5.5, 6.0), (4.5, 6.0), (3.5, 5.5), (2.5, 5.0))
#: Höhe der Hüfte (Drehpunkt von Leib und Wirbel)
WAIST_Y = 1.5


def build() -> Model:
    m = Model("ventor", 128, 128)
    m.tex_res = 2

    # ================================================================== der Wirbelwind (statt der Beine)
    m.bone("wind", pivot=(0, WAIST_Y, 0))
    parent, y = "wind", 0.0
    for i, (half, height) in enumerate(RINGS):
        name = f"ring_{i}"
        m.bone(name, parent, pivot=(0, y, 0))
        m.cube(name, (-half, 0, -half), (2 * half, height, 2 * half), "gust", tag=f"ring:{i}",
               faces=("front", "back", "west", "east"))
        parent, y = name, height
    # was der Wind mit sich reißt: Kiesel, Sand, ein Fetzen roten Tuchs
    m.cube("ring_0", (4.6, 3.6, -1.2), (0.9, 0.9, 0.9), "pebble")
    m.cube("ring_1", (-4.1, 2.2, 1.6), (0.8, 0.8, 0.8), "pebble")
    m.cube("ring_1", (1.4, 4.4, -4.0), (0.7, 0.7, 0.7), "pebble")
    m.cube("ring_2", (2.8, 1.6, 1.9), (0.8, 0.8, 0.8), "pebble")
    m.cube("ring_2", (-2.2, 3.4, -3.1), (0.7, 0.7, 0.7), "pebble")
    m.cube("ring_0", (-5.0, 1.4, 1.0), (0.0, 1.2, 2.2), "scrap")
    m.cube("ring_1", (2.0, 1.0, 4.1), (2.0, 1.0, 0.0), "scrap")

    # ================================================================== der Leib (Drehpunkt an der Hüfte)
    m.bone("body", pivot=(0, WAIST_Y, 0))
    m.cube("body", (-4.0, -8.2, -2.3), (8.0, 6.0, 4.6), "abdomen")
    m.cube("body", (-4.9, -15.0, -2.8), (9.8, 7.2, 5.6), "chest")
    m.cube("body", (-1.6, -16.6, -1.5), (3.2, 2.0, 3.0), "neck")
    m.cube("body", (-4.6, -2.4, -2.75), (9.2, 3.0, 5.5), "belt")
    m.cube("body", (-1.4, -2.75, -3.1), (2.8, 3.6, 0.4), "buckle")
    # der rote Schurz unter dem Gürtel (vorn und hinten, damit er im Wind flattert)
    m.bone("loin_front", "body", pivot=(0, 0.4, -2.75))
    m.cube("loin_front", (-2.8, 0, -0.4), (5.6, 7.5, 0.4), "loin")
    m.bone("loin_back", "body", pivot=(0, 0.4, 2.75))
    m.cube("loin_back", (-2.8, 0, 0), (5.6, 7.0, 0.4), "loin")

    # ================================================================== der Helm
    m.bone("head", "body", pivot=(0, -16.2, 0))
    m.cube("head", (-3.7, -8.8, -3.9), (7.4, 8.8, 7.8), "helm")
    m.cube("head", (-4.7, -6.0, -4.9), (9.4, 0.8, 10.6), "brim")                      # die Krempe (hinten der Nackenschutz)
    m.cube("head", (-3.0, -9.8, -3.2), (6.0, 1.0, 6.4), "helm_top")                  # die Glocke rundet sich oben
    m.cube("head", (-1.0, -10.7, -3.7), (2.0, 1.0, 7.6), "crest_base")
    m.cube("head", (-1.0, -17.6, -5.3), (2.0, 7.0, 11.4), "crest")                    # der Kamm aus rotem Rosshaar
    m.bone("crest_tail", "head", pivot=(0, -12.0, 5.7), rotation=(16, 0, 0))
    m.cube("crest_tail", (-0.7, 0, -0.8), (1.4, 8.0, 1.6), "crest_tail")

    # ================================================================== die Arme (rechts bei −x: frei; links: Manica, Galerus)
    m.bone("arm_r", "body", pivot=(-6.4, -13.8, 0))
    m.cube("arm_r", (-1.8, -1.6, -1.8), (3.6, 7.6, 3.6), "arm")
    m.bone("forearm_r", "arm_r", pivot=(0, 6.0, 0))
    m.cube("forearm_r", (-1.6, 0, -1.6), (3.2, 6.6, 3.2), "wrap")
    m.cube("forearm_r", (-1.85, 4.3, -1.85), (3.7, 1.5, 3.7), "cuff")
    m.bone("hand_r", "forearm_r", pivot=(0, 6.6, 0))
    m.cube("hand_r", (-1.6, 0, -1.6), (3.2, 3.2, 3.2), "hand")

    m.bone("arm_l", "body", pivot=(6.4, -13.8, 0))
    m.cube("arm_l", (-1.8, -1.6, -1.8), (3.6, 7.6, 3.6), "manica")
    m.cube("arm_l", (-2.25, -2.3, -2.25), (4.5, 2.8, 4.5), "manica_top")
    m.bone("galerus", "arm_l", pivot=(1.6, -1.9, 0), rotation=(0, 0, 14))
    m.cube("galerus", (-0.6, -4.6, -2.5), (1.4, 5.4, 5.0), "galerus")
    m.cube("galerus", (-0.6, -5.4, -2.7), (2.2, 0.8, 5.4), "galerus_lip")               # die Oberkante biegt nach außen
    m.bone("forearm_l", "arm_l", pivot=(0, 6.0, 0))
    m.cube("forearm_l", (-1.6, 0, -1.6), (3.2, 6.6, 3.2), "manica")
    m.bone("hand_l", "forearm_l", pivot=(0, 6.6, 0))
    m.cube("hand_l", (-1.6, 0, -1.6), (3.2, 3.2, 3.2), "hand")

    # ================================================================== der Sturmdreizack (Spitze nach vorn, wenn der Arm hängt)
    m.bone("trident", "hand_r", pivot=(0, 1.7, 0), rotation=(90, 0, 0))
    m.cube("trident", (-0.95, 6.8, -0.95), (1.9, 1.5, 1.9), "pommel")
    m.cube("trident", (-0.55, -17.5, -0.55), (1.1, 24.3, 1.1), "shaft")
    m.cube("trident", (-0.8, -2.8, -0.8), (1.6, 6.4, 1.6), "grip")
    m.cube("trident", (-0.95, -19.0, -0.95), (1.9, 1.6, 1.9), "socket")
    m.cube("trident", (-3.3, -20.4, -0.7), (6.6, 1.4, 1.4), "crossbar")
    m.cube("trident", (-0.5, -28.4, -0.5), (1.0, 8.0, 1.0), "prong", tag="prong")
    m.cube("trident", (-0.3, -TRIDENT_TIP, -0.3), (0.6, 1.2, 0.6), "prong_tip")
    for sx in (1, -1):
        x0 = 2.3 if sx > 0 else -3.2
        m.cube("trident", (x0, -26.2, -0.45), (0.9, 5.8, 0.9), "prong", tag="prong")
        m.cube("trident", (x0 + 0.15, -27.2, -0.3), (0.6, 1.0, 0.6), "prong_tip")
        m.cube("trident", (x0 + (0.9 if sx > 0 else -0.6), -25.0, -0.35), (0.6, 1.5, 0.7), "barb")

    # ================================================================== das Wurfnetz in der Linken
    m.bone("net", "hand_l", pivot=(0, 2.6, 0))
    m.cube("net", (-1.4, -0.6, -1.5), (2.8, 2.8, 3.0), "net_bunch")
    for name, rot, width, length in (("net_a", (8, 35, 6), 7.6, 11.5), ("net_b", (-6, -52, -4), 6.4, 10.0)):
        m.bone(name, "net", pivot=(0, 1.8, 0), rotation=rot)
        m.cube(name, (-width / 2, 0, 0), (width, length, 0.0), "net")
        for wx in (-width / 2, 0.0, width / 2):
            m.cube(name, (wx - 0.45, length - 0.3, -0.45), (0.9, 0.9, 0.9), "weight")
    return m


# =============================================================================================
# Hilfen
# =============================================================================================

def _uv(ctx: Ctx) -> tuple[float, float]:
    return (ctx.s + 0.5) / max(ctx.w, 1), (ctx.t + 0.5) / max(ctx.h, 1)


def _lit(ctx: Ctx, key: str, v: float, strength: float = 1.0):
    """Ein leuchtendes Texel: die Farbe auch ins Leuchtbild (strength < 1: gedämpft)."""
    pal = ctx.pal[key]
    col = pal[max(0, min(len(pal) - 1, int(v * len(pal))))]
    ctx.glow = col if strength >= 1.0 else mix((0, 0, 0, 255), col, strength)
    return col


def _edge(ctx: Ctx, width: int = 1) -> bool:
    return ctx.s < width or ctx.t < width or ctx.s >= ctx.w - width or ctx.t >= ctx.h - width


def _bronze(ctx: Ctx, v: float, patina: float = 0.14, drips: bool = True):
    """Bronze des Standbilds: dunkel und warm, im Licht golden; Grünspan in Flecken (patina: wie viel) und in Bahnen, die
    wie nach Regen von oben herablaufen."""
    x, y, z = ctx.world
    n = noise3(x * 0.45, y * 0.45, z * 0.45, 4101)
    p = noise3(x * 0.32 + 7.0, y * 0.32, z * 0.32, 4102)
    if p > 1.0 - patina:
        k = (p - (1.0 - patina)) / max(patina, 1e-3)
        return pick(ctx.pal["patina"], 0.3 + k * 0.45 + _LIT.get(ctx.face, 0.0) + (n - 0.5) * 0.12)
    if drips and ctx.face in ("front", "back", "west", "east"):
        across = x if ctx.face in ("front", "back") else z
        col = int(math.floor(across * 2.0))
        r = hash01(col, ctx.face, ctx.cube.bone.name, 4104)
        if r > 0.86:
            start = hash01(col, 4105) * ctx.cube.size[1] * 0.6
            run = 1.2 + hash01(col, 4106) * 3.0
            k = (ctx.local[1] - ctx.cube.origin[1] - start) / run
            if 0.0 <= k <= 1.0:
                return mix(pick(ctx.pal["patina"], 0.42 - k * 0.15), pick(ctx.pal["bronze"], v), k * 0.7)
    col = pick(ctx.pal["bronze"], v + (n - 0.5) * 0.14 + _LIT.get(ctx.face, 0.0))
    if hash01(ctx.u, ctx.v, 4103) > 0.985:
        col = pick(ctx.pal["bronze"], v + 0.25)                                            # ein Glanzpunkt
    return col


def _crease(ctx: Ctx, v: float):
    """Eine Falte im Erz: dunkle Bronze, darin Grünspan."""
    if hash01(ctx.u, ctx.v, 4111) > 0.55:
        return pick(ctx.pal["patina"], 0.25 + v * 0.3)
    return pick(ctx.pal["bronze"], 0.12 + v * 0.1)


def _crack(ctx: Ctx, amount: float = 1.0):
    """Risse im Erz, durch die der Wind leuchtet (None, wo keiner ist)."""
    x, y, z = ctx.world
    ridge = abs(noise3(x * 0.38, y * 0.38, z * 0.38, 4121) - 0.5)
    zone = noise3(x * 0.16 + 3.0, y * 0.16, z * 0.16, 4122)
    if zone > 1.0 - 0.3 * amount and ridge < 0.022:
        return _lit(ctx, "wind", 0.62 + (0.022 - ridge) * 8.0, 0.9)
    if zone > 1.0 - 0.3 * amount and ridge < 0.04:
        return pick(ctx.pal["bronze"], 0.08)
    return None


def _copper(ctx: Ctx, v: float, patina: float = 0.12):
    """Kupfer der Rüstung: poliert, rötlich; an den Rändern ein wenig Grünspan."""
    x, y, z = ctx.world
    n = noise3(x * 0.6, y * 0.6, z * 0.6, 4131)
    p = noise3(x * 0.4 - 5.0, y * 0.4, z * 0.4, 4132)
    if p > 1.0 - patina:
        return pick(ctx.pal["patina"], 0.4 + (p - (1.0 - patina)) * 2.0 + _LIT.get(ctx.face, 0.0))
    return pick(ctx.pal["copper"], v + (n - 0.5) * 0.1 + _LIT.get(ctx.face, 0.0))


def _rivet(ctx: Ctx):
    return pick(ctx.pal["copper"], 0.92)


def _spiral(u: float, v: float, turns: float = 1.6, width: float = 0.12) -> bool:
    """Eine Windspirale um die Mitte (u, v in −1..1): liegt der Punkt auf dem Band?"""
    r = math.hypot(u, v)
    if r > 1.0 or r < 0.12:
        return False
    a = (math.atan2(v, u) / math.tau) % 1.0
    phase = (r * turns - a) % 1.0
    return phase < width * (1.4 - r * 0.6) or phase > 1.0 - width * 0.4


# =============================================================================================
# Wirbel
# =============================================================================================

def _gust(ctx: Ctx):
    """Die Windfäden eines Rings: helle, gestrichelte Bänder, die sich schräg um den Ring nach oben winden; dazwischen
    nichts (dort sieht man den Dunst der zweiten Ebene). Unten an der Spitze dichter und dünner."""
    tag = ctx.cube.tag or "ring:0"
    ring = int(tag.split(":")[1])
    x, y, z = ctx.local
    a = math.atan2(z, x) / math.tau                                                    # −0.5..0.5 rund um den Ring
    rows = ctx.cube.size[1] * 2.0                                                      # Texel-Zeilen der Seite
    ty = ctx.local[1] - ctx.cube.origin[1]                                              # 0 oben … Höhe unten
    best = 0.0
    for fam, (spacing, climb, cut) in enumerate(((3.5, 1.0, 0.42), (5.0, 2.0, 0.5))):
        phase = (ty * 2.0 / spacing + a * climb * (rows / spacing) * 0.5 + ring * 0.37 + fam * 0.5) % 1.0
        d = min(phase, 1.0 - phase) * spacing                                           # Abstand in Texeln
        if d < 0.6:
            band = math.floor(ty * 2.0 / spacing + a * climb * (rows / spacing) * 0.5 + ring * 0.37 + fam * 0.5)
            dash = noise3(a * 7.0 + fam * 3.1, band * 1.7 + ring * 5.0, fam, 4201)
            if dash > cut:
                best = max(best, min(1.0, (dash - cut) * 4.0) * (1.0 - d / 0.6 * 0.5))
    if best <= 0.0:
        if hash01(ctx.u, ctx.v, 4202) > 0.992:
            return pick(ctx.pal["sand"], 0.5 + hash01(ctx.u, ctx.v, 4203) * 0.4)        # einzelne Sandkörner
        return None
    if best > 0.7:
        return _lit(ctx, "wind", 0.58 + best * 0.3, 0.6)
    return _lit(ctx, "gust", 0.3 + best * 0.7, 0.35)


def _pebble(ctx: Ctx):
    return pick(ctx.pal["sand"], 0.35 + _LIT.get(ctx.face, 0.0) + hash01(ctx.u, ctx.v, 4211) * 0.2)


def _scrap(ctx: Ctx):
    """Ein Fetzen roten Tuchs (ausgefranst)."""
    if hash01(ctx.u, ctx.v, 4221) > 0.8:
        return None
    return pick(ctx.pal["red"], 0.45 + hash01(ctx.u, ctx.v, 4222) * 0.2)


# =============================================================================================
# Leib
# =============================================================================================

#: der Lederriemen schräg über die Brust: von der linken Schulter (+x) zur rechten Hüfte (−x), in Knochen-Koordinaten
STRAP = ((4.4, -15.0), (-3.9, -2.4))
STRAP_HALF = 0.85


def _strap(ctx: Ctx):
    """Der Lederriemen (None, wo keiner ist): Leder mit hellen Nähten an den Rändern, oben ein Kupferring."""
    if ctx.face not in ("front", "back"):
        return None
    x, y = ctx.local[0], ctx.local[1]
    (x1, y1), (x2, y2) = STRAP
    dx, dy = x2 - x1, y2 - y1
    length = math.hypot(dx, dy)
    d = ((x - x1) * dy - (y - y1) * dx) / length                                       # Abstand zur Linie
    along_ = ((x - x1) * dx + (y - y1) * dy) / length
    if ctx.face == "back":
        d = -((-x - x1) * dy - (y - y1) * dx) / length                                  # hinten gespiegelt
        along_ = ((-x - x1) * dx + (y - y1) * dy) / length
    if abs(d) > STRAP_HALF or along_ < -0.5 or along_ > length + 0.5:
        return None
    if 1.2 < along_ < 2.6 and ctx.face == "front":
        ring = math.hypot(along_ - 1.9, d)
        if 0.35 < ring < 0.85:
            return pick(ctx.pal["copper"], 0.85)                                      # der Ring, der den Galerus hält
    if abs(d) > STRAP_HALF - 0.3:
        return pick(ctx.pal["leather"], 0.82 if (ctx.s + ctx.t) % 3 == 0 else 0.2)    # Naht
    return pick(ctx.pal["leather"], 0.42 + (noise3(along_ * 0.8, d, 0, 4231) - 0.5) * 0.2 + _LIT.get(ctx.face, 0.0))


def _chest(ctx: Ctx):
    """Die Brust: Brustmuskeln (oben im Licht, darunter eine dunkle Falte), das Brustbein, die Schlüsselbeine; hinten die
    Schulterblätter und die Wirbelsäule. Risse, durch die der Wind leuchtet; der Lederriemen."""
    s = _strap(ctx)
    if s is not None:
        return s
    c = _crack(ctx, 0.8)
    if c is not None:
        return c
    u, v = _uv(ctx)
    if ctx.face == "top":
        return _bronze(ctx, 0.62)
    if ctx.face == "bottom":
        return _bronze(ctx, 0.3)
    if ctx.face == "front":
        if abs(u - 0.5) < 0.035 and 0.12 < v < 0.86:
            return _crease(ctx, 0.3)                                                    # das Brustbein
        for cu, sx in ((0.27, -1), (0.73, 1)):
            du = (u - cu) / 0.25
            bottom = 0.74 - 0.18 * du * du + 0.06 * du * sx
            if abs(du) < 1.0 and v < bottom:
                if v > bottom - 0.06:
                    return _crease(ctx, 0.2)                                            # die Falte unter dem Brustmuskel
                light = 0.68 + (0.5 - v) * 0.35 - abs(du) * 0.12
                return _bronze(ctx, light, 0.12)
        if v < 0.12:
            return _bronze(ctx, 0.72, 0.1)                                              # die Schlüsselbeine
        return _bronze(ctx, 0.5)
    if ctx.face == "back":
        if abs(u - 0.5) < 0.03:
            return _crease(ctx, 0.25)                                                   # die Wirbelsäule
        for cu in (0.28, 0.72):
            if math.hypot((u - cu) / 0.2, (v - 0.4) / 0.3) < 1.0:
                return _bronze(ctx, 0.56 + (0.4 - v) * 0.3)                             # die Schulterblätter
        return _bronze(ctx, 0.44)
    return _bronze(ctx, 0.5 - rel_y(ctx) * 0.1)


def _abdomen(ctx: Ctx):
    """Der Bauch: sechs gemeißelte Muskeln (in Falten Grünspan), die schrägen Bauchmuskeln an den Seiten; hinten der Rücken."""
    s = _strap(ctx)
    if s is not None:
        return s
    c = _crack(ctx, 1.0)
    if c is not None:
        return c
    u, v = _uv(ctx)
    if ctx.face == "front":
        if abs(u - 0.5) < 0.04:
            return _crease(ctx, 0.3)
        if u < 0.16 or u > 0.84:
            return _bronze(ctx, 0.4 - abs(u - 0.5) * 0.2)                              # die Flanken
        row = v * 3.0
        if (row % 1.0) > 0.86:
            return _crease(ctx, 0.25)
        k = row % 1.0
        return _bronze(ctx, 0.66 - k * 0.22 - abs(u - 0.5) * 0.15, 0.1)
    if ctx.face == "back":
        if abs(u - 0.5) < 0.03:
            return _crease(ctx, 0.25)
        return _bronze(ctx, 0.42 + (0.5 - abs(u - 0.5)) * 0.12)
    return _bronze(ctx, 0.42)


def _neck(ctx: Ctx):
    return _bronze(ctx, 0.46)


def _belt(ctx: Ctx):
    """Der breite Kupfergürtel: drei gewölbte Bänder, im mittleren Nieten, an den Rändern dunkel."""
    if ctx.face in ("top", "bottom"):
        return _copper(ctx, 0.3)
    band = ctx.t * 3 // max(ctx.h, 1)
    k = (ctx.t * 3 / max(ctx.h, 1)) % 1.0
    if band == 1 and ctx.s % 4 == 1 and abs(k - 0.5) < 0.2:
        return _rivet(ctx)
    v = 0.62 - abs(k - 0.35) * 0.6 - (0.0 if band == 1 else 0.06)
    return _copper(ctx, v)


def _buckle(ctx: Ctx):
    """Die Schnalle: eine runde Scheibe aus hellem Erz, darin glimmt eine Windspirale."""
    if ctx.face != "front":
        return _copper(ctx, 0.5)
    u, v = _uv(ctx)
    du, dv = (u - 0.5) * 2.0, (v - 0.5) * 2.0 * (ctx.cube.size[1] / ctx.cube.size[0])
    r = math.hypot(du, dv)
    if r > 1.0:
        return _copper(ctx, 0.36)
    if r > 0.8:
        return pick(ctx.pal["bronze"], 0.86)
    if _spiral(du / 0.8, dv / 0.8):
        return _lit(ctx, "wind", 0.7, 0.8)
    return pick(ctx.pal["bronze"], 0.5 + (1.0 - r) * 0.2)


def _loin(ctx: Ctx):
    """Der rote Schurz: Falten, oben ein Saum mit Kupfernieten, unten vom Wind zerfranst."""
    if ctx.face in ("top", "bottom"):
        return pick(ctx.pal["red"], 0.3)
    ry = rel_y(ctx)
    col = int(_uv(ctx)[0] * 12)
    end = 0.84 + hash01(col, ctx.cube.origin[2] > 0, 4301) * 0.16
    if ry > end:
        return None
    if ctx.t < 2:
        return pick(ctx.pal["copper"], 0.85) if ctx.s % 4 == 1 else pick(ctx.pal["red"], 0.2)
    x = ctx.local[0]
    fold = 0.5 + 0.5 * math.sin(x * 2.1)
    v = 0.36 + fold * 0.24 - ry * 0.12 + _LIT.get(ctx.face, 0.0)
    if ry > end - 0.05:
        v -= 0.12
    return pick(ctx.pal["red"], v + (hash01(ctx.u, ctx.v, 4302) - 0.5) * 0.05)


# =============================================================================================
# Helm
# =============================================================================================

#: das Visier (Höhen unter der Oberkante des Helms): das Gitter von … bis, die Augen dahinter (u-Mitten, Höhe)
GRILLE = (3.7, 6.5)
EYES = ((0.31, 5.0), (0.69, 5.0))


def _helm(ctx: Ctx):
    """Die Galea: oben die polierte Glocke mit einer getriebenen Windspirale an jeder Seite; unter der Krempe das Visier –
    ein feines Gitter über den Augen (dahinter leuchten sie), darunter die Kinnplatte mit einer Rippe und Nieten."""
    u, v = _uv(ctx)
    o, size = ctx.cube.origin, ctx.cube.size
    yv = ctx.local[1] - o[1]                                                            # 0 oben … 8,8 unten
    if ctx.face == "top":
        return _bronze(ctx, 0.84, 0.1, False)
    if ctx.face == "bottom":
        return pick(ctx.pal["bronze"], 0.06)
    under = yv > 3.2                                                                    # unter der Krempe
    if ctx.face == "front" and under:
        if GRILLE[0] <= yv <= GRILLE[1] and 0.1 < u < 0.9:
            if yv < GRILLE[0] + 0.5 or yv > GRILLE[1] - 0.5 or u < 0.15 or u > 0.85:
                return pick(ctx.pal["bronze"], 0.88)                                    # der Rahmen des Gitters
            if ctx.s % 3 == 0:
                return pick(ctx.pal["bronze"], 0.66 - (yv - GRILLE[0]) * 0.06)        # die Stäbe des Gitters
            for cu, cy in EYES:
                du, dy = (u - cu) * size[0], yv - cy
                if abs(du) < 0.8 and abs(dy) < 0.3:
                    return _lit(ctx, "wind", 0.96)                                      # die Augen: ein schmaler Schlitz Licht
                if abs(du) < 1.15 and abs(dy) < 0.55:
                    return _lit(ctx, "wind", 0.12, 0.4)
            return pick(ctx.pal["bronze"], 0.0)
        if abs(u - 0.5) < 0.06 and yv > GRILLE[1]:
            return _bronze(ctx, 0.9, 0.0, False)                                        # die Rippe der Kinnplatte
        if yv > GRILLE[1] + 1.2 and ctx.s % 3 == 1 and ctx.t == ctx.h - 2:
            return _rivet(ctx)
        return _bronze(ctx, 0.74 - (yv - 3.2) * 0.04, 0.12, False)
    if ctx.face in ("west", "east") and not under:
        sp = _spiral((u - 0.5) * 2.4, (yv / 3.2 - 0.55) * 2.0 * 3.2 / size[2] * 1.2, 1.3, 0.16)
        if sp:
            return _bronze(ctx, 0.95, 0.0, False)                                       # die Windspirale (getrieben)
    if under and ctx.face in ("west", "east"):
        if ctx.t >= ctx.h - 1:
            return pick(ctx.pal["bronze"], 0.85)
        if ctx.s % 5 == 2 and ctx.t == ctx.h - 3:
            return _rivet(ctx)                                                         # Nieten am Wangenstück
    return _bronze(ctx, 0.78 - (yv / size[1]) * 0.2, 0.16, False)


def _helm_top(ctx: Ctx):
    return _bronze(ctx, 0.86 if ctx.face == "top" else 0.8, 0.1, False)


def _brim(ctx: Ctx):
    """Die Krempe: gewalzter Rand (hell), Nieten rundum, unten im Schatten."""
    if ctx.face == "bottom":
        return pick(ctx.pal["bronze"], 0.12)
    if ctx.face == "top":
        u, v = _uv(ctx)
        edge = min(u, 1.0 - u, v, 1.0 - v)
        if edge < 0.05:
            return pick(ctx.pal["bronze"], 0.92)
        if edge < 0.09 and (ctx.s + ctx.t) % 4 == 0:
            return _rivet(ctx)
        return _bronze(ctx, 0.66, 0.2)
    return pick(ctx.pal["bronze"], 0.84 + _LIT.get(ctx.face, 0.0))


def _crest_base(ctx: Ctx):
    if ctx.face in ("west", "east") and ctx.s % 3 == 1:
        return _rivet(ctx)
    return _copper(ctx, 0.55)


#: Umriss des Kamms (Seitenansicht): Höhe über der Halterung (0..1) je Stelle von vorn (0) nach hinten (1) – ein Bogen
def _crest_height(f: float) -> float:
    k = 2.0 * f - 1.0
    return 0.42 + 0.58 * math.sqrt(max(0.0, 1.0 - k * k))


def _hair(ctx: Ctx, base: float = 0.45):
    """Rosshaar: senkrechte Strähnen in Rot, mal heller, mal dunkler."""
    strand = hash01(ctx.s, int(ctx.cube.origin[2] * 10), ctx.face, 4401)
    v = base + (strand - 0.5) * 0.32 + _LIT.get(ctx.face, 0.0) - rel_y(ctx) * 0.1
    return pick(ctx.pal["red"], v)


def _crest(ctx: Ctx):
    """Der Kamm aus rotem Rosshaar: ein Bogen von vorn nach hinten, oben zerzaust (einzelne Strähnen stehen ab)."""
    o, size = ctx.cube.origin, ctx.cube.size
    f = (ctx.local[2] - o[2]) / size[2]                                                 # 0 vorn … 1 hinten
    h = 1.0 - (ctx.local[1] - o[1]) / size[1]                                           # 0 unten … 1 oben
    top = _crest_height(f) - hash01(int(f * 46), 4411) * 0.08
    if ctx.face in ("west", "east"):
        if h > top:
            return None
        return _hair(ctx, 0.38 + h * 0.3)
    if ctx.face == "top":
        return None
    if ctx.face in ("front", "back"):
        if h > _crest_height(0.02 if ctx.face == "front" else 0.98):
            return None
        return _hair(ctx, 0.36)
    return _hair(ctx, 0.3)


def _crest_tail(ctx: Ctx):
    ry = rel_y(ctx)
    if ry > 0.82 + hash01(ctx.s, 4421) * 0.18:
        return None
    return _hair(ctx, 0.45 - ry * 0.1)


# =============================================================================================
# Arme
# =============================================================================================

def _arm(ctx: Ctx):
    """Der freie rechte Oberarm: Bronze, oben der runde Schultermuskel (im Licht)."""
    c = _crack(ctx, 0.6)
    if c is not None:
        return c
    ry = rel_y(ctx)
    if ctx.face == "top":
        return _bronze(ctx, 0.66)
    if ctx.face == "bottom":
        return _bronze(ctx, 0.3)
    if 0.42 < ry < 0.47:
        return _crease(ctx, 0.25)                                                       # unter dem Schultermuskel
    return _bronze(ctx, 0.62 - ry * 0.16 if ry < 0.45 else 0.5 - (ry - 0.45) * 0.1)


def _wrap(ctx: Ctx):
    """Der rechte Unterarm: Bronze, umwickelt mit schrägen Lederbinden."""
    if ctx.face in ("top", "bottom"):
        return _bronze(ctx, 0.4)
    band = (ctx.t + ctx.s // 2) % 5
    if band < 3:
        return pick(ctx.pal["leather"], 0.5 + (0.18 if band == 0 else 0.0) - (0.15 if band == 2 else 0.0) + _LIT.get(ctx.face, 0.0))
    return _bronze(ctx, 0.48)


def _cuff(ctx: Ctx):
    if ctx.face in ("top", "bottom"):
        return _copper(ctx, 0.35)
    if ctx.s % 4 == 2 and ctx.t == ctx.h // 2:
        return _rivet(ctx)
    return _copper(ctx, 0.6 - abs(rel_y(ctx) - 0.4) * 0.4)


def _hand(ctx: Ctx):
    """Die Faust aus Bronze: die Knöchel vorn, die Finger als Rillen."""
    if ctx.face == "bottom":
        return _bronze(ctx, 0.3)
    if ctx.face == "front" and ctx.t < 2:
        return _bronze(ctx, 0.78, 0.0) if ctx.s % 2 == 0 else _bronze(ctx, 0.6, 0.0)   # die Knöchel
    if ctx.face in ("front", "west", "east") and ctx.t > 2 and ctx.s % 2 == 1:
        return _crease(ctx, 0.3)
    return _bronze(ctx, 0.56)


def _manica(ctx: Ctx):
    """Die Manica: sich überlappende Kupferschienen (oben hell, unten der Schatten der nächsten), Nieten an der Seite, innen
    Lederriemen."""
    if ctx.face in ("top", "bottom"):
        return _copper(ctx, 0.4)
    k = (ctx.t % 6) / 6.0
    if ctx.face == "west" and ctx.cube.bone.name.endswith("_l") and abs(_uv(ctx)[0] - 0.5) < 0.18:
        return pick(ctx.pal["leather"], 0.45 + _LIT.get(ctx.face, 0.0))                 # der Riemen innen
    if ctx.t % 6 == 2 and ctx.s % 3 == 1 and ctx.face in ("front", "back", "east"):
        return _rivet(ctx)
    if ctx.t % 6 == 5:
        return _copper(ctx, 0.18)
    return _copper(ctx, 0.74 - k * 0.38)


def _manica_top(ctx: Ctx):
    if ctx.face == "bottom":
        return _copper(ctx, 0.2)
    if ctx.face == "top":
        u, v = _uv(ctx)
        ring = int(max(abs(u - 0.5), abs(v - 0.5)) * 6)
        return _copper(ctx, 0.75 - ring * 0.08)
    if ctx.t >= ctx.h - 1:
        return pick(ctx.pal["red"], 0.5)                                                # das rote Futter am Rand
    return _copper(ctx, 0.62 - rel_y(ctx) * 0.2)


def _galerus(ctx: Ctx):
    """Der Galerus: eine hohe Kupferplatte über der linken Schulter, aus drei getriebenen Wülsten wie die Schienen der
    Manica; der obere Rand ist gewalzt."""
    if ctx.face in ("top", "bottom"):
        return _copper(ctx, 0.4)
    if ctx.face in ("west", "east"):
        k = (ctx.t % 4) / 4.0
        if ctx.t % 4 == 3:
            return _copper(ctx, 0.16)
        if ctx.t % 4 == 1 and ctx.s in (1, ctx.w - 2):
            return _rivet(ctx)
        return _copper(ctx, 0.66 - k * 0.3, 0.16)
    return _copper(ctx, 0.42)


def _galerus_lip(ctx: Ctx):
    return pick(ctx.pal["copper"], 0.72 + _LIT.get(ctx.face, 0.0))


# =============================================================================================
# Der Sturmdreizack
# =============================================================================================

def _pommel(ctx: Ctx):
    if ctx.face in ("west", "east", "front", "back") and ctx.t == ctx.h // 2:
        return pick(ctx.pal["copper"], 0.95)
    return _copper(ctx, 0.62)


def _shaft(ctx: Ctx):
    """Der Schaft: dunkles Eschenholz, um das sich eine Kupferspirale windet."""
    if (ctx.t + ctx.s * 2) % 9 == 0:
        return pick(ctx.pal["copper"], 0.75 + _LIT.get(ctx.face, 0.0))
    grain = hash01(ctx.s, ctx.t // 3, 4501)
    return pick(ctx.pal["wood"], 0.4 + grain * 0.25 + _LIT.get(ctx.face, 0.0))


def _grip(ctx: Ctx):
    band = (ctx.t + ctx.s) % 4
    return pick(ctx.pal["leather"], 0.4 + (0.22 if band == 0 else 0.0) + _LIT.get(ctx.face, 0.0))


def _socket(ctx: Ctx):
    if ctx.t in (0, ctx.h - 1):
        return pick(ctx.pal["copper"], 0.9)
    return _copper(ctx, 0.55)


def _crossbar(ctx: Ctx):
    """Das Querstück: Bronze, in der Mitte vorn und hinten eine leuchtende Windrune."""
    u, v = _uv(ctx)
    if ctx.face in ("front", "back") and abs(u - 0.5) < 0.12:
        if (ctx.s + ctx.t) % 2 == 0 or abs(u - 0.5) < 0.04:
            return _lit(ctx, "wind", 0.72, 0.9)
    if ctx.face in ("west", "east"):
        return pick(ctx.pal["bronze"], 0.8)
    return pick(ctx.pal["bronze"], 0.66 + _LIT.get(ctx.face, 0.0) - abs(u - 0.5) * 0.2)


def _prong(ctx: Ctx):
    """Eine Zinke: helles, poliertes Erz; zur Spitze hin leuchtet sie (der Wind sammelt sich dort)."""
    o, size = ctx.cube.origin, ctx.cube.size
    f = (ctx.local[1] - o[1]) / size[1]                                                 # 0 Spitze … 1 Querstück
    if f < 0.3:
        return _lit(ctx, "wind", 0.55 + (0.3 - f) * 1.4, 0.5 + (0.3 - f) * 1.6)
    edge = ctx.s == 0
    return pick(ctx.pal["steel"], 0.6 + (0.2 if edge else 0.0) + _LIT.get(ctx.face, 0.0) - f * 0.15)


def _prong_tip(ctx: Ctx):
    return _lit(ctx, "wind", 0.9)


def _barb(ctx: Ctx):
    return pick(ctx.pal["steel"], 0.55 + _LIT.get(ctx.face, 0.0))


# =============================================================================================
# Das Wurfnetz
# =============================================================================================

def _net(ctx: Ctx):
    """Das Netz: Rautenmaschen aus Hanfseil, Knoten an den Kreuzungen; unten ein dickeres Saumseil."""
    x, y = ctx.local[0], ctx.local[1]
    size = ctx.cube.size
    if y > size[1] - 0.6:
        return pick(ctx.pal["rope"], 0.5 + (0.2 if (ctx.s + ctx.t) % 3 == 0 else 0.0))   # das Saumseil
    period = 1.9
    a = ((x + y) / period) % 1.0
    b = ((x - y) / period) % 1.0
    on_a = a < 0.18 or a > 0.94
    on_b = b < 0.18 or b > 0.94
    if on_a and on_b:
        return pick(ctx.pal["rope"], 0.82)                                              # Knoten
    if on_a or on_b:
        if hash01(ctx.u, ctx.v, 4601) > 0.97:
            return None                                                                 # eine gerissene Masche
        return pick(ctx.pal["rope"], 0.48 + (0.15 if on_a else 0.0) + _LIT.get(ctx.face, 0.0))
    return None


def _net_bunch(ctx: Ctx):
    """Das gebündelte Netz in der Hand: aufgewickelte Seile."""
    coil = (ctx.t + (ctx.s // 3)) % 3
    if coil == 2:
        return pick(ctx.pal["rope"], 0.15)
    return pick(ctx.pal["rope"], 0.42 + coil * 0.2 + _LIT.get(ctx.face, 0.0) + (hash01(ctx.u, ctx.v, 4611) - 0.5) * 0.1)


def _weight(ctx: Ctx):
    return pick(ctx.pal["lead"], 0.5 + _LIT.get(ctx.face, 0.0) + (0.25 if ctx.face == "top" else 0.0))


def materials(variant: str) -> dict:
    return {"gust": _gust, "pebble": _pebble, "scrap": _scrap, "abdomen": _abdomen, "chest": _chest, "neck": _neck,
            "belt": _belt, "buckle": _buckle, "loin": _loin, "helm": _helm, "brim": _brim, "crest_base": _crest_base,
            "crest": _crest, "crest_tail": _crest_tail, "helm_top": _helm_top, "arm": _arm, "wrap": _wrap, "cuff": _cuff, "hand": _hand,
            "manica": _manica, "manica_top": _manica_top, "galerus": _galerus, "galerus_lip": _galerus_lip, "pommel": _pommel,
            "shaft": _shaft, "grip": _grip, "socket": _socket, "crossbar": _crossbar, "prong": _prong, "prong_tip": _prong_tip,
            "barb": _barb, "net": _net, "net_bunch": _net_bunch, "weight": _weight}


def hidden(variant: str) -> list[str]:
    return []
