package io.github.sirschrolli.kingdomsatwar.gametest;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.config.ModServerConfig;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierEntity;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierType;
import io.github.sirschrolli.kingdomsatwar.registry.ModBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.ModDataComponents;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.ModItems;
import io.github.sirschrolli.kingdomsatwar.siege.SiegeCrew;
import io.github.sirschrolli.kingdomsatwar.siege.SiegeKitItem;
import io.github.sirschrolli.kingdomsatwar.siege.SiegeProjectile;
import io.github.sirschrolli.kingdomsatwar.siege.SiegeType;
import io.github.sirschrolli.kingdomsatwar.siege.SiegeWeaponEntity;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * Tests der Belagerungswaffen: Aufstellen, Laden, Reparieren und Zerlegen; der Rammbock bricht Tore auf (Mauern nicht);
 * die Balliste durchschlägt Gegner und Glas und bleibt stecken; das Katapult schlägt eine Bresche; der Tribok trifft auf
 * 70 Blöcke; eine Bedienmannschaft nimmt Befehle des Kommandostabs an und verschont die eigenen Leute; Schutzregeln
 * (Siegelblöcke, Einstellung) und das Zerbrechen.
 *
 * <p>Die Schießplätze liegen weit draußen (eigene, dauerhaft geladene Chunks), damit kein Wurf andere Tests trifft.
 */
final class SiegeGameTests {
    private static final Identifier ARENA = KingdomsAtWar.id("test_arena");

    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> KIT =
            ModGameTests.TEST_FUNCTIONS.register("siege_kit", () -> SiegeGameTests::kit);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> RAM =
            ModGameTests.TEST_FUNCTIONS.register("siege_ram_gate", () -> SiegeGameTests::ram);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> BALLISTA =
            ModGameTests.TEST_FUNCTIONS.register("siege_ballista_pierce", () -> SiegeGameTests::ballista);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> CATAPULT =
            ModGameTests.TEST_FUNCTIONS.register("siege_catapult_breach", () -> SiegeGameTests::catapult);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> TREBUCHET =
            ModGameTests.TEST_FUNCTIONS.register("siege_trebuchet_aim", () -> SiegeGameTests::trebuchet);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> CREW =
            ModGameTests.TEST_FUNCTIONS.register("siege_crew", () -> SiegeGameTests::crew);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> PROTECTION =
            ModGameTests.TEST_FUNCTIONS.register("siege_protection", () -> SiegeGameTests::protection);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> BREAK =
            ModGameTests.TEST_FUNCTIONS.register("siege_break_apart", () -> SiegeGameTests::breakApart);

    private SiegeGameTests() {}

    static void init() {}

    static void register(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment) {
        add(event, environment, KIT, 100);
        add(event, environment, RAM, 500);
        add(event, environment, BALLISTA, 500);
        add(event, environment, CATAPULT, 700);
        add(event, environment, TREBUCHET, 900);
        add(event, environment, CREW, 1400);
        add(event, environment, PROTECTION, 100);
        add(event, environment, BREAK, 100);
    }

