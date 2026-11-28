#!/usr/bin/env python3
"""
3D-Modelle der Waffenkammer (0.84): fünf Waffen und vier Spitzhacken, wie sie in der Hand gezeigt werden.

Gebaut wie die Waffen aus Monsterteilen (tools/monster_gear_models.py, dessen Bausteine hier verwendet werden): senkrecht um
x = z = 8, der Griff unten, die Spitze oben, aus Quadern und gebogenen, sich verjüngenden Strängen. Flache Teile, die aus
mehreren Quadern bestehen (Axtblätter, Hammerköpfe), bekommen je ihren Ausschnitt eines gemeinsamen Texturbereichs – so liegt
das Bild über alle Quader hinweg wie aus einem Guss.

Die Texturbereiche (UV, Einheiten 0..16 = 32 Pixel) werden hier festgelegt; tools/armory_textures.py malt sie aus.

Ausgabe: src/main/java/.../datagen/ArmoryShapes.java. Mit --preview DATEI zeichnet das Skript die Modelle (schräg wie in der
Hand und von der Seite), ohne Spiel.
"""
from __future__ import annotations

import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
import monster_gear_models as mg  # noqa: E402
from monster_gear_models import Shape, _sub  # noqa: E402

ROOT = Path(__file__).resolve().parent.parent
JAVA = ROOT / "src/main/java/io/github/sirschrolli/kingdomsatwar/datagen/ArmoryShapes.java"

UV = tuple[float, float, float, float]

