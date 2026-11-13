"""
Seelenreiher: ein großer Watvogel der Seelensümpfe, gut zweieinhalb Blöcke hoch. Schieferblaues Gefieder, dessen
Federspitzen seelentürkis glimmen – an den Schwungfedern, im Federschopf, an den Schmuckfedern der Brust und am Schwanz;
lange schwarze Stelzenbeine mit gespreizten Zehen, ein Hals in S-Form und ein langer, knochenbleicher Speerschnabel mit
einer türkis glühenden Naht. Über den Augen eine schwarze Kappe, aus der drei lange Schopffedern wehen.

Knochen (für SoulHeronModel in Java): body (Rumpf, vorn erhoben) mit tail, plumes (Brustschmuck), den Flügeln
left_wing/right_wing (je mit *_outer: Handschwingen) und dem Hals neck1 → neck2 → neck3 → head (mit jaw: Unterschnabel,
crest: Schopf); die Beine left_leg/right_leg → *_shin → *_foot hängen an der Wurzel (so stehen sie, wenn der Rumpf
sich neigt).
"""
from __future__ import annotations

from ..geometry import Model
from ..paint import Ctx, hash01, hexc, mix, noise3, pick, rel_y

PALETTES = {
    "soul_heron": {
        "plume": [hexc(c) for c in ("#1e2632", "#2e3a4a", "#44546a", "#5c6e86", "#7a8ca4", "#9cacc0", "#c2ceda")],
        "belly": [hexc(c) for c in ("#5a6676", "#7a8696", "#9aa6b4", "#b8c2ce", "#d4dce4", "#eef2f6")],
        "dark": [hexc(c) for c in ("#06080c", "#0e1218", "#181e28", "#242c38", "#344050")],
        "soul": [hexc(c) for c in ("#0e3a40", "#146a70", "#20a0a0", "#3ad8d0", "#7af4ea", "#c8fff8")],
        "beak": [hexc(c) for c in ("#3a3830", "#6a665a", "#9a9482", "#c8c0aa", "#e8e0cc", "#f8f4e8")],
        "leg": [hexc(c) for c in ("#050608", "#0e1014", "#1a1e24", "#2a3038", "#3e4652")],
    },
}


def build() -> Model:
    m = Model("soul_heron", 128, 96)
    m.tex_res = 2
    # Rumpf, vorn erhoben
    m.bone("body", pivot=(0, 5.0, 1.0), rotation=(-16, 0, 0))
    m.cube("body", (-3.5, -3.4, -7.0), (7, 7, 13), "plumage")
    m.cube("body", (-3.0, 2.4, -6.2), (6, 1.8, 10), "belly")
    m.cube("body", (-2.6, -4.2, -6.4), (5.2, 1.2, 9.4), "plumage")        # gewölbter Rücken
    # Brustschmuckfedern, die vorn herabhängen
    m.bone("plumes", "body", pivot=(0, 0.6, -7.0), rotation=(14, 0, 0))
    m.cube("plumes", (-2.3, 0, -0.7), (4.6, 6.5, 0.9), "plume")
    # Schwanz
    m.bone("tail", "body", pivot=(0, -1.6, 6.0), rotation=(22, 0, 0))
    m.cube("tail", (-2.6, -0.6, 0), (5.2, 1.2, 6.0), "tail")
    # Flügel, angelegt: die Schulter vorn oben, Arm- und Handschwingen reichen nach hinten über den Schwanz
    for side, sx in (("left", 1), ("right", -1)):
        wing = f"{side}_wing"
        m.bone(wing, "body", pivot=(3.4 * sx, -2.9, -5.6), rotation=(-3, 0, 0))
        x0 = 0.0 if sx > 0 else -1.2
        m.cube(wing, (x0, 0, 0), (1.2, 6.4, 8.5), "wing")
        outer = f"{side}_wing_outer"
        m.bone(outer, wing, pivot=(0, 0.4, 8.4), rotation=(-6, 0, 0))
        m.cube(outer, (x0 + 0.1, 0, 0), (1.0, 5.2, 7.0), "primaries")
    # Hals in S-Form: vor, zurück, wieder vor – dann der waagrechte Kopf mit dem Speerschnabel
    m.bone("neck1", "body", pivot=(0, -2.4, -6.2), rotation=(40, 0, 0))
    m.cube("neck1", (-1.5, -6.2, -1.5), (3.0, 6.2, 3.0), "neck")
    m.bone("neck2", "neck1", pivot=(0, -6.0, 0), rotation=(-56, 0, 0))
    m.cube("neck2", (-1.15, -6.0, -1.15), (2.3, 6.0, 2.3), "neck")
    m.bone("neck3", "neck2", pivot=(0, -5.8, 0), rotation=(36, 0, 0))
    m.cube("neck3", (-1.0, -4.6, -1.0), (2.0, 4.6, 2.0), "neck")
    m.bone("head", "neck3", pivot=(0, -4.4, 0), rotation=(-4, 0, 0))
    m.cube("head", (-1.6, -2.6, -3.4), (3.2, 3.0, 4.6), "head")
    m.cube("head", (-0.65, -1.7, -11.6), (1.3, 1.0, 8.4), "beak")
    m.cube("head", (1.55, -1.85, -2.7), (0.25, 0.7, 0.9), "eye")
    m.cube("head", (-1.8, -1.85, -2.7), (0.25, 0.7, 0.9), "eye")
    m.bone("jaw", "head", pivot=(0, -0.7, -3.2), rotation=(2, 0, 0))
    m.cube("jaw", (-0.55, 0, -7.8), (1.1, 0.7, 7.8), "beak")
    m.bone("crest", "head", pivot=(0, -2.3, 0.8), rotation=(58, 0, 0))
    m.cube("crest", (-0.45, -0.25, 0), (0.9, 0.5, 7.0), "crest")
    m.cube("crest", (0.35, 0.1, 0), (0.6, 0.4, 5.2), "crest")
    m.cube("crest", (-0.95, 0.1, 0), (0.6, 0.4, 5.2), "crest")
    # Stelzenbeine: oben befiedert, das Fersengelenk knickt nach hinten, vorn drei gespreizte Zehen, hinten eine
    for side, sx in (("left", 1), ("right", -1)):
        leg = f"{side}_leg"
        m.bone(leg, pivot=(1.7 * sx, 8.2, 1.6), rotation=(-10, 0, 0))
        m.cube(leg, (-0.85, -0.4, -0.85), (1.7, 3.0, 1.7), "thigh")
        m.cube(leg, (-0.55, 2.6, -0.55), (1.1, 5.0, 1.1), "leg")
        shin = f"{side}_shin"
        m.bone(shin, leg, pivot=(0, 7.4, 0), rotation=(16, 0, 0))
        m.cube(shin, (-0.5, 0, -0.5), (1.0, 8.6, 1.0), "leg")
        foot = f"{side}_foot"
        m.bone(foot, shin, pivot=(0, 8.4, 0), rotation=(-6, 0, 0))
        m.cube(foot, (-0.3, -0.3, -3.6), (0.6, 0.55, 3.8), "leg")
        m.cube(foot, (-1.5, -0.3, -2.8), (0.5, 0.5, 3.0), "leg")
        m.cube(foot, (1.0, -0.3, -2.8), (0.5, 0.5, 3.0), "leg")
        m.cube(foot, (-0.25, -0.3, 0.1), (0.5, 0.5, 1.8), "leg")
    return m


