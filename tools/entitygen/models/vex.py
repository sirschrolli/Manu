"""
Plagegeist (statt Vex) auf dem Skelett des Vanilla-Plagegeists: dieselben Teile und Drehpunkte wie VexModel (ein
Knochen "root" mit Kopf und Leib, am Leib Arme und Flügel), damit Flattern, Ausholen und das Schwert in der Hand passen.

Ein kleiner Geist in zerschlissener Kutte: Unter der Kapuze sitzt ein bleiches Schädelgesicht mit glühenden Augen, die
Kutte franst nach unten in Rauchfetzen aus (statt Beinen), die dünnen Arme stecken in zerrissenen Ärmeln und enden in
knöchernen Händen, die Flügel sind Fetzen aus Leichentuch und Rauch. Durch einen Riss in der Brust schimmert ein
Seelenlicht. Der Plagegeist wird durchscheinend und voll beleuchtet gezeichnet; die Fetzen sind halb durchsichtig.

  * plagegeist_violett: tiefes Violett, fliederfarbene Rauchfetzen, magentaweiße Augen
  * plagegeist_tuerkis: dunkles Seegrün, türkisfarbene Rauchfetzen, eisweiße Augen
"""
from __future__ import annotations

import math

from ..geometry import Bone, Model
from ..paint import Ctx, hash01, mix, noise3, pick, ramp, rel_y


def _pal(**kw) -> dict:
    return {k: ramp(*v) if isinstance(v, tuple) else v for k, v in kw.items()}


PALETTES = {
    "plagegeist_violett": _pal(hood=("#150a22", "#6e44a6"), rag=("#1e0f30", "#8a62c4"), wisp=("#3a1d5c", "#e6c8ff"),
                               bone=("#5a4e66", "#f2ecf8"), socket=("#0a0410", "#2c1a3e"), eye=("#d050ff", "#fff0ff"),
                               core=("#9a3af0", "#ffe0ff"), wing=("#2c1446", "#d2acff"), trim=("#3a2a14", "#c8a860")),
    "plagegeist_tuerkis": _pal(hood=("#06181c", "#2c7c7a"), rag=("#0a2a2e", "#3ea6a2"), wisp=("#0e3e40", "#c4fff6"),
                               bone=("#4a5c58", "#eef8f4"), socket=("#030c0c", "#143434"), eye=("#30f0e0", "#f0fffd"),
                               core=("#18b8c0", "#d8fffa"), wing=("#0a323a", "#9cfff0"), trim=("#2a3a3a", "#a8c8c0")),
}


def _a(col, alpha: float):
    """Farbe mit Deckkraft 0..1 (der Plagegeist wird durchscheinend gezeichnet)."""
    return col[:3] + (max(0, min(255, round(alpha * 255))),)


