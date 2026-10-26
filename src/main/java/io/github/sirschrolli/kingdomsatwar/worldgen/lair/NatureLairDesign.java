package io.github.sirschrolli.kingdomsatwar.worldgen.lair;

import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import io.github.sirschrolli.kingdomsatwar.loot.ModLootTables;
import io.github.sirschrolli.kingdomsatwar.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.entity.BrushableBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;

/**
 * Verdantis' Uralter Hain: eine runde Lichtung (gut 55 Blöcke weit) inmitten riesiger, uralter Bäume. In der Mitte
 * liegt der überwucherte Hügel, unter dem die Hüterin schläft – Moos, Wurzelerde und Blumen, von Wurzelranken
 * umschlungen. Rundherum ein Kreis bemooster Menhire und die leuchtenden Lebensblüten, aus denen Verdantis im Kampf
 * Kraft schöpft. Am Rand der Lichtung steht der Mutterbaum mit einem Stamm von neun Blöcken und einer Krone, die
 * weit über die Lichtung ragt (mit einer Höhlung samt Fundstellen), dazu ein Weiher mit Seerosen und ein Ring weiterer
 * Hainbäume, deren Wurzeln sich in Bögen über den Boden winden. Ringsum liegt der Uralte Hain ({@link GroveZone}).
 */
public final class NatureLairDesign implements LairDesign {
    private static final int R = 50;
    private static final int TOP = 48;
    /** Halbmesser des Hügels über dem Schlafplatz. */
    public static final double MOUND_RADIUS = 8.5;
    public static final double MOUND_HEIGHT = 6.0;
    /** Halbmesser des Kreises der Lebensblüten. */
    public static final double BLOSSOM_RING = 20.0;
    private final GroveZone zone = new GroveZone(this, R);

    @Override
    public int radius() {
        return R;
    }

    @Override
    public int zoneRadius() {
        return 112;
    }

    @Override
    public void buildZone(LairContext ctx) {
        this.zone.build(ctx);
    }

    @Override
    public int depthBelow() {
        return 12;
    }

    @Override
    public int heightAbove() {
        return TOP;
    }

    @Override
    public int floorHeight(Structure.GenerationContext context, int x, int z) {
        int avg = DragonLairStructure.averageHeight(context, x, z, 30, Heightmap.Types.WORLD_SURFACE_WG);
        return Mth.clamp(avg, context.chunkGenerator().getSeaLevel() + 2, 200);
    }

    private static double gladeRadius(LairContext ctx, double angle) {
        return 27.0 + (ctx.angleNoise(angle, 2.3, 311) - 0.5) * 7.0;
    }

    private static double motherAngle(LairContext ctx) {
        return ctx.rand(0, 0, 399) * Mth.TWO_PI;
    }

    private static double pondAngle(LairContext ctx) {
        return motherAngle(ctx) + Math.PI + (ctx.rand(0, 1, 399) - 0.5) * 1.2;
    }

    /** Die neun Plätze der Lebensblüten im Kreis um den Hügel (Höhe = Nestboden); auch Verdantis lässt sie dort sprießen. */
    public static java.util.List<BlockPos> blossomRing(BlockPos center) {
        java.util.List<BlockPos> spots = new java.util.ArrayList<>();
        for (int k = 0; k < 9; k++) {
            double a = k * Mth.TWO_PI / 9 + 0.2;
            spots.add(center.offset((int) Math.round(Math.cos(a) * BLOSSOM_RING), 0, (int) Math.round(Math.sin(a) * BLOSSOM_RING)));
        }
        return spots;
    }

    @Override
    public void build(LairContext ctx) {
        int cx = ctx.center.getX();
        int cy = ctx.center.getY();
        int cz = ctx.center.getZ();
        for (int x = Math.max(ctx.box.minX(), cx - R); x <= Math.min(ctx.box.maxX(), cx + R); x++) {
            for (int z = Math.max(ctx.box.minZ(), cz - R); z <= Math.min(ctx.box.maxZ(), cz + R); z++) {
                double dx = x - cx;
                double dz = z - cz;
                double d = Math.sqrt(dx * dx + dz * dz);
                if (d <= R) {
                    this.column(ctx, x, z, d, Math.atan2(dz, dx), cy);
                }
            }
        }
        this.features(ctx, cx, cy, cz);
    }

