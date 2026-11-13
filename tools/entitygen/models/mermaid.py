"""
Meerjungfrau (seit 0.93): das Meervolk des Urmeers. Oben ein Mensch – großes Gesicht mit leuchtenden Augen, langes,
fließendes Haar, Flossenohren, ein Oberteil aus zwei Muscheln, eine Perlenkette –, unten ein langer, geschuppter
Fischschwanz in vier Gliedern mit einer breiten Schwanzflosse (zwei Lappen), Schleierflossen an den Hüften, einem Kamm
auf dem Schwanzrücken und kleinen Flossen an den Unterarmen.

Aufrecht (wie wassertretend) steht sie im Modell; beim Schwimmen kippt das Java-Modell den Knochen "swim" nach vorn, und
der Schwanz schlägt wie bei einem Delfin auf und ab.

Ein Modell für alle Berufe und die Tritonwachen – das Zubehör (Stirnband und Perlennetz der Perlentaucherin, Korallenkrone
und Blüte der Korallengärtnerin, Muschelkamm und Werkzeuggürtel der Muschelbauerin, Seestern und Muschelhorn der
Gezeitensängerin, Helm, Panzer, Schulterstücke und Dreizack der Tritonwache) steckt in eigenen Knochen und wird im
Spiel je Beruf ein- oder ausgeblendet. Die Texturen gehören zu den sechs Gestalten (Haar-, Haut- und Schwanzfarben):
Koralle, Tang, Perle, Lagune, Gold und Tiefsee (mit leuchtenden Flecken).
"""
from __future__ import annotations

import math

from ..geometry import Model
from ..paint import Ctx, hash01, mix, noise3, pick, ramp, rel_y

# Zubehör, das alle Gestalten tragen (gleiche Farben)
_GEAR = {
    "gold": ramp("#6a3a0a", "#fff0a0", steps=7, hue_shift=0.02),
    "pearl": ramp("#a89ab8", "#ffffff", steps=6, hue_shift=0.0),
    "coral": ramp("#7a0e2a", "#ff9ab0", steps=7),
    "leather": ramp("#2a1a10", "#9a6a40", steps=6),
    "net": ramp("#3a3020", "#c8b890", steps=5),
    "bronze": ramp("#2a3a30", "#e8c87a", steps=7, hue_shift=-0.02),
    "patina": ramp("#0e4a44", "#7ae0c8", steps=6),
    "shaft": ramp("#2a1a10", "#a87a4a", steps=6),
    "star": ramp("#8a2a10", "#ffc070", steps=6),
    "conch": ramp("#8a5a4a", "#fff0e0", steps=7),
    "lip": ramp("#8a2a3a", "#ff9aa8", steps=5),
    "lash": ramp("#140a14", "#4a3040", steps=4),
    "white": ramp("#c8d0e0", "#ffffff", steps=4),
}


def _look(skin, hair, tail, fin, shell, iris, glow=False) -> dict:
    pal = dict(_GEAR)
    pal["skin"] = ramp(*skin, steps=7, hue_shift=-0.02)
    pal["hair"] = ramp(*hair, steps=7)
    pal["tail"] = ramp(*tail, steps=8)
    pal["fin"] = ramp(*fin, steps=7)
    pal["shell"] = ramp(*shell, steps=6)
    pal["iris"] = ramp(*iris, steps=5)
    pal["glow"] = [1] if glow else []
    return pal


PALETTES = {
    "coral": _look(("#9a5a4a", "#ffe6d8"), ("#5a0e14", "#ff8a5a"), ("#7a0a30", "#ffa88a"), ("#e8486a", "#fff0ea"),
                   ("#c86a7a", "#fff0f4"), ("#0a4a5a", "#7af0e8")),
    "kelp": _look(("#7a4a30", "#f4c8a0"), ("#06281a", "#6ad09a"), ("#06362a", "#7ae0b0"), ("#2a8a6a", "#d8fff0"),
                  ("#8a7a3a", "#f8eaa8"), ("#6a3a0a", "#ffd070")),
    "pearl": _look(("#9a7470", "#ffeee6"), ("#3a3a5e", "#ffffff"), ("#32206a", "#eadcff"), ("#8a6ad8", "#ffffff"),
                   ("#a86aa8", "#ffe4f4"), ("#3a1a6a", "#d8b8ff")),
    "lagoon": _look(("#4a2a1a", "#c8906a"), ("#06304a", "#7af0f0"), ("#062a6a", "#8ae8ff"), ("#2a90d0", "#e4ffff"),
                    ("#c85a2a", "#ffd8a8"), ("#0a2a7a", "#8ac8ff")),
    "gold": _look(("#a8744a", "#ffe8cc"), ("#6a3a06", "#fff0a0"), ("#7a3206", "#ffe080"), ("#e88a20", "#fff6cc"),
                  ("#1a7a7a", "#a8fff0"), ("#1a5a1a", "#9af07a")),
    "abyss": _look(("#6a7a9a", "#e8f0ff"), ("#040a24", "#4a6ad0"), ("#040c24", "#3a6ad0"), ("#14306a", "#7af0ff"),
                   ("#2a1a4a", "#a88ad8"), ("#0a5a6a", "#9afff4"), glow=True),
}

