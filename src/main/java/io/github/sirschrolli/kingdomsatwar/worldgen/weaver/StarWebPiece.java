package io.github.sirschrolli.kingdomsatwar.worldgen.weaver;

import io.github.sirschrolli.kingdomsatwar.block.voidrealm.WeaverNestBlockEntity;
import io.github.sirschrolli.kingdomsatwar.loot.ModLootTables;
import io.github.sirschrolli.kingdomsatwar.registry.StarWeaver;
import io.github.sirschrolli.kingdomsatwar.registry.VoidBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.VoidGear;
import io.github.sirschrolli.kingdomsatwar.worldgen.lair.LairContext;
import io.github.sirschrolli.kingdomsatwar.worldgen.lair.ModStructures;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import org.jspecify.annotations.Nullable;

/**
 * Das Sternennetz (seit 0.99), Chunk für Chunk gebaut – frei schwebend in den leeren Weiten der Leere:
 * <ol>
 *     <li>Die <b>Netzplatte</b>: eine runde Scheibe, auf der man geht – acht Speichen und vier Ringe aus leuchtender
 *     <b>Sternenseide</b>, dazwischen <b>Sternengewebe</b>, durch das man ins Nichts hinabsieht. Am Rand wird das Netz offen
 *     (nur noch Speichen und der äußere Ring – Vorsicht!).</li>
 *     <li>In der Mitte das <b>Sternengelege</b>; darüber hängt die Sternenweberin in Andacht an ihrem Faden.</li>
 *     <li>Sechs <b>Ankerfelsen</b> ringsum (Leerenstein mit Moos, Kristallen und Adern aus Himmelseisen), an denen sechs
 *     durchhängende <b>Taue</b> aus Sternenseide das Netz halten; hoch über der Mitte die <b>Sternenkrone</b>, ein kleiner
 *     Fels, von dem Fäden zu allen Ankern hinabführen.</li>
 *     <li>An den Tauen und unter dem Rand hängen <b>Sternenkokons</b> (zerschlagen geben sie frei, was sich verfing), in den
 *     Ecken spannt sich <b>Sternennetz</b>; auf dem größten Ankerfelsen steht eine Truhe.</li>
 * </ol>
 */
public class StarWebPiece extends StructurePiece {
    /** Halbmesser des festen Netzes (Sternengewebe zwischen Speichen und Ringen) … */
    public static final double DISC = 13.2;
    /** … und des offenen Rands (nur Speichen und der äußere Ring). */
    public static final double EDGE = 15.5;
    /** So hoch über der Netzplatte hängt die Sternenkrone. */
    public static final int CROWN = 22;
    /** Ankerfelsen: so weit von der Mitte (von … bis). */
    public static final int ANCHOR_MIN = 24;
    public static final int ANCHOR_MAX = 29;
    /** So weit reicht das ganze Bauwerk (waagerecht) … */
    public static final int REACH = 35;
    /** … und so tief unter / hoch über die Netzplatte. */
    public static final int BELOW = 19;
    public static final int ABOVE = 27;
    private static final int ANCHORS = 6;
    private static final int SPOKES = 8;
    private static final double[] RINGS = {4.0, 7.5, 11.0, DISC};

    private final BlockPos center;
    private final long seed;

    public StarWebPiece(BlockPos center, long seed) {
        super(ModStructures.STAR_WEB_PIECE.get(), 0, makeBox(center));
        this.center = center;
        this.seed = seed;
    }

    public StarWebPiece(CompoundTag tag) {
        super(ModStructures.STAR_WEB_PIECE.get(), tag);
        this.center = new BlockPos(tag.getIntOr("CX", 0), tag.getIntOr("CY", 120), tag.getIntOr("CZ", 0));
        this.seed = tag.getLongOr("Seed", 0L);
    }

    public static BoundingBox makeBox(BlockPos c) {
        return new BoundingBox(c.getX() - REACH - 1, c.getY() - BELOW, c.getZ() - REACH - 1, c.getX() + REACH + 1, c.getY() + ABOVE,
                c.getZ() + REACH + 1);
    }

    /** Wo das Gelege liegt (in der Mitte, auf der Netzplatte) – dort ist auch der Platz der Weberin. */
    public static BlockPos nest(BlockPos center) {
        return center;
    }

