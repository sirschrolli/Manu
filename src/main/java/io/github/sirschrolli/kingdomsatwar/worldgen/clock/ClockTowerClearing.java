package io.github.sirschrolli.kingdomsatwar.worldgen.clock;

import com.mojang.serialization.MapCodec;
import io.github.sirschrolli.kingdomsatwar.worldgen.GenChunks;
import io.github.sirschrolli.kingdomsatwar.worldgen.lair.ModStructures;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementFilter;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;

/**
 * Das leer gefegte Auge um einen Uhrturm (seit 1.1): Jeder Chunk räumt seinen Teil frei, sobald er entsteht – Splitter und
 * Pflanzen der Nachbarchunks, die erst danach entstehen, würden hineinwachsen. Deshalb halten alle Features des Abgrunds
 * Abstand: Wer näher als {@link #KEEP_OUT} Blöcke an der Mitte eines Uhrturms beginnt, entsteht nicht (der freigeräumte Raum
 * und der größte Splitter). Gelesen werden nur Chunks, die beim Erzeugen ohnehin bereitstehen.
 */
public final class ClockTowerClearing {
    /** So weit (waagerecht, Blöcke) von der Mitte des Zifferblatts beginnt kein Feature. */
    public static final double KEEP_OUT = ClockTowerPiece.R_CLEAR + 15.0;

    private ClockTowerClearing() {}

    /** Beginnt hier ein Feature zu nah an einem Uhrturm? */
    public static boolean isNearClockTower(WorldGenLevel level, BlockPos pos) {
        Structure tower = level.registryAccess().lookupOrThrow(Registries.STRUCTURE).getValue(ModStructures.CLOCK_TOWER_STRUCTURE);
        if (tower == null) {
            return false;
        }
        // die Bezüge in den vier Nachbarecken: Das Bauwerk reicht 48 Blöcke weit, so findet es sich bis gut 60 Blöcke
        LongSet seen = new LongOpenHashSet();
        for (int dx = -1; dx <= 1; dx += 2) {
            for (int dz = -1; dz <= 1; dz += 2) {
                ChunkAccess chunk = GenChunks.readable(level, (pos.getX() + dx * 16) >> 4, (pos.getZ() + dz * 16) >> 4, ChunkStatus.STRUCTURE_REFERENCES);
                if (chunk != null) {
                    seen.addAll(chunk.getReferencesForStructure(tower));
                }
            }
        }
        for (long key : seen) {
            ChunkPos start = ChunkPos.unpack(key);
            ChunkAccess chunk = GenChunks.readable(level, start.x(), start.z(), ChunkStatus.STRUCTURE_STARTS);
            StructureStart found = chunk != null ? chunk.getStartForStructure(tower) : null;
            if (found == null || !found.isValid()) {
                continue;
            }
            for (StructurePiece piece : found.getPieces()) {
                if (piece instanceof ClockTowerPiece clock) {
                    BlockPos c = clock.center();
                    if (Math.hypot(pos.getX() - c.getX(), pos.getZ() - c.getZ()) < KEEP_OUT) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /** Platzierungsfilter: nur fern der Uhrtürme (für alle Features des Abgrunds). */
    public enum Filter implements PlacementFilter {
        INSTANCE;

        public static final MapCodec<Filter> CODEC = MapCodec.unit(INSTANCE);

        @Override
        public boolean shouldPlace(PlacementContext context, RandomSource random, BlockPos origin) {
            return !isNearClockTower(context.getLevel(), origin);
        }

        @Override
        public MapCodec<Filter> codec() {
            return CODEC;
        }
    }
}
