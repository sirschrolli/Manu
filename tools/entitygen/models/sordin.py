"""
Sordin, der Kantor der Stille (seit 1.7): Er sang einst in der Stillen Kathedrale das Lied, das die Tiefe zum Schweigen
brachte – und sang so lange, bis der Sculk durch seine Kutte wuchs und die Pfeifen der Orgel in seinen Rücken. Nun sitzt er
am Spieltisch und spielt, ohne dass ein Ton erklingt.

Eine hagere, hohe Gestalt (im Spiel 1,3-fach vergrößert, mit den Pfeifen über vier Blöcke hoch): eine lange Kutte aus
dunklem Tuch, unten in Fetzen, durch die der Sculk heraufkriecht; ein Schulterkragen mit Kupfersaum, eine Stola aus
Seegrün mit Kupferstickerei, ein Strick als Gürtel. Auf der Brust ein Medaillon wie die Fensterrose der Kathedrale (blaues
und schwarzes Glas, das leuchtet). Eine tiefe Kapuze mit spitzem Zipfel, aus der zwei Fühler aus Sculk wachsen; darin kein
Gesicht – nur Schatten, zwei matte Lichtpunkte und ein runder, singender Mund, der leuchtet. Aus dem Rücken wächst ein
Fächer aus sieben grün angelaufenen Orgelpfeifen (die Mitte am höchsten), in deren Mündern das Licht des Sculk glimmt.
Lange, dürre Arme mit weiten Ärmeln, bleiche Hände mit langen Fingern (zum Spielen gemacht). In der Rechten die
Stimmgabel: ein Griff aus Kupfer, zwei Zinken aus Hallkristall.

Knochen-Namen werden in SordinModel (Java) für die Animationen verwendet.
"""
from __future__ import annotations

import math

from ..geometry import Model
from ..paint import Ctx, hash01, hexc, mix, noise3, pick, ramp, rel_y


def stops(*colors: str) -> list:
    """Handverlesene Tonleiter."""
    return [hexc(c) for c in colors]


PALETTES = {
    "sordin": {
        "cloth": ramp("#0b0e15", "#5a6580", steps=8),
        "stole": stops("#061a1e", "#0b2f35", "#13474e", "#1e6168", "#2d7c80"),
        "copper": stops("#4a2a18", "#7a4a2c", "#a8653a", "#d08a52", "#f2b98a"),
        "verdigris": stops("#163a33", "#25594b", "#3b7d68", "#5aa58a", "#86cdb0"),
        "sculk": stops("#020d10", "#06252b", "#0b3e46", "#0f5560"),
        "glow": stops("#06424c", "#0b6f7c", "#139fac", "#29dfeb", "#8af4fb", "#e6ffff"),
        "bone": ramp("#3e434b", "#d4d8de", steps=8),
        "glass": stops("#0a1a4a", "#14328a", "#2a5cd0", "#5c9cff", "#bcdcff"),
        "lead": ramp("#050508", "#24262e"),
        "crystal": stops("#2c5a80", "#4a86b0", "#6aa6d0", "#94c8ea", "#b4e0ff", "#f2fbff"),
        "rope": stops("#1a2a2c", "#2c4446", "#46666a", "#6a8c8e"),
    },
}

_LIT = {"top": 0.08, "front": 0.03, "west": 0.0, "east": -0.03, "back": -0.06, "bottom": -0.14}

#: der Pfeifenfächer: (Neigung zur Seite in Grad, Länge, Neigung nach hinten) – von links nach rechts, die Mitte am höchsten
PIPES = ((-46.0, 11.0, 14.0), (-31.0, 15.0, 12.0), (-15.0, 19.0, 10.0), (0.0, 23.0, 8.0), (15.0, 19.0, 10.0), (31.0, 15.0, 12.0),
         (46.0, 11.0, 14.0))
PIPE_WIDTH = 1.7
#: die Stimmgabel: Griff, Zinken (Länge) und Abstand der Zinken
FORK_GRIP = 9.0
FORK_PRONG = 15.0
FORK_GAP = 1.6


