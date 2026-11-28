"""
Schwefelwürfel (statt des Vanilla-Schwefelwürfels): Größen wie bei Vanilla (Hülle 18³ und Kern 16³, das Junge 10³ und
8³). Alle vier Würfel liegen als eigene Knochen in EINEM Modell mit einer gemeinsamen Textur:

  * "cube"        – Hülle des großen Würfels (durchscheinend; Alpha in der Textur)
  * "inner"       – Kern des großen Würfels (deckend; wird durch den geschluckten Block ersetzt)
  * "small"       – Hülle des Jungen
  * "small_inner" – Kern des Jungen

Im Java-Teil entsteht je Ebene eine LayerDefinition aus derselben Geometrie, in der nur der jeweilige Knochen Quader
behält (MeshDefinition.getRoot().retainPartsAndChildren(Set.of(name))) – so bleibt auch "cube" für getHead() erhalten.

Aussehen: bernsteingelbes Gelee mit Glanz, Bläschen und einer Kruste aus Schwefelkristallen an Kanten und Ecken; im
Kern geschichteter Schwefel mit hellen Kristalladern. Vorn ein eigenes Gesicht (hohe, ovale Augen, gewelltes Maul).
"""
from __future__ import annotations

import math

from ..geometry import Model
from ..paint import Ctx, hash01, noise3, ramp

PALETTES = {
    "schwefelwuerfel": {
        "gel": ramp("#8a5a06", "#fff07a"), "rim": ramp("#6a3a04", "#f0b830"), "crystal": ramp("#c8960c", "#fffcc8"),
        "core": ramp("#8a4a06", "#ffd44a"), "strata": ramp("#6a3004", "#d88a24"), "bubble": ramp("#f0d070", "#fffef0"),
        "eye": ramp("#140802", "#4a2a0a"), "glint": ramp("#fff0c0", "#ffffff"), "mouth": ramp("#2a1002", "#7a4010"),
    },
}
for _p in PALETTES.values():
    _p["back"] = _p["gel"]


def build() -> Model:
    m = Model("sulfur_cube", 128, 128)
    m.bone("cube")
    m.cube("cube", (-9, -9, -9), (18, 18, 18), "shell")
    m.bone("inner")
    m.cube("inner", (-8, -8, -8), (16, 16, 16), "core")
    m.bone("small")
    m.cube("small", (-5, -5, -5), (10, 10, 10), "shell")
    m.bone("small_inner")
    m.cube("small_inner", (-4, -4, -4), (8, 8, 8), "core")
    return m


# =============================================================================================
# Malen
# =============================================================================================

def _c(r: list, i: float):
    return r[max(0, min(len(r) - 1, int(round(i))))]


def _a(col, alpha: float):
    return col[:3] + (max(0, min(255, int(alpha))),)


def _small(ctx: Ctx) -> bool:
    return ctx.cube.bone.name.startswith("small")


# Gesicht auf der Vorderseite der Hülle (Spalte, Zeile) – groß (18) und klein (10)
_FACE_BIG = {
    "eye": [(5, 6), (6, 6), (5, 7), (6, 7), (5, 8), (6, 8), (11, 6), (12, 6), (11, 7), (12, 7), (11, 8), (12, 8)],
    "glint": [(5, 6), (11, 6)],
    "mouth": [(7, 12), (8, 11), (9, 12), (10, 11)],
}
_FACE_SMALL = {
    "eye": [(2, 3), (2, 4), (7, 3), (7, 4)],
    "glint": [(2, 3), (7, 3)],
    "mouth": [(4, 6), (5, 6)],
}

# Kristallkrusten: (Seite, Spalte, Zeile) des Mittelpunkts; je Kruste eine kleine Raute aus Facetten
_CRUST_BIG = [("top", 2, 3), ("top", 14, 13), ("top", 9, 8), ("front", 15, 1), ("front", 1, 16), ("east", 2, 2),
              ("east", 14, 15), ("west", 15, 2), ("west", 3, 14), ("back", 9, 1), ("back", 15, 15), ("bottom", 4, 12)]
_CRUST_SMALL = [("top", 2, 2), ("top", 7, 7), ("front", 8, 1), ("east", 1, 8), ("west", 8, 1), ("back", 2, 8)]

# Bläschen: (Seite, Spalte, Zeile, Größe 1 = Punkt, 3 = Ring)
_BUBBLES_BIG = [("front", 3, 3, 3), ("front", 14, 8, 1), ("front", 3, 12, 1), ("east", 6, 9, 3), ("east", 12, 4, 1),
                ("west", 9, 6, 3), ("west", 13, 12, 1), ("back", 4, 9, 3), ("back", 12, 5, 1), ("top", 12, 4, 3),
                ("top", 5, 13, 1)]
_BUBBLES_SMALL = [("front", 7, 7, 1), ("east", 4, 5, 1), ("west", 5, 4, 1), ("back", 4, 4, 1), ("top", 6, 3, 1)]


