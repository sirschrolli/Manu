"""
Der Stundenwächter (1.01): der Wächter des Uhrturms im Abgrund. Als die Welten zerbrachen, blieb die große Uhr stehen –
eine Minute vor Mitternacht; ihr Turm stürzte in den Abgrund und mit ihm ihr Wächter, ein Ritter aus Uhrbronze. Im Spiel
1,6-fach vergrößert (gut viereinhalb Blöcke hoch, mit dem Zifferblatt hinter dem Kopf über fünf).

  * ein breiter Ritter in Plattenrüstung aus Uhrbronze (warm glänzend, mit Nieten und gravierten Säumen) über geschwärztem
    Stahl und Kettengeflecht
  * in der Brust ein Fenster mit Messingrahmen, dahinter greifen zwei Zahnräder ineinander und drehen sich
  * hinter dem Kopf ein großes Zifferblatt wie ein Heiligenschein: Email mit Minutenring, zwölf Stundenmarken (die vier
    Hauptstunden aus leuchtendem Chronit), ein Lünettenring aus Bronze; davor die gebläuten Zeiger (die in Java laufen)
  * ein geschlossener Topfhelm mit T-förmigem Sehschlitz, aus dem Bernsteinlicht glüht; oben ein Kamm wie ein Uhrzeiger
  * Schulterstücke aus drei gestaffelten Platten, außen je ein Zahnrad; Armkacheln, Panzerhandschuhe
  * rechts der Minutenzeiger als Langschwert (gebläuter Stahl, Messingseele, durchbrochenes Ornament und eine Rautenspitze),
    links der Stundenzeiger (kürzer, breiter, mit einer Lilienspitze)
  * am Gürtel ein Pendel mit einer Chronitscheibe, an den Hüften zwei Uhrgewichte wie Tannenzapfen an Ketten
  * drei kleine Zahnräder kreisen um ihn (Knochen orbit_0..2, in Java bewegt)

Boden-Regel: y = 24 ist der Boden, oben ist -y, vorn ist -z. Rechts ist -x (wie bei Minecraft-Menschen). Die Knochen-Namen
verwendet HourWardenModel (Java): hips (tasset_*, pendulum, weight_l, weight_r), leg_l/leg_r > shin_l/shin_r (> knee_gear_*),
body > chest (> gear_big, gear_small, head > crest, dial > hand_minute, hand_hour, pauldron_l/r > pgear_l/r, arm_l/arm_r >
fore_l/fore_r > hour_blade / minute_blade), orbit_0..2.
"""
from __future__ import annotations

import math

from ..geometry import Model
from ..paint import Ctx, hash01, hexc, mix, noise3, pick, rel_y

PALETTES = {
    "hour_warden": {
        "bronze": [hexc(c) for c in ("#1e1006", "#331c0a", "#4e2d10", "#6c4016", "#8c561e", "#aa6e28", "#c38634", "#d8a046",
                                     "#e8bc62", "#f6d88e", "#fff2c8")],
        "brass": [hexc(c) for c in ("#3a2608", "#5a3c0e", "#7c5716", "#a07420", "#c4942e", "#deb44a", "#f0d27a", "#fff0b8")],
        "patina": [hexc(c) for c in ("#1a221a", "#24332a", "#345041", "#4a6e58", "#679078")],
        "iron": [hexc(c) for c in ("#07080b", "#0e1015", "#16191f", "#20242c", "#2c313b", "#3a404c", "#4c5361", "#646c7b",
                                   "#838b9a")],
        "blued": [hexc(c) for c in ("#06081a", "#0b1030", "#121a4a", "#1b2766", "#283a86", "#3a52a6", "#5670c2", "#7e96dc",
                                    "#b4c6f2")],
        "enamel": [hexc(c) for c in ("#6e6250", "#8f826c", "#ad9f86", "#c7baa0", "#dcd0b6", "#ebe1ca", "#f6efdd", "#fffaf0")],
        "ink": [hexc(c) for c in ("#0a0a12", "#141626", "#1f233a", "#2d324e")],
        "amber": [hexc(c) for c in ("#6a2c06", "#a2480c", "#d26a16", "#f08f24", "#ffb347", "#ffd27e", "#fff0c4")],
        "leather": [hexc(c) for c in ("#140b06", "#22130a", "#331d10", "#472817", "#5c3520")],
        "works": [hexc(c) for c in ("#050404", "#0d0906", "#170f09", "#22160c", "#2f1f10")],
    }
}

_LIT = {"top": 0.08, "front": 0.03, "west": 0.0, "east": -0.03, "back": -0.06, "bottom": -0.14}

# Haltung der Arme in der Ruhe (Grad um x, y, z) und der Unterarme; die Klingen zeigen schräg nach vorn unten
R_ARM = (-6.0, 0.0, 8.0)
L_ARM = (-6.0, 0.0, -8.0)
FORE = (-28.0, 0.0, 0.0)
# Lage der Klingen in der Faust: die Klinge zeigt entlang -y (des Klingenknochens); gedreht zeigt sie nach vorn unten
BLADE_TILT = (160.0, 0.0, 0.0)


# =============================================================================================
# Aufbau
# =============================================================================================

def _disc(m: Model, bone: str, cx: float, cy: float, cz: float, size: float, depth: float, material: str, axis: str = "z",
          tag: str = "") -> None:
    """Eine runde Scheibe (gemalt; außen durchsichtig) mit der Mitte (cx, cy, cz): flach in der Ebene quer zu {axis}."""
    h = size / 2.0
    if axis == "z":
        m.cube(bone, (cx - h, cy - h, cz - depth / 2.0), (size, size, depth), material, tag=tag, faces=("front", "back"))
    else:
        m.cube(bone, (cx - depth / 2.0, cy - h, cz - h), (depth, size, size), material, tag=tag, faces=("west", "east"))