# Texturbereiche je Gegenstand (u0, v0, u1, v1) in UV-Einheiten (je zwei Pixel). Flächen ("plate"-Bereiche) stehen aufrecht:
# oben im Bild ist oben am Gegenstand. Stränge: v0 = Spitze, v1 = Fuß.
REGIONS: dict[str, dict[str, UV]] = {
    "steel_halberd": {
        "shaft": (0, 0, 1, 16),         # Eschenholz
        "shaft_side": (1, 0, 2, 16),
        "wrap": (2, 0, 4, 5),           # Lederwicklung am Griff
        "steel": (2, 5, 4, 7),          # Stahl (Schuh, Bänder, Kragen)
        "steel_top": (2, 7, 4, 9),
        "blade": (4, 0, 10, 8),         # das Axtblatt von vorn (links die Schneide)
        "edge": (10, 0, 11, 8),         # die Schneide (hell)
        "blade_side": (11, 0, 12, 8),
        "point": (12, 0, 14, 12),       # die Stoßklinge oben: Spitze oben
        "point_edge": (14, 0, 15, 12),
        "hook": (4, 8, 6, 14),          # der Haken hinten
        "hook_edge": (6, 8, 7, 14),
        "tassel": (7, 8, 10, 14),       # roter Quast unter dem Kopf
        "tassel_side": (10, 8, 11, 14),
    },
    "keeper_maul": {
        "handle": (0, 0, 1, 16),        # versteinertes Glimmerholz
        "handle_side": (1, 0, 2, 16),
        "wrap": (2, 0, 4, 5),           # Lederwicklung
        "band": (2, 5, 4, 7),           # Mondsteinbänder am Stiel (leuchten)
        "band_top": (2, 7, 4, 9),
        "head": (4, 0, 12, 6),          # der Kopf von vorn: gefügte Feensteinquader, in der Mitte der Mondstein
        "head_side": (4, 6, 12, 9),     # von oben/unten
        "face": (12, 0, 16, 6),         # die Schlagflächen: grober Fels
        "gem": (12, 6, 14, 8),          # der Mondstein vorn und hinten (leuchtet)
        "crystal": (14, 6, 16, 14),     # Kristalle oben auf dem Kopf: Spitze oben
        "crystal_edge": (12, 8, 13, 14),
        "pommel": (4, 9, 6, 11),        # der Knaufstein
        "pommel_top": (6, 9, 8, 11),
    },
    "sea_witch_staff": {
        "shaft": (0, 0, 1, 16),         # gebleichtes Treibholz
        "shaft_side": (1, 0, 2, 16),
        "knot": (2, 0, 4, 2),           # Knorren
        "knot_top": (2, 2, 4, 3),
        "wrap": (2, 3, 4, 8),           # Tang um den Griff
        "coral": (4, 0, 6, 10),         # Zinken aus schwarzer Koralle: Spitze oben
        "coral_edge": (6, 0, 7, 10),
        "orb": (8, 0, 12, 4),           # die leuchtende Kugel
        "orb_side": (8, 4, 12, 8),
        "cord": (12, 0, 13, 6),         # Schnüre
        "shell": (13, 0, 15, 2),        # Muscheln
        "tooth": (13, 2, 14, 5),        # ein Haizahn
    },
    "shepherd_crook": {
        "shaft": (0, 0, 1, 16),         # Sumpfeiche
        "shaft_side": (1, 0, 2, 16),
        "wrap": (2, 0, 4, 5),           # Strick um den Griff
        "iron": (2, 5, 4, 7),           # geschmiedetes Eisen (Fuß, Bänder, Zwinge)
        "iron_top": (2, 7, 4, 9),
        "hook": (4, 0, 6, 12),          # der eiserne Haken: Spitze oben
        "hook_edge": (6, 0, 7, 12),
        "roof": (8, 0, 11, 2),          # Dach und Boden der Laterne
        "roof_top": (8, 2, 11, 4),
        "glass": (11, 0, 14, 4),        # das Glas der Laterne, dahinter Seelenfeuer (leuchtet)
        "glass_top": (11, 4, 14, 6),
        "chain": (14, 0, 15, 4),        # die Kette der Laterne
        "cord": (15, 0, 16, 6),         # die Schnur des Talismans
        "bone": (8, 4, 10, 8),          # ein Knochen als Talisman
    },
    "minute_hand": {
        "grip": (0, 0, 2, 6),           # dunkles Leder, gewickelt
        "brass": (2, 0, 4, 3),          # Messing (Knauf, Parierstange, Klingenwurzel)
        "brass_top": (2, 3, 4, 5),
        "gem": (4, 0, 5, 1),            # Chronit im Knauf (leuchtet)
        "guard": (4, 1, 12, 3),         # die Parierstange von vorn (graviert)
        "gear": (12, 0, 14, 2),         # kleine Zahnräder an den Enden der Parierstange
        "blade": (0, 6, 2, 13),         # die breite Klingenwurzel: gebläuter Stahl, Messingseele
        "blade_side": (2, 6, 3, 13),
        "ring": (4, 4, 8, 8),           # das durchbrochene Ornament (Ring mit Chronit)
        "narrow": (3, 6, 4, 16),        # die lange, schmale Klinge
        "tip": (8, 4, 12, 10),          # die Rautenspitze: Spitze oben
        "core": (12, 4, 13, 16),        # die Chronitader in der Klinge (leuchtet)
    },
    "draconium_warhammer": {
        "handle": (0, 0, 2, 16),        # dunkles Holz mit Draconiumbändern
        "wrap": (2, 0, 4, 6),           # Schuppenleder
        "gold": (2, 6, 4, 8),           # goldene Fassungen
        "gold_top": (2, 8, 4, 10),
        "head": (4, 0, 12, 5),          # der Kopf von vorn (Draconium mit Schuppen, in der Mitte die Glut)
        "head_side": (4, 5, 8, 10),     # der Kopf von oben/unten
        "face": (8, 5, 12, 10),         # die Schlagflächen (links/rechts)
        "rune": (12, 0, 14, 3),         # glühende Rune auf dem Kopf (leuchtet)
        "spike": (14, 0, 16, 8),        # der Dorn oben: Spitze oben
        "spike_edge": (12, 3, 13, 11),
        "fang": (4, 10, 6, 16),         # Klauen an den Seiten des Kopfs
        "fang_edge": (6, 10, 7, 16),
        "ember": (13, 3, 14, 6),        # Glut (leuchtet)
    },
    "nightsteel_dagger": {
        "grip": (0, 0, 2, 6),           # Griff mit Lederwicklung
        "metal": (0, 6, 2, 8),          # Nachtstahl (Knauf, Parier)
        "metal_top": (0, 8, 2, 10),
        "gem": (2, 0, 4, 2),            # Mondstein im Knauf (leuchtet)
        "guard": (2, 2, 6, 4),          # Parierstange von vorn
        "guard_side": (2, 4, 6, 5),
        "blade": (6, 0, 10, 16),        # die Klinge: Spitze oben, links die Schneide
        "blade_edge": (10, 0, 11, 16),
        "fuller": (11, 0, 12, 16),      # die violette Hohlkehle (leuchtet)
        "fuller_side": (12, 0, 13, 16),
    },
    "abyssite_greatsword": {
        "grip": (0, 0, 2, 10),          # langer Griff, umwickelt
        "metal": (0, 10, 2, 12),        # Abyssit (Knauf, Fassungen)
        "metal_top": (0, 12, 2, 14),
        "gem": (0, 14, 2, 16),          # Kristall im Knauf (leuchtet)
        "guard": (2, 0, 10, 3),         # die breite Parierstange von vorn
        "guard_side": (2, 3, 10, 4),
        "blade": (10, 0, 14, 16),       # die Klinge: Spitze oben
        "blade_edge": (14, 0, 15, 16),
        "rune": (15, 0, 16, 16),        # glühende Runen in der Mitte (leuchten)
        "crystal": (2, 4, 4, 10),       # Kristallzacken an der Parierstange (leuchten)
        "crystal_edge": (4, 4, 5, 10),
    },
    # die Stimmgabel (1.7): Sordins Waffe – ein Griff aus Kupfer in seegrünem Leder, zwei Zinken aus Hallkristall
    "tuning_fork": {
        "grip": (0, 0, 2, 8),           # seegrüne Lederwicklung
        "copper": (0, 8, 2, 10),        # grün angelaufenes Kupfer (Kappe, Kragen, Hals)
        "copper_top": (0, 10, 2, 12),
        "gem": (0, 12, 2, 14),          # der Hallkristall im Knauf (leuchtet)
        "yoke": (2, 0, 9, 2),           # der Bügel von vorn (Kupfer)
        "yoke_side": (2, 2, 9, 3),
        "prong": (9, 0, 11, 16),        # die Zinken aus Hallkristall: Spitze oben
        "prong_edge": (11, 0, 12, 16),
        "ring": (12, 0, 13, 16),        # leuchtende Ringe um die Zinken
    },
    # der Morgenstern (1.8): Kunos Waffe – ein Stiel aus Eschenholz, schwarzes Leder mit goldener Naht, Eisenschienen und eine
    # Kugel aus geschwärztem Eisen voller Stacheln aus blankem Stahl
    "morning_star": {
        "shaft": (0, 0, 1, 16),         # Eschenholz
        "shaft_side": (1, 0, 2, 16),
        "wrap": (2, 0, 4, 6),           # schwarzes Leder mit goldener Naht
        "iron": (2, 6, 4, 8),           # geschwärztes Eisen (Knauf, Bänder)
        "iron_top": (2, 8, 4, 10),
        "gold": (2, 10, 4, 12),         # Gold (Knopf, Kragen)
        "gold_top": (2, 12, 4, 14),
        "langet": (4, 0, 6, 6),         # die Eisenschienen unter dem Kopf, mit Nieten
        "head": (6, 0, 12, 6),          # die Kugel: geschwärztes, gehämmertes Eisen
        "head_side": (6, 6, 12, 12),
        "head_top": (12, 0, 16, 4),
        "spike": (12, 4, 14, 10),       # die Stacheln: blanker Stahl, Spitze oben
        "spike_tip": (14, 4, 15, 6),
    },
    # der Schlangenzahn (1.10): Quetzaras Giftzahn als Dolch – ein Griff aus Jade mit Golddraht, ein Smaragd im Knauf, zwei
    # goldene Schlangenköpfe als Parierstange, die Klinge aus Elfenbein mit leuchtender Giftrinne, am Knauf zwei Quetzalfedern
    "serpent_fang": {
        "grip": (0, 0, 2, 6),           # Jade, mit Golddraht umwickelt
        "gold": (0, 6, 2, 8),           # Gold (Knauf, Kragen)
        "gold_top": (0, 8, 2, 10),
        "gem": (0, 10, 2, 12),          # der Smaragd im Knauf (leuchtet)
        "guard": (2, 0, 8, 2),          # die Parierstange von vorn: zwei Schlangenköpfe
        "guard_side": (2, 2, 8, 3),
        "feather": (2, 4, 4, 12),       # die Quetzalfedern am Knauf: Spitze oben
        "feather_edge": (4, 4, 5, 12),
        "blade": (8, 0, 12, 16),        # der Giftzahn: Elfenbein, Spitze oben
        "blade_edge": (12, 0, 13, 16),
        "venom": (13, 0, 14, 16),       # die Giftrinne (leuchtet)
        "venom_side": (14, 0, 15, 16),
    },
    # die Phasenklinge (1.5): Phasmors Zweihänder aus einem einzigen Phasitkristall
    "phase_blade": {
        "grip": (0, 0, 2, 8),           # violette Lederwicklung
        "metal": (0, 8, 2, 10),         # Leerenstahl (Knaufkappe, Kragen)
        "metal_top": (0, 10, 2, 12),
        "gem": (0, 12, 2, 14),          # der Phasit im Knauf (leuchtet)
        "guard": (2, 0, 8, 2),          # die Parierstange aus Purpur von vorn, in der Mitte ein Phasit
        "guard_side": (2, 2, 8, 3),
        "shard": (2, 3, 4, 9),          # Phasitsplitter an den Enden der Parierstange (leuchten)
        "shard_edge": (4, 3, 5, 9),
        "blade": (8, 0, 13, 16),        # die Klinge aus Phasit: Spitze oben
        "blade_edge": (13, 0, 14, 16),  # die Schneiden
        "ridge": (14, 0, 15, 16),       # der Mittelgrat (leuchtet)
    },
    "steel_double_axe": {
        "shaft": (0, 0, 1, 16),         # Eiche
        "shaft_side": (1, 0, 2, 16),
        "wrap": (2, 0, 4, 5),
        "steel": (2, 5, 4, 7),
        "steel_top": (2, 7, 4, 9),
        "blade": (4, 0, 14, 8),         # beide Blätter von vorn, als ein Bild (links und rechts die Schneiden)
        "blade_side": (14, 0, 15, 8),
        "edge": (15, 0, 16, 8),
        "spike": (4, 8, 6, 14),         # die Spitze oben
        "spike_edge": (6, 8, 7, 14),
    },
    "miner_hammer": {
        "handle": (0, 0, 1, 16),        # Eschenholz
        "handle_side": (1, 0, 2, 16),
        "wrap": (2, 0, 4, 5),
        "steel": (2, 5, 4, 7),
        "steel_top": (2, 7, 4, 9),
        "head": (4, 0, 12, 5),          # der schwere Kopf von vorn
        "head_top": (4, 5, 12, 8),      # von oben/unten
        "face": (12, 0, 16, 5),         # die Schlagflächen
        "pick": (4, 8, 6, 15),          # die Dorne an den Ecken: Spitze oben
        "pick_edge": (6, 8, 7, 15),
        "brass": (7, 8, 9, 10),         # Messingnieten
    },
    "ember_pickaxe": {
        "handle": (0, 0, 1, 16),        # verkohltes Holz
        "handle_side": (1, 0, 2, 16),
        "wrap": (2, 0, 4, 5),
        "gold": (2, 5, 4, 7),
        "gold_top": (2, 7, 4, 9),
        "pick": (4, 0, 7, 16),          # die Picke (Draconium): Spitze oben
        "pick_edge": (7, 0, 8, 16),
        "vein": (8, 0, 9, 16),          # die Glutader (leuchtet)
        "vein_edge": (9, 0, 10, 16),
        "core": (10, 0, 14, 4),         # die Fassung in der Mitte von vorn, mit Glutstein
        "core_side": (10, 4, 14, 6),
        "stone": (14, 0, 16, 2),        # Glutstein (leuchtet)
    },
    "vein_breaker": {
        "handle": (0, 0, 1, 16),
        "handle_side": (1, 0, 2, 16),
        "wrap": (2, 0, 4, 5),
        "metal": (2, 5, 4, 7),
        "metal_top": (2, 7, 4, 9),
        "pick": (4, 0, 7, 16),          # die lange Picke (Nachtstahl)
        "pick_edge": (7, 0, 8, 16),
        "vein": (8, 0, 9, 16),          # violette Adern (leuchten)
        "vein_edge": (9, 0, 10, 16),
        "core": (10, 0, 14, 4),
        "core_side": (10, 4, 14, 6),
        "ore": (14, 0, 16, 3),          # eingelassene Erzkristalle (leuchten)
    },
    "ore_seeker": {
        "handle": (0, 0, 1, 16),
        "handle_side": (1, 0, 2, 16),
        "wrap": (2, 0, 4, 5),
        "metal": (2, 5, 4, 7),
        "metal_top": (2, 7, 4, 9),
        "pick": (4, 0, 7, 16),          # die Picke (Abyssit)
        "pick_edge": (7, 0, 8, 16),
        "vein": (8, 0, 9, 16),          # Lichtlinien (leuchten)
        "vein_edge": (9, 0, 10, 16),
        "core": (10, 0, 14, 4),         # die Fassung des Auges
        "core_side": (10, 4, 14, 6),
        "eye": (14, 0, 16, 4),          # das Auge aus Abyssitkristall (leuchtet)
    },
}


