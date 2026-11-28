"""
Untote auf dem Menschen-Skelett der Vanilla-Zombies (gleiche Teile und Drehpunkte, damit Rüstung, Gegenstände in der
Hand, Köpfe und alle Zombie-Bewegungen passen). Ein Modell für die ganze Familie – das Zubehör (Unterkiefer, Helm,
Kittelsaum, Kutte) steckt in eigenen Knochen und bleibt auf den Bildern der anderen durchsichtig.

  * Wiedergänger (statt Zombie): Bauer im zerrissenen Kittel, Söldner im rostigen Kettenhemd mit Eisenhut,
    Mönch in der Kutte – fahlgrüne, fleckige Haut, eingefallene Augen mit glimmendem Rot, schlaffer Unterkiefer
  * Mumie (statt Wüstenzombie): in Leinenbinden gewickelt mit Goldkragen, oder als vertrockneter Wüstenwanderer in
    Tuch und Turban – bernsteinfarben glimmende Augen
  * Wasserleiche (statt Ertrunkener): aufgedunsen und blaugrau, Matrose im Streifenhemd oder Fischer in Ölzeug und
    Strickmütze, Seetang und Seepocken, kalt leuchtende Augen

(Die untoten Dorfbewohner tragen das Modell und die Tracht der lebenden Dorfbewohner, siehe
tools/generate_undead_villagers.py.)
"""
from __future__ import annotations

import math

from ..geometry import Model
from ..paint import Ctx, hash01, mix, noise3, pick, ramp, rel_y

_ROT = {"skin": ramp("#2c3a26", "#9aae7c", steps=7), "flesh": ramp("#3a1414", "#a85a52"),
        "bone": ramp("#8a8270", "#f0ead6"), "eye": ramp("#3a0402", "#ff5a3a"), "socket": ramp("#0a0806", "#2a2420")}
_DRY = {"skin": ramp("#2a2016", "#7a6046", steps=7), "flesh": ramp("#2a1a10", "#6a4a30"),
        "bone": ramp("#8a7a60", "#ece0c4"), "eye": ramp("#4a2a02", "#ffc04a"), "socket": ramp("#080604", "#24180e")}
_SOAKED = {"skin": ramp("#1e3440", "#9cc0c4", steps=7), "flesh": ramp("#2a1a2a", "#8a5a6a"),
           "bone": ramp("#7a8a8a", "#e4ece4"), "eye": ramp("#04303a", "#7affe8"), "socket": ramp("#04080c", "#16242c"),
           "kelp": ramp("#0e2a10", "#5aa040"), "barnacle": ramp("#5a5a50", "#e8e4d4")}


def _pal(base: dict, **extra) -> dict:
    pal = dict(base)
    for key, value in extra.items():
        pal[key] = ramp(*value) if isinstance(value, tuple) else value
    pal.setdefault("back", pal["skin"])
    return pal


