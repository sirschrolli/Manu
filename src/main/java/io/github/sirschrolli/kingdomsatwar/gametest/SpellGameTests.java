package io.github.sirschrolli.kingdomsatwar.gametest;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import io.github.sirschrolli.kingdomsatwar.entity.boss.mage.MageBossEntity;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierEntity;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierType;
import io.github.sirschrolli.kingdomsatwar.magic.AetheritStaffItem;
import io.github.sirschrolli.kingdomsatwar.magic.KnownSpells;
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
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> FX =
            ModGameTests.TEST_FUNCTIONS.register("spell_fx", () -> SpellGameTests::fx);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> STAFF =
            ModGameTests.TEST_FUNCTIONS.register("staff_mastery", () -> SpellGameTests::staff);

    private SpellGameTests() {}

    static void init() {}

    static void register(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment) {
        List<DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>>> tests = new ArrayList<>(SIGNATURES.values());
        tests.add(PASSIVES);
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
        // Rang und Aussehen
        StaffMastery.setXp(staff, StaffMastery.Rank.MAGISTER.xp());
        CustomModelData look = staff.get(DataComponents.CUSTOM_MODEL_DATA);
        helper.assertTrue(look != null && look.getFloat(0) != null && look.getFloat(0) == 2.0f, "Rang nicht im Aussehen");
        helper.assertTrue(look.getColor(0) != null && look.getColor(0) == Spell.AETHER_COLOR, "Kristall nicht in der Farbe des Zaubers");
        AetheritStaffItem.setSpells(staff, KnownSpells.DEFAULT.withLevel(Spell.FIRE, 1).select(Spell.FIRE));
        CustomModelData fire = staff.get(DataComponents.CUSTOM_MODEL_DATA);
        helper.assertTrue(fire != null && fire.getColor(0) != null && fire.getColor(0) == Spell.FIRE.color(),
                "Kristall wechselt die Farbe nicht mit dem Zauber");
        StaffMastery.setXp(staff, 0);
        // Zaubern bringt Erfahrung; der Aufstieg kommt mit der Erfahrung
        ServerPlayer player = MageGameTests.survivalPlayer(helper);
        try {
            BlockPos at = helper.absolutePos(new BlockPos(1, 2, 1));
            player.teleportTo(level, at.getX() + 0.5, at.getY(), at.getZ() + 0.5, java.util.Set.of(), 0.0f, 0.0f, true);
            AetheritStaffItem.select(staff, Spell.AETHER_BOLT);
            helper.assertTrue(AetheritStaffItem.cast(level, player, staff, 1.0f), "Zauber gelingt nicht");
            helper.assertTrue(StaffMastery.xp(staff) == 2, "Voll geladener Zauber bringt nicht 2 Erfahrung: " + StaffMastery.xp(staff));
            helper.assertTrue(player.getCooldowns().isOnCooldown(staff), "keine Abklingzeit nach dem Zauber");
            helper.assertTrue(!AetheritStaffItem.cast(level, player, staff, 0.0f), "Zauber trotz Abklingzeit");
            StaffMastery.award(player, staff, StaffMastery.Rank.ADEPT.xp());
            helper.assertTrue(StaffMastery.rank(staff) == StaffMastery.Rank.ADEPT, "kein Aufstieg zum Adepten");
        } finally {
            level.getServer().getPlayerList().remove(player);
        }
        // nur herstellbar: Rezepte für Kern und Stab
        var recipes = level.getServer().getRecipeManager().recipeMap();
        for (String name : List.of("aether_core", "aetherit_staff")) {
            ResourceKey<Recipe<?>> key = ResourceKey.create(Registries.RECIPE, KingdomsAtWar.id(name));
            helper.assertTrue(recipes.byKey(key) != null, "Rezept fehlt: " + name);
        }
        helper.succeed();
    }
}