    // ------------------------------------------------------------------ Gelände

    private void column(LairContext ctx, int x, int z, double d, double angle, int cy) {
        int top0 = ctx.originalHeight(x, z, false) - 1;
        int h0 = ctx.groundY(x, z);
        double glade = gladeRadius(ctx, angle);
        double n = ctx.noise(x, z, 0.09, 321);
        int top = cy + TOP;
        int hs;
        boolean clearAll = d < R - 8;
        if (d < glade) {
            double rimRise = d > glade - 7 ? (d - (glade - 7)) / 7.0 * 1.5 : 0.0;
            hs = cy - 1 + (int) Math.round(rimRise + (d > 12 ? (n - 0.5) * 1.2 : 0.0));
        } else {
            double u = Mth.clamp((d - glade) / (R - glade), 0, 1);
            double target = Mth.lerp(LairContext.smooth(u), cy + 0.5, h0) + (n - 0.5) * 2.0;
            hs = (int) Math.round(target);
        }
        int bottom = Math.min(h0, hs) - 6;
        for (int y = bottom; y < hs; y++) {
            int depth = hs - y;
            ctx.set(x, y, z, depth <= 3 ? (depth == 1 && ctx.rand(x, z, 322) < 0.3 ? Blocks.ROOTED_DIRT : Blocks.DIRT).defaultBlockState()
                    : Blocks.STONE.defaultBlockState());
        }
        BlockState surface = d < glade ? this.gladeFloor(ctx, x, z, d) : this.forestFloor(ctx, x, z);
        ctx.set(x, hs, z, surface);
        if (clearAll || hs > h0) {
            int clearTo = clearAll ? Math.max(top, top0 + 1) : Math.max(hs + 2, Math.min(top0 + 1, hs + 3));
            ctx.fill(x, hs + 1, clearTo, z, Blocks.AIR.defaultBlockState());
        }
        if (clearAll && ctx.get(x, hs + 1, z).isAir()) {
            this.floorPlant(ctx, x, hs + 1, z, surface, d, d < glade);
        }
    }

    private BlockState gladeFloor(LairContext ctx, int x, int z, double d) {
        double n = ctx.noise(x, z, 0.08, 323);
        double m = ctx.noise(x, z, 0.16, 324);
        if (d < MOUND_RADIUS + 2.5) {
            return n > 0.55 ? Blocks.MOSS_BLOCK.defaultBlockState() : Blocks.GRASS_BLOCK.defaultBlockState();
        }
        if (n > 0.66) {
            return Blocks.MOSS_BLOCK.defaultBlockState();
        }
        if (m > 0.8 && d > 18) {
            return Blocks.PODZOL.defaultBlockState();
        }
        return Blocks.GRASS_BLOCK.defaultBlockState();
    }

    private BlockState forestFloor(LairContext ctx, int x, int z) {
        double n = ctx.noise(x, z, 0.07, 325);
        double r = ctx.rand(x, z, 326);
        if (n > 0.6) {
            return Blocks.MOSS_BLOCK.defaultBlockState();
        }
        if (n < 0.25) {
            return r < 0.5 ? Blocks.PODZOL.defaultBlockState() : Blocks.ROOTED_DIRT.defaultBlockState();
        }
        return r < 0.12 ? Blocks.COARSE_DIRT.defaultBlockState() : Blocks.GRASS_BLOCK.defaultBlockState();
    }

