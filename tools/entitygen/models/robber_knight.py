"""
Kuno von Rabenfels, der Raubritter (seit 1.8): Herr der Raubritterburg im dunklen Wald, Schrecken der Handelsstraßen. Er
hält Hof in seinem Burghof – hoch zu Ross, die Lanze aufrecht, unter dem Rabenbanner seines Geschlechts.

Ein Modell aus Ross und Reiter (im Spiel 1,15-fach vergrößert, mit dem Raben auf dem Helm gut dreieinhalb Blöcke hoch):

* **Das Ross** – ein schwerer Rappe (ein Friese: tiefschwarz mit bläulichem Glanz, lange Mähne, Fesselbehang), unter einer
  schwarzen **Schabracke** mit goldener Borte, die in Zacken endet; auf jeder Flanke ein goldenes Rund mit dem schwarzen
  Raben. Auf dem Kopf ein **Rossstirn** aus geschwärztem Stahl mit goldenem Rand und einem Busch schwarzer Federn, auf dem
  Rücken ein hoher Kriegssattel.
* **Der Reiter** – in geschwärztem Plattenharnisch mit goldenen Kanten über einem Kettenhemd, darüber ein goldener
  **Waffenrock** mit dem schwarzen Raben, ein schwarzer Mantel, am Gürtel ein Horn. Auf dem Kopf ein **Topfhelm** mit
  goldenem Kreuz und einem schwarzen **Raben** mit gebreiteten Schwingen als Helmzier.
* **Die Waffen** – zu Ross die **Lanze** (Schaft in Schwarz und Gold gewunden, eine Brechscheibe vor der Hand, ein
  gespaltener Wimpel in Gold und Schwarz), am linken Arm der **Rabenschild** (Gold, darin der schwarze Rabe); zu Fuß der
  **Morgenstern** (ein Stiel mit Eisenbändern, ein Kopf voller Stacheln).

Ross und Reiter sind zwei eigene Knochen unter der Wurzel: Zu Fuß blendet das Java-Modell das Ross aus und stellt den Reiter
auf den Boden. Knochen-Namen werden in RobberKnightModel (Java) für die Animationen verwendet.
"""
from __future__ import annotations

import math

from ..geometry import Model
from ..paint import Ctx, hash01, hexc, mix, noise3, pick, rel_y


def stops(*colors: str) -> list:
    """Handverlesene Tonleiter."""
    return [hexc(c) for c in colors]


PALETTES = {
    "robber_knight": {
        "coat": stops("#060609", "#0f1016", "#191b23", "#252832", "#343846", "#4b5164", "#6c7590"),
        "hair": stops("#040405", "#0b0b0f", "#15161c", "#21232c", "#33364a"),
        "hoof": stops("#121110", "#211f1c", "#36322d", "#524c45"),
        "skin": stops("#121012", "#1e191c", "#2e2629", "#43383c", "#5a4c51"),
        "eye": stops("#000000", "#090706", "#241b14", "#e8e0d0"),
        "cloth": stops("#09090c", "#121218", "#1b1c24", "#262833", "#363946", "#4a4e5f"),
        "gold": stops("#3e2a07", "#674a10", "#94701e", "#c49a30", "#e4c254", "#f6e08e", "#fff6cc"),
        "steel": stops("#0a0b0e", "#14161b", "#1f2229", "#2d313a", "#41464f", "#5d636e", "#878e9b", "#b4bbc6"),
        "mail": stops("#1d2025", "#2f333a", "#454a53", "#60666f", "#7f868f", "#a6adb6"),
        "leather": stops("#1a110a", "#2b1b10", "#3f2817", "#573821", "#73492c"),
        "wood": stops("#22150b", "#342013", "#4a2f1b", "#634025", "#7e5431"),
        "red": stops("#240505", "#420a0a", "#651212", "#8a1c1c", "#b02a2a"),
        "ivory": stops("#544a38", "#7e715a", "#a89a7e", "#cbbfa4", "#e8e0cc"),
    },
}

_LIT = {"top": 0.08, "front": 0.03, "west": 0.0, "east": -0.03, "back": -0.06, "bottom": -0.14}

#: der Rabe im Wappen (sitzend, nach links gewandt – heraldisch rechts): X Leib, o Auge (golden)
RAVEN = (
    "....XXX.......",
    "...XXXXX......",
    "XXXXoXXX......",
    "..XXXXXXX.....",
    "...XXXXXXXX...",
    "...XXXXXXXXXX.",
    "....XXXXXXXXXX",
    "....XXXXXXXXX.",
    ".....XXXXXXX..",
    "......XXXXX...",
    "......X...X...",
    ".....XX..XX...",
)
RAVEN_W = len(RAVEN[0])
RAVEN_H = len(RAVEN)

#: die Lanze (in der Hand, Spitze nach vorn): Griff hinter der Hand, Länge bis zur Spitze
LANCE_BUTT = 8.0
LANCE_TIP = 35.0


