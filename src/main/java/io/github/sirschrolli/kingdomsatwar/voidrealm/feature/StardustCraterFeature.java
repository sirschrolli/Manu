package io.github.sirschrolli.kingdomsatwar.voidrealm.feature;

import com.mojang.serialization.MapCodec;
import io.github.sirschrolli.kingdomsatwar.registry.VoidBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;

/**
 * Sternenkrater: eine flache Mulde im Sternenstaub mit aufgeworfenem Rand – in ihrer Mitte steckt ein gefallener Stern,
 * ein Klumpen aus Leerenkristall, um den Drusen wachsen.
 */
public record StardustCraterFeature() implements Feature {
    public static final MapCodec<StardustCraterFeature> CODEC = MapCodec.unit(StardustCraterFeature::new);

    @Override
    public MapCodec<StardustCraterFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
        BlockPos ground = origin.below();
        if (!level.getBlockState(ground).is(VoidBlocks.STARDUST.get())) {
            return false;
        }
        BlockState dust = VoidBlocks.STARDUST.get().defaultBlockState();
        BlockState crystal = VoidBlocks.VOID_CRYSTAL.get().defaultBlockState();
        int radius = 3 + random.nextInt(4);
        int depth = Math.max(1, radius / 2);
        for (int dx = -radius - 1; dx <= radius + 1; dx++) {
            for (int dz = -radius - 1; dz <= radius + 1; dz++) {
                double d = Math.sqrt(dx * dx + dz * dz);
                BlockPos column = ground.offset(dx, 0, dz);
                if (!VoidBuild.inReach(origin, column) || !level.getBlockState(column).isSolidRender()) {
                    continue;
                }
                if (d <= radius) {
                    // Mulde: von oben abtragen
                    int dig = (int) Math.round(depth * (1.0 - (d / radius) * (d / radius)));
                    for (int y = 0; y < dig; y++) {
                        level.setBlock(column.below(y), Blocks.AIR.defaultBlockState(), 2);
                    }
                    level.setBlock(column.below(dig), dust, 2);
                    for (int y = 1; y <= 2; y++) {
                        if (level.getBlockState(column.above(y)).canBeReplaced()) {
                            level.setBlock(column.above(y), Blocks.AIR.defaultBlockState(), 2);
                        }
                    }
                } else if (d <= radius + 1.2 && random.nextInt(3) > 0) {
                    VoidBuild.place(level, origin, column.above(), dust);   // aufgeworfener Rand
                }
            }
        }
        // der gefallene Stern
        BlockPos star = ground.below(depth - 1);
        VoidBuild.blob(level, origin, star, 1.6 + random.nextDouble(), crystal, random, 0.6);
        for (int k = 0; k < 6; k++) {
            BlockPos at = star.offset(random.nextInt(5) - 2, 2, random.nextInt(5) - 2);
            while (at.getY() > star.getY() - 2 && level.isEmptyBlock(at.below())) {
                at = at.below();
            }
            if (level.isEmptyBlock(at) && level.getBlockState(at.below()).isSolidRender()) {
                VoidBuild.place(level, origin, at, VoidBlocks.VOID_CRYSTAL_CLUSTER.get().defaultBlockState());
            }
        }
        return true;
    }
}