# =============================================================================================
# Bemalung
# =============================================================================================

_LIT = {"top": 0.12, "front": 0.04, "west": 0.0, "east": -0.04, "back": -0.06, "bottom": -0.18}


def _feathers(ctx: Ctx, base: float, tip_glow: float = 0.0) -> tuple:
    """Gefieder in versetzten Reihen kleiner Federn: oben im Federbogen heller, am Rand dunkler; manche Spitzen glimmen."""
    x, y, z = ctx.world
    row = int((y + 40.0) / 1.3)
    shift = 0.65 if row % 2 else 0.0
    cx = ((z + 40.0) / 1.3 + shift) % 1.0
    ry = ((y + 40.0) / 1.3) % 1.0
    v = base + _LIT[ctx.face] + (0.5 - abs(cx - 0.5)) * 0.18 - ry * 0.14 + (noise3(x * 0.5, y * 0.5, z * 0.5, 31) - 0.5) * 0.16
    if ry > 0.82 and abs(cx - 0.5) < 0.25 and hash01(row, int((z + 40) / 1.3), 32) < tip_glow:
        col = pick(ctx.pal["soul"], 0.62)
        ctx.glow = mix((0, 0, 0, 255), col, 0.8)
        return col
    return pick(ctx.pal["plume"], v)


def _plumage(ctx: Ctx):
    return _feathers(ctx, 0.42, 0.05)


def _belly(ctx: Ctx):
    x, y, z = ctx.world
    v = 0.45 + _LIT[ctx.face] + (noise3(x * 0.6, y * 0.6, z * 0.6, 33) - 0.5) * 0.18
    return pick(ctx.pal["belly"], v)


def _thigh(ctx: Ctx):
    # befiederter Oberschenkel, unten in Fransen
    if rel_y(ctx) > 0.78 and hash01(ctx.u, 34) > 0.5:
        return pick(ctx.pal["plume"], 0.25)
    return _feathers(ctx, 0.38)


