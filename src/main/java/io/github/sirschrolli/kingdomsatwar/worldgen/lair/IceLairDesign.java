package io.github.sirschrolli.kingdomsatwar.worldgen.lair;

import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import io.github.sirschrolli.kingdomsatwar.loot.ModLootTables;
import io.github.sirschrolli.kingdomsatwar.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.entity.BrushableBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;

/**
 * Glaciaras Gletscherkessel: ein runder Kessel zwischen Eiswänden (bis 20 Blöcke hoch) mit Gesimsen, unter denen
 * Eiszapfen hängen, und Eisnadeln auf dem Grat. Der Boden ist ein zugefrorener See aus Pack- und Blaueis mit
 * Schneefeldern; in der Mitte steht der riesige Eiskristall, in dem die Frostkönigin schläft. Dazu erfrorene
 * Säulenreste, ein im Eis eingeschlossenes Gerippe, leuchtende Kristallgruppen und eine Schlucht als Zugang. Ringsum
 * liegt der Frostkessel ({@link FrostZone}).
 */
public final class IceLairDesign implements LairDesign {
    private static final int R = 46;
    private static final int TOP = 44;
    private final FrostZone zone = new FrostZone(this, R);

    @Override
    public int radius() {
        return R;
    }

    @Override
    public int zoneRadius() {
        return 116;
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
        return Mth.clamp(avg - 2, context.chunkGenerator().getSeaLevel() + 2, 200);
    }

    private static double wallRadius(LairContext ctx, double angle) {
        return 31.0 + (ctx.angleNoise(angle, 2.4, 211) - 0.5) * 8.0;
    }

    private static double wallHeight(LairContext ctx, double angle) {
        return 15.0 + (ctx.angleNoise(angle, 3.3, 212) - 0.5) * 10.0;
    }

    private static double lakeRadius(LairContext ctx, double angle) {
        return 17.0 + (ctx.angleNoise(angle, 2.0, 213) - 0.5) * 5.0;
    }

    private double gorgeAngle(LairContext ctx) {
        return ctx.rand(0, 0, 299) * Mth.TWO_PI;
    }

    @Override
    public void build(LairContext ctx) {
        int cx = ctx.center.getX();
        int cy = ctx.center.getY();
        int cz = ctx.center.getZ();
        double gorge = this.gorgeAngle(ctx);
        for (int x = Math.max(ctx.box.minX(), cx - R); x <= Math.min(ctx.box.maxX(), cx + R); x++) {
            for (int z = Math.max(ctx.box.minZ(), cz - R); z <= Math.min(ctx.box.maxZ(), cz + R); z++) {
                double dx = x - cx;
                double dz = z - cz;
                double d = Math.sqrt(dx * dx + dz * dz);
                if (d <= R) {
                    this.column(ctx, x, z, d, Math.atan2(dz, dx), gorge, cy);
                }
            }
        }
        this.features(ctx, cx, cy, cz, gorge);
    }