def build() -> Model:
    m = Model("sordin", 128, 128)
    m.tex_res = 2
    # ------------------------------------------------------------------ Rumpf (Drehpunkt an der Hüfte)
    m.bone("body", pivot=(0, 0, 0))
    m.cube("body", (-4.2, -16, -2.4), (8.4, 16, 4.8), "torso")
    m.cube("body", (-2.1, -12.2, -2.95), (4.2, 4.2, 0.6), "medallion")                # die Fensterrose
    for sx in (1, -1):                                                                  # die Stola
        m.cube("body", (2.2 if sx > 0 else -3.6, -15.6, -2.75), (1.4, 14.2, 0.4), "stole")
    m.cube("body", (-4.6, -1.8, -2.8), (9.2, 1.5, 5.6), "cincture")                    # der Strick
    m.cube("body", (1.6, -0.5, -3.0), (1.0, 5.0, 0.8), "rope_end")                     # sein Ende hängt herab
    m.cube("body", (-5.3, -16.8, -3.0), (10.6, 4.6, 6.0), "mozzetta")                  # der Schulterkragen
    # die Kutte unterhalb der Hüfte (zwei Glieder, damit sie schwingt)
    m.bone("skirt", "body", pivot=(0, 0, 0))
    m.cube("skirt", (-4.6, 0, -2.8), (9.2, 11, 5.6), "skirt")
    m.bone("skirt_low", "skirt", pivot=(0, 11, 0))
    m.cube("skirt_low", (-5.0, 0, -3.1), (10.0, 10.6, 6.2), "skirt_low")

    # ------------------------------------------------------------------ Kopf, Kapuze, Fühler
    m.bone("head", "body", pivot=(0, -16.4, -0.2))
    m.cube("head", (-3.0, -7.0, -2.8), (6.0, 7.0, 6.0), "face")
    m.cube("head", (-3.8, -8.2, -3.5), (7.6, 8.8, 7.8), "hood")
    m.bone("hood_tip", "head", pivot=(0, -8.0, 3.2), rotation=(-38, 0, 0))
    m.cube("hood_tip", (-1.5, -1.0, 0), (3.0, 2.2, 4.4), "hood")
    for side, sx in (("l", 1), ("r", -1)):
        _grow_feeler(m, "head", f"feeler_{side}", (2.0 * sx, -7.8, 0.4), (-18.0, 0.0, 26.0 * sx), 9.0, 1.0)

    # ------------------------------------------------------------------ der Pfeifenfächer auf dem Rücken
    m.bone("pipes", "body", pivot=(0, -12.5, 2.6))
    m.cube("pipes", (-3.2, -1.6, -0.4), (6.4, 3.2, 1.4), "pipe_mount")                 # die Windlade
    for i, (lean, length, back) in enumerate(PIPES):
        pipe = f"pipe_{i}"
        m.bone(pipe, "pipes", pivot=((i - 3) * 0.95, -1.0, 0.4), rotation=(-back, 0, lean))
        m.cube(pipe, (-PIPE_WIDTH / 2, -length, -PIPE_WIDTH / 2), (PIPE_WIDTH, length, PIPE_WIDTH), "pipe")
        m.cube(pipe, (-PIPE_WIDTH / 2 - 0.25, -length - 0.6, -PIPE_WIDTH / 2 - 0.25), (PIPE_WIDTH + 0.5, 0.8, PIPE_WIDTH + 0.5),
               "pipe_rim")

    # ------------------------------------------------------------------ Arme mit weiten Ärmeln, lange Finger
    for side, sx in (("left", 1), ("right", -1)):
        arm = f"{side}_arm"
        m.bone(arm, "body", pivot=(5.0 * sx, -14.6, 0))
        m.cube(arm, (-1.5, -1.6, -1.5), (3.0, 13.0, 3.0), "sleeve_upper")
        fore = f"{side}_forearm"
        m.bone(fore, arm, pivot=(0, 11.2, 0))
        m.cube(fore, (-0.8, 0, -0.8), (1.6, 11.5, 1.6), "bone")
        m.cube(fore, (-2.0, -0.2, -2.0), (4.0, 7.2, 4.0), "sleeve")                     # der weite Ärmel
        hand = f"{side}_hand"
        m.bone(hand, fore, pivot=(0, 11.5, 0))
        m.cube(hand, (-1.1, 0, -1.0), (2.2, 2.2, 2.0), "bone")
        for k, (fx, fz) in enumerate(((-0.95, -1.0), (-0.25, -1.0), (0.45, -1.0), (0.45, 0.2))):
            m.cube(hand, (fx, 2.0, fz), (0.55, 5.4 - (1.2 if k == 3 else 0.0) - (0.4 if k == 0 else 0.0), 0.55), "finger")
    # in der Rechten die Stimmgabel (zeigt nach vorn, wenn der Arm hängt; die Haltungen drehen sie)
    m.bone("fork", "right_hand", pivot=(0, 3.4, -0.2), rotation=(90, 0, 0))
    m.cube("fork", (-0.65, -FORK_GRIP / 2, -0.65), (1.3, FORK_GRIP, 1.3), "fork_grip")
    m.cube("fork", (-1.0, FORK_GRIP / 2, -1.0), (2.0, 1.4, 2.0), "fork_metal")          # der Knauf
    m.cube("fork", (-0.9, -FORK_GRIP / 2 - 2.4, -0.9), (1.8, 2.4, 1.8), "fork_metal")   # der Hals
    w = FORK_GAP / 2 + 1.0
    m.cube("fork", (-w, -FORK_GRIP / 2 - 3.5, -0.7), (2 * w, 1.2, 1.4), "fork_metal")   # der Bügel
    for sx in (1, -1):
        x0 = FORK_GAP / 2 if sx > 0 else -FORK_GAP / 2 - 1.0
        m.cube("fork", (x0, -FORK_GRIP / 2 - 3.5 - FORK_PRONG, -0.6), (1.0, FORK_PRONG, 1.2), "prong")

    # ------------------------------------------------------------------ Beine (unter der Kutte)
    for side, sx in (("left", 1), ("right", -1)):
        leg = f"{side}_leg"
        m.bone(leg, "body", pivot=(2.0 * sx, 0.5, 0))
        m.cube(leg, (-1.1, 0, -1.1), (2.2, 12, 2.2), "limb")
        shin = f"{side}_shin"
        m.bone(shin, leg, pivot=(0, 12, 0))
        m.cube(shin, (-1.0, 0, -1.0), (2.0, 10.5, 2.0), "limb")
        m.cube(shin, (-1.25, 10.0, -3.4), (2.5, 1.5, 4.2), "foot")
    return m


