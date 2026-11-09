package io.github.sirschrolli.kingdomsatwar.worldgen.atoll;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import io.github.sirschrolli.kingdomsatwar.worldgen.lair.NestLocator;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.densityfunction.SamplerContext;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import org.jspecify.annotations.Nullable;

/**
 * Wo das Versunkene Atoll liegt (rein aus dem Startwert der Welt): in einer Grundrichtung wird spiralförmig nach einem
 * Meer gesucht, das die ganze Fläche des Atolls (Radius {@link #AREA}) bedeckt – mindestens {@link #NEST_SPACING} Blöcke
 * von jedem Drachennest entfernt. Findet sich nichts, wird der Ring erweitert; als letzte Möglichkeit liegt es in der
 * Grundrichtung (das Atoll formt seine Umgebung selbst).
 */
public final class AtollLocator {
    public static final int AREA = 46;
    public static final int NEST_SPACING = 400;
    private static final Map<ChunkGeneratorStructureState, ChunkPos> CACHE = Collections.synchronizedMap(new WeakHashMap<>());

    private AtollLocator() {}

    /** Chunk des Atolls (oder null, wenn die Welt keines hat). */
    public static @Nullable ChunkPos find(ChunkGeneratorStructureState state, AtollPlacement placement) {
        ChunkPos cached = CACHE.get(state);
        if (cached != null) {
            return cached;
        }
        synchronized (CACHE) {
            cached = CACHE.get(state);
            if (cached == null) {
                cached = compute(state, placement);
                if (cached != null) {
                    CACHE.put(state, cached);
                }
            }
            return cached;
        }
    }

    private static @Nullable ChunkPos compute(ChunkGeneratorStructureState state, AtollPlacement p) {
        BiomeSource source = NestLocator.biomeSource(state);
        if (source == null) {
            return null;
        }
        long started = System.currentTimeMillis();
        Climate.Sampler sampler = state.randomState().createClimateSampler(SamplerContext.EMPTY_UNCACHED);
        BiomeResolver resolver = source.createResolver(sampler);
        ChunkPos origin = state.getDimensionOrigin();
        int ox = origin.getMiddleBlockX();
        int oz = origin.getMiddleBlockZ();
        RandomSource random = RandomSource.create(state.getLevelSeed() ^ 0xA70_11L ^ p.salt());
        double base = random.nextDouble() * Mth.TWO_PI;
        for (int pass = 1; pass <= 2; pass++) {
            int maxDistance = pass == 1 ? p.maxDistance() : p.maxDistance() * 3;
            int minDistance = pass == 1 ? p.minDistance() : p.minDistance() / 2;
            for (int r = minDistance; r <= maxDistance; r += 48) {
                int steps = Math.max(16, (int) (Mth.TWO_PI * r / 80));
                for (int k = 0; k < steps; k++) {
                    int idx = (k % 2 == 0) ? k / 2 : -(k + 1) / 2;
                    double a = base + idx * Mth.TWO_PI / steps;
                    int x = ox + (int) Math.round(Math.cos(a) * r);
                    int z = oz + (int) Math.round(Math.sin(a) * r);
                    if (!farFromNests(state, x, z)) {
                        continue;
                    }
                    if (NestLocator.fitsArea(resolver, sampler, p.biomes(), false, x, z, AREA)) {
                        KingdomsAtWar.LOGGER.info("Versunkenes Atoll bei {} {} (gesucht in {} ms)", x, z, System.currentTimeMillis() - started);
                        return new ChunkPos(x >> 4, z >> 4);
                    }
                }
            }
        }
        int x = ox + (int) (Math.cos(base) * p.minDistance());
        int z = oz + (int) (Math.sin(base) * p.minDistance());
        KingdomsAtWar.LOGGER.info("Versunkenes Atoll: kein passendes Meer gefunden, liegt bei {} {}", x, z);
        return new ChunkPos(x >> 4, z >> 4);
    }

    private static boolean farFromNests(ChunkGeneratorStructureState state, int x, int z) {
        for (DragonType type : DragonType.values()) {
            ChunkPos nest = NestLocator.find(state, type);
            if (nest == null) {
                continue;
            }
            double dx = nest.getMiddleBlockX() - x;
            double dz = nest.getMiddleBlockZ() - z;
            if (dx * dx + dz * dz < (double) NEST_SPACING * NEST_SPACING) {
                return false;
            }
        }
        return true;
    }

    /** Chunk des Atolls in dieser Welt (null, wenn sie keines hat). */
    public static @Nullable ChunkPos find(ServerLevel level) {
        ChunkGeneratorStructureState state = level.getChunkSource().getGeneratorState();
        for (Holder<StructureSet> set : state.possibleStructureSets()) {
            if (set.value().placement() instanceof AtollPlacement p) {
                return find(state, p);
            }
        }
        return null;
    }

    /** Mitte der Lagune (auf der Mitte des Tempelplatzes), null ohne Atoll. */
    public static @Nullable BlockPos center(ServerLevel level) {
        ChunkPos chunk = find(level);
        return chunk == null ? null : new BlockPos(chunk.getMiddleBlockX(), AtollPiece.platformTop(level.getSeaLevel()) + 1, chunk.getMiddleBlockZ());
    }

    /** Lage des Herzens des Atolls (unter der Mitte). */
    public static BlockPos core(BlockPos center) {
        return center.below(5);
    }
}
