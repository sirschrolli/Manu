package io.github.sirschrolli.kingdomsatwar.gametest;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.entity.creature.nether.BristleGiant;
import io.github.sirschrolli.kingdomsatwar.entity.creature.nether.Borstling;
import io.github.sirschrolli.kingdomsatwar.entity.creature.nether.Glutkeiler;
import io.github.sirschrolli.kingdomsatwar.entity.creature.nether.Lavastelzer;
import io.github.sirschrolli.kingdomsatwar.entity.creature.nether.Moderkeiler;
import io.github.sirschrolli.kingdomsatwar.entity.creature.nether.RottenBristling;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Strider;
import net.minecraft.world.entity.monster.Zoglin;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.PiglinBrute;
import net.minecraft.world.entity.monster.zombie.ZombifiedPiglin;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

/** Tests des Nether-Volks und seiner Tiere (E5a): Borstlinge, Borstenhünen, Moderborstlinge, Glutkeiler, Moderkeiler, Lavastelzer. */
final class NetherGameTests {
    private static final Identifier ARENA = KingdomsAtWar.id("test_arena");

    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> SPAWN =
            ModGameTests.TEST_FUNCTIONS.register("nether_spawn", () -> NetherGameTests::spawn);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> CONVERSION =
            ModGameTests.TEST_FUNCTIONS.register("nether_vanilla_replaced", () -> NetherGameTests::vanillaReplaced);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> ZOMBIFY =
            ModGameTests.TEST_FUNCTIONS.register("nether_zombification", () -> NetherGameTests::zombification);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> AVOID =
            ModGameTests.TEST_FUNCTIONS.register("nether_avoid_undead", () -> NetherGameTests::avoidUndead);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> BARTER =
            ModGameTests.TEST_FUNCTIONS.register("nether_barter", () -> NetherGameTests::barter);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> HOGLIN_BREEDING =
            ModGameTests.TEST_FUNCTIONS.register("nether_hoglin_breeding", () -> NetherGameTests::hoglinBreeding);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> HOGLIN_RETREAT =
            ModGameTests.TEST_FUNCTIONS.register("nether_hoglin_retreat", () -> NetherGameTests::hoglinRetreat);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> STRIDER_RIDE =
            ModGameTests.TEST_FUNCTIONS.register("nether_strider_ride", () -> NetherGameTests::striderRide);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> STRIDER_JOCKEY =
            ModGameTests.TEST_FUNCTIONS.register("nether_strider_jockey", () -> NetherGameTests::striderJockey);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> STRIDER_COLD =
            ModGameTests.TEST_FUNCTIONS.register("nether_strider_cold", () -> NetherGameTests::striderCold);

    private NetherGameTests() {}

    static void init() {}

