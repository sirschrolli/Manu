package io.github.sirschrolli.kingdomsatwar.worldgen.witch;

import io.github.sirschrolli.kingdomsatwar.loot.ModLootTables;
import io.github.sirschrolli.kingdomsatwar.registry.SeaBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.SeaGear;
import io.github.sirschrolli.kingdomsatwar.registry.SeaWitch;
import io.github.sirschrolli.kingdomsatwar.worldgen.lair.LairContext;
import io.github.sirschrolli.kingdomsatwar.worldgen.lair.ModStructures;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.ChainBlock;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

/**
 * Die Hexengrotte (seit 0.97), Chunk für Chunk gebaut:
 * <ol>
 *     <li>Rings um die Grotte eine <b>tote Zone</b>: Bäume, Tang und Pflanzen weichen, auf dem Grund liegen Kies, tote
 *     Korallen, Knochen und hier und da ein Schädel – so steht die Kuppel frei, auch mitten im Tangwald.</li>
 *     <li>Eine <b>Kuppel aus Gezeitenstein</b> (buckelig, mit alten Mauerresten, toten Korallen und hier und da Aquamarin),
 *     innen voll Wasser; der Boden aus Sand und Kies, mit Seegras und Leuchtalgen.</li>
 *     <li>Ein <b>Eingang</b> zur offenen See: ein Gang durch die <b>Rippen eines Wals</b> (drei Bögen aus Knochen mit Schädeln
 *     und einer Seelenlaterne), davor ein Pfad aus Kies und Knochen zwischen zwei Laternenpfosten.</li>
 *     <li>In der Mitte ein <b>Podest</b> aus Gezeitensteinziegeln, darauf der <b>Hexenkessel</b> – über ihm schwebt die
 *     Meerhexe; ringsum vier Säulen aus Knochen mit Seelenlaternen, dahinter zwei <b>Blasensäulen</b> über Seelensand (in
 *     ihnen bekommt man Luft).</li>
 *     <li>An den Wänden <b>Nischen</b> mit Braustand, Kerzen, Schädeln und Töpfen; von der Decke hängen Laternen an Ketten,
 *     Leuchtflechten überziehen die Decke.</li>
 *     <li>Hinten das <b>Wrack</b> eines Bootes (Rumpf und Mast) mit einer Truhe, eine zweite Truhe hinter dem Kessel.</li>
 * </ol>
 */
public class WitchGrottoPiece extends StructurePiece {
    /** Halbmesser der Kuppel (außen) und ihre Höhe über dem Boden. */
    public static final int RADIUS = 16;
    public static final int HEIGHT = 14;
    /** So dick ist die Schale. */
    public static final int SHELL = 2;
    /** Bis hierhin reicht das Bauwerk (die tote Zone rings um die Kuppel, der Pfad vor dem Eingang). */
    public static final int REACH = RADIUS + 10;
    /** Wo der Kessel steht (über dem Boden) … */
    public static final int CAULDRON_Y = 2;
    /** … und so hoch über dem Boden schwebt die Hexe (drei Blöcke über dem Kessel). */
    public static final int HOVER = CAULDRON_Y + 3;
    /** So weit ragt die Wand höchstens herein (die Kuppel ist buckelig). */
    private static final double BUMP = 1.3;

    private final BlockPos center;
    private final long seed;
    private final int sea;

    public WitchGrottoPiece(BlockPos center, long seed, int sea) {
        super(ModStructures.WITCH_GROTTO_PIECE.get(), 0, makeBox(center));
        this.center = center;
        this.seed = seed;
        this.sea = sea;
    }

    public WitchGrottoPiece(CompoundTag tag) {
        super(ModStructures.WITCH_GROTTO_PIECE.get(), tag);
        this.center = new BlockPos(tag.getIntOr("CX", 0), tag.getIntOr("CY", 60), tag.getIntOr("CZ", 0));
        this.seed = tag.getLongOr("Seed", 0L);
        this.sea = tag.getIntOr("Sea", 96);
    }

    public static BoundingBox makeBox(BlockPos c) {
        return new BoundingBox(c.getX() - REACH, c.getY() - 4, c.getZ() - REACH, c.getX() + REACH, c.getY() + HEIGHT + 20, c.getZ() + REACH);
    }

