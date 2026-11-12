package io.github.sirschrolli.kingdomsatwar.gametest;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.item.voidrealm.StarwalkerArmor;
import io.github.sirschrolli.kingdomsatwar.registry.VoidBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.VoidGear;
import io.github.sirschrolli.kingdomsatwar.voidrealm.VoidBiomes;
import io.github.sirschrolli.kingdomsatwar.voidrealm.feature.StardustCraterFeature;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * Tests zu 0.99 in der Leere: Himmelseisen (Erz, Beute, Adern im Leerenstein, Kern der gefallenen Sterne) und die Rüstung
 * der Sternwandler (Schutz, Schwerkraft, Sternenschweben, Sternenanker).
 */
final class StarWeaverGameTests {
    private static final Identifier ARENA = KingdomsAtWar.id("test_arena");

    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> SKY_IRON =
            ModGameTests.TEST_FUNCTIONS.register("star_weaver_sky_iron", () -> StarWeaverGameTests::skyIron);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> ARMOR =
            ModGameTests.TEST_FUNCTIONS.register("star_weaver_armor", () -> StarWeaverGameTests::armor);

    private StarWeaverGameTests() {}

    /** Lädt die Klasse (meldet die Testfunktionen an). */
    static void init() {}

    static void register(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment) {
        for (var test : List.of(SKY_IRON, ARMOR)) {
            event.registerTest(test.getId(), new FunctionGameTestInstance(test.key(),
                    new TestData<>(environment, Level.OVERWORLD, ARENA, 400, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
        }
    }

    // =====================================================================================
    // Himmelseisen
    // =====================================================================================

    /** Das Erz gibt rohes Himmelseisen, wächst in allen Biomen der Leere als Ader im Leerenstein und im Kern gefallener Sterne. */
    private static void skyIron(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos center = helper.absolutePos(new BlockPos(7, 6, 7));
        List<ItemStack> drops = Block.getDrops(VoidGear.SKY_IRON_ORE.get().defaultBlockState(), level, center, null, null,
                new ItemStack(Items.NETHERITE_PICKAXE));
        helper.assertTrue(drops.stream().anyMatch(s -> s.is(VoidGear.RAW_SKY_IRON.get())), "Das Erz gibt kein rohes Himmelseisen: " + drops);
        helper.assertTrue(VoidGear.SKY_IRON_ORE.get().defaultBlockState().getLightEmission() > 0, "Das Erz schimmert nicht");

        // in jedem Biom der Leere
        var biomes = level.registryAccess().lookupOrThrow(Registries.BIOME);
        var placedKey = ResourceKey.create(Registries.PLACED_FEATURE, KingdomsAtWar.id("void/ore_sky_iron"));
        for (var key : VoidBiomes.ALL) {
            boolean found = false;
            for (var step : biomes.getOrThrow(key).value().getGenerationSettings().features()) {
                for (var placed : step) {
                    found |= placed.is(placedKey);
                }
            }
            helper.assertTrue(found, "Kein Himmelseisen im Biom " + key.identifier());
        }

        // eine Ader im Leerenstein
        var feature = level.registryAccess().lookupOrThrow(Registries.FEATURE)
                .getOrThrow(ResourceKey.create(Registries.FEATURE, KingdomsAtWar.id("void/ore_sky_iron"))).value();
        var generator = level.getChunkSource().getGenerator();
        Block stone = VoidBlocks.VOID_STONE.base().get();
        for (BlockPos p : BlockPos.betweenClosed(center.offset(-3, -3, -3), center.offset(3, 3, 3))) {
            level.setBlock(p, stone.defaultBlockState(), Block.UPDATE_CLIENTS);
        }
        int grown = 0;
        for (int seed = 0; seed < 6 && grown == 0; seed++) {
            feature.place(level, generator, RandomSource.create(seed), center);
            grown = count(level, center, 3, VoidGear.SKY_IRON_ORE.get());
        }
        helper.assertTrue(grown > 0, "Keine Himmelseisenader im Leerenstein");
        clear(level, center, 3);

        // ein Sternenkrater: im gefallenen Stern steckt Himmelseisen
        BlockPos floor = helper.absolutePos(new BlockPos(7, 2, 7));
        for (BlockPos p : BlockPos.betweenClosed(floor.offset(-7, -3, -7), floor.offset(7, 0, 7))) {
            level.setBlock(p, (p.getY() == floor.getY() ? VoidBlocks.STARDUST.get() : stone).defaultBlockState(), Block.UPDATE_CLIENTS);
        }
        int core = 0;
        for (int seed = 0; seed < 8 && core == 0; seed++) {
            new StardustCraterFeature().place(level, generator, RandomSource.create(seed), floor.above());
            core = count(level, floor, 7, VoidGear.SKY_IRON_ORE.get());
        }
        helper.assertTrue(core > 0, "Im gefallenen Stern steckt kein Himmelseisen");
        clear(level, floor, 7);
        helper.succeed();
    }

    private static int count(ServerLevel level, BlockPos center, int r, Block block) {
        int n = 0;
        for (BlockPos p : BlockPos.betweenClosed(center.offset(-r, -r, -r), center.offset(r, r, r))) {
            n += level.getBlockState(p).is(block) ? 1 : 0;
        }
        return n;
    }

    private static void clear(ServerLevel level, BlockPos center, int r) {
        for (BlockPos p : BlockPos.betweenClosed(center.offset(-r, -r, -r), center.offset(r, r, r))) {
            level.setBlock(p, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    // =====================================================================================
    // Die Rüstung der Sternwandler
    // =====================================================================================

    /** Schutz, leichter mit jedem Teil, weiche Landung; Sternenschweben (nur mit vollem Satz) und Sternenanker mit Abklingzeit. */
    private static void armor(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        var set = VoidGear.starwalkerSet();
        EquipmentSlot[] slots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
        double[] armor = new double[1];
        double[] fall = new double[1];
        for (int i = 0; i < set.size(); i++) {
            var modifiers = new ItemStack(set.get(i).get()).getOrDefault(net.minecraft.core.component.DataComponents.ATTRIBUTE_MODIFIERS,
                    net.minecraft.world.item.component.ItemAttributeModifiers.EMPTY);
            modifiers.forEach(slots[i], (attribute, modifier) -> {
                if (attribute.is(Attributes.ARMOR)) {
                    armor[0] += modifier.amount();
                } else if (attribute.is(Attributes.SAFE_FALL_DISTANCE)) {
                    fall[0] += modifier.amount();
                }
            });
        }
        helper.assertTrue(armor[0] >= 20.0, "Die Rüstung der Sternwandler schützt zu wenig: " + armor[0]);
        helper.assertTrue(fall[0] >= 3.0, "Die Stiefel fangen keine Stürze ab: " + fall[0]);

        ServerPlayer player = MageGameTests.survivalPlayer(helper, "test-starwalker", new io.netty.channel.embedded.EmbeddedChannel[1], 0);
        double gravity = player.getAttributeValue(Attributes.GRAVITY);
        player.setItemSlot(EquipmentSlot.CHEST, new ItemStack(set.get(1).get()));
        helper.assertTrue(StarwalkerArmor.pieces(player) == 1 && !StarwalkerArmor.fullSet(player), "Ein Teil zählt nicht richtig");
        helper.assertFalse(StarwalkerArmor.anchor(player, new Vec3(0.0, 100.0, 0.0)), "Der Anker trägt schon mit einem Teil");
        for (int i = 0; i < set.size(); i++) {
            player.setItemSlot(slots[i], new ItemStack(set.get(i).get()));
        }
        // die Attribute der Ausrüstung setzt das Spiel erst im nächsten Tick – hier von Hand wie das Spiel
        for (int i = 0; i < set.size(); i++) {
            new ItemStack(set.get(i).get()).forEachModifier(slots[i], (attribute, modifier) -> {
                var instance = player.getAttribute(attribute);
                if (instance != null && !instance.hasModifier(modifier.id())) {
                    instance.addTransientModifier(modifier);
                }
            });
        }
        double light = player.getAttributeValue(Attributes.GRAVITY);
        helper.assertTrue(StarwalkerArmor.fullSet(player), "Der volle Satz zählt nicht");
        helper.assertTrue(light < gravity * 0.85 && light > gravity * 0.7, "Die Rüstung macht nicht leichter: " + gravity + " → " + light);

        // Sternenschweben: ein kurzer, unsichtbarer Sinkflug, der beim Loslassen endet (ein Trank bleibt)
        helper.assertTrue(StarwalkerArmor.glide(player, true), "Kein Sternenschweben");
        MobEffectInstance glide = player.getEffect(MobEffects.SLOW_FALLING);
        helper.assertTrue(glide != null && glide.isAmbient() && !glide.isVisible(), "Der Sinkflug fehlt: " + glide);
        helper.assertFalse(StarwalkerArmor.glide(player, false), "Schweben ohne Taste");
        helper.assertTrue(player.getEffect(MobEffects.SLOW_FALLING) == null, "Der Sinkflug endet nicht");
        player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 1200));
        StarwalkerArmor.glide(player, false);
        helper.assertTrue(player.getEffect(MobEffects.SLOW_FALLING) != null, "Die Rüstung nimmt einen Trank des sanften Falls weg");
        player.removeEffect(MobEffects.SLOW_FALLING);

        // Sternenanker: zurück auf festen Boden, dann eine Weile nicht
        BlockPos ground = helper.absolutePos(new BlockPos(3, 1, 3));
        Vec3 safe = Vec3.atBottomCenterOf(ground.above());
        player.teleportTo(level, safe.x + 20.0, level.getMinY() - 8.0, safe.z, java.util.Set.of(), 0.0f, 0.0f, true);
        player.setDeltaMovement(0.0, -2.0, 0.0);
        player.fallDistance = 40.0;
        helper.assertTrue(StarwalkerArmor.anchorDepth(level) > player.getY(), "Unter der Welt greift der Anker nicht");
        helper.assertTrue(StarwalkerArmor.anchor(player, safe), "Der Anker trägt nicht zurück");
        helper.assertTrue(player.position().distanceTo(safe) < 0.5, "Nicht auf dem festen Boden: " + player.position());
        helper.assertTrue(player.fallDistance == 0.0 && player.getDeltaMovement().lengthSqr() < 1.0E-4, "Der Sturz geht weiter");
        helper.assertFalse(StarwalkerArmor.anchorReady(player), "Keine Abklingzeit");
        helper.assertFalse(StarwalkerArmor.anchor(player, safe), "Der Anker trägt gleich noch einmal");
        level.getServer().getPlayerList().remove(player);
        helper.succeed();
    }
}
