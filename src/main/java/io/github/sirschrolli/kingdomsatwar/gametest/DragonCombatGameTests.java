package io.github.sirschrolli.kingdomsatwar.gametest;

import com.mojang.authlib.GameProfile;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import io.github.sirschrolli.kingdomsatwar.entity.boss.DragonBossEntity;
import io.github.sirschrolli.kingdomsatwar.entity.dragon.DragonEntity;
import io.github.sirschrolli.kingdomsatwar.entity.dragon.DragonStage;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.netty.channel.embedded.EmbeddedChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * Kampftests der Drachen (0.87): Der eigene Drache verteidigt seinen Herrn am Boden und aus der Luft, hilft ihm beim
 * Angriff, wehrt sich gegen einen Bogenschützen, holt einen Glutgeist aus der Luft und einen Schützen von einer Säule;
 * ein Drachen-Boss findet den Spieler und greift ihn an, ohne irgendwo hängen zu bleiben. Jeder Test schreibt alle
 * zwanzig Ticks den Zustand ins Protokoll ("[Kampf] …").
 */
final class DragonCombatGameTests {
    private static final Identifier ARENA = KingdomsAtWar.id("test_arena");

    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> DEFEND =
            ModGameTests.TEST_FUNCTIONS.register("dragon_fight_defend", () -> h -> defend(h, false));
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> DEFEND_AIR =
            ModGameTests.TEST_FUNCTIONS.register("dragon_fight_defend_air", () -> h -> defend(h, true));
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> ASSIST =
            ModGameTests.TEST_FUNCTIONS.register("dragon_fight_assist", () -> DragonCombatGameTests::assist);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> ARCHER =
            ModGameTests.TEST_FUNCTIONS.register("dragon_fight_archer", () -> DragonCombatGameTests::archer);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> BLAZE =
            ModGameTests.TEST_FUNCTIONS.register("dragon_fight_blaze", () -> DragonCombatGameTests::blaze);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> PILLAR =
            ModGameTests.TEST_FUNCTIONS.register("dragon_fight_pillar", () -> DragonCombatGameTests::pillar);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> BOSS =
            ModGameTests.TEST_FUNCTIONS.register("dragon_fight_boss", () -> h -> boss(h, false));
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> BOSS_WALL =
            ModGameTests.TEST_FUNCTIONS.register("dragon_fight_boss_wall", () -> h -> boss(h, true));
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> FOREST =
            ModGameTests.TEST_FUNCTIONS.register("dragon_fight_forest", () -> DragonCombatGameTests::forest);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> LANDS =
            ModGameTests.TEST_FUNCTIONS.register("dragon_fight_lands", () -> DragonCombatGameTests::lands);

    private DragonCombatGameTests() {}

    static void init() {}

    static void register(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment) {
        for (var test : List.of(DEFEND, DEFEND_AIR, ASSIST, ARCHER, BLAZE, PILLAR, FOREST, LANDS)) {
            add(event, environment, test, 900);
        }
        add(event, environment, BOSS, 1500);
        add(event, environment, BOSS_WALL, 1500);
    }

