package io.github.sirschrolli.kingdomsatwar.gametest;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.block.TownBannerBlock;
import io.github.sirschrolli.kingdomsatwar.config.ModServerConfig;
import io.github.sirschrolli.kingdomsatwar.economy.Coin;
import io.github.sirschrolli.kingdomsatwar.economy.town.Town;
import io.github.sirschrolli.kingdomsatwar.economy.town.TownManager;
import io.github.sirschrolli.kingdomsatwar.economy.town.Towns;
import io.github.sirschrolli.kingdomsatwar.entity.realm.RealmWarriorEntity;
import io.github.sirschrolli.kingdomsatwar.registry.ModBlocks;
import io.github.sirschrolli.kingdomsatwar.war.Realm;
import io.github.sirschrolli.kingdomsatwar.war.RealmRaids;
import io.github.sirschrolli.kingdomsatwar.war.RealmState;
import io.github.sirschrolli.kingdomsatwar.war.WarriorRole;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * Tests der Heerhaufen: Zusammensetzung, Anmarsch aus Richtung der Burg, Sieg mit Kriegskasse im Lager, Plündern des
 * unverteidigten Lagers und der Abzug, der Angriff zur rechten Zeit (und nicht ohne Krieg oder bei abgeschalteter
 * Einstellung). Eigene Testgruppe: die Siedlungstests räumen zwischendurch alle Siedlungen weg.
 */
final class RaidGameTests {
    private static final Identifier ARENA = KingdomsAtWar.id("test_arena");

    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> BAND =
            ModGameTests.TEST_FUNCTIONS.register("raid_band", () -> RaidGameTests::band);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> VICTORY =
            ModGameTests.TEST_FUNCTIONS.register("raid_march_victory", () -> RaidGameTests::victory);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> PLUNDER =
            ModGameTests.TEST_FUNCTIONS.register("raid_plunder", () -> RaidGameTests::plunder);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> SCHEDULE =
            ModGameTests.TEST_FUNCTIONS.register("raid_schedule", () -> RaidGameTests::schedule);

    private RaidGameTests() {}

    static void init() {}

    static void register(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment) {
        add(event, environment, BAND, 40);
        add(event, environment, VICTORY, 1400);
        add(event, environment, PLUNDER, 1400);
        // großzügig: im großen Testlauf laden die weit entfernten Chunks unter Last langsamer
        add(event, environment, SCHEDULE, 4000);
    }

