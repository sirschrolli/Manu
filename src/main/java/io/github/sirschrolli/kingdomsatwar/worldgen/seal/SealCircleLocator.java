package io.github.sirschrolli.kingdomsatwar.worldgen.seal;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import io.github.sirschrolli.kingdomsatwar.worldgen.lair.NestLocator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.densityfunction.DensityFunction;
import net.minecraft.world.level.levelgen.densityfunction.SamplerContext;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import org.jspecify.annotations.Nullable;

/**
 * Findet den einen Ort des Siegelkreises in einer Welt (rein aus dem Startwert, also immer gleich): spiralförmig in
 * einem Ring um den Weltursprung, im Landesinneren, in einem passenden Biom (Ebenen, Wiesen, lichte Wälder …), auf
 * möglichst ebenem Gelände und weit weg von allen Drachennestern.
 *
 * <p>Die Geländehöhe wird schon vor der Erzeugung der Chunks über die „vorläufige Oberfläche“ des Geländegenerators
 * geschätzt (wie sie auch das Spiel für Grundwasser und Oberfläche nutzt).
 */
public final class SealCircleLocator {
    /** Radius der Fläche, die im Biom liegen muss. */
    private static final int AREA = 32;
    /** Mindestabstand zu jedem Drachennest. */
    private static final int NEST_CLEARANCE = 400;
    /** Größter erlaubter Höhenunterschied auf der Fläche (beim ersten Durchgang). */
    private static final int MAX_RELIEF = 9;
    private static final Map<ChunkGeneratorStructureState, Optional<ChunkPos>> CACHE = Collections.synchronizedMap(new WeakHashMap<>());
    private static volatile @Nullable Cached last;

    private record Cached(ChunkGeneratorStructureState state, Optional<ChunkPos> pos) {}

    private SealCircleLocator() {}

    public static @Nullable ChunkPos find(ServerLevel level) {
        return find(level.getServer().overworld().getChunkSource().getGeneratorState());
    }

    /** Ungefähre Mitte des Kreises (Mitte seines Chunks; Höhe unbekannt, daher Meereshöhe). */
    public static @Nullable BlockPos approximateCenter(ServerLevel level) {
        ChunkPos chunk = find(level);
        ServerLevel overworld = level.getServer().overworld();
        return chunk == null ? null : new BlockPos(chunk.getMiddleBlockX(), overworld.getSeaLevel(), chunk.getMiddleBlockZ());
    }

    public static @Nullable ChunkPos find(ChunkGeneratorStructureState state) {
        Cached cached = last;
        if (cached != null && cached.state() == state) {
            return cached.pos().orElse(null);
        }
        Optional<ChunkPos> pos = CACHE.get(state);
        if (pos == null) {
            synchronized (CACHE) {
                pos = CACHE.get(state);
                if (pos == null) {
                    pos = Optional.ofNullable(compute(state));
                    CACHE.put(state, pos);
                }
            }
        }
        last = new Cached(state, pos);
        return pos.orElse(null);
    }

