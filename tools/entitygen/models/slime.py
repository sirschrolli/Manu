"""
Schleim (statt Vanilla-Schleim): Kern, Augen und Maul wie beim Vanilla-Schleim, dazu die Außenhülle als eigener
Knochen ("outer") im selben Modell – der Renderer zeichnet Kern und Hülle getrennt (die Hülle durchscheinend; die
Durchsichtigkeit steckt im Alphakanal der Textur). Eigenes Gesicht: schwere, halb geschlossene Lider über glänzenden
Augen und ein schiefes Maul.

Arten:
  * Sumpfschleim – trübgrünes Gelee mit Algenflocken und Bläschen, im Kern ein Zweig, ein Kiesel und Samen
  * Höhlenschleim – türkisgrau, im Kern Kristallsplitter, die schwach leuchten
"""
from __future__ import annotations

import math

from ..geometry import Model
from ..paint import Ctx, hash01, mix, noise3, ramp

PALETTES = {
    "schleim_sumpf": {
        "gel": ramp("#173a12", "#a8e070"), "core": ramp("#0e2a0c", "#5aa040"), "algae": ramp("#0e2008", "#4a6a1a"),
        "bubble": ramp("#9ad080", "#f2ffe0"), "eye": ramp("#040604", "#2a3a22"), "glint": ramp("#c8e8b0", "#ffffff"),
        "mouth": ramp("#1a0606", "#6a2a1a"), "twig": ramp("#2a1a0a", "#7a5430"), "pebble": ramp("#3a3a34", "#9a9a8a"),
        "seed": ramp("#5a4a10", "#d8b84a"), "crystal": ramp("#6a8a5a", "#e8ffd8"),
    },
    "schleim_hoehle": {
        "gel": ramp("#1a3a3e", "#9ad8d0"), "core": ramp("#10282c", "#4a8a8a"), "algae": ramp("#0e1e20", "#3a5a5a"),
        "bubble": ramp("#8ac8c8", "#f0ffff"), "eye": ramp("#04060a", "#1e2a34"), "glint": ramp("#a8e8f0", "#ffffff"),
        "mouth": ramp("#0a0a14", "#3a2a4a"), "twig": ramp("#2a2a2a", "#6a6a6a"), "pebble": ramp("#2e3236", "#8a9298"),
        "seed": ramp("#3a4a5a", "#a8b8c8"), "crystal": ramp("#4ab8c8", "#e8ffff"),
    },
}
for _p in PALETTES.values():
    _p["back"] = _p["gel"]


def build() -> Model:
    m = Model("slime", 64, 32)
    # Kern mit Gesicht (wie SlimeModel.createInnerBodyLayer: alle Teile ohne eigene Lage, Quader absolut)
    m.bone("cube")
    m.cube("cube", (-3, 17, -3), (6, 6, 6), "core")
    m.bone("right_eye")
    m.cube("right_eye", (-3.25, 18, -3.5), (2, 2, 2), "eye")
    m.bone("left_eye")
    m.cube("left_eye", (1.25, 18, -3.5), (2, 2, 2), "eye")
    m.bone("mouth")
    m.cube("mouth", (0, 21, -3.5), (1, 1, 1), "mouth")
    # Hülle (wie SlimeModel.createOuterBodyLayer)
    m.bone("outer")
    m.cube("outer", (-4, 16, -4), (8, 8, 8), "shell")
    return m


# =============================================================================================
# Malen
# =============================================================================================

def _c(r: list, i: float):
    return r[max(0, min(len(r) - 1, int(round(i))))]


def _a(col, alpha: int):
    return col[:3] + (max(0, min(255, int(alpha))),)


def _cave(ctx: Ctx) -> bool:
    return ctx.params.get("variant") == "schleim_hoehle"


# Bläschen auf der Hülle: (Seite, Spalte, Zeile, Größe 1 = Punkt, 2 = Ring)
_BUBBLES = [("front", 6, 1, 1), ("front", 6, 6, 1), ("east", 2, 5, 2), ("east", 5, 2, 1), ("west", 4, 4, 2),
            ("west", 1, 1, 1), ("back", 3, 3, 2), ("back", 6, 6, 1), ("top", 5, 2, 2), ("top", 2, 5, 1)]


def _shell(ctx: Ctx):
    """Durchscheinendes Gelee: vorn über dem Gesicht klar, sonst leicht trüb; unten trüber (Bodensatz), dunklere
    Kanten, Glanzlicht oben links, ein paar Bläschen und Flocken."""
    pal = ctx.pal
    f, s, t, w, h = ctx.face, ctx.s, ctx.t, ctx.w, ctx.h
    x, y, z = ctx.world
    if f == "bottom":
        return _a(_c(pal["gel"], 1.4), 220)
    depth = (y - 16.0) / 8.0 if f != "top" else 0.0               # 0 oben ... 1 unten
    # Glanzlicht: kurzer schräger Streifen oben links auf jeder Seite, oben ein Fleck
    if f in ("front", "west", "east", "back") and (s, t) in ((1, 1), (2, 1), (1, 2)):
        return _a(_c(pal["bubble"], 5 if (s, t) == (1, 1) else 4), 230)
    if f == "top" and (s, t) in ((1, 5), (2, 5), (1, 6)):
        return _a(_c(pal["bubble"], 5 if (s, t) == (1, 6) else 4), 215)
    if s in (0, w - 1) or t in (0, h - 1):
        return _a(_c(pal["gel"], 2.6 - depth * 0.8), 205 + depth * 20)   # Kante: fester, dunkler
    for bf, bs, bt, size in _BUBBLES:
        if bf != f:
            continue
        if size == 1 and (s, t) == (bs, bt):
            return _a(_c(pal["bubble"], 4), 200)
        if size == 2 and (s, t) in ((bs, bt), (bs + 1, bt), (bs, bt + 1), (bs + 1, bt + 1)):
            light = (s, t) == (bs, bt)
            return _a(_c(pal["bubble"], 5 if light else 3), 195 if light else 165)
    clear_face = f == "front" and 1 <= s <= 6 and 1 <= t <= 6
    if not clear_face:
        n = noise3(x * 0.6, y * 0.6, z * 0.6, 401)
        if n > 0.7 and hash01(ctx.u, ctx.v, 402) > 0.4:
            return _a(_c(pal["algae"], 2.4 + (n - 0.7) * 8), 210)      # Flocken im Gelee
    i = 3.8 - depth * 1.2 + (noise3(x * 0.35, y * 0.35, z * 0.35, 403) - 0.5) * (0.4 if clear_face else 0.9)
    alpha = (82 if clear_face else 132) + depth * 40
    return _a(_c(pal["gel"], i), alpha)


