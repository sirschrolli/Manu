package io.github.sirschrolli.kingdomsatwar.worldgen.lair;

import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import io.github.sirschrolli.kingdomsatwar.loot.ModLootTables;
import io.github.sirschrolli.kingdomsatwar.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
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
 * Voltarions Sturmgipfel: Der Gipfel eines hohen Berges ist zu einem Plateau (gut 40 Blöcke weit) abgetragen, dessen
 * Rand als Felskranz steil zum Berg abbricht. Über den Boden aus dunklem Sturmstein ziehen sich verästelte Blitzfiguren
 * aus geladenem Sturmstein (wie Lichtenberg-Figuren) bis zu einem runden Podest in der Mitte, auf das Voltarion beim
 * Erwachen herabfährt. Ein Ring aus neun Blitzsäulen mit grün angelaufenen Blitzableitern umgibt das Podest, einige
 * sind geborsten. Überall stecken Fulguritröhren im versengten Fels, Sturmkristalle wachsen in den Fugen, und am Rand
 * steht die verfallene Sturmwarte der alten Sturmwächter (mit Truhe und Fundstellen). Ringsum liegen die Sturmklippen
 * ({@link StormZone}).
 */
public final class LightningLairDesign implements LairDesign {
    private static final int R = 48;
    private static final int TOP = 46;
    private static final int PILLARS = 9;
    private final StormZone zone = new StormZone(this, R);

    @Override
    public int radius() {
        return R;
    }

    @Override
    public int zoneRadius() {
        return 108;
    }

    @Override
    public void buildZone(LairContext ctx) {
        this.zone.build(ctx);
    }

    @Override
    public int depthBelow() {
        return 30;
    }

    @Override
    public int heightAbove() {
        return TOP;
    }

    @Override
    public int floorHeight(Structure.GenerationContext context, int x, int z) {
        int avg = DragonLairStructure.averageHeight(context, x, z, 20, Heightmap.Types.WORLD_SURFACE_WG);
        int sea = context.chunkGenerator().getSeaLevel();
        return Mth.clamp(avg + 3, sea + 12, 250);
    }

    @Override
    public BlockPos corePos(BlockPos center) {
        return center.below(8);
    }

    private static double plateauRadius(LairContext ctx, double angle) {
        return 19.0 + (ctx.angleNoise(angle, 2.8, 701) - 0.5) * 7.0;
    }

    /** Höhe des Felskranzes am Plateaurand (an manchen Stellen ragen Zacken auf, dazwischen Lücken). */
    private static double crownHeight(LairContext ctx, double angle) {
        double n = ctx.angleNoise(angle, 9.0, 702);
        return n > 0.55 ? (n - 0.55) * 14.0 : 0.0;
    }

    private static double towerAngle(LairContext ctx) {
        return ctx.rand(0, 0, 799) * Mth.TWO_PI;
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
        double pr = plateauRadius(ctx, angle);
        double n = ctx.noise(x, z, 0.1, 703);
        boolean plateau = d < pr;
        int hs;
        if (plateau) {
            hs = cy - 1;
        } else {
            double t = Mth.clamp((d - pr) / (R - pr), 0, 1);
            double crown = d < pr + 5 ? crownHeight(ctx, angle) * Math.max(0.0, 1.0 - Math.abs(d - pr - 2.0) / 3.0) : 0.0;
            double target = Mth.lerp(LairContext.smooth(t), cy - 1 + (d < pr + 3 ? 1 : 0), h0) + crown;
            hs = (int) Math.round(target + (n - 0.5) * 2.5);
        }
        int bottom = Math.min(h0, hs) - (plateau ? 4 : 6);
        for (int y = bottom; y < hs; y++) {
            ctx.set(x, y, z, this.rock(ctx, x, y, z, cy));
        }
        ctx.set(x, hs, z, plateau ? this.floor(ctx, x, z) : this.rock(ctx, x, hs, z, cy));
        int clearTo = d < pr + 6 ? cy + TOP : Math.max(hs + 2, top0 + 1);
        if (d < R - 6 || hs < top0) {
            ctx.fill(x, hs + 1, clearTo, z, Blocks.AIR.defaultBlockState());
        }
    }

