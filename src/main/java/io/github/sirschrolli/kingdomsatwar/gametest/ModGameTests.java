package io.github.sirschrolli.kingdomsatwar.gametest;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.army.Recruitment;
import io.github.sirschrolli.kingdomsatwar.block.BattlementBlock;
import io.github.sirschrolli.kingdomsatwar.block.ElementalDragonEggBlock;
import io.github.sirschrolli.kingdomsatwar.block.FeastBlock;
import io.github.sirschrolli.kingdomsatwar.block.SeatBlock;
import io.github.sirschrolli.kingdomsatwar.block.TableBlock;
import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import io.github.sirschrolli.kingdomsatwar.entity.SeatEntity;
import io.github.sirschrolli.kingdomsatwar.entity.dragon.DragonEntity;
import io.github.sirschrolli.kingdomsatwar.entity.dragon.DragonStage;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierEntity;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierType;
import io.github.sirschrolli.kingdomsatwar.item.DragonScaleArmorItem;
import io.github.sirschrolli.kingdomsatwar.magic.AetheritStaffItem;
import io.github.sirschrolli.kingdomsatwar.magic.KnownSpells;
import io.github.sirschrolli.kingdomsatwar.magic.Spell;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.ModBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.ModItems;
import io.github.sirschrolli.kingdomsatwar.worldgen.ModWorldgen;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Automatische Tests, die auf GitHub bei jedem Build in einem echten Minecraft-Server laufen
 * ({@code gradlew runGameTestServer}). Sie prüfen, ob Blöcke, Loot-Tables, Rezepte und
 * Weltgenerierung korrekt geladen werden.
 */
public final class ModGameTests {
    public static final DeferredRegister<Consumer<GameTestHelper>> TEST_FUNCTIONS =
            DeferredRegister.create(Registries.TEST_FUNCTION, KingdomsAtWar.MODID);

    private static final Identifier PLATFORM = KingdomsAtWar.id("test_platform");
    /** Größere Fläche (9x6x9) für Lebewesen; offen zum Himmel. */
    private static final Identifier ARENA = KingdomsAtWar.id("test_arena");
    private static final BlockPos TEST_POS = new BlockPos(2, 1, 2);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> PLACE_BLOCKS =
            TEST_FUNCTIONS.register("place_blocks", () -> ModGameTests::placeBlocks);
    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> LOOT_TABLES =
            TEST_FUNCTIONS.register("loot_tables", () -> ModGameTests::lootTables);
    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> ORE_DROPS =
            TEST_FUNCTIONS.register("ore_drops", () -> ModGameTests::oreDrops);
    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> CROP_HARVEST =
            TEST_FUNCTIONS.register("crop_harvest", () -> ModGameTests::cropHarvest);
    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> RECIPES =
            TEST_FUNCTIONS.register("recipes", () -> ModGameTests::recipes);
    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> WORLDGEN =
            TEST_FUNCTIONS.register("worldgen", () -> ModGameTests::worldgen);
    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> ORE_VEINS =
            TEST_FUNCTIONS.register("ore_veins", () -> ModGameTests::oreVeins);
    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> FLOWER_PATCHES =
            TEST_FUNCTIONS.register("flower_patches", () -> ModGameTests::flowerPatches);
    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> FLOWER_POTS =
            TEST_FUNCTIONS.register("flower_pots", () -> ModGameTests::flowerPots);
    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> WOOD_BURNS =
            TEST_FUNCTIONS.register("wood_burns", () -> ModGameTests::woodBurns);
    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> DRAGON_EGG_INCUBATION =
            TEST_FUNCTIONS.register("dragon_egg_incubation", () -> ModGameTests::dragonEggIncubation);
    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> DRAGON_EGG_HATCHES =
            TEST_FUNCTIONS.register("dragon_egg_hatches", () -> ModGameTests::dragonEggHatches);
    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> DRAGON_GROWTH =
            TEST_FUNCTIONS.register("dragon_growth", () -> ModGameTests::dragonGrowth);
    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> DRAGON_FEEDING =
            TEST_FUNCTIONS.register("dragon_feeding", () -> ModGameTests::dragonFeeding);
    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> DRAGON_BREATH =
            TEST_FUNCTIONS.register("dragon_breath", () -> ModGameTests::dragonBreath);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> SOLDIER_RECRUIT =
            TEST_FUNCTIONS.register("soldier_recruit", () -> ModGameTests::soldierRecruit);
    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> SOLDIER_FIGHTS =
            TEST_FUNCTIONS.register("soldier_fights", () -> ModGameTests::soldierFights);
    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> SOLDIER_ALLIES =
            TEST_FUNCTIONS.register("soldier_allies", () -> ModGameTests::soldierAllies);
    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> SOLDIER_HOLDS_POST =
            TEST_FUNCTIONS.register("soldier_holds_post", () -> ModGameTests::soldierHoldsPost);
    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> BANDIT_LOOT =
            TEST_FUNCTIONS.register("bandit_loot", () -> ModGameTests::banditLoot);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> KONTERIT_ARMOR =
            TEST_FUNCTIONS.register("konterit_armor", () -> ModGameTests::konteritArmor);
    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> SCALE_ARMOR_SET =
            TEST_FUNCTIONS.register("scale_armor_set", () -> ModGameTests::scaleArmorSet);
    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> DRACONIUM_IGNITES =
            TEST_FUNCTIONS.register("draconium_ignites", () -> ModGameTests::draconiumIgnites);
    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> DRAGON_ARMOR =
            TEST_FUNCTIONS.register("dragon_armor", () -> ModGameTests::dragonArmor);
    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> GLIDER_AND_CROWN =
            TEST_FUNCTIONS.register("glider_and_crown", () -> ModGameTests::gliderAndCrown);
    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> CREATURES_SPAWN =
            TEST_FUNCTIONS.register("creatures_spawn", () -> ModGameTests::creaturesSpawn);
    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> BOAR_RETALIATES =
            TEST_FUNCTIONS.register("boar_retaliates", () -> ModGameTests::boarRetaliates);
    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> LINDWORM_HUNTS =
            TEST_FUNCTIONS.register("lindworm_hunts", () -> ModGameTests::lindwormHunts);
    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> TROLL_FIGHTS_SOLDIERS =
            TEST_FUNCTIONS.register("troll_fights_soldiers", () -> ModGameTests::trollFightsSoldiers);
    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> FURNITURE_SEATS =
            TEST_FUNCTIONS.register("furniture_seats", () -> ModGameTests::furnitureSeats);
    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> DECOR_BLOCKS =
            TEST_FUNCTIONS.register("decor_blocks", () -> ModGameTests::decorBlocks);
    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> DRINKS_AND_FEAST =
            TEST_FUNCTIONS.register("drinks_and_feast", () -> ModGameTests::drinksAndFeast);
    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> STAFF_SPELLS =
            TEST_FUNCTIONS.register("staff_spells", () -> ModGameTests::staffSpells);
    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> STRUCTURES_GENERATE =
            TEST_FUNCTIONS.register("structures_generate", () -> ModGameTests::structuresGenerate);
    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> DRAGON_BOSS_LIFECYCLE =
            TEST_FUNCTIONS.register("dragon_boss_lifecycle", () -> ModGameTests::dragonBossLifecycle);
    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> DRAGON_BOSS_RAGE =
            TEST_FUNCTIONS.register("dragon_boss_rage", () -> ModGameTests::dragonBossRage);
    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> DRAGON_COMPASS =
            TEST_FUNCTIONS.register("dragon_compass", () -> ModGameTests::dragonCompass);
    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> SEAL_CIRCLE =
            TEST_FUNCTIONS.register("seal_circle", () -> ModGameTests::sealCircle);

    static {
        ShadowGameTests.init();   // meldet die Tests der Schattenwelt an
        UmbrerGameTests.init();   // und die der Umbrer
        EconomyGameTests.init();  // und die der Wirtschaft
        TownGameTests.init();     // und die der eigenen Siedlung
        MagicGameTests.init();    // und die der Magie
        MageGameTests.init();     // Erzmagier und ihre Türme
        OverworldGameTests.init(); // die eigenen Oberwelt-Biome
        VillageGameTests.init();   // die Dörfer und ihre Bewohner
    }

    private ModGameTests() {}

