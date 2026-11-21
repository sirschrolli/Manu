package io.github.sirschrolli.kingdomsatwar.voidrealm.feature;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;

/** Schwebende Trümmer der Alten (Säulentrommeln, Mauerstücke, Stufen) frei über einem Bruchstück. */
public record FloatingDebrisFeature() implements Feature {
    public static final MapCodec<FloatingDebrisFeature> CODEC = MapCodec.unit(FloatingDebrisFeature::new);

    @Override
    public MapCodec<FloatingDebrisFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
        if (!level.getBlockState(origin.below()).isSolidRender()) {
            return false;
        }
        VoidRuinFeature.debrisOnly(level, origin, random, 2 + random.nextInt(3));
        return true;
    }
}
