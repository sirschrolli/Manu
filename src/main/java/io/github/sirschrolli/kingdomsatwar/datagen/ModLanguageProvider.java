package io.github.sirschrolli.kingdomsatwar.datagen;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierType;
import io.github.sirschrolli.kingdomsatwar.registry.ModBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.ModItems;
import io.github.sirschrolli.kingdomsatwar.registry.ShadowBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.ShadowItems;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.LanguageProvider;

/**
 * Übersetzungen auf Deutsch ({@code de_de}) und Englisch ({@code en_us}).
 * Beide Sprachen stehen nebeneinander, damit keine Übersetzung vergessen wird.
 */
public class ModLanguageProvider extends LanguageProvider {
    private final boolean german;

    public ModLanguageProvider(PackOutput output, String locale) {
        super(output, KingdomsAtWar.MODID, locale);
        this.german = locale.startsWith("de");
    }

    @Override
    protected void addTranslations() {
        bosses();
        shadowWorld();
        // --- Kreativ-Tabs ---
        // (kurz halten: längere Titel ragen im Kreativ-Inventar über den Rand)
        text("itemGroup.kingdomsatwar.building", "Kingdoms at War: Building", "Kingdoms at War: Bauen");
        text("itemGroup.kingdomsatwar.resources", "Kingdoms at War: Resources", "Kingdoms at War: Rohstoffe");
        text("itemGroup.kingdomsatwar.dragons", "Kingdoms at War: Creatures", "Kingdoms at War: Kreaturen");
        text("itemGroup.kingdomsatwar.farming", "Kingdoms at War: Food", "Kingdoms at War: Nahrung");
        text("itemGroup.kingdomsatwar.kingdom", "Kingdoms at War: Army", "Kingdoms at War: Armee");
        text("itemGroup.kingdomsatwar.equipment", "Kingdoms at War: Equipment", "Kingdoms at War: Ausrüstung");
        text("itemGroup.kingdomsatwar.nature", "Kingdoms at War: Wilds", "Kingdoms at War: Wildnis");

        // --- Frostkessel ---
        text("biome.kingdomsatwar.frostbasin", "Frost Basin", "Frostkessel");
        block(ModBlocks.FROST_CRYSTAL, "Frost Crystal", "Frostkristall");
        block(ModBlocks.GLACIER_ICE, "Glacier Ice", "Gletschereis");
        set("rime_stone", "rime_stone", "Rime Stone", "Raureifstein", "Rime Stone", "Raureifstein");
        block(ModBlocks.FROZEN_SOIL, "Frozen Soil", "Frosterde");
        block(ModBlocks.ICICLE, "Icicle", "Eiszapfen");

        // --- Uralter Hain ---
        text("biome.kingdomsatwar.ancient_grove", "Ancient Grove", "Uralter Hain");
        block(ModBlocks.ANCIENT_LOG, "Ancient Log", "Urholzstamm");
        roof("ancient_planks", "ancient_plank", "Ancient Planks", "Urholzbretter", "Ancient Plank", "Urholz");
        block(ModBlocks.ANCIENT_LEAVES, "Ancient Leaves", "Uraltes Laub");
        block(ModBlocks.GLOWING_MOSS, "Glowing Moss", "Leuchtmoos");
        block(ModBlocks.GLOWCAP, "Glowcap", "Leuchtkappe");
        block(ModBlocks.BRAMBLE, "Bramble", "Dornengestrüpp");
        block(ModBlocks.GNARLED_ROOTS, "Gnarled Roots", "Knorrige Wurzeln");

        // --- Zerklüftete Weiten ---
        text("biome.kingdomsatwar.rugged_wilds", "Rugged Wilds", "Zerklüftete Weiten");
        block(ModBlocks.AMBER_CRYSTAL, "Amber Crystal", "Bernsteinkristall");
        block(ModBlocks.AMBER_CLUSTER, "Amber Cluster", "Bernsteindruse");
        set("shale", "shale", "Shale", "Schiefer", "Shale", "Schiefer");
        block(ModBlocks.RUBBLE, "Rubble", "Geröll");
        block(ModBlocks.RIFT_STONE, "Rift Stone", "Spaltgestein");

        // --- Sturmklippen ---
        text("biome.kingdomsatwar.storm_cliffs", "Storm Cliffs", "Sturmklippen");
        set("stormstone", "stormstone", "Stormstone", "Sturmstein", "Stormstone", "Sturmstein");
        block(ModBlocks.CHARGED_STORMSTONE, "Charged Stormstone", "Geladener Sturmstein");
        block(ModBlocks.FULGURITE, "Fulgurite", "Fulgurit");
        block(ModBlocks.STORM_CRYSTAL, "Storm Crystal", "Sturmkristall");

        // --- Abgrundgrotte ---
        text("biome.kingdomsatwar.abyssal_grotto", "Abyssal Grotto", "Abgrundgrotte");
        set("abyssal_stone", "abyssal_stone", "Abyssal Stone", "Abgrundstein", "Abyssal Stone", "Abgrundstein");
        block(ModBlocks.ABYSSAL_SAND, "Abyssal Sand", "Abgrundsand");
        block(ModBlocks.GLOW_CORAL, "Glow Coral", "Leuchtkoralle");
        block(ModBlocks.NACRE_BLOCK, "Nacre Block", "Perlmuttblock");

        // --- Aschelande ---
        text("biome.kingdomsatwar.ashlands", "Ashlands", "Aschelande");
        block(ModBlocks.ASH_BLOCK, "Ash", "Asche");
        block(ModBlocks.ASH_LAYER, "Ash Layer", "Ascheschicht");
        block(ModBlocks.CINDER_SOIL, "Cinder Soil", "Aschenerde");
        set("volcanic_rock", "volcanic_rock", "Volcanic Rock", "Vulkangestein", "Volcanic Rock", "Vulkangestein");
        block(ModBlocks.GLOWING_VOLCANIC_ROCK, "Glowing Volcanic Rock", "Glühendes Vulkangestein");
        block(ModBlocks.CHARRED_LOG, "Charred Log", "Verkohlter Stamm");
        block(ModBlocks.EMBER_VENT, "Ember Vent", "Glutspalte");

        // --- Steinblöcke ---
        set("medieval_stone", "medieval_stone", "Medieval Stone", "Mittelalterlicher Stein",
                "Medieval Stone", "Mittelalterliche Stein");
        set("rough_fieldstone", "rough_fieldstone", "Rough Fieldstone", "Grober Naturstein",
                "Rough Fieldstone", "Grobe Naturstein");
        set("hewn_sandstone", "hewn_sandstone", "Hewn Sandstone", "Behauener Sandstein",
                "Hewn Sandstone", "Behauene Sandstein");
        set("dark_stone_bricks", "dark_stone_brick", "Dark Stone Bricks", "Dunkle Steinziegel",
                "Dark Stone Brick", "Dunkle Steinziegel");
        set("limestone_bricks", "limestone_brick", "Light Limestone Bricks", "Helle Kalksteinziegel",
                "Light Limestone Brick", "Helle Kalksteinziegel");
        set("weathered_stone_bricks", "weathered_stone_brick", "Weathered Stone Bricks", "Verwitterte Steinziegel",
                "Weathered Stone Brick", "Verwitterte Steinziegel");
        set("mossy_wall_stones", "mossy_wall_stone", "Mossy Wall Stones", "Moosige Mauersteine",
                "Mossy Wall Stone", "Moosige Mauerstein");
        set("cracked_stone_blocks", "cracked_stone_block", "Cracked Stone Blocks", "Gerissene Steinblöcke",
                "Cracked Stone Block", "Gerissene Steinblock");
        block(ModBlocks.STONE_PILLAR, "Stone Pillar", "Steinpfeiler");

        // --- Holz und Fachwerk ---
        block(ModBlocks.TIMBER_BEAM, "Timber Beam", "Holzbalken");
        block(ModBlocks.TIMBER_FRAME, "Half-Timbering", "Fachwerk");
        block(ModBlocks.TIMBER_FRAME_DIAGONAL, "Half-Timbering (Diagonal)", "Fachwerk (diagonal)");
        block(ModBlocks.TIMBER_FRAME_CROSS, "Half-Timbering (Cross)", "Fachwerk (Kreuz)");

        // --- Dächer ---
        roof("red_roof_tiles", "red_roof_tile", "Red Roof Tiles", "Rote Dachziegel", "Red Roof Tile", "Rote Dachziegel");
        roof("brown_roof_tiles", "brown_roof_tile", "Brown Roof Tiles", "Braune Dachziegel", "Brown Roof Tile", "Braune Dachziegel");
        roof("dark_slate_shingles", "dark_slate_shingle", "Dark Slate Shingles", "Dunkle Schieferplatten",
                "Dark Slate Shingle", "Dunkle Schieferplatten");
        roof("wooden_shingles", "wooden_shingle", "Wooden Shingles", "Holzschindeln", "Wooden Shingle", "Holzschindel");
        roof("light_roof_tiles", "light_roof_tile", "Light Roof Tiles", "Helle Dachziegel", "Light Roof Tile", "Helle Dachziegel");
        roof("copper_roof", "copper_roof", "Copper Roof", "Kupferdach", "Copper Roof", "Kupferdach");

        // --- Erze ---
        block(ModBlocks.CARBON_ORE, "Carbon Ore", "Kohlenstofferz");
        block(ModBlocks.DEEPSLATE_CARBON_ORE, "Deepslate Carbon Ore", "Tiefenschiefer-Kohlenstofferz");
        block(ModBlocks.GEMSTONE_ORE, "Gemstone Ore", "Edelstein-Erz");
        block(ModBlocks.DEEPSLATE_GEMSTONE_ORE, "Deepslate Gemstone Ore", "Tiefenschiefer-Edelstein-Erz");
        block(ModBlocks.KONTERIT_ORE, "Konterit Ore", "Konteriterz");
        block(ModBlocks.DEEPSLATE_KONTERIT_ORE, "Deepslate Konterit Ore", "Tiefenschiefer-Konteriterz");
        block(ModBlocks.AETHERIT_ORE, "Aetherit Ore", "Aetheriterz");
        block(ModBlocks.DEEPSLATE_AETHERIT_ORE, "Deepslate Aetherit Ore", "Tiefenschiefer-Aetheriterz");
        block(ModBlocks.DRACONIUM_ORE, "Draconium Ore", "Draconiumerz");
        block(ModBlocks.DEEPSLATE_DRACONIUM_ORE, "Deepslate Draconium Ore", "Tiefenschiefer-Draconiumerz");
        block(ModBlocks.WYVERNIT_ORE, "Wyvernit Ore", "Wyverniterz");

        // --- Speicherblöcke ---
        block(ModBlocks.CARBON_BLOCK, "Block of Carbon", "Kohlenstoffblock");
        block(ModBlocks.GEMSTONE_BLOCK, "Block of Gemstone", "Edelsteinblock");
        block(ModBlocks.KONTERIT_BLOCK, "Block of Konterit", "Konteritblock");
        block(ModBlocks.AETHERIT_BLOCK, "Block of Aetherit", "Aetheritblock");
        block(ModBlocks.DRACONIUM_BLOCK, "Block of Draconium", "Draconiumblock");
        block(ModBlocks.WYVERNIT_BLOCK, "Block of Wyvernit", "Wyvernitblock");
        block(ModBlocks.STEEL_BLOCK, "Block of Steel", "Stahlblock");

        // --- Rohstoffe ---
        item(ModItems.CARBON, "Carbon", "Kohlenstoff");
        lore("carbon", "Fuel and the basis for steel", "Brennstoff und Grundlage für Stahl");
        item(ModItems.GEMSTONE, "Gemstone", "Edelstein");
        lore("gemstone", "The treasure of the dragons", "Der Schatz der Drachen");
        item(ModItems.KONTERIT, "Konterit", "Konterit");
        lore("konterit", "The ore of the skies – light and very stable", "Das Erz der Lüfte – leicht und sehr stabil");
        item(ModItems.AETHERIT, "Aetherit", "Aetherit");
        lore("aetherit", "The ore of magic", "Das Erz der Magie");
        item(ModItems.DRACONIUM, "Draconium", "Draconium");
        lore("draconium", "The essence of the dragons", "Die Essenz der Drachen");
        item(ModItems.WYVERNIT, "Wyvernit", "Wyvernit");
        lore("wyvernit", "Found high up – for flight technology", "Kommt in großer Höhe vor – für Flugtechnik");
        item(ModItems.STEEL_BLEND, "Steel Blend", "Stahlmischung");
        item(ModItems.STEEL_INGOT, "Steel Ingot", "Stahlbarren");
        lore("steel_ingot", "For steel tools, weapons and armour", "Für Werkzeuge, Waffen und Rüstungen aus Stahl");

        // --- Drachen ---
        egg(DragonType.FIRE, "Fire Dragon Egg", "Feuerdrachen-Ei",
                "The power of the flames", "Die Macht der Flammen",
                "Habitat: volcanic regions, lava caves", "Lebensraum: Vulkanregionen, Lava-Höhlen");
        egg(DragonType.ICE, "Ice Dragon Egg", "Eisdrachen-Ei",
                "The cold of eternity", "Die Kälte der Ewigkeit",
                "Habitat: snowy regions, icebergs", "Lebensraum: Schneeregionen, Eisberge");
        egg(DragonType.LIGHTNING, "Lightning Dragon Egg", "Blitzdrachen-Ei",
                "The force of the storm", "Die Kraft des Sturms",
                "Habitat: high mountains, storm regions", "Lebensraum: hohe Berge, Sturmregionen");
        egg(DragonType.NATURE, "Nature Dragon Egg", "Naturdrachen-Ei",
                "The harmony of the wild", "Die Harmonie der Wildnis",
                "Habitat: forests, jungles, fertile plains", "Lebensraum: Wälder, Dschungel, fruchtbare Ebenen");
        egg(DragonType.EARTH, "Earth Dragon Egg", "Erddrachen-Ei",
                "The strength of the mountains", "Die Stärke der Berge",
                "Habitat: mountains, caves, rocky lands", "Lebensraum: Gebirge, Höhlen, Felslandschaften");
        egg(DragonType.WATER, "Water Dragon Egg", "Wasserdrachen-Ei",
                "The depth of the ocean", "Die Tiefe des Ozeans",
                "Habitat: oceans, deep lakes, underwater caves", "Lebensraum: Ozeane, tiefe Seen, Unterwasserhöhlen");
        hatchHint(DragonType.FIRE, "To hatch: place near lava or fire", "Zum Ausbrüten: nahe Lava oder Feuer legen");
        hatchHint(DragonType.ICE, "To hatch: place on ice or snow", "Zum Ausbrüten: auf Eis oder Schnee legen");
        hatchHint(DragonType.LIGHTNING, "To hatch: thunderstorm or lightning rod", "Zum Ausbrüten: Gewitter oder Blitzableiter");
        hatchHint(DragonType.NATURE, "To hatch: on grass in the light, near plants", "Zum Ausbrüten: im Hellen auf Gras, nahe Pflanzen");
        hatchHint(DragonType.EARTH, "To hatch: underground on stone", "Zum Ausbrüten: unter der Erde auf Stein legen");
        hatchHint(DragonType.WATER, "To hatch: place under water", "Zum Ausbrüten: unter Wasser legen");

        dragon(DragonType.FIRE, "Fire Dragon", "Feuerdrache", "Feuerdrachen");
        dragon(DragonType.ICE, "Ice Dragon", "Eisdrache", "Eisdrachen");
        dragon(DragonType.LIGHTNING, "Lightning Dragon", "Blitzdrache", "Blitzdrachen");
        dragon(DragonType.NATURE, "Nature Dragon", "Naturdrache", "Naturdrachen");
        dragon(DragonType.EARTH, "Earth Dragon", "Erddrache", "Erddrachen");
        dragon(DragonType.WATER, "Water Dragon", "Wasserdrache", "Wasserdrachen");
        text("dragon.kingdomsatwar.stage.1", "Hatchling", "Schlüpfling");
        text("dragon.kingdomsatwar.stage.2", "Young Dragon", "Jungdrache");
        text("dragon.kingdomsatwar.stage.3", "Adolescent Dragon", "Halbstarker Drache");
        text("dragon.kingdomsatwar.stage.4", "Adult Dragon", "Ausgewachsener Drache");
        text("dragon.kingdomsatwar.stage.5", "Ancient Dragon", "Uralter Drache");
        text("message.kingdomsatwar.dragon_grew", "%s has grown: %s!", "%s ist gewachsen: %s!");
        text("message.kingdomsatwar.dragon_mode.0", "%s follows you", "%s folgt dir");
        text("message.kingdomsatwar.dragon_mode.1", "%s stays here", "%s bleibt hier");
        text("message.kingdomsatwar.dragon_mode.2", "%s roams freely", "%s streift frei umher");
        text("message.kingdomsatwar.dragon_ride_hint", "%s: take off / climb · %s: breath · %s: dismount",
                "%s: Abheben / Steigen · %s: Odem · %s: Absteigen");
        text("key.category.kingdomsatwar.dragons", "Kingdoms at War: Dragons", "Kingdoms at War: Drachen");
        text("key.kingdomsatwar.dragon_breath", "Dragon Breath (while riding)", "Drachenodem (beim Reiten)");
        text("message.kingdomsatwar.dragon_hatched", "The %s has hatched and chosen you!", "Der %s ist geschlüpft und hat dich erwählt!");

        item(ModItems.DRAGON_SCALES.get(DragonType.FIRE), "Fire Dragon Scale", "Feuerdrachen-Schuppe");
        item(ModItems.DRAGON_SCALES.get(DragonType.ICE), "Ice Dragon Scale", "Eisdrachen-Schuppe");
        item(ModItems.DRAGON_SCALES.get(DragonType.LIGHTNING), "Lightning Dragon Scale", "Blitzdrachen-Schuppe");
        item(ModItems.DRAGON_SCALES.get(DragonType.NATURE), "Nature Dragon Scale", "Naturdrachen-Schuppe");
        item(ModItems.DRAGON_SCALES.get(DragonType.EARTH), "Earth Dragon Scale", "Erddrachen-Schuppe");
        item(ModItems.DRAGON_SCALES.get(DragonType.WATER), "Water Dragon Scale", "Wasserdrachen-Schuppe");
        lore("dragon_scale", "Dragons shed scales as they grow", "Drachen verlieren beim Wachsen Schuppen");
        item(ModItems.DRAGON_FOOD, "Dragon Food", "Drachenfutter");
        lore("dragon_food", "Heals dragons and makes them grow faster", "Heilt Drachen und lässt sie schneller wachsen");

        // --- Geräusche (Untertitel) ---
        subtitle("entity.dragon.ambient", "Dragon growls", "Drache knurrt");
        subtitle("entity.dragon.baby_ambient", "Young dragon chirps", "Junger Drache fiept");
        subtitle("entity.dragon.hurt", "Dragon hurts", "Drache nimmt Schaden");
        subtitle("entity.dragon.death", "Dragon dies", "Drache stirbt");
        subtitle("entity.dragon.roar", "Dragon roars", "Drache brüllt");
        subtitle("entity.dragon.flap", "Dragon flaps its wings", "Drache schlägt mit den Flügeln");
        subtitle("entity.dragon.step", "Dragon walks", "Drache läuft");
        subtitle("entity.dragon.eat", "Dragon eats", "Drache frisst");
        subtitle("entity.dragon.bite", "Dragon bites", "Drache beißt");
        subtitle("entity.dragon.grow", "Dragon grows", "Drache wächst");
        subtitle("entity.dragon.breath.fire", "Dragon breathes fire", "Drache speit Feuer");
        subtitle("entity.dragon.breath.ice", "Dragon breathes frost", "Drache speit Frost");
        subtitle("entity.dragon.breath.lightning", "Dragon breathes lightning", "Drache speit Blitze");
        subtitle("entity.dragon.breath.nature", "Dragon breathes spores", "Drache speit Sporen");
        subtitle("entity.dragon.breath.earth", "Dragon breathes rubble", "Drache speit Geröll");
        subtitle("entity.dragon.breath.water", "Dragon breathes water", "Drache speit Wasser");
        subtitle("block.dragon_egg.crack", "Dragon egg cracks", "Drachenei knackt");
        subtitle("block.dragon_egg.hatch", "Dragon hatches", "Drache schlüpft");
        subtitle("intro.narration", "Narrator speaks", "Erzähler spricht");
        subtitle("intro.music", "Intro music plays", "Intro-Musik spielt");
        subtitle("voice.soldier.recruit", "Soldier reports for duty", "Soldat meldet sich zum Dienst");
        subtitle("voice.soldier.follow", "Soldier follows", "Soldat folgt");
        subtitle("voice.soldier.hold", "Soldier holds position", "Soldat hält die Stellung");
        subtitle("voice.soldier.attack", "Soldier battle cry", "Soldat ruft zum Angriff");
        subtitle("voice.soldier.victory", "Soldier cheers", "Soldat jubelt");
        subtitle("voice.soldier.idle", "Soldier mutters", "Soldat murmelt");

        // --- Erzähl-Intro ---
        text("intro.kingdomsatwar.line1", "Once, this world lay silent and untouched.", "Einst lag diese Welt still und unberührt.");
        text("intro.kingdomsatwar.line2", "Then the kingdoms rose – with walls of stone and towers that reached up to the clouds.",
                "Dann erhoben sich die Königreiche – mit Mauern aus Stein und Türmen, die bis an die Wolken reichten.");
        text("intro.kingdomsatwar.line3", "But deep within the mountains, in volcanoes, ice and sea, the dragons awoke.",
                "Doch tief in den Bergen, in Vulkanen, Eis und Meer, erwachten die Drachen.");
        text("intro.kingdomsatwar.line4", "Six kinds – born of fire, ice, lightning, nature, earth and water.",
                "Sechs Arten – geboren aus Feuer, Eis, Blitz, Natur, Erde und Wasser.");
        text("intro.kingdomsatwar.line5", "Whoever finds their eggs and raises them with patience gains an ally mightier than any army.",
                "Wer ihre Eier findet und sie mit Geduld aufzieht, gewinnt einen Verbündeten, mächtiger als jede Armee.");
        text("intro.kingdomsatwar.line6", "Deep below, rare ores lie waiting: Draconium, Aetherit, Konterit – the stuff that legends are forged from.",
                "In der Tiefe warten seltene Erze: Draconium, Aetherit, Konterit – der Stoff, aus dem Legenden geschmiedet werden.");
        text("intro.kingdomsatwar.line7", "Gather knights and soldiers around you, raise your castle and defend your land.",
                "Sammle Ritter und Soldaten um dich, errichte deine Burg und verteidige dein Land.");
        text("intro.kingdomsatwar.line8", "The world is ready. The dragons are waiting.", "Die Welt ist bereit. Die Drachen warten.");
        text("intro.kingdomsatwar.line9", "This is your kingdom.", "Dies ist dein Königreich.");
        text("intro.kingdomsatwar.title", "Kingdoms at War – Intro", "Kingdoms at War – Intro");
        text("intro.kingdomsatwar.skip", "Space: skip", "Leertaste: überspringen");
        text("kingdomsatwar.intro.intro_music", "Kingdoms at War – The Dragons Awaken", "Kingdoms at War – Das Erwachen der Drachen");

        // --- Einstellungen ---
        text("kingdomsatwar.configuration.title", "Kingdoms at War – Settings", "Kingdoms at War – Einstellungen");
        text("kingdomsatwar.configuration.section.kingdomsatwar.client.toml", "Game settings", "Spiel-Einstellungen");
        text("kingdomsatwar.configuration.section.kingdomsatwar.client.toml.title", "Kingdoms at War – Game settings",
                "Kingdoms at War – Spiel-Einstellungen");
        text("kingdomsatwar.configuration.showIntro", "Show intro in new worlds", "Intro in neuen Welten zeigen");
        text("kingdomsatwar.configuration.showIntro.tooltip",
                "Plays the narrated intro the first time you enter a world. You can watch it again at any time with /kaw intro.",
                "Spielt beim ersten Betreten einer Welt das Erzähl-Intro ab. Mit /kaw intro kannst du es jederzeit wieder ansehen.");

        // --- Armee: Soldaten, Banditen, Rekrutierungstisch, Kommandostab ---
        army();
        equipment();
        creatures();
        decor();
        magic();

        // --- Landwirtschaft ---
        item(ModItems.TOMATO_SEEDS, "Tomato Seeds", "Tomatensamen");
        item(ModItems.TOMATO, "Tomato", "Tomate");
        item(ModItems.CABBAGE_SEEDS, "Cabbage Seeds", "Kohlsamen");
        item(ModItems.CABBAGE, "Cabbage", "Kohl");
        item(ModItems.ONION, "Onion", "Zwiebel");
        item(ModItems.BARLEY_SEEDS, "Barley Seeds", "Gerstensamen");
        item(ModItems.BARLEY, "Barley", "Gerste");
        block(ModBlocks.TOMATOES, "Tomatoes", "Tomaten");
        block(ModBlocks.CABBAGES, "Cabbages", "Kohl");
        block(ModBlocks.ONIONS, "Onions", "Zwiebeln");
        block(ModBlocks.BARLEY, "Barley", "Gerste");
        item(ModItems.BREAD_ROLL, "Bread Roll", "Brötchen");
        item(ModItems.VEGETABLE_STEW, "Vegetable Stew", "Gemüseeintopf");
        item(ModItems.MEAT_PIE, "Meat Pie", "Fleischpastete");
        item(ModItems.APPLE_PIE, "Apple Pie", "Apfelkuchen");

        block(ModBlocks.LAVENDER, "Lavender", "Lavendel");
        block(ModBlocks.GARDEN_ROSE, "Garden Rose", "Gartenrose");
        block(ModBlocks.FIRE_LILY, "Fire Lily", "Feuerlilie");
        block(ModBlocks.FROST_FLOWER, "Frost Flower", "Frostblume");
        block(ModBlocks.POTTED_LAVENDER, "Potted Lavender", "Lavendel im Topf");
        block(ModBlocks.POTTED_GARDEN_ROSE, "Potted Garden Rose", "Gartenrose im Topf");
        block(ModBlocks.POTTED_FIRE_LILY, "Potted Fire Lily", "Feuerlilie im Topf");
        block(ModBlocks.POTTED_FROST_FLOWER, "Potted Frost Flower", "Frostblume im Topf");
        block(ModBlocks.GROVE_BELL, "Grove Bell", "Hainglocke");
        block(ModBlocks.POTTED_GROVE_BELL, "Potted Grove Bell", "Hainglocke im Topf");
        block(ModBlocks.LIFE_BLOSSOM, "Life Blossom", "Lebensblüte");
        block(ModBlocks.POTTED_LIFE_BLOSSOM, "Potted Life Blossom", "Lebensblüte im Topf");
        block(ModBlocks.MOONFLOWER, "Moonflower", "Mondblume");
        block(ModBlocks.POTTED_MOONFLOWER, "Potted Moonflower", "Mondblume im Topf");

        // --- Hopfen, Trauben, Knoblauch, Getränke, Festmahl ---
        block(ModBlocks.HOPS, "Hops", "Hopfen");
        block(ModBlocks.GRAPES, "Grapevine", "Weinrebe");
        block(ModBlocks.GARLIC_CROP, "Garlic", "Knoblauch");
        item(ModItems.HOPS, "Hops", "Hopfen");
        item(ModItems.GRAPES, "Grapes", "Weintrauben");
        item(ModItems.GARLIC, "Garlic", "Knoblauch");
        item(ModItems.GARLIC_BREAD, "Garlic Bread", "Knoblauchbrot");
        item(ModItems.ALE, "Ale", "Bier");
        lore("ale", "Strength for a while – too much makes you dizzy", "Stärke für eine Weile – zu viel macht schwindelig");
        item(ModItems.WINE, "Red Wine", "Rotwein");
        lore("wine", "Warms the heart and slowly heals", "Wärmt das Herz und heilt langsam");
        item(ModItems.MEAD, "Mead", "Met");
        lore("mead", "The drink of heroes: extra hearts for two minutes", "Der Trank der Helden: Extraherzen für zwei Minuten");
        block(ModBlocks.FEAST, "Feast", "Festmahl");
        lore("feast", "Place it on the table – four hearty servings", "Auf die Tafel stellen – vier üppige Portionen");
    }