    private void column(LairContext ctx, int x, int z, double d, double angle, double gorge, int cy) {
        int top0 = ctx.originalHeight(x, z, false) - 1;
        int h0 = ctx.groundY(x, z);
        double wall = wallRadius(ctx, angle);
        double gorgeDiff = Math.abs(Mth.wrapDegrees((angle - gorge) * Mth.RAD_TO_DEG)) * Mth.DEG_TO_RAD;
        double gorgeF = gorgeDiff < 0.13 ? 1.0 : gorgeDiff < 0.26 ? 1.0 - (gorgeDiff - 0.13) / 0.13 : 0.0;
        double n = ctx.noise(x, z, 0.09, 221);
        int top = cy + TOP;
        double floorEdge = wall - 5.0;
        if (d < floorEdge) {
            int hs = cy - 1;
            ctx.fill(x, Math.min(h0, hs) - 6, hs - 1, z, below(ctx, x, z));
            ctx.set(x, hs, z, this.floor(ctx, x, z, d, angle));
            ctx.fill(x, hs + 1, top, z, Blocks.AIR.defaultBlockState());
            if (d > lakeRadius(ctx, angle) + 1 && ctx.get(x, hs, z).is(Blocks.SNOW_BLOCK) && n > 0.55) {
                ctx.set(x, hs + 1, z, Blocks.SNOW.defaultBlockState().setValue(SnowLayerBlock.LAYERS, 1 + (int) ((n - 0.55) * 8)));
            }
            return;
        }
        double wallH = wallHeight(ctx, angle) * (1.0 - gorgeF) + 1.0 * gorgeF;
        int hs;
        if (d < wall) {
            // steile Eiswand
            double t = (d - floorEdge) / 5.0;
            hs = cy - 1 + (int) Math.round(wallH * Mth.clamp(t * 1.6, 0, 1) + (n - 0.5) * 2.0);
        } else {
            double u = Mth.clamp((d - wall) / (R - wall), 0, 1);
            double outer = Mth.lerp(LairContext.smooth(u), cy - 1 + wallH, h0);
            if (gorgeF > 0) {
                outer = Mth.lerp(gorgeF, outer, Mth.lerp(u, cy - 1, h0));
            }
            hs = (int) Math.round(outer + (n - 0.5) * 2.0);
        }
        int bottom = Math.min(h0, hs) - 6;
        for (int y = bottom; y <= hs; y++) {
            ctx.set(x, y, z, this.wallBlock(ctx, x, y, z, hs, cy, d >= wall));
        }
        int clearTo = d < wall ? top : Math.max(Math.max(h0, hs) + 2, top0 + 1);
        ctx.fill(x, hs + 1, clearTo, z, Blocks.AIR.defaultBlockState());
        if (ctx.get(x, hs, z).is(Blocks.SNOW_BLOCK) && ctx.rand(x, z, 222) < 0.4) {
            ctx.set(x, hs + 1, z, Blocks.SNOW.defaultBlockState().setValue(SnowLayerBlock.LAYERS, 1 + (int) (ctx.rand(x, z, 223) * 3)));
        }
    }

    private static BlockState below(LairContext ctx, int x, int z) {
        return ctx.rand(x, z, 224) < 0.4 ? Blocks.PACKED_ICE.defaultBlockState() : ModBlocks.RIME_STONE.base().get().defaultBlockState();
    }

    /** Boden: zugefrorener See (Pack-, Blau- und Gletschereis mit Rissen), außen Schneefelder und Raureif. */
    private BlockState floor(LairContext ctx, int x, int z, double d, double angle) {
        double lake = lakeRadius(ctx, angle);
        double r = ctx.rand(x, z, 225);
        if (d < lake) {
            double crack = Math.abs(Math.sin(angle * 4.0 + ctx.noise(x, z, 0.06, 226) * 5.0));
            if (crack < 0.06 && d > 6) {
                return Blocks.BLUE_ICE.defaultBlockState();
            }
            return r < 0.55 ? Blocks.PACKED_ICE.defaultBlockState() : r < 0.85 ? ModBlocks.GLACIER_ICE.get().defaultBlockState()
                    : Blocks.BLUE_ICE.defaultBlockState();
        }
        if (d < lake + 1.5) {
            return ModBlocks.GLACIER_ICE.get().defaultBlockState();
        }
        double n = ctx.noise(x, z, 0.12, 227);
        if (n > 0.68) {
            return ModBlocks.RIME_STONE.base().get().defaultBlockState();
        }
        return r < 0.85 ? Blocks.SNOW_BLOCK.defaultBlockState() : Blocks.PACKED_ICE.defaultBlockState();
    }

