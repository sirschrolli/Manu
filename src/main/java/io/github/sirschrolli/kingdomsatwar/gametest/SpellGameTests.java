package io.github.sirschrolli.kingdomsatwar.gametest;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import io.github.sirschrolli.kingdomsatwar.entity.boss.mage.MageBossEntity;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierEntity;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierType;
import io.github.sirschrolli.kingdomsatwar.magic.AetheritStaffItem;
import io.github.sirschrolli.kingdomsatwar.magic.KnownSpells;
import io.github.sirschrolli.kingdomsatwar.magic.Mana;
import io.github.sirschrolli.kingdomsatwar.magic.Spell;
import io.github.sirschrolli.kingdomsatwar.magic.StaffMastery;
import io.github.sirschrolli.kingdomsatwar.magic.fx.SpellFxEntity;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.ModItems;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
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
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * Tests der Meisterzauber und Fähigkeiten der Erzmagier und des Aetherit-Stabs: Jeder Schlag und jede Falle trifft einen
 * stillstehenden Soldaten; Zauberwirkungen lösen zur rechten Zeit aus; Steinhaut, Frostpanzer und Lebenssaft wirken;
 * der Stab ist unzerstörbar, beginnt als Lehrling und steigt mit Erfahrung auf.
 */
final class SpellGameTests {
    private static final Identifier ARENA = KingdomsAtWar.id("test_arena");

    private static final Map<DragonType, DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>>> SIGNATURES =
            new EnumMap<>(DragonType.class);

    static {
        for (DragonType element : DragonType.values()) {
            SIGNATURES.put(element, ModGameTests.TEST_FUNCTIONS.register("mage_signature_" + element.id(),
                    () -> helper -> signatures(helper, element)));
        }
    }

    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> PASSIVES =
            ModGameTests.TEST_FUNCTIONS.register("mage_passives", () -> SpellGameTests::passives);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> GIFTS =
            ModGameTests.TEST_FUNCTIONS.register("mage_gifts", () -> SpellGameTests::gifts);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> FX =
            ModGameTests.TEST_FUNCTIONS.register("spell_fx", () -> SpellGameTests::fx);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> STAFF =
            ModGameTests.TEST_FUNCTIONS.register("staff_mastery", () -> SpellGameTests::staff);

    private SpellGameTests() {}

    static void init() {}

