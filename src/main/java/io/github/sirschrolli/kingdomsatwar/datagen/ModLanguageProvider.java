package io.github.sirschrolli.kingdomsatwar.datagen;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierType;
import io.github.sirschrolli.kingdomsatwar.registry.ModBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.ModItems;
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
        // --- Kreativ-Tabs ---
        // (kurz halten: längere Titel ragen im Kreativ-Inventar über den Rand)
        text("itemGroup.kingdomsatwar.building", "Kingdoms at War: Building", "Kingdoms at War: Bauen");
        text("itemGroup.kingdomsatwar.resources", "Kingdoms at War: Resources", "Kingdoms at War: Rohstoffe");
        text("itemGroup.kingdomsatwar.dragons", "Kingdoms at War: Creatures", "Kingdoms at War: Kreaturen");
        text("itemGroup.kingdomsatwar.farming", "Kingdoms at War: Food", "Kingdoms at War: Nahrung");
        text("itemGroup.kingdomsatwar.kingdom", "Kingdoms at War: Army", "Kingdoms at War: Armee");
        text("itemGroup.kingdomsatwar.equipment", "Kingdoms at War: Equipment", "Kingdoms at War: Ausrüstung");

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
        item(ModItems.DRAGON_EGG_FRAGMENT, "Dragon Egg Fragment", "Drachenei-Fragment");
        lore("dragon_egg_fragment", "Very rare – used for dragon eggs", "Sehr selten – für Dracheneier");
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
