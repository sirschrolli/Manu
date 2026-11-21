"""
Die sechs Erzmagier: groß, in langen Roben mit hohem Stehkragen, Schulterumhang und reich bestickter Schärpe. Auf den
Schultern sitzen Kristalle ihres Elements, am Saum leuchten Runen, in der rechten Hand halten sie einen langen Stab mit
einer leuchtenden Kugel in einer Fassung aus Klauen.

Ein Modell für alle sechs – Kopfschmuck, Bart und Haare stecken in eigenen Knochen und werden je Element ein- oder
ausgeblendet (siehe GEAR); auf den Texturen der anderen bleibt dieses Zubehör durchsichtig:

  * Feuer (Ignatius): Kapuze, Flammenkrone, Bart
  * Eis (Isolde): Kristallkrone, langes silbernes Haar
  * Blitz (Fulgor): hoher spitzer Hut mit geknickter Spitze, langer Bart
  * Natur (Sylvana): Geweihkrone mit Blättern, langes Haar
  * Erde (Gorvan): Kapuze, Steinreif, Bart
  * Wasser (Marena): Muschelkrone mit Flossen, langes Haar

Knochen-Namen werden in MageModel (Java) für Animationen und das Zubehör verwendet.
"""
from __future__ import annotations

import math

from ..geometry import Model
from ..paint import Ctx, hash01, mix, noise3, pick, ramp, rel_y

ELEMENTS = ("fire", "ice", "lightning", "nature", "earth", "water")

# Welches Zubehör trägt welcher Magier (Knochen-Namen)?
GEAR = {
    "fire": {"hood", "flame_crown", "beard"},
    "ice": {"crystal_crown", "hair"},
    "lightning": {"hat", "beard", "beard_long"},
    "nature": {"antlers", "hair"},
    "earth": {"hood", "stone_circlet", "beard"},
    "water": {"shell_crown", "hair"},
}
ALL_GEAR = set().union(*GEAR.values())


def _pal(robe, robe2, trim, glow, skin=("#6a4a3a", "#e8c8a8"), hair=("#5a5a62", "#e6e6ee"), **extra) -> dict:
    pal = {
        "robe": ramp(*robe, steps=7),
        "robe2": ramp(*robe2, steps=7),
        "trim": ramp(*trim, steps=6),
        "glow": ramp(*glow, steps=5),
        "skin": ramp(*skin, steps=6),
        "hair": ramp(*hair, steps=6),
        "eye": ramp(glow[0], "#ffffff", steps=5),
        "wood": ramp("#1e140e", "#7a5a3e", steps=6),
        "leather": ramp("#1e1410", "#6a4a34", steps=6),
        "metal": ramp("#3a3440", "#e8e4f0", steps=7),
    }
    for key, value in extra.items():
        pal[key] = ramp(*value)
    return pal


PALETTES = {
    "fire": _pal(("#2a0606", "#c8341a"), ("#140808", "#6a1e12"), ("#6a3a0a", "#ffd86a"), ("#ff4a0a", "#fff0a0"),
                 hair=("#3a3a3a", "#d8d0c8"), flame=("#c81e04", "#fff4b0")),
    "ice": _pal(("#0a1a36", "#6aa8e0"), ("#0a1428", "#2a5a8a"), ("#5a6a82", "#f4faff"), ("#3ab0ff", "#f0ffff"),
                skin=("#7a6a74", "#f4e8ec"), hair=("#8a96b0", "#ffffff"), crystal=("#2a7ac8", "#f0fcff")),
    "lightning": _pal(("#120a2e", "#5a48c8"), ("#0a0820", "#2a2068"), ("#6a4a10", "#ffe27a"), ("#ffd21a", "#fffbe0"),
                      hair=("#5a5a66", "#f4f4fa"), star=("#c8b0ff", "#ffffff"), copper=("#5a2a10", "#f0a060")),
    "nature": _pal(("#0a200c", "#4ea83c"), ("#081808", "#1e5a1c"), ("#4a3a14", "#d8c070"), ("#6ae03a", "#f0ffc0"),
                   skin=("#6a4a32", "#e8c49c"), hair=("#3a1606", "#c86a2a"), leaf=("#1e5a14", "#9ae05a"),
                   antler=("#3a2a18", "#d8c8a0")),
    "earth": _pal(("#1e1208", "#a8763a"), ("#140c06", "#5a3a1a"), ("#5a3a10", "#f0c060"), ("#ff9a2a", "#fff0b0"),
                  hair=("#4a4038", "#c8bca8"), stone=("#3a3430", "#a49a8e")),
    "water": _pal(("#04202e", "#2a98b8"), ("#041622", "#0e4e68"), ("#4a6a72", "#e8fbff"), ("#28d0ff", "#e8ffff"),
                  skin=("#6a5a5a", "#ecdcd4"), hair=("#04121e", "#2a5a7a"), shell=("#8a5a6a", "#ffe8f0"),
                  fin=("#0a5a7a", "#7ae8ff")),
}


