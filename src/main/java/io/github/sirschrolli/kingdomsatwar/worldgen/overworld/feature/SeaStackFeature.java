package io.github.sirschrolli.kingdomsatwar.worldgen.overworld.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;

/**
 * Felsnadeln im Meer: eine Gruppe von ein bis drei schroffen Brandungstürmen, die vom Meeresgrund weit über das Wasser
 * ragen. Ihr Gestein ist geschichtet ({@code body} mit Bändern aus {@code band}), an der Wasserlinie hat die Brandung sie
 * eingeschnürt und mit Moos überzogen, oben trägt jede einen grünen Schopf. Manchmal verbindet ein natürlicher
 * Felsbogen zwei Nadeln – ein Felsentor über dem Wasser.
 */
public record SeaStackFeature(BlockState body, BlockState band, BlockState weathered) implements Feature {
    public static final MapCodec<SeaStackFeature> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            BlockState.CODEC.fieldOf("body").forGetter(SeaStackFeature::body),
            BlockState.CODEC.fieldOf("band").forGetter(SeaStackFeature::band),
            BlockState.CODEC.fieldOf("weathered").forGetter(SeaStackFeature::weathered)
    ).apply(i, SeaStackFeature::new));

    @Override
    public MapCodec<SeaStackFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
        int sea = SeaLevel.of(generator);
        int count = 1 + (random.nextFloat() < 0.55f ? 1 : 0) + (random.nextFloat() < 0.3f ? 1 : 0);
        BlockPos first = null;
        int placed = 0;
        for (int i = 0; i < count; i++) {
            int x = origin.getX() + (i == 0 ? 0 : random.nextInt(15) - 7);
            int z = origin.getZ() + (i == 0 ? 0 : random.nextInt(15) - 7);
            int floor = level.getHeight(Heightmap.Types.OCEAN_FLOOR, x, z);
            int depth = sea - floor;
            if (depth < 2 || depth > 20) {
                continue;
            }
            double radius = (i == 0 ? 2.6 : 1.8) + random.nextDouble() * 1.3;
            int above = 6 + random.nextInt(i == 0 ? 12 : 8);
            BlockPos base = new BlockPos(x, floor, z);
            this.stack(level, random, base, sea, radius, above);
            if (first == null) {
                first = base;
            } else if (random.nextFloat() < 0.35f) {
                this.arch(level, random, first, base, sea);
            }
            placed++;
        }
        return placed > 0;
    }

    /** Eine Nadel: Querschnitt wellig (Rauschen je Winkel), oben schmaler, an der Wasserlinie eingeschnürt. */
    private void stack(WorldGenLevel level, RandomSource random, BlockPos base, int sea, double radius, int above) {
        int top = sea + above;
        int height = top - base.getY();
        double leanX = (random.nextDouble() - 0.5) * 0.12;
        double leanZ = (random.nextDouble() - 0.5) * 0.12;
        double[] wobble = new double[8];
        for (int k = 0; k < wobble.length; k++) {
            wobble[k] = 0.75 + random.nextDouble() * 0.5;
        }
        int phase = random.nextInt(3);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int reach = (int) Math.ceil(radius + 2);
        for (int dy = -3; dy <= height; dy++) {
            int y = base.getY() + dy;
            double t = Mth.clamp((double) dy / height, 0.0, 1.0);
            double r = radius * (1.0 - 0.32 * t);
            // die Brandung schnürt die Nadel an der Wasserlinie ein
            int fromSea = Math.abs(y - sea);
            if (fromSea <= 1) {
                r *= 0.82;
            }
            // oben ein abgerundeter Kopf
            if (dy > height - 2) {
                r *= dy == height ? 0.62 : 0.86;
            }
            double cx = base.getX() + 0.5 + leanX * dy;
            double cz = base.getZ() + 0.5 + leanZ * dy;
            for (int dx = -reach; dx <= reach; dx++) {
                for (int dz = -reach; dz <= reach; dz++) {
                    double px = base.getX() + dx + 0.5 - cx;
                    double pz = base.getZ() + dz + 0.5 - cz;
                    double angle = Math.atan2(pz, px);
                    double w = wobble[(int) ((angle + Math.PI) / (2 * Math.PI) * 8) & 7];
                    if (px * px + pz * pz > r * r * w * w) {
                        continue;
                    }
                    pos.set(base.getX() + dx, y, base.getZ() + dz);
                    BlockState here = level.getBlockState(pos);
                    if (dy < 0 && !Kit.ground(here) && !here.is(Blocks.WATER)) {
                        continue;
                    }
                    if (dy >= 0 && !Kit.openOrWater(here) && !Kit.ground(here)) {
                        continue;
                    }
                    BlockState state = (y + phase) % 3 == 0 ? this.band : this.body;
                    if (fromSea <= 1 && random.nextFloat() < 0.55f || y < sea - 1 && random.nextFloat() < 0.18f) {
                        state = this.weathered;
                    }
                    level.setBlock(pos, state, 2);
                }
            }
        }
        // der grüne Schopf: Gras und Moos auf den obersten Blöcken, ein paar Halme
        int topReach = (int) Math.ceil(radius);
        for (int dx = -topReach; dx <= topReach; dx++) {
            for (int dz = -topReach; dz <= topReach; dz++) {
                int x = base.getX() + dx;
                int z = base.getZ() + dz;
                int y = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z) - 1;
                if (y < sea + 3) {
                    continue;
                }
                pos.set(x, y, z);
                BlockState here = level.getBlockState(pos);
                if (here.is(this.body.getBlock()) || here.is(this.band.getBlock()) || here.is(this.weathered.getBlock())) {
                    level.setBlock(pos, random.nextFloat() < 0.75f ? Blocks.GRASS_BLOCK.defaultBlockState() : Blocks.MOSS_BLOCK.defaultBlockState(), 2);
                    BlockPos up = pos.above();
                    if (level.getBlockState(up).isAir() && random.nextFloat() < 0.45f) {
                        level.setBlock(up, random.nextFloat() < 0.75f ? Blocks.SHORT_GRASS.defaultBlockState() : Blocks.FERN.defaultBlockState(), 2);
                    }
                }
            }
        }
    }

    /** Ein Felsbogen zwischen zwei Nadeln: ein flacher Bogen knapp über dem Wasser, oben bewachsen. */
    private void arch(WorldGenLevel level, RandomSource random, BlockPos a, BlockPos b, int sea) {
        double dx = b.getX() - a.getX();
        double dz = b.getZ() - a.getZ();
        double length = Math.sqrt(dx * dx + dz * dz);
        if (length < 5 || length > 13) {
            return;
        }
        int rise = 3 + random.nextInt(3);
        int steps = (int) (length * 2);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int s = 0; s <= steps; s++) {
            double u = (double) s / steps;
            double x = a.getX() + 0.5 + dx * u;
            double z = a.getZ() + 0.5 + dz * u;
            double y = sea + rise + 2.5 * Math.sin(Math.PI * u);
            double thick = 1.4 + 0.6 * (1 - Math.sin(Math.PI * u));
            for (int ox = -1; ox <= 1; ox++) {
                for (int oz = -1; oz <= 1; oz++) {
                    for (int oy = -1; oy <= 1; oy++) {
                        if (ox * ox + oz * oz + oy * oy > thick * thick) {
                            continue;
                        }
                        pos.set(Mth.floor(x) + ox, Mth.floor(y) + oy, Mth.floor(z) + oz);
                        if (Kit.openOrWater(level.getBlockState(pos))) {
                            level.setBlock(pos, (pos.getY() % 3 == 0) ? this.band : this.body, 2);
                        }
                    }
                }
            }
        }
    }
}
