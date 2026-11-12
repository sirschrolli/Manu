package io.github.sirschrolli.kingdomsatwar.worldgen.shepherd;

import com.mojang.serialization.MapCodec;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import java.util.Arrays;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

/**
 * Der Seelenpferch (seit 0.98): im Seelensumpf der Schattenwelt ein runder Pferch auf aufgeschüttetem Grund, umgeben von
 * einem Zaun mit Seelenlaternen, in der Mitte der Glockenstuhl mit der Seelenglocke – dort steht der Seelenhirte in Andacht.
 * Er entsteht nur, wo der Sumpf eben genug ist und nicht zu tief unter Wasser liegt; dafür sucht er rund um den Chunk der
 * Verteilung bis {@link #SEARCH} Blöcke weit. Fern von Nyxars Zitadelle; die Dörfer der Umbrer meidet die Verteilung. Die
 * Form baut {@link SoulPenPiece}.
 */
public class SoulPenStructure extends Structure {
    public static final MapCodec<SoulPenStructure> CODEC = simpleCodec(SoulPenStructure::new);
    /** So uneben darf der Sumpf unter dem Pferch höchstens sein. */
    public static final int MAX_ROUGHNESS = 9;
    /** Abstand der Messpunkte (5 × 5 Punkte, also gut der ganze Pferch). */
    public static final int SPREAD = 7;

    public SoulPenStructure(Structure.StructureSettings settings) {
        super(settings);
    }

    @Override
    protected Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext context) {
        ChunkPos chunk = context.chunkPos();
        if (io.github.sirschrolli.kingdomsatwar.worldgen.umbrer.UmbrerVillageStructure.nearCitadelArea(context, chunk, 220)) {
            return Optional.empty();
        }
        long seed = context.random().nextLong();
        BlockPos center = find(context.chunkGenerator(), context.heightAccessor(), context.randomState(), context.biomeResolver(),
                context.validBiome(), chunk, seed);
        if (center == null) {
            return Optional.empty();
        }
        int sea = context.chunkGenerator().getSeaLevel();
        KingdomsAtWar.LOGGER.debug("Seelenpferch bei {} {} {}", center.getX(), center.getY(), center.getZ());
        return Optional.of(new Structure.GenerationStub(center, builder -> builder.addPiece(new SoulPenPiece(center, seed, sea))));
    }

    /** So weit vom Chunk der Verteilung sucht er nach einem Platz im Sumpf (erst die Mitte, dann zwei Ringe). */
    public static final int SEARCH = 96;
    private static final int[][] RING = {{1, 0}, {1, 1}, {0, 1}, {-1, 1}, {-1, 0}, {-1, -1}, {0, -1}, {1, -1}};

    /**
     * Sucht rund um den Chunk der Verteilung einen Platz für einen Pferch: die Mitte des Chunks, dann acht Orte im Abstand von
     * {@link #SEARCH} / 2 und acht im Abstand von {@link #SEARCH} (die Reihenfolge je nach Zufall gedreht). Gibt die Mitte des
     * Pferchs zurück (oder null). Auch für Tests (ohne Welt).
     */
    public static @org.jspecify.annotations.Nullable BlockPos find(net.minecraft.world.level.chunk.ChunkGenerator generator,
            net.minecraft.world.level.LevelHeightAccessor height, net.minecraft.world.level.levelgen.RandomState random,
            net.minecraft.world.level.biome.BiomeResolver biomes, java.util.function.Predicate<Holder<Biome>> valid, ChunkPos chunk, long seed) {
        int turn = Math.floorMod((int) (seed >>> 13), 8);
        for (int i = 0; i < 17; i++) {
            int x = chunk.getMiddleBlockX();
            int z = chunk.getMiddleBlockZ();
            if (i > 0) {
                int[] dir = RING[(i - 1 + turn) % 8];
                int dist = i <= 8 ? SEARCH / 2 : SEARCH;
                double k = dir[0] != 0 && dir[1] != 0 ? Math.sqrt(0.5) : 1.0;
                x += (int) Math.round(dir[0] * dist * k);
                z += (int) Math.round(dir[1] * dist * k);
            }
            BlockPos site = site(generator, height, random, biomes, valid, x, z);
            if (site != null) {
                return site;
            }
        }
        return null;
    }

    /**
     * Passt ein Pferch mit der Mitte (x, z)? Der Sumpf muss eben genug sein und darf nicht zu tief unter Wasser oder zu hoch
     * liegen, das Biom muss passen. Gibt die Mitte auf Höhe des Bodens zurück (oder null). Auch für Tests (ohne Welt).
     */
    public static @org.jspecify.annotations.Nullable BlockPos site(net.minecraft.world.level.chunk.ChunkGenerator generator,
            net.minecraft.world.level.LevelHeightAccessor height, net.minecraft.world.level.levelgen.RandomState random,
            net.minecraft.world.level.biome.BiomeResolver biomes, java.util.function.Predicate<Holder<Biome>> valid, int x, int z) {
        int sea = generator.getSeaLevel();
        // erst das Biom (billig), dann das Gelände
        int middle = generator.getBaseHeight(x, z, Heightmap.Types.OCEAN_FLOOR_WG, height, random);
        if (!valid.test(biomes.getNoiseBiome(QuartPos.fromBlock(x), QuartPos.fromBlock(floor(middle, sea)), QuartPos.fromBlock(z)))) {
            return null;
        }
        int[] heights = new int[25];
        int i = 0;
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                heights[i++] = generator.getBaseHeight(x + dx * SPREAD, z + dz * SPREAD, Heightmap.Types.OCEAN_FLOOR_WG, height, random);
            }
        }
        int[] sorted = heights.clone();
        Arrays.sort(sorted);
        int ground = sorted[sorted.length / 2];
        if (sorted[sorted.length - 3] - sorted[2] > MAX_ROUGHNESS || ground < sea - 6 || ground > sea + 14) {
            return null;
        }
        int floor = floor(ground, sea);
        Holder<Biome> biome = biomes.getNoiseBiome(QuartPos.fromBlock(x), QuartPos.fromBlock(floor), QuartPos.fromBlock(z));
        return valid.test(biome) ? new BlockPos(x, floor, z) : null;
    }

    /** Die Höhe, auf der man im Pferch steht: auf dem Sumpf, aber immer über dem Wasser. */
    public static int floor(int ground, int sea) {
        return Math.max(ground, sea + 1);
    }

    @Override
    public StructureType<?> type() {
        return io.github.sirschrolli.kingdomsatwar.worldgen.lair.ModStructures.SOUL_PEN.get();
    }
}