    /** Pflanzen auf der Lichtung (niedrig, die Kampffläche bleibt frei) und im Unterholz (üppig). */
    private void floorPlant(LairContext ctx, int x, int y, int z, BlockState surface, double d, boolean glade) {
        double r = ctx.rand(x, z, 327);
        boolean moss = surface.is(Blocks.MOSS_BLOCK);
        BlockState plant = null;
        if (glade) {
            if (d < MOUND_RADIUS + 1) {
                return;
            }
            if (moss) {
                plant = r < 0.4 ? Blocks.MOSS_CARPET.defaultBlockState() : null;
            } else if (r < 0.22) {
                plant = Blocks.SHORT_GRASS.defaultBlockState();
            } else if (r < 0.27) {
                plant = ModBlocks.GROVE_BELL.get().defaultBlockState();
            } else if (r < 0.31) {
                plant = switch ((int) (ctx.rand(x, z, 328) * 5)) {
                    case 0 -> Blocks.ALLIUM.defaultBlockState();
                    case 1 -> Blocks.AZURE_BLUET.defaultBlockState();
                    case 2 -> Blocks.OXEYE_DAISY.defaultBlockState();
                    case 3 -> Blocks.CORNFLOWER.defaultBlockState();
                    default -> Blocks.LILY_OF_THE_VALLEY.defaultBlockState();
                };
            } else if (r < 0.34) {
                plant = Blocks.FERN.defaultBlockState();
            }
        } else {
            if (moss) {
                plant = r < 0.4 ? Blocks.MOSS_CARPET.defaultBlockState() : r < 0.47 ? ModBlocks.GLOWCAP.get().defaultBlockState()
                        : r < 0.6 ? Blocks.FERN.defaultBlockState() : null;
            } else if (r < 0.25) {
                plant = Blocks.FERN.defaultBlockState();
            } else if (r < 0.33) {
                if (ctx.get(x, y + 1, z).isAir()) {
                    ctx.set(x, y, z, Blocks.LARGE_FERN.defaultBlockState().setValue(DoublePlantBlock.HALF, DoubleBlockHalf.LOWER));
                    ctx.set(x, y + 1, z, Blocks.LARGE_FERN.defaultBlockState().setValue(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER));
                }
                return;
            } else if (r < 0.45) {
                plant = Blocks.SHORT_GRASS.defaultBlockState();
            } else if (r < 0.48) {
                plant = ModBlocks.BRAMBLE.get().defaultBlockState();
            } else if (r < 0.52) {
                plant = ModBlocks.GROVE_BELL.get().defaultBlockState();
            } else if (r < 0.535) {
                plant = Blocks.AZALEA.defaultBlockState();
            }
        }
        if (plant != null) {
            ctx.set(x, y, z, plant);
        }
    }

    // ------------------------------------------------------------------ Einzelheiten

    private void features(LairContext ctx, int cx, int cy, int cz) {
        double mother = motherAngle(ctx);
        double pond = pondAngle(ctx);
        // der Weiher
        this.pond(ctx, cx + (int) Math.round(Math.cos(pond) * 21), cy, cz + (int) Math.round(Math.sin(pond) * 21));
        // Ring der Hainbäume (zwei Lücken als Wege)
        int trees = 8;
        for (int k = 0; k < trees; k++) {
            double a = mother + (k + 0.5) * Mth.TWO_PI / (trees + 1) + (ctx.rand(k, 1, 330) - 0.5) * 0.3;
            if (k == 2 || k == 6) {
                continue;
            }
            double dist = 36 + ctx.rand(k, 2, 330) * 8;
            int tx = cx + (int) Math.round(Math.cos(a) * dist);
            int tz = cz + (int) Math.round(Math.sin(a) * dist);
            int ty = cy;
            int thick = ctx.rand(k, 3, 330) < 0.5 ? 3 : 2;
            GroveZone.groveTree(ctx, tx, ty + 1, tz, 18 + (int) (ctx.rand(k, 4, 330) * 9), thick, 6.5 + ctx.rand(k, 5, 330) * 2.0, 14);
            if (k % 3 == 0) {
                // ein Wurzelbogen vom Baum zur Lichtung
                double back = a + Math.PI;
                this.rootArch(ctx, tx, ty, tz, (int) Math.round(tx + Math.cos(back) * 11), cy, (int) Math.round(tz + Math.sin(back) * 11));
            }
        }
        // der Mutterbaum am Rand der Lichtung
        int mx = cx + (int) Math.round(Math.cos(mother) * 34);
        int mz = cz + (int) Math.round(Math.sin(mother) * 34);
        this.motherTree(ctx, mx, cy, mz, mother + Math.PI);
        // Menhirkreis um den Hügel
        for (int k = 0; k < 7; k++) {
            double a = k * Mth.TWO_PI / 7 + 0.45;
            int sx = cx + (int) Math.round(Math.cos(a) * 15);
            int sz = cz + (int) Math.round(Math.sin(a) * 15);
            this.menhir(ctx, sx, cy, sz, k);
        }
        // Lebensblüten (nicht im Weiher)
        int pondX = cx + (int) Math.round(Math.cos(pond) * 21);
        int pondZ = cz + (int) Math.round(Math.sin(pond) * 21);
        for (BlockPos spot : blossomRing(ctx.center)) {
            if (Math.hypot(spot.getX() - pondX, spot.getZ() - pondZ) > 7.5) {
                blossomBed(ctx, spot.getX(), cy, spot.getZ());
            }
        }
        // der überwucherte Hügel, unter dem Verdantis schläft
        mound(ctx, cx, cy, cz);
        // Nestkern
        BlockPos core = this.corePos(ctx.center);
        if (ctx.inside(core.getX(), core.getY(), core.getZ())) {
            ctx.set(core.getX(), core.getY(), core.getZ(), ModBlocks.DRAGON_LAIR_CORE.get().defaultBlockState());
            if (ctx.level.getBlockEntity(core) instanceof io.github.sirschrolli.kingdomsatwar.block.entity.DragonLairCoreBlockEntity be) {
                be.setup(DragonType.NATURE, ctx.center);
            }
        }
    }

