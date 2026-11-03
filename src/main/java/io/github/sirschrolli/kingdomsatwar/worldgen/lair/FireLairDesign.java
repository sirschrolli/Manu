package io.github.sirschrolli.kingdomsatwar.worldgen.lair;

import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import io.github.sirschrolli.kingdomsatwar.loot.ModLootTables;
import io.github.sirschrolli.kingdomsatwar.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.entity.BrushableBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.material.Fluids;

/**
 * Pyrrhons Vulkankrater: ein Ring aus Basalt und Tuff (bis 22 Blöcke hoch) um einen Kraterboden aus Basalt und
 * Schwarzstein mit glühenden Rissen, in der Mitte der Lavasee, aus dem der Drache erwacht. Dazu Basaltnadeln mit
 * ewigen Flammen, die Gerippe früherer Opfer, rauchende Schlote, Lavafälle am inneren Kraterrand,
 * Draconium-Adern in der Wand und eine Bresche als Zugang. Ringsum liegen die Aschelande ({@link AshlandsZone}).
 */
public final class FireLairDesign implements LairDesign {
    private static final int R = 48;
    private static final int TOP = 46;
    private final AshlandsZone zone = new AshlandsZone(this, R);

    @Override
    public int radius() {
        return R;
    }

    /** Die Aschelande reichen gut 120 Blöcke weit. */
    @Override
    public int zoneRadius() {
        return 124;
    }

    @Override
    public void buildZone(LairContext ctx) {
        this.zone.build(ctx);
    }

    @Override
    public int depthBelow() {
        return 14;
    }

    @Override
    public int heightAbove() {
        return TOP;
    }

    @Override
    public int floorHeight(Structure.GenerationContext context, int x, int z) {
        int avg = DragonLairStructure.averageHeight(context, x, z, 32, Heightmap.Types.WORLD_SURFACE_WG);
        return Mth.clamp(avg, context.chunkGenerator().getSeaLevel() + 3, 190);
    }

    private static double rimRadius(LairContext ctx, double angle) {
        return 33.0 + (ctx.angleNoise(angle, 2.2, 11) - 0.5) * 7.0;
    }

    private static double rimHeight(LairContext ctx, double angle) {
        return 17.0 + (ctx.angleNoise(angle, 3.1, 12) - 0.5) * 9.0;
    }

    private static double lakeRadius(LairContext ctx, double angle) {
        return 8.5 + (ctx.angleNoise(angle, 2.0, 13) - 0.5) * 3.0;
    }

    private double breachAngle(LairContext ctx) {
        return ctx.rand(0, 0, 99) * Mth.TWO_PI;
    }

    @Override
    public void build(LairContext ctx) {
        int cx = ctx.center.getX();
        int cy = ctx.center.getY();
        int cz = ctx.center.getZ();
        double breach = this.breachAngle(ctx);
        for (int x = Math.max(ctx.box.minX(), cx - R); x <= Math.min(ctx.box.maxX(), cx + R); x++) {
            for (int z = Math.max(ctx.box.minZ(), cz - R); z <= Math.min(ctx.box.maxZ(), cz + R); z++) {
                double dx = x - cx;
                double dz = z - cz;
                double d = Math.sqrt(dx * dx + dz * dz);
                if (d > R) {
                    continue;
                }
                this.column(ctx, x, z, d, Math.atan2(dz, dx), breach, cy);
            }
        }
        this.features(ctx, cx, cy, cz);
    }