def build() -> Model:
    m = Model("vex", 64, 64)
    # Vanilla: ein Knochen "root" unter der Wurzel (VexModel holt ihn mit root.getChild("root"))
    vroot = Bone("root", m.root, (0.0, -2.5, 0.0))
    m.root.children.append(vroot)
    m.bones["vex_root"] = vroot

    m.bone("head", "vex_root", pivot=(0, 20, 0))
    m.cube("head", (-2.5, -5, -2.5), (5, 5, 5), "skull")
    m.bone("hood", "head")                                                       # Kapuze um den Schädel
    m.cube("hood", (-2.5, -5, -2.5), (5, 5, 5), "hood", inflate=0.55)
    m.cube("hood", (-2.1, -5.95, -3.35), (4.2, 1.0, 1.3), "hood_peak")       # vorstehende Kapuzenkante über dem Gesicht
    m.bone("hood_tip", "hood", pivot=(0, -5.3, 2.4), rotation=(-38, 0, 0))     # Zipfel, nach hinten abgeknickt
    m.cube("hood_tip", (-1.5, -2.2, -1.6), (3, 2.2, 2.2), "hood_tip")
    m.bone("hood_tail", "hood_tip", pivot=(0, -2.0, 0.2), rotation=(-30, 0, 0))
    m.cube("hood_tail", (-0.7, -1.8, -0.7), (1.4, 1.8, 1.4), "hood_tip")

    m.bone("body", "vex_root", pivot=(0, 20, 0))
    m.cube("body", (-1.5, 0, -1), (3, 4, 2), "robe")
    m.cube("body", (-1.5, 1, -1), (3, 5, 2), "shroud", inflate=-0.2)
    m.bone("mantle", "body")                                                     # Schulterkragen der Kutte
    m.cube("mantle", (-2.0, -0.4, -1.4), (4, 1.6, 2.8), "mantle")
    m.bone("tatters", "body", pivot=(0, 5.4, 0), rotation=(8, 0, 0))            # ausfransende Rauchfetzen
    m.cube("tatters", (-1.3, 0, -0.8), (2.6, 3, 1.6), "tatters")
    m.bone("wisp", "tatters", pivot=(0, 2.8, 0.1), rotation=(12, 0, 0))
    m.cube("wisp", (-0.7, 0, -0.45), (1.4, 2.6, 0.9), "wisp")

    m.bone("right_arm", "body", pivot=(-1.75, 0.25, 0))
    m.cube("right_arm", (-1.25, -0.5, -1), (2, 4, 2), "arm", inflate=-0.1)
    m.bone("left_arm", "body", pivot=(1.75, 0.25, 0))
    m.cube("left_arm", (-0.75, -0.5, -1), (2, 4, 2), "arm", inflate=-0.1)
    m.bone("left_wing", "body", pivot=(0.5, 1, 1))
    m.cube("left_wing", (0, 0, 0), (0, 5, 8), "wing")
    m.bone("right_wing", "body", pivot=(-0.5, 1, 1))
    m.cube("right_wing", (0, 0, 0), (0, 5, 8), "wing")
    return m


def _variant(ctx: Ctx) -> str:
    return ctx.params.get("variant")


def _cloth(ctx: Ctx, key: str, base: float, seed: int, folds: float = 0.08):
    x, y, z = ctx.world
    n = noise3(x * 0.7, y * 0.5, z * 0.7, seed)
    v = base + (n - 0.5) * 0.16 + (hash01(ctx.u, ctx.v, seed) - 0.5) * 0.05
    across = x if ctx.face in ("front", "back") else z
    v += math.sin(across * 2.2 + n * 2.0) * folds                              # senkrechte Falten
    if ctx.face == "top":
        v += 0.1
    elif ctx.face == "bottom":
        v -= 0.12
    return pick(ctx.pal[key], v)


# ------------------------------------------------------------------------------------------------- Kopf

def _skull(ctx: Ctx):
    """Bleiches Schädelgesicht tief in der Kapuze: dunkle Augenhöhlen mit glühenden Punkten, Nasenloch, Zahnreihe."""
    face, s, t = ctx.face, ctx.s, ctx.t
    shadow = pick(ctx.pal["socket"], 0.55)
    if face != "front" or s in (0, ctx.w - 1) or t == 0:
        return shadow                                                             # im Schatten der Kapuze
    bone = lambda v: pick(ctx.pal["bone"], v + (hash01(ctx.u, ctx.v, 701) - 0.5) * 0.05)  # noqa: E731
    if t == 1:
        if s in (1, 3):
            eye = pick(ctx.pal["eye"], 0.2)                                       # glühender Punkt in der Augenhöhle
            ctx.glow = pick(ctx.pal["eye"], 0.45)
            return eye
        return bone(0.3)                                                          # Nasenwurzel zwischen den Höhlen
    if t == 2:
        return pick(ctx.pal["socket"], 0.12) if s == 2 else bone(0.82)           # Nasenloch, Wangenknochen
    if t == 3:
        return bone(0.62) if s == 2 else bone(0.78)                               # Zahnreihe
    return bone(0.46) if s == 2 else pick(ctx.pal["socket"], 0.35)                # schmales Kinn


