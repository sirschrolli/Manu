package io.github.sirschrolli.kingdomsatwar.worldgen.lair;

import io.github.sirschrolli.kingdomsatwar.registry.ModBlocks;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;

/**
 * Der Frostkessel rund um Glaciaras Gletscherkessel (gut 110 Blöcke weit): ewiges Eis und Schnee. Bäume erfrieren
 * (nahe am Kessel verschwinden sie, am Rand bleiben kahle Stämme), Gewässer frieren zu, der Boden wird zu Schnee,
 * Gletschereis, Raureifstein und Frosterde, Schneewehen türmen sich. Dazwischen stehen Eisnadeln aus Packeis,
 * leuchtende Frostkristalle, bereifte Felsbrocken, Frostblumen und erfrorene Baumstümpfe.
 */
final class FrostZone extends NestZone {
    FrostZone(LairDesign design, int innerRadius) {
        super(design, innerRadius);
    }

    @Override
    protected void column(LairContext ctx, int x, int z, double local) {
        boolean glacier = local > 0.7;
        int y = surfaceAfterPlants(ctx, x, z, (state, px, py, pz) -> {
            if (state.is(BlockTags.LEAVES)) {
                return Blocks.AIR.defaultBlockState();
            }
            if (state.is(BlockTags.LOGS)) {
                return glacier ? Blocks.AIR.defaultBlockState() : state;   // am Rand bleiben kahle Stämme stehen
            }
            if (state.is(Blocks.SNOW)) {
                return state;
            }
            return Blocks.AIR.defaultBlockState();
        });
        BlockState ground = ctx.get(x, y, z);
        // Gewässer frieren zu (die oberste Lage)
        if (!ground.getFluidState().isEmpty()) {
            if (ground.getFluidState().is(Fluids.WATER) && local > 0.35) {
                ctx.set(x, y, z, local > 0.8 ? Blocks.PACKED_ICE.defaultBlockState() : Blocks.ICE.defaultBlockState());
                if (ctx.rand(x, z, 101) < 0.25 && ctx.get(x, y + 1, z).isAir()) {
                    ctx.set(x, y + 1, z, Blocks.SNOW.defaultBlockState());
                }
            }
            return;
        }
        if (local < 0.3) {
            // ganz am Rand: nur ein wenig Schnee
            if (ctx.rand(x, z, 102) < local * 2.5 && ctx.get(x, y + 1, z).isAir() && LairContext.isGround(ground)) {
                ctx.set(x, y + 1, z, Blocks.SNOW.defaultBlockState());
            }
            return;
        }
        double n1 = ctx.noise(x, z, 0.035, 103);
        double n2 = ctx.noise(x, z, 0.11, 104);
        int depth = reshapeDepth(ctx, x, y, z);
        if (local < 0.5) {
            // Übergang: Schnee- und Frosterde-Flecken
            if (ctx.rand(x, z, 105) < (local - 0.3) * 5.0 && isReplaceableGround(ground)) {
                ctx.set(x, y, z, ctx.rand(x, z, 106) < 0.6 ? Blocks.SNOW_BLOCK.defaultBlockState() : ModBlocks.FROZEN_SOIL.get().defaultBlockState());
                if (ctx.get(x, y + 1, z).isAir()) {
                    ctx.set(x, y + 1, z, Blocks.SNOW.defaultBlockState());
                }
            }
            return;
        }
        BlockState surface;
        boolean vein = Math.abs(n2 - 0.5) < 0.02 && n1 > 0.4 && local > 0.75;
        if (vein) {
            surface = Blocks.BLUE_ICE.defaultBlockState();
        } else if (n1 > 0.66) {
            surface = ModBlocks.GLACIER_ICE.get().defaultBlockState();
        } else if (n2 > 0.78) {
            surface = ModBlocks.RIME_STONE.base().get().defaultBlockState();
        } else if (n1 < 0.3 && local < 0.75) {
            surface = ModBlocks.FROZEN_SOIL.get().defaultBlockState();
        } else {
            surface = Blocks.SNOW_BLOCK.defaultBlockState();
        }
        ctx.set(x, y, z, surface);
        boolean snowy = surface.is(Blocks.SNOW_BLOCK) || surface.is(ModBlocks.FROZEN_SOIL.get());
        this.fillBelow(ctx, x, y, z, depth, (k, yy) -> {
            if (snowy && k <= 2) {
                return k == 1 && surface.is(ModBlocks.FROZEN_SOIL.get()) ? ModBlocks.FROZEN_SOIL.get().defaultBlockState()
                        : Blocks.SNOW_BLOCK.defaultBlockState();
            }
            double strata = ctx.noise(x * 0.3 + z * 0.3, yy, 0.35, 107);
            return strata < 0.5 ? ModBlocks.RIME_STONE.base().get().defaultBlockState()
                    : strata < 0.78 ? Blocks.PACKED_ICE.defaultBlockState() : ModBlocks.GLACIER_ICE.get().defaultBlockState();
        });
        // Schneewehen
        if (snowy && ctx.get(x, y + 1, z).isAir()) {
            double drift = ctx.noise(x, z, 0.07, 108) + (ctx.rand(x, z, 109) - 0.5) * 0.08;
            int layers = drift > 0.45 ? Math.min(6, 1 + (int) ((drift - 0.45) * 16)) : (ctx.rand(x, z, 110) < 0.5 ? 1 : 0);
            if (layers > 0) {
                ctx.set(x, y + 1, z, Blocks.SNOW.defaultBlockState().setValue(SnowLayerBlock.LAYERS, layers));
            }
        }
    }

