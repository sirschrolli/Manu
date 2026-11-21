"""
Karkinos, der Riffkoloss: ein uralter Einsiedler der Tiefe, so groß wie ein Haus. Als der Tempel des Meeresvolks in
den Fluten versank, kroch er in dessen Kuppel und machte sie zu seinem Gehäuse – im Gestein steckt bis heute der
Gezeiten-Speer des letzten Gezeitenkönigs.

  * ein breiter, korallenroter Panzer mit Dornen am Vorderrand, Seepocken und Algenflecken, darunter ein heller Bauch
  * zwei Stielaugen, die türkis glimmen, lange Fühler und Mundplatten, die sich öffnen
  * links die gewaltige Brecherschere (Seepocken und Korallen auf dem Rücken), rechts die schlanke Greifschere mit
    gezahnter Schneide; je mit beweglichem Finger
  * sechs lange, gebänderte Laufbeine in drei Gliedern
  * auf dem Rücken die Kuppel des Tempels aus Meeresmarmor mit Goldsaum und Pilastern, überwachsen von Korallen und
    Anemonen, an ihrem Rand hängen Tangsträhnen; schräg darin steckt der leuchtende Gezeiten-Speer

Knochen-Namen werden in ReefColossusModel (Java) für die Animationen verwendet. Im Spiel ×2.
"""
from __future__ import annotations

import math

from ..geometry import Model
from ..paint import Ctx, hash01, hexc, mix, noise3, pick

PALETTES = {
    "reef_colossus": {
        "shell": [hexc(c) for c in ("#4a120c", "#6e1c12", "#932a18", "#b23c1e", "#cc5428", "#e07038", "#f09858")],
        "spot": [hexc(c) for c in ("#3a0c08", "#521410")],
        "belly": [hexc(c) for c in ("#a88a70", "#c4a688", "#dcc4a6", "#efe0c8")],
        "leg": [hexc(c) for c in ("#7a2414", "#9c3418", "#bc4a20", "#d4642c", "#e8864a")],
        "tip": [hexc(c) for c in ("#140c0a", "#2a1a14", "#40281e")],
        "eye": [hexc(c) for c in ("#0a3a3a", "#14787a", "#2ad0c8", "#8affea", "#e0fff8")],
        "dome": [hexc(c) for c in ("#4e6c68", "#6a8c86", "#86a8a0", "#a2c0b8", "#bcd4cc", "#d6e8e0")],
        "vein": [hexc(c) for c in ("#3c5a56", "#4a6a64")],
        "gold": [hexc(c) for c in ("#6a4a18", "#a8782a", "#d4a440", "#f0cc68", "#fff0a8")],
        "algae": [hexc(c) for c in ("#22401a", "#335a22", "#4a7a2c")],
        "barnacle": [hexc(c) for c in ("#7a746a", "#a8a294", "#d0cabc", "#ece8de")],
        "coral_red": [hexc(c) for c in ("#7a1020", "#b01e2e", "#e04050", "#ff8088")],
        "coral_pink": [hexc(c) for c in ("#983a62", "#c45a84", "#e886a8", "#ffbcd0")],
        "coral_yellow": [hexc(c) for c in ("#987010", "#c8a020", "#ecd040", "#fff08a")],
        "anemone": [hexc(c) for c in ("#4a2272", "#7a34a8", "#a856d0", "#f0b8ff")],
        "kelp": [hexc(c) for c in ("#1e2a0e", "#2e4014", "#44581c", "#5e7426", "#7a9030")],
        "wood": [hexc(c) for c in ("#5a4a3a", "#7a6a56", "#a0907a", "#c8baa2")],
        "blade": [hexc(c) for c in ("#0a4050", "#126e80", "#22a4b8", "#5adce6", "#c4fff6")],
        "pearl": [hexc(c) for c in ("#c8b8d4", "#e8dcf0", "#ffffff")],
        "mouth": [hexc(c) for c in ("#240808", "#3e1214", "#5a1c20")],
        "hole": [hexc(c) for c in ("#0a1416", "#122024", "#1c2e32")],
    },
}

BODY = (0.0, 14.0, -4.0)
SHELL = (0.0, -1.0, 11.0)          # relativ zum Körper
SHELL_ABS = (BODY[0] + SHELL[0], BODY[1] + SHELL[1], BODY[2] + SHELL[2])
# Stufen der Kuppel: (y von, y bis, Halbbreite) relativ zum Gehäuse
TIERS = [(-3.0, 1.0, 12.0), (-6.5, -3.0, 11.2), (-9.5, -6.5, 9.6), (-12.0, -9.5, 7.4), (-14.0, -12.0, 4.8)]


