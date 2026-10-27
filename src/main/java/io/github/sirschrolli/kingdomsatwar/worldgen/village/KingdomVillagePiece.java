package io.github.sirschrolli.kingdomsatwar.worldgen.village;

import io.github.sirschrolli.kingdomsatwar.entity.villager.VillageProfession;
import io.github.sirschrolli.kingdomsatwar.worldgen.lair.ModStructures;
import io.github.sirschrolli.kingdomsatwar.worldgen.umbrer.VillageBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import org.jspecify.annotations.Nullable;

/**
 * Ein Bauteil eines Dorfs: der Dorfplatz, ein Weg oder ein Gebäude. Gespeichert werden nur Art, Bauweise, Ursprung,
 * Drehung und Startwert (beim Weg die Höhen) – alles andere ergibt sich daraus, in jedem Chunk gleich.
 */
public class KingdomVillagePiece extends StructurePiece {
    public enum Kind { SQUARE, ROAD, BUILDING }

    /** Weg: Brücke bzw. Steg an dieser Stelle. */
    public static final int BRIDGE = 1;
    /** Weg: Laternenpfahl links bzw. rechts (in Wegrichtung gesehen). */
    public static final int LAMP_LEFT = 2;
    public static final int LAMP_RIGHT = 4;

    private final Kind kind;
    private final VillageStyle style;
    private final int variant;
    private final BlockPos origin;
    private final Rotation rotation;
    private final long seed;
    /** Mitte des Dorfplatzes (an sie sind die Bewohner gebunden). */
    private final BlockPos center;
    private final String villageName;
    /** Gebäude: welches; um wie viel der Fußboden über dem Weg vor der Tür liegt (−1, 0, 1). */
    private final @Nullable VillageBuilding building;
    private final int step;
    /** Weg: Richtung, Höhen (oberster Block) je Schritt ab dem Ursprung, Merkmale je Schritt, ab welchem Schritt gebaut wird. */
    private final Direction direction;
    private final int[] heights;
    private final int[] flags;
    private final int skip;
    /** Platz: Halbmesser; Mittelstück (Brunnen, Dorflinde …). Gebäude: Besonderheiten ({@link #BEHIND}). */
    private final int radius;
    private final int feature;
    /** Gebäude hinter einem anderen (ohne eigenen Weg vor der Tür): dort wohnt niemand, der heimfinden müsste. */
    static final int BEHIND = 1;

    private KingdomVillagePiece(Kind kind, VillageStyle style, int variant, BlockPos origin, Rotation rotation, long seed, BlockPos center,
            String villageName, @Nullable VillageBuilding building, int step, Direction direction, int[] heights, int[] flags, int skip,
            int radius, int feature, BoundingBox box) {
        super(ModStructures.VILLAGE_PIECE.get(), 0, box);
        this.kind = kind;
        this.style = style;
        this.variant = variant;
        this.origin = origin;
        this.rotation = rotation;
        this.seed = seed;
        this.center = center;
        this.villageName = villageName;
        this.building = building;
        this.step = step;
        this.direction = direction;
        this.heights = heights;
        this.flags = flags;
        this.skip = skip;
        this.radius = radius;
        this.feature = feature;
    }

