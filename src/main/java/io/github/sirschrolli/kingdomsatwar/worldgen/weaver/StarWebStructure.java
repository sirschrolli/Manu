package io.github.sirschrolli.kingdomsatwar.worldgen.weaver;

import com.mojang.serialization.MapCodec;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.worldgen.lair.ModStructures;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import org.jspecify.annotations.Nullable;

/**
 * Das Sternennetz (seit 0.99): frei schwebend in den leeren Weiten der Leere (Sternennichts), wo zwischen den Archipelen
 * nichts ist – die Netzplatte mit dem Gelege der Sternenweberin, ringsum Ankerfelsen, darüber die Sternenkrone. Es entsteht
 * nur, wo um die Mitte genug Raum ist: kein Bruchstück darf in das Netz ragen (gesucht auf einigen Höhen, rund um den Chunk
 * der Verteilung bis {@link #SEARCH} Blöcke weit). Die Form baut {@link StarWebPiece}.
 */
public class StarWebStructure extends Structure {
    public static final MapCodec<StarWebStructure> CODEC = simpleCodec(StarWebStructure::new);
    /** Diese Höhen der Netzplatte werden der Reihe nach versucht. */
    public static final int[] HEIGHTS = {118, 108, 128, 98, 138};
    /** So weit vom Chunk der Verteilung sucht es nach einem freien Raum (erst die Mitte, dann acht Orte im Kreis). */
    public static final int SEARCH = 56;
    private static final int[][] RING = {{1, 0}, {1, 1}, {0, 1}, {-1, 1}, {-1, 0}, {-1, -1}, {0, -1}, {1, -1}};

    public StarWebStructure(Structure.StructureSettings settings) {
        super(settings);
    }

    @Override
    protected Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext context) {
        ChunkPos chunk = context.chunkPos();
        long seed = context.random().nextLong();
        BlockPos center = find(context.chunkGenerator(), context.heightAccessor(), context.randomState(), context.biomeResolver(),
                context.validBiome(), chunk, seed);
        if (center == null) {
            return Optional.empty();
        }
        KingdomsAtWar.LOGGER.debug("Sternennetz bei {} {} {}", center.getX(), center.getY(), center.getZ());
        return Optional.of(new Structure.GenerationStub(center, builder -> builder.addPiece(new StarWebPiece(center, seed))));
    }

    /** Sucht rund um den Chunk einen freien Raum für das Netz (die Mitte, dann acht Orte im Abstand {@link #SEARCH}). Auch für Tests. */
    public static @Nullable BlockPos find(ChunkGenerator generator, LevelHeightAccessor height, RandomState random, BiomeResolver biomes,
            Predicate<Holder<Biome>> valid, ChunkPos chunk, long seed) {
        int turn = Math.floorMod((int) (seed >>> 17), 8);
        for (int i = 0; i < 9; i++) {
            int x = chunk.getMiddleBlockX();
            int z = chunk.getMiddleBlockZ();
            if (i > 0) {
                int[] dir = RING[(i - 1 + turn) % 8];
                double k = dir[0] != 0 && dir[1] != 0 ? Math.sqrt(0.5) : 1.0;
                x += (int) Math.round(dir[0] * SEARCH * k);
                z += (int) Math.round(dir[1] * SEARCH * k);
            }
            BlockPos site = site(generator, height, random, biomes, valid, x, z);
            if (site != null) {
                return site;
            }
        }
        return null;
    }

    /**
     * Passt das Netz mit der Mitte (x, z)? Das Biom muss passen, und auf einer der Höhen ({@link #HEIGHTS}) darf kein
     * Bruchstück in seinen Raum ragen (geprüft an 17 Säulen: oben liegt nichts höher als knapp unter den tiefsten Ankerfelsen,
     * es sei denn, weit darüber).
     * Gibt die Mitte der Netzplatte zurück (oder null). Auch für Tests (ohne Welt).
     */
    public static @Nullable BlockPos site(ChunkGenerator generator, LevelHeightAccessor height, RandomState random, BiomeResolver biomes,
            Predicate<Holder<Biome>> valid, int x, int z) {
        int[] tops = new int[17];
        int n = 0;
        tops[n++] = generator.getBaseHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG, height, random);
        for (int ring = 0; ring < 2; ring++) {
            double r = ring == 0 ? 18.0 : StarWebPiece.REACH - 3.0;
            for (int k = 0; k < 8; k++) {
                double a = k * Math.PI / 4.0 + ring * Math.PI / 8.0;
                tops[n++] = generator.getBaseHeight(x + (int) Math.round(Math.cos(a) * r), z + (int) Math.round(Math.sin(a) * r),
                        Heightmap.Types.WORLD_SURFACE_WG, height, random);
            }
        }
        for (int f : HEIGHTS) {
            if (!valid.test(biomes.getNoiseBiome(QuartPos.fromBlock(x), QuartPos.fromBlock(f), QuartPos.fromBlock(z)))) {
                continue;
            }
            boolean free = true;
            for (int top : tops) {
                // ein Bruchstück reicht in den Raum (hoch darüber treibende, flache Splitter stören nicht)
                if (top > f - StarWebPiece.BELOW - 2 && top < f + 70) {
                    free = false;
                    break;
                }
            }
            if (free) {
                return new BlockPos(x, f, z);
            }
        }
        return null;
    }

    @Override
    public StructureType<?> type() {
        return ModStructures.STAR_WEB.get();
    }
}