    private static @Nullable ChunkPos compute(ChunkGeneratorStructureState state) {
        SealCirclePlacement placement = null;
        for (Holder<StructureSet> set : state.possibleStructureSets()) {
            if (set.value().placement() instanceof SealCirclePlacement p) {
                placement = p;
            }
        }
        if (placement == null) {
            return null;
        }
        BiomeSource source = NestLocator.biomeSource(state);
        if (source == null) {
            return null;
        }
        long started = System.currentTimeMillis();
        Climate.Sampler sampler = state.randomState().createClimateSampler(SamplerContext.EMPTY_UNCACHED);
        BiomeResolver resolver = source.createResolver(sampler);
        List<int[]> nests = new ArrayList<>();
        for (DragonType type : DragonType.values()) {
            ChunkPos nest = NestLocator.find(state, type);
            if (nest != null) {
                nests.add(new int[] {nest.getMiddleBlockX(), nest.getMiddleBlockZ()});
            }
        }
        ChunkPos origin = state.getDimensionOrigin();
        int ox = origin.getMiddleBlockX();
        int oz = origin.getMiddleBlockZ();
        RandomSource random = RandomSource.create(state.getLevelSeed() ^ 0x51E9C1C1L ^ placement.salt());
        double base = random.nextDouble() * Mth.TWO_PI;
        int[] found = search(state.randomState(), resolver, sampler, placement, nests, ox, oz, base, true);
        if (found == null) {
            found = search(state.randomState(), resolver, sampler, placement, nests, ox, oz, base, false);
        }
        if (found == null) {
            int r = placement.minDistance();
            found = new int[] {ox + (int) (Math.cos(base) * r), oz + (int) (Math.sin(base) * r)};
            KingdomsAtWar.LOGGER.info("Siegelkreis: kein passender Ort gefunden, liegt bei {} {}", found[0], found[1]);
        }
        ChunkPos result = new ChunkPos(found[0] >> 4, found[1] >> 4);
        KingdomsAtWar.LOGGER.info("Siegelkreis berechnet in {} ms: {}", System.currentTimeMillis() - started, result);
        return result;
    }

    private static int @Nullable [] search(RandomState randomState, BiomeResolver resolver, Climate.Sampler sampler, SealCirclePlacement p,
            List<int[]> nests, int ox, int oz, double base, boolean strict) {
        int maxDistance = strict ? p.maxDistance() : p.maxDistance() * 2;
        for (int r = p.minDistance(); r <= maxDistance; r += 32) {
            int steps = Math.max(12, (int) (Mth.TWO_PI * r / 48));
            for (int k = 0; k < steps; k++) {
                int idx = (k % 2 == 0) ? k / 2 : -(k + 1) / 2;
                double a = base + idx * Mth.TWO_PI / steps;
                int x = ox + (int) Math.round(Math.cos(a) * r);
                int z = oz + (int) Math.round(Math.sin(a) * r);
                if (nearNest(nests, x, z)) {
                    continue;
                }
                if (!NestLocator.fitsArea(resolver, sampler, p.biomes(), true, x, z, AREA)) {
                    continue;
                }
                if (relief(randomState, x, z) <= (strict ? MAX_RELIEF : MAX_RELIEF * 2)) {
                    return new int[] {x, z};
                }
            }
        }
        return null;
    }

    private static boolean nearNest(List<int[]> nests, int x, int z) {
        for (int[] nest : nests) {
            double dx = nest[0] - x;
            double dz = nest[1] - z;
            if (dx * dx + dz * dz < (double) NEST_CLEARANCE * NEST_CLEARANCE) {
                return true;
            }
        }
        return false;
    }

    /** Höhenunterschied der geschätzten Oberfläche auf der Fläche (Mitte und drei Ringe), unter Meereshöhe zählt als uneben. */
    public static int relief(RandomState randomState, int x, int z) {
        DensityFunction surface = randomState.surfaceSystem().preliminarySurfaceFunction();
        int sea = randomState.surfaceSystem().getSeaLevel();
        float min = Float.MAX_VALUE;
        float max = -Float.MAX_VALUE;
        int[][] rings = {{0, 1}, {10, 6}, {20, 8}, {28, 10}};
        for (int[] ring : rings) {
            for (int k = 0; k < ring[1]; k++) {
                double a = k * Mth.TWO_PI / ring[1];
                int px = x + (int) Math.round(Math.cos(a) * ring[0]);
                int pz = z + (int) Math.round(Math.sin(a) * ring[0]);
                float h = randomState.sampleBlockValueUncached(surface, px, 0, pz);
                if (h < sea + 1) {
                    return 999;
                }
                min = Math.min(min, h);
                max = Math.max(max, h);
            }
        }
        return Math.round(max - min);
    }
}
