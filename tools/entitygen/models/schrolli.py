"""
Schrolli der Göttliche: der Schöpfer selbst, der aus seinem Göttlichen Hof herabsteigt – ein schwebender Seraph aus Licht
und Gold, gut sechs Blöcke groß.

  * ein Gewand aus strahlendem Weiß mit goldenen Stickereien, darüber ein goldener Brustpanzer mit einer Sonne, die
    leuchtet; goldene Schultern, ein Halsring, ein Gürtel mit Himmelsstein, eine bestickte Stola
  * ein jugendliches, ruhiges Gesicht mit leuchtenden Augen, goldenes, langes Haar, eine Krone aus neun Lichtstrahlen
  * hinter dem Kopf ein großer Heiligenschein: ein Sonnenrad aus Gold mit zwölf Strahlen (er dreht sich langsam)
  * sechs Schwingen wie bei einem Seraph – zwei hoch erhoben, zwei weit ausgebreitet, zwei gesenkt –, weiße Federn mit
    goldenen Spitzen, die Schwungfedern an den Enden gefächert
  * in der rechten Hand das Zepter mit einem achtstrahligen Sonnenstern, in der linken die Weltkugel (ein blauer
    Himmelsglobus mit goldenen Ländern, Goldband und Kreuz)
  * unten fließt das Gewand in Bahnen aus, die in Licht vergehen – er hat keine Füße, er schwebt

Knochen-Namen werden in SchrolliModel (Java) animiert. Im Spiel ×2,0.
"""
from __future__ import annotations

import math

from ..geometry import Model
from ..paint import Ctx, hash01, hexc, mix, noise3, pick, ramp, rel_y

_BASE = {
    "white": ramp("#8c8ca8", "#ffffff", steps=8, hue_shift=-0.02),
    "cloth": ramp("#b8b0c8", "#fffdf6", steps=7, hue_shift=-0.03),
    "gold": [hexc(c) for c in ("#5a3a10", "#8a5e1c", "#b8862a", "#dcae3e", "#f2cf62", "#ffe8a0", "#fff8dc")],
    "light": [hexc(c) for c in ("#c08a2a", "#f0c050", "#ffe08a", "#fff2c4", "#fffbea", "#ffffff")],
    "skin": ramp("#b88a6a", "#fff4e6", steps=7, hue_shift=-0.02),
    "hair": [hexc(c) for c in ("#7a4e14", "#a8701e", "#d09a2e", "#ecc04a", "#fadc7a", "#fff0b8")],
    "eye": [hexc(c) for c in ("#5aa8e0", "#a8dcff", "#e8f8ff", "#ffffff")],
    "gem": [hexc(c) for c in ("#0e3a7a", "#1a62b8", "#3a94e8", "#7cc4ff", "#c8ecff", "#ffffff")],
    "globe": [hexc(c) for c in ("#0a1a4a", "#103074", "#1a4aa2", "#2c6ccc", "#5a9ae8", "#9acbff")],
    "feather": ramp("#9a98b4", "#ffffff", steps=7, hue_shift=-0.03),
    "shadow": [hexc(c) for c in ("#3a3450", "#56506e", "#76708e")],
    # die Wahre Gestalt: das Gewand wird zum Weltall, die Federn schillern
    "space": [hexc(c) for c in ("#04021a", "#0a0630", "#140c48", "#22125e", "#341a78", "#4a2a92")],
    "nebula": [hexc(c) for c in ("#6a1e8a", "#a02a9e", "#d24aa8", "#f08ac0", "#5a8ae8", "#4ac8f0", "#a0f0ff")],
    "prism": [hexc(c) for c in ("#ffd070", "#ffa0a8", "#e890ff", "#a0a8ff", "#80e0ff", "#a8ffd8", "#fff6a0")],
}

PALETTES = {
    "schrolli": dict(_BASE),
    "schrolli_true": dict(_BASE, **{
        "gold": [hexc(c) for c in ("#6a4a18", "#9a7024", "#cc9c34", "#ecc450", "#ffe07a", "#fff2b8", "#ffffff")],
        "skin": ramp("#d8b48a", "#fffaf0", steps=7, hue_shift=-0.02),
        "eye": [hexc(c) for c in ("#fff4c0", "#fffbe0", "#ffffff", "#ffffff")],
        "hair": [hexc(c) for c in ("#b08020", "#d8a830", "#f0c850", "#ffe080", "#fff0b0", "#ffffff")],
        "feather": ramp("#b0a8d0", "#ffffff", steps=7, hue_shift=-0.03),
    }),
}

# Teile, die nur die Wahre Gestalt zeigt (im Java-Modell anfangs unsichtbar)
TRUE_RING_A = 15.0
TRUE_RING_B = 17.5
TRUE_CORONA = 13.6

# ---------------------------------------------------------------------------------------------
# Maße
# ---------------------------------------------------------------------------------------------
HALO_R = 10.5           # Halbmesser des Sonnenrads
HALO_SEGMENTS = 16
HALO_RAYS = 12

# Schwingen: (Name, Ansatz y, Ansatz z, Drehung (x, y, z) für links, Längen der drei Glieder, Knick an den Gelenken (z),
# Größe der Federn)
WINGS = (
    ("upper", -13.0, 3.0, (-6.0, -16.0, -48.0), (8.0, 9.0, 8.0), -10.0, 0.86),
    ("mid", -10.5, 3.4, (0.0, -22.0, -12.0), (10.0, 11.0, 10.0), 9.0, 1.0),
    ("lower", -7.5, 3.2, (6.0, -18.0, 24.0), (6.5, 7.0, 6.5), 12.0, 0.72),
)
# Federn je Glied: (Anzahl, Länge von, bis, Winkel von, bis (Grad, von senkrecht hinab zur Spitze hin), Breite)
FEATHERS = (
    (3, 5.0, 6.5, 0.0, 6.0, 3.4),
    (4, 8.0, 11.5, 8.0, 18.0, 3.2),
    (5, 12.0, 17.5, 24.0, 64.0, 3.0),
)


