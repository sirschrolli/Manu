package io.github.sirschrolli.kingdomsatwar.worldgen.atoll;

import com.mojang.serialization.MapCodec;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.worldgen.lair.ModStructures;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

/**
 * Das Versunkene Atoll: ein Ring aus Klippen und Felsnadeln um eine flache, türkise Lagune; in ihrer Mitte die Ruine
 * eines Tempels des Meeresvolks – und ein Hügel, aus dem ein leuchtender Speer ragt. Die Form baut {@link AtollPiece},
 * wo es liegt, bestimmt {@link AtollPlacement}.
 */
public class AtollStructure extends Structure {
    public static final MapCodec<AtollStructure> CODEC = simpleCodec(AtollStructure::new);

    public AtollStructure(Structure.StructureSettings settings) {
        super(settings);
    }

    @Override
    protected Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext context) {
        ChunkPos chunk = context.chunkPos();
        int sea = context.chunkGenerator().getSeaLevel();
        BlockPos center = new BlockPos(chunk.getMiddleBlockX(), AtollPiece.platformTop(sea) + 1, chunk.getMiddleBlockZ());
        long seed = context.random().nextLong();
        KingdomsAtWar.LOGGER.info("Versunkenes Atoll bei {} {} {}", center.getX(), center.getY(), center.getZ());
        return Optional.of(new Structure.GenerationStub(center, builder -> builder.addPiece(new AtollPiece(center, seed, sea))));
    }

    @Override
    public StructureType<?> type() {
        return ModStructures.SUNKEN_ATOLL.get();
    }
}