    private void column(LairContext ctx, int x, int z, double d, double angle, double breach, int cy) {
        int top0 = ctx.originalHeight(x, z, false) - 1;        // oberster Block (auch Bäume)
        int h0 = ctx.groundY(x, z);                            // oberster Block des Geländes
        double rim = rimRadius(ctx, angle);
        double lake = lakeRadius(ctx, angle);
        double breachDiff = Math.abs(Mth.wrapDegrees((angle - breach) * Mth.RAD_TO_DEG)) * Mth.DEG_TO_RAD;
        double breachF = breachDiff < 0.16 ? 1.0 : breachDiff < 0.3 ? 1.0 - (breachDiff - 0.16) / 0.14 : 0.0;
        double n = ctx.noise(x, z, 0.08, 21);
        int top = cy + TOP;
        if (d < lake) {
            // Lavasee (4 tief) über Basalt
            ctx.fill(x, Math.min(h0, cy) - 8, cy - 6, z, Blocks.BASALT.defaultBlockState());
            ctx.set(x, cy - 5, z, Blocks.MAGMA_BLOCK.defaultBlockState());
            ctx.fill(x, cy - 4, cy - 1, z, Blocks.LAVA.defaultBlockState());
            ctx.fill(x, cy, top, z, Blocks.AIR.defaultBlockState());
            return;
        }
        double floorEdge = rim - 7.0;
        if (d < floorEdge) {
            int hs = cy - 1 + (int) Math.round((n - 0.5) * 1.6);
            ctx.fill(x, Math.min(h0, hs) - 6, hs - 1, z, below(ctx, x, z, hs));
            ctx.set(x, hs, z, this.floor(ctx, x, z, d, lake, angle));
            ctx.fill(x, hs + 1, top, z, Blocks.AIR.defaultBlockState());
            return;
        }
        double rimH = rimHeight(ctx, angle) * (1.0 - breachF) + 1.0 * breachF;
        int hs;
        if (d < rim) {
            double t = (d - floorEdge) / 7.0;
            hs = cy - 1 + (int) Math.round(rimH * LairContext.smooth(Mth.clamp(t, 0, 1)) + (n - 0.5) * 2.0);
        } else {
            double u = Mth.clamp((d - rim) / (R - rim), 0, 1);
            double outer = Mth.lerp(LairContext.smooth(u), cy - 1 + rimH, h0);
            if (breachF > 0) {
                outer = Mth.lerp(breachF, outer, Mth.lerp(u, cy - 1, h0));
            }
            hs = (int) Math.round(outer + (n - 0.5) * 2.0);
        }
        int bottom = Math.min(h0, hs) - 6;
        boolean outer = d >= rim;
        for (int y = bottom; y <= hs; y++) {
            ctx.set(x, y, z, this.rimBlock(ctx, x, y, z, hs, cy, outer));
        }
        // Innen alles frei räumen, außen über dem neuen Hang (samt Bäumen)
        int clearTo = d < rim ? top : Math.max(Math.max(h0, hs) + 2, top0 + 1);
        ctx.fill(x, hs + 1, clearTo, z, Blocks.AIR.defaultBlockState());
    }

    private static BlockState below(LairContext ctx, int x, int z, int y) {
        return ctx.rand(x, z, 5) < 0.3 ? Blocks.BLACKSTONE.defaultBlockState() : Blocks.BASALT.defaultBlockState();
    }

    private BlockState floor(LairContext ctx, int x, int z, double d, double lake, double angle) {
        double crack = Math.abs(Math.sin(angle * 5.0 + ctx.noise(x, z, 0.05, 31) * 4.0));
        if (crack < 0.05 && d > lake + 2 && d < lake + 18) {
            return Blocks.LAVA.defaultBlockState();
        }
        double r = ctx.rand(x, z, 7);
        if (d < lake + 2.5) {
            return r < 0.35 ? Blocks.OBSIDIAN.defaultBlockState() : Blocks.MAGMA_BLOCK.defaultBlockState();
        }
        if (crack < 0.13 && d < lake + 20) {
            return Blocks.MAGMA_BLOCK.defaultBlockState();
        }
        if (r < 0.5) {
            return Blocks.BASALT.defaultBlockState();
        } else if (r < 0.78) {
            return Blocks.BLACKSTONE.defaultBlockState();
        } else if (r < 0.92) {
            return Blocks.SMOOTH_BASALT.defaultBlockState();
        }
        return Blocks.MAGMA_BLOCK.defaultBlockState();
    }

    private BlockState rimBlock(LairContext ctx, int x, int y, int z, int hs, int cy, boolean outer) {
        double r = ctx.rand(x * 31 + y, z, 9);
        // glühende Stellen: an der inneren Kraterwand häufiger, am äußeren Hang nur vereinzelt
        if (y >= hs - 1 && ctx.rand(x, z, 17) < (outer ? 0.012 : 0.06)) {
            return Blocks.MAGMA_BLOCK.defaultBlockState();
        }
        if (y < hs - 1 && r < 0.004) {
            return ModBlocks.DRACONIUM_ORE.get().defaultBlockState();   // Draconium-Adern in der Kraterwand
        }
        int rel = y - cy;
        if (rel > 12) {
            return r < 0.6 ? Blocks.TUFF.defaultBlockState() : Blocks.BASALT.defaultBlockState();
        } else if (rel > 4) {
            return r < 0.55 ? Blocks.BASALT.defaultBlockState() : r < 0.85 ? Blocks.TUFF.defaultBlockState() : Blocks.BLACKSTONE.defaultBlockState();
        }
        return r < 0.6 ? Blocks.BLACKSTONE.defaultBlockState() : Blocks.BASALT.defaultBlockState();
    }

