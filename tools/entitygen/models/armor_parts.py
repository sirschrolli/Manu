"""
3D-Teile der getragenen Rüstungen (seit 0.95): über der gewöhnlichen (flachen) Rüstung sitzen Teile passend zum Material.

  * Stahl: Kamm und roter Helmbusch, Schulterstücke aus drei Platten mit Goldsaum, Ellbogenkacheln, Beintaschen und
    Kniebuckel, Schuhspitzen und goldene Sporen – ein Ritter
  * Konterit (leicht und schnell): Flügel an den Schläfen, ein Stirnstein, sichelförmige Schulterstücke, ein kurzer Umhang,
    Kniesteine und Flügel an den Knöcheln
  * Nachtstahl: eine Krone aus Nachtkristallen, Stacheln auf Schultern, Rücken, Knien und Fersen (die Spitzen leuchten)
  * Abyssit: zwei große Hörner, ein leuchtender Stirnkristall, schwere Schulterstücke mit Kristallen, Kristalle auf dem
    Rücken, an den Schenkeln und Fersen
  * Drachenschuppen (je Element): Hörner (Feuer geschwungen mit Glut, Eis aus Kristall, Blitz gezackt, Natur ein Geweih mit
    Blättern, Erde Widderhörner, Wasser Flossen), ein Kamm, Rückenstacheln (Natur Blätter, Wasser ein Flossensaum, Erde
    Platten), Schulterschuppen mit Kralle, ein Drachenschwanz zu den Beinschienen (mit der Spitze des Elements) und Krallen
    an den Stiefeln
  * Edelsteinkrone: Zacken und Edelsteine über dem Reif
  * Mondstein (seit 0.96, die Rüstung der Zauberer): ein Stirnreif aus Mondsilber mit leuchtender Mondsichel und Mondstein,
    ein langer Sternenmantel bis an die Knie, Mondsicheln auf den Schultern, Armreife mit Mondsteinen, ein Gürtel mit
    Sichelschnalle, Robenschöße über den Beinen und spitze, aufgebogene Schuhe mit Mondsicheln an den Knöcheln
  * Dämmerquarz (seit 0.98, die Rüstung der Schleicher): eine tiefe Kapuze mit langem Zipfel, der über den Rücken fällt,
    eine Halbmaske aus Stoff über Mund und Nase; ein kurzer, unten zerschlissener Schulterumhang mit einer Schließe aus
    zwei Quarzspitzen, Lederwickel an den Oberarmen und Handgelenken; ein Gürtel mit zwei Taschen und einem Dolch im
    Rücken, Knieschützer aus Leder mit Quarzniete; weiche Stiefel mit umgeschlagenem Schaft und Wickelriemen
  * Aquamarin (seit 0.97, die Taucherrüstung): ein runder Taucherhelm aus Aquamarin mit Messingkragen, vorn ein großes
    Sichtfenster (man sieht das Gesicht dahinter), an den Seiten kleine Bullaugen, oben ein Ventil, hinten der Schlauch;
    zwei Luftflaschen auf dem Rücken mit Ventilen und Messingbändern, eine Druckanzeige auf der Brust, flache
    Schulterplatten und Armreife; ein Bleigürtel mit Messingschnalle, Knieplatten; Flossen an den Schuhen

Knochen: <satz>_<platz>_<knochen> (z. B. steel_chest_left_arm), je im Raum des gleichnamigen Knochens des Spielers
(Drehpunkt 0) – ArmorPartsLayer.java hängt sie an Kopf, Körper, Arme und Beine. Die Oberfläche der flachen Rüstung liegt
beim Helm bei ±5 (oben -9), beim Brustpanzer bei x ±5, z ±3, an den Armen bei y -3 (oben), an den Stiefeln bei ±2.9.
"""
from __future__ import annotations

import math

from ..geometry import Model
from ..paint import Ctx, along, hash01, hexc, mix, noise3, pick, ramp, rel_y

SETS = ["steel", "konterit", "nightsteel", "abyssite", "fire", "ice", "lightning", "nature", "earth", "water", "crown",
        "leather", "chainmail", "iron", "gold", "diamond", "netherite", "copper", "turtle", "moonstone", "aquamarine", "dusk"]
SLOTS = {"head": ["head"], "chest": ["body", "left_arm", "right_arm"], "legs": ["body", "left_leg", "right_leg"],
         "feet": ["left_leg", "right_leg"]}

PALETTES = {
    "armor_parts": {
        "steel": ramp("#3a4250", "#eef3f8", 8),
        "gold": ramp("#5a3606", "#fff0b0", 8),
        "crown_gold": ramp("#6a3c02", "#fff2a0", 8, hue_shift=0.02),
        "plume": ramp("#3a040a", "#ff7a5e", 7),
        "kon": ramp("#260838", "#e4a8f8", 8),
        "silver": ramp("#454c5a", "#f4f6fa", 7),
        "feather": ramp("#8a7cae", "#ffffff", 7),
        "kon_cloth": ramp("#22062e", "#a45ad0", 7),
        "kon_gem": ramp("#7a2ab0", "#ffe0ff", 6),
        "night": ramp("#12122a", "#a2aadc", 8),
        "night_crystal": ramp("#2a1a6a", "#f0ecff", 7),
        "night_glow": ramp("#7a6aff", "#ffffff", 5),
        "aby": ramp("#0a0610", "#6e5c7c", 7),
        "aby_crystal": ramp("#5a0a52", "#ffc0f4", 7),
        "aby_glow": ramp("#d02ab8", "#ffd0f8", 5),
        "fire_horn": ramp("#120606", "#6a4a40", 7),
        "fire_glow": ramp("#ff4a10", "#fff0a0", 5),
        "fire_scale": ramp("#3e0206", "#f0603a", 8),
        "ice_crystal": ramp("#2e5e8e", "#f6fcff", 7),
        "ice_glow": ramp("#9ae0ff", "#ffffff", 4),
        "ice_scale": ramp("#3a5a7e", "#dcf0ff", 8),
        "light_horn": ramp("#121218", "#6e6c7e", 7),
        "light_gold": ramp("#6a4a06", "#fff4a0", 7),
        "light_glow": ramp("#ffd02a", "#ffffe0", 4),
        "light_scale": ramp("#18181f", "#9a96a6", 8),
        "antler": ramp("#2e1c0c", "#d8b480", 7),
        "leaf": ramp("#0a3410", "#a8e070", 7),
        "nature_scale": ramp("#00320e", "#7ac862", 8),
        "ram": ramp("#3a2814", "#ecdcb8", 8),
        "stone": ramp("#34343a", "#b2b0b6", 7),
        "earth_scale": ramp("#341e0c", "#c4a27a", 8),
        "amber": ramp("#7a3a04", "#ffd27a", 5),
        "fin": ramp("#023452", "#a8f0f4", 7),
        "water_scale": ramp("#022c48", "#64c2e2", 8),
        "pearl": ramp("#a89a90", "#ffffff", 5),
        "ruby": ramp("#5a0412", "#ff8a96", 5),
        "sapphire": ramp("#0a1a6a", "#9ac4ff", 5),
        "emerald": ramp("#043a18", "#8af0a8", 5),
        "amethyst": ramp("#3a0e6a", "#e0b0ff", 5),
        # die Rüstungen von Minecraft (eigene Farben, passend zu ihren Werkstoffen)
        "fur": ramp("#5a3e26", "#f2e2c4", 7),
        "leather": ramp("#3a2210", "#b07a48", 7),
        "plume_white": ramp("#7a2a1a", "#ffffff", 6),
        "mail": ramp("#3a3e46", "#d0d4da", 7),
        "iron": ramp("#5a5e66", "#f4f4f4", 8),
        "gold_armor": ramp("#9a6000", "#fffbd2", 8, hue_shift=0.02),
        "diamond": ramp("#06505e", "#b0fff2", 8, hue_shift=0.02),
        "netherite": ramp("#1a1618", "#7a6e72", 8),
        "copper": ramp("#6a2e18", "#f6b080", 8),
        "verdigris": ramp("#1e5a4a", "#8ae0c0", 6),
        "turtle": ramp("#163a12", "#9ad06a", 7),
        # Mondstein (0.96)
        "moon": ramp("#3a4486", "#edf1ff", 8),
        "moon_glow": ramp("#2f6ee6", "#eefaff", 6),
        "moon_gem": ramp("#6c78c4", "#ffffff", 6),
        "moon_cloth": ramp("#0a0c2a", "#3e4ca6", 7),
        "moon_star": ramp("#9a9ae8", "#fffef0", 4),
        # Aquamarin (0.97)
        "aqua": ramp("#05497a", "#a4f2ff", 8),
        "brass": ramp("#4a2c06", "#ffeaa4", 8, hue_shift=0.02),
        "aqua_glass": ramp("#06222e", "#4aa8be", 6),
        "aqua_gem": ramp("#1f78a8", "#e6fdff", 6),
        "rubber": ramp("#101418", "#4e5a64", 6),
        "lead": ramp("#25282e", "#a0a6b2", 7),
        "aqua_fin": ramp("#063c44", "#7fe4e0", 8),
        "dial": ramp("#b8b0a0", "#fffcf2", 4),
        # Dämmerquarz (0.98): die Rüstung der Schleicher
        "dusk_cloth": ramp("#16121f", "#7a6e96", 8),
        "dusk_leather": ramp("#1e130c", "#9a7658", 7),
        "dusk_quartz": ramp("#2a1d4a", "#efe6ff", 7),
        "dusk_ember": ramp("#5c2434", "#ffcaa4", 6),
    }
}

# Elemente der Drachenschuppen: Schuppenfarbe, Hornfarbe, Leuchtfarbe (oder None)
DRAGON = {
    "fire": ("fire_scale", "fire_horn", "fire_glow"),
    "ice": ("ice_scale", "ice_crystal", "ice_glow"),
    "lightning": ("light_scale", "light_horn", "light_glow"),
    "nature": ("nature_scale", "antler", None),
    "earth": ("earth_scale", "ram", None),
    "water": ("water_scale", "fin", None),
}


# =============================================================================================
# Aufbau
# =============================================================================================

def _slot(m: Model, s: str, slot: str, bone: str) -> str:
    name = f"{s}_{slot}_{bone}"
    m.bone(name, "root", (0.0, 0.0, 0.0))
    return name


def _chain(m: Model, parent: str, name: str, pivot, segs, material, kind: str = "up", overlap: float = 0.25) -> list[str]:
    """Eine Kette aus Gliedern (Horn, Stachel, Schwanz, Federkiel): jedes Glied wächst aus seinem Knochen nach oben (-y,
    kind="up") oder unten (+y, kind="down"), das nächste sitzt an seiner Spitze. segs: (breite, länge, tiefe, drehung) –
    die Drehung des ersten Glieds gilt gegenüber dem Elternknochen, die übrigen gegenüber dem vorigen Glied. Die Glieder
    tragen im Tag, welches Stück der ganzen Länge sie sind (für Verläufe vom Ansatz zur Spitze)."""
    total = sum(s[1] for s in segs)
    acc = 0.0
    prev, piv = parent, pivot
    names = []
    for i, (w, length, d, rot) in enumerate(segs):
        bname = f"{name}_{i}" if len(segs) > 1 else name
        m.bone(bname, prev, piv, rot)
        a, b = acc / total, (acc + length) / total
        mat = material(i) if callable(material) else material
        if kind == "up":
            m.cube(bname, (-w / 2, -length, -d / 2), (w, length, d), mat, tag=f"up:{a:.3f}:{b:.3f}")
            piv = (0.0, -length + overlap, 0.0)
        else:
            m.cube(bname, (-w / 2, 0.0, -d / 2), (w, length, d), mat, tag=f"down:{a:.3f}:{b:.3f}")
            piv = (0.0, length - overlap, 0.0)
        acc += length
        prev = bname
        names.append(bname)
    return names


def _spike(m: Model, parent: str, name: str, pivot, rot, height: float, width: float, material) -> None:
    """Ein Stachel oder Kristall: breiter Grund, schmale Spitze."""
    _chain(m, parent, name, pivot, [(width, height * 0.55, width, rot), (width * 0.55, height * 0.5, width * 0.55, (0.0, 0.0, 0.0))],
           material)


def _mx(x: float, w: float, side: int) -> float:
    """Ursprung eines Quaders der Breite w auf der linken Seite (side=1) oder gespiegelt auf der rechten (side=-1)."""
    return x if side > 0 else -(x + w)


# ------------------------------------------------------------------ Stahl