def build() -> Model:
    m = Model("mage", 256, 128)

    # ------------------------------------------------------------------ Rumpf mit Stehkragen und Schulterumhang
    m.bone("body", pivot=(0, 10, 0))
    m.cube("body", (-4, -12, -2.5), (8, 12, 5), "torso")
    m.cube("body", (-4.4, -1.8, -2.9), (8.8, 2.2, 5.8), "belt")
    m.cube("body", (-1.1, -2.1, -3.3), (2.2, 2.6, 0.6), "buckle")
    m.cube("body", (-5.2, -12.6, -3.2), (10.4, 3.4, 6.4), "mantle")
    m.cube("body", (-4.6, -16.2, 1.6), (9.2, 4.2, 1.2), "collar")              # hoher Stehkragen hinter dem Kopf
    m.cube("body", (-2.2, -11.5, -2.95), (4.4, 10.2, 0.5), "tabard")           # Schärpe vorn
    # Kristalle des Elements auf den Schultern
    for side in (1, -1):
        name = "shard_l" if side > 0 else "shard_r"
        m.bone(name, "body", pivot=(5.3 * side, -12.5, 0.2), rotation=(-6, 0, -32 * side))
        m.cube(name, (-1.1, -4.8, -1.1), (2.2, 4.8, 2.2), "shard")
        m.cube(name, (-0.6, -6.6, -0.6), (1.2, 1.8, 1.2), "shard")
        m.bone(name + "_small", name, pivot=(1.0 * side, 0.0, 0.0), rotation=(0, 0, -22 * side))
        m.cube(name + "_small", (-0.6, -3.0, -0.6), (1.2, 3.0, 1.2), "shard")

    # Umhang hinten bis zum Boden (schwingt beim Gehen und Schweben)
    m.bone("cape", "body", pivot=(0, -12.2, 3.0), rotation=(5, 0, 0))
    m.cube("cape", (-5.0, 0, 0), (10, 24.5, 0.8), "cape")

    # Robe bis zum Boden (schwingt), unten ein Saum mit leuchtenden Runen
    m.bone("skirt", "body", pivot=(0, 0, 0))
    m.cube("skirt", (-4.5, 0, -3.0), (9, 7, 6), "robe")
    m.cube("skirt", (-5.2, 7, -3.6), (10.4, 6, 7.2), "robe")
    m.cube("skirt", (-5.6, 13, -4.0), (11.2, 1.6, 8), "hem")
    m.cube("skirt", (-1.8, 0, -3.4), (3.6, 13.4, 0.5), "tabard")              # Schärpe fällt vorn herab

    # ------------------------------------------------------------------ Kopf
    m.bone("head", "body", pivot=(0, -12, 0))
    m.cube("head", (-3.5, -8, -3.5), (7, 8, 7), "face")
    m.cube("head", (-0.5, -4.6, -4.1), (1, 2.2, 0.7), "skin")                 # Nase
    m.cube("head", (-2.7, -5.4, -3.6), (1.7, 0.9, 0.1), "eye")
    m.cube("head", (1.0, -5.4, -3.6), (1.7, 0.9, 0.1), "eye")
    m.cube("head", (-3.6, -6.4, -3.62), (2.8, 0.6, 0.12), "brow")
    m.cube("head", (0.8, -6.4, -3.62), (2.8, 0.6, 0.12), "brow")

    # Bart (kurz) und langer, spitzer Bart
    m.bone("beard", "head", pivot=(0, -2.4, -3.5))
    m.cube("beard", (-3.3, 0, -0.7), (6.6, 2.8, 1.6), "beard")
    m.cube("beard", (-3.6, -2.2, -0.3), (1.0, 2.6, 1.2), "beard")            # Koteletten
    m.cube("beard", (2.6, -2.2, -0.3), (1.0, 2.6, 1.2), "beard")
    m.cube("beard", (-1.8, -1.4, -0.75), (3.6, 0.9, 0.5), "beard")           # Schnurrbart
    m.bone("beard_long", "beard", pivot=(0, 2.6, 0.1), rotation=(-6, 0, 0))
    m.cube("beard_long", (-2.4, 0, -0.6), (4.8, 4.2, 1.3), "beard")
    m.cube("beard_long", (-1.5, 4.0, -0.5), (3.0, 3.6, 1.1), "beard")
    m.cube("beard_long", (-0.7, 7.4, -0.4), (1.4, 2.6, 0.9), "beard")

    # langes Haar (hinten und zwei Strähnen vorn)
    m.bone("hair", "head", pivot=(0, 0, 0))
    m.cube("hair", (-3.8, -8.4, -3.8), (7.6, 2.2, 7.6), "hair")
    m.cube("hair", (-3.9, -6.4, 1.6), (7.8, 14, 2.6), "hair")
    for side in (1, -1):
        x = 2.6 if side > 0 else -3.9
        m.cube("hair", (x, -6.4, -3.7), (1.3, 11, 1.6), "hair")

    # Kapuze (offen nach vorn und unten)
    m.bone("hood", "head", pivot=(0, 0, 0))
    m.cube("hood", (-4.2, -8.9, -4.0), (8.4, 9.4, 8.2), "hood", faces=("top", "back", "west", "east"))
    m.cube("hood", (-4.2, -8.9, -4.1), (8.4, 1.0, 0.4), "trim")
    m.cube("hood", (-1.6, -9.8, 2.2), (3.2, 2.2, 3.0), "hood")

    # Feuer: Flammenkrone über der Kapuze
    m.bone("flame_crown", "head", pivot=(0, -9.0, 0))
    m.cube("flame_crown", (-4.4, -1.2, -4.4), (8.8, 1.2, 8.8), "gold")
    for i, (x, h) in enumerate(((-3.4, 2.6), (-1.6, 3.6), (0.0, 4.6), (1.6, 3.6), (3.4, 2.6))):
        m.cube("flame_crown", (x - 0.6, -1.2 - h, -4.6), (1.2, h, 0.8), "flame")

    # Eis: Kristallkrone (Zacken unterschiedlicher Höhe, leicht nach außen geneigt)
    m.bone("crystal_crown", "head", pivot=(0, -8.2, 0))
    m.cube("crystal_crown", (-4.0, -1.0, -4.0), (8.0, 1.0, 8.0), "silver")
    for i, (x, h, tilt) in enumerate(((-3.0, 3.2, -14), (-1.4, 4.4, -6), (0.2, 5.6, 0), (1.8, 4.4, 6), (3.2, 3.2, 14))):
        name = f"ice_spike_{i}"
        m.bone(name, "crystal_crown", pivot=(x, -1.0, -3.6), rotation=(-8, 0, tilt))
        m.cube(name, (-0.6, -h, -0.6), (1.2, h, 1.2), "crystal")

    # Blitz: hoher spitzer Hut mit breiter Krempe, Spitze zweimal geknickt
    m.bone("hat", "head", pivot=(0, -8.4, 0))
    m.cube("hat", (-6.6, -0.6, -6.6), (13.2, 1.0, 13.2), "hat")
    m.cube("hat", (-4.0, -4.4, -4.0), (8.0, 3.8, 8.0), "hat")
    m.cube("hat", (-4.1, -1.8, -4.1), (8.2, 1.2, 8.2), "hat_band")
    m.bone("hat_mid", "hat", pivot=(0, -4.4, 0), rotation=(-10, 0, 0))
    m.cube("hat_mid", (-2.9, -3.8, -2.9), (5.8, 3.8, 5.8), "hat")
    m.bone("hat_top", "hat_mid", pivot=(0, -3.8, 0), rotation=(-18, 0, 0))
    m.cube("hat_top", (-1.9, -3.4, -1.9), (3.8, 3.4, 3.8), "hat")
    m.bone("hat_tip", "hat_top", pivot=(0, -3.4, 0), rotation=(-28, 0, 0))
    m.cube("hat_tip", (-1.0, -3.0, -1.0), (2.0, 3.0, 2.0), "hat")
    m.cube("hat_tip", (-0.5, -4.2, -0.5), (1.0, 1.4, 1.0), "hat")

    # Natur: Geweih aus Ästen mit Blättern
    m.bone("antlers", "head", pivot=(0, -8.0, 0))
    m.cube("antlers", (-3.9, -0.9, -3.9), (7.8, 0.9, 7.8), "antler")
    for side in (1, -1):
        name = "antler_l" if side > 0 else "antler_r"
        m.bone(name, "antlers", pivot=(2.6 * side, -0.8, -1.0), rotation=(-10, 0, 24 * side))
        m.cube(name, (-0.6, -6.0, -0.6), (1.2, 6.0, 1.2), "antler")
        m.cube(name, ((0.4 if side > 0 else -2.8), -4.2, -0.5), (2.4, 1.0, 1.0), "antler")
        m.cube(name, ((0.2 if side > 0 else -2.0), -7.2, -0.5), (1.8, 1.0, 1.0), "antler")
        m.cube(name, ((-1.6 if side > 0 else -0.6), -3.0, -0.4), (2.2, 1.6, 0.8), "leaf")
        m.cube(name, ((0.8 if side > 0 else -2.6), -6.0, -0.4), (1.8, 1.4, 0.8), "leaf")

    # Erde: schwerer Steinreif über der Kapuze
    m.bone("stone_circlet", "head", pivot=(0, -8.4, 0))
    m.cube("stone_circlet", (-4.6, -1.6, -4.6), (9.2, 1.8, 9.2), "stone")
    m.cube("stone_circlet", (-1.2, -3.4, -4.9), (2.4, 2.4, 0.8), "stone")
    m.cube("stone_circlet", (-0.7, -2.9, -5.2), (1.4, 1.4, 0.4), "gem")

    # Wasser: Muschelkrone mit zwei Flossen
    m.bone("shell_crown", "head", pivot=(0, -8.2, 0))
    m.cube("shell_crown", (-4.0, -1.0, -4.0), (8.0, 1.0, 8.0), "silver")
    m.cube("shell_crown", (-2.2, -4.0, -4.3), (4.4, 3.2, 0.9), "shell")
    m.cube("shell_crown", (-0.6, -3.0, -4.6), (1.2, 1.2, 0.4), "gem")
    for side in (1, -1):
        name = "fin_l" if side > 0 else "fin_r"
        m.bone(name, "shell_crown", pivot=(3.9 * side, -1.0, 0.0), rotation=(0, 0, -20 * side))
        m.cube(name, ((0 if side > 0 else -0.6), -4.6, -2.4), (0.6, 4.6, 5.0), "fin")

    # ------------------------------------------------------------------ Arme mit weiten Ärmeln
    for side in (1, -1):
        name = "left_arm" if side > 0 else "right_arm"
        m.bone(name, "body", pivot=(5.4 * side, -11, 0))
        m.cube(name, (-1.6, -1, -1.6), (3.2, 8, 3.2), "sleeve")
        m.cube(name, (-2.5, 6.4, -2.5), (5.0, 4.4, 5.0), "cuff")
        m.cube(name, (-1.0, 10.2, -1.0), (2.0, 2.6, 2.0), "hand")

    # Stab in der rechten Hand: Schaft, Fassung aus vier Klauen, leuchtende Kugel
    m.bone("staff", "right_arm", pivot=(0, 11.5, -0.4))
    m.cube("staff", (-0.6, -19, -0.6), (1.2, 32, 1.2), "wood")
    m.cube("staff", (-1.0, -20.4, -1.0), (2.0, 1.6, 2.0), "metal")
    m.cube("staff", (-1.9, -25.2, -1.9), (3.8, 3.8, 3.8), "orb")
    for dx, dz in ((-1, -1), (1, -1), (-1, 1), (1, 1)):
        m.cube("staff", (dx * 1.7 - 0.35, -25.8, dz * 1.7 - 0.35), (0.7, 5.2, 0.7), "metal")
    m.cube("staff", (-0.9, 12.6, -0.9), (1.8, 1.4, 1.8), "metal")             # Spitze unten
    return m