def _octagon(m: Model, bone: str, y0: float, y1: float, r: float, material: str) -> None:
    """Eine achteckige Stufe aus drei Quadern (lang in x, lang in z, und die Ecken)."""
    h = y1 - y0
    short = r * 0.62
    corner = r * 0.86
    m.cube(bone, (-r, y0, -short), (2 * r, h, 2 * short), material)
    m.cube(bone, (-short, y0, -r), (2 * short, h, 2 * r), material)
    m.cube(bone, (-corner, y0, -corner), (2 * corner, h, 2 * corner), material)


def build() -> Model:
    m = Model("reef_colossus", 512, 512)
    m.tex_res = 1
    m.world_faces = False
    m.bone("body", pivot=BODY)
    # Panzer: breit, vorn mit Dornen, oben zwei Wölbungen
    m.cube("body", (-11, -5, -10), (22, 9, 14), "shell")
    m.cube("body", (-9, -6.5, -9), (18, 1.5, 11), "shell")
    m.cube("body", (-6, -7.5, -7), (12, 1.0, 7), "shell")
    m.cube("body", (-12, -3, -8), (24, 5, 9), "shell")                              # Seitenwülste
    for x in (-8.0, -4.0, 0.0, 4.0, 8.0):
        m.cube("body", (x - 0.9, -4.6, -11.6), (1.8, 1.8, 1.8), "spike")            # Dornen am Vorderrand
    m.cube("body", (-1.3, -5.2, -12.6), (2.6, 2.2, 2.6), "spike")                   # Stirnhorn
    for sx in (-1, 1):
        m.cube("body", (12.0 * sx - (1.6 if sx > 0 else 0.0), -2.4, -6.5), (1.6, 1.6, 1.6), "spike")   # Seitendornen
    m.cube("body", (-9, 4, -9), (18, 1.5, 12), "belly")                             # Bauchplatten
    # Mundplatten (öffnen sich beim Blasenstrahl)
    for side, sx in (("left", 1), ("right", -1)):
        bone = f"mouth_{side}"
        m.bone(bone, "body", pivot=(0.2 * sx, 1.6, -10.2))
        m.cube(bone, (0 if sx > 0 else -3.4, 0, -1.2), (3.4, 3.8, 1.2), "mouth")
    # Stielaugen
    for side, sx in (("left", 1), ("right", -1)):
        eye = f"{side}_eye"
        m.bone(eye, "body", pivot=(4.5 * sx, -6.5, -8.0))
        m.cube(eye, (-0.9, -6, -0.9), (1.8, 6, 1.8), "stalk")
        m.cube(eye, (-1.6, -9.0, -1.6), (3.2, 3.2, 3.2), "eye")
        # Fühler: zwei Glieder, weit nach vorn und oben
        ant = f"{side}_antenna"
        m.bone(ant, "body", pivot=(2.0 * sx, -3.5, -10.5), rotation=(-32, -18 * sx, 0))
        m.cube(ant, (-0.45, -0.45, -10), (0.9, 0.9, 10), "antenna")
        tip = f"{side}_antenna_tip"
        m.bone(tip, ant, pivot=(0, 0, -10), rotation=(-18, 0, 0))
        m.cube(tip, (-0.3, -0.3, -9), (0.6, 0.6, 9), "antenna")

    # Brecherschere (links, gewaltig) und Greifschere (rechts, schlank)
    m.bone("left_arm", "body", pivot=(10, 1.5, -7), rotation=(0, -30, 0))
    m.cube("left_arm", (0, -3.0, -3.0), (8, 6.0, 6.0), "claw_arm")
    m.bone("left_forearm", "left_arm", pivot=(8, 0, 0), rotation=(0, 75, 0))
    m.cube("left_forearm", (-3.0, -3.0, -7), (6.0, 6.0, 7), "claw_arm")
    m.bone("left_claw", "left_forearm", pivot=(0, 0, -7), rotation=(0, -10, 0))
    m.cube("left_claw", (-5.5, -5.0, -12.5), (11, 10, 12.5), "claw")
    m.cube("left_claw", (-5.2, -2.4, -20.0), (5.4, 6.4, 7.5), "clawtip")             # fester Finger
    for (x, y, z, w, h, d, mat) in ((-2.5, -5.4, -8.0, 2.4, 1.2, 2.4, "coral_red"), (0.5, -5.0, -4.0, 2.0, 0.8, 2.0, "barnacle"),
                                   (-3.8, -4.9, -3.2, 1.6, 0.7, 1.6, "barnacle"), (1.6, -5.6, -9.5, 1.4, 1.4, 1.4, "coral_yellow"),
                                   (-1.0, -6.6, -7.6, 0.8, 1.6, 0.8, "coral_red")):
        m.cube("left_claw", (x, y, z), (w, h, d), mat)
    m.bone("left_claw_finger", "left_claw", pivot=(4.6, 1.2, -12.5), rotation=(0, -8, 0))
    m.cube("left_claw_finger", (-4.4, -2.6, -8.5), (4.4, 5.6, 8.5), "clawtip")

    m.bone("right_arm", "body", pivot=(-10, 1.5, -7), rotation=(0, 30, 0))
    m.cube("right_arm", (-8, -2.1, -2.1), (8, 4.2, 4.2), "claw_arm")
    m.bone("right_forearm", "right_arm", pivot=(-8, 0, 0), rotation=(0, -75, 0))
    m.cube("right_forearm", (-2.1, -2.1, -5), (4.2, 4.2, 5), "claw_arm")
    m.bone("right_claw", "right_forearm", pivot=(0, 0, -5), rotation=(0, 10, 0))
    m.cube("right_claw", (-3.8, -3.6, -10), (7.6, 7.2, 10), "claw")
    m.cube("right_claw", (-3.6, -1.8, -18.5), (3.6, 4.2, 8.5), "cutter")              # fester Finger, gezahnt
    m.cube("right_claw", (-1.5, -4.0, -6.0), (1.6, 0.8, 1.6), "barnacle")
    m.bone("right_claw_finger", "right_claw", pivot=(-3.6, 0.6, -10), rotation=(0, 8, 0))
    m.cube("right_claw_finger", (0, -1.8, -8.8), (3.2, 3.8, 8.8), "cutter")

    # sechs Laufbeine in drei Gliedern
    for side, sx in (("left", 1), ("right", -1)):
        for k in range(3):
            leg = f"{side}_leg_{k}"
            m.bone(leg, "body", pivot=(10.5 * sx, 3.0, -4.5 + k * 5.0), rotation=(0, (k - 1) * 20 * sx, -28 * sx))
            m.cube(leg, (0 if sx > 0 else -9, -1.3, -1.3), (9, 2.6, 2.6), "leg")
            low = leg + "_low"
            m.bone(low, leg, pivot=(9.0 * sx, 0, 0), rotation=(0, 0, 70 * sx))
            m.cube(low, (0 if sx > 0 else -11, -1.05, -1.05), (11, 2.1, 2.1), "leg")
            tip = leg + "_tip"
            m.bone(tip, low, pivot=(11.0 * sx, 0, 0), rotation=(0, 0, 20 * sx))
            m.cube(tip, (0 if sx > 0 else -4, -0.7, -0.7), (4, 1.4, 1.4), "legtip")

    # die Kuppel des versunkenen Tempels
    m.bone("shell", "body", pivot=SHELL)
    for (y0, y1, r) in TIERS:
        _octagon(m, "shell", y0, y1, r, "dome")
    m.cube("shell", (-2.3, -15.4, -2.3), (4.6, 1.4, 4.6), "dome")                  # Scheitel
    m.cube("shell", (-1.0, -17.0, -1.0), (2.0, 1.6, 2.0), "gold")                  # goldener Knauf
    m.cube("shell", (-12.6, 0.2, -8.0), (25.2, 1.2, 16.0), "gold")                 # Goldsaum am Rand
    m.cube("shell", (-8.0, 0.2, -12.6), (16.0, 1.2, 25.2), "gold")
    for k in range(8):                                                             # Pilaster
        a = math.radians(22.5 + k * 45)
        x, z = math.sin(a) * 10.8, math.cos(a) * 10.8
        m.cube("shell", (x - 0.9, -6.0, z - 0.9), (1.8, 6.5, 1.8), "pilaster")
    # Korallen und Anemonen auf der Kuppel
    reef = [
        (-8.0, -6.8, -5.2, 3.0, 1.2, 3.0, "coral_pink"), (-7.6, -8.6, -4.8, 2.2, 1.8, 2.2, "coral_pink"),
        (5.4, -9.8, 3.4, 1.0, 3.4, 1.0, "coral_red"), (6.6, -9.0, 2.8, 1.0, 2.6, 1.0, "coral_red"),
        (4.6, -8.4, 4.6, 1.0, 2.0, 1.0, "coral_red"), (5.8, -11.0, 4.0, 2.4, 0.8, 0.8, "coral_red"),
        (-3.4, -11.6, 4.2, 1.6, 2.6, 1.6, "coral_yellow"), (-2.2, -11.0, 5.2, 1.2, 2.0, 1.2, "coral_yellow"),
        (8.2, -4.4, -5.6, 2.4, 1.4, 2.4, "anemone"), (-9.6, -4.0, 4.6, 2.4, 1.4, 2.4, "anemone"),
        (1.6, -14.2, -2.6, 2.0, 1.2, 2.0, "anemone"), (-9.0, -7.6, 1.6, 1.6, 1.0, 1.6, "barnacle"),
        (9.0, -7.8, -1.0, 1.6, 1.0, 1.6, "barnacle"), (-4.4, -10.4, -5.6, 2.6, 0.8, 2.6, "coral_pink"),
    ]
    for (x, y, z, w, h, d, mat) in reef:
        m.cube("shell", (x, y, z), (w, h, d), mat)
    # Tangsträhnen hängen an den Flanken der Kuppel herab
    for k, (deg, y, length) in enumerate(((40, -5.0, 6.5), (115, -3.5, 5.0), (200, -6.0, 7.0), (290, -4.0, 5.5))):
        a = math.radians(deg)
        x, z = math.sin(a) * 11.4, math.cos(a) * 11.4
        kelp = f"kelp_{k}"
        m.bone(kelp, "shell", pivot=(x, y, z), rotation=(0, -deg, 8 if k % 2 else -8))
        m.cube(kelp, (-1.3, 0, -0.25), (2.6, length, 0.5), "kelp")
    # der Gezeiten-Speer, schräg in der Kuppel
    m.bone("spear", "shell", pivot=(4.2, -9.5, -1.2), rotation=(-18, 30, -32))
    m.cube("spear", (-0.55, -17.0, -0.55), (1.1, 21.0, 1.1), "wood")
    for y in (-6.0, -11.0, -15.6):
        m.cube("spear", (-0.8, y, -0.8), (1.6, 1.0, 1.6), "gold")
    m.cube("spear", (-2.4, -18.6, -0.9), (4.8, 1.6, 1.8), "gold")                  # Parierstück
    m.cube("spear", (-0.8, -19.0, -1.15), (1.6, 1.6, 0.6), "pearl")                # Perle
    m.cube("spear", (-1.7, -25.6, -0.45), (3.4, 7.0, 0.9), "blade")                # Klinge
    m.cube("spear", (-1.0, -28.6, -0.35), (2.0, 3.0, 0.7), "blade")                # Spitze
    m.cube("spear", (-0.4, -30.0, -0.25), (0.8, 1.4, 0.5), "blade")
    for sx in (-1, 1):
        m.cube("spear", (2.4 * sx - (1.2 if sx > 0 else 0.0), -21.4, -0.35), (1.2, 3.0, 0.7), "blade")   # Widerhaken
    return m