    /** Wird nur auf dem Test-Server bzw. in der Entwicklungsumgebung ausgelöst. */
    public static void registerTests(RegisterGameTestsEvent event) {
        Holder<TestEnvironmentDefinition<?>> environment = event.registerEnvironment(KingdomsAtWar.id("default"));
        for (DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> test :
                List.of(PLACE_BLOCKS, LOOT_TABLES, ORE_DROPS, CROP_HARVEST, RECIPES, WORLDGEN, ORE_VEINS, FLOWER_PATCHES,
                        FLOWER_POTS, WOOD_BURNS, DRAGON_COMPASS)) {
            event.registerTest(test.getId(), new FunctionGameTestInstance(test.key(),
                    new TestData<>(environment, PLATFORM, 100, 0, true)));
        }
        for (DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> test :
                List.of(DRAGON_EGG_INCUBATION, DRAGON_EGG_HATCHES, DRAGON_GROWTH, DRAGON_FEEDING, DRAGON_BREATH,
                        SOLDIER_RECRUIT, SOLDIER_FIGHTS, SOLDIER_ALLIES, SOLDIER_HOLDS_POST, BANDIT_LOOT, KONTERIT_ARMOR,
                        SCALE_ARMOR_SET, DRACONIUM_IGNITES, DRAGON_ARMOR, GLIDER_AND_CROWN, CREATURES_SPAWN, BOAR_RETALIATES,
                        LINDWORM_HUNTS, TROLL_FIGHTS_SOLDIERS, FURNITURE_SEATS, DECOR_BLOCKS, DRINKS_AND_FEAST,
                        STAFF_SPELLS, STRUCTURES_GENERATE, SEAL_CIRCLE, DRAGON_BOSS_RAGE)) {
            event.registerTest(test.getId(), new FunctionGameTestInstance(test.key(),
                    new TestData<>(environment, Level.OVERWORLD, ARENA, 200, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
        }
        ShadowGameTests.register(event, environment);
        UmbrerGameTests.register(event, environment);
        EconomyGameTests.register(event, environment);
        TownGameTests.register(event, environment);
        MagicGameTests.register(event, environment);
        MageGameTests.register(event, environment);
        OverworldGameTests.register(event, environment);
        VillageGameTests.register(event, environment);
        // Drachen-Bosse brauchen länger (Erwachen + Todesanimation)
        event.registerTest(DRAGON_BOSS_LIFECYCLE.getId(), new FunctionGameTestInstance(DRAGON_BOSS_LIFECYCLE.key(),
                new TestData<>(environment, Level.OVERWORLD, ARENA, 700, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
    }

    // =====================================================================================
    // Tests
    // =====================================================================================

    /** Alle Blöcke mit Gegenstand lassen sich setzen. */
    private static void placeBlocks(GameTestHelper helper) {
        for (DeferredHolder<Block, ? extends Block> holder : ModBlocks.BLOCKS.getEntries()) {
            Block block = holder.value();
            if (block instanceof CropBlock || block.asItem() == Items.AIR) {
                continue; // Feldfrüchte und Blumentöpfe brauchen besonderen Untergrund
            }
            if (block instanceof net.minecraft.world.level.block.FlowerBlock) {
                continue; // Blumen brauchen Erde darunter
            }
            helper.setBlock(TEST_POS, block);
            helper.assertBlockPresent(block, TEST_POS);
        }
        helper.succeed();
    }

    /** Jeder Block hat eine Loot-Table, die vom Server geladen wurde (außer dem unzerstörbaren Nestkern). */
    private static void lootTables(GameTestHelper helper) {
        var registries = helper.getLevel().getServer().reloadableRegistries();
        for (DeferredHolder<Block, ? extends Block> holder : ModBlocks.BLOCKS.getEntries()) {
            Block block = holder.value();
            if (block.defaultDestroyTime() < 0.0f) {
                continue;   // unzerstörbar (Nestkern, Teile des Siegelkreises): ohne Beute
            }
            ResourceKey<LootTable> key = block.getLootTable().orElseThrow(
                    () -> helper.assertionException("Block ohne Loot-Table: %s", holder.getId()));
            helper.assertTrue(registries.getLootTable(key) != LootTable.EMPTY,
                    "Loot-Table fehlt: " + key.identifier());
        }
        helper.succeed();
    }

    /** Erze lassen beim Abbauen mit einer Spitzhacke ihren Rohstoff fallen. */
    private static void oreDrops(GameTestHelper helper) {
        assertDrops(helper, ModBlocks.CARBON_ORE, ModItems.CARBON.get());
        assertDrops(helper, ModBlocks.DEEPSLATE_GEMSTONE_ORE, ModItems.GEMSTONE.get());
        assertDrops(helper, ModBlocks.KONTERIT_ORE, ModItems.KONTERIT.get());
        assertDrops(helper, ModBlocks.AETHERIT_ORE, ModItems.AETHERIT.get());
        assertDrops(helper, ModBlocks.DRACONIUM_ORE, ModItems.DRACONIUM.get());
        assertDrops(helper, ModBlocks.WYVERNIT_ORE, ModItems.WYVERNIT.get());
        helper.succeed();
    }

    /** Reife Feldfrüchte lassen ihre Frucht fallen. */
    private static void cropHarvest(GameTestHelper helper) {
        helper.setBlock(TEST_POS.below(), Blocks.FARMLAND);
        assertCropDrops(helper, ModBlocks.TOMATOES, ModItems.TOMATO.get());
        assertCropDrops(helper, ModBlocks.CABBAGES, ModItems.CABBAGE.get());
        assertCropDrops(helper, ModBlocks.ONIONS, ModItems.ONION.get());
        assertCropDrops(helper, ModBlocks.BARLEY, ModItems.BARLEY.get());
        assertCropDrops(helper, ModBlocks.HOPS, ModItems.HOPS.get());
        assertCropDrops(helper, ModBlocks.GRAPES, ModItems.GRAPES.get());
        assertCropDrops(helper, ModBlocks.GARLIC_CROP, ModItems.GARLIC.get());
        helper.succeed();
    }

    /** Einige wichtige Rezepte wurden geladen. */
    private static void recipes(GameTestHelper helper) {
        var recipes = helper.getLevel().getServer().getRecipeManager().recipeMap();
        for (String name : List.of("steel_blend", "steel_ingot_from_blasting",
                "medieval_stone_stairs", "red_roof_tile_stairs", "konterit_block", "vegetable_stew",
                "medieval_stone_from_stone_stonecutting", "draconium_from_smelting_draconium_ore", "recruitment_table",
                "command_baton", "steel_sword", "steel_pickaxe", "steel_chestplate", "konterit_boots", "draconium_sword",
                "draconium_axe", "fire_dragon_scale_helmet", "water_dragon_scale_boots", "wyvernit_glider", "gemstone_crown",
                "steel_dragon_armor", "draconium_dragon_armor", "cooked_venison", "cooked_venison_from_campfire_cooking",
                "white_plaster", "clay_plaster_stairs", "leaded_glass", "leaded_glass_pane", "castle_door", "green_shutters",
                "steel_lantern", "aetherit_lantern", "chandelier", "medieval_stone_battlement", "ale", "wine", "mead",
                "garlic_bread", "feast", "aetherit_staff",
                "medieval_stone_battlement_from_medieval_stone_stonecutting", "oak_chair", "oak_bench", "oak_table", "throne")) {
            ResourceKey<Recipe<?>> key = ResourceKey.create(Registries.RECIPE, KingdomsAtWar.id(name));
            helper.assertTrue(recipes.byKey(key) != null, "Rezept fehlt: " + key.identifier());
        }
        // Dracheneier gibt es nur nach dem Sieg über einen Drachen-Boss, Zauber nur aus den Tafeln der Erzmagier – kein Rezept
        for (DragonType type : DragonType.values()) {
            for (String name : List.of(type.id() + "_dragon_egg", type.id() + "_rune", type.id() + "_spell_tablet")) {
                ResourceKey<Recipe<?>> key = ResourceKey.create(Registries.RECIPE, KingdomsAtWar.id(name));
                helper.assertTrue(recipes.byKey(key) == null, "Darf kein Rezept haben: " + key.identifier());
            }
        }
        helper.succeed();
    }

    /** Die Erzadern und Blumenfelder sind registriert. */
    private static void worldgen(GameTestHelper helper) {
        var placed = helper.getLevel().registryAccess().lookupOrThrow(Registries.PLACED_FEATURE);
        for (ModWorldgen.OreVein vein : ModWorldgen.ORE_VEINS) {
            helper.assertTrue(placed.get(vein.placedKey()).isPresent(), "Erzader fehlt: " + vein.name());
        }
        for (ModWorldgen.FlowerPatch patch : ModWorldgen.FLOWER_PATCHES) {
            helper.assertTrue(placed.get(patch.placedKey()).isPresent(), "Blumenfeld fehlt: " + patch.name());
        }
        helper.succeed();
    }

    /** Jede Erzader setzt in massivem Stein (bzw. Tiefenschiefer) tatsächlich ihr Erz. */
    private static void oreVeins(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        var features = level.registryAccess().lookupOrThrow(Registries.FEATURE);
        // Erzadern prüfen bei der Weltgenerierung die Oberfläche (OCEAN_FLOOR_WG). In einer fertigen Welt
        // gibt es diese Höhenkarte nicht mehr, darum wird sie für den Testbereich neu berechnet.
        BlockPos center = helper.absolutePos(TEST_POS);
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                Heightmap.primeHeightmaps(level.getChunk(center.offset(dx * 16, 0, dz * 16)),
                        EnumSet.of(Heightmap.Types.OCEAN_FLOOR_WG));
            }
        }
        for (ModWorldgen.OreVein vein : ModWorldgen.ORE_VEINS) {
            Feature feature = features.getOrThrow(vein.featureKey()).value();
            for (Block rock : vein.deepslateOre() == null ? List.of(Blocks.STONE) : List.of(Blocks.STONE, Blocks.DEEPSLATE)) {
                Block expected = rock == Blocks.STONE ? vein.stoneOre().get() : vein.deepslateOre().get();
                boolean found = false;
                for (int seed = 0; seed < 40 && !found; seed++) {
                    fillPlatform(helper, rock);
                    feature.place(level, level.getChunkSource().getGenerator(), RandomSource.create(seed),
                            helper.absolutePos(TEST_POS));
                    found = containsBlock(helper, expected);
                }
                helper.assertTrue(found, "Erzader " + vein.name() + " setzt kein " + expected + " in " + rock);
            }
        }
        helper.succeed();
    }

    /** Die Blumenfelder setzen ihre Blume auf Gras. */
    private static void flowerPatches(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        var features = level.registryAccess().lookupOrThrow(Registries.FEATURE);
        for (ModWorldgen.FlowerPatch patch : ModWorldgen.FLOWER_PATCHES) {
            helper.setBlock(TEST_POS.below(), Blocks.GRASS_BLOCK);
            helper.setBlock(TEST_POS, Blocks.AIR);
            features.getOrThrow(patch.featureKey()).value()
                    .place(level, level.getChunkSource().getGenerator(), RandomSource.create(0), helper.absolutePos(TEST_POS));
            helper.assertBlockPresent(patch.flower().get(), TEST_POS);
        }
        helper.succeed();
    }

    /** Jede Zierblume passt in einen leeren Blumentopf (Rechtsklick mit der Blume). */
    private static void flowerPots(GameTestHelper helper) {
        FlowerPotBlock emptyPot = (FlowerPotBlock) Blocks.FLOWER_POT;
        ModBlocks.FLOWERS.forEach((flower, pot) -> helper.assertTrue(emptyPot.getFullPot(flower.get()) == pot.get(),
                flower.getId() + " passt nicht in den Blumentopf"));
        helper.succeed();
    }

    /** Holzblöcke fangen Feuer, Steinblöcke nicht. */
    private static void woodBurns(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(TEST_POS);
        for (DeferredBlock<? extends Block> block : ModBlocks.woodenBlocks()) {
            BlockState state = block.get().defaultBlockState();
            helper.assertTrue(state.isFlammable(level, pos, Direction.UP) && state.getFlammability(level, pos, Direction.UP) > 0,
                    block.getId() + " brennt nicht");
        }
        BlockState stone = ModBlocks.MEDIEVAL_STONE.base().get().defaultBlockState();
        helper.assertTrue(!stone.isFlammable(level, pos, Direction.UP), "Mittelalterlicher Stein darf nicht brennen");
        helper.succeed();
    }

    /** Jedes Drachenei brütet nur an seinem passenden Ort. */
    private static void dragonEggIncubation(GameTestHelper helper) {
        arenaFloor(helper);
        java.util.Map<DragonType, BlockPos> spots = new java.util.EnumMap<>(DragonType.class);
        spots.put(DragonType.FIRE, new BlockPos(1, 1, 1));
        spots.put(DragonType.ICE, new BlockPos(4, 1, 1));
        spots.put(DragonType.LIGHTNING, new BlockPos(7, 1, 1));
        spots.put(DragonType.NATURE, new BlockPos(1, 1, 6));
        spots.put(DragonType.EARTH, new BlockPos(4, 1, 6));
        spots.put(DragonType.WATER, new BlockPos(7, 1, 6));
        ServerLevel level = helper.getLevel();
        // Auf blankem Stein unter freiem Himmel brütet keines der Eier (außer bei Gewitter das Blitz-Ei)
        spots.forEach((type, pos) -> {
            helper.setBlock(pos, ModBlocks.DRAGON_EGGS.get(type).get());
            ElementalDragonEggBlock egg = ModBlocks.DRAGON_EGGS.get(type).get();
            if (type != DragonType.LIGHTNING || !level.isThundering()) {
                helper.assertFalse(egg.canIncubate(level, helper.absolutePos(pos), helper.getBlockState(pos)),
                        type.id() + "-Ei brütet auf blankem Stein");
            }
        });
        // Passende Orte herrichten
        helper.setBlock(spots.get(DragonType.FIRE).below(), Blocks.MAGMA_BLOCK);
        helper.setBlock(spots.get(DragonType.ICE).below(), Blocks.PACKED_ICE);
        helper.setBlock(spots.get(DragonType.LIGHTNING).east(), Blocks.LIGHTNING_ROD.weathering().unaffected());
        BlockPos nature = spots.get(DragonType.NATURE);
        helper.setBlock(nature.below(), Blocks.GRASS_BLOCK);
        helper.setBlock(nature.east(), Blocks.OAK_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true));
        helper.setBlock(nature.north(), Blocks.GLOWSTONE);
        helper.setBlock(spots.get(DragonType.EARTH).above(2), Blocks.STONE);
        helper.setBlock(spots.get(DragonType.WATER), ModBlocks.DRAGON_EGGS.get(DragonType.WATER).get().defaultBlockState()
                .setValue(ElementalDragonEggBlock.WATERLOGGED, true));
        // Licht breitet sich erst nach ein paar Ticks aus
        helper.succeedWhen(() -> spots.forEach((type, pos) -> {
            ElementalDragonEggBlock egg = ModBlocks.DRAGON_EGGS.get(type).get();
            helper.assertTrue(egg.canIncubate(level, helper.absolutePos(pos), helper.getBlockState(pos)),
                    type.id() + "-Ei brütet nicht am passenden Ort");
            // … aber ohne einen Spieler in der Nähe schlüpft nichts (keine wilden Schlüpflinge)
            helper.assertFalse(egg.canHatchNow(level, helper.absolutePos(pos), helper.getBlockState(pos)),
                    type.id() + "-Ei brütet ohne Zeugen");
        }));
    }

    /** Ein brütendes Ei bekommt Risse, dann schlüpft ein Schlüpfling. */
    private static void dragonEggHatches(GameTestHelper helper) {
        arenaFloor(helper);
        BlockPos pos = new BlockPos(4, 1, 4);
        ElementalDragonEggBlock egg = ModBlocks.DRAGON_EGGS.get(DragonType.FIRE).get();
        helper.setBlock(pos.below(), Blocks.MAGMA_BLOCK);
        helper.setBlock(pos, egg);
        ServerLevel level = helper.getLevel();
        BlockPos absolute = helper.absolutePos(pos);
        for (int stage = 1; stage <= 3; stage++) {
            egg.advance(level, absolute, level.getBlockState(absolute), 1);
            helper.assertBlockProperty(pos, ElementalDragonEggBlock.HATCH, stage);
        }
        egg.advance(level, absolute, level.getBlockState(absolute), 1);
        helper.assertBlockNotPresent(egg, pos);
        DragonEntity dragon = helper.findOneEntity(ModEntities.DRAGONS.get(DragonType.FIRE).get());
        helper.assertTrue(dragon.getStage() == DragonStage.HATCHLING, "Geschlüpfter Drache ist kein Schlüpfling");
        helper.assertTrue(dragon.position().distanceTo(Vec3.atBottomCenterOf(absolute)) < 1.0, "Drache schlüpft nicht am Ei");
        helper.succeed();
    }

    /** Drachen wachsen in fünf Stufen; Größe, Leben und Fähigkeiten passen sich an. */
    private static void dragonGrowth(GameTestHelper helper) {
        arenaFloor(helper);
        DragonEntity dragon = helper.spawnWithNoFreeWill(ModEntities.DRAGONS.get(DragonType.ICE).get(), new BlockPos(4, 1, 4));
        float previousScale = 0;
        for (DragonStage stage : DragonStage.values()) {
            helper.assertTrue(dragon.getStage() == stage, "Erwartet Stufe " + stage + ", ist " + dragon.getStage());
            helper.assertValueEqual((float) dragon.getMaxHealth(), (float) stage.maxHealth(), "Leben in Stufe " + stage);
            helper.assertTrue(dragon.getScale() > previousScale, "Drache wird in Stufe " + stage + " nicht größer");
            helper.assertTrue(stage.rideable() == (stage.number() >= 4), "Reiten ab Stufe 4");
            previousScale = dragon.getScale();
            if (!stage.isLast()) {
                dragon.addGrowth(stage.growthTicks());
            }
        }
        helper.assertTrue(dragon.getHealth() == dragon.getMaxHealth(), "Drache ist nach dem Wachsen nicht geheilt");
        helper.succeed();
    }

    /** Füttern heilt und lässt den Drachen wachsen; Edelsteine zähmen wilde Schlüpflinge (mit etwas Glück). */
    private static void dragonFeeding(GameTestHelper helper) {
        arenaFloor(helper);
        DragonEntity dragon = helper.spawnWithNoFreeWill(ModEntities.DRAGONS.get(DragonType.NATURE).get(), new BlockPos(4, 1, 4));
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        dragon.setHealth(4.0f);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.DRAGON_FOOD.get(), 2));
        dragon.mobInteract(player, InteractionHand.MAIN_HAND);
        helper.assertTrue(dragon.getHealth() > 4.0f, "Drachenfutter heilt nicht");
        helper.assertTrue(dragon.getGrowthProgress() > 0.0f, "Drachenfutter lässt den Drachen nicht wachsen");
        helper.assertValueEqual(player.getMainHandItem().getCount(), 1, "Drachenfutter wird nicht verbraucht");
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.GEMSTONE.get(), 64));
        for (int i = 0; i < 64 && !dragon.isTame(); i++) {
            dragon.mobInteract(player, InteractionHand.MAIN_HAND);
        }
        helper.assertTrue(dragon.isTame() && dragon.isOwnedBy(player), "Edelsteine zähmen den Schlüpfling nicht");
        helper.succeed();
    }

    /** Der Feuerodem eines ausgewachsenen Drachen verletzt und entzündet ein Ziel vor ihm. */
    private static void dragonBreath(GameTestHelper helper) {
        arenaFloor(helper);
        DragonEntity dragon = helper.spawnWithNoFreeWill(ModEntities.DRAGONS.get(DragonType.FIRE).get(), new BlockPos(1, 1, 4));
        dragon.applyStage(DragonStage.ADULT, true);
        dragon.snapTo(dragon.getX(), dragon.getY(), dragon.getZ(), -90.0f, 0.0f); // Blick nach +X
        dragon.setYBodyRot(-90.0f);
        dragon.setYHeadRot(-90.0f);
        var target = helper.spawnWithNoFreeWill(EntityTypes.PIG, new BlockPos(5, 1, 4));
        Vec3 direction = target.getBoundingBox().getCenter().subtract(dragon.headPosition()).normalize();
        helper.assertTrue(dragon.startBreath(direction), "Odem startet nicht");
        helper.succeedWhen(() -> {
            helper.assertTrue(target.getHealth() < target.getMaxHealth() || !target.isAlive(), "Ziel nimmt keinen Schaden");
            helper.assertTrue(target.getRemainingFireTicks() > 0 || !target.isAlive(), "Ziel brennt nicht");
        });
    }

    /** Am Rekrutierungstisch wirbt man Soldaten gegen ihren Sold in Münzen an; ohne genug Geld geht es nicht. */
    private static void soldierRecruit(GameTestHelper helper) {
        arenaFloor(helper);
        BlockPos table = new BlockPos(4, 1, 4);
        helper.setBlock(table, ModBlocks.RECRUITMENT_TABLE.get());
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        Vec3 stand = helper.absoluteVec(new Vec3(4.5, 1, 1.5));
        player.snapTo(stand.x, stand.y, stand.z, 0.0f, 0.0f);
        player.getInventory().add(new ItemStack(ModItems.SILVER_COIN.get(), 5));
        BlockPos tablePos = helper.absolutePos(table);
        helper.assertTrue(Recruitment.recruit(player, tablePos, SoldierType.SWORDSMAN) == Recruitment.Result.RECRUITED,
                "Schwertkämpfer lässt sich nicht anwerben");
        helper.assertValueEqual(io.github.sirschrolli.kingdomsatwar.economy.Coins.total(player), 0, "Münzen nach dem Anwerben");
        SoldierEntity soldier = helper.findOneEntity(ModEntities.SOLDIERS.get(SoldierType.SWORDSMAN).get());
        helper.assertTrue(soldier.isOwnedBy(player), "Soldat gehört nicht dem Spieler");
        helper.assertTrue(soldier.getMainHandItem().is(Items.IRON_SWORD), "Soldat hat kein Schwert");
        helper.assertTrue(soldier.hasCustomName(), "Soldat hat keinen Namen");
        helper.assertTrue(Recruitment.recruit(player, tablePos, SoldierType.SWORDSMAN) == Recruitment.Result.NOT_ENOUGH,
                "Anwerben ohne Münzen muss scheitern");
        // Ritter kosten 1 Gold 5 Silber und zwei Stahlbarren – bezahlt mit 2 Gold, das Wechselgeld kommt zurück
        player.getInventory().add(new ItemStack(ModItems.GOLD_COIN.get(), 2));
        player.getInventory().add(new ItemStack(ModItems.STEEL_INGOT.get(), 1));
        helper.assertTrue(Recruitment.recruit(player, tablePos, SoldierType.KNIGHT) == Recruitment.Result.NOT_ENOUGH,
                "Ritter ohne genug Stahl darf nicht erscheinen");
        player.getInventory().add(new ItemStack(ModItems.STEEL_INGOT.get(), 1));
        helper.assertTrue(Recruitment.recruit(player, tablePos, SoldierType.KNIGHT) == Recruitment.Result.RECRUITED,
                "Ritter lässt sich nicht anwerben");
        helper.assertValueEqual(io.github.sirschrolli.kingdomsatwar.economy.Coins.total(player), 50, "Wechselgeld nach dem Ritter");
        SoldierEntity knight = helper.findOneEntity(ModEntities.SOLDIERS.get(SoldierType.KNIGHT).get());
        helper.assertTrue(knight.getOffhandItem().is(Items.SHIELD), "Ritter hat keinen Schild");
        // zu weit weg
        player.snapTo(stand.x + 30, stand.y, stand.z, 0.0f, 0.0f);
        helper.assertTrue(Recruitment.recruit(player, tablePos, SoldierType.SWORDSMAN) == Recruitment.Result.TOO_FAR,
                "Anwerben aus der Ferne muss scheitern");
        helper.succeed();
    }

    /** Ein Ritter besiegt einen Wüstenzombie, der ihm zu nahe kommt. */
    private static void soldierFights(GameTestHelper helper) {
        arenaFloor(helper);
        SoldierEntity knight = helper.spawn(ModEntities.SOLDIERS.get(SoldierType.KNIGHT).get(), new BlockPos(2, 1, 4));
        knight.equip();
        var husk = helper.spawn(EntityTypes.HUSK, new BlockPos(6, 1, 4));
        helper.succeedWhen(() -> helper.assertTrue(!husk.isAlive(), "Der Zombie lebt noch"));
    }

    /** Soldaten verletzen weder ihren Besitzer noch einander; der Besitzer kann seine Soldaten nicht schlagen. */
    private static void soldierAllies(GameTestHelper helper) {
        arenaFloor(helper);
        ServerLevel level = helper.getLevel();
        Player owner = helper.makeMockPlayer(GameType.SURVIVAL);
        SoldierEntity archer = helper.spawnWithNoFreeWill(ModEntities.SOLDIERS.get(SoldierType.ARCHER).get(), new BlockPos(2, 1, 2));
        SoldierEntity swordsman = helper.spawnWithNoFreeWill(ModEntities.SOLDIERS.get(SoldierType.SWORDSMAN).get(), new BlockPos(6, 1, 6));
        archer.setOwner(owner);
        swordsman.setOwner(owner);
        helper.assertTrue(archer.isAlliedTo(swordsman) && swordsman.isAlliedTo(owner), "Soldaten sind nicht verbündet");
        float health = swordsman.getHealth();
        swordsman.hurtServer(level, level.damageSources().mobAttack(archer), 6.0f);
        swordsman.hurtServer(level, level.damageSources().playerAttack(owner), 6.0f);
        helper.assertValueEqual(swordsman.getHealth(), health, "Leben des Schwertkämpfers nach Angriffen von Verbündeten");
        float ownerHealth = owner.getHealth();
        owner.hurtServer(level, level.damageSources().mobAttack(archer), 4.0f);
        helper.assertValueEqual(owner.getHealth(), ownerHealth, "Leben des Besitzers nach einem Treffer des eigenen Soldaten");
        var zombie = helper.spawnWithNoFreeWill(EntityTypes.ZOMBIE, new BlockPos(4, 1, 6));
        helper.assertTrue(swordsman.canAttack(zombie) && !swordsman.canAttack(archer), "Freund und Feind verwechselt");
        helper.succeed();
    }

    /** Wer Stellung halten soll, geht zu seinem Posten und bleibt dort. */
    private static void soldierHoldsPost(GameTestHelper helper) {
        arenaFloor(helper);
        SoldierEntity soldier = helper.spawn(ModEntities.SOLDIERS.get(SoldierType.SWORDSMAN).get(), new BlockPos(1, 1, 1));
        BlockPos post = helper.absolutePos(new BlockPos(7, 1, 7));
        soldier.setMode(SoldierEntity.MODE_HOLD, post);
        helper.succeedWhen(() -> helper.assertTrue(soldier.blockPosition().closerThan(post, 1.6),
                "Soldat ist nicht an seinem Posten: " + soldier.blockPosition()));
    }

    /** Banditen haben eine Beute-Tabelle mit Smaragden. */
    private static void banditLoot(GameTestHelper helper) {
        var registries = helper.getLevel().getServer().reloadableRegistries();
        for (var type : List.of(ModEntities.BANDIT.get(), ModEntities.BANDIT_ARCHER.get())) {
            ResourceKey<LootTable> key = type.getDefaultLootTable().orElseThrow();
            helper.assertTrue(registries.getLootTable(key) != LootTable.EMPTY, "Beute fehlt: " + key.identifier());
        }
        helper.succeed();
    }

    /** Konterit-Rüstung macht schneller und federt Stürze ab (je Teil). */
    private static void konteritArmor(GameTestHelper helper) {
        arenaFloor(helper);
        var zombie = helper.spawnWithNoFreeWill(EntityTypes.ZOMBIE, new BlockPos(4, 1, 4));
        double speed = zombie.getAttributeValue(Attributes.MOVEMENT_SPEED);
        double safeFall = zombie.getAttributeValue(Attributes.SAFE_FALL_DISTANCE);
        zombie.setItemSlot(EquipmentSlot.HEAD, new ItemStack(ModItems.KONTERIT_HELMET.get()));
        zombie.setItemSlot(EquipmentSlot.CHEST, new ItemStack(ModItems.KONTERIT_CHESTPLATE.get()));
        zombie.setItemSlot(EquipmentSlot.LEGS, new ItemStack(ModItems.KONTERIT_LEGGINGS.get()));
        zombie.setItemSlot(EquipmentSlot.FEET, new ItemStack(ModItems.KONTERIT_BOOTS.get()));
        helper.succeedWhen(() -> {
            helper.assertTrue(zombie.getAttributeValue(Attributes.MOVEMENT_SPEED) > speed * 1.15,
                    "Konterit macht nicht schneller: " + zombie.getAttributeValue(Attributes.MOVEMENT_SPEED));
            helper.assertTrue(zombie.getAttributeValue(Attributes.SAFE_FALL_DISTANCE) >= safeFall + 4.0,
                    "Konterit federt Stürze nicht ab");
            helper.assertTrue(zombie.getArmorValue() >= 15, "Konterit-Rüstung schützt zu wenig: " + zombie.getArmorValue());
        });
    }

    /** Der volle Satz Drachenschuppen schenkt die Kraft des Elements, ein unvollständiger nicht. */
    private static void scaleArmorSet(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        var fire = ModItems.DRAGON_SCALE_ARMOR.get(DragonType.FIRE);
        EquipmentSlot[] slots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
        for (int i = 0; i < 3; i++) {
            player.setItemSlot(slots[i], new ItemStack(fire.get(i).get()));
        }
        player.setItemSlot(EquipmentSlot.FEET, new ItemStack(ModItems.DRAGON_SCALE_ARMOR.get(DragonType.ICE).get(3).get()));
        helper.assertTrue(DragonScaleArmorItem.fullSet(player) == null, "Gemischter Satz zählt als voller Satz");
        player.setItemSlot(EquipmentSlot.FEET, new ItemStack(fire.get(3).get()));
        helper.assertTrue(DragonScaleArmorItem.fullSet(player) == DragonType.FIRE, "Voller Feuer-Satz nicht erkannt");
        DragonScaleArmorItem.applySetBonus(player, DragonType.FIRE);
        helper.assertTrue(player.hasEffect(MobEffects.FIRE_RESISTANCE), "Kein Feuerschutz durch den Feuer-Satz");
        // Eis-Schuppen schützen schon einzeln vor Pulverschnee-Frost
        var zombie = helper.spawnWithNoFreeWill(EntityTypes.ZOMBIE, new BlockPos(4, 1, 4));
        zombie.setItemSlot(EquipmentSlot.FEET, new ItemStack(ModItems.DRAGON_SCALE_ARMOR.get(DragonType.ICE).get(3).get()));
        helper.assertFalse(zombie.canFreeze(), "Eis-Schuppen schützen nicht vor Frost");
        helper.succeed();
    }

    /** Draconium-Waffen setzen das Ziel in Brand. */
    private static void draconiumIgnites(GameTestHelper helper) {
        arenaFloor(helper);
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        var pig = helper.spawnWithNoFreeWill(EntityTypes.PIG, new BlockPos(4, 1, 4));
        player.snapTo(pig.getX() - 1.5, pig.getY(), pig.getZ(), -90.0f, 0.0f);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.DRACONIUM_SWORD.get()));
        player.attack(pig);
        helper.assertTrue(pig.getRemainingFireTicks() > 0, "Das Draconiumschwert entzündet nicht");
        helper.succeed();
    }

    /** Ausgewachsene, gezähmte Drachen tragen einen Panzer; Schlüpflinge nicht. */
    private static void dragonArmor(GameTestHelper helper) {
        arenaFloor(helper);
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        DragonEntity young = helper.spawnWithNoFreeWill(ModEntities.DRAGONS.get(DragonType.EARTH).get(), new BlockPos(2, 1, 2));
        young.tame(player);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.STEEL_DRAGON_ARMOR.get()));
        young.mobInteract(player, InteractionHand.MAIN_HAND);
        helper.assertTrue(young.getBodyArmorItem().isEmpty(), "Ein Schlüpfling darf keinen Panzer tragen");

        DragonEntity adult = helper.spawnWithNoFreeWill(ModEntities.DRAGONS.get(DragonType.EARTH).get(), new BlockPos(5, 1, 5));
        adult.applyStage(DragonStage.ADULT, false);
        adult.tame(player);
        int armorBefore = adult.getArmorValue();
        adult.mobInteract(player, InteractionHand.MAIN_HAND);
        helper.assertTrue(adult.getBodyArmorItem().is(ModItems.STEEL_DRAGON_ARMOR.get()), "Drache trägt den Panzer nicht");
        helper.assertTrue(player.getMainHandItem().isEmpty(), "Der Panzer wurde nicht aus der Hand genommen");
        helper.succeedWhen(() -> helper.assertTrue(adult.getArmorValue() >= armorBefore + 10,
                "Der Panzer schützt nicht: " + armorBefore + " -> " + adult.getArmorValue()));
    }

    /** Der Wyvernit-Gleiter trägt wie Elytren, die Krone bringt Glück. */
    private static void gliderAndCrown(GameTestHelper helper) {
        ItemStack glider = new ItemStack(ModItems.WYVERNIT_GLIDER.get());
        helper.assertTrue(LivingEntity.canGlideUsing(glider, EquipmentSlot.CHEST), "Mit dem Gleiter kann man nicht gleiten");
        helper.assertFalse(LivingEntity.canGlideUsing(glider, EquipmentSlot.LEGS), "Gleiter funktioniert im falschen Platz");
        ItemStack crown = new ItemStack(ModItems.GEMSTONE_CROWN.get());
        helper.assertTrue(crown.getAttributeModifiers().compute(Attributes.LUCK, 0.0, EquipmentSlot.HEAD) >= 2.0,
                "Die Krone bringt kein Glück");
        helper.assertTrue(crown.getAttributeModifiers().compute(Attributes.ARMOR, 0.0, EquipmentSlot.HEAD) >= 2.0,
                "Die Krone schützt nicht");
        helper.succeed();
    }

    /** Alle wilden Kreaturen erscheinen, leben und haben eine Beute-Tabelle. */
    private static void creaturesSpawn(GameTestHelper helper) {
        arenaFloor(helper);
        var registries = helper.getLevel().getServer().reloadableRegistries();
        List<net.minecraft.world.entity.EntityType<? extends net.minecraft.world.entity.Mob>> types = List.of(
                ModEntities.WILD_BOAR.get(), ModEntities.DEER.get(), ModEntities.GOOSE.get(), ModEntities.WILL_O_WISP.get(),
                ModEntities.CAVE_TROLL.get(), ModEntities.LINDWORM.get());
        List<net.minecraft.world.entity.Mob> mobs = new java.util.ArrayList<>();
        for (int i = 0; i < types.size(); i++) {
            var type = types.get(i);
            mobs.add(helper.spawnWithNoFreeWill(type, new BlockPos(1 + (i % 3) * 3, 1, 2 + (i / 3) * 4)));
            ResourceKey<LootTable> key = type.getDefaultLootTable().orElseThrow();
            helper.assertTrue(registries.getLootTable(key) != LootTable.EMPTY, "Beute fehlt: " + key.identifier());
        }
        helper.runAfterDelay(10, () -> {
            for (var mob : mobs) {
                helper.assertTrue(mob.isAlive(), mob.getType().getDescriptionId() + " lebt nicht mehr");
                helper.assertValueEqual(mob.getHealth(), mob.getMaxHealth(), "Leben von " + mob.getType().getDescriptionId());
            }
            helper.succeed();
        });
    }

    /** Ein angegriffenes Wildschwein greift den Angreifer an. */
    private static void boarRetaliates(GameTestHelper helper) {
        arenaFloor(helper);
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        var boar = helper.spawn(ModEntities.WILD_BOAR.get(), new BlockPos(4, 1, 4));
        player.snapTo(boar.getX() + 2, boar.getY(), boar.getZ(), 90.0f, 0.0f);
        // erst nach ein paar Ticks angreifen (HurtByTargetGoal vergleicht den Zeitpunkt des Treffers)
        helper.runAfterDelay(5, () -> boar.hurtServer(helper.getLevel(), helper.getLevel().damageSources().playerAttack(player), 1.0f));
        helper.succeedWhen(() -> {
            helper.assertTrue(boar.getTarget() == player, "Das Wildschwein wehrt sich nicht");
            helper.assertTrue(boar.isCharging(), "Das Wildschwein stürmt nicht los");
        });
    }

    /** Ein Lindwurm jagt Tiere. */
    private static void lindwormHunts(GameTestHelper helper) {
        arenaFloor(helper);
        var lindworm = helper.spawn(ModEntities.LINDWORM.get(), new BlockPos(2, 1, 4));
        var pig = helper.spawnWithNoFreeWill(EntityTypes.PIG, new BlockPos(5, 1, 4));
        helper.succeedWhen(() -> helper.assertTrue(pig.getHealth() < pig.getMaxHealth() || !pig.isAlive(),
                "Der Lindwurm greift das Schwein nicht an"));
    }

    /** Höhlentrolle und Soldaten bekämpfen sich. */
    private static void trollFightsSoldiers(GameTestHelper helper) {
        arenaFloor(helper);
        var troll = helper.spawn(ModEntities.CAVE_TROLL.get(), new BlockPos(2, 1, 4));
        SoldierEntity soldier = helper.spawn(ModEntities.SOLDIERS.get(SoldierType.SWORDSMAN).get(), new BlockPos(6, 1, 4));
        soldier.equip();
        helper.succeedWhen(() -> helper.assertTrue(soldier.getHealth() < soldier.getMaxHealth() || troll.getHealth() < troll.getMaxHealth(),
                "Troll und Soldat kämpfen nicht"));
    }

    /**
     * Drachen-Boss: unverwundbar beim Erwachen, danach kämpfbereit; Schaden von Nicht-Spielern zählt nur zum Teil,
     * Treffer am Kopf zählen mehr; nach dem Todesstoß folgt die Todesanimation, dann liegt das Ei im Nest.
     */
    private static void dragonBossLifecycle(GameTestHelper helper) {
        arenaFloor(helper);
        ServerLevel level = helper.getLevel();
        BlockPos homeRel = new BlockPos(4, 1, 4);
        BlockPos home = helper.absolutePos(homeRel);
        // Der Boss erwacht über den Nestkern unter seinem Nest
        BlockPos corePos = home.below(7);
        level.setBlock(corePos, ModBlocks.DRAGON_LAIR_CORE.get().defaultBlockState(), net.minecraft.world.level.block.Block.UPDATE_ALL);
        var core = (io.github.sirschrolli.kingdomsatwar.block.entity.DragonLairCoreBlockEntity) level.getBlockEntity(corePos);
        helper.assertTrue(core != null, "Nestkern ohne Blockobjekt");
        core.setup(DragonType.FIRE, home);
        var boss = core.awaken(level);
        helper.assertTrue(boss != null, "Boss lässt sich nicht erzeugen");
        helper.assertTrue(corePos.equals(boss.getLairCore()), "Boss kennt seinen Nestkern nicht");
        helper.assertTrue(core.phase() == io.github.sirschrolli.kingdomsatwar.world.LairRecords.Phase.AWAKE, "Nest ist nicht erwacht");
        helper.assertTrue(boss.getBossState() == io.github.sirschrolli.kingdomsatwar.entity.boss.DragonBossEntity.State.AWAKENING,
                "Boss erwacht nicht");
        helper.assertFalse(boss.hurtServer(level, level.damageSources().magic(), 10.0f), "Boss ist beim Erwachen verwundbar");
        int awake = boss.awakeningTicks() + 5;
        helper.runAfterDelay(awake, () -> {
            helper.assertTrue(boss.getBossState() == io.github.sirschrolli.kingdomsatwar.entity.boss.DragonBossEntity.State.FIGHT,
                    "Boss ist nach dem Erwachen nicht kampfbereit: " + boss.getBossState());
            helper.assertTrue(Math.abs(boss.getY() - home.getY()) < 1.5, "Boss ist nicht aus dem Nest gestiegen: " + boss.getY());
            float before = boss.getHealth();
            boss.hurtServer(level, level.damageSources().magic(), 50.0f);
            float body = before - boss.getHealth();
            helper.assertTrue(Math.abs(body - 50.0f * io.github.sirschrolli.kingdomsatwar.entity.boss.DragonBossEntity.NPC_DAMAGE_FACTOR) < 0.6f,
                    "Schaden ohne Spieler sollte gemindert sein: " + body);
            before = boss.getHealth();
            boss.bossParts()[0].hurtServer(level, level.damageSources().magic(), 50.0f);
            float head = before - boss.getHealth();
            helper.assertTrue(head > body * 1.3f, "Kopftreffer zählen nicht mehr: " + head + " / " + body);
            boss.bossParts()[0].discard();
            helper.assertFalse(boss.bossParts()[0].isRemoved(), "Trefferzone lässt sich einzeln entfernen");
            boss.setHealth(1.0f);
            boss.hurtServer(level, level.damageSources().magic(), 50.0f);
            helper.assertTrue(boss.isDeadOrDying(), "Boss stirbt nicht");
        });
        helper.runAfterDelay(awake + io.github.sirschrolli.kingdomsatwar.entity.boss.DragonBossEntity.DEATH_TICKS + 10, () -> {
            helper.assertTrue(boss.isRemoved(), "Boss wurde nach der Todesanimation nicht entfernt");
            helper.assertBlockPresent(ModBlocks.DRAGON_EGGS.get(DragonType.FIRE).get(), homeRel);
            helper.assertTrue(core.phase() == io.github.sirschrolli.kingdomsatwar.world.LairRecords.Phase.DEFEATED,
                    "Nest ist nach dem Sieg nicht besiegt: " + core.phase());
            helper.assertTrue(io.github.sirschrolli.kingdomsatwar.world.LairRecords.of(level).isDefeated(DragonType.FIRE),
                    "Die Welt weiß nichts vom Sieg");
            core.reset(level);
            helper.assertFalse(io.github.sirschrolli.kingdomsatwar.world.LairRecords.of(level).isDefeated(DragonType.FIRE),
                    "Nest lässt sich nicht zurücksetzen");
            level.setBlock(corePos, Blocks.STONE.defaultBlockState(), net.minecraft.world.level.block.Block.UPDATE_ALL);
            helper.succeed();
        });
    }

    /**
     * Drachen-Boss als echter Boss: Nester wecken ihn erst aus nächster Nähe (höchstens fünf Blöcke vom schlafenden
     * Drachen); je mehr Schaden er nimmt, desto härter und schneller schlägt er zu; beim Phasenwechsel bäumt er sich auf
     * und ist dabei kurz unverwundbar.
     */
    private static void dragonBossRage(GameTestHelper helper) {
        arenaFloor(helper);
        ServerLevel level = helper.getLevel();
        // Weckabstand: Rand des schlafenden Drachen + 5 Blöcke
        BlockPos center = helper.absolutePos(new BlockPos(4, 1, 4));
        for (DragonType element : DragonType.values()) {
            double r = io.github.sirschrolli.kingdomsatwar.block.entity.DragonLairCoreBlockEntity.sleepRadius(element)
                    + io.github.sirschrolli.kingdomsatwar.block.entity.DragonLairCoreBlockEntity.WAKE_DISTANCE;
            double cx = center.getX() + 0.5;
            double cz = center.getZ() + 0.5;
            helper.assertTrue(io.github.sirschrolli.kingdomsatwar.block.entity.DragonLairCoreBlockEntity.wakes(element, center,
                    cx + r - 0.5, center.getY() + 1, cz), "Weckt nicht in Reichweite: " + element);
            helper.assertFalse(io.github.sirschrolli.kingdomsatwar.block.entity.DragonLairCoreBlockEntity.wakes(element, center,
                    cx + r + 1.0, center.getY() + 1, cz), "Weckt schon im Vorbeigehen: " + element);
        }
        var boss = io.github.sirschrolli.kingdomsatwar.registry.ModEntities.bossType(DragonType.FIRE)
                .create(level, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
        helper.assertTrue(boss != null, "Boss lässt sich nicht erzeugen");
        boss.snapTo(center.getX() + 0.5, center.getY(), center.getZ() + 0.5, 0.0f, 0.0f);
        level.addFreshEntity(boss);
        boss.devPose(io.github.sirschrolli.kingdomsatwar.entity.boss.DragonBossEntity.MoveMode.GROUND);
        helper.assertTrue(Math.abs(boss.damageMultiplier() - 1.0f) < 1e-3, "Ungereizt schon verstärkt: " + boss.damageMultiplier());
        float max = boss.getMaxHealth();
        boss.setHealth(max * 0.5f);
        helper.assertTrue(boss.rage() > 0.45f && boss.damageMultiplier() > 1.25f && boss.tempo() > 1.15f,
                "Wut wirkt nicht: " + boss.rage() + " / " + boss.damageMultiplier() + " / " + boss.tempo());
        helper.runAfterDelay(3, () -> {
            helper.assertTrue(boss.getPhase() == 2, "Phase 2 beginnt nicht: " + boss.getPhase());
            helper.assertTrue(boss.isInTransition(), "Kein Aufbäumen beim Phasenwechsel");
            float before = boss.getHealth();
            helper.assertFalse(boss.hurtServer(level, level.damageSources().magic(), 20.0f), "Im Aufbäumen verwundbar");
            helper.assertTrue(boss.getHealth() == before, "Im Aufbäumen Schaden genommen");
        });
        helper.runAfterDelay(io.github.sirschrolli.kingdomsatwar.entity.boss.DragonBossEntity.TRANSITION_TICKS + 10, () -> {
            helper.assertFalse(boss.isInTransition(), "Aufbäumen endet nicht");
            float before = boss.getHealth();
            boss.hurtServer(level, level.damageSources().magic(), 20.0f);
            helper.assertTrue(boss.getHealth() < before, "Nach dem Aufbäumen unverwundbar");
            boss.discard();
            helper.succeed();
        });
    }

    /**
     * Drachenkompass: richtet sich auf ein Nest aus (Nadel über lodestone_tracker, Farbe des Elements, Ziel), und die
     * Abstandsregel der kleinen Bauwerke lässt je Feld genau einen Chunk zu.
     */
    private static void dragonCompass(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ItemStack stack = new ItemStack(ModItems.DRAGON_COMPASS.get());
        io.github.sirschrolli.kingdomsatwar.item.DragonCompassItem.aim(stack, level, DragonType.ICE, new net.minecraft.world.level.ChunkPos(20, -5));
        var target = stack.get(io.github.sirschrolli.kingdomsatwar.registry.ModDataComponents.COMPASS_TARGET.get());
        helper.assertTrue(target != null && target.element() == DragonType.ICE && !target.defeated(), "Falsches Ziel: " + target);
        var tracker = stack.get(net.minecraft.core.component.DataComponents.LODESTONE_TRACKER);
        helper.assertTrue(tracker != null && !tracker.tracked() && tracker.target().isPresent(), "Nadel ohne Ziel");
        var pos = tracker.target().get();
        helper.assertTrue(pos.dimension() == Level.OVERWORLD && pos.pos().getX() == 20 * 16 + 8 && pos.pos().getZ() == -5 * 16 + 8,
                "Nadel zeigt woandershin: " + pos);
        var color = stack.get(net.minecraft.core.component.DataComponents.CUSTOM_MODEL_DATA);
        helper.assertTrue(color != null && Integer.valueOf(DragonType.ICE.color()).equals(color.getColor(0)), "Edelstein nicht in Eisfarbe");
        // Abstandsregel: in einem Feld von 14×14 Chunks genau ein möglicher Chunk, nie in den letzten 6 Reihen
        for (int region = 0; region < 4; region++) {
            int found = 0;
            for (int x = 0; x < 14; x++) {
                for (int z = 0; z < 14; z++) {
                    int cx = region * 14 + x;
                    int cz = -region * 14 + z;
                    if (io.github.sirschrolli.kingdomsatwar.worldgen.SpacedPlacement.isGridChunk(1234L, 99, cx, cz, 14, 6)) {
                        found++;
                        helper.assertTrue(x < 8 && z < 8, "Bauwerks-Chunk zu nah am nächsten Feld: " + x + "/" + z);
                    }
                }
            }
            helper.assertTrue(found == 1, "Feld " + region + " hat " + found + " mögliche Chunks");
        }
        helper.succeed();
    }

    /**
     * Siegelkreis: Der Grundriss passt zusammen (Sockel ↔ Mitte, Monolithen im Sechseck, 25 verschiedene Platten), ein
     * erwachender Monolith lässt nur die Runen seines Elements leuchten und füllt den Sockel, und Siegel, Fortschritt und
     * Chronik überstehen Speichern und Netz unverändert.
     */
    private static void sealCircle(GameTestHelper helper) {
        arenaFloor(helper);
        ServerLevel level = helper.getLevel();
        BlockPos origin = new BlockPos(1000, 64, -2000);
        java.util.Set<BlockPos> monoliths = new java.util.HashSet<>();
        for (DragonType element : DragonType.values()) {
            BlockPos socket = io.github.sirschrolli.kingdomsatwar.worldgen.seal.SealCircleLayout.socket(origin, element);
            helper.assertTrue(io.github.sirschrolli.kingdomsatwar.worldgen.seal.SealCircleLayout.centerFromSocket(socket, element).equals(origin),
                    "Mitte aus dem Sockel falsch: " + element);
            BlockPos m = io.github.sirschrolli.kingdomsatwar.worldgen.seal.SealCircleLayout.monolith(origin, element);
            double d = Math.sqrt(m.distSqr(origin));
            helper.assertTrue(Math.abs(d - io.github.sirschrolli.kingdomsatwar.worldgen.seal.SealCircleLayout.MONOLITH_RADIUS) < 1.0,
                    "Monolith nicht auf dem Kreis: " + element + " " + d);
            Direction in = io.github.sirschrolli.kingdomsatwar.worldgen.seal.SealCircleLayout.inward(element);
            helper.assertTrue((origin.getX() - m.getX()) * in.getStepX() + (origin.getZ() - m.getZ()) * in.getStepZ() > 0,
                    "Sockel zeigt nicht zur Mitte: " + element);
            helper.assertTrue(io.github.sirschrolli.kingdomsatwar.worldgen.seal.SealCircleLayout.inlays(origin, element).size() >= 6,
                    "Zu wenige Bodenzeichen: " + element);
            monoliths.add(m);
        }
        helper.assertTrue(monoliths.size() == 6, "Monolithen fallen zusammen");
        java.util.Set<Integer> parts = new java.util.HashSet<>();
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                parts.add(io.github.sirschrolli.kingdomsatwar.worldgen.seal.SealCircleLayout.platePart(dx, dz));
            }
        }
        helper.assertTrue(parts.size() == 25 && java.util.Collections.min(parts) == 0 && java.util.Collections.max(parts) == 24,
                "Siegelplatten falsch nummeriert");

        // Ein Monolith erwacht (nachgebaut mitten in der Arena)
        DragonType fire = DragonType.FIRE;
        BlockPos m = helper.absolutePos(new BlockPos(5, 0, 5));
        BlockPos center = m.subtract(io.github.sirschrolli.kingdomsatwar.worldgen.seal.SealCircleLayout.monolith(BlockPos.ZERO, fire));
        Direction in = io.github.sirschrolli.kingdomsatwar.worldgen.seal.SealCircleLayout.inward(fire);
        BlockState rune = ModBlocks.SEAL_RUNE.get().defaultBlockState().setValue(io.github.sirschrolli.kingdomsatwar.block.SealMarkBlock.ELEMENT, fire);
        for (int y = 4; y <= 7; y++) {
            level.setBlock(m.relative(in).above(y), rune, Block.UPDATE_ALL);
        }
        BlockPos foreign = m.above(5);
        level.setBlock(foreign, rune.setValue(io.github.sirschrolli.kingdomsatwar.block.SealMarkBlock.ELEMENT, DragonType.ICE), Block.UPDATE_ALL);
        BlockPos socket = io.github.sirschrolli.kingdomsatwar.worldgen.seal.SealCircleLayout.socket(center, fire);
        level.setBlock(socket, ModBlocks.SEAL_SOCKET.get().defaultBlockState()
                .setValue(io.github.sirschrolli.kingdomsatwar.block.SealSocketBlock.ELEMENT, fire)
                .setValue(io.github.sirschrolli.kingdomsatwar.block.SealSocketBlock.FACING, in), Block.UPDATE_ALL);
        io.github.sirschrolli.kingdomsatwar.world.SealCircle.light(level, center, fire);
        for (int y = 4; y <= 7; y++) {
            helper.assertTrue(level.getBlockState(m.relative(in).above(y)).getValue(io.github.sirschrolli.kingdomsatwar.block.SealMarkBlock.LIT),
                    "Rune des Feuers leuchtet nicht (Höhe " + y + ")");
        }
        helper.assertFalse(level.getBlockState(foreign).getValue(io.github.sirschrolli.kingdomsatwar.block.SealMarkBlock.LIT),
                "Fremde Rune leuchtet mit");
        helper.assertTrue(level.getBlockState(socket).getValue(io.github.sirschrolli.kingdomsatwar.block.SealSocketBlock.FILLED),
                "Sockel nicht gefüllt");

        // Speichern und Netz
        io.github.sirschrolli.kingdomsatwar.world.SealState seal = new io.github.sirschrolli.kingdomsatwar.world.SealState();
        seal.insert(fire);
        seal.setCenter(origin);
        var sealTag = io.github.sirschrolli.kingdomsatwar.world.SealState.CODEC.encodeStart(net.minecraft.nbt.NbtOps.INSTANCE, seal).getOrThrow();
        var sealBack = io.github.sirschrolli.kingdomsatwar.world.SealState.CODEC.parse(net.minecraft.nbt.NbtOps.INSTANCE, sealTag).getOrThrow();
        helper.assertTrue(sealBack.isInserted(fire) && !sealBack.isInserted(DragonType.ICE) && origin.equals(sealBack.center())
                && !sealBack.isBreaking() && !sealBack.isBroken(), "Siegel falsch gespeichert: " + sealTag);
        io.github.sirschrolli.kingdomsatwar.story.StoryProgress progress = new io.github.sirschrolli.kingdomsatwar.story.StoryProgress();
        progress.set(io.github.sirschrolli.kingdomsatwar.story.StoryProgress.Flag.RECRUITED);
        progress.discover(DragonType.WATER);
        progress.setLastCard(3);
        var progressTag = io.github.sirschrolli.kingdomsatwar.story.StoryProgress.CODEC.encodeStart(net.minecraft.nbt.NbtOps.INSTANCE, progress)
                .getOrThrow();
        var progressBack = io.github.sirschrolli.kingdomsatwar.story.StoryProgress.CODEC.parse(net.minecraft.nbt.NbtOps.INSTANCE, progressTag)
                .getOrThrow();
        helper.assertTrue(progressBack.has(io.github.sirschrolli.kingdomsatwar.story.StoryProgress.Flag.RECRUITED)
                && !progressBack.has(io.github.sirschrolli.kingdomsatwar.story.StoryProgress.Flag.FOUND_CIRCLE)
                && progressBack.hasDiscovered(DragonType.WATER) && progressBack.lastCard() == 3, "Fortschritt falsch gespeichert: " + progressTag);
        var data = new io.github.sirschrolli.kingdomsatwar.story.ChronicleData(4, progress.flagBits(), 1, false,
                new io.github.sirschrolli.kingdomsatwar.story.ChronicleData.Place(2, 340), List.of(
                        new io.github.sirschrolli.kingdomsatwar.story.ChronicleData.Dragon(fire, 2, true, true,
                                new io.github.sirschrolli.kingdomsatwar.story.ChronicleData.Place(
                                        io.github.sirschrolli.kingdomsatwar.story.ChronicleData.HERE, 10)),
                        new io.github.sirschrolli.kingdomsatwar.story.ChronicleData.Dragon(DragonType.ICE, 0, false, false,
                                io.github.sirschrolli.kingdomsatwar.story.ChronicleData.Place.NONE)));
        net.minecraft.network.RegistryFriendlyByteBuf buf = new net.minecraft.network.RegistryFriendlyByteBuf(io.netty.buffer.Unpooled.buffer(),
                level.registryAccess());
        io.github.sirschrolli.kingdomsatwar.story.ChronicleData.STREAM_CODEC.encode(buf, data);
        var copy = io.github.sirschrolli.kingdomsatwar.story.ChronicleData.STREAM_CODEC.decode(buf);
        helper.assertTrue(copy.equals(data), "Chronik übers Netz verändert: " + copy);
        helper.assertTrue(copy.current() == io.github.sirschrolli.kingdomsatwar.story.Chapter.DRAGON_2 && copy.insertedCount() == 1,
                "Kapitel oder Splitterzahl falsch");
        helper.succeed();
    }

    /**
     * Sitzmöbel: Ein Klick setzt den Spieler mit Blick nach vorn auf den Stuhl, ein zweiter findet dort keinen Platz,
     * auf dem Thron sitzt auch ein Ritter. Wird der Stuhl abgebaut, steht der Spieler wieder auf.
     */
    private static void furnitureSeats(GameTestHelper helper) {
        arenaFloor(helper);
        ServerLevel level = helper.getLevel();
        BlockPos chair = new BlockPos(2, 1, 2);
        helper.setBlock(chair, ModBlocks.OAK_CHAIR.get().defaultBlockState().setValue(SeatBlock.FACING, Direction.SOUTH));
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.snapTo(helper.absoluteVec(new Vec3(2.5, 1, 4.5)));
        helper.useBlock(chair, player);
        helper.assertTrue(player.getVehicle() instanceof SeatEntity, "Spieler sitzt nicht auf dem Stuhl");
        helper.assertTrue(Math.abs(net.minecraft.util.Mth.wrapDegrees(player.getYRot() - Direction.SOUTH.toYRot())) < 1.0f,
                "Spieler schaut nicht nach vorn");
        double seatTop = helper.absolutePos(chair).getY() + 9.0 / 16.0;
        helper.assertTrue(Math.abs(player.getY() + 0.6 - seatTop) < 0.05, "Spieler sitzt nicht auf der Sitzfläche: " + player.getY());

        SoldierEntity knight = helper.spawn(ModEntities.SOLDIERS.get(SoldierType.KNIGHT).get(), new BlockPos(5, 1, 5));
        knight.setNoAi(true);
        helper.assertFalse(SeatEntity.sit(level, helper.absolutePos(chair), 9.0 / 16.0, Direction.SOUTH, knight),
                "Zwei sitzen auf demselben Stuhl");
        BlockPos throne = new BlockPos(6, 1, 2);
        helper.setBlock(throne, ModBlocks.THRONE.get());
        SeatBlock throneBlock = ModBlocks.THRONE.get();
        helper.assertTrue(SeatEntity.sit(level, helper.absolutePos(throne), throneBlock.seatHeight(), Direction.NORTH, knight),
                "Der Ritter kann sich nicht auf den Thron setzen");

        helper.destroyBlock(chair);
        helper.runAfterDelay(3, () -> {
            helper.assertFalse(player.isPassenger(), "Spieler sitzt noch, obwohl der Stuhl abgebaut ist");
            helper.assertTrue(knight.isPassenger(), "Der Ritter ist vom Thron gefallen");
            helper.assertTrue(level.getEntitiesOfClass(SeatEntity.class, new net.minecraft.world.phys.AABB(helper.absolutePos(chair))).isEmpty(),
                    "Der Sitz des abgebauten Stuhls ist nicht verschwunden");
            helper.succeed();
        });
    }

    /**
     * Einrichtung: Tische verbinden sich zur Tafel, der Kronleuchter fällt ohne Halt herunter, Zinnen bilden
     * Zahn und Scharte quer zur Blickrichtung, Burgtür und Fensterläden lassen sich öffnen.
     */
    private static void decorBlocks(GameTestHelper helper) {
        arenaFloor(helper);
        ServerLevel level = helper.getLevel();
        // Tafel aus zwei Tischen: Beine nur an den äußeren Ecken
        Player player = helper.makeMockPlayer(GameType.CREATIVE);
        placeWithHand(helper, player, ModBlocks.OAK_TABLE.get(), new BlockPos(2, 0, 2));
        placeWithHand(helper, player, ModBlocks.OAK_TABLE.get(), new BlockPos(3, 0, 2));
        BlockState west = helper.getBlockState(new BlockPos(2, 1, 2));
        BlockState east = helper.getBlockState(new BlockPos(3, 1, 2));
        helper.assertTrue(west.is(ModBlocks.OAK_TABLE.get()) && west.getValue(TableBlock.EAST) && !west.getValue(TableBlock.WEST),
                "Linker Tisch ist nicht mit dem rechten verbunden: " + west);
        helper.assertTrue(east.getValue(TableBlock.WEST) && !east.getValue(TableBlock.EAST),
                "Rechter Tisch ist nicht mit dem linken verbunden: " + east);

        // Kronleuchter hängt an der Decke und fällt ohne sie herunter
        BlockPos ceiling = new BlockPos(6, 4, 6);
        helper.setBlock(ceiling, Blocks.OAK_PLANKS);
        helper.setBlock(ceiling.below(), ModBlocks.CHANDELIER.get());
        helper.assertTrue(helper.getBlockState(ceiling.below()).canSurvive(level, helper.absolutePos(ceiling.below())),
                "Kronleuchter hält nicht an der Decke");
        helper.setBlock(ceiling, Blocks.AIR);
        helper.assertBlockNotPresent(ModBlocks.CHANDELIER.get(), ceiling.below());

        // Zinnen: Mauerzahn über die volle Tiefe, Scharte an den Seiten
        BlockState battlement = ModBlocks.MEDIEVAL_STONE_BATTLEMENT.get().defaultBlockState().setValue(BattlementBlock.FACING, Direction.NORTH);
        var shape = battlement.getShape(level, BlockPos.ZERO);
        helper.assertTrue(contains(shape, 0.5, 0.75, 0.05) && contains(shape, 0.5, 0.75, 0.95), "Mauerzahn fehlt");
        helper.assertTrue(!contains(shape, 0.1, 0.75, 0.5) && contains(shape, 0.1, 0.25, 0.5), "Scharte fehlt");
        var turned = battlement.setValue(BattlementBlock.FACING, Direction.EAST).getShape(level, BlockPos.ZERO);
        helper.assertTrue(contains(turned, 0.05, 0.75, 0.5) && !contains(turned, 0.5, 0.75, 0.1), "Gedrehte Zinne falsch");

        // Burgtür und Fensterläden öffnen sich per Hand
        Player hand = helper.makeMockPlayer(GameType.SURVIVAL);
        placeWithHand(helper, hand, ModBlocks.CASTLE_DOOR.get(), new BlockPos(1, 0, 6));
        helper.assertBlockPresent(ModBlocks.CASTLE_DOOR.get(), new BlockPos(1, 2, 6));
        helper.useBlock(new BlockPos(1, 1, 6), hand);
        helper.assertTrue(helper.getBlockState(new BlockPos(1, 1, 6)).getValue(net.minecraft.world.level.block.DoorBlock.OPEN),
                "Burgtür öffnet sich nicht");
        helper.setBlock(new BlockPos(3, 1, 6), ModBlocks.GREEN_SHUTTERS.get());
        helper.useBlock(new BlockPos(3, 1, 6), hand);
        helper.assertTrue(helper.getBlockState(new BlockPos(3, 1, 6)).getValue(net.minecraft.world.level.block.TrapDoorBlock.OPEN),
                "Fensterladen öffnet sich nicht");
        helper.succeed();
    }

    /**
     * Festmahl: vier Portionen, dann ist es aufgegessen. Bier, Wein und Met wirken und lassen die Flasche übrig.
     * Die Mondblume leuchtet.
     */
    private static void drinksAndFeast(GameTestHelper helper) {
        arenaFloor(helper);
        ServerLevel level = helper.getLevel();
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.getFoodData().setFoodLevel(0);
        BlockPos feast = new BlockPos(2, 1, 2);
        helper.setBlock(feast, ModBlocks.FEAST.get());
        for (int i = 0; i < FeastBlock.SERVINGS; i++) {
            helper.assertBlockPresent(ModBlocks.FEAST.get(), feast);
            helper.useBlock(feast, player);
        }
        helper.assertBlockNotPresent(ModBlocks.FEAST.get(), feast);
        helper.assertTrue(player.getFoodData().getFoodLevel() == 20, "Festmahl macht nicht satt: " + player.getFoodData().getFoodLevel());

        java.util.Map<Item, net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect>> drinks = java.util.Map.of(
                ModItems.ALE.get(), MobEffects.STRENGTH, ModItems.WINE.get(), MobEffects.REGENERATION,
                ModItems.MEAD.get(), MobEffects.ABSORPTION);
        drinks.forEach((drink, effect) -> {
            ItemStack rest = new ItemStack(drink).finishUsingItem(level, player);
            helper.assertTrue(player.hasEffect(effect), drink + " wirkt nicht");
            helper.assertTrue(rest.is(Items.GLASS_BOTTLE), drink + " lässt keine leere Flasche übrig, sondern " + rest);
        });
        helper.assertTrue(ModBlocks.MOONFLOWER.get().defaultBlockState().getLightEmission(level, BlockPos.ZERO) > 0, "Mondblume leuchtet nicht");
        helper.succeed();
    }

    /**
     * Aetherit-Stab: Der Aether-Blitz trifft das anvisierte Ziel, Frost verlangsamt, der Erdstoß verletzt Feinde,
     * aber nicht den eigenen Wolf, Natur heilt, die Woge schenkt Wasseratmung, Feuer und Blitz erscheinen. Höhere
     * Stufen sind stärker. Ein neuer Stab kennt nur den Aether-Blitz; gewählt werden kann nur, was er gelernt hat. Der
     * gewählte Zauber kostet Haltbarkeit und hat seine eigene Abklingzeit – ein anderer Zauber ist sofort bereit.
     */
    private static void staffSpells(GameTestHelper helper) {
        arenaFloor(helper);
        ServerLevel level = helper.getLevel();
        Player mage = helper.makeMockPlayer(GameType.SURVIVAL);
        mage.snapTo(helper.absoluteVec(new Vec3(4.5, 1, 1.5)), 0.0f, 8.0f);            // Blick nach Süden
        var golem = helper.spawn(EntityTypes.IRON_GOLEM, new BlockPos(4, 1, 5));         // vorn: Blitz und Frost
        golem.setNoAi(true);
        var flank = helper.spawn(EntityTypes.IRON_GOLEM, new BlockPos(8, 1, 1));         // seitlich: Erdstoß
        flank.setNoAi(true);
        var wolf = helper.spawn(EntityTypes.WOLF, new BlockPos(6, 1, 2));
        wolf.setNoAi(true);
        wolf.tame(mage);

        float health = golem.getHealth();
        helper.assertTrue(Spell.AETHER_BOLT.cast(level, mage) && golem.getHealth() < health, "Der Aether-Blitz trifft nicht");
        helper.assertTrue(flank.getHealth() == flank.getMaxHealth(), "Der Aether-Blitz trifft auch daneben");
        // Stufe III trifft härter als Stufe I (Schaden je Stufe: 100 %, 135 %, 170 %)
        golem.setHealth(golem.getMaxHealth());
        golem.setInvulnerableTime(0);
        Spell.AETHER_BOLT.cast(level, mage, 1);
        float lost1 = golem.getMaxHealth() - golem.getHealth();
        golem.setHealth(golem.getMaxHealth());
        golem.setInvulnerableTime(0);
        Spell.AETHER_BOLT.cast(level, mage, 3);
        float lost3 = golem.getMaxHealth() - golem.getHealth();
        helper.assertTrue(lost3 > lost1 * 1.4f, "Stufe III ist nicht stärker: " + lost1 + " / " + lost3);
        Spell.ICE.cast(level, mage);
        helper.assertTrue(golem.hasEffect(MobEffects.SLOWNESS) && !flank.hasEffect(MobEffects.SLOWNESS),
                "Frosthauch trifft nicht (nur) den Kegel vor dem Zaubernden");
        float wolfHealth = wolf.getHealth();
        Spell.EARTH.cast(level, mage);
        helper.assertTrue(flank.getHealth() < flank.getMaxHealth(), "Erdstoß trifft den Golem nicht");
        helper.assertTrue(wolf.getHealth() == wolfHealth, "Erdstoß verletzt den eigenen Wolf");
        Spell.NATURE.cast(level, mage, 2);
        helper.assertTrue(mage.hasEffect(MobEffects.REGENERATION) && wolf.hasEffect(MobEffects.REGENERATION), "Natur heilt nicht");
        helper.assertTrue(mage.hasEffect(MobEffects.ABSORPTION), "Natur Stufe II gibt keinen Schild");
        Spell.WATER.cast(level, mage);
        helper.assertTrue(mage.hasEffect(MobEffects.WATER_BREATHING), "Woge schenkt keine Wasseratmung");
        Spell.FIRE.cast(level, mage, 2);
        helper.assertTrue(level.getEntitiesOfClass(net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball.class,
                mage.getBoundingBox().inflate(4)).size() == 3, "Feuer Stufe II schießt nicht drei Feuerbälle");

        // Der Stab: zuerst nur der Aether-Blitz; gelernte Zauber lassen sich wählen, ungelernte nicht
        ItemStack staff = new ItemStack(ModItems.AETHERIT_STAFF.get());
        helper.assertTrue(KnownSpells.of(staff).equals(KnownSpells.DEFAULT), "Neuer Stab kennt mehr als den Aether-Blitz");
        AetheritStaffItem.setSpells(staff, KnownSpells.DEFAULT.withLevel(Spell.LIGHTNING, 2));
        helper.assertTrue(!AetheritStaffItem.select(staff, Spell.FIRE), "Ungelernter Zauber lässt sich wählen");
        helper.assertTrue(AetheritStaffItem.select(staff, Spell.LIGHTNING) && KnownSpells.of(staff).selected() == Spell.LIGHTNING,
                "Gelernter Zauber lässt sich nicht wählen");
        var cooldown = staff.get(net.minecraft.core.component.DataComponents.USE_COOLDOWN);
        helper.assertTrue(cooldown != null && cooldown.cooldownGroup().equals(java.util.Optional.of(Spell.LIGHTNING.cooldownGroup()))
                && cooldown.ticks() == Spell.LIGHTNING.cooldown(2), "Abklingzeit des Stabs passt nicht zum gewählten Zauber");
        mage.setItemInHand(InteractionHand.MAIN_HAND, staff);
        staff.use(level, mage, InteractionHand.MAIN_HAND);
        helper.assertTrue(staff.getDamageValue() == 3, "Blitz kostet nicht 3 Haltbarkeit: " + staff.getDamageValue());
        helper.assertTrue(mage.getCooldowns().isOnCooldown(staff), "Blitzschlag hat keine Abklingzeit");
        helper.assertTrue(!level.getEntitiesOfClass(net.minecraft.world.entity.LightningBolt.class, golem.getBoundingBox().inflate(3)).isEmpty(),
                "Kein Blitz am Ziel");
        AetheritStaffItem.select(staff, Spell.AETHER_BOLT);
        helper.assertTrue(!mage.getCooldowns().isOnCooldown(staff), "Der Aether-Blitz teilt sich die Abklingzeit mit dem Blitzschlag");
        // Speichern und Laden der gelernten Zauber
        KnownSpells spells = KnownSpells.of(staff);
        var tag = KnownSpells.CODEC.encodeStart(net.minecraft.nbt.NbtOps.INSTANCE, spells).getOrThrow();
        helper.assertTrue(KnownSpells.CODEC.parse(net.minecraft.nbt.NbtOps.INSTANCE, tag).getOrThrow().equals(spells),
                "Gelernte Zauber gehen beim Speichern verloren");
        helper.succeed();
    }

    /**
     * Bauwerke: In der Mitte von Pyrrhons Vulkankrater liegt der Lavasee mit dem Nestkern darunter; eine Burgruine hat
     * Mauern aus Mod-Steinen und eine Truhe mit Beute. Banditen werden für den Test gleich entfernt.
     */
    private static void structuresGenerate(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        var generator = level.getChunkSource().getGenerator();
        BlockPos center = helper.absolutePos(new BlockPos(4, 1, 4));
        flatGround(helper);
        // Mitte des Vulkankraters (nur der Teil über der Arena wird gebaut): Lavasee, Basalt, Nestkern darunter
        var lairCenter = center.below(1);
        var area = new net.minecraft.world.level.levelgen.structure.BoundingBox(center.getX() - 4, center.getY() - 10, center.getZ() - 4,
                center.getX() + 4, center.getY() + 12, center.getZ() + 4);
        new io.github.sirschrolli.kingdomsatwar.worldgen.lair.DragonLairPiece(DragonType.FIRE, lairCenter, 7L)
                .postProcess(level, level.structureManager(), generator, RandomSource.create(7), area,
                        net.minecraft.world.level.ChunkPos.containing(center), lairCenter);
        helper.assertTrue(level.getBlockState(lairCenter.below(2)).is(Blocks.LAVA), "Kein Lavasee in der Kratermitte");
        helper.assertTrue(level.getBlockState(lairCenter.below(7)).is(ModBlocks.DRAGON_LAIR_CORE.get()), "Kein Nestkern unter dem Lavasee");
        helper.assertTrue(level.getBlockEntity(lairCenter.below(7))
                instanceof io.github.sirschrolli.kingdomsatwar.block.entity.DragonLairCoreBlockEntity core && core.element() == DragonType.FIRE,
                "Nestkern ohne Feuer-Element");
        for (var pos : BlockPos.betweenClosed(area.minX(), lairCenter.getY() - 9, area.minZ(), area.maxX(), area.maxY(), area.maxZ())) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), net.minecraft.world.level.block.Block.UPDATE_CLIENTS);
        }

        flatGround(helper);
        helper.assertTrue(new io.github.sirschrolli.kingdomsatwar.worldgen.CastleRuinFeature()
                .place(level, generator, RandomSource.create(11), center), "Die Burgruine wurde nicht gebaut");
        removeSpawned(helper);
        helper.assertTrue(countInArea(helper, ModBlocks.STONE_PILLAR.get()) >= 4, "Ruine ohne Ecktürme");
        boolean chest = false;
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-4, 0, -4), center.offset(4, 0, 4))) {
            if (level.getBlockEntity(pos) instanceof net.minecraft.world.level.block.entity.ChestBlockEntity box
                    && io.github.sirschrolli.kingdomsatwar.loot.ModLootTables.CASTLE_RUIN_CHEST.equals(box.getLootTable())) {
                chest = true;
            }
        }
        helper.assertTrue(chest, "Ruine ohne Beutetruhe");
        flatGround(helper);
        helper.succeed();
    }

    /** Ebener Steinboden (13×13 um die Arena) und darüber freie Luft. */
    private static void flatGround(GameTestHelper helper) {
        for (int x = -2; x <= 10; x++) {
            for (int z = -2; z <= 10; z++) {
                helper.setBlock(x, 0, z, Blocks.STONE);
                for (int y = 1; y <= 9; y++) {
                    helper.setBlock(x, y, z, Blocks.AIR);
                }
            }
        }
    }

    private static int countInArea(GameTestHelper helper, Block block) {
        int count = 0;
        for (BlockPos pos : BlockPos.betweenClosed(helper.absolutePos(new BlockPos(-2, -1, -2)), helper.absolutePos(new BlockPos(10, 9, 10)))) {
            if (helper.getLevel().getBlockState(pos).is(block)) {
                count++;
            }
        }
        return count;
    }

    /** Vom Bauwerk erzeugte Drachen und Banditen entfernen (sie sollen keine anderen Tests stören). */
    private static void removeSpawned(GameTestHelper helper) {
        net.minecraft.world.phys.AABB area = new net.minecraft.world.phys.AABB(Vec3.atLowerCornerOf(helper.absolutePos(new BlockPos(-3, -1, -3))),
                Vec3.atLowerCornerOf(helper.absolutePos(new BlockPos(11, 10, 11))));
        helper.getLevel().getEntitiesOfClass(net.minecraft.world.entity.Mob.class, area,
                mob -> mob instanceof DragonEntity || mob instanceof io.github.sirschrolli.kingdomsatwar.entity.soldier.BanditEntity)
                .forEach(net.minecraft.world.entity.Entity::discard);
    }

    /** Setzt den Block wie ein Spieler mit dem Gegenstand in der Hand oben auf {@code below}. */
    private static void placeWithHand(GameTestHelper helper, Player player, Block block, BlockPos below) {
        ItemStack stack = new ItemStack(block);
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        helper.placeAt(player, stack, below, Direction.UP);
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
    }

    private static boolean contains(net.minecraft.world.phys.shapes.VoxelShape shape, double x, double y, double z) {
        return shape.toAabbs().stream().anyMatch(box -> box.contains(x, y, z));
    }

    // =====================================================================================
    // Hilfsmethoden
    // =====================================================================================

    /** Steinboden der Test-Arena (die Struktur wird beim Teststart nicht zuverlässig mit Boden gesetzt). */
    private static void arenaFloor(GameTestHelper helper) {
        for (int x = 0; x < 9; x++) {
            for (int z = 0; z < 9; z++) {
                helper.setBlock(x, 0, z, Blocks.STONE);
            }
        }
    }

    private static void fillPlatform(GameTestHelper helper, Block block) {
        for (int x = 0; x < 5; x++) {
            for (int y = 0; y < 3; y++) {
                for (int z = 0; z < 5; z++) {
                    helper.setBlock(x, y, z, block);
                }
            }
        }
    }

    private static boolean containsBlock(GameTestHelper helper, Block block) {
        for (int x = 0; x < 5; x++) {
            for (int y = 0; y < 3; y++) {
                for (int z = 0; z < 5; z++) {
                    if (helper.getBlockState(new BlockPos(x, y, z)).is(block)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static void assertDrops(GameTestHelper helper, DeferredBlock<? extends Block> block, Item expected) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(TEST_POS);
        BlockState state = block.get().defaultBlockState();
        List<ItemStack> drops = Block.getDrops(state, level, pos, null, null, new ItemStack(Items.NETHERITE_PICKAXE));
        helper.assertTrue(drops.stream().anyMatch(stack -> stack.is(expected)),
                block.getId() + " lässt nicht " + expected + " fallen, sondern " + drops);
    }

    private static void assertCropDrops(GameTestHelper helper, DeferredBlock<? extends CropBlock> crop, Item expected) {
        BlockState ripe = crop.get().getStateForAge(crop.get().getMaxAge());
        helper.setBlock(TEST_POS, ripe);
        helper.assertBlockProperty(TEST_POS, CropBlock.AGE, CropBlock.MAX_AGE);
        List<ItemStack> drops = Block.getDrops(ripe, helper.getLevel(), helper.absolutePos(TEST_POS), null);
        helper.assertTrue(drops.stream().anyMatch(stack -> stack.is(expected)),
                crop.getId() + " lässt nicht " + expected + " fallen, sondern " + drops);
    }
}
