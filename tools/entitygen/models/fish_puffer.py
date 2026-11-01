"""
Kugelfisch in drei Stufen auf den Knochen der drei Vanilla-Modelle. Die Klassen lesen nur die Brustflossen
(PufferfishSmallModel: left_fin/right_fin, PufferfishMidModel und PufferfishBigModel: left_blue_fin/right_blue_fin) und
setzen deren zRot in jedem Bild neu. Alle Vanilla-Knochen bleiben mit Namen, Eltern, Drehpunkten und Drehungen
erhalten; die Stachelflächen sind größer, dazu kommen eigene Teile.

  * klein  (puffState 0): ockergelber Fisch mit Glotzaugen auf den oberen Kanten, hellem Schnabel, Schwanzstiel und
                          -fächer, kleinen Flossen, einer braunen Sattelbinde und weißem Bauch – Stacheln angelegt
  * mittel (puffState 1): halb aufgeblasen – runde Kugel (Kern mit sechs Beulen), kurze Stacheln an den Kanten (nicht
                          an den unteren Längskanten), zwei braune Sättel, weißer Bauch, Glotzaugen an den Seiten
  * groß   (puffState 2): ganz aufgeblasen – große Kugel, lange Stacheln mit dunklen Spitzen an allen zwölf Kanten und
                          quer über Rücken und Bauch, große glänzende Augen

Je Stufe eine eigene Geometrie und Textur:
  build_small() -> kugelfisch_klein (32x32), build_mid() -> kugelfisch_mittel (64x32),
  build_big()   -> kugelfisch_gross (64x64); build() ist die kleine Stufe.
stage_module("klein" | "mittel" | "gross") liefert je Stufe ein Modul-Objekt (PALETTES, build, materials) für
write_model. Eigene Knochen (mittel/groß): top_left_spines, top_right_spines, bottom_left_spines,
bottom_right_spines (Stacheln der Längskanten; die unteren nur groß) und tail_fin; Augen, Schnabel und Beulen sind Quader von "body".
"""
from __future__ import annotations

import math
from types import SimpleNamespace

from ..geometry import Model
from ..paint import Ctx, pick, ramp
from .fish_cod import fin_membrane, tone

PAL = {k: ramp(*v, steps=8) for k, v in {
    "skin": ("#6a4a0e", "#f8d468"),       # Ockergelb
    "saddle": ("#2e1a0a", "#7a5226"),     # braune Sättel
    "belly": ("#c4bca4", "#fffdf4"),      # weißer Bauch
    "cream": ("#a89058", "#f4e4b0"),      # Übergang zum Bauch
    "spine": ("#a08654", "#fbf2d2"),      # Stachel, hell
    "tip": ("#2a1a0a", "#7a5428"),        # Stachelspitze, dunkel
    "fin": ("#8a6a24", "#f4dc8c"),        # Flossen, hell ocker
    "finedge": ("#5a4214", "#b08e44"),
    "iris": ("#8a6418", "#fff0a8"),       # heller, goldener Augenring
    "beak": ("#b8ac88", "#fbf6e2"),
}.items()}

STAGES = ("klein", "mittel", "gross")

# Mitte und halbe Ausdehnung der Kugel je Stufe (Welt-Koordinaten) und die Sättel (Welt-z von … bis)
# eye: Auge der rechten Seite (y von, y bis, z von, z bis) – darum ein heller Ring hinten und unten
_GEO = {
    "klein": SimpleNamespace(cy=21.5, hx=1.5, hy=1.5, hz=2.0, saddles=[(0.0, 1.0)], saddle_low=0.1, eye=None),
    "mittel": SimpleNamespace(cy=19.5, hx=3.5, hy=3.5, hz=3.5, saddles=[(-1.5, 0.5), (1.5, 3.5)], saddle_low=-0.1,
                              eye=(18, 19, -1.5, -0.5)),
    "gross": SimpleNamespace(cy=18.0, hx=5.0, hy=5.0, hz=5.0, saddles=[(-2.0, 0.0), (2.0, 4.0)], saddle_low=-0.2,
                             eye=(15, 17, -3, -1)),
}

