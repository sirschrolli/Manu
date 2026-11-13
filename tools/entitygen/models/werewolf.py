"""
Werwolf: ein gebeugter Riese aus Wolf und Mensch, gut zweieinhalb Blöcke groß – zottiges, graubraunes Fell mit
hellerem Bauch und einer dichten Mähne über Nacken und Schultern, ein langer Wolfskopf mit Reißzähnen, spitzen Ohren und
glühenden, bernsteinfarbenen Augen; lange Arme mit Krallen, Hinterläufe wie ein Wolf (auf den Zehen), ein buschiger
Schwanz und die Fetzen einer Hose, die er einmal als Mensch trug.

Knochen-Namen werden in WerewolfModel (Java) für die Animationen verwendet (aufrecht gehen, auf allen vieren rennen,
Ansprung, Krallenhieb, Heulen).
"""
from __future__ import annotations

from ..geometry import Model
from ..paint import Ctx, hash01, hexc, mix, noise3, pick, rel_y

PALETTES = {
    "werewolf": {
        "fur": [hexc(c) for c in ("#221e1c", "#352e2a", "#4a403a", "#60544a", "#78695c", "#928270", "#ae9e88")],
        "belly": [hexc(c) for c in ("#5e5246", "#7a6c5c", "#968874", "#b0a28c", "#c8bca6")],
        "mane": [hexc(c) for c in ("#161412", "#262220", "#3a3430", "#504842", "#6a6058", "#8a8076")],
        "skin": [hexc(c) for c in ("#2a2020", "#3c2e2c", "#523e3a", "#6a504a")],
        "nose": [hexc(c) for c in ("#0c0a0a", "#1c1616", "#2e2626")],
        "mouth": [hexc(c) for c in ("#3a0e10", "#5e1a1c", "#8a2a2c")],
        "tooth": [hexc(c) for c in ("#9a9078", "#d8ccb0", "#f4ecd8")],
        "claw": [hexc(c) for c in ("#1a1614", "#3a322c", "#6a5e52", "#a09484")],
        "eye": [hexc(c) for c in ("#8a3a00", "#ff9a1a", "#ffd25a", "#fff4b0")],
        "cloth": [hexc(c) for c in ("#1c2028", "#2a3040", "#3a4256", "#4c566c")],
    },
}


