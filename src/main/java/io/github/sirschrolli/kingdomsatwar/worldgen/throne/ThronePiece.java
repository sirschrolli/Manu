package io.github.sirschrolli.kingdomsatwar.worldgen.throne;

import io.github.sirschrolli.kingdomsatwar.block.voidrealm.ThroneSealBlockEntity;
import io.github.sirschrolli.kingdomsatwar.registry.VoidBlocks;
import io.github.sirschrolli.kingdomsatwar.worldgen.lair.LairContext;
import io.github.sirschrolli.kingdomsatwar.worldgen.lair.ModStructures;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

/**
 * Baut den Thron der Alten Chunk für Chunk (alle Formen ergeben sich aus Mitte und Startwert):
 * <ol>
 *     <li>Ringsum wird die Leere freigeräumt – ein weiter, leerer Raum, in dem der Platz allein schwebt.</li>
 *     <li>Der Platz: eine Scheibe aus Sternenmarmor (Radius 22) mit dunklen Bändern, acht Strahlen mit leuchtenden
 *     Kristallen, einem Kristallring und einem Siegel in der Mitte, einer niedrigen, stellenweise zerbrochenen Brüstung
 *     mit Kristallen und einem Eingang im Süden.</li>
 *     <li>Darunter ein zerklüfteter Kegel aus Leerenstein, aus dem Kristalle und Sternenranken ins Nichts hängen.</li>
 *     <li>Sechs Säulen (zwei zerbrochen, ihre Trommeln schweben über dem Stumpf), vier Kristalllampen.</li>
 *     <li>Im Norden das Podest in drei Stufen mit dem Thron, dahinter ein Kristallturm, zu beiden Seiten Obelisken;
 *     darunter verborgen das Thronsiegel.</li>
 *     <li>Im Süden schwebende Trittsteine, ringsum treibende Trümmer.</li>
 * </ol>
 */
public class ThronePiece extends StructurePiece {
    /** Höhe des Bodens (der Platz liegt darauf, ein Block höher). */
    public static final int FLOOR = 118;
    static final double R = 22.0;
    static final double R_CLEAR = 40.0;
    static final int CLEAR_DOWN = 36;
    static final int CLEAR_UP = 30;
    static final int DAIS_Z = -16;
    /** Wo der Thron steht (Norden, relativ zur Mitte); das Siegel liegt darunter, drei Blöcke unter dem Boden. */
    public static final int THRONE_Z = -17;
    static final double[] PILLARS = {22.5, 67.5, 112.5, 157.5, 202.5, 337.5};

    private final BlockPos center;
    private final long seed;

    public ThronePiece(BlockPos center, long seed) {
        super(ModStructures.VOID_THRONE_PIECE.get(), 0, makeBox(center));
        this.center = center;
        this.seed = seed;
    }

    public ThronePiece(CompoundTag tag) {
        super(ModStructures.VOID_THRONE_PIECE.get(), tag);
        this.center = new BlockPos(tag.getIntOr("CX", 0), tag.getIntOr("CY", FLOOR), tag.getIntOr("CZ", 0));
        this.seed = tag.getLongOr("Seed", 0L);
    }

    private static BoundingBox makeBox(BlockPos c) {
        int r = (int) R_CLEAR + 1;
        return new BoundingBox(c.getX() - r, c.getY() - CLEAR_DOWN, c.getZ() - r, c.getX() + r, c.getY() + CLEAR_UP, c.getZ() + r);
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        tag.putInt("CX", this.center.getX());
        tag.putInt("CY", this.center.getY());
        tag.putInt("CZ", this.center.getZ());
        tag.putLong("Seed", this.seed);
    }

    /** Mitte des Platzes, ein Block über dem Boden (dort, wo der König erwacht). */
    public BlockPos arenaCenter() {
        return this.center.above();
    }

    // =====================================================================================
    // Baustoffe
    // =====================================================================================

    private static BlockState s(Block block) {
        return block.defaultBlockState();
    }

    private static BlockState marble() {
        return s(VoidBlocks.ASTRAL_MARBLE.base().get());
    }