# =============================================================================================
# Malen
# =============================================================================================

def _hidden(ctx: Ctx) -> bool:
    """Gehört der Quader zu Zubehör, das dieser Magier nicht trägt?"""
    variant = ctx.params.get("variant")
    bone = ctx.cube.bone
    while bone is not None:
        if bone.name in ALL_GEAR:
            return bone.name not in GEAR[variant]
        bone = bone.parent
    return False


def _cloth(ctx: Ctx, key: str, base: float = 0.42, folds: float = 0.12):
    x, y, z = ctx.world
    n = noise3(x * 0.5, y * 0.22, z * 0.5, 311)
    fold = math.sin(x * 1.2 + n * 3.0) * folds
    v = base + fold + (n - 0.5) * 0.14
    if ctx.face == "top":
        v += 0.06
    return pick(ctx.pal[key], v)


def _glow(ctx: Ctx, v: float):
    col = pick(ctx.pal["glow"], v)
    ctx.glow = col
    return col


def _sigil(variant: str, s: int, t: int, w: int, h: int) -> bool:
    """Zeichen des Elements auf der Schärpe (Feldgröße w x h, Pixel s, t)."""
    cx, cy = (w - 1) / 2, (h - 1) / 2
    x, y = s - cx, t - cy
    if variant == "fire":
        return (abs(x) <= 1 and -2 <= y <= 1) or (y == -3 and x == 0) or (y == 2 and abs(x) <= 1)
    if variant == "ice":
        return (x == 0 and abs(y) <= 2) or (y == 0 and abs(x) <= 2) or (abs(x) == abs(y) and abs(x) == 1)
    if variant == "lightning":
        return (x, y) in ((1, -3), (0, -2), (0, -1), (-1, 0), (0, 0), (1, 0), (0, 1), (-1, 2))
    if variant == "nature":
        return (x == 0 and y >= -1) or (y == -1 and abs(x) == 1) or (y == -2 and abs(x) <= 1 and x != 0)
    if variant == "earth":
        return (y == 2 and abs(x) <= 2) or (y == 0 and abs(x) <= 1) or (y == -2 and x == 0) or (abs(x) == 2 and y == 1) \
            or (abs(x) == 1 and y in (-1, 1))
    if variant == "water":
        return (y in (-1, 1) and (s + (0 if y < 0 else 1)) % 2 == 0)
    return False


