package io.github.sirschrolli.kingdomsatwar.block.fae;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.function.Supplier;

/** Eine Blume des Feenreichs: über ihrem Kelch steigen hin und wieder winzige Funken auf. */
public class FaeFlowerBlock extends FlowerBlock {
    private final Supplier<? extends ParticleOptions> sparkle;
    private final int chance;

    public FaeFlowerBlock(Holder<MobEffect> stewEffect, float seconds, Supplier<? extends ParticleOptions> sparkle, int chance,
            BlockBehaviour.Properties properties) {
        super(stewEffect, seconds, properties);
        this.sparkle = sparkle;
        this.chance = chance;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(this.chance) != 0) {
            return;
        }
        Vec3 offset = state.getOffset(pos);
        level.addParticle(this.sparkle.get(), pos.getX() + 0.5 + offset.x + (random.nextDouble() - 0.5) * 0.4,
                pos.getY() + 0.6 + random.nextDouble() * 0.3, pos.getZ() + 0.5 + offset.z + (random.nextDouble() - 0.5) * 0.4,
                0.0, 0.015, 0.0);
    }
}
