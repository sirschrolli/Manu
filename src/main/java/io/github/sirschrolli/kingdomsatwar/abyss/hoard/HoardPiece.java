package io.github.sirschrolli.kingdomsatwar.abyss.hoard;

import io.github.sirschrolli.kingdomsatwar.block.abyss.HoardSealBlockEntity;
import io.github.sirschrolli.kingdomsatwar.registry.AbyssBlocks;
import io.github.sirschrolli.kingdomsatwar.worldgen.lair.LairContext;
import io.github.sirschrolli.kingdomsatwar.worldgen.lair.ModStructures;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.ChunkPos;

/**
 * Der Schlund der Welten: der Hort des Weltenfressers, einmal in jedem Abgrund. Chunk für Chunk gebaut (alle Formen
 * ergeben sich aus Mitte und Startwert):
 * <ol>
 *     <li>Ringsum wird das Chaos freigeräumt – ein weiter, leerer Raum, in dem der Ring allein schwebt.</li>
 *     <li>Der Ring: Boden aus poliertem Chaosgestein (innen {@value #R_IN}, außen {@value #R_OUT} Blöcke vom Mittelpunkt),
 *     ein Saum aus gemeißeltem Chaosgestein und eine glühende Naht um den Schlund in der Mitte, sieben Weltennähte wie
 *     Speichen, ein Band aus Ziegeln, außen eine niedrige, zerbrochene Brüstung.</li>
 *     <li>Darunter ein zerklüfteter Ring-Kegel mit eingebackenen Stücken fremder Welten; drei Lavafälle stürzen vom Rand
 *     ins Nichts. Der Schlund selbst bleibt bis ganz unten offen.</li>
 *     <li>Um den Schlund sieben Sockel mit den <b>Kronensplittern</b> des Zerbrochenen Königs – sie halten das Siegel.</li>
 *     <li>Ringsum treiben Bruchstücke verschlungener Welten; im Süden tragen zwei kleine Inseln mit Hebekristallen hinauf.</li>
 *     <li>Das Siegel des Horts liegt verborgen im Kegel unter dem Norden des Rings.</li>
 * </ol>
 */
public class HoardPiece extends StructurePiece {
    /** Höhe des Bodens (man steht einen Block darüber). */
    public static final int FLOOR = 140;
    public static final double R_IN = 11.5;
    public static final double R_OUT = 27.5;
    /** Abstand der Kronensplitter vom Mittelpunkt. */
    public static final double SHARD_RADIUS = 15.5;
    public static final int SHARDS = 7;
    /** Wo das Siegel liegt (relativ zur Mitte, im Kegel unter dem Norden des Rings). */
    public static final int SEAL_Z = -19;
    /** Der freigeräumte Raum: so weit reicht er höchstens (sein Rand ist unregelmäßig, siehe {@link #clearEdge}). */
    static final double R_CLEAR = 100.0;
    static final int CLEAR_DOWN = 74;
    static final int CLEAR_UP = 86;

    private final BlockPos center;
    private final long seed;

    public HoardPiece(BlockPos center, long seed) {
        super(ModStructures.WORLD_EATER_HOARD_PIECE.get(), 0, makeBox(center));
        this.center = center;
        this.seed = seed;
    }