    /** Die Schattenwelt: Biome, Blöcke, Meldungen. */
    private void shadowWorld() {
        text("itemGroup.kingdomsatwar.shadow_world", "Kingdoms at War: Shadows", "Kingdoms at War: Schatten");
        text("block.kingdomsatwar.bed.shadow_world", "No one finds sleep in the Shadow World – but your bed remembers where you wake.",
                "In der Schattenwelt findet niemand Schlaf – doch dein Bett merkt sich, wo du erwachst.");

        // Biome
        text("biome.kingdomsatwar.shadow_forest", "Shadow Forest", "Schattenwald");
        text("biome.kingdomsatwar.pale_wastes", "Pale Wastes", "Bleiche Öde");
        text("biome.kingdomsatwar.crystal_chasms", "Crystal Chasms", "Kristallschluchten");
        text("biome.kingdomsatwar.soul_marsh", "Soul Marsh", "Seelensumpf");
        text("biome.kingdomsatwar.shadow_peaks", "Shadow Peaks", "Schattengipfel");
        text("biome.kingdomsatwar.glimmer_meadows", "Glimmer Meadows", "Glimmerweiten");
        text("biome.kingdomsatwar.abyssal_sea", "Abyssal Sea", "Abgrundmeer");
        text("biome.kingdomsatwar.floating_isles", "Floating Isles", "Schwebende Inseln");
        text("biome.kingdomsatwar.shadow_depths", "Shadow Depths", "Schattentiefen");

        // Gestein
        set("shadow_stone", "shadow_stone", "Shadow Stone", "Schattenstein", "Shadow Stone", "Schattenstein");
        set("shadow_stone_bricks", "shadow_stone_brick", "Shadow Stone Bricks", "Schattensteinziegel", "Shadow Stone Brick",
                "Schattensteinziegel");
        set("polished_shadow_stone", "polished_shadow_stone", "Polished Shadow Stone", "Polierter Schattenstein",
                "Polished Shadow Stone", "Polierte Schattenstein");
        block(ShadowBlocks.CRACKED_SHADOW_STONE_BRICKS, "Cracked Shadow Stone Bricks", "Rissige Schattensteinziegel");
        block(ShadowBlocks.CHISELED_SHADOW_STONE, "Chiseled Shadow Stone", "Gemeißelter Schattenstein");
        block(ShadowBlocks.DEEP_SHADOW_STONE, "Deep Shadow Stone", "Tiefschattenstein");

        // Böden
        block(ShadowBlocks.SHADOW_SOIL, "Shadow Soil", "Schattenerde");
        block(ShadowBlocks.SHADOW_GRASS, "Shadow Grass Block", "Schattengrasblock");
        block(ShadowBlocks.SHADOW_SAND, "Shadow Sand", "Schattensand");
        block(ShadowBlocks.ASH_SNOW_BLOCK, "Ash Snow Block", "Aschschneeblock");
        block(ShadowBlocks.ASH_SNOW, "Ash Snow", "Aschschnee");

        // Holz
        block(ShadowBlocks.SHADOW_LOG, "Shadow Log", "Schattenholzstamm");
        block(ShadowBlocks.SHADOW_WOOD, "Shadow Wood", "Schattenholz");
        block(ShadowBlocks.STRIPPED_SHADOW_LOG, "Stripped Shadow Log", "Entrindeter Schattenholzstamm");
        block(ShadowBlocks.STRIPPED_SHADOW_WOOD, "Stripped Shadow Wood", "Entrindetes Schattenholz");
        roof("shadow_planks", "shadow_plank", "Shadow Planks", "Schattenholzbretter", "Shadow Wood", "Schattenholz");
        block(ShadowBlocks.SHADOW_LEAVES, "Shadow Leaves", "Schattenlaub");

        // Licht und Pflanzen
        block(ShadowBlocks.NIGHT_CRYSTAL, "Night Crystal", "Nachtkristall");
        block(ShadowBlocks.NIGHT_CRYSTAL_CLUSTER, "Night Crystal Cluster", "Nachtkristalldruse");
        block(ShadowBlocks.GLIMMER_MOSS, "Glimmer Moss", "Glimmmoos");
        block(ShadowBlocks.GHOSTCAP, "Ghostcap", "Geisterpilz");
        block(ShadowBlocks.SHADOW_SPROUTS, "Shadow Sprouts", "Schattenhalme");
        block(ShadowBlocks.PALE_GRASS, "Pale Grass", "Bleichgras");
        block(ShadowBlocks.SHADOW_SAPLING, "Shadow Sapling", "Schattensetzling");
        block(ShadowBlocks.POTTED_SHADOW_SAPLING, "Potted Shadow Sapling", "Schattensetzling im Topf");
        block(ShadowBlocks.MOON_LANTERN, "Moon Lantern", "Mondlaterne");
        block(ShadowBlocks.SOUL_BERRY_BUSH, "Soul Berry Bush", "Seelenbeerenstrauch");
        block(ShadowBlocks.GLIMMER_VINES, "Glimmer Vines", "Glimmerranken");
        block(ShadowBlocks.GLIMMER_VINES_PLANT, "Glimmer Vines Plant", "Glimmerranken");
        item(ShadowItems.SOUL_BERRIES, "Soul Berries", "Seelenbeeren");
        lore("soul_berries", "Lets you see in the dark for a while", "Lässt dich eine Weile im Dunkeln sehen");

        // Schattenholz, Acker, Pfad, Dämmerkorn
        block(ShadowBlocks.SHADOW_DOOR, "Shadow Door", "Schattenholztür");
        block(ShadowBlocks.SHADOW_TRAPDOOR, "Shadow Trapdoor", "Schattenholzfalltür");
        block(ShadowBlocks.SHADOW_FENCE, "Shadow Fence", "Schattenholzzaun");
        block(ShadowBlocks.SHADOW_FENCE_GATE, "Shadow Fence Gate", "Schattenholzzauntor");
        block(ShadowBlocks.SHADOW_FARMLAND, "Shadow Farmland", "Schattenackerland");
        block(ShadowBlocks.SHADOW_PATH, "Shadow Path", "Schattenpfad");
        block(ShadowBlocks.DUSKGRAIN, "Duskgrain", "Dämmerkorn");
        item(ShadowItems.DUSKGRAIN_SEEDS, "Duskgrain Seeds", "Dämmerkorn-Saat");
        item(ShadowItems.DUSKGRAIN, "Duskgrain", "Dämmerkorn");
        item(ShadowItems.DUSK_BREAD, "Dusk Bread", "Dämmerbrot");
        lore("dusk_bread", "The bread of the Umbrer – dark, hearty and filling", "Das Brot der Umbrer – dunkel, kräftig und sättigend");

        // Kreaturen
        creature(ModEntities.SHADE_STALKER, ShadowItems.SHADE_STALKER_SPAWN_EGG, "Shade Stalker", "Schattenpirscher", "Schattenpirscher");
        creature(ModEntities.CRYSTAL_CRAWLER, ShadowItems.CRYSTAL_CRAWLER_SPAWN_EGG, "Crystal Crawler", "Kristallkriecher", "Kristallkriecher");
        creature(ModEntities.NIGHT_WING, ShadowItems.NIGHT_WING_SPAWN_EGG, "Night Wing", "Nachtschwinge", "Nachtschwingen");
        creature(ModEntities.MOON_STAG, ShadowItems.MOON_STAG_SPAWN_EGG, "Moon Stag", "Mondhirsch", "Mondhirsch");
        creature(ModEntities.GLIMMER_MOTH, ShadowItems.GLIMMER_MOTH_SPAWN_EGG, "Glimmer Moth", "Glimmerfalter", "Glimmerfalter");
        item(ShadowItems.SHADOW_ESSENCE, "Shadow Essence", "Schattenessenz");

        // Die Umbrer
        creature(ModEntities.UMBRER, ShadowItems.UMBRER_SPAWN_EGG, "Umbrer", "Umbrer", "Umbrer");
        creature(ModEntities.UMBRER_GUARD, ShadowItems.UMBRER_GUARD_SPAWN_EGG, "Umbrer Guard", "Umbrer-Wächter", "Umbrer-Wächter");
        text("entity.kingdomsatwar.umbrer.moonsmith", "Moonsmith", "Mondschmied");
        text("entity.kingdomsatwar.umbrer.herbalist", "Herbalist", "Kräuterkundige");
        text("entity.kingdomsatwar.umbrer.crystal_cutter", "Crystal Cutter", "Kristallschleifer");
        text("entity.kingdomsatwar.umbrer.seer", "Seer", "Seher");
        text("entity.kingdomsatwar.umbrer.farmer", "Dusk Farmer", "Dämmerbauer");
        text("entity.kingdomsatwar.umbrer.guard", "Umbrer Guard", "Umbrer-Wächter");
        text("entity.kingdomsatwar.umbrer_guard.greeting1", "Welcome, wanderer from the light.", "Willkommen, Wanderer aus dem Licht.");
        text("entity.kingdomsatwar.umbrer_guard.greeting2", "The lanterns keep the shadows away. Stay in their glow.",
                "Die Laternen halten die Schatten fern. Bleib in ihrem Schein.");
        text("entity.kingdomsatwar.umbrer_guard.greeting3", "We have kept watch ever since the seal was closed.",
                "Wir halten Wache, seit das Siegel geschlossen wurde.");
        text("entity.kingdomsatwar.umbrer_guard.greeting4", "Beware the stalkers. Look them in the eye – never look away.",
                "Hüte dich vor den Pirschern. Sieh ihnen in die Augen – schau nie weg.");
        text("entity.kingdomsatwar.umbrer_guard.greeting5", "Moonsilver opens every door in this village.",
                "Mondsilber öffnet dir jede Tür in diesem Dorf.");
        // Die Chronik der Umbrer (beim Seher zu haben)
        text("book.kingdomsatwar.umbrer_chronicle.title", "Chronicle of the Umbrer", "Chronik der Umbrer");
        text("book.kingdomsatwar.umbrer_chronicle.page1",
                "THE CHRONICLE OF THE UMBRER\n\nwritten down by Ilvaren, seer of the Glimmer Meadows\n\nfor those who come from the light",
                "DIE CHRONIK DER UMBRER\n\naufgeschrieben von Ilvaren, Seher der Glimmerweiten\n\nfür jene, die aus dem Licht kommen");
        text("book.kingdomsatwar.umbrer_chronicle.page2",
                "Before the seal, the world was one. We Umbrer lived in the valleys between light and dark – until Nyxar awoke, the dragon who devours light.",
                "Vor dem Siegel war die Welt eins. Wir Umbrer lebten in den Tälern zwischen Licht und Dunkel – bis Nyxar erwachte, der Drache, der das Licht frisst.");
        text("book.kingdomsatwar.umbrer_chronicle.page3",
                "Six dragons stood against him. They could not slay him, but they locked him away – and with him this whole land – behind a seal. We stayed to guard him.",
                "Sechs Drachen stellten sich ihm entgegen. Töten konnten sie ihn nicht, doch sie verschlossen ihn – und mit ihm dieses ganze Land – hinter einem Siegel. Wir blieben, um ihn zu bewachen.");
        text("book.kingdomsatwar.umbrer_chronicle.page4",
                "For centuries we have lived in eternal twilight. We build towers of shadow stone, grow duskgrain and trade in moonsilver. The shades that stalk between the trees are Nyxar's brood.",
                "Seit Jahrhunderten leben wir im ewigen Zwielicht. Wir bauen Türme aus Schattenstein, ziehen Dämmerkorn und tauschen in Mondsilber. Die Schatten, die zwischen den Bäumen pirschen, sind Nyxars Brut.");
        text("book.kingdomsatwar.umbrer_chronicle.page5",
                "Now the seal is broken. Nyxar sleeps in his citadel, but he will wake. Wanderer from the light: gather nightsteel and abyssite. And never look away when a shadow looks at you.",
                "Nun ist das Siegel gebrochen. Nyxar schläft in seiner Zitadelle, doch er wird erwachen. Wanderer aus dem Licht: Sammle Nachtstahl und Abyssit. Und schau nie weg, wenn dich ein Schatten ansieht.");
        lore("shadow_essence", "What remains of a shade – sought after by the Umbrer", "Was von einem Schatten bleibt – begehrt bei den Umbrern");

        // Erze und Speicherblöcke
        block(ShadowBlocks.MOONSILVER_ORE, "Moonsilver Ore", "Mondsilbererz");
        block(ShadowBlocks.DEEP_MOONSILVER_ORE, "Deep Moonsilver Ore", "Tiefes Mondsilbererz");
        block(ShadowBlocks.NIGHTSTEEL_ORE, "Nightsteel Ore", "Nachtstahlerz");
        block(ShadowBlocks.DEEP_NIGHTSTEEL_ORE, "Deep Nightsteel Ore", "Tiefes Nachtstahlerz");
        block(ShadowBlocks.SOUL_CRYSTAL_ORE, "Soul Crystal Ore", "Seelenkristallerz");
        block(ShadowBlocks.ABYSSITE_ORE, "Abyssite Ore", "Abyssiterz");
        block(ShadowBlocks.MOONSILVER_BLOCK, "Block of Moonsilver", "Mondsilberblock");
        block(ShadowBlocks.NIGHTSTEEL_BLOCK, "Block of Nightsteel", "Nachtstahlblock");
        block(ShadowBlocks.SOUL_CRYSTAL_BLOCK, "Block of Soul Crystal", "Seelenkristallblock");
        block(ShadowBlocks.ABYSSITE_BLOCK, "Block of Abyssite", "Abyssitblock");

        // Rohstoffe
        item(ShadowItems.RAW_MOONSILVER, "Raw Moonsilver", "Rohmondsilber");
        item(ShadowItems.MOONSILVER_INGOT, "Moonsilver Ingot", "Mondsilberbarren");
        item(ShadowItems.MOONSILVER_NUGGET, "Moonsilver Nugget", "Mondsilberklümpchen");
        item(ShadowItems.RAW_NIGHTSTEEL, "Raw Nightsteel", "Rohnachtstahl");
        item(ShadowItems.NIGHTSTEEL_INGOT, "Nightsteel Ingot", "Nachtstahlbarren");
        item(ShadowItems.SOUL_CRYSTAL, "Soul Crystal", "Seelenkristall");
        lore("soul_crystal", "Something whispers inside it", "Darin flüstert etwas");
        item(ShadowItems.ABYSSITE_SHARD, "Abyssite Shard", "Abyssitsplitter");
        item(ShadowItems.ABYSSITE_INGOT, "Abyssite Ingot", "Abyssitbarren");
        item(ShadowItems.NIGHT_CRYSTAL_SHARD, "Night Crystal Shard", "Nachtkristallsplitter");
        item(ShadowItems.ABYSSITE_UPGRADE_SMITHING_TEMPLATE, "Smithing Template", "Schmiedevorlage");
        text("item.kingdomsatwar.smithing_template.abyssite_upgrade.applies_to", "Nightsteel Equipment", "Nachtstahl-Ausrüstung");
        text("item.kingdomsatwar.smithing_template.abyssite_upgrade.ingredients", "Abyssite Ingot", "Abyssitbarren");
        text("item.kingdomsatwar.smithing_template.abyssite_upgrade.base_slot_description",
                "Add nightsteel armor, weapon, or tool", "Nachtstahl-Rüstung, -Waffe oder -Werkzeug hinzufügen");
        text("item.kingdomsatwar.smithing_template.abyssite_upgrade.additions_slot_description",
                "Add Abyssite Ingot", "Abyssitbarren hinzufügen");

        // Nachtstahl
        item(ShadowItems.NIGHTSTEEL_SWORD, "Nightsteel Sword", "Nachtstahlschwert");
        item(ShadowItems.NIGHTSTEEL_PICKAXE, "Nightsteel Pickaxe", "Nachtstahlspitzhacke");
        item(ShadowItems.NIGHTSTEEL_AXE, "Nightsteel Axe", "Nachtstahlaxt");
        item(ShadowItems.NIGHTSTEEL_SHOVEL, "Nightsteel Shovel", "Nachtstahlschaufel");
        item(ShadowItems.NIGHTSTEEL_HOE, "Nightsteel Hoe", "Nachtstahlhacke");
        item(ShadowItems.NIGHTSTEEL_HELMET, "Nightsteel Helmet", "Nachtstahlhelm");
        item(ShadowItems.NIGHTSTEEL_CHESTPLATE, "Nightsteel Chestplate", "Nachtstahlharnisch");
        item(ShadowItems.NIGHTSTEEL_LEGGINGS, "Nightsteel Leggings", "Nachtstahlbeinschutz");
        item(ShadowItems.NIGHTSTEEL_BOOTS, "Nightsteel Boots", "Nachtstahlstiefel");
        lore("nightsteel_helmet", "Lets you see clearly in the Shadow World", "Lässt dich in der Schattenwelt klar sehen");

        // Abyssit
        item(ShadowItems.ABYSSITE_SWORD, "Abyssite Sword", "Abyssitschwert");
        item(ShadowItems.ABYSSITE_PICKAXE, "Abyssite Pickaxe", "Abyssitspitzhacke");
        item(ShadowItems.ABYSSITE_AXE, "Abyssite Axe", "Abyssitaxt");
        item(ShadowItems.ABYSSITE_SHOVEL, "Abyssite Shovel", "Abyssitschaufel");
        item(ShadowItems.ABYSSITE_HOE, "Abyssite Hoe", "Abyssithacke");
        item(ShadowItems.ABYSSITE_HELMET, "Abyssite Helmet", "Abyssithelm");
        item(ShadowItems.ABYSSITE_CHESTPLATE, "Abyssite Chestplate", "Abyssitharnisch");
        item(ShadowItems.ABYSSITE_LEGGINGS, "Abyssite Leggings", "Abyssitbeinschutz");
        item(ShadowItems.ABYSSITE_BOOTS, "Abyssite Boots", "Abyssitstiefel");
        lore("abyssite_boots", "You never take fall damage", "Du nimmst nie Fallschaden");

        // Mondsilber
        item(ShadowItems.MOONSILVER_BLADE, "Moonsilver Blade", "Mondsilberklinge");
        lore("moonsilver_blade", "Strikes shadow creatures and the undead hard", "Trifft Schattenwesen und Untote hart");
        text(ModItems.loreKey("moonsilver_blade", 2), "Struck foes glow brightly", "Getroffene leuchten hell auf");
    }

