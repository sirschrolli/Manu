package io.github.sirschrolli.kingdomsatwar.worldgen.atoll;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.block.sea.AtollCoreBlockEntity;
import io.github.sirschrolli.kingdomsatwar.block.sea.SeashellBlock;
import io.github.sirschrolli.kingdomsatwar.registry.SeaBlocks;
import io.github.sirschrolli.kingdomsatwar.worldgen.lair.LairContext;
import io.github.sirschrolli.kingdomsatwar.worldgen.lair.ModStructures;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SeaPickleBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.storage.loot.LootTable;

/**
 * Baut das Versunkene Atoll Chunk für Chunk (alle Formen ergeben sich aus Mitte und Startwert):
 * <ol>
 *     <li>Ein Ring aus Klippen (Fels in Schichten, oben Gras, Palmen und hier und da eine hohe Felsnadel) umschließt
 *     eine Lagune; durch drei Durchlässe strömt das Meer herein. Außen fällt der Ring zum Meeresgrund ab.</li>
 *     <li>Die Lagune ist flach und türkis: knietiefes Wasser über hellem Sand, dazwischen trockene Sandbänke und ein paar
 *     tiefere Rinnen; Seegras, Korallen, Anemonen und Muscheln.</li>
 *     <li>In der Mitte die Ruine eines Tempels des Meeresvolks: ein runder Platz aus Prismarin mit Ringen aus dunklem
 *     Prismarin und vier eingelassenen Seelaternen, sechs Säulen (manche zerbrochen, ihre Trommeln liegen in der Lagune),
 *     Stufen hinab ins Wasser und eine Truhe.</li>
 *     <li>Auf dem Platz der Hügel, unter dem Karkinos schläft: Sand, aus dem die Kuppel seines Gehäuses schaut, und darin
 *     der leuchtende Gezeiten-Speer. Darunter verborgen das Herz des Atolls.</li>
 * </ol>
 */
public class AtollPiece extends StructurePiece {
    /** Radius, bis zu dem das Atoll sein Gelände formt. */
    static final int REACH = 40;
    static final double PLATFORM = 9.6;
    static final double MOUND = 4.6;
    public static final ResourceKey<LootTable> CHEST = ResourceKey.create(Registries.LOOT_TABLE, KingdomsAtWar.id("chests/sunken_atoll"));

    private final BlockPos center;
    private final long seed;
    private final int sea;

    public AtollPiece(BlockPos center, long seed, int sea) {
        super(ModStructures.SUNKEN_ATOLL_PIECE.get(), 0, makeBox(center, sea));
        this.center = center;
        this.seed = seed;
        this.sea = sea;
    }

    public AtollPiece(CompoundTag tag) {
        super(ModStructures.SUNKEN_ATOLL_PIECE.get(), tag);
        this.center = new BlockPos(tag.getIntOr("CX", 0), tag.getIntOr("CY", 63), tag.getIntOr("CZ", 0));
        this.seed = tag.getLongOr("Seed", 0L);
        this.sea = tag.getIntOr("Sea", 63);
    }

    private static BoundingBox makeBox(BlockPos c, int sea) {
        return new BoundingBox(c.getX() - REACH, sea - 40, c.getZ() - REACH, c.getX() + REACH, sea + 24, c.getZ() + REACH);
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        tag.putInt("CX", this.center.getX());
        tag.putInt("CY", this.center.getY());
        tag.putInt("CZ", this.center.getZ());
        tag.putLong("Seed", this.seed);
        tag.putInt("Sea", this.sea);
    }

    /** Oberkante des Tempelplatzes (bündig mit dem Wasserspiegel). */
    public static int platformTop(int sea) {
        return sea - 1;
    }

    // =====================================================================================
    // Form
    // =====================================================================================

    private static BlockState s(Block block) {
        return block.defaultBlockState();
    }

    /** Innerer Radius des Klippenrings in Richtung {@code a}. */
    private double ringInner(LairContext ctx, double a) {
        return 22.0 + 2.2 * Math.sin(3 * a + ctx.rand(1, 0, 11) * 6.28) + 1.4 * Math.sin(5 * a + ctx.rand(2, 0, 11) * 6.28);
    }

