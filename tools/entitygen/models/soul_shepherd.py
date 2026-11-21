"""
Der Seelenhirte (0.98): der Hüter des Seelenpferchs im Seelensumpf der Schattenwelt. Einst führte er die Seelen der Toten
ins Licht; seit das Siegel die Schattenwelt verschloss, hütet er sie in seinem Pferch und wartet. Im Spiel 1,5-fach
vergrößert (gut dreieinhalb Blöcke hoch).

  * ein hagerer, vorgebeugter Riese in einer langen, aschgrauen Kutte, die nach unten weiter wird und am Saum in Fetzen
    hängt; am Saum ein Band aus schwach glimmenden Seelenrunen
  * über den Schultern ein zotteliges, schmutzig-helles Schaffell, vorn mit herabhängenden Zotteln, hinten ein langer,
    zerrissener Umhang bis fast zum Boden
  * eine tiefe Kapuze mit Spitze, darin kein Gesicht, sondern ein Widderschädel: die lange Schnauze ragt aus der Kapuze,
    in den Augenhöhlen brennt türkises Seelenlicht; zwei schwere, gerippte Widderhörner rollen sich seitlich um die Kapuze
  * lange, knochige Arme mit weiten Ärmeln, dürre Finger mit dunklen Krallen; um die Hüfte ein Strick mit Talismanen
  * rechts der Hirtenstab aus Sumpfeiche, oben ein eiserner Haken, an dem eine Laterne voll Seelenfeuer hängt; links eine
    Handglocke aus angelaufener Bronze
  * drei Irrlichter kreisen um ihn (Knochen wisp_0..2, in Java bewegt)

Boden-Regel: y = 24 ist der Boden, oben ist -y, vorn ist -z. Die Knochen-Namen verwendet SoulShepherdModel (Java):
body > chest > head (jaw, snout, hood_peak, horn_l_*/horn_r_*), mantle (mantle_rag_*), cloak > cloak_tip, left_arm >
left_arm_fore > hand_bell, right_arm > right_arm_fore > crook > hook_0..5 > lantern; skirt (hem_*), left_leg, right_leg,
charm_0..2, wisp_0..2.
"""
from __future__ import annotations

import math

from ..geometry import Model
from ..paint import Ctx, hash01, hexc, mix, noise3, pick, rel_y

PALETTES = {
    "shepherd": {
        "robe": [hexc(c) for c in ("#121117", "#1b1a22", "#26242f", "#33303e", "#423e4f", "#534e62", "#676176", "#7d778c",
                                   "#958fa3")],
        "hood": [hexc(c) for c in ("#07070a", "#0e0d13", "#16141c", "#1f1c27", "#2a2633", "#373241", "#464050")],
        "fleece": [hexc(c) for c in ("#3a352e", "#4f4940", "#665f54", "#7f776a", "#988f80", "#b0a796", "#c6bdab", "#d9d1bf")],
        "skull": [hexc(c) for c in ("#2a251c", "#4a4232", "#6e6450", "#948a72", "#b7ad94", "#d3cab0", "#e9e1c9", "#f6f0dc")],
        "horn": [hexc(c) for c in ("#17110c", "#261c13", "#3a2c1e", "#50402c", "#6a563c", "#86704f", "#a48c66", "#c2ab82")],
        "soul": [hexc(c) for c in ("#0d4a46", "#13706a", "#1e9a90", "#3fc4b6", "#7fe6d8", "#c2fbf2", "#f0fffc")],
        "flesh": [hexc(c) for c in ("#1d1f22", "#2c2f33", "#3e4247", "#53585d", "#6b7075", "#878b8e", "#a4a7a7")],
        "wood": [hexc(c) for c in ("#140d08", "#21160e", "#2f2116", "#3e2d1e", "#4f3a27", "#634a33", "#7a5d41")],
        "iron": [hexc(c) for c in ("#0f1012", "#1a1c20", "#272a30", "#363a41", "#474c54", "#5d636c", "#7a818b", "#9aa1ab")],
        "rust": [hexc(c) for c in ("#2a160c", "#432414", "#5e351e", "#7a4a2a")],
        "rope": [hexc(c) for c in ("#251e14", "#382e1f", "#4c412d", "#62553c", "#7a6c4e", "#938463")],
        "bronze": [hexc(c) for c in ("#18201b", "#253129", "#33443a", "#46594b", "#5d735f", "#7b8f73", "#9caa84", "#c0c49c")],
        "glass": [hexc(c) for c in ("#0a2f2d", "#11504c", "#1b7a73", "#33a89d", "#6ad6c8", "#b4f6ec", "#f2fffd")],
    }
}

_LIT = {"top": 0.08, "front": 0.03, "west": 0.0, "east": -0.03, "back": -0.06, "bottom": -0.14}

# Haltung (Grad um x): der Leib neigt sich vor, die Brust noch mehr (ein Buckel); der Kopf schaut wieder nach vorn
BODY_LEAN = 8.0
CHEST_LEAN = 14.0
# Haltung des rechten Arms (hält den Stab vor sich) und des Stabs (steht senkrecht)
R_ARM = (-16.0, 0.0, 18.0)
R_FORE = (-44.0, 0.0, 0.0)
# Haken des Stabs: Glieder (Dicke, Länge, Drehung um x) – eine Krümme nach vorn, deren Spitze wieder nach unten zeigt
HOOK = [(1.5, 2.6, 16.0), (1.4, 2.6, 40.0), (1.3, 2.6, 46.0), (1.2, 2.5, 46.0), (1.1, 2.4, 42.0), (1.0, 2.0, 34.0)]


