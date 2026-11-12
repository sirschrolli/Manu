"""
Ignara, die Flammenpriesterin (seit 1.2): die letzte Priesterin der Feuerdiener, die durch die Verlorene Pforte in die
Unterwelt kamen. Im Flammenheiligtum hütet sie das ewige Feuer – und ist mit ihm eins geworden.

Eine hochgewachsene Frau (im Spiel 1,2-fach vergrößert, mit dem Haar gut drei Blöcke hoch), die über dem Boden schwebt:
bronzene Haut mit glühenden Glutzeichen auf den Armen, ein Gewand aus tiefrotem Tuch mit goldenen Säumen, das nach unten
dunkler, wie verkohlt wird und in einem Saum aus Flammen endet; ein Mieder mit V-Ausschnitt und einer Kette aus
Feueropalen, eine goldene Schärpe mit Opalschnalle, ein Schulterkragen aus Gold und ein Umhang, dessen Ränder brennen.
Über den Augen eine goldene Halbmaske mit glühenden Sehschlitzen, darüber ein Stirnreif mit fünf Strahlen und einem
Feueropal; ihr Haar sind lebendige Flammen, die nach oben und hinten lodern. Hinter ihren Schultern kreist ein Rad aus
acht Feueropal-Splittern. In der Rechten die Flammenpeitsche (Griff aus Schwarzgold mit Opalknauf, die Schnur aus sechs
glühenden Gliedern), über der Linken schwebt eine Feuerkugel. In der Phönixgestalt wachsen ihr Schwingen aus Flammen.

Knochen-Namen werden in IgnaraModel (Java) für die Animationen verwendet.
"""
from __future__ import annotations

import math

from ..geometry import Model
from ..paint import Ctx, hash01, hexc, mix, noise3, pick, ramp, rel_y


def stops(*colors: str) -> list:
    """Handverlesene Tonleiter (für Glut, Flammen und Opal: der lineare Verlauf von Dunkelrot nach Gelb würde braun)."""
    return [hexc(c) for c in colors]


PALETTES = {
    "ignara": {
        "skin": stops("#4a2414", "#6e3a22", "#94563a", "#b8754e", "#d49468", "#e8b48a", "#f6d2b0"),
        "lip": ramp("#2a0606", "#b8303a", steps=5),
        "robe": stops("#120204", "#2c0608", "#4e0a0e", "#7a1216", "#a81c1e", "#d0322a", "#ee5a3c"),
        "char": ramp("#060304", "#3a1c18", steps=5),
        "gold": stops("#3a2206", "#6e4410", "#a8701c", "#d49c30", "#f0c450", "#ffe08a", "#fff6cc"),
        "ember": stops("#5c1404", "#a02806", "#de4a0c", "#ff7418", "#ff9c2c", "#ffc860", "#fff0b0"),
        "flame": stops("#7a1402", "#d23a08", "#ff6a12", "#ff9a26", "#ffc64e", "#ffe88a", "#fffbe0"),
        "opal": stops("#6a1002", "#b02408", "#f0500e", "#ff8020", "#ffb040", "#ffe07a", "#fff6c8"),
        "flash": stops("#127a46", "#2fc070", "#7ff0a8", "#e6fff0"),
        "leather": ramp("#0c0606", "#4a2a1e", steps=6),
        "eye": stops("#ff8a20", "#ffc860", "#fff4c0", "#ffffff"),
    },
}

_LIT = {"top": 0.08, "front": 0.03, "west": 0.0, "east": -0.03, "back": -0.06, "bottom": -0.14}

#: das Rad der Splitter hinter den Schultern: so viele Splitter, so weit von der Nabe
SHARDS = 8
WHEEL_RADIUS = 7.0
#: die Glieder der Peitschenschnur: Länge und Dicke
LASH = ((4.6, 0.95), (4.4, 0.9), (4.2, 0.82), (4.0, 0.74), (3.8, 0.64), (3.6, 0.52))


