package io.github.sirschrolli.kingdomsatwar.worldgen.kraken;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.block.sea.KrakenCoreBlockEntity;
import io.github.sirschrolli.kingdomsatwar.registry.ModBlocks;
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
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.storage.loot.LootTable;

/**
 * Der Krakenschlund (seit 0.91): eine Stelle im offenen Urmeer, an der sich der Grund zu einem weiten Becken senkt.
 * <ol>
 *     <li>In der Mitte der <b>Schlund</b>: ein Trichter aus Abgrundstein, gut siebzig Blöcke unter dem Meeresspiegel, an
 *     seinen Wänden glimmen Meereslaternen wie Augen; ganz unten das <b>Herz des Schlunds</b>. Dort schläft der Kraken.</li>
 *     <li>Ringsum das <b>Becken</b>: Abgrundsand, Kies und Gezeitenstein, zum Schlund hin tiefer; darauf das Gerippe eines
 *     Seedrachens, das Wrack eines Schiffs (mit einer Truhe), Röhrenwürmer und Schlote am Rand des Schlunds, Leuchtalgen,
 *     Anemonen, Muscheln und hoher Riesentang.</li>
 *     <li>Darum der <b>Felsenring</b>: zwölf Brandungsfelsen aus Gezeitenstein, die schief aus der Tiefe bis hoch über das
 *     Wasser ragen (manche abgebrochen, knapp unter der Oberfläche), zwischen einigen ein Felsbogen. Wer den Ring betritt,
 *     weckt den Kraken.</li>
 * </ol>
 */
public class KrakenMawPiece extends StructurePiece {
    /** Radius, bis zu dem der Schlund sein Gelände formt. */
    static final int REACH = 46;
    /** Radius des Schlunds am Grund des Beckens. */
    static final double MAW = 8.5;
    /** Mittlerer Radius des Felsenrings. */
    static final double RING = 33.0;
    /** So tief unter dem Meeresspiegel liegt der Boden des Schlunds. */
    public static final int MAW_DEPTH = 72;
    static final int STACKS = 12;
    public static final ResourceKey<LootTable> CHEST = ResourceKey.create(Registries.LOOT_TABLE, KingdomsAtWar.id("chests/kraken_maw"));

    private final BlockPos center;
    private final long seed;
    private final int sea;

    public KrakenMawPiece(BlockPos center, long seed, int sea) {
        super(ModStructures.KRAKEN_MAW_PIECE.get(), 0, makeBox(center, sea));
        this.center = center;
        this.seed = seed;
        this.sea = sea;
    }

    public KrakenMawPiece(CompoundTag tag) {
        super(ModStructures.KRAKEN_MAW_PIECE.get(), tag);
        this.center = new BlockPos(tag.getIntOr("CX", 0), tag.getIntOr("CY", 96), tag.getIntOr("CZ", 0));
        this.seed = tag.getLongOr("Seed", 0L);
        this.sea = tag.getIntOr("Sea", 96);
    }

    private static BoundingBox makeBox(BlockPos c, int sea) {
        return new BoundingBox(c.getX() - REACH, sea - MAW_DEPTH - 4, c.getZ() - REACH, c.getX() + REACH, sea + 24, c.getZ() + REACH);
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        tag.putInt("CX", this.center.getX());
        tag.putInt("CY", this.center.getY());
        tag.putInt("CZ", this.center.getZ());
        tag.putLong("Seed", this.seed);
        tag.putInt("Sea", this.sea);
    }

    /** Boden des Schlunds (dort liegt das Herz). */
    public static int bottom(int sea) {
        return sea - MAW_DEPTH;
    }

    private static BlockState s(Block block) {
        return block.defaultBlockState();
    }

    // =====================================================================================
    // Form
    // =====================================================================================

    /** Ein Brandungsfelsen: Fuß (x, z) und Radius, Neigung bis zur Spitze, Höhe der Spitze. */
    private record Stack(double x, double z, double radius, double leanX, double leanZ, int top) {}

