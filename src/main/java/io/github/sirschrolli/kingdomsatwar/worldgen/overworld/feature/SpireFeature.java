package io.github.sirschrolli.kingdomsatwar.worldgen.overworld.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;

/**
 * Eine Nadel, die sich nach oben verjüngt und leicht neigt: Eisnadeln auf den Frostgipfeln, Felsnadeln in den
 * Felsschluchten, Brandungstürme an der Sturmküste. Der Körper ({@code body}) trägt alle {@code bandEvery} Blöcke ein
 * zwei Blöcke hohes Band ({@code band}), auf der Spitze sitzt {@code tip}. Der Fuß reicht drei Blöcke tief in den Boden;
 * mit {@code inWater} steht die Nadel auch im Wasser (dann vom Grund aus).
 */
public record SpireFeature(BlockState body, BlockState band, int bandEvery, BlockState tip, int minHeight, int maxHeight, float baseRadius, float lean,
        boolean inWater) implements Feature {
    public static final MapCodec<SpireFeature> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            BlockState.CODEC.fieldOf("body").forGetter(SpireFeature::body),
            BlockState.CODEC.fieldOf("band").forGetter(SpireFeature::band),
            Codec.intRange(2, 64).fieldOf("band_every").forGetter(SpireFeature::bandEvery),
            BlockState.CODEC.fieldOf("tip").forGetter(SpireFeature::tip),
            Codec.intRange(1, 64).fieldOf("min_height").forGetter(SpireFeature::minHeight),
            Codec.intRange(1, 64).fieldOf("max_height").forGetter(SpireFeature::maxHeight),
            Codec.floatRange(0.5f, 8.0f).fieldOf("base_radius").forGetter(SpireFeature::baseRadius),
            Codec.floatRange(0.0f, 1.0f).fieldOf("lean").forGetter(SpireFeature::lean),
            Codec.BOOL.fieldOf("in_water").forGetter(SpireFeature::inWater)
    ).apply(i, SpireFeature::new));

    @Override
    public MapCodec<SpireFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
        if (!this.inWater && !level.getFluidState(origin).isEmpty() || !Kit.ground(level.getBlockState(origin.below()))) {
            return false;
        }
        int height = this.minHeight + random.nextInt(Math.max(1, this.maxHeight - this.minHeight + 1));
        if (this.inWater) {
            // im Wasser muss die Nadel gut über die Oberfläche ragen
            int water = 0;
            while (water < 24 && !level.getFluidState(origin.above(water)).isEmpty()) {
                water++;
            }
            height += water;
        }
        double radius = this.baseRadius * (0.8 + random.nextDouble() * 0.4);
        double angle = random.nextDouble() * Math.PI * 2.0;
        double leanX = Math.cos(angle) * this.lean * random.nextDouble();
        double leanZ = Math.sin(angle) * this.lean * random.nextDouble();
        int phase = random.nextInt(this.bandEvery);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        double cx = origin.getX() + 0.5;
        double cz = origin.getZ() + 0.5;
        for (int y = -3; y < height; y++) {
            double t = Math.max(0, y) / (double) height;
            // unten bauchig, oben spitz
            double r = radius * Math.pow(1.0 - t, 0.85) + 0.35;
            double x0 = cx + leanX * Math.max(0, y);
            double z0 = cz + leanZ * Math.max(0, y);
            int ir = (int) Math.ceil(r);
            boolean banded = Math.floorMod(y + phase, this.bandEvery) < 2;
            for (int dx = -ir; dx <= ir; dx++) {
                for (int dz = -ir; dz <= ir; dz++) {
                    double px = Math.floor(x0) + dx + 0.5 - x0;
                    double pz = Math.floor(z0) + dz + 0.5 - z0;
                    double d = px * px + pz * pz;
                    if (d > r * r * (1.0 - 0.12 * random.nextDouble())) {
                        continue;
                    }
                    pos.set(Math.floor(x0) + dx, origin.getY() + y, Math.floor(z0) + dz);
                    BlockState old = level.getBlockState(pos);
                    boolean below = y < 0;
                    if (below ? !Kit.ground(old) && !Kit.openOrWater(old) : !(this.inWater ? Kit.openOrWater(old) : Kit.open(old))) {
                        continue;
                    }
                    level.setBlock(pos, banded ? this.band : this.body, 2);
                }
            }
        }
        BlockPos top = BlockPos.containing(cx + leanX * height, origin.getY() + height, cz + leanZ * height);
        if (Kit.open(level.getBlockState(top))) {
            level.setBlock(top, this.tip, 2);
        }
        return true;
    }
}