def _robe(ctx: Ctx):
    variant = ctx.params.get("variant")
    ry = rel_y(ctx)
    col = _cloth(ctx, "robe", 0.4 - 0.08 * ry)
    # dunklere Falten nur an den Seiten (vorn liegt die Schärpe)
    if ctx.face in ("west", "east") and int(ctx.local[2] * 1.3 + 50) % 4 == 0:
        col = mix(col, pick(ctx.pal["robe2"], 0.4), 0.45)
    # Sterne auf der Robe des Blitzmagiers
    if variant == "lightning" and hash01(ctx.u, ctx.v, 312) > 0.972:
        star = pick(ctx.pal["star"], 0.7)
        ctx.glow = star
        return star
    return col


def _hem(ctx: Ctx):
    """Saum: Goldborte, darüber eine Reihe leuchtender Runen."""
    if ctx.face in ("top", "bottom"):
        return _cloth(ctx, "robe2", 0.35, 0.04)
    if ctx.t == 0 or ctx.t >= ctx.h - 1:
        return pick(ctx.pal["trim"], 0.6 if ctx.t == 0 else 0.35)
    if ctx.s % 4 == 1 or (ctx.s % 4 == 2 and hash01(ctx.s, ctx.cube.uv) > 0.5):
        return _glow(ctx, 0.55)
    return _cloth(ctx, "robe2", 0.38, 0.04)


