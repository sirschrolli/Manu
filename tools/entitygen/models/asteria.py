"""
Asteria, die Sternenreiterin: die letzte Reiterin des Sternenordens, der einst die Sternwarten im End erbaute. Sie
reitet einen gewaltigen, gepanzerten Sternenrochen – anderthalbmal so groß wie die Sternenrochen der Leere, mit einer
silbernen Stirnplatte, einem Rückenkamm aus Silberschuppen und einer violetten Satteldecke mit Goldsaum.

Die Reiterin: eine silberne Plattenrüstung mit Goldsaum und einem leuchtenden Stern auf der Brust, darüber ein
nachtblauer Wappenrock mit Sternen; ein geschlossener Helm mit T-Sehschlitz, in dem zwei Augen glimmen, darauf eine
Krone aus fünf goldenen Zacken mit leuchtenden Spitzen; dahinter weht ein Schweif aus Sternenlicht und ein Umhang wie
der Nachthimmel. Rechts führt sie die Sternenlanze (silberner Schaft mit goldener Wendel, Brechscheibe, ein Wimpel mit
dem Stern des Ordens und eine Spitze aus einem leuchtenden Sternkristall), links einen runden Schild mit demselben Stern.
Sie kniet wie eine Jockey-Reiterin im hohen Sattel.

Knochen-Namen werden in AsteriaModel (Java) für die Animationen verwendet. Grundhaltung: ruhiger Flug, die Lanze
aufrecht. Im Spiel 1,2-fach vergrößert.
"""
from __future__ import annotations

import math

from ..geometry import Model
from ..paint import Ctx, hash01, hexc, mix, noise3, pick, rel_y
from . import star_ray

PALETTES = {
    "asteria": {
        **star_ray.PALETTES["star_ray"],
        "silver": [hexc(c) for c in ("#3a3f58", "#596080", "#7d86a8", "#a2acca", "#c4cce4", "#e2e8f6", "#fafcff")],
        "gold": [hexc(c) for c in ("#5a3c0e", "#8a6018", "#b88a2c", "#e0b84e", "#f8de88", "#fff4c8")],
        "cloth": [hexc(c) for c in ("#080a22", "#10143a", "#1a2052", "#262e6a", "#343e84")],
        "violet": [hexc(c) for c in ("#22103a", "#3a1a56", "#562a74", "#764096", "#9a5cb8")],
        "hair": [hexc(c) for c in ("#4a6ab0", "#7096d6", "#9ebcf0", "#c8dcff", "#eef6ff")],
        "visor": [hexc(c) for c in ("#04050c", "#0a0c1a")],
        "eyes": [hexc(c) for c in ("#3aa0f0", "#8ad8ff", "#e6f8ff")],
        "crystal": [hexc(c) for c in ("#2a5ac8", "#4a86f0", "#7ab0ff", "#b4d6ff", "#e6f4ff", "#ffffff")],
        # der Bauch des großen Rochens: tiefer, schieferblau-violett (nicht so bleich wie bei den Sternenrochen der Leere)
        "belly": [hexc(c) for c in ("#3e3e68", "#4e4e7c", "#62608e", "#7a78a6", "#9290bc", "#aaa8d0")],
    },
}

# Der Rochen ist anderthalbmal so groß wie ein Sternenrochen; seine Muster (Sterne, Sternbilder, Nebel) bleiben gleich
K = 1.5
BODY = (0.0, 17.0, 0.0)
SEAT = -5.4                       # Sitzfläche des Sattels (über dem Leib)


def _k(v):
    return tuple(round(c * K, 4) for c in v)


