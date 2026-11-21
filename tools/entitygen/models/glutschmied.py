"""
Malleus, der Glutschmied: der Meisterschmied der Brandgebeine, der auf dem Dach der Aschenesse an der Glutkrone
arbeitet. Ein riesiges, verkohltes Gerippe (im Spiel 1,4-fach vergrößert, knapp vier Blöcke hoch) mit breiten Schultern
unter Platten aus geschwärztem Eisen, einem Widderschädel mit glühenden Augenhöhlen und gekrümmten Hörnern, einem
Brustkorb, in dem eine Herzesse glüht, Ketten um die Unterarme, einer angesengten Lederschürze mit Werkzeugschlaufen und
eisenbeschlagenen Beinschienen. Links schwingt er den Schmiedehammer, rechts führt er sein Meisterstück: den Glutspeer.

Knochen-Namen werden in GlutschmiedModel (Java) für die Animationen verwendet.
"""
from __future__ import annotations

import math

from ..geometry import Model
from ..paint import Ctx, Voronoi3D, hash01, hexc, mix, noise3, pick, ramp, rel_y


def stops(*colors: str) -> list:
    """Handverlesene Tonleiter (für Glut und Flammen: der lineare Verlauf von Dunkelrot nach Gelb würde braun)."""
    return [hexc(c) for c in colors]

PALETTES = {
    "glutschmied": {
        "bone": ramp("#140f0d", "#7a6e66", steps=8), "socket": ramp("#010000", "#140806"),
        "ember": stops("#5c1404", "#a02806", "#de4a0c", "#ff7418", "#ff9c2c", "#ffc860", "#fff0b0"),
        "heart": stops("#6a1002", "#b02408", "#f0500e", "#ff8020", "#ffb040", "#ffe07a", "#fff6c8"),
        "iron": ramp("#0c0c0e", "#5c5a5e", steps=7), "rivet": ramp("#3a2a0c", "#e8b850"),
        "leather": ramp("#120a06", "#5a3a22", steps=6), "eye": ramp("#8a2002", "#fff2b0"),
        "horn": ramp("#0e0a08", "#4e4038", steps=6), "wood": ramp("#100a06", "#4a2e1a", steps=6),
        "gold": ramp("#5a3a0a", "#ffe08a"), "blade": stops("#5a0c02", "#9c1e04", "#dc420a", "#ff6c16", "#ff9a2a", "#ffc850", "#fff0a0"),
        "chain": ramp("#0a0a0a", "#4a4644", steps=5), "flame": stops("#8a1c02", "#e0480a", "#ff7a16", "#ffa832", "#ffd66a", "#fff4c0"),
    },
}