def build() -> Model:
    m = Model("werewolf", 128, 128)
    m.tex_res = 2
    # Becken und Brust: gebeugt, die Brust weit vorn
    m.bone("hips", pivot=(0, 4.0, 1.5))
    m.cube("hips", (-4.5, -3.0, -3.0), (9, 5, 6), "fur")
    m.cube("hips", (-4.8, -1.2, -3.2), (9.6, 3.4, 6.4), "trousers")
    m.bone("body", "hips", pivot=(0, -2.5, 0), rotation=(32, 0, 0))
    m.cube("body", (-5.5, -13.0, -4.0), (11, 13, 8), "chest")
    m.cube("body", (-6.2, -15.5, -3.2), (12.4, 5.0, 8.4), "mane")              # Mähne über Nacken und Schultern
    m.cube("body", (-4.5, -17.0, -0.5), (9, 3.0, 5.0), "mane")
    m.cube("body", (-2.0, -14.0, 3.8), (4, 9.0, 1.4), "mane")                  # Rückenkamm
    # Kopf: Schädel, lange Schnauze, Unterkiefer, Ohren
    m.bone("head", "body", pivot=(0, -14.0, -3.5), rotation=(-30, 0, 0))
    m.cube("head", (-3.6, -6.4, -5.5), (7.2, 7.0, 6.5), "fur")
    m.cube("head", (-3.9, -2.0, -4.8), (7.8, 3.6, 5.0), "mane")                # Backenbart
    m.cube("head", (-2.3, -4.6, -12.0), (4.6, 3.2, 6.7), "snout")
    m.cube("head", (-1.7, -5.3, -10.6), (3.4, 0.9, 5.2), "bridge")             # Nasenrücken
    m.cube("head", (-1.0, -4.9, -12.4), (2.0, 1.3, 1.0), "nose")
    m.cube("head", (-3.5, -5.7, -5.8), (1.7, 1.1, 0.4), "eye")
    m.cube("head", (1.8, -5.7, -5.8), (1.7, 1.1, 0.4), "eye")
    m.cube("head", (-3.4, -6.6, -6.0), (6.8, 1.0, 1.2), "fur")                 # Stirnwulst über den Augen
    m.cube("head", (-2.1, -1.5, -11.7), (0.7, 1.2, 0.7), "tooth")              # obere Reißzähne
    m.cube("head", (1.4, -1.5, -11.7), (0.7, 1.2, 0.7), "tooth")
    m.bone("jaw", "head", pivot=(0, -1.4, -5.0), rotation=(8, 0, 0))
    m.cube("jaw", (-2.0, 0, -6.7), (4.0, 1.8, 6.7), "jaw")
    m.cube("jaw", (-1.7, -0.9, -6.5), (0.6, 1.0, 0.6), "tooth")                # untere Reißzähne
    m.cube("jaw", (1.1, -0.9, -6.5), (0.6, 1.0, 0.6), "tooth")
    for side, sx in (("left", 1), ("right", -1)):
        ear = f"{side}_ear"
        m.bone(ear, "head", pivot=(2.6 * sx, -6.2, -1.2), rotation=(-12, 0, 14 * sx))
        m.cube(ear, (-1.4, -2.4, -0.7), (2.8, 2.4, 1.4), "ear")
        m.cube(ear, (-0.8, -4.4, -0.5), (1.6, 2.0, 1.0), "ear")
    # Arme: lang, mit Krallenhänden
    for side, sx in (("left", 1), ("right", -1)):
        arm = f"{side}_arm"
        m.bone(arm, "body", pivot=(6.4 * sx, -11.5, -0.5), rotation=(-26, 0, -8 * sx))
        m.cube(arm, (-2.2, -1.5, -2.4), (4.4, 11.5, 4.8), "fur")
        m.cube(arm, (-2.5, -2.0, -2.6), (5.0, 4.0, 5.2), "mane")               # zottige Schulter
        fore = f"{side}_forearm"
        m.bone(fore, arm, pivot=(0, 9.8, 0), rotation=(-30, 0, 0))
        m.cube(fore, (-1.9, 0, -2.0), (3.8, 9.5, 4.0), "fur")
        m.cube(fore, (-2.2, 9.0, -2.4), (4.4, 3.0, 4.6), "skin")               # Hand
        for k, dx in enumerate((-1.5, 0.0, 1.5)):
            m.cube(fore, (dx - 0.35, 11.8, -2.2), (0.7, 2.4, 0.7), "claw")
        m.cube(fore, (-0.35 + 1.9 * sx, 10.2, -1.6), (0.7, 1.6, 0.7), "claw")  # Daumenkralle
    # Hinterläufe wie ein Wolf: Oberschenkel nach vorn, Unterschenkel zurück, lange Fußknochen, auf den Zehen
    for side, sx in (("left", 1), ("right", -1)):
        thigh = f"{side}_leg"
        m.bone(thigh, "hips", pivot=(3.4 * sx, -0.5, 0.5), rotation=(-26, 0, 0))
        m.cube(thigh, (-2.4, -0.5, -2.5), (4.8, 8.5, 5.2), "fur")
        m.cube(thigh, (-2.6, 0.5, -2.7), (5.2, 4.5, 5.6), "trousers")
        shin = f"{side}_shin"
        m.bone(shin, thigh, pivot=(0, 7.6, 0.3), rotation=(58, 0, 0))
        m.cube(shin, (-1.7, 0, -1.8), (3.4, 7.8, 3.6), "fur")
        m.cube(shin, (-1.9, 0, -2.0), (3.8, 2.6, 4.0), "trousers_rag")
        foot = f"{side}_foot"
        m.bone(foot, shin, pivot=(0, 7.4, 0), rotation=(-38, 0, 0))
        m.cube(foot, (-1.4, 0, -1.4), (2.8, 5.6, 2.8), "fur")
        m.cube(foot, (-1.9, 4.6, -4.4), (3.8, 1.6, 4.8), "skin")               # Pfote
        for dx in (-1.2, 0.0, 1.2):
            m.cube(foot, (dx - 0.3, 5.0, -5.4), (0.6, 1.0, 1.0), "claw")
    # buschiger Schwanz
    m.bone("tail", "hips", pivot=(0, -1.5, 3.0), rotation=(48, 0, 0))
    m.cube("tail", (-1.6, -1.4, 0), (3.2, 3.0, 6.0), "mane")
    m.bone("tail_tip", "tail", pivot=(0, 0, 6.0), rotation=(14, 0, 0))
    m.cube("tail_tip", (-1.9, -1.6, 0), (3.8, 3.4, 5.5), "mane")
    m.cube("tail_tip", (-1.2, -1.0, 5.5), (2.4, 2.2, 2.0), "fur")
    return m