    private static void add(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment,
            DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> test, int maxTicks) {
        event.registerTest(test.getId(), new FunctionGameTestInstance(test.key(),
                new TestData<>(environment, Level.OVERWORLD, ARENA, maxTicks, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
    }

    // =====================================================================================
    // Hilfen
    // =====================================================================================

    private static ServerPlayer player(GameTestHelper helper, Vec3 at) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.connection.markClientLoaded();
        player.getAbilities().instabuild = false;
        player.getAbilities().invulnerable = false;
        player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
        player.getInventory().clearContent();
        player.snapTo(at.x, at.y, at.z, 0.0f, 0.0f);
        return player;
    }

    private static void leave(GameTestHelper helper, ServerPlayer player) {
        helper.getLevel().getServer().getPlayerList().remove(player);
    }

    /** Spieler blickt auf diesen Punkt. */
    private static void lookAt(ServerPlayer player, Vec3 target) {
        Vec3 d = target.subtract(player.getEyePosition());
        float yaw = (float) (-Mth.atan2(d.x, d.z) * Mth.RAD_TO_DEG);
        float pitch = (float) (-Mth.atan2(d.y, d.horizontalDistance()) * Mth.RAD_TO_DEG);
        player.setYRot(yaw);
        player.setYHeadRot(yaw);
        player.setXRot(pitch);
    }

    /**
     * Ein Schießplatz weit draußen: Chunks laden und dauerhaft halten, ein Steinboden ({@code halfWidth} zu jeder Seite,
     * {@code back} Blöcke hinter und {@code length} vor dem Ursprung, also nach Norden) mit Luft darüber.
     */
    private record Range(ServerLevel level, BlockPos origin, int halfWidth, int back, int length, List<long[]> chunks) {
        AABB area() {
            return new AABB(this.origin.getX() - this.halfWidth - 2, this.origin.getY() - 4, this.origin.getZ() - this.length - 2,
                    this.origin.getX() + this.halfWidth + 3, this.origin.getY() + 90, this.origin.getZ() + this.back + 3);
        }

        Vec3 at(double dx, double dy, double dz) {
            return new Vec3(this.origin.getX() + dx, this.origin.getY() + dy, this.origin.getZ() + dz);
        }

        BlockPos pos(int dx, int dy, int dz) {
            return this.origin.offset(dx, dy, dz);
        }

        /** Frisch geladene Chunks bekommen ihre Wesen erst nach ein paar Ticks – vorher sähe niemand die neuen Wesen. */
        boolean entitiesLoaded() {
            for (long[] c : this.chunks) {
                if (!this.level.areEntitiesLoaded(net.minecraft.world.level.ChunkPos.pack((int) c[0], (int) c[1]))) {
                    return false;
                }
            }
            return true;
        }

        /** Alles Bewegliche wegräumen und die Chunks freigeben. */
        void close() {
            for (Entity e : this.level.getEntitiesOfClass(Entity.class, this.area(), e -> !(e instanceof Player))) {
                e.discard();
            }
            for (long[] c : this.chunks) {
                this.level.setChunkForced((int) c[0], (int) c[1], false);
            }
        }
    }

    private static Range range(GameTestHelper helper, int slot, int halfWidth, int back, int length) {
        ServerLevel level = helper.getLevel();
        BlockPos base = helper.absolutePos(BlockPos.ZERO);
        BlockPos origin = new BlockPos(base.getX() + 9000 + slot * 320, 150, base.getZ() + 9000);
        List<long[]> chunks = new ArrayList<>();
        for (int cx = (origin.getX() - halfWidth - 4) >> 4; cx <= (origin.getX() + halfWidth + 4) >> 4; cx++) {
            for (int cz = (origin.getZ() - length - 4) >> 4; cz <= (origin.getZ() + back + 4) >> 4; cz++) {
                level.getChunk(cx, cz);
                level.setChunkForced(cx, cz, true);
                chunks.add(new long[] {cx, cz});
            }
        }
        BlockState stone = Blocks.STONE.defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();
        for (int x = -halfWidth; x <= halfWidth; x++) {
            for (int z = -length; z <= back; z++) {
                level.setBlock(origin.offset(x, -1, z), stone, Block.UPDATE_CLIENTS);
                for (int y = 0; y < 14; y++) {
                    level.setBlock(origin.offset(x, y, z), air, Block.UPDATE_CLIENTS);
                }
            }
        }
        return new Range(level, origin, halfWidth, back, length, chunks);
    }

    /** Ablauf auf dem Schießplatz: erst warten, bis seine Chunks samt Wesen geladen sind, dann aufbauen. */
    private static net.minecraft.gametest.framework.GameTestSequence prepared(GameTestHelper helper, Range r, Runnable setup) {
        return helper.startSequence()
                .thenWaitUntil(() -> helper.assertTrue(ModGameTests.awaitLoading(r.entitiesLoaded()), "Chunks des Schießplatzes laden nicht"))
                .thenExecute(setup)
                .thenIdle(2);
    }

    private static SiegeWeaponEntity weapon(ServerLevel level, SiegeType type, Vec3 at, float yaw) {
        SiegeWeaponEntity weapon = ModEntities.siegeWeapon(type).create(level, EntitySpawnReason.COMMAND);
        weapon.snapTo(at.x, at.y, at.z, yaw, 0.0f);
        weapon.setYHeadRot(yaw);
        weapon.setAmmo(weapon.capacity());
        level.addFreshEntity(weapon);
        return weapon;
    }

    /** Eine Mumie ohne eigenen Willen (verbrennt nicht in der Sonne) mit viel Leben. */
    private static LivingEntity target(ServerLevel level, Vec3 at) {
        var mob = ModEntities.HUSK.get().create(level, EntitySpawnReason.COMMAND);
        mob.snapTo(at.x, at.y, at.z, 0.0f, 0.0f);
        mob.setNoAi(true);
        mob.getAttribute(Attributes.MAX_HEALTH).setBaseValue(60.0);
        mob.setHealth(60.0f);
        mob.setPersistenceRequired();
        level.addFreshEntity(mob);
        return mob;
    }

    private static int count(ServerLevel level, BlockPos from, BlockPos to, Block block) {
        int n = 0;
        for (BlockPos pos : BlockPos.betweenClosed(from, to)) {
            if (level.getBlockState(pos).is(block)) {
                n++;
            }
        }
        return n;
    }

    // =====================================================================================
    // Bausatz: aufstellen, laden, reparieren, speichern, zerlegen
    // =====================================================================================

    private static void kit(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = player(helper, helper.absoluteVec(new Vec3(4.5, 1, 0.6)));
        try {
            // aufstellen: in Blickrichtung des Spielers, der Bausatz ist verbraucht
            player.setYRot(0.0f);
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.CATAPULT_KIT.get()));
            BlockPos ground = helper.absolutePos(new BlockPos(4, 0, 5));
            InteractionResult placed = player.getMainHandItem().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                    new BlockHitResult(Vec3.atCenterOf(ground).add(0.0, 0.5, 0.0), Direction.UP, ground, false)));
            helper.assertTrue(placed.consumesAction(), "Katapult lässt sich nicht aufstellen: " + placed);
            helper.assertTrue(player.getMainHandItem().isEmpty(), "Bausatz nicht verbraucht");
            List<SiegeWeaponEntity> found = level.getEntitiesOfClass(SiegeWeaponEntity.class, new AABB(ground.above()).inflate(1.5));
            helper.assertValueEqual(found.size(), 1, "aufgestellte Katapulte");
            SiegeWeaponEntity catapult = found.getFirst();
            helper.assertTrue(catapult.siege() == SiegeType.CATAPULT && catapult.isOwnedBy(player), "Katapult gehört nicht dem Spieler");
            helper.assertTrue(Math.abs(catapult.healthFraction() - 1.0f) < 1.0e-3f, "neues Katapult nicht unversehrt");