def plate(s: Shape, x0: float, y0: float, x1: float, y1: float, z0: float, z1: float, region: str, frame: UV,
          side: str | None = None, cap: str | None = None, glow: bool = False, rot: tuple[float, float, float] = (0.0, 0.0, 0.0),
          pivot: tuple[float, float, float] | None = None) -> None:
    """Ein Quader einer Fläche: vorn/hinten bekommt er den Ausschnitt des Bereichs, der seiner Lage im Rahmen entspricht
    (frame = x0, y0, x1, y1 der ganzen Fläche; oben im Bild = oben am Gegenstand)."""
    fx0, fy0, fx1, fy1 = frame
    uv = _sub(s.r(region), (x0 - fx0) / (fx1 - fx0), (fy1 - y1) / (fy1 - fy0), (x1 - fx0) / (fx1 - fx0), (fy1 - y0) / (fy1 - fy0))
    s.box(x0, y0, z0, x1, y1, z1, uv, side=side, cap=cap, glow=glow, rot=rot, pivot=pivot)


# =============================================================================
# Waffen
# =============================================================================

def steel_halberd() -> Shape:
    """Stahl-Hellebarde: ein langer Eschenschaft mit Lederwicklung und Stahlschuh, oben ein Kragen mit rotem Quast, das breite
    Axtblatt (die Schneide halbmondförmig), hinten ein gebogener Haken und obenauf die lange Stoßklinge."""
    s = Shape(REGIONS["steel_halberd"])
    s.box(7.35, -7.0, 7.35, 8.65, -5.8, 8.65, "steel", cap="steel_top")
    s.chain(8.0, -7.0, 180, 0, [0.9], (-0.45, -0.12), (0.45, 0.12), (0.9, 0.3), "point", "point_edge")
    s.box(7.5, -5.8, 7.5, 8.5, 20.4, 8.5, "shaft", side="shaft_side", cap="steel_top")
    s.box(7.32, -2.6, 7.32, 8.68, 2.4, 8.68, "wrap", cap="steel_top")
    s.box(7.3, 8.0, 7.3, 8.7, 8.8, 8.7, "steel", cap="steel_top")
    # der Quast unter dem Kopf
    s.box(7.2, 13.0, 7.2, 8.8, 13.8, 8.8, "steel", cap="steel_top")
    s.chain(7.6, 13.0, 168, -6, [1.3, 1.2, 1.0], (-0.55, -0.4), (0.55, 0.4), (0.9, 0.6), "tassel", "tassel_side")
    s.chain(8.4, 13.0, 192, 6, [1.2, 1.1, 0.9], (-0.5, -0.35), (0.5, 0.35), (0.9, 0.6), "tassel", "tassel_side")
    # der Kragen um den Kopf
    s.box(7.2, 14.0, 7.2, 8.8, 15.2, 8.8, "steel", cap="steel_top")
    s.box(7.2, 20.0, 7.2, 8.8, 20.9, 8.8, "steel", cap="steel_top")
    # das Axtblatt: links, nach außen höher (Halbmond), die Schneide hell
    frame = (2.6, 13.6, 7.5, 21.6)
    for (x0, x1, y0, y1) in ((6.2, 7.5, 15.4, 19.6), (5.0, 6.2, 14.9, 20.1), (3.8, 5.0, 14.4, 20.7), (3.2, 3.8, 14.0, 21.1)):
        plate(s, x0, y0, x1, y1, 7.78, 8.22, "blade", frame, side="blade_side", cap="blade_side")
    plate(s, 2.6, 14.2, 3.2, 20.9, 7.84, 8.16, "edge", frame, side="edge", cap="edge")
    s.box(2.7, 20.6, 7.84, 3.5, 21.9, 8.16, "edge", rot=(0, 0, -28), pivot=(3.1, 20.8, 8.0))
    s.box(2.7, 13.3, 7.84, 3.5, 14.6, 8.16, "edge", rot=(0, 0, 28), pivot=(3.1, 14.4, 8.0))
    # der Haken hinten
    s.chain(8.6, 17.6, -86, -14, [1.3, 1.2, 1.0, 0.8], (-0.75, -0.12), (0.75, 0.12), (0.5, 0.25), "hook", "hook_edge")
    # die Stoßklinge
    s.chain(8.0, 20.8, 0, 0, [1.6, 1.6, 1.5, 1.3, 1.0], (-0.95, -0.08), (0.95, 0.08), (0.5, 0.18), "point", "point_edge")
    return s


def draconium_warhammer() -> Shape:
    """Draconium-Kriegshammer: ein kräftiger Stiel mit Schuppenleder und goldenen Fassungen, oben der schwere Kopf aus Draconium
    mit zwei Schlagflächen, Drachenklauen an den Seiten, einer glühenden Rune vorn und hinten und einem Dorn obenauf."""
    s = Shape(REGIONS["draconium_warhammer"])
    s.box(7.1, -6.4, 7.1, 8.9, -5.0, 8.9, "gold", cap="gold_top")
    s.box(7.8, -6.9, 7.8, 8.2, -6.4, 8.2, "ember", glow=True)
    s.box(7.3, -5.0, 7.3, 8.7, 12.6, 8.7, "handle", cap="gold_top")
    s.box(7.15, -3.6, 7.15, 8.85, 2.6, 8.85, "wrap", cap="gold_top")
    for y in (4.6, 8.4):
        s.box(7.2, y, 7.2, 8.8, y + 0.8, 8.8, "gold", cap="gold_top")
    s.box(6.9, 11.8, 6.9, 9.1, 12.8, 9.1, "gold", cap="gold_top")
    # der Kopf: Mittelstück, nach außen die Schlagflächen
    frame = (3.4, 12.6, 12.6, 17.4)
    plate(s, 5.4, 12.6, 10.6, 17.4, 6.0, 10.0, "head", frame, side="face", cap="head_side")
    plate(s, 4.2, 12.9, 5.4, 17.1, 6.2, 9.8, "head", frame, side="face", cap="head_side")
    plate(s, 10.6, 12.9, 11.8, 17.1, 6.2, 9.8, "head", frame, side="face", cap="head_side")
    s.box(3.4, 13.2, 6.4, 4.2, 16.8, 9.6, "face", cap="head_side")
    s.box(11.8, 13.2, 6.4, 12.6, 16.8, 9.6, "face", cap="head_side")
    # Rune vorn und hinten (leuchtet)
    s.box(7.3, 13.9, 5.86, 8.7, 16.1, 6.02, "rune", glow=True)
    s.box(7.3, 13.9, 9.98, 8.7, 16.1, 10.14, "rune", glow=True)
    # Drachenklauen oben und unten an den Seiten des Kopfs
    s.chain(5.0, 17.3, 30, 16, [0.9, 0.8, 0.6], (-0.4, -0.1), (0.4, 0.1), (0.7, 0.3), "fang", "fang_edge")
    s.chain(11.0, 17.3, -30, -16, [0.9, 0.8, 0.6], (-0.4, -0.1), (0.4, 0.1), (0.7, 0.3), "fang", "fang_edge")
    s.chain(5.0, 12.7, 150, -16, [0.9, 0.7], (-0.4, -0.1), (0.4, 0.1), (0.7, 0.3), "fang", "fang_edge")
    s.chain(11.0, 12.7, -150, 16, [0.9, 0.7], (-0.4, -0.1), (0.4, 0.1), (0.7, 0.3), "fang", "fang_edge")
    # der Dorn obenauf
    s.box(7.2, 17.3, 7.2, 8.8, 18.0, 8.8, "gold", cap="gold_top")
    s.chain(8.0, 18.0, 0, 0, [1.3, 1.2, 1.0, 0.7], (-0.8, -0.08), (0.8, 0.08), (0.8, 0.12), "spike", "spike_edge")
    return s


