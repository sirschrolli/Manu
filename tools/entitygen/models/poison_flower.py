"""
Giftblume – Zauber des Naturelements: eine riesige giftige Blüte wächst aus dem Boden und öffnet sich (ca. 31 hoch
geöffnet, geschlossen bis ca. 39). Boden-Regel: y = 24 ist der Boden, oben ist -y.

Knochen:
  * stem             Drehpunkt (0, 24, 0) auf dem Boden: dicker, leicht geschwungener Stiel (drei Stücke, 4 dick,
                     24 hoch) mit Kelchblättern unter der Kapsel
  * bulb             Kind von stem, Drehpunkt oben auf dem Stiel (0.8, -24, -0.4): runde Samenkapsel (8 × 6 × 8), oben
                     die leuchtende Narbe mit sechs Staubblättern
  * petal0 … petal5  Kinder von bulb, Drehpunkt am oberen Rand der Kapsel, Drehung (0, i · 60°, 0). Jedes Blütenblatt
                     liegt in seinem Knochen gleich: es zeigt nach lokal -Z nach außen (ca. 10 lang, 1 dick, 6,6 breit),
                     die Oberseite nach oben, die Spitze leicht aufgebogen. Grundhaltung = ganz geöffnet (waagrecht);
                     xRot < 0 hebt das Blatt (−π/2 = senkrecht nach oben), xRot > 0 senkt es.
  * leaf0 … leaf3    Kinder von stem, Drehpunkt am Stiel knapp über dem Boden, Drehung (0, 45° + i · 90°, 0): breite
                     Blätter nach lokal -Z nach außen, zur Spitze hin hängend (gleiche Regel wie die Blütenblätter)

Bemalung: Blütenblätter tief violett bis magenta mit hellem Rand und giftig gelbgrünen Flecken (leuchten), Narbe
und Staubbeutel leuchten säuregrün, Kapsel dunkel violett-grün mit Rippen, Stiel grün mit dunklen Rippen.
"""
from __future__ import annotations

import math

from ..geometry import Model
from ..paint import Ctx, hash01, hexc, mix, noise3, pick

PALETTES = {
    "poison_flower": {
        "petal": [hexc(c) for c in ("#1c0624", "#30093a", "#470e54", "#5f136c", "#7a1c86", "#94299c", "#ae3eae",
                                    "#c65cc0", "#dc86d2", "#eeb4e4")],
        "toxic": [hexc(c) for c in ("#304a06", "#4e780a", "#74a812", "#9cd422", "#c6f046", "#eaff96")],
        "stem": [hexc(c) for c in ("#0c1c0e", "#132a15", "#1b3c1c", "#245024", "#2e662c", "#3a7e36", "#4a9842",
                                   "#5eb052")],
        "pod": [hexc(c) for c in ("#120c18", "#1e1426", "#2a1e34", "#382842", "#463450", "#56425e", "#685470")],
    },
}

STEM_TOP = (0.8, -24.0, -0.4)
POD_RIM_Y = -4.4
POD_RIM_R = 3.0