def _steel(m: Model) -> None:
    h = _slot(m, "steel", "head", "head")
    m.cube(h, (-0.5, -10.6, -4.6), (1.0, 1.7, 9.2), "steel_plate")                         # Kamm
    m.cube(h, (-1.0, -11.2, 2.2), (2.0, 1.4, 2.2), "gold")                                   # Halter des Buschs
    _chain(m, h, "steel_plume", (0.0, -10.6, 3.3), [(2.6, 2.6, 2.6, (-12.0, 0.0, 0.0)), (3.0, 3.2, 2.8, (-28.0, 0.0, 0.0)),
                                                     (2.9, 3.6, 2.6, (-32.0, 0.0, 0.0)), (2.5, 3.6, 2.2, (-30.0, 0.0, 0.0)),
                                                     (2.0, 3.4, 1.8, (-22.0, 0.0, 0.0)), (1.4, 3.0, 1.3, (-14.0, 0.0, 0.0))], "plume")
    _slot(m, "steel", "chest", "body")
    for side, bone in ((1, "left_arm"), (-1, "right_arm")):
        a = _slot(m, "steel", "chest", bone)
        p = f"steel_pauldron_{bone}"
        m.bone(p, a, (1.0 * side, -3.0, 0.0), (0.0, 0.0, 14.0 * side))
        m.cube(p, (_mx(-3.4, 7.2, side), -1.7, -3.7), (7.2, 2.1, 7.4), "steel_pauldron")
        m.cube(p, (_mx(-2.3, 6.6, side), 0.2, -3.8), (6.6, 1.8, 7.6), "steel_pauldron")
        m.cube(p, (_mx(-1.1, 5.6, side), 1.8, -3.6), (5.6, 1.6, 7.2), "steel_pauldron")
        m.cube(a, (_mx(3.6, 1.0, side), 4.2, -1.7), (1.0, 3.2, 3.4), "steel_plate")         # Ellbogenkachel
        m.cube(a, (_mx(4.4, 0.6, side), 5.2, -0.7), (0.6, 1.2, 1.4), "gold")
    _slot(m, "steel", "legs", "body")
    for side, bone in ((1, "left_leg"), (-1, "right_leg")):
        g = _slot(m, "steel", "legs", bone)
        t = f"steel_tasset_{bone}"
        m.bone(t, g, (0.0, -0.8, -2.8), (-7.0, 0.0, 0.0))
        m.cube(t, (-2.8, 0.0, -0.6), (5.6, 4.8, 1.0), "steel_pauldron")                     # Beintasche mit Goldsaum
        m.cube(g, (-2.0, 5.1, -3.3), (4.0, 2.8, 1.0), "steel_plate")                         # Kniebuckel
        m.cube(g, (_mx(2.1, 0.8, side), 5.5, -2.8), (0.8, 2.0, 2.2), "steel_plate")         # Seitenflügel des Knies
    for bone in ("left_leg", "right_leg"):
        f = _slot(m, "steel", "feet", bone)
        m.cube(f, (-2.5, 10.5, -4.2), (5.0, 2.4, 1.5), "steel_sabaton")                      # Schuhspitze
        m.cube(f, (-0.35, 10.6, 2.8), (0.7, 0.7, 1.9), "gold")                               # Sporn
        m.cube(f, (-0.9, 10.05, 4.5), (1.8, 1.8, 0.5), "spur")                               # Rädchen


# ------------------------------------------------------------------ Konterit

def _wing(m: Model, parent: str, name: str, pivot, rot, scale: float = 1.0) -> None:
    """Ein kleiner Flügel: ein silberner Ansatz, daraus fünf Federn als Fächer (flach, in der y-z-Ebene) – die unterste
    zeigt nach hinten, die oberen steigen immer steiler auf, die längsten in der Mitte."""
    m.bone(name, parent, pivot, rot)
    s = scale
    m.cube(name, (-0.6, -1.6 * s, -1.2 * s), (1.2, 2.4 * s, 2.0 * s), "silver")
    for k, (angle, length, width) in enumerate(((4.0, 4.6, 1.3), (20.0, 5.6, 1.3), (36.0, 5.8, 1.2), (52.0, 5.0, 1.1), (68.0, 3.8, 1.0))):
        f = f"{name}_feather_{k}"
        m.bone(f, name, (0.0, -0.4 * s - 0.25 * k * s, 0.4 * s), (angle, 0.0, 0.0))
        m.cube(f, (-0.25, -width * s / 2, 0.0), (0.5, width * s, length * s), "feather")


def _konterit(m: Model) -> None:
    h = _slot(m, "konterit", "head", "head")
    _wing(m, h, "kon_wing_left", (5.2, -5.2, -0.8), (16.0, 16.0, 0.0), 1.45)
    _wing(m, h, "kon_wing_right", (-5.2, -5.2, -0.8), (16.0, -16.0, 0.0), 1.45)
    m.cube(h, (-1.0, -6.8, -5.6), (2.0, 2.0, 0.7), "kon_gem")                               # Stirnstein
    b = _slot(m, "konterit", "chest", "body")
    m.bone("kon_cape", b, (0.0, 0.2, 3.0), (6.0, 0.0, 0.0))
    m.cube("kon_cape", (-4.5, 0.0, 0.0), (9.0, 6.6, 0.6), "kon_cloth")
    m.bone("kon_cape_hem", "kon_cape", (0.0, 6.4, 0.05), (8.0, 0.0, 0.0))
    m.cube("kon_cape_hem", (-4.3, 0.0, 0.0), (8.6, 5.2, 0.6), "kon_cloth")
    m.cube(b, (-4.9, -0.8, -3.5), (1.4, 1.4, 0.7), "silver")                               # Spangen des Umhangs
    m.cube(b, (3.5, -0.8, -3.5), (1.4, 1.4, 0.7), "silver")
    for side, bone in ((1, "left_arm"), (-1, "right_arm")):
        a = _slot(m, "konterit", "chest", bone)
        p = f"kon_pauldron_{bone}"
        m.bone(p, a, (1.0 * side, -3.2, 0.0), (0.0, 0.0, 12.0 * side))
        m.cube(p, (_mx(-3.0, 6.6, side), -1.3, -3.5), (6.6, 1.5, 7.0), "kon_plate")
        m.cube(p, (_mx(-0.6, 1.2, side), -1.9, -3.5), (1.2, 0.7, 7.0), "silver")            # Grat
        m.cube(p, (_mx(1.2, 1.6, side), -2.2, -0.8), (1.6, 1.0, 1.6), "kon_gem")
    _slot(m, "konterit", "legs", "body")
    for bone in ("left_leg", "right_leg"):
        g = _slot(m, "konterit", "legs", bone)
        m.cube(g, (-1.5, 4.6, -3.0), (3.0, 3.2, 0.8), "kon_plate")
        m.cube(g, (-0.6, 5.6, -3.4), (1.2, 1.2, 0.5), "kon_gem")
    for side, bone in ((1, "left_leg"), (-1, "right_leg")):
        f = _slot(m, "konterit", "feet", bone)
        _wing(m, f, f"kon_heel_{bone}", (2.9 * side, 9.6, 0.2), (24.0, 14.0 * side, 0.0), 0.85)


# ------------------------------------------------------------------ Nachtstahl

def _nightsteel(m: Model) -> None:
    h = _slot(m, "nightsteel", "head", "head")
    m.cube(h, (-5.3, -9.5, -3.5), (10.6, 1.0, 1.2), "night_plate")                          # Reif der Krone
    for k, (x, height) in enumerate(((-3.6, 2.6), (-1.8, 3.8), (0.0, 5.2), (1.8, 3.8), (3.6, 2.6))):
        _spike(m, h, f"night_crown_{k}", (x, -9.3, -2.9), (-8.0, 0.0, x * 6.0), height, 1.8 if k == 2 else 1.5, "night_crystal")
    b = _slot(m, "nightsteel", "chest", "body")
    for k, (y, height) in enumerate(((1.6, 4.0), (4.6, 3.2), (7.6, 2.4))):
        _spike(m, b, f"night_back_{k}", (0.0, y, 2.9), (-52.0, 0.0, 0.0), height, 1.6, "night_crystal")
    for side, bone in ((1, "left_arm"), (-1, "right_arm")):
        a = _slot(m, "nightsteel", "chest", bone)
        p = f"night_pauldron_{bone}"
        m.bone(p, a, (1.0 * side, -3.0, 0.0), (0.0, 0.0, 10.0 * side))
        m.cube(p, (_mx(-3.2, 6.8, side), -1.5, -3.6), (6.8, 1.7, 7.2), "night_plate")
        for k, (z, height) in enumerate(((-2.2, 2.6), (0.0, 3.6), (2.2, 2.6))):
            _spike(m, p, f"night_shoulder_{bone}_{k}", (2.2 * side, -1.4, z), (0.0, 0.0, 28.0 * side), height, 1.4, "night_crystal")
    _slot(m, "nightsteel", "legs", "body")
    for bone in ("left_leg", "right_leg"):
        g = _slot(m, "nightsteel", "legs", bone)
        m.cube(g, (-1.9, 4.8, -3.2), (3.8, 3.0, 1.0), "night_plate")
        _spike(m, g, f"night_knee_{bone}", (0.0, 6.0, -3.0), (62.0, 0.0, 0.0), 2.8, 1.3, "night_crystal")
    for bone in ("left_leg", "right_leg"):
        f = _slot(m, "nightsteel", "feet", bone)
        m.cube(f, (-2.4, 10.7, -3.9), (4.8, 2.1, 1.0), "night_plate")
        _spike(m, f, f"night_heel_{bone}", (0.0, 11.0, 2.8), (-58.0, 0.0, 0.0), 2.4, 1.2, "night_crystal")


# ------------------------------------------------------------------ Abyssit

def _abyssite(m: Model) -> None:
    h = _slot(m, "abyssite", "head", "head")
    for side in (1, -1):
        _chain(m, h, f"aby_horn_{'left' if side > 0 else 'right'}", (4.0 * side, -8.6, -1.2),
               [(2.6, 3.0, 2.6, (-12.0, 0.0, 34.0 * side)), (2.1, 3.0, 2.1, (-28.0, 0.0, -10.0 * side)),
                (1.6, 2.8, 1.6, (-34.0, 0.0, -12.0 * side)), (1.1, 2.4, 1.1, (-36.0, 0.0, -6.0 * side)),
                (0.7, 1.8, 0.7, (-30.0, 0.0, 0.0))], "aby_horn")
    _spike(m, h, "aby_brow", (0.0, -6.2, -5.2), (-14.0, 0.0, 0.0), 3.2, 1.6, "aby_crystal")
    b = _slot(m, "abyssite", "chest", "body")
    _spike(m, b, "aby_back_0", (0.0, 3.0, 2.9), (-46.0, 0.0, 0.0), 4.8, 1.8, "aby_crystal")
    _spike(m, b, "aby_back_1", (1.7, 3.8, 2.9), (-40.0, 0.0, 22.0), 3.4, 1.3, "aby_crystal")
    _spike(m, b, "aby_back_2", (-1.7, 3.8, 2.9), (-40.0, 0.0, -22.0), 3.4, 1.3, "aby_crystal")
    for side, bone in ((1, "left_arm"), (-1, "right_arm")):
        a = _slot(m, "abyssite", "chest", bone)
        p = f"aby_pauldron_{bone}"
        m.bone(p, a, (1.0 * side, -3.0, 0.0), (0.0, 0.0, 16.0 * side))
        m.cube(p, (_mx(-3.6, 7.6, side), -2.0, -3.9), (7.6, 2.4, 7.8), "aby_plate")
        m.cube(p, (_mx(-2.4, 6.8, side), 0.3, -4.0), (6.8, 2.0, 8.0), "aby_plate")
        _spike(m, p, f"aby_shoulder_{bone}_0", (1.4 * side, -1.9, -1.4), (10.0, 0.0, 24.0 * side), 3.6, 1.5, "aby_crystal")
        _spike(m, p, f"aby_shoulder_{bone}_1", (2.2 * side, -1.9, 1.5), (-12.0, 0.0, 36.0 * side), 2.6, 1.1, "aby_crystal")
    _slot(m, "abyssite", "legs", "body")
    for bone in ("left_leg", "right_leg"):
        g = _slot(m, "abyssite", "legs", bone)
        m.cube(g, (-2.7, -0.5, -3.3), (5.4, 4.2, 1.0), "aby_plate")
        m.cube(g, (-0.7, 0.4, -3.8), (1.4, 2.4, 0.6), "aby_gem")
        m.cube(g, (-2.0, 5.0, -3.3), (4.0, 2.8, 1.0), "aby_plate")
    for bone in ("left_leg", "right_leg"):
        f = _slot(m, "abyssite", "feet", bone)
        m.cube(f, (-2.5, 10.7, -4.0), (5.0, 2.1, 1.1), "aby_plate")
        _spike(m, f, f"aby_heel_{bone}_0", (0.9, 10.8, 2.8), (-56.0, 0.0, 12.0), 2.6, 1.1, "aby_crystal")
        _spike(m, f, f"aby_heel_{bone}_1", (-0.9, 10.8, 2.8), (-56.0, 0.0, -12.0), 2.0, 0.9, "aby_crystal")


# ------------------------------------------------------------------ Drachenschuppen