# =============================================================================================
# Bemalung
# =============================================================================================

def _barnacle_dot(ctx: Ctx, density: float, seed: int) -> bool:
    x, y, z = ctx.world
    return hash01(int(x * 2), int(y * 2), int(z * 2), seed) < density


def _shell(ctx: Ctx):
    x, y, z = ctx.world
    v = 0.5 + (noise3(x * 0.22, y * 0.22, z * 0.22, 3) - 0.5) * 0.35 + (0.14 if ctx.face == "top" else -0.06)
    col = pick(ctx.pal["shell"], v)
    if ctx.face == "bottom":
        return pick(ctx.pal["belly"], 0.45)
    if noise3(x * 0.35, y * 0.35, z * 0.35, 4) > 0.7:
        col = pick(ctx.pal["spot"], 0.5)
    if ctx.face == "top" and noise3(x * 0.15, 0.0, z * 0.15, 9) > 0.66:
        col = mix(col, pick(ctx.pal["algae"], 0.5 + hash01(ctx.u, ctx.v) * 0.4), 0.75)
    if _barnacle_dot(ctx, 0.035, 11):
        col = pick(ctx.pal["barnacle"], 0.75)
    edge = min(ctx.s, ctx.w - 1 - ctx.s, ctx.t, ctx.h - 1 - ctx.t)
    if edge == 0 and ctx.face == "top":
        col = mix(col, pick(ctx.pal["shell"], 0.95), 0.4)
    return col