def build() -> Model:
    m = Model("poison_flower", 128, 80)
    m.tex_res = 2

    # ---------------------------------------------------------------- Stiel (leicht geschwungen)
    m.bone("stem", pivot=(0, 24, 0))
    m.cube("stem", (-2.1, -9.0, -2.1), (4.2, 10.2, 4.2), "stem")
    m.cube("stem", (-1.75, -17.2, -2.0), (3.9, 8.8, 3.9), "stem")
    m.cube("stem", (-1.1, -24.4, -2.25), (3.8, 7.8, 3.7), "stem")
    m.cube("stem", (-2.6, -1.4, -2.6), (5.2, 2.6, 5.2), "stem_foot")             # verdickter Fuß
    for k, (x, z, w, d) in enumerate(((-1.5, -3.3, 4.6, 1.2), (-1.5, 2.1, 4.6, 1.2), (-2.7, -1.6, 1.2, 4.4),
                                      (2.7, -1.6, 1.2, 4.4))):
        m.cube("stem", (x, -24.2, z), (w, 2.4, d), "sepal")                      # Kelchblätter
    # Blätter am Fuß
    for i in range(4):
        yaw = 45 + i * 90
        a = math.radians(yaw)
        dx, dz = -math.sin(a), -math.cos(a)
        name = f"leaf{i}"
        m.bone(name, "stem", pivot=(round(dx * 1.6, 3), -2.4 - 0.5 * (i % 2), round(dz * 1.6, 3)), rotation=(0, yaw, 0))
        m.cube(name, (-2.3, -0.9, -5.4), (4.6, 0.8, 5.8), "leaf")                # innen, waagrecht
        m.cube(name, (-2.9, -0.3, -10.6), (5.8, 0.8, 5.4), "leaf")               # außen, hängt etwas

    # ---------------------------------------------------------------- Samenkapsel
    m.bone("bulb", "stem", pivot=STEM_TOP)
    m.cube("bulb", (-2.6, -1.3, -2.6), (5.2, 2.0, 5.2), "pod")                   # Hals
    m.cube("bulb", (-4.0, -4.9, -3.0), (8.0, 3.8, 6.0), "pod")
    m.cube("bulb", (-3.0, -5.1, -4.0), (6.0, 4.1, 8.0), "pod")
    m.cube("bulb", (-3.1, -6.0, -3.1), (6.2, 1.2, 6.2), "pod_top")
    m.cube("bulb", (-1.6, -6.7, -1.6), (3.2, 0.9, 3.2), "stigma")                # leuchtende Narbe
    for k in range(6):
        a = math.radians(30 + k * 60)
        sx, sz = math.cos(a) * 2.2, math.sin(a) * 2.2
        m.cube("bulb", (sx - 0.25, -8.6, sz - 0.25), (0.5, 2.8, 0.5), "stamen")   # Staubfaden
        m.cube("bulb", (sx - 0.45, -9.4, sz - 0.45), (0.9, 0.9, 0.9), "anther")   # Staubbeutel

    # ---------------------------------------------------------------- Blütenblätter
    for i in range(6):
        yaw = i * 60
        a = math.radians(yaw)
        dx, dz = -math.sin(a), -math.cos(a)                                      # lokal -Z in der Kapsel
        name = f"petal{i}"
        m.bone(name, "bulb", pivot=(round(dx * POD_RIM_R, 3), POD_RIM_Y, round(dz * POD_RIM_R, 3)),
               rotation=(0, yaw, 0))
        m.cube(name, (-1.7, -0.5, -3.4), (3.4, 1.0, 4.0), "petal")               # Ansatz
        m.cube(name, (-3.3, -0.5, -8.0), (6.6, 1.0, 4.9), "petal")               # Mitte (am breitesten)
        m.cube(name, (-2.3, -1.1, -10.4), (4.6, 1.0, 2.7), "petal")              # Spitze, aufgebogen
    return m


# =============================================================================================
# Bemalen
# =============================================================================================

def _petal_shape(z: float) -> float:
    """Halbe Breite des Blütenblatts in der Tiefe -z (0 am Ansatz … 10.4 an der Spitze)."""
    t = max(0.0, min(1.0, -z / 10.4))
    if t < 0.5:
        return 1.2 + 2.1 * math.sin(t / 0.5 * math.pi / 2) ** 0.8
    k = (t - 0.5) / 0.5
    return 3.3 * math.sqrt(max(0.0, 1.0 - k ** 1.8)) * (1.0 - 0.2 * k)


def _spots(x: float, z: float, seed: int) -> float:
    """Giftflecken: Abstand (0 = Mitte eines Flecks, ab 1 = kein Fleck)."""
    best = 9.0
    for (sx, sz, r) in ((-1.2, -5.0, 0.75), (1.4, -6.4, 0.6), (-0.3, -8.0, 0.55), (1.0, -3.8, 0.45),
                        (-1.9, -7.0, 0.42), (0.4, -9.2, 0.35)):
        jx = (hash01(seed, int(sx * 10), 401) - 0.5) * 0.8
        jz = (hash01(seed, int(sz * 10), 403) - 0.5) * 0.8
        best = min(best, math.hypot(x - sx - jx, z - sz - jz) / r)
    return best


