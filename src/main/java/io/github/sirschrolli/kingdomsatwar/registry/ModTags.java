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
        /** Natürlicher Boden der eigenen Biome (Vulkangestein, Asche, Reifstein, Torf …): darauf dürfen Bäume und Felsen stehen. */
        public static final TagKey<Block> NATURAL_GROUND = mod("natural_ground");
        /** Stämme und Holz der Schattenbäume (für die Bretter). */
        public static final TagKey<Block> SHADOW_LOGS = mod("shadow_logs");
        /** Glimmerholz-Stämme (Feenreich). */
        public static final TagKey<Block> GLIMMERWOOD_LOGS = mod("glimmerwood_logs");
        /** Bruchstein aus eigenem Gestein (für Steinwerkzeuge, Öfen und Braustände). */
        public static final TagKey<Block> COBBLED_STONES = mod("cobbled_stones");
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

        /** Was Belagerungswaffen nie zerschlagen (Stadtbanner, Siegelkreis, Tore der Welten, Bannsiegel der Magier …). */
        public static final TagKey<Block> SIEGE_PROOF = mod("siege_proof");
        /** Dragon Architect: was beim Vermessen nicht in den Bauplan kommt (Spawner, Portale, Grundgestein, Stadtbanner …). */
        public static final TagKey<Block> ARCHITECT_UNBUILDABLE = mod("architect/unbuildable");
        /**
         * Dragon Architect: Kostbares, das der Baumeister nicht aus Baustoff-Einheiten macht, sondern genau so aus dem Lager
         * nimmt (Erze, Edelstein- und Metallblöcke, Obsidian, Amboss, Leuchtfeuer …).
         */
        public static final TagKey<Block> ARCHITECT_PRECIOUS = mod("architect/precious");
        /** Dragon Architect: gewachsener Boden (Erde, Gras, Fels, Sand, Kies …) – unter der Bodenlinie bleibt er, wie er ist. */
        public static final TagKey<Block> ARCHITECT_TERRAIN = mod("architect/terrain");

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
        /** Drachen aller Art: Drachen, Drachen-Bosse, Lindwürmer (Drachentöter trifft sie hart, Drachenschuppe schützt vor ihnen). */
        public static final TagKey<net.minecraft.world.entity.EntityType<?>> DRAGONS = TagKey.create(Registries.ENTITY_TYPE,
                KingdomsAtWar.id("dragons"));
        /** Wesen des Feenreichs (Feen, Irrlichter, Pilzlinge, Dornwichte …). */
        public static final TagKey<net.minecraft.world.entity.EntityType<?>> FAE_CREATURES = TagKey.create(Registries.ENTITY_TYPE,
                KingdomsAtWar.id("fae_creatures"));
        /** Werwesen (der Werwolf): Mondsilber trifft sie besonders hart. */
        public static final TagKey<net.minecraft.world.entity.EntityType<?>> WEREBEASTS = TagKey.create(Registries.ENTITY_TYPE,
                KingdomsAtWar.id("werebeasts"));
        /** Wer sich an Dornenhecken nicht sticht (die Wesen der Dornenwildnis). */
        public static final TagKey<net.minecraft.world.entity.EntityType<?>> THORN_IMMUNE = TagKey.create(Registries.ENTITY_TYPE,
                KingdomsAtWar.id("thorn_immune"));

        private EntityTypes() {}
    }

    /** Verzauberungs-Tags: wo die Verzauberungen der Mod als Beute liegen. */
    public static final class Enchantments {
        /** Schatzkammern und Herrscher der Reiche: Lebensraub, Rüstungsbrecher, Pfeilhagel, Windschritt. */
        public static final TagKey<net.minecraft.world.item.enchantment.Enchantment> LOOT_REALM = TagKey.create(Registries.ENCHANTMENT,
                KingdomsAtWar.id("loot/realm"));
        /** Drachen: Drachentöter und Drachenschuppe. */
        public static final TagKey<net.minecraft.world.item.enchantment.Enchantment> LOOT_DRAGON = TagKey.create(Registries.ENCHANTMENT,
                KingdomsAtWar.id("loot/dragon"));
        /** Erzmagier und ihre Türme: Frostklinge, Lebensraub, Windschritt. */
        public static final TagKey<net.minecraft.world.item.enchantment.Enchantment> LOOT_ARCANE = TagKey.create(Registries.ENCHANTMENT,
                KingdomsAtWar.id("loot/arcane"));

        private Enchantments() {}
    }

    public static final class Biomes {
        /** Hier ziehen nachts Banditen umher. */
        public static final TagKey<Biome> HAS_BANDITS = TagKey.create(Registries.BIOME, KingdomsAtWar.id("has_bandits"));
        /** Eigene Hof- und Weidetiere (wo Vanilla die entsprechenden Tiere hatte, mit gleichen Gewichten). */
        public static final TagKey<Biome> HAS_CATTLE = TagKey.create(Registries.BIOME, KingdomsAtWar.id("has_cattle"));
        public static final TagKey<Biome> HAS_MUSHROOM_CATTLE = TagKey.create(Registries.BIOME, KingdomsAtWar.id("has_mushroom_cattle"));
        public static final TagKey<Biome> HAS_PIGS = TagKey.create(Registries.BIOME, KingdomsAtWar.id("has_pigs"));
        public static final TagKey<Biome> HAS_SHEEP = TagKey.create(Registries.BIOME, KingdomsAtWar.id("has_sheep"));
        public static final TagKey<Biome> HAS_FEW_SHEEP = TagKey.create(Registries.BIOME, KingdomsAtWar.id("has_few_sheep"));
        public static final TagKey<Biome> HAS_CHICKENS = TagKey.create(Registries.BIOME, KingdomsAtWar.id("has_chickens"));
        public static final TagKey<Biome> HAS_HORSES = TagKey.create(Registries.BIOME, KingdomsAtWar.id("has_horses"));
        public static final TagKey<Biome> HAS_FEW_HORSES = TagKey.create(Registries.BIOME, KingdomsAtWar.id("has_few_horses"));
        public static final TagKey<Biome> HAS_DONKEYS = TagKey.create(Registries.BIOME, KingdomsAtWar.id("has_donkeys"));
        public static final TagKey<Biome> HAS_GOATS = TagKey.create(Registries.BIOME, KingdomsAtWar.id("has_goats"));
        public static final TagKey<Biome> HAS_FEW_GOATS = TagKey.create(Registries.BIOME, KingdomsAtWar.id("has_few_goats"));
        public static final TagKey<Biome> HAS_MANY_HARES = TagKey.create(Registries.BIOME, KingdomsAtWar.id("has_many_hares"));
        public static final TagKey<Biome> HAS_HARES = TagKey.create(Registries.BIOME, KingdomsAtWar.id("has_hares"));
        public static final TagKey<Biome> HAS_FEW_HARES = TagKey.create(Registries.BIOME, KingdomsAtWar.id("has_few_hares"));
        /** Hier werden die Vanilla-Hoftiere aus den Spawn-Listen genommen (die eigenen treten an ihre Stelle). */
        public static final TagKey<Biome> REPLACES_VANILLA_ANIMALS = TagKey.create(Registries.BIOME,
                KingdomsAtWar.id("replaces_vanilla_animals"));
        public static final TagKey<Biome> HAS_WILD_BOARS = TagKey.create(Registries.BIOME, KingdomsAtWar.id("has_wild_boars"));
        public static final TagKey<Biome> HAS_DEER = TagKey.create(Registries.BIOME, KingdomsAtWar.id("has_deer"));
        public static final TagKey<Biome> HAS_GEESE = TagKey.create(Registries.BIOME, KingdomsAtWar.id("has_geese"));
        public static final TagKey<Biome> HAS_WILL_O_WISPS = TagKey.create(Registries.BIOME, KingdomsAtWar.id("has_will_o_wisps"));
        public static final TagKey<Biome> HAS_CAVE_TROLLS = TagKey.create(Registries.BIOME, KingdomsAtWar.id("has_cave_trolls"));
        public static final TagKey<Biome> HAS_LINDWORMS = TagKey.create(Registries.BIOME, KingdomsAtWar.id("has_lindworms"));
        public static final TagKey<Biome> HAS_WEREWOLVES = TagKey.create(Registries.BIOME, KingdomsAtWar.id("has_werewolves"));
        public static final TagKey<Biome> HAS_MIRE_SHAMBLERS = TagKey.create(Registries.BIOME, KingdomsAtWar.id("has_mire_shamblers"));
        public static final TagKey<Biome> HAS_EMBER_WYRMS = TagKey.create(Registries.BIOME, KingdomsAtWar.id("has_ember_wyrms"));
        public static final TagKey<Biome> HAS_ASH_WRAITHS = TagKey.create(Registries.BIOME, KingdomsAtWar.id("has_ash_wraiths"));
        public static final TagKey<Biome> HAS_RIFT_HOUNDS = TagKey.create(Registries.BIOME, KingdomsAtWar.id("has_rift_hounds"));
        public static final TagKey<Biome> HAS_CHORUS_MAWS = TagKey.create(Registries.BIOME, KingdomsAtWar.id("has_chorus_maws"));
        /** Wo Drachennester der jeweiligen Art entstehen. */
        public static final java.util.Map<io.github.sirschrolli.kingdomsatwar.dragon.DragonType, TagKey<Biome>> HAS_DRAGON_NESTS = nestTags();
        public static final TagKey<Biome> HAS_CASTLE_RUINS = TagKey.create(Registries.BIOME, KingdomsAtWar.id("has_castle_ruins"));
        /** Wo der Turm des Erzmagiers eines Elements stehen kann. */
        public static final java.util.Map<io.github.sirschrolli.kingdomsatwar.dragon.DragonType, TagKey<Biome>> HAS_MAGE_TOWERS = towerTags();
        /** Wo der Siegelkreis liegen kann (ebenes, offenes Land). */
        public static final TagKey<Biome> HAS_SEAL_CIRCLE = TagKey.create(Registries.BIOME, KingdomsAtWar.id("has_seal_circle"));
        /** Wo die Stammburgen der fremden Reiche stehen dürfen (ebenes, offenes Land). */
        public static final TagKey<Biome> HAS_REALM_CASTLE = TagKey.create(Registries.BIOME, KingdomsAtWar.id("has_realm_castle"));
        /** Wo in der Schattenwelt Dörfer der Umbrer liegen können. */
        public static final TagKey<Biome> HAS_UMBRER_VILLAGE = TagKey.create(Registries.BIOME, KingdomsAtWar.id("has_umbrer_village"));
        /** Wo Nyxars Schattenzitadelle stehen kann (weites, festes Land der Schattenwelt). */
        /** Bauwerke der Schattenwelt: verlassene Mondtempel, Kristallminen, Leuchttürme an der Küste, Ruinen der alten Welt. */
        public static final TagKey<Biome> HAS_MOON_TEMPLE = TagKey.create(Registries.BIOME, KingdomsAtWar.id("has_moon_temple"));
        public static final TagKey<Biome> HAS_CRYSTAL_MINE = TagKey.create(Registries.BIOME, KingdomsAtWar.id("has_crystal_mine"));
        public static final TagKey<Biome> HAS_LIGHTHOUSE = TagKey.create(Registries.BIOME, KingdomsAtWar.id("has_lighthouse"));
        public static final TagKey<Biome> HAS_OLD_WORLD_RUIN = TagKey.create(Registries.BIOME, KingdomsAtWar.id("has_old_world_ruin"));
        public static final TagKey<Biome> HAS_SHADOW_CITADEL = TagKey.create(Registries.BIOME, KingdomsAtWar.id("has_shadow_citadel"));
        /** Wo der Thron der Alten schweben kann (alle Biome der Leere – er schwebt frei über allem). */
        public static final TagKey<Biome> HAS_VOID_THRONE = TagKey.create(Registries.BIOME, KingdomsAtWar.id("has_void_throne"));
        /** Wo das Versunkene Atoll liegen kann (Meere). */
        public static final TagKey<Biome> HAS_SUNKEN_ATOLL = TagKey.create(Registries.BIOME, KingdomsAtWar.id("has_sunken_atoll"));
        /** Wo Dörfer der jeweiligen Bauweise entstehen (Fachwerk, nordisch, Lehm, Pfahlbau). */
        public static final java.util.Map<io.github.sirschrolli.kingdomsatwar.worldgen.village.VillageStyle, TagKey<Biome>> HAS_VILLAGE = villageTags();
        /** Wo die eigenen Bauwerke der Oberwelt stehen können (je Art). */
        public static final java.util.Map<io.github.sirschrolli.kingdomsatwar.worldgen.site.SiteKind, TagKey<Biome>> HAS_SITE = siteTags();
        /** Alle Biome, in denen Dörfer stehen können – dort halten Bäume und Felsen Abstand zu den Häusern und Wegen. */
        public static final TagKey<Biome> VILLAGE_GROUND = TagKey.create(Registries.BIOME, KingdomsAtWar.id("village_ground"));

        private static java.util.Map<io.github.sirschrolli.kingdomsatwar.worldgen.site.SiteKind, TagKey<Biome>> siteTags() {
            java.util.Map<io.github.sirschrolli.kingdomsatwar.worldgen.site.SiteKind, TagKey<Biome>> tags =
                    new java.util.EnumMap<>(io.github.sirschrolli.kingdomsatwar.worldgen.site.SiteKind.class);
            for (var kind : io.github.sirschrolli.kingdomsatwar.worldgen.site.SiteKind.values()) {
                tags.put(kind, TagKey.create(Registries.BIOME, KingdomsAtWar.id("has_site/" + kind.id())));
            }
            return java.util.Collections.unmodifiableMap(tags);
        }

        private static java.util.Map<io.github.sirschrolli.kingdomsatwar.worldgen.village.VillageStyle, TagKey<Biome>> villageTags() {
            java.util.Map<io.github.sirschrolli.kingdomsatwar.worldgen.village.VillageStyle, TagKey<Biome>> tags =
                    new java.util.EnumMap<>(io.github.sirschrolli.kingdomsatwar.worldgen.village.VillageStyle.class);
            for (var style : io.github.sirschrolli.kingdomsatwar.worldgen.village.VillageStyle.values()) {
                tags.put(style, TagKey.create(Registries.BIOME, KingdomsAtWar.id("has_village/" + style.id())));
            }
            return java.util.Collections.unmodifiableMap(tags);
        }

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
        public static final TagKey<Item> REPAIRS_SHATTERED_CROWN = mod("repairs_shattered_crown");
        public static final TagKey<Item> REPAIRS_TIDE_SPEAR = mod("repairs_tide_spear");
        public static final TagKey<Item> REPAIRS_GLIDER = mod("repairs_wyvernit_glider");
        public static final TagKey<Item> REPAIRS_FAE_ELYTRA = mod("repairs_fae_elytra");
        /** Reparieren die Waffen und Rüstungen aus Monsterteilen (0.70). */
        public static final TagKey<Item> REPAIRS_CLAW_BLADE = mod("repairs_claw_blade");
        public static final TagKey<Item> REPAIRS_BOG_CLUB = mod("repairs_bog_club");
        public static final TagKey<Item> REPAIRS_EMBER_FANG = mod("repairs_ember_fang");
        public static final TagKey<Item> REPAIRS_SOUL_SCYTHE = mod("repairs_soul_scythe");
        public static final TagKey<Item> REPAIRS_PELT_ARMOR = mod("repairs_werewolf_pelt_armor");
        public static final TagKey<Item> REPAIRS_EMBER_ARMOR = mod("repairs_ember_scale_armor");
        /** Womit sich der Glutspeer reparieren lässt (Netheritschrott). */
        public static final TagKey<Item> REPAIRS_GLUT_SPEAR = mod("repairs_glut_spear");
        /** Repariert die Sternenlanze (Sternsplitter). */
        public static final TagKey<Item> REPAIRS_STAR_LANCE = mod("repairs_star_lance");
        /** Repariert die Sternenschwingen (Phantomhaut, Sternsplitter). */
        public static final TagKey<Item> REPAIRS_STAR_WINGS = mod("repairs_star_wings");
        /** Repariert Kartendeck und Zylinder der Mystik (Feenstaub). */
        public static final TagKey<Item> REPAIRS_MYSTIC_GEAR = mod("repairs_mystic_gear");
        /** Repariert Bannhammer und Fehlerflügel (Fehlersplitter). */
        public static final TagKey<Item> REPAIRS_GLITCH_GEAR = mod("repairs_glitch_gear");
        public static final TagKey<Item> REPAIRS_DRACONIUM = mod("repairs_draconium");
        /** Alle Rüstungsteile aus Drachenschuppen. */
        public static final TagKey<Item> DRAGON_SCALE_ARMOR = mod("dragon_scale_armor");

        public static TagKey<Item> scaleArmorRepair(io.github.sirschrolli.kingdomsatwar.dragon.DragonType type) {
            return mod("repairs_" + type.id() + "_dragon_scale_armor");
        }

        public static final TagKey<Item> DRAGON_EGGS = mod("dragon_eggs");
        /** Essen, das Bürger und Soldaten nicht nehmen: Verdorbenes, Giftiges, rohes Huhn und Kostbares. */
        public static final TagKey<Item> UNFIT_FOOD = mod("unfit_food");
        public static final TagKey<Item> SHADOW_LOGS = mod("shadow_logs");
        public static final TagKey<Item> GLIMMERWOOD_LOGS = mod("glimmerwood_logs");
        /** Bruchstein aus eigenem Gestein (für Steinwerkzeuge, Öfen und Braustände). */
        public static final TagKey<Item> COBBLED_STONES = mod("cobbled_stones");
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
