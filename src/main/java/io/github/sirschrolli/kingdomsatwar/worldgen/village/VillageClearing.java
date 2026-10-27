package io.github.sirschrolli.kingdomsatwar.worldgen.village;

import com.mojang.serialization.MapCodec;
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
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;

/**
 * Die Lichtung um ein Dorf: Bäume, Felsen und Ruinen, die nach dem Dorf entstehen, halten Abstand zu seinen Häusern,
 * Wegen und dem Platz – als hätten die Dörfler ihr Land gerodet. Gelesen werden nur Chunks, die beim Erzeugen ohnehin
 * bereitstehen (Bezüge in der Nähe, die Startchunks höchstens acht Chunks entfernt).
 */
public final class VillageClearing {
    /** Abstand (Blöcke), den Bäume zusätzlich zur Fläche der Bauteile halten. */
    public static final int MARGIN = 2;

    private VillageClearing() {}

    /** Liegt der Punkt höchstens {@code margin} Blöcke neben einem Bauteil eines Dorfs? */
    public static boolean isNearVillage(WorldGenLevel level, BlockPos pos, int margin) {
        var registry = level.registryAccess().lookupOrThrow(Registries.STRUCTURE);
        LongSet seen = new LongOpenHashSet();
        for (VillageStyle style : VillageStyle.values()) {
            Structure village = registry.getValue(ModStructures.village(style));
            if (village == null) {
                continue;
            }
            seen.clear();
            for (int dx = -1; dx <= 1; dx += 2) {
                for (int dz = -1; dz <= 1; dz += 2) {
                    ChunkAccess chunk = level.getChunk((pos.getX() + dx * margin) >> 4, (pos.getZ() + dz * margin) >> 4,
                            ChunkStatus.STRUCTURE_REFERENCES, false);
                    if (chunk != null) {
                        seen.addAll(chunk.getReferencesForStructure(village));
                    }
                }
            }
            for (long key : seen) {
                ChunkPos start = ChunkPos.unpack(key);
                ChunkAccess chunk = level.getChunk(start.x(), start.z(), ChunkStatus.STRUCTURE_STARTS, false);
                StructureStart found = chunk != null ? chunk.getStartForStructure(village) : null;
                if (found == null || !found.isValid()) {
                    continue;
                }
                for (StructurePiece piece : found.getPieces()) {
                    BoundingBox box = piece.getBoundingBox();
                    if (pos.getX() >= box.minX() - margin && pos.getX() <= box.maxX() + margin
                            && pos.getZ() >= box.minZ() - margin && pos.getZ() <= box.maxZ() + margin) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /** Platzierungsfilter: nur außerhalb der Dörfer (für Bäume, Felsen, Teiche und Ruinen). */
    public enum Filter implements PlacementFilter {
        INSTANCE;

        public static final MapCodec<Filter> CODEC = MapCodec.unit(INSTANCE);

        @Override
        public boolean shouldPlace(PlacementContext context, RandomSource random, BlockPos origin) {
            return !isNearVillage(context.getLevel(), origin, MARGIN);
        }

        @Override
        public MapCodec<Filter> codec() {
            return CODEC;
        }
    }
}