    /**
     * Der überwucherte Hügel: eine Kuppel aus Wurzelerde, Erde und Moos (innen hohl), obenauf Gras, Farne und
     * Blumen; knorrige Wurzelranken umschlingen ihn vom Boden bis zum Scheitel.
     */
    public static void mound(LairContext ctx, int cx, int cy, int cz) {
        int reach = (int) Math.ceil(MOUND_RADIUS) + 1;
        for (int dx = -reach; dx <= reach; dx++) {
            for (int dz = -reach; dz <= reach; dz++) {
                double r = Math.sqrt(dx * dx + dz * dz);
                if (r > MOUND_RADIUS) {
                    continue;
                }
                int x = cx + dx;
                int z = cz + dz;
                double hTop = MOUND_HEIGHT * Math.sqrt(1.0 - (r / MOUND_RADIUS) * (r / MOUND_RADIUS)) + (ctx.noise(x, z, 0.3, 331) - 0.5);
                int top = cy - 1 + (int) Math.round(hTop);
                double inner = MOUND_RADIUS - 2.2;
                double hIn = r < inner ? (MOUND_HEIGHT - 2.0) * Math.sqrt(1.0 - (r / inner) * (r / inner)) : -1;
                for (int y = cy; y <= top; y++) {
                    if (y - cy < hIn) {
                        ctx.set(x, y, z, Blocks.AIR.defaultBlockState());
                        continue;
                    }
                    BlockState state;
                    if (y == top) {
                        state = ctx.rand(x, z, 332) < 0.6 ? Blocks.GRASS_BLOCK.defaultBlockState() : Blocks.MOSS_BLOCK.defaultBlockState();
                    } else {
                        double q = ctx.rand(x, z + y * 17, 333);
                        state = q < 0.45 ? Blocks.ROOTED_DIRT.defaultBlockState() : q < 0.8 ? Blocks.DIRT.defaultBlockState()
                                : Blocks.MOSS_BLOCK.defaultBlockState();
                    }
                    ctx.set(x, y, z, state);
                }
                // Bewuchs obenauf
                double p = ctx.rand(x, z, 334);
                BlockState plant = p < 0.3 ? Blocks.SHORT_GRASS.defaultBlockState() : p < 0.42 ? Blocks.FERN.defaultBlockState()
                        : p < 0.5 ? ModBlocks.GROVE_BELL.get().defaultBlockState() : p < 0.55 ? Blocks.MOSS_CARPET.defaultBlockState()
                        : p < 0.58 ? ModBlocks.GLOWCAP.get().defaultBlockState() : null;
                if (plant != null && top >= cy) {
                    ctx.set(x, top + 1, z, plant);
                }
            }
        }
        // ein Azaleenbusch auf dem Scheitel
        ctx.set(cx, cy + (int) MOUND_HEIGHT, cz, Blocks.FLOWERING_AZALEA.defaultBlockState());
        // Wurzelranken vom Boden über den Hügel
        for (int k = 0; k < 6; k++) {
            double a = k * Mth.TWO_PI / 6 + ctx.rand(k, 0, 335) * 0.6;
            for (int s = 0; s <= 12; s++) {
                double r = MOUND_RADIUS + 1.0 - s * (MOUND_RADIUS + 1.0) / 12.0;
                double aa = a + s * 0.06;
                int x = cx + (int) Math.round(Math.cos(aa) * r);
                int z = cz + (int) Math.round(Math.sin(aa) * r);
                double hTop = r >= MOUND_RADIUS ? 0 : MOUND_HEIGHT * Math.sqrt(1.0 - (r / MOUND_RADIUS) * (r / MOUND_RADIUS));
                int y = cy - 1 + (int) Math.round(hTop);
                ctx.set(x, y + 1, z, ModBlocks.GNARLED_ROOTS.get().defaultBlockState());
            }
        }
    }

