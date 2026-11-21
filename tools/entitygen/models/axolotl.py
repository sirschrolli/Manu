"""
Molche (statt Axolotl) auf dem Skelett des Vanilla-Axolotls: dieselben Teile und Drehpunkte wie AdultAxolotlModel
(Leib mit Kopf, drei Kiemenbüschel am Kopf, vier Beine, Schwanz), damit Schwimmen, Kriechen, Schweben, Liegen und
Totstellen genau wie bei Vanilla wirken. Eine Gestalt je Vanilla-Variante, damit Eimer, Zucht und die seltene blaue
Variante unverändert bleiben:

  * grottenolm  (statt "lucy"/rosa): blassrosa, fast durchscheinender Höhlenolm mit winzigen Augen und langen,
    korallenroten Kiemen
  * wildmolch   (statt "wild"/braun): olivbraun gefleckter Molch mit orangem, schwarz getupftem Bauch
  * goldmolch   (statt "gold"): goldgelb mit feinen braunen Sprenkeln und orangen Kiemen
  * eismolch    (statt "cyan"): eisblass-türkis mit weißen Kiemen
  * blaumolch   (statt "blue", selten): tiefblau mit hellen Sternsprenkeln und violetten Kiemen, deren Spitzen leuchten

Zubehör, das Vanilla nicht kennt (Kiefer, Rückenkamm, Schwanzmuskel, Füße), steckt als zusätzliche Quader in den
Vanilla-Knochen und bewegt sich mit ihnen.
"""
from __future__ import annotations

import math

from ..geometry import Model
from ..paint import Ctx, hash01, mix, noise3, pick, ramp


def _pal(**kw) -> dict:
    pal = {k: ramp(*v) if isinstance(v, tuple) else v for k, v in kw.items()}
    pal.setdefault("back", pal["skin"])
    return pal


PALETTES = {
    "grottenolm": _pal(skin=("#b47c74", "#fff2ee"), belly=("#c89a92", "#fffaf8"), spot=("#d89890", "#f8d4cc"),
                       gill=("#8a1818", "#ff9484"), tip=("#d04a40", "#ffd0c8"), eye=("#6a4040", "#a07070"),
                       mouth=("#6a3434", "#b87070"), fin=("#c8a098", "#fff0ec")),
    "wildmolch": _pal(skin=("#2a2416", "#8c7c4c"), belly=("#a8380c", "#ffa040"), spot=("#140e08", "#3e3020"),
                      gill=("#3a1a28", "#a8607a"), tip=("#6a3048", "#e0a0b8"), eye=("#a07018", "#ffd060"),
                      mouth=("#1a120a", "#4a3a24"), fin=("#4a4228", "#a89a68")),
    "goldmolch": _pal(skin=("#8a5408", "#ffe27a"), belly=("#c88a20", "#fff0b0"), spot=("#5a2e08", "#a8601a"),
                      gill=("#a83a08", "#ffa040"), tip=("#d86a18", "#ffd890"), eye=("#2a1404", "#6a3a10"),
                      mouth=("#6a3a08", "#b8701a"), fin=("#c8961e", "#fff0a0")),
    "eismolch": _pal(skin=("#3a7a86", "#dafcff"), belly=("#6aa8b0", "#f2ffff"), spot=("#2a6070", "#84c4d0"),
                     gill=("#8ab4c0", "#ffffff"), tip=("#b8dce4", "#ffffff"), eye=("#0e2a3a", "#2a5a7a"),
                     mouth=("#2a5a66", "#6a9aa6"), fin=("#8ac8d2", "#f2ffff")),
    "blaumolch": _pal(skin=("#0c1448", "#4e6ee0"), belly=("#1a2a7a", "#7a9af0"), spot=("#9ab8ff", "#e8f0ff"),
                      gill=("#3a1470", "#b080ff"), tip=("#8a50e0", "#f0d8ff"), eye=("#0a0a1e", "#2a2a5a"),
                      mouth=("#0a0e30", "#26307a"), fin=("#2a3a9a", "#8aa4f8")),
}