def _mount(m: Model):
    m.bone("body", pivot=BODY)
    m.cube("body", _k((-5.5, -2.0, -8.5)), _k((11, 4, 17)), "body")
    m.cube("body", _k((-7.0, -1.5, -6.5)), _k((14, 3, 13)), "body")
    m.cube("body", _k((-3.5, -2.5, -6.0)), _k((7, 0.5, 11)), "body")
    # Rückenkamm aus Silberschuppen hinter dem Sattel
    for i, z in enumerate((5.6, 8.6, 11.4)):
        w = 3.2 - i * 0.7
        m.cube("body", (-w / 2, -4.4 + i * 0.25, z), (w, 0.9, 2.4), "scale_plate")
    # Satteldecke über Rücken und Flanken
    m.cube("body", (-8.6, -3.5, -5.0), (17.2, 0.5, 9.0), "caparison")
    m.cube("body", (-8.7, -3.5, -5.0), (0.5, 4.2, 9.0), "caparison")
    m.cube("body", (8.2, -3.5, -5.0), (0.5, 4.2, 9.0), "caparison")
    # hoher Sattel: Sitz, Knauf, hohe Rückenlehne, Kniepolster
    m.cube("body", (-4.0, -5.4, -4.6), (8.0, 1.9, 8.2), "saddle")
    m.cube("body", (-1.6, -7.6, -5.3), (3.2, 2.4, 1.6), "pommel")
    m.cube("body", (-3.8, -9.2, 3.0), (7.6, 4.0, 1.4), "cantle")
    m.cube("body", (-6.4, -4.0, -6.8), (2.4, 0.6, 3.6), "saddle")
    m.cube("body", (4.0, -4.0, -6.8), (2.4, 0.6, 3.6), "saddle")

    m.bone("head", "body", pivot=_k((0, 0.2, -8.5)))
    m.cube("head", _k((-5.0, -1.4, -3.5)), _k((10, 2.8, 3.5)), "mount_head")
    # silberne Stirnplatte mit eingelassenem Sternkristall
    m.cube("head", (-4.8, -2.8, -5.0), (9.6, 0.8, 4.6), "barding")
    m.cube("head", (-0.8, -3.4, -4.4), (1.6, 0.6, 1.6), "crystal")
    for side, sx in (("left", 1), ("right", -1)):
        eye_x = 5.0 if sx > 0 else -5.6
        m.cube("head", _k((eye_x, -1.0, -2.6)), _k((0.6, 1.2, 1.2)), "eye")
        # Kopflappen: schlanker als beim Sternenrochen, leicht nach oben gestellt und eingerollt
        fin = f"{side}_cephalic"
        m.bone(fin, "head", pivot=_k((4.0 * sx, -0.2, -3.2)), rotation=(-10, 8 * sx, 0))
        m.cube(fin, _k((-0.6, -0.45, -3.4)), _k((1.2, 0.9, 3.4)), "mount_fin")
        curl = f"{side}_cephalic_curl"
        m.bone(curl, fin, pivot=_k((0, 0.45, -3.4)), rotation=(-62, 0, 0))
        m.cube(curl, _k((-0.55, -0.4, -1.3)), _k((1.1, 0.8, 1.3)), "mount_fin")

    for side, sx in (("left", 1), ("right", -1)):
        w1 = f"{side}_wing"
        m.bone(w1, "body", pivot=_k((6.5 * sx, 0, -1.5)))
        m.cube(w1, _k((0 if sx > 0 else -14, -1.1, -6.5)), _k((14, 2.2, 13)), "wing")
        w2 = f"{side}_wing_mid"
        m.bone(w2, w1, pivot=_k((14 * sx, 0, -1.0)), rotation=(0, -16 * sx, 0))
        m.cube(w2, _k((0 if sx > 0 else -12, -0.7, -5.0)), _k((12, 1.4, 8.5)), "wing")
        w3 = f"{side}_wing_tip"
        m.bone(w3, w2, pivot=_k((12 * sx, 0, -1.5)), rotation=(0, -26 * sx, 0))
        m.cube(w3, _k((0 if sx > 0 else -8, -0.4, -2.6)), _k((8, 0.8, 4.2)), "wing")
        pel = f"{side}_pelvic"
        m.bone(pel, "body", pivot=_k((3.2 * sx, 0.5, 7.5)), rotation=(0, 22 * sx, 0))
        m.cube(pel, _k((-1.5, -0.4, 0)), _k((3, 0.8, 4.5)), "wing")

    m.bone("tail", "body", pivot=_k((0, 0, 8.5)))
    m.cube("tail", _k((-0.7, -0.6, 0)), _k((1.4, 1.2, 9)), "tail")
    m.bone("tail_mid", "tail", pivot=_k((0, 0, 9)))
    m.cube("tail_mid", _k((-0.5, -0.45, 0)), _k((1.0, 0.9, 10)), "tail")
    m.bone("tail_tip", "tail_mid", pivot=_k((0, 0, 10)))
    m.cube("tail_tip", _k((-0.35, -0.35, 0)), _k((0.7, 0.7, 9)), "tail")
    m.cube("tail_tip", _k((-0.6, -0.6, 8.5)), _k((1.2, 1.2, 1.2)), "glowtip")