def _spike(ctx: Ctx):
    t = (ctx.t + 0.5) / max(ctx.h, 1)
    return pick(ctx.pal["shell"], 0.85 - t * 0.4) if ctx.face != "top" else pick(ctx.pal["shell"], 0.98)


def _belly(ctx: Ctx):
    x, y, z = ctx.world
    col = pick(ctx.pal["belly"], 0.5 + (noise3(x * 0.3, 1.0, z * 0.3, 5) - 0.5) * 0.3)
    if int(z) % 3 == 0:
        col = pick(ctx.pal["belly"], 0.2)                                           # Plattenfugen
    return col


def _mouth(ctx: Ctx):
    col = pick(ctx.pal["mouth"], 0.5 + (hash01(ctx.u, ctx.v, 3) - 0.5) * 0.4)
    if ctx.t % 3 == 0:
        col = pick(ctx.pal["shell"], 0.3)
    return col


def _stalk(ctx: Ctx):
    return pick(ctx.pal["leg"], 0.55 + (0.2 if ctx.face == "top" else 0.0))


def _eye(ctx: Ctx):
    # türkis glimmendes Facettenauge mit senkrechtem, dunklem Schlitz
    if ctx.face in ("bottom",):
        return pick(ctx.pal["leg"], 0.3)
    cx = (ctx.w - 1) / 2
    slit = abs(ctx.s - cx) < 0.6 and ctx.face in ("front", "west", "east", "back")
    if slit:
        return (8, 20, 20, 255)
    v = 0.38 + 0.3 * (1.0 - ctx.t / max(ctx.h, 1)) + (hash01(ctx.u, ctx.v, 5) - 0.5) * 0.15
    col = pick(ctx.pal["eye"], v)
    ctx.glow = col
    return col