def _hood(ctx: Ctx):
    face, s, t = ctx.face, ctx.s, ctx.t
    if face == "bottom":
        return None
    if face == "front":
        # nur der Rand: das Gesicht liegt tief in der Kapuze
        if t == 0 or s in (0, ctx.w - 1):
            return _cloth(ctx, "hood", 0.36, 711, folds=0.0)
        return None
    col = _cloth(ctx, "hood", 0.42, 712)
    if face in ("west", "east", "back") and t == ctx.h - 1:
        # ausgefranster Saum über den Schultern
        return pick(ctx.pal["hood"], 0.26) if hash01(ctx.u, 713) > 0.3 else None
    if face in ("west", "east") and t == 0 or face == "back" and t == 0:
        col = pick(ctx.pal["hood"], 0.5)                                          # gerundete Kante oben
    if face in ("west", "east") and s == (ctx.w - 1 if face == "west" else 0):
        col = pick(ctx.pal["hood"], 0.24)                                         # Öffnung: dunkler Umschlag
    return col


def _hood_peak(ctx: Ctx):
    if ctx.face == "bottom":
        return pick(ctx.pal["hood"], 0.12)
    if ctx.face == "front":
        return pick(ctx.pal["hood"], 0.3 + (0.06 if ctx.s % 2 else 0.0))         # Kante wirft Schatten aufs Gesicht
    return _cloth(ctx, "hood", 0.5, 714, folds=0.0)


def _hood_tip(ctx: Ctx):
    if ctx.face == "bottom":
        return pick(ctx.pal["hood"], 0.18)
    return _cloth(ctx, "hood", 0.42, 721, folds=0.04)


# ------------------------------------------------------------------------------------------------- Leib

def _robe(ctx: Ctx):
    face, s, t = ctx.face, ctx.s, ctx.t
    if face == "front" and s == 1 and t == 2:
        col = pick(ctx.pal["core"], 0.45)                                         # Seelenlicht im Riss der Brust
        ctx.glow = col
        return col
    if face == "front" and (s, t) in ((0, 2), (2, 2), (1, 1), (1, 3)):
        return pick(ctx.pal["rag"], 0.12)                                         # ausgefranster Riss
    col = _cloth(ctx, "rag", 0.4, 731)
    if face == "front" and t == 0:
        return pick(ctx.pal["trim"], 0.45)                                        # Kordel unter dem Kragen
    return col


def _shroud(ctx: Ctx):
    """Unterleib: die Kutte wird nach unten heller und durchscheinender, der Saum reißt auf."""
    if ctx.face in ("top",):
        return None
    ry = rel_y(ctx)
    if ctx.face == "bottom":
        return _a(pick(ctx.pal["rag"], 0.3), 0.7)
    col = _cloth(ctx, "rag", 0.36, 741)
    col = mix(col, pick(ctx.pal["wisp"], 0.45), max(0.0, ry - 0.4) * 0.6)
    if ctx.t == ctx.h - 1 and hash01(ctx.s, ctx.face, 742) > 0.6:
        return None                                                               # Risse im Saum
    return _a(col, 0.97 - ry * 0.17)


def _mantle(ctx: Ctx):
    face, s, t = ctx.face, ctx.s, ctx.t
    if face == "bottom":
        return pick(ctx.pal["hood"], 0.15)
    col = _cloth(ctx, "hood", 0.45, 751, folds=0.05)
    if face != "top" and t == ctx.h - 1:
        return _a(pick(ctx.pal["hood"], 0.3), 0.9) if hash01(ctx.u, 752) > 0.3 else None
    if face == "front" and s == ctx.w // 2 and t == 0:
        col = pick(ctx.pal["trim"], 0.8)                                          # Fibel
    return col


def _tatters(ctx: Ctx):
    """Rauchfetzen unter der Kutte: senkrechte Strähnen, die nach unten verblassen und ungleich lang auslaufen."""
    if ctx.face in ("top", "bottom"):
        return None
    ry = rel_y(ctx)
    strand = hash01(ctx.s, ctx.face, 761)
    if ry > 0.5 + strand * 0.5:
        return None
    x, y, z = ctx.world
    v = 0.4 + (noise3(x * 0.9, y * 0.6, z * 0.9, 762) - 0.5) * 0.24 + (0.1 if ctx.s % 2 else 0.0)
    col = mix(pick(ctx.pal["rag"], 0.45), pick(ctx.pal["wisp"], v + 0.2), 0.35 + ry * 0.55)
    return _a(col, 0.8 - ry * 0.4)