    public KingdomVillagePiece(CompoundTag tag) {
        super(ModStructures.VILLAGE_PIECE.get(), tag);
        this.kind = Kind.valueOf(tag.getStringOr("Kind", "BUILDING"));
        this.style = VillageStyle.valueOf(tag.getStringOr("Style", "TIMBER"));
        this.variant = tag.getIntOr("Variant", 0);
        this.origin = new BlockPos(tag.getIntOr("OX", 0), tag.getIntOr("OY", 64), tag.getIntOr("OZ", 0));
        this.rotation = Rotation.values()[Mth.clamp(tag.getIntOr("Rot", 0), 0, 3)];
        this.seed = tag.getLongOr("Seed", 0L);
        this.center = new BlockPos(tag.getIntOr("CX", this.origin.getX()), tag.getIntOr("CY", this.origin.getY()),
                tag.getIntOr("CZ", this.origin.getZ()));
        this.villageName = tag.getStringOr("Name", "");
        String b = tag.getStringOr("Building", "");
        VillageBuilding found = null;
        for (VillageBuilding v : VillageBuilding.values()) {
            if (v.name().equals(b)) {
                found = v;
            }
        }
        this.building = found;
        this.step = tag.getIntOr("Step", 0);
        this.direction = Direction.from3DDataValue(tag.getIntOr("Dir", 2));
        this.heights = tag.getIntArray("Road").orElse(new int[0]);
        this.flags = tag.getIntArray("Flags").orElse(new int[this.heights.length]);
        this.skip = tag.getIntOr("Skip", 0);
        this.radius = tag.getIntOr("Radius", 7);
        this.feature = tag.getIntOr("Feature", 0);
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        tag.putString("Kind", this.kind.name());
        tag.putString("Style", this.style.name());
        tag.putInt("Variant", this.variant);
        tag.putInt("OX", this.origin.getX());
        tag.putInt("OY", this.origin.getY());
        tag.putInt("OZ", this.origin.getZ());
        tag.putInt("Rot", this.rotation.ordinal());
        tag.putLong("Seed", this.seed);
        tag.putInt("CX", this.center.getX());
        tag.putInt("CY", this.center.getY());
        tag.putInt("CZ", this.center.getZ());
        tag.putString("Name", this.villageName);
        if (this.building != null) {
            tag.putString("Building", this.building.name());
        }
        tag.putInt("Step", this.step);
        tag.putInt("Dir", this.direction.get3DDataValue());
        tag.putIntArray("Road", this.heights);
        tag.putIntArray("Flags", this.flags);
        tag.putInt("Skip", this.skip);
        tag.putInt("Radius", this.radius);
        tag.putInt("Feature", this.feature);
    }

    // =====================================================================================
    // Erzeugen
    // =====================================================================================

    static KingdomVillagePiece square(VillageStyle style, int variant, BlockPos center, String name, int radius, int feature, long seed) {
        BoundingBox box = new BoundingBox(center.getX() - radius - 2, center.getY() - 16, center.getZ() - radius - 2,
                center.getX() + radius + 2, center.getY() + 14, center.getZ() + radius + 2);
        return new KingdomVillagePiece(Kind.SQUARE, style, variant, center, Rotation.NONE, seed, center, name, null, 0, Direction.NORTH,
                new int[0], new int[0], 0, radius, feature, box);
    }

    static KingdomVillagePiece road(VillageStyle style, int variant, BlockPos start, BlockPos center, String name, Direction direction,
            int[] heights, int[] flags, int skip, long seed) {
        int length = heights.length - 1;
        int minY = Integer.MAX_VALUE;
        int maxY = Integer.MIN_VALUE;
        for (int t = skip; t <= length; t++) {
            minY = Math.min(minY, heights[t]);
            maxY = Math.max(maxY, heights[t]);
        }
        if (minY > maxY) {
            minY = maxY = start.getY();
        }
        int x0 = start.getX() + direction.getStepX() * skip;
        int z0 = start.getZ() + direction.getStepZ() * skip;
        int x1 = start.getX() + direction.getStepX() * length;
        int z1 = start.getZ() + direction.getStepZ() * length;
        BoundingBox box = new BoundingBox(Math.min(x0, x1) - 3, minY - 16, Math.min(z0, z1) - 3, Math.max(x0, x1) + 3, maxY + 6,
                Math.max(z0, z1) + 3);
        return new KingdomVillagePiece(Kind.ROAD, style, variant, start, Rotation.NONE, seed, center, name, null, 0, direction, heights, flags,
                skip, 0, 0, box);
    }

    static KingdomVillagePiece building(VillageStyle style, int variant, VillageBuilding kind, BlockPos origin, Rotation rotation, long seed,
            BlockPos center, String name, int step) {
        return building(style, variant, kind, origin, rotation, seed, center, name, step, 0);
    }

    static KingdomVillagePiece building(VillageStyle style, int variant, VillageBuilding kind, BlockPos origin, Rotation rotation, long seed,
            BlockPos center, String name, int step, int extras) {
        BoundingBox box = footprint(origin, rotation, kind.width, kind.depth, origin.getY() - 20, origin.getY() + kind.height)
                .inflatedBy(3, 0, 3);
        return new KingdomVillagePiece(Kind.BUILDING, style, variant, origin, rotation, seed, center, name, kind, step, Direction.NORTH,
                new int[0], new int[0], 0, 0, extras, box);
    }

    /** Liegt das Gebäude hinter einem anderen (Feld hinter einem Haus)? */
    boolean behind() {
        return this.kind == Kind.BUILDING && (this.feature & BEHIND) != 0;
    }