def build() -> Model:
    m = Model("axolotl", 64, 64)
    # Leib (Drehpunkt wie Vanilla), Rückenkamm als Fläche über dem Rücken
    m.bone("body", pivot=(0, 19.5, 5))
    m.cube("body", (-4, -2, -9), (8, 4, 10), "body")
    m.cube("body", (0, -4, -7), (0, 2, 8), "crest", faces=("west",))
    # Kopf: flacher Oberkopf und ein etwas schmalerer Unterkiefer – dazwischen das breite Lächeln
    m.bone("head", "body", pivot=(0, 0, -9))
    m.cube("head", (-4.5, -3, -5), (9, 4, 5), "head")
    m.cube("head", (-4, 1, -5), (8, 1, 5), "jaw")
    # Kiemenbüschel: Fächer aus drei gefiederten Ästen je Seite, oben ein Kranz (Flächen, Lücken durchsichtig)
    m.bone("top_gills", "head", pivot=(0, -3, -1))
    m.cube("top_gills", (-4.5, -4, 0), (9, 4, 0), "top_gill", faces=("front",))
    m.bone("left_gills", "head", pivot=(-4, 0, -1))
    m.cube("left_gills", (-5, -6, 0), (5, 8, 0), "side_gill", faces=("front",))
    m.bone("right_gills", "head", pivot=(4, 0, -1))
    m.cube("right_gills", (0, -6, 0), (5, 8, 0), "side_gill", faces=("front",))
    # Beine: kurzer Oberschenkel und flacher Fuß mit vier Zehen
    for name, px, pz in (("right_hind_leg", -3.5, -1), ("left_hind_leg", 3.5, -1), ("right_front_leg", -3.5, -8),
                         ("left_front_leg", 3.5, -8)):
        m.bone(name, "body", pivot=(px, 1, pz))
        m.cube(name, (-1, 0, -0.5), (2, 3, 1), "leg")
        m.cube(name, (-2, 3, -3.5), (4, 0, 4), "foot", faces=("top",))
    # Schwanz: runder Muskelstrang, der sich verjüngt, und ein hoher Flossensaum (Fläche)
    m.bone("tail", "body", pivot=(0, 0, 1))
    m.cube("tail", (-1, -1.5, 0), (2, 3, 5), "tail_core")
    m.cube("tail", (-0.5, -1, 5), (1, 2, 4), "tail_core")
    m.cube("tail", (0, -3, 0), (0, 6, 12), "tail_fin", faces=("west",))
    return m


# =============================================================================================
# Malen
# =============================================================================================

def _variant(ctx: Ctx) -> str:
    return ctx.params.get("variant")


def _lit(ctx: Ctx, v: float) -> float:
    if ctx.face == "top":
        return v + 0.06
    if ctx.face == "bottom":
        return v - 0.08
    if ctx.face in ("west", "back"):
        return v - 0.03
    return v


def _blotch(x: float, y: float, z: float, size: float, seed: int, chance: float, radius: float) -> float:
    """Runde Flecken im Raum (über alle Seiten hinweg stimmig): 0 = Mitte eines Flecks, >= 1 = kein Fleck."""
    cx, cy, cz = math.floor(x / size), math.floor(y / size), math.floor(z / size)
    best = 9.0
    for dx in (-1, 0, 1):
        for dy in (-1, 0, 1):
            for dz in (-1, 0, 1):
                ix, iy, iz = cx + dx, cy + dy, cz + dz
                h = hash01(ix, iy, iz, seed)
                if h > chance:
                    continue
                px = (ix + 0.2 + 0.6 * hash01(ix, iy, iz, seed + 1)) * size
                py = (iy + 0.2 + 0.6 * hash01(ix, iy, iz, seed + 2)) * size
                pz = (iz + 0.2 + 0.6 * hash01(ix, iy, iz, seed + 3)) * size
                r = radius * (0.65 + 0.7 * hash01(ix, iy, iz, seed + 4))
                d = math.sqrt((x - px) ** 2 + (y - py) ** 2 + (z - pz) ** 2) / r
                best = min(best, d)
    return best