    private double ringWidth(LairContext ctx, double a) {
        return 6.0 + 1.6 * Math.sin(2 * a + ctx.rand(3, 0, 11) * 6.28);
    }

    /** Höhe des Rings über dem Meer (negativ: ein Durchlass für das Meer). */
    private double ringHeight(LairContext ctx, double a) {
        double base = ctx.rand(4, 0, 11) * 6.28;
        for (int k = 0; k < 3; k++) {
            double gap = base + k * 2.094 + (ctx.rand(5, k, 11) - 0.5) * 0.6;
            double d = Math.abs(Mth.wrapDegrees((a - gap) * Mth.RAD_TO_DEG));
            if (d < 9.0) {
                return -3.0;
            }
            if (d < 16.0) {
                return -3.0 + (d - 9.0) / 7.0 * 5.0;
            }
        }
        double n = ctx.noise(Math.cos(a) * 30.0, Math.sin(a) * 30.0, 0.09, 13);
        return 3.0 + 9.0 * n * n;
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator, RandomSource random,
            BoundingBox chunkBB, ChunkPos chunkPos, BlockPos referencePos) {
        LairContext ctx = new LairContext(level, chunkBB, this.center, this.seed);
        int cx = this.center.getX();
        int cz = this.center.getZ();
        for (int x = chunkBB.minX(); x <= chunkBB.maxX(); x++) {
            for (int z = chunkBB.minZ(); z <= chunkBB.maxZ(); z++) {
                double dx = x + 0.5 - (cx + 0.5);
                double dz = z + 0.5 - (cz + 0.5);
                double r = Math.sqrt(dx * dx + dz * dz);
                if (r > REACH) {
                    continue;
                }
                this.column(ctx, x, z, r, Math.atan2(dz, dx));
            }
        }
        this.temple(ctx);
        this.spires(ctx);
        this.palms(ctx);
        buildMound(level, chunkBB, this.center);
        BlockPos core = AtollLocator.core(this.center);
        if (chunkBB.isInside(core)) {
            level.setBlock(core, s(SeaBlocks.ATOLL_CORE.get()), Block.UPDATE_CLIENTS);
            if (level.getBlockEntity(core) instanceof AtollCoreBlockEntity atoll) {
                atoll.setup(this.center);
            }
        }
        // die Truhe des Meeresvolks am Fuß einer zerbrochenen Säule
        double a = ctx.rand(9, 9, 31) * Math.PI * 2.0;
        BlockPos chest = this.center.offset((int) Math.round(Math.cos(a) * 6.0), 0, (int) Math.round(Math.sin(a) * 6.0));
        this.createChest(level, chunkBB, random, chest, CHEST, Blocks.CHEST.defaultBlockState()
                .setValue(net.minecraft.world.level.block.ChestBlock.FACING, Direction.Plane.HORIZONTAL.getRandomDirection(random)));
    }