def _dragon(m: Model, e: str) -> None:
    scale, horn, glow = DRAGON[e]
    h = _slot(m, e, "head", "head")
    for side in (1, -1):
        sd = "left" if side > 0 else "right"
        if e == "fire":
            # geschwungen wie die Hörner der Drachen: erst hinauf, dann weit nach hinten; die Spitzen glühen
            _chain(m, h, f"fire_horn_{sd}", (3.2 * side, -8.7, -1.6),
                   [(2.2, 2.8, 2.2, (-24.0, 0.0, 22.0 * side)), (1.8, 2.8, 1.8, (-34.0, 0.0, -6.0 * side)),
                    (1.3, 2.6, 1.3, (-38.0, 0.0, -6.0 * side)), (0.8, 2.2, 0.8, (-34.0, 0.0, 0.0))], "dragon_horn_fire")
        elif e == "ice":
            # gerade Hörner aus Eiskristall, dazu ein kleiner Kristall daneben
            _chain(m, h, f"ice_horn_{sd}", (3.0 * side, -8.7, -1.0),
                   [(2.0, 3.6, 2.0, (-28.0, 0.0, 16.0 * side)), (1.2, 3.4, 1.2, (-6.0, 0.0, 0.0))], "dragon_horn_ice")
            _spike(m, h, f"ice_side_{sd}", (4.4 * side, -7.4, 0.6), (-36.0, 0.0, 40.0 * side), 2.4, 1.1, "dragon_horn_ice")
        elif e == "lightning":
            # gezackt wie ein Blitz, mit goldenen Spitzen
            _chain(m, h, f"light_horn_{sd}", (3.1 * side, -8.7, -1.2),
                   [(1.9, 2.4, 1.9, (-20.0, 0.0, 26.0 * side)), (1.6, 2.2, 1.6, (-50.0, 0.0, -18.0 * side)),
                    (1.3, 2.2, 1.3, (40.0, 0.0, 10.0 * side)), (1.0, 2.0, 1.0, (-56.0, 0.0, -6.0 * side)),
                    (0.7, 1.8, 0.7, (34.0, 0.0, 0.0))], "dragon_horn_lightning")
        elif e == "nature":
            # ein Geweih: Stange mit zwei Enden und Blättern
            beam = _chain(m, h, f"antler_{sd}", (3.0 * side, -8.7, -0.8),
                          [(1.4, 3.0, 1.4, (-14.0, 0.0, 30.0 * side)), (1.2, 3.0, 1.2, (-22.0, 0.0, -10.0 * side)),
                           (0.9, 2.6, 0.9, (-20.0, 0.0, -8.0 * side))], "antler")
            _chain(m, beam[0], f"antler_tine_{sd}_0", (0.0, -2.4, 0.0), [(0.8, 2.0, 0.8, (50.0, 0.0, 10.0 * side))], "antler")
            _chain(m, beam[1], f"antler_tine_{sd}_1", (0.0, -2.2, 0.0), [(0.7, 1.8, 0.7, (48.0, 0.0, -14.0 * side))], "antler")
            m.bone(f"antler_leaf_{sd}", beam[0], (0.6 * side, -1.4, 0.0), (0.0, 0.0, 50.0 * side))
            m.cube(f"antler_leaf_{sd}", (-0.15, -2.6, -0.9), (0.3, 2.6, 1.8), "leaf")
        elif e == "earth":
            # Widderhörner: dick, nach hinten und unten gerollt um die Schläfe
            _chain(m, h, f"ram_horn_{sd}", (5.0 * side, -8.0, -0.6),
                   [(2.8, 2.6, 2.8, (-24.0, 0.0, 28.0 * side)), (2.5, 2.6, 2.5, (-55.0, 0.0, 0.0)),
                    (2.2, 2.6, 2.2, (-55.0, 0.0, 0.0)), (1.9, 2.4, 1.9, (-55.0, 0.0, 0.0)),
                    (1.5, 2.2, 1.5, (-55.0, 0.0, 0.0)), (1.0, 1.8, 1.0, (-45.0, 0.0, 0.0))], "ram")
        else:
            # Wasser: Flossen an den Seiten des Kopfes, gefächert nach hinten
            _fan(m, h, f"head_fin_{sd}", (5.3 * side, -4.6, -1.0), (0.0, 14.0 * side, 0.0),
                 ((-6.0, 4.6, 1.6), (14.0, 5.4, 1.6), (34.0, 5.0, 1.5), (54.0, 3.8, 1.3)))
    # ein Kamm aus Schuppenstacheln über den Scheitel
    for k, (z, height) in enumerate(((-3.2, 1.6), (-0.6, 2.2), (2.0, 1.8))):
        crest = "leaf" if e == "nature" else "fin" if e == "water" else "stone" if e == "earth" else f"dragon_horn_{e}"
        _spike(m, h, f"{e}_crest_{k}", (0.0, -9.2, z), (-26.0, 0.0, 0.0), height, 1.0, crest)

    b = _slot(m, e, "chest", "body")
    if e == "water":
        # ein Flossensaum entlang des Rückgrats
        for k, (y, length) in enumerate(((0.6, 4.4), (3.4, 4.0), (6.2, 3.4), (9.0, 2.6))):
            _fan(m, b, f"water_back_fin_{k}", (0.0, y, 2.9), (0.0, 0.0, 0.0), ((-38.0, length, 2.2), (-8.0, length * 0.8, 2.0)))
    elif e == "nature":
        for k, (y, length) in enumerate(((1.0, 4.2), (4.0, 3.6), (7.0, 3.0))):
            m.bone(f"nature_frond_{k}", b, (0.0, y, 2.9), (-48.0, 0.0, 0.0))
            m.cube(f"nature_frond_{k}", (-1.1, -length, -0.15), (2.2, length, 0.3), "leaf")
    elif e == "earth":
        for k, (y, height) in enumerate(((0.8, 3.2), (4.0, 3.6), (7.4, 2.8))):
            m.bone(f"earth_plate_{k}", b, (0.0, y, 2.9), (-40.0, 0.0, 0.0))
            m.cube(f"earth_plate_{k}", (-0.4, -height, -1.2), (0.8, height, 2.4), "stone")
            m.cube(f"earth_plate_{k}", (-0.3, -height - 0.9, -0.6), (0.6, 1.0, 1.2), "stone")
    else:
        for k, (y, height) in enumerate(((0.8, 3.4), (3.6, 3.8), (6.6, 3.2), (9.4, 2.4))):
            _spike(m, b, f"{e}_back_{k}", (0.0, y, 2.9), (-50.0, 0.0, 0.0), height, 1.4, f"dragon_horn_{e}")
    for side, bone in ((1, "left_arm"), (-1, "right_arm")):
        a = _slot(m, e, "chest", bone)
        p = f"{e}_pauldron_{bone}"
        m.bone(p, a, (1.0 * side, -3.0, 0.0), (0.0, 0.0, 12.0 * side))
        m.cube(p, (_mx(-3.2, 6.8, side), -1.5, -3.6), (6.8, 1.7, 7.2), "dragon_scale_" + e)
        m.cube(p, (_mx(-2.0, 6.0, side), 0.2, -3.7), (6.0, 1.4, 7.4), "dragon_scale_" + e)
        claw = "leaf" if e == "nature" else "fin" if e == "water" else "stone" if e == "earth" else f"dragon_horn_{e}"
        _spike(m, p, f"{e}_claw_{bone}", (2.4 * side, -1.4, 0.0), (0.0, 0.0, 40.0 * side), 2.6, 1.3, claw)

    t = _slot(m, e, "legs", "body")
    tail = _chain(m, t, f"{e}_tail", (0.0, 10.2, 2.2),
                  [(3.0, 3.0, 3.0, (30.0, 0.0, 0.0)), (2.6, 3.0, 2.6, (10.0, 0.0, 0.0)), (2.2, 2.8, 2.2, (12.0, 0.0, 0.0)),
                   (1.7, 2.6, 1.7, (12.0, 0.0, 0.0)), (1.2, 2.2, 1.2, (10.0, 0.0, 0.0))],
                  "dragon_tail_" + e, kind="down")
    _tail_tip(m, tail[-1], e)
    for bone in ("left_leg", "right_leg"):
        g = _slot(m, e, "legs", bone)
        m.cube(g, (-2.0, 5.0, -3.2), (4.0, 2.6, 1.0), "dragon_scale_" + e)                 # Knieschuppe

    for bone in ("left_leg", "right_leg"):
        f = _slot(m, e, "feet", bone)
        for k, x in enumerate((-1.6, 0.0, 1.6)):
            name = f"{e}_talon_{bone}_{k}"
            m.bone(name, f, (x, 11.6, -2.8), (28.0, (x * 8.0), 0.0))
            m.cube(name, (-0.45, -0.45, -2.0), (0.9, 0.9, 2.0), f"talon_{e}")


def _tail_tip(m: Model, parent: str, e: str) -> None:
    """Die Spitze des Drachenschwanzes je Element."""
    name = f"{e}_tail_tip"
    m.bone(name, parent, (0.0, 2.4, 0.0), (8.0, 0.0, 0.0))
    if e == "fire":
        # eine Flamme aus Glut
        m.cube(name, (-1.2, 0.0, -0.7), (2.4, 2.4, 1.4), "dragon_flame")
        m.cube(name, (-0.7, 2.2, -0.4), (1.4, 2.0, 0.8), "dragon_flame")
        m.cube(name, (-0.3, 4.0, -0.2), (0.6, 1.2, 0.4), "dragon_flame")
    elif e == "ice":
        m.cube(name, (-0.9, 0.0, -0.9), (1.8, 2.4, 1.8), "dragon_horn_ice")
        m.cube(name, (-0.5, 2.2, -0.5), (1.0, 2.2, 1.0), "dragon_horn_ice")
    elif e == "lightning":
        # eine Klinge wie ein Blitz
        m.cube(name, (-0.3, 0.0, -1.6), (0.6, 2.2, 3.2), "dragon_horn_lightning")
        m.cube(name, (-0.25, 2.0, -0.6), (0.5, 2.0, 2.6), "dragon_horn_lightning")
    elif e == "nature":
        m.cube(name, (-0.15, 0.0, -1.4), (0.3, 4.2, 2.8), "leaf")
    elif e == "earth":
        # eine Keule aus Stein mit Bernstein
        m.cube(name, (-1.6, 0.0, -1.6), (3.2, 2.8, 3.2), "stone")
        m.cube(name, (-0.6, 0.6, -1.9), (1.2, 1.2, 0.5), "amber")
    else:
        _fan(m, name, "water_tail_fin", (0.0, 0.6, 0.0), (90.0, 0.0, 0.0), ((-40.0, 3.6, 1.6), (-10.0, 4.0, 1.6), (20.0, 3.6, 1.6)))


def _fan(m: Model, parent: str, name: str, pivot, rot, rays) -> None:
    """Eine Flosse als Fächer: dünne Platten (in der y-z-Ebene) vom Ansatz nach hinten, jede um ihren Winkel aufgestellt
    (rays: Winkel, Länge, Breite)."""
    m.bone(name, parent, pivot, rot)
    for k, (angle, length, width) in enumerate(rays):
        f = f"{name}_ray_{k}"
        m.bone(f, name, (0.0, 0.0, 0.0), (angle, 0.0, 0.0))
        m.cube(f, (-0.25, -width / 2, 0.0), (0.5, width, length), "fin")


# ------------------------------------------------------------------ Edelsteinkrone

def _crown(m: Model) -> None:
    h = _slot(m, "crown", "head", "head")
    # der Reif um den Helm, darüber Zacken (vorn die höchste), Edelsteine im Reif
    m.cube(h, (-5.4, -9.9, -5.4), (10.8, 1.8, 0.8), "crown_gold")
    m.cube(h, (-5.4, -9.9, 4.6), (10.8, 1.8, 0.8), "crown_gold")
    m.cube(h, (-5.4, -9.9, -4.6), (0.8, 1.8, 9.2), "crown_gold")
    m.cube(h, (4.6, -9.9, -4.6), (0.8, 1.8, 9.2), "crown_gold")
    points = [((0.0, -5.4), 3.2), ((-3.2, -5.4), 2.2), ((3.2, -5.4), 2.2), ((0.0, 5.4), 2.4), ((-3.2, 5.4), 1.8),
              ((3.2, 5.4), 1.8), ((-5.4, -1.8), 2.0), ((-5.4, 1.8), 2.0), ((5.4, -1.8), 2.0), ((5.4, 1.8), 2.0)]
    for k, ((x, z), height) in enumerate(points):
        _chain(m, h, f"crown_point_{k}", (x, -9.7, z), [(1.4, height * 0.6, 0.8 if abs(z) > 5 else 1.4, (0.0, 0.0, 0.0)),
                                                       (0.7, height * 0.5, 0.5 if abs(z) > 5 else 0.7, (0.0, 0.0, 0.0))], "crown_gold")
        m.cube(f"crown_point_{k}_1", (-0.5, -height * 0.5 - 0.8, -0.5), (1.0, 1.0, 1.0), "pearl")
    for (x, y, z, w, hh, d, gem) in ((-0.9, -9.6, -5.9, 1.8, 1.2, 0.6, "ruby"), (-4.0, -9.4, -5.8, 1.0, 0.8, 0.5, "sapphire"),
                                     (3.0, -9.4, -5.8, 1.0, 0.8, 0.5, "emerald"), (-5.9, -9.5, -0.6, 0.6, 1.0, 1.2, "amethyst"),
                                     (5.3, -9.5, -0.6, 0.6, 1.0, 1.2, "amethyst"), (-0.6, -9.5, 5.3, 1.2, 1.0, 0.6, "sapphire")):
        m.cube(h, (x, y, z), (w, hh, d), gem)


# ------------------------------------------------------------------ Mondstein (0.96)

def _crescent(m: Model, parent: str, name: str, pivot, rot, scale: float = 1.0) -> None:
    """Eine Mondsichel, aufrecht in der x-y-Ebene, die Spitzen nach oben: unten ein Bogen, zu beiden Seiten drei Glieder,
    die erst nach außen, dann hinauf und zur Spitze wieder einwärts streben (leuchtend)."""
    m.bone(name, parent, pivot, rot)
    k = scale
    m.cube(name, (-1.3 * k, -0.9 * k, -0.35), (2.6 * k, 0.9 * k, 0.7), "moon_crescent")
    for side in (1, -1):
        _chain(m, name, f"{name}_{'left' if side > 0 else 'right'}", (1.05 * k * side, -0.45 * k, 0.0),
               [(0.95 * k, 1.5 * k, 0.65, (0.0, 0.0, 58.0 * side)), (0.75 * k, 1.35 * k, 0.6, (0.0, 0.0, -40.0 * side)),
                (0.5 * k, 1.15 * k, 0.5, (0.0, 0.0, -36.0 * side))], "moon_crescent")


