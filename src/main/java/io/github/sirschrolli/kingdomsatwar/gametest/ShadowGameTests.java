package io.github.sirschrolli.kingdomsatwar.gametest;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.item.MoonsilverBladeItem;
import io.github.sirschrolli.kingdomsatwar.registry.ModBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.ShadowBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.ShadowItems;
import io.github.sirschrolli.kingdomsatwar.shadow.CounterGate;
import io.github.sirschrolli.kingdomsatwar.shadow.ShadowBiomes;
import io.github.sirschrolli.kingdomsatwar.shadow.ShadowDimension;
import io.github.sirschrolli.kingdomsatwar.shadow.ShadowEquipment;
import io.github.sirschrolli.kingdomsatwar.shadow.ShadowTerrain;
import io.github.sirschrolli.kingdomsatwar.world.ShadowGate;
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
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.portal.TeleportTransition;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * Tests der Schattenwelt: Dimension und Gegentor, Erze, Setzlinge, Ausrüstung und Rezepte.
 */
final class ShadowGameTests {
    private static final Identifier PLATFORM = KingdomsAtWar.id("test_platform");
    private static final Identifier ARENA = KingdomsAtWar.id("test_arena");
    private static final BlockPos TEST_POS = new BlockPos(2, 1, 2);

    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> SHADOW_WORLD =
            ModGameTests.TEST_FUNCTIONS.register("shadow_world", () -> ShadowGameTests::shadowWorld);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> SHADOW_ORES =
            ModGameTests.TEST_FUNCTIONS.register("shadow_ores", () -> ShadowGameTests::shadowOres);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> SHADOW_SAPLING =
            ModGameTests.TEST_FUNCTIONS.register("shadow_sapling", () -> ShadowGameTests::shadowSapling);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> SHADOW_GEAR =
            ModGameTests.TEST_FUNCTIONS.register("shadow_gear", () -> ShadowGameTests::shadowGear);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> SHADOW_RECIPES =
            ModGameTests.TEST_FUNCTIONS.register("shadow_recipes", () -> ShadowGameTests::shadowRecipes);

    private ShadowGameTests() {}

    /** Lädt die Klasse (meldet die Testfunktionen an). */
    static void init() {}