def _petal(ctx: Ctx):
    """Blütenblatt: Umriss als spitzes Oval (Rest durchsichtig), oben tiefviolett am Ansatz über Magenta zum hellen
    Rand, dunkle Mittelader und feine Längsadern, gelbgrün leuchtende Giftflecken; unten dunkler."""
    x, y, z = ctx.local
    face = ctx.lface
    half = _petal_shape(z)
    if face in ("front", "back"):
        # Stirnseiten nur, wo der Umriss die Kante berührt
        if abs(x) > half:
            return None
        return pick(ctx.pal["petal"], 0.5)
    if abs(x) > half + 0.05:
        return None
    if face in ("west", "east"):
        if half < abs(x) - 0.05 or half < ctx.cube.size[0] / 2 - 0.3:
            return None                                                   # Seite nur, wo das Blatt so breit ist
        return pick(ctx.pal["petal"], 0.72)
    t = max(0.0, min(1.0, -z / 10.4))
    s = abs(x) / max(half, 1e-3)
    seed = int(ctx.cube.bone.name[-1])
    if face == "bottom":
        # Unterseite (außen an der geschlossenen Knospe): dunkleres Violett, helle Längsadern, heller Saum
        v = 0.34 + 0.22 * t - 0.06 * s
        if s > 0.84:
            v = 0.62
        elif abs(x) < 0.3:
            v += 0.1
        elif abs(((x * 1.6 + abs(z) * 0.25) % 1.4) - 0.7) < 0.1 and 0.15 < t < 0.8:
            v += 0.06
        return pick(ctx.pal["petal"], v)
    # Oberseite
    v = 0.18 + 0.48 * t ** 0.8 + 0.06 * (1 - s)
    if s > 0.82:
        v = 0.86 + 0.08 * t                                               # heller Saum
    elif abs(x) < 0.28 and t < 0.85:
        v = 0.14 + 0.2 * t                                                # Mittelader
    elif abs(((x * 1.6 + abs(z) * 0.25) % 1.4) - 0.7) < 0.1 and 0.15 < t < 0.8:
        v -= 0.08                                                         # feine Längsadern
    d = _spots(x, z, seed)
    if d < 1.0 and s < 0.85:
        g = pick(ctx.pal["toxic"], 0.55 + 0.4 * (1 - d))
        ctx.glow = pick(ctx.pal["toxic"], 0.4 + 0.4 * (1 - d))
        return g
    if d < 1.35 and s < 0.85:
        v -= 0.12                                                         # dunkler Hof um den Fleck
    return pick(ctx.pal["petal"], v)


def _pod(ctx: Ctx):
    """Samenkapsel: dunkel violett-grün, Rippen von unten nach oben, oben heller, kleine leuchtende Poren."""
    x, y, z = ctx.local
    face = ctx.lface
    ang = math.atan2(z, x)
    rib = abs(((ang / (math.pi / 4)) % 1.0) - 0.5) * 2                  # 0 Rippe … 1 Furche
    v = 0.44 + 0.16 * (1 - rib) + 0.12 * max(0.0, (-y - 1.0) / 5.0)
    if face == "top":
        v += 0.12
    elif face == "bottom":
        v -= 0.18
    if face not in ("top", "bottom") and ctx.t == ctx.h - 1:
        v -= 0.08
    col = pick(ctx.pal["pod"], v)
    if rib > 0.82 and face != "bottom":
        col = mix(col, pick(ctx.pal["stem"], 0.35), 0.5)                 # grüner Schimmer in den Furchen
    if face != "bottom" and hash01(round(x * 2), round(y * 2), round(z * 2), 405) > 0.95:
        col = pick(ctx.pal["toxic"], 0.7)
        ctx.glow = pick(ctx.pal["toxic"], 0.45)
    return col


def _pod_top(ctx: Ctx):
    """Oberseite der Kapsel: ringförmig zur Narbe hin heller, der innere Ring glimmt."""
    x, y, z = ctx.local
    r = math.hypot(x, z)
    if ctx.lface == "top":
        if r < 2.4:
            col = pick(ctx.pal["toxic"], 0.45 + 0.2 * (2.4 - r))
            ctx.glow = pick(ctx.pal["toxic"], 0.3 + 0.15 * (2.4 - r))
            return col
        return pick(ctx.pal["pod"], 0.62 - 0.1 * (r - 2.4))
    return _pod(ctx)


def _stigma(ctx: Ctx):
    """Narbe: leuchtet säuregrün, in der Mitte fast weiß."""
    x, y, z = ctx.local
    r = math.hypot(x, z) / 1.6
    v = 0.95 - 0.35 * r if ctx.lface == "top" else 0.62
    col = pick(ctx.pal["toxic"], v)
    ctx.glow = col
    return col