def build() -> Model:
    m = Model("glutschmied", 128, 128)
    # --- Rumpf: Becken, Wirbelsäule, Brustkorb mit Herzesse, Schulterjoch aus Eisenplatten, Gürtel
    m.bone("body", pivot=(0, 6, 0), rotation=(6, 0, 0))
    m.cube("body", (-4.5, -1, -2.5), (9, 3, 5), "pelvis")
    m.cube("body", (-1, -9, 0.6), (2, 9, 2), "spine")
    m.cube("body", (-5.5, -17, -3.5), (11, 9, 7), "ribs")
    m.cube("body", (-2, -15, -1.5), (4, 4, 3), "heart")
    m.cube("body", (-8, -19.5, -4), (16, 3, 8), "yoke")
    m.cube("body", (-5, -2.4, -3), (10, 2, 6), "belt")
    # Lederschürze (pendelt beim Gehen)
    m.bone("apron", "body", pivot=(0, -1, -3.2), rotation=(-4, 0, 0))
    m.cube("apron", (-4, 0, -0.6), (8, 11, 0.6), "apron")
    # --- Kopf: Widderschädel mit Hörnern, Unterkiefer
    m.bone("head", "body", pivot=(0, -19.5, -1), rotation=(-6, 0, 0))
    m.cube("head", (-3.5, -7, -4.5), (7, 7, 7), "skull")
    m.cube("head", (-2.5, -3.5, -6.5), (5, 3, 2.2), "snout")
    # Widderhörner: vom Scheitel nach außen und hinten, dann in einem Bogen nach unten und wieder nach vorn
    path = [((3.0, -7.0, -0.8), 2.8), ((4.6, -7.4, 1.0), 2.6), ((5.9, -6.2, 2.6), 2.4), ((6.6, -4.0, 2.8), 2.2),
            ((6.6, -2.0, 1.6), 1.9), ((6.1, -0.6, -0.2), 1.6), ((5.4, 0.2, -1.6), 1.2)]
    for side, sx in (("left", 1), ("right", -1)):
        horn = f"{side}_horn"
        m.bone(horn, "head", pivot=(0, 0, 0))
        for i, ((cx, cy, cz), size) in enumerate(path):
            h = size / 2
            m.cube(horn, (cx * sx - h, cy - h, cz - h), (size, size, size), "horn_tip" if i == len(path) - 1 else "horn")
    # Flammen auf dem Scheitel (wie bei den Brandgebeinen, nur größer; flackern)
    m.bone("flames", "head", pivot=(0, -7, -1))
    m.bone("flame_1", "flames", rotation=(0, 45, 0))
    m.cube("flame_1", (-4, -6, 0), (8, 6, 0), "flame")
    m.bone("flame_2", "flames", rotation=(0, 135, 0))
    m.cube("flame_2", (-4, -6, 0), (8, 6, 0), "flame")
    m.bone("jaw", "head", pivot=(0, -0.8, -3.2), rotation=(8, 0, 0))
    m.cube("jaw", (-2.5, 0, -3.2), (5, 1.8, 4), "jaw")
    # --- Arme: Schulterplatten, Oberarm, Unterarm mit Ketten, Hand; links der Hammer, rechts der Speer
    for side, sx in (("left", 1), ("right", -1)):
        arm = f"{side}_arm"
        m.bone(arm, "body", pivot=(8.5 * sx, -17.5, 0), rotation=(0, 0, -8 * sx))
        m.cube(arm, (-3 if sx > 0 else -3, -3, -3.5), (6, 4.5, 7), "pauldron")
        m.cube(arm, (-1.2, 0, -1.2), (2.4, 9, 2.4), "limb")
        m.cube(arm, (-1.6, 8.2, -1.6), (3.2, 1.6, 3.2), "knob")
        fore = f"{side}_forearm"
        m.bone(fore, arm, pivot=(0, 9, 0), rotation=(-12, 0, 0))
        m.cube(fore, (-1.2, 0, -1.2), (2.4, 8.5, 2.4), "limb")
        m.cube(fore, (-1.9, 2.0, -1.9), (3.8, 4.0, 3.8), "chain", inflate=0.05)
        m.cube(fore, (-2, 8.2, -2), (4, 3.5, 4), "hand")
    # Schmiedehammer (links)
    m.bone("hammer", "left_forearm", pivot=(0, 10, 0), rotation=(0, 0, 0))
    m.cube("hammer", (-0.7, -4, -0.7), (1.4, 16, 1.4), "handle")
    # der Kopf liegt längs (vorn/hinten): beim Schlag nach vorn trifft eine Bahn genau nach unten
    m.cube("hammer", (-2.5, 9, -3.5), (5, 4.5, 7), "hammer_head")
    m.cube("hammer", (-1.8, 9.6, -4.2), (3.6, 3.3, 0.7), "hammer_face")
    m.cube("hammer", (-1.8, 9.6, 3.5), (3.6, 3.3, 0.7), "hammer_face")
    # Glutspeer (rechts): Schaft, goldene Ringe, Parierstück, glühende Klinge (zeigt nach vorn)
    m.bone("spear", "right_forearm", pivot=(0, 10, 0), rotation=(-90, 0, 0))
    m.cube("spear", (-0.7, -9, -0.7), (1.4, 30, 1.4), "shaft")
    m.cube("spear", (-1.0, -6, -1.0), (2, 1.2, 2), "ring")
    m.cube("spear", (-1.0, 12, -1.0), (2, 1.2, 2), "ring")
    m.cube("spear", (-2.4, 20.5, -1.1), (4.8, 1.6, 2.2), "guard")
    # die Klinge: breit und blattförmig wie die des Glutspeers, den Spieler ihm abringen
    m.cube("spear", (-1.5, 22, -0.5), (3.0, 2.4, 1.0), "blade")
    m.cube("spear", (-2.0, 24.4, -0.5), (4.0, 3.0, 1.0), "blade")
    m.cube("spear", (-1.4, 27.4, -0.45), (2.8, 2.2, 0.9), "blade")
    m.cube("spear", (-0.8, 29.6, -0.4), (1.6, 1.8, 0.8), "blade")
    m.cube("spear", (-0.3, 31.4, -0.3), (0.6, 1.4, 0.6), "blade")
    # --- Beine: Oberschenkelknochen, Knie, Beinschienen, eisenbeschlagene Füße
    for side, sx in (("left", 1), ("right", -1)):
        leg = f"{side}_leg"
        m.bone(leg, pivot=(3.6 * sx, 7, 0))
        m.cube(leg, (-1.3, 0, -1.3), (2.6, 7.5, 2.6), "limb")
        m.cube(leg, (-1.7, 7, -1.7), (3.4, 1.8, 3.4), "knob")
        m.cube(leg, (-2.2, 8.6, -2.2), (4.4, 6.4, 4.4), "greave")
        m.cube(leg, (-2.4, 15, -3.6), (4.8, 2, 6.4), "boot")
    return m