PALETTES = {
    # Wiedergänger
    "zombie_bauer": _pal(_ROT, cloth=("#3a2a1a", "#9a7a52"), trousers=("#1e1a16", "#5e5446"), rope=("#4a3a22", "#c8aa6a"),
                         hair=("#0e0a08", "#3e2e22"), shoe=("#120e0a", "#4a3a2a")),
    "zombie_soeldner": _pal(_ROT, cloth=("#4a1010", "#b04a3a"), mail=("#24221f", "#a8a49a"), rust=("#4a2208", "#b06a2a"),
                            trousers=("#1a1612", "#4e443a"), metal=("#2a2622", "#8a8278"), hair=("#1a120c", "#5a4430"),
                            shoe=("#140e0a", "#5a4430"), rope=("#3a2a1a", "#8a6a42")),
    "zombie_moench": _pal(_ROT, cloth=("#2a1c14", "#7e6450"), rope=("#5a4a2a", "#d8c08a"), trousers=("#241810", "#5e4a3a"),
                          hair=("#1a120e", "#5e4a3a"), shoe=("#100c08", "#3a2c20")),
    # Mumien
    "husk_mumie": _pal(_DRY, linen=("#5e4e36", "#efe4c8"), gold=("#5a3a08", "#f0c850"), lapis=("#0a1a4a", "#3a6ad0"),
                       stain=("#3a2a14", "#8a6a3a"), hair=("#0e0a08", "#2a2018"), shoe=("#2a2016", "#7a6046")),
    "husk_wuestling": _pal(_DRY, cloth=("#5a3e22", "#d8b47a"), scarf=("#4a1e14", "#c0644a"), trousers=("#4a3420", "#b0905e"),
                           rope=("#3a2414", "#8a6a3a"), hair=("#0e0a08", "#2a2018"), shoe=("#24180e", "#6a4e30"),
                           stain=("#3a2a14", "#8a6a3a"), gold=("#5a3a08", "#f0c850")),
    # Wasserleichen
    "drowned_matrose": _pal(_SOAKED, cloth=("#14243e", "#6a8ab8"), stripe=("#8a9aa4", "#eef2f0"), trousers=("#2a2a24", "#7a7460"),
                            rope=("#3a3a2a", "#a09a7a"), hair=("#0e2a10", "#4a8a3a"), shoe=("#0e1418", "#3a4a50")),
    "drowned_fischer": _pal(_SOAKED, cloth=("#4a4410", "#c8b84a"), knit=("#2a1a2a", "#7a4a6a"), trousers=("#1e2a2a", "#5a6e6a"),
                            rope=("#3a3a2a", "#a09a7a"), hair=("#1a120c", "#5a4a3a"), shoe=("#0e1418", "#3a4a50")),
}

# Zubehör je Bild (Knochen-Namen); was nicht dabei ist, bleibt durchsichtig
GEAR = {
    "zombie_bauer": {"jaw", "skirt"},
    "zombie_soeldner": {"jaw", "helm"},
    "zombie_moench": {"jaw", "robe"},
    "husk_mumie": {"skirt"},
    "husk_wuestling": {"jaw", "robe"},
    "drowned_matrose": {"jaw"},
    "drowned_fischer": {"jaw", "skirt"},
}
ALL_GEAR = set().union(*GEAR.values())

# Was der Hut-Knochen (das Vanilla-Überlager) zeigt
HAT = {"zombie_bauer": "hair", "zombie_soeldner": "coif", "zombie_moench": "hood", "husk_mumie": "wraps",
       "husk_wuestling": "turban", "drowned_matrose": "kelp", "drowned_fischer": "knit"}


def build() -> Model:
    m = Model("undead", 128, 64)
    m.bone("head")
    m.cube("head", (-4, -8, -4), (8, 8, 8), "head")
    m.bone("hat", "head")
    m.cube("hat", (-4, -8, -4), (8, 8, 8), "hat", inflate=0.5)
    m.bone("jaw", "head", pivot=(0, -0.6, -2.0), rotation=(12, 0, 0))              # schlaffer Unterkiefer unterm Gesicht
    m.cube("jaw", (-3, 0, -2.4), (6, 1.8, 2.4), "jaw")
    m.bone("helm", "head")                                                         # Eisenhut des Söldners
    m.cube("helm", (-4.6, -9.4, -4.6), (9.2, 3.4, 9.2), "helm")
    m.cube("helm", (-6, -6.4, -6), (12, 0.8, 12), "helm")

    m.bone("body")
    m.cube("body", (-4, 0, -2), (8, 12, 4), "torso")
    m.bone("skirt", "body")                                                        # Kittelsaum bzw. Schurz über den Hüften
    m.cube("skirt", (-4.3, 9.5, -2.3), (8.6, 5, 4.6), "skirt")
    m.bone("robe", "body")                                                         # Kutte bzw. Wüstengewand bis zu den Knöcheln
    m.cube("robe", (-4.4, 9.5, -2.4), (8.8, 11.5, 4.8), "robe")

    m.bone("right_arm", pivot=(-5, 2, 0))
    m.cube("right_arm", (-3, -2, -2), (4, 12, 4), "arm")
    m.bone("left_arm", pivot=(5, 2, 0))
    m.cube("left_arm", (-1, -2, -2), (4, 12, 4), "arm")
    m.bone("right_leg", pivot=(-1.9, 12, 0))
    m.cube("right_leg", (-2, 0, -2), (4, 12, 4), "leg")
    m.bone("left_leg", pivot=(1.9, 12, 0))
    m.cube("left_leg", (-2, 0, -2), (4, 12, 4), "leg")
    return m


