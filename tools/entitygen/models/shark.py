"""
Hai: ein schlanker Jäger der warmen und tiefen Meere – stromlinienförmig, mit spitzer Schnauze, schwarzen Augen,
fünf Kiemenspalten und einem Maul voller dreieckiger Zähne, dessen Unterkiefer weit aufklappt. Eine hohe Rückenflosse
(die man über dem Wasser sieht), breite Brustflossen und eine Schwanzflosse mit langem oberem Lappen.

Gegenschattiert: oben schiefergrau-blau, unten weiß, mit scharfer, leicht welliger Grenze; die Flossenspitzen dunkler.
Knochen-Namen werden in SharkModel (Java) für Schwimmen und Biss verwendet.
"""
from __future__ import annotations

from ..geometry import Model
from ..paint import Ctx, hash01, hexc, mix, noise3, pick

PALETTES = {
    "shark": {
        "back": [hexc(c) for c in ("#2a3440", "#36424f", "#43515f", "#52606e", "#62707e", "#74828f")],
        "belly": [hexc(c) for c in ("#b8c0c6", "#ccd3d8", "#dde2e6", "#eaeef0", "#f4f6f7")],
        "fin": [hexc(c) for c in ("#1c242e", "#2a3440", "#3a4652")],
        "mouth": [hexc(c) for c in ("#3a1a1e", "#6a2a30", "#9a4a4e")],
        "tooth": [hexc(c) for c in ("#c8c4b4", "#ecead8", "#ffffff")],
        "eye": [hexc(c) for c in ("#050608", "#14181e", "#9aa8b8")],
    },
}

BODY = (0.0, 18.0, 0.0)


def build() -> Model:
    m = Model("shark", 160, 96)
    m.tex_res = 2
    m.bone("body", pivot=BODY)
    m.cube("body", (-4.0, -4.0, -8.0), (8, 8, 16), "skin")
    m.cube("body", (-3.0, -4.8, -6.0), (6, 0.8, 12), "skin")                       # Rückenwölbung
    m.cube("body", (-3.0, 4.0, -6.5), (6, 0.8, 11), "skin")                        # Bauchwölbung

    m.bone("head", "body", pivot=(0, -0.2, -8.0))
    m.cube("head", (-3.6, -3.6, -6.5), (7.2, 6.4, 6.5), "skin")
    m.cube("head", (-2.6, -3.0, -9.5), (5.2, 4.6, 3.0), "skin")                    # Schnauze
    m.cube("head", (-1.6, -2.4, -11.0), (3.2, 3.2, 1.5), "skin")                   # Schnauzenspitze
    m.cube("head", (-2.4, 1.6, -9.2), (4.8, 0.6, 0.6), "teeth")                    # obere Zahnreihe
    for side, sx in (("left", 1), ("right", -1)):
        m.cube("head", ((3.6 if sx > 0 else -4.2), -1.8, -6.0), (0.6, 1.2, 1.2), "eye")
    m.bone("jaw", "head", pivot=(0, 2.8, -3.0))
    m.cube("jaw", (-3.0, 0, -6.0), (6.0, 1.6, 6.0), "jaw")
    m.cube("jaw", (-2.6, -0.6, -5.8), (5.2, 0.6, 0.6), "teeth")                    # untere Zahnreihe

    m.bone("dorsal", "body", pivot=(0, -4.6, -2.5), rotation=(-28, 0, 0))
    m.cube("dorsal", (-0.6, -6.5, -1.5), (1.2, 6.5, 4.0), "fin")
    m.bone("dorsal_tip", "dorsal", pivot=(0, -6.5, 0), rotation=(-20, 0, 0))
    m.cube("dorsal_tip", (-0.4, -2.5, -1.0), (0.8, 2.5, 2.2), "fin")
    for side, sx in (("left", 1), ("right", -1)):
        pec = f"{side}_pectoral"
        m.bone(pec, "body", pivot=(3.6 * sx, 2.8, -4.0), rotation=(18, 0, 32 * sx))
        m.cube(pec, (0 if sx > 0 else -7, -0.5, -1.0), (7, 1.0, 4.5), "fin")
        pel = f"{side}_pelvic"
        m.bone(pel, "body", pivot=(2.2 * sx, 3.6, 5.5), rotation=(20, 0, 25 * sx))
        m.cube(pel, (0 if sx > 0 else -3, -0.4, -0.5), (3, 0.8, 2.5), "fin")

    m.bone("tail", "body", pivot=(0, -0.4, 8.0))
    m.cube("tail", (-3.0, -3.2, 0), (6, 6.4, 7), "skin")
    m.cube("tail", (-0.4, -5.2, 3.0), (0.8, 2.0, 2.0), "fin")                       # zweite Rückenflosse
    m.cube("tail", (-0.4, 3.2, 3.5), (0.8, 1.6, 2.0), "fin")                        # Afterflosse
    m.bone("tail_end", "tail", pivot=(0, 0, 7))
    m.cube("tail_end", (-1.6, -2.0, 0), (3.2, 4.0, 6), "skin")
    m.bone("caudal", "tail_end", pivot=(0, 0, 5.5))
    m.bone("caudal_upper", "caudal", pivot=(0, -1.0, 0), rotation=(38, 0, 0))
    m.cube("caudal_upper", (-0.5, -9.0, -1.0), (1.0, 9.0, 3.4), "fin")
    m.bone("caudal_lower", "caudal", pivot=(0, 1.0, 0), rotation=(-40, 0, 0))
    m.cube("caudal_lower", (-0.5, 0, -1.0), (1.0, 5.5, 2.8), "fin")
    return m


