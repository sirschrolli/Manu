package io.github.sirschrolli.kingdomsatwar.gametest;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.army.Recruitment;
import io.github.sirschrolli.kingdomsatwar.army.Training;
import io.github.sirschrolli.kingdomsatwar.army.Veterancy;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierEntity;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierType;
import io.github.sirschrolli.kingdomsatwar.registry.ModBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.ModItems;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * Tests zur Kampferfahrung der Truppen: Stufen und ihre Schwellen, Erfahrung aus Treffern und Siegen (auch für die
 * Kameraden, nie für Kämpfe gegen Unschuldige), was jede Stufe bringt, Ränge erst ab einer Stufe – bis zur Legende mit
 * ihrem zweiten Atem und ihrem Vorbild –, Speichern und alte Spielstände.
 */
final class VeterancyGameTests {
    private static final Identifier ARENA = KingdomsAtWar.id("test_arena");

    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> LEVELS =
            ModGameTests.TEST_FUNCTIONS.register("veterancy_levels", () -> VeterancyGameTests::levels);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> BATTLE =
            ModGameTests.TEST_FUNCTIONS.register("veterancy_battle", () -> VeterancyGameTests::battle);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> RANKS =
            ModGameTests.TEST_FUNCTIONS.register("veterancy_ranks", () -> VeterancyGameTests::ranks);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> LEGEND =
            ModGameTests.TEST_FUNCTIONS.register("veterancy_legend", () -> VeterancyGameTests::legend);

    private VeterancyGameTests() {}

    static void init() {}

