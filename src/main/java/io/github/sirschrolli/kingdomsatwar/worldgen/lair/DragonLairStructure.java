package io.github.sirschrolli.kingdomsatwar.worldgen.lair;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

/**
 * Das riesige Nest eines Drachen-Bosses (ca. 100 Blöcke breit). Die Form baut {@link DragonLairPiece} mit dem
 * Entwurf der jeweiligen Art ({@link LairDesign}); wo das Nest liegt, bestimmt {@link UniqueNestPlacement}.
 */
public class DragonLairStructure extends Structure {
    public static final MapCodec<DragonLairStructure> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            settingsCodec(instance),
            DragonType.CODEC.fieldOf("element").forGetter(s -> s.element)
    ).apply(instance, DragonLairStructure::new));

    private final DragonType element;

    public DragonLairStructure(Structure.StructureSettings settings, DragonType element) {
        super(settings);
        this.element = element;
    }

    @Override
    protected Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext context) {
        ChunkPos chunk = context.chunkPos();
        int x = chunk.getMiddleBlockX();
        int z = chunk.getMiddleBlockZ();
        LairDesign design = LairDesigns.of(this.element);
        int y = design.floorHeight(context, x, z);
        BlockPos center = new BlockPos(x, y, z);
        long seed = context.random().nextLong();
        return Optional.of(new Structure.GenerationStub(center, builder -> builder.addPiece(new DragonLairPiece(this.element, center, seed))));
    }

    @Override
    public StructureType<?> type() {
        return ModStructures.DRAGON_LAIR.get();
    }

    /** Gemittelte Geländehöhe um einen Punkt (für den Nestboden). */
    public static int averageHeight(Structure.GenerationContext context, int x, int z, int radius, Heightmap.Types type) {
        int sum = 0;
        int n = 0;
        for (int dx = -radius; dx <= radius; dx += radius / 2) {
            for (int dz = -radius; dz <= radius; dz += radius / 2) {
                sum += context.chunkGenerator().getBaseHeight(x + dx, z + dz, type, context.heightAccessor(), context.randomState());
                n++;
            }
        }
        return n == 0 ? 64 : Math.round((float) sum / n);
    }
}