    private Stack stack(LairContext ctx, int i) {
        double a = (i + (ctx.rand(i, 1, 41) - 0.5) * 0.45) * Math.PI * 2.0 / STACKS;
        double r = RING + (ctx.rand(i, 2, 41) - 0.5) * 4.0;
        double x = this.center.getX() + 0.5 + Math.cos(a) * r;
        double z = this.center.getZ() + 0.5 + Math.sin(a) * r;
        double radius = 2.6 + ctx.rand(i, 3, 41) * 1.5;
        double lean = ctx.rand(i, 4, 41) * 2.4;
        double la = ctx.rand(i, 5, 41) * Math.PI * 2.0;
        boolean broken = ctx.rand(i, 6, 41) < 0.25;
        int top = broken ? this.sea - 3 - (int) (ctx.rand(i, 7, 41) * 6.0) : this.sea + 5 + (int) (ctx.rand(i, 7, 41) * 11.0);
        return new Stack(x, z, radius, Math.cos(la) * lean, Math.sin(la) * lean, top);
    }

    /** Oberkante des Grunds (oberster fester Block) – im Schlund dessen Boden. */
    private int floor(LairContext ctx, int x, int z, double r, int natural) {
        double n = (ctx.noise(x, z, 0.09, 42) - 0.5) * 3.0 + (ctx.noise(x, z, 0.3, 43) - 0.5) * 1.2;
        if (r <= 30.0) {
            double t = Mth.clamp((r - MAW) / (30.0 - MAW), 0.0, 1.0);
            return (int) Math.round(this.sea - 47 + t * 10.0 + n * (0.4 + 0.6 * t));
        }
        int ring = (int) Math.round(this.sea - 37 + n);
        if (r <= 37.0) {
            return ring;
        }
        double t = LairContext.smooth(Mth.clamp((r - 37.0) / (REACH - 37.0), 0.0, 1.0));
        return (int) Math.round(Mth.lerp(t, ring, Math.min(natural, this.sea - 4)));
    }

    /** Radius des Trichters in der Höhe {@code y} (unten enger). */
    private double funnel(int y) {
        int bottom = bottom(this.sea);
        double t = Mth.clamp((y - bottom) / (double) (this.sea - 47 - bottom), 0.0, 1.0);
        return 5.5 + (MAW - 5.5) * t;
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator, RandomSource random,
            BoundingBox chunkBB, ChunkPos chunkPos, BlockPos referencePos) {
        LairContext ctx = new LairContext(level, chunkBB, this.center, this.seed);
        int cx = this.center.getX();
        int cz = this.center.getZ();
        Stack[] stacks = new Stack[STACKS];
        for (int i = 0; i < STACKS; i++) {
            stacks[i] = this.stack(ctx, i);
        }
        for (int x = chunkBB.minX(); x <= chunkBB.maxX(); x++) {
            for (int z = chunkBB.minZ(); z <= chunkBB.maxZ(); z++) {
                double dx = x + 0.5 - (cx + 0.5);
                double dz = z + 0.5 - (cz + 0.5);
                double r = Math.sqrt(dx * dx + dz * dz);
                if (r > REACH) {
                    continue;
                }
                int top = this.column(ctx, x, z, r);
                for (Stack st : stacks) {
                    this.stackColumn(ctx, st, x, z, top);
                }
                this.decorate(ctx, x, z, r, top);
            }
        }
        this.arches(ctx, stacks);
        // das Herz des Schlunds, ganz unten
        BlockPos core = KrakenMawLocator.core(this.center);
        if (chunkBB.isInside(core)) {
            level.setBlock(core, s(SeaBlocks.KRAKEN_CORE.get()), Block.UPDATE_CLIENTS);
            if (level.getBlockEntity(core) instanceof KrakenCoreBlockEntity heart) {
                heart.setup(this.center);
            }
        }
        // die Truhe im Wrack
        BlockPos chest = this.wreckChest(ctx);
        if (chunkBB.isInside(chest)) {
            this.createChest(level, chunkBB, random, chest, CHEST, Blocks.CHEST.defaultBlockState()
                    .setValue(net.minecraft.world.level.block.ChestBlock.FACING, Direction.Plane.HORIZONTAL.getRandomDirection(random))
                    .setValue(net.minecraft.world.level.block.ChestBlock.WATERLOGGED, true));
        }
    }