    static void register(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment) {
        for (var test : List.of(LEVELS, BATTLE, RANKS, LEGEND)) {
            event.registerTest(test.getId(), new FunctionGameTestInstance(test.key(),
                    new TestData<>(environment, Level.OVERWORLD, ARENA, 200, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
        }
    }

    /** Die Schwellen steigen, jede Stufe gehört zu genau ihrer Erfahrung, Ränge verlangen steigende Stufen. */
    private static void levels(GameTestHelper helper) {
        helper.assertValueEqual(Veterancy.total(1), 0, "Stufe 1 ohne Erfahrung");
        for (int level = 2; level <= Veterancy.MAX_LEVEL; level++) {
            int need = Veterancy.total(level);
            helper.assertTrue(need > Veterancy.total(level - 1), "Schwelle zu Stufe " + level + " steigt nicht");
            helper.assertValueEqual(Veterancy.levelFor(need), level, "Stufe zur Schwelle " + need);
            helper.assertValueEqual(Veterancy.levelFor(need - 1), level - 1, "Stufe knapp unter der Schwelle " + need);
            float progress = Veterancy.progress(need - 1);
            helper.assertTrue(progress > 0.5f && progress < 1.0f, "Fortschritt kurz vor Stufe " + level + ": " + progress);
        }
        helper.assertValueEqual(Veterancy.levelFor(1_000_000), Veterancy.MAX_LEVEL, "höchste Stufe");
        helper.assertTrue(Veterancy.progress(Veterancy.total(Veterancy.MAX_LEVEL)) == 1.0f, "auf der höchsten Stufe ist die Leiste voll");
        for (int rank = 1; rank <= SoldierEntity.MAX_RANK; rank++) {
            helper.assertTrue(Veterancy.minLevel(rank) > Veterancy.minLevel(rank - 1), "Rang " + rank + " verlangt keine höhere Stufe");
        }
        helper.assertValueEqual(Veterancy.minLevel(SoldierEntity.MAX_RANK), 15, "Legende ab Stufe 15");
        helper.succeed();
    }

    /**
     * Ein Schwertkämpfer erschlägt einen Zombie (das eigene Wesen, das den Zombie ersetzt): Erfahrung für den Treffer und den Sieg, ein Kamerad in der Nähe lernt
     * ein Viertel mit; für einen Dorfbewohner gibt es nichts. Jede Stufe bringt mehr Leben, Schaden und Rüstung – die
     * Wunden bleiben dabei, wie sie sind.
     */
    private static void battle(GameTestHelper helper) {
        floor(helper);
        ServerLevel level = helper.getLevel();
        ServerPlayer player = player(helper);
        try {
            SoldierEntity soldier = soldier(helper, SoldierType.SWORDSMAN, new Vec3(2.5, 1, 4.5), player);
            SoldierEntity mate = soldier(helper, SoldierType.ARCHER, new Vec3(6.5, 1, 4.5), player);
            helper.assertValueEqual(soldier.getCombatLevel(), 1, "Stufe am Anfang");
            LivingEntity zombie = helper.spawn(ModEntities.ZOMBIE.get(), new BlockPos(4, 1, 6));
            float expected = Veterancy.killExperience(zombie) + zombie.getMaxHealth() * Veterancy.PER_DAMAGE;
            zombie.hurtServer(level, level.damageSources().mobAttack(soldier), 1000.0f);
            helper.assertTrue(!zombie.isAlive(), "der Zombie lebt noch");
            int gained = soldier.getCombatExperience();
            helper.assertTrue(gained >= expected * 0.75f && gained <= expected + 1.0f, "Erfahrung für den Zombie: " + gained + " statt " + expected);
            helper.assertValueEqual(mate.getCombatExperience(), (int) (Veterancy.killExperience(zombie) * Veterancy.SHARE), "Anteil des Kameraden");

            // gegen Unschuldige gibt es nichts
            LivingEntity villager = helper.spawn(ModEntities.VILLAGER.get(), new BlockPos(4, 1, 2));
            int before = soldier.getCombatExperience();
            villager.hurtServer(level, level.damageSources().mobAttack(soldier), 1000.0f);
            helper.assertValueEqual(soldier.getCombatExperience(), before, "Erfahrung für einen Dorfbewohner");

            // Stufen bringen Leben, Schaden und Rüstung
            soldier.setCombatExperience(0);
            double health = soldier.getAttributeValue(Attributes.MAX_HEALTH);
            double damage = soldier.getAttributeValue(Attributes.ATTACK_DAMAGE);
            double armor = soldier.getAttributeValue(Attributes.ARMOR);
            soldier.setHealth(soldier.getMaxHealth() - 6.0f);
            Veterancy.award(soldier, Veterancy.total(11));
            helper.assertValueEqual(soldier.getCombatLevel(), 11, "Stufe nach viel Erfahrung");
            double base = SoldierType.SWORDSMAN.health();
            helper.assertTrue(Math.abs(soldier.getAttributeValue(Attributes.MAX_HEALTH) - (health + base * 0.3)) < 0.01,
                    "Leben auf Stufe 11: " + soldier.getAttributeValue(Attributes.MAX_HEALTH));
            helper.assertTrue(Math.abs(soldier.getAttributeValue(Attributes.ATTACK_DAMAGE) - (damage + 1.5)) < 0.01, "Schaden auf Stufe 11");
            helper.assertTrue(Math.abs(soldier.getAttributeValue(Attributes.ARMOR) - (armor + 1.0)) < 0.01, "Rüstung auf Stufe 11");
            helper.assertTrue(Math.abs(soldier.getMaxHealth() - soldier.getHealth() - 6.0f) < 0.01f, "die Wunden sollten bleiben");
            // die höchste Stufe ist die Grenze
            Veterancy.award(soldier, 100_000);
            helper.assertValueEqual(soldier.getCombatLevel(), Veterancy.MAX_LEVEL, "höchste Stufe");
            helper.assertValueEqual(soldier.getCombatExperience(), Veterancy.total(Veterancy.MAX_LEVEL), "Erfahrung über der höchsten Stufe");
            helper.succeed();
        } finally {
            player.discard();
        }
    }

    /**
     * Ränge erst ab ihrer Stufe: ein Soldat auf Stufe 15 wird Schritt für Schritt bis zur Legende befördert (Meister
     * braucht einen Edelstein, Legende Edelsteine und Draconium) – danach geht es nicht weiter. Rang und Erfahrung
     * überstehen Speichern und Laden; ein Soldat aus einem alten Spielstand bekommt die Erfahrung, die sein Rang verlangt.
     */
    private static void ranks(GameTestHelper helper) {
        floor(helper);
        ServerLevel level = helper.getLevel();
        BlockPos tableLocal = new BlockPos(4, 1, 4);
        helper.setBlock(tableLocal, ModBlocks.RECRUITMENT_TABLE.get());
        BlockPos table = helper.absolutePos(tableLocal);
        ServerPlayer player = player(helper);
        try {
            SoldierEntity soldier = soldier(helper, SoldierType.SWORDSMAN, new Vec3(2.5, 1, 4.5), player);
            player.getInventory().add(new ItemStack(ModItems.GOLD_COIN.get(), 9));
            player.getInventory().add(new ItemStack(Items.IRON_INGOT, 3));
            player.getInventory().add(new ItemStack(ModItems.STEEL_INGOT.get(), 5));
            player.getInventory().add(new ItemStack(ModItems.GEMSTONE.get(), 3));
            player.getInventory().add(new ItemStack(ModItems.DRACONIUM.get(), 1));
            soldier.setCombatExperience(Veterancy.total(9));
            for (int rank = 1; rank <= 2; rank++) {
                helper.assertTrue(Training.train(player, table, soldier.getId(), Training.Action.RANK) == Recruitment.Result.TRAINED,
                        "Befördern zu Rang " + rank + " klappt nicht");
            }
            helper.assertTrue(!Training.readyForRank(soldier), "Meister schon auf Stufe 9?");
            helper.assertTrue(Training.train(player, table, soldier.getId(), Training.Action.RANK) == Recruitment.Result.NEEDS_LEVEL,
                    "Meister erst ab Stufe 10");
            Training.Cost master = Training.rankCost(SoldierType.SWORDSMAN, 2);
            Training.Cost legend = Training.rankCost(SoldierType.SWORDSMAN, 3);
            helper.assertTrue(master != null && master.items().stream().anyMatch(p -> p.item().get() == ModItems.GEMSTONE.get()),
                    "Meister ohne Edelstein");
            helper.assertTrue(legend != null && legend.items().stream().anyMatch(p -> p.item().get() == ModItems.DRACONIUM.get()),
                    "Legende ohne Draconium");
            helper.assertTrue(legend.coins() > master.coins(), "die Legende kostet nicht mehr Sold als der Meister");
            soldier.setCombatExperience(Veterancy.total(15));
            for (int rank = 3; rank <= SoldierEntity.MAX_RANK; rank++) {
                helper.assertTrue(Training.train(player, table, soldier.getId(), Training.Action.RANK) == Recruitment.Result.TRAINED,
                        "Befördern zu Rang " + rank + " klappt nicht");
            }
            helper.assertValueEqual(soldier.getRank(), SoldierEntity.MAX_RANK, "Rang der Legende");
            helper.assertTrue(soldier.getCustomName() != null && soldier.getCustomName().getString().contains("★★★★"), "Name zeigt vier Sterne nicht");
            helper.assertTrue(Training.train(player, table, soldier.getId(), Training.Action.RANK) == Recruitment.Result.MAX_RANK,
                    "über die Legende hinaus darf es nicht gehen");
            double base = SoldierType.SWORDSMAN.health();
            helper.assertTrue(Math.abs(soldier.getAttributeValue(Attributes.MAX_HEALTH) - base * (1.0 + 4 * 0.25 + 14 * Veterancy.HEALTH_PER_LEVEL))
                    < 0.01, "Leben der Legende: " + soldier.getAttributeValue(Attributes.MAX_HEALTH));

            // Speichern und Laden
            TagValueOutput out = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, level.registryAccess());
            soldier.saveWithoutId(out);
            CompoundTag tag = out.buildResult();
            SoldierEntity loaded = ModEntities.SOLDIERS.get(SoldierType.SWORDSMAN).get().create(level, EntitySpawnReason.LOAD);
            loaded.load(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), tag));
            helper.assertValueEqual(loaded.getRank(), SoldierEntity.MAX_RANK, "Rang nach dem Laden");
            helper.assertValueEqual(loaded.getCombatExperience(), Veterancy.total(15), "Erfahrung nach dem Laden");
            // ein Elitekämpfer aus einem älteren Spielstand (ohne Erfahrung) steht auf Stufe 6
            CompoundTag old = tag.copy();
            old.remove("Experience");
            old.putInt("Rank", 2);
            SoldierEntity veteran = ModEntities.SOLDIERS.get(SoldierType.SWORDSMAN).get().create(level, EntitySpawnReason.LOAD);
            veteran.load(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), old));
            helper.assertValueEqual(veteran.getCombatLevel(), Veterancy.minLevel(2), "Stufe eines alten Elitekämpfers");
            helper.succeed();
        } finally {
            player.discard();
        }
    }

    /**
     * Die Legende: Sinkt sie unter ein Viertel ihrer Lebenspunkte, rafft sie sich auf die Hälfte auf – aber nicht gleich
     * noch einmal. Kameraden an ihrer Seite lernen ein Viertel schneller (sie selbst nicht).
     */
    private static void legend(GameTestHelper helper) {
        floor(helper);
        ServerLevel level = helper.getLevel();
        ServerPlayer player = player(helper);
        try {
            SoldierEntity legend = soldier(helper, SoldierType.KNIGHT, new Vec3(2.5, 1, 4.5), player);
            legend.setCombatExperience(Veterancy.total(15));
            legend.setRank(SoldierEntity.MAX_RANK);
            legend.setHealth(legend.getMaxHealth());
            SoldierEntity mate = soldier(helper, SoldierType.SWORDSMAN, new Vec3(6.5, 1, 4.5), player);
            // zweiter Atem (Magie geht durch jede Rüstung)
            legend.hurtServer(level, level.damageSources().magic(), legend.getHealth() - legend.getMaxHealth() * 0.15f);
            helper.assertTrue(legend.isAlive() && Math.abs(legend.getHealth() - legend.getMaxHealth() * 0.5f) < 0.5f,
                    "kein zweiter Atem: " + legend.getHealth() + "/" + legend.getMaxHealth());
            legend.damageCooldownTime = 0;
            legend.hurtServer(level, level.damageSources().magic(), legend.getHealth() - legend.getMaxHealth() * 0.15f);
            helper.assertTrue(legend.getHealth() < legend.getMaxHealth() * 0.25f, "zweiter Atem gleich noch einmal: " + legend.getHealth() + "/"
                    + legend.getMaxHealth());
            // Vorbild: der Kamerad lernt mehr, die Legende selbst nicht
            int mateBefore = mate.getCombatExperience();
            Veterancy.award(mate, 100.0f);
            helper.assertValueEqual(mate.getCombatExperience() - mateBefore, Math.round(100.0f * Veterancy.LEGEND_BONUS), "Erfahrung an der Seite der Legende");
            SoldierEntity stranger = soldier(helper, SoldierType.SWORDSMAN, new Vec3(4.5, 1, 1.5), null);
            Veterancy.award(stranger, 100.0f);
            helper.assertValueEqual(stranger.getCombatExperience(), 100, "fremde Soldaten lernen nicht von der Legende");
            helper.succeed();
        } finally {
            player.discard();
        }
    }

    // --- Hilfen ---

    private static void floor(GameTestHelper helper) {
        for (int x = 0; x < 9; x++) {
            for (int z = 0; z < 9; z++) {
                helper.setBlock(x, 0, z, Blocks.STONE);
            }
        }
    }

    private static ServerPlayer player(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.getAbilities().instabuild = false;
        player.setGameMode(GameType.SURVIVAL);
        player.getInventory().clearContent();
        Vec3 at = helper.absoluteVec(new Vec3(4.5, 1, 1.5));
        player.snapTo(at.x, at.y, at.z, 0.0f, 0.0f);
        return player;
    }

    private static SoldierEntity soldier(GameTestHelper helper, SoldierType type, Vec3 local, ServerPlayer owner) {
        ServerLevel level = helper.getLevel();
        SoldierEntity soldier = ModEntities.SOLDIERS.get(type).get().create(level, EntitySpawnReason.COMMAND);
        Vec3 at = helper.absoluteVec(local);
        soldier.snapTo(at.x, at.y, at.z, 0.0f, 0.0f);
        soldier.finalizeSpawn(level, level.getCurrentDifficultyAt(soldier.blockPosition()), EntitySpawnReason.COMMAND, null);
        if (owner != null) {
            soldier.setOwner(owner);
        }
        soldier.setNoAi(true);
        level.addFreshEntity(soldier);
        return soldier;
    }
}