def keeper_maul() -> Shape:
    """Der Hüterhammer (0.96): ein langer Stiel aus versteinertem Glimmerholz mit Lederwicklung und Mondsteinbändern, unten ein
    Knaufstein, oben der schwere Kopf aus gefügtem Feenstein – vorn und hinten ein leuchtender Mondstein, die Schlagflächen
    grober Fels, obenauf zwei Mondsteinkristalle."""
    s = Shape(REGIONS["keeper_maul"])
    s.box(7.0, -7.0, 7.0, 9.0, -5.4, 9.0, "pommel", cap="pommel_top")
    s.box(7.35, -5.4, 7.35, 8.65, 12.8, 8.65, "handle", side="handle_side", cap="band_top")
    s.box(7.2, -4.2, 7.2, 8.8, 1.8, 8.8, "wrap", cap="band_top")
    for y in (3.6, 7.6, 11.4):
        s.box(7.15, y, 7.15, 8.85, y + 0.8, 8.85, "band", cap="band_top", glow=True)
    # der Kopf: Mittelstück, nach außen die Schlagflächen (etwas schmaler, wie behauen)
    frame = (3.0, 12.6, 13.0, 18.8)
    plate(s, 5.0, 12.6, 11.0, 18.8, 5.6, 10.4, "head", frame, side="face", cap="head_side")
    plate(s, 3.8, 12.9, 5.0, 18.5, 5.8, 10.2, "head", frame, side="face", cap="head_side")
    plate(s, 11.0, 12.9, 12.2, 18.5, 5.8, 10.2, "head", frame, side="face", cap="head_side")
    s.box(3.0, 13.3, 6.2, 3.8, 18.1, 9.8, "face", cap="head_side")
    s.box(12.2, 13.3, 6.2, 13.0, 18.1, 9.8, "face", cap="head_side")
    # der Mondstein vorn und hinten (leuchtet)
    s.box(6.9, 14.3, 5.44, 9.1, 17.1, 5.62, "gem", glow=True)
    s.box(6.9, 14.3, 10.38, 9.1, 17.1, 10.56, "gem", glow=True)
    # zwei Mondsteinkristalle obenauf
    s.chain(6.6, 18.6, 18, -6, [1.5, 1.3, 0.9], (-0.7, -0.15), (0.7, 0.15), (1.1, 0.3), "crystal", "crystal_edge", glow=True)
    s.chain(9.6, 18.6, -24, 8, [1.1, 0.9, 0.6], (-0.55, -0.12), (0.55, 0.12), (0.9, 0.25), "crystal", "crystal_edge", glow=True)
    return s


def sea_witch_staff() -> Shape:
    """Der Hexenstab (0.97): ein Stab aus gebleichtem Treibholz mit Knorren, am Griff mit Tang umwickelt; oben halten drei
    Zinken aus schwarzer Koralle eine leuchtende, meerblaue Kugel; darunter hängen Muscheln und ein Haizahn an Schnüren."""
    s = Shape(REGIONS["sea_witch_staff"])
    s.box(7.1, -8.0, 7.1, 8.9, -6.8, 8.9, "knot", cap="knot_top")
    s.box(7.35, -6.8, 7.35, 8.65, 6.2, 8.65, "shaft", side="shaft_side", cap="knot_top")
    s.box(7.3, 6.0, 7.3, 8.7, 14.8, 8.7, "shaft", side="shaft_side", cap="knot_top", rot=(0.0, 0.0, 3.0), pivot=(8.0, 6.0, 8.0))
    s.box(7.2, -3.0, 7.2, 8.8, 2.2, 8.8, "wrap", cap="knot_top")
    s.box(7.15, 7.6, 7.15, 8.85, 8.7, 8.85, "knot", cap="knot_top")
    # drei Zinken aus schwarzer Koralle, nach außen gebogen
    s.chain(7.0, 14.4, 30, -16, [1.6, 1.5, 1.1], (-0.42, -0.1), (0.42, 0.1), (0.75, 0.25), "coral", "coral_edge")
    s.chain(9.0, 14.4, -30, 16, [1.6, 1.5, 1.1], (-0.42, -0.1), (0.42, 0.1), (0.75, 0.25), "coral", "coral_edge")
    s.chain(8.0, 14.4, 0, 0, [1.4, 1.2, 0.9], (-0.38, -0.1), (0.38, 0.1), (0.7, 0.22), "coral", "coral_edge", z=9.0)
    # die Kugel zwischen den Zinken (leuchtet)
    s.box(6.6, 16.0, 6.6, 9.4, 18.8, 9.4, "orb", cap="orb_side", glow=True)
    # Schnüre mit Muscheln und einem Haizahn
    s.box(9.15, 11.6, 7.9, 9.35, 14.4, 8.1, "cord")
    s.box(8.8, 10.6, 7.6, 9.7, 11.6, 8.4, "shell")
    s.box(6.65, 10.9, 7.9, 6.85, 14.4, 8.1, "cord")
    s.box(6.45, 9.4, 7.85, 7.05, 10.9, 8.15, "tooth")
    return s


def shepherd_crook() -> Shape:
    """Der Hirtenstab (0.98): ein langer Stab aus Sumpfeiche mit eisernem Fuß, am Griff mit Strick umwickelt, oben eine Zwinge
    und der eiserne Haken, der sich nach links krümmt und mit der Spitze wieder nach unten zeigt; an ihr hängt an einer kurzen
    Kette eine Laterne voll Seelenfeuer, an der Zwinge ein Knochen an einer Schnur."""
    s = Shape(REGIONS["shepherd_crook"])
    s.box(7.2, -9.0, 7.2, 8.8, -7.8, 8.8, "iron", cap="iron_top")
    s.box(7.35, -7.8, 7.35, 8.65, 15.2, 8.65, "shaft", side="shaft_side", cap="iron_top")
    s.box(7.2, -3.4, 7.2, 8.8, 2.0, 8.8, "wrap", cap="iron_top")
    s.box(7.25, 6.4, 7.25, 8.75, 7.2, 8.75, "iron", cap="iron_top")
    s.box(7.15, 14.8, 7.15, 8.85, 16.4, 8.85, "iron", cap="iron_top")
    # der Haken
    x, y, _ = s.chain(8.0, 16.2, 0, [0, 38, 44, 46, 44, 36], [1.6, 1.6, 1.5, 1.4, 1.3, 1.1], (-0.6, -0.42), (0.6, 0.42),
                      (1.1, 0.8), "hook", "hook_edge")
    # die Laterne an der Spitze: Kette, Dach, Glas (leuchtet), Boden
    s.box(x - 0.15, y - 1.3, 7.85, x + 0.15, y + 0.1, 8.15, "chain")
    s.box(x - 1.25, y - 1.9, 6.75, x + 1.25, y - 1.3, 9.25, "roof", cap="roof_top")
    s.box(x - 1.0, y - 4.2, 7.0, x + 1.0, y - 1.9, 9.0, "glass", cap="glass_top", glow=True)
    s.box(x - 1.25, y - 4.7, 6.75, x + 1.25, y - 4.2, 9.25, "roof", cap="roof_top")
    # ein Knochen an einer Schnur an der Zwinge
    s.box(8.85, 12.6, 7.9, 9.05, 15.2, 8.1, "cord")
    s.box(8.55, 11.0, 7.7, 9.35, 12.8, 8.3, "bone")
    return s


