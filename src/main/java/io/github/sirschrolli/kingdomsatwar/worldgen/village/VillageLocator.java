package io.github.sirschrolli.kingdomsatwar.worldgen.village;

import io.github.sirschrolli.kingdomsatwar.worldgen.lair.ModStructures;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureCheckResult;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import org.jspecify.annotations.Nullable;

/**
 * Findet Dörfer in der Nähe – für die Karten des Kartografen und für das, was sich die Dörfler über ihre Nachbarn
 * erzählen. Gesucht wird Ring um Ring im Raster der Dorf-Platzierung; ein Dorf (z. B. das eigene) lässt sich
 * ausschließen. Von einem gefundenen Dorf kennt man Mitte, Name und Bauweise.
 */
public final class VillageLocator {
    private VillageLocator() {}

    /** Ein Dorf: Mitte des Dorfplatzes, Name und Bauweise. */
    public record Found(BlockPos center, String name, VillageStyle style) {}

    /**
     * Das nächste Dorf von {@code from} aus, höchstens {@code rings} Rasterfelder entfernt (ein Feld sind 34 Chunks);
     * {@code skip}: Startchunk eines Dorfs, das nicht zählt.
     */
    public static @Nullable Found nearest(ServerLevel level, BlockPos from, @Nullable ChunkPos skip, int rings) {
        var registry = level.registryAccess().lookupOrThrow(Registries.STRUCTURE);
        List<Holder<Structure>> villages = new ArrayList<>();
        for (VillageStyle style : VillageStyle.values()) {
            registry.get(ModStructures.village(style)).ifPresent(villages::add);
        }
        if (villages.isEmpty()) {
            return null;
        }
        ChunkGeneratorStructureState state = level.getChunkSource().getGeneratorState();
        RandomSpreadStructurePlacement spread = null;
        for (StructurePlacement placement : state.getPlacementsForStructure(villages.get(0))) {
            if (placement instanceof RandomSpreadStructurePlacement random) {
                spread = random;
            }
        }
        if (spread == null) {
            return null;
        }
        StructureManager structures = level.structureManager();
        int ox = from.getX() >> 4;
        int oz = from.getZ() >> 4;
        Found best = null;
        double bestDist = Double.MAX_VALUE;
        for (int r = 0; r <= rings; r++) {
            for (int x = -r; x <= r; x++) {
                for (int z = -r; z <= r; z++) {
                    if (Math.abs(x) != r && Math.abs(z) != r) {
                        continue;
                    }
                    ChunkPos target = spread.getPotentialStructureChunk(state.getLevelSeed(), ox + spread.spacing() * x, oz + spread.spacing() * z);
                    if (target.equals(skip) || !spread.isStructureChunk(state, target.x(), target.z())) {
                        continue;
                    }
                    Found found = at(level, structures, villages, spread, target);
                    if (found != null) {
                        double d = found.center().distSqr(from);
                        if (d < bestDist) {
                            best = found;
                            bestDist = d;
                        }
                    }
                }
            }
            // im ersten Ring mit einem Treffer liegt (fast immer) auch der nächste
            if (best != null) {
                return best;
            }
        }
        return null;
    }

    private static @Nullable Found at(ServerLevel level, StructureManager structures, List<Holder<Structure>> villages,
            RandomSpreadStructurePlacement spread, ChunkPos target) {
        for (Holder<Structure> village : villages) {
            StructureCheckResult check = structures.checkStructurePresence(target, village.value(), spread, false);
            if (check == StructureCheckResult.START_NOT_PRESENT) {
                continue;
            }
            ChunkAccess chunk = level.getChunk(target.x(), target.z(), ChunkStatus.STRUCTURE_STARTS);
            StructureStart start = structures.getStartForStructure(village.value(), chunk);
            if (start != null && start.isValid() && !start.getPieces().isEmpty()
                    && start.getPieces().getFirst() instanceof KingdomVillagePiece square) {
                return new Found(square.center(), square.villageName(), square.style());
            }
        }
        return null;
    }

    /** Der Startchunk eines Dorfs mit dieser Mitte (der Dorfplatz liegt in der Mitte seines Startchunks). */
    public static ChunkPos startChunk(BlockPos center) {
        return ChunkPos.containing(center);
    }
}
