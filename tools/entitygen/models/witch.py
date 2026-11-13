"""
Hexe auf dem Menschen-Skelett (gleiche Teile und Drehpunkte wie das Vanilla-Menschenmodell, damit sie wie die
Bewohner der Mod geht, Tränke in der Hand hält und Gegenstände trägt). Spitzhut mit breiter Krempe, der in drei Stufen
nach hinten abknickt, Hutband mit Schnalle, krumme Nase mit Warze, lange Haare über den Rücken, Umhang über den
Schultern, Kleid bis zum Boden, Gürtel mit Tränkefläschchen und Kräuterbeutel.

  * hexe_sumpf: Moorhexe – Moosgrün und Lila, Pilze am Hut, grünlich fahle Haut
  * hexe_nacht: Nachthexe – Schwarz und tiefes Violett, Silberschmuck, Mondsichel an der Schnalle
  * hexe_kraut: Kräuterhexe – Rostrot und Braun, Kräuter am Hutband, sommersprossig
"""
from __future__ import annotations

import math

from ..geometry import Model
from ..paint import Ctx, hash01, mix, noise3, pick, ramp, rel_y


def _pal(**kw) -> dict:
    pal = {k: ramp(*v) if isinstance(v, tuple) else v for k, v in kw.items()}
    pal.setdefault("back", pal["skin"])
    return pal


PALETTES = {
    "hexe_sumpf": _pal(skin=("#5a6a4a", "#c4caa0"), hair=("#1e1a12", "#6a6448"), hat=("#1e1428", "#6a4a7e"),
                       band=("#2a3a18", "#7a9a48"), buckle=("#4a4030", "#c8b47a"), dress=("#1c2618", "#5e7448"),
                       shawl=("#2a1a30", "#7a5a8a"), eye=("#2a4a08", "#c8ff5a"), lip=("#3a2a2a", "#8a5a5a"),
                       glass=("#2a5a3a", "#9affc8"), potion=("#3a0a4a", "#e05aff"), leather=("#24180e", "#7a5a3a"),
                       mushroom=("#5a0a08", "#e04a3a"), boot=("#100c0a", "#3a2e24")),
    "hexe_nacht": _pal(skin=("#6a5a5e", "#e6d6d0"), hair=("#08080c", "#3a3848"), hat=("#0a0a10", "#3a3448"),
                       band=("#2a0a3a", "#9a4ac8"), buckle=("#5a5a62", "#e8e8f0"), dress=("#0e0a16", "#4a3a5e"),
                       shawl=("#1a0a26", "#6a3a8a"), eye=("#3a0a5a", "#d08aff"), lip=("#2a0a1a", "#7a2a4a"),
                       glass=("#3a3a5a", "#c8c8ff"), potion=("#0a1a4a", "#5ab4ff"), leather=("#14100e", "#4a3a30"),
                       mushroom=("#2a0a3a", "#9a4ac8"), boot=("#08080a", "#2a2a30")),
    "hexe_kraut": _pal(skin=("#7a5040", "#f0c8a8"), hair=("#3a1608", "#c0602a"), hat=("#24140c", "#7a4a2a"),
                       band=("#3a4a14", "#a8c04a"), buckle=("#5a4010", "#e0b850"), dress=("#3a120c", "#a8442a"),
                       shawl=("#2a1e10", "#8a6a3a"), eye=("#2a3a08", "#b0e050"), lip=("#4a1a14", "#a84a3a"),
                       glass=("#3a4a2a", "#d0f0a0"), potion=("#4a2a04", "#ffb030"), leather=("#24180e", "#8a6038"),
                       mushroom=("#3a4a14", "#a8c04a"), boot=("#140e0a", "#4a3a2a")),
}


