#!/usr/bin/env python3
"""
Erzeugt Modell-Geometrie (Java) und Texturen aller Mobs der Mod.

Aufruf (aus dem Projektordner):  python3 tools/generate_entities.py [--preview]
Ergebnis:
  src/main/java/.../client/model/geom/<Name>Geometry.java   (nicht von Hand ändern)
  src/main/resources/assets/kingdomsatwar/textures/entity/<mob>/<variante>.png (+ _glow.png)
"""
from __future__ import annotations

import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))

from entitygen.models import (boss_earth, boss_fire, boss_ice, boss_lightning, boss_nature, boss_water, cave_troll,  # noqa: E402
                              crystal_crawler, deer, dragon, dragon_armor, glimmer_moth, goose, lindworm, night_wing,
                              shade_stalker, umbrer, vineling, wild_boar, will_o_wisp)
from entitygen.paint import paint_model  # noqa: E402

ROOT = Path(__file__).resolve().parent.parent
JAVA = ROOT / "src/main/java/io/github/sirschrolli/kingdomsatwar/client/model/geom"
TEX = ROOT / "src/main/resources/assets/kingdomsatwar/textures/entity"
PACKAGE = "io.github.sirschrolli.kingdomsatwar.client.model.geom"


def write_model(module, class_name: str, doc: str, folder: str, variants: dict, preview: bool):
    model = module.build()
    model.pack()
    JAVA.mkdir(parents=True, exist_ok=True)
    (JAVA / f"{class_name}.java").write_text(model.java_class(PACKAGE, class_name, doc), encoding="utf-8")
    out = TEX / folder
    out.mkdir(parents=True, exist_ok=True)
    for i, (variant, pal) in enumerate(variants.items()):
        img, glow = paint_model(model, module.materials(variant), pal, seed=100 + i, params={"variant": variant})
        img.save(out / f"{variant}.png")
        if glow.getbbox() is not None:
            glow.save(out / f"{variant}_glow.png")
        if preview:
            img.resize((img.width * 3, img.height * 3)).save(f"/tmp/claude-0/-home-claude/50786bb5-6cc3-5907-a25e-13bee43305bb/scratchpad/ent_{folder.replace("/", "_")}_{variant}.png")
    print(f"{class_name}: {len(model.all_cubes())} Quader, {len(variants)} Varianten")


def main():
    preview = "--preview" in sys.argv
    write_model(dragon, "DragonGeometry", "Geometrie des Drachen-Modells (ausgewachsene Größe).", "dragon",
                dragon.PALETTES, preview)
    write_model(dragon_armor, "DragonArmorGeometry",
                "Geometrie der Drachenpanzer (gleiche Knochen wie das Drachen-Modell, nur Panzerplatten).",
                "dragon/armor", dragon_armor.PALETTES, preview)
    # Wilde Kreaturen
    write_model(wild_boar, "WildBoarGeometry", "Geometrie des Wildschweins.", "wild_boar", wild_boar.PALETTES, preview)
    write_model(deer, "DeerGeometry", "Geometrie des Rothirschs (mit Geweih).", "deer", deer.PALETTES, preview)
    write_model(goose, "GooseGeometry", "Geometrie der Gans.", "goose", goose.PALETTES, preview)
    write_model(will_o_wisp, "WillOWispGeometry", "Geometrie des Irrlichts.", "will_o_wisp", will_o_wisp.PALETTES, preview)
    write_model(cave_troll, "CaveTrollGeometry", "Geometrie des Höhlentrolls.", "cave_troll", cave_troll.PALETTES, preview)
    write_model(lindworm, "LindwormGeometry", "Geometrie des Lindwurms.", "lindworm", lindworm.PALETTES, preview)
    # Drachen-Bosse (Grundgröße, im Spiel vergrößert)
    write_model(boss_fire, "PyrrhonGeometry", "Geometrie von Pyrrhon, dem Feuerdrachen-Boss (Grundgröße, im Spiel ×2,1).",
                "boss", boss_fire.PALETTES, preview)
    write_model(boss_ice, "GlaciaraGeometry", "Geometrie von Glaciara, dem Eisdrachen-Boss (Grundgröße, im Spiel ×2,1).",
                "boss", boss_ice.PALETTES, preview)
    write_model(boss_nature, "VerdantisGeometry",
                "Geometrie von Verdantis, dem Naturdrachen-Boss (Grundgröße, im Spiel ×2,0).", "boss", boss_nature.PALETTES, preview)
    write_model(boss_earth, "TerragonGeometry",
                "Geometrie von Terragon, dem Erddrachen-Boss (Grundgröße, im Spiel ×2,2).", "boss", boss_earth.PALETTES, preview)
    write_model(boss_lightning, "VoltarionGeometry",
                "Geometrie von Voltarion, dem Blitzdrachen-Boss (Grundgröße, im Spiel ×2,1).", "boss", boss_lightning.PALETTES,
                preview)
    write_model(boss_water, "ThalassaGeometry",
                "Geometrie von Thalassa, dem Wasserdrachen-Boss (Grundgröße, im Spiel ×2,0).", "boss", boss_water.PALETTES, preview)
    write_model(vineling, "VinelingGeometry", "Geometrie des Ranklings (Diener von Verdantis).", "vineling", vineling.PALETTES,
                preview)
    # Kreaturen der Schattenwelt
    write_model(shade_stalker, "ShadeStalkerGeometry", "Geometrie des Schattenpirschers.", "shade_stalker", shade_stalker.PALETTES,
                preview)
    write_model(crystal_crawler, "CrystalCrawlerGeometry", "Geometrie des Kristallkriechers.", "crystal_crawler",
                crystal_crawler.PALETTES, preview)
    write_model(night_wing, "NightWingGeometry", "Geometrie der Nachtschwinge.", "night_wing", night_wing.PALETTES, preview)
    write_model(glimmer_moth, "GlimmerMothGeometry", "Geometrie des Glimmerfalters.", "glimmer_moth", glimmer_moth.PALETTES, preview)
    write_model(umbrer, "UmbrerGeometry", "Geometrie der Umbrer (alle Berufe, Zubehör je Beruf).", "umbrer", umbrer.PALETTES, preview)


if __name__ == "__main__":
    main()