def minute_hand() -> Shape:
    """Der Minutenzeiger (1.01): der lange Zeiger der großen Uhr als Klinge – ein Messingknauf mit Chronit, ein umwickelter
    Griff, eine gravierte Parierstange mit zwei kleinen Zahnrädern, eine breite Klingenwurzel mit durchbrochenem Ringornament,
    dann die lange, schmale Klinge aus gebläutem Stahl mit leuchtender Chronitader und oben die durchbrochene Rautenspitze."""
    s = Shape(REGIONS["minute_hand"])
    s.box(7.2, -8.4, 7.2, 8.8, -6.9, 8.8, "brass", cap="brass_top")
    s.box(7.55, -8.9, 7.55, 8.45, -8.4, 8.45, "gem", glow=True)
    s.box(7.4, -6.9, 7.4, 8.6, -1.6, 8.6, "grip", cap="brass_top")
    plate(s, 4.8, -1.6, 11.2, -0.5, 7.35, 8.65, "guard", (4.8, -1.6, 11.2, -0.5), side="brass", cap="brass_top")
    s.box(3.4, -2.1, 7.75, 5.0, -0.0, 8.25, "gear", side="brass", cap="brass_top")
    s.box(11.0, -2.1, 7.75, 12.6, -0.0, 8.25, "gear", side="brass", cap="brass_top")
    s.box(7.0, -0.5, 7.55, 9.0, 1.0, 8.45, "brass", cap="brass_top")
    s.box(7.1, 1.0, 7.7, 8.9, 8.2, 8.3, "blade", side="blade_side", cap="blade_side")
    s.box(6.3, 3.6, 7.62, 9.7, 7.0, 8.38, "ring", side="brass", cap="brass_top")
    s.box(7.4, 8.2, 7.76, 8.6, 19.2, 8.24, "narrow", side="blade_side", cap="blade_side")
    s.box(6.2, 19.2, 7.76, 9.8, 24.0, 8.24, "tip", side="blade_side", cap="blade_side")
    s.box(7.86, 1.2, 7.66, 8.14, 19.4, 8.34, "core", glow=True)
    return s


def nightsteel_dagger() -> Shape:
    """Nachtstahl-Dolch: ein Knauf mit Mondstein, der umwickelte Griff, eine geschwungene Parierstange und die kurze, breite
    Klinge mit violett glühender Hohlkehle."""
    s = Shape(REGIONS["nightsteel_dagger"])
    s.box(7.25, -3.4, 7.25, 8.75, -2.2, 8.75, "metal", cap="metal_top")
    s.box(7.6, -3.75, 7.6, 8.4, -3.3, 8.4, "gem", glow=True)
    s.box(7.4, -2.2, 7.4, 8.6, 2.0, 8.6, "grip", cap="metal_top")
    # Parierstange: Mitte und zwei nach oben geschwungene Arme
    plate(s, 6.8, 1.9, 9.2, 2.9, 7.2, 8.8, "guard", (4.6, 1.9, 11.4, 2.9), side="guard_side", cap="guard_side")
    s.chain(6.9, 2.4, 92, 18, [0.9, 0.8, 0.6], (-0.45, -0.25), (0.45, 0.25), (1.2, 0.6), "metal", "metal")
    s.chain(9.1, 2.4, -92, -18, [0.9, 0.8, 0.6], (-0.45, -0.25), (0.45, 0.25), (1.2, 0.6), "metal", "metal")
    # die Klinge: breit am Ansatz, leicht geschwungen
    lengths = [1.6, 1.6, 1.5, 1.3, 1.0, 0.7]
    s.chain(8.0, 2.8, 0, [0, 2, 3, 4, 5, 6], lengths, (-1.25, -0.1), (1.25, 0.1), (0.5, 0.2), "blade", "blade_edge")
    s.chain(8.0, 2.8, 0, [0, 2, 3, 4, 5, 6], lengths[:4], (-0.22, -0.08), (0.22, 0.08), (0.58, 0.3), "fuller", "fuller_side", glow=True)
    return s


def serpent_fang() -> Shape:
    """Der Schlangenzahn (1.10): ein Knauf aus Gold mit einem Smaragd, der Griff aus Jade, ein goldener Kragen, als Parierstange
    zwei goldene Schlangenköpfe, die nach außen blicken, dann der Giftzahn – breit am Ansatz, leicht gekrümmt wie ein Fangzahn,
    mit einer grün leuchtenden Giftrinne; am Knauf hängen zwei Quetzalfedern."""
    s = Shape(REGIONS["serpent_fang"])
    s.box(7.2, -3.6, 7.2, 8.8, -2.4, 8.8, "gold", cap="gold_top")
    s.box(7.55, -4.0, 7.55, 8.45, -3.5, 8.45, "gem", glow=True)
    s.box(7.4, -2.4, 7.4, 8.6, 1.7, 8.6, "grip", cap="gold_top")
    s.box(7.25, 1.6, 7.25, 8.75, 2.2, 8.75, "gold", cap="gold_top")
    # die Parierstange: Mitte und zwei Schlangenköpfe, die sich nach außen und oben recken
    plate(s, 6.6, 2.0, 9.4, 3.0, 7.3, 8.7, "guard", (4.2, 2.0, 11.8, 3.0), side="guard_side", cap="guard_side")
    s.chain(6.7, 2.5, 96, 24, [0.9, 0.8, 0.7], (-0.48, -0.32), (0.48, 0.32), (1.0, 0.75), "gold", "gold")
    s.chain(9.3, 2.5, -96, -24, [0.9, 0.8, 0.7], (-0.48, -0.32), (0.48, 0.32), (1.0, 0.75), "gold", "gold")
    # der Giftzahn: gekrümmt, zur Spitze schmal
    lengths = [1.5, 1.5, 1.5, 1.4, 1.3, 1.1, 0.9, 0.7]
    bends = [0, -3, -4, -5, -6, -7, -8, -9]
    s.chain(8.0, 2.9, 0, bends, lengths, (-1.15, -0.1), (1.15, 0.1), (0.55, 0.2), "blade", "blade_edge")
    s.chain(8.0, 2.9, 0, bends[:6], lengths[:6], (-0.24, -0.06), (0.24, 0.06), (0.62, 0.3), "venom", "venom_side", glow=True)
    # zwei Quetzalfedern hängen vom Knauf
    s.chain(7.6, -3.3, 158, 10, [1.3, 1.2, 1.0], (-0.55, -0.15), (0.55, 0.15), (0.2, 0.2), "feather", "feather_edge")
    s.chain(8.4, -3.3, -166, -7, [1.2, 1.1, 0.9], (-0.5, -0.15), (0.5, 0.15), (0.2, 0.2), "feather", "feather_edge")
    return s


def abyssite_greatsword() -> Shape:
    """Abyssit-Großschwert: ein langer, zweihändiger Griff mit Kristallknauf, die breite Parierstange mit Kristallzacken und die
    gewaltige Klinge mit glühenden Runen in der Mitte."""
    s = Shape(REGIONS["abyssite_greatsword"])
    s.box(7.1, -7.4, 7.1, 8.9, -6.0, 8.9, "metal", cap="metal_top")
    s.box(7.45, -8.2, 7.45, 8.55, -7.3, 8.55, "gem", glow=True)
    s.box(7.35, -6.0, 7.35, 8.65, 1.6, 8.65, "grip", cap="metal_top")
    s.box(7.2, -2.6, 7.2, 8.8, -1.9, 8.8, "metal", cap="metal_top")
    # Parierstange
    frame = (2.6, 1.5, 13.4, 3.3)
    plate(s, 4.2, 1.5, 11.8, 3.3, 7.0, 9.0, "guard", frame, side="guard_side", cap="guard_side")
    plate(s, 2.6, 1.9, 4.2, 3.0, 7.2, 8.8, "guard", frame, side="guard_side", cap="guard_side")
    plate(s, 11.8, 1.9, 13.4, 3.0, 7.2, 8.8, "guard", frame, side="guard_side", cap="guard_side")
    s.chain(3.0, 3.0, 20, 14, [1.0, 0.8, 0.6], (-0.35, -0.08), (0.35, 0.08), (0.7, 0.3), "crystal", "crystal_edge", glow=True)
    s.chain(13.0, 3.0, -20, -14, [1.0, 0.8, 0.6], (-0.35, -0.08), (0.35, 0.08), (0.7, 0.3), "crystal", "crystal_edge", glow=True)
    s.chain(3.0, 1.9, 160, -14, [0.8, 0.6], (-0.3, -0.08), (0.3, 0.08), (0.6, 0.3), "crystal", "crystal_edge", glow=True)
    s.chain(13.0, 1.9, -160, 14, [0.8, 0.6], (-0.3, -0.08), (0.3, 0.08), (0.6, 0.3), "crystal", "crystal_edge", glow=True)
    # die Klinge
    lengths = [3.0, 3.0, 3.0, 3.0, 2.8, 2.2, 1.4]
    s.chain(8.0, 3.2, 0, 0, lengths, (-1.6, -0.15), (1.6, 0.15), (0.6, 0.24), "blade", "blade_edge")
    s.chain(8.0, 3.6, 0, 0, [3.0, 3.0, 3.0, 2.6], (-0.25, -0.18), (0.25, 0.18), (0.66, 0.5), "rune", "rune", glow=True)
    return s


