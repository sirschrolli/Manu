package io.github.sirschrolli.kingdomsatwar.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import io.github.sirschrolli.kingdomsatwar.worldgen.lair.NestLocator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementFilter;

/**
 * Platzierung mit Abstand für kleine Bauwerke (z. B. Burgruinen): Die Welt ist in Felder von {@code spacing}×
 * {@code spacing} Chunks eingeteilt, und in jedem Feld kommt nur ein fester Chunk in Frage (wie bei Vanilla-Bauwerken).
 * So liegen zwei Bauwerke mindestens {@code separation} Chunks auseinander. Außerdem bleibt um jedes Drachennest ein
 * Umkreis von {@code nest_distance} Blöcken frei.
 */
public record SpacedPlacement(int spacing, int separation, int salt, int nestDistance) implements PlacementFilter {
    public static final MapCodec<SpacedPlacement> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.intRange(2, 256).fieldOf("spacing").forGetter(SpacedPlacement::spacing),
            Codec.intRange(0, 255).fieldOf("separation").forGetter(SpacedPlacement::separation),
            Codec.INT.fieldOf("salt").forGetter(SpacedPlacement::salt),
            Codec.intRange(0, 4096).optionalFieldOf("nest_distance", 0).forGetter(SpacedPlacement::nestDistance)
    ).apply(instance, SpacedPlacement::new));

    @Override
    public boolean shouldPlace(PlacementContext context, RandomSource random, BlockPos origin) {
        WorldGenLevel level = context.getLevel();
        int chunkX = SectionPos.blockToSectionCoord(origin.getX());
        int chunkZ = SectionPos.blockToSectionCoord(origin.getZ());
        if (!isGridChunk(level.getSeed(), this.salt, chunkX, chunkZ, this.spacing, this.separation)) {
            return false;
        }
        return this.nestDistance <= 0 || farFromNests(level.getLevel(), origin.getX(), origin.getZ(), this.nestDistance);
    }

    /** Ist dieser Chunk der eine mögliche Chunk seines Feldes? (gleiche Rechnung wie Vanilla „random_spread“) */
    public static boolean isGridChunk(long seed, int salt, int chunkX, int chunkZ, int spacing, int separation) {
        int regionX = Math.floorDiv(chunkX, spacing);
        int regionZ = Math.floorDiv(chunkZ, spacing);
        WorldgenRandom random = new WorldgenRandom(new LegacyRandomSource(0L));
        random.setLargeFeatureWithSalt(seed, regionX, regionZ, salt);
        int range = Math.max(1, spacing - separation);
        return regionX * spacing + random.nextInt(range) == chunkX && regionZ * spacing + random.nextInt(range) == chunkZ;
    }

    /** Liegt der Punkt mindestens {@code distance} Blöcke von allen Drachennestern entfernt? */
    public static boolean farFromNests(ServerLevel level, int x, int z, int distance) {
        ChunkGeneratorStructureState state = level.getChunkSource().getGeneratorState();
        for (DragonType type : DragonType.values()) {
            ChunkPos nest = NestLocator.find(state, type);
            if (nest != null) {
                double dx = nest.getMiddleBlockX() - x;
                double dz = nest.getMiddleBlockZ() - z;
                if (dx * dx + dz * dz < (double) distance * distance) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public MapCodec<SpacedPlacement> codec() {
        return CODEC;
    }
}
