package io.github.sirschrolli.kingdomsatwar.worldgen.lair;

import io.github.sirschrolli.kingdomsatwar.block.GlowCoralBlock;
import io.github.sirschrolli.kingdomsatwar.registry.ModBlocks;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SeaPickleBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Die Abgrundgrotte rund um Thalassas Versunkene Grotte (gut 110 Blöcke weit) – am Meeresgrund: Sand und Kies werden
 * zu dunklem Abgrundsand, Felsrücken aus Abgrundstein brechen hervor. Zwischen Seegras und Tang leuchten Korallen und
 * Meeresgurken, Schlote aus Basalt lassen Blasen aufsteigen, die Gebeine von Walen liegen im Sand, und geborstene
 * Säulen aus Prismarin erzählen von einem versunkenen Reich.
 */
final class AbyssZone extends NestZone {
    AbyssZone(LairDesign design, int innerRadius) {
        super(design, innerRadius);
    }

    /** Oberster fester Block des Meeresgrunds (unter Wasser und Pflanzen). */
    private static int seabed(LairContext ctx, int x, int z) {
        return ctx.originalHeight(x, z, true) - 1;
    }

    @Override
    protected void column(LairContext ctx, int x, int z, double local) {
        int y = seabed(ctx, x, z);
        BlockState ground = ctx.get(x, y, z);
        if (ctx.get(x, y + 1, z).getFluidState().isEmpty() || !isReplaceableGround(ground)) {
            return;   // nur am Meeresgrund (unter Wasser)
        }
        if (ctx.rand(x, z, 901) > local * 1.7) {
            return;   // am Rand fleckig
        }
        double n1 = ctx.noise(x, z, 0.05, 902);
        double n2 = ctx.noise(x, z, 0.15, 903);
        BlockState surface;
        if (n1 > 0.72) {
            surface = ModBlocks.ABYSSAL_STONE.base().get().defaultBlockState();
        } else if (n2 > 0.76) {
            surface = Blocks.GRAVEL.defaultBlockState();
        } else if (n2 < 0.16) {
            surface = Blocks.CLAY.defaultBlockState();
        } else {
            surface = ModBlocks.ABYSSAL_SAND.get().defaultBlockState();
        }
        ctx.set(x, y, z, surface);
        boolean soft = !surface.is(ModBlocks.ABYSSAL_STONE.base().get());
        int depth = reshapeDepth(ctx, x, y, z);
        this.fillBelow(ctx, x, y, z, depth, (k, yy) -> soft && k <= 2 ? ModBlocks.ABYSSAL_SAND.get().defaultBlockState()
                : ModBlocks.ABYSSAL_STONE.base().get().defaultBlockState());
        BlockState above = ctx.get(x, y + 1, z);
        if (!above.is(Blocks.WATER)) {
            return;   // Seegras und Tang bleiben stehen
        }
        double r = ctx.rand(x, z, 904);
        if (r < 0.012 * local) {
            ctx.set(x, y + 1, z, ModBlocks.GLOW_CORAL.get().defaultBlockState().setValue(GlowCoralBlock.WATERLOGGED, true));
        } else if (r < 0.022 * local) {
            ctx.set(x, y + 1, z, Blocks.SEA_PICKLE.defaultBlockState().setValue(SeaPickleBlock.PICKLES, 1 + (int) (r * 1000) % 4));
        }
    }

    @Override
    protected double decorationChance() {
        return 0.01;
    }

    @Override
    protected int decorationMargin() {
        return 6;
    }

    @Override
    protected void decoration(LairContext ctx, int x, int z, double r, double local) {
        int y = seabed(ctx, x, z);
        if (!ctx.get(x, y + 1, z).is(Blocks.WATER) && !ctx.get(x, y + 1, z).is(Blocks.SEAGRASS)
                && !ctx.get(x, y + 1, z).is(Blocks.KELP) && !ctx.get(x, y + 1, z).is(Blocks.KELP_PLANT)) {
            return;
        }
        if (r < 0.2) {
            this.reef(ctx, x, y, z);
        } else if (r < 0.36) {
            this.vent(ctx, x, y, z);
        } else if (r < 0.5) {
            this.ribs(ctx, x, y, z);
        } else if (r < 0.66) {
            this.ruin(ctx, x, y, z);
        } else {
            this.rock(ctx, x, y, z);
        }
    }

    private static void water(LairContext ctx, int x, int y, int z, BlockState state) {
        BlockState old = ctx.get(x, y, z);
        if (old.is(Blocks.WATER) || old.is(Blocks.SEAGRASS) || old.is(Blocks.TALL_SEAGRASS) || old.is(Blocks.KELP) || old.is(Blocks.KELP_PLANT)) {
            ctx.set(x, y, z, state);
        }
    }

