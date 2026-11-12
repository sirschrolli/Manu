package io.github.sirschrolli.kingdomsatwar.worldgen.witch;

import com.mojang.serialization.MapCodec;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import java.util.Arrays;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

/**
 * Die Hexengrotte (seit 0.97): eine Kuppel aus Gezeitenstein auf dem Grund des Urmeers, innen voll Wasser – das Reich der
 * Meerhexe. Sie entsteht nur, wo der Grund eben genug ist und über der Kuppel noch genug Wasser steht. Die Form baut
 * {@link WitchGrottoPiece}.
 */
public class WitchGrottoStructure extends Structure {
    public static final MapCodec<WitchGrottoStructure> CODEC = simpleCodec(WitchGrottoStructure::new);
    /** So uneben darf der Grund unter der Grotte höchstens sein. */
    public static final int MAX_ROUGHNESS = 7;
    /** Abstand der Messpunkte (5 × 5 Punkte, also gut die ganze Kuppel). */
    public static final int SPREAD = 8;

    public WitchGrottoStructure(Structure.StructureSettings settings) {
        super(settings);
    }

    @Override
    protected Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext context) {
        ChunkPos chunk = context.chunkPos();
        int x = chunk.getMiddleBlockX();
        int z = chunk.getMiddleBlockZ();
        int sea = context.chunkGenerator().getSeaLevel();
        int[] heights = new int[25];
        int i = 0;
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                heights[i++] = context.chunkGenerator().getBaseHeight(x + dx * SPREAD, z + dz * SPREAD, Heightmap.Types.OCEAN_FLOOR_WG,
                        context.heightAccessor(), context.randomState());
            }
        }
        int[] sorted = heights.clone();
        Arrays.sort(sorted);
        int floor = sorted[sorted.length / 2] - 1;
        if (sorted[sorted.length - 3] - sorted[2] > MAX_ROUGHNESS || sea - floor < WitchGrottoPiece.HEIGHT + 6) {
            return Optional.empty();
        }
        Holder<Biome> biome = context.biomeResolver().getNoiseBiome(QuartPos.fromBlock(x), QuartPos.fromBlock(floor + 4), QuartPos.fromBlock(z));
        if (!context.validBiome().test(biome)) {
            return Optional.empty();
        }
        BlockPos center = new BlockPos(x, floor, z);
        long seed = context.random().nextLong();
        KingdomsAtWar.LOGGER.debug("Hexengrotte bei {} {} {}", x, floor, z);
        return Optional.of(new Structure.GenerationStub(center, builder -> builder.addPiece(new WitchGrottoPiece(center, seed, sea))));
    }

    @Override
    public StructureType<?> type() {
        return io.github.sirschrolli.kingdomsatwar.worldgen.lair.ModStructures.WITCH_GROTTO.get();
    }
}