# =============================================================================================
# Malen
# =============================================================================================

def _variant(ctx: Ctx) -> str:
    return ctx.params.get("variant")


def _hidden(ctx: Ctx) -> bool:
    bone = ctx.cube.bone
    while bone is not None:
        if bone.name in ALL_GEAR:
            return bone.name not in GEAR[_variant(ctx)]
        bone = bone.parent
    return False


def _kind(ctx: Ctx) -> str:
    v = _variant(ctx)
    return "mummy" if v == "husk_mumie" else "dry" if v.startswith("husk") else "drowned" if v.startswith("drowned") else "zombie"


def _skin(ctx: Ctx, base: float = 0.5):
    """Verwesende Haut: fleckig, mit dunklen Adern, Blutergüssen; Wasserleichen mit Seepocken."""
    x, y, z = ctx.world
    n = noise3(x * 0.6, y * 0.6, z * 0.6, 301)
    v = base + (n - 0.5) * 0.3 + (hash01(ctx.u, ctx.v, 302) - 0.5) * 0.08
    if ctx.face == "top":
        v += 0.05
    elif ctx.face == "bottom":
        v -= 0.1
    col = pick(ctx.pal["skin"], v)
    bruise = noise3(x * 0.35 + 7, y * 0.35, z * 0.35, 303)
    if bruise > 0.7:
        col = mix(col, pick(ctx.pal["flesh"], 0.3), 0.4)
    vein = abs(math.sin(x * 1.7 + y * 0.9 + noise3(x * 0.3, y * 0.3, z * 0.3, 304) * 6.0))
    if vein < 0.05 and hash01(ctx.u, ctx.v, 305) > 0.45:
        col = mix(col, pick(ctx.pal["skin"], 0.1), 0.5)
    if _kind(ctx) == "drowned" and hash01(int(x * 2), int(y * 2), int(z * 2), 306) > 0.94:
        col = pick(ctx.pal["barnacle"], 0.55 + hash01(ctx.u, ctx.v) * 0.3)          # Seepocken
    return col


def _wound(ctx: Ctx, col):
    """Wenige offene Stellen mit rohem Fleisch und hellem Knochen."""
    x, y, z = ctx.world
    w = noise3(x * 0.45 + 31, y * 0.45, z * 0.45, 307)
    if w > 0.78:
        if w > 0.83 and hash01(ctx.u, ctx.v, 308) > 0.55:
            return pick(ctx.pal["bone"], 0.6)
        return pick(ctx.pal["flesh"], 0.35 + (w - 0.78) * 2.5)
    return col


def _cloth(ctx: Ctx, key: str, base: float = 0.45, tear: float = 0.0, seed: int = 311):
    """Grobes Tuch mit Falten, Flecken und (bei tear > 0) Rissen, durch die die Haut scheint."""
    x, y, z = ctx.world
    n = noise3(x * 0.5, y * 0.3, z * 0.5, seed)
    v = base + math.sin(x * 1.4 + n * 3.0) * 0.08 + (n - 0.5) * 0.16 + (hash01(ctx.u, ctx.v, seed) - 0.5) * 0.08
    if ctx.face == "top":
        v += 0.06
    if tear > 0:
        t = noise3(x * 0.55 + 13, y * 0.55, z * 0.55, seed + 1)
        if t > 1.0 - tear:
            return None                                                                # Loch im Stoff
        if t > 1.0 - tear - 0.05:
            v -= 0.18                                                                  # ausgefranster Rand
    dirt = noise3(x * 0.25, y * 0.25 + 40, z * 0.25, seed + 2)
    col = pick(ctx.pal[key], v)
    if dirt > 0.68:
        col = mix(col, pick(ctx.pal["skin"], 0.2), 0.3)
    return col