def _stamen(ctx: Ctx):
    return pick(ctx.pal["stem"], 0.55 if ctx.lface != "bottom" else 0.3)


def _anther(ctx: Ctx):
    col = pick(ctx.pal["toxic"], 0.85 if ctx.lface == "top" else 0.66)
    ctx.glow = col
    return col


def _stem(ctx: Ctx):
    """Stiel: grün mit dunklen Rippen (senkrechte Linien), zum Fuß dunkler, Licht von links oben."""
    x, y, z = ctx.local
    face = ctx.lface
    if face in ("top", "bottom"):
        return pick(ctx.pal["stem"], 0.5 if face == "top" else 0.2)
    u = (ctx.s + 0.5) / ctx.w
    v = 0.5 + {"west": 0.06, "front": 0.03, "east": -0.04, "back": -0.06}[face]
    v -= 0.22 * max(0.0, min(1.0, (y + 6.0) / 7.0))                      # Fuß dunkler
    v += (noise3(x * 0.3, y * 0.2, z * 0.3, 407) - 0.5) * 0.1
    if any(abs(u - c) < 0.09 for c in (0.25, 0.75)):
        v -= 0.2                                                          # Rippen
    elif any(abs(u - c) < 0.09 for c in (0.12, 0.62)):
        v += 0.08                                                         # Licht auf den Rippen
    return pick(ctx.pal["stem"], v)


def _stem_foot(ctx: Ctx):
    x, y, z = ctx.local
    v = 0.24 + (0.1 if ctx.lface == "top" else 0.0) + (noise3(x, y, z, 409) - 0.5) * 0.1
    col = pick(ctx.pal["stem"], v)
    return mix(col, pick(ctx.pal["pod"], 0.3), 0.35)


def _sepal(ctx: Ctx):
    """Kelchblätter unter der Kapsel: grün mit violetter Spitze oben."""
    if ctx.lface == "top":
        return pick(ctx.pal["petal"], 0.3)
    if ctx.t == 0:
        return pick(ctx.pal["petal"], 0.26)
    return pick(ctx.pal["stem"], 0.5 - 0.1 * (ctx.t / max(1, ctx.h - 1)))


def _leaf(ctx: Ctx):
    """Breites Blatt (nach lokal -Z): Umriss gemalt, Mittelrippe, schräge Seitenadern, dunkler Rand; ein paar
    giftige Flecken (leuchten schwach)."""
    x, y, z = ctx.local
    face = ctx.lface
    t = max(0.0, min(1.0, -z / 10.6))
    if t < 0.08:
        half = 1.0
    else:
        k = (t - 0.08) / 0.92
        half = 2.9 * math.sin(math.pi * min(1.0, k * 1.02)) ** 0.7 * (1.0 - 0.15 * k)
    if face in ("front", "back"):
        return None if abs(x) > half else pick(ctx.pal["stem"], 0.3)
    if abs(x) > half + 0.05:
        return None
    if face in ("west", "east"):
        if half < ctx.cube.size[0] / 2 - 0.3:
            return None
        return pick(ctx.pal["stem"], 0.3)
    s = abs(x) / max(half, 1e-3)
    if face == "bottom":
        return pick(ctx.pal["stem"], 0.26 + 0.08 * (1 - s))
    v = 0.46 + 0.14 * (1 - s) - 0.06 * t
    if abs(x) < 0.3:
        v = 0.66                                                          # Mittelrippe
    elif abs(((-z * 0.9 - abs(x) * 0.8) % 2.0) - 1.0) < 0.14 and s < 0.9:
        v -= 0.1                                                          # Seitenadern
    if s > 0.86:
        v -= 0.14                                                         # Rand
    seed = int(ctx.cube.bone.name[-1])
    d = _spots(x * 0.9, z, seed + 10)
    if d < 0.7 and s < 0.8 and t > 0.3:
        col = pick(ctx.pal["toxic"], 0.5)
        ctx.glow = pick(ctx.pal["toxic"], 0.25)
        return col
    return pick(ctx.pal["stem"], v)


def materials(variant: str) -> dict:
    return {"petal": _petal, "pod": _pod, "pod_top": _pod_top, "stigma": _stigma, "stamen": _stamen,
            "anther": _anther, "stem": _stem, "stem_foot": _stem_foot, "sepal": _sepal, "leaf": _leaf}