def build() -> Model:
    m = Model("witch", 128, 128)
    m.bone("head")
    m.cube("head", (-4, -8, -4), (8, 8, 8), "head")
    m.bone("hat", "head")                                                       # Vanilla-Überlager: Stirnfransen und Haar
    m.cube("hat", (-4, -8, -4), (8, 8, 8), "fringe", inflate=0.4)
    m.bone("nose", "head", pivot=(0, -3, -4), rotation=(-8, 0, 0))              # krumme Nase
    m.cube("nose", (-1, -1, -2), (2, 4, 2), "nose")
    m.bone("wart", "nose", pivot=(0.6, 2.2, -2.0))
    m.cube("wart", (-0.4, -0.4, -0.6), (0.8, 0.8, 0.8), "wart")
    m.bone("long_hair", "head", pivot=(0, -4.6, 4.1), rotation=(-12, 0, 0))     # lange Haare über den Rücken
    m.cube("long_hair", (-4.5, 0, 0), (9, 11, 1), "hair")
    # Spitzhut: Krempe, Kopf und drei nach hinten abknickende Stufen, Hutband
    m.bone("hat_brim", "head", pivot=(0, -7.6, 0))
    m.cube("hat_brim", (-7, -0.5, -7), (14, 1, 14), "brim")
    m.bone("hat_crown", "hat_brim", pivot=(0, -0.5, 0))
    m.cube("hat_crown", (-4.5, -4, -4.5), (9, 4, 9), "crown")
    m.cube("hat_crown", (-4.7, -1.4, -4.7), (9.4, 1.4, 9.4), "band")
    m.bone("hat_tier1", "hat_crown", pivot=(0, -4, 0.6), rotation=(-12, 0, 4))
    m.cube("hat_tier1", (-3, -4, -3), (6, 4, 6), "crown")
    m.bone("hat_tier2", "hat_tier1", pivot=(0, -4, 0.4), rotation=(-18, 0, 4))
    m.cube("hat_tier2", (-2, -3.5, -2), (4, 3.5, 4), "crown")
    m.bone("hat_tip", "hat_tier2", pivot=(0, -3.5, 0.3), rotation=(-28, 0, 6))
    m.cube("hat_tip", (-1, -3, -1), (2, 3, 2), "tip")
    m.bone("hat_deco", "hat_crown")                                             # Pilze bzw. Kräuterbüschel am Hutband
    m.cube("hat_deco", (2.6, -3.2, -5.2), (2, 2, 1), "deco")
    m.cube("hat_deco", (3.6, -2.2, -5.0), (1.4, 1.4, 1), "deco")

    m.bone("body")
    m.cube("body", (-4, 0, -2), (8, 12, 4), "torso")
    m.bone("shawl", "body")                                                     # Umhang über Schultern und Rücken
    m.cube("shawl", (-4.5, -0.3, -2.5), (9, 4, 5), "shawl")
    m.cube("shawl", (-4.5, 3.7, 2.0), (9, 9, 0.6), "shawl_back")
    m.bone("dress", "body")                                                     # Kleid bis zum Boden
    m.cube("dress", (-4.4, 9.5, -2.4), (8.8, 11.6, 4.8), "dress")
    m.bone("belt", "body")                                                      # Gürtel mit Fläschchen und Beutel
    m.cube("belt", (-4.3, 8.6, -2.3), (8.6, 1.4, 4.6), "belt")
    m.cube("belt", (-3.6, 9.6, -3.2), (1.6, 2.4, 1.2), "vial")
    m.cube("belt", (-1.4, 9.8, -3.0), (1.2, 2.0, 1.0), "vial")
    m.cube("belt", (2.0, 9.4, -3.0), (2.4, 2.6, 1.4), "pouch")

    m.bone("right_arm", pivot=(-5, 2, 0))
    m.cube("right_arm", (-3, -2, -2), (4, 12, 4), "arm")
    m.cube("right_arm", (-3.4, 6.4, -2.4), (4.8, 3, 4.8), "cuff")
    m.bone("left_arm", pivot=(5, 2, 0))
    m.cube("left_arm", (-1, -2, -2), (4, 12, 4), "arm")
    m.cube("left_arm", (-1.4, 6.4, -2.4), (4.8, 3, 4.8), "cuff")
    m.bone("right_leg", pivot=(-1.9, 12, 0))
    m.cube("right_leg", (-2, 0, -2), (4, 12, 4), "leg")
    m.bone("left_leg", pivot=(1.9, 12, 0))
    m.cube("left_leg", (-2, 0, -2), (4, 12, 4), "leg")
    return m


def _variant(ctx: Ctx) -> str:
    return ctx.params.get("variant")


