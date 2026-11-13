"""
Eisenwächter (statt Eisengolem) auf dem Skelett des Vanilla-Eisengolems: gleiche Knochen und Drehpunkte wie
IronGolemModel (head, body, right/left_arm, right/left_leg), damit Gehen, Schlag, Blumen-Reichen und das
Blumen-Halten (IronGolemFlowerLayer) genauso laufen. Eigene Quader und Bilder:

  * ein geschmiedeter Wächter: Topfhelm mit Kamm, Sehschlitz und glühenden Augen, Kinnreff unter dem Visier
  * genieteter Brustharnisch mit Halsberge; in der Brust ein Schmiedefeuer hinter einem Gitter, oben auf dem Rücken
    ein Rauchabzug – das Feuer beseelt den Wächter
  * Schulterstücke mit Folgeschiene, Ellbogenkachel, breite Armschienen und schwere Panzerhandschuhe
  * Beintaschen, Kniebuckel, Beinröhren und breite Eisenschuhe
  * Messingnieten und -kanten, ein Ledergurt mit Schnalle

Vier Gestalten nach Land: Schmiedeeisen (überall), Mooswächter (Wälder, Dschungel, Sümpfe – Moos, Efeu, Blüten),
Frostwächter (Schnee und Eis – Raureif, Eiszapfen) und Rostwächter (Küsten und Strände – Rost, Laufspuren).
Moos- und Schneepolster sowie Eiszapfen sind eigene Quader, die nur bei ihrer Gestalt bemalt sind.

Die Risse (wenig, mittel, stark beschädigt) sind eigene Bilder über demselben Modell (siehe ``crack_overlay``).
"""
from __future__ import annotations

import math

from PIL import Image

from ..geometry import Model, face_rects, texel_to_local
from ..paint import Ctx, Voronoi3D, hash01, mix, noise3, pick, ramp, rel_y

_IRON = {
    "brass": ramp("#3e260a", "#f2d27a", steps=8),
    "leather": ramp("#1e1008", "#8e5c34", steps=7),
    "ember": ramp("#4a0c00", "#fff2b0", steps=8, hue_shift=-0.06),
    "soot": ramp("#08080a", "#3a3836"),
    "dark": ramp("#020203", "#1a1614"),
}

PALETTES = {
    "schmiedeeisen": {**_IRON, "iron": ramp("#15181d", "#d4d8de", steps=10, hue_shift=0.02)},
    "mooswaechter": {**_IRON, "iron": ramp("#141a17", "#bfc6bb", steps=10, hue_shift=0.02),
                     "moss": ramp("#13240b", "#9cc456", steps=8), "leaf": ramp("#0f2a0c", "#62ad3c", steps=7),
                     "bloom": ramp("#6a7ac0", "#f4f6ff", steps=5)},
    "frostwaechter": {**_IRON, "iron": ramp("#1a2129", "#e4eef6", steps=10, hue_shift=0.03),
                      "rime": ramp("#7fa3bd", "#ffffff", steps=7), "ice": ramp("#3f7ea6", "#e6fbff", steps=7)},
    "rostwaechter": {**_IRON, "iron": ramp("#1c1512", "#c9c0b8", steps=10, hue_shift=0.02),
                     "rust": ramp("#2e1409", "#c8783a", steps=8, hue_shift=-0.03)},
}

LOOKS = tuple(PALETTES)