    private void bosses() {
        add(ModEntities.PYRRHON.get(), pick("Pyrrhon", "Pyrrhon"));
        text("boss.kingdomsatwar.angered", "%s grows furious!", "%s wird wütend!");
        text("boss.kingdomsatwar.enraged", "%s flies into a rage!", "%s gerät in Raserei!");
        text("boss.kingdomsatwar.last_stand", "%s rears up for a final stand!", "%s bäumt sich ein letztes Mal auf!");
        text("boss.kingdomsatwar.fire", "Pyrrhon, the Ash Lord", "Pyrrhon, der Aschenfürst");
        text("boss.kingdomsatwar.fire.title", "The Ash Lord", "Der Aschenfürst");
        add(ModEntities.GLACIARA.get(), pick("Glaciara", "Glaciara"));
        text("boss.kingdomsatwar.ice.title", "The Frost Queen", "Die Frostkönigin");
        add(ModEntities.VERDANTIS.get(), pick("Verdantis", "Verdantis"));
        text("boss.kingdomsatwar.nature.title", "Guardian of the Grove", "Die Hüterin des Hains");
        add(ModEntities.VINELING.get(), pick("Vineling", "Rankling"));
        add(ModEntities.TERRAGON.get(), pick("Terragon", "Terragon"));
        text("boss.kingdomsatwar.earth.title", "The Mountainbreaker", "Der Bergbrecher");
        add(ModEntities.VOLTARION.get(), pick("Voltarion", "Voltarion"));
        text("boss.kingdomsatwar.lightning.title", "The Stormbringer", "Der Sturmbringer");
        add(ModEntities.THALASSA.get(), pick("Thalassa", "Thalassa"));
        text("boss.kingdomsatwar.water.title", "Mistress of the Deep", "Die Herrin der Tiefe");
        add(ModEntities.BOSS_PROJECTILE.get(), pick("Dragon Projectile", "Drachengeschoss"));
        add(ModEntities.SEAL_BREAK.get(), pick("Seal Break", "Siegelbruch"));
        text("command.kingdomsatwar.boss.summoned", "%s awakens!", "%s erwacht!");
        lairs();
        story();
    }

