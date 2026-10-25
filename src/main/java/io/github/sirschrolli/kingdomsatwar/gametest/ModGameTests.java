package io.github.sirschrolli.kingdomsatwar.gametest;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.registry.ModBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.ModItems;
import io.github.sirschrolli.kingdomsatwar.worldgen.ModWorldgen;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.storage.loot.LootTable;
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

    private ModGameTests() {}

    /** Wird nur auf dem Test-Server bzw. in der Entwicklungsumgebung ausgelöst. */
    public static void registerTests(RegisterGameTestsEvent event) {
        Holder<TestEnvironmentDefinition<?>> environment = event.registerEnvironment(KingdomsAtWar.id("default"));
        for (DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> test :
                List.of(PLACE_BLOCKS, LOOT_TABLES, ORE_DROPS, CROP_HARVEST, RECIPES, WORLDGEN, ORE_VEINS, FLOWER_PATCHES)) {
            event.registerTest(test.getId(), new FunctionGameTestInstance(test.key(),
                    new TestData<>(environment, PLATFORM, 100, 0, true)));
        }
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

    /** Jeder Block hat eine Loot-Table, die vom Server geladen wurde. */
    private static void lootTables(GameTestHelper helper) {
        var registries = helper.getLevel().getServer().reloadableRegistries();
        for (DeferredHolder<Block, ? extends Block> holder : ModBlocks.BLOCKS.getEntries()) {
            Block block = holder.value();
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
        helper.succeed();
    }

    /** Einige wichtige Rezepte wurden geladen. */
    private static void recipes(GameTestHelper helper) {
        var recipes = helper.getLevel().getServer().getRecipeManager().recipeMap();
        for (String name : List.of("steel_blend", "steel_ingot_from_blasting", "dragon_egg_fragment", "fire_dragon_egg",
                "medieval_stone_stairs", "red_roof_tile_stairs", "konterit_block", "vegetable_stew",
                "medieval_stone_from_stone_stonecutting", "draconium_from_smelting_draconium_ore")) {
            ResourceKey<Recipe<?>> key = ResourceKey.create(Registries.RECIPE, KingdomsAtWar.id(name));
            helper.assertTrue(recipes.byKey(key) != null, "Rezept fehlt: " + key.identifier());
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

    // =====================================================================================
    // Hilfsmethoden
    // =====================================================================================

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
