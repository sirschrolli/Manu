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
                              hoglin, hoglin_baby, strider, strider_baby, ghast, blaze, magma_cube, wither, wither_skull,
                              enderman, endermite, shulker, shulker_bullet, ender_dragon,
                              crystal_crawler, deer, dragon, dragon_armor, glimmer_moth, goose, lindworm, moosling, night_wing,
                              shade_stalker, umbrer, vineling, wild_boar, will_o_wisp,
                              ice_lance, icicle, ice_prison, phoenix, poison_flower, stone_fist, stone_spike, thorn_vine, moon_crystal, coral_spike,
                              void_maw, shard_warden, distortling, star_ray, warden_shard, shattered_king,
                              shattered_crown, manta_ray, glow_jellyfish, shark, anglerfish, giant_crab, reef_colossus, kraken, mermaid, sea_witch,
                              soul_shepherd,
                              armor_parts,
                              fairy, mushroomling, thorn_wight, moth_queen, glutschmied, asteria, stone_keeper, keeper_boulder,
                              miss_mystik, light_dove, magic_sword, anni_troll,
                              werewolf, mire_shambler, ember_wyrm, ash_wraith, soul_heron, night_ram, rift_hound, chorus_maw,
                              world_eater, world_eater_segment, world_eater_tail, world_chunk,
                              schrolli, seraphling, divine_spear, divine_feather, divine_halo, divine_planet,
                              snow_leopard, primeval_elk, storm_gull, griffin, ember_salamander, mire_snapper, flamingo, butterfly)
from entitygen.paint import paint_model  # noqa: E402

ROOT = Path(__file__).resolve().parent.parent
JAVA = ROOT / "src/main/java/io/github/sirschrolli/kingdomsatwar/client/model/geom"
TEX = ROOT / "src/main/resources/assets/kingdomsatwar/textures/entity"
PACKAGE = "io.github.sirschrolli.kingdomsatwar.client.model.geom"


ONLY: set[str] = set()


def write_model(module, class_name: str, doc: str, folder: str, variants: dict, preview: bool, same_seed: bool = False):
    if ONLY and class_name not in ONLY:
        return
    model = module.build()
    model.pack()
    JAVA.mkdir(parents=True, exist_ok=True)
    (JAVA / f"{class_name}.java").write_text(model.java_class(PACKAGE, class_name, doc), encoding="utf-8")
    out = TEX / folder
    out.mkdir(parents=True, exist_ok=True)
    for i, (variant, pal) in enumerate(variants.items()):
        # same_seed: alle Varianten mit demselben Zufall bemalt (sie unterscheiden sich nur in den Farben)
        img, glow = paint_model(model, module.materials(variant), pal, seed=100 if same_seed else 100 + i, params={"variant": variant})
        img.save(out / f"{variant}.png")
        if glow.getbbox() is not None:
            glow.save(out / f"{variant}_glow.png")
        if preview:
            img.resize((img.width * 3, img.height * 3)).save(f"/tmp/claude-0/-home-claude/50786bb5-6cc3-5907-a25e-13bee43305bb/scratchpad/ent_{folder.replace("/", "_")}_{variant}.png")
    print(f"{class_name}: {len(model.all_cubes())} Quader, {len(variants)} Varianten")