    static void register(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment) {
        for (var test : List.of(SHADOW_ORES, SHADOW_RECIPES)) {
            event.registerTest(test.getId(), new FunctionGameTestInstance(test.key(), new TestData<>(environment, PLATFORM, 100, 0, true)));
        }
        for (var test : List.of(SHADOW_WORLD, SHADOW_SAPLING, SHADOW_GEAR)) {
            event.registerTest(test.getId(), new FunctionGameTestInstance(test.key(),
                    new TestData<>(environment, Level.OVERWORLD, ARENA, 400, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
        }
    }

    /**
     * Die Schattenwelt: Dimensionstyp und Gelände sind registriert; das Gelände hat überall festen Grund, genug Land und an
     * der Oberfläche die Oberflächen-Biome (nicht die Tiefen). Gibt es die Welt (nicht auf dem Test-Server, der nur die
     * Grunddimensionen lädt), entsteht das Gegentor und führt hin und zurück.
     */
    private static void shadowWorld(GameTestHelper helper) {
        ServerLevel overworld = helper.getLevel();
        var server = overworld.getServer();
        var registries = server.registryAccess();
        helper.assertTrue(registries.lookupOrThrow(Registries.DIMENSION_TYPE).get(ShadowDimension.TYPE).isPresent(),
                "Der Dimensionstyp der Schattenwelt fehlt");
        Holder<NoiseGeneratorSettings> settings = registries.lookupOrThrow(Registries.NOISE_SETTINGS).getOrThrow(ShadowTerrain.SETTINGS);
        ServerLevel shadow = server.getLevel(ShadowGate.SHADOW_WORLD);
        ChunkGenerator generator;
        RandomState random;
        LevelHeightAccessor height;
        if (shadow != null) {
            generator = shadow.getChunkSource().getGenerator();
            random = shadow.getChunkSource().randomState();
            height = shadow;
        } else {
            generator = new NoiseBasedChunkGenerator(MultiNoiseBiomeSource.createFromList(
                    ShadowBiomes.parameters(registries.lookupOrThrow(Registries.BIOME))), settings);
            random = RandomState.create(registries.lookupOrThrow(Registries.NOISE), 20_261_026L, settings.value());
            height = LevelHeightAccessor.create(-64, 384);
        }
        var possible = generator.getBiomeSource().possibleBiomes();
        for (ResourceKey<Biome> biome : ShadowBiomes.ALL) {
            helper.assertTrue(possible.stream().anyMatch(holder -> holder.is(biome)), "Biom fehlt in der Schattenwelt: " + biome.identifier());
        }
        // Gelände: 36 Säulen in einem weiten Raster
        BiomeResolver biomes = generator.getBiomeSource().createUncachedResolver(random);
        int land = 0;
        int depthsAtSurface = 0;
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 6; j++) {
                int x = (i - 3) * 700 + 123;
                int z = (j - 3) * 700 - 77;
                int ground = generator.getBaseHeight(x, z, Heightmap.Types.OCEAN_FLOOR_WG, height, random);
                helper.assertTrue(ground > -40, "Kein fester Grund bei " + x + " " + z + " (Höhe " + ground + ")");
                if (ground > ShadowTerrain.SEA_LEVEL) {
                    land++;
                }
                if (biomes.getNoiseBiome(x >> 2, (ground + 2) >> 2, z >> 2).is(ShadowBiomes.SHADOW_DEPTHS)) {
                    depthsAtSurface++;
                }
            }
        }
        helper.assertTrue(land >= 12, "Zu wenig Land in der Schattenwelt: " + land + " von 36");
        helper.assertTrue(depthsAtSurface <= 4, "Die Tiefen reichen bis an die Oberfläche: " + depthsAtSurface + " von 36");
        if (shadow != null) {
            // Gegentor: wird gebaut und führt hin und zurück
            BlockPos gate = CounterGate.ensure(shadow, BlockPos.ZERO);
            helper.assertTrue(shadow.getBlockState(gate).is(ModBlocks.SHADOW_PORTAL.get()), "Im Gegentor wogt kein Schattentor: " + gate);
            Player player = helper.makeMockPlayer(GameType.SURVIVAL);
            TeleportTransition there = ShadowGate.destination(overworld, player, BlockPos.ZERO);
            helper.assertTrue(there != null && there.newLevel() == shadow, "Das Schattentor führt nicht in die Schattenwelt");
            TeleportTransition back = ShadowGate.destination(shadow, player, gate);
            helper.assertTrue(back != null && back.newLevel() == server.overworld(), "Das Gegentor führt nicht zurück");
        } else {
            Player player = helper.makeMockPlayer(GameType.SURVIVAL);
            helper.assertTrue(ShadowGate.destination(overworld, player, BlockPos.ZERO) == null,
                    "Ohne Schattenwelt darf das Tor nirgendwohin führen");
        }
        helper.succeed();
    }

    /** Die Erze der Schattenwelt lassen ihre Rohstoffe fallen (Abyssit sich selbst), Drusen ihre Splitter. */
    private static void shadowOres(GameTestHelper helper) {
        assertDrops(helper, ShadowBlocks.MOONSILVER_ORE, ShadowItems.RAW_MOONSILVER.get());
        assertDrops(helper, ShadowBlocks.DEEP_MOONSILVER_ORE, ShadowItems.RAW_MOONSILVER.get());
        assertDrops(helper, ShadowBlocks.NIGHTSTEEL_ORE, ShadowItems.RAW_NIGHTSTEEL.get());
        assertDrops(helper, ShadowBlocks.DEEP_NIGHTSTEEL_ORE, ShadowItems.RAW_NIGHTSTEEL.get());
        assertDrops(helper, ShadowBlocks.SOUL_CRYSTAL_ORE, ShadowItems.SOUL_CRYSTAL.get());
        assertDrops(helper, ShadowBlocks.ABYSSITE_ORE, ShadowBlocks.ABYSSITE_ORE.get().asItem());
        assertDrops(helper, ShadowBlocks.NIGHT_CRYSTAL_CLUSTER, ShadowItems.NIGHT_CRYSTAL_SHARD.get());
        assertDrops(helper, ShadowBlocks.SHADOW_GRASS, ShadowBlocks.SHADOW_SOIL.get().asItem());
        // Abyssit braucht mindestens eine Diamantspitzhacke
        BlockState abyssite = ShadowBlocks.ABYSSITE_ORE.get().defaultBlockState();
        helper.assertFalse(new ItemStack(Items.IRON_PICKAXE).isCorrectToolForDrops(abyssite), "Abyssit lässt sich mit Eisen abbauen");
        helper.assertTrue(new ItemStack(Items.DIAMOND_PICKAXE).isCorrectToolForDrops(abyssite), "Abyssit lässt sich nicht mit Diamant abbauen");
        helper.succeed();
    }

    /** Ein Schattensetzling auf Schattengras wächst zu einem Schattenbaum. */
    private static void shadowSapling(GameTestHelper helper) {
        for (int x = 0; x < 9; x++) {
            for (int z = 0; z < 9; z++) {
                helper.setBlock(x, 0, z, ShadowBlocks.SHADOW_GRASS.get());
            }
        }
        BlockPos sapling = new BlockPos(4, 1, 4);
        helper.setBlock(sapling, ShadowBlocks.SHADOW_SAPLING.get());
        ServerLevel level = helper.getLevel();
        BlockPos abs = helper.absolutePos(sapling);
        for (int i = 0; i < 8 && level.getBlockState(abs).is(ShadowBlocks.SHADOW_SAPLING.get()); i++) {
            BlockState state = level.getBlockState(abs);
            ((SaplingBlock) state.getBlock()).advanceTree(level, abs, state, level.getRandom());
        }
        helper.assertBlockPresent(ShadowBlocks.SHADOW_LOG.get(), sapling);
        helper.succeed();
    }

    /** Mondsilberklinge, Nachtstahlhelm, Abyssit-Stiefel und die Schutzwerte der Rüstungen. */
    private static void shadowGear(GameTestHelper helper) {
        for (int x = 0; x < 9; x++) {
            for (int z = 0; z < 9; z++) {
                helper.setBlock(x, 0, z, net.minecraft.world.level.block.Blocks.STONE);
            }
        }
        // Mondsilberklinge: hart gegen Untote, normal gegen Tiere
        var zombie = helper.spawnWithNoFreeWill(EntityTypes.ZOMBIE, new BlockPos(2, 1, 2));
        var pig = helper.spawnWithNoFreeWill(EntityTypes.PIG, new BlockPos(6, 1, 6));
        MoonsilverBladeItem blade = ShadowItems.MOONSILVER_BLADE.get();
        DamageSource source = helper.getLevel().damageSources().generic();
        helper.assertTrue(blade.getAttackDamageBonus(zombie, 5.0f, source) >= MoonsilverBladeItem.BONUS_DAMAGE,
                "Die Mondsilberklinge trifft Untote nicht härter");
        helper.assertTrue(blade.getAttackDamageBonus(pig, 5.0f, source) == 0.0f, "Die Mondsilberklinge trifft auch Tiere härter");
        // Nachtstahlhelm: Sicht in der Schattenwelt
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        helper.assertFalse(ShadowEquipment.hasShadowSight(player), "Sicht ohne Helm");
        player.setItemSlot(EquipmentSlot.HEAD, new ItemStack(ShadowItems.NIGHTSTEEL_HELMET.get()));
        helper.assertTrue(ShadowEquipment.hasShadowSight(player), "Der Nachtstahlhelm schenkt keine Sicht");
        // Abyssit-Stiefel: kein Fallschaden; Rüstungswerte
        var wearer = helper.spawnWithNoFreeWill(EntityTypes.ZOMBIE, new BlockPos(4, 1, 6));
        equip(wearer, ShadowItems.ABYSSITE_HELMET.get(), ShadowItems.ABYSSITE_CHESTPLATE.get(), ShadowItems.ABYSSITE_LEGGINGS.get(),
                ShadowItems.ABYSSITE_BOOTS.get());
        var night = helper.spawnWithNoFreeWill(EntityTypes.ZOMBIE, new BlockPos(6, 1, 2));
        equip(night, ShadowItems.NIGHTSTEEL_HELMET.get(), ShadowItems.NIGHTSTEEL_CHESTPLATE.get(), ShadowItems.NIGHTSTEEL_LEGGINGS.get(),
                ShadowItems.NIGHTSTEEL_BOOTS.get());
        helper.succeedWhen(() -> {
            helper.assertTrue(wearer.getAttributeValue(Attributes.FALL_DAMAGE_MULTIPLIER) <= 0.0001,
                    "Die Abyssit-Stiefel fangen Stürze nicht ab: " + wearer.getAttributeValue(Attributes.FALL_DAMAGE_MULTIPLIER));
            helper.assertTrue(night.getArmorValue() >= 20, "Nachtstahl schützt zu wenig: " + night.getArmorValue());
            helper.assertTrue(wearer.getAttributeValue(Attributes.ARMOR_TOUGHNESS) > night.getAttributeValue(Attributes.ARMOR_TOUGHNESS),
                    "Abyssit ist nicht zäher als Nachtstahl");
        });
    }

    /** Die wichtigsten Rezepte der Schattenwelt sind da. */
    private static void shadowRecipes(GameTestHelper helper) {
        var recipes = helper.getLevel().getServer().getRecipeManager().recipeMap();
        for (String name : List.of("shadow_planks", "shadow_wood", "polished_shadow_stone", "shadow_stone_bricks", "chiseled_shadow_stone",
                "shadow_stone_stairs", "shadow_plank_slab", "ash_snow", "moonsilver_ingot_from_smelting_raw_moonsilver",
                "nightsteel_ingot_from_blasting_raw_nightsteel", "abyssite_shard_from_smelting_abyssite_ore", "abyssite_ingot",
                "nightsteel_sword", "nightsteel_chestplate", "abyssite_sword_smithing", "abyssite_boots_smithing",
                "abyssite_upgrade_smithing_template", "moon_lantern", "moonsilver_blade", "night_crystal", "moonsilver_block",
                "moonsilver_nugget_from_moonsilver_ingot")) {
            ResourceKey<Recipe<?>> key = ResourceKey.create(Registries.RECIPE, KingdomsAtWar.id(name));
            helper.assertTrue(recipes.byKey(key) != null, "Rezept fehlt: " + key.identifier());
        }
        helper.succeed();
    }

    // =====================================================================================
    // Hilfen
    // =====================================================================================

    private static void equip(net.minecraft.world.entity.Mob mob, Item helmet, Item chest, Item legs, Item boots) {
        mob.setItemSlot(EquipmentSlot.HEAD, new ItemStack(helmet));
        mob.setItemSlot(EquipmentSlot.CHEST, new ItemStack(chest));
        mob.setItemSlot(EquipmentSlot.LEGS, new ItemStack(legs));
        mob.setItemSlot(EquipmentSlot.FEET, new ItemStack(boots));
    }

    private static void assertDrops(GameTestHelper helper, DeferredBlock<? extends Block> block, Item expected) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(TEST_POS);
        BlockState state = block.get().defaultBlockState();
        List<ItemStack> drops = Block.getDrops(state, level, pos, null, null, new ItemStack(Items.NETHERITE_PICKAXE));
        helper.assertTrue(drops.stream().anyMatch(stack -> stack.is(expected)),
                block.getId() + " lässt nicht " + expected + " fallen, sondern " + drops);
    }
}
