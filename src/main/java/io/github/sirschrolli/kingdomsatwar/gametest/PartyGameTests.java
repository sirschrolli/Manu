package io.github.sirschrolli.kingdomsatwar.gametest;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.entity.boss.BossParty;
import io.github.sirschrolli.kingdomsatwar.entity.boss.DragonBossEntity;
import io.github.sirschrolli.kingdomsatwar.entity.realm.RealmWarriorEntity;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierEntity;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierType;
import io.github.sirschrolli.kingdomsatwar.hero.HeroLevel;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.war.WarriorRole;
import io.netty.channel.embedded.EmbeddedChannel;
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
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * Mehrspieler: Bosse, gegen die mehrere Spieler gemeinsam kämpfen (siehe {@link BossParty}). Ein Fürst eines Reichs dient
 * als Boss (er schläft nicht und braucht keinen Hort).
 */
final class PartyGameTests {
    private static final Identifier ARENA = KingdomsAtWar.id("test_arena");

    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> SCALING =
            ModGameTests.TEST_FUNCTIONS.register("party_boss_scaling", () -> PartyGameTests::scaling);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> REWARDS =
            ModGameTests.TEST_FUNCTIONS.register("party_boss_rewards", () -> PartyGameTests::rewards);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> SOLDIER =
            ModGameTests.TEST_FUNCTIONS.register("party_boss_soldier", () -> PartyGameTests::soldier);

    private PartyGameTests() {}

    static void init() {}