def build() -> Model:
    m = Model("robber_knight", 256, 256)
    m.tex_res = 2

    # ================================================================== das Ross (Rumpf zwischen y −1 und 11, Kopf bei −z)
    m.bone("horse", pivot=(0, 5, 0))
    m.cube("horse", (-6, -6, -13), (12, 12, 26), "coat")
    m.cube("horse", (-5, -4.6, -14.7), (10, 9.6, 1.8), "coat")                           # die Brust
    # die Schabracke: über den Rücken, zu beiden Seiten (bis über die Knie), vorn über die Brust, hinten über die Kruppe
    m.cube("horse", (-6.45, -6.7, -12.0), (12.9, 0.9, 24.6), "trapper_top")
    for name, sx in (("trapper_l", 1), ("trapper_r", -1)):
        m.bone(name, "horse", pivot=(6.3 * sx, -6, 0))
        m.cube(name, (0 if sx > 0 else -0.7, -0.5, -12.9), (0.7, 15.6, 25.8), "trapper_side")
    m.bone("trapper_front", "horse", pivot=(0, -4.6, -14.75))
    m.cube("trapper_front", (-6.5, 0, -0.7), (13, 12.2, 0.7), "trapper_front")
    m.bone("trapper_back", "horse", pivot=(0, -5.6, 13.05))
    m.cube("trapper_back", (-6.5, 0, 0), (13, 13.2, 0.7), "trapper_back")
    # der Kriegssattel: Sitz, hohe Rückenlehne, Vorderzwiesel
    m.cube("horse", (-4.2, -8.0, -4.2), (8.4, 1.8, 9.0), "saddle")
    m.cube("horse", (-4.0, -11.6, 4.0), (8.0, 4.2, 1.6), "saddle_back")
    m.cube("horse", (-3.0, -10.0, -5.4), (6.0, 2.4, 1.4), "saddle")

    # Hals, Kopf (mit Rossstirn und Federbusch), Ohren, Mähne
    m.bone("neck", "horse", pivot=(0, -3, -12), rotation=(30, 0, 0))
    m.cube("neck", (-3.0, -16.5, -3.9), (6, 17.5, 7.8), "neck")
    m.cube("neck", (-1.4, -18.4, 2.8), (2.8, 19.2, 3.3), "mane")
    m.bone("mane_drape", "neck", pivot=(1.4, -16.2, 3.8))
    m.cube("mane_drape", (0, 0, -1.4), (1.2, 16.0, 4.8), "mane")
    m.bone("head", "neck", pivot=(0, -15.2, -0.8))
    m.cube("head", (-3.5, -4.3, -3.0), (7, 6.4, 7.9), "head")
    m.cube("head", (-2.6, -2.7, -9.6), (5.2, 5.8, 6.8), "muzzle")
    m.cube("head", (-3.65, -5.0, -3.15), (7.3, 0.8, 8.2), "chanfron")                  # die Rossstirn: über dem Schädel
    m.cube("head", (-2.75, -3.45, -9.8), (5.5, 0.8, 7.0), "chanfron")                  # und über der Nase
    m.cube("head", (-1.4, -3.5, -9.85), (2.8, 2.9, 0.45), "chanfron")                  # die Nasenplatte vorn
    m.cube("head", (-0.75, -9.0, 0.4), (1.5, 4.1, 1.5), "plume")                       # der Federbusch
    m.cube("head", (-1.8, -5.6, -1.7), (3.6, 0.7, 3.4), "mane")                        # Stirnschopf
    for name, sx in (("ear_l", 1), ("ear_r", -1)):
        m.bone(name, "head", pivot=(1.95 * sx, -4.6, 2.8), rotation=(-10, 0, 12 * sx))
        m.cube(name, (-0.9, -3.4, -0.6), (1.8, 3.6, 1.3), "ear")

    # die Beine: Oberschenkel bis zum Knie, Röhre mit Fesselbehang, Huf
    for leg, x, z in (("fl", 3.7, -9.8), ("fr", -3.7, -9.8), ("hl", 3.7, 9.6), ("hr", -3.7, 9.6)):
        m.bone(f"leg_{leg}", "horse", pivot=(x, 5, z))
        m.cube(f"leg_{leg}", (-2.0, -1, -2.2), (4.0, 8, 4.4), "leg")
        m.bone(f"shin_{leg}", f"leg_{leg}", pivot=(0, 7, 0))
        m.cube(f"shin_{leg}", (-1.5, 0, -1.65), (3.0, 5.4, 3.3), "leg_low")
        m.cube(f"shin_{leg}", (-2.05, 3.6, -2.25), (4.1, 2.6, 4.5), "feather")
        m.cube(f"shin_{leg}", (-1.9, 5.4, -2.1), (3.8, 1.6, 4.2), "hoof")

    # der Schweif
    m.bone("tail", "horse", pivot=(0, -4.4, 13.1), rotation=(28, 0, 0))
    m.cube("tail", (-1.8, -0.4, -0.7), (3.6, 8.0, 3.4), "tail")
    m.bone("tail_low", "tail", pivot=(0, 7.2, 0.2))
    m.cube("tail_low", (-2.4, 0, -1.0), (4.8, 11.0, 4.0), "tail")

    # ================================================================== der Reiter (Hüfte; im Sattel bei y −3)
    m.bone("knight", pivot=(0, -3, 0.6))
    m.bone("torso", "knight", pivot=(0, 0, 0))
    m.cube("torso", (-4.4, -12, -2.6), (8.8, 12, 5.2), "surcoat")
    m.cube("torso", (-4.75, -1.9, -2.95), (9.5, 1.6, 5.9), "belt")
    m.cube("torso", (-3.1, -13.3, -2.7), (6.2, 1.5, 5.4), "mail")                       # der Kragen aus Kettengeflecht
    m.bone("tabard", "torso", pivot=(0, -0.4, -2.75))
    m.cube("tabard", (-3.7, 0, -0.5), (7.4, 7.4, 0.5), "tabard")
    m.bone("tabard_back", "torso", pivot=(0, -0.4, 2.75))
    m.cube("tabard_back", (-3.7, 0, 0), (7.4, 7.4, 0.5), "tabard")
    m.bone("cape", "torso", pivot=(0, -11.8, 2.95))
    m.cube("cape", (-4.9, 0, 0), (9.8, 16.5, 0.6), "cape")
    # das Horn am Gürtel (links)
    m.bone("horn", "torso", pivot=(4.9, -1.4, -0.4))
    m.cube("horn", (0, 0, -2.6), (1.3, 1.3, 3.4), "horn")
    m.cube("horn", (-0.25, -0.9, -4.1), (1.8, 2.0, 1.6), "horn_bell")

    # der Topfhelm mit dem Raben
    m.bone("helm", "torso", pivot=(0, -12.6, 0))
    m.cube("helm", (-3.8, -8.6, -3.8), (7.6, 8.6, 7.6), "helm")
    m.bone("crest", "helm", pivot=(0, -8.6, 0.4))
    m.cube("crest", (-1.0, -2.8, -1.6), (2.0, 2.8, 4.4), "raven")                       # der Leib
    m.cube("crest", (-0.8, -4.0, -2.8), (1.6, 1.6, 1.8), "raven")                       # der Hals
    m.cube("crest", (-0.95, -5.6, -3.9), (1.9, 2.1, 2.2), "raven")                      # der Kopf
    m.cube("crest", (-0.45, -5.0, -5.8), (0.9, 0.8, 2.0), "beak")
    m.cube("crest", (-0.85, -2.2, 2.6), (1.7, 0.8, 3.2), "raven")                       # der Schwanz
    # die Schwingen: steil erhoben und weit zurückgeschwungen (so sieht man den Raben auch von der Seite)
    for name, sx in (("wing_l", 1), ("wing_r", -1)):
        m.bone(name, "crest", pivot=(0.9 * sx, -2.4, 0.2), rotation=(0, -34 * sx, -52 * sx))
        m.cube(name, (0 if sx > 0 else -7.0, -0.45, -1.6), (7.0, 0.9, 4.2), "raven_wing")

    # die Arme (rechts bei −x): Schulterstück, Kettenärmel, Armschiene, Panzerhandschuh
    for side, sx in (("r", -1), ("l", 1)):
        arm = f"arm_{side}"
        m.bone(arm, "torso", pivot=(5.6 * sx, -10.8, 0))
        m.cube(arm, (-2.0, -1.4, -2.0), (4, 6.8, 4), "mail")
        m.cube(arm, (-2.75, -2.5, -2.65), (5.5, 3.7, 5.3), "pauldron")
        fore = f"forearm_{side}"
        m.bone(fore, arm, pivot=(0, 5.4, 0))
        m.cube(fore, (-1.9, 0, -1.9), (3.8, 5.4, 3.8), "vambrace")
        m.cube(fore, (-2.15, -0.9, -2.25), (4.3, 1.7, 4.5), "couter")                   # der Ellbogenschutz
        hand = f"hand_{side}"
        m.bone(hand, fore, pivot=(0, 5.4, 0))
        m.cube(hand, (-1.65, 0, -1.65), (3.3, 2.9, 3.3), "gauntlet")

    # die Beine: Diechling, Beinröhre mit Kniebuckel, Eisenschuh mit Sporn
    for side, sx in (("r", -1), ("l", 1)):
        thigh = f"thigh_{side}"
        m.bone(thigh, "knight", pivot=(2.3 * sx, 0, 0))
        m.cube(thigh, (-1.95, 0, -2.0), (3.9, 6.4, 4.0), "cuisse")
        shin = f"shin_{side}"
        m.bone(shin, thigh, pivot=(0, 6.4, 0))
        m.cube(shin, (-1.75, 0, -1.85), (3.5, 5.3, 3.7), "greave")
        m.cube(shin, (-1.95, -0.9, -2.3), (3.9, 1.9, 1.6), "couter")                    # der Kniebuckel
        m.cube(shin, (-1.8, 4.6, -3.0), (3.6, 1.3, 4.9), "sabaton")
        m.cube(shin, (-0.3, 4.75, 1.85), (0.6, 0.6, 1.5), "spur")

    # ================================================================== Waffen
    # die Lanze in der Rechten: zeigt nach vorn, wenn der Arm hängt (die Haltungen drehen sie)
    m.bone("lance", "hand_r", pivot=(0, 1.6, 0), rotation=(90, 0, 0))
    m.cube("lance", (-0.9, -1.2, -0.9), (1.8, LANCE_BUTT + 1.2, 1.8), "lance_grip")
    m.cube("lance", (-2.3, -3.8, -2.3), (4.6, 2.6, 4.6), "vamplate")                    # die Brechscheibe
    m.cube("lance", (-1.0, -17.0, -1.0), (2.0, 13.2, 2.0), "lance")
    m.cube("lance", (-0.75, -31.0, -0.75), (1.5, 14.0, 1.5), "lance")
    m.cube("lance", (-0.6, -LANCE_TIP, -0.6), (1.2, LANCE_TIP - 31.0, 1.2), "lance_tip")
    m.bone("pennant", "lance", pivot=(0, -28.5, 0))
    m.cube("pennant", (-0.1, 0, -6.6), (0.2, 7.0, 6.0), "pennant")
    # der Rabenschild am linken Unterarm (außen)
    m.bone("shield", "forearm_l", pivot=(2.0, 2.6, 0))
    m.cube("shield", (0, -5.6, -4.9), (1.0, 13.2, 9.8), "shield")
    # der Morgenstern (zu Fuß, in der Rechten)
    m.bone("mace", "hand_r", pivot=(0, 1.6, 0), rotation=(90, 0, 0))
    m.cube("mace", (-0.75, -9.0, -0.75), (1.5, 13.0, 1.5), "mace_haft")
    m.cube("mace", (-2.2, -14.4, -2.2), (4.4, 5.4, 4.4), "mace_head")
    for (ox, oy, oz, sx, sy, sz) in ((-0.55, -12.6, -3.5, 1.1, 1.4, 1.3), (-0.55, -12.6, 2.2, 1.1, 1.4, 1.3),
                                     (-3.5, -12.6, -0.55, 1.3, 1.4, 1.1), (2.2, -12.6, -0.55, 1.3, 1.4, 1.1),
                                     (-0.55, -15.8, -0.55, 1.1, 1.4, 1.1)):
        m.cube("mace", (ox, oy, oz), (sx, sy, sz), "spike")
    # das Horn in der Linken (nur beim Hornruf)
    m.bone("horn_hand", "hand_l", pivot=(0, 2.0, 0))
    m.cube("horn_hand", (-0.65, -0.6, -2.6), (1.3, 1.3, 3.4), "horn")
    m.cube("horn_hand", (-0.9, -1.5, -4.1), (1.8, 2.0, 1.6), "horn_bell")
    return m