    /** Gestein des Gipfels: Sturmstein mit Bändern aus Tuff und Tiefenschiefer, Kupferadern und geladenen Adern. */
    private BlockState rock(LairContext ctx, int x, int y, int z, int cy) {
        double vein = ctx.noise(x * 0.5 + y * 0.7, z * 0.5 - y * 0.3, 0.35, 704);
        if (vein > 0.94) {
            return ModBlocks.CHARGED_STORMSTONE.get().defaultBlockState();
        }
        if (ctx.rand(x * 31 + y, z * 17 - y, 705) < 0.006) {
            return Blocks.COPPER_ORE.defaultBlockState();
        }
        int band = Math.floorMod(y - cy + (int) Math.round(ctx.noise(x, z, 0.05, 706) * 3.0), 10);
        return switch (band) {
            case 3 -> Blocks.SMOOTH_BASALT.defaultBlockState();
            case 7 -> Blocks.DEEPSLATE.defaultBlockState();
            case 8 -> Blocks.COBBLED_DEEPSLATE.defaultBlockState();
            default -> ModBlocks.STORMSTONE.base().get().defaultBlockState();
        };
    }

    /** Plateauboden: Sturmstein mit Flecken aus Kies, Tuff und Tiefenschiefer-Bruch. */
    private BlockState floor(LairContext ctx, int x, int z) {
        double n = ctx.noise(x, z, 0.12, 707);
        double r = ctx.rand(x, z, 708);
        if (n > 0.72) {
            return Blocks.GRAVEL.defaultBlockState();
        }
        if (n < 0.22) {
            return Blocks.SMOOTH_BASALT.defaultBlockState();
        }
        return r < 0.12 ? Blocks.COBBLED_DEEPSLATE.defaultBlockState() : ModBlocks.STORMSTONE.base().get().defaultBlockState();
    }

    // ------------------------------------------------------------------ Einzelheiten

    private void features(LairContext ctx, int cx, int cy, int cz) {
        this.lichtenberg(ctx, cx, cy, cz);
        this.dais(ctx, cx, cy, cz);
        double tower = towerAngle(ctx);
        // der Ring der Blitzsäulen (die Lücke vor der Sturmwarte bleibt frei)
        for (int k = 0; k < PILLARS; k++) {
            double a = tower + Math.PI / PILLARS + k * Mth.TWO_PI / PILLARS + (ctx.rand(k, 0, 720) - 0.5) * 0.12;
            double rr = Math.min(15.0, plateauRadius(ctx, a) - 4.5);
            int px = cx + (int) Math.round(Math.cos(a) * rr);
            int pz = cz + (int) Math.round(Math.sin(a) * rr);
            boolean broken = ctx.rand(k, 1, 720) < 0.3;
            int height = broken ? 4 + (int) (ctx.rand(k, 2, 720) * 4) : 11 + (int) (ctx.rand(k, 3, 720) * 5);
            this.pillar(ctx, px, cy, pz, height, broken, k, a);
            StormZone.crystals(ctx, px + (int) Math.round(Math.cos(a) * 2), cy - 1, pz + (int) Math.round(Math.sin(a) * 2));
        }
        // Einschlagstellen auf dem Plateau
        for (int k = 0; k < 11; k++) {
            double a = ctx.rand(k, 4, 721) * Mth.TWO_PI;
            double rr = 7.5 + ctx.rand(k, 5, 721) * 9.0;
            int sx = cx + (int) Math.round(Math.cos(a) * rr);
            int sz = cz + (int) Math.round(Math.sin(a) * rr);
            StormZone.strike(ctx, sx, cy - 1, sz);
        }
        // Felsnadeln auf dem Felskranz
        for (int k = 0; k < 6; k++) {
            double a = tower + 0.8 + k * Mth.TWO_PI / 6.6 + ctx.rand(k, 6, 722) * 0.3;
            double rr = plateauRadius(ctx, a) + 3.0;
            int sx = cx + (int) Math.round(Math.cos(a) * rr);
            int sz = cz + (int) Math.round(Math.sin(a) * rr);
            StormZone.spire(ctx, sx, cy + 1, sz, 6 + (int) (ctx.rand(k, 7, 722) * 7));
        }
        this.watchtower(ctx, cx, cy, cz, tower);
        // Nestkern
        BlockPos core = this.corePos(ctx.center);
        if (ctx.inside(core.getX(), core.getY(), core.getZ())) {
            ctx.set(core.getX(), core.getY(), core.getZ(), ModBlocks.DRAGON_LAIR_CORE.get().defaultBlockState());
            if (ctx.level.getBlockEntity(core) instanceof io.github.sirschrolli.kingdomsatwar.block.entity.DragonLairCoreBlockEntity be) {
                be.setup(DragonType.LIGHTNING, ctx.center);
            }
        }
    }

