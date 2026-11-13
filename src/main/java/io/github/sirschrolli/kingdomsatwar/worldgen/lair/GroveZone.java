package io.github.sirschrolli.kingdomsatwar.worldgen.lair;

import io.github.sirschrolli.kingdomsatwar.registry.ModBlocks;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.material.Fluids;

/**
 * Der Uralte Hain rund um Verdantis' Nest (gut 110 Blöcke weit): Der Wald wird älter und üppiger. Der Boden wird zu
 * Moos, Podsol, Wurzelerde und Gras, darauf wachsen Farne, Moosteppiche, Hainglocken und andere Blumen, Leuchtkappen
 * und Dornengestrüpp. Die vorhandenen Bäume bleiben stehen; dazwischen wachsen uralte Hainbäume mit breiten
 * Wurzeln, und es liegen bemooste Felsen und umgestürzte Stämme im Unterholz.
 */
final class GroveZone extends NestZone {
    GroveZone(LairDesign design, int innerRadius) {
        super(design, innerRadius);
    }

    @Override
    protected void column(LairContext ctx, int x, int z, double local) {
        int y = ctx.groundY(x, z);
        BlockState ground = ctx.get(x, y, z);
        if (!ground.getFluidState().isEmpty()) {
            if (ground.getFluidState().is(Fluids.WATER) && ground.is(Blocks.WATER) && ctx.get(x, y + 1, z).isAir()
                    && ctx.rand(x, z, 301) < 0.07 * local) {
                ctx.set(x, y + 1, z, Blocks.LILY_PAD.defaultBlockState());
            }
            return;
        }
        if (!isSoftGround(ground) && !ground.is(Blocks.MOSS_BLOCK) && !ground.is(Blocks.PODZOL)) {
            return;
        }
        double n1 = ctx.noise(x, z, 0.05, 302);
        double n2 = ctx.noise(x, z, 0.13, 303);
        double change = ctx.rand(x, z, 304);
        if (change > 0.25 + local) {
            return;   // am Rand bleibt fleckig etwas vom alten Boden
        }
        BlockState surface;
        if (n1 > 0.6 && local > 0.35) {
            surface = Blocks.MOSS_BLOCK.defaultBlockState();
        } else if (n2 > 0.74) {
            surface = Blocks.PODZOL.defaultBlockState();
        } else if (n2 < 0.16 && local > 0.5) {
            surface = Blocks.ROOTED_DIRT.defaultBlockState();
        } else if (n1 < 0.18) {
            surface = Blocks.COARSE_DIRT.defaultBlockState();
        } else if (n1 > 0.5 && n2 > 0.55 && local > 0.7 && ctx.rand(x, z, 305) < 0.12) {
            surface = ModBlocks.GLOWING_MOSS.get().defaultBlockState();
        } else {
            surface = Blocks.GRASS_BLOCK.defaultBlockState();
        }
        if (ground.is(Blocks.SAND) || ground.is(Blocks.GRAVEL)) {
            surface = local > 0.6 ? Blocks.MOSS_BLOCK.defaultBlockState() : Blocks.COARSE_DIRT.defaultBlockState();
        }
        ctx.set(x, y, z, surface);
        BlockState above = ctx.get(x, y + 1, z);
        if (!(above.isAir() || (above.canBeReplaced() && above.getFluidState().isEmpty() && !above.is(Blocks.SNOW)))) {
            return;
        }
        if (above.is(BlockTags.SMALL_FLOWERS) && ctx.rand(x, z, 306) < 0.5) {
            return;   // vorhandene Blumen dürfen bleiben
        }
        this.plant(ctx, x, y + 1, z, surface, local, n1, n2);
    }

    /** Pflanze auf dem neuen Boden (oder Luft). */
    private void plant(LairContext ctx, int x, int y, int z, BlockState surface, double local, double n1, double n2) {
        double r = ctx.rand(x, z, 307);
        boolean moss = surface.is(Blocks.MOSS_BLOCK) || surface.is(ModBlocks.GLOWING_MOSS.get());
        BlockState plant = Blocks.AIR.defaultBlockState();
        if (moss) {
            if (r < 0.35) {
                plant = Blocks.MOSS_CARPET.defaultBlockState();
            } else if (r < 0.39 && local > 0.55) {
                plant = ModBlocks.GLOWCAP.get().defaultBlockState();
            } else if (r < 0.5) {
                plant = Blocks.FERN.defaultBlockState();
            }
        } else if (r < 0.2) {
            plant = Blocks.SHORT_GRASS.defaultBlockState();
        } else if (r < 0.3) {
            plant = Blocks.FERN.defaultBlockState();
        } else if (r < 0.36) {
            this.tallPlant(ctx, x, y, z, Blocks.LARGE_FERN.defaultBlockState());
            return;
        } else if (r < 0.39 && n2 > 0.45) {
            plant = ModBlocks.GROVE_BELL.get().defaultBlockState();
        } else if (r < 0.41) {
            plant = (n1 > 0.5 ? Blocks.LILY_OF_THE_VALLEY : Blocks.ALLIUM).defaultBlockState();
        } else if (r < 0.43 && n1 > 0.35 && n1 < 0.45 && local > 0.5) {
            plant = ModBlocks.BRAMBLE.get().defaultBlockState();
        } else if (r < 0.435 && !surface.is(Blocks.ROOTED_DIRT)) {
            plant = Blocks.AZALEA.defaultBlockState();
        }
        if (!plant.isAir()) {
            ctx.set(x, y, z, plant);
        }
    }

