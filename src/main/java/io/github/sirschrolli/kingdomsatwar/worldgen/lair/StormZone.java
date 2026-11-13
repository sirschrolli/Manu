package io.github.sirschrolli.kingdomsatwar.worldgen.lair;

import io.github.sirschrolli.kingdomsatwar.registry.ModBlocks;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Die Sturmklippen rund um Voltarions Sturmgipfel (gut 105 Blöcke weit): Der Berg wird zu dunklem, bläulichem
 * Sturmstein, durchzogen von gleißenden Adern aus geladenem Sturmstein. Die Bäume sind vom Blitz verkohlt (nur Stümpfe
 * stehen noch), Laub und Blumen verschwinden, nur zähes Gras hält sich. Überall liegen die Spuren der Einschläge:
 * Fulguritröhren in versengtem Fels, Sturmkristalle, Felsnadeln mit gespaltener Spitze und die alten Blitzableiter der
 * Sturmwächter auf den Felsbrocken.
 */
final class StormZone extends NestZone {
    StormZone(LairDesign design, int innerRadius) {
        super(design, innerRadius);
    }

    @Override
    protected void column(LairContext ctx, int x, int z, double local) {
        int y = surfaceAfterPlants(ctx, x, z, (state, px, py, pz) -> {
            if (state.is(BlockTags.LEAVES)) {
                return Blocks.AIR.defaultBlockState();
            }
            if (state.is(BlockTags.LOGS)) {
                // vom Blitz verkohlte Stämme; nahe dem Gipfel sind auch sie zerfallen
                if (local > 0.7 && ctx.rand(px, pz, 601) < local) {
                    return Blocks.AIR.defaultBlockState();
                }
                BlockState charred = ModBlocks.CHARRED_LOG.get().defaultBlockState();
                return state.hasProperty(RotatedPillarBlock.AXIS)
                        ? charred.setValue(RotatedPillarBlock.AXIS, state.getValue(RotatedPillarBlock.AXIS)) : charred;
            }
            if (state.is(Blocks.SNOW)) {
                return local > 0.5 ? Blocks.AIR.defaultBlockState() : state;
            }
            return ctx.rand(px, pz, 602) < local * 1.3 ? Blocks.AIR.defaultBlockState() : state;
        });
        BlockState ground = ctx.get(x, y, z);
        if (!ground.getFluidState().isEmpty() || local < 0.2 || !isReplaceableGround(ground)) {
            return;
        }
        if (ctx.rand(x, z, 603) > local * 1.7) {
            return;   // am Rand fleckig
        }
        double n1 = ctx.noise(x, z, 0.05, 604);
        double n2 = ctx.noise(x, z, 0.14, 605);
        // Blitzadern: schmale Höhenlinien eines großräumigen Rauschens
        boolean vein = Math.abs(ctx.noise(x, z, 0.03, 606) - 0.5) < 0.011 && local > 0.45;
        BlockState surface;
        if (vein) {
            surface = ModBlocks.CHARGED_STORMSTONE.get().defaultBlockState();
        } else if (n1 > 0.7) {
            surface = Blocks.SMOOTH_BASALT.defaultBlockState();
        } else if (n2 > 0.74) {
            surface = Blocks.GRAVEL.defaultBlockState();
        } else if (n2 < 0.2) {
            surface = Blocks.COBBLED_DEEPSLATE.defaultBlockState();
        } else if (ground.is(Blocks.GRASS_BLOCK) && n1 < 0.38 && local < 0.75) {
            surface = Blocks.GRASS_BLOCK.defaultBlockState();   // zähes, graues Berggras
        } else {
            surface = ModBlocks.STORMSTONE.base().get().defaultBlockState();
        }
        ctx.set(x, y, z, surface);
        boolean grass = surface.is(Blocks.GRASS_BLOCK);
        int depth = reshapeDepth(ctx, x, y, z);
        this.fillBelow(ctx, x, y, z, depth, (k, yy) -> {
            if (grass && k <= 2) {
                return Blocks.DIRT.defaultBlockState();
            }
            double strata = ctx.noise(x * 0.25 + z * 0.25, yy, 0.4, 607);
            return strata < 0.6 ? ModBlocks.STORMSTONE.base().get().defaultBlockState()
                    : strata < 0.8 ? Blocks.DEEPSLATE.defaultBlockState() : Blocks.COBBLED_DEEPSLATE.defaultBlockState();
        });
        if (vein || !ctx.get(x, y + 1, z).isAir()) {
            return;
        }
        double r = ctx.rand(x, z, 608);
        if (grass && r < 0.18) {
            ctx.set(x, y + 1, z, Blocks.SHORT_GRASS.defaultBlockState());
        } else if (!grass && r < 0.012 && local > 0.4) {
            ctx.set(x, y + 1, z, ModBlocks.STORM_CRYSTAL.get().defaultBlockState());
        }
    }