def _torso(ctx: Ctx):
    col = _cloth(ctx, "robe", 0.46)
    if ctx.face == "front" and ctx.t < 2:
        return pick(ctx.pal["trim"], 0.5)
    return col


def _mantle(ctx: Ctx):
    col = _cloth(ctx, "robe2", 0.5, 0.06)
    if ctx.face != "top" and ctx.t >= ctx.h - 1:
        return pick(ctx.pal["trim"], 0.55)
    if ctx.face == "top" and (ctx.s in (0, ctx.w - 1) or ctx.t in (0, ctx.h - 1)):
        return pick(ctx.pal["trim"], 0.45)
    return col


def _collar(ctx: Ctx):
    col = _cloth(ctx, "robe2", 0.44, 0.05)
    if ctx.face in ("front", "back"):
        if ctx.t == 0:
            return pick(ctx.pal["trim"], 0.7)
        if ctx.face == "front" and ctx.t == 2 and ctx.s % 3 == 1:
            return _glow(ctx, 0.45)                 # leuchtende Punkte am Kragen (zum Kopf hin)
    return col


def _tabard(ctx: Ctx):
    variant = ctx.params.get("variant")
    if ctx.face not in ("front", "back"):
        return pick(ctx.pal["trim"], 0.4)
    if ctx.s == 0 or ctx.s == ctx.w - 1:
        return pick(ctx.pal["trim"], 0.62)
    base = pick(ctx.pal["robe2"], 0.32 + (hash01(ctx.u, ctx.v, 313) - 0.5) * 0.08)
    # Zeichen des Elements oben auf der Schärpe (Brust) bzw. auf dem herabfallenden Teil
    field_h = min(ctx.h, 7)
    if ctx.face == "front" and ctx.t < field_h and _sigil(variant, ctx.s, ctx.t, ctx.w, field_h):
        return _glow(ctx, 0.7)
    if ctx.face == "front" and ctx.t > field_h and ctx.t % 6 == 3 and ctx.s == ctx.w // 2:
        return pick(ctx.pal["trim"], 0.7)
    return base