    private void tallPlant(LairContext ctx, int x, int y, int z, BlockState plant) {
        if (ctx.get(x, y + 1, z).isAir()) {
            ctx.set(x, y, z, plant.setValue(DoublePlantBlock.HALF, DoubleBlockHalf.LOWER));
            ctx.set(x, y + 1, z, plant.setValue(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER));
        }
    }

    // =====================================================================================
    // Einzelheiten: Hainbäume, bemooste Felsen, umgestürzte Stämme, Pilz- und Blumenflecken
    // =====================================================================================

    @Override
    protected double decorationChance() {
        return 0.018;
    }

    @Override
    protected double decorationMinStrength() {
        return 0.45;
    }

    @Override
    protected int decorationMargin() {
        return 10;
    }

    @Override
    protected void decoration(LairContext ctx, int x, int z, double r, double local) {
        int y = ctx.groundY(x, z);
        BlockState ground = ctx.get(x, y, z);
        if (!ground.getFluidState().isEmpty() || !LairContext.isGround(ground)) {
            return;
        }
        if (r < 0.08) {
            if (local > 0.6 && ctx.rand(x, z, 311) < 0.6) {
                groveTree(ctx, x, y + 1, z, 13 + (int) (ctx.rand(x, z, 312) * 8), ctx.rand(x, z, 313) < 0.4 ? 2 : 1,
                        5.0 + ctx.rand(x, z, 314) * 2, 3);
            }
        } else if (r < 0.22) {
            this.mossyBoulder(ctx, x, y, z);
        } else if (r < 0.34) {
            this.fallenLog(ctx, x, y, z);
        } else if (r < 0.52) {
            this.patch(ctx, x, y, z, ModBlocks.GLOWCAP.get().defaultBlockState(), 2.5, 0.45, true);
        } else if (r < 0.62) {
            this.patch(ctx, x, y, z, ModBlocks.BRAMBLE.get().defaultBlockState(), 2.2, 0.6, false);
        } else if (r < 0.8) {
            this.patch(ctx, x, y, z, ModBlocks.GROVE_BELL.get().defaultBlockState(), 3.0, 0.4, false);
        } else {
            this.stump(ctx, x, y, z);
        }
    }

