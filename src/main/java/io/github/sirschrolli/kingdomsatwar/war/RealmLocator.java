package io.github.sirschrolli.kingdomsatwar.war;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import io.github.sirschrolli.kingdomsatwar.worldgen.lair.NestLocator;
import io.github.sirschrolli.kingdomsatwar.worldgen.realm.RealmCastlePlacement;
import io.github.sirschrolli.kingdomsatwar.worldgen.seal.SealCircleLocator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
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
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.densityfunction.SamplerContext;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import org.jspecify.annotations.Nullable;

/**
 * Findet die Stammburgen der vier Reiche (rein aus dem Startwert, also immer gleich): je eine in einer Himmelsrichtung
 * (die Richtungen sind je Welt gedreht), in einem Ring um den Weltursprung, auf ebenem, offenem Land, weit weg von den
 * Drachennestern, vom Siegelkreis und voneinander. Die Geländehöhe wird wie beim Siegelkreis schon vor der Erzeugung
 * über die „vorläufige Oberfläche“ geschätzt.
 */
public final class RealmLocator {
    /** Radius der Fläche, die im Biom liegen muss (die Burg misst gut 70 × 70 Blöcke). */
    private static final int AREA = 44;
    private static final int NEST_CLEARANCE = 450;
    private static final int SEAL_CLEARANCE = 450;
    private static final int CASTLE_CLEARANCE = 800;
    private static final int MAX_RELIEF = 10;
    private static final int RING_CLEARANCE = 160;
    private static final Map<ChunkGeneratorStructureState, Map<Realm, ChunkPos>> CACHE = Collections.synchronizedMap(new WeakHashMap<>());
    /** Dieselben Orte nach dem Rauschzustand der Welt – so findet das Bauwerk sein Reich (es kennt den Zustand nicht). */
    private static final Map<RandomState, Map<Realm, ChunkPos>> BY_NOISE = Collections.synchronizedMap(new WeakHashMap<>());

    private RealmLocator() {}

    public static Map<Realm, ChunkPos> all(ServerLevel level) {
        return find(level.getServer().overworld().getChunkSource().getGeneratorState());
    }

    public static @Nullable ChunkPos find(ServerLevel level, Realm realm) {
        return all(level).get(realm);
    }

    /** Ungefähre Mitte einer Burg (Mitte ihres Chunks, Höhe unbekannt: Meereshöhe). */
    public static @Nullable BlockPos approximateCenter(ServerLevel level, Realm realm) {
        ChunkPos chunk = find(level, realm);
        return chunk == null ? null : new BlockPos(chunk.getMiddleBlockX(), level.getServer().overworld().getSeaLevel(), chunk.getMiddleBlockZ());
    }

    /** Das Reich, dessen Burg in diesem Chunk steht (oder {@code null}). */
    public static @Nullable Realm at(ChunkGeneratorStructureState state, int chunkX, int chunkZ) {
        for (Map.Entry<Realm, ChunkPos> entry : find(state).entrySet()) {
            if (entry.getValue().x() == chunkX && entry.getValue().z() == chunkZ) {
                return entry.getKey();
            }
        }
        return null;
    }

    /** Liegt der Punkt näher als {@code distance} Blöcke an einer Stammburg? (für andere Bauwerke und Dörfer) */
    public static boolean near(ChunkGeneratorStructureState state, int x, int z, int distance) {
        for (ChunkPos castle : find(state).values()) {
            double dx = castle.getMiddleBlockX() - x;
            double dz = castle.getMiddleBlockZ() - z;
            if (dx * dx + dz * dz < (double) distance * distance) {
                return true;
            }
        }
        return false;
    }

    /**
     * Das Reich, dessen Burg in diesem Chunk steht – nach dem Rauschzustand der Welt (für das Bauwerk selbst; die
     * Platzierung hat die Orte dann schon berechnet).
     */
    public static @Nullable Realm at(RandomState randomState, int chunkX, int chunkZ) {
        Map<Realm, ChunkPos> found = BY_NOISE.get(randomState);
        if (found == null) {
            return null;
        }
        for (Map.Entry<Realm, ChunkPos> entry : found.entrySet()) {
            if (entry.getValue().x() == chunkX && entry.getValue().z() == chunkZ) {
                return entry.getKey();
            }
        }
        return null;
    }

    public static Map<Realm, ChunkPos> find(ChunkGeneratorStructureState state) {
        Map<Realm, ChunkPos> found = CACHE.get(state);
        if (found == null) {
            synchronized (CACHE) {
                found = CACHE.get(state);
                if (found == null) {
                    found = compute(state);
                    CACHE.put(state, found);
                    BY_NOISE.put(state.randomState(), found);
                }
            }
        }
        return found;
    }

