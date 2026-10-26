package io.github.sirschrolli.kingdomsatwar.worldgen.lair;

import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

/**
 * Das ganze Nest als ein großes Bauteil. Gebaut wird Chunk für Chunk: Jeder Chunk setzt nur seine eigenen Blöcke,
 * alle Formen ergeben sich aus Mitte und Startwert – so passen die Chunks nahtlos zusammen.
 */
public class DragonLairPiece extends StructurePiece {
    private final DragonType element;
    private final BlockPos center;
    private final long seed;

    public DragonLairPiece(DragonType element, BlockPos center, long seed) {
        super(ModStructures.DRAGON_LAIR_PIECE.get(), 0, makeBox(element, center));
        this.element = element;
        this.center = center;
        this.seed = seed;
    }

    public DragonLairPiece(CompoundTag tag) {
        super(ModStructures.DRAGON_LAIR_PIECE.get(), tag);
        this.element = DragonType.byId(tag.getStringOr("Element", "fire"));
        this.center = new BlockPos(tag.getIntOr("CX", 0), tag.getIntOr("CY", 64), tag.getIntOr("CZ", 0));
        this.seed = tag.getLongOr("Seed", 0L);
    }

    private static BoundingBox makeBox(DragonType element, BlockPos center) {
        LairDesign design = LairDesigns.of(element);
        int r = design.radius();
        return new BoundingBox(center.getX() - r, center.getY() - design.depthBelow(), center.getZ() - r,
                center.getX() + r, center.getY() + design.heightAbove(), center.getZ() + r);
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        tag.putString("Element", this.element.id());
        tag.putInt("CX", this.center.getX());
        tag.putInt("CY", this.center.getY());
        tag.putInt("CZ", this.center.getZ());
        tag.putLong("Seed", this.seed);
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator, RandomSource random,
            BoundingBox chunkBB, ChunkPos chunkPos, BlockPos referencePos) {
        LairDesigns.of(this.element).build(new LairContext(level, chunkBB, this.center, this.seed));
    }

    public DragonType element() {
        return this.element;
    }

    public BlockPos center() {
        return this.center;
    }
}