def _mail(ctx: Ctx):
    """Kettenhemd: Reihen kleiner Ringe (hell oben, dunkel unten), versetzt; Rostflecken."""
    s, t = ctx.s, ctx.t
    if t % 2 == 0:
        v = 0.7 if (s + t // 2) % 2 == 0 else 0.46
    else:
        v = 0.22 if (s + t // 2) % 2 == 0 else 0.36
    col = pick(ctx.pal["mail"], v + (hash01(ctx.u, ctx.v, 321) - 0.5) * 0.06)
    x, y, z = ctx.world
    if noise3(x * 0.5, y * 0.5, z * 0.5, 322) > 0.64:
        col = mix(col, pick(ctx.pal["rust"], 0.45 + hash01(ctx.u, ctx.v) * 0.2), 0.55)
    return col


def _bandage(ctx: Ctx):
    """Leinenbinden: leicht schräge Lagen von zwei Pixeln, jede Lage etwas anders getönt, die untere Kante als Fuge;
    vergilbte Flecken, nur selten eine Lücke auf die vertrocknete Haut."""
    x, y, z = ctx.world
    across = x if ctx.face in ("front", "back") else z if ctx.face in ("west", "east") else x + z
    band = y + across * 0.28
    idx = math.floor(band / 2.0)
    frac = band - idx * 2.0
    v = 0.66 + (hash01(idx, int(ctx.cube.origin[0] * 10), 331) - 0.5) * 0.16
    if frac > 1.45:
        v -= 0.24                                                                      # Fuge zur nächsten Lage
    elif frac < 0.4:
        v += 0.06
    col = pick(ctx.pal["linen"], v + (hash01(ctx.u, ctx.v, 332) - 0.5) * 0.05)
    stain = noise3(x * 0.3, y * 0.3, z * 0.3, 333)
    if stain > 0.66:
        col = mix(col, pick(ctx.pal["stain"], 0.6), min(0.5, (stain - 0.66) * 3.0))
    gap = noise3(x * 0.55 + 9, y * 0.55, z * 0.55, 334)
    if gap > 0.84 and frac > 0.9:
        return _skin(ctx, 0.3)
    return col


# ---------------------------------------------------------------------------------------------
# Teile
# ---------------------------------------------------------------------------------------------

def _head(ctx: Ctx):
    kind = _kind(ctx)
    col = _bandage(ctx) if kind == "mummy" else _skin(ctx, 0.52)
    if ctx.face != "front":
        if kind not in ("mummy",) and ctx.face in ("west", "east", "back") and ctx.t < 3:
            return pick(ctx.pal["hair"], 0.35 + hash01(ctx.u, ctx.v, 341) * 0.15)      # Haaransatz
        if ctx.face == "top" and kind != "mummy":
            return pick(ctx.pal["hair"], 0.3 + hash01(ctx.u, ctx.v, 342) * 0.2)
        return _wound(ctx, col) if kind == "zombie" else col
    s, t = ctx.s, ctx.t
    # Gesicht (8x8): Brauenwulst, tiefe Augenhöhlen mit glimmenden Pupillen, Nasenlöcher, Oberkiefer mit Zähnen
    if t == 2 and s in (1, 2, 5, 6):
        return pick(ctx.pal["linen"] if kind == "mummy" else ctx.pal["skin"], 0.28)
    if t in (3, 4) and s in (1, 2, 5, 6):
        if t == 3 and s in (2, 5):
            glow = pick(ctx.pal["eye"], 0.85)
            ctx.glow = glow
            return glow
        return pick(ctx.pal["socket"], 0.35 if t == 3 else 0.6)
    if t == 5 and s in (3, 4):
        return pick(ctx.pal["socket"], 0.3) if kind != "mummy" else pick(ctx.pal["linen"], 0.4)  # Nasenlöcher
    if kind == "mummy":
        if t == 6 and 2 <= s <= 5:
            return pick(ctx.pal["socket"], 0.4)                                           # Schlitz in den Binden
        return col
    if t == 6 and 1 <= s <= 6:
        return pick(ctx.pal["socket"], 0.25) if s in (1, 6) else pick(ctx.pal["bone"], 0.72 if s % 2 else 0.55)
    if t == 7 and 1 <= s <= 6:
        return pick(ctx.pal["socket"], 0.15)                                              # offener Mund
    return col


def _hat(ctx: Ctx):
    """Vanilla-Überlager des Kopfes: Haare, Kapuze, Kettenhaube, Binden, Turban, Seetang oder Strickmütze."""
    style = HAT[_variant(ctx)]
    face, s, t, w, h = ctx.face, ctx.s, ctx.t, ctx.w, ctx.h
    if style == "hair":
        if face == "top":
            return pick(ctx.pal["hair"], 0.35 + hash01(ctx.u, ctx.v, 351) * 0.25) if hash01(ctx.u, ctx.v, 352) > 0.18 else None
        if face == "front":
            return pick(ctx.pal["hair"], 0.3) if t == 0 and hash01(s, 353) > 0.35 else None
        length = 2 + int(hash01(ctx.u, 354) * 4) + (1 if face == "back" else 0)          # Strähnen ungleich lang
        return pick(ctx.pal["hair"], 0.3 + hash01(ctx.u, ctx.v, 355) * 0.2) if t < length else None
    if style == "hood":
        if face == "front":
            if t == 0 or s in (0, w - 1):
                return pick(ctx.pal["cloth"], 0.22)                                        # Kapuzenrand um das Gesicht
            return None
        col = _cloth(ctx, "cloth", 0.42, tear=0.1, seed=356)
        if col is not None and face == "back" and t >= h - 2:
            return pick(ctx.pal["cloth"], 0.28)
        return col
    if style == "coif":
        # Kettenhaube: Hinterkopf und Seiten aus Kettengeflecht, vorn um das Gesicht offen; oben sitzt der Eisenhut
        if face == "front":
            return _mail(ctx) if t == 0 or (s in (0, w - 1) and t >= 5) else None
        if face == "top":
            return None
        return _mail(ctx)
    if style == "wraps":
        # lose Bindenenden, die vom Kopf abstehen
        if face == "front":
            return pick(ctx.pal["linen"], 0.72) if (t == 1 and s in (0, 1, 2)) or (t == 6 and s >= 6) else None
        if face == "top":
            return None
        return pick(ctx.pal["linen"], 0.6 + hash01(ctx.u, ctx.v) * 0.12) if hash01(ctx.u // 2, ctx.v, 357) > 0.82 else None
    if style == "turban":
        # Turban und Gesichtstuch des Wüstenwanderers: nur die Augenpartie bleibt frei
        if face == "bottom":
            return None
        if face == "front":
            if 3 <= t <= 4:
                return None                                                                # Augenschlitz
            if t >= 5:
                # Gesichtstuch aus hellem Wüstenstoff, unten ausgefranst
                if t == h - 1 and hash01(s, 358) < 0.3:
                    return None
                return pick(ctx.pal["cloth"], 0.52 - (0.14 if (s + t) % 4 == 0 else 0.0) - (0.1 if t == 5 else 0.0))
            if t == 1 and s in (3, 4):
                return pick(ctx.pal["gold"], 0.75 if s == 3 else 0.55)                    # Spange
        # gewickelte Bahnen: schräg versetzte Falten mit dunkler Fuge
        band = (t + (ctx.u // 3)) % 3
        col = pick(ctx.pal["scarf"], (0.6, 0.46, 0.3)[band] + (hash01(ctx.u, ctx.v, 359) - 0.5) * 0.06)
        if face == "back" and t >= h - 2:
            return pick(ctx.pal["scarf"], 0.36) if hash01(s, 360) > 0.35 else None        # herabhängendes Tuchende
        return col
    if style == "kelp":
        # Seetang im Haar: lange grüne Strähnen
        if face == "top":
            return pick(ctx.pal["kelp"], 0.35 + hash01(ctx.u, ctx.v, 358) * 0.3) if hash01(ctx.u, ctx.v, 359) > 0.3 else None
        if face == "front":
            return pick(ctx.pal["kelp"], 0.45) if t <= 1 and hash01(s, 360) > 0.4 else None
        length = 3 + int(hash01(ctx.u, 361) * 5)
        return pick(ctx.pal["kelp"], 0.3 + hash01(ctx.u, ctx.v, 362) * 0.3) if t < length else None
    if style == "knit":
        # Strickmütze des Fischers mit umgeschlagenem Rand, darunter nasse Haarsträhnen
        if face == "top":
            return pick(ctx.pal["knit"], 0.5 + (0.1 if ctx.s % 2 else 0.0))
        if t <= 2:
            rib = 0.42 + (0.14 if s % 2 else 0.0)
            return pick(ctx.pal["knit"], rib - (0.12 if t == 2 else 0.0))
        if face != "front" and t <= 4:
            return pick(ctx.pal["hair"], 0.35) if hash01(ctx.u, 363) > 0.35 else None
        return None
    return None


def _jaw(ctx: Ctx):
    if _hidden(ctx):
        return None
    if ctx.face == "top":
        # von oben: dunkler Mundraum, vorn die Zahnreihe
        return pick(ctx.pal["bone"], 0.68 if ctx.s % 2 == 0 else 0.5) if ctx.t == ctx.h - 1 else pick(ctx.pal["socket"], 0.2)
    if ctx.face == "front":
        if ctx.t == 0:
            return pick(ctx.pal["bone"], 0.66 if ctx.s % 2 == 0 else 0.48) if 1 <= ctx.s <= 4 else pick(ctx.pal["socket"], 0.3)
        return _skin(ctx, 0.4)
    return _skin(ctx, 0.4)


def _helm(ctx: Ctx):
    if _hidden(ctx):
        return None
    x, y, z = ctx.world
    v = 0.5 + (noise3(x * 0.6, y * 0.6, z * 0.6, 371) - 0.5) * 0.3
    if ctx.face == "top":
        v += 0.12
    col = pick(ctx.pal["metal"], v)
    if noise3(x * 0.4, y * 0.4, z * 0.4, 372) > 0.58:
        col = mix(col, pick(ctx.pal["rust"], 0.5), 0.6)
    if ctx.cube.size[1] < 1.5 and ctx.face in ("west", "east", "front", "back"):
        col = pick(ctx.pal["metal"], 0.3)
    elif ctx.face != "top" and ctx.t == ctx.h - 1:
        col = pick(ctx.pal["metal"], 0.25)                                                 # Kante zur Krempe
    return col


def _torso(ctx: Ctx):
    v = _variant(ctx)
    kind = _kind(ctx)
    face, s, t = ctx.face, ctx.s, ctx.t
    if kind == "mummy":
        if t <= 2 and face in ("front", "back", "west", "east"):
            # Goldkragen mit Lapis-Perlen
            if face == "front" and t == 1 and s % 3 == 1:
                return pick(ctx.pal["lapis"], 0.6)
            return pick(ctx.pal["gold"], 0.5 + (0.2 if (s + t) % 2 == 0 else 0.0) - 0.15 * (t == 2))
        return _bandage(ctx)
    if v == "zombie_soeldner":
        # Wappenrock über dem Kettenhemd (vorn und hinten), Gürtel
        if t == 9:
            return pick(ctx.pal["rope"], 0.35 + (0.3 if s == 4 else 0.0))
        if face in ("front", "back") and 2 <= s <= 5 and t >= 1:
            col = _cloth(ctx, "cloth", 0.42, tear=0.1, seed=381)
            return col if col is not None else _mail(ctx)
        return _mail(ctx)
    if v == "drowned_matrose":
        col = _cloth(ctx, "cloth", 0.45, tear=0.16, seed=382)
        if col is None:
            return _ribs(ctx)
        if t % 3 == 1 and face != "top":
            col = pick(ctx.pal["stripe"], 0.55 + hash01(ctx.u, ctx.v) * 0.1)
        x, y, z = ctx.world
        if noise3(x * 0.4, y * 0.3, z * 0.4, 383) > 0.7:
            col = mix(col, pick(ctx.pal["kelp"], 0.45), 0.7)                        # Tang hängt über den Schultern
        return col
    if v == "drowned_fischer":
        # Ölzeug-Jacke mit Knebelknöpfen, darunter der Strickpulli am Hals
        if face == "front" and t <= 1 and 2 <= s <= 5:
            return pick(ctx.pal["knit"], 0.45 + (0.1 if s % 2 else 0.0))
        col = _cloth(ctx, "cloth", 0.42, tear=0.08, seed=385)
        if col is None:
            return _ribs(ctx)
        if face == "front" and s in (3, 4):
            return pick(ctx.pal["cloth"], 0.25) if s == 4 else (pick(ctx.pal["rope"], 0.6) if t % 3 == 1 else col)
        x, y, z = ctx.world
        if noise3(x * 0.4, y * 0.3, z * 0.4, 386) > 0.72:
            col = mix(col, pick(ctx.pal["kelp"], 0.4), 0.6)
        return col
    if v == "husk_wuestling":
        # weites Wüstengewand mit Schärpe, vertrocknete Haut am Hals
        if face == "front" and t <= 1 and 2 <= s <= 5:
            return _skin(ctx, 0.4)
        if t in (8, 9):
            return pick(ctx.pal["scarf"], 0.45 + (0.12 if (s + t) % 2 else 0.0))
        col = _cloth(ctx, "cloth", 0.5, tear=0.08, seed=387)
        return col if col is not None else _ribs(ctx)
    # Kittel, Kutte: Strick als Gürtel, zerrissen über den Rippen
    col = _cloth(ctx, "cloth", 0.45, tear=0.16 if v == "zombie_bauer" else 0.08, seed=384)
    if col is None:
        return _ribs(ctx)
    if t == 9 and face in ("front", "back", "west", "east"):
        return pick(ctx.pal["rope"], 0.4 + (0.25 if s % 2 == 0 else 0.0))
    if face == "front" and t <= 1 and 2 <= s <= 5:
        return _skin(ctx, 0.45)                                                         # Halsausschnitt
    return col


def _ribs(ctx: Ctx):
    """Unter dem zerrissenen Stoff: eingefallene Haut, Rippen schimmern durch."""
    col = _skin(ctx, 0.42)
    if ctx.face in ("front", "back") and ctx.t % 2 == 0 and 2 <= ctx.t <= 8:
        col = mix(col, pick(ctx.pal["bone"], 0.55), 0.55)
    return col


def _skirt(ctx: Ctx):
    if _hidden(ctx):
        return None
    if ctx.face in ("top", "bottom"):
        return None
    kind = _kind(ctx)
    if kind == "mummy":
        # Leinenschurz mit Goldgürtel und senkrechten Falten
        if ctx.t == 0:
            return pick(ctx.pal["gold"], 0.6 if ctx.s % 2 else 0.45)
        if ctx.t == ctx.h - 1 and ctx.s % 2:
            return None
        return pick(ctx.pal["linen"], 0.62 + (0.1 if ctx.s % 2 else -0.04))
    if ctx.t >= ctx.h - 1:
        return _cloth(ctx, "cloth", 0.35) if hash01(ctx.u, 391) > 0.45 else None            # ausgefranster Saum
    return _cloth(ctx, "cloth", 0.42, tear=0.08, seed=392)


def _robe(ctx: Ctx):
    if _hidden(ctx):
        return None
    if ctx.face in ("top", "bottom"):
        return None
    if ctx.t >= ctx.h - 1:
        return _cloth(ctx, "cloth", 0.28) if hash01(ctx.u, 393) > 0.4 else None
    col = _cloth(ctx, "cloth", 0.4, tear=0.06, seed=394)
    if col is not None and ctx.face == "front" and abs(ctx.local[0]) < 0.6 and ctx.t < 4:
        return pick(ctx.pal["rope"], 0.5)                                                  # Strickende des Zingulums
    if col is not None and ctx.face == "front" and abs(ctx.local[0]) > 3.6:
        return pick(ctx.pal["cloth"], 0.3)                                                 # Falte an der Seite
    return col


def _arm(ctx: Ctx):
    v = _variant(ctx)
    kind = _kind(ctx)
    ry = rel_y(ctx)
    left = ctx.cube.bone.name == "left_arm"
    if kind == "mummy":
        return _bandage(ctx)
    hand = ry > 0.72
    if hand:
        if ctx.face == "bottom":
            return pick(ctx.pal["skin"], 0.25)
        if ctx.face == "front" and ctx.t == ctx.h - 1 and ctx.s % 2 == 0:
            return pick(ctx.pal["bone"], 0.6)                                             # Fingerknochen
        return _skin(ctx, 0.5)
    if v == "zombie_soeldner":
        return _mail(ctx) if ry < 0.5 or left else _wound(ctx, _skin(ctx, 0.48))
    # Ärmel: links kurz und zerrissen, rechts lang
    sleeve_end = 0.38 if left else 0.62
    if v in ("zombie_moench", "husk_wuestling", "drowned_fischer"):
        sleeve_end = 0.66                                                                 # weite, lange Ärmel
    if ry < sleeve_end:
        col = _cloth(ctx, "cloth", 0.44, tear=0.08, seed=397)
        if col is not None:
            if v == "drowned_matrose" and ctx.t % 3 == 1:
                return pick(ctx.pal["stripe"], 0.55)
            return col
    return _wound(ctx, _skin(ctx, 0.5))


def _leg(ctx: Ctx):
    kind = _kind(ctx)
    v = _variant(ctx)
    ry = rel_y(ctx)
    if kind == "mummy":
        return _bandage(ctx)
    if ctx.face == "bottom" or ry > 0.85:
        barefoot = kind == "drowned" and v == "drowned_matrose" or ctx.cube.bone.name == "left_leg" and v == "zombie_bauer"
        if barefoot:
            return _skin(ctx, 0.4) if ctx.face != "bottom" else pick(ctx.pal["skin"], 0.2)
        return pick(ctx.pal["shoe"], 0.35 + hash01(ctx.u, ctx.v) * 0.12 + (0.1 if ctx.face == "top" else 0.0))
    # Hose, unten zerrissen
    cut = 0.62 + hash01(ctx.u, 401) * 0.18
    if v in ("drowned_fischer", "husk_wuestling"):
        cut = 0.86
    if ry < cut:
        col = _cloth(ctx, "trousers", 0.4, tear=0.06, seed=402)
        if col is not None:
            return col
    return _wound(ctx, _skin(ctx, 0.46)) if kind == "zombie" else _skin(ctx, 0.46)


def materials(variant: str) -> dict:
    return {
        "head": _head,
        "hat": _hat,
        "jaw": _jaw,
        "helm": _helm,
        "torso": _torso,
        "skirt": _skirt,
        "robe": _robe,
        "arm": _arm,
        "leg": _leg,
    }


def hidden(variant: str) -> list[str]:
    return sorted(ALL_GEAR - GEAR[variant])
