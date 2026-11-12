package io.github.sirschrolli.kingdomsatwar.gametest;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.abyss.AbyssBiomes;
import io.github.sirschrolli.kingdomsatwar.abyss.feature.SeamVeinFeature;
import io.github.sirschrolli.kingdomsatwar.item.abyss.TimewardenArmor;
import io.github.sirschrolli.kingdomsatwar.registry.AbyssBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.AbyssGear;
import io.github.sirschrolli.kingdomsatwar.registry.ModTags;
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
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * Tests der Erweiterung des Abgrunds (seit 1.01): Chronit (Erz, Drops, an den Weltennähten), die Rüstung der Zeitwächter
 * (Schutz, Die Zeit stockt, Zweite Chance mit Totem und Abklingzeit).
 */
final class ClockTowerGameTests {
    private static final Identifier ARENA = KingdomsAtWar.id("test_arena");
    private static final EquipmentSlot[] SLOTS = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> CHRONIT =
            ModGameTests.TEST_FUNCTIONS.register("clock_chronit", () -> ClockTowerGameTests::chronit);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> ARMOR =
            ModGameTests.TEST_FUNCTIONS.register("clock_armor", () -> ClockTowerGameTests::armor);

    private ClockTowerGameTests() {}

    /** Lädt die Klasse (meldet die Testfunktionen an). */
    static void init() {}

