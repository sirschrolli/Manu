package io.github.sirschrolli.kingdomsatwar.gametest;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.entity.shadow.ShadeStalkerEntity;
import io.github.sirschrolli.kingdomsatwar.entity.umbrer.UmbrerEntity;
import io.github.sirschrolli.kingdomsatwar.entity.umbrer.UmbrerGuardEntity;
import io.github.sirschrolli.kingdomsatwar.entity.umbrer.UmbrerProfession;
import io.github.sirschrolli.kingdomsatwar.loot.ModLootTables;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.ModTags;
import io.github.sirschrolli.kingdomsatwar.registry.ShadowBlocks;
import io.github.sirschrolli.kingdomsatwar.worldgen.lair.ModStructures;
import io.github.sirschrolli.kingdomsatwar.worldgen.umbrer.UmbrerVillagePiece;
import io.github.sirschrolli.kingdomsatwar.worldgen.umbrer.UmbrerVillagePiece.Kind;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * Tests der Umbrer: jedes Gebäude ihrer Dörfer lässt sich bauen (mit Tür, Truhe, Bewohnern), sie handeln in jedem Beruf,
 * und ihre Wächter schützen sie vor Schattenwesen und Angreifern.
 */
final class UmbrerGameTests {
    private static final Identifier ARENA = KingdomsAtWar.id("test_arena");
    /** 14 × 32 × 14: Platz für jedes einzelne Gebäude, auch den Seherturm. */
    private static final Identifier VILLAGE = KingdomsAtWar.id("test_village");

    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> UMBRER_BUILDINGS =
            ModGameTests.TEST_FUNCTIONS.register("umbrer_buildings", () -> UmbrerGameTests::buildings);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> UMBRER_TRADING =
            ModGameTests.TEST_FUNCTIONS.register("umbrer_trading", () -> UmbrerGameTests::trading);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> UMBRER_GUARDS =
            ModGameTests.TEST_FUNCTIONS.register("umbrer_guards", () -> UmbrerGameTests::guards);

    private UmbrerGameTests() {}

    /** Lädt die Klasse (meldet die Testfunktionen an). */
    static void init() {}

