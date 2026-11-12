package io.github.sirschrolli.kingdomsatwar.gametest;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.block.sea.KrakenCoreBlockEntity;
import io.github.sirschrolli.kingdomsatwar.entity.glitch.AnniTrollEntity;
import io.github.sirschrolli.kingdomsatwar.entity.sea.KrakenEntity;
import io.github.sirschrolli.kingdomsatwar.entity.sea.KrakenTentacleEntity;
import io.github.sirschrolli.kingdomsatwar.entity.sea.SharkEntity;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierType;
import io.github.sirschrolli.kingdomsatwar.fishing.Medium;
import io.github.sirschrolli.kingdomsatwar.fishing.RodType;
import io.github.sirschrolli.kingdomsatwar.registry.FishingItems;
import io.github.sirschrolli.kingdomsatwar.registry.ModBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.ModTags;
import io.github.sirschrolli.kingdomsatwar.registry.SeaBlocks;
import io.github.sirschrolli.kingdomsatwar.urmeer.UrmeerBiomes;
import io.github.sirschrolli.kingdomsatwar.worldgen.kraken.KrakenMawLocator;
import io.github.sirschrolli.kingdomsatwar.worldgen.kraken.KrakenMawPiece;
import io.github.sirschrolli.kingdomsatwar.worldgen.kraken.KrakenMawPlacement;
import io.github.sirschrolli.kingdomsatwar.worldgen.lair.ModStructures;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * Tests des Kraken (0.91): der Kampf (Emporsteigen, sieben Angriffe, Befreiung aus dem Würgegriff, Wut, Tod, Beute,
 * Rückruf), der Krakenschlund als Bauwerk des Urmeers und die Krakenangel.
 */
final class KrakenGameTests {
    private static final Identifier ARENA = KingdomsAtWar.id("test_arena");

    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> KRAKEN_FIGHT =
            ModGameTests.TEST_FUNCTIONS.register("kraken_fight", () -> KrakenGameTests::krakenFight);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> KRAKEN_MAW =
            ModGameTests.TEST_FUNCTIONS.register("kraken_maw", () -> KrakenGameTests::krakenMaw);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> KRAKEN_SWIM =
            ModGameTests.TEST_FUNCTIONS.register("kraken_swim", () -> KrakenGameTests::krakenSwim);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> KRAKEN_ROD =
            ModGameTests.TEST_FUNCTIONS.register("kraken_rod", () -> KrakenGameTests::krakenRod);

    private KrakenGameTests() {}

    /** Lädt die Klasse (meldet die Testfunktionen an). */
    static void init() {}

