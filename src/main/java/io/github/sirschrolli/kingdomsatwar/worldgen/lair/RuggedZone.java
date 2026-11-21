package io.github.sirschrolli.kingdomsatwar.worldgen.lair;

import io.github.sirschrolli.kingdomsatwar.registry.ModBlocks;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Die Zerklüfteten Weiten rund um Terragons Felsschlucht (gut 110 Blöcke weit): Das Land wird karg und steinig. Bäume
 * und Büsche verschwinden (am Rand bleiben dürre Stämme), der Boden wird zu grober Erde, Kies, Geröll, Stein, Andesit
 * und Schiefer; dazwischen trockenes Gras und tote Büsche. Felsnadeln aus Schiefer und Terrakotta ragen auf, Felsbrocken
 * liegen herum, Bernsteinkristalle brechen aus dem Boden, und glimmende Risse aus Spaltgestein ziehen sich durchs Land.
 */
final class RuggedZone extends NestZone {
    RuggedZone(LairDesign design, int innerRadius) {
        super(design, innerRadius);
    }

    @Override
    protected void column(LairContext ctx, int x, int z, double local) {
        boolean barren = local > 0.6;
        int y = surfaceAfterPlants(ctx, x, z, (state, px, py, pz) -> {
            if (state.is(BlockTags.LEAVES)) {
                return Blocks.AIR.defaultBlockState();
            }
            if (state.is(BlockTags.LOGS)) {
                return barren ? Blocks.AIR.defaultBlockState() : state;   // am Rand bleiben dürre Stämme
            }
            if (state.is(Blocks.SNOW)) {
                return state;
            }
            return barren || ctx.rand(px, pz, 401) < local ? Blocks.AIR.defaultBlockState() : state;
        });
        BlockState ground = ctx.get(x, y, z);
        if (!ground.getFluidState().isEmpty() || local < 0.25) {
            return;
        }
        if (!isReplaceableGround(ground)) {
            return;
        }
        double n1 = ctx.noise(x, z, 0.04, 402);
        double n2 = ctx.noise(x, z, 0.12, 403);
        if (ctx.rand(x, z, 404) > local * 1.6) {
            return;   // am Rand fleckig
        }
        // glimmende Risse: schmale Höhenlinien eines großräumigen Rauschens
        boolean crack = Math.abs(ctx.noise(x, z, 0.022, 405) - 0.5) < 0.012 && local > 0.55;
        BlockState surface;
        if (crack) {
            surface = ModBlocks.RIFT_STONE.get().defaultBlockState();
        } else if (n1 > 0.66) {
            surface = n2 > 0.5 ? Blocks.ANDESITE.defaultBlockState() : Blocks.STONE.defaultBlockState();
        } else if (n1 > 0.58) {
            surface = ModBlocks.SHALE.base().get().defaultBlockState();
        } else if (n2 > 0.72) {
            surface = Blocks.GRAVEL.defaultBlockState();
        } else if (n2 < 0.2) {
            surface = ModBlocks.RUBBLE.get().defaultBlockState();
        } else {
            surface = Blocks.COARSE_DIRT.defaultBlockState();
        }
        ctx.set(x, y, z, surface);
        boolean soft = surface.is(Blocks.COARSE_DIRT) || surface.is(Blocks.GRAVEL) || surface.is(ModBlocks.RUBBLE.get());
        int depth = reshapeDepth(ctx, x, y, z);
        this.fillBelow(ctx, x, y, z, depth, (k, yy) -> {
            if (soft && k <= 2) {
                return surface.is(ModBlocks.RUBBLE.get()) ? Blocks.GRAVEL.defaultBlockState() : Blocks.COARSE_DIRT.defaultBlockState();
            }
            double strata = ctx.noise(x * 0.25 + z * 0.25, yy, 0.4, 406);
            return strata < 0.45 ? Blocks.STONE.defaultBlockState()
                    : strata < 0.7 ? ModBlocks.SHALE.base().get().defaultBlockState() : Blocks.ANDESITE.defaultBlockState();
        });
        if (crack || !soft || !ctx.get(x, y + 1, z).isAir()) {
            return;
        }
        double r = ctx.rand(x, z, 407);
        if (r < 0.05) {
            ctx.set(x, y + 1, z, Blocks.DEAD_BUSH.defaultBlockState());
        } else if (r < 0.14) {
            ctx.set(x, y + 1, z, Blocks.SHORT_DRY_GRASS.defaultBlockState());
        } else if (r < 0.16 && surface.is(Blocks.COARSE_DIRT)) {
            ctx.set(x, y + 1, z, Blocks.TALL_DRY_GRASS.defaultBlockState());
        }
    }

