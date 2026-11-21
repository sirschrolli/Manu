package io.github.sirschrolli.kingdomsatwar.gametest;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.item.deep.ListenerArmor;
import io.github.sirschrolli.kingdomsatwar.registry.EchoGear;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.ModTags;
import io.github.sirschrolli.kingdomsatwar.worldgen.ModWorldgen;
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
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SculkSensorBlock;
import net.minecraft.world.level.block.entity.SculkSensorBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SculkSensorPhase;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * Tests der Tiefen Dunkelheit (seit 1.6): Hallkristall (Erz, Drops, Adern nur in der Tiefen Dunkelheit, Rezepte) und die
 * Rüstung der Lauscher (Schutz, Schallschutz, klare Sicht, Wollsohlen gegen Sculk-Sensoren, Echoortung).
 */
final class EchoGameTests {
    private static final Identifier ARENA = KingdomsAtWar.id("test_arena");
    private static final EquipmentSlot[] SLOTS = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
    /** Die Plattform der Rüstungsprobe: eine Höhe, in der kein anderer Test baut (sie reicht über das eigene Testfeld hinaus). */
    private static final int HEIGHT = 150;

    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> CRYSTAL =
            ModGameTests.TEST_FUNCTIONS.register("echo_crystal", () -> EchoGameTests::crystal);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> ARMOR =
            ModGameTests.TEST_FUNCTIONS.register("echo_listener_armor", () -> EchoGameTests::armor);

    private EchoGameTests() {}

    /** Lädt die Klasse (meldet die Testfunktionen an). */
    static void init() {}

