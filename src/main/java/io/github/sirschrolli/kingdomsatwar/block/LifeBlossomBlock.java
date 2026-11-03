package io.github.sirschrolli.kingdomsatwar.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Lebensblüte: leuchtende Blüte, aus der Verdantis im Kampf neue Kraft schöpft. Im Hain wachsen einige davon rund um
 * das Nest; wer sie abbricht, nimmt der Hüterin die Heilung.
 */
public class LifeBlossomBlock extends FlowerBlock {
    public LifeBlossomBlock(Holder<MobEffect> effect, float seconds, BlockBehaviour.Properties properties) {
        super(effect, seconds, properties);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(4) == 0) {
            level.addParticle(ParticleTypes.SPORE_BLOSSOM_AIR, pos.getX() + 0.3 + random.nextDouble() * 0.4, pos.getY() + 0.7,
                    pos.getZ() + 0.3 + random.nextDouble() * 0.4, 0.0, 0.02, 0.0);
        }
        if (random.nextInt(12) == 0) {
            level.addParticle(ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5, pos.getY() + 0.8, pos.getZ() + 0.5, 0.0, 0.0, 0.0);
        }
    }
}
