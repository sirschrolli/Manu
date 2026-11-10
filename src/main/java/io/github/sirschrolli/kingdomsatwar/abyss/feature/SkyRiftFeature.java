package io.github.sirschrolli.kingdomsatwar.abyss.feature;

import com.mojang.serialization.MapCodec;
import io.github.sirschrolli.kingdomsatwar.registry.AbyssBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;

/**
 * Ein <b>Himmelsriss</b>: hoch über dem Chaos ist die Welt selbst gesprungen – ein zackiger, weißglühender Riss mit roten
 * Rändern, der sich quer über den Himmel zieht und sich verästelt. Von unten sieht man viele davon; wer zu nah heranfliegt,
 * verbrennt sich.
 */
public record SkyRiftFeature() implements Feature {
    public static final MapCodec<SkyRiftFeature> CODEC = MapCodec.unit(SkyRiftFeature::new);

    @Override
    public MapCodec<SkyRiftFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
        BlockState rift = AbyssBlocks.SKY_RIFT.get().defaultBlockState();
        double heading = random.nextDouble() * Math.PI;
        double dx = Math.cos(heading);
        double dz = Math.sin(heading);
        int segments = 5 + random.nextInt(5);
        // von der Mitte aus in beide Richtungen
        for (int sign = -1; sign <= 1; sign += 2) {
            double x = origin.getX() + 0.5;
            double y = origin.getY() + 0.5;
            double z = origin.getZ() + 0.5;
            double width = 2.2;
            for (int s = 0; s < segments; s++) {
                int len = 2 + random.nextInt(3);
                double turn = (random.nextDouble() - 0.5) * 0.9;
                double sx = (dx * Math.cos(turn) - dz * Math.sin(turn)) * sign;
                double sz = (dx * Math.sin(turn) + dz * Math.cos(turn)) * sign;
                double sy = (random.nextDouble() - 0.5) * 1.2;
                for (int i = 0; i < len; i++) {
                    x += sx;
                    y += sy;
                    z += sz;
                    stroke(level, origin, rift, x, y, z, width);
                }
                width = Math.max(0.6, width * 0.82);
                // Verästelung
                if (random.nextInt(3) == 0) {
                    double bx = x;
                    double by = y;
                    double bz = z;
                    double ba = heading + (random.nextBoolean() ? 1 : -1) * (0.6 + random.nextDouble() * 0.6);
                    for (int i = 0; i < 2 + random.nextInt(4); i++) {
                        bx += Math.cos(ba) * sign;
                        bz += Math.sin(ba) * sign;
                        by += (random.nextDouble() - 0.5) * 1.4;
                        stroke(level, origin, rift, bx, by, bz, 0.6);
                    }
                }
            }
        }
        return true;
    }

    private static void stroke(WorldGenLevel level, BlockPos origin, BlockState rift, double x, double y, double z, double width) {
        int r = Mth.ceil(width);
        for (int ox = -r; ox <= r; ox++) {
            for (int oy = -r; oy <= r; oy++) {
                for (int oz = -r; oz <= r; oz++) {
                    if (ox * ox + oy * oy * 2.0 + oz * oz > width * width) {
                        continue;
                    }
                    AbyssBuild.place(level, origin, BlockPos.containing(x + ox, y + oy, z + oz), rift);
                }
            }
        }
    }
}