    public BlockPos center() {
        return this.center;
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        tag.putInt("CX", this.center.getX());
        tag.putInt("CY", this.center.getY());
        tag.putInt("CZ", this.center.getZ());
        tag.putLong("Seed", this.seed);
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator, RandomSource random,
            BoundingBox chunkBB, net.minecraft.world.level.ChunkPos chunkPos, BlockPos referencePos) {
        build(level, chunkBB, this.center, this.seed, random, this);
    }

    /** Baut das Netz (für die Welt Chunk für Chunk, für Tests in einem Zug). {@code piece} legt die Truhe an (null: direkt). */
    public static void build(WorldGenLevel level, BoundingBox box, BlockPos center, long seed, RandomSource random, @Nullable StarWebPiece piece) {
        new Builder(level, box, center, seed).run(random, piece);
    }

    /** Ein Ankerfels: Mitte und Größe. */
    record Anchor(double x, double y, double z, double size) {}

    // =====================================================================================

    private static final class Builder {
        final WorldGenLevel level;
        final BoundingBox box;
        final LairContext ctx;
        final int cx;
        final int cz;
        final int f;
        final double turn;
        final Anchor[] anchors = new Anchor[ANCHORS];
        final Anchor crown;

        Builder(WorldGenLevel level, BoundingBox box, BlockPos center, long seed) {
            this.level = level;
            this.box = box;
            this.ctx = new LairContext(level, box, center, seed);
            this.cx = center.getX();
            this.cz = center.getZ();
            this.f = center.getY();
            this.turn = this.ctx.rand(1, 2, 3) * Math.PI / 4.0;
            for (int k = 0; k < ANCHORS; k++) {
                double a = this.turn + k * Mth.TWO_PI / ANCHORS + (this.ctx.rand(k, 7, 11) - 0.5) * 0.35;
                double r = ANCHOR_MIN + this.ctx.rand(k, 8, 13) * (ANCHOR_MAX - ANCHOR_MIN);
                double dy = -7.0 + this.ctx.rand(k, 9, 17) * 18.0;
                double size = 3.0 + this.ctx.rand(k, 10, 19) * 1.6 + (k == 0 ? 0.8 : 0.0);
                this.anchors[k] = new Anchor(this.cx + 0.5 + Math.cos(a) * r, this.f + dy, this.cz + 0.5 + Math.sin(a) * r, size);
            }
            this.crown = new Anchor(this.cx + 0.5, this.f + CROWN, this.cz + 0.5, 2.6);
        }

        boolean in(int x, int y, int z) {
            return this.box.isInside(x, y, z) && y > this.level.getMinY() && y < this.level.getMaxY();
        }

        void set(int x, int y, int z, BlockState state) {
            if (this.in(x, y, z)) {
                this.level.setBlock(new BlockPos(x, y, z), state, Block.UPDATE_CLIENTS);
            }
        }

        void setIfOpen(int x, int y, int z, BlockState state) {
            if (this.in(x, y, z) && this.level.getBlockState(new BlockPos(x, y, z)).canBeReplaced()) {
                this.level.setBlock(new BlockPos(x, y, z), state, Block.UPDATE_CLIENTS);
            }
        }

        /** Der Block an dieser Stelle – außerhalb des Chunks, der gerade entsteht, gilt Luft (dort darf nichts gelesen werden). */
        BlockState get(int x, int y, int z) {
            if (!this.box.isInside(x, y, z)) {
                return Blocks.AIR.defaultBlockState();
            }
            return this.level.getBlockState(new BlockPos(x, y, z));
        }

        double dist(double x, double z) {
            double dx = x + 0.5 - (this.cx + 0.5);
            double dz = z + 0.5 - (this.cz + 0.5);
            return Math.sqrt(dx * dx + dz * dz);
        }

        void run(RandomSource random, @Nullable StarWebPiece piece) {
            for (int k = 0; k < ANCHORS; k++) {
                this.rock(this.anchors[k], 100 + k, k == 0);
            }
            this.rock(this.crown, 99, false);
            this.disc();
            for (int k = 0; k < ANCHORS; k++) {
                this.rope(k);
                this.canopy(k);
            }
            this.cocoons();
            this.webs();
            this.nest();
            this.chest(random, piece);
        }

        // ------------------------------------------------------------------ die Netzplatte