def build() -> Model:
    m = Model("schrolli", 320, 320)
    m.tex_res = 2

    # ---------------------------------------------------------------- Leib
    m.bone("body", pivot=(0, 0, 0))
    m.cube("body", (-5.5, -15.0, -3.2), (11, 15, 6.4), "tunic")                      # Brust und Leib
    m.cube("body", (-5.9, -15.6, -3.8), (11.8, 7.4, 1.0), "cuirass")                # Brustpanzer mit der Sonne
    m.cube("body", (-5.9, -15.6, 2.9), (11.8, 7.4, 0.9), "gold")                    # Rückenplatte
    m.cube("body", (-6.6, -16.6, -3.6), (13.2, 2.0, 7.2), "gold")                   # Halsring
    m.cube("body", (-6.3, -2.4, -3.7), (12.6, 2.6, 7.4), "gold")                    # Gürtel
    m.cube("body", (-1.3, -2.3, -4.25), (2.6, 2.4, 0.6), "gem")                     # Himmelsstein am Gürtel
    # ---------------------------------------------------------------- Kopf, Haar, Krone
    m.bone("head", "body", pivot=(0, -16.6, -0.3))
    m.cube("head", (-4, -8, -4), (8, 8, 8), "face")
    m.cube("head", (-4.4, -8.8, -4.4), (8.8, 1.6, 8.8), "hair")                    # Scheitel
    m.cube("head", (-4.2, -8.3, -4.7), (8.4, 2.2, 0.8), "bangs")                   # Stirnhaar
    for sx in (-1, 1):
        m.cube("head", (sx * 4.3 - 0.5, -7.6, -3.4), (1.0, 7.4, 6.8), "hair")      # Seitensträhnen
    m.bone("hair_back", "head", pivot=(0, -7.6, 3.6), rotation=(8, 0, 0))
    m.cube("hair_back", (-4.4, 0, -0.9), (8.8, 9.5, 1.8), "hair_long")
    m.bone("hair_tip", "hair_back", pivot=(0, 9.5, 0), rotation=(6, 0, 0))
    m.cube("hair_tip", (-4.0, 0, -0.8), (8.0, 5.0, 1.6), "hair_tip")
    m.cube("head", (-4.55, -6.9, -4.55), (9.1, 1.0, 9.1), "gold")                  # Stirnreif
    m.cube("head", (-0.7, -7.0, -4.9), (1.4, 1.3, 0.5), "gem")                     # Stein im Stirnreif
    m.bone("crown", "head", pivot=(0, -7.8, -0.6))
    for k in range(9):
        a = -76.0 + k * 19.0
        length = 6.4 - abs(k - 4) * 0.85
        name = f"crown_{k}"
        m.bone(name, "crown", pivot=(math.sin(math.radians(a)) * 3.6, -0.6, -math.cos(math.radians(a)) * 0.6), rotation=(0, 0, a))
        m.cube(name, (-0.45, -length, -0.45), (0.9, length, 0.9), "ray")

    # ---------------------------------------------------------------- der Heiligenschein: ein Sonnenrad hinter dem Kopf
    m.bone("halo", "head", pivot=(0, -5.0, 7.0))
    seg = 2 * math.pi * HALO_R / HALO_SEGMENTS + 0.5
    for k in range(HALO_SEGMENTS):
        a = 360.0 * k / HALO_SEGMENTS
        r = math.radians(a)
        name = f"halo_{k}"
        m.bone(name, "halo", pivot=(math.cos(r) * HALO_R, math.sin(r) * HALO_R, 0), rotation=(0, 0, a + 90.0))
        m.cube(name, (-seg / 2, -0.8, -0.6), (seg, 1.6, 1.2), "halo")
    for k in range(HALO_RAYS):
        a = 360.0 * k / HALO_RAYS + 15.0
        r = math.radians(a)
        length = 4.2 if k % 2 == 0 else 2.6
        name = f"halo_ray_{k}"
        m.bone(name, "halo", pivot=(math.cos(r) * (HALO_R + 0.9), math.sin(r) * (HALO_R + 0.9), 0.2), rotation=(0, 0, a - 90.0))
        m.cube(name, (-0.5, 0, -0.35), (1.0, length, 0.7), "ray")
    m.bone("halo_inner", "halo", pivot=(0, 0, 0.4))
    inner = 6.6
    seg2 = 2 * math.pi * inner / 12 + 0.4
    for k in range(12):
        a = 30.0 * k
        r = math.radians(a)
        name = f"halo_inner_{k}"
        m.bone(name, "halo_inner", pivot=(math.cos(r) * inner, math.sin(r) * inner, 0), rotation=(0, 0, a + 90.0))
        m.cube(name, (-seg2 / 2, -0.4, -0.3), (seg2, 0.8, 0.6), "halo_thin")

    # ---------------------------------------------------------------- Arme, Zepter, Weltkugel
    # Ruhehaltung: die Rechte hält das Zepter aufrecht vor sich, die Linke die Weltkugel vor der Brust
    for side, sx, arm_rot, fore_rot in (("left", 1, (-18.0, 0.0, -8.0), (-58.0, 0.0, 0.0)),
                                         ("right", -1, (-26.0, 0.0, 10.0), (-34.0, 0.0, 0.0))):
        arm = f"{side}_arm"
        m.bone(arm, "body", pivot=(7.2 * sx, -14.4, 0), rotation=arm_rot)
        m.cube(arm, (-2.3, 0.2, -2.4), (4.6, 8.4, 4.8), "sleeve")                    # weiter Ärmel
        m.cube(arm, (-2.9, -2.4, -3.0), (5.8, 3.6, 6.0), "gold")                     # Schulterstück
        m.cube(arm, (-3.1, -3.1, -2.2), (6.2, 1.0, 4.4), "gold_edge")                # sein Grat
        m.bone(f"{side}_forearm", arm, pivot=(0, 8.4, 0), rotation=fore_rot)
        m.cube(f"{side}_forearm", (-2.2, 0, -2.2), (4.4, 2.8, 4.4), "cuff")          # Stulpe des Ärmels
        m.cube(f"{side}_forearm", (-1.9, 2.6, -1.9), (3.8, 3.4, 3.8), "bracer")      # goldene Armschiene
        m.cube(f"{side}_forearm", (-1.6, 6.0, -1.7), (3.2, 3.2, 3.4), "skin")       # Hand

    # das Zepter in der rechten Hand: ein langer Goldstab, oben ein achtstrahliger Sonnenstern um einen Himmelsstein
    m.bone("scepter", "right_forearm", pivot=(0, 7.6, -0.4), rotation=(60.0, 0.0, -10.0))
    m.cube("scepter", (-0.6, -15.0, -0.6), (1.2, 26.0, 1.2), "staff")
    m.cube("scepter", (-0.9, 10.0, -0.9), (1.8, 1.6, 1.8), "gold")                 # Fuß
    m.bone("scepter_head", "scepter", pivot=(0, -16.8, 0))
    m.cube("scepter_head", (-1.4, -1.4, -1.4), (2.8, 2.8, 2.8), "sun_gem")
    for k in range(4):
        name = f"scepter_star_{k}"
        m.bone(name, "scepter_head", pivot=(0, 0, 0), rotation=(0, 0, 45.0 * k))
        length = 8.0 if k % 2 == 0 else 5.6
        m.cube(name, (-length / 2, -0.45, -0.35), (length, 0.9, 0.7), "ray")
    m.cube("scepter_head", (-2.0, 1.4, -0.5), (4.0, 0.8, 1.0), "gold")             # Kragen unter dem Stern

    # die Weltkugel in der linken Hand: ein blauer Globus mit goldenen Ländern, Goldband und Kreuz
    m.bone("orb", "left_forearm", pivot=(0, 10.4, -1.2), rotation=(76.0, 0.0, 8.0))
    m.cube("orb", (-2.4, -2.4, -2.4), (4.8, 4.8, 4.8), "globe")
    m.cube("orb", (-1.8, -2.9, -1.8), (3.6, 5.8, 3.6), "globe")
    m.cube("orb", (-2.9, -1.8, -1.8), (5.8, 3.6, 3.6), "globe")
    m.cube("orb", (-1.8, -1.8, -2.9), (3.6, 3.6, 5.8), "globe")
    m.cube("orb", (-3.0, -0.4, -3.0), (6.0, 0.8, 6.0), "gold_band")
    m.cube("orb", (-0.35, -5.0, -0.35), (0.7, 2.2, 0.7), "gold")                  # Kreuz
    m.cube("orb", (-1.0, -4.4, -0.35), (2.0, 0.6, 0.7), "gold")

    # ---------------------------------------------------------------- das Gewand, das in Licht vergeht
    m.bone("robe", "body", pivot=(0, 0.4, 0))
    m.cube("robe", (-6.2, 0, -3.8), (12.4, 6, 7.6), "robe")
    for name, pivot, rot, origin, size, low in (
            ("robe_front", (0, 0, -4.0), (-10, 0, 0), (-7.0, 0, -0.6), (14.0, 10, 1.2), (14.0, 11, 1.2)),
            ("robe_back", (0, 0, 4.0), (9, 0, 0), (-7.0, 0, -0.6), (14.0, 10, 1.2), (14.0, 12, 1.2)),
            ("robe_left", (6.6, 0, 0), (0, 0, -10), (-0.6, 0, -4.6), (1.2, 10, 9.2), (1.2, 10, 9.2)),
            ("robe_right", (-6.6, 0, 0), (0, 0, 10), (-0.6, 0, -4.6), (1.2, 10, 9.2), (1.2, 10, 9.2))):
        m.bone(name, "robe", pivot=pivot, rotation=rot)
        m.cube(name, origin, size, "robe_stole" if name == "robe_front" else "robe")
        m.bone(name + "_low", name, pivot=(0, 10, 0))
        m.cube(name + "_low", origin, low, "robe_light")

    # ---------------------------------------------------------------- sechs Schwingen
    # jede aus drei Gliedern (Oberarm, Unterarm, Hand); an jedem hängen Federn, die zur Spitze hin länger werden und sich
    # fächern – die Handschwingen ragen weit hinaus. Darüber eine Lage Deckfedern.
    for pos, y, z, rot, segs, bend, size in WINGS:
        for side, sx in (("left", 1), ("right", -1)):
            parent = "body"
            pivot = (2.6 * sx, y, z)
            rotation = (rot[0], rot[1] * sx, rot[2] * sx)
            for i, length in enumerate(segs):
                name = f"wing_{pos}_{side}" + ("" if i == 0 else "_mid" if i == 1 else "_tip")
                m.bone(name, parent, pivot=pivot, rotation=rotation)
                x0 = 0.0 if sx > 0 else -length
                m.cube(name, (x0, -1.0, -0.8), (length, 2.0, 1.6), "wing_arm")                     # Flügelknochen
                m.cube(name, (x0, 0.6, -0.35), (length, 3.6 * size + i * 0.6, 0), "covert", tag=f"{pos}{side}{i}")
                count, l0, l1, a0, a1, w = FEATHERS[i]
                for j in range(count):
                    k = j / max(1, count - 1)
                    fname = f"{name}_f{j}"
                    fx = (length * (0.12 + 0.8 * k)) * sx
                    angle = a0 + (a1 - a0) * k
                    m.bone(fname, name, pivot=(fx, 0.7, 0.05 + 0.12 * j), rotation=(0, 0, -angle * sx))
                    flen = (l0 + (l1 - l0) * k) * size
                    m.cube(fname, (-w * size / 2, 0, 0), (w * size, flen, 0), "primary", tag=f"{pos}{side}{i}{j}")
                parent = name
                pivot = (length * sx, 0, 0)
                rotation = (0, -6 * sx, bend * sx)

    # ---------------------------------------------------------------- nur in der Wahren Gestalt
    # zwei Räder im Rad, mit Augen besetzt, kreisen um ihn (das eine waagrecht um die Hüfte, das andere senkrecht)
    m.bone("true_rings", "body", pivot=(0, -8.0, 0))
    for ring, radius, count, rot in (("true_ring_a", TRUE_RING_A, 20, (0, 0, 0)), ("true_ring_b", TRUE_RING_B, 22, (90, 0, 0))):
        m.bone(ring, "true_rings", pivot=(0, 0, 0), rotation=rot)
        seg = 2 * math.pi * radius / count + 0.45
        for k in range(count):
            a = 360.0 * k / count
            r = math.radians(a)
            name = f"{ring}_{k}"
            m.bone(name, ring, pivot=(math.cos(r) * radius, 0, math.sin(r) * radius), rotation=(0, -(a + 90.0), 0))
            m.cube(name, (-seg / 2, -1.3, -0.7), (seg, 2.6, 1.4), "ring_eye" if k % 2 == 0 else "ring_rune", tag=f"{ring}{k}")
    # ein Strahlenkranz hinter dem Heiligenschein: vierundzwanzig lange Lichtstrahlen
    m.bone("true_corona", "halo", pivot=(0, 0, 0.9))
    for k in range(24):
        a = 360.0 * k / 24 + 7.5
        r = math.radians(a)
        length = 9.5 if k % 2 == 0 else 5.5
        name = f"true_corona_{k}"
        m.bone(name, "true_corona", pivot=(math.cos(r) * TRUE_CORONA, math.sin(r) * TRUE_CORONA, 0), rotation=(0, 0, a - 90.0))
        m.cube(name, (-0.9, 0, 0), (1.8, length, 0), "corona")
    return m


