"""
Windgeist (statt Breeze) – Wind-Ebene: dasselbe Knochengerüst wie breeze.py (gleiche Namen und Drehpunkte, damit
ein BreezeModel beide Netze backen kann), aber nur die drei Windwirbel tragen Quader (Vanilla-Größen: unten 5x7x5,
Mitte 12/8/5 breit, oben 18/12/5 breit – ineinander geschachtelt).

Das Bild wandert im Spiel waagrecht (RenderTypes.breezeWind verschiebt U jedes Bild), daher sieht jede Fläche mit
der Zeit ihre ganze Bildzeile. Das Muster ist deshalb eine Funktion der Bildlage (u, v) allein, nahtlos in beide
Richtungen (Periode = Bildbreite bzw. -höhe): durchscheinende, leicht gewellte Windstreifen mit hellem Kern, darunter
ein Hauch von Dunst, dazu ein paar mitgerissene Blätter und Staubkörner.

Wichtig für das Bild im Spiel: :func:`full_texture` füllt das GANZE Bild (auch die Stellen zwischen den Flächen, die
beim Wandern durch die Flächen ziehen). Die Flächen-Maler (:func:`materials`) liefern an ihren Stellen genau dasselbe,
für Vorschauen und als Rückfall.
"""
from __future__ import annotations

import math
import random

from PIL import Image

from . import breeze
from ..geometry import Model
from ..paint import Ctx, hexc

W, H = 128, 128

PALETTES = {
    "windgeist": {
        "haze": hexc("#c4e2ec"), "edge": hexc("#a6d8ea"), "core": hexc("#f2fcff"),
        "dust": hexc("#8a8270"), "leaf": hexc("#6e9a3a"), "leaf_dark": hexc("#3e6422"),
        "leaf2": hexc("#c08a2e"), "leaf2_dark": hexc("#7a4e18"),
    },
}
for _p in PALETTES.values():
    _p["back"] = [_p["haze"]]


def build() -> Model:
    base = breeze.build()
    m = Model("breeze_wind", W, H)
    # gleiches Gerüst (Namen, Drehpunkte, Winkel), ohne die Quader des Körpers

    def copy(b, parent: str):
        for ch in b.children:
            m.bone(ch.name, parent, pivot=ch.pivot, rotation=ch.rotation)
            copy(ch, ch.name)

    copy(base.root, "root")
    m.cube("wind_bottom", (-2.5, -7, -2.5), (5, 7, 5), "wind")
    m.cube("wind_mid", (-6, -6, -6), (12, 6, 12), "wind")
    m.cube("wind_mid", (-4, -6, -4), (8, 6, 8), "wind")
    m.cube("wind_mid", (-2.5, -6, -2.5), (5, 6, 5), "wind")
    m.cube("wind_top", (-9, -8, -9), (18, 8, 18), "wind")
    m.cube("wind_top", (-6, -8, -6), (12, 8, 12), "wind")
    m.cube("wind_top", (-2.5, -8, -2.5), (5, 8, 5), "wind")
    return m


# =============================================================================================
# Das nahtlose Windbild
# =============================================================================================

def _lanes(seed: int = 7301):
    rng = random.Random(seed)
    lanes = []
    n = 21
    for k in range(n):
        lanes.append({
            "v": (k + rng.uniform(-0.25, 0.25)) * H / n,
            "amp": rng.uniform(0.5, 2.2),
            "m": rng.choice((1, 1, 2, 3)),
            "phase": rng.uniform(0, math.tau),
            "dash_n": rng.choice((1, 2, 2, 3, 4)),
            "dash_phase": rng.uniform(0, math.tau),
            "dash_cut": rng.uniform(-0.55, 0.0),
            "width": rng.uniform(0.6, 1.7),
            "peak": rng.uniform(140, 225),
        })
    return lanes


_LANES = _lanes()