    static void register(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment) {
        event.registerTest(UMBRER_BUILDINGS.getId(), new FunctionGameTestInstance(UMBRER_BUILDINGS.key(),
                new TestData<>(environment, Level.OVERWORLD, VILLAGE, 200, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
        for (var test : List.of(UMBRER_TRADING, UMBRER_GUARDS)) {
            event.registerTest(test.getId(), new FunctionGameTestInstance(test.key(),
                    new TestData<>(environment, Level.OVERWORLD, ARENA, 200, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
        }
    }

    // =====================================================================================
    // Gebäude
    // =====================================================================================

    /**
     * Baut jedes Gebäude einzeln (ungedreht) auf der Testfläche und prüft Tür, Truhe mit der richtigen Beute, Bewohner und
     * ein paar kennzeichnende Blöcke; dazu ein gedrehtes Wohnhaus (Tür und Blickrichtung müssen mitgedreht sein).
     * Außerdem: Bauwerk, Strukturgruppe und Biom-Liste des Dorfs sind registriert.
     */
    private static void buildings(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        var structures = level.registryAccess().lookupOrThrow(Registries.STRUCTURE);
        helper.assertTrue(structures.get(ModStructures.UMBRER_VILLAGE_STRUCTURE).isPresent(), "Das Dorf der Umbrer ist nicht registriert");
        helper.assertTrue(level.registryAccess().lookupOrThrow(Registries.STRUCTURE_SET).get(ModStructures.UMBRER_VILLAGES).isPresent(),
                "Die Strukturgruppe der Dörfer fehlt");
        helper.assertTrue(level.registryAccess().lookupOrThrow(Registries.BIOME).get(ModTags.Biomes.HAS_UMBRER_VILLAGE)
                .map(set -> set.size() >= 3).orElse(false), "Zu wenige Biome für Dörfer der Umbrer");

        BlockPos origin = new BlockPos(1, 2, 2);
        build(helper, Kind.HOUSE, origin, Rotation.NONE);
        assertDoor(helper, origin.offset(3, 1, 0), Direction.SOUTH);
        assertLoot(helper, origin.offset(1, 1, 5), origin.offset(5, 1, 5), ModLootTables.UMBRER_HOUSE);
        assertResidents(helper, 1, 0, null);
        assertBlock(helper, origin.offset(3, 7, 3), ShadowBlocks.POLISHED_SHADOW_STONE.base().get(), "First des Wohnhauses");

        build(helper, Kind.LONGHOUSE, origin, Rotation.NONE);
        assertDoor(helper, origin.offset(5, 1, 0), Direction.SOUTH);
        assertLoot(helper, origin.offset(9, 1, 5), null, ModLootTables.UMBRER_HOUSE);
        assertResidents(helper, 2, 0, UmbrerProfession.FARMER);

        build(helper, Kind.SMITHY, origin, Rotation.NONE);
        assertLoot(helper, origin.offset(7, 1, 7), null, ModLootTables.UMBRER_SMITHY);
        assertResidents(helper, 1, 0, UmbrerProfession.MOONSMITH);
        assertBlock(helper, origin.offset(1, 2, 7), Blocks.SOUL_FIRE, "Seelenfeuer der Esse");
        assertBlock(helper, origin.offset(1, 11, 7), Blocks.SOUL_CAMPFIRE, "Feuer auf dem Schornstein");

        build(helper, Kind.HERB_HUT, origin, Rotation.NONE);
        assertDoor(helper, origin.offset(3, 1, 0), Direction.SOUTH);
        assertLoot(helper, origin.offset(5, 1, 5), null, ModLootTables.UMBRER_HERBALIST);
        assertResidents(helper, 1, 0, UmbrerProfession.HERBALIST);
        assertBlock(helper, origin.offset(1, 1, 2), Blocks.BREWING_STAND, "Braustand");
        BlockState fence = level.getBlockState(helper.absolutePos(origin.offset(0, 1, 8)));
        helper.assertTrue(fence.is(ShadowBlocks.SHADOW_FENCE.get()) && fence.getValue(FenceBlock.NORTH) && fence.getValue(FenceBlock.SOUTH),
                "Der Gartenzaun ist nicht verbunden: " + fence);

        build(helper, Kind.CRYSTAL_WORKSHOP, origin, Rotation.NONE);
        assertLoot(helper, origin.offset(5, 1, 7), null, ModLootTables.UMBRER_WORKSHOP);
        assertResidents(helper, 1, 0, UmbrerProfession.CRYSTAL_CUTTER);
        assertBlock(helper, origin.offset(3, 7, 4), ShadowBlocks.NIGHT_CRYSTAL.get(), "Kristallfirst");
        assertBlock(helper, origin.offset(3, 8, 4), ShadowBlocks.NIGHT_CRYSTAL_CLUSTER.get(), "Kristallkamm");

        build(helper, Kind.SEER_TOWER, origin, Rotation.NONE);
        assertDoor(helper, origin.offset(4, 1, 0), Direction.SOUTH);
        assertLoot(helper, origin.offset(7, 1, 2), null, ModLootTables.UMBRER_SEER);
        assertResidents(helper, 1, 0, UmbrerProfession.SEER);
        assertBlock(helper, origin.offset(4, 13, 4), Blocks.ENCHANTING_TABLE, "Zaubertisch");
        for (int y = 1; y <= 18; y++) {
            assertBlock(helper, origin.offset(4, y, 7), Blocks.LADDER, "Leiter im Seherturm (Höhe " + y + ")");
        }
        assertBlock(helper, origin.offset(4, 28, 4), ShadowBlocks.NIGHT_CRYSTAL_CLUSTER.get(), "Kristallspitze des Seherturms");

        build(helper, Kind.WATCHTOWER, origin, Rotation.NONE);
        assertResidents(helper, 0, 1, null);
        for (int y = 1; y <= 8; y++) {
            assertBlock(helper, origin.offset(2, y, 3), Blocks.LADDER, "Leiter im Wachturm (Höhe " + y + ")");
        }
        helper.assertTrue(level.getBlockState(helper.absolutePos(origin.offset(2, 1, 0))).isAir(), "Der Wachturm hat keinen offenen Durchgang");

        build(helper, Kind.FARM, origin, Rotation.NONE);
        assertBlock(helper, origin.offset(1, 0, 1), ShadowBlocks.SHADOW_FARMLAND.get(), "Ackerboden");
        assertBlock(helper, origin.offset(4, 0, 4), Blocks.WATER, "Wasserrinne");

        build(helper, Kind.CRYSTAL_GARDEN, origin, Rotation.NONE);
        assertBlock(helper, origin.offset(3, 3, 3), ShadowBlocks.NIGHT_CRYSTAL.get(), "Kristallsäule");
        assertBlock(helper, origin.offset(3, 4, 3), ShadowBlocks.NIGHT_CRYSTAL_CLUSTER.get(), "Druse auf der Säule");

        // gedreht (Vorderseite nach Osten): Tür und Truhe liegen an den gedrehten Stellen
        BlockPos turned = new BlockPos(10, 2, 2);
        build(helper, Kind.HOUSE, turned, Rotation.CLOCKWISE_90);
        assertDoor(helper, turned.offset(new BlockPos(3, 1, 0).rotate(Rotation.CLOCKWISE_90)), Direction.WEST);
        assertLoot(helper, turned.offset(new BlockPos(1, 1, 5).rotate(Rotation.CLOCKWISE_90)),
                turned.offset(new BlockPos(5, 1, 5).rotate(Rotation.CLOCKWISE_90)), ModLootTables.UMBRER_HOUSE);
        helper.succeed();
    }

    /** Räumt die Testfläche und baut ein Gebäude (ohne Dorf drumherum). */
    private static void build(GameTestHelper helper, Kind kind, BlockPos origin, Rotation rotation) {
        ServerLevel level = helper.getLevel();
        for (Entity entity : level.getEntitiesOfClass(Entity.class, area(helper), e -> !(e instanceof Player))) {
            entity.discard();
        }
        for (int x = 0; x < 14; x++) {
            for (int y = 1; y < 32; y++) {
                for (int z = 0; z < 14; z++) {
                    helper.setBlock(x, y, z, Blocks.AIR);
                }
            }
        }
        BlockPos absolute = helper.absolutePos(origin);
        UmbrerVillagePiece piece = UmbrerVillagePiece.single(kind, absolute, rotation, 1234L);
        piece.postProcess(level, level.structureManager(), level.getChunkSource().getGenerator(), level.getRandom(), piece.getBoundingBox(),
                ChunkPos.containing(absolute), absolute);
    }

    private static AABB area(GameTestHelper helper) {
        return new AABB(helper.absolutePos(BlockPos.ZERO)).expandTowards(14, 32, 14).inflate(1.0);
    }

    private static void assertBlock(GameTestHelper helper, BlockPos pos, Block block, String what) {
        BlockState state = helper.getLevel().getBlockState(helper.absolutePos(pos));
        helper.assertTrue(state.is(block), what + " fehlt bei " + pos + ": " + state);
    }

    private static void assertDoor(GameTestHelper helper, BlockPos pos, Direction facing) {
        BlockState lower = helper.getLevel().getBlockState(helper.absolutePos(pos));
        BlockState upper = helper.getLevel().getBlockState(helper.absolutePos(pos.above()));
        helper.assertTrue(lower.is(ShadowBlocks.SHADOW_DOOR.get()) && lower.getValue(DoorBlock.HALF) == DoubleBlockHalf.LOWER
                && upper.is(ShadowBlocks.SHADOW_DOOR.get()) && upper.getValue(DoorBlock.HALF) == DoubleBlockHalf.UPPER,
                "Keine Tür bei " + pos + ": " + lower + " / " + upper);
        helper.assertTrue(lower.getValue(DoorBlock.FACING) == facing, "Die Tür bei " + pos + " zeigt nach " + lower.getValue(DoorBlock.FACING));
    }

    /** Eine Truhe mit der Beute steht an einer der beiden Stellen. */
    private static void assertLoot(GameTestHelper helper, BlockPos pos, BlockPos alternative, ResourceKey<LootTable> table) {
        for (BlockPos candidate : alternative == null ? List.of(pos) : List.of(pos, alternative)) {
            if (helper.getLevel().getBlockEntity(helper.absolutePos(candidate)) instanceof RandomizableContainerBlockEntity container
                    && table.equals(container.getLootTable())) {
                return;
            }
        }
        helper.fail("Keine Truhe mit " + table.identifier() + " bei " + pos + (alternative != null ? " oder " + alternative : ""));
    }

    /** Genau so viele Umbrer und Wächter wohnen im Gebäude (und einer übt den erwarteten Beruf aus). */
    private static void assertResidents(GameTestHelper helper, int umbrer, int guards, UmbrerProfession expected) {
        List<UmbrerEntity> residents = helper.getLevel().getEntitiesOfClass(UmbrerEntity.class, area(helper));
        List<UmbrerGuardEntity> watch = helper.getLevel().getEntitiesOfClass(UmbrerGuardEntity.class, area(helper));
        helper.assertTrue(residents.size() == umbrer, "Erwartet " + umbrer + " Umbrer, gefunden " + residents.size());
        helper.assertTrue(watch.size() == guards, "Erwartet " + guards + " Wächter, gefunden " + watch.size());
        if (expected != null) {
            helper.assertTrue(residents.stream().anyMatch(u -> u.profession() == expected), "Kein " + expected + " im Gebäude");
        }
        for (UmbrerEntity resident : residents) {
            helper.assertTrue(resident.isPersistenceRequired(), "Ein Bewohner würde verschwinden");
            helper.assertTrue(resident.hasHome(), "Ein Bewohner ist an kein Dorf gebunden");
        }
    }

    // =====================================================================================
    // Handel
    // =====================================================================================

    /** Jeder Beruf hat brauchbare Angebote; der Seher verkauft die Chronik der Umbrer. Umbrer können Türen öffnen. */
    private static void trading(GameTestHelper helper) {
        for (UmbrerProfession profession : UmbrerProfession.TRADERS) {
            UmbrerEntity umbrer = helper.spawnWithNoFreeWill(ModEntities.UMBRER.get(), new BlockPos(4, 1, 4));
            umbrer.setProfession(profession);
            var offers = umbrer.getOffers();
            helper.assertTrue(offers.size() >= 4, profession + " hat nur " + offers.size() + " Angebote");
            for (MerchantOffer offer : offers) {
                helper.assertFalse(offer.getResult().isEmpty(), profession + " verkauft nichts für " + offer.getCostA());
                helper.assertFalse(offer.getCostA().isEmpty(), profession + " verschenkt " + offer.getResult());
            }
            if (profession == UmbrerProfession.SEER) {
                helper.assertTrue(offers.stream().anyMatch(o -> o.getResult().has(DataComponents.WRITTEN_BOOK_CONTENT)),
                        "Der Seher verkauft keine Chronik");
            }
            helper.assertTrue(umbrer.getNavigation().getNodeEvaluator().canOpenDoors(), "Umbrer können keine Türen öffnen");
            umbrer.discard();
        }
        helper.succeed();
    }

    // =====================================================================================
    // Wächter
    // =====================================================================================

    /** Ein Wächter greift ein Schattenwesen in der Nähe an – und jeden, der einen Umbrer schlägt. */
    private static void guards(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        for (int x = 0; x < 9; x++) {
            for (int z = 0; z < 9; z++) {
                helper.setBlock(x, 0, z, ShadowBlocks.SHADOW_STONE.base().get());
            }
        }
        UmbrerGuardEntity guard = helper.spawn(ModEntities.UMBRER_GUARD.get(), new BlockPos(1, 1, 1));
        UmbrerEntity umbrer = helper.spawnWithNoFreeWill(ModEntities.UMBRER.get(), new BlockPos(2, 1, 3));
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.snapTo(umbrer.getX() + 1.0, umbrer.getY(), umbrer.getZ(), 90.0f, 0.0f);
        umbrer.hurtServer(level, level.damageSources().playerAttack(player), 1.0f);
        helper.assertTrue(guard.getTarget() == player, "Der Wächter stellt sich dem Angreifer nicht entgegen: " + guard.getTarget());
        guard.setTarget(null);
        ShadeStalkerEntity stalker = helper.spawnWithNoFreeWill(ModEntities.SHADE_STALKER.get(), new BlockPos(6, 1, 6));
        helper.succeedWhen(() -> helper.assertTrue(guard.getTarget() == stalker,
                "Der Wächter beachtet den Schattenpirscher nicht: " + guard.getTarget()));
    }
}
