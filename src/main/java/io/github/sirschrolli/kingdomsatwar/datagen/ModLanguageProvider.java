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
        EconomyLang.register(this::text);
        VillageLang.register(this::text);
        WarLang.register(this::text);
        ArchitectLang.register(this::text);
        SiegeLang.register(this::text);
        HeroLang.register(this::text);
        FishingLang.register(this::text);
        ArmoryLang.register(this::text);
        BeastLang.register(this::text);
        RelicLang.register(this::text);
        SkyLang.register(this::text);
        VeterancyLang.register(this::text);
        RulesLang.register(this::text);
        PlaceLang.register(this::text);
        bosses();
        shadowWorld();
        voidWorld();
        // Untertitel der Umgebungsklänge
        io.github.sirschrolli.kingdomsatwar.registry.AmbientSounds.SUBTITLES
                .forEach((sound, names) -> this.subtitle("ambient." + sound, names.get(0), names.get(1)));
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
        set("cobbled_rime_stone", "cobbled_rime_stone", "Cobbled Rime Stone", "Raureifbruchstein", "Cobbled Rime Stone", "Raureifbruchstein");
        block(ModBlocks.FROZEN_SOIL, "Frozen Soil", "Frosterde");
        block(ModBlocks.ICICLE, "Icicle", "Eiszapfen");

        // --- Uralter Hain ---
        text("biome.kingdomsatwar.ancient_grove", "Ancient Grove", "Uralter Hain");
        block(ModBlocks.ANCIENT_LOG, "Ancient Log", "Urholzstamm");
        roof("ancient_planks", "ancient_plank", "Ancient Planks", "Urholzbretter", "Ancient Plank", "Urholz");
        roof("charred_planks", "charred_plank", "Charred Planks", "Verkohlte Bretter", "Charred Plank", "Verkohlte Holz");
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
        set("cobbled_shale", "cobbled_shale", "Cobbled Shale", "Schieferbruchstein", "Cobbled Shale", "Schieferbruchstein");
        block(ModBlocks.RUBBLE, "Rubble", "Geröll");
        block(ModBlocks.RIFT_STONE, "Rift Stone", "Spaltgestein");

        // --- Sturmklippen ---
        text("biome.kingdomsatwar.storm_cliffs", "Storm Cliffs", "Sturmklippen");
        set("stormstone", "stormstone", "Stormstone", "Sturmstein", "Stormstone", "Sturmstein");
        set("cobbled_stormstone", "cobbled_stormstone", "Cobbled Stormstone", "Sturmbruchstein", "Cobbled Stormstone", "Sturmbruchstein");
        block(ModBlocks.CHARGED_STORMSTONE, "Charged Stormstone", "Geladener Sturmstein");
        block(ModBlocks.FULGURITE, "Fulgurite", "Fulgurit");
        block(ModBlocks.STORM_CRYSTAL, "Storm Crystal", "Sturmkristall");

        // --- Abgrundgrotte ---
        text("biome.kingdomsatwar.abyssal_grotto", "Abyssal Grotto", "Abgrundgrotte");
        set("abyssal_stone", "abyssal_stone", "Abyssal Stone", "Abgrundstein", "Abyssal Stone", "Abgrundstein");
        set("cobbled_abyssal_stone", "cobbled_abyssal_stone", "Cobbled Abyssal Stone", "Abgrundbruchstein", "Cobbled Abyssal Stone",
                "Abgrundbruchstein");
        block(ModBlocks.ABYSSAL_SAND, "Abyssal Sand", "Abgrundsand");
        block(ModBlocks.GLOW_CORAL, "Glow Coral", "Leuchtkoralle");
        block(ModBlocks.NACRE_BLOCK, "Nacre Block", "Perlmuttblock");

        // --- Eigene Oberwelt-Biome ---
        text("biome.kingdomsatwar.ash_fields", "Ash Fields", "Aschefelder");
        text("biome.kingdomsatwar.frost_peaks", "Frost Peaks", "Frostgipfel");
        text("biome.kingdomsatwar.storm_coast", "Storm Coast", "Sturmküste");
        text("biome.kingdomsatwar.primeval_forest", "Primeval Forest", "Urhain");
        text("biome.kingdomsatwar.rock_gorges", "Rock Gorges", "Felsschluchten");
        text("biome.kingdomsatwar.coral_coast", "Coral Coast", "Korallenküste");
        text("biome.kingdomsatwar.royal_meadows", "Royal Meadows", "Königsauen");
        text("biome.kingdomsatwar.mist_moor", "Mist Moor", "Nebelmoor");
        block(ModBlocks.PEAT, "Peat", "Torf");
        block(ModBlocks.GOLD_LEAF_BLOCK, "Gold Leaf Block", "Blattgold");
        block(ModBlocks.REEDS, "Reeds", "Schilf");

        // --- Aschelande ---
        text("biome.kingdomsatwar.ashlands", "Ashlands", "Aschelande");
        block(ModBlocks.ASH_BLOCK, "Ash", "Asche");
        block(ModBlocks.ASH_LAYER, "Ash Layer", "Ascheschicht");
        block(ModBlocks.CINDER_SOIL, "Cinder Soil", "Aschenerde");
        set("volcanic_rock", "volcanic_rock", "Volcanic Rock", "Vulkangestein", "Volcanic Rock", "Vulkangestein");
        set("cobbled_volcanic_rock", "cobbled_volcanic_rock", "Cobbled Volcanic Rock", "Vulkanbruchstein", "Cobbled Volcanic Rock",
                "Vulkanbruchstein");
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
        item(ModItems.REINFORCED_HANDLE, "Reinforced Handle", "Beschlagener Griff");
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
        subtitle("story.narration", "Narrator speaks", "Erzähler spricht");
        subtitle("voice.soldier.recruit", "Soldier reports for duty", "Soldat meldet sich zum Dienst");
        subtitle("voice.soldier.follow", "Soldier follows", "Soldat folgt");
        subtitle("voice.soldier.hold", "Soldier holds position", "Soldat hält die Stellung");
        subtitle("voice.soldier.attack", "Soldier battle cry", "Soldat ruft zum Angriff");
        subtitle("voice.soldier.victory", "Soldier cheers", "Soldat jubelt");
        subtitle("voice.soldier.idle", "Soldier mutters", "Soldat murmelt");

        // --- Erzähl-Intro „Das Siegel“ (Texte wie in tools/audio/lines.py) ---
        text("intro.kingdomsatwar.line1", "There was a time when a darkness rose from the depths of this world and swallowed every light.",
                "Es gab eine Zeit, da stieg aus der Tiefe dieser Welt eine Dunkelheit empor, die alles Licht verschlang.");
        text("intro.kingdomsatwar.line2", "Forests withered, rivers turned black, and the people fled before the shadows.",
                "Wälder verdorrten, Flüsse wurden schwarz, und die Menschen flohen vor den Schatten.");
        text("intro.kingdomsatwar.line3", "Then six dragons rose up – born of fire, ice, lightning, nature, earth and water.",
                "Da erhoben sich sechs Drachen – geboren aus Feuer, Eis, Blitz, Natur, Erde und Wasser.");
        text("intro.kingdomsatwar.line4", "Together they forged a Seal and banished the darkness deep beneath the earth.",
                "Gemeinsam schmiedeten sie ein Siegel und verbannten die Dunkelheit tief unter die Erde.");
        text("intro.kingdomsatwar.line5", "Ever since, each of them has carried a shard of that Seal in its heart.",
                "Jeder von ihnen trägt seitdem einen Splitter dieses Siegels in seinem Herzen.");
        text("intro.kingdomsatwar.line6", "But the centuries passed, and the burden of the Seal drove them wild.",
                "Doch die Jahrhunderte vergingen, und die Last des Siegels machte sie wild.");
        text("intro.kingdomsatwar.line7", "They guard their nests – in ash and ice, on stormy peaks, in ancient groves, deep within the rock and at the bottom of the sea – and suffer no one to come near.",
                "Sie hüten ihre Nester – in Asche und Eis, auf Sturmgipfeln, in uralten Hainen, tief im Fels und am Grund des Meeres – und dulden niemanden in ihrer Nähe.");
        text("intro.kingdomsatwar.line8", "Now your time has come. Found a kingdom, gather knights around you and raise an army.",
                "Nun ist deine Zeit gekommen. Gründe ein Königreich, sammle Ritter um dich und stelle ein Heer auf.");
        text("intro.kingdomsatwar.line9", "Defeat the six dragons and bring their shards back to the ring of stones.",
                "Bezwinge die sechs Drachen und bring ihre Splitter zurück in den Kreis der Steine.");
        text("intro.kingdomsatwar.line10", "But beware: what the Seal holds back is only waiting for it to break.",
                "Doch hüte dich: Was das Siegel verschließt, wartet nur darauf, dass es bricht.");
        text("intro.kingdomsatwar.line11", "This is your kingdom.",
                "Dies ist dein Königreich.");
        // --- Der Erzähler zu den Kapiteln (Texte wie in tools/audio/lines.py) ---
        text("story.kingdomsatwar.narration.kingdom", "Every kingdom begins with a single step. Find a piece of land, plant your banner there – and win the first who will entrust you with his sword.",
                "Jedes Königreich beginnt mit einem einzigen Schritt. Such dir ein Stück Land, pflanze dort dein Banner – und gewinne den Ersten, der dir sein Schwert anvertraut.");
        text("story.kingdomsatwar.narration.chronicle", "Now the first of the faithful follow you. Open the Chronicle: it tells of a ring of six stones where the Seal rests. That is where your true path begins.",
                "Nun folgen dir die ersten Getreuen. Schlag die Chronik auf: Sie erzählt von einem Kreis aus sechs Steinen, in dem das Siegel ruht. Dort beginnt dein eigentlicher Weg.");
        text("story.kingdomsatwar.narration.dragon_1", "Six stones, six empty niches. Each dragon carries its shard in its heart – and not one of them will give it up willingly. The dragon compass will show you the way.",
                "Sechs Steine, sechs leere Nischen. Jeder Drache trägt seinen Splitter im Herzen – und keiner von ihnen wird ihn freiwillig hergeben. Der Drachenkompass weist dir den Weg.");
        text("story.kingdomsatwar.narration.fallen_fire", "Pyrrhon, the Ash Lord, has fallen. His embers have died, but his shard still burns.",
                "Pyrrhon, der Aschenfürst, ist gefallen. Seine Glut ist erloschen, doch sein Splitter brennt noch.");
        text("story.kingdomsatwar.narration.fallen_ice", "Glaciara, the Frost Queen, has fallen. The eternal ice has fallen silent.",
                "Glaciara, die Frostkönigin, ist gefallen. Das ewige Eis ist verstummt.");
        text("story.kingdomsatwar.narration.fallen_lightning", "Voltarion, the Stormbringer, has fallen. Above the peaks, the storm is passing.",
                "Voltarion, der Sturmbringer, ist gefallen. Über den Gipfeln verzieht sich das Gewitter.");
        text("story.kingdomsatwar.narration.fallen_nature", "Verdantis, Guardian of the Grove, has fallen. The ancient forest has grown still.",
                "Verdantis, die Hüterin des Hains, ist gefallen. Im uralten Wald ist es still geworden.");
        text("story.kingdomsatwar.narration.fallen_earth", "Terragon, the Mountainbreaker, has fallen. The earth has stopped trembling.",
                "Terragon, der Bergbrecher, ist gefallen. Die Erde hat aufgehört zu beben.");
        text("story.kingdomsatwar.narration.fallen_water", "Thalassa, Mistress of the Deep, has fallen. The sea lies calm.",
                "Thalassa, die Herrin der Tiefe, ist gefallen. Das Meer liegt ruhig.");
        text("story.kingdomsatwar.narration.remaining_5", "Five dragons still guard the Seal. Bring the shard to its stone in the circle.",
                "Noch fünf Drachen bewachen das Siegel. Bring den Splitter zu seinem Stein im Kreis.");
        text("story.kingdomsatwar.narration.remaining_4", "Four dragons remain. With every shard the Seal grows weaker – and the darkness beneath grows restless.",
                "Noch vier Drachen. Mit jedem Splitter wird das Siegel schwächer – und das Dunkel darunter unruhiger.");
        text("story.kingdomsatwar.narration.remaining_3", "Half the task is done. Three dragons remain – and a whisper rises from the deep.",
                "Die Hälfte ist vollbracht. Noch drei Drachen – und aus der Tiefe dringt ein Flüstern.");
        text("story.kingdomsatwar.narration.remaining_2", "Two dragons remain. Whoever listens at the seal circle by night hears something knocking beneath the stones.",
                "Noch zwei Drachen. Wer nachts am Siegelkreis lauscht, hört es unter den Steinen pochen.");
        text("story.kingdomsatwar.narration.remaining_1", "Only one dragon still guards the Seal. Once its shard, too, rests in the circle, the Seal will break. Be ready for what waits beneath.",
                "Nur noch ein Drache bewacht das Siegel. Ruht auch sein Splitter im Kreis, wird es brechen. Sei bereit für das, was darunter wartet.");
        text("story.kingdomsatwar.narration.all_fallen", "The last dragon has fallen. Six shards, six stones – the Seal is waiting for you.",
                "Der letzte Drache ist gefallen. Sechs Splitter, sechs Steine – das Siegel wartet auf dich.");
        text("story.kingdomsatwar.narration.seal", "The Seal is broken. What the dragons kept locked away for so long has awoken. The gate stands open – and beyond it lies the Shadow World.",
                "Das Siegel ist gebrochen. Was die Drachen so lange verschlossen hielten, ist erwacht. Das Tor steht offen – und dahinter wartet die Schattenwelt.");
        text("story.kingdomsatwar.narration.crown", "The Shadow World. For centuries the Umbran folk have lived here, and somewhere in the darkness, Nyxar waits. But do not forget: back home, a realm awaits its crown.",
                "Die Schattenwelt. Seit Jahrhunderten lebt hier das Volk der Umbrer, und irgendwo in der Dunkelheit wartet Nyxar. Doch vergiss nicht: Daheim wartet ein Reich auf seine Krone.");
        text("story.kingdomsatwar.narration.nyxar", "Nyxar, the Shadow Dragon, has fallen. The shadows recede – but they do not forget.",
                "Nyxar, der Schattendrache, ist gefallen. Die Schatten weichen zurück – doch sie vergessen nicht.");
        text("story.kingdomsatwar.narration.crowned", "And so ends the Chronicle of the Seal. A kingdom, born of a single step, has found its crown. Long live the crown!",
                "Und so endet die Chronik des Siegels. Ein Königreich, geboren aus einem einzigen Schritt, hat seine Krone gefunden. Lang lebe die Krone!");
        text("intro.kingdomsatwar.title", "Kingdoms at War – Intro", "Kingdoms at War – Intro");
        text("intro.kingdomsatwar.skip", "Space: skip", "Leertaste: überspringen");
        text("kingdomsatwar.intro.intro_music", "Kingdoms at War – The Seal", "Kingdoms at War – Das Siegel");

        // --- Einstellungen ---
        text("kingdomsatwar.configuration.title", "Kingdoms at War – Settings", "Kingdoms at War – Einstellungen");
        text("kingdomsatwar.configuration.section.kingdomsatwar.client.toml", "Game settings", "Spiel-Einstellungen");
        text("kingdomsatwar.configuration.section.kingdomsatwar.client.toml.title", "Kingdoms at War – Game settings",
                "Kingdoms at War – Spiel-Einstellungen");
        text("kingdomsatwar.configuration.showIntro", "Show intro in new worlds", "Intro in neuen Welten zeigen");
        text("kingdomsatwar.configuration.showIntro.tooltip",
                "Plays the narrated intro the first time you enter a world. You can watch it again at any time with /kaw intro.",
                "Spielt beim ersten Betreten einer Welt das Erzähl-Intro ab. Mit /kaw intro kannst du es jederzeit wieder ansehen.");
        text("kingdomsatwar.configuration.spiderLook", "Spider look (arachnophobia mode)", "Aussehen der Spinnen (Arachnophobie-Modus)");
        text("kingdomsatwar.configuration.biomeAmbience", "Biome sounds", "Biom-Klänge");
        text("kingdomsatwar.configuration.biomeAmbience.tooltip", "Wind, rustling leaves, birds, surf … – in every biome, as soon as you enter it.",
                "Wind, raschelndes Laub, Vögel, Brandung … – in jedem Biom, sofort beim Betreten.");
        text("kingdomsatwar.configuration.biomeAmbienceVolume", "Biome sound volume (%)", "Lautstärke der Biom-Klänge (%)");
        text("kingdomsatwar.configuration.biomeAmbienceVolume.tooltip",
                "0 = silent. The same value as the \"Biome sounds\" slider in Minecraft's Music & Sounds options.",
                "0 = stumm. Derselbe Wert wie der Regler „Biom-Klänge“ in den Musik- und Toneinstellungen von Minecraft.");
        // der eigene Regler in den Musik- und Toneinstellungen von Minecraft
        text("kingdomsatwar.options.sounds", "Kingdoms at War", "Kingdoms at War");
        text("kingdomsatwar.options.biomeAmbience", "Biome sounds", "Biom-Klänge");
        text("kingdomsatwar.options.biomeAmbience.tooltip",
                "Wind, leaves, birds, surf and the sounds of the other worlds. Independent of the \"Ambient/Environment\" slider.",
                "Wind, Laub, Vögel, Brandung und die Klänge der fremden Welten. Unabhängig vom Regler „Umgebung“.");
        text("kingdomsatwar.configuration.spiderLook.tooltip",
                "Detailed: the mod's lifelike spiders. Classic: Minecraft's plain spiders. Mooslings: arachnophobia mode – round, friendly moss balls without legs instead of spiders.",
                "Detailliert: die lebensechten Spinnen der Mod. Klassisch: die schlichten Spinnen von Minecraft. Mooslinge: Arachnophobie-Modus – runde, freundliche Moosbälle ohne Beine statt Spinnen.");
        text("kingdomsatwar.configuration.spiderLook.detailed", "Detailed", "Detailliert");
        text("kingdomsatwar.configuration.spiderLook.classic", "Classic (Minecraft)", "Klassisch (Minecraft)");
        text("kingdomsatwar.configuration.spiderLook.moosling", "Mooslings (arachnophobia)", "Mooslinge (Arachnophobie)");
        text("kingdomsatwar.configuration.heroHearts", "Hearts and armour in layers", "Herzen und Rüstung in Schichten");
        text("kingdomsatwar.configuration.heroHearts.tooltip",
                "From 21 hearts (or 21 armour points) on, the bar shows a single row in coloured layers with the number next to it, instead of stacking rows into a wall (hearts) or cutting off at ten icons (armour).",
                "Ab 21 Herzen (oder 21 Rüstungspunkten) zeigt die Leiste eine einzige Reihe in farbigen Schichten mit der Zahl daneben, statt Reihen zu einer Wand zu stapeln (Herzen) oder bei zehn Panzern aufzuhören (Rüstung).");
        text("kingdomsatwar.configuration.narratorVoice", "Narrator voice for chapters", "Erzählerstimme zu den Kapiteln");
        text("kingdomsatwar.configuration.narratorVoice.tooltip",
                "The narrator speaks at the start of every chapter of the story (otherwise only his words appear).",
                "Der Erzähler spricht zu Beginn jedes Kapitels der Geschichte (sonst erscheinen nur seine Worte).");

        // --- Armee: Soldaten, Banditen, Feldherrentisch, Kommandostab ---
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
    /** Die Leere: Dimension, Biome, Blöcke, Gegenstände, Meldungen. */
    private void voidWorld() {
        text("itemGroup.kingdomsatwar.the_void", "Kingdoms at War: The Void", "Kingdoms at War: Die Leere");
        text("dimension.kingdomsatwar.the_void", "The Void", "Die Leere");
        text("biome.kingdomsatwar.void_expanse", "Starless Expanse", "Sternennichts");
        text("biome.kingdomsatwar.void_crystal_forest", "Crystal Forest", "Kristallwald");
        text("biome.kingdomsatwar.void_ruins", "Weightless Ruins", "Schwerelose Ruinen");
        text("biome.kingdomsatwar.void_stardust_fields", "Stardust Fields", "Sternenstaubfelder");
        text("biome.kingdomsatwar.void_shard_cliffs", "Shard Cliffs", "Splitterklippen");
        text("biome.kingdomsatwar.void_world_remnants", "World Remnants", "Weltenreste");

        // Wesen der Leere
        creature(ModEntities.VOID_MAW, io.github.sirschrolli.kingdomsatwar.registry.VoidBlocks.VOID_MAW_SPAWN_EGG, "Void Maw",
                "Leerenschlund", "Leerenschlund");
        creature(ModEntities.SHARD_WARDEN, io.github.sirschrolli.kingdomsatwar.registry.VoidBlocks.SHARD_WARDEN_SPAWN_EGG, "Shard Warden",
                "Splitterwächter", "Splitterwächter");
        creature(ModEntities.DISTORTLING, io.github.sirschrolli.kingdomsatwar.registry.VoidBlocks.DISTORTLING_SPAWN_EGG, "Distortling",
                "Zerrling", "Zerrling");
        creature(ModEntities.STAR_RAY, io.github.sirschrolli.kingdomsatwar.registry.VoidBlocks.STAR_RAY_SPAWN_EGG, "Star Ray",
                "Sternenrochen", "Sternenrochen");
        add(ModEntities.WARDEN_SHARD.get(), pick("Crystal Shard", "Kristallsplitter"));
        add(ModEntities.KRAKEN_TENTACLE.get(), pick("Kraken Tentacle", "Krakenarm"));
        subtitle("entity.void_maw.ambient", "Void Maw warbles", "Leerenschlund gurgelt");
        subtitle("entity.void_maw.hurt", "Void Maw hurts", "Leerenschlund nimmt Schaden");
        subtitle("entity.void_maw.death", "Void Maw dies", "Leerenschlund stirbt");
        subtitle("entity.void_maw.inhale", "Void Maw inhales", "Leerenschlund saugt an");
        subtitle("entity.void_maw.bite", "Void Maw bites", "Leerenschlund schnappt zu");
        subtitle("entity.shard_warden.ambient", "Shard Warden hums", "Splitterwächter summt");
        subtitle("entity.shard_warden.hurt", "Shard Warden cracks", "Splitterwächter splittert");
        subtitle("entity.shard_warden.death", "Shard Warden crumbles", "Splitterwächter zerfällt");
        subtitle("entity.shard_warden.step", "Shard Warden stomps", "Splitterwächter stampft");
        subtitle("entity.shard_warden.awaken", "Shard Warden awakens", "Splitterwächter erwacht");
        subtitle("entity.shard_warden.charge", "Crystals resonate", "Kristalle schwingen");
        subtitle("entity.shard_warden.shoot", "Shard Warden hurls a shard", "Splitterwächter schleudert einen Splitter");
        subtitle("entity.shard_warden.slam", "Shard Warden slams", "Splitterwächter schlägt zu");
        subtitle("entity.warden_shard.hit", "Crystal shard shatters", "Kristallsplitter zerspringt");
        subtitle("entity.distortling.ambient", "Distortling crackles", "Zerrling knistert");
        subtitle("entity.distortling.hurt", "Distortling glitches", "Zerrling flackert");
        subtitle("entity.distortling.death", "Distortling breaks apart", "Zerrling zerfällt");
        subtitle("entity.distortling.blink", "Distortling blinks", "Zerrling springt");
        subtitle("entity.distortling.attack", "Distortling slashes", "Zerrling schlägt zu");
        subtitle("entity.star_ray.ambient", "Star Ray sings", "Sternenrochen singt");
        subtitle("entity.star_ray.hurt", "Star Ray hurts", "Sternenrochen nimmt Schaden");
        subtitle("entity.star_ray.death", "Star Ray dies", "Sternenrochen stirbt");

        // Der Zerbrochene König und sein Thron
        creature(ModEntities.SHATTERED_KING, io.github.sirschrolli.kingdomsatwar.registry.VoidBlocks.SHATTERED_KING_SPAWN_EGG,
                "The Shattered King", "Der Zerbrochene König", "Zerbrochener-König");
        text("boss.kingdomsatwar.void", "The Shattered King", "Der Zerbrochene König");
        text("boss.kingdomsatwar.void.title", "Last King of the Ancients", "Der letzte König der Alten");
        text("message.kingdomsatwar.shattered_king.wrath", "The Shattered King rages!", "Der Zerbrochene König zürnt!");
        text("throne.kingdomsatwar.warning", "A throne of starlight hovers ahead – something old is watching.",
                "Vor dir schwebt ein Thron aus Sternenlicht – etwas Altes wacht dort.");
        block(io.github.sirschrolli.kingdomsatwar.registry.VoidBlocks.THRONE_SEAL, "Throne Seal", "Thronsiegel");
        item(io.github.sirschrolli.kingdomsatwar.registry.VoidBlocks.SHATTERED_CROWN, "Crown of the Shattered King",
                "Krone des Zerbrochenen Königs");
        text(ModItems.loreKey("shattered_crown", 1), "Seven shards of the King's crown hover above it",
                "Über ihr schweben sieben Splitter der Königskrone");
        text(ModItems.loreKey("shattered_crown", 2), "Its wearer falls gently, as if weightless (sneak to drop)",
                "Wer sie trägt, fällt sanft wie schwerelos (Schleichen: normal fallen)");
        text("item.kingdomsatwar.dragon_compass.throne", "Throne of the Ancients", "Thron der Alten");
        subtitle("entity.shattered_king.ambient", "The Shattered King hums", "Der Zerbrochene König summt");
        subtitle("entity.shattered_king.hurt", "Marble cracks", "Marmor bricht");
        subtitle("entity.shattered_king.death", "The Shattered King falls apart", "Der Zerbrochene König zerfällt");
        subtitle("entity.shattered_king.awaken", "The Shattered King rises", "Der Zerbrochene König erhebt sich");
        subtitle("entity.shattered_king.roar", "The Shattered King roars", "Der Zerbrochene König brüllt");
        subtitle("entity.shattered_king.sweep", "A giant hand sweeps", "Eine riesige Hand fegt heran");
        subtitle("entity.shattered_king.gravity", "Gravity breaks", "Die Schwerkraft bricht");
        subtitle("entity.shattered_king.beam", "Starbeam roars", "Sternenstrahl braust");
        subtitle("entity.shattered_king.rift", "Claws tear the ground open", "Klauen reißen den Boden auf");
        subtitle("entity.shattered_king.erupt", "The void erupts", "Die Leere bricht hervor");
        subtitle("entity.shattered_king.quake", "The Shattered King shakes the ground", "Der Zerbrochene König lässt den Boden beben");

        set("void_stone", "void_stone", "Void Stone", "Leerenstein", "Void Stone", "Leerenstein");
        set("void_stone_bricks", "void_stone_brick", "Void Stone Bricks", "Leerensteinziegel", "Void Stone Brick", "Leerensteinziegel");
        set("polished_void_stone", "polished_void_stone", "Polished Void Stone", "Polierter Leerenstein", "Polished Void Stone",
                "Polierte Leerenstein");
        block(io.github.sirschrolli.kingdomsatwar.registry.VoidBlocks.CRACKED_VOID_STONE_BRICKS, "Cracked Void Stone Bricks",
                "Rissige Leerensteinziegel");
        block(io.github.sirschrolli.kingdomsatwar.registry.VoidBlocks.CHISELED_VOID_STONE, "Chiseled Void Stone", "Gemeißelter Leerenstein");
        set("astral_marble", "astral_marble", "Astral Marble", "Sternenmarmor", "Astral Marble", "Sternenmarmor");
        set("astral_marble_bricks", "astral_marble_brick", "Astral Marble Bricks", "Sternenmarmorziegel", "Astral Marble Brick",
                "Sternenmarmorziegel");
        block(io.github.sirschrolli.kingdomsatwar.registry.VoidBlocks.ASTRAL_PILLAR, "Astral Pillar", "Sternenmarmorsäule");
        block(io.github.sirschrolli.kingdomsatwar.registry.VoidBlocks.CHISELED_ASTRAL_MARBLE, "Chiseled Astral Marble",
                "Gemeißelter Sternenmarmor");
        block(io.github.sirschrolli.kingdomsatwar.registry.VoidBlocks.VOID_MOSS, "Void Moss", "Leerenmoos");
        block(io.github.sirschrolli.kingdomsatwar.registry.VoidBlocks.STARDUST, "Stardust", "Sternenstaub");
        block(io.github.sirschrolli.kingdomsatwar.registry.VoidBlocks.VOID_CRYSTAL, "Void Crystal", "Leerenkristall");
        block(io.github.sirschrolli.kingdomsatwar.registry.VoidBlocks.VOID_CRYSTAL_CLUSTER, "Void Crystal Cluster", "Leerenkristall-Druse");
        block(io.github.sirschrolli.kingdomsatwar.registry.VoidBlocks.CRYSTAL_FOLIAGE, "Crystal Foliage", "Kristalllaub");
        block(io.github.sirschrolli.kingdomsatwar.registry.VoidBlocks.VOID_BLOOM, "Void Bloom", "Leerenblüte");
        block(io.github.sirschrolli.kingdomsatwar.registry.VoidBlocks.STAR_VINES, "Star Vines", "Sternenranke");
        block(io.github.sirschrolli.kingdomsatwar.registry.VoidBlocks.STAR_VINES_PLANT, "Star Vines", "Sternenranke");
        block(io.github.sirschrolli.kingdomsatwar.registry.VoidBlocks.VOID_RIFT, "Void Rift", "Leerenriss");
        item(io.github.sirschrolli.kingdomsatwar.registry.VoidBlocks.VOID_SHARD, "Void Shard", "Leerensplitter");

        // Der Abgrund
        text("itemGroup.kingdomsatwar.the_abyss", "Kingdoms at War: The Abyss", "Kingdoms at War: Der Abgrund");
        text("dimension.kingdomsatwar.the_abyss", "The Abyss", "Der Abgrund");
        text("biome.kingdomsatwar.abyss_fractured_lands", "Fractured Lands", "Bruchlande");
        text("biome.kingdomsatwar.abyss_ember_falls", "Ember Falls", "Glutsturz");
        text("biome.kingdomsatwar.abyss_star_wreck", "Star Wreck", "Sternentrümmer");
        text("biome.kingdomsatwar.abyss_shadow_chasm", "Shadow Chasm", "Schattenschlund");
        text("biome.kingdomsatwar.abyss_seam", "The Seam", "Die Naht");

        // Der Weltenfresser und der Schlund der Welten (0.73)
        creature(ModEntities.WORLD_EATER, io.github.sirschrolli.kingdomsatwar.registry.AbyssBlocks.WORLD_EATER_SPAWN_EGG,
                "The World-Eater", "Der Weltenfresser", "Weltenfresser");
        add(ModEntities.WORLD_CHUNK.get(), pick("World Chunk", "Weltbrocken"));
        text("boss.kingdomsatwar.world_eater", "The World-Eater", "Der Weltenfresser");
        item(io.github.sirschrolli.kingdomsatwar.registry.AbyssBlocks.WORLD_SPEAR, "World Spear", "Weltenspeer");
        lore("world_spear", "Forged from the fang of the World-Eater", "Aus dem Fangzahn des Weltenfressers geschmiedet");
        text(ModItems.loreKey("world_spear", 2), "In the Abyss and the Void: strikes harder; every hit carries one of four worlds",
                "Im Abgrund und in der Leere: trifft härter; jeder Treffer trägt eine von vier Welten in sich");
        text(ModItems.loreKey("world_spear", 3), "Embers burn, frost slows, End lifts, shadow blinds – every fourth hit calls down a world chunk",
                "Glut brennt, Frost lähmt, End hebt, Schatten blendet – jeder vierte Treffer ruft einen Weltbrocken herab");
        block(io.github.sirschrolli.kingdomsatwar.registry.AbyssBlocks.WORLD_EATER_TROPHY, "Head of the World-Eater", "Kopf des Weltenfressers");
        lore("world_eater_trophy", "The trophy of the World Savior", "Die Trophäe des Weltenretters");
        text(ModItems.loreKey("world_eater_trophy", 2), "Its seams still glow; now and then embers rise from its maw",
                "Seine Nähte glühen noch; ab und zu steigt Glut aus seinem Rachen");
        text("message.kingdomsatwar.world_eater.wrath", "The World-Eater rages – the ring is breaking!", "Der Weltenfresser tobt – der Ring bricht!");
        text("hoard.kingdomsatwar.warning", "Beneath the ring ahead, something vast is stirring.", "Unter dem Ring vor dir regt sich etwas Gewaltiges.");
        block(io.github.sirschrolli.kingdomsatwar.registry.AbyssBlocks.SEAL_SHARD, "Crown Shard", "Kronensplitter");
        block(io.github.sirschrolli.kingdomsatwar.registry.AbyssBlocks.HOARD_SEAL, "Hoard Seal", "Siegel des Horts");
        text("title.kingdomsatwar.world_savior", "World Savior", "Weltenretter");
        text("title.kingdomsatwar.world_savior.sub", "The World-Eater has fallen", "Der Weltenfresser ist gefallen");
        text("title.kingdomsatwar.world_savior.prefix", "World Savior", "Weltenretter");
        text("message.kingdomsatwar.world_savior", "%s has helped slay the World-Eater and is now a World Savior!",
                "%s hat den Weltenfresser mit bezwungen und ist jetzt Weltenretter!");
        text("item.kingdomsatwar.dragon_compass.hoard", "Maw of Worlds", "Schlund der Welten");
        subtitle("entity.world_eater.ambient", "The World-Eater growls", "Der Weltenfresser grollt");
        subtitle("entity.world_eater.hurt", "Chaos stone cracks", "Chaosgestein bricht");
        subtitle("entity.world_eater.death", "The World-Eater breaks apart", "Der Weltenfresser zerbricht");
        subtitle("entity.world_eater.awaken", "Something rises from the abyss", "Etwas steigt aus dem Abgrund");
        subtitle("entity.world_eater.shatter", "A crown shard shatters", "Ein Kronensplitter zerspringt");
        subtitle("entity.world_eater.roar", "The World-Eater roars", "Der Weltenfresser brüllt");
        subtitle("entity.world_eater.bite", "Stone jaws snap", "Steinerne Kiefer schnappen zu");
        subtitle("entity.world_eater.inhale", "The World-Eater inhales", "Der Weltenfresser saugt an");
        subtitle("entity.world_eater.snap", "The maw slams shut", "Der Rachen schlägt zu");
        subtitle("entity.world_eater.spit", "The World-Eater spits", "Der Weltenfresser speit");
        subtitle("entity.world_eater.chunk", "A world chunk shatters", "Ein Weltbrocken zerschellt");
        subtitle("entity.world_eater.sweep", "A colossal body sweeps past", "Ein gewaltiger Leib fegt vorbei");
        subtitle("entity.world_eater.breath", "Rift fire roars", "Rissfeuer braust");
        subtitle("entity.world_eater.gravity", "Gravity breaks", "Die Schwerkraft bricht");
        subtitle("entity.world_eater.call", "The World-Eater calls", "Der Weltenfresser ruft");
        subtitle("entity.world_eater.rift", "A rift tears open", "Ein Riss reißt auf");
        subtitle("entity.world_eater.crumble", "A segment crumbles", "Ein Leibglied zerbricht");
        set("chaos_stone", "chaos_stone", "Chaos Stone", "Chaosgestein", "Chaos Stone", "Chaosgestein");
        set("chaos_stone_bricks", "chaos_stone_brick", "Chaos Stone Bricks", "Chaossteinziegel", "Chaos Stone Brick", "Chaossteinziegel");
        set("polished_chaos_stone", "polished_chaos_stone", "Polished Chaos Stone", "Poliertes Chaosgestein", "Polished Chaos Stone",
                "Polierte Chaosgestein");
        block(io.github.sirschrolli.kingdomsatwar.registry.AbyssBlocks.CRACKED_CHAOS_STONE_BRICKS, "Cracked Chaos Stone Bricks", "Rissige Chaossteinziegel");
        block(io.github.sirschrolli.kingdomsatwar.registry.AbyssBlocks.CHISELED_CHAOS_STONE, "Chiseled Chaos Stone", "Gemeißeltes Chaosgestein");
        block(io.github.sirschrolli.kingdomsatwar.registry.AbyssBlocks.WORLD_SEAM, "World Seam", "Weltennaht");
        block(io.github.sirschrolli.kingdomsatwar.registry.AbyssBlocks.FLOAT_CRYSTAL, "Float Crystal", "Schwebekristall");
        block(io.github.sirschrolli.kingdomsatwar.registry.AbyssBlocks.LIFT_CRYSTAL, "Lift Crystal", "Hebekristall");
        block(io.github.sirschrolli.kingdomsatwar.registry.AbyssBlocks.ABYSS_RIFT, "Abyss Rift", "Abgrundriss");
        item(io.github.sirschrolli.kingdomsatwar.registry.AbyssBlocks.RIFT_KEY, "Rift Key", "Rissschlüssel");
        text(ModItems.loreKey("rift_key", 1), "Forged from the shards of the Shattered King's crown",
                "Aus den Splittern der Krone des Zerbrochenen Königs geschmiedet");
        text(ModItems.loreKey("rift_key", 2), "Use on the ground in the Void: tears open a rift into the Abyss",
                "In der Leere auf den Boden gerichtet: reißt einen Riss in den Abgrund auf");
        text("item.kingdomsatwar.rift_key.not_void", "Only in the Void does the world tear deep enough.",
                "Nur in der Leere reißt die Welt tief genug auf.");
        text("item.kingdomsatwar.rift_key.in_abyss", "There is nothing deeper than the Abyss.", "Tiefer als der Abgrund geht es nicht.");
        text("item.kingdomsatwar.rift_key.cannot", "The Abyss cannot be reached from here.", "Der Abgrund ist von hier aus nicht zu erreichen.");
        text("item.kingdomsatwar.rift_key.no_space", "There is not enough room for a rift here.", "Hier ist nicht genug Platz für einen Riss.");
        text("item.kingdomsatwar.rift_key.too_close", "Another rift is too close.", "Ein anderer Riss ist zu nah.");
        text("item.kingdomsatwar.rift_key.anchor", "This rift cannot be closed.", "Dieser Riss lässt sich nicht schließen.");
        text("message.kingdomsatwar.abyss.spat_out", "The Abyss hurls you back – wounded, but alive.",
                "Der Abgrund schleudert dich zurück – verwundet, aber lebendig.");
        text("message.kingdomsatwar.abyss.throne_gate", "Behind the throne, the world tears open – a rift into the Abyss.",
                "Hinter dem Thron reißt die Welt auf – ein Riss in den Abgrund.");
        text("block.kingdomsatwar.bed.the_abyss", "There is no sleep in the Abyss.", "Im Abgrund schläft niemand.");

        // Das Meer
        text("itemGroup.kingdomsatwar.the_sea", "Kingdoms at War: The Sea", "Kingdoms at War: Das Meer");
        creature(ModEntities.MANTA_RAY, io.github.sirschrolli.kingdomsatwar.registry.SeaBlocks.MANTA_RAY_SPAWN_EGG, "Manta Ray", "Mantarochen", "Mantarochen");
        creature(ModEntities.GLOW_JELLYFISH, io.github.sirschrolli.kingdomsatwar.registry.SeaBlocks.GLOW_JELLYFISH_SPAWN_EGG, "Glow Jellyfish", "Leuchtqualle", "Leuchtquallen");
        creature(ModEntities.SHARK, io.github.sirschrolli.kingdomsatwar.registry.SeaBlocks.SHARK_SPAWN_EGG, "Shark", "Hai", "Hai");
        creature(ModEntities.ANGLERFISH, io.github.sirschrolli.kingdomsatwar.registry.SeaBlocks.ANGLERFISH_SPAWN_EGG, "Anglerfish", "Anglerfisch", "Anglerfisch");
        creature(ModEntities.GIANT_CRAB, io.github.sirschrolli.kingdomsatwar.registry.SeaBlocks.GIANT_CRAB_SPAWN_EGG, "Giant Crab", "Riesenkrabbe", "Riesenkrabben");
        block(io.github.sirschrolli.kingdomsatwar.registry.SeaBlocks.DEEP_SEA_LANTERN, "Deep Sea Lantern", "Tiefseelaterne");
        item(io.github.sirschrolli.kingdomsatwar.registry.SeaBlocks.GLOW_JELLY, "Glow Jelly", "Leuchtgelee");
        lore("glow_jelly", "Brew into an Awkward Potion for Night Vision", "In einen seltsamen Trank gebraut: Nachtsicht");
        item(io.github.sirschrolli.kingdomsatwar.registry.SeaBlocks.ANGLER_LURE, "Angler Lure", "Leuchtköder");
        lore("angler_lure", "Still glowing – the heart of a Deep Sea Lantern", "Leuchtet noch immer – das Herz einer Tiefseelaterne");
        item(io.github.sirschrolli.kingdomsatwar.registry.SeaBlocks.SHARK_TOOTH, "Shark Tooth", "Haizahn");
        lore("shark_tooth", "With a stick and a feather: six arrows", "Mit Stock und Feder: sechs Pfeile");
        item(io.github.sirschrolli.kingdomsatwar.registry.SeaBlocks.CRAB_CLAW, "Crab Claw", "Krabbenschere");
        lore("crab_claw", "In the off hand you reach further", "In der Zweithand greifst du weiter");
        item(io.github.sirschrolli.kingdomsatwar.registry.SeaBlocks.CRAB_MEAT, "Raw Crab Meat", "Rohes Krabbenfleisch");
        item(io.github.sirschrolli.kingdomsatwar.registry.SeaBlocks.COOKED_CRAB_MEAT, "Cooked Crab Meat", "Gegartes Krabbenfleisch");
        item(io.github.sirschrolli.kingdomsatwar.registry.SeaBlocks.GLOW_JELLYFISH_BUCKET, "Bucket of Glow Jellyfish", "Eimer mit Leuchtqualle");
        // Meeresgrund (0.62)
        block(io.github.sirschrolli.kingdomsatwar.registry.SeaBlocks.PURPLE_SEA_ANEMONE, "Purple Sea Anemone", "Purpur-Seeanemone");
        block(io.github.sirschrolli.kingdomsatwar.registry.SeaBlocks.RED_SEA_ANEMONE, "Fire Sea Anemone", "Feuer-Seeanemone");
        block(io.github.sirschrolli.kingdomsatwar.registry.SeaBlocks.GREEN_SEA_ANEMONE, "Emerald Sea Anemone", "Smaragd-Seeanemone");
        block(io.github.sirschrolli.kingdomsatwar.registry.SeaBlocks.ORANGE_SEA_ANEMONE, "Sun Sea Anemone", "Sonnen-Seeanemone");
        block(io.github.sirschrolli.kingdomsatwar.registry.SeaBlocks.COCKLE_SHELLS, "Cockle Shells", "Herzmuscheln");
        block(io.github.sirschrolli.kingdomsatwar.registry.SeaBlocks.SCALLOP_SHELLS, "Scallop Shells", "Kammmuscheln");
        block(io.github.sirschrolli.kingdomsatwar.registry.SeaBlocks.WHELK_SHELLS, "Whelk Shells", "Wellhornschnecken");
        block(io.github.sirschrolli.kingdomsatwar.registry.SeaBlocks.TUBE_WORMS, "Tube Worms", "Röhrenwürmer");
        block(io.github.sirschrolli.kingdomsatwar.registry.SeaBlocks.VENT_ROCK, "Vent Rock", "Schlotgestein");
        block(io.github.sirschrolli.kingdomsatwar.registry.SeaBlocks.SULFUR_CRUST, "Sulfur Crust", "Schwefelkruste");
        block(io.github.sirschrolli.kingdomsatwar.registry.SeaBlocks.HYDROTHERMAL_VENT, "Hydrothermal Vent", "Heißer Schlot");
        block(io.github.sirschrolli.kingdomsatwar.registry.SeaBlocks.GLOW_ALGAE, "Glow Algae", "Leuchtalgen");
        block(io.github.sirschrolli.kingdomsatwar.registry.SeaBlocks.GIANT_KELP, "Giant Kelp", "Riesentang");
        block(io.github.sirschrolli.kingdomsatwar.registry.SeaBlocks.GIANT_KELP_PLANT, "Giant Kelp", "Riesentang");
        // Karkinos, der Riffkoloss, und das Versunkene Atoll (0.62)
        creature(ModEntities.REEF_COLOSSUS, io.github.sirschrolli.kingdomsatwar.registry.SeaBlocks.REEF_COLOSSUS_SPAWN_EGG,
                "Karkinos, the Reef Colossus", "Karkinos, der Riffkoloss", "Karkinos");
        text("boss.kingdomsatwar.reef", "Karkinos, the Reef Colossus", "Karkinos, der Riffkoloss");
        text("boss.kingdomsatwar.reef.title", "Lord of the Sunken Atoll", "Herr des Versunkenen Atolls");
        text("message.kingdomsatwar.reef_colossus.tide", "Karkinos calls the tide!", "Karkinos ruft die Flut!");
        // der Kraken und der Krakenschlund (0.91)
        // (der Name der Kreatur ohne Artikel – sonst hieße es „von Der Kraken getötet“; die Bossleiste sagt „Der Kraken“)
        creature(ModEntities.KRAKEN, io.github.sirschrolli.kingdomsatwar.registry.SeaBlocks.KRAKEN_SPAWN_EGG, "Kraken", "Kraken", "Kraken");
        text("boss.kingdomsatwar.kraken", "The Kraken", "Der Kraken");
        text("boss.kingdomsatwar.kraken.title", "Lord of the Primordial Sea", "Herr des Urmeers");
        text("kraken.kingdomsatwar.warning", "The water above the maw is eerily still …", "Das Wasser über dem Schlund ist unheimlich still …");
        // das Meervolk und die Meeresdörfer (0.93)
        creature(ModEntities.MERMAID, io.github.sirschrolli.kingdomsatwar.registry.SeaBlocks.MERMAID_SPAWN_EGG, "Mermaid", "Meerjungfrau",
                "Meerjungfrauen");
        creature(ModEntities.TRITON, io.github.sirschrolli.kingdomsatwar.registry.SeaBlocks.TRITON_SPAWN_EGG, "Triton Guard", "Tritonwache",
                "Tritonwachen");
        text("entity.kingdomsatwar.mermaid.pearl_diver", "Pearl Diver", "Perlentaucherin");
        text("entity.kingdomsatwar.mermaid.coral_gardener", "Coral Gardener", "Korallengärtnerin");
        text("entity.kingdomsatwar.mermaid.shell_builder", "Shell Builder", "Muschelbauerin");
        text("entity.kingdomsatwar.mermaid.tide_singer", "Tide Singer", "Gezeitensängerin");
        text("entity.kingdomsatwar.mermaid.guard", "Triton Guard", "Tritonwache");
        text("entity.kingdomsatwar.triton.greeting1", "Welcome to our village, land-walker.", "Willkommen in unserem Dorf, Landgänger.");
        text("entity.kingdomsatwar.triton.greeting2", "Pearls buy everything here – and our friendship.", "Perlen kaufen hier alles – auch unsere Freundschaft.");
        text("entity.kingdomsatwar.triton.greeting3", "Mind the sharks beyond the gardens.", "Hüte dich vor den Haien hinter den Gärten.");
        text("entity.kingdomsatwar.triton.greeting4", "Deep down the Kraken sleeps. Do not wake it lightly.",
                "Tief unten schläft der Kraken. Weck ihn nicht leichtfertig.");
        text("entity.kingdomsatwar.triton.greeting5", "Listen – the tide singer is singing again.", "Hör nur – die Gezeitensängerin singt wieder.");
        item(io.github.sirschrolli.kingdomsatwar.registry.SeaBlocks.PEARL, "Pearl", "Perle");
        lore("pearl", "Currency of the merfolk", "Die Währung des Meervolks");
        subtitle("entity.mermaid.ambient", "Mermaid hums", "Meerjungfrau summt");
        subtitle("entity.mermaid.trade", "Mermaid sings", "Meerjungfrau singt");
        subtitle("entity.mermaid.yes", "Mermaid agrees", "Meerjungfrau stimmt zu");
        subtitle("entity.mermaid.no", "Mermaid disagrees", "Meerjungfrau lehnt ab");
        subtitle("entity.mermaid.hurt", "Mermaid hurts", "Meerjungfrau nimmt Schaden");
        subtitle("entity.mermaid.death", "Mermaid dies", "Meerjungfrau stirbt");
        subtitle("entity.triton.ambient", "Triton Guard hums", "Tritonwache brummt");
        subtitle("entity.triton.hurt", "Triton Guard hurts", "Tritonwache nimmt Schaden");
        subtitle("entity.triton.death", "Triton Guard dies", "Tritonwache stirbt");
        text("item.kingdomsatwar.tide_horn.kraken_recalled", "Deep below, something huge begins to stir …",
                "Tief unten regt sich etwas Gewaltiges …");
        text("atoll.kingdomsatwar.warning", "Something stirs beneath the sand of the lagoon …", "Unter dem Sand der Lagune regt sich etwas …");
        block(io.github.sirschrolli.kingdomsatwar.registry.SeaBlocks.ATOLL_CORE, "Heart of the Atoll", "Herz des Atolls");
        block(io.github.sirschrolli.kingdomsatwar.registry.SeaBlocks.KRAKEN_CORE, "Heart of the Maw", "Herz des Schlunds");
        // Das Urmeer (0.89)
        text("dimension.kingdomsatwar.urmeer", "The Primordial Sea", "Das Urmeer");
        text("message.kingdomsatwar.urmeer_no_lava", "In the Primordial Sea, lava hisses out at once",
                "Im Urmeer erlischt die Lava zischend");
        text("message.kingdomsatwar.urmeer_no_sponge", "The Primordial Sea cannot be drained – the sponge soaks up, but the water stays",
                "Das Urmeer lässt sich nicht trockenlegen – der Schwamm saugt sich voll, doch das Wasser bleibt");
        text("biome.kingdomsatwar.urmeer_deep", "Primordial Expanse", "Urmeer-Weiten");
        text("biome.kingdomsatwar.urmeer_reef", "Coral Banks", "Korallenbänke");
        text("biome.kingdomsatwar.urmeer_kelp_forest", "Kelp Forest", "Tangwald");
        text("biome.kingdomsatwar.urmeer_coral_garden", "Coral Garden", "Korallengarten");
        text("biome.kingdomsatwar.urmeer_pearl_grounds", "Pearl Grounds", "Perlmuttgründe");
        text("biome.kingdomsatwar.urmeer_glow_depths", "Glowing Depths", "Leuchtende Tiefe");
        text("biome.kingdomsatwar.urmeer_trench", "Deep Sea Trench", "Tiefseegraben");
        text("biome.kingdomsatwar.urmeer_isles", "Storm Isles", "Sturminseln");
        set("tide_stone", "tide_stone", "Tide Stone", "Gezeitenstein", "Tide Stone", "Gezeitenstein");
        set("tide_stone_bricks", "tide_stone_brick", "Tide Stone Bricks", "Gezeitensteinziegel", "Tide Stone Brick", "Gezeitensteinziegel");
        block(io.github.sirschrolli.kingdomsatwar.registry.SeaBlocks.CHISELED_TIDE_STONE, "Chiseled Tide Stone", "Gemeißelter Gezeitenstein");
        block(io.github.sirschrolli.kingdomsatwar.registry.SeaBlocks.TIDE_GATE, "Tide Gate", "Gezeitentor");
        text("block.kingdomsatwar.tide_gate.no_frame", "The heart of the sea needs a complete frame of tide stone bricks.",
                "Das Herz des Meeres braucht einen vollständigen Rahmen aus Gezeitensteinziegeln.");
        // Das Feenreich (0.63)
        text("itemGroup.kingdomsatwar.the_fae_realm", "Kingdoms at War: The Fae Realm", "Kingdoms at War: Das Feenreich");
        text("itemGroup.kingdomsatwar.legends", "Kingdoms at War: Legends", "Kingdoms at War: Legenden");
        text("dimension.kingdomsatwar.fae_realm", "The Fae Realm", "Das Feenreich");
        text("biome.kingdomsatwar.fae_meadows", "Blossom Meadows", "Blütenwiesen");
        text("biome.kingdomsatwar.glimmerwood", "Glimmerwood", "Glimmerwald");
        text("biome.kingdomsatwar.fae_mushroom_grove", "Mushroom Grove", "Pilzhain");
        text("biome.kingdomsatwar.moonlit_lakes", "Moonlit Lakes", "Mondseen");
        text("biome.kingdomsatwar.thornwild", "Thornwild", "Dornenwildnis");
        text("block.kingdomsatwar.bed.fae_realm", "Night never falls in the Fae Realm – but your bed will remember this place.",
                "Im Feenreich wird es nie Nacht – aber dein Bett merkt sich diesen Ort.");
        set("faestone", "faestone", "Faestone", "Feenstein", "Faestone", "Feenstein");
        set("faestone_bricks", "faestone_brick", "Faestone Bricks", "Feensteinziegel", "Faestone Brick", "Feensteinziegel");
        block(io.github.sirschrolli.kingdomsatwar.registry.FaeBlocks.MOSSY_FAESTONE_BRICKS, "Mossy Faestone Bricks", "Bemooste Feensteinziegel");
        set("moonstone_bricks", "moonstone_brick", "Moonstone Bricks", "Mondsteinziegel", "Moonstone Brick", "Mondsteinziegel");
        block(io.github.sirschrolli.kingdomsatwar.registry.FaeBlocks.CHISELED_MOONSTONE, "Chiseled Moonstone", "Gemeißelter Mondstein");
        // Mondstein (0.96)
        block(io.github.sirschrolli.kingdomsatwar.registry.FaeBlocks.MOONSTONE_ORE, "Moonstone Ore", "Mondsteinerz");
        block(io.github.sirschrolli.kingdomsatwar.registry.FaeBlocks.MOONSTONE_BLOCK, "Block of Moonstone", "Mondsteinblock");
        item(io.github.sirschrolli.kingdomsatwar.registry.FaeBlocks.MOONSTONE, "Moonstone", "Mondstein");
        lore("moonstone", "A milky stone with a wandering blue shimmer – it stores moonlight",
                "Ein milchiger Stein, in dem ein blauer Schimmer wandert – er speichert Mondlicht");
        item(io.github.sirschrolli.kingdomsatwar.registry.FaeGear.MOONSTONE_HELMET, "Moonstone Circlet", "Mondsteindiadem");
        item(io.github.sirschrolli.kingdomsatwar.registry.FaeGear.MOONSTONE_CHESTPLATE, "Moonstone Chestplate", "Mondsteinharnisch");
        item(io.github.sirschrolli.kingdomsatwar.registry.FaeGear.MOONSTONE_LEGGINGS, "Moonstone Leggings", "Mondsteinbeinschutz");
        item(io.github.sirschrolli.kingdomsatwar.registry.FaeGear.MOONSTONE_BOOTS, "Moonstone Boots", "Mondsteinstiefel");
        lore("moonstone_armor", "Stores moonlight: each piece +15 mana and 10 % faster mana regeneration",
                "Speichert Mondlicht: jeder Teil +15 Mana und 10 % schnellere Erholung");
        text(ModItems.loreKey("moonstone_armor", 2), "Full set: another +20 mana and 10 % more",
                "Voller Satz: noch einmal +20 Mana und 10 % mehr");
        // Aquamarin (0.97)
        block(io.github.sirschrolli.kingdomsatwar.registry.SeaGear.AQUAMARINE_ORE, "Aquamarine Ore", "Aquamarinerz");
        block(io.github.sirschrolli.kingdomsatwar.registry.SeaGear.AQUAMARINE_BLOCK, "Block of Aquamarine", "Aquamarinblock");
        item(io.github.sirschrolli.kingdomsatwar.registry.SeaGear.AQUAMARINE, "Aquamarine", "Aquamarin");
        lore("aquamarine", "A clear stone the colour of the sea over pale sand", "Ein klarer Stein in der Farbe des Meeres über hellem Sand");
        item(io.github.sirschrolli.kingdomsatwar.registry.SeaGear.AQUAMARINE_HELMET, "Aquamarine Diving Helmet", "Aquamarin-Taucherhelm");
        item(io.github.sirschrolli.kingdomsatwar.registry.SeaGear.AQUAMARINE_CHESTPLATE, "Aquamarine Diving Harness", "Aquamarin-Taucherharnisch");
        item(io.github.sirschrolli.kingdomsatwar.registry.SeaGear.AQUAMARINE_LEGGINGS, "Aquamarine Leggings", "Aquamarin-Beinschutz");
        item(io.github.sirschrolli.kingdomsatwar.registry.SeaGear.AQUAMARINE_BOOTS, "Aquamarine Flippers", "Aquamarin-Flossen");
        lore("aquamarine_armor", "Under water: breathe longer (helmet, harness), swim faster (leggings, flippers)",
                "Unter Wasser: länger atmen (Helm, Harnisch), schneller schwimmen (Beinschutz, Flossen)");
        text(ModItems.loreKey("aquamarine_armor", 2), "Full set: water breathing and clear sight under water",
                "Voller Satz: Wasseratmung und klare Sicht unter Wasser");
        // Dämmerquarz und die Dämmerrüstung (0.98)
        block(io.github.sirschrolli.kingdomsatwar.registry.ShadowGear.DUSK_QUARTZ_ORE, "Dusk Quartz Ore", "Dämmerquarzerz");
        block(io.github.sirschrolli.kingdomsatwar.registry.ShadowGear.DEEP_DUSK_QUARTZ_ORE, "Deep Dusk Quartz Ore", "Tiefes Dämmerquarzerz");
        block(io.github.sirschrolli.kingdomsatwar.registry.ShadowGear.DUSK_QUARTZ_BLOCK, "Block of Dusk Quartz", "Dämmerquarzblock");
        item(io.github.sirschrolli.kingdomsatwar.registry.ShadowGear.DUSK_QUARTZ, "Dusk Quartz", "Dämmerquarz");
        lore("dusk_quartz", "A smoky quartz holding the colours of dusk – night above, a last red glow below",
                "Ein rauchiger Quarz mit den Farben der Dämmerung – oben Nacht, unten ein letztes Abendrot");
        item(io.github.sirschrolli.kingdomsatwar.registry.ShadowGear.DUSK_HOOD, "Dusk Hood", "Dämmerkapuze");
        item(io.github.sirschrolli.kingdomsatwar.registry.ShadowGear.DUSK_CLOAK, "Dusk Cloak", "Dämmermantel");
        item(io.github.sirschrolli.kingdomsatwar.registry.ShadowGear.DUSK_LEGGINGS, "Dusk Leggings", "Dämmerbeinlinge");
        item(io.github.sirschrolli.kingdomsatwar.registry.ShadowGear.DUSK_BOOTS, "Dusk Boots", "Dämmerstiefel");
        lore("dusk_armor", "Each piece: monsters notice you later",
                "Jeder Teil: Monster bemerken dich später");
        text(ModItems.loreKey("dusk_armor", 2), "Sneak faster (leggings, boots), soft landing (boots), see in the dark while sneaking (hood)",
                "Schneller schleichen (Beinlinge, Stiefel), sanft landen (Stiefel), beim Schleichen im Dunkeln sehen (Kapuze)");
        text(ModItems.loreKey("dusk_armor", 3), "Full set while sneaking: almost unseen",
                "Voller Satz beim Schleichen: fast unsichtbar");
        // Himmelseisen und die Rüstung der Sternwandler (0.99)
        block(io.github.sirschrolli.kingdomsatwar.registry.VoidGear.SKY_IRON_ORE, "Sky Iron Ore", "Himmelseisenerz");
        block(io.github.sirschrolli.kingdomsatwar.registry.VoidGear.SKY_IRON_BLOCK, "Block of Sky Iron", "Himmelseisenblock");
        item(io.github.sirschrolli.kingdomsatwar.registry.VoidGear.RAW_SKY_IRON, "Raw Sky Iron", "Rohes Himmelseisen");
        lore("raw_sky_iron", "Iron from a fallen star, under a black crust of fire",
                "Eisen aus einem gefallenen Stern, unter einer schwarzen Kruste aus Feuer");
        item(io.github.sirschrolli.kingdomsatwar.registry.VoidGear.SKY_IRON_INGOT, "Sky Iron Ingot", "Himmelseisenbarren");
        lore("sky_iron_ingot", "Polished, it shows the pattern of the stars",
                "Geschliffen zeigt es das Muster der Sterne");
        item(io.github.sirschrolli.kingdomsatwar.registry.VoidGear.STARWALKER_HELMET, "Starwalker Helm", "Sternwandlerhelm");
        item(io.github.sirschrolli.kingdomsatwar.registry.VoidGear.STARWALKER_CHESTPLATE, "Starwalker Cuirass", "Sternwandlerharnisch");
        item(io.github.sirschrolli.kingdomsatwar.registry.VoidGear.STARWALKER_LEGGINGS, "Starwalker Greaves", "Sternwandlerbeinschienen");
        item(io.github.sirschrolli.kingdomsatwar.registry.VoidGear.STARWALKER_BOOTS, "Starwalker Boots", "Sternwandlerstiefel");
        lore("starwalker_armor", "Each piece: lighter – jump higher, fall slower; boots: soft landing",
                "Jeder Teil: leichter – höher springen, langsamer fallen; Stiefel: sanft landen");
        text(ModItems.loreKey("starwalker_armor", 2), "Full set: hold jump in the air to float down gently",
                "Voller Satz: Sprungtaste in der Luft halten – du schwebst sanft herab");
        text(ModItems.loreKey("starwalker_armor", 3), "Full set: falling into the void carries you back (every two minutes)",
                "Voller Satz: Sturz ins Nichts trägt dich zurück (alle zwei Minuten)");
        text("message.kingdomsatwar.starwalker_anchor", "The stars carry you back to firm ground",
                "Die Sterne tragen dich zurück auf festen Boden");
        // Chronit und die Rüstung der Zeitwächter (1.1)
        block(io.github.sirschrolli.kingdomsatwar.registry.AbyssGear.CHRONIT_ORE, "Chronite Ore", "Chronit-Erz");
        block(io.github.sirschrolli.kingdomsatwar.registry.AbyssGear.CHRONIT_BLOCK, "Block of Chronite", "Chronitblock");
        item(io.github.sirschrolli.kingdomsatwar.registry.AbyssGear.CHRONIT, "Chronite", "Chronit");
        lore("chronit", "Time, curdled to crystal where two worlds meet – sparks hang frozen inside",
                "Zeit, zu Kristall geronnen, wo zwei Welten aneinanderstoßen – innen stehen erstarrte Funken");
        item(io.github.sirschrolli.kingdomsatwar.registry.AbyssGear.TIMEWARDEN_HELMET, "Timewarden Helm", "Zeitwächterhelm");
        item(io.github.sirschrolli.kingdomsatwar.registry.AbyssGear.TIMEWARDEN_CHESTPLATE, "Timewarden Cuirass", "Zeitwächterharnisch");
        item(io.github.sirschrolli.kingdomsatwar.registry.AbyssGear.TIMEWARDEN_LEGGINGS, "Timewarden Greaves", "Zeitwächterbeinschienen");
        item(io.github.sirschrolli.kingdomsatwar.registry.AbyssGear.TIMEWARDEN_BOOTS, "Timewarden Boots", "Zeitwächterstiefel");
        lore("timewarden_armor", "Each piece: those who strike you sometimes falter (slower for a moment)",
                "Jeder Teil: Wer dich trifft, gerät manchmal ins Stocken (kurz langsamer)");
        text(ModItems.loreKey("timewarden_armor", 2), "Full set: a deadly blow turns time back three seconds",
                "Voller Satz: Ein tödlicher Treffer spult die Zeit drei Sekunden zurück");
        text(ModItems.loreKey("timewarden_armor", 3), "Then your clock stands still for three minutes",
                "Danach steht deine Uhr drei Minuten still");
        text("message.kingdomsatwar.timewarden_rewind", "Time turns back – your clock stands still",
                "Die Zeit spult zurück – deine Uhr steht still");
        text("message.kingdomsatwar.timewarden_ready", "Your clock is ticking again", "Deine Uhr tickt wieder");
        subtitle("item.timewarden.rewind", "Time turns back", "Die Zeit spult zurück");
        subtitle("item.timewarden.tick", "Time falters", "Die Zeit stockt");
        subtitle("item.timewarden.ready", "A clock ticks again", "Eine Uhr tickt wieder");
        // Feueropal und die Rüstung der Glutwandler (1.1.6)
        block(io.github.sirschrolli.kingdomsatwar.registry.FlameGear.FIRE_OPAL_ORE, "Fire Opal Ore", "Feueropal-Erz");
        block(io.github.sirschrolli.kingdomsatwar.registry.FlameGear.BLACKSTONE_FIRE_OPAL_ORE, "Blackstone Fire Opal Ore", "Schwarzstein-Feueropal-Erz");
        block(io.github.sirschrolli.kingdomsatwar.registry.FlameGear.FIRE_OPAL_BLOCK, "Block of Fire Opal", "Feueropalblock");
        block(io.github.sirschrolli.kingdomsatwar.registry.FlameGear.LAVA_CRUST, "Lava Crust", "Glutkruste");
        item(io.github.sirschrolli.kingdomsatwar.registry.FlameGear.FIRE_OPAL, "Fire Opal", "Feueropal");
        lore("fire_opal", "Red and gold like the lava around it – and full of green and blue flashes",
                "Rot und gold wie die Lava ringsum – und voller grüner und blauer Blitze");
        item(io.github.sirschrolli.kingdomsatwar.registry.FlameGear.EMBERWALKER_HELMET, "Emberwalker Helm", "Glutwandlerhelm");
        item(io.github.sirschrolli.kingdomsatwar.registry.FlameGear.EMBERWALKER_CHESTPLATE, "Emberwalker Cuirass", "Glutwandlerharnisch");
        item(io.github.sirschrolli.kingdomsatwar.registry.FlameGear.EMBERWALKER_LEGGINGS, "Emberwalker Greaves", "Glutwandlerbeinschienen");
        item(io.github.sirschrolli.kingdomsatwar.registry.FlameGear.EMBERWALKER_BOOTS, "Emberwalker Boots", "Glutwandlerstiefel");
        lore("emberwalker_armor", "Each piece: a fifth less harm from fire and lava",
                "Jeder Teil: ein Fünftel weniger Schaden durch Feuer und Lava");
        text(ModItems.loreKey("emberwalker_armor", 2), "Helm: you see far inside lava",
                "Helm: In der Lava siehst du weit");
        text(ModItems.loreKey("emberwalker_armor", 3), "Boots: lava hardens beneath your steps – walk across it",
                "Stiefel: Lava erstarrt unter deinen Schritten – du gehst darüber");
        // Phasit und die Rüstung der Leerwandler (1.4)
        block(io.github.sirschrolli.kingdomsatwar.registry.PhaseGear.PHASIT_ORE, "Phasit Ore", "Phasiterz");
        block(io.github.sirschrolli.kingdomsatwar.registry.PhaseGear.PHASIT_BLOCK, "Block of Phasit", "Phasitblock");
        item(io.github.sirschrolli.kingdomsatwar.registry.PhaseGear.PHASIT, "Phasit", "Phasit");
        lore("phasit", "It flickers as if it were not quite here",
                "Er flackert, als wäre er nicht ganz da");
        item(io.github.sirschrolli.kingdomsatwar.registry.PhaseGear.VOIDWALKER_HELMET, "Voidwalker Hood", "Kapuze der Leerwandler");
        item(io.github.sirschrolli.kingdomsatwar.registry.PhaseGear.VOIDWALKER_CHESTPLATE, "Voidwalker Coat", "Mantel der Leerwandler");
        item(io.github.sirschrolli.kingdomsatwar.registry.PhaseGear.VOIDWALKER_LEGGINGS, "Voidwalker Leggings", "Beinlinge der Leerwandler");
        item(io.github.sirschrolli.kingdomsatwar.registry.PhaseGear.VOIDWALKER_BOOTS, "Voidwalker Boots", "Stiefel der Leerwandler");
        lore("voidwalker_armor", "Each piece: a chance to phase away from a blow",
                "Jeder Teil: mit etwas Glück phast du einem Hieb davon");
        text(ModItems.loreKey("voidwalker_armor", 2), "Boots: ender pearls no longer hurt",
                "Stiefel: Enderperlen tun nicht mehr weh");
        text(ModItems.loreKey("voidwalker_armor", 3), "Full set: void walkers stay calm when you look at them",
                "Ganzer Satz: Leerwandler bleiben ruhig, wenn du sie ansiehst");
        block(io.github.sirschrolli.kingdomsatwar.registry.FaeBlocks.FAE_SOIL, "Fae Soil", "Feenerde");
        block(io.github.sirschrolli.kingdomsatwar.registry.FaeBlocks.FAE_GRASS, "Fae Grass Block", "Feengrasblock");
        block(io.github.sirschrolli.kingdomsatwar.registry.FaeBlocks.FAE_MOSS, "Fae Moss", "Feenmoos");
        block(io.github.sirschrolli.kingdomsatwar.registry.FaeBlocks.GLIMMERWOOD_LOG, "Glimmerwood Log", "Glimmerholzstamm");
        block(io.github.sirschrolli.kingdomsatwar.registry.FaeBlocks.GLIMMERWOOD_WOOD, "Glimmerwood", "Glimmerholz");
        block(io.github.sirschrolli.kingdomsatwar.registry.FaeBlocks.STRIPPED_GLIMMERWOOD_LOG, "Stripped Glimmerwood Log",
                "Entrindeter Glimmerholzstamm");
        block(io.github.sirschrolli.kingdomsatwar.registry.FaeBlocks.STRIPPED_GLIMMERWOOD_WOOD, "Stripped Glimmerwood", "Entrindetes Glimmerholz");
        roof("glimmerwood_planks", "glimmerwood_plank", "Glimmerwood Planks", "Glimmerholzbretter", "Glimmerwood", "Glimmerholz");
        block(io.github.sirschrolli.kingdomsatwar.registry.FaeBlocks.GLIMMERWOOD_DOOR, "Glimmerwood Door", "Glimmerholztür");
        block(io.github.sirschrolli.kingdomsatwar.registry.FaeBlocks.GLIMMERWOOD_TRAPDOOR, "Glimmerwood Trapdoor", "Glimmerholzfalltür");
        block(io.github.sirschrolli.kingdomsatwar.registry.FaeBlocks.GLIMMERWOOD_FENCE, "Glimmerwood Fence", "Glimmerholzzaun");
        block(io.github.sirschrolli.kingdomsatwar.registry.FaeBlocks.GLIMMERWOOD_FENCE_GATE, "Glimmerwood Fence Gate", "Glimmerholzzauntor");
        block(io.github.sirschrolli.kingdomsatwar.registry.FaeBlocks.ROSE_GLIMMER_LEAVES, "Rose Glimmer Leaves", "Rosenglimmerlaub");
        block(io.github.sirschrolli.kingdomsatwar.registry.FaeBlocks.AZURE_GLIMMER_LEAVES, "Azure Glimmer Leaves", "Türkisglimmerlaub");
        block(io.github.sirschrolli.kingdomsatwar.registry.FaeBlocks.GLIMMERWOOD_SAPLING, "Glimmerwood Sapling", "Glimmerholzsetzling");
        block(io.github.sirschrolli.kingdomsatwar.registry.FaeBlocks.POTTED_GLIMMERWOOD_SAPLING, "Potted Glimmerwood Sapling",
                "Eingetopfter Glimmerholzsetzling");
        seaWoods();
        block(io.github.sirschrolli.kingdomsatwar.registry.FaeBlocks.FAE_MUSHROOM_STEM, "Giant Mushroom Stem", "Riesenpilzstiel");
        block(io.github.sirschrolli.kingdomsatwar.registry.FaeBlocks.ROSE_MUSHROOM_CAP, "Rose Mushroom Cap", "Rosenpilzhut");
        block(io.github.sirschrolli.kingdomsatwar.registry.FaeBlocks.AZURE_MUSHROOM_CAP, "Sky Mushroom Cap", "Himmelspilzhut");
        block(io.github.sirschrolli.kingdomsatwar.registry.FaeBlocks.VIOLET_MUSHROOM_CAP, "Violet Mushroom Cap", "Veilchenpilzhut");
        block(io.github.sirschrolli.kingdomsatwar.registry.FaeBlocks.FAE_SPROUTS, "Fae Sprouts", "Feenhalme");
        block(io.github.sirschrolli.kingdomsatwar.registry.FaeBlocks.MOONBELL, "Moonbell", "Mondglocke");
        block(io.github.sirschrolli.kingdomsatwar.registry.FaeBlocks.FAE_ROSE, "Fae Rose", "Feenrose");
        block(io.github.sirschrolli.kingdomsatwar.registry.FaeBlocks.STARLACE, "Starlace", "Sternspitze");
        block(io.github.sirschrolli.kingdomsatwar.registry.FaeBlocks.LANTERN_MUSHROOM, "Lantern Mushrooms", "Laternenpilze");
        block(io.github.sirschrolli.kingdomsatwar.registry.FaeBlocks.GLOW_VINES, "Glow Vines", "Leuchtranke");
        block(io.github.sirschrolli.kingdomsatwar.registry.FaeBlocks.GLOW_VINES_PLANT, "Glow Vines", "Leuchtranke");
        block(io.github.sirschrolli.kingdomsatwar.registry.FaeBlocks.MOON_LILY, "Moon Lily", "Mondlilie");
        block(io.github.sirschrolli.kingdomsatwar.registry.FaeBlocks.THORNBRIAR, "Thornbriar", "Dornenranke");
        block(io.github.sirschrolli.kingdomsatwar.registry.FaeBlocks.THORN_HEDGE, "Thorn Hedge", "Dornenhecke");
        block(io.github.sirschrolli.kingdomsatwar.registry.FaeBlocks.FAE_PORTAL, "Fae Portal", "Feenportal");
        item(io.github.sirschrolli.kingdomsatwar.registry.FaeBlocks.FAE_DUST, "Fae Dust", "Feenstaub");
        text(ModItems.loreKey("fae_dust", 1), "Use on a frame of Moonstone Bricks (at least 4 × 5)",
                "Auf einen Rahmen aus Mondsteinziegeln anwenden (mindestens 4 × 5)");
        text(ModItems.loreKey("fae_dust", 2), "to open a gate into the Fae Realm", "öffnet ein Tor ins Feenreich");
        text("item.kingdomsatwar.fae_dust.no_frame", "The dust glitters, but finds no frame of Moonstone Bricks.",
                "Der Staub glitzert, doch er findet keinen Rahmen aus Mondsteinziegeln.");
        creature(ModEntities.FAIRY, io.github.sirschrolli.kingdomsatwar.registry.FaeBlocks.FAIRY_SPAWN_EGG, "Fairy", "Fee", "Feen");
        creature(ModEntities.FAE_WISP, io.github.sirschrolli.kingdomsatwar.registry.FaeBlocks.FAE_WISP_SPAWN_EGG, "Fae Wisp", "Feen-Irrlicht",
                "Feen-Irrlicht");
        creature(ModEntities.MUSHROOMLING, io.github.sirschrolli.kingdomsatwar.registry.FaeBlocks.MUSHROOMLING_SPAWN_EGG, "Mushroomling",
                "Pilzling", "Pilzling");
        creature(ModEntities.THORN_WIGHT, io.github.sirschrolli.kingdomsatwar.registry.FaeBlocks.THORN_WIGHT_SPAWN_EGG, "Thorn Wight",
                "Dornwicht", "Dornwicht");
        add(ModEntities.THORN.get(), pick("Thorn", "Dorn"));
        subtitle("entity.fairy.ambient", "Fairy chimes", "Fee klingt");
        subtitle("entity.fairy.hurt", "Fairy hurts", "Fee wird verletzt");
        subtitle("entity.fairy.death", "Fairy fades", "Fee verblasst");
        subtitle("entity.fairy.gift", "Fairy rejoices", "Fee freut sich");
        subtitle("entity.mushroomling.ambient", "Mushroomling squeaks", "Pilzling quiekt");
        subtitle("entity.mushroomling.hurt", "Mushroomling hurts", "Pilzling wird verletzt");
        subtitle("entity.mushroomling.death", "Mushroomling pops", "Pilzling platzt");
        subtitle("entity.mushroomling.puff", "Spores burst", "Sporen stieben");
        subtitle("entity.thorn_wight.volley", "Thorns fly", "Dornen fliegen");
        subtitle("block.fae_portal.ambient", "Fae Portal shimmers", "Feenportal schimmert");
        subtitle("block.fae_portal.light", "Fae Portal opens", "Feenportal öffnet sich");
        subtitle("block.fae_portal.travel", "Fae Portal whirls", "Feenportal wirbelt");
        // Noctua, die Mottenkönigin, und der Mondhain
        creature(ModEntities.MOTH_QUEEN, io.github.sirschrolli.kingdomsatwar.registry.FaeBlocks.MOTH_QUEEN_SPAWN_EGG,
                "Noctua, the Moth Queen", "Noctua, die Mottenkönigin", "Mottenkönigin");
        add(ModEntities.MOON_MOTH.get(), pick("Moon Moth", "Mondfalter"));
        text("boss.kingdomsatwar.moth", "Noctua, the Moth Queen", "Noctua, die Mottenkönigin");
        text("boss.kingdomsatwar.moth.named", "%s", "%s");
        text("boss.kingdomsatwar.moth.title", "Mistress of the Moon Grove", "Herrin des Mondhains");
        text("message.kingdomsatwar.moth_queen.lullaby", "Don't look at her while she sings!", "Sieh nicht hin, wenn sie singt!");
        text("message.kingdomsatwar.moth_queen.eclipse", "The moon goes dark … watch the glowing rings!",
                "Der Mond verfinstert sich … achte auf die leuchtenden Kreise!");
        text("message.kingdomsatwar.moth_queen.cocoon", "You are spun into a silk cocoon!", "Du bist in einen Seidenkokon gesponnen!");
        block(io.github.sirschrolli.kingdomsatwar.registry.FaeBlocks.MOTH_SILK, "Moth Silk", "Mottenseide");
        block(io.github.sirschrolli.kingdomsatwar.registry.FaeBlocks.SILK_STRANDS, "Silk Strands", "Seidenfäden");
        block(io.github.sirschrolli.kingdomsatwar.registry.FaeBlocks.MOTH_COCOON, "Moth Queen's Cocoon", "Kokon der Mottenkönigin");
        lore("moth_cocoon", "Sprinkle Fae Dust on it to wake the Moth Queen", "Mit Feenstaub bestreuen, um die Mottenkönigin zu wecken");
        text("block.kingdomsatwar.moth_cocoon.hint", "Something pulses softly inside … perhaps a pinch of Fae Dust would wake it.",
                "Darin pulsiert etwas sanft … vielleicht weckt es eine Prise Feenstaub.");
        text("block.kingdomsatwar.moth_cocoon.peaceful", "The cocoon sleeps soundly (the world is set to Peaceful).",
                "Der Kokon schläft tief und fest (die Welt steht auf „Friedlich“).");
        item(io.github.sirschrolli.kingdomsatwar.registry.FaeBlocks.FAE_ELYTRA, "Fae Elytra", "Feen-Elytren");
        lore("fae_elytra", "The wings of Noctua, the Moth Queen", "Die Flügel Noctuas, der Mottenkönigin");
        text(ModItems.loreKey("fae_elytra", 2), "While gliding, jump: a mighty wingbeat", "Im Gleitflug springen: ein kräftiger Flügelschlag");
        text(ModItems.loreKey("fae_elytra", 3), "No damage when crashing into walls", "Kein Schaden beim Aufprall an Wänden");
        text(ModItems.loreKey("fae_elytra", 4), "Repair with Fae Dust", "Mit Feenstaub reparieren");
        for (String[] e : new String[][] {{"ambient", "Moth Queen hums", "Mottenkönigin summt"}, {"hurt", "Moth Queen hurts", "Mottenkönigin nimmt Schaden"},
                {"death", "Moth Queen fades", "Mottenkönigin verglimmt"}, {"screech", "Moth Queen shrieks", "Mottenkönigin kreischt"},
                {"awaken", "The cocoon tears open", "Der Kokon reißt auf"}, {"gust", "Mighty wingbeats", "Mächtige Flügelschläge"},
                {"scales", "Glittering scales trickle", "Glitzernde Schuppen rieseln"}, {"beam_charge", "Moonlight gathers", "Mondlicht sammelt sich"},
                {"beam", "Moonbeam strikes", "Mondstrahl schlägt ein"}, {"lullaby", "Moth Queen sings", "Mottenkönigin singt"},
                {"dive", "Moth Queen dives", "Mottenkönigin stößt herab"}}) {
            subtitle("entity.moth_queen." + e[0], e[1], e[2]);
        }
        subtitle("item.fae_elytra.flap", "Wings beat", "Flügel schlagen");
        // der Steinhüter und der alte Mondsteinbruch (0.96)
        creature(ModEntities.STONE_KEEPER, io.github.sirschrolli.kingdomsatwar.registry.FaeBlocks.STONE_KEEPER_SPAWN_EGG,
                "Stone Keeper", "Steinhüter", "Steinhüter");
        add(ModEntities.KEEPER_BOULDER.get(), pick("Keeper's Boulder", "Felsbrocken des Steinhüters"));
        text("boss.kingdomsatwar.keeper", "The Stone Keeper", "Der Steinhüter");
        text("boss.kingdomsatwar.keeper.named", "%s", "%s");
        text("boss.kingdomsatwar.keeper.title", "Keeper of the Old Moonstone Quarry", "Hüter des alten Mondsteinbruchs");
        item(io.github.sirschrolli.kingdomsatwar.registry.FaeGear.KEEPER_MAUL, "Keeper's Maul", "Hüterhammer");
        lore("keeper_maul", "The weapon of the Stone Keeper – heavy, slow, every blow lands", "Die Waffe des Steinhüters – schwer, langsam, jeder Schlag sitzt");
        text(ModItems.loreKey("keeper_maul", 2), "Use on solid ground: a trail of moonstone crystals bursts forth",
                "Benutzen auf festem Boden: eine Spur aus Mondsteinkristallen bricht hervor");
        text(ModItems.loreKey("keeper_maul", 3), "Repair with Moonstone", "Mit Mondstein reparieren");
        block(io.github.sirschrolli.kingdomsatwar.registry.FaeBlocks.KEEPER_STONE, "Keeper Stone", "Hüterstein");
        lore("keeper_stone", "Carved by the old people: an eye above a crescent", "Vom alten Volk gemeißelt: ein Auge über einer Mondsichel");
        text(ModItems.loreKey("keeper_stone", 2), "Once its glow is gone, a Moonstone calls a new Keeper",
                "Ist es erloschen, ruft ein Mondstein einen neuen Hüter");
        text("block.kingdomsatwar.keeper_stone.hint", "Something vast sleeps here … step down to the bottom of the quarry, if you dare.",
                "Hier schläft etwas Gewaltiges … steig hinab zum Grund des Bruchs, wenn du dich traust.");
        text("block.kingdomsatwar.keeper_stone.awake", "The Keeper is awake!", "Der Hüter ist erwacht!");
        text("block.kingdomsatwar.keeper_stone.defeated", "The stone has gone dark. A Moonstone on it would call a new Keeper.",
                "Der Stein ist erloschen. Ein Mondstein darauf würde einen neuen Hüter rufen.");
        text("block.kingdomsatwar.keeper_stone.warning", "Stone grinds deep below … something guards this quarry.",
                "Tief unten knirscht Stein … etwas bewacht diesen Bruch.");
        text("block.kingdomsatwar.keeper_stone.peaceful", "The Keeper sleeps soundly (the world is set to Peaceful).",
                "Der Hüter schläft tief und fest (die Welt steht auf „Friedlich“).");
        for (String[] e : new String[][] {{"ambient", "Stone Keeper rumbles", "Steinhüter grollt"},
                {"hurt", "Stone Keeper cracks", "Steinhüter splittert"}, {"death", "Stone Keeper crumbles", "Steinhüter zerfällt"},
                {"step", "Heavy stone footsteps", "Schwere Schritte aus Stein"}, {"awaken", "Stone grinds awake", "Fels erwacht knirschend"},
                {"roar", "Stone Keeper roars", "Steinhüter brüllt"}, {"smash", "Fists strike the ground", "Fäuste schlagen auf"},
                {"stomp", "The ground quakes", "Der Boden bebt"}, {"dig", "Rock tears loose", "Fels reißt los"},
                {"throw", "Boulder hurled", "Felsbrocken fliegt"}, {"boulder", "Boulder shatters", "Felsbrocken zerschellt"},
                {"crystal", "Crystals burst from the ground", "Kristalle brechen hervor"},
                {"beam_charge", "Moonlight gathers in the stone", "Mondlicht sammelt sich im Stein"},
                {"beam", "Moonbeam blazes", "Mondstrahl gleißt"}, {"armor", "Crystal armor grows", "Kristallpanzer wächst"},
                {"shatter", "Crystal armor shatters", "Kristallpanzer zerspringt"}}) {
            subtitle("entity.stone_keeper." + e[0], e[1], e[2]);
        }
        // die Meerhexe und die Hexengrotte (0.97)
        creature(ModEntities.SEA_WITCH, io.github.sirschrolli.kingdomsatwar.registry.SeaWitch.SEA_WITCH_SPAWN_EGG, "Sea Witch", "Meerhexe", "Meerhexen");
        add(ModEntities.INK_BOLT.get(), pick("Witch's Ink", "Hexentinte"));
        text("boss.kingdomsatwar.sea_witch", "The Sea Witch", "Die Meerhexe");
        text("boss.kingdomsatwar.sea_witch.named", "%s", "%s");
        text("boss.kingdomsatwar.sea_witch.title", "Outcast of the Deep, Mistress of the Grotto", "Verstoßene der Tiefe, Herrin der Grotte");
        item(io.github.sirschrolli.kingdomsatwar.registry.SeaWitch.WITCH_STAFF, "Sea Witch's Staff", "Hexenstab");
        lore("sea_witch_staff", "The staff of the Sea Witch – driftwood, black coral and a glowing orb",
                "Der Stab der Meerhexe – Treibholz, schwarze Koralle und eine leuchtende Kugel");
        text(ModItems.loreKey("sea_witch_staff", 2), "Use: three bolts of witch's ink that follow your target (faster under water)",
                "Benutzen: drei Kugeln aus Hexentinte, die deinem Ziel folgen (unter Wasser schneller)");
        text(ModItems.loreKey("sea_witch_staff", 3), "Repair with Aquamarine", "Mit Aquamarin reparieren");
        block(io.github.sirschrolli.kingdomsatwar.registry.SeaWitch.WITCH_CAULDRON, "Witch's Cauldron", "Hexenkessel");
        text("block.kingdomsatwar.witch_cauldron.hint", "A brew bubbles and glows … and someone hovers above it in a trance. Come closer, if you dare.",
                "Ein Trank brodelt und leuchtet … und über ihm schwebt jemand in Trance. Komm näher, wenn du dich traust.");
        text("block.kingdomsatwar.witch_cauldron.awake", "The Sea Witch is awake!", "Die Meerhexe ist erwacht!");
        text("block.kingdomsatwar.witch_cauldron.defeated", "The cauldron has gone cold. A Pearl dropped into it would call the Witch back.",
                "Der Kessel ist erkaltet. Eine Perle hineingeworfen würde die Hexe zurückrufen.");
        text("block.kingdomsatwar.witch_cauldron.warning", "A whisper drifts through the water … something brews down here.",
                "Ein Flüstern treibt durchs Wasser … hier unten braut jemand etwas.");
        text("block.kingdomsatwar.witch_cauldron.peaceful", "The brew lies still (the world is set to Peaceful).",
                "Der Trank liegt still (die Welt steht auf „Friedlich“).");
        text("message.kingdomsatwar.sea_witch.song", "The Sea Witch sings – don't look at her!", "Die Meerhexe singt – sieh sie nicht an!");
        text("message.kingdomsatwar.sea_witch.bubble", "A bubble closes around you – the air is pressed out!",
                "Eine Blase schließt dich ein – sie presst dir die Luft ab!");
        text("message.kingdomsatwar.sea_witch.curse", "The Curse of the Deep lies upon you.", "Der Fluch der Tiefe liegt auf dir.");
        text("message.kingdomsatwar.sea_witch.fury", "The Sea Witch screams in fury!", "Die Meerhexe schreit vor Zorn!");
        for (String[] e : new String[][] {{"ambient", "Sea Witch whispers", "Meerhexe flüstert"},
                {"hurt", "Sea Witch hisses", "Meerhexe faucht"}, {"death", "Sea Witch dissolves", "Meerhexe zerfließt"},
                {"awaken", "Sea Witch awakens", "Meerhexe erwacht"}, {"laugh", "Sea Witch cackles", "Meerhexe lacht"},
                {"song", "Sea Witch sings", "Meerhexe singt"}, {"cast", "Sea Witch casts a spell", "Meerhexe zaubert"},
                {"ink", "Witch's ink flies", "Hexentinte fliegt"}, {"ink_hit", "Ink bursts", "Tinte zerplatzt"},
                {"whirlpool", "Water whirls", "Wasser strudelt"}, {"bubble", "Bubble closes", "Blase schließt sich"},
                {"summon", "Sea Witch calls sharks", "Meerhexe ruft Haie"}, {"coral", "Coral bursts from the seabed", "Korallen brechen hervor"},
                {"curse", "Curse of the Deep", "Fluch der Tiefe"}, {"tail", "Tail lashes", "Schwanz peitscht"},
                {"fury", "Sea Witch screams", "Meerhexe schreit"}}) {
            subtitle("entity.sea_witch." + e[0], e[1], e[2]);
        }
        // der Seelenhirte und der Seelenpferch (0.98)
        creature(ModEntities.SOUL_SHEPHERD, io.github.sirschrolli.kingdomsatwar.registry.SoulShepherd.SOUL_SHEPHERD_SPAWN_EGG, "Soul Shepherd",
                "Seelenhirte", "Seelenhirten");
        add(ModEntities.SOUL_WISP.get(), pick("Will-o'-the-Wisp", "Irrlicht"));
        text("boss.kingdomsatwar.soul_shepherd", "The Soul Shepherd", "Der Seelenhirte");
        text("boss.kingdomsatwar.soul_shepherd.named", "%s", "%s");
        text("boss.kingdomsatwar.soul_shepherd.title", "Keeper of the Lost, Warden of the Soul Pen", "Hüter der Verlorenen, Wächter des Seelenpferchs");
        item(io.github.sirschrolli.kingdomsatwar.registry.SoulShepherd.CROOK, "Shepherd's Crook", "Hirtenstab");
        lore("shepherd_crook", "The crook of the Soul Shepherd – swamp oak, an iron hook and a lantern of soul fire",
                "Der Stab des Seelenhirten – Sumpfeiche, ein eiserner Haken und eine Laterne voll Seelenfeuer");
        text(ModItems.loreKey("shepherd_crook", 2), "Use: a chain of soul light hooks your target and drags it to you",
                "Benutzen: eine Kette aus Seelenlicht hakt dein Ziel und reißt es zu dir");
        text(ModItems.loreKey("shepherd_crook", 3), "Repair with Dusk Quartz", "Mit Dämmerquarz reparieren");
        block(io.github.sirschrolli.kingdomsatwar.registry.SoulShepherd.SOUL_BELL, "Soul Bell", "Seelenglocke");
        text("block.kingdomsatwar.soul_bell.hint", "Soul fire glimmers inside the bell … and beneath it someone stands in silent prayer. Come closer, if you dare.",
                "In der Glocke glimmt Seelenfeuer … und unter ihr steht jemand in stiller Andacht. Komm näher, wenn du dich traust.");
        text("block.kingdomsatwar.soul_bell.awake", "The Soul Shepherd is awake!", "Der Seelenhirte ist erwacht!");
        text("block.kingdomsatwar.soul_bell.defeated", "The bell has fallen silent. A Soul Crystal held to it would call the Shepherd back.",
                "Die Glocke ist verstummt. Ein Seelenkristall an ihr würde den Hirten zurückrufen.");
        text("block.kingdomsatwar.soul_bell.warning", "A bell tolls somewhere in the fog … something is watching over this swamp.",
                "Irgendwo im Nebel läutet eine Glocke … jemand wacht über diesen Sumpf.");
        text("block.kingdomsatwar.soul_bell.peaceful", "The bell hangs still (the world is set to Peaceful).",
                "Die Glocke hängt still (die Welt steht auf „Friedlich“).");
        text("message.kingdomsatwar.soul_shepherd.fury", "The Soul Shepherd rings his bell in wrath!", "Der Seelenhirte läutet im Zorn!");
        for (String[] e : new String[][] {{"ambient", "Soul Shepherd breathes", "Seelenhirte atmet"},
                {"hurt", "Soul Shepherd groans", "Seelenhirte stöhnt"}, {"death", "Soul Shepherd fades", "Seelenhirte vergeht"},
                {"step", "Heavy footsteps in the mire", "Schwere Schritte im Morast"}, {"awaken", "Soul Shepherd awakens", "Seelenhirte erwacht"},
                {"bell", "Soul bell tolls", "Seelenglocke läutet"}, {"hook", "Chain rattles", "Kette klirrt"},
                {"lantern", "Soul fire roars", "Seelenfeuer faucht"}, {"wisps", "Will-o'-the-wisps rise", "Irrlichter steigen auf"},
                {"wisp_hit", "Will-o'-the-wisp bursts", "Irrlicht zerstiebt"}, {"fog", "Soul Shepherd dissolves into fog", "Seelenhirte zerfließt zu Nebel"},
                {"pen", "Ring of soul fire flares", "Ring aus Seelenfeuer lodert"}, {"crash", "Crook strikes the ground", "Stab schlägt in den Grund"},
                {"storm", "Souls whirl", "Seelen wirbeln"}, {"fury", "Soul Shepherd screams", "Seelenhirte schreit"}}) {
            subtitle("entity.soul_shepherd." + e[0], e[1], e[2]);
        }
        // die Sternenweberin und das Sternennetz (0.99)
        creature(ModEntities.STAR_WEAVER, io.github.sirschrolli.kingdomsatwar.registry.StarWeaver.STAR_WEAVER_SPAWN_EGG, "Star Weaver", "Sternenweberin", "Sternenweberin");
        add(ModEntities.STAR_SPIDERLING.get(), pick("Star Spiderling", "Sternspinnling"));
        add(ModEntities.STAR_WEB_SHOT.get(), pick("Ball of Star Web", "Netzballen"));
        add(ModEntities.STAR_ARROW.get(), pick("Star Arrow", "Sternenpfeil"));
        text("boss.kingdomsatwar.star_weaver", "The Star Weaver", "Die Sternenweberin");
        text("boss.kingdomsatwar.star_weaver.named", "%s", "%s");
        text("boss.kingdomsatwar.star_weaver.title", "Spinner of the Void, Keeper of the Fallen Stars",
                "Spinnerin der Leere, Hüterin der gefallenen Sterne");
        block(io.github.sirschrolli.kingdomsatwar.registry.StarWeaver.STAR_SILK, "Star Silk", "Sternenseide");
        block(io.github.sirschrolli.kingdomsatwar.registry.StarWeaver.STAR_WEAVE, "Star Weave", "Sternengewebe");
        block(io.github.sirschrolli.kingdomsatwar.registry.StarWeaver.STAR_WEB, "Star Cobweb", "Sternenspinnwebe");
        block(io.github.sirschrolli.kingdomsatwar.registry.StarWeaver.STAR_COCOON, "Star Cocoon", "Sternenkokon");
        block(io.github.sirschrolli.kingdomsatwar.registry.StarWeaver.WEAVER_NEST, "Weaver's Clutch", "Sternengelege");
        item(io.github.sirschrolli.kingdomsatwar.registry.StarWeaver.STAR_BOW, "Star Bow", "Sternenbogen");
        lore("star_bow", "The bow of the Star Weaver – night-blue chitin, strung with star silk",
                "Der Bogen der Sternenweberin – nachtblaues Chitin, bespannt mit Sternenseide");
        text(ModItems.loreKey("star_bow", 2), "Arrows fly weightless; drawn while sneaking, a web arrow that binds its target",
                "Pfeile fliegen schwerelos; im Schleichen gespannt ein Netzpfeil, der sein Ziel fesselt");
        text(ModItems.loreKey("star_bow", 3), "Repair with Sky Iron", "Mit Himmelseisen reparieren");
        text("block.kingdomsatwar.weaver_nest.hint", "Seven eggs glimmer with starlight … and high above, something hangs motionless in the web. Come closer, if you dare.",
                "Sieben Eier glimmen im Sternenlicht … und hoch darüber hängt etwas reglos im Netz. Komm näher, wenn du dich traust.");
        text("block.kingdomsatwar.weaver_nest.awake", "The Star Weaver is awake!", "Die Sternenweberin ist erwacht!");
        text("block.kingdomsatwar.weaver_nest.defeated", "The eggs have gone cold. A Sky Iron Ingot laid in the clutch would wake the Weaver again.",
                "Die Eier sind erkaltet. Ein Himmelseisenbarren im Gelege würde die Weberin wieder wecken.");
        text("block.kingdomsatwar.weaver_nest.warning", "The threads tremble … something huge is watching over this web.",
                "Die Fäden zittern … etwas Riesiges wacht über dieses Netz.");
        text("block.kingdomsatwar.weaver_nest.peaceful", "The web lies still (the world is set to Peaceful).",
                "Das Netz liegt still (die Welt steht auf „Friedlich“).");
        text("message.kingdomsatwar.star_weaver.fury", "The Star Weaver shrieks – the stars tremble!", "Die Sternenweberin kreischt – die Sterne erzittern!");
        for (String[] e : new String[][] {{"ambient", "Star Weaver clicks", "Sternenweberin klickt"},
                {"hurt", "Star Weaver hisses", "Sternenweberin zischt"}, {"death", "Star Weaver fades", "Sternenweberin erlischt"},
                {"step", "Legs tap on threads", "Beine tippen auf Fäden"}, {"awaken", "Star Weaver awakens", "Sternenweberin erwacht"},
                {"thread", "Thread whirs", "Faden sirrt"}, {"web", "Web ball splats", "Netzballen klatscht"},
                {"constellation", "Stars light up", "Sterne leuchten auf"}, {"starfall", "Stars fall", "Sterne fallen"},
                {"cocoon", "Threads wrap tight", "Fäden umwickeln"}, {"brood", "Brood hatches", "Brut schlüpft"},
                {"drop", "Star Weaver drops", "Sternenweberin stürzt herab"}, {"bite", "Star Weaver bites", "Sternenweberin beißt"},
                {"spindle", "Threads whirl", "Fäden wirbeln"}, {"fury", "Star Weaver shrieks", "Sternenweberin kreischt"},
                {"spiderling", "Star spiderling chirps", "Sternspinnling zirpt"}}) {
            subtitle("entity.star_weaver." + e[0], e[1], e[2]);
        }
        // der Uhrturm und der Stundenwächter (1.1)
        creature(ModEntities.HOUR_WARDEN, io.github.sirschrolli.kingdomsatwar.registry.ClockTower.HOUR_WARDEN_SPAWN_EGG, "Hour Warden", "Stundenwächter",
                "Stundenwächter");
        add(ModEntities.CLOCK_GEAR.get(), pick("Clockwork Gear", "Zahnrad"));
        add(ModEntities.CLOCK_PENDULUM.get(), pick("Great Pendulum", "Großes Pendel"));
        text("boss.kingdomsatwar.hour_warden", "The Hour Warden", "Der Stundenwächter");
        text("boss.kingdomsatwar.hour_warden.named", "%s", "%s");
        text("boss.kingdomsatwar.hour_warden.title", "Keeper of the Last Minute", "Hüter der letzten Minute");
        set("clock_bronze", "clock_bronze", "Clock Bronze", "Uhrbronze", "Clock Bronze", "Uhrbronze");
        block(io.github.sirschrolli.kingdomsatwar.registry.ClockTower.CLOCKWORK, "Clockwork", "Uhrwerk");
        block(io.github.sirschrolli.kingdomsatwar.registry.ClockTower.CLOCK_FACE, "Clock Face", "Zifferblatt");
        block(io.github.sirschrolli.kingdomsatwar.registry.ClockTower.CLOCK_HEART, "Clockwork Heart", "Uhrwerkherz");
        item(io.github.sirschrolli.kingdomsatwar.registry.ClockTower.MINUTE_HAND, "Minute Hand", "Minutenzeiger");
        lore("minute_hand", "The long hand of the great clock – blued steel around a vein of Chronit",
                "Der lange Zeiger der großen Uhr – gebläuter Stahl um eine Ader aus Chronit");
        text(ModItems.loreKey("minute_hand", 2), "Every twelfth hit strikes the hour; right-click: a Time Leap forward",
                "Jeder zwölfte Treffer schlägt die Stunde; Rechtsklick: ein Zeitsprung nach vorn");
        text(ModItems.loreKey("minute_hand", 3), "Repair with Chronit", "Mit Chronit reparieren");
        text("item.kingdomsatwar.minute_hand.ready", "The hand stands at a minute to the hour …", "Der Zeiger steht eine Minute vor der vollen Stunde …");
        text("block.kingdomsatwar.clock_heart.hint", "Deep inside, a heart of brass is ticking … and above it stands a knight of bronze, frozen at a minute to midnight. Come closer, if you dare.",
                "Tief drinnen tickt ein Herz aus Messing … und darüber steht ein Ritter aus Bronze, erstarrt eine Minute vor Mitternacht. Komm näher, wenn du dich traust.");
        text("block.kingdomsatwar.clock_heart.awake", "The Hour Warden is awake!", "Der Stundenwächter ist erwacht!");
        text("block.kingdomsatwar.clock_heart.defeated", "The heart has stopped. A piece of Chronit laid into it would wind it up – and wake a new Warden.",
                "Das Herz steht still. Ein Chronit darin würde es wieder aufziehen – und einen neuen Wächter wecken.");
        text("block.kingdomsatwar.clock_heart.warning", "Somewhere a clock is ticking that should have stopped long ago …",
                "Irgendwo tickt eine Uhr, die längst stehen sollte …");
        text("block.kingdomsatwar.clock_heart.peaceful", "The heart lies still (the world is set to Peaceful).",
                "Das Herz steht still (die Welt steht auf „Friedlich“).");
        text("message.kingdomsatwar.hour_warden.stop_warning", "The Hour Warden raises his hand – the clock ticks faster … look away!",
                "Der Stundenwächter hebt die Hand – die Uhr tickt schneller … schau weg!");
        text("message.kingdomsatwar.hour_warden.frozen", "Time stands still around you!", "Die Zeit steht um dich still!");
        text("message.kingdomsatwar.hour_warden.rewind", "The Hour Warden kneels and turns his time back – strike him hard!",
                "Der Stundenwächter kniet nieder und dreht seine Zeit zurück – triff ihn hart!");
        text("message.kingdomsatwar.hour_warden.stagger", "His clockwork skips a beat – he staggers!", "Sein Uhrwerk springt aus dem Takt – er taumelt!");
        text("message.kingdomsatwar.hour_warden.fury", "The Twelfth Hour! The great bell begins to toll …", "Die Zwölfte Stunde! Die große Glocke beginnt zu schlagen …");
        for (String[] e : new String[][] {{"ambient", "Clockwork ticks", "Uhrwerk tickt"}, {"hurt", "Bronze clangs", "Bronze klirrt"},
                {"death", "Clockwork winds down", "Uhrwerk läuft aus"}, {"step", "Heavy bronze steps", "Schwere Bronzeschritte"},
                {"awaken", "A clock starts ticking", "Eine Uhr setzt sich in Gang"}, {"sweep", "Blade sweeps", "Klinge fegt"},
                {"strike", "The hour strikes", "Die Stunde schlägt"}, {"gears", "Gears whir", "Zahnräder surren"},
                {"gear_hit", "Gear clangs", "Zahnrad klirrt"}, {"time_stop", "Time stops", "Die Zeit steht still"},
                {"frozen", "Frozen in time", "In der Zeit erstarrt"}, {"pendulum", "Pendulum swings", "Pendel schwingt"},
                {"hourglass", "Sand pours", "Sand rinnt"}, {"hourglass_burst", "Hourglass bursts", "Sanduhr birst"},
                {"blink", "Time Leap", "Zeitsprung"}, {"rewind", "Clockwork turns back", "Uhrwerk läuft rückwärts"},
                {"stagger", "Spring snaps", "Feder springt"}, {"fury", "The Twelfth Hour", "Die Zwölfte Stunde"},
                {"toll", "Great bell tolls", "Große Glocke schlägt"}}) {
            subtitle("entity.hour_warden." + e[0], e[1], e[2]);
        }
        subtitle("item.minute_hand.hour", "The hour strikes", "Die Stunde schlägt");
        subtitle("item.minute_hand.jump", "Time Leap", "Zeitsprung");
        // Malleus, der Glutschmied (0.64)
        creature(ModEntities.GLUTSCHMIED, io.github.sirschrolli.kingdomsatwar.registry.ForgeBlocks.GLUTSCHMIED_SPAWN_EGG,
                "Malleus, the Ember Smith", "Malleus, der Glutschmied", "Glutschmied");
        add(ModEntities.GLUTKLUMPEN.get(), pick("Molten Glob", "Glutklumpen"));
        text("boss.kingdomsatwar.forge", "Malleus, the Ember Smith", "Malleus, der Glutschmied");
        text("boss.kingdomsatwar.forge.title", "Master Smith of the Ash Forge", "Meisterschmied der Aschenesse");
        block(io.github.sirschrolli.kingdomsatwar.registry.ForgeBlocks.GLUT_ANVIL, "Ember Anvil", "Glutamboss");
        lore("glut_anvil", "The anvil of Malleus, atop the Ash Forge", "Der Amboss von Malleus auf dem Dach der Aschenesse");
        text(ModItems.loreKey("glut_anvil", 2), "Gone cold: a Netherite Ingot rekindles the forge – and calls him back",
                "Erkaltet: Ein Netheritbarren facht die Esse neu an – und ruft ihn zurück");
        text("block.kingdomsatwar.glut_anvil.hint", "A spear of glowing metal lies on the anvil … whoever comes close wakes the smith.",
                "Auf dem Amboss liegt ein Speer aus glühendem Metall … wer näher kommt, weckt den Schmied.");
        text("block.kingdomsatwar.glut_anvil.awake", "The anvil is empty – the smith has taken up his spear.",
                "Der Amboss ist leer – der Schmied hat seinen Speer genommen.");
        text("block.kingdomsatwar.glut_anvil.cold", "The anvil has gone cold. A Netherite Ingot might rekindle the forge.",
                "Der Amboss ist erkaltet. Ein Netheritbarren könnte die Esse neu entfachen.");
        text("block.kingdomsatwar.glut_anvil.warning", "Hammer blows ring out from the roof of the Ash Forge …",
                "Vom Dach der Aschenesse hallen Hammerschläge …");
        text("block.kingdomsatwar.glut_anvil.peaceful", "The forge stays cold (the world is set to Peaceful).",
                "Die Esse bleibt kalt (die Welt steht auf „Friedlich“).");
        // Ignara, die Flammenpriesterin, und das Flammenheiligtum (1.2)
        creature(ModEntities.IGNARA, io.github.sirschrolli.kingdomsatwar.registry.FlameSanctuary.IGNARA_SPAWN_EGG,
                "Ignara, the Flame Priestess", "Ignara, die Flammenpriesterin", "Flammenpriesterin");
        add(ModEntities.OPAL_SHARD.get(), pick("Opal Shard", "Opalsplitter"));
        text("boss.kingdomsatwar.ignara", "Ignara, the Flame Priestess", "Ignara, die Flammenpriesterin");
        text("boss.kingdomsatwar.ignara.title", "Keeper of the Eternal Flame", "Hüterin der ewigen Flamme");
        block(io.github.sirschrolli.kingdomsatwar.registry.FlameSanctuary.FLAME_ALTAR, "Flame Altar", "Flammenaltar");
        lore("flame_altar", "The altar of the eternal flame in the Flame Sanctuary", "Der Altar der ewigen Flamme im Flammenheiligtum");
        text(ModItems.loreKey("flame_altar", 2), "Gone out: a Fire Opal rekindles the flame – and calls her back",
                "Erloschen: Ein Feueropal entzündet die Flamme neu – und ruft sie zurück");
        text("block.kingdomsatwar.flame_altar.hint", "A priestess hovers before the altar, deep in prayer … whoever comes close disturbs her.",
                "Vor dem Altar schwebt eine Priesterin, ins Gebet versunken … wer näher kommt, stört sie.");
        text("block.kingdomsatwar.flame_altar.awake", "The altar blazes – the priestess has risen.",
                "Der Altar lodert – die Priesterin hat sich erhoben.");
        text("block.kingdomsatwar.flame_altar.defeated", "The eternal flame has gone out. A Fire Opal might rekindle it.",
                "Die ewige Flamme ist erloschen. Ein Feueropal könnte sie neu entzünden.");
        text("block.kingdomsatwar.flame_altar.warning", "A solemn chant drifts across the lava …",
                "Ein feierlicher Gesang weht über die Lava …");
        text("block.kingdomsatwar.flame_altar.peaceful", "The flame stays out (the world is set to Peaceful).",
                "Die Flamme bleibt aus (die Welt steht auf „Friedlich“).");
        item(io.github.sirschrolli.kingdomsatwar.registry.FlameSanctuary.FLAME_WHIP, "Flame Whip", "Flammenpeitsche");
        lore("flame_whip", "The whip of Ignara, the Flame Priestess", "Die Peitsche von Ignara, der Flammenpriesterin");
        text(ModItems.loreKey("flame_whip", 2), "Reaches farther than a sword; every hit sets foes ablaze",
                "Reicht weiter als ein Schwert; jeder Treffer setzt in Brand");
        text(ModItems.loreKey("flame_whip", 3), "Use: the lash strikes the first foe up to ten blocks away and pulls it close",
                "Benutzen: Der Peitschenschlag trifft das erste Wesen bis zehn Blöcke weit und reißt es heran");
        for (String[] e : new String[][] {{"ambient", "Flame Priestess hums", "Flammenpriesterin summt"},
                {"pray", "Flame Priestess chants a hymn", "Flammenpriesterin singt einen Hymnus"},
                {"hurt", "Flame Priestess hurts", "Flammenpriesterin nimmt Schaden"},
                {"death", "Flame Priestess fades", "Flammenpriesterin verlischt"},
                {"awaken", "Flame Priestess rises", "Flammenpriesterin erhebt sich"},
                {"call", "Flame Priestess calls the Blazes", "Flammenpriesterin ruft die Lohen"},
                {"lash", "Whip cracks", "Peitsche knallt"}, {"swing", "Whip whirls", "Peitsche schwirrt"},
                {"wave", "Lava surges", "Lava wogt heran"}, {"pillar", "Pillar of flame erupts", "Flammensäule bricht hervor"},
                {"charge", "Flames gather", "Flammen sammeln sich"}, {"shard", "Opal shard bursts", "Opalsplitter zerspringt"},
                {"phoenix", "Phoenix rises", "Phönix steigt auf"}, {"dive", "Phoenix dives", "Phönix stürzt herab"}}) {
            subtitle("entity.ignara." + e[0], e[1], e[2]);
        }
        subtitle("block.flame_altar.ignite", "Flame Altar ignites", "Flammenaltar entzündet sich");
        subtitle("item.flame_whip.crack", "Flame Whip cracks", "Flammenpeitsche knallt");
        // Waffen und Rüstungen aus Monsterteilen (0.70)
        item(io.github.sirschrolli.kingdomsatwar.registry.MonsterGear.WEREWOLF_PELT, "Werewolf Pelt", "Werwolfspelz");
        lore("werewolf_pelt", "Thick, dark fur – still warm", "Dichtes, dunkles Fell – noch warm");
        item(io.github.sirschrolli.kingdomsatwar.registry.MonsterGear.WEREWOLF_CLAW, "Werewolf Claw", "Werwolfsklaue");
        lore("werewolf_claw", "Long, curved and razor-sharp", "Lang, gebogen und messerscharf");
        item(io.github.sirschrolli.kingdomsatwar.registry.MonsterGear.BOG_ROOT, "Bog Root", "Moorwurzel");
        lore("bog_root", "Gnarled, wet and as tough as leather", "Knorrig, nass und zäh wie Leder");
        item(io.github.sirschrolli.kingdomsatwar.registry.MonsterGear.BOG_HEART, "Bog Heart", "Moorherz");
        lore("bog_heart", "A lump of mud and roots that still beats softly", "Ein Klumpen aus Schlamm und Wurzeln, der noch leise schlägt");
        item(io.github.sirschrolli.kingdomsatwar.registry.MonsterGear.EMBER_SCALE, "Ember Scale", "Glutwurmschuppe");
        lore("ember_scale", "Basalt-hard, glowing at the seams", "Hart wie Basalt, an den Rändern glühend");
        item(io.github.sirschrolli.kingdomsatwar.registry.MonsterGear.EMBER_FANG, "Ember Fang", "Glutzahn");
        lore("ember_fang", "The fang of an Ember Wyrm – it never cools", "Der Fangzahn eines Glutwurms – er erkaltet nie");
        item(io.github.sirschrolli.kingdomsatwar.registry.MonsterGear.SOUL_ASH, "Soul Ash", "Seelenasche");
        lore("soul_ash", "Fine grey ash with a faint blue shimmer", "Feine graue Asche mit einem blassblauen Schimmer");
        item(io.github.sirschrolli.kingdomsatwar.registry.MonsterGear.ASH_VEIL, "Ash Veil", "Aschenschleier");
        lore("ash_veil", "A shred of an Ash Wraith's shroud", "Ein Fetzen aus dem Gewand eines Aschengeists");
        item(io.github.sirschrolli.kingdomsatwar.registry.MonsterGear.CLAW_BLADE, "Claw Blade", "Krallenklinge");
        lore("claw_blade", "Three werewolf claws on a fist grip – fast", "Drei Werwolfsklauen an einem Faustgriff – schnell");
        text(ModItems.loreKey("claw_blade", 2), "Stronger at night; every kill sends you into a frenzy",
                "Nachts schärfer; jeder Sieg versetzt in einen Blutrausch");
        item(io.github.sirschrolli.kingdomsatwar.registry.MonsterGear.BOG_CLUB, "Bog Club", "Moorkeule");
        lore("bog_club", "A gnarled root club with glowing toadstools", "Ein knorriger Wurzelknüppel mit leuchtenden Pilzen");
        text(ModItems.loreKey("bog_club", 2), "Roots grasp what it strikes; mends itself in rain and water",
                "Wurzeln packen, was sie trifft; heilt sich in Regen und Wasser");
        item(io.github.sirschrolli.kingdomsatwar.registry.MonsterGear.EMBER_FANG_BLADE, "Ember Fang Blade", "Glutzahn-Klinge");
        lore("ember_fang_blade", "A curved blade forged from an Ember Wyrm's fang", "Eine gebogene Klinge aus dem Fangzahn eines Glutwurms");
        text(ModItems.loreKey("ember_fang_blade", 2), "Sets foes ablaze and strikes burning foes harder",
                "Setzt in Brand und trifft Brennende härter");
        item(io.github.sirschrolli.kingdomsatwar.registry.MonsterGear.SOUL_SCYTHE, "Soul Scythe", "Seelensense");
        lore("soul_scythe", "Its blade is soul fire – it weakens and burns", "Ihre Klinge ist Seelenfeuer – sie schwächt und brennt");
        text(ModItems.loreKey("soul_scythe", 2), "Use: drift like ash up to seven blocks ahead, unseen and unharmed",
                "Benutzen: wie Asche bis zu sieben Blöcke nach vorn verwehen, unsichtbar und unverwundbar");
        item(io.github.sirschrolli.kingdomsatwar.registry.MonsterGear.PELT_HELMET, "Wolf Head Hood", "Wolfskopf-Haube");
        item(io.github.sirschrolli.kingdomsatwar.registry.MonsterGear.PELT_CHESTPLATE, "Pelt Mantle", "Pelzmantel");
        item(io.github.sirschrolli.kingdomsatwar.registry.MonsterGear.PELT_LEGGINGS, "Pelt Leggings", "Pelzbeinlinge");
        item(io.github.sirschrolli.kingdomsatwar.registry.MonsterGear.PELT_BOOTS, "Pelt Boots", "Pelzstiefel");
        lore("werewolf_pelt_armor", "Werewolf pelt – light and warm, each piece makes you faster",
                "Werwolfspelz – leicht und warm, jedes Teil macht schneller");
        text(ModItems.loreKey("werewolf_pelt_armor", 2), "Full set: swift and keen-eyed at night", "Voller Satz: nachts schnell und nachtsichtig");
        item(io.github.sirschrolli.kingdomsatwar.registry.MonsterGear.EMBER_HELMET, "Horned Ember Helm", "Glutwurm-Hörnerhelm");
        item(io.github.sirschrolli.kingdomsatwar.registry.MonsterGear.EMBER_CHESTPLATE, "Ember Scale Cuirass", "Glutwurm-Schuppenpanzer");
        item(io.github.sirschrolli.kingdomsatwar.registry.MonsterGear.EMBER_LEGGINGS, "Ember Scale Greaves", "Glutwurm-Beinschienen");
        item(io.github.sirschrolli.kingdomsatwar.registry.MonsterGear.EMBER_BOOTS, "Ember Scale Boots", "Glutwurm-Stiefel");
        lore("ember_scale_armor", "Ember Wyrm scales – glowing at the seams", "Glutwurmschuppen – an den Nähten glühend");
        text(ModItems.loreKey("ember_scale_armor", 2), "Full set: immune to fire; whoever strikes you catches fire",
                "Voller Satz: feuerfest; wer dich trifft, fängt Feuer");
        item(io.github.sirschrolli.kingdomsatwar.registry.ForgeBlocks.GLUT_SPEAR, "Ember Spear", "Glutspeer");
        lore("glut_spear", "The masterpiece of Malleus, the Ember Smith", "Das Meisterstück von Malleus, dem Glutschmied");
        text(ModItems.loreKey("glut_spear", 2), "In the Nether or while burning: strikes harder; every hit sets foes ablaze",
                "Im Nether oder brennend: trifft härter; jeder Treffer setzt in Brand");
        text(ModItems.loreKey("glut_spear", 3), "In hand, in the Nether: shields you from fire and lava",
                "In der Hand, im Nether: schützt vor Feuer und Lava");
        item(io.github.sirschrolli.kingdomsatwar.registry.ForgeBlocks.MOLTEN_GLOB, "Molten Glob", "Glutklumpen");
        lore("molten_glob", "Molten metal from the Ember Smith's forge – burns like a Blaze Rod",
                "Geschmolzenes Metall aus der Esse des Glutschmieds – brennt wie eine Lohenrute");
        for (String[] e : new String[][] {{"clang", "Hammer rings on the anvil", "Hammer klingt auf dem Amboss"},
                {"roar", "Ember Smith roars", "Glutschmied brüllt"}, {"thrust", "Spear thrusts", "Speer stößt zu"},
                {"slam", "Hammer shakes the roof", "Hammerschlag erschüttert das Dach"}, {"sparks", "Sparks fly", "Funken stieben"},
                {"throw", "Molten metal is hurled", "Glut wird geschleudert"}, {"glob", "Molten glob bursts", "Glutklumpen zerplatzt"},
                {"call", "Ember Smith calls his apprentices", "Glutschmied ruft seine Gesellen"}, {"stoke", "The forge flares up", "Die Esse lodert auf"},
                {"ambient", "Ember Smith breathes", "Glutschmied atmet"}, {"hurt", "Ember Smith hurts", "Glutschmied nimmt Schaden"},
                {"death", "Ember Smith crumbles", "Glutschmied zerfällt"}, {"step", "Heavy footsteps", "Schwere Schritte"}}) {
            subtitle("entity.glutschmied." + e[0], e[1], e[2]);
        }
        // Asteria, die Sternenreiterin (0.65)
        creature(ModEntities.ASTERIA, io.github.sirschrolli.kingdomsatwar.registry.StarBlocks.ASTERIA_SPAWN_EGG,
                "Asteria, the Star Rider", "Asteria, die Sternenreiterin", "Sternenreiterin");
        add(ModEntities.STARFALL.get(), pick("Falling Star", "Fallender Stern"));
        add(ModEntities.COMET_LANCE.get(), pick("Comet Lance", "Kometenlanze"));
        text("boss.kingdomsatwar.star", "Asteria, the Star Rider", "Asteria, die Sternenreiterin");
        text("boss.kingdomsatwar.star.title", "Last Rider of the Star Order", "Letzte Reiterin des Sternenordens");
        text("entity.kingdomsatwar.asteria.supernova", "Asteria gathers the light of the stars – take cover!",
                "Asteria sammelt das Licht der Sterne – geh in Deckung!");
        block(io.github.sirschrolli.kingdomsatwar.registry.StarBlocks.STARSTONE, "Starstone", "Sternstein");
        lore("starstone", "The star crystal of the Star Order, in the court of the Starwatch",
                "Der Sternkristall des Sternenordens im Sternenhof der Sternwarte");
        text(ModItems.loreKey("starstone", 2), "Dimmed: an End Crystal rekindles it – and calls her back",
                "Erloschen: Ein Endkristall entzündet ihn neu – und ruft sie zurück");
        text("block.kingdomsatwar.starstone.hint", "The crystal hums … whoever steps up to it calls the Star Rider down from the sky.",
                "Der Kristall summt … wer an ihn herantritt, ruft die Sternenreiterin vom Himmel.");
        text("block.kingdomsatwar.starstone.awake", "The Starstone blazes – the Star Rider is here.",
                "Der Sternstein lodert – die Sternenreiterin ist da.");
        text("block.kingdomsatwar.starstone.dim", "The Starstone has dimmed. An End Crystal might rekindle it.",
                "Der Sternstein ist erloschen. Ein Endkristall könnte ihn neu entzünden.");
        text("block.kingdomsatwar.starstone.warning", "Above the Starwatch, a star moves against the sky …",
                "Über der Sternwarte zieht ein Stern gegen den Himmel …");
        text("block.kingdomsatwar.starstone.peaceful", "The Starstone stays dark (the world is set to Peaceful).",
                "Der Sternstein bleibt dunkel (die Welt steht auf „Friedlich“).");
        item(io.github.sirschrolli.kingdomsatwar.registry.StarBlocks.STAR_LANCE, "Star Lance", "Sternenlanze");
        lore("star_lance", "The lance of Asteria, the Star Rider", "Die Lanze Asterias, der Sternenreiterin");
        text(ModItems.loreKey("star_lance", 2), "In the End or under the night sky: strikes harder; every third hit calls down a star",
                "Im End oder unter dem Nachthimmel: trifft härter; jeder dritte Treffer ruft einen Stern herab");
        text(ModItems.loreKey("star_lance", 3), "Targets glow; in hand you fall gently like a shooting star",
                "Getroffene leuchten; in der Hand sinkst du sanft wie eine Sternschnuppe");
        item(io.github.sirschrolli.kingdomsatwar.registry.StarBlocks.STAR_WINGS, "Star Wings", "Sternenschwingen");
        lore("star_wings", "The wings of Asteria's armored star ray", "Die Schwingen von Asterias gepanzertem Sternenrochen");
        text(ModItems.loreKey("star_wings", 2), "While gliding, jump: a star thrust like a rocket",
                "Im Gleitflug springen: ein Sternenschub wie eine Rakete");
        text(ModItems.loreKey("star_wings", 3), "Leave a trail of starlight", "Hinterlassen eine Spur aus Sternenlicht");
        text(ModItems.loreKey("star_wings", 4), "Repair with Phantom Membrane or Star Shards", "Mit Phantomhaut oder Sternsplittern reparieren");
        item(io.github.sirschrolli.kingdomsatwar.registry.StarBlocks.STAR_SHARD, "Star Shard", "Sternsplitter");
        lore("star_shard", "A shard of fallen starlight – repairs the Star Lance and Star Wings",
                "Ein Splitter gefallenen Sternenlichts – repariert Sternenlanze und Sternenschwingen");
        for (String[] e : new String[][] {{"cry", "Star Rider calls out", "Sternenreiterin ruft"},
                {"descent", "Starlight descends", "Sternenlicht stößt herab"}, {"charge", "Star Rider charges", "Sternenreiterin stürmt an"},
                {"impact", "Lance strikes", "Lanze trifft"}, {"starfall", "Stars whistle down", "Sterne fallen pfeifend"},
                {"star_impact", "Star shatters", "Stern zerspringt"}, {"constellation", "Constellation chimes", "Sternbild klingt"},
                {"comet_throw", "Lance is hurled", "Lanze wird geschleudert"}, {"comet_impact", "Comet bursts", "Komet zerplatzt"},
                {"wingbeat", "Star ray beats its wings", "Sternenrochen schlägt die Schwingen"},
                {"supernova_charge", "Starlight gathers", "Sternenlicht sammelt sich"}, {"supernova", "Supernova", "Supernova"},
                {"ambient", "Star ray hums", "Sternenrochen summt"}, {"hurt", "Star Rider hurts", "Sternenreiterin nimmt Schaden"},
                {"death", "Star Rider fades into starlight", "Sternenreiterin vergeht in Sternenlicht"},
                {"black_hole", "A black hole opens", "Ein Schwarzes Loch öffnet sich"},
                {"collapse", "Black hole collapses", "Schwarzes Loch stürzt in sich zusammen"},
                {"crescent", "Moonlight crescent hisses", "Mondsichel zischt"}}) {
            subtitle("entity.asteria." + e[0], e[1], e[2]);
        }
        subtitle("item.star_wings.boost", "Star thrust", "Sternenschub");
        // MissMystik, die Feen-Zauberkünstlerin (0.66)
        creature(ModEntities.MISS_MYSTIK, io.github.sirschrolli.kingdomsatwar.registry.MystikBlocks.MISS_MYSTIK_SPAWN_EGG,
                "MissMystik, the Fae Illusionist", "MissMystik, die Feen-Zauberkünstlerin", "MissMystik");
        add(ModEntities.MYSTIK_IMAGE.get(), pick("Mirror Image of MissMystik", "Spiegelbild MissMystiks"));
        add(ModEntities.MAGIC_CARD.get(), pick("Magic Card", "Zauberkarte"));
        add(ModEntities.LIGHT_DOVE.get(), pick("Light Dove", "Lichttaube"));
        add(ModEntities.MAGIC_HAT.get(), pick("MissMystik's Top Hat", "MissMystiks Zylinder"));
        add(ModEntities.MAGIC_SWORD.get(), pick("Sword of Light", "Schwert aus Licht"));
        text("boss.kingdomsatwar.mystic", "MissMystik", "MissMystik");
        text("boss.kingdomsatwar.mystic.title", "The Fae Illusionist – her last performance", "Die Feen-Zauberkünstlerin – ihre letzte Vorstellung");
        text("entity.kingdomsatwar.miss_mystik.encore", "MissMystik: »Encore! Encore!«", "MissMystik: »Zugabe! Zugabe!«");
        text("entity.kingdomsatwar.miss_mystik.pick_a_card", "MissMystik: »Pick a card … any card!«",
                "MissMystik: »Zieh eine Karte … irgendeine!«");
        text("entity.kingdomsatwar.miss_mystik.finale", "MissMystik: »And now … the grand finale!« – take cover!",
                "MissMystik: »Und nun … das große Finale!« – geh in Deckung!");
        block(io.github.sirschrolli.kingdomsatwar.registry.MystikBlocks.STAGE_HAT, "Great Magic Top Hat", "Großer Zauberzylinder");
        lore("stage_hat", "On the Magic Stage of the Fae Realm – MissMystik waits inside",
                "Auf der Zauberbühne des Feenreichs – darin wartet MissMystik");
        text(ModItems.loreKey("stage_hat", 2), "Empty after her defeat: Fae Dust calls her back",
                "Nach ihrer Niederlage leer: Feenstaub ruft sie zurück");
        text("block.kingdomsatwar.stage_hat.hint", "Something rustles inside the top hat … step closer, if you dare.",
                "Im Zylinder raschelt es … tritt näher, wenn du dich traust.");
        text("block.kingdomsatwar.stage_hat.show", "The show is on – MissMystik is performing.",
                "Die Vorstellung läuft – MissMystik tritt auf.");
        text("block.kingdomsatwar.stage_hat.empty", "The top hat is empty. A pinch of Fae Dust might bring her back.",
                "Der Zylinder ist leer. Eine Prise Feenstaub könnte sie zurückholen.");
        text("block.kingdomsatwar.stage_hat.warning", "Music drifts across the glade … somewhere a stage awaits its audience.",
                "Musik weht über die Lichtung … irgendwo wartet eine Bühne auf ihr Publikum.");
        text("block.kingdomsatwar.stage_hat.peaceful", "The top hat stays silent (the world is set to Peaceful).",
                "Der Zylinder bleibt still (die Welt steht auf „Friedlich“).");
        block(io.github.sirschrolli.kingdomsatwar.registry.MystikBlocks.VELVET_CURTAIN, "Velvet Curtain", "Samtvorhang");
        block(io.github.sirschrolli.kingdomsatwar.registry.MystikBlocks.VELVET_CURTAIN_FRINGE, "Fringed Velvet Curtain",
                "Samtvorhang mit Fransen");
        item(io.github.sirschrolli.kingdomsatwar.registry.MystikBlocks.MYSTIC_DECK, "Mystic Deck", "Kartendeck der Mystik");
        lore("mystic_deck", "MissMystik's own magic cards", "MissMystiks eigene Zauberkarten");
        text(ModItems.loreKey("mystic_deck", 2), "Use: throws a fan of three glowing cards",
                "Benutzen: wirft einen Fächer aus drei leuchtenden Karten");
        text(ModItems.loreKey("mystic_deck", 3), "Every fifth throw: an ace up the sleeve – five harder cards",
                "Jeder fünfte Wurf: ein Ass im Ärmel – fünf härtere Karten");
        text("item.kingdomsatwar.mystic_deck.ace", "An ace up the sleeve!", "Ein Ass im Ärmel!");
        item(io.github.sirschrolli.kingdomsatwar.registry.MystikBlocks.MYSTIC_TOP_HAT, "Mystic Top Hat", "Zylinder der Mystik");
        lore("mystic_top_hat", "MissMystik's top hat – worn on the head", "MissMystiks Zylinder – auf dem Kopf zu tragen");
        text(ModItems.loreKey("mystic_top_hat", 2), "Protects like a fine helmet and brings luck",
                "Schützt wie ein guter Helm und bringt Glück");
        text(ModItems.loreKey("mystic_top_hat", 3), "Below a third of your health: vanishing trick (invisible and fast, every 90 s)",
                "Unter einem Drittel Leben: Verschwindetrick (unsichtbar und schnell, alle 90 s)");
        text("item.kingdomsatwar.mystic_top_hat.vanish", "Vanishing trick!", "Verschwindetrick!");
        for (String[] e : new String[][] {{"ambient", "MissMystik hums a tune", "MissMystik summt eine Melodie"},
                {"laugh", "MissMystik giggles", "MissMystik kichert"}, {"hurt", "MissMystik hurts", "MissMystik nimmt Schaden"},
                {"death", "MissMystik takes her final bow", "MissMystik verbeugt sich ein letztes Mal"},
                {"drumroll", "Drumroll", "Trommelwirbel"}, {"tada", "Ta-da!", "Tada!"}, {"applause", "Applause", "Applaus"},
                {"card_throw", "Cards whirl", "Karten wirbeln"}, {"card_hit", "Card strikes", "Karte trifft"},
                {"doves", "Doves flutter up", "Tauben flattern auf"}, {"vanish", "MissMystik vanishes", "MissMystik verschwindet"},
                {"reappear", "MissMystik reappears", "MissMystik taucht auf"}, {"mirrors", "Mirrors chime", "Spiegel klingen"},
                {"hat_throw", "Top hat whirls", "Zylinder wirbelt"}, {"vortex", "Magic vortex", "Zaubersog"},
                {"swords", "Swords appear", "Schwerter erscheinen"}, {"stab", "Swords strike", "Schwerter stoßen zu"},
                {"spotlight", "Spotlights flare", "Scheinwerfer flammen auf"}, {"card_trick", "A card is drawn", "Eine Karte wird gezogen"},
                {"firework", "Fireworks", "Feuerwerk"}}) {
            subtitle("entity.miss_mystik." + e[0], e[1], e[2]);
        }
        subtitle("item.mystic_deck.throw", "Cards fly", "Karten fliegen");
        subtitle("item.mystic_top_hat.vanish", "Vanishing trick", "Verschwindetrick");
        // Anni der Troll (0.67)
        creature(ModEntities.ANNI_TROLL, io.github.sirschrolli.kingdomsatwar.registry.AnniBlocks.ANNI_TROLL_SPAWN_EGG, "Anni the Troll", "Anni der Troll", "Anni");
        add(ModEntities.GLITCH_CLONE.get(), pick("Glitch Clone", "Fehler-Klon"));
        add(ModEntities.PIXEL_CUBE.get(), pick("Pixel", "Pixel"));
        add(ModEntities.MISSING_ZONE.get(), pick("Missing Texture", "Fehlende Textur"));
        add(ModEntities.SCANLINE.get(), pick("Scanline Error", "Zeilenfehler"));
        add(ModEntities.AFTERIMAGE.get(), pick("Afterimage", "Nachbild"));
        text("boss.kingdomsatwar.anni", "Anni the Troll", "Anni der Troll");
        text("boss.kingdomsatwar.anni.named", "%s", "%s");
        text("boss.kingdomsatwar.anni.title", "Error 404: mercy not found", "Fehler 404: Gnade nicht gefunden");
        text("entity.kingdomsatwar.anni_troll.loading", "Loading Anni … ██████░░░░ 60 %", "Lade Anni … ██████░░░░ 60 %");
        text("entity.kingdomsatwar.anni_troll.disconnect", "Anni lost the connection.", "Anni hat die Verbindung verloren.");
        text("entity.kingdomsatwar.anni_troll.rollback", "Rollback! The world jerked you back.", "Rückruck! Die Welt hat dich zurückgerissen.");
        text("entity.kingdomsatwar.anni_troll.crash", "Anni.exe is not responding.", "Anni.exe reagiert nicht.");
        text("entity.kingdomsatwar.anni_troll.restart", "Restarting Anni … – jump when the wave comes!",
                "Anni wird neu gestartet … – spring, wenn die Welle kommt!");
        text("entity.kingdomsatwar.anni_troll.game_over", "GAME OVER – Anni crashed for good.", "GAME OVER – Anni ist endgültig abgestürzt.");
        text("boss.kingdomsatwar.anni2", "Anni the Troll 2.0", "Anni der Troll 2.0");
        text("boss.kingdomsatwar.anni2.named", "%s", "%s");
        text("boss.kingdomsatwar.anni2.title", "Now with even more bugs!", "Jetzt mit noch mehr Fehlern!");
        text("entity.kingdomsatwar.anni_troll.fatal", "Fatal exception – Anni.exe has stopped.", "Schwerer Ausnahmefehler – Anni.exe wurde beendet.");
        text("entity.kingdomsatwar.anni_troll.searching", "Searching for a backup …", "Suche nach einer Sicherung …");
        text("entity.kingdomsatwar.anni_troll.found", "Backup found: anni_final_FINAL_v2.sav", "Sicherung gefunden: anni_final_ENDGUELTIG_v2.sav");
        text("entity.kingdomsatwar.anni_troll.restoring", "Restoring Anni … %s", "Stelle Anni wieder her … %s");
        block(io.github.sirschrolli.kingdomsatwar.registry.AnniBlocks.ERROR_CORE, "Error Core", "Fehlerkern");
        lore("error_core", "The heart of an Error Zone – Anni the Troll is loaded here", "Das Herz einer Fehlerzone – hier wird Anni der Troll geladen");
        text(ModItems.loreKey("error_core", 2), "Crashed after his defeat: a block of redstone restarts it",
                "Nach seiner Niederlage abgestürzt: ein Redstoneblock startet ihn neu");
        text("block.kingdomsatwar.error_core.hint", "The core flickers. Something big is waiting to be loaded …",
                "Der Kern flimmert. Etwas Großes wartet darauf, geladen zu werden …");
        text("block.kingdomsatwar.error_core.running", "Anni is running – the core hums.", "Anni läuft – der Kern summt.");
        text("block.kingdomsatwar.error_core.crashed", "The core has crashed. A block of redstone might restart it.",
                "Der Kern ist abgestürzt. Ein Redstoneblock könnte ihn neu starten.");
        text("block.kingdomsatwar.error_core.warning", "The world flickers … something here is broken.", "Die Welt flimmert … hier ist etwas kaputt.");
        text("block.kingdomsatwar.error_core.peaceful", "The core stays dark (the world is set to Peaceful).",
                "Der Kern bleibt dunkel (die Welt steht auf „Friedlich“).");
        block(io.github.sirschrolli.kingdomsatwar.registry.AnniBlocks.MISSING_BLOCK, "Missing Texture Block", "Fehlerblock");
        block(io.github.sirschrolli.kingdomsatwar.registry.AnniBlocks.STATIC_BLOCK, "Static", "Bildrauschen");
        item(io.github.sirschrolli.kingdomsatwar.registry.AnniBlocks.GLITCH_SHARD, "Glitch Shard", "Fehlersplitter");
        lore("glitch_shard", "A splinter of a broken world – repairs the Bannhammer and the Glitch Wings",
                "Ein Splitter kaputter Welt – repariert Bannhammer und Fehlerflügel");
        item(io.github.sirschrolli.kingdomsatwar.registry.AnniBlocks.BANNHAMMER, "Bannhammer", "Bannhammer");
        lore("bannhammer", "Anni's hammer – half of its head is missing", "Annis Hammer – die Hälfte seines Kopfes fehlt");
        text(ModItems.loreKey("bannhammer", 2), "Every third hit: jolt (the target freezes and is pulled back)",
                "Jeder dritte Treffer: Ruckler (das Ziel hängt fest und wird zurückgerissen)");
        text(ModItems.loreKey("bannhammer", 3), "Every fifth hit: ban – a burst of pixels hits everything around",
                "Jeder fünfte Treffer: Bann – ein Knall aus Pixeln trifft alles ringsum");
        text("item.kingdomsatwar.bannhammer.ban", "Banned!", "Gebannt!");
        item(io.github.sirschrolli.kingdomsatwar.registry.AnniBlocks.GLITCH_WINGS, "Glitch Wings", "Fehlerflügel");
        lore("glitch_wings", "Wings of missing texture – glide like elytra", "Flügel aus fehlender Textur – gleiten wie Elytren");
        text(ModItems.loreKey("glitch_wings", 2), "While gliding, jump: blink eight blocks ahead (every 2 s)",
                "Im Gleitflug Springen: acht Blöcke nach vorn blinzeln (alle 2 s)");
        text(ModItems.loreKey("glitch_wings", 3), "Leaves colour fringes behind", "Ziehen Farbsäume hinter sich her");
        for (String[] e : new String[][] {{"ambient", "Anni chuckles", "Anni kichert"}, {"laugh", "Anni laughs", "Anni lacht"},
                {"hurt", "Anni hurts", "Anni nimmt Schaden"}, {"death", "Anni crashes for good", "Anni stürzt endgültig ab"},
                {"step", "Heavy footsteps", "Schwere Schritte"}, {"boot", "Something is loading", "Etwas wird geladen"},
                {"disconnect", "Connection lost", "Verbindung verloren"}, {"windup", "Anni winds up", "Anni holt aus"},
                {"smash", "Hammer smashes", "Hammer kracht"}, {"lag", "Anni lags", "Anni hängt"},
                {"teleport", "Anni glitches away", "Anni springt"}, {"miss", "Hit not registered", "Treffer nicht registriert"},
                {"clone", "Anni duplicates", "Anni verdoppelt sich"}, {"clone_crash", "Clone crashes", "Klon stürzt ab"},
                {"clone_pop", "Clone pops", "Klon zerplatzt"}, {"missing", "Texture goes missing", "Textur fehlt"},
                {"rain", "Pixels fall", "Pixel regnen"}, {"pixel", "Pixel impacts", "Pixel schlägt ein"},
                {"rollback_mark", "Positions saved", "Stellen gespeichert"}, {"rollback", "Rollback", "Rückruck"},
                {"scan", "Scanline sweeps", "Zeile fegt"}, {"crash", "Error sound", "Fehlerklang"},
                {"restart", "Anni restarts", "Anni startet neu"}, {"revive", "Anni restores a backup", "Anni lädt eine Sicherung"}}) {
            subtitle("entity.anni_troll." + e[0], e[1], e[2]);
        }
        subtitle("block.error_core.hum", "Error core hums", "Fehlerkern summt");
        subtitle("block.error_core.on", "Error core restarts", "Fehlerkern startet");
        subtitle("block.error_core.off", "Error core crashes", "Fehlerkern stürzt ab");
        subtitle("item.bannhammer.jolt", "Target jolts", "Ziel ruckelt");
        subtitle("item.bannhammer.ban", "Ban!", "Bann!");
        subtitle("item.glitch_wings.blink", "Blink", "Blinzeln");
        creatorTexts();
        block(io.github.sirschrolli.kingdomsatwar.registry.SeaBlocks.EMBEDDED_TIDE_SPEAR, "Tide Spear", "Gezeiten-Speer");
        item(io.github.sirschrolli.kingdomsatwar.registry.SeaBlocks.TIDE_SPEAR, "Tide Spear", "Gezeiten-Speer");
        lore("tide_spear", "The spear of the last Tide King", "Der Speer des letzten Gezeitenkönigs");
        text(ModItems.loreKey("tide_spear", 2), "In water and rain: strikes harder, every hit raises a wave",
                "Im Wasser und Regen: trifft härter, jeder Treffer schlägt eine Welle");
        text(ModItems.loreKey("tide_spear", 3), "In hand: swim like a dolphin", "In der Hand: schwimmen wie ein Delfin");
        item(io.github.sirschrolli.kingdomsatwar.registry.SeaBlocks.TIDE_HORN, "Tide Horn", "Gezeitenhorn");
        lore("tide_horn", "Blow it: the sea answers from the Sunken Atoll – in the Primordial Sea from the Kraken's maw",
                "Hineinblasen: Das Meer antwortet aus Richtung des Versunkenen Atolls – im Urmeer aus dem Krakenschlund");
        text(ModItems.loreKey("tide_horn", 2), "With a Heart of the Sea: calls back Karkinos or the Kraken",
                "Mit einem Herz des Meeres: ruft Karkinos oder den Kraken zurück");
        text("item.kingdomsatwar.tide_horn.direction", "The sea answers from the %s – %s blocks away",
                "Das Meer antwortet aus dem %s – %s Blöcke entfernt");
        text("item.kingdomsatwar.tide_horn.none", "Only the wind answers.", "Nur der Wind antwortet.");
        text("item.kingdomsatwar.tide_horn.offering", "The sea wants an offering: hold a Heart of the Sea in your other hand.",
                "Das Meer verlangt eine Gabe: Halte ein Herz des Meeres in der anderen Hand.");
        text("item.kingdomsatwar.tide_horn.recalled", "The sand of the lagoon begins to churn …", "Der Sand der Lagune gerät in Bewegung …");
        for (String[] e : new String[][] {{"ambient", "Karkinos rumbles", "Karkinos grummelt"}, {"hurt", "Karkinos hurts", "Karkinos nimmt Schaden"},
                {"death", "Karkinos dies", "Karkinos stirbt"}, {"roar", "Karkinos roars", "Karkinos brüllt"},
                {"clack", "Karkinos clacks his claws", "Karkinos klappert mit den Scheren"}, {"slam", "Claw slams down", "Schere schmettert herab"},
                {"jet", "Bubble jet roars", "Blasenstrahl rauscht"}, {"burrow", "Something digs", "Etwas gräbt"},
                {"erupt", "Karkinos breaks out", "Karkinos bricht hervor"}, {"roll", "Shell rolls", "Panzer rollt"},
                {"step", "Heavy steps", "Schwere Schritte"}, {"tide", "The tide rises", "Die Flut steigt"},
                {"lance", "Water crashes down", "Wasser stürzt herab"}}) {
            subtitle("entity.reef_colossus." + e[0], e[1], e[2]);
        }
        for (String[] e : new String[][] {{"ambient", "The Kraken gurgles", "Der Kraken gurgelt"}, {"hurt", "The Kraken hurts", "Der Kraken nimmt Schaden"},
                {"death", "The Kraken dies", "Der Kraken stirbt"}, {"rise", "Something huge rises", "Etwas Gewaltiges steigt empor"},
                {"roar", "The Kraken roars", "Der Kraken brüllt"}, {"wave", "A great wave crashes", "Eine Flutwelle bricht"},
                {"sweep", "Tentacles sweep the water", "Arme fegen durchs Wasser"}, {"grab", "A tentacle grabs", "Ein Fangarm packt zu"},
                {"bite", "The Kraken bites", "Der Kraken beißt zu"}, {"ink", "The Kraken spits ink", "Der Kraken spuckt Tinte"},
                {"whirl", "A whirlpool roars", "Ein Strudel tost"}, {"slam", "A tentacle slams down", "Ein Fangarm schlägt nieder"},
                {"swim", "The Kraken swims", "Der Kraken schwimmt"}, {"charge", "The Kraken charges", "Der Kraken rammt"},
                {"dive", "The Kraken dives", "Der Kraken taucht ab"}, {"breach", "The Kraken bursts from the deep", "Der Kraken bricht hervor"},
                {"geyser", "A geyser erupts", "Ein Geysir schießt empor"}, {"tentacle", "Tentacles burst from the water", "Arme brechen aus dem Wasser"}}) {
            subtitle("entity.kraken." + e[0], e[1], e[2]);
        }
        subtitle("entity.manta_ray.hurt", "Manta Ray hurts", "Mantarochen nimmt Schaden");
        subtitle("entity.manta_ray.death", "Manta Ray dies", "Mantarochen stirbt");
        subtitle("entity.manta_ray.leap", "Manta Ray leaps", "Mantarochen springt");
        subtitle("entity.manta_ray.splash", "Manta Ray splashes down", "Mantarochen klatscht ins Wasser");
        subtitle("entity.glow_jellyfish.ambient", "Glow Jellyfish shimmers", "Leuchtqualle schimmert");
        subtitle("entity.glow_jellyfish.hurt", "Glow Jellyfish hurts", "Leuchtqualle nimmt Schaden");
        subtitle("entity.glow_jellyfish.death", "Glow Jellyfish dies", "Leuchtqualle stirbt");
        subtitle("entity.glow_jellyfish.sting", "Glow Jellyfish stings", "Leuchtqualle nesselt");
        subtitle("entity.shark.hurt", "Shark hurts", "Hai nimmt Schaden");
        subtitle("entity.shark.death", "Shark dies", "Hai stirbt");
        subtitle("entity.shark.bite", "Shark bites", "Hai beißt zu");
        subtitle("entity.anglerfish.ambient", "Anglerfish hums", "Anglerfisch summt");
        subtitle("entity.anglerfish.hurt", "Anglerfish hurts", "Anglerfisch nimmt Schaden");
        subtitle("entity.anglerfish.death", "Anglerfish dies", "Anglerfisch stirbt");
        subtitle("entity.anglerfish.snap", "Anglerfish snaps", "Anglerfisch schnappt zu");
        subtitle("entity.anglerfish.bite", "Anglerfish bites", "Anglerfisch beißt");
        subtitle("entity.giant_crab.clack", "Giant Crab clacks", "Riesenkrabbe klappert");
        subtitle("entity.giant_crab.pinch", "Giant Crab pinches", "Riesenkrabbe kneift");
        subtitle("entity.giant_crab.hurt", "Giant Crab hurts", "Riesenkrabbe nimmt Schaden");
        subtitle("entity.giant_crab.death", "Giant Crab dies", "Riesenkrabbe stirbt");
        subtitle("entity.giant_crab.step", "Giant Crab scuttles", "Riesenkrabbe trippelt");

        text(ModItems.loreKey("shadow_heart", 3), "Use on the ground to tear open a rift into the Void",
                "Auf den Boden gerichtet reißt es einen Riss in die Leere auf");
        text("item.kingdomsatwar.shadow_heart.in_void", "The Void has no deeper place to tear open.",
                "In der Leere lässt sich nichts tiefer aufreißen.");
        text("item.kingdomsatwar.shadow_heart.cannot", "The heart stays still here.", "Das Herz bleibt hier still.");
        text("item.kingdomsatwar.shadow_heart.no_space", "The rift needs room: 9 blocks wide, 10 high, on solid ground.",
                "Der Riss braucht Platz: 9 Blöcke breit, 10 hoch, auf festem Grund.");
        text("item.kingdomsatwar.shadow_heart.too_close", "Another rift is too close.", "Ein anderer Riss ist zu nah.");
        text("item.kingdomsatwar.shadow_heart.anchor", "This rift is anchored to the Void and cannot be closed.",
                "Dieser Riss ist in der Leere verankert und lässt sich nicht schließen.");
        text("message.kingdomsatwar.void.spat_out", "The Void spits you out – wounded, but alive.",
                "Die Leere spuckt dich wieder aus – verwundet, aber lebendig.");
        text("block.kingdomsatwar.bed.the_void", "There is no sleep in the Void.", "Die Leere kennt keinen Schlaf.");
    }

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
        set("cobbled_shadow_stone", "cobbled_shadow_stone", "Cobbled Shadow Stone", "Schattenbruchstein", "Cobbled Shadow Stone",
                "Schattenbruchstein");
        set("cobbled_deep_shadow_stone", "cobbled_deep_shadow_stone", "Cobbled Deep Shadow Stone", "Tiefschattenbruchstein",
                "Cobbled Deep Shadow Stone", "Tiefschattenbruchstein");
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
        block(ShadowBlocks.MOON_FERN, "Moon Fern", "Mondfarn");
        block(ShadowBlocks.STAR_BLOSSOM, "Star Blossom", "Sternblüte");
        block(ShadowBlocks.SOUL_REEDS, "Soul Reeds", "Seelenschilf");
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
        add(ModEntities.NYXAR.get(), pick("Nyxar", "Nyxar"));
        text("boss.kingdomsatwar.shadow", "Nyxar, the Shadow Dragon", "Nyxar, der Schattendrache");
        text("boss.kingdomsatwar.shadow.title", "Devourer of Light", "Der Lichtverschlinger");
        subtitle("entity.nyxar.breath", "Nyxar exhales shadow", "Nyxar haucht Schatten");
        subtitle("entity.nyxar.shriek", "Nyxar shrieks", "Nyxar kreischt");
        text("citadel.kingdomsatwar.name", "The Shadow Citadel", "Die Schattenzitadelle");
        text("citadel.kingdomsatwar.warning", "The shadows grow heavier… something on the throne is watching you.",
                "Die Schatten werden schwerer … etwas auf dem Thron beobachtet dich.");
        block(ModBlocks.SHADOW_THRONE_CORE, "Throne Core", "Thronkern");
        item(io.github.sirschrolli.kingdomsatwar.registry.ShadowItems.SHADOW_HEART, "Heart of Shadows", "Herz der Schatten");
        lore("shadow_heart", "All that remains of Nyxar", "Alles, was von Nyxar blieb");
        text(ModItems.loreKey("shadow_heart", 2), "A single soul still glimmers inside", "In ihm glimmt noch eine einzige Seele");
        text("message.kingdomsatwar.story.nyxar_fallen", "%s has fallen! Light returns to the Shadow World.",
                "%s ist gefallen! Das Licht kehrt in die Schattenwelt zurück.");
        text("message.kingdomsatwar.story.ending_title", "The Seal is Avenged", "Das Siegel ist gerächt");
        text("message.kingdomsatwar.story.ending_subtitle", "Nyxar is no more", "Nyxar ist nicht mehr");
        text("message.kingdomsatwar.story.ending",
                "The devourer of light is gone. Where his shadow lay, the Umbrer will light lanterns again – and your name will be in their chronicle.",
                "Der Lichtverschlinger ist fort. Wo sein Schatten lag, werden die Umbrer wieder Laternen entzünden – und dein Name steht in ihrer Chronik.");
        text("item.kingdomsatwar.dragon_compass.citadel", "Nyxar's Shadow Citadel", "Nyxars Schattenzitadelle");
        add(ModEntities.BOSS_PROJECTILE.get(), pick("Dragon Projectile", "Drachengeschoss"));
        add(ModEntities.SEAL_BREAK.get(), pick("Seal Break", "Siegelbruch"));
        add(ModEntities.INTRO_VISION.get(), pick("Vision of the Seal", "Vision des Siegels"));
        text("command.kingdomsatwar.boss.summoned", "%s awakens!", "%s erwacht!");
        text("command.kingdomsatwar.dim.done", "You are now %s (%s, %s, %s).", "Du bist jetzt %s (%s, %s, %s).");
        text("command.kingdomsatwar.dim.already", "You are already %s.", "Du bist schon %s.");
        text("command.kingdomsatwar.dim.missing", "This dimension does not exist in this world.", "Diese Dimension gibt es in dieser Welt nicht.");
        text("command.kingdomsatwar.dim.name.oberwelt", "in the Overworld", "in der Oberwelt");
        text("command.kingdomsatwar.dim.name.nether", "in the Nether", "im Nether");
        text("command.kingdomsatwar.dim.name.end", "in the End", "im End");
        text("command.kingdomsatwar.dim.name.schattenwelt", "in the Shadow World", "in der Schattenwelt");
        text("command.kingdomsatwar.dim.name.leere", "in the Void", "in der Leere");
        text("command.kingdomsatwar.dim.name.sphaere", "in the Arcane Sphere", "in der Arkanen Sphäre");
        text("command.kingdomsatwar.dim.name.other", "in %s", "in %s");
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
        lore("chronicle", "The story of the seal – chapter by chapter (also opens with the chronicle key)",
                "Die Geschichte des Siegels – Kapitel für Kapitel (öffnet sich auch mit der Taste der Chronik)");
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
                        + "someone to unite it.\n\nPlant a town banner to found your settlement. Coins buy you a builder, farmers and "
                        + "craftsmen – and every army begins with the knights and mercenaries of the villages.",
                "Du bist in einem fremden Land angekommen, mit kaum mehr als dem, was du am Leib trägst. Doch dieses Land wartet auf "
                        + "jemanden, der es eint.\n\nSetze ein Stadtbanner und gründe deine Siedlung. Mit Münzen heuerst du einen "
                        + "Baumeister, Bauern und Handwerker an – und jedes Heer beginnt mit den Rittern und Söldnern der Dörfer.");
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
        text("chronicle.kingdomsatwar.goal.town", "Found your settlement with a town banner", "Gründe deine Siedlung mit einem Stadtbanner");
        text("chronicle.kingdomsatwar.goal.recruit", "Hire your first citizen or soldier", "Heuere deinen ersten Bürger oder Soldaten an");
        text("chronicle.kingdomsatwar.goal.read", "Read the chronicle (key %s)", "Lies die Chronik (Taste %s)");
        text("message.kingdomsatwar.story.chronicle_key", "Press %s to open the Chronicle of the Seal – it tells the story and shows your goals.",
                "Drücke %s, um die Chronik des Siegels aufzuschlagen – sie erzählt die Geschichte und nennt deine Ziele.");
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
        farmAnimals();
        creature(ModEntities.WILD_BOAR, ModItems.WILD_BOAR_SPAWN_EGG, "Wild Boar", "Wildschwein", "Wildschwein");
        creature(ModEntities.DEER, ModItems.DEER_SPAWN_EGG, "Red Deer", "Rothirsch", "Rothirsch");
        creature(ModEntities.GOOSE, ModItems.GOOSE_SPAWN_EGG, "Goose", "Gans", "Gänse");
        creature(ModEntities.WILL_O_WISP, ModItems.WILL_O_WISP_SPAWN_EGG, "Will-o'-the-Wisp", "Irrlicht", "Irrlicht");
        creature(ModEntities.CAVE_TROLL, ModItems.CAVE_TROLL_SPAWN_EGG, "Cave Troll", "Höhlentroll", "Höhlentroll");
        creature(ModEntities.LINDWORM, ModItems.LINDWORM_SPAWN_EGG, "Lindworm", "Lindwurm", "Lindwurm");
        // Neue Monster (0.69)
        creature(ModEntities.WEREWOLF, ModItems.WEREWOLF_SPAWN_EGG, "Werewolf", "Werwolf", "Werwolf");
        creature(ModEntities.MIRE_SHAMBLER, ModItems.MIRE_SHAMBLER_SPAWN_EGG, "Mire Shambler", "Moorschlurfer", "Moorschlurfer");
        creature(ModEntities.EMBER_WYRM, ModItems.EMBER_WYRM_SPAWN_EGG, "Ember Wyrm", "Glutwurm", "Glutwurm");
        creature(ModEntities.ASH_WRAITH, ModItems.ASH_WRAITH_SPAWN_EGG, "Ash Wraith", "Aschengeist", "Aschengeist");
        // Monster der Schattenwelt und des Ends (0.71)
        creature(ModEntities.SOUL_HERON, ModItems.SOUL_HERON_SPAWN_EGG, "Soul Heron", "Seelenreiher", "Seelenreiher");
        creature(ModEntities.NIGHT_RAM, ModItems.NIGHT_RAM_SPAWN_EGG, "Night Ram", "Nachtwidder", "Nachtwidder");
        creature(ModEntities.RIFT_HOUND, ModItems.RIFT_HOUND_SPAWN_EGG, "Rift Hound", "Rissjäger", "Rissjäger");
        creature(ModEntities.CHORUS_MAW, ModItems.CHORUS_MAW_SPAWN_EGG, "Chorus Maw", "Chorusschlund", "Chorusschlund");
        add(ModEntities.MONSTER_SHOT.get(), pick("Monster Missile", "Monstergeschoss"));
        for (String[] e : new String[][] {
                {"werewolf.growl", "Werewolf growls", "Werwolf knurrt"}, {"werewolf.howl", "Werewolf howls", "Werwolf heult"},
                {"werewolf.snarl", "Werewolf snarls", "Werwolf faucht"}, {"werewolf.hurt", "Werewolf hurts", "Werwolf wird verletzt"},
                {"werewolf.death", "Werewolf dies", "Werwolf stirbt"}, {"werewolf.step", "Footsteps", "Schritte"},
                {"mire_shambler.ambient", "Mire shambler gurgles", "Moorschlurfer gurgelt"},
                {"mire_shambler.hurt", "Mire shambler hurts", "Moorschlurfer wird verletzt"},
                {"mire_shambler.death", "Mire shambler collapses", "Moorschlurfer zerfällt"},
                {"mire_shambler.step", "Squelching footsteps", "Schmatzende Schritte"},
                {"mire_shambler.throw", "Mud is hurled", "Schlamm wird geschleudert"},
                {"mire_shambler.grasp", "Roots grasp", "Wurzeln packen zu"},
                {"mire_shambler.rise", "Something rises from the mud", "Etwas steigt aus dem Schlamm"},
                {"mire_shambler.splat", "Mud splats", "Schlamm klatscht"},
                {"ember_wyrm.ambient", "Ember wyrm rumbles", "Glutwurm grollt"}, {"ember_wyrm.hurt", "Ember wyrm hurts", "Glutwurm wird verletzt"},
                {"ember_wyrm.death", "Ember wyrm dies", "Glutwurm stirbt"}, {"ember_wyrm.surface", "Ember wyrm surfaces", "Glutwurm taucht auf"},
                {"ember_wyrm.spit", "Ember wyrm spits magma", "Glutwurm spuckt Magma"}, {"ember_wyrm.bite", "Ember wyrm bites", "Glutwurm beißt"},
                {"ember_wyrm.dive", "Ember wyrm dives", "Glutwurm taucht ab"},
                {"ash_wraith.ambient", "Ash wraith whispers", "Aschengeist flüstert"}, {"ash_wraith.hurt", "Ash wraith hurts", "Aschengeist wird verletzt"},
                {"ash_wraith.death", "Ash wraith crumbles to ash", "Aschengeist zerfällt zu Asche"},
                {"ash_wraith.cloud", "Ash billows", "Asche wallt auf"}, {"ash_wraith.bolt", "Soul fire hisses", "Seelenfeuer zischt"},
                {"ash_wraith.fade", "Ash wraith fades", "Aschengeist verweht"},
                {"soul_heron.call", "Soul heron croaks", "Seelenreiher krächzt"}, {"soul_heron.stab", "Soul heron stabs", "Seelenreiher stößt zu"},
                {"soul_heron.wings", "Wings beat", "Flügel schlagen"}, {"soul_heron.hurt", "Soul heron hurts", "Seelenreiher wird verletzt"},
                {"soul_heron.death", "Soul heron dies", "Seelenreiher stirbt"}, {"soul_heron.step", "Wading steps", "Watende Schritte"},
                {"night_ram.ambient", "Night ram grunts", "Nachtwidder brummt"}, {"night_ram.bleat", "Night ram bellows", "Nachtwidder röhrt"},
                {"night_ram.scrape", "Hoof scrapes", "Huf scharrt"}, {"night_ram.charge", "Night ram charges", "Nachtwidder stürmt los"},
                {"night_ram.impact", "Horns crash", "Hörner krachen"}, {"night_ram.stomp", "Crystals shatter", "Kristalle bersten"},
                {"night_ram.hurt", "Night ram hurts", "Nachtwidder wird verletzt"}, {"night_ram.death", "Night ram dies", "Nachtwidder stirbt"},
                {"night_ram.step", "Hoofbeats", "Hufschläge"},
                {"rift_hound.ambient", "Rift hound growls", "Rissjäger knurrt"}, {"rift_hound.howl", "Rift hound howls", "Rissjäger heult"},
                {"rift_hound.phase", "Something tears through the void", "Etwas reißt durch die Leere"},
                {"rift_hound.bite", "Rift hound bites", "Rissjäger beißt"}, {"rift_hound.hurt", "Rift hound hurts", "Rissjäger wird verletzt"},
                {"rift_hound.death", "Rift hound dissolves", "Rissjäger zerfällt"}, {"rift_hound.step", "Footsteps", "Schritte"},
                {"chorus_maw.ambient", "Chorus maw rustles", "Chorusschlund raschelt"}, {"chorus_maw.open", "A blossom opens", "Eine Blüte öffnet sich"},
                {"chorus_maw.lash", "Vine lashes", "Ranke peitscht"}, {"chorus_maw.bite", "Chorus maw snaps", "Chorusschlund schnappt zu"},
                {"chorus_maw.spit", "Chorus maw spits", "Chorusschlund spuckt"},
                {"chorus_maw.teleport", "Chorus maw vanishes", "Chorusschlund verschwindet"},
                {"chorus_maw.hurt", "Chorus maw hurts", "Chorusschlund wird verletzt"}, {"chorus_maw.death", "Chorus maw withers", "Chorusschlund welkt"}}) {
            subtitle("entity." + e[0], e[1], e[2]);
        }

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
    /** Eigene Hof- und Weidetiere: Art, Spawn-Ei und je Rasse ein eigener Name. */
    private void farmAnimals() {
        creature(ModEntities.CATTLE, ModItems.CATTLE_SPAWN_EGG, "Cattle", "Hausrind", "Rinder");
        breeds(ModEntities.CATTLE, new String[][]{{"fleckvieh", "Simmental Cattle", "Fleckvieh"}, {"braunvieh", "Brown Swiss", "Braunvieh"},
                {"hochland", "Highland Cattle", "Hochlandrind"}, {"steppe", "Steppe Cattle", "Steppenrind"},
                {"pilz", "Mushroom Cattle", "Pilzrind"}});
        creature(ModEntities.FARM_PIG, ModItems.PIG_SPAWN_EGG, "Pig", "Hausschwein", "Schweine");
        breeds(ModEntities.FARM_PIG, new String[][]{{"landschwein", "Landrace Pig", "Landschwein"},
                {"sattelschwein", "Saddleback Pig", "Sattelschwein"}, {"bunt", "Spotted Pig", "Buntes Schwein"},
                {"wollschwein", "Woolly Pig", "Wollschwein"}});
        creature(ModEntities.FARM_SHEEP, ModItems.SHEEP_SPAWN_EGG, "Sheep", "Schaf", "Schaf");
        breeds(ModEntities.FARM_SHEEP, new String[][]{{"merino", "Merino Sheep", "Merinoschaf"}, {"heidschnucke", "Heath Sheep", "Heidschnucke"},
                {"schwarzkopf", "Blackface Sheep", "Schwarzkopfschaf"}});
        creature(ModEntities.FARM_CHICKEN, ModItems.CHICKEN_SPAWN_EGG, "Chicken", "Huhn", "Hühner");
        breeds(ModEntities.FARM_CHICKEN, new String[][]{{"white", "White Hen", "Weiße Henne"}, {"brown", "Brown Hen", "Braune Henne"},
                {"black", "Black Hen", "Schwarze Henne"}, {"barred", "Barred Hen", "Sperberhenne"}, {"rooster", "Rooster", "Hahn"}});
        creature(ModEntities.FARM_HORSE, ModItems.HORSE_SPAWN_EGG, "Horse", "Pferd", "Pferde");
        breeds(ModEntities.FARM_HORSE, new String[][]{{"fuchs", "Chestnut Horse", "Fuchs"}, {"brauner", "Bay Horse", "Brauner"},
                {"friese", "Friesian", "Friese"}, {"schimmel", "Dapple Grey", "Apfelschimmel"}, {"schecke", "Pinto", "Schecke"},
                {"haflinger", "Haflinger", "Haflinger"}, {"kaltblut", "Draft Horse", "Kaltblut"}, {"falbe", "Dun Horse", "Falbe"},
                {"pony", "Pony", "Pony"}});
        creature(ModEntities.FARM_DONKEY, ModItems.DONKEY_SPAWN_EGG, "Donkey", "Esel", "Esel");
        breeds(ModEntities.FARM_DONKEY, new String[][]{{"grau", "Grey Donkey", "Grauesel"}, {"braun", "Brown Donkey", "Brauner Esel"}});
        creature(ModEntities.FARM_MULE, ModItems.MULE_SPAWN_EGG, "Mule", "Maultier", "Maultier");
        breeds(ModEntities.FARM_MULE, new String[][]{{"braun", "Mule", "Maultier"}, {"rappe", "Black Mule", "Rappmaultier"}});
        creature(ModEntities.FARM_GOAT, ModItems.GOAT_SPAWN_EGG, "Goat", "Ziege", "Ziegen");
        breeds(ModEntities.FARM_GOAT, new String[][]{{"steinbock", "Ibex", "Steinbock"}, {"gaemse", "Chamois", "Gämse"},
                {"schnee", "Snow Goat", "Schneeziege"}, {"haus", "Domestic Goat", "Hausziege"}});
        creature(ModEntities.HARE, ModItems.HARE_SPAWN_EGG, "Hare", "Hase", "Hasen");
        breeds(ModEntities.HARE, new String[][]{{"feldhase", "Brown Hare", "Feldhase"}, {"schneehase", "Snow Hare", "Schneehase"},
                {"wuestenhase", "Desert Hare", "Wüstenhase"}, {"kaninchen", "Wild Rabbit", "Wildkaninchen"},
                {"schecke", "Spotted Rabbit", "Scheckenkaninchen"}, {"mordshase", "Killer Hare", "Mordshase"}});
        // Eigene Wildtiere
        creature(ModEntities.WOLF, ModItems.WOLF_SPAWN_EGG, "Wolf", "Wolf", "Wolfs");
        breeds(ModEntities.WOLF, new String[][]{{"grau", "Grey Wolf", "Grauwolf"}, {"polar", "Arctic Wolf", "Polarwolf"},
                {"schwarz", "Black Wolf", "Schwarzwolf"}, {"timber", "Timber Wolf", "Timberwolf"}, {"steppe", "Steppe Wolf", "Steppenwolf"},
                {"rot", "Red Wolf", "Rotwolf"}});
        creature(ModEntities.FOX, ModItems.FOX_SPAWN_EGG, "Fox", "Fuchs", "Fuchs");
        breeds(ModEntities.FOX, new String[][]{{"rot", "Red Fox", "Rotfuchs"}, {"polar", "Arctic Fox", "Polarfuchs"},
                {"wueste", "Fennec Fox", "Wüstenfuchs"}, {"silber", "Silver Fox", "Silberfuchs"}});
        creature(ModEntities.BEAR, ModItems.BEAR_SPAWN_EGG, "Bear", "Bär", "Bären");
        breeds(ModEntities.BEAR, new String[][]{{"eis", "Polar Bear", "Eisbär"}, {"braun", "Brown Bear", "Braunbär"},
                {"schwarz", "Black Bear", "Schwarzbär"}});
        creature(ModEntities.PANDA, ModItems.PANDA_SPAWN_EGG, "Panda", "Panda", "Panda");
        creature(ModEntities.CAT, ModItems.CAT_SPAWN_EGG, "Cat", "Hauskatze", "Katzen");
        breeds(ModEntities.CAT, new String[][]{{"getigert", "Tabby Cat", "Getigerte Katze"}, {"rot", "Ginger Tomcat", "Roter Kater"},
                {"siam", "Siamese Cat", "Siamkatze"}, {"kartaeuser", "Chartreux", "Kartäuser"}, {"glueck", "Calico Cat", "Glückskatze"},
                {"perser", "Persian Cat", "Perserkatze"}, {"smoking", "Tuxedo Cat", "Smokingkatze"}, {"hexe", "Witch's Cat", "Hexenkatze"}});
        creature(ModEntities.WILDCAT, ModItems.WILDCAT_SPAWN_EGG, "Wildcat", "Wildkatze", "Wildkatzen");
        breeds(ModEntities.WILDCAT, new String[][]{{"ozelot", "Ocelot", "Ozelot"}, {"luchs", "Lynx", "Luchs"},
                {"waldkatze", "European Wildcat", "Waldkatze"}});
        creature(ModEntities.PARROT, ModItems.PARROT_SPAWN_EGG, "Parrot", "Papagei", "Papageien");
        breeds(ModEntities.PARROT, new String[][]{{"ara", "Scarlet Macaw", "Hellroter Ara"}, {"hyazinth", "Hyacinth Macaw", "Hyazinthara"},
                {"amazone", "Amazon Parrot", "Amazonenpapagei"}, {"gelbbrust", "Blue-and-yellow Macaw", "Gelbbrustara"},
                {"grau", "Grey Parrot", "Graupapagei"}});
        creature(ModEntities.BEE, ModItems.BEE_SPAWN_EGG, "Bee", "Biene", "Bienen");
        breeds(ModEntities.BEE, new String[][]{{"honig", "Honey Bee", "Honigbiene"}, {"hummel", "Bumblebee", "Hummel"}});
        creature(ModEntities.BAT, ModItems.BAT_SPAWN_EGG, "Bat", "Fledermaus", "Fledermaus");
        breeds(ModEntities.BAT, new String[][]{{"abendsegler", "Noctule", "Abendsegler"}, {"mausohr", "Mouse-eared Bat", "Mausohr"},
                {"vampir", "Vampire Bat", "Vampirfledermaus"}});
        creature(ModEntities.ARMADILLO, ModItems.ARMADILLO_SPAWN_EGG, "Armadillo", "Gürteltier", "Gürteltier");
        breeds(ModEntities.ARMADILLO, new String[][]{{"neunbinden", "Nine-banded Armadillo", "Neunbinden-Gürteltier"},
                {"kugel", "Three-banded Armadillo", "Kugelgürteltier"}});
        creature(ModEntities.CAMEL, ModItems.CAMEL_SPAWN_EGG, "Camel", "Kamel", "Kamel");
        breeds(ModEntities.CAMEL, new String[][]{{"dromedar", "Dromedary", "Dromedar"}, {"trampeltier", "Bactrian Camel", "Trampeltier"}});
        creature(ModEntities.LLAMA, ModItems.LLAMA_SPAWN_EGG, "Llama", "Lama", "Lama");
        breeds(ModEntities.LLAMA, new String[][]{{"creme", "Cream Llama", "Cremefarbenes Lama"}, {"weiss", "White Llama", "Weißes Lama"},
                {"braun", "Brown Llama", "Braunes Lama"}, {"grau", "Grey Llama", "Graues Lama"}});
        creature(ModEntities.TRADER_LLAMA, ModItems.TRADER_LLAMA_SPAWN_EGG, "Trader Llama", "Händlerlama", "Händlerlama");
        // Eigene Monster (E4a)
        creature(ModEntities.ZOMBIE, ModItems.ZOMBIE_SPAWN_EGG, "Revenant", "Wiedergänger", "Wiedergänger");
        breeds(ModEntities.ZOMBIE, new String[][]{{"bauer", "Revenant", "Wiedergänger"}, {"soeldner", "Undead Mercenary", "Untoter Söldner"},
                {"moench", "Undead Monk", "Untoter Mönch"}});
        creature(ModEntities.HUSK, ModItems.HUSK_SPAWN_EGG, "Mummy", "Mumie", "Mumien");
        breeds(ModEntities.HUSK, new String[][]{{"mumie", "Mummy", "Mumie"}, {"wuestling", "Desert Corpse", "Wüstenleiche"}});
        creature(ModEntities.DROWNED, ModItems.DROWNED_SPAWN_EGG, "Drowned Corpse", "Wasserleiche", "Wasserleichen");
        breeds(ModEntities.DROWNED, new String[][]{{"matrose", "Drowned Sailor", "Wasserleiche"},
                {"fischer", "Drowned Fisherman", "Ertrunkener Fischer"}});
        creature(ModEntities.ZOMBIE_VILLAGER, ModItems.ZOMBIE_VILLAGER_SPAWN_EGG, "Undead Villager", "Untoter Dorfbewohner",
                "Untoter-Dorfbewohner");
        add(ModEntities.ZOMBIE_VILLAGER.get().getDescriptionId() + ".female", pick("Undead Villager", "Untote Dorfbewohnerin"));
        creature(ModEntities.SKELETON, ModItems.SKELETON_SPAWN_EGG, "Boneman", "Gerippe", "Gerippe");
        breeds(ModEntities.SKELETON, new String[][]{{"knochen", "Boneman", "Gerippe"}, {"ritter", "Bone Knight", "Knochenritter"},
                {"jaeger", "Bone Hunter", "Knochenjäger"}});
        creature(ModEntities.STRAY, ModItems.STRAY_SPAWN_EGG, "Frostbones", "Frostgebein", "Frostgebein");
        creature(ModEntities.BOGGED, ModItems.BOGGED_SPAWN_EGG, "Bogbones", "Moorgebein", "Moorgebein");
        creature(ModEntities.PARCHED, ModItems.PARCHED_SPAWN_EGG, "Drybones", "Dörrgebein", "Dörrgebein");
        creature(ModEntities.CREEPER, ModItems.CREEPER_SPAWN_EGG, "Creepling", "Kriecher", "Kriecher");
        breeds(ModEntities.CREEPER, new String[][]{{"wald", "Forest Creepling", "Waldkriecher"}, {"wueste", "Desert Creepling", "Wüstenkriecher"},
                {"schnee", "Snow Creepling", "Schneekriecher"}, {"hoehle", "Cave Creepling", "Höhlenkriecher"},
                {"dschungel", "Jungle Creepling", "Dschungelkriecher"}});
        creature(ModEntities.SPIDER, ModItems.SPIDER_SPAWN_EGG, "Spider", "Spinne", "Spinnen");
        breeds(ModEntities.SPIDER, new String[][]{{"wald", "Wolf Spider", "Waldspinne"}, {"wueste", "Desert Tarantula", "Wüstenspinne"},
                {"dschungel", "Jungle Spider", "Dschungelspinne"}, {"schnee", "Snow Spider", "Schneespinne"}});
        creature(ModEntities.CAVE_SPIDER, ModItems.CAVE_SPIDER_SPAWN_EGG, "Cave Spider", "Höhlenspinne", "Höhlenspinnen");
        creature(ModEntities.SLIME, ModItems.SLIME_SPAWN_EGG, "Ooze", "Schleim", "Schleim");
        breeds(ModEntities.SLIME, new String[][]{{"sumpf", "Bog Ooze", "Sumpfschleim"}, {"hoehle", "Cave Ooze", "Höhlenschleim"}});
        creature(ModEntities.SULFUR_CUBE, ModItems.SULFUR_CUBE_SPAWN_EGG, "Brimstone Cube", "Schwefelwürfel", "Schwefelwürfel");
        creature(ModEntities.WITCH, ModItems.WITCH_SPAWN_EGG, "Hag", "Hexe", "Hexen");
        breeds(ModEntities.WITCH, new String[][]{{"sumpf", "Bog Hag", "Moorhexe"}, {"nacht", "Night Hag", "Nachthexe"},
                {"kraut", "Herb Hag", "Kräuterhexe"}});
        creature(ModEntities.PHANTOM, ModItems.PHANTOM_SPAWN_EGG, "Nightmare", "Nachtmahr", "Nachtmahr");
        breeds(ModEntities.PHANTOM, new String[][]{{"nachtmahr", "Nightmare", "Nachtmahr"}, {"bleich", "Pale Nightmare", "Bleicher Nachtmahr"}});
        creature(ModEntities.SILVERFISH, ModItems.SILVERFISH_SPAWN_EGG, "Silverling", "Silberfischchen", "Silberfischchen");
        breeds(ModEntities.SILVERFISH, new String[][]{{"silber", "Silverling", "Silberfischchen"}, {"tiefe", "Deep Silverling", "Tiefenfischchen"}});
        creature(ModEntities.ZOMBIE_HORSE, ModItems.ZOMBIE_HORSE_SPAWN_EGG, "Rotting Horse", "Zombiepferd", "Zombiepferd");
        breeds(ModEntities.ZOMBIE_HORSE, new String[][]{{"grau", "Rotting Horse", "Zombiepferd"},
                {"rappe", "Rotting Black Horse", "Verwester Rappe"}});
        creature(ModEntities.SKELETON_HORSE, ModItems.SKELETON_HORSE_SPAWN_EGG, "Bone Horse", "Skelettpferd", "Skelettpferd");
        breeds(ModEntities.SKELETON_HORSE, new String[][]{{"bleich", "Bone Horse", "Skelettpferd"},
                {"alt", "Ancient Bone Horse", "Uraltes Skelettpferd"}});
        creature(ModEntities.CAMEL_HUSK, ModItems.CAMEL_HUSK_SPAWN_EGG, "Dried Camel", "Dörrkamel", "Dörrkamel");
        breeds(ModEntities.CAMEL_HUSK, new String[][]{{"doerr", "Dried Camel", "Dörrkamel"}, {"mumie", "Mummified Camel", "Mumienkamel"}});
        // übrige Monster (E4b)
        creature(ModEntities.PILLAGER, ModItems.PILLAGER_SPAWN_EGG, "Highwayman", "Wegelagerer", "Wegelagerer");
        breeds(ModEntities.PILLAGER, new String[][]{{"wald", "Forest Highwayman", "Wegelagerer"},
                {"steppe", "Steppe Raider", "Steppenräuber"}, {"nord", "Northern Brigand", "Nordräuber"}});
        creature(ModEntities.VINDICATOR, ModItems.VINDICATOR_SPAWN_EGG, "Butcher", "Schlächter", "Schlächter");
        breeds(ModEntities.VINDICATOR, new String[][]{{"henker", "Headsman", "Henker"}, {"berserker", "Berserker", "Berserker"}});
        creature(ModEntities.EVOKER, ModItems.EVOKER_SPAWN_EGG, "Conjurer", "Beschwörer", "Beschwörer");
        breeds(ModEntities.EVOKER, new String[][]{{"schatten", "Shadow Conjurer", "Schattenbeschwörer"},
                {"runen", "Rune Priest", "Runenpriester"}});
        creature(ModEntities.ILLUSIONER, ModItems.ILLUSIONER_SPAWN_EGG, "Trickster", "Gaukler", "Gaukler");
        creature(ModEntities.VEX, ModItems.VEX_SPAWN_EGG, "Tormentor", "Plagegeist", "Plagegeist");
        breeds(ModEntities.VEX, new String[][]{{"violett", "Tormentor", "Plagegeist"}, {"tuerkis", "Rune Tormentor", "Runengeist"}});
        creature(ModEntities.RAVAGER, ModItems.RAVAGER_SPAWN_EGG, "Devastator", "Verwüster", "Verwüster");
        breeds(ModEntities.RAVAGER, new String[][]{{"auerochse", "War Aurochs", "Kriegsauerochse"},
                {"steppe", "Steppe Devastator", "Steppenungetüm"}});
        creature(ModEntities.GUARDIAN, ModItems.GUARDIAN_SPAWN_EGG, "Reef Guardian", "Riffwächter", "Riffwächter");
        creature(ModEntities.ELDER_GUARDIAN, ModItems.ELDER_GUARDIAN_SPAWN_EGG, "Ancient Reef Guardian", "Uralter Riffwächter",
                "Uralter-Riffwächter");
        creature(ModEntities.BREEZE, ModItems.BREEZE_SPAWN_EGG, "Wind Spirit", "Windgeist", "Windgeist");
        creature(ModEntities.CREAKING, ModItems.CREAKING_SPAWN_EGG, "Gnarl", "Knorz", "Knorz");
        creature(ModEntities.WARDEN, ModItems.WARDEN_SPAWN_EGG, "Deepwarden", "Tiefenhüter", "Tiefenhüter");
        // Wassertiere (E4c)
        creature(ModEntities.COD, ModItems.COD_SPAWN_EGG, "Codfish", "Dorsch", "Dorsch");
        breeds(ModEntities.COD, new String[][]{{"dorsch", "Codfish", "Dorsch"}, {"seelachs", "Pollock", "Seelachs"}});
        creature(ModEntities.SALMON, ModItems.SALMON_SPAWN_EGG, "Salmon", "Lachs", "Lachs");
        breeds(ModEntities.SALMON, new String[][]{{"lachs", "Salmon", "Lachs"}, {"rotlachs", "Sockeye Salmon", "Rotlachs"}});
        creature(ModEntities.PUFFERFISH, ModItems.PUFFERFISH_SPAWN_EGG, "Pufferfish", "Kugelfisch", "Kugelfisch");
        creature(ModEntities.TROPICAL_FISH, ModItems.TROPICAL_FISH_SPAWN_EGG, "Reef Fish", "Rifffisch", "Rifffisch");
        creature(ModEntities.SQUID, ModItems.SQUID_SPAWN_EGG, "Inkfish", "Tintenfisch", "Tintenfisch");
        creature(ModEntities.GLOW_SQUID, ModItems.GLOW_SQUID_SPAWN_EGG, "Deep Kraken", "Tiefseekrake", "Tiefseekraken");
        creature(ModEntities.DOLPHIN, ModItems.DOLPHIN_SPAWN_EGG, "Dolphin", "Delfin", "Delfin");
        breeds(ModEntities.DOLPHIN, new String[][]{{"tuemmler", "Bottlenose Dolphin", "Großer Tümmler"}, {"schwertwal", "Orca", "Schwertwal"}});
        creature(ModEntities.TURTLE, ModItems.TURTLE_SPAWN_EGG, "Sea Turtle", "Meeresschildkröte", "Meeresschildkröten");
        breeds(ModEntities.TURTLE, new String[][]{{"suppen", "Green Sea Turtle", "Suppenschildkröte"},
                {"karett", "Hawksbill Turtle", "Karettschildkröte"}});
        creature(ModEntities.AXOLOTL, ModItems.AXOLOTL_SPAWN_EGG, "Newt", "Molch", "Molch");
        creature(ModEntities.FROG, ModItems.FROG_SPAWN_EGG, "Frog", "Frosch", "Frosch");
        creature(ModEntities.TADPOLE, ModItems.TADPOLE_SPAWN_EGG, "Polliwog", "Kaulquappe", "Kaulquappen");
        creature(ModEntities.NAUTILUS, ModItems.NAUTILUS_SPAWN_EGG, "Nautilus", "Nautilus", "Nautilus");
        creature(ModEntities.ZOMBIE_NAUTILUS, ModItems.ZOMBIE_NAUTILUS_SPAWN_EGG, "Ghost Nautilus", "Geisternautilus", "Geisternautilus");
        // Golems und Wichtel (E4d)
        creature(ModEntities.IRON_GOLEM, ModItems.IRON_GOLEM_SPAWN_EGG, "Iron Sentinel", "Eisenwächter", "Eisenwächter");
        breeds(ModEntities.IRON_GOLEM, new String[][]{{"schmiedeeisen", "Iron Sentinel", "Eisenwächter"}, {"mooswaechter", "Moss Sentinel", "Mooswächter"},
                {"frostwaechter", "Frost Sentinel", "Frostwächter"}, {"rostwaechter", "Rust Sentinel", "Rostwächter"}});
        creature(ModEntities.SNOW_GOLEM, ModItems.SNOW_GOLEM_SPAWN_EGG, "Snowman", "Schneemann", "Schneemann");
        creature(ModEntities.COPPER_GOLEM, ModItems.COPPER_GOLEM_SPAWN_EGG, "Copper Servant", "Kupferknecht", "Kupferknecht");
        creature(ModEntities.ALLAY, ModItems.ALLAY_SPAWN_EGG, "Sprite", "Wichtel", "Wichtel");
        breeds(ModEntities.ALLAY, new String[][]{{"mooswichtel", "Moss Sprite", "Mooswichtel"}, {"pilzwichtel", "Toadstool Sprite", "Pilzwichtel"},
                {"laubwichtel", "Leaf Sprite", "Laubwichtel"}, {"sternwichtel", "Star Sprite", "Sternwichtel"}});
        // seltene Wesen und der fahrende Händler (E4d-2)
        creature(ModEntities.SNIFFER, ModItems.SNIFFER_SPAWN_EGG, "Primeval Digger", "Urwühler", "Urwühler");
        breeds(ModEntities.SNIFFER, new String[][]{{"zottelwuehler", "Shaggy Digger", "Zottelwühler"}, {"steppenwuehler", "Steppe Digger", "Steppenwühler"},
                {"frostwuehler", "Frost Digger", "Frostwühler"}});
        creature(ModEntities.HAPPY_GHAST, ModItems.HAPPY_GHAST_SPAWN_EGG, "Cloud Jelly", "Wolkenqualle", "Wolkenqualle");
        breeds(ModEntities.HAPPY_GHAST, new String[][]{{"schoenwetter", "Fair-Weather Cloud", "Schönwetterwolke"},
                {"abendrot", "Sunset Cloud", "Abendwolke"}, {"regenwolke", "Rain Cloudlet", "Regenwölkchen"}});
        creature(ModEntities.WANDERING_TRADER, ModItems.WANDERING_TRADER_SPAWN_EGG, "Travelling Merchant", "Fahrender Händler",
                "Fahrender-Händler");
        // Nether-Volk (E5a)
        creature(ModEntities.PIGLIN, ModItems.PIGLIN_SPAWN_EGG, "Bristling", "Borstling", "Borstling");
        breeds(ModEntities.PIGLIN, new String[][]{{"karmesin", "Crimson Bristling", "Karmesinborstling"}, {"aschen", "Ashen Bristling", "Aschenborstling"},
                {"gold", "Gilded Bristling", "Goldborstling"}});
        creature(ModEntities.PIGLIN_BRUTE, ModItems.PIGLIN_BRUTE_SPAWN_EGG, "Bristle Giant", "Borstenhüne", "Borstenhünen");
        creature(ModEntities.ZOMBIFIED_PIGLIN, ModItems.ZOMBIFIED_PIGLIN_SPAWN_EGG, "Rotten Bristling", "Moderborstling", "Moderborstling");
        breeds(ModEntities.ZOMBIFIED_PIGLIN, new String[][]{{"moder_karmesin", "Rotten Bristling", "Moderborstling"},
                {"moder_aschen", "Withered Bristling", "Dörrborstling"}});
        // Tiere des Nethers (E5a-2)
        creature(ModEntities.HOGLIN, ModItems.HOGLIN_SPAWN_EGG, "Ember Boar", "Glutkeiler", "Glutkeiler");
        breeds(ModEntities.HOGLIN, new String[][]{{"karmesin", "Crimson Boar", "Karmesinkeiler"}, {"aschen", "Ashen Boar", "Aschenkeiler"},
                {"schlacke", "Slag Boar", "Schlackenkeiler"}});
        creature(ModEntities.ZOGLIN, ModItems.ZOGLIN_SPAWN_EGG, "Rotten Boar", "Moderkeiler", "Moderkeiler");
        breeds(ModEntities.ZOGLIN, new String[][]{{"moder_karmesin", "Rotten Boar", "Moderkeiler"},
                {"moder_aschen", "Withered Boar", "Dörrkeiler"}});
        creature(ModEntities.STRIDER, ModItems.STRIDER_SPAWN_EGG, "Lava Stilt", "Lavastelzer", "Lavastelzer");
        breeds(ModEntities.STRIDER, new String[][]{{"glut", "Ember Stilt", "Glutstelzer"}, {"seele", "Soul Stilt", "Seelenstelzer"}});
        creature(ModEntities.GHAST, ModItems.GHAST_SPAWN_EGG, "Smoke Jelly", "Rauchqualle", "Rauchqualle");
        breeds(ModEntities.GHAST, new String[][]{{"asche", "Ash Jelly", "Aschenqualle"}, {"glut", "Ember Jelly", "Glutqualle"},
                {"seele", "Soul Jelly", "Seelenqualle"}});
        creature(ModEntities.BLAZE, ModItems.BLAZE_SPAWN_EGG, "Ember Spirit", "Glutgeist", "Glutgeist");
        breeds(ModEntities.BLAZE, new String[][]{{"glut", "Ember Spirit", "Glutgeist"}, {"seele", "Soul Spirit", "Seelengeist"}});
        creature(ModEntities.MAGMA_CUBE, ModItems.MAGMA_CUBE_SPAWN_EGG, "Ember Ooze", "Glutschleim", "Glutschleim");
        breeds(ModEntities.MAGMA_CUBE, new String[][]{{"glut", "Ember Ooze", "Glutschleim"}, {"basalt", "Basalt Ooze", "Basaltschleim"}});
        creature(ModEntities.WITHER_SKELETON, ModItems.WITHER_SKELETON_SPAWN_EGG, "Charred Bones", "Brandgebein", "Brandgebein");
        breeds(ModEntities.WITHER_SKELETON, new String[][]{{"brand", "Charred Bones", "Brandgebein"}, {"seele", "Soul Bones", "Seelengebein"}});
        creature(ModEntities.WITHER, ModItems.WITHER_SPAWN_EGG, "Ash King", "Aschenkönig", "Aschenkönig");
        creature(ModEntities.ENDERMAN, ModItems.ENDERMAN_SPAWN_EGG, "Void Walker", "Leerwandler", "Leerwandler");
        breeds(ModEntities.ENDERMAN, new String[][]{{"leere", "Void Walker", "Leerwandler"}, {"wirr", "Warped Walker", "Wirrwandler"}});
        creature(ModEntities.ENDERMITE, ModItems.ENDERMITE_SPAWN_EGG, "Void Mite", "Leermilbe", "Leermilben");
        creature(ModEntities.SHULKER, ModItems.SHULKER_SPAWN_EGG, "Shrineling", "Schreinling", "Schreinling");
        // der Enderdrache bleibt der Vanilla-Drache (der Kampf hängt an ihm), sieht aber aus wie der Sternenfresser und heißt so
        add("entity.minecraft.ender_dragon", pick("Star Devourer", "Sternenfresser"));
        // Fortschritte, die jetzt zu den eigenen Bauwerken führen
        add("advancements.nether.find_fortress.title", pick("The Ash Forge", "Die Aschenesse"));
        add("advancements.end.find_end_city.title", pick("The Starwatch", "Die Sternwarte"));
        // die Köpfe von Vanilla tragen das Gesicht der eigenen Wesen – und deren Namen
        for (String[] head : new String[][] {{"skeleton_skull", "skeleton_wall_skull", "Boneman Skull", "Gerippeschädel"},
                {"wither_skeleton_skull", "wither_skeleton_wall_skull", "Charred Bones Skull", "Brandgebeinschädel"},
                {"zombie_head", "zombie_wall_head", "Revenant Head", "Wiedergängerkopf"},
                {"creeper_head", "creeper_wall_head", "Creepling Head", "Kriecherkopf"},
                {"piglin_head", "piglin_wall_head", "Bristling Head", "Borstlingskopf"},
                {"dragon_head", "dragon_wall_head", "Star Devourer Head", "Sternenfresserkopf"}}) {
            add("block.minecraft." + head[0], pick(head[2], head[3]));
            add("block.minecraft." + head[1], pick(head[2], head[3]));
        }
        add("advancements.end.find_end_city.description", pick("Enter the forgotten order's observatory at the edge of the End",
                "Betritt die Sternwarte des vergessenen Ordens am Rand des Ends"));
        add("advancements.nether.find_bastion.title", pick("The Hoard Fortress", "Die Hortfeste"));
        add("advancements.nether.find_bastion.description", pick("Enter the Bristlings' golden hoard fortress",
                "Betritt die goldene Stufenburg der Borstlinge"));
        add("advancements.nether.find_fortress.description", pick("Enter the Ash King's forge in the Nether",
                "Betritt die Esse des Aschenkönigs in der Unterwelt"));
        add("advancements.story.follow_ender_eye.description", pick("Follow an Eye of Ender to a Fallen Star",
                "Folge einem Enderauge zu einem Sternensturz"));
        add("advancements.adventure.minecraft_trials_edition.description", pick("Step foot in an Arena of Trials",
                "Betritt eine Arena der Prüfungen"));
        add("item.minecraft.ender_dragon_spawn_egg", pick("Star Devourer Spawn Egg", "Sternenfresser-Spawn-Ei"));
    }

    private void breeds(Supplier<? extends net.minecraft.world.entity.EntityType<?>> type, String[][] names) {
        for (String[] name : names) {
            add(type.get().getDescriptionId() + "." + name[0], pick(name[1], name[2]));
        }
    }

    /** Schrolli der Schöpfer (0.75): der Boss, seine Zeilen, Altar, Funke, Zepter, Schwingen, Titel und Klänge. */
    private void creatorTexts() {
        creature(ModEntities.SCHROLLI, io.github.sirschrolli.kingdomsatwar.registry.CreatorBlocks.SCHROLLI_SPAWN_EGG, "Schrolli the Creator",
                "Schrolli der Schöpfer", "Schrolli");
        creature(ModEntities.SERAPHLING, io.github.sirschrolli.kingdomsatwar.registry.CreatorBlocks.SERAPHLING_SPAWN_EGG, "Seraphling", "Seraphling",
                "Seraphling");
        add(ModEntities.DIVINE_SHOT.get(), pick("Divine Light", "Göttliches Licht"));
        text("itemGroup.kingdomsatwar.creation", "Kingdoms at War: Creation", "Kingdoms at War: Die Schöpfung");
        text("boss.kingdomsatwar.schrolli", "Schrolli the Creator", "Schrolli der Schöpfer");
        text("boss.kingdomsatwar.schrolli.title", "Creator of All Worlds", "Schöpfer aller Welten");
        text("boss.kingdomsatwar.schrolli_true", "Schrolli the Creator – True Form", "Schrolli der Schöpfer – Wahre Gestalt");
        text("boss.kingdomsatwar.schrolli_true.title", "The Creator reveals his true face", "Der Schöpfer zeigt sein wahres Antlitz");
        text("boss.kingdomsatwar.schrolli_almighty", "Schrolli the Almighty", "Schrolli der Allmächtige");
        text("boss.kingdomsatwar.schrolli_almighty.title", "The heart of the sun beats", "Das Herz der Sonne schlägt");
        // mit Namensschild: der Name, aber weiter die eigene Bossleiste
        text("boss.kingdomsatwar.schrolli.named", "%s", "%s");
        text("boss.kingdomsatwar.schrolli_true.named", "%s", "%s");
        text("boss.kingdomsatwar.schrolli_almighty.named", "%s", "%s");
        // die Bühnen der Schöpfung (seit 0.78)
        text("arena.kingdomsatwar.creation.firmament", "The Firmament", "Das Firmament");
        text("arena.kingdomsatwar.creation.firmament.subtitle", "The court shatters – beneath you, the vault of heaven",
                "Der Hof zerbricht – unter euch das Himmelszelt");
        text("arena.kingdomsatwar.creation.sun_heart", "The Heart of the Sun", "Das Herz der Sonne");
        text("arena.kingdomsatwar.creation.sun_heart.subtitle", "He has pulled you into his sun", "Er hat euch in seine Sonne gerissen");
        text("boss.kingdomsatwar.schrolli.speaker", "[Schrolli]", "[Schrolli]");
        String[][] lines = {
                {"awaken", "Who dares to disturb the peace of creation?", "Wer wagt es, die Ruhe der Schöpfung zu stören?"},
                {"creator", "I made your world. I can take it back.", "Ich habe eure Welt erschaffen. Ich kann sie auch wieder nehmen."},
                {"dust", "You fight well … for creatures of dust.", "Ihr kämpft gut … für Geschöpfe aus Staub."},
                {"enough", "Enough! Feel the might of the light!", "Genug! Spürt die Macht des Lichts!"},
                {"time", "Time obeys me.", "Die Zeit gehorcht mir."},
                {"light", "Let there be light!", "Es werde Licht!"},
                {"shattered", "You have shattered my form …", "Ihr habt meine Gestalt zerbrochen …"},
                {"true_face", "… so now behold my true face!", "… so seht nun mein wahres Antlitz!"},
                {"stars", "I made the stars. I can shatter them as well!", "Ich erschuf die Sterne. Ich kann sie auch zerschmettern!"},
                {"sun", "Perish in the light of the first sun!", "Vergeht im Licht der ersten Sonne!"},
                {"beginning", "All returns to the beginning!", "Alles kehrt zum Anfang zurück!"},
                {"last_judgment", "The Last Judgment has come!", "Das Jüngste Gericht ist gekommen!"},
                {"anew", "I create myself anew!", "Ich erschaffe mich neu!"},
                {"impossible", "Impossible …!", "Unmöglich …!"},
                {"defeated", "You … have defeated … creation itself …", "Ihr … habt … die Schöpfung selbst … bezwungen …"},
                {"farewell", "Then go … and create your own world.", "So geht … und erschafft … eure eigene Welt."},
                {"hand", "Now I shall test you with my own hand!", "Nun prüfe ich euch mit eigener Hand!"},
                {"world_blade", "This blade parted night from day!", "Diese Klinge schied die Nacht vom Tag!"},
                {"transcend", "You have broken even my true form … then all light shall fall into darkness!",
                        "Ihr habt selbst meine Wahre Gestalt gebrochen … so falle alles Licht in die Finsternis!"},
                {"almighty", "From the darkness I rise – as the sun itself. I am ALMIGHTY!",
                        "Aus der Finsternis erhebe ich mich – als die Sonne selbst. Ich bin ALLMÄCHTIG!"},
                {"sun_heart", "You stand in the heart of my sun – and still you fight?", "Ihr steht im Herzen meiner Sonne – und kämpft noch immer?"},
                {"solar_judgment", "Be judged by the sun!", "Die Sonne richtet euch!"},
        };
        for (String[] l : lines) {
            text("entity.kingdomsatwar.schrolli.line." + l[0], l[1], l[2]);
        }
        text("entity.kingdomsatwar.schrolli.judged_you", "You have been judged! Let others share the light with you!",
                "Du wurdest gerichtet! Lass andere das Licht mit dir teilen!");
        text("entity.kingdomsatwar.schrolli.judged", "%s has been judged – step into the circle to share the judgment!",
                "%s wurde gerichtet – tretet in den Kreis, um das Urteil zu teilen!");
        text("entity.kingdomsatwar.schrolli.look_away", "Look away!", "Seht weg!");
        text("entity.kingdomsatwar.schrolli.guard", "He raises his wings in guard – strike him from behind!",
                "Er hebt die Schwingen zur Wehr – greift ihn von hinten an!");
        text("entity.kingdomsatwar.schrolli.stuck", "His blade is stuck in the ground – strike now!",
                "Seine Klinge steckt im Boden – jetzt zuschlagen!");
        text("entity.kingdomsatwar.schrolli.blinded", "Blinded by the light!", "Vom Licht geblendet!");
        text("entity.kingdomsatwar.schrolli.looked_away", "You looked away – the light spares you.", "Du hast weggesehen – das Licht verschont dich.");
        text("entity.kingdomsatwar.schrolli.time_stands", "Time stands still …", "Die Zeit steht still …");
        text("entity.kingdomsatwar.schrolli.time_runs", "Time runs again – dodge the spears!", "Die Zeit läuft weiter – weicht den Speeren aus!");
        text("entity.kingdomsatwar.schrolli.nova", "SUPERNOVA", "SUPERNOVA");
        text("entity.kingdomsatwar.schrolli.nova.hint", "Get behind a pillar – or flee!", "Hinter eine Säule – oder flieht!");
        text("entity.kingdomsatwar.schrolli.nova.covered", "The pillar shielded you.", "Die Säule hat dich geschützt.");
        text("entity.kingdomsatwar.schrolli.apocalypse", "LAST JUDGMENT", "JÜNGSTES GERICHT");
        text("entity.kingdomsatwar.schrolli.true_title", "The True Form", "Die Wahre Gestalt");
        text("entity.kingdomsatwar.schrolli.solar", "JUDGMENT OF THE SUN", "GERICHT DER SONNE");
        text("entity.kingdomsatwar.schrolli.solar.hint", "Outside – inside – outside … and then into the light!", "Außen – innen – außen … und dann ins Licht!");
        text("entity.kingdomsatwar.schrolli.solar.in", "Into the white circle!", "In den weißen Kreis!");
        text("entity.kingdomsatwar.schrolli.solar.out", "Out of the circle!", "Hinaus aus dem Kreis!");
        text("entity.kingdomsatwar.schrolli.solar.light", "INTO THE LIGHT!", "INS LICHT!");
        text("entity.kingdomsatwar.schrolli.solar.safe", "The light shielded you.", "Das Licht hat dich geschützt.");
        text("entity.kingdomsatwar.schrolli.comet", "A comet is falling on you – run!", "Ein Komet stürzt auf dich herab – lauf!");
        text("entity.kingdomsatwar.schrolli.recreation", "Re-creation … interrupt him! (%s %%)", "Neuschöpfung … unterbrecht ihn! (%s %%)");
        text("entity.kingdomsatwar.schrolli.stunned", "The re-creation was interrupted – strike now!",
                "Die Neuschöpfung wurde unterbrochen – schlagt jetzt zu!");
        text("message.kingdomsatwar.schrolli.defeated", "Schrolli the Creator has been defeated!", "Schrolli der Schöpfer wurde bezwungen!");
        text("title.kingdomsatwar.godslayer", "Godslayer", "Gottbezwinger");
        text("title.kingdomsatwar.godslayer.sub", "Schrolli the Creator has fallen", "Schrolli der Schöpfer ist gefallen");
        text("title.kingdomsatwar.godslayer.prefix", "Godslayer", "Gottbezwinger");
        text("message.kingdomsatwar.godslayer", "%s has defeated the Creator and is now a Godslayer!",
                "%s hat den Schöpfer bezwungen und ist nun ein Gottbezwinger!");
        text("death.attack.kingdomsatwar.divine", "%1$s was erased by the Creator's light", "%1$s wurde vom Licht des Schöpfers ausgelöscht");
        text("death.attack.kingdomsatwar.divine.player", "%1$s was erased by the light of %2$s", "%1$s wurde vom Licht von %2$s ausgelöscht");
        // der Altar
        block(io.github.sirschrolli.kingdomsatwar.registry.CreatorBlocks.CREATOR_ALTAR, "Altar of the Creator", "Altar des Schöpfers");
        lore("creator_altar", "Place a Creator's Spark in it – then touch it", "Lege einen Schöpferfunken hinein – dann berühre ihn");
        text(ModItems.loreKey("creator_altar", 2), "Calls Schrolli the Creator: everyone close by is lifted into Creation",
                "Ruft Schrolli den Schöpfer: alle, die nahe stehen, werden in die Schöpfung gehoben");
        text(ModItems.loreKey("creator_altar", 3), "The strongest foe of all – bring friends", "Der stärkste Gegner von allen – bringt Freunde mit");
        text("block.kingdomsatwar.creator_altar.hint", "The altar waits for a Creator's Spark.", "Der Altar wartet auf einen Schöpferfunken.");
        text("block.kingdomsatwar.creator_altar.armed", "The spark rests in the altar – touch it to call the Creator (sneak to take it back).",
                "Der Funke ruht im Altar – berühre ihn, um den Schöpfer zu rufen (schleichend nimmst du ihn zurück).");
        text("block.kingdomsatwar.creator_altar.taken", "You take the spark back.", "Du nimmst den Funken zurück.");
        text("block.kingdomsatwar.creator_altar.creative", "Only survivors may challenge the Creator.", "Nur Überlebende dürfen den Schöpfer herausfordern.");
        text("block.kingdomsatwar.creator_altar.calling", "The altar awakens! Whoever stands in the golden circle will be lifted into Creation …",
                "Der Altar erwacht! Wer im goldenen Kreis steht, wird in die Schöpfung gehoben …");
        text("block.kingdomsatwar.creator_altar.coming", "The Creator is coming …", "Der Schöpfer kommt …");
        text("block.kingdomsatwar.creator_altar.awake", "The Creator is awake.", "Der Schöpfer ist erwacht.");
        // Funke, Zepter, Schwingen
        item(io.github.sirschrolli.kingdomsatwar.registry.CreatorBlocks.CREATOR_SPARK, "Creator's Spark", "Schöpferfunke");
        lore("creator_spark", "A splinter of the first light", "Ein Splitter des ersten Lichts");
        text(ModItems.loreKey("creator_spark", 2), "Dropped by the World-Eater – or forged from the trophies of all great foes",
                "Beute des Weltenfressers – oder aus den Trophäen aller großen Gegner geschmiedet");
        text(ModItems.loreKey("creator_spark", 3), "Place it in the Altar of the Creator", "Lege ihn in den Altar des Schöpfers");
        item(io.github.sirschrolli.kingdomsatwar.registry.CreatorBlocks.CREATOR_SCEPTER, "Spear of the Creator", "Speer des Schöpfers");
        lore("creator_scepter", "The Creator's spear of gold and light – unbreakable", "Der Speer des Schöpfers aus Gold und Licht – unzerbrechlich");
        text(ModItems.loreKey("creator_scepter", 2), "Strikes harder than any sword – every third hit unleashes a wave of light",
                "Trifft härter als jedes Schwert – jeder dritte Treffer entfesselt eine Lichtwelle");
        text(ModItems.loreKey("creator_scepter", 3), "Right-click: hurl a spear of light", "Rechtsklick: einen Lichtspeer schleudern");
        text(ModItems.loreKey("creator_scepter", 4), "Sneak + right-click: call a pillar of light where you look",
                "Schleichen + Rechtsklick: eine Säule aus Licht herabrufen, wohin du siehst");
        text(ModItems.loreKey("creator_scepter", 5), "Wear halo, wings and rings: the Creator's image", "Mit Heiligenschein, Schwingen und Rädern: Ebenbild des Schöpfers");
        item(io.github.sirschrolli.kingdomsatwar.registry.CreatorBlocks.CREATOR_WINGS, "Wings of the Creator", "Schwingen des Schöpfers");
        lore("creator_wings", "Feathered wings, white with golden tips – they glide like elytra", "Schwingen aus Federn, weiß mit goldenen Spitzen – gleiten wie Elytren");
        text(ModItems.loreKey("creator_wings", 2), "Protect like a chestplate: 10 armour, 4 toughness", "Schützen wie ein Harnisch: 10 Rüstung, 4 Härte");
        text(ModItems.loreKey("creator_wings", 3), "Jump while gliding: Wingbeat – a surge forward (every 1.5 seconds)",
                "Im Gleitflug springen: Schwingenschlag – ein Stoß nach vorn (alle 1,5 Sekunden)");
        text(ModItems.loreKey("creator_wings", 4), "No fall or impact damage – the wings catch you",
                "Kein Fall- und Aufprallschaden – die Schwingen fangen dich auf");
        text(ModItems.loreKey("creator_wings", 5), "They never break", "Sie zerbrechen nie");
        item(io.github.sirschrolli.kingdomsatwar.registry.CreatorBlocks.CREATOR_HALO, "Halo of the Creator", "Heiligenschein des Schöpfers");
        lore("creator_halo", "A ring of golden light above your head – protects like a helmet (4 armour, 3 toughness)",
                "Ein Ring aus goldenem Licht über dem Kopf – schützt wie ein Helm (4 Rüstung, 3 Härte)");
        text(ModItems.loreKey("creator_halo", 2), "You see in the dark – no blindness, no darkness", "Du siehst im Dunkeln – keine Blindheit, keine Dunkelheit");
        text(ModItems.loreKey("creator_halo", 3), "Blessing: half a heart every 3 seconds while wounded", "Segen: ein halbes Herz alle 3 Sekunden, solange du verwundet bist");
        text(ModItems.loreKey("creator_halo", 4), "Undead nearby catch fire and burn", "Untote in der Nähe fangen Feuer und versengen");
        text(ModItems.loreKey("creator_halo", 5), "It never breaks", "Er zerbricht nie");
        item(io.github.sirschrolli.kingdomsatwar.registry.CreatorBlocks.CREATOR_RINGS, "Wheels of the Creator", "Räder des Schöpfers");
        lore("creator_rings", "Two golden rings full of eyes circle your hips – protect like leggings (8 armour, 3 toughness)",
                "Zwei goldene Ringe voller Augen kreisen um deine Hüften – schützen wie Beinschienen (8 Rüstung, 3 Härte)");
        text(ModItems.loreKey("creator_rings", 2), "12 % faster – and they carry you up steps", "12 % schneller – und sie tragen dich Stufen hinauf");
        text(ModItems.loreKey("creator_rings", 3), "Their eyes reveal foes nearby – they glow through walls",
                "Ihre Augen enthüllen Feinde in der Nähe – sie leuchten durch Wände");
        text(ModItems.loreKey("creator_rings", 4), "Some projectiles bounce off and fly back", "Manches Geschoss prallt an ihnen ab und fliegt zurück");
        text(ModItems.loreKey("creator_rings", 5), "Halo, wings and wheels together: Wingbeat twice as fast, blessing twice as strong",
                "Heiligenschein, Schwingen und Räder zusammen: Schwingenschlag doppelt so schnell, Segen doppelt so stark");
        for (String[] e : new String[][] {{"ambient", "The Creator hums", "Der Schöpfer summt"}, {"hurt", "The Creator hurts", "Der Schöpfer nimmt Schaden"},
                {"death", "The Creator fades", "Der Schöpfer vergeht"}, {"awaken", "The Creator descends", "Der Schöpfer steigt herab"},
                {"ascend", "The Creator ascends", "Der Schöpfer steigt auf"}, {"voice", "The Creator speaks", "Der Schöpfer spricht"},
                {"spear", "Spear of light forms", "Lichtspeer entsteht"}, {"spear_fly", "Spear of light flies", "Lichtspeer fliegt"},
                {"pillar", "Pillar of light strikes", "Säule aus Licht schlägt ein"}, {"charge", "Light gathers", "Licht sammelt sich"},
                {"beam", "Beam of light", "Lichtstrahl"}, {"smite", "Scepter strikes", "Zepter schlägt zu"}, {"halo", "Halo whirs", "Heiligenschein surrt"},
                {"orb", "Vortex roars", "Sog dröhnt"}, {"blast", "Light bursts", "Licht zerbirst"}, {"wings", "Wings beat", "Schwingen schlagen"},
                {"feathers", "Feathers whirl", "Federn wirbeln"}, {"blink", "The Creator flashes", "Der Schöpfer blitzt"},
                {"choir", "Angel choir", "Engelschor"}, {"judgment", "Bell of judgment", "Glocke des Gerichts"},
                {"timestop", "Time stops", "Die Zeit hält an"}, {"time_resume", "Time runs again", "Die Zeit läuft weiter"},
                {"tick", "Clock ticks", "Uhr tickt"}, {"word", "Light blazes", "Licht gleißt"}, {"supernova", "Supernova", "Supernova"},
                {"planet", "A world forms", "Eine Welt entsteht"}, {"impact", "Heavy impact", "Schwerer Einschlag"},
                {"star", "Star falls", "Stern fällt"}, {"stun", "The Creator falls", "Der Schöpfer stürzt"},
                {"heal", "The Creator re-creates himself", "Der Schöpfer erschafft sich neu"},
                {"blade", "Blade of light forms", "Klinge aus Licht entsteht"}, {"swing", "Blade swings", "Klinge schwingt"},
                {"slash", "Blade cuts", "Klinge schneidet"}, {"dash", "The Creator dashes", "Der Schöpfer stürmt vor"},
                {"whirl", "Blade whirls", "Klinge wirbelt"}, {"guard", "The Creator raises his wings", "Der Schöpfer hebt die Schwingen"},
                {"parry", "The Creator parries", "Der Schöpfer pariert"}, {"land", "The Creator lands", "Der Schöpfer landet"}}) {
            subtitle("entity.schrolli." + e[0], e[1], e[2]);
        }
        for (String[] e : new String[][] {{"ambient", "Seraphling hums", "Seraphling summt"}, {"hurt", "Seraphling hurts", "Seraphling nimmt Schaden"},
                {"death", "Seraphling fades", "Seraphling vergeht"}, {"shoot", "Seraphling shoots", "Seraphling schießt"}}) {
            subtitle("entity.seraphling." + e[0], e[1], e[2]);
        }
        subtitle("block.creator_altar.awaken", "Altar awakens", "Altar erwacht");
        subtitle("block.creator_altar.hum", "Altar hums", "Altar summt");
        subtitle("item.creator_scepter.cast", "Spear casts", "Speer wirkt");
    }

    /** Die Meereshölzer (0.92): Tangholz, Korallenholz, Perlholz, Tiefseeholz – je Stamm, Holz, Bretter, Laub, Setzling … */
    private void seaWoods() {
        for (io.github.sirschrolli.kingdomsatwar.registry.SeaWoods.SeaWood wood : io.github.sirschrolli.kingdomsatwar.registry.SeaWoods.ALL) {
            String en = wood.english;
            String de = wood.german;
            block(wood.log, en + " Log", de + "stamm");
            block(wood.wood, en, de);
            block(wood.strippedLog, "Stripped " + en + " Log", "Entrindeter " + de + "stamm");
            block(wood.strippedWood, "Stripped " + en, "Entrindetes " + de);
            roof(wood.name + "_planks", wood.name + "_plank", en + " Planks", de + "bretter", en, de);
            block(wood.door, en + " Door", de + "tür");
            block(wood.trapdoor, en + " Trapdoor", de + "falltür");
            block(wood.fence, en + " Fence", de + "zaun");
            block(wood.fenceGate, en + " Fence Gate", de + "zauntor");
            String[] leaves = switch (wood.name) {
                case "kelpwood" -> new String[] {"Kelp Fronds", "Tangwedel"};
                case "coralwood" -> new String[] {"Coral Fans", "Korallenfächer"};
                case "pearlwood" -> new String[] {"Pearl Leaves", "Perlblätter"};
                default -> new String[] {"Glowfronds", "Leuchtwedel"};
            };
            block(wood.leaves, leaves[0], leaves[1]);
            block(wood.sapling, en + " Sapling", de + "setzling");
            block(wood.pottedSapling, "Potted " + en + " Sapling", "Eingetopfter " + de + "setzling");
        }
    }

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

    /** Magie: Aetherit-Stab, Zauber, Zaubertafeln, Zauberkreis (siehe {@link MagicLang}). */
    private void magic() {
        MagicLang.register(this::text);
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
        block(ModBlocks.BUILDER_SCAFFOLD, "Builder's Scaffold", "Baugerüst");
        block(ModBlocks.ARCANE_BARRIER, "Arcane Seal", "Bannsiegel");
        text("message.kingdomsatwar.mage.sealed", "The archmage's magic holds the sanctum together – defeat him to get out.",
                "Die Magie des Erzmagiers hält das Sanktum zusammen – besiege ihn, um hinauszukommen.");
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
                "Chain mail and sword: reliable and cheap – the backbone of any army.",
                "Kettenhemd und Schwert: zuverlässig und günstig – das Rückgrat jeder Armee.");
        soldier(SoldierType.ARCHER, "Archer", "Bogenschütze", "Bogenschützen", "Ranged fighter", "Fernkämpfer",
                "Shoots from afar, never at friends. Lightly armoured – keep him behind the front.",
                "Schießt aus der Ferne, nie auf Freunde. Leicht gerüstet: gehört hinter die Front.");
        soldier(SoldierType.KNIGHT, "Knight", "Ritter", "Ritter", "Armoured", "Gepanzert",
                "Plate armour and shield: blocks arrows and holds the front line.",
                "Plattenrüstung und Schild: wehrt Pfeile ab und hält die vorderste Reihe.");
        soldier(SoldierType.CAPTAIN, "Captain", "Hauptmann", "Hauptmann", "Leader", "Anführer",
                "Makes all soldiers nearby stronger and tougher and leads them into battle.",
                "Macht alle Soldaten in seiner Nähe stärker und zäher und führt sie an.");
        soldier(SoldierType.SPEARMAN, "Spearman", "Speerträger", "Speerträger", "Long reach", "Lange Reichweite",
                "Keeps foes at bay with his long spear and hits riders, horses and big beasts especially hard.",
                "Hält Gegner mit dem langen Spieß auf Abstand und trifft Reiter, Pferde und große Ungetüme besonders hart.");
        soldier(SoldierType.CROSSBOWMAN, "Crossbowman", "Armbrustschütze", "Armbrustschützen", "Heavy ranged", "Schwerer Fernkampf",
                "Slow to reload, but his bolts fly far and flat and hit hard – never at friends.",
                "Spannt langsam, aber seine Bolzen fliegen weit und flach und schlagen hart ein – nie auf Freunde.");
        soldier(SoldierType.SHIELDBEARER, "Shield Bearer", "Schildträger", "Schildträger", "Protector", "Beschützer",
                "Keeps his big shield raised, bangs on it and draws the attackers of his comrades onto himself.",
                "Hält den großen Schild hoch, schlägt darauf und zieht die Angreifer seiner Kameraden auf sich.");
        soldier(SoldierType.MAN_AT_ARMS, "Man-at-Arms", "Waffenknecht", "Waffenknecht", "Heavy infantry", "Schwer gepanzert",
                "Plate armour from head to toe; arrows often glance off, and his mace also hits the foes beside his target.",
                "Plattenpanzer vom Scheitel bis zur Sohle: Pfeile gleiten oft ab, und sein Streitkolben trifft auch die Gegner neben "
                        + "dem Ziel.");
        soldier(SoldierType.LANCER, "Lancer", "Lanzenreiter", "Lanzenreiter", "Cavalry", "Reiterei",
                "Rides his own warhorse and charges through the enemy lines with his lance lowered – the faster, the harder.",
                "Reitet sein eigenes Schlachtross und galoppiert mit gesenkter Lanze durch die feindlichen Reihen – je schneller, desto "
                        + "härter.");
        soldier(SoldierType.ENGINEER, "Siege Engineer", "Belagerungsknecht", "Belagerungsknecht", "Siege expert", "Belagerung",
                "Reloads siege weapons half again as fast and repairs them – but rather weak in melee.",
                "Lädt Belagerungswaffen um die Hälfte schneller nach und bessert sie aus – im Nahkampf aber eher schwach.");
        add(ModEntities.WARHORSE.get(), pick("Warhorse", "Schlachtross"));
        text("soldier.kingdomsatwar.name", "%s %s", "%s %s");
        text("soldier.kingdomsatwar.name.ranked", "%s %s %s", "%s %s %s");
        text("soldier.kingdomsatwar.rank.0", "Recruit", "Rekrut");
        text("soldier.kingdomsatwar.rank.1", "Veteran", "Veteran");
        text("soldier.kingdomsatwar.rank.2", "Elite", "Elite");
        text("soldier.kingdomsatwar.rank.3", "Master", "Meister");
        text("soldier.kingdomsatwar.rank.4", "Legend", "Legende");
        text("message.kingdomsatwar.soldier_trained", "%s has completed the training!", "%s hat die Ausbildung abgeschlossen!");
        add(ModEntities.BANDIT.get(), pick("Bandit", "Bandit"));
        // Krieger der fremden Reiche ("Fußknecht von Eisenmark", "Eisenmark Footman")
        var warriors = ModEntities.REALM_WARRIORS;
        add(warriors.get(io.github.sirschrolli.kingdomsatwar.war.WarriorRole.FOOTMAN).get(), pick("Footman", "Fußknecht"));
        add(warriors.get(io.github.sirschrolli.kingdomsatwar.war.WarriorRole.ARCHER).get(), pick("Wall Archer", "Wallschütze"));
        add(warriors.get(io.github.sirschrolli.kingdomsatwar.war.WarriorRole.CROSSBOWMAN).get(), pick("Crossbowman", "Armbrustschütze"));
        add(warriors.get(io.github.sirschrolli.kingdomsatwar.war.WarriorRole.KNIGHT).get(), pick("Castle Knight", "Burgritter"));
        add(warriors.get(io.github.sirschrolli.kingdomsatwar.war.WarriorRole.CAPTAIN).get(), pick("Castle Captain", "Burghauptmann"));
        add(warriors.get(io.github.sirschrolli.kingdomsatwar.war.WarriorRole.LORD).get(), pick("Ruler", "Herrscher"));
        text("entity.kingdomsatwar.realm_warrior.of", "%2$s %1$s", "%1$s von %2$s");
        add(ModEntities.BANDIT_ARCHER.get(), pick("Bandit Archer", "Banditen-Bogenschütze"));
        item(ModItems.BANDIT_SPAWN_EGG, "Bandit Spawn Egg", "Banditen-Spawn-Ei");
        item(ModItems.BANDIT_ARCHER_SPAWN_EGG, "Bandit Archer Spawn Egg", "Banditen-Bogenschützen-Spawn-Ei");

        block(ModBlocks.RECRUITMENT_TABLE, "Commander's Table", "Feldherrentisch");
        text(ModItems.loreKey("recruitment_table", 1), "Heal your soldiers in the camp and give out provisions",
                "Hier heilst du deine Soldaten im Feldlager und teilst Proviant zu");
        text(ModItems.loreKey("recruitment_table", 2), "Level them up: higher ranks, knights and captains",
                "Und hier bildest du sie aus: höhere Ränge, Ritter und Hauptleute");
        item(ModItems.DRAGON_HORN, "Dragon Horn", "Drachenhorn");
        text(ModItems.loreKey("dragon_horn", 1), "Calls your dragons to war (up to 160 blocks)",
                "Ruft deine Drachen in den Krieg (bis 160 Blöcke)");
        text(ModItems.loreKey("dragon_horn", 2), "Point at an enemy: they dive at it with their breath",
                "Auf einen Feind zeigen: Sturzangriff mit dem Odem");
        text(ModItems.loreKey("dragon_horn", 3), "Point at a place: air strike on every enemy around it",
                "Auf einen Ort zeigen: Luftangriff auf jeden Feind dort");
        text(ModItems.loreKey("dragon_horn", 4), "Point at the sky or sneak: they return to you",
                "In den Himmel zeigen oder schleichen: Rückkehr");
        text("message.kingdomsatwar.dragon_horn.none", "No dragon of yours hears the horn", "Kein Drache von dir hört das Horn");
        text("message.kingdomsatwar.dragon_horn.attack", "%s dragon(s) attack %s", "%s Drache(n) greifen %s an");
        text("message.kingdomsatwar.dragon_horn.strike", "%s dragon(s) fly an air strike", "%s Drache(n) fliegen einen Luftangriff");
        text("message.kingdomsatwar.dragon_horn.recall", "%s dragon(s) return to you", "%s Drache(n) kehren zu dir zurück");
        text("message.kingdomsatwar.dragon_retreat", "%s is badly wounded and breaks off the attack",
                "%s ist schwer verwundet und bricht den Angriff ab");
        text("message.kingdomsatwar.dragon_defend", "%s dragon(s) rise to defend your settlement!",
                "%s Drache(n) steigen auf und verteidigen deine Siedlung!");
        item(ModItems.COMMAND_BATON, "Command Baton", "Kommandostab");
        text(ModItems.loreKey("command_baton", 1), "Point at an enemy: attack", "Auf einen Feind zeigen: Angriff");
        text(ModItems.loreKey("command_baton", 2), "Point at the ground: hold position (sneak: patrol)",
                "Auf den Boden zeigen: Stellung halten (Schleichen: Patrouille)");
        text(ModItems.loreKey("command_baton", 3), "Point at the sky: everyone follows", "In den Himmel zeigen: alle folgen");
        text(ModItems.loreKey("command_baton", 4), "Sneak + click your soldier: dismiss", "Schleichen + eigenen Soldaten anklicken: entlassen");
        text(ModItems.loreKey("command_baton", 5), "Sneak + point at the sky: everyone returns to the garrison",
                "Schleichen + in den Himmel zeigen: alle kehren in die Garnison heim");
        text(ModItems.loreKey("command_baton", 6), "Click a siege weapon: a soldier mans it, then point at its target",
                "Belagerungswaffe anklicken: ein Soldat bedient sie, dann aufs Ziel zeigen");

        // Fenster des Feldherrentischs
        text("gui.kingdomsatwar.recruitment.title", "Commander's Table", "Feldherrentisch");
        text("gui.kingdomsatwar.recruitment.army", "Army: %s / %s", "Armee: %s / %s");
        text("gui.kingdomsatwar.recruitment.heal", "Heal everyone", "Alle heilen");
        text("gui.kingdomsatwar.recruitment.heal.cost", "Heal everyone (%s wounded) – %s", "Alle heilen (%s verwundet) – %s");
        text("gui.kingdomsatwar.recruitment.heal.none", "Everyone in the camp is unhurt", "Im Feldlager sind alle unverletzt");
        text("gui.kingdomsatwar.recruitment.heal.none.hint", "Wounded soldiers of yours within %s blocks of the table are healed here.",
                "Hier werden deine verwundeten Soldaten im Umkreis von %s Blöcken um den Tisch geheilt.");
        text("gui.kingdomsatwar.recruitment.heal.hint",
                "The field surgeon tends to all your wounded within %s blocks: full health, no more poison or fire. "
                        + "He charges 1 bronze per %s missing health. Lancers who lost their warhorse get a new one from the "
                        + "stables (3 silver).",
                "Der Feldscher versorgt alle deine Verwundeten im Umkreis von %s Blöcken: volle Gesundheit, kein Gift, kein "
                        + "Feuer mehr. Er nimmt 1 Bronze je %s fehlende Leben. Lanzenreiter, die ihr Ross verloren haben, "
                        + "bekommen ein neues aus den Ställen (3 Silber).");
        text("gui.kingdomsatwar.recruitment.health", "%s health", "%s Leben");
        text("gui.kingdomsatwar.recruitment.armor", "%s armour", "%s Rüstung");
        text("gui.kingdomsatwar.recruitment.melee", "Sword", "Schwert");
        text("gui.kingdomsatwar.recruitment.ranged", "Bow", "Bogen");
        text("gui.kingdomsatwar.recruitment.cost", "Price:", "Preis:");
        text("gui.kingdomsatwar.recruitment.have", "You have %s", "Du hast %s");
        text("gui.kingdomsatwar.recruitment.missing", "Missing: %s× %s", "Es fehlen: %s× %s");
        text("gui.kingdomsatwar.recruitment.result.healed", "Everyone healed!", "Alle geheilt!");
        text("gui.kingdomsatwar.recruitment.result.supplied", "Provisions given out!", "Proviant verteilt!");
        text("gui.kingdomsatwar.recruitment.result.no_food", "No food here", "Kein Essen da");
        text("gui.kingdomsatwar.recruitment.result.nothing_needed", "Provisions are full", "Proviant ist voll");
        text("gui.kingdomsatwar.recruitment.result.marched", "On campaign!", "Auf zum Schlachtzug!");
        text("gui.kingdomsatwar.recruitment.result.returned", "Back to the garrison", "Zurück in die Garnison");
        text("gui.kingdomsatwar.recruitment.result.no_town", "No settlement", "Keine Siedlung");
        text("gui.kingdomsatwar.recruitment.result.no_soldiers", "Nobody here", "Niemand hier");
        text("message.kingdomsatwar.recruit.supplied", "Your soldiers in the camp have their provisions.", "Deine Soldaten im Feldlager haben ihren Proviant.");
        text("message.kingdomsatwar.recruit.no_food", "There is no food – neither in the storage of your settlement nor in your pack.",
                "Es ist kein Essen da – weder im Lager deiner Siedlung noch in deinem Gepäck.");
        text("message.kingdomsatwar.recruit.nothing_needed", "Everyone in the camp already has full provisions.",
                "Alle im Feldlager haben schon vollen Proviant.");
        text("message.kingdomsatwar.recruit.marched", "Your soldiers march with you.", "Deine Soldaten ziehen mit dir.");
        text("message.kingdomsatwar.recruit.returned", "Your soldiers return to the garrison.", "Deine Soldaten kehren in die Garnison zurück.");
        text("message.kingdomsatwar.recruit.no_town", "Without a settlement there is no garrison.", "Ohne Siedlung gibt es keine Garnison.");
        text("message.kingdomsatwar.recruit.no_soldiers", "None of your soldiers is here.", "Keiner deiner Soldaten ist hier.");
        text("gui.kingdomsatwar.recruitment.provisions", "Give out provisions", "Proviant zuteilen");
        text("gui.kingdomsatwar.recruitment.provisions.cost", "Give out %s rations", "%s Rationen zuteilen");
        text("gui.kingdomsatwar.recruitment.provisions.full", "Everyone has full provisions", "Alle haben vollen Proviant");
        text("gui.kingdomsatwar.recruitment.provisions.hint",
                "Every soldier in the camp gets rations for up to %s days – from the storage of your settlement (if the table "
                        + "stands in it) and from your pack. In the garrison they eat from the storage of your settlement, on "
                        + "campaign they live on their rations. Whoever has nothing goes hungry: no more healing on his own and "
                        + "weaker blows.",
                "Jeder Soldat im Feldlager bekommt Rationen für bis zu %s Tage – aus dem Lager deiner Siedlung (wenn der Tisch "
                        + "darin steht) und aus deinem Gepäck. In der Garnison essen sie aus dem Stadtlager, auf dem Schlachtzug "
                        + "von ihrem Proviant. Wer nichts hat, hungert: Er heilt nicht mehr von selbst und schlägt schwächer zu.");
        text("gui.kingdomsatwar.recruitment.march", "Go on campaign", "Auf Schlachtzug");
        text("gui.kingdomsatwar.recruitment.march.count", "Go on campaign (%s)", "Auf Schlachtzug (%s)");
        text("gui.kingdomsatwar.recruitment.march.hint", "All soldiers of the garrison here in the camp follow you until you send them home.",
                "Alle Soldaten der Garnison hier im Feldlager folgen dir, bis du sie heimschickst.");
        text("gui.kingdomsatwar.recruitment.return.count", "Send home (%s)", "Heimkehr (%s)");
        text("gui.kingdomsatwar.recruitment.return.hint", "They return to the garrison of your settlement and patrol there.",
                "Sie kehren in die Garnison deiner Siedlung zurück und gehen dort Streife.");
        text("gui.kingdomsatwar.recruitment.return.no_town", "Without a settlement there is no garrison – found one with a town banner.",
                "Ohne Siedlung gibt es keine Garnison – gründe eine mit einem Stadtbanner.");
        text("gui.kingdomsatwar.recruitment.rations", "Provisions for %s days", "Proviant für %s Tage");
        text("gui.kingdomsatwar.recruitment.rations.hungry", "Hungry – no provisions left", "Hungrig – kein Proviant mehr");
        text("gui.kingdomsatwar.recruitment.on_campaign", " – on campaign", " – auf dem Schlachtzug");
        text("gui.kingdomsatwar.recruitment.in_garrison", " – in the garrison", " – in der Garnison");
        text("gui.kingdomsatwar.recruitment.no_garrison", " – follows you (no settlement)", " – folgt dir (keine Siedlung)");
        text("message.kingdomsatwar.soldier_hungry", "%s is hungry – fill the storage of your settlement or give out provisions at "
                + "your commander's table!", "%s hat Hunger – fülle das Lager deiner Siedlung oder teile am Feldherrentisch Proviant zu!");
        text("message.kingdomsatwar.garrison.joined", "%s moves into the garrison of %s.", "%s zieht in die Garnison von %s.");
        text("message.kingdomsatwar.garrison.no_town", "You have no settlement – there is no garrison to return to.",
                "Du hast keine Siedlung – es gibt keine Garnison, in die sie heimkehren könnten.");
        text("message.kingdomsatwar.garrison.sent_home", "%s soldiers return to the garrison of %s.", "%s Soldaten kehren in die Garnison von %s heim.");
        text("gui.kingdomsatwar.recruitment.result.nothing_to_heal", "Nobody is wounded", "Niemand ist verwundet");
        text("gui.kingdomsatwar.recruitment.result.too_far", "Too far away", "Zu weit weg");
        text("gui.kingdomsatwar.recruitment.result.not_enough", "Too expensive", "Zu teuer");
        text("gui.kingdomsatwar.recruitment.result.army_full", "Army full", "Armee voll");
        text("gui.kingdomsatwar.recruitment.result.no_space", "No room", "Kein Platz");
        text("message.kingdomsatwar.recruit.healed", "The field surgeon has tended to all your wounded.",
                "Der Feldscher hat alle deine Verwundeten versorgt.");
        text("message.kingdomsatwar.recruit.nothing_to_heal", "None of your soldiers in the camp is wounded.",
                "Keiner deiner Soldaten im Feldlager ist verwundet.");
        text("message.kingdomsatwar.recruit.too_far", "You are too far away from the table.", "Du bist zu weit vom Tisch entfernt.");
        text("message.kingdomsatwar.recruit.not_enough", "You can't afford this soldier yet.", "Diesen Soldaten kannst du dir noch nicht leisten.");
        text("message.kingdomsatwar.recruit.army_full", "Your army is complete – you can't lead more soldiers.",
                "Deine Armee ist vollzählig – mehr Soldaten kannst du nicht führen.");
        text("message.kingdomsatwar.recruit.no_space", "There is no room next to the table.", "Neben dem Tisch ist kein Platz.");
        text("message.kingdomsatwar.recruit.trained", "Promoted!", "Befördert!");
        text("message.kingdomsatwar.recruit.promoted", "Training complete!", "Ausbildung abgeschlossen!");
        text("message.kingdomsatwar.recruit.max_rank", "This soldier can't be trained any further.",
                "Dieser Soldat lässt sich nicht weiter ausbilden.");
        text("message.kingdomsatwar.recruit.not_here", "That soldier isn't here (any more).", "Dieser Soldat ist nicht (mehr) hier.");
        text("message.kingdomsatwar.recruit.hired", "Hired! Your mercenary joins your army.", "Angeheuert! Dein Söldner tritt in dein Heer ein.");
        text("gui.kingdomsatwar.recruitment.result.trained", "Promoted!", "Befördert!");
        text("gui.kingdomsatwar.recruitment.result.promoted", "Trained!", "Ausgebildet!");
        text("gui.kingdomsatwar.recruitment.result.max_rank", "Highest rank", "Höchster Rang");
        text("gui.kingdomsatwar.recruitment.result.not_here", "Not here any more", "Nicht mehr da");
        text("gui.kingdomsatwar.recruitment.result.hired", "Hired!", "Angeheuert!");

        // Ausbilden am Feldherrentisch
        text("gui.kingdomsatwar.recruitment.villages_hint",
                "New soldiers are not recruited here: hire the knights and mercenaries waiting in the mercenary halls of the "
                        + "villages. Then bring them to your camp to heal them and level them up.",
                "Neue Soldaten wirbst du hier nicht an: Heuere die Ritter und Söldner an, die im Söldnerhaus der Dörfer "
                        + "warten. Im Feldlager heilst du sie dann und bildest sie aus.");
        text("gui.kingdomsatwar.recruitment.no_soldiers", "None of your soldiers is in the camp (up to %s blocks around the table).",
                "Keiner deiner Soldaten ist im Feldlager (bis %s Blöcke um den Tisch).");
        text("gui.kingdomsatwar.recruitment.no_soldier", "No soldier in the camp", "Kein Soldat im Feldlager");
        text("gui.kingdomsatwar.recruitment.rank_up", "Promote", "Befördern");
        text("gui.kingdomsatwar.recruitment.rank_up.hint", "Promote to %s: +25%% health, +2 armour, +1 damage",
                "Befördern zum Rang %s: +25 %% Leben, +2 Rüstung, +1 Schaden");
        text("gui.kingdomsatwar.recruitment.max_rank", "Already a legend – the highest rank", "Schon Legende – der höchste Rang");
        text("gui.kingdomsatwar.recruitment.max_rank.short", "Highest rank reached", "Höchster Rang erreicht");
        text("gui.kingdomsatwar.recruitment.promote", "Train", "Ausbilden");
        text("gui.kingdomsatwar.recruitment.promote_to", "To %s", "Zum %s");
        text("gui.kingdomsatwar.recruitment.promote.hint", "Train as %s – he keeps his name and rank",
                "Ausbilden als %s – Name und Rang bleiben");
        text("gui.kingdomsatwar.recruitment.promote.none", "This kind of soldier can't be trained any further",
                "Diese Art lässt sich nicht weiter ausbilden");
        text("gui.kingdomsatwar.recruitment.promote.none.short", "No further training", "Keine weitere Ausbildung");
        text("gui.kingdomsatwar.recruitment.rank_cost", "Promotion:", "Befördern:");
        text("gui.kingdomsatwar.recruitment.promote_cost", "Training:", "Ausbildung:");

        // Söldner in den Dörfern
        block(ModBlocks.MERCENARY_BOARD, "Mercenary Board", "Söldnertafel");
        text("sign.kingdomsatwar.mercenaries", "Mercenaries", "Söldner");
        text("gui.kingdomsatwar.mercenaries.title", "Mercenary Board", "Söldnertafel");
        text("gui.kingdomsatwar.mercenaries.title.village", "Mercenaries of %s", "Söldner von %s");
        text("gui.kingdomsatwar.mercenaries.hire", "Hire", "Anheuern");
        text("gui.kingdomsatwar.mercenaries.hire.hint", "Hire %s for %s", "%s für %s anheuern");
        text("gui.kingdomsatwar.mercenaries.none", "Nobody is waiting here today. New mercenaries arrive every morning.",
                "Heute wartet hier niemand mehr. Jeden Morgen kommen neue Söldner.");
        text("gui.kingdomsatwar.mercenaries.hint", "You can heal, train and promote them later at your commander's table.",
                "Heilen, ausbilden und befördern kannst du sie später an deinem Feldherrentisch.");
        text("gui.kingdomsatwar.mercenaries.wage", "Wage:", "Sold:");

        // Befehle und Zustand
        text("message.kingdomsatwar.soldier_order.0", "%s follows you", "%s folgt dir");
        text("message.kingdomsatwar.soldier_order.1", "%s holds this position", "%s hält diese Stellung");
        text("message.kingdomsatwar.soldier_order.2", "%s patrols here", "%s patrouilliert hier");
        text("message.kingdomsatwar.soldier_mode.0", "following", "folgt");
        text("message.kingdomsatwar.soldier_mode.1", "holding position", "hält Stellung");
        text("message.kingdomsatwar.soldier_mode.2", "on patrol", "auf Patrouille");
        text("message.kingdomsatwar.soldier_status", "%s – %s/%s health – %s – %s", "%s – %s/%s Leben – %s – %s");
        text("message.kingdomsatwar.soldier_price", "%s serves for %s – hold a coin and use him", "%s dient für %s – nimm eine Münze in die Hand und sprich ihn an");
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