def phase_blade() -> Shape:
    """Die Phasenklinge (1.5): ein langer Griff in violettem Leder mit einem Phasit im Knauf, eine Parierstange aus Purpur mit
    Phasitsplittern an den Enden und die Klinge aus einem einzigen Phasitkristall mit leuchtendem Mittelgrat."""
    s = Shape(REGIONS["phase_blade"])
    s.box(7.4, -8.7, 7.4, 8.6, -7.4, 8.6, "gem", glow=True)
    s.box(7.15, -7.5, 7.15, 8.85, -6.6, 8.85, "metal", cap="metal_top")
    s.box(7.35, -6.6, 7.35, 8.65, 1.5, 8.65, "grip", cap="metal_top")
    s.box(7.2, 0.7, 7.2, 8.8, 1.6, 8.8, "metal", cap="metal_top")
    # die Parierstange
    frame = (2.4, 1.5, 13.6, 3.3)
    plate(s, 4.4, 1.5, 11.6, 3.3, 7.0, 9.0, "guard", frame, side="guard_side", cap="guard_side")
    plate(s, 2.4, 1.9, 4.4, 3.0, 7.2, 8.8, "guard", frame, side="guard_side", cap="guard_side")
    plate(s, 11.6, 1.9, 13.6, 3.0, 7.2, 8.8, "guard", frame, side="guard_side", cap="guard_side")
    s.chain(2.8, 3.0, 26, 12, [1.3, 1.0, 0.7], (-0.4, -0.1), (0.4, 0.1), (0.7, 0.3), "shard", "shard_edge", glow=True)
    s.chain(13.2, 3.0, -26, -12, [1.3, 1.0, 0.7], (-0.4, -0.1), (0.4, 0.1), (0.7, 0.3), "shard", "shard_edge", glow=True)
    s.chain(2.8, 1.9, 154, -12, [0.9, 0.6], (-0.3, -0.08), (0.3, 0.08), (0.6, 0.3), "shard", "shard_edge", glow=True)
    s.chain(13.2, 1.9, -154, 12, [0.9, 0.6], (-0.3, -0.08), (0.3, 0.08), (0.6, 0.3), "shard", "shard_edge", glow=True)
    # die Klinge und ihr leuchtender Mittelgrat
    lengths = [3.2, 3.2, 3.2, 3.2, 3.0, 2.4, 1.4]
    s.chain(8.0, 3.2, 0, 0, lengths, (-1.8, -0.2), (1.8, 0.2), (0.62, 0.24), "blade", "blade_edge")
    s.chain(8.0, 3.6, 0, 0, [3.2, 3.2, 3.2, 3.0, 2.4], (-0.28, -0.22), (0.28, 0.22), (0.68, 0.5), "ridge", "ridge", glow=True)
    return s


def tuning_fork() -> Shape:
    """Die Stimmgabel (1.7): ein Griff in seegrünem Leder mit einem Hallkristall im Knauf, ein Kragen und ein Hals aus
    grün angelaufenem Kupfer, der Bügel und zwei lange Zinken aus Hallkristall, um die leuchtende Ringe laufen."""
    s = Shape(REGIONS["tuning_fork"])
    s.box(7.4, -8.6, 7.4, 8.6, -7.4, 8.6, "gem", glow=True)
    s.box(7.15, -7.5, 7.15, 8.85, -6.6, 8.85, "copper", cap="copper_top")
    s.box(7.35, -6.6, 7.35, 8.65, 2.0, 8.65, "grip", cap="copper_top")
    s.box(7.15, 1.4, 7.15, 8.85, 2.4, 8.85, "copper", cap="copper_top")
    s.box(7.5, 2.4, 7.5, 8.5, 4.8, 8.5, "copper", cap="copper_top")
    # der Bügel (ein flaches U) und die Zinken
    frame = (4.6, 4.6, 11.4, 6.0)
    plate(s, 4.6, 4.6, 11.4, 6.0, 7.35, 8.65, "yoke", frame, side="yoke_side", cap="yoke_side")
    for x0 in (4.6, 9.9):
        s.box(x0, 6.0, 7.4, x0 + 1.5, 19.2, 8.6, "prong", side="prong_edge", cap="prong_edge")
        s.box(x0 + 0.2, 19.2, 7.6, x0 + 1.3, 20.2, 8.4, "prong", side="prong_edge", cap="prong_edge")
        for y in (9.0, 13.0, 17.0):
            s.box(x0 - 0.12, y, 7.28, x0 + 1.62, y + 0.6, 8.72, "ring", glow=True)
    return s


def spike(s: Shape, c: tuple[float, float, float], d: tuple[float, float, float], base: float,
          lengths: tuple[float, ...] = (0.75, 0.7, 0.55), widths: tuple[float, ...] = (1.0, 0.62, 0.26)) -> None:
    """Ein vierkantiger Stachel aus sich verjüngenden Quadern: sein Fuß liegt im Abstand base vom Mittelpunkt c, er weist in
    Richtung d (gedreht zuerst um X, dann um Z, um den Mittelpunkt)."""
    import math
    dx, dy, dz = d
    n = math.sqrt(dx * dx + dy * dy + dz * dz)
    dx, dy, dz = dx / n, dy / n, dz / n
    rx = math.degrees(math.asin(max(-1.0, min(1.0, dz))))
    rz = math.degrees(math.atan2(-dx, dy))
    cx, cy, cz = c
    y = cy + base
    k = len(lengths)
    for i, (ln, w) in enumerate(zip(lengths, widths)):
        uv = _sub(s.r("spike"), 0.0, (k - 1 - i) / k, 1.0, (k - i) / k)
        top = y + ln + (0.12 if i < k - 1 else 0.0)
        s.box(cx - w / 2, y, cz - w / 2, cx + w / 2, top, cz + w / 2, uv, side=uv, cap="spike_tip", rot=(rx, 0.0, rz), pivot=c)
        y += ln


def morning_star() -> Shape:
    """Der Morgenstern (1.8): ein goldener Knopf und ein Knauf aus geschwärztem Eisen, der Stiel aus Eschenholz, unten in schwarzem
    Leder mit goldener Naht, darüber ein goldener Kragen und Eisenbänder, unter dem Kopf Eisenschienen mit Nieten; der Kopf eine
    Kugel aus geschwärztem Eisen (drei sich kreuzende Quader und ein Würfel für die Rundung) mit siebzehn Stacheln aus blankem
    Stahl – einer obenauf, vier ringsum, vier schräg dazwischen, je vier schräg nach oben und nach unten."""
    s = Shape(REGIONS["morning_star"])
    s.box(7.55, -7.2, 7.55, 8.45, -6.6, 8.45, "gold", cap="gold_top")
    s.box(7.15, -6.6, 7.15, 8.85, -5.3, 8.85, "iron", cap="iron_top")
    s.box(7.4, -5.3, 7.4, 8.6, 14.4, 8.6, "shaft", side="shaft_side", cap="iron_top")
    s.box(7.25, -5.0, 7.25, 8.75, 1.4, 8.75, "wrap", cap="iron_top")
    s.box(7.2, 1.4, 7.2, 8.8, 2.1, 8.8, "gold", cap="gold_top")
    for y in (5.0, 8.2):
        s.box(7.28, y, 7.28, 8.72, y + 0.6, 8.72, "iron", cap="iron_top")
    s.box(7.22, 10.6, 7.22, 8.78, 14.2, 8.78, "langet", cap="iron_top")
    # der Kopf
    c = (8.0, 16.6, 8.0)
    cx, cy, cz = c
    s.box(cx - 2.6, cy - 1.6, cz - 1.7, cx + 2.6, cy + 1.6, cz + 1.7, "head", side="head_side", cap="head_top")
    s.box(cx - 1.7, cy - 2.6, cz - 1.7, cx + 1.7, cy + 2.6, cz + 1.7, "head", side="head_side", cap="head_top")
    s.box(cx - 1.7, cy - 1.6, cz - 2.6, cx + 1.7, cy + 1.6, cz + 2.6, "head", side="head_side", cap="head_top")
    s.box(cx - 2.15, cy - 2.15, cz - 2.15, cx + 2.15, cy + 2.15, cz + 2.15, "head", side="head_side", cap="head_top")
    # die Stacheln
    spike(s, c, (0, 1, 0), 2.4, (0.85, 0.8, 0.65), (1.1, 0.68, 0.28))
    for d in ((1, 0, 0), (-1, 0, 0), (0, 0, 1), (0, 0, -1)):
        spike(s, c, d, 2.4)
    for d in ((1, 0, 1), (-1, 0, 1), (1, 0, -1), (-1, 0, -1)):
        spike(s, c, d, 2.75, (0.65, 0.6, 0.5), (0.9, 0.56, 0.24))
    for d in ((1, 1, 0), (-1, 1, 0), (0, 1, 1), (0, 1, -1)):
        spike(s, c, d, 2.75, (0.65, 0.6, 0.5), (0.9, 0.56, 0.24))
    for d in ((1, -1, 0), (-1, -1, 0), (0, -1, 1), (0, -1, -1)):
        spike(s, c, d, 2.75, (0.6, 0.55, 0.45), (0.86, 0.54, 0.22))
    return s