def _pos(ctx: Ctx):
    x, y, z = ctx.world
    return x - BODY[0], y - BODY[1], z - BODY[2]


def _skin(ctx: Ctx):
    x, y, z = _pos(ctx)
    # Grenze zwischen Rücken und Bauch: etwas unter der Mitte, leicht wellig
    line = 0.8 + (noise3(x * 0.2, 0.0, z * 0.25, 5) - 0.5) * 1.6
    v = 0.45 + (noise3(x * 0.3, y * 0.3, z * 0.3, 6) - 0.5) * 0.18
    back = pick(ctx.pal["back"], v + 0.12 * (1.0 - min(1.0, (y + 5) / 6.0)))
    belly = pick(ctx.pal["belly"], 0.6 + (noise3(x * 0.3, y, z * 0.3, 7) - 0.5) * 0.15)
    if ctx.face == "top":
        col = back
    elif ctx.face == "bottom":
        col = belly
    else:
        col = back if y < line - 0.3 else (belly if y > line + 0.3 else mix(back, belly, 0.5))
    # Kiemenspalten: fünf senkrechte Linien vor den Brustflossen (Seiten von Kopf und Leib)
    if ctx.face in ("west", "east") and -10.0 < z < -5.0 and -2.5 < y < 2.0:
        k = (z + 10.0) / 1.0
        if abs(k - round(k)) < 0.16 and 1 <= round(k) <= 5:
            col = mix(col, pick(ctx.pal["fin"], 0.2), 0.7)
    # feine Narben
    if hash01(ctx.u, ctx.v, 8) < 0.006:
        col = mix(col, pick(ctx.pal["belly"], 0.3), 0.6)
    return col


def _fin(ctx: Ctx):
    col = _skin(ctx)
    t = (ctx.t + 0.5) / max(ctx.h, 1)
    s = (ctx.s + 0.5) / max(ctx.w, 1)
    edge = max(1.0 - t, s) if ctx.face in ("west", "east") else 0.0
    return mix(col, pick(ctx.pal["fin"], 0.4), min(0.7, 0.25 + edge * 0.5))


def _jaw(ctx: Ctx):
    if ctx.face == "top":
        return pick(ctx.pal["mouth"], 0.4 + 0.3 * hash01(ctx.u, ctx.v, 9))
    return pick(ctx.pal["belly"], 0.55)


def _teeth(ctx: Ctx):
    # dreieckige Zähne: abwechselnd Spitze und Lücke
    if ctx.s % 2 == 0:
        return pick(ctx.pal["tooth"], 0.8)
    return pick(ctx.pal["mouth"], 0.3)


def _eye(ctx: Ctx):
    if ctx.s == 0 and ctx.t == 0:
        return pick(ctx.pal["eye"], 1.0)
    return pick(ctx.pal["eye"], 0.1)


def materials(variant: str) -> dict:
    return {"skin": _skin, "fin": _fin, "jaw": _jaw, "teeth": _teeth, "eye": _eye}