# =============================================================================================
# Hilfen
# =============================================================================================

def _uv(ctx: Ctx) -> tuple[float, float]:
    return (ctx.s + 0.5) / max(ctx.w, 1), (ctx.t + 0.5) / max(ctx.h, 1)


def _raven_at(u: float, v: float, cu: float, cv: float, size_u: float, size_v: float) -> str | None:
    """Der Rabe im Wappen: 'X' (Leib), 'o' (Auge) oder None, wenn (u, v) außerhalb liegt."""
    x = (u - cu) / size_u + 0.5
    y = (v - cv) / size_v + 0.5
    if not (0.0 <= x < 1.0 and 0.0 <= y < 1.0):
        return None
    c = RAVEN[int(y * RAVEN_H)][int(x * RAVEN_W)]
    return c if c != "." else None


def _gold(ctx: Ctx, v: float):
    x, y, z = ctx.world
    n = noise3(x * 0.9, y * 0.9, z * 0.9, 3801)
    return pick(ctx.pal["gold"], v + (n - 0.5) * 0.14 + _LIT.get(ctx.face, 0.0))


def _steel(ctx: Ctx, v: float):
    """Geschwärzter Stahl: matt, mit hellen Kratzern."""
    x, y, z = ctx.world
    n = noise3(x * 0.6, y * 0.6, z * 0.6, 3811)
    col = pick(ctx.pal["steel"], v + (n - 0.5) * 0.1 + _LIT.get(ctx.face, 0.0))
    if hash01(ctx.u, ctx.v, 3812) > 0.985:
        col = pick(ctx.pal["steel"], v + 0.3)
    return col