def write_texture(module, name: str, folder: str, variants: dict, preview: bool):
    """Wie write_model, aber nur die Textur (die Geometrie und ihre Java-Klasse gehören einem anderen Modell)."""
    if ONLY and name not in ONLY:
        return
    model = module.build()
    model.pack()
    out = TEX / folder
    out.mkdir(parents=True, exist_ok=True)
    for i, (variant, pal) in enumerate(variants.items()):
        img, glow = paint_model(model, module.materials(variant), pal, seed=100 + i, params={"variant": variant})
        img.save(out / f"{variant}.png")
        if glow.getbbox() is not None:
            glow.save(out / f"{variant}_glow.png")
    print(f"{name}: {len(model.all_cubes())} Quader, {len(variants)} Varianten (nur Textur)")


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
    # Wesen des Feenreichs
    write_model(fairy, "FairyGeometry", "Geometrie der Fee (Skelett des Hilfsgeists).", "fairy", fairy.PALETTES, preview)
    write_model(mushroomling, "MushroomlingGeometry", "Geometrie des Pilzlings.", "mushroomling", mushroomling.PALETTES, preview)
    write_model(thorn_wight, "ThornWightGeometry", "Geometrie des Dornwichts.", "thorn_wight", thorn_wight.PALETTES, preview)
    write_model(glutschmied, "GlutschmiedGeometry", "Geometrie des Glutschmieds Malleus (im Spiel 1,4-fach vergrößert).",
                "glutschmied", glutschmied.PALETTES, preview)
    write_model(stone_keeper, "StoneKeeperGeometry", "Geometrie des Steinhüters (im Spiel 1,4-fach vergrößert).",
                "stone_keeper", stone_keeper.PALETTES, preview)
    write_model(keeper_boulder, "KeeperBoulderGeometry", "Geometrie des Felsbrockens, den der Steinhüter schleudert.",
                "stone_keeper/boulder", keeper_boulder.PALETTES, preview)
    write_model(asteria, "AsteriaGeometry",
                "Geometrie Asterias, der Sternenreiterin, auf ihrem gepanzerten Sternenrochen (im Spiel 1,2-fach vergrößert).",
                "asteria", asteria.PALETTES, preview)
    write_model(miss_mystik, "MissMystikGeometry",
                "Geometrie MissMystiks, der Feen-Zauberkünstlerin (Frack, Zylinder, Maske, zwei Paar Feenflügel, Zauberstab, Kartenfächer).",
                "miss_mystik", miss_mystik.PALETTES, preview)
    write_model(light_dove, "LightDoveGeometry", "Geometrie der Lichttaube (MissMystik).", "miss_mystik", light_dove.PALETTES, preview)
    write_model(magic_sword, "MagicSwordGeometry", "Geometrie eines Schwerts der Schwerterkiste (MissMystik).", "miss_mystik",
                magic_sword.PALETTES, preview)
    write_model(anni_troll, "AnniTrollGeometry",
                "Geometrie Annis, des Trolls (im Spiel 1,2-fach vergrößert): Flickenkittel, Bannhammer, Haarschopf, Pixelkrone, zerfallener Arm.",
                "anni_troll", anni_troll.PALETTES, preview, same_seed=True)
    write_model(moth_queen, "MothQueenGeometry", "Geometrie der Mottenkönigin (im Spiel 2,5-fach vergrößert).", "moth_queen",
                moth_queen.PALETTES, preview)
    write_model(cave_troll, "CaveTrollGeometry", "Geometrie des Höhlentrolls.", "cave_troll", cave_troll.PALETTES, preview)
    # Neue Monster (0.69)
    write_model(werewolf, "WerewolfGeometry", "Geometrie des Werwolfs.", "werewolf", werewolf.PALETTES, preview)
    write_model(mire_shambler, "MireShamblerGeometry", "Geometrie des Moorschlurfers.", "mire_shambler", mire_shambler.PALETTES, preview)
    write_model(ember_wyrm, "EmberWyrmGeometry", "Geometrie des Glutwurms (Schädel als Ursprung, sechs Glieder).", "ember_wyrm",
                ember_wyrm.PALETTES, preview)
    write_model(ash_wraith, "AshWraithGeometry", "Geometrie des Aschengeists.", "ash_wraith", ash_wraith.PALETTES, preview)
    # Monster der Schattenwelt und des Ends (0.71)
    write_model(soul_heron, "SoulHeronGeometry", "Geometrie des Seelenreihers.", "soul_heron", soul_heron.PALETTES, preview)
    write_model(night_ram, "NightRamGeometry", "Geometrie des Nachtwidders.", "night_ram", night_ram.PALETTES, preview)
    write_model(rift_hound, "RiftHoundGeometry", "Geometrie des Rissjägers.", "rift_hound", rift_hound.PALETTES, preview)
    write_model(chorus_maw, "ChorusMawGeometry", "Geometrie des Chorusschlunds.", "chorus_maw", chorus_maw.PALETTES, preview)
    # Der Weltenfresser (0.73): Kopf, Leibglied, Schwanz und die Weltbrocken, die er ausspeit
    write_model(world_eater, "WorldEaterGeometry", "Geometrie des Kopfs des Weltenfressers (im Spiel doppelt so groß).",
                "world_eater", world_eater.PALETTES, preview)
    write_model(world_eater_segment, "WorldEaterSegmentGeometry",
                "Geometrie eines Leibglieds des Weltenfressers (je Glied ist eines der vier Weltstücke zu sehen).", "world_eater",
                {"world_eater_segment": world_eater_segment.PALETTES["world_eater"]}, preview)
    write_model(world_eater_tail, "WorldEaterTailGeometry", "Geometrie des Schwanzes des Weltenfressers.", "world_eater",
                {"world_eater_tail": world_eater_tail.PALETTES["world_eater"]}, preview)
    write_model(world_chunk, "WorldChunkGeometry", "Geometrie eines Weltbrockens (Glut, Frost, End, Schatten).", "world_chunk",
                world_chunk.PALETTES, preview)
    write_model(lindworm, "LindwormGeometry", "Geometrie des Lindwurms.", "lindworm", lindworm.PALETTES, preview)
    # Tiere der eigenen Biome (0.85)
    write_model(snow_leopard, "SnowLeopardGeometry", "Geometrie des Schneeleoparden (Frostgipfel).", "snow_leopard",
                snow_leopard.PALETTES, preview)
    write_model(primeval_elk, "PrimevalElkGeometry", "Geometrie des Urelchs (Urhain; Sattel, Zaumzeug und Truhen als eigene Knochen).",
                "primeval_elk", primeval_elk.PALETTES, preview)
    write_model(storm_gull, "StormGullGeometry", "Geometrie der Sturmmöwe (Ruhehaltung: Flügel ausgebreitet).", "storm_gull",
                storm_gull.PALETTES, preview, same_seed=True)
    write_model(griffin, "GriffinGeometry", "Geometrie des Greifen (Ruhehaltung: Schwingen ausgebreitet).", "griffin",
                griffin.PALETTES, preview, same_seed=True)
    write_model(ember_salamander, "EmberSalamanderGeometry", "Geometrie des Glutsalamanders.", "ember_salamander",
                ember_salamander.PALETTES, preview, same_seed=True)
    write_model(mire_snapper, "MireSnapperGeometry", "Geometrie des Moorschnappers.", "mire_snapper",
                mire_snapper.PALETTES, preview, same_seed=True)
    write_model(flamingo, "FlamingoGeometry", "Geometrie des Flamingos (Ruhehaltung: Flügel ausgebreitet).", "flamingo",
                flamingo.PALETTES, preview, same_seed=True)
    write_model(butterfly, "ButterflyGeometry", "Geometrie der Schmetterlinge (Flügelform in der Textur).", "butterfly",
                butterfly.PALETTES, preview, same_seed=True)
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
    write_model(moosling, "MooslingGeometry", "Geometrie der Mooslinge (Arachnophobie-Modus: Spinnenknochen ohne Beine).", "moosling",
                moosling.PALETTES, preview)
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
    # übrige Nether-Wesen (E5b)
    write_model(ghast, "RauchqualleGeometry",
                "Geometrie der Rauchqualle (Körper und Fangarme wie beim Vanilla-Ghast, dazu Haube und Kranz; im Spiel ×4,5).",
                "ghast", ghast.PALETTES, preview)
    write_model(blaze, "GlutgeistGeometry",
                "Geometrie des Glutgeists (Kopf und zwölf Stäbe wie bei der Vanilla-Lohe, dazu Krone, Bart und Kern).",
                "blaze", blaze.PALETTES, preview)
    write_model(magma_cube, "GlutschleimGeometry",
                "Geometrie des Glutschleims (acht Scheiben und Kern wie beim Vanilla-Magmawürfel, dazu Schollen und Tropfen).",
                "magma_cube", magma_cube.PALETTES, preview)
    write_model(wither, "AschenkoenigGeometry",
                "Geometrie des Aschenkönigs (drei Köpfe wo der Vanilla-Wither feuert, Krone, Hörner, Joch, Brustkorb mit Herz, "
                "Mantel, Schwanz und Krallenarme; im Spiel ×2). Mit Hülle für den Seelenschild.",
                "wither", wither.PALETTES, preview)
    if not ONLY or "AschenkoenigGeometry" in ONLY:
        wither.shield_texture().save(TEX / "wither" / "seelenschild.png")
    write_model(wither_skull, "SeelenschaedelGeometry",
                "Geometrie des Seelenschädels, den der Aschenkönig abfeuert (Knochen \"head\" wie beim Vanilla-Schädel, mit Flammenschweif).",
                "wither", wither_skull.PALETTES, preview)
    # Wesen des Ends (E5c)
    write_model(enderman, "LeerwandlerGeometry",
                "Geometrie des Leerwandlers (Knochen wie beim Vanilla-Enderman, Kiefer als \"hat\", Splitterkranz \"halo\").",
                "enderman", enderman.PALETTES, preview)
    write_model(endermite, "LeermilbeGeometry", "Geometrie der Leermilbe (vier Segmente wie bei der Vanilla-Endermite, Beine, Fühler, Dornen).",
                "endermite", endermite.PALETTES, preview)
    write_model(shulker, "SchreinlingGeometry",
                "Geometrie des Schreinlings (Deckel, Sockel und Kopf wie beim Vanilla-Shulker, dazu Medaillon, Beschläge, Zähne, Fuß und Fühler).",
                "shulker", shulker.PALETTES, preview)
    write_model(shulker_bullet, "SchreinfunkeGeometry", "Geometrie des Schreinfunkens (Knochen \"main\" wie beim Vanilla-Shulker-Geschoss).",
                "shulker", shulker_bullet.PALETTES, preview)
    write_model(ender_dragon, "SternenfresserGeometry",
                "Geometrie des Sternenfressers (Knochen, Drehpunkte und Abstände wie beim Vanilla-Enderdrachen, dessen Animation Hals, "
                "Schwanz und Flügel bewegt; dazu Hörner, Kamm, Kristalldornen, Krallen und Schwanzklinge).",
                "ender_dragon", ender_dragon.PALETTES, preview)
    if not ONLY or "SternenfresserGeometry" in ONLY:
        ender_dragon.dissolve_mask(256, 256).save(TEX / "ender_dragon" / "sternenfresser_zerfall.png")
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
    # Wesen der Leere
    write_model(void_maw, "VoidMawGeometry", "Geometrie des Leerenschlunds (Glocke, Saum, Maul mit vier Lippen, Fangfäden).",
                "void_maw", void_maw.PALETTES, preview)
    write_model(shard_warden, "ShardWardenGeometry", "Geometrie des Splitterwächters (offene Brust mit kreisendem Herz).",
                "shard_warden", shard_warden.PALETTES, preview)
    write_model(distortling, "DistortlingGeometry", "Geometrie des Zerrlings (Leib aus Bruchstücken mit Lücken dazwischen).",
                "distortling", distortling.PALETTES, preview)
    if not ONLY or "DistortlingGeometry" in ONLY:
        # Schatten-Textur für die Farbspaltung (weiß, im Spiel türkis bzw. magenta getönt und durchscheinend)
        from PIL import Image
        base = Image.open(TEX / "distortling" / "distortling.png").convert("RGBA")
        ghost = Image.new("RGBA", base.size, (0, 0, 0, 0))
        gp, bp = ghost.load(), base.load()
        for y in range(base.height):
            for x in range(base.width):
                if bp[x, y][3] > 0:
                    gp[x, y] = (255, 255, 255, 255)
        ghost.save(TEX / "distortling" / "distortling_ghost.png")
    write_model(star_ray, "StarRayGeometry", "Geometrie des Sternenrochens (Schwingen in drei Gliedern, Peitschenschwanz).",
                "star_ray", star_ray.PALETTES, preview)
    write_model(warden_shard, "WardenShardGeometry", "Geometrie des Kristallsplitters (Geschoss des Splitterwächters).",
                "warden_shard", warden_shard.PALETTES, preview)
    write_model(shattered_king, "ShatteredKingGeometry",
                "Geometrie des Zerbrochenen Königs (Boss der Leere): Leib mit offener Brust und zersprungenem Herz, Kopf mit Bart "
                "und schwebender Kristallkrone, Schultern, Gewand in Bahnen, zwei frei schwebende Hände und kreisende Trümmer.",
                "shattered_king", shattered_king.PALETTES, preview)
    write_model(shattered_crown, "ShatteredCrownGeometry",
                "Getragene Krone des Zerbrochenen Königs (Teile wie HumanoidModel, nur der Kopf trägt Reif und schwebende "
                "Kristallsplitter); Textur als Rüstungsschicht.", "equipment/humanoid", shattered_crown.PALETTES, preview)
    # Wesen des Meeres
    write_model(manta_ray, "MantaRayGeometry",
                "Geometrie des Mantarochens (Brustflossen in drei Gliedern, Kopfflossen, Peitschenschwanz).", "manta_ray",
                manta_ray.PALETTES, preview)
    write_model(glow_jellyfish, "GlowJellyfishGeometry",
                "Geometrie der Leuchtqualle (Glocke in Stufen, leuchtender Saum, vier Mundarme, acht Fangfäden in drei Gliedern; "
                "durchscheinend).", "glow_jellyfish", glow_jellyfish.PALETTES, preview)
    write_model(shark, "SharkGeometry", "Geometrie des Hais (Kopf mit aufklappbarem Kiefer, Schwanz in zwei Gliedern).", "shark",
                shark.PALETTES, preview)
    write_model(anglerfish, "AnglerfishGeometry",
                "Geometrie des Anglerfischs (Unterkiefer mit Nadelzähnen, Angel in zwei Gliedern mit leuchtendem Köder).",
                "anglerfish", anglerfish.PALETTES, preview)
    write_model(giant_crab, "GiantCrabGeometry",
                "Geometrie der Riesenkrabbe (Scheren mit beweglichem Finger, sechs Laufbeine in zwei Gliedern, Stielaugen).",
                "giant_crab", giant_crab.PALETTES, preview)
    write_model(reef_colossus, "ReefColossusGeometry",
                "Geometrie von Karkinos, dem Riffkoloss (Panzer mit Stielaugen, Fühlern und Mundplatten, Brecher- und Greifschere "
                "mit beweglichem Finger, sechs Laufbeine in drei Gliedern, die Tempelkuppel mit Korallen, Tang und dem Gezeiten-Speer; "
                "im Spiel ×2).", "reef_colossus", reef_colossus.PALETTES, preview)
    write_model(kraken, "KrakenGeometry",
                "Geometrie des Kraken (Kopf mit goldenen Augen, hoher Mantel mit Flossen, Schnabel, acht Arme in sechs Gliedern mit "
                "Saugnäpfen, zwei Fangarme in acht Gliedern mit Keule; im Spiel ×3).", "kraken", kraken.PALETTES, preview)
    write_model(mermaid, "MermaidGeometry",
                "Geometrie des Meervolks (seit 0.93): Meerjungfrauen aller Berufe und Tritonwachen, Zubehör je Beruf; sechs Gestalten.",
                "mermaid", mermaid.PALETTES, preview, same_seed=True)
    write_model(sea_witch, "SeaWitchGeometry",
                "Geometrie der Meerhexe (seit 0.97): hagere Meerjungfrau mit Tanghaar, treibenden Strähnen, Korallenkrone, Tangumhang, "
                "Krallen, Hexenstab mit leuchtender Kugel und einem gebänderten Aalschwanz mit Stacheln und Fächerflossen; im Spiel ×1,5.",
                "sea_witch", sea_witch.PALETTES, preview, same_seed=True)
    write_model(soul_shepherd, "SoulShepherdGeometry",
                "Geometrie des Seelenhirten (seit 0.98): hagerer, gebeugter Riese in zerschlissener Kutte mit Runensaum, Schaffell "
                "über den Schultern, Kapuze mit Widderschädel, gerollten Hörnern und Seelenlicht in den Augen, knochige Arme, "
                "Hirtenstab mit Haken und Seelenlaterne, Handglocke, Talismane am Strick, drei Irrlichter; im Spiel ×1,5.",
                "soul_shepherd", soul_shepherd.PALETTES, preview)
    write_model(armor_parts, "ArmorPartsGeometry",
                "Geometrie der 3D-Teile getragener Rüstungen (seit 0.95): je Rüstungssatz und Platz ein Knochen <satz>_<platz>_<knochen>.",
                "armor_parts", armor_parts.PALETTES, preview, same_seed=True)
    # Zauber-Effekte
    write_model(phoenix, "PhoenixGeometry",
                "Geometrie des Phönix (Geschoss des Feuerzaubers, fliegt nach -Z, Rumpfmitte im Ursprung; Flügel schlagen über "
                "zRot von left_wing/right_wing und den Handschwingen, Schweif und Kopf bewegen sich).",
                "spell/phoenix", phoenix.PALETTES, preview)
    write_model(ice_lance, "IceLanceGeometry",
                "Geometrie der Eislanze (Geschoss des Eiszaubers, ein Knochen \"lance\", Spitze bei -Z, Mitte im Ursprung; "
                "durchscheinend).", "spell/ice_lance", ice_lance.PALETTES, preview)
    write_model(icicle, "IcicleGeometry",
                "Geometrie des Eiszapfens (Geschoss Glaciaras, ein Knochen \"icicle\", Spitze bei -Z, Mitte im Ursprung; "
                "durchscheinend).", "boss/icicle", icicle.PALETTES, preview)
    write_model(ice_prison, "IcePrisonGeometry",
                "Geometrie des Eisgefängnisses (acht Kristalle shard0–shard7 mit Drehpunkt am Fuß auf dem Boden, dazu der "
                "Kristallknoten \"cap\"; durchscheinend, die Kristalle wachsen einzeln aus dem Boden).",
                "spell/ice_prison", ice_prison.PALETTES, preview)
    # Korallenspieße (0.97): Form und Java-Klasse der Mondsteinkristalle, nur die Textur ist eigen
    write_texture(coral_spike, "CoralSpikeTexture", "spell/coral_spike", coral_spike.PALETTES, preview)
    write_model(moon_crystal, "MoonCrystalGeometry", "Geometrie der Mondsteinkristalle (Kristallspur des Steinhüters, seit 0.96).",
                "spell/moon_crystal", moon_crystal.PALETTES, preview)
    write_model(stone_spike, "StoneSpikeGeometry",
                "Geometrie des Steinstachels (Felsdorn \"spike\" und Geröll \"rubble\", beide mit Drehpunkt auf dem Boden).",
                "spell/stone_spike", stone_spike.PALETTES, preview)
    write_model(stone_fist, "StoneFistGeometry",
                "Geometrie der Steinfaust (Unterarm \"arm\" mit Drehpunkt auf dem Boden, darauf die Faust \"fist\", Knöchel oben).",
                "spell/stone_fist", stone_fist.PALETTES, preview)
    write_model(thorn_vine, "ThornVineGeometry",
                "Geometrie der Dornenranke (Kette seg0–seg5, jedes Glied mit Drehpunkt an seinem Fuß; schwingt über xRot/zRot).",
                "spell/thorn_vine", thorn_vine.PALETTES, preview)
    write_model(poison_flower, "PoisonFlowerGeometry",
                "Geometrie der Giftblume (Stiel, Samenkapsel \"bulb\", sechs Blütenblätter petal0–petal5 mit Drehung i·60° um Y, "
                "zeigen nach lokal -Z; öffnen/schließen über xRot; vier Blätter leaf0–leaf3 am Fuß).",
                "spell/poison_flower", poison_flower.PALETTES, preview)
    # Schrolli der Schöpfer (0.75)
    write_model(schrolli, "SchrolliGeometry",
                "Geometrie Schrollis des Schöpfers (im Spiel ×2, in der Wahren Gestalt ×2,6): Gewand, Brustpanzer, sechs Schwingen, "
                "Heiligenschein, Lichtkrone, Zepter und Weltkugel; nur in der Wahren Gestalt sichtbar: true_rings (zwei Räder mit "
                "Augen) und true_corona (Strahlenkranz); nur am Boden (seit 0.76): Beine (left_leg/right_leg) und die Klinge aus "
                "Licht (blade).", "schrolli", schrolli.PALETTES, preview)
    write_model(seraphling, "SeraphlingGeometry", "Geometrie des Seraphlings (Lichtbote Schrollis): Antlitz, Heiligenschein, "
                "zwei Paar Schwingen.", "schrolli", seraphling.PALETTES, preview)
    write_model(divine_spear, "DivineSpearGeometry", "Geometrie des Lichtspeers (Knochen \"spear\", Spitze bei -Z).", "schrolli/shots",
                divine_spear.PALETTES, preview)
    write_model(divine_feather, "DivineFeatherGeometry", "Geometrie der goldenen Feder (Knochen \"feather\", Spitze bei -Z).",
                "schrolli/shots", divine_feather.PALETTES, preview)
    write_model(divine_halo, "DivineHaloGeometry", "Geometrie des geworfenen Heiligenscheins (Knochen \"disc\", dreht um Y).",
                "schrolli/shots", divine_halo.PALETTES, preview)
    write_model(divine_planet, "DivinePlanetGeometry", "Geometrie einer Welt der Schöpfung (Knochen \"planet\" und \"ring\").",
                "schrolli/planet", divine_planet.PALETTES, preview, same_seed=True)


if __name__ == "__main__":
    main()
