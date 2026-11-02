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

from entitygen.models import (boss_earth, boss_fire, boss_ice, boss_lightning, boss_nature, boss_shadow, boss_water,  # noqa: E402
                              cattle, chicken, donkey, goat, hare, horse, pig, sheep,
                              armadillo, bat, bear, bee, camel, feline, fox, llama, panda, parrot, wolf,
                              cave_troll, mage,
                              bones, camel_husk, creeper, phantom, silverfish, slime, spider, sulfur_cube, undead, undead_horse, witch,
                              breeze, breeze_wind, creaking, guardian, illager, ravager, vex, warden,
                              axolotl, dolphin, fish_cod, fish_puffer, fish_salmon, fish_tropical, frog, nautilus, squid, tadpole,
                              turtle,
                              allay, copper_golem, iron_golem, snow_golem, happy_ghast, sniffer, sniffer_baby, piglin, piglin_baby,
                              hoglin, hoglin_baby, strider, strider_baby,
                              crystal_crawler, deer, dragon, dragon_armor, glimmer_moth, goose, lindworm, night_wing,
                              shade_stalker, umbrer, vineling, wild_boar, will_o_wisp)
from entitygen.paint import paint_model  # noqa: E402

ROOT = Path(__file__).resolve().parent.parent
JAVA = ROOT / "src/main/java/io/github/sirschrolli/kingdomsatwar/client/model/geom"
TEX = ROOT / "src/main/resources/assets/kingdomsatwar/textures/entity"
PACKAGE = "io.github.sirschrolli.kingdomsatwar.client.model.geom"


ONLY: set[str] = set()


