package io.github.sirschrolli.kingdomsatwar.gametest;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.entity.combat.Family;
import io.github.sirschrolli.kingdomsatwar.entity.miniboss.MiniBoss;
import io.github.sirschrolli.kingdomsatwar.entity.miniboss.MiniBossCore;
import io.github.sirschrolli.kingdomsatwar.entity.miniboss.MiniSpecial;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierType;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.rules.Hardness;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * Tests der Mini-Bosse (seit 1.15): jede ihrer Künste wirkt (trifft, ruft Diener, lähmt), sie schlafen, bis man sie stört,
 * geraten in Rage, kehren heim und schlafen wieder ein, ihre Diener vergehen mit ihnen – und sie zählen für die Härte als
 * Bosse, ohne in {@code c:bosses} zu stehen.
 */
final class MiniBossGameTests {
    private static final Identifier TOWN = KingdomsAtWar.id("test_town");

    /** Eine Kunst: wer, welche, wie weit der Ritter steht und was danach gelten muss. */
    private record Case(String name, Supplier<? extends EntityType<? extends Mob>> type, String special, double distance, Check check) {}

    private enum Check {
        HURT, MINIONS, SLOWED
    }

    private static final List<Case> CASES = List.of(
            new Case("brood_web", () -> ModEntities.BROOD_MOTHER.get(), "web", 8.0, Check.HURT),
            new Case("brood_brood", () -> ModEntities.BROOD_MOTHER.get(), "brood", 6.0, Check.MINIONS),
            new Case("brood_pounce", () -> ModEntities.BROOD_MOTHER.get(), "pounce", 9.0, Check.HURT),
            new Case("brood_venom", () -> ModEntities.BROOD_MOTHER.get(), "venom", 4.0, Check.HURT),
            new Case("crypt_raise", () -> ModEntities.CRYPT_KING.get(), "raise", 6.0, Check.MINIONS),
            new Case("crypt_cleave", () -> ModEntities.CRYPT_KING.get(), "cleave", 3.0, Check.HURT),
            new Case("crypt_chill", () -> ModEntities.CRYPT_KING.get(), "chill", 3.5, Check.HURT),
            new Case("crypt_bones", () -> ModEntities.CRYPT_KING.get(), "bones", 8.0, Check.HURT),
            new Case("rune_slam", () -> ModEntities.RUNE_WARDEN.get(), "slam", 3.5, Check.HURT),
            new Case("rune_beam", () -> ModEntities.RUNE_WARDEN.get(), "beam", 10.0, Check.HURT),
            new Case("rune_mines", () -> ModEntities.RUNE_WARDEN.get(), "mines", 8.0, Check.HURT),
            new Case("rune_ward", () -> ModEntities.RUNE_WARDEN.get(), "ward", 3.5, Check.HURT),
            new Case("troll_boulder", () -> ModEntities.TROLL_FATHER.get(), "boulder", 10.0, Check.HURT),
            new Case("troll_stomp", () -> ModEntities.TROLL_FATHER.get(), "stomp", 4.0, Check.HURT),
            new Case("troll_roar", () -> ModEntities.TROLL_FATHER.get(), "roar", 5.0, Check.SLOWED),
            new Case("troll_spin", () -> ModEntities.TROLL_FATHER.get(), "spin", 2.8, Check.HURT));

    private static final List<DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>>> SPECIALS = new ArrayList<>();

    static {
        for (Case c : CASES) {
            SPECIALS.add(ModGameTests.TEST_FUNCTIONS.register("miniboss_" + c.name(), () -> helper -> special(helper, c)));
        }
    }

    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> RULES =
            ModGameTests.TEST_FUNCTIONS.register("miniboss_rules", () -> MiniBossGameTests::rules);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> SLEEP =
            ModGameTests.TEST_FUNCTIONS.register("miniboss_sleep_wake", () -> MiniBossGameTests::sleepWake);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> NEAR =
            ModGameTests.TEST_FUNCTIONS.register("miniboss_wake_near", () -> MiniBossGameTests::wakeNear);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> REST =
            ModGameTests.TEST_FUNCTIONS.register("miniboss_rest", () -> MiniBossGameTests::rest);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> RAGE =
            ModGameTests.TEST_FUNCTIONS.register("miniboss_enrage", () -> MiniBossGameTests::enrage);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> MINIONS =
            ModGameTests.TEST_FUNCTIONS.register("miniboss_minions_vanish", () -> MiniBossGameTests::minionsVanish);