def build() -> Model:
    m = Model("ignara", 192, 192)
    m.tex_res = 2
    # ------------------------------------------------------------------ Rumpf: Gewand, Schärpe, Mieder
    m.bone("body", pivot=(0, 2, 0))
    m.bone("skirt", "body", pivot=(0, 0, 0))
    # das Gewand fällt in vier Bahnen, nach unten weiter (A-Linie)
    m.cube("skirt", (-4.4, 0, -2.9), (8.8, 4.6, 5.8), "robe_top")
    m.cube("skirt", (-5.2, 4.6, -3.4), (10.4, 4.6, 6.8), "robe_mid")
    m.cube("skirt", (-6.0, 9.2, -4.0), (12.0, 4.8, 8.0), "robe_mid")
    m.cube("skirt", (-6.8, 14.0, -4.6), (13.6, 5.0, 9.2), "robe_low")
    # der Saum aus Flammen: vier Flammenwände rund um den Saum (flackern)
    m.bone("hem", "skirt", pivot=(0, 18.6, 0))
    m.cube("hem", (-6.8, 0, -4.7), (13.6, 5, 0), "hem_flame")
    m.cube("hem", (-6.8, 0, 4.7), (13.6, 5, 0), "hem_flame")
    m.cube("hem", (-6.9, 0, -4.6), (0, 5, 9.2), "hem_flame")
    m.cube("hem", (6.9, 0, -4.6), (0, 5, 9.2), "hem_flame")
    # Schärpe mit Opalschnalle
    m.cube("body", (-4.3, -1.6, -2.9), (8.6, 2.2, 5.8), "sash")
    m.cube("body", (-1.0, -1.9, -3.4), (2.0, 2.0, 0.6), "opal")
    # das Mieder
    m.bone("torso", "body", pivot=(0, -1, 0))
    m.cube("torso", (-4.0, -8.0, -2.4), (8, 7.4, 4.8), "bodice")
    # die Kette aus Feueropalen über dem Ausschnitt
    for i, (x, y) in enumerate(((-1.6, -6.3), (0.0, -5.6), (1.6, -6.3))):
        m.cube("torso", (x - 0.45, y, -2.75), (0.9, 0.9, 0.45), "opal")
    # der goldene Schulterkragen
    m.cube("torso", (-5.0, -8.8, -2.9), (10, 2.0, 5.8), "collar")
    # der Umhang: von den Schultern über den Rücken bis zu den Waden (zwei Glieder, die Ränder brennen)
    m.bone("cape", "torso", pivot=(0, -8.4, 2.6), rotation=(8, 0, 0))
    m.cube("cape", (-4.6, 0, 0), (9.2, 11, 0.6), "cape")
    m.bone("cape_low", "cape", pivot=(0, 11, 0.05), rotation=(6, 0, 0))
    m.cube("cape_low", (-4.4, 0, 0), (8.8, 9, 0.5), "cape_low")

    # ------------------------------------------------------------------ Kopf: Gesicht, Maske, Stirnreif, Flammenhaar
    m.bone("head", "torso", pivot=(0, -8.6, 0))
    m.cube("head", (-1.2, -1.2, -1.2), (2.4, 1.6, 2.4), "skin")                      # Hals
    m.cube("head", (-3.0, -7.2, -3.0), (6, 6.2, 6), "face")
    m.cube("head", (-3.25, -5.7, -3.3), (6.5, 2.2, 0.6), "mask")
    m.cube("head", (-3.2, -7.6, -3.2), (6.4, 1.0, 6.4), "circlet")
    m.bone("crown", "head", pivot=(0, -7.4, -3.1))
    for i, (x, rz, h) in enumerate(((-2.4, -34, 2.0), (-1.2, -16, 2.8), (0.0, 0, 3.6), (1.2, 16, 2.8), (2.4, 34, 2.0))):
        ray = f"crown_ray_{i}"
        m.bone(ray, "crown", pivot=(x, 0, 0), rotation=(-10, 0, rz))
        m.cube(ray, (-0.22, -h, -0.22), (0.44, h, 0.44), "ray")
    m.cube("crown", (-0.65, -1.6, -0.45), (1.3, 1.3, 0.55), "opal")
    # das Haar: lebendige Flammen, die nach oben und hinten lodern (vier Wände, über Kreuz)
    m.bone("hair", "head", pivot=(0, -6.6, 0.8), rotation=(-18, 0, 0))
    m.bone("hair_1", "hair", rotation=(0, 0, 0))
    m.cube("hair_1", (-4.5, -13, 0), (9, 13, 0), "hair_flame")
    m.bone("hair_2", "hair", rotation=(0, 90, 0))
    m.cube("hair_2", (-4.0, -11, 0), (8, 11, 0), "hair_flame")
    m.bone("hair_3", "hair", rotation=(0, 45, 0))
    m.cube("hair_3", (-3.5, -9, 0), (7, 9, 0), "hair_flame")
    m.bone("hair_4", "hair", rotation=(0, -45, 0))
    m.cube("hair_4", (-3.5, -9, 0), (7, 9, 0), "hair_flame")
    # zwei Strähnen aus Flammen fallen hinter den Ohren auf die Schultern
    for side, sx in (("l", 1), ("r", -1)):
        lock = f"lock_{side}"
        m.bone(lock, "head", pivot=(3.0 * sx, -5.6, 1.0), rotation=(14, 0, -10 * sx))
        m.cube(lock, (-0.7, 0, -0.7), (1.4, 7.0, 1.4), "lock")

    # ------------------------------------------------------------------ Arme
    for side, sx in (("left", 1), ("right", -1)):
        arm = f"{side}_arm"
        m.bone(arm, "torso", pivot=(4.7 * sx, -7.4, 0), rotation=(-8, 0, -12 * sx))
        m.cube(arm, (-1.1, -0.4, -1.1), (2.2, 5.8, 2.2), "skin_tattoo")
        m.cube(arm, (-1.3, 1.2, -1.3), (2.6, 1.0, 2.6), "gold")                        # Armreif
        fore = f"{side}_forearm"
        m.bone(fore, arm, pivot=(0, 5.4, 0), rotation=(-26, 0, 0))
        m.cube(fore, (-1.0, 0, -1.0), (2.0, 5.2, 2.0), "skin_tattoo")
        m.cube(fore, (-1.25, 2.4, -1.25), (2.5, 2.2, 2.5), "bracer")
        m.cube(fore, (-1.0, 5.0, -1.0), (2.0, 2.2, 2.0), "hand")
    # über der Linken schwebt die Feuerkugel
    m.bone("orb", "left_forearm", pivot=(0, 9.4, -1.4))
    m.cube("orb", (-1.6, -1.6, -1.6), (3.2, 3.2, 3.2), "fireball")
    m.cube("orb", (-1.0, -1.0, -1.0), (2.0, 2.0, 2.0), "fireball_core", inflate=0.0)
    # in der Rechten die Flammenpeitsche: Griff, Knauf, sechs Glieder der Schnur
    m.bone("whip", "right_forearm", pivot=(0, 6.2, 0), rotation=(-80, 0, 0))
    m.cube("whip", (-0.6, -1.2, -0.6), (1.2, 6.4, 1.2), "whip_handle")
    m.cube("whip", (-0.85, -2.6, -0.85), (1.7, 1.6, 1.7), "opal")
    m.cube("whip", (-0.8, 4.6, -0.8), (1.6, 0.8, 1.6), "gold")
    parent = "whip"
    y = 5.4
    for i, (length, thick) in enumerate(LASH):
        seg = f"whip_{i + 1}"
        m.bone(seg, parent, pivot=(0, y, 0), rotation=(6, 0, 0))
        m.cube(seg, (-thick / 2, 0, -thick / 2), (thick, length, thick), "lash")
        parent = seg
        y = length

    # ------------------------------------------------------------------ das Rad der Splitter hinter den Schultern
    m.bone("wheel", "torso", pivot=(0, -6.0, 4.6))
    for i in range(SHARDS):
        a = i * 360.0 / SHARDS
        shard = f"shard_{i}"
        m.bone(shard, "wheel", pivot=(0, 0, 0), rotation=(0, 0, a))
        m.cube(shard, (-0.6, -WHEEL_RADIUS - 1.6, -0.4), (1.2, 3.2, 0.8), "shard")
        m.cube(shard, (-0.35, -WHEEL_RADIUS - 2.5, -0.3), (0.7, 0.9, 0.6), "shard_tip")
    m.cube("wheel", (-0.9, -0.9, -0.5), (1.8, 1.8, 1.0), "opal")

    # ------------------------------------------------------------------ die Schwingen der Phönixgestalt (nur dann sichtbar)
    for side, sx in (("left", 1), ("right", -1)):
        wing = f"{side}_wing"
        m.bone(wing, "torso", pivot=(1.6 * sx, -6.6, 2.8), rotation=(0, -24 * sx, -12 * sx))
        m.cube(wing, (0 if sx > 0 else -12, -7, 0), (12, 12, 0), "wing_inner")
        tip = f"{side}_wing_tip"
        m.bone(tip, wing, pivot=(11.0 * sx, -4.0, 0), rotation=(0, -14 * sx, -18 * sx))
        m.cube(tip, (0 if sx > 0 else -12, -6, 0), (12, 13, 0), "wing_outer")
    return m