def _rot(deg):
    """Drehung eines Knochens wie in Minecraft (erst x, dann y, dann z) als 3×3-Matrix."""
    ax, ay, az = (math.radians(v) for v in deg)
    cx, sx, cy, sy, cz, sz = math.cos(ax), math.sin(ax), math.cos(ay), math.sin(ay), math.cos(az), math.sin(az)
    rx = ((1, 0, 0), (0, cx, -sx), (0, sx, cx))
    ry = ((cy, 0, sy), (0, 1, 0), (-sy, 0, cy))
    rz = ((cz, -sz, 0), (sz, cz, 0), (0, 0, 1))
    return _mul(rz, _mul(ry, rx))


def _mul(a, b):
    return tuple(tuple(sum(a[i][k] * b[k][j] for k in range(3)) for j in range(3)) for i in range(3))


def _upright(m: Model, parent: str):
    """Die Drehung (Grad x, y, z), mit der ein Kind von {parent} wieder gerade in der Welt steht: hebt alle Drehungen der
    Kette bis zur Wurzel auf (die Umkehrung, zerlegt wie Minecraft dreht)."""
    chain = []
    b = m.bones[parent]
    while b is not None and b.name != "root":
        chain.append(b.rotation)
        b = b.parent
    total = ((1, 0, 0), (0, 1, 0), (0, 0, 1))
    for rot in reversed(chain):
        total = _mul(total, _rot(rot))
    inv = tuple(tuple(total[j][i] for j in range(3)) for i in range(3))   # Drehmatrix: Umkehrung = Transponierte
    beta = -math.asin(max(-1.0, min(1.0, inv[2][0])))
    alpha = math.atan2(inv[2][1], inv[2][2])
    gamma = math.atan2(inv[1][0], inv[0][0])
    return math.degrees(alpha), math.degrees(beta), math.degrees(gamma)


def _ram_horn(m: Model, parent: str, side: str, sx: int, pivot) -> None:
    """Ein Widderhorn wie beim Nachtwidder, nur größer: Glieder entlang +z, erst nach oben hinten, dann in einer Spirale
    herab und um die Kapuze nach vorn; jedes Glied ein wenig nach außen gedreht, sodass die Spitze absteht."""
    sizes = [(3.0, 3.0), (2.8, 3.0), (2.6, 2.8), (2.3, 2.6), (2.0, 2.4), (1.6, 2.2), (1.2, 1.9)]
    turns = [(46.0, 28.0, 0.0), (-50.0, 6.0, 0.0), (-52.0, 6.0, 0.0), (-52.0, 6.0, 0.0), (-50.0, 8.0, 0.0), (-46.0, 8.0, 0.0),
             (-40.0, 10.0, 0.0)]
    total = sum(length for _, length in sizes)
    acc = 0.0
    prev, piv = parent, pivot
    for i, ((w, length), (rx, ry, rz)) in enumerate(zip(sizes, turns)):
        b = f"horn_{side}_{i}"
        m.bone(b, prev, pivot=piv, rotation=(rx, ry * sx, rz))
        m.cube(b, (-w / 2, -w / 2, 0.0), (w, w, length + 0.35), "horn", tag=f"{acc / total:.3f}:{(acc + length) / total:.3f}")
        acc += length
        prev, piv = b, (0.0, 0.0, length)