            // laden: Steinkugeln bis der Vorrat voll ist, der Rest bleibt in der Hand
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.SIEGE_STONE.get(), 16));
            catapult.interact(player, InteractionHand.MAIN_HAND, Vec3.ZERO);
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.SIEGE_STONE.get(), 5));
            catapult.interact(player, InteractionHand.MAIN_HAND, Vec3.ZERO);
            helper.assertValueEqual(catapult.ammo(), catapult.capacity(), "Vorrat nach dem Laden");
            helper.assertValueEqual(player.getMainHandItem().getCount(), 5, "Steinkugeln trotz vollem Vorrat genommen");
            // Bolzen passen nicht ins Katapult
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.BALLISTA_BOLT.get(), 4));
            catapult.interact(player, InteractionHand.MAIN_HAND, Vec3.ZERO);
            helper.assertValueEqual(player.getMainHandItem().getCount(), 4, "Bolzen ins Katapult geladen");

            // reparieren: jedes Brett ein Zehntel
            catapult.damage(level, SiegeType.CATAPULT.health() * 0.35f, null);
            helper.assertTrue(Math.abs(catapult.healthFraction() - 0.65f) < 0.01f, "Schaden falsch: " + catapult.healthFraction());
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.OAK_PLANKS, 2));
            catapult.interact(player, InteractionHand.MAIN_HAND, Vec3.ZERO);
            catapult.interact(player, InteractionHand.MAIN_HAND, Vec3.ZERO);
            helper.assertTrue(Math.abs(catapult.healthFraction() - 0.85f) < 0.01f, "Reparatur falsch: " + catapult.healthFraction());
            helper.assertTrue(player.getMainHandItem().isEmpty(), "Bretter nicht verbraucht");

            // speichern und laden
            var output = net.minecraft.world.level.storage.TagValueOutput.createWithContext(net.minecraft.util.ProblemReporter.DISCARDING,
                    level.registryAccess());
            catapult.saveWithoutId(output);
            SiegeWeaponEntity copy = ModEntities.CATAPULT.get().create(level, EntitySpawnReason.LOAD);
            copy.load(net.minecraft.world.level.storage.TagValueInput.create(net.minecraft.util.ProblemReporter.DISCARDING, level.registryAccess(),
                    output.buildResult()));
            helper.assertTrue(copy.ammo() == catapult.ammo() && Math.abs(copy.healthFraction() - catapult.healthFraction()) < 1.0e-3f
                    && copy.isOwnedBy(player) && copy.ready(), "Katapult nach dem Laden anders");

            // zerlegen: schleichend mit leerer Hand – Bausatz mit Zustand und die Munition kommen ins Gepäck
            player.setShiftKeyDown(true);
            catapult.interact(player, InteractionHand.MAIN_HAND, Vec3.ZERO);
            player.setShiftKeyDown(false);
            helper.assertTrue(catapult.isRemoved(), "Katapult nicht zerlegt");
            ItemStack kit = ItemStack.EMPTY;
            int stones = 0;
            for (ItemStack stack : player.getInventory()) {
                if (stack.is(ModItems.CATAPULT_KIT.get())) {
                    kit = stack;
                } else if (stack.is(ModItems.SIEGE_STONE.get())) {
                    stones += stack.getCount();
                }
            }
            helper.assertTrue(!kit.isEmpty(), "kein Bausatz nach dem Zerlegen");
            helper.assertTrue(Math.abs(SiegeKitItem.condition(kit) - 0.85f) < 0.01f && kit.has(ModDataComponents.SIEGE_CONDITION.get()),
                    "Bausatz merkt sich den Zustand nicht: " + SiegeKitItem.condition(kit));
            helper.assertValueEqual(stones, 16, "Munition nach dem Zerlegen");

            // wieder aufstellen: der Zustand bleibt
            player.getInventory().clearContent();
            player.setItemInHand(InteractionHand.MAIN_HAND, kit);
            player.getMainHandItem().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                    new BlockHitResult(Vec3.atCenterOf(ground).add(0.0, 0.5, 0.0), Direction.UP, ground, false)));
            List<SiegeWeaponEntity> again = level.getEntitiesOfClass(SiegeWeaponEntity.class, new AABB(ground.above()).inflate(1.5));
            helper.assertTrue(again.size() == 1 && Math.abs(again.getFirst().healthFraction() - 0.85f) < 0.01f, "Zustand nach dem Aufstellen");
            again.getFirst().discard();

            // kein Platz: unter einer niedrigen Decke lässt sich der Tribok nicht aufstellen
            for (int x = 2; x <= 6; x++) {
                for (int z = 3; z <= 7; z++) {
                    helper.setBlock(x, 3, z, Blocks.STONE);
                }
            }
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.TREBUCHET_KIT.get()));
            InteractionResult blocked = player.getMainHandItem().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                    new BlockHitResult(Vec3.atCenterOf(ground).add(0.0, 0.5, 0.0), Direction.UP, ground, false)));
            helper.assertTrue(!blocked.consumesAction() && player.getMainHandItem().getCount() == 1, "Tribok unter der Decke aufgestellt");
            helper.assertTrue(level.getEntitiesOfClass(SiegeWeaponEntity.class, new AABB(ground.above()).inflate(2.0)).isEmpty(),
                    "Waffe trotz fehlenden Platzes");
        } finally {
            leave(helper, player);
        }
        helper.succeed();
    }

    // =====================================================================================
    // Rammbock
    // =====================================================================================

    /** Der Rammbock bricht ein Tor (zwei Stöße) und ein Fallgitter (drei Stöße) auf; die Steinmauer hält. */
    private static void ram(GameTestHelper helper) {
        Range r = range(helper, 0, 6, 4, 12);
        ServerLevel level = r.level();
        // Mauer quer bei z = -6: unten Tür (Mitte) zwischen Steinziegeln, darüber Gitter, oben Steinziegel
        int z = -6;
        for (int x = -3; x <= 3; x++) {
            for (int y = 0; y <= 3; y++) {
                level.setBlock(r.pos(x, y, z), Blocks.STONE_BRICKS.defaultBlockState(), Block.UPDATE_ALL);
            }
        }
        BlockState door = Blocks.OAK_DOOR.defaultBlockState().setValue(DoorBlock.FACING, Direction.NORTH);
        level.setBlock(r.pos(0, 0, z), door.setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER), Block.UPDATE_CLIENTS);
        level.setBlock(r.pos(0, 1, z), door.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER), Block.UPDATE_CLIENTS);
        level.setBlock(r.pos(-1, 1, z), Blocks.IRON_BARS.defaultBlockState(), Block.UPDATE_ALL);
        level.setBlock(r.pos(1, 1, z), Blocks.IRON_BARS.defaultBlockState(), Block.UPDATE_ALL);
        SiegeWeaponEntity[] ram = {null};
        int[] strikes = {0};
        Runnable strike = () -> {
            ram[0].devPose(1000);
            helper.assertTrue(ram[0].fire(level, null), "Rammbock stößt nicht zu");
            strikes[0]++;
        };
        prepared(helper, r, () -> ram[0] = weapon(level, SiegeType.RAM, r.at(0.5, 0.0, -2.4), 180.0f))
                .thenExecute(strike)
                .thenIdle(8)
                .thenExecute(() -> {
                    helper.assertTrue(level.getBlockState(r.pos(0, 0, z)).is(Blocks.OAK_DOOR), "Tür bricht schon beim ersten Stoß");
                    strike.run();
                })
                .thenIdle(8)
                .thenExecute(() -> {
                    helper.assertTrue(level.getBlockState(r.pos(0, 0, z)).isAir() && level.getBlockState(r.pos(0, 1, z)).isAir(),
                            "Tür nach zwei Stößen nicht offen: " + level.getBlockState(r.pos(0, 0, z)));
                    helper.assertTrue(level.getBlockState(r.pos(1, 1, z)).is(Blocks.IRON_BARS), "Gitter bricht zu früh");
                    strike.run();
                })
                .thenIdle(8)
                .thenExecute(() -> {
                    helper.assertTrue(level.getBlockState(r.pos(-1, 1, z)).isAir() && level.getBlockState(r.pos(1, 1, z)).isAir(),
                            "Gitter nach drei Stößen nicht zerschlagen");
                    for (int x : new int[] {-1, 1}) {
                        helper.assertTrue(level.getBlockState(r.pos(x, 0, z)).is(Blocks.STONE_BRICKS)
                                && level.getBlockState(r.pos(x, 2, z)).is(Blocks.STONE_BRICKS), "Rammbock zerschlägt die Steinmauer");
                    }
                    helper.assertTrue(level.getBlockState(r.pos(0, 2, z)).is(Blocks.STONE_BRICKS), "Rammbock zerschlägt den Sturz");
                    helper.assertValueEqual(strikes[0], 3, "Stöße");
                    r.close();
                })
                .thenSucceed();
    }

    // =====================================================================================
    // Balliste
    // =====================================================================================

    /** Der Bolzen durchschlägt zwei Gegner, zerschlägt eine Glasscheibe und bleibt in der Mauer stecken. */
    private static void ballista(GameTestHelper helper) {
        Range r = range(helper, 1, 5, 4, 24);
        ServerLevel level = r.level();
        SiegeWeaponEntity[] ballista = {null};
        LivingEntity[] foes = {null, null};
        for (int y = 0; y <= 2; y++) {
            for (int x = -1; x <= 1; x++) {
                level.setBlock(r.pos(x, y, -12), Blocks.GLASS_PANE.defaultBlockState(), Block.UPDATE_ALL);
                level.setBlock(r.pos(x, y, -17), Blocks.STONE_BRICKS.defaultBlockState(), Block.UPDATE_ALL);
            }
        }
        prepared(helper, r, () -> {
            ballista[0] = weapon(level, SiegeType.BALLISTA, r.at(0.5, 0.0, 0.5), 180.0f);
            foes[0] = target(level, r.at(0.5, 0.0, -5.5));
            foes[1] = target(level, r.at(0.5, 0.0, -8.5));
        })
                .thenExecute(() -> {
                    helper.assertTrue(ballista[0].fire(level, null), "Balliste schießt nicht");
                    helper.assertValueEqual(ballista[0].ammo(), ballista[0].capacity() - 1, "Bolzen nach dem Schuss");
                })
                .thenWaitUntil(() -> helper.assertTrue(foes[0].getHealth() < foes[0].getMaxHealth() && foes[1].getHealth() < foes[1].getMaxHealth(),
                        "Bolzen durchschlägt nicht beide: " + foes[0].getHealth() + " / " + foes[1].getHealth()))
                .thenWaitUntil(() -> helper.assertTrue(level.getBlockState(r.pos(0, 0, -12)).isAir() || level.getBlockState(r.pos(0, 1, -12)).isAir(),
                        "Glas nicht zerschlagen"))
                .thenWaitUntil(() -> {
                    List<SiegeProjectile> bolts = level.getEntitiesOfClass(SiegeProjectile.class, r.area());
                    helper.assertTrue(bolts.size() == 1 && bolts.getFirst().stuck(), "Bolzen steckt nicht in der Mauer");
                    helper.assertTrue(bolts.getFirst().getZ() > r.origin().getZ() - 17.5, "Bolzen steckt hinter der Mauer");
                })
                .thenExecute(() -> {
                    helper.assertTrue(foes[0].getMaxHealth() - foes[0].getHealth() > foes[1].getMaxHealth() - foes[1].getHealth(),
                            "der zweite Gegner leidet nicht weniger als der erste");
                    helper.assertTrue(level.getBlockState(r.pos(0, 1, -17)).is(Blocks.STONE_BRICKS), "Bolzen zerschlägt Stein");
                    r.close();
                })
                .thenSucceed();
    }

    // =====================================================================================
    // Katapult
    // =====================================================================================

    /** Das Katapult zielt selbst auf eine Mauer in 26 Blöcken: sie bekommt eine Bresche, ein Gegner daneben Schaden. */
    private static void catapult(GameTestHelper helper) {
        Range r = range(helper, 2, 6, 4, 34);
        ServerLevel level = r.level();
        int wallZ = -26;
        for (int x = -3; x <= 3; x++) {
            for (int y = 0; y <= 5; y++) {
                level.setBlock(r.pos(x, y, wallZ), Blocks.STONE_BRICKS.defaultBlockState(), Block.UPDATE_ALL);
            }
        }
        int before = count(level, r.pos(-3, 0, wallZ), r.pos(3, 5, wallZ), Blocks.STONE_BRICKS);
        SiegeWeaponEntity[] catapult = {null};
        LivingEntity[] foe = {null};
        Vec3 aim = r.at(0.5, 3.0, wallZ + 1.0);
        prepared(helper, r, () -> {
            catapult[0] = weapon(level, SiegeType.CATAPULT, r.at(0.5, 0.0, 0.5), 180.0f);
            foe[0] = target(level, r.at(2.0, 0.0, wallZ + 2.0));
        })
                .thenExecute(() -> {
                    catapult[0].aimAt(aim);
                    helper.assertTrue(catapult[0].fire(level, null), "Katapult wirft nicht");
                })
                .thenWaitUntil(() -> helper.assertTrue(count(level, r.pos(-3, 0, wallZ), r.pos(3, 5, wallZ), Blocks.STONE_BRICKS) <= before - 2,
                        "keine Bresche: " + count(level, r.pos(-3, 0, wallZ), r.pos(3, 5, wallZ), Blocks.STONE_BRICKS) + " von " + before))
                .thenExecute(() -> {
                    helper.assertTrue(foe[0].getHealth() < foe[0].getMaxHealth(), "Gegner an der Mauer bleibt unversehrt");
                    helper.assertTrue(level.getEntitiesOfClass(SiegeProjectile.class, r.area()).isEmpty(), "Steinkugel liegt noch herum");
                    // der Wurf traf die Mauer in der Mitte (nicht daneben oder davor)
                    helper.assertTrue(level.getBlockState(r.pos(0, 3, wallZ)).isAir() || level.getBlockState(r.pos(0, 2, wallZ)).isAir()
                            || level.getBlockState(r.pos(0, 4, wallZ)).isAir(), "Bresche nicht dort, wo gezielt wurde");
                    r.close();
                })
                .thenSucceed();
    }

    // =====================================================================================
    // Tribok
    // =====================================================================================

    /** Der Tribok trifft ein Ziel in 70 Blöcken Entfernung auf wenige Blöcke genau und reißt dort einen Krater. */
    private static void trebuchet(GameTestHelper helper) {
        Range r = range(helper, 3, 6, 6, 84);
        ServerLevel level = r.level();
        SiegeWeaponEntity[] trebuchet = {null};
        Vec3 target = r.at(0.5, 0.0, -69.5);
        Vec3[] last = {null};
        boolean[] seen = {false};
        helper.onEachTick(() -> {
            for (SiegeProjectile p : level.getEntitiesOfClass(SiegeProjectile.class, r.area())) {
                last[0] = p.position();
                seen[0] = true;
            }
        });
        prepared(helper, r, () -> trebuchet[0] = weapon(level, SiegeType.TREBUCHET, r.at(0.5, 0.0, 0.5), 180.0f))
                .thenExecute(() -> {
                    trebuchet[0].aimAt(target);
                    helper.assertTrue(trebuchet[0].fire(level, null), "Tribok wirft nicht");
                    helper.assertValueEqual(trebuchet[0].ammo(), trebuchet[0].capacity() - 2, "der Tribok wirft zwei Steinkugeln auf einmal");
                })
                .thenWaitUntil(() -> helper.assertTrue(seen[0], "kein Brocken fliegt"))
                .thenWaitUntil(() -> helper.assertTrue(level.getEntitiesOfClass(SiegeProjectile.class, r.area()).isEmpty(), "Brocken landet nicht"))
                .thenExecute(() -> {
                    double miss = last[0].subtract(target).horizontalDistance();
                    helper.assertTrue(miss < 4.0, "Tribok verfehlt das Ziel um " + Math.round(miss) + " Blöcke (Einschlag bei " + last[0] + ")");
                    int crater = 0;
                    for (BlockPos pos : BlockPos.betweenClosed(BlockPos.containing(target).offset(-4, -1, -4), BlockPos.containing(target).offset(4, -1, 4))) {
                        if (level.getBlockState(pos).isAir()) {
                            crater++;
                        }
                    }
                    helper.assertTrue(crater >= 3, "kein Krater am Einschlag (" + crater + " Blöcke)");
                    r.close();
                })
                .thenSucceed();
    }

    // =====================================================================================
    // Bedienmannschaft
    // =====================================================================================

    /**
     * Kommandostab: ein freier Soldat läuft zum Katapult und steigt auf; ausgewählt und auf eine Stelle gezeigt, wirft die
     * Mannschaft dorthin. Der Wurf trifft einen Gegner dort, ein eigener Soldat daneben bleibt verschont.
     */
    private static void crew(GameTestHelper helper) {
        Range r = range(helper, 4, 7, 6, 40);
        ServerLevel level = r.level();
        ServerPlayer player = player(helper, r.at(4.5, 0.0, 3.5));
        Vec3 spot = r.at(0.5, 0.0, -29.5);
        SiegeWeaponEntity[] w = {null};
        SoldierEntity[] crew = {null, null};
        LivingEntity[] foe = {null};
        float[] friendHealth = {0.0f};
        prepared(helper, r, () -> {
            w[0] = weapon(level, SiegeType.CATAPULT, r.at(0.5, 0.0, 0.5), 180.0f);
            w[0].setOwner(player.getUUID());
            SoldierEntity gunner = ModEntities.SOLDIERS.get(SoldierType.SWORDSMAN).get().create(level, EntitySpawnReason.COMMAND);
            gunner.snapTo(r.at(-4.5, 0.0, 4.5));
            level.addFreshEntity(gunner);
            gunner.recruitBy(player);
            // er wartet auf seinem Platz (statt dem Spieler hinterherzulaufen), bis der Stab ihn ruft
            gunner.setMode(SoldierEntity.MODE_HOLD, gunner.blockPosition());
            foe[0] = target(level, spot.add(0.8, 0.0, 0.3));
            SoldierEntity friend = ModEntities.SOLDIERS.get(SoldierType.ARCHER).get().create(level, EntitySpawnReason.COMMAND);
            friend.snapTo(spot.add(-1.2, 0.0, -0.4));
            level.addFreshEntity(friend);
            friend.recruitBy(player);
            friend.setNoAi(true);
            crew[0] = gunner;
            crew[1] = friend;
        })
                .thenExecute(() -> {
                    // der Testserver setzt den Spieler gern zurück: vor jedem Befehl an seinen Platz stellen
                    player.snapTo(r.at(4.5, 0.0, 3.5));
                    lookAt(player, w[0].position().add(0.0, 1.0, 0.0));
                    helper.assertTrue(SiegeCrew.baton(level, player), "Kommandostab beachtet das Katapult nicht (Spieler bei "
                            + player.blockPosition() + ", Katapult bei " + w[0].blockPosition() + ")");
                    helper.assertTrue(crew[0].getMode() == SoldierEntity.MODE_HOLD && !crew[0].getNavigation().isDone(),
                            "kein Soldat zum Katapult gerufen");
                })
                .thenWaitUntil(() -> helper.assertTrue(crew[0].getVehicle() == w[0], "Soldat steigt nicht aufs Katapult (Abstand "
                        + String.format(java.util.Locale.ROOT, "%.1f", crew[0].distanceTo(w[0])) + ", Befehl " + crew[0].getMode() + ", Weg "
                        + (crew[0].getNavigation().isDone() ? "keiner" : "unterwegs") + ", Fahrzeug " + crew[0].getVehicle() + ", lebt "
                        + crew[0].isAlive() + ")"))
                .thenExecute(() -> {
                    helper.assertTrue(SiegeCrew.isCrew(crew[0]), "Soldat gilt nicht als Bedienmannschaft");
                    friendHealth[0] = crew[1].getHealth();
                    // auswählen, dann auf die Stelle zeigen
                    player.snapTo(r.at(4.5, 0.0, 3.5));
                    lookAt(player, w[0].position().add(0.0, 1.0, 0.0));
                    helper.assertTrue(SiegeCrew.baton(level, player), "Katapult lässt sich nicht auswählen");
                    lookAt(player, spot);
                    helper.assertTrue(SiegeCrew.baton(level, player), "Befehl an die Mannschaft geht verloren");
                })
                .thenWaitUntil(() -> helper.assertTrue(w[0].ammo() < w[0].capacity(), "Mannschaft wirft nicht"))
                .thenWaitUntil(() -> helper.assertTrue(foe[0].getHealth() < foe[0].getMaxHealth(), "Wurf trifft den Gegner an der Stelle nicht"))
                .thenExecute(() -> {
                    helper.assertTrue(crew[1].getHealth() >= friendHealth[0], "eigener Soldat vom Wurf verletzt");
                    helper.assertTrue(crew[0].getVehicle() == w[0], "Mannschaft steigt ab");
                    crew[0].discard();
                    crew[1].discard();
                    leave(helper, player);
                    r.close();
                })
                .thenSucceed();
    }

    // =====================================================================================
    // Schutz und Zerbrechen
    // =====================================================================================

    /** Siegelblöcke und Banner halten jedem Einschlag stand; ist Blockschaden abgeschaltet, bleibt alles heil. */
    private static void protection(GameTestHelper helper) {
        Range r = range(helper, 5, 5, 3, 10);
        ServerLevel level = r.level();
        Runnable wall = () -> {
            for (int x = -2; x <= 2; x++) {
                for (int y = 0; y <= 2; y++) {
                    level.setBlock(r.pos(x, y, -6), Blocks.STONE_BRICKS.defaultBlockState(), Block.UPDATE_ALL);
                }
            }
            // Banner und Siegelplatte stehen auf Obsidian (den kein Wurf zerschlägt), sonst fielen sie mit dem Boden
            level.setBlock(r.pos(0, -1, -5), Blocks.OBSIDIAN.defaultBlockState(), Block.UPDATE_ALL);
            level.setBlock(r.pos(1, -1, -5), Blocks.OBSIDIAN.defaultBlockState(), Block.UPDATE_ALL);
            BlockState banner = ModBlocks.TOWN_BANNER.get().defaultBlockState();
            level.setBlock(r.pos(0, 0, -5), banner, Block.UPDATE_ALL);
            level.setBlock(r.pos(0, 1, -5), banner.setValue(io.github.sirschrolli.kingdomsatwar.block.TownBannerBlock.HALF, DoubleBlockHalf.UPPER),
                    Block.UPDATE_ALL);
            level.setBlock(r.pos(1, 0, -5), ModBlocks.SEAL_PLATE.get().defaultBlockState(), Block.UPDATE_ALL);
        };
        wall.run();
        int before = count(level, r.pos(-2, 0, -6), r.pos(2, 2, -6), Blocks.STONE_BRICKS);
        SiegeProjectile stone = SiegeProjectile.devCreate(level, SiegeType.TREBUCHET, null, r.at(0.5, 1.0, -4.8));
        stone.devImpact(level);
        int after = count(level, r.pos(-2, 0, -6), r.pos(2, 2, -6), Blocks.STONE_BRICKS);
        helper.assertTrue(after < before, "Einschlag zerschlägt die Mauer nicht (" + after + " von " + before + ")");
        helper.assertTrue(level.getBlockState(r.pos(0, 0, -5)).is(ModBlocks.TOWN_BANNER.get())
                && level.getBlockState(r.pos(0, 1, -5)).is(ModBlocks.TOWN_BANNER.get()), "Stadtbanner zerschlagen");
        helper.assertTrue(level.getBlockState(r.pos(1, 0, -5)).is(ModBlocks.SEAL_PLATE.get()), "Siegelplatte zerschlagen");
        // Blockschaden abgeschaltet
        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, r.area())) {
            item.discard();
        }
        wall.run();
        boolean was = ModServerConfig.SIEGE_BLOCK_DAMAGE.get();
        try {
            ModServerConfig.SIEGE_BLOCK_DAMAGE.set(false);
            SiegeProjectile.devCreate(level, SiegeType.TREBUCHET, null, r.at(0.5, 1.0, -4.8)).devImpact(level);
        } finally {
            ModServerConfig.SIEGE_BLOCK_DAMAGE.set(was);
        }
        helper.assertValueEqual(count(level, r.pos(-2, 0, -6), r.pos(2, 2, -6), Blocks.STONE_BRICKS), before,
                "Blockschaden trotz abgeschalteter Einstellung");
        r.close();
        helper.succeed();
    }

    /** Zerschlagen zerfällt die Waffe in Bretter, Stöcke und Eisen; im Kreativmodus verschwindet sie mit einem Schlag. */
    private static void breakApart(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        SiegeWeaponEntity ballista = weapon(level, SiegeType.BALLISTA, helper.absoluteVec(new Vec3(4.5, 1.0, 4.5)), 0.0f);
        ballista.damage(level, SiegeType.BALLISTA.health() * 0.5f, null);
        helper.assertTrue(ballista.isAlive() && Math.abs(ballista.healthFraction() - 0.5f) < 0.01f, "halber Schaden falsch");
        ballista.damage(level, SiegeType.BALLISTA.health(), null);
        helper.assertTrue(ballista.isRemoved(), "Balliste zerbricht nicht");
        AABB around = new AABB(helper.absolutePos(new BlockPos(4, 1, 4))).inflate(3.0);
        List<ItemEntity> drops = level.getEntitiesOfClass(ItemEntity.class, around);
        helper.assertTrue(drops.stream().anyMatch(i -> i.getItem().is(Items.OAK_PLANKS)), "keine Bretter aus den Trümmern");
        drops.forEach(Entity::discard);
        // im Kreativmodus: ein Schlag
        SiegeWeaponEntity ram = weapon(level, SiegeType.RAM, helper.absoluteVec(new Vec3(4.5, 1.0, 4.5)), 0.0f);
        ServerPlayer player = player(helper, helper.absoluteVec(new Vec3(4.5, 1.0, 1.0)));
        try {
            player.getAbilities().instabuild = true;
            ram.hurtServer(level, level.damageSources().playerAttack(player), 1.0f);
            helper.assertTrue(ram.isRemoved(), "Rammbock widersteht dem Schlag im Kreativmodus");
            helper.assertTrue(level.getEntitiesOfClass(ItemEntity.class, around).isEmpty(), "Kreativ-Abriss lässt Trümmer fallen");
        } finally {
            leave(helper, player);
        }
        helper.succeed();
    }
}
