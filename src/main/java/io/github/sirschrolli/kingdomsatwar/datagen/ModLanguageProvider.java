package io.github.sirschrolli.kingdomsatwar.datagen;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import io.github.sirschrolli.kingdomsatwar.registry.ModBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.ModItems;
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
        text("itemGroup.kingdomsatwar.building", "Kingdoms at War: Castles & Building", "Kingdoms at War: Burgen & Bauen");
        text("itemGroup.kingdomsatwar.resources", "Dragon Warfare: Ores & Resources", "Dragon Warfare: Erze & Rohstoffe");
        text("itemGroup.kingdomsatwar.dragons", "Dragon Warfare: Dragons", "Dragon Warfare: Drachen");
        text("itemGroup.kingdomsatwar.farming", "Kingdoms at War: Farming & Food", "Kingdoms at War: Landwirtschaft & Nahrung");

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
        lore("steel_ingot", "For future weapons and armour", "Für künftige Waffen und Rüstungen");

        // --- Drachen ---
        egg(DragonType.FIRE, "Fire Dragon Egg", "Feuerdrachen-Ei",
                "The power of the flames", "Die Macht der Flammen",
                "Habitat: volcanic regions, lava caves", "Lebensraum: Vulkanregionen, Lava-Höhlen");
        egg(DragonType.ICE, "Ice Dragon Egg", "Eisdrachen-Ei",
                "The cold of eternity", "Die Kälte der Ewigkeit",
                "Habitat: snowy regions, ice mountains", "Lebensraum: Schneeregionen, Eisberge");
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
        for (DragonType type : DragonType.values()) {
            text(ModItems.loreKey(type.id() + "_dragon_egg", 3), "Hatching follows in a later version",
                    "Ausbrüten folgt in einer späteren Version");
        }

        item(ModItems.DRAGON_SCALES.get(DragonType.FIRE), "Fire Dragon Scale", "Feuerdrachen-Schuppe");
        item(ModItems.DRAGON_SCALES.get(DragonType.ICE), "Ice Dragon Scale", "Eisdrachen-Schuppe");
        item(ModItems.DRAGON_SCALES.get(DragonType.LIGHTNING), "Lightning Dragon Scale", "Blitzdrachen-Schuppe");
        item(ModItems.DRAGON_SCALES.get(DragonType.NATURE), "Nature Dragon Scale", "Naturdrachen-Schuppe");
        item(ModItems.DRAGON_SCALES.get(DragonType.EARTH), "Earth Dragon Scale", "Erddrachen-Schuppe");
        item(ModItems.DRAGON_SCALES.get(DragonType.WATER), "Water Dragon Scale", "Wasserdrachen-Schuppe");
        lore("dragon_scale", "For dragon armour (coming with the dragons)", "Für Drachenrüstungen (kommt mit den Drachen)");
        item(ModItems.DRAGON_EGG_FRAGMENT, "Dragon Egg Fragment", "Drachenei-Fragment");
        lore("dragon_egg_fragment", "Very rare – used for dragon eggs", "Sehr selten – für Dracheneier");
        item(ModItems.DRAGON_FOOD, "Dragon Food", "Drachenfutter");
        lore("dragon_food", "Will be used to tame dragons", "Wird später zum Zähmen von Drachen gebraucht");

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

    private void egg(DragonType type, String nameEn, String nameDe, String line1En, String line1De,
            String line2En, String line2De) {
        String id = type.id() + "_dragon_egg";
        block(ModBlocks.DRAGON_EGGS.get(type), nameEn, nameDe);
        text(ModItems.loreKey(id, 1), line1En, line1De);
        text(ModItems.loreKey(id, 2), line2En, line2De);
    }
}