    static void register(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment) {
        for (var test : List.of(SPAWN, CONVERSION, AVOID, HOGLIN_BREEDING, HOGLIN_RETREAT, STRIDER_RIDE, STRIDER_JOCKEY, STRIDER_COLD)) {
            event.registerTest(test.getId(), new FunctionGameTestInstance(test.key(),
                    new TestData<>(environment, Level.OVERWORLD, ARENA, 300, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
        }
        for (var test : List.of(ZOMBIFY, BARTER)) {                       // brauchen länger (Verwandlung, Bewundern)
            event.registerTest(test.getId(), new FunctionGameTestInstance(test.key(),
                    new TestData<>(environment, Level.OVERWORLD, ARENA, 700, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
        }
    }

    private static Map<EntityType<?>, EntityType<? extends Mob>> pairs() {
        Map<EntityType<?>, EntityType<? extends Mob>> map = new LinkedHashMap<>();
        map.put(EntityTypes.PIGLIN, ModEntities.PIGLIN.get());
        map.put(EntityTypes.PIGLIN_BRUTE, ModEntities.PIGLIN_BRUTE.get());
        map.put(EntityTypes.ZOMBIFIED_PIGLIN, ModEntities.ZOMBIFIED_PIGLIN.get());
        map.put(EntityTypes.HOGLIN, ModEntities.HOGLIN.get());
        map.put(EntityTypes.ZOGLIN, ModEntities.ZOGLIN.get());
        map.put(EntityTypes.STRIDER, ModEntities.STRIDER.get());
        return map;
    }

    private static void floor(GameTestHelper helper) {
        for (int x = 0; x < 9; x++) {
            for (int z = 0; z < 9; z++) {
                helper.setBlock(x, 0, z, Blocks.NETHERRACK);
            }
        }
    }

    private static AABB arena(GameTestHelper helper) {
        return new AABB(helper.absolutePos(new BlockPos(4, 1, 4))).inflate(10.0);
    }

    private static void place(GameTestHelper helper, Mob mob, BlockPos relative) {
        BlockPos pos = helper.absolutePos(relative);
        mob.snapTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0.0f, 0.0f);
        helper.getLevel().addFreshEntity(mob);
    }

    // =====================================================================================

    /** Alle drei erscheinen und leben; Beute wie bei Vanilla, dieselben Tags und Kategorien; Namen je Gestalt. */
    private static void spawn(GameTestHelper helper) {
        floor(helper);
        for (var pair : pairs().entrySet()) {
            EntityType<? extends Mob> ours = pair.getValue();
            helper.assertTrue(ours.getDefaultLootTable().isPresent(), "keine Beute-Tabelle: " + ours.getDescriptionId());
            helper.assertTrue(ours.getCategory() == pair.getKey().getCategory(), ours.getDescriptionId() + " in falscher Kategorie");
            helper.assertTrue(ours.fireImmune() == pair.getKey().fireImmune(), ours.getDescriptionId() + ": Feuerfestigkeit anders");
            pair.getKey().builtInRegistryHolder().tags().forEach(tag -> helper.assertTrue(ours.builtInRegistryHolder().is(tag),
                    ours.getDescriptionId() + " fehlt im Tag " + tag.location()));
        }
        helper.assertTrue(ModEntities.ZOMBIFIED_PIGLIN.get().builtInRegistryHolder().is(EntityTypeTags.ZOMBIES), "Moderborstling kein Zombie");
        Borstling borstling = helper.spawnWithNoFreeWill(ModEntities.PIGLIN.get(), new BlockPos(2, 1, 2));
        borstling.setImmuneToZombification(true);
        borstling.setLook(Borstling.Look.GOLD);
        helper.assertTrue(borstling.getName().getString().equals(Component.translatable("entity.kingdomsatwar.piglin.gold").getString()),
                "Name des Goldborstlings: " + borstling.getName().getString());
        BristleGiant giant = helper.spawnWithNoFreeWill(ModEntities.PIGLIN_BRUTE.get(), new BlockPos(5, 1, 2));
        giant.setImmuneToZombification(true);
        RottenBristling rotten = helper.spawnWithNoFreeWill(ModEntities.ZOMBIFIED_PIGLIN.get(), new BlockPos(5, 1, 6));
        rotten.setLook(RottenBristling.Look.MODER_ASCHEN);
        helper.assertTrue(rotten.getName().getString().equals(Component.translatable("entity.kingdomsatwar.zombified_piglin.moder_aschen").getString()),
                "Name des Dörrborstlings: " + rotten.getName().getString());
        Glutkeiler keiler = helper.spawnWithNoFreeWill(ModEntities.HOGLIN.get(), new BlockPos(2, 1, 6));
        keiler.setImmuneToZombification(true);
        keiler.setLook(Glutkeiler.Look.SCHLACKE);
        helper.assertTrue(keiler.getName().getString().equals(Component.translatable("entity.kingdomsatwar.hoglin.schlacke").getString()),
                "Name des Schlackenkeilers: " + keiler.getName().getString());
        Moderkeiler moder = helper.spawnWithNoFreeWill(ModEntities.ZOGLIN.get(), new BlockPos(7, 1, 7));
        Lavastelzer stelzer = helper.spawnWithNoFreeWill(ModEntities.STRIDER.get(), new BlockPos(7, 1, 2));
        stelzer.setLook(Lavastelzer.Look.SEELE);
        helper.assertTrue(stelzer.getName().getString().equals(Component.translatable("entity.kingdomsatwar.strider.seele").getString()),
                "Name des Seelenstelzers: " + stelzer.getName().getString());
        helper.runAfterDelay(10, () -> {
            for (Mob mob : List.<Mob>of(borstling, giant, rotten, keiler, moder, stelzer)) {
                helper.assertTrue(mob.isAlive() && !mob.isRemoved(), mob.getType().getDescriptionId() + " lebt nicht mehr");
            }
            helper.succeed();
        });
    }

    /**
     * Vanilla-Piglins (mit Armbrust, als Kind), Piglin-Barbar (mit Goldaxt) und Zombie-Piglin werden zu den eigenen –
     * dieselbe UUID, Kind bleibt Kind, die Waffen bleiben in der Hand.
     */
    private static void vanillaReplaced(GameTestHelper helper) {
        floor(helper);
        ServerLevel level = helper.getLevel();
        Piglin piglin = EntityTypes.PIGLIN.create(level, EntitySpawnReason.SPAWN_ITEM_USE);
        piglin.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.CROSSBOW));
        piglin.setImmuneToZombification(true);
        piglin.setNoAi(true);
        place(helper, piglin, new BlockPos(1, 1, 1));
        Piglin child = EntityTypes.PIGLIN.create(level, EntitySpawnReason.SPAWN_ITEM_USE);
        child.setBaby(true);
        child.setImmuneToZombification(true);
        child.setNoAi(true);
        place(helper, child, new BlockPos(4, 1, 1));
        PiglinBrute brute = EntityTypes.PIGLIN_BRUTE.create(level, EntitySpawnReason.SPAWN_ITEM_USE);
        brute.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.GOLDEN_AXE));
        brute.setImmuneToZombification(true);
        brute.setNoAi(true);
        place(helper, brute, new BlockPos(7, 1, 1));
        ZombifiedPiglin zombified = EntityTypes.ZOMBIFIED_PIGLIN.create(level, EntitySpawnReason.SPAWN_ITEM_USE);
        zombified.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.GOLDEN_SWORD));
        zombified.setNoAi(true);
        place(helper, zombified, new BlockPos(4, 1, 6));
        Hoglin hoglin = EntityTypes.HOGLIN.create(level, EntitySpawnReason.SPAWN_ITEM_USE);
        hoglin.setBaby(true);
        hoglin.setImmuneToZombification(true);
        hoglin.setNoAi(true);
        place(helper, hoglin, new BlockPos(1, 1, 7));
        Zoglin zoglin = EntityTypes.ZOGLIN.create(level, EntitySpawnReason.SPAWN_ITEM_USE);
        zoglin.setNoAi(true);
        place(helper, zoglin, new BlockPos(7, 1, 7));
        Strider strider = EntityTypes.STRIDER.create(level, EntitySpawnReason.SPAWN_ITEM_USE);
        strider.setItemSlot(EquipmentSlot.SADDLE, new ItemStack(Items.SADDLE));
        strider.setNoAi(true);
        place(helper, strider, new BlockPos(7, 1, 4));
        UUID p = piglin.getUUID(), c = child.getUUID(), b = brute.getUUID(), z = zombified.getUUID();
        UUID h = hoglin.getUUID(), zg = zoglin.getUUID(), st = strider.getUUID();
        helper.runAfterDelay(2, () -> {
            helper.assertTrue(level.getEntity(h) instanceof Glutkeiler ours && ours.isBaby(), "Frischling nicht mehr klein: " + level.getEntity(h));
            helper.assertTrue(level.getEntity(zg) instanceof Moderkeiler, "kein Moderkeiler: " + level.getEntity(zg));
            helper.assertTrue(level.getEntity(st) instanceof Lavastelzer ours && ours.isSaddled(), "Lavastelzer ohne Sattel: " + level.getEntity(st));
            helper.assertTrue(level.getEntity(p) instanceof Borstling ours && ours.getMainHandItem().is(Items.CROSSBOW),
                    "Borstling ohne Armbrust: " + level.getEntity(p));
            helper.assertTrue(level.getEntity(c) instanceof Borstling ours && ours.isBaby(), "Kind nicht mehr Kind: " + level.getEntity(c));
            helper.assertTrue(level.getEntity(b) instanceof BristleGiant ours && ours.getMainHandItem().is(Items.GOLDEN_AXE),
                    "Borstenhüne ohne Axt: " + level.getEntity(b));
            helper.assertTrue(level.getEntity(z) instanceof RottenBristling ours && ours.getMainHandItem().is(Items.GOLDEN_SWORD),
                    "Moderborstling ohne Schwert: " + level.getEntity(z));
            for (Mob mob : level.getEntitiesOfClass(Mob.class, arena(helper))) {
                helper.assertTrue(!pairs().containsKey(mob.getType()), "noch ein Vanilla-Wesen: " + mob.getType().getDescriptionId());
            }
            helper.succeed();
        });
    }

    /**
     * In der Oberwelt wird ein Borstling nach einer Weile zum Moderborstling, ein Glutkeiler zum Moderkeiler (wie Piglin und
     * Hoglin bei Vanilla) – und sie behalten ihre Gestalt: karmesinrot bleibt karmesinrot.
     */
    private static void zombification(GameTestHelper helper) {
        floor(helper);
        Borstling borstling = helper.spawnWithNoFreeWill(ModEntities.PIGLIN.get(), new BlockPos(2, 1, 4));
        borstling.setLook(Borstling.Look.KARMESIN);
        Glutkeiler keiler = helper.spawnWithNoFreeWill(ModEntities.HOGLIN.get(), new BlockPos(6, 1, 4));
        keiler.setLook(Glutkeiler.Look.KARMESIN);
        UUID id = borstling.getUUID(), boar = keiler.getUUID();
        helper.succeedWhen(() -> {
            helper.assertTrue(helper.getLevel().getEntity(id) == null, "der Borstling ist noch da");
            helper.assertTrue(helper.getLevel().getEntity(boar) == null, "der Glutkeiler ist noch da");
            helper.assertTrue(!helper.getLevel().getEntitiesOfClass(RottenBristling.class, arena(helper),
                    r -> r.getLook() == RottenBristling.Look.MODER_KARMESIN).isEmpty(), "kein karmesinroter Moderborstling");
            helper.assertTrue(!helper.getLevel().getEntitiesOfClass(Moderkeiler.class, arena(helper),
                    m -> m.getLook() == Moderkeiler.Look.MODER_KARMESIN).isEmpty(), "kein karmesinroter Moderkeiler");
            helper.assertTrue(helper.getLevel().getEntitiesOfClass(ZombifiedPiglin.class, arena(helper),
                    zp -> !(zp instanceof RottenBristling)).isEmpty(), "ein Vanilla-Zombie-Piglin");
            helper.assertTrue(helper.getLevel().getEntitiesOfClass(Zoglin.class, arena(helper),
                    zg -> !(zg instanceof Moderkeiler)).isEmpty(), "ein Vanilla-Zoglin");
        });
    }

    /** Ein Borstling merkt sich einen Moderborstling in der Nähe als Untoten – und weicht ihm aus. */
    private static void avoidUndead(GameTestHelper helper) {
        floor(helper);
        Borstling borstling = helper.spawn(ModEntities.PIGLIN.get(), new BlockPos(2, 1, 4));
        borstling.setImmuneToZombification(true);
        RottenBristling rotten = helper.spawnWithNoFreeWill(ModEntities.ZOMBIFIED_PIGLIN.get(), new BlockPos(5, 1, 4));
        helper.succeedWhen(() -> helper.assertTrue(borstling.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_ZOMBIFIED)
                .filter(rotten::equals).isPresent(), "der Moderborstling ist nicht als Untoter vermerkt"));
    }

    /** Ein Goldbarren vor die Hufe: der Borstling bewundert ihn und tauscht ihn gegen etwas aus seinem Beutel. */
    private static void barter(GameTestHelper helper) {
        floor(helper);
        for (int x = 0; x < 9; x++) {
            for (int z = 0; z < 9; z++) {
                if (x == 0 || z == 0 || x == 8 || z == 8) {
                    helper.setBlock(x, 1, z, Blocks.NETHER_BRICK_FENCE);   // nicht weglaufen
                }
            }
        }
        Borstling borstling = helper.spawn(ModEntities.PIGLIN.get(), new BlockPos(4, 1, 4));
        borstling.setImmuneToZombification(true);
        BlockPos at = helper.absolutePos(new BlockPos(4, 1, 5));
        ItemEntity gold = new ItemEntity(helper.getLevel(), at.getX() + 0.5, at.getY() + 0.2, at.getZ() + 0.5, new ItemStack(Items.GOLD_INGOT));
        helper.getLevel().addFreshEntity(gold);
        helper.succeedWhen(() -> helper.assertTrue(!helper.getLevel().getEntitiesOfClass(ItemEntity.class, arena(helper),
                item -> !item.getItem().is(Items.GOLD_INGOT)).isEmpty(), "noch nichts eingetauscht"));
    }

    /**
     * Zwei Glutkeiler, die Karmesinpilze gefressen haben, finden sich über das Vanilla-Gehirn (es sucht Partner unter den
     * Hoglins – die Glutkeiler gelten als solche) und bekommen einen Frischling der eigenen Art.
     */
    private static void hoglinBreeding(GameTestHelper helper) {
        floor(helper);
        Glutkeiler a = helper.spawn(ModEntities.HOGLIN.get(), new BlockPos(2, 1, 4));
        Glutkeiler b = helper.spawn(ModEntities.HOGLIN.get(), new BlockPos(6, 1, 4));
        for (Glutkeiler keiler : List.of(a, b)) {
            keiler.setImmuneToZombification(true);
            keiler.setLook(Glutkeiler.Look.ASCHEN);
            keiler.setInLove(null);
        }
        helper.succeedWhen(() -> helper.assertTrue(!helper.getLevel().getEntitiesOfClass(Glutkeiler.class, arena(helper),
                keiler -> keiler.isBaby() && keiler.getLook() == Glutkeiler.Look.ASCHEN).isEmpty(), "noch kein Frischling"));
    }

    /**
     * Trifft ein Glutkeiler einen Borstling, während mehr Borstlinge als Keiler um ihn stehen, flieht er (Vanilla prüft dafür
     * den Typ „Piglin“ – die Borstlinge gelten als solche).
     */
    private static void hoglinRetreat(GameTestHelper helper) {
        floor(helper);
        Glutkeiler keiler = helper.spawn(ModEntities.HOGLIN.get(), new BlockPos(4, 1, 2));
        keiler.setImmuneToZombification(true);
        List<Borstling> folk = new java.util.ArrayList<>();
        for (int x : new int[] {2, 4, 6}) {
            Borstling borstling = helper.spawnWithNoFreeWill(ModEntities.PIGLIN.get(), new BlockPos(x, 1, 6));
            borstling.setImmuneToZombification(true);
            folk.add(borstling);
        }
        helper.runAfterDelay(40, () -> {
            helper.assertTrue(keiler.getBrain().getMemory(MemoryModuleType.VISIBLE_ADULT_PIGLIN_COUNT).orElse(0) >= 2,
                    "der Glutkeiler zählt die Borstlinge nicht");
            keiler.doHurtTarget(helper.getLevel(), folk.get(1));
            helper.assertTrue(keiler.getBrain().hasMemoryValue(MemoryModuleType.AVOID_TARGET), "der Glutkeiler flieht nicht");
            helper.succeed();
        });
    }

    /** Gesattelt und mit der Wirrpilzrute lenkt man den Lavastelzer und treibt ihn an – wie den Schreiter. */
    private static void striderRide(GameTestHelper helper) {
        floor(helper);
        Lavastelzer stelzer = helper.spawn(ModEntities.STRIDER.get(), new BlockPos(4, 1, 4));
        stelzer.setItemSlot(EquipmentSlot.SADDLE, new ItemStack(Items.SADDLE));
        ServerPlayer rider = IdentityGameTests.rider(helper, new BlockPos(4, 1, 2));
        rider.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.WARPED_FUNGUS_ON_A_STICK));
        helper.assertTrue(rider.startRiding(stelzer, true, false), "Aufsitzen misslungen");
        helper.assertTrue(rider.getControlledVehicle() == stelzer, "der Lavastelzer lässt sich nicht lenken");
        InteractionResult result = rider.getMainHandItem().use(helper.getLevel(), rider, InteractionHand.MAIN_HAND);
        helper.assertTrue(result.consumesAction(), "die Wirrpilzrute treibt den Lavastelzer nicht an: " + result);
        rider.stopRiding();
        rider.discard();
        helper.succeed();
    }

    /** Ein Vanilla-Zombie-Piglin auf einem Lavastelzer (Jockey) wird zum Moderborstling – und bleibt im Sattel. */
    private static void striderJockey(GameTestHelper helper) {
        floor(helper);
        ServerLevel level = helper.getLevel();
        Lavastelzer stelzer = helper.spawnWithNoFreeWill(ModEntities.STRIDER.get(), new BlockPos(4, 1, 4));
        stelzer.setItemSlot(EquipmentSlot.SADDLE, new ItemStack(Items.SADDLE));
        ZombifiedPiglin jockey = EntityTypes.ZOMBIFIED_PIGLIN.create(level, EntitySpawnReason.JOCKEY);
        jockey.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.WARPED_FUNGUS_ON_A_STICK));
        jockey.snapTo(stelzer.getX(), stelzer.getY(), stelzer.getZ(), 0.0f, 0.0f);
        jockey.startRiding(stelzer, true, false);
        level.addFreshEntity(jockey);
        helper.succeedWhen(() -> helper.assertTrue(stelzer.getFirstPassenger() instanceof RottenBristling rider
                && rider.getMainHandItem().is(Items.WARPED_FUNGUS_ON_A_STICK), "kein Moderborstling im Sattel: " + stelzer.getFirstPassenger()));
    }

    /** Außerhalb der Lava friert der Lavastelzer (fahles Bild, langsam), in der Lava wird ihm wieder warm. */
    private static void striderCold(GameTestHelper helper) {
        floor(helper);
        for (int x = 5; x <= 7; x++) {
            for (int z = 3; z <= 5; z++) {
                helper.setBlock(x, 0, z, Blocks.LAVA);
            }
        }
        Lavastelzer cold = helper.spawnWithNoFreeWill(ModEntities.STRIDER.get(), new BlockPos(2, 1, 4));
        Lavastelzer warm = helper.spawnWithNoFreeWill(ModEntities.STRIDER.get(), new BlockPos(6, 0, 4));
        helper.succeedWhen(() -> {
            helper.assertTrue(cold.isSuffocating(), "der Lavastelzer an Land friert nicht");
            helper.assertFalse(warm.isSuffocating(), "der Lavastelzer in der Lava friert");
        });
    }
}