def _specks(seed: int = 7311):
    """Blätter und Staubkörner: (u, v, Art) – Blätter sitzen auf Windstreifen."""
    rng = random.Random(seed)
    out = []
    for k, lane in enumerate(_LANES):
        if k % 3 == 1:
            u = rng.randrange(W)
            out.append((u, _center(lane, u), "leaf" if k % 2 else "leaf2"))
    for _ in range(26):
        out.append((rng.randrange(W), rng.uniform(0, H), "dust"))
    return out


def _center(lane: dict, u: float) -> float:
    return lane["v"] + lane["amp"] * math.sin(math.tau * lane["m"] * u / W + lane["phase"])


_SPECKS = None


def _vdist(a: float, b: float) -> float:
    d = abs(a - b) % H
    return min(d, H - d)


def wind_rgba(u: int, v: int, pal: dict) -> tuple[int, int, int, int] | None:
    """Farbe des Windbilds an (u, v) – periodisch in u (Breite) und v (Höhe)."""
    u = u % W
    v = v % H
    x = u + 0.5
    y = v + 0.5
    # Dunst: ein Hauch, in großen weichen Wellen
    haze_a = 7 + 7 * math.sin(math.tau * (2 * x / W + y / H)) + 5 * math.sin(math.tau * (3 * x / W - 2 * y / H) + 1.3)
    best_a = 0.0
    best_core = 0.0
    for lane in _LANES:
        d = _vdist(y, _center(lane, x))
        if d > lane["width"] + 1.0:
            continue
        dash = math.sin(math.tau * lane["dash_n"] * x / W + lane["dash_phase"]) - lane["dash_cut"]
        if dash <= 0:
            continue
        fade = min(1.0, dash * 2.2)                                     # weiche Enden der Streifen
        prof = max(0.0, 1.0 - d / (lane["width"] + 1.0))
        a = lane["peak"] * prof * fade
        if a > best_a:
            best_a = a
            best_core = prof * fade
    if best_a < haze_a:
        if haze_a < 8:
            return None
        h = pal["haze"]
        return h[0], h[1], h[2], int(haze_a)
    edge, core = pal["edge"], pal["core"]
    t = max(0.0, min(1.0, (best_core - 0.35) / 0.45))
    col = tuple(round(edge[i] + (core[i] - edge[i]) * t) for i in range(3))
    return col[0], col[1], col[2], int(min(235, best_a + haze_a * 0.4))


def _speck_at(u: int, v: int, pal: dict):
    global _SPECKS
    if _SPECKS is None:
        _SPECKS = _specks()
    for su, sv, kind in _SPECKS:
        du = (u - su) % W
        dv = int(math.floor(v - sv)) % H
        if kind == "dust":
            if du == 0 and dv == 0:
                return pal["dust"][:3] + (210,)
        else:
            # kleines Blatt 3x2: Spitze links, Stiel rechts unten
            light, dark = (pal["leaf"], pal["leaf_dark"]) if kind == "leaf" else (pal["leaf2"], pal["leaf2_dark"])
            shape = {(0, 0): light, (1, 0): light, (2, 0): dark, (1, 1): dark, (2, 1): light}
            c = shape.get((du, dv))
            if c is not None:
                return c[:3] + (240,)
    return None


def full_texture(variant: str = "windgeist") -> tuple[Image.Image, Image.Image]:
    """Das ganze Windbild (W x H), nahtlos; Leuchtbild leer. Dieses Bild als breeze_wind-Textur verwenden."""
    pal = PALETTES[variant]
    img = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    px = img.load()
    for v in range(H):
        for u in range(W):
            c = _speck_at(u, v, pal) or wind_rgba(u, v, pal)
            if c is not None:
                px[u, v] = c
    return img, Image.new("RGBA", (W, H), (0, 0, 0, 0))


def _wind(ctx: Ctx):
    # dieselbe Farbe wie im ganzen Bild an dieser Stelle (paint_model hellt Ober-/Unterseiten noch leicht auf bzw. ab)
    return _speck_at(ctx.u, ctx.v, ctx.pal) or wind_rgba(ctx.u, ctx.v, ctx.pal)


def materials(variant: str) -> dict:
    return {"wind": _wind}


def hidden(variant: str) -> list[str]:
    return []