def _belly_mix(ctx: Ctx) -> float:
    """0 = Rücken, 1 = Bauch – nach der Höhe im Leib (Seiten gehen sanft über)."""
    if ctx.face == "top":
        return 0.0
    if ctx.face == "bottom":
        return 1.0
    y = ctx.world[1]
    return max(0.0, min(1.0, (y - 20.2) / 1.6))


def _skin(ctx: Ctx, base: float = 0.56, pattern: bool = True) -> tuple:
    """Haut mit sanftem Übergang vom Rücken zum Bauch und dem Muster der Gestalt."""
    v, (x, y, z) = _variant(ctx), ctx.world
    belly = _belly_mix(ctx)
    tone = base + (noise3(x * 0.35, y * 0.35, z * 0.35, 11) - 0.5) * 0.08
    col = pick(ctx.pal["skin"], _lit(ctx, tone))
    if belly > 0:
        bcol = pick(ctx.pal["belly"], _lit(ctx, 0.55 + (noise3(x * 0.4, y, z * 0.4, 12) - 0.5) * 0.06))
        col = mix(col, bcol, belly)
    on_belly = belly > 0.55
    if not pattern:
        return col
    if v == "wildmolch":
        if on_belly:
            if _blotch(x, y, z, 2.2, 21, 0.35, 0.55) < 1.0:
                return pick(ctx.pal["spot"], 0.25)                          # schwarze Tupfen auf dem orangen Bauch
        elif _blotch(x, y, z, 2.6, 22, 0.55, 1.05) < 1.0:
            return pick(ctx.pal["spot"], _lit(ctx, 0.45 + hash01(ctx.u, ctx.v, 23) * 0.12))   # dunkle Marmorflecken
        if not on_belly and belly > 0.2 and hash01(ctx.u, ctx.v, 24) > 0.8:
            return pick(ctx.pal["belly"], 0.7)                              # orange Pünktchen an den Flanken
    elif v == "goldmolch":
        if not on_belly and hash01(int(x * 2), int(y * 2), int(z * 2), 25) > 0.9:
            return pick(ctx.pal["spot"], _lit(ctx, 0.5))                    # feine braune Sprenkel
    elif v == "eismolch":
        if not on_belly and _blotch(x, y, z, 3.0, 26, 0.35, 0.7) < 1.0:
            return pick(ctx.pal["spot"], _lit(ctx, 0.55))                   # wenige blaugraue Flecken
    elif v == "blaumolch":
        if not on_belly and hash01(int(x * 2), int(y * 2), int(z * 2), 27) > 0.93:
            return pick(ctx.pal["spot"], 0.75)                              # helle Sternsprenkel
    elif v == "grottenolm":
        # durchscheinende Haut: ein Hauch rosiger Adern an den Flanken
        if ctx.face in ("west", "east") and abs(math.sin(z * 1.3 + y * 0.6)) < 0.12:
            return mix(col, pick(ctx.pal["spot"], 0.4), 0.5)
    return col


def _body(ctx: Ctx):
    col = _skin(ctx)
    x, y, z = ctx.world
    if ctx.face == "top" and abs(x) < 0.6:
        col = mix(col, pick(ctx.pal["skin"], 0.3), 0.35)                    # Rückenlinie
    return col


def _crest(ctx: Ctx):
    """Rückenkamm: niedriger Hautsaum mit welliger Oberkante."""
    if ctx.face not in ("west", "east"):
        return None
    s, t, w = ctx.s, ctx.t, ctx.w
    z = ctx.local[2]
    height = 1.0 + 0.6 * math.sin((z + 7) / 8 * math.pi)                  # in der Mitte am höchsten
    if (ctx.h - t) > height + 0.4 + 0.3 * math.sin(s * 1.9):
        return None
    edge = (ctx.h - t) > height - 0.3
    return pick(ctx.pal["fin"], 0.62 if edge else 0.5)


def _eye(ctx: Ctx, near: bool) -> tuple:
    """Kleines Auge an der vorderen oberen Kopfecke (vorn und an der Seite sichtbar)."""
    if _variant(ctx) == "grottenolm":
        return mix(_skin(ctx, 0.5), pick(ctx.pal["eye"], 0.5), 0.6)        # winzige, kaum sichtbare Augen
    return pick(ctx.pal["eye"], 0.95 if near else 0.3)


