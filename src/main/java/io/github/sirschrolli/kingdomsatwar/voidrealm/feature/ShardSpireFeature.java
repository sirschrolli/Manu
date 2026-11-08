package io.github.sirschrolli.kingdomsatwar.voidrealm.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.sirschrolli.kingdomsatwar.registry.VoidBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.phys.Vec3;

/**
 * Kristallspitze: Ein riesiger Splitter aus Leerenkristall ragt schräg aus dem Fels in die Leere – unten breit und mit
 * Leerenstein ummantelt, oben spitz und leuchtend; um seinen Fuß stehen kleinere Splitter. Hängende Spitzen wachsen von
 * der Unterseite der Bruchstücke abwärts ins Nichts.
 */
public record ShardSpireFeature(int minLength, int maxLength, float radius, boolean hanging) implements Feature {
    public static final MapCodec<ShardSpireFeature> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Codec.intRange(3, 48).fieldOf("min_length").forGetter(ShardSpireFeature::minLength),
            Codec.intRange(3, 48).fieldOf("max_length").forGetter(ShardSpireFeature::maxLength),
            Codec.floatRange(0.5f, 6.0f).fieldOf("radius").forGetter(ShardSpireFeature::radius),
            Codec.BOOL.optionalFieldOf("hanging", false).forGetter(ShardSpireFeature::hanging)
    ).apply(i, ShardSpireFeature::new));

    @Override
    public MapCodec<ShardSpireFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
        BlockPos anchor = this.hanging ? origin.above() : origin.below();
        if (!level.getBlockState(anchor).isSolidRender()) {
            return false;
        }
        int length = this.minLength + random.nextInt(Math.max(1, this.maxLength - this.minLength + 1));
        double tilt = Math.toRadians(8 + random.nextDouble() * 32);
        double heading = random.nextDouble() * Math.PI * 2;
        double up = this.hanging ? -1 : 1;
        Vec3 dir = new Vec3(Math.sin(tilt) * Math.cos(heading), Math.cos(tilt) * up, Math.sin(tilt) * Math.sin(heading));
        Vec3 base = Vec3.atBottomCenterOf(origin).add(0, this.hanging ? 0.5 : -1.5, 0);
        spire(level, origin, base, dir, length, this.radius * (0.8 + random.nextFloat() * 0.4), random);
        // Nebensplitter am Fuß
        int small = 2 + random.nextInt(3);
        for (int k = 0; k < small; k++) {
            double a = heading + Math.PI * 2 * k / small + random.nextDouble();
            Vec3 at = base.add(Math.cos(a) * (this.radius + 1.5), 0, Math.sin(a) * (this.radius + 1.5));
            double t2 = Math.toRadians(15 + random.nextDouble() * 35);
            Vec3 d2 = new Vec3(Math.sin(t2) * Math.cos(a), Math.cos(t2) * up, Math.sin(t2) * Math.sin(a));
            spire(level, origin, at, d2, Math.max(3, length / 3 + random.nextInt(3)), Math.max(0.7, this.radius * 0.4), random);
        }
        return true;
    }

    private static void spire(WorldGenLevel level, BlockPos origin, Vec3 base, Vec3 dir, int length, double radius, RandomSource random) {
        BlockState crystal = VoidBlocks.VOID_CRYSTAL.get().defaultBlockState();
        BlockState stone = VoidBlocks.VOID_STONE.base().get().defaultBlockState();
        for (int i = 0; i <= length * 2; i++) {
            double t = i / (double) (length * 2);
            Vec3 c = base.add(dir.scale(t * length));
            double r = radius * (1.0 - t) + 0.35;
            int ir = (int) Math.ceil(r);
            for (int dx = -ir; dx <= ir; dx++) {
                for (int dy = -ir; dy <= ir; dy++) {
                    for (int dz = -ir; dz <= ir; dz++) {
                        double d2 = dx * dx + dy * dy + dz * dz;
                        if (d2 > r * r) {
                            continue;
                        }
                        BlockPos pos = BlockPos.containing(c.x + dx, c.y + dy, c.z + dz);
                        boolean shell = t < 0.35 && d2 > (r - 1.0) * (r - 1.0) && random.nextFloat() < 0.75f;
                        VoidBuild.place(level, origin, pos, shell ? stone : crystal);
                    }
                }
            }
        }
    }
}