PALETTES = {"kugelfisch_klein": PAL}


# =============================================================================================
# Geometrie
# =============================================================================================

def build_small() -> Model:
    """puffState 0 – für PufferfishSmallModel (liest left_fin, right_fin)."""
    m = Model("fish_puffer_small", 32, 32)
    m.bone("body", pivot=(0, 23, 0))
    m.cube("body", (-1.5, -3, -2), (3, 3, 3), "skin")
    m.cube("body", (-1, -2.5, 1), (2, 2, 1), "skin")                    # Schwanzstiel
    m.cube("body", (-1, -2, -2.5), (2, 1, 0.5), "beak")                 # Schnabel
    m.cube("body", (0, -4, 0), (0, 1, 1), "fin")                        # kleine Rückenflosse
    m.cube("body", (0, 0, 0), (0, 1, 1), "fin")                         # kleine Afterflosse
    m.bone("right_eye", pivot=(0, 20, 0))
    m.cube("right_eye", (-2, -0.5, -1.5), (1, 1, 1), "eye")             # Glotzaugen auf den oberen Kanten
    m.bone("left_eye", pivot=(0, 20, 0))
    m.cube("left_eye", (1, -0.5, -1.5), (1, 1, 1), "eye")
    m.bone("back_fin", pivot=(0, 22, 1.5))
    m.cube("back_fin", (0, -2, 0.5), (0, 3, 2), "tail")                 # Schwanzfächer (senkrecht)
    m.bone("right_fin", pivot=(-1.5, 22, -1.5))
    m.cube("right_fin", (-1, 0, 0), (1, 0, 2), "fin")
    m.bone("left_fin", pivot=(1.5, 22, -1.5))
    m.cube("left_fin", (0, 0, 0), (1, 0, 2), "fin")
    return m


def _ball(m: Model, core: float, bump: float, top: float):
    """Kern (Würfel, Kantenlänge 2*core, Oberkante bei ``top`` relativ zum Drehpunkt) und sechs flache Beulen
    (Kantenlänge 2*bump, eine Einheit hoch) – zusammen eine Kugel mit abgeschrägten Kanten."""
    cy = top + core                                                     # Mitte (relativ)
    m.cube("body", (-core, top, -core), (2 * core, 2 * core, 2 * core), "skin")
    m.cube("body", (-core - 1, cy - bump, -bump), (1, 2 * bump, 2 * bump), "skin")      # rechts
    m.cube("body", (core, cy - bump, -bump), (1, 2 * bump, 2 * bump), "skin")           # links
    m.cube("body", (-bump, cy - bump, -core - 1), (2 * bump, 2 * bump, 1), "skin")      # vorn
    m.cube("body", (-bump, cy - bump, core), (2 * bump, 2 * bump, 1), "skin")           # hinten
    m.cube("body", (-bump, top - 1, -bump), (2 * bump, 1, 2 * bump), "skin")            # oben
    m.cube("body", (-bump, top + 2 * core, -bump), (2 * bump, 1, 2 * bump), "skin")     # unten


def _edge_spines(m: Model, core: float, top_y: float, bottom_y: float, out: float, bottom: bool = True):
    """Stacheln der Längskanten (entlang z) – eigene Knochen, um 45° nach außen gekippt."""
    length = 2 * core
    m.bone("top_right_spines", pivot=(-core, top_y, 0), rotation=(0, 0, -45))
    m.cube("top_right_spines", (0, -out, -core), (0, out, length), "spines")
    m.bone("top_left_spines", pivot=(core, top_y, 0), rotation=(0, 0, 45))
    m.cube("top_left_spines", (0, -out, -core), (0, out, length), "spines")
    if not bottom:
        return
    m.bone("bottom_right_spines", pivot=(-core, bottom_y, 0), rotation=(0, 0, 45))
    m.cube("bottom_right_spines", (0, 0, -core), (0, out, length), "spines")
    m.bone("bottom_left_spines", pivot=(core, bottom_y, 0), rotation=(0, 0, -45))
    m.cube("bottom_left_spines", (0, 0, -core), (0, out, length), "spines")


