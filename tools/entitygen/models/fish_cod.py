"""
Dorsch und Seelachs (statt Kabeljau) auf den Knochen des Vanilla-Kabeljaus (CodModel liest nur "tail_fin"; Leib,
Kopf, Schnauze, Brust-, Rücken- und Schwanzflosse sitzen an den Vanilla-Drehpunkten, damit Schwimmen, Zappeln an Land
und Eimer genau wie bei Vanilla aussehen). Dazu eigene Teile: ein schlanker Schwanzstiel, drei getrennte Rückenflossen
und zwei Afterflossen als bemalte Flächen, beim Dorsch der Bartfaden am Kinn.

  * dorsch:   olivbrauner, dunkel gesprenkelter Rücken, gelbliche, ebenfalls gesprenkelte Flanken, cremeweißer Bauch,
              helle Seitenlinie, Bartfaden am Kinn, goldener Augenring
  * seelachs: dunkel grüngrauer Rücken, silbrige Flanken, helle Seitenlinie, heller Unterkiefer, kein Bartfaden

Der Leib ist eine Reihe höher als bei Vanilla (5 statt 4), damit Rücken, Seitenlinie und Bauch Platz haben.

Die kleinen Helfer (Licht, Flossen, Augen) nutzen auch die anderen Fische (fish_salmon, fish_puffer, fish_tropical).
"""
from __future__ import annotations

import math

from ..geometry import Model
from ..paint import Ctx, hash01, mix, pick, ramp


def _pal(**kw) -> dict:
    pal = {k: ramp(*v, steps=8) if isinstance(v, tuple) else v for k, v in kw.items()}
    pal.setdefault("back", pal["dorsal"])
    return pal


PALETTES = {
    "dorsch": _pal(dorsal=("#2e2a14", "#8e7e46"), flank=("#6a5a2c", "#e2cf8a"), belly=("#a89c80", "#fdf8ea"),
                   spot=("#1e180c", "#5a4424"), line=("#c8c0a0", "#fbf6e2"), fin=("#3a3218", "#b09c62"),
                   finedge=("#1e1c10", "#5e5636"), iris=("#5a3a08", "#e8c050"), mouth=("#2a2016", "#7a6a50")),
    "seelachs": _pal(dorsal=("#16221e", "#5e7a6e"), flank=("#5a6a6a", "#d8e2e0"), belly=("#a8b0b0", "#fcfdfc"),
                     spot=("#16221e", "#3a4a44"), line=("#c8d4d0", "#f4f8f6"), fin=("#1e2a26", "#6a8078"),
                     finedge=("#0e1614", "#3a4a44"), iris=("#4a4a40", "#d8d4c0"), mouth=("#2a2e2c", "#8a9490")),
}


def build() -> Model:
    m = Model("fish_cod", 64, 32)
    m.bone("body", pivot=(0, 22, 0))
    m.cube("body", (-1, -3, 0), (2, 5, 5), "body")                       # Rumpf (eine Reihe höher als Vanilla)
    m.cube("body", (-1, -2, 5), (2, 3, 2), "stalk")                     # schlanker Schwanzstiel
    m.bone("head", pivot=(0, 22, 0))
    m.cube("head", (-1, -3, -3), (2, 5, 3), "head")
    m.bone("barbel", "head", pivot=(0, 1, -3.5), rotation=(24, 0, 0))    # Bartfaden am Kinn (nur der Dorsch)
    m.cube("barbel", (0, 0, -0.5), (0, 2, 1), "barbel")
    m.bone("nose", pivot=(0, 22, -3))
    m.cube("nose", (-1, -2, -1), (2, 3, 1), "nose")
    m.bone("right_fin", pivot=(-1, 23, 0), rotation=(0, 0, -45))
    m.cube("right_fin", (-2, 0, -1), (2, 0, 3), "pec_fin")
    m.bone("left_fin", pivot=(1, 23, 0), rotation=(0, 0, 45))
    m.cube("left_fin", (0, 0, -1), (2, 0, 3), "pec_fin")
    m.bone("tail_fin", pivot=(0, 22, 7))
    m.cube("tail_fin", (0, -3, 0), (0, 5, 4), "tail")
    m.bone("top_fin", pivot=(0, 20, 0))
    m.cube("top_fin", (0, -4, -2), (0, 3, 10), "dorsal_fin")             # drei Rückenflossen auf einer Fläche
    m.bone("bottom_fin", pivot=(0, 24, 0.5))
    m.cube("bottom_fin", (0, 0, 0), (0, 2, 6), "anal_fin")               # zwei Afterflossen
    return m