    private static BlockState marbleBricks() {
        return s(VoidBlocks.ASTRAL_MARBLE_BRICKS.base().get());
    }

    private static BlockState voidBricks() {
        return s(VoidBlocks.VOID_STONE_BRICKS.base().get());
    }

    private static BlockState polished() {
        return s(VoidBlocks.POLISHED_VOID_STONE.base().get());
    }

    private static BlockState voidStone() {
        return s(VoidBlocks.VOID_STONE.base().get());
    }

    private static BlockState crystal() {
        return s(VoidBlocks.VOID_CRYSTAL.get());
    }

    private static BlockState cluster() {
        return s(VoidBlocks.VOID_CRYSTAL_CLUSTER.get());
    }

    private static BlockState chiseled() {
        return s(VoidBlocks.CHISELED_ASTRAL_MARBLE.get());
    }

    private static BlockState pillar(Direction.Axis axis) {
        return VoidBlocks.ASTRAL_PILLAR.get().defaultBlockState().setValue(RotatedPillarBlock.AXIS, axis);
    }

    private static BlockState stairs(Direction facing) {
        return VoidBlocks.ASTRAL_MARBLE_BRICKS.stairs().get().defaultBlockState().setValue(StairBlock.FACING, facing)
                .setValue(StairBlock.HALF, Half.BOTTOM);
    }

    private static BlockState slab(SlabType type) {
        return VoidBlocks.ASTRAL_MARBLE_BRICKS.slab().get().defaultBlockState().setValue(SlabBlock.TYPE, type);
    }

    // =====================================================================================
    // Formen
    // =====================================================================================

    /** Wie tief der Kegel unter dem Platz an dieser Stelle hinabreicht (0 = gar nicht). */
    private int depth(LairContext ctx, int dx, int dz, double r) {
        if (r > R + 0.8) {
            return 0;
        }
        if (r > R) {
            return 2;
        }
        double inner = R - r;
        double d = 3.0 + Math.pow(inner, 1.15) * 0.95 + (ctx.noise(dx, dz, 0.16, 31) - 0.5) * 6.0;
        if (ctx.rand(dx, dz, 33) < 0.07) {
            d += 2.0 + ctx.rand(dz, dx, 35) * 5.0;                // Zacken
        }
        return (int) Math.round(d);
    }

    /** Muster des Bodens. */
    private BlockState floor(LairContext ctx, int dx, int dz, double r) {
        if (r > R - 1.0) {
            return polished();
        }
        if (r <= 2.5) {
            return chiseled();
        }
        if (r <= 3.5) {
            return crystal();
        }
        // acht Strahlen aus dunklem Stein, auf ihnen leuchtende Kristalle
        double a = Math.atan2(dz, dx);
        double sector = Math.round(a / (Math.PI / 4.0)) * (Math.PI / 4.0);
        double off = Math.abs(r * Math.sin(a - sector));
        if (off < 0.62 && r < 19.6) {
            int ri = (int) Math.round(r);
            return ri == 7 || ri == 14 ? crystal() : polished();
        }
        boolean worn = ctx.rand(dx, dz, 41) < 0.05;
        if (r >= 9.4 && r < 10.6 || r >= 19.6) {
            return worn ? s(VoidBlocks.CRACKED_VOID_STONE_BRICKS.get()) : voidBricks();
        }
        if (r >= 16.4 && r < 17.4) {
            return marbleBricks();
        }
        return worn ? marbleBricks() : marble();
    }