# =============================================================================================
# Bemalung
# =============================================================================================

_LIT = {"top": 0.12, "front": 0.04, "west": 0.0, "east": -0.04, "back": -0.06, "bottom": -0.18}


def _strands(ctx: Ctx, scale: float = 1.0) -> float:
    """Fell: große, weiche Helligkeitsflächen, darin längs gestreckte Strähnen und kleine Büschel."""
    x, y, z = ctx.world
    macro = (noise3(x * 0.16, y * 0.16, z * 0.16, 70) - 0.5) * 0.24
    strand = (noise3(x * 0.85 * scale, y * 0.16 * scale, z * 0.85 * scale, 71) - 0.5) * 0.18
    tuft = (hash01(ctx.u // 2, ctx.v // 2, 73) - 0.5) * 0.07
    return macro + strand + tuft


def _fur(ctx: Ctx):
    v = 0.52 + _LIT[ctx.face] - rel_y(ctx) * 0.1 + _strands(ctx)
    # dunkler Sattel über Rücken und Nacken, hellere Flanken und Läufe
    if ctx.face in ("top", "back") and ctx.cube.bone.name in ("body", "hips", "head", "tail"):
        v -= 0.14
    elif "arm" in ctx.cube.bone.name or "leg" in ctx.cube.bone.name or "shin" in ctx.cube.bone.name:
        v += 0.06
    # zottige Unterkante: einzelne Strähnen hängen dunkler herab
    if ctx.face not in ("top", "bottom") and ctx.t >= ctx.h - 2 and hash01(ctx.u, 0, 77) > 0.55:
        v -= 0.14
    return pick(ctx.pal["fur"], v)


def _chest(ctx: Ctx):
    """Die Brust: vorn ein heller Latz, der sich zum Bauch hin verjüngt (wie bei einem Wolf)."""
    if ctx.face == "front":
        rs = (ctx.s + 0.5) / max(ctx.w, 1) - 0.5
        rt = (ctx.t + 0.5) / max(ctx.h, 1)
        width = 0.36 - rt * 0.22
        if abs(rs) < width:
            edge = abs(rs) > width - 0.06
            return pick(ctx.pal["belly"], (0.42 if edge else 0.55) + _strands(ctx) * 0.9)
    return _fur(ctx)


def _mane(ctx: Ctx):
    # die Mähne: dunkel am Ansatz, die Spitzen silbrig aufgehellt
    v = 0.42 + _LIT[ctx.face] + _strands(ctx, 1.4) * 1.2 + (1.0 - rel_y(ctx)) * 0.12
    if ctx.face not in ("top", "bottom") and ctx.t >= ctx.h - 1 and hash01(ctx.u, 0, 79) > 0.45:
        return (0, 0, 0, 0)                                            # Fransen an der Unterkante
    return pick(ctx.pal["mane"], v)


def _belly(ctx: Ctx):
    return pick(ctx.pal["belly"], 0.5 + _LIT[ctx.face] + _strands(ctx) * 0.8)


def _snout(ctx: Ctx):
    rs = (ctx.s + 0.5) / max(ctx.w, 1)
    if ctx.face == "front":
        # die Lefzen: unten dunkel
        if rel_y(ctx) > 0.7:
            return pick(ctx.pal["mouth"], 0.2)
        return pick(ctx.pal["fur"], 0.42 + _strands(ctx) * 0.6)
    if ctx.face == "bottom":
        return pick(ctx.pal["mouth"], 0.3)
    if ctx.face in ("west", "east") and rel_y(ctx) > 0.72:
        return pick(ctx.pal["mouth"], 0.25)                            # Maulspalte an den Seiten
    if ctx.face == "top" and abs(rs - 0.5) < 0.2:
        return pick(ctx.pal["fur"], 0.6 + _strands(ctx) * 0.5)          # heller Nasenrücken
    return pick(ctx.pal["fur"], 0.5 + _LIT[ctx.face] + _strands(ctx) * 0.6)


def _bridge(ctx: Ctx):
    return pick(ctx.pal["fur"], 0.6 + _LIT[ctx.face] + _strands(ctx) * 0.5)


def _jaw(ctx: Ctx):
    if ctx.face == "top":
        return pick(ctx.pal["mouth"], 0.5 + (hash01(ctx.u, ctx.v, 81) - 0.5) * 0.2)   # Zunge und Gaumen
    if ctx.face == "bottom":
        return pick(ctx.pal["belly"], 0.4)
    return pick(ctx.pal["fur"], 0.42 + _strands(ctx) * 0.5)


def _nose(ctx: Ctx):
    if ctx.face == "front" and ctx.t == 0:
        return pick(ctx.pal["nose"], 0.8)
    return pick(ctx.pal["nose"], 0.3)


def _eye(ctx: Ctx):
    rs = (ctx.s + 0.5) / max(ctx.w, 1)
    col = pick(ctx.pal["eye"], 0.9 if 0.3 < rs < 0.7 else 0.55)
    if ctx.face == "front" and abs(rs - 0.5) < 0.12:
        col = pick(ctx.pal["nose"], 0.1)                               # Pupille
        ctx.glow = mix((0, 0, 0, 255), pick(ctx.pal["eye"], 0.5), 0.4)
        return col
    ctx.glow = col
    return col


def _ear(ctx: Ctx):
    if ctx.face == "front":
        rs = (ctx.s + 0.5) / max(ctx.w, 1)
        if 0.25 < rs < 0.75 and rel_y(ctx) > 0.25:
            return pick(ctx.pal["skin"], 0.6)                          # Innenseite
    return _fur(ctx)


def _tooth(ctx: Ctx):
    return pick(ctx.pal["tooth"], 0.7 - rel_y(ctx) * 0.3 + _LIT[ctx.face] * 0.5)


def _claw(ctx: Ctx):
    return pick(ctx.pal["claw"], 0.65 - rel_y(ctx) * 0.4 + _LIT[ctx.face] * 0.5)


def _skin(ctx: Ctx):
    if ctx.face == "bottom":
        return pick(ctx.pal["skin"], 0.25)                             # Ballen
    return mix(_fur(ctx), pick(ctx.pal["skin"], 0.5), 0.35)


def _trousers(ctx: Ctx):
    """Fetzen einer dunklen Leinenhose: ausgefranste Kanten, Risse, durch die das Fell sieht."""
    x, y, z = ctx.world
    if noise3(x * 0.7, y * 0.7, z * 0.7, 83) > 0.68:
        return _fur(ctx)                                               # Riss
    if ctx.face not in ("top", "bottom") and ctx.t >= ctx.h - 1 and hash01(ctx.u, 0, 85) > 0.5:
        return (0, 0, 0, 0)
    v = 0.45 + _LIT[ctx.face] + (noise3(x * 0.4, y * 0.15, z * 0.4, 87) - 0.5) * 0.25
    if int(y * 2) % 5 == 0:
        v -= 0.08                                                      # Webfalten
    return pick(ctx.pal["cloth"], v)


def _trousers_rag(ctx: Ctx):
    if ctx.face in ("top", "bottom"):
        return (0, 0, 0, 0)
    if rel_y(ctx) > 0.45 and hash01(ctx.u, 0, 89) > 0.4:
        return (0, 0, 0, 0)                                            # lange Fransen
    return _trousers(ctx)


def materials(variant: str) -> dict:
    return {"fur": _fur, "chest": _chest, "mane": _mane, "belly": _belly, "snout": _snout, "bridge": _bridge, "jaw": _jaw, "nose": _nose, "eye": _eye,
            "ear": _ear, "tooth": _tooth, "claw": _claw, "skin": _skin, "trousers": _trousers, "trousers_rag": _trousers_rag}