def write_model(module, class_name: str, doc: str, folder: str, variants: dict, preview: bool):
    if ONLY and class_name not in ONLY:
        return
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
    if "--only" in sys.argv:                 # nur diese Modelle neu erzeugen, z. B. --only IronSentinelGeometry,SnowmanGeometry
        ONLY.update(sys.argv[sys.argv.index("--only") + 1].split(","))
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
    # Eigene Tiere (ersetzen die Vanilla-Tiere)
    write_model(cattle, "CattleGeometry", "Geometrie des Hausrinds (Hörner, Schopf und Pilze je Rasse).", "cattle",
                cattle.PALETTES, preview)
    write_model(pig, "FarmPigGeometry", "Geometrie des Hausschweins (mit Sattel).", "pig", pig.PALETTES, preview)
    write_model(sheep, "FarmSheepGeometry", "Geometrie des Schafs (Wolle als eigene Knochen, eingefärbt im Renderer).",
                "sheep", sheep.PALETTES, preview)
    write_model(chicken, "FarmChickenGeometry", "Geometrie des Huhns (Hahn mit großem Kamm und Sichelfedern).", "chicken",
                chicken.PALETTES, preview)
    write_model(goat, "FarmGoatGeometry", "Geometrie der Ziegen (Hörner je Art, einzeln abbrechbar).", "goat",
                goat.PALETTES, preview)
    write_model(hare, "HareGeometry", "Geometrie von Hase und Kaninchen.", "hare", hare.PALETTES, preview)
    write_model(horse, "FarmHorseGeometry",
                "Geometrie der Pferde (Skelett wie beim Vanilla-Pferd, damit Sattel und Rossharnisch passen).", "horse",
                horse.PALETTES, preview)
    write_model(donkey, "FarmDonkeyGeometry",
                "Geometrie von Esel und Maultier (Skelett wie bei Vanilla, damit Sattel und Packtaschen passen).", "donkey",
                donkey.PALETTES, preview)
    # Eigene Wildtiere (ersetzen die Vanilla-Wildtiere; Knochen wie bei Vanilla, damit dessen Animationen passen)
    write_model(wolf, "WildWolfGeometry", "Geometrie der Wölfe (Skelett wie beim Vanilla-Wolf, Halsband als eigener Knochen).",
                "wolf", wolf.PALETTES, preview)
    write_model(fox, "WildFoxGeometry", "Geometrie der Füchse (Fennek mit großen Ohren als eigene Knochen).", "fox",
                fox.PALETTES, preview)
    write_model(bear, "BearGeometry", "Geometrie der Bären (Skelett wie beim Eisbären, Buckel beim Braunbären).", "bear",
                bear.PALETTES, preview)
    write_model(panda, "WildPandaGeometry", "Geometrie des Pandas (je Wesenszug eine Fellzeichnung).", "panda",
                panda.PALETTES, preview)
    write_model(feline, "FelineGeometry",
                "Geometrie von Hauskatzen und Wildkatzen (Pinsel, Stummelschwanz, Halskrause und Halsband als eigene Knochen).",
                "feline", feline.PALETTES, preview)
    write_model(parrot, "WildParrotGeometry", "Geometrie der Papageien (Skelett wie beim Vanilla-Papagei).", "parrot",
                parrot.PALETTES, preview)
    write_model(bee, "WildBeeGeometry", "Geometrie von Honigbiene und Hummel.", "bee", bee.PALETTES, preview)
    write_model(bat, "WildBatGeometry", "Geometrie der Fledermäuse.", "bat", bat.PALETTES, preview)
    write_model(armadillo, "WildArmadilloGeometry", "Geometrie der Gürteltiere (mit eingerollter Kugel).", "armadillo",
                armadillo.PALETTES, preview)
    write_model(camel, "WildCamelGeometry", "Geometrie von Dromedar und Trampeltier (zweiter Höcker als eigener Knochen).",
                "camel", camel.PALETTES, preview)
    write_model(llama, "WildLlamaGeometry", "Geometrie der Lamas (Skelett wie bei Vanilla, damit Teppich und Kisten passen).",
                "llama", llama.PALETTES, preview)
    # Eigene Monster (ersetzen die Vanilla-Monster; Knochen wie bei Vanilla, damit Rüstung, Gegenstände und Animationen passen)
    write_model(undead, "UndeadGeometry",
                "Geometrie von Wiedergänger, Mumie und Wasserleiche (Menschen-Skelett der Zombies, Zubehör als eigene Knochen).",
                "undead", undead.PALETTES, preview)
    write_model(bones, "BonesGeometry",
                "Geometrie von Gerippe, Frostgebein, Moorgebein und Dörrgebein (Skelett-Knochen wie bei Vanilla, Pilze am Kopf).",
                "skeleton", bones.PALETTES, preview)
    write_model(creeper, "CreeperGeometry", "Geometrie der Kriecher (Hauptquader wie beim Vanilla-Creeper, Blätter und Blüten als Hülle).",
                "creeper", creeper.PALETTES, preview)
    write_model(spider, "SpiderGeometry", "Geometrie der Spinnen (Vanilla-Knochen, geknickte Beine als Kind-Knochen).", "spider",
                spider.PALETTES, preview)
    write_model(silverfish, "SilverfishGeometry", "Geometrie der Silberfischchen (Vanilla-Segmente mit Beinen und Fühlern).",
                "silverfish", silverfish.PALETTES, preview)
    write_model(slime, "SlimeGeometry", "Geometrie der Schleime (Kern mit Gesicht und durchscheinende Hülle als eigener Knochen).",
                "slime", slime.PALETTES, preview)
    write_model(sulfur_cube, "SulfurCubeGeometry",
                "Geometrie der Schwefelwürfel (Hülle und Kern, groß und klein, je als eigener Knochen).", "sulfur_cube",
                sulfur_cube.PALETTES, preview)
    write_model(witch, "WitchGeometry", "Geometrie der Hexen (Menschen-Skelett mit Spitzhut, Nase, Haaren, Umhang und Kleid).", "witch",
                witch.PALETTES, preview)
    write_model(phantom, "PhantomGeometry", "Geometrie der Nachtmahre (Knochen wie beim Vanilla-Phantom).", "phantom",
                phantom.PALETTES, preview)
    write_model(undead_horse, "UndeadHorseGeometry", "Geometrie der Zombie- und Skelettpferde (Pferde-Skelett der Mod).",
                "undead_horse", undead_horse.PALETTES, preview)
    write_model(camel_husk, "CamelHuskGeometry", "Geometrie der Dörrkamele (Kamel-Skelett der Mod).", "camel_husk",
                camel_husk.PALETTES, preview)
    # übrige Monster (E4b)
    write_model(illager, "IllagerGeometry",
                "Geometrie der Gesetzlosen (Skelett des Vanilla-Illagers mit Menschenkopf; Kapuzen, Mützen, Bärte, Roben als eigene Knochen).",
                "illager", illager.PALETTES, preview)
    write_model(vex, "VexGeometry", "Geometrie der Plagegeister (Knochen wie beim Vanilla-Plagegeist, Kapuze und Fetzen).", "vex",
                vex.PALETTES, preview)
    write_model(ravager, "RavagerGeometry", "Geometrie der Verwüster (Knochen wie beim Vanilla-Verwüster, Hörner, Fell und Geschirr).",
                "ravager", ravager.PALETTES, preview)
    write_model(guardian, "GuardianGeometry",
                "Geometrie der Riffwächter (Knochen und Stacheln wie beim Vanilla-Wächter; der uralte wird im Spiel ×2,35 gezeichnet).",
                "guardian", guardian.PALETTES, preview)
    write_model(breeze, "BreezeGeometry", "Geometrie des Windgeists (Körper: Kopf mit Kapuze, Runensteine, Augen).", "breeze",
                breeze.PALETTES, preview)
    write_model(breeze_wind, "BreezeWindGeometry", "Geometrie des Windgeists (Wind-Ebene mit nahtlos rollender Textur).",
                "breeze/wind", breeze_wind.PALETTES, preview)
    wind_img, _ = breeze_wind.full_texture()               # die Wind-Textur rollt: das ganze Bild, nicht nur die Flächen
    wind_img.save(TEX / "breeze/wind" / "windgeist.png")
    write_model(creaking, "CreakingGeometry", "Geometrie des Knorz (Knochen wie beim Vanilla-Knarz, Geweih aus Ästen).", "creaking",
                creaking.PALETTES, preview)
    write_model(warden, "WardenGeometry", "Geometrie des Tiefenhüters (Knochen wie beim Vanilla-Wärter, Hörner statt Fühler).",
                "warden", warden.PALETTES, preview)
    # Wassertiere (E4c): Knochen wie bei Vanilla, eigene Quader und Texturen
    write_model(fish_cod, "CodfishGeometry", "Geometrie des Dorschs (Knochen wie beim Vanilla-Kabeljau, dazu Bartel und Afterflosse).",
                "cod", fish_cod.PALETTES, preview)
    write_model(fish_salmon, "SalmonidGeometry", "Geometrie des Lachses (Knochen wie beim Vanilla-Lachs, dazu Buckel, Hakenkiefer und Flossen).",
                "salmon", fish_salmon.PALETTES, preview)
    for stage, size in (("klein", "Small"), ("mittel", "Mid"), ("gross", "Big")):
        module = fish_puffer.stage_module(stage)
        write_model(module, f"Puffer{size}Geometry", f"Geometrie des Kugelfischs ({stage}, Knochen wie beim Vanilla-Kugelfisch).",
                    "pufferfish", module.PALETTES, preview)
    for shape, size in (("a", "Small"), ("b", "Large")):
        module = fish_tropical.shape_module(shape)
        write_model(module, f"ReefFish{size}Geometry", f"Geometrie des Rifffischs (Form {shape.upper()}, Grundfarbe getönt).",
                    "tropical_fish", module.PALETTES, preview)
        pattern = fish_tropical.shape_module(shape, pattern=True)
        write_model(pattern, f"ReefFish{size}PatternGeometry", f"Geometrie der Muster-Ebene des Rifffischs (Form {shape.upper()}).",
                    "tropical_fish", pattern.PALETTES, preview)
    write_model(axolotl, "NewtGeometry", "Geometrie des Molchs (Knochen wie beim Vanilla-Axolotl).", "axolotl", axolotl.PALETTES, preview)
    write_model(frog, "ToadGeometry", "Geometrie des Froschs (Knochen wie beim Vanilla-Frosch).", "frog", frog.PALETTES, preview)
    write_model(tadpole, "PolliwogGeometry", "Geometrie der Kaulquappe (Knochen wie bei Vanilla, Flossensaum durchscheinend).",
                "tadpole", tadpole.PALETTES, preview)
    write_model(squid, "InkfishGeometry", "Geometrie der Tintenfische und Tiefseekraken (Knochen wie beim Vanilla-Tintenfisch).",
                "squid", squid.PALETTES, preview)
    write_model(dolphin, "SeaDolphinGeometry", "Geometrie der Delfine (Knochen wie beim Vanilla-Delfin, Finne je Art).",
                "dolphin", dolphin.PALETTES, preview)
    write_model(turtle, "SeaTurtleGeometry", "Geometrie der Meeresschildkröten (Knochen wie bei der Vanilla-Schildkröte).",
                "turtle", turtle.PALETTES, preview)
    write_model(nautilus, "SeaNautilusGeometry", "Geometrie der Nautilusse (Knochen wie beim Vanilla-Nautilus, Fangarme an den Mundteilen).",
                "nautilus", nautilus.PALETTES, preview)
    # Golems und Wichtel (E4d): Knochen wie bei Vanilla, eigene Quader und Texturen
    write_model(iron_golem, "IronSentinelGeometry",
                "Geometrie des Eisenwächters (Knochen wie beim Vanilla-Eisengolem; Polster und Eiszapfen nur je Gestalt bemalt).",
                "iron_golem", iron_golem.PALETTES, preview)
    if not ONLY or "IronSentinelGeometry" in ONLY:
        crack_model = iron_golem.build()
        crack_model.pack()
        for level in ("low", "medium", "high"):
            iron_golem.crack_overlay(crack_model, level).save(TEX / "iron_golem" / f"risse_{level}.png")
    write_model(snow_golem, "SnowmanGeometry",
                "Geometrie des Schneemanns (Knochen wie beim Vanilla-Schneegolem; Nase und Mütze nur ohne Kürbis sichtbar).",
                "snow_golem", snow_golem.PALETTES, preview)
    write_model(copper_golem, "CopperServantGeometry",
                "Geometrie des Kupferknechts (Knochen wie beim Vanilla-Kupfergolem, dazu der Aufziehschlüssel).",
                "copper_golem", copper_golem.PALETTES, preview)
    write_model(allay, "HouseSpriteGeometry",
                "Geometrie der Wichtel (Knochen wie beim Vanilla-Hilfsgeist; Zipfelmütze oder Fliegenpilz je Gestalt).",
                "allay", allay.PALETTES, preview)
    # Seltene Wesen (E4d-2): Knochen wie bei Vanilla, eigene Quader und Texturen
    write_model(sniffer, "PrimevalDiggerGeometry",
                "Geometrie des Urwühlers (Knochen wie beim Vanilla-Schnüffler; Fellbehang, Buckel, Moosmatte, Backenbart).",
                "sniffer", sniffer.PALETTES, preview)
    write_model(sniffer_baby, "PrimevalDiggerBabyGeometry",
                "Geometrie des jungen Urwühlers (Knochen wie beim Vanilla-Schnüfflerjungen; Flaum, Stirnbüschel, Farnsprössling).",
                "sniffer/baby", sniffer_baby.PALETTES, preview)
    write_model(happy_ghast, "CloudJellyGeometry",
                "Geometrie der Wolkenqualle (Körper und Fangarme wie beim glücklichen Ghast, dazu der Ballenkranz; im Spiel ×4).",
                "happy_ghast", happy_ghast.PALETTES, preview)
    # Nether-Volk (E5a): Knochen wie bei den Vanilla-Piglins
    write_model(piglin, "BorstlingGeometry",
                "Geometrie der Borstlinge, Borstenhünen und Moderborstlinge (Knochen wie beim Vanilla-Piglin; Helm und Panzer nur beim Hünen bemalt).",
                "piglin", piglin.PALETTES, preview)
    write_model(piglin_baby, "BorstlingBabyGeometry",
                "Geometrie der jungen Borstlinge und Moderborstlinge (Knochen wie beim Vanilla-Piglin-Kind).",
                "piglin/baby", {k: v for k, v in piglin_baby.PALETTES.items() if k != "borstenhuene"}, preview)
    write_model(hoglin, "GlutkeilerGeometry",
                "Geometrie der Glutkeiler und Moderkeiler (Knochen wie beim Vanilla-Hoglin; Pilze nur beim Karmesinkeiler, Kruste nur beim Schlackenkeiler bemalt).",
                "hoglin", hoglin.PALETTES, preview)
    write_model(hoglin_baby, "GlutkeilerBabyGeometry",
                "Geometrie der Glut-Frischlinge und jungen Moderkeiler (Knochen wie beim Vanilla-Hoglin-Jungtier).",
                "hoglin/baby", hoglin_baby.PALETTES, preview)
    write_model(strider, "LavastelzerGeometry",
                "Geometrie der Lavastelzer (Knochen und Rumpf wie beim Vanilla-Schreiter, damit der Sattel passt).",
                "strider", strider.PALETTES, preview)
    write_model(strider_baby, "LavastelzerBabyGeometry", "Geometrie der jungen Lavastelzer (Knochen wie beim Vanilla-Schreiter-Jungtier).",
                "strider/baby", strider_baby.PALETTES, preview)
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
    write_model(boss_shadow, "NyxarGeometry",
                "Geometrie von Nyxar, dem Schattendrachen (Endgegner, Grundgröße, im Spiel ×2,3).", "boss", boss_shadow.PALETTES,
                preview)
    write_model(mage, "MageGeometry", "Geometrie der sechs Erzmagier (Zubehör je Element, siehe MageModel).", "mage",
                mage.PALETTES, preview)
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
