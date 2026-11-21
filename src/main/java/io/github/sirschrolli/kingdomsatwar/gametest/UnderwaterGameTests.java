package io.github.sirschrolli.kingdomsatwar.gametest;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.block.sea.SeaSaplingBlock;
import io.github.sirschrolli.kingdomsatwar.registry.ModTags;
import io.github.sirschrolli.kingdomsatwar.registry.SeaWoods;
import io.github.sirschrolli.kingdomsatwar.urmeer.UrmeerBiomes;
import io.github.sirschrolli.kingdomsatwar.urmeer.feature.SeaTreeFeature;
import java.util.ArrayList;
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
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootTable;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * Tests der Unterwasserwelt des Urmeers (0.92): die vier Meereshölzer (Blöcke, Rezepte, Beute, Tags), ihre Bäume unter
 * Wasser (Laub geflutet und mit Abstand zum Holz, nie bis an die Oberfläche), die Setzlinge (wachsen unter Wasser) und
 * die vier neuen Biome.
 */
final class UnderwaterGameTests {
    private static final Identifier ARENA = KingdomsAtWar.id("test_arena");
    /** Ein Becken um die Mitte des Testplatzes: so weit hinaus und so hoch steht Wasser. */
    private static final int REACH = 8;
    private static final int DEPTH = 26;

    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> SEA_WOODS =
            ModGameTests.TEST_FUNCTIONS.register("sea_woods", () -> UnderwaterGameTests::seaWoods);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> SEA_SAPLING =
            ModGameTests.TEST_FUNCTIONS.register("sea_sapling", () -> UnderwaterGameTests::seaSapling);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> URMEER_UNDERWATER_BIOMES =
            ModGameTests.TEST_FUNCTIONS.register("urmeer_underwater_biomes", () -> UnderwaterGameTests::biomes);
    static final List<DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>>> TREES = new ArrayList<>();

    static {
        for (SeaTreeFeature.Kind kind : SeaTreeFeature.Kind.values()) {
            TREES.add(ModGameTests.TEST_FUNCTIONS.register("sea_tree_" + kind.getSerializedName(), () -> helper -> seaTree(helper, kind)));
        }
    }

    private UnderwaterGameTests() {}

    /** Lädt die Klasse (meldet die Testfunktionen an). */
    static void init() {}

