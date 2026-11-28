package io.github.sirschrolli.kingdomsatwar.gametest;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.entity.ReplacesVanilla;
import io.github.sirschrolli.kingdomsatwar.entity.creature.farm.FarmAnimalConversion;
import io.github.sirschrolli.kingdomsatwar.entity.creature.farm.FarmPig;
import io.github.sirschrolli.kingdomsatwar.entity.creature.golem.IronSentinel;
import io.github.sirschrolli.kingdomsatwar.entity.creature.monster.PlantCreeper;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import java.util.ArrayList;
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
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * Tests der „Vanilla-Identität“: Die eigenen Wesen gelten für Vanilla-Code als das Wesen, das sie ersetzen
 * ({@code entity.is(EntityTypes.X)}), damit dessen Verhalten unverändert bleibt.
 */
final class IdentityGameTests {
    private static final Identifier ARENA = KingdomsAtWar.id("test_arena");

    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> ALL_TYPES =
            ModGameTests.TEST_FUNCTIONS.register("identity_all_types", () -> IdentityGameTests::allTypes);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> GOLEM_CREEPER =
            ModGameTests.TEST_FUNCTIONS.register("identity_golem_spares_creeper", () -> IdentityGameTests::golemSparesCreeper);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> PIG_BOOST =
            ModGameTests.TEST_FUNCTIONS.register("identity_pig_boost", () -> IdentityGameTests::pigBoost);

    private IdentityGameTests() {}

    static void init() {}

    static void register(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment) {
        for (var test : List.of(ALL_TYPES, GOLEM_CREEPER, PIG_BOOST)) {
            event.registerTest(test.getId(), new FunctionGameTestInstance(test.key(),
                    new TestData<>(environment, Level.OVERWORLD, ARENA, 200, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
        }
    }

    static ServerPlayer rider(GameTestHelper helper, BlockPos relative) {
        @SuppressWarnings("removal")
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.SURVIVAL);
        player.getInventory().clearContent();
        Vec3 at = helper.absoluteVec(Vec3.atBottomCenterOf(relative));
        player.snapTo(at.x, at.y, at.z, 0.0f, 0.0f);
        return player;
    }

    /**
     * Jedes eigene Wesen, dessen Klasse die Vanilla-Klasse erweitert, gilt als das ersetzte Wesen und als es selbst;
     * eines mit anderer Klasse (das Hausrind, der eigene Dorfbewohner) gibt sich nicht dafür aus – sonst schlüge eine
     * Umwandlung in die Vanilla-Klasse fehl.
     */
    private static void allTypes(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        List<String> problems = new ArrayList<>();
        int checked = 0;
        for (EntityType<?> vanilla : FarmAnimalConversion.replacedTypes()) {
            EntityType<? extends Mob> ownType = FarmAnimalConversion.replacementType(vanilla);
            Entity original = vanilla.create(level, EntitySpawnReason.COMMAND);
            Entity own = ownType.create(level, EntitySpawnReason.COMMAND);
            if (original == null || own == null) {
                problems.add("nicht erzeugbar: " + ownType.getDescriptionId());
                continue;
            }
            checked++;
            boolean subclass = original.getClass().isInstance(own);
            if (subclass && !(own instanceof ReplacesVanilla)) {
                problems.add(own.getClass().getSimpleName() + " ohne ReplacesVanilla");
            }
            if (!own.is(ownType)) {
                problems.add(own.getClass().getSimpleName() + " ist nicht mehr es selbst");
            }
            if (subclass != own.is(vanilla) && FarmAnimalConversion.vanillaOf(ownType) == vanilla) {
                problems.add(own.getClass().getSimpleName() + (subclass ? " gilt nicht als " : " gibt sich aus als ")
                        + vanilla.getDescriptionId());
            }
            if (original.is(ownType)) {
                problems.add("das Vanilla-Wesen " + vanilla.getDescriptionId() + " gilt als das eigene");
            }
            own.discard();
            original.discard();
        }
        helper.assertTrue(problems.isEmpty(), String.join("; ", problems));
        helper.assertTrue(checked > 70, "nur " + checked + " Typen geprüft");
        helper.succeed();
    }

    /** Ein Eisenwächter greift keinen Kriecher an (Vanilla-Eisengolems meiden Creeper, damit das Dorf nicht in die Luft fliegt). */
    private static void golemSparesCreeper(GameTestHelper helper) {
        IronSentinel golem = helper.spawnWithNoFreeWill(ModEntities.IRON_GOLEM.get(), new BlockPos(2, 1, 2));
        PlantCreeper creeper = helper.spawnWithNoFreeWill(ModEntities.CREEPER.get(), new BlockPos(5, 1, 5));
        helper.assertFalse(golem.canAttack(creeper), "der Eisenwächter würde den Kriecher angreifen");
        helper.succeed();
    }

    /** Auf dem gesattelten Hausschwein treibt die Karottenrute das Schwein an – wie bei Vanilla. */
    private static void pigBoost(GameTestHelper helper) {
        for (int x = 0; x < 9; x++) {
            for (int z = 0; z < 9; z++) {
                helper.setBlock(x, 0, z, Blocks.GRASS_BLOCK);
            }
        }
        FarmPig pig = helper.spawn(ModEntities.FARM_PIG.get(), new BlockPos(4, 1, 4));
        pig.setItemSlot(EquipmentSlot.SADDLE, new ItemStack(Items.SADDLE));
        ServerPlayer player = rider(helper, new BlockPos(4, 1, 3));
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.CARROT_ON_A_STICK));
        helper.assertTrue(player.startRiding(pig, true, false), "Aufsitzen misslungen");
        helper.assertTrue(player.getControlledVehicle() == pig, "das Schwein lässt sich nicht lenken");
        InteractionResult result = player.getMainHandItem().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        helper.assertTrue(result.consumesAction(), "die Karottenrute treibt das Schwein nicht an: " + result);
        player.stopRiding();
        player.discard();
        helper.succeed();
    }
}