def build() -> Model:
    m = Model("iron_golem", 128, 128)

    # ---------------------------------------------------------------- Kopf: Topfhelm (Drehpunkt wie Vanilla)
    m.bone("head", pivot=(0, -7, -2))
    m.cube("head", (-4, -12, -5.5), (8, 10, 8), "helm")
    m.cube("head", (-0.6, -13.8, -5.2), (1.2, 1.8, 7.6), "crest")              # Kamm von der Stirn zum Nacken
    m.cube("head", (-4.4, -11.2, -6.1), (8.8, 1.2, 1.0), "brow")              # Stirnreif über dem Sehschlitz
    m.cube("head", (-4.5, -5.2, -6.4), (9, 3.2, 1.6), "bevor")                # Kinnreff
    m.cube("head", (-4.3, -12.5, -5.0), (8.6, 0.8, 7.2), "pad_helm")          # Moos-/Schneepolster (nur je Gestalt)
    for i, x in enumerate((-4.3, -0.4, 3.5)):                                 # Eiszapfen am Stirnreif (neben den Augen)
        m.cube("head", (x, -10.0, -6.2), (0.8, 1.4 + (i == 1) * 0.8, 0.8), "icicle")

    # ---------------------------------------------------------------- Rumpf
    m.bone("body", pivot=(0, -7, 0))
    m.cube("body", (-9, -2, -6), (18, 12, 11), "chest")
    m.cube("body", (-3.5, 0.5, -6.7), (7, 5.5, 0.7), "grate")                 # Gitter vor dem Schmiedefeuer
    m.cube("body", (-5.5, -3.2, -5), (11, 1.4, 9.5), "gorget")                # Halsberge
    m.cube("body", (-1.6, -6.4, 4.2), (3.2, 5, 2.6), "flue")                  # Rauchabzug auf dem Rücken
    m.cube("body", (-2.1, -7.4, 3.7), (4.2, 1.2, 3.6), "flue_cap")
    m.cube("body", (-4.5, 10, -3), (9, 5, 6), "waist", inflate=0.5)          # Hüfte mit Gurt

    # ---------------------------------------------------------------- Arme (Drehpunkte wie Vanilla)
    for side, sx in (("right", -1), ("left", 1)):
        arm = f"{side}_arm"
        m.bone(arm, pivot=(0, -7, 0))

        def box(x0, y, z, w, h, d, mat, inflate=0.0):
            # x0 = innere Kante (zum Körper hin), w nach außen
            ox = -x0 - w if sx < 0 else x0
            m.cube(arm, (ox, y, z), (w, h, d), mat, inflate=inflate)

        box(8.2, -4.2, -4.2, 6.6, 5.2, 8.4, "pauldron")                       # Schulterstück
        box(8.6, 0.6, -3.8, 5.8, 2.6, 7.6, "lame")                            # Folgeschiene
        box(9.6, 2.6, -2.4, 3.2, 8.4, 4.8, "upper")                           # Oberarm (Gelenkstange mit Bändern)
        box(9.0, 10.4, -3.2, 4.4, 3.4, 6.4, "elbow")                          # Ellbogenkachel
        box(8.8, 13.4, -3.4, 4.8, 8.4, 6.8, "vambrace")                       # Armschiene
        box(8.5, 21.4, -3.7, 5.4, 6.4, 7.4, "fist")                           # Panzerhandschuh
        box(8.0, -4.6, -4.6, 7.4, 0.8, 9.2, "pad_pauldron")                   # Polster oben auf der Schulter
        for z, h in ((-3.4, 1.8), (-0.6, 2.6), (2.4, 1.6)):
            box(13.4, 3.2, z, 0.8, h, 0.8, "icicle")                          # Eiszapfen an der Folgeschiene

    # ---------------------------------------------------------------- Beine (Drehpunkte wie Vanilla)
    for side, px in (("right", -4.0), ("left", 5.0)):
        leg = f"{side}_leg"
        m.bone(leg, pivot=(px, 11, 0))
        m.cube(leg, (-3.7, -3.2, -3.4), (6.4, 7.2, 5.8), "thigh")             # Beintasche
        m.cube(leg, (-3.4, 4.0, -3.0), (5.8, 6.6, 5.2), "shin")               # Beinröhre
        m.cube(leg, (-3.9, 3.0, -3.9), (6.8, 3.0, 1.4), "knee")               # Kniebuckel
        m.cube(leg, (-4.0, 10.4, -4.9), (7.0, 2.6, 7.4), "sabaton")           # Eisenschuh
    return m


# =============================================================================================
# Bemalen
# =============================================================================================

def _look(ctx: Ctx) -> str:
    return ctx.params.get("variant", "schmiedeeisen")


_LIT = {"top": 0.08, "front": 0.03, "west": 0.0, "east": -0.02, "back": -0.05, "bottom": -0.14}
_PITS = Voronoi3D(2.6, 17)


def _bevel(ctx: Ctx) -> float:
    """Gerundete Kanten wie bei Vanilla: obere Kante einer Fläche hell, untere dunkel, Seitenränder etwas dunkler."""
    if ctx.face in ("top", "bottom"):
        edge = ctx.s == 0 or ctx.s == ctx.w - 1 or ctx.t == 0 or ctx.t == ctx.h - 1
        return -0.05 if edge else 0.0
    v = 0.0
    if ctx.t == 0:
        v += 0.1
    elif ctx.t == ctx.h - 1:
        v -= 0.12
    if ctx.s == 0 or ctx.s == ctx.w - 1:
        v -= 0.05
    return v


def _dome(ctx: Ctx, amount: float = 0.07) -> float:
    """Gewölbte Platte: Mitte der Fläche heller, zu den Seiten hin dunkler."""
    if ctx.w <= 2:
        return 0.0
    u = ctx.s / (ctx.w - 1) * 2.0 - 1.0
    return amount * (1.0 - u * u) - amount * 0.4