def _rider(m: Model):
    m.bone("rider", "body", pivot=(0, SEAT, 0.6))
    m.cube("rider", (-3.0, -2.4, -2.0), (6, 2.6, 4), "faulds")
    m.bone("torso", "rider", pivot=(0, -2.2, 0), rotation=(6, 0, 0))
    m.cube("torso", (-3.2, -9.0, -2.0), (6.4, 9, 4.0), "cuirass")
    m.cube("torso", (-3.5, -9.2, -2.2), (7.0, 3.4, 4.4), "chest")                  # Brustplatte
    m.cube("torso", (-2.4, -5.8, -2.4), (4.8, 7.4, 0.4), "tabard")
    m.cube("torso", (-2.4, -5.8, 2.0), (4.8, 7.4, 0.4), "tabard")
    m.cube("torso", (-1.75, -9.1, -2.7), (3.5, 3.5, 0.3), "emblem")
    m.cube("torso", (-3.4, -1.2, -2.2), (6.8, 1.2, 4.4), "belt")
    m.cube("torso", (-1.6, -10.2, -1.6), (3.2, 1.2, 3.2), "silver")               # Halsberge
    # Kopf: geschlossener Helm mit T-Sehschlitz, Krone, Schweif aus Sternenlicht
    m.bone("helm", "torso", pivot=(0, -10.0, 0))
    m.cube("helm", (-2.6, -5.6, -2.7), (5.2, 5.6, 5.4), "helmet")
    m.cube("helm", (-2.0, -3.9, -2.95), (4.0, 1.0, 0.3), "visor")
    m.cube("helm", (-0.5, -3.0, -2.95), (1.0, 2.2, 0.3), "visor")
    m.cube("helm", (-0.35, -5.9, -2.9), (0.7, 5.0, 0.4), "gold")                  # Grat über Stirn und Nasenschutz
    m.cube("helm", (-0.35, -6.0, -2.6), (0.7, 0.5, 5.2), "gold")
    m.bone("crown", "helm", pivot=(0, -5.6, 0))
    m.cube("crown", (-2.85, -0.9, -2.95), (5.7, 0.9, 5.9), "crown", faces=("front", "back", "west", "east"))
    for ang, h in ((-62, 1.3), (-32, 2.0), (0, 2.9), (32, 2.0), (62, 1.3)):
        a = math.radians(ang)
        x = math.sin(a) * 2.6
        z = -math.cos(a) * 2.6
        m.cube("crown", (x - 0.3, -0.9 - h, z - 0.3), (0.6, h, 0.6), "crown")
        m.cube("crown", (x - 0.3, -1.5 - h, z - 0.3), (0.6, 0.6, 0.6), "crown_tip")
    m.bone("plume", "helm", pivot=(0, -5.0, 2.5), rotation=(-20, 0, 0))
    m.cube("plume", (-0.9, -0.9, -0.4), (1.8, 1.8, 1.2), "gold")                  # Ring
    m.cube("plume", (-1.3, -0.7, 0.8), (2.6, 1.4, 4.6), "hair")
    m.bone("plume_mid", "plume", pivot=(0, 0, 5.4), rotation=(-10, 0, 0))
    m.cube("plume_mid", (-1.0, -0.6, 0), (2.0, 1.2, 5.0), "hair")
    m.bone("plume_tip", "plume_mid", pivot=(0, 0, 5.0), rotation=(-8, 0, 0))
    m.cube("plume_tip", (-0.7, -0.45, 0), (1.4, 0.9, 4.0), "hair")
    m.cube("plume_tip", (-0.35, -0.3, 4.0), (0.7, 0.6, 2.2), "hair")
    # Umhang (zwei Glieder, damit er weht)
    m.bone("cape", "torso", pivot=(0, -8.8, 2.3), rotation=(38, 0, 0))
    m.cube("cape", (-4.0, 0, 0), (8, 11.0, 0.6), "cape")
    m.bone("cape_tip", "cape", pivot=(0, 11.0, 0), rotation=(14, 0, 0))
    m.cube("cape_tip", (-3.8, 0, 0), (7.6, 7.5, 0.6), "cape")
    # Arme: Schulterplatten, Arme, Handschuhe
    for side, sx in (("left", 1), ("right", -1)):
        arm = f"{side}_arm"
        m.bone(arm, "torso", pivot=(4.3 * sx, -8.2, 0), rotation=(-20, 0, -8 * sx))
        m.cube(arm, (-1.9, -1.8, -2.4), (3.8, 3.2, 4.8), "pauldron")
        m.cube(arm, (-2.1, 0.8, -2.2), (4.2, 1.0, 4.4), "pauldron")
        m.cube(arm, (-1.15, 0, -1.15), (2.3, 8.0, 2.3), "armor_arm")
        m.cube(arm, (-1.4, 6.4, -1.4), (2.8, 2.6, 2.8), "gauntlet")
    # Sternenlanze: in der Grundhaltung aufrecht, die Spitze oben
    m.bone("lance", "right_arm", pivot=(0, 7.8, 0), rotation=(200, 0, -10))
    m.cube("lance", (-0.6, -8.0, -0.6), (1.2, 34.0, 1.2), "lance_shaft")
    m.cube("lance", (-1.0, -9.2, -1.0), (2.0, 1.2, 2.0), "gold")                  # Knauf
    m.cube("lance", (-1.0, -1.0, -1.0), (2.0, 2.4, 2.0), "grip")
    m.cube("lance", (-2.2, -2.6, -2.2), (4.4, 1.2, 4.4), "vamplate")              # Brechscheibe
    m.cube("lance", (-1.6, -3.8, -1.6), (3.2, 1.2, 3.2), "vamplate")
    m.cube("lance", (-0.9, 10.0, -0.9), (1.8, 0.8, 1.8), "gold")
    m.cube("lance", (-0.9, 18.0, -0.9), (1.8, 0.8, 1.8), "gold")
    m.cube("lance", (-1.3, 25.4, -1.3), (2.6, 1.0, 2.6), "gold")
    # Spitze: ein Sternkristall – vier Zacken quer, dann die lange Spitze
    m.cube("lance", (-0.95, 26.4, -0.95), (1.9, 2.2, 1.9), "lance_tip")
    m.cube("lance", (-2.6, 26.9, -0.35), (5.2, 1.2, 0.7), "lance_tip")
    m.cube("lance", (-0.35, 26.9, -2.6), (0.7, 1.2, 5.2), "lance_tip")
    m.cube("lance", (-0.7, 28.6, -0.7), (1.4, 3.0, 1.4), "lance_tip")
    m.cube("lance", (-0.45, 31.6, -0.45), (0.9, 2.4, 0.9), "lance_tip")
    m.cube("lance", (-0.2, 34.0, -0.2), (0.4, 1.4, 0.4), "lance_tip")
    # Wimpel unter der Spitze (zwei Glieder, damit er flattert)
    m.bone("pennant", "lance", pivot=(0, 22.6, -0.6))
    m.cube("pennant", (-0.1, -2.4, -5.0), (0.2, 4.8, 5.0), "pennant")
    m.bone("pennant_tip", "pennant", pivot=(0, 0, -5.0))
    m.cube("pennant_tip", (-0.1, -1.8, -3.5), (0.2, 3.6, 3.5), "pennant")
    m.cube("pennant_tip", (-0.1, -1.0, -5.5), (0.2, 2.0, 2.0), "pennant")
    # Schild am linken Arm
    m.bone("shield", "left_arm", pivot=(1.5, 5.4, -0.4), rotation=(10, -8, 0))
    m.cube("shield", (0, -3.8, -3.8), (0.9, 7.6, 7.6), "shield")
    m.cube("shield", (0, -2.8, -4.6), (0.9, 5.6, 0.8), "shield_rim")
    m.cube("shield", (0, -2.8, 3.8), (0.9, 5.6, 0.8), "shield_rim")
    m.cube("shield", (0, -4.6, -2.8), (0.9, 0.8, 5.6), "shield_rim")
    m.cube("shield", (0, 3.8, -2.8), (0.9, 0.8, 5.6), "shield_rim")
    m.cube("shield", (0.9, -1.75, -1.75), (0.4, 3.5, 3.5), "emblem")
    # Beine: kniend im hohen Sattel, die Oberschenkel nach vorn, die Schienbeine an den Kniepolstern
    for side, sx in (("left", 1), ("right", -1)):
        leg = f"{side}_leg"
        m.bone(leg, "rider", pivot=(2.0 * sx, -0.6, -0.2), rotation=(-77, -31 * sx, 0))
        m.cube(leg, (-1.25, 0, -1.25), (2.5, 6.4, 2.5), "greave")
        m.cube(leg, (-1.45, 5.0, -1.7), (2.9, 1.9, 1.0), "knee")
        shin = f"{side}_shin"
        m.bone(shin, "rider", pivot=(5.2 * sx, 0.8, -5.6), rotation=(84, -3 * sx, 0))
        m.cube(shin, (-1.1, 0, -1.1), (2.2, 6.0, 2.2), "greave")
        m.cube(shin, (-1.3, 5.4, -1.4), (2.6, 1.8, 2.6), "boot")


