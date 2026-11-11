package io.github.sirschrolli.kingdomsatwar.worldgen.site;

import io.github.sirschrolli.kingdomsatwar.worldgen.lair.ModStructures;
import io.github.sirschrolli.kingdomsatwar.worldgen.umbrer.VillageBuilder;
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
 * Ein Bauteil eines eigenen Bauwerks der Oberwelt. Was es ist, sagen Art ({@link SiteKind}) und Teil ({@code part}, je
 * Art verschieden: etwa Stollen, Kreuzung oder Förderschacht der Mine); gebaut wird es in eigenen Koordinaten um
 * {@code origin} (gedreht um {@code rotation}) mit den Werten aus {@code data}.
 */
public class SitePiece extends StructurePiece {
    private final SiteKind kind;
    private final int part;
    private final BlockPos origin;
    private final Rotation rotation;
    private final long seed;
    private final int[] data;

    public SitePiece(SiteKind kind, int part, BlockPos origin, Rotation rotation, long seed, int[] data, BoundingBox box) {
        super(ModStructures.SITE_PIECE.get(), 0, box);
        this.kind = kind;
        this.part = part;
        this.origin = origin.immutable();
        this.rotation = rotation;
        this.seed = seed;
        this.data = data;
    }

    public SitePiece(CompoundTag tag) {
        super(ModStructures.SITE_PIECE.get(), tag);
        SiteKind found = SiteKind.FALLEN_STAR;
        String name = tag.getStringOr("Kind", "");
        for (SiteKind k : SiteKind.values()) {
            if (k.id().equals(name)) {
                found = k;
            }
        }
        this.kind = found;
        this.part = tag.getIntOr("Part", 0);
        this.origin = new BlockPos(tag.getIntOr("OX", 0), tag.getIntOr("OY", 64), tag.getIntOr("OZ", 0));
        this.rotation = Rotation.values()[Mth.clamp(tag.getIntOr("Rot", 0), 0, 3)];
        this.seed = tag.getLongOr("Seed", 0L);
        this.data = tag.getIntArray("Data").orElse(new int[0]);
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        tag.putString("Kind", this.kind.id());
        tag.putInt("Part", this.part);
        tag.putInt("OX", this.origin.getX());
        tag.putInt("OY", this.origin.getY());
        tag.putInt("OZ", this.origin.getZ());
        tag.putInt("Rot", this.rotation.ordinal());
        tag.putLong("Seed", this.seed);
        tag.putIntArray("Data", this.data);
    }

    /** Die Grundfläche eines gedrehten Quaders in eigenen Koordinaten (Ursprung bei {@code origin}). */
    public static BoundingBox box(BlockPos origin, Rotation rotation, int x0, int y0, int z0, int x1, int y1, int z1) {
        BlockPos a = origin.offset(new BlockPos(x0, y0, z0).rotate(rotation));
        BlockPos b = origin.offset(new BlockPos(x1, y1, z1).rotate(rotation));
        return BoundingBox.fromCorners(a, b);
    }

    public SiteKind kind() {
        return this.kind;
    }

    public int part() {
        return this.part;
    }

    public BlockPos origin() {
        return this.origin;
    }

    public Rotation rotation() {
        return this.rotation;
    }

    public long seed() {
        return this.seed;
    }

    /** Ein Wert aus {@code data} (0, wenn es ihn nicht gibt). */
    public int data(int index) {
        return index < this.data.length ? this.data[index] : 0;
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator, RandomSource ignored,
            BoundingBox chunkBox, ChunkPos chunkPos, BlockPos referencePos) {
        VillageBuilder b = new VillageBuilder(level, chunkBox, this.origin, this.rotation, this.seed);
        switch (this.kind) {
            case FALLEN_STAR -> FallenStar.build(b, this);
            case AQUEDUCT -> Aqueduct.build(b, this);
            case TIDE_SHRINE -> TideShrine.build(b, this);
            case SEA_DRAGON -> SeaDragon.build(b, this);
            case CARAVANSERAI -> Caravanserai.build(b, this);
            case SERPENT_TEMPLE -> SerpentTemple.build(b, this);
            case WITCH_TREE -> WitchTree.build(b, this);
            case FROST_GROTTO -> FrostGrotto.build(b, this);
            case BANDIT_CAMP -> BanditCamp.build(b, this);
            case CHARCOAL_CAMP -> CharcoalCamp.build(b, this);
            case EMBER_GATE -> EmberGate.build(b, this);
            case CLAM_FIELD -> ClamField.build(b, this);
            case CORAL_PALACE -> CoralPalace.build(b, this);
            case BURIED_ABBEY -> BuriedAbbey.build(b, this);
            case SILENT_CATHEDRAL -> SilentCathedral.build(b, this);
            case TRIAL_ARENA -> TrialArena.build(b, this);
            case ROBBER_CASTLE -> RobberCastle.build(b, this);
            case ASH_FORGE -> AshForge.build(b, this);
            case HORTFESTE -> Hortfeste.build(b, this);
            case GIANT_BONES -> GiantBones.build(b, this);
            case LOST_GATE -> LostGate.build(b, this);
            case STARWATCH -> Starwatch.build(b, this);
            case ERROR_ZONE -> ErrorZone.build(b, this);
            case SUNKEN_TEMPLE -> SunkenTemple.build(b, this);
        }
        b.finish();
    }
}
