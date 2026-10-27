package io.github.sirschrolli.kingdomsatwar.worldgen.lair;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.registry.ModBlocks;
import io.github.sirschrolli.kingdomsatwar.worldgen.ModBiomes;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Letzter Schliff an den Nest-Biomen: Nachbar-Chunks, die später erzeugt werden, setzen manchmal noch Pflanzen oder
 * Bäume über die Chunkgrenze in ein schon fertiges Nest-Biom (z. B. Kakteen oder Gras in die Aschelande). Sobald ein
 * neuer Chunk fertig geladen ist, räumt dieser Schritt dort auf – im nächsten Tick, wie es das Ereignis verlangt.
 */
@EventBusSubscriber(modid = KingdomsAtWar.MODID)
public final class LairCleanup {
    private record Pending(ServerLevel level, ChunkPos pos) {}

    private static final Queue<Pending> QUEUE = new ConcurrentLinkedQueue<>();

    private LairCleanup() {}

    @SubscribeEvent
    static void onChunkLoad(ChunkEvent.Load event) {
        if (!event.isNewChunk() || !(event.getLevel() instanceof ServerLevel level) || level.dimension() != Level.OVERWORLD) {
            return;
        }
        LevelChunk chunk = event.getChunk();
        if (hasNestBiome(chunk)) {
            QUEUE.add(new Pending(level, chunk.getPos()));
        }
    }

    @SubscribeEvent
    static void onServerTick(ServerTickEvent.Post event) {
        for (int i = 0; i < 16; i++) {
            Pending pending = QUEUE.poll();
            if (pending == null) {
                return;
            }
            // notfalls wieder laden (z. B. wenn der Chunk nur kurz für die Weltgenerierung gebraucht wurde)
            LevelChunk chunk = pending.level().getChunk(pending.pos().x(), pending.pos().z());
            if (chunk != null) {
                cleanup(pending.level(), chunk);
            }
        }
    }

    private static boolean hasNestBiome(LevelChunk chunk) {
        int minX = chunk.getPos().getMinBlockX();
        int minZ = chunk.getPos().getMinBlockZ();
        for (int dx = 2; dx < 16; dx += 4) {
            for (int dz = 2; dz < 16; dz += 4) {
                int y = chunk.getHeight(Heightmap.Types.WORLD_SURFACE, minX + dx, minZ + dz);
                if (ModBiomes.isNestBiome(chunk.getNoiseBiome(QuartPos.fromBlock(minX + dx), QuartPos.fromBlock(y), QuartPos.fromBlock(minZ + dz)))) {
                    return true;
                }
            }
        }
        return false;
    }

    /** Räumt auf allen Säulen des Chunks, deren Oberfläche in einem Nest-Biom liegt (je nach Biom). */
    private static void cleanup(ServerLevel level, LevelChunk chunk) {
        int minX = chunk.getPos().getMinBlockX();
        int minZ = chunk.getPos().getMinBlockZ();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int dx = 0; dx < 16; dx++) {
            for (int dz = 0; dz < 16; dz++) {
                int x = minX + dx;
                int z = minZ + dz;
                int top = chunk.getHeight(Heightmap.Types.WORLD_SURFACE, x, z);
                var biome = chunk.getNoiseBiome(QuartPos.fromBlock(x), QuartPos.fromBlock(top), QuartPos.fromBlock(z));
                if (!ModBiomes.isNestBiome(biome) || biome.is(ModBiomes.ANCIENT_GROVE)) {
                    continue;   // im Uralten Hain dürfen Pflanzen und Bäume von nebenan bleiben
                }
                boolean frost = biome.is(ModBiomes.FROSTBASIN);
                boolean rugged = biome.is(ModBiomes.RUGGED_WILDS);
                boolean storm = biome.is(ModBiomes.STORM_CLIFFS);
                for (int y = top; y > top - 28 && y > level.getMinY(); y--) {
                    pos.set(x, y, z);
                    BlockState state = chunk.getBlockState(pos);
                    if (state.isAir() || (frost ? keepFrost(state) : rugged ? keepRugged(state) : storm ? keepStorm(state) : keep(state))) {
                        continue;
                    }
                    if (!state.getFluidState().isEmpty() || LairContext.isGround(state)) {
                        break;
                    }
                    if (!isVegetation(state)) {
                        continue;   // Laternen, Ketten, Stufen, Truhen … gehören zum Nest selbst
                    }
                    if (state.is(BlockTags.LOGS)) {
                        if (rugged || storm) {
                            level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
                        } else if (!frost) {
                            BlockState charred = ModBlocks.CHARRED_LOG.get().defaultBlockState();
                            if (state.hasProperty(RotatedPillarBlock.AXIS)) {
                                charred = charred.setValue(RotatedPillarBlock.AXIS, state.getValue(RotatedPillarBlock.AXIS));
                            }
                            level.setBlock(pos, charred, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
                        }
                    } else {
                        level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
                    }
                }
            }
        }
    }

