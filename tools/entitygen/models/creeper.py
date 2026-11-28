"""
Kriecher (statt Creeper) auf dem Skelett des Vanilla-Creepers – gleiche Teile, Drehpunkte und Grundquader, damit die
Vanilla-Bewegungen und die Aufladungs-Aura (CREEPER_ARMOR) passen. Der Kriecher ist ein Pflanzenwesen: ein Leib aus
Blattschuppen auf vier Wurzelbeinen, ein grimmiges Gesicht mit schräg gestellten Augen und einem Dornenmaul.

Über den Grundquadern liegt je eine etwas größere Hülle (Kopf und Leib), die nur dort bemalt ist, wo etwas absteht
(Blattkrone, Ranken, Stacheln, Reif, Flechten); dazu Zierteile je Art (Blüte, Eiszapfen, Leuchtpilze).

Arten (nach Biom):
  * Waldkriecher      – Moos- und Blattgrün, Blattkrone, Ranken, Gänseblümchen
  * Wüstenkriecher    – Kaktusrippen, helle Stacheln, rosa Kaktusblüte
  * Schneekriecher    – Tannennadeln mit Reif, Schneehaube, Eiszapfen unter dem Kinn
  * Höhlenkriecher    – Flechten auf Stein, schwach leuchtende Pilze und Augen
  * Dschungelkriecher – sattes, glänzendes Laub, große Blüte, Lianen
"""
from __future__ import annotations

import math

from ..geometry import Model
from ..paint import Ctx, hash01, mix, noise3, ramp

_C = {
    "hole": ramp("#050604", "#2a2a20"),
    "tooth": ramp("#8a8460", "#f2ecc8"),
    "bark": ramp("#1e160c", "#7a6040"),
    "root": ramp("#2a2014", "#b09a70"),
}

PALETTES = {
    "kriecher_wald": {**_C, "leaf": ramp("#14300e", "#86c04a"), "vein": ramp("#0c1e08", "#3e6a22"),
                      "moss": ramp("#2a4a12", "#a8c860"), "flower": ramp("#c8c0b0", "#ffffff"),
                      "flower_core": ramp("#a87808", "#ffd84a"), "stem": ramp("#1a3a10", "#5a8a30")},
    "kriecher_wueste": {**_C, "leaf": ramp("#1e3a1a", "#8ab862"), "vein": ramp("#0e200c", "#3a5e2a"),
                        "spine": ramp("#a89a70", "#fff6d8"), "fuzz": ramp("#c8c0a8", "#ffffff"),
                        "flower": ramp("#a8205a", "#ff8ac0"), "flower_core": ramp("#c89a10", "#ffe060"),
                        "stem": ramp("#2a4a20", "#6a9a48"), "sand": ramp("#8a6a3a", "#e8d09a")},
    "kriecher_schnee": {**_C, "leaf": ramp("#0c2018", "#4e8a62"), "vein": ramp("#06120c", "#24443a"),
                        "frost": ramp("#8aa8c0", "#ffffff"), "ice": ramp("#6a9ac0", "#e8f8ff")},
    "kriecher_hoehle": {**_C, "leaf": ramp("#26302a", "#8e9c8c"), "vein": ramp("#121614", "#3e4840"),
                        "lichen": ramp("#323a18", "#9aa45a"), "rust": ramp("#6a3010", "#e0904a"),
                        "glow": ramp("#0a5a5a", "#8affe8"), "cap": ramp("#0e3a44", "#5ad8d0"),
                        "stem": ramp("#4a5a5a", "#c8d8d0")},
    "kriecher_dschungel": {**_C, "leaf": ramp("#0a3410", "#5ee048"), "vein": ramp("#062008", "#1e6a1c"),
                           "flower": ramp("#c8380a", "#ffc860"), "flower_core": ramp("#7a0a52", "#ff6ad0"),
                           "stem": ramp("#0e3a0c", "#3a8a28"), "liana": ramp("#1e2a0a", "#6a7a2a")},
}
for _p in PALETTES.values():
    _p["back"] = _p["leaf"]

# Zierteile je Art (Knochen-Namen); was nicht dabei ist, bleibt durchsichtig
GEAR = {
    "kriecher_wald": {"flower"},
    "kriecher_wueste": {"flower"},
    "kriecher_schnee": {"icicles"},
    "kriecher_hoehle": {"mushrooms"},
    "kriecher_dschungel": {"flower"},
}
ALL_GEAR = set().union(*GEAR.values())