    /**
     * Blitzfiguren: Vom Podest laufen fünf verästelte Bahnen aus geladenem Sturmstein über den Plateauboden – im
     * Zickzack um ihre Hauptrichtung, mit Seitenästen, die sich wieder verzweigen.
     */
    private void lichtenberg(LairContext ctx, int cx, int cy, int cz) {
        int branches = 5;
        for (int b = 0; b < branches; b++) {
            double a = b * Mth.TWO_PI / branches + (ctx.rand(b, 0, 730) - 0.5) * 0.6;
            double length = 13.0 + ctx.rand(b, 1, 730) * 7.0;
            this.bolt(ctx, cx + 0.5 + Math.cos(a) * 5.0, cz + 0.5 + Math.sin(a) * 5.0, a, length, cy - 1, 1000 + b * 97, 0);
        }
    }

    private void bolt(LairContext ctx, double x, double z, double dir, double length, int y, int salt, int depth) {
        double px = x;
        double pz = z;
        for (int s = 0; s < length; s++) {
            double a = dir + (ctx.rand(salt, s, 731) - 0.5) * 1.5;
            px += Math.cos(a);
            pz += Math.sin(a);
            int bx = Mth.floor(px);
            int bz = Mth.floor(pz);
            if (LairContext.isGround(ctx.get(bx, y, bz))) {
                ctx.set(bx, y, bz, ModBlocks.CHARGED_STORMSTONE.get().defaultBlockState());
            }
            if (depth < 2 && s > 2 && ctx.rand(salt, s, 732) < 0.11) {
                double side = ctx.rand(salt, s, 733) < 0.5 ? 1.0 : -1.0;
                this.bolt(ctx, px, pz, dir + side * (0.55 + ctx.rand(salt, s, 734) * 0.5), (length - s) * 0.55, y, salt * 31 + s, depth + 1);
            }
        }
    }

