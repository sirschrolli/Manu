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
 *     <li>Ein Ring aus flachen Sandinseln (Motus) umschließt eine Lagune: weiße Strände mit Muscheln und Gelegen der
 *     Meeresschildkröten, in der Mitte der Inseln Grün mit Farnen, Büschen und Bambus, darüber Kokospalmen und zwei, drei
 *     Nadeln aus hellem Korallenkalk. Zwischen den Inseln liegen Riffdächer knapp unter dem Wasser, durch drei Durchlässe
 *     strömt das Meer herein. Außen säumt eine Riffkante aus lebenden Korallen den Ring, dann fällt der Hang zum
 *     Meeresgrund ab.</li>
 *     <li>Die Lagune ist flach und türkis: knietiefes Wasser über hellem Sand, dazwischen trockene Sandbänke und ein paar
 *     tiefere Rinnen, Korallenköpfe bis knapp unter die Oberfläche; Seegras, Anemonen und Muscheln.</li>
 *     <li>In der Mitte die Ruine eines Tempels des Meeresvolks: ein runder Platz aus Prismarin mit Ringen aus dunklem
 *     Prismarin und vier eingelassenen Seelaternen, sechs Säulen (manche zerbrochen, ihre Trommeln liegen in der Lagune),
 *     Stufen hinab ins Wasser und eine Truhe.</li>
 *     <li>Auf dem Platz der Hügel, unter dem Karkinos schläft: Sand, aus dem die Kuppel seines Gehäuses schaut, und darin
 *     der leuchtende Gezeiten-Speer. Darunter verborgen das Herz des Atolls.</li>
 * </ol>
 */
public class AtollPiece extends StructurePiece {
    /** Radius, bis zu dem das Atoll sein Gelände formt. */
    static final int REACH = 46;
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

    /** Innerer Radius des Inselrings in Richtung {@code a}. */
    private double ringInner(LairContext ctx, double a) {
        return 22.0 + 2.2 * Math.sin(3 * a + ctx.rand(1, 0, 11) * 6.28) + 1.4 * Math.sin(5 * a + ctx.rand(2, 0, 11) * 6.28);
    }

    private double ringWidth(LairContext ctx, double a) {
        return 8.0 + 1.8 * Math.sin(2 * a + ctx.rand(3, 0, 11) * 6.28);
    }

    /**
     * Höhe des Rings über dem Meer: flache Inseln (Motus) aus Sand, dazwischen Riffdächer knapp unter dem Wasser und drei
     * Durchlässe, durch die das Meer in die Lagune strömt (negativ: unter Wasser).
     */
    private double ringHeight(LairContext ctx, double a) {
        double base = ctx.rand(4, 0, 11) * 6.28;
        for (int k = 0; k < 3; k++) {
            double gap = base + k * 2.094 + (ctx.rand(5, k, 11) - 0.5) * 0.6;
            double d = Math.abs(Mth.wrapDegrees((a - gap) * Mth.RAD_TO_DEG));
            if (d < 8.0) {
                return -3.0;
            }
            if (d < 15.0) {
                return -3.0 + (d - 8.0) / 7.0 * 4.0;
            }
        }
        // zwischen den Inseln schmale Riffdächer, auf den Inseln sanfte Dünen
        double n = ctx.angleNoise(a, 3.2, 13);
        if (n < 0.16) {
            return -1.0 + n * 6.0;
        }
        return 1.4 + 3.2 * Mth.clamp((n - 0.16) / 0.6, 0.0, 1.0);
    }

    private enum Kind { PLATFORM, LAGOON, ISLAND, REEF, SLOPE }

    /** Form einer Säule: Art, Oberkante (oberster fester Block), Lage im Ring (u: 0 innen, 1 außen) und Abstand außerhalb. */
    private record Profile(Kind kind, int top, double r, double u, double outside) {}