def _iron(ctx: Ctx, base: float = 0.5, seed: int = 1, hammer: float = 1.0, dome: float = 0.07) -> tuple:
    """Geschmiedetes Eisen: ruhige, gewölbte Fläche, flache Hammerschläge, oben heller, gerundete Kanten."""
    x, y, z = ctx.world
    v = base + _LIT[ctx.face] + _bevel(ctx) + _dome(ctx, dome)
    v += (noise3(x * 0.2, y * 0.2, z * 0.2, seed) - 0.5) * 0.08
    if hammer:
        val, edge = _PITS(x + seed * 3.1, y, z)
        if edge < 0.16:
            v -= 0.035 * hammer                                           # Rand eines Hammerschlags
        elif val > 0.78:
            v += 0.03 * hammer
    v += (hash01(ctx.u, ctx.v, seed) - 0.5) * 0.025
    return _weather(ctx, pick(ctx.pal["iron"], v))


def _rivet(ctx: Ctx) -> tuple:
    key = "iron" if _look(ctx) == "rostwaechter" else "brass"
    return _weather(ctx, pick(ctx.pal[key], 0.86 if key == "brass" else 0.78), rivet=True)


def _rivets(ctx: Ctx, cond) -> tuple | None:
    """Niete (cond(s, t) wahr) mit einem Schattenpunkt darunter."""
    if cond(ctx.s, ctx.t):
        return _rivet(ctx)
    if ctx.t > 0 and cond(ctx.s, ctx.t - 1) and ctx.face not in ("top", "bottom"):
        return _weather(ctx, pick(ctx.pal["iron"], 0.24))
    return None


def _weather(ctx: Ctx, col, rivet: bool = False):
    """Gestalt-eigene Spuren auf dem Metall: Moos und Efeu, Raureif oder Rost."""
    look = _look(ctx)
    x, y, z = ctx.world
    face = ctx.face
    if look == "mooswaechter":
        n = noise3(x * 0.3, y * 0.3, z * 0.3, 41)
        if face == "top" and n > 0.48:
            return pick(ctx.pal["moss"], 0.32 + (n - 0.48) * 1.5 + (hash01(ctx.u, ctx.v, "m") - 0.5) * 0.18)
        if face in ("front", "west", "east", "back") and ctx.t <= (1 if n > 0.62 else 0) and n > 0.52:
            return pick(ctx.pal["moss"], 0.3 + (n - 0.52) * 1.2)          # Moos hängt über die Oberkanten
        if not rivet and _ivy(ctx):
            return _leaf(ctx)
        g = noise3(x * 0.12, y * 0.12, z * 0.12, 43)
        if g > 0.64 and not rivet:
            return mix(col, pick(ctx.pal["moss"], 0.35), (g - 0.64) * 1.4)  # grünliche Patina
        return col
    if look == "frostwaechter":
        n = noise3(x * 0.35, y * 0.35, z * 0.35, 51)
        if face == "top" and n > 0.3:
            return pick(ctx.pal["rime"], 0.58 + (n - 0.3) * 0.7 + (hash01(ctx.u, ctx.v, "r") - 0.5) * 0.12)
        if face not in ("top", "bottom") and ctx.t <= (1 if n > 0.55 else 0) and n > 0.35:
            return pick(ctx.pal["rime"], 0.66)                            # Reif an der Oberkante
        if face != "bottom" and not rivet:
            crystal = hash01(math.floor(x * 1.5), math.floor(y * 1.5), math.floor(z * 1.5), 53)
            if crystal > 0.985:
                return pick(ctx.pal["rime"], 0.92)                        # Eiskristall
            f = noise3(x * 0.2, y * 0.2, z * 0.2, 55)
            if f > 0.62:
                return mix(col, pick(ctx.pal["rime"], 0.45), (f - 0.62) * 1.6)  # Reifschleier
        return col
    if look == "rostwaechter":
        n = noise3(x * 0.24, y * 0.24, z * 0.24, 61)
        streak = noise3(x * 1.3, y * 0.06, z * 1.3, 63)
        low = face not in ("top", "bottom") and ctx.t >= ctx.h - 2 and n > 0.4
        run = face not in ("top", "bottom") and streak > 0.8 and n > 0.42
        if n > 0.74 or low or run:
            t = min(1.0, max(n - 0.6, streak - 0.7, 0.15) * 2.4)
            r = pick(ctx.pal["rust"], 0.3 + t * 0.36 + (hash01(ctx.u, ctx.v, "r") - 0.5) * 0.14)
            if n > 0.8 and hash01(math.floor(x * 2), math.floor(y * 2), math.floor(z * 2), 65) > 0.96:
                r = pick(ctx.pal["rust"], 0.2)                            # abgeplatzter Rost
            return r
        if n > 0.6 and not rivet:
            return mix(col, pick(ctx.pal["rust"], 0.36), (n - 0.6) * 4.0)
        return col
    return col


