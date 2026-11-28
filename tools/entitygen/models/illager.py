"""
Die Gesetzlosen (statt der Illager) auf dem Skelett des Vanilla-Illagers – gleiche Teile und Drehpunkte (Kopf mit
Überlager "hat", verschränkte Arme "arms" mit "left_shoulder", Arme, Beine), damit Armbrust, Axt, Zauber, Jubel, Reiten
und verschränkte Arme genau wie bei Vanilla sitzen. Anders als die Illager sind es Menschen: ein normaler Kopf mit
eigenem Gesicht, ein Leib in Wams, Gambeson, Robe oder Pelz. Ein Modell für die ganze Familie – das Zubehör (Kapuzen,
Mützen, Bärte, Mäntel, Roben, Schürze, Köcher) steckt in eigenen Knochen und bleibt bei den anderen durchsichtig.

  * Wegelagerer (statt Plünderer, mit der Armbrust):
      - wald:   grüne Gugel mit Zipfel und Schulterkragen, Lederwams über grünem Kittel, Bolzenköcher, Narbe
      - steppe: Turban und Gesichtsschleier, gesteppter Gambeson in Ocker, rote Schärpe, Kajal um die Augen
      - nord:   Fellmütze mit Ohrenklappen, geflochtener Bart, Pelzumhang, blaugraues Wollhemd
  * Schlächter (statt Diener, mit der Axt):
      - henker:    schwarze Spitzhaube mit Augenschlitzen, nackte Arme, Lederschürze mit Blutflecken
      - berserker: wilde rote Mähne, Waidbemalung im Gesicht, Bärenfell über den Schultern, Lederriemen
  * Beschwörer (statt Magier, ruft Plagegeister und Fangzähne):
      - schatten: tiefe Kapuze, aus der nur zwei violette Augen leuchten, schwarzviolette Robe mit goldenen Runen
      - runen:    kahler Runenpriester mit leuchtenden Zeichen auf Stirn und Robe, langer weißer Bart, Knochenkragen
  * Gaukler (statt Illusionist): Narrenkappe mit zwei Zipfeln und Schellen, weiße Halbmaske, Rautenmuster in Blau und
    Karmin
"""
from __future__ import annotations

import math

from ..geometry import Model
from ..paint import Ctx, hash01, mix, noise3, pick, ramp, rel_y


def _pal(**kw) -> dict:
    pal = {k: ramp(*v) if isinstance(v, tuple) else v for k, v in kw.items()}
    pal.setdefault("back", pal["skin"])
    return pal


_FAIR = ("#5a3a2c", "#f0c8a4")
_TAN = ("#4a2c1a", "#d8a070")
_RUDDY = ("#5a2c22", "#eab494")

PALETTES = {
    "wegelagerer_wald": _pal(skin=_RUDDY, hair=("#1e140c", "#7a5432"), iris=("#1a2a10", "#6a8a3a"),
                             cloth=("#142410", "#4e6e2c"), cloth2=("#2a2418", "#7a6a48"), leather=("#24160c", "#9a6a3e"),
                             tunic=("#24300e", "#7a8a3a"),
                             metal=("#2a2a2a", "#c8c4b8"), trim=("#3a2a10", "#c09a4a"), boot=("#160e08", "#5a4028"),
                             wrap=("#3a3424", "#a89a74"), fletch=("#5a1a10", "#e0d4b8")),
    "wegelagerer_steppe": _pal(skin=_TAN, hair=("#0e0a08", "#4a3a2a"), iris=("#1a1006", "#7a4a1e"),
                               cloth=("#4a3418", "#d8b070"), cloth2=("#2a1e14", "#7a5a3a"), leather=("#2a1a0e", "#9a6a3a"),
                               metal=("#3a2a10", "#e0c070"), trim=("#4a0e0a", "#d0402a"), boot=("#1e140a", "#6a4a2a"),
                               veil=("#0c1634", "#5a7ac8"), kohl=("#0a0806", "#2a2018")),
    "wegelagerer_nord": _pal(skin=_FAIR, hair=("#5a3a12", "#f0c870"), iris=("#14243a", "#6a9ad0"),
                             cloth=("#1a2430", "#5a7088"), cloth2=("#2a2420", "#7a6a5a"), leather=("#24180e", "#8a6038"),
                             metal=("#2a2a2a", "#b8b8b0"), trim=("#2a1a0a", "#b08a4a"), boot=("#1a120c", "#5a4434"),
                             fur=("#2a1c12", "#b08a5e"), furlight=("#5a4a3a", "#e8dcc8"), crown=("#3a0e0a", "#a8382a")),
    "schlaechter_henker": _pal(skin=_RUDDY, hair=("#140e0a", "#4a3424"), iris=("#2a0a06", "#8a3a1a"),
                               cloth=("#0e0c0c", "#4a4242"), cloth2=("#1a1612", "#4a4038"), leather=("#1e120a", "#7a5232"),
                               metal=("#1e1e1e", "#9a968e"), trim=("#2a0a08", "#6a1a12"), boot=("#0e0a08", "#3a2c22"),
                               blood=("#2a0402", "#7a0e08")),
    "schlaechter_berserker": _pal(skin=_FAIR, hair=("#2a0a04", "#c05a28"), iris=("#14243a", "#7aa8d8"),
                                  cloth=("#2a1e14", "#7a6044"), cloth2=("#2a2016", "#6a5a44"), leather=("#1e140a", "#8a5a30"),
                                  metal=("#3a2a12", "#d0a050"), trim=("#3a2a12", "#c09040"), boot=("#160e0a", "#4a3626"),
                                  fur=("#1a1008", "#7a5630"), furlight=("#3a2a18", "#b08a5a"), paint=("#0a1a4a", "#3a6ad8")),
    "beschwoerer_schatten": _pal(skin=("#2a2028", "#8a7a84"), hair=("#08060a", "#2a2230"), iris=("#3a0a6a", "#e0a0ff"),
                                 cloth=("#0c0812", "#4a3462"), cloth2=("#140e1c", "#3a2c48"), leather=("#140e10", "#4a3a34"),
                                 metal=("#4a3410", "#f0c860"), trim=("#4a3410", "#f0c860"), boot=("#08060a", "#2a2230"),
                                 rune=("#5a1a9a", "#f0b0ff"), bone=("#6a6050", "#ece4cc")),
    "beschwoerer_runen": _pal(skin=("#5a463e", "#d8bca8"), hair=("#8a8a8a", "#f4f4f0"), iris=("#0a3a3a", "#6affe8"),
                              cloth=("#240c0a", "#7a2a1e"), cloth2=("#1a0e0a", "#5a2a1a"), leather=("#1e140e", "#6a4a32"),
                              metal=("#3a3020", "#c8b48a"), trim=("#3a2a1a", "#b0905a"), boot=("#120a08", "#3a2a22"),
                              rune=("#0a5a5a", "#8afff0"), bone=("#6a6050", "#f0e8d0")),
    "gaukler": _pal(skin=("#5a3a30", "#ecc4a8"), hair=("#140c0a", "#4a2a1a"), iris=("#1a1a3a", "#7a8ae0"),
                    cloth=("#0e1a4a", "#4a6ad8"), cloth2=("#4a0814", "#d8304a"), leather=("#24160c", "#7a5432"),
                    metal=("#5a3a08", "#ffd050"), trim=("#5a3a08", "#ffd050"), boot=("#0a0a14", "#3a3a5a"),
                    mask=("#8a8a90", "#fafaf6"), rune=("#2a1a6a", "#c0b0ff")),
}