    private static void add(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment,
            DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> test, int maxTicks) {
        event.registerTest(test.getId(), new FunctionGameTestInstance(test.key(),
                new TestData<>(environment, Level.OVERWORLD, ARENA, maxTicks, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
    }

    // =====================================================================================
    // Hilfen
    // =====================================================================================

    /** Eine Siedlung weit draußen: Chunks im Umkreis von 64 Blöcken geladen, Banner und eine Truhe mit Vorräten daneben. */
    private record Site(ServerLevel level, BlockPos banner, BlockPos chest, Town town, ServerPlayer owner, List<long[]> chunks) {
        boolean loaded() {
            for (long[] c : this.chunks) {
                if (!this.level.areEntitiesLoaded(ChunkPos.pack((int) c[0], (int) c[1]))) {
                    return false;
                }
            }
            return true;
        }

        /** Der Testserver setzt Spieler gern zurück: wieder an seinen Platz. */
        void place(Vec3 offset) {
            this.owner.snapTo(this.banner.getX() + 0.5 + offset.x, this.banner.getY() + offset.y, this.banner.getZ() + 0.5 + offset.z);
        }

        int stored() {
            int n = 0;
            if (this.level.getBlockEntity(this.chest) instanceof Container container) {
                for (int i = 0; i < container.getContainerSize(); i++) {
                    n += container.getItem(i).getCount();
                }
            }
            return n;
        }

        int count(net.minecraft.world.item.Item item) {
            int n = 0;
            if (this.level.getBlockEntity(this.chest) instanceof Container container) {
                for (int i = 0; i < container.getContainerSize(); i++) {
                    if (container.getItem(i).is(item)) {
                        n += container.getItem(i).getCount();
                    }
                }
            }
            return n;
        }

        /** Aufräumen – nur das Eigene (die anderen Heerhaufen-Tests laufen gleichzeitig). */
        void close(GameTestHelper helper) {
            RealmRaids.devForget(this.level, this.owner.getUUID());
            Town town = Towns.of(this.level.getServer()).byOwner(this.owner.getUUID());
            if (town != null) {
                TownManager.disband(this.level.getServer(), town);
            }
            RealmState state = RealmState.of(this.level);
            for (Realm realm : Realm.values()) {
                state.makePeace(realm, this.owner.getUUID());
            }
            state.save(this.level);
            for (long[] c : this.chunks) {
                this.level.setChunkForced((int) c[0], (int) c[1], false);
            }
            this.level.getServer().getPlayerList().remove(this.owner);
        }
    }

    private static Site site(GameTestHelper helper, int slot, Vec3 ownerOffset) {
        ServerLevel level = helper.getLevel();
        BlockPos base = helper.absolutePos(BlockPos.ZERO);
        int x = base.getX() + 12000 + slot * 400;
        int z = base.getZ() + 12000;
        List<long[]> chunks = new ArrayList<>();
        for (int cx = (x - 64) >> 4; cx <= (x + 64) >> 4; cx++) {
            for (int cz = (z - 64) >> 4; cz <= (z + 64) >> 4; cz++) {
                level.getChunk(cx, cz);
                level.setChunkForced(cx, cz, true);
                chunks.add(new long[] {cx, cz});
            }
        }
        BlockPos banner = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, new BlockPos(x, 0, z));
        // ebener, fester Platz um das Banner
        for (int dx = -6; dx <= 6; dx++) {
            for (int dz = -6; dz <= 6; dz++) {
                level.setBlock(banner.offset(dx, -1, dz), Blocks.STONE_BRICKS.defaultBlockState(), Block.UPDATE_CLIENTS);
                for (int dy = 0; dy < 4; dy++) {
                    level.setBlock(banner.offset(dx, dy, dz), Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
                }
            }
        }
        ServerPlayer owner = helper.makeMockServerPlayerInLevel();
        owner.connection.markClientLoaded();
        owner.getAbilities().instabuild = false;
        owner.getAbilities().invulnerable = true;
        owner.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
        Vec3 at = Vec3.atBottomCenterOf(banner).add(ownerOffset);
        owner.snapTo(at.x, banner.getY() + ownerOffset.y, at.z, 0.0f, 0.0f);
        BlockState lower = ModBlocks.TOWN_BANNER.get().defaultBlockState();
        level.setBlock(banner, lower, Block.UPDATE_ALL);
        level.setBlock(banner.above(), lower.setValue(TownBannerBlock.HALF, DoubleBlockHalf.UPPER), Block.UPDATE_ALL);
        helper.assertValueEqual(TownManager.placeBanner(owner, level, banner), TownManager.FoundResult.FOUNDED, "Banner setzen");
        Town town = Towns.of(level.getServer()).byOwner(owner.getUUID());
        helper.assertTrue(town != null, "Siedlung nicht gegründet");
        // das Lager: eine Truhe neben dem Banner mit Vorräten
        BlockPos chest = banner.offset(2, 0, 0);
        level.setBlock(chest, Blocks.CHEST.defaultBlockState(), Block.UPDATE_ALL);
        if (level.getBlockEntity(chest) instanceof Container container) {
            container.setItem(0, new ItemStack(Items.BREAD, 20));
            container.setItem(1, new ItemStack(Items.OAK_PLANKS, 40));
            container.setItem(2, new ItemStack(Items.IRON_INGOT, 10));
        }
        return new Site(level, banner, chest, town, owner, chunks);
    }

    private static double meanDistance(List<RealmWarriorEntity> raiders, BlockPos to) {
        return raiders.stream().mapToDouble(w -> Math.sqrt(w.distanceToSqr(Vec3.atCenterOf(to)))).average().orElse(0.0);
    }

    // =====================================================================================
    // Tests
    // =====================================================================================

    /** Zusammensetzung: ein Hauptmann, wachsende Größe (6 … 14), Ritter erst ab dem zweiten Heerhaufen. */
    private static void band(GameTestHelper helper) {
        RandomSource random = RandomSource.create(5);
        List<WarriorRole> first = RealmRaids.band(1, 0, random);
        helper.assertValueEqual(first.size(), 6, "Größe des ersten Heerhaufens");
        helper.assertTrue(!first.contains(WarriorRole.KNIGHT) && !first.contains(WarriorRole.LORD), "erster Heerhaufen mit Ritter oder Herrscher");
        int previous = first.size();
        for (int number = 2; number <= 12; number++) {
            List<WarriorRole> band = RealmRaids.band(number, 2, random);
            helper.assertValueEqual((int) band.stream().filter(r -> r == WarriorRole.CAPTAIN).count(), 1, "Hauptleute im Heerhaufen " + number);
            helper.assertTrue(band.size() >= previous && band.size() <= RealmRaids.MAX_BAND, "Größe des Heerhaufens " + number + ": " + band.size());
            previous = band.size();
        }
        helper.assertValueEqual(RealmRaids.band(30, 4, random).size(), RealmRaids.MAX_BAND, "größter Heerhaufen");
        helper.succeed();
    }

    /**
     * Ein Heerhaufen sammelt sich auf der Seite der Burg in rund 44 Blöcken, zieht zum Banner und greift die Soldaten
     * des Spielers an; ist er geschlagen, liegt die Kriegskasse im Lager und der Heerhaufen ist vorbei.
     */
    private static void victory(GameTestHelper helper) {
        Site[] site = {null};
        RealmRaids.Raid[] raid = {null};
        double[] start = {0.0};
        helper.startSequence()
                .thenExecute(() -> site[0] = site(helper, 0, new Vec3(0.0, 0.0, 1.5)))
                .thenWaitUntil(() -> helper.assertTrue(site[0].loaded(), "Chunks laden nicht"))
                .thenExecute(() -> {
                    ServerLevel level = site[0].level();
                    RealmState.of(level).declareWar(Realm.RABENSTEIN, site[0].owner().getUUID());
                    raid[0] = RealmRaids.start(level, Realm.RABENSTEIN, site[0].owner(), site[0].town());
                    helper.assertTrue(raid[0] != null, "kein Heerhaufen");
                    List<RealmWarriorEntity> raiders = RealmRaids.devRaiders(level, raid[0]);
                    helper.assertValueEqual(raiders.size(), raid[0].total(), "Krieger in der Welt");
                    helper.assertTrue(raiders.stream().allMatch(w -> w.realm() == Realm.RABENSTEIN && raid[0].id().equals(w.raid())),
                            "Krieger gehören nicht zum Heerhaufen");
                    helper.assertTrue(raiders.stream().filter(w -> w.role() == WarriorRole.CAPTAIN).count() == 1, "kein Hauptmann");
                    start[0] = meanDistance(raiders, site[0].banner());
                    helper.assertTrue(start[0] > 28.0 && start[0] < 62.0, "Sammelplatz in " + Math.round(start[0]) + " Blöcken");
                    helper.assertTrue(RealmRaids.against(level, site[0].town()) == raid[0], "Heerhaufen gegen die Siedlung nicht vermerkt");
                })
                .thenIdle(160)
                .thenExecute(() -> {
                    double now = meanDistance(RealmRaids.devRaiders(site[0].level(), raid[0]), site[0].banner());
                    helper.assertTrue(now < start[0] - 8.0, "Heerhaufen marschiert nicht: " + Math.round(start[0]) + " → " + Math.round(now));
                    // alle erschlagen
                    for (RealmWarriorEntity w : RealmRaids.devRaiders(site[0].level(), raid[0])) {
                        w.hurtServer(site[0].level(), site[0].level().damageSources().playerAttack(site[0].owner()), 10_000.0f);
                    }
                })
                .thenWaitUntil(() -> helper.assertTrue(RealmRaids.against(site[0].level(), site[0].town()) == null, "Heerhaufen nicht beendet"))
                .thenExecute(() -> {
                    helper.assertTrue(site[0].count(Coin.SILVER.item()) >= 4, "keine Kriegskasse im Lager");
                    helper.assertTrue(site[0].count(Items.BREAD) == 20, "Lager trotz Sieg geplündert");
                    site[0].close(helper);
                })
                .thenSucceed();
    }

    /** Steht niemand am Banner, plündert der Heerhaufen das Lager (etwa ein Drittel) und zieht ab. */
    private static void plunder(GameTestHelper helper) {
        Site[] site = {null};
        RealmRaids.Raid[] raid = {null};
        int[] before = {0};
        helper.startSequence()
                // der Spieler ist weit weg vom Banner (keine Verteidigung)
                .thenExecute(() -> site[0] = site(helper, 1, new Vec3(0.0, 0.0, -40.0)))
                .thenWaitUntil(() -> helper.assertTrue(site[0].loaded(), "Chunks laden nicht"))
                .thenExecute(() -> {
                    ServerLevel level = site[0].level();
                    RealmState.of(level).declareWar(Realm.DORNFELS, site[0].owner().getUUID());
                    raid[0] = RealmRaids.start(level, Realm.DORNFELS, site[0].owner(), site[0].town());
                    helper.assertTrue(raid[0] != null, "kein Heerhaufen");
                    before[0] = site[0].stored();
                    // gleich ans Banner (der Weg ist im anderen Test geprüft)
                    RealmRaids.devAdvance(level, raid[0], 3.0);
                    site[0].place(new Vec3(0.0, 0.0, -40.0));
                })
                .thenWaitUntil(() -> helper.assertTrue(raid[0].phase() == RealmRaids.Phase.RETREAT, "Heerhaufen plündert nicht"))
                .thenExecute(() -> {
                    int after = site[0].stored();
                    helper.assertTrue(after < before[0] && after > before[0] / 2, "Plündern falsch: " + before[0] + " → " + after);
                    helper.assertValueEqual(site[0].count(Items.BREAD), 14, "Brot nach dem Plündern");
                    // auf dem Rückweg: Ziel ist der Sammelplatz
                    helper.assertTrue(RealmRaids.devRaiders(site[0].level(), raid[0]).stream().allMatch(w -> raid[0].muster().equals(w.raidGoal())),
                            "Heerhaufen zieht nicht ab");
                    site[0].close(helper);
                })
                .thenSucceed();
    }

    /**
     * Zeitplan: Liegt der Spieler mit einem Reich im Krieg und ist die Frist um, kommt der Heerhaufen, sobald er in seiner
     * Siedlung ist – nicht ohne Krieg und nicht bei abgeschalteter Einstellung.
     */
    private static void schedule(GameTestHelper helper) {
        Site[] site = {null};
        long[] marks = {0L};
        helper.startSequence()
                .thenExecute(() -> site[0] = site(helper, 2, new Vec3(0.0, 0.0, 2.0)))
                .thenWaitUntil(() -> helper.assertTrue(site[0].loaded(), "Chunks laden nicht"))
                .thenExecute(() -> {
                    // ohne Krieg: nichts
                    site[0].place(new Vec3(0.0, 0.0, 2.0));
                    marks[0] = site[0].level().getGameTime();
                })
                .thenIdle(110)
                .thenExecute(() -> {
                    ServerLevel level = site[0].level();
                    helper.assertTrue(RealmRaids.against(level, site[0].town()) == null, "Heerhaufen ohne Krieg");
                    // Krieg, Frist noch nicht um
                    RealmState.of(level).declareWar(Realm.SILBERAU, site[0].owner().getUUID());
                    RealmRaids.devFeud(level, Realm.SILBERAU, site[0].owner().getUUID(), level.getGameTime());
                    site[0].place(new Vec3(0.0, 0.0, 2.0));
                })
                .thenIdle(110)
                .thenExecute(() -> {
                    ServerLevel level = site[0].level();
                    helper.assertTrue(RealmRaids.against(level, site[0].town()) == null, "Heerhaufen vor Ablauf der Frist");
                    // Einstellung aus: trotz abgelaufener Frist nichts
                    RealmRaids.devFeud(level, Realm.SILBERAU, site[0].owner().getUUID(), level.getGameTime() - 30L * 24000L);
                    ModServerConfig.REALM_RAIDS.set(false);
                    site[0].place(new Vec3(0.0, 0.0, 2.0));
                })
                .thenIdle(110)
                .thenExecute(() -> {
                    ModServerConfig.REALM_RAIDS.set(true);
                    helper.assertTrue(RealmRaids.against(site[0].level(), site[0].town()) == null, "Heerhaufen trotz abgeschalteter Einstellung");
                    site[0].place(new Vec3(0.0, 0.0, 2.0));
                })
                .thenWaitUntil(() -> {
                    RealmRaids.Raid raid = RealmRaids.against(site[0].level(), site[0].town());
                    helper.assertTrue(raid != null && raid.realm() == Realm.SILBERAU, "kein Heerhaufen nach Ablauf der Frist");
                })
                .thenExecute(() -> site[0].close(helper))
                .thenSucceed();
    }
}