# =============================================================================================
# Bemalen
# =============================================================================================

def _glow(ctx: Ctx, key: str, v: float, strength: float = 1.0):
    col = pick(ctx.pal[key], v)
    ctx.glow = col if strength >= 1.0 else mix((0, 0, 0, 255), col, strength)
    return col


def _skin(ctx: Ctx, base: float = 0.55):
    x, y, z = ctx.world
    n = noise3(x * 0.9, y * 0.9, z * 0.9, 77)
    return pick(ctx.pal["skin"], base + (n - 0.5) * 0.08 + _LIT[ctx.face] - rel_y(ctx) * 0.05)


def _skin_plain(ctx: Ctx):
    return _skin(ctx)


def _skin_tattoo(ctx: Ctx):
    """Arme: bronzene Haut, darüber Glutzeichen – geschwungene Linien, die glühen."""
    x, y, z = ctx.world
    band = math.sin(y * 1.6 + math.sin(x * 2.0 + z * 2.0) * 1.4)
    if ctx.face in ("front", "west", "east") and abs(band) < 0.13 and hash01(int(y * 2), ctx.face, 3) > 0.35:
        return _glow(ctx, "ember", 0.62)
    return _skin(ctx)


def _hand(ctx: Ctx):
    if ctx.face in ("front", "back") and ctx.s % 2 == 1 and rel_y(ctx) > 0.55:
        return _skin(ctx, 0.4)                                     # Finger
    return _skin(ctx, 0.52)