def _blade(m: Model, bone: str, length: float, width: float, tip: float, kind: str) -> None:
    """Eine Zeigerklinge entlang -y (Griff um den Ursprung): Knauf, Griff, Parierstange mit Zahnrädern, Klinge, Spitze."""
    m.cube(bone, (-0.85, 1.9, -0.85), (1.7, 1.7, 1.7), "brass")                       # der Knauf
    m.cube(bone, (-0.55, -2.1, -0.55), (1.1, 4.1, 1.1), "grip")                       # der Griff
    m.cube(bone, (-2.6, -3.0, -0.75), (5.2, 0.9, 1.5), "brass")                       # die Parierstange
    _disc(m, bone, -2.9, -2.55, 0.0, 2.0, 0.6, "gear_small")
    _disc(m, bone, 2.9, -2.55, 0.0, 2.0, 0.6, "gear_small")
    m.cube(bone, (-1.0, -4.4, -0.45), (2.0, 1.4, 0.9), "brass")                       # die Klingenwurzel
    body = length - tip
    m.cube(bone, (-width / 2.0, -4.4 - body, -0.3), (width, body, 0.6), f"{kind}_blade", faces=("front", "back", "west", "east"))
    m.cube(bone, (-tip * 0.6, -4.4 - body - tip, -0.3), (tip * 1.2, tip, 0.6), f"{kind}_tip", faces=("front", "back"))
    # das durchbrochene Ornament nahe der Wurzel (ein Ring mit Chronit)
    _disc(m, bone, 0.0, -4.4 - body * 0.22, 0.0, width + 1.6, 0.6, "ornament")


