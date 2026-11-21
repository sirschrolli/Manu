package io.github.sirschrolli.kingdomsatwar.worldgen.lair;

import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import io.github.sirschrolli.kingdomsatwar.loot.ModLootTables;
import io.github.sirschrolli.kingdomsatwar.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.RailBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.entity.BrushableBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;

/**
 * Terragons Felsschlucht: ein tief eingesunkener Kessel (gut 55 Blöcke weit) zwischen steilen, geschichteten Wänden aus
 * Schiefer, Stein und Terrakotta mit Erzadern und Bernsteinkristallen. Eine schmale Schlucht führt hinein; an ihrem
 * Eingang liegen die Reste eines verlassenen Bergwerks (Stollen mit Stützbalken, Schienen, Laternen, Fässer und
 * Fundstellen). Auf dem Kesselboden aus Kies, Geröll und Stein laufen glimmende Risse sternförmig auf den Felshügel in
 * der Mitte zu, unter dem der Bergbrecher schläft; Felsnadeln und ein Felsbogen ragen auf. Ringsum liegen die
 * Zerklüfteten Weiten ({@link RuggedZone}).
 */
public final class EarthLairDesign implements LairDesign {
    private static final int R = 48;
    private static final int TOP = 34;
    public static final double MOUND_RADIUS = 6.5;
    private final RuggedZone zone = new RuggedZone(this, R);

    @Override
    public int radius() {
        return R;
    }

    @Override
    public int zoneRadius() {
        return 114;
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
        int avg = DragonLairStructure.averageHeight(context, x, z, 30, Heightmap.Types.WORLD_SURFACE_WG);
        return Mth.clamp(avg - 9, context.chunkGenerator().getSeaLevel() + 1, 190);
    }

    private static double floorRadius(LairContext ctx, double angle) {
        return 25.0 + (ctx.angleNoise(angle, 2.6, 511) - 0.5) * 7.0;
    }

    private static double wallHeight(LairContext ctx, double angle) {
        return 19.0 + (ctx.angleNoise(angle, 3.4, 512) - 0.5) * 10.0;
    }

    private static double riftAngle(LairContext ctx) {
        return ctx.rand(0, 0, 599) * Mth.TWO_PI;
    }

    @Override
    public void build(LairContext ctx) {
        int cx = ctx.center.getX();
        int cy = ctx.center.getY();
        int cz = ctx.center.getZ();
        double rift = riftAngle(ctx);
        for (int x = Math.max(ctx.box.minX(), cx - R); x <= Math.min(ctx.box.maxX(), cx + R); x++) {
            for (int z = Math.max(ctx.box.minZ(), cz - R); z <= Math.min(ctx.box.maxZ(), cz + R); z++) {
                double dx = x - cx;
                double dz = z - cz;
                double d = Math.sqrt(dx * dx + dz * dz);
                if (d <= R) {
                    this.column(ctx, x, z, d, Math.atan2(dz, dx), rift, cy);
                }
            }
        }
        this.features(ctx, cx, cy, cz, rift);
    }

    // ------------------------------------------------------------------ Gelände