def _belt(ctx: Ctx):
    v = 0.4 + (hash01(ctx.u, ctx.v, 314) - 0.5) * 0.1
    if ctx.face != "top" and (ctx.t == 0 or ctx.t == ctx.h - 1):
        return pick(ctx.pal["trim"], 0.5)
    return pick(ctx.pal["leather"], v)


def _buckle(ctx: Ctx):
    if ctx.face == "front" and 0 < ctx.s < ctx.w - 1 and 0 < ctx.t < ctx.h - 1:
        return _glow(ctx, 0.75)
    return pick(ctx.pal["trim"], 0.7)


def _shard(ctx: Ctx):
    variant = ctx.params.get("variant")
    v = 0.35 + (1.0 - rel_y(ctx)) * 0.55 if ctx.face not in ("top", "bottom") else 0.9
    if ctx.face in ("west", "back"):
        v -= 0.15
    key = {"ice": "crystal", "nature": "leaf", "earth": "stone", "water": "fin"}.get(variant, "glow")
    col = pick(ctx.pal[key] if key in ctx.pal else ctx.pal["glow"], v)
    if variant in ("fire", "lightning", "ice", "water") or v > 0.7:
        ctx.glow = mix(col, pick(ctx.pal["glow"], 0.7), 0.4)
    return col


def _skin(ctx: Ctx):
    x, y, z = ctx.world
    n = noise3(x * 0.9, y * 0.9, z * 0.9, 315)
    return pick(ctx.pal["skin"], 0.56 + (n - 0.5) * 0.16 + (0.05 if ctx.face == "top" else 0.0))


def _face(ctx: Ctx):
    variant = ctx.params.get("variant")
    col = _skin(ctx)
    if ctx.face == "front":
        x, y = ctx.local[0], ctx.local[1]
        ax = abs(x)
        if -3.2 < y < -2.4 and ax > 2.0:
            return pick(ctx.pal["skin"], 0.38)                              # Falten unter den Augen
        if -2.0 < y < -1.3 and ax < 1.3:
            return pick(ctx.pal["skin"], 0.3)                               # Mund
        if y > -1.0:
            return pick(ctx.pal["skin"], 0.45)
        if -7.6 < y < -6.9 and ax < 2.5 and variant in ("fire", "lightning", "earth"):
            return pick(ctx.pal["skin"], 0.42)                              # Stirnfalte der alten Magier
    if ctx.face == "top":
        return pick(ctx.pal["hair"], 0.4)
    if ctx.face == "back" or (ctx.face in ("west", "east") and ctx.local[2] > 0.5):
        return pick(ctx.pal["hair"], 0.45 + (hash01(ctx.u, ctx.v, 316) - 0.5) * 0.12)
    return col


def _eye(ctx: Ctx):
    inner = (ctx.s == ctx.w - 1) if ctx.local[0] < 0 else (ctx.s == 0)
    col = pick(ctx.pal["eye"], 0.45 if inner else 0.9)
    ctx.glow = col
    return col


def _brow(ctx: Ctx):
    if _hidden_brow(ctx):
        return None
    return pick(ctx.pal["hair"], 0.55)


def _hidden_brow(ctx: Ctx) -> bool:
    return False


def _beard(ctx: Ctx):
    if _hidden(ctx):
        return None
    x, y, z = ctx.world
    strand = (int(x * 2.2) + int(z)) % 3
    v = 0.6 + strand * 0.1 + (hash01(ctx.u, ctx.v, 317) - 0.5) * 0.1
    if ctx.face == "bottom":
        v -= 0.15
    return pick(ctx.pal["hair"], v)


