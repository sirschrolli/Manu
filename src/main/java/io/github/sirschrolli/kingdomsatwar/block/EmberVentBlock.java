package io.github.sirschrolli.kingdomsatwar.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Glutspalte: ein Schlot im Vulkangestein der Aschelande. Aus ihm steigt eine Rauchsäule, Glut und Lavatropfen
 * spritzen heraus. Wer unvorsichtig darauf tritt, verbrennt sich (Schleichen schützt).
 */
public class EmberVentBlock extends Block {
    public EmberVentBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        if (!entity.isSteppingCarefully() && entity instanceof LivingEntity living && !living.fireImmune()) {
            entity.hurt(level.damageSources().hotFloor(), 2.0f);
            if (level.getRandom().nextInt(5) == 0) {
                entity.igniteForSeconds(2.0f);
            }
        }
        super.stepOn(level, pos, state, entity);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!level.getBlockState(pos.above()).isAir()) {
            return;   // verstopft
        }
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 1.0;
        double z = pos.getZ() + 0.5;
        level.addAlwaysVisibleParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, true,
                x + (random.nextDouble() - 0.5) * 0.4, y + random.nextDouble() * 0.5, z + (random.nextDouble() - 0.5) * 0.4,
                0.0, 0.06, 0.0);
        if (random.nextInt(3) == 0) {
            level.addParticle(ParticleTypes.LARGE_SMOKE, x + (random.nextDouble() - 0.5) * 0.3, y, z + (random.nextDouble() - 0.5) * 0.3,
                    0.0, 0.04, 0.0);
        }
        if (random.nextInt(4) == 0) {
            level.addParticle(ParticleTypes.LAVA, x, y, z, 0.0, 0.0, 0.0);
        }
        if (random.nextInt(6) == 0) {
            level.addParticle(ParticleTypes.FLAME, x + (random.nextDouble() - 0.5) * 0.3, y + 0.05, z + (random.nextDouble() - 0.5) * 0.3,
                    0.0, 0.03, 0.0);
        }
        if (random.nextInt(30) == 0) {
            level.playLocalSound(x, y, z, SoundEvents.LAVA_POP, SoundSource.BLOCKS, 0.5f, 0.8f + random.nextFloat() * 0.3f, false);
        }
        if (random.nextInt(60) == 0) {
            level.playLocalSound(x, y, z, SoundEvents.FIRE_AMBIENT, SoundSource.BLOCKS, 0.7f, 0.6f + random.nextFloat() * 0.3f, false);
        }
    }
}