def build() -> Model:
    m = Model("creeper", 128, 64)
    m.bone("head", pivot=(0, 6, 0))
    m.cube("head", (-4, -8, -4), (8, 8, 8), "head")
    m.bone("head_shell", "head")                                                     # Krone, Stacheln, Reif, Flechten
    m.cube("head_shell", (-4.5, -8.5, -4.5), (9, 9, 9), "head_shell")
    m.bone("flower", "head", pivot=(1.5, -8.6, 0.5), rotation=(-6, 0, 10))            # Blüte auf dem Scheitel
    m.cube("flower", (-0.5, -2, -0.5), (1, 2, 1), "flower_stem")
    m.cube("flower", (-1.5, -3, -1.5), (3, 1, 3), "flower_bloom")
    m.bone("icicles", "head", pivot=(0, 0, 0))                                      # Eiszapfen unter dem Kinn
    m.cube("icicles", (-3.5, 0, -3.8), (1, 3, 1), "icicle")
    m.cube("icicles", (-0.6, 0, -3.6), (1, 2, 1), "icicle")
    m.cube("icicles", (2.2, 0, -3.9), (1, 4, 1), "icicle")
    m.bone("mushrooms", "head", pivot=(-1.5, -8, 1.0))                             # Leuchtpilze auf dem Scheitel
    m.cube("mushrooms", (-0.5, -2, -0.5), (1, 2, 1), "mush_stem")
    m.cube("mushrooms", (-1.5, -3, -1.5), (3, 1, 3), "mush_cap")
    m.cube("mushrooms", (2.5, -1, -2.5), (1, 1, 1), "mush_stem")
    m.cube("mushrooms", (2, -2, -3), (2, 1, 2), "mush_cap")

    m.bone("body", pivot=(0, 6, 0))
    m.cube("body", (-4, 0, -2), (8, 12, 4), "body")
    m.bone("body_shell", "body")                                                     # Ranken, Stacheln, Nadeln, Flechten
    m.cube("body_shell", (-4.5, 0, -2.5), (9, 12, 5), "body_shell")

    for name, pivot in (("right_hind_leg", (-2, 18, 4)), ("left_hind_leg", (2, 18, 4)),
                        ("right_front_leg", (-2, 18, -4)), ("left_front_leg", (2, 18, -4))):
        m.bone(name, pivot=pivot)
        m.cube(name, (-2, 0, -2), (4, 6, 4), "leg")
    return m


# =============================================================================================
# Hilfen
# =============================================================================================

def _variant(ctx: Ctx) -> str:
    return ctx.params.get("variant")


def _kind(ctx: Ctx) -> str:
    return _variant(ctx).split("_", 1)[1]


def _hidden(ctx: Ctx) -> bool:
    bone = ctx.cube.bone
    while bone is not None:
        if bone.name in ALL_GEAR:
            return bone.name not in GEAR[_variant(ctx)]
        bone = bone.parent
    return False


def _c(r: list, i: float):
    """Farbe Nummer i (0 = dunkel ... 5 = hell) aus einer Rampe."""
    return r[max(0, min(len(r) - 1, int(round(i))))]


def _perim(ctx: Ctx) -> float:
    """Lage rings um einen Quader (vorn -> links -> hinten -> rechts), damit Muster über die Kanten laufen."""
    ox, oy, oz = ctx.cube.origin
    w, h, d = ctx.cube.size
    lx, ly, lz = ctx.local
    f = ctx.face
    if f == "front":
        return lx - ox
    if f == "east":
        return w + (lz - oz)
    if f == "back":
        return w + d + (ox + w - lx)
    if f == "west":
        return 2 * w + d + (oz + d - lz)
    return lx - ox