def build() -> Model:
    m = Model("soul_shepherd", 256, 256)
    m.tex_res = 2

    # ------------------------------------------------------------------ Beine (unter der Kutte; die Füße in Lumpen)
    for sx in (1, -1):
        leg = "left_leg" if sx > 0 else "right_leg"
        m.bone(leg, pivot=(2.0 * sx, 10.0, 0.4))
        m.cube(leg, (-1.1, 0.0, -1.1), (2.2, 12.4, 2.2), "flesh")
        m.cube(leg, (-1.4, 12.2, -4.4), (2.8, 1.8, 5.0), "rags")
        m.cube(leg, (-1.2, 12.9, -5.2), (2.4, 1.1, 0.9), "finger")                   # die Zehenkrallen

    # ------------------------------------------------------------------ die Kutte unten (am Boden weiter, in Fetzen)
    m.bone("skirt", pivot=(0.0, 10.0, 0.4))
    m.cube("skirt", (-4.2, -1.2, -2.9), (8.4, 6.4, 5.8), "robe")
    m.cube("skirt", (-4.8, 5.0, -3.4), (9.6, 5.2, 6.8), "robe_hem")
    hem = [(-3.6, -3.4, 0.0, 3.8), (-1.2, -3.4, 0.0, 3.2), (1.2, -3.4, 0.0, 3.6), (3.6, -3.4, 0.0, 3.0),           # vorn
           (-3.6, 3.4, 180.0, 3.4), (-1.2, 3.4, 180.0, 4.0), (1.2, 3.4, 180.0, 3.2), (3.6, 3.4, 180.0, 3.8),      # hinten
           (4.8, -1.4, 90.0, 3.4), (4.8, 1.4, 90.0, 3.8), (-4.8, -1.4, -90.0, 3.6), (-4.8, 1.4, -90.0, 3.2)]       # seitlich
    for i, (x, z, yaw, length) in enumerate(hem):
        b = f"hem_{i}"
        m.bone(b, "skirt", pivot=(x, 10.0, z), rotation=(-7.0 if i % 2 else -4.0, yaw, 0.0))
        m.cube(b, (-1.25, 0.0, -0.35), (2.5, length, 0.7), "hem")

    # ------------------------------------------------------------------ Leib: Bauch (body) und die gebeugte Brust (chest)
    m.bone("body", pivot=(0.0, 10.0, 0.4), rotation=(BODY_LEAN, 0.0, 0.0))
    m.cube("body", (-3.7, -8.6, -2.5), (7.4, 9.0, 5.0), "robe")
    m.cube("body", (-3.95, -1.9, -2.75), (7.9, 1.2, 5.5), "rope")                      # der Strick um die Hüfte
    m.cube("body", (2.6, -2.2, -3.0), (1.4, 1.4, 1.0), "rope")                          # der Knoten
    charms = [(-2.4, -2.9, 3.6, "bone_charm"), (0.8, -2.9, 4.6, "skull_charm"), (3.2, -2.9, 2.8, "vial")]
    for i, (x, z, length, kind) in enumerate(charms):
        b = f"charm_{i}"
        m.bone(b, "body", pivot=(x, -1.4, z), rotation=(-BODY_LEAN, 0.0, 5.0 if i % 2 else -5.0))
        m.cube(b, (-0.15, 0.0, -0.15), (0.3, length, 0.3), "rope")
        if kind == "bone_charm":
            m.cube(b, (-0.35, length, -0.35), (0.7, 2.4, 0.7), "bone")
            m.cube(b, (-0.7, length + 2.2, -0.5), (1.4, 0.7, 1.0), "bone")
        elif kind == "skull_charm":
            m.cube(b, (-0.8, length, -0.8), (1.6, 1.5, 1.6), "bone")
            m.cube(b, (-0.5, length + 1.5, -1.0), (1.0, 0.6, 1.2), "bone")
        else:
            m.cube(b, (-0.25, length, -0.25), (0.5, 0.5, 0.5), "iron")
            m.cube(b, (-0.6, length + 0.5, -0.6), (1.2, 1.8, 1.2), "vial")

    m.bone("chest", "body", pivot=(0.0, -8.2, 0.0), rotation=(CHEST_LEAN, 0.0, 0.0))
    m.cube("chest", (-4.3, -9.4, -2.8), (8.6, 9.8, 5.6), "robe")
    # das Schaffell über den Schultern: breit, oben flach, unten zottelig
    m.bone("mantle", "chest", pivot=(0.0, -9.4, 0.0))
    m.cube("mantle", (-6.2, -0.8, -3.7), (12.4, 4.6, 7.6), "fleece")
    m.cube("mantle", (-5.4, -1.6, -3.0), (10.8, 1.0, 6.2), "fleece")
    for i, (x, length) in enumerate(((-4.4, 3.6), (-1.6, 4.6), (1.4, 3.8), (4.2, 4.4))):
        m.bone(f"mantle_rag_{i}", "mantle", pivot=(x, 3.8, -3.5), rotation=(-6.0, 0.0, 0.0))
        m.cube(f"mantle_rag_{i}", (-1.2, 0.0, -0.4), (2.4, length, 0.8), "fleece_tuft")
    for i, (x, length) in enumerate(((-5.4, 3.0), (5.4, 3.4))):                          # an den Schultern
        m.bone(f"mantle_side_{i}", "mantle", pivot=(x, 3.8, 0.0), rotation=(0.0, 0.0, 8.0 if x > 0 else -8.0))
        m.cube(f"mantle_side_{i}", (-0.8, 0.0, -2.6), (1.6, length, 5.2), "fleece_tuft")
    # der Umhang hinten: vom Fell bis fast an den Boden (gegen die Beugung ausgerichtet, damit er herabhängt)
    m.bone("cloak", "chest", pivot=(0.0, -8.6, 3.4), rotation=(-(BODY_LEAN + CHEST_LEAN) + 7.0, 0.0, 0.0))
    m.cube("cloak", (-5.2, 0.0, 0.0), (10.4, 13.0, 0.7), "cloak")
    m.bone("cloak_tip", "cloak", pivot=(0.0, 13.0, 0.0), rotation=(4.0, 0.0, 0.0))
    m.cube("cloak_tip", (-5.0, 0.0, 0.0), (10.0, 10.0, 0.6), "cloak_tip")

    # ------------------------------------------------------------------ Kopf: Kapuze, Widderschädel, Hörner
    m.bone("head", "chest", pivot=(0.0, -9.6, -1.2), rotation=(-(BODY_LEAN + CHEST_LEAN) + 6.0, 0.0, 0.0))
    m.cube("head", (-2.6, -6.6, -3.4), (5.2, 6.0, 5.0), "skull")
    m.bone("snout", "head", pivot=(0.0, -4.0, -3.4), rotation=(36.0, 0.0, 0.0))        # die lange Schnauze, gesenkt
    m.cube("snout", (-1.5, 0.0, -4.2), (3.0, 2.7, 4.4), "snout")
    m.cube("snout", (-1.15, 0.2, -5.6), (2.3, 2.2, 1.5), "nose")
    m.bone("jaw", "head", pivot=(0.0, -1.2, -2.6), rotation=(46.0, 0.0, 0.0))          # der Unterkiefer, halb offen
    m.cube("jaw", (-1.3, 0.0, -4.8), (2.6, 0.9, 5.0), "jaw")
    # die Kapuze: oben, an den Seiten und hinten um den Schädel, vorn ein Saum; innen dunkel
    m.cube("head", (-4.5, -9.8, -4.8), (9.0, 1.4, 9.4), "hood")
    for sx in (1, -1):
        m.cube("head", (3.7 if sx > 0 else -4.5, -8.4, -4.8), (0.8, 8.8, 9.4), "hood")
        m.cube("head", (3.8 if sx > 0 else -4.7, -8.6, -5.6), (0.9, 9.2, 0.9), "hood_rim")
    m.cube("head", (-3.7, -8.4, 3.8), (7.4, 8.8, 0.8), "hood")
    m.cube("head", (-4.7, -10.0, -5.6), (9.4, 1.4, 0.9), "hood_rim")
    m.bone("hood_peak", "head", pivot=(0.0, -9.8, -4.4), rotation=(28.0, 0.0, 0.0))    # vorn eine Spitze über der Stirn
    m.cube("hood_peak", (-1.8, -1.2, -2.6), (3.6, 1.3, 3.0), "hood")
    m.bone("hood_tail", "head", pivot=(0.0, -9.0, 3.8), rotation=(-62.0, 0.0, 0.0))    # hinten der Zipfel
    m.cube("hood_tail", (-2.2, -2.2, 0.0), (4.4, 2.2, 3.2), "hood")
    m.cube("hood_tail", (-1.2, -1.6, 3.0), (2.4, 1.6, 2.2), "hood")
    for side, sx in (("l", 1), ("r", -1)):
        _ram_horn(m, "head", side, sx, (4.2 * sx, -7.6, -2.2))

    # ------------------------------------------------------------------ Arme: lang, knochig, weite Ärmel
    arm_pose = {1: ((-(BODY_LEAN + CHEST_LEAN) - 4.0, 0.0, -6.0), (-16.0, 0.0, 0.0)), -1: (R_ARM, R_FORE)}
    for sx in (1, -1):
        arm = "left_arm" if sx > 0 else "right_arm"
        fore = f"{arm}_fore"
        arm_rot, fore_rot = arm_pose[sx]
        m.bone(arm, "chest", pivot=(5.2 * sx, -8.2, 0.0), rotation=arm_rot)
        m.cube(arm, (-1.4, -1.0, -1.4), (2.8, 10.0, 2.8), "sleeve")
        m.bone(fore, arm, pivot=(0.0, 8.8, 0.0), rotation=fore_rot)
        m.cube(fore, (-0.95, 0.0, -0.95), (1.9, 8.6, 1.9), "flesh")
        m.cube(fore, (-2.0, -0.6, -2.0), (4.0, 5.0, 4.0), "sleeve_wide")              # der weite Ärmel
        m.bone(f"{arm}_rag", fore, pivot=(0.0, 4.4, 1.9), rotation=(10.0, 0.0, 0.0))
        m.cube(f"{arm}_rag", (-1.7, 0.0, -0.3), (3.4, 4.6, 0.5), "hem")                # ein Fetzen hängt vom Ärmel
        m.cube(fore, (-1.2, 8.4, -1.4), (2.4, 2.2, 2.6), "flesh")                      # die Hand
        for i, fz in enumerate((-1.25, -0.35, 0.55)):
            m.cube(fore, (-0.3, 10.4, fz), (0.6, 2.8 - 0.4 * (i % 2), 0.6), "finger")
        m.cube(fore, (-0.3 - 0.9 * sx, 9.4, -1.6), (0.6, 1.6, 0.6), "finger")          # der Daumen

    # ------------------------------------------------------------------ der Hirtenstab (rechte Hand), Haken und Laterne
    # der Stab steht senkrecht: er hebt die Drehungen von Leib, Brust, Arm und Unterarm auf
    m.bone("crook", "right_arm_fore", pivot=(0.0, 9.6, -0.2), rotation=_upright(m, "right_arm_fore"))
    m.cube("crook", (-0.7, -27.0, -0.7), (1.4, 44.0, 1.4), "wood")
    m.cube("crook", (-0.9, -4.0, -0.9), (1.8, 1.6, 1.8), "wood_knot")                 # die Griffwicklung
    m.cube("crook", (-0.9, -1.2, -0.9), (1.8, 2.8, 1.8), "rope")
    m.cube("crook", (-0.9, 15.8, -0.9), (1.8, 1.4, 1.8), "iron")                       # der eiserne Fuß
    m.cube("crook", (-0.95, -28.4, -0.95), (1.9, 1.8, 1.9), "iron")                    # die Zwinge unter dem Haken
    prev, piv = "crook", (0.0, -28.0, 0.0)
    for i, (w, length, rx) in enumerate(HOOK):
        b = f"hook_{i}"
        m.bone(b, prev, pivot=piv, rotation=(rx, 0.0, 0.0))
        m.cube(b, (-w / 2, -length - 0.2, -w / 2), (w, length + 0.4, w), "iron")
        prev, piv = b, (0.0, -length, 0.0)
    # Laterne an einer kurzen Kette an der Spitze; hängt senkrecht (hebt die Krümme auf, in Java nachgeführt)
    m.bone("lantern", prev, pivot=(0.0, -2.0, 0.0), rotation=(-sum(h[2] for h in HOOK), 0.0, 0.0))
    m.cube("lantern", (-0.2, 0.0, -0.2), (0.4, 2.4, 0.4), "iron")                      # die Kette
    m.cube("lantern", (-1.0, 2.2, -1.0), (2.0, 0.8, 2.0), "iron")                      # der Ring oben
    m.cube("lantern", (-2.2, 3.0, -2.2), (4.4, 1.0, 4.4), "lantern_roof")
    m.cube("lantern", (-1.8, 4.0, -1.8), (3.6, 4.4, 3.6), "lantern_glass")
    m.cube("lantern", (-2.2, 8.4, -2.2), (4.4, 1.0, 4.4), "lantern_roof")
    m.cube("lantern", (-0.6, 9.4, -0.6), (1.2, 0.8, 1.2), "iron")                      # der Zapfen unten

    # ------------------------------------------------------------------ die Handglocke (linke Hand)
    m.bone("hand_bell", "left_arm_fore", pivot=(0.0, 10.6, -0.4), rotation=_upright(m, "left_arm_fore"))
    m.cube("hand_bell", (-0.45, -0.6, -0.45), (0.9, 2.6, 0.9), "wood")                 # der Griff
    m.cube("hand_bell", (-1.3, 2.0, -1.3), (2.6, 0.9, 2.6), "bronze")
    m.cube("hand_bell", (-1.8, 2.9, -1.8), (3.6, 2.6, 3.6), "bronze")
    m.cube("hand_bell", (-2.3, 5.5, -2.3), (4.6, 0.9, 4.6), "bell_lip")
    m.cube("hand_bell", (-0.45, 5.8, -0.45), (0.9, 1.2, 0.9), "iron")                  # der Klöppel

    # ------------------------------------------------------------------ Irrlichter (kreisen um ihn)
    for i in range(3):
        w = f"wisp_{i}"
        m.bone(w, pivot=(0.0, -4.0, 0.0))
        m.cube(w, (-0.9, -0.9, -0.9), (1.8, 1.8, 1.8), "wisp")
        m.cube(w, (-0.55, -0.55, 0.9), (1.1, 1.1, 1.6), "wisp_tail")
        m.cube(w, (-0.3, -0.3, 2.5), (0.6, 0.6, 1.2), "wisp_tail")
    return m