def build_mid() -> Model:
    """puffState 1 – für PufferfishMidModel (liest left_blue_fin, right_blue_fin)."""
    m = Model("fish_puffer_mid", 64, 32)
    m.bone("body", pivot=(0, 22, 0))
    _ball(m, 2.5, 1.5, -5)
    m.cube("body", (-4, -4, -1.5), (0.5, 1, 1), "eye")                  # Glotzaugen an den Seiten
    m.cube("body", (3.5, -4, -1.5), (0.5, 1, 1), "eye")
    m.cube("body", (-1, -2, -4), (2, 1, 0.5), "beak")
    m.bone("right_blue_fin", pivot=(-2.5, 18, -1.5))
    m.cube("right_blue_fin", (-2, 0.5, 1), (2, 0, 2), "fin")            # hinter dem Auge
    m.bone("left_blue_fin", pivot=(2.5, 18, -1.5))
    m.cube("left_blue_fin", (0, 0.5, 1), (2, 0, 2), "fin")
    m.bone("top_front_fin", pivot=(0, 17, -2.5), rotation=(45, 0, 0))
    m.cube("top_front_fin", (-2.5, -1, 0), (5, 1, 0), "spines")
    m.bone("top_back_fin", pivot=(0, 17, 2.5), rotation=(-45, 0, 0))
    m.cube("top_back_fin", (-2.5, -1, 0), (5, 1, 0), "spines")
    m.bone("right_front_fin", pivot=(-2.5, 22, -2.5), rotation=(0, -45, 0))
    m.cube("right_front_fin", (-1, -5, 0), (1, 5, 0), "spines")
    m.bone("right_back_fin", pivot=(-2.5, 22, 2.5), rotation=(0, 45, 0))
    m.cube("right_back_fin", (-1, -5, 0), (1, 5, 0), "spines")
    m.bone("left_back_fin", pivot=(2.5, 22, 2.5), rotation=(0, -45, 0))
    m.cube("left_back_fin", (0, -5, 0), (1, 5, 0), "spines")
    m.bone("left_front_fin", pivot=(2.5, 22, -2.5), rotation=(0, 45, 0))
    m.cube("left_front_fin", (0, -5, 0), (1, 5, 0), "spines")
    m.bone("bottom_back_fin", pivot=(-2.5, 22, 2.5), rotation=(45, 0, 0))
    m.cube("bottom_back_fin", (0, 0, 0), (5, 1, 0), "spines")
    m.bone("bottom_front_fin", pivot=(0, 22, -2.5), rotation=(-45, 0, 0))
    m.cube("bottom_front_fin", (-2.5, 0, 0), (5, 1, 0), "spines")
    _edge_spines(m, 2.5, 17, 22, 1, bottom=False)
    m.bone("tail_fin", pivot=(0, 19.5, 3.5))
    m.cube("tail_fin", (0, -1.5, 0), (0, 3, 2), "tail")
    return m


