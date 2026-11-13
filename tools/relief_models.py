#!/usr/bin/env python3
"""
Plastische Modelle aus Gegenstandsbildern ("Relief", seit 0.80).

Ein flaches Bild (16 x 16) wird zu einem Körper mit Tiefe: Jedes sichtbare Pixel wird ein Quader, und je weiter es vom Rand
der Form entfernt ist, desto dicker ist er – der Rand bleibt ein Pixel stark (wie bei den Gegenständen von Minecraft), die
Mitte wölbt sich bis zu vier Pixel. So liegt ein Totem wie ein geschnitztes Götzenbild in der Hand und ein Apfel wie eine
Frucht, statt wie ein Stück Papier. Vorder- und Rückseite zeigen das Bild (hinten gespiegelt, wie bei Minecraft), die Kanten
und Stufen die Farbe des jeweiligen Randpixels.

Gleich dicke Pixel werden zu möglichst großen Rechtecken zusammengefasst; Seitenflächen entstehen nur, wo wirklich eine
Kante frei liegt. Das Modell erbt von "minecraft:item/generated" (oder "item/handheld") – also dieselbe Haltung in Hand, am
Boden und im Rahmen – und ersetzt nur die Gestalt. Im Inventar bleibt das flache Bild (siehe ModModelProvider.relief).

Aufruf:  python3 tools/relief_models.py            schreibt alle Modelle nach src/main/resources/.../models/item/relief/
         python3 tools/relief_models.py --check    prüft nur (Zahl der Quader)
"""
from __future__ import annotations

import json
import os
import sys

from PIL import Image

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
TEXTURES = os.path.join(ROOT, "src/main/resources/assets/kingdomsatwar/textures")
MODELS = os.path.join(ROOT, "src/main/resources/assets/kingdomsatwar/models/item/relief")

# Gegenstand -> (Elternmodell, größte halbe Dicke in Pixeln, Zuwachs je Pixel Abstand vom Rand)
ITEMS: dict[str, tuple[str, float, float]] = {
    "totem_ember": ("minecraft:item/generated", 2.0, 0.5),
    "totem_frost": ("minecraft:item/generated", 2.0, 0.5),
    "totem_storm": ("minecraft:item/generated", 2.0, 0.5),
    "totem_stone": ("minecraft:item/generated", 2.5, 0.5),
    "totem_grove": ("minecraft:item/generated", 2.0, 0.5),
    "totem_tide": ("minecraft:item/generated", 2.0, 0.5),
    "totem_shadow": ("minecraft:item/generated", 2.0, 0.5),
    "totem_void": ("minecraft:item/generated", 2.0, 0.5),
    "totem_creator": ("minecraft:item/generated", 2.0, 0.5),
    "dragon_apple": ("minecraft:item/generated", 3.5, 0.75),
    "star_apple": ("minecraft:item/generated", 3.5, 0.75),
    "shadow_apple": ("minecraft:item/generated", 3.5, 0.75),
    "immortal_apple": ("minecraft:item/generated", 3.5, 0.75),
    # Fischbeutel (0.81): prall gefüllt
    "fish_bag": ("minecraft:item/generated", 3.0, 0.75),
    # der Sternenbogen (0.99): ruhend und drei Stufen gespannt, gehalten wie der Bogen von Minecraft
    "star_bow": ("minecraft:item/bow", 0.75, 0.5),
    "star_bow_pulling_0": ("minecraft:item/bow", 0.75, 0.5),
    "star_bow_pulling_1": ("minecraft:item/bow", 0.75, 0.5),
    "star_bow_pulling_2": ("minecraft:item/bow", 0.75, 0.5),
}