def steel_double_axe() -> Shape:
    """Stahl-Doppelaxt: ein Eichenstiel mit Lederwicklung, oben zwei halbmondförmige Blätter (links und rechts) und eine kurze
    Spitze."""
    s = Shape(REGIONS["steel_double_axe"])
    s.box(7.3, -5.6, 7.3, 8.7, -4.6, 8.7, "steel", cap="steel_top")
    s.box(7.45, -4.6, 7.45, 8.55, 16.4, 8.55, "shaft", side="shaft_side", cap="steel_top")
    s.box(7.3, -2.8, 7.3, 8.7, 2.2, 8.7, "wrap", cap="steel_top")
    s.box(7.1, 10.2, 7.1, 8.9, 16.0, 8.9, "steel", cap="steel_top")
    frame = (1.4, 8.2, 14.6, 18.0)
    for (x0, x1, y0, y1) in ((5.6, 7.1, 11.0, 15.2), (4.2, 5.6, 10.2, 16.0), (3.0, 4.2, 9.3, 16.9), (2.2, 3.0, 8.7, 17.5)):
        plate(s, x0, y0, x1, y1, 7.75, 8.25, "blade", frame, side="blade_side", cap="blade_side")
        plate(s, 16.0 - x1, y0, 16.0 - x0, y1, 7.75, 8.25, "blade", frame, side="blade_side", cap="blade_side")
    plate(s, 1.6, 8.6, 2.2, 17.6, 7.82, 8.18, "edge", frame, side="edge", cap="edge")
    plate(s, 13.8, 8.6, 14.4, 17.6, 7.82, 8.18, "edge", frame, side="edge", cap="edge")
    s.chain(8.0, 16.0, 0, 0, [1.2, 1.0, 0.7], (-0.8, -0.08), (0.8, 0.08), (0.6, 0.2), "spike", "spike_edge")
    return s


# =============================================================================
# Spitzhacken
# =============================================================================

def _pick_head(s: Shape, y: float, reach: float, left: bool, right: bool, lengths: list[float], bend: float, width: float,
               region: str = "pick", edge: str = "pick_edge", vein: str | None = "vein", vein_edge: str = "vein_edge") -> None:
    """Die beiden Arme einer Spitzhacke: von der Mitte (x = 8, Höhe y) nach links und rechts, nach unten gebogen und spitz."""
    for sign, on in ((1, left), (-1, right)):
        if not on:
            continue
        s.chain(8.0, y, sign * 90, sign * bend, lengths, (-width, -0.1), (width, 0.1), (0.9, 0.3), region, edge)
        if vein:
            s.chain(8.0, y, sign * 90, sign * bend, lengths[:-1], (-0.18, -0.1), (0.18, 0.1), (0.98, 0.5), vein, vein_edge, glow=True)


def miner_hammer() -> Shape:
    """Bergmannshammer: ein Eschenstiel mit Stahlbändern, oben ein schwerer, breiter Hammerkopf mit zwei Schlagflächen und
    Dornen an den Ecken – er bricht drei mal drei Blöcke auf einmal."""
    s = Shape(REGIONS["miner_hammer"])
    s.box(7.3, -5.4, 7.3, 8.7, -4.4, 8.7, "steel", cap="steel_top")
    s.box(7.45, -4.4, 7.45, 8.55, 13.0, 8.55, "handle", side="handle_side", cap="steel_top")
    s.box(7.3, -2.8, 7.3, 8.7, 2.2, 8.7, "wrap", cap="steel_top")
    s.box(7.25, 6.0, 7.25, 8.75, 6.8, 8.75, "steel", cap="steel_top")
    frame = (2.4, 11.6, 13.6, 17.0)
    plate(s, 4.4, 11.6, 11.6, 17.0, 5.9, 10.1, "head", frame, side="face", cap="head_top")
    plate(s, 2.4, 12.0, 4.4, 16.6, 6.2, 9.8, "head", frame, side="face", cap="head_top")
    plate(s, 11.6, 12.0, 13.6, 16.6, 6.2, 9.8, "head", frame, side="face", cap="head_top")
    for (x, z) in ((5.2, 5.78), (10.2, 5.78), (5.2, 10.02), (10.2, 10.02)):
        s.box(x, 12.4, z, x + 0.6, 13.0, z + 0.2, "brass")
        s.box(x, 15.6, z, x + 0.6, 16.2, z + 0.2, "brass")
    # Dorne an den oberen Ecken
    s.chain(3.0, 16.6, 38, 10, [0.9, 0.7], (-0.35, -0.08), (0.35, 0.08), (0.7, 0.3), "pick", "pick_edge")
    s.chain(13.0, 16.6, -38, -10, [0.9, 0.7], (-0.35, -0.08), (0.35, 0.08), (0.7, 0.3), "pick", "pick_edge")
    s.box(7.1, 17.0, 7.1, 8.9, 17.6, 8.9, "steel", cap="steel_top")
    return s


def ember_pickaxe() -> Shape:
    """Glutspitzhacke: ein verkohlter Stiel mit goldenen Fassungen, oben eine Fassung mit Glutstein und zwei Picken aus Draconium,
    in deren Mitte eine Glutader leuchtet."""
    s = Shape(REGIONS["ember_pickaxe"])
    s.box(7.3, -5.4, 7.3, 8.7, -4.4, 8.7, "gold", cap="gold_top")
    s.box(7.45, -4.4, 7.45, 8.55, 15.4, 8.55, "handle", side="handle_side", cap="gold_top")
    s.box(7.3, -2.8, 7.3, 8.7, 2.2, 8.7, "wrap", cap="gold_top")
    _pick_head(s, 14.2, 6.0, True, True, [1.6, 1.5, 1.4, 1.2, 0.9], 13.0, 0.95)
    plate(s, 6.7, 13.0, 9.3, 15.6, 6.8, 9.2, "core", (6.7, 13.0, 9.3, 15.6), side="core_side", cap="core_side")
    s.box(7.55, 13.75, 6.66, 8.45, 14.85, 6.82, "stone", glow=True)
    s.box(7.55, 13.75, 9.18, 8.45, 14.85, 9.34, "stone", glow=True)
    s.box(7.2, 15.6, 7.2, 8.8, 16.2, 8.8, "gold", cap="gold_top")
    return s


