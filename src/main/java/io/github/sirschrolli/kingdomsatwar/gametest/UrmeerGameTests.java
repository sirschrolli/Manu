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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
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

    static void register(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment) {
        for (var test : java.util.List.of(URMEER_WORLD, URMEER_TIDE_GATE)) {
            event.registerTest(test.getId(), new FunctionGameTestInstance(test.key(),
                    new TestData<>(environment, Level.OVERWORLD, ARENA, 400, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
        }
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
