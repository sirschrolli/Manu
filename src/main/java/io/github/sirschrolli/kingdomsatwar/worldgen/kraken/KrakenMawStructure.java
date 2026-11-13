package io.github.sirschrolli.kingdomsatwar.worldgen.kraken;

import com.mojang.serialization.MapCodec;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.worldgen.lair.ModStructures;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

/**
 * Der Krakenschlund (seit 0.91): ein Becken im offenen Urmeer, in dessen Mitte ein Schlund in die Tiefe führt, ringsum ein
 * Ring aus Brandungsfelsen. Die Form baut {@link KrakenMawPiece}, wo er liegt, bestimmt {@link KrakenMawPlacement}.
 */
public class KrakenMawStructure extends Structure {
    public static final MapCodec<KrakenMawStructure> CODEC = simpleCodec(KrakenMawStructure::new);

    public KrakenMawStructure(Structure.StructureSettings settings) {
        super(settings);
    }

    @Override
    protected Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext context) {
        ChunkPos chunk = context.chunkPos();
        int sea = context.chunkGenerator().getSeaLevel();
        BlockPos center = new BlockPos(chunk.getMiddleBlockX(), sea, chunk.getMiddleBlockZ());
        long seed = context.random().nextLong();
        KingdomsAtWar.LOGGER.info("Krakenschlund bei {} {} {}", center.getX(), center.getY(), center.getZ());
        return Optional.of(new Structure.GenerationStub(center, builder -> builder.addPiece(new KrakenMawPiece(center, seed, sea))));
    }

    @Override
    public StructureType<?> type() {
        return ModStructures.KRAKEN_MAW.get();
    }
}
