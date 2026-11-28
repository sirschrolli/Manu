"""
Umbrer: das Volk der Schattenwelt – groß, schlank, mit fahler, lavendelgrauer Haut, langen spitzen Ohren und
silbrig leuchtenden Augen. Sie tragen lange Roben mit Kapuze und einem Umhang über den Schultern; Säume und Schärpe
sind mit Mondsilber bestickt.

Ein Modell für alle Berufe – das Zubehör (Schürze und Hammer, Kräutertasche, Schleifbrille, Seherhut, Strohhut,
Rüstung und Speer) steckt in eigenen Knochen und wird im Spiel je Beruf ein- oder ausgeblendet; auf der Textur der
anderen Berufe bleibt es durchsichtig.

Knochen-Namen werden in UmbrerModel (Java) für Animationen und das Zubehör verwendet.
"""
from __future__ import annotations

import math

from ..geometry import Model
from ..paint import Ctx, hash01, mix, noise3, pick, ramp, rel_y

SKIN = {"skin": ramp("#5e5670", "#d6cfe4"), "eye": ramp("#9ac8ff", "#ffffff"), "lip": ramp("#4a3e5a", "#8a7a9a")}


def _pal(robe: tuple[str, str], trim: tuple[str, str], **extra) -> dict:
    pal = dict(SKIN)
    pal["robe"] = ramp(*robe, steps=7)
    pal["trim"] = ramp(*trim)
    pal["leather"] = ramp("#22161c", "#7a5a5e")
    pal["metal"] = ramp("#4a5470", "#eef2ff", steps=7)
    pal["wood"] = ramp("#1e1426", "#6a4a7a")
    pal["shoe"] = ramp("#141018", "#4a4054")
    pal["back"] = pal["robe"]
    for key, value in extra.items():
        pal[key] = ramp(*value)
    return pal


# Beruf -> Farben. Zubehör, das ein Beruf nicht trägt, bleibt auf seiner Textur durchsichtig (siehe GEAR).
PALETTES = {
    "moonsmith": _pal(("#2a2c3c", "#9a9cb8"), ("#7a84a4", "#eef2ff"), soot=("#141016", "#3a3440")),
    "herbalist": _pal(("#143a34", "#62b4a0"), ("#7a6a3a", "#e4d494"), herb=("#1e4a2e", "#7ac87a")),
    "crystal_cutter": _pal(("#1c285e", "#6a80dc"), ("#5a50d0", "#dcd4ff"), crystal=("#3a2aa0", "#e6deff")),
    "seer": _pal(("#28144c", "#8a5ad0"), ("#8a7ac8", "#f4eeff"), star=("#b8a8ff", "#ffffff")),
    "farmer": _pal(("#382e24", "#a08a70"), ("#6a5a3a", "#c8b47a"), straw=("#6a5424", "#e8d084")),
    "guard": _pal(("#18203c", "#5a6aa0"), ("#7a84a4", "#eef2ff"), plume=("#3a1a6a", "#a070e0")),
}

# Welches Zubehör trägt welcher Beruf (Knochen-Namen)?
GEAR = {
    "moonsmith": {"hood", "apron"},
    "herbalist": {"hood", "satchel", "sprig"},
    "crystal_cutter": {"hood", "goggles", "pendant"},
    "seer": {"hood", "seer_hat", "amulet"},
    "farmer": {"straw_hat"},
    "guard": {"helmet", "armor", "pauldron_l", "pauldron_r", "spear", "cape"},
}
ALL_GEAR = set().union(*GEAR.values())