    /** Eine Säule des Geländes: Fels und Sand bis zur Oberkante, darüber Wasser bis zum Meeresspiegel, darüber Luft. */
    private void column(LairContext ctx, int x, int z, double r, double a) {
        int ground = ctx.originalHeight(x, z, true) - 1;
        int water = this.sea - 1;                          // oberster Wasserblock
        double inner = this.ringInner(ctx, a);
        double width = this.ringWidth(ctx, a);
        int top;
        BlockState topBlock;
        BlockState under = s(Blocks.SAND);
        boolean rock = false;
        double u = (r - inner) / width;
        if (r < PLATFORM) {
            top = platformTop(this.sea);
            topBlock = this.platformBlock(ctx, x, z, r, a);
            under = s(Blocks.PRISMARINE_BRICKS);
        } else if (u < 0.0) {
            // Lagune: knietief, Sandbänke, Rinnen; zum Ring hin ein trockener Strand
            double n = ctx.noise(x, z, 0.075, 21) + (ctx.noise(x, z, 0.21, 22) - 0.5) * 0.25;
            top = water - 1;
            if (n > 0.6 || u > -0.12) {
                top = water;
            } else if (n < 0.28 && r > PLATFORM + 3) {
                top = water - 2;
            }
            topBlock = n > 0.7 && ctx.rand(x, z, 23) < 0.3 ? s(Blocks.SANDSTONE) : s(Blocks.SAND);
        } else if (u <= 1.0) {
            // der Klippenring: steile Flanken, flache Krone
            double h = this.ringHeight(ctx, a);
            double shape = 1.0 - Math.pow(Math.abs(2.0 * u - 1.0), 3.0);
            double rough = (ctx.noise(x, z, 0.3, 24) - 0.5) * 2.0;
            top = (int) Math.round(water + Math.max(-2.0, h * shape + rough));
            if (top > water) {
                rock = true;
                topBlock = top > water + 1 ? s(Blocks.GRASS_BLOCK) : s(Blocks.SAND);
            } else {
                topBlock = s(Blocks.SAND);
            }
        } else {
            // außen: zum Meeresgrund hin abfallend
            double t = Mth.clamp((u - 1.0) * width / 9.0, 0.0, 1.0);
            double rough = (ctx.noise(x, z, 0.25, 25) - 0.5) * 2.0;
            top = (int) Math.round(Mth.lerp(t * t * (3 - 2 * t), water - 2.0, Math.min(ground, water - 2.0)) + rough);
            topBlock = ctx.rand(x, z, 26) < 0.25 ? s(Blocks.GRAVEL) : s(Blocks.SAND);
        }
        int bottom = Math.min(ground, top) - 4;
        for (int y = bottom; y <= top; y++) {
            BlockState state;
            int depth = top - y;
            if (y == top) {
                state = topBlock;
            } else if (rock) {
                state = this.cliffRock(ctx, x, y, z, depth);
            } else if (depth < 3) {
                state = under;
            } else {
                state = s(Blocks.STONE);
            }
            ctx.set(x, y, z, state);
        }
        for (int y = top + 1; y <= water; y++) {
            ctx.set(x, y, z, s(Blocks.WATER));
        }
        for (int y = Math.max(top + 1, water + 1); y <= this.sea + 22; y++) {
            ctx.set(x, y, z, s(Blocks.AIR));
        }
        this.decorate(ctx, x, z, r, top, water, rock);
    }

    private BlockState cliffRock(LairContext ctx, int x, int y, int z, int depth) {
        if (depth < 2) {
            return s(Blocks.DIRT);
        }
        int band = Math.floorMod(y + (int) (ctx.noise(x, z, 0.05, 27) * 4), 5);
        return switch (band) {
            case 0 -> s(Blocks.CALCITE);
            case 1, 2 -> s(Blocks.STONE);
            case 3 -> s(Blocks.ANDESITE);
            default -> ctx.rand(x, y * 31 + z, 28) < 0.15 ? s(Blocks.MOSSY_COBBLESTONE) : s(Blocks.STONE);
        };
    }

    /** Der Tempelplatz: Prismarinziegel, zwei Ringe aus dunklem Prismarin, vier Seelaternen, am Rand Stufen. */
    private BlockState platformBlock(LairContext ctx, int x, int z, double r, double a) {
        if (Math.abs(r - 5.4) < 0.55 || Math.abs(r - 8.6) < 0.5) {
            return s(Blocks.DARK_PRISMARINE);
        }
        double deg = Mth.wrapDegrees(a * Mth.RAD_TO_DEG + 45.0);
        if (Math.abs(r - 7.0) < 0.7 && Math.abs(Mth.wrapDegrees(deg - Math.round(deg / 90.0) * 90.0)) < 5.0) {
            return s(Blocks.SEA_LANTERN);
        }
        if (r > 9.0) {
            // ausgetretene Stufen hinab in die Lagune
            Direction out = Math.abs(Math.cos(a)) > Math.abs(Math.sin(a)) ? (Math.cos(a) > 0 ? Direction.EAST : Direction.WEST)
                    : (Math.sin(a) > 0 ? Direction.SOUTH : Direction.NORTH);
            return s(Blocks.PRISMARINE_BRICK_STAIRS).setValue(StairBlock.FACING, out.getOpposite());
        }
        return ctx.rand(x, z, 29) < 0.12 ? s(Blocks.PRISMARINE) : s(Blocks.PRISMARINE_BRICKS);
    }