    /** Die sechs Drachennester: Namen, Warnungen beim Näherkommen, Nestkern, Drachenkompass und Befehle. */
    private void lairs() {
        lair(DragonType.FIRE, "Pyrrhon's Volcanic Crater", "Pyrrhons Vulkankrater",
                "The air shimmers with heat… something vast slumbers here.",
                "Die Luft flimmert vor Hitze … etwas Gewaltiges schläft hier.");
        lair(DragonType.ICE, "Glaciara's Glacier Basin", "Glaciaras Gletscherkessel",
                "Your breath freezes… beneath the ice, something stirs.",
                "Dein Atem gefriert … unter dem Eis regt sich etwas.");
        lair(DragonType.LIGHTNING, "Voltarion's Storm Peaks", "Voltarions Sturmgipfel",
                "The air crackles… a storm lies in wait here.",
                "Die Luft knistert … hier lauert ein Sturm.");
        lair(DragonType.NATURE, "Verdantis' Ancient Grove", "Verdantis' Uralter Hain",
                "The trees whisper… the grove is watching you.",
                "Die Bäume flüstern … der Hain beobachtet dich.");
        lair(DragonType.EARTH, "Terragon's Rift", "Terragons Felsschlucht",
                "The ground trembles beneath your feet…",
                "Der Boden bebt unter deinen Füßen …");
        lair(DragonType.WATER, "Thalassa's Sunken Grotto", "Thalassas Versunkene Grotte",
                "The water turns cold and dark… something vast circles below you.",
                "Das Wasser wird kalt und dunkel … etwas Gewaltiges zieht unter dir seine Kreise.");
        block(ModBlocks.DRAGON_LAIR_CORE, "Lair Core", "Nestkern");

        item(ModItems.DRAGON_COMPASS, "Dragon Compass", "Drachenkompass");
        lore("dragon_compass", "Shows the way to the lairs of the great dragons", "Weist den Weg zu den Nestern der großen Drachen");
        text("item.kingdomsatwar.dragon_compass.unaimed", "Right-click to align the needle", "Rechtsklick richtet die Nadel aus");
        text("item.kingdomsatwar.dragon_compass.seeking", "Seeking: %s", "Sucht: %s");
        text("item.kingdomsatwar.dragon_compass.defeated", "Its dragon has been defeated.", "Sein Drache ist besiegt.");
        text("item.kingdomsatwar.dragon_compass.hint", "Right-click: direction · Sneak + right-click: next lair",
                "Rechtsklick: Richtung · Schleichen + Rechtsklick: nächstes Nest");
        text("item.kingdomsatwar.dragon_compass.aimed", "%s – %s", "%s – %s");
        text("item.kingdomsatwar.dragon_compass.distance", "about %s blocks to the %s", "etwa %s Blöcke im %s");
        text("item.kingdomsatwar.dragon_compass.here", "right here!", "genau hier!");
        text("item.kingdomsatwar.dragon_compass.other_dimension", "in the Overworld", "in der Oberwelt");
        text("item.kingdomsatwar.dragon_compass.defeated_suffix", " (defeated)", " (besiegt)");
        text("item.kingdomsatwar.dragon_compass.no_nests", "The needle trembles – this world has no dragon lairs.",
                "Die Nadel zittert – in dieser Welt gibt es keine Drachennester.");
        String[][] directions = {{"n", "north", "Norden"}, {"ne", "northeast", "Nordosten"}, {"e", "east", "Osten"},
                {"se", "southeast", "Südosten"}, {"s", "south", "Süden"}, {"sw", "southwest", "Südwesten"},
                {"w", "west", "Westen"}, {"nw", "northwest", "Nordwesten"}};
        for (String[] d : directions) {
            text("direction.kingdomsatwar." + d[0], d[1], d[2]);
        }

        text("command.kingdomsatwar.lair.header", "Dragon lairs of this world:", "Drachennester dieser Welt:");
        text("command.kingdomsatwar.lair.entry", "%s at %s, %s – %s", "%s bei %s, %s – %s");
        text("command.kingdomsatwar.lair.unexplored", "not yet discovered", "noch nicht erkundet");
        text("command.kingdomsatwar.lair.phase.dormant", "asleep", "schlafend");
        text("command.kingdomsatwar.lair.phase.awake", "awake", "erwacht");
        text("command.kingdomsatwar.lair.phase.defeated", "defeated", "besiegt");
        text("command.kingdomsatwar.lair.none", "This world has no dragon lairs.", "In dieser Welt gibt es keine Drachennester.");
        text("command.kingdomsatwar.lair.no_core", "There is no lair core near you.", "In deiner Nähe gibt es keinen Nestkern.");
        text("command.kingdomsatwar.lair.awakened", "The dragon of %s awakens!", "Der Drache von %s erwacht!");
        text("command.kingdomsatwar.lair.cannot_awaken", "This dragon cannot awaken (yet).", "Dieser Drache kann (noch) nicht erwachen.");
        text("command.kingdomsatwar.lair.reset", "%s sleeps again – its dragon can be challenged anew.",
                "%s schläft wieder – sein Drache kann erneut herausgefordert werden.");
    }