def build_big() -> Model:
    """puffState 2 – für PufferfishBigModel (liest left_blue_fin, right_blue_fin)."""
    m = Model("fish_puffer_big", 64, 64)
    m.bone("body", pivot=(0, 22, 0))
    _ball(m, 4, 3, -8)
    m.cube("body", (-5.5, -7, -3), (0.5, 2, 2), "eye")                  # große Augen vorn oben an den Seiten
    m.cube("body", (5, -7, -3), (0.5, 2, 2), "eye")
    m.cube("body", (-1, -4, -6), (2, 1, 1), "beak")
    m.bone("right_blue_fin", pivot=(-4, 15, -2))
    m.cube("right_blue_fin", (-3, 0.5, 1), (3, 0, 2), "fin")            # hinter dem Auge
    m.bone("left_blue_fin", pivot=(4, 15, -2))
    m.cube("left_blue_fin", (0, 0.5, 1), (3, 0, 2), "fin")
    m.bone("top_front_fin", pivot=(0, 14, -4), rotation=(45, 0, 0))
    m.cube("top_front_fin", (-4, -2, 0), (8, 2, 0), "spines")
    m.bone("top_middle_fin", pivot=(0, 14, 0))
    m.cube("top_middle_fin", (-3, -3, 0), (6, 2, 0), "spines")          # quer über den Rücken (auf der Beule)
    m.bone("top_back_fin", pivot=(0, 14, 4), rotation=(-45, 0, 0))
    m.cube("top_back_fin", (-4, -2, 0), (8, 2, 0), "spines")
    m.bone("right_front_fin", pivot=(-4, 22, -4), rotation=(0, -45, 0))
    m.cube("right_front_fin", (-2, -8, 0), (2, 8, 0), "spines")
    m.bone("left_front_fin", pivot=(4, 22, -4), rotation=(0, 45, 0))
    m.cube("left_front_fin", (0, -8, 0), (2, 8, 0), "spines")
    m.bone("bottom_front_fin", pivot=(0, 22, -4), rotation=(-45, 0, 0))
    m.cube("bottom_front_fin", (-4, 0, 0), (8, 2, 0), "spines")
    m.bone("bottom_middle_fin", pivot=(0, 22, 0))
    m.cube("bottom_middle_fin", (-3, 1, 0), (6, 1, 0), "spines")        # quer über den Bauch (kurz)
    m.bone("bottom_back_fin", pivot=(0, 22, 4), rotation=(45, 0, 0))
    m.cube("bottom_back_fin", (-4, 0, 0), (8, 2, 0), "spines")
    m.bone("right_back_fin", pivot=(-4, 22, 4), rotation=(0, 45, 0))
    m.cube("right_back_fin", (-2, -8, 0), (2, 8, 0), "spines")
    m.bone("left_back_fin", pivot=(4, 22, 4), rotation=(0, -45, 0))
    m.cube("left_back_fin", (0, -8, 0), (2, 8, 0), "spines")
    _edge_spines(m, 4, 14, 22, 2)
    m.bone("tail_fin", pivot=(0, 18, 5))
    m.cube("tail_fin", (0, -2, 0), (0, 4, 3), "tail")
    return m


def build() -> Model:
    return build_small()


_BUILDERS = {"klein": build_small, "mittel": build_mid, "gross": build_big}


def stage_module(stage: str) -> SimpleNamespace:
    """Modul-Objekt einer Stufe für write_model (PALETTES mit genau einer Textur: kugelfisch_<stufe>)."""
    return SimpleNamespace(PALETTES={f"kugelfisch_{stage}": PAL}, build=_BUILDERS[stage], materials=materials,
                           hidden=hidden, preview_pose=preview_pose)


def hidden(variant: str) -> list[str]:
    return []


def preview_pose(variant: str) -> dict:
    return {}


# =============================================================================================
# Malen
# =============================================================================================

def _geo(ctx: Ctx) -> SimpleNamespace:
    return _GEO[ctx.params.get("variant", "kugelfisch_klein").rsplit("_", 1)[1]]


def _skin(ctx: Ctx):
    g = _geo(ctx)
    x, y, z = ctx.world
    ny = (y - g.cy) / g.hy                                              # -1 oben … +1 unten
    if ctx.face == "bottom" or ny >= 0.35:
        return tone(ctx, "belly", 0.78, seed=1)
    in_saddle = ny < g.saddle_low and any(a <= z < b for a, b in g.saddles) and ctx.face not in ("front", "back")
    if g.eye and ctx.face in ("west", "east") and abs(x) > g.hx - 0.01:
        y0, y1, z0, z1 = g.eye
        if y0 <= y < y1 + 1 and z0 <= z < z1 + 1 and not (y < y1 and z < z1):
            return pick(ctx.pal["iris"], 0.72)                          # heller Ring um das Auge
    if in_saddle:
        edge = ny > g.saddle_low - 0.25                                 # Unterkante etwas heller
        return tone(ctx, "saddle", 0.5 if edge else 0.36, seed=2)
    if ny >= 0.15:
        return tone(ctx, "cream", 0.66, seed=3)
    return tone(ctx, "skin", 0.6 - max(0.0, ny) * 0.3, seed=4)


