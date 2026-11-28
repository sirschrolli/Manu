package io.github.sirschrolli.kingdomsatwar.gametest;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.entity.boss.fury.BossFury;
import io.github.sirschrolli.kingdomsatwar.entity.boss.fury.Fury;
import io.github.sirschrolli.kingdomsatwar.entity.boss.fury.FuryAttack;
import io.github.sirschrolli.kingdomsatwar.entity.boss.fury.FuryElement;
import io.github.sirschrolli.kingdomsatwar.entity.boss.fury.FuryProfile;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierType;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

/** Tests des Zorns der Bosse (seit 1.14): jeder Boss hat seine Zornangriffe, jeder trifft, und vor der Vernichtung schützt Deckung. */
final class FuryGameTests {
    private static final Identifier ARENA = KingdomsAtWar.id("test_arena");

    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> PROFILES =
            ModGameTests.TEST_FUNCTIONS.register("fury_profiles", () -> FuryGameTests::profiles);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> RAIN =
            ModGameTests.TEST_FUNCTIONS.register("fury_rain", () -> helper -> attack(helper, FuryAttack.RAIN, 0.0, 3.5));
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> WAVES =
            ModGameTests.TEST_FUNCTIONS.register("fury_waves", () -> helper -> attack(helper, FuryAttack.WAVES, 0.0, 3.5));
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> LINES =
            ModGameTests.TEST_FUNCTIONS.register("fury_lines", () -> helper -> attack(helper, FuryAttack.LINES, 3.5, 0.0));
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> PULL =
            ModGameTests.TEST_FUNCTIONS.register("fury_pull", () -> helper -> attack(helper, FuryAttack.PULL, -3.5, 0.0));
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> BEAMS =
            ModGameTests.TEST_FUNCTIONS.register("fury_beams", () -> helper -> attack(helper, FuryAttack.BEAMS, 0.0, -3.5));
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> SECTORS =
            ModGameTests.TEST_FUNCTIONS.register("fury_sectors", () -> helper -> attack(helper, FuryAttack.SECTORS, 2.5, 2.5));
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> NOVA =
            ModGameTests.TEST_FUNCTIONS.register("fury_nova_cover", () -> FuryGameTests::novaCover);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> ENRAGE =
            ModGameTests.TEST_FUNCTIONS.register("fury_enrage", () -> FuryGameTests::enrage);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> AUTO =
            ModGameTests.TEST_FUNCTIONS.register("fury_auto", () -> FuryGameTests::auto);

    private FuryGameTests() {}

    static void init() {}