def vein_breaker() -> Shape:
    """Adernbrecher: ein Stiel mit Nachtstahlbändern, oben eine lange, nach unten gebogene Picke und ein kurzer Haken nach hinten,
    in der Fassung eingelassene Erzkristalle und violett glühende Adern."""
    s = Shape(REGIONS["vein_breaker"])
    s.box(7.3, -5.4, 7.3, 8.7, -4.4, 8.7, "metal", cap="metal_top")
    s.box(7.45, -4.4, 7.45, 8.55, 15.4, 8.55, "handle", side="handle_side", cap="metal_top")
    s.box(7.3, -2.8, 7.3, 8.7, 2.2, 8.7, "wrap", cap="metal_top")
    s.box(7.3, 8.0, 7.3, 8.7, 8.7, 8.7, "metal", cap="metal_top")
    # lange Picke nach links, kurzer Haken nach rechts
    s.chain(8.0, 14.2, 90, 12, [1.8, 1.7, 1.6, 1.4, 1.1, 0.8], (-1.0, -0.1), (1.0, 0.1), (0.9, 0.3), "pick", "pick_edge")
    s.chain(8.0, 14.2, 90, 12, [1.8, 1.7, 1.6, 1.4, 1.1], (-0.18, -0.1), (0.18, 0.1), (0.98, 0.5), "vein", "vein_edge", glow=True)
    s.chain(8.0, 14.2, -90, -22, [1.4, 1.1, 0.8], (-0.9, -0.1), (0.9, 0.1), (0.9, 0.35), "pick", "pick_edge")
    plate(s, 6.6, 12.8, 9.4, 15.8, 6.7, 9.3, "core", (6.6, 12.8, 9.4, 15.8), side="core_side", cap="core_side")
    for z0, z1 in ((6.56, 6.72), (9.28, 9.44)):
        s.box(7.1, 13.4, z0, 8.0, 14.4, z1, "ore", glow=True)
        s.box(8.2, 14.4, z0, 8.9, 15.2, z1, "ore", glow=True)
    s.box(7.2, 15.8, 7.2, 8.8, 16.4, 8.8, "metal", cap="metal_top")
    return s


def ore_seeker() -> Shape:
    """Erzspürer: ein Stiel mit Abyssitbändern, oben zwei schlanke, gebogene Picken mit Lichtlinien und in der Mitte ein großes
    Auge aus Abyssitkristall, das durch Stein sieht."""
    s = Shape(REGIONS["ore_seeker"])
    s.box(7.3, -5.4, 7.3, 8.7, -4.4, 8.7, "metal", cap="metal_top")
    s.box(7.45, -4.4, 7.45, 8.55, 14.6, 8.55, "handle", side="handle_side", cap="metal_top")
    s.box(7.3, -2.8, 7.3, 8.7, 2.2, 8.7, "wrap", cap="metal_top")
    s.box(7.3, 8.0, 7.3, 8.7, 8.7, 8.7, "metal", cap="metal_top")
    _pick_head(s, 14.6, 6.0, True, True, [1.6, 1.5, 1.4, 1.2, 1.0, 0.7], 11.0, 0.8)
    plate(s, 6.3, 12.6, 9.7, 16.6, 6.6, 9.4, "core", (6.3, 12.6, 9.7, 16.6), side="core_side", cap="core_side")
    s.box(6.95, 13.3, 6.42, 9.05, 15.9, 6.62, "eye", glow=True)
    s.box(6.95, 13.3, 9.38, 9.05, 15.9, 9.58, "eye", glow=True)
    s.box(7.4, 16.6, 7.4, 8.6, 17.4, 8.6, "metal", cap="metal_top")
    s.chain(8.0, 17.4, 0, 0, [0.7, 0.5], (-0.4, -0.08), (0.4, 0.08), (0.6, 0.2), "pick", "pick_edge")
    return s


ITEMS = {
    "STEEL_HALBERD": ("steel_halberd", steel_halberd),
    "DRACONIUM_WARHAMMER": ("draconium_warhammer", draconium_warhammer),
    "KEEPER_MAUL": ("keeper_maul", keeper_maul),
    "SEA_WITCH_STAFF": ("sea_witch_staff", sea_witch_staff),
    "SHEPHERD_CROOK": ("shepherd_crook", shepherd_crook),
    "MINUTE_HAND": ("minute_hand", minute_hand),
    "NIGHTSTEEL_DAGGER": ("nightsteel_dagger", nightsteel_dagger),
    "SERPENT_FANG": ("serpent_fang", serpent_fang),
    "ABYSSITE_GREATSWORD": ("abyssite_greatsword", abyssite_greatsword),
    "PHASE_BLADE": ("phase_blade", phase_blade),
    "TUNING_FORK": ("tuning_fork", tuning_fork),
    "MORNING_STAR": ("morning_star", morning_star),
    "STEEL_DOUBLE_AXE": ("steel_double_axe", steel_double_axe),
    "MINER_HAMMER": ("miner_hammer", miner_hammer),
    "EMBER_PICKAXE": ("ember_pickaxe", ember_pickaxe),
    "VEIN_BREAKER": ("vein_breaker", vein_breaker),
    "ORE_SEEKER": ("ore_seeker", ore_seeker),
}


# =============================================================================
# Java und Vorschau
# =============================================================================

def java() -> str:
    out = [
        "package io.github.sirschrolli.kingdomsatwar.datagen;",
        "",
        "/**",
        " * Die Teile der Modelle der Waffenkammer (0.84), senkrecht um x = z = 8 gebaut. Je Teil: von x, y, z, bis x, y, z;",
        " * UV vorn/hinten (u0, v0, u1, v1), UV der Seiten, UV oben/unten; Leuchten (0/1); eigene Drehung (Grad um X, Y, Z) und ihr",
        " * Drehpunkt (x, y, z).",
        " *",
        " * <p>ERZEUGT von tools/armory_models.py – nicht von Hand ändern.",
        " */",
        "final class ArmoryShapes {",
        "    private ArmoryShapes() {}",
    ]
    # je Gegenstand eine eigene Methode (der statische Block einer Klasse darf höchstens 64 KB groß sein)
    for const, (name, fn) in ITEMS.items():
        out.append("")
        out.append(f"    /** {name} */")
        out.append(f"    static final float[][] {const} = {name}();")
    for const, (name, fn) in ITEMS.items():
        out.append("")
        out.append(f"    private static float[][] {name}() {{")
        out.append("        return new float[][] {")
        for p in fn().parts:
            vals = [*p.lo, *p.hi, *p.uv, *p.side, *p.cap, 1.0 if p.glow else 0.0, *p.rot, *p.pivot]
            out.append("                {" + ", ".join(mg._f(v) for v in vals) + "},")
        out.append("        };")
        out.append("    }")
    out.append("}")
    return "\n".join(out) + "\n"


def debug_atlas(name: str):
    """Ein Atlas zum Prüfen der Gestalt: jeder Bereich in einer eigenen, flachen Farbe."""
    from PIL import Image, ImageDraw
    img = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    glow = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    d, g = ImageDraw.Draw(img), ImageDraw.Draw(glow)
    palette = [(200, 60, 60), (60, 160, 60), (60, 90, 200), (210, 170, 50), (160, 70, 190), (60, 180, 180), (230, 120, 40),
               (150, 150, 150), (240, 240, 240), (100, 60, 30), (230, 90, 160), (120, 200, 90), (40, 40, 60), (250, 220, 150)]
    for i, (key, (u0, v0, u1, v1)) in enumerate(REGIONS[name].items()):
        col = palette[i % len(palette)]
        box = (round(u0 * 2), round(v0 * 2), round(u1 * 2) - 1, round(v1 * 2) - 1)
        d.rectangle(box, fill=col + (255,))
        g.rectangle(box, fill=col + (255,))
        d.rectangle((box[0], box[1], box[2], box[1]), fill=tuple(min(255, c + 50) for c in col) + (255,))
    return img, glow


def preview(path: str, debug: bool) -> None:
    from PIL import Image
    if not debug:
        import armory_textures as tex
    views = [((0, 0), True), ((-35, 18), True), ((90, 0), False), ((30, -20), False)]
    tiles = []
    for const, (name, fn) in ITEMS.items():
        atlas, glow = debug_atlas(name) if debug else tex.atlas(name)
        tiles.append([mg.render(fn(), atlas, glow, v, size=360, scale=10.0, tilt=t) for v, t in views])
    w, h = tiles[0][0].size
    sheet = Image.new("RGBA", (w * len(views), h * len(tiles)), (40, 40, 40, 255))
    for j, row in enumerate(tiles):
        for i, t in enumerate(row):
            sheet.paste(t, (i * w, j * h))
    sheet.save(path)
    print("Vorschau:", path)


def main(argv: list[str]) -> None:
    if "--preview" in argv:
        preview(argv[argv.index("--preview") + 1], "--debug" in argv)
        return
    JAVA.write_text(java(), encoding="utf-8")
    print(f"geschrieben: {JAVA.relative_to(ROOT)}")


if __name__ == "__main__":
    main(sys.argv[1:])