# Zubehör je Gestalt (alle übrigen Zubehör-Knochen bleiben durchsichtig)
GEAR = {
    "wegelagerer_wald": {"hood", "liripipe", "collar", "coat", "belt", "quiver"},
    "wegelagerer_steppe": {"cap", "collar", "coat", "belt"},
    "wegelagerer_nord": {"cap", "beard", "braid", "cape", "coat", "belt"},
    "schlaechter_henker": {"hood", "hood_point", "apron", "belt"},
    "schlaechter_berserker": {"beard", "braid", "cape", "belt", "coat"},
    "beschwoerer_schatten": {"hood", "robe", "collar", "right_cuff", "left_cuff", "cross_cuffs"},
    "beschwoerer_runen": {"beard_long", "robe", "collar", "right_cuff", "left_cuff", "cross_cuffs"},
    "gaukler": {"hood", "jester", "coat", "collar", "right_cuff", "left_cuff", "cross_cuffs"},
}
ALL_GEAR = set().union(*GEAR.values())


def build() -> Model:
    m = Model("illager", 128, 128)
    # ---------------------------------------------------------------- Kopf
    m.bone("head")
    m.cube("head", (-4, -8, -4), (8, 8, 8), "head")
    m.bone("hat", "head")                                                       # Überlager: Haar, Schleier, Maske
    m.cube("hat", (-4, -8, -4), (8, 8, 8), "hat", inflate=0.45)
    m.bone("nose", "head", pivot=(0, -4, -4))
    m.cube("nose", (-1, 0, -1), (2, 2, 1), "nose")
    # Kapuze (Gugel, Henkershaube, Beschwörerkapuze, Narrenkappe) mit Zipfel, Spitze und Narrenzipfeln
    m.bone("hood", "head")
    m.cube("hood", (-4, -8, -4), (8, 8, 8), "hood", inflate=0.95)
    m.bone("liripipe", "hood", pivot=(0, -6.6, 4.4), rotation=(-50, 0, 0))
    m.cube("liripipe", (-1.3, -1.1, 0), (2.6, 2.4, 4.4), "liripipe")
    m.bone("liripipe_tip", "liripipe", pivot=(0, 0.2, 4.0), rotation=(-30, 0, 0))
    m.cube("liripipe_tip", (-0.8, -0.6, 0), (1.6, 1.6, 5), "liripipe")
    m.bone("hood_point", "hood", pivot=(0, -8.9, 0.6), rotation=(-10, 0, 0))
    m.cube("hood_point", (-3.4, -1.2, -3.4), (6.8, 1.2, 6.8), "hood_point")
    m.cube("hood_point", (-2.2, -2.4, -2.2), (4.4, 1.2, 4.4), "hood_point")
    m.cube("hood_point", (-1.1, -3.8, -1.1), (2.2, 1.4, 2.2), "hood_point")
    m.bone("jester", "hood")
    m.bone("jester_r", "jester", pivot=(-3.2, -8.6, 0), rotation=(0, 0, 52))
    m.cube("jester_r", (-1.4, -6, -1.4), (2.8, 6, 2.8), "jester")
    m.bone("jester_r2", "jester_r", pivot=(0, -6, 0), rotation=(0, 0, 58))
    m.cube("jester_r2", (-1, -4, -1), (2, 4, 2), "jester")
    m.cube("jester_r2", (-0.9, -5.6, -0.9), (1.8, 1.8, 1.8), "bell")
    m.bone("jester_l", "jester", pivot=(3.2, -8.6, 0), rotation=(0, 0, -52))
    m.cube("jester_l", (-1.4, -6, -1.4), (2.8, 6, 2.8), "jester")
    m.bone("jester_l2", "jester_l", pivot=(0, -6, 0), rotation=(0, 0, -58))
    m.cube("jester_l2", (-1, -4, -1), (2, 4, 2), "jester")
    m.cube("jester_l2", (-0.9, -5.6, -0.9), (1.8, 1.8, 1.8), "bell")
    # Mütze: Turban oder Fellmütze mit Ohrenklappen
    m.bone("cap", "head")
    m.cube("cap", (-4.7, -9.3, -4.7), (9.4, 3.6, 9.4), "cap")
    m.cube("cap", (-4.9, -6.2, -2.4), (1, 4.6, 4.8), "flap")
    m.cube("cap", (3.9, -6.2, -2.4), (1, 4.6, 4.8), "flap")
    # Bärte
    m.bone("beard", "head")
    m.cube("beard", (-4, -3.4, -4.7), (8, 4.6, 0.9), "beard")
    m.bone("braid", "beard", pivot=(0, 1.2, -4.5))
    m.cube("braid", (-1.2, 0, -0.4), (2.4, 4.4, 1.2), "braid")
    m.bone("beard_long", "head")
    m.cube("beard_long", (-3.6, -3.4, -4.7), (7.2, 12.6, 1.0), "beard_long")

    # ---------------------------------------------------------------- Leib
    m.bone("body")
    m.cube("body", (-4, 0, -3), (8, 12, 6), "torso")
    m.bone("coat", "body")                                                      # Wams/Gambeson bis über die Hüften
    m.cube("coat", (-4, 0, -3), (8, 16, 6), "coat", inflate=0.45)
    m.bone("robe", "body")                                                      # Robe bis zum Boden (wie Vanilla)
    m.cube("robe", (-4, 0, -3), (8, 20, 6), "robe", inflate=0.5)
    m.bone("collar", "body")                                                    # Schulterkragen, Schal, Knochenkragen
    m.cube("collar", (-5, -0.6, -3.9), (10, 3.4, 7.8), "collar")
    m.bone("cape", "body")                                                      # Pelz über Schultern und Rücken
    m.cube("cape", (-5.2, -0.9, -3.7), (10.4, 2.6, 7.4), "cape_top")
    m.cube("cape", (-4.8, 1.6, 3.4), (9.6, 15, 1.1), "cape_back")
    m.bone("apron", "body")                                                     # Lederschürze des Henkers
    m.cube("apron", (-3.4, 3.5, -3.75), (6.8, 14, 0.6), "apron")
    m.bone("belt", "body")
    m.cube("belt", (-4.6, 8.8, -3.6), (9.2, 1.7, 7.2), "belt")
    m.cube("belt", (1.6, 9.6, -4.3), (2.6, 2.8, 1.2), "pouch")
    m.bone("quiver", "body", pivot=(0.5, 5.5, 3.7), rotation=(0, 0, 24))         # Bolzenköcher quer über dem Rücken
    m.cube("quiver", (-1.6, -5, 0), (3.2, 10, 2.2), "quiver")
    m.cube("quiver", (-1.3, -6.6, 0.3), (2.6, 1.6, 1.6), "fletch")

    # ---------------------------------------------------------------- Arme
    m.bone("arms", pivot=(0, 3, -1), rotation=(-42.97, 0, 0))                   # verschränkte Arme (Vanilla)
    m.cube("arms", (-8, -2, -2), (4, 8, 4), "cross_arm")
    m.cube("arms", (-4, 2, -2), (8, 4, 4), "cross_fore")
    m.bone("left_shoulder", "arms")
    m.cube("left_shoulder", (4, -2, -2), (4, 8, 4), "cross_arm")
    m.bone("cross_cuffs", "arms")                                               # weite Ärmel bei verschränkten Armen
    m.cube("cross_cuffs", (-8.6, 1.6, -2.6), (5.2, 4.8, 5.2), "cuff")
    m.cube("cross_cuffs", (3.4, 1.6, -2.6), (5.2, 4.8, 5.2), "cuff")
    m.bone("right_arm", pivot=(-5, 2, 0))
    m.cube("right_arm", (-3, -2, -2), (4, 12, 4), "arm")
    m.bone("right_cuff", "right_arm")                                           # weite Ärmel der Roben
    m.cube("right_cuff", (-3.5, 4.6, -2.5), (5, 5.4, 5), "cuff")
    m.bone("left_arm", pivot=(5, 2, 0))
    m.cube("left_arm", (-1, -2, -2), (4, 12, 4), "arm")
    m.bone("left_cuff", "left_arm")
    m.cube("left_cuff", (-1.5, 4.6, -2.5), (5, 5.4, 5), "cuff")
    m.bone("right_leg", pivot=(-2, 12, 0))
    m.cube("right_leg", (-2, 0, -2), (4, 12, 4), "leg")
    m.bone("left_leg", pivot=(2, 12, 0))
    m.cube("left_leg", (-2, 0, -2), (4, 12, 4), "leg")
    return m