def _moonstone(m: Model) -> None:
    h = _slot(m, "moonstone", "head", "head")
    # der Stirnreif aus Mondsilber (rundum), vorn ein Mondstein, darüber die Mondsichel, an den Schläfen kleine Steine
    m.cube(h, (-5.45, -7.7, -5.45), (10.9, 1.0, 0.6), "silver")
    m.cube(h, (-5.45, -7.7, 4.85), (10.9, 1.0, 0.6), "silver")
    m.cube(h, (-5.45, -7.7, -4.85), (0.6, 1.0, 9.7), "silver")
    m.cube(h, (4.85, -7.7, -4.85), (0.6, 1.0, 9.7), "silver")
    m.cube(h, (-1.3, -8.5, -5.75), (2.6, 2.4, 0.4), "silver")                               # Fassung
    m.cube(h, (-0.95, -8.2, -6.15), (1.9, 1.8, 0.6), "moon_gem")
    _crescent(m, h, "moon_diadem", (0.0, -8.6, -5.75), (-8.0, 0.0, 0.0), 1.25)
    m.cube(h, (-5.95, -7.9, -0.7), (0.55, 1.4, 1.4), "moon_gem")
    m.cube(h, (5.4, -7.9, -0.7), (0.55, 1.4, 1.4), "moon_gem")

    b = _slot(m, "moonstone", "chest", "body")
    # der Sternenmantel, an zwei Mondsteinen auf den Schultern
    m.bone("moon_cape", b, (0.0, -0.2, 3.1), (5.0, 0.0, 0.0))
    m.cube("moon_cape", (-4.7, 0.0, 0.0), (9.4, 8.6, 0.6), "moon_cloth")
    m.bone("moon_cape_hem", "moon_cape", (0.0, 8.4, 0.05), (8.0, 0.0, 0.0))
    m.cube("moon_cape_hem", (-4.6, 0.0, 0.0), (9.2, 8.4, 0.6), "moon_cloth")
    for x in (-4.4, 2.8):
        m.cube(b, (x, -0.7, -3.6), (1.6, 1.6, 0.7), "moon_gem")
    # ein großer Mondstein auf der Brust in einer Fassung aus Mondsilber
    m.cube(b, (-1.7, 1.4, -3.45), (3.4, 3.6, 0.5), "silver")
    m.cube(b, (-1.15, 1.9, -3.85), (2.3, 2.6, 0.6), "moon_gem")
    for side, bone in ((1, "left_arm"), (-1, "right_arm")):
        a = _slot(m, "moonstone", "chest", bone)
        p = f"moon_pauldron_{bone}"
        m.bone(p, a, (1.0 * side, -3.0, 0.0), (0.0, 0.0, 12.0 * side))
        m.cube(p, (_mx(-3.1, 6.6, side), -1.5, -3.6), (6.6, 1.7, 7.2), "moon_plate")
        m.cube(p, (_mx(-2.0, 5.8, side), 0.2, -3.7), (5.8, 1.3, 7.4), "moon_plate")
        _crescent(m, p, f"moon_shoulder_{bone}", (1.4 * side, -1.4, 0.0), (0.0, 90.0 * side, 0.0), 1.0)
        # Armreif mit Mondstein
        m.cube(a, (_mx(-2.25, 6.5, side), 5.5, -3.25), (6.5, 2.3, 6.5), "moon_plate")
        m.cube(a, (_mx(0.4, 1.2, side), 6.0, -3.7), (1.2, 1.3, 0.5), "moon_gem")

    t = _slot(m, "moonstone", "legs", "body")
    # Gürtel mit Sichelschnalle
    _crescent(m, t, "moon_buckle", (0.0, 11.5, -3.3), (0.0, 0.0, 0.0), 0.75)
    m.cube(t, (-0.6, 10.6, -3.55), (1.2, 1.1, 0.5), "moon_gem")
    for side, bone in ((1, "left_leg"), (-1, "right_leg")):
        g = _slot(m, "moonstone", "legs", bone)
        # Robenschöße: vorn und außen, unten mit Silberborte
        m.bone(f"moon_robe_{bone}", g, (0.0, -0.6, -2.75), (-6.0, 0.0, 0.0))
        m.cube(f"moon_robe_{bone}", (-2.4, 0.0, -0.3), (4.8, 7.8, 0.5), "moon_cloth")
        m.bone(f"moon_robe_side_{bone}", g, (2.75 * side, -0.6, 0.0), (0.0, 0.0, -6.0 * side))
        m.cube(f"moon_robe_side_{bone}", (_mx(0.0, 0.5, side), 0.0, -2.4), (0.5, 7.8, 4.8), "moon_cloth")

    for side, bone in ((1, "left_leg"), (-1, "right_leg")):
        f = _slot(m, "moonstone", "feet", bone)
        # spitze, aufgebogene Schuhe der Zauberer
        _chain(m, f, f"moon_toe_{bone}", (0.0, 12.0, -2.3),
               [(3.4, 1.7, 1.4, (90.0, 0.0, 0.0)), (2.4, 1.5, 1.1, (-28.0, 0.0, 0.0)), (1.4, 1.3, 0.8, (-42.0, 0.0, 0.0)),
                (0.7, 1.0, 0.6, (-46.0, 0.0, 0.0))], "moon_plate")
        _crescent(m, f, f"moon_ankle_{bone}", (3.35 * side, 9.8, 0.4), (0.0, 90.0 * side, 0.0), 0.62)


# ------------------------------------------------------------------ Dämmerquarz (0.98): die Rüstung der Schleicher

def _quartz_points(m: Model, parent: str, name: str, pivot, rot, scale: float = 1.0) -> None:
    """Eine kleine Druse aus zwei Quarzspitzen (Schließe, Niete)."""
    _spike(m, parent, f"{name}_a", pivot, rot, 2.2 * scale, 1.0 * scale, "dusk_gem")
    _spike(m, parent, f"{name}_b", (pivot[0] + 0.7 * scale, pivot[1] + 0.2, pivot[2]), (rot[0], rot[1], rot[2] - 38.0),
           1.5 * scale, 0.8 * scale, "dusk_gem")


def _dusk(m: Model) -> None:
    h = _slot(m, "dusk", "head", "head")
    # die Kapuze: oben, an den Seiten und hinten etwas weiter als der Kopf, vorn offen; darüber ein Saum um das Gesicht
    m.cube(h, (-5.5, -9.7, -5.3), (11.0, 1.1, 10.8), "dusk_cloth")
    for side in (1, -1):
        m.cube(h, (_mx(5.0, 0.55, side), -8.8, -5.3), (0.55, 8.4, 10.8), "dusk_cloth")
    m.cube(h, (-5.0, -8.8, 5.0), (10.0, 8.6, 0.55), "dusk_cloth")
    m.cube(h, (-5.55, -9.2, -5.85), (11.1, 1.5, 0.6), "dusk_hood_rim")       # der Saum oben über der Stirn
    for side in (1, -1):
        m.cube(h, (_mx(4.3, 1.25, side), -8.0, -5.85), (1.25, 6.4, 0.6), "dusk_hood_rim")   # die Seiten des Saums
    # die Halbmaske über Mund und Nase (Stoff, in der Mitte eine Falte)
    m.cube(h, (-3.3, -3.6, -5.55), (6.6, 2.8, 0.55), "dusk_mask")
    m.cube(h, (-0.45, -3.75, -5.75), (0.9, 3.0, 0.35), "dusk_mask")
    # der Zipfel: fällt vom Hinterkopf über den Nacken
    _chain(m, h, "dusk_hood_tip", (0.0, -8.4, 5.3),
           [(4.6, 2.6, 1.0, (62.0, 0.0, 0.0)), (3.4, 2.8, 0.9, (-30.0, 0.0, 0.0)), (2.4, 2.8, 0.8, (-22.0, 0.0, 0.0)),
            (1.4, 2.4, 0.7, (-12.0, 0.0, 0.0))], "dusk_cloth", kind="down")

    b = _slot(m, "dusk", "chest", "body")
    # der Schulterumhang: über die Schultern gelegt, hinten bis zur Taille, unten zerschlissen
    m.cube(b, (-5.4, -0.55, -3.55), (10.8, 1.1, 7.1), "dusk_cloth")
    m.bone("dusk_cape", b, (0.0, 0.2, 3.25), (6.0, 0.0, 0.0))
    m.cube("dusk_cape", (-5.2, 0.0, 0.0), (10.4, 6.8, 0.6), "dusk_cape")
    for i, x in enumerate((-5.2, -2.7, -0.2, 2.3)):
        m.cube("dusk_cape", (x, 6.8, 0.02), (2.2, 1.2 + (0.9 if i % 2 == 0 else 0.0), 0.56), "dusk_cape")
    # die Schließe vorn an der linken Schulter: zwei Quarzspitzen in einer Lederfassung
    m.cube(b, (2.0, 0.2, -3.75), (2.2, 2.0, 0.5), "dusk_leather")
    _quartz_points(m, b, "dusk_clasp", (3.0, 1.8, -4.0), (-12.0, 0.0, 14.0), 0.9)
    # der Riemen schräg über die Brust (über der flachen Rüstung)
    for i in range(5):
        m.cube(b, (-3.9 + i * 1.55, 2.4 + i * 1.6, -3.45), (1.7, 1.2, 0.35), "dusk_leather")
    for side, bone in ((1, "left_arm"), (-1, "right_arm")):
        a = _slot(m, "dusk", "chest", bone)
        # Lederwickel am Oberarm und am Handgelenk
        m.cube(a, (_mx(-2.35, 6.7, side), 1.2, -3.35), (6.7, 1.3, 6.7), "dusk_wrap")
        m.cube(a, (_mx(-2.35, 6.7, side), 2.9, -3.35), (6.7, 1.0, 6.7), "dusk_wrap")
        m.cube(a, (_mx(-2.3, 6.6, side), 7.6, -3.3), (6.6, 1.6, 6.6), "dusk_wrap")

    t = _slot(m, "dusk", "legs", "body")
    # der Gürtel mit zwei Taschen an der rechten Hüfte und einem Dolch quer im Rücken
    m.cube(t, (-4.4, 10.2, -3.45), (2.2, 2.4, 1.0), "dusk_leather")
    m.cube(t, (-4.6, 10.1, -1.0), (0.9, 2.2, 2.0), "dusk_leather")
    m.cube(t, (-4.3, 9.9, -3.6), (1.6, 0.6, 1.2), "dusk_leather")
    m.cube(t, (-0.7, 10.4, -3.6), (1.4, 1.4, 0.45), "dusk_buckle")
    m.bone("dusk_dagger", t, (0.0, 11.0, 3.3), (0.0, 0.0, 74.0))
    m.cube("dusk_dagger", (-0.45, -2.5, 0.0), (0.9, 3.0, 0.7), "dusk_leather")        # die Scheide
    m.cube("dusk_dagger", (-0.7, -3.0, -0.05), (1.4, 0.5, 0.8), "dusk_buckle")         # die Parierstange
    m.cube("dusk_dagger", (-0.3, -4.6, 0.05), (0.6, 1.6, 0.6), "dusk_leather")         # der Griff
    m.cube("dusk_dagger", (-0.4, -5.1, 0.0), (0.8, 0.6, 0.7), "dusk_gem")              # ein Quarz als Knauf
    for side, bone in ((1, "left_leg"), (-1, "right_leg")):
        g = _slot(m, "dusk", "legs", bone)
        # Knieschutz aus Leder, darauf eine Quarzniete
        m.cube(g, (-2.35, 3.4, -3.05), (4.7, 2.8, 0.6), "dusk_leather")
        m.cube(g, (-0.55, 4.25, -3.4), (1.1, 1.1, 0.45), "dusk_gem")
        m.cube(g, (-2.4, 7.0, -2.4), (4.8, 0.9, 4.8), "dusk_wrap")

    for side, bone in ((1, "left_leg"), (-1, "right_leg")):
        f = _slot(m, "dusk", "feet", bone)
        # der umgeschlagene Schaft der weichen Stiefel und zwei Wickelriemen
        m.cube(f, (-2.55, 6.4, -2.55), (5.1, 1.5, 5.1), "dusk_cloth")
        m.cube(f, (-2.45, 8.6, -2.45), (4.9, 0.8, 4.9), "dusk_wrap")
        m.cube(f, (-2.45, 10.0, -2.45), (4.9, 0.8, 4.9), "dusk_wrap")
        m.cube(f, (_mx(2.4, 0.4, side), 8.4, -0.6), (0.4, 1.2, 1.2), "dusk_buckle")


# ------------------------------------------------------------------ Aquamarin (0.97): die Taucherrüstung

#: das große Sichtfenster vorn im Taucherhelm (Knochenraum des Kopfes): x, y von – bis
VISOR = (-2.4, 2.4, -7.3, -2.1)
#: so weit liegt die Vorderseite der Helmschale vor dem Kopf
DOME = 5.7