    /** Was im Frostkessel hingehört: Schnee, Eis, Eiszapfen, Frostkristalle und -blumen. */
    private static boolean keepFrost(BlockState state) {
        return state.is(Blocks.SNOW) || state.is(ModBlocks.ICICLE.get()) || state.is(ModBlocks.FROST_FLOWER.get())
                || state.is(ModBlocks.FROST_CRYSTAL.get()) || state.is(Blocks.STRIPPED_SPRUCE_LOG) || state.is(BlockTags.ICE);
    }

    /** Pflanzen und Bäume (nur die räumt der Schritt weg – alles andere hat das Nest selbst gebaut). */
    private static boolean isVegetation(BlockState state) {
        return state.is(BlockTags.LOGS) || state.is(BlockTags.LEAVES) || state.is(BlockTags.SAPLINGS) || state.is(BlockTags.FLOWERS)
                || state.is(Blocks.CACTUS) || state.is(Blocks.CACTUS_FLOWER) || state.is(Blocks.SUGAR_CANE) || state.is(Blocks.VINE)
                || state.is(Blocks.SWEET_BERRY_BUSH) || state.is(Blocks.PUMPKIN) || state.is(Blocks.MELON) || state.is(Blocks.BEE_NEST)
                || state.is(Blocks.MOSS_CARPET) || state.is(Blocks.BROWN_MUSHROOM_BLOCK) || state.is(Blocks.RED_MUSHROOM_BLOCK)
                || state.is(Blocks.MUSHROOM_STEM) || state.is(Blocks.BAMBOO) || state.is(Blocks.COCOA)
                || state.canBeReplaced() && state.getFluidState().isEmpty() && !state.is(Blocks.SNOW);
    }

    /** Was in die Sturmklippen gehört: Schnee, zähes Gras, Sturmkristalle. */
    private static boolean keepStorm(BlockState state) {
        return state.is(Blocks.SNOW) || state.is(Blocks.SHORT_GRASS) || state.is(ModBlocks.STORM_CRYSTAL.get())
                || state.is(ModBlocks.CHARRED_LOG.get());
    }

    /** Was in die Zerklüfteten Weiten gehört: trockenes Gras, tote Büsche, Bernstein. */
    private static boolean keepRugged(BlockState state) {
        return state.is(Blocks.DEAD_BUSH) || state.is(Blocks.SHORT_DRY_GRASS) || state.is(Blocks.TALL_DRY_GRASS)
                || state.is(ModBlocks.AMBER_CLUSTER.get()) || state.is(ModBlocks.AMBER_CRYSTAL.get());
    }

    /** Was in den Aschelanden hingehört (vom Nest selbst gesetzt). */
    private static boolean keep(BlockState state) {
        return state.is(ModBlocks.ASH_LAYER.get()) || state.is(ModBlocks.FIRE_LILY.get()) || state.is(Blocks.DEAD_BUSH)
                || state.is(ModBlocks.CHARRED_LOG.get()) || state.is(Blocks.FIRE);
    }
}
