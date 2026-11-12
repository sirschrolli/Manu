package io.github.sirschrolli.kingdomsatwar.gametest;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.block.sea.TideGateBlock;
import io.github.sirschrolli.kingdomsatwar.fishing.Habitat;
import io.github.sirschrolli.kingdomsatwar.fishing.Medium;
import io.github.sirschrolli.kingdomsatwar.registry.SeaBlocks;
import io.github.sirschrolli.kingdomsatwar.urmeer.TideGate;
import io.github.sirschrolli.kingdomsatwar.urmeer.TideGateState;
import io.github.sirschrolli.kingdomsatwar.urmeer.UrmeerBiomes;
import io.github.sirschrolli.kingdomsatwar.urmeer.UrmeerDimension;
import io.github.sirschrolli.kingdomsatwar.urmeer.UrmeerTerrain;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.portal.TeleportTransition;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * Tests des Urmeers (0.89): die Welt (Biome, Meer ohne Küsten mit seltenen Inseln, Gezeitenstein, Fische) und die
 * Gezeitentore (Rahmen aus Gezeitensteinziegeln, mit dem Herz des Meeres erweckt; die Reise hin und zurück).
 */
final class UrmeerGameTests {
    private static final Identifier ARENA = KingdomsAtWar.id("test_arena");

    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> URMEER_WORLD =
            ModGameTests.TEST_FUNCTIONS.register("urmeer_world", () -> UrmeerGameTests::world);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> URMEER_TIDE_GATE =
            ModGameTests.TEST_FUNCTIONS.register("urmeer_tide_gate", () -> UrmeerGameTests::tideGate);

    private UrmeerGameTests() {}

    /** Lädt die Klasse (meldet die Testfunktionen an). */
    static void init() {}

    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> URMEER_AQUAMARINE =
            ModGameTests.TEST_FUNCTIONS.register("urmeer_aquamarine", () -> UrmeerGameTests::aquamarine);

    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> URMEER_NO_LAVA =
            ModGameTests.TEST_FUNCTIONS.register("urmeer_no_lava", () -> UrmeerGameTests::noLava);