def _cells(x: float, y: float, z: float, cell: float, stretch: float, seed: int) -> tuple[float, float, float]:
    """Zellen im Raum (Blätter, Schuppen, Steinplatten): (Zufallswert der Zelle, Randnähe 0..1, Lage in der Zelle
    von oben (-1) nach unten (+1)). ``stretch`` > 1 macht die Zellen höher als breit."""
    ys = y / stretch
    cx, cy, cz = math.floor(x / cell), math.floor(ys / cell), math.floor(z / cell)
    best = second = 1e9
    best_v = 0.0
    best_dy = 0.0
    for dx in (-1, 0, 1):
        for dy in (-1, 0, 1):
            for dz in (-1, 0, 1):
                ix, iy, iz = cx + dx, cy + dy, cz + dz
                px = (ix + hash01(ix, iy, iz, seed)) * cell
                py = (iy + hash01(ix, iy, iz, seed + 1)) * cell
                pz = (iz + hash01(ix, iy, iz, seed + 2)) * cell
                dist = (px - x) ** 2 + (py - ys) ** 2 + (pz - z) ** 2
                if dist < best:
                    second = best
                    best = dist
                    best_v = hash01(ix, iy, iz, seed + 3)
                    best_dy = (ys - py) / cell
                elif dist < second:
                    second = dist
    edge = (math.sqrt(second) - math.sqrt(best)) / (cell * 0.5)
    return best_v, max(0.0, min(1.0, edge)), max(-1.0, min(1.0, best_dy * 1.6))


# =============================================================================================
# Haut je Art
# =============================================================================================

# Blattschindeln (Reihen um eine halbe Breite versetzt): oben vom Licht getroffen, unten eine Spitze mit Schatten
_SHINGLE = [[4, 4, 4, 3],
            [3, 4, 3, 2],
            [1, 2, 2, 1]]
# große, glänzende Dschungelblätter mit Mittelrippe
_BIGLEAF = [[3, 4, 5, 5, 4, 3],
            [3, 4, 4, 4, 4, 2],
            [2, 3, 2, 3, 3, 2],
            [1, 1, 2, 2, 1, 1]]


