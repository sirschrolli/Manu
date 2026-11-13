package io.github.sirschrolli.kingdomsatwar.block.arena;

import io.github.sirschrolli.kingdomsatwar.registry.ArenaGear;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Ein Wurfnetz, das sich um jemanden gelegt hat (seit 1.9): Wer darin steckt, kommt kaum vom Fleck – wie in einem
 * Spinnennetz, nur fester. Mit der Hand oder einer Klinge ist es schnell zerrissen; sonst löst es sich nach
 * {@value #LIFETIME} Ticks von selbst. Ventor wirft es, und das Wurfnetz aus seiner Beute ebenso.
 */
public class GladiatorNetBlock extends Block {
    /** So lange (Ticks) hält ein Netz. */
    public static final int LIFETIME = 70;
    private static final VoxelShape SHAPE = Block.box(1.0, 0.0, 1.0, 15.0, 14.0, 15.0);

    public GladiatorNetBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    /**
     * Legt ein Netz an {@code pos}, wenn dort Luft (oder etwas Ersetzbares ohne Wasser) ist; liegt dort schon eines, hält es
     * länger. Gibt zurück, ob dort jetzt ein Netz ist.
     */
    public static boolean place(ServerLevel level, BlockPos pos, int lifetime) {
        Block net = ArenaGear.GLADIATOR_NET_BLOCK.get();
        BlockState here = level.getBlockState(pos);
        if (here.is(net)) {
            level.scheduleTick(pos, net, lifetime);
            return true;
        }
        if (!here.isAir() && !(here.canBeReplaced() && here.getFluidState().isEmpty()) || !level.isInWorldBounds(pos)) {
            return false;
        }
        level.setBlock(pos, net.defaultBlockState(), Block.UPDATE_ALL);
        level.scheduleTick(pos, net, lifetime);
        return true;
    }

    /** Steckt das Wesen in einem Netz (an den Füßen oder am Kopf)? */
    public static boolean entangled(Entity entity) {
        Level level = entity.level();
        Block net = ArenaGear.GLADIATOR_NET_BLOCK.get();
        return level.getBlockState(entity.blockPosition()).is(net)
                || level.getBlockState(BlockPos.containing(entity.getX(), entity.getEyeY(), entity.getZ())).is(net);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!level.isClientSide() && !oldState.is(this)) {
            level.scheduleTick(pos, this, LIFETIME);
        }
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effectApplier,
            boolean isPrecise) {
        if (entity instanceof LivingEntity) {
            entity.makeStuckInBlock(state, new Vec3(0.2, 0.3, 0.2));
        }
    }

    /** Das Netz löst sich: die Seile fallen auseinander. */
    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        level.removeBlock(pos, false);
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, state), pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                14, 0.3, 0.3, 0.3, 0.05);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }
}
