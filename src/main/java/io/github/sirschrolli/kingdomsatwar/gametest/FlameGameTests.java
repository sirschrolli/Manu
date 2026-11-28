package io.github.sirschrolli.kingdomsatwar.gametest;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.item.nether.EmberwalkerArmor;
import io.github.sirschrolli.kingdomsatwar.registry.FlameGear;
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
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * Tests der Erweiterung des Nethers (seit 1.1.6): Feueropal (Erz, Drops, Adern im Nether, Rezepte) und die Rüstung der
 * Glutwandler (Schutz, weniger Feuerschaden je Teil, Glutwandeln über Lava).
 */
final class FlameGameTests {
    private static final Identifier ARENA = KingdomsAtWar.id("test_arena");
    private static final EquipmentSlot[] SLOTS = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> OPAL =
            ModGameTests.TEST_FUNCTIONS.register("flame_opal", () -> FlameGameTests::opal);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> ARMOR =
            ModGameTests.TEST_FUNCTIONS.register("flame_armor", () -> FlameGameTests::armor);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> CRUST =
            ModGameTests.TEST_FUNCTIONS.register("flame_crust", () -> FlameGameTests::crust);

    private FlameGameTests() {}

    /** Lädt die Klasse (meldet die Testfunktionen an). */
    static void init() {}