def build() -> Model:
    m = Model("umbrer", 128, 128)

    # ------------------------------------------------------------------ Beine (fast ganz unter der Robe)
    for side in (1, -1):
        name = "left_leg" if side > 0 else "right_leg"
        m.bone(name, pivot=(1.6 * side, 12, 0))
        m.cube(name, (-1, 0, -1), (2, 11, 2), "leg")
        m.cube(name, (-1.2, 10.5, -3.2), (2.4, 1.5, 4), "shoe")

    # ------------------------------------------------------------------ Rumpf
    m.bone("body", pivot=(0, 10, 0))
    m.cube("body", (-4, -12, -2.5), (8, 12, 5), "torso")
    m.cube("body", (-4.3, -1.6, -2.8), (8.6, 2, 5.6), "sash")
    m.cube("body", (-1.2, -1.2, -3.15), (2.4, 4.5, 0.4), "sash")            # Schärpenenden vorne
    m.cube("body", (-4.8, -12.6, -3.05), (9.6, 3.2, 6.1), "mantle")         # Schulterumhang

    # Robe unterhalb der Hüfte (schwingt beim Gehen)
    m.bone("skirt", "body", pivot=(0, 0, 0))
    m.cube("skirt", (-4.3, 0, -2.8), (8.6, 7, 5.6), "robe")
    m.cube("skirt", (-5, 7, -3.3), (10, 6, 6.6), "robe")

    # ------------------------------------------------------------------ Kopf
    m.bone("head", "body", pivot=(0, -12, 0))
    m.cube("head", (-3.5, -8, -3.25), (7, 8, 6.5), "face")
    m.cube("head", (-0.5, -4.4, -3.95), (1, 2, 0.7), "skin")                # schmale Nase
    m.cube("head", (-2.8, -5.2, -3.3), (1.6, 1, 0.1), "eye")
    m.cube("head", (1.2, -5.2, -3.3), (1.6, 1, 0.1), "eye")
    for side in (1, -1):
        name = "ear_l" if side > 0 else "ear_r"
        # lange, spitze Ohren, schräg nach oben und hinten
        m.bone(name, "head", pivot=(3.5 * side, -4.6, -0.2), rotation=(0, -32 * side, -24 * side))
        m.cube(name, (0 if side > 0 else -5, -0.9, -0.4), (5, 1.8, 0.8), "skin")

    # Kapuze: offen nach vorn und unten
    m.bone("hood", "head", pivot=(0, 0, 0))
    m.cube("hood", (-4.1, -8.7, -3.75), (8.2, 9, 7.7), "hood", faces=("top", "back", "west", "east"))
    m.cube("hood", (-1.8, -9.6, 1.6), (3.6, 2, 3.4), "hood")                # Zipfel
    m.cube("hood", (-4.1, -8.7, -3.85), (8.2, 1, 0.4), "trim")              # Saum über der Stirn

    # ------------------------------------------------------------------ Arme (lange Ärmel, schmale Hände)
    for side in (1, -1):
        name = "left_arm" if side > 0 else "right_arm"
        m.bone(name, "body", pivot=(5.2 * side, -11, 0))
        m.cube(name, (-1.5, -1, -1.5), (3, 8, 3), "sleeve")
        m.cube(name, (-1.8, 6.5, -1.8), (3.6, 3.5, 3.6), "cuff")
        m.cube(name, (-1, 9.5, -1), (2, 2.5, 2), "hand")

    # ------------------------------------------------------------------ Zubehör der Berufe
    # Mondschmied: Lederschürze und Hammer am Gürtel
    m.bone("apron", "body", pivot=(0, 0, 0))
    m.cube("apron", (-3.4, -7, -3.0), (6.8, 16, 0.4), "leather")
    m.cube("apron", (4.1, -1.5, -1.6), (0.8, 4.5, 0.8), "wood")             # Hammerstiel
    m.cube("apron", (3.7, 2.6, -2.3), (1.6, 1.4, 2.2), "metal")             # Hammerkopf

    # Kräuterkundige: Tasche an der Hüfte, Riemen, Kräuterzweig an der Kapuze
    m.bone("satchel", "body", pivot=(0, 0, 0))
    m.cube("satchel", (-5.9, -1.5, -1.6), (1.6, 3.5, 3.2), "leather")
    m.cube("satchel", (-6.1, -2.2, -0.6), (1.4, 1, 1.2), "herb")            # Kräuter schauen heraus
    m.cube("satchel", (-4.2, -11.8, -2.7), (8.4, 0.8, 0.1), "leather")      # Riemen über der Brust
    m.bone("sprig", "hood", pivot=(3.9, -7, -1), rotation=(0, 0, 25))
    m.cube("sprig", (-0.5, -3.5, -0.5), (1, 3.5, 1), "herb")
    m.cube("sprig", (-1.2, -4.5, -0.4), (2.4, 1.2, 0.8), "herb")

    # Kristallschleifer: Schleifbrille auf der Kapuze, Kristallanhänger
    m.bone("goggles", "head", pivot=(0, 0, 0))
    m.cube("goggles", (-4.2, -7.6, -4.0), (8.4, 1.2, 0.5), "leather")
    m.cube("goggles", (-2.9, -7.9, -4.3), (1.9, 1.8, 0.5), "lens")
    m.cube("goggles", (1.0, -7.9, -4.3), (1.9, 1.8, 0.5), "lens")
    m.bone("pendant", "body", pivot=(0, 0, 0))
    m.cube("pendant", (-0.6, -9.4, -2.85), (1.2, 2.2, 0.4), "crystal")

    # Seher: hoher, spitzer Hut über der Kapuze, Mondamulett
    m.bone("seer_hat", "hood", pivot=(0, -8.7, 0), rotation=(-8, 0, 0))
    m.cube("seer_hat", (-2.8, -2.6, -2.8), (5.6, 2.6, 5.6), "hood")
    m.cube("seer_hat", (-1.9, -5, -1.9), (3.8, 2.6, 3.8), "hood")
    m.cube("seer_hat", (-1, -7, -1), (2, 2.2, 2), "hood")
    m.cube("seer_hat", (-0.4, -8.2, -0.4), (0.8, 1.4, 0.8), "trim")
    m.bone("amulet", "body", pivot=(0, 0, 0))
    m.cube("amulet", (-1, -9.8, -2.9), (2, 2, 0.4), "moon")

    # Dämmerbauer: breiter Strohhut
    m.bone("straw_hat", "head", pivot=(0, -8, 0))
    m.cube("straw_hat", (-6, -0.6, -6), (12, 0.8, 12), "straw")
    m.cube("straw_hat", (-3.9, -2.8, -3.6), (7.8, 2.4, 7.2), "straw")

    # Wächter: Helm mit Kamm, Brustpanzer, Schulterstücke, Umhang, Speer
    m.bone("helmet", "head", pivot=(0, 0, 0))
    m.cube("helmet", (-4, -8.6, -3.75), (8, 5.2, 7.5), "metal")
    m.cube("helmet", (-0.6, -11.2, -3.4), (1.2, 2.8, 7), "plume")
    m.cube("helmet", (-0.5, -4.2, -4.15), (1, 2.2, 0.5), "metal")            # Nasenschutz
    m.bone("armor", "body", pivot=(0, 0, 0))
    m.cube("armor", (-4.3, -11.6, -2.85), (8.6, 7.6, 5.7), "metal")
    m.bone("cape", "body", pivot=(0, -11.5, 2.6), rotation=(6, 0, 0))
    m.cube("cape", (-4.5, 0, 0), (9, 21, 0.6), "cape")
    for side in (1, -1):
        name = "pauldron_l" if side > 0 else "pauldron_r"
        arm = "left_arm" if side > 0 else "right_arm"
        m.bone(name, arm, pivot=(0, -1, 0), rotation=(0, 0, -10 * side))
        m.cube(name, (-2.2, -1.2, -2.2), (4.4, 3, 4.4), "metal")
    m.bone("spear", "right_arm", pivot=(0, 10.5, -1.6))
    m.cube("spear", (-0.5, -16, -0.5), (1, 30, 1), "wood")
    m.cube("spear", (-1.2, -21, -0.4), (2.4, 5, 0.8), "metal")
    m.cube("spear", (-1.3, -16.4, -0.7), (2.6, 0.8, 1.4), "trim")
    return m