def _skin(ctx: Ctx, base: float = 0.55):
    x, y, z = ctx.world
    v = base + (noise3(x * 0.5, y * 0.5, z * 0.5, 501) - 0.5) * 0.12 + (hash01(ctx.u, ctx.v, 502) - 0.5) * 0.05
    if ctx.face == "top":
        v += 0.05
    col = pick(ctx.pal["skin"], v)
    if _variant(ctx) == "hexe_kraut" and hash01(ctx.u, ctx.v, 503) > 0.9:
        col = pick(ctx.pal["hair"], 0.6)                                               # Sommersprossen
    return col


def _fabric(ctx: Ctx, key: str, base: float = 0.45, seed: int = 511, folds: bool = True):
    x, y, z = ctx.world
    n = noise3(x * 0.45, y * 0.25, z * 0.45, seed)
    v = base + (n - 0.5) * 0.14 + (hash01(ctx.u, ctx.v, seed) - 0.5) * 0.06
    if folds:
        v += math.sin((x if ctx.face in ("front", "back") else z) * 1.6 + n * 2.0) * 0.07
    if ctx.face == "top":
        v += 0.06
    return pick(ctx.pal[key], v)


def _head(ctx: Ctx):
    col = _skin(ctx)
    if ctx.face == "top" or ctx.face in ("west", "east", "back") and ctx.t < 4:
        return pick(ctx.pal["hair"], 0.35 + hash01(ctx.u, ctx.v, 521) * 0.2)
    if ctx.face == "back":
        return pick(ctx.pal["hair"], 0.3 + hash01(ctx.u, ctx.v, 522) * 0.2)
    if ctx.face != "front":
        return col
    s, t = ctx.s, ctx.t
    if t == 2 and s in (1, 2, 5, 6):
        return pick(ctx.pal["hair"], 0.3)                                              # Brauen
    if t == 3 and s in (1, 2, 5, 6):
        if s in (2, 5):
            glow = pick(ctx.pal["eye"], 0.8)
            ctx.glow = mix(glow, (0, 0, 0, 255), 0.35)
            return glow
        return pick(ctx.pal["skin"], 0.9)
    if t == 4 and s in (1, 2, 5, 6):
        return pick(ctx.pal["skin"], 0.3)                                              # Schatten unter den Augen
    if t == 6 and 2 <= s <= 5:
        return pick(ctx.pal["lip"], 0.45 if s in (3, 4) else 0.3)                       # schmaler Mund
    if t == 7 and s in (3, 4):
        return pick(ctx.pal["skin"], 0.42)
    return col


def _fringe(ctx: Ctx):
    """Haar-Überlager: Stirnfransen und Strähnen an den Seiten; oben nichts (dort sitzt der Hut)."""
    face, s, t = ctx.face, ctx.s, ctx.t
    if face in ("top", "bottom"):
        return None
    if face == "front":
        if t == 0 or t == 1 and s in (0, 1, 6, 7) or t <= 4 and s in (0, 7):
            return pick(ctx.pal["hair"], 0.38 + hash01(ctx.u, ctx.v, 531) * 0.15)
        return None
    length = 6 + int(hash01(ctx.u, 532) * 3)
    return pick(ctx.pal["hair"], 0.3 + hash01(ctx.u, ctx.v, 533) * 0.25) if t < length else None


def _nose(ctx: Ctx):
    col = _skin(ctx, 0.58)
    if ctx.face == "bottom":
        return pick(ctx.pal["skin"], 0.3)
    if ctx.face == "front" and ctx.t == ctx.h - 1:
        return pick(ctx.pal["skin"], 0.45)
    return col


def _wart(ctx: Ctx):
    return pick(ctx.pal["skin"], 0.3 if ctx.face != "top" else 0.4)


def _hair(ctx: Ctx):
    if ctx.face in ("top",):
        return pick(ctx.pal["hair"], 0.4)
    ry = rel_y(ctx)
    strand = hash01(ctx.s if ctx.face in ("front", "back") else 0, 541)
    if ry > 0.8 + strand * 0.18:
        return None                                                                     # ungleich lange Spitzen
    v = 0.3 + hash01(ctx.u, 542) * 0.25 + (0.1 if ctx.s % 3 == 0 else 0.0)
    return pick(ctx.pal["hair"], v)