def hidden(variant: str) -> list[str]:
    """Nur für die Vorschau: Zubehör der anderen Gestalten und die verschränkten Arme (wie im Spiel beim Gehen)."""
    hide = [g for g in ALL_GEAR if g not in GEAR[variant]]
    if variant.startswith("wegelagerer"):
        hide.append("arms")
    else:
        hide += ["right_arm", "left_arm"]
    return hide


# =============================================================================================
# Malen
# =============================================================================================

def _variant(ctx: Ctx) -> str:
    return ctx.params.get("variant")


def _kind(ctx: Ctx) -> str:
    return _variant(ctx).split("_")[0]


def _gear_hidden(ctx: Ctx) -> bool:
    bone = ctx.cube.bone
    while bone is not None:
        if bone.name in ALL_GEAR:
            return bone.name not in GEAR[_variant(ctx)]
        bone = bone.parent
    return False


def _gear(painter):
    """Zubehör-Maler: bei Gestalten ohne dieses Zubehör durchsichtig."""
    def paint(ctx: Ctx):
        return None if _gear_hidden(ctx) else painter(ctx)
    return paint


def _n(ctx: Ctx, scale: float = 0.5, seed: int = 0) -> float:
    x, y, z = ctx.world
    return noise3(x * scale, y * scale, z * scale, seed)


def _lit(ctx: Ctx, v: float) -> float:
    """Einheitliches Licht: oben hell, Rücken und Unterseite dunkler, die rechte Flanke etwas heller."""
    if ctx.face == "top":
        return v + 0.08
    if ctx.face == "bottom":
        return v - 0.1
    if ctx.face == "back":
        return v - 0.05
    if ctx.face == "west":
        return v - 0.02
    return v


def _cloth(ctx: Ctx, key: str = "cloth", base: float = 0.45, seed: int = 11, folds: float = 0.07) -> tuple:
    x, y, z = ctx.world
    n = _n(ctx, 0.45, seed)
    v = base + (n - 0.5) * 0.12 + (hash01(ctx.u, ctx.v, seed) - 0.5) * 0.04
    if folds:
        across = x if ctx.face in ("front", "back") else z
        v += math.sin(across * 1.7 + n * 2.2) * folds * min(1.0, max(0.0, rel_y(ctx) * 1.6))
    return pick(ctx.pal[key], _lit(ctx, v))


def _skin(ctx: Ctx, base: float = 0.56) -> tuple:
    v = base + (_n(ctx, 0.6, 21) - 0.5) * 0.08 + (hash01(ctx.u, ctx.v, 22) - 0.5) * 0.04
    return pick(ctx.pal["skin"], _lit(ctx, v))


def _leather(ctx: Ctx, base: float = 0.45, seed: int = 31) -> tuple:
    v = base + (_n(ctx, 0.7, seed) - 0.5) * 0.14 + (hash01(ctx.u, ctx.v, seed) - 0.5) * 0.05
    return pick(ctx.pal["leather"], _lit(ctx, v))


def _glow(ctx: Ctx, col: tuple, dim: float = 0.3) -> tuple:
    ctx.glow = mix(col, (0, 0, 0, 255), dim)
    return col


def _hair(ctx: Ctx, base: float = 0.45, seed: int = 41) -> tuple:
    across = ctx.s if ctx.face in ("front", "back", "top") else ctx.s + 3
    v = base + (hash01(across, 0, seed) - 0.5) * 0.22 + (hash01(ctx.u, ctx.v, seed + 1) - 0.5) * 0.08
    return pick(ctx.pal["hair"], _lit(ctx, v))


# ---------------------------------------------------------------- Kopf und Gesicht

def _eye(ctx: Ctx, s: int, left: bool, glow: bool = False) -> tuple:
    """Auge über zwei Texel: außen das Weiße, innen die Iris (bei Leuchtaugen beide leuchtend)."""
    inner = s in (2, 5)
    if glow:
        col = pick(ctx.pal["iris"], 0.95 if inner else 0.7)
        return _glow(ctx, col, 0.15 if inner else 0.35)
    if inner:
        return pick(ctx.pal["iris"], 0.55)
    return (236, 230, 220, 255)


def _face(ctx: Ctx):
    v, s, t = _variant(ctx), ctx.s, ctx.t
    if v == "beschwoerer_schatten":
        # das Gesicht liegt im Schatten der Kapuze: fast schwarz, nur die Augen glühen
        if t == 3 and s in (1, 2, 5, 6):
            return _eye(ctx, s, s < 4, glow=True)
        if t == 4 and s in (2, 5):
            return pick(ctx.pal["rune"], 0.35)
        return pick(ctx.pal["skin"], 0.06 + hash01(ctx.u, ctx.v, 51) * 0.05 + (0.05 if t >= 5 else 0.0))
    col = _skin(ctx, 0.58)
    if t == 0 or t == 1 and v not in ("beschwoerer_runen",):
        # Stirn unter dem Haaransatz etwas dunkler
        col = _skin(ctx, 0.5 if t == 0 else 0.54)
    if t == 2 and s in (1, 2, 5, 6):
        brow = pick(ctx.pal["hair"], 0.3 if v != "beschwoerer_runen" else 0.75)
        if v == "schlaechter_berserker" and s in (2, 5):
            brow = pick(ctx.pal["hair"], 0.35)
        return brow
    if t == 3 and s in (1, 2, 5, 6):
        return _eye(ctx, s, s < 4, glow=v == "beschwoerer_runen")
    if t == 3 and s in (0, 7) and v == "wegelagerer_steppe":
        return pick(ctx.pal["kohl"], 0.5)                                       # Kajal bis zur Schläfe
    if t == 4 and s in (1, 2, 5, 6):
        col = _skin(ctx, 0.46)                                                  # Schatten unter den Augen
    if t == 5 and s in (1, 6):
        col = _skin(ctx, 0.5)
    if t == 6 and 2 <= s <= 5:
        col = pick(ctx.pal["skin"], 0.28 if s in (3, 4) else 0.34)              # Mund
    if t == 7 and s in (3, 4):
        col = _skin(ctx, 0.48)
    # Besonderheiten
    if v == "wegelagerer_wald":
        if (s, t) in ((5, 2), (6, 4), (6, 5), (7, 3)):
            return pick(ctx.pal["skin"], 0.82)                                  # helle Narbe quer über die Wange
        if t >= 5 and hash01(ctx.u, ctx.v, 52) > 0.45:
            return pick(ctx.pal["hair"], 0.3)                                   # Stoppelbart
    if v == "wegelagerer_nord" and t >= 5:
        return pick(ctx.pal["hair"], 0.5)
    if v == "schlaechter_henker":
        if t >= 5 and hash01(ctx.u, ctx.v, 53) > 0.35:
            return pick(ctx.pal["hair"], 0.25)
    if v == "schlaechter_berserker":
        if t in (3, 4) and s in (0, 3, 4, 7) or t == 4 and s in (1, 2, 5, 6):
            return pick(ctx.pal["paint"], 0.5 + (0.15 if t == 3 else 0.0))     # blaue Waidbemalung um die Augen
        if t == 1 and s in (3, 4):
            return pick(ctx.pal["paint"], 0.55)
    if v == "beschwoerer_runen":
        if t == 1 and 2 <= s <= 5 or t == 0 and s in (3, 4):
            return _glow(ctx, pick(ctx.pal["rune"], 0.85 if s in (3, 4) else 0.6), 0.25)   # Rune auf der Stirn
        if t == 4 and s in (0, 7):
            return _glow(ctx, pick(ctx.pal["rune"], 0.6), 0.35)
    if v == "gaukler" and t == 6 and s in (2, 5):
        return pick(ctx.pal["skin"], 0.4)                                       # spöttisches Lächeln
    return col