def _grow_feeler(m: Model, parent: str, name: str, pivot, rot, length: float, width: float) -> None:
    """Ein Fühler aus Sculk: drei Glieder, die sich nach außen und hinten biegen, die Spitze leuchtet."""
    segs = [(width, length * 0.4, width, rot), (width * 0.8, length * 0.33, width * 0.8, (-14.0, 0.0, 12.0 * (1 if rot[2] > 0 else -1))),
            (width * 0.62, length * 0.27, width * 0.62, (-18.0, 0.0, 16.0 * (1 if rot[2] > 0 else -1)))]
    prev, piv = parent, pivot
    total = sum(s[1] for s in segs)
    acc = 0.0
    for i, (w, seg, d, r) in enumerate(segs):
        bname = f"{name}_{i}"
        m.bone(bname, prev, piv, r)
        a, b = acc / total, (acc + seg) / total
        m.cube(bname, (-w / 2, -seg, -d / 2), (w, seg, d), "feeler", tag=f"up:{a:.3f}:{b:.3f}")
        piv = (0.0, -seg + 0.25, 0.0)
        acc += seg
        prev = bname


# =============================================================================================
# Hilfen
# =============================================================================================

def _uv(ctx: Ctx) -> tuple[float, float]:
    return (ctx.s + 0.5) / max(ctx.w, 1), (ctx.t + 0.5) / max(ctx.h, 1)