    /** Eiswand: Schichten aus Pack-, Blau- und Gletschereis über Raureifstein, oben Schnee. */
    private BlockState wallBlock(LairContext ctx, int x, int y, int z, int hs, int cy, boolean outer) {
        if (y == hs) {
            return outer ? Blocks.SNOW_BLOCK.defaultBlockState()
                    : (ctx.rand(x, z, 228) < 0.5 ? Blocks.SNOW_BLOCK.defaultBlockState() : Blocks.PACKED_ICE.defaultBlockState());
        }
        int rel = y - cy;
        double band = ctx.noise(x * 0.25 + z * 0.25, y, 0.45, 229);
        if (rel < 1) {
            return ModBlocks.RIME_STONE.base().get().defaultBlockState();
        }
        if (band < 0.32) {
            return Blocks.BLUE_ICE.defaultBlockState();
        } else if (band < 0.62) {
            return Blocks.PACKED_ICE.defaultBlockState();
        } else if (band < 0.8) {
            return ModBlocks.GLACIER_ICE.get().defaultBlockState();
        }
        return ModBlocks.RIME_STONE.base().get().defaultBlockState();
    }

    // ------------------------------------------------------------------ Einzelheiten

    private void features(LairContext ctx, int cx, int cy, int cz, double gorge) {
        // der große Eiskristall in der Mitte (Glaciara schläft darin)
        this.bigCrystal(ctx, cx, cy, cz);
        // Gesimse mit Eiszapfen an der Innenseite der Wand
        for (int k = 0; k < 40; k++) {
            double a = k * Mth.TWO_PI / 40 + (ctx.rand(k, 1, 240) - 0.5) * 0.1;
            double diff = Math.abs(Mth.wrapDegrees((a - gorge) * Mth.RAD_TO_DEG)) * Mth.DEG_TO_RAD;
            if (diff < 0.35 || ctx.rand(k, 2, 240) < 0.35) {
                continue;
            }
            double wr = wallRadius(ctx, a) - 5.0;
            int lx = cx + (int) Math.round(Math.cos(a) * wr);
            int lz = cz + (int) Math.round(Math.sin(a) * wr);
            int wh = (int) Math.round(wallHeight(ctx, a)) - 3;
            int sy = cy - 1 + Math.max(5, wh);
            if (ctx.inside(lx, sy, lz) || ctx.inside(lx - (int) Math.round(Math.cos(a)), sy, lz - (int) Math.round(Math.sin(a)))) {
                this.cornice(ctx, lx, sy, lz, a, cx, cz);
            }
        }
        // Eisnadeln auf dem Grat
        for (int k = 0; k < 9; k++) {
            double a = k * Mth.TWO_PI / 9 + (ctx.rand(k, 3, 241) - 0.5) * 0.4;
            double diff = Math.abs(Mth.wrapDegrees((a - gorge) * Mth.RAD_TO_DEG)) * Mth.DEG_TO_RAD;
            if (diff < 0.4) {
                continue;
            }
            double wr = wallRadius(ctx, a) + 1.0;
            int sx = cx + (int) Math.round(Math.cos(a) * wr);
            int sz = cz + (int) Math.round(Math.sin(a) * wr);
            if (ctx.inside(sx, cy, sz)) {
                int base = ctx.originalHeight(sx, sz, false);
                this.spike(ctx, sx, base, sz, 8 + (int) (ctx.rand(k, 4, 241) * 12));
            }
        }
        // erfrorene Säulenreste und Kristallgruppen auf dem Kesselboden
        for (int k = 0; k < 6; k++) {
            double a = ctx.rand(k, 5, 242) * Mth.TWO_PI;
            double rr = 12 + ctx.rand(k, 6, 242) * 10;
            int px = cx + (int) Math.round(Math.cos(a) * rr);
            int pz = cz + (int) Math.round(Math.sin(a) * rr);
            if (k % 2 == 0) {
                this.brokenPillar(ctx, px, cy, pz, 2 + (int) (ctx.rand(k, 7, 242) * 5));
            } else {
                this.crystalCluster(ctx, px, cy, pz, k);
            }
        }
        // ein Gerippe, im Eis eingeschlossen, daneben Fundstücke im Kies
        double a = ctx.rand(0, 8, 243) * Mth.TWO_PI;
        int bx = cx + (int) Math.round(Math.cos(a) * 20);
        int bz = cz + (int) Math.round(Math.sin(a) * 20);
        this.frozenBones(ctx, bx, cy, bz);
        for (int j = 0; j < 2; j++) {
            this.suspicious(ctx, bx + (j == 0 ? 3 : -3), cy - 1, bz + (j == 0 ? -2 : 2), 2000L + j);
        }
        // Nestkern unter dem Kristall
        BlockPos core = this.corePos(ctx.center);
        if (ctx.inside(core.getX(), core.getY(), core.getZ())) {
            ctx.set(core.getX(), core.getY(), core.getZ(), ModBlocks.DRAGON_LAIR_CORE.get().defaultBlockState());
            if (ctx.level.getBlockEntity(core) instanceof io.github.sirschrolli.kingdomsatwar.block.entity.DragonLairCoreBlockEntity be) {
                be.setup(DragonType.ICE, ctx.center);
            }
        }
    }

