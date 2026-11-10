package io.github.sirschrolli.kingdomsatwar.block.abyss;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Ein Stück Himmelsriss: weißglühendes Licht, wo die Welt gesprungen ist. Man kann hindurchfliegen – aber es verbrennt und
 * schleudert einen zurück. Unzerstörbar, ohne Gegenstand.
 */
public class SkyRiftBlock extends Block {
    public SkyRiftBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effectApplier, boolean isPrecise) {
        if (level instanceof ServerLevel server && entity instanceof LivingEntity living && living.tickCount % 10 == 0) {
            living.hurtServer(server, server.damageSources().magic(), 4.0f);
            living.igniteForSeconds(3.0f);
            Vec3 away = living.position().subtract(Vec3.atCenterOf(pos)).normalize().scale(0.8);
            living.push(away.x, -0.6, away.z);
            living.needsSync = true;
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(6) == 0) {
            level.addParticle(random.nextBoolean() ? ParticleTypes.FLAME : ParticleTypes.END_ROD, pos.getX() + random.nextDouble(),
                    pos.getY() + random.nextDouble(), pos.getZ() + random.nextDouble(), 0.0, -0.02, 0.0);
        }
    }

    @Override
    protected ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
        return ItemStack.EMPTY;
    }
}