def _face(ctx: Ctx):
    """Das Gesicht: schmal, unter der Maske eine gerade Nase, dunkelrote Lippen, ein spitzes Kinn."""
    if ctx.face != "front":
        if ctx.face == "top":
            return _glow(ctx, "flame", 0.5)                         # der Scheitel unter den Flammen glüht
        if ctx.face in ("west", "east", "back") and rel_y(ctx) < 0.5:
            return _glow(ctx, "flame", 0.35 + rel_y(ctx) * 0.3)     # Flammenhaar an Schläfen und Hinterkopf
        return _skin(ctx, 0.5)
    lx, ly, _ = ctx.local
    if ly < -5.7:
        return _glow(ctx, "flame", 0.45)                            # Stirn unter dem Reif: Flammenhaar fällt herein
    if abs(lx) < 0.4 and -3.6 < ly < -2.2:
        return _skin(ctx, 0.66)                                     # Nasenrücken
    if abs(lx) < 1.0 and -2.2 <= ly < -1.7:
        return _skin(ctx, 0.4)                                      # Schatten unter der Nase
    if abs(lx) < 1.1 and -1.65 < ly < -1.05:
        return pick(ctx.pal["lip"], 0.55 if ly < -1.35 else 0.75)    # Lippen
    if abs(lx) > 2.2 and ly > -3.0:
        return _skin(ctx, 0.46)                                     # Wangen im Schatten
    return _skin(ctx, 0.58)