    /** Der Eiskristall: sechseckige, leicht schräge Säule (13 hoch) mit Spitze, umringt von kleineren Kristallen. */
    private void bigCrystal(LairContext ctx, int cx, int cy, int cz) {
        int height = 13;
        double lean = 0.12;
        for (int y = 0; y <= height + 4; y++) {
            double radius = y < height ? 4.4 : 4.4 * (1.0 - (double) (y - height) / 5.0);
            if (radius <= 0.2) {
                break;
            }
            double ox = y * lean;
            int reach = (int) Math.ceil(radius) + 2;
            for (int dx = -reach; dx <= reach; dx++) {
                for (int dz = -reach; dz <= reach; dz++) {
                    double hx = dx - ox;
                    // Sechseck: Abstand über die drei Achsen
                    double hex = Math.max(Math.abs(hx), Math.max(Math.abs(hx * 0.5 + dz * 0.866), Math.abs(hx * 0.5 - dz * 0.866)));
                    if (hex <= radius) {
                        ctx.set(cx + dx, cy + y, cz + dz, ModBlocks.FROST_CRYSTAL.get().defaultBlockState());
                    }
                }
            }
        }
        // kleinere Kristalle ringsum
        for (int k = 0; k < 7; k++) {
            double a = k * Mth.TWO_PI / 7 + ctx.rand(k, 9, 244) * 0.5;
            double rr = 6.5 + ctx.rand(k, 10, 244) * 2.5;
            int sx = cx + (int) Math.round(Math.cos(a) * rr);
            int sz = cz + (int) Math.round(Math.sin(a) * rr);
            int h = 2 + (int) (ctx.rand(k, 11, 244) * 5);
            for (int y = 0; y < h; y++) {
                int shift = (int) Math.round(y * 0.3 * Math.cos(a));
                int shiftZ = (int) Math.round(y * 0.3 * Math.sin(a));
                ctx.set(sx + shift, cy + y, sz + shiftZ, ModBlocks.FROST_CRYSTAL.get().defaultBlockState());
                if (y < h - 1 && h > 3) {
                    ctx.set(sx + shift + 1, cy + y, sz + shiftZ, ModBlocks.FROST_CRYSTAL.get().defaultBlockState());
                }
            }
        }
    }

    /** Gesims aus Packeis, das zur Kesselmitte hin vorragt; darunter hängen Eiszapfen. */
    private void cornice(LairContext ctx, int x, int y, int z, double angle, int cx, int cz) {
        int ix = (int) Math.round(-Math.cos(angle));
        int iz = (int) Math.round(-Math.sin(angle));
        for (int k = 0; k <= 1; k++) {
            int ox = x + ix * k;
            int oz = z + iz * k;
            ctx.set(ox, y, oz, Blocks.PACKED_ICE.defaultBlockState());
            if (ctx.get(ox, y + 1, oz).isAir()) {
                ctx.set(ox, y + 1, oz, Blocks.SNOW.defaultBlockState());
            }
            int length = 1 + (int) (ctx.rand(ox, oz, 245) * 3.5);
            for (int j = 1; j <= length; j++) {
                if (ctx.get(ox, y - j, oz).isAir()) {
                    ctx.set(ox, y - j, oz, ModBlocks.ICICLE.get().defaultBlockState());
                } else {
                    break;
                }
            }
        }
    }

