package io.github.sirschrolli.kingdomsatwar.worldgen.throne;

import com.mojang.serialization.MapCodec;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.worldgen.lair.ModStructures;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

/**
 * Der Thron der Alten: ein schwebender Platz aus Sternenmarmor hoch in der Leere, auf dem der Zerbrochene König über
 * seinem Thron wacht. Die Form baut {@link ThronePiece}; wo er schwebt, bestimmt {@link ThronePlacement}.
 */
public class ThroneStructure extends Structure {
    public static final MapCodec<ThroneStructure> CODEC = simpleCodec(ThroneStructure::new);

    public ThroneStructure(Structure.StructureSettings settings) {
        super(settings);
    }

    @Override
    protected Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext context) {
        ChunkPos chunk = context.chunkPos();
        BlockPos center = new BlockPos(chunk.getMiddleBlockX(), ThronePiece.FLOOR, chunk.getMiddleBlockZ());
        long seed = context.random().nextLong();
        KingdomsAtWar.LOGGER.info("Thron der Alten bei {} {} {}", center.getX(), center.getY(), center.getZ());
        return Optional.of(new Structure.GenerationStub(center, builder -> builder.addPiece(new ThronePiece(center, seed))));
    }

    @Override
    public StructureType<?> type() {
        return ModStructures.VOID_THRONE.get();
    }
}