def _mask(ctx: Ctx):
    """Die goldene Halbmaske über den Augen: geschwungene Ränder, zwei glühende Sehschlitze, eine Flamme auf der Stirn."""
    if ctx.face != "front":
        return pick(ctx.pal["gold"], 0.5 + _LIT[ctx.face])
    lx, ly, _ = ctx.local
    for ex in (-1.45, 1.45):
        if abs(lx - ex) < 0.85 and -4.85 < ly < -4.3:
            return _glow(ctx, "eye", 0.85 if abs(lx - ex) < 0.45 else 0.5)
    if abs(lx) < 0.35 and ly < -4.9:
        return _glow(ctx, "flame", 0.7)
    v = 0.66 - rel_y(ctx) * 0.2 + (0.12 if ctx.t == 0 else 0.0)
    if ctx.s in (0, ctx.w - 1):
        v -= 0.15
    return pick(ctx.pal["gold"], v)


def _gold_mat(ctx: Ctx, base: float = 0.58):
    v = base + _LIT[ctx.face] + (0.12 if ctx.t == 0 else 0.0) - (0.12 if ctx.t == ctx.h - 1 else 0.0)
    v += (hash01(ctx.u, ctx.v, 9) - 0.5) * 0.06
    return pick(ctx.pal["gold"], v)


def _gold(ctx: Ctx):
    return _gold_mat(ctx)


def _circlet(ctx: Ctx):
    if ctx.face in ("top", "bottom"):
        return None if ctx.face == "top" and 1 <= ctx.s < ctx.w - 1 and 1 <= ctx.t < ctx.h - 1 else _gold_mat(ctx, 0.5)
    return _gold_mat(ctx, 0.62)


def _ray(ctx: Ctx):
    """Ein Strahl des Stirnreifs: Gold, die Spitze glüht."""
    if rel_y(ctx) < 0.25:
        return _glow(ctx, "flame", 0.75)
    return _gold_mat(ctx, 0.66)


def _collar(ctx: Ctx):
    """Der Schulterkragen: Gold mit einer Reihe Flammenzungen als Prägung, unten ein dunkler Saum."""
    if ctx.face == "top":
        return _gold_mat(ctx, 0.66)
    if ctx.t >= ctx.h - 1:
        return pick(ctx.pal["robe"], 0.25)
    if ctx.face in ("front", "back") and (ctx.s + ctx.t) % 4 == 0:
        return _gold_mat(ctx, 0.4)
    return _gold_mat(ctx, 0.6)


def _robe(ctx: Ctx, base: float, char_from: float = 2.0):
    """Tiefrotes Tuch mit Falten (senkrechte Bahnen), nach unten dunkler; ab {@code char_from} (Höhe im Gewand, 0..1)
    verkohlt, mit Glut in den Brandlöchern."""
    x, y, z = ctx.world
    fold = math.sin((x if ctx.face in ("front", "back") else z) * 2.2 + 0.7) * 0.07
    v = base + fold + _LIT[ctx.face] + (noise3(x * 1.1, y * 0.4, z * 1.1, 13) - 0.5) * 0.08
    depth = (y - 2.0) / 19.0                                        # 0 an der Hüfte, 1 am Saum
    if depth > char_from:
        burn = noise3(x * 0.6, y * 0.45, z * 0.6, 29)
        k = (depth - char_from) / (1.0 - char_from)
        if burn < k * 0.42 - 0.08:
            return _glow(ctx, "ember", 0.5 + (k * 0.42 - burn) * 0.9)
        v -= k * 0.3
    return pick(ctx.pal["robe"], v)


def _robe_top(ctx: Ctx):
    # oben ein goldener Saum, vorn eine Bahn mit Flammenstickerei
    if ctx.face != "top" and ctx.t <= 1:
        return _gold_mat(ctx, 0.55)
    if ctx.face == "front" and abs(ctx.local[0]) < 1.2:
        return _embroidery(ctx)
    return _robe(ctx, 0.55)


def _robe_mid(ctx: Ctx):
    if ctx.face == "front" and abs(ctx.local[0]) < 1.6:
        return _embroidery(ctx)
    return _robe(ctx, 0.5)


def _robe_low(ctx: Ctx):
    if ctx.face == "bottom":
        return _glow(ctx, "ember", 0.5)                             # unten glüht das Gewand
    if ctx.face == "front" and abs(ctx.local[0]) < 2.0 and ctx.t < ctx.h - 3:
        return _embroidery(ctx)
    if ctx.t >= ctx.h - 1:
        return _glow(ctx, "ember", 0.62)                            # der glühende Saum
    return _robe(ctx, 0.44, 0.7)


