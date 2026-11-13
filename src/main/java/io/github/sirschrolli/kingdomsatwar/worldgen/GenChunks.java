package io.github.sirschrolli.kingdomsatwar.worldgen;

import net.minecraft.ReportedException;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import org.jspecify.annotations.Nullable;

/**
 * Sicherer Blick in Nachbarchunks während der Weltgenerierung.
 *
 * <p>Beim Erzeugen der Merkmale (Bäume, Felsen, Brücken …) stehen nur die direkten Nachbarchunks fertig bereit – mit
 * ihren Bezügen auf Bauwerke –, die Startchunks der Bauwerke bis acht Chunks weit. Seit Minecraft 26 bricht jeder
 * Zugriff darüber hinaus das Spiel ab („Requested chunk unavailable during world generation“), auch wenn man nur
 * nachsehen wollte; früher kam einfach nichts zurück. Diese Hilfe liefert in dem Fall {@code null}.
 */
public final class GenChunks {
    private GenChunks() {}

    /** Der Chunk mit mindestens {@code status}, wenn er gerade gelesen werden darf (sonst {@code null}). */
    public static @Nullable ChunkAccess readable(LevelReader level, int chunkX, int chunkZ, ChunkStatus status) {
        if (level instanceof WorldGenRegion region) {
            ChunkPos center = region.getCenter();
            int distance = Math.max(Math.abs(chunkX - center.x()), Math.abs(chunkZ - center.z()));
            int limit = status.isOrBefore(ChunkStatus.STRUCTURE_STARTS) ? 8 : 1;
            if (distance > limit) {
                return null;
            }
            try {
                return level.getChunk(chunkX, chunkZ, status, false);
            } catch (ReportedException e) {
                // ein anderer Erzeugungsschritt mit engeren Grenzen: dann eben ohne diesen Chunk
                return null;
            }
        }
        return level.getChunk(chunkX, chunkZ, status, false);
    }
}
