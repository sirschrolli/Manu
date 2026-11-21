package io.github.sirschrolli.kingdomsatwar.block.sea;

import io.github.sirschrolli.kingdomsatwar.registry.ModParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BubbleColumnBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.enums.BubbleColumnDirection;

/**
 * Heißer Schlot: die Öffnung einer heißen Quelle am Grund der Tiefsee. In ihr glüht es; schwarzer, mineralreicher Rauch
 * quillt heraus, und darüber steigt eine Säule aus Blasen bis zur Oberfläche (wie über Seelensand – man wird nach
 * oben getragen). Wer unvorsichtig darauf tritt, verbrennt sich (Schleichen schützt).
 */
public class HydrothermalVentBlock extends Block {
    public HydrothermalVentBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public BubbleColumnDirection getBubbleColumnDirection(BlockState state) {
        return BubbleColumnDirection.UPWARD;
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        level.scheduleTick(pos, this, 20);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction direction,
            BlockPos neighbourPos, BlockState neighbourState, RandomSource random) {
        if (direction == Direction.UP && neighbourState.is(Blocks.WATER)) {
            ticks.scheduleTick(pos, this, 20);
        }
        return super.updateShape(state, level, ticks, pos, direction, neighbourPos, neighbourState, random);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        BubbleColumnBlock.updateColumn(Blocks.BUBBLE_COLUMN, level, pos.above(), state);
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        if (!entity.isSteppingCarefully() && entity instanceof LivingEntity living && !living.fireImmune()) {
            entity.hurt(level.damageSources().hotFloor(), 2.0f);
        }
        super.stepOn(level, pos, state, entity);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        BlockPos above = pos.above();
        boolean water = level.getFluidState(above).is(FluidTags.WATER);
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 1.02;
        double z = pos.getZ() + 0.5;
        if (water) {
            // schwarzer Rauch quillt auf und breitet sich aus
            for (int i = 0; i < 2; i++) {
                level.addAlwaysVisibleParticle(ModParticles.VENT_SMOKE.get(), true, x + (random.nextDouble() - 0.5) * 0.3, y,
                        z + (random.nextDouble() - 0.5) * 0.3, (random.nextDouble() - 0.5) * 0.01, 0.06 + random.nextDouble() * 0.03,
                        (random.nextDouble() - 0.5) * 0.01);
            }
            if (random.nextInt(3) == 0) {
                level.addParticle(ParticleTypes.BUBBLE_COLUMN_UP, x + (random.nextDouble() - 0.5) * 0.5, y, z + (random.nextDouble() - 0.5) * 0.5,
                        0.0, 0.05, 0.0);
            }
            if (random.nextInt(80) == 0) {
                level.playLocalSound(x, y, z, SoundEvents.BUBBLE_COLUMN_UPWARDS_AMBIENT, SoundSource.BLOCKS, 0.5f,
                        0.6f + random.nextFloat() * 0.2f, false);
            }
        } else if (level.getBlockState(above).isAir()) {
            level.addAlwaysVisibleParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, true, x + (random.nextDouble() - 0.5) * 0.4, y,
                    z + (random.nextDouble() - 0.5) * 0.4, 0.0, 0.05, 0.0);
            if (random.nextInt(40) == 0) {
                level.playLocalSound(x, y, z, SoundEvents.LAVA_POP, SoundSource.BLOCKS, 0.4f, 0.7f + random.nextFloat() * 0.3f, false);
            }
        }
    }
}