# Seit 0.88: die übrigen Gegenstände, die man in die Hand nimmt – Barren, Steine, Schuppen, Monsterteile, Runen, Münzen,
# Essen und Trinken, Hörner, Kronen, Laternen … (nicht: Fische, Spawn-Eier, Pflanzen, Saatgut, Bausätze, Pläne und Papier).
# Gruppe -> (größte halbe Dicke, Zuwachs je Pixel Abstand vom Rand), dann die Gegenstände der Gruppe.
GROUPS: dict[tuple[float, float], list[str]] = {
    (1.25, 0.5): [  # Barren und Erze: kantig, gleichmäßig dick
        "abyssite_ingot", "moonsilver_ingot", "nightsteel_ingot", "steel_ingot", "raw_moonsilver", "raw_nightsteel", "steel_blend",
        "sky_iron_ingot", "raw_sky_iron",
        "draconium", "konterit", "carbon", "wyvernit", "aetherit", "moonsilver_nugget", "reinforced_handle", "siege_stone",
    ],
    (1.5, 0.5): [   # Steine, Kristalle, Splitter
        "gemstone", "abyssite_shard", "star_shard", "void_shard", "glitch_shard", "night_crystal_shard", "soul_crystal", "moonstone", "aquamarine", "dusk_quartz",
        "chronit", "fire_opal", "phasit", "echo_crystal", "mana_crystal", "heart_crystal",
        "molten_glob", "shadow_heart", "bog_heart", "aether_core", "shadow_essence", "creator_spark",
        "earth_seal_shard", "fire_seal_shard", "ice_seal_shard", "lightning_seal_shard", "nature_seal_shard", "water_seal_shard",
    ],
    (1.0, 0.5): [   # flache Dinge: Schuppen, Münzen, Runen, Tafeln, Muscheln
        "earth_dragon_scale", "fire_dragon_scale", "ice_dragon_scale", "lightning_dragon_scale", "nature_dragon_scale",
        "water_dragon_scale", "ember_scale", "bronze_coin", "silver_coin", "gold_coin",
        "earth_rune", "fire_rune", "ice_rune", "lightning_rune", "nature_rune", "water_rune",
        "earth_spell_tablet", "fire_spell_tablet", "ice_spell_tablet", "lightning_spell_tablet", "nature_spell_tablet",
        "water_spell_tablet", "creator_halo", "creator_rings",
    ],
    (1.75, 0.5): [  # Essen und Trinken
        "apple_pie", "bread_roll", "cooked_crab_meat", "cooked_venison", "crab_meat", "dusk_bread", "garlic_bread", "meat_pie",
        "venison", "ale", "mead", "wine", "vegetable_stew", "dragon_food",
    ],
    (2.0, 0.6): [   # Rundes: Gemüse und Früchte
        "cabbage", "tomato", "moth_cocoon",
    ],
    (1.25, 0.45): [  # Gerät und Schmuck
        "ember_fang", "werewolf_claw", "crab_claw", "shark_tooth", "bog_root", "dragon_horn", "tide_horn", "gemstone_crown",
        "shattered_crown", "rift_key", "angler_lure",
        "chronicle",
    ],
}
for (_half, _slope), _names in GROUPS.items():
    for _name in _names:
        ITEMS.setdefault(_name, ("minecraft:item/generated", _half, _slope))

# Aus eigener Sicht wären die dicken Gegenstände sonst klobig: etwas kleiner und tiefer in der Hand (Minecraft: 0.68)
_FIRST_PERSON = {
    "firstperson_righthand": {"rotation": [0, -90, 25], "translation": [1.13, 2.8, 1.13], "scale": [0.58, 0.58, 0.58]},
    "firstperson_lefthand": {"rotation": [0, 90, -25], "translation": [1.13, 2.8, 1.13], "scale": [0.58, 0.58, 0.58]},
}


def held(rotation, translation, scale) -> dict:
    """Haltung in der Hand eines anderen (dritte Person); die linke Hand spiegelt Minecraft selbst."""
    return {"thirdperson_righthand": {"rotation": list(rotation), "translation": list(translation), "scale": [scale, scale, scale]}}


# In der Hand eines anderen liegen flache Gegenstände bei Minecraft waagerecht (Spitze nach vorn). Ein Götzenbild hält man
# aufrecht vor sich, das Gesicht nach vorn; einen Apfel ebenso, nur kleiner. (Eigene Sicht, Boden und Rahmen erbt das Modell.)
TOTEM_HELD = held((78, 0, 0), (0, 3.5, 1.5), 0.6)
APPLE_HELD = held((75, 0, 0), (0, 2.5, 0.5), 0.45)
DISPLAY: dict[str, dict] = {name: (TOTEM_HELD if name.startswith("totem_") else APPLE_HELD) for name in ITEMS}
# Der Fischbeutel ist prall: In der eigenen Hand säße er sonst zu groß im Bild – etwas kleiner und tiefer, in beiden Händen.
DISPLAY["fish_bag"] = dict(held((75, 0, 0), (0, 2.5, 0.5), 0.5), **{
    "firstperson_righthand": {"rotation": [0, -90, 25], "translation": [1.13, 2.4, 1.13], "scale": [0.54, 0.54, 0.54]},
    "firstperson_lefthand": {"rotation": [0, 90, -25], "translation": [1.13, 2.4, 1.13], "scale": [0.54, 0.54, 0.54]},
})


for _names in GROUPS.values():
    for _name in _names:
        DISPLAY[_name] = {**DISPLAY.get(_name, {}), **_FIRST_PERSON}


def distance(mask: set[tuple[int, int]]) -> dict[tuple[int, int], float]:
    """Abstand jedes Pixels zum Rand der Form (Randpixel = 1; Chamfer 3-4)."""
    inf = 10 ** 6
    d = {p: inf for p in mask}
    for (x, y) in mask:
        if any((x + dx, y + dy) not in mask for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1))):
            d[(x, y)] = 3
    changed = True
    while changed:
        changed = False
        for (x, y) in mask:
            best = d[(x, y)]
            for dx, dy, c in ((1, 0, 3), (-1, 0, 3), (0, 1, 3), (0, -1, 3), (1, 1, 4), (-1, -1, 4), (1, -1, 4), (-1, 1, 4)):
                q = (x + dx, y + dy)
                if q in d and d[q] + c < best:
                    best = d[q] + c
            if best < d[(x, y)]:
                d[(x, y)] = best
                changed = True
    return {p: v / 3.0 for p, v in d.items()}