def _tile(p: float, y: float, tile: list, seed: int) -> tuple[float, int, int]:
    """Wert aus einem versetzten Kachelmuster plus leichte Abweichung je Kachel; (Wert, Zeile in der Kachel, Kachel-Id)."""
    th, tw = len(tile), len(tile[0])
    row = int(math.floor(y / th))
    off = (tw // 2) if row % 2 else 0
    col = int(math.floor((p + off) / tw))
    sx = int(math.floor(p + off)) % tw
    sy = int(math.floor(y)) % th
    jitter = (hash01(row, col, seed) - 0.5) * 0.9
    return tile[sy][sx] + jitter, sy, hash(row * 131 + col)


def _skin(ctx: Ctx):
    """Grundmuster von Kopf und Leib je Art (laufen über die Kanten, weil rings um den Quader gezählt wird)."""
    kind = _kind(ctx)
    x, y, z = ctx.world
    pal = ctx.pal
    f = ctx.face
    if f == "bottom":
        return _c(pal["vein"], 1)
    top = f == "top"
    p = (x + 16.0) if top else _perim(ctx)
    yy = (z + 16.0) if top else y
    if kind in ("wald", "schnee"):
        v, sy, _ = _tile(p, yy, _SHINGLE, 41)
        if kind == "schnee":
            if top:
                return _c(pal["frost"], 3.6 + (hash01(int(x * 2), int(z * 2), 71) - 0.5) * 1.4)
            col = _c(pal["leaf"], v - 0.6)
            rel = ctx.local[1] - ctx.cube.origin[1]
            upper = rel < 2.0 and ctx.cube.bone.name in ("head", "body")
            if sy == 0 and (upper or hash01(int(math.floor(p)), int(math.floor(yy)), 74) > 0.62):
                col = mix(col, _c(pal["frost"], 3.4), 0.72)          # Reif auf einzelnen Schuppenkanten
            return col
        col = _c(pal["leaf"], v)
        n = noise3(x * 0.3, y * 0.3, z * 0.3, 52)
        if n > 0.66 and sy < 2:
            col = mix(col, _c(pal["moss"], 2.4 + (v - 2) * 0.8), 0.75)     # Moospolster
        return col
    if kind == "dschungel":
        v, sy, _ = _tile(p, yy, _BIGLEAF, 43)
        return _c(pal["leaf"], v)
    if kind == "wueste":
        # Kaktusrippen: helle Kante, Flanke, dunkle Furche; dazu weiße Flaumpolster am Grat
        if top:
            r = max(abs(ctx.local[0] - (ctx.cube.origin[0] + ctx.cube.size[0] / 2)),
                    abs(ctx.local[2] - (ctx.cube.origin[2] + ctx.cube.size[2] / 2)))
            return _c(pal["leaf"], 4.2 - (int(r) % 2) * 1.2)
        k = int(math.floor(p)) % 3
        col = _c(pal["leaf"], (4, 3, 1.2)[k] + (hash01(int(p), int(y), 61) - 0.5) * 0.5)
        if k == 0 and int(math.floor(y)) % 3 == 1:
            col = _c(pal["fuzz"], 4)
        return col
    # hoehle: ruhige Steinplatten, wenige Flechtenflecken
    val, edge, dy = _cells(x, y, z, 3.0, 1.0, 81)
    i = 2.5 + val * 1.3 - dy * 0.5
    if edge < 0.16:
        i = 0.8
    col = _c(pal["leaf"], i)
    n = noise3(x * 0.22 + 5, y * 0.22, z * 0.22, 82)
    if n > 0.74 and edge >= 0.16:
        col = _c(pal["lichen"], 2.4 + (n - 0.74) * 6 + val)
    elif n < 0.2 and edge >= 0.16 and ctx.cube.bone.name in ("head", "body"):
        col = _c(pal["rust"], 2.2 + val)
    return col


# =============================================================================================
# Teile
# =============================================================================================

# Gesicht (8x8, Spalte 0 = vom Betrachter aus links): schräg gestellte, grimmige Augen und ein Dornenmaul
_FACE = [
    "........",
    "........",
    ".A....A.",
    ".AB..BA.",
    "..B..B..",
    "........",
    ".MMMMMM.",
    ".MTMMTM.",
]


def _head(ctx: Ctx):
    if ctx.face == "front":
        ch = _FACE[ctx.t][ctx.s]
        if ch != ".":
            pal = ctx.pal
            if ch == "T":
                return _c(pal["tooth"], 4)
            if ch == "A":
                return _c(pal["hole"], 0)
            if ch == "B":
                if _kind(ctx) == "hoehle" and ctx.t == 3:
                    ctx.glow = _c(pal["glow"], 4)
                    return _c(pal["glow"], 5)
                return _c(pal["hole"], 1)
            # Maul: Oberkante am dunkelsten (Tiefe), unten etwas heller
            return _c(pal["hole"], 0 if ctx.t == 6 else 1)
        col = _skin(ctx)
        # Brauenwulst: über den Augen eine Stufe dunkler (wirkt eingesunken), unter dem Maul ein Schatten
        if ctx.t == 1 and ctx.s in (1, 2, 5, 6) or ctx.t == 2 and ctx.s in (2, 5):
            col = mix(col, _c(ctx.pal["vein"], 1), 0.45)
        return col
    return _skin(ctx)


def _body(ctx: Ctx):
    col = _skin(ctx)
    # Leib unten etwas dunkler (Schatten unter dem Kopf oben ebenfalls)
    if ctx.face in ("front", "back", "west", "east"):
        ly = ctx.local[1]
        if ly < 1.0:
            col = mix(col, _c(ctx.pal["vein"], 0), 0.4)
        elif ly > 10.0:
            col = mix(col, _c(ctx.pal["vein"], 1), 0.25)
    return col


def _leg(ctx: Ctx):
    """Beine: oben Leib, unten ein Bündel Wurzeln (zwei Pixel breit, dazwischen Schatten) mit hellen Spitzen."""
    pal = ctx.pal
    kind = _kind(ctx)
    ly = ctx.local[1]
    if ctx.face == "top":
        return _c(pal["vein"], 1)
    if ctx.face == "bottom":
        return _c(pal["root"], 1.2 + (ctx.s + ctx.t) % 2 * 1.4)
    p = int(math.floor(_perim(ctx)))
    k = p % 3
    start = 3.0 + (1.0 if hash01(p, 94) > 0.6 else 0.0)          # manche Wurzeln setzen tiefer an
    if ly < start or k == 2 and ly < 4.0:
        col = _skin(ctx)
        if ly >= start - 1.0:
            col = mix(col, _c(pal["vein"], 0), 0.3)
        return col
    if k == 2:
        return _c(pal["bark"], 1.0)
    if ly >= 5.0:
        if kind == "wueste":
            return _c(pal["sand"], 4 - k * 0.6)
        if kind == "schnee":
            return _c(pal["frost"], 4 - k * 0.6)
        return _c(pal["root"], 4.4 - k * 0.8)
    return _c(pal["bark"], 3.6 - k * 0.8 + (hash01(p, int(ly), 95) - 0.5) * 0.5)


# ---------------------------------------------------------------------------------------------
# Hüllen (nur bemalt, wo etwas absteht)
# ---------------------------------------------------------------------------------------------

def _head_shell(ctx: Ctx):
    kind = _kind(ctx)
    pal = ctx.pal
    f, s, t = ctx.face, ctx.s, ctx.t
    if f == "bottom":
        return None
    if kind in ("wald", "dschungel"):
        # Blattkrone: oben dichtes Laub, an den Seiten ein ungleich langer Blattsaum; vorn nur über der Stirn
        if f == "top":
            x, y, z = ctx.world
            val, edge, _ = _cells(x, y, z, 2.4, 1.0, 103)
            return _c(pal["leaf"], 1.2 if edge < 0.2 else 3.2 + val * 1.5)
        length = 1 + int(hash01(s, f, kind, 101) * (2.0 if f == "front" else 3.2))
        if f == "front":
            length = min(length, 2)
            if 1 <= s <= 7 and t == 1 and _FACE[2][min(7, max(0, s - 1))] == "A":
                length = 1                                     # über den Augen kurz
        if t < length:
            i = 3 - t * 1.2 + (hash01(s, t, f, 102) - 0.5)
            if t == length - 1:
                i -= 0.8
            return _c(pal["leaf"], i)
        # Ranke/Liane: hängt an einer Seite herab
        if f in ("west", "east") and s == (2 if f == "west" else 6) and t < 8:
            return _c(pal["stem" if kind == "wald" else "liana"], 2.5 + (t % 2))
        return None
    if kind == "wueste":
        # einzelne Stacheln stehen aus den Rippen heraus
        if f == "top":
            return None
        if (s % 3 == 0) and (t % 3 == 1) and hash01(s, t, f, 111) > 0.25:
            if f == "front" and _FACE[min(7, t)][min(7, max(0, s - 1))] != ".":
                return None
            return _c(pal["spine"], 4 if hash01(s, t, f, 112) > 0.5 else 3)
        return None
    if kind == "schnee":
        # Schneehaube: oben ganz weiß, an den Seiten Tropfen und Reifkristalle
        if f == "top":
            v = 4 + (hash01(s, t, 121) - 0.5) * 1.2
            return _c(pal["frost"], v)
        drip = 1 + int(hash01(s, f, 122) * 2.4)
        if f == "front":
            drip = 1
        if t < drip:
            return _c(pal["frost"], 4 - t * 0.8)
        if f != "front" and t >= 3 and hash01(s, t, f, 123) > 0.93:
            return _c(pal["ice"], 5)
        return None
    # hoehle: einzelne Flechtenbüschel stehen ab (oben ein Moospolster)
    if f == "top":
        n = noise3(ctx.world[0] * 0.5, 0, ctx.world[2] * 0.5, 131)
        return _c(pal["lichen"], 2.2 + n * 2.0) if n > 0.6 else None
    n = noise3(ctx.world[0] * 0.4 + 3, ctx.world[1] * 0.4, ctx.world[2] * 0.4, 132)
    if n > 0.76 and t <= 3 and not (f == "front" and t >= 1):
        return _c(pal["lichen"], 2.0 + (n - 0.76) * 10)
    return None


def _body_shell(ctx: Ctx):
    kind = _kind(ctx)
    pal = ctx.pal
    f, s, t = ctx.face, ctx.s, ctx.t
    if f in ("top", "bottom"):
        return None
    p = int(math.floor(_perim(ctx)))
    y = ctx.local[1]
    if kind == "wald":
        # Ranke windet sich schräg um den Leib, mit kleinen Blättern
        path = int(round(y * 1.6)) % 28
        if p == path or p == (path + 14) % 28:
            return _c(pal["stem"], 2.5 + (int(y) % 2))
        if (p == path + 1 or p == (path + 15) % 28) and int(y) % 3 == 0:
            return _c(pal["leaf"], 4)
        return None
    if kind == "dschungel":
        # Lianen hängen an den Seiten und hinten herab (mit Blättchen), drei Blüten sitzen am Leib
        iy = int(math.floor(y))
        for fp, fy in _JUNGLE_BLOOMS:
            dx, dyy = p - fp, iy - fy
            if (dx, dyy) == (0, 0):
                return _c(pal["flower_core"], 4)
            if (abs(dx), abs(dyy)) in ((1, 0), (0, 1)):
                return _c(pal["flower"], 4.2 if dyy <= 0 else 3.4)
        if p in (11, 18, 25) and hash01(p, int(y // 4), 141) > 0.15:
            return _c(pal["liana"], 2.5 + (int(y) % 2) * 0.8)
        if p in (12, 19, 26) and int(y) % 4 == 2:
            return _c(pal["leaf"], 4.5)
        return None
    if kind == "wueste":
        if p % 3 == 0 and int(y) % 3 == 1 and hash01(p, int(y), 151) > 0.2:
            return _c(pal["spine"], 4 if hash01(p, int(y), 152) > 0.5 else 3)
        return None
    if kind == "schnee":
        # Nadelbüschel mit Reifspitzen stehen ab
        if (p + int(y)) % 4 == 0 and hash01(p, int(y), 161) > 0.45:
            return _c(pal["frost"], 4) if y < 6 else _c(pal["leaf"], 3)
        return None
    # hoehle: kleine Gruppen leuchtender Pilzköpfchen, dazwischen Flechtenbüschel
    for gx, gy in _GLOW_SPOTS:
        if abs(p - gx) <= 1 and abs(y - gy) <= 1.0:
            if (p - gx, int(math.floor(y - gy + 0.5))) in ((0, 0), (1, 1), (-1, 1), (0, -1)) or (p == gx and abs(y - gy) < 0.6):
                ctx.glow = _c(pal["glow"], 4)
                return _c(pal["glow"], 5)
    n = noise3(ctx.world[0] * 0.4, ctx.world[1] * 0.4, ctx.world[2] * 0.4, 171)
    if n > 0.78:
        return _c(pal["lichen"], 2.0 + (n - 0.78) * 10)
    return None


# Blüten am Leib des Dschungelkriechers (Lage rings um den Leib, Zeile)
_JUNGLE_BLOOMS = [(2, 2), (17, 6), (7, 9)]

# Pilzgruppen am Leib (Lage rings um den Leib, Höhe)
_GLOW_SPOTS = [(2, 8.5), (12, 3.5), (20, 9.5), (25, 5.5)]


# ---------------------------------------------------------------------------------------------
# Zierteile
# ---------------------------------------------------------------------------------------------

def _flower_stem(ctx: Ctx):
    if _hidden(ctx):
        return None
    return _c(ctx.pal["stem"], 2 + (ctx.t % 2))


def _flower_bloom(ctx: Ctx):
    """Blüte (3x1x3): oben ein Kreuz aus Blütenblättern um die Mitte, die Ecken frei."""
    if _hidden(ctx):
        return None
    pal = ctx.pal
    s, t = ctx.s, ctx.t
    if ctx.face in ("top", "bottom"):
        if s != 1 and t != 1:
            return None
        if s == 1 and t == 1:
            return _c(pal["flower_core"], 4 if ctx.face == "top" else 2)
        return _c(pal["flower"], 5 if ctx.face == "top" else 3)
    if s != 1:
        return None
    return _c(pal["flower"], 4)


def _icicle(ctx: Ctx):
    if _hidden(ctx):
        return None
    pal = ctx.pal
    if ctx.face == "bottom":
        return _c(pal["ice"], 5)
    rel = (ctx.local[1] - ctx.cube.origin[1]) / ctx.cube.size[1]
    return _c(pal["ice"], 4.5 - rel * 2.0 + (0.6 if ctx.face in ("front", "west") else 0))


def _mush_stem(ctx: Ctx):
    if _hidden(ctx):
        return None
    return _c(ctx.pal["stem"], 3 + (ctx.s % 2) * 0.6)


def _mush_cap(ctx: Ctx):
    if _hidden(ctx):
        return None
    pal = ctx.pal
    if ctx.face == "bottom":
        return _c(pal["stem"], 2)                              # Lamellen
    if ctx.face == "top" and (ctx.s + ctx.t) % 2 == 0:
        col = _c(pal["glow"], 5)
        ctx.glow = _c(pal["glow"], 4)
        return col
    col = _c(pal["cap"], 3 + (ctx.s % 2) * 0.6)
    ctx.glow = _c(pal["cap"], 2)
    return col


def materials(variant: str) -> dict:
    return {
        "head": _head,
        "head_shell": _head_shell,
        "body": _body,
        "body_shell": _body_shell,
        "leg": _leg,
        "flower_stem": _flower_stem,
        "flower_bloom": _flower_bloom,
        "icicle": _icicle,
        "mush_stem": _mush_stem,
        "mush_cap": _mush_cap,
    }


def hidden(variant: str) -> list[str]:
    return sorted(ALL_GEAR - GEAR[variant])