_CRACKS = Voronoi3D(3.6, 731)
_LIT = {"top": 0.1, "front": 0.04, "west": 0.0, "east": -0.04, "back": -0.06, "bottom": -0.14}


def _ember(ctx: Ctx, v: float):
    col = pick(ctx.pal["ember"], v)
    ctx.glow = col
    return col


def _charred(ctx: Ctx, cracks: float = 0.075, seed: int = 0):
    """Verkohlter Knochen: dunkel, oben ein Hauch Asche, in wenigen, langen Rissen glimmt Glut."""
    x, y, z = ctx.world
    _, edge = _CRACKS(x * 1.0 + seed, y, z)
    cracks *= 0.5
    if edge < cracks:
        return _ember(ctx, 0.5 + (cracks - edge) / cracks * 0.35)
    v = 0.44 + noise3(x * 0.5, y * 0.5, z * 0.5, 17 + seed) * 0.26 + _LIT[ctx.face] - rel_y(ctx) * 0.06
    if ctx.face == "top" and hash01(ctx.u, ctx.v, 5) > 0.55:
        v += 0.2                                                   # Asche
    return pick(ctx.pal["bone"], v)


def _limb(ctx: Ctx):
    return _charred(ctx, 0.06, 1)


def _knob(ctx: Ctx):
    return _charred(ctx, 0.1, 2)


def _pelvis(ctx: Ctx):
    return _charred(ctx, 0.07, 3)


def _spine(ctx: Ctx):
    # Wirbel: helle Knochenringe, dazwischen Glut
    if int(ctx.local[1] * 1.0) % 2 == 0:
        return _charred(ctx, 0.0, 4)
    return _ember(ctx, 0.45)


def _ribs(ctx: Ctx):
    """Brustkorb: waagerechte Rippen mit Lücken (durchsichtig), durch die die Herzesse leuchtet; vorn das Brustbein."""
    if ctx.face in ("top", "bottom"):
        return _charred(ctx, 0.05, 5)
    lx, ly, lz = ctx.local
    row = int(math.floor(ly - ctx.cube.origin[1]))
    if ctx.face == "front" and abs(lx) < 0.9:
        return _charred(ctx, 0.08, 6)                              # Brustbein
    if row % 2 == 1:
        return None                                                # Lücke zwischen den Rippen
    return _charred(ctx, 0.08, 7)


def _heart(ctx: Ctx):
    """Die Herzesse: ein glühender Klumpen (rot zum Rand, gelb im Kern), dunkle Schlacke in den Rissen."""
    x, y, z = ctx.world
    _, edge = _CRACKS(x * 2.2, y * 2.2, z * 2.2)
    centre = 1.0 - min(1.0, (abs(ctx.s - ctx.w / 2) / max(1, ctx.w / 2) + abs(ctx.t - ctx.h / 2) / max(1, ctx.h / 2)) / 1.6)
    col = pick(ctx.pal["heart"], 0.2 if edge < 0.06 else 0.4 + centre * 0.4)
    ctx.glow = col
    return col


def _iron(ctx: Ctx, rivets: bool = True):
    """Geschwärztes Eisen, gehämmert (Flecken), Ränder heller, goldene Nieten."""
    x, y, z = ctx.world
    v = 0.36 + noise3(x * 0.8, y * 0.8, z * 0.8, 31) * 0.22 + _LIT[ctx.face]
    if ctx.s == 0 or ctx.t == 0 or ctx.s == ctx.w - 1 or ctx.t == ctx.h - 1:
        v += 0.12
    if rivets and ctx.face not in ("bottom",) and (ctx.s % 4 == 1) and ctx.t == 1:
        return pick(ctx.pal["rivet"], 0.7)
    return pick(ctx.pal["iron"], v)


