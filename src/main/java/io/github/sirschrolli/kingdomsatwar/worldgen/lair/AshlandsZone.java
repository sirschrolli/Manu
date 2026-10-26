package io.github.sirschrolli.kingdomsatwar.worldgen.lair;

import io.github.sirschrolli.kingdomsatwar.registry.ModBlocks;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;

/**
 * Die Aschelande rund um Pyrrhons Vulkankrater (bis gut 120 Blöcke weit): Der Boden wird zu Asche, Aschenerde und
 * Vulkangestein mit glühenden Rissen, Aschedünen türmen sich, Bäume verkohlen, Pflanzen verbrennen, flache Tümpel
 * trocknen aus. Dazwischen stehen verkohlte Baumgerippe, Basaltsäulen, rauchende Glutspalten, Feuerlilien und
 * Knochen. Zum Rand hin wird die Verwüstung lückenhaft und geht in die ursprüngliche Landschaft über.
 *
 * <p>Alles ergibt sich aus Lage und Startwert des Nests, damit die Chunks nahtlos zusammenpassen.
 */
final class AshlandsZone {
    /** Bis hierher gestaltet der Krater selbst. */
    private final int craterRadius;
    private final LairDesign design;

    AshlandsZone(LairDesign design, int craterRadius) {
        this.design = design;
        this.craterRadius = craterRadius;
    }

    void build(LairContext ctx) {
        int cx = ctx.center.getX();
        int cz = ctx.center.getZ();
        for (int x = ctx.box.minX(); x <= ctx.box.maxX(); x++) {
            for (int z = ctx.box.minZ(); z <= ctx.box.maxZ(); z++) {
                double local = this.strength(ctx, x, z, cx, cz);
                if (local > 0.08) {
                    this.column(ctx, x, z, local);
                }
            }
        }
        // Einzelheiten auch knapp außerhalb des Chunks prüfen, damit z. B. Äste über die Chunkgrenze hinweg passen
        int margin = 3;
        for (int x = ctx.box.minX() - margin; x <= ctx.box.maxX() + margin; x++) {
            for (int z = ctx.box.minZ() - margin; z <= ctx.box.maxZ() + margin; z++) {
                double r = ctx.rand(x, z, 75);
                if (r < 0.02) {
                    double local = this.strength(ctx, x, z, cx, cz);
                    if (local > 0.6) {
                        this.decoration(ctx, x, z, r);
                    }
                }
            }
        }
    }

    /**
     * Wie stark die Verwüstung hier ist: 1 nahe am Krater, zum Biomrand hin abnehmend, mit Flecken (Rauschen), damit
     * der Übergang natürlich aussieht. 0 innerhalb des Kraters (den baut der Entwurf selbst) und außerhalb des Bioms.
     */
    private double strength(LairContext ctx, int x, int z, int cx, int cz) {
        double dx = x - cx;
        double dz = z - cz;
        double d = Math.sqrt(dx * dx + dz * dz);
        if (d < this.craterRadius) {
            return 0;
        }
        double edge = this.design.zoneEdge(ctx, Math.atan2(dz, dx));
        if (d >= edge) {
            return 0;
        }
        double t = Mth.clamp((edge - d) / 38.0, 0, 1);
        return t + (ctx.noise(x, z, 0.045, 72) - 0.5) * 0.7;
    }