    /** Was in einer Säule wächst und liegt: Seegras, Korallen, Anemonen, Muscheln; auf den Klippen Gras. */
    private void decorate(LairContext ctx, int x, int z, double r, int top, int water, boolean rock) {
        double roll = ctx.rand(x, z, 41);
        if (rock) {
            if (top > water + 1 && roll < 0.3) {
                ctx.set(x, top + 1, z, roll < 0.05 ? s(Blocks.FERN) : s(Blocks.SHORT_GRASS));
            }
            return;
        }
        if (r < PLATFORM) {
            return;
        }
        if (top < water) {
            // unter Wasser
            BlockState plant = null;
            if (roll < 0.16) {
                plant = s(Blocks.SEAGRASS);
            } else if (roll < 0.19) {
                plant = SeaBlocks.anemones().get((int) (ctx.rand(x, z, 42) * 4)).get().defaultBlockState();
            } else if (roll < 0.215) {
                Block[] fans = {Blocks.TUBE_CORAL_FAN, Blocks.BRAIN_CORAL_FAN, Blocks.BUBBLE_CORAL_FAN, Blocks.FIRE_CORAL_FAN, Blocks.HORN_CORAL_FAN};
                plant = s(fans[(int) (ctx.rand(x, z, 43) * fans.length)]);
            } else if (roll < 0.235) {
                plant = s(Blocks.SEA_PICKLE).setValue(SeaPickleBlock.PICKLES, 1 + (int) (ctx.rand(x, z, 44) * 3));
            } else if (roll < 0.26) {
                plant = this.shells(ctx, x, z, true);
            }
            if (plant != null && top + 1 <= water) {
                ctx.set(x, top + 1, z, plant);
            }
        } else if (top == water && roll < 0.05 && r < 26) {
            ctx.set(x, top + 1, z, this.shells(ctx, x, z, false));
        }
    }

    private BlockState shells(LairContext ctx, int x, int z, boolean wet) {
        var kinds = SeaBlocks.shells();
        return kinds.get((int) (ctx.rand(x, z, 45) * kinds.size())).get().defaultBlockState()
                .setValue(SeashellBlock.SHELLS, 1 + (int) (ctx.rand(x, z, 46) * 2.4))
                .setValue(SeashellBlock.FACING, Direction.Plane.HORIZONTAL.stream().toList().get((int) (ctx.rand(x, z, 47) * 4)))
                .setValue(SeashellBlock.WATERLOGGED, wet);
    }

    /** Sechs Säulen um den Platz; die zerbrochenen haben ihre Trommeln in die Lagune geworfen. */
    private void temple(LairContext ctx) {
        int floor = platformTop(this.sea);
        double turn = ctx.rand(7, 7, 51) * 60.0;
        for (int k = 0; k < 6; k++) {
            double a = Math.toRadians(turn + k * 60.0);
            int x = this.center.getX() + (int) Math.round(Math.cos(a) * 7.6);
            int z = this.center.getZ() + (int) Math.round(Math.sin(a) * 7.6);
            boolean broken = ctx.rand(k, 1, 52) < 0.5;
            int height = broken ? 2 + (int) (ctx.rand(k, 2, 52) * 2) : 6;
            ctx.set(x, floor + 1, z, s(Blocks.DARK_PRISMARINE));
            for (int y = 2; y <= height; y++) {
                ctx.set(x, floor + y, z, s(Blocks.PRISMARINE_BRICKS));
            }
            if (!broken) {
                ctx.set(x, floor + height + 1, z, s(Blocks.DARK_PRISMARINE));
                ctx.set(x, floor + height + 2, z, s(Blocks.PRISMARINE_BRICK_SLAB));
                // ein goldener Ring unter dem Kapitell
                ctx.set(x, floor + height, z, s(Blocks.GOLD_BLOCK));
            } else {
                // die Trommeln liegen ein Stück weiter draußen im Wasser
                double fall = a + (ctx.rand(k, 3, 52) - 0.5) * 0.8;
                for (int d = 0; d < 3; d++) {
                    int fx = this.center.getX() + (int) Math.round(Math.cos(fall) * (11.5 + d));
                    int fz = this.center.getZ() + (int) Math.round(Math.sin(fall) * (11.5 + d));
                    Direction.Axis axis = Math.abs(Math.cos(fall)) > Math.abs(Math.sin(fall)) ? Direction.Axis.X : Direction.Axis.Z;
                    ctx.set(fx, floor - 1, fz, s(Blocks.QUARTZ_PILLAR).setValue(RotatedPillarBlock.AXIS, axis));
                }
            }
        }
    }

