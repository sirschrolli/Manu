package io.github.sirschrolli.kingdomsatwar.worldgen.lair;

import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import io.github.sirschrolli.kingdomsatwar.loot.ModLootTables;
import io.github.sirschrolli.kingdomsatwar.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SeaPickleBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.entity.BrushableBlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;

/**
 * Thalassas Versunkene Grotte: tief unter dem Meer wölbt sich eine riesige Kuppel aus Abgrundstein über einer
 * Luftblase. In ihrer Mitte liegt ein dunkler, tiefer See, in dem die Herrin der Tiefe schläft; ringsum ein Ufer aus
 * Abgrundsand mit den Ruinen eines versunkenen Tempels: ein Säulenkranz aus Prismarin, der die Kuppel trägt, ein
 * Thron mit einem alten Conduit und Truhen, die Gebeine eines Wals, Ketten mit Meereslaternen über dem Wasser und
 * Leuchtkorallen. Durch ein Rundfenster im Scheitel schimmert das Meer.
 *
 * <p>Der einzige Weg hinein ist ein gefluteter Gang: Er beginnt außen am Meeresgrund unter einem Torbogen und mündet
 * unter Wasser in den See – man taucht im See auf und klettert ans Ufer. (Wasser fließt nicht nach oben, darum bleibt
 * die Luftblase erhalten.) Ringsum liegt die Abgrundgrotte ({@link AbyssZone}).
 */