    /**
     * Das Podest in der Mitte: eine runde, eine Stufe hohe Scheibe aus Sturmstein mit einem vierstrahligen Stern aus
     * geladenem Sturmstein, umgeben von einem Ring aus Stufen; an vier Ecken stehen Fulguritröhren mit Sturmkristallen.
     */
    public static void dais(LairContext ctx, int cx, int cy, int cz) {
        BlockState stone = ModBlocks.STORMSTONE.base().get().defaultBlockState();
        BlockState charged = ModBlocks.CHARGED_STORMSTONE.get().defaultBlockState();
        BlockState slab = ModBlocks.STORMSTONE.slab().get().defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM);
        for (int dx = -7; dx <= 7; dx++) {
            for (int dz = -7; dz <= 7; dz++) {
                double d = Math.sqrt(dx * dx + dz * dz);
                int x = cx + dx;
                int z = cz + dz;
                if (d <= 4.6) {
                    double a = Math.atan2(dz, dx);
                    double ray = Math.abs(Math.sin(a * 2.0));
                    boolean star = d < 1.2 || ray < 0.12 + 0.2 / Math.max(1.0, d);
                    ctx.set(x, cy - 1, z, stone);
                    ctx.set(x, cy, z, star ? charged : stone);
                } else if (d <= 5.7) {
                    ctx.set(x, cy - 1, z, stone);
                    ctx.set(x, cy, z, slab);
                }
            }
        }
        for (int k = 0; k < 4; k++) {
            double a = Math.PI / 4 + k * Math.PI / 2;
            int x = cx + (int) Math.round(Math.cos(a) * 6.8);
            int z = cz + (int) Math.round(Math.sin(a) * 6.8);
            for (int h = 0; h < 3 - (k % 2); h++) {
                ctx.set(x, cy + h, z, ModBlocks.FULGURITE.get().defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y));
            }
            ctx.setIfOpen(x + 1, cy, z, ModBlocks.STORM_CRYSTAL.get().defaultBlockState());
            ctx.setIfOpen(x, cy, z - 1, ModBlocks.STORM_CRYSTAL.get().defaultBlockState());
        }
    }

    /**
     * Blitzsäule: ein Sockel aus Sturmstein mit Stufen, darauf ein schlanker Schaft mit Bändern aus geladenem
     * Sturmstein, gekrönt von einem grün angelaufenen Kupferblock und einem Blitzableiter. Geborstene Säulen enden in
     * einer zackigen Bruchkante, ihre Trümmer liegen daneben.
     */
    private void pillar(LairContext ctx, int x, int cy, int z, int height, boolean broken, int salt, double angle) {
        if (!ctx.near(x, z, 8)) {
            return;
        }
        BlockState stone = ModBlocks.STORMSTONE.base().get().defaultBlockState();
        BlockState charged = ModBlocks.CHARGED_STORMSTONE.get().defaultBlockState();
        // Fundament (reicht tief, damit es auch über Spalten nicht schwebt)
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                for (int y = cy - 6; y <= cy + 1; y++) {
                    ctx.set(x + dx, y, z + dz, stone);
                }
            }
        }
        // Stufen rund um den Sockel
        Direction[] sides = {Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};
        for (Direction side : sides) {
            for (int w = -1; w <= 1; w++) {
                int sx = x + side.getStepX() * 2 + (side.getAxis() == Direction.Axis.Z ? w : 0);
                int sz = z + side.getStepZ() * 2 + (side.getAxis() == Direction.Axis.X ? w : 0);
                ctx.set(sx, cy - 1, sz, stone);
                ctx.set(sx, cy, sz, ModBlocks.STORMSTONE.stairs().get().defaultBlockState()
                        .setValue(StairBlock.FACING, side.getOpposite()).setValue(StairBlock.HALF, Half.BOTTOM));
            }
        }
        // Schaft
        for (int h = 2; h < height; h++) {
            boolean band = h % 4 == 0;
            ctx.set(x, cy + h, z, band ? charged : stone);
            if (h < 4) {
                for (Direction side : sides) {
                    ctx.set(x + side.getStepX(), cy + h, z + side.getStepZ(), stoneWall());
                }
            }
        }
        if (broken) {
            // zackige Bruchkante und Trümmer
            ctx.setIfOpen(x + (salt % 2 == 0 ? 1 : 0), cy + height, z + (salt % 2 == 0 ? 0 : 1), ModBlocks.STORMSTONE.slab().get().defaultBlockState());
            double fx = Math.cos(angle + 1.3);
            double fz = Math.sin(angle + 1.3);
            for (int k = 2; k < 7; k++) {
                if (ctx.rand(salt, k, 723) < 0.6) {
                    int tx = x + (int) Math.round(fx * k);
                    int tz = z + (int) Math.round(fz * k);
                    ctx.setIfOpen(tx, cy, tz, ctx.rand(salt, k, 724) < 0.5 ? stone : Blocks.COBBLED_DEEPSLATE.defaultBlockState());
                }
            }
            return;
        }
        ctx.set(x, cy + height, z, Blocks.COPPER_BLOCK.waxed().oxidized().defaultBlockState());
        ctx.set(x, cy + height + 1, z, Blocks.LIGHTNING_ROD.waxed().oxidized().defaultBlockState());
        ctx.setIfOpen(x, cy + height + 2, z, Blocks.LIGHTNING_ROD.waxed().oxidized().defaultBlockState());
    }

    /**
     * Die verfallene Sturmwarte am Plateaurand: ein runder Turm aus Tiefenschiefer-Ziegeln mit eingestürzter Krone
     * (an einer Seite noch elf Blöcke hoch), Kupfergittern vor den Fenstern, einem Rest der grün angelaufenen Kuppel
     * und einem verbogenen Blitzableiter. Drinnen: eine Truhe der Sturmwächter, eine Laterne, ein halb eingestürzter
     * Zwischenboden; vor der Tür liegen Trümmer und Fundstellen.
     */
    private void watchtower(LairContext ctx, int cx, int cy, int cz, double a0) {
        double pr = plateauRadius(ctx, a0);
        int tx = cx + (int) Math.round(Math.cos(a0) * (pr + 0.5));
        int tz = cz + (int) Math.round(Math.sin(a0) * (pr + 0.5));
        if (!ctx.near(tx, tz, 14)) {
            return;
        }
        double inward = a0 + Math.PI;
        BlockState bricks = Blocks.DEEPSLATE_BRICKS.defaultBlockState();
        BlockState cracked = Blocks.CRACKED_DEEPSLATE_BRICKS.defaultBlockState();
        BlockState tiles = Blocks.DEEPSLATE_TILES.defaultBlockState();
        for (int dx = -5; dx <= 5; dx++) {
            for (int dz = -5; dz <= 5; dz++) {
                double d = Math.sqrt(dx * dx + dz * dz);
                if (d > 4.4) {
                    continue;
                }
                int x = tx + dx;
                int z = tz + dz;
                // Fundament bis tief in den Fels
                for (int y = cy - 14; y < cy; y++) {
                    ctx.set(x, y, z, y == cy - 1 ? Blocks.POLISHED_DEEPSLATE.defaultBlockState() : bricks);
                }
                double a = Math.atan2(dz, dx);
                if (d > 3.3) {
                    // Mauer: an der Wetterseite höher, sonst eingestürzt
                    int wallTop = towerWallTop(ctx, a, a0);
                    double diff = Math.abs(Mth.wrapDegrees((a - inward) * Mth.RAD_TO_DEG));
                    for (int h = 0; h < wallTop; h++) {
                        boolean door = diff < 16 && h < 3;
                        boolean window = h == 4 && diff > 60 && Math.floorMod((int) Math.round(a * 4 / Math.PI), 2) == 0;
                        if (door) {
                            ctx.set(x, cy + h, z, Blocks.AIR.defaultBlockState());
                        } else if (window) {
                            ctx.set(x, cy + h, z, Blocks.COPPER_BARS.waxed().oxidized().defaultBlockState());
                        } else {
                            double q = ctx.rand(x, z + h * 13, 741);
                            ctx.set(x, cy + h, z, h == 6 ? tiles : q < 0.25 ? cracked : bricks);
                        }
                    }
                    for (int h = wallTop; h < 14; h++) {
                        ctx.set(x, cy + h, z, Blocks.AIR.defaultBlockState());
                    }
                    // Reste der Kuppel: grün angelaufene Kupferstufen auf der hohen Mauerseite
                    if (Math.cos(a - a0) > 0.8 && ctx.rand(x, z, 743) < 0.7) {
                        Direction facing = Direction.fromYRot(Math.toDegrees(a) - 90.0).getOpposite();
                        ctx.set(x, cy + wallTop, z, Blocks.CUT_COPPER_STAIRS.waxed().oxidized().defaultBlockState()
                                .setValue(StairBlock.FACING, facing).setValue(StairBlock.HALF, Half.BOTTOM));
                    }
                } else {
                    // innen: frei bis auf den halb eingestürzten Zwischenboden
                    for (int h = 0; h < 14; h++) {
                        ctx.set(x, cy + h, z, Blocks.AIR.defaultBlockState());
                    }
                    if (Math.cos(a - a0) > 0.1 && d > 1.2) {
                        ctx.set(x, cy + 5, z, Blocks.DEEPSLATE_TILE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.TOP));
                    }
                }
            }
        }
        // verbogener Blitzableiter auf der höchsten Stelle der Mauer
        int rdx = (int) Math.round(Math.cos(a0) * 3.9);
        int rdz = (int) Math.round(Math.sin(a0) * 3.9);
        int rTop = towerWallTop(ctx, Math.atan2(rdz, rdx), a0);
        ctx.set(tx + rdx, cy + rTop, tz + rdz, Blocks.LIGHTNING_ROD.waxed().oxidized().defaultBlockState());
        ctx.set(tx + rdx, cy + rTop + 1, tz + rdz, Blocks.COPPER_CHAIN.waxed().oxidized().defaultBlockState());
        // drinnen: Truhe, Laterne, ein umgestürzter Stuhl aus Treppen
        double bx = Math.cos(a0);
        double bz = Math.sin(a0);
        int chestX = tx + (int) Math.round(bx * 2);
        int chestZ = tz + (int) Math.round(bz * 2);
        Direction toCenter = Direction.fromYRot(Math.toDegrees(inward) - 90);
        if (ctx.inside(chestX, cy, chestZ)) {
            ctx.set(chestX, cy, chestZ, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, toCenter));
            if (ctx.level.getBlockEntity(new BlockPos(chestX, cy, chestZ)) instanceof ChestBlockEntity chest) {
                chest.setLootTable(ModLootTables.STORM_WATCHTOWER_CHEST, ctx.seed + 7101L);
            }
        }
        ctx.set(tx, cy + 4, tz, Blocks.COPPER_LANTERN.waxed().oxidized().defaultBlockState().setValue(LanternBlock.HANGING, true));
        ctx.set(tx, cy + 5, tz, Blocks.COPPER_CHAIN.waxed().oxidized().defaultBlockState());
        int sx = tx - (int) Math.round(bz * 2);
        int sz = tz + (int) Math.round(bx * 2);
        ctx.setIfOpen(sx, cy, sz, Blocks.DEEPSLATE_BRICK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, toCenter.getClockWise()));
        ctx.setIfOpen(tx + (int) Math.round(bz * 2), cy, tz - (int) Math.round(bx * 2), Blocks.BARREL.defaultBlockState());
        // Trümmer vor der Tür und Fundstellen im Kies
        double ox = Math.cos(inward);
        double oz = Math.sin(inward);
        for (int k = 0; k < 9; k++) {
            int px = tx + (int) Math.round(ox * (5 + ctx.rand(k, 0, 742) * 4) + (ctx.rand(k, 1, 742) - 0.5) * 6);
            int pz = tz + (int) Math.round(oz * (5 + ctx.rand(k, 0, 742) * 4) + (ctx.rand(k, 2, 742) - 0.5) * 6);
            ctx.setIfOpen(px, cy, pz, ctx.rand(k, 3, 742) < 0.5 ? cracked : Blocks.COBBLED_DEEPSLATE.defaultBlockState());
        }
        for (int k = 0; k < 3; k++) {
            int px = tx + (int) Math.round(ox * (6 + k * 2) - oz * (k - 1) * 3);
            int pz = tz + (int) Math.round(oz * (6 + k * 2) + ox * (k - 1) * 3);
            this.suspicious(ctx, px, cy - 1, pz, 7200L + k);
        }
    }

    /** Höhe der Turmmauer in dieser Richtung (an der Wetterseite bis elf Blöcke, sonst eingestürzt). */
    private static int towerWallTop(LairContext ctx, double angle, double a0) {
        double side = Math.cos(angle - a0);
        return 5 + (int) Math.round(3.0 + 3.0 * side + (ctx.angleNoise(angle, 5.0, 740) - 0.5) * 4.0);
    }

    private static BlockState stoneWall() {
        return java.util.Objects.requireNonNull(ModBlocks.STORMSTONE.wall()).get().defaultBlockState();
    }

    private void suspicious(LairContext ctx, int x, int y, int z, long salt) {
        if (!ctx.inside(x, y, z)) {
            return;
        }
        ctx.set(x, y, z, Blocks.SUSPICIOUS_GRAVEL.defaultBlockState());
        if (ctx.level.getBlockEntity(new BlockPos(x, y, z)) instanceof BrushableBlockEntity brushable) {
            brushable.setLootTable(ModLootTables.DRAGON_NEST_ARCHAEOLOGY.get(DragonType.LIGHTNING), ctx.seed + salt);
        }
    }
}