def _crust(ctx: Ctx, table) -> object | None:
    """Kleine Kristallrauten: Mitte hell, obere/linke Facette heller als untere/rechte."""
    for f, cs, ct in table:
        if f != ctx.face:
            continue
        ds, dt = ctx.s - cs, ctx.t - ct
        if abs(ds) + abs(dt) <= 1:
            if (ds, dt) == (0, 0):
                return _c(ctx.pal["crystal"], 4.4)
            return _c(ctx.pal["crystal"], 3.6 if (ds < 0 or dt < 0) else 2.0)
        if abs(ds) == 1 and abs(dt) == 1 and hash01(cs, ct, ds, dt, 601) > 0.55:
            return _c(ctx.pal["crystal"], 2.8)
    return None


def _shell(ctx: Ctx):
    """Durchscheinendes Gelee: Mitte der Seiten klar, zum Rand hin fester; Kante dunkler; Glanzstreifen oben links,
    Bläschen, Kristallkruste; vorn das Gesicht."""
    pal = ctx.pal
    f, s, t, w, h = ctx.face, ctx.s, ctx.t, ctx.w, ctx.h
    small = _small(ctx)
    crust = _crust(ctx, _CRUST_SMALL if small else _CRUST_BIG)
    if crust is not None:
        return _a(crust, 255)
    if f == "front":
        face = _FACE_SMALL if small else _FACE_BIG
        if (s, t) in face["glint"]:
            return _a(_c(pal["glint"], 4), 255)
        if (s, t) in face["eye"]:
            return _a(_c(pal["eye"], 0 if t > (3 if small else 6) else 1), 250)
        if (s, t) in face["mouth"]:
            return _a(_c(pal["mouth"], 1), 245)
    # Glanzstreifen oben links (nur Seiten), oben ein Glanzfleck
    if f in ("front", "west", "east", "back"):
        streak = ((1, 1), (2, 1), (3, 1), (1, 2), (1, 3)) if not small else ((1, 1), (2, 1), (1, 2))
        if (s, t) in streak:
            return _a(_c(pal["bubble"], 5 if (s, t) == (1, 1) else 4), 225)
    elif f == "top":
        spot = ((2, h - 3), (3, h - 3), (2, h - 4)) if not small else ((1, h - 2), (2, h - 2))
        if (s, t) in spot:
            return _a(_c(pal["bubble"], 5), 215)
    for bf, bs, bt, size in (_BUBBLES_SMALL if small else _BUBBLES_BIG):
        if bf != f:
            continue
        if size == 1 and (s, t) == (bs, bt):
            return _a(_c(pal["bubble"], 4), 200)
        if size == 3 and abs(s - bs) <= 1 and abs(t - bt) <= 1 and (s, t) != (bs, bt):
            light = (s - bs) + (t - bt) < 0
            return _a(_c(pal["bubble"], 5 if light else 3), 205 if light else 160)
    # Kante fester und dunkler; nach innen klarer
    d = min(s, t, w - 1 - s, h - 1 - t)
    if d == 0:
        return _a(_c(pal["rim"], 2.6), 215)
    x, y, z = ctx.world
    n = noise3(x * 0.25, y * 0.25, z * 0.25, 611)
    i = 3.8 + (n - 0.5) * 1.0 - (0.5 if f == "bottom" else 0.0)
    alpha = 100 + max(0, 3 - d) * 22 + (n - 0.5) * 30
    if f == "bottom":
        alpha += 40
    return _a(_c(pal["gel"], i), alpha)


def _core(ctx: Ctx):
    """Kern (deckend): geschichteter Schwefel mit welligen Lagen, hellen Kristalladern und dunklen Einschlüssen."""
    pal = ctx.pal
    x, y, z = ctx.world
    small = _small(ctx)
    # wellige Schichten
    wave = math.sin((x + z) * 0.45) * 1.2 + noise3(x * 0.3, y * 0.3, z * 0.3, 621) * 1.6
    band = (y + wave) / (2.2 if small else 2.8)
    k = int(math.floor(band)) % 3
    i = (3.8, 3.0, 3.4)[k] + (noise3(x * 0.6, y * 0.6, z * 0.6, 622) - 0.5) * 0.8
    col = _c(pal["core"], i)
    if k == 1 and (band % 1.0) < 0.3:
        col = _c(pal["strata"], 2.0)                                                 # dunkle Fuge zwischen den Lagen
    # Kristalladern: schmale, schräge Bänder heller Kristalle
    vein = abs(math.sin(x * 0.55 - y * 0.35 + z * 0.4 + noise3(x * 0.2, y * 0.2, z * 0.2, 623) * 3.0))
    if vein < (0.10 if small else 0.08):
        col = _c(pal["crystal"], 4.4)
    elif vein < (0.18 if small else 0.15) and hash01(ctx.u, ctx.v, 624) > 0.5:
        col = _c(pal["crystal"], 3)
    # einzelne dunkle Einschlüsse (Gasblasen im Schwefel)
    if hash01(int(math.floor(x)), int(math.floor(y)), int(math.floor(z)), 625) > 0.975:
        col = _c(pal["strata"], 0.6)
    return col


def materials(variant: str) -> dict:
    return {"shell": _shell, "core": _core}


def hidden(variant: str) -> list[str]:
    # Vorschau: der große Würfel (das Junge liegt in derselben Textur und wird nur im Spiel getrennt gezeichnet)
    return ["small", "small_inner"]