    private MiniBossGameTests() {}

    static void init() {}

    static void register(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment) {
        List<DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>>> all = new ArrayList<>(SPECIALS);
        all.addAll(List.of(RULES, SLEEP, NEAR, REST, RAGE, MINIONS));
        for (var test : all) {
            event.registerTest(test.getId(), new FunctionGameTestInstance(test.key(),
                    new TestData<>(environment, Level.OVERWORLD, TOWN, 300, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
        }
    }

    // =====================================================================================
    // Hilfen
    // =====================================================================================

    /** Ein ebener Steinboden mit Luft darüber (die Mini-Bosse sind groß). */
    private static void arena(GameTestHelper helper) {
        for (int x = 2; x <= 45; x++) {
            for (int z = 2; z <= 45; z++) {
                helper.setBlock(x, 0, z, Blocks.STONE);
                for (int y = 1; y <= 12; y++) {
                    helper.setBlock(x, y, z, Blocks.AIR);
                }
            }
        }
        helper.getLevel().getServer().setDifficulty(Difficulty.HARD, true);
    }

    /** Der Mini-Boss in der Mitte; seine gewöhnlichen Hiebe richten keinen Schaden an (nur seine Künste sollen zählen). */
    private static Mob boss(GameTestHelper helper, EntityType<? extends Mob> type) {
        Mob boss = helper.spawn(type, new BlockPos(24, 1, 24));
        Vec3 at = helper.absoluteVec(new Vec3(24.5, 1.0, 24.5));
        boss.snapTo(at.x, at.y, at.z, 0.0f, 0.0f);
        boss.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(0.0);
        return boss;
    }

    /** Ein Ritter, der viel aushält und still steht – {@code distance} Blöcke südlich des Bosses. */
    private static LivingEntity knight(GameTestHelper helper, double distance) {
        LivingEntity knight = helper.spawnWithNoFreeWill(ModEntities.SOLDIERS.get(SoldierType.KNIGHT).get(), new BlockPos(24, 1, 24));
        knight.getAttribute(Attributes.MAX_HEALTH).setBaseValue(800.0);
        knight.setHealth(800.0f);
        Vec3 at = helper.absoluteVec(new Vec3(24.5, 1.0, 24.5 + distance));
        knight.snapTo(at.x, at.y, at.z, 180.0f, 0.0f);
        return knight;
    }

    private static MiniBossCore core(Mob boss) {
        return ((MiniBoss) boss).miniBoss();
    }

    private static float hurt(LivingEntity e) {
        return e.getMaxHealth() - e.getHealth();
    }

    // =====================================================================================

    /** Eine Kunst wirkt: der Ritter wird getroffen (oder Diener erscheinen, oder er ist gelähmt). */
    private static void special(GameTestHelper helper, Case c) {
        arena(helper);
        ServerLevel level = helper.getLevel();
        Mob boss = boss(helper, c.type().get());
        LivingEntity knight = knight(helper, c.distance());
        helper.runAfterDelay(5, () -> {
            MiniBossCore core = core(boss);
            helper.assertTrue(core.force(level, c.special(), knight), "keine Kunst " + c.special() + " bei " + boss.getName().getString());
            MiniSpecial current = core.current();
            helper.assertTrue(current != null && current.id().equals(c.special()), c.special() + " läuft nicht");
            helper.assertTrue(((MiniBoss) boss).isAwake(), "er ist nicht erwacht");
            ModGameTests.await(helper, () -> switch (c.check()) {
                case HURT -> hurt(knight) > 0.0f;
                case MINIONS -> core.minionCount(level) >= (c.special().equals("raise") ? 2 : 1);
                case SLOWED -> knight.hasEffect(MobEffects.SLOWNESS);
            }, 220, () -> c.name() + ": nichts geschehen (Schaden " + hurt(knight) + ", Diener " + core.minionCount(level) + ")", helper::succeed);
        });
    }

    /** Jeder Mini-Boss hat vier Künste (eine nur in Rage), zählt für die Härte als Boss, steht nicht in c:bosses, hat keinen Kampfstil. */
    private static void rules(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        for (Supplier<? extends EntityType<? extends Mob>> type : List.<Supplier<? extends EntityType<? extends Mob>>>of(ModEntities.BROOD_MOTHER,
                ModEntities.CRYPT_KING, ModEntities.RUNE_WARDEN, ModEntities.TROLL_FATHER)) {
            Mob boss = type.get().create(level, EntitySpawnReason.MOB_SUMMONED);
            helper.assertTrue(boss instanceof MiniBoss, "kein Mini-Boss: " + type.get());
            String name = boss.getName().getString();
            List<MiniSpecial> specials = core(boss).specials();
            helper.assertTrue(specials.size() == 4, name + ": " + specials.size() + " Künste");
            helper.assertTrue(specials.stream().filter(MiniSpecial::enragedOnly).count() == 1, name + ": nicht genau eine Kunst der Rage");
            helper.assertTrue(Hardness.isBoss(boss), name + " zählt für die Härte nicht als Boss");
            helper.assertFalse(type.get().builtInRegistryHolder().is(net.neoforged.neoforge.common.Tags.EntityTypes.BOSSES), name + " steht in c:bosses");
            helper.assertTrue(Family.of(boss) == null, name + " hat einen Kampfstil der gewöhnlichen Monster");
            helper.assertFalse(boss.removeWhenFarAway(1000.0), name + " verschwindet in der Ferne");
            boss.discard();
        }
        helper.succeed();
    }

    /** Er schläft und schaut niemanden an; trifft ihn jemand, erwacht er und hat den Angreifer im Visier. */
    private static void sleepWake(GameTestHelper helper) {
        arena(helper);
        ServerLevel level = helper.getLevel();
        Mob boss = boss(helper, ModEntities.TROLL_FATHER.get());
        LivingEntity knight = knight(helper, 5.0);
        helper.runAfterDelay(30, () -> {
            helper.assertFalse(((MiniBoss) boss).isAwake(), "er ist von selbst erwacht");
            helper.assertTrue(boss.getTarget() == null, "er hat im Schlaf ein Ziel");
            boss.hurtServer(level, level.damageSources().mobAttack(knight), 2.0f);
            ModGameTests.await(helper, () -> ((MiniBoss) boss).isAwake() && boss.getTarget() == knight, 40,
                    () -> "er erwacht nicht (wach " + ((MiniBoss) boss).isAwake() + ", Ziel " + boss.getTarget() + ")", helper::succeed);
        });
    }

    /**
     * Wer ihn weckt: ein Spieler im Kreativmodus nicht (auch nicht aus der Nähe); ein Spieler im Überlebensmodus, der nahe kommt
     * und ihn sieht – nicht aus der Ferne und nicht durch eine Mauer.
     */
    private static void wakeNear(GameTestHelper helper) {
        arena(helper);
        for (int x = 18; x <= 30; x++) {
            for (int y = 1; y <= 6; y++) {
                helper.setBlock(x, y, 18, Blocks.STONE_BRICKS);
            }
        }
        Mob boss = boss(helper, ModEntities.CRYPT_KING.get());
        ServerPlayer creative = helper.makeMockServerPlayerInLevel();
        Vec3 near = helper.absoluteVec(new Vec3(24.5, 1.0, 28.5));
        creative.snapTo(near.x, near.y, near.z, 180.0f, 0.0f);
        helper.runAfterDelay(30, () -> {
            helper.getLevel().getServer().getPlayerList().remove(creative);
            helper.assertFalse(((MiniBoss) boss).isAwake(), "ein Spieler im Kreativmodus hat ihn geweckt");
            MiniBossCore core = core(boss);
            net.minecraft.world.entity.player.Player survival = helper.makeMockPlayer(GameType.SURVIVAL);
            survival.snapTo(near.x, near.y, near.z, 180.0f, 0.0f);
            helper.assertTrue(core.wakes(survival), "ein Spieler im Überlebensmodus vor ihm weckt ihn nicht");
            Vec3 far = helper.absoluteVec(new Vec3(24.5, 1.0, 40.5));
            survival.snapTo(far.x, far.y, far.z, 180.0f, 0.0f);
            helper.assertFalse(core.wakes(survival), "ein Spieler 16 Blöcke entfernt weckt ihn");
            Vec3 behind = helper.absoluteVec(new Vec3(24.5, 1.0, 15.5));
            survival.snapTo(behind.x, behind.y, behind.z, 0.0f, 0.0f);
            helper.assertFalse(core.wakes(survival), "ein Spieler hinter einer Mauer weckt ihn");
            survival.discard();
            helper.succeed();
        });
    }

    /** Heimgeschickt: zurück an seinem Platz, geheilt, wieder im Schlaf, nicht mehr in Rage. */
    private static void rest(GameTestHelper helper) {
        arena(helper);
        ServerLevel level = helper.getLevel();
        Mob boss = boss(helper, ModEntities.RUNE_WARDEN.get());
        LivingEntity knight = knight(helper, 6.0);
        helper.runAfterDelay(5, () -> {
            MiniBossCore core = core(boss);
            BlockPos home = boss.blockPosition();
            core.setHome(home);
            core.wake(level, knight);
            boss.setHealth(boss.getMaxHealth() * 0.3f);
            helper.runAfterDelay(3, () -> {
                helper.assertTrue(((MiniBoss) boss).isEnraged(), "bei 30 % nicht in Rage");
                Vec3 away = helper.absoluteVec(new Vec3(34.5, 1.0, 34.5));
                boss.teleportTo(away.x, away.y, away.z);
                core.rest(level);
                helper.assertFalse(((MiniBoss) boss).isAwake(), "er schläft nicht");
                helper.assertFalse(((MiniBoss) boss).isEnraged(), "er ist noch in Rage");
                helper.assertTrue(boss.getHealth() >= boss.getMaxHealth() - 0.01f, "nicht geheilt");
                helper.assertTrue(boss.blockPosition().distSqr(home) <= 4, "nicht daheim: " + boss.blockPosition() + " statt " + home);
                helper.succeed();
            });
        });
    }

    /** Unter 40 % seiner Kraft gerät er in Rage und wird schneller. */
    private static void enrage(GameTestHelper helper) {
        arena(helper);
        ServerLevel level = helper.getLevel();
        Mob boss = boss(helper, ModEntities.BROOD_MOTHER.get());
        LivingEntity knight = knight(helper, 8.0);
        helper.runAfterDelay(5, () -> {
            core(boss).wake(level, knight);
            core(boss).hold(200);
            double speed = boss.getAttribute(Attributes.MOVEMENT_SPEED).getBaseValue();
            boss.setHealth(boss.getMaxHealth() * MiniBossCore.ENRAGE_AT * 0.9f);
            ModGameTests.await(helper, () -> ((MiniBoss) boss).isEnraged(), 20, () -> "keine Rage", () -> {
                helper.assertTrue(boss.getAttribute(Attributes.MOVEMENT_SPEED).getBaseValue() > speed, "in Rage nicht schneller");
                helper.succeed();
            });
        });
    }

    /** Stirbt der Gruftkönig, vergehen seine Gruftwächter mit ihm. */
    private static void minionsVanish(GameTestHelper helper) {
        arena(helper);
        ServerLevel level = helper.getLevel();
        Mob boss = boss(helper, ModEntities.CRYPT_KING.get());
        LivingEntity knight = knight(helper, 6.0);
        helper.runAfterDelay(5, () -> {
            MiniBossCore core = core(boss);
            core.force(level, "raise", knight);
            ModGameTests.await(helper, () -> core.minionCount(level) >= 2, 80, () -> "keine Gruftwächter", () -> {
                List<Mob> guards = level.getEntitiesOfClass(Mob.class, boss.getBoundingBox().inflate(12.0), core::isMinion);
                helper.assertTrue(guards.size() >= 2, "die Gruftwächter sind nicht in der Welt: " + guards.size());
                boss.kill(level);
                helper.runAfterDelay(2, () -> {
                    for (Mob guard : guards) {
                        helper.assertTrue(guard.isRemoved() || !guard.isAlive(), "ein Gruftwächter bleibt nach dem Tod des Königs");
                    }
                    helper.succeed();
                });
            });
        });
    }
}
