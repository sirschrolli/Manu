package io.github.sirschrolli.kingdomsatwar.worldgen.shepherd;

import io.github.sirschrolli.kingdomsatwar.block.shadow.SoulBellBlockEntity;
import io.github.sirschrolli.kingdomsatwar.loot.ModLootTables;
import io.github.sirschrolli.kingdomsatwar.registry.ShadowBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.ShadowGear;
import io.github.sirschrolli.kingdomsatwar.registry.SoulShepherd;
import io.github.sirschrolli.kingdomsatwar.worldgen.lair.LairContext;
import io.github.sirschrolli.kingdomsatwar.worldgen.lair.ModStructures;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.CrossCollisionBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import org.jspecify.annotations.Nullable;

/**
 * Der Seelenpferch (seit 0.98), Chunk für Chunk gebaut:
 * <ol>
 *     <li>Eine <b>Lichtung</b> im Sumpf: Schilf, Sprossen und Farne weichen (Bäume wachsen hier gar nicht erst, siehe
 *     {@link #inClearing}).</li>
 *     <li>Der <b>Pferch</b>: ein runder, aufgeschütteter Grund, eine Weide aus Schattengras mit zertretenen Flecken aus
 *     Seelenerde und Schlamm, Wegen von den Toren zum Podest, halb versunkenen Knochen und ein paar Schädeln; ringsum ein Steg aus Schattenholz und ein <b>Zaun</b> mit Pfosten, auf
 *     denen Seelenlaternen und Schafsschädel stehen. Vier Tore (Süden, Osten, Westen, Norden).</li>
 *     <li>In der Mitte ein <b>Podest</b> aus Schlammziegeln mit Dämmerquarz an den Ecken, darüber der <b>Glockenstuhl</b>:
 *     zwei Pfosten, ein Balken, ein kleines Giebeldach, zwei Laternen an Ketten – und unter dem Balken die
 *     <b>Seelenglocke</b>. Vor ihr steht der Seelenhirte in Andacht (mit Blick zum Südtor).</li>
 *     <li>Vier <b>Feuerschalen</b>: Säulen aus Schlammziegeln, oben Seelenerde mit Seelenfeuer.</li>
 *     <li><b>Stege</b> auf Stelzen nach Süden, Osten und Westen hinaus über den Sumpf, mit Geländer, Laternenpfosten und einer
 *     Plattform am Ende.</li>
 *     <li>Im Norden die <b>Hütte des Hirten</b> auf Stelzen: Schattenholz mit Giebeldach, ein Heulager, ein Fass, Kerzen,
 *     ein Block Dämmerquarz und eine Truhe mit Beute.</li>
 * </ol>
 */
public class SoulPenPiece extends StructurePiece {
    /** Halbmesser des Zauns. */
    public static final int PEN = 14;
    /** Bis hierhin reicht der Boden des Pferchs (dahinter der Steg am Zaun). */
    public static final int ARENA = 12;
    /** So weit reichen die Stege … */
    public static final int PATH_END = 25;
    /** … und so weit das ganze Bauwerk samt Lichtung. */
    public static final int REACH = 30;
    /** Die Glocke hängt so hoch über dem Boden … */
    public static final int BELL_Y = 5;
    /** … die Mitte der Hütte liegt so weit nördlich. */
    public static final int HUT = 21;
    /** Das Haupttor (der Hirte blickt dorthin). */
    public static final Direction GATE = Direction.SOUTH;

    private final BlockPos center;
    private final long seed;
    private final int sea;

    public SoulPenPiece(BlockPos center, long seed, int sea) {
        super(ModStructures.SOUL_PEN_PIECE.get(), 0, makeBox(center));
        this.center = center;
        this.seed = seed;
        this.sea = sea;
    }

    public SoulPenPiece(CompoundTag tag) {
        super(ModStructures.SOUL_PEN_PIECE.get(), tag);
        this.center = new BlockPos(tag.getIntOr("CX", 0), tag.getIntOr("CY", 64), tag.getIntOr("CZ", 0));
        this.seed = tag.getLongOr("Seed", 0L);
        this.sea = tag.getIntOr("Sea", 63);
    }

    /** Der Kasten des Bauwerks: etwas größer als die Lichtung, damit auch die Chunks ringsum vom Pferch wissen (Bäume). */
    public static BoundingBox makeBox(BlockPos c) {
        int r = REACH + 6;
        return new BoundingBox(c.getX() - r, c.getY() - 12, c.getZ() - r, c.getX() + r, c.getY() + 16, c.getZ() + r);
    }

    /** Wo die Seelenglocke hängt. */
    public static BlockPos bell(BlockPos center) {
        return center.above(BELL_Y);
    }