LOOKS = tuple(PALETTES)

# Welches Zubehör trägt welcher Beruf (Knochen-Namen)? Alles andere aus ALL_GEAR wird ausgeblendet.
_MERMAID = {"top", "necklace", "locks_l", "locks_r", "hair_back", "bracelet_l", "bracelet_r"}
GEAR = {
    "pearl_diver": _MERMAID | {"diver_band", "pearl_net"},
    "coral_gardener": _MERMAID | {"coral_crown", "flower"},
    "shell_builder": _MERMAID | {"shell_comb", "tool_belt"},
    "tide_singer": _MERMAID | {"starfish", "conch"},
    "guard": {"helmet", "armor", "pauldron_l", "pauldron_r", "trident", "bracelet_l", "bracelet_r"},
}
ALL_GEAR = set().union(*GEAR.values())

HIP_Y = 6.0


def build() -> Model:
    m = Model("mermaid", 128, 128)
    m.tex_res = 2
    # aufrecht: Kopf oben, die Schwanzflosse reicht bis zum Boden (y = 24); der Knochen "swim" dreht beim Schwimmen
    m.bone("swim", pivot=(0, HIP_Y, 0))

    # ------------------------------------------------------------------ Oberkörper
    m.bone("torso", "swim", pivot=(0, 0, 0))
    m.cube("torso", (-3.0, -9.5, -1.7), (6.0, 5.0, 3.4), "chest")
    m.cube("torso", (-2.4, -4.5, -1.4), (4.8, 4.5, 2.8), "belly")

    # Oberteil aus zwei Muscheln an einem Band
    m.bone("top", "torso", pivot=(0, 0, 0))
    m.cube("top", (-2.95, -8.6, -2.25), (2.75, 2.6, 0.7), "shell")
    m.cube("top", (0.2, -8.6, -2.25), (2.75, 2.6, 0.7), "shell")
    m.cube("top", (-3.15, -7.6, -1.9), (6.3, 0.6, 3.8), "band", faces=("back", "west", "east"))
    # Perlenkette mit großer Perle
    m.bone("necklace", "torso", pivot=(0, 0, 0))
    m.cube("necklace", (-2.1, -9.6, -1.95), (4.2, 0.5, 0.3), "pearls")
    m.cube("necklace", (-0.55, -9.3, -2.15), (1.1, 1.1, 0.6), "pearl_big")

    # ------------------------------------------------------------------ Kopf
    m.bone("head", "torso", pivot=(0, -9.5, 0))
    m.cube("head", (-3.4, -6.8, -3.3), (6.8, 6.8, 6.6), "face")
    m.cube("head", (-3.65, -7.3, -3.5), (7.3, 1.3, 7.1), "hair")                     # Scheitel
    m.cube("head", (-3.6, -6.4, -3.65), (7.2, 1.7, 0.5), "bangs")                    # Stirnfransen
    m.cube("head", (-3.7, -6.4, -2.6), (0.6, 4.4, 5.9), "hair_side")                 # Haar über den Schläfen
    m.cube("head", (3.1, -6.4, -2.6), (0.6, 4.4, 5.9), "hair_side")
    for side, sx in (("l", 1), ("r", -1)):
        # Flossenohren
        ear = f"ear_{side}"
        m.bone(ear, "head", pivot=(3.3 * sx, -3.6, 0.2), rotation=(0, 28 * sx, 0))
        m.cube(ear, (0, -1.6, -0.2), (0, 3.2, 2.6), "ear_fin")
        # lange Strähnen vor den Schultern
        lock = f"locks_{side}"
        m.bone(lock, "head", pivot=(3.15 * sx, -5.6, -1.4), rotation=(-6, 0, -4 * sx))
        m.cube(lock, (-0.55, 0, -0.9), (1.1, 9.0, 1.9), "lock")
    # langes Haar über den Rücken, in zwei Gliedern (fließt im Wasser)
    m.bone("hair_back", "head", pivot=(0, -6.2, 3.0), rotation=(8, 0, 0))
    m.cube("hair_back", (-3.6, 0, -0.5), (7.2, 9.0, 1.4), "hair_back")
    m.bone("hair_tip", "hair_back", pivot=(0, 9.0, 0), rotation=(10, 0, 0))
    m.cube("hair_tip", (-3.2, 0, -0.4), (6.4, 7.5, 1.1), "hair_tip")

    # ------------------------------------------------------------------ Arme
    for side, sx in (("l", 1), ("r", -1)):
        arm = "left_arm" if sx > 0 else "right_arm"
        m.bone(arm, "torso", pivot=(3.75 * sx, -8.9, 0))
        m.cube(arm, (-0.9, -0.6, -0.9), (1.8, 8.0, 1.8), "arm")
        m.cube(arm, (-0.95, 7.2, -1.0), (1.9, 1.9, 2.0), "hand")
        m.cube(arm, (0.9 if sx > 0 else -0.9, 3.2, -0.2), (0, 3.4, 2.0), "arm_fin")      # Flosse am Unterarm
        bracelet = f"bracelet_{side}"
        m.bone(bracelet, arm, pivot=(0, 0, 0))
        m.cube(bracelet, (-1.05, 5.6, -1.05), (2.1, 0.7, 2.1), "gold")

    # ------------------------------------------------------------------ Schwanz
    m.bone("tail1", "swim", pivot=(0, 0, 0))
    m.cube("tail1", (-2.6, 0, -1.55), (5.2, 4.0, 3.1), "hips")
    for side, sx in (("l", 1), ("r", -1)):
        veil = f"veil_{side}"
        m.bone(veil, "tail1", pivot=(2.55 * sx, 0.6, 0.2), rotation=(0, 10 * sx, -12 * sx))
        m.cube(veil, (0, 0, -1.6), (0, 7.5, 3.6), "veil")
    m.bone("tail2", "tail1", pivot=(0, 4.0, 0), rotation=(6, 0, 0))
    m.cube("tail2", (-2.2, 0, -1.35), (4.4, 3.6, 2.7), "scales")
    m.cube("tail2", (0, 0.2, 1.2), (0, 3.4, 1.9), "frill")
    m.bone("tail3", "tail2", pivot=(0, 3.6, 0), rotation=(10, 0, 0))
    m.cube("tail3", (-1.75, 0, -1.1), (3.5, 3.4, 2.2), "scales")
    m.cube("tail3", (0, 0.2, 0.95), (0, 3.2, 1.7), "frill")
    m.bone("tail4", "tail3", pivot=(0, 3.4, 0), rotation=(12, 0, 0))
    m.cube("tail4", (-1.25, 0, -0.85), (2.5, 3.0, 1.7), "scales")
    m.bone("fluke", "tail4", pivot=(0, 2.8, 0.05), rotation=(8, 0, 0))
    m.cube("fluke", (-0.9, -0.4, -0.6), (1.8, 1.6, 1.2), "scales")
    for side, sx in (("l", 1), ("r", -1)):
        lobe = f"fluke_{side}"
        m.bone(lobe, "fluke", pivot=(0.4 * sx, 0.2, 0), rotation=(0, 0, 28 * sx))
        m.cube(lobe, (0 if sx > 0 else -7.5, 0, 0), (7.5, 5.6, 0), "fluke")

    # ------------------------------------------------------------------ Zubehör der Berufe
    # Perlentaucherin: Stirnband mit Perle, Netzbeutel voller Perlen an der Hüfte
    m.bone("diver_band", "head", pivot=(0, 0, 0))
    m.cube("diver_band", (-3.8, -7.15, -3.8), (7.6, 0.7, 7.6), "band_head", faces=("front", "west", "east", "back"))
    m.cube("diver_band", (-0.6, -7.45, -4.1), (1.2, 1.2, 0.6), "pearl_big")
    m.bone("pearl_net", "tail1", pivot=(-2.6, 0.4, -0.4), rotation=(0, 0, 8))
    m.cube("pearl_net", (-1.6, 0, -1.2), (1.6, 3.2, 2.4), "net")
    m.cube("pearl_net", (-1.4, -0.5, -0.6), (1.2, 0.5, 1.2), "leather")

    # Korallengärtnerin: Krone aus Korallenzweigen, eine Seeanemone hinter dem Ohr
    m.bone("coral_crown", "head", pivot=(0, -7.3, -1.0))
    for i, (x, rz, h) in enumerate(((-2.2, -24, 2.6), (-0.6, -6, 3.4), (1.0, 10, 3.0), (2.4, 26, 2.4))):
        bone = f"coral_{i}"
        m.bone(bone, "coral_crown", pivot=(x, 0, 0), rotation=(-8, 0, rz))
        m.cube(bone, (-0.4, -h, -0.4), (0.8, h, 0.8), "coral")
        m.cube(bone, (-0.95 if i % 2 else 0.15, -h * 0.65, -0.35), (0.8, 0.7, 0.7), "coral")
    m.bone("flower", "head", pivot=(-3.7, -5.0, 0.4))
    m.cube("flower", (-0.9, -1.2, -1.2), (1.2, 2.4, 2.4), "anemone")

    # Muschelbauerin: Kamm aus einer Jakobsmuschel im Haar, Gürtel mit Muschelhammer
    m.bone("shell_comb", "head", pivot=(2.8, -6.6, 1.4), rotation=(0, 0, 22))
    m.cube("shell_comb", (-1.4, -2.2, -0.3), (2.8, 2.4, 0.6), "scallop")
    m.bone("tool_belt", "tail1", pivot=(0, 0, 0))
    m.cube("tool_belt", (-2.75, 0.2, -1.7), (5.5, 0.8, 3.4), "leather")
    m.cube("tool_belt", (2.6, 0.0, -1.0), (0.6, 3.2, 0.6), "shaft")
    m.cube("tool_belt", (2.3, 2.9, -1.6), (1.2, 1.2, 1.8), "conch")

    # Gezeitensängerin: Seestern im Haar, Muschelhorn an der Hüfte
    m.bone("starfish", "head", pivot=(-2.7, -6.5, -2.4), rotation=(0, -20, -14))
    m.cube("starfish", (-1.6, -1.6, -0.15), (3.2, 3.2, 0.3), "star")
    m.bone("conch", "tail1", pivot=(2.6, 0.6, -0.2), rotation=(0, 0, -20))
    m.cube("conch", (0, 0, -1.0), (1.5, 2.6, 2.0), "conch")
    m.cube("conch", (0.2, 2.6, -0.6), (1.0, 1.4, 1.2), "conch")
    m.cube("conch", (0.3, -0.7, -0.6), (0.9, 0.7, 1.2), "conch")

    # Tritonwache: Helm mit Flossenkamm, Brustpanzer, Schulterstücke, Dreizack
    m.bone("helmet", "head", pivot=(0, 0, 0))
    # offen nach vorn: nur Kappe, Seiten und Nacken, vorn ein Stirnreif
    m.cube("helmet", (-3.75, -7.6, -3.8), (7.5, 4.4, 7.6), "helmet", faces=("top", "back", "west", "east"))
    m.cube("helmet", (-3.75, -7.6, -3.85), (7.5, 1.6, 0.4), "helmet")
    m.cube("helmet", (-0.45, -10.0, -3.0), (0.9, 2.6, 7.0), "crest")
    m.cube("helmet", (-0.5, -3.4, -4.2), (1.0, 2.0, 0.5), "helmet")                  # Nasenschutz
    m.cube("helmet", (-3.85, -3.4, -2.6), (0.6, 2.6, 2.8), "helmet")                 # Wangenschutz
    m.cube("helmet", (3.25, -3.4, -2.6), (0.6, 2.6, 2.8), "helmet")
    m.bone("armor", "torso", pivot=(0, 0, 0))
    m.cube("armor", (-3.3, -9.8, -2.05), (6.6, 6.3, 4.1), "armor")
    m.cube("armor", (-2.9, -3.6, -1.75), (5.8, 1.0, 3.5), "leather")
    for side, sx in (("l", 1), ("r", -1)):
        arm = "left_arm" if sx > 0 else "right_arm"
        p = f"pauldron_{side}"
        m.bone(p, arm, pivot=(0, -0.6, 0), rotation=(0, 0, -12 * sx))
        m.cube(p, (-1.5, -1.3, -1.4), (3.0, 2.3, 2.8), "armor")
    m.bone("trident", "right_arm", pivot=(0, 8.2, -0.1))
    m.cube("trident", (-0.4, -15.0, -0.4), (0.8, 27.0, 0.8), "shaft")
    m.cube("trident", (-2.4, -15.8, -0.45), (4.8, 0.9, 0.9), "trident")
    m.cube("trident", (-0.45, -20.5, -0.45), (0.9, 4.8, 0.9), "trident")
    m.cube("trident", (-2.4, -18.8, -0.4), (0.8, 3.1, 0.8), "trident")
    m.cube("trident", (1.6, -18.8, -0.4), (0.8, 3.1, 0.8), "trident")
    return m


