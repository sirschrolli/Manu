package io.github.sirschrolli.kingdomsatwar.block.voidrealm;

import io.github.sirschrolli.kingdomsatwar.registry.VoidBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BonemealSource;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Leerenmoos: ein dichtes, samtiges Moos von der Farbe der Nacht, das auf den Bruchstücken der Leere wächst. Mit
 * Knochenmehl sprießen Leerenblüten und kleine Kristalldrusen daraus.
 */
public class VoidMossBlock extends Block implements BonemealableBlock {
    public VoidMossBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state, BonemealSource source) {
        return level.getBlockState(pos.above()).isAir();
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state, BonemealSource source) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state, BonemealSource source) {
        for (int i = 0; i < 24; i++) {
            BlockPos at = pos.offset(random.nextInt(7) - 3, random.nextInt(3) - 1, random.nextInt(7) - 3);
            if (!level.getBlockState(at).is(VoidBlocks.VOID_MOSS.get()) || !level.getBlockState(at.above()).isAir()) {
                continue;
            }
            BlockState plant = random.nextInt(8) == 0 ? VoidBlocks.VOID_CRYSTAL_CLUSTER.get().defaultBlockState()
                    : VoidBlocks.VOID_BLOOM.get().defaultBlockState();
            if (plant.canSurvive(level, at.above())) {
                level.setBlock(at.above(), plant, Block.UPDATE_ALL);
            }
        }
    }
}