    /** Wo der Hirte in Andacht steht: vor der Glocke, zum Haupttor hin. */
    public static BlockPos home(BlockPos center) {
        return center.relative(GATE);
    }

    public BlockPos center() {
        return this.center;
    }

    /**
     * Liegt (x, z) im Bereich eines Seelenpferchs (plus {@code margin})? Beim Erzeugen der Welt über die Verweise des Chunks auf
     * die Pferche ringsum – so wachsen auch Bäume aus Nachbarchunks, die erst nach dem Pferch entstehen, nicht hinein.
     */
    public static boolean inClearing(WorldGenLevel level, int x, int z, int margin) {
        var structure = level.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.STRUCTURE)
                .getValue(ModStructures.SOUL_PEN_STRUCTURE);
        var chunk = structure == null ? null : io.github.sirschrolli.kingdomsatwar.worldgen.GenChunks.readable(level, x >> 4, z >> 4,
                net.minecraft.world.level.chunk.status.ChunkStatus.STRUCTURE_REFERENCES);
        if (chunk == null) {
            return false;
        }
        double keep = REACH + margin;
        for (long ref : chunk.getReferencesForStructure(structure)) {
            net.minecraft.world.level.ChunkPos startPos = net.minecraft.world.level.ChunkPos.unpack(ref);
            var startChunk = io.github.sirschrolli.kingdomsatwar.worldgen.GenChunks.readable(level, startPos.x(), startPos.z(),
                    net.minecraft.world.level.chunk.status.ChunkStatus.STRUCTURE_STARTS);
            var start = startChunk != null ? startChunk.getStartForStructure(structure) : null;
            if (start == null || !start.isValid()) {
                continue;
            }
            for (var piece : start.getPieces()) {
                if (piece instanceof SoulPenPiece pen) {
                    double dx = pen.center.getX() - x;
                    double dz = pen.center.getZ() - z;
                    if (dx * dx + dz * dz < keep * keep) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        tag.putInt("CX", this.center.getX());
        tag.putInt("CY", this.center.getY());
        tag.putInt("CZ", this.center.getZ());
        tag.putLong("Seed", this.seed);
        tag.putInt("Sea", this.sea);
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator, RandomSource random,
            BoundingBox chunkBB, net.minecraft.world.level.ChunkPos chunkPos, BlockPos referencePos) {
        build(level, chunkBB, this.center, this.seed, this.sea, random, this);
    }

    /**
     * Baut den Pferch (für die Welt Chunk für Chunk, für Tests in einem Zug). {@code piece} legt die Truhe an (null: die
     * Beutetabelle wird direkt gesetzt).
     */
    public static void build(WorldGenLevel level, BoundingBox box, BlockPos center, long seed, int sea, RandomSource random,
            @Nullable SoulPenPiece piece) {
        new Builder(level, box, center, seed, sea).run(random, piece);
    }

    // =====================================================================================

    private static final class Builder {
        final WorldGenLevel level;
        final BoundingBox box;
        final LairContext ctx;
        final int cx;
        final int cz;
        final int f;
        final int sea;

        Builder(WorldGenLevel level, BoundingBox box, BlockPos center, long seed, int sea) {
            this.level = level;
            this.box = box;
            this.ctx = new LairContext(level, box, center, seed);
            this.cx = center.getX();
            this.cz = center.getZ();
            this.f = center.getY();
            this.sea = sea;
        }

        boolean in(int x, int y, int z) {
            return this.box.isInside(x, y, z) && y > this.level.getMinY() && y < this.level.getMaxY();
        }

        void set(int x, int y, int z, BlockState state) {
            if (this.in(x, y, z)) {
                this.level.setBlock(new BlockPos(x, y, z), state, Block.UPDATE_CLIENTS);
            }
        }

        void set(int x, int y, int z, Block block) {
            this.set(x, y, z, block.defaultBlockState());
        }

        BlockState get(int x, int y, int z) {
            return this.level.getBlockState(new BlockPos(x, y, z));
        }

        double rand(int a, int b, int salt) {
            return this.ctx.rand(a, b, salt);
        }

        double dist(int x, int z) {
            double dx = x - this.cx;
            double dz = z - this.cz;
            return Math.sqrt(dx * dx + dz * dz);
        }

        /** Luft oder (unter dem Wasserspiegel) Wasser. */
        void clear(int x, int y, int z) {
            if (this.in(x, y, z)) {
                this.level.setBlock(new BlockPos(x, y, z), y <= this.sea ? Blocks.WATER.defaultBlockState() : Blocks.AIR.defaultBlockState(),
                        Block.UPDATE_CLIENTS);
            }
        }

        void run(RandomSource random, @Nullable SoulPenPiece piece) {
            this.clearing();
            this.ground();
            this.walkRing();
            this.paths();
            this.hut(random, piece);
            this.fence();
            this.dais();
            this.belfry();
            this.braziers();
            this.bones();
            this.bell();
        }

        // ------------------------------------------------------------------ Lichtung und Grund

        /** Oberster Geländeblock der Säule (Bäume und Pflanzen zählen nicht) – höchstens 14 unter dem Boden des Pferchs, sonst -1. */
        private int groundTop(int x, int z) {
            for (int y = Math.min(this.box.maxY(), this.f + 12); y > this.f - 14; y--) {
                if (LairContext.isGround(this.get(x, y, z))) {
                    return y;
                }
            }
            return -1;
        }

        /** Ist das ein Teil des Sumpfs, der weichen darf (Schilf, Sprossen, Farne, Pilze, Laub, Ranken)? */
        private static boolean plant(BlockState state) {
            return !state.isAir() && !LairContext.isGround(state) && state.getFluidState().isEmpty() || state.is(net.minecraft.tags.BlockTags.LEAVES)
                    || state.is(net.minecraft.tags.BlockTags.LOGS) || (!state.getFluidState().isEmpty() && !state.getFluidState().isSource());
        }

        /** Im Pferch und auf den Stegen weicht alles Grüne; über dem Grund des Pferchs auch Hügel. */
        private void clearing() {
            for (int x = this.box.minX(); x <= this.box.maxX(); x++) {
                for (int z = this.box.minZ(); z <= this.box.maxZ(); z++) {
                    double d = this.dist(x, z);
                    boolean pen = d <= PEN + 1.5;
                    boolean walk = this.onPath(x, z, 2) || this.onHut(x, z, 2);
                    if (!pen && !walk && d > PEN + 4.5) {
                        continue;
                    }
                    int top = Math.min(this.box.maxY(), this.f + 15);
                    int from = pen ? this.f : this.f - 1;
                    for (int y = from; y <= top; y++) {
                        BlockState s = this.get(x, y, z);
                        if (s.isAir()) {
                            continue;
                        }
                        if (pen || plant(s) || (walk && y >= this.f)) {
                            this.clear(x, y, z);
                        }
                    }
                    // am Ufer rings um den Pferch: nur Pflanzen und Laub
                    if (!pen && !walk) {
                        for (int y = this.f - 3; y < from; y++) {
                            if (plant(this.get(x, y, z))) {
                                this.clear(x, y, z);
                            }
                        }
                    }
                }
            }
        }

        /** Der aufgeschüttete Grund des Pferchs: oben Seelenerde, Schlamm und Schattenerde, darunter Schlamm bis auf den Sumpf. */
        private void ground() {
            for (int x = this.box.minX(); x <= this.box.maxX(); x++) {
                for (int z = this.box.minZ(); z <= this.box.maxZ(); z++) {
                    double d = this.dist(x, z);
                    double bank = PEN + 3.5 - this.ctx.angleNoise(Math.atan2(z - this.cz, x - this.cx), 4.0, 31) * 2.0;
                    if (d > bank) {
                        continue;
                    }
                    int g = this.groundTop(x, z);
                    int topY = d <= PEN + 1.0 ? this.f - 1 : this.f - 2 - (d > PEN + 2.5 ? 1 : 0);
                    int bottom = g < 0 ? this.f - 12 : g;
                    for (int y = topY - 1; y > bottom && y > this.f - 13; y--) {
                        this.set(x, y, z, this.rand(x, z + y * 31, 33) < 0.85 ? Blocks.MUD : Blocks.PACKED_MUD);
                    }
                    if (d <= PEN + 1.0) {
                        this.set(x, topY, z, this.surface(x, z, d));
                    } else {
                        // die Böschung: Schlamm, Moos und Schattenerde, darüber Luft
                        double n = this.ctx.noise(x, z, 0.3, 35);
                        this.set(x, topY, z, n < 0.35 ? Blocks.MUD : n < 0.6 ? Blocks.MOSS_BLOCK : ShadowBlocks.SHADOW_SOIL.get());
                        for (int y = topY + 1; y <= this.f; y++) {
                            this.clear(x, y, z);
                        }
                    }
                }
            }
        }

        /**
         * Der Boden im Pferch: eine Weide aus Schattengras, darin zertretene Flecken aus Seelenerde und Schlamm, am Rand Moos;
         * von jedem Tor führt ein Weg aus gestampftem Schlamm und Schlammziegeln zum Podest.
         */
        private BlockState surface(int x, int z, double d) {
            if (d > ARENA + 0.5) {
                return ShadowBlocks.SHADOW_PLANKS.base().get().defaultBlockState();
            }
            int dx = Math.abs(x - this.cx);
            int dz = Math.abs(z - this.cz);
            if ((dx <= 1 || dz <= 1) && d > 3.0) {
                // die Wege: in der Mitte Ziegel, am Rand gestampfter Schlamm, hier und da ein loser Stein
                boolean middle = dx == 0 || dz == 0;
                double r = this.rand(x, z, 43);
                return (middle ? (r < 0.75 ? Blocks.MUD_BRICKS : Blocks.PACKED_MUD) : (r < 0.7 ? Blocks.PACKED_MUD : Blocks.MUD_BRICKS))
                        .defaultBlockState();
            }
            double n = this.ctx.noise(x, z, 0.24, 37);
            double m = this.ctx.noise(x, z, 0.55, 39);
            if (n < 0.3) {
                return (m < 0.55 ? Blocks.SOUL_SOIL : Blocks.MUD).defaultBlockState();                 // zertreten
            }
            if (n < 0.38) {
                return ShadowBlocks.SHADOW_SOIL.get().defaultBlockState();
            }
            if (d > ARENA - 2.0 && m > 0.6) {
                return Blocks.MOSS_BLOCK.defaultBlockState();
            }
            return ShadowBlocks.SHADOW_GRASS.get().defaultBlockState();
        }

        /** Der Steg am Zaun entlang (Bohlen aus Schattenholz, an den Stößen Platten). */
        private void walkRing() {
            for (int x = this.box.minX(); x <= this.box.maxX(); x++) {
                for (int z = this.box.minZ(); z <= this.box.maxZ(); z++) {
                    double d = this.dist(x, z);
                    if (d > ARENA + 0.5 && d <= PEN + 1.0) {
                        this.set(x, this.f - 1, z, this.rand(x, z, 41) < 0.12 ? ShadowBlocks.STRIPPED_SHADOW_LOG.get().defaultBlockState()
                                .setValue(RotatedPillarBlock.AXIS, Math.abs(x - this.cx) > Math.abs(z - this.cz) ? Direction.Axis.Z : Direction.Axis.X)
                                : ShadowBlocks.SHADOW_PLANKS.base().get().defaultBlockState());
                    }
                }
            }
        }

        // ------------------------------------------------------------------ Zaun und Tore

        /** Liegt (x, z) auf dem Zaunring (innerer Rand der Scheibe mit dem Halbmesser des Zauns, in 4er-Nachbarschaft geschlossen)? */
        private boolean ringCell(int x, int z) {
            double r = PEN + 0.5;
            if (this.dist(x, z) >= r) {
                return false;
            }
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if ((dx != 0 || dz != 0) && this.dist(x + dx, z + dz) >= r) {
                        return true;
                    }
                }
            }
            return false;
        }

        /** Liegt (x, z) in einem der vier Tore (drei Blöcke breit)? */
        private boolean gateCell(int x, int z) {
            int dx = x - this.cx;
            int dz = z - this.cz;
            return Math.abs(dx) <= 1 && Math.abs(dz) > ARENA || Math.abs(dz) <= 1 && Math.abs(dx) > ARENA;
        }

        private boolean fenceCell(int x, int z) {
            return this.ringCell(x, z) && !this.gateCell(x, z);
        }

        /** Die Pfosten: 16 rundherum, dazu je zwei an jedem Tor. */
        private boolean postCell(int x, int z) {
            if (!this.fenceCell(x, z)) {
                return false;
            }
            int dx = x - this.cx;
            int dz = z - this.cz;
            if ((Math.abs(dx) == 2 && Math.abs(dz) > ARENA) || (Math.abs(dz) == 2 && Math.abs(dx) > ARENA)) {
                return true;
            }
            for (int i = 0; i < 16; i++) {
                double a = i * Math.PI / 8.0 + Math.PI / 16.0;
                int px = this.cx + (int) Math.round(Math.cos(a) * PEN);
                int pz = this.cz + (int) Math.round(Math.sin(a) * PEN);
                if (px == x && pz == z) {
                    return true;
                }
            }
            return false;
        }

        private void fence() {
            BlockState fence = ShadowBlocks.SHADOW_FENCE.get().defaultBlockState();
            for (int x = this.box.minX(); x <= this.box.maxX(); x++) {
                for (int z = this.box.minZ(); z <= this.box.maxZ(); z++) {
                    if (!this.fenceCell(x, z)) {
                        continue;
                    }
                    if (this.postCell(x, z)) {
                        int dx = x - this.cx;
                        int dz = z - this.cz;
                        boolean gatePost = (Math.abs(dx) == 2 && Math.abs(dz) > ARENA) || (Math.abs(dz) == 2 && Math.abs(dx) > ARENA);
                        int h = gatePost ? 3 : 2;
                        for (int y = 0; y < h; y++) {
                            this.set(x, this.f + y, z, ShadowBlocks.SHADOW_LOG.get().defaultBlockState());
                        }
                        // auf den Pfosten Seelenlaternen, auf jedem vierten ein Schafsschädel
                        int i = (int) Math.round((Math.atan2(dz, dx) + Math.PI) / (Math.PI / 8.0));
                        if (!gatePost && i % 4 == 1) {
                            int rot = Math.floorMod(Math.round((float) (Math.toDegrees(Math.atan2(dx, -dz)) / 22.5)), 16);
                            this.set(x, this.f + h, z, Blocks.SKELETON_SKULL.defaultBlockState().setValue(SkullBlock.ROTATION, rot));
                        } else {
                            this.set(x, this.f + h, z, Blocks.SOUL_LANTERN.defaultBlockState());
                        }
                        continue;
                    }
                    BlockState s = fence
                            .setValue(CrossCollisionBlock.NORTH, this.fenceCell(x, z - 1))
                            .setValue(CrossCollisionBlock.SOUTH, this.fenceCell(x, z + 1))
                            .setValue(CrossCollisionBlock.WEST, this.fenceCell(x - 1, z))
                            .setValue(CrossCollisionBlock.EAST, this.fenceCell(x + 1, z));
                    this.set(x, this.f, z, s);
                }
            }
        }

        // ------------------------------------------------------------------ Podest, Glockenstuhl, Feuerschalen

        /** Das Podest unter dem Glockenstuhl: Schlammziegel in Ringen, an den Ecken Dämmerquarz. */
        private void dais() {
            for (int dx = -3; dx <= 3; dx++) {
                for (int dz = -3; dz <= 3; dz++) {
                    if (dx * dx + dz * dz > 11) {
                        continue;
                    }
                    int x = this.cx + dx;
                    int z = this.cz + dz;
                    boolean corner = Math.abs(dx) == 2 && Math.abs(dz) == 2;
                    this.set(x, this.f - 1, z, corner ? ShadowGear.DUSK_QUARTZ_ORE.get().defaultBlockState()
                            : (Math.abs(dx) + Math.abs(dz)) % 3 == 0 ? Blocks.CHISELED_TUFF_BRICKS.defaultBlockState() : Blocks.MUD_BRICKS.defaultBlockState());
                }
            }
        }

        /** Der Glockenstuhl: zwei Pfosten, der Balken darüber, ein kleines Giebeldach und zwei Laternen an Ketten. */
        private void belfry() {
            int top = this.f + BELL_Y + 1;
            for (int s : new int[] {-3, 3}) {
                for (int y = this.f; y < top; y++) {
                    this.set(this.cx + s, y, this.cz, ShadowBlocks.SHADOW_LOG.get().defaultBlockState());
                }
                // Streben unter den Enden des Balkens (außen an den Pfosten)
                this.set(this.cx + s + Integer.signum(s), top - 1, this.cz, ShadowBlocks.SHADOW_PLANKS.stairs().get().defaultBlockState()
                        .setValue(StairBlock.FACING, s < 0 ? Direction.EAST : Direction.WEST).setValue(StairBlock.HALF, Half.TOP));
            }
            for (int dx = -4; dx <= 4; dx++) {
                this.set(this.cx + dx, top, this.cz, ShadowBlocks.SHADOW_LOG.get().defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.X));
            }
            for (int dx = -4; dx <= 4; dx++) {
                int x = this.cx + dx;
                this.set(x, top + 1, this.cz - 1, ShadowBlocks.SHADOW_PLANKS.stairs().get().defaultBlockState().setValue(StairBlock.FACING, Direction.SOUTH));
                this.set(x, top + 1, this.cz, ShadowBlocks.SHADOW_PLANKS.base().get());
                this.set(x, top + 1, this.cz + 1, ShadowBlocks.SHADOW_PLANKS.stairs().get().defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH));
                this.set(x, top + 2, this.cz, ShadowBlocks.SHADOW_PLANKS.slab().get().defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM));
            }
            for (int s : new int[] {-2, 2}) {
                this.set(this.cx + s, top - 1, this.cz, Blocks.IRON_CHAIN.defaultBlockState());
                this.set(this.cx + s, top - 2, this.cz, Blocks.SOUL_LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));
            }
        }

        /** Die Glocke unter dem Balken; sie weiß, wo ihr Hirte steht. */
        private void bell() {
            BlockPos pos = SoulPenPiece.bell(new BlockPos(this.cx, this.f, this.cz));
            if (!this.box.isInside(pos)) {
                return;
            }
            this.level.setBlock(pos, SoulShepherd.SOUL_BELL.get().defaultBlockState(), Block.UPDATE_CLIENTS);
            if (this.level.getBlockEntity(pos) instanceof SoulBellBlockEntity be) {
                be.setHome(home(new BlockPos(this.cx, this.f, this.cz)));
            }
        }

        /** Vier Feuerschalen: Säulen aus Schlammziegeln mit einer Schale aus Seelenerde, in der Seelenfeuer brennt. */
        private void braziers() {
            for (int i = 0; i < 4; i++) {
                double a = Math.PI / 4.0 + i * Math.PI / 2.0;
                int x = this.cx + (int) Math.round(Math.cos(a) * 8.0);
                int z = this.cz + (int) Math.round(Math.sin(a) * 8.0);
                // eine Säule aus Schlammziegeln, oben eine Schale aus Seelenerde, darin brennt Seelenfeuer (ohne Rauch)
                this.set(x, this.f - 1, z, Blocks.MUD_BRICKS);
                this.set(x, this.f, z, Blocks.MUD_BRICK_WALL.defaultBlockState());
                this.set(x, this.f + 1, z, Blocks.SOUL_SOIL);
                this.set(x, this.f + 2, z, Blocks.SOUL_FIRE);
                for (Direction d : Direction.Plane.HORIZONTAL) {
                    this.set(x + d.getStepX(), this.f + 1, z + d.getStepZ(), Blocks.MUD_BRICK_SLAB.defaultBlockState()
                            .setValue(SlabBlock.TYPE, SlabType.TOP));
                }
            }
        }

        /** Halb versunkene Knochen und ein paar Schädel im Pferch. */
        private void bones() {
            for (int x = this.box.minX(); x <= this.box.maxX(); x++) {
                for (int z = this.box.minZ(); z <= this.box.maxZ(); z++) {
                    double d = this.dist(x, z);
                    if (d < 4.0 || d > ARENA - 0.5 || !this.in(x, this.f, z)) {
                        continue;
                    }
                    double r = this.rand(x, z, 51);
                    if (r < 0.02) {
                        this.set(x, this.f - 1, z, Blocks.BONE_BLOCK.defaultBlockState().setValue(RotatedPillarBlock.AXIS,
                                this.rand(x, z, 53) < 0.5 ? Direction.Axis.X : Direction.Axis.Z));
                    } else if (r < 0.026 && this.get(x, this.f, z).isAir()) {
                        this.set(x, this.f, z, Blocks.SKELETON_SKULL.defaultBlockState().setValue(SkullBlock.ROTATION, (int) (this.rand(x, z, 55) * 16)));
                    } else if (r < 0.05 && this.get(x, this.f, z).isAir() && this.get(x, this.f - 1, z).is(ShadowBlocks.SHADOW_SOIL.get())) {
                        this.set(x, this.f, z, ShadowBlocks.SHADOW_SPROUTS.get());
                    }
                }
            }
        }

        // ------------------------------------------------------------------ Stege

        /** Liegt (x, z) auf einem der Stege (Süden, Osten, Westen bis zur Plattform; Norden bis zur Hütte), plus {@code margin}? */
        private boolean onPath(int x, int z, int margin) {
            int dx = x - this.cx;
            int dz = z - this.cz;
            // Süden, Osten, Westen: drei breit bis PATH_END, dort eine Plattform (5 × 5)
            if (Math.abs(dx) <= 1 + margin && dz > ARENA && dz <= PATH_END + 3 + margin) {
                return true;
            }
            if (Math.abs(dz) <= 1 + margin && Math.abs(dx) > ARENA && Math.abs(dx) <= PATH_END + 3 + margin) {
                return true;
            }
            if (dz > PATH_END - 2 - margin && dz <= PATH_END + 2 + margin && Math.abs(dx) <= 2 + margin) {
                return true;
            }
            if (Math.abs(dx) > PATH_END - 2 - margin && Math.abs(dx) <= PATH_END + 2 + margin && Math.abs(dz) <= 2 + margin) {
                return true;
            }
            // Norden: zur Hütte
            return Math.abs(dx) <= 1 + margin && dz < -ARENA && dz >= -(HUT - 3) - margin;
        }

        private void paths() {
            BlockState plank = ShadowBlocks.SHADOW_PLANKS.base().get().defaultBlockState();
            BlockState slab = ShadowBlocks.SHADOW_PLANKS.slab().get().defaultBlockState().setValue(SlabBlock.TYPE, SlabType.TOP);
            for (Direction d : new Direction[] {Direction.SOUTH, Direction.EAST, Direction.WEST, Direction.NORTH}) {
                Direction side = d.getClockWise();
                int end = d == Direction.NORTH ? HUT - 3 : PATH_END;
                for (int along = PEN; along <= end + (d == Direction.NORTH ? 0 : 2); along++) {
                    boolean platform = d != Direction.NORTH && along >= end - 2;
                    int half = platform ? 2 : 1;
                    for (int across = -half; across <= half; across++) {
                        int x = this.cx + d.getStepX() * along + side.getStepX() * across;
                        int z = this.cz + d.getStepZ() * along + side.getStepZ() * across;
                        if (this.dist(x, z) <= PEN + 1.0) {
                            continue;
                        }
                        this.set(x, this.f - 1, z, (along + across) % 5 == 0 && !platform ? slab : plank);
                        // Stelzen bis auf den Grund
                        if ((Math.abs(across) == half) && along % 3 == 0 || platform && Math.abs(across) == half && Math.abs(along - end) == 2) {
                            this.stilt(x, z);
                        }
                    }
                    // Geländer: an beiden Seiten, alle vier Blöcke ein Pfosten mit Laterne
                    if (!platform && along > PEN + 1) {
                        for (int s : new int[] {-2, 2}) {
                            int x = this.cx + d.getStepX() * along + side.getStepX() * s;
                            int z = this.cz + d.getStepZ() * along + side.getStepZ() * s;
                            if (along % 4 == 0) {
                                this.set(x, this.f - 1, z, ShadowBlocks.SHADOW_LOG.get());
                                this.set(x, this.f, z, ShadowBlocks.SHADOW_LOG.get());
                                this.set(x, this.f + 1, z, Blocks.SOUL_LANTERN);
                                this.stilt(x, z);
                            } else {
                                this.set(x, this.f - 1, z, slab);
                                this.set(x, this.f, z, ShadowBlocks.SHADOW_FENCE.get().defaultBlockState()
                                        .setValue(d.getAxis() == Direction.Axis.Z ? CrossCollisionBlock.NORTH : CrossCollisionBlock.EAST, true)
                                        .setValue(d.getAxis() == Direction.Axis.Z ? CrossCollisionBlock.SOUTH : CrossCollisionBlock.WEST, true));
                            }
                        }
                    }
                }
                if (d != Direction.NORTH) {
                    // am Ende der Plattform zwei Laternenpfosten
                    for (int s : new int[] {-2, 2}) {
                        int x = this.cx + d.getStepX() * (end + 2) + side.getStepX() * s;
                        int z = this.cz + d.getStepZ() * (end + 2) + side.getStepZ() * s;
                        this.set(x, this.f, z, ShadowBlocks.SHADOW_LOG.get());
                        this.set(x, this.f + 1, z, ShadowBlocks.SHADOW_FENCE.get());
                        this.set(x, this.f + 2, z, Blocks.SOUL_LANTERN);
                    }
                }
            }
        }

        /** Eine Stelze aus Schattenholz vom Steg bis auf den Grund. */
        private void stilt(int x, int z) {
            for (int y = this.f - 2; y > this.f - 14; y--) {
                if (!this.in(x, y, z)) {
                    return;
                }
                BlockState s = this.get(x, y, z);
                if (LairContext.isGround(s) && !s.is(ShadowBlocks.SHADOW_PLANKS.base().get())) {
                    return;
                }
                this.set(x, y, z, ShadowBlocks.SHADOW_LOG.get());
            }
        }

        // ------------------------------------------------------------------ die Hütte

        private boolean onHut(int x, int z, int margin) {
            int dx = x - this.cx;
            int dz = z - this.cz + HUT;
            return Math.abs(dx) <= 4 + margin && Math.abs(dz) <= 4 + margin;
        }

        /**
         * Die Hütte des Hirten (7 × 7, Tür nach Süden zum Pferch): auf Stelzen, Eckpfosten aus Schattenholz, Wände aus Bohlen
         * mit je einem Fenster, ein steiles Giebeldach; innen ein Heulager, ein Fass mit einer Laterne, Kerzen, ein Block
         * Dämmerquarz und die Truhe.
         */
        private void hut(RandomSource random, @Nullable SoulPenPiece piece) {
            int hz = this.cz - HUT;
            BlockState plank = ShadowBlocks.SHADOW_PLANKS.base().get().defaultBlockState();
            for (int dx = -3; dx <= 3; dx++) {
                for (int dz = -3; dz <= 3; dz++) {
                    int x = this.cx + dx;
                    int z = hz + dz;
                    this.set(x, this.f - 1, z, plank);
                    boolean corner = Math.abs(dx) == 3 && Math.abs(dz) == 3;
                    if (corner || (Math.abs(dx) == 3 && dz == 0) || (Math.abs(dz) == 3 && dx == 0 && dz < 0)) {
                        this.stilt(x, z);
                    }
                    for (int y = this.f; y <= this.f + 3; y++) {
                        if (corner) {
                            this.set(x, y, z, ShadowBlocks.SHADOW_LOG.get());
                        } else if (Math.abs(dx) == 3 || Math.abs(dz) == 3) {
                            boolean window = y == this.f + 1 && (Math.abs(dx) == 3 && dz == 0 || dz == -3 && Math.abs(dx) == 1);
                            boolean beam = y == this.f + 3;
                            if (window) {
                                this.set(x, y, z, Blocks.GLASS_PANE.defaultBlockState()
                                        .setValue(CrossCollisionBlock.NORTH, Math.abs(dx) == 3).setValue(CrossCollisionBlock.SOUTH, Math.abs(dx) == 3)
                                        .setValue(CrossCollisionBlock.EAST, Math.abs(dz) == 3).setValue(CrossCollisionBlock.WEST, Math.abs(dz) == 3));
                            } else if (beam) {
                                this.set(x, y, z, ShadowBlocks.SHADOW_LOG.get().defaultBlockState().setValue(RotatedPillarBlock.AXIS,
                                        Math.abs(dz) == 3 ? Direction.Axis.X : Direction.Axis.Z));
                            } else {
                                this.set(x, y, z, plank);
                            }
                        } else {
                            this.clear(x, y, z);
                        }
                    }
                }
            }
            // die Tür (nach Süden)
            BlockState door = ShadowBlocks.SHADOW_DOOR.get().defaultBlockState().setValue(DoorBlock.FACING, Direction.NORTH)
                    .setValue(DoorBlock.HINGE, DoorHingeSide.LEFT);
            this.set(this.cx, this.f, hz + 3, door.setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER));
            this.set(this.cx, this.f + 1, hz + 3, door.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER));
            this.set(this.cx, this.f + 2, hz + 3, plank);
            this.set(this.cx, this.f + 3, hz + 4, Blocks.SOUL_LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));
            // das Dach: steile Giebel (First in Ost-West-Richtung), die Giebelwände aus Bohlen
            BlockState stairs = ShadowBlocks.SHADOW_PLANKS.stairs().get().defaultBlockState();
            for (int k = 0; k <= 4; k++) {
                int y = this.f + 4 + k;
                for (int dx = -4; dx <= 4; dx++) {
                    int x = this.cx + dx;
                    if (k < 4) {
                        this.set(x, y, hz - 4 + k, stairs.setValue(StairBlock.FACING, Direction.SOUTH));
                        this.set(x, y, hz + 4 - k, stairs.setValue(StairBlock.FACING, Direction.NORTH));
                        if (Math.abs(dx) == 3) {
                            for (int dz = -3 + k; dz <= 3 - k; dz++) {
                                this.set(x, y, hz + dz, plank);
                            }
                        } else if (Math.abs(dx) < 3) {
                            for (int dz = -3 + k; dz <= 3 - k; dz++) {
                                this.clear(x, y, hz + dz);
                            }
                        }
                    } else {
                        this.set(x, y, hz, ShadowBlocks.SHADOW_PLANKS.slab().get().defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM));
                    }
                }
            }
            // innen: Heulager, Fass mit Laterne, Kerzen, Dämmerquarz, die Truhe
            this.set(this.cx - 2, this.f, hz - 2, Blocks.HAY_BLOCK.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Z));
            this.set(this.cx - 2, this.f, hz - 1, Blocks.HAY_BLOCK.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Z));
            this.set(this.cx - 1, this.f, hz - 2, Blocks.CARPET.pick(net.minecraft.world.item.DyeColor.WHITE));
            this.set(this.cx + 2, this.f, hz + 1, Blocks.BARREL.defaultBlockState().setValue(BarrelBlock.FACING, Direction.UP));
            this.set(this.cx + 2, this.f + 1, hz + 1, Blocks.SOUL_LANTERN);
            this.set(this.cx - 2, this.f, hz + 2, ShadowGear.DUSK_QUARTZ_BLOCK.get());
            this.set(this.cx - 2, this.f + 1, hz + 2, Blocks.CANDLE.defaultBlockState().setValue(CandleBlock.CANDLES, 3).setValue(CandleBlock.LIT, true));
            for (int y = this.f + 3; y <= this.f + 7; y++) {
                this.set(this.cx, y, hz, Blocks.IRON_CHAIN.defaultBlockState());
            }
            this.set(this.cx, this.f + 2, hz, Blocks.SOUL_LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));
            this.chest(new BlockPos(this.cx + 2, this.f, hz - 2), Direction.SOUTH, random, piece);
        }

        private void chest(BlockPos pos, Direction facing, RandomSource random, @Nullable SoulPenPiece piece) {
            if (!this.box.isInside(pos)) {
                return;
            }
            BlockState state = Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, facing);
            if (piece != null) {
                piece.createChest(this.level, this.box, random, pos, ModLootTables.SOUL_PEN_CHEST, state);
            } else {
                this.level.setBlock(pos, state, Block.UPDATE_CLIENTS);
                if (this.level.getBlockEntity(pos) instanceof ChestBlockEntity be) {
                    be.setLootTable(ModLootTables.SOUL_PEN_CHEST, random.nextLong());
                }
            }
        }
    }
}