# =============================================================================================
# Malen
# =============================================================================================

def _hidden(ctx: Ctx) -> bool:
    """Gehört der Quader zu Zubehör, das dieser Beruf nicht trägt?"""
    variant = ctx.params.get("variant")
    bone = ctx.cube.bone
    while bone is not None:
        if bone.name in ALL_GEAR:
            return bone.name not in GEAR[variant]
        bone = bone.parent
    return False


def _cloth(ctx: Ctx, key: str, base: float = 0.42, folds: float = 0.12):
    x, y, z = ctx.world
    n = noise3(x * 0.5, y * 0.25, z * 0.5, 211)
    fold = math.sin(x * 1.3 + n * 3.0) * folds
    v = base + fold + (n - 0.5) * 0.14
    if ctx.face == "top":
        v += 0.06
    return pick(ctx.pal[key], v)


def _robe(ctx: Ctx):
    variant = ctx.params.get("variant")
    ry = rel_y(ctx)
    lower = ctx.cube.size[1] < 6.5
    col = _cloth(ctx, "robe", 0.4 - (0.06 if lower and ry > 0.5 else 0.0))
    # bestickter Saum unten und vorne eine Mittelnaht
    if lower and ctx.face != "top" and ctx.t >= ctx.h - 2:
        return pick(ctx.pal["trim"], 0.55 if ctx.t == ctx.h - 1 else 0.35)
    if ctx.face == "front" and abs(ctx.local[0]) < 0.6:
        col = pick(ctx.pal["trim"], 0.3)
    if variant == "seer" and hash01(ctx.u, ctx.v, 212) > 0.965:
        star = pick(ctx.pal["star"], 0.6)
        ctx.glow = star
        return star
    if variant == "moonsmith" and lower and hash01(ctx.u, ctx.v, 213) > 0.86:
        col = mix(col, pick(ctx.pal["soot"], 0.3), 0.6)                # Ruß am Saum
    return col