def _embroidery(ctx: Ctx):
    """Die Bahn vorn am Gewand: dunkelrot, darauf goldene Flammenzungen übereinander (Stickerei)."""
    lx, ly, _ = ctx.local
    phase = (ly * 1.1) % 3.0
    width = 1.2 - phase * 0.35
    if abs(lx) < max(0.2, width) and phase < 2.6:
        return _gold_mat(ctx, 0.5 + (0.2 if phase < 0.8 else 0.0))
    return pick(ctx.pal["robe"], 0.28 + _LIT[ctx.face])


def _bodice(ctx: Ctx):
    """Das Mieder: tiefrot, goldene Säume an den Kanten; vorn ein V-Ausschnitt (Haut) mit der Opalkette."""
    lx, ly, _ = ctx.local
    if ctx.face == "front":
        depth = ly + 8.0                                            # 0 oben … 7.4 unten
        if depth < 3.4 and abs(lx) < 2.6 - depth * 0.75:
            return _skin(ctx, 0.6)
        if depth < 3.6 and abs(abs(lx) - (2.6 - depth * 0.75)) < 0.35:
            return _gold_mat(ctx, 0.62)                             # Saum des Ausschnitts
        if abs(lx) < 0.3 and depth > 3.6:
            return _gold_mat(ctx, 0.5)                              # Schnürung
    if ctx.face != "top" and (ctx.t == ctx.h - 1 or ctx.s in (0, ctx.w - 1) and ctx.face in ("west", "east")):
        return _gold_mat(ctx, 0.48)
    return _robe(ctx, 0.56)


def _sash(ctx: Ctx):
    if ctx.face in ("top", "bottom"):
        return _gold_mat(ctx, 0.45)
    if ctx.t in (0, ctx.h - 1):
        return _gold_mat(ctx, 0.7)
    return _gold_mat(ctx, 0.55) if ctx.s % 3 else _gold_mat(ctx, 0.4)