    /** Ein paar hohe Felsnadeln auf dem Ring – die Wahrzeichen des Atolls, von weitem zu sehen. */
    private void spires(LairContext ctx) {
        for (int k = 0; k < 4; k++) {
            double a = ctx.rand(k, 11, 61) * Math.PI * 2.0;
            double r = this.ringInner(ctx, a) + this.ringWidth(ctx, a) * 0.5;
            if (this.ringHeight(ctx, a) < 2.0) {
                continue;
            }
            int x = this.center.getX() + (int) Math.round(Math.cos(a) * r);
            int z = this.center.getZ() + (int) Math.round(Math.sin(a) * r);
            if (!ctx.near(x, z, 4)) {
                continue;
            }
            int base = this.sea - 3;
            int height = 10 + (int) (ctx.rand(k, 12, 61) * 9);
            double radius = 2.2 + ctx.rand(k, 13, 61);
            for (int y = 0; y < height + 10; y++) {
                double t = y / (double) (height + 10);
                double rr = radius * (1.0 - 0.55 * t);
                int reach = (int) Math.ceil(rr);
                for (int dx = -reach; dx <= reach; dx++) {
                    for (int dz = -reach; dz <= reach; dz++) {
                        if (dx * dx + dz * dz > rr * rr) {
                            continue;
                        }
                        int by = base + y;
                        if (by > this.sea + height) {
                            continue;
                        }
                        BlockState state = by == this.sea + height || by == this.sea + height - 1 && dx * dx + dz * dz > rr * rr * 0.4
                                ? s(Blocks.GRASS_BLOCK) : this.cliffRock(ctx, x + dx, by, z + dz, 3);
                        ctx.set(x + dx, by, z + dz, state);
                    }
                }
            }
        }
    }

    /** Palmen auf der Krone des Rings (Stamm geneigt, Wedel aus Dschungellaub). */
    private void palms(LairContext ctx) {
        for (int k = 0; k < 9; k++) {
            double a = ctx.rand(k, 21, 71) * Math.PI * 2.0;
            double h = this.ringHeight(ctx, a);
            if (h < 3.0) {
                continue;
            }
            double r = this.ringInner(ctx, a) + this.ringWidth(ctx, a) * (0.3 + ctx.rand(k, 22, 71) * 0.4);
            int x = this.center.getX() + (int) Math.round(Math.cos(a) * r);
            int z = this.center.getZ() + (int) Math.round(Math.sin(a) * r);
            if (!ctx.near(x, z, 5) || !ctx.reachable(x, z)) {
                continue;
            }
            int y = ctx.originalHeight(x, z, false);
            BlockState below = ctx.get(x, y - 1, z);
            if (!below.is(Blocks.GRASS_BLOCK) && !below.is(Blocks.SAND)) {
                continue;
            }
            int height = 5 + (int) (ctx.rand(k, 23, 71) * 3);
            double lean = ctx.rand(k, 24, 71) * Math.PI * 2.0;
            double lx = Math.cos(lean);
            double lz = Math.sin(lean);
            int tx = x;
            int tz = z;
            for (int i = 0; i < height; i++) {
                tx = x + (int) Math.round(lx * i * i / (double) (height * 2));
                tz = z + (int) Math.round(lz * i * i / (double) (height * 2));
                ctx.set(tx, y + i, tz, s(Blocks.JUNGLE_LOG));
            }
            int ty = y + height;
            BlockState leaves = s(Blocks.JUNGLE_LEAVES).setValue(LeavesBlock.PERSISTENT, true);
            ctx.set(tx, ty, tz, leaves);
            for (Direction d : Direction.Plane.HORIZONTAL) {
                for (int i = 1; i <= 3; i++) {
                    int droop = i == 3 ? -1 : 0;
                    ctx.setIfOpen(tx + d.getStepX() * i, ty + droop, tz + d.getStepZ() * i, leaves);
                }
                ctx.setIfOpen(tx + d.getStepX() + d.getClockWise().getStepX(), ty, tz + d.getStepZ() + d.getClockWise().getStepZ(), leaves);
            }
            ctx.setIfOpen(tx, ty + 1, tz, leaves);
        }
    }

