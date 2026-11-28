package io.github.sirschrolli.kingdomsatwar.block.voidrealm;

import io.github.sirschrolli.kingdomsatwar.block.ShadowPlantBlock;
import io.github.sirschrolli.kingdomsatwar.registry.VoidBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Leerenblüte: eine kelchförmige Blüte aus der Leere, deren Blätter wie dünnes Glas schimmern; aus ihrer Mitte steigen
 * winzige Sternenfunken auf. Wächst auf Leerenmoos und Sternenstaub – und überall, wo Gras wächst.
 */
public class VoidBloomBlock extends ShadowPlantBlock {
    public VoidBloomBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(VoidBlocks.VOID_MOSS.get()) || state.is(VoidBlocks.STARDUST.get()) || super.mayPlaceOn(state, level, pos);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(14) == 0) {
            level.addParticle(ParticleTypes.END_ROD, pos.getX() + 0.35 + random.nextDouble() * 0.3, pos.getY() + 0.7,
                    pos.getZ() + 0.35 + random.nextDouble() * 0.3, 0.0, 0.012, 0.0);
        }
    }
}