def _wing(ctx: Ctx):
    """Deckfedern: Reihen schräg nach hinten; die unterste Reihe mit glimmenden Säumen."""
    ry = rel_y(ctx)
    if ry > 0.86:
        col = pick(ctx.pal["soul"], 0.55 + (hash01(ctx.u, ctx.v, 35) - 0.5) * 0.2)
        if hash01(ctx.u // 3, 36) > 0.35:
            ctx.glow = mix((0, 0, 0, 255), col, 0.75)
            return col
    return _feathers(ctx, 0.36 - ry * 0.08, 0.04)


def _primaries(ctx: Ctx):
    """Handschwingen: lange Federn längs, zur Spitze (hinten) dunkler, die Säume glimmen türkis."""
    x, y, z = ctx.world
    lz = ctx.local[2]
    along = max(0.0, min(1.0, lz / 7.0))
    fy = (ctx.local[1] + 0.3) / 1.1
    feather = int(fy)
    edge = fy % 1.0 > 0.8
    # gefingerte Enden: zwischen den Federn klaffen Lücken, und jede zweite Feder ist etwas kürzer
    if ctx.face in ("west", "east") and (along > 0.8 and fy % 1.0 > 0.62 or along > 0.92 and feather % 2):
        return None
    if along > 0.62 and (edge or along > 0.86):
        col = pick(ctx.pal["soul"], 0.45 + along * 0.4)
        ctx.glow = mix((0, 0, 0, 255), col, 0.5 + along * 0.4)
        return col
    v = 0.34 - along * 0.18 + _LIT[ctx.face] + (0.06 if feather % 2 else 0.0) - (0.12 if edge else 0.0)
    return pick(ctx.pal["plume"], v + (noise3(x, y, z * 0.3, 37) - 0.5) * 0.08)


def _plume(ctx: Ctx):
    """Schmuckfedern: lange, feine Strähnen, unten glimmend."""
    ry = rel_y(ctx)
    strand = hash01(ctx.u // 1, 38)
    if ry > 0.7 + strand * 0.2:
        col = pick(ctx.pal["soul"], 0.5 + ry * 0.4)
        ctx.glow = mix((0, 0, 0, 255), col, 0.7)
        return col
    return pick(ctx.pal["plume"], 0.5 + strand * 0.2 - ry * 0.2 + _LIT[ctx.face])


def _tail(ctx: Ctx):
    lz = ctx.local[2]
    if lz > 4.6:
        col = pick(ctx.pal["soul"], 0.6)
        ctx.glow = mix((0, 0, 0, 255), col, 0.7)
        return col
    return _feathers(ctx, 0.32)


def _neck(ctx: Ctx):
    """Hals: hell an den Seiten, vorn ein dunkler Streifen aus Tupfen (wie beim Graureiher)."""
    x, y, z = ctx.world
    if ctx.face == "front":
        rs = (ctx.s + 0.5) / max(ctx.w, 1)
        if abs(rs - 0.5) < 0.22 and hash01(ctx.v // 2, 39) > 0.3:
            return pick(ctx.pal["dark"], 0.5)
        return pick(ctx.pal["belly"], 0.7)
    v = 0.55 + _LIT[ctx.face] + (noise3(x * 0.7, y * 0.7, z * 0.7, 40) - 0.5) * 0.12
    if ctx.face == "back":
        v -= 0.15
    return pick(ctx.pal["plume"], v)


def _head(ctx: Ctx):
    """Kopf: schwarze Kappe über den Augen, darunter helles Gesicht; um das Auge ein türkiser Ring."""
    ly = ctx.local[1]
    if ly < -1.9 or ctx.face == "top":
        return pick(ctx.pal["dark"], 0.35 + _LIT[ctx.face])
    return pick(ctx.pal["belly"], 0.78 + _LIT[ctx.face])


def _beak(ctx: Ctx):
    """Speerschnabel: knochenbleich, zur Spitze heller; an den Seiten eine türkis glühende Naht."""
    lz = ctx.local[2]
    tip = max(0.0, min(1.0, -lz / 11.0))
    if ctx.face in ("west", "east") and abs(rel_y(ctx) - 0.55) < 0.17 and tip > 0.25:
        col = pick(ctx.pal["soul"], 0.7)
        ctx.glow = col
        return col
    v = 0.45 + tip * 0.45 + _LIT[ctx.face]
    if ctx.face == "bottom":
        v -= 0.2
    return pick(ctx.pal["beak"], v)


def _eye(ctx: Ctx):
    col = pick(ctx.pal["soul"], 0.95 if 0.25 < (ctx.t + 0.5) / max(ctx.h, 1) < 0.75 else 0.6)
    ctx.glow = col
    return col


def _crest(ctx: Ctx):
    lz = ctx.local[2]
    if lz > 3.8:
        col = pick(ctx.pal["soul"], 0.5 + (lz - 3.8) / 3.2 * 0.45)
        ctx.glow = mix((0, 0, 0, 255), col, 0.8)
        return col
    return pick(ctx.pal["dark"], 0.4 + _LIT[ctx.face])


def _leg(ctx: Ctx):
    """Stelzenbeine: schwarz, mit feinen Schuppenringen."""
    ly = ctx.local[1]
    ring = (ly * 1.6) % 1.0 < 0.22
    v = 0.55 + _LIT[ctx.face] - (0.2 if ring else 0.0) + (hash01(ctx.u, ctx.v, 41) - 0.5) * 0.1
    return pick(ctx.pal["leg"], v)


def materials(variant: str) -> dict:
    return {"plumage": _plumage, "belly": _belly, "thigh": _thigh, "wing": _wing, "primaries": _primaries, "plume": _plume,
            "tail": _tail, "neck": _neck, "head": _head, "beak": _beak, "eye": _eye, "crest": _crest, "leg": _leg}