def _aquamarine(m: Model) -> None:
    h = _slot(m, "aquamarine", "head", "head")
    # die Schale: ein runder Helm aus Stufen (die Vorderseite der großen Stufe hat das Sichtfenster), seitlich gewölbt
    m.cube(h, (-DOME, -8.6, -DOME), (2 * DOME, 8.0, 2 * DOME), {"front": "aqua_visor", "*": "aqua_shell"})
    m.cube(h, (-5.0, -9.6, -5.0), (10.0, 1.0, 10.0), "aqua_shell")
    m.cube(h, (-3.7, -10.4, -3.7), (7.4, 0.8, 7.4), "aqua_shell")
    for side in (1, -1):
        m.cube(h, (_mx(DOME, 0.5, side), -7.8, -4.4), (0.5, 6.4, 8.8), "aqua_shell")
    m.cube(h, (-4.4, -7.8, DOME), (8.8, 6.4, 0.5), "aqua_shell")
    # oben das Ventil mit Griff
    m.cube(h, (-0.8, -11.4, -0.8), (1.6, 1.0, 1.6), "aqua_brass")
    m.cube(h, (-1.9, -11.9, -0.35), (3.8, 0.5, 0.7), "aqua_brass")
    # der Messingkragen mit Schrauben
    m.cube(h, (-6.1, -1.0, -6.1), (12.2, 1.3, 12.2), "aqua_collar")
    # der Rahmen des Sichtfensters (vorstehend, mit abgerundeten Ecken)
    x0, x1, y0, y1 = VISOR
    m.cube(h, (x0, y0 - 0.8, -DOME - 0.8), (x1 - x0, 0.8, 0.8), "aqua_brass")
    m.cube(h, (x0, y1, -DOME - 0.8), (x1 - x0, 0.8, 0.8), "aqua_brass")
    m.cube(h, (x0 - 0.8, y0, -DOME - 0.8), (0.8, y1 - y0, 0.8), "aqua_brass")
    m.cube(h, (x1, y0, -DOME - 0.8), (0.8, y1 - y0, 0.8), "aqua_brass")
    for cx, cy in ((x0, y0), (x1 - 0.7, y0), (x0, y1 - 0.7), (x1 - 0.7, y1 - 0.7)):
        m.cube(h, (cx, cy, -DOME - 0.55), (0.7, 0.7, 0.55), "aqua_brass")
    # die Bullaugen an den Seiten
    for side in (1, -1):
        wx = DOME + 0.5
        m.cube(h, (_mx(wx, 0.3, side), -6.3, -1.5), (0.3, 3.0, 3.0), "aqua_glass")
        m.cube(h, (_mx(wx, 0.6, side), -6.9, -2.1), (0.6, 0.6, 4.2), "aqua_brass")
        m.cube(h, (_mx(wx, 0.6, side), -3.3, -2.1), (0.6, 0.6, 4.2), "aqua_brass")
        m.cube(h, (_mx(wx, 0.6, side), -6.3, -2.1), (0.6, 3.0, 0.6), "aqua_brass")
        m.cube(h, (_mx(wx, 0.6, side), -6.3, 1.5), (0.6, 3.0, 0.6), "aqua_brass")
    # hinten der Anschluss des Luftschlauchs, der Schlauch hängt hinab zu den Flaschen
    m.cube(h, (0.9, -3.4, DOME + 0.5), (1.6, 1.6, 0.9), "aqua_brass")
    m.cube(h, (1.15, -2.0, DOME + 0.75), (1.1, 2.6, 1.1), "aqua_hose")

    b = _slot(m, "aquamarine", "chest", "body")
    # zwei Luftflaschen auf dem Rücken, gefasst von zwei Messingbändern; oben die Ventile mit Handrad
    for side in (1, -1):
        tx = _mx(0.6, 3.0, side)
        m.cube(b, (tx, 1.2, 3.2), (3.0, 8.0, 2.8), "aqua_tank")
        m.cube(b, (tx + 0.3, 0.6, 3.5), (2.4, 0.6, 2.2), "aqua_tank")
        m.cube(b, (tx + 0.3, 9.2, 3.5), (2.4, 0.5, 2.2), "aqua_tank")
        m.cube(b, (tx + 1.0, -0.1, 4.1), (1.0, 0.7, 1.0), "aqua_brass")
        m.cube(b, (_mx(3.4, 0.6, side), 0.0, 4.15), (0.6, 0.6, 0.9), "aqua_brass")
    for y in (2.2, 7.2):
        m.cube(b, (-3.8, y, 3.0), (7.6, 0.7, 3.2), "aqua_brass")
    # der Schlauch vom rechten Ventil nach hinten oben (dort hängt der Schlauch des Helms)
    m.cube(b, (1.55, -0.6, 5.0), (1.1, 1.0, 1.8), "aqua_hose")
    # die Druckanzeige auf der Brust
    m.cube(b, (1.1, 1.6, -3.75), (3.0, 3.0, 0.6), {"front": "aqua_dial", "*": "aqua_brass"})
    for side, bone in ((1, "left_arm"), (-1, "right_arm")):
        a = _slot(m, "aquamarine", "chest", bone)
        p = f"aqua_pauldron_{bone}"
        m.bone(p, a, (1.0 * side, -3.0, 0.0), (0.0, 0.0, 10.0 * side))
        m.cube(p, (_mx(-2.6, 6.0, side), -1.3, -3.4), (6.0, 1.6, 6.8), "aqua_plate")
        m.cube(p, (_mx(-1.6, 5.0, side), 0.3, -3.5), (5.0, 1.0, 7.0), "aqua_plate")
        # Armreif mit Aquamarin
        m.cube(a, (_mx(-2.25, 6.5, side), 6.2, -3.25), (6.5, 1.5, 6.5), "aqua_brass")
        m.cube(a, (_mx(0.4, 1.2, side), 6.35, -3.7), (1.2, 1.2, 0.5), "aqua_gem")

    t = _slot(m, "aquamarine", "legs", "body")
    # der Bleigürtel: vier Bleistücke, vorn die Messingschnalle
    m.cube(t, (-4.6, 10.3, -3.55), (2.4, 1.9, 0.75), "aqua_lead")
    m.cube(t, (2.2, 10.3, -3.55), (2.4, 1.9, 0.75), "aqua_lead")
    for side in (1, -1):
        m.cube(t, (_mx(4.6, 0.75, side), 10.3, -1.3), (0.75, 1.9, 2.6), "aqua_lead")
    m.cube(t, (-1.1, 10.5, -3.6), (2.2, 1.5, 0.6), "aqua_brass")
    for side, bone in ((1, "left_leg"), (-1, "right_leg")):
        g = _slot(m, "aquamarine", "legs", bone)
        # Knieplatte mit Messingsaum und einem Aquamarin
        m.cube(g, (-2.3, 3.6, -3.05), (4.6, 2.6, 0.6), "aqua_plate")
        m.cube(g, (-0.55, 4.35, -3.4), (1.1, 1.1, 0.4), "aqua_gem")

    for side, bone in ((1, "left_leg"), (-1, "right_leg")):
        f = _slot(m, "aquamarine", "feet", bone)
        # ein Riemen um den Schuh, außen die Schnalle
        m.cube(f, (-2.55, 9.3, -2.55), (5.1, 0.9, 5.1), "aqua_strap")
        m.cube(f, (_mx(2.55, 0.45, side), 9.15, -0.7), (0.45, 1.2, 1.4), "aqua_brass")
        # die Flosse: breit, nach vorn, zur Spitze leicht aufgebogen und breiter
        _chain(m, f, f"aqua_fin_{bone}", (0.0, 11.75, -2.0),
               [(4.6, 3.4, 0.45, (90.0, 0.0, 0.0)), (5.4, 3.2, 0.4, (-5.0, 0.0, 0.0)), (6.0, 2.6, 0.35, (-9.0, 0.0, 0.0))],
               "aqua_fin", overlap=0.15)


# ------------------------------------------------------------------ die Rüstungen von Minecraft

def _leather(m: Model) -> None:
    """Leder (der Jäger): eine Feder am Band der Kappe, ein Pelzkragen, ein Gürtel mit Tasche, Pelzstulpen."""
    h = _slot(m, "leather", "head", "head")
    m.bone("leather_feather", h, (5.0, -6.4, 1.4), (-30.0, 0.0, 16.0))
    m.cube("leather_feather", (-0.3, -8.2, -1.1), (0.6, 8.2, 2.2), "quill")
    m.cube(h, (4.6, -7.4, 0.2), (1.2, 1.8, 2.4), "leather_strap")
    b = _slot(m, "leather", "chest", "body")
    m.cube(b, (-5.6, -1.8, -3.7), (11.2, 2.8, 7.4), "fur", inflate=0.15)              # Pelzkragen über den Schultern
    for side, bone in ((1, "left_arm"), (-1, "right_arm")):
        a = _slot(m, "leather", "chest", bone)
        m.cube(a, (_mx(-2.4, 6.9, side), -4.0, -3.5), (6.9, 2.2, 7.0), "fur", inflate=0.1)
    g = _slot(m, "leather", "legs", "body")
    m.cube(g, (-4.9, 10.6, -2.9), (9.8, 1.2, 5.8), "leather_strap")                     # Gürtel
    m.cube(g, (2.4, 10.9, -3.6), (2.4, 2.6, 1.2), "leather_strap")                      # Tasche
    m.cube(g, (-4.2, 10.4, -3.3), (1.4, 1.6, 0.6), "gold")                              # Schnalle
    _slot(m, "leather", "legs", "left_leg")
    _slot(m, "leather", "legs", "right_leg")
    for bone in ("left_leg", "right_leg"):
        f = _slot(m, "leather", "feet", bone)
        m.cube(f, (-3.6, 6.6, -3.6), (7.2, 3.0, 7.2), "fur")


def _chainmail(m: Model) -> None:
    """Kettenhemd: ein Kettenkragen am Helm (über Nacken und Schultern), Kettenschultern, ein Kettenschurz."""
    h = _slot(m, "chainmail", "head", "head")
    m.cube(h, (-5.3, -3.6, 4.8), (10.6, 5.4, 0.6), "mail")                              # hinten
    m.cube(h, (-5.6, -3.6, -4.4), (0.6, 5.4, 9.4), "mail")                              # Seiten
    m.cube(h, (5.0, -3.6, -4.4), (0.6, 5.4, 9.4), "mail")
    m.cube(h, (-0.5, -6.0, -5.7), (1.0, 4.0, 0.7), "iron_plate")                         # Nasenschutz
    _slot(m, "chainmail", "chest", "body")
    for side, bone in ((1, "left_arm"), (-1, "right_arm")):
        a = _slot(m, "chainmail", "chest", bone)
        m.cube(a, (_mx(-2.2, 6.6, side), -3.7, -3.5), (6.6, 1.0, 7.0), "mail")
        m.cube(a, (_mx(3.9, 0.6, side), -3.4, -3.5), (0.6, 4.6, 7.0), "mail")           # hängt über die Schulter
    g = _slot(m, "chainmail", "legs", "body")
    m.cube(g, (-4.8, 10.0, -3.0), (9.6, 4.6, 0.6), "mail")                              # Schurz vorn
    m.cube(g, (-4.8, 10.0, 2.4), (9.6, 4.6, 0.6), "mail")                               # hinten
    _slot(m, "chainmail", "legs", "left_leg")
    _slot(m, "chainmail", "legs", "right_leg")


def _iron(m: Model) -> None:
    """Eisen: ein Nasenschutz und ein Grat über den Helm, runde Schulterstücke aus zwei Platten, Kniebuckel, Schuhkappen."""
    h = _slot(m, "iron", "head", "head")
    m.cube(h, (-0.6, -6.4, -5.8), (1.2, 4.4, 0.8), "iron_plate")
    m.cube(h, (-0.6, -10.0, -4.4), (1.2, 1.1, 8.8), "iron_plate")
    _slot(m, "iron", "chest", "body")
    for side, bone in ((1, "left_arm"), (-1, "right_arm")):
        a = _slot(m, "iron", "chest", bone)
        p = f"iron_pauldron_{bone}"
        m.bone(p, a, (1.0 * side, -3.0, 0.0), (0.0, 0.0, 12.0 * side))
        m.cube(p, (_mx(-3.0, 6.6, side), -1.4, -3.5), (6.6, 1.8, 7.0), "iron_plate")
        m.cube(p, (_mx(-1.8, 5.8, side), 0.4, -3.6), (5.8, 1.6, 7.2), "iron_plate")
    _slot(m, "iron", "legs", "body")
    for bone in ("left_leg", "right_leg"):
        g = _slot(m, "iron", "legs", bone)
        m.cube(g, (-1.9, 5.1, -3.2), (3.8, 2.8, 1.0), "iron_plate")
    for bone in ("left_leg", "right_leg"):
        f = _slot(m, "iron", "feet", bone)
        m.cube(f, (-2.4, 10.6, -4.0), (4.8, 2.3, 1.3), "iron_plate")


def _gold_set(m: Model) -> None:
    """Gold (prächtig): ein Kamm aus drei goldenen Strahlen und ein Rubin an der Stirn, Schulterstücke mit Sonnenstein,
    eine Sonnenscheibe auf der Brust, Beintaschen, Schuhkappen."""
    h = _slot(m, "gold", "head", "head")
    for k, (ang, length) in enumerate(((-30.0, 3.4), (0.0, 4.6), (30.0, 3.4))):
        _spike(m, h, f"gold_ray_{k}", (0.0, -9.2, -1.0), (ang * 0.4 - 10.0, 0.0, ang), length, 1.3, "gold_plate")
    m.cube(h, (-1.0, -7.2, -5.7), (2.0, 2.0, 0.7), "ruby")
    b = _slot(m, "gold", "chest", "body")
    m.cube(b, (-1.8, 2.4, -3.7), (3.6, 3.6, 0.7), "gold_plate")                          # Sonnenscheibe
    m.cube(b, (-0.7, 3.5, -4.1), (1.4, 1.4, 0.5), "ruby")
    for side, bone in ((1, "left_arm"), (-1, "right_arm")):
        a = _slot(m, "gold", "chest", bone)
        p = f"gold_pauldron_{bone}"
        m.bone(p, a, (1.0 * side, -3.0, 0.0), (0.0, 0.0, 14.0 * side))
        m.cube(p, (_mx(-3.2, 7.0, side), -1.6, -3.6), (7.0, 2.0, 7.2), "gold_plate")
        m.cube(p, (_mx(-2.0, 6.2, side), 0.3, -3.7), (6.2, 1.6, 7.4), "gold_plate")
        m.cube(p, (_mx(1.4, 1.4, side), -2.2, -0.7), (1.4, 0.8, 1.4), "ruby")
    _slot(m, "gold", "legs", "body")
    for bone in ("left_leg", "right_leg"):
        g = _slot(m, "gold", "legs", bone)
        m.cube(g, (-2.7, -0.6, -3.3), (5.4, 4.4, 1.0), "gold_plate")
        m.cube(g, (-1.9, 5.1, -3.2), (3.8, 2.8, 1.0), "gold_plate")
    for bone in ("left_leg", "right_leg"):
        f = _slot(m, "gold", "feet", bone)
        m.cube(f, (-2.4, 10.6, -4.0), (4.8, 2.3, 1.3), "gold_plate")