    public HoardPiece(CompoundTag tag) {
        super(ModStructures.WORLD_EATER_HOARD_PIECE.get(), tag);
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

    /** Winkel der sieben Speichen (Weltennähte); die Kronensplitter stehen genau dazwischen. Norden (−z) zuerst. */
    static double spokeAngle(int k) {
        return -Math.PI / 2.0 + k * Mth.TWO_PI / SHARDS;
    }

    /** Wo Kronensplitter {@code k} steht – zu {@code home}, der Mitte des Rings einen Block über dem Boden. */
    public static BlockPos shardPosition(BlockPos home, int k) {
        double a = spokeAngle(k) + Math.PI / SHARDS;
        return new BlockPos(home.getX() + (int) Math.round(Math.cos(a) * SHARD_RADIUS), home.getY() + 1,
                home.getZ() + (int) Math.round(Math.sin(a) * SHARD_RADIUS));
    }

    /** Das Siegel zu einer Mitte {@code home} (ein Block über dem Boden). */
    public static BlockPos sealPosition(BlockPos home) {
        return new BlockPos(home.getX(), home.getY() - 4, home.getZ() + SEAL_Z);
    }

    // =====================================================================================
    // Baustoffe
    // =====================================================================================

    private static BlockState s(Block block) {
        return block.defaultBlockState();
    }

    private static BlockState stone() {
        return s(AbyssBlocks.CHAOS_STONE.base().get());
    }

    private static BlockState polished() {
        return s(AbyssBlocks.POLISHED_CHAOS_STONE.base().get());
    }

    private static BlockState bricks() {
        return s(AbyssBlocks.CHAOS_STONE_BRICKS.base().get());
    }

    private static BlockState cracked() {
        return s(AbyssBlocks.CRACKED_CHAOS_STONE_BRICKS.get());
    }

    private static BlockState chiseled() {
        return s(AbyssBlocks.CHISELED_CHAOS_STONE.get());
    }

    private static BlockState seam() {
        return s(AbyssBlocks.WORLD_SEAM.get());
    }

    /** Ein Stück einer verschlungenen Welt, im Kegel eingebacken oder als Bruchstück. */
    private static BlockState worldBlock(int kind, boolean top) {
        return switch (kind) {
            case 0 -> top ? s(Blocks.GRASS_BLOCK) : s(Blocks.DIRT);
            case 1 -> top ? s(Blocks.CRIMSON_NYLIUM) : s(Blocks.NETHERRACK);
            case 2 -> s(Blocks.END_STONE);
            case 3 -> top ? s(Blocks.SNOW_BLOCK) : s(Blocks.PACKED_ICE);
            default -> top ? s(Blocks.SAND) : s(Blocks.SANDSTONE);
        };
    }

    // =====================================================================================
    // Formen
    // =====================================================================================

    /** Wie tief der Ring-Kegel an dieser Stelle hinabreicht (0 = gar nicht). */
    private int depth(LairContext ctx, int dx, int dz, double r) {
        if (r < R_IN - 0.2 || r > R_OUT + 0.8) {
            return 0;
        }
        double m = Mth.clamp((r - R_IN) / (R_OUT - R_IN), 0.0, 1.0);
        double d = 3.0 + Math.sin(Math.PI * m) * 15.0 + (ctx.noise(dx, dz, 0.15, 71) - 0.5) * 7.0;
        if (ctx.rand(dx, dz, 73) < 0.08) {
            d += 3.0 + ctx.rand(dz, dx, 75) * 8.0;                // Zacken
        }
        return Math.max(2, (int) Math.round(d));
    }

    /** Muster des Bodens. */
    private BlockState floor(LairContext ctx, int dx, int dz, double r) {
        if (r < R_IN + 1.0) {
            return ctx.rand(dx, dz, 81) < 0.5 ? chiseled() : polished();
        }
        if (r < R_IN + 1.9) {
            return seam();
        }
        double a = Math.atan2(dz, dx);
        for (int k = 0; k < SHARDS; k++) {
            double off = Math.abs(r * Math.sin(a - spokeAngle(k)));
            if (off < 0.55 && Math.cos(a - spokeAngle(k)) > 0 && r < R_OUT - 2.0) {
                return seam();
            }
        }
        if (r >= 19.4 && r < 20.4) {
            return bricks();
        }
        if (r >= R_OUT - 1.3) {
            return ctx.rand(dx, dz, 83) < 0.3 ? cracked() : bricks();
        }
        double w = ctx.rand(dx, dz, 85);
        if (w < 0.02) {
            return worldBlock((int) (ctx.rand(dz, dx, 87) * 5), true);    // ein eingebackenes Stück fremder Welt
        }
        return w < 0.08 ? stone() : polished();
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
        for (int k = 0; k < SHARDS; k++) {
            this.shardPedestal(ctx, k);
        }
        this.lavaFalls(ctx);
        this.debris(ctx);
        this.liftIslands(ctx);
        this.seal(ctx);
    }

    /** Rand des freien Raums in Richtung {@code angle} auf Höhe {@code y}: zwischen 72 % und 100 % von {@link #R_CLEAR}. */
    private static double clearEdge(LairContext ctx, double angle, int y) {
        double n = 0.6 * ctx.angleNoise(angle + y * 0.013, 4.0, 151) + 0.4 * ctx.noise(angle * 9.0, y * 0.11, 1.0, 153);
        return R_CLEAR * (0.72 + 0.28 * n);
    }

    private void column(LairContext ctx, int x, int z, int dx, int dz) {
        double r = Math.sqrt(dx * dx + dz * dz);
        if (r > R_CLEAR + 0.5) {
            return;
        }
        // den Raum ringsum freiräumen: ein weites, flaches Auge im Chaos mit unregelmäßigem Rand (die Bruchstücke ringsum
        // enden zerklüftet, nicht wie abgeschnitten)
        double angle = Math.atan2(dz, dx);
        for (int y = FLOOR - CLEAR_DOWN; y <= FLOOR + CLEAR_UP; y++) {
            double dy = (y - FLOOR) * (y < FLOOR ? 1.3 : 1.2);
            double edge = clearEdge(ctx, angle, y);
            if (r * r + dy * dy <= edge * edge && !ctx.get(x, y, z).isAir()) {
                ctx.set(x, y, z, Blocks.AIR.defaultBlockState());
            }
        }
        int depth = this.depth(ctx, dx, dz, r);
        if (depth <= 0) {
            return;
        }
        // der Ring-Kegel darunter: Chaosgestein mit glühenden Adern und eingebackenen Stücken fremder Welten
        for (int k = 1; k <= depth; k++) {
            int y = FLOOR - k;
            BlockState state;
            if (k == 1) {
                state = bricks();
            } else if (ctx.rand(x + y * 13, z, 91) < 0.06) {
                state = seam();
            } else if (ctx.noise(x * 0.7 + y, z * 0.7 - y, 0.09, 93) > 0.74) {
                state = worldBlock((int) (ctx.noise(x, z + y * 3, 0.03, 95) * 5) % 5, false);
            } else {
                state = stone();
            }
            ctx.set(x, y, z, state);
        }
        int bottom = FLOOR - depth - 1;
        if (ctx.rand(x, z, 97) < 0.05) {
            ctx.set(x, bottom, z, seam());
        }
        if (r > R_OUT || r < R_IN) {
            return;
        }
        // der Boden
        ctx.set(x, FLOOR, z, this.floor(ctx, dx, dz, r));
        // die Brüstung: niedrig, stellenweise zerbrochen, mit Lücken an den Speichen
        if (r > R_OUT - 1.0) {
            double a = Math.atan2(dz, dx);
            boolean gap = false;
            for (int k = 0; k < SHARDS; k++) {
                if (Math.abs(Mth.wrapDegrees(Math.toDegrees(a - spokeAngle(k)))) < 4.5) {
                    gap = true;
                }
            }
            boolean broken = ctx.angleNoise(a, 6.0, 99) < 0.3;
            if (!gap && !broken) {
                ctx.set(x, FLOOR + 1, z, s(AbyssBlocks.CHAOS_STONE_BRICKS.wall().get()));
            } else if (broken && !gap && ctx.rand(x, z, 101) < 0.4) {
                ctx.set(x, FLOOR + 1, z, s(AbyssBlocks.CHAOS_STONE_BRICKS.slab().get()));
            }
        }
    }

    /** Ein Sockel aus gemeißeltem Chaosgestein mit einem Kronensplitter – das Siegel. */
    private void shardPedestal(LairContext ctx, int k) {
        BlockPos shard = shardPosition(this.center.above(), k);
        int x = shard.getX();
        int z = shard.getZ();
        for (int a = -1; a <= 1; a++) {
            for (int b = -1; b <= 1; b++) {
                ctx.set(x + a, FLOOR, z + b, a == 0 && b == 0 ? chiseled() : polished());
            }
        }
        ctx.set(x, FLOOR + 1, z, chiseled());
        ctx.set(x, FLOOR + 2, z, AbyssBlocks.SEAL_SHARD.get().defaultBlockState().setValue(AmethystClusterBlock.FACING, Direction.UP));
        // der Splitter kennt die Mitte: seine Lichtkette führt zum Siegelstern über dem Schlund
        BlockPos pos = new BlockPos(x, FLOOR + 2, z);
        if (ctx.inside(x, FLOOR + 2, z) && ctx.level.getBlockEntity(pos) instanceof io.github.sirschrolli.kingdomsatwar.block.abyss.SealShardBlockEntity be) {
            be.setup(this.center.above());
        }
    }

    /** Drei Lavafälle stürzen vom äußeren Rand des Kegels ins Nichts. */
    private void lavaFalls(LairContext ctx) {
        for (int k = 0; k < 3; k++) {
            double a = ctx.rand(k, 3, 111) * Mth.TWO_PI;
            int x = this.center.getX() + (int) Math.round(Math.cos(a) * (R_OUT + 0.6));
            int z = this.center.getZ() + (int) Math.round(Math.sin(a) * (R_OUT + 0.6));
            int y = FLOOR - 3;
            if (ctx.inside(x, y, z)) {
                ctx.set(x, y, z, s(Blocks.LAVA));
                ctx.level.scheduleTick(new BlockPos(x, y, z), net.minecraft.world.level.material.Fluids.LAVA, 0);
            }
        }
    }

    /** Ringsum treibende Bruchstücke verschlungener Welten (kleine, unförmige Klumpen). */
    private void debris(LairContext ctx) {
        for (int i = 0; i < 26; i++) {
            // außerhalb der Bahn des Weltenfressers (40 Blöcke), bis an den Rand des freien Raums
            double a = ctx.rand(i, 7, 121) * Mth.TWO_PI;
            double r = 52.0 + ctx.rand(i, 9, 123) * 22.0;
            int x0 = this.center.getX() + (int) Math.round(Math.cos(a) * r);
            int z0 = this.center.getZ() + (int) Math.round(Math.sin(a) * r);
            int y0 = FLOOR - 34 + (int) (ctx.rand(i, 11, 125) * 70.0);
            int size = 2 + (int) (ctx.rand(i, 13, 127) * 4.5);
            int kind = (int) (ctx.rand(i, 15, 129) * 6);
            for (int ox = -size; ox <= size; ox++) {
                for (int oz = -size; oz <= size; oz++) {
                    for (int oy = -size; oy <= 1; oy++) {
                        double d = Math.sqrt(ox * ox + oz * oz + oy * oy * 2.2);
                        if (d > size + (ctx.rand(x0 + ox, z0 + oz + oy * 31, 131) - 0.5) * 1.4) {
                            continue;
                        }
                        boolean top = oy == 1 || d > size - 0.8 && oy >= 0;
                        BlockState st = kind == 5 ? (ctx.rand(ox + x0, oz + z0 + oy, 133) < 0.15 ? seam() : stone()) : worldBlock(kind, top);
                        ctx.set(x0 + ox, y0 + oy, z0 + oz, st);
                    }
                }
            }
        }
    }

    /** Im Süden zwei kleine Inseln mit Hebekristallen: Sie tragen hinauf an den Rand des Rings. */
    private void liftIslands(LairContext ctx) {
        int[][] islands = {{0, -17, (int) Math.round(R_OUT + 2.5)}, {-7, -34, (int) Math.round(R_OUT + 9.0)}};
        for (int[] is : islands) {
            int x0 = this.center.getX() + is[0];
            int y0 = FLOOR + is[1];
            int z0 = this.center.getZ() + is[2];
            for (int ox = -4; ox <= 4; ox++) {
                for (int oz = -4; oz <= 4; oz++) {
                    double d = Math.sqrt(ox * ox + oz * oz);
                    if (d > 3.8) {
                        continue;
                    }
                    ctx.set(x0 + ox, y0, z0 + oz, d < 1.2 ? chiseled() : polished());
                    int hang = (int) Math.round((3.8 - d) * 1.5);
                    for (int k = 1; k <= hang; k++) {
                        ctx.set(x0 + ox, y0 - k, z0 + oz, ctx.rand(x0 + ox, z0 + oz + k, 141) < 0.1 ? seam() : stone());
                    }
                }
            }
            ctx.set(x0, y0 + 1, z0, AbyssBlocks.LIFT_CRYSTAL.get().defaultBlockState().setValue(AmethystClusterBlock.FACING, Direction.UP));
        }
    }

    /** Das Siegel des Horts, verborgen im Kegel unter dem Norden des Rings. */
    private void seal(LairContext ctx) {
        BlockPos home = this.center.above();
        BlockPos pos = sealPosition(home);
        if (ctx.inside(pos.getX(), pos.getY(), pos.getZ())) {
            ctx.set(pos.getX(), pos.getY(), pos.getZ(), s(AbyssBlocks.HOARD_SEAL.get()));
            if (ctx.level.getBlockEntity(pos) instanceof HoardSealBlockEntity be) {
                be.setup(home);
            }
        }
    }
}