# =============================================================================================
# Bemalung
# =============================================================================================

_LIT = {"top": 0.12, "front": 0.05, "west": 0.0, "east": -0.03, "back": -0.06, "bottom": -0.18}


def _edge(ctx: Ctx) -> int:
    return min(ctx.s, ctx.w - 1 - ctx.s, ctx.t, ctx.h - 1 - ctx.t)


def _n(ctx: Ctx, scale: float = 1.0, seed: int = 0) -> float:
    x, y, z = ctx.world
    return noise3(x * scale, y * scale, z * scale, seed) - 0.5


def _true(ctx: Ctx) -> bool:
    return (ctx.params or {}).get("variant") == "schrolli_true"


def _cosmos(ctx: Ctx, v: float) -> tuple:
    """Das Gewand der Wahren Gestalt: tiefes Weltall mit leuchtenden Nebeln und Sternen."""
    x, y, z = ctx.world
    n1 = noise3(x * 0.16, y * 0.16, z * 0.16, 81)
    n2 = noise3(x * 0.31 + 5.0, y * 0.31, z * 0.31, 82)
    n3 = noise3(x * 0.7, y * 0.7 + 3.0, z * 0.7, 84)
    col = pick(ctx.pal["space"], 0.18 + n1 * 0.45 + _LIT[ctx.face] * 0.9 + (v - 0.66) * 0.5)
    neb = max(0.0, n2 * 0.8 + n3 * 0.35 - 0.52) * 2.6
    if neb > 0.0:
        tint = pick(ctx.pal["nebula"], n1 * 0.9 + n3 * 0.25)
        col = mix(col, tint, min(0.8, neb))
        ctx.glow = mix((0, 0, 0, 255), tint, min(0.75, neb * 0.9))
    h = hash01(ctx.u, ctx.v, 83)
    if h > 0.968:
        star = pick(ctx.pal["light"], 0.82 + (h - 0.968) * 5.0)
        col = star
        ctx.glow = star
    return col