def _antenna(ctx: Ctx):
    x, y, z = ctx.world
    band = int(abs(z) * 0.8) % 2 == 0
    return pick(ctx.pal["leg"], 0.75 if band else 0.4)


def _claw(ctx: Ctx):
    x, y, z = ctx.world
    v = 0.55 + (noise3(x * 0.3, y * 0.3, z * 0.3, 7) - 0.5) * 0.35 + (0.15 if ctx.face == "top" else 0.0)
    col = pick(ctx.pal["shell"], v)
    if ctx.face == "bottom":
        return mix(col, pick(ctx.pal["belly"], 0.5), 0.6)
    if hash01(ctx.u, ctx.v, 8) < 0.07:
        col = pick(ctx.pal["shell"], v + 0.25)                                       # Höcker
    if _barnacle_dot(ctx, 0.05, 13) and ctx.face == "top":
        col = pick(ctx.pal["barnacle"], 0.7)
    if ctx.face == "top" and noise3(x * 0.25, 1.0, z * 0.25, 21) > 0.68:
        col = mix(col, pick(ctx.pal["algae"], 0.6), 0.7)
    return col


def _claw_arm(ctx: Ctx):
    col = _claw(ctx)
    if ctx.face not in ("west", "east") and (ctx.s == 0 or ctx.s == ctx.w - 1):
        col = mix(col, pick(ctx.pal["belly"], 0.6), 0.5)                              # helle Gelenke
    return col


def _clawtip(ctx: Ctx):
    col = _claw(ctx)
    if ctx.face == "front":
        return pick(ctx.pal["tip"], 0.4)
    s = (ctx.s + 0.5) / max(ctx.w, 1)
    t = (ctx.t + 0.5) / max(ctx.h, 1)
    along = s if ctx.face in ("top", "bottom") else t
    return mix(col, pick(ctx.pal["tip"], 0.45), min(1.0, max(0.0, 1.0 - along * 1.4)))


