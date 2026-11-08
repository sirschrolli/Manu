"""
Splitterwächter: ein uralter Wächter der Alten, gehauen aus Sternenmarmor, der seit dem Zerbrechen der Welten über
ihren Ruinen wacht.

  * ein wuchtiger Leib aus bleichem Sternenmarmor mit violetten Adern und goldenen Einlegearbeiten an den Kanten;
    durch Risse sickert das violette Licht der Leere
  * die Brust ist offen: darin schwebt und kreist sein Herz, ein geschliffener Leerenkristall
  * ein glatter, gesichtsloser Helm mit einem einzigen leuchtenden Sehschlitz, darauf eine Krone aus kleinen Kristallen
  * mächtige Schulterstücke, aus denen Leerenkristalle wachsen; auf dem Rücken ein ganzer Kristallkamm
  * lange Arme mit Kristallklingen an den Unterarmen und schweren Fäusten
  * um ihn kreisen drei Marmorbrocken, schwerelos wie die Trümmer der Ruinen

Knochen-Namen werden in ShardWardenModel (Java) für die Animationen verwendet.
"""
from __future__ import annotations

import math

from ..geometry import Model
from ..paint import Ctx, hash01, hexc, mix, noise3, pick

PALETTES = {
    "shard_warden": {
        "marble": [hexc(c) for c in ("#8a80a8", "#a49cc0", "#bcb4d4", "#cfc8e2", "#ddd6ec", "#e9e4f4", "#f6f3fb")],
        "vein": [hexc(c) for c in ("#6a5aa0", "#8a78c4", "#a898dc")],
        "gold": [hexc(c) for c in ("#7a5420", "#a8742a", "#d6a23c", "#f2cf62", "#fff0a8")],
        "dark": [hexc(c) for c in ("#0e0b1e", "#161230", "#1f1a3e", "#2a244e", "#36305e")],
        "crystal": [hexc(c) for c in ("#2e1660", "#4c2a96", "#7a44d0", "#a070ec", "#c8a8ff", "#e6d8ff", "#ffffff")],
        "light": [hexc(c) for c in ("#5a2ab0", "#8a4ae8", "#b88aff", "#e4d0ff", "#ffffff")],
        "visor": [hexc(c) for c in ("#2a7ab8", "#46b6e6", "#9ae6ff", "#e8fbff")],
    },
}

# Schulterkristalle je Seite: (x, y, z, Neigung um X, Neigung um Z, Länge, Breite) – x zeigt nach außen
SHOULDER_SHARDS = [(1.0, -3.0, -1.5, -14, 26, 8.0, 2.2), (2.5, -2.5, 2.0, 18, 40, 6.0, 1.8), (-0.5, -3.0, 2.5, 30, 14, 4.5, 1.4)]
# Rückenkamm: (x, y, z, Neigung um X, Neigung um Z, Länge, Breite)
BACK_SHARDS = [(-4.0, -20.5, 5.0, 32, -12, 10.0, 2.6), (1.5, -21.0, 5.5, 26, 8, 13.0, 3.0), (5.5, -18.0, 5.0, 38, 20, 8.0, 2.2),
               (-1.0, -15.0, 5.5, 52, -4, 7.0, 2.0)]
# Krone: (x, z, Neigung um X, Neigung um Z, Länge)
CROWN = [(0.0, 0.5, -6, 0, 4.0), (-2.6, 0.0, 0, -22, 3.0), (2.6, 0.0, 0, 22, 3.0)]