    static void register(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment) {
        List<DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>>> tests = new ArrayList<>(SIGNATURES.values());
        tests.add(PASSIVES);
        tests.add(GIFTS);
        tests.add(FX);
        tests.add(STAFF);
        for (var test : tests) {
            event.registerTest(test.getId(), new FunctionGameTestInstance(test.key(),
                    new TestData<>(environment, Level.OVERWORLD, ARENA, 400, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
        }
    }

    private static void floor(GameTestHelper helper) {
        for (int x = 0; x < 9; x++) {
            for (int z = 0; z < 9; z++) {
                helper.setBlock(x, 0, z, Blocks.STONE);
            }
        }
    }

    private static MageBossEntity mage(GameTestHelper helper, DragonType element, BlockPos rel) {
        ServerLevel level = helper.getLevel();
        BlockPos at = helper.absolutePos(rel);
        MageBossEntity mage = ModEntities.MAGES.get(element).get().create(level, EntitySpawnReason.MOB_SUMMONED);
        helper.assertTrue(mage != null, "kein Erzmagier");
        mage.snapTo(at.getX() + 0.5, at.getY(), at.getZ() + 0.5, 0.0f, 0.0f);
        level.addFreshEntity(mage);
        return mage;
    }

    /** Ein zäher Soldat als Ziel, der sich nicht rührt (kein Schild, kein Ausweichen). */
    private static SoldierEntity dummy(GameTestHelper helper, BlockPos rel) {
        ServerLevel level = helper.getLevel();
        BlockPos at = helper.absolutePos(rel);
        SoldierEntity soldier = ModEntities.SOLDIERS.get(SoldierType.ARCHER).get().create(level, EntitySpawnReason.MOB_SUMMONED);
        helper.assertTrue(soldier != null, "kein Soldat");
        soldier.snapTo(at.getX() + 0.5, at.getY(), at.getZ() + 0.5, 180.0f, 0.0f);
        soldier.setNoAi(true);
        soldier.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(1.0);   // bleibt im Testfeld
        soldier.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000.0);
        soldier.setHealth(1000.0f);
        level.addFreshEntity(soldier);
        return soldier;
    }

    // =====================================================================================
    // Meisterzauber
    // =====================================================================================

    /** Schlag und Falle des Erzmagiers treffen einen Soldaten, der still im Saal steht. */
    private static void signatures(GameTestHelper helper, DragonType element) {
        ServerLevel level = helper.getLevel();
        floor(helper);
        MageBossEntity mage = mage(helper, element, new BlockPos(4, 1, 3));
        SoldierEntity target = dummy(helper, new BlockPos(4, 1, 7));
        mage.devOnlyEnemy(target);                  // Tests nebenan bleiben unberührt
        mage.devFight(level);
        mage.setNoAi(true);                         // nur der getestete Zauber, keine anderen Angriffe
        float[] health = new float[1];
        int[] tick = {0};
        int strike = MageBossEntity.signatureDuration(element, false);
        int trap = MageBossEntity.signatureDuration(element, true);
        helper.onEachTick(() -> {
            int t = tick[0]++;
            if (t < strike) {
                mage.devSignature(level, target, false, t);
            } else if (t >= strike + 60 && t < strike + 60 + trap) {
                mage.devSignature(level, target, true, t - strike - 60);
            }
        });
        helper.startSequence()
                .thenExecute(() -> health[0] = target.getHealth())
                .thenWaitUntil(() -> helper.assertTrue(target.getHealth() < health[0] || tick[0] > strike + 55,
                        element.id() + ": der Schlag trifft nicht"))
                .thenExecute(() -> {
                    helper.assertTrue(target.getHealth() < health[0], element.id() + ": der Schlag trifft nicht");
                    target.setHealth(target.getMaxHealth());
                    target.removeAllEffects();
                    health[0] = target.getHealth();
                })
                .thenWaitUntil(() -> helper.assertTrue(tick[0] > strike + 60 + 2, "warten"))
                .thenWaitUntil(() -> helper.assertTrue(target.getHealth() < health[0] || tick[0] > strike + 60 + trap + 120,
                        element.id() + ": die Falle trifft nicht"))
                .thenExecute(() -> {
                    helper.assertTrue(target.getHealth() < health[0], element.id() + ": die Falle trifft nicht");
                    // die Wirkungen räumen sich selbst auf
                    for (SpellFxEntity fx : level.getEntitiesOfClass(SpellFxEntity.class, new AABB(target.blockPosition()).inflate(40))) {
                        helper.assertTrue(fx.tickCount <= fx.life(), "Zauberwirkung bleibt über ihre Zeit hinaus");
                    }
                })
                .thenSucceed();
    }

    // =====================================================================================
    // Fähigkeiten
    // =====================================================================================

    /** Steinhaut dämpft Treffer, der Frostpanzer fängt sie ab und wächst nach, Sylvana heilt sich in Ruhe. */
    private static void passives(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        floor(helper);
        ServerPlayer player = MageGameTests.survivalPlayer(helper);
        player.getAbilities().invulnerable = true;
        BlockPos center = helper.absolutePos(new BlockPos(4, 1, 4));
        player.teleportTo(level, center.getX() + 0.5, center.getY(), center.getZ() + 0.5, java.util.Set.of(), 0.0f, 0.0f, true);
        MageBossEntity fire = mage(helper, DragonType.FIRE, new BlockPos(1, 1, 1));
        MageBossEntity earth = mage(helper, DragonType.EARTH, new BlockPos(7, 1, 1));
        MageBossEntity ice = mage(helper, DragonType.ICE, new BlockPos(1, 1, 7));
        MageBossEntity nature = mage(helper, DragonType.NATURE, new BlockPos(7, 1, 7));
        for (MageBossEntity m : List.of(fire, earth, ice, nature)) {
            m.devOnlyEnemy(player);
            m.devFight(level);
        }
        float[] health = new float[1];
        helper.startSequence()
                .thenWaitUntil(() -> helper.assertTrue(ice.wardShards() == MageBossEntity.WARD_SHARDS, "Isoldes Frostpanzer wächst nicht"))
                .thenExecute(() -> {
                    // Steinhaut: derselbe Hieb trifft Gorvan schwächer als Ignatius
                    float f0 = fire.getHealth();
                    float e0 = earth.getHealth();
                    fire.setInvulnerableTime(0);
                    earth.setInvulnerableTime(0);
                    fire.hurtServer(level, level.damageSources().playerAttack(player), 20.0f);
                    earth.hurtServer(level, level.damageSources().playerAttack(player), 20.0f);
                    float fireLoss = f0 - fire.getHealth();
                    float earthLoss = e0 - earth.getHealth();
                    helper.assertTrue(fireLoss > 0.0f && earthLoss < fireLoss * 0.9f,
                            "Steinhaut wirkt nicht (Feuer " + fireLoss + ", Erde " + earthLoss + ")");
                    // Frostpanzer: fängt den Hieb ab, Eisklingen zerbrechen
                    float i0 = ice.getHealth();
                    ice.setInvulnerableTime(0);
                    ice.hurtServer(level, level.damageSources().playerAttack(player), 10.0f);
                    helper.assertTrue(ice.getHealth() == i0 && ice.wardShards() < MageBossEntity.WARD_SHARDS,
                            "Frostpanzer fängt nichts ab (" + ice.wardShards() + " Klingen)");
                    // Sylvana heilt sich, wenn man sie in Ruhe lässt
                    nature.setHealth(nature.getMaxHealth() * 0.5f);
                    health[0] = nature.getHealth();
                })
                .thenWaitUntil(() -> helper.assertTrue(nature.getHealth() > health[0], "Sylvana heilt sich nicht"))
                .thenExecute(() -> {
                    for (MageBossEntity m : List.of(fire, earth, ice, nature)) {
                        m.discard();
                    }
                    level.getServer().getPlayerList().remove(player);
                })
                .thenSucceed();
    }

    /**
     * Die weiteren Fähigkeiten: Das Heilritual heilt – und bricht unter genug Schaden; Ignatius steht einmal aus der Asche
     * auf; Isolde heilt im Eiskokon, der sich zerschlagen lässt; Fulgors Blitzableiter stärkt ihn und trifft, wer nah
     * steht; Sylvanas Dornen stechen zurück; Gorvans Erdwacht packt den Schützen; Marenas Gegenwelle stößt fort.
     */
    private static void gifts(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        floor(helper);
        ServerPlayer player = MageGameTests.survivalPlayer(helper);
        player.connection.markClientLoaded();   // sonst ist er unverwundbar wie ein Spieler, der gerade erst die Welt lädt
        player.getAbilities().invulnerable = false;   // die Testwelt läuft im Kreativmodus
        player.getAbilities().instabuild = false;
        BlockPos corner = helper.absolutePos(new BlockPos(8, 1, 0));
        player.teleportTo(level, corner.getX() + 0.5, corner.getY(), corner.getZ() + 0.5, java.util.Set.of(), 0.0f, 0.0f, true);
        SoldierEntity dummy = dummy(helper, new BlockPos(4, 1, 7));
        List<MageBossEntity> mages = new ArrayList<>();
        float[] mark = new float[2];
        MageBossEntity[] current = new MageBossEntity[1];
        SoldierEntity[] shooter = new SoldierEntity[1];
        helper.startSequence()
                .thenExecute(() -> {
                    // Erneuerung: ein ganzes Ritual heilt gut ein Achtel
                    MageBossEntity water = still(helper, DragonType.WATER, new BlockPos(4, 1, 4), dummy, mages);
                    water.setHealth(water.getMaxHealth() * 0.5f);
                    float before = water.getHealth();
                    for (int t = 0; t < MageBossEntity.giftDuration(false); t++) {
                        water.devGift(level, false, t);
                    }
                    helper.assertTrue(water.getHealth() >= before + water.getMaxHealth() * 0.12f,
                            "Erneuerung heilt zu wenig: " + (water.getHealth() - before));
                    water.discard();
                    // ein Ritual, das genug Schaden abbekommt, zerbricht
                    MageBossEntity fire = mage(helper, DragonType.FIRE, new BlockPos(4, 1, 4));
                    mages.add(fire);
                    fire.devOnlyEnemy(dummy);
                    fire.devFight(level);
                    current[0] = fire;
                })
                .thenIdle(3)
                .thenExecute(() -> {
                    MageBossEntity fire = current[0];
                    fire.setHealth(fire.getMaxHealth() * 0.5f);
                    fire.devAttack(8);
                    helper.assertTrue(fire.devRenewing(), "Heilritual beginnt nicht");
                    fire.damageCooldownTime = 0;
                    fire.hurtServer(level, level.damageSources().playerAttack(player), 40.0f);
                    helper.assertTrue(!fire.devRenewing() && fire.devStagger() > 0, "Heilritual bricht nicht unter Schaden");
                    fire.discard();
                    // Phönixwiedergeburt: der erste tödliche Schlag verbrennt ihn nur
                    MageBossEntity phoenix = still(helper, DragonType.FIRE, new BlockPos(4, 1, 4), dummy, mages);
                    phoenix.damageCooldownTime = 0;
                    phoenix.hurtServer(level, level.damageSources().playerAttack(player), 100000.0f);
                    helper.assertTrue(phoenix.isAlive() && phoenix.devReborn() && phoenix.devRebirth() > 0,
                            "Ignatius steht nicht aus der Asche auf");
                    phoenix.damageCooldownTime = 0;
                    phoenix.hurtServer(level, level.damageSources().playerAttack(player), 50.0f);
                    helper.assertTrue(phoenix.isAlive(), "in den Flammen verwundbar");
                    while (phoenix.devRebirth() > 0) {
                        phoenix.devGiftsTick(level);
                    }
                    helper.assertTrue(Math.abs(phoenix.getHealth() - phoenix.getMaxHealth() * 0.3f) < 1.0f,
                            "nach der Wiedergeburt " + phoenix.getHealth() + " statt 30 %");
                    phoenix.damageCooldownTime = 0;
                    phoenix.hurtServer(level, level.damageSources().playerAttack(player), 100000.0f);
                    helper.assertTrue(phoenix.isDeadOrDying(), "Ignatius stirbt nicht beim zweiten Mal");
                    phoenix.discard();
                    // die Phönixe aus der Asche fliegen nicht in die Nachbartests
                    for (var bird : level.getEntitiesOfClass(io.github.sirschrolli.kingdomsatwar.magic.fx.SpellProjectile.class,
                            new AABB(helper.absolutePos(new BlockPos(4, 1, 4))).inflate(24.0))) {
                        bird.discard();
                    }
                    // Eiskokon: fast besiegt hüllt sie sich in Eis, Hiebe treffen nur den Kokon – bis er zerspringt
                    MageBossEntity ice = still(helper, DragonType.ICE, new BlockPos(4, 1, 4), dummy, mages);
                    ice.setHealth(ice.getMaxHealth() * 0.2f);
                    ice.devGiftsTick(level);
                    helper.assertTrue(ice.devCocoon() > 0, "kein Eiskokon");
                    float inside = ice.getHealth();
                    ice.damageCooldownTime = 0;
                    ice.hurtServer(level, level.damageSources().playerAttack(player), 10.0f);
                    helper.assertTrue(ice.getHealth() >= inside, "Hieb trifft Isolde im Kokon");
                    ice.damageCooldownTime = 0;
                    ice.hurtServer(level, level.damageSources().playerAttack(player), 60.0f);
                    helper.assertTrue(ice.devCocoon() == 0 && ice.devStagger() > 0, "Kokon zerspringt nicht");
                    ice.discard();
                    // Blitzableiter: heilt und trifft, wer in der Nähe steht
                    MageBossEntity storm = still(helper, DragonType.LIGHTNING, new BlockPos(4, 1, 5), dummy, mages);
                    storm.setHealth(storm.getMaxHealth() * 0.5f);
                    float stormBefore = storm.getHealth();
                    dummy.damageCooldownTime = 0;
                    float dummyBefore = dummy.getHealth();
                    for (int t = 0; t < MageBossEntity.giftDuration(true); t++) {
                        storm.devGift(level, true, t);
                    }
                    helper.assertTrue(storm.getHealth() >= stormBefore + storm.getMaxHealth() * 0.05f, "Blitzableiter heilt nicht");
                    helper.assertTrue(dummy.getHealth() < dummyBefore, "Entladung trifft niemanden");
                    storm.discard();
                    // Dornenhaut: wer zuschlägt, sticht sich
                    MageBossEntity nature = still(helper, DragonType.NATURE, new BlockPos(4, 1, 4), player, mages);
                    player.teleportTo(level, nature.getX() + 2.0, nature.getY(), nature.getZ(), java.util.Set.of(), 0.0f, 0.0f, true);
                    mark[0] = player.getHealth();
                    player.damageCooldownTime = 0;
                    nature.damageCooldownTime = 0;
                    float natureBefore = nature.getHealth();
                    boolean natureHit = nature.hurtServer(level, level.damageSources().playerAttack(player), 10.0f);
                    helper.assertTrue(player.getHealth() < mark[0], "Dornenhaut sticht nicht zurück (Treffer " + natureHit + ", Magierin "
                            + natureBefore + " -> " + nature.getHealth() + ", Abstand " + player.distanceTo(nature) + ", Spieler " + player.getHealth()
                            + ", Modus " + player.gameMode() + ", geladen " + player.connection.hasClientLoaded() + ")");
                    nature.discard();
                    player.setHealth(player.getMaxHealth());
                    player.removeAllEffects();
                    // Gegenwelle: nach dem dritten Nahkampftreffer stößt eine Welle den Angreifer fort
                    MageBossEntity tide = still(helper, DragonType.WATER, new BlockPos(4, 1, 4), player, mages);
                    player.damageCooldownTime = 0;
                    mark[0] = player.getHealth();
                    for (int i = 0; i < 3; i++) {
                        tide.damageCooldownTime = 0;
                        tide.hurtServer(level, level.damageSources().playerAttack(player), 4.0f);
                    }
                    helper.assertTrue(player.getHealth() < mark[0], "Gegenwelle trifft nicht");
                    tide.discard();
                    player.setHealth(player.getMaxHealth());
                    // Erdwacht: wer aus der Ferne schießt, den packt eine Steinfaust
                    shooter[0] = dummy(helper, new BlockPos(8, 1, 8));
                    MageBossEntity earth = still(helper, DragonType.EARTH, new BlockPos(0, 1, 0), shooter[0], mages);
                    var arrow = net.minecraft.world.entity.EntityTypes.ARROW.create(level, EntitySpawnReason.TRIGGERED);
                    helper.assertTrue(arrow != null, "kein Pfeil");
                    arrow.setOwner(shooter[0]);
                    shooter[0].damageCooldownTime = 0;
                    mark[1] = shooter[0].getHealth();
                    earth.damageCooldownTime = 0;
                    earth.hurtServer(level, level.damageSources().arrow(arrow, shooter[0]), 5.0f);
                })
                .thenWaitUntil(() -> helper.assertTrue(shooter[0] != null && shooter[0].getHealth() < mark[1], "Erdwacht packt den Schützen nicht"))
                .thenExecute(() -> {
                    for (MageBossEntity m : mages) {
                        m.discard();
                    }
                    dummy.discard();
                    if (shooter[0] != null) {
                        shooter[0].discard();
                    }
                    level.getServer().getPlayerList().remove(player);
                })
                .thenSucceed();
    }

    /** Ein Erzmagier im Kampf, ohne eigenen Willen (die Fähigkeiten treibt der Test an), der nur {@code enemy} als Gegner sieht. */
    private static MageBossEntity still(GameTestHelper helper, DragonType element, BlockPos rel, net.minecraft.world.entity.LivingEntity enemy,
            List<MageBossEntity> all) {
        MageBossEntity mage = mage(helper, element, rel);
        mage.setNoAi(true);
        mage.devOnlyEnemy(enemy);
        mage.devFight(helper.getLevel());
        all.add(mage);
        return mage;
    }

    // =====================================================================================
    // Zauberwirkungen
    // =====================================================================================

    /** Eine Wirkung löst nach ihrer Zeit aus und verschwindet; eine verankerte folgt ihrem Wesen. */
    private static void fx(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        floor(helper);
        Vec3 at = Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(4, 1, 4)));
        boolean[] expired = {false};
        int[] ticks = {0};
        SpellFxEntity circle = SpellFxEntity.create(level, SpellFxEntity.Kind.CIRCLE, at, DragonType.FIRE, 12, 1.5f)
                .onTick(fx -> ticks[0]++).onExpire(fx -> expired[0] = true).spawn();
        SoldierEntity soldier = dummy(helper, new BlockPos(2, 1, 2));
        SpellFxEntity bubble = SpellFxEntity.create(level, SpellFxEntity.Kind.BUBBLE, soldier.position(), DragonType.WATER, 0x7FE0FF, 40, 2.0f)
                .anchor(soldier).spawn();
        SpellFxEntity beam = SpellFxEntity.create(level, SpellFxEntity.Kind.BEAM, at.add(0, 1, 0), null, Spell.AETHER_COLOR, 30, 0.2f)
                .end(at.add(3, 2, 1)).spawn();
        helper.assertTrue(beam.endPoint().distanceTo(at.add(3, 2, 1)) < 0.01, "Endpunkt des Strahls stimmt nicht");
        helper.startSequence()
                .thenWaitUntil(() -> helper.assertTrue(circle.isRemoved(), "Warnkreis verschwindet nicht"))
                .thenExecute(() -> {
                    helper.assertTrue(expired[0], "Warnkreis löst nicht aus");
                    helper.assertTrue(ticks[0] >= 11 && ticks[0] <= 13, "Warnkreis lief " + ticks[0] + " statt 12 Ticks");
                    soldier.teleportTo(soldier.getX() + 2.0, soldier.getY(), soldier.getZ());
                })
                .thenIdle(2)
                .thenExecute(() -> {
                    helper.assertTrue(bubble.position().distanceTo(soldier.position()) < 0.01, "Blase folgt dem Gefangenen nicht");
                    soldier.discard();
                })
                .thenIdle(2)
                .thenExecute(() -> helper.assertTrue(bubble.isRemoved(), "Blase bleibt ohne ihren Gefangenen"))
                .thenSucceed();
    }