    // =====================================================================================
    // Bauen
    // =====================================================================================

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator, RandomSource random,
            BoundingBox chunkBB, ChunkPos chunkPos, BlockPos referencePos) {
        LairContext ctx = new LairContext(level, chunkBB, this.center, this.seed);
        int cx = this.center.getX();
        int cz = this.center.getZ();
        for (int x = chunkBB.minX(); x <= chunkBB.maxX(); x++) {
            for (int z = chunkBB.minZ(); z <= chunkBB.maxZ(); z++) {
                this.column(ctx, x, z, x - cx, z - cz);
            }
        }
        for (int i = 0; i < PILLARS.length; i++) {
            double a = Math.toRadians(PILLARS[i]);
            this.pillar(ctx, (int) Math.round(Math.cos(a) * 18.5), (int) Math.round(Math.sin(a) * 18.5), i == 1 || i == 4, i);
        }
        for (int k = 0; k < 4; k++) {
            double a = Math.toRadians(45.0 + 90.0 * k);
            this.lamp(ctx, (int) Math.round(Math.cos(a) * 12.5), (int) Math.round(Math.sin(a) * 12.5));
        }
        this.dais(ctx);
        this.throne(ctx);
        this.obelisk(ctx, -5, -20);
        this.obelisk(ctx, 5, -20);
        this.steppingStones(ctx);
        this.debris(ctx);
        this.seal(ctx);
    }

    private void column(LairContext ctx, int x, int z, int dx, int dz) {
        double r = Math.sqrt(dx * dx + dz * dz);
        if (r > R_CLEAR + 0.5) {
            return;
        }
        // den Raum ringsum freiräumen (ein flaches Ellipsoid)
        for (int y = FLOOR - CLEAR_DOWN; y <= FLOOR + CLEAR_UP; y++) {
            double dy = (y - FLOOR - 2) * (y < FLOOR ? 1.05 : 1.3);
            if (r * r + dy * dy <= R_CLEAR * R_CLEAR && !ctx.get(x, y, z).isAir()) {
                ctx.set(x, y, z, Blocks.AIR.defaultBlockState());
            }
        }
        int depth = this.depth(ctx, dx, dz, r);
        if (depth <= 0) {
            return;
        }
        // der Kegel darunter
        for (int k = 1; k <= depth; k++) {
            int y = FLOOR - k;
            BlockState state;
            if (k == 1) {
                state = voidBricks();
            } else if (k == 2 && r > R - 1.6) {
                state = polished();
            } else if (k > depth - 3 && ctx.rand(x * 5 + y, z, 43) < 0.4 || ctx.rand(x + y * 7, z, 45) < 0.035) {
                state = crystal();
            } else {
                state = voidStone();
            }
            ctx.set(x, y, z, state);
        }
        // unten hängen Sternenranken und Kristallspitzen
        int bottom = FLOOR - depth - 1;
        double h = ctx.rand(x, z, 47);
        if (h < 0.14) {
            int len = 2 + (int) (ctx.rand(z, x, 49) * 6);
            for (int i = 0; i < len; i++) {
                ctx.set(x, bottom - i, z, s(i == len - 1 ? VoidBlocks.STAR_VINES.get() : VoidBlocks.STAR_VINES_PLANT.get()));
            }
        } else if (h < 0.2) {
            ctx.set(x, bottom, z, crystal());
            if (ctx.rand(x, z, 51) < 0.5) {
                ctx.set(x, bottom - 1, z, crystal());
            }
        }
        if (r > R) {
            return;
        }
        // der Boden
        ctx.set(x, FLOOR, z, this.floor(ctx, dx, dz, r));
        // die Brüstung: niedrig, stellenweise zerbrochen, im Süden offen
        if (r > R - 1.0) {
            double a = Math.atan2(dz, dx);
            boolean entrance = Math.abs(Mth.wrapDegrees(Math.toDegrees(a) - 90.0)) < 9.0;
            boolean broken = ctx.angleNoise(a, 5.0, 53) < 0.28;
            if (!entrance && !broken) {
                ctx.set(x, FLOOR + 1, z, marbleBricks());
                int along = Mth.floor((a + Math.PI) * R);
                if (along % 9 == 0) {
                    ctx.set(x, FLOOR + 2, z, cluster());
                } else if (along % 9 == 4) {
                    ctx.set(x, FLOOR + 2, z, slab(SlabType.BOTTOM));
                }
            } else if (broken && !entrance && ctx.rand(x, z, 55) < 0.35) {
                ctx.set(x, FLOOR + 1, z, slab(SlabType.BOTTOM));
            }
        }
    }

    /** Eine Säule: Sockel, Schaft im Kreuz, Kapitell mit Kristall – oder zerbrochen, die Trommeln schweben darüber. */
    private void pillar(LairContext ctx, int px, int pz, boolean broken, int index) {
        int x0 = this.center.getX() + px;
        int z0 = this.center.getZ() + pz;
        for (int a = -1; a <= 1; a++) {
            for (int b = -1; b <= 1; b++) {
                ctx.set(x0 + a, FLOOR + 1, z0 + b, a != 0 && b != 0 ? polished() : voidBricks());
            }
        }
        int top = FLOOR + 13;
        int stump = broken ? FLOOR + 4 + (int) (ctx.rand(px, pz, 61) * 3) : top;
        for (int y = FLOOR + 2; y < (broken ? stump : top); y++) {
            ctx.set(x0, y, z0, pillar(Direction.Axis.Y));
            if (y < FLOOR + 4 || y > top - 3) {
                for (Direction d : Direction.Plane.HORIZONTAL) {
                    ctx.set(x0 + d.getStepX(), y, z0 + d.getStepZ(), y < FLOOR + 4 ? marbleBricks() : marble());
                }
            }
        }
        if (!broken) {
            for (int a = -1; a <= 1; a++) {
                for (int b = -1; b <= 1; b++) {
                    ctx.set(x0 + a, top, z0 + b, a == 0 && b == 0 ? chiseled() : a != 0 && b != 0 ? slab(SlabType.BOTTOM) : marbleBricks());
                }
            }
            ctx.set(x0, top + 1, z0, crystal());
            ctx.set(x0, top + 2, z0, cluster());
            return;
        }
        // zerbrochen: über dem Stumpf schweben Trommeln, schräg versetzt, dazu das Kapitell
        int gap = 2 + (int) (ctx.rand(pz, px, 63) * 2);
        int y = stump + gap;
        for (int drum = 0; drum < 3; drum++) {
            int ox = drum == 1 ? (ctx.rand(px, drum, 65) < 0.5 ? 1 : -1) : 0;
            int oz = drum == 2 ? (ctx.rand(pz, drum, 67) < 0.5 ? 1 : -1) : 0;
            Direction.Axis axis = drum == 1 ? Direction.Axis.X : Direction.Axis.Y;
            ctx.set(x0 + ox, y, z0 + oz, pillar(axis));
            if (axis == Direction.Axis.X) {
                ctx.set(x0 + ox + 1, y, z0 + oz, pillar(axis));
            } else {
                ctx.set(x0 + ox, y + 1, z0 + oz, pillar(axis));
                y++;
            }
            y += 2;
        }
        ctx.set(x0, y + 1, z0, chiseled());
        ctx.set(x0 + 1, y + 1, z0, slab(SlabType.TOP));
        ctx.set(x0, y + 2, z0, cluster());
        // Bruchstücke am Fuß
        for (int i = 0; i < 5; i++) {
            double a = ctx.rand(index, i, 69) * Math.PI * 2;
            double r = 2.0 + ctx.rand(i, index, 71) * 2.5;
            ctx.set(x0 + (int) Math.round(Math.cos(a) * r), FLOOR + 1, z0 + (int) Math.round(Math.sin(a) * r),
                    i % 2 == 0 ? slab(SlabType.BOTTOM) : s(VoidBlocks.CRACKED_VOID_STONE_BRICKS.get()));
        }
    }

    /** Kristalllampe: dunkler Sockel, gemeißelter Marmor, ein leuchtender Kristall mit Druse. */
    private void lamp(LairContext ctx, int px, int pz) {
        int x0 = this.center.getX() + px;
        int z0 = this.center.getZ() + pz;
        ctx.set(x0, FLOOR + 1, z0, polished());
        for (Direction d : Direction.Plane.HORIZONTAL) {
            ctx.set(x0 + d.getStepX(), FLOOR + 1, z0 + d.getStepZ(), slab(SlabType.BOTTOM));
        }
        ctx.set(x0, FLOOR + 2, z0, chiseled());
        ctx.set(x0, FLOOR + 3, z0, crystal());
        ctx.set(x0, FLOOR + 4, z0, cluster());
    }

    /** Das Podest im Norden: drei Stufen, vorn je eine breite Treppe. */
    private void dais(LairContext ctx) {
        int cx = this.center.getX();
        int cz = this.center.getZ();
        for (int dx = -7; dx <= 7; dx++) {
            for (int dz = DAIS_Z - 7; dz <= DAIS_Z + 7; dz++) {
                double rd = Math.sqrt(dx * dx + (dz - DAIS_Z) * (dz - DAIS_Z));
                if (Math.sqrt(dx * dx + dz * dz) > R - 1.0) {
                    continue;
                }
                int x = cx + dx;
                int z = cz + dz;
                boolean front = dz > DAIS_Z && Math.abs(dx) <= 2;
                if (rd <= 6.5) {
                    ctx.set(x, FLOOR + 1, z, front && rd > 5.5 ? stairs(Direction.NORTH) : marbleBricks());
                }
                if (rd <= 4.5) {
                    ctx.set(x, FLOOR + 2, z, front && rd > 3.5 ? stairs(Direction.NORTH) : marble());
                }
                if (rd <= 2.5) {
                    ctx.set(x, FLOOR + 3, z, front && rd > 1.5 ? stairs(Direction.NORTH) : polished());
                }
            }
        }
        // Kristalle an den Ecken der unteren Stufe
        for (int side = -1; side <= 1; side += 2) {
            ctx.set(cx + side * 5, FLOOR + 2, cz + DAIS_Z + 3, cluster());
        }
    }

    /** Der Thron: Sitz, Armlehnen, eine hohe Lehne mit Kristallkrone, dahinter ein Turm aus Kristall. */
    private void throne(LairContext ctx) {
        int x = this.center.getX();
        int z = this.center.getZ() + THRONE_Z;
        int y = FLOOR + 4;
        ctx.set(x, y, z, stairs(Direction.NORTH));
        ctx.set(x - 1, y, z, slab(SlabType.BOTTOM));
        ctx.set(x + 1, y, z, slab(SlabType.BOTTOM));
        for (int dy = 0; dy <= 2; dy++) {
            ctx.set(x - 1, y + dy, z - 1, marbleBricks());
            ctx.set(x + 1, y + dy, z - 1, marbleBricks());
        }
        for (int dy = 0; dy <= 4; dy++) {
            ctx.set(x, y + dy, z - 1, pillar(Direction.Axis.Y));
        }
        ctx.set(x - 1, y + 3, z - 1, cluster());
        ctx.set(x + 1, y + 3, z - 1, cluster());
        ctx.set(x, y + 5, z - 1, chiseled());
        ctx.set(x, y + 6, z - 1, crystal());
        ctx.set(x, y + 7, z - 1, cluster());
        // Kristallturm hinter dem Thron
        int sz = this.center.getZ() + THRONE_Z - 4;
        this.spire(ctx, x, sz, FLOOR + 2, 13);
        this.spire(ctx, x - 3, sz + 1, FLOOR + 2, 7);
        this.spire(ctx, x + 3, sz + 1, FLOOR + 2, 8);
    }

    /** Ein Kristallturm: dicker Fuß, dünner werdend, oben eine Druse. */
    private void spire(LairContext ctx, int x, int z, int base, int height) {
        for (int dy = 0; dy < height; dy++) {
            ctx.set(x, base + dy, z, crystal());
            if (dy < height * 0.55) {
                for (Direction d : Direction.Plane.HORIZONTAL) {
                    if (dy < height * 0.3 || ctx.rand(x + d.ordinal(), base + dy, 81) < 0.5) {
                        ctx.set(x + d.getStepX(), base + dy, z + d.getStepZ(), crystal());
                    }
                }
            }
        }
        ctx.set(x, base + height, z, cluster());
    }

    /** Obelisk neben dem Podest: dunkler Schaft auf Marmorsockel, oben ein Kristall. */
    private void obelisk(LairContext ctx, int px, int pz) {
        int x = this.center.getX() + px;
        int z = this.center.getZ() + pz;
        int base = FLOOR + 2;
        ctx.set(x, base, z, marbleBricks());
        for (int dy = 1; dy <= 6; dy++) {
            ctx.set(x, base + dy, z, dy == 4 ? s(VoidBlocks.CHISELED_VOID_STONE.get()) : polished());
        }
        ctx.set(x, base + 7, z, crystal());
        ctx.set(x, base + 8, z, cluster());
    }

    /** Schwebende Trittsteine vom Eingang nach Süden. */
    private void steppingStones(LairContext ctx) {
        int[][] stones = {{0, 25, 0}, {1, 29, -1}, {-1, 33, -1}, {1, 37, -2}};
        for (int[] st : stones) {
            int x0 = this.center.getX() + st[0];
            int z0 = this.center.getZ() + st[1];
            int top = FLOOR + st[2];
            for (int a = -2; a <= 2; a++) {
                for (int b = -2; b <= 2; b++) {
                    double d = Math.sqrt(a * a + b * b);
                    if (d > 1.9) {
                        continue;
                    }
                    ctx.set(x0 + a, top, z0 + b, d < 1.2 ? marbleBricks() : voidBricks());
                    int below = d < 0.5 ? 3 : d < 1.2 ? 2 : 1;
                    for (int k = 1; k <= below; k++) {
                        ctx.set(x0 + a, top - k, z0 + b, k == below && d < 0.5 ? crystal() : voidStone());
                    }
                }
            }
        }
    }

    /** Trümmer, die um den Platz treiben (nicht über den Trittsteinen). */
    private void debris(LairContext ctx) {
        for (int i = 0; i < 16; i++) {
            double a = ctx.rand(i, 7, 91) * Math.PI * 2;
            if (Math.abs(Mth.wrapDegrees(Math.toDegrees(a) - 90.0)) < 16.0) {
                continue;
            }
            double r = 26.0 + ctx.rand(i, 9, 93) * 10.0;
            int x0 = this.center.getX() + (int) Math.round(Math.cos(a) * r);
            int z0 = this.center.getZ() + (int) Math.round(Math.sin(a) * r);
            int y0 = FLOOR - 8 + (int) (ctx.rand(i, 11, 95) * 24);
            int size = 1 + (int) (ctx.rand(i, 13, 97) * 2.0);
            int kind = (int) (ctx.rand(i, 15, 99) * 4);
            for (int a2 = -size; a2 <= size; a2++) {
                for (int b2 = -size; b2 <= size; b2++) {
                    for (int c2 = -size; c2 <= size; c2++) {
                        double d = Math.sqrt(a2 * a2 + b2 * b2 * 1.4 + c2 * c2);
                        if (d > size + 0.3 + ctx.rand(a2 + i, b2 + c2, 101) * 0.6) {
                            continue;
                        }
                        BlockState state = switch (kind) {
                            case 0 -> marbleBricks();
                            case 1 -> ctx.rand(a2, c2 + i, 103) < 0.3 ? s(VoidBlocks.CRACKED_VOID_STONE_BRICKS.get()) : voidBricks();
                            case 2 -> b2 < 0 ? voidStone() : marble();
                            default -> pillar(Direction.Axis.X);
                        };
                        ctx.set(x0 + a2, y0 + b2, z0 + c2, state);
                    }
                }
            }
            if (kind != 3 && ctx.rand(i, 17, 105) < 0.5) {
                ctx.set(x0, y0 + size + 1, z0, cluster());
            }
            if (ctx.rand(i, 19, 107) < 0.4) {
                ctx.set(x0, y0 - size - 1, z0, crystal());
            }
        }
    }

    /** Das Thronsiegel, verborgen unter dem Thron. */
    private void seal(LairContext ctx) {
        BlockPos pos = new BlockPos(this.center.getX(), FLOOR - 3, this.center.getZ() + THRONE_Z);
        if (ctx.inside(pos.getX(), pos.getY(), pos.getZ())) {
            ctx.set(pos.getX(), pos.getY(), pos.getZ(), s(VoidBlocks.THRONE_SEAL.get()));
            if (ctx.level.getBlockEntity(pos) instanceof ThroneSealBlockEntity be) {
                be.setup(this.arenaCenter());
            }
        }
    }
}