    // =====================================================================================
    // Der Hügel über dem schlafenden Koloss
    // =====================================================================================

    /** Baut den Hügel (auch nach einem Rückzug): Sand, darin die Kuppel mit Korallen und der leuchtende Speer. */
    public static void buildMound(LevelAccessor level, BlockPos center) {
        buildMound(level, null, center);
    }

    private static void buildMound(LevelAccessor level, @org.jspecify.annotations.Nullable BoundingBox box, BlockPos center) {
        int y0 = center.getY();
        for (int dx = -5; dx <= 5; dx++) {
            for (int dz = -5; dz <= 5; dz++) {
                double d = Math.sqrt(dx * dx + dz * dz);
                if (d > MOUND) {
                    continue;
                }
                double h = 3.4 * (1.0 - (d / MOUND) * (d / MOUND));
                int top = y0 + (int) Math.floor(h);
                for (int y = y0; y <= top; y++) {
                    BlockState state;
                    if (d < 2.6 && y == top) {
                        // die Kuppel des Gehäuses schaut aus dem Sand
                        state = d < 1.0 ? s(Blocks.GOLD_BLOCK) : (Math.abs(dx) + Math.abs(dz)) % 2 == 0 ? s(Blocks.PRISMARINE_BRICKS)
                                : s(Blocks.DARK_PRISMARINE);
                    } else if (d < 2.2 && y == top - 1) {
                        state = s(Blocks.PRISMARINE_BRICKS);
                    } else {
                        state = (dx * 7 + dz * 13 + y) % 9 == 0 ? s(Blocks.GRAVEL) : s(Blocks.SAND);
                    }
                    place(level, box, center.getX() + dx, y, center.getZ() + dz, state);
                }
                // Korallen und Muscheln auf dem Hügel
                int k = Math.floorMod(dx * 31 + dz * 17, 23);
                if (d > 2.6 && d < 4.2 && k < 3) {
                    Block[] corals = {Blocks.FIRE_CORAL_BLOCK, Blocks.BRAIN_CORAL_BLOCK, Blocks.HORN_CORAL_BLOCK};
                    place(level, box, center.getX() + dx, top, center.getZ() + dz, s(corals[k]));
                } else if (d > 2.6 && k == 5) {
                    place(level, box, center.getX() + dx, top + 1, center.getZ() + dz, SeaBlocks.WHELK_SHELLS.get().defaultBlockState());
                }
            }
        }
        // der Speer, schräg aus der Kuppel
        place(level, box, center.getX(), y0 + (int) Math.floor(3.4) + 1, center.getZ(), SeaBlocks.EMBEDDED_TIDE_SPEAR.get().defaultBlockState());
    }

    /** Räumt den Hügel weg (wenn der Koloss erwacht): darunter liegt der Tempelplatz frei. */
    public static void clearMound(LevelAccessor level, BlockPos center) {
        int y0 = center.getY();
        for (int dx = -5; dx <= 5; dx++) {
            for (int dz = -5; dz <= 5; dz++) {
                if (dx * dx + dz * dz > (MOUND + 0.6) * (MOUND + 0.6)) {
                    continue;
                }
                for (int y = y0; y <= y0 + 6; y++) {
                    BlockPos pos = new BlockPos(center.getX() + dx, y, center.getZ() + dz);
                    if (!level.getBlockState(pos).isAir()) {
                        level.setBlock(pos, s(Blocks.AIR), Block.UPDATE_ALL);
                    }
                }
            }
        }
    }

    private static void place(LevelAccessor level, @org.jspecify.annotations.Nullable BoundingBox box, int x, int y, int z, BlockState state) {
        if (box != null && !box.isInside(x, y, z)) {
            return;
        }
        level.setBlock(new BlockPos(x, y, z), state, Block.UPDATE_CLIENTS);
    }
}