def _diamond(m: Model) -> None:
    """Diamant: ein Kamm aus Diamantsplittern über den Scheitel, Kristalle auf den Schultern, ein Diamant auf der Brust,
    Diamanten an Knien und Fersen."""
    h = _slot(m, "diamond", "head", "head")
    for k, (z, height) in enumerate(((-3.0, 2.6), (-0.4, 3.6), (2.2, 2.8))):
        _spike(m, h, f"diamond_crest_{k}", (0.0, -9.2, z), (-20.0, 0.0, 0.0), height, 1.6, "diamond_crystal")
    b = _slot(m, "diamond", "chest", "body")
    m.cube(b, (-1.0, 2.0, -3.8), (2.0, 2.6, 0.8), "diamond_gem")
    for side, bone in ((1, "left_arm"), (-1, "right_arm")):
        a = _slot(m, "diamond", "chest", bone)
        p = f"diamond_pauldron_{bone}"
        m.bone(p, a, (1.0 * side, -3.0, 0.0), (0.0, 0.0, 10.0 * side))
        m.cube(p, (_mx(-3.0, 6.6, side), -1.4, -3.5), (6.6, 1.6, 7.0), "diamond_plate")
        _spike(m, p, f"diamond_shoulder_{bone}_0", (1.8 * side, -1.3, -1.0), (8.0, 0.0, 24.0 * side), 3.2, 1.5, "diamond_crystal")
        _spike(m, p, f"diamond_shoulder_{bone}_1", (2.6 * side, -1.3, 1.6), (-10.0, 0.0, 38.0 * side), 2.2, 1.1, "diamond_crystal")
    _slot(m, "diamond", "legs", "body")
    for bone in ("left_leg", "right_leg"):
        g = _slot(m, "diamond", "legs", bone)
        m.cube(g, (-1.0, 5.4, -3.4), (2.0, 2.0, 0.8), "diamond_gem")
    for bone in ("left_leg", "right_leg"):
        f = _slot(m, "diamond", "feet", bone)
        _spike(m, f, f"diamond_heel_{bone}", (0.0, 10.9, 2.8), (-56.0, 0.0, 0.0), 2.2, 1.2, "diamond_crystal")


def _netherite(m: Model) -> None:
    """Netherit (schwer): kurze, nach vorn gebogene Hörner, schwere Schulterstücke mit Nieten und zwei Dornen, Schenkel-
    platten, Schuhkappen mit Dorn."""
    h = _slot(m, "netherite", "head", "head")
    for side in (1, -1):
        _chain(m, h, f"netherite_horn_{'left' if side > 0 else 'right'}", (4.4 * side, -7.6, -2.6),
               [(2.0, 2.0, 2.0, (0.0, 0.0, 70.0 * side)), (1.6, 2.0, 1.6, (30.0, 0.0, -30.0 * side)),
                (1.1, 1.8, 1.1, (34.0, 0.0, -24.0 * side))], "netherite_horn")
    m.cube(h, (-4.6, -6.2, -5.7), (9.2, 0.8, 0.7), "netherite_plate")                   # Sehschlitz-Leiste
    _slot(m, "netherite", "chest", "body")
    for side, bone in ((1, "left_arm"), (-1, "right_arm")):
        a = _slot(m, "netherite", "chest", bone)
        p = f"netherite_pauldron_{bone}"
        m.bone(p, a, (1.0 * side, -3.0, 0.0), (0.0, 0.0, 15.0 * side))
        m.cube(p, (_mx(-3.4, 7.4, side), -1.9, -3.8), (7.4, 2.3, 7.6), "netherite_plate")
        m.cube(p, (_mx(-2.2, 6.6, side), 0.4, -3.9), (6.6, 1.9, 7.8), "netherite_plate")
        for k, z in enumerate((-1.6, 1.6)):
            _spike(m, p, f"netherite_spike_{bone}_{k}", (2.0 * side, -1.8, z), (0.0, 0.0, 30.0 * side), 2.4, 1.2, "netherite_horn")
    _slot(m, "netherite", "legs", "body")
    for bone in ("left_leg", "right_leg"):
        g = _slot(m, "netherite", "legs", bone)
        m.cube(g, (-2.7, -0.6, -3.3), (5.4, 4.4, 1.0), "netherite_plate")
        m.cube(g, (-1.9, 5.1, -3.2), (3.8, 2.8, 1.0), "netherite_plate")
    for bone in ("left_leg", "right_leg"):
        f = _slot(m, "netherite", "feet", bone)
        m.cube(f, (-2.5, 10.4, -4.2), (5.0, 2.5, 1.5), "netherite_plate")
        _spike(m, f, f"netherite_toe_{bone}", (0.0, 11.2, -4.0), (62.0, 0.0, 0.0), 1.6, 1.0, "netherite_horn")


def _copper(m: Model) -> None:
    """Kupfer: eine Helmspitze, runde Schulterplatten mit Nieten (mit Grünspan), Knieplatten, Schuhkappen."""
    h = _slot(m, "copper", "head", "head")
    m.cube(h, (-1.6, -10.0, -1.6), (3.2, 1.2, 3.2), "copper_plate")
    _chain(m, h, "copper_spike", (0.0, -9.8, 0.0), [(1.6, 2.0, 1.6, (0.0, 0.0, 0.0)), (0.8, 2.0, 0.8, (0.0, 0.0, 0.0))], "copper_plate")
    _slot(m, "copper", "chest", "body")
    for side, bone in ((1, "left_arm"), (-1, "right_arm")):
        a = _slot(m, "copper", "chest", bone)
        p = f"copper_pauldron_{bone}"
        m.bone(p, a, (1.0 * side, -3.0, 0.0), (0.0, 0.0, 12.0 * side))
        m.cube(p, (_mx(-3.0, 6.6, side), -1.5, -3.5), (6.6, 1.9, 7.0), "copper_plate")
    _slot(m, "copper", "legs", "body")
    for bone in ("left_leg", "right_leg"):
        g = _slot(m, "copper", "legs", bone)
        m.cube(g, (-1.9, 5.1, -3.2), (3.8, 2.8, 1.0), "copper_plate")
    for bone in ("left_leg", "right_leg"):
        f = _slot(m, "copper", "feet", bone)
        m.cube(f, (-2.4, 10.6, -4.0), (4.8, 2.3, 1.3), "copper_plate")


def _turtle(m: Model) -> None:
    """Schildkrötenpanzer: ein gewölbter Panzer aus Schilden über dem Kopf."""
    h = _slot(m, "turtle", "head", "head")
    m.cube(h, (-5.6, -9.9, -5.6), (11.2, 1.0, 11.2), "turtle_shell")
    m.cube(h, (-4.4, -10.9, -4.4), (8.8, 1.0, 8.8), "turtle_shell")
    m.cube(h, (-2.8, -11.6, -2.8), (5.6, 0.8, 5.6), "turtle_shell")


def build() -> Model:
    m = Model("armor_parts", 256, 320)
    m.tex_res = 2
    _steel(m)
    _konterit(m)
    _nightsteel(m)
    _abyssite(m)
    for e in DRAGON:
        _dragon(m, e)
    _crown(m)
    _leather(m)
    _chainmail(m)
    _iron(m)
    _gold_set(m)
    _diamond(m)
    _netherite(m)
    _copper(m)
    _turtle(m)
    _moonstone(m)
    _aquamarine(m)
    _dusk(m)
    # Plätze ohne eigene Teile bekommen leere Knochen, damit Java jeden Namen findet
    for s in SETS:
        for slot, bones in SLOTS.items():
            for bone in bones:
                name = f"{s}_{slot}_{bone}"
                if name not in m.bones:
                    m.bone(name, "root", (0.0, 0.0, 0.0))
    return m


# =============================================================================================
# Bemalen
# =============================================================================================

def _seg_t(ctx: Ctx) -> float:
    """0 am Ansatz, 1 an der Spitze – über alle Glieder einer Kette (Tag up:a:b oder down:a:b)."""
    tag = ctx.cube.tag or ""
    parts = tag.split(":")
    if len(parts) != 3:
        return along(ctx)
    kind, a, b = parts[0], float(parts[1]), float(parts[2])
    oy, hgt = ctx.cube.origin[1], ctx.cube.size[1]
    ly = ctx.local[1]
    tl = (oy + hgt - ly) / hgt if kind == "up" else (ly - oy) / hgt
    return a + (b - a) * max(0.0, min(1.0, tl))


def _noise(ctx: Ctx, seed: int = 0) -> float:
    return hash01(ctx.u, ctx.v, seed)


def _metal(key: str, rim: str | None = None, rim_from: float = 0.78, rivets: bool = False):
    """Blankes Metall: oben hell, unten dunkel, helle Kanten, feines Korn; mit rim ein Saum (Gold) an der Unterkante."""
    def paint(ctx: Ctx):
        ry = rel_y(ctx)
        v = 0.66 - 0.34 * ry
        if ctx.face == "top":
            v = 0.8
        elif ctx.face == "bottom":
            v = 0.28
        if ctx.s == 0 or ctx.t == 0:
            v += 0.12
        if ctx.s == ctx.w - 1 or ctx.t == ctx.h - 1:
            v -= 0.12
        v += (_noise(ctx, 3) - 0.5) * 0.08
        col = pick(ctx.pal[key], v)
        if rim and ctx.face not in ("top", "bottom") and ry >= rim_from:
            col = pick(ctx.pal[rim], 0.5 + 0.35 * (1.0 - ry) + (_noise(ctx, 5) - 0.5) * 0.1)
        if rivets and ctx.face == "front" and ctx.t == 2 and ctx.s % 6 == 3:
            col = pick(ctx.pal["gold"], 0.85)
        return col
    return paint


def _gold(key: str = "gold"):
    def paint(ctx: Ctx):
        ry = rel_y(ctx)
        v = 0.72 - 0.3 * ry + (0.14 if ctx.face == "top" else 0.0) - (0.25 if ctx.face == "bottom" else 0.0)
        if (ctx.s + ctx.t) % 5 == 0:
            v += 0.12
        return pick(ctx.pal[key], v + (_noise(ctx, 7) - 0.5) * 0.08)
    return paint


def _spur(ctx: Ctx):
    """Das Rädchen des Sporns: ein Stern aus Gold."""
    if ctx.face in ("front", "back"):
        cx, cy = (ctx.w - 1) / 2, (ctx.h - 1) / 2
        dx, dy = ctx.s - cx, ctx.t - cy
        if abs(dx) + abs(dy) > max(cx, cy) + 0.6 and not (abs(dx) < 0.6 or abs(dy) < 0.6):
            return None
    return pick(ctx.pal["gold"], 0.6 + (_noise(ctx, 9) - 0.5) * 0.2)


def _plume(ctx: Ctx):
    """Helmbusch aus Rosshaar: Strähnen entlang des Buschs, zur Spitze heller und lockerer."""
    t = _seg_t(ctx)
    strand = hash01(int(ctx.local[0] * 4 + ctx.local[2] * 3), 11)
    v = 0.35 + 0.35 * strand + 0.2 * (1.0 - abs(rel_y(ctx) - 0.5) * 2)
    if t > 0.85 and _noise(ctx, 13) > 0.6:
        return None                                       # ausgefranste Spitze
    return pick(ctx.pal["plume"], v + 0.1 * t)


def _sabaton(ctx: Ctx):
    """Schuhspitze aus geschobenen Platten (Querstreifen)."""
    base = _metal("steel")(ctx)
    if ctx.face in ("top", "west", "east") and int(ctx.local[2] * 2) % 2 == 0:
        return mix(base, (40, 44, 52, 255), 0.25)
    return base


def _feather(ctx: Ctx):
    """Feder: Kiel in der Mitte, zur Spitze (hinten) weiß, Fahnen leicht schräg gestreift."""
    oz, d = ctx.cube.origin[2], ctx.cube.size[2]
    tz = (ctx.local[2] - oz) / d if d > 0 else 0.0
    ry = rel_y(ctx)
    v = 0.35 + 0.55 * tz + (0.08 if int((ctx.local[2] - ctx.local[1]) * 2) % 2 else 0.0)
    if abs(ry - 0.5) < 0.18 and ctx.face in ("west", "east"):
        v -= 0.18                                         # Kiel
    if tz > 0.9 and abs(ry - 0.5) > 0.3:
        return None                                       # spitz zulaufend
    col = pick(ctx.pal["feather"], v)
    if tz < 0.3:
        col = mix(pick(ctx.pal["kon"], 0.7), col, tz / 0.3)   # am Ansatz violett wie das Konterit
    return col