    /** Die Geschichte „Das Siegel“: Siegelkreis, Splitter, Chronik, Kapitel, Nachrichten und Befehle. */
    private void story() {
        // Drachen (Namen für Chronik und Nachrichten)
        String[][] dragons = {
                {"fire", "Pyrrhon", "Pyrrhon, the Ash Lord", "Pyrrhon, der Aschenfürst", "A dragon of fire", "Ein Drache des Feuers",
                        "slumbers in a hot, dry land", "schläft in einem heißen, trockenen Land", "Pyrrhon's", "Pyrrhons"},
                {"ice", "Glaciara", "Glaciara, the Frost Queen", "Glaciara, die Frostkönigin", "A dragon of ice", "Ein Drache des Eises",
                        "rests in eternal ice and snow", "ruht in ewigem Eis und Schnee", "Glaciara's", "Glaciaras"},
                {"lightning", "Voltarion", "Voltarion, the Stormbringer", "Voltarion, der Sturmbringer", "A dragon of storms",
                        "Ein Drache der Stürme", "lurks high on the jagged peaks", "lauert hoch auf den zerklüfteten Gipfeln", "Voltarion's",
                        "Voltarions"},
                {"nature", "Verdantis", "Verdantis, Guardian of the Grove", "Verdantis, die Hüterin des Hains", "A dragon of the forest",
                        "Ein Drache des Waldes", "watches deep within an ancient forest", "wacht tief in einem uralten Wald", "Verdantis'",
                        "Verdantis'"},
                {"earth", "Terragon", "Terragon, the Mountainbreaker", "Terragon, der Bergbrecher", "A dragon of stone",
                        "Ein Drache des Gesteins", "sleeps among windswept hills and rocks", "schläft zwischen windgepeitschten Hügeln und Felsen",
                        "Terragon's", "Terragons"},
                {"water", "Thalassa", "Thalassa, Mistress of the Deep", "Thalassa, die Herrin der Tiefe", "A dragon of the deep",
                        "Ein Drache der Tiefe", "circles at the bottom of the deep ocean", "zieht auf dem Grund des tiefen Ozeans seine Kreise",
                        "Thalassa's", "Thalassas"}};
        for (String[] d : dragons) {
            text("lair.kingdomsatwar." + d[0] + ".dragon", d[1], d[1]);
            if (!d[0].equals("fire")) {
                text("boss.kingdomsatwar." + d[0], d[2], d[3]);
            }
            text("lair.kingdomsatwar." + d[0] + ".unknown", d[4], d[5]);
            text("lair.kingdomsatwar." + d[0] + ".hint", d[6], d[7]);
            item(ModItems.SEAL_SHARDS.get(DragonType.byId(d[0])), d[8] + " Seal Shard", d[9] + " Siegelsplitter");
        }
        lore("seal_shard", "A sixth of the ancient seal", "Ein Sechstel des uralten Siegels");
        text(ModItems.loreKey("seal_shard", 2), "Belongs in its monolith at the Seal Circle", "Gehört in seinen Monolithen am Siegelkreis");

        // Siegelkreis
        set("sealstone", "sealstone", "Sealstone", "Siegelstein", "Sealstone", "Siegelstein");
        set("sealstone_bricks", "sealstone_brick", "Sealstone Bricks", "Siegelsteinziegel", "Sealstone Brick", "Siegelsteinziegel");
        block(ModBlocks.CRACKED_SEALSTONE_BRICKS, "Cracked Sealstone Bricks", "Rissige Siegelsteinziegel");
        block(ModBlocks.MOSSY_SEALSTONE_BRICKS, "Mossy Sealstone Bricks", "Bemooste Siegelsteinziegel");
        block(ModBlocks.POLISHED_SEALSTONE, "Polished Sealstone", "Polierter Siegelstein");
        block(ModBlocks.CHISELED_SEALSTONE, "Chiseled Sealstone", "Gemeißelter Siegelstein");
        block(ModBlocks.SEAL_MONOLITH, "Seal Monolith", "Siegelmonolith");
        block(ModBlocks.SEAL_RUNE, "Seal Rune", "Siegelrune");
        block(ModBlocks.SEAL_INLAY, "Seal Inlay", "Siegeleinlage");
        block(ModBlocks.SEAL_SOCKET, "Seal Socket", "Siegelsockel");
        block(ModBlocks.SEAL_PLATE, "Seal Plate", "Siegelplatte");
        block(ModBlocks.SHADOW_PORTAL, "Shadow Gate", "Schattentor");
        text("block.kingdomsatwar.seal_socket.wrong_shard", "This socket awaits the shard of %s.", "Dieser Sockel wartet auf den Splitter von %s.");
        text("block.kingdomsatwar.seal_socket.empty", "The shard of %s belongs here.", "Hierher gehört der Splitter von %s.");
        text("block.kingdomsatwar.seal_socket.filled", "The shard of %s rests in its monolith.", "Der Splitter von %s ruht in seinem Monolithen.");
        text("block.kingdomsatwar.shadow_portal.sealed", "The gate swirls, but it lets no one through… yet.",
                "Das Tor wogt, doch es lässt noch niemanden hindurch …");

        // Chronik
        item(ModItems.CHRONICLE, "Chronicle of the Seal", "Chronik des Siegels");
        lore("chronicle", "The story of the seal – chapter by chapter", "Die Geschichte des Siegels – Kapitel für Kapitel");
        text("gui.kingdomsatwar.chronicle.title", "Chronicle of the Seal", "Chronik des Siegels");
        text("chapter.kingdomsatwar.label", "Chapter %s", "Kapitel %s");
        text("chapter.kingdomsatwar.label.prolog", "Prologue", "Prolog");
        text("chapter.kingdomsatwar.label.epilog", "Epilogue", "Epilog");
        text("chapter.kingdomsatwar.prolog", "The Seal", "Das Siegel");
        text("chapter.kingdomsatwar.kingdom", "A New Kingdom", "Ein neues Königreich");
        text("chapter.kingdomsatwar.chronicle", "The Chronicle of the Seal", "Die Chronik des Siegels");
        String[][] ordinals = {{"First", "erste"}, {"Second", "zweite"}, {"Third", "dritte"}, {"Fourth", "vierte"}, {"Fifth", "fünfte"},
                {"Sixth", "sechste"}};
        for (int i = 0; i < 6; i++) {
            text("chapter.kingdomsatwar.dragon_" + (i + 1), "The " + ordinals[i][0] + " Dragon", "Der " + ordinals[i][1] + " Drache");
        }
        text("chapter.kingdomsatwar.dragons", "The Six Dragons", "Die sechs Drachen");
        text("chapter.kingdomsatwar.seal", "The Seal Breaks", "Das Siegel bricht");
        text("chapter.kingdomsatwar.crown", "The Crown", "Die Krone");
        text("chapter.kingdomsatwar.prolog.card", "Long ago, six dragons sealed away the darkness.",
                "Vor langer Zeit verschlossen sechs Drachen die Dunkelheit.");
        text("chapter.kingdomsatwar.kingdom.card", "Every kingdom begins with a single step.", "Jedes Königreich beginnt mit einem einzigen Schritt.");
        text("chapter.kingdomsatwar.chronicle.card", "The chronicle tells of a circle of six stones…",
                "Die Chronik erzählt von einem Kreis aus sechs Steinen …");
        text("chapter.kingdomsatwar.dragon_1.card", "Six dragons. Six shards. One seal.", "Sechs Drachen. Sechs Splitter. Ein Siegel.");
        String[][] remaining = {{"Five", "fünf"}, {"Four", "vier"}, {"Three", "drei"}, {"Two", "zwei"}};
        for (int i = 0; i < 4; i++) {
            text("chapter.kingdomsatwar.dragon_" + (i + 2) + ".card", remaining[i][0] + " dragons still guard the seal.",
                    "Noch " + remaining[i][1] + " Drachen bewachen das Siegel.");
        }
        text("chapter.kingdomsatwar.dragon_6.card", "Only one dragon still guards the seal.", "Nur noch ein Drache bewacht das Siegel.");
        text("chapter.kingdomsatwar.seal.card", "The seal is broken. The gate stands open.", "Das Siegel ist gebrochen. Das Tor steht offen.");
        text("chapter.kingdomsatwar.crown.card", "A realm awaits its ruler.", "Ein Reich wartet auf seinen Herrscher.");
        text("chapter.kingdomsatwar.card.fallen", "%s has fallen. A monolith awaits its shard.", "%s ist gefallen. Ein Monolith wartet auf seinen Splitter.");

        text("chronicle.kingdomsatwar.prolog.text",
                "When the world was young, the Shadow World broke forth from the depths. Six elemental dragons – fire, ice, lightning, "
                        + "nature, earth and water – stood against it and locked it away behind a seal. Ever since, each of them has carried "
                        + "a sixth of that seal in its heart.\n\nBut the burden changed them. Over the centuries the guardians themselves "
                        + "grew wild, and today they suffer no one near their lairs.",
                "Als die Welt noch jung war, brach die Schattenwelt aus der Tiefe hervor. Sechs Elementardrachen – Feuer, Eis, Blitz, "
                        + "Natur, Erde und Wasser – stellten sich ihr entgegen und verschlossen sie mit einem Siegel. Seitdem trägt jeder "
                        + "von ihnen ein Sechstel dieses Siegels in seinem Herzen.\n\nDoch die Last hat sie verändert. Über die "
                        + "Jahrhunderte wurden die Hüter selbst wild, und heute dulden sie niemanden mehr in der Nähe ihrer Nester.");
        text("chronicle.kingdomsatwar.kingdom.text",
                "You have arrived in a foreign land with little more than the clothes on your back. Yet this land is waiting for "
                        + "someone to unite it.\n\nBuild a home, gather supplies and find people who will follow you. Every army begins "
                        + "at a recruitment table.",
                "Du bist in einem fremden Land angekommen, mit kaum mehr als dem, was du am Leib trägst. Doch dieses Land wartet auf "
                        + "jemanden, der es eint.\n\nBaue dir ein Zuhause, sammle Vorräte und finde Menschen, die dir folgen. Jedes "
                        + "Heer beginnt an einem Rekrutierungstisch.");
        text("chronicle.kingdomsatwar.chronicle.text",
                "This chronicle was written by the keepers of the seal, generation after generation. It tells of a circle of six "
                        + "monoliths not far from the heart of the world: the Seal Circle. It was there that the seal was once forged."
                        + "\n\nSeek out the circle. The pages themselves will show you the way.",
                "Diese Chronik wurde von den Hütern des Siegels geschrieben, Generation um Generation. Sie erzählt von einem Kreis "
                        + "aus sechs Monolithen, nicht weit vom Herzen der Welt: dem Siegelkreis. Dort wurde das Siegel einst geschmiedet."
                        + "\n\nSuche den Kreis. Die Seiten selbst weisen dir den Weg.");
        text("chronicle.kingdomsatwar.dragons.text",
                "At the Seal Circle, a monolith awaits the shard of each dragon. The dragons slumber in their lairs, each in its own "
                        + "land. None of them can be defeated alone – bring your army. The dragon compass will show you the way.\n\n"
                        + "When a dragon falls, its egg and its seal shard remain. Bring the shard to its monolith.",
                "Im Siegelkreis wartet zu jedem Drachen ein Monolith auf seinen Splitter. Die Drachen schlafen in ihren Nestern, "
                        + "jeder in seinem eigenen Land. Keiner von ihnen lässt sich allein bezwingen – nimm dein Heer mit. Der "
                        + "Drachenkompass weist dir den Weg.\n\nIst ein Drache gefallen, bleiben sein Ei und sein Siegelsplitter zurück. "
                        + "Bring den Splitter zu seinem Monolithen.");
        text("chronicle.kingdomsatwar.seal.text",
                "When the last shard came to rest in its monolith, the seal shattered. What the dragons kept locked away for an age "
                        + "now lies open: at the heart of the circle yawns the gate to the Shadow World.\n\nNo one knows what awaits you "
                        + "on the other side.",
                "Als der letzte Splitter in seinem Monolithen ruhte, zerbrach das Siegel. Was die Drachen ein Zeitalter lang "
                        + "verschlossen hielten, liegt nun offen: Im Herzen des Kreises klafft das Tor in die Schattenwelt.\n\nNiemand "
                        + "weiß, was dich auf der anderen Seite erwartet.");
        text("chronicle.kingdomsatwar.crown.text",
                "The land has seen a hero who defeated dragons and passed through the gate of shadows. Only one thing remains: the "
                        + "crown.\n\nReturn home to your kingdom. Your people are waiting for you.",
                "Das Land hat einen Helden gesehen, der Drachen bezwang und das Tor der Schatten durchschritt. Nun fehlt nur noch "
                        + "eines: die Krone.\n\nKehre heim in dein Königreich. Dein Volk wartet auf dich.");
        text("chronicle.kingdomsatwar.goals", "Goals", "Ziele");
        text("chronicle.kingdomsatwar.goal.table", "Set up a recruitment table", "Stelle einen Rekrutierungstisch auf");
        text("chronicle.kingdomsatwar.goal.recruit", "Recruit your first soldier", "Wirb deinen ersten Soldaten an");
        text("chronicle.kingdomsatwar.goal.read", "Read the chronicle", "Lies die Chronik");
        text("chronicle.kingdomsatwar.goal.find_circle", "Find the Seal Circle", "Finde den Siegelkreis");
        text("chronicle.kingdomsatwar.goal.defeat", "Defeat the six dragons (%s/6)", "Besiege die sechs Drachen (%s/6)");
        text("chronicle.kingdomsatwar.goal.insert", "Place the seal shards in their monoliths (%s/6)",
                "Setze die Siegelsplitter in ihre Monolithen (%s/6)");
        text("chronicle.kingdomsatwar.goal.enter_shadow", "Step through the Shadow Gate", "Schreite durch das Schattentor");
        text("chronicle.kingdomsatwar.goal.crown", "Be crowned in your throne room", "Lass dich in deinem Thronsaal krönen");
        text("chronicle.kingdomsatwar.circle.here", "You are standing in the Seal Circle.", "Du stehst im Siegelkreis.");
        text("chronicle.kingdomsatwar.circle.unknown", "Only in the Overworld can the chronicle show you the way to the Seal Circle.",
                "Nur in der Oberwelt kann dir die Chronik den Weg zum Siegelkreis zeigen.");
        text("chronicle.kingdomsatwar.circle.distance", "The Seal Circle lies about %s blocks to the %s.",
                "Der Siegelkreis liegt etwa %s Blöcke im %s.");
        text("chronicle.kingdomsatwar.gate.here", "The Shadow Gate lies right before you.", "Das Schattentor liegt direkt vor dir.");
        text("chronicle.kingdomsatwar.gate.unknown", "The Shadow Gate lies in the heart of the Seal Circle.",
                "Das Schattentor liegt im Herzen des Siegelkreises.");
        text("chronicle.kingdomsatwar.gate.distance", "The Shadow Gate lies about %s blocks to the %s, in the heart of the Seal Circle.",
                "Das Schattentor liegt etwa %s Blöcke im %s, im Herzen des Siegelkreises.");
        text("chronicle.kingdomsatwar.dragon.inserted", "Defeated – its shard rests in the monolith.", "Besiegt – sein Splitter ruht im Monolithen.");
        text("chronicle.kingdomsatwar.dragon.defeated", "Defeated – its shard belongs in the Seal Circle.",
                "Besiegt – sein Splitter gehört in den Siegelkreis.");
        text("chronicle.kingdomsatwar.dragon.awake", "Has awakened!", "Ist erwacht!");
        text("chronicle.kingdomsatwar.dragon.sleeps", "Sleeps in %s, %s.", "Schläft in %s, %s.");
        text("chronicle.kingdomsatwar.dragon.rumor", "It %s, %s.", "Er %s, %s.");
        text("chronicle.kingdomsatwar.where.exact", "about %s blocks to the %s", "etwa %s Blöcke im %s");
        text("chronicle.kingdomsatwar.where.near", "not far to the %s", "nicht weit im %s");
        text("chronicle.kingdomsatwar.where.some", "a few hundred blocks to the %s", "einige hundert Blöcke im %s");
        text("chronicle.kingdomsatwar.where.far", "far to the %s", "weit im %s");
        text("chronicle.kingdomsatwar.where.here", "very close by", "ganz in der Nähe");
        text("chronicle.kingdomsatwar.where.unknown", "in the Overworld", "in der Oberwelt");
        text("chronicle.kingdomsatwar.where.nowhere", "in a place the chronicle does not yet know", "an einem Ort, den die Chronik noch nicht kennt");
        text("chronicle.kingdomsatwar.seal.count", "%s of 6 shards in place", "%s von 6 Splittern eingesetzt");
        text("chronicle.kingdomsatwar.seal.broken", "The seal is broken", "Das Siegel ist gebrochen");

        // Nachrichten
        text("message.kingdomsatwar.seal.inserted", "The monolith of %s awakens. (%s/6)", "Der Monolith von %s erwacht. (%s/6)");
        text("message.kingdomsatwar.seal.trembles", "The ground trembles… the seal begins to break!", "Der Boden bebt … das Siegel beginnt zu brechen!");
        text("message.kingdomsatwar.seal.broken", "The seal is broken. The gate to the Shadow World stands open.",
                "Das Siegel ist gebrochen. Das Tor in die Schattenwelt steht offen.");
        text("message.kingdomsatwar.story.discovered", "New entry in the chronicle: %s", "Neuer Eintrag in der Chronik: %s");
        text("message.kingdomsatwar.story.found_circle", "You have found the Seal Circle. Six monoliths stand silent around the ancient seal.",
                "Du hast den Siegelkreis gefunden. Sechs Monolithen stehen stumm um das uralte Siegel.");
        text("message.kingdomsatwar.story.dragon_fallen", "%s has fallen! Its seal shard belongs in the Seal Circle.",
                "%s ist gefallen! Sein Siegelsplitter gehört in den Siegelkreis.");

        // Befehle
        text("command.kingdomsatwar.story.reset", "Your story progress has been reset.", "Dein Fortschritt in der Geschichte wurde zurückgesetzt.");
        text("command.kingdomsatwar.seal.status", "Seal Circle at %s – %s of 6 shards – %s", "Siegelkreis bei %s – %s von 6 Splittern – %s");
        text("command.kingdomsatwar.seal.state.intact", "seal intact", "Siegel unversehrt");
        text("command.kingdomsatwar.seal.state.breaking", "the seal is breaking", "das Siegel bricht gerade");
        text("command.kingdomsatwar.seal.state.broken", "seal broken", "Siegel gebrochen");
        text("command.kingdomsatwar.seal.none", "This world has no Seal Circle.", "In dieser Welt gibt es keinen Siegelkreis.");
        text("command.kingdomsatwar.seal.insert_failed", "Not possible – is the Seal Circle loaded?", "Nicht möglich – ist der Siegelkreis geladen?");
        text("command.kingdomsatwar.seal.reset", "The seal is whole again.", "Das Siegel ist wieder ganz.");
    }