# =============================================================================================
# Bemalen
# =============================================================================================

def _folds(ctx: Ctx, scale: float = 1.1) -> float:
    """Senkrechte Falten (über die Breite der Fläche), leicht unruhig."""
    x, y, z = ctx.world
    a = (x + z) * scale + noise3(x * 0.3, y * 0.15, z * 0.3, 2100) * 2.4
    return 0.5 + 0.5 * math.sin(a)


def _robe_value(ctx: Ctx, base: float) -> float:
    x, y, z = ctx.world
    v = base + 0.2 * _folds(ctx) + (noise3(x * 0.9, y * 0.5, z * 0.9, 2101) - 0.5) * 0.08 + _LIT[ctx.face]
    v -= max(0.0, (y - 14.0) / 10.0) * 0.18                                # unten dunkler (Sumpfwasser, Schatten)
    return v


def _robe(ctx: Ctx):
    """Die Kutte: aschgraues, grobes Tuch in weichen Falten, mit feinen Fäden quer."""
    v = _robe_value(ctx, 0.36)
    if ctx.t % 3 == 0 and hash01(ctx.s, ctx.t, ctx.cube.origin, 2102) > 0.7:
        v -= 0.06
    return pick(ctx.pal["robe"], v)


_GLYPHS = ({(1, 0), (1, 1), (2, 1)}, {(0, 0), (1, 1), (2, 0)}, {(1, 0), (2, 0), (1, 1)}, {(0, 1), (1, 0), (2, 1)}, {(1, 0), (1, 1)})


