package io.github.sirschrolli.kingdomsatwar.worldgen.citadel;

import com.mojang.serialization.MapCodec;
import io.github.sirschrolli.kingdomsatwar.worldgen.lair.ModStructures;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

/**
 * Nyxars Schattenzitadelle: eine achteckige Festung aus Schattenstein (ca. 80 Blöcke breit) mit acht Türmen, einem
 * Torhaus im Süden und einem offenen Thronhof, in dem Nyxar auf seinem Thron schläft. Die Form baut
 * {@link CitadelPiece}; wo sie steht, bestimmt {@link CitadelPlacement}.
 */
public class CitadelStructure extends Structure {
    public static final MapCodec<CitadelStructure> CODEC = simpleCodec(CitadelStructure::new);

    public CitadelStructure(Structure.StructureSettings settings) {
        super(settings);
    }

    @Override
    protected Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext context) {
        ChunkPos chunk = context.chunkPos();
        int x = chunk.getMiddleBlockX();
        int z = chunk.getMiddleBlockZ();
        int floor = floorHeight(context, x, z);
        BlockPos center = new BlockPos(x, floor, z);
        long seed = context.random().nextLong();
        // Für die Biomprüfung zählt die echte Oberfläche in der Mitte (der Hofboden kann ein Stück darunter liegen,
        // dort gilt schon das Biom der Tiefe)
        int surface = context.chunkGenerator().getBaseHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(),
                context.randomState());
        BlockPos check = new BlockPos(x, Math.max(floor, surface), z);
        io.github.sirschrolli.kingdomsatwar.KingdomsAtWar.LOGGER.info("Schattenzitadelle bei {} {} (Hofboden {}, Gelände {}, Biom {})", x, z,
                floor, surface, context.biomeResolver().getNoiseBiome(check.getX() >> 2, check.getY() >> 2, check.getZ() >> 2)
                        .getRegisteredName());
        return Optional.of(new Structure.GenerationStub(check, builder -> builder.addPiece(new CitadelPiece(center, seed))));
    }

    /** Höhe des Hofbodens: Mittel des Geländes über die ganze Fläche (etwas angehoben – die Festung thront über dem Land). */
    static int floorHeight(Structure.GenerationContext context, int x, int z) {
        int sum = 0;
        int n = 0;
        int[][] rings = {{0, 1}, {14, 6}, {26, 10}, {38, 14}};
        for (int[] ring : rings) {
            for (int k = 0; k < ring[1]; k++) {
                double a = k * Math.PI * 2 / ring[1];
                int px = x + (int) Math.round(Math.cos(a) * ring[0]);
                int pz = z + (int) Math.round(Math.sin(a) * ring[0]);
                sum += context.chunkGenerator().getBaseHeight(px, pz, Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(),
                        context.randomState());
                n++;
            }
        }
        int sea = context.chunkGenerator().getSeaLevel();
        return Math.max(sea + 3, Math.round((float) sum / n) + 1);
    }

    @Override
    public StructureType<?> type() {
        return ModStructures.SHADOW_CITADEL.get();
    }
}