def build() -> Model:
    m = Model("hour_warden", 256, 128)
    m.tex_res = 2

    # ------------------------------------------------------------------ Beine: Beintaschen, Kniekacheln mit Zahnrad, Schuhe
    for sx in (1, -1):
        side = "l" if sx > 0 else "r"
        leg = f"leg_{side}"
        m.bone(leg, pivot=(3.0 * sx, 6.5, 0.2))
        m.cube(leg, (-2.1, 0.0, -2.2), (4.2, 8.0, 4.4), "mail")
        m.cube(leg, (-2.4, 0.4, -2.6), (4.8, 5.4, 2.4), "bronze")                    # die Beintasche vorn
        m.cube(leg, (-2.5, 0.2, -2.7), (5.0, 0.8, 2.6), "brass")
        shin = f"shin_{side}"
        m.bone(shin, leg, pivot=(0.0, 8.0, 0.0))
        m.cube(shin, (-1.9, 0.0, -2.0), (3.8, 8.6, 4.0), "iron")
        m.cube(shin, (-2.2, 1.2, -2.4), (4.4, 6.6, 2.6), "bronze")                    # die Beinschiene
        m.cube(shin, (-2.3, 7.6, -2.5), (4.6, 0.7, 2.7), "brass")
        m.cube(shin, (-2.3, 8.4, -4.6), (4.6, 1.6, 6.8), "sabaton")                    # der Schuh
        m.cube(shin, (-1.9, 8.8, -5.6), (3.8, 1.2, 1.2), "sabaton")
        m.cube(shin, (-2.0, -1.2, -2.9), (4.0, 2.6, 1.2), "bronze")                   # die Kniekachel
        m.bone(f"knee_gear_{side}", shin, pivot=(0.0, 0.1, -3.6))
        _disc(m, f"knee_gear_{side}", 0.0, 0.0, 0.0, 2.6, 0.5, "gear_small")

    # ------------------------------------------------------------------ Hüften: Gürtel, Beintaschen, Pendel, Uhrgewichte
    m.bone("hips", pivot=(0.0, 5.0, 0.0))
    m.cube("hips", (-4.6, -2.6, -2.9), (9.2, 4.4, 5.8), "iron")
    m.cube("hips", (-4.9, -2.9, -3.2), (9.8, 1.6, 6.4), "belt")
    m.cube("hips", (-1.3, -3.1, -3.6), (2.6, 2.0, 0.6), "buckle")
    for name, x, z, yaw, w in (("tasset_fl", 2.2, -3.2, 0.0, 3.8), ("tasset_fr", -2.2, -3.2, 0.0, 3.8),
                               ("tasset_bl", 2.2, 3.2, 180.0, 3.8), ("tasset_br", -2.2, 3.2, 180.0, 3.8)):
        m.bone(name, "hips", pivot=(x, -1.4, z), rotation=(-9.0, yaw, 0.0))
        m.cube(name, (-w / 2.0, 0.0, -0.4), (w, 5.2, 0.8), "tasset")
    for name, x, rz in (("tasset_l", 4.9, -7.0), ("tasset_r", -4.9, 7.0)):
        m.bone(name, "hips", pivot=(x, -1.4, 0.0), rotation=(0.0, 0.0, rz))
        m.cube(name, (-0.4, 0.0, -2.5), (0.8, 5.6, 5.0), "tasset")
    # das Pendel vor dem Gürtel (schwingt in Java)
    m.bone("pendulum", "hips", pivot=(0.0, -1.2, -3.9))
    m.cube("pendulum", (-0.3, 0.0, -0.3), (0.6, 6.4, 0.6), "brass")
    _disc(m, "pendulum", 0.0, 7.6, 0.0, 3.6, 0.6, "bob")
    # zwei Uhrgewichte an Ketten an den Hüften
    for sx in (1, -1):
        name = "weight_l" if sx > 0 else "weight_r"
        m.bone(name, "hips", pivot=(5.0 * sx, -1.8, -1.2))
        m.cube(name, (-0.25, 0.0, -0.25), (0.5, 5.4, 0.5), "chain")
        m.cube(name, (-1.1, 5.2, -1.1), (2.2, 0.6, 2.2), "brass")
        m.cube(name, (-0.95, 5.8, -0.95), (1.9, 4.4, 1.9), "weight")
        m.cube(name, (-0.5, 10.2, -0.5), (1.0, 0.8, 1.0), "brass")

    # ------------------------------------------------------------------ Leib: Bauch und die Brust mit dem Uhrwerkfenster
    m.bone("body", pivot=(0.0, 3.0, 0.0))
    m.cube("body", (-4.2, -5.6, -2.7), (8.4, 5.8, 5.4), "mail")
    for i, y in enumerate((-5.4, -3.6, -1.8)):
        m.cube("body", (-4.5 + i * 0.15, y, -2.95), (9.0 - i * 0.3, 1.6, 0.9), "lame")   # die Bauchreifen
    m.bone("chest", "body", pivot=(0.0, -5.0, 0.0))
    m.cube("chest", (-5.6, -11.0, -2.6), (11.2, 11.0, 6.4), "works")                # der Kern (im Fenster sichtbar)
    # der Harnisch vorn um das Fenster (x -3..3, y -8..-2): oben, unten, links, rechts, die Ecken schräg
    m.cube("chest", (-6.0, -11.4, -3.9), (12.0, 3.4, 1.4), "cuirass")
    m.cube("chest", (-6.0, -2.0, -3.9), (12.0, 2.2, 1.4), "cuirass")
    m.cube("chest", (-6.0, -8.0, -3.9), (3.0, 6.0, 1.4), "cuirass")
    m.cube("chest", (3.0, -8.0, -3.9), (3.0, 6.0, 1.4), "cuirass")
    for cx, cy in ((-3.0, -8.0), (2.0, -8.0), (-3.0, -3.0), (2.0, -3.0)):
        m.cube("chest", (cx, cy, -3.9), (1.0, 1.0, 1.4), "frame")                    # die Ecken: ein Achteck
    m.cube("chest", (-6.0, -11.4, -2.6), (1.0, 11.6, 6.6), "cuirass")                # die Seiten
    m.cube("chest", (5.0, -11.4, -2.6), (1.0, 11.6, 6.6), "cuirass")
    m.cube("chest", (-5.6, -11.4, 3.6), (11.2, 11.6, 1.2), "backplate")              # der Rücken
    m.cube("chest", (-3.3, -13.0, -3.0), (6.6, 1.8, 6.2), "gorget")                  # der Halsberge
    # die Zahnräder im Fenster (drehen sich in Java)
    m.bone("gear_big", "chest", pivot=(-0.6, -4.6, -2.75))
    _disc(m, "gear_big", 0.0, 0.0, 0.0, 5.4, 0.4, "gear")
    m.bone("gear_small", "chest", pivot=(1.9, -6.6, -3.05))
    _disc(m, "gear_small", 0.0, 0.0, 0.0, 3.0, 0.4, "gear_small")

    # ------------------------------------------------------------------ Kopf: Topfhelm mit Sehschlitz und Zeigerkamm
    m.bone("head", "chest", pivot=(0.0, -12.6, -0.3))
    m.cube("head", (-3.1, -7.2, -3.3), (6.2, 7.2, 6.6), "helm")
    m.cube("head", (-3.4, -7.6, -3.6), (6.8, 1.0, 7.2), "brass")                     # der Kronreif
    m.cube("head", (-3.3, -0.8, -3.5), (6.6, 0.8, 7.0), "brass")                     # der Saum unten
    m.cube("head", (-0.45, -7.0, -3.75), (0.9, 2.3, 0.5), "brass")                   # die Mittelleiste über der Stirn
    m.cube("head", (-3.25, -5.3, -3.7), (6.5, 0.6, 0.45), "brass")                   # der Brauenreif über dem Sehschlitz
    m.bone("crest", "head", pivot=(0.0, -7.6, 0.0))
    m.cube("crest", (-0.3, -4.2, -3.6), (0.6, 4.2, 7.6), "crest", faces=("west", "east", "top"))

    # ------------------------------------------------------------------ das Zifferblatt hinter dem Kopf mit den Zeigern
    m.bone("dial", "chest", pivot=(0.0, -15.6, 4.9), rotation=(-8.0, 0.0, 0.0))
    _disc(m, "dial", 0.0, 0.0, 0.0, 20.0, 0.6, "dial")
    m.cube("dial", (-0.6, 9.4, -0.3), (1.2, 4.2, 0.8), "brass")                      # die Halterung zum Rücken
    m.bone("hand_hour", "dial", pivot=(0.0, 0.0, -0.45))
    m.cube("hand_hour", (-1.0, -6.4, -0.15), (2.0, 7.4, 0.3), "hand_hour", faces=("front", "back"))
    m.bone("hand_minute", "dial", pivot=(0.0, 0.0, -0.7))
    m.cube("hand_minute", (-1.0, -9.4, -0.15), (2.0, 10.6, 0.3), "hand_minute", faces=("front", "back"))
    m.cube("hand_minute", (-0.9, -0.9, -0.55), (1.8, 1.8, 0.5), "hub")

    # ------------------------------------------------------------------ Schultern, Arme, Klingen
    for sx in (1, -1):
        side = "l" if sx > 0 else "r"
        pd = f"pauldron_{side}"
        m.bone(pd, "chest", pivot=(5.6 * sx, -10.8, 0.4), rotation=(0.0, 0.0, -10.0 * sx))
        lo = (lambda w: 0.0 if sx > 0 else -w)
        m.cube(pd, (lo(5.2) - 0.6 * sx, -1.2, -3.8), (5.2, 2.4, 7.6), "pauldron")
        m.cube(pd, (lo(5.0) + 0.6 * sx, 0.9, -3.6), (5.0, 2.0, 7.2), "pauldron")
        m.cube(pd, (lo(4.6) + 1.5 * sx, 2.6, -3.4), (4.6, 1.8, 6.8), "pauldron")
        m.cube(pd, (lo(5.4) - 0.7 * sx, -1.5, -4.0), (5.4, 0.5, 8.0), "brass")        # der Saum oben
        m.bone(f"pgear_{side}", pd, pivot=(6.5 * sx, 1.2, 0.0))
        _disc(m, f"pgear_{side}", 0.0, 0.0, 0.0, 4.6, 0.6, "gear", axis="x")
        arm = f"arm_{side}"
        m.bone(arm, "chest", pivot=(6.9 * sx, -9.6, 0.4), rotation=L_ARM if sx > 0 else R_ARM)
        m.cube(arm, (-1.8, -0.6, -1.8), (3.6, 7.0, 3.6), "mail")
        m.cube(arm, (-2.0, 1.2, -2.0), (4.0, 3.8, 4.0), "bronze")                     # die Oberarmröhre
        fore = f"fore_{side}"
        m.bone(fore, arm, pivot=(0.0, 6.6, 0.0), rotation=FORE)
        m.cube(fore, (-2.2, -0.9, -2.2), (4.4, 2.2, 4.4), "couter")                  # die Armkachel
        m.cube(fore, (-1.9, 1.0, -1.9), (3.8, 5.4, 3.8), "bronze")                    # die Unterarmröhre
        m.cube(fore, (-2.2, 6.2, -2.3), (4.4, 3.2, 4.6), "gauntlet")                 # der Panzerhandschuh
        m.cube(fore, (-2.3, 5.6, -2.4), (4.6, 0.8, 4.8), "brass")
        blade = "hour_blade" if sx > 0 else "minute_blade"
        m.bone(blade, fore, pivot=(0.0, 7.9, -0.4), rotation=BLADE_TILT)
        if sx > 0:
            _blade(m, blade, 16.0, 3.4, 4.6, "hour")
        else:
            _blade(m, blade, 25.0, 2.4, 4.2, "minute")

    # ------------------------------------------------------------------ drei kleine Zahnräder, die um ihn kreisen
    for i in range(3):
        o = f"orbit_{i}"
        m.bone(o, pivot=(0.0, -2.0, 0.0))
        _disc(m, o, 0.0, 0.0, 0.0, 3.2 - i * 0.3, 0.5, "gear_orbit")
    return m