    /** Für Tests und Bilder: ein einzelnes Gebäude ohne Dorf (Fußboden auf Höhe des Ursprungs, Weg davor gleich hoch). */
    public static KingdomVillagePiece single(VillageStyle style, int variant, VillageBuilding kind, BlockPos origin, Rotation rotation, long seed) {
        return building(style, variant, kind, origin, rotation, seed, origin, "Testdorf", 0);
    }

    /** Grundfläche eines gedrehten Rechtecks (Ursprung = vorne links). */
    static BoundingBox footprint(BlockPos origin, Rotation rotation, int width, int depth, int minY, int maxY) {
        BlockPos a = origin;
        BlockPos b = origin.offset(new BlockPos(width - 1, 0, depth - 1).rotate(rotation));
        return new BoundingBox(Math.min(a.getX(), b.getX()), minY, Math.min(a.getZ(), b.getZ()), Math.max(a.getX(), b.getX()), maxY,
                Math.max(a.getZ(), b.getZ()));
    }

    // =====================================================================================
    // Abfragen
    // =====================================================================================

    public Kind kind() {
        return this.kind;
    }

    public VillageStyle style() {
        return this.style;
    }

    public int variant() {
        return this.variant;
    }

    public BlockPos origin() {
        return this.origin;
    }

    public Rotation rotation() {
        return this.rotation;
    }

    public BlockPos center() {
        return this.center;
    }

    public String villageName() {
        return this.villageName;
    }

    public @Nullable VillageBuilding building() {
        return this.building;
    }

    /** Um wie viel der Fußboden über dem Weg vor der Tür liegt (−1, 0 oder 1). */
    public int step() {
        return this.step;
    }

    Direction direction() {
        return this.direction;
    }

    int[] heights() {
        return this.heights;
    }

    int[] flags() {
        return this.flags;
    }

    int skip() {
        return this.skip;
    }

    int radius() {
        return this.radius;
    }

    int feature() {
        return this.feature;
    }

    long seed() {
        return this.seed;
    }

    /** Die Fläche, die Bäume und Felsen meiden (Gebäude, Weg samt Rand, Platz). */
    public BoundingBox clearing() {
        return this.getBoundingBox();
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator, RandomSource ignored,
            BoundingBox chunkBox, ChunkPos chunkPos, BlockPos referencePos) {
        VillagePalette palette = this.style.palette(this.variant);
        VillageBuilder b = new VillageBuilder(level, chunkBox, this.origin, this.rotation, this.seed);
        switch (this.kind) {
            case SQUARE -> VillageSquare.build(b, palette, this);
            case ROAD -> VillageRoads.build(b, palette, this);
            case BUILDING -> VillageBuildings.build(new Arch(b, palette), this);
        }
        b.finish();
    }

    /**
     * Ein Bewohner, der in diesem Gebäude lebt (und arbeitet): erscheint bei (x, y, z), schläft im Bett bei {@code bed},
     * arbeitet bei {@code work} (beides in Gebäude-Koordinaten, {@code null}: keins). Gesetzt wird er nur in dem Chunk,
     * in dem er steht.
     */
    public void resident(VillageBuilder b, int x, int y, int z, VillageProfession profession, @Nullable BlockPos bed, @Nullable BlockPos work) {
        BlockPos pos = b.world(x, y, z);
        if (!b.inside(pos)) {
            return;
        }
        // vor der Haustür: von dort aus findet jeder hinein (auch wenn der Weg ums Haus herum weit ist)
        BlockPos door = b.entrance != null ? b.world(b.entrance.getX(), b.entrance.getY(), b.entrance.getZ())
                : this.building == null ? null : b.world(this.building.width / 2, 1, -1);
        BoundingBox house = this.building == null ? null
                : footprint(this.origin, this.rotation, this.building.width, this.building.depth, this.origin.getY() - 2, this.origin.getY() + this.building.height);
        VillageResidents.villager(b.level, pos, profession, bed == null ? null : b.world(bed.getX(), bed.getY(), bed.getZ()),
                work == null ? null : b.world(work.getX(), work.getY(), work.getZ()), door, house, this.center, this.villageName, b.random);
    }

    /** Ein Wächter, der um {@code post} (Gebäude-Koordinaten) seine Runde geht; {@code archer}: Bogenschütze. */
    public void guard(VillageBuilder b, int x, int y, int z, boolean archer) {
        BlockPos pos = b.world(x, y, z);
        if (!b.inside(pos)) {
            return;
        }
        VillageResidents.guard(b.level, pos, archer, this.center, this.villageName, b.random);
    }
}