def _cutter(ctx: Ctx):
    col = _clawtip(ctx)
    # die gezahnte Schneide: helle Zähnchen an der Innenkante
    if ctx.face in ("west", "east") and ctx.t == ctx.h - 1 and ctx.s % 2 == 0:
        return pick(ctx.pal["barnacle"], 0.85)
    return col


def _leg(ctx: Ctx):
    x, y, z = ctx.world
    v = 0.5 + (noise3(x * 0.4, y * 0.4, z * 0.4, 6) - 0.5) * 0.3 + (0.15 if ctx.face == "top" else 0.0)
    col = pick(ctx.pal["leg"], v)
    if ctx.face == "bottom":
        col = mix(col, pick(ctx.pal["belly"], 0.5), 0.5)
    if ctx.face not in ("west", "east") and (ctx.s == 0 or ctx.s == ctx.w - 1):
        col = mix(col, pick(ctx.pal["belly"], 0.6), 0.5)
    # Bänder quer über das Glied
    if ctx.face in ("top", "front", "back") and (ctx.s // 3) % 3 == 2:
        col = mix(col, pick(ctx.pal["shell"], 0.25), 0.4)
    return col


def _legtip(ctx: Ctx):
    col = _leg(ctx)
    s = (ctx.s + 0.5) / max(ctx.w, 1)
    return mix(col, pick(ctx.pal["tip"], 0.5), max(0.0, s - 0.45) * 1.8) if ctx.face in ("top", "bottom", "front", "back") else col


def _dome(ctx: Ctx):
    x, y, z = ctx.world
    sx, sy, sz = x - SHELL_ABS[0], y - SHELL_ABS[1], z - SHELL_ABS[2]
    if ctx.face == "bottom":
        return pick(ctx.pal["hole"], 0.5)
    v = 0.55 + (noise3(x * 0.18, y * 0.18, z * 0.18, 31) - 0.5) * 0.3 + (0.12 if ctx.face == "top" else 0.0)
    col = pick(ctx.pal["dome"], v)
    # Adern im Marmor
    vein = abs(noise3(x * 0.25, y * 0.25, z * 0.25, 33) - 0.5)
    if vein < 0.03:
        col = pick(ctx.pal["vein"], 0.5)
    # die Bresche: vorn rechts oben ist die Kuppel eingebrochen, dahinter Dunkel
    bx, by, bz = sx - 5.0, sy + 8.0, sz + 5.5
    if bx * bx * 0.7 + by * by * 1.1 + bz * bz * 0.7 < 16.0 + noise3(x, y, z, 35) * 8.0:
        return pick(ctx.pal["hole"], 0.3 + hash01(ctx.u, ctx.v, 2) * 0.4)
    # Bewuchs: Algen und Seepocken, oben mehr
    if ctx.face == "top" and noise3(x * 0.2, y * 0.2, z * 0.2, 37) > 0.6:
        col = mix(col, pick(ctx.pal["algae"], 0.3 + hash01(ctx.u, ctx.v, 4) * 0.6), 0.8)
    elif noise3(x * 0.3, y * 0.3, z * 0.3, 39) > 0.72:
        col = mix(col, pick(ctx.pal["algae"], 0.5), 0.55)
    if _barnacle_dot(ctx, 0.03, 41):
        col = pick(ctx.pal["barnacle"], 0.7 + hash01(ctx.u, ctx.v, 6) * 0.3)
    # ein Fries aus Wellenranken knapp über dem Goldsaum
    if ctx.face in ("west", "east", "front", "back") and -2.4 < sy < -0.8:
        wave = math.sin((sx + sz) * 0.9) > 0.3
        col = pick(ctx.pal["gold"], 0.55) if wave else mix(col, pick(ctx.pal["gold"], 0.3), 0.4)
    return col


def _pilaster(ctx: Ctx):
    x, y, z = ctx.world
    t = (ctx.t + 0.5) / max(ctx.h, 1)
    col = pick(ctx.pal["dome"], 0.7 - t * 0.2)
    if ctx.t <= 1 or ctx.t >= ctx.h - 2:
        col = pick(ctx.pal["gold"], 0.6)                                             # Kapitell und Fuß
    elif ctx.s % 2 == 0 and ctx.face not in ("top", "bottom"):
        col = pick(ctx.pal["dome"], 0.45)                                            # Kanneluren
    return col


def _gold(ctx: Ctx):
    v = 0.55 + (0.25 if ctx.face == "top" else 0.0) + (hash01(ctx.u, ctx.v, 7) - 0.5) * 0.2
    col = pick(ctx.pal["gold"], v)
    if hash01(ctx.u, ctx.v, 9) < 0.08:
        col = mix(col, pick(ctx.pal["algae"], 0.5), 0.6)                               # Grünspan
    return col


def _coral(key: str):
    def paint(ctx: Ctx):
        t = (ctx.t + 0.5) / max(ctx.h, 1)
        v = 0.4 + (0.45 if ctx.face == "top" else 0.3 * (1.0 - t)) + (hash01(ctx.u, ctx.v, 12) - 0.5) * 0.25
        col = pick(ctx.pal[key], v)
        if key == "coral_pink" and ctx.face == "top" and (ctx.s + ctx.t) % 3 == 0:
            col = pick(ctx.pal[key], 0.25)                                           # Hirnkoralle: Windungen
        return col
    return paint


def _anemone(ctx: Ctx):
    if ctx.face == "top":
        col = pick(ctx.pal["anemone"], 0.95 if (ctx.s + ctx.t) % 2 == 0 else 0.7)
        if (ctx.s + ctx.t) % 2 == 0:
            ctx.glow = col
        return col
    col = pick(ctx.pal["anemone"], 0.55 if ctx.t < 1 else 0.3)
    return col


def _barnacle(ctx: Ctx):
    if ctx.face == "top":
        cx, cy = (ctx.w - 1) / 2, (ctx.h - 1) / 2
        if abs(ctx.s - cx) < 0.6 and abs(ctx.t - cy) < 0.6:
            return pick(ctx.pal["barnacle"], 0.1)
        return pick(ctx.pal["barnacle"], 0.9)
    return pick(ctx.pal["barnacle"], 0.5 + (hash01(ctx.u, ctx.v) - 0.5) * 0.3)


def _kelp(ctx: Ctx):
    t = (ctx.t + 0.5) / max(ctx.h, 1)
    mid = abs(ctx.s - (ctx.w - 1) / 2) < 0.6
    col = pick(ctx.pal["kelp"], 0.75 - t * 0.35 if not mid else 0.9)
    if ctx.face not in ("front", "back"):
        col = pick(ctx.pal["kelp"], 0.35)
    if (ctx.t + ctx.s) % 5 == 0:
        col = pick(ctx.pal["kelp"], 0.5)
    return col


def _wood(ctx: Ctx):
    # Treibholz, gebleicht, mit feiner Maserung längs
    col = pick(ctx.pal["wood"], 0.5 + (0.2 if ctx.face in ("west", "front") else 0.0) + (hash01(ctx.s, 3) - 0.5) * 0.3)
    if ctx.t % 4 == 0 and hash01(ctx.u, ctx.v, 5) < 0.5:
        col = pick(ctx.pal["wood"], 0.2)
    return col


def _blade(ctx: Ctx):
    # Aquamarin: innen hell (Leuchtader), außen tief türkis
    cx = (ctx.w - 1) / 2
    core = abs(ctx.s - cx) < 0.8 and ctx.face in ("front", "back")
    v = 0.95 if core else 0.55 + (0.2 if ctx.face in ("west", "front") else 0.0) - abs(ctx.s - cx) / max(ctx.w, 1) * 0.4
    col = pick(ctx.pal["blade"], v)
    ctx.glow = col if core else mix(col, (0, 0, 0, 255), 0.45)
    return col


def _pearl(ctx: Ctx):
    col = pick(ctx.pal["pearl"], 0.9 if (ctx.s + ctx.t) % 3 else 0.5)
    ctx.glow = mix(col, (0, 0, 0, 255), 0.3)
    return col


def materials(variant: str) -> dict:
    return {"shell": _shell, "spike": _spike, "belly": _belly, "mouth": _mouth, "stalk": _stalk, "eye": _eye, "antenna": _antenna,
            "claw": _claw, "claw_arm": _claw_arm, "clawtip": _clawtip, "cutter": _cutter, "leg": _leg, "legtip": _legtip,
            "dome": _dome, "pilaster": _pilaster, "gold": _gold, "coral_red": _coral("coral_red"),
            "coral_pink": _coral("coral_pink"), "coral_yellow": _coral("coral_yellow"), "anemone": _anemone,
            "barnacle": _barnacle, "kelp": _kelp, "wood": _wood, "blade": _blade, "pearl": _pearl}