def _torso(ctx: Ctx):
    if _hidden(ctx):
        return None
    col = _cloth(ctx, "robe", 0.46)
    if ctx.face == "front":
        lx = ctx.local[0]
        # Kragen und Knopfleiste
        if ctx.t < 2:
            return pick(ctx.pal["trim"], 0.45)
        if abs(lx) < 0.5:
            return pick(ctx.pal["trim"], 0.3 if ctx.t % 3 else 0.7)
    if ctx.params.get("variant") == "seer" and hash01(ctx.u, ctx.v, 214) > 0.965:
        star = pick(ctx.pal["star"], 0.6)
        ctx.glow = star
        return star
    return col


def _mantle(ctx: Ctx):
    col = _cloth(ctx, "robe", 0.56, 0.06)
    if ctx.face != "top" and ctx.t == ctx.h - 1:
        return pick(ctx.pal["trim"], 0.6)
    if ctx.face == "front" and abs(ctx.local[0]) < 0.8:
        return pick(ctx.pal["trim"], 0.8)                               # Spange
    return col


def _sash(ctx: Ctx):
    v = 0.4 + (hash01(ctx.u, ctx.v, 215) - 0.5) * 0.12
    if ctx.face in ("front", "back", "west", "east") and (ctx.t == 0 or ctx.t == ctx.h - 1):
        v += 0.25
    return pick(ctx.pal["trim"], v)


def _hood(ctx: Ctx):
    if _hidden(ctx):
        return None
    col = _cloth(ctx, "robe", 0.38, 0.08)
    if ctx.params.get("variant") == "seer" and hash01(ctx.u, ctx.v, 216) > 0.96:
        star = pick(ctx.pal["star"], 0.7)
        ctx.glow = star
        return star
    # Saum an der vorderen Öffnung
    if ctx.face in ("west", "east"):
        front_edge = ctx.s == ctx.w - 1 if ctx.face == "west" else ctx.s == 0
        if front_edge:
            return pick(ctx.pal["trim"], 0.5)
    return col


def _trim(ctx: Ctx):
    if _hidden(ctx):
        return None
    return pick(ctx.pal["trim"], 0.5 + (hash01(ctx.u, ctx.v, 217) - 0.5) * 0.2)


def _sleeve(ctx: Ctx):
    return _cloth(ctx, "robe", 0.44, 0.08)


def _cuff(ctx: Ctx):
    col = _cloth(ctx, "robe", 0.5, 0.06)
    if ctx.face != "top" and ctx.t >= ctx.h - 1:
        return pick(ctx.pal["trim"], 0.55)
    return col


def _skin(ctx: Ctx):
    x, y, z = ctx.world
    n = noise3(x * 0.9, y * 0.9, z * 0.9, 218)
    return pick(ctx.pal["skin"], 0.55 + (n - 0.5) * 0.18 + (0.06 if ctx.face == "top" else 0.0))


def _face(ctx: Ctx):
    col = _skin(ctx)
    if ctx.face == "front":
        x, y = ctx.local[0], ctx.local[1]
        ax = abs(x)
        if -6.6 < y < -5.6 and 0.8 < ax < 3.2:
            return pick(ctx.pal["skin"], 0.32)                          # Brauen
        if -2.3 < y < -1.4 and ax < 1.2:
            return pick(ctx.pal["lip"], 0.65)                           # schmaler Mund
        if -4.2 < y < -2.4 and ax > 2.4:
            return pick(ctx.pal["skin"], 0.44)                          # hohe Wangen
        if y > -1.0:
            return pick(ctx.pal["skin"], 0.46)                          # Kinn
    if ctx.face == "top":
        return pick(ctx.pal["skin"], 0.35)                              # Scheitel (unter der Kapuze kaum sichtbar)
    return col


def _hand(ctx: Ctx):
    col = _skin(ctx)
    if ctx.face == "front" and ctx.t == ctx.h - 1 and ctx.s % 2 == 1:
        return pick(ctx.pal["skin"], 0.35)
    return col


def _eye(ctx: Ctx):
    # silbrig leuchtende Iris, zur Nase hin ein dunklerer Kern
    inner = (ctx.s == ctx.w - 1) if ctx.local[0] < 0 else (ctx.s == 0)
    col = pick(ctx.pal["eye"], 0.35 if inner else 0.85)
    ctx.glow = col
    return col


def _leg(ctx: Ctx):
    return pick(ctx.pal["robe"], 0.2 + hash01(ctx.u, ctx.v, 219) * 0.06)