def _head(ctx: Ctx):
    if _gear_hidden(ctx):
        return None
    v, face = _variant(ctx), ctx.face
    if face == "front":
        return _face(ctx)
    if v == "beschwoerer_schatten":
        return pick(ctx.pal["skin"], 0.12)
    bald = v == "beschwoerer_runen"
    if face == "top":
        if bald:
            col = _skin(ctx, 0.62)
            if ctx.s in (3, 4) and ctx.t >= 4:
                return _glow(ctx, pick(ctx.pal["rune"], 0.7), 0.3)               # Rune läuft über den Scheitel
            return col
        return _hair(ctx, 0.42)
    # Seiten und Hinterkopf: Haar oben, Ohren und Nacken
    if bald:
        if face in ("west", "east") and ctx.t in (3, 4) and ctx.s in (3, 4):
            return _skin(ctx, 0.45)                                             # Ohr
        if face == "back" and ctx.t in (2, 3) and ctx.s in (2, 5):
            return _glow(ctx, pick(ctx.pal["rune"], 0.55), 0.35)
        return _skin(ctx, 0.56)
    hair_to = 7 if face == "back" else 2
    if ctx.t <= hair_to:
        return _hair(ctx, 0.38)
    if face in ("west", "east") and ctx.t in (3, 4) and ctx.s in (4, 5):
        return _skin(ctx, 0.45)                                                 # Ohr
    if v in ("wegelagerer_nord",) and ctx.t >= 5:
        return pick(ctx.pal["hair"], 0.48)                                      # Backenbart
    return _skin(ctx, 0.54)


def _nose(ctx: Ctx):
    v = _variant(ctx)
    if v == "beschwoerer_schatten":
        return pick(ctx.pal["skin"], 0.1)
    if v == "wegelagerer_steppe":
        return pick(ctx.pal["veil"], _lit(ctx, 0.5))                           # unter dem Schleier
    if v == "schlaechter_henker":
        return pick(ctx.pal["cloth"], _lit(ctx, 0.3))                          # unter der Haube
    if v == "gaukler":
        return pick(ctx.pal["mask"], 0.85 if ctx.face != "bottom" else 0.6)    # Nase der Halbmaske
    if ctx.face == "bottom":
        return pick(ctx.pal["skin"], 0.3)
    if ctx.face == "top":
        return _skin(ctx, 0.66)
    if v == "schlaechter_berserker" and ctx.face == "front" and ctx.t == 0:
        return pick(ctx.pal["paint"], 0.5)
    return _skin(ctx, 0.6 if ctx.face == "front" else 0.5)


def _hat(ctx: Ctx):
    """Überlager: wildes Haar, Schleier, Maske, Strähnen unter der Fellmütze."""
    v, face, s, t = _variant(ctx), ctx.face, ctx.s, ctx.t
    if v == "wegelagerer_steppe":
        # Schleier über Mund und Nase und das Tuch des Turbans um Kopf und Nacken
        if face == "front":
            if t >= 4 and not (t == 4 and s in (0, 7)):
                v_ = 0.5 - (0.12 if t == 4 else 0.0) - (0.08 if (s + 1) % 3 == 0 and t >= 5 else 0.0)
                return pick(ctx.pal["veil"], v_)                                # Schleier mit senkrechten Falten
            return None
        if face in ("west", "east", "back") and t >= 2:
            band = t + s // 3
            return pick(ctx.pal["veil"], _lit(ctx, 0.46 - (0.14 if band % 3 == 0 else 0.0)))
        return None
    if v == "wegelagerer_nord":
        # blonde Strähnen, die unter der Mütze bis über die Ohren fallen
        if face in ("west", "east") and t <= 5 and (s >= 5 or t <= 3):
            return _hair(ctx, 0.5) if hash01(ctx.u, 63) > 0.15 else None
        if face == "back" and t <= 6:
            return _hair(ctx, 0.46) if t <= 5 or hash01(ctx.u, 64) > 0.4 else None
        return None
    if v == "schlaechter_berserker":
        # wilde Mähne: oben zerzaust, hinten lang, vorn ein paar Strähnen
        if face == "top":
            return _hair(ctx, 0.52)
        if face == "front":
            if t == 0 and s % 3 != 1 or t == 1 and s in (0, 1, 6, 7) or t <= 5 and s in (0, 7):
                return _hair(ctx, 0.48)
            return None
        if face == "back":
            if t == ctx.h - 1 and s % 2 == 1:
                return None
            return _hair(ctx, 0.42)
        if face in ("west", "east"):
            # zerzauste Strähnen: hinten länger als vorn, ungleich lang
            back = s <= 3 if face == "west" else s >= 4
            longest = 8 if back else 4 + (s % 2)
            return _hair(ctx, 0.44) if t < longest - int(hash01(ctx.u, 66) * 1.6) else None
        return None
    if v == "gaukler":
        # weiße Halbmaske über Stirn, Augen und Nase mit rautenförmigen Augenlöchern
        if face == "front" and 1 <= t <= 4:
            if t == 3 and s in (1, 2, 5, 6):
                return None                                                     # Augenlöcher
            if t in (2, 4) and s in (2, 5):
                return pick(ctx.pal["rune"], 0.45)                              # Raute um die Augen
            col = pick(ctx.pal["mask"], 0.82 + (hash01(ctx.u, ctx.v, 67) - 0.5) * 0.06)
            if t == 4 and s in (0, 7):
                return pick(ctx.pal["mask"], 0.6)
            return col
        return None
    return None


