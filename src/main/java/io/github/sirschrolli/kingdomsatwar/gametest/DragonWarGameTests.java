package io.github.sirschrolli.kingdomsatwar.gametest;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import io.github.sirschrolli.kingdomsatwar.entity.dragon.DragonEntity;
import io.github.sirschrolli.kingdomsatwar.entity.dragon.DragonStage;
import io.github.sirschrolli.kingdomsatwar.entity.dragon.DragonWar;
import io.github.sirschrolli.kingdomsatwar.entity.realm.RealmWarriorEntity;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierEntity;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierType;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.ModItems;
import io.github.sirschrolli.kingdomsatwar.war.Realm;
import io.github.sirschrolli.kingdomsatwar.war.RealmState;
import io.github.sirschrolli.kingdomsatwar.war.WarriorRole;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestSequence;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
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
 * Tests der Drachen im Krieg: Angriffsbefehl mit Sturzflug und Odem, Luftangriff auf mehrere Gegner, Rückzug bei schweren
 * Wunden, wer Feind ist (und dass die Schützen der Reiche Drachen angreifen), die Verteidigung der Siedlung gegen einen
 * Heerhaufen und das Drachenhorn ohne Drachen.
 */
final class DragonWarGameTests {
    private static final Identifier ARENA = KingdomsAtWar.id("test_arena");

    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> ATTACK =
            ModGameTests.TEST_FUNCTIONS.register("dragonwar_attack", () -> DragonWarGameTests::attack);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> STRIKE =
            ModGameTests.TEST_FUNCTIONS.register("dragonwar_strike", () -> DragonWarGameTests::strike);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> RETREAT =
            ModGameTests.TEST_FUNCTIONS.register("dragonwar_retreat", () -> DragonWarGameTests::retreat);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> ENEMIES =
            ModGameTests.TEST_FUNCTIONS.register("dragonwar_enemies", () -> DragonWarGameTests::enemies);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> DEFEND =
            ModGameTests.TEST_FUNCTIONS.register("dragonwar_defend", () -> DragonWarGameTests::defend);

    private DragonWarGameTests() {}

    static void init() {}

    static void register(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment) {
        add(event, environment, ATTACK, 1800);
        add(event, environment, STRIKE, 2400);
        add(event, environment, RETREAT, 60);
        add(event, environment, ENEMIES, 60);
        add(event, environment, DEFEND, 60);
    }

