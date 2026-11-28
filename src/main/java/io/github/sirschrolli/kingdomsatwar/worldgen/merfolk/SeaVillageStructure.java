package io.github.sirschrolli.kingdomsatwar.worldgen.merfolk;

import com.mojang.serialization.MapCodec;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.urmeer.UrmeerBiomes;
import io.github.sirschrolli.kingdomsatwar.worldgen.lair.ModStructures;
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
 * Ein Meeresdorf (seit 0.93): auf dem Grund des Urmeers, wo der Boden eben genug und das Wasser tief genug ist. Gebaut aus
 * dem Holz der Landschaft (Tangholz im Tangwald, Korallenholz im Korallengarten, Perlholz über den Perlmuttgründen …),
 * mit Perlholz und Perlmutt geschmückt. Die Form baut {@link SeaVillagePiece}.
 */
public class SeaVillageStructure extends Structure {
    public static final MapCodec<SeaVillageStructure> CODEC = simpleCodec(SeaVillageStructure::new);
    /** So viel Wasser muss mindestens über dem Dorfplatz stehen. */
    public static final int MIN_DEPTH = 15;
    /** So uneben darf der Grund im Dorf höchstens sein (Unterschied der Höhen). */
    public static final int MAX_ROUGHNESS = 12;

    public SeaVillageStructure(Structure.StructureSettings settings) {
        super(settings);
    }

    @Override
    protected Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext context) {
        ChunkPos chunk = context.chunkPos();
        int x = chunk.getMiddleBlockX();
        int z = chunk.getMiddleBlockZ();
        int sea = context.chunkGenerator().getSeaLevel();
        // der Grund rings um die Mitte (ohne Pflanzen und Bäume): der Platz liegt auf seiner mittleren Höhe
        int[] heights = new int[25];
        int i = 0;
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                heights[i++] = context.chunkGenerator().getBaseHeight(x + dx * 12, z + dz * 12, Heightmap.Types.OCEAN_FLOOR_WG,
                        context.heightAccessor(), context.randomState());
            }
        }
        int[] sorted = heights.clone();
        Arrays.sort(sorted);
        int floor = sorted[sorted.length / 2] - 1;
        if (sorted[sorted.length - 3] - sorted[2] > MAX_ROUGHNESS || sea - floor < MIN_DEPTH) {
            return Optional.empty();
        }
        Holder<Biome> biome = context.biomeResolver().getNoiseBiome(QuartPos.fromBlock(x), QuartPos.fromBlock(floor + 4), QuartPos.fromBlock(z));
        if (!context.validBiome().test(biome)) {
            return Optional.empty();
        }
        int wood = woodFor(biome);
        BlockPos center = new BlockPos(x, floor, z);
        long seed = context.random().nextLong();
        KingdomsAtWar.LOGGER.debug("Meeresdorf bei {} {} {}", x, floor, z);
        return Optional.of(new Structure.GenerationStub(center, builder -> builder.addPiece(new SeaVillagePiece(center, seed, sea, wood))));
    }

    /** Welches Meeresholz (Nummer in SeaWoods.ALL: Tang, Koralle, Perle, Tiefsee) das Dorf baut. */
    static int woodFor(Holder<Biome> biome) {
        if (biome.is(UrmeerBiomes.KELP_FOREST)) {
            return 0;
        }
        if (biome.is(UrmeerBiomes.CORAL_GARDEN) || biome.is(UrmeerBiomes.REEF)) {
            return 1;
        }
        if (biome.is(UrmeerBiomes.PEARL_GROUNDS)) {
            return 2;
        }
        if (biome.is(UrmeerBiomes.GLOW_DEPTHS) || biome.is(UrmeerBiomes.TRENCH)) {
            return 3;
        }
        return 0;
    }

    @Override
    public StructureType<?> type() {
        return ModStructures.SEA_VILLAGE.get();
    }
}
