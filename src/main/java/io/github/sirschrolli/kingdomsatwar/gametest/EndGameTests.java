package io.github.sirschrolli.kingdomsatwar.gametest;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.entity.creature.end.Leermilbe;
import io.github.sirschrolli.kingdomsatwar.entity.creature.end.Leerwandler;
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
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enderman;
import net.minecraft.world.entity.monster.Endermite;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

/** Tests der Wesen des Ends (E5c): Leerwandler, Wirrwandler und Leermilben. */
final class EndGameTests {
    private static final Identifier ARENA = KingdomsAtWar.id("test_arena");

    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> SPAWN =
            ModGameTests.TEST_FUNCTIONS.register("end_spawn", () -> EndGameTests::spawn);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> REPLACED =
            ModGameTests.TEST_FUNCTIONS.register("end_vanilla_replaced", () -> EndGameTests::vanillaReplaced);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> PEARL =
            ModGameTests.TEST_FUNCTIONS.register("end_pearl_mite", () -> EndGameTests::pearlMite);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> HUNT =
            ModGameTests.TEST_FUNCTIONS.register("end_walker_hunts_mites", () -> EndGameTests::walkerHuntsMites);

    private EndGameTests() {}

    static void init() {}

    static void register(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment) {
        for (var test : List.of(SPAWN, REPLACED, PEARL, HUNT)) {
            event.registerTest(test.getId(), new FunctionGameTestInstance(test.key(),
                    new TestData<>(environment, Level.OVERWORLD, ARENA, 300, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
        }
    }

    private static Map<EntityType<?>, EntityType<? extends Mob>> pairs() {
        Map<EntityType<?>, EntityType<? extends Mob>> map = new LinkedHashMap<>();
        map.put(EntityTypes.ENDERMAN, ModEntities.ENDERMAN.get());
        map.put(EntityTypes.ENDERMITE, ModEntities.ENDERMITE.get());
        return map;
    }

    private static void floor(GameTestHelper helper) {
        for (int x = 0; x < 9; x++) {
            for (int z = 0; z < 9; z++) {
                helper.setBlock(x, 0, z, Blocks.END_STONE);
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

    /** Beide erscheinen und leben; Beute wie bei Vanilla, dieselben Tags und Kategorien; Namen je Gestalt. */
    private static void spawn(GameTestHelper helper) {
        floor(helper);
        for (var pair : pairs().entrySet()) {
            EntityType<? extends Mob> ours = pair.getValue();
            helper.assertTrue(ours.getDefaultLootTable().isPresent(), "keine Beute-Tabelle: " + ours.getDescriptionId());
            helper.assertTrue(ours.getCategory() == pair.getKey().getCategory(), ours.getDescriptionId() + " in falscher Kategorie");
            pair.getKey().builtInRegistryHolder().tags().forEach(tag -> helper.assertTrue(ours.builtInRegistryHolder().is(tag),
                    ours.getDescriptionId() + " fehlt im Tag " + tag.location()));
        }
        Leerwandler wandler = helper.spawnWithNoFreeWill(ModEntities.ENDERMAN.get(), new BlockPos(2, 1, 2));
        wandler.setLook(Leerwandler.Look.WIRR);
        helper.assertTrue(wandler.getName().getString().equals(Component.translatable("entity.kingdomsatwar.enderman.wirr").getString()),
                "Name des Wirrwandlers: " + wandler.getName().getString());
        Leermilbe milbe = helper.spawnWithNoFreeWill(ModEntities.ENDERMITE.get(), new BlockPos(6, 1, 6));
        helper.runAfterDelay(10, () -> {
            helper.assertTrue(wandler.isAlive() && milbe.isAlive(), "nicht mehr am Leben");
            helper.succeed();
        });
    }

    /**
     * Ein Vanilla-Enderman mit Block in den Händen wird ein Leerwandler, der den Block weiterträgt; eine Vanilla-Endermite
     * wird eine Leermilbe (gleiche UUID).
     */
    private static void vanillaReplaced(GameTestHelper helper) {
        floor(helper);
        ServerLevel level = helper.getLevel();
        Enderman enderman = EntityTypes.ENDERMAN.create(level, EntitySpawnReason.SPAWN_ITEM_USE);
        enderman.setCarriedBlock(Blocks.GRASS_BLOCK.defaultBlockState());
        enderman.setNoAi(true);
        place(helper, enderman, new BlockPos(2, 1, 2));
        Endermite mite = EntityTypes.ENDERMITE.create(level, EntitySpawnReason.SPAWN_ITEM_USE);
        mite.setNoAi(true);
        place(helper, mite, new BlockPos(6, 1, 6));
        UUID e = enderman.getUUID(), m = mite.getUUID();
        helper.runAfterDelay(2, () -> {
            helper.assertTrue(level.getEntity(e) instanceof Leerwandler ours && ours.getCarriedBlock() != null
                            && ours.getCarriedBlock().is(Blocks.GRASS_BLOCK), "Leerwandler ohne Block: " + level.getEntity(e));
            helper.assertTrue(level.getEntity(e) instanceof Leerwandler ours && ours.getLook() == Leerwandler.Look.LEERE,
                    "in der Oberwelt kein Leerwandler");
            helper.assertTrue(level.getEntity(m) instanceof Leermilbe, "keine Leermilbe: " + level.getEntity(m));
            for (Mob mob : level.getEntitiesOfClass(Mob.class, arena(helper))) {
                helper.assertTrue(!pairs().containsKey(mob.getType()), "noch ein Vanilla-Wesen: " + mob.getType().getDescriptionId());
            }
            helper.succeed();
        });
    }

    /** Aus einer geworfenen Enderperle kriecht manchmal eine Milbe – es ist eine Leermilbe (das Spiel erzeugt eine Endermite). */
    private static void pearlMite(GameTestHelper helper) {
        floor(helper);
        ServerLevel level = helper.getLevel();
        Endermite vanilla = EntityTypes.ENDERMITE.create(level, EntitySpawnReason.TRIGGERED);   // so legt die Perle sie an
        place(helper, vanilla, new BlockPos(4, 1, 4));
        helper.runAfterDelay(2, () -> {
            var mites = level.getEntitiesOfClass(Endermite.class, arena(helper));
            helper.assertTrue(mites.size() == 1 && mites.getFirst() instanceof Leermilbe, "keine Leermilbe: " + mites);
            helper.succeed();
        });
    }

    /** Leerwandler jagen Leermilben (wie Endermen Endermiten). */
    private static void walkerHuntsMites(GameTestHelper helper) {
        floor(helper);
        Leerwandler wandler = helper.spawn(ModEntities.ENDERMAN.get(), new BlockPos(2, 1, 4));
        Leermilbe milbe = helper.spawnWithNoFreeWill(ModEntities.ENDERMITE.get(), new BlockPos(6, 1, 4));
        milbe.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.RESISTANCE, 400, 9));
        helper.succeedWhen(() -> helper.assertTrue(wandler.getTarget() == milbe, "der Leerwandler jagt die Milbe nicht"));
    }
}
