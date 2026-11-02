package io.github.sirschrolli.kingdomsatwar.gametest;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.entity.creature.nether.BristleGiant;
import io.github.sirschrolli.kingdomsatwar.entity.creature.nether.Borstling;
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
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.item.ItemEntity;
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

/** Tests des Nether-Volks (E5a): Borstlinge, Borstenhünen, Moderborstlinge. */
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

    private NetherGameTests() {}

    static void init() {}

    static void register(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment) {
        for (var test : List.of(SPAWN, CONVERSION, AVOID)) {
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
        helper.runAfterDelay(10, () -> {
            for (Mob mob : List.<Mob>of(borstling, giant, rotten)) {
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
        UUID p = piglin.getUUID(), c = child.getUUID(), b = brute.getUUID(), z = zombified.getUUID();
        helper.runAfterDelay(2, () -> {
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

    /** In der Oberwelt wird ein Borstling nach einer Weile zum Moderborstling (wie der Piglin zum Zombie-Piglin). */
    private static void zombification(GameTestHelper helper) {
        floor(helper);
        Borstling borstling = helper.spawnWithNoFreeWill(ModEntities.PIGLIN.get(), new BlockPos(4, 1, 4));
        UUID id = borstling.getUUID();
        helper.succeedWhen(() -> {
            helper.assertTrue(helper.getLevel().getEntity(id) == null, "der Borstling ist noch da");
            helper.assertTrue(!helper.getLevel().getEntitiesOfClass(RottenBristling.class, arena(helper)).isEmpty(), "kein Moderborstling");
            helper.assertTrue(helper.getLevel().getEntitiesOfClass(ZombifiedPiglin.class, arena(helper),
                    zp -> !(zp instanceof RottenBristling)).isEmpty(), "ein Vanilla-Zombie-Piglin");
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
}