    private static void add(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment,
            DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> test, int maxTicks) {
        event.registerTest(test.getId(), new FunctionGameTestInstance(test.key(),
                new TestData<>(environment, Level.OVERWORLD, ARENA, maxTicks, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
    }

    // =====================================================================================
    // Hilfen
    // =====================================================================================

    private static void floor(GameTestHelper helper) {
        for (int x = 0; x < 9; x++) {
            for (int z = 0; z < 9; z++) {
                helper.setBlock(x, 0, z, Blocks.STONE);
            }
        }
    }

    private static ServerPlayer player(GameTestHelper helper, Vec3 at) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.connection.markClientLoaded();
        player.getAbilities().instabuild = false;
        player.getAbilities().invulnerable = true;
        player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
        player.getInventory().clearContent();
        player.snapTo(at.x, at.y, at.z, 0.0f, 0.0f);
        return player;
    }

    private static void leave(GameTestHelper helper, ServerPlayer player) {
        helper.getLevel().getServer().getPlayerList().remove(player);
    }

    /** Ein ausgewachsener Drache des Spielers an {@code at}. */
    private static DragonEntity dragon(ServerLevel level, DragonType type, Vec3 at, Player owner) {
        DragonEntity dragon = ModEntities.DRAGONS.get(type).get().create(level, EntitySpawnReason.COMMAND);
        dragon.snapTo(at.x, at.y, at.z, 0.0f, 0.0f);
        dragon.applyStage(DragonStage.ADULT, false);
        dragon.setHealth(dragon.getMaxHealth());
        level.addFreshEntity(dragon);
        dragon.bondWith(owner);
        return dragon;
    }

    /** Eine Mumie ohne eigenen Willen mit {@code health} Leben. */
    private static Mob dummy(ServerLevel level, Vec3 at, double health) {
        var mob = ModEntities.HUSK.get().create(level, EntitySpawnReason.COMMAND);
        mob.snapTo(at.x, at.y, at.z, 0.0f, 0.0f);
        mob.setNoAi(true);
        mob.getAttribute(Attributes.MAX_HEALTH).setBaseValue(health);
        mob.setHealth((float) health);
        mob.setPersistenceRequired();
        level.addFreshEntity(mob);
        return mob;
    }

    /** Ein Feld weit draußen (Chunks dauerhaft geladen, Steinboden, viel Luft darüber). */
    private record Field(ServerLevel level, BlockPos origin, int half, List<long[]> chunks) {
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
            AABB area = new AABB(this.origin).inflate(this.half + 12, 40, this.half + 12);
            for (Entity e : this.level.getEntitiesOfClass(Entity.class, area, e -> !(e instanceof Player))) {
                e.discard();
            }
            for (long[] c : this.chunks) {
                this.level.setChunkForced((int) c[0], (int) c[1], false);
            }
        }
    }

    private static Field field(GameTestHelper helper, int slot, int half) {
        ServerLevel level = helper.getLevel();
        BlockPos base = helper.absolutePos(BlockPos.ZERO);
        BlockPos origin = new BlockPos(base.getX() + 21000 + slot * 320, 120, base.getZ() + 21000);
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
                for (int y = 0; y < 30; y++) {
                    level.setBlock(origin.offset(x, y, z), air, Block.UPDATE_CLIENTS);
                }
            }
        }
        return new Field(level, origin, half, chunks);
    }

    private static GameTestSequence prepared(GameTestHelper helper, Field f, Runnable setup) {
        return helper.startSequence()
                .thenWaitUntil(() -> helper.assertTrue(ModGameTests.awaitLoading(f.loaded()), "Chunks des Felds laden nicht"))
                .thenExecute(setup)
                .thenIdle(2);
    }

    // =====================================================================================
    // Angriff und Luftangriff
    // =====================================================================================

    /** Auf Befehl steigt der Drache auf, stößt auf das Ziel herab und trifft es mit seinem Odem (es brennt). */
    private static void attack(GameTestHelper helper) {
        Field f = field(helper, 0, 30);
        ServerLevel level = f.level();
        ServerPlayer player = player(helper, f.at(-20.5, 0.0, 20.5));
        DragonEntity[] dragon = {null};
        Mob[] foe = {null};
        boolean[] flew = {false};
        prepared(helper, f, () -> {
            dragon[0] = dragon(level, DragonType.FIRE, f.at(0.5, 0.0, 14.5), player);
            foe[0] = dummy(level, f.at(0.5, 0.0, -10.5), 400.0);
            DragonWar.attack(level, null, List.of(dragon[0]), foe[0]);
            helper.assertTrue(dragon[0].onWarOrder() && dragon[0].getTarget() == foe[0], "der Drache übernimmt den Angriffsbefehl nicht");
        })
                .thenWaitUntil(() -> {
                    flew[0] |= dragon[0].isFlying() && dragon[0].getY() > f.origin().getY() + 6.0;
                    helper.assertTrue(foe[0].getHealth() < 400.0f - 8.0f, "der Odem trifft nicht (Leben " + foe[0].getHealth()
                            + ", Abstand " + String.format(java.util.Locale.ROOT, "%.1f", dragon[0].distanceTo(foe[0])) + ", fliegt "
                            + dragon[0].isFlying() + ", Höhe " + String.format(java.util.Locale.ROOT, "%.1f", dragon[0].getY() - f.origin().getY())
                            + ")");
                })
                .thenExecute(() -> {
                    helper.assertTrue(flew[0], "der Drache greift nicht aus der Luft an");
                    helper.assertTrue(foe[0].getRemainingFireTicks() > 0 || foe[0].getHealth() < 400.0f, "das Ziel brennt nicht");
                    helper.assertTrue(player.getHealth() >= player.getMaxHealth(), "der Odem trifft den Herrn");
                    leave(helper, player);
                    f.close();
                })
                .thenSucceed();
    }

    /** Luftangriff: Der Drache kreist über dem Ort und nimmt sich nacheinander alle drei Gegner dort vor. */
    private static void strike(GameTestHelper helper) {
        Field f = field(helper, 1, 30);
        ServerLevel level = f.level();
        ServerPlayer player = player(helper, f.at(-20.5, 0.0, 20.5));
        DragonEntity[] dragon = {null};
        List<Mob> foes = new ArrayList<>();
        prepared(helper, f, () -> {
            dragon[0] = dragon(level, DragonType.LIGHTNING, f.at(0.5, 0.0, 16.5), player);
            foes.add(dummy(level, f.at(-5.5, 0.0, -8.5), 300.0));
            foes.add(dummy(level, f.at(6.5, 0.0, -11.5), 300.0));
            foes.add(dummy(level, f.at(0.5, 0.0, -17.5), 300.0));
            DragonWar.strike(level, null, List.of(dragon[0]), BlockPos.containing(f.at(0.5, 0.0, -11.5)));
            helper.assertTrue(dragon[0].onWarOrder() && dragon[0].strikePos() != null, "der Drache übernimmt den Luftangriff nicht");
        })
                .thenWaitUntil(() -> {
                    int hurt = (int) foes.stream().filter(m -> m.getHealth() < 300.0f).count();
                    helper.assertTrue(hurt == foes.size(), "nur " + hurt + " von " + foes.size() + " Gegnern getroffen");
                })
                .thenExecute(() -> {
                    leave(helper, player);
                    f.close();
                })
                .thenSucceed();
    }

    // =====================================================================================
    // Rückzug, Feinde, Verteidigung
    // =====================================================================================

    /** Schwer verwundet bricht der Drache den Angriff ab; das Horn ohne Drachen in der Nähe bewirkt nichts. */
    private static void retreat(GameTestHelper helper) {
        floor(helper);
        ServerLevel level = helper.getLevel();
        ServerPlayer player = player(helper, helper.absoluteVec(new Vec3(1.5, 1, 1.5)));
        ServerPlayer lonely = player(helper, helper.absoluteVec(new Vec3(7.5, 1, 7.5)));
        DragonEntity dragon = dragon(level, DragonType.ICE, helper.absoluteVec(new Vec3(4.5, 1, 4.5)), player);
        Mob foe = dummy(level, helper.absoluteVec(new Vec3(4.5, 1, 1.5)), 100.0);
        helper.startSequence()
                .thenExecute(() -> {
                    helper.assertTrue(DragonWar.horn(level, lonely) == DragonWar.Order.NONE, "das Horn ruft fremde Drachen");
                    dragon.orderAttack(foe);
                    dragon.setHealth(dragon.getMaxHealth() * 0.2f);
                })
                .thenWaitUntil(() -> helper.assertTrue(!dragon.onWarOrder() && dragon.getTarget() == null,
                        "der verwundete Drache greift weiter an"))
                .thenExecute(() -> {
                    dragon.discard();
                    foe.discard();
                    leave(helper, player);
                    leave(helper, lonely);
                })
                .thenSucceed();
    }

    /**
     * Feinde sind Monster und die Krieger von Reichen im Krieg – nicht die eigenen Soldaten, nicht Spieler, nicht die
     * Krieger eines Reichs im Frieden; die Schützen eines Reichs im Krieg halten den Drachen für einen Eindringling.
     */
    private static void enemies(GameTestHelper helper) {
        floor(helper);
        ServerLevel level = helper.getLevel();
        ServerPlayer player = player(helper, helper.absoluteVec(new Vec3(1.5, 1, 1.5)));
        RealmState state = RealmState.of(level);
        try {
            DragonEntity dragon = dragon(level, DragonType.EARTH, helper.absoluteVec(new Vec3(4.5, 1, 4.5)), player);
            Mob husk = dummy(level, helper.absoluteVec(new Vec3(1.5, 1, 7.5)), 20.0);
            SoldierEntity soldier = ModEntities.SOLDIERS.get(SoldierType.ARCHER).get().create(level, EntitySpawnReason.COMMAND);
            soldier.snapTo(helper.absoluteVec(new Vec3(7.5, 1, 1.5)));
            level.addFreshEntity(soldier);
            soldier.setOwner(player);
            RealmWarriorEntity archer = ModEntities.REALM_WARRIORS.get(WarriorRole.ARCHER).get().create(level, EntitySpawnReason.COMMAND);
            archer.setRealm(Realm.RABENSTEIN);
            archer.snapTo(helper.absoluteVec(new Vec3(7.5, 1, 7.5)));
            archer.setPost(archer.blockPosition(), archer.blockPosition());
            level.addFreshEntity(archer);
            helper.assertTrue(DragonWar.isEnemy(dragon, husk), "Monster ist kein Feind");
            helper.assertTrue(!DragonWar.isEnemy(dragon, soldier), "eigener Soldat gilt als Feind");
            helper.assertTrue(!DragonWar.isEnemy(dragon, player), "der Herr gilt als Feind");
            helper.assertTrue(!DragonWar.isEnemy(dragon, archer), "Krieger eines Reichs im Frieden gilt als Feind");
            state.declareWar(Realm.RABENSTEIN, player.getUUID());
            helper.assertTrue(DragonWar.isEnemy(dragon, archer), "Krieger eines Reichs im Krieg ist kein Feind");
            helper.assertTrue(archer.isIntruder(dragon), "die Schützen des Reichs greifen den Drachen nicht an");
            dragon.setFlying(true);
            helper.assertTrue(archer.isIntruder(dragon), "die Schützen greifen den fliegenden Drachen nicht an");
            for (Entity e : List.of(dragon, husk, soldier, archer)) {
                e.discard();
            }
            helper.assertTrue(new ItemStack(ModItems.DRAGON_HORN.get()).getMaxStackSize() == 1, "Drachenhorn stapelt sich");
            helper.succeed();
        } finally {
            state.makePeace(Realm.RABENSTEIN, player.getUUID());
            leave(helper, player);
        }
    }

    /** Kommt ein Heerhaufen, steigen die Drachen in der Nähe auf und verteidigen das Banner. */
    private static void defend(GameTestHelper helper) {
        floor(helper);
        ServerLevel level = helper.getLevel();
        ServerPlayer player = player(helper, helper.absoluteVec(new Vec3(1.5, 1, 1.5)));
        try {
            DragonEntity dragon = dragon(level, DragonType.NATURE, helper.absoluteVec(new Vec3(4.5, 1, 4.5)), player);
            dragon.setMode(DragonEntity.MODE_SIT);
            BlockPos banner = helper.absolutePos(new BlockPos(7, 1, 7));
            helper.assertValueEqual(DragonWar.defend(level, player.getUUID(), banner, 1200), 1, "Drachen zur Verteidigung");
            helper.assertTrue(dragon.onWarOrder() && banner.equals(dragon.strikePos()), "der Drache verteidigt das Banner nicht");
            helper.assertTrue(dragon.getMode() == DragonEntity.MODE_FOLLOW && !dragon.isOrderedToSit(), "der Drache bleibt sitzen");
            dragon.discard();
            helper.succeed();
        } finally {
            leave(helper, player);
        }
    }
}
