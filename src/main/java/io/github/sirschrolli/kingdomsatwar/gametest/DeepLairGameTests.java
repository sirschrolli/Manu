package io.github.sirschrolli.kingdomsatwar.gametest;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.entity.miniboss.MiniBoss;
import io.github.sirschrolli.kingdomsatwar.loot.ModLootTables;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.worldgen.site.SitePiece;
import io.github.sirschrolli.kingdomsatwar.worldgen.site.SiteSamples;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * Tests der Hallen der Tiefe (seit 1.15): Jedes der vier Bauwerke steht vollständig da – mit seinem schlafenden Mini-Boss an
 * seinem Platz, seinen Spawnern, Truhen und seinen Kennzeichen (Thron und Wendeltreppe der Gruft, Eiersäcke und Netze der
 * Brutgrotte, Esse, Schatzkammertür und Stollen der Zwergenhalle, Feuerstelle, Lager und Hort der Trollhöhle).
 */
final class DeepLairGameTests {
    private static final Identifier TOWN = KingdomsAtWar.id("test_town");

    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> CRYPT =
            ModGameTests.TEST_FUNCTIONS.register("site_crypt", () -> DeepLairGameTests::crypt);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> BROOD_GROTTO =
            ModGameTests.TEST_FUNCTIONS.register("site_brood_grotto", () -> DeepLairGameTests::broodGrotto);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> DWARF_HALL =
            ModGameTests.TEST_FUNCTIONS.register("site_dwarf_hall", () -> DeepLairGameTests::dwarfHall);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> TROLL_DEN =
            ModGameTests.TEST_FUNCTIONS.register("site_troll_den", () -> DeepLairGameTests::trollDen);

    private DeepLairGameTests() {}

    static void init() {}