# =============================================================================================
# Bemalen
# =============================================================================================

_LIT = {"top": 0.08, "front": 0.03, "west": 0.0, "east": -0.03, "back": -0.06, "bottom": -0.14}


def _gear_bone(ctx: Ctx) -> str | None:
    bone = ctx.cube.bone
    while bone is not None:
        if bone.name in ALL_GEAR:
            return bone.name
        bone = bone.parent
    return None


def _glowing(ctx: Ctx) -> bool:
    return bool(ctx.pal.get("glow"))


def _skin(ctx: Ctx, base: float = 0.6):
    x, y, z = ctx.world
    n = noise3(x * 0.8, y * 0.8, z * 0.8, 931)
    return pick(ctx.pal["skin"], base + (n - 0.5) * 0.08 + _LIT[ctx.face])


def _chest(ctx: Ctx):
    col = _skin(ctx)
    if ctx.face == "front":
        if ctx.t == 0:
            return _skin(ctx, 0.66)                                         # Schlüsselbein im Licht
        if ctx.t == 1 and (ctx.s in (2, 3, ctx.w - 3, ctx.w - 4)):
            return _skin(ctx, 0.5)
    if ctx.face in ("west", "east") and ctx.t <= 1:
        return _skin(ctx, 0.66)                                             # Schultern
    return col