    /** Eine Säule: Grund (im Schlund der Trichter), darüber Wasser bis zum Meeresspiegel, darüber Luft. Gibt die Oberkante zurück. */
    private int column(LairContext ctx, int x, int z, double r) {
        int natural = ctx.originalHeight(x, z, true) - 1;
        int water = this.sea - 1;
        int bottom = bottom(this.sea);
        int top = this.floor(ctx, x, z, r, natural);
        int low = Math.min(natural, top) - 3;
        boolean maw = r < MAW + 3.0;
        if (maw) {
            low = bottom - 3;
        }
        for (int y = low; y <= water; y++) {
            BlockState state;
            if (maw && y > bottom && y <= top + 1 && r < this.funnel(y)) {
                // im Trichter: Wasser
                state = s(Blocks.WATER);
            } else if (maw && y == bottom && r < this.funnel(y)) {
                state = ctx.rand(x, z, 44) < 0.3 ? s(Blocks.GRAVEL) : s(ModBlocks.ABYSSAL_SAND.get());
            } else if (y <= top) {
                state = this.ground(ctx, x, y, z, r, top - y, maw);
            } else {
                state = s(Blocks.WATER);
            }
            ctx.set(x, y, z, state);
        }
        for (int y = water + 1; y <= this.sea + 24; y++) {
            ctx.set(x, y, z, s(Blocks.AIR));
        }
        return maw && r < this.funnel(top) ? bottom : top;
    }

    /** Der Grund: oben Abgrundsand, Kies und Gezeitenstein, darunter Gezeitenstein; um den Schlund Abgrundstein. */
    private BlockState ground(LairContext ctx, int x, int y, int z, double r, int depth, boolean maw) {
        if (maw) {
            // die Wand des Trichters: Abgrundstein, hier und da eine Meereslaterne wie ein Auge im Dunkeln
            if (ctx.rand(x, y * 13 + z, 45) < 0.035) {
                return s(Blocks.SEA_LANTERN);
            }
            return ctx.rand(x, y * 7 + z, 46) < 0.18 ? s(Blocks.BLACKSTONE) : s(ModBlocks.ABYSSAL_STONE.base().get());
        }
        if (depth == 0) {
            double n = ctx.noise(x, z, 0.12, 47);
            if (r < 16.0 && n < 0.55) {
                return s(ModBlocks.ABYSSAL_SAND.get());
            }
            return n < 0.3 ? s(Blocks.GRAVEL) : n < 0.7 ? s(ModBlocks.ABYSSAL_SAND.get()) : s(SeaBlocks.TIDE_STONE.base().get());
        }
        if (depth < 3 && r < 20.0) {
            return s(ModBlocks.ABYSSAL_SAND.get());
        }
        return ctx.rand(x, y * 5 + z, 48) < 0.12 ? s(ModBlocks.ABYSSAL_STONE.base().get()) : s(SeaBlocks.TIDE_STONE.base().get());
    }

    /** Ein Brandungsfelsen in dieser Säule: vom Grund bis zur Spitze, nach oben schmaler, geneigt, mit Bändern. */
    private void stackColumn(LairContext ctx, Stack st, int x, int z, int floor) {
        double dx0 = x + 0.5 - st.x();
        double dz0 = z + 0.5 - st.z();
        double reach = st.radius() + Math.abs(st.leanX()) + Math.abs(st.leanZ()) + 1.0;
        if (dx0 * dx0 + dz0 * dz0 > reach * reach) {
            return;
        }
        int base = floor - 2;
        int span = Math.max(1, st.top() - base);
        for (int y = base; y <= st.top(); y++) {
            double t = (y - base) / (double) span;
            double cx = st.x() + st.leanX() * t;
            double cz = st.z() + st.leanZ() * t;
            double dx = x + 0.5 - cx;
            double dz = z + 0.5 - cz;
            double a = Math.atan2(dz, dx);
            double rough = (ctx.angleNoise(a + y * 0.11, 2.0, 49 + (int) (st.x() * 7)) - 0.5) * 1.1;
            double radius = st.radius() * (1.0 - 0.38 * t) + rough;
            if (dx * dx + dz * dz > radius * radius) {
                continue;
            }
            BlockState state;
            if (y % 7 == 3 && ctx.rand(x, z, 50) < 0.7) {
                state = s(SeaBlocks.TIDE_STONE_BRICKS.base().get());
            } else if (ctx.rand(x, y * 3 + z, 51) < 0.08) {
                state = s(Blocks.CALCITE);
            } else {
                state = s(SeaBlocks.TIDE_STONE.base().get());
            }
            ctx.set(x, y, z, state);
        }
    }

