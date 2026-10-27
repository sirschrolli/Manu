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
        int r = Math.max(design.radius(), design.zoneRadius());
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

    /**
     * Baut den Teil im aktuellen Chunk: erst wird das Nest-Biom in den Chunk gemalt (Farben, Partikel, Geräusche),
     * dann die Landschaft rundherum umgestaltet, zuletzt das eigentliche Nest gebaut.
     */
    @Override
    public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator, RandomSource random,
            BoundingBox chunkBB, ChunkPos chunkPos, BlockPos referencePos) {
        LairDesign design = LairDesigns.of(this.element);
        LairContext ctx = new LairContext(level, chunkBB, this.center, this.seed);
        this.paintBiome(level, chunkPos, design, ctx);
        design.buildZone(ctx);
        design.build(ctx);
    }

    private void paintBiome(WorldGenLevel level, ChunkPos chunkPos, LairDesign design, LairContext ctx) {
        var key = io.github.sirschrolli.kingdomsatwar.worldgen.ModBiomes.forNest(this.element);
        if (key == null) {
            return;
        }
        var biome = level.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.BIOME).get(key);
        if (biome.isEmpty()) {
            return;
        }
        net.minecraft.world.level.chunk.ChunkAccess chunk = level.getChunk(chunkPos.x(), chunkPos.z());
        boolean any = false;
        for (int qx = 0; qx < 4 && !any; qx++) {
            for (int qz = 0; qz < 4 && !any; qz++) {
                any = design.inZone(ctx, chunkPos.getMinBlockX() + qx * 4 + 2, chunkPos.getMinBlockZ() + qz * 4 + 2);
            }
        }
        if (!any) {
            return;
        }
        net.minecraft.core.Holder<net.minecraft.world.level.biome.Biome> nestBiome = biome.get();
        chunk.fillBiomesFromNoise((qx, qy, qz) -> design.inZone(ctx, net.minecraft.core.QuartPos.toBlock(qx) + 2,
                net.minecraft.core.QuartPos.toBlock(qz) + 2) ? nestBiome : chunk.getNoiseBiome(qx, qy, qz));
        chunk.markUnsaved();
    }

    public DragonType element() {
        return this.element;
    }

    public BlockPos center() {
        return this.center;
    }
}