def build() -> Model:
    m = Model("asteria", 256, 256)
    m.tex_res = 2
    _mount(m)
    _rider(m)
    return m


# =============================================================================================
# Bemalung
# =============================================================================================

_LIT = {"top": 0.12, "front": 0.04, "west": 0.0, "east": -0.04, "back": -0.06, "bottom": -0.16}


def _scaled(painter):
    """Malt den großen Rochen mit den Mustern des Sternenrochens (in dessen Maßstab)."""
    def paint(ctx: Ctx):
        x, y, z = ctx.world
        ctx.world = (BODY[0] + (x - BODY[0]) / K, BODY[1] + (y - BODY[1]) / K, BODY[2] + (z - BODY[2]) / K)
        return painter(ctx)
    return paint


def _mount_head(ctx: Ctx):
    """Kopf des großen Rochens: vorn dunkel wie der Rücken (mit violettem Schimmer nach unten), darunter das breite Maul."""
    x, y, z = star_ray._pos(ctx)
    if ctx.face == "front":
        t = (ctx.t + 0.5) / max(ctx.h, 1)
        if t > 0.64 and abs(x) < 3.4:
            return pick(ctx.pal["back"], 0.02)
        col = star_ray._back_col(ctx, x, y, z)
        if ctx.t == 0:
            return mix(col, pick(ctx.pal["nebula"], 0.6), 0.35)
        return mix(col, pick(ctx.pal["nebula"], 0.25), max(0.0, t - 0.3) * 0.35)
    return star_ray._head(ctx)


def _mount_wing(ctx: Ctx):
    """Schwingen wie beim Sternenrochen, nur der Saum an den (beim großen Rochen dicken) Kanten dunkler."""
    col = star_ray._wing(ctx)
    if ctx.face in ("top", "bottom"):
        return col
    x, y, z = star_ray._pos(ctx)
    rim = mix(star_ray._back_col(ctx, x, y, z), pick(ctx.pal["nebula"], 0.55), 0.24 if ctx.t > 0 else 0.5)
    return rim[:3] + (col[3],)