    private void column(LairContext ctx, int x, int z, double d, double angle, double rift, int cy) {
        int top0 = ctx.originalHeight(x, z, false) - 1;
        int h0 = ctx.groundY(x, z);
        double floorR = floorRadius(ctx, angle);
        double wallH = wallHeight(ctx, angle);
        double riftDiff = Math.abs(Mth.wrapDegrees((angle - rift) * Mth.RAD_TO_DEG)) * Mth.DEG_TO_RAD;
        double riftHalf = 3.5 / Math.max(6.0, d);                  // die Schlucht ist überall etwa sieben Blöcke breit
        double riftF = riftDiff < riftHalf ? 1.0 : riftDiff < riftHalf * 1.8 ? 1.0 - (riftDiff - riftHalf) / (riftHalf * 0.8) : 0.0;
        double n = ctx.noise(x, z, 0.1, 521);
        int top = cy + TOP;
        int hs;
        boolean wall = false;
        if (d < floorR) {
            hs = cy - 1 + (d > floorR - 4 ? 1 : 0) + (int) Math.round((n - 0.5) * 1.2);
        } else {
            double u = Mth.clamp((d - floorR) / 6.0, 0, 1);
            double rim = cy - 1 + wallH * Math.min(1.0, u * 1.25);
            double outer = d > floorR + 6 ? Mth.lerp(LairContext.smooth(Mth.clamp((d - floorR - 6) / (R - floorR - 6), 0, 1)),
                    cy - 1 + wallH, h0) : rim;
            double target = Math.max(rim, outer);
            // die Schlucht: Boden steigt sanft bis auf das Gelände am Rand
            double riftFloor = Mth.lerp(Mth.clamp((d - floorR) / (R - floorR), 0, 1), cy - 1, h0);
            target = Mth.lerp(riftF, target, riftFloor);
            hs = (int) Math.round(target + (n - 0.5) * 2.0);
            wall = riftF < 0.5;
        }
        int bottom = Math.min(h0, hs) - 6;
        for (int y = bottom; y < hs; y++) {
            ctx.set(x, y, z, this.rock(ctx, x, y, z, cy, wall && d >= floorR));
        }
        BlockState surface = d < floorR || riftF > 0.5 ? this.floor(ctx, x, z, d, angle) : this.rock(ctx, x, hs, z, cy, true);
        if (wall && d >= floorR && hs > cy + 6 && ctx.rand(x, z, 522) < 0.5) {
            surface = Blocks.COARSE_DIRT.defaultBlockState();   // oben auf der Wand etwas Erde
        }
        ctx.set(x, hs, z, surface);
        int clearTo = d < floorR + 7 ? top : Math.max(hs + 2, top0 + 1);
        if (d < R - 6 || hs < top0) {
            ctx.fill(x, hs + 1, clearTo, z, Blocks.AIR.defaultBlockState());
        }
    }

    /** Geschichtetes Gestein: Bänder aus Schiefer, Stein, Granit und Terrakotta, Erzadern und Bernstein. */
    private BlockState rock(LairContext ctx, int x, int y, int z, int cy, boolean wall) {
        double vein = ctx.noise(x * 0.5 + y * 0.7, z * 0.5 - y * 0.3, 0.35, 523);
        if (wall && vein > 0.93) {
            return ModBlocks.RIFT_STONE.get().defaultBlockState();
        }
        double ore = ctx.rand(x * 31 + y, z * 17 - y, 524);
        if (wall && ore < 0.012) {
            return ore < 0.004 ? Blocks.GOLD_ORE.defaultBlockState() : ore < 0.008 ? Blocks.IRON_ORE.defaultBlockState()
                    : Blocks.COPPER_ORE.defaultBlockState();
        }
        int band = Math.floorMod(y - cy + (int) Math.round(ctx.noise(x, z, 0.05, 525) * 3.0), 11);
        return switch (band) {
            case 0, 1 -> ModBlocks.SHALE.base().get().defaultBlockState();
            case 2 -> Blocks.DYED_TERRACOTTA.brown().defaultBlockState();
            case 3, 4 -> Blocks.STONE.defaultBlockState();
            case 5 -> Blocks.TERRACOTTA.defaultBlockState();
            case 6, 7 -> Blocks.GRANITE.defaultBlockState();
            case 8 -> Blocks.DYED_TERRACOTTA.lightGray().defaultBlockState();
            default -> Blocks.ANDESITE.defaultBlockState();
        };
    }

    /** Kesselboden: Kies, Geröll, grobe Erde und Stein, glimmende Risse sternförmig zur Mitte. */
    private BlockState floor(LairContext ctx, int x, int z, double d, double angle) {
        double crack = Math.abs(Math.sin(angle * 3.5 + ctx.noise(x, z, 0.08, 526) * 2.5));
        if (crack < 0.07 && d > MOUND_RADIUS + 1 && d < 24) {
            return ModBlocks.RIFT_STONE.get().defaultBlockState();
        }
        double n = ctx.noise(x, z, 0.12, 527);
        double r = ctx.rand(x, z, 528);
        if (n > 0.66) {
            return Blocks.GRAVEL.defaultBlockState();
        }
        if (n < 0.26) {
            return ModBlocks.RUBBLE.get().defaultBlockState();
        }
        return r < 0.3 ? Blocks.STONE.defaultBlockState() : r < 0.55 ? Blocks.ANDESITE.defaultBlockState() : Blocks.COARSE_DIRT.defaultBlockState();
    }