    static void register(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment) {
        event.registerTest(KRAKEN_ROD.getId(), new FunctionGameTestInstance(KRAKEN_ROD.key(),
                new TestData<>(environment, Level.OVERWORLD, ARENA, 200, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
        // der Ausschnitt des Schlunds ragt über den Testplatz hinaus
        event.registerTest(KRAKEN_MAW.getId(), new FunctionGameTestInstance(KRAKEN_MAW.key(),
                new TestData<>(environment, Level.OVERWORLD, ARENA, 200, 0, true, Rotation.NONE, false, 1, 1, true, 16)));
        // seine Angriffe reichen weit (Flutwelle 30 Blöcke) – Abstand zu den anderen Tests; und der Kampf dauert
        event.registerTest(KRAKEN_FIGHT.getId(), new FunctionGameTestInstance(KRAKEN_FIGHT.key(),
                new TestData<>(environment, Level.OVERWORLD, ARENA, 2600, 0, true, Rotation.NONE, false, 1, 1, true, 40)));
        // er schwimmt bis zu 18 Blöcke um die Mitte – Abstand zu den anderen Tests
        event.registerTest(KRAKEN_SWIM.getId(), new FunctionGameTestInstance(KRAKEN_SWIM.key(),
                new TestData<>(environment, Level.OVERWORLD, ARENA, 400, 0, true, Rotation.NONE, false, 1, 1, true, 30)));
    }

    private static JsonObject lang(String name) {
        try (InputStream in = KrakenGameTests.class.getResourceAsStream("/assets/kingdomsatwar/lang/" + name + ".json")) {
            return in == null ? new JsonObject() : JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
        } catch (java.io.IOException e) {
            return new JsonObject();
        }
    }

    /** Hält die Chunks um {@code c} geladen (der Kraken schwimmt weit über den Testplatz hinaus) – oder gibt sie wieder frei. */
    private static void forceArea(ServerLevel level, BlockPos c, int blocks, boolean on) {
        for (int cx = (c.getX() - blocks) >> 4; cx <= (c.getX() + blocks) >> 4; cx++) {
            for (int cz = (c.getZ() - blocks) >> 4; cz <= (c.getZ() + blocks) >> 4; cz++) {
                level.setChunkForced(cx, cz, on);
            }
        }
    }

    private static void heal(LivingEntity e) {
        e.setHealth(e.getMaxHealth());
        e.setInvulnerableTime(0);
        e.hurtTime = 0;
    }

    /** Heilt den Ritter und stellt ihn zurück auf seinen Platz (die Angriffe schleudern ihn weit fort). */
    private static void reset(GameTestHelper helper, LivingEntity knight) {
        heal(knight);
        net.minecraft.world.phys.Vec3 home = helper.absoluteVec(new net.minecraft.world.phys.Vec3(4.5, 1.0, 1.5));
        knight.teleportTo(home.x, home.y, home.z);
        knight.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
        knight.fallDistance = 0.0f;
    }

    /**
     * Der Kampf: Das Herz des Schlunds weckt den Kraken; er steigt empor (tief unten unverwundbar), dann treffen
     * Tentakelschlag, Armfeger, Würgegriff (wer ihn hart trifft, kommt frei; sonst beißt er zu), Tintenwolke, Strudel, Ruf der
     * Tiefe (Haie) und Flutwelle; schwer verletzt wird er wütend. Besiegt sinkt er, lässt die Krakenangel fallen, die Haie
     * ziehen ab, der Schlund ist befreit – und das Gezeitenhorn ruft ihn zurück.
     */
    private static void krakenFight(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        for (int x = 0; x < 9; x++) {
            for (int z = 0; z < 9; z++) {
                helper.setBlock(x, 0, z, ModBlocks.ABYSSAL_SAND.get());
            }
        }
        helper.assertTrue(ModEntities.KRAKEN.get().getCategory() == MobCategory.MONSTER, "Der Kraken: falsche Gruppe");
        // knapp unter Anni dem Troll
        helper.assertTrue(KrakenEntity.MAX_HEALTH < AnniTrollEntity.MAX_HEALTH && KrakenEntity.MAX_HEALTH >= AnniTrollEntity.MAX_HEALTH * 0.9,
                "Der Kraken ist nicht knapp unter Anni: " + KrakenEntity.MAX_HEALTH);
        var krakenAttributes = KrakenEntity.createAttributes().build();
        var anniAttributes = AnniTrollEntity.createAttributes().build();
        helper.assertTrue(krakenAttributes.getBaseValue(Attributes.ATTACK_DAMAGE) < anniAttributes.getBaseValue(Attributes.ATTACK_DAMAGE)
                && krakenAttributes.getBaseValue(Attributes.ARMOR) <= anniAttributes.getBaseValue(Attributes.ARMOR), "Der Kraken ist stärker als Anni");
        helper.assertTrue(level.getServer().reloadableRegistries().getLootTable(ModEntities.KRAKEN.get().getDefaultLootTable().orElseThrow())
                != LootTable.EMPTY, "Der Kraken hat keine Beute");
        helper.assertFalse(new ItemStack(SeaBlocks.KRAKEN_SPAWN_EGG.get()).isEmpty(), "Kein Spawn-Ei des Kraken");
        // er ragt am Rand des Testplatzes empor (sein Leib reicht darüber hinaus), der Ritter steht sieben Blöcke vor ihm –
        // beide auf dem Platz (dessen Chunks sicher geladen sind)
        BlockPos center = helper.absolutePos(new BlockPos(4, 1, 8));
        BlockPos corePos = helper.absolutePos(new BlockPos(4, -3, 8));
        forceArea(level, center, 24, true);
        level.setBlock(corePos, SeaBlocks.KRAKEN_CORE.get().defaultBlockState(), Block.UPDATE_ALL);
        if (!(level.getBlockEntity(corePos) instanceof KrakenCoreBlockEntity core)) {
            helper.fail("Das Herz des Schlunds hat keine Blockentität");
            return;
        }
        core.setup(center);
        helper.assertTrue(core.phase() == KrakenCoreBlockEntity.Phase.DORMANT, "Der Schlund schläft nicht");
        helper.assertTrue(core.wakes(center.getX() + 20.5, center.getY(), center.getZ() + 0.5), "Im Felsenring weckt man ihn nicht");
        helper.assertFalse(core.wakes(center.getX() + 45.5, center.getY(), center.getZ() + 0.5), "Weit draußen weckt man ihn");
        KrakenEntity kraken = core.awaken(level);
        if (kraken == null) {
            helper.fail("Das Herz weckt den Kraken nicht");
            return;
        }
        helper.assertTrue(core.phase() == KrakenCoreBlockEntity.Phase.AWAKE, "Das Herz merkt das Erwachen nicht");
        helper.assertTrue(kraken.isRising() && kraken.sink() > 0.9f, "Der Kraken steigt nicht aus der Tiefe");
        float before = kraken.getHealth();
        kraken.hurtServer(level, level.damageSources().generic(), 40.0f);
        helper.assertTrue(kraken.getHealth() >= before, "Tief unten ist der Kraken verwundbar");

        var knight = helper.spawnWithNoFreeWill(ModEntities.SOLDIERS.get(SoldierType.KNIGHT).get(), new BlockPos(4, 1, 1));
        // ohne eigene Bewegung: die Schläge schleudern ihn nicht vom Platz (Treffer und Schaden zählen trotzdem)
        knight.setNoAi(true);
        // ein zäher Ritter, damit er alle Angriffe übersteht
        knight.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000.0);
        reset(helper, knight);
        float[] hp = new float[1];
        net.minecraft.world.phys.Vec3[] start = new net.minecraft.world.phys.Vec3[1];
        helper.startSequence()
                .thenExecuteAfter(KrakenEntity.RISE_TICKS + 4, () -> {
                    helper.assertFalse(kraken.isRising(), "Der Kraken steigt nicht ganz empor");
                    helper.assertTrue(kraken.sink() < 0.01f, "Der Kraken bleibt halb versunken: " + kraken.sink());
                    helper.assertFalse(kraken.isInvulnerableTo(level, level.damageSources().generic()), "Oben ist der Kraken unverwundbar");
                    reset(helper, knight);
                    hp[0] = knight.getHealth();
                    kraken.forceAttack(level, "slam", knight, true);
                })
                .thenExecuteAfter(KrakenEntity.SLAM_HIT + 2, () ->
                        helper.assertTrue(knight.getHealth() < hp[0], "Der Tentakelschlag trifft nicht (Ritter " + knight.position() + " "
                                + knight.getHealth() + "/" + hp[0] + ", Kraken " + kraken.position() + " " + kraken.currentAttack() + ")"))
                .thenExecuteAfter(KrakenEntity.SLAM_TICKS - KrakenEntity.SLAM_HIT, () -> {
                    helper.assertTrue("none".equals(kraken.currentAttack()), "Der Tentakelschlag endet nicht: " + kraken.currentAttack());
                    reset(helper, knight);
                    hp[0] = knight.getHealth();
                    kraken.forceAttack(level, "sweep", knight, true);
                })
                .thenExecuteAfter(KrakenEntity.SWEEP_TO + 2, () -> helper.assertTrue(knight.getHealth() < hp[0], "Der Armfeger trifft nicht"))
                .thenExecuteAfter(KrakenEntity.SWEEP_TICKS - KrakenEntity.SWEEP_TO, () -> {
                    reset(helper, knight);
                    kraken.forceAttack(level, "grab", knight, true);
                })
                .thenExecuteAfter(KrakenEntity.GRAB_CATCH + 4, () -> {
                    helper.assertTrue(knight.getUUID().equals(kraken.grabbedId()), "Der Würgegriff packt nicht");
                    // wer ihn hart genug trifft, befreit die Beute
                    kraken.setInvulnerableTime(0);
                    kraken.hurtServer(level, level.damageSources().generic(), KrakenEntity.GRAB_BREAK + 6.0f);
                })
                .thenExecuteAfter(3, () -> {
                    helper.assertTrue(kraken.grabbedId() == null, "Der Kraken lässt trotz harter Treffer nicht los");
                    helper.assertTrue("none".equals(kraken.currentAttack()), "Nach der Befreiung greift er weiter: " + kraken.currentAttack());
                    reset(helper, knight);
                    hp[0] = knight.getHealth();
                    kraken.forceAttack(level, "grab", knight, true);
                })
                .thenExecuteAfter(KrakenEntity.GRAB_BITE + 2, () -> {
                    helper.assertTrue(kraken.grabbedId() == null, "Nach dem Biss hält er die Beute weiter fest");
                    helper.assertTrue(knight.getHealth() < hp[0] - 15.0f, "Der Biss trifft nicht: " + (hp[0] - knight.getHealth()));
                })
                .thenExecuteAfter(KrakenEntity.GRAB_TICKS - KrakenEntity.GRAB_BITE, () -> {
                    reset(helper, knight);
                    kraken.forceAttack(level, "ink", knight, true);
                })
                .thenExecuteAfter(KrakenEntity.INK_SPIT + 2, () -> {
                    List<AreaEffectCloud> clouds = level.getEntitiesOfClass(AreaEffectCloud.class, knight.getBoundingBox().inflate(4.0),
                            c -> c.getOwner() == kraken);
                    helper.assertFalse(clouds.isEmpty(), "Keine Tintenwolke beim Ziel");
                })
                .thenExecuteAfter(KrakenEntity.INK_TICKS - KrakenEntity.INK_SPIT, () -> {
                    reset(helper, knight);
                    hp[0] = knight.getHealth();
                    kraken.forceAttack(level, "whirl", knight, true);
                })
                .thenExecuteAfter(KrakenEntity.WHIRL_FROM + 6, () ->
                        helper.assertTrue("whirl".equals(kraken.currentAttack()), "Kein Strudel: " + kraken.currentAttack()))
                .thenExecuteAfter(KrakenEntity.WHIRL_BURST - KrakenEntity.WHIRL_FROM - 4, () ->
                        helper.assertTrue(knight.getHealth() < hp[0], "Die Wassersäule des Strudels trifft nicht"))
                .thenExecuteAfter(KrakenEntity.WHIRL_TICKS - KrakenEntity.WHIRL_BURST, () -> {
                    reset(helper, knight);
                    kraken.forceAttack(level, "call", knight, true);
                })
                .thenExecuteAfter(30, () -> {
                    int sharks = level.getEntitiesOfClass(SharkEntity.class, new AABB(center).inflate(30.0), Entity::isAlive).size();
                    helper.assertTrue(sharks >= 2, "Der Ruf der Tiefe bringt keine Haie: " + sharks);
                })
                .thenExecuteAfter(KrakenEntity.CALL_TICKS - 30, () -> {
                    reset(helper, knight);
                    hp[0] = knight.getHealth();
                    kraken.forceAttack(level, "wave", knight, true);
                })
                .thenExecuteAfter(KrakenEntity.WAVE_SLAM + 10, () -> helper.assertTrue(knight.getHealth() < hp[0], "Die Flutwelle trifft nicht"))
                .thenExecuteAfter(KrakenEntity.WAVE_TICKS - KrakenEntity.WAVE_SLAM, () -> {
                    // Rammstoß: er schießt durch den Ritter hindurch
                    reset(helper, knight);
                    hp[0] = knight.getHealth();
                    start[0] = kraken.position();
                    kraken.forceAttack(level, "charge", knight, true);
                })
                .thenExecuteAfter(KrakenEntity.CHARGE_WINDUP - 2, () ->
                        helper.assertTrue(kraken.position().distanceTo(start[0]) < 1.0, "Beim Ausholen zum Rammstoß schwimmt er schon los"))
                .thenExecuteAfter(KrakenEntity.CHARGE_DASH + 4, () -> {
                    double moved = kraken.position().distanceTo(start[0]);
                    helper.assertTrue(moved > 8.0, "Der Rammstoß trägt ihn nicht weit: " + moved);
                    helper.assertTrue(knight.getHealth() < hp[0], "Der Rammstoß trifft nicht (Ritter " + knight.position() + ", Kraken "
                            + kraken.position() + ")");
                })
                .thenExecuteAfter(KrakenEntity.CHARGE_TICKS - KrakenEntity.CHARGE_WINDUP - KrakenEntity.CHARGE_DASH, () -> {
                    helper.assertTrue("none".equals(kraken.currentAttack()), "Der Rammstoß endet nicht: " + kraken.currentAttack());
                    // Ausbruch aus der Tiefe: abtauchen (unverwundbar), unter Wasser zum Ziel, hervorbrechen
                    kraken.debugReturnHome();
                    reset(helper, knight);
                    hp[0] = knight.getHealth();
                    kraken.forceAttack(level, "breach", knight, true);
                })
                .thenExecuteAfter(KrakenEntity.BREACH_DIVE + 6, () -> {
                    helper.assertTrue(kraken.sink() > 0.9f, "Er taucht nicht ab: " + kraken.sink());
                    helper.assertTrue(kraken.isInvulnerableTo(level, level.damageSources().generic()), "Abgetaucht ist er verwundbar");
                    helper.assertFalse(kraken.isPickable(), "Abgetaucht kann man ihn treffen");
                })
                .thenExecuteAfter(KrakenEntity.BREACH_WARN - KrakenEntity.BREACH_DIVE - 6, () -> {
                    double d = Math.sqrt(Math.pow(kraken.getX() - knight.getX(), 2) + Math.pow(kraken.getZ() - knight.getZ(), 2));
                    helper.assertTrue(d < 3.0, "Unter Wasser schwimmt er nicht zum Ziel: " + d);
                })
                .thenExecuteAfter(KrakenEntity.BREACH_ERUPT - KrakenEntity.BREACH_WARN + 3, () -> {
                    helper.assertTrue(knight.getHealth() < hp[0] - 15.0f, "Der Ausbruch trifft nicht: " + (hp[0] - knight.getHealth()));
                    helper.assertTrue(kraken.sink() < 0.0f, "Er bricht nicht über das Wasser hinaus: " + kraken.sink());
                    helper.assertFalse(kraken.isInvulnerableTo(level, level.damageSources().generic()), "Nach dem Ausbruch bleibt er unverwundbar");
                })
                .thenExecuteAfter(KrakenEntity.BREACH_TICKS - KrakenEntity.BREACH_ERUPT - 3, () -> {
                    helper.assertTrue("none".equals(kraken.currentAttack()) && Math.abs(kraken.sink()) < 0.01f,
                            "Nach dem Ausbruch kommt er nicht zur Ruhe: " + kraken.currentAttack() + " " + kraken.sink());
                    // Geysirfeld: zwei Felder nacheinander, das erste genau unter dem Ziel
                    kraken.debugReturnHome();
                    reset(helper, knight);
                    hp[0] = knight.getHealth();
                    kraken.forceAttack(level, "geyser", knight, true);
                })
                .thenExecuteAfter(KrakenEntity.GEYSER_FIRST + KrakenEntity.GEYSER_WARN - 4, () ->
                        helper.assertTrue(knight.getHealth() >= hp[0], "Der Geysir trifft vor der Zeit"))
                .thenExecuteAfter(6, () -> {
                    helper.assertTrue(knight.getHealth() < hp[0], "Der Geysir trifft nicht");
                    reset(helper, knight);
                    hp[0] = knight.getHealth();
                })
                .thenExecuteAfter(KrakenEntity.GEYSER_SECOND - KrakenEntity.GEYSER_FIRST + 2, () ->
                        helper.assertTrue(knight.getHealth() < hp[0], "Das zweite Geysirfeld trifft nicht"))
                .thenExecuteAfter(KrakenEntity.GEYSER_TICKS - KrakenEntity.GEYSER_SECOND - KrakenEntity.GEYSER_WARN, () -> {
                    // Tentakelkäfig: sieben Arme um den Ritter, sie ziehen sich zusammen und schlagen zu
                    reset(helper, knight);
                    kraken.forceAttack(level, "cage", knight, true);
                })
                .thenExecuteAfter(KrakenEntity.CAGE_SPAWN + KrakenTentacleEntity.RISE, () -> {
                    List<KrakenTentacleEntity> arms = level.getEntitiesOfClass(KrakenTentacleEntity.class, knight.getBoundingBox().inflate(14.0),
                            Entity::isAlive);
                    helper.assertTrue(arms.size() == KrakenEntity.CAGE_ARMS - 1, "Der Käfig hat nicht sieben Arme: " + arms.size());
                    for (KrakenTentacleEntity arm : arms) {
                        double r = Math.sqrt(Math.pow(arm.getX() - knight.getX(), 2) + Math.pow(arm.getZ() - knight.getZ(), 2));
                        helper.assertTrue(r > KrakenTentacleEntity.END_RADIUS + 1.0 && r < KrakenEntity.CAGE_RADIUS + 0.5,
                                "Ein Arm steht nicht im Kreis um das Ziel: " + r);
                    }
                    // ein harter Schlag trennt einen Arm ab – der Kraken spürt es
                    float krakenBefore = kraken.getHealth();
                    KrakenTentacleEntity cut = arms.get(0);
                    cut.hurtServer(level, level.damageSources().generic(), KrakenTentacleEntity.CUT_DAMAGE + 4.0f);
                    helper.assertTrue(cut.isCut(), "Ein harter Schlag trennt den Arm nicht ab");
                    helper.assertTrue(kraken.getHealth() < krakenBefore, "Der Kraken spürt den abgeschlagenen Arm nicht");
                    reset(helper, knight);
                    hp[0] = knight.getHealth();
                })
                .thenExecuteAfter(KrakenTentacleEntity.CLOSE_TO - KrakenTentacleEntity.RISE, () -> {
                    for (KrakenTentacleEntity arm : level.getEntitiesOfClass(KrakenTentacleEntity.class, knight.getBoundingBox().inflate(14.0),
                            a -> a.isAlive() && !a.isCut())) {
                        double r = Math.sqrt(Math.pow(arm.getX() - arm.cageCenter().x, 2) + Math.pow(arm.getZ() - arm.cageCenter().z, 2));
                        helper.assertTrue(r < KrakenTentacleEntity.END_RADIUS + 0.5, "Der Käfig zieht sich nicht zusammen: " + r);
                    }
                    reset(helper, knight);
                    hp[0] = knight.getHealth();
                })
                .thenExecuteAfter(KrakenTentacleEntity.SLAM - KrakenTentacleEntity.CLOSE_TO + 2, () ->
                        helper.assertTrue(knight.getHealth() < hp[0] - 10.0f, "Der Käfig zermalmt nicht: " + (hp[0] - knight.getHealth())))
                .thenExecuteAfter(KrakenEntity.CAGE_TICKS - KrakenEntity.CAGE_SPAWN - KrakenTentacleEntity.SLAM, () -> {
                    helper.assertTrue("none".equals(kraken.currentAttack()), "Der Käfig endet nicht: " + kraken.currentAttack());
                    helper.assertTrue(level.getEntitiesOfClass(KrakenTentacleEntity.class, knight.getBoundingBox().inflate(20.0), Entity::isAlive).isEmpty(),
                            "Die Arme des Käfigs bleiben");
                    kraken.debugReturnHome();
                    // schwer verletzt: Wut
                    kraken.setHealth(kraken.getMaxHealth() * 0.3f);
                })
                .thenExecuteAfter(2, () -> {
                    helper.assertTrue(kraken.isEnraged(), "Schwer verletzt wird der Kraken nicht wütend");
                    kraken.setHealth(1.0f);
                    kraken.setInvulnerableTime(0);
                    BossLootGameTests.firstVictory(helper, kraken, 50.0f);
                    helper.assertTrue(kraken.isDeadOrDying(), "Der Kraken fällt nicht");
                })
                .thenExecuteAfter(KrakenEntity.DEATH_TICKS + 8, () -> {
                    helper.assertTrue(core.phase() == KrakenCoreBlockEntity.Phase.DEFEATED, "Der Schlund merkt den Sieg nicht");
                    AABB area = new AABB(center).inflate(40.0);
                    helper.assertFalse(level.getEntitiesOfClass(ItemEntity.class, area, e -> e.getItem().is(FishingItems.rod(RodType.KRAKEN))).isEmpty(),
                            "Der Kraken lässt die Krakenangel nicht fallen");
                    helper.assertTrue(level.getEntitiesOfClass(SharkEntity.class, area, Entity::isAlive).isEmpty(), "Die Haie bleiben nach seinem Tod");
                    helper.assertTrue(level.getEntitiesOfClass(KrakenEntity.class, area, Entity::isAlive).isEmpty(), "Der Kraken bleibt nach dem Tod");
                    // das Gezeitenhorn ruft ihn zurück
                    helper.assertTrue(core.recall(level), "Der Besiegte lässt sich nicht zurückrufen");
                    helper.assertTrue(core.phase() == KrakenCoreBlockEntity.Phase.AWAKE, "Nach dem Rückruf ist der Schlund nicht wach");
                    helper.assertFalse(core.recall(level), "Ein wacher Kraken lässt sich ein zweites Mal rufen");
                    core.reset(level);
                    helper.assertTrue(core.phase() == KrakenCoreBlockEntity.Phase.DORMANT, "Der Schlund schläft nach dem Zurücksetzen nicht");
                })
                .thenExecuteAfter(1, () -> {
                    helper.assertTrue(level.getEntitiesOfClass(KrakenEntity.class, new AABB(center).inflate(40.0), Entity::isAlive).isEmpty(),
                            "Nach dem Zurücksetzen bleibt ein Kraken");
                    forceArea(level, center, 24, false);
                })
                .thenSucceed();
    }

    /**
     * Er schwimmt (seit 1.00): um sein Ziel herum, aber nie weiter als {@link KrakenEntity#SWIM_RADIUS} Blöcke von der Mitte,
     * nicht schneller als {@link KrakenEntity#SWIM_SPEED}, den Blick in Fahrtrichtung; auf Befehl schwimmend greift er nicht an.
     */
    private static void krakenSwim(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        for (int x = 0; x < 9; x++) {
            for (int z = 0; z < 9; z++) {
                helper.setBlock(x, 0, z, ModBlocks.ABYSSAL_SAND.get());
            }
        }
        BlockPos center = helper.absolutePos(new BlockPos(4, 1, 4));
        forceArea(level, center, 26, true);
        KrakenEntity kraken = ModEntities.KRAKEN.get().create(level, net.minecraft.world.entity.EntitySpawnReason.MOB_SUMMONED);
        if (kraken == null) {
            helper.fail("Kein Kraken");
            return;
        }
        kraken.snapTo(center.getX() + 0.5, center.getY(), center.getZ() + 0.5, 0.0f, 0.0f);
        kraken.bindTo(center, null);
        level.addFreshEntity(kraken);
        var knight = helper.spawnWithNoFreeWill(ModEntities.SOLDIERS.get(SoldierType.KNIGHT).get(), new BlockPos(1, 1, 1));
        knight.setNoAi(true);
        kraken.forceSwim(knight, true);
        double[] most = new double[2];
        int[] count = new int[2];
        net.minecraft.world.phys.Vec3 c = net.minecraft.world.phys.Vec3.atBottomCenterOf(center);
        helper.onEachTick(() -> {
            if (!kraken.isAlive()) {
                return;
            }
            double r = Math.sqrt(Math.pow(kraken.getX() - c.x, 2) + Math.pow(kraken.getZ() - c.z, 2));
            most[0] = Math.max(most[0], r);
            double speed = kraken.swimSpeed();
            most[1] = Math.max(most[1], speed);
            double dx = kraken.getX() - kraken.xo;
            double dz = kraken.getZ() - kraken.zo;
            if (speed > 0.09 && dx * dx + dz * dz > 0.005) {
                count[0]++;
                float moveYaw = -((float) net.minecraft.util.Mth.atan2(dx, dz)) * net.minecraft.util.Mth.RAD_TO_DEG;
                if (Math.abs(net.minecraft.util.Mth.wrapDegrees(moveYaw - kraken.getYRot())) < 40.0f) {
                    count[1]++;
                }
            }
        });
        helper.runAfterDelay(300, () -> {
            helper.assertTrue(most[0] > 5.0, "Der Kraken schwimmt nicht umher: höchstens " + most[0] + " Blöcke von der Mitte");
            helper.assertTrue(most[0] <= KrakenEntity.SWIM_RADIUS + 0.01, "Der Kraken schwimmt über seinen Bereich hinaus: " + most[0]);
            helper.assertTrue(most[1] > KrakenEntity.SWIM_SPEED * 0.7 && most[1] <= KrakenEntity.SWIM_SPEED + 0.005,
                    "Falsches Tempo: " + most[1]);
            helper.assertTrue(count[0] > 60, "Der Kraken macht kaum Fahrt: " + count[0]);
            helper.assertTrue(count[1] >= count[0] * 0.75, "Der Kraken schwimmt seitwärts: " + count[1] + "/" + count[0]);
            helper.assertTrue("none".equals(kraken.currentAttack()), "Beim Schwimmen auf Befehl greift er an: " + kraken.currentAttack());
            kraken.discard();
            forceArea(level, center, 26, false);
            helper.succeed();
        });
    }

    /**
     * Der Krakenschlund: Bauwerk und Platzierung angemeldet, nur im tiefen Urmeer, eine Truhe mit Beute; ein Ausschnitt
     * vom Boden des Schlunds (gebaut wie beim Erzeugen der Welt): ganz unten das Herz mit dem Schlund als Mitte, darüber
     * Wasser, ein Ring aus dunklem Prismarin, ringsum die Trichterwand aus Abgrundstein.
     */
    private static void krakenMaw(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        var registries = level.getServer().registryAccess();
        helper.assertTrue(registries.lookupOrThrow(Registries.STRUCTURE).get(ModStructures.KRAKEN_MAW_STRUCTURE).isPresent(), "Das Bauwerk fehlt");
        var set = registries.lookupOrThrow(Registries.STRUCTURE_SET).get(ModStructures.KRAKEN_MAW_SET);
        helper.assertTrue(set.isPresent() && set.get().value().placement() instanceof KrakenMawPlacement, "Die Platzierung des Schlunds fehlt");
        var biomes = registries.lookupOrThrow(Registries.BIOME);
        helper.assertTrue(biomes.getOrThrow(UrmeerBiomes.DEEP).is(ModTags.Biomes.HAS_KRAKEN_MAW), "Kein Schlund im tiefen Urmeer");
        for (var no : List.of(Biomes.DEEP_OCEAN, Biomes.WARM_OCEAN, Biomes.PLAINS)) {
            helper.assertFalse(biomes.getOrThrow(no).is(ModTags.Biomes.HAS_KRAKEN_MAW), "Ein Schlund in " + no.identifier());
        }
        helper.assertTrue(level.getServer().reloadableRegistries().getLootTable(KrakenMawPiece.CHEST) != LootTable.EMPTY,
                "Die Truhe im Wrack hat keine Beute");

        // der Boden des Schlunds liegt auf Höhe des Testplatzes
        BlockPos center0 = helper.absolutePos(new BlockPos(4, 1, 4));
        int sea = center0.getY() + KrakenMawPiece.MAW_DEPTH;
        BlockPos center = new BlockPos(center0.getX(), sea, center0.getZ());
        BlockPos corePos = KrakenMawLocator.core(center);
        helper.assertTrue(corePos.getY() == center0.getY(), "Das Herz liegt nicht am Boden des Schlunds");
        BoundingBox box = new BoundingBox(center.getX() - 9, corePos.getY() - 3, center.getZ() - 9, center.getX() + 9, corePos.getY() + 12,
                center.getZ() + 9);
        new KrakenMawPiece(center, 4242L, sea).postProcess(level, level.structureManager(), level.getChunkSource().getGenerator(),
                RandomSource.create(7), box, net.minecraft.world.level.ChunkPos.containing(center), center);
        helper.assertTrue(level.getBlockState(corePos).is(SeaBlocks.KRAKEN_CORE.get()), "Kein Herz am Boden des Schlunds");
        helper.assertTrue(level.getBlockEntity(corePos) instanceof KrakenCoreBlockEntity heart && heart.center().equals(center)
                && heart.phase() == KrakenCoreBlockEntity.Phase.DORMANT, "Das Herz kennt die Mitte des Schlunds nicht");
        helper.assertTrue(level.getBlockState(corePos.above()).is(Blocks.WATER), "Über dem Herz ist kein Wasser");
        helper.assertTrue(level.getBlockState(corePos.east(2)).is(Blocks.DARK_PRISMARINE), "Kein Ring aus dunklem Prismarin um das Herz");
        helper.assertTrue(level.getBlockState(corePos.offset(3, 2, 0)).is(Blocks.WATER), "Der Schlund ist unten nicht offen");
        int wall = 0;
        for (int x = box.minX(); x <= box.maxX(); x++) {
            for (int z = box.minZ(); z <= box.maxZ(); z++) {
                for (int y = corePos.getY() + 1; y <= box.maxY(); y++) {
                    BlockState s = level.getBlockState(new BlockPos(x, y, z));
                    if (s.is(ModBlocks.ABYSSAL_STONE.base().get()) || s.is(Blocks.BLACKSTONE) || s.is(Blocks.SEA_LANTERN)) {
                        wall++;
                    }
                }
            }
        }
        helper.assertTrue(wall > 300, "Keine Trichterwand aus Abgrundstein: " + wall);
        helper.assertTrue(level.getBlockState(corePos.offset(8, 2, 0)).isSolidRender(), "Der Trichter hat außen keine Wand");
        helper.succeed();
    }

    /**
     * Die Krakenangel: episch, feuerfest, langlebig (länger als die Drachenangel), ohne Rezept, mit Prismarinkristallen
     * auszubessern; die stärkste im Wasser (schnellerer Biss, größere Fangzone, mehr Glück) und unter Wasser und im Urmeer
     * noch stärker; auf dem Trockenen (Lava, Leere) nur ein wenig. Dazu Namen, Texte und Klänge des Kraken.
     */
    private static void krakenRod(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        RodType kraken = RodType.KRAKEN;
        ItemStack rod = new ItemStack(FishingItems.rod(kraken));
        helper.assertTrue(rod.getRarity() == Rarity.EPIC, "Die Krakenangel ist nicht episch");
        helper.assertTrue(rod.has(DataComponents.DAMAGE_RESISTANT), "Die Krakenangel verbrennt");
        helper.assertTrue(rod.getMaxDamage() > RodType.DRAGON.durability(), "Die Krakenangel hält weniger als die Drachenangel");
        helper.assertFalse(kraken.craftable(), "Die Krakenangel hat ein Rezept");
        helper.assertTrue(rod.isValidRepairItem(new ItemStack(Items.PRISMARINE_CRYSTALS)), "Prismarinkristalle bessern die Krakenangel nicht aus");
        var water = new RodType.Place(Medium.WATER, false, false);
        var sea = new RodType.Place(Medium.WATER, false, false, true);
        var lava = new RodType.Place(Medium.LAVA, false, false);
        for (RodType other : RodType.values()) {
            if (other != kraken && other != RodType.GLUT && other != RodType.STAR) {
                helper.assertTrue(kraken.haste(water) >= other.haste(water) && kraken.zone(water) >= other.zone(water)
                        && kraken.luck(water) >= other.luck(water), "Im Wasser ist " + other + " besser als die Krakenangel");
            }
        }
        helper.assertTrue(kraken.haste(sea) > kraken.haste(water) && kraken.zone(sea) > kraken.zone(water) && kraken.luck(sea) > kraken.luck(water),
                "Unter Wasser und im Urmeer ist die Krakenangel nicht stärker");
        helper.assertTrue(kraken.haste(lava) < kraken.haste(water) && RodType.GLUT.haste(lava) > kraken.haste(lava),
                "In der Lava ist die Krakenangel zu stark");
        BlockPos pos = helper.absolutePos(new BlockPos(4, 1, 4));
        helper.assertTrue(RodType.Place.of(level, pos, Medium.WATER, true).sea(), "Unter Wasser ausgeworfen zählt nicht als tief im Meer");
        helper.assertFalse(RodType.Place.of(level, pos, Medium.WATER, false).sea(), "Ein Teich in der Oberwelt zählt als Urmeer");
        // Namen und Texte
        for (JsonObject lang : List.of(lang("de_de"), lang("en_us"))) {
            for (String key : List.of("entity.kingdomsatwar.kraken", "boss.kingdomsatwar.kraken", "boss.kingdomsatwar.kraken.title",
                    "kraken.kingdomsatwar.warning", "item.kingdomsatwar.tide_horn.kraken_recalled", "block.kingdomsatwar.kraken_core",
                    "item.kingdomsatwar.kraken_spawn_egg", "item.kingdomsatwar.kraken_fishing_rod", "entity.kingdomsatwar.kraken_tentacle")) {
                helper.assertTrue(lang.has(key), "Text fehlt: " + key);
            }
        }
        // die Klänge: jedes Ereignis mit Untertitel, die eigenen Aufnahmen liegen bei
        JsonObject sounds;
        try (InputStream in = KrakenGameTests.class.getResourceAsStream("/assets/kingdomsatwar/sounds.json")) {
            sounds = in == null ? new JsonObject() : JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
        } catch (java.io.IOException e) {
            sounds = new JsonObject();
        }
        JsonObject german = lang("de_de");
        for (String event : List.of("ambient", "hurt", "death", "rise", "roar", "wave", "sweep", "grab", "bite", "ink", "whirl", "slam", "swim", "charge",
                "dive", "breach", "geyser", "tentacle")) {
            String key = "entity.kraken." + event;
            helper.assertTrue(sounds.has(key), "Klang fehlt: " + key);
            helper.assertTrue(german.has("subtitles.kingdomsatwar." + key), "Untertitel fehlt: " + key);
        }
        for (String file : List.of("kraken_roar1", "kraken_roar2", "kraken_ambient1", "kraken_rise1", "kraken_grab1", "kraken_sweep1", "kraken_death1",
                "kraken_swim1", "kraken_charge1", "kraken_dive1", "kraken_breach1", "kraken_geyser1", "kraken_tentacle1")) {
            helper.assertTrue(KrakenGameTests.class.getResource("/assets/kingdomsatwar/sounds/entity/sea/" + file + ".ogg") != null,
                    "Aufnahme fehlt: " + file);
        }
        helper.assertTrue(level.getServer().reloadableRegistries().getLootTable(ModEntities.KRAKEN.get().getDefaultLootTable().orElseThrow())
                != LootTable.EMPTY, "Der Kraken hat keine Beute");
        helper.succeed();
    }
}
