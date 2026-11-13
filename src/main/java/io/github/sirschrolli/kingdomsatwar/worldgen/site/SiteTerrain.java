package io.github.sirschrolli.kingdomsatwar.worldgen.site;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;

/**
 * Das Gelände, wie es der Generator vor allen Bäumen und Bauten formt – beim Planen eines Bauwerks abgefragt (mit
 * Zwischenspeicher), damit jeder Chunk dasselbe Bauwerk baut.
 */
public final class SiteTerrain {
    private final Structure.GenerationContext context;
    private final Long2ObjectOpenHashMap<int[]> cache = new Long2ObjectOpenHashMap<>();
    private final Long2ObjectOpenHashMap<NoiseColumn> columns = new Long2ObjectOpenHashMap<>();

    public SiteTerrain(Structure.GenerationContext context) {
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
    public int ground(int x, int z) {
        return this.column(x, z)[0] - 1;
    }

    /** Erste freie Höhe (über einem Gewässer: die Wasseroberfläche + 1). */
    public int surface(int x, int z) {
        return this.column(x, z)[1];
    }

    public boolean water(int x, int z) {
        int[] c = this.column(x, z);
        return c[1] > c[0];
    }

    /** Wassertiefe (0 = trocken). */
    public int waterDepth(int x, int z) {
        int[] c = this.column(x, z);
        return Math.max(0, c[1] - c[0]);
    }

    public int seaLevel() {
        return this.context.chunkGenerator().getSeaLevel();
    }

    public int minY() {
        return this.context.heightAccessor().getMinY();
    }

    /** Das Biom an einer Stelle (an der Oberfläche bzw. in der angegebenen Höhe). */
    public Holder<Biome> biome(int x, int y, int z) {
        return this.context.biomeResolver().getNoiseBiome(QuartPos.fromBlock(x), QuartPos.fromBlock(y), QuartPos.fromBlock(z));
    }

    /** Liegt die Stelle in einem Biom, in dem das Bauwerk stehen darf? */
    public boolean valid(int x, int y, int z) {
        return this.context.validBiome().test(this.biome(x, y, z));
    }

    /**
     * Die ganze Säule des Geländes an einer Stelle, wie der Generator sie formt (Gestein, Flüssigkeit, Luft – noch ohne
     * Oberfläche, Höhlen und Bewuchs). In der Unterwelt gibt es keine Oberfläche, die man mit Höhenkarten fassen könnte:
     * Dort fragt man so ab, wo Fels ist und wo Hohlraum.
     */
    public NoiseColumn column3d(int x, int z) {
        long key = BlockPos.asLong(x, 0, z);
        NoiseColumn c = this.columns.get(key);
        if (c == null) {
            c = this.context.chunkGenerator().getBaseColumn(x, z, this.context.heightAccessor(), this.context.randomState());
            this.columns.put(key, c);
        }
        return c;
    }

    /** Fester Fels an der Stelle (weder Luft noch Flüssigkeit)? */
    public boolean rock(int x, int y, int z) {
        BlockState state = this.column3d(x, z).getBlock(y);
        return !state.isAir() && state.getFluidState().isEmpty();
    }

    /** Lava (oder eine andere Flüssigkeit) an der Stelle? */
    public boolean fluid(int x, int y, int z) {
        return !this.column3d(x, z).getBlock(y).getFluidState().isEmpty();
    }

    /** Kleinste und größte Bodenhöhe auf einem Quadrat (Kantenlänge 2r + 1, alle {@code step} Blöcke). */
    public int[] span(int cx, int cz, int r, int step) {
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        for (int dx = -r; dx <= r; dx += step) {
            for (int dz = -r; dz <= r; dz += step) {
                int g = this.ground(cx + dx, cz + dz);
                min = Math.min(min, g);
                max = Math.max(max, g);
            }
        }
        return new int[] {min, max};
    }
}
