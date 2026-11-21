package io.github.sirschrolli.kingdomsatwar.gametest;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.item.end.VoidwalkerArmor;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.ModTags;
import io.github.sirschrolli.kingdomsatwar.registry.PhaseGear;
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
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enderman;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * Tests der Erweiterung des Ends (seit 1.4): Phasit (Erz, Drops, Adern auf den äußeren Inseln, Rezepte) und die Rüstung der
 * Leerwandler (Schutz, Phasenschritt, Wartezeit, schwebende Sohlen, ruhige Leerwandler).
 */
final class PhaseGameTests {
    private static final Identifier ARENA = KingdomsAtWar.id("test_arena");
    private static final EquipmentSlot[] SLOTS = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> PHASIT =
            ModGameTests.TEST_FUNCTIONS.register("phase_phasit", () -> PhaseGameTests::phasit);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> ARMOR =
            ModGameTests.TEST_FUNCTIONS.register("phase_armor", () -> PhaseGameTests::armor);

    private PhaseGameTests() {}

    /** Lädt die Klasse (meldet die Testfunktionen an). */
    static void init() {}

    static void register(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment) {
        for (var test : List.of(PHASIT, ARMOR)) {
            event.registerTest(test.getId(), new FunctionGameTestInstance(test.key(),
                    new TestData<>(environment, Level.OVERWORLD, ARENA, 300, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
        }
    }

    // =====================================================================================
    // Phasit
    // =====================================================================================

    /**
     * Das Erz gibt Phasit (ab einer Diamantspitzhacke) und schimmert; auf den äußeren Inseln gibt es Adern (nicht auf der
     * Hauptinsel), und eine Ader setzt im Endstein Kristalle; Rezepte für Schmelzen, Block und Rüstung.
     */
    private static void phasit(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos center = helper.absolutePos(new BlockPos(4, 3, 4));
        BlockState ore = PhaseGear.PHASIT_ORE.get().defaultBlockState();
        List<ItemStack> drops = Block.getDrops(ore, level, center, null, null, new ItemStack(Items.DIAMOND_PICKAXE));
        helper.assertTrue(drops.stream().anyMatch(s -> s.is(PhaseGear.PHASIT.get())), "Das Erz gibt keinen Phasit: " + drops);
        // die Büschel leuchten nur in ihrer eigenen Leuchtebene: Der Block selbst gibt kein Licht (sonst stäche er flach und hell
        // beleuchtet aus dem Endstein heraus)
        helper.assertTrue(ore.getLightEmission() == 0, "Das Erz gibt Licht ab");
        helper.assertTrue(ore.is(BlockTags.NEEDS_DIAMOND_TOOL) && ore.is(BlockTags.MINEABLE_WITH_PICKAXE), "Das Erz braucht keine Diamantspitzhacke");
        helper.assertFalse(new ItemStack(Items.IRON_PICKAXE).isCorrectToolForDrops(ore), "Schon die Eisenspitzhacke baut es ab");
        helper.assertTrue(ore.is(ModTags.Blocks.ORES_PHASIT) && ore.is(net.neoforged.neoforge.common.Tags.Blocks.ORES), "Der Erz-Tag fehlt");
        helper.assertTrue(PhaseGear.PHASIT_BLOCK.get().defaultBlockState().getLightEmission() >= 8, "Der Block leuchtet nicht");
        helper.assertTrue(new ItemStack(PhaseGear.PHASIT.get()).is(ModTags.Items.GEMS_PHASIT), "Phasit hat keinen Edelstein-Tag");

        // auf den äußeren Inseln gibt es Adern, auf der Hauptinsel nicht
        var biomes = level.registryAccess().lookupOrThrow(Registries.BIOME);
        for (var key : List.of(Biomes.END_HIGHLANDS, Biomes.END_MIDLANDS, Biomes.END_BARRENS, Biomes.SMALL_END_ISLANDS, Biomes.THE_END)) {
            boolean found = false;
            for (var step : biomes.getOrThrow(key).value().getGenerationSettings().features()) {
                for (var placed : step) {
                    found |= placed.is(ModWorldgen.ORE_PHASIT_PLACED);
                }
            }
            helper.assertTrue(found == (key != Biomes.THE_END), (found ? "Phasit-Adern im Biom " : "Keine Phasit-Adern im Biom ") + key.identifier());
        }

        // eine Ader in einem Feld aus Endstein
        for (BlockPos p : BlockPos.betweenClosed(center.offset(-3, -2, -3), center.offset(3, 2, 3))) {
            level.setBlock(p, Blocks.END_STONE.defaultBlockState(), Block.UPDATE_CLIENTS);
        }
        var feature = level.registryAccess().lookupOrThrow(Registries.FEATURE).getOrThrow(ModWorldgen.ORE_PHASIT).value();
        var generator = level.getChunkSource().getGenerator();
        int crystals = 0;
        for (int seed = 0; seed < 40 && crystals == 0; seed++) {
            feature.place(level, generator, RandomSource.create(seed), center);
            crystals = count(level, center, PhaseGear.PHASIT_ORE.get());
        }
        for (BlockPos p : BlockPos.betweenClosed(center.offset(-3, -2, -3), center.offset(3, 2, 3))) {
            level.setBlock(p, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
        }
        helper.assertTrue(crystals > 0, "Die Ader setzt keine Kristalle in den Endstein");

        var recipes = level.getServer().getRecipeManager().recipeMap();
        for (String id : List.of("phasit_from_smelting_phasit_ore", "phasit_from_blasting_phasit_ore", "phasit_block", "phasit_from_phasit_block",
                "voidwalker_helmet", "voidwalker_chestplate", "voidwalker_leggings", "voidwalker_boots")) {
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
    // Die Rüstung der Leerwandler
    // =====================================================================================

    /**
     * Schutz wie Diamant, mit Phasit auszubessern; der Phasenschritt bringt den Träger vier bis acht Blöcke fort auf festen
     * Boden und hat eine Wartezeit; nur Nahkampfhiebe zählen; mit den Stiefeln tun Enderperlen nicht weh; mit dem ganzen
     * Satz werden die Leerwandler nicht wütend.
     */
    private static void armor(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        var set = PhaseGear.voidwalkerSet();
        double[] armor = new double[1];
        for (int i = 0; i < set.size(); i++) {
            ItemStack stack = new ItemStack(set.get(i).get());
            stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY).forEach(SLOTS[i], (attribute, modifier) -> {
                if (attribute.is(Attributes.ARMOR)) {
                    armor[0] += modifier.amount();
                }
            });
            helper.assertTrue(stack.isValidRepairItem(new ItemStack(PhaseGear.PHASIT.get())), "Nicht mit Phasit auszubessern: " + stack);
        }
        helper.assertTrue(armor[0] >= 20.0, "Schutz wie Diamant fehlt: " + armor[0]);

        // eine Plattform hoch über der Arena (andere Tests stehen am Boden) – in einer Höhe, in der kein anderer Test baut: Die
        // Plattform reicht weit über das eigene Testfeld hinaus
        BlockPos center = helper.absolutePos(new BlockPos(4, 140, 4));
        for (BlockPos p : BlockPos.betweenClosed(center.offset(-11, -1, -11), center.offset(11, -1, 11))) {
            level.setBlock(p, Blocks.STONE.defaultBlockState(), Block.UPDATE_CLIENTS);
        }
        ServerPlayer player = MageGameTests.survivalPlayer(helper, "test-voidwalker", new io.netty.channel.embedded.EmbeddedChannel[1], 0);
        Mob zombie = ModEntities.ZOMBIE.get().create(level, EntitySpawnReason.MOB_SUMMONED);
        Enderman walker = ModEntities.ENDERMAN.get().create(level, EntitySpawnReason.MOB_SUMMONED);
        try {
            player.connection.markClientLoaded();
            player.getAbilities().invulnerable = false;
            player.teleportTo(level, center.getX() + 0.5, center.getY(), center.getZ() + 0.5, java.util.Set.of(), 0.0f, 0.0f, true);
            level.getChunkSource().move(player);
            zombie.snapTo(center.getX() + 1.5, center.getY(), center.getZ() + 0.5, 0.0f, 0.0f);
            for (int pieces = 0; pieces <= 4; pieces++) {
                for (int i = 0; i < 4; i++) {
                    player.setItemSlot(SLOTS[i], i < pieces ? new ItemStack(set.get(i).get()) : ItemStack.EMPTY);
                }
                helper.assertTrue(VoidwalkerArmor.pieces(player) == pieces, "Teile zählen falsch: " + VoidwalkerArmor.pieces(player));
            }
            // nur Nahkampfhiebe
            helper.assertTrue(VoidwalkerArmor.melee(level.damageSources().mobAttack(zombie)), "Ein Hieb zählt nicht als Nahkampf");
            Arrow arrow = new Arrow(level, zombie, new ItemStack(Items.ARROW), null);
            helper.assertFalse(VoidwalkerArmor.melee(level.damageSources().arrow(arrow, zombie)), "Ein Pfeil zählt als Nahkampf");
            helper.assertFalse(VoidwalkerArmor.melee(level.damageSources().fall()), "Fallschaden zählt als Nahkampf");

            // der Phasenschritt: weg vom Angreifer, vier bis acht Blöcke, auf festen Boden
            VoidwalkerArmor.forget(player);
            helper.assertTrue(VoidwalkerArmor.ready(player), "Ohne Wartezeit nicht bereit");
            Vec3 from = player.position();
            boolean phased = VoidwalkerArmor.phase(level, player, zombie);
            double moved = Math.hypot(player.getX() - from.x, player.getZ() - from.z);
            helper.assertTrue(phased && moved >= VoidwalkerArmor.MIN_JUMP - 0.6 && moved <= VoidwalkerArmor.MAX_JUMP + 0.9,
                    "Der Phasenschritt springt nicht vier bis acht Blöcke: " + phased + ", " + moved);
            helper.assertTrue(player.distanceToSqr(zombie) > from.distanceToSqr(zombie.position()), "Der Phasenschritt führt zum Angreifer hin");
            helper.assertTrue(level.getBlockState(player.blockPosition().below()).is(Blocks.STONE), "Nach dem Phasenschritt kein Boden unter den Füßen");
            helper.assertFalse(VoidwalkerArmor.ready(player), "Gleich wieder bereit (keine Wartezeit)");
            VoidwalkerArmor.forget(player);

            // schwebende Sohlen: Enderperlen tun mit den Stiefeln nicht weh, ohne sie schon
            player.setHealth(player.getMaxHealth());
            player.setInvulnerableTime(0);
            player.hurtServer(level, level.damageSources().enderPearl(), 5.0f);
            helper.assertTrue(player.getHealth() >= player.getMaxHealth() - 0.01f, "Enderperle tut trotz der Stiefel weh: " + player.getHealth());
            player.setItemSlot(EquipmentSlot.FEET, ItemStack.EMPTY);
            player.setInvulnerableTime(0);
            player.hurtServer(level, level.damageSources().enderPearl(), 5.0f);
            helper.assertTrue(player.getHealth() < player.getMaxHealth() - 0.01f, "Enderperle tut ohne Stiefel nicht weh");

            // einer von ihnen: nur mit dem ganzen Satz bleiben die Leerwandler ruhig
            helper.assertFalse(CommonHooks.shouldSuppressEnderManAnger(walker, player), "Schon drei Teile beruhigen die Leerwandler");
            player.setItemSlot(EquipmentSlot.FEET, new ItemStack(PhaseGear.VOIDWALKER_BOOTS.get()));
            helper.assertTrue(CommonHooks.shouldSuppressEnderManAnger(walker, player), "Der ganze Satz beruhigt die Leerwandler nicht");
            helper.succeed();
        } finally {
            zombie.discard();
            walker.discard();
            level.getServer().getPlayerList().remove(player);
            for (BlockPos p : BlockPos.betweenClosed(center.offset(-11, -1, -11), center.offset(11, -1, 11))) {
                level.setBlock(p, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
            }
        }
    }
}
