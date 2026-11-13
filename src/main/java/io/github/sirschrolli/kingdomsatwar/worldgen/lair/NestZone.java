package io.github.sirschrolli.kingdomsatwar.worldgen.lair;

import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Grundgerüst der Nest-Biome: Rund um das eigentliche Nest wird die Landschaft Säule für Säule umgestaltet – mit einer
 * Stärke, die zum Rand des Bioms hin fleckig abnimmt, damit der Übergang natürlich wirkt. Die einzelnen Biome legen
 * fest, was aus Pflanzen, Wasser, Boden und Gestein wird und welche Einzelheiten sie verstreuen.
 *
 * <p>Alles ergibt sich aus Lage und Startwert des Nests, damit die Chunks nahtlos zusammenpassen.
 */
abstract class NestZone {
    protected final LairDesign design;
    /** Bis hierher gestaltet das Nest selbst. */
    protected final int innerRadius;

    NestZone(LairDesign design, int innerRadius) {
        this.design = design;
        this.innerRadius = innerRadius;
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
        // Einzelheiten auch knapp außerhalb des Chunks prüfen, damit Formen über die Chunkgrenze hinweg passen
        int margin = this.decorationMargin();
        for (int x = ctx.box.minX() - margin; x <= ctx.box.maxX() + margin; x++) {
            for (int z = ctx.box.minZ() - margin; z <= ctx.box.maxZ() + margin; z++) {
                double r = ctx.rand(x, z, 75);
                if (r < this.decorationChance()) {
                    double local = this.strength(ctx, x, z, cx, cz);
                    if (local > this.decorationMinStrength()) {
                        this.decoration(ctx, x, z, r / this.decorationChance(), local);
                    }
                }
            }
        }
    }

    /** Wie stark die Umgestaltung hier ist (0 im Nest und außerhalb des Bioms, sonst zum Rand hin fleckig abnehmend). */
    protected double strength(LairContext ctx, int x, int z, int cx, int cz) {
        double dx = x - cx;
        double dz = z - cz;
        double d = Math.sqrt(dx * dx + dz * dz);
        if (d < this.innerRadius) {
            return 0;
        }
        double edge = this.design.zoneEdge(ctx, Math.atan2(dz, dx));
        if (d >= edge) {
            return 0;
        }
        double t = Mth.clamp((edge - d) / 30.0, 0, 1);
        return t + (ctx.noise(x, z, 0.045, 72) - 0.5) * 0.6;
    }

    protected abstract void column(LairContext ctx, int x, int z, double local);

    /** Einzelheit an dieser Stelle; {@code r} ist gleichverteilt in 0..1. */
    protected abstract void decoration(LairContext ctx, int x, int z, double r, double local);

    protected double decorationChance() {
        return 0.02;
    }

    protected double decorationMinStrength() {
        return 0.6;
    }

    protected int decorationMargin() {
        return 3;
    }

    // =====================================================================================
    // Hilfen
    // =====================================================================================

    /**
     * Wie tief die Oberfläche umgestaltet wird: mindestens 5 Blöcke, an Steilhängen bis hinunter zum tiefsten
     * Nachbarn, damit an Klippen und Schluchtwänden nichts Fremdes hervorschaut.
     */
    protected static int reshapeDepth(LairContext ctx, int x, int y, int z) {
        int floor = Math.min(Math.min(ctx.groundY(x + 1, z), ctx.groundY(x - 1, z)), Math.min(ctx.groundY(x, z + 1), ctx.groundY(x, z - 1)));
        return Mth.clamp(y - floor + 1, 5, 40);
    }

    /** Füllt unter der Oberfläche bis {@code depth} tief (Erze, Bauwerke und Höhlen bleiben), danach freiliegende Wände. */
    protected void fillBelow(LairContext ctx, int x, int y, int z, int depth, Layer layer) {
        int gaps = 0;
        boolean inGap = false;
        for (int k = 1; k <= depth; k++) {
            BlockState below = ctx.get(x, y - k, z);
            if (!isReplaceableGround(below)) {
                if (!below.getFluidState().isEmpty()) {
                    break;
                }
                if (below.isAir()) {
                    if (!inGap && ++gaps > 2) {
                        break;
                    }
                    inGap = true;
                }
                continue;
            }
            inGap = false;
            ctx.set(x, y - k, z, layer.at(k, y - k));
        }
        for (int k = depth + 1; k <= 40; k++) {
            int yy = y - k;
            BlockState below = ctx.get(x, yy, z);
            if (isReplaceableGround(below) && exposed(ctx, x, yy, z)) {
                ctx.set(x, yy, z, layer.at(Math.max(k, 3), yy));
            }
        }
    }

    /** Was in Tiefe {@code k} (1 = direkt unter der Oberfläche) auf Höhe {@code y} liegt. */
    @FunctionalInterface
    protected interface Layer {
        BlockState at(int k, int y);
    }

    protected static boolean exposed(LairContext ctx, int x, int y, int z) {
        return ctx.get(x + 1, y, z).isAir() || ctx.get(x - 1, y, z).isAir() || ctx.get(x, y, z + 1).isAir()
                || ctx.get(x, y, z - 1).isAir() || ctx.get(x, y - 1, z).isAir() || ctx.get(x, y + 1, z).isAir();
    }

    protected static boolean isSoftGround(BlockState state) {
        return state.is(BlockTags.DIRT) || state.is(BlockTags.SAND) || state.is(Blocks.GRAVEL) || state.is(Blocks.GRASS_BLOCK);
    }

    /** Darf beim Umgestalten ersetzt werden (Erde, Sand, Stein, Terrakotta, Schnee …) – Erze und Bauwerke nicht. */
    protected static boolean isReplaceableGround(BlockState state) {
        return isSoftGround(state) || state.is(BlockTags.BASE_STONE_OVERWORLD) || state.is(BlockTags.TERRACOTTA)
                || state.is(Blocks.SANDSTONE) || state.is(Blocks.RED_SANDSTONE) || state.is(Blocks.SMOOTH_SANDSTONE)
                || state.is(Blocks.SMOOTH_RED_SANDSTONE) || state.is(Blocks.CLAY) || state.is(Blocks.MUD)
                || state.is(Blocks.SNOW_BLOCK) || state.is(Blocks.PACKED_ICE) || state.is(Blocks.ICE) || state.is(Blocks.BLUE_ICE)
                || state.is(Blocks.CALCITE) || state.is(Blocks.MOSS_BLOCK) || state.is(Blocks.POWDER_SNOW);
    }

    /** Oberster Block der Säule nach dem Entfernen der Pflanzen (oder Flüssigkeit). */
    protected static int surfaceAfterPlants(LairContext ctx, int x, int z, PlantRule rule) {
        int y = ctx.originalHeight(x, z, false) - 1;
        int min = ctx.level.getMinY() + 1;
        while (y > min) {
            BlockState state = ctx.get(x, y, z);
            if (state.isAir()) {
                y--;
                continue;
            }
            if (!state.getFluidState().isEmpty() && !state.is(BlockTags.LEAVES)) {
                break;
            }
            if (LairContext.isGround(state)) {
                break;
            }
            BlockState replacement = rule.replace(state, x, y, z);
            if (replacement != state) {
                ctx.set(x, y, z, replacement);
            }
            y--;
        }
        return y;
    }

    /** Was aus Stämmen, Laub und Pflanzen wird. */
    @FunctionalInterface
    protected interface PlantRule {
        BlockState replace(BlockState state, int x, int y, int z);
    }
}