    // ------------------------------------------------------------------ Einzelheiten

    private void features(LairContext ctx, int cx, int cy, int cz) {
        // Basaltnadeln, drei davon mit ewigen Flammen
        for (int k = 0; k < 7; k++) {
            double a = k * Mth.TWO_PI / 7 + (ctx.rand(k, 1, 40) - 0.5) * 0.5;
            double rr = rimRadius(ctx, a) - 11 - ctx.rand(k, 2, 40) * 3;
            int sx = cx + (int) Math.round(Math.cos(a) * rr);
            int sz = cz + (int) Math.round(Math.sin(a) * rr);
            int height = 8 + (int) (ctx.rand(k, 3, 40) * 9);
            double base = 1.6 + ctx.rand(k, 4, 40) * 1.1;
            this.spire(ctx, sx, cy, sz, height, base, k % 2 == 0);
        }
        // Gerippe früherer Opfer
        for (int k = 0; k < 2; k++) {
            double a = ctx.rand(k, 5, 41) * Mth.TWO_PI;
            double rr = 15 + ctx.rand(k, 6, 41) * 6;
            int bx = cx + (int) Math.round(Math.cos(a) * rr);
            int bz = cz + (int) Math.round(Math.sin(a) * rr);
            this.ribcage(ctx, bx, cy, bz, ctx.rand(k, 7, 41) < 0.5 ? Direction.Axis.X : Direction.Axis.Z, 11 + (int) (ctx.rand(k, 8, 41) * 4));
            // Fundstücke im Kies daneben (mit dem Pinsel freilegen)
            for (int j = 0; j < 2; j++) {
                int gx = bx + (j == 0 ? 3 : -3);
                int gz = bz + (j == 0 ? -2 : 2);
                this.suspicious(ctx, gx, cy - 1, gz, 1000L * k + j);
            }
        }
        // rauchende Schlote
        for (int k = 0; k < 5; k++) {
            double a = ctx.rand(k, 9, 42) * Mth.TWO_PI;
            double rr = 12 + ctx.rand(k, 10, 42) * 9;
            int vx = cx + (int) Math.round(Math.cos(a) * rr);
            int vz = cz + (int) Math.round(Math.sin(a) * rr);
            ctx.set(vx, cy - 2, vz, Blocks.HAY_BLOCK.defaultBlockState());
            ctx.set(vx, cy - 1, vz, Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, true)
                    .setValue(CampfireBlock.SIGNAL_FIRE, true));
            for (Direction dir : Direction.Plane.HORIZONTAL) {
                ctx.set(vx + dir.getStepX(), cy - 1, vz + dir.getStepZ(), Blocks.BLACKSTONE.defaultBlockState());
            }
        }
        // Lavafälle am inneren Kraterrand
        for (int k = 0; k < 3; k++) {
            double a = ctx.rand(k, 11, 43) * Mth.TWO_PI;
            double rr = rimRadius(ctx, a) - 1.5;
            int lx = cx + (int) Math.round(Math.cos(a) * rr);
            int lz = cz + (int) Math.round(Math.sin(a) * rr);
            if (ctx.inside(lx, cy, lz)) {
                int y = ctx.originalHeight(lx, lz, false);
                if (y > cy + 6) {
                    BlockPos pos = new BlockPos(lx, y - 1, lz);
                    ctx.set(lx, y - 1, lz, Blocks.LAVA.defaultBlockState());
                    ctx.level.scheduleTick(pos, Fluids.LAVA, 1);
                }
            }
        }
        // Nestkern unter dem Lavasee
        BlockPos core = this.corePos(ctx.center);
        if (ctx.inside(core.getX(), core.getY(), core.getZ())) {
            ctx.set(core.getX(), core.getY(), core.getZ(), ModBlocks.DRAGON_LAIR_CORE.get().defaultBlockState());
            if (ctx.level.getBlockEntity(core) instanceof io.github.sirschrolli.kingdomsatwar.block.entity.DragonLairCoreBlockEntity be) {
                be.setup(DragonType.FIRE, ctx.center);
            }
        }
    }

    private void spire(LairContext ctx, int sx, int cy, int sz, int height, double base, boolean flame) {
        int reach = (int) Math.ceil(base) + 1;
        for (int y = 0; y < height; y++) {
            double r = base * (1.0 - (double) y / (height + 2)) + 0.35;
            for (int dx = -reach; dx <= reach; dx++) {
                for (int dz = -reach; dz <= reach; dz++) {
                    if (dx * dx + dz * dz <= r * r) {
                        boolean core = dx == 0 && dz == 0;
                        BlockState state = core || ctx.rand(sx + dx, sz + dz + y * 7, 44) < 0.65
                                ? Blocks.BASALT.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y)
                                : Blocks.BLACKSTONE.defaultBlockState();
                        ctx.set(sx + dx, cy + y, sz + dz, state);
                    }
                }
            }
        }
        if (flame) {
            ctx.set(sx, cy + height, sz, Blocks.NETHERRACK.defaultBlockState());
            ctx.set(sx, cy + height + 1, sz, Blocks.FIRE.defaultBlockState());
        }
        // Fuß in den Boden
        for (int dx = -reach; dx <= reach; dx++) {
            for (int dz = -reach; dz <= reach; dz++) {
                if (dx * dx + dz * dz <= (base + 0.6) * (base + 0.6)) {
                    ctx.fill(sx + dx, cy - 3, cy - 1, sz + dz, Blocks.BASALT.defaultBlockState());
                }
            }
        }
    }

    /** Gerippe: Wirbelsäule aus Knochenblöcken, Rippenbögen links und rechts, Schädel am Ende. */
    private void ribcage(LairContext ctx, int bx, int cy, int bz, Direction.Axis axis, int length) {
        boolean alongX = axis == Direction.Axis.X;
        BlockState spine = Blocks.BONE_BLOCK.defaultBlockState().setValue(RotatedPillarBlock.AXIS, axis);
        BlockState rib = Blocks.BONE_BLOCK.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y);
        for (int i = 0; i < length; i++) {
            int x = bx + (alongX ? i - length / 2 : 0);
            int z = bz + (alongX ? 0 : i - length / 2);
            int arch = (int) Math.round(Math.sin((double) i / length * Math.PI) * 2.0);
            ctx.set(x, cy + 3 + arch, z, spine);
            if (i % 2 == 1 && i > 1 && i < length - 2) {
                for (int side = -1; side <= 1; side += 2) {
                    for (int k = 0; k <= 4; k++) {
                        int off = (int) Math.round(Math.sin(k / 4.0 * Math.PI * 0.6) * 3.2) * side;
                        int ry = cy + 3 + arch - (int) Math.round(k * 0.9);
                        ctx.set(x + (alongX ? 0 : off), ry, z + (alongX ? off : 0), rib);
                    }
                }
            }
        }
        // Schädel
        int sx = bx + (alongX ? length / 2 + 1 : 0);
        int sz = bz + (alongX ? 0 : length / 2 + 1);
        for (int dx = 0; dx <= 1; dx++) {
            for (int dz = 0; dz <= 1; dz++) {
                for (int dy = 0; dy <= 1; dy++) {
                    ctx.set(sx + dx, cy + dy, sz + dz, Blocks.BONE_BLOCK.defaultBlockState());
                }
            }
        }
        ctx.set(sx + (alongX ? 2 : 0), cy, sz + (alongX ? 0 : 2), Blocks.BONE_BLOCK.defaultBlockState());
    }

    private void suspicious(LairContext ctx, int x, int y, int z, long salt) {
        if (!ctx.inside(x, y, z)) {
            return;
        }
        BlockPos pos = new BlockPos(x, y, z);
        ctx.set(x, y, z, Blocks.SUSPICIOUS_GRAVEL.defaultBlockState());
        if (ctx.level.getBlockEntity(pos) instanceof BrushableBlockEntity brushable) {
            brushable.setLootTable(ModLootTables.DRAGON_NEST_ARCHAEOLOGY.get(DragonType.FIRE), ctx.seed + salt);
        }
    }
}