def _yoke(ctx: Ctx):
    return _iron(ctx)


def _pauldron(ctx: Ctx):
    # gestaffelte Platten: dunkle Fugen quer
    if ctx.face not in ("top", "bottom") and ctx.t in (1, 3):
        return pick(ctx.pal["iron"], 0.15)
    return _iron(ctx)


def _greave(ctx: Ctx):
    return _iron(ctx)


def _boot(ctx: Ctx):
    if ctx.face == "front" and ctx.t == ctx.h - 1:
        return pick(ctx.pal["rivet"], 0.6)
    return _iron(ctx, rivets=False)


def _belt(ctx: Ctx):
    if ctx.face == "front" and abs(ctx.local[0]) < 1.0:
        col = pick(ctx.pal["gold"], 0.7)                          # Schnalle
        return col
    if ctx.face in ("west", "east", "front") and ctx.s % 4 == 2:
        return pick(ctx.pal["iron"], 0.5)                          # Werkzeugschlaufen
    return pick(ctx.pal["leather"], 0.42 + _LIT[ctx.face] + (hash01(ctx.u, ctx.v) - 0.5) * 0.08)


def _apron(ctx: Ctx):
    """Angesengte Lederschürze: Ränder versengt, ein paar Brandlöcher mit glühendem Saum."""
    if ctx.face not in ("front", "back"):
        return pick(ctx.pal["leather"], 0.3)
    x, y, z = ctx.world
    burn = noise3(x * 0.55, y * 0.55, z * 0.55, 41)
    if burn > 0.78:
        return None
    if burn > 0.72:
        return _ember(ctx, 0.5)
    v = 0.4 + noise3(x * 1.4, y * 1.4, z, 42) * 0.18 + (0.08 if ctx.face == "front" else -0.06)
    if ctx.t >= ctx.h - 2:
        v -= 0.16                                                   # versengter Saum
    if ctx.s in (0, ctx.w - 1):
        v -= 0.1
    return pick(ctx.pal["leather"], v)


def _skull(ctx: Ctx):
    """Widderschädel: verkohlt, tiefe Augenhöhlen mit glühenden Augen, Nähte voller Glut."""
    if ctx.face == "front":
        lx, ly, lz = ctx.local
        # Augenhöhlen
        for ex in (-1.8, 1.8):
            d = math.hypot(lx - ex, ly - (-3.8))
            if d < 1.25:
                col = pick(ctx.pal["eye"], 0.95 if d < 0.6 else 0.6)
                ctx.glow = col
                return col
            if d < 1.6:
                return pick(ctx.pal["socket"], 0.5)
    if ctx.face == "top" and abs(ctx.local[0]) < 0.6:
        return _ember(ctx, 0.5)                                     # Schädelnaht
    return _charred(ctx, 0.07, 8)


def _snout(ctx: Ctx):
    if ctx.face == "front" and ctx.t == ctx.h - 1:
        return pick(ctx.pal["socket"], 0.6)                         # Zahnreihe im Schatten
    if ctx.face == "front" and ctx.t == ctx.h - 2 and ctx.s % 2 == 0:
        return pick(ctx.pal["bone"], 0.8)                           # Zähne
    if ctx.face == "front" and ctx.t == 0 and ctx.s in (1, ctx.w - 2):
        return pick(ctx.pal["socket"], 0.4)                         # Nüstern
    return _charred(ctx, 0.05, 9)


def _jaw(ctx: Ctx):
    if ctx.face == "top" and ctx.local[2] < -2.0 and ctx.s % 2 == 0:
        return pick(ctx.pal["bone"], 0.85)                          # Zähne
    return _charred(ctx, 0.06, 10)


def _horn(ctx: Ctx):
    """Gerippte Widderhörner: Querrillen, nach außen heller."""
    x, y, z = ctx.world
    ridge = (math.sin((x + z) * 2.4) * 0.5 + 0.5)
    v = 0.3 + ridge * 0.3 + _LIT[ctx.face]
    return pick(ctx.pal["horn"], v)


def _horn_tip(ctx: Ctx):
    return _ember(ctx, 0.62)


def _chain(ctx: Ctx):
    """Ketten um den Unterarm: Glieder abwechselnd quer und längs, dazwischen schimmert der Knochen."""
    if ctx.face in ("top", "bottom"):
        return None
    link = (ctx.s + ctx.t * 2) % 4
    if link == 3:
        return None
    return pick(ctx.pal["chain"], 0.35 + (0.3 if link == 1 else 0.0) + _LIT[ctx.face])


