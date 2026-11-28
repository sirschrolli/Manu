package io.github.sirschrolli.kingdomsatwar.gametest;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.registry.Cuisine;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.worldgen.site.SiteKind;
import io.github.sirschrolli.kingdomsatwar.worldgen.site.SitePiece;
import io.github.sirschrolli.kingdomsatwar.worldgen.site.WonderGround;
import io.github.sirschrolli.kingdomsatwar.worldgen.site.Wonders;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * Tests der Bauwerke von 2.0: Jedes Musterstück wird auf seinem Grund gebaut (weit draußen, jedes an seinem Platz) und muss
 * seine Truhen haben – mit Beute, die es gibt – und seine Kennzeichen: die Käselaibe der Sennhütte, das Boot und die
 * Schmuggler in der Höhle, die Bienenstöcke der Imkerei, den Spawner im Hünengrab, Braustände und Bücher im Alchemistenturm,
 * die Räucheröfen der Jagdhütte …
 */
final class WonderGameTests {
    private static final Identifier ARENA = KingdomsAtWar.id("test_arena");
    private static final List<DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>>> TESTS = new ArrayList<>();

    /** Ein Kennzeichen: so viele Blöcke dieser Art mindestens. */
    private record Mark(Supplier<Block> block, int min) {}

    /** Bewohner: so viele Wesen dieser Art mindestens. */
    private record Dweller(Supplier<EntityType<?>> type, int min) {}

    private static final Map<SiteKind, List<Mark>> MARKS = new EnumMap<>(SiteKind.class);
    private static final Map<SiteKind, List<Dweller>> DWELLERS = new EnumMap<>(SiteKind.class);

    static {
        MARKS.put(SiteKind.SENN_HUT, List.of(new Mark(() -> Cuisine.CHEESE_WHEEL.get(), 10), new Mark(() -> Blocks.BELL, 1),
                new Mark(() -> Blocks.WATER_CAULDRON, 1)));
        DWELLERS.put(SiteKind.SENN_HUT, List.of(new Dweller(() -> ModEntities.CATTLE.get(), 2), new Dweller(() -> ModEntities.FARM_GOAT.get(), 1)));
        MARKS.put(SiteKind.SMUGGLER_COVE, List.of(new Mark(() -> Blocks.WATER, 40), new Mark(() -> Blocks.CARTOGRAPHY_TABLE, 1),
                new Mark(() -> Blocks.STONE_BRICK_STAIRS, 8)));
        DWELLERS.put(SiteKind.SMUGGLER_COVE, List.of(new Dweller(() -> ModEntities.BANDIT.get(), 2),
                new Dweller(() -> ModEntities.BANDIT_ARCHER.get(), 1), new Dweller(() -> net.minecraft.world.entity.EntityTypes.OAK_BOAT, 1)));
        MARKS.put(SiteKind.APIARY, List.of(new Mark(() -> Blocks.BEEHIVE, 6), new Mark(() -> Blocks.HONEYCOMB_BLOCK, 2),
                new Mark(() -> Blocks.HAY_BLOCK, 8)));
        DWELLERS.put(SiteKind.APIARY, List.of(new Dweller(() -> ModEntities.BEE.get(), 3)));
        MARKS.put(SiteKind.BARROW, List.of(new Mark(() -> Blocks.SPAWNER, 1), new Mark(() -> Blocks.DECORATED_POT, 3),
                new Mark(() -> Blocks.SKELETON_SKULL, 2), new Mark(() -> Blocks.TUFF, 10)));
        MARKS.put(SiteKind.ALCHEMIST_TOWER, List.of(new Mark(() -> Blocks.BREWING_STAND, 2), new Mark(() -> Blocks.BOOKSHELF, 6),
                new Mark(() -> Blocks.LAVA_CAULDRON, 1), new Mark(() -> Blocks.NETHER_WART, 2)));
        DWELLERS.put(SiteKind.ALCHEMIST_TOWER, List.of(new Dweller(() -> ModEntities.WITCH.get(), 1)));
        MARKS.put(SiteKind.HUNTING_LODGE, List.of(new Mark(() -> Blocks.SMOKER, 2), new Mark(() -> Blocks.FLETCHING_TABLE, 1),
                new Mark(() -> Blocks.LADDER, 4), new Mark(() -> Blocks.STRAW_BED, 2)));
        // Unterwelt
        MARKS.put(SiteKind.EMBER_DUNGEON, List.of(new Mark(() -> Blocks.IRON_BARS, 20), new Mark(() -> Blocks.SOUL_CAMPFIRE, 4),
                new Mark(() -> Blocks.SPAWNER, 1), new Mark(() -> Blocks.LAVA_CAULDRON, 2)));
        DWELLERS.put(SiteKind.EMBER_DUNGEON, List.of(new Dweller(() -> ModEntities.WITHER_SKELETON.get(), 2)));
        MARKS.put(SiteKind.STRIDER_RANCH, List.of(new Mark(() -> Cuisine.EMBER_PEPPERS.get(), 6), new Mark(() -> Blocks.NETHER_BRICK_FENCE, 20),
                new Mark(() -> Blocks.LAVA_CAULDRON, 1)));
        DWELLERS.put(SiteKind.STRIDER_RANCH, List.of(new Dweller(() -> ModEntities.STRIDER.get(), 3), new Dweller(() -> ModEntities.PIGLIN.get(), 1)));
        MARKS.put(SiteKind.WART_BREWERY, List.of(new Mark(() -> Blocks.NETHER_WART, 20), new Mark(() -> Blocks.BREWING_STAND, 2),
                new Mark(() -> Blocks.WATER_CAULDRON, 2)));
        DWELLERS.put(SiteKind.WART_BREWERY, List.of(new Dweller(() -> ModEntities.PIGLIN.get(), 2)));
        MARKS.put(SiteKind.SOUL_CHAPEL, List.of(new Mark(() -> Blocks.BELL, 1), new Mark(() -> Blocks.SOUL_CAMPFIRE, 2),
                new Mark(() -> Blocks.CRIMSON_STAIRS, 12)));
        DWELLERS.put(SiteKind.SOUL_CHAPEL, List.of(new Dweller(() -> ModEntities.SKELETON.get(), 2)));
        MARKS.put(SiteKind.GOLD_MINE, List.of(new Mark(() -> Blocks.NETHER_GOLD_ORE, 10), new Mark(() -> Blocks.BLAST_FURNACE, 2),
                new Mark(() -> Blocks.RAIL, 5), new Mark(() -> Blocks.GOLD_BLOCK, 2)));
        DWELLERS.put(SiteKind.GOLD_MINE, List.of(new Dweller(() -> ModEntities.PIGLIN.get(), 3), new Dweller(() -> ModEntities.PIGLIN_BRUTE.get(), 1),
                new Dweller(() -> net.minecraft.world.entity.EntityTypes.CHEST_MINECART, 1)));
        MARKS.put(SiteKind.BROKEN_BRIDGE, List.of(new Mark(() -> Blocks.NETHER_BRICK_FENCE, 20), new Mark(() -> Blocks.RED_NETHER_BRICKS, 30),
                new Mark(() -> Blocks.SOUL_LANTERN, 8)));
        DWELLERS.put(SiteKind.BROKEN_BRIDGE, List.of(new Dweller(() -> ModEntities.BLAZE.get(), 2), new Dweller(() -> ModEntities.WITHER_SKELETON.get(), 1)));
        for (Wonders.Wonder wonder : Wonders.all()) {
            TESTS.add(ModGameTests.TEST_FUNCTIONS.register("wonder_" + wonder.kind().id(), () -> helper -> test(helper, wonder)));
        }
    }