    private Profile profile(LairContext ctx, int x, int z) {
        double dx = x - this.center.getX();
        double dz = z - this.center.getZ();
        double r = Math.sqrt(dx * dx + dz * dz);
        double a = Math.atan2(dz, dx);
        int water = this.sea - 1;
        if (r < PLATFORM) {
            return new Profile(Kind.PLATFORM, platformTop(this.sea), r, -1.0, 0.0);
        }
        double inner = this.ringInner(ctx, a);
        double width = this.ringWidth(ctx, a);
        double u = (r - inner) / width;
        if (u < 0.0) {
            // Lagune: knietief, Sandbänke, Rinnen; zum Ring hin flacher Strand
            double n = ctx.noise(x, z, 0.075, 21) + (ctx.noise(x, z, 0.21, 22) - 0.5) * 0.25;
            int top = water - 1;
            if (n > 0.62 || u > -0.1 && this.ringHeight(ctx, a) > 0.0) {
                top = water;
            } else if (n < 0.3 && r > PLATFORM + 3) {
                top = water - 2;
            }
            return new Profile(Kind.LAGOON, top, r, u, 0.0);
        }
        if (u <= 1.0) {
            double h = this.ringHeight(ctx, a);
            double shape = 1.0 - Math.pow(Math.abs(2.0 * u - 1.0), 3.2);
            double rough = (ctx.noise(x, z, 0.22, 24) - 0.5) * 1.4;
            int top = (int) Math.round(water + Math.max(-3.0, h * shape + (h > 0 ? rough : rough * 0.5)));
            if (h <= 0.0) {
                top = Math.min(top, water - 1);
            }
            return new Profile(top > water ? Kind.ISLAND : Kind.REEF, top, r, u, 0.0);
        }
        // außen: erst die Riffkante knapp unter dem Wasser, dann der Hang hinab zum Meeresgrund
        double o = (u - 1.0) * width;
        double rough = (ctx.noise(x, z, 0.25, 25) - 0.5) * 2.0;
        if (o < 4.0) {
            int top = water - 1 - (int) Math.floor(o * 0.55 + Math.max(0.0, rough) * 0.6);
            return new Profile(Kind.REEF, top, r, u, o);
        }
        int ground = ctx.originalHeight(x, z, true) - 1;
        double span = Math.max(4.0, REACH - inner - width - 4.0);
        double t = Mth.clamp((o - 4.0) / span, 0.0, 1.0);
        int top = (int) Math.round(Mth.lerp(LairContext.smooth(t), water - 3.0, Math.min(ground, water - 3.0)) + rough);
        return new Profile(Kind.SLOPE, top, r, u, o);
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator, RandomSource random,
            BoundingBox chunkBB, ChunkPos chunkPos, BlockPos referencePos) {
        LairContext ctx = new LairContext(level, chunkBB, this.center, this.seed);
        int cx = this.center.getX();
        int cz = this.center.getZ();
        for (int x = chunkBB.minX(); x <= chunkBB.maxX(); x++) {
            for (int z = chunkBB.minZ(); z <= chunkBB.maxZ(); z++) {
                double dx = x - cx;
                double dz = z - cz;
                if (dx * dx + dz * dz > REACH * REACH) {
                    continue;
                }
                this.column(ctx, x, z);
            }
        }
        this.coralHeads(ctx);
        this.temple(ctx);
        this.pinnacles(ctx);
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

    private static final Block[] CORAL_BLOCKS = {Blocks.BRAIN_CORAL_BLOCK, Blocks.TUBE_CORAL_BLOCK, Blocks.FIRE_CORAL_BLOCK,
            Blocks.HORN_CORAL_BLOCK, Blocks.BUBBLE_CORAL_BLOCK};
    private static final Block[] DEAD_CORAL_BLOCKS = {Blocks.DEAD_BRAIN_CORAL_BLOCK, Blocks.DEAD_TUBE_CORAL_BLOCK, Blocks.DEAD_HORN_CORAL_BLOCK};
    private static final Block[] CORALS = {Blocks.BRAIN_CORAL, Blocks.TUBE_CORAL, Blocks.FIRE_CORAL, Blocks.HORN_CORAL, Blocks.BUBBLE_CORAL};
    private static final Block[] CORAL_FANS = {Blocks.TUBE_CORAL_FAN, Blocks.BRAIN_CORAL_FAN, Blocks.BUBBLE_CORAL_FAN, Blocks.FIRE_CORAL_FAN,
            Blocks.HORN_CORAL_FAN};

    /** Ein Riffblock: meist lebende Koralle, dazwischen abgestorbene und Sand. */
    private BlockState reefBlock(LairContext ctx, int x, int y, int z) {
        double n = ctx.noise(x, z, 0.35, 32);
        double roll = ctx.rand(x, y * 31 + z, 33);
        if (n < 0.3 || roll < 0.12) {
            return roll < 0.5 ? s(Blocks.SAND) : s(DEAD_CORAL_BLOCKS[(int) (ctx.rand(x, z, 34) * DEAD_CORAL_BLOCKS.length)]);
        }
        return s(CORAL_BLOCKS[(int) (ctx.noise(x, z, 0.18, 35) * 4.99)]);
    }

    /** Eine Säule des Geländes: Sand, Sandstein und Korallenkalk bis zur Oberkante, darüber Wasser, darüber Luft. */
    private void column(LairContext ctx, int x, int z) {
        Profile p = this.profile(ctx, x, z);
        int ground = ctx.originalHeight(x, z, true) - 1;
        int water = this.sea - 1;
        int top = p.top();
        int bottom = Math.min(ground, top) - 4;
        boolean grass = p.kind() == Kind.ISLAND && top >= water + 2 && p.u() > 0.18 && p.u() < 0.82
                && ctx.noise(x, z, 0.14, 36) > 0.42;
        for (int y = bottom; y <= top; y++) {
            int depth = top - y;
            BlockState state = switch (p.kind()) {
                case PLATFORM -> y == top ? this.platformBlock(ctx, x, z, p.r(), Math.atan2(z - this.center.getZ(), x - this.center.getX()))
                        : depth < 3 ? s(Blocks.PRISMARINE_BRICKS) : s(Blocks.CALCITE);
                case ISLAND -> y == top ? (grass ? s(Blocks.GRASS_BLOCK) : s(Blocks.SAND))
                        : depth == 1 && grass ? s(Blocks.DIRT)
                        : depth < 3 ? s(Blocks.SAND) : depth < 5 ? s(Blocks.SANDSTONE)
                        : ctx.rand(x, y * 7 + z, 37) < 0.15 ? s(Blocks.DIORITE) : s(Blocks.CALCITE);
                case REEF -> depth == 0 && top >= water - 4 ? this.reefBlock(ctx, x, y, z)
                        : depth < 2 ? s(Blocks.SAND) : s(Blocks.CALCITE);
                case LAGOON -> y == top ? (ctx.noise(x, z, 0.075, 21) > 0.7 && ctx.rand(x, z, 23) < 0.3 ? s(Blocks.SANDSTONE) : s(Blocks.SAND))
                        : depth < 3 ? s(Blocks.SAND) : s(Blocks.SANDSTONE);
                case SLOPE -> y == top ? (p.outside() < 7.0 && ctx.rand(x, z, 38) < 0.3 ? this.reefBlock(ctx, x, y, z)
                        : ctx.rand(x, z, 26) < 0.22 ? s(Blocks.GRAVEL) : s(Blocks.SAND))
                        : depth < 3 ? s(Blocks.SAND) : s(Blocks.STONE);
            };
            ctx.set(x, y, z, state);
        }
        for (int y = top + 1; y <= water; y++) {
            ctx.set(x, y, z, s(Blocks.WATER));
        }
        for (int y = Math.max(top + 1, water + 1); y <= this.sea + 22; y++) {
            ctx.set(x, y, z, s(Blocks.AIR));
        }
        this.decorate(ctx, x, z, p, grass);
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

    /**
     * Was in einer Säule wächst und liegt: unter Wasser Seegras, Korallen, Anemonen und Muscheln; am Strand Muscheln und
     * Schildkröteneier; im Grün der Inseln Farne, Gras, Büsche und Bambus.
     */
    private void decorate(LairContext ctx, int x, int z, Profile p, boolean grass) {
        int water = this.sea - 1;
        int top = p.top();
        double roll = ctx.rand(x, z, 41);
        if (p.kind() == Kind.PLATFORM) {
            return;
        }
        if (top < water) {
            BlockState plant = null;
            if (p.kind() == Kind.REEF || p.kind() == Kind.SLOPE && p.outside() < 7.0) {
                // auf dem Riff: Korallen, Fächer und Seegurken
                if (roll < 0.22) {
                    plant = s(CORALS[(int) (ctx.rand(x, z, 48) * CORALS.length)]);
                } else if (roll < 0.36) {
                    plant = s(CORAL_FANS[(int) (ctx.rand(x, z, 43) * CORAL_FANS.length)]);
                } else if (roll < 0.42) {
                    plant = s(Blocks.SEA_PICKLE).setValue(SeaPickleBlock.PICKLES, 1 + (int) (ctx.rand(x, z, 44) * 4));
                } else if (roll < 0.47) {
                    plant = SeaBlocks.anemones().get((int) (ctx.rand(x, z, 42) * 4)).get().defaultBlockState();
                } else if (roll < 0.55) {
                    plant = s(Blocks.SEAGRASS);
                }
            } else if (roll < 0.16) {
                plant = s(Blocks.SEAGRASS);
            } else if (roll < 0.195) {
                plant = SeaBlocks.anemones().get((int) (ctx.rand(x, z, 42) * 4)).get().defaultBlockState();
            } else if (roll < 0.22) {
                plant = s(CORAL_FANS[(int) (ctx.rand(x, z, 43) * CORAL_FANS.length)]);
            } else if (roll < 0.24) {
                plant = s(Blocks.SEA_PICKLE).setValue(SeaPickleBlock.PICKLES, 1 + (int) (ctx.rand(x, z, 44) * 3));
            } else if (roll < 0.27) {
                plant = this.shells(ctx, x, z, true);
            }
            if (plant != null) {
                ctx.set(x, top + 1, z, plant);
            }
            return;
        }
        if (top == water) {
            // trockene Sandbank in der Lagune
            if (roll < 0.05 && p.r() < 26) {
                ctx.set(x, top + 1, z, this.shells(ctx, x, z, false));
            }
            return;
        }
        if (grass) {
            if (roll < 0.22) {
                ctx.set(x, top + 1, z, roll < 0.08 ? s(Blocks.FERN) : s(Blocks.SHORT_GRASS));
            } else if (roll < 0.25) {
                // ein Busch aus Dschungellaub
                BlockState bush = s(Blocks.JUNGLE_LEAVES).setValue(LeavesBlock.PERSISTENT, true);
                ctx.set(x, top + 1, z, bush);
                if (ctx.rand(x, z, 49) < 0.4) {
                    ctx.setIfOpen(x, top + 2, z, bush);
                }
            } else if (roll < 0.265) {
                int h = 2 + (int) (ctx.rand(x, z, 50) * 4);
                for (int i = 1; i <= h; i++) {
                    ctx.set(x, top + i, z, s(Blocks.BAMBOO).setValue(net.minecraft.world.level.block.BambooStalkBlock.LEAVES,
                            i == h ? net.minecraft.world.level.block.state.properties.BambooLeaves.LARGE
                                    : i == h - 1 ? net.minecraft.world.level.block.state.properties.BambooLeaves.SMALL
                                    : net.minecraft.world.level.block.state.properties.BambooLeaves.NONE));
                }
            }
            return;
        }
        // Strand der Inseln: Muscheln, hier und da ein Gelege der Meeresschildkröten
        if (roll < 0.06) {
            ctx.set(x, top + 1, z, this.shells(ctx, x, z, false));
        } else if (roll < 0.068 && top == water + 1) {
            ctx.set(x, top + 1, z, s(Blocks.TURTLE_EGG).setValue(net.minecraft.world.level.block.TurtleEggBlock.EGGS,
                    1 + (int) (ctx.rand(x, z, 51) * 3)));
        } else if (roll < 0.09) {
            ctx.set(x, top + 1, z, s(Blocks.SHORT_DRY_GRASS));
        }
    }

    private BlockState shells(LairContext ctx, int x, int z, boolean wet) {
        var kinds = SeaBlocks.shells();
        return kinds.get((int) (ctx.rand(x, z, 45) * kinds.size())).get().defaultBlockState()
                .setValue(SeashellBlock.SHELLS, 1 + (int) (ctx.rand(x, z, 46) * 2.4))
                .setValue(SeashellBlock.FACING, Direction.Plane.HORIZONTAL.stream().toList().get((int) (ctx.rand(x, z, 47) * 4)))
                .setValue(SeashellBlock.WATERLOGGED, wet);
    }

    /** Korallenköpfe in der Lagune: kleine Riffe, die bis knapp unter die Oberfläche wachsen. */
    private void coralHeads(LairContext ctx) {
        int water = this.sea - 1;
        for (int k = 0; k < 12; k++) {
            double a = ctx.rand(k, 1, 55) * Math.PI * 2.0;
            double inner = this.ringInner(ctx, a);
            double r = PLATFORM + 3.5 + ctx.rand(k, 2, 55) * Math.max(1.0, inner - PLATFORM - 7.0);
            int x0 = this.center.getX() + (int) Math.round(Math.cos(a) * r);
            int z0 = this.center.getZ() + (int) Math.round(Math.sin(a) * r);
            if (!ctx.near(x0, z0, 3)) {
                continue;
            }
            double size = 1.2 + ctx.rand(k, 3, 55) * 1.3;
            Block body = CORAL_BLOCKS[(int) (ctx.rand(k, 4, 55) * CORAL_BLOCKS.length)];
            Block second = CORAL_BLOCKS[(int) (ctx.rand(k, 5, 55) * CORAL_BLOCKS.length)];
            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    double d = Math.sqrt(dx * dx + dz * dz);
                    if (d > size) {
                        continue;
                    }
                    int x = x0 + dx;
                    int z = z0 + dz;
                    Profile p = this.profile(ctx, x, z);
                    if (p.kind() != Kind.LAGOON || p.top() >= water) {
                        continue;
                    }
                    int peak = d < size * 0.5 ? water - 1 : water - 2;
                    for (int y = p.top() + 1; y <= peak; y++) {
                        ctx.set(x, y, z, s(ctx.rand(x, y * 13 + z, 56) < 0.3 ? second : body));
                    }
                    if (peak + 1 <= water) {
                        double roll = ctx.rand(x, z, 57);
                        BlockState cap = roll < 0.45 ? s(CORALS[(int) (ctx.rand(x, z, 58) * CORALS.length)])
                                : roll < 0.75 ? s(CORAL_FANS[(int) (ctx.rand(x, z, 59) * CORAL_FANS.length)])
                                : s(Blocks.SEA_PICKLE).setValue(SeaPickleBlock.PICKLES, 1 + (int) (ctx.rand(x, z, 60) * 3));
                        ctx.set(x, peak + 1, z, cap);
                    }
                }
            }
        }
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

    /** Zwei, drei Nadeln aus hellem Korallenkalk auf den Inseln – die Wahrzeichen des Atolls, von weitem zu sehen. */
    private void pinnacles(LairContext ctx) {
        int water = this.sea - 1;
        for (int k = 0; k < 5; k++) {
            double a = ctx.rand(k, 11, 61) * Math.PI * 2.0;
            if (this.ringHeight(ctx, a) < 2.2) {
                continue;
            }
            double r = this.ringInner(ctx, a) + this.ringWidth(ctx, a) * 0.55;
            int x = this.center.getX() + (int) Math.round(Math.cos(a) * r);
            int z = this.center.getZ() + (int) Math.round(Math.sin(a) * r);
            if (!ctx.near(x, z, 4)) {
                continue;
            }
            int height = 6 + (int) (ctx.rand(k, 12, 61) * 7);
            double radius = 1.7 + ctx.rand(k, 13, 61) * 0.9;
            double leanX = (ctx.rand(k, 14, 61) - 0.5) * 0.25;
            double leanZ = (ctx.rand(k, 15, 61) - 0.5) * 0.25;
            for (int y = -4; y <= height; y++) {
                double t = Math.max(0.0, y) / (double) height;
                double rr = radius * (1.0 - 0.6 * t) + (y < 1 ? 0.8 : 0.0);
                int ox = (int) Math.round(leanX * y);
                int oz = (int) Math.round(leanZ * y);
                int reach = (int) Math.ceil(rr);
                for (int dx = -reach; dx <= reach; dx++) {
                    for (int dz = -reach; dz <= reach; dz++) {
                        double d2 = dx * dx + dz * dz;
                        double edge = rr + (ctx.rand(x + dx, y * 17 + z + dz, 62) - 0.5) * 0.8;
                        if (d2 > edge * edge) {
                            continue;
                        }
                        int by = water + 1 + y;
                        int band = Math.floorMod(by + (int) (ctx.noise(x + dx, z + dz, 0.3, 63) * 3), 4);
                        BlockState state = band == 0 ? s(Blocks.DIORITE) : band == 1 && ctx.rand(x + dx, by, 64) < 0.5
                                ? s(Blocks.SMOOTH_SANDSTONE) : s(Blocks.CALCITE);
                        ctx.set(x + dx + ox, by, z + dz + oz, state);
                    }
                }
            }
        }
    }

    /**
     * Kokospalmen auf den Inseln: ein schlanker, gebogener Stamm, oben eine Krone aus acht hängenden Wedeln, darunter
     * Kokosnüsse (Kakaofrüchte am Stamm).
     */
    private void palms(LairContext ctx) {
        int water = this.sea - 1;
        for (int k = 0; k < 26; k++) {
            double a = ctx.rand(k, 21, 71) * Math.PI * 2.0;
            if (this.ringHeight(ctx, a) < 1.5) {
                continue;
            }
            double r = this.ringInner(ctx, a) + this.ringWidth(ctx, a) * (0.2 + ctx.rand(k, 22, 71) * 0.6);
            int x = this.center.getX() + (int) Math.round(Math.cos(a) * r);
            int z = this.center.getZ() + (int) Math.round(Math.sin(a) * r);
            if (!ctx.near(x, z, 6)) {
                continue;
            }
            Profile p = this.profile(ctx, x, z);
            if (p.kind() != Kind.ISLAND || p.top() < water + 1) {
                continue;
            }
            this.palm(ctx, x, p.top() + 1, z, k);
        }
    }

    private void palm(LairContext ctx, int x, int y, int z, int k) {
        int height = 6 + (int) (ctx.rand(k, 23, 71) * 4);
        double lean = ctx.rand(k, 24, 71) * Math.PI * 2.0;
        double bend = 1.2 + ctx.rand(k, 25, 71) * 1.2;
        double lx = Math.cos(lean);
        double lz = Math.sin(lean);
        int px = x;
        int pz = z;
        for (int i = 0; i < height; i++) {
            double t = i / (double) (height - 1);
            int tx = x + (int) Math.round(lx * bend * t * t);
            int tz = z + (int) Math.round(lz * bend * t * t);
            if (tx != px || tz != pz) {
                // ein Knick: den Stamm verbunden halten
                ctx.set(tx, y + i - 1, tz, s(Blocks.JUNGLE_LOG));
            }
            ctx.set(tx, y + i, tz, s(Blocks.JUNGLE_LOG));
            px = tx;
            pz = tz;
        }
        int ty = y + height;
        BlockState leaves = s(Blocks.JUNGLE_LEAVES).setValue(LeavesBlock.PERSISTENT, true);
        ctx.set(px, ty, pz, leaves);
        ctx.setIfOpen(px, ty + 1, pz, leaves);
        for (Direction d : Direction.Plane.HORIZONTAL) {
            // lange Wedel in die vier Richtungen, die sich nach außen hinabneigen
            int[] droop = {0, 0, -1, -2};
            for (int i = 1; i <= 4; i++) {
                ctx.setIfOpen(px + d.getStepX() * i, ty + droop[i - 1], pz + d.getStepZ() * i, leaves);
            }
            ctx.setIfOpen(px + d.getStepX(), ty + 1, pz + d.getStepZ(), leaves);
            // kürzere Wedel schräg dazwischen
            Direction side = d.getClockWise();
            int sx = d.getStepX() + side.getStepX();
            int sz = d.getStepZ() + side.getStepZ();
            ctx.setIfOpen(px + sx, ty, pz + sz, leaves);
            ctx.setIfOpen(px + sx * 2, ty - 1, pz + sz * 2, leaves);
            // Kokosnüsse unter der Krone
            if (ctx.rand(k, d.get2DDataValue(), 72) < 0.45) {
                ctx.setIfOpen(px + d.getStepX(), ty - 1, pz + d.getStepZ(), s(Blocks.COCOA)
                        .setValue(net.minecraft.world.level.block.CocoaBlock.AGE, 2)
                        .setValue(net.minecraft.world.level.block.CocoaBlock.FACING, d.getOpposite()));
            }
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