    // ------------------------------------------------------------------ Einzelheiten

    private void features(LairContext ctx, int cx, int cy, int cz, double rift) {
        // Felsnadeln auf dem Kesselboden
        for (int k = 0; k < 5; k++) {
            double a = rift + Math.PI * 0.35 + k * Mth.TWO_PI * 0.13 + ctx.rand(k, 0, 530) * 0.3;
            double rr = 15 + ctx.rand(k, 1, 530) * 6;
            int sx = cx + (int) Math.round(Math.cos(a) * rr);
            int sz = cz + (int) Math.round(Math.sin(a) * rr);
            RuggedZone.spire(ctx, sx, cy, sz, 8 + (int) (ctx.rand(k, 2, 530) * 7));
        }
        // Bernsteindrusen am Fuß der Wände
        for (int k = 0; k < 12; k++) {
            double a = k * Mth.TWO_PI / 12 + ctx.rand(k, 3, 531) * 0.4;
            double diff = Math.abs(Mth.wrapDegrees((a - rift) * Mth.RAD_TO_DEG));
            if (diff < 20) {
                continue;
            }
            double rr = floorRadius(ctx, a) - 2.5;
            int px = cx + (int) Math.round(Math.cos(a) * rr);
            int pz = cz + (int) Math.round(Math.sin(a) * rr);
            this.geode(ctx, px, cy, pz, k);
        }
        // ein Felsbogen über dem Kessel (quer zur Schlucht)
        double archA = rift + Math.PI * 0.5;
        this.arch(ctx, cx + (int) Math.round(Math.cos(archA) * 27), cy + 14, cz + (int) Math.round(Math.sin(archA) * 27),
                cx + (int) Math.round(Math.cos(archA + 0.9) * 27), cy + 14, cz + (int) Math.round(Math.sin(archA + 0.9) * 27));
        // das verlassene Bergwerk am Eingang der Schlucht
        this.mineCamp(ctx, cx, cy, cz, rift);
        // der Felshügel, unter dem Terragon schläft
        mound(ctx, cx, cy, cz);
        // Nestkern
        BlockPos core = this.corePos(ctx.center);
        if (ctx.inside(core.getX(), core.getY(), core.getZ())) {
            ctx.set(core.getX(), core.getY(), core.getZ(), ModBlocks.DRAGON_LAIR_CORE.get().defaultBlockState());
            if (ctx.level.getBlockEntity(core) instanceof io.github.sirschrolli.kingdomsatwar.block.entity.DragonLairCoreBlockEntity be) {
                be.setup(DragonType.EARTH, ctx.center);
            }
        }
    }

    @Override
    public BlockPos corePos(BlockPos center) {
        return center.below(12);
    }