    private static Map<Realm, ChunkPos> compute(ChunkGeneratorStructureState state) {
        Map<Realm, ChunkPos> result = new EnumMap<>(Realm.class);
        RealmCastlePlacement placement = null;
        for (Holder<StructureSet> set : state.possibleStructureSets()) {
            if (set.value().placement() instanceof RealmCastlePlacement p) {
                placement = p;
            }
        }
        BiomeSource source = NestLocator.biomeSource(state);
        if (placement == null || source == null) {
            return result;
        }
        long started = System.currentTimeMillis();
        Climate.Sampler sampler = state.randomState().createClimateSampler(SamplerContext.EMPTY_UNCACHED);
        BiomeResolver resolver = source.createResolver(sampler);
        List<int[]> avoid = new ArrayList<>();
        for (DragonType type : DragonType.values()) {
            ChunkPos nest = NestLocator.find(state, type);
            if (nest != null) {
                avoid.add(new int[] {nest.getMiddleBlockX(), nest.getMiddleBlockZ(), NEST_CLEARANCE});
            }
        }
        ChunkPos seal = SealCircleLocator.find(state);
        if (seal != null) {
            avoid.add(new int[] {seal.getMiddleBlockX(), seal.getMiddleBlockZ(), SEAL_CLEARANCE});
        }
        // Bauwerke in Ringen um den Ursprung (z. B. der Sternensturz) haben keine Sperrzone: ihnen weicht die Burg aus
        for (Holder<StructureSet> set : state.possibleStructureSets()) {
            if (set.value().placement() instanceof net.minecraft.world.level.levelgen.structure.placement.ConcentricRingsStructurePlacement rings) {
                List<ChunkPos> positions = state.getRingPositionsFor(rings);
                if (positions != null) {
                    for (ChunkPos pos : positions) {
                        avoid.add(new int[] {pos.getMiddleBlockX(), pos.getMiddleBlockZ(), RING_CLEARANCE});
                    }
                }
            }
        }
        ChunkPos origin = state.getDimensionOrigin();
        int ox = origin.getMiddleBlockX();
        int oz = origin.getMiddleBlockZ();
        RandomSource random = RandomSource.create(state.getLevelSeed() ^ 0x7EA1C0DEL ^ placement.salt());
        double base = random.nextDouble() * Mth.TWO_PI;
        for (Realm realm : Realm.values()) {
            double direction = base + realm.ordinal() * Mth.HALF_PI;
            int[] found = search(state.randomState(), resolver, sampler, placement, avoid, ox, oz, direction, true);
            if (found == null) {
                found = search(state.randomState(), resolver, sampler, placement, avoid, ox, oz, direction, false);
            }
            if (found == null) {
                int r = placement.minDistance();
                found = new int[] {ox + (int) (Math.cos(direction) * r), oz + (int) (Math.sin(direction) * r)};
                KingdomsAtWar.LOGGER.info("Reich {}: kein passender Ort gefunden, die Burg liegt bei {} {}", realm.id(), found[0], found[1]);
            }
            avoid.add(new int[] {found[0], found[1], CASTLE_CLEARANCE});
            result.put(realm, new ChunkPos(found[0] >> 4, found[1] >> 4));
        }
        KingdomsAtWar.LOGGER.info("Burgen der Reiche berechnet in {} ms: {}", System.currentTimeMillis() - started, result);
        return result;
    }

    private static int @Nullable [] search(RandomState randomState, BiomeResolver resolver, Climate.Sampler sampler, RealmCastlePlacement p,
            List<int[]> avoid, int ox, int oz, double direction, boolean strict) {
        int maxDistance = strict ? p.maxDistance() : p.maxDistance() * 2;
        double spread = Math.toRadians(strict ? 38.0 : 44.0);
        for (int r = p.minDistance(); r <= maxDistance; r += 48) {
            int steps = Math.max(6, (int) (2.0 * spread * r / 56.0));
            for (int k = 0; k <= steps; k++) {
                int idx = (k % 2 == 0) ? k / 2 : -(k + 1) / 2;
                double a = direction + idx * (2.0 * spread / steps);
                int x = ox + (int) Math.round(Math.cos(a) * r);
                int z = oz + (int) Math.round(Math.sin(a) * r);
                if (blocked(avoid, x, z)) {
                    continue;
                }
                if (!NestLocator.fitsArea(resolver, sampler, p.biomes(), true, x, z, AREA)) {
                    continue;
                }
                if (SealCircleLocator.relief(randomState, x, z) <= (strict ? MAX_RELIEF : MAX_RELIEF * 2)) {
                    return new int[] {x, z};
                }
            }
        }
        return null;
    }

    private static boolean blocked(List<int[]> avoid, int x, int z) {
        for (int[] a : avoid) {
            double dx = a[0] - x;
            double dz = a[1] - z;
            if (dx * dx + dz * dz < (double) a[2] * a[2]) {
                return true;
            }
        }
        return false;
    }
}