def _edge(ctx: Ctx, width: int = 1) -> bool:
    """Liegt das Texel am Rand seiner Fläche?"""
    return ctx.s < width or ctx.t < width or ctx.s >= ctx.w - width or ctx.t >= ctx.h - width


def _plate(ctx: Ctx, base: float = 0.42):
    """Plattenharnisch: geschwärzter Stahl mit goldener Kante, oben im Licht."""
    if ctx.face in ("top", "bottom"):
        return _steel(ctx, base - (0.1 if ctx.face == "bottom" else -0.04))
    if _edge(ctx):
        return _gold(ctx, 0.55)
    ry = rel_y(ctx)
    return _steel(ctx, base + 0.12 - ry * 0.18)


def _mail(ctx: Ctx):
    """Kettengeflecht: Reihen kleiner Ringe."""
    ring = (ctx.s + (ctx.t % 2)) % 2 == 0
    v = 0.45 + (0.18 if ring else -0.05) + _LIT.get(ctx.face, 0.0) + (hash01(ctx.u, ctx.v, 3821) - 0.5) * 0.08
    if ctx.face == "bottom":
        v -= 0.2
    return pick(ctx.pal["mail"], v)


def _cloth(ctx: Ctx, pal: str, base: float):
    x, y, z = ctx.world
    fold = 0.5 + 0.5 * math.sin(x * 1.1 + z * 0.7)
    v = base + 0.1 * fold + _LIT.get(ctx.face, 0.0) + (noise3(x * 0.4, y * 0.3, z * 0.4, 3831) - 0.5) * 0.08
    return pick(ctx.pal[pal], v)


# =============================================================================================
# Das Ross
# =============================================================================================