    private void spike(LairContext ctx, int x, int y, int z, int height) {
        double base = 1.3 + height * 0.08;
        int reach = (int) Math.ceil(base) + 1;
        for (int k = -2; k < height; k++) {
            double r = base * (1.0 - (double) Math.max(0, k) / (height + 1)) + 0.3;
            for (int dx = -reach; dx <= reach; dx++) {
                for (int dz = -reach; dz <= reach; dz++) {
                    if (dx * dx + dz * dz <= r * r) {
                        boolean core = dx == 0 && dz == 0 && k < height - 2;
                        ctx.set(x + dx, y + k, z + dz, core ? Blocks.BLUE_ICE.defaultBlockState() : Blocks.PACKED_ICE.defaultBlockState());
                    }
                }
            }
        }
    }

    private void brokenPillar(LairContext ctx, int x, int cy, int z, int height) {
        for (int y = 0; y < height; y++) {
            BlockState state = ctx.rand(x, z + y, 246) < 0.7 ? ModBlocks.RIME_STONE.wall().get().defaultBlockState()
                    : ModBlocks.RIME_STONE.base().get().defaultBlockState();
            if (y == 0) {
                state = ModBlocks.RIME_STONE.base().get().defaultBlockState();
            }
            ctx.set(x, cy + y, z, state);
        }
        ctx.set(x, cy + height, z, Blocks.SNOW.defaultBlockState().setValue(SnowLayerBlock.LAYERS, 2));
        // abgebrochenes Stück daneben
        ctx.set(x + 1, cy, z + 1, ModBlocks.RIME_STONE.slab().get().defaultBlockState());
    }

    private void crystalCluster(LairContext ctx, int x, int cy, int z, int salt) {
        for (int i = 0; i < 4; i++) {
            int ox = (int) Math.round((ctx.rand(salt, i, 247) - 0.5) * 3);
            int oz = (int) Math.round((ctx.rand(i, salt, 247) - 0.5) * 3);
            int h = 1 + (int) (ctx.rand(salt + i, 3, 247) * 4);
            for (int y = 0; y < h; y++) {
                ctx.set(x + ox, cy + y, z + oz, ModBlocks.FROST_CRYSTAL.get().defaultBlockState());
            }
        }
    }

    /** Gerippe im Eis: Knochen, umschlossen von Packeis (wie in einem Eisblock eingefroren). */
    private void frozenBones(LairContext ctx, int bx, int cy, int bz) {
        Direction.Axis axis = ctx.rand(bx, bz, 248) < 0.5 ? Direction.Axis.X : Direction.Axis.Z;
        boolean alongX = axis == Direction.Axis.X;
        BlockState spine = Blocks.BONE_BLOCK.defaultBlockState().setValue(RotatedPillarBlock.AXIS, axis);
        for (int i = -4; i <= 4; i++) {
            int x = bx + (alongX ? i : 0);
            int z = bz + (alongX ? 0 : i);
            ctx.set(x, cy, z, spine);
            ctx.set(x, cy + 1, z, Blocks.PACKED_ICE.defaultBlockState());
            if (i % 2 == 0 && Math.abs(i) < 4) {
                for (int side = -1; side <= 1; side += 2) {
                    int rx = x + (alongX ? 0 : side * 2);
                    int rz = z + (alongX ? side * 2 : 0);
                    ctx.set(rx, cy, rz, Blocks.BONE_BLOCK.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y));
                    ctx.set(rx, cy + 1, rz, Blocks.PACKED_ICE.defaultBlockState());
                }
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
            brushable.setLootTable(ModLootTables.DRAGON_NEST_ARCHAEOLOGY.get(DragonType.ICE), ctx.seed + salt);
        }
    }
}