    private void lair(DragonType type, String nameEn, String nameDe, String warningEn, String warningDe) {
        text("lair.kingdomsatwar." + type.id() + ".name", nameEn, nameDe);
        text("lair.kingdomsatwar." + type.id() + ".warning", warningEn, warningDe);
    }

    private void creatures() {
        creature(ModEntities.WILD_BOAR, ModItems.WILD_BOAR_SPAWN_EGG, "Wild Boar", "Wildschwein", "Wildschwein");
        creature(ModEntities.DEER, ModItems.DEER_SPAWN_EGG, "Red Deer", "Rothirsch", "Rothirsch");
        creature(ModEntities.GOOSE, ModItems.GOOSE_SPAWN_EGG, "Goose", "Gans", "Gänse");
        creature(ModEntities.WILL_O_WISP, ModItems.WILL_O_WISP_SPAWN_EGG, "Will-o'-the-Wisp", "Irrlicht", "Irrlicht");
        creature(ModEntities.CAVE_TROLL, ModItems.CAVE_TROLL_SPAWN_EGG, "Cave Troll", "Höhlentroll", "Höhlentroll");
        creature(ModEntities.LINDWORM, ModItems.LINDWORM_SPAWN_EGG, "Lindworm", "Lindwurm", "Lindwurm");

        item(ModItems.VENISON, "Raw Venison", "Rohes Wildbret");
        item(ModItems.COOKED_VENISON, "Cooked Venison", "Wildbraten");
        item(ModItems.WISP_DUST, "Wisp Dust", "Irrlichtstaub");
        lore("wisp_dust", "Glowing dust – an ingredient for magic", "Leuchtender Staub – eine Zutat für Magie");
        item(ModItems.TROLL_CLUB, "Troll Club", "Trollkeule");
        lore("troll_club", "Heavy: hurls enemies away and shatters shields", "Schwer: schleudert Gegner weg und zerschmettert Schilde");

        subtitle("entity.wild_boar.ambient", "Wild boar grunts", "Wildschwein grunzt");
        subtitle("entity.wild_boar.angry", "Wild boar snorts angrily", "Wildschwein schnaubt wütend");
        subtitle("entity.wild_boar.hurt", "Wild boar squeals", "Wildschwein quiekt");
        subtitle("entity.wild_boar.death", "Wild boar dies", "Wildschwein stirbt");
        subtitle("entity.wild_boar.step", "Footsteps", "Schritte");
        subtitle("entity.deer.ambient", "Deer calls", "Hirsch ruft");
        subtitle("entity.deer.hurt", "Deer hurts", "Hirsch wird verletzt");
        subtitle("entity.deer.death", "Deer dies", "Hirsch stirbt");
        subtitle("entity.deer.step", "Hooves patter", "Hufe trappeln");
        subtitle("entity.goose.ambient", "Goose honks", "Gans schnattert");
        subtitle("entity.goose.hiss", "Goose hisses", "Gans zischt");
        subtitle("entity.goose.hurt", "Goose hurts", "Gans wird verletzt");
        subtitle("entity.goose.death", "Goose dies", "Gans stirbt");
        subtitle("entity.will_o_wisp.ambient", "Will-o'-the-wisp chimes", "Irrlicht klingt");
        subtitle("entity.will_o_wisp.hurt", "Will-o'-the-wisp flickers", "Irrlicht flackert");
        subtitle("entity.will_o_wisp.death", "Will-o'-the-wisp goes out", "Irrlicht erlischt");
        subtitle("entity.will_o_wisp.vanish", "Will-o'-the-wisp fades", "Irrlicht verblasst");
        subtitle("entity.cave_troll.ambient", "Cave troll growls", "Höhlentroll grollt");
        subtitle("entity.cave_troll.hurt", "Cave troll hurts", "Höhlentroll wird verletzt");
        subtitle("entity.cave_troll.death", "Cave troll dies", "Höhlentroll stirbt");
        subtitle("entity.cave_troll.step", "Heavy footsteps", "Schwere Schritte");
        subtitle("entity.cave_troll.smash", "Club smashes", "Keulenhieb");
        subtitle("entity.cave_troll.petrify", "Cave troll turns to stone", "Höhlentroll versteinert");
        subtitle("entity.lindworm.ambient", "Lindworm hisses", "Lindwurm zischt");
        subtitle("entity.lindworm.hurt", "Lindworm hurts", "Lindwurm wird verletzt");
        subtitle("entity.lindworm.death", "Lindworm dies", "Lindwurm stirbt");
        subtitle("entity.lindworm.bite", "Lindworm bites", "Lindwurm beißt");
        subtitle("entity.lindworm.slither", "Lindworm slithers", "Lindwurm kriecht");
    }