def _coat(ctx: Ctx):
    """Rappfell: tiefschwarz mit bläulichem Glanz auf dem Rücken und an den Muskeln."""
    x, y, z = ctx.world
    ry = rel_y(ctx)
    v = 0.36 + _LIT.get(ctx.face, 0.0) * 1.4 - ry * 0.18
    v += (noise3(x * 0.35, y * 0.35, z * 0.35, 3841) - 0.5) * 0.14
    if ctx.face in ("west", "east") and ry < 0.3:
        v += 0.1                                                                          # Glanz
    if ctx.face == "bottom":
        v = 0.12
    v += (hash01(ctx.u, ctx.v, 3842) - 0.5) * 0.04
    return pick(ctx.pal["coat"], v)


def _neck(ctx: Ctx):
    return _coat(ctx)


def _head(ctx: Ctx):
    """Der Kopf: Rappfell, große dunkle Augen mit Glanzpunkt an den Seiten."""
    lx, ly, lz = ctx.local
    if ctx.face in ("west", "east") and 0.6 <= lz < 2.4 and -2.6 <= ly < -1.2:
        if ly < -1.9 and lz >= 1.5:
            return pick(ctx.pal["eye"], 0.9)                                              # Glanzpunkt
        return pick(ctx.pal["eye"], 0.2)
    return _coat(ctx)


def _muzzle(ctx: Ctx):
    lx, ly, lz = ctx.local
    if ctx.face == "front":
        if -1.2 <= ly < 0.2 and (ctx.s <= 1 or ctx.s >= ctx.w - 2):
            return pick(ctx.pal["skin"], 0.02)                                            # Nüstern
        if ly >= 1.4:
            return pick(ctx.pal["skin"], 0.25)
        return pick(ctx.pal["skin"], 0.5 + hash01(ctx.u, ctx.v, 3851) * 0.15)
    if ly > 0.8 and ctx.face in ("west", "east"):
        return pick(ctx.pal["skin"], 0.4)
    return _coat(ctx)


def _ear(ctx: Ctx):
    return pick(ctx.pal["coat"], 0.3 + _LIT.get(ctx.face, 0.0))


def _hair(ctx: Ctx, base: float = 0.42):
    """Langes Haar (Mähne, Schweif, Fesselbehang): Strähnen mit Glanz."""
    x, y, z = ctx.world
    strand = hash01(int(x * 2.0 + z * 1.3), 3861)
    v = base + (strand - 0.5) * 0.3 + _LIT.get(ctx.face, 0.0)
    if (ctx.s + int(strand * 3)) % 4 == 0:
        v += 0.12
    return pick(ctx.pal["hair"], v)


def _mane(ctx: Ctx):
    return _hair(ctx)


def _tail(ctx: Ctx):
    ry = rel_y(ctx)
    if ctx.cube.size[1] > 9 and ry > 0.9 and hash01(ctx.s, 3862) > 0.5:
        return None                                                                       # ausfransende Spitze
    return _hair(ctx, 0.4)


def _feather(ctx: Ctx):
    ry = rel_y(ctx)
    if ry > 0.8 and hash01(ctx.s, ctx.face, 3863) > 0.55:
        return None
    return _hair(ctx, 0.38)


def _leg(ctx: Ctx):
    return _coat(ctx)


def _leg_low(ctx: Ctx):
    return pick(ctx.pal["coat"], 0.28 + _LIT.get(ctx.face, 0.0) + (hash01(ctx.u, ctx.v, 3871) - 0.5) * 0.06)


def _hoof(ctx: Ctx):
    if ctx.face == "bottom":
        return pick(ctx.pal["hoof"], 0.1)
    ry = rel_y(ctx)
    return pick(ctx.pal["hoof"], 0.55 - ry * 0.3 + (hash01(ctx.s, 3872) - 0.5) * 0.15)


def _trapper(ctx: Ctx, emblem: bool, dagged: bool):
    """Die Schabracke: schwarzes Tuch, eine goldene Borte; auf den Flanken ein goldenes Rund mit dem schwarzen Raben; der
    untere Saum endet in Zacken (mit goldener Kante)."""
    u, v = _uv(ctx)
    if ctx.face in ("top", "bottom") or ctx.face in ("west", "east") and ctx.cube.size[0] < 1.0 and not emblem:
        pass
    outer = emblem and ctx.face in ("west", "east") or not emblem and ctx.face in ("front", "back")
    if dagged and ctx.face not in ("top", "bottom"):
        # Zacken: je 4 Texel ein Dreieck, 3 Texel tief
        k = ctx.s % 4
        depth = abs(k - 1.5) / 1.5 * 3.0
        if ctx.t >= ctx.h - 1 - depth:
            return None
        if ctx.t >= ctx.h - 3 - depth:
            return _gold(ctx, 0.5)
    if not outer:
        return _cloth(ctx, "cloth", 0.12)
    if ctx.t < 2:
        return _gold(ctx, 0.6)                                                            # die Borte oben
    if emblem:
        # das goldene Rund mit dem Raben (etwas vor der Mitte, auf der Höhe des Bauchs)
        cu = 0.4 if (ctx.face == "east") else 0.6
        du, dv = (u - cu) * ctx.w, (v - 0.42) * ctx.h
        r = math.hypot(du, dv)
        rad = min(ctx.w, ctx.h) * 0.3
        if r < rad:
            mark = _raven_at(du / (rad * 2) + 0.5, dv / (rad * 2) + 0.5, 0.5, 0.52, 0.86, 0.82)
            if mark == "X":
                return pick(ctx.pal["cloth"], 0.08)
            if mark == "o":
                return _gold(ctx, 0.8)
            return _gold(ctx, 0.62 - r / rad * 0.15)
        if r < rad + 1.5:
            return _gold(ctx, 0.36)                                                       # sein dunkler Rand
    return _cloth(ctx, "cloth", 0.22)