    @Override
    protected double decorationChance() {
        return 0.012;
    }

    @Override
    protected int decorationMargin() {
        return 5;
    }

    @Override
    protected void decoration(LairContext ctx, int x, int z, double r, double local) {
        int y = ctx.groundY(x, z);
        BlockState ground = ctx.get(x, y, z);
        if (!ground.getFluidState().isEmpty() || !LairContext.isGround(ground)) {
            return;
        }
        if (r < 0.22) {
            spire(ctx, x, y + 1, z, 5 + (int) (ctx.rand(x, z, 610) * 8 * local));
        } else if (r < 0.48) {
            strike(ctx, x, y, z);
        } else if (r < 0.62) {
            crystals(ctx, x, y, z);
        } else if (r < 0.74) {
            this.rodStone(ctx, x, y, z);
        } else {
            this.copperOutcrop(ctx, x, y, z);
        }
    }

    /** Felsnadel aus Sturmstein und Tuff, die Spitze vom Blitz gespalten (zwei Zacken), mit Adern aus geladenem Stein. */
    static void spire(LairContext ctx, int x, int y, int z, int height) {
        if (!ctx.near(x, z, 3)) {
            return;
        }
        BlockState stone = ModBlocks.STORMSTONE.base().get().defaultBlockState();
        for (int k = -2; k < height; k++) {
            double t = Math.max(0, k) / (double) height;
            double r = 1.5 - 0.9 * t + (k < 1 ? 0.6 : 0.0);
            int reach = (int) Math.ceil(r);
            for (int dx = -reach; dx <= reach; dx++) {
                for (int dz = -reach; dz <= reach; dz++) {
                    if (dx * dx + dz * dz <= r * r + 0.3) {
                        double q = ctx.rand(x + dx, z + dz + k * 7, 611);
                        BlockState s = q < 0.08 ? ModBlocks.CHARGED_STORMSTONE.get().defaultBlockState()
                                : q < 0.3 ? Blocks.COBBLED_DEEPSLATE.defaultBlockState() : stone;
                        ctx.setIfOpen(x + dx, y + k, z + dz, s);
                    }
                }
            }
        }
        // gespaltene Spitze: zwei schmale Zacken, dazwischen ein Fulgurit
        boolean alongX = ctx.rand(x, z, 612) < 0.5;
        for (int k = 0; k < 3; k++) {
            ctx.setIfOpen(x + (alongX ? 1 : 0), y + height + k, z + (alongX ? 0 : 1), stone);
            if (k < 2) {
                ctx.setIfOpen(x - (alongX ? 1 : 0), y + height + k, z - (alongX ? 0 : 1), stone);
            }
        }
        ctx.setIfOpen(x, y + height, z, ModBlocks.FULGURITE.get().defaultBlockState());
    }

