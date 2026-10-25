package io.github.sirschrolli.kingdomsatwar.gametest;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.block.ElementalDragonEggBlock;
import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import io.github.sirschrolli.kingdomsatwar.entity.dragon.DragonEntity;
import io.github.sirschrolli.kingdomsatwar.entity.dragon.DragonStage;
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
import net.minecraft.world.entity.EntityTypes;
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

    private ModGameTests() {}

    /** Wird nur auf dem Test-Server bzw. in der Entwicklungsumgebung ausgelöst. */
    public static void registerTests(RegisterGameTestsEvent event) {
        Holder<TestEnvironmentDefinition<?>> environment = event.registerEnvironment(KingdomsAtWar.id("default"));
        for (DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> test :
                List.of(PLACE_BLOCKS, LOOT_TABLES, ORE_DROPS, CROP_HARVEST, RECIPES, WORLDGEN, ORE_VEINS, FLOWER_PATCHES,
                        FLOWER_POTS, WOOD_BURNS)) {
            event.registerTest(test.getId(), new FunctionGameTestInstance(test.key(),
                    new TestData<>(environment, PLATFORM, 100, 0, true)));
        }
        for (DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> test :
                List.of(DRAGON_EGG_INCUBATION, DRAGON_EGG_HATCHES, DRAGON_GROWTH, DRAGON_FEEDING, DRAGON_BREATH)) {
            event.registerTest(test.getId(), new FunctionGameTestInstance(test.key(),
                    new TestData<>(environment, Level.OVERWORLD, ARENA, 200, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
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