    private void column(LairContext ctx, int x, int z, double local) {
        int top = ctx.originalHeight(x, z, false) - 1;
        int min = ctx.level.getMinY() + 1;
        // 1) Pflanzen und Bäume: Laub verbrennt, Stämme verkohlen, Pflanzen verschwinden
        int y = top;
        while (y > min) {
            BlockState state = ctx.get(x, y, z);
            if (state.isAir()) {
                y--;
                continue;
            }
            if (!state.getFluidState().isEmpty() && !state.is(BlockTags.LEAVES)) {
                break;
            }
            if (state.is(BlockTags.LOGS)) {
                if (!state.is(ModBlocks.CHARRED_LOG.get())) {
                    BlockState charred = ModBlocks.CHARRED_LOG.get().defaultBlockState();
                    if (state.hasProperty(RotatedPillarBlock.AXIS)) {
                        charred = charred.setValue(RotatedPillarBlock.AXIS, state.getValue(RotatedPillarBlock.AXIS));
                    }
                    ctx.set(x, y, z, charred);
                }
                y--;
                continue;
            }
            if (LairContext.isGround(state)) {
                break;
            }
            ctx.set(x, y, z, Blocks.AIR.defaultBlockState());   // Laub, Gras, Blumen, Kakteen, Schnee …
            y--;
        }
        if (local < 0.3) {
            return;   // ganz am Rand: nur verbrannte Pflanzen
        }
        BlockState ground = ctx.get(x, y, z);
        // 2) Wasser: flache Tümpel trocknen aus und füllen sich mit Asche
        if (!ground.getFluidState().isEmpty()) {
            if (ground.getFluidState().is(Fluids.WATER) && local > 0.55) {
                int depth = 0;
                while (depth < 5 && !ctx.get(x, y - depth, z).getFluidState().isEmpty()) {
                    depth++;
                }
                if (depth <= 3) {
                    for (int k = 0; k < depth; k++) {
                        ctx.set(x, y - k, z, k == 0 ? ModBlocks.ASH_BLOCK.get().defaultBlockState()
                                : ModBlocks.CINDER_SOIL.get().defaultBlockState());
                    }
                }
            }
            return;
        }
        double n1 = ctx.noise(x, z, 0.035, 73);
        double n2 = ctx.noise(x, z, 0.11, 74);
        if (local < 0.5) {
            // Übergang: der Boden ist nur angesengt
            if (isSoftGround(ground) && ctx.rand(x, z, 77) < (local - 0.3) * 4.0) {
                ctx.set(x, y, z, ModBlocks.CINDER_SOIL.get().defaultBlockState());
            }
            return;
        }
        // 3) Oberfläche: Asche, Aschenerde, Vulkangestein; glühende Risse
        BlockState surface;
        boolean crack = Math.abs(n2 - 0.5) < 0.022 && n1 > 0.38 && local > 0.75;
        if (crack) {
            surface = ctx.rand(x, z, 78) < 0.7 ? ModBlocks.GLOWING_VOLCANIC_ROCK.get().defaultBlockState() : Blocks.MAGMA_BLOCK.defaultBlockState();
        } else if (n1 > 0.7) {
            double r = ctx.rand(x, z, 79);
            surface = r < 0.75 ? ModBlocks.VOLCANIC_ROCK.base().get().defaultBlockState()
                    : r < 0.9 ? Blocks.BASALT.defaultBlockState() : Blocks.BLACKSTONE.defaultBlockState();
        } else if (n1 < 0.3) {
            surface = ModBlocks.CINDER_SOIL.get().defaultBlockState();
        } else {
            surface = ModBlocks.ASH_BLOCK.get().defaultBlockState();
        }
        if (n2 > 0.9 && local > 0.8) {
            surface = Blocks.MAGMA_BLOCK.defaultBlockState();   // heiße Stellen
        }
        ctx.set(x, y, z, surface);
        // darunter: Asche bzw. Aschenerde, dann Vulkangestein (Erze bleiben erhalten)
        for (int k = 1; k <= 5; k++) {
            BlockState below = ctx.get(x, y - k, z);
            if (!isReplaceableGround(below)) {
                break;
            }
            BlockState fill;
            if (k <= 2 && !surface.is(ModBlocks.VOLCANIC_ROCK.base().get()) && !crack) {
                fill = surface.is(ModBlocks.ASH_BLOCK.get()) && k == 1 ? ModBlocks.ASH_BLOCK.get().defaultBlockState()
                        : ModBlocks.CINDER_SOIL.get().defaultBlockState();
            } else {
                fill = ctx.rand(x * 7 + k, z, 80) < 0.8 ? ModBlocks.VOLCANIC_ROCK.base().get().defaultBlockState() : Blocks.TUFF.defaultBlockState();
            }
            ctx.set(x, y - k, z, fill);
        }
        // 4) Aschedünen: Schichten auf der Asche
        if (surface.is(ModBlocks.ASH_BLOCK.get()) || surface.is(ModBlocks.CINDER_SOIL.get())) {
            double dune = ctx.noise(x, z, 0.06, 76) + (ctx.rand(x, z, 81) - 0.5) * 0.08;
            if (dune > 0.5) {
                int layers = Math.min(5, 1 + (int) ((dune - 0.5) * 14));
                if (ctx.get(x, y + 1, z).isAir()) {
                    ctx.set(x, y + 1, z, ModBlocks.ASH_LAYER.get().defaultBlockState().setValue(SnowLayerBlock.LAYERS, layers));
                }
            }
        }
    }