def preview_pose(variant: str) -> dict:
    return {"tail_fin": (0, 14, 0)}


# =============================================================================================
# Gemeinsame Helfer der Fische
# =============================================================================================

def lit(ctx: Ctx, v: float) -> float:
    """Licht von oben: Oberseite heller, Unterseite und Rücken (hinten) dunkler."""
    if ctx.face == "top":
        return v + 0.07
    if ctx.face == "bottom":
        return v - 0.08
    if ctx.face == "back":
        return v - 0.04
    return v


def tone(ctx: Ctx, key: str, v: float, jitter: float = 0.03, seed: int = 0) -> tuple:
    """Farbe aus dem Verlauf ``key`` mit ganz leichter, ruhiger Körnung (keine Tarnflecken)."""
    return pick(ctx.pal[key], lit(ctx, v + (hash01(ctx.u, ctx.v, seed) - 0.5) * jitter))


def fin_membrane(ctx: Ctx, key: str, edge_key: str, along: float, across: float, base: float = 0.5,
                 ray_axis: float | None = None) -> tuple:
    """Flossenhaut: Strahlen (helle und dunklere Streifen quer zu ``ray_axis``) und ein dunklerer Saum zum Rand hin.

    ``along`` 0 = Flossenansatz, 1 = Rand; ``across`` = Lage quer zu den Strahlen (in Texeln)."""
    v = base + 0.1 * (1 - along)
    if ray_axis is not None and int(math.floor(across)) % 2 == 1:
        v -= 0.08                                                        # Flossenstrahl
    if along > 0.78:
        return pick(ctx.pal[edge_key], 0.55 + (hash01(ctx.u, ctx.v, 31) - 0.5) * 0.1)
    return pick(ctx.pal[key], v)


def eye_texel(ctx: Ctx, pupil_dark: tuple = (14, 12, 10, 255), glint: bool = False) -> tuple:
    if glint:
        return (236, 236, 226, 255)
    return pupil_dark