    /** Der Felshügel: Kuppel aus Geröll, Schiefer und Spaltgestein, gekrönt von Bernsteinkristallen. */
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
                double hTop = 4.0 * Math.sqrt(1.0 - (r / MOUND_RADIUS) * (r / MOUND_RADIUS)) + (ctx.noise(x, z, 0.4, 540) - 0.5) * 1.2;
                int top = cy - 1 + (int) Math.round(hTop);
                for (int y = cy - 1; y <= top; y++) {
                    double q = ctx.rand(x, z + y * 13, 541);
                    BlockState s = q < 0.3 ? ModBlocks.RUBBLE.get().defaultBlockState() : q < 0.55 ? ModBlocks.SHALE.base().get().defaultBlockState()
                            : q < 0.68 ? ModBlocks.RIFT_STONE.get().defaultBlockState() : q < 0.85 ? Blocks.COBBLESTONE.defaultBlockState()
                            : Blocks.ANDESITE.defaultBlockState();
                    ctx.set(x, y, z, s);
                }
                if (r < 2.5 && ctx.rand(x, z, 542) < 0.5) {
                    int h = 1 + (int) (ctx.rand(x, z, 543) * 3);
                    for (int k = 1; k <= h; k++) {
                        ctx.set(x, top + k, z, ModBlocks.AMBER_CRYSTAL.get().defaultBlockState());
                    }
                } else if (ctx.rand(x, z, 544) < 0.12) {
                    ctx.set(x, top + 1, z, ModBlocks.AMBER_CLUSTER.get().defaultBlockState());
                }
            }
        }
    }

    /** Bernsteindruse: Kristallsäulen und kleine Drusen in einer Nische. */
    private void geode(LairContext ctx, int x, int cy, int z, int salt) {
        for (int i = 0; i < 5; i++) {
            int ox = (int) Math.round((ctx.rand(salt, i, 550) - 0.5) * 3);
            int oz = (int) Math.round((ctx.rand(i, salt, 550) - 0.5) * 3);
            int h = 1 + (int) (ctx.rand(salt + i, 3, 550) * 4);
            for (int y = 0; y < h; y++) {
                ctx.set(x + ox, cy + y, z + oz, ModBlocks.AMBER_CRYSTAL.get().defaultBlockState());
            }
            ctx.setIfOpen(x + ox + 1, cy, z + oz, ModBlocks.AMBER_CLUSTER.get().defaultBlockState());
        }
    }

    /** Natürlicher Felsbogen zwischen zwei Punkten (dicker Bogen aus Schiefer und Stein). */
    private void arch(LairContext ctx, int x0, int y0, int z0, int x1, int y1, int z1) {
        int steps = 60;
        for (int s = 0; s <= steps; s++) {
            double t = (double) s / steps;
            double x = Mth.lerp(t, x0, x1);
            double z = Mth.lerp(t, z0, z1);
            double y = Mth.lerp(t, y0, y1) + Math.sin(t * Math.PI) * 7.0;
            double thick = 1.6 + 1.2 * (1.0 - Math.sin(t * Math.PI));
            int reach = (int) Math.ceil(thick);
            for (int dx = -reach; dx <= reach; dx++) {
                for (int dy = -reach; dy <= reach; dy++) {
                    for (int dz = -reach; dz <= reach; dz++) {
                        if (dx * dx + dy * dy + dz * dz <= thick * thick) {
                            int bx = (int) Math.round(x) + dx;
                            int by = (int) Math.round(y) + dy;
                            int bz = (int) Math.round(z) + dz;
                            ctx.set(bx, by, bz, dy == reach ? Blocks.COARSE_DIRT.defaultBlockState()
                                    : (by % 3 == 0 ? ModBlocks.SHALE.base().get().defaultBlockState() : Blocks.STONE.defaultBlockState()));
                        }
                    }
                }
            }
        }
    }

    /**
     * Verlassenes Bergwerk neben dem Eingang der Schlucht: ein Stollen mit Stützrahmen und Schienen führt in die
     * Kesselwand, davor stehen Fässer, eine Laterne, Bretter und ein Erzhaufen; im Kies liegen Fundstellen.
     */
    private void mineCamp(LairContext ctx, int cx, int cy, int cz, double rift) {
        double a = rift + 0.5;
        double fx = Math.cos(a);
        double fz = Math.sin(a);
        double sx = -fz;
        double sz = fx;
        double start = floorRadius(ctx, a) - 2;
        BlockState log = Blocks.SPRUCE_LOG.defaultBlockState();
        Direction.Axis across = Math.abs(sx) > Math.abs(sz) ? Direction.Axis.X : Direction.Axis.Z;
        RailShape shape = Math.abs(fx) > Math.abs(fz) ? RailShape.EAST_WEST : RailShape.NORTH_SOUTH;
        for (int k = 0; k <= 9; k++) {
            int px = cx + (int) Math.round(fx * (start + k));
            int pz = cz + (int) Math.round(fz * (start + k));
            for (int w = -1; w <= 1; w++) {
                int wx = px + (int) Math.round(sx * w);
                int wz = pz + (int) Math.round(sz * w);
                for (int h = 0; h < 3; h++) {
                    ctx.set(wx, cy + h, wz, Blocks.AIR.defaultBlockState());
                }
                ctx.set(wx, cy - 1, wz, Blocks.GRAVEL.defaultBlockState());
            }
            if (k % 3 == 1) {
                // Stützrahmen: zwei Pfosten und ein Querbalken
                for (int w : new int[] {-1, 1}) {
                    int wx = px + (int) Math.round(sx * w);
                    int wz = pz + (int) Math.round(sz * w);
                    ctx.set(wx, cy, wz, log);
                    ctx.set(wx, cy + 1, wz, log);
                }
                for (int w = -1; w <= 1; w++) {
                    ctx.set(px + (int) Math.round(sx * w), cy + 2, pz + (int) Math.round(sz * w), log.setValue(RotatedPillarBlock.AXIS, across));
                }
                if (k == 4) {
                    ctx.set(px, cy + 1, pz, Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));
                }
            }
            if (ctx.rand(px, pz, 560) < 0.85 && k > 0) {
                ctx.set(px, cy, pz, Blocks.RAIL.defaultBlockState().setValue(RailBlock.SHAPE, shape));
            }
        }
        // Lager vor dem Stollen
        int ox = cx + (int) Math.round(fx * (start - 3));
        int oz = cz + (int) Math.round(fz * (start - 3));
        ctx.set(ox + (int) Math.round(sx * 2), cy, oz + (int) Math.round(sz * 2), Blocks.BARREL.defaultBlockState());
        ctx.set(ox + (int) Math.round(sx * 3), cy, oz + (int) Math.round(sz * 3), Blocks.BARREL.defaultBlockState());
        ctx.set(ox + (int) Math.round(sx * 2), cy + 1, oz + (int) Math.round(sz * 2), Blocks.LANTERN.defaultBlockState());
        ctx.set(ox - (int) Math.round(sx * 2), cy, oz - (int) Math.round(sz * 2), Blocks.SPRUCE_PLANKS.defaultBlockState());
        ctx.set(ox - (int) Math.round(sx * 2), cy + 1, oz - (int) Math.round(sz * 2), Blocks.SPRUCE_SLAB.defaultBlockState());
        ctx.set(ox - (int) Math.round(sx * 3), cy, oz - (int) Math.round(sz * 3), Blocks.RAW_IRON_BLOCK.defaultBlockState());
        ctx.set(ox - (int) Math.round(sx * 3 + fx), cy, oz - (int) Math.round(sz * 3 + fz), ModBlocks.RUBBLE.get().defaultBlockState());
        ctx.set(ox + (int) Math.round(sx * 2 - fx * 2), cy, oz + (int) Math.round(sz * 2 - fz * 2),
                Blocks.SPRUCE_FENCE.defaultBlockState().setValue(FenceBlock.NORTH, false));
        ctx.set(ox + (int) Math.round(sx * 2 - fx * 2), cy + 1, oz + (int) Math.round(sz * 2 - fz * 2), Blocks.LANTERN.defaultBlockState());
        this.suspicious(ctx, ox, cy - 1, oz, 4000L);
        this.suspicious(ctx, cx + (int) Math.round(fx * (start + 8)), cy - 1, cz + (int) Math.round(fz * (start + 8)), 4001L);
    }

    private void suspicious(LairContext ctx, int x, int y, int z, long salt) {
        if (!ctx.inside(x, y, z)) {
            return;
        }
        BlockPos pos = new BlockPos(x, y, z);
        ctx.set(x, y, z, Blocks.SUSPICIOUS_GRAVEL.defaultBlockState());
        if (ctx.level.getBlockEntity(pos) instanceof BrushableBlockEntity brushable) {
            brushable.setLootTable(ModLootTables.DRAGON_NEST_ARCHAEOLOGY.get(DragonType.EARTH), ctx.seed + salt);
        }
    }
}