# =============================================================================================
# Bemalen
# =============================================================================================

def _plane(ctx: Ctx) -> tuple[float, float, float]:
    """Lage des Texels auf einer flachen Scheibe: (u, v, Halbmesser) – u, v von der Mitte aus (v nach oben positiv)."""
    ox, oy, oz = ctx.cube.origin
    sx, sy, sz = ctx.cube.size
    lx, ly, lz = ctx.local
    cy = oy + sy / 2.0
    if sx < sz:                                        # die Scheibe steht quer zu x (Ebene z–y)
        cz = oz + sz / 2.0
        u = (lz - cz) * (1.0 if ctx.lface == "east" else -1.0)
        return u, cy - ly, min(sy, sz) / 2.0
    cx = ox + sx / 2.0
    u = (lx - cx) * (1.0 if ctx.lface == "front" else -1.0)
    return u, cy - ly, min(sx, sy) / 2.0


def _bronze_value(ctx: Ctx, base: float) -> float:
    """Polierte Bronze: weiche Wölbung (oben heller, unten im Schatten), helle Kanten, kaum Unruhe, Licht je Seite."""
    x, y, z = ctx.world
    v = base + 0.1 + _LIT[ctx.face] + (noise3(x * 0.3, y * 0.3, z * 0.3, 3101) - 0.5) * 0.05
    r = rel_y(ctx)
    v += (0.4 - r) * 0.12
    if ctx.face in ("front", "west", "east", "back"):
        edge = min(ctx.s, ctx.w - 1 - ctx.s)
        if edge == 0:
            v -= 0.08
        elif edge == 1:
            v += 0.07
    return v


def _rivets(ctx: Ctx) -> int:
    """Nieten entlang der oberen und unteren Kante großer Platten: 1 = Niet (hell), 2 = sein Schatten, 0 = keiner."""
    if ctx.face not in ("front", "back", "west", "east") or ctx.w < 10 or ctx.h < 8:
        return 0
    if ctx.t in (2, ctx.h - 3) and ctx.s % 7 == 3 and 3 <= ctx.s <= ctx.w - 4:
        return 1
    if ctx.t in (3, ctx.h - 2) and ctx.s % 7 == 3 and 3 <= ctx.s <= ctx.w - 4:
        return 2
    return 0


def _bronze(ctx: Ctx):
    v = _bronze_value(ctx, 0.5)
    r = _rivets(ctx)
    if r == 1:
        return pick(ctx.pal["brass"], 0.88)
    if r == 2:
        v -= 0.14
    # Grünspan in den Fugen ganz unten (selten)
    if rel_y(ctx) > 0.9 and ctx.face in ("front", "west", "east", "back") and hash01(ctx.u, ctx.v, 3103) > 0.8:
        return pick(ctx.pal["patina"], 0.5 + hash01(ctx.u, 3104) * 0.3)
    return pick(ctx.pal["bronze"], v)


def _brass(ctx: Ctx):
    """Messingsäume: hell, mit einer gravierten Mittellinie."""
    v = 0.55 + _LIT[ctx.face]
    if ctx.face in ("front", "back", "west", "east") and ctx.h >= 3 and ctx.t == ctx.h // 2:
        v -= 0.18
    if (ctx.s + ctx.t) % 6 == 0:
        v += 0.12
    return pick(ctx.pal["brass"], v)