    /** Felsbögen zwischen drei Paaren benachbarter Felsen, die beide über das Wasser ragen. */
    private void arches(LairContext ctx, Stack[] stacks) {
        for (int k = 0; k < STACKS; k++) {
            Stack a = stacks[k];
            Stack b = stacks[(k + 1) % STACKS];
            if (ctx.rand(k, 9, 52) > 0.3 || a.top() < this.sea + 6 || b.top() < this.sea + 6) {
                continue;
            }
            int h0 = this.sea + 3;
            int h1 = Math.min(a.top(), b.top()) - 1;
            double ax = a.x() + a.leanX() * 0.5;
            double az = a.z() + a.leanZ() * 0.5;
            double bx = b.x() + b.leanX() * 0.5;
            double bz = b.z() + b.leanZ() * 0.5;
            int steps = (int) (Math.sqrt((bx - ax) * (bx - ax) + (bz - az) * (bz - az)) * 2.0);
            for (int i = 0; i <= steps; i++) {
                double t = i / (double) steps;
                double px = Mth.lerp(t, ax, bx);
                double pz = Mth.lerp(t, az, bz);
                int py = (int) Math.round(h0 + (h1 - h0) * 0.6 + Math.sin(t * Math.PI) * 3.0);
                for (int ox = -1; ox <= 1; ox++) {
                    for (int oz = -1; oz <= 1; oz++) {
                        if (ox * ox + oz * oz > 1) {
                            continue;
                        }
                        int x = Mth.floor(px) + ox;
                        int z = Mth.floor(pz) + oz;
                        ctx.set(x, py, z, s(SeaBlocks.TIDE_STONE.base().get()));
                        ctx.set(x, py + 1, z, (ox == 0 && oz == 0) ? s(SeaBlocks.TIDE_STONE.base().get()) : s(Blocks.AIR));
                    }
                }
            }
        }
    }

    // =====================================================================================
    // Was auf dem Grund liegt
    // =====================================================================================

    /** Lage und Richtung des Gerippes und des Wracks (fest aus dem Startwert). */
    private double[] skeleton(LairContext ctx) {
        double a = ctx.rand(1, 1, 53) * Math.PI * 2.0;
        double heading = a + Math.PI / 2.0 + (ctx.rand(2, 1, 53) - 0.5) * 0.8;
        return new double[] {this.center.getX() + 0.5 + Math.cos(a) * 17.0, this.center.getZ() + 0.5 + Math.sin(a) * 17.0, heading};
    }

    private double[] wreck(LairContext ctx) {
        double[] sk = this.skeleton(ctx);
        double a = Math.atan2(sk[1] - this.center.getZ() - 0.5, sk[0] - this.center.getX() - 0.5) + Math.PI + (ctx.rand(3, 1, 53) - 0.5) * 1.2;
        double heading = a + Math.PI / 2.0 + (ctx.rand(4, 1, 53) - 0.5) * 0.6;
        return new double[] {this.center.getX() + 0.5 + Math.cos(a) * 20.0, this.center.getZ() + 0.5 + Math.sin(a) * 20.0, heading};
    }

    private BlockPos wreckChest(LairContext ctx) {
        double[] w = this.wreck(ctx);
        int x = Mth.floor(w[0]);
        int z = Mth.floor(w[1]);
        double r = Math.sqrt((x + 0.5 - this.center.getX() - 0.5) * (x + 0.5 - this.center.getX() - 0.5)
                + (z + 0.5 - this.center.getZ() - 0.5) * (z + 0.5 - this.center.getZ() - 0.5));
        return new BlockPos(x, this.floor(ctx, x, z, r, this.sea - 40) + 1, z);
    }