        /** Auf einer Speiche? (Abstand von der nächsten der acht Linien durch die Mitte.) */
        private boolean onSpoke(int x, int z) {
            double dx = x - this.cx;
            double dz = z - this.cz;
            double r = Math.sqrt(dx * dx + dz * dz);
            if (r < 0.5) {
                return true;
            }
            double a = Math.atan2(dz, dx) - this.turn;
            double step = Mth.TWO_PI / SPOKES;
            double nearest = Math.round(a / step) * step;
            return Math.abs(Math.sin(a - nearest)) * r < 0.62;
        }

        private boolean onRing(double d) {
            for (double r : RINGS) {
                if (Math.abs(d - r) < 0.5) {
                    return true;
                }
            }
            return Math.abs(d - EDGE) < 0.55;
        }

        /** Die Scheibe: Speichen und Ringe aus Sternenseide, dazwischen Sternengewebe; am Rand offen. */
        private void disc() {
            BlockState silk = StarWeaver.STAR_SILK.get().defaultBlockState();
            BlockState weave = StarWeaver.STAR_WEAVE.get().defaultBlockState();
            int r = (int) Math.ceil(EDGE) + 1;
            for (int x = this.cx - r; x <= this.cx + r; x++) {
                for (int z = this.cz - r; z <= this.cz + r; z++) {
                    double d = this.dist(x, z);
                    if (d > EDGE + 0.6) {
                        continue;
                    }
                    boolean line = d < 1.8 || this.onSpoke(x, z) || this.onRing(d);
                    if (d <= DISC + 0.4) {
                        this.set(x, this.f - 1, z, line ? silk : weave);
                    } else if (line) {
                        this.set(x, this.f - 1, z, silk);
                    }
                    // über der Platte bleibt es frei
                    for (int y = this.f; y <= this.f + 4; y++) {
                        if (this.in(x, y, z) && !this.get(x, y, z).isAir()) {
                            this.set(x, y, z, Blocks.AIR.defaultBlockState());
                        }
                    }
                }
            }
        }

        // ------------------------------------------------------------------ Ankerfelsen und Krone

        /** Ein Fels: oben flach mit Moos, unten spitz zulaufend; Leerenstein mit Adern aus Himmelseisen und Kristallen. */
        private void rock(Anchor a, int salt, boolean big) {
            double s = a.size();
            int r = (int) Math.ceil(s * 1.3) + 1;
            int top = (int) Math.ceil(s * 0.8);
            int bottom = (int) Math.ceil(s * 2.3);
            int ax = Mth.floor(a.x());
            int ay = Mth.floor(a.y());
            int az = Mth.floor(a.z());
            BlockState stone = VoidBlocks.VOID_STONE.base().get().defaultBlockState();
            for (int x = ax - r; x <= ax + r; x++) {
                for (int z = az - r; z <= az + r; z++) {
                    if (!this.ctx.near(x, z, 0)) {
                        continue;
                    }
                    double dx = x + 0.5 - a.x();
                    double dz = z + 0.5 - a.z();
                    double flat = Math.sqrt(dx * dx + dz * dz);
                    double wob = 0.8 + 0.4 * this.ctx.angleNoise(Math.atan2(dz, dx), 3.0, salt);
                    int highest = Integer.MIN_VALUE;
                    for (int y = ay - bottom; y <= ay + top; y++) {
                        double dy = y + 0.5 - a.y();
                        double rr = dy >= 0.0 ? s * Math.sqrt(Math.max(0.0, 1.0 - (dy / (s * 0.8)) * (dy / (s * 0.8))))
                                : s * Math.max(0.0, 1.0 + dy / (s * 2.3));
                        if (flat > rr * wob) {
                            continue;
                        }
                        double n = this.ctx.rand(x * 31 + y, z, salt);
                        BlockState state = n < 0.035 ? VoidGear.SKY_IRON_ORE.get().defaultBlockState()
                                : n < 0.06 ? VoidBlocks.VOID_CRYSTAL.get().defaultBlockState() : stone;
                        this.set(x, y, z, state);
                        highest = Math.max(highest, y);
                    }
                    if (highest != Integer.MIN_VALUE) {
                        this.set(x, highest, z, VoidBlocks.VOID_MOSS.get().defaultBlockState());
                        double deco = this.ctx.rand(x, z, salt + 7);
                        if (deco < 0.12) {
                            this.setIfOpen(x, highest + 1, z, VoidBlocks.VOID_CRYSTAL_CLUSTER.get().defaultBlockState());
                        } else if (deco < 0.2) {
                            this.setIfOpen(x, highest + 1, z, VoidBlocks.VOID_BLOOM.get().defaultBlockState());
                        }
                    }
                }
            }
        }

