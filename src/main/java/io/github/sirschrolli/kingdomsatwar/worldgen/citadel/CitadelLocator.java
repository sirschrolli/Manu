package io.github.sirschrolli.kingdomsatwar.worldgen.citadel;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.worldgen.lair.NestLocator;
import io.github.sirschrolli.kingdomsatwar.worldgen.seal.SealCircleLocator;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.densityfunction.SamplerContext;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import org.jspecify.annotations.Nullable;

/**
 * Findet den Ort der Schattenzitadelle: aus dem Startwert der Welt, Ring für Ring um die Mitte der Schattenwelt, die
 * erste Stelle, an der eine weite Fläche ganz in einem passenden Biom liegt und das Gelände nicht zu wild ist. Das
 * Ergebnis wird je Welt zwischengespeichert (die Suche dauert einige Dutzend Millisekunden).
 */
public final class CitadelLocator {
    /** Radius der Fläche, die ganz im passenden Biom liegen muss. */
    private static final int AREA = 44;
    private static final int MAX_RELIEF = 16;
    private static final Map<RandomState, Optional<ChunkPos>> CACHE = Collections.synchronizedMap(new WeakHashMap<>());

    private CitadelLocator() {}

    /** Chunk der Zitadelle in der Schattenwelt (null, wenn es dort keine Zitadelle gibt). */
    public static @Nullable ChunkPos find(ServerLevel shadow) {
        ChunkGeneratorStructureState state = shadow.getChunkSource().getGeneratorState();
        for (Holder<StructureSet> set : state.possibleStructureSets()) {
            if (set.value().placement() instanceof CitadelPlacement p) {
                return find(state, p);
            }
        }
        return null;
    }

    /** Ungefähre Mitte der Zitadelle (Höhe: Meereshöhe, solange sie noch nicht erzeugt ist). */
    public static @Nullable BlockPos approximateCenter(ServerLevel shadow) {
        ChunkPos chunk = find(shadow);
        return chunk == null ? null : new BlockPos(chunk.getMiddleBlockX(), shadow.getSeaLevel(), chunk.getMiddleBlockZ());
    }

    public static @Nullable ChunkPos find(ChunkGeneratorStructureState state, CitadelPlacement placement) {
        BiomeSource source = NestLocator.biomeSource(state);
        if (source == null) {
            return null;
        }
        return find(state.getLevelSeed(), source, state.randomState(), placement);
    }

    /** Auch ohne Strukturzustand nutzbar (z. B. von anderen Bauwerken, die der Zitadelle ausweichen). */
    public static @Nullable ChunkPos find(long seed, BiomeSource source, RandomState randomState, CitadelPlacement placement) {
        Optional<ChunkPos> pos = CACHE.get(randomState);
        if (pos == null) {
            synchronized (CACHE) {
                pos = CACHE.get(randomState);
                if (pos == null) {
                    pos = Optional.ofNullable(compute(seed, source, randomState, placement));
                    CACHE.put(randomState, pos);
                }
            }
        }
        return pos.orElse(null);
    }

    private static @Nullable ChunkPos compute(long seed, BiomeSource source, RandomState randomState, CitadelPlacement p) {
        long started = System.currentTimeMillis();
        Climate.Sampler sampler = randomState.createClimateSampler(SamplerContext.EMPTY_UNCACHED);
        BiomeResolver resolver = source.createResolver(sampler);
        RandomSource random = RandomSource.create(seed ^ 0xC17A_DE11L ^ p.salt());
        double base = random.nextDouble() * Mth.TWO_PI;
        int[] found = search(randomState, resolver, sampler, p.biomes(), p.minDistance(), p.maxDistance(), base, MAX_RELIEF);
        if (found == null) {
            found = search(randomState, resolver, sampler, p.biomes(), p.minDistance(), p.maxDistance() * 2, base, MAX_RELIEF * 2);
        }
        if (found == null) {
            int r = p.minDistance();
            found = new int[] {(int) (Math.cos(base) * r), (int) (Math.sin(base) * r)};
            KingdomsAtWar.LOGGER.info("Schattenzitadelle: kein passender Ort gefunden, liegt bei {} {}", found[0], found[1]);
        }
        ChunkPos result = new ChunkPos(found[0] >> 4, found[1] >> 4);
        KingdomsAtWar.LOGGER.info("Schattenzitadelle berechnet in {} ms: {}", System.currentTimeMillis() - started, result);
        return result;
    }

    private static int @Nullable [] search(RandomState randomState, BiomeResolver resolver, Climate.Sampler sampler, HolderSet<Biome> biomes,
            int minDistance, int maxDistance, double base, int maxRelief) {
        for (int r = minDistance; r <= maxDistance; r += 32) {
            int steps = Math.max(12, (int) (Mth.TWO_PI * r / 48));
            for (int k = 0; k < steps; k++) {
                int idx = (k % 2 == 0) ? k / 2 : -(k + 1) / 2;
                double a = base + idx * Mth.TWO_PI / steps;
                int x = (int) Math.round(Math.cos(a) * r);
                int z = (int) Math.round(Math.sin(a) * r);
                if (!NestLocator.fitsArea(resolver, sampler, biomes, false, x, z, AREA)) {
                    continue;
                }
                if (SealCircleLocator.relief(randomState, x, z) <= maxRelief) {
                    return new int[] {x, z};
                }
            }
        }
        return null;
    }
}