def _mount_fin(ctx: Ctx):
    """Kopflappen: oben und an den Seiten dunkel wie der Rücken mit violettem Saum, unten der Bauch."""
    x, y, z = star_ray._pos(ctx)
    if ctx.face == "bottom":
        return star_ray._belly(ctx, x, z)
    col = star_ray._back_col(ctx, x, y, z)
    if ctx.face in ("front", "west", "east") and ctx.t >= ctx.h - 1:
        col = mix(col, pick(ctx.pal["nebula"], 0.6), 0.4)
    return col


def _plate(ctx: Ctx, base: float = 0.6, trim: bool = True):
    """Polierter Silberharnisch: oben hell, nach unten dunkler, ein Glanzstreif, am Rand ein Goldsaum."""
    x, y, z = ctx.world
    v = base + _LIT[ctx.face] - rel_y(ctx) * 0.2 + (noise3(x * 0.4, y * 0.4, z * 0.4, 31) - 0.5) * 0.06
    rs = (ctx.s + 0.5) / max(ctx.w, 1)
    if ctx.face in ("front", "west", "east", "back") and abs(rs - 0.3) < 0.07:
        v += 0.16                                                   # Glanzstreif
    if trim and ctx.face != "top" and ctx.face != "bottom" and (ctx.t == 0 or ctx.t >= ctx.h - 1):
        return pick(ctx.pal["gold"], 0.6 + _LIT[ctx.face])
    return pick(ctx.pal["silver"], v)


def _cuirass(ctx: Ctx):
    return _plate(ctx, 0.56)


def _chest(ctx: Ctx):
    # die Brustplatte: hell gewölbt, mit eingravierten Sternbildern (feine leuchtende Punkte)
    if ctx.face == "front" and hash01(ctx.u, ctx.v, 41) > 0.95:
        col = pick(ctx.pal["crystal"], 0.75)
        ctx.glow = mix((0, 0, 0, 255), col, 0.55)
        return col
    rs = (ctx.s + 0.5) / max(ctx.w, 1)
    base = 0.66 - abs(rs - 0.5) * 0.25 if ctx.face == "front" else 0.6
    return _plate(ctx, base)


def _faulds(ctx: Ctx):
    # Bauchreifen: waagrechte Plattenstreifen
    if ctx.face in ("front", "west", "east", "back") and ctx.t % 2 == 1:
        return pick(ctx.pal["silver"], 0.4 + _LIT[ctx.face])
    return _plate(ctx, 0.55, trim=False)


def _cloth(ctx: Ctx, pal: str = "cloth"):
    """Nachtblauer Stoff mit gestickten Sternen."""
    x, y, z = ctx.world
    v = 0.5 + _LIT[ctx.face] + (noise3(x * 0.6, y * 0.25, z * 0.6, 33) - 0.5) * 0.3
    col = pick(ctx.pal[pal], v)
    h = hash01(ctx.u, ctx.v, 43)
    if h > 0.95 and ctx.face in ("front", "back"):
        star = pick(ctx.pal["star"], 0.4 + (h - 0.95) * 12)
        ctx.glow = mix((0, 0, 0, 255), star, 0.7)
        return star
    return col


def _tabard(ctx: Ctx):
    if ctx.face in ("front", "back") and (ctx.t >= ctx.h - 1 or ctx.s in (0, ctx.w - 1)):
        return pick(ctx.pal["gold"], 0.65)
    return _cloth(ctx)


def _star_shape(cx: float, cy: float) -> float:
    """Vierzackiger Stern um (0,0), Werte −0,5..0,5: 1 innen, 0 außen."""
    ax, ay = abs(cx), abs(cy)
    core = ax + ay < 0.2
    spike = (ax < 0.07 and ay < 0.5) or (ay < 0.07 and ax < 0.5)
    diag = abs(ax - ay) < 0.05 and ax < 0.22
    return 1.0 if core else 0.85 if spike else 0.6 if diag else 0.0


def _emblem(ctx: Ctx):
    """Der Stern des Ordens: vierzackig, weiß glühend in einem blauen Kranz."""
    cx = (ctx.s + 0.5) / max(ctx.w, 1) - 0.5
    cy = (ctx.t + 0.5) / max(ctx.h, 1) - 0.5
    k = _star_shape(cx, cy)
    if k > 0:
        col = pick(ctx.pal["crystal"], 0.6 + k * 0.4)
        ctx.glow = col
        return col
    col = pick(ctx.pal["crystal"], 0.25)
    ctx.glow = mix((0, 0, 0, 255), col, 0.5)
    return col


def _belt(ctx: Ctx):
    if ctx.face == "front" and abs((ctx.s + 0.5) / max(ctx.w, 1) - 0.5) < 0.1:
        return pick(ctx.pal["gold"], 0.85)                          # Schnalle
    return pick(ctx.pal["violet"], 0.4 + _LIT[ctx.face])