    static void register(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment) {
        for (var test : java.util.List.of(URMEER_WORLD, URMEER_TIDE_GATE, URMEER_AQUAMARINE, URMEER_NO_LAVA)) {
            event.registerTest(test.getId(), new FunctionGameTestInstance(test.key(),
                    new TestData<>(environment, Level.OVERWORLD, ARENA, 400, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
        }
    }

    /**
     * Keine Lava im Urmeer (seit 0.99): Dort erlischt ein ausgegossener Eimer Lava (der Eimer bleibt voll), anderswo fließt sie
     * wie gewohnt. Gibt es das Urmeer auf dem Testserver nicht, wird nur die Regel und das Ausgießen in der Oberwelt geprüft.
     */
    private static void noLava(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        helper.assertTrue(io.github.sirschrolli.kingdomsatwar.urmeer.UrmeerLava.forbidden(UrmeerDimension.LEVEL), "Lava ist im Urmeer erlaubt");
        helper.assertFalse(io.github.sirschrolli.kingdomsatwar.urmeer.UrmeerLava.forbidden(Level.OVERWORLD), "Lava ist in der Oberwelt verboten");
        helper.assertTrue(io.github.sirschrolli.kingdomsatwar.urmeer.UrmeerLava.holdsLava(new net.minecraft.world.item.ItemStack(
                net.minecraft.world.item.Items.LAVA_BUCKET)), "Ein Lavaeimer enthält keine Lava");
        helper.assertFalse(io.github.sirschrolli.kingdomsatwar.urmeer.UrmeerLava.holdsLava(new net.minecraft.world.item.ItemStack(
                net.minecraft.world.item.Items.WATER_BUCKET)), "Ein Wassereimer enthält Lava");
        var bucket = (net.minecraft.world.item.BucketItem) net.minecraft.world.item.Items.LAVA_BUCKET;
        BlockPos here = helper.absolutePos(new BlockPos(4, 2, 4));
        helper.assertTrue(bucket.emptyContents(null, level, here, null, new net.minecraft.world.item.ItemStack(bucket)),
                "In der Oberwelt lässt sich keine Lava mehr ausgießen");
        helper.assertTrue(level.getFluidState(here).is(net.minecraft.tags.FluidTags.LAVA), "Keine Lava in der Oberwelt");
        level.setBlock(here, Blocks.AIR.defaultBlockState(), net.minecraft.world.level.block.Block.UPDATE_ALL);
        ServerLevel sea = level.getServer().getLevel(UrmeerDimension.LEVEL);
        if (sea != null) {
            BlockPos up = new BlockPos(0, UrmeerTerrain.SEA_LEVEL + 40, 0);
            sea.getChunk(up);
            helper.assertFalse(bucket.emptyContents(null, sea, up, null, new net.minecraft.world.item.ItemStack(bucket)),
                    "Im Urmeer lässt sich Lava ausgießen");
            helper.assertFalse(sea.getFluidState(up).is(net.minecraft.tags.FluidTags.LAVA), "Lava im Urmeer");
        }
        helper.succeed();
    }

    /**
     * Die Welt: alle vier Biome angemeldet und im Erzeuger; das Gelände ist fast überall Meer (gut vierzig Blöcke tief), mit
     * wenigen Inseln; unter dem Grund liegt Gezeitenstein, bis zum Meeresspiegel steht Wasser; beim Angeln beißen Salzwasserfische.
     */
    private static void world(GameTestHelper helper) {
        var server = helper.getLevel().getServer();
        var biomes = server.registryAccess().lookupOrThrow(Registries.BIOME);
        for (ResourceKey<Biome> biome : UrmeerBiomes.ALL) {
            helper.assertTrue(biomes.get(biome).isPresent(), "Biom fehlt: " + biome.identifier());
        }
        // der Erzeuger des Urmeers – aus der Welt, oder (auf dem Testserver gibt es nur die drei Welten von Minecraft) selbst gebaut
        ServerLevel sea = server.getLevel(UrmeerDimension.LEVEL);
        var settings = server.registryAccess().lookupOrThrow(Registries.NOISE_SETTINGS).getOrThrow(UrmeerTerrain.SETTINGS);
        net.minecraft.world.level.chunk.ChunkGenerator generator = sea != null ? sea.getChunkSource().getGenerator()
                : new net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator(
                        net.minecraft.world.level.biome.MultiNoiseBiomeSource.createFromList(UrmeerBiomes.parameters(biomes)), settings);
        net.minecraft.world.level.levelgen.RandomState random = sea != null ? sea.getChunkSource().randomState()
                : net.minecraft.world.level.levelgen.RandomState.create(server.registryAccess().lookupOrThrow(Registries.NOISE), 89_089L,
                        settings.value());
        net.minecraft.world.level.LevelHeightAccessor heights = sea != null ? sea
                : net.minecraft.world.level.LevelHeightAccessor.create(UrmeerTerrain.MIN_Y, UrmeerTerrain.HEIGHT);
        var possible = generator.getBiomeSource().possibleBiomes();
        for (ResourceKey<Biome> biome : UrmeerBiomes.ALL) {
            helper.assertTrue(possible.stream().anyMatch(h -> h.is(biome)), "Biom kommt im Urmeer nicht vor: " + biome.identifier());
        }
        helper.assertTrue(generator.getSeaLevel() == UrmeerTerrain.SEA_LEVEL, "Falscher Meeresspiegel: " + generator.getSeaLevel());
        int samples = 0;
        int land = 0;
        int deep = 0;
        int columnsChecked = 0;
        int[] found = new int[UrmeerBiomes.ALL.size()];
        var resolver = generator.getBiomeSource().createUncachedResolver(random);
        for (int gx = -20; gx < 20; gx++) {
            for (int gz = -20; gz < 20; gz++) {
                int x = gx * 96 + 13;
                int z = gz * 96 + 7;
                int h = generator.getBaseHeight(x, z, Heightmap.Types.OCEAN_FLOOR_WG, heights, random);
                samples++;
                if (h >= UrmeerTerrain.SEA_LEVEL) {
                    land++;
                } else if (h <= UrmeerTerrain.SEA_LEVEL - 25) {
                    deep++;
                }
                var biome = resolver.getNoiseBiome(x >> 2, UrmeerTerrain.SEA_LEVEL >> 2, z >> 2);
                for (int i = 0; i < found.length; i++) {
                    if (biome.is(UrmeerBiomes.ALL.get(i))) {
                        found[i]++;
                    }
                }
                if (h < UrmeerTerrain.SEA_LEVEL - 20 && columnsChecked < 6) {
                    var column = generator.getBaseColumn(x, z, heights, random);
                    helper.assertTrue(column.getBlock(UrmeerTerrain.SEA_LEVEL - 1).is(Blocks.WATER), "Kein Wasser am Meeresspiegel bei " + x + " " + z);
                    helper.assertTrue(column.getBlock(UrmeerTerrain.SEA_LEVEL).isAir(), "Das Meer steht zu hoch bei " + x + " " + z);
                    helper.assertTrue(column.getBlock(h - 6).is(SeaBlocks.TIDE_STONE.base().get()), "Unter dem Grund liegt kein Gezeitenstein bei "
                            + x + " " + z + ": " + column.getBlock(h - 6));
                    columnsChecked++;
                }
            }
        }
        KingdomsAtWar.LOGGER.info("[Urmeer] {} Proben: Land {}, tiefes Meer {}; Weiten {}, Korallenbänke {}, Gräben {}, Sturminseln {}, Tangwald {}, "
                + "Korallengarten {}, Perlmuttgründe {}, Leuchtende Tiefe {}", samples, land, deep, found[0], found[1], found[2], found[3], found[4],
                found[5], found[6], found[7]);
        for (int i = 0; i < found.length; i++) {
            helper.assertTrue(found[i] > 0, "Biom nirgends gefunden: " + UrmeerBiomes.ALL.get(i).identifier());
        }
        // wie oft taugt der Grund für eine Hexengrotte (eben genug, tief genug)?
        int okRough = 0;
        int okDepth = 0;
        int okBoth = 0;
        int[] rough = new int[30];
        java.util.Map<String, Integer> grottoBiomes = new java.util.TreeMap<>();
        for (int gx = -12; gx < 12; gx++) {
            for (int gz = -12; gz < 12; gz++) {
                int x = gx * 160 + 8;
                int z = gz * 160 + 8;
                int[] hs = new int[25];
                int i = 0;
                for (int dx = -2; dx <= 2; dx++) {
                    for (int dz = -2; dz <= 2; dz++) {
                        hs[i++] = generator.getBaseHeight(x + dx * io.github.sirschrolli.kingdomsatwar.worldgen.witch.WitchGrottoStructure.SPREAD,
                                z + dz * io.github.sirschrolli.kingdomsatwar.worldgen.witch.WitchGrottoStructure.SPREAD, Heightmap.Types.OCEAN_FLOOR_WG,
                                heights, random);
                    }
                }
                java.util.Arrays.sort(hs);
                int r = hs[hs.length - 3] - hs[2];
                rough[Math.min(29, r)]++;
                boolean a = r <= io.github.sirschrolli.kingdomsatwar.worldgen.witch.WitchGrottoStructure.MAX_ROUGHNESS;
                boolean b = UrmeerTerrain.SEA_LEVEL - (hs[12] - 1) >= io.github.sirschrolli.kingdomsatwar.worldgen.witch.WitchGrottoPiece.HEIGHT + 6;
                okRough += a ? 1 : 0;
                okDepth += b ? 1 : 0;
                okBoth += a && b ? 1 : 0;
                var gb = resolver.getNoiseBiome(x >> 2, (hs[12] + 3) >> 2, z >> 2);
                String name = gb.unwrapKey().map(k -> k.identifier().getPath()).orElse("?");
                grottoBiomes.merge(name + (a && b ? "+" : "-"), 1, Integer::sum);
            }
        }
        KingdomsAtWar.LOGGER.info("[Urmeer] Hexengrotte: eben genug {}/576, tief genug {}/576, beides {}/576; Unebenheit {}; Biome {}", okRough,
                okDepth, okBoth, java.util.Arrays.toString(rough), grottoBiomes);
        helper.assertTrue(okBoth > 576 / 5, "Zu wenig ebener, tiefer Grund für Hexengrotten: " + okBoth + " von 576");
        float landShare = land / (float) samples;
        float deepShare = deep / (float) samples;
        helper.assertTrue(deepShare > 0.55f, "Zu wenig offenes, tiefes Meer: " + deepShare);
        helper.assertTrue(land > 0 && landShare < 0.15f, "Inseln: " + land + " von " + samples);
        helper.assertTrue(columnsChecked > 0, "Keine Säule geprüft");
        // Angeln: Salzwasser überall, Tiefsee über den Weiten
        if (sea != null) {
            BlockPos probe = new BlockPos(13, UrmeerTerrain.SEA_LEVEL - 1, 7);
            sea.getChunk(probe.getX() >> 4, probe.getZ() >> 4);
            helper.assertTrue(Habitat.at(sea, probe, Medium.WATER).contains(Habitat.SALT), "Im Urmeer beißen keine Salzwasserfische");
        }
        helper.succeed();
    }

    /**
     * Aquamarin (0.97): das Erz braucht eine Eisenspitzhacke und gibt Aquamarin; jedes Biom des Urmeers hat Adern und Findlinge;
     * eine Ader wächst im Gezeitenstein, ein Findling auf dem Grund (mit Aquamarin darin); die Taucherrüstung hilft unter
     * Wasser (Atem, Schwimmen, Abbauen – und die Rüstung selbst bleibt), der volle Satz schenkt unter Wasser klare Sicht.
     */
    private static void aquamarine(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        var ore = io.github.sirschrolli.kingdomsatwar.registry.SeaGear.AQUAMARINE_ORE.get().defaultBlockState();
        helper.assertTrue(ore.requiresCorrectToolForDrops(), "Aquamarinerz fällt auch ohne Werkzeug");
        helper.assertFalse(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.STONE_PICKAXE).isCorrectToolForDrops(ore),
                "Eine Steinspitzhacke reicht für Aquamarinerz");
        helper.assertTrue(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.IRON_PICKAXE).isCorrectToolForDrops(ore),
                "Eine Eisenspitzhacke reicht nicht für Aquamarinerz");
        BlockPos orePos = helper.absolutePos(new BlockPos(1, 1, 1));
        level.setBlock(orePos, ore, net.minecraft.world.level.block.Block.UPDATE_ALL);
        var drops = net.minecraft.world.level.block.Block.getDrops(ore, level, orePos, null, null,
                new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.IRON_PICKAXE));
        helper.assertTrue(drops.stream().anyMatch(st -> st.is(io.github.sirschrolli.kingdomsatwar.registry.SeaGear.AQUAMARINE.get())),
                "Aquamarinerz gibt keinen Aquamarin: " + drops);
        level.setBlock(orePos, Blocks.AIR.defaultBlockState(), net.minecraft.world.level.block.Block.UPDATE_ALL);

        // in jedem Biom: Adern und Findlinge
        var biomes = level.registryAccess().lookupOrThrow(Registries.BIOME);
        for (ResourceKey<Biome> key : UrmeerBiomes.ALL) {
            var settings = biomes.getOrThrow(key).value().getGenerationSettings();
            boolean veins = false;
            boolean rocks = false;
            for (var step : settings.features()) {
                for (var placed : step) {
                    veins |= placed.is(io.github.sirschrolli.kingdomsatwar.urmeer.UrmeerFeatures.ORE_AQUAMARINE_PLACED);
                    rocks |= placed.is(io.github.sirschrolli.kingdomsatwar.urmeer.UrmeerFeatures.AQUAMARINE_ROCKS_PLACED);
                }
            }
            helper.assertTrue(veins, "Keine Aquamarinadern im Biom " + key.identifier());
            helper.assertTrue(rocks, "Keine Aquamarinfindlinge im Biom " + key.identifier());
        }

        // eine Ader im Gezeitenstein, ein Findling auf Sand
        var features = level.registryAccess().lookupOrThrow(Registries.FEATURE);
        var generator = level.getChunkSource().getGenerator();
        BlockPos center = helper.absolutePos(new BlockPos(4, 4, 4));
        var tide = SeaBlocks.TIDE_STONE.base().get().defaultBlockState();
        for (BlockPos p : BlockPos.betweenClosed(center.offset(-3, -3, -3), center.offset(3, 3, 3))) {
            level.setBlock(p, tide, net.minecraft.world.level.block.Block.UPDATE_CLIENTS);
        }
        int veinOre = 0;
        for (int seed = 0; seed < 4 && veinOre == 0; seed++) {
            features.getOrThrow(io.github.sirschrolli.kingdomsatwar.urmeer.UrmeerFeatures.ORE_AQUAMARINE).value().place(level, generator,
                    net.minecraft.util.RandomSource.create(seed), center);
            for (BlockPos p : BlockPos.betweenClosed(center.offset(-3, -3, -3), center.offset(3, 3, 3))) {
                veinOre += level.getBlockState(p).is(io.github.sirschrolli.kingdomsatwar.registry.SeaGear.AQUAMARINE_ORE.get()) ? 1 : 0;
            }
        }
        helper.assertTrue(veinOre > 0, "Keine Ader im Gezeitenstein gewachsen");
        for (BlockPos p : BlockPos.betweenClosed(center.offset(-3, -3, -3), center.offset(3, 3, 3))) {
            level.setBlock(p, Blocks.AIR.defaultBlockState(), net.minecraft.world.level.block.Block.UPDATE_CLIENTS);
        }
        BlockPos rockAt = helper.absolutePos(new BlockPos(4, 2, 4));
        for (BlockPos p : BlockPos.betweenClosed(rockAt.offset(-7, -1, -7), rockAt.offset(7, -1, 7))) {
            level.setBlock(p, Blocks.SAND.defaultBlockState(), net.minecraft.world.level.block.Block.UPDATE_CLIENTS);
        }
        int rockStone = 0;
        int rockOre = 0;
        for (int seed = 0; seed < 6 && rockOre == 0; seed++) {
            features.getOrThrow(io.github.sirschrolli.kingdomsatwar.urmeer.UrmeerFeatures.AQUAMARINE_ROCKS).value().place(level, generator,
                    net.minecraft.util.RandomSource.create(100 + seed), rockAt);
            rockStone = 0;
            for (BlockPos p : BlockPos.betweenClosed(rockAt.offset(-7, -4, -7), rockAt.offset(7, 7, 7))) {
                BlockState st = level.getBlockState(p);
                rockStone += st.is(SeaBlocks.TIDE_STONE.base().get()) ? 1 : 0;
                rockOre += st.is(io.github.sirschrolli.kingdomsatwar.registry.SeaGear.AQUAMARINE_ORE.get()) ? 1 : 0;
            }
        }
        helper.assertTrue(rockStone >= 6, "Kein Findling aus Gezeitenstein: " + rockStone);
        helper.assertTrue(rockOre > 0, "Kein Aquamarin im Findling");
        for (BlockPos p : BlockPos.betweenClosed(rockAt.offset(-7, -4, -7), rockAt.offset(7, 7, 7))) {
            level.setBlock(p, Blocks.AIR.defaultBlockState(), net.minecraft.world.level.block.Block.UPDATE_CLIENTS);
        }

        // die Taucherrüstung: unter Wasser helfen die Teile, die Rüstung selbst bleibt
        var set = io.github.sirschrolli.kingdomsatwar.registry.SeaGear.aquamarineSet();
        double[] oxygen = new double[1];
        double[] swim = new double[1];
        double[] mining = new double[1];
        double[] armor = new double[1];
        net.minecraft.world.entity.EquipmentSlot[] slots = {net.minecraft.world.entity.EquipmentSlot.HEAD, net.minecraft.world.entity.EquipmentSlot.CHEST,
                net.minecraft.world.entity.EquipmentSlot.LEGS, net.minecraft.world.entity.EquipmentSlot.FEET};
        for (int i = 0; i < set.size(); i++) {
            var piece = set.get(i);
            var modifiers = new net.minecraft.world.item.ItemStack(piece.get()).getOrDefault(net.minecraft.core.component.DataComponents.ATTRIBUTE_MODIFIERS,
                    net.minecraft.world.item.component.ItemAttributeModifiers.EMPTY);
            double before = armor[0];
            modifiers.forEach(slots[i], (attribute, modifier) -> {
                if (attribute.is(net.minecraft.world.entity.ai.attributes.Attributes.OXYGEN_BONUS)) {
                    oxygen[0] += modifier.amount();
                } else if (attribute.is(net.minecraft.world.entity.ai.attributes.Attributes.WATER_MOVEMENT_EFFICIENCY)) {
                    swim[0] += modifier.amount();
                } else if (attribute.is(net.minecraft.world.entity.ai.attributes.Attributes.SUBMERGED_MINING_SPEED)) {
                    mining[0] += modifier.amount();
                } else if (attribute.is(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR)) {
                    armor[0] += modifier.amount();
                }
            });
            helper.assertTrue(armor[0] > before, "Ein Teil der Taucherrüstung schützt nicht: " + piece.getId());
        }
        helper.assertTrue(Math.abs(oxygen[0] - 2.5) < 0.01, "Helm und Harnisch lassen nicht länger atmen: " + oxygen[0]);
        helper.assertTrue(Math.abs(swim[0] - 0.66) < 0.01, "Beinschutz und Flossen lassen nicht schneller schwimmen: " + swim[0]);
        helper.assertTrue(mining[0] > 0.3, "Der Helm lässt unter Wasser nicht schneller abbauen: " + mining[0]);
        helper.assertTrue(armor[0] >= 15.0, "Die Taucherrüstung schützt zu wenig: " + armor[0]);

        // klare Sicht: nur mit dem vollen Satz und den Augen unter Wasser
        BlockPos pool = helper.absolutePos(new BlockPos(6, 1, 6));
        for (BlockPos p : BlockPos.betweenClosed(pool, pool.above(2))) {
            level.setBlock(p, Blocks.WATER.defaultBlockState(), net.minecraft.world.level.block.Block.UPDATE_CLIENTS);
        }
        ServerPlayer player = MageGameTests.survivalPlayer(helper, "test-aquamarine-diver", new io.netty.channel.embedded.EmbeddedChannel[1], 0);
        player.teleportTo(level, pool.getX() + 0.5, pool.getY(), pool.getZ() + 0.5, java.util.Set.of(), 0.0f, 0.0f, true);
        helper.assertTrue(io.github.sirschrolli.kingdomsatwar.item.sea.AquamarineArmor.eyesInWater(player), "Der Taucher hat die Augen nicht unter Wasser");
        player.setItemSlot(net.minecraft.world.entity.EquipmentSlot.HEAD, new net.minecraft.world.item.ItemStack(set.get(0).get()));
        helper.assertFalse(io.github.sirschrolli.kingdomsatwar.item.sea.AquamarineArmor.refresh(player), "Klare Sicht schon mit einem Teil");
        player.setItemSlot(net.minecraft.world.entity.EquipmentSlot.CHEST, new net.minecraft.world.item.ItemStack(set.get(1).get()));
        player.setItemSlot(net.minecraft.world.entity.EquipmentSlot.LEGS, new net.minecraft.world.item.ItemStack(set.get(2).get()));
        player.setItemSlot(net.minecraft.world.entity.EquipmentSlot.FEET, new net.minecraft.world.item.ItemStack(set.get(3).get()));
        helper.assertTrue(io.github.sirschrolli.kingdomsatwar.item.sea.AquamarineArmor.pieces(player) == 4, "Der volle Satz zählt nicht");
        helper.assertTrue(io.github.sirschrolli.kingdomsatwar.item.sea.AquamarineArmor.refresh(player), "Keine klare Sicht unter Wasser");
        var sight = player.getEffect(net.minecraft.world.effect.MobEffects.NIGHT_VISION);
        helper.assertTrue(sight != null && sight.isAmbient() && !sight.showIcon() && sight.getDuration() > 200,
                "Die klare Sicht fehlt (oder zeigt ein Symbol, oder flackert bald): " + sight);
        // seit 0.99: der volle Satz lässt unter Wasser atmen – still, ohne Symbol
        var breath = player.getEffect(net.minecraft.world.effect.MobEffects.WATER_BREATHING);
        helper.assertTrue(breath != null && !breath.showIcon() && !breath.isVisible(), "Keine stille Wasseratmung mit dem vollen Satz: " + breath);
        player.teleportTo(level, pool.getX() + 0.5, pool.getY() + 6, pool.getZ() + 0.5, java.util.Set.of(), 0.0f, 0.0f, true);
        helper.assertFalse(io.github.sirschrolli.kingdomsatwar.item.sea.AquamarineArmor.refresh(player), "Klare Sicht auch über Wasser");
        helper.assertTrue(player.getEffect(net.minecraft.world.effect.MobEffects.NIGHT_VISION) == null, "Die klare Sicht endet nicht beim Auftauchen");
        helper.assertTrue(player.hasEffect(net.minecraft.world.effect.MobEffects.WATER_BREATHING), "Die Wasseratmung endet schon beim Auftauchen");
        // ein schwächerer, langer Trank wartet unter dem Zustand der Rüstung und bleibt, wenn sie abgelegt wird
        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.WATER_BREATHING, 4800, 0));
        player.setItemSlot(net.minecraft.world.entity.EquipmentSlot.FEET, net.minecraft.world.item.ItemStack.EMPTY);
        io.github.sirschrolli.kingdomsatwar.item.sea.AquamarineArmor.refresh(player);
        var potion = player.getEffect(net.minecraft.world.effect.MobEffects.WATER_BREATHING);
        helper.assertTrue(potion != null && potion.getDuration() > 4000, "Die Rüstung nimmt einen Trank der Wasseratmung weg: " + potion);
        player.removeEffect(net.minecraft.world.effect.MobEffects.WATER_BREATHING);
        player.setItemSlot(net.minecraft.world.entity.EquipmentSlot.FEET, new net.minecraft.world.item.ItemStack(set.get(3).get()));
        io.github.sirschrolli.kingdomsatwar.item.sea.AquamarineArmor.refresh(player);
        helper.assertTrue(player.hasEffect(net.minecraft.world.effect.MobEffects.WATER_BREATHING), "Keine Wasseratmung nach dem Anlegen");
        player.setItemSlot(net.minecraft.world.entity.EquipmentSlot.FEET, net.minecraft.world.item.ItemStack.EMPTY);
        io.github.sirschrolli.kingdomsatwar.item.sea.AquamarineArmor.refresh(player);
        helper.assertTrue(player.getEffect(net.minecraft.world.effect.MobEffects.WATER_BREATHING) == null, "Die Wasseratmung endet nicht beim Ablegen");
        for (BlockPos p : BlockPos.betweenClosed(pool, pool.above(2))) {
            level.setBlock(p, Blocks.AIR.defaultBlockState(), net.minecraft.world.level.block.Block.UPDATE_CLIENTS);
        }
        level.getServer().getPlayerList().remove(player);
        helper.succeed();
    }

    /**
     * Die Gezeitentore: ein Rahmen aus Gezeitensteinziegeln (innen 2 × 3) wird erst mit dem Herz des Meeres zum Tor; die
     * Reise führt ins Urmeer auf eine Plattform mit einem runden Tor über dem Wasser, von dort zurück zum Rahmen. Bricht
     * der Rahmen, erlischt das Tor.
     */
    private static void tideGate(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        for (int x = 0; x < 9; x++) {
            for (int z = 0; z < 9; z++) {
                helper.setBlock(x, 0, z, Blocks.STONE);
            }
        }
        Block bricks = SeaBlocks.TIDE_STONE_BRICKS.base().get();
        for (int x = 2; x <= 5; x++) {
            helper.setBlock(x, 1, 4, bricks);
            helper.setBlock(x, 5, 4, bricks);
        }
        for (int y = 1; y <= 5; y++) {
            helper.setBlock(2, y, 4, bricks);
            helper.setBlock(5, y, 4, bricks);
        }
        // unten im Rahmen steht Wasser: der Schleier dort ist geflutet
        helper.setBlock(3, 2, 4, Blocks.WATER);
        helper.assertFalse(TideGate.tryLight(level, helper.absolutePos(new BlockPos(7, 0, 7)), Direction.UP), "Das Tor entsteht ohne Rahmen");
        helper.assertTrue(TideGate.tryLight(level, helper.absolutePos(new BlockPos(3, 1, 4)), Direction.UP),
                "Das Herz des Meeres erweckt den Rahmen nicht");
        for (int x = 3; x <= 4; x++) {
            for (int y = 2; y <= 4; y++) {
                helper.assertBlockPresent(SeaBlocks.TIDE_GATE.get(), new BlockPos(x, y, 4));
            }
        }
        helper.assertTrue(level.getBlockState(helper.absolutePos(new BlockPos(3, 2, 4))).getValue(TideGateBlock.WATERLOGGED)
                && level.getFluidState(helper.absolutePos(new BlockPos(3, 2, 4))).is(Fluids.WATER), "Der Schleier im Wasser ist nicht geflutet");
        helper.assertFalse(level.getBlockState(helper.absolutePos(new BlockPos(4, 4, 4))).getValue(TideGateBlock.WATERLOGGED),
                "Der Schleier an der Luft ist geflutet");
        // das runde Tor: Schleier und Ring passen zusammen (jeder Schleierblock hat in der Fläche nur Schleier oder Rahmen als Nachbarn)
        int veil = 0;
        for (int a = -TideGate.RING; a <= TideGate.RING; a++) {
            for (int b = -TideGate.RING; b <= TideGate.RING; b++) {
                if (!TideGate.inDisc(a, b)) {
                    continue;
                }
                veil++;
                for (int[] d : new int[][] {{1, 0}, {-1, 0}, {0, 1}, {0, -1}}) {
                    int na = a + d[0];
                    int nb = b + d[1];
                    var ring = TideGate.ringBlock(na, nb);
                    helper.assertTrue(TideGate.inDisc(na, nb) || (ring != null && TideGate.isFrame(ring)),
                            "Der Ring hält den Schleier nicht bei " + na + " " + nb);
                }
            }
        }
        helper.assertTrue(veil == 37, "Der runde Schleier hat " + veil + " Blöcke");
        ServerLevel sea = level.getServer().getLevel(UrmeerDimension.LEVEL);
        var player = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        BlockPos entry = helper.absolutePos(new BlockPos(3, 3, 4));
        TeleportTransition there = TideGate.destination(level, player, entry);
        if (sea != null) {
            helper.assertTrue(there != null && there.newLevel() == sea, "Das Tor führt nicht ins Urmeer");
            BlockPos landing = BlockPos.containing(there.position());
            helper.assertTrue(sea.getBlockState(landing.below()).isSolid(), "Im Urmeer fehlt der Boden unter der Ankunft: " + sea.getBlockState(landing.below()));
            helper.assertTrue(landing.getY() > UrmeerTerrain.SEA_LEVEL - 1 || !sea.getFluidState(landing.below()).isEmpty()
                    || sea.getBlockState(landing.below()).is(SeaBlocks.TIDE_STONE_BRICKS.base().get())
                    || sea.getBlockState(landing.below()).is(SeaBlocks.CHISELED_TIDE_STONE.get())
                    || sea.getBlockState(landing.below()).is(SeaBlocks.TIDE_STONE.base().get()), "Die Ankunft liegt nicht auf der Plattform");
            var state = TideGateState.of(sea);
            var gate = state.nearest(UrmeerDimension.LEVEL, landing, 8);
            helper.assertTrue(gate != null && sea.getBlockState(gate.foot()).is(SeaBlocks.TIDE_GATE.get()), "Im Urmeer steht kein Tor");
            int ring = 0;
            for (BlockPos p : BlockPos.betweenClosed(gate.foot().offset(-6, -2, -6), gate.foot().offset(6, 12, 6))) {
                if (sea.getBlockState(p).is(SeaBlocks.CHISELED_TIDE_STONE.get())) {
                    ring++;
                }
            }
            helper.assertTrue(ring >= 5, "Am Tor im Urmeer fehlen die Schlusssteine: " + ring);
            // und zurück: zum Rahmen in der Oberwelt
            TeleportTransition back = TideGate.destination(sea, player, gate.foot());
            helper.assertTrue(back != null && back.newLevel() == level, "Das Tor im Urmeer führt nicht zurück");
            BlockPos home = BlockPos.containing(back.position());
            helper.assertTrue(home.distManhattan(entry) <= 6, "Der Rückweg endet nicht am Rahmen: " + home + " statt bei " + entry);
        } else {
            helper.assertTrue(there == null, "Ohne Urmeer darf das Tor nirgendwohin führen");
        }
        // Rahmen abgebaut: das Tor erlischt (unten bleibt das Wasser stehen)
        helper.setBlock(2, 3, 4, Blocks.AIR);
        helper.runAfterDelay(2, () -> {
            helper.assertBlockNotPresent(SeaBlocks.TIDE_GATE.get(), new BlockPos(3, 3, 4));
            helper.assertBlockNotPresent(SeaBlocks.TIDE_GATE.get(), new BlockPos(3, 2, 4));
            helper.succeed();
        });
    }
}