def _cloth_base(ctx: Ctx, v: float) -> tuple:
    if _true(ctx):
        return _cosmos(ctx, v)
    # weiche Falten: senkrechte Wellen mit Rauschen
    x, y, z = ctx.world
    fold = math.sin(x * 1.3 + _n(ctx, 0.3, 3) * 3.0) * 0.06 + math.sin(z * 1.1 + 1.2) * 0.03
    return pick(ctx.pal["cloth"], v + fold + _LIT[ctx.face] + _n(ctx, 0.5, 4) * 0.06)


def _ring_eye(ctx: Ctx):
    """Ein Glied der Räder: Gold, außen ein offenes Auge, das leuchtet."""
    col = _gold(ctx)
    if ctx.face != "front":
        return col
    s = (ctx.s + 0.5) / max(ctx.w, 1) - 0.5
    t = (ctx.t + 0.5) / max(ctx.h, 1) - 0.5
    lid = (s / 0.36) ** 2 + (t / 0.34) ** 2
    if lid < 1.0:
        r = math.hypot(s / 0.36 * 0.36, t)
        if r < 0.09:
            col = hexc("#0a0a1a")                                                     # Pupille
            ctx.glow = None
        elif r < 0.2:
            col = pick(ctx.pal["gem"], 0.62 + (0.2 - r) * 2.0)                         # Iris
            ctx.glow = col
        else:
            col = pick(ctx.pal["light"], 0.92)                                         # das Weiße, hell
            ctx.glow = mix((0, 0, 0, 255), col, 0.55)
    elif lid < 1.35:
        col = pick(ctx.pal["gold"], 0.9)                                               # Lidrand
        ctx.glow = mix((0, 0, 0, 255), pick(ctx.pal["light"], 0.7), 0.5)
    return col


