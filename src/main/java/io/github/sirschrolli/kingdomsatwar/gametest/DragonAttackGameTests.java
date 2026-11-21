package io.github.sirschrolli.kingdomsatwar.gametest;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import io.github.sirschrolli.kingdomsatwar.entity.boss.BossAttack;
import io.github.sirschrolli.kingdomsatwar.entity.boss.BossProjectile;
import io.github.sirschrolli.kingdomsatwar.entity.boss.DragonBossEntity;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierEntity;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierType;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * Die großen eigenen Angriffe der sechs Drachen-Bosse: Jeder Boss kennt seine zwei neuen Angriffe, jeder läuft von
 * Anfang bis Ende ohne Fehler durch und trifft einen Ritter, der zwölf Blöcke vor dem Drachen steht (Splittersturm und
 * Felslawine schießen dafür ihre Geschosse ab).
 */
final class DragonAttackGameTests {
    private static final Identifier TOWN = KingdomsAtWar.id("test_town");

    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> SIGNATURE =
            ModGameTests.TEST_FUNCTIONS.register("dragon_signature_attacks", () -> DragonAttackGameTests::signature);

    /** Die neuen Angriffe je Element. */
    private static final Map<DragonType, List<String>> NEW = Map.of(
            DragonType.FIRE, List.of("fire_tornado", "magma_fissures"),
            DragonType.ICE, List.of("frost_beam", "shard_nova"),
            DragonType.LIGHTNING, List.of("ball_lightning", "storm_cage"),
            DragonType.NATURE, List.of("thorn_wave", "solar_beam"),
            DragonType.EARTH, List.of("rolling_boulders", "earthsplitter"),
            DragonType.WATER, List.of("geyser_field", "water_serpents"));

    private DragonAttackGameTests() {}

    static void init() {}

    static void register(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment) {
        event.registerTest(SIGNATURE.getId(), new FunctionGameTestInstance(SIGNATURE.key(),
                new TestData<>(environment, Level.OVERWORLD, TOWN, 100, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
    }

    private static void signature(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        TownGameTests.clearArea(helper);
        int ground = TownGameTests.ground(helper);
        BlockPos home = helper.absolutePos(new BlockPos(24, ground, 14));
        List<String> problems = new ArrayList<>();
        for (DragonType type : List.of(DragonType.FIRE, DragonType.ICE, DragonType.LIGHTNING, DragonType.NATURE, DragonType.EARTH,
                DragonType.WATER)) {
            var bossType = ModEntities.bossType(type);
            if (bossType == null) {
                problems.add(type.id() + ": kein Boss");
                continue;
            }
            DragonBossEntity boss = bossType.create(level, EntitySpawnReason.COMMAND);
            SoldierEntity knight = ModEntities.SOLDIERS.get(SoldierType.KNIGHT).get().create(level, EntitySpawnReason.COMMAND);
            try {
                // der Drache schaut nach Süden auf den Ritter
                boss.snapTo(home.getX() + 0.5, home.getY(), home.getZ() + 0.5, 0.0f, 0.0f);
                boss.setYBodyRot(0.0f);
                boss.setYHeadRot(0.0f);
                boss.setHome(home);
                boss.setNoAi(true);
                level.addFreshEntity(boss);
                knight.snapTo(home.getX() + 0.5, home.getY(), home.getZ() + 12.5, 180.0f, 0.0f);
                knight.getAttribute(Attributes.MAX_HEALTH).setBaseValue(4000.0);
                knight.setNoAi(true);
                level.addFreshEntity(knight);
                for (String name : NEW.get(type)) {
                    BossAttack attack = boss.findAttack(name);
                    if (attack == null) {
                        problems.add(type.id() + ": kennt " + name + " nicht");
                        continue;
                    }
                    knight.setHealth(knight.getMaxHealth());
                    knight.damageCooldownTime = 0;
                    knight.snapTo(home.getX() + 0.5, home.getY(), home.getZ() + 12.5, 180.0f, 0.0f);
                    knight.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
                    int before = projectiles(helper, boss);
                    try {
                        attack.start(level, boss, knight);
                        for (int t = 0; t < attack.duration(); t++) {
                            attack.tick(level, boss, knight, t);
                            // Treffer nacheinander zählen (im selben Spieltick gäbe es sonst die Schonfrist nach dem ersten)
                            knight.damageCooldownTime = 0;
                        }
                        attack.stop(level, boss);
                    } catch (RuntimeException e) {
                        problems.add(type.id() + "/" + name + ": " + e);
                        KingdomsAtWar.LOGGER.error("[Test] Drachenangriff {}", name, e);
                        continue;
                    }
                    boolean hit = knight.getHealth() < knight.getMaxHealth() - 0.01f;
                    KingdomsAtWar.LOGGER.info("[Test] {}/{}: Leben {} / {}", type.id(), name, knight.getHealth(), knight.getMaxHealth());
                    boolean shot = projectiles(helper, boss) > before;
                    // Splittersturm und Felslawine schießen Geschosse ab (die treffen erst später)
                    boolean projectiles = name.equals("shard_nova") || name.equals("rolling_boulders");
                    if (projectiles ? !shot : !hit) {
                        problems.add(type.id() + "/" + name + (projectiles ? ": keine Geschosse" : ": trifft den Ritter nicht"));
                    }
                }
            } finally {
                boss.discard();
                knight.discard();
                for (var p : level.getEntitiesOfClass(BossProjectile.class, area(helper))) {
                    p.discard();
                }
            }
        }
        helper.assertTrue(problems.isEmpty(), "Neue Drachenangriffe: " + String.join("; ", problems));
        helper.succeed();
    }

    private static int projectiles(GameTestHelper helper, DragonBossEntity boss) {
        return helper.getLevel().getEntitiesOfClass(BossProjectile.class, area(helper)).size();
    }

    private static AABB area(GameTestHelper helper) {
        return AABB.encapsulatingFullBlocks(helper.absolutePos(new BlockPos(-20, -10, -20)), helper.absolutePos(new BlockPos(68, 40, 68)));
    }
}