def _shoe(ctx: Ctx):
    v = 0.4 + (0.15 if ctx.face == "top" else 0.0) + (hash01(ctx.u, ctx.v, 220) - 0.5) * 0.1
    return pick(ctx.pal["shoe"], v)


def _leather(ctx: Ctx):
    if _hidden(ctx):
        return None
    x, y, z = ctx.world
    n = noise3(x * 0.7, y * 0.7, z * 0.7, 221)
    v = 0.4 + (n - 0.5) * 0.25
    if ctx.face == "front" and (ctx.s == 0 or ctx.s == ctx.w - 1):
        v -= 0.15                                                         # Nähte am Rand
    return pick(ctx.pal["leather"], v)


def _metal(ctx: Ctx):
    if _hidden(ctx):
        return None
    ry = rel_y(ctx) if ctx.face not in ("top", "bottom") else 0.0
    v = 0.62 - ry * 0.25 + (0.15 if ctx.face == "top" else 0.0) + (hash01(ctx.u, ctx.v, 222) - 0.5) * 0.06
    if ctx.face in ("front", "west", "east") and ctx.t == 0:
        v += 0.2                                                          # Glanzkante
    return pick(ctx.pal["metal"], v)


def _wood(ctx: Ctx):
    if _hidden(ctx):
        return None
    x, y, z = ctx.world
    return pick(ctx.pal["wood"], 0.45 + math.sin(y * 1.4 + x) * 0.1)


def _herb(ctx: Ctx):
    if _hidden(ctx):
        return None
    v = 0.35 + hash01(ctx.u, ctx.v, 223) * 0.5
    if hash01(ctx.u, ctx.v, 224) > 0.75:
        return None if ctx.cube.size[1] > 3 else pick(ctx.pal["herb"], 0.9)
    return pick(ctx.pal["herb"], v)


def _lens(ctx: Ctx):
    if _hidden(ctx):
        return None
    col = pick(ctx.pal["crystal"], 0.75 if (ctx.s + ctx.t) % 3 else 0.95)
    ctx.glow = col
    return col


def _crystal(ctx: Ctx):
    if _hidden(ctx):
        return None
    col = pick(ctx.pal["crystal"], 0.5 + (1.0 - rel_y(ctx)) * 0.45)
    ctx.glow = col
    return col


def _moon(ctx: Ctx):
    if _hidden(ctx):
        return None
    if ctx.face == "front":
        # Mondsichel
        cx, cy = ctx.s - 1.0, ctx.t - 1.0
        if (cx * cx + cy * cy) <= 1.6 and not (ctx.s == 1 and ctx.t == 0):
            col = pick(ctx.pal["star"], 0.95)
            ctx.glow = col
            return col
    return pick(ctx.pal["trim"], 0.6)


def _straw(ctx: Ctx):
    if _hidden(ctx):
        return None
    x, y, z = ctx.world
    weave = (int(x * 2) + int(z * 2)) % 2
    v = 0.45 + weave * 0.15 + (hash01(ctx.u, ctx.v, 225) - 0.5) * 0.12
    if ctx.cube.size[1] > 1 and ctx.face != "top" and ctx.t == ctx.h - 1:
        return pick(ctx.pal["trim"], 0.3)                                 # Hutband
    return pick(ctx.pal["straw"], v)


def _plume(ctx: Ctx):
    if _hidden(ctx):
        return None
    return pick(ctx.pal["plume"], 0.4 + rel_y(ctx) * -0.2 + hash01(ctx.u, ctx.v, 226) * 0.3)


def _cape(ctx: Ctx):
    if _hidden(ctx):
        return None
    ry = rel_y(ctx)
    lx = ctx.local[0] - ctx.cube.origin[0]
    if ry > 0.94 and int(lx * 2) % 3 == 0:
        return None                                                       # gezackter Saum
    col = _cloth(ctx, "plume", 0.35, 0.1)
    if ctx.face == "back" and (ctx.s == 0 or ctx.s == ctx.w - 1):
        return pick(ctx.pal["trim"], 0.4)
    return col


def materials(variant: str) -> dict:
    return {
        "torso": _torso,
        "sash": _sash,
        "mantle": _mantle,
        "robe": _robe,
        "hood": _hood,
        "trim": _trim,
        "sleeve": _sleeve,
        "cuff": _cuff,
        "skin": _skin,
        "face": _face,
        "hand": _hand,
        "eye": _eye,
        "leg": _leg,
        "shoe": _shoe,
        "leather": _leather,
        "metal": _metal,
        "wood": _wood,
        "herb": _herb,
        "lens": _lens,
        "crystal": _crystal,
        "moon": _moon,
        "straw": _straw,
        "plume": _plume,
        "cape": _cape,
    }