    @Override
    protected void decoration(LairContext ctx, int x, int z, double r, double local) {
        int y = ctx.groundY(x, z);
        BlockState ground = ctx.get(x, y, z);
        if (!ground.getFluidState().isEmpty() || !LairContext.isGround(ground)) {
            return;
        }
        if (r < 0.10) {
            this.iceSpike(ctx, x, y + 1, z, 5 + (int) (ctx.rand(x, z, 111) * 11 * local));
        } else if (r < 0.24) {
            this.crystals(ctx, x, y, z);
        } else if (r < 0.34) {
            this.boulder(ctx, x, y, z);
        } else if (r < 0.62) {
            if (ground.is(ModBlocks.FROZEN_SOIL.get()) && ctx.get(x, y + 1, z).canBeReplaced()) {
                ctx.set(x, y + 1, z, ModBlocks.FROST_FLOWER.get().defaultBlockState());
            }
        } else if (r < 0.70) {
            // erfrorener Baumstumpf
            int height = 2 + (int) (ctx.rand(x, z, 112) * 3);
            for (int k = 1; k <= height; k++) {
                ctx.setIfOpen(x, y + k, z, Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState());
            }
            ctx.setIfOpen(x, y + height + 1, z, Blocks.SNOW.defaultBlockState());
        }
    }

    /** Eisnadel wie in den Eiszapfen-Ebenen: Packeis mit Kern aus Blaueis, unten breiter. */
    private void iceSpike(LairContext ctx, int x, int y, int z, int height) {
        double base = 1.2 + height * 0.09;
        int reach = (int) Math.ceil(base) + 1;
        for (int k = 0; k < height; k++) {
            double r = base * (1.0 - (double) k / (height + 1)) + 0.3;
            for (int dx = -reach; dx <= reach; dx++) {
                for (int dz = -reach; dz <= reach; dz++) {
                    if (dx * dx + dz * dz <= r * r) {
                        boolean core = dx == 0 && dz == 0 && k < height - 2;
                        ctx.setIfOpen(x + dx, y + k, z + dz, core ? Blocks.BLUE_ICE.defaultBlockState() : Blocks.PACKED_ICE.defaultBlockState());
                    }
                }
            }
        }
        for (int dx = -reach; dx <= reach; dx++) {
            for (int dz = -reach; dz <= reach; dz++) {
                if (dx * dx + dz * dz <= base * base) {
                    ctx.fill(x + dx, y - 3, y - 1, z + dz, Blocks.PACKED_ICE.defaultBlockState());
                }
            }
        }
    }

    /** Eine Gruppe leuchtender Frostkristalle (zwei bis fünf schräge Säulen). */
    private void crystals(LairContext ctx, int x, int y, int z) {
        int count = 2 + (int) (ctx.rand(x, z, 113) * 4);
        for (int i = 0; i < count; i++) {
            int ox = (int) Math.round((ctx.rand(x + i, z, 114) - 0.5) * 3);
            int oz = (int) Math.round((ctx.rand(x, z + i, 115) - 0.5) * 3);
            int height = 1 + (int) (ctx.rand(x + i, z + i, 116) * 3);
            int lean = ctx.rand(x - i, z, 117) < 0.5 ? 1 : -1;
            for (int k = 1; k <= height; k++) {
                int shift = k > 2 ? lean : 0;
                ctx.setIfOpen(x + ox + shift, y + k, z + oz, ModBlocks.FROST_CRYSTAL.get().defaultBlockState());
            }
        }
    }

    /** Bereifter Felsbrocken. */
    private void boulder(LairContext ctx, int x, int y, int z) {
        double radius = 1.2 + ctx.rand(x, z, 118) * 1.3;
        int reach = (int) Math.ceil(radius);
        for (int dx = -reach; dx <= reach; dx++) {
            for (int dy = 0; dy <= reach; dy++) {
                for (int dz = -reach; dz <= reach; dz++) {
                    if (dx * dx + dy * dy * 1.6 + dz * dz <= radius * radius) {
                        BlockState state = dy == reach || ctx.rand(x + dx, z + dz + dy, 119) < 0.3
                                ? Blocks.SNOW_BLOCK.defaultBlockState() : ModBlocks.RIME_STONE.base().get().defaultBlockState();
                        ctx.setIfOpen(x + dx, y + dy, z + dz, state);
                    }
                }
            }
        }
    }
}