def _brim(ctx: Ctx):
    x, y, z = ctx.world
    r = math.hypot(x, z)
    v = 0.36 + (noise3(x * 0.4, 0, z * 0.4, 551) - 0.5) * 0.14
    if ctx.face == "top":
        v += 0.06 - 0.12 * (r < 5.2)                                                  # Schatten um den Hutkopf
    if ctx.face == "bottom":
        v -= 0.1
    if ctx.face in ("top", "bottom") and r > 6.6:
        v -= 0.08                                                                      # ausgefranster Rand
        if hash01(ctx.u, ctx.v, 552) > 0.86:
            return None
    return pick(ctx.pal["hat"], v)


def _crown(ctx: Ctx):
    x, y, z = ctx.world
    v = 0.42 + (noise3(x * 0.5, y * 0.4, z * 0.5, 561) - 0.5) * 0.16
    if ctx.face == "top":
        v += 0.08
    elif ctx.face in ("west", "back"):
        v -= 0.06
    # Flicken und Nähte
    if hash01(int(x * 1.5), int(y * 1.5), int(z * 1.5), 562) > 0.94:
        v -= 0.14
    return pick(ctx.pal["hat"], v)


def _tip(ctx: Ctx):
    return pick(ctx.pal["hat"], 0.48 if ctx.face != "bottom" else 0.3)


def _band(ctx: Ctx):
    if ctx.face in ("top", "bottom"):
        return None
    if ctx.face == "front" and ctx.s in (3, 4, 5, 6):
        # Schnalle (bei der Nachthexe als Mondsichel)
        if _variant(ctx) == "hexe_nacht":
            return pick(ctx.pal["buckle"], 0.85) if (ctx.s, ctx.t) in ((4, 0), (3, 1), (4, 1)) else pick(ctx.pal["band"], 0.3)
        return pick(ctx.pal["buckle"], 0.75 if ctx.s in (3, 6) or ctx.t == 0 else 0.2)
    return pick(ctx.pal["band"], 0.45 + (0.08 if ctx.s % 2 else 0.0))


def _deco(ctx: Ctx):
    v = _variant(ctx)
    if v == "hexe_sumpf":
        # zwei Pilze: roter Hut mit hellen Tupfen
        if ctx.face == "top" or ctx.t == 0:
            return pick(ctx.pal["mushroom"], 0.9) if hash01(ctx.u, ctx.v, 571) > 0.7 else pick(ctx.pal["mushroom"], 0.5)
        return pick(ctx.pal["buckle"], 0.8)
    if v == "hexe_nacht":
        # Rabenfeder bzw. Amethyst
        return pick(ctx.pal["band"], 0.35 + hash01(ctx.u, ctx.v, 572) * 0.4)
    # Kräuterbüschel
    return pick(ctx.pal["mushroom"], 0.35 + hash01(ctx.u, ctx.v, 573) * 0.45)


def _torso(ctx: Ctx):
    face, s, t = ctx.face, ctx.s, ctx.t
    if face == "front" and t <= 1 and 2 <= s <= 5:
        return _skin(ctx, 0.5)                                                         # Ausschnitt
    col = _fabric(ctx, "dress", 0.42, 581)
    if face == "front" and s in (3, 4) and t % 3 == 1 and t < 8:
        return pick(ctx.pal["buckle"], 0.55)                                           # Knöpfe am Mieder
    if face == "front" and t <= 7 and s in (2, 5):
        return pick(ctx.pal["dress"], 0.25)                                            # Schnürung des Mieders
    return col


def _shawl(ctx: Ctx):
    if ctx.face == "bottom":
        return None
    col = _fabric(ctx, "shawl", 0.45, 591)
    if ctx.face != "top" and ctx.t == ctx.h - 1:
        return pick(ctx.pal["shawl"], 0.3) if hash01(ctx.u, 592) > 0.3 else None      # Fransen
    if ctx.face == "front" and ctx.s in (3, 4, 5) and ctx.t == 0:
        return pick(ctx.pal["buckle"], 0.7)                                            # Fibel
    return col