    private void decorate(LairContext ctx, int x, int z, double r, int top) {
        if (r < MAW + 0.5) {
            // am Boden des Schlunds: Knochen und ein Ring aus dunklem Prismarin um das Herz
            int bottom = bottom(this.sea);
            if (top == bottom) {
                double d = Math.sqrt((x - this.center.getX()) * (x - this.center.getX()) + (z - this.center.getZ()) * (z - this.center.getZ()));
                if (d < 1.0) {
                    // über dem Herz bleibt Wasser
                    return;
                }
                if (d < 2.5) {
                    ctx.set(x, bottom, z, s(Blocks.DARK_PRISMARINE));
                } else if (ctx.rand(x, z, 54) < 0.12) {
                    ctx.set(x, bottom + 1, z, s(Blocks.BONE_BLOCK));
                } else if (ctx.rand(x, z, 55) < 0.25) {
                    ctx.setIfOpen(x, bottom + 1, z, s(SeaBlocks.GLOW_ALGAE.get()));
                }
            }
            return;
        }
        if (this.skeletonColumn(ctx, x, z, top) || this.wreckColumn(ctx, x, z, top)) {
            return;
        }
        double roll = ctx.rand(x, z, 56);
        BlockState below = ctx.get(x, top, z);
        if (!below.isSolidRender() || r > REACH - 2) {
            return;
        }
        if (r < MAW + 4.0) {
            // am Rand des Schlunds: Röhrenwürmer und ein paar Schlote
            if (roll < 0.035) {
                ctx.set(x, top + 1, z, s(SeaBlocks.HYDROTHERMAL_VENT.get()));
            } else if (roll < 0.3) {
                ctx.setIfOpen(x, top + 1, z, s(SeaBlocks.TUBE_WORMS.get()));
            }
            return;
        }
        if (roll < 0.02 && r > 12.0 && r < 31.0) {
            // hoher Riesentang
            int height = 8 + (int) (ctx.rand(x, z, 57) * 14.0);
            int y0 = top + 1;
            int y1 = Math.min(this.sea - 3, y0 + height);
            for (int y = y0; y <= y1; y++) {
                ctx.set(x, y, z, y == y1 ? s(SeaBlocks.GIANT_KELP.get()) : s(SeaBlocks.GIANT_KELP_PLANT.get()));
            }
        } else if (roll < 0.08) {
            ctx.setIfOpen(x, top + 1, z, s(SeaBlocks.GLOW_ALGAE.get()));
        } else if (roll < 0.1) {
            Block[] anemones = {SeaBlocks.PURPLE_SEA_ANEMONE.get(), SeaBlocks.RED_SEA_ANEMONE.get(), SeaBlocks.GREEN_SEA_ANEMONE.get(),
                    SeaBlocks.ORANGE_SEA_ANEMONE.get()};
            ctx.setIfOpen(x, top + 1, z, s(anemones[(int) (ctx.rand(x, z, 58) * anemones.length)]));
        } else if (roll < 0.115) {
            Block[] shells = {SeaBlocks.COCKLE_SHELLS.get(), SeaBlocks.SCALLOP_SHELLS.get(), SeaBlocks.WHELK_SHELLS.get()};
            BlockState shell = s(shells[(int) (ctx.rand(x, z, 59) * shells.length)]);
            if (shell.hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED)) {
                shell = shell.setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED, true);
            }
            ctx.setIfOpen(x, top + 1, z, shell);
        } else if (roll < 0.16 && r < 30.0) {
            ctx.setIfOpen(x, top + 1, z, s(Blocks.SEAGRASS));
        }
    }

    /** Das Gerippe eines Seedrachens: Wirbelsäule, Rippenbögen, ein Schädel. */
    private boolean skeletonColumn(LairContext ctx, int x, int z, int top) {
        double[] sk = this.skeleton(ctx);
        double hx = Math.cos(sk[2]);
        double hz = Math.sin(sk[2]);
        double px = x + 0.5 - sk[0];
        double pz = z + 0.5 - sk[1];
        double u = px * hx + pz * hz;
        double v = -px * hz + pz * hx;
        double length = 20.0;
        if (u < -2.5 || u > length || Math.abs(v) > 4.6) {
            return false;
        }
        boolean placed = false;
        Direction.Axis along = Math.abs(hx) > Math.abs(hz) ? Direction.Axis.X : Direction.Axis.Z;
        BlockState spine = s(Blocks.BONE_BLOCK).setValue(RotatedPillarBlock.AXIS, along);
        if (u < 0.5) {
            // der Schädel: ein Klotz aus Knochen mit leeren Augenhöhlen
            if (Math.abs(v) < 2.0) {
                for (int y = top + 1; y <= top + 3; y++) {
                    boolean eye = y == top + 2 && Math.abs(v) > 0.8 && u > -1.5;
                    ctx.set(x, y, z, eye ? s(Blocks.WATER) : s(Blocks.BONE_BLOCK));
                }
                return true;
            }
            return false;
        }
        if (Math.abs(v) < 0.6) {
            ctx.set(x, top + 1, z, spine);
            placed = true;
        }
        // Rippen alle zwei Blöcke, zum Schwanz hin kleiner
        int k = (int) Math.floor(u);
        if (k % 2 == 0 && u < length - 3.0) {
            double radius = 4.2 * (1.0 - u / length * 0.7);
            if (Math.abs(v) <= radius) {
                double h = Math.sqrt(Math.max(0.0, radius * radius - v * v));
                int y1 = top + 1 + (int) Math.round(h);
                int y0 = Math.abs(v) > radius * 0.7 ? top + 1 : y1;
                for (int y = y0; y <= y1; y++) {
                    ctx.set(x, y, z, s(Blocks.BONE_BLOCK));
                }
                placed = true;
            }
        }
        return placed;
    }

    /** Das Wrack: ein halber Rumpf aus dunklen Eichenbrettern, schräg im Sand, daneben der gebrochene Mast. */
    private boolean wreckColumn(LairContext ctx, int x, int z, int top) {
        double[] w = this.wreck(ctx);
        double hx = Math.cos(w[2]);
        double hz = Math.sin(w[2]);
        double px = x + 0.5 - w[0];
        double pz = z + 0.5 - w[1];
        double u = px * hx + pz * hz;
        double v = -px * hz + pz * hx;
        // der Mast liegt quer davor
        if (u > 6.5 && u < 15.5 && Math.abs(v - (u - 6.5) * 0.35) < 0.6) {
            Direction.Axis along = Math.abs(hx) > Math.abs(hz) ? Direction.Axis.X : Direction.Axis.Z;
            ctx.set(x, top + 1, z, s(Blocks.STRIPPED_DARK_OAK_LOG).setValue(RotatedPillarBlock.AXIS, along));
            return true;
        }
        if (Math.abs(u) > 6.5 || Math.abs(v) > 3.0) {
            return false;
        }
        // Querschnitt: ein U – unten der Kiel, die Seiten steigen; das Heck ist abgebrochen (niedriger)
        double side = Math.abs(v) / 3.0;
        int keel = top + 1;
        int wall = keel + (int) Math.round(side * side * 3.2);
        int rim = keel + (u > 3.0 ? 2 : 4) - (ctx.rand(x, z, 60) < 0.25 ? 1 : 0);
        if (Math.abs(v) < 0.6 && Math.abs(u) < 6.5) {
            ctx.set(x, keel, z, s(Blocks.STRIPPED_DARK_OAK_LOG).setValue(RotatedPillarBlock.AXIS,
                    Math.abs(hx) > Math.abs(hz) ? Direction.Axis.X : Direction.Axis.Z));
        } else {
            ctx.set(x, wall, z, s(Blocks.DARK_OAK_PLANKS));
            if (Math.abs(v) > 2.2) {
                for (int y = wall; y <= rim; y++) {
                    if (ctx.rand(x, y * 11 + z, 61) < 0.85) {
                        ctx.set(x, y, z, s(Blocks.DARK_OAK_PLANKS));
                    }
                }
            }
        }
        return true;
    }
}