def _hair(ctx: Ctx):
    if _hidden(ctx):
        return None
    x, y, z = ctx.world
    strand = (int(x * 1.8) + int(z * 0.5)) % 4
    v = 0.45 + strand * 0.08 + (hash01(ctx.u, ctx.v, 318) - 0.5) * 0.12
    variant = ctx.params.get("variant")
    if variant == "nature" and hash01(ctx.u, ctx.v, 319) > 0.93:
        return pick(ctx.pal["leaf"], 0.6)                                  # Blätter im Haar
    if variant == "ice" and hash01(ctx.u, ctx.v, 320) > 0.96:
        col = pick(ctx.pal["crystal"], 0.9)
        ctx.glow = col
        return col
    if ctx.cube.size[1] > 10 and rel_y(ctx) > 0.9 and ctx.face != "top" and hash01(ctx.s, 321) > 0.5:
        return None                                                          # ausgefranste Spitzen
    return pick(ctx.pal["hair"], v)


def _hood(ctx: Ctx):
    if _hidden(ctx):
        return None
    col = _cloth(ctx, "robe2", 0.42, 0.08)
    if ctx.face in ("west", "east"):
        front_edge = ctx.s == ctx.w - 1 if ctx.face == "west" else ctx.s == 0
        if front_edge:
            return pick(ctx.pal["trim"], 0.5)
    return col


def _trim(ctx: Ctx):
    if _hidden(ctx):
        return None
    return pick(ctx.pal["trim"], 0.5 + (hash01(ctx.u, ctx.v, 322) - 0.5) * 0.2)


def _gold(ctx: Ctx):
    if _hidden(ctx):
        return None
    v = 0.55 + (0.2 if ctx.face == "top" else 0.0) - rel_y(ctx) * 0.2 + (hash01(ctx.u, ctx.v, 323) - 0.5) * 0.08
    return pick(ctx.pal["trim"], v)


def _silver(ctx: Ctx):
    if _hidden(ctx):
        return None
    v = 0.6 + (0.2 if ctx.face == "top" else 0.0) + (hash01(ctx.u, ctx.v, 324) - 0.5) * 0.1
    return pick(ctx.pal["metal"], v)


def _flame(ctx: Ctx):
    if _hidden(ctx):
        return None
    v = 0.35 + (1.0 - rel_y(ctx)) * 0.6
    col = pick(ctx.pal["flame"], v)
    ctx.glow = col
    return col


def _crystal(ctx: Ctx):
    if _hidden(ctx):
        return None
    v = 0.4 + (1.0 - rel_y(ctx)) * 0.55 - (0.12 if ctx.face in ("west", "back") else 0.0)
    col = pick(ctx.pal["crystal"], v)
    ctx.glow = mix(col, (255, 255, 255, 255), 0.2)
    return col


def _hat(ctx: Ctx):
    if _hidden(ctx):
        return None
    col = _cloth(ctx, "robe2", 0.45, 0.06)
    if hash01(ctx.u, ctx.v, 325) > 0.955:
        star = pick(ctx.pal["star"], 0.8)
        ctx.glow = star
        return star
    return col


def _hat_band(ctx: Ctx):
    if _hidden(ctx):
        return None
    if ctx.face == "front" and abs(ctx.local[0]) < 0.8:
        return _glow(ctx, 0.8)
    return pick(ctx.pal["trim"], 0.55)


def _antler(ctx: Ctx):
    if _hidden(ctx):
        return None
    v = 0.45 + (1.0 - rel_y(ctx)) * 0.3 + (hash01(ctx.u, ctx.v, 326) - 0.5) * 0.1
    return pick(ctx.pal["antler"], v)


def _leaf(ctx: Ctx):
    if _hidden(ctx):
        return None
    v = 0.4 + hash01(ctx.u, ctx.v, 327) * 0.45
    if hash01(ctx.u, ctx.v, 328) > 0.85:
        col = pick(ctx.pal["glow"], 0.6)
        ctx.glow = col
        return col
    return pick(ctx.pal["leaf"], v)


def _stone(ctx: Ctx):
    if _hidden(ctx):
        return None
    x, y, z = ctx.world
    n = noise3(x * 0.8, y * 0.8, z * 0.8, 329)
    v = 0.4 + (n - 0.5) * 0.35 + (0.12 if ctx.face == "top" else 0.0)
    if hash01(ctx.u, ctx.v, 330) > 0.93:
        col = pick(ctx.pal["glow"], 0.5)
        ctx.glow = col
        return col                                                           # Bernstein-Adern
    return pick(ctx.pal["stone"], v)


