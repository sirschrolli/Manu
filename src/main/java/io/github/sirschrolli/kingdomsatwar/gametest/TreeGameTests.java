package io.github.sirschrolli.kingdomsatwar.gametest;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.loot.ModLootTables;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.features.TreeFeatures;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * Tests der neuen Baumformen und der kargen Truhen: Unter den Vanilla-Namen stehen die eigenen Bäume (mit Ästen, Wolken,
 * Etagen, Schirmen oder hängenden Kronen), ihr Laub verdorrt nicht (überall höchstens sechs Schritte vom Holz), auch aus
 * Setzlingen wachsen sie so; Truhen geben spürbar weniger her, aber nie nichts, und was man braucht (das Herz des
 * Meeres), liegt immer darin.
 */
final class TreeGameTests {
    private static final Identifier TOWN = KingdomsAtWar.id("test_town");

    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> SHAPES =
            ModGameTests.TEST_FUNCTIONS.register("trees_new_shapes", () -> TreeGameTests::shapes);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> SAPLINGS =
            ModGameTests.TEST_FUNCTIONS.register("trees_from_saplings", () -> TreeGameTests::saplings);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> LEAN_CHESTS =
            ModGameTests.TEST_FUNCTIONS.register("loot_lean_chests", () -> TreeGameTests::leanChests);

    private TreeGameTests() {}

    static void init() {}