    /**
     * Ein Hainbaum aus Urholz (auch vom Nest benutzt): Stamm mit Wurzelfüßen und knorrigen Wurzeln am Boden, oben
     * eine Krone aus mehreren Laubballen, darunter hängen Höhlenranken mit Leuchtbeeren.
     */
    static void groveTree(LairContext ctx, int x, int y, int z, int height, int thick, double crown, int below) {
        BlockState log = ModBlocks.ANCIENT_LOG.get().defaultBlockState();
        // Stamm (1x1, 2x2 oder 3x3), reicht tief in den Boden (so steht er auch auf unebenem Gelände)
        for (int k = -below; k < height; k++) {
            for (int dx = 0; dx < thick; dx++) {
                for (int dz = 0; dz < thick; dz++) {
                    trunk(ctx, x + dx, y + k, z + dz, log);
                }
            }
        }
        // Wurzelfüße und knorrige Wurzeln
        int[][] dirs = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        for (int i = 0; i < 4; i++) {
            int[] d = dirs[i];
            int len = 1 + (int) (ctx.rand(x + i, z, 315) * (thick + 2));
            int bx = d[0] > 0 ? x + thick - 1 : x;
            int bz = d[1] > 0 ? z + thick - 1 : z;
            for (int s = 1; s <= len; s++) {
                int px = bx + d[0] * s;
                int pz = bz + d[1] * s;
                int h = Math.max(0, 2 - s);
                for (int k = -1; k <= h; k++) {
                    trunk(ctx, px, y + k, pz, s == len ? ModBlocks.GNARLED_ROOTS.get().defaultBlockState()
                            : log.setValue(RotatedPillarBlock.AXIS, d[0] != 0 ? Direction.Axis.X : Direction.Axis.Z));
                }
            }
        }
        // Krone: ein großer Laubballen und zwei bis drei kleinere, leicht versetzt
        double cxw = x + (thick - 1) / 2.0;
        double czw = z + (thick - 1) / 2.0;
        int top = y + height;
        blob(ctx, cxw, top, czw, crown, crown * 0.6);
        int extra = 2 + (int) (ctx.rand(x, z, 316) * 2);
        for (int i = 0; i < extra; i++) {
            double a = ctx.rand(x, z + i, 317) * Math.PI * 2;
            double off = crown * 0.7;
            double bx = cxw + Math.cos(a) * off;
            double bz = czw + Math.sin(a) * off;
            double by = top - 2 - ctx.rand(x + i, z, 318) * 3;
            // Ast dorthin
            for (int s = 1; s <= (int) off; s++) {
                double t = s / off;
                trunk(ctx, (int) Math.round(cxw + (bx - cxw) * t), (int) Math.round(top - 3 + (by - top + 3) * t),
                        (int) Math.round(czw + (bz - czw) * t), log.setValue(RotatedPillarBlock.AXIS,
                                Math.abs(Math.cos(a)) > Math.abs(Math.sin(a)) ? Direction.Axis.X : Direction.Axis.Z));
            }
            blob(ctx, bx, by, bz, crown * 0.62, crown * 0.42);
        }
        // Höhlenranken mit Leuchtbeeren unter der Krone
        int reach = (int) Math.ceil(crown);
        for (int dx = -reach; dx <= reach; dx++) {
            for (int dz = -reach; dz <= reach; dz++) {
                int vx = (int) Math.round(cxw) + dx;
                int vz = (int) Math.round(czw) + dz;
                if (ctx.rand(vx, vz, 319) > 0.07 || dx * dx + dz * dz > crown * crown) {
                    continue;
                }
                hangingVines(ctx, vx, top + 2, vz, 2 + (int) (ctx.rand(vx, vz, 320) * 6));
            }
        }
    }

    /** Laubballen (Ellipsoid) aus dauerhaftem Urlaub; ersetzt nur Luft und Pflanzen. */
    static void blob(LairContext ctx, double cx, double cy, double cz, double radius, double height) {
        BlockState leaves = ModBlocks.ANCIENT_LEAVES.get().defaultBlockState().setValue(LeavesBlock.PERSISTENT, true);
        int reach = (int) Math.ceil(radius) + 1;
        int reachY = (int) Math.ceil(height) + 1;
        int bx = (int) Math.floor(cx);
        int by = (int) Math.floor(cy);
        int bz = (int) Math.floor(cz);
        for (int dx = -reach; dx <= reach; dx++) {
            for (int dz = -reach; dz <= reach; dz++) {
                for (int dy = -reachY; dy <= reachY; dy++) {
                    double ex = (bx + dx + 0.5 - cx) / radius;
                    double ez = (bz + dz + 0.5 - cz) / radius;
                    double ey = (by + dy + 0.5 - cy) / height;
                    double e = ex * ex + ey * ey + ez * ez;
                    double edge = 1.0 - 0.25 * ctx.rand(bx + dx, bz + dz + dy * 31, 321);
                    if (e <= edge) {
                        ctx.setIfOpen(bx + dx, by + dy, bz + dz, leaves);
                    }
                }
            }
        }
    }

    /** Hängende Höhlenranken (teils mit Leuchtbeeren) – nur unter festem Halt. */
    static void hangingVines(LairContext ctx, int x, int y, int z, int length) {
        // erste Luft unter dem Laub suchen
        int start = y;
        for (int k = 0; k < 8; k++) {
            if (ctx.get(x, start, z).isAir() && !ctx.get(x, start + 1, z).isAir()) {
                break;
            }
            start--;
        }
        if (!ctx.get(x, start, z).isAir() || ctx.get(x, start + 1, z).isAir()) {
            return;
        }
        for (int k = 0; k < length; k++) {
            int yy = start - k;
            if (!ctx.get(x, yy, z).isAir()) {
                return;
            }
            boolean last = k == length - 1 || !ctx.get(x, yy - 1, z).isAir();
            boolean berries = ctx.rand(x + k, z, 322) < 0.35;
            BlockState vine = last
                    ? Blocks.CAVE_VINES.defaultBlockState().setValue(net.minecraft.world.level.block.CaveVines.BERRIES, berries)
                            .setValue(net.minecraft.world.level.block.GrowingPlantHeadBlock.AGE, 25)
                    : Blocks.CAVE_VINES_PLANT.defaultBlockState().setValue(net.minecraft.world.level.block.CaveVines.BERRIES, berries);
            ctx.set(x, yy, z, vine);
            if (last) {
                return;
            }
        }
    }