    /** Einzelheiten: verkohlte Baumgerippe, Glutspalten, Basaltsäulen, Feuerlilien, tote Büsche, Knochen. */
    private void decoration(LairContext ctx, int x, int z, double r) {
        int y = ctx.groundY(x, z);
        BlockState ground = ctx.get(x, y, z);
        if (!ground.getFluidState().isEmpty() || !LairContext.isGround(ground)) {
            return;
        }
        if (r < 0.0026) {
            this.charredTree(ctx, x, y + 1, z);
        } else if (r < 0.0038) {
            // Glutspalte mit einem Kranz aus glühendem Gestein
            ctx.set(x, y, z, ModBlocks.EMBER_VENT.get().defaultBlockState());
            ctx.set(x, y + 1, z, Blocks.AIR.defaultBlockState());
            for (Direction dir : Direction.Plane.HORIZONTAL) {
                if (ctx.rand(x + dir.getStepX(), z + dir.getStepZ(), 85) < 0.7) {
                    ctx.set(x + dir.getStepX(), y, z + dir.getStepZ(), ModBlocks.GLOWING_VOLCANIC_ROCK.get().defaultBlockState());
                }
            }
        } else if (r < 0.0056) {
            int height = 2 + (int) (ctx.rand(x, z, 86) * 5);
            for (int k = 1; k <= height; k++) {
                ctx.set(x, y + k, z, Blocks.BASALT.defaultBlockState());
            }
            if (ctx.rand(x, z, 87) < 0.4) {
                ctx.set(x, y + height + 1, z, Blocks.BASALT.defaultBlockState());
            }
        } else if (r < 0.0095) {
            if (ground.is(ModBlocks.CINDER_SOIL.get()) && ctx.get(x, y + 1, z).canBeReplaced()) {
                ctx.set(x, y + 1, z, ModBlocks.FIRE_LILY.get().defaultBlockState());
            }
        } else if (r < 0.0145) {
            if ((ground.is(ModBlocks.CINDER_SOIL.get()) || ground.is(ModBlocks.ASH_BLOCK.get())) && ctx.get(x, y + 1, z).canBeReplaced()) {
                ctx.set(x, y + 1, z, Blocks.DEAD_BUSH.defaultBlockState());
            }
        } else if (r < 0.0152) {
            Direction.Axis axis = ctx.rand(x, z, 88) < 0.5 ? Direction.Axis.X : Direction.Axis.Z;
            ctx.set(x, y + 1, z, Blocks.BONE_BLOCK.defaultBlockState().setValue(RotatedPillarBlock.AXIS, axis));
            int sx = axis == Direction.Axis.X ? 1 : 0;
            int sz = 1 - sx;
            if (ctx.rand(x, z, 89) < 0.6) {
                ctx.set(x + sx, y + 1, z + sz, Blocks.BONE_BLOCK.defaultBlockState().setValue(RotatedPillarBlock.AXIS, axis));
            }
        }
    }

    /** Verkohlter Baum: ein Stamm (3–6 hoch) mit ein, zwei kahlen Ästen. */
    private void charredTree(LairContext ctx, int x, int y, int z) {
        int height = 3 + (int) (ctx.rand(x, z, 91) * 4);
        BlockState vertical = ModBlocks.CHARRED_LOG.get().defaultBlockState();
        for (int k = 0; k < height; k++) {
            ctx.setIfOpen(x, y + k, z, vertical);
        }
        int branches = 1 + (int) (ctx.rand(x, z, 92) * 2.2);
        for (int b = 0; b < branches; b++) {
            Direction dir = Direction.Plane.HORIZONTAL.getRandomDirection(net.minecraft.util.RandomSource.create(ctx.seed + x * 31L + z * 17L + b));
            int by = y + height - 1 - b;
            int length = 1 + (int) (ctx.rand(x + b, z, 93) * 2.5);
            BlockState horizontal = vertical.setValue(RotatedPillarBlock.AXIS, dir.getAxis());
            for (int k = 1; k <= length; k++) {
                int up = k == length && length > 1 ? 1 : 0;
                ctx.setIfOpen(x + dir.getStepX() * k, by + up, z + dir.getStepZ() * k, up == 1 ? vertical : horizontal);
            }
        }
    }

    private static boolean isSoftGround(BlockState state) {
        return state.is(BlockTags.DIRT) || state.is(BlockTags.SAND) || state.is(Blocks.GRAVEL) || state.is(Blocks.GRASS_BLOCK);
    }

    /** Darf beim Umgestalten ersetzt werden (Erde, Sand, Stein, Terrakotta …) – Erze und Bauwerke nicht. */
    private static boolean isReplaceableGround(BlockState state) {
        return isSoftGround(state) || state.is(BlockTags.BASE_STONE_OVERWORLD) || state.is(BlockTags.TERRACOTTA)
                || state.is(Blocks.SANDSTONE) || state.is(Blocks.RED_SANDSTONE) || state.is(Blocks.CLAY)
                || state.is(Blocks.SNOW_BLOCK) || state.is(Blocks.PACKED_ICE) || state.is(Blocks.ICE);
    }
}