def build() -> Model:
    m = Model("shard_warden", 256, 128)
    m.tex_res = 2
    # --- Beine (Hüfte auf Höhe 6, Füße auf 24) ---
    for side, sx in (("right", -1), ("left", 1)):
        leg = f"{side}_leg"
        m.bone(leg, pivot=(4.6 * sx, 6, 0.5))
        m.cube(leg, (-3, 0, -3), (6, 9, 6), "marble")
        m.cube(leg, (-3.3, 8.5, -3.3), (6.6, 6.5, 6.6), "marble")
        m.cube(leg, (-2.6, 7.2, -4.1), (5.2, 3.4, 1.2), "trim")                 # Kniescheibe
        m.cube(leg, (-3.6, 15, -4.8), (7.2, 3, 8.6), "marble")                  # Fuß

    # --- Rumpf (Drehpunkt in der Hüfte) ---
    m.bone("body", pivot=(0, 6, 0))
    m.cube("body", (-6.5, -6.5, -4), (13, 7, 8), "dark")                         # Mitte
    m.cube("body", (-7.5, -2.5, -4.5), (15, 3, 9), "trim")                       # Gürtel
    # Brust: offen nach vorn, darin das Herz
    m.cube("body", (-10, -21, 2), (20, 15, 4), "marble")                         # Rücken
    m.cube("body", (-10, -21, -6), (5.6, 15, 8), "marble")                       # rechte Brust
    m.cube("body", (4.4, -21, -6), (5.6, 15, 8), "marble")                       # linke Brust
    m.cube("body", (-4.4, -21, -6), (8.8, 3.6, 8), "marble")                     # Kragen
    m.cube("body", (-4.4, -9.2, -5.6), (8.8, 3.2, 7.6), "marble")                # Brustbein
    m.cube("body", (-4.4, -17.4, 1.2), (8.8, 8.2, 0.8), "hollow")               # Rückwand der Höhlung
    m.cube("body", (-11, -22, -6.6), (22, 1.6, 9.4), "trim")                     # oberer Rand
    for k, (x0, w) in enumerate(((-5.2, 0.8), (4.4, 0.8))):                       # leuchtende Runen an der Öffnung
        m.cube("body", (x0, -17.0, -6.3), (w, 7.4, 0.4), "rune")
    m.bone("core", "body", pivot=(0, -13.3, -1.8))
    m.cube("core", (-2.3, -2.3, -2.3), (4.6, 4.6, 4.6), "core")
    m.bone("core_inner", "core", rotation=(45, 0, 45))
    m.cube("core_inner", (-2.0, -2.0, -2.0), (4.0, 4.0, 4.0), "core")
    m.bone("back_crystals", "body")
    for k, (x, y, z, rx, rz, length, width) in enumerate(BACK_SHARDS):
        name = f"back_shard_{k}"
        m.bone(name, "back_crystals", pivot=(x, y, z), rotation=(rx, 0, rz))
        m.cube(name, (-width / 2, -length, -width / 2), (width, length + 1.5, width), "crystal")

    # --- Kopf ---
    m.bone("head", "body", pivot=(0, -22, -1.5))
    m.cube("head", (-4, -8, -4.5), (8, 8.5, 8.5), "helm")
    m.cube("head", (-4.5, -6.2, -5.0), (9, 1.6, 1.2), "trim")                   # Stirnband über dem Schlitz
    m.cube("head", (-3.2, -4.6, -4.75), (6.4, 1.2, 0.4), "visor")              # Sehschlitz
    for k, (x, z, rx, rz, length) in enumerate(CROWN):
        name = f"crown_{k}"
        m.bone(name, "head", pivot=(x, -8, z), rotation=(rx, 0, rz))
        m.cube(name, (-0.8, -length, -0.8), (1.6, length + 0.5, 1.6), "crystal")

    # --- Arme mit Schulterstücken, Unterarmen und Fäusten ---
    for side, sx in (("right", -1), ("left", 1)):
        arm = f"{side}_arm"
        m.bone(arm, "body", pivot=(12.0 * sx, -18.5, 0))
        m.cube(arm, (-4.5, -3.5, -5), (9, 6.5, 10), "marble")                   # Schulterstück
        m.cube(arm, (-4.9, 2.4, -5.4), (9.8, 1.4, 10.8), "trim")                # Saum des Schulterstücks
        m.cube(arm, (-2.8, 3, -2.8), (5.6, 8, 5.6), "dark")                     # Oberarm
        for k, (x, y, z, rx, rz, length, width) in enumerate(SHOULDER_SHARDS):
            name = f"{side}_shoulder_shard_{k}"
            m.bone(name, arm, pivot=(x * sx, y, z), rotation=(rx, 0, rz * sx))
            m.cube(name, (-width / 2, -length, -width / 2), (width, length + 1.5, width), "crystal")
        fore = f"{side}_forearm"
        m.bone(fore, arm, pivot=(0, 11, 0))
        m.cube(fore, (-3.6, 0, -3.6), (7.2, 9, 7.2), "marble")
        m.cube(fore, (-3.4, 8.6, -3.8), (6.8, 5.6, 7.6), "fist")
        m.cube(fore, (-3.9, 1.0, -3.9), (7.8, 1.4, 7.8), "trim")                # Armreif
        for k, kx in enumerate((-2.2, 0.0, 2.2)):
            knuckle = f"{side}_knuckle_{k}"
            m.bone(knuckle, fore, pivot=(kx, 10.2, -3.6), rotation=(-62, 0, (k - 1) * 14))
            m.cube(knuckle, (-0.6, -2.4 - (k % 2) * 0.8, -0.6), (1.2, 2.8 + (k % 2) * 0.8, 1.2), "crystal")
        blade = f"{side}_blade"
        m.bone(blade, fore, pivot=(3.6 * sx, 4.5, 0), rotation=(0, 0, -8 * sx))
        m.cube(blade, (-0.6 if sx > 0 else -0.6, -6, -1.6), (1.2, 12, 3.2), "crystal")

    # --- schwebende Marmorbrocken (kreisen um den Wächter) ---
    m.bone("orbit", pivot=(0, -6, 0))
    for k, (r, ang, y, size) in enumerate(((17.0, 30, -2.0, (3.4, 4.6, 3.0)), (16.0, 150, 4.0, (2.6, 3.2, 2.8)),
                                           (18.0, 260, -6.0, (2.2, 2.8, 2.4)))):
        a = math.radians(ang)
        name = f"fragment_{k}"
        m.bone(name, "orbit", pivot=(math.cos(a) * r, y, math.sin(a) * r), rotation=(18 * k, 25 * k, 12))
        m.cube(name, (-size[0] / 2, -size[1] / 2, -size[2] / 2), size, "fragment")
    return m