def _shawl_back(ctx: Ctx):
    if ctx.face in ("top", "bottom"):
        return None
    if ctx.face == "back" or ctx.face == "front":
        ry = rel_y(ctx)
        # spitz zulaufendes Tuch: unten nur noch die Mitte
        half = 4.5 * (1.0 - ry * 0.75)
        if abs(ctx.local[0]) > half:
            return None
        if ry > 0.92:
            return pick(ctx.pal["shawl"], 0.3) if hash01(ctx.u, 593) > 0.4 else None
    return _fabric(ctx, "shawl", 0.42, 594)


def _dress(ctx: Ctx):
    if ctx.face in ("top", "bottom"):
        return None
    col = _fabric(ctx, "dress", 0.38, 601)
    if ctx.t >= ctx.h - 1:
        return pick(ctx.pal["dress"], 0.22) if hash01(ctx.u, 602) > 0.35 else None     # zerschlissener Saum
    if ctx.t >= ctx.h - 3 and hash01(ctx.u, ctx.v, 603) > 0.8:
        return pick(ctx.pal["dress"], 0.25)
    return col


def _belt(ctx: Ctx):
    if ctx.face in ("top", "bottom"):
        return pick(ctx.pal["leather"], 0.3)
    v = 0.42 + (0.1 if ctx.s % 4 == 0 else 0.0)
    if ctx.face == "front" and ctx.s in (4, 5):
        return pick(ctx.pal["buckle"], 0.7)
    return pick(ctx.pal["leather"], v)


def _vial(ctx: Ctx):
    ry = rel_y(ctx)
    if ry < 0.25:
        return pick(ctx.pal["leather"], 0.55)                                          # Korken
    if ctx.face == "top":
        return pick(ctx.pal["leather"], 0.6)
    col = pick(ctx.pal["potion"], 0.4 + ry * 0.4)
    if ctx.s == 0 and ctx.face in ("front",):
        col = pick(ctx.pal["glass"], 0.9)                                              # Glanzlicht
    if ry > 0.45:
        ctx.glow = mix(col, (0, 0, 0, 255), 0.4)                                       # der Trank glimmt leise
    return col


def _pouch(ctx: Ctx):
    if ctx.face == "top":
        return pick(ctx.pal["leather"], 0.35)
    if ctx.t == 0:
        return pick(ctx.pal["leather"], 0.62)                                          # Klappe
    return pick(ctx.pal["leather"], 0.45 + (hash01(ctx.u, ctx.v, 611) - 0.5) * 0.1)


def _arm(ctx: Ctx):
    ry = rel_y(ctx)
    if ry > 0.72:
        col = _skin(ctx, 0.5)
        if ctx.face == "bottom":
            return pick(ctx.pal["skin"], 0.35)
        if ctx.face == "front" and ctx.t == ctx.h - 1 and ctx.s in (0, 3):
            return pick(ctx.pal["skin"], 0.3)                                          # lange Fingernägel
        return col
    return _fabric(ctx, "dress", 0.4, 621)


def _cuff(ctx: Ctx):
    if ctx.face in ("top",):
        return None
    if ctx.face == "bottom":
        return pick(ctx.pal["dress"], 0.15)
    col = _fabric(ctx, "shawl", 0.4, 631, folds=False)
    if ctx.t == ctx.h - 1:
        return pick(ctx.pal["shawl"], 0.28) if hash01(ctx.u, 632) > 0.3 else None      # weite, ausgefranste Ärmel
    return col


def _leg(ctx: Ctx):
    ry = rel_y(ctx)
    if ry > 0.7 or ctx.face == "bottom":
        v = 0.3 + (0.12 if ctx.face == "top" else 0.0) + (0.1 if ctx.face == "front" and ctx.t == ctx.h - 1 else 0.0)
        return pick(ctx.pal["boot"], v + hash01(ctx.u, ctx.v) * 0.06)
    return pick(ctx.pal["dress"], 0.25 + hash01(ctx.u, ctx.v, 641) * 0.05)              # Strümpfe unter dem Kleid


def materials(variant: str) -> dict:
    return {
        "head": _head, "fringe": _fringe, "nose": _nose, "wart": _wart, "hair": _hair,
        "brim": _brim, "crown": _crown, "tip": _tip, "band": _band, "deco": _deco,
        "torso": _torso, "shawl": _shawl, "shawl_back": _shawl_back, "dress": _dress,
        "belt": _belt, "vial": _vial, "pouch": _pouch, "arm": _arm, "cuff": _cuff, "leg": _leg,
    }