        // ------------------------------------------------------------------ Taue und Fäden

        /** Eine durchhängende Linie aus {@code state} von a nach b (sag: so tief hängt sie in der Mitte durch). */
        private void line(double x0, double y0, double z0, double x1, double y1, double z1, double sag, BlockState state, boolean thick) {
            double len = Math.sqrt((x1 - x0) * (x1 - x0) + (y1 - y0) * (y1 - y0) + (z1 - z0) * (z1 - z0));
            int steps = (int) Math.ceil(len * 3.0);
            for (int i = 0; i <= steps; i++) {
                double t = (double) i / steps;
                double x = Mth.lerp(t, x0, x1);
                double z = Mth.lerp(t, z0, z1);
                double y = Mth.lerp(t, y0, y1) - sag * 4.0 * t * (1.0 - t);
                int bx = Mth.floor(x);
                int by = Mth.floor(y);
                int bz = Mth.floor(z);
                if (!this.ctx.near(bx, bz, 0)) {
                    continue;
                }
                this.setIfOpen(bx, by, bz, state);
                if (thick) {
                    this.setIfOpen(bx, by + 1, bz, state);
                }
            }
        }

        /** Ein Tau vom Rand der Platte zum Ankerfels (dick, hängt durch). */
        private void rope(int k) {
            Anchor a = this.anchors[k];
            double ang = Math.atan2(a.z() - (this.cz + 0.5), a.x() - (this.cx + 0.5));
            double x0 = this.cx + 0.5 + Math.cos(ang) * (EDGE - 0.3);
            double z0 = this.cz + 0.5 + Math.sin(ang) * (EDGE - 0.3);
            double dx = a.x() - x0;
            double dz = a.z() - z0;
            double d = Math.sqrt(dx * dx + dz * dz);
            double x1 = a.x() - dx / d * a.size() * 0.7;
            double z1 = a.z() - dz / d * a.size() * 0.7;
            double sag = 1.5 + this.ctx.rand(k, 21, 23) * 1.5;
            this.line(x0, this.f - 0.5, z0, x1, a.y() + a.size() * 0.3, z1, sag, StarWeaver.STAR_SILK.get().defaultBlockState(), true);
        }

        /** Ein Faden von der Sternenkrone hinab zum Ankerfels (dünn, kaum durchhängend). */
        private void canopy(int k) {
            Anchor a = this.anchors[k];
            double ang = Math.atan2(a.z() - this.crown.z(), a.x() - this.crown.x());
            double x0 = this.crown.x() + Math.cos(ang) * 1.6;
            double z0 = this.crown.z() + Math.sin(ang) * 1.6;
            this.line(x0, this.crown.y() - 1.0, z0, a.x(), a.y() + a.size() * 0.75, a.z(), 0.8, StarWeaver.STAR_SILK.get().defaultBlockState(),
                    false);
        }

        // ------------------------------------------------------------------ Kokons, Netze, Gelege, Truhe