def _helmet(ctx: Ctx):
    v = 0.66 + _LIT[ctx.face] - rel_y(ctx) * 0.24
    if ctx.face in ("west", "east") and rel_y(ctx) > 0.45 and (ctx.s + ctx.t) % 3 == 0:
        v -= 0.12                                                   # Luftlöcher an den Wangen
    if ctx.face != "top" and ctx.t >= ctx.h - 1:
        return pick(ctx.pal["gold"], 0.6)
    return pick(ctx.pal["silver"], v)


def _visor(ctx: Ctx):
    # T-Sehschlitz: schwarz, im Querbalken zwei glimmende Augen
    if ctx.cube.size[0] > 2.0 and ctx.face == "front":
        rx = (ctx.s + 0.5) / max(ctx.w, 1)
        if 0.12 < rx < 0.38 or 0.62 < rx < 0.88:
            col = pick(ctx.pal["eyes"], 0.5)
            ctx.glow = col
            return col
    return pick(ctx.pal["visor"], 0.3)


def _crown(ctx: Ctx):
    v = 0.62 + _LIT[ctx.face] - rel_y(ctx) * 0.15
    if ctx.cube.size[1] < 1.0 and ctx.face in ("front", "west", "east", "back") and ctx.s % 4 == 1:
        col = pick(ctx.pal["crystal"], 0.7)                          # kleine Steine im Reif
        ctx.glow = mix((0, 0, 0, 255), col, 0.7)
        return col
    return pick(ctx.pal["gold"], v)


def _crown_tip(ctx: Ctx):
    col = pick(ctx.pal["crystal"], 0.82)
    ctx.glow = col
    return col


def _hair(ctx: Ctx):
    """Schweif aus Sternenlicht: bläulich weiß in Strähnen, hier und da ein Funke."""
    along = ctx.local[2]
    strand = (noise3(ctx.local[0] * 1.6, 0.0, along * 0.15, 35) - 0.5) * 0.45
    v = 0.62 + _LIT[ctx.face] * 0.6 + strand
    col = pick(ctx.pal["hair"], v)
    ctx.glow = mix((0, 0, 0, 255), col, 0.42)
    if hash01(ctx.u, ctx.v, 45) > 0.94:
        col = pick(ctx.pal["star"], 0.95)
        ctx.glow = col
    fade = {"plume": 0.0, "plume_mid": 0.35, "plume_tip": 0.7}.get(ctx.cube.bone.name, 0.0)
    fade += max(0.0, ctx.local[2]) / 40.0
    return col[:3] + (round(255 - min(1.0, fade) * 120),)


def _cape(ctx: Ctx):
    x, y, z = ctx.world
    if ctx.face in ("back", "west", "east", "bottom"):
        col = pick(ctx.pal["back"], 0.42 + (noise3(x * 0.3, y * 0.3, 0.0, 37) - 0.5) * 0.3)
        neb = noise3(x * 0.25, y * 0.18, 3.0, 38)
        if neb > 0.58:
            col = mix(col, pick(ctx.pal["nebula"], (neb - 0.58) * 2.2), min(0.65, (neb - 0.58) * 2.4))
        h = hash01(ctx.u, ctx.v, 47)
        if ctx.face == "back" and h > 0.93:
            star = pick(ctx.pal["star"], 0.3 + (h - 0.93) * 10)
            ctx.glow = mix((0, 0, 0, 255), star, 0.8)
            return star
        if ctx.lface == "bottom" or ctx.t >= ctx.h - 1 and ctx.cube.bone.name == "cape_tip":
            return pick(ctx.pal["gold"], 0.55)
        return col
    if ctx.face == "front":
        return pick(ctx.pal["violet"], 0.38 + (noise3(x * 0.3, y * 0.3, 1.0, 39) - 0.5) * 0.2)   # Futter
    return pick(ctx.pal["gold"], 0.5)


def _pauldron(ctx: Ctx):
    if ctx.face == "top":
        rs = (ctx.s + 0.5) / max(ctx.w, 1)
        if abs(rs - 0.5) < 0.08:
            return pick(ctx.pal["gold"], 0.75)
    return _plate(ctx, 0.64)


def _armor_arm(ctx: Ctx):
    if ctx.face != "top" and ctx.face != "bottom" and int(ctx.local[1] * 2) % 6 == 0:
        return pick(ctx.pal["silver"], 0.38 + _LIT[ctx.face])         # Gelenkstreifen
    return _plate(ctx, 0.56, trim=False)


def _gauntlet(ctx: Ctx):
    return _plate(ctx, 0.5)


def _greave(ctx: Ctx):
    return _plate(ctx, 0.56)


def _knee(ctx: Ctx):
    if ctx.face == "front" and abs((ctx.s + 0.5) / max(ctx.w, 1) - 0.5) < 0.18:
        return pick(ctx.pal["gold"], 0.75)
    return _plate(ctx, 0.66)


