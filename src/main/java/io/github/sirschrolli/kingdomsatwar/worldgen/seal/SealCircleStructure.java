package io.github.sirschrolli.kingdomsatwar.worldgen.seal;

import com.mojang.serialization.MapCodec;
import java.util.Arrays;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

/**
 * Der Siegelkreis: eine runde Plattform aus uraltem Siegelstein mit sechs Monolithen um das Siegel in der Mitte.
 * Hier werden die Siegelsplitter eingesetzt; nach dem sechsten Sieg öffnet sich hier das Tor in die Schattenwelt.
 * Wo er liegt, bestimmt {@link SealCirclePlacement}, gebaut wird er von {@link SealCirclePiece}.
 */
public class SealCircleStructure extends Structure {
    public static final MapCodec<SealCircleStructure> CODEC = simpleCodec(SealCircleStructure::new);

    public SealCircleStructure(Structure.StructureSettings settings) {
        super(settings);
    }

    @Override
    protected Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext context) {
        ChunkPos chunk = context.chunkPos();
        int x = chunk.getMiddleBlockX();
        int z = chunk.getMiddleBlockZ();
        // Boden der Plattform: Median der Geländehöhe (ein Block über dem Gelände)
        int[] heights = new int[25];
        int n = 0;
        for (int dx = -20; dx <= 20; dx += 10) {
            for (int dz = -20; dz <= 20; dz += 10) {
                heights[n++] = context.chunkGenerator().getBaseHeight(x + dx, z + dz, Heightmap.Types.WORLD_SURFACE_WG,
                        context.heightAccessor(), context.randomState());
            }
        }
        Arrays.sort(heights);
        int floor = Math.max(heights[12], context.chunkGenerator().getSeaLevel() + 1);
        BlockPos center = new BlockPos(x, floor, z);
        long seed = context.random().nextLong();
        return Optional.of(new Structure.GenerationStub(center, builder -> builder.addPiece(new SealCirclePiece(center, seed))));
    }

    @Override
    public StructureType<?> type() {
        return io.github.sirschrolli.kingdomsatwar.worldgen.lair.ModStructures.SEAL_CIRCLE.get();
    }
}