    private WonderGameTests() {}

    static void init() {}

    static void register(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment) {
        for (var test : TESTS) {
            event.registerTest(test.getId(), new FunctionGameTestInstance(test.key(),
                    new TestData<>(environment, Level.OVERWORLD, ARENA, 300, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
        }
    }

    private static void test(GameTestHelper helper, Wonders.Wonder wonder) {
        ServerLevel level = helper.getLevel();
        BlockPos origin = new BlockPos(48000 + wonder.kind().ordinal() * 160, 140, 48000);
        SitePiece piece = WonderGround.build(level, wonder, origin);
        BoundingBox box = piece.getBoundingBox();
        // Truhen und Fässer mit Beute, die es gibt
        int containers = 0;
        Map<Block, Integer> counts = new java.util.HashMap<>();
        for (BlockPos pos : BlockPos.betweenClosed(box.minX(), box.minY(), box.minZ(), box.maxX(), box.maxY(), box.maxZ())) {
            Block block = level.getBlockState(pos).getBlock();
            counts.merge(block, 1, Integer::sum);
            BlockEntity entity = level.getBlockEntity(pos);
            if (entity instanceof RandomizableContainer container && container.getLootTable() != null) {
                ResourceKey<LootTable> key = container.getLootTable();
                helper.assertTrue(level.getServer().reloadableRegistries().getLootTable(key) != LootTable.EMPTY,
                        wonder.kind().id() + ": Beute fehlt: " + key.identifier());
                containers++;
            }
        }
        helper.assertTrue(containers >= 1, wonder.kind().id() + ": keine Truhe mit Beute");
        helper.assertTrue(counts.getOrDefault(Blocks.AIR, 0) < box.getXSpan() * box.getYSpan() * box.getZSpan(), wonder.kind().id() + ": nichts gebaut");
        for (Mark mark : MARKS.getOrDefault(wonder.kind(), List.of())) {
            int n = counts.getOrDefault(mark.block().get(), 0);
            helper.assertTrue(n >= mark.min(), wonder.kind().id() + ": " + n + "× " + mark.block().get() + " (mindestens " + mark.min() + ")");
        }
        AABB area = AABB.of(box).inflate(2.0);
        for (Dweller dweller : DWELLERS.getOrDefault(wonder.kind(), List.of())) {
            int n = level.getEntities((Entity) null, area, e -> e.getType() == dweller.type().get()).size();
            helper.assertTrue(n >= dweller.min(), wonder.kind().id() + ": " + n + "× " + dweller.type().get().getDescription().getString()
                    + " (mindestens " + dweller.min() + ")");
        }
        // aufräumen: die Wesen fort (sie sollen keine anderen Tests stören)
        for (Entity e : level.getEntities((Entity) null, area, e -> !(e instanceof net.minecraft.world.entity.player.Player))) {
            e.discard();
        }
        helper.succeed();
    }
}