# =============================================================================================
# Bemalung
# =============================================================================================

def _veins(x, y, z) -> float:
    """Dünne, gewundene Adern im Marmor (0 = keine, 1 = Mitte einer Ader)."""
    w = noise3(x * 0.12, y * 0.12, z * 0.12, 5) * 7.0
    a = abs(math.sin(x * 0.35 + y * 0.22 - z * 0.3 + w))
    b = abs(math.sin(-x * 0.18 + y * 0.41 + z * 0.25 + w * 0.7 + 1.7))
    return max(0.0, 1.0 - min(a, b * 1.3) / 0.09)


def _crack(x, y, z) -> float:
    """Risse, durch die das Licht der Leere sickert."""
    n = noise3(x * 0.32, y * 0.32, z * 0.32, 9)
    return max(0.0, 1.0 - abs(n - 0.5) / 0.018) if noise3(x * 0.08, y * 0.08, z * 0.08, 10) > 0.62 else 0.0


def _edge(ctx: Ctx) -> int:
    return min(ctx.s, ctx.w - 1 - ctx.s, ctx.t, ctx.h - 1 - ctx.t)


def _marble(ctx: Ctx):
    x, y, z = ctx.world
    v = 0.6 + (noise3(x * 0.22, y * 0.22, z * 0.22, 3) - 0.5) * 0.3 + (noise3(x * 0.9, y * 0.9, z * 0.9, 4) - 0.5) * 0.12
    v += 0.1 if ctx.face == "top" else -0.12 if ctx.face == "bottom" else 0.0
    v -= max(0.0, y - 6.0) * 0.012                               # nach unten etwas verwittert
    col = pick(ctx.pal["marble"], v)
    vein = _veins(x, y, z)
    if vein > 0:
        col = mix(col, pick(ctx.pal["vein"], 0.4 + vein * 0.5), vein * 0.7)
    crack = _crack(x, y, z)
    cx, cy = x, y - (6 - 13.3)
    rc = math.hypot(cx, cy)
    if 4.0 < rc < 11.0 and ctx.face == "front" and z < -4.0:
        ang = math.atan2(cy, cx)
        ray = abs(math.sin(ang * 3.0 + noise3(x * 0.25, y * 0.25, 0, 19) * 2.2))
        if ray < 0.07 + 0.05 * (1.0 - rc / 11.0):
            crack = max(crack, 1.0 - rc / 13.0)
    if crack > 0.3:
        col = mix(hexc("#1a1236"), pick(ctx.pal["light"], 0.6), crack)
        ctx.glow = mix((0, 0, 0, 255), pick(ctx.pal["light"], 0.55), crack * 0.9)
    # Kanten: außen ein dunkler Saum, innen eine helle Fase (die Platten wirken gehauen, nicht gekachelt)
    e = _edge(ctx)
    if e == 0:
        col = mix(col, pick(ctx.pal["marble"], v - 0.28), 0.55)
    elif e == 1 and ctx.face != "bottom":
        col = mix(col, pick(ctx.pal["marble"], v + 0.2), 0.35)
    return col