_PUPIL = (16, 18, 24, 255)


def _eye(ctx: Ctx):
    """Vorstehender Augapfel: hell goldene Seiten, die Pupille schaut zur Seite."""
    x, y, z = ctx.world
    outward = (ctx.face == "west" and x < 0) or (ctx.face == "east" and x > 0)
    if not outward:
        return pick(ctx.pal["iris"], 0.72)                              # Rand des vorstehenden Auges
    if ctx.cube.size[1] >= 2 and ctx.t == 0 and ctx.s == (1 if ctx.face == "west" else 0):
        return (236, 240, 228, 255)                                     # Glanzpunkt vorn oben (großes Auge)
    return _PUPIL


def _beak(ctx: Ctx):
    return tone(ctx, "beak", 0.72 if ctx.face != "bottom" else 0.6, seed=5)


def _spike_positions(n: int) -> set[int]:
    """Jeder zweite Texel: bei ungerader Länge symmetrisch mit Stacheln an beiden Enden (5 -> 0, 2, 4)."""
    return {i for i in range(n) if i % 2 == (0 if n % 2 else 1)}


def _spines(ctx: Ctx):
    w, h, d = ctx.cube.size
    ox, oy, oz = ctx.cube.origin
    lx, ly, lz = ctx.local
    if d == 0 and w < h:                                                # senkrechte Kante: nach außen = x
        out, along, n, length = abs(lx), ly - oy, int(h), w
    elif d == 0:                                                        # Querkante: nach außen = y
        out, along, n, length = abs(ly), lx - ox, int(w), h
        if oy < 0 and oy + h < 0:                                       # Rückenreihe auf der Beule: Fuß unten
            out = abs(ly - (oy + h))
    else:                                                               # Längskante: nach außen = y
        out, along, n, length = abs(ly), lz - oz, int(d), h
    if int(math.floor(along)) not in _spike_positions(n):
        return None
    belly = ctx.world[1] > _geo(ctx).cy + 1
    if length <= 1:                                                     # kurze Stacheln (halb aufgeblasen)
        return pick(ctx.pal["belly"], 0.66) if belly else pick(ctx.pal["spine"], 0.42)
    if out > length - 1:                                                # Spitze
        return pick(ctx.pal["spine"], 0.58 if belly else 0.3)
    return pick(ctx.pal["belly"], 0.76) if belly else pick(ctx.pal["spine"], 0.62)


def _fin(ctx: Ctx):
    w, h, d = ctx.cube.size
    lx, ly, lz = ctx.local
    if w == 0:                                                          # kleine Rücken-/Afterflosse
        return fin_membrane(ctx, "fin", "finedge", 0.5, lz, base=0.5)
    oz = ctx.cube.origin[2]
    reach = abs(lx) / w                                                 # 0 = Ansatz am Leib, 1 = Rand
    return fin_membrane(ctx, "fin", "finedge", min(1.0, reach * 0.85), lz - oz, base=0.5, ray_axis=0)


def _tail(ctx: Ctx):
    _, ly, lz = ctx.local
    w, h, d = ctx.cube.size
    _, oy, oz = ctx.cube.origin
    along = (lz - oz) / d                                               # 0 = Ansatz, 1 = Rand
    mid = oy + h / 2
    if abs(ly - mid) > h / 2 * (0.45 + 0.65 * along):
        return None                                                     # Fächer: schmal am Ansatz
    return fin_membrane(ctx, "fin", "finedge", along * 0.9, ly - oy, base=0.5, ray_axis=1)


def materials(variant: str) -> dict:
    return {"skin": _skin, "eye": _eye, "beak": _beak, "spines": _spines, "fin": _fin, "tail": _tail}