def _hood(ctx: Ctx):
    if _gear_hidden(ctx):
        return None
    v, face, s, t = _variant(ctx), ctx.face, ctx.s, ctx.t
    if face == "bottom":
        return None
    if v == "wegelagerer_wald":
        if face == "front" and 1 <= s <= 6 and t >= 1:
            return None                                                         # Gesichtsöffnung
        col = _cloth(ctx, "cloth", 0.44, 71, folds=0.05)
        if face == "front" and (t == 0 or s in (0, 7)):
            col = pick(ctx.pal["cloth"], 0.34)                                  # Saum um die Öffnung
        return col
    if v == "schlaechter_henker":
        col = pick(ctx.pal["cloth"], _lit(ctx, 0.3 + (_n(ctx, 0.6, 72) - 0.5) * 0.1 + (hash01(ctx.u, ctx.v, 73) - 0.5) * 0.04))
        if face == "front":
            if t == 3 and s in (1, 2, 5, 6):
                return None                                                     # Augenschlitze
            if t in (2, 4) and s in (1, 2, 5, 6):
                return pick(ctx.pal["cloth"], 0.16)                             # tiefe Ränder der Schlitze
            if t == 6 and 2 <= s <= 5:
                return pick(ctx.pal["cloth"], 0.22)                             # Atemnaht
        if face in ("west", "east", "back") and t == ctx.h - 1:
            return pick(ctx.pal["cloth"], 0.2)
        return col
    if v == "beschwoerer_schatten":
        if face == "front" and 2 <= s <= 5 and t >= 2:
            return None                                                         # tiefe, schmale Öffnung
        col = _cloth(ctx, "cloth", 0.38, 74, folds=0.06)
        if face == "front" and (t == 1 or s in (1, 6)) and t >= 1:
            col = pick(ctx.pal["trim"], 0.55 if (s + t) % 2 else 0.4)            # goldener Saum
        if face == "front" and (s in (0, 7) or t == 0):
            col = pick(ctx.pal["cloth"], 0.3)
        return col
    if v == "gaukler":
        if face == "front" and 1 <= s <= 6 and t >= 1:
            return None
        # zweifarbig: rechte Hälfte blau, linke karmin
        x = ctx.world[0]
        key = "cloth" if x < 0 else "cloth2"
        col = pick(ctx.pal[key], _lit(ctx, 0.5 + (_n(ctx, 0.5, 75) - 0.5) * 0.1))
        if face == "front" and (t == 0 or s in (0, 7)):
            col = pick(ctx.pal["trim"], 0.6 if (s + t) % 2 == 0 else 0.42)       # goldene Borte
        return col
    return None


def _liripipe(ctx: Ctx):
    if _gear_hidden(ctx):
        return None
    if ctx.face in ("front",) and ctx.cube.bone.name == "liripipe":
        return None
    return _cloth(ctx, "cloth", 0.4, 76, folds=0.0)


def _hood_point(ctx: Ctx):
    if _gear_hidden(ctx):
        return None
    return pick(ctx.pal["cloth"], _lit(ctx, 0.3 + (hash01(ctx.u, ctx.v, 77) - 0.5) * 0.05))


def _jester(ctx: Ctx):
    if _gear_hidden(ctx):
        return None
    right = ctx.cube.bone.name.startswith("jester_r")
    key = "cloth" if right else "cloth2"
    v = 0.5 + (_n(ctx, 0.6, 78) - 0.5) * 0.1
    if ctx.face == "bottom":
        v -= 0.12
    return pick(ctx.pal[key], _lit(ctx, v))


def _bell(ctx: Ctx):
    if _gear_hidden(ctx):
        return None
    v = 0.62 + (0.25 if ctx.face == "top" or (ctx.s, ctx.t) == (0, 0) else 0.0) - (0.25 if ctx.face == "bottom" else 0.0)
    return pick(ctx.pal["trim"], v)