def _head(ctx: Ctx):
    face, s, t, w, h = ctx.face, ctx.s, ctx.t, ctx.w, ctx.h
    col = _skin(ctx, 0.58, pattern=face != "front")                         # Gesicht ohne Sprenkel
    if face == "front":
        if t == 1 and s in (0, w - 1):
            return _eye(ctx, False)
        if t == h - 1:
            return mix(col, pick(ctx.pal["mouth"], 0.4), 0.7)               # breites Lächeln
        if t == h - 2 and s in (0, w - 1):
            return mix(col, pick(ctx.pal["mouth"], 0.45), 0.6)              # Mundwinkel nach oben
        if t == 2 and s in (3, w - 4):
            return mix(col, pick(ctx.pal["mouth"], 0.4), 0.3)                # Nasenlöcher
        return col
    if face in ("west", "east"):
        front = s == w - 1 if face == "west" else s == 0
        second = s == w - 2 if face == "west" else s == 1
        if t == 1 and (front or second):
            return _eye(ctx, second)
        if t == 2 and second and _variant(ctx) != "grottenolm":
            return mix(col, pick(ctx.pal["skin"], 0.3), 0.4)                 # Schatten unter dem Auge
        if t == h - 1:
            return mix(col, pick(ctx.pal["mouth"], 0.45), 0.6)              # die Mundspalte läuft zur Seite weiter
    return col


def _jaw(ctx: Ctx):
    face = ctx.face
    if face == "bottom":
        return pick(ctx.pal["belly"], 0.58)
    col = mix(_skin(ctx, 0.6), pick(ctx.pal["belly"], 0.62), 0.55)
    if face == "front" and ctx.t == ctx.h - 1:
        return mix(col, pick(ctx.pal["belly"], 0.45), 0.5)                  # Kinn
    return col


def _frond(ctx: Ctx, px: float, py: float, ax: float, ay: float, ends: list[tuple[float, float]], fringe: float = 1.25):
    """Gefiederter Kiemenast: Stiel von (ax, ay) zu den Endpunkten, Fiedern beidseits, dazwischen Lücken."""
    best = None
    for ex, ey in ends:
        dx, dy = ex - ax, ey - ay
        length = math.hypot(dx, dy)
        k = max(0.0, min(1.0, ((px - ax) * dx + (py - ay) * dy) / (length * length)))
        qx, qy = ax + dx * k, ay + dy * k
        d = math.hypot(px - qx, py - qy)
        if best is None or d < best[0]:
            best = (d, k)
    d, k = best
    if d < 0.5:
        part, along = "stalk", k
    elif d < fringe and k > 0.15 and (int(math.floor(k * 9)) % 2 == 0):
        part, along = "fringe", k
    else:
        return None
    gill = ctx.pal["gill"]
    if along > 0.8:
        col = pick(ctx.pal["tip"], 0.55 + (along - 0.8) * 1.5)
        if _variant(ctx) == "blaumolch":
            ctx.glow = mix(col, (0, 0, 0, 255), 0.25)                        # leuchtende Kiemenspitzen
        return col
    v = 0.35 + along * 0.4 + (0.1 if part == "fringe" else 0.0)
    return pick(gill, v)


def _side_gill(ctx: Ctx):
    """Seitlicher Kiemenfächer (Fläche 5 x 8): drei gefiederte Äste vom Kopf schräg nach oben, außen und unten."""
    if ctx.face not in ("front", "back"):
        return None
    lx, ly = ctx.local[0], ctx.local[1]
    # in Richtung "außen" spiegeln: links liegt die Fläche bei negativem x, rechts bei positivem
    out = -lx if ctx.cube.bone.name == "left_gills" else lx
    return _frond(ctx, out, ly, 0.2, -0.6, [(4.6, -5.6), (4.8, -2.4), (4.3, 1.4)])


