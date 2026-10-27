package io.github.sirschrolli.kingdomsatwar.worldgen.village;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;

/**
 * Das Gelände, wie es der Generator vor allen Bäumen und Bauten formt – Höhen und Biome werden beim Planen des Dorfs
 * abgefragt (mit Zwischenspeicher), damit alle Teile in jedem Chunk gleich gebaut werden.
 */
final class VillageTerrain {
    private final Structure.GenerationContext context;
    private final Long2ObjectOpenHashMap<int[]> cache = new Long2ObjectOpenHashMap<>();
    private final it.unimi.dsi.fastutil.longs.Long2BooleanOpenHashMap biomes = new it.unimi.dsi.fastutil.longs.Long2BooleanOpenHashMap();

    VillageTerrain(Structure.GenerationContext context) {
        this.context = context;
    }

    private int[] column(int x, int z) {
        long key = BlockPos.asLong(x, 0, z);
        int[] c = this.cache.get(key);
        if (c == null) {
            c = new int[] {
                    this.context.chunkGenerator().getBaseHeight(x, z, Heightmap.Types.OCEAN_FLOOR_WG, this.context.heightAccessor(),
                            this.context.randomState()),
                    this.context.chunkGenerator().getBaseHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG, this.context.heightAccessor(),
                            this.context.randomState())};
            this.cache.put(key, c);
        }
        return c;
    }

    /** Oberster fester Block (y). */
    int ground(int x, int z) {
        return this.column(x, z)[0] - 1;
    }

    /** Erste freie Höhe (über Wasser die Wasseroberfläche + 1). */
    int surface(int x, int z) {
        return this.column(x, z)[1];
    }

    boolean water(int x, int z) {
        int[] c = this.column(x, z);
        return c[1] > c[0];
    }

    /** Wassertiefe (0 = trocken). */
    int waterDepth(int x, int z) {
        int[] c = this.column(x, z);
        return Math.max(0, c[1] - c[0]);
    }

    int seaLevel() {
        return this.context.chunkGenerator().getSeaLevel();
    }

    /** 1: Biom des Dorfs, 2: Fluss (nur für Brücken), 0: fremdes Land. */
    private int biomeClass(int x, int z) {
        long key = BlockPos.asLong(x >> 2, 0, z >> 2);
        if (!this.biomes.containsKey(key)) {
            if (this.rivers.contains(key)) {
                return 2;
            }
            int y = Math.max(this.ground(x, z), this.surface(x, z) - 1) + 1;
            Holder<Biome> biome = this.context.biomeResolver().getNoiseBiome(QuartPos.fromBlock(x), QuartPos.fromBlock(y), QuartPos.fromBlock(z));
            boolean own = this.context.validBiome().test(biome);
            this.biomes.put(key, own);
            if (!own && biome.is(net.minecraft.tags.BiomeTags.IS_RIVER)) {
                this.rivers.add(key);
                return 2;
            }
            return own ? 1 : 0;
        }
        if (this.rivers.contains(key)) {
            return 2;
        }
        return this.biomes.get(key) ? 1 : 0;
    }

    private final it.unimi.dsi.fastutil.longs.LongOpenHashSet rivers = new it.unimi.dsi.fastutil.longs.LongOpenHashSet();

    /** Darf hier ein Weg verlaufen (Biom des Dorfs oder ein Fluss, über den eine Brücke führt)? */
    boolean allowed(int x, int z) {
        return this.biomeClass(x, z) > 0;
    }

    /** Darf hier ein Gebäude stehen (nur im Biom des Dorfs)? */
    boolean buildable(int x, int z) {
        return this.biomeClass(x, z) == 1;
    }
}