def _trapper_top(ctx: Ctx):
    if ctx.face == "top":
        u, v = _uv(ctx)
        if u < 0.06 or u > 0.94:
            return _gold(ctx, 0.55)
        return _cloth(ctx, "cloth", 0.26)
    return _cloth(ctx, "cloth", 0.2)


def _trapper_side(ctx: Ctx):
    return _trapper(ctx, True, True)


def _trapper_front(ctx: Ctx):
    return _trapper(ctx, False, True)


def _trapper_back(ctx: Ctx):
    return _trapper(ctx, False, True)


def _saddle(ctx: Ctx):
    """Der Kriegssattel: dunkles Leder, Nieten aus Messing."""
    if _edge(ctx) and (ctx.s + ctx.t) % 3 == 0 and ctx.face != "bottom":
        return _gold(ctx, 0.6)
    return pick(ctx.pal["leather"], 0.45 + _LIT.get(ctx.face, 0.0) + (hash01(ctx.u, ctx.v, 3881) - 0.5) * 0.1)


def _saddle_back(ctx: Ctx):
    if ctx.face == "back":
        u, v = _uv(ctx)
        if abs(u - 0.5) < 0.12 and 0.2 < v < 0.8 or abs(v - 0.45) < 0.12 and 0.3 < u < 0.7:
            return _gold(ctx, 0.62)                                                       # ein goldenes Kreuz
    return _saddle(ctx)


def _chanfron(ctx: Ctx):
    """Die Rossstirn: geschwärzter Stahl, goldener Rand, ein Grat in der Mitte."""
    u, v = _uv(ctx)
    if _edge(ctx):
        return _gold(ctx, 0.5)
    if ctx.face == "top" and abs(u - 0.5) < 0.1:
        return _steel(ctx, 0.62)
    return _steel(ctx, 0.42)


def _plume(ctx: Ctx):
    ry = rel_y(ctx)
    if ry < 0.15 and ctx.face != "bottom":
        return _gold(ctx, 0.5)                                                            # die Hülse
    return _hair(ctx, 0.3 + (1.0 - ry) * 0.2)


# =============================================================================================
# Der Reiter
# =============================================================================================

def _surcoat(ctx: Ctx):
    """Der Waffenrock: Gold, vorn und hinten der schwarze Rabe; an den Seiten schwarz (der Rock ist ärmellos)."""
    u, v = _uv(ctx)
    if ctx.face in ("west", "east"):
        return _mail(ctx)
    if ctx.face == "top":
        return _mail(ctx)
    if ctx.face == "bottom":
        return _cloth(ctx, "cloth", 0.1)
    if ctx.t < 2 or ctx.s < 1 or ctx.s >= ctx.w - 1:
        return _cloth(ctx, "cloth", 0.18)                                                 # schwarze Säume
    mark = _raven_at(u, v, 0.5, 0.48, 0.74, 0.62)
    if mark == "X":
        return pick(ctx.pal["cloth"], 0.06 + (0.06 if ctx.face == "front" and ctx.t % 3 == 0 else 0.0))
    if mark == "o":
        return pick(ctx.pal["red"], 0.8)
    return _gold(ctx, 0.58 - v * 0.12)


def _belt(ctx: Ctx):
    u, v = _uv(ctx)
    if ctx.face == "front" and abs(u - 0.5) < 0.1:
        return _gold(ctx, 0.75)                                                           # die Schnalle
    return pick(ctx.pal["leather"], 0.4 + _LIT.get(ctx.face, 0.0))


def _tabard(ctx: Ctx):
    """Die Schöße des Waffenrocks: Gold mit schwarzem Saum, unten in Zacken."""
    k = ctx.s % 4
    depth = abs(k - 1.5) / 1.5 * 2.0
    if ctx.face not in ("top", "bottom") and ctx.t >= ctx.h - 1 - depth:
        return None
    if ctx.face in ("front", "back"):
        if ctx.t >= ctx.h - 3 - depth or ctx.s < 1 or ctx.s >= ctx.w - 1:
            return _cloth(ctx, "cloth", 0.16)
        return _gold(ctx, 0.52)
    return _cloth(ctx, "cloth", 0.14)


def _cape(ctx: Ctx):
    """Der Mantel: schwarz, innen rot gefüttert; unten zerschlissen."""
    ry = rel_y(ctx)
    col_i = ctx.s // 2
    end = 0.92 + hash01(col_i, ctx.face, 3891) * 0.08
    if ctx.face not in ("top", "bottom") and ry > end:
        return None
    if ctx.face == "front":
        return pick(ctx.pal["red"], 0.35 + (hash01(ctx.u, ctx.v, 3892) - 0.5) * 0.1)      # das Futter
    return _cloth(ctx, "cloth", 0.2 - ry * 0.06)