    private static void add(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment,
            DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> test, int maxTicks) {
        event.registerTest(test.getId(), new FunctionGameTestInstance(test.key(),
                new TestData<>(environment, Level.OVERWORLD, ARENA, maxTicks, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
    }

    // =====================================================================================
    // Hilfen
    // =====================================================================================

    /** Ein Feld weit draußen (Chunks dauerhaft geladen, Steinboden, viel Luft darüber). */
    record Field(ServerLevel level, BlockPos origin, int half, List<long[]> chunks) {
        Vec3 at(double dx, double dy, double dz) {
            return new Vec3(this.origin.getX() + dx, this.origin.getY() + dy, this.origin.getZ() + dz);
        }

        boolean loaded() {
            for (long[] c : this.chunks) {
                if (!this.level.areEntitiesLoaded(ChunkPos.pack((int) c[0], (int) c[1]))) {
                    return false;
                }
            }
            return true;
        }

        void close() {
            AABB area = new AABB(this.origin).inflate(this.half + 16, 60, this.half + 16);
            for (Entity e : this.level.getEntitiesOfClass(Entity.class, area, e -> !(e instanceof Player))) {
                e.discard();
            }
            for (long[] c : this.chunks) {
                this.level.setChunkForced((int) c[0], (int) c[1], false);
            }
        }
    }

    static Field field(GameTestHelper helper, int slot, int half) {
        ServerLevel level = helper.getLevel();
        BlockPos base = helper.absolutePos(BlockPos.ZERO);
        BlockPos origin = new BlockPos(base.getX() + 26000 + slot * 360, 120, base.getZ() - 26000);
        List<long[]> chunks = new ArrayList<>();
        for (int cx = (origin.getX() - half - 8) >> 4; cx <= (origin.getX() + half + 8) >> 4; cx++) {
            for (int cz = (origin.getZ() - half - 8) >> 4; cz <= (origin.getZ() + half + 8) >> 4; cz++) {
                level.getChunk(cx, cz);
                level.setChunkForced(cx, cz, true);
                chunks.add(new long[] {cx, cz});
            }
        }
        BlockState stone = Blocks.STONE.defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();
        for (int x = -half; x <= half; x++) {
            for (int z = -half; z <= half; z++) {
                level.setBlock(origin.offset(x, -1, z), stone, Block.UPDATE_CLIENTS);
                for (int y = 0; y < 40; y++) {
                    level.setBlock(origin.offset(x, y, z), air, Block.UPDATE_CLIENTS);
                }
            }
        }
        return new Field(level, origin, half, chunks);
    }

    /** Ein echter Spieler im Überlebensmodus (die Schein-Spieler der Tests sind immer im Kreativmodus). */
    static ServerPlayer survivor(GameTestHelper helper, Vec3 at, double health) {
        ServerLevel level = helper.getLevel();
        CommonListenerCookie cookie = CommonListenerCookie.createInitial(new GameProfile(UUID.randomUUID(), "test-owner"), false);
        ServerPlayer player = new ServerPlayer(level.getServer(), level, cookie.gameProfile(), cookie.clientInformation()) {
            @Override
            public GameType gameMode() {
                return GameType.SURVIVAL;
            }
        };
        Connection connection = new Connection(PacketFlow.SERVERBOUND);
        new EmbeddedChannel(connection);
        level.getServer().getPlayerList().placeNewPlayer(connection, player, cookie);
        // sonst gilt der Spieler als noch ladend – unverwundbar, kein Gegner sieht ihn als Ziel
        player.connection.markClientLoaded();
        // die Fähigkeiten stehen sonst noch auf Kreativ (unverwundbar, fliegt) – dann sieht ihn kein Gegner als Ziel
        player.getAbilities().invulnerable = false;
        player.getAbilities().instabuild = false;
        player.getAbilities().mayfly = false;
        player.getAbilities().flying = false;
        player.onUpdateAbilities();
        player.snapTo(at.x, at.y, at.z, 180.0f, 0.0f);
        player.getAttribute(Attributes.MAX_HEALTH).setBaseValue(health);
        player.setHealth((float) health);
        return player;
    }

    static void leave(GameTestHelper helper, ServerPlayer player) {
        helper.getLevel().getServer().getPlayerList().remove(player);
    }

    /** Ein ausgewachsener Drache des Spielers an {@code at}, der ihm folgt. */
    static DragonEntity dragon(ServerLevel level, DragonType type, Vec3 at, Player owner, boolean flying) {
        DragonEntity dragon = ModEntities.DRAGONS.get(type).get().create(level, EntitySpawnReason.COMMAND);
        dragon.snapTo(at.x, at.y, at.z, 0.0f, 0.0f);
        dragon.applyStage(DragonStage.ADULT, false);
        dragon.setHealth(dragon.getMaxHealth());
        level.addFreshEntity(dragon);
        dragon.bondWith(owner);
        if (flying) {
            dragon.setFlying(true);
        }
        return dragon;
    }

    static <T extends Mob> T foe(ServerLevel level, net.minecraft.world.entity.EntityType<T> type, Vec3 at, @org.jspecify.annotations.Nullable LivingEntity target) {
        T mob = type.create(level, EntitySpawnReason.COMMAND);
        mob.snapTo(at.x, at.y, at.z, 0.0f, 0.0f);
        mob.setPersistenceRequired();
        // ein Helm, damit Untote nicht in der Sonne verbrennen (sonst erledigt die Sonne, was der Drache tun soll)
        mob.setItemSlot(net.minecraft.world.entity.EquipmentSlot.HEAD, new net.minecraft.world.item.ItemStack(
                net.minecraft.world.item.Items.LEATHER_HELMET));
        level.addFreshEntity(mob);
        if (target != null) {
            mob.setTarget(target);
        }
        return mob;
    }

    private static String rel(Field f, Entity e) {
        return String.format(Locale.ROOT, "(%.1f %.1f %.1f)", e.getX() - f.origin().getX(), e.getY() - f.origin().getY(),
                e.getZ() - f.origin().getZ());
    }

    private static String goals(Mob mob) {
        List<String> running = new ArrayList<>();
        for (WrappedGoal goal : mob.goalSelector.getAvailableGoals()) {
            if (goal.isRunning()) {
                running.add(goal.getGoal().getClass().getSimpleName());
            }
        }
        return String.join(",", running);
    }

    /** Eine Zeile Zustand des Drachen ins Protokoll. */
    static void log(String test, Field f, int t, DragonEntity dragon, @org.jspecify.annotations.Nullable LivingEntity foe) {
        LivingEntity target = dragon.getTarget();
        KingdomsAtWar.LOGGER.info(String.format(Locale.ROOT,
                "[Kampf] %s t=%d drache=%s fliegt=%b boden=%b ziel=%s abstand=%.1f gegner=%s leben=%.0f ziele=[%s] weg=%s odem=%d",
                test, t, rel(f, dragon), dragon.isFlying(), dragon.onGround(),
                target == null ? "-" : target.getType().toShortString(), target == null ? -1.0 : dragon.distanceTo(target),
                foe == null ? "-" : rel(f, foe), foe == null ? -1.0f : foe.getHealth(), goals(dragon),
                dragon.getNavigation().isDone() ? "fertig" : "unterwegs", dragon.getBreathTicks()));
    }

    /**
     * Wartet, bis {@code done} gilt (höchstens {@code limit} Ticks), schreibt dabei den Zustand ins Protokoll und räumt
     * am Ende auf.
     */
    private static void run(GameTestHelper helper, String name, Field f, Runnable setup, int limit, java.util.function.IntConsumer each,
            java.util.function.BooleanSupplier done, java.util.function.Supplier<String> why, Runnable cleanup) {
        int[] t = {0};
        String[] failure = {null};
        helper.startSequence()
                .thenWaitUntil(() -> helper.assertTrue(ModGameTests.awaitLoading(f.loaded()), "Chunks des Felds laden nicht"))
                .thenExecute(setup)
                .thenWaitUntil(() -> {
                    if (failure[0] != null) {
                        helper.fail(failure[0]);
                    }
                    t[0]++;
                    each.accept(t[0]);
                    if (done.getAsBoolean()) {
                        return;
                    }
                    if (t[0] >= limit) {
                        failure[0] = name + ": " + why.get();
                        cleanup.run();
                        helper.fail(failure[0]);
                    }
                    helper.fail(name + " läuft noch");
                })
                .thenExecute(cleanup)
                .thenSucceed();
    }

    // =====================================================================================
    // Der eigene Drache
    // =====================================================================================

    /** Ein Wiedergänger greift den Herrn an – der Drache (am Boden oder in der Luft) stellt und erledigt ihn. */
    private static void defend(GameTestHelper helper, boolean air) {
        String name = air ? "verteidigt_luft" : "verteidigt";
        Field f = field(helper, air ? 1 : 0, 40);
        ServerLevel level = f.level();
        ServerPlayer owner = survivor(helper, f.at(0.5, 0.0, 0.5), 400.0);
        DragonEntity[] dragon = {null};
        Mob[] zombie = {null};
        run(helper, name, f, () -> {
            owner.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 20000, 3, false, false));
            dragon[0] = dragon(level, air ? DragonType.ICE : DragonType.FIRE, f.at(9.5, air ? 11.0 : 0.0, 9.5), owner, air);
            zombie[0] = foe(level, ModEntities.ZOMBIE.get(), f.at(0.5, 0.0, -13.5), owner);
        }, 860, t -> {
            if (t % 20 == 0) {
                log(name, f, t, dragon[0], zombie[0]);
            }
            // der Wiedergänger schlägt zu, sobald er nah ist (der Herr steht still)
            if (zombie[0].isAlive() && zombie[0].distanceTo(owner) < 2.2 && t % 20 == 0) {
                owner.hurtServer(level, level.damageSources().mobAttack(zombie[0]), 2.0f);
            }
        }, () -> !zombie[0].isAlive() && (air || owner.getHealth() >= 400.0f - 0.01f), () -> "der Drache erledigt den Angreifer nicht "
                + "rechtzeitig (Leben " + zombie[0].getHealth() + ", Herr " + owner.getHealth() + ")", () -> {
            leave(helper, owner);
            f.close();
        });
    }

    /** Zwischen Drache und Angreifer steht ein Wäldchen – der Drache findet trotzdem hin (zur Not über die Bäume). */
    private static void forest(GameTestHelper helper) {
        String name = "wald";
        Field f = field(helper, 7, 40);
        ServerLevel level = f.level();
        ServerPlayer owner = survivor(helper, f.at(0.5, 0.0, -12.5), 400.0);
        DragonEntity[] dragon = {null};
        Mob[] zombie = {null};
        run(helper, name, f, () -> {
            // drei Reihen Bäume (Stamm fünf hoch, Krone aus Laub) quer über das Feld
            for (int row = 0; row < 3; row++) {
                for (int x = -20; x <= 20; x += 3) {
                    int ox = x + row, oz = -2 + row * 3;
                    for (int y = 0; y < 5; y++) {
                        level.setBlock(BlockPos.containing(f.at(ox, y, oz)), Blocks.OAK_LOG.defaultBlockState(), Block.UPDATE_CLIENTS);
                    }
                    for (int dx = -1; dx <= 1; dx++) {
                        for (int dz = -1; dz <= 1; dz++) {
                            for (int y = 3; y < 7; y++) {
                                BlockPos p = BlockPos.containing(f.at(ox + dx, y, oz + dz));
                                if (level.getBlockState(p).isAir()) {
                                    level.setBlock(p, Blocks.OAK_LEAVES.defaultBlockState()
                                            .setValue(net.minecraft.world.level.block.LeavesBlock.PERSISTENT, true), Block.UPDATE_CLIENTS);
                                }
                            }
                        }
                    }
                }
            }
            owner.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 20000, 3, false, false));
            dragon[0] = dragon(level, DragonType.FIRE, f.at(0.5, 0.0, 14.5), owner, false);
            dragon[0].setMode(DragonEntity.MODE_SIT);
            zombie[0] = foe(level, ModEntities.ZOMBIE.get(), f.at(3.5, 0.0, -16.5), owner);
        }, 860, t -> {
            if (t == 2) {
                dragon[0].setMode(DragonEntity.MODE_FOLLOW);
                dragon[0].setTarget(zombie[0]);
            }
            if (t % 20 == 0) {
                log(name, f, t, dragon[0], zombie[0]);
            }
        }, () -> !zombie[0].isAlive(), () -> "der Drache kommt nicht durch den Wald (Leben " + zombie[0].getHealth() + ")", () -> {
            leave(helper, owner);
            f.close();
        });
    }

    /** Ein fliegender Drache ohne Ziel schwebt nicht neben seinem Herrn in der Luft, sondern landet. */
    private static void lands(GameTestHelper helper) {
        String name = "landet";
        Field f = field(helper, 8, 40);
        ServerLevel level = f.level();
        ServerPlayer owner = survivor(helper, f.at(0.5, 0.0, 0.5), 400.0);
        DragonEntity[] dragon = {null};
        run(helper, name, f, () -> dragon[0] = dragon(level, DragonType.NATURE, f.at(6.5, 7.0, 6.5), owner, true), 400, t -> {
            if (t % 20 == 0) {
                log(name, f, t, dragon[0], null);
            }
        }, () -> !dragon[0].isFlying() && dragon[0].onGround() && dragon[0].distanceTo(owner) > 5.5, () -> "der Drache landet nicht "
                + "(Höhe " + String.format(Locale.ROOT, "%.2f", dragon[0].getY() - f.origin().getY()) + ") oder zu nah am Herrn ("
                + String.format(Locale.ROOT, "%.1f", dragon[0].distanceTo(owner)) + ")", () -> {
            leave(helper, owner);
            f.close();
        });
    }

    /** Der Herr schlägt einen Knochenmann – der Drache greift ihn mit an. */
    private static void assist(GameTestHelper helper) {
        String name = "hilft";
        Field f = field(helper, 2, 40);
        ServerLevel level = f.level();
        ServerPlayer owner = survivor(helper, f.at(0.5, 0.0, 0.5), 400.0);
        DragonEntity[] dragon = {null};
        Mob[] skeleton = {null};
        run(helper, name, f, () -> {
            owner.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 20000, 3, false, false));
            dragon[0] = dragon(level, DragonType.NATURE, f.at(-8.5, 0.0, 6.5), owner, false);
            skeleton[0] = foe(level, ModEntities.SKELETON.get(), f.at(3.5, 0.0, -14.5), null);
            skeleton[0].setNoAi(true);
            skeleton[0].hurtServer(level, level.damageSources().playerAttack(owner), 1.0f);
            owner.setLastHurtMob(skeleton[0]);
        }, 860, t -> {
            if (t % 20 == 0) {
                log(name, f, t, dragon[0], skeleton[0]);
            }
        }, () -> !skeleton[0].isAlive(), () -> "der Drache hilft nicht (Leben " + skeleton[0].getHealth() + ")", () -> {
            leave(helper, owner);
            f.close();
        });
    }

    /** Ein Knochenmann schießt aus der Ferne auf den Drachen – der wehrt sich. */
    private static void archer(GameTestHelper helper) {
        String name = "bogenschuetze";
        Field f = field(helper, 3, 40);
        ServerLevel level = f.level();
        ServerPlayer owner = survivor(helper, f.at(0.5, 0.0, 12.5), 400.0);
        DragonEntity[] dragon = {null};
        Mob[] skeleton = {null};
        run(helper, name, f, () -> {
            owner.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 20000, 3, false, false));
            dragon[0] = dragon(level, DragonType.LIGHTNING, f.at(0.5, 0.0, 6.5), owner, false);
            skeleton[0] = foe(level, ModEntities.SKELETON.get(), f.at(0.5, 0.0, -14.5), dragon[0]);
            skeleton[0].setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new net.minecraft.world.item.ItemStack(
                    net.minecraft.world.item.Items.BOW));
        }, 860, t -> {
            if (t % 20 == 0) {
                log(name, f, t, dragon[0], skeleton[0]);
            }
        }, () -> !skeleton[0].isAlive(), () -> "der Drache wehrt sich nicht (Leben " + skeleton[0].getHealth() + ", Drache "
                + dragon[0].getHealth() + "/" + dragon[0].getMaxHealth() + ")", () -> {
            leave(helper, owner);
            f.close();
        });
    }

    /** Ein Glutgeist schwebt über dem Herrn und greift ihn an – der Eisdrache holt ihn herunter. */
    private static void blaze(GameTestHelper helper) {
        String name = "glutgeist";
        Field f = field(helper, 4, 40);
        ServerLevel level = f.level();
        ServerPlayer owner = survivor(helper, f.at(0.5, 0.0, 0.5), 400.0);
        DragonEntity[] dragon = {null};
        Mob[] blaze = {null};
        run(helper, name, f, () -> {
            owner.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 20000, 3, false, false));
            owner.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 20000, 0, false, false));
            dragon[0] = dragon(level, DragonType.ICE, f.at(8.5, 0.0, 8.5), owner, false);
            blaze[0] = foe(level, ModEntities.BLAZE.get(), f.at(0.5, 7.0, -9.5), owner);
            owner.hurtServer(level, level.damageSources().mobAttack(blaze[0]), 1.0f);
        }, 860, t -> {
            if (t % 20 == 0) {
                log(name, f, t, dragon[0], blaze[0]);
            }
        }, () -> !blaze[0].isAlive(), () -> "der Drache holt den Glutgeist nicht herunter (Leben " + blaze[0].getHealth() + ")", () -> {
            leave(helper, owner);
            f.close();
        });
    }

    /** Ein Schütze auf einer sechs Blöcke hohen Säule beschießt den Herrn – der Erddrache holt ihn herunter. */
    private static void pillar(GameTestHelper helper) {
        String name = "saeule";
        Field f = field(helper, 5, 40);
        ServerLevel level = f.level();
        ServerPlayer owner = survivor(helper, f.at(0.5, 0.0, 0.5), 400.0);
        DragonEntity[] dragon = {null};
        Mob[] skeleton = {null};
        run(helper, name, f, () -> {
            for (int y = 0; y < 6; y++) {
                level.setBlock(BlockPos.containing(f.at(0.5, y, -12.5)), Blocks.STONE.defaultBlockState(), Block.UPDATE_CLIENTS);
            }
            owner.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 20000, 3, false, false));
            dragon[0] = dragon(level, DragonType.EARTH, f.at(8.5, 0.0, 8.5), owner, false);
            skeleton[0] = foe(level, ModEntities.SKELETON.get(), f.at(0.5, 6.0, -12.5), owner);
            skeleton[0].setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new net.minecraft.world.item.ItemStack(
                    net.minecraft.world.item.Items.BOW));
            owner.hurtServer(level, level.damageSources().mobAttack(skeleton[0]), 1.0f);
        }, 860, t -> {
            if (t % 20 == 0) {
                log(name, f, t, dragon[0], skeleton[0]);
            }
        }, () -> !skeleton[0].isAlive(), () -> "der Drache holt den Schützen nicht von der Säule (Leben " + skeleton[0].getHealth() + ")",
                () -> {
                    leave(helper, owner);
                    f.close();
                });
    }

    // =====================================================================================
    // Drachen-Boss
    // =====================================================================================

    /**
     * Pyrrhon erwacht und muss den Spieler (viel Leben, Widerstand) in 50 Sekunden mehrmals treffen; er darf dabei
     * nirgends länger als fünf Sekunden auf der Stelle stehen, während der Spieler außer Reichweite ist.
     */
    private static void boss(GameTestHelper helper, boolean wall) {
        String name = wall ? "boss_mauer" : "boss";
        Field f = field(helper, wall ? 9 : 6, 44);
        ServerLevel level = f.level();
        ServerPlayer player = survivor(helper, f.at(0.5, 0.0, 22.5), 1000.0);
        DragonBossEntity[] boss = {null};
        float[] lastHealth = {1000.0f};
        int[] hits = {0};
        Vec3[] lastPos = {null};
        int[] still = {0};
        int[] worstStill = {0};
        run(helper, name, f, () -> {
            if (wall) {
                // eine fünf Blöcke hohe Mauer quer zwischen Boss und Spieler
                for (int x = -18; x <= 18; x++) {
                    for (int y = 0; y < 5; y++) {
                        level.setBlock(BlockPos.containing(f.at(x, y, 13.5)), Blocks.STONE_BRICKS.defaultBlockState(), Block.UPDATE_CLIENTS);
                    }
                }
            }
            player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 20000, 2, false, false));
            var type = ModEntities.bossType(DragonType.FIRE);
            boss[0] = type.create(level, EntitySpawnReason.COMMAND);
            boss[0].snapTo(f.at(0.5, 0.0, 0.5).x, f.origin().getY(), f.at(0.5, 0.0, 0.5).z, 0.0f, 0.0f);
            boss[0].setHome(f.origin());
            level.addFreshEntity(boss[0]);
            boss[0].devPose(DragonBossEntity.MoveMode.GROUND);
        }, 1460, t -> {
            DragonBossEntity b = boss[0];
            if (player.getHealth() < lastHealth[0] - 0.5f) {
                hits[0]++;
            }
            lastHealth[0] = player.getHealth();
            if (t % 20 == 0) {
                var attack = b.currentAttack();
                KingdomsAtWar.LOGGER.info(String.format(Locale.ROOT,
                        "[Kampf] " + name + " t=%d pos=%s zustand=%s modus=%s angriff=%s ziel=%s abstand=%.1f treffer=%d spieler=%.0f",
                        t, rel(f, b), b.getBossState(), b.getMoveMode(), attack == null ? "-" : attack.name(),
                        b.getTarget() == null ? "-" : b.getTarget().getType().toShortString(), b.distanceTo(player), hits[0],
                        player.getHealth()));
                if (b.getBossState() == DragonBossEntity.State.FIGHT && attack == null) {
                    Vec3 now = b.position();
                    double reach = 7.0 + b.getBbWidth() * 0.5;
                    if (lastPos[0] != null && now.distanceTo(lastPos[0]) < 0.3 && b.distanceTo(player) > reach) {
                        still[0] += 20;
                        worstStill[0] = Math.max(worstStill[0], still[0]);
                    } else {
                        still[0] = 0;
                    }
                    lastPos[0] = now;
                }
            }
        }, () -> hits[0] >= 4, () -> "der Boss trifft den Spieler nur " + hits[0] + "-mal (längster Stillstand " + worstStill[0] + " Ticks)",
                () -> {
                    leave(helper, player);
                    f.close();
                });
    }
}