    /**
     * Einschlagstelle: eine Fulguritröhre ragt aus versengtem Fels (ein Ring aus Basalt und Schwarzstein), daneben
     * geladener Stein und ein Sturmkristall.
     */
    static void strike(LairContext ctx, int x, int y, int z) {
        if (!ctx.near(x, z, 3)) {
            return;
        }
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                double d = Math.sqrt(dx * dx + dz * dz);
                if (d > 2.3 || ctx.rand(x + dx, z + dz, 620) < d * 0.25) {
                    continue;
                }
                int gy = ctx.groundY(x + dx, z + dz);
                if (LairContext.isGround(ctx.get(x + dx, gy, z + dz))) {
                    BlockState s = d < 1.1 ? ModBlocks.CHARGED_STORMSTONE.get().defaultBlockState()
                            : ctx.rand(x + dx, z + dz, 621) < 0.5 ? Blocks.SMOOTH_BASALT.defaultBlockState() : Blocks.BLACKSTONE.defaultBlockState();
                    ctx.set(x + dx, gy, z + dz, s);
                }
            }
        }
        int h = 1 + (int) (ctx.rand(x, z, 622) * 3);
        for (int k = 0; k <= h; k++) {
            ctx.setIfOpen(x, y + k, z, ModBlocks.FULGURITE.get().defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y));
        }
        if (ctx.rand(x, z, 623) < 0.6) {
            ctx.setIfOpen(x + 1, y + 1, z, ModBlocks.STORM_CRYSTAL.get().defaultBlockState());
        }
    }

    /** Sturmkristalle in einem Grüppchen. */
    static void crystals(LairContext ctx, int x, int y, int z) {
        if (!ctx.near(x, z, 3)) {
            return;
        }
        int count = 3 + (int) (ctx.rand(x, z, 630) * 4);
        for (int i = 0; i < count; i++) {
            int ox = (int) Math.round((ctx.rand(x + i, z, 631) - 0.5) * 4);
            int oz = (int) Math.round((ctx.rand(x, z + i, 632) - 0.5) * 4);
            int gy = ctx.groundY(x + ox, z + oz);
            if (LairContext.isGround(ctx.get(x + ox, gy, z + oz))) {
                if (ctx.rand(x + ox, z + oz, 633) < 0.35) {
                    ctx.set(x + ox, gy, z + oz, ModBlocks.CHARGED_STORMSTONE.get().defaultBlockState());
                }
                ctx.setIfOpen(x + ox, gy + 1, z + oz, ModBlocks.STORM_CRYSTAL.get().defaultBlockState());
            }
        }
    }

    /** Felsbrocken mit einem alten, grün angelaufenen Blitzableiter der Sturmwächter. */
    private void rodStone(LairContext ctx, int x, int y, int z) {
        double radius = 1.3 + ctx.rand(x, z, 640) * 1.2;
        int reach = (int) Math.ceil(radius);
        int top = y;
        for (int dx = -reach; dx <= reach; dx++) {
            for (int dy = 0; dy <= reach; dy++) {
                for (int dz = -reach; dz <= reach; dz++) {
                    if (dx * dx + dy * dy * 1.3 + dz * dz <= radius * radius) {
                        double q = ctx.rand(x + dx, z + dz + dy * 7, 641);
                        ctx.setIfOpen(x + dx, y + dy, z + dz, q < 0.6 ? ModBlocks.STORMSTONE.base().get().defaultBlockState()
                                : q < 0.85 ? Blocks.COBBLED_DEEPSLATE.defaultBlockState() : Blocks.SMOOTH_BASALT.defaultBlockState());
                        if (dx == 0 && dz == 0) {
                            top = Math.max(top, y + dy);
                        }
                    }
                }
            }
        }
        ctx.setIfOpen(x, top + 1, z, Blocks.LIGHTNING_ROD.waxed().oxidized().defaultBlockState());
    }

    /** Freiliegende Kupferader, grün angelaufen, mit ein paar Brocken Roh-Kupfer. */
    private void copperOutcrop(LairContext ctx, int x, int y, int z) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                for (int dy = 0; dy <= (dx == 0 && dz == 0 ? 2 : 1); dy++) {
                    double q = ctx.rand(x + dx, z + dz + dy * 5, 650);
                    BlockState s = q < 0.4 ? Blocks.COPPER_ORE.defaultBlockState() : q < 0.5 ? Blocks.RAW_COPPER_BLOCK.defaultBlockState()
                            : q < 0.62 ? Blocks.COPPER_BLOCK.waxed().weathered().defaultBlockState()
                            : ModBlocks.STORMSTONE.base().get().defaultBlockState();
                    ctx.setIfOpen(x + dx, y + dy, z + dz, s);
                }
            }
        }
    }
}