def jitter_dot(ctx: Ctx, a: float, b: float, density: float = 0.7, seed: int = 41) -> bool:
    """Gleichmäßig verteilte Einzelpunkte: in jedem 2x2-Feld höchstens ein Texel an zufälliger Stelle – nie verklumpt
    und nie als Schachbrett. ``a``/``b``: die beiden Flächen-Koordinaten (in Texeln)."""
    ia, ib = int(math.floor(a)), int(math.floor(b))
    block = (ia // 2, ib // 2, ctx.face)
    if hash01(*block, seed) > density:
        return False
    slot = int(hash01(*block, seed + 1) * 4)
    return (ia % 2, ib % 2) == (slot % 2, slot // 2)


def hump(z: float, centers: list[tuple[float, float, float]]) -> float:
    """Höhe (in Texeln) einer Flosse aus mehreren Buckeln: (Anfang, Ende, Höhe)."""
    best = 0.0
    for a, b, h in centers:
        if a <= z < b:
            mid = (a + b) / 2
            half = (b - a) / 2
            best = max(best, h * (1 - (abs(z - mid) / half) ** 2 * 0.9))
    return best


# =============================================================================================
# Malen
# =============================================================================================

def _variant(ctx: Ctx) -> str:
    return ctx.params.get("variant")


def _speckle(ctx: Ctx, a: float, b: float, density: float = 0.7) -> bool:
    """Leoparden-Sprenkel des Dorschs (der Seelachs bleibt ungefleckt)."""
    return _variant(ctx) == "dorsch" and jitter_dot(ctx, a, b, density)


def _flank(ctx: Ctx, wz: float, wy: float, top: float, bottom: float, line_row: float | None) -> tuple:
    """Flanke von Rumpf, Schwanzstiel und Kopf: Rücken – Flanke – Bauch je nach Höhe (0 = oben, 1 = unten)."""
    rel = (wy - top) / max(0.01, bottom - top)
    v = _variant(ctx)
    if line_row is not None and abs(wy - line_row) < 0.5:
        return pick(ctx.pal["line"], 0.6)                                    # helle Seitenlinie
    if rel < 0.25:
        col = tone(ctx, "dorsal", 0.46, seed=11)                             # Rücken
        if _speckle(ctx, wz, wy):
            col = pick(ctx.pal["spot"], 0.3)
        return col
    if line_row is None or wy < line_row:
        col = tone(ctx, "dorsal", 0.74, seed=12)                             # obere Flanke
        if _speckle(ctx, wz + 1, wy, 0.6):
            col = pick(ctx.pal["spot"], 0.55)
        return col
    if rel < 0.8:
        col = tone(ctx, "flank", 0.62 + (0.1 if v == "seelachs" else 0.0), seed=13)    # untere Flanke
        if _speckle(ctx, wz + 3, wy, 0.3):
            col = mix(col, pick(ctx.pal["spot"], 1.0), 0.6)
        return col
    return tone(ctx, "belly", 0.74, seed=14)                                 # Bauch


def _body(ctx: Ctx):
    x, y, z = ctx.world
    if ctx.face == "top":
        col = tone(ctx, "dorsal", 0.46, seed=21)
        if _speckle(ctx, x + 3, z):
            col = pick(ctx.pal["spot"], 0.32)
        return col
    if ctx.face == "bottom":
        return tone(ctx, "belly", 0.68, seed=22)
    if ctx.face == "back":
        return tone(ctx, "flank", 0.42, seed=23)
    return _flank(ctx, z, y, 19.0, 24.0, 21.5)


def _stalk(ctx: Ctx):
    x, y, z = ctx.world
    if ctx.face == "top":
        return tone(ctx, "dorsal", 0.44, seed=24)
    if ctx.face == "bottom":
        return tone(ctx, "belly", 0.62, seed=25)
    if ctx.face in ("back", "front"):
        return tone(ctx, "flank", 0.4, seed=26)
    return _flank(ctx, z, y, 20.0, 23.0, 21.5)


def _head(ctx: Ctx):
    x, y, z = ctx.world
    if ctx.face == "top":
        return tone(ctx, "dorsal", 0.5, seed=31)
    if ctx.face == "bottom":
        return tone(ctx, "belly", 0.74, seed=32)
    if ctx.face == "front":
        # das Gesicht über und unter der Schnauze
        if y < 20:
            return tone(ctx, "dorsal", 0.46, seed=33)
        return tone(ctx, "belly" if y > 23 else "dorsal", 0.7, seed=33)
    if ctx.face == "back":
        return tone(ctx, "flank", 0.4, seed=34)
    # Flanke des Kopfes: großes Auge vorn oben (Pupille mit Glanzpunkt-Ring), Kiemendeckel als Bogen am Hinterrand
    if -3 < z < -2 and 20 < y < 21:
        return eye_texel(ctx)
    if -2 < z < -1 and 20 < y < 21:
        return pick(ctx.pal["iris"], 0.66)                                   # goldener Ring hinter der Pupille
    if -3 < z < -2 and 19 < y < 20:
        return pick(ctx.pal["iris"], 0.4)                                    # Lid
    if -1 < z < 0 and 20 < y < 23.5:
        return pick(ctx.pal["dorsal"], 0.3 if y < 22 else 0.5)                # Kiemendeckel-Rand
    if 21 < y < 22:
        return tone(ctx, "dorsal", 0.7, seed=35)                             # Wange
    return _flank(ctx, z, y, 19.0, 24.0, None)


def _nose(ctx: Ctx):
    x, y, z = ctx.world
    v = _variant(ctx)
    if ctx.face == "top":
        return tone(ctx, "dorsal", 0.48, seed=41)
    if ctx.face == "bottom":
        return tone(ctx, "belly", 0.7 if v == "seelachs" else 0.6, seed=42)
    if ctx.face == "front":
        if 21 < y < 22:
            return pick(ctx.pal["mouth"], 0.62)                          # Maulspalte
        if y > 22:
            return tone(ctx, "belly" if v == "seelachs" else "flank", 0.66 if v == "seelachs" else 0.6, seed=43)
        return tone(ctx, "dorsal", 0.52, seed=44)
    # Seiten der Schnauze: oben Rücken, der Maulwinkel, unten hell
    if y < 21:
        return tone(ctx, "dorsal", 0.5, seed=45)
    if y < 22:
        return pick(ctx.pal["mouth"], 0.62)
    return tone(ctx, "belly", 0.66, seed=46)


def _barbel(ctx: Ctx):
    if _variant(ctx) != "dorsch":
        return None
    if ctx.local[1] > 1.4:
        return pick(ctx.pal["line"], 0.5)
    return pick(ctx.pal["line"], 0.72)


def _pec_fin(ctx: Ctx):
    lx, _, lz = ctx.local
    reach = abs(lx) / 2.0                                                # 0 = Ansatz, 1 = Spitze
    back = (lz + 1) / 3.0                                                # 0 = vorn, 1 = hinten
    if reach > 0.5 and back > 0.67:
        return None                                                      # abgerundete Spitze
    across = lz + 1
    return fin_membrane(ctx, "fin", "finedge", reach * 0.9 + back * 0.2, across, base=0.52, ray_axis=0)


def _dorsal_fin(ctx: Ctx):
    _, ly, lz = ctx.local
    height = hump(lz, [(-2, 1.5, 3.0), (2, 5.5, 2.6), (6, 8, 1.8)])
    above_base = -ly                                                     # 0 = Ansatz am Rücken, 2 = oben
    if above_base > height:
        return None
    col = fin_membrane(ctx, "fin", "finedge", above_base / max(0.01, height), lz + 2, base=0.48, ray_axis=0)
    if _variant(ctx) == "dorsch" and hash01(int(math.floor(lz)), int(math.floor(ly)), 51) < 0.22:
        col = pick(ctx.pal["spot"], 0.5)                                 # Flecken auf den Rückenflossen
    return col


def _anal_fin(ctx: Ctx):
    _, ly, lz = ctx.local
    height = hump(lz, [(0, 2, 1.8), (3, 6, 2.0)])
    if ly > height:
        return None
    return fin_membrane(ctx, "fin", "finedge", ly / max(0.01, height), lz, base=0.56, ray_axis=0)


def _tail(ctx: Ctx):
    _, ly, lz = ctx.local
    # schmal am Stiel, zum Rand hin voll; der Rand fast gerade, die Ecken leicht gerundet
    half = 1.5 + min(1.0, lz / 2.0)
    if abs(ly) > half:
        return None
    if lz > 3 and abs(ly) >= 2:
        return None
    along = lz / 4.0
    return fin_membrane(ctx, "fin", "finedge", along * 0.95, ly + 2.5, base=0.5, ray_axis=1)


def materials(variant: str) -> dict:
    return {"body": _body, "stalk": _stalk, "head": _head, "barbel": _barbel, "nose": _nose, "pec_fin": _pec_fin,
            "tail": _tail, "dorsal_fin": _dorsal_fin, "anal_fin": _anal_fin}


def hidden(variant: str) -> list[str]:
    return [] if variant == "dorsch" else ["barbel"]