def _trim(ctx: Ctx):
    x, y, z = ctx.world
    v = 0.52 + (noise3(x * 0.6, y * 0.6, z * 0.6, 14) - 0.5) * 0.16 + (0.15 if ctx.face == "top" else 0.0)
    col = pick(ctx.pal["gold"], v)
    # eingravierte Rauten in regelmäßigem Abstand
    if ctx.face in ("front", "back", "west", "east") and ctx.h >= 3:
        k = (ctx.s + abs(ctx.t - ctx.h // 2) * 2) % 10
        if k == 0 and 0 < ctx.t < ctx.h - 1:
            col = pick(ctx.pal["gold"], v - 0.25)
    if _edge(ctx) == 0:
        col = pick(ctx.pal["gold"], v - 0.15)
    elif ctx.t == 1:
        col = pick(ctx.pal["gold"], v + 0.25)
    return col


def _dark(ctx: Ctx):
    x, y, z = ctx.world
    v = 0.35 + (noise3(x * 0.5, y * 0.5, z * 0.5, 15) - 0.5) * 0.35
    col = pick(ctx.pal["dark"], v)
    # leuchtende Leitbahnen im Stein
    if abs(math.sin(y * 0.9 + noise3(x * 0.3, y * 0.3, z * 0.3, 16) * 3.0)) < 0.08:
        col = mix(col, pick(ctx.pal["light"], 0.4), 0.7)
        ctx.glow = mix((0, 0, 0, 255), pick(ctx.pal["light"], 0.35), 0.8)
    return col


def _rune(ctx: Ctx):
    k = ctx.t % 5
    on = k in (0, 1, 3) and not (k == 3 and ctx.s % 2 == 0)
    col = pick(ctx.pal["light"], 0.75) if on else pick(ctx.pal["gold"], 0.5)
    if on:
        ctx.glow = pick(ctx.pal["light"], 0.65)
    return col


def _hollow(ctx: Ctx):
    """Rückwand der Brusthöhle: vom Herz angestrahlt."""
    x, y, z = ctx.world
    d = math.hypot(x, y - (6 - 13.3))
    col = mix(pick(ctx.pal["dark"], 0.2), pick(ctx.pal["light"], 0.5), max(0.0, 1.0 - d / 5.0) * 0.8)
    ctx.glow = mix((0, 0, 0, 255), pick(ctx.pal["light"], 0.4), max(0.0, 1.0 - d / 5.5) * 0.9)
    return col


def _core(ctx: Ctx):
    e = _edge(ctx)
    s_ = (ctx.s + 0.5) / max(ctx.w, 1)
    t = (ctx.t + 0.5) / max(ctx.h, 1)
    v = 0.45 + (0.4 if e == 0 else 0.0) + max(0.0, 0.25 - math.hypot(s_ - 0.35, t - 0.35)) * 1.6 + hash01(ctx.u, ctx.v, 18) * 0.08
    col = pick(ctx.pal["crystal"], v)
    ctx.glow = col
    return col


def _crystal(ctx: Ctx):
    t = (ctx.t + 0.5) / max(ctx.h, 1)
    a = 1.0 - t if ctx.face not in ("top", "bottom") else (1.0 if ctx.face == "top" else 0.0)
    edge = ctx.s == 0 or ctx.s == ctx.w - 1
    v = 0.18 + a * 0.62 + (0.12 if edge else 0.0) + (0.06 if ctx.face in ("front", "west") else 0.0)
    col = pick(ctx.pal["crystal"], v)
    # Facetten: eine hellere Diagonale über jede Fläche
    if abs((ctx.s + 0.5) / max(ctx.w, 1) - t) < 0.08:
        col = mix(col, pick(ctx.pal["crystal"], v + 0.2), 0.5)
    if a > 0.5 or edge:
        ctx.glow = mix((0, 0, 0, 255), pick(ctx.pal["crystal"], min(0.95, v + 0.1)), 0.7)
    return col


def _helm(ctx: Ctx):
    col = _marble(ctx)
    if ctx.face == "front":
        # glatte Maske: leicht glänzend, ohne Adern um den Schlitz
        t = (ctx.t + 0.5) / max(ctx.h, 1)
        col = mix(col, pick(ctx.pal["marble"], 0.82 - abs(t - 0.5) * 0.4), 0.55)
    return col


def _visor(ctx: Ctx):
    s = (ctx.s + 0.5) / max(ctx.w, 1)
    col = pick(ctx.pal["visor"], 0.95 - abs(s - 0.5) * 0.9)
    ctx.glow = col
    return col


def _fist(ctx: Ctx):
    col = _marble(ctx)
    if ctx.face == "front" and ctx.t > 1 and ctx.t % 3 == 0:
        col = pick(ctx.pal["marble"], 0.2)                       # Fingerfugen
    if ctx.face == "bottom":
        col = mix(col, pick(ctx.pal["crystal"], 0.5), 0.4)
    return col


def _fragment(ctx: Ctx):
    col = _marble(ctx)
    if ctx.face == "bottom" or (ctx.face in ("west", "east") and ctx.t > ctx.h * 0.6):
        # Bruchkante: dunkler Stein der Leere
        col = mix(col, pick(ctx.pal["dark"], 0.5), 0.6)
    return col


def materials(variant: str) -> dict:
    return {"marble": _marble, "trim": _trim, "rune": _rune, "dark": _dark, "hollow": _hollow, "core": _core, "crystal": _crystal,
            "helm": _helm, "visor": _visor, "fist": _fist, "fragment": _fragment}
