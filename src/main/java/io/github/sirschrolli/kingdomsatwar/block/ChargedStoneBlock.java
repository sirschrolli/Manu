package io.github.sirschrolli.kingdomsatwar.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/** Geladener Sturmstein: Funken knistern über die Blitzadern, und wer darauf steht, bekommt ab und zu einen Schlag. */
public class ChargedStoneBlock extends Block {
    public ChargedStoneBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        if (level instanceof ServerLevel serverLevel && entity instanceof LivingEntity living && !living.isSteppingCarefully()
                && level.getRandom().nextInt(40) == 0) {
            living.hurtServer(serverLevel, level.damageSources().lightningBolt(), 1.0f);
            serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK, entity.getX(), entity.getY() + 0.2, entity.getZ(), 8, 0.3, 0.1, 0.3, 0.2);
            level.playSound(null, pos, SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.BLOCKS, 0.4f, 1.8f);
        }
        super.stepOn(level, pos, state, entity);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(10) == 0 && level.getBlockState(pos.above()).isAir()) {
            level.addParticle(ParticleTypes.ELECTRIC_SPARK, pos.getX() + random.nextDouble(), pos.getY() + 1.02, pos.getZ() + random.nextDouble(),
                    0.0, 0.05, 0.0);
        }
    }
}
