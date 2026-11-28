package io.github.sirschrolli.kingdomsatwar.block.fae;

import io.github.sirschrolli.kingdomsatwar.registry.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

/**
 * Dornenhecke: ein dichtes Geflecht violetter Dornenranken, so hoch wie ein Block – das Dickicht der Dornenwildnis. Wer
 * sich daran reibt, wird gestochen (die Wesen der Wildnis nicht). Mit Schere oder Schwert schnell geschnitten.
 */
public class ThornHedgeBlock extends Block {
    /** Ein Pixel schmaler als der Block: so berührt man die Dornen, wenn man dagegen läuft. */
    private static final VoxelShape COLLISION = Block.column(14.0, 0.0, 15.0);

    public ThornHedgeBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return COLLISION;
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effectApplier,
            boolean isPrecise) {
        if (!(entity instanceof LivingEntity) || entity.is(ModTags.EntityTypes.THORN_IMMUNE) || !(level instanceof ServerLevel serverLevel)) {
            return;
        }
        Vec3 movement = entity.isClientAuthoritative() ? entity.getKnownMovement() : entity.oldPosition().subtract(entity.position());
        if (movement.horizontalDistanceSqr() > 1.0e-5 || entity.getRandom().nextInt(20) == 0) {
            entity.hurtServer(serverLevel, level.damageSources().sweetBerryBush(), 1.0f);
        }
    }

    @Override
    public @Nullable PathType getBlockPathType(BlockState state, BlockGetter level, BlockPos pos, @Nullable Mob mob) {
        return PathType.DAMAGING;
    }

    @Override
    public @Nullable PathType getAdjacentBlockPathType(BlockState state, BlockGetter level, BlockPos pos, @Nullable Mob mob,
            PathType originalType) {
        return PathType.DAMAGING_IN_NEIGHBOR;
    }
}
