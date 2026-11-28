package io.github.sirschrolli.kingdomsatwar.abyss.hoard;

import com.mojang.serialization.MapCodec;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.worldgen.lair.ModStructures;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

/**
 * Der Schlund der Welten: ein schwebender Ring aus Chaosgestein hoch im Abgrund, unter dem der Weltenfresser versiegelt
 * liegt. Die Form baut {@link HoardPiece}; wo er schwebt, bestimmt die feste Ringplatzierung (wie beim Thron der Alten).
 */
public class HoardStructure extends Structure {
    public static final MapCodec<HoardStructure> CODEC = simpleCodec(HoardStructure::new);

    public HoardStructure(Structure.StructureSettings settings) {
        super(settings);
    }

    @Override
    protected Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext context) {
        ChunkPos chunk = context.chunkPos();
        BlockPos center = new BlockPos(chunk.getMiddleBlockX(), HoardPiece.FLOOR, chunk.getMiddleBlockZ());
        long seed = context.random().nextLong();
        KingdomsAtWar.LOGGER.info("Schlund der Welten bei {} {} {}", center.getX(), center.getY(), center.getZ());
        return Optional.of(new Structure.GenerationStub(center, builder -> builder.addPiece(new HoardPiece(center, seed))));
    }

    @Override
    public StructureType<?> type() {
        return ModStructures.WORLD_EATER_HOARD.get();
    }
}
