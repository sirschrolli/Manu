package io.github.sirschrolli.kingdomsatwar.worldgen.lair;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.QuartPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.densityfunction.SamplerContext;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import org.jspecify.annotations.Nullable;

/**
 * Findet für jede der sechs Drachenarten den einen Ort ihres Nests in einer Welt.
 *
 * <p>Vorgehen (rein aus dem Startwert der Welt, also immer gleich): Je Art gibt es eine Grundrichtung und einen
 * Entfernungsring um den Weltursprung. Darin wird spiralförmig nach dem passenden Biom gesucht – und zwar so, dass
 * das ganze Nest (±48 Blöcke und etwas Rand, 49 Messpunkte) im Biom liegt und alle Nester mindestens {@link #MIN_SPACING} Blöcke
 * voneinander entfernt sind. Findet sich nichts, wird der Ring erweitert; als letzte Möglichkeit liegt das Nest in der
 * Grundrichtung (das Nest gestaltet seine Umgebung selbst).
 *
 * <p>Das Biom an der Oberfläche wird über den Klima-Parameter „Tiefe“ bestimmt: Er ist an der Erdoberfläche 0.
 */
public final class NestLocator {
    public static final int MIN_SPACING = 640;
    private static final int AREA = 48;
    private static final Map<ChunkGeneratorStructureState, Map<DragonType, ChunkPos>> CACHE = Collections.synchronizedMap(new WeakHashMap<>());
    private static @Nullable Field biomeSourceField;
    /** Zuletzt benutzte Welt – erspart bei den vielen Abfragen der Weltgenerierung die Sperre der Zwischenablage. */
    private static volatile @Nullable Cached last;

    private record Cached(ChunkGeneratorStructureState state, Map<DragonType, ChunkPos> map) {}

    private NestLocator() {}

    /** Chunk des Nests dieser Art (oder null, wenn die Welt keine Nester hat, z. B. eine flache Welt). */
    public static @Nullable ChunkPos find(ChunkGeneratorStructureState state, DragonType element) {
        Cached cached = last;
        if (cached != null && cached.state() == state) {
            return cached.map().get(element);
        }
        Map<DragonType, ChunkPos> map = CACHE.get(state);
        if (map == null) {
            synchronized (CACHE) {
                map = CACHE.get(state);
                if (map == null) {
                    map = computeAll(state);
                    CACHE.put(state, map);
                }
            }
        }
        last = new Cached(state, map);
        return map.get(element);
    }

    /** Alle Nest-Chunks einer Welt (für Drachenkompass, Karten und Story). */
    public static Map<DragonType, ChunkPos> all(ServerLevel level) {
        ChunkGeneratorStructureState state = level.getChunkSource().getGeneratorState();
        Map<DragonType, ChunkPos> result = new EnumMap<>(DragonType.class);
        for (DragonType type : DragonType.values()) {
            ChunkPos pos = find(state, type);
            if (pos != null) {
                result.put(type, pos);
            }
        }
        return result;
    }

    private static Map<DragonType, ChunkPos> computeAll(ChunkGeneratorStructureState state) {
        Map<DragonType, UniqueNestPlacement> placements = new EnumMap<>(DragonType.class);
        for (Holder<StructureSet> set : state.possibleStructureSets()) {
            if (set.value().placement() instanceof UniqueNestPlacement p) {
                placements.put(p.element(), p);
            }
        }
        Map<DragonType, ChunkPos> result = new EnumMap<>(DragonType.class);
        if (placements.isEmpty()) {
            return result;
        }
        BiomeSource source = biomeSource(state);
        if (source == null) {
            return result;
        }
        long started = System.currentTimeMillis();
        Climate.Sampler sampler = state.randomState().createClimateSampler(SamplerContext.EMPTY_UNCACHED);
        BiomeResolver resolver = source.createResolver(sampler);
        ChunkPos origin = state.getDimensionOrigin();
        int ox = origin.getMiddleBlockX();
        int oz = origin.getMiddleBlockZ();
        RandomSource random = RandomSource.create(state.getLevelSeed() ^ 0x5EA1D5A1L);
        double spin = random.nextDouble() * Mth.TWO_PI;
        // feste Reihenfolge: zuerst die nahen Nester
        DragonType[] order = {DragonType.NATURE, DragonType.WATER, DragonType.EARTH, DragonType.ICE, DragonType.LIGHTNING, DragonType.FIRE};
        int index = 0;
        for (DragonType type : order) {
            UniqueNestPlacement p = placements.get(type);
            if (p == null) {
                continue;
            }
            double base = spin + index * Mth.TWO_PI / order.length + (random.nextDouble() - 0.5) * 0.6;
            index++;
            int[] found = search(resolver, sampler, p, ox, oz, base, result, 1);
            if (found == null) {
                found = search(resolver, sampler, p, ox, oz, base, result, 2);
            }
            if (found == null) {
                int r = p.minDistance();
                found = new int[] {ox + (int) (Math.cos(base) * r), oz + (int) (Math.sin(base) * r)};
                KingdomsAtWar.LOGGER.info("Drachennest {}: kein passendes Biom gefunden, liegt bei {} {}", type.id(), found[0], found[1]);
            }
            result.put(type, new ChunkPos(found[0] >> 4, found[1] >> 4));
        }
        KingdomsAtWar.LOGGER.info("Drachennester berechnet in {} ms: {}", System.currentTimeMillis() - started, result);
        return result;
    }