# ------------------------------------------------------------------------------- Efeu (Mooswächter)

def _ivy(ctx: Ctx) -> bool:
    """Efeuranken: hängen von Schultern und Helm herab und schlängeln sich über Brust und Arme."""
    if ctx.face in ("top", "bottom"):
        return False
    x, y, z = ctx.world
    c = x if ctx.face in ("front", "back") else z
    for c0, y0, y1, ph in ((-6.2, -9.0, 1.5, 0.4), (5.6, -9.0, -1.5, 1.7), (-12.0, -4.0, 14.0, 2.3),
                           (12.0, -4.0, 9.0, 0.9), (2.4, -18.0, -12.5, 1.2), (-1.5, 3.0, 18.0, 2.8)):
        if not (y0 <= y <= y1):
            continue
        wave = c0 + 0.8 * math.sin(y * 0.55 + ph)
        if abs(c - wave) < 0.5:
            return True
        k = (y * 0.5 + ph) % 2.0
        side = 1 if math.floor(y * 0.5 + ph) % 2 == 0 else -1
        if k < 0.9 and 0.5 <= (c - wave) * side < 1.6:
            return True                                                   # Blätter beidseits der Ranke
    return False


def _leaf(ctx: Ctx):
    x, y, z = ctx.world
    if hash01(math.floor(x * 2), math.floor(y * 2), math.floor(z * 2), "bloom") > 0.94:
        return pick(ctx.pal["bloom"], 0.7)                                # kleine Blüte
    return pick(ctx.pal["leaf"], 0.3 + noise3(x * 0.8, y * 0.8, z * 0.8, 47) * 0.55)


# ------------------------------------------------------------------------------- Kopf

def _helm(ctx: Ctx):
    """
    Topfhelm. Vorn (10 Reihen): Stirn (0–2, unter dem Stirnreif), Sehschlitz mit zwei glühenden Augen (3),
    Schlitzschatten (4), Gesichtsplatte mit Mittelgrat und Atemlöchern (5–6), darunter verdeckt das Kinnreff den Rest.
    """
    face, s, t = ctx.face, ctx.s, ctx.t
    if face == "front":
        if t == 3 and 0 < s < 7:
            if s in (1, 2, 5, 6):
                inner = s in (2, 5)
                col = pick(ctx.pal["ember"], 0.96 if inner else 0.74)
                ctx.glow = col
                return col
            return pick(ctx.pal["dark"], 0.5)
        if t == 4 and 0 < s < 7:
            if s in (1, 2, 5, 6):
                ctx.glow = pick(ctx.pal["ember"], 0.25)
                return pick(ctx.pal["ember"], 0.18)                       # Glutschein unter den Augen
            return pick(ctx.pal["dark"], 0.35)
        if s in (3, 4) and t >= 5:
            v = 0.7 if s == 3 else 0.42                                   # Mittelgrat
            return _weather(ctx, pick(ctx.pal["iron"], v))
        if (t == 5 and s in (1, 6)) or (t == 6 and s in (1, 2, 5, 6)):
            return pick(ctx.pal["dark"], 0.45 if (s + t) % 2 else 0.3)    # Atemlöcher
        if t == 2:
            return _iron(ctx, 0.34, seed=3, dome=0.0)                     # Schatten unter dem Stirnreif
        return _iron(ctx, 0.53, seed=3)
    if face in ("west", "east"):
        front_col = ctx.w - 1 if face == "west" else 0
        if t in (3, 4) and s == front_col:
            return pick(ctx.pal["dark"], 0.5)                             # Schlitz läuft um die Ecke
        if s in (3, 4) and t in (4, 5, 6) and (s + t) % 2 == 0:
            return pick(ctx.pal["iron"], 0.22)                            # Luftlöcher an der Schläfe
        r = _rivets(ctx, lambda a, b: b == 8 and a in (1, 6))
        return r if r is not None else _iron(ctx, 0.48, seed=4)
    if face == "back":
        return _iron(ctx, 0.44, seed=6)
    if face == "top":
        return _iron(ctx, 0.56, seed=5)
    return _iron(ctx, 0.4, seed=6)


def _crest(ctx: Ctx):
    """Eiserner Kamm mit Messingkante."""
    if ctx.face == "top":
        return _weather(ctx, pick(ctx.pal["brass"], 0.8 if ctx.t % 3 else 0.62), rivet=True)
    if ctx.face == "bottom":
        return pick(ctx.pal["iron"], 0.2)
    if ctx.t == 0:
        return _weather(ctx, pick(ctx.pal["brass"], 0.66), rivet=True)
    return _iron(ctx, 0.56 - ctx.t * 0.08, seed=7, hammer=0, dome=0.0)


