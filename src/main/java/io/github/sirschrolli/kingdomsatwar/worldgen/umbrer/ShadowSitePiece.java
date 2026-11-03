package io.github.sirschrolli.kingdomsatwar.worldgen.umbrer;

import io.github.sirschrolli.kingdomsatwar.worldgen.lair.ModStructures;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

/**
 * Ein Bauwerk der Schattenwelt als ein Bauteil (Tempel, Mine, Leuchtturm oder Ruine). Gespeichert werden nur Art,
 * Ursprung (Mitte, Höhe des Bodens), Drehung und Startwert; gebaut wird Chunk für Chunk mit {@link VillageBuilder}, die
 * Formen selbst stehen in {@link ShadowSites}.
 */
public class ShadowSitePiece extends StructurePiece {
    private final ShadowSiteStructure.Kind kind;
    private final BlockPos origin;
    private final Rotation rotation;
    private final long seed;

    public ShadowSitePiece(ShadowSiteStructure.Kind kind, BlockPos origin, Rotation rotation, long seed) {
        super(ModStructures.SHADOW_SITE_PIECE.get(), 0, makeBox(kind, origin));
        this.kind = kind;
        this.origin = origin;
        this.rotation = rotation;
        this.seed = seed;
    }

    public ShadowSitePiece(CompoundTag tag) {
        super(ModStructures.SHADOW_SITE_PIECE.get(), tag);
        ShadowSiteStructure.Kind found = ShadowSiteStructure.Kind.TEMPLE;
        String id = tag.getStringOr("Kind", "moon_temple");
        for (ShadowSiteStructure.Kind k : ShadowSiteStructure.Kind.values()) {
            if (k.id().equals(id)) {
                found = k;
            }
        }
        this.kind = found;
        this.origin = new BlockPos(tag.getIntOr("OX", 0), tag.getIntOr("OY", 64), tag.getIntOr("OZ", 0));
        this.rotation = Rotation.values()[Mth.clamp(tag.getIntOr("Rot", 0), 0, 3)];
        this.seed = tag.getLongOr("Seed", 0L);
    }

    private static BoundingBox makeBox(ShadowSiteStructure.Kind kind, BlockPos o) {
        return new BoundingBox(o.getX() - kind.radius, o.getY() - kind.below, o.getZ() - kind.radius,
                o.getX() + kind.radius, o.getY() + kind.above, o.getZ() + kind.radius);
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        tag.putString("Kind", this.kind.id());
        tag.putInt("OX", this.origin.getX());
        tag.putInt("OY", this.origin.getY());
        tag.putInt("OZ", this.origin.getZ());
        tag.putInt("Rot", this.rotation.ordinal());
        tag.putLong("Seed", this.seed);
    }

    public ShadowSiteStructure.Kind kind() {
        return this.kind;
    }

    public BlockPos origin() {
        return this.origin;
    }

    public Rotation rotation() {
        return this.rotation;
    }

    /**
     * Die Fläche, die Bäume, Felsnadeln und Ranken frei lassen: bei der Mine nur der Platz um den Förderturm (Halle,
     * Stollen und Kristallkammer liegen tief unter der Erde, darüber darf der Wald stehen), sonst das ganze Bauwerk.
     */
    public BoundingBox clearing() {
        if (this.kind == ShadowSiteStructure.Kind.MINE) {
            return new BoundingBox(this.origin.getX() - 7, this.origin.getY() - 4, this.origin.getZ() - 7,
                    this.origin.getX() + 7, this.origin.getY() + 12, this.origin.getZ() + 7);
        }
        return this.getBoundingBox();
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator, RandomSource random,
            BoundingBox chunkBB, ChunkPos chunkPos, BlockPos referencePos) {
        VillageBuilder b = new VillageBuilder(level, chunkBB, this.origin, this.rotation, this.seed);
        switch (this.kind) {
            case TEMPLE -> ShadowSites.temple(b);
            case MINE -> ShadowSites.mine(b);
            case LIGHTHOUSE -> ShadowSites.lighthouse(b);
            case RUIN -> ShadowSites.ruin(b);
        }
        b.finish();
    }
}