    static void register(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment) {
        for (var test : List.of(SEA_WOODS, URMEER_UNDERWATER_BIOMES)) {
            event.registerTest(test.getId(), new FunctionGameTestInstance(test.key(),
                    new TestData<>(environment, Level.OVERWORLD, ARENA, 200, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
        }
        // die Bäume brauchen ein Becken, das über den Testplatz hinausreicht
        List<DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>>> pools = new ArrayList<>(TREES);
        pools.add(SEA_SAPLING);
        for (var test : pools) {
            event.registerTest(test.getId(), new FunctionGameTestInstance(test.key(),
                    new TestData<>(environment, Level.OVERWORLD, ARENA, 200, 0, true, Rotation.NONE, false, 1, 1, true, 12)));
        }
    }

    /** Ein Becken: Sandgrund, darüber {@link #DEPTH} Blöcke Wasser, ringsum bis {@link #REACH} Blöcke um die Mitte (4, 4). */
    private static void pool(GameTestHelper helper) {
        for (int x = 4 - REACH; x <= 4 + REACH; x++) {
            for (int z = 4 - REACH; z <= 4 + REACH; z++) {
                helper.setBlock(x, 0, z, Blocks.SAND);
                for (int y = 1; y <= DEPTH; y++) {
                    helper.setBlock(x, y, z, Blocks.WATER);
                }
            }
        }
    }

    /**
     * Die Meereshölzer: jeder Satz vollständig (Stamm, Holz, entrindet, Bretter, Treppe, Stufe, Tür, Falltür, Zaun,
     * Zauntor, Laub, Setzling) mit Gegenstand, in den Holz-Tags, mit Rezepten und Beute; die Axt entrindet.
     */
    private static void seaWoods(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        helper.assertTrue(SeaWoods.ALL.size() == 4, "Nicht vier Meereshölzer: " + SeaWoods.ALL.size());
        var recipes = level.getServer().getRecipeManager();
        for (SeaWoods.SeaWood wood : SeaWoods.ALL) {
            for (var block : wood.blocks()) {
                helper.assertFalse(new ItemStack(block.get()).isEmpty(), "Kein Gegenstand: " + block.getId());
                ResourceKey<LootTable> loot = block.get().getLootTable().orElse(null);
                helper.assertTrue(loot != null && level.getServer().reloadableRegistries().getLootTable(loot) != LootTable.EMPTY,
                        "Keine Beute: " + block.getId());
            }
            helper.assertTrue(wood.log.get().defaultBlockState().is(BlockTags.LOGS) && wood.log.get().defaultBlockState().is(wood.logsTag),
                    "Stamm nicht bei den Stämmen: " + wood.name);
            helper.assertTrue(new ItemStack(wood.log.get()).is(ItemTags.LOGS) && new ItemStack(wood.log.get()).is(wood.logsItemTag),
                    "Stamm-Gegenstand nicht bei den Stämmen: " + wood.name);
            helper.assertTrue(wood.planks.base().get().defaultBlockState().is(BlockTags.PLANKS)
                    && new ItemStack(wood.planks.base().get()).is(ItemTags.PLANKS), "Bretter nicht bei den Brettern: " + wood.name);
            helper.assertTrue(wood.leaves.get().defaultBlockState().is(BlockTags.LEAVES), "Laub nicht beim Laub: " + wood.name);
            helper.assertTrue(new ItemStack(wood.sapling.get()).is(ItemTags.SAPLINGS), "Setzling nicht bei den Setzlingen: " + wood.name);
            helper.assertTrue(wood.door.get().defaultBlockState().is(BlockTags.WOODEN_DOORS)
                    && wood.fence.get().defaultBlockState().is(BlockTags.WOODEN_FENCES), "Tür oder Zaun nicht aus Holz: " + wood.name);
            for (String recipe : List.of(wood.name + "_planks", wood.name + "_wood", "stripped_" + wood.name + "_wood", wood.name + "_door",
                    wood.name + "_trapdoor", wood.name + "_fence", wood.name + "_fence_gate")) {
                helper.assertTrue(recipes.byKey(ResourceKey.create(Registries.RECIPE, KingdomsAtWar.id(recipe))).isPresent(), "Rezept fehlt: " + recipe);
            }
            // das Laub hält Wasser
            BlockState leaf = wood.leaves.get().defaultBlockState().setValue(LeavesBlock.WATERLOGGED, true);
            helper.assertTrue(leaf.getFluidState().is(net.minecraft.tags.FluidTags.WATER), "Das Laub hält kein Wasser: " + wood.name);
            // die Axt entrindet
            var strip = net.neoforged.neoforge.registries.datamaps.builtin.NeoForgeDataMaps.TRANSFORMABLES;
            helper.assertTrue(wood.log.get().builtInRegistryHolder().getData(strip) != null, "Die Axt entrindet nicht: " + wood.name);
        }
        // Tiefseeholz leuchtet ein wenig, seine Wedel stark; Perlblätter schimmern
        helper.assertTrue(SeaWoods.ABYSS.leaves.get().defaultBlockState().getLightEmission() >= 10, "Die Leuchtwedel leuchten nicht");
        helper.assertTrue(SeaWoods.PEARL.leaves.get().defaultBlockState().getLightEmission() > 0, "Die Perlblätter schimmern nicht");
        helper.succeed();
    }

    /**
     * Ein Meeresbaum in einem Becken: Holz der richtigen Art, Laub geflutet und mit Abstand zum Holz (es welkt nicht), nichts
     * höher als drei Blöcke unter der Oberfläche.
     */
    private static void seaTree(GameTestHelper helper, SeaTreeFeature.Kind kind) {
        ServerLevel level = helper.getLevel();
        pool(helper);
        BlockPos origin = helper.absolutePos(new BlockPos(4, 1, 4));
        boolean placed = false;
        for (int attempt = 0; attempt < 6 && !placed; attempt++) {
            placed = new SeaTreeFeature(kind).place(level, level.getChunkSource().getGenerator(), RandomSource.create(9_200L + attempt), origin);
        }
        helper.assertTrue(placed, "Der Baum wächst nicht: " + kind.getSerializedName());
        SeaWoods.SeaWood wood = switch (kind) {
            case KELP -> SeaWoods.KELP;
            case CORAL -> SeaWoods.CORAL;
            case PEARL -> SeaWoods.PEARL;
            case ABYSS -> SeaWoods.ABYSS;
        };
        int logs = 0;
        int leaves = 0;
        int dry = 0;
        int far = 0;
        int top = 0;
        for (int x = 4 - REACH; x <= 4 + REACH; x++) {
            for (int z = 4 - REACH; z <= 4 + REACH; z++) {
                for (int y = 1; y <= DEPTH + 2; y++) {
                    BlockState state = helper.getBlockState(new BlockPos(x, y, z));
                    if (state.is(wood.log.get()) || state.is(wood.wood.get())) {
                        logs++;
                        top = Math.max(top, y);
                    } else if (state.is(wood.leaves.get())) {
                        leaves++;
                        top = Math.max(top, y);
                        if (!state.getValue(LeavesBlock.WATERLOGGED)) {
                            dry++;
                        }
                        if (state.getValue(LeavesBlock.DISTANCE) > 6) {
                            far++;
                        }
                    }
                }
            }
        }
        helper.assertTrue(logs >= 5, "Zu wenig Holz: " + logs);
        helper.assertTrue(leaves >= 8, "Zu wenig Laub: " + leaves);
        helper.assertTrue(dry == 0, "Laub ohne Wasser: " + dry);
        helper.assertTrue(far == 0, "Laub zu weit vom Holz (es welkt): " + far);
        helper.assertTrue(top <= DEPTH - 3, "Der Baum wächst bis an die Oberfläche: " + top);
        helper.assertTrue(helper.getBlockState(new BlockPos(4, 1, 4)).is(wood.log.get()), "Der Stamm steht nicht am Grund");
        helper.succeed();
    }

    /** Ein Setzling unter Wasser: geflutet, hält sich auf Sand, wächst ohne Licht zum Baum. */
    private static void seaSapling(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        pool(helper);
        BlockPos pos = new BlockPos(4, 1, 4);
        BlockState sapling = SeaWoods.CORAL.sapling.get().defaultBlockState().setValue(SeaSaplingBlock.WATERLOGGED, true);
        helper.setBlock(pos, sapling);
        BlockState state = helper.getBlockState(pos);
        helper.assertTrue(state.is(SeaWoods.CORAL.sapling.get()) && state.getFluidState().is(net.minecraft.tags.FluidTags.WATER),
                "Der Setzling steht nicht geflutet im Wasser");
        helper.assertTrue(state.canSurvive(level, helper.absolutePos(pos)), "Der Setzling hält sich nicht auf Sand");
        helper.assertFalse(state.canSurvive(level, helper.absolutePos(new BlockPos(4, 6, 4))), "Der Setzling schwebt im Wasser");
        // zwei Stufen (wie Knochenmehl): beim zweiten Mal wird er zum Baum
        BlockPos abs = helper.absolutePos(pos);
        for (int i = 0; i < 2 && helper.getBlockState(pos).is(SeaWoods.CORAL.sapling.get()); i++) {
            ((SeaSaplingBlock) SeaWoods.CORAL.sapling.get()).advanceTree(level, abs, level.getBlockState(abs), RandomSource.create(4_200L + i));
        }
        helper.assertTrue(helper.getBlockState(pos).is(SeaWoods.CORAL.log.get()), "Der Setzling wächst nicht zum Korallenbaum: "
                + helper.getBlockState(pos));
        helper.succeed();
    }

    /**
     * Die vier Landschaften unter Wasser: angemeldet, im Urmeer möglich, in widerspruchsfreier Reihenfolge der Features (sonst
     * verweigert das Spiel die Welt), mit ihren Bäumen; der Krakenschlund bleibt im offenen Meer; beim Angeln die Tiefe.
     */
    private static void biomes(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        var registries = level.getServer().registryAccess();
        var biomes = registries.lookupOrThrow(Registries.BIOME);
        List<Holder<Biome>> urmeer = new ArrayList<>();
        for (ResourceKey<Biome> key : UrmeerBiomes.ALL) {
            var biome = biomes.get(key);
            helper.assertTrue(biome.isPresent(), "Biom fehlt: " + key.identifier());
            urmeer.add(biome.get());
        }
        // die Features aller Biome des Urmeers müssen sich in eine Reihenfolge bringen lassen
        try {
            net.minecraft.world.level.biome.FeatureSorter.buildFeaturesPerStep(urmeer, holder -> holder.value().getGenerationSettings().features(), true);
        } catch (RuntimeException e) {
            helper.fail("Die Features der Biome des Urmeers widersprechen sich: " + e.getMessage());
            return;
        }
        String[] woods = {"kelpwood", "coralwood", "pearlwood", "abysswood"};
        for (int i = 0; i < UrmeerBiomes.UNDERWATER.size(); i++) {
            ResourceKey<net.minecraft.world.level.levelgen.placement.PlacedFeature> tree = ResourceKey.create(Registries.PLACED_FEATURE,
                    KingdomsAtWar.id("overworld/urmeer/" + woods[i] + "_tree"));
            Biome biome = biomes.getOrThrow(UrmeerBiomes.UNDERWATER.get(i)).value();
            helper.assertTrue(biome.getGenerationSettings().features().stream().anyMatch(set -> set.stream().anyMatch(f -> f.is(tree))),
                    "Keine Bäume in " + UrmeerBiomes.UNDERWATER.get(i).identifier());
        }
        for (var key : List.of(UrmeerBiomes.DEEP, UrmeerBiomes.KELP_FOREST, UrmeerBiomes.PEARL_GROUNDS)) {
            helper.assertTrue(biomes.getOrThrow(key).is(ModTags.Biomes.HAS_KRAKEN_MAW), "Kein Krakenschlund in " + key.identifier());
        }
        helper.assertFalse(biomes.getOrThrow(UrmeerBiomes.GLOW_DEPTHS).is(ModTags.Biomes.HAS_KRAKEN_MAW), "Ein Krakenschlund im Graben");
        helper.succeed();
    }
}