def _helm(ctx: Ctx):
    """Der Topfhelm: geschwärzter Stahl; vorn ein goldenes Kreuz, der Sehschlitz, Atemlöcher; goldener Rand oben."""
    u, v = _uv(ctx)
    if ctx.face == "front":
        if 0.36 < v < 0.46 and 0.12 < u < 0.88 and abs(u - 0.5) > 0.06:
            return pick(ctx.pal["steel"], 0.0)                                            # der Sehschlitz
        if abs(u - 0.5) < 0.06 and v > 0.14:
            return _gold(ctx, 0.62)                                                       # das Kreuz, senkrecht
        if 0.36 < v < 0.46 and abs(u - 0.5) < 0.2:
            return _gold(ctx, 0.62)
        if v > 0.6 and (ctx.s + ctx.t) % 3 == 0 and abs(u - 0.5) > 0.12 and abs(u - 0.5) < 0.38:
            return pick(ctx.pal["steel"], 0.05)                                           # Atemlöcher
    if ctx.t < 1 and ctx.face != "top" or ctx.t >= ctx.h - 1 and ctx.face != "bottom":
        return _gold(ctx, 0.5)
    if ctx.face == "top":
        return _steel(ctx, 0.5)
    return _steel(ctx, 0.4 + (0.1 if ctx.face == "front" else 0.0) - rel_y(ctx) * 0.08)


def _raven(ctx: Ctx):
    """Der Rabe der Helmzier: schwarz mit bläulichem Glanz, ein goldenes Auge."""
    lx, ly, lz = ctx.local
    if ctx.cube.size[2] < 2.3 and ctx.cube.size[1] > 2.0 and ctx.face in ("west", "east") and abs(ly + 4.9) < 0.45 and lz < -2.9:
        return _gold(ctx, 0.85)
    return pick(ctx.pal["coat"], 0.24 + _LIT.get(ctx.face, 0.0) * 1.5 + (hash01(ctx.u, ctx.v, 3901) - 0.5) * 0.1)


def _beak(ctx: Ctx):
    return pick(ctx.pal["steel"], 0.15 + _LIT.get(ctx.face, 0.0))