def _ring_rune(ctx: Ctx):
    """Ein Glied der Räder ohne Auge: Gold mit leuchtenden Runen."""
    col = _gold(ctx)
    if ctx.face in ("front", "back"):
        k = int(hash01(ctx.cube.tag, "rune") * 4)
        s, t = ctx.s, ctx.t
        mark = ((s + k) % 5 == 0 and 1 <= t <= ctx.h - 2) or (t == ctx.h // 2 and (s + k * 2) % 5 in (1, 2))
        if mark:
            col = pick(ctx.pal["light"], 0.95)
            ctx.glow = col
    return col


def _corona(ctx: Ctx):
    """Ein langer Strahl des Kranzes: innen weiß, außen golden, zur Spitze hin verblassend."""
    t = (ctx.t + 0.5) / max(ctx.h, 1)
    s = (ctx.s + 0.5) / max(ctx.w, 1) - 0.5
    width = 0.5 - t * 0.38
    if abs(s) > width:
        return None
    col = pick(ctx.pal["light"], 0.98 - t * 0.5 - abs(s) * 0.6)
    ctx.glow = mix((0, 0, 0, 255), col, 1.0 - t * 0.45)
    return col[:3] + (round(255 - t * 120),)


def _embroidery(ctx: Ctx) -> bool:
    """Goldene Stickerei: ein Rankenband aus kleinen Ringen und Punkten."""
    s, t = ctx.s, ctx.t
    return (s + t * 2) % 7 == 0 or (s % 6 == 3 and t % 4 == 1)


def _tunic(ctx: Ctx):
    col = _cloth_base(ctx, 0.72)
    if ctx.face in ("front", "back") and ctx.s in (0, 1, ctx.w - 2, ctx.w - 1):
        col = pick(ctx.pal["gold"], 0.6)
    return col


def _gold(ctx: Ctx):
    x, y, z = ctx.world
    v = 0.56 + _n(ctx, 0.6, 14) * 0.18 + (0.16 if ctx.face == "top" else -0.12 if ctx.face == "bottom" else 0.0)
    col = pick(ctx.pal["gold"], v)
    e = _edge(ctx)
    if e == 0:
        col = pick(ctx.pal["gold"], v - 0.2)
    elif e == 1 and ctx.face != "bottom":
        col = pick(ctx.pal["gold"], v + 0.22)
    if hash01(ctx.u, ctx.v, 3) > 0.985:
        col = pick(ctx.pal["light"], 0.9)
        ctx.glow = mix((0, 0, 0, 255), col, 0.6)
    return col


def _gold_edge(ctx: Ctx):
    col = _gold(ctx)
    if ctx.face in ("top", "front", "back"):
        ctx.glow = mix((0, 0, 0, 255), pick(ctx.pal["light"], 0.6), 0.35)
    return col


def _gold_band(ctx: Ctx):
    col = _gold(ctx)
    if ctx.face in ("front", "back", "west", "east") and ctx.s % 4 == 1:
        col = pick(ctx.pal["gem"], 0.7)
        ctx.glow = col
    return col


def _cuirass(ctx: Ctx):
    """Der Brustpanzer: Gold, in der Mitte eine Sonne aus Licht mit zwölf Strahlen."""
    col = _gold(ctx)
    if ctx.face != "front":
        return col
    s = (ctx.s + 0.5) / max(ctx.w, 1) - 0.5
    t = (ctx.t + 0.5) / max(ctx.h, 1) - 0.42
    r = math.hypot(s * 1.0, t * 0.82)
    a = math.atan2(t, s)
    ray = abs(math.sin(a * 6.0))
    if r < 0.12:
        col = pick(ctx.pal["light"], 0.98 - r * 2.0)
        ctx.glow = col
    elif r < 0.15:
        col = pick(ctx.pal["gold"], 0.95)
        ctx.glow = mix((0, 0, 0, 255), pick(ctx.pal["light"], 0.7), 0.6)
    elif r < 0.36 and ray < 0.22 + (0.36 - r) * 0.6:
        col = pick(ctx.pal["light"], 0.82 - r)
        ctx.glow = mix((0, 0, 0, 255), col, 0.85 - r * 1.5)
    elif ctx.t in (1, ctx.h - 2):
        col = pick(ctx.pal["gold"], 0.85)
    return col


def _gem(ctx: Ctx):
    s = (ctx.s + 0.5) / max(ctx.w, 1)
    t = (ctx.t + 0.5) / max(ctx.h, 1)
    v = 0.45 + max(0.0, 0.35 - math.hypot(s - 0.35, t - 0.3)) * 1.6 + (0.1 if ctx.face == "top" else 0.0)
    col = pick(ctx.pal["gem"], v)
    ctx.glow = col
    return col


def _stole(ctx: Ctx):
    if ctx.face not in ("front", "back"):
        return pick(ctx.pal["gold"], 0.45)
    col = _cloth_base(ctx, 0.62)
    col = mix(col, pick(ctx.pal["gem"], 0.55), 0.22)
    if ctx.s in (0, ctx.w - 1):
        col = pick(ctx.pal["gold"], 0.62)
    elif (ctx.t // 3) % 4 == 0 and ctx.t % 3 == 1:
        col = pick(ctx.pal["gold"], 0.75)                                            # goldene Querstreifen
        ctx.glow = mix((0, 0, 0, 255), pick(ctx.pal["light"], 0.6), 0.3)
    if ctx.t >= ctx.h - 3:
        col = pick(ctx.pal["gold"], 0.7)                                             # Fransen
        if ctx.t == ctx.h - 1 and ctx.s % 2 == 1:
            return None
    return col


def _skin(ctx: Ctx):
    return pick(ctx.pal["skin"], 0.74 + _LIT[ctx.face] - rel_y(ctx) * 0.05 + (hash01(ctx.u, ctx.v) - 0.5) * 0.03)


def _face(ctx: Ctx):
    """Gesicht (16×16): ruhige, junge Züge, goldene Brauen, leuchtende Augen, ein feines Lächeln."""
    face, s, t = ctx.face, ctx.s, ctx.t
    if face == "front":
        if t <= 2:
            return _hair(ctx)
        if t == 5 and (3 <= s <= 6 or 9 <= s <= 12):
            return pick(ctx.pal["hair"], 0.55)                                       # Brauen
        if t in (6, 7) and (3 <= s <= 6 or 9 <= s <= 12):
            inner = s in (4, 5, 10, 11)
            col = pick(ctx.pal["eye"], 0.92 if inner else 0.6)
            ctx.glow = col
            return col
        if t == 8 and (s in (4, 5, 10, 11)):
            return pick(ctx.pal["skin"], 0.62)                                       # Lidschatten unter dem Auge
        if 8 <= t <= 10 and s in (7, 8):
            return pick(ctx.pal["skin"], 0.84 if t < 10 else 0.66)                   # Nase
        if t == 12 and s in (6, 9):
            return pick(ctx.pal["skin"], 0.56)                                       # Mundwinkel
        if t == 12 and 7 <= s <= 8:
            return mix(pick(ctx.pal["skin"], 0.6), hexc("#c8786a"), 0.35)
        if t == 13 and 7 <= s <= 8:
            return mix(pick(ctx.pal["skin"], 0.72), hexc("#d89080"), 0.25)
        if t in (9, 10) and s in (2, 3, 12, 13):
            return mix(pick(ctx.pal["skin"], 0.8), hexc("#f0a890"), 0.18)            # Wangen
        return pick(ctx.pal["skin"], 0.82 - (0.05 if s in (0, 15) else 0.0) + (hash01(ctx.u, ctx.v) - 0.5) * 0.02)
    if face in ("west", "east"):
        if t <= 3 or s >= 10:
            return _hair(ctx)
        if t in (7, 8) and s in (5, 6):
            return pick(ctx.pal["skin"], 0.66)                                       # Ohr
        return _skin(ctx)
    if face in ("top", "back"):
        return _hair(ctx)
    return pick(ctx.pal["skin"], 0.6)


def _hair(ctx: Ctx):
    strand = hash01(ctx.s, ctx.face, ctx.cube.origin, "strand")
    v = 0.5 + strand * 0.25 + _LIT[ctx.face] - rel_y(ctx) * 0.1
    if hash01(ctx.u, ctx.v, "shine") > 0.9:
        v += 0.2
    col = pick(ctx.pal["hair"], v)
    if hash01(ctx.u, ctx.v, "glint") > 0.985:
        ctx.glow = mix((0, 0, 0, 255), pick(ctx.pal["light"], 0.8), 0.6)
    return col


def _bangs(ctx: Ctx):
    if ctx.face == "front" and ctx.t == ctx.h - 1 and ctx.s % 3 == 1:
        return None
    return _hair(ctx)


def _hair_long(ctx: Ctx):
    if ctx.face == "front":
        return pick(ctx.pal["hair"], 0.3)
    return _hair(ctx)


def _hair_tip(ctx: Ctx):
    if ctx.face in ("back", "west", "east", "front") and ctx.t >= ctx.h - 3:
        k = hash01(ctx.s, "tip", ctx.cube.origin)
        if ctx.t >= ctx.h - 3 + int(k * 3):
            return None
    return _hair(ctx)


def _ray(ctx: Ctx):
    """Ein Lichtstrahl (Krone, Heiligenschein, Sonnenstern): zur Spitze hin weißer."""
    t = (ctx.t + 0.5) / max(ctx.h, 1)
    v = 0.55 + (1.0 - t) * 0.4 if ctx.face not in ("top", "bottom") else 0.95
    col = pick(ctx.pal["light"], v)
    ctx.glow = col
    return col


def _halo(ctx: Ctx):
    s = (ctx.s + 0.5) / max(ctx.w, 1)
    t = (ctx.t + 0.5) / max(ctx.h, 1)
    v = 0.66 + (0.24 if ctx.face in ("front", "back") and 0.3 < t < 0.7 else 0.0) + math.sin(s * math.pi * 4) * 0.04
    if ctx.face in ("front", "back") and (ctx.t == 0 or ctx.t == ctx.h - 1):
        col = pick(ctx.pal["gold"], 0.72)
        ctx.glow = mix((0, 0, 0, 255), col, 0.7)
        return col
    col = pick(ctx.pal["light"], v)
    ctx.glow = col
    return col


def _halo_thin(ctx: Ctx):
    col = pick(ctx.pal["light"], 0.82)
    ctx.glow = mix((0, 0, 0, 255), col, 0.85)
    return col


def _sun_gem(ctx: Ctx):
    s = (ctx.s + 0.5) / max(ctx.w, 1)
    t = (ctx.t + 0.5) / max(ctx.h, 1)
    v = 0.6 + max(0.0, 0.4 - math.hypot(s - 0.4, t - 0.35)) * 1.2
    col = pick(ctx.pal["light"], v)
    ctx.glow = col
    return col


def _sleeve(ctx: Ctx):
    col = _cloth_base(ctx, 0.7)
    r = rel_y(ctx)
    if r > 0.82:
        col = pick(ctx.pal["gold"], 0.62 + (0.15 if ctx.t % 2 == 0 else 0.0))       # goldener Saum
    elif r > 0.74 and _embroidery(ctx):
        col = pick(ctx.pal["gold"], 0.7)
    return col


def _bracer(ctx: Ctx):
    col = _gold(ctx)
    if ctx.face in ("front", "west", "east", "back") and 3 <= ctx.t <= 5 and ctx.s % 4 == 2:
        col = pick(ctx.pal["gem"], 0.72)
        ctx.glow = col
    return col


def _staff(ctx: Ctx):
    col = _gold(ctx)
    if ctx.face in ("front", "back", "west", "east") and ctx.t % 9 in (0, 1):
        col = pick(ctx.pal["cloth"], 0.9)                                            # weiße Bänder
    if ctx.face in ("front", "back", "west", "east") and ctx.t % 18 == 9:
        col = pick(ctx.pal["gem"], 0.75)
        ctx.glow = col
    return col


def _globe(ctx: Ctx):
    """Ein Himmelsglobus: tiefblau, mit goldenen Ländern und hellen Sternen."""
    x, y, z = ctx.world
    if _true(ctx):
        v = 0.6 + noise3(x * 0.9, y * 0.9, z * 0.9, 64) * 0.4
        col = pick(ctx.pal["light"], v)
        ctx.glow = col
        return col
    land = noise3(x * 0.55 + 3, y * 0.55, z * 0.55, 61)
    v = 0.4 + _LIT[ctx.face] * 1.5 + _n(ctx, 0.8, 62) * 0.2
    col = pick(ctx.pal["globe"], v)
    if land > 0.6:
        col = pick(ctx.pal["gold"], 0.55 + (land - 0.6) * 1.4)
        ctx.glow = mix((0, 0, 0, 255), pick(ctx.pal["light"], 0.6), 0.45)
    elif hash01(ctx.u, ctx.v, 63) > 0.95:
        col = pick(ctx.pal["light"], 0.95)
        ctx.glow = col
    else:
        ctx.glow = mix((0, 0, 0, 255), pick(ctx.pal["gem"], 0.6), 0.35)
    return col


def _robe(ctx: Ctx):
    col = _cloth_base(ctx, 0.66)
    if ctx.face in ("front", "back", "west", "east"):
        r = rel_y(ctx)
        if ctx.t <= 1:
            col = pick(ctx.pal["gold"], 0.6)
        elif _embroidery(ctx) and r > 0.55:
            col = pick(ctx.pal["gold"], 0.68)
    return col


def _robe_stole(ctx: Ctx):
    """Die Vorderbahn des Gewands: in der Mitte die Stola – tiefblau, goldgesäumt, mit goldenen Sonnenkreuzen."""
    col = _robe(ctx)
    if ctx.face != "front":
        return col
    c = ctx.w / 2 - 0.5
    d = abs(ctx.s - c)
    half = ctx.w * 0.16
    if d <= half:
        if _true(ctx):
            col = mix(pick(ctx.pal["light"], 0.75), pick(ctx.pal["gold"], 0.8), 0.4)
            ctx.glow = mix((0, 0, 0, 255), col, 0.7)
        else:
            col = mix(pick(ctx.pal["gem"], 0.32), pick(ctx.pal["globe"], 0.3), 0.5)
        if d >= half - 1:
            col = pick(ctx.pal["gold"], 0.72)
        elif ctx.t % 9 in (3, 4, 5) and d < 1.2 or ctx.t % 9 == 4 and d < 2.5:
            col = pick(ctx.pal["gold"], 0.85)                                       # Sonnenkreuz
            ctx.glow = mix((0, 0, 0, 255), pick(ctx.pal["light"], 0.7), 0.45)
    return col


def _cuff(ctx: Ctx):
    col = _cloth_base(ctx, 0.74)
    if ctx.face in ("front", "back", "west", "east"):
        if ctx.t >= ctx.h - 2:
            col = pick(ctx.pal["gold"], 0.66 + (0.12 if ctx.s % 3 == 0 else 0.0))
        elif ctx.t == ctx.h - 3 and ctx.s % 2 == 0:
            col = pick(ctx.pal["gold"], 0.78)
    return col


def _robe_light(ctx: Ctx):
    """Der Saum des Gewands vergeht in Licht: unten zerfasert, durchscheinend und leuchtend."""
    t = (ctx.t + 0.5) / max(ctx.h, 1)
    along = ctx.s
    cut = 0.62 + 0.3 * abs(math.sin(along * 0.8 + hash01(ctx.cube.bone.name) * 6)) + (hash01(along, ctx.cube.bone.name, 45) - 0.5) * 0.18
    if t > cut:
        return None
    col = mix(_cloth_base(ctx, 0.8), pick(ctx.pal["light"], 0.9), max(0.0, (t - 0.25) / 0.7))
    glow = max(0.0, (t - 0.3) / 0.6)
    if glow > 0:
        ctx.glow = mix((0, 0, 0, 255), pick(ctx.pal["light"], 0.85), min(1.0, glow))
    alpha = 255 if t < cut - 0.3 else round(255 - (t - (cut - 0.3)) / 0.3 * 140)
    return col[:3] + (max(110, alpha),)


def _wing_arm(ctx: Ctx):
    col = pick(ctx.pal["feather"], 0.78 + _LIT[ctx.face] + _n(ctx, 0.9, 71) * 0.08)
    if ctx.face == "top" or ctx.t == 0:
        col = pick(ctx.pal["gold"], 0.7)
        ctx.glow = mix((0, 0, 0, 255), pick(ctx.pal["light"], 0.5), 0.25)
    return col


def _feather_rows(ctx: Ctx, rows: float, tip_gold: float):
    """Federn auf einer Fläche: Reihen von Federn (Kiel, Fahne, Spitze), unten gezackt, die Spitzen golden."""
    w = ctx.w
    feather_w = 4
    k = ctx.s // feather_w
    within = (ctx.s % feather_w) / (feather_w - 1)
    jag = hash01(k, ctx.cube.tag, "jag")
    t = (ctx.t + 0.5) / max(ctx.h, 1)
    # Spitze der Feder: abgerundet, jede ein wenig anders lang
    end = 0.86 + jag * 0.14
    if t > end - abs(within - 0.5) * 0.22:
        return None
    v = 0.84 - t * 0.12 + (hash01(k, ctx.cube.tag, "shade") - 0.5) * 0.08
    if abs(within - 0.5) < 0.12:
        v -= 0.16                                                                    # der Kiel
    elif within < 0.08 or within > 0.92:
        v -= 0.1                                                                     # Rand zur Nachbarfeder
    col = pick(ctx.pal["feather"], v)
    if t > end - tip_gold:
        tip = pick(ctx.pal["prism"], hash01(k, ctx.cube.tag, "prism")) if _true(ctx) else pick(ctx.pal["gold"], 0.72)
        col = mix(col, tip, 0.75)
        ctx.glow = mix((0, 0, 0, 255), tip if _true(ctx) else pick(ctx.pal["light"], 0.75), 0.7 if _true(ctx) else 0.55)
    elif ctx.face == "back":
        col = mix(col, pick(ctx.pal["shadow"], 0.6), 0.25)
    return col


def _covert(ctx: Ctx):
    return _feather_rows(ctx, 1, 0.12)


def _feather(ctx: Ctx):
    return _feather_rows(ctx, 1, 0.16)


def _primary(ctx: Ctx):
    """Eine lange Schwungfeder: schmal, mit Kiel, zur Spitze hin golden und leuchtend."""
    s = (ctx.s + 0.5) / max(ctx.w, 1)
    t = (ctx.t + 0.5) / max(ctx.h, 1)
    width = 0.5 - max(0.0, t - 0.55) * 0.9
    if abs(s - 0.5) > width or t > 0.97:
        return None
    v = 0.86 - t * 0.12
    if abs(s - 0.5) < 0.08:
        v -= 0.16
    col = pick(ctx.pal["feather"], v)
    if _true(ctx) and t > 0.4:
        # schillernd: von der Mitte zur Spitze durch die Farben des Lichts
        tip = pick(ctx.pal["prism"], (t - 0.4) / 0.6 * 0.85 + hash01(ctx.cube.tag, "p") * 0.15)
        col = mix(col, tip, min(1.0, (t - 0.4) * 2.2))
        ctx.glow = mix((0, 0, 0, 255), tip, min(0.9, (t - 0.4) * 2.0))
    elif t > 0.74:
        col = mix(col, pick(ctx.pal["gold"], 0.75), min(1.0, (t - 0.74) * 4.0))
        ctx.glow = mix((0, 0, 0, 255), pick(ctx.pal["light"], 0.8), min(0.8, (t - 0.74) * 3.5))
    elif ctx.face == "back":
        col = mix(col, pick(ctx.pal["shadow"], 0.6), 0.25)
    return col


def materials(variant: str) -> dict:
    return {
        "tunic": _tunic, "cuirass": _cuirass, "gold": _gold, "gold_edge": _gold_edge, "gold_band": _gold_band, "gem": _gem,
        "stole": _stole, "face": _face, "skin": _skin, "hair": _hair, "bangs": _bangs, "hair_long": _hair_long,
        "hair_tip": _hair_tip, "ray": _ray, "halo": _halo, "halo_thin": _halo_thin, "sun_gem": _sun_gem, "sleeve": _sleeve,
        "bracer": _bracer, "staff": _staff, "globe": _globe, "robe": _robe, "robe_light": _robe_light,
        "wing_arm": _wing_arm, "covert": _covert, "feather": _feather, "primary": _primary,
        "ring_eye": _ring_eye, "ring_rune": _ring_rune, "corona": _corona, "robe_stole": _robe_stole, "cuff": _cuff,
    }