def _lit(ctx: Ctx, key: str, v: float, strength: float = 1.0):
    pal = ctx.pal[key]
    col = pal[max(0, min(len(pal) - 1, int(v * len(pal))))]
    ctx.glow = col if strength >= 1.0 else mix((0, 0, 0, 255), col, strength)
    return col


def _cloth(ctx: Ctx, base: float = 0.3):
    """Dunkles Tuch: senkrechte Falten, ein wenig Rauschen, oben heller."""
    x, y, z = ctx.world
    fold = 0.5 + 0.5 * math.sin(x * 1.25 + z * 0.6)
    v = base + 0.14 * fold + _LIT.get(ctx.face, 0.0) + (noise3(x * 0.5, y * 0.25, z * 0.5, 2701) - 0.5) * 0.1
    v += (hash01(ctx.u, ctx.v, 2702) - 0.5) * 0.04
    return pick(ctx.pal["cloth"], v)


def _sculk(ctx: Ctx, amount: float):
    """Sculk, der das Tuch überzieht (amount 0–1): dunkles Türkis, darin glimmende Punkte. None, wo keiner ist."""
    x, y, z = ctx.world
    n = noise3(x * 0.45, y * 0.45, z * 0.45, 2711)
    if n > 1.0 - amount:
        r = hash01(ctx.u, ctx.v, 2712)
        if r > 0.965:
            return _lit(ctx, "glow", 0.5 + (r - 0.965) * 12.0, 0.9)
        return pick(ctx.pal["sculk"], 0.3 + (n - (1.0 - amount)) * 1.6)
    return None


def _copper(ctx: Ctx, v: float):
    """Kupfer, grün angelaufen: Grünspan in Flecken, wo er fehlt, glänzt das Metall."""
    x, y, z = ctx.world
    n = noise3(x * 0.7, y * 0.7, z * 0.7, 2721)
    if n > 0.55:
        return pick(ctx.pal["copper"], v + (n - 0.55) * 0.4)
    return pick(ctx.pal["verdigris"], v + (0.55 - n) * 0.3)


# =============================================================================================
# Kopf, Kapuze, Fühler
# =============================================================================================

def _face(ctx: Ctx):
    """Kein Gesicht – nur Schatten, zwei matte Lichtpunkte und ein runder, singender Mund, der leuchtet."""
    u, v = _uv(ctx)
    if ctx.face != "front":
        return pick(ctx.pal["cloth"], 0.04)
    d = math.hypot((u - 0.5) * 1.15, v - 0.66)
    if d < 0.2:
        if d > 0.12:
            return _lit(ctx, "glow", 0.62 + (0.2 - d) * 1.6)                           # der leuchtende Rand des Mundes
        return _lit(ctx, "glow", 0.18, 0.7)                                             # innen tief
    for cu in (0.3, 0.7):
        if abs(u - cu) < 0.06 and abs(v - 0.34) < 0.05:
            return _lit(ctx, "glow", 0.42, 0.75)                                        # zwei matte Lichtpunkte
    # Tränenspuren aus Sculk unter den Augen
    for cu in (0.3, 0.7):
        if abs(u - cu) < 0.035 and 0.4 < v < 0.52:
            return _lit(ctx, "glow", 0.22, 0.5)
    return pick(ctx.pal["cloth"], 0.03 + (1.0 - v) * 0.04)


def _hood(ctx: Ctx):
    """Die Kapuze: dunkles Tuch, vorn offen (darin der Kopf), der Rand mit einem Saum aus Kupferfäden; Sculk am Zipfel."""
    u, v = _uv(ctx)
    if ctx.face == "front" and ctx.cube.size[2] > 6.0:
        if 0.14 < u < 0.86 and v > 0.16:
            return None                                                                 # die Öffnung
        if 0.08 < u < 0.92 and v > 0.1:
            return _copper(ctx, 0.5)                                                    # der Saum
    s = _sculk(ctx, 0.12) if ctx.face in ("top", "back") else None
    if s is not None:
        return s
    return _cloth(ctx, 0.22 if ctx.face != "top" else 0.3)