    static void register(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment) {
        for (var test : List.of(CRYPT, BROOD_GROTTO, DWARF_HALL, TROLL_DEN)) {
            event.registerTest(test.getId(), new FunctionGameTestInstance(test.key(),
                    new TestData<>(environment, Level.OVERWORLD, TOWN, 120, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
        }
    }

    // =====================================================================================
    // Hilfen
    // =====================================================================================

    /** Der ganze Testbereich voller Stein (in den Fels werden die Bauwerke gehauen); alles Lebendige weg. */
    private static void rock(GameTestHelper helper, int top) {
        ServerLevel level = helper.getLevel();
        for (BlockPos pos : BlockPos.betweenClosed(helper.absolutePos(new BlockPos(0, 1, 0)), helper.absolutePos(new BlockPos(47, 29, 47)))) {
            level.setBlock(pos, pos.getY() - helper.absolutePos(BlockPos.ZERO).getY() <= top ? Blocks.STONE.defaultBlockState()
                    : Blocks.AIR.defaultBlockState(), 2 | 16);
        }
        for (Entity e : level.getEntities((Entity) null, AABB.encapsulatingFullBlocks(helper.absolutePos(new BlockPos(-2, -2, -2)),
                helper.absolutePos(new BlockPos(50, 32, 50))), e -> !(e instanceof ServerPlayer))) {
            e.discard();
        }
    }

    private static BoundingBox area(GameTestHelper helper) {
        return BoundingBox.fromCorners(helper.absolutePos(new BlockPos(0, 1, 0)), helper.absolutePos(new BlockPos(47, 29, 47)));
    }

    private static void build(GameTestHelper helper, SitePiece piece) {
        ServerLevel level = helper.getLevel();
        piece.postProcess(level, level.structureManager(), level.getChunkSource().getGenerator(), RandomSource.create(5), area(helper),
                ChunkPos.containing(piece.origin()), piece.origin());
    }

    private static int count(GameTestHelper helper, Predicate<BlockState> test) {
        int n = 0;
        for (BlockPos pos : BlockPos.betweenClosed(helper.absolutePos(new BlockPos(0, 1, 0)), helper.absolutePos(new BlockPos(47, 29, 47)))) {
            if (test.test(helper.getLevel().getBlockState(pos))) {
                n++;
            }
        }
        return n;
    }

    /** Truhen (und Fässer) mit genau dieser Beute-Tabelle. */
    private static int chests(GameTestHelper helper, ResourceKey<LootTable> table) {
        int n = 0;
        for (BlockPos pos : BlockPos.betweenClosed(helper.absolutePos(new BlockPos(0, 1, 0)), helper.absolutePos(new BlockPos(47, 29, 47)))) {
            BlockEntity be = helper.getLevel().getBlockEntity(pos);
            if (be instanceof RandomizableContainer container && table.equals(container.getLootTable())) {
                n++;
            }
        }
        return n;
    }

    private static int spawners(GameTestHelper helper, EntityType<?> type) {
        int n = 0;
        for (BlockPos pos : BlockPos.betweenClosed(helper.absolutePos(new BlockPos(0, 1, 0)), helper.absolutePos(new BlockPos(47, 29, 47)))) {
            if (helper.getLevel().getBlockEntity(pos) instanceof SpawnerBlockEntity spawner
                    && spawner.getSpawner().getOrCreateDisplayEntity(helper.getLevel(), pos) != null
                    && spawner.getSpawner().getOrCreateDisplayEntity(helper.getLevel(), pos).getType() == type) {
                n++;
            }
        }
        return n;
    }

    /** Der Mini-Boss: genau einer, schlafend, nahe an seinem Platz (und dieser ist sein Heimplatz). */
    private static void boss(GameTestHelper helper, List<String> problems, EntityType<? extends Mob> type, BlockPos place) {
        List<? extends Mob> found = helper.getLevel().getEntities(type, AABB.encapsulatingFullBlocks(helper.absolutePos(BlockPos.ZERO),
                helper.absolutePos(new BlockPos(47, 29, 47))), Entity::isAlive);
        if (found.size() != 1) {
            problems.add(type.getDescription().getString() + ": " + found.size() + " statt einem");
            return;
        }
        Mob boss = found.getFirst();
        if (!(boss instanceof MiniBoss mini) || mini.isAwake()) {
            problems.add(type.getDescription().getString() + " schläft nicht");
        } else if (mini.miniBoss().home() == null || mini.miniBoss().home().distSqr(place) > 2) {
            problems.add(type.getDescription().getString() + ": Heimplatz " + mini.miniBoss().home() + " statt " + place);
        }
        if (boss.position().distanceToSqr(place.getX() + 0.5, place.getY(), place.getZ() + 0.5) > 4.0) {
            problems.add(type.getDescription().getString() + " steht bei " + boss.blockPosition() + " statt " + place);
        }
    }

    // =====================================================================================

    /**
     * Die Gruft: der Gruftkönig auf seinem Thron, zwei Spawner der Gerippe in den Beinhäusern, zwei Truhen dort und zwei im Hort,
     * die Wendeltreppe hinauf zum Mausoleum (die letzte Stufe bündig mit seinem Boden), Seelenlaternen und Sarkophage.
     */
    private static void crypt(GameTestHelper helper) {
        rock(helper, 18);
        BlockPos o = helper.absolutePos(new BlockPos(24, 3, 20));
        int surface = 14;
        build(helper, SiteSamples.crypt(o, surface));
        List<String> problems = new ArrayList<>();
        boss(helper, problems, ModEntities.CRYPT_KING.get(), SiteSamples.cryptThrone(o).above());
        if (spawners(helper, ModEntities.SKELETON.get()) != 2) {
            problems.add("Spawner der Gerippe: " + spawners(helper, ModEntities.SKELETON.get()));
        }
        if (chests(helper, ModLootTables.SITE_CRYPT) != 2 || chests(helper, ModLootTables.SITE_CRYPT_HOARD) != 2) {
            problems.add("Truhen: Beinhäuser " + chests(helper, ModLootTables.SITE_CRYPT) + ", Hort " + chests(helper, ModLootTables.SITE_CRYPT_HOARD));
        }
        BlockPos seat = SiteSamples.cryptThrone(o);
        if (!helper.getLevel().getBlockState(seat).is(Blocks.POLISHED_BLACKSTONE_SLAB)) {
            problems.add("kein Thron: " + helper.getLevel().getBlockState(seat));
        }
        int steps = count(helper, st -> st.is(Blocks.STONE_BRICK_STAIRS));
        if (steps < surface) {
            problems.add("Wendeltreppe: " + steps + " Stufen");
        }
        BlockPos shaft = SiteSamples.cryptShaft(o);
        if (!helper.getLevel().getBlockState(shaft.above(surface + 1)).is(Blocks.SOUL_LANTERN)) {
            problems.add("keine Laterne auf dem Mittelpfeiler oben");
        }
        if (count(helper, st -> st.is(Blocks.SOUL_LANTERN)) < 9) {
            problems.add("Seelenlaternen: " + count(helper, st -> st.is(Blocks.SOUL_LANTERN)));
        }
        if (count(helper, st -> st.is(Blocks.SKELETON_SKULL) || st.is(Blocks.SKELETON_WALL_SKULL)) < 10) {
            problems.add("Schädel: " + count(helper, st -> st.is(Blocks.SKELETON_SKULL) || st.is(Blocks.SKELETON_WALL_SKULL)));
        }
        // die Halle: frei zwischen Eingang und Thron
        for (int z = -8; z <= 7; z++) {
            if (!helper.getLevel().getBlockState(o.offset(0, 2, z)).isAir()) {
                problems.add("der Weg zum Thron ist versperrt bei z=" + z);
                break;
            }
        }
        helper.assertTrue(problems.isEmpty(), String.join("; ", problems));
        helper.succeed();
    }

    /** Die Brutgrotte: die Brutmutter auf ihrem Hügel, zwei Nester mit Spawnern, zwei Truhen der Opfer, Eiersäcke, Netze, Fäden. */
    private static void broodGrotto(GameTestHelper helper) {
        rock(helper, 22);
        BlockPos o = helper.absolutePos(new BlockPos(24, 3, 24));
        build(helper, SiteSamples.broodGrotto(o));
        List<String> problems = new ArrayList<>();
        boss(helper, problems, ModEntities.BROOD_MOTHER.get(), o.offset(0, 3, 0));
        if (spawners(helper, ModEntities.CAVE_SPIDER.get()) != 2) {
            problems.add("Spawner der Höhlenspinnen: " + spawners(helper, ModEntities.CAVE_SPIDER.get()));
        }
        if (chests(helper, ModLootTables.SITE_BROOD_GROTTO) != 2) {
            problems.add("Truhen: " + chests(helper, ModLootTables.SITE_BROOD_GROTTO));
        }
        int webs = count(helper, st -> st.is(Blocks.COBWEB));
        int sacs = count(helper, st -> st.is(Blocks.MUSHROOM_STEM));
        int threads = count(helper, st -> st.is(Blocks.PALE_HANGING_MOSS));
        if (webs < 80 || sacs < 25 || threads < 15) {
            problems.add("Netze " + webs + ", Eiersäcke " + sacs + ", Fäden " + threads);
        }
        // die Höhle ist hohl: genug Luft
        int air = count(helper, BlockState::isAir);
        if (air < 4000) {
            problems.add("zu wenig Höhle: " + air);
        }
        helper.assertTrue(problems.isEmpty(), String.join("; ", problems));
        helper.succeed();
    }

    /**
     * Die Zwergenhalle: der Runenwächter vor der Schatzkammer, deren Eisentür sich mit dem Knopf öffnet; drei Truhen in der
     * Schatzkammer, Fässer und Truhen der Zwerge, die Esse mit eingesperrter Lava, Kupferleuchten, Schienen in den Stollen.
     */
    private static void dwarfHall(GameTestHelper helper) {
        rock(helper, 20);
        BlockPos o = helper.absolutePos(new BlockPos(24, 3, 26));
        build(helper, SiteSamples.dwarfHall(o));
        List<String> problems = new ArrayList<>();
        boss(helper, problems, ModEntities.RUNE_WARDEN.get(), SiteSamples.dwarfWarden(o));
        if (chests(helper, ModLootTables.SITE_DWARF_HOARD) != 3) {
            problems.add("Truhen der Schatzkammer: " + chests(helper, ModLootTables.SITE_DWARF_HOARD));
        }
        if (chests(helper, ModLootTables.SITE_DWARF_HALL) < 5) {
            problems.add("Fässer und Truhen der Zwerge: " + chests(helper, ModLootTables.SITE_DWARF_HALL));
        }
        BlockPos door = SiteSamples.dwarfVaultDoor(o);
        BlockState lower = helper.getLevel().getBlockState(door);
        if (!lower.is(Blocks.IRON_DOOR) || !helper.getLevel().getBlockState(door.above()).is(Blocks.IRON_DOOR)) {
            problems.add("keine Eisentür zur Schatzkammer: " + lower);
        }
        int lava = count(helper, st -> st.is(Blocks.LAVA));
        if (lava != 1) {
            problems.add("Lava: " + lava + " (genau der Kern der Esse)");
        }
        int bulbs = count(helper, st -> st.getBlock() instanceof net.minecraft.world.level.block.CopperBulbBlock
                && st.getValue(net.minecraft.world.level.block.CopperBulbBlock.LIT));
        if (bulbs < 12) {
            problems.add("Kupferleuchten: " + bulbs);
        }
        if (count(helper, st -> st.is(Blocks.RAIL)) < 12) {
            problems.add("Schienen: " + count(helper, st -> st.is(Blocks.RAIL)));
        }
        if (count(helper, st -> st.is(Blocks.BLAST_FURNACE)) != 2) {
            problems.add("Hochöfen: " + count(helper, st -> st.is(Blocks.BLAST_FURNACE)));
        }
        // der Knopf öffnet die Tür
        BlockPos button = door.offset(1, 1, 1);
        BlockState b = helper.getLevel().getBlockState(button);
        if (!b.is(Blocks.STONE_BUTTON)) {
            problems.add("kein Knopf neben der Tür: " + b);
            helper.assertTrue(problems.isEmpty(), String.join("; ", problems));
            return;
        }
        ((net.minecraft.world.level.block.ButtonBlock) b.getBlock()).press(b, helper.getLevel(), button, null);
        helper.runAfterDelay(4, () -> {
            if (!helper.getLevel().getBlockState(door).getValue(DoorBlock.OPEN)) {
                problems.add("der Knopf öffnet die Tür nicht");
            }
            helper.assertTrue(problems.isEmpty(), String.join("; ", problems));
            helper.succeed();
        });
    }

    /** Die Trollhöhle: der Trollvater auf seinem Lager, die Feuerstelle brennt, zwei Truhen im Hort, Gold und rohes Erz, Tropfsteine. */
    private static void trollDen(GameTestHelper helper) {
        rock(helper, 22);
        BlockPos o = helper.absolutePos(new BlockPos(24, 3, 22));
        build(helper, SiteSamples.trollDen(o));
        List<String> problems = new ArrayList<>();
        boss(helper, problems, ModEntities.TROLL_FATHER.get(), SiteSamples.trollBed(o).above(3));
        if (chests(helper, ModLootTables.SITE_TROLL_DEN) != 2) {
            problems.add("Truhen im Hort: " + chests(helper, ModLootTables.SITE_TROLL_DEN));
        }
        if (!helper.getLevel().getBlockState(SiteSamples.trollFire(o).above()).is(Blocks.CAMPFIRE)) {
            problems.add("keine Feuerstelle");
        }
        if (count(helper, st -> st.is(Blocks.RAW_GOLD_BLOCK) || st.is(Blocks.GOLD_BLOCK)) < 3) {
            problems.add("kein Gold im Hort");
        }
        if (count(helper, st -> st.is(Blocks.BONE_BLOCK)) < 12) {
            problems.add("Knochen: " + count(helper, st -> st.is(Blocks.BONE_BLOCK)));
        }
        if (count(helper, st -> st.is(Blocks.POINTED_DRIPSTONE) || st.is(Blocks.HANGING_ROOTS)) < 10) {
            problems.add("Wurzeln und Tropfsteine: " + count(helper, st -> st.is(Blocks.POINTED_DRIPSTONE) || st.is(Blocks.HANGING_ROOTS)));
        }
        helper.assertTrue(problems.isEmpty(), String.join("; ", problems));
        helper.succeed();
    }
}