def _cloth(ctx: Ctx):
    """Stoff des Umhangs: weiche Falten, unten ausgefranst."""
    ry = rel_y(ctx)
    fold = 0.5 + 0.5 * math.sin(ctx.local[0] * 1.6)
    if ctx.cube.origin[0] > -4.4 and ry > 0.86 and _noise(ctx, 17) > 0.45:
        return None
    v = 0.3 + 0.35 * fold - 0.15 * ry
    if ctx.face == "front":
        v -= 0.12                                         # Innenseite
    return pick(ctx.pal["kon_cloth"], v)


def _gem(key: str, glowing: bool = True):
    def paint(ctx: Ctx):
        v = 0.55 + 0.3 * (1.0 - rel_y(ctx))
        if ctx.s == 0 and ctx.t == 0:
            v = 0.99
        col = pick(ctx.pal[key], v)
        if glowing:
            ctx.glow = col
        return col
    return paint


def _crystal(key: str, glow_key: str | None, glow_from: float = 0.6):
    """Kristall: Facetten (schräge Bänder), zur Spitze hell; die Spitze leuchtet."""
    def paint(ctx: Ctx):
        t = _seg_t(ctx)
        facet = ((ctx.s + ctx.t) // 2) % 3
        v = 0.3 + 0.55 * t + (0.12 if facet == 0 else -0.04 if facet == 1 else 0.0)
        if ctx.face in ("west", "front"):
            v += 0.08
        col = pick(ctx.pal[key], v)
        if glow_key and t >= glow_from:
            g = pick(ctx.pal[glow_key], (t - glow_from) / (1.0 - glow_from))
            ctx.glow = g
            col = mix(col, g, 0.6)
        return col
    return paint


def _horn(key: str, glow_key: str | None = None, glow_from: float = 0.82, rings: bool = True, tip_key: str | None = None):
    """Horn: vom dunklen Ansatz zur hellen Spitze, mit Ringen; mit glow_key glüht die Spitze, mit tip_key ist sie golden."""
    def paint(ctx: Ctx):
        t = _seg_t(ctx)
        v = 0.25 + 0.6 * t
        if rings and int(t * 14) % 3 == 0 and t < 0.85:
            v -= 0.1
        if ctx.face in ("west", "back"):
            v -= 0.06
        col = pick(ctx.pal[key], v + (_noise(ctx, 19) - 0.5) * 0.06)
        if tip_key and t >= glow_from:
            col = pick(ctx.pal[tip_key], 0.4 + 0.5 * (t - glow_from) / (1.0 - glow_from))
        if glow_key and t >= glow_from:
            g = pick(ctx.pal[glow_key], (t - glow_from) / (1.0 - glow_from))
            ctx.glow = g
            col = mix(col, g, 0.7)
        return col
    return paint


def _aby_horn(ctx: Ctx):
    """Abyssit-Horn: schwarzes Chitin, durch das leuchtende Adern ziehen; die Spitze glüht."""
    t = _seg_t(ctx)
    v = 0.2 + 0.45 * t + (_noise(ctx, 23) - 0.5) * 0.08
    col = pick(ctx.pal["aby"], v)
    vein = abs(math.sin(ctx.local[0] * 2.3 + ctx.local[2] * 1.7 + t * 9.0))
    if vein < 0.12 or t > 0.86:
        g = pick(ctx.pal["aby_glow"], 0.4 + 0.6 * t)
        ctx.glow = g
        col = mix(col, g, 0.75)
    return col


def _scale(key: str):
    """Schuppen: versetzte Reihen von Bögen, oben heller."""
    def paint(ctx: Ctx):
        x, y, z = ctx.local
        a = x if ctx.face in ("front", "back", "top", "bottom") else z
        row = int(math.floor(y * 1.2))
        k = (a * 1.2 + (row % 2) * 0.5) % 1.0
        edge = abs(k - 0.5) * 2
        yy = (y * 1.2) % 1.0
        v = 0.55 - 0.25 * rel_y(ctx) + 0.12 * (1.0 - edge)
        if yy > 0.78 - 0.25 * edge * edge:
            v -= 0.22
        if ctx.face == "top":
            v += 0.1
        return pick(ctx.pal[key], v + (_noise(ctx, 29) - 0.5) * 0.06)
    return paint


def _tail(e: str):
    """Drachenschwanz: oben Schuppen, unten die helleren Bauchplatten, zur Spitze schmaler."""
    scale = DRAGON[e][0]
    scales = _scale(scale)

    def paint(ctx: Ctx):
        if ctx.face == "front":
            t = _seg_t(ctx)
            k = (ctx.local[1] * 1.3) % 1.0
            v = 0.72 - (0.2 if k > 0.8 else 0.0) - 0.1 * t
            return pick(ctx.pal[scale], v)
        return scales(ctx)
    return paint


def _leaf(ctx: Ctx):
    """Blatt (ein flacher Quader): Mittelrippe entlang der Länge, zur Spitze schmal, Ränder heller."""
    size, origin = ctx.cube.size, ctx.cube.origin
    thin, mid, long_ = sorted(range(3), key=lambda i: size[i])
    t = (ctx.local[long_] - origin[long_]) / size[long_] if size[long_] > 0 else 0.0
    across = abs((ctx.local[mid] - origin[mid]) / size[mid] - 0.5) * 2 if size[mid] > 0 else 0.0
    width = 1.0 - abs(t - 0.45) * 1.5
    big = {0: ("west", "east"), 1: ("top", "bottom"), 2: ("front", "back")}[thin]
    if ctx.face in big and across > max(0.22, width):
        return None
    v = 0.42 + 0.22 * across + 0.1 * (1.0 - t)
    if across < 0.14:
        v = 0.78                                          # Mittelrippe
    return pick(ctx.pal["leaf"], v + (_noise(ctx, 31) - 0.5) * 0.06)


def _stone(ctx: Ctx):
    x, y, z = ctx.local
    n = noise3(x * 0.8, y * 0.8, z * 0.8, 37)
    v = 0.35 + 0.35 * n + 0.15 * (1.0 - rel_y(ctx))
    if _noise(ctx, 41) > 0.93:
        v -= 0.2
    return pick(ctx.pal["stone"], v)


def _fin(ctx: Ctx):
    """Flosse: Strahlen vom Ansatz nach außen, dazwischen durchscheinende Haut, der Saum hell und ausgefranst."""
    oz, d = ctx.cube.origin[2], ctx.cube.size[2]
    tz = (ctx.local[2] - oz) / d if d > 0 else 0.0
    ray = (ctx.local[1] * 1.6) % 1.0 < 0.3
    if tz > 0.88 and _noise(ctx, 43) > 0.55:
        return None
    v = 0.35 + 0.45 * tz + (0.15 if ray else 0.0)
    return pick(ctx.pal["fin"], v)


def _flame(ctx: Ctx):
    """Die glühende Flammenspitze des Feuerschwanzes."""
    t = max(0.0, min(1.0, (ctx.local[1]) / 5.0))
    col = pick(ctx.pal["fire_glow"], 1.0 - t * 0.8)
    ctx.glow = col
    return col


def _talon(e: str):
    scale, horn, glow = DRAGON[e]
    key = "stone" if e == "earth" else "antler" if e == "nature" else horn if e in ("fire", "lightning", "ice") else "pearl"

    def paint(ctx: Ctx):
        tz = (ctx.cube.origin[2] + ctx.cube.size[2] - ctx.local[2]) / ctx.cube.size[2]
        return pick(ctx.pal[key], 0.35 + 0.55 * tz)
    return paint


def _crown_gold(ctx: Ctx):
    v = 0.74 - 0.34 * rel_y(ctx) + (0.16 if ctx.face == "top" else 0.0) - (0.2 if ctx.face == "bottom" else 0.0)
    if ctx.t == 0 or ctx.s == 0:
        v += 0.12
    return pick(ctx.pal["crown_gold"], v + (_noise(ctx, 47) - 0.5) * 0.05)


def _fur(ctx: Ctx):
    """Pelz: Büschel (Strähnen), unten zottelig."""
    x, y, z = ctx.local
    tuft = hash01(int(x * 2.5) + int(z * 2.5) * 7, 53)
    v = 0.35 + 0.4 * tuft + 0.15 * (1.0 - rel_y(ctx))
    if rel_y(ctx) > 0.86 and _noise(ctx, 55) > 0.7 and ctx.face not in ("top", "bottom"):
        return None
    return pick(ctx.pal["fur"], v)


def _quill(ctx: Ctx):
    """Die Feder an der Jägerkappe: weiß mit roter Spitze, Kiel in der Mitte."""
    oy, h = ctx.cube.origin[1], ctx.cube.size[1]
    t = (oy + h - ctx.local[1]) / h if h > 0 else 0.0
    across = abs((ctx.local[2] - ctx.cube.origin[2]) / ctx.cube.size[2] - 0.5) * 2
    if t > 0.9 and across > 0.3:
        return None
    if across < 0.2:
        return pick(ctx.pal["plume_white"], 0.4)
    return pick(ctx.pal["plume_white"], 0.95 - 0.7 * max(0.0, t - 0.65) / 0.35)


def _mail(ctx: Ctx):
    """Kettengeflecht: Reihen kleiner Ringe, dazwischen Lücken (durchsichtig)."""
    if ctx.u % 2 == 1 and ctx.v % 2 == 1:
        return None
    v = 0.6 - 0.25 * rel_y(ctx) + (0.12 if (ctx.u // 2 + ctx.v // 2) % 2 else -0.05)
    return pick(ctx.pal["mail"], v)


def _verdigris(base):
    """Kupfer mit Flecken von Grünspan."""
    def paint(ctx: Ctx):
        col = base(ctx)
        x, y, z = ctx.local
        n = noise3(x * 0.7 + 3.0, y * 0.7, z * 0.7, 59)
        if n > 0.62:
            col = mix(col, pick(ctx.pal["verdigris"], 0.4 + (n - 0.62) * 1.5), 0.75)
        if ctx.face in ("front", "west", "east", "back") and ctx.t == 1 and ctx.s % 5 == 2:
            col = pick(ctx.pal["copper"], 0.95)               # Nieten
        return col
    return paint


def _turtle_shell(ctx: Ctx):
    """Panzer aus Schilden: sechseckige Felder mit dunklen Fugen."""
    x, y, z = ctx.local
    cell = 2.6
    q = ((x / cell) + 0.5 * (int(math.floor(z / cell)) % 2)) % 1.0
    r = (z / cell) % 1.0
    edge = min(q, 1.0 - q, r, 1.0 - r)
    v = 0.55 + 0.15 * (1.0 - rel_y(ctx)) + (_noise(ctx, 61) - 0.5) * 0.08
    if edge < 0.12:
        v = 0.22
    return pick(ctx.pal["turtle"], v)


def _moon_gem(ctx: Ctx):
    """Mondstein: milchig weiß, oben ein Glanz – quer darüber wandert ein blauer Schimmer, der leuchtet."""
    ry = rel_y(ctx)
    v = 0.62 + 0.3 * (1.0 - ry) + (_noise(ctx, 67) - 0.5) * 0.06
    if ctx.s == 0 and ctx.t == 0:
        v = 1.0
    col = pick(ctx.pal["moon_gem"], v)
    band = abs(ry - 0.45)
    if band < 0.22 and ctx.face not in ("top", "bottom"):
        g = pick(ctx.pal["moon_glow"], 0.55 + 0.4 * (1.0 - band / 0.22))
        col = mix(col, g, 0.6)
        ctx.glow = col
    return col


def _moon_crescent(ctx: Ctx):
    """Die Mondsichel: helles Mondlicht, zu den Spitzen weißer; sie leuchtet ganz."""
    t = _seg_t(ctx) if (ctx.cube.tag or "").count(":") == 2 else 0.0
    v = 0.5 + 0.45 * t + (0.08 if ctx.face in ("front", "west") else 0.0)
    col = pick(ctx.pal["moon_glow"], v)
    ctx.glow = col
    return col


def _star_cloth(ctx: Ctx):
    """Sternenmantel: nachtblauer Stoff mit weichen Falten, darin gestickte Sterne (sie glimmen), unten eine Silberborte."""
    ry = rel_y(ctx)
    fold = 0.5 + 0.5 * math.sin((ctx.local[0] + ctx.local[2]) * 1.5)
    v = 0.34 + 0.32 * fold - 0.12 * ry + (_noise(ctx, 71) - 0.5) * 0.05
    if ctx.face == "front" and ctx.cube.size[2] < 1.0 and ctx.cube.size[0] > 6.0:
        v -= 0.14                                         # Innenseite des Mantels
    col = pick(ctx.pal["moon_cloth"], v)
    hem = (ctx.cube.origin[1] + ctx.cube.size[1] - ctx.local[1]) < 0.75
    if hem and ctx.face not in ("top", "bottom"):
        return pick(ctx.pal["silver"], 0.62 + (_noise(ctx, 73) - 0.5) * 0.15)
    n = hash01(ctx.u * 3 + 1, ctx.v * 5 + 2, 79)
    if n > 0.963:
        star = pick(ctx.pal["moon_star"], 0.6 + (n - 0.963) * 10.0)
        ctx.glow = mix((0, 0, 0, 255), star, 0.75)
        return star
    return col


def _aqua_shell(ctx: Ctx):
    """Die Helmschale aus Aquamarin: glatt und glänzend, oben hell, unten tief meerblau; ein heller Lichtstreif läuft schräg
    über jede Seite, feine Schliffkanten an den Rändern."""
    ry = rel_y(ctx)
    v = 0.74 - 0.4 * ry
    if ctx.face == "top":
        v = 0.86
    elif ctx.face == "bottom":
        v = 0.3
    if ctx.s == 0 or ctx.t == 0:
        v += 0.1
    if ctx.s == ctx.w - 1 or ctx.t == ctx.h - 1:
        v -= 0.12
    k = (ctx.s + ctx.t) / max(1.0, (ctx.w + ctx.h) * 0.5)
    if 0.42 < k < 0.56 and ctx.face not in ("top", "bottom"):
        v += 0.16
    v += (_noise(ctx, 101) - 0.5) * 0.05
    return pick(ctx.pal["aqua"], v)


def _aqua_visor(ctx: Ctx):
    """Die Vorderseite der Helmschale: im Sichtfenster durchsichtig (nur zwei schräge Glanzstriche auf dem Glas)."""
    x, y = ctx.local[0], ctx.local[1]
    x0, x1, y0, y1 = VISOR
    if x0 <= x <= x1 and y0 <= y <= y1:
        u, w = x - x0, y - y0
        d = u + w
        if (1.4 < d < 2.0 and u < 2.4) or (2.6 < d < 2.95 and u < 1.9):
            return mix(pick(ctx.pal["aqua_glass"], 0.99), (255, 255, 255, 255), 0.55)
        return None
    return _aqua_shell(ctx)


def _aqua_brass(ctx: Ctx):
    """Messing: warm glänzend, oben hell, an den Kanten Glanz, unten dunkel."""
    ry = rel_y(ctx)
    v = 0.7 - 0.36 * ry
    if ctx.face == "top":
        v = 0.86
    elif ctx.face == "bottom":
        v = 0.3
    if ctx.s == 0 or ctx.t == 0:
        v += 0.12
    if ctx.s == ctx.w - 1 or ctx.t == ctx.h - 1:
        v -= 0.14
    v += (_noise(ctx, 103) - 0.5) * 0.06
    return pick(ctx.pal["brass"], v)


def _aqua_collar(ctx: Ctx):
    """Der Kragen: Messing mit einer Reihe Schrauben (heller Kopf, dunkler Schatten darunter)."""
    col = _aqua_brass(ctx)
    if ctx.face not in ("top", "bottom") and ctx.h >= 2:
        if ctx.t == ctx.h // 2 and ctx.s % 5 == 2:
            return pick(ctx.pal["brass"], 0.97)
        if ctx.t == ctx.h // 2 + 1 and ctx.s % 5 == 2:
            return pick(ctx.pal["brass"], 0.25)
    return col


def _aqua_glass(ctx: Ctx):
    """Das Glas der Bullaugen: dunkles Meerblau, oben links ein Glanz."""
    v = 0.3 + 0.25 * (1.0 - rel_y(ctx))
    if ctx.s + ctx.t <= 1:
        return mix(pick(ctx.pal["aqua_glass"], 0.99), (255, 255, 255, 255), 0.5)
    return pick(ctx.pal["aqua_glass"], v)


def _aqua_tank(ctx: Ctx):
    """Die Luftflaschen: rund (seitlich hell zur Mitte, dunkel zu den Kanten), Aquamarin-Lack, oben und unten Messingringe."""
    if ctx.face in ("top", "bottom"):
        return pick(ctx.pal["aqua"], 0.62 if ctx.face == "top" else 0.3)
    across = math.sin(math.pi * (ctx.s + 0.5) / max(1, ctx.w))
    v = 0.22 + 0.5 * across - 0.1 * rel_y(ctx)
    if ctx.face in ("west", "east"):
        v -= 0.06
    col = pick(ctx.pal["aqua"], v)
    if ctx.cube.size[1] > 4.0:
        ry = rel_y(ctx)
        if ry < 0.08 or ry > 0.92:
            col = pick(ctx.pal["brass"], 0.3 + 0.55 * across)
        elif 0.5 < ctx.s / max(1, ctx.w) < 0.68 and 0.12 < ry < 0.86:
            col = mix(col, (255, 255, 255, 255), 0.18)   # Glanz längs der Flasche
    return col


def _aqua_hose(ctx: Ctx):
    """Der Schlauch: dunkler Gummi mit Rippen."""
    along = ctx.local[1] if ctx.cube.size[1] >= ctx.cube.size[2] else ctx.local[2]
    rib = (along * 2.5) % 1.0 < 0.35
    v = 0.45 - 0.2 * rel_y(ctx) + (0.18 if rib else 0.0)
    return pick(ctx.pal["rubber"], v)


def _aqua_dial(ctx: Ctx):
    """Die Druckanzeige: ein helles Zifferblatt im Messingring, ein roter Zeiger, kleine Striche."""
    cx, cy = (ctx.w - 1) / 2.0, (ctx.h - 1) / 2.0
    dx, dy = ctx.s - cx, ctx.t - cy
    r = math.hypot(dx, dy)
    if r > min(cx, cy) - 0.35:
        return _aqua_brass(ctx)
    # der Zeiger: von der Mitte schräg nach oben rechts
    if dx >= -0.6 and dy <= 0.6 and abs(dx + dy) < 0.8 and r < min(cx, cy) - 0.9:
        return (200, 44, 34, 255)
    col = pick(ctx.pal["dial"], 0.92 - 0.35 * (r / max(cx, 1.0)))
    if r > min(cx, cy) - 1.4 and (ctx.s * 3 + ctx.t * 5) % 4 == 0:
        col = pick(ctx.pal["rubber"], 0.35)
    return col


def _aqua_strap(ctx: Ctx):
    """Der Riemen der Flossen: dunkler Gummi."""
    return pick(ctx.pal["rubber"], 0.38 + 0.18 * (1.0 - rel_y(ctx)) + (_noise(ctx, 107) - 0.5) * 0.06)


def _aqua_fin(ctx: Ctx):
    """Das Flossenblatt: Rippen längs (dunkler), dazwischen das Blatt, zu den Rändern und zur Spitze heller."""
    t = _seg_t(ctx)
    ox, w = ctx.cube.origin[0], ctx.cube.size[0]
    tx = (ctx.local[0] - ox) / w if w > 0 else 0.5
    edge = min(tx, 1.0 - tx)
    rib = ((ctx.local[0] + 0.3) % 1.25) < 0.32
    v = 0.4 + 0.3 * t + (0.2 if edge < 0.08 else 0.0) - (0.18 if rib and edge >= 0.08 else 0.0)
    if ctx.face == "bottom":
        v -= 0.15
    return pick(ctx.pal["aqua_fin"], v)


def _dusk_cloth(ctx: Ctx):
    """Tuch der Dämmerrüstung: rauchviolett, weiche Falten, ein wenig fleckig."""
    ry = rel_y(ctx)
    fold = 0.5 + 0.5 * math.sin((ctx.local[0] + ctx.local[2]) * 1.3 + ctx.local[1] * 0.4)
    v = 0.3 + 0.3 * fold - 0.1 * ry + (_noise(ctx, 131) - 0.5) * 0.08
    if ctx.face == "bottom":
        v -= 0.12
    return pick(ctx.pal["dusk_cloth"], v)


def _dusk_cape(ctx: Ctx):
    """Der Schulterumhang: wie das Tuch, innen dunkler, am Rand eine Lederborte."""
    v = 0.28 + 0.3 * (0.5 + 0.5 * math.sin(ctx.local[0] * 1.7)) - 0.1 * rel_y(ctx)
    if ctx.face == "front":
        v -= 0.14                                         # Innenseite
    return pick(ctx.pal["dusk_cloth"], v + (_noise(ctx, 137) - 0.5) * 0.06)


def _dusk_hood_rim(ctx: Ctx):
    """Der Saum der Kapuze: doppelt gelegtes Tuch, außen heller, mit einer feinen Naht."""
    v = 0.48 + 0.12 * (1.0 - rel_y(ctx))
    if ctx.t == 0 or ctx.s == 0:
        v -= 0.16
    return pick(ctx.pal["dusk_cloth"], v)


def _dusk_mask(ctx: Ctx):
    """Die Halbmaske: dunkles Tuch, quer gefaltet."""
    v = 0.22 + 0.18 * (0.5 + 0.5 * math.sin(ctx.local[1] * 3.4))
    return pick(ctx.pal["dusk_cloth"], v)


def _dusk_leather(ctx: Ctx):
    """Leder: warmes Dunkelbraun, Kanten heller, feine Stiche."""
    v = 0.4 + 0.2 * (1.0 - rel_y(ctx)) + (_noise(ctx, 139) - 0.5) * 0.1
    if (ctx.s + ctx.t) % 5 == 0 and ctx.face not in ("top", "bottom"):
        v += 0.14                                         # Stiche
    return pick(ctx.pal["dusk_leather"], v)


def _dusk_wrap(ctx: Ctx):
    """Wickelriemen: schräg übereinanderliegende Lederbänder."""
    band = ((ctx.local[0] + ctx.local[2] + ctx.local[1] * 2.0) % 1.6) < 0.5
    v = 0.32 + (0.16 if band else 0.0) + (_noise(ctx, 149) - 0.5) * 0.08
    return pick(ctx.pal["dusk_leather"], v)


def _dusk_gem(ctx: Ctx):
    """Dämmerquarz: zur Spitze nachtviolett und hell, am Fuß ein Rest Abendrot; die Spitze glimmt."""
    t = _seg_t(ctx)
    facet = ((ctx.s + ctx.t) // 2) % 3
    v = 0.32 + 0.55 * t + (0.12 if facet == 0 else -0.04 if facet == 1 else 0.0)
    if ctx.face in ("west", "front"):
        v += 0.08
    col = pick(ctx.pal["dusk_quartz"], v)
    if t < 0.3:
        col = mix(col, pick(ctx.pal["dusk_ember"], 0.45 + t), 0.55 * (1.0 - t / 0.3))
    if t > 0.72:
        ctx.glow = mix((0, 0, 0, 255), col, 0.7)
    return col


def materials(variant: str) -> dict:
    out = {
        "fur": _fur,
        "quill": _quill,
        "leather_strap": _metal("leather"),
        "mail": _mail,
        "iron_plate": _metal("iron"),
        "gold_plate": _gold("gold_armor"),
        "diamond_crystal": _crystal("diamond", None),
        "diamond_plate": _metal("diamond"),
        "diamond_gem": _gem("diamond", glowing=False),
        "netherite_plate": _metal("netherite", rim="gold", rim_from=0.86, rivets=True),
        "netherite_horn": _horn("netherite", rings=True),
        "copper_plate": _verdigris(_metal("copper")),
        "turtle_shell": _turtle_shell,
        "steel_plate": _metal("steel"),
        "steel_pauldron": _metal("steel", rim="gold", rim_from=0.72, rivets=True),
        "steel_sabaton": _sabaton,
        "gold": _gold(),
        "spur": _spur,
        "plume": _plume,
        "silver": _metal("silver"),
        "feather": _feather,
        "kon_cloth": _cloth,
        "kon_plate": _metal("kon", rim="silver", rim_from=0.8),
        "kon_gem": _gem("kon_gem"),
        "night_plate": _metal("night", rim="silver", rim_from=0.8),
        "night_crystal": _crystal("night_crystal", "night_glow", 0.62),
        "aby_plate": _metal("aby", rim="aby_crystal", rim_from=0.85),
        "aby_crystal": _crystal("aby_crystal", "aby_glow", 0.35),
        "aby_gem": _gem("aby_crystal"),
        "aby_horn": _aby_horn,
        "dragon_horn_fire": _horn("fire_horn", "fire_glow", 0.8),
        "dragon_horn_ice": _crystal("ice_crystal", "ice_glow", 0.85),
        "dragon_horn_lightning": _horn("light_horn", "light_glow", 0.84, tip_key="light_gold"),
        "antler": _horn("antler", rings=False),
        "ram": _horn("ram", rings=True),
        "leaf": _leaf,
        "stone": _stone,
        "amber": _gem("amber"),
        "fin": _fin,
        "pearl": _gem("pearl", glowing=False),
        "dragon_flame": _flame,
        "crown_gold": _crown_gold,
        "ruby": _gem("ruby", glowing=False),
        "sapphire": _gem("sapphire", glowing=False),
        "emerald": _gem("emerald", glowing=False),
        "amethyst": _gem("amethyst", glowing=False),
        "moon_plate": _metal("moon", rim="silver", rim_from=0.82),
        "moon_gem": _moon_gem,
        "moon_crescent": _moon_crescent,
        "moon_cloth": _star_cloth,
        "aqua_shell": _aqua_shell,
        "aqua_visor": _aqua_visor,
        "aqua_brass": _aqua_brass,
        "aqua_collar": _aqua_collar,
        "aqua_glass": _aqua_glass,
        "aqua_tank": _aqua_tank,
        "aqua_hose": _aqua_hose,
        "aqua_dial": _aqua_dial,
        "aqua_strap": _aqua_strap,
        "aqua_fin": _aqua_fin,
        "aqua_plate": _metal("aqua", rim="brass", rim_from=0.8),
        "aqua_lead": _metal("lead"),
        "aqua_gem": _gem("aqua_gem"),
        "dusk_cloth": _dusk_cloth,
        "dusk_cape": _dusk_cape,
        "dusk_hood_rim": _dusk_hood_rim,
        "dusk_mask": _dusk_mask,
        "dusk_leather": _dusk_leather,
        "dusk_wrap": _dusk_wrap,
        "dusk_gem": _dusk_gem,
        "dusk_buckle": _metal("silver"),
    }
    for e in DRAGON:
        out["dragon_scale_" + e] = _scale(DRAGON[e][0])
        out["dragon_tail_" + e] = _tail(e)
        out["talon_" + e] = _talon(e)
    return out