def halves(img: Image.Image, max_half: float, slope: float) -> dict[tuple[int, int], float]:
    """Halbe Dicke je sichtbarem Pixel (in Pixeln, auf halbe Pixel gerundet)."""
    px = img.load()
    mask = {(x, y) for y in range(img.height) for x in range(img.width) if px[x, y][3] > 24}
    dist = distance(mask)
    out = {}
    for p, d in dist.items():
        half = min(max_half, 0.5 + (d - 1.0) * slope)
        out[p] = max(0.5, round(half * 2.0) / 2.0)
    return out


def rectangles(depth: dict[tuple[int, int], float]) -> list[tuple[int, int, int, int, float]]:
    """Fasst gleich dicke Pixel zu Rechtecken zusammen: (x0, y0, x1, y1, halbe Dicke), x1/y1 ausschließlich."""
    seen: set[tuple[int, int]] = set()
    out = []
    for (x, y) in sorted(depth, key=lambda p: (p[1], p[0])):
        if (x, y) in seen:
            continue
        h = depth[(x, y)]
        x1 = x
        while (x1, y) in depth and (x1, y) not in seen and depth[(x1, y)] == h:
            x1 += 1
        y1 = y + 1
        while all((xx, y1) in depth and (xx, y1) not in seen and depth[(xx, y1)] == h for xx in range(x, x1)):
            y1 += 1
        for yy in range(y, y1):
            for xx in range(x, x1):
                seen.add((xx, yy))
        out.append((x, y, x1, y1, h))
    return out


def model(name: str, img: Image.Image, parent: str, max_half: float, slope: float) -> dict:
    depth = halves(img, max_half, slope)
    scale = 16.0 / img.width  # Bildpixel -> Modelleinheiten (und UV)
    elements = []
    for (x0, y0, x1, y1, h) in rectangles(depth):
        def lower(pts) -> bool:
            """Liegt an dieser Seite irgendwo eine Kante frei (Nachbar fehlt oder ist dünner)?"""
            return any(depth.get(p, 0.0) < h for p in pts)

        u0, v0, u1, v1 = x0 * scale, y0 * scale, x1 * scale, y1 * scale
        faces = {
            "south": {"uv": [u0, v0, u1, v1], "texture": "#layer0"},
            "north": {"uv": [u1, v0, u0, v1], "texture": "#layer0"},
        }
        if lower([(x0 - 1, y) for y in range(y0, y1)]):
            faces["west"] = {"uv": [u0, v0, u0 + scale, v1], "texture": "#layer0"}
        if lower([(x1, y) for y in range(y0, y1)]):
            faces["east"] = {"uv": [u1 - scale, v0, u1, v1], "texture": "#layer0"}
        if lower([(x, y0 - 1) for x in range(x0, x1)]):
            faces["up"] = {"uv": [u0, v0, u1, v0 + scale], "texture": "#layer0"}
        if lower([(x, y1) for x in range(x0, x1)]):
            faces["down"] = {"uv": [u0, v1 - scale, u1, v1], "texture": "#layer0"}
        elements.append({
            "from": [x0 * scale, 16.0 - y1 * scale, 8.0 - h * scale],
            "to": [x1 * scale, 16.0 - y0 * scale, 8.0 + h * scale],
            "faces": faces,
        })
    texture = f"kingdomsatwar:item/{name}"
    out = {"parent": parent, "textures": {"layer0": texture, "particle": texture}}
    if name in DISPLAY:
        out["display"] = DISPLAY[name]
    out["elements"] = elements
    return out


def build() -> dict[str, dict]:
    out = {}
    for name, (parent, max_half, slope) in ITEMS.items():
        img = Image.open(os.path.join(TEXTURES, "item", name + ".png")).convert("RGBA")
        out[name] = model(name, img, parent, max_half, slope)
    return out


JAVA = os.path.join(ROOT, "src/main/java/io/github/sirschrolli/kingdomsatwar/datagen/ReliefNames.java")


def java() -> str:
    names = sorted(ITEMS)
    lines = ["package io.github.sirschrolli.kingdomsatwar.datagen;", "", "import java.util.Set;", "", "/**",
             " * Die Gegenstände mit plastischem Modell (item/relief/&lt;name&gt;): im Inventar das Bild, sonst das Modell.",
             " *", " * <p>ERZEUGT von tools/relief_models.py – nicht von Hand ändern.", " */", "final class ReliefNames {",
             "    private ReliefNames() {}", "", "    static final Set<String> NAMES = Set.of("]
    for i, n in enumerate(names):
        lines.append(f'            "{n}"' + ("," if i < len(names) - 1 else ");"))
    lines.append("}")
    return "\n".join(lines) + "\n"


def main() -> None:
    models = build()
    for name, data in models.items():
        print(f"{name:22s} {len(data['elements']):3d} Quader")
    if "--check" in sys.argv:
        return
    os.makedirs(MODELS, exist_ok=True)
    with open(JAVA, "w", encoding="utf-8") as f:
        f.write(java())
    for name, data in models.items():
        with open(os.path.join(MODELS, name + ".json"), "w", encoding="utf-8") as f:
            json.dump(data, f, separators=(",", ":"))
            f.write("\n")


if __name__ == "__main__":
    main()
