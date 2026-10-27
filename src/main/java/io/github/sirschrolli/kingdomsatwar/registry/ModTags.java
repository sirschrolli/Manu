package io.github.sirschrolli.kingdomsatwar.registry;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;

/**
 * Eigene Tags. Die {@code c:}-Tags sind die gemeinsamen Tags aller NeoForge-Mods –
 * so können andere Mods im Modpack unsere Erze und Rohstoffe erkennen.
 */
public final class ModTags {
    public static final class Blocks {
        public static final TagKey<Block> DRAGON_EGGS = mod("dragon_eggs");
        /** Stämme und Holz der Schattenbäume (für die Bretter). */
        public static final TagKey<Block> SHADOW_LOGS = mod("shadow_logs");
        /** Gestein der Schattenwelt, in dem Erze entstehen können. */
        public static final TagKey<Block> SHADOW_STONE_ORE_REPLACEABLES = mod("shadow_stone_ore_replaceables");
        public static final TagKey<Block> DEEP_SHADOW_STONE_ORE_REPLACEABLES = mod("deep_shadow_stone_ore_replaceables");
        public static final TagKey<Block> ORES_MOONSILVER = common("ores/moonsilver");
        public static final TagKey<Block> ORES_NIGHTSTEEL = common("ores/nightsteel");
        public static final TagKey<Block> ORES_SOUL_CRYSTAL = common("ores/soul_crystal");
        public static final TagKey<Block> ORES_ABYSSITE = common("ores/abyssite");
        public static final TagKey<Block> STORAGE_BLOCKS_MOONSILVER = common("storage_blocks/moonsilver");
        public static final TagKey<Block> STORAGE_BLOCKS_NIGHTSTEEL = common("storage_blocks/nightsteel");
        public static final TagKey<Block> STORAGE_BLOCKS_SOUL_CRYSTAL = common("storage_blocks/soul_crystal");
        public static final TagKey<Block> STORAGE_BLOCKS_ABYSSITE = common("storage_blocks/abyssite");
        public static final TagKey<Block> ORES_CARBON = common("ores/carbon");
        public static final TagKey<Block> ORES_GEMSTONE = common("ores/gemstone");
        public static final TagKey<Block> ORES_KONTERIT = common("ores/konterit");
        public static final TagKey<Block> ORES_AETHERIT = common("ores/aetherit");
        public static final TagKey<Block> ORES_DRACONIUM = common("ores/draconium");
        public static final TagKey<Block> ORES_WYVERNIT = common("ores/wyvernit");
        public static final TagKey<Block> STORAGE_BLOCKS_CARBON = common("storage_blocks/carbon");
        public static final TagKey<Block> STORAGE_BLOCKS_GEMSTONE = common("storage_blocks/gemstone");
        public static final TagKey<Block> STORAGE_BLOCKS_KONTERIT = common("storage_blocks/konterit");
        public static final TagKey<Block> STORAGE_BLOCKS_AETHERIT = common("storage_blocks/aetherit");
        public static final TagKey<Block> STORAGE_BLOCKS_DRACONIUM = common("storage_blocks/draconium");
        public static final TagKey<Block> STORAGE_BLOCKS_WYVERNIT = common("storage_blocks/wyvernit");
        public static final TagKey<Block> STORAGE_BLOCKS_STEEL = common("storage_blocks/steel");

        private static TagKey<Block> mod(String path) {
            return TagKey.create(Registries.BLOCK, KingdomsAtWar.id(path));
        }

        private static TagKey<Block> common(String path) {
            return TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath("c", path));
        }