def _boot(ctx: Ctx):
    return _plate(ctx, 0.44)


def _lance_shaft(ctx: Ctx):
    """Silberweißer Schaft mit einer goldenen Wendel."""
    ly = ctx.local[1]
    ang = {"front": 0, "east": 1, "back": 2, "west": 3}.get(ctx.lface, 0)
    if int(ly * 1.2 + ang * 1.5) % 6 == 0 and ctx.lface not in ("top", "bottom"):
        return pick(ctx.pal["gold"], 0.65 + _LIT[ctx.face] * 0.5)
    return pick(ctx.pal["silver"], 0.72 + _LIT[ctx.face] * 0.6)


def _grip(ctx: Ctx):
    if ctx.t % 2 == 0:
        return pick(ctx.pal["violet"], 0.35)
    return pick(ctx.pal["violet"], 0.55)


def _vamplate(ctx: Ctx):
    if ctx.lface in ("top", "bottom"):
        rs = (ctx.s + 0.5) / max(ctx.w, 1) - 0.5
        rt = (ctx.t + 0.5) / max(ctx.h, 1) - 0.5
        if abs(abs(rs) - abs(rt)) < 0.06:
            return pick(ctx.pal["gold"], 0.75)
        return pick(ctx.pal["silver"], 0.7 - math.hypot(rs, rt) * 0.4)
    return pick(ctx.pal["gold"], 0.6 + _LIT[ctx.face] * 0.5)


def _gold(ctx: Ctx):
    return pick(ctx.pal["gold"], 0.62 + _LIT[ctx.face])


def _lance_tip(ctx: Ctx):
    """Die Spitze: Sternkristall, innen weiß glühend, an den Kanten eisblau."""
    edge = ctx.s in (0, ctx.w - 1) or ctx.t in (0, ctx.h - 1)
    lift = ctx.local[1] / 36.0
    col = pick(ctx.pal["crystal"], (0.55 if edge else 0.78) + lift * 0.15)
    ctx.glow = col
    return col


def _pennant(ctx: Ctx):
    """Wimpel: nachtblau mit goldenem Saum und dem weißen Stern des Ordens."""
    w = ctx.cube.size[2]
    lz = ctx.local[2] - ctx.cube.origin[2]
    ly = ctx.local[1] - ctx.cube.origin[1]
    h = ctx.cube.size[1]
    if ctx.lface in ("west", "east"):
        if ly < 0.5 or ly > h - 0.5:
            return pick(ctx.pal["gold"], 0.65)
        if ctx.cube.bone.name == "pennant":
            k = _star_shape((lz / w - 0.5) * 1.1, (ly / h - 0.5) * 1.1)
            if k > 0:
                col = pick(ctx.pal["star"], 0.7 + k * 0.3)
                ctx.glow = mix((0, 0, 0, 255), col, 0.8)
                return col
        return pick(ctx.pal["cloth"], 0.55 - lz * 0.02)
    return pick(ctx.pal["gold"], 0.55)


def _shield(ctx: Ctx):
    """Runder Schild: nachtblaues Feld, goldene Sternstrahlen zum silbernen Rand."""
    if ctx.lface in ("west", "east"):
        cx = (ctx.s + 0.5) / max(ctx.w, 1) - 0.5
        cy = (ctx.t + 0.5) / max(ctx.h, 1) - 0.5
        r = math.hypot(cx, cy)
        if r > 0.43:
            return pick(ctx.pal["silver"], 0.72)
        if abs(cx) < 0.045 or abs(cy) < 0.045 or abs(abs(cx) - abs(cy)) < 0.03 and r > 0.16:
            return pick(ctx.pal["gold"], 0.7)
        if 0.36 < r <= 0.43:
            return pick(ctx.pal["gold"], 0.5)
        if hash01(ctx.u, ctx.v, 49) > 0.93:
            col = pick(ctx.pal["star"], 0.7)
            ctx.glow = mix((0, 0, 0, 255), col, 0.6)
            return col
        return pick(ctx.pal["cloth"], 0.62 - r * 0.6)
    return pick(ctx.pal["silver"], 0.55 + _LIT[ctx.face])


def _shield_rim(ctx: Ctx):
    return pick(ctx.pal["silver"], 0.68 + _LIT[ctx.face] * 0.5)


def _barding(ctx: Ctx):
    if ctx.face in ("front", "west", "east") or ctx.t in (0, ctx.h - 1) or ctx.s in (0, ctx.w - 1):
        return pick(ctx.pal["gold"], 0.62 + _LIT[ctx.face])
    rs = (ctx.s + 0.5) / max(ctx.w, 1)
    return pick(ctx.pal["silver"], 0.62 + _LIT[ctx.face] - abs(rs - 0.5) * 0.3)


def _scale_plate(ctx: Ctx):
    if ctx.face == "top":
        rs = (ctx.s + 0.5) / max(ctx.w, 1)
        return pick(ctx.pal["silver"], 0.75 - abs(rs - 0.5) * 0.5)
    return pick(ctx.pal["gold"], 0.55 + _LIT[ctx.face])