    /**
     * Hält einen Punkt im Inneren der Kuppel um {@code center}: den Fußpunkt eines Körpers der Höhe {@code body}, mindestens
     * {@code margin} Blöcke von Wand und Decke entfernt und über dem Boden (für die Hexe und für Kameras).
     */
    public static net.minecraft.world.phys.Vec3 keepInside(BlockPos center, net.minecraft.world.phys.Vec3 p, double body, double margin) {
        double ri = RADIUS - SHELL - BUMP;
        double cx = center.getX() + 0.5;
        double cz = center.getZ() + 0.5;
        double dx = p.x - cx;
        double dz = p.z - cz;
        double flat = Math.sqrt(dx * dx + dz * dz);
        double max = Math.max(0.0, ri - margin - 0.8);
        if (flat > max) {
            dx *= max / flat;
            dz *= max / flat;
            flat = max;
        }
        double k = Math.max(0.0, 1.0 - (flat / ri) * (flat / ri));
        double bottom = center.getY() + 1.0 + Math.min(margin, 0.4);
        double top = center.getY() + 1.0 + (HEIGHT - SHELL) * Math.sqrt(k) - margin - body;
        return new net.minecraft.world.phys.Vec3(cx + dx, Math.max(bottom, Math.min(p.y, top)), cz + dz);
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        tag.putInt("CX", this.center.getX());
        tag.putInt("CY", this.center.getY());
        tag.putInt("CZ", this.center.getZ());
        tag.putLong("Seed", this.seed);
        tag.putInt("Sea", this.sea);
    }

    public BlockPos center() {
        return this.center;
    }

    /** In welche Richtung der Eingang dieser Grotte zeigt. */
    public Direction door() {
        return entrance(this.seed);
    }

    /** Wo der Hexenkessel steht. */
    public static BlockPos cauldron(BlockPos center) {
        return center.above(CAULDRON_Y);
    }

    /** In welche Richtung der Eingang zeigt (aus dem Startwert). */
    public static Direction entrance(long seed) {
        return Direction.from2DDataValue((int) Math.floorMod(seed >>> 7, 4));
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator, RandomSource random,
            BoundingBox chunkBB, net.minecraft.world.level.ChunkPos chunkPos, BlockPos referencePos) {
        build(level, chunkBB, this.center, this.seed, this.sea, random, this);
    }