def _cap(ctx: Ctx):
    if _gear_hidden(ctx):
        return None
    v, face = _variant(ctx), ctx.face
    if face == "bottom":
        return None
    if v == "wegelagerer_steppe":
        # Turban aus Indigotuch: schräg gewickelte Bahnen mit dunklen Kanten, oben ein Knoten
        if face == "top":
            x, _, z = ctx.local
            r = math.hypot(x, z)
            ring = int(r * 0.9) % 2
            return pick(ctx.pal["veil"], 0.62 - (0.1 if ring else 0.0) - (0.08 if r < 1.2 else 0.0))
        band = ctx.t + (ctx.s // 3)                            # Bahnen laufen schräg um den Kopf
        edge = band % 3 == 0
        return pick(ctx.pal["veil"], _lit(ctx, 0.5 - (0.16 if edge else 0.0) + (0.05 if band % 3 == 1 else 0.0)))
    # Fellmütze: Krone aus rotem Tuch, unten ein dicker Fellrand
    if face == "top":
        x, _, z = ctx.local
        if max(abs(x), abs(z)) > 3.6:
            return _fur(ctx, 0.56)
        seam = abs(x) < 0.5 or abs(z) < 0.5
        return pick(ctx.pal["crown"], 0.5 - (0.14 if seam else 0.0) + (hash01(ctx.u, ctx.v, 81) - 0.5) * 0.06)
    if ctx.t >= ctx.h - 2:
        return _fur(ctx, 0.56)
    return pick(ctx.pal["crown"], _lit(ctx, 0.44 + (hash01(ctx.u, ctx.v, 82) - 0.5) * 0.06 - (0.1 if ctx.s % 4 == 0 else 0.0)))


def _flap(ctx: Ctx):
    if _gear_hidden(ctx) or _variant(ctx) != "wegelagerer_nord":
        return None
    if ctx.face == "top":
        return None
    return _fur(ctx, 0.5)


def _beard(ctx: Ctx):
    if _gear_hidden(ctx):
        return None
    if ctx.face in ("top",):
        return None
    s, t = ctx.s, ctx.t
    if ctx.face == "front":
        if t == 0 and s in (3, 4):
            return None                                                         # unter der Nase frei
        if t == 1 and 2 <= s <= 5:
            return pick(ctx.pal["skin"], 0.3) if s in (3, 4) else _hair(ctx, 0.5)   # Mund im Bart
        if t >= 3 and s in (0, 7):
            return None if hash01(ctx.u, 91) > 0.3 else _hair(ctx, 0.36)
        if t == ctx.h - 1 and hash01(ctx.u, 92) > 0.55:
            return None
    return _hair(ctx, 0.48 if t <= 1 else 0.42)


def _braid(ctx: Ctx):
    if _gear_hidden(ctx):
        return None
    if ctx.face == "top":
        return None
    t = ctx.t
    if t in (1, 3):
        return pick(ctx.pal["metal" if _variant(ctx) == "schlaechter_berserker" else "leather"], 0.6)   # Ringe/Bänder
    return _hair(ctx, 0.52 - (0.06 if (ctx.s + t) % 2 else 0.0))


def _beard_long(ctx: Ctx):
    if _gear_hidden(ctx):
        return None
    if ctx.face == "top":
        return None
    s, t, h = ctx.s, ctx.t, ctx.h
    if ctx.face == "front":
        if t == 0 and s in (3, 4):
            return None
        if t == 1 and 2 <= s <= 5:
            return pick(ctx.pal["skin"], 0.3) if s in (3, 4) else pick(ctx.pal["hair"], 0.72)
        width = 3.6 * (1.0 - max(0, t - 5) / (h - 3) * 0.75)
        if abs(ctx.local[0]) > width:
            return None                                                         # spitz zulaufender Bart
        if t >= h - 1 and s not in (3, 4):
            return None
        # Strähnen: helle und etwas dunklere Spalten, zur Spitze grauer
        v = 0.78 - (0.12 if s in (1, 4, 6) else 0.0) - t / h * 0.16
        if t == 2 and s in (1, 6):
            v = 0.7                                                             # Schnurrbartenden
        return pick(ctx.pal["hair"], v)
    return pick(ctx.pal["hair"], 0.6 - t / h * 0.12)


# ---------------------------------------------------------------- Leib

def _torso(ctx: Ctx):
    v, face, s, t = _variant(ctx), ctx.face, ctx.s, ctx.t
    x, y, z = ctx.world
    if v == "schlaechter_berserker":
        # nackter Oberkörper mit gekreuzten Lederriemen
        if face in ("front", "back") and (abs(x - (y - 6) * 0.6) < 0.8 or abs(x + (y - 6) * 0.6) < 0.8):
            return _leather(ctx, 0.42)
        col = _skin(ctx, 0.56)
        if face == "front" and t in (3, 4) and s in (1, 2, 5, 6):
            col = _skin(ctx, 0.48)                                              # Brustmuskeln
        if face == "front" and t >= 6 and s in (3, 4):
            col = _skin(ctx, 0.5 if t % 2 else 0.6)                             # Bauch
        return col
    if v == "schlaechter_henker":
        # dunkles Hemd, darüber ein breiter Schulterriemen mit Nieten
        if face in ("front", "back") and abs(x + (y - 5) * 0.55) < 1.0:
            return pick(ctx.pal["metal"], 0.6) if t % 3 == 0 else _leather(ctx, 0.4)
        if face == "front" and t <= 1 and 2 <= s <= 5:
            return _skin(ctx, 0.5)
        return _cloth(ctx, "cloth2", 0.36, 101, folds=0.04)
    if v.startswith("beschwoerer") or v == "gaukler":
        return _cloth(ctx, "cloth2", 0.4, 102)
    # Wegelagerer: Kittel unter dem Wams (sieht man nur am Hals)
    if face == "front" and t <= 1 and 2 <= s <= 5:
        return _skin(ctx, 0.5)
    return _cloth(ctx, "cloth2", 0.42, 103)


def _harlequin(ctx: Ctx, base: float = 0.5) -> tuple:
    """Harlekin-Rauten (4 breit, 6 hoch) in Blau, Karmin und Gold: keine zwei benachbarten Rauten gleich."""
    s, t = ctx.s, ctx.t
    a = (s + 0.5) / 4.0 + (t + 0.5) / 6.0
    b = (s + 0.5) / 4.0 - (t + 0.5) / 6.0
    fa, fb = a % 1.0, b % 1.0
    key = ("cloth", "cloth2", "trim")[int(math.floor(a) + math.floor(b)) % 3]
    v = base + (0.06 if min(fa, fb) > 0.55 else 0.0) - (0.08 if min(fa, 1 - fa, fb, 1 - fb) < 0.1 else 0.0)
    if key == "trim":
        v -= 0.08
    return pick(ctx.pal[key], _lit(ctx, v))


def _coat(ctx: Ctx):
    if _gear_hidden(ctx):
        return None
    v, face, s, t = _variant(ctx), ctx.face, ctx.s, ctx.t
    if v == "schlaechter_berserker":
        # nur ein Fellschurz unter dem Gürtel (der Oberkörper bleibt nackt)
        if face in ("top", "bottom") or t < 11:
            return None
        if t >= ctx.h - 1 and hash01(ctx.u, 116) > 0.5:
            return None
        return _fur(ctx, 0.44)
    if face in ("top", "bottom"):
        return _cloth(ctx, "cloth", 0.5, 111, folds=0.0) if face == "top" else None
    ry = rel_y(ctx)
    hem = t >= ctx.h - 1
    if v == "gaukler":
        # Narrenkittel im Rautenmuster mit gezacktem Saum und Schellen
        if t >= ctx.h - 3:
            depth = abs((s % 4) - 1.5)                         # Zacken: in der Mitte jeder Spitze tiefer
            if t - (ctx.h - 3) > 2 - depth:
                return None
            if t == ctx.h - 1:
                return pick(ctx.pal["trim"], 0.72)             # Schelle an der Spitze
        return _harlequin(ctx)
    if v == "wegelagerer_wald":
        # Lederwams mit Schnürung über grünem Kittel; unter dem Gürtel fällt der Kittel in Bahnen
        if t >= 11:
            col = _cloth(ctx, "tunic", 0.44, 112)
            if hem:
                return pick(ctx.pal["tunic"], 0.26) if hash01(ctx.u, 113) > 0.25 else None
            if face in ("front", "back") and s in (3, 4) and t >= 12:
                return pick(ctx.pal["tunic"], 0.28)                             # Schlitz vorn und hinten
            return col
        if face == "front" and s in (3, 4):
            if t % 2 == 0:
                return pick(ctx.pal["leather"], 0.25)                           # Schnürung
            return pick(ctx.pal["tunic"], 0.5)
        col = _leather(ctx, 0.5)
        if face == "front" and s in (2, 5):
            col = _leather(ctx, 0.38)
        if face in ("front", "back") and t == 0:
            col = _leather(ctx, 0.6)
        if face in ("west", "east") and t % 4 == 3:
            col = _leather(ctx, 0.4)                                            # Nähte an der Seite
        return col
    if v == "wegelagerer_steppe":
        # gesteppter Gambeson: senkrechte Wülste mit Steppnähten dazwischen, quer alle vier Reihen eine Naht
        seam = s % 2 == 0 or t % 5 == 4
        col = pick(ctx.pal["cloth"], _lit(ctx, 0.54 - (0.11 if seam else 0.0) + (0.04 if s % 2 == 1 and t % 5 == 1 else 0.0)))
        if face == "front" and s in (3, 4):
            col = pick(ctx.pal["cloth"], 0.38)
            if s == 4 and t % 3 == 1 and t < 11:
                col = pick(ctx.pal["metal"], 0.72)                              # Messingknöpfe
        if hem:
            col = pick(ctx.pal["cloth"], 0.32)
        return col
    # Nord: Wollhemd mit Brettchenborte am Saum und an der Brust
    col = _cloth(ctx, "cloth", 0.44, 115)
    if t == ctx.h - 2 or face == "front" and t in (1, 2) and 2 <= s <= 5:
        col = pick(ctx.pal["trim"], 0.55 if (s + t) % 2 else 0.4)
    if hem:
        col = pick(ctx.pal["cloth"], 0.28)
    if ry < 0.06:
        col = pick(ctx.pal["cloth"], 0.52)
    return col


def _robe(ctx: Ctx):
    if _gear_hidden(ctx):
        return None
    v, face, s, t = _variant(ctx), ctx.face, ctx.s, ctx.t
    if face == "bottom":
        return None
    if face == "top":
        return _cloth(ctx, "cloth", 0.46, 121, folds=0.0)
    col = _cloth(ctx, "cloth", 0.4, 123, folds=0.08)
    if v == "beschwoerer_schatten":
        # goldene Borte vorn und am Saum, leuchtende Runen auf der Borte
        if face == "front" and s in (3, 4):
            if t % 4 == 2 and t > 4:
                return _glow(ctx, pick(ctx.pal["rune"], 0.8), 0.3)
            return pick(ctx.pal["trim"], 0.5 if s == 3 else 0.42)
        if t >= ctx.h - 2:
            if t == ctx.h - 2 and s % 3 == 1:
                return _glow(ctx, pick(ctx.pal["rune"], 0.75), 0.35)
            return pick(ctx.pal["trim"], 0.46 if t == ctx.h - 2 else 0.3)
        if face == "back" and 6 <= t <= 10 and s in (3, 4):
            return pick(ctx.pal["trim"], 0.45)
        return col
    if v == "beschwoerer_runen":
        # dunkelrote Robe: vorn eine Kette leuchtender Runen, am Saum eine geflochtene Knochenborte
        if face == "front" and s in (3, 4) and 3 <= t < ctx.h - 3:
            glyph = (t - 3) % 5
            on = {0: (3, 4), 1: (3,), 2: (3, 4), 3: (4,), 4: ()}[glyph]
            if s in on:
                return _glow(ctx, pick(ctx.pal["rune"], 0.82), 0.25)
            return pick(ctx.pal["cloth"], 0.24)
        if face == "front" and s in (2, 5) and t >= 3:
            return pick(ctx.pal["trim"], 0.35)
        if t == ctx.h - 3:
            return pick(ctx.pal["trim"], 0.45)                                  # schmale Goldborte
        if t == ctx.h - 2:
            return pick(ctx.pal["bone"], 0.66) if s % 2 == 0 else pick(ctx.pal["cloth"], 0.22)   # Knochenperlen
        if t == ctx.h - 1:
            return pick(ctx.pal["cloth"], 0.2)
        return col
    return col


def _collar(ctx: Ctx):
    if _gear_hidden(ctx):
        return None
    v, face, s, t = _variant(ctx), ctx.face, ctx.s, ctx.t
    if face == "bottom":
        return None
    if v == "wegelagerer_wald":
        # Schulterkragen der Gugel mit gezaddeltem Rand
        if face != "top" and t == ctx.h - 1 and s % 2 == 1:
            return None
        col = _cloth(ctx, "cloth", 0.46, 131, folds=0.0)
        if face != "top" and t == ctx.h - 2:
            col = pick(ctx.pal["cloth"], 0.32)
        return col
    if v == "wegelagerer_steppe":
        # rote Schärpe als Schal um den Hals
        if face == "top" and abs(ctx.local[0]) < 3 and abs(ctx.local[2]) < 2.6:
            return None
        return pick(ctx.pal["trim"], _lit(ctx, 0.48 - (0.12 if (s + t) % 3 == 0 else 0.0)))
    if v == "beschwoerer_schatten":
        # hoher Stehkragen mit Goldrand und Schädelfibel
        if face == "front" and s in (4, 5) and t in (1, 2):
            return pick(ctx.pal["bone"], 0.75 if t == 1 else 0.5)
        if face != "top" and t == 0:
            return pick(ctx.pal["trim"], 0.55)
        return _cloth(ctx, "cloth", 0.32, 133, folds=0.0)
    if v == "beschwoerer_runen":
        # Federmantel über den Schultern, unten eine Reihe Knochenperlen
        if face == "top":
            if abs(ctx.local[0]) < 3 and abs(ctx.local[2]) < 2.6:
                return None
            return pick(ctx.pal["leather"], _lit(ctx, 0.3 + (hash01(ctx.u, ctx.v, 134) - 0.5) * 0.08))
        if t == ctx.h - 1:
            return pick(ctx.pal["bone"], 0.7) if s % 2 == 0 else None
        feather = (s + (t % 2)) % 2
        return pick(ctx.pal["leather"] if feather else ctx.pal["cloth"], _lit(ctx, 0.3 + 0.1 * (t == 0) + 0.06 * feather))
    if v == "gaukler":
        # Zackenkragen mit Schellen
        if face != "top" and t == ctx.h - 1:
            return pick(ctx.pal["trim"], 0.7) if s % 3 == 1 else None
        x = ctx.world[0]
        return pick(ctx.pal["cloth2" if (int(math.floor(x / 2)) % 2) else "cloth"], _lit(ctx, 0.5))
    return None


def _fur(ctx: Ctx, base: float = 0.45, seed: int = 141):
    """Fell aus überlappenden Büscheln: senkrechte Strähnen, unten an jedem Büschel eine helle Spitze, darunter ein
    dunkler Spalt – keine Sprenkel."""
    face = ctx.face
    if face in ("top", "bottom"):
        v = base + (hash01(ctx.u, ctx.v, seed) - 0.5) * 0.16
        return pick(ctx.pal["fur"], _lit(ctx, v + 0.04))
    col_i = ctx.s
    shift = int(hash01(col_i, seed) * 4)                       # Büschel jeder Strähne gegeneinander versetzt
    row = (ctx.t + shift) % 4
    v = base + (hash01(col_i, seed + 1) - 0.5) * 0.08
    if row == 3:
        v -= 0.14                                              # Spalt unter dem Büschel
    elif row == 2:
        v += 0.1                                               # helle Spitze
    elif row == 0:
        v -= 0.03
    return pick(ctx.pal["fur"], _lit(ctx, v))


def _cape_top(ctx: Ctx):
    if _gear_hidden(ctx):
        return None
    if ctx.face == "bottom":
        return None
    if ctx.face == "top" and abs(ctx.local[0]) < 3.2 and abs(ctx.local[2]) < 2.6:
        return None                                                             # Halsöffnung
    if _variant(ctx) == "schlaechter_berserker" and ctx.face == "front" and ctx.s in (3, 4, 5, 6) and ctx.t == 1:
        return pick(ctx.pal["furlight"], 0.85)                                  # Bärenkrallen als Fibel
    return _fur(ctx, 0.5)


def _cape_back(ctx: Ctx):
    if _gear_hidden(ctx):
        return None
    if ctx.face in ("top", "bottom"):
        return None
    ry = rel_y(ctx)
    if ry > 0.9 and hash01(ctx.u, 142) > 0.45:
        return None                                                             # zottiger Rand
    if ctx.face == "front":
        return _leather(ctx, 0.3)                                               # Innenseite: gegerbtes Leder
    return _fur(ctx, 0.46 - ry * 0.08)


def _apron(ctx: Ctx):
    if _gear_hidden(ctx):
        return None
    if ctx.face in ("top", "bottom"):
        return None
    col = _leather(ctx, 0.42, 151)
    if ctx.face == "front":
        if ctx.t == ctx.h - 1:
            return _leather(ctx, 0.3)
        if hash01(int(ctx.world[0] * 1.3), int(ctx.world[1] * 1.3), 152) > 0.8 and ctx.t > 4:
            return pick(ctx.pal["blood"], 0.5 + hash01(ctx.u, ctx.v, 153) * 0.3)  # alte Blutflecken
        if ctx.t == 1 and ctx.s in (1, 5):
            return pick(ctx.pal["metal"], 0.6)                                  # Nieten
    return col


def _belt(ctx: Ctx):
    if _gear_hidden(ctx):
        return None
    v = _variant(ctx)
    if v == "wegelagerer_steppe":
        # breite rote Schärpe mit Fransen an der Seite
        return pick(ctx.pal["trim"], _lit(ctx, 0.48 + (0.06 if ctx.t == 0 else 0.0) - (0.06 if ctx.s % 3 == 0 else 0.0)))
    if ctx.face == "bottom":
        return _leather(ctx, 0.3)
    if ctx.face == "front" and ctx.s in (4, 5):
        big = v.startswith("schlaechter")
        return pick(ctx.pal["metal"], 0.72 if ctx.t == 0 or big and ctx.s == 4 else 0.5)   # Schnalle
    if v == "schlaechter_berserker" and ctx.s % 3 == 0:
        return pick(ctx.pal["metal"], 0.55)                                     # Bronzebeschläge
    return _leather(ctx, 0.4 + (0.08 if ctx.t == 0 else 0.0))


def _pouch(ctx: Ctx):
    if _gear_hidden(ctx):
        return None
    if _variant(ctx) == "wegelagerer_steppe":
        return pick(ctx.pal["trim"], 0.4) if ctx.t == ctx.h - 1 and ctx.s % 2 == 0 else None   # Fransen der Schärpe
    if ctx.face == "top":
        return _leather(ctx, 0.5)
    if ctx.t == 0:
        return _leather(ctx, 0.6)                                               # Klappe
    return _leather(ctx, 0.44)


def _quiver(ctx: Ctx):
    if _gear_hidden(ctx):
        return None
    if ctx.face == "top":
        return pick(ctx.pal["leather"], 0.2)
    col = _leather(ctx, 0.46, 161)
    if ctx.t in (1, 7):
        return pick(ctx.pal["metal"], 0.55)                                     # Beschläge
    if ctx.face == "front" and ctx.s == 1:
        return _leather(ctx, 0.32)                                              # Naht
    return col


def _fletch(ctx: Ctx):
    if _gear_hidden(ctx):
        return None
    if ctx.face == "bottom":
        return None
    # Bolzen mit roten und weißen Federn
    if ctx.face == "top":
        return pick(ctx.pal["fletch"], 0.8 if (ctx.s + ctx.t) % 2 else 0.3)
    return pick(ctx.pal["fletch"], 0.75 if ctx.s % 2 else 0.35) if ctx.t <= 1 else pick(ctx.pal["leather"], 0.55)


# ---------------------------------------------------------------- Arme und Beine

def _hand(ctx: Ctx) -> tuple:
    v = _variant(ctx)
    if v == "beschwoerer_schatten":
        return pick(ctx.pal["skin"], 0.3 + hash01(ctx.u, ctx.v, 171) * 0.06)    # fahle, graue Hände
    if v == "wegelagerer_nord" or v == "schlaechter_henker":
        return _leather(ctx, 0.36)                                              # Handschuhe
    return _skin(ctx, 0.52)


def _sleeve(ctx: Ctx, along: float) -> tuple | None:
    """Ärmel bzw. Arm je Gestalt; along 0 = Schulter, 1 = Hand."""
    v = _variant(ctx)
    if along > 0.8:
        return _hand(ctx)
    if v == "schlaechter_berserker":
        col = _skin(ctx, 0.55)
        if 0.18 < along < 0.26:
            return pick(ctx.pal["metal"], 0.6)                                  # Armring
        if along > 0.6:
            return _leather(ctx, 0.42 if ctx.t % 2 else 0.36)                   # Lederwickel am Unterarm
        if ctx.face == "west" and 0.3 < along < 0.55 and (ctx.s + ctx.t) % 3 == 0:
            return pick(ctx.pal["paint"], 0.45)                                 # Tätowierung
        return col
    if v == "schlaechter_henker":
        if along < 0.12:
            return _cloth(ctx, "cloth2", 0.36, 172, folds=0.0)
        if along > 0.55:
            col = _leather(ctx, 0.4)                                            # lange Stulpen
            return pick(ctx.pal["metal"], 0.55) if ctx.t % 4 == 0 else col
        return _skin(ctx, 0.55 - (0.06 if ctx.face == "back" else 0.0))
    if v == "wegelagerer_wald":
        if along > 0.6:
            return _leather(ctx, 0.4)                                           # Armschiene
        return _cloth(ctx, "cloth", 0.44, 173)
    if v == "wegelagerer_steppe":
        if along > 0.66:
            return pick(ctx.pal["veil"], 0.5 - (0.08 if ctx.t % 2 else 0))     # gewickelte Unterarme
        return _cloth(ctx, "cloth", 0.5, 174, folds=0.03)
    if v == "wegelagerer_nord":
        if along > 0.62:
            return _fur(ctx, 0.5)                                               # Fellstulpen
        return _cloth(ctx, "cloth", 0.44, 175)
    if v == "gaukler":
        right = ctx.world[0] < 0
        return pick(ctx.pal["cloth2" if right else "cloth"], _lit(ctx, 0.5 + (_n(ctx, 0.5, 176) - 0.5) * 0.08))
    return _cloth(ctx, "cloth", 0.38, 177)


def _arm(ctx: Ctx):
    return _sleeve(ctx, rel_y(ctx))


def _cuff(ctx: Ctx):
    if _gear_hidden(ctx):
        return None
    v = _variant(ctx)
    if ctx.face == "top":
        return None
    if ctx.face == "bottom":
        return pick(ctx.pal["cloth"], 0.14)                                     # dunkles Ärmelinneres
    if ctx.t >= ctx.h - 1:
        if v == "gaukler":
            return pick(ctx.pal["trim"], 0.6 if ctx.s % 2 == 0 else 0.42)
        return pick(ctx.pal["trim"], 0.5) if v == "beschwoerer_schatten" else pick(ctx.pal["bone"], 0.55)
    if v == "gaukler":
        right = ctx.world[0] < 0
        return pick(ctx.pal["cloth" if right else "cloth2"], _lit(ctx, 0.5))
    return _cloth(ctx, "cloth", 0.4, 181, folds=0.05)


def _cross_arm(ctx: Ctx):
    """Oberarm der verschränkten Arme (8 hoch, liegt schräg vor der Brust)."""
    along = ctx.t / max(1, ctx.h - 1) * 0.66
    return _sleeve(ctx, along)


def _cross_fore(ctx: Ctx):
    """Verschränkte Unterarme: Ärmel in der Mitte, die Hände an den Enden."""
    v = _variant(ctx)
    x = ctx.local[0]
    if abs(x) > 2.6 and ctx.face in ("front", "top", "bottom", "back"):
        return _hand(ctx)
    if ctx.face in ("west", "east"):
        return _hand(ctx)
    if abs(x) < 0.5 and ctx.face == "front":
        return pick(ctx.pal["cloth"], 0.2) if v not in ("schlaechter_berserker", "schlaechter_henker") else _skin(ctx, 0.4)
    return _sleeve(ctx, 0.5)


def _leg(ctx: Ctx):
    v = _variant(ctx)
    ry = rel_y(ctx)
    boot_from = {"wegelagerer_wald": 0.5, "wegelagerer_steppe": 0.62, "wegelagerer_nord": 0.45,
                 "schlaechter_henker": 0.52, "schlaechter_berserker": 0.55}.get(v, 0.8)
    if ry >= boot_from or ctx.face == "bottom":
        if v == "wegelagerer_nord" and ctx.face != "bottom":
            return _fur(ctx, 0.48) if ry < boot_from + 0.12 else pick(ctx.pal["boot"], _lit(ctx, 0.42))
        if v == "gaukler":
            return pick(ctx.pal["boot"], _lit(ctx, 0.45))
        col = pick(ctx.pal["boot"], _lit(ctx, 0.4 + (hash01(ctx.u, ctx.v, 191) - 0.5) * 0.06))
        if ctx.face == "front" and ctx.t == ctx.h - 1:
            col = pick(ctx.pal["boot"], 0.55)
        if abs(ry - boot_from) < 0.06 and ctx.face != "bottom":
            col = pick(ctx.pal["boot"], 0.6)                                    # Stulpe
        return col
    if v == "wegelagerer_wald":
        # Hose mit Wickelgamaschen
        if ry > 0.3:
            return pick(ctx.pal["wrap"], _lit(ctx, 0.48 - (0.1 if (ctx.t + (ctx.s if ctx.face in ('front', 'back') else 0)) % 3 == 0 else 0)))
        return _cloth(ctx, "cloth2", 0.38, 192, folds=0.0)
    if v == "schlaechter_berserker":
        return _fur(ctx, 0.4) if ry < 0.3 else _cloth(ctx, "cloth2", 0.38, 193, folds=0.0)
    if v == "gaukler":
        # Strumpfhosen in zwei Farben
        right = ctx.world[0] < 0
        return pick(ctx.pal["cloth2" if right else "cloth"], _lit(ctx, 0.46))
    return _cloth(ctx, "cloth2", 0.36, 194, folds=0.0)


def materials(variant: str) -> dict:
    return {
        "head": _head, "hat": _hat, "nose": _nose, "hood": _hood, "liripipe": _liripipe, "hood_point": _hood_point,
        "jester": _jester, "bell": _bell, "cap": _cap, "flap": _flap, "beard": _beard, "braid": _braid,
        "beard_long": _beard_long, "torso": _torso, "coat": _coat, "robe": _robe, "collar": _collar,
        "cape_top": _cape_top, "cape_back": _cape_back, "apron": _apron, "belt": _belt, "pouch": _pouch,
        "quiver": _quiver, "fletch": _fletch, "cross_arm": _cross_arm, "cross_fore": _cross_fore, "arm": _arm,
        "cuff": _cuff, "leg": _leg,
    }