    static void register(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment) {
        for (var test : List.of(PROFILES, RAIN, WAVES, LINES, PULL, BEAMS, SECTORS, NOVA, ENRAGE)) {
            event.registerTest(test.getId(), new FunctionGameTestInstance(test.key(),
                    new TestData<>(environment, Level.OVERWORLD, ARENA, 300, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
        }
        event.registerTest(AUTO.getId(), new FunctionGameTestInstance(AUTO.key(),
                new TestData<>(environment, Level.OVERWORLD, ARENA, 500, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
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
        helper.getLevel().getServer().setDifficulty(Difficulty.HARD, true);
    }

    /** Ein Ritter, der viel aushält und still steht. */
    private static LivingEntity knight(GameTestHelper helper, double x, double z) {
        LivingEntity knight = helper.spawnWithNoFreeWill(ModEntities.SOLDIERS.get(SoldierType.KNIGHT).get(), new BlockPos(0, 1, 0));
        knight.getAttribute(Attributes.MAX_HEALTH).setBaseValue(600.0);
        knight.setHealth(600.0f);
        Vec3 at = helper.absoluteVec(new Vec3(x, 1.0, z));
        knight.snapTo(at.x, at.y, at.z, 0.0f, 0.0f);
        return knight;
    }

    /** Der Glutschmied (Feuer) in der Mitte, ohne eigenen Willen – nur sein Zorn wirkt, und nur in dieser Arena. */
    private static Mob boss(GameTestHelper helper) {
        Mob boss = still(helper);
        BossFury.hold(boss, 100000);
        return boss;
    }

    /** Der Boss ohne eigene Angriffe (er denkt nicht) – sein Zorn läuft trotzdem. */
    private static Mob still(GameTestHelper helper) {
        Mob boss = helper.spawn(ModEntities.GLUTSCHMIED.get(), new BlockPos(4, 1, 4));
        boss.setNoAi(true);
        Vec3 at = helper.absoluteVec(new Vec3(4.5, 1.0, 4.5));
        boss.snapTo(at.x, at.y, at.z, 0.0f, 0.0f);
        helper.assertTrue(BossFury.of(boss) != null, "der Glutschmied hat keinen Zorn");
        BossFury.confine(boss, helper.getBounds().inflate(1.0));
        return boss;
    }

    private static float hurt(LivingEntity knight) {
        return knight.getMaxHealth() - knight.getHealth();
    }

    // =====================================================================================

    /** Jeder Boss hat zwei verschiedene Zornangriffe, und jeder Angriff hat in jedem Element einen Namen. */
    private static void profiles(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        List<Supplier<? extends EntityType<? extends Mob>>> bosses = new ArrayList<>(List.of(ModEntities.PYRRHON, ModEntities.GLACIARA,
                ModEntities.VERDANTIS, ModEntities.TERRAGON, ModEntities.VOLTARION, ModEntities.THALASSA, ModEntities.NYXAR, ModEntities.PHASMOR,
                ModEntities.QUETZARA, ModEntities.STONE_KEEPER, ModEntities.SORDIN, ModEntities.GLUTSCHMIED, ModEntities.IGNARA,
                ModEntities.MOTH_QUEEN, ModEntities.SHATTERED_KING, ModEntities.REEF_COLOSSUS, ModEntities.ANNI_TROLL, ModEntities.ASTERIA,
                ModEntities.MISS_MYSTIK, ModEntities.SOUL_SHEPHERD, ModEntities.STAR_WEAVER, ModEntities.WORLD_EATER, ModEntities.HOUR_WARDEN,
                ModEntities.KRAKEN, ModEntities.SEA_WITCH, ModEntities.ROBBER_KNIGHT, ModEntities.VENTOR, ModEntities.SCHROLLI));
        bosses.addAll(ModEntities.MAGES.values());
        for (Supplier<? extends EntityType<? extends Mob>> type : bosses) {
            Mob boss = type.get().create(level, EntitySpawnReason.MOB_SUMMONED);
            helper.assertTrue(boss != null, "kein " + type.get());
            FuryProfile profile = FuryProfile.of(boss);
            String name = boss.getName().getString();
            helper.assertTrue(profile != null, "kein Zorn: " + name);
            helper.assertTrue(profile.attacks().size() == 2 && profile.attacks().get(0) != profile.attacks().get(1),
                    "zwei verschiedene Zornangriffe: " + name + " " + profile.attacks());
            helper.assertTrue(!profile.attacks().contains(FuryAttack.NOVA), "die Vernichtung kommt erst im Zorn: " + name);
            boss.discard();
        }
        Set<String> keys = new HashSet<>();
        for (FuryAttack attack : FuryAttack.values()) {
            for (FuryElement element : FuryElement.values()) {
                helper.assertTrue(keys.add(attack.key(element)), "doppelter Schlüssel: " + attack.key(element));
                helper.assertTrue(!attack.english(element).isBlank() && !attack.german(element).isBlank(), "ohne Namen: " + attack.key(element));
            }
        }
        helper.succeed();
    }

    /** Ein Zornangriff trifft einen Ritter, der in der Nähe des Bosses stehen bleibt. */
    private static void attack(GameTestHelper helper, FuryAttack attack, double dx, double dz) {
        floor(helper);
        Mob boss = boss(helper);
        LivingEntity knight = knight(helper, 4.5 + dx, 4.5 + dz);
        helper.runAfterDelay(5, () -> {
            helper.assertTrue(BossFury.force(boss, attack), "kein Zorn");
            helper.assertTrue(BossFury.current(boss) == attack, "der Angriff läuft nicht");
            ModGameTests.await(helper, () -> BossFury.current(boss) == null, 200,
                    () -> attack + " hört nicht auf", () -> {
                        helper.assertTrue(BossFury.struck(boss, knight), attack + " hat den Ritter nicht getroffen");
                        helper.assertTrue(hurt(knight) > 0.0f, attack + " hat keinen Schaden angerichtet");
                        helper.succeed();
                    });
        });
    }

    /** Die Vernichtung: wer hinter einer Mauer steht, bleibt verschont – wer im Freien steht, nicht. */
    private static void novaCover(GameTestHelper helper) {
        floor(helper);
        for (int x = 2; x <= 6; x++) {
            for (int y = 1; y <= 4; y++) {
                helper.setBlock(x, y, 6, Blocks.STONE_BRICKS);
            }
        }
        Mob boss = boss(helper);
        LivingEntity open = knight(helper, 1.5, 4.5);
        LivingEntity covered = knight(helper, 4.5, 7.5);
        helper.runAfterDelay(5, () -> {
            helper.assertTrue(BossFury.force(boss, FuryAttack.NOVA), "kein Zorn");
            ModGameTests.await(helper, () -> BossFury.current(boss) == null, 200, () -> "die Vernichtung hört nicht auf", () -> {
                helper.assertTrue(BossFury.struck(boss, open), "der Ritter im Freien wurde nicht getroffen");
                helper.assertTrue(hurt(open) > 0.0f, "kein Schaden im Freien");
                helper.assertFalse(BossFury.struck(boss, covered), "der Ritter hinter der Mauer wurde getroffen");
                helper.succeed();
            });
        });
    }

    /** Bei halber Kraft gerät der Boss in Zorn: Wutausbruch, schneller, und als nächstes kommt die Vernichtung. */
    private static void enrage(GameTestHelper helper) {
        floor(helper);
        Mob boss = boss(helper);
        BossFury.allowAuto(boss);
        LivingEntity knight = knight(helper, 4.5, 7.0);
        boss.setTarget(knight);
        double speed = boss.getAttributeValue(Attributes.MOVEMENT_SPEED);
        boss.setHealth(boss.getMaxHealth() * 0.45f);
        Vec3 before = knight.position();
        ModGameTests.await(helper, () -> BossFury.enraged(boss), 120, () -> "der Glutschmied gerät nicht in Zorn", () -> {
            helper.assertTrue(boss.getAttributeValue(Attributes.MOVEMENT_SPEED) > speed + 1.0e-4, "im Zorn nicht schneller");
            helper.assertTrue(hurt(knight) > 0.0f, "der Wutausbruch hat den Ritter nicht getroffen");
            helper.runAfterDelay(6, () -> {
                helper.assertTrue(knight.position().distanceTo(before) > 0.5, "der Wutausbruch hat den Ritter nicht weggestoßen");
                Fury fury = BossFury.of(boss);
                helper.assertTrue(fury != null && fury.enraged(), "Zorn verloren");
                helper.succeed();
            });
        });
    }

    /** Im Kampf beginnt der Boss von selbst einen seiner beiden Zornangriffe. */
    private static void auto(GameTestHelper helper) {
        floor(helper);
        Mob boss = still(helper);
        BossFury.allowAuto(boss);
        LivingEntity knight = knight(helper, 4.5, 7.5);
        boss.setTarget(knight);
        FuryProfile profile = FuryProfile.of(boss);
        helper.assertTrue(profile != null, "kein Profil");
        ModGameTests.await(helper, () -> BossFury.current(boss) != null, Fury.FIRST + 120, () -> "kein Zornangriff von selbst", () -> {
            helper.assertTrue(profile.attacks().contains(BossFury.current(boss)), "fremder Zornangriff: " + BossFury.current(boss));
            helper.succeed();
        });
    }
}