    /** Ein bemooster Menhir (manche geneigt oder abgebrochen), oben Moos und Leuchtflechten. */
    private void menhir(LairContext ctx, int x, int cy, int z, int k) {
        int height = 3 + (int) (ctx.rand(k, 10, 340) * 3);
        boolean broken = ctx.rand(k, 11, 340) < 0.25;
        int lean = ctx.rand(k, 12, 340) < 0.4 ? 1 : 0;
        int dirX = ctx.rand(k, 13, 340) < 0.5 ? 1 : -1;
        if (broken) {
            height = 2;
        }
        for (int y = 0; y < height; y++) {
            int ox = y >= height - 1 ? lean * dirX : 0;
            BlockState state = ctx.rand(x, z + y, 341) < 0.55 ? Blocks.MOSSY_STONE_BRICKS.defaultBlockState()
                    : ctx.rand(x, z + y, 342) < 0.5 ? Blocks.MOSSY_COBBLESTONE.defaultBlockState() : Blocks.CRACKED_STONE_BRICKS.defaultBlockState();
            ctx.set(x + ox, cy + y, z, state);
            if (y < 2) {
                ctx.set(x + 1, cy + y, z, Blocks.MOSSY_STONE_BRICKS.defaultBlockState());
            }
        }
        ctx.set(x + (lean * dirX), cy + height, z, Blocks.MOSS_CARPET.defaultBlockState());
        if (broken) {
            ctx.set(x - dirX * 2, cy, z + 1, Blocks.MOSSY_STONE_BRICK_SLAB.defaultBlockState());
            ctx.set(x - dirX * 3, cy, z + 1, Blocks.MOSSY_COBBLESTONE.defaultBlockState());
        }
        ctx.set(x, cy + 1, z - 1, Blocks.GLOW_LICHEN.defaultBlockState()
                .setValue(net.minecraft.world.level.block.MultifaceBlock.getFaceProperty(Direction.SOUTH), true));
    }