    @Override
    protected double decorationChance() {
        return 0.016;
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
        if (r < 0.14) {
            this.spire(ctx, x, y + 1, z, 5 + (int) (ctx.rand(x, z, 410) * 9 * local));
        } else if (r < 0.36) {
            this.boulder(ctx, x, y, z);
        } else if (r < 0.52) {
            this.crystals(ctx, x, y, z);
        } else if (r < 0.62) {
            this.oreOutcrop(ctx, x, y, z);
        } else {
            // Geröllhaufen mit toten Büschen
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (ctx.rand(x + dx, z + dz, 411) < 0.6) {
                        int gy = ctx.groundY(x + dx, z + dz);
                        ctx.setIfOpen(x + dx, gy + 1, z + dz, ModBlocks.RUBBLE.get().defaultBlockState());
                    }
                }
            }
        }
    }

    /** Felsnadel aus Schichten (Schiefer, Terrakotta, Stein), in der Mitte schlanker, oben ein breiter Deckstein. */
    static void spire(LairContext ctx, int x, int y, int z, int height) {
        if (!ctx.near(x, z, 3)) {
            return;
        }
        BlockState[] layers = {ModBlocks.SHALE.base().get().defaultBlockState(), Blocks.TERRACOTTA.defaultBlockState(),
                Blocks.DYED_TERRACOTTA.brown().defaultBlockState(), Blocks.STONE.defaultBlockState(), Blocks.DYED_TERRACOTTA.lightGray().defaultBlockState()};
        for (int k = -2; k < height; k++) {
            double t = Math.max(0, k) / (double) height;
            double r = 1.6 - 0.9 * Math.sin(t * Math.PI) + (k < 1 ? 0.6 : 0.0);
            int reach = (int) Math.ceil(r);
            BlockState layer = layers[Math.floorMod((y + k) / 2, layers.length)];
            for (int dx = -reach; dx <= reach; dx++) {
                for (int dz = -reach; dz <= reach; dz++) {
                    if (dx * dx + dz * dz <= r * r + 0.3) {
                        ctx.setIfOpen(x + dx, y + k, z + dz, layer);
                    }
                }
            }
        }
        // Deckstein
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                if (dx * dx + dz * dz <= 4) {
                    ctx.setIfOpen(x + dx, y + height, z + dz, ModBlocks.SHALE.base().get().defaultBlockState());
                }
            }
        }
    }

    private void boulder(LairContext ctx, int x, int y, int z) {
        double radius = 1.2 + ctx.rand(x, z, 420) * 1.6;
        int reach = (int) Math.ceil(radius);
        for (int dx = -reach; dx <= reach; dx++) {
            for (int dy = -1; dy <= reach; dy++) {
                for (int dz = -reach; dz <= reach; dz++) {
                    if (dx * dx + dy * dy * 1.4 + dz * dz <= radius * radius) {
                        double q = ctx.rand(x + dx, z + dz + dy * 7, 421);
                        BlockState s = q < 0.4 ? Blocks.ANDESITE.defaultBlockState() : q < 0.75 ? Blocks.COBBLESTONE.defaultBlockState()
                                : ModBlocks.SHALE.base().get().defaultBlockState();
                        ctx.setIfOpen(x + dx, y + dy, z + dz, s);
                    }
                }
            }
        }
    }

    /** Bernsteinkristalle, die aus dem Boden brechen (Säulen und kleine Drusen). */
    static void crystals(LairContext ctx, int x, int y, int z) {
        if (!ctx.near(x, z, 3)) {
            return;
        }
        int count = 2 + (int) (ctx.rand(x, z, 430) * 3);
        for (int i = 0; i < count; i++) {
            int ox = (int) Math.round((ctx.rand(x + i, z, 431) - 0.5) * 4);
            int oz = (int) Math.round((ctx.rand(x, z + i, 432) - 0.5) * 4);
            int gy = ctx.groundY(x + ox, z + oz);
            int h = 1 + (int) (ctx.rand(x + i, z + i, 433) * 3);
            for (int k = 1; k <= h; k++) {
                ctx.setIfOpen(x + ox, gy + k, z + oz, ModBlocks.AMBER_CRYSTAL.get().defaultBlockState());
            }
            ctx.setIfOpen(x + ox + 1, gy + 1, z + oz, ModBlocks.AMBER_CLUSTER.get().defaultBlockState());
        }
    }

    /** Freiliegende Erzader auf einem Felsbuckel (Eisen, Kupfer, selten Gold). */
    private void oreOutcrop(LairContext ctx, int x, int y, int z) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                for (int dy = 0; dy <= (dx == 0 && dz == 0 ? 2 : 1); dy++) {
                    double q = ctx.rand(x + dx, z + dz + dy * 5, 440);
                    BlockState s = q < 0.12 ? Blocks.GOLD_ORE.defaultBlockState() : q < 0.35 ? Blocks.IRON_ORE.defaultBlockState()
                            : q < 0.55 ? Blocks.COPPER_ORE.defaultBlockState() : Blocks.STONE.defaultBlockState();
                    ctx.setIfOpen(x + dx, y + dy, z + dz, s);
                }
            }
        }
    }
}