    static void register(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment) {
        for (var test : List.of(SHAPES, SAPLINGS, LEAN_CHESTS)) {
            event.registerTest(test.getId(), new FunctionGameTestInstance(test.key(),
                    new TestData<>(environment, Level.OVERWORLD, TOWN, 100, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
        }
    }

    private static void ground(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        for (BlockPos pos : BlockPos.betweenClosed(helper.absolutePos(new BlockPos(0, 1, 0)), helper.absolutePos(new BlockPos(47, 29, 47)))) {
            level.setBlock(pos, pos.getY() == helper.absolutePos(new BlockPos(0, 1, 0)).getY() ? Blocks.GRASS_BLOCK.defaultBlockState()
                    : Blocks.AIR.defaultBlockState(), 2 | 16);
        }
    }

    /** Holz und Laub eines Baums in einem Kasten; das Laub muss überall höchstens sechs Schritte vom Holz sein. */
    private record Count(int logs, int leaves, int far, int width) {}

    private static Count count(GameTestHelper helper, BlockPos a, BlockPos b) {
        int logs = 0;
        int leaves = 0;
        int far = 0;
        int minX = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        for (BlockPos pos : BlockPos.betweenClosed(a, b)) {
            BlockState state = helper.getLevel().getBlockState(pos);
            if (state.is(BlockTags.LOGS)) {
                logs++;
            } else if (state.is(BlockTags.LEAVES)) {
                leaves++;
                if (state.hasProperty(BlockStateProperties.DISTANCE) && state.getValue(BlockStateProperties.DISTANCE) > 6) {
                    far++;
                }
                minX = Math.min(minX, pos.getX());
                maxX = Math.max(maxX, pos.getX());
            }
        }
        return new Count(logs, leaves, far, leaves == 0 ? 0 : maxX - minX + 1);
    }

    /** Die neuen Formen, je eine auf eigenem Platz: Holz, Laub, kein verdorrendes Blatt, und die Kronen sind breit. */
    private static void shapes(GameTestHelper helper) {
        ground(helper);
        ServerLevel level = helper.getLevel();
        var features = level.registryAccess().lookupOrThrow(Registries.FEATURE);
        List<ResourceKey<Feature>> keys = List.of(TreeFeatures.OAK, TreeFeatures.FANCY_OAK, TreeFeatures.BIRCH, TreeFeatures.SPRUCE,
                TreeFeatures.PINE, TreeFeatures.ACACIA, TreeFeatures.DARK_OAK, TreeFeatures.CHERRY, TreeFeatures.SWAMP_OAK);
        List<String> problems = new ArrayList<>();
        for (int i = 0; i < keys.size(); i++) {
            ResourceKey<Feature> key = keys.get(i);
            Feature feature = features.getOrThrow(key).value();
            if (!(feature instanceof TreeFeature tree) || !(tree.trunkPlacer() instanceof io.github.sirschrolli.kingdomsatwar.worldgen.tree.BranchedTrunkPlacer)) {
                problems.add(key.identifier().getPath() + " hat keine neue Gestalt");
                continue;
            }
            BlockPos spot = helper.absolutePos(new BlockPos(8 + (i % 3) * 15, 2, 8 + (i / 3) * 15));
            boolean placed = false;
            for (int attempt = 0; attempt < 4 && !placed; attempt++) {
                placed = feature.place(level, level.getChunkSource().getGenerator(), RandomSource.create(31L * i + attempt), spot);
            }
            if (!placed) {
                problems.add(key.identifier().getPath() + " wächst nicht");
                continue;
            }
            Count c = count(helper, spot.offset(-7, -1, -7), spot.offset(7, 26, 7));
            if (c.logs() < 4 || c.leaves() < 15 || c.far() > 0) {
                problems.add(key.identifier().getPath() + ": " + c.logs() + " Holz, " + c.leaves() + " Laub, " + c.far() + " verdorrend");
            }
            if (key == TreeFeatures.OAK && c.width() < 6) {
                problems.add("die Eiche ist schmal: " + c.width());
            }
        }
        helper.assertTrue(problems.isEmpty(), String.join("; ", problems));
        helper.succeed();
    }

    /** Aus Setzlingen wachsen die neuen Formen: Eiche, Birke, Fichte, Akazie, Kirsche (dazu die Schwarzeiche aus vier Setzlingen). */
    private static void saplings(GameTestHelper helper) {
        ground(helper);
        ServerLevel level = helper.getLevel();
        record Grow(TreeGrower grower, BlockState sapling, boolean mega) {}
        List<Grow> grows = List.of(new Grow(TreeGrower.OAK, Blocks.OAK_SAPLING.defaultBlockState(), false),
                new Grow(TreeGrower.BIRCH, Blocks.BIRCH_SAPLING.defaultBlockState(), false),
                new Grow(TreeGrower.SPRUCE, Blocks.SPRUCE_SAPLING.defaultBlockState(), false),
                new Grow(TreeGrower.ACACIA, Blocks.ACACIA_SAPLING.defaultBlockState(), false),
                new Grow(TreeGrower.CHERRY, Blocks.CHERRY_SAPLING.defaultBlockState(), false),
                new Grow(TreeGrower.DARK_OAK, Blocks.DARK_OAK_SAPLING.defaultBlockState(), true));
        List<String> problems = new ArrayList<>();
        for (int i = 0; i < grows.size(); i++) {
            Grow g = grows.get(i);
            BlockPos spot = helper.absolutePos(new BlockPos(8 + (i % 3) * 15, 2, 10 + (i / 3) * 18));
            int w = g.mega() ? 2 : 1;
            for (int dx = 0; dx < w; dx++) {
                for (int dz = 0; dz < w; dz++) {
                    level.setBlock(spot.offset(dx, 0, dz), g.sapling(), 2);
                }
            }
            boolean grown = false;
            for (int attempt = 0; attempt < 6 && !grown; attempt++) {
                grown = g.grower().growTree(level, level.getChunkSource().getGenerator(), spot, g.sapling(), RandomSource.create(77L + attempt));
            }
            Count c = count(helper, spot.offset(-7, -1, -7), spot.offset(8, 24, 8));
            if (!grown || c.logs() < 4 || c.leaves() < 12 || c.far() > 0) {
                problems.add(BuiltInName.of(g.sapling()) + ": " + (grown ? "" : "wächst nicht, ") + c.logs() + " Holz, " + c.leaves() + " Laub, "
                        + c.far() + " verdorrend");
            }
        }
        helper.assertTrue(problems.isEmpty(), String.join("; ", problems));
        helper.succeed();
    }

    private static final class BuiltInName {
        static String of(BlockState state) {
            return net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath();
        }
    }

    /**
     * Karge Truhen: Eine Truhe im Dorfhaus gibt im Mittel deutlich weniger her (aber nie nichts), und im Gezeitenschrein
     * liegt immer das Herz des Meeres.
     */
    private static void leanChests(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        LootParams params = new LootParams.Builder(level).withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(helper.absolutePos(BlockPos.ZERO)))
                .create(LootContextParamSets.CHEST);
        LootTable house = level.getServer().reloadableRegistries().getLootTable(ModLootTables.VILLAGE_HOUSE);
        int rolls = 300;
        int stacks = 0;
        int items = 0;
        int empty = 0;
        for (int k = 0; k < rolls; k++) {
            List<ItemStack> loot = house.getRandomItems(params);
            if (loot.isEmpty()) {
                empty++;
            }
            stacks += loot.size();
            for (ItemStack s : loot) {
                items += s.getCount();
            }
        }
        double perChest = stacks / (double) rolls;
        double itemsPerChest = items / (double) rolls;
        helper.assertTrue(empty == 0, "leere Truhen: " + empty);
        helper.assertTrue(perChest <= 3.2, "zu viel in einer Truhe: " + perChest + " Stapel im Mittel");
        helper.assertTrue(itemsPerChest <= 6.5, "zu viel in einer Truhe: " + itemsPerChest + " Dinge im Mittel");
        LootTable shrine = level.getServer().reloadableRegistries().getLootTable(ModLootTables.SITE_TIDE_SHRINE);
        for (int k = 0; k < 40; k++) {
            helper.assertTrue(shrine.getRandomItems(params).stream().anyMatch(s -> s.is(Items.HEART_OF_THE_SEA)),
                    "das Herz des Meeres fehlt im Gezeitenschrein");
        }
        helper.succeed();
    }
}
