package io.github.sirschrolli.kingdomsatwar.worldgen.clock;

import com.mojang.serialization.MapCodec;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.worldgen.lair.ModStructures;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import org.jspecify.annotations.Nullable;

/**
 * Der Uhrturm (seit 1.01): frei schwebend im Abgrund, in einem leer gefegten Auge des Chaos – das große Zifferblatt mit dem
 * Uhrwerkherz, der Turm mit Uhrwerkkammer und Glockenstube, schwebende Zahnräder. Die Höhe des Zifferblatts hängt vom Zufall
 * des Bauwerks ab ({@link #HEIGHTS}); das Biom an der Mitte muss passen. Die Form baut {@link ClockTowerPiece}.
 */
public class ClockTowerStructure extends Structure {
    public static final MapCodec<ClockTowerStructure> CODEC = simpleCodec(ClockTowerStructure::new);
    /** Mögliche Höhen des Zifferblatts. */
    public static final int[] HEIGHTS = {104, 112, 120, 128};

    public ClockTowerStructure(Structure.StructureSettings settings) {
        super(settings);
    }

    @Override
    protected Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext context) {
        ChunkPos chunk = context.chunkPos();
        long seed = context.random().nextLong();
        BlockPos center = site(context.biomeResolver(), context.validBiome(), chunk, seed);
        if (center == null) {
            return Optional.empty();
        }
        KingdomsAtWar.LOGGER.debug("Uhrturm bei {} {} {}", center.getX(), center.getY(), center.getZ());
        return Optional.of(new Structure.GenerationStub(center, builder -> builder.addPiece(new ClockTowerPiece(center, seed))));
    }

    /** Die Mitte des Zifferblatts für diesen Chunk (oder null, wenn das Biom nicht passt). Auch für Tests (ohne Welt). */
    public static @Nullable BlockPos site(BiomeResolver biomes, Predicate<Holder<Biome>> valid, ChunkPos chunk, long seed) {
        int x = chunk.getMiddleBlockX();
        int z = chunk.getMiddleBlockZ();
        int y = HEIGHTS[(int) Math.floorMod(seed >>> 21, (long) HEIGHTS.length)];
        if (!valid.test(biomes.getNoiseBiome(QuartPos.fromBlock(x), QuartPos.fromBlock(y), QuartPos.fromBlock(z)))) {
            return null;
        }
        return new BlockPos(x, y, z);
    }

    @Override
    public StructureType<?> type() {
        return ModStructures.CLOCK_TOWER.get();
    }
}