def _belly(ctx: Ctx):
    """Bauch und Taille: Haut, ein Nabel, unten gehen die Schuppen in einem V nach oben über."""
    if ctx.face == "bottom":
        return _scale_color(ctx, 0.2)
    rows = ctx.h
    # das V: in der Mitte reichen die Schuppen tiefer hinab als an den Seiten
    if ctx.face in ("front", "back"):
        center = abs(ctx.s - (ctx.w - 1) / 2.0) / max(1.0, (ctx.w - 1) / 2.0)
        edge = rows - 3 + int(round(center * 2.0)) - 2
    else:
        edge = rows - 4
    if ctx.t >= edge:
        return _scales(ctx)
    if ctx.t == edge - 1 and hash01(ctx.u, ctx.v, 932) > 0.4:
        return mix(_skin(ctx), _scale_color(ctx, 0.75), 0.45)               # ein paar einzelne Schuppen
    if ctx.face == "front" and ctx.t == rows // 2 - 1 and abs(ctx.s - (ctx.w - 1) / 2.0) < 0.6:
        return _skin(ctx, 0.4)                                              # Nabel
    return _skin(ctx, 0.58)


def _face(ctx: Ctx):
    """Gesicht (14×14 Texel): große Augen mit Wimpern und Glanzpunkt, Brauen, Wangenrot, kleiner Mund."""
    face, s, t, w = ctx.face, ctx.s, ctx.t, ctx.w
    if face == "front":
        m = min(s, w - 1 - s)                                               # Abstand zum Rand (symmetrisch)
        if t <= 2:
            return _hair(ctx)
        if t == 4 and m in (2, 3, 4):
            return pick(ctx.pal["hair"], 0.22)                              # Brauen
        if 5 <= t <= 8 and m in (2, 3, 4, 5):
            return _eye(ctx, m, t)
        if t in (9, 10) and m in (1, 2):
            return mix(_skin(ctx, 0.66), pick(ctx.pal["lip"], 0.8), 0.32)    # Wangenrot
        if t == 9 and m == 6:
            return _skin(ctx, 0.52)                                         # Nasenschatten
        if t == 11 and m == 6:
            return pick(ctx.pal["lip"], 0.5)                                # kleiner Mund
        if t == 11 and m == 5:
            return _skin(ctx, 0.54)                                         # Mundwinkel (ein Lächeln)
        if t == 12 and m == 6:
            return mix(_skin(ctx, 0.62), pick(ctx.pal["lip"], 0.8), 0.3)
        if t >= 12 and m <= 1:
            return _skin(ctx, 0.52)                                         # Kinnlinie
        return _skin(ctx, 0.66 - (0.04 if m == 0 else 0.0))
    if face in ("west", "east"):
        if t <= 3 or ctx.s >= ctx.w // 2:
            return _hair(ctx)
        return _skin(ctx, 0.56)
    if face in ("top", "back"):
        return _hair(ctx)
    return _skin(ctx, 0.5)