def _cape(ctx: Ctx, low: bool = False):
    """Der Umhang: dunkelrot, innen fast schwarz; die Ränder brennen, unten verkohlt mit glühenden Löchern."""
    x, y, z = ctx.world
    if ctx.face == "front":
        return pick(ctx.pal["robe"], 0.18)
    edge = min(ctx.s, ctx.w - 1 - ctx.s)
    if edge == 0 and hash01(ctx.t // 2, ctx.s, 3) > 0.45:
        return _glow(ctx, "ember", 0.45 + hash01(ctx.t, 3) * 0.25)
    burn = noise3(x * 0.7, y * 0.7, z, 51)
    if low:
        k = rel_y(ctx)
        if burn < k * 0.5 - 0.1:
            return None if burn < k * 0.5 - 0.25 else _glow(ctx, "ember", 0.55)
        if ctx.t == ctx.h - 1:
            return _glow(ctx, "flame", 0.6)
    v = 0.5 + math.sin(x * 1.9) * 0.08 + _LIT[ctx.face] - (rel_y(ctx) * 0.2 if low else 0.0)
    if not low and ctx.t <= 1:
        return _gold_mat(ctx, 0.55)                                 # oben ein goldener Saum unter dem Kragen
    return pick(ctx.pal["robe"], v)


def _cape_top(ctx: Ctx):
    return _cape(ctx)


def _cape_low(ctx: Ctx):
    return _cape(ctx, True)


_FLAME_HAIR = ["....o.....o.",
               "...oO....oO.",
               "..oOO.o..OO.",
               ".oOYOoO.oYOo",
               ".OYYOOYooYYO",
               "oOYWYOYYOYWY",
               "OYWWYYWWYYWW",
               "YWWWWWWWWWWW"]


def _flame_pattern(ctx: Ctx, pattern: list[str], key: str = "flame", shift: float = 0.0):
    """Flammen (nach Lage auf der Ebene gemalt, damit Vorder- und Rückseite zusammenpassen)."""
    lx, ly, lz = ctx.local
    ox, oy, oz = ctx.cube.origin
    sx, sy, sz = ctx.cube.size
    horiz = (lx - ox) / sx if sx > 0 else (lz - oz) / sz
    col_i = max(0, min(len(pattern[0]) - 1, int(horiz * len(pattern[0]))))
    row_i = max(0, min(len(pattern) - 1, int((ly - oy) / sy * len(pattern))))
    level = {"o": 0.12, "O": 0.36, "Y": 0.6, "W": 0.84}.get(pattern[row_i][col_i])
    if level is None:
        return None
    return _glow(ctx, key, min(0.99, level + shift))


def _hair_flame(ctx: Ctx):
    return _flame_pattern(ctx, _FLAME_HAIR)


_HEM = [".o..o...o..o..o..",
        "oO.oO..oOo.Oo.oO.",
        "OYoOYooOYOoYOoYOo",
        "YWYYWYYYWYYWYYWYY",
        "WWWWWWWWWWWWWWWWW"]


_HEM_DOWN = list(reversed(_HEM))


def _hem_flame(ctx: Ctx):
    # die Flammen des Saums wachsen nach UNTEN aus dem Gewand: die Zeichnung steht auf dem Kopf
    return _flame_pattern(ctx, _HEM_DOWN, "flame", -0.05)


def _lock(ctx: Ctx):
    """Eine Strähne aus Flammen: oben am Kopf hell, zur Spitze rot und durchbrochen."""
    ry = rel_y(ctx)
    if ry > 0.8 and hash01(ctx.s, ctx.t, ctx.face, 7) > 0.55:
        return None
    return _glow(ctx, "flame", 0.8 - ry * 0.6)


def _opal(ctx: Ctx):
    """Feueropal: gewölbt, oben links ein Glanz, unten rechts das Glühen im Innern; hier und da ein grüner Blitz."""
    if ctx.face not in ("front", "top"):
        return _glow(ctx, "opal", 0.35, 0.6)
    cx, cy = (ctx.w - 1) / 2.0, (ctx.h - 1) / 2.0
    u = (ctx.s - cx) / max(cx, 0.5)
    v = (ctx.t - cy) / max(cy, 0.5)
    if ctx.s == 0 and ctx.t == 0:
        return _glow(ctx, "opal", 0.99)
    if hash01(ctx.s * 7 + ctx.t * 13, ctx.cube.origin[1], 5) > 0.84 and abs(u) < 0.8 and abs(v) < 0.8:
        return _glow(ctx, "flash", 0.7, 0.8)
    return _glow(ctx, "opal", 0.56 - 0.12 * (u + v) * 0.5 + 0.22 * max(0.0, 1.0 - math.hypot(u - 0.4, v - 0.4)), 0.85)


def _bracer(ctx: Ctx):
    """Armschiene aus Gold mit einem Feueropal außen."""
    if ctx.face in ("west", "east") and abs(ctx.s - (ctx.w - 1) / 2.0) < 1.0 and abs(ctx.t - (ctx.h - 1) / 2.0) < 1.0:
        return _glow(ctx, "opal", 0.6, 0.85)
    return _gold_mat(ctx, 0.56)


def _fireball(ctx: Ctx):
    """Die Feuerkugel: außen rote Flammen, nach innen gelb; die Ecken sind durchbrochen."""
    cx, cy = (ctx.w - 1) / 2.0, (ctx.h - 1) / 2.0
    r = max(abs(ctx.s - cx) / max(cx, 0.5), abs(ctx.t - cy) / max(cy, 0.5))
    if r > 0.86 and hash01(ctx.s, ctx.t, ctx.face, 11) > 0.45:
        return None
    return _glow(ctx, "flame", 0.62 - r * 0.38 + hash01(ctx.s, ctx.t, ctx.face) * 0.1)


def _fireball_core(ctx: Ctx):
    return _glow(ctx, "flame", 0.92)


def _whip_handle(ctx: Ctx):
    """Der Griff: dunkles Leder, schräg gewickelt, mit goldenen Ringen."""
    if (ctx.t + ctx.s) % 3 == 0:
        return pick(ctx.pal["leather"], 0.22)
    if ctx.t % 6 == 0:
        return _gold_mat(ctx, 0.55)
    return pick(ctx.pal["leather"], 0.48 + _LIT[ctx.face])


def _lash(ctx: Ctx):
    """Ein Glied der Schnur: ein glühender Strang, innen hell, mit dunkler Glut in den Windungen."""
    x, y, z = ctx.world
    twist = math.sin(y * 3.0 + (x + z) * 2.0)
    return _glow(ctx, "ember", 0.66 + twist * 0.14)


def _shard(ctx: Ctx):
    """Ein Splitter des Rads: Feueropal, längs geschliffen, zur Spitze heller."""
    ry = rel_y(ctx)
    if ctx.face in ("west", "east"):
        return _glow(ctx, "opal", 0.35 + (1.0 - ry) * 0.2, 0.8)
    v = 0.48 + (1.0 - ry) * 0.3 + (0.1 if ctx.s == 0 else 0.0)
    if hash01(ctx.s, ctx.t, ctx.cube.origin[1], 17) > 0.86:
        return _glow(ctx, "flash", 0.6, 0.8)
    return _glow(ctx, "opal", v, 0.9)


def _shard_tip(ctx: Ctx):
    return _glow(ctx, "opal", 0.9)


_WING_IN = ["..........oO",
            "......o.oOOY",
            "....oOOoOYYW",
            "..oOOYOOYYWW",
            ".oOYYYYYYWWW",
            "oOYYWWWWWWWW",
            "OYYWWWWWWWWW",
            "oOYYWWWWWWWW",
            ".oOYYYYWWWWW",
            "..ooOOOYYYWW",
            "......oOOYYY",
            ".........oOO"]
_WING_OUT = ["...........o",
             "........o.oO",
             ".....o.oOoOY",
             "...oOOoOYOYY",
             ".oOOYOOYYYYW",
             "oOYYYYYYWWWW",
             "OYYWWWWWWWWW",
             "oOYYYWWWWWWW",
             ".oOOYYYYYWWW",
             "...oOOOYYYYY",
             ".....ooOOOYY",
             "........ooOO",
             "...........o"]


def _wing(ctx: Ctx, pattern: list[str]):
    """Eine Schwinge aus Flammen: Federn aus Feuer, an der Schulter weißglühend, nach außen rot und ausgefranst."""
    lx, ly, _ = ctx.local
    ox, oy, _ = ctx.cube.origin
    sx, sy, _ = ctx.cube.size
    left = ox >= 0.0                                               # die linke Schwinge geht nach +x, die rechte nach -x
    horiz = (lx - ox) / sx
    if not left:
        horiz = 1.0 - horiz                                        # die Schulter liegt immer bei der Nabe
    col_i = max(0, min(len(pattern[0]) - 1, int((1.0 - horiz) * len(pattern[0]))))
    row_i = max(0, min(len(pattern) - 1, int((ly - oy) / sy * len(pattern))))
    level = {"o": 0.12, "O": 0.36, "Y": 0.6, "W": 0.84}.get(pattern[row_i][col_i])
    if level is None:
        return None
    return _glow(ctx, "flame", level)


def _wing_inner(ctx: Ctx):
    return _wing(ctx, _WING_IN)


def _wing_outer(ctx: Ctx):
    return _wing(ctx, _WING_OUT)


def materials(variant: str) -> dict:
    return {"skin": _skin_plain, "skin_tattoo": _skin_tattoo, "hand": _hand, "face": _face, "mask": _mask, "gold": _gold,
            "circlet": _circlet, "ray": _ray, "collar": _collar, "robe_top": _robe_top, "robe_mid": _robe_mid, "robe_low": _robe_low,
            "bodice": _bodice, "sash": _sash, "cape": _cape_top, "cape_low": _cape_low, "hair_flame": _hair_flame,
            "hem_flame": _hem_flame, "lock": _lock, "opal": _opal, "bracer": _bracer, "fireball": _fireball,
            "fireball_core": _fireball_core, "whip_handle": _whip_handle, "lash": _lash, "shard": _shard, "shard_tip": _shard_tip,
            "wing_inner": _wing_inner, "wing_outer": _wing_outer}