    /** Beet mit einer Lebensblüte: ein Kreis aus Moos, die Blüte in der Mitte, Leuchtkappen daneben. */
    public static void blossomBed(LairContext ctx, int x, int cy, int z) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                ctx.set(x + dx, cy - 1, z + dz, Blocks.MOSS_BLOCK.defaultBlockState());
                ctx.set(x + dx, cy, z + dz, Blocks.AIR.defaultBlockState());
            }
        }
        ctx.set(x, cy, z, ModBlocks.LIFE_BLOSSOM.get().defaultBlockState());
        ctx.set(x + 1, cy, z + 1, ModBlocks.GLOWCAP.get().defaultBlockState());
        ctx.set(x - 1, cy, z, Blocks.MOSS_CARPET.defaultBlockState());
    }

    /** Weiher mit Seerosen, Lehmgrund und Farnen am Ufer. */
    private void pond(LairContext ctx, int px, int cy, int pz) {
        double radius = 4.6;
        int reach = (int) Math.ceil(radius) + 2;
        for (int dx = -reach; dx <= reach; dx++) {
            for (int dz = -reach; dz <= reach; dz++) {
                double r = Math.sqrt(dx * dx + dz * dz) + (ctx.noise(px + dx, pz + dz, 0.3, 350) - 0.5) * 1.2;
                int x = px + dx;
                int z = pz + dz;
                if (r < radius) {
                    int depth = r < radius - 2 ? 3 : r < radius - 1 ? 2 : 1;
                    ctx.set(x, cy - 1 - depth, z, ctx.rand(x, z, 351) < 0.5 ? Blocks.CLAY.defaultBlockState() : Blocks.MUD.defaultBlockState());
                    for (int k = 0; k < depth; k++) {
                        ctx.set(x, cy - 1 - k, z, Blocks.WATER.defaultBlockState());
                    }
                    ctx.set(x, cy, z, ctx.rand(x, z, 352) < 0.22 ? Blocks.LILY_PAD.defaultBlockState() : Blocks.AIR.defaultBlockState());
                } else if (r < radius + 1.4) {
                    ctx.set(x, cy - 1, z, Blocks.MOSS_BLOCK.defaultBlockState());
                    double q = ctx.rand(x, z, 353);
                    ctx.set(x, cy, z, q < 0.3 ? Blocks.FERN.defaultBlockState() : q < 0.4 ? Blocks.SUGAR_CANE.defaultBlockState()
                            : q < 0.6 ? Blocks.MOSS_CARPET.defaultBlockState() : Blocks.AIR.defaultBlockState());
                }
            }
        }
    }

    /**
     * Der Mutterbaum: Stamm von neun Blöcken (rund), Wurzelfüße bis weit in die Lichtung, eine Höhlung zur Lichtung
     * hin mit Fundstellen im Kies, mächtige Äste und eine Krone, die über die Lichtung ragt.
     */
    private void motherTree(LairContext ctx, int x, int cy, int z, double facing) {
        BlockState log = ModBlocks.ANCIENT_LOG.get().defaultBlockState();
        double radius = 4.3;
        int height = 32;
        int reach = 7;
        double hx = Math.cos(facing);
        double hz = Math.sin(facing);
        for (int dx = -reach; dx <= reach; dx++) {
            for (int dz = -reach; dz <= reach; dz++) {
                double r = Math.sqrt(dx * dx + dz * dz);
                for (int k = -12; k < height; k++) {
                    double flare = k < 4 ? Math.min(3.0, (4 - k) * 0.55) : 0.0;
                    double rr = radius + flare - (k > height - 8 ? (k - (height - 8)) * 0.3 : 0.0)
                            + (ctx.noise(x + dx * 7 + k, z + dz * 7, 0.4, 360) - 0.5) * 0.8;
                    if (r > rr) {
                        continue;
                    }
                    // Höhlung zur Lichtung hin
                    double along = dx * hx + dz * hz;
                    double side = Math.abs(-dx * hz + dz * hx);
                    boolean hollow = k >= 0 && k < 6 && along > -1.5 && side < 2.2 - k * 0.15 && r < rr - 0.8;
                    if (hollow) {
                        ctx.set(x + dx, cy + k, z + dz, Blocks.AIR.defaultBlockState());
                        continue;
                    }
                    ctx.set(x + dx, cy + k, z + dz, log);
                }
            }
        }
        // Boden der Höhlung: Wurzelerde, Moos und Fundstellen
        for (int s = 0; s <= 3; s++) {
            int fx = x + (int) Math.round(hx * s);
            int fz = z + (int) Math.round(hz * s);
            ctx.set(fx, cy - 1, fz, Blocks.ROOTED_DIRT.defaultBlockState());
            ctx.set(fx, cy, fz, s == 1 ? Blocks.MOSS_CARPET.defaultBlockState() : Blocks.AIR.defaultBlockState());
        }
        this.suspicious(ctx, x, cy - 1, z, 3000L);
        this.suspicious(ctx, x + (int) Math.round(-hz * 1.5), cy - 1, z + (int) Math.round(hx * 1.5), 3001L);
        ctx.set(x + (int) Math.round(hx), cy, z + (int) Math.round(hz), ModBlocks.GLOWCAP.get().defaultBlockState());
        // Wurzeln, die sich in die Lichtung winden
        for (int i = 0; i < 6; i++) {
            double a = facing + (i - 2.5) * 0.42;
            int len = 9 + (int) (ctx.rand(i, 0, 361) * 7);
            for (int s = 3; s <= len; s++) {
                int rx = x + (int) Math.round(Math.cos(a) * s);
                int rz = z + (int) Math.round(Math.sin(a) * s);
                int h = s < 7 ? 1 : 0;
                BlockState root = s > len - 3 ? ModBlocks.GNARLED_ROOTS.get().defaultBlockState()
                        : log.setValue(RotatedPillarBlock.AXIS, Math.abs(Math.cos(a)) > Math.abs(Math.sin(a)) ? Direction.Axis.X : Direction.Axis.Z);
                for (int k = 0; k <= h; k++) {
                    ctx.set(rx, cy + k, rz, root);
                }
            }
        }
        // Äste und Krone
        int crownY = cy + height;
        GroveZone.blob(ctx, x, crownY, z, 12.0, 6.5);
        for (int i = 0; i < 5; i++) {
            double a = facing + (i - 2) * 0.9 + (ctx.rand(i, 1, 362) - 0.5) * 0.3;
            double len = 10 + ctx.rand(i, 2, 362) * 5;
            int startY = cy + height - 10 + (int) (ctx.rand(i, 3, 362) * 4);
            double endX = x + Math.cos(a) * len;
            double endZ = z + Math.sin(a) * len;
            double endY = startY + 4 + ctx.rand(i, 4, 362) * 3;
            for (int s = 0; s <= (int) len; s++) {
                double t = s / len;
                int bx = (int) Math.round(x + (endX - x) * t);
                int bz = (int) Math.round(z + (endZ - z) * t);
                int by = (int) Math.round(startY + (endY - startY) * t);
                BlockState branch = log.setValue(RotatedPillarBlock.AXIS, Math.abs(Math.cos(a)) > Math.abs(Math.sin(a)) ? Direction.Axis.X : Direction.Axis.Z);
                ctx.set(bx, by, bz, branch);
                if (s < len * 0.5) {
                    ctx.set(bx, by + 1, bz, branch);
                }
            }
            GroveZone.blob(ctx, endX, endY + 1, endZ, 6.5, 3.8);
        }
        // Höhlenranken mit Leuchtbeeren unter der Krone
        for (int dx = -16; dx <= 16; dx++) {
            for (int dz = -16; dz <= 16; dz++) {
                if (dx * dx + dz * dz > 15 * 15 || ctx.rand(x + dx, z + dz, 363) > 0.06) {
                    continue;
                }
                GroveZone.hangingVines(ctx, x + dx, crownY + 2, z + dz, 3 + (int) (ctx.rand(x + dx, z + dz, 364) * 9));
            }
        }
    }

    /** Ein Bogen aus knorrigen Wurzeln von (x0, y0, z0) zu (x1, y1, z1), in der Mitte gut fünf Blöcke hoch. */
    private void rootArch(LairContext ctx, int x0, int y0, int z0, int x1, int y1, int z1) {
        int steps = 40;
        for (int s = 0; s <= steps; s++) {
            double t = (double) s / steps;
            double x = Mth.lerp(t, x0, x1);
            double z = Mth.lerp(t, z0, z1);
            double y = Mth.lerp(t, y0, y1) + Math.sin(t * Math.PI) * 5.5;
            int bx = (int) Math.round(x);
            int by = (int) Math.round(y);
            int bz = (int) Math.round(z);
            BlockState state = t < 0.25 ? ModBlocks.ANCIENT_LOG.get().defaultBlockState() : ModBlocks.GNARLED_ROOTS.get().defaultBlockState();
            ctx.set(bx, by, bz, state);
            if (t < 0.5) {
                ctx.set(bx, by - 1, bz, state);
            }
        }
    }

    private void suspicious(LairContext ctx, int x, int y, int z, long salt) {
        if (!ctx.inside(x, y, z)) {
            return;
        }
        BlockPos pos = new BlockPos(x, y, z);
        ctx.set(x, y, z, Blocks.SUSPICIOUS_GRAVEL.defaultBlockState());
        if (ctx.level.getBlockEntity(pos) instanceof BrushableBlockEntity brushable) {
            brushable.setLootTable(ModLootTables.DRAGON_NEST_ARCHAEOLOGY.get(DragonType.NATURE), ctx.seed + salt);
        }
    }
}