def _eye(ctx: Ctx, m: int, t: int):
    # m: 2 (außen) … 5 (innen); t: 5 Wimpern, 6–8 Auge
    if t == 5:
        if m == 2:
            return pick(ctx.pal["lash"], 0.2)                               # Wimpernschwung nach außen
        return pick(ctx.pal["lash"], 0.1) if m <= 4 else _skin(ctx, 0.6)
    if m == 5:
        return pick(ctx.pal["white"], 0.6) if t == 7 else _skin(ctx, 0.6)  # inneres Augenweiß
    if t == 6:
        col = pick(ctx.pal["white"], 0.95) if m == 3 else pick(ctx.pal["iris"], 0.45)
    elif t == 7:
        col = pick(ctx.pal["iris"], 0.1) if m == 3 else pick(ctx.pal["iris"], 0.75)
    else:
        col = pick(ctx.pal["iris"], 0.95) if m in (3, 4) else pick(ctx.pal["lash"], 0.3)
    if _glowing(ctx) and t >= 6:
        ctx.glow = col
    return col


def _hair(ctx: Ctx, extra: float = 0.0):
    strand = hash01(ctx.s // 2, ctx.face, ctx.cube.origin, 933)
    v = 0.5 + strand * 0.2 + _LIT[ctx.face] - rel_y(ctx) * 0.1 + extra
    if hash01(ctx.u, ctx.v, 934) > 0.93:
        v += 0.18                                                           # Glanz
    return pick(ctx.pal["hair"], v)


def _bangs(ctx: Ctx):
    if ctx.face == "front" and ctx.t == ctx.h - 1 and ctx.s % 3 == 1:
        return None                                                         # gezackte Spitzen
    if ctx.face == "bottom":
        return pick(ctx.pal["hair"], 0.3)
    return _hair(ctx, 0.04)


def _hair_side(ctx: Ctx):
    if ctx.face in ("west", "east") and ctx.t == ctx.h - 1 and ctx.s % 2 == 0:
        return None
    return _hair(ctx)


def _lock(ctx: Ctx):
    if ctx.t == ctx.h - 1 and ctx.s % 2 == 1:
        return None
    r = rel_y(ctx)
    col = _hair(ctx, 0.04)
    if r > 0.75:
        col = mix(col, pick(ctx.pal["hair"], 0.92), (r - 0.75) * 1.6)       # hellere Spitzen
    return col


def _hair_back(ctx: Ctx):
    if ctx.face == "front":
        return pick(ctx.pal["hair"], 0.25)
    return _hair(ctx)


def _hair_tip(ctx: Ctx):
    # Spitzen: unten ausgefranst, zur Spitze hin heller
    r = rel_y(ctx)
    if ctx.face in ("back", "front", "west", "east") and ctx.t >= ctx.h - 3:
        if hash01(ctx.s, ctx.cube.origin, 935) < (ctx.t - (ctx.h - 4)) * 0.3:
            return None
    col = _hair(ctx, 0.06)
    return mix(col, pick(ctx.pal["hair"], 0.95), max(0.0, r - 0.45))


def _ear_fin(ctx: Ctx):
    return _fin_paint(ctx, along_axis=2, rays_axis=1, ragged=0.35)


# -------------------------------------------------------------------- Schuppen und Flossen

def _scale_color(ctx: Ctx, v: float):
    """Schwanzfarbe: zur Flosse hin heller und mit einem Hauch der Flossenfarbe."""
    y = ctx.world[1]
    k = min(1.0, max(0.0, (y - HIP_Y) / 16.0))
    col = pick(ctx.pal["tail"], v * 0.75 + 0.1 + k * 0.18)
    return mix(col, pick(ctx.pal["fin"], 0.5 + v * 0.3), k * 0.35)


def _scales(ctx: Ctx):
    """Schuppen in versetzten Reihen: jede mit dunklem Rand unten, hellem Glanz oben; der Bauch heller."""
    s, t = ctx.s, ctx.t
    row = t // 2
    shift = 1 if row % 2 else 0
    fs = (s + shift) % 3
    ft = t % 2
    v = 0.52
    if ft == 1 and fs != 1:
        v -= 0.18                                                           # Rand
    elif ft == 0 and fs == 1:
        v += 0.16                                                           # Glanz
    if ctx.face == "front":
        v += 0.08                                                           # Bauchseite heller
    if ctx.face == "back":
        v -= 0.06
    v += _LIT[ctx.face] + (hash01(ctx.u, ctx.v, 936) - 0.5) * 0.06
    col = _scale_color(ctx, v)
    if _glowing(ctx) and ft == 0 and fs == 1 and hash01(row, (s + shift) // 3, ctx.cube.origin, 937) > 0.8:
        col = pick(ctx.pal["fin"], 0.95)                                    # Leuchtflecken der Tiefsee
        ctx.glow = col
    return col


def _hips(ctx: Ctx):
    if ctx.face == "top":
        return _skin(ctx, 0.5)
    return _scales(ctx)


def _fin_paint(ctx: Ctx, along_axis: int, rays_axis: int, ragged: float, start: float = 0.0):
    """Flosse: vom Ansatz zum Rand heller, mit Strahlen; der Rand ausgefranst (durchsichtig)."""
    lo = ctx.cube.origin
    size = ctx.cube.size
    a = (ctx.local[along_axis] - lo[along_axis]) / max(0.01, size[along_axis])
    b = (ctx.local[rays_axis] - lo[rays_axis]) / max(0.01, size[rays_axis])
    wave = math.sin(b * 9.0) * 0.06
    if a > 0.86 + wave and hash01(ctx.u, ctx.v, 938) < ragged + (a - 0.86) * 3.0:
        return None
    ray = (int(b * 7.0 + a * 1.5) % 2) == 0
    v = 0.35 + a * 0.5 + (0.06 if ray else -0.04) + start
    col = pick(ctx.pal["fin"], v)
    if _glowing(ctx) and a > 0.72:
        ctx.glow = pick(ctx.pal["fin"], 0.9)
    return col


def _veil(ctx: Ctx):
    # Schleierflosse: lang nach unten, die Strahlen laufen längs
    return _fin_paint(ctx, along_axis=1, rays_axis=2, ragged=0.25)


def _frill(ctx: Ctx):
    return _fin_paint(ctx, along_axis=2, rays_axis=1, ragged=0.3, start=0.05)


def _arm_fin(ctx: Ctx):
    return _fin_paint(ctx, along_axis=2, rays_axis=1, ragged=0.3)


def _fluke(ctx: Ctx):
    """Ein Lappen der Schwanzflosse: am Ansatz breit, zur Spitze schmal, die Hinterkante geschwungen."""
    w, h = ctx.cube.size[0], ctx.cube.size[1]
    u = min(1.0, abs(ctx.local[0]) / w)                                      # 0 am Ansatz (x = 0), 1 an der Spitze
    v = (ctx.local[1] - ctx.cube.origin[1]) / h
    # Umriss: oben gerade, unten eine Sichel, die zur Spitze hin ansteigt; in der Mitte die Kerbe
    bottom = 1.0 - 0.82 * u ** 1.4
    notch = 0.55 + 0.45 * min(1.0, u * 3.5)
    if v > bottom * notch:
        return None
    if u > 0.97:
        return None
    # Strahlen fächern vom Ansatz aus
    ang = math.atan2(v, u + 0.05)
    ray = int(ang * 9.0) % 2 == 0
    edge = (bottom * notch - v) < 0.12
    val = 0.3 + u * 0.45 + v * 0.15 + (0.07 if ray else -0.03) + (0.12 if edge else 0.0)
    col = pick(ctx.pal["fin"], val)
    if _glowing(ctx) and (edge or u > 0.8):
        ctx.glow = pick(ctx.pal["fin"], 0.9)
    return mix(col, _scale_color(ctx, 0.5), max(0.0, 0.35 - u) * 0.8)


# -------------------------------------------------------------------- Arme, Muscheln, Schmuck

def _arm(ctx: Ctx):
    if ctx.face == "bottom":
        return _skin(ctx, 0.5)
    return _skin(ctx, 0.6 - rel_y(ctx) * 0.04)


def _hand(ctx: Ctx):
    if ctx.face == "front" and ctx.t == ctx.h - 1 and ctx.s % 2 == 1:
        return _skin(ctx, 0.46)                                             # Finger
    return _skin(ctx, 0.6)


def _shell(ctx: Ctx):
    """Muschel: Rippen, die vom unteren Rand nach oben fächern; heller Rand."""
    if _gear_hidden(ctx):
        return None
    if ctx.face == "front":
        cx = (ctx.w - 1) / 2.0
        dx = ctx.s - cx
        ang = math.atan2(dx, ctx.h - ctx.t)
        rib = int((ang + 2.0) * 4.0) % 2 == 0
        v = 0.5 + (0.12 if rib else -0.06) + (0.15 if ctx.t == 0 else 0.0)
        if ctx.t == 0 and abs(dx) > cx - 0.6:
            return None                                                     # gerundete Ecken
        if ctx.t == ctx.h - 1 or abs(dx) > cx - 0.6:
            v -= 0.22                                                       # dunkler Rand
        return pick(ctx.pal["shell"], v)
    return pick(ctx.pal["shell"], 0.38)


def _band(ctx: Ctx):
    return pick(ctx.pal["shell"], 0.3 + hash01(ctx.u, ctx.v, 939) * 0.08)


def _pearls(ctx: Ctx):
    if _gear_hidden(ctx):
        return None
    # einzelne Perlen mit Glanz
    if ctx.s % 2 == 1 and ctx.face == "front":
        return pick(ctx.pal["pearl"], 0.45)
    return pick(ctx.pal["pearl"], 0.85 if ctx.t == 0 else 0.7)


def _pearl_big(ctx: Ctx):
    if _gear_hidden(ctx):
        return None
    hi = ctx.s == 0 and ctx.t == 0
    return pick(ctx.pal["pearl"], 1.0 if hi else 0.72 - rel_y(ctx) * 0.2)


def _gold(ctx: Ctx):
    if _gear_hidden(ctx):
        return None
    v = 0.55 + (0.25 if ctx.t == 0 else 0.0) + (hash01(ctx.u, ctx.v, 940) - 0.5) * 0.1
    if ctx.face in ("front", "back", "west", "east") and ctx.s % 3 == 1:
        v = 0.9                                                             # eingefasste Steinchen
        return pick(ctx.pal["fin"], 0.85)
    return pick(ctx.pal["gold"], v)


def _gear_hidden(ctx: Ctx) -> bool:
    return False


# -------------------------------------------------------------------- Zubehör

def _band_head(ctx: Ctx):
    v = 0.4 + (0.2 if ctx.t == 0 else 0.0)
    return pick(ctx.pal["leather"], v + (hash01(ctx.u, ctx.v, 941) - 0.5) * 0.1)


def _net(ctx: Ctx):
    # Netzbeutel: Maschen, dahinter Perlen
    if (ctx.s + ctx.t) % 3 == 0 or (ctx.s - ctx.t) % 3 == 0:
        return pick(ctx.pal["net"], 0.55)
    if hash01(ctx.u, ctx.v, 942) > 0.35:
        return pick(ctx.pal["pearl"], 0.6 + hash01(ctx.u, ctx.v, 943) * 0.35)
    return None


def _leather(ctx: Ctx):
    x, y, z = ctx.world
    n = noise3(x * 0.7, y * 0.7, z * 0.7, 944)
    v = 0.4 + (n - 0.5) * 0.25
    if ctx.face in ("front", "back", "west", "east") and ctx.t == 0:
        v += 0.12
    return pick(ctx.pal["leather"], v)


def _coral(ctx: Ctx):
    v = 0.45 + (1.0 - rel_y(ctx)) * 0.3 + _LIT[ctx.face]
    if hash01(ctx.u, ctx.v, 945) > 0.8:
        v += 0.18                                                           # Polypen
    return pick(ctx.pal["coral"], v)


def _anemone(ctx: Ctx):
    # Seeanemone: helle Fangarme mit dunklerer Mitte
    if ctx.face in ("west", "east"):
        cx, cy = (ctx.w - 1) / 2.0, (ctx.h - 1) / 2.0
        d = math.hypot(ctx.s - cx, ctx.t - cy)
        if d > cx + 0.4:
            return None
        if d < 1.0:
            return pick(ctx.pal["coral"], 0.3)
        return pick(ctx.pal["fin"], 0.55 + (int(math.atan2(ctx.t - cy, ctx.s - cx) * 3) % 2) * 0.25)
    return pick(ctx.pal["fin"], 0.4)


def _scallop(ctx: Ctx):
    if ctx.face in ("front", "back"):
        cx = (ctx.w - 1) / 2.0
        dx = ctx.s - cx
        dy = ctx.h - 1 - ctx.t
        if math.hypot(dx * 0.95, dy * 1.05) > ctx.w / 2.0 + 0.3:
            return None
        rib = int((math.atan2(dx, dy + 0.5) + 2.0) * 3.5) % 2 == 0
        return pick(ctx.pal["conch"], 0.6 + (0.18 if rib else -0.04))
    return pick(ctx.pal["conch"], 0.45)


def _shaft(ctx: Ctx):
    x, y, z = ctx.world
    return pick(ctx.pal["shaft"], 0.45 + math.sin(y * 1.1 + x) * 0.08 + _LIT[ctx.face])


def _conch(ctx: Ctx):
    # Muschelhorn: Spirale aus hellen und dunklen Bändern
    y = ctx.world[1]
    band = int(y * 2.4 + ctx.s * 0.7) % 3
    v = 0.55 + (0.2 if band == 0 else -0.05 if band == 1 else 0.08) + _LIT[ctx.face]
    return pick(ctx.pal["conch"], v)


def _star(ctx: Ctx):
    # Seestern: fünf Arme aus der Mitte
    if ctx.face in ("front", "back"):
        cx, cy = (ctx.w - 1) / 2.0, (ctx.h - 1) / 2.0
        dx, dy = ctx.s - cx, ctx.t - cy
        r = math.hypot(dx, dy)
        ang = math.atan2(dy, dx)
        arm = abs(math.cos(2.5 * ang + 0.3))
        if r > 0.9 + arm * (cx + 0.2):
            return None
        v = 0.55 + (0.2 if (ctx.s + ctx.t) % 2 == 0 and r > 1.2 else 0.0) - r * 0.04
        return pick(ctx.pal["star"], v)
    return pick(ctx.pal["star"], 0.4)


def _helmet(ctx: Ctx):
    x, y, z = ctx.world
    n = noise3(x * 0.6, y * 0.6, z * 0.6, 946)
    v = 0.55 + _LIT[ctx.face] + (0.15 if ctx.face == "top" else 0.0)
    if ctx.face in ("front", "west", "east", "back") and ctx.t == ctx.h - 1:
        v += 0.18                                                           # Rand
    col = pick(ctx.pal["bronze"], v)
    if n > 0.7:
        col = mix(col, pick(ctx.pal["patina"], 0.5), 0.4)                   # grüne Patina
    return col


def _crest(ctx: Ctx):
    # Flossenkamm in der Flossenfarbe der Gestalt
    if ctx.face in ("west", "east") and ctx.t == 0 and ctx.s % 2 == 0:
        return None
    return _fin_paint(ctx, along_axis=1, rays_axis=2, ragged=0.0, start=0.1) or pick(ctx.pal["fin"], 0.6)


def _armor(ctx: Ctx):
    """Brustpanzer und Schulterstücke aus Bronze: Lamellen mit hellen Kanten, ein Grat in der Mitte, etwas Patina."""
    x, y, z = ctx.world
    v = 0.5 + _LIT[ctx.face]
    lame = int((y + 20.0) * 1.25)
    if (y + 20.0) * 1.25 - lame < 0.25:
        v += 0.2                                                            # helle Oberkante jeder Lamelle
    elif (y + 20.0) * 1.25 - lame > 0.8:
        v -= 0.16                                                           # Schatten darunter
    if ctx.face == "front" and abs(ctx.local[0]) < 0.4:
        v += 0.14                                                           # Grat in der Mitte
    if ctx.face == "top":
        v += 0.1
    col = pick(ctx.pal["bronze"], v + (hash01(ctx.u, ctx.v, 948) - 0.5) * 0.05)
    if noise3(x * 0.45, y * 0.45, z * 0.45, 947) > 0.76:
        col = mix(col, pick(ctx.pal["patina"], 0.55), 0.4)
    return col


def _trident(ctx: Ctx):
    v = 0.55 + _LIT[ctx.face] + (0.25 if ctx.face == "top" else 0.0) - rel_y(ctx) * 0.1
    col = pick(ctx.pal["patina"], v)
    return col


def materials(variant: str) -> dict:
    return {
        "chest": _chest, "belly": _belly, "face": _face, "hair": _hair, "bangs": _bangs, "hair_side": _hair_side,
        "lock": _lock, "hair_back": _hair_back, "hair_tip": _hair_tip, "ear_fin": _ear_fin,
        "arm": _arm, "hand": _hand, "arm_fin": _arm_fin, "gold": _gold,
        "hips": _hips, "scales": _scales, "veil": _veil, "frill": _frill, "fluke": _fluke,
        "shell": _shell, "band": _band, "pearls": _pearls, "pearl_big": _pearl_big,
        "band_head": _band_head, "net": _net, "leather": _leather, "coral": _coral, "anemone": _anemone,
        "scallop": _scallop, "shaft": _shaft, "conch": _conch, "star": _star,
        "helmet": _helmet, "crest": _crest, "armor": _armor, "trident": _trident,
    }