    // =====================================================================================
    // Der Stab
    // =====================================================================================

    private static ItemStack withXp(int xp) {
        ItemStack staff = new ItemStack(ModItems.AETHERIT_STAFF.get());
        StaffMastery.setXp(staff, xp);
        return staff;
    }

    /** Unzerstörbar, nur herstellbar, als Lehrling schwach, Aufstieg mit Erfahrung, Aussehen und Ladung. */
    private static void staff(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ItemStack staff = new ItemStack(ModItems.AETHERIT_STAFF.get());
        helper.assertTrue(!staff.isDamageableItem(), "Der Stab hat Haltbarkeit");
        helper.assertTrue(!staff.canBeHurtBy(level.damageSources().inFire()) && !staff.canBeHurtBy(level.damageSources().cactus()),
                "Der Stab verbrennt");
        helper.assertTrue(StaffMastery.rank(staff) == StaffMastery.Rank.APPRENTICE && StaffMastery.power(staff, 0.0f) < 0.7f,
                "Ein neuer Stab ist nicht schwach");
        helper.assertTrue(Math.abs(StaffMastery.power(staff, 1.0f) - StaffMastery.power(staff, 0.0f) * 1.5f) < 1.0e-4f,
                "Voll geladen nicht 50 % stärker");
        helper.assertTrue(AetheritStaffItem.charge(staff, 0) == 0.0f && AetheritStaffItem.charge(staff, 200) == 1.0f, "Ladung falsch");
        // Stufen: 1.000, 2.000, 5.000, 10.000 und 100.000 Erfahrung; auf Stufe 5 150 % Kraft
        int[][] steps = {{999, 0}, {1_000, 1}, {1_999, 1}, {2_000, 2}, {5_000, 3}, {9_999, 3}, {10_000, 4}, {99_999, 4}, {100_000, 5}};
        for (int[] step : steps) {
            helper.assertTrue(StaffMastery.Rank.of(step[0]).level() == step[1], step[0] + " Erfahrung ergibt nicht Stufe " + step[1]);
        }
        helper.assertTrue(Math.abs(StaffMastery.Rank.AETHERLORD.power() - 1.5f) < 1.0e-4f, "Stufe 5 nicht 150 % Kraft");
        // Stufe und Aussehen
        StaffMastery.setXp(staff, StaffMastery.Rank.MAGISTER.xp());
        CustomModelData look = staff.get(DataComponents.CUSTOM_MODEL_DATA);
        helper.assertTrue(look != null && look.getFloat(0) != null && look.getFloat(0) == 3.0f, "Stufe nicht im Aussehen");
        helper.assertTrue(look.getColor(0) != null && look.getColor(0) == Spell.AETHER_COLOR, "Kristall nicht in der Farbe des Zaubers");
        AetheritStaffItem.setSpells(staff, KnownSpells.DEFAULT.withLevel(Spell.FIRE, 1).select(Spell.FIRE));
        CustomModelData fire = staff.get(DataComponents.CUSTOM_MODEL_DATA);
        helper.assertTrue(fire != null && fire.getColor(0) != null && fire.getColor(0) == Spell.FIRE.color(),
                "Kristall wechselt die Farbe nicht mit dem Zauber");
        StaffMastery.setXp(staff, 0);
        // Zaubern bringt Erfahrung; der Aufstieg kommt mit der Erfahrung
        ServerPlayer player = MageGameTests.survivalPlayer(helper);
        player.getAbilities().instabuild = false;      // die Testwelt läuft im Kreativmodus – hier zählt das Mana
        try {
            BlockPos at = helper.absolutePos(new BlockPos(1, 2, 1));
            player.teleportTo(level, at.getX() + 0.5, at.getY(), at.getZ() + 0.5, java.util.Set.of(), 0.0f, 0.0f, true);
            player.getInventory().setItem(player.getInventory().getSelectedSlot(), staff);
            AetheritStaffItem.select(staff, Spell.AETHER_BOLT);
            Mana.set(player, Mana.BASE_MAX);
            helper.assertTrue(AetheritStaffItem.cast(level, player, staff, 1.0f), "Zauber gelingt nicht");
            helper.assertTrue(StaffMastery.xp(staff) == 2, "Voll geladener Zauber bringt nicht 2 Erfahrung: " + StaffMastery.xp(staff));
            int cost = Mana.cost(Spell.AETHER_BOLT, 1, 1.0f);
            helper.assertTrue(Math.abs(Mana.get(player) - (Mana.BASE_MAX - cost)) < 0.01f, "Zauber kostet kein Mana: " + Mana.get(player));
            helper.assertTrue(player.getCooldowns().isOnCooldown(staff), "keine Abklingzeit nach dem Zauber");
            helper.assertTrue(!AetheritStaffItem.cast(level, player, staff, 0.0f), "Zauber trotz Abklingzeit");
            // das Mana füllt sich wieder auf
            float before = Mana.get(player);
            Mana.regenerate(player, 20);
            helper.assertTrue(Math.abs(Mana.get(player) - before - Mana.BASE_REGEN) < 0.01f, "Mana erholt sich nicht: " + Mana.get(player));
            // ohne genug Mana kein Zauber (ein anderer Zauber, also keine Abklingzeit)
            ItemStack healer = new ItemStack(ModItems.AETHERIT_STAFF.get());
            AetheritStaffItem.setSpells(healer, KnownSpells.DEFAULT.withLevel(Spell.NATURE, 1).select(Spell.NATURE));
            Mana.set(player, 3.0f);
            helper.assertTrue(!AetheritStaffItem.cast(level, player, healer, 0.0f) && Math.abs(Mana.get(player) - 3.0f) < 0.01f,
                    "Zauber ohne genug Mana");
            // die Heilende Blüte heilt sofort
            Mana.set(player, Mana.BASE_MAX);
            player.setHealth(8.0f);
            helper.assertTrue(AetheritStaffItem.cast(level, player, healer, 0.0f), "Heilende Blüte gelingt nicht");
            helper.assertTrue(player.getHealth() >= 8.0f + 2.0f, "Heilende Blüte heilt nicht sofort: " + player.getHealth());
            // mehr Mana mit höherer Stufe
            helper.assertTrue(Mana.max(staff) == Mana.BASE_MAX && Mana.max(withXp(StaffMastery.Rank.AETHERLORD.xp())) == Mana.BASE_MAX
                    + 5 * Mana.PER_LEVEL, "Mana wächst nicht mit der Stufe");
            StaffMastery.award(player, staff, StaffMastery.Rank.ADEPT.xp());
            helper.assertTrue(StaffMastery.rank(staff) == StaffMastery.Rank.ADEPT, "kein Aufstieg zum Adepten");
        } finally {
            level.getServer().getPlayerList().remove(player);
        }
        // nur herstellbar: Rezepte für Kern und Stab (der Stab wird nicht mehr gebraucht)
        var recipes = level.getServer().getRecipeManager().recipeMap();
        for (String name : List.of("aether_core", "aetherit_staff")) {
            ResourceKey<Recipe<?>> key = ResourceKey.create(Registries.RECIPE, KingdomsAtWar.id(name));
            helper.assertTrue(recipes.byKey(key) != null, "Rezept fehlt: " + name);
        }
        helper.succeed();
    }
}