# Einschlüsse im Kern (Seite -> Pixel): Zweig, Kiesel, Samen bzw. Kristallsplitter
_CORE_SWAMP = {
    "front": {(5, 0): ("seed", 4), (0, 5): ("pebble", 3)},
    "east": {(1, 1): ("twig", 2), (2, 2): ("twig", 3), (3, 3): ("twig", 2), (4, 4): ("twig", 3), (4, 1): ("seed", 4)},
    "west": {(2, 3): ("pebble", 3), (3, 3): ("pebble", 4), (2, 4): ("pebble", 2), (3, 4): ("pebble", 3), (4, 1): ("seed", 3)},
    "back": {(1, 1): ("seed", 4), (3, 4): ("pebble", 3), (4, 4): ("pebble", 2), (4, 2): ("twig", 3), (5, 3): ("twig", 2)},
    "top": {(2, 2): ("seed", 4), (4, 3): ("pebble", 4), (3, 3): ("pebble", 3)},
    "bottom": {(2, 2): ("pebble", 2), (3, 2): ("pebble", 3)},
}
_CORE_CAVE = {
    "front": {(0, 5): ("crystal", 4), (5, 0): ("crystal", 5), (5, 5): ("crystal", 3)},
    "east": {(1, 2): ("crystal", 5), (2, 2): ("crystal", 3), (1, 3): ("crystal", 4), (4, 4): ("crystal", 5), (4, 5): ("crystal", 3)},
    "west": {(2, 1): ("crystal", 4), (3, 1): ("crystal", 5), (3, 2): ("crystal", 3), (1, 4): ("pebble", 3), (2, 4): ("pebble", 4)},
    "back": {(1, 4): ("crystal", 5), (2, 4): ("crystal", 3), (2, 3): ("crystal", 4), (4, 1): ("crystal", 4)},
    "top": {(1, 1): ("crystal", 5), (2, 1): ("crystal", 3), (4, 4): ("crystal", 4), (3, 3): ("pebble", 3)},
    "bottom": {(3, 3): ("crystal", 3)},
}


def _core(ctx: Ctx):
    """Dichter Kern mit Einschlüssen; vorn ein schiefes Maul unter den Augen."""
    pal = ctx.pal
    f, s, t = ctx.face, ctx.s, ctx.t
    table = _CORE_CAVE if _cave(ctx) else _CORE_SWAMP
    hit = table.get(f, {}).get((s, t))
    if hit is not None:
        key, i = hit
        col = _c(pal[key], i)
        if key == "crystal" and i >= 4:
            ctx.glow = _c(pal["crystal"], i - 1)
        return col
    if f == "front":
        # Maul: schiefe Linie, rechts am Maul-Würfel hochgezogen
        if (t == 4 and s == 2) or (t == 3 and s == 4):
            return _c(pal["mouth"], 1)
        # Wangen: eine Stufe heller unter den Augen
        if t == 3 and s in (0, 5):
            return _c(pal["core"], 4)
    x, y, z = ctx.world
    n = noise3(x * 0.5, y * 0.5, z * 0.5, 411)
    i = 2.6 + (n - 0.5) * 1.0 - (y - 17.0) / 6.0 * 0.7
    if f == "top":
        i += 0.6
    if f == "bottom":
        i -= 0.8
    return _c(pal["core"], i)


def _eye(ctx: Ctx):
    """Augen mit schwerem Lid: obere Hälfte Lid (Kern-Farbe), unten das dunkle, glänzende Auge."""
    pal = ctx.pal
    f, s, t = ctx.face, ctx.s, ctx.t
    if f == "front":
        # großes dunkles Auge mit einem Glanzpunkt oben links (Licht von links oben)
        return _c(pal["glint"], 4) if (s, t) == (0, 0) else _c(pal["eye"], 0)
    if f == "top":
        return _c(pal["core"], 3.6)
    if f in ("west", "east"):
        return _c(pal["core"], 3.0) if t == 0 else _c(pal["eye"], 1)
    return _c(pal["core"], 2.4)


def _mouth(ctx: Ctx):
    pal = ctx.pal
    if ctx.face == "front":
        return _c(pal["mouth"], 1)
    return _c(pal["mouth"], 0.5)


def materials(variant: str) -> dict:
    return {"shell": _shell, "core": _core, "eye": _eye, "mouth": _mouth}


def hidden(variant: str) -> list[str]:
    return []