def _brow(ctx: Ctx):
    if ctx.face == "bottom":
        return pick(ctx.pal["dark"], 0.8)
    if ctx.face == "front":
        r = _rivets(ctx, lambda a, b: b == 0 and a in (1, 4, 7))
        if r is not None and ctx.t == 0:
            return r
        return _weather(ctx, pick(ctx.pal["brass"], 0.62 - ctx.t * 0.22))
    return _weather(ctx, pick(ctx.pal["brass"], 0.52 + _LIT[ctx.face]))


def _bevor(ctx: Ctx):
    if ctx.face == "front":
        if ctx.t == 0:
            return _iron(ctx, 0.7, seed=8, hammer=0, dome=0.0)
        if ctx.t == 2 and ctx.s % 2 == 0 and 0 < ctx.s < ctx.w - 1:
            return pick(ctx.pal["dark"], 0.55)                            # Kerben im Kinnreff
        return _iron(ctx, 0.5, seed=8)
    if ctx.face == "top":
        return pick(ctx.pal["iron"], 0.28)
    return _iron(ctx, 0.42, seed=9, dome=0.0)


# ------------------------------------------------------------------------------- Polster und Eiszapfen

def _pad(ctx: Ctx):
    """Moospolster bzw. Schneedecke oben auf Helm und Schultern – nur bei Moos- und Frostwächter, unregelmäßig."""
    look = _look(ctx)
    if look not in ("mooswaechter", "frostwaechter") or ctx.face == "bottom":
        return None
    x, y, z = ctx.world
    n = noise3(x * 0.42, 0.0, z * 0.42, 71 if look == "mooswaechter" else 73)
    if ctx.face == "top":
        if n < 0.4:
            return None
        if look == "mooswaechter":
            if hash01(math.floor(x * 2), math.floor(z * 2), "flower") > 0.95:
                return pick(ctx.pal["bloom"], 0.8)
            return pick(ctx.pal["moss"], 0.45 + (n - 0.4) * 0.8 + (hash01(ctx.u, ctx.v) - 0.5) * 0.18)
        return pick(ctx.pal["rime"], 0.74 + (n - 0.4) * 0.4)
    if n < 0.5:
        return None                                                       # Saum nur unter dichtem Polster
    if look == "mooswaechter":
        return pick(ctx.pal["moss"], 0.28 + (n - 0.5) * 0.6)
    return pick(ctx.pal["rime"], 0.6)


def _icicle(ctx: Ctx):
    if _look(ctx) != "frostwaechter":
        return None
    if ctx.face in ("top", "bottom"):
        return pick(ctx.pal["ice"], 0.8 if ctx.face == "top" else 0.5)
    return pick(ctx.pal["ice"], 0.9 - rel_y(ctx) * 0.45 + (0.06 if ctx.s == 0 else 0.0))


# ------------------------------------------------------------------------------- Rumpf