def _robe_hem(ctx: Ctx):
    """Der untere Teil der Kutte; knapp über dem Saum ein Band aus schwach glimmenden Seelenrunen."""
    if ctx.face in ("top", "bottom"):
        return pick(ctx.pal["robe"], 0.18 if ctx.face == "bottom" else 0.4)
    ly = ctx.local[1] - ctx.cube.origin[1]                                  # 0 oben … 5,2 unten
    if 3.4 <= ly <= 4.4:
        band_t = ly - 3.4
        if band_t < 0.2 or band_t > 0.8:
            return pick(ctx.pal["robe"], 0.12)                              # dunkle Borte
        glyph = _GLYPHS[int(hash01(ctx.s // 4, ctx.face, 2120) * len(_GLYPHS)) % len(_GLYPHS)]
        cell = (ctx.s % 4, 0 if band_t < 0.5 else 1)
        if cell in glyph:
            col = pick(ctx.pal["soul"], 0.5)
            ctx.glow = mix((0, 0, 0, 255), col, 0.7)
            return col
        return pick(ctx.pal["robe"], 0.2)
    return pick(ctx.pal["robe"], _robe_value(ctx, 0.32))


def _hem(ctx: Ctx):
    """Fetzen am Saum: nach unten dunkler, die Enden ausgefranst."""
    r = rel_y(ctx)
    if r > 0.7 and hash01(ctx.s, ctx.t // 2, ctx.cube.origin, 2103) > 1.25 - r * 0.8:
        return None
    if r > 0.55 and ctx.s % 3 == 1 and hash01(ctx.s, ctx.cube.origin, 2104) > 0.5:
        return None                                                         # Risse in der Länge
    return pick(ctx.pal["robe"], _robe_value(ctx, 0.3) - r * 0.08)


def _cloak(ctx: Ctx):
    """Der Umhang hinten: wie die Kutte, nur dunkler; innen fast schwarz."""
    if ctx.face == "front":
        return pick(ctx.pal["hood"], 0.18)
    return pick(ctx.pal["robe"], _robe_value(ctx, 0.26))


def _cloak_tip(ctx: Ctx):
    r = rel_y(ctx)
    if r > 0.55:
        k = hash01(ctx.s // 2, 2105)
        if r > 0.55 + k * 0.45:
            return None                                                     # unten in langen Zacken zerrissen
    if ctx.face == "front":
        return pick(ctx.pal["hood"], 0.16)
    return pick(ctx.pal["robe"], _robe_value(ctx, 0.24))


def _hood(ctx: Ctx):
    """Die Kapuze: tiefdunkles Tuch; die Innenseiten zum Schädel hin fast schwarz."""
    lx, ly, lz = ctx.local
    inner = ((ctx.face == "west" and lx > 3.0) or (ctx.face == "east" and lx < -3.0) or (ctx.face == "bottom" and ly < -8.0)
             or (ctx.face == "front" and lz > 3.0))
    if inner:
        return pick(ctx.pal["hood"], 0.02)
    x, y, z = ctx.world
    v = 0.42 + 0.2 * _folds(ctx, 1.6) + (noise3(x * 0.8, y * 0.8, z * 0.8, 2106) - 0.5) * 0.1 + _LIT[ctx.face]
    return pick(ctx.pal["hood"], v)


def _hood_rim(ctx: Ctx):
    """Der Saum der Kapuze: umgeschlagen, etwas heller, mit einer Naht."""
    v = 0.6 + _LIT[ctx.face] + (0.08 if (ctx.s + ctx.t) % 5 == 0 else 0.0)
    return pick(ctx.pal["hood"], v)


def _fleece(ctx: Ctx):
    """Schaffell: dicke, verfilzte Locken, schmutzig-hell, unten rußig."""
    x, y, z = ctx.world
    n1 = noise3(x * 1.6, y * 1.6, z * 1.6, 2107)
    n2 = noise3(x * 0.5, y * 0.5, z * 0.5, 2108)
    curl = abs(math.sin(x * 2.3 + n1 * 5.0) * math.cos(z * 2.1 + y * 1.7 + n1 * 4.0))
    v = 0.42 + curl * 0.3 + (n2 - 0.5) * 0.25 + _LIT[ctx.face]
    if ctx.face == "bottom":
        v -= 0.2
    return pick(ctx.pal["fleece"], v)


def _fleece_tuft(ctx: Ctx):
    """Herabhängende Zotteln: unten spitz und dunkler."""
    r = rel_y(ctx)
    w = ctx.w
    if r > 0.6 and ctx.face in ("front", "back", "west", "east"):
        half = (1.0 - r) / 0.4 * (w / 2.0)
        if abs(ctx.s - (w - 1) / 2.0) > half + 0.5:
            return None
    return mix(_fleece(ctx), pick(ctx.pal["fleece"], 0.15), r * 0.45)


def _skull(ctx: Ctx):
    """Der Widderschädel: altes, rußiges Bein voller Risse; vorn zwei tiefe, schräge Augenhöhlen, in deren Grund ein
    Funke Seelenlicht brennt; darüber vorspringende Brauenwülste."""
    x, y, z = ctx.world
    n = noise3(x * 1.1, y * 1.1, z * 1.1, 2109)
    v = 0.5 + (n - 0.5) * 0.18 + _LIT[ctx.face]
    lx, ly = ctx.local[0], ctx.local[1]
    if ctx.face == "front":
        # die Höhle: eine schräge Ellipse (außen höher), innen dunkel, in der Mitte der Funke
        ax = abs(lx) - 1.3
        ay = ly + 4.75
        rot = math.radians(-26.0)                                         # innen tiefer: ein finsterer Blick
        u = ax * math.cos(rot) + ay * math.sin(rot)
        w = -ax * math.sin(rot) + ay * math.cos(rot)
        d = math.hypot(u / 1.05, w / 0.62)
        core = math.hypot(ax + 0.08, ay - 0.05)
        if d < 1.0:
            if core < 0.26:
                col = pick(ctx.pal["soul"], 0.92)
                ctx.glow = col
                return col
            if core < 0.5:
                col = pick(ctx.pal["soul"], 0.38)
                ctx.glow = mix((0, 0, 0, 255), col, 0.75)
                return col
            return pick(ctx.pal["skull"], 0.0 if d < 0.8 else 0.08)
        if d < 1.35 and w < 0:
            v += 0.16                                                       # die Brauenwülste (hell, vorspringend)
        elif d < 1.3:
            v -= 0.16                                                       # der Rand der Höhle
        if abs(lx) < 0.35 and ly > -4.2:
            v -= 0.14                                                       # die Naht zur Schnauze
    if ly < -5.6 or ctx.face == "top":
        v -= 0.22                                                           # unter der Kapuze im Schatten
    if hash01(ctx.u, ctx.v, 2110) > 0.93:
        v -= 0.28                                                           # Risse
    return pick(ctx.pal["skull"], v)


def _snout(ctx: Ctx):
    """Die Schnauze: oben das lange Nasenbein (hell), an den Seiten Schatten und unten eine Reihe Zähne."""
    lx, ly, lz = ctx.local
    v = 0.46 + (-lz / 4.2) * 0.12 + _LIT[ctx.face] + (hash01(ctx.u, ctx.v, 2111) - 0.5) * 0.1
    if ctx.face == "top":
        v += 0.1 if abs(lx) < 0.5 else 0.0
        if abs(lx) < 0.18:
            v -= 0.2                                                        # die Naht zwischen den Nasenbeinen
    if ctx.face in ("west", "east"):
        if ly > 2.2:
            return pick(ctx.pal["skull"], 0.78 if int(lz * 2) % 2 == 0 else 0.3)   # die Zahnreihe
        v -= 0.1
    if ctx.face == "bottom":
        return pick(ctx.pal["skull"], 0.06)
    return pick(ctx.pal["skull"], v)


def _nose(ctx: Ctx):
    """Die Spitze der Schnauze: vorn oben die herzförmige Nasenöffnung, tief und dunkel."""
    lx, ly = ctx.local[0], ctx.local[1]
    if ctx.face == "front":
        ny = (ly - 0.2) / 2.2                                               # 0 oben … 1 unten
        if ny < 0.34 and abs(lx) < 0.55:
            return pick(ctx.pal["skull"], 0.0)
    if ctx.face == "top" and abs(lx) < 0.5 and ctx.local[2] < -4.8:
        return pick(ctx.pal["skull"], 0.02)                                 # die Öffnung reicht auf die Oberseite
    if ctx.face == "bottom":
        return pick(ctx.pal["skull"], 0.08)
    return pick(ctx.pal["skull"], 0.62 + _LIT[ctx.face] + (hash01(ctx.u, ctx.v, 2121) - 0.5) * 0.1)


def _jaw(ctx: Ctx):
    """Der Unterkiefer, oben eine Reihe Zähne, innen dunkel."""
    if ctx.face == "top":
        if abs(ctx.local[0]) > 0.7:
            return pick(ctx.pal["skull"], 0.82 if int(-ctx.local[2] * 2) % 2 == 0 else 0.5)
        return pick(ctx.pal["skull"], 0.04)
    return pick(ctx.pal["skull"], 0.4 + _LIT[ctx.face] + (hash01(ctx.u, ctx.v, 2122) - 0.5) * 0.1)


def _horn(ctx: Ctx):
    """Widderhorn: quer gerippt, vom dunklen Ansatz zur hellen, glatten Spitze."""
    parts = (ctx.cube.tag or "0:1").split(":")
    a, b = float(parts[0]), float(parts[1])
    lz = ctx.local[2]
    t = a + (b - a) * max(0.0, min(1.0, lz / max(0.1, ctx.cube.size[2])))
    ridge = (lz * 1.3) % 1.0
    v = 0.2 + 0.5 * t + _LIT[ctx.face]
    if t < 0.85:
        v += 0.14 if ridge < 0.3 else (-0.1 if ridge > 0.8 else 0.0)
    if ctx.face == "top":
        v += 0.08
    return pick(ctx.pal["horn"], v + (hash01(ctx.u, ctx.v, 2112) - 0.5) * 0.08)


def _flesh(ctx: Ctx):
    """Tote, graue Haut straff über Knochen; Sehnen in der Länge."""
    x, y, z = ctx.world
    v = 0.4 + (noise3(x * 0.9, y * 0.9, z * 0.9, 2113) - 0.5) * 0.16 + _LIT[ctx.face]
    if ctx.face in ("front", "back", "west", "east") and ctx.s % 3 == 1:
        v += 0.07
    return pick(ctx.pal["flesh"], v)


def _finger(ctx: Ctx):
    if rel_y(ctx) > 0.72:
        return pick(ctx.pal["horn"], 0.12)                                  # dunkle Krallen
    return pick(ctx.pal["flesh"], 0.5 + _LIT[ctx.face] + (0.1 if ctx.t % 3 == 0 else 0.0))


def _sleeve(ctx: Ctx):
    return pick(ctx.pal["robe"], _robe_value(ctx, 0.34))


def _sleeve_wide(ctx: Ctx):
    if ctx.face == "bottom":
        return pick(ctx.pal["robe"], 0.04)                                  # innen dunkel
    v = _robe_value(ctx, 0.38)
    if rel_y(ctx) > 0.82 and ctx.face != "top":
        v -= 0.14                                                           # der Saum
    return pick(ctx.pal["robe"], v)


def _rags(ctx: Ctx):
    band = (ctx.t + (ctx.s // 2)) % 3 == 0
    return pick(ctx.pal["rope"], 0.3 + (0.14 if band else 0.0) + _LIT[ctx.face])


def _rope(ctx: Ctx):
    twist = (ctx.s + ctx.t) % 3 == 0
    return pick(ctx.pal["rope"], 0.42 + (0.2 if twist else 0.0) + _LIT[ctx.face])


def _bone(ctx: Ctx):
    return pick(ctx.pal["skull"], 0.62 + _LIT[ctx.face] + (hash01(ctx.u, ctx.v, 2114) - 0.5) * 0.12)


def _vial(ctx: Ctx):
    """Ein Fläschchen mit eingefangener Seele."""
    if ctx.face in ("top", "bottom"):
        return pick(ctx.pal["iron"], 0.4)
    col = pick(ctx.pal["glass"], 0.55 + (0.3 if ctx.s == 1 else 0.0))
    ctx.glow = mix((0, 0, 0, 255), col, 0.8)
    return col


def _bronze(ctx: Ctx):
    """Die Glocke: dunkle Bronze, grünspanig, oben ein heller Glanz."""
    v = 0.42 + (1.0 - rel_y(ctx)) * 0.2 + _LIT[ctx.face]
    if ctx.s == 1 and ctx.face in ("front", "west"):
        v += 0.25                                                           # Glanzkante
    if hash01(ctx.u, ctx.v, 2115) > 0.8:
        v -= 0.12
    return pick(ctx.pal["bronze"], v)


def _bell_lip(ctx: Ctx):
    if ctx.face == "bottom":
        col = pick(ctx.pal["soul"], 0.35)                                   # innen schimmert Seelenlicht
        ctx.glow = mix((0, 0, 0, 255), col, 0.6)
        return col
    return pick(ctx.pal["bronze"], 0.5 + _LIT[ctx.face] + (0.12 if ctx.t == 0 else 0.0))


def _wood(ctx: Ctx):
    """Sumpfeiche: dunkel, mit Längsfasern, Kerben und Astlöchern."""
    x, y, z = ctx.world
    fiber = math.sin(ctx.s * 2.1 + y * 0.3) * 0.07
    v = 0.48 + fiber + _LIT[ctx.face] + (noise3(x * 0.4, y * 0.5, z * 0.4, 2116) - 0.5) * 0.2
    if hash01(ctx.s, int(y * 1.5), ctx.face, 2117) > 0.95:
        v -= 0.28
    return pick(ctx.pal["wood"], v)


def _wood_knot(ctx: Ctx):
    return pick(ctx.pal["wood"], 0.28 + _LIT[ctx.face] + (0.12 if ctx.t == 0 else 0.0))


def _iron(ctx: Ctx):
    """Geschmiedetes Eisen: dunkel, an den Kanten heller, hier und da Rost."""
    v = 0.4 + _LIT[ctx.face] + (0.16 if ctx.s == 0 or ctx.t == 0 else 0.0)
    if hash01(ctx.u, ctx.v, 2118) > 0.88:
        return pick(ctx.pal["rust"], 0.5 + (hash01(ctx.u, ctx.v, 2119) - 0.5) * 0.4)
    return pick(ctx.pal["iron"], v)


def _lantern_roof(ctx: Ctx):
    if ctx.face == "bottom":
        col = pick(ctx.pal["glass"], 0.5)                                   # von unten angestrahlt
        ctx.glow = mix((0, 0, 0, 255), col, 0.5)
        return col
    return pick(ctx.pal["iron"], 0.45 + _LIT[ctx.face] + (0.15 if ctx.t == 0 else 0.0))


def _lantern_glass(ctx: Ctx):
    """Das Glas der Laterne: dahinter Seelenfeuer – in der Mitte gleißend, zum Rand türkis; dazu eiserne Streben."""
    if ctx.face in ("top", "bottom"):
        col = pick(ctx.pal["glass"], 0.7)
        ctx.glow = col
        return col
    if ctx.s in (0, ctx.w - 1):
        return pick(ctx.pal["iron"], 0.35)                                  # die Streben an den Ecken
    cx = (ctx.w - 1) / 2.0
    ds = abs(ctx.s - cx) / max(1.0, cx)
    dt = (ctx.t - ctx.h * 0.62) / (ctx.h * 0.5)
    flame = ds * 1.1 + max(0.0, dt) * 0.8 + max(0.0, -dt) * 0.55
    col = pick(ctx.pal["glass"], 1.0 - flame * 0.55)
    ctx.glow = col
    return col


def _wisp(ctx: Ctx):
    col = pick(ctx.pal["soul"], 0.92 if (ctx.s + ctx.t) % 3 else 1.0)
    ctx.glow = col
    return col


def _wisp_tail(ctx: Ctx):
    col = pick(ctx.pal["soul"], 0.62 - rel_y(ctx) * 0.1 - (0.15 if ctx.cube.size[2] < 1.4 else 0.0))
    ctx.glow = col
    return col


def materials(variant: str) -> dict:
    return {
        "robe": _robe, "robe_hem": _robe_hem, "hem": _hem, "cloak": _cloak, "cloak_tip": _cloak_tip, "hood": _hood,
        "hood_rim": _hood_rim, "fleece": _fleece, "fleece_tuft": _fleece_tuft, "skull": _skull, "snout": _snout, "nose": _nose,
        "jaw": _jaw, "horn": _horn, "flesh": _flesh, "finger": _finger, "sleeve": _sleeve, "sleeve_wide": _sleeve_wide,
        "rags": _rags, "rope": _rope, "bone": _bone, "vial": _vial, "bronze": _bronze, "bell_lip": _bell_lip, "wood": _wood,
        "wood_knot": _wood_knot, "iron": _iron, "lantern_roof": _lantern_roof, "lantern_glass": _lantern_glass, "wisp": _wisp,
        "wisp_tail": _wisp_tail,
    }