    static void register(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment) {
        for (var test : List.of(SCALING, REWARDS, SOLDIER)) {
            event.registerTest(test.getId(), new FunctionGameTestInstance(test.key(),
                    new TestData<>(environment, Level.OVERWORLD, ARENA, 100, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
        }
    }

    private static RealmWarriorEntity boss(GameTestHelper helper, WarriorRole role, BlockPos relative) {
        ServerLevel level = helper.getLevel();
        RealmWarriorEntity warrior = ModEntities.REALM_WARRIORS.get(role).get().create(level, EntitySpawnReason.MOB_SUMMONED);
        if (warrior == null) {
            throw new IllegalStateException("Kein Krieger");
        }
        Vec3 at = helper.absoluteVec(Vec3.atBottomCenterOf(relative));
        warrior.snapTo(at.x, at.y, at.z, 0.0f, 0.0f);
        warrior.setNoAi(true);
        level.addFreshEntity(warrior);
        return warrior;
    }

    private static ServerPlayer player(GameTestHelper helper, String name, BlockPos relative) {
        ServerPlayer player = MageGameTests.survivalPlayer(helper, name, new EmbeddedChannel[1], 0);
        player.getInventory().clearContent();
        player.connection.markClientLoaded();   // sonst ist er unverwundbar wie ein Spieler, der gerade erst die Welt lädt
        player.getAbilities().invulnerable = false;   // die Testwelt läuft im Kreativmodus
        player.getAbilities().instabuild = false;
        Vec3 at = helper.absoluteVec(Vec3.atBottomCenterOf(relative));
        player.teleportTo(helper.getLevel(), at.x, at.y, at.z, java.util.Set.of(), 0.0f, 0.0f, true);
        return player;
    }

    /**
     * Mit jedem Mitstreiter wird der Fürst stärker (+35 % Lebenskraft), der Anteil seiner Lebenskraft bleibt; wer von ihm
     * getroffen wird, kämpft auch mit. Ein gewöhnlicher Krieger ist kein Boss; die Drachen werden beim Erwachen stärker und
     * nicht noch einmal.
     */
    private static void scaling(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        RealmWarriorEntity lord = boss(helper, WarriorRole.LORD, new BlockPos(4, 1, 4));
        RealmWarriorEntity archer = boss(helper, WarriorRole.ARCHER, new BlockPos(2, 1, 4));
        ServerPlayer a = player(helper, "party-a", new BlockPos(4, 1, 1));
        ServerPlayer b = player(helper, "party-b", new BlockPos(5, 1, 1));
        ServerPlayer c = player(helper, "party-c", new BlockPos(6, 1, 1));
        try {
            helper.assertTrue(BossParty.isBoss(lord) && !BossParty.isBoss(archer), "Nur der Fürst ist ein Boss");
            double base = lord.getMaxHealth();
            lord.hurtServer(level, level.damageSources().playerAttack(a), 10.0f);
            helper.assertValueEqual(BossParty.members(lord).size(), 1, "Mitstreiter nach dem ersten Treffer");
            helper.assertTrue(Math.abs(lord.getMaxHealth() - base) < 0.01, "Allein wird er nicht stärker: " + lord.getMaxHealth());
            float fraction = lord.getHealth() / lord.getMaxHealth();
            lord.damageCooldownTime = 0;
            lord.hurtTime = 0;
            lord.hurtServer(level, level.damageSources().playerAttack(b), 10.0f);
            helper.assertValueEqual(BossParty.members(lord).size(), 2, "Mitstreiter nach dem zweiten Spieler");
            helper.assertTrue(Math.abs(lord.getMaxHealth() - base * (1.0 + BossParty.PER_ALLY)) < 0.01,
                    "Zu zweit: " + lord.getMaxHealth() + " statt " + base * (1.0 + BossParty.PER_ALLY));
            float after = lord.getHealth() / lord.getMaxHealth();
            helper.assertTrue(after < fraction && after > fraction - 0.05f, "Der Anteil der Lebenskraft springt: " + fraction + " → " + after);
            // wer von ihm getroffen wird, kämpft mit
            c.hurtServer(level, level.damageSources().mobAttack(lord), 1.0f);
            helper.assertValueEqual(BossParty.members(lord).size(), 3, "Mitstreiter, nachdem er den dritten traf");
            helper.assertTrue(Math.abs(lord.getMaxHealth() - base * (1.0 + 2 * BossParty.PER_ALLY)) < 0.01, "Zu dritt: " + lord.getMaxHealth());
            // derselbe Spieler zählt nur einmal; ein gewöhnlicher Krieger sammelt keine Mitstreiter
            BossParty.join(lord, a);
            helper.assertValueEqual(BossParty.members(lord).size(), 3, "Ein Spieler zählt doppelt");
            helper.assertTrue(!BossParty.join(archer, a) && BossParty.members(archer).isEmpty(), "Ein Bogenschütze ist kein Boss");
            // die Drachen zählen ihre Herausforderer selbst
            DragonBossEntity dragon = ModEntities.PYRRHON.get().create(level, EntitySpawnReason.MOB_SUMMONED);
            if (dragon != null) {
                double dragonBase = dragon.getMaxHealth();
                BossParty.join(dragon, a);
                BossParty.join(dragon, b);
                helper.assertTrue(BossParty.members(dragon).size() == 2 && Math.abs(dragon.getMaxHealth() - dragonBase) < 0.01,
                        "Ein Drache wird doppelt stärker");
                dragon.discard();
            }
            helper.succeed();
        } finally {
            lord.discard();
            archer.discard();
            for (ServerPlayer p : List.of(a, b, c)) {
                level.getServer().getPlayerList().remove(p);
            }
        }
    }

    /**
     * Zwei kämpfen, einer setzt den letzten Treffer: Der Sieger bekommt seine Beute wie immer auf den Boden; der andere
     * bekommt Heldenerfahrung, den Sieg in seiner Statistik und seine eigene Beute ins Inventar. Wer weit weg ist, nichts.
     */
    private static void rewards(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        RealmWarriorEntity lord = boss(helper, WarriorRole.LORD, new BlockPos(4, 1, 4));
        ServerPlayer a = player(helper, "spoils-a", new BlockPos(4, 1, 1));
        ServerPlayer b = player(helper, "spoils-b", new BlockPos(5, 1, 1));
        ServerPlayer c = player(helper, "spoils-c", new BlockPos(6, 1, 1));
        try {
            lord.hurtServer(level, level.damageSources().playerAttack(b), 10.0f);
            lord.damageCooldownTime = 0;
            lord.hurtServer(level, level.damageSources().playerAttack(c), 10.0f);
            lord.damageCooldownTime = 0;
            // c ist beim Sieg weit fort
            Vec3 far = helper.absoluteVec(new Vec3(4.5, 1.0, 4.5)).add(400.0, 0.0, 0.0);
            c.teleportTo(level, far.x, far.y, far.z, java.util.Set.of(), 0.0f, 0.0f, true);
            int heroB = HeroLevel.of(b).experience();
            int heroC = HeroLevel.of(c).experience();
            lord.hurtServer(level, level.damageSources().playerAttack(a), 100_000.0f);
            helper.assertTrue(!lord.isAlive(), "Der Fürst fällt nicht");
            var killed = Stats.ENTITY_KILLED.get(lord.getType());
            helper.assertValueEqual(a.getStats().getValue(killed), 1, "Sieg des Siegers");
            helper.assertValueEqual(b.getStats().getValue(killed), 1, "Sieg des Mitstreiters");
            helper.assertTrue(HeroLevel.of(b).experience() > heroB, "Der Mitstreiter bekommt keine Heldenerfahrung");
            helper.assertTrue(!b.getInventory().isEmpty(), "Der Mitstreiter bekommt keine eigene Beute");
            helper.assertTrue(a.getInventory().isEmpty(), "Die Beute des Siegers landet nicht am Boden");
            helper.assertTrue(c.getStats().getValue(killed) == 0 && HeroLevel.of(c).experience() == heroC && c.getInventory().isEmpty(),
                    "Wer weit fort ist, bekommt trotzdem etwas");
            helper.succeed();
        } finally {
            lord.discard();
            for (ServerPlayer p : List.of(a, b, c)) {
                level.getServer().getPlayerList().remove(p);
            }
        }
    }

    /**
     * Setzt ein Soldat den letzten Treffer, zählt der Sieg trotzdem für alle Mitstreiter – auch für seinen Herrn, der ihn
     * nie selbst getroffen hat: Statistik (also der erste eigene Sieg), eigene Beute und Heldenerfahrung (so viel, wie der
     * Herr eines Soldaten bekommt). Die Beute am Boden gehört dem, der den Fürsten zuletzt traf.
     */
    private static void soldier(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        RealmWarriorEntity lord = boss(helper, WarriorRole.LORD, new BlockPos(4, 1, 4));
        ServerPlayer a = player(helper, "army-a", new BlockPos(4, 1, 1));
        ServerPlayer b = player(helper, "army-b", new BlockPos(5, 1, 1));
        SoldierEntity soldier = helper.spawn(ModEntities.SOLDIERS.get(SoldierType.SWORDSMAN).get(), new BlockPos(5, 1, 3));
        try {
            soldier.setOwner(b);
            soldier.setNoAi(true);
            lord.hurtServer(level, level.damageSources().playerAttack(a), 10.0f);
            lord.damageCooldownTime = 0;
            lord.hurtServer(level, level.damageSources().mobAttack(soldier), 10.0f);
            lord.damageCooldownTime = 0;
            helper.assertValueEqual(BossParty.members(lord).size(), 2, "Der Herr des Soldaten kämpft nicht mit");
            int heroA = HeroLevel.of(a).experience();
            int heroB = HeroLevel.of(b).experience();
            lord.hurtServer(level, level.damageSources().mobAttack(soldier), 100_000.0f);
            helper.assertTrue(!lord.isAlive(), "Der Fürst fällt nicht");
            var killed = Stats.ENTITY_KILLED.get(lord.getType());
            helper.assertValueEqual(a.getStats().getValue(killed), 1, "Sieg des Spielers");
            helper.assertValueEqual(b.getStats().getValue(killed), 1, "Sieg des Herrn");
            helper.assertTrue(a.getInventory().isEmpty(), "Die Beute dessen, der ihn zuletzt traf, landet nicht am Boden");
            helper.assertTrue(!b.getInventory().isEmpty(), "Der Herr bekommt keine eigene Beute");
            // (Fortschritte wie „Monsterjäger“ bringen noch etwas dazu)
            int full = HeroLevel.killExperience(lord);
            int share = Math.round(full * HeroLevel.FOLLOWER_SHARE);
            int gainA = HeroLevel.of(a).experience() - heroA;
            int gainB = HeroLevel.of(b).experience() - heroB;
            helper.assertTrue(gainA >= share && gainA < full, "Heldenerfahrung des Spielers: " + gainA + " statt " + share);
            helper.assertTrue(gainB >= share && gainB < full, "Heldenerfahrung des Herrn: " + gainB + " statt " + share);
            helper.succeed();
        } finally {
            lord.discard();
            soldier.discard();
            for (ServerPlayer p : List.of(a, b)) {
                level.getServer().getPlayerList().remove(p);
            }
        }
    }
}