def _chest(ctx: Ctx):
    """
    Brustharnisch: vorn eine gewölbte Brustplatte mit Mittelgrat, Ruß um das Schmiedefeuer und eine Bauchplatte
    darunter; hinten Rückenplatte mit Naht; an den Flanken zwei Spangen.
    """
    face, s, t = ctx.face, ctx.s, ctx.t
    x, y, z = ctx.world
    ly = y + 7.0
    if face == "front":
        r = _rivets(ctx, lambda a, b: (b == 1 and a in (1, ctx.w - 2)) or (b == 10 and a in (2, 6, 11, 15)))
        if r is not None:
            return r
        if t == 9:
            return pick(ctx.pal["iron"], 0.22)                            # Fuge zur Bauchplatte
        if abs(x) < 0.5 and (t < 2 or t > 9):
            return _iron(ctx, 0.66, seed=11, hammer=0, dome=0.0)          # Grat
        col = _iron(ctx, 0.52 if t < 9 else 0.46, seed=12, dome=0.1)
        if 0.0 < ly < 7.5 and abs(x) < 5.5:
            soot = max(0.0, 1.0 - max(0.0, abs(x) - 3.4) / 2.1) * max(0.0, 1.0 - max(0.0, 0.6 - ly) * 0.8)
            return mix(col, pick(ctx.pal["soot"], 0.45), 0.5 * soot)      # Ruß rund um das Feuer
        return col
    if face == "back":
        if abs(x) < 2.4 and t < 6:
            return mix(_iron(ctx, 0.44, seed=13), pick(ctx.pal["soot"], 0.4), 0.55 - t * 0.08)  # Ruß am Abzug
        if s in (ctx.w // 2 - 1, ctx.w // 2) and t >= 6:
            return pick(ctx.pal["iron"], 0.3 if s == ctx.w // 2 else 0.5)  # Rückennaht
        return _iron(ctx, 0.45, seed=13, dome=0.1)
    if face in ("west", "east"):
        if s in (3, 8) and 1 < t < 10:
            return pick(ctx.pal["leather"], 0.42 if t % 3 else 0.6)       # Lederspangen an der Flanke
        return _iron(ctx, 0.47, seed=14)
    if face == "top":
        return _iron(ctx, 0.56, seed=15, dome=0.0)
    return _iron(ctx, 0.34, seed=16, dome=0.0)


def _grate(ctx: Ctx):
    """Ofengitter: Messingrahmen, Stäbe, dahinter das Schmiedefeuer – unten weißgelbe Glut, oben dunkelrote Flammen."""
    face, s, t = ctx.face, ctx.s, ctx.t
    if face != "front":
        return pick(ctx.pal["brass"], 0.42 + _LIT[face])
    if s in (0, ctx.w - 1) or t in (0, ctx.h - 1):
        if (s in (0, ctx.w - 1)) and (t in (0, ctx.h - 1)):
            return pick(ctx.pal["brass"], 0.92)                           # Nieten an den Ecken
        return pick(ctx.pal["brass"], 0.7 if t == 0 else 0.5 if s == 0 else 0.42)
    if s % 2 == 0:
        return pick(ctx.pal["iron"], 0.3 if t % 2 else 0.38)             # Gitterstab
    depth = (t - 1) / max(1, ctx.h - 3)                                   # 0 oben … 1 unten
    flick = hash01(ctx.u, ctx.v, "ember")
    v = 0.3 + depth * 0.55 + (flick - 0.5) * 0.2
    col = pick(ctx.pal["ember"], min(0.99, max(0.05, v)))
    ctx.glow = col
    return col


def _gorget(ctx: Ctx):
    if ctx.face == "top":
        if ctx.t in (0, ctx.h - 1) or ctx.s in (0, ctx.w - 1):
            return _weather(ctx, pick(ctx.pal["brass"], 0.66))
        return pick(ctx.pal["dark"], 0.7)                                 # Halsöffnung unter dem Helm
    if ctx.face == "bottom":
        return pick(ctx.pal["iron"], 0.2)
    if ctx.face == "front" and ctx.s % 4 == 1 and ctx.t == 0:
        return _rivet(ctx)
    return _weather(ctx, pick(ctx.pal["brass"], 0.56 - ctx.t * 0.16 + _LIT[ctx.face]))


def _flue(ctx: Ctx):
    if ctx.face == "top":
        return pick(ctx.pal["soot"], 0.2)
    v = 0.42 + _LIT[ctx.face] - rel_y(ctx) * 0.1 + (0.05 if ctx.t % 2 == 0 else 0.0)
    col = pick(ctx.pal["iron"], v)
    return _weather(ctx, mix(col, pick(ctx.pal["soot"], 0.45), 0.4 * (1.0 - rel_y(ctx))))


def _flue_cap(ctx: Ctx):
    if ctx.face == "top":
        if 0 < ctx.s < ctx.w - 1 and 0 < ctx.t < ctx.h - 1:
            col = pick(ctx.pal["ember"], 0.38)                            # Glut tief im Abzug
            ctx.glow = pick(ctx.pal["ember"], 0.32)
            return col
        return pick(ctx.pal["soot"], 0.55)
    return mix(pick(ctx.pal["iron"], 0.52 + _LIT[ctx.face] - ctx.t * 0.2), pick(ctx.pal["soot"], 0.4), 0.45)


def _waist(ctx: Ctx):
    """Hüftstück mit Ledergurt und Messingschnalle."""
    face, s, t = ctx.face, ctx.s, ctx.t
    if face in ("top", "bottom"):
        return _iron(ctx, 0.38, seed=21, dome=0.0)
    if t in (1, 2):
        if face == "front" and ctx.w // 2 - 1 <= s <= ctx.w // 2 + 1:
            return pick(ctx.pal["brass"], 0.88 if t == 1 and s == ctx.w // 2 else 0.6)  # Schnalle
        return pick(ctx.pal["leather"], 0.5 + (0.1 if t == 1 else -0.06) + (hash01(ctx.u, ctx.v) - 0.5) * 0.1)
    if t == 3:
        return pick(ctx.pal["leather"], 0.2)
    return _iron(ctx, 0.46 - (t - 3) * 0.05, seed=22)


# ------------------------------------------------------------------------------- Arme

def _pauldron(ctx: Ctx):
    face, s, t = ctx.face, ctx.s, ctx.t
    if face == "bottom":
        return pick(ctx.pal["iron"], 0.22)
    if face == "top":
        x = ctx.world[0]
        return _iron(ctx, 0.6 - abs(abs(x) - 11.5) * 0.035, seed=31, dome=0.0)
    if t == ctx.h - 1:
        return _weather(ctx, pick(ctx.pal["brass"], 0.52))               # Messingkante unten
    r = _rivets(ctx, lambda a, b: b == ctx.h - 3 and a % 3 == 1)
    return r if r is not None else _iron(ctx, 0.54 - t * 0.025, seed=32, dome=0.09)


def _lame(ctx: Ctx):
    if ctx.face in ("top", "bottom"):
        return pick(ctx.pal["iron"], 0.25)
    if ctx.t == 0:
        return _iron(ctx, 0.66, seed=33, hammer=0, dome=0.0)
    if ctx.t == ctx.h - 1:
        return pick(ctx.pal["iron"], 0.24)
    return _iron(ctx, 0.48, seed=34)


def _upper(ctx: Ctx):
    """Oberarm: dunkle Gelenkstange mit zwei Eisenbändern."""
    if ctx.face in ("top", "bottom"):
        return pick(ctx.pal["iron"], 0.3)
    if ctx.t in (2, 3, 6, 7):
        return _iron(ctx, 0.52 if ctx.t in (2, 6) else 0.4, seed=35, hammer=0)
    return _weather(ctx, pick(ctx.pal["iron"], 0.26 + (hash01(ctx.u, ctx.v) - 0.5) * 0.04 + _LIT[ctx.face] * 0.5))


def _elbow(ctx: Ctx):
    if ctx.face in ("top", "bottom"):
        return pick(ctx.pal["iron"], 0.3 if ctx.face == "top" else 0.2)
    if ctx.face in ("west", "east"):
        r = _rivets(ctx, lambda a, b: b == 1 and a == ctx.w // 2)
        if r is not None:
            return r
    return _iron(ctx, 0.56 - ctx.t * 0.07, seed=36)


def _vambrace(ctx: Ctx):
    face, s, t = ctx.face, ctx.s, ctx.t
    if face == "top":
        return pick(ctx.pal["iron"], 0.32)
    if face == "bottom":
        return pick(ctx.pal["iron"], 0.2)
    if t == 0:
        return _weather(ctx, pick(ctx.pal["brass"], 0.62))
    if t == ctx.h - 1:
        return pick(ctx.pal["iron"], 0.22)
    if t == 4:
        return pick(ctx.pal["iron"], 0.28)                                # Fuge der Armschiene
    return _iron(ctx, 0.52 if t < 4 else 0.47, seed=37)


def _fist(ctx: Ctx):
    """Panzerhandschuh: Knöchelplatte, Fingerglieder vorn und unten, Daumen innen."""
    face, s, t = ctx.face, ctx.s, ctx.t
    if face == "top":
        return pick(ctx.pal["iron"], 0.3)
    if face == "bottom":
        return pick(ctx.pal["iron"], 0.36 if s % 2 else 0.22)             # Fingerkuppen
    if t in (0, 1):
        return _iron(ctx, 0.62 - t * 0.12, seed=38, hammer=0, dome=0.0)   # Knöchelplatte
    if face in ("front", "back"):
        if t >= 3 and s % 2 == 1:
            return pick(ctx.pal["iron"], 0.26)                            # Fugen zwischen den Fingern
        return _iron(ctx, 0.5 - (t - 2) * 0.03, seed=39, dome=0.0)
    x = ctx.world[0]
    inner = (face == "east") == (x < 0)
    if inner and 2 <= t <= 4 and 2 <= s <= 4:
        return _iron(ctx, 0.58 if t == 2 else 0.44, seed=40, dome=0.0)   # Daumen
    return _iron(ctx, 0.46, seed=41)


# ------------------------------------------------------------------------------- Beine

def _thigh(ctx: Ctx):
    face, t = ctx.face, ctx.t
    if face in ("top", "bottom"):
        return pick(ctx.pal["iron"], 0.28)
    if t in (3,):
        return pick(ctx.pal["iron"], 0.24)                                # Fuge der Beintaschen
    if t in (0, 4):
        return _iron(ctx, 0.62, seed=51, hammer=0, dome=0.0)
    return _iron(ctx, 0.49 - (t % 4) * 0.02, seed=52, dome=0.09)


def _shin(ctx: Ctx):
    if ctx.face in ("top", "bottom"):
        return pick(ctx.pal["iron"], 0.26)
    if ctx.face == "front" and ctx.s == ctx.w // 2:
        return _iron(ctx, 0.66, seed=53, hammer=0, dome=0.0)              # Grat der Beinröhre
    return _iron(ctx, 0.47, seed=54, dome=0.1)


def _knee(ctx: Ctx):
    if ctx.face == "bottom":
        return pick(ctx.pal["iron"], 0.2)
    if ctx.face == "front":
        r = _rivets(ctx, lambda a, b: b == 1 and a == ctx.w // 2)
        if r is not None:
            return r
    return _iron(ctx, 0.62 - ctx.t * 0.09, seed=55)


def _sabaton(ctx: Ctx):
    face, s, t = ctx.face, ctx.s, ctx.t
    if face == "bottom":
        return pick(ctx.pal["dark"], 0.9)
    if face == "top":
        if t >= ctx.h - 3 and s % 2 == 0:
            return pick(ctx.pal["iron"], 0.34)                            # Zehenschienen
        return _iron(ctx, 0.52, seed=56, dome=0.0)
    if t == ctx.h - 1:
        return pick(ctx.pal["iron"], 0.18)
    return _iron(ctx, 0.47 - t * 0.04, seed=57)


def materials(variant: str) -> dict:
    return {
        "helm": _helm, "crest": _crest, "brow": _brow, "bevor": _bevor, "pad_helm": _pad,
        "pad_pauldron": _pad, "icicle": _icicle, "chest": _chest, "grate": _grate, "gorget": _gorget, "flue": _flue,
        "flue_cap": _flue_cap, "waist": _waist, "pauldron": _pauldron, "lame": _lame, "upper": _upper, "elbow": _elbow,
        "vambrace": _vambrace, "fist": _fist, "thigh": _thigh, "shin": _shin, "knee": _knee, "sabaton": _sabaton,
    }


# =============================================================================================
# Risse (Ebene über dem Modell, wie Vanillas iron_golem_crackiness_*.png)
# =============================================================================================

_CRACK_LEVELS = {"low": (0.3, 4, 0), "medium": (0.6, 7, 1), "high": (0.9, 10, 2)}
_SKIP_CRACKS = ("icicle", "pad_helm", "pad_pauldron", "grate", "flue_cap", "crest", "brow", "gorget")


def crack_overlay(model: Model, level: str) -> Image.Image:
    """
    Risse über dem Modell wie bei Vanilla: gezackte Linien, die von einem Punkt einer Fläche aus laufen und sich
    verzweigen – je stärker beschädigt, desto mehr Flächen haben Risse und desto länger sind sie (die Risse der
    schwächeren Stufe bleiben in der stärkeren erhalten). In den langen Rissen der höchsten Stufe glimmt das Feuer.
    """
    import random
    res = getattr(model, "tex_res", 1)
    img = Image.new("RGBA", (model.tex_w * res, model.tex_h * res), (0, 0, 0, 0))
    px = img.load()
    share, length, branches = _CRACK_LEVELS[level]
    dark, lip, ember = (24, 20, 18, 255), (168, 168, 172, 255), (255, 132, 36, 255)
    for ci, cube in enumerate(model.all_cubes()):
        if cube.material in _SKIP_CRACKS:
            continue
        for face, (u0, v0, fw, fh) in face_rects(cube).items():
            fw, fh = fw * res, fh * res
            if face == "bottom" or fw * fh < 12 or min(fw, fh) < 3:
                continue
            rng = random.Random(f"{ci}-{face}")
            if rng.random() > share:
                continue
            cracks = max(1, (fw * fh) // 60)
            for k in range(cracks):
                s, t = rng.randrange(fw), rng.randrange(fh)
                ds, dt = rng.choice(((1, 1), (1, -1), (-1, 1), (-1, -1), (1, 0), (0, 1)))
                path = []
                for step in range(length + rng.randrange(3)):
                    if not (0 <= s < fw and 0 <= t < fh):
                        break
                    path.append((s, t))
                    r = rng.random()
                    if r < 0.3:
                        s += ds
                    elif r < 0.6:
                        t += dt
                    else:
                        s += ds
                        t += dt
                for i, (a, b) in enumerate(path):
                    deep = level == "high" and len(path) > 6 and 2 <= i < len(path) - 2
                    px[u0 + a, v0 + b] = ember if deep and i % 2 == 0 else dark
                    if 0 <= a + 1 < fw and 0 <= b - 1 < fh and px[u0 + a + 1, v0 + b - 1][3] == 0 and rng.random() < 0.35:
                        px[u0 + a + 1, v0 + b - 1] = lip                 # aufgebogener Rand
                for _ in range(branches):
                    if len(path) < 4:
                        break
                    a, b = path[rng.randrange(1, len(path) - 1)]
                    da, db = rng.choice(((1, -1), (-1, 1), (1, 1), (-1, -1)))
                    for _step in range(2 + rng.randrange(3)):
                        a, b = a + da, b + (db if rng.random() < 0.7 else 0)
                        if 0 <= a < fw and 0 <= b < fh:
                            px[u0 + a, v0 + b] = dark
    return img


def hidden(variant: str) -> list[str]:
    return []