    /** Stamm: ersetzt Luft, Pflanzen und Laub (keine anderen Stämme oder festen Blöcke). */
    static void trunk(LairContext ctx, int x, int y, int z, BlockState log) {
        if (!ctx.inside(x, y, z)) {
            return;
        }
        BlockState old = ctx.get(x, y, z);
        if (old.isAir() || old.canBeReplaced() || old.is(BlockTags.LEAVES) || isSoftGround(old) || old.is(Blocks.MOSS_BLOCK)
                || old.is(Blocks.PODZOL) || old.is(Blocks.ROOTED_DIRT)) {
            ctx.set(x, y, z, log);
        }
    }

    private void mossyBoulder(LairContext ctx, int x, int y, int z) {
        double radius = 1.3 + ctx.rand(x, z, 330) * 1.6;
        int reach = (int) Math.ceil(radius);
        for (int dx = -reach; dx <= reach; dx++) {
            for (int dy = -1; dy <= reach; dy++) {
                for (int dz = -reach; dz <= reach; dz++) {
                    if (dx * dx + dy * dy * 1.5 + dz * dz > radius * radius) {
                        continue;
                    }
                    boolean top = dy == reach || (dx * dx + (dy + 1) * (dy + 1) * 1.5 + dz * dz > radius * radius);
                    BlockState state = top && ctx.rand(x + dx, z + dz, 331) < 0.7 ? Blocks.MOSS_BLOCK.defaultBlockState()
                            : ctx.rand(x + dx, z + dz + dy, 332) < 0.5 ? Blocks.MOSSY_COBBLESTONE.defaultBlockState()
                            : Blocks.COBBLESTONE.defaultBlockState();
                    ctx.setIfOpen(x + dx, y + dy, z + dz, state);
                }
            }
        }
        ctx.setIfOpen(x, y + reach + 1, z, Blocks.MOSS_CARPET.defaultBlockState());
    }

    /** Umgestürzter, bemooster Stamm mit Leuchtkappen. */
    private void fallenLog(LairContext ctx, int x, int y, int z) {
        boolean alongX = ctx.rand(x, z, 340) < 0.5;
        int len = 5 + (int) (ctx.rand(x, z, 341) * 6);
        BlockState log = ModBlocks.ANCIENT_LOG.get().defaultBlockState().setValue(RotatedPillarBlock.AXIS,
                alongX ? Direction.Axis.X : Direction.Axis.Z);
        for (int i = 0; i < len; i++) {
            int px = x + (alongX ? i : 0);
            int pz = z + (alongX ? 0 : i);
            int gy = ctx.groundY(px, pz);
            if (Math.abs(gy - y) > 2) {
                return;
            }
            ctx.setIfOpen(px, y + 1, pz, log);
            double r = ctx.rand(px, pz, 342);
            if (r < 0.45) {
                ctx.setIfOpen(px, y + 2, pz, Blocks.MOSS_CARPET.defaultBlockState());
            } else if (r < 0.6) {
                ctx.setIfOpen(px, y + 2, pz, ModBlocks.GLOWCAP.get().defaultBlockState());
            }
        }
    }

    /** Fleck aus einer Pflanze (auf Gras, Moos und Erde). */
    private void patch(LairContext ctx, int x, int y, int z, BlockState plant, double radius, double density, boolean mossy) {
        int reach = (int) Math.ceil(radius);
        for (int dx = -reach; dx <= reach; dx++) {
            for (int dz = -reach; dz <= reach; dz++) {
                if (dx * dx + dz * dz > radius * radius || ctx.rand(x + dx, z + dz, 350) > density) {
                    continue;
                }
                int gy = ctx.groundY(x + dx, z + dz);
                BlockState g = ctx.get(x + dx, gy, z + dz);
                if (!LairContext.isGround(g) || !g.getFluidState().isEmpty()) {
                    continue;
                }
                if (mossy && (isSoftGround(g) || g.is(Blocks.PODZOL))) {
                    ctx.set(x + dx, gy, z + dz, Blocks.MOSS_BLOCK.defaultBlockState());
                }
                BlockState above = ctx.get(x + dx, gy + 1, z + dz);
                if (above.isAir() || (above.canBeReplaced() && above.getFluidState().isEmpty())) {
                    ctx.set(x + dx, gy + 1, z + dz, plant);
                }
            }
        }
    }

    /** Alter, bemooster Baumstumpf. */
    private void stump(LairContext ctx, int x, int y, int z) {
        int h = 1 + (int) (ctx.rand(x, z, 360) * 2);
        for (int k = 1; k <= h; k++) {
            ctx.setIfOpen(x, y + k, z, ModBlocks.ANCIENT_LOG.get().defaultBlockState());
        }
        ctx.setIfOpen(x, y + h + 1, z, ctx.rand(x, z, 361) < 0.5 ? Blocks.MOSS_CARPET.defaultBlockState()
                : ModBlocks.GLOWCAP.get().defaultBlockState());
    }
}