    private static int @Nullable [] search(BiomeResolver resolver, Climate.Sampler sampler, UniqueNestPlacement p, int ox, int oz,
            double base, Map<DragonType, ChunkPos> taken, int pass) {
        int maxDistance = pass == 1 ? p.maxDistance() : p.maxDistance() * 2 + 1000;
        int minDistance = pass == 1 ? p.minDistance() : Math.max(300, p.minDistance() / 2);
        for (int r = minDistance; r <= maxDistance; r += 64) {
            int steps = Math.max(16, (int) (Mth.TWO_PI * r / 96));
            for (int k = 0; k < steps; k++) {
                int idx = (k % 2 == 0) ? k / 2 : -(k + 1) / 2;
                double a = base + idx * Mth.TWO_PI / steps;
                int x = ox + (int) Math.round(Math.cos(a) * r);
                int z = oz + (int) Math.round(Math.sin(a) * r);
                if (!farFromOthers(x, z, taken)) {
                    continue;
                }
                if (fitsArea(resolver, sampler, p.biomes(), p.inland(), x, z, area(p.element()))) {
                    return new int[] {x, z};
                }
            }
        }
        return null;
    }

    /** So weit muss das passende Biom reichen: Gipfel sind schmal, dort genügt der Kern (das Nest formt den Rest). */
    private static int area(DragonType type) {
        return type == DragonType.LIGHTNING ? 24 : AREA;
    }

    private static boolean farFromOthers(int x, int z, Map<DragonType, ChunkPos> taken) {
        for (ChunkPos other : taken.values()) {
            double dx = other.getMiddleBlockX() - x;
            double dz = other.getMiddleBlockZ() - z;
            if (dx * dx + dz * dz < (double) MIN_SPACING * MIN_SPACING) {
                return false;
            }
        }
        return true;
    }

    /**
     * Liegt die ganze Fläche (Radius {@code area}) im passenden Biom? Geprüft werden die Mitte und 48 Punkte auf vier
     * Ringen bis etwas über den Rand hinaus – so fallen auch schmale Flüsse oder Biomzipfel auf, die das Bauwerk
     * durchschneiden würden.
     */
    public static boolean fitsArea(BiomeResolver resolver, Climate.Sampler sampler, HolderSet<Biome> biomes, boolean inland, int x, int z, int area) {
        if (!matches(resolver, sampler, biomes, inland, x, z)) {
            return false;
        }
        // Messringe: Radius und Zahl der Punkte (Abstand der Punkte höchstens ~24 Blöcke)
        int[][] rings = {{area / 3, 6}, {area * 2 / 3, 10}, {area, 14}, {area + 14, 18}};
        for (int[] ring : rings) {
            double r = ring[0];
            for (int k = 0; k < ring[1]; k++) {
                double a = (k + 0.5 * (r / 16 % 2)) * Mth.TWO_PI / ring[1];
                if (!matches(resolver, sampler, biomes, inland, x + (int) Math.round(Math.cos(a) * r), z + (int) Math.round(Math.sin(a) * r))) {
                    return false;
                }
            }
        }
        return true;
    }

    /** Landesinneres: Kontinentalität mindestens „mittleres Inland“ (Küsten und Flachland am Meer fallen weg). */
    private static final long INLAND = Climate.quantizeCoord(0.03f);

    private static boolean matches(BiomeResolver resolver, Climate.Sampler sampler, HolderSet<Biome> biomes, boolean inland, int x, int z) {
        if (inland && sampler.sample(QuartPos.fromBlock(x), 0, QuartPos.fromBlock(z)).continentalness() < INLAND) {
            return false;
        }
        return matches(resolver, sampler, biomes, x, z);
    }

    private static boolean matches(BiomeResolver resolver, Climate.Sampler sampler, HolderSet<Biome> biomes, int x, int z) {
        return biomes.contains(surfaceBiome(resolver, sampler, x, z));
    }

    /** Biom an der Erdoberfläche: dort, wo der Klima-Parameter „Tiefe“ von negativ (Luft) zu positiv (Boden) wechselt. */
    public static Holder<Biome> surfaceBiome(BiomeResolver resolver, Climate.Sampler sampler, int x, int z) {
        int qx = QuartPos.fromBlock(x);
        int qz = QuartPos.fromBlock(z);
        int lo = QuartPos.fromBlock(-64);
        int hi = QuartPos.fromBlock(316);
        // Tiefe nimmt nach oben ab: suche die höchste Lage mit Tiefe >= 0
        while (hi - lo > 1) {
            int mid = (lo + hi) >>> 1;
            if (sampler.sample(qx, mid, qz).depth() >= 0) {
                lo = mid;
            } else {
                hi = mid;
            }
        }
        return resolver.getNoiseBiome(qx, lo, qz);
    }

    public static @Nullable BiomeSource biomeSource(ChunkGeneratorStructureState state) {
        try {
            if (biomeSourceField == null) {
                Field f = ChunkGeneratorStructureState.class.getDeclaredField("biomeSource");
                f.setAccessible(true);
                biomeSourceField = f;
            }
            return (BiomeSource) biomeSourceField.get(state);
        } catch (ReflectiveOperationException | RuntimeException e) {
            KingdomsAtWar.LOGGER.error("Drachennester: Biomquelle nicht lesbar", e);
            return null;
        }
    }
}
