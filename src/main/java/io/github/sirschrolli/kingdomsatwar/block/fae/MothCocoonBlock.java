package io.github.sirschrolli.kingdomsatwar.block.fae;

import io.github.sirschrolli.kingdomsatwar.entity.fae.MothQueenEntity;
import io.github.sirschrolli.kingdomsatwar.registry.FaeBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.ModParticles;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

/**
 * Der Kokon der Mottenkönigin auf dem Altar im Mondhain: ein mannshoher, schimmernder Seidenkokon, in dem es leise
 * pulsiert. Unzerstörbar – aber wer ihn mit Feenstaub bestreut, weckt Noctua. Nach ihrem Tod spinnt sich ein neuer.
 */
public class MothCocoonBlock extends Block {
    private static final VoxelShape SHAPE = Block.column(12.0, 0.0, 24.0);

    public MothCocoonBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hitResult) {
        if (!stack.is(FaeBlocks.FAE_DUST.get())) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        if (level instanceof ServerLevel server) {
            if (!awaken(server, pos, player)) {
                return InteractionResult.FAIL;
            }
            stack.consume(1, player);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide()) {
            player.sendOverlayMessage(Component.translatable("block.kingdomsatwar.moth_cocoon.hint").withStyle(ChatFormatting.LIGHT_PURPLE));
        }
        return InteractionResult.SUCCESS;
    }

    /**
     * Weckt die Mottenkönigin: Der Kokon reißt auf, und Noctua steigt daraus empor. Schlägt fehl, wenn sie schon wach ist
     * oder die Welt auf „Friedlich“ steht.
     */
    public static boolean awaken(ServerLevel level, BlockPos pos, @Nullable Player player) {
        if (level.getDifficulty() == Difficulty.PEACEFUL) {
            if (player != null) {
                player.sendOverlayMessage(Component.translatable("block.kingdomsatwar.moth_cocoon.peaceful").withStyle(ChatFormatting.GRAY));
            }
            return false;
        }
        if (!level.getEntitiesOfClass(MothQueenEntity.class, new AABB(pos).inflate(64.0)).isEmpty()) {
            return false;
        }
        MothQueenEntity queen = ModEntities.MOTH_QUEEN.get().create(level, EntitySpawnReason.TRIGGERED);
        if (queen == null) {
            return false;
        }
        level.removeBlock(pos, false);
        queen.snapTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, level.getRandom().nextFloat() * 360.0f, 0.0f);
        queen.awaken(pos);
        level.addFreshEntity(queen);
        level.playSound(null, pos, SoundEvents.WOOL_BREAK, SoundSource.BLOCKS, 2.0f, 0.6f);
        level.playSound(null, pos, SoundEvents.SNIFFER_EGG_CRACK, SoundSource.BLOCKS, 2.0f, 0.7f);
        level.sendParticles(ModParticles.FAE_SPARK.get(), pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 60, 0.6, 0.9, 0.6, 0.08);
        level.sendParticles(ParticleTypes.CLOUD, pos.getX() + 0.5, pos.getY() + 0.8, pos.getZ() + 0.5, 20, 0.5, 0.6, 0.5, 0.02);
        return true;
    }

    /** Ein leises Glimmen: Funken steigen am Kokon auf, ab und zu ein Mondfunke. */
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(3) == 0) {
            double a = random.nextDouble() * Math.PI * 2.0;
            level.addParticle(ModParticles.FAE_SPARK.get(), pos.getX() + 0.5 + Math.cos(a) * 0.55, pos.getY() + 0.2 + random.nextDouble() * 1.4,
                    pos.getZ() + 0.5 + Math.sin(a) * 0.55, 0.0, 0.015, 0.0);
        }
        if (random.nextInt(14) == 0) {
            level.addParticle(ParticleTypes.END_ROD, pos.getX() + 0.5, pos.getY() + 1.9, pos.getZ() + 0.5, 0.0, 0.03, 0.0);
        }
    }
}