def _iron(ctx: Ctx):
    x, y, z = ctx.world
    v = 0.42 + _LIT[ctx.face] + (noise3(x * 0.8, y * 0.8, z * 0.8, 3105) - 0.5) * 0.16
    if ctx.s == 0 or ctx.t == 0:
        v += 0.1
    return pick(ctx.pal["iron"], v)


def _mail(ctx: Ctx):
    """Kettengeflecht: kleine Ringe in versetzten Reihen, dunkel dazwischen."""
    row = ctx.t // 2
    col = (ctx.s + (row % 2)) // 2
    ring = (ctx.s + (row % 2)) % 2 == 0 and ctx.t % 2 == 0
    v = 0.46 + _LIT[ctx.face] + (0.16 if ring else -0.06) + (hash01(col, row, ctx.cube.origin, 3106) - 0.5) * 0.05
    return pick(ctx.pal["iron"], v)


def _lame(ctx: Ctx):
    """Bauchreifen: Bronze, die untere Kante dunkel (die nächste Schiene liegt darunter)."""
    v = _bronze_value(ctx, 0.48)
    if ctx.face in ("front", "west", "east") and ctx.t >= ctx.h - 1:
        v -= 0.25
    return pick(ctx.pal["bronze"], v)


def _belt(ctx: Ctx):
    if ctx.face in ("front", "back", "west", "east") and (ctx.t == 0 or ctx.t == ctx.h - 1):
        return pick(ctx.pal["brass"], 0.6)
    return pick(ctx.pal["leather"], 0.5 + _LIT[ctx.face] + (0.1 if (ctx.s // 3) % 2 else 0.0))


def _buckle(ctx: Ctx):
    """Die Gürtelschnalle: ein Messingrahmen mit einem Chronit in der Mitte."""
    if ctx.face == "front":
        cx, cy = (ctx.w - 1) / 2.0, (ctx.h - 1) / 2.0
        if abs(ctx.s - cx) <= 1.0 and abs(ctx.t - cy) <= 0.6:
            col = pick(ctx.pal["amber"], 0.75)
            ctx.glow = col
            return col
    return pick(ctx.pal["brass"], 0.65 + _LIT[ctx.face])


def _tasset(ctx: Ctx):
    """Beintaschen: Bronzeplatten mit Messingsaum unten und einer eingravierten Stundenmarke."""
    v = _bronze_value(ctx, 0.46)
    if rel_y(ctx) > 0.86:
        return pick(ctx.pal["brass"], 0.6 + _LIT[ctx.face])
    if ctx.face in ("front", "back", "west", "east") and ctx.w >= 6:
        cx = (ctx.w - 1) / 2.0
        if abs(ctx.s - cx) < 0.6 and 0.3 < rel_y(ctx) < 0.66:
            return pick(ctx.pal["brass"], 0.62)                             # die Stundenmarke (ein Messingstrich)
    return pick(ctx.pal["bronze"], v)


def _cuirass(ctx: Ctx):
    """Der Harnisch: große Bronzeplatten; um das Fenster ein Messingrahmen (die Kanten zum Fenster hin hell)."""
    v = _bronze_value(ctx, 0.52)
    r = _rivets(ctx)
    if r == 1:
        return pick(ctx.pal["brass"], 0.92)
    if r == 2:
        v -= 0.16
    lx, ly = ctx.local[0], ctx.local[1]
    if ctx.face == "front":
        # der Rahmen um das Fenster (x -3..3, y -8..-2): ein Ring aus Messing
        dx = max(abs(lx) - 3.0, 0.0)
        dy = max(-8.0 - ly, ly + 2.0, 0.0)
        d = math.hypot(dx, dy)
        if d < 0.9:
            return pick(ctx.pal["brass"], 0.78 - d * 0.3)
        if d < 1.3:
            v -= 0.2
        # eine Gravur: Strahlen vom Fenster aus (wie eine Sonne)
        a = math.atan2(ly + 5.0, lx)
        if 1.3 < d < 3.2 and abs(math.sin(a * 6.0)) < 0.12:
            v -= 0.14
    return pick(ctx.pal["bronze"], v)


def _frame(ctx: Ctx):
    return pick(ctx.pal["brass"], 0.7 + _LIT[ctx.face])


def _backplate(ctx: Ctx):
    v = _bronze_value(ctx, 0.42)
    if ctx.face == "back" and ctx.s % 8 == 4:
        v -= 0.12                                                           # senkrechte Kehlungen
    return pick(ctx.pal["bronze"], v)


def _gorget(ctx: Ctx):
    v = _bronze_value(ctx, 0.5)
    if ctx.face in ("front", "back", "west", "east") and ctx.t % 2 == 1:
        v -= 0.14                                                           # geschuppte Reifen
    return pick(ctx.pal["bronze"], v)


def _works(ctx: Ctx):
    """Der Kern hinter dem Fenster: tiefes Dunkel, ein großes Rad im Schatten und wenige Bernsteinfunken."""
    if ctx.face != "front":
        return pick(ctx.pal["iron"], 0.3)
    lx, ly = ctx.local[0], ctx.local[1]
    r = math.hypot(lx + 0.6, ly + 4.6)
    a = math.atan2(ly + 4.6, lx + 0.6)
    v = 0.3 - r * 0.04
    if 3.1 < r < 3.7 and math.cos(a * 9.0) > -0.2:
        v += 0.4                                                            # ein großes Rad dahinter, im Schatten
    if hash01(ctx.u, ctx.v, 3108) > 0.985:
        col = pick(ctx.pal["amber"], 0.6)
        ctx.glow = mix((0, 0, 0, 255), col, 0.7)
        return col
    return pick(ctx.pal["works"], v)


def _gear_shape(ctx: Ctx, teeth: int, holes: int, glow_hub: bool):
    """Ein Zahnrad auf einer flachen Scheibe: Zähne außen, ein Kranz, Speichen mit Fenstern dazwischen, die Nabe. None =
    durchsichtig (außerhalb oder in einem Fenster)."""
    u, v, R = _plane(ctx)
    r = math.hypot(u, v)
    a = math.atan2(v, u)
    if r > R:
        return None
    root = R * 0.8
    if r > root:
        # Zähne: trapezförmig (außen schmaler)
        k = (a / (2.0 * math.pi) * teeth) % 1.0
        width = 0.5 - (r - root) / (R - root) * 0.18
        if abs(k - 0.5) > width / 2.0 + 0.04:
            return None
    if holes and R * 0.3 < r < R * 0.6:
        k = (a / (2.0 * math.pi) * holes) % 1.0
        if 0.18 < k < 0.82:
            return None                                                     # die Fenster zwischen den Speichen
    if r < R * 0.2 and glow_hub:
        col = pick(ctx.pal["amber"], 0.82 - r / (R * 0.2) * 0.25)
        ctx.glow = col
        return col
    shade = 0.62 if r > root else 0.5 if r > R * 0.6 else 0.56
    if abs(r - root) < R * 0.07 or abs(r - R * 0.6) < R * 0.06:
        shade -= 0.12                                                       # Ringrillen
    if ctx.lface in ("back", "west"):
        shade -= 0.08
    shade += 0.08 if v > 0 else -0.04                                     # oben im Licht
    return pick(ctx.pal["brass"] if r > R * 0.6 else ctx.pal["bronze"], shade)


def _gear(ctx: Ctx):
    return _gear_shape(ctx, 10, 4, True)


def _gear_small(ctx: Ctx):
    return _gear_shape(ctx, 7, 0, True)


def _gear_orbit(ctx: Ctx):
    col = _gear_shape(ctx, 10, 4, True)
    if col is not None and ctx.glow is None:
        ctx.glow = mix((0, 0, 0, 255), col, 0.35)                           # sie glimmen schwach
    return col


def _bob(ctx: Ctx):
    """Die Pendelscheibe: ein Bronzering um eine Scheibe aus Chronit, in der ein Funke steht."""
    u, v, R = _plane(ctx)
    r = math.hypot(u, v)
    if r > R:
        return None
    if r > R * 0.72:
        return pick(ctx.pal["brass"], 0.7 - (r - R * 0.72) / (R * 0.28) * 0.3 + (0.1 if v > 0 else 0.0))
    col = pick(ctx.pal["amber"], 0.55 + (1.0 - r / (R * 0.72)) * 0.35 + (0.08 if u - v < -0.5 else 0.0))
    ctx.glow = col
    return col


def _dial(ctx: Ctx):
    """Das große Zifferblatt: vorn Email mit Minutenring und zwölf Stundenmarken (die Hauptstunden aus Chronit, sie
    leuchten), außen eine Lünette aus Bronze; hinten Bronze mit Ringen und Speichen."""
    u, v, R = _plane(ctx)
    r = math.hypot(u, v)
    if r > R:
        return None
    k = r / R
    a = math.atan2(u, v)                                                     # 0 = zwölf Uhr, im Uhrzeigersinn
    if ctx.lface == "back":
        # hinten: Bronze mit drei erhabenen Ringen und sechs Speichen, oben im Licht
        if k > 0.92:
            return pick(ctx.pal["brass"], 0.6 + (0.1 if v > 0 else -0.05))
        ring = min(abs(k - 0.78), abs(k - 0.52), abs(k - 0.26)) < 0.03
        spoke = abs(math.sin(a * 3.0)) < 0.05 and 0.26 < k < 0.78
        base = 0.5 + v / R * 0.08
        if ring or spoke:
            return pick(ctx.pal["brass"], 0.55 + v / R * 0.1)
        return pick(ctx.pal["bronze"], base)
    if k > 0.93:
        knurl = (int(a * 60) % 2) * 0.1
        return pick(ctx.pal["bronze"], 0.62 + knurl + (0.12 if v > 0 else -0.06))       # die Lünette, oben im Licht
    if k > 0.89:
        return pick(ctx.pal["bronze"], 0.3)
    hour = a / (2.0 * math.pi) * 12.0
    nearest = round(hour)
    off = abs(hour - nearest) * 2.0 * math.pi / 12.0 * r                     # Abstand zur Stundenlinie (Einheiten)
    main = nearest % 3 == 0
    if 0.66 < k < 0.89:
        # die Stundenmarken: Balken im Ring (die vier Hauptstunden breiter und aus Chronit)
        if off < (0.75 if main else 0.42) and k > 0.69:
            if main:
                col = pick(ctx.pal["amber"], 0.72 + (k - 0.69) * 0.8)
                ctx.glow = col
                return col
            return pick(ctx.pal["ink"], 0.4)
        # der Minutenring: eine feine Linie (einzelne Striche wären bei dieser Größe nur Krümel)
        if abs(k - 0.83) < 0.012:
            return pick(ctx.pal["ink"], 0.5)
        return pick(ctx.pal["enamel"], 0.72 + (noise3(u * 0.7, v * 0.7, 1.0, 3111) - 0.5) * 0.1 + (0.05 if v > 0 else 0.0))
    if 0.64 < k <= 0.66 or 0.24 < k < 0.26:
        return pick(ctx.pal["ink"], 0.5)
    if k > 0.26:
        # die Mitte: Guilloché – feine, wellige Ringe
        wave = math.sin(k * 60.0 + math.sin(a * 12.0) * 1.2)
        return pick(ctx.pal["enamel"], 0.6 + wave * 0.06 + (0.06 if v > 0 else 0.0))
    return pick(ctx.pal["bronze"], 0.55 + (0.15 if v > 0 else 0.0) - k)


def _hand(ctx: Ctx, kind: str):
    """Ein gebläuter Zeiger: dünner Schaft, nahe der Spitze eine Raute (Minute) oder ein Herz (Stunde); hinten ein Gegengewicht."""
    lx, ly = ctx.local[0], ctx.local[1]
    length = -ctx.cube.origin[1]                                            # von der Mitte bis zur Spitze
    t = -ly / max(1.0, length)                                              # 0 Mitte … 1 Spitze
    if kind == "minute":
        if t < -0.02:
            half = 0.55
        elif t < 0.7:
            half = 0.24
        elif t < 0.9:
            half = 0.24 + (1.0 - abs(t - 0.8) / 0.1) * 0.7                     # die Raute
        else:
            half = max(0.05, (1.0 - t) * 2.2)
    else:
        if t < -0.02:
            half = 0.7
        elif t < 0.55:
            half = 0.36
        elif t < 0.85:
            c = abs(t - 0.7) / 0.15
            half = 0.36 + (1.0 - c * c) * 0.62                               # das Herz
        else:
            half = max(0.06, (1.0 - t) * 2.6)
    if abs(lx) > half:
        return None
    hollow = (kind == "minute" and 0.75 < t < 0.85 and abs(lx) < half - 0.45) or (kind == "hour" and 0.62 < t < 0.78 and abs(lx) < half - 0.5)
    if hollow:
        return None                                                         # durchbrochen
    v = 0.45 + (0.25 if lx < 0 else 0.0) + (0.1 if abs(lx) < 0.12 else 0.0)
    return pick(ctx.pal["blued"], v)


def _hand_minute(ctx: Ctx):
    return _hand(ctx, "minute")


def _hand_hour(ctx: Ctx):
    return _hand(ctx, "hour")


def _hub(ctx: Ctx):
    col = pick(ctx.pal["amber"], 0.85 if ctx.face == "front" else 0.6)
    ctx.glow = col
    return col


def _helm(ctx: Ctx):
    """Der Topfhelm: Bronze; vorn ein T-förmiger Sehschlitz, aus dem Bernsteinlicht glüht, darunter Atemlöcher."""
    v = _bronze_value(ctx, 0.52)
    if ctx.face == "front":
        lx, ly = ctx.local[0], ctx.local[1]
        if -4.6 < ly < -3.7 and abs(lx) < 2.6:                               # der Querschlitz
            if abs(lx) > 0.5 and abs(abs(lx) - 1.4) < 0.75:
                col = pick(ctx.pal["amber"], 0.92 - abs(abs(lx) - 1.4) * 0.4)   # die Augen
                ctx.glow = col
                return col
            col = pick(ctx.pal["amber"], 0.35)
            ctx.glow = mix((0, 0, 0, 255), col, 0.5)
            return col
        if -3.7 <= ly < -1.6 and abs(lx) < 0.45:                             # der senkrechte Schlitz
            col = pick(ctx.pal["amber"], 0.3)
            ctx.glow = mix((0, 0, 0, 255), col, 0.4)
            return col
        if -1.8 < ly < -0.9 and abs(lx) > 1.0 and abs(lx) < 2.6 and ctx.s % 2 == 0:
            return pick(ctx.pal["works"], 0.2)                              # Atemlöcher
        if abs(ly + 4.15) < 0.75 and abs(lx) < 2.9:
            v -= 0.12                                                       # Schatten um den Schlitz
    if ctx.face == "top":
        v += 0.06
    return pick(ctx.pal["bronze"], v)


def _crest(ctx: Ctx):
    """Der Kamm: ein Zeiger, der von vorn nach hinten über den Helm läuft (vorn die Spitze)."""
    lz, ly = ctx.local[2], ctx.local[1]
    t = (lz + 3.6) / 7.6                                                     # 0 vorn … 1 hinten
    height = 4.2 * (0.35 + 0.65 * math.sin(min(1.0, t * 1.15) * math.pi * 0.5)) if t < 0.85 else 4.2 * (1.0 - (t - 0.85) * 2.5)
    if -ly > height:
        return None
    if ctx.face == "top":
        return pick(ctx.pal["brass"], 0.8)
    v = 0.5 + _LIT[ctx.face] + (-ly / 4.2) * 0.25
    if abs(-ly - height * 0.5) < 0.25:
        return pick(ctx.pal["amber"], 0.7)
    return pick(ctx.pal["brass"], v)


def _pauldron(ctx: Ctx):
    v = _bronze_value(ctx, 0.5)
    if ctx.face in ("front", "back", "west", "east") and ctx.t >= ctx.h - 1:
        v -= 0.22                                                           # die Kante über der nächsten Platte
    if ctx.face == "top":
        v += 0.05
    r = _rivets(ctx)
    if r == 1:
        return pick(ctx.pal["brass"], 0.9)
    return pick(ctx.pal["bronze"], v)


def _couter(ctx: Ctx):
    v = _bronze_value(ctx, 0.54)
    if ctx.face in ("front", "west", "east") and ctx.w >= 4:
        cx = (ctx.w - 1) / 2.0
        if abs(ctx.s - cx) < 1.5 and abs(ctx.t - (ctx.h - 1) / 2.0) < 1.5:
            return pick(ctx.pal["brass"], 0.82)                             # die Rosette
    return pick(ctx.pal["bronze"], v)


def _gauntlet(ctx: Ctx):
    """Panzerhandschuh: geschuppte Fingerplatten aus Bronze, an den Knöcheln Messing, unten (die Handfläche) dunkles Leder."""
    if ctx.face == "bottom":
        return pick(ctx.pal["leather"], 0.4)
    if ctx.face == "top":
        return pick(ctx.pal["bronze"], 0.6)
    v = 0.48 + _LIT[ctx.face] + (0.14 if ctx.t % 3 == 0 else 0.0) + (0.5 - rel_y(ctx)) * 0.1
    if ctx.t <= 1:
        return pick(ctx.pal["brass"], 0.7)                                  # die Knöchelleiste
    return pick(ctx.pal["bronze"], v)


def _sabaton(ctx: Ctx):
    v = _bronze_value(ctx, 0.46)
    if ctx.face in ("west", "east", "front") and ctx.s % 3 == 0:
        v -= 0.1                                                            # die Reifen des Schuhs
    if ctx.face == "bottom":
        return pick(ctx.pal["iron"], 0.2)
    return pick(ctx.pal["bronze"], v)


def _chain(ctx: Ctx):
    return pick(ctx.pal["brass"], 0.45 + (0.25 if ctx.t % 3 == 0 else 0.0) + _LIT[ctx.face])


def _weight(ctx: Ctx):
    """Ein Uhrgewicht wie ein Tannenzapfen: Schuppen in versetzten Reihen."""
    row = ctx.t // 2
    k = (ctx.s + row) % 3
    v = 0.4 + _LIT[ctx.face] + (0.18 if k == 0 else 0.0) - (0.12 if ctx.t % 2 == 1 else 0.0)
    v += (0.5 - rel_y(ctx)) * 0.12
    return pick(ctx.pal["bronze"], v)


def _grip(ctx: Ctx):
    return pick(ctx.pal["leather"], 0.45 + (0.25 if (ctx.t + ctx.s) % 4 == 0 else 0.0) + _LIT[ctx.face])


def _blade_paint(ctx: Ctx, kind: str):
    """Gebläuter Stahl, in der Mitte eine Messingseele mit einer leuchtenden Chronitader; die Schneiden hell."""
    if ctx.face in ("west", "east"):
        return pick(ctx.pal["blued"], 0.8)
    cx = (ctx.w - 1) / 2.0
    d = abs(ctx.s - cx) / max(1.0, cx)
    if d < 0.2:
        if kind == "minute" or ctx.t % 2 == 0:
            col = pick(ctx.pal["amber"], 0.7)
            ctx.glow = mix((0, 0, 0, 255), col, 0.85)
            return col
    if d < 0.4:
        return pick(ctx.pal["brass"], 0.62)
    if d > 0.85:
        return pick(ctx.pal["blued"], 0.88)                                 # die Schneide
    return pick(ctx.pal["blued"], 0.42 + (0.18 if ctx.s < cx else 0.0) + (hash01(ctx.u, ctx.v, 3112) - 0.5) * 0.06)


def _minute_blade(ctx: Ctx):
    return _blade_paint(ctx, "minute")


def _hour_blade(ctx: Ctx):
    return _blade_paint(ctx, "hour")


def _tip(ctx: Ctx, kind: str):
    """Die Spitze: eine Raute (Minute) oder eine Lilie (Stunde) aus gebläutem Stahl mit Messingrand."""
    lx = ctx.local[0]
    oy, h = ctx.cube.origin[1], ctx.cube.size[1]
    t = (ctx.local[1] - oy) / h                                              # 0 Spitze … 1 Fuß
    w = ctx.cube.size[0] / 2.0
    if kind == "minute":
        half = w * t / 0.55 if t < 0.55 else w - (w - 0.9) * (t - 0.55) / 0.45
    elif t < 0.2:
        half = 0.6 * t / 0.2                                                # die Spitze der Lilie
    elif t < 0.65:
        half = 0.6 + (w - 0.6) * math.sin((t - 0.2) / 0.45 * math.pi)      # die beiden Blätter
    else:
        half = 1.35
    if abs(lx) > half:
        return None
    if abs(lx) > half - 0.25:
        return pick(ctx.pal["brass"], 0.75)
    if abs(lx) < 0.25 and 0.25 < t < 0.75:
        col = pick(ctx.pal["amber"], 0.8)
        ctx.glow = col
        return col
    return pick(ctx.pal["blued"], 0.5 + (0.2 if lx < 0 else 0.0))


def _minute_tip(ctx: Ctx):
    return _tip(ctx, "minute")


def _hour_tip(ctx: Ctx):
    return _tip(ctx, "hour")


def _ornament(ctx: Ctx):
    """Ein durchbrochener Ring an der Klinge, innen ein Chronit."""
    u, v, R = _plane(ctx)
    r = math.hypot(u, v)
    if r > R:
        return None
    if r > R * 0.66:
        return pick(ctx.pal["brass"], 0.72 + (0.1 if v > 0 else 0.0))
    if r > R * 0.4:
        return None
    col = pick(ctx.pal["amber"], 0.8)
    ctx.glow = col
    return col


def materials(variant: str) -> dict:
    return {
        "bronze": _bronze, "brass": _brass, "iron": _iron, "mail": _mail, "lame": _lame, "belt": _belt, "buckle": _buckle,
        "tasset": _tasset, "cuirass": _cuirass, "frame": _frame, "backplate": _backplate, "gorget": _gorget, "works": _works,
        "gear": _gear, "gear_small": _gear_small, "gear_orbit": _gear_orbit, "bob": _bob, "dial": _dial, "hand_minute": _hand_minute,
        "hand_hour": _hand_hour, "hub": _hub, "helm": _helm, "crest": _crest, "pauldron": _pauldron, "couter": _couter,
        "gauntlet": _gauntlet, "sabaton": _sabaton, "chain": _chain, "weight": _weight, "grip": _grip,
        "minute_blade": _minute_blade, "hour_blade": _hour_blade, "minute_tip": _minute_tip, "hour_tip": _hour_tip,
        "ornament": _ornament,
    }