    static void register(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment) {
        for (var test : List.of(OPAL, ARMOR, CRUST)) {
            event.registerTest(test.getId(), new FunctionGameTestInstance(test.key(),
                    new TestData<>(environment, Level.OVERWORLD, ARENA, 900, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
        }
    }

    // =====================================================================================
    // Feueropal
    // =====================================================================================

    /**
     * Beide Erze geben Feueropal (ab einer Diamantspitzhacke) und schimmern; Feueropal verbrennt nicht; in jedem Biom des Nethers
     * gibt es Adern, und eine Ader setzt in Netherrack und Schwarzstein Knollen; Rezepte für Schmelzen, Block und Rüstung.
     */
    private static void opal(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos center = helper.absolutePos(new BlockPos(4, 3, 4));
        for (BlockState ore : List.of(FlameGear.FIRE_OPAL_ORE.get().defaultBlockState(), FlameGear.BLACKSTONE_FIRE_OPAL_ORE.get().defaultBlockState())) {
            List<ItemStack> drops = Block.getDrops(ore, level, center, null, null, new ItemStack(Items.DIAMOND_PICKAXE));
            helper.assertTrue(drops.stream().anyMatch(s -> s.is(FlameGear.FIRE_OPAL.get())), "Das Erz gibt keinen Feueropal: " + drops);
            helper.assertTrue(ore.getLightEmission() > 0, "Das Erz schimmert nicht: " + ore);
            helper.assertTrue(ore.is(BlockTags.NEEDS_DIAMOND_TOOL) && ore.is(BlockTags.MINEABLE_WITH_PICKAXE), "Das Erz braucht keine Diamantspitzhacke");
            helper.assertFalse(new ItemStack(Items.IRON_PICKAXE).isCorrectToolForDrops(ore), "Schon die Eisenspitzhacke baut es ab");
            helper.assertTrue(ore.is(ModTags.Blocks.ORES_FIRE_OPAL), "Der Erz-Tag fehlt: " + ore);
        }
        helper.assertTrue(FlameGear.FIRE_OPAL_BLOCK.get().defaultBlockState().getLightEmission() >= 8, "Der Block leuchtet nicht");
        ItemStack opal = new ItemStack(FlameGear.FIRE_OPAL.get());
        helper.assertTrue(opal.has(DataComponents.DAMAGE_RESISTANT) && opal.is(ModTags.Items.GEMS_FIRE_OPAL), "Feueropal verbrennt oder hat keinen Tag");

        // in jedem Biom des Nethers gibt es Adern
        var biomes = level.registryAccess().lookupOrThrow(Registries.BIOME);
        for (var key : List.of(Biomes.NETHER_WASTES, Biomes.CRIMSON_FOREST, Biomes.WARPED_FOREST, Biomes.SOUL_SAND_VALLEY, Biomes.BASALT_DELTAS)) {
            boolean found = false;
            for (var step : biomes.getOrThrow(key).value().getGenerationSettings().features()) {
                for (var placed : step) {
                    found |= placed.is(ModWorldgen.ORE_FIRE_OPAL_PLACED);
                }
            }
            helper.assertTrue(found, "Keine Feueropal-Adern im Biom " + key.identifier());
        }

        // eine Ader in einem Feld aus Netherrack (links) und Schwarzstein (rechts)
        for (BlockPos p : BlockPos.betweenClosed(center.offset(-3, -2, -3), center.offset(3, 2, 3))) {
            level.setBlock(p, (p.getX() < center.getX() ? Blocks.NETHERRACK : Blocks.BLACKSTONE).defaultBlockState(), Block.UPDATE_CLIENTS);
        }
        var feature = level.registryAccess().lookupOrThrow(Registries.FEATURE).getOrThrow(ModWorldgen.ORE_FIRE_OPAL).value();
        var generator = level.getChunkSource().getGenerator();
        int netherrack = 0;
        int blackstone = 0;
        for (int seed = 0; seed < 40 && (netherrack == 0 || blackstone == 0); seed++) {
            feature.place(level, generator, RandomSource.create(seed), center.offset(seed % 2 == 0 ? -1 : 1, 0, 0));
            netherrack = count(level, center, FlameGear.FIRE_OPAL_ORE.get());
            blackstone = count(level, center, FlameGear.BLACKSTONE_FIRE_OPAL_ORE.get());
        }
        for (BlockPos p : BlockPos.betweenClosed(center.offset(-3, -2, -3), center.offset(3, 2, 3))) {
            level.setBlock(p, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
        }
        helper.assertTrue(netherrack > 0 && blackstone > 0, "Die Ader setzt keine Knollen: " + netherrack + " im Netherrack, " + blackstone
                + " im Schwarzstein");
        helper.assertTrue(netherrack + blackstone <= 4 * ModWorldgen.FIRE_OPAL_SIZE * 40, "Zu viele Knollen");

        var recipes = level.getServer().getRecipeManager().recipeMap();
        for (String id : List.of("fire_opal_from_smelting_fire_opal_ore", "fire_opal_from_blasting_blackstone_fire_opal_ore", "fire_opal_block",
                "fire_opal_from_fire_opal_block", "emberwalker_helmet", "emberwalker_chestplate", "emberwalker_leggings", "emberwalker_boots")) {
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
    // Die Rüstung der Glutwandler
    // =====================================================================================

    /** Schutz wie Diamant und etwas härter, verbrennt nicht, mit Feueropal auszubessern; je Teil ein Fünftel weniger Feuerschaden. */
    private static void armor(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        var set = FlameGear.emberwalkerSet();
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
            helper.assertTrue(stack.isValidRepairItem(new ItemStack(FlameGear.FIRE_OPAL.get())), "Nicht mit Feueropal auszubessern: " + stack);
        }
        helper.assertTrue(armor[0] >= 20.0 && toughness[0] >= 10.0, "Schutz wie Diamant fehlt: " + armor[0] + " / " + toughness[0]);

        ServerPlayer player = MageGameTests.survivalPlayer(helper, "test-emberwalker", new io.netty.channel.embedded.EmbeddedChannel[1], 0);
        try {
            player.connection.markClientLoaded();
            player.getAbilities().invulnerable = false;
            player.setData(io.github.sirschrolli.kingdomsatwar.registry.ModAttachments.HERO_IMMORTAL, -1L);
            // Brennen (umgeht Rüstungspunkte): ohne Rüstung zehn, mit jedem Teil zwei weniger
            for (int pieces = 0; pieces <= 4; pieces++) {
                for (int i = 0; i < 4; i++) {
                    player.setItemSlot(SLOTS[i], i < pieces ? new ItemStack(set.get(i).get()) : ItemStack.EMPTY);
                }
                helper.assertTrue(EmberwalkerArmor.pieces(player) == pieces, "Teile zählen falsch: " + EmberwalkerArmor.pieces(player));
                player.setHealth(player.getMaxHealth());
                player.damageCooldownTime = 0;
                player.setInvulnerableTime(0);
                player.hurtServer(level, level.damageSources().onFire(), 10.0f);
                float lost = player.getMaxHealth() - player.getHealth();
                float expected = 10.0f * EmberwalkerArmor.fireFactor(pieces);
                helper.assertTrue(Math.abs(lost - expected) < 0.01f, pieces + " Teile: " + lost + " statt " + expected + " Schaden durch Feuer");
            }
            // anderer Schaden wird nicht gemindert (Hunger umgeht die Rüstung ebenso)
            player.setHealth(player.getMaxHealth());
            player.damageCooldownTime = 0;
            player.setInvulnerableTime(0);
            player.hurtServer(level, level.damageSources().starve(), 4.0f);
            helper.assertTrue(Math.abs(player.getMaxHealth() - player.getHealth() - 4.0f) < 0.01f, "Auch anderer Schaden wird gemindert");
            helper.succeed();
        } finally {
            level.getServer().getPlayerList().remove(player);
        }
    }

    /**
     * Glutwandeln: Mit den Stiefeln erstarrt die Lava unter einem zur Kruste (nur stille Lava, nur am Boden), die Kruste hält,
     * solange man darauf steht, und schmilzt wieder, wenn man fort ist; zerschlagen gibt sie die Lava frei. Ohne Stiefel
     * erstarrt nichts.
     */
    private static void crust(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos o = helper.absolutePos(new BlockPos(0, 1, 0));
        // ein Lavabecken (x 3–7, z 2–7) in der Arena (9 × 9), ringsum ein Ufer aus Netherrack
        for (BlockPos p : BlockPos.betweenClosed(o.offset(0, -1, 0), o.offset(8, 0, 8))) {
            level.setBlock(p, Blocks.NETHERRACK.defaultBlockState(), Block.UPDATE_CLIENTS);
        }
        for (BlockPos p : BlockPos.betweenClosed(o.offset(3, 0, 2), o.offset(7, 0, 7))) {
            level.setBlock(p, Blocks.LAVA.defaultBlockState(), Block.UPDATE_CLIENTS);
        }
        ServerPlayer player = MageGameTests.survivalPlayer(helper, "test-emberwalker-boots", new io.netty.channel.embedded.EmbeddedChannel[1], 0);
        try {
            player.connection.markClientLoaded();
            BlockPos shore = o.offset(1, 1, 4);
            player.teleportTo(level, shore.getX() + 0.5, shore.getY(), shore.getZ() + 0.5, java.util.Set.of(), -90.0f, 0.0f, true);
            level.getChunkSource().move(player);
            player.setOnGround(true);
            helper.assertTrue(EmberwalkerArmor.walk(level, player) == 0, "Ohne Stiefel erstarrt Lava");
            helper.assertTrue(level.getBlockState(o.offset(3, 0, 4)).is(Blocks.LAVA), "Ohne Stiefel erstarrt Lava: " + level.getBlockState(o.offset(3, 0, 4)));
            player.setItemSlot(EquipmentSlot.FEET, new ItemStack(FlameGear.EMBERWALKER_BOOTS.get()));
            int made = EmberwalkerArmor.walk(level, player);
            helper.assertTrue(made >= 6, "Unter den Stiefeln erstarrt kaum Lava: " + made);
            helper.assertTrue(level.getBlockState(o.offset(3, 0, 4)).is(FlameGear.LAVA_CRUST.get()), "Vor dem Ufer keine Kruste");
            helper.assertTrue(level.getBlockState(o.offset(7, 0, 4)).is(Blocks.LAVA), "Zu weit draußen schon Kruste");
            // im Sprung erstarrt nichts
            player.setOnGround(false);
            helper.assertTrue(EmberwalkerArmor.walk(level, player) == 0, "Im Sprung erstarrt Lava");
            // auf die Kruste: weiter draußen erstarrt mehr
            BlockPos out = o.offset(4, 1, 4);
            player.teleportTo(level, out.getX() + 0.5, out.getY(), out.getZ() + 0.5, java.util.Set.of(), -90.0f, 0.0f, true);
            player.setOnGround(true);
            int more = EmberwalkerArmor.walk(level, player);
            helper.assertTrue(level.getBlockState(o.offset(7, 0, 4)).is(FlameGear.LAVA_CRUST.get()), "Auf der Kruste erstarrt keine weitere Lava: "
                    + more + " neu, dort " + level.getBlockState(o.offset(7, 0, 4)));
            // zerschlagen: die Lava ist wieder da
            BlockPos broken = o.offset(3, 0, 2);
            helper.assertTrue(level.getBlockState(broken).is(FlameGear.LAVA_CRUST.get()), "Am Rand keine Kruste: " + level.getBlockState(broken));
            level.getBlockState(broken).getBlock().playerDestroy(level, player, broken, level.getBlockState(broken), null, ItemStack.EMPTY);
            helper.assertTrue(level.getBlockState(broken).is(Blocks.LAVA), "Zerschlagene Kruste lässt keine Lava zurück");
        } catch (RuntimeException e) {
            level.getServer().getPlayerList().remove(player);
            throw e;
        }
        BlockPos under = o.offset(4, 0, 4);
        // solange er darauf steht, hält die Kruste
        helper.runAfterDelay(160, () -> {
            try {
                helper.assertTrue(level.getBlockState(under).is(FlameGear.LAVA_CRUST.get()), "Die Kruste unter seinen Füßen schmilzt: "
                        + level.getBlockState(under));
            } catch (RuntimeException e) {
                level.getServer().getPlayerList().remove(player);
                throw e;
            }
            // er zieht die Stiefel aus und geht ans Ufer: bald ist alles wieder Lava
            player.setItemSlot(EquipmentSlot.FEET, ItemStack.EMPTY);
            player.teleportTo(level, o.getX() + 0.5, o.getY() + 1, o.getZ() + 8.5, java.util.Set.of(), 0.0f, 0.0f, true);
            helper.runAfterDelay(260, () -> {
                level.getServer().getPlayerList().remove(player);
                int crusts = 0;
                for (BlockPos p : BlockPos.betweenClosed(o.offset(3, 0, 2), o.offset(7, 0, 7))) {
                    crusts += level.getBlockState(p).is(FlameGear.LAVA_CRUST.get()) ? 1 : 0;
                    helper.assertTrue(level.getBlockState(p).is(FlameGear.LAVA_CRUST.get()) || level.getBlockState(p).is(Blocks.LAVA),
                            "Wo Lava war, ist jetzt " + level.getBlockState(p));
                }
                helper.assertTrue(crusts == 0, "Die Kruste schmilzt nicht wieder: " + crusts + " Blöcke");
                for (BlockPos p : BlockPos.betweenClosed(o.offset(0, -1, 0), o.offset(8, 0, 8))) {
                    level.setBlock(p, (p.getY() < o.getY() ? Blocks.STONE : Blocks.AIR).defaultBlockState(), Block.UPDATE_CLIENTS);
                }
                helper.succeed();
            });
        });
    }
}
