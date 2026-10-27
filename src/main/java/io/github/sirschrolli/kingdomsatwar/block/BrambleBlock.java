package io.github.sirschrolli.kingdomsatwar.block;

import io.github.sirschrolli.kingdomsatwar.entity.boss.DragonBossEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Dornengestrüpp: verschlungene, dornige Ranken. Wer hindurchläuft, wird gebremst und gestochen. */
public class BrambleBlock extends VegetationBlock {
    private static final VoxelShape SHAPE = Block.column(14.0, 0.0, 13.0);

    public BrambleBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(BlockTags.SUPPORTS_VEGETATION) || state.is(BlockTags.MOSS_BLOCKS) || state.is(BlockTags.BASE_STONE_OVERWORLD);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE.move(state.getOffset(pos));
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effectApplier,
            boolean isPrecise) {
        if (!(entity instanceof LivingEntity) || entity instanceof DragonBossEntity) {
            return;
        }
        entity.makeStuckInBlock(state, new Vec3(0.7, 0.75, 0.7));
        if (level instanceof ServerLevel serverLevel) {
            Vec3 movement = entity.isClientAuthoritative() ? entity.getKnownMovement() : entity.oldPosition().subtract(entity.position());
            if (movement.horizontalDistanceSqr() > 1.0e-5) {
                entity.hurtServer(serverLevel, level.damageSources().cactus(), 1.5f);
            }
        }
    }
}