    /** Kreatur mit Spawn-Ei ({@code eggStemDe}: Wortstamm für "...-Spawn-Ei"). */
    private void creature(Supplier<? extends net.minecraft.world.entity.EntityType<?>> type, Supplier<? extends Item> egg,
            String english, String deutsch, String eggStemDe) {
        add(type.get(), pick(english, deutsch));
        item(egg, english + " Spawn Egg", eggStemDe + "-Spawn-Ei");
    }

    private void equipment() {
        // Stahl
        item(ModItems.STEEL_SWORD, "Steel Sword", "Stahlschwert");
        item(ModItems.STEEL_PICKAXE, "Steel Pickaxe", "Stahlspitzhacke");
        item(ModItems.STEEL_AXE, "Steel Axe", "Stahlaxt");
        item(ModItems.STEEL_SHOVEL, "Steel Shovel", "Stahlschaufel");
        item(ModItems.STEEL_HOE, "Steel Hoe", "Stahlhacke");
        item(ModItems.STEEL_HELMET, "Steel Helmet", "Stahlhelm");
        item(ModItems.STEEL_CHESTPLATE, "Steel Chestplate", "Stahlharnisch");
        item(ModItems.STEEL_LEGGINGS, "Steel Leggings", "Stahlbeinschutz");
        item(ModItems.STEEL_BOOTS, "Steel Boots", "Stahlstiefel");
        // Konterit
        item(ModItems.KONTERIT_HELMET, "Konterit Helmet", "Konterithelm");
        item(ModItems.KONTERIT_CHESTPLATE, "Konterit Chestplate", "Konteritharnisch");
        item(ModItems.KONTERIT_LEGGINGS, "Konterit Leggings", "Konteritbeinschutz");
        item(ModItems.KONTERIT_BOOTS, "Konterit Boots", "Konteritstiefel");
        lore("konterit_armor", "Light: +4% speed and softer falls per piece", "Leicht: je Teil +4 % Tempo und sanftere Stürze");
        // Draconium
        item(ModItems.DRACONIUM_SWORD, "Draconium Sword", "Draconiumschwert");
        item(ModItems.DRACONIUM_AXE, "Draconium Axe", "Draconiumaxt");
        lore("draconium_weapon", "Sets every enemy it hits on fire", "Setzt jeden getroffenen Gegner in Brand");
        // Drachenschuppen
        scaleArmor(DragonType.FIRE, "Fire", "Feuer", "Full set: fire resistance", "Voller Satz: Feuerresistenz");
        scaleArmor(DragonType.ICE, "Ice", "Eis", "Full set: speed · every piece stops freezing",
                "Voller Satz: Schnelligkeit · jedes Teil schützt vor Frost");
        scaleArmor(DragonType.LIGHTNING, "Lightning", "Blitz", "Full set: great speed", "Voller Satz: große Schnelligkeit");
        scaleArmor(DragonType.NATURE, "Nature", "Natur", "Full set: regeneration", "Voller Satz: Regeneration");
        scaleArmor(DragonType.EARTH, "Earth", "Erd", "Full set: resistance", "Voller Satz: Resistenz");
        scaleArmor(DragonType.WATER, "Water", "Wasser", "Full set: water breathing and dolphin's grace",
                "Voller Satz: Unterwasseratmung und Gunst des Delfins");
        // Wyvernit, Edelstein
        item(ModItems.WYVERNIT_GLIDER, "Wyvernit Glider", "Wyvernit-Gleiter");
        text(ModItems.loreKey("wyvernit_glider", 1), "Glide through the air like with elytra", "Gleite durch die Luft wie mit Elytren");
        text(ModItems.loreKey("wyvernit_glider", 2), "Repair with Wyvernit", "Mit Wyvernit reparieren");
        item(ModItems.GEMSTONE_CROWN, "Royal Crown", "Königskrone");
        lore("gemstone_crown", "Luck +2 – fit for kings and queens", "Glück +2 – für Könige und Königinnen");
        // Drachenpanzer
        item(ModItems.STEEL_DRAGON_ARMOR, "Steel Dragon Armour", "Stahl-Drachenpanzer");
        text(ModItems.loreKey("steel_dragon_armor", 1), "Armour for your fully grown dragon", "Rüstung für deinen ausgewachsenen Drachen");
        text(ModItems.loreKey("steel_dragon_armor", 2), "Use on your dragon · remove with shears",
                "Am Drachen benutzen · mit der Schere abnehmen");
        item(ModItems.DRACONIUM_DRAGON_ARMOR, "Draconium Dragon Armour", "Draconium-Drachenpanzer");
        text(ModItems.loreKey("draconium_dragon_armor", 1), "Glowing, fireproof armour for your dragon",
                "Glühende, feuerfeste Rüstung für deinen Drachen");
        text(ModItems.loreKey("draconium_dragon_armor", 2), "Use on your dragon · remove with shears",
                "Am Drachen benutzen · mit der Schere abnehmen");
        text("message.kingdomsatwar.dragon_armor_too_young", "%s must be tamed and fully grown to wear armour",
                "%s muss gezähmt und ausgewachsen sein, um einen Panzer zu tragen");
    }

    /** Rüstungssatz aus Drachenschuppen: vier Teile und die Wirkung des vollen Satzes. */
    private void scaleArmor(DragonType type, String english, String deutsch, String bonusEn, String bonusDe) {
        List<? extends Supplier<? extends Item>> set = ModItems.DRAGON_SCALE_ARMOR.get(type);
        item(set.get(0), english + " Scale Helmet", deutsch + "schuppen-Helm");
        item(set.get(1), english + " Scale Chestplate", deutsch + "schuppen-Harnisch");
        item(set.get(2), english + " Scale Leggings", deutsch + "schuppen-Beinschutz");
        item(set.get(3), english + " Scale Boots", deutsch + "schuppen-Stiefel");
        lore("dragon_scale_armor_" + type.id(), bonusEn, bonusDe);
    }

    /** Magie: Aetherit-Stab und Runen. */
    private void magic() {
        item(ModItems.AETHERIT_STAFF, "Aetherit Staff", "Aetherit-Stab");
        text(ModItems.loreKey("aetherit_staff", 1), "Right-click: Aether bolt", "Rechtsklick: Aether-Blitz");
        text(ModItems.loreKey("aetherit_staff", 2), "Hold a rune in your other hand to cast its spell",
                "Eine Rune in der anderen Hand wirkt ihren Zauber");
        rune(DragonType.FIRE, "Fire Rune", "Feuerrune", "Hurls a fireball", "Schleudert einen Feuerball");
        rune(DragonType.ICE, "Ice Rune", "Eisrune", "Breath of frost: slows and freezes water", "Frosthauch: verlangsamt und friert Wasser");
        rune(DragonType.LIGHTNING, "Lightning Rune", "Blitzrune", "Calls down lightning", "Ruft einen Blitz herab");
        rune(DragonType.NATURE, "Nature Rune", "Naturrune", "Heals you and your allies, plants sprout", "Heilt dich und Verbündete, Pflanzen sprießen");
        rune(DragonType.EARTH, "Earth Rune", "Erdrune", "Earthquake: hurls enemies away", "Erdstoß: schleudert Feinde fort");
        rune(DragonType.WATER, "Water Rune", "Wasserrune", "Tidal wave: pushes back, puts out fires", "Woge: drängt zurück, löscht Feuer");
    }

    private void rune(DragonType type, String english, String deutsch, String loreEn, String loreDe) {
        item(ModItems.RUNES.get(type), english, deutsch);
        text(ModItems.loreKey(type.id() + "_rune", 1), loreEn, loreDe);
    }

    /** Einrichtung und Burgbau: Putz, Fenster, Türen, Läden, Licht, Zinnen, Möbel. */
    private void decor() {
        roof("white_plaster", "white_plaster", "White Plaster", "Kalkputz", "White Plaster", "Kalkputz");
        roof("clay_plaster", "clay_plaster", "Clay Plaster", "Lehmputz", "Clay Plaster", "Lehmputz");
        block(ModBlocks.LEADED_GLASS, "Leaded Glass", "Bleiglas");
        block(ModBlocks.LEADED_GLASS_PANE, "Leaded Glass Pane", "Bleiglasscheibe");
        block(ModBlocks.CASTLE_DOOR, "Castle Door", "Burgtür");
        block(ModBlocks.GREEN_SHUTTERS, "Green Shutters", "Grüne Fensterläden");
        block(ModBlocks.RED_SHUTTERS, "Red Shutters", "Rote Fensterläden");
        block(ModBlocks.STEEL_LANTERN, "Steel Lantern", "Stahllaterne");
        block(ModBlocks.AETHERIT_LANTERN, "Aetherit Lantern", "Aetheritlaterne");
        block(ModBlocks.CHANDELIER, "Chandelier", "Kronleuchter");
        block(ModBlocks.MEDIEVAL_STONE_BATTLEMENT, "Medieval Stone Battlement", "Mittelalterliche Steinzinne");
        block(ModBlocks.DARK_STONE_BRICK_BATTLEMENT, "Dark Stone Brick Battlement", "Dunkle Steinziegelzinne");
        block(ModBlocks.LIMESTONE_BRICK_BATTLEMENT, "Light Limestone Brick Battlement", "Helle Kalksteinziegelzinne");
        block(ModBlocks.OAK_CHAIR, "Oak Chair", "Eichenstuhl");
        block(ModBlocks.OAK_BENCH, "Oak Bench", "Eichenbank");
        block(ModBlocks.OAK_TABLE, "Oak Table", "Eichentisch");
        block(ModBlocks.THRONE, "Throne", "Thron");
        lore("seat", "Right-click to sit down", "Rechtsklick zum Hinsetzen");
        lore("throne", "Fit for a ruler – right-click to sit down", "Eines Herrschers würdig – Rechtsklick zum Hinsetzen");
        text("block.kingdomsatwar.seat.occupied", "Someone is already sitting here.", "Hier sitzt schon jemand.");
        text("block.kingdomsatwar.seat.obstructed", "There is no room to sit here.", "Hier ist kein Platz zum Sitzen.");
        add(ModEntities.SEAT.get(), pick("Seat", "Sitz"));
    }