def _feeler(ctx: Ctx):
    """Ein Fühler aus Sculk: dunkles Türkis, zur Spitze hin heller, die Spitze leuchtet."""
    tag = ctx.cube.tag or ""
    f = 0.5
    if tag.startswith("up:"):
        a, b = (float(x) for x in tag.split(":")[1:3])
        o, size, p = ctx.cube.origin, ctx.cube.size, ctx.local
        k = 1.0 - (p[1] - o[1]) / size[1]                                              # 0 unten … 1 oben
        f = a + (b - a) * k
    if f > 0.86:
        return _lit(ctx, "glow", 0.6 + (f - 0.86) * 2.5)
    if ctx.face in ("front", "back") and ctx.t % 3 == 1:
        return _lit(ctx, "glow", 0.25 + f * 0.25, 0.6)
    return pick(ctx.pal["sculk"], 0.35 + f * 0.6 + _LIT.get(ctx.face, 0.0))


# =============================================================================================
# Rumpf
# =============================================================================================

def _torso(ctx: Ctx):
    s = _sculk(ctx, 0.08) if ctx.face == "back" else None
    if s is not None:
        return s
    return _cloth(ctx, 0.26)


def _medallion(ctx: Ctx):
    """Das Medaillon wie die Fensterrose: ein Kupferring, darin acht Felder aus blauem und schwarzem Glas, die Mitte hell."""
    u, v = _uv(ctx)
    if ctx.face != "front":
        return _copper(ctx, 0.45)
    du, dv = u - 0.5, v - 0.5
    d = math.hypot(du, dv) * 2.0
    if d > 0.98:
        return _copper(ctx, 0.35)
    if d > 0.8:
        return _copper(ctx, 0.62)
    if d < 0.22:
        return _lit(ctx, "glass", 0.95)
    ang = (math.degrees(math.atan2(dv, du)) + 360.0) % 360.0
    sector = int((ang + 22.5) // 45.0) % 8
    edge = abs(((ang + 22.5) % 45.0) - 22.5) > 19.0
    if edge:
        return pick(ctx.pal["lead"], 0.4)
    if sector % 2 == 0:
        return _lit(ctx, "glass", 0.45 + (0.8 - d) * 0.5)
    return _lit(ctx, "glass", 0.12, 0.6)


def _stole(ctx: Ctx):
    """Die Stola: Seegrün mit Kupferstickerei (kleine Kreuze), unten Fransen."""
    if ctx.face in ("top", "bottom"):
        return pick(ctx.pal["stole"], 0.3)
    v = _uv(ctx)[1]
    if v > 0.94:
        return pick(ctx.pal["copper"], 0.6) if ctx.s % 2 == 0 else None
    if ctx.face == "front":
        if ctx.s == 0 or ctx.s == ctx.w - 1:
            return pick(ctx.pal["copper"], 0.55)
        cy = ctx.t % 6
        if (cy == 2 and ctx.w > 2) or (cy in (1, 3) and ctx.s == ctx.w // 2):
            return pick(ctx.pal["copper"], 0.7)
    return pick(ctx.pal["stole"], 0.45 + _LIT.get(ctx.face, 0.0) + (hash01(ctx.s, ctx.t, 2731) - 0.5) * 0.08)


def _cincture(ctx: Ctx):
    """Der Strick: gedreht (schräge Bänder)."""
    band = (ctx.s + ctx.t) % 3
    return pick(ctx.pal["rope"], 0.35 + (0.25 if band == 0 else 0.0) + _LIT.get(ctx.face, 0.0))


def _rope_end(ctx: Ctx):
    if _uv(ctx)[1] > 0.86:
        return pick(ctx.pal["rope"], 0.2) if ctx.s % 2 == 0 else None                  # ausgefranst
    return _cincture(ctx)


def _mozzetta(ctx: Ctx):
    """Der Schulterkragen: dunkles Tuch, unten ein Saum aus Kupfer, vorn eine Reihe Knöpfe."""
    u, v = _uv(ctx)
    if ctx.face == "bottom":
        return _cloth(ctx, 0.08)
    if ctx.face != "top" and ctx.t >= ctx.h - 1:
        return _copper(ctx, 0.55)
    if ctx.face == "front" and abs(u - 0.5) < 0.04 and ctx.t % 3 == 1:
        return _copper(ctx, 0.75)
    return _cloth(ctx, 0.3)


def _skirt(ctx: Ctx, low: bool = False):
    """Die Kutte: Falten; unten (low) kriecht der Sculk herauf, und der Saum ist zerschlissen."""
    if ctx.face == "top":
        return _cloth(ctx, 0.12)
    if ctx.face == "bottom":
        return pick(ctx.pal["sculk"], 0.2)
    ry = rel_y(ctx)
    if low:
        col_i = int(_uv(ctx)[0] * 18)
        end = 0.88 + hash01(col_i, ctx.face, 2741) * 0.12
        if ry > end:
            return None
        s = _sculk(ctx, 0.15 + ry * 0.55)
        if s is not None:
            return s
        if ry > end - 0.06:
            return _lit(ctx, "glow", 0.3, 0.5)                                           # der Saum glimmt
    if ctx.face == "front" and abs(_uv(ctx)[0] - 0.5) < 0.05:
        return _copper(ctx, 0.62) if ctx.t % 3 == 1 and not low else _cloth(ctx, 0.12)  # Knopfleiste
    return _cloth(ctx, 0.26 - ry * 0.06)


def _skirt_top(ctx: Ctx):
    return _skirt(ctx)


def _skirt_low(ctx: Ctx):
    return _skirt(ctx, True)


# =============================================================================================
# Pfeifen
# =============================================================================================

def _pipe(ctx: Ctx):
    """Eine Orgelpfeife: Kupfer, grün angelaufen, rund schattiert; unten der Mund (ein dunkler Schlitz, darunter glimmt
    das Licht des Sculk)."""
    o, size, p = ctx.cube.origin, ctx.cube.size, ctx.local
    f = (p[1] - o[1]) / size[1]                                                         # 0 oben … 1 unten
    if ctx.face == "top":
        return pick(ctx.pal["lead"], 0.2)
    if ctx.face == "bottom":
        return _copper(ctx, 0.2)
    across = abs(_uv(ctx)[0] - 0.5) * 2.0
    if ctx.face == "front" and 0.76 < f < 0.86 and across < 0.7:
        if f < 0.8:
            return pick(ctx.pal["lead"], 0.1)                                           # der Schlitz
        return _lit(ctx, "glow", 0.55 + (0.86 - f) * 3.0)                               # darunter das Licht
    v = 0.62 - across * 0.35 + _LIT.get(ctx.face, 0.0) - f * 0.1
    return _copper(ctx, v)


def _pipe_rim(ctx: Ctx):
    if ctx.face == "top":
        return pick(ctx.pal["lead"], 0.1) if 0.2 < _uv(ctx)[0] < 0.8 and 0.2 < _uv(ctx)[1] < 0.8 else _copper(ctx, 0.7)
    return _copper(ctx, 0.7)


def _pipe_mount(ctx: Ctx):
    if ctx.face == "front":
        return pick(ctx.pal["cloth"], 0.1)
    return _copper(ctx, 0.4 + _LIT.get(ctx.face, 0.0))


# =============================================================================================
# Glieder
# =============================================================================================

def _sleeve(ctx: Ctx, upper: bool = False):
    """Ärmel: dunkles Tuch, der weite Ärmel unten mit einem Kupfersaum, innen dunkel."""
    if ctx.face == "bottom":
        return pick(ctx.pal["cloth"], 0.02)
    if not upper and ctx.face != "top" and ctx.t >= ctx.h - 2:
        return _copper(ctx, 0.5 + (0.1 if ctx.t == ctx.h - 1 else 0.0))
    return _cloth(ctx, 0.28)


def _sleeve_upper(ctx: Ctx):
    return _sleeve(ctx, True)


def _sleeve_wide(ctx: Ctx):
    return _sleeve(ctx)


def _bone(ctx: Ctx):
    """Bleiche Haut über Knochen: grau, an den Gelenken dunkler."""
    v = 0.5 + _LIT.get(ctx.face, 0.0) + (hash01(ctx.u, ctx.v, 2751) - 0.5) * 0.06
    if ctx.face not in ("top", "bottom") and ctx.t % 5 == 4:
        v -= 0.12
    return pick(ctx.pal["bone"], v)


def _finger(ctx: Ctx):
    f = (ctx.local[1] - ctx.cube.origin[1]) / ctx.cube.size[1]
    v = 0.56 - f * 0.12 + _LIT.get(ctx.face, 0.0)
    if f > 0.86:
        v -= 0.18                                                                        # die Nägel dunkel
    return pick(ctx.pal["bone"], v)


def _limb(ctx: Ctx):
    return pick(ctx.pal["bone"], 0.3 + _LIT.get(ctx.face, 0.0))


def _foot(ctx: Ctx):
    if ctx.face == "top" and ctx.t < ctx.h - 1 and ctx.s % 2 == 0:
        return pick(ctx.pal["bone"], 0.42)
    return pick(ctx.pal["bone"], 0.32 + _LIT.get(ctx.face, 0.0))


# =============================================================================================
# Die Stimmgabel
# =============================================================================================

def _fork_grip(ctx: Ctx):
    """Der Griff: Kupfer mit einer Wicklung aus seegrünem Leder."""
    band = (ctx.t + ctx.s) % 3
    if band == 0:
        return pick(ctx.pal["stole"], 0.6 + _LIT.get(ctx.face, 0.0))
    return pick(ctx.pal["stole"], 0.35 + _LIT.get(ctx.face, 0.0))


def _fork_metal(ctx: Ctx):
    return pick(ctx.pal["copper"], 0.55 + _LIT.get(ctx.face, 0.0) + (0.1 if ctx.t == 0 else 0.0))


def _prong(ctx: Ctx):
    """Eine Zinke aus Hallkristall: blass, die Kanten hell, Ringe wie Wellen; die Spitze leuchtet."""
    o, size, p = ctx.cube.origin, ctx.cube.size, ctx.local
    f = (p[1] - o[1]) / size[1]                                                         # 0 Spitze … 1 Bügel
    if ctx.face == "bottom":
        return pick(ctx.pal["crystal"], 0.3)
    if ctx.face == "top":
        return _lit(ctx, "crystal", 0.95)
    if f < 0.1:
        return _lit(ctx, "crystal", 0.92)
    ring = (ctx.t % 5) == 2
    edge = ctx.s == 0 or ctx.s == ctx.w - 1
    v = 0.45 + (0.25 if edge else 0.0) + (0.2 if ring else 0.0) + (1.0 - f) * 0.1
    return _lit(ctx, "crystal", v, 0.55)


def materials(variant: str) -> dict:
    return {"face": _face, "hood": _hood, "feeler": _feeler, "torso": _torso, "medallion": _medallion, "stole": _stole,
            "cincture": _cincture, "rope_end": _rope_end, "mozzetta": _mozzetta, "skirt": _skirt_top, "skirt_low": _skirt_low,
            "pipe": _pipe, "pipe_rim": _pipe_rim, "pipe_mount": _pipe_mount, "sleeve_upper": _sleeve_upper,
            "sleeve": _sleeve_wide, "bone": _bone, "finger": _finger, "limb": _limb, "foot": _foot, "fork_grip": _fork_grip,
            "fork_metal": _fork_metal, "prong": _prong}


def hidden(variant: str) -> list[str]:
    return []
