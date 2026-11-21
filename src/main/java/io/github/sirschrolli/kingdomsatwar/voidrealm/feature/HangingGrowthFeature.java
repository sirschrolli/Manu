package io.github.sirschrolli.kingdomsatwar.voidrealm.feature;

import com.mojang.serialization.MapCodec;
import io.github.sirschrolli.kingdomsatwar.registry.VoidBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;

/**
 * Unter den Bruchstücken: Zapfen aus Leerenstein mit Kristallspitzen hängen ins Nichts, dazwischen lange, leuchtende
 * Sternenranken.
 */
public record HangingGrowthFeature() implements Feature {
    public static final MapCodec<HangingGrowthFeature> CODEC = MapCodec.unit(HangingGrowthFeature::new);

    @Override
    public MapCodec<HangingGrowthFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
        if (!level.getBlockState(origin.above()).isSolidRender() || !level.isEmptyBlock(origin)) {
            return false;
        }
        if (random.nextInt(3) == 0) {
            // Zapfen
            BlockState stone = VoidBlocks.VOID_STONE.base().get().defaultBlockState();
            BlockState crystal = VoidBlocks.VOID_CRYSTAL.get().defaultBlockState();
            int len = 3 + random.nextInt(6);
            for (int y = 0; y < len; y++) {
                double r = 1.4 * (1.0 - y / (double) len);
                int ir = (int) Math.ceil(r);
                for (int dx = -ir; dx <= ir; dx++) {
                    for (int dz = -ir; dz <= ir; dz++) {
                        if (dx * dx + dz * dz <= r * r + 0.15) {
                            VoidBuild.place(level, origin, origin.offset(dx, -y, dz), y >= len - 2 ? crystal : stone);
                        }
                    }
                }
            }
            return true;
        }
        // Sternenranken
        BlockState tip = VoidBlocks.STAR_VINES.get().defaultBlockState();
        BlockState body = VoidBlocks.STAR_VINES_PLANT.get().defaultBlockState();
        for (int k = 0; k < 4; k++) {
            BlockPos start = origin.offset(random.nextInt(5) - 2, 0, random.nextInt(5) - 2);
            if (!level.isEmptyBlock(start) || !level.getBlockState(start.above()).isSolidRender()) {
                continue;
            }
            int len = 2 + random.nextInt(8);
            for (int i = 0; i < len; i++) {
                BlockPos at = start.below(i);
                if (!VoidBuild.inReach(origin, at) || !level.isEmptyBlock(at)) {
                    break;
                }
                boolean last = i == len - 1 || !level.isEmptyBlock(at.below());
                level.setBlock(at, last ? tip : body, 2);
                if (last) {
                    break;
                }
            }
        }
        return true;
    }
}