    static void register(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment) {
        for (var test : List.of(CHRONIT, ARMOR)) {
            event.registerTest(test.getId(), new FunctionGameTestInstance(test.key(),
                    new TestData<>(environment, Level.OVERWORLD, ARENA, 400, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
        }
    }

    // =====================================================================================
    // Chronit
    // =====================================================================================

    /**
     * Das Erz gibt Chronit (ab einer Diamantspitzhacke) und schimmert; Chronit wächst in jedem Biom des Abgrunds unter und neben
     * den Weltennähten; Rezepte für Schmelzen, Block und Rüstung gibt es.
     */
    private static void chronit(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockState ore = AbyssGear.CHRONIT_ORE.get().defaultBlockState();
        BlockPos center = helper.absolutePos(new BlockPos(7, 4, 7));
        List<ItemStack> drops = Block.getDrops(ore, level, center, null, null, new ItemStack(Items.DIAMOND_PICKAXE));
        helper.assertTrue(drops.stream().anyMatch(s -> s.is(AbyssGear.CHRONIT.get())), "Das Erz gibt kein Chronit: " + drops);
        helper.assertTrue(ore.getLightEmission() > 0 && AbyssGear.CHRONIT_BLOCK.get().defaultBlockState().getLightEmission() > 0,
                "Chronit schimmert nicht");
        helper.assertTrue(ore.is(BlockTags.NEEDS_DIAMOND_TOOL) && ore.is(BlockTags.MINEABLE_WITH_PICKAXE), "Das Erz braucht keine Diamantspitzhacke");
        helper.assertTrue(new ItemStack(Items.DIAMOND_PICKAXE).isCorrectToolForDrops(ore), "Die Diamantspitzhacke baut es nicht ab");
        helper.assertFalse(new ItemStack(Items.IRON_PICKAXE).isCorrectToolForDrops(ore), "Schon die Eisenspitzhacke baut es ab");
        helper.assertTrue(ore.is(ModTags.Blocks.ORES_CHRONIT) && new ItemStack(AbyssGear.CHRONIT.get()).is(ModTags.Items.GEMS_CHRONIT),
                "Die Tags des Chronits fehlen");

        // in jedem Biom des Abgrunds gibt es Weltennähte (und damit Chronit)
        var biomes = level.registryAccess().lookupOrThrow(Registries.BIOME);
        var seams = ResourceKey.create(Registries.PLACED_FEATURE, KingdomsAtWar.id("abyss/world_seams"));
        for (var key : AbyssBiomes.ALL) {
            boolean found = false;
            for (var step : biomes.getOrThrow(key).value().getGenerationSettings().features()) {
                for (var placed : step) {
                    found |= placed.is(seams);
                }
            }
            helper.assertTrue(found, "Keine Weltennähte im Biom " + key.identifier());
        }

        // eine Weltennaht über einem Feld aus Chaosgestein: darunter und daneben gerinnt Chronit
        Block stone = AbyssBlocks.CHAOS_STONE.base().get();
        for (BlockPos p : BlockPos.betweenClosed(center.offset(-7, -2, -7), center.offset(7, 0, 7))) {
            level.setBlock(p, stone.defaultBlockState(), Block.UPDATE_CLIENTS);
        }
        var generator = level.getChunkSource().getGenerator();
        int seam = 0;
        int chronit = 0;
        for (int seed = 0; seed < 24 && chronit < 2; seed++) {
            new SeamVeinFeature().place(level, generator, RandomSource.create(seed), center.above());
            seam = count(level, center, AbyssBlocks.WORLD_SEAM.get());
            chronit = count(level, center, AbyssGear.CHRONIT_ORE.get());
        }
        helper.assertTrue(seam > 0, "Keine Weltennaht auf dem Chaosgestein (Höhe " + center.getY() + ")");
        helper.assertTrue(chronit >= 2, "An der Weltennaht wächst kaum Chronit: " + chronit + " bei " + seam + " Nahtblöcken");
        for (BlockPos p : BlockPos.betweenClosed(center.offset(-7, -2, -7), center.offset(7, 1, 7))) {
            level.setBlock(p, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
        }

        // Rezepte: Schmelzen, Hochofen, Block und zurück, die vier Teile der Rüstung
        var recipes = level.getServer().getRecipeManager().recipeMap();
        for (String id : List.of("chronit_from_smelting_chronit_ore", "chronit_from_blasting_chronit_ore", "chronit_block",
                "chronit_from_chronit_block", "timewarden_helmet", "timewarden_chestplate", "timewarden_leggings", "timewarden_boots")) {
            helper.assertTrue(recipes.byKey(ResourceKey.create(Registries.RECIPE, KingdomsAtWar.id(id))) != null, "Rezept fehlt: " + id);
        }
        helper.succeed();
    }

    private static int count(ServerLevel level, BlockPos center, Block block) {
        int n = 0;
        for (BlockPos p : BlockPos.betweenClosed(center.offset(-7, -2, -7), center.offset(7, 1, 7))) {
            n += level.getBlockState(p).is(block) ? 1 : 0;
        }
        return n;
    }

    // =====================================================================================
    // Die Rüstung der Zeitwächter
    // =====================================================================================

    /**
     * Schutz wie Netherit, feuerfest, mit Chronit auszubessern; wer den Träger trifft, stockt manchmal; mit dem vollen Satz spult
     * ein tödlicher Treffer drei Sekunden zurück (Ort, Herzen, schädliche Wirkungen fort), danach steht die Uhr still. Ein Totem
     * kommt zuerst, ohne den vollen Satz hilft nichts.
     */
    private static void armor(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        var set = AbyssGear.timewardenSet();
        double[] armor = new double[1];
        double[] toughness = new double[1];
        for (int i = 0; i < set.size(); i++) {
            ItemStack stack = new ItemStack(set.get(i).get());
            stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY).forEach(SLOTS[i], (attribute, modifier) -> {
                if (attribute.is(Attributes.ARMOR)) {
                    armor[0] += modifier.amount();
                } else if (attribute.is(Attributes.ARMOR_TOUGHNESS)) {
                    toughness[0] += modifier.amount();
                }
            });
            helper.assertTrue(stack.has(DataComponents.DAMAGE_RESISTANT), "Verbrennt in Lava: " + stack);
            helper.assertTrue(stack.isValidRepairItem(new ItemStack(AbyssGear.CHRONIT.get())), "Nicht mit Chronit auszubessern: " + stack);
        }
        helper.assertTrue(armor[0] >= 20.0 && toughness[0] >= 12.0, "Schutz wie Netherit fehlt: " + armor[0] + " / " + toughness[0]);

        ServerPlayer player = MageGameTests.survivalPlayer(helper, "test-timewarden", new io.netty.channel.embedded.EmbeddedChannel[1], 0);
        try {
            vulnerable(player);
            player.setItemSlot(EquipmentSlot.CHEST, new ItemStack(set.get(1).get()));
            helper.assertTrue(TimewardenArmor.pieces(player) == 1 && !TimewardenArmor.fullSet(player), "Ein Teil zählt nicht richtig");
            for (int i = 0; i < set.size(); i++) {
                player.setItemSlot(SLOTS[i], new ItemStack(set.get(i).get()));
            }
            helper.assertTrue(TimewardenArmor.fullSet(player), "Der volle Satz zählt nicht");

            // Die Zeit stockt: wer ihn trifft, wird manchmal langsamer (bei vier Teilen in etwa jedem dritten Treffer)
            Zombie zombie = io.github.sirschrolli.kingdomsatwar.registry.ModEntities.ZOMBIE.get().create(level, EntitySpawnReason.EVENT);
            helper.assertTrue(zombie != null, "Kein Zombie");
            zombie.snapTo(helper.absolutePos(new BlockPos(10, 1, 10)), 0.0f, 0.0f);
            zombie.setNoAi(true);
            level.addFreshEntity(zombie);
            TimewardenArmor.stall(level, zombie, player);
            MobEffectInstance slow = zombie.getEffect(MobEffects.SLOWNESS);
            helper.assertTrue(slow != null && slow.getAmplifier() >= 2 && zombie.hasEffect(MobEffects.MINING_FATIGUE), "Die Zeit stockt nicht: " + slow);
            zombie.removeAllEffects();
            int hits = 0;
            while (hits < 60 && !zombie.hasEffect(MobEffects.SLOWNESS)) {
                player.setHealth(player.getMaxHealth());
                ready(player);
                player.hurtServer(level, level.damageSources().mobAttack(zombie), 2.0f);
                hits++;
            }
            helper.assertTrue(zombie.hasEffect(MobEffects.SLOWNESS), "In 60 Treffern stockte die Zeit kein einziges Mal");
            zombie.discard();

            // Zweite Chance: der Weg der letzten drei Sekunden – erst hier (A, gesund), dann dort (B, angeschlagen)
            TimewardenArmor.resetCooldown(player);
            helper.assertTrue(TimewardenArmor.rewindReady(player), "Die Uhr tickt nicht");
            BlockPos a = helper.absolutePos(new BlockPos(3, 1, 3));
            BlockPos b = helper.absolutePos(new BlockPos(11, 1, 9));
            moveTo(player, a);
            player.setHealth(18.0f);
            for (int i = 0; i < 20; i++) {
                TimewardenArmor.remember(player);
            }
            moveTo(player, b);
            player.setHealth(3.0f);
            for (int i = 0; i < 30; i++) {
                TimewardenArmor.remember(player);
            }
            TimewardenArmor.Moment oldest = TimewardenArmor.oldest(player);
            helper.assertTrue(oldest != null && oldest.pos().distanceTo(Vec3.atBottomCenterOf(a)) < 0.6, "Der Weg wird nicht gemerkt: " + oldest);
            player.addEffect(new MobEffectInstance(MobEffects.POISON, 200, 1));
            player.addEffect(new MobEffectInstance(MobEffects.SPEED, 200));
            player.igniteForSeconds(5.0f);
            ready(player);
            player.hurtServer(level, level.damageSources().generic(), 10_000.0f);
            helper.assertTrue(player.isAlive() && !player.isDeadOrDying(), "Die Zeit spult nicht zurück: " + player.getHealth());
            helper.assertTrue(player.position().distanceTo(Vec3.atBottomCenterOf(a)) < 0.6, "Nicht zurück an der alten Stelle: " + player.position());
            helper.assertTrue(Math.abs(player.getHealth() - 18.0f) < 0.01f, "Nicht die Herzen von damals: " + player.getHealth());
            helper.assertFalse(player.hasEffect(MobEffects.POISON) || player.isOnFire(), "Gift oder Feuer bleiben");
            helper.assertTrue(player.hasEffect(MobEffects.SPEED), "Auch die guten Wirkungen sind fort");
            helper.assertTrue(player.isInvulnerable(), "Nach dem Zurückspulen nicht kurz unverwundbar");
            helper.assertFalse(TimewardenArmor.rewindReady(player), "Die Uhr steht danach nicht still");
            helper.assertTrue(TimewardenArmor.oldest(player) == null, "Der alte Weg bleibt gemerkt");

            // war man noch nicht lange genug unterwegs, bleibt man stehen – mit mindestens vier Herzen
            TimewardenArmor.resetCooldown(player);
            moveTo(player, b);
            player.setHealth(2.0f);
            TimewardenArmor.remember(player);
            ready(player);
            player.hurtServer(level, level.damageSources().generic(), 10_000.0f);
            helper.assertTrue(player.isAlive() && player.getHealth() >= TimewardenArmor.MIN_HEALTH - 0.01f,
                    "Weniger als vier Herzen nach dem Zurückspulen: " + player.getHealth());

            // ein Totem kommt zuerst (die Uhr bleibt bereit)
            TimewardenArmor.resetCooldown(player);
            player.setHealth(player.getMaxHealth());
            ready(player);
            player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.TOTEM_OF_UNDYING));
            player.hurtServer(level, level.damageSources().generic(), 10_000.0f);
            helper.assertTrue(player.isAlive() && player.getOffhandItem().isEmpty(), "Das Totem wirkt nicht zuerst: " + player.getOffhandItem());
            helper.assertTrue(TimewardenArmor.rewindReady(player), "Nach dem Totem steht die Uhr still");

            // ohne den vollen Satz hilft nichts
            player.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
            player.removeAllEffects();
            player.setAbsorptionAmount(0.0f);
            player.setHealth(player.getMaxHealth());
            ready(player);
            player.hurtServer(level, level.damageSources().generic(), 10_000.0f);
            helper.assertTrue(player.isDeadOrDying(), "Ohne Helm spult die Zeit trotzdem zurück");
            helper.succeed();
        } finally {
            level.getServer().getPlayerList().remove(player);
        }
    }

    /** Ein Testspieler, der Schaden nimmt (im Spiel angekommen, nicht unverwundbar, keine Kreativkräfte). */
    private static void vulnerable(ServerPlayer player) {
        player.connection.markClientLoaded();
        player.getAbilities().invulnerable = false;
        player.getAbilities().instabuild = false;
        player.setData(io.github.sirschrolli.kingdomsatwar.registry.ModAttachments.HERO_IMMORTAL, -1L);
        ready(player);
    }

    /** Keine Schonzeit nach einem Treffer. */
    private static void ready(ServerPlayer player) {
        player.damageCooldownTime = 0;
        player.setInvulnerableTime(0);
    }

    private static void moveTo(ServerPlayer player, BlockPos pos) {
        player.teleportTo(player.level(), pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, java.util.Set.of(), 0.0f, 0.0f, true);
        player.level().getChunkSource().move(player);
    }
}