    /** Leuchtkorallen-Riff: ein Hügel aus Abgrundstein, dicht bewachsen. */
    private void reef(LairContext ctx, int x, int y, int z) {
        if (!ctx.near(x, z, 4)) {
            return;
        }
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                double d = Math.sqrt(dx * dx + dz * dz);
                if (d > 2.4) {
                    continue;
                }
                int h = d < 1.2 ? 2 : 1;
                int gy = seabed(ctx, x + dx, z + dz);
                for (int k = 1; k <= h; k++) {
                    water(ctx, x + dx, gy + k, z + dz, ModBlocks.ABYSSAL_STONE.base().get().defaultBlockState());
                }
                if (ctx.rand(x + dx, z + dz, 910) < 0.7) {
                    water(ctx, x + dx, gy + h + 1, z + dz, ModBlocks.GLOW_CORAL.get().defaultBlockState().setValue(GlowCoralBlock.WATERLOGGED, true));
                }
            }
        }
    }

    /** Schlot: eine Säule aus Basalt mit Seelensand obenauf – Blasen steigen in einer Säule auf. */
    private void vent(LairContext ctx, int x, int y, int z) {
        if (!ctx.near(x, z, 2)) {
            return;
        }
        int h = 2 + (int) (ctx.rand(x, z, 920) * 4);
        for (int k = 1; k <= h; k++) {
            water(ctx, x, y + k, z, Blocks.BASALT.defaultBlockState());
            if (k == 1) {
                for (Direction side : Direction.Plane.HORIZONTAL) {
                    if (ctx.rand(x + side.getStepX(), z + side.getStepZ(), 921) < 0.6) {
                        water(ctx, x + side.getStepX(), y + 1, z + side.getStepZ(), Blocks.BLACKSTONE.defaultBlockState());
                    }
                }
            }
        }
        water(ctx, x, y + h + 1, z, Blocks.SOUL_SAND.defaultBlockState());
        // aufsteigende Blasen bis zur Oberfläche
        int top = ctx.originalHeight(x, z, false) - 1;
        BlockState bubbles = Blocks.BUBBLE_COLUMN.defaultBlockState().setValue(net.minecraft.world.level.block.BubbleColumnBlock.DRAG_DOWN, false);
        for (int k = y + h + 2; k <= top; k++) {
            if (ctx.get(x, k, z).is(Blocks.WATER)) {
                ctx.set(x, k, z, bubbles);
            }
        }
    }

    /** Rippenbögen eines Wals im Sand. */
    private void ribs(LairContext ctx, int x, int y, int z) {
        if (!ctx.near(x, z, 8)) {
            return;
        }
        boolean alongX = ctx.rand(x, z, 930) < 0.5;
        for (int k = -2; k <= 2; k++) {
            for (int s = -1; s <= 1; s += 2) {
                for (int step = 0; step <= 6; step++) {
                    double t = step / 6.0;
                    double w = s * (1.0 + Math.sin(t * Math.PI) * 2.0);
                    int h = (int) Math.round(Math.sin(t * Math.PI * 0.9) * (3.5 - Math.abs(k) * 0.4));
                    int bx = x + (alongX ? k * 2 : (int) Math.round(w));
                    int bz = z + (alongX ? (int) Math.round(w) : k * 2);
                    water(ctx, bx, seabed(ctx, bx, bz) + 1 + h, bz, Blocks.BONE_BLOCK.defaultBlockState());
                }
            }
        }
    }

    /** Geborstene Säule aus Prismarin, daneben ein umgestürztes Stück. */
    private void ruin(LairContext ctx, int x, int y, int z) {
        if (!ctx.near(x, z, 6)) {
            return;
        }
        int h = 2 + (int) (ctx.rand(x, z, 940) * 6);
        for (int k = 1; k <= h; k++) {
            water(ctx, x, y + k, z, k == 1 ? Blocks.PRISMARINE_BRICKS.defaultBlockState() : Blocks.PRISMARINE.defaultBlockState());
        }
        boolean alongX = ctx.rand(x, z, 941) < 0.5;
        for (int k = 2; k < 5; k++) {
            int bx = x + (alongX ? k : 0);
            int bz = z + (alongX ? 0 : k);
            water(ctx, bx, seabed(ctx, bx, bz) + 1, bz, Blocks.PRISMARINE.defaultBlockState());
        }
        if (ctx.rand(x, z, 942) < 0.5) {
            water(ctx, x, y + h + 1, z, Blocks.SEA_LANTERN.defaultBlockState());
        }
    }

    /** Felsrücken aus Abgrundstein mit Perlmuttadern. */
    private void rock(LairContext ctx, int x, int y, int z) {
        if (!ctx.near(x, z, 4)) {
            return;
        }
        double radius = 1.5 + ctx.rand(x, z, 950) * 1.8;
        int reach = (int) Math.ceil(radius);
        for (int dx = -reach; dx <= reach; dx++) {
            for (int dy = 0; dy <= reach; dy++) {
                for (int dz = -reach; dz <= reach; dz++) {
                    if (dx * dx + dy * dy * 1.5 + dz * dz <= radius * radius) {
                        double q = ctx.rand(x + dx, z + dz + dy * 7, 951);
                        water(ctx, x + dx, y + 1 + dy, z + dz, q < 0.1 ? ModBlocks.NACRE_BLOCK.get().defaultBlockState()
                                : ModBlocks.ABYSSAL_STONE.base().get().defaultBlockState());
                    }
                }
            }
        }
    }
}