    /**
     * Baut die Grotte (für die Welt Chunk für Chunk, für Tests in einem Zug). {@code piece} legt die Truhen an (null: die
     * Truhen bekommen ihre Beute direkt).
     */
    public static void build(WorldGenLevel level, BoundingBox box, BlockPos center, long seed, int sea, RandomSource random,
            WitchGrottoPiece piece) {
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
        final Direction door;

        Builder(WorldGenLevel level, BoundingBox box, BlockPos center, long seed, int sea) {
            this.level = level;
            this.box = box;
            this.ctx = new LairContext(level, box, center, seed);
            this.cx = center.getX();
            this.cz = center.getZ();
            this.f = center.getY();
            this.sea = sea;
            this.door = entrance(seed);
        }

        boolean in(int x, int y, int z) {
            return this.box.isInside(x, y, z) && y > this.level.getMinY() && y < this.level.getMaxY();
        }

        void set(int x, int y, int z, BlockState state) {
            if (!this.in(x, y, z)) {
                return;
            }
            if (y < this.sea && state.hasProperty(BlockStateProperties.WATERLOGGED)) {
                state = state.setValue(BlockStateProperties.WATERLOGGED, true);
            }
            this.level.setBlock(new BlockPos(x, y, z), state, Block.UPDATE_CLIENTS);
        }

        void set(int x, int y, int z, Block block) {
            this.set(x, y, z, block.defaultBlockState());
        }

        void water(int x, int y, int z) {
            if (this.in(x, y, z)) {
                this.level.setBlock(new BlockPos(x, y, z), y < this.sea ? Blocks.WATER.defaultBlockState() : Blocks.AIR.defaultBlockState(),
                        Block.UPDATE_CLIENTS);
            }
        }

        double rand(int a, int b, int salt) {
            return this.ctx.rand(a, b, salt);
        }

        /** Außenhalbmesser der Kuppel in dieser Richtung (buckelig). */
        double outer(double angle) {
            return RADIUS + (this.ctx.angleNoise(angle, 3.0, 11) - 0.5) * 2.6;
        }

        /** Liegt (x, y, z) im Gang des Eingangs (ein Spitzbogen, fünf Blöcke breit, sechs hoch)? */
        boolean doorway(int x, int y, int z) {
            int along = (x - this.cx) * this.door.getStepX() + (z - this.cz) * this.door.getStepZ();
            int across = (x - this.cx) * this.door.getStepZ() - (z - this.cz) * this.door.getStepX();
            int dy = y - this.f;
            if (along < RADIUS - SHELL - 4 || along > RADIUS + 4 || dy < 1 || dy > 6) {
                return false;
            }
            return Math.abs(across) <= (dy <= 4 ? 2 : dy == 5 ? 1 : 0);
        }

        void run(RandomSource random, WitchGrottoPiece piece) {
            this.clearing();
            this.shell();
            this.floor();
            this.dais();
            this.pillars();
            this.niches();
            this.ceiling();
            this.bubbles();
            this.wreck(random, piece);
            this.outside();
            this.ribs();
            this.cauldron(random, piece);
        }

        // ------------------------------------------------------------------ die tote Zone

        /** Oberster Geländeblock der Säule unter dem Meeresspiegel (Bäume, Tang und Pflanzen zählen nicht) – oder -1. */
        private int groundTop(int x, int z) {
            BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos(x, Math.min(this.box.maxY(), this.sea - 1), z);
            int lowest = Math.max(this.level.getMinY() + 1, this.f - 14);
            for (; p.getY() > lowest; p.move(Direction.DOWN)) {
                if (LairContext.isGround(this.level.getBlockState(p))) {
                    return p.getY();
                }
            }
            return -1;
        }

        /** Rings um die Grotte wächst nichts mehr: Bäume, Tang und Pflanzen weichen, auf dem Grund liegen tote Korallen und Knochen. */
        private void clearing() {
            for (int x = this.box.minX(); x <= this.box.maxX(); x++) {
                for (int z = this.box.minZ(); z <= this.box.maxZ(); z++) {
                    double dx = x - this.cx;
                    double dz = z - this.cz;
                    double d = Math.sqrt(dx * dx + dz * dz);
                    double edge = REACH - 1.5 - this.ctx.angleNoise(Math.atan2(dz, dx), 5.0, 81) * 4.5;
                    if (d > edge || !this.in(x, this.f, z)) {
                        continue;
                    }
                    int ground = this.groundTop(x, z);
                    if (ground < 0) {
                        continue;
                    }
                    BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos(x, ground + 1, z);
                    for (; p.getY() < this.sea && p.getY() <= this.box.maxY(); p.move(Direction.UP)) {
                        BlockState state = this.level.getBlockState(p);
                        if (!state.isAir() && !state.is(Blocks.WATER)) {
                            this.water(x, p.getY(), z);
                        }
                    }
                    if (d > RADIUS + 1.0 && ground < this.sea - 2) {
                        this.deadGround(x, ground, z, d > edge - 2.0);
                    }
                }
            }
        }

        /** Der Grund der toten Zone: Kies in Flecken, tote Korallen, Knochen, selten ein Schädel (am Rand weniger). */
        private void deadGround(int x, int g, int z, boolean rim) {
            BlockState top = this.level.getBlockState(new BlockPos(x, g, z));
            boolean soft = top.is(Blocks.SAND) || top.is(Blocks.RED_SAND) || top.is(Blocks.GRAVEL) || top.is(Blocks.CLAY)
                    || top.is(Blocks.MUD) || top.is(Blocks.DIRT) || top.is(SeaBlocks.TIDE_STONE.base().get());
            if (!soft) {
                return;
            }
            if (this.ctx.noise(x, z, 0.16, 85) > (rim ? 0.72 : 0.6)) {
                this.set(x, g, z, Blocks.GRAVEL);
            }
            double r = this.rand(x, z, 83) * (rim ? 2.0 : 1.0);
            BlockState thing = null;
            if (r < 0.03) {
                thing = (switch ((int) (this.rand(x, z, 87) * 4)) {
                    case 0 -> Blocks.DEAD_BRAIN_CORAL_FAN;
                    case 1 -> Blocks.DEAD_TUBE_CORAL_FAN;
                    case 2 -> Blocks.DEAD_HORN_CORAL_FAN;
                    default -> Blocks.DEAD_FIRE_CORAL_FAN;
                }).defaultBlockState();
            } else if (r < 0.045) {
                thing = (this.rand(x, z, 89) < 0.5 ? Blocks.DEAD_BUBBLE_CORAL : Blocks.DEAD_HORN_CORAL).defaultBlockState();
            } else if (r < 0.052) {
                thing = Blocks.BONE_BLOCK.defaultBlockState().setValue(RotatedPillarBlock.AXIS,
                        this.rand(x, z, 91) < 0.5 ? Direction.Axis.X : Direction.Axis.Z);
            } else if (r < 0.056) {
                thing = (this.rand(x, z, 93) < 0.5 ? Blocks.DEAD_BRAIN_CORAL_BLOCK : Blocks.DEAD_TUBE_CORAL_BLOCK).defaultBlockState();
            } else if (r < 0.058) {
                thing = Blocks.DRIED_KELP_BLOCK.defaultBlockState();
            } else if (r < 0.059) {
                thing = Blocks.SKELETON_SKULL.defaultBlockState().setValue(SkullBlock.ROTATION, (int) (this.rand(x, z, 95) * 16));
            }
            if (thing != null) {
                this.set(x, g + 1, z, thing);
            }
        }

        // ------------------------------------------------------------------ Schale und Inneres

        private BlockState shellBlock(int x, int y, int z) {
            double n = this.rand(x * 31 + y, z * 17 - y, 21);
            if (n < 0.025) {
                return SeaGear.AQUAMARINE_ORE.get().defaultBlockState();
            }
            if (n < 0.13) {
                return SeaBlocks.TIDE_STONE_BRICKS.base().get().defaultBlockState();
            }
            if (n < 0.2) {
                return Blocks.DEAD_TUBE_CORAL_BLOCK.defaultBlockState();
            }
            if (n < 0.24) {
                return Blocks.DEAD_BRAIN_CORAL_BLOCK.defaultBlockState();
            }
            if (n < 0.27) {
                return Blocks.GRAVEL.defaultBlockState();
            }
            return SeaBlocks.TIDE_STONE.base().get().defaultBlockState();
        }

        private void shell() {
            for (int x = this.box.minX(); x <= this.box.maxX(); x++) {
                for (int z = this.box.minZ(); z <= this.box.maxZ(); z++) {
                    double dx = x - this.cx;
                    double dz = z - this.cz;
                    double rh = Math.sqrt(dx * dx + dz * dz);
                    double ro = this.outer(Math.atan2(dz, dx));
                    if (rh > ro + 0.5) {
                        continue;
                    }
                    double ri = ro - SHELL;
                    for (int y = this.f - 3; y <= this.f + HEIGHT + 2; y++) {
                        if (!this.in(x, y, z)) {
                            continue;
                        }
                        int dy = y - this.f;
                        if (dy <= 0) {
                            // Fundament: bis zum Grund hinab aufgefüllt
                            this.set(x, y, z, dy == 0 ? Blocks.SAND.defaultBlockState() : SeaBlocks.TIDE_STONE.base().get().defaultBlockState());
                            continue;
                        }
                        double eo = Math.sqrt((rh / ro) * (rh / ro) + (dy / (double) HEIGHT) * (dy / (double) HEIGHT));
                        double ei = Math.sqrt((rh / ri) * (rh / ri) + (dy / (double) (HEIGHT - SHELL)) * (dy / (double) (HEIGHT - SHELL)));
                        if (ei <= 1.0 || this.doorway(x, y, z)) {
                            this.water(x, y, z);
                        } else if (eo <= 1.0) {
                            this.set(x, y, z, this.shellBlock(x, y, z));
                        } else if (dy <= 2) {
                            this.water(x, y, z);                  // was außen an der Kuppel klebt (Tang, Seegras), weicht
                        }
                    }
                }
            }
        }

        private boolean inside(int x, int z, double margin) {
            double dx = x - this.cx;
            double dz = z - this.cz;
            return Math.sqrt(dx * dx + dz * dz) < this.outer(Math.atan2(dz, dx)) - SHELL - margin;
        }

        private void floor() {
            for (int x = this.cx - RADIUS; x <= this.cx + RADIUS; x++) {
                for (int z = this.cz - RADIUS; z <= this.cz + RADIUS; z++) {
                    if (!this.inside(x, z, 0.0) || !this.in(x, this.f, z)) {
                        continue;
                    }
                    double n = this.ctx.noise(x, z, 0.22, 31);
                    BlockState ground = n > 0.7 ? Blocks.GRAVEL.defaultBlockState() : n < 0.2 ? SeaBlocks.TIDE_STONE.base().get().defaultBlockState()
                            : Blocks.SAND.defaultBlockState();
                    this.set(x, this.f, z, ground);
                    double r = this.rand(x, z, 33);
                    double d = Math.hypot(x - this.cx, z - this.cz);
                    if (d > 5.0 && r < 0.12) {
                        this.set(x, this.f + 1, z, Blocks.SEAGRASS.defaultBlockState());
                    } else if (d > 5.0 && r < 0.17) {
                        this.set(x, this.f + 1, z, SeaBlocks.GLOW_ALGAE.get().defaultBlockState());
                    } else if (d > 6.0 && r < 0.19 && this.inside(x, z, 1.5)) {
                        this.set(x, this.f + 1, z, Blocks.BONE_BLOCK.defaultBlockState()
                                .setValue(RotatedPillarBlock.AXIS, r < 0.18 ? Direction.Axis.X : Direction.Axis.Z));
                    }
                }
            }
        }

        // ------------------------------------------------------------------ Podest, Säulen, Nischen

        private void dais() {
            for (int dx = -5; dx <= 5; dx++) {
                for (int dz = -5; dz <= 5; dz++) {
                    double d = Math.hypot(dx, dz);
                    if (d > 3.3) {
                        if (d <= 4.6) {
                            this.set(this.cx + dx, this.f, this.cz + dz, SeaBlocks.TIDE_STONE_BRICKS.base().get().defaultBlockState());
                        }
                        continue;
                    }
                    int x = this.cx + dx;
                    int z = this.cz + dz;
                    this.set(x, this.f, z, SeaBlocks.TIDE_STONE_BRICKS.base().get().defaultBlockState());
                    BlockState top = d < 1.2 ? SeaBlocks.CHISELED_TIDE_STONE.get().defaultBlockState()
                            : d > 2.5 ? SeaBlocks.TIDE_STONE_BRICKS.slab().get().defaultBlockState()
                            : SeaBlocks.TIDE_STONE_BRICKS.base().get().defaultBlockState();
                    this.set(x, this.f + 1, z, top);
                    if (d < 2.6 && this.in(x, this.f + 2, z)) {
                        this.water(x, this.f + 2, z);
                    }
                }
            }
        }

        private void pillars() {
            for (int[] p : new int[][] {{6, 6}, {-6, 6}, {6, -6}, {-6, -6}}) {
                int x = this.cx + p[0] - Integer.signum(p[0]);
                int z = this.cz + p[1] - Integer.signum(p[1]);
                for (int y = this.f + 1; y <= this.f + 4; y++) {
                    this.set(x, y, z, Blocks.BONE_BLOCK.defaultBlockState());
                }
                this.set(x, this.f + 5, z, Blocks.SOUL_LANTERN.defaultBlockState());
            }
        }

        /** Zwei Blasensäulen über Seelensand hinter dem Kessel, bis unter die Decke (in ihnen bekommt man Luft). */
        private void bubbles() {
            Direction back = this.door.getOpposite();
            Direction side = back.getClockWise();
            for (int s : new int[] {-5, 5}) {
                int x = this.cx + back.getStepX() * 3 + side.getStepX() * s;
                int z = this.cz + back.getStepZ() * 3 + side.getStepZ() * s;
                int top = this.ceilingY(x, z);
                if (top < 0 || !this.in(x, this.f, z)) {
                    continue;
                }
                this.set(x, this.f, z, Blocks.SOUL_SAND);
                for (int y = this.f + 1; y < top; y++) {
                    if (this.in(x, y, z) && this.sea > y) {
                        this.level.setBlock(new BlockPos(x, y, z), Blocks.BUBBLE_COLUMN.defaultBlockState()
                                .setValue(net.minecraft.world.level.block.BubbleColumnBlock.DRAG_DOWN, false), Block.UPDATE_CLIENTS);
                    }
                }
                // ein Kranz aus Seegurken um den Fuß
                for (Direction d : Direction.Plane.HORIZONTAL) {
                    if (this.rand(x + d.getStepX(), z + d.getStepZ(), 97) < 0.5) {
                        this.set(x + d.getStepX(), this.f + 1, z + d.getStepZ(), Blocks.SEA_PICKLE.defaultBlockState()
                                .setValue(BlockStateProperties.PICKLES, 1 + (int) (this.rand(x, z + d.ordinal(), 99) * 3)));
                    }
                }
            }
        }

        private void niches() {
            int count = 9;
            double a0 = this.rand(1, 2, 41) * Math.PI * 2.0;
            for (int i = 0; i < count; i++) {
                double a = a0 + i * Math.PI * 2.0 / count;
                double r = this.outer(a) - SHELL - 1.2;
                int x = this.cx + (int) Math.round(Math.cos(a) * r);
                int z = this.cz + (int) Math.round(Math.sin(a) * r);
                if (this.doorway(x, this.f + 1, z) || this.near(x, z, this.door, 4)) {
                    continue;
                }
                this.set(x, this.f + 1, z, SeaBlocks.TIDE_STONE_BRICKS.base().get().defaultBlockState());
                BlockState thing = switch (i % 5) {
                    case 0 -> Blocks.BREWING_STAND.defaultBlockState();
                    case 1 -> Blocks.CANDLE.defaultBlockState().setValue(CandleBlock.CANDLES, 3);
                    case 2 -> Blocks.SKELETON_SKULL.defaultBlockState().setValue(SkullBlock.ROTATION,
                            Math.floorMod(Math.round((float) (Math.toDegrees(a) + 90.0) / 22.5f), 16));
                    case 3 -> Blocks.DECORATED_POT.defaultBlockState();
                    default -> Blocks.SEA_PICKLE.defaultBlockState().setValue(BlockStateProperties.PICKLES, 3);
                };
                this.set(x, this.f + 2, z, thing);
            }
        }

        /** Liegt (x, z) nahe beim Eingang? */
        private boolean near(int x, int z, Direction d, int margin) {
            int along = (x - this.cx) * d.getStepX() + (z - this.cz) * d.getStepZ();
            int across = (x - this.cx) * d.getStepZ() - (z - this.cz) * d.getStepX();
            return along > RADIUS - SHELL - 6 && Math.abs(across) <= margin;
        }

        private void ceiling() {
            // Laternen an Ketten
            for (int[] p : new int[][] {{0, 8}, {8, 0}, {0, -8}, {-8, 0}}) {
                int x = this.cx + p[0];
                int z = this.cz + p[1];
                int top = this.ceilingY(x, z);
                if (top < this.f + 10 || this.doorway(x, this.f + 1, z) || this.near(x, z, this.door, 3)) {
                    continue;
                }
                for (int y = top - 1; y >= this.f + 9; y--) {
                    this.set(x, y, z, Blocks.IRON_CHAIN.defaultBlockState().setValue(ChainBlock.AXIS, Direction.Axis.Y));
                }
                this.set(x, this.f + 8, z, Blocks.SOUL_LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));
            }
            // Leuchtflechten an der Decke
            for (int x = this.cx - RADIUS; x <= this.cx + RADIUS; x++) {
                for (int z = this.cz - RADIUS; z <= this.cz + RADIUS; z++) {
                    if (!this.inside(x, z, 1.0) || this.rand(x, z, 51) > 0.16) {
                        continue;
                    }
                    int top = this.ceilingY(x, z);
                    if (top > this.f + 7) {
                        this.set(x, top - 1, z, Blocks.GLOW_LICHEN.defaultBlockState().setValue(BlockStateProperties.UP, true));
                    }
                }
            }
        }