        /**
         * Kokons unter den Tauen und unter dem äußeren Ring: an gut jeder zwölften Stelle und dazu an jedem Tau zwei feste Stellen
         * (ein Drittel und zwei Drittel des Wegs zum Ankerfels) – so hängen in jedem Netz mindestens ein Dutzend. Jede Säule
         * entscheidet für sich (die Welt entsteht Chunk für Chunk).
         */
        private void cocoons() {
            java.util.Set<Long> fixed = new java.util.HashSet<>();
            for (int k = 0; k < ANCHORS; k++) {
                Anchor a = this.anchors[k];
                double ang = Math.atan2(a.z() - (this.cz + 0.5), a.x() - (this.cx + 0.5));
                double x0 = this.cx + 0.5 + Math.cos(ang) * (EDGE - 0.3);
                double z0 = this.cz + 0.5 + Math.sin(ang) * (EDGE - 0.3);
                double dx = a.x() - x0;
                double dz = a.z() - z0;
                double d = Math.sqrt(dx * dx + dz * dz);
                double x1 = a.x() - dx / d * a.size() * 0.7;
                double z1 = a.z() - dz / d * a.size() * 0.7;
                for (double t : new double[] {0.33, 0.66}) {
                    fixed.add(BlockPos.asLong(Mth.floor(Mth.lerp(t, x0, x1)), 0, Mth.floor(Mth.lerp(t, z0, z1))));
                }
            }
            BlockState cocoon = StarWeaver.STAR_COCOON.get().defaultBlockState();
            BlockState silk = StarWeaver.STAR_SILK.get().defaultBlockState();
            int r = REACH;
            for (int x = this.cx - r; x <= this.cx + r; x++) {
                for (int z = this.cz - r; z <= this.cz + r; z++) {
                    if (!this.ctx.near(x, z, 0)) {
                        continue;
                    }
                    double d = this.dist(x, z);
                    boolean must = fixed.contains(BlockPos.asLong(x, 0, z));
                    if (!must && (d < DISC + 1.0 || d > ANCHOR_MIN - 3.0 || this.ctx.rand(x, z, 41) > 0.07)) {
                        continue;
                    }
                    // unter dem untersten Block Sternenseide dieser Säule (am Tau oder am Rand)
                    for (int y = this.f + 6; y > this.f - BELOW + 2; y--) {
                        if (this.in(x, y, z) && this.get(x, y, z).equals(silk) && this.get(x, y - 1, z).isAir() && this.in(x, y - 1, z)) {
                            this.set(x, y - 1, z, cocoon);
                            break;
                        }
                    }
                }
            }
        }

        /** Sternennetz in den Lücken am Rand und an den Felsen. */
        private void webs() {
            BlockState web = StarWeaver.STAR_WEB.get().defaultBlockState();
            int r = (int) Math.ceil(EDGE) + 1;
            for (int x = this.cx - r; x <= this.cx + r; x++) {
                for (int z = this.cz - r; z <= this.cz + r; z++) {
                    double d = this.dist(x, z);
                    if (d > DISC + 0.6 && d < EDGE - 0.6 && !this.onSpoke(x, z) && this.ctx.rand(x, z, 43) < 0.22) {
                        this.setIfOpen(x, this.f - 1, z, web);
                    }
                }
            }
            for (int k = 0; k < ANCHORS; k++) {
                Anchor a = this.anchors[k];
                for (int i = 0; i < 3; i++) {
                    double ang = this.ctx.rand(k, i, 47) * Mth.TWO_PI;
                    int x = Mth.floor(a.x() + Math.cos(ang) * (a.size() + 0.6));
                    int z = Mth.floor(a.z() + Math.sin(ang) * (a.size() + 0.6));
                    if (this.ctx.near(x, z, 0)) {
                        this.setIfOpen(x, Mth.floor(a.y()), z, web);
                    }
                }
            }
        }

        private void nest() {
            BlockPos pos = StarWebPiece.nest(new BlockPos(this.cx, this.f, this.cz));
            if (!this.box.isInside(pos)) {
                return;
            }
            this.level.setBlock(pos, StarWeaver.WEAVER_NEST.get().defaultBlockState(), Block.UPDATE_CLIENTS);
            if (this.level.getBlockEntity(pos) instanceof WeaverNestBlockEntity be) {
                be.setHome(pos);
            }
        }

        /** Eine Truhe auf dem größten Ankerfels (von einem, der es nicht schaffte). */
        private void chest(RandomSource random, @Nullable StarWebPiece piece) {
            Anchor a = this.anchors[0];
            int x = Mth.floor(a.x());
            int z = Mth.floor(a.z());
            if (!this.ctx.near(x, z, 0)) {
                return;
            }
            int y = Mth.floor(a.y()) + (int) Math.ceil(a.size() * 0.8) + 1;
            while (y > Mth.floor(a.y()) - 2 && this.get(x, y - 1, z).isAir()) {
                y--;
            }
            BlockPos pos = new BlockPos(x, y, z);
            if (!this.box.isInside(pos)) {
                return;
            }
            Direction facing = Direction.fromYRot(Math.toDegrees(Math.atan2(this.cz - z, this.cx - x)) - 90.0);
            BlockState state = Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, facing);
            if (piece != null) {
                piece.createChest(this.level, this.box, random, pos, ModLootTables.STAR_WEB_CHEST, state);
            } else {
                this.level.setBlock(pos, state, Block.UPDATE_CLIENTS);
                if (this.level.getBlockEntity(pos) instanceof ChestBlockEntity be) {
                    be.setLootTable(ModLootTables.STAR_WEB_CHEST, random.nextLong());
                }
            }
        }
    }
}