def _wisp(ctx: Ctx):
    if ctx.face in ("top", "bottom"):
        return None
    ry = rel_y(ctx)
    if ry > 0.4 + hash01(ctx.s, ctx.face, 771) * 0.6:
        return None
    col = pick(ctx.pal["wisp"], 0.6 + ry * 0.3)
    return _a(col, 0.5 - ry * 0.3)


def _arm(ctx: Ctx):
    """Zerrissener Ärmel, unten knöcherne Finger (in der rechten Hand liegt das Schwert)."""
    ry = rel_y(ctx)
    if ctx.face == "bottom":
        return pick(ctx.pal["bone"], 0.45)
    if ry > 0.74:
        v = 0.62 + (0.14 if ctx.face == "front" else 0.0)
        if ctx.face in ("front", "back") and ctx.s == 1 and ctx.t == ctx.h - 1:
            v = 0.35                                                              # Spalt zwischen den Fingern
        return pick(ctx.pal["bone"], v + (hash01(ctx.u, ctx.v, 781) - 0.5) * 0.06)
    col = _cloth(ctx, "rag", 0.42, 782, folds=0.03)
    if ry > 0.5 and hash01(ctx.s, ctx.face, 783) > 0.55:
        col = pick(ctx.pal["rag"], 0.22)                                          # ausgerissener Ärmelsaum
    if ctx.face == "top":
        col = pick(ctx.pal["hood"], 0.45)
    return col


def _wing(ctx: Ctx):
    """
    Flügel aus Leichentuch und Rauch: an der Wurzel volle Höhe, zur Spitze hin schmaler; der untere Rand reißt in
    lange Fetzen aus, die hell verwehen. Oben eine dunkle, feste Kante, dazu zwei dunkle Faltenlinien.
    """
    lz = ctx.local[2]
    frac = max(0.0, min(1.0, lz / 8.0))                                            # 0 = Wurzel, 1 = Spitze
    ry = rel_y(ctx)
    col_i = int(lz)
    # Umriss: oben gerade, unten tief eingerissen (jede zweite Spalte ein langer Fetzen, dazwischen Kerben)
    limit = 0.98 - frac * 0.55
    cut = hash01(col_i, 791)
    limit += 0.18 if cut > 0.62 else -0.2 if cut < 0.3 else 0.0             # lange Fetzen und tiefe Kerben
    if col_i >= 7 and ry > 0.25:
        return None                                                               # Spitze: nur die Kante
    if ry > limit:
        return None
    x, y, z = ctx.world
    v = 0.3 + frac * 0.35 + ry * 0.2 + (noise3(x * 0.7, y * 0.9, z * 0.7, 792) - 0.5) * 0.12
    if ry < 0.2:
        v = 0.2 + frac * 0.15                                                     # feste dunkle Oberkante
    elif col_i in (2, 5):
        v -= 0.12                                                                 # Faltenlinien
    col = pick(ctx.pal["wing"], v)
    if limit - ry < 0.22:
        col = mix(col, pick(ctx.pal["wisp"], 0.75), 0.45)                         # die Fetzen verwehen hell
    alpha = 0.9 - frac * 0.35 - ry * 0.25
    return _a(col, max(0.3, alpha))


def materials(variant: str) -> dict:
    return {"skull": _skull, "hood": _hood, "hood_peak": _hood_peak, "hood_tip": _hood_tip, "robe": _robe, "shroud": _shroud,
            "mantle": _mantle, "tatters": _tatters, "wisp": _wisp, "arm": _arm, "wing": _wing}


def preview_pose(variant: str) -> dict:
    """So, wie VexModel ihn im Flug zeigt (Flügel gespreizt, Leib leicht nach vorn, Arme abgespreizt)."""
    return {"left_wing": (27, 63, -27), "right_wing": (27, -63, 27), "body": (9, 0, 0), "right_arm": (0, 0, 36),
            "left_arm": (0, 0, -36)}
