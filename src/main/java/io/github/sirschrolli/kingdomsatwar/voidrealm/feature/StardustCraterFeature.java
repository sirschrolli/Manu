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
        skyIronCore(level, origin, star, random);
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

    /**
     * Der Kern des gefallenen Sterns (seit 0.99): Himmelseisen, vom Kristall umhüllt (innen ganz, außen hier und da), dazu
     * ein paar Brocken, die beim Einschlag in den Grund unter dem Stern getrieben wurden.
     */
    private static void skyIronCore(WorldGenLevel level, BlockPos origin, BlockPos star, RandomSource random) {
        BlockState iron = io.github.sirschrolli.kingdomsatwar.registry.VoidGear.SKY_IRON_ORE.get().defaultBlockState();
        double core = 1.1 + random.nextDouble() * 0.6;
        int r = (int) Math.ceil(core) + 1;
        for (int dx = -r; dx <= r; dx++) {
            for (int dy = -r; dy <= r; dy++) {
                for (int dz = -r; dz <= r; dz++) {
                    BlockPos p = star.offset(dx, dy, dz);
                    if (!VoidBuild.inReach(origin, p) || !level.getBlockState(p).is(VoidBlocks.VOID_CRYSTAL.get())) {
                        continue;
                    }
                    double d = Math.sqrt(dx * dx + dy * dy * 1.4 + dz * dz);
                    if (d <= core || random.nextFloat() < 0.22f) {
                        level.setBlock(p, iron, 2);
                    }
                }
            }
        }
        for (int k = 0; k < 4; k++) {
            BlockPos p = star.offset(random.nextInt(3) - 1, -1 - random.nextInt(2), random.nextInt(3) - 1);
            BlockState there = level.getBlockState(p);
            if (VoidBuild.inReach(origin, p) && (there.is(VoidBlocks.STARDUST.get()) || there.is(VoidBlocks.VOID_STONE.base().get()))) {
                level.setBlock(p, iron, 2);
            }
        }
    }
}