def _hand(ctx: Ctx):
    if ctx.face in ("front", "back") and ctx.s % 2 == 1 and rel_y(ctx) > 0.45:
        return pick(ctx.pal["socket"], 0.6)                         # Finger
    return _charred(ctx, 0.08, 11)


def _handle(ctx: Ctx):
    """Hammerstiel: dunkles Holz, umwickelt."""
    if int(ctx.local[1] + 10) % 3 == 0:
        return pick(ctx.pal["leather"], 0.5)
    return pick(ctx.pal["wood"], 0.38 + _LIT[ctx.face] + (hash01(ctx.u, ctx.v) - 0.5) * 0.08)


def _hammer_head(ctx: Ctx):
    # die Schlagkante glüht (frisch aus der Esse)
    if ctx.face in ("front", "back"):
        return _ember(ctx, 0.55 + noise3(*ctx.world, 3) * 0.3)
    return _iron(ctx, rivets=False)


def _hammer_face(ctx: Ctx):
    return _ember(ctx, 0.72)


def _shaft(ctx: Ctx):
    """Speerschaft: verkohltes Eisenholz mit feinen Glutfäden."""
    x, y, z = ctx.world
    if hash01(ctx.u, ctx.v, 21) > 0.93:
        return _ember(ctx, 0.4)
    return pick(ctx.pal["wood"], 0.34 + _LIT[ctx.face] + noise3(x, y * 0.3, z, 23) * 0.2)


def _ring(ctx: Ctx):
    col = pick(ctx.pal["gold"], 0.55 + _LIT[ctx.face] + (0.25 if ctx.s % 2 == 0 else 0.0))
    return col


def _guard(ctx: Ctx):
    if ctx.face in ("front", "back") and ctx.s in (0, ctx.w - 1):
        return _ember(ctx, 0.5)
    return pick(ctx.pal["gold"], 0.5 + _LIT[ctx.face])


def _blade(ctx: Ctx):
    """Die Klinge: geschmolzenes Metall, innen weißglühend, zum Rand orange, mit dunkler Schlackenader in der Mitte."""
    lx, ly, lz = ctx.local
    w = max(0.5, ctx.cube.size[0] / 2)
    edge = abs(lx - (ctx.cube.origin[0] + w)) / w if ctx.face in ("front", "back") else 0.5
    v = 0.8 - edge * 0.35
    if ctx.face in ("front", "back") and abs(lx - (ctx.cube.origin[0] + w)) < 0.35:
        v = 0.38                                                    # dunkle Schlackenader in der Mitte
    col = pick(ctx.pal["blade"], v)
    ctx.glow = col
    return col


_FLAME = ["...o....",
          "..oOo..o",
          ".oOYOooO",
          "oOYWYOOY",
          "OYWWWYYW",
          "YWWWWWWW"]


def _flame(ctx: Ctx):
    """Flammen auf dem Schädel (nach Lage auf der Ebene gemalt, damit Vorder- und Rückseite zusammenpassen)."""
    lx, ly, _ = ctx.local
    ox, oy, _ = ctx.cube.origin
    col_i = max(0, min(len(_FLAME[0]) - 1, int((lx - ox) / ctx.cube.size[0] * len(_FLAME[0]))))
    row_i = max(0, min(len(_FLAME) - 1, int((ly - oy) / ctx.cube.size[1] * len(_FLAME))))
    level = {"o": 0.2, "O": 0.45, "Y": 0.72, "W": 0.92}.get(_FLAME[row_i][col_i])
    if level is None:
        return None
    col = pick(ctx.pal["flame"], level)
    ctx.glow = col
    return col


def materials(variant: str) -> dict:
    return {"pelvis": _pelvis, "spine": _spine, "ribs": _ribs, "heart": _heart, "yoke": _yoke, "belt": _belt, "apron": _apron,
            "skull": _skull, "snout": _snout, "horn": _horn, "horn_tip": _horn_tip, "jaw": _jaw, "pauldron": _pauldron,
            "limb": _limb, "knob": _knob, "chain": _chain, "hand": _hand, "handle": _handle, "hammer_head": _hammer_head,
            "hammer_face": _hammer_face, "shaft": _shaft, "ring": _ring, "guard": _guard, "blade": _blade,
            "greave": _greave, "boot": _boot, "flame": _flame}