        /** Unterkante der Decke über (x, z) – der erste Block der Schale über dem Boden (oder -1). */
        private int ceilingY(int x, int z) {
            double dx = x - this.cx;
            double dz = z - this.cz;
            double rh = Math.sqrt(dx * dx + dz * dz);
            double ri = this.outer(Math.atan2(dz, dx)) - SHELL;
            if (rh >= ri) {
                return -1;
            }
            double k = 1.0 - (rh / ri) * (rh / ri);
            return this.f + (int) Math.floor((HEIGHT - SHELL) * Math.sqrt(k)) + 1;
        }

        // ------------------------------------------------------------------ Wrack, Truhen, Kessel

        private void wreck(RandomSource random, WitchGrottoPiece piece) {
            Direction back = this.door.getOpposite();
            Direction side = back.getClockWise();
            int bx = this.cx + back.getStepX() * 9;
            int bz = this.cz + back.getStepZ() * 9;
            // ein Stück Rumpf: schräge Planken, ein Kiel, Spanten
            for (int s = -4; s <= 4; s++) {
                int x = bx + side.getStepX() * s;
                int z = bz + side.getStepZ() * s;
                int h = 1 + (Math.abs(s) >= 2 ? 1 : 0) + (Math.abs(s) >= 3 ? 1 : 0) + (Math.abs(s) >= 4 ? 1 : 0);
                this.set(x, this.f + 1, z, Blocks.DARK_OAK_PLANKS.defaultBlockState());
                for (int k = 1; k <= h; k++) {
                    if (this.rand(x, k, 61) < 0.2) {
                        continue;                                  // Löcher im Rumpf
                    }
                    this.set(x + back.getStepX(), this.f + 1 + k, z + back.getStepZ(),
                            Blocks.DARK_OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, this.door));
                }
            }
            // der Mast, schräg an die Wand gelehnt
            for (int k = 0; k < 7; k++) {
                int x = bx + side.getStepX() * (k / 2 - 1) + back.getStepX() * (k / 4);
                int z = bz + side.getStepZ() * (k / 2 - 1) + back.getStepZ() * (k / 4);
                this.set(x, this.f + 2 + k, z, Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState());
            }
            BlockPos chest = new BlockPos(bx - side.getStepX() * 2, this.f + 2, bz - side.getStepZ() * 2);
            this.chest(chest, this.door, random, piece);
        }

        private void chest(BlockPos pos, Direction facing, RandomSource random, WitchGrottoPiece piece) {
            if (!this.box.isInside(pos)) {
                return;
            }
            BlockState state = Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, facing).setValue(ChestBlock.WATERLOGGED, true);
            if (piece != null) {
                piece.createChest(this.level, this.box, random, pos, ModLootTables.WITCH_GROTTO_CHEST, state);
            } else {
                this.level.setBlock(pos, state, Block.UPDATE_CLIENTS);
                if (this.level.getBlockEntity(pos) instanceof ChestBlockEntity be) {
                    be.setLootTable(ModLootTables.WITCH_GROTTO_CHEST, random.nextLong());
                }
            }
        }

        private void cauldron(RandomSource random, WitchGrottoPiece piece) {
            BlockPos pos = WitchGrottoPiece.cauldron(new BlockPos(this.cx, this.f, this.cz));
            if (this.box.isInside(pos)) {
                this.level.setBlock(pos, SeaWitch.WITCH_CAULDRON.get().defaultBlockState(), Block.UPDATE_CLIENTS);
            }
            Direction back = this.door.getOpposite();
            this.chest(pos.relative(back, 2).relative(back.getClockWise()).below(), this.door, random, piece);
        }

        // ------------------------------------------------------------------ Eingang und Umgebung

        /**
         * Der Gang durch die Rippen eines Wals: drei Spitzbögen aus Knochen vor dem Eingang (der erste in der Wand der Kuppel),
         * auf dem ersten ein Schädel, an den Schultern des zweiten zwei Schädel, unter seinem Scheitel eine Seelenlaterne.
         */
        private void ribs() {
            Direction d = this.door;
            Direction side = d.getClockWise();
            int first = (int) Math.floor(this.outer(Math.atan2(d.getStepZ(), d.getStepX())));
            int[][] ribs = {{first, 3, 5}, {first + 3, 3, 4}, {first + 6, 2, 4}};
            int rot = Math.floorMod(Math.round(-d.toYRot() / 22.5f) + 8, 16);
            for (int i = 0; i < ribs.length; i++) {
                int along = ribs[i][0];
                int half = ribs[i][1];
                int posts = ribs[i][2];
                int ax = this.cx + d.getStepX() * along;
                int az = this.cz + d.getStepZ() * along;
                int y = this.f + 1;
                for (; y <= this.f + posts; y++) {
                    for (int s : new int[] {-half, half}) {
                        this.set(ax + side.getStepX() * s, y, az + side.getStepZ() * s, Blocks.BONE_BLOCK.defaultBlockState());
                    }
                }
                for (int k = half - 1; k >= 0; k--, y++) {
                    for (int s : new int[] {-k, k}) {
                        this.set(ax + side.getStepX() * s, y, az + side.getStepZ() * s, Blocks.BONE_BLOCK.defaultBlockState()
                                .setValue(RotatedPillarBlock.AXIS, k == 0 ? side.getAxis() : Direction.Axis.Y));
                    }
                }
                if (i == 0) {
                    this.set(ax, y, az, Blocks.SKELETON_SKULL.defaultBlockState().setValue(SkullBlock.ROTATION, rot));
                } else if (i == 1) {
                    for (int s : new int[] {-(half - 1), half - 1}) {
                        this.set(ax + side.getStepX() * s, this.f + posts + 2, az + side.getStepZ() * s,
                                Blocks.SKELETON_SKULL.defaultBlockState().setValue(SkullBlock.ROTATION, rot));
                    }
                    this.set(ax, y - 2, az, Blocks.SOUL_LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));
                }
            }
        }

        private void outside() {
            Direction d = this.door;
            Direction side = d.getClockWise();
            int first = (int) Math.floor(this.outer(Math.atan2(d.getStepZ(), d.getStepX())));
            // der Pfad: am Eingang eben (auf Höhe des Bodens, darunter aufgefüllt), weiter draußen auf dem Grund
            for (int k = first - 1; k <= REACH - 3; k++) {
                for (int s = -2; s <= 2; s++) {
                    int x = this.cx + d.getStepX() * k + side.getStepX() * s;
                    int z = this.cz + d.getStepZ() * k + side.getStepZ() * s;
                    if (!this.in(x, this.f, z)) {
                        continue;
                    }
                    boolean flat = k <= first + 7;
                    int top = flat ? this.f : this.groundTop(x, z);
                    if (top < 0 || Math.abs(top - this.f) > 4) {
                        continue;
                    }
                    if (flat) {
                        for (int y = this.f - 3; y < this.f; y++) {
                            if (!LairContext.isGround(this.level.getBlockState(new BlockPos(x, y, z)))) {
                                this.set(x, y, z, SeaBlocks.TIDE_STONE.base().get());
                            }
                        }
                        for (int y = this.f + 1; y <= this.f + 8; y++) {
                            this.water(x, y, z);
                        }
                    }
                    if (Math.abs(s) <= 1) {
                        this.set(x, top, z, this.rand(x, z, 71) < 0.15 ? Blocks.BONE_BLOCK.defaultBlockState() : Blocks.GRAVEL.defaultBlockState());
                    } else if (flat) {
                        this.set(x, top, z, this.rand(x, z, 73) < 0.5 ? Blocks.GRAVEL.defaultBlockState()
                                : SeaBlocks.TIDE_STONE.base().get().defaultBlockState());
                    }
                }
            }
            // zwei Laternenpfosten aus Knochen am Anfang des Pfads
            int k = REACH - 4;
            for (int s : new int[] {-3, 3}) {
                int x = this.cx + d.getStepX() * k + side.getStepX() * s;
                int z = this.cz + d.getStepZ() * k + side.getStepZ() * s;
                int top = this.in(x, this.f, z) ? this.groundTop(x, z) : -1;
                if (top < 0 || Math.abs(top - this.f) > 4) {
                    continue;
                }
                this.set(x, top + 1, z, Blocks.BONE_BLOCK);
                this.set(x, top + 2, z, Blocks.BONE_BLOCK);
                this.set(x, top + 3, z, Blocks.SOUL_LANTERN);
            }
        }
    }

    /** Wie die Kuppel aussieht, für Tests: Halbmesser innen über dem Boden in der Mitte. */
    public static int innerHeight() {
        return HEIGHT - SHELL;
    }
}