def _caparison(ctx: Ctx):
    """Satteldecke: violett mit Goldsaum, an den Flanken mit Fransen."""
    if ctx.face in ("top", "bottom"):
        if ctx.s in (0, 1, ctx.w - 1, ctx.w - 2) or ctx.t in (0, 1, ctx.h - 1, ctx.h - 2):
            return pick(ctx.pal["gold"], 0.62)
        rs = (ctx.s + 0.5) / max(ctx.w, 1) - 0.5
        rt = (ctx.t + 0.5) / max(ctx.h, 1) - 0.5
        if abs(rs) > 0.3 and _star_shape((abs(rs) - 0.4) * 5.0, rt * 1.6) > 0:
            col = pick(ctx.pal["star"], 0.7)
            ctx.glow = mix((0, 0, 0, 255), col, 0.6)
            return col
        return pick(ctx.pal["violet"], 0.48 + _LIT[ctx.face] + (noise3(ctx.u * 0.3, ctx.v * 0.3, 0, 51) - 0.5) * 0.15)
    if ctx.face in ("west", "east"):
        ry = rel_y(ctx)
        if ry > 0.82:
            return pick(ctx.pal["gold"], 0.6) if ctx.s % 2 == 0 else (0, 0, 0, 0)       # Fransen
        if ry > 0.72 or ry < 0.1:
            return pick(ctx.pal["gold"], 0.62)
        return pick(ctx.pal["violet"], 0.42 + (noise3(ctx.u * 0.3, ctx.v * 0.3, 0, 52) - 0.5) * 0.15)
    return pick(ctx.pal["gold"], 0.55)


def _saddle(ctx: Ctx):
    if ctx.face == "top":
        rs = (ctx.s + 0.5) / max(ctx.w, 1) - 0.5
        rt = (ctx.t + 0.5) / max(ctx.h, 1) - 0.5
        if abs(rs) > 0.42 or abs(rt) > 0.42:
            return pick(ctx.pal["gold"], 0.6)
        return pick(ctx.pal["violet"], 0.5 + (hash01(ctx.u, ctx.v, 53) - 0.5) * 0.08)
    if ctx.t == 0:
        return pick(ctx.pal["gold"], 0.65)
    return pick(ctx.pal["violet"], 0.32 + _LIT[ctx.face] + (hash01(ctx.u, ctx.v, 54) - 0.5) * 0.06)


def _pommel(ctx: Ctx):
    return pick(ctx.pal["gold"], 0.6 + _LIT[ctx.face])


def _cantle(ctx: Ctx):
    if ctx.face in ("front", "back"):
        rs = (ctx.s + 0.5) / max(ctx.w, 1) - 0.5
        rt = (ctx.t + 0.5) / max(ctx.h, 1) - 0.5
        if ctx.face == "back" and _star_shape(rs * 1.6, rt) > 0:
            col = pick(ctx.pal["star"], 0.75)
            ctx.glow = mix((0, 0, 0, 255), col, 0.7)
            return col
        if abs(rs) > 0.44 or rt < -0.4:
            return pick(ctx.pal["gold"], 0.62)
        return pick(ctx.pal["violet"], 0.42 + _LIT[ctx.face])
    return pick(ctx.pal["gold"], 0.58 + _LIT[ctx.face])


def _crystal(ctx: Ctx):
    col = pick(ctx.pal["crystal"], 0.75 + _LIT[ctx.face])
    ctx.glow = col
    return col


def materials(variant: str) -> dict:
    mats = {name: _scaled(p) for name, p in star_ray.materials("star_ray").items()}
    mats["mount_head"] = _scaled(_mount_head)
    mats["mount_fin"] = _scaled(_mount_fin)
    mats["wing"] = _scaled(_mount_wing)
    mats.update({
        "barding": _barding, "scale_plate": _scale_plate, "caparison": _caparison, "saddle": _saddle, "pommel": _pommel,
        "cantle": _cantle, "crystal": _crystal, "faulds": _faulds, "cuirass": _cuirass, "chest": _chest,
        "tabard": _tabard, "emblem": _emblem, "belt": _belt, "silver": lambda c: _plate(c, 0.62, trim=False),
        "helmet": _helmet, "visor": _visor, "gold": _gold, "crown": _crown, "crown_tip": _crown_tip, "hair": _hair,
        "cape": _cape, "pauldron": _pauldron, "armor_arm": _armor_arm, "gauntlet": _gauntlet, "greave": _greave,
        "knee": _knee, "boot": _boot, "lance_shaft": _lance_shaft, "grip": _grip, "vamplate": _vamplate,
        "lance_tip": _lance_tip, "pennant": _pennant, "shield": _shield, "shield_rim": _shield_rim,
    })
    return mats


def preview_pose(variant: str) -> dict:
    return {"left_wing": (0, 0, -8), "right_wing": (0, 0, 8), "left_wing_mid": (0, 0, -10), "right_wing_mid": (0, 0, 10)}