public final class WaterLairDesign implements LairDesign {
    private static final int R = 46;
    private static final int DOME_HEIGHT = 20;
    private static final int SHELL = 3;
    /** Die Abgrundgrotte beginnt gleich hinter der Kuppelwand (der Meeresgrund ringsum gehört schon zu ihr). */
    private final AbyssZone zone = new AbyssZone(this, 33);

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
        return 24;
    }

    @Override
    public int heightAbove() {
        return DOME_HEIGHT + SHELL + 6;
    }

    @Override
    public int floorHeight(Structure.GenerationContext context, int x, int z) {
        int seabed = DragonLairStructure.averageHeight(context, x, z, 30, Heightmap.Types.OCEAN_FLOOR_WG);
        int sea = context.chunkGenerator().getSeaLevel();
        return Mth.clamp(seabed - 4, context.heightAccessor().getMinY() + 40, sea - DOME_HEIGHT - SHELL - 5);
    }

    @Override
    public BlockPos corePos(BlockPos center) {
        return center.below(18);
    }

    /** Halbmesser des Sees (unregelmäßig). */
    static double lakeRadius(LairContext ctx, double angle) {
        return 15.0 + (ctx.angleNoise(angle, 2.4, 801) - 0.5) * 3.0;
    }

    /** Halbmesser der Kuppel am Boden (innen). */
    private static double domeRadius(LairContext ctx, double angle) {
        return 27.0 + (ctx.angleNoise(angle, 2.0, 802) - 0.5) * 3.0;
    }

    private static double entranceAngle(LairContext ctx) {
        return ctx.rand(0, 0, 899) * Mth.TWO_PI;
    }

    /** Innere Deckenhöhe über dem Ufer (Ellipsoid). */
    private static double ceiling(double d, double rin) {
        double u = Math.min(1.0, d / rin);
        return DOME_HEIGHT * Math.sqrt(1.0 - u * u);
    }

    @Override
    public void build(LairContext ctx) {
        int cx = ctx.center.getX();
        int cy = ctx.center.getY();
        int cz = ctx.center.getZ();
        double entrance = entranceAngle(ctx);
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
        this.tunnel(ctx, cx, cy, cz, entrance);
        this.features(ctx, cx, cy, cz, entrance);
    }

    // ------------------------------------------------------------------ Kuppel, Ufer und See

    private void column(LairContext ctx, int x, int z, double d, double angle, int cy) {
        double rin = domeRadius(ctx, angle);
        double lake = lakeRadius(ctx, angle);
        if (d > rin + SHELL + 0.5) {
            return;   // außerhalb der Kuppel: der Meeresgrund gehört der Abgrundgrotte
        }
        BlockState stone = ModBlocks.ABYSSAL_STONE.base().get().defaultBlockState();
        int bottom = cy - 24;
        if (d > rin) {
            // die Kuppelwand reicht bis tief in den Grund
            int top = cy - 1 + (int) Math.ceil(ceiling(rin - 0.5, rin)) + SHELL;
            for (int y = bottom; y <= top; y++) {
                ctx.set(x, y, z, this.shell(ctx, x, y, z, cy));
            }
            return;
        }
        double ceil = ceiling(d, rin);
        int ceilY = cy - 1 + (int) Math.round(ceil);
        // Schale über der Luftblase (mit Rundfenster im Scheitel)
        for (int y = ceilY + 1; y <= ceilY + SHELL; y++) {
            boolean oculus = d < 3.2 && y == ceilY + SHELL;
            ctx.set(x, y, z, oculus ? Blocks.STAINED_GLASS.lightBlue().defaultBlockState() : this.shell(ctx, x, y, z, cy));
        }
        if (d < 3.2) {
            for (int y = ceilY + 1; y < ceilY + SHELL; y++) {
                ctx.set(x, y, z, Blocks.STAINED_GLASS.lightBlue().defaultBlockState());
            }
        }
        // Luft unter der Kuppel
        int floorY = cy - 1;
        for (int y = floorY + 1; y <= ceilY; y++) {
            ctx.set(x, y, z, Blocks.AIR.defaultBlockState());
        }
        if (d < lake) {
            // der See: tief und dunkel, zum Rand hin steil ansteigend
            double edge = Math.max(0.0, (d - lake * 0.7) / (lake * 0.3));
            int lakeBottom = cy - 16 + (int) Math.round(edge * edge * 9.0);
            for (int y = bottom; y < lakeBottom; y++) {
                ctx.set(x, y, z, stone);
            }
            ctx.set(x, lakeBottom, z, ctx.rand(x, z, 803) < 0.6 ? ModBlocks.ABYSSAL_SAND.get().defaultBlockState() : stone);
            for (int y = lakeBottom + 1; y <= cy - 2; y++) {
                ctx.set(x, y, z, Blocks.WATER.defaultBlockState());
            }
            ctx.set(x, cy - 1, z, Blocks.AIR.defaultBlockState());
            // Seegrund: Leuchtkorallen, Meeresgurken, Seegras
            double r = ctx.rand(x, z, 804);
            if (r < 0.12) {
                ctx.set(x, lakeBottom + 1, z, ModBlocks.GLOW_CORAL.get().defaultBlockState()
                        .setValue(io.github.sirschrolli.kingdomsatwar.block.GlowCoralBlock.WATERLOGGED, true));
            } else if (r < 0.2) {
                ctx.set(x, lakeBottom + 1, z, Blocks.SEA_PICKLE.defaultBlockState().setValue(SeaPickleBlock.PICKLES, 1 + (int) (r * 30) % 4));
            } else if (r < 0.3 && lakeBottom < cy - 5) {
                ctx.set(x, lakeBottom + 1, z, Blocks.SEAGRASS.defaultBlockState());
            }
            return;
        }
        // Ufer: fester Grund bis tief hinab, oben Abgrundsand, Stein und Kies
        for (int y = bottom; y < floorY; y++) {
            ctx.set(x, y, z, stone);
        }
        double n = ctx.noise(x, z, 0.12, 805);
        BlockState floor = n > 0.7 ? Blocks.GRAVEL.defaultBlockState() : n < 0.3 ? stone : ModBlocks.ABYSSAL_SAND.get().defaultBlockState();
        if (d < lake + 1.5) {
            floor = stone;   // Uferkante aus Fels
            // in die Uferkante eingelassene Meereslaternen: ein leuchtender Ring um den See
            if (d > lake + 0.5 && Math.floorMod((int) Math.round(angle * 40 / Math.PI), 5) == 0) {
                floor = Blocks.SEA_LANTERN.defaultBlockState();
            }
        }
        ctx.set(x, floorY, z, floor);
        // kleine Pfützen und Leuchtkorallen am Ufer
        double r = ctx.rand(x, z, 806);
        if (d > lake + 3 && d < rin - 3 && ctx.noise(x, z, 0.2, 807) > 0.82) {
            ctx.set(x, floorY, z, Blocks.WATER.defaultBlockState());
            ctx.set(x, floorY - 1, z, ModBlocks.ABYSSAL_SAND.get().defaultBlockState());
        } else if (r < 0.07 && d > lake + 2) {
            ctx.set(x, floorY + 1, z, ModBlocks.GLOW_CORAL.get().defaultBlockState());
        }
    }

    /** Kuppelschale: Abgrundstein mit Flecken aus dunklem Prismarin und Adern aus Perlmutt. */
    private BlockState shell(LairContext ctx, int x, int y, int z, int cy) {
        double n = ctx.noise(x * 0.6 + y * 0.4, z * 0.6 - y * 0.3, 0.2, 808);
        if (n > 0.86) {
            return Blocks.DARK_PRISMARINE.defaultBlockState();
        }
        if (n < 0.1) {
            return ModBlocks.NACRE_BLOCK.get().defaultBlockState();
        }
        return ModBlocks.ABYSSAL_STONE.base().get().defaultBlockState();
    }

    // ------------------------------------------------------------------ Gefluteter Gang

    /**
     * Der Gang: ein gefluteter Tunnel (fünf breit, vier hoch) vom Torbogen am Meeresgrund unter der Kuppelwand
     * hindurch, steigt nie über den Uferboden und mündet tief im See. Wände aus Prismarin, alle sechs Blöcke eine
     * Meereslaterne.
     */
    private void tunnel(LairContext ctx, int cx, int cy, int cz, double a) {
        double fx = Math.cos(a);
        double fz = Math.sin(a);
        double sx = -fz;
        double sz = fx;
        double lake = lakeRadius(ctx, a);
        double rin = domeRadius(ctx, a);
        double start = lake - 3.0;
        double end = rin + SHELL + 13.0;
        int steps = (int) Math.ceil((end - start) * 2);
        for (int s = 0; s <= steps; s++) {
            double t = start + s * 0.5;
            // unter dem Ufer bleibt der Gang tief, draußen steigt er zum Meeresgrund
            double outside = Math.max(0.0, (t - rin - SHELL) / 13.0);
            double floorY = cy - 11 + outside * 15.0;
            int px = cx + (int) Math.round(fx * t);
            int pz = cz + (int) Math.round(fz * t);
            if (!ctx.near(px, pz, 5)) {
                continue;
            }
            for (int w = -3; w <= 3; w++) {
                int x = px + (int) Math.round(sx * w);
                int z = pz + (int) Math.round(sz * w);
                int fy = (int) Math.floor(floorY);
                for (int h = -1; h <= 5; h++) {
                    int y = fy + h;
                    boolean wall = Math.abs(w) == 3 || h == -1 || h == 5;
                    if (wall) {
                        if (t < lake + 0.5) {
                            continue;   // im See keine Wände
                        }
                        BlockState block = h == -1 ? Blocks.PRISMARINE_BRICKS.defaultBlockState() : Blocks.PRISMARINE.defaultBlockState();
                        if (h == 4 && Math.abs(w) == 3 && s % 12 == 0) {
                            block = Blocks.SEA_LANTERN.defaultBlockState();
                        }
                        ctx.set(x, y, z, block);
                    } else {
                        ctx.set(x, y, z, Blocks.WATER.defaultBlockState());
                    }
                }
            }
        }
        // der Torbogen am Meeresgrund
        int gx = cx + (int) Math.round(fx * end);
        int gz = cz + (int) Math.round(fz * end);
        if (ctx.near(gx, gz, 8)) {
            int gy = cy - 11 + 15;
            for (int w = -4; w <= 4; w += 8) {
                int x = gx + (int) Math.round(sx * w);
                int z = gz + (int) Math.round(sz * w);
                for (int h = -2; h <= 7; h++) {
                    ctx.set(x, gy + h, z, h == 7 ? Blocks.DARK_PRISMARINE.defaultBlockState()
                            : Blocks.PRISMARINE_BRICKS.defaultBlockState());
                }
                ctx.set(x, gy + 8, z, Blocks.SEA_LANTERN.defaultBlockState());
            }
            for (int w = -4; w <= 4; w++) {
                int x = gx + (int) Math.round(sx * w);
                int z = gz + (int) Math.round(sz * w);
                double arc = 7 + Math.cos(w / 4.0 * Math.PI * 0.5) * 2.0;
                ctx.set(x, gy + (int) Math.round(arc), z, Blocks.DARK_PRISMARINE.defaultBlockState());
            }
        }
    }

    // ------------------------------------------------------------------ Tempelruinen

    private void features(LairContext ctx, int cx, int cy, int cz, double entrance) {
        double throne = entrance + Math.PI;
        // Säulenkranz: zehn Säulen tragen die Kuppel (vor dem Gang und dem Thron Lücken)
        for (int k = 0; k < 10; k++) {
            double a = entrance + Math.PI / 10 + k * Mth.TWO_PI / 10;
            double rr = (lakeRadius(ctx, a) + domeRadius(ctx, a)) * 0.5 + 0.5;
            int px = cx + (int) Math.round(Math.cos(a) * rr);
            int pz = cz + (int) Math.round(Math.sin(a) * rr);
            int top = cy - 1 + (int) Math.floor(ceiling(rr, domeRadius(ctx, a)));
            boolean broken = ctx.rand(k, 1, 810) < 0.3;
            this.column(ctx, px, cy, pz, top, broken, k, a);
        }
        this.throne(ctx, cx, cy, cz, throne);
        this.whaleBones(ctx, cx, cy, cz, entrance + Math.PI * 0.5);
        // Ketten mit Meereslaternen über dem See
        for (int k = 0; k < 8; k++) {
            double a = k * Mth.TWO_PI / 8 + 0.4;
            double rr = 6.0 + ctx.rand(k, 2, 811) * 7.0;
            int x = cx + (int) Math.round(Math.cos(a) * rr);
            int z = cz + (int) Math.round(Math.sin(a) * rr);
            if (!ctx.near(x, z, 1)) {
                continue;
            }
            int top = cy - 1 + (int) Math.round(ceiling(rr, domeRadius(ctx, a)));
            int low = cy + 3 + (int) (ctx.rand(k, 3, 811) * 4);
            for (int y = low + 1; y <= top; y++) {
                ctx.set(x, y, z, Blocks.IRON_CHAIN.defaultBlockState());
            }
            ctx.set(x, low, z, Blocks.SEA_LANTERN.defaultBlockState());
        }
        // Leuchtflechten an der Kuppeldecke
        for (int x = Math.max(ctx.box.minX(), cx - 30); x <= Math.min(ctx.box.maxX(), cx + 30); x++) {
            for (int z = Math.max(ctx.box.minZ(), cz - 30); z <= Math.min(ctx.box.maxZ(), cz + 30); z++) {
                double dx = x - cx;
                double dz = z - cz;
                double d = Math.sqrt(dx * dx + dz * dz);
                double rin = domeRadius(ctx, Math.atan2(dz, dx));
                if (d < 4 || d > rin - 1 || ctx.rand(x, z, 812) > 0.2) {
                    continue;
                }
                int y = cy - 1 + (int) Math.round(ceiling(d, rin));
                if (ctx.get(x, y, z).isAir() && !ctx.get(x, y + 1, z).isAir()) {
                    ctx.set(x, y, z, Blocks.GLOW_LICHEN.defaultBlockState().setValue(MultifaceBlock.getFaceProperty(Direction.UP), true));
                }
            }
        }
        // Nestkern unter dem Seegrund
        BlockPos core = this.corePos(ctx.center);
        if (ctx.inside(core.getX(), core.getY(), core.getZ())) {
            ctx.set(core.getX(), core.getY(), core.getZ(), ModBlocks.DRAGON_LAIR_CORE.get().defaultBlockState());
            if (ctx.level.getBlockEntity(core) instanceof io.github.sirschrolli.kingdomsatwar.block.entity.DragonLairCoreBlockEntity be) {
                be.setup(DragonType.WATER, ctx.center);
            }
        }
    }

    /** Tempelsäule bis unter die Kuppel: Sockel, Schaft aus Prismarin, Kapitell mit Meereslaterne (oder geborsten). */
    private void column(LairContext ctx, int x, int cy, int z, int top, boolean broken, int salt, double angle) {
        if (!ctx.near(x, z, 6)) {
            return;
        }
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                ctx.set(x + dx, cy - 1, z + dz, Blocks.PRISMARINE_BRICKS.defaultBlockState());
                ctx.set(x + dx, cy, z + dz, Blocks.PRISMARINE_BRICK_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM));
            }
        }
        int height = broken ? 3 + (int) (ctx.rand(salt, 4, 813) * 4) : top - cy;
        for (int h = 0; h < height; h++) {
            BlockState block = h % 5 == 2 ? ModBlocks.NACRE_BLOCK.get().defaultBlockState() : Blocks.PRISMARINE.defaultBlockState();
            if (h == 1) {
                block = Blocks.SEA_LANTERN.defaultBlockState();   // leuchtender Säulenfuß
            }
            ctx.set(x, cy + h, z, block);
        }
        if (broken) {
            // Trümmer liegen am Ufer
            double fx = Math.cos(angle + 1.2);
            double fz = Math.sin(angle + 1.2);
            for (int k = 2; k < 7; k++) {
                if (ctx.rand(salt, k, 814) < 0.65) {
                    ctx.setIfOpen(x + (int) Math.round(fx * k), cy, z + (int) Math.round(fz * k),
                            ctx.rand(salt, k, 815) < 0.5 ? Blocks.PRISMARINE.defaultBlockState() : Blocks.PRISMARINE_BRICKS.defaultBlockState());
                }
            }
            return;
        }
        // Kapitell
        for (Direction side : Direction.Plane.HORIZONTAL) {
            ctx.set(x + side.getStepX(), top - 1, z + side.getStepZ(), Blocks.DARK_PRISMARINE_STAIRS.defaultBlockState()
                    .setValue(StairBlock.FACING, side.getOpposite()).setValue(StairBlock.HALF, Half.TOP));
        }
        ctx.set(x, top - 1, z, Blocks.SEA_LANTERN.defaultBlockState());
        ctx.set(x, top, z, Blocks.DARK_PRISMARINE.defaultBlockState());
    }

    /**
     * Der Thron der Tiefe gegenüber dem Gang: eine Plattform mit Stufen aus dunklem Prismarin, darauf ein uraltes
     * Conduit auf einem Perlmuttsockel, zwei Truhen und Fundstellen im Sand davor.
     */
    private void throne(LairContext ctx, int cx, int cy, int cz, double a) {
        double rin = domeRadius(ctx, a);
        double fx = Math.cos(a);
        double fz = Math.sin(a);
        double sx = -fz;
        double sz = fx;
        int tx = cx + (int) Math.round(fx * (rin - 6.5));
        int tz = cz + (int) Math.round(fz * (rin - 6.5));
        if (!ctx.near(tx, tz, 9)) {
            return;
        }
        Direction toCenter = Direction.fromYRot(Math.toDegrees(a + Math.PI) - 90.0);
        for (int u = -5; u <= 5; u++) {
            for (int v = -3; v <= 3; v++) {
                int x = tx + (int) Math.round(sx * u + fx * v);
                int z = tz + (int) Math.round(sz * u + fz * v);
                int level = v <= -2 ? 0 : v <= 0 ? 1 : 2;     // Stufen zum Thron hinauf (zur Mitte hin niedriger)
                for (int h = 0; h < level; h++) {
                    ctx.set(x, cy + h, z, Blocks.DARK_PRISMARINE.defaultBlockState());
                }
                if (v == -2 || v == 0) {
                    ctx.set(x, cy + level, z, Blocks.DARK_PRISMARINE_STAIRS.defaultBlockState()
                            .setValue(StairBlock.FACING, toCenter.getOpposite()).setValue(StairBlock.HALF, Half.BOTTOM));
                }
            }
        }
        // Conduit auf dem Perlmuttsockel
        int px = tx + (int) Math.round(fx * 2);
        int pz = tz + (int) Math.round(fz * 2);
        ctx.set(px, cy + 2, pz, ModBlocks.NACRE_BLOCK.get().defaultBlockState());
        ctx.set(px, cy + 3, pz, Blocks.PRISMARINE_WALL.defaultBlockState());
        ctx.set(px, cy + 4, pz, Blocks.CONDUIT.defaultBlockState());
        // zwei Perlmuttsäulen links und rechts mit Laternen
        for (int side = -1; side <= 1; side += 2) {
            int x = tx + (int) Math.round(sx * 4 * side + fx * 2);
            int z = tz + (int) Math.round(sz * 4 * side + fz * 2);
            for (int h = 2; h < 6; h++) {
                ctx.set(x, cy + h, z, ModBlocks.NACRE_BLOCK.get().defaultBlockState());
            }
            ctx.set(x, cy + 6, z, Blocks.SEA_LANTERN.defaultBlockState());
        }
        // Truhen
        for (int side = -1; side <= 1; side += 2) {
            int x = tx + (int) Math.round(sx * 2 * side + fx * 1);
            int z = tz + (int) Math.round(sz * 2 * side + fz * 1);
            if (ctx.inside(x, cy + 2, z)) {
                ctx.set(x, cy + 2, z, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, toCenter));
                if (ctx.level.getBlockEntity(new BlockPos(x, cy + 2, z)) instanceof ChestBlockEntity chest) {
                    chest.setLootTable(ModLootTables.SUNKEN_GROTTO_CHEST, ctx.seed + 8101L + side);
                }
            }
        }
        // Fundstellen im Sand vor den Stufen
        for (int k = -1; k <= 1; k++) {
            int x = tx + (int) Math.round(sx * k * 3 - fx * 5);
            int z = tz + (int) Math.round(sz * k * 3 - fz * 5);
            if (ctx.inside(x, cy - 1, z)) {
                ctx.set(x, cy - 1, z, Blocks.SUSPICIOUS_SAND.defaultBlockState());
                if (ctx.level.getBlockEntity(new BlockPos(x, cy - 1, z)) instanceof BrushableBlockEntity brushable) {
                    brushable.setLootTable(ModLootTables.DRAGON_NEST_ARCHAEOLOGY.get(DragonType.WATER), ctx.seed + 8200L + k);
                }
            }
        }
    }

    /** Die Gebeine eines Wals: Rippenbögen aus Knochenblöcken am Ufer, dazu der Schädel. */
    private void whaleBones(LairContext ctx, int cx, int cy, int cz, double a) {
        double rr = (lakeRadius(ctx, a) + domeRadius(ctx, a)) * 0.5;
        double fx = Math.cos(a);
        double fz = Math.sin(a);
        double sx = -fz;
        double sz = fx;
        int bx = cx + (int) Math.round(fx * rr);
        int bz = cz + (int) Math.round(fz * rr);
        if (!ctx.near(bx, bz, 10)) {
            return;
        }
        BlockState bone = Blocks.BONE_BLOCK.defaultBlockState();
        for (int k = -3; k <= 3; k++) {
            double height = 4.5 - Math.abs(k) * 0.5;
            for (int s = -1; s <= 1; s += 2) {
                for (int step = 0; step <= 8; step++) {
                    double t = step / 8.0;
                    double w = s * (1.0 + Math.sin(t * Math.PI) * 2.8) * (1.0 - t * 0.15);
                    double h = Math.sin(t * Math.PI * 0.9) * height;
                    int x = bx + (int) Math.round(sx * k * 2 + fx * w);
                    int z = bz + (int) Math.round(sz * k * 2 + fz * w);
                    ctx.setIfOpen(x, cy + (int) Math.round(h), z, bone.setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y));
                }
            }
        }
        // Wirbelsäule und Schädel
        for (int k = -7; k <= 9; k++) {
            int x = bx + (int) Math.round(sx * k);
            int z = bz + (int) Math.round(sz * k);
            ctx.setIfOpen(x, cy, z, bone.setValue(RotatedPillarBlock.AXIS, Math.abs(sx) > Math.abs(sz) ? Direction.Axis.X : Direction.Axis.Z));
        }
        for (int u = 0; u < 3; u++) {
            for (int v = -1; v <= 1; v++) {
                int x = bx + (int) Math.round(sx * (10 + u) + fx * v);
                int z = bz + (int) Math.round(sz * (10 + u) + fz * v);
                ctx.setIfOpen(x, cy, z, bone);
                if (u < 2 && v == 0) {
                    ctx.setIfOpen(x, cy + 1, z, bone);
                }
            }
        }
    }
}