    static void register(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment) {
        for (var test : List.of(CRYSTAL, ARMOR)) {
            event.registerTest(test.getId(), new FunctionGameTestInstance(test.key(),
                    new TestData<>(environment, Level.OVERWORLD, ARENA, 300, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
        }
    }

    // =====================================================================================
    // Hallkristall
    // =====================================================================================

    /**
     * Das Erz gibt Hallkristall (ab einer Diamantspitzhacke) und gibt selbst kein Licht; Adern gibt es nur in der Tiefen
     * Dunkelheit, und eine Ader setzt im Tiefenschiefer Kristalle; der Block leuchtet; Rezepte für Schmelzen, Block und Rüstung.
     */
    private static void crystal(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos center = helper.absolutePos(new BlockPos(4, 3, 4));
        BlockState ore = EchoGear.ECHO_CRYSTAL_ORE.get().defaultBlockState();
        List<ItemStack> drops = Block.getDrops(ore, level, center, null, null, new ItemStack(Items.DIAMOND_PICKAXE));
        helper.assertTrue(drops.stream().anyMatch(s -> s.is(EchoGear.ECHO_CRYSTAL.get())), "Das Erz gibt keinen Hallkristall: " + drops);
        helper.assertTrue(ore.getLightEmission() == 0, "Das Erz gibt Licht ab");
        helper.assertTrue(ore.is(BlockTags.NEEDS_DIAMOND_TOOL) && ore.is(BlockTags.MINEABLE_WITH_PICKAXE), "Das Erz braucht keine Diamantspitzhacke");
        helper.assertFalse(new ItemStack(Items.IRON_PICKAXE).isCorrectToolForDrops(ore), "Schon die Eisenspitzhacke baut es ab");
        helper.assertTrue(ore.is(ModTags.Blocks.ORES_ECHO_CRYSTAL) && ore.is(net.neoforged.neoforge.common.Tags.Blocks.ORES), "Der Erz-Tag fehlt");
        BlockState block = EchoGear.ECHO_CRYSTAL_BLOCK.get().defaultBlockState();
        helper.assertTrue(block.getLightEmission() >= 6, "Der Block leuchtet nicht");
        helper.assertTrue(block.is(BlockTags.BEACON_BASE_BLOCKS), "Der Block trägt kein Leuchtfeuer");
        helper.assertTrue(new ItemStack(EchoGear.ECHO_CRYSTAL.get()).is(ModTags.Items.GEMS_ECHO_CRYSTAL), "Hallkristall hat keinen Edelstein-Tag");

        // Adern nur in der Tiefen Dunkelheit – nicht in den anderen Höhlen und nicht an der Oberfläche
        var biomes = level.registryAccess().lookupOrThrow(Registries.BIOME);
        for (var key : List.of(Biomes.DEEP_DARK, Biomes.DRIPSTONE_CAVES, Biomes.LUSH_CAVES, Biomes.PLAINS, Biomes.DARK_FOREST)) {
            boolean found = false;
            for (var step : biomes.getOrThrow(key).value().getGenerationSettings().features()) {
                for (var placed : step) {
                    found |= placed.is(ModWorldgen.ORE_ECHO_CRYSTAL_PLACED);
                }
            }
            helper.assertTrue(found == (key == Biomes.DEEP_DARK), (found ? "Hallkristall-Adern im Biom " : "Keine Hallkristall-Adern im Biom ") + key.identifier());
        }

        // eine Ader in einem Feld aus Tiefenschiefer
        for (BlockPos p : BlockPos.betweenClosed(center.offset(-3, -2, -3), center.offset(3, 2, 3))) {
            level.setBlock(p, Blocks.DEEPSLATE.defaultBlockState(), Block.UPDATE_CLIENTS);
        }
        var feature = level.registryAccess().lookupOrThrow(Registries.FEATURE).getOrThrow(ModWorldgen.ORE_ECHO_CRYSTAL).value();
        var generator = level.getChunkSource().getGenerator();
        int crystals = 0;
        for (int seed = 0; seed < 40 && crystals == 0; seed++) {
            feature.place(level, generator, RandomSource.create(seed), center);
            crystals = count(level, center, EchoGear.ECHO_CRYSTAL_ORE.get());
        }
        for (BlockPos p : BlockPos.betweenClosed(center.offset(-3, -2, -3), center.offset(3, 2, 3))) {
            level.setBlock(p, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
        }
        helper.assertTrue(crystals > 0, "Die Ader setzt keine Kristalle in den Tiefenschiefer");

        var recipes = level.getServer().getRecipeManager().recipeMap();
        for (String id : List.of("echo_crystal_from_smelting_echo_crystal_ore", "echo_crystal_from_blasting_echo_crystal_ore", "echo_crystal_block",
                "echo_crystal_from_echo_crystal_block", "listener_helmet", "listener_chestplate", "listener_leggings", "listener_boots")) {
            helper.assertTrue(recipes.byKey(ResourceKey.create(Registries.RECIPE, KingdomsAtWar.id(id))) != null, "Rezept fehlt: " + id);
        }
        helper.succeed();
    }

    private static int count(ServerLevel level, BlockPos center, Block block) {
        int n = 0;
        for (BlockPos p : BlockPos.betweenClosed(center.offset(-3, -2, -3), center.offset(3, 2, 3))) {
            n += level.getBlockState(p).is(block) ? 1 : 0;
        }
        return n;
    }

    // =====================================================================================
    // Die Rüstung der Lauscher
    // =====================================================================================

    /**
     * Schutz wie Diamant, mit Hallkristall auszubessern; jeder Teil fängt ein Fünftel des Schallschlags ab; der Helm hält die
     * Dunkelheit von Tiefenhüter und Kreischer fern (die Finsternis anderer Gegner nicht); mit den Wollsohlen hört ein
     * Sculk-Sensor die Schritte nicht (ohne sie schon); die Echoortung lässt Wesen in der Nähe aufleuchten (ferne nicht) und
     * ruft nur beim Schleichen im ganzen Satz.
     */
    private static void armor(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        var set = EchoGear.listenerSet();
        double[] armor = new double[1];
        for (int i = 0; i < set.size(); i++) {
            ItemStack stack = new ItemStack(set.get(i).get());
            stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY).forEach(SLOTS[i], (attribute, modifier) -> {
                if (attribute.is(Attributes.ARMOR)) {
                    armor[0] += modifier.amount();
                }
            });
            helper.assertTrue(stack.isValidRepairItem(new ItemStack(EchoGear.ECHO_CRYSTAL.get())), "Nicht mit Hallkristall auszubessern: " + stack);
        }
        helper.assertTrue(armor[0] >= 20.0, "Schutz wie Diamant fehlt: " + armor[0]);

        BlockPos center = helper.absolutePos(new BlockPos(4, HEIGHT, 4));
        for (BlockPos p : BlockPos.betweenClosed(center.offset(-6, -1, -6), center.offset(6, -1, 6))) {
            level.setBlock(p, Blocks.STONE.defaultBlockState(), Block.UPDATE_CLIENTS);
        }
        // der Sculk-Sensor kommt erst, wenn alles angelegt ist (sonst hört er schon das Anlegen der Rüstung)
        BlockPos sensorPos = center.offset(3, 0, 0);

        ServerPlayer player = MageGameTests.survivalPlayer(helper, "test-listener", new io.netty.channel.embedded.EmbeddedChannel[1], 0);
        Mob target = ModEntities.ZOMBIE.get().create(level, EntitySpawnReason.MOB_SUMMONED);
        Mob near = ModEntities.ZOMBIE.get().create(level, EntitySpawnReason.MOB_SUMMONED);
        Mob far = ModEntities.ZOMBIE.get().create(level, EntitySpawnReason.MOB_SUMMONED);
        Warden warden = ModEntities.WARDEN.get().create(level, EntitySpawnReason.MOB_SUMMONED);
        Runnable cleanup = () -> {
            target.discard();
            near.discard();
            far.discard();
            warden.discard();
            level.getServer().getPlayerList().remove(player);
            for (BlockPos p : BlockPos.betweenClosed(center.offset(-6, -1, -6), center.offset(6, 0, 6))) {
                level.setBlock(p, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
            }
        };
        try {
            player.connection.markClientLoaded();
            player.getAbilities().invulnerable = false;
            player.teleportTo(level, center.getX() + 0.5, center.getY(), center.getZ() + 0.5, java.util.Set.of(), 0.0f, 0.0f, true);
            level.getChunkSource().move(player);
            for (int pieces = 0; pieces <= 4; pieces++) {
                for (int i = 0; i < 4; i++) {
                    player.setItemSlot(SLOTS[i], i < pieces ? new ItemStack(set.get(i).get()) : ItemStack.EMPTY);
                }
                helper.assertTrue(ListenerArmor.pieces(player) == pieces, "Teile zählen falsch: " + ListenerArmor.pieces(player));
            }

            // Schallschutz: ein Fünftel weniger je Teil (der Schallschlag geht sonst durch jede Rüstung)
            target.setNoAi(true);
            target.snapTo(center.getX() + 0.5, center.getY(), center.getZ() - 11.5, 0.0f, 0.0f);
            float bare = sonicLoss(level, target, player);
            for (int i = 0; i < 4; i++) {
                target.setItemSlot(SLOTS[i], new ItemStack(set.get(i).get()));
            }
            float armored = sonicLoss(level, target, player);
            helper.assertTrue(bare > 4.0f && Math.abs(armored / bare - 0.2f) < 0.06f,
                    "Der Schallschlag wird nicht um vier Fünftel gemindert: " + bare + " -> " + armored);

            // klare Sicht: die Dunkelheit aus der Tiefe nicht, die Finsternis anderer Gegner schon
            helper.assertFalse(player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 200), null), "Der Kreischer hüllt trotz Helm in Dunkelheit");
            helper.assertFalse(player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 200), warden), "Der Tiefenhüter hüllt trotz Helm in Dunkelheit");
            helper.assertFalse(player.hasEffect(MobEffects.DARKNESS), "Dunkelheit trotz Helm");
            helper.assertTrue(player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 200), target), "Der Helm hält auch die Finsternis anderer Gegner fern");
            player.removeEffect(MobEffects.DARKNESS);
            player.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
            helper.assertTrue(player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 200), null), "Ohne Helm keine Dunkelheit");
            player.removeEffect(MobEffects.DARKNESS);
            player.setItemSlot(EquipmentSlot.HEAD, new ItemStack(EchoGear.LISTENER_HELMET.get()));

            // die Echoortung: Wesen in der Nähe leuchten auf, ferne nicht; danach eine Wartezeit
            near.setNoAi(true);
            near.snapTo(center.getX() + 0.5, center.getY() + 4.0, center.getZ() + 10.5, 0.0f, 0.0f);
            far.setNoAi(true);
            far.snapTo(center.getX() + 0.5, center.getY() + 4.0, center.getZ() - 20.5, 0.0f, 0.0f);
            level.addFreshEntity(near);
            level.addFreshEntity(far);
            ListenerArmor.forget(player);
            player.setPose(Pose.STANDING);
            helper.assertFalse(ListenerArmor.listen(player), "Ruft, ohne zu schleichen");
            player.setPose(Pose.CROUCHING);
            player.setItemSlot(EquipmentSlot.FEET, ItemStack.EMPTY);
            helper.assertFalse(ListenerArmor.listen(player), "Ruft schon mit drei Teilen");
            player.setItemSlot(EquipmentSlot.FEET, new ItemStack(EchoGear.LISTENER_BOOTS.get()));
            helper.assertTrue(ListenerArmor.listen(player), "Ruft nicht, obwohl er im ganzen Satz schleicht");
            helper.assertTrue(near.hasEffect(MobEffects.GLOWING), "Das Wesen in der Nähe leuchtet nicht auf");
            helper.assertFalse(far.hasEffect(MobEffects.GLOWING), "Auch das ferne Wesen leuchtet auf");
            helper.assertFalse(player.hasEffect(MobEffects.GLOWING), "Der Rufer leuchtet selbst auf");
            helper.assertFalse(ListenerArmor.listen(player), "Gleich wieder ein Ruf (keine Wartezeit)");
            player.setPose(Pose.STANDING);

            // die Wollsohlen: Mit den Stiefeln erreicht ein Schritt keinen Lauscher, ohne sie schon
            freshSensor(level, sensorPos);
            Vec3 step = player.position();
            helper.assertFalse(CommonHooks.onVanillaGameEvent(level, GameEvent.STEP, step, GameEvent.Context.of(player)),
                    "Der Schritt geht trotz Wollsohlen hinaus");
            helper.assertTrue(CommonHooks.onVanillaGameEvent(level, GameEvent.BLOCK_PLACE, step, GameEvent.Context.of(player)),
                    "Die Wollsohlen schlucken auch, was nichts mit Schritten zu tun hat");
            level.gameEvent(player, GameEvent.STEP, step);
        } catch (RuntimeException e) {
            cleanup.run();
            throw e;
        }
        helper.runAfterDelay(15, () -> {
            try {
                helper.assertTrue(heard(level, sensorPos) == 0, "Der Sculk-Sensor hört die Schritte trotz Wollsohlen");
                player.setItemSlot(EquipmentSlot.FEET, ItemStack.EMPTY);
                helper.assertTrue(CommonHooks.onVanillaGameEvent(level, GameEvent.STEP, player.position(), GameEvent.Context.of(player)),
                        "Ohne Stiefel geht der Schritt nicht hinaus");
                // ein frischer Sensor: Er soll nur den Schritt hören, nicht das Ablegen der Stiefel
                freshSensor(level, sensorPos);
                level.gameEvent(player, GameEvent.STEP, player.position());
            } catch (RuntimeException e) {
                cleanup.run();
                throw e;
            }
        });
        helper.runAfterDelay(35, () -> {
            try {
                int frequency = heard(level, sensorPos);
                helper.assertTrue(frequency == VibrationSystem.getGameEventFrequency(GameEvent.STEP),
                        "Ohne Wollsohlen hört der Sculk-Sensor den Schritt nicht (Probe kaputt?): " + frequency);
                helper.succeed();
            } finally {
                cleanup.run();
            }
        });
    }

    /** Wie viel Leben ein Schallschlag von 10 nimmt. */
    private static float sonicLoss(ServerLevel level, Mob victim, ServerPlayer attacker) {
        victim.setHealth(victim.getMaxHealth());
        victim.setInvulnerableTime(0);
        victim.damageCooldownTime = 0;
        float before = victim.getHealth();
        victim.hurtServer(level, level.damageSources().sonicBoom(attacker), 10.0f);
        return before - victim.getHealth();
    }

    /** Setzt einen neuen Sculk-Sensor (der noch nichts gehört hat). */
    private static void freshSensor(ServerLevel level, BlockPos sensorPos) {
        level.setBlock(sensorPos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        level.setBlock(sensorPos, Blocks.SCULK_SENSOR.defaultBlockState(), Block.UPDATE_ALL);
    }

    /** Was der Sculk-Sensor zuletzt gehört hat (die Frequenz; 0 = nichts; -1 = er schlägt an, ohne etwas gehört zu haben). */
    private static int heard(ServerLevel level, BlockPos sensorPos) {
        BlockState state = level.getBlockState(sensorPos);
        int frequency = level.getBlockEntity(sensorPos) instanceof SculkSensorBlockEntity sensor ? sensor.getLastVibrationFrequency() : 0;
        return frequency == 0 && state.is(Blocks.SCULK_SENSOR) && SculkSensorBlock.getPhase(state) != SculkSensorPhase.INACTIVE ? -1 : frequency;
    }
}