def _raven_wing(ctx: Ctx):
    """Die Schwingen: Federn in Reihen, an den Spitzen gespreizt."""
    u, v = _uv(ctx)
    if ctx.face in ("top", "bottom"):
        tip = u if ctx.local[0] > 0 else 1.0 - u
        if tip > 0.78 and (ctx.t % 2 == 0) and hash01(ctx.t, 3902) > 0.3:
            return None
        v2 = 0.2 + (0.1 if (ctx.s // 2) % 2 == 0 else 0.0) + _LIT.get(ctx.face, 0.0) * 1.5
        return pick(ctx.pal["coat"], v2)
    return pick(ctx.pal["coat"], 0.18)


def _pauldron(ctx: Ctx):
    return _plate(ctx, 0.46)


def _vambrace(ctx: Ctx):
    return _plate(ctx, 0.4)


def _couter(ctx: Ctx):
    if ctx.face in ("top", "bottom"):
        return _steel(ctx, 0.36)
    return _gold(ctx, 0.5) if _edge(ctx) else _steel(ctx, 0.52)


def _gauntlet(ctx: Ctx):
    """Panzerhandschuh: Platten über den Fingern."""
    if ctx.face == "bottom":
        return _steel(ctx, 0.2)
    if ctx.t % 2 == 1 and ctx.face != "top":
        return _steel(ctx, 0.3)
    return _steel(ctx, 0.48)


def _cuisse(ctx: Ctx):
    """Diechling: Platten über dem Kettengeflecht, vorn eine goldene Leiste."""
    u, v = _uv(ctx)
    if ctx.face in ("west", "east", "back"):
        return _mail(ctx)
    if ctx.face == "front" and abs(u - 0.5) < 0.1:
        return _gold(ctx, 0.5)
    return _plate(ctx, 0.42)


def _greave(ctx: Ctx):
    return _plate(ctx, 0.4)


def _sabaton(ctx: Ctx):
    if ctx.face == "bottom":
        return _steel(ctx, 0.1)
    if ctx.face == "top" and ctx.t % 2 == 0:
        return _steel(ctx, 0.3)
    return _steel(ctx, 0.44)


def _spur(ctx: Ctx):
    return _gold(ctx, 0.6)


def _horn(ctx: Ctx):
    """Das Horn: Elfenbein mit goldenen Ringen."""
    lx, ly, lz = ctx.local
    if int((lz + 10) * 1.5) % 3 == 0:
        return _gold(ctx, 0.62)
    return pick(ctx.pal["ivory"], 0.55 + _LIT.get(ctx.face, 0.0) + (hash01(ctx.u, ctx.v, 3911) - 0.5) * 0.1)


def _horn_bell(ctx: Ctx):
    if ctx.face == "front":
        u, v = _uv(ctx)
        if 0.25 < u < 0.75 and 0.25 < v < 0.75:
            return pick(ctx.pal["ivory"], 0.05)                                           # die Öffnung
    return _gold(ctx, 0.55)


# =============================================================================================
# Waffen
# =============================================================================================

def _lance(ctx: Ctx):
    """Der Lanzenschaft: in Schwarz und Gold gewunden."""
    x, y, z = ctx.local
    if ctx.face in ("top", "bottom"):
        return pick(ctx.pal["wood"], 0.4)
    band = int(math.floor((y + ctx.s * 0.6) / 2.2)) % 2
    if band == 0:
        return _gold(ctx, 0.5)
    return pick(ctx.pal["cloth"], 0.2 + _LIT.get(ctx.face, 0.0))


def _lance_grip(ctx: Ctx):
    if ctx.t % 2 == 0 and ctx.face not in ("top", "bottom"):
        return pick(ctx.pal["leather"], 0.55)
    return pick(ctx.pal["leather"], 0.35 + _LIT.get(ctx.face, 0.0))


def _vamplate(ctx: Ctx):
    """Die Brechscheibe vor der Hand: geschwärzter Stahl mit goldenem Rand."""
    if ctx.face in ("top", "bottom"):
        u, v = _uv(ctx)
        return _gold(ctx, 0.5) if (u < 0.12 or u > 0.88 or v < 0.12 or v > 0.88) else _steel(ctx, 0.5)
    return _gold(ctx, 0.45) if _edge(ctx) else _steel(ctx, 0.45)


def _lance_tip(ctx: Ctx):
    return _steel(ctx, 0.75 - rel_y(ctx) * 0.2)


def _pennant(ctx: Ctx):
    """Der Wimpel: gespalten (Schwalbenschwanz), oben Gold, unten Schwarz, mit dem Raben-Auge als goldenem Punkt."""
    u, v = _uv(ctx)
    # u läuft am Schaft entlang (zur Hand hin), v vom Schaft fort; der Schwalbenschwanz schneidet das Ende ein
    along = ctx.t / max(ctx.h - 1, 1)
    away = ctx.s / max(ctx.w - 1, 1)
    if ctx.face in ("west", "east"):
        if along > 0.62 and abs(away - 0.5) < (along - 0.62) * 1.1:
            return None
        if along > 0.8 + away * 0.0 and away > 0.0 and along > 0.94:
            return None
        return _gold(ctx, 0.55) if away < 0.5 else pick(ctx.pal["cloth"], 0.2)
    return _gold(ctx, 0.45)


def _shield(ctx: Ctx):
    """Der Rabenschild: ein Dreieckschild – Gold, darin der schwarze Rabe; ein Rand aus geschwärztem Stahl."""
    u, v = _uv(ctx)
    if ctx.face in ("west", "east"):
        # die Spitze unten: die Seiten laufen ab der Hälfte zusammen
        half = 0.5 if v < 0.5 else 0.5 - (v - 0.5) * 0.92
        if abs(u - 0.5) > half:
            return None
        if abs(u - 0.5) > half - 0.07 or v < 0.05:
            return _steel(ctx, 0.5)
        if ctx.face == "west":
            return pick(ctx.pal["leather"], 0.35 + (0.1 if ctx.t % 4 == 0 else 0.0))     # innen: Leder und Riemen
        mark = _raven_at(u, v, 0.5, 0.42, 0.74, 0.5)
        if mark == "X":
            return pick(ctx.pal["cloth"], 0.07)
        if mark == "o":
            return pick(ctx.pal["red"], 0.8)
        return _gold(ctx, 0.6 - v * 0.15)
    v2 = _uv(ctx)[1]
    if ctx.face in ("front", "back") and v2 > 0.5:
        return None                                                                       # die Seitenkanten gibt es unten nicht
    return _steel(ctx, 0.4)


def _mace_haft(ctx: Ctx):
    x, y, z = ctx.local
    if int((y + 20) * 0.9) % 4 == 0 and ctx.face not in ("top", "bottom"):
        return _steel(ctx, 0.45)                                                          # Eisenbänder
    return pick(ctx.pal["wood"], 0.4 + _LIT.get(ctx.face, 0.0) + (hash01(ctx.t, 3921) - 0.5) * 0.1)


def _mace_head(ctx: Ctx):
    if _edge(ctx):
        return _steel(ctx, 0.3)
    return _steel(ctx, 0.46 + (hash01(ctx.u, ctx.v, 3922) - 0.5) * 0.1)


def _spike(ctx: Ctx):
    return _steel(ctx, 0.7)


def materials(variant: str) -> dict:
    return {
        "coat": _coat, "neck": _neck, "head": _head, "muzzle": _muzzle, "ear": _ear, "mane": _mane, "tail": _tail,
        "feather": _feather, "leg": _leg, "leg_low": _leg_low, "hoof": _hoof, "trapper_top": _trapper_top,
        "trapper_side": _trapper_side, "trapper_front": _trapper_front, "trapper_back": _trapper_back, "saddle": _saddle,
        "saddle_back": _saddle_back, "chanfron": _chanfron, "plume": _plume,
        "surcoat": _surcoat, "belt": _belt, "mail": _mail, "tabard": _tabard, "cape": _cape, "helm": _helm, "raven": _raven,
        "beak": _beak, "raven_wing": _raven_wing, "pauldron": _pauldron, "vambrace": _vambrace, "couter": _couter,
        "gauntlet": _gauntlet, "cuisse": _cuisse, "greave": _greave, "sabaton": _sabaton, "spur": _spur, "horn": _horn,
        "horn_bell": _horn_bell, "lance": _lance, "lance_grip": _lance_grip, "vamplate": _vamplate, "lance_tip": _lance_tip,
        "pennant": _pennant, "shield": _shield, "mace_haft": _mace_haft, "mace_head": _mace_head, "spike": _spike,
    }


def hidden(variant: str) -> list[str]:
    return []