    private void army() {
        soldier(SoldierType.SWORDSMAN, "Swordsman", "Schwertkämpfer", "Schwertkämpfer", "Melee fighter", "Nahkämpfer",
                "Chain mail, sword and the kingdom's tabard. Reliable and cheap – the backbone of any army.",
                "Kettenhemd, Schwert und Wappenrock. Zuverlässig und günstig – das Rückgrat jeder Armee.");
        soldier(SoldierType.ARCHER, "Archer", "Bogenschütze", "Bogenschützen", "Ranged fighter", "Fernkämpfer",
                "Shoots from afar and never at friends. Lightly armoured – best kept behind the swordsmen.",
                "Schießt aus der Ferne und nie auf Freunde. Leicht gerüstet – am besten hinter den Schwertkämpfern.");
        soldier(SoldierType.KNIGHT, "Knight", "Ritter", "Ritter", "Armoured", "Gepanzert",
                "Full plate armour and the kingdom's shield. Blocks arrows and holds the front line.",
                "Plattenrüstung und Schild des Königreichs. Wehrt Pfeile ab und hält die vorderste Reihe.");
        soldier(SoldierType.CAPTAIN, "Captain", "Hauptmann", "Hauptmann", "Leader", "Anführer",
                "Makes all soldiers nearby stronger and tougher and leads them into battle.",
                "Macht alle Soldaten in seiner Nähe stärker und zäher und führt sie in die Schlacht.");
        text("soldier.kingdomsatwar.name", "%s %s", "%s %s");
        add(ModEntities.BANDIT.get(), pick("Bandit", "Bandit"));
        add(ModEntities.BANDIT_ARCHER.get(), pick("Bandit Archer", "Banditen-Bogenschütze"));
        item(ModItems.BANDIT_SPAWN_EGG, "Bandit Spawn Egg", "Banditen-Spawn-Ei");
        item(ModItems.BANDIT_ARCHER_SPAWN_EGG, "Bandit Archer Spawn Egg", "Banditen-Bogenschützen-Spawn-Ei");

        block(ModBlocks.RECRUITMENT_TABLE, "Recruitment Table", "Rekrutierungstisch");
        text(ModItems.loreKey("recruitment_table", 1), "Recruit soldiers for emeralds", "Hier wirbst du Soldaten gegen Smaragde an");
        text(ModItems.loreKey("recruitment_table", 2), "Knights also need steel, captains a gemstone",
                "Ritter brauchen zusätzlich Stahl, Hauptleute einen Edelstein");
        item(ModItems.COMMAND_BATON, "Command Baton", "Kommandostab");
        text(ModItems.loreKey("command_baton", 1), "Point at an enemy: attack", "Auf einen Feind zeigen: Angriff");
        text(ModItems.loreKey("command_baton", 2), "Point at the ground: hold position (sneak: patrol)",
                "Auf den Boden zeigen: Stellung halten (Schleichen: Patrouille)");
        text(ModItems.loreKey("command_baton", 3), "Point at the sky: everyone follows", "In den Himmel zeigen: alle folgen");
        text(ModItems.loreKey("command_baton", 4), "Sneak + click your soldier: dismiss", "Schleichen + eigenen Soldaten anklicken: entlassen");

        // Fenster des Rekrutierungstischs
        text("gui.kingdomsatwar.recruitment.title", "Recruitment Table", "Rekrutierungstisch");
        text("gui.kingdomsatwar.recruitment.army", "Army: %s / %s", "Armee: %s / %s");
        text("gui.kingdomsatwar.recruitment.recruit", "Recruit", "Anwerben");
        text("gui.kingdomsatwar.recruitment.health", "%s health", "%s Leben");
        text("gui.kingdomsatwar.recruitment.armor", "%s armour", "%s Rüstung");
        text("gui.kingdomsatwar.recruitment.melee", "Sword", "Schwert");
        text("gui.kingdomsatwar.recruitment.ranged", "Bow", "Bogen");
        text("gui.kingdomsatwar.recruitment.cost", "Price:", "Preis:");
        text("gui.kingdomsatwar.recruitment.have", "You have %s", "Du hast %s");
        text("gui.kingdomsatwar.recruitment.missing", "Missing: %s× %s", "Es fehlen: %s× %s");
        text("gui.kingdomsatwar.recruitment.result.recruited", "Recruited!", "Angeworben!");
        text("gui.kingdomsatwar.recruitment.result.too_far", "Too far away", "Zu weit weg");
        text("gui.kingdomsatwar.recruitment.result.not_enough", "Too expensive", "Zu teuer");
        text("gui.kingdomsatwar.recruitment.result.army_full", "Army full", "Armee voll");
        text("gui.kingdomsatwar.recruitment.result.no_space", "No room", "Kein Platz");
        text("message.kingdomsatwar.recruit.recruited", "Recruited! Your new soldier follows you.", "Angeworben! Dein neuer Soldat folgt dir.");
        text("message.kingdomsatwar.recruit.too_far", "You are too far away from the table.", "Du bist zu weit vom Tisch entfernt.");
        text("message.kingdomsatwar.recruit.not_enough", "You can't afford this soldier yet.", "Diesen Soldaten kannst du dir noch nicht leisten.");
        text("message.kingdomsatwar.recruit.army_full", "Your army is complete – you can't lead more soldiers.",
                "Deine Armee ist vollzählig – mehr Soldaten kannst du nicht führen.");
        text("message.kingdomsatwar.recruit.no_space", "There is no room next to the table.", "Neben dem Tisch ist kein Platz.");

        // Befehle und Zustand
        text("message.kingdomsatwar.soldier_order.0", "%s follows you", "%s folgt dir");
        text("message.kingdomsatwar.soldier_order.1", "%s holds this position", "%s hält diese Stellung");
        text("message.kingdomsatwar.soldier_order.2", "%s patrols here", "%s patrouilliert hier");
        text("message.kingdomsatwar.soldier_mode.0", "following", "folgt");
        text("message.kingdomsatwar.soldier_mode.1", "holding position", "hält Stellung");
        text("message.kingdomsatwar.soldier_mode.2", "on patrol", "auf Patrouille");
        text("message.kingdomsatwar.soldier_status", "%s – %s/%s health – %s", "%s – %s/%s Leben – %s");
        text("message.kingdomsatwar.soldier_price", "%s serves for %s emeralds", "%s dient für %s Smaragde");
        text("message.kingdomsatwar.soldier_equipped", "%s now fights with: %s", "%s kämpft jetzt mit: %s");
        text("message.kingdomsatwar.command.none", "No soldiers of yours nearby", "Keine eigenen Soldaten in der Nähe");
        text("message.kingdomsatwar.command.attack", "%s soldiers attack %s!", "%s Soldaten greifen %s an!");
        text("message.kingdomsatwar.command.hold", "%s soldiers hold this position", "%s Soldaten halten diese Stellung");
        text("message.kingdomsatwar.command.patrol", "%s soldiers patrol here", "%s Soldaten patrouillieren hier");
        text("message.kingdomsatwar.command.follow", "%s soldiers follow you", "%s Soldaten folgen dir");
        text("message.kingdomsatwar.command.dismissed", "%s has left your service", "%s wurde aus dem Dienst entlassen");
        text("message.kingdomsatwar.army_summoned", "%s × %s joined your army", "%s × %s sind deiner Armee beigetreten");

        // Geräusche
        subtitle("entity.soldier.hurt", "Soldier hurts", "Soldat nimmt Schaden");
        subtitle("entity.soldier.death", "Soldier falls", "Soldat fällt");
        subtitle("entity.soldier.armor", "Armour clinks", "Rüstung klirrt");
        subtitle("entity.bandit.ambient", "Bandit mutters", "Bandit murmelt");
        subtitle("entity.bandit.hurt", "Bandit hurts", "Bandit nimmt Schaden");
        subtitle("entity.bandit.death", "Bandit dies", "Bandit stirbt");
        subtitle("item.command_baton.command", "Order given", "Befehl erteilt");

        // Einstellungen der Welt
        text("kingdomsatwar.configuration.section.kingdomsatwar.synced.toml", "World settings", "Welt-Einstellungen");
        text("kingdomsatwar.configuration.section.kingdomsatwar.synced.toml.title", "Kingdoms at War – World settings",
                "Kingdoms at War – Welt-Einstellungen");
        text("kingdomsatwar.configuration.armyLimit", "Army size limit", "Größte Armee");
        text("kingdomsatwar.configuration.armyLimit.tooltip", "How many soldiers each player can recruit at most.",
                "Wie viele Soldaten jeder Spieler höchstens anwerben kann.");
    }

    /** Soldat: Name, Spawn-Ei, Rolle und Beschreibung im Anwerbe-Fenster. */
    private void soldier(SoldierType type, String english, String deutsch, String germanStem, String roleEn, String roleDe,
            String descriptionEn, String descriptionDe) {
        add(ModEntities.SOLDIERS.get(type).get(), pick(english, deutsch));
        item(ModItems.SOLDIER_SPAWN_EGGS.get(type), english + " Spawn Egg", germanStem + "-Spawn-Ei");
        text("soldier.kingdomsatwar." + type.id() + ".role", roleEn, roleDe);
        text(type.descriptionKey(), descriptionEn, descriptionDe);
    }

    private String pick(String english, String deutsch) {
        return german ? deutsch : english;
    }

    private void text(String key, String english, String deutsch) {
        add(key, pick(english, deutsch));
    }

    private void block(Supplier<? extends Block> block, String english, String deutsch) {
        addBlock(block, pick(english, deutsch));
    }

    private void item(Supplier<? extends Item> item, String english, String deutsch) {
        addItem(item, pick(english, deutsch));
    }

    private void lore(String name, String english, String deutsch) {
        text(ModItems.loreKey(name, 1), english, deutsch);
    }

    private void blockKey(String id, String english, String deutsch) {
        text("block." + KingdomsAtWar.MODID + "." + id, english, deutsch);
    }

    /**
     * Stein-Set: Vollblock, Treppe, Stufe und Mauer. Für die Varianten wird der Wortstamm
     * ({@code stemEn}/{@code stemDe}) mit "Treppe", "Stufe" bzw. "Mauer" kombiniert.
     */
    private void set(String base, String variant, String nameEn, String nameDe, String stemEn, String stemDe) {
        blockKey(base, nameEn, nameDe);
        blockKey(variant + "_stairs", stemEn + " Stairs", germanVariant(stemDe, "treppe"));
        blockKey(variant + "_slab", stemEn + " Slab", germanVariant(stemDe, "stufe"));
        blockKey(variant + "_wall", stemEn + " Wall", germanVariant(stemDe, "mauer"));
    }

    private void roof(String base, String variant, String nameEn, String nameDe, String stemEn, String stemDe) {
        blockKey(base, nameEn, nameDe);
        blockKey(variant + "_stairs", stemEn + " Stairs", germanVariant(stemDe, "treppe"));
        blockKey(variant + "_slab", stemEn + " Slab", germanVariant(stemDe, "stufe"));
    }

    /** "Moosige Mauerstein" + "treppe" -> "Moosige Mauersteintreppe". */
    private static String germanVariant(String stem, String suffix) {
        return stem + suffix;
    }

    private void hatchHint(DragonType type, String english, String deutsch) {
        text(ModItems.loreKey(type.id() + "_dragon_egg", 3), english, deutsch);
    }

    /** Drache: Name des Lebewesens und des Spawn-Eis ("Feuerdrachen-Spawn-Ei"). */
    private void dragon(DragonType type, String english, String deutsch, String germanStem) {
        add(ModEntities.DRAGONS.get(type).get(), pick(english, deutsch));
        item(ModItems.DRAGON_SPAWN_EGGS.get(type), english + " Spawn Egg", germanStem + "-Spawn-Ei");
    }

    private void subtitle(String sound, String english, String deutsch) {
        text("subtitles." + KingdomsAtWar.MODID + "." + sound, english, deutsch);
    }

    private void egg(DragonType type, String nameEn, String nameDe, String line1En, String line1De,
            String line2En, String line2De) {
        String id = type.id() + "_dragon_egg";
        block(ModBlocks.DRAGON_EGGS.get(type), nameEn, nameDe);
        text(ModItems.loreKey(id, 1), line1En, line1De);
        text(ModItems.loreKey(id, 2), line2En, line2De);
    }
}