        private Blocks() {}
    }

    public static final class EntityTypes {
        /** Wesen der Schattenwelt (die Mondsilberklinge trifft sie besonders hart). */
        public static final TagKey<net.minecraft.world.entity.EntityType<?>> SHADOW_CREATURES = TagKey.create(Registries.ENTITY_TYPE,
                KingdomsAtWar.id("shadow_creatures"));
        /** Monster, die Münzen fallen lassen, wenn ein Spieler sie besiegt. */
        public static final TagKey<net.minecraft.world.entity.EntityType<?>> DROPS_COINS = TagKey.create(Registries.ENTITY_TYPE,
                KingdomsAtWar.id("drops_coins"));

        private EntityTypes() {}
    }

    public static final class Biomes {
        /** Hier ziehen nachts Banditen umher. */
        public static final TagKey<Biome> HAS_BANDITS = TagKey.create(Registries.BIOME, KingdomsAtWar.id("has_bandits"));
        public static final TagKey<Biome> HAS_WILD_BOARS = TagKey.create(Registries.BIOME, KingdomsAtWar.id("has_wild_boars"));
        public static final TagKey<Biome> HAS_DEER = TagKey.create(Registries.BIOME, KingdomsAtWar.id("has_deer"));
        public static final TagKey<Biome> HAS_GEESE = TagKey.create(Registries.BIOME, KingdomsAtWar.id("has_geese"));
        public static final TagKey<Biome> HAS_WILL_O_WISPS = TagKey.create(Registries.BIOME, KingdomsAtWar.id("has_will_o_wisps"));
        public static final TagKey<Biome> HAS_CAVE_TROLLS = TagKey.create(Registries.BIOME, KingdomsAtWar.id("has_cave_trolls"));
        public static final TagKey<Biome> HAS_LINDWORMS = TagKey.create(Registries.BIOME, KingdomsAtWar.id("has_lindworms"));
        /** Wo Drachennester der jeweiligen Art entstehen. */
        public static final java.util.Map<io.github.sirschrolli.kingdomsatwar.dragon.DragonType, TagKey<Biome>> HAS_DRAGON_NESTS = nestTags();
        public static final TagKey<Biome> HAS_CASTLE_RUINS = TagKey.create(Registries.BIOME, KingdomsAtWar.id("has_castle_ruins"));
        /** Wo der Turm des Erzmagiers eines Elements stehen kann. */
        public static final java.util.Map<io.github.sirschrolli.kingdomsatwar.dragon.DragonType, TagKey<Biome>> HAS_MAGE_TOWERS = towerTags();
        /** Wo der Siegelkreis liegen kann (ebenes, offenes Land). */
        public static final TagKey<Biome> HAS_SEAL_CIRCLE = TagKey.create(Registries.BIOME, KingdomsAtWar.id("has_seal_circle"));
        /** Wo in der Schattenwelt Dörfer der Umbrer liegen können. */
        public static final TagKey<Biome> HAS_UMBRER_VILLAGE = TagKey.create(Registries.BIOME, KingdomsAtWar.id("has_umbrer_village"));
        /** Wo Nyxars Schattenzitadelle stehen kann (weites, festes Land der Schattenwelt). */
        /** Bauwerke der Schattenwelt: verlassene Mondtempel, Kristallminen, Leuchttürme an der Küste, Ruinen der alten Welt. */
        public static final TagKey<Biome> HAS_MOON_TEMPLE = TagKey.create(Registries.BIOME, KingdomsAtWar.id("has_moon_temple"));
        public static final TagKey<Biome> HAS_CRYSTAL_MINE = TagKey.create(Registries.BIOME, KingdomsAtWar.id("has_crystal_mine"));
        public static final TagKey<Biome> HAS_LIGHTHOUSE = TagKey.create(Registries.BIOME, KingdomsAtWar.id("has_lighthouse"));
        public static final TagKey<Biome> HAS_OLD_WORLD_RUIN = TagKey.create(Registries.BIOME, KingdomsAtWar.id("has_old_world_ruin"));
        public static final TagKey<Biome> HAS_SHADOW_CITADEL = TagKey.create(Registries.BIOME, KingdomsAtWar.id("has_shadow_citadel"));

        private static java.util.Map<io.github.sirschrolli.kingdomsatwar.dragon.DragonType, TagKey<Biome>> nestTags() {
            java.util.Map<io.github.sirschrolli.kingdomsatwar.dragon.DragonType, TagKey<Biome>> tags =
                    new java.util.EnumMap<>(io.github.sirschrolli.kingdomsatwar.dragon.DragonType.class);
            for (io.github.sirschrolli.kingdomsatwar.dragon.DragonType type : io.github.sirschrolli.kingdomsatwar.dragon.DragonType.values()) {
                tags.put(type, TagKey.create(Registries.BIOME, KingdomsAtWar.id("has_" + type.id() + "_dragon_nests")));
            }
            return java.util.Collections.unmodifiableMap(tags);
        }

        private static java.util.Map<io.github.sirschrolli.kingdomsatwar.dragon.DragonType, TagKey<Biome>> towerTags() {
            java.util.Map<io.github.sirschrolli.kingdomsatwar.dragon.DragonType, TagKey<Biome>> tags =
                    new java.util.EnumMap<>(io.github.sirschrolli.kingdomsatwar.dragon.DragonType.class);
            for (io.github.sirschrolli.kingdomsatwar.dragon.DragonType type : io.github.sirschrolli.kingdomsatwar.dragon.DragonType.values()) {
                tags.put(type, TagKey.create(Registries.BIOME, KingdomsAtWar.id("has_" + type.id() + "_mage_tower")));
            }
            return java.util.Collections.unmodifiableMap(tags);
        }

        private Biomes() {}
    }

    public static final class Items {
        public static final TagKey<Item> STEEL_TOOL_MATERIALS = mod("steel_tool_materials");
        public static final TagKey<Item> DRACONIUM_TOOL_MATERIALS = mod("draconium_tool_materials");
        public static final TagKey<Item> REPAIRS_STEEL_ARMOR = mod("repairs_steel_armor");
        public static final TagKey<Item> REPAIRS_KONTERIT_ARMOR = mod("repairs_konterit_armor");
        public static final TagKey<Item> REPAIRS_CROWN = mod("repairs_gemstone_crown");
        public static final TagKey<Item> REPAIRS_GLIDER = mod("repairs_wyvernit_glider");
        public static final TagKey<Item> REPAIRS_DRACONIUM = mod("repairs_draconium");
        /** Alle Rüstungsteile aus Drachenschuppen. */
        public static final TagKey<Item> DRAGON_SCALE_ARMOR = mod("dragon_scale_armor");

        public static TagKey<Item> scaleArmorRepair(io.github.sirschrolli.kingdomsatwar.dragon.DragonType type) {
            return mod("repairs_" + type.id() + "_dragon_scale_armor");
        }

        public static final TagKey<Item> DRAGON_EGGS = mod("dragon_eggs");
        public static final TagKey<Item> SHADOW_LOGS = mod("shadow_logs");
        public static final TagKey<Item> NIGHTSTEEL_TOOL_MATERIALS = mod("nightsteel_tool_materials");
        public static final TagKey<Item> ABYSSITE_TOOL_MATERIALS = mod("abyssite_tool_materials");
        public static final TagKey<Item> MOONSILVER_TOOL_MATERIALS = mod("moonsilver_tool_materials");
        public static final TagKey<Item> REPAIRS_NIGHTSTEEL_ARMOR = mod("repairs_nightsteel_armor");
        public static final TagKey<Item> REPAIRS_ABYSSITE_ARMOR = mod("repairs_abyssite_armor");
        public static final TagKey<Item> ORES_MOONSILVER = common("ores/moonsilver");
        public static final TagKey<Item> ORES_NIGHTSTEEL = common("ores/nightsteel");
        public static final TagKey<Item> ORES_SOUL_CRYSTAL = common("ores/soul_crystal");
        public static final TagKey<Item> ORES_ABYSSITE = common("ores/abyssite");
        public static final TagKey<Item> INGOTS_MOONSILVER = common("ingots/moonsilver");
        public static final TagKey<Item> INGOTS_NIGHTSTEEL = common("ingots/nightsteel");
        public static final TagKey<Item> INGOTS_ABYSSITE = common("ingots/abyssite");
        public static final TagKey<Item> NUGGETS_MOONSILVER = common("nuggets/moonsilver");
        public static final TagKey<Item> RAW_MATERIALS_MOONSILVER = common("raw_materials/moonsilver");
        public static final TagKey<Item> RAW_MATERIALS_NIGHTSTEEL = common("raw_materials/nightsteel");
        public static final TagKey<Item> GEMS_SOUL_CRYSTAL = common("gems/soul_crystal");
        public static final TagKey<Item> STORAGE_BLOCKS_MOONSILVER = common("storage_blocks/moonsilver");
        public static final TagKey<Item> STORAGE_BLOCKS_NIGHTSTEEL = common("storage_blocks/nightsteel");
        public static final TagKey<Item> STORAGE_BLOCKS_SOUL_CRYSTAL = common("storage_blocks/soul_crystal");
        public static final TagKey<Item> STORAGE_BLOCKS_ABYSSITE = common("storage_blocks/abyssite");
        public static final TagKey<Item> DRAGON_SCALES = mod("dragon_scales");
        public static final TagKey<Item> ORES_CARBON = common("ores/carbon");
        public static final TagKey<Item> ORES_GEMSTONE = common("ores/gemstone");
        public static final TagKey<Item> ORES_KONTERIT = common("ores/konterit");
        public static final TagKey<Item> ORES_AETHERIT = common("ores/aetherit");
        public static final TagKey<Item> ORES_DRACONIUM = common("ores/draconium");
        public static final TagKey<Item> ORES_WYVERNIT = common("ores/wyvernit");
        public static final TagKey<Item> STORAGE_BLOCKS_CARBON = common("storage_blocks/carbon");
        public static final TagKey<Item> STORAGE_BLOCKS_GEMSTONE = common("storage_blocks/gemstone");
        public static final TagKey<Item> STORAGE_BLOCKS_KONTERIT = common("storage_blocks/konterit");
        public static final TagKey<Item> STORAGE_BLOCKS_AETHERIT = common("storage_blocks/aetherit");
        public static final TagKey<Item> STORAGE_BLOCKS_DRACONIUM = common("storage_blocks/draconium");
        public static final TagKey<Item> STORAGE_BLOCKS_WYVERNIT = common("storage_blocks/wyvernit");
        public static final TagKey<Item> STORAGE_BLOCKS_STEEL = common("storage_blocks/steel");
        public static final TagKey<Item> GEMS_GEMSTONE = common("gems/gemstone");
        public static final TagKey<Item> GEMS_KONTERIT = common("gems/konterit");
        public static final TagKey<Item> GEMS_AETHERIT = common("gems/aetherit");
        public static final TagKey<Item> GEMS_DRACONIUM = common("gems/draconium");
        public static final TagKey<Item> GEMS_WYVERNIT = common("gems/wyvernit");
        public static final TagKey<Item> INGOTS_STEEL = common("ingots/steel");
        public static final TagKey<Item> CROPS_TOMATO = common("crops/tomato");
        public static final TagKey<Item> CROPS_CABBAGE = common("crops/cabbage");
        public static final TagKey<Item> CROPS_ONION = common("crops/onion");
        public static final TagKey<Item> CROPS_BARLEY = common("crops/barley");
        public static final TagKey<Item> CROPS_HOPS = common("crops/hops");
        public static final TagKey<Item> CROPS_GRAPE = common("crops/grape");
        public static final TagKey<Item> CROPS_GARLIC = common("crops/garlic");
        public static final TagKey<Item> SEEDS_TOMATO = common("seeds/tomato");
        public static final TagKey<Item> SEEDS_CABBAGE = common("seeds/cabbage");
        public static final TagKey<Item> SEEDS_BARLEY = common("seeds/barley");

        private static TagKey<Item> mod(String path) {
            return TagKey.create(Registries.ITEM, KingdomsAtWar.id(path));
        }

        private static TagKey<Item> common(String path) {
            return TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", path));
        }

        private Items() {}
    }

    private ModTags() {}
}