def _top_gill(ctx: Ctx):
    """Oberer Kiemenkranz (Fläche 9 x 4): eine Reihe feiner Fäden, die vom Kopf nach oben stehen, unterschiedlich
    lang, mit hellen Spitzen – zwischen den Fäden durchsichtig."""
    if ctx.face not in ("front", "back"):
        return None
    s, t, h = ctx.s, ctx.t, ctx.h
    col_i = s if ctx.face == "front" else ctx.w - 1 - s
    if col_i % 2 == 1:
        return pick(ctx.pal["gill"], 0.3) if t == h - 1 else None          # nur unten verbunden
    length = 2 + (1 if col_i % 4 == 2 else 0) + (1 if col_i in (2, 6) else 0)
    if h - 1 - t >= length:
        return None
    tip = h - 1 - t == length - 1
    if tip:
        col = pick(ctx.pal["tip"], 0.7)
        if _variant(ctx) == "blaumolch":
            ctx.glow = mix(col, (0, 0, 0, 255), 0.25)
        return col
    return pick(ctx.pal["gill"], 0.4 + (h - 1 - t) * 0.12)


def _leg(ctx: Ctx):
    col = _skin(ctx, 0.55)
    if ctx.face == "bottom":
        return pick(ctx.pal["belly"], 0.5)
    return col


_FOOT = (      # von oben, Zeile 0 = vorn: zwei lange äußere, zwei kurze innere Finger, Ballen unter dem Bein
    "o..o",
    "toot",
    ".pp.",
    ".pp.",
)


def _foot(ctx: Ctx):
    """Flacher Fuß mit vier gespreizten Fingern (zwischen den Fingern durchsichtig)."""
    lx, lz = ctx.local[0], ctx.local[2]
    r, c = int(math.floor(lz + 3.5)), int(math.floor(lx + 2))
    if not (0 <= r < 4 and 0 <= c < 4):
        return None
    k = _FOOT[r][c]
    if k == ".":
        return None
    return _skin(ctx, {"o": 0.66, "t": 0.58, "p": 0.52}[k])


def _tail_core(ctx: Ctx):
    col = _skin(ctx, 0.56)
    if ctx.face == "top" and abs(ctx.world[0]) < 0.5:
        col = mix(col, pick(ctx.pal["skin"], 0.3), 0.3)
    return col


def _tail_fin(ctx: Ctx):
    """Flossensaum des Schwanzes: oben und unten ein heller Saum mit welligem Rand, zur Spitze hin schmaler."""
    if ctx.face not in ("west", "east"):
        return None
    ly, lz = ctx.local[1], ctx.local[2]
    k = lz / 12.0                                                           # 0 am Ansatz, 1 an der Spitze
    top = -1.2 - 1.9 * math.sin(min(1.0, k * 1.15) * math.pi) * (1.0 - 0.25 * k)
    bottom = 1.2 + 1.2 * math.sin(min(1.0, k * 1.1) * math.pi) * (1.0 - 0.3 * k)
    wave = 0.25 * math.sin(lz * 2.1)
    if k > 0.97:
        return None
    if ly < top + wave or ly > bottom - wave * 0.6:
        return None
    # Muskelstrang in der Mitte, zur Spitze hin schmaler
    core = 0.9 * (1.0 - k * 0.8)
    if abs(ly + 0.05) < core:
        return _skin(ctx, 0.52)
    edge = min(ly - (top + wave), (bottom - wave * 0.6) - ly) < 0.6
    v = 0.5 + (0.12 if edge else 0.0) + 0.05 * math.sin(lz * 1.4)
    return pick(ctx.pal["fin"], v)


def materials(variant: str) -> dict:
    return {
        "body": _body, "crest": _crest, "head": _head, "jaw": _jaw, "top_gill": _top_gill, "side_gill": _side_gill,
        "leg": _leg, "foot": _foot, "tail_core": _tail_core, "tail_fin": _tail_fin,
    }


def preview_pose(variant: str) -> dict:
    """Vorschau: im Wasser schwebend wie bei Vanilla (Beine angelegt, Kiemen gespreizt)."""
    return {
        "left_gills": (0, -34, 0), "right_gills": (0, 34, 0), "top_gills": (-25, 0, 0),
        "left_hind_leg": (135, 27, 99), "right_hind_leg": (135, -27, -99),
        "left_front_leg": (45, 117, 0), "right_front_leg": (45, -117, 0),
    }