def _gem(ctx: Ctx):
    if _hidden(ctx):
        return None
    return _glow(ctx, 0.8)


def _shell(ctx: Ctx):
    if _hidden(ctx):
        return None
    ridge = int(ctx.local[0] * 1.6 + 20) % 2
    v = 0.5 + ridge * 0.2 - rel_y(ctx) * 0.15
    return pick(ctx.pal["shell"], v)


def _fin(ctx: Ctx):
    if _hidden(ctx):
        return None
    v = 0.4 + (1.0 - rel_y(ctx)) * 0.4
    if int(ctx.local[2] * 1.4 + 20) % 3 == 0:
        v -= 0.15
    col = pick(ctx.pal["fin"], v)
    if v > 0.7:
        ctx.glow = col
    return col


def _cape(ctx: Ctx):
    variant = ctx.params.get("variant")
    ry = rel_y(ctx)
    lx = ctx.local[0] - ctx.cube.origin[0]
    if ry > 0.96 and int(lx * 2) % 3 == 0:
        return None                                                          # gezackter Saum
    col = _cloth(ctx, "robe2", 0.36 + 0.08 * (1 - ry), 0.1)
    if ctx.face in ("back", "front"):
        if ctx.s == 0 or ctx.s == ctx.w - 1:
            return pick(ctx.pal["trim"], 0.5)
        if ctx.face == "back" and ctx.h - 4 <= ctx.t <= ctx.h - 3 and ctx.s % 3 == 1:
            return _glow(ctx, 0.5)                                           # Runenreihe unten am Umhang
        if ctx.face == "back" and variant == "lightning" and hash01(ctx.u, ctx.v, 333) > 0.965:
            star = pick(ctx.pal["star"], 0.75)
            ctx.glow = star
            return star
    return col


def _sleeve(ctx: Ctx):
    return _cloth(ctx, "robe", 0.44, 0.08)


def _cuff(ctx: Ctx):
    col = _cloth(ctx, "robe2", 0.48, 0.05)
    if ctx.face != "top" and ctx.t >= ctx.h - 1:
        return pick(ctx.pal["trim"], 0.6)
    if ctx.face not in ("top", "bottom") and ctx.t == ctx.h - 2 and ctx.s % 3 == 1:
        return _glow(ctx, 0.5)
    return col


def _hand(ctx: Ctx):
    col = _skin(ctx)
    if ctx.face == "front" and ctx.t == ctx.h - 1 and ctx.s % 2 == 1:
        return pick(ctx.pal["skin"], 0.38)
    return col


def _wood(ctx: Ctx):
    x, y, z = ctx.world
    v = 0.42 + math.sin(y * 1.1 + x * 2.0) * 0.1 + (hash01(ctx.u, ctx.v, 331) - 0.5) * 0.08
    if int(y) % 9 == 0:
        return pick(ctx.pal["trim"], 0.5)                                   # Goldringe am Schaft
    return pick(ctx.pal["wood"], v)


def _metal(ctx: Ctx):
    v = 0.55 - rel_y(ctx) * 0.2 + (0.18 if ctx.face == "top" else 0.0) + (hash01(ctx.u, ctx.v, 332) - 0.5) * 0.06
    return pick(ctx.pal["trim"], v)


def _orb(ctx: Ctx):
    s, t = ctx.s, ctx.t
    cx, cy = (ctx.w - 1) / 2, (ctx.h - 1) / 2
    d = math.hypot(s - cx, t - cy) / max(cx, 1)
    v = 0.95 - d * 0.5
    if ctx.face in ("west", "back"):
        v -= 0.1
    return _glow(ctx, v)


def materials(variant: str) -> dict:
    return {
        "torso": _torso, "belt": _belt, "buckle": _buckle, "mantle": _mantle, "collar": _collar, "tabard": _tabard,
        "shard": _shard, "robe": _robe, "hem": _hem, "face": _face, "skin": _skin, "eye": _eye, "brow": _brow,
        "beard": _beard, "hair": _hair, "hood": _hood, "trim": _trim, "gold": _gold, "silver": _silver,
        "flame": _flame, "crystal": _crystal, "hat": _hat, "hat_band": _hat_band, "antler": _antler, "leaf": _leaf,
        "stone": _stone, "gem": _gem, "shell": _shell, "fin": _fin, "sleeve": _sleeve, "cuff": _cuff, "hand": _hand,
        "wood": _wood, "metal": _metal, "orb": _orb, "cape": _cape,
    }
