package io.github.sirschrolli.kingdomsatwar.block.voidrealm;

import io.github.sirschrolli.kingdomsatwar.registry.StarWeaver;
import io.github.sirschrolli.kingdomsatwar.registry.VoidGear;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

/**
 * Das Sternengelege (seit 0.99): In der Mitte des Sternennetzes liegt in einem Bett aus Sternenseide das Gelege der
 * Sternenweberin – Eier, in denen Sternenlicht glimmt. Über ihm hängt sie in Andacht; ist sie besiegt, erlischt das Licht –
 * ein Himmelseisenbarren im Gelege (ein gefallener Stern) lockt eine neue Weberin. Unzerstörbar; man geht darüber wie über
 * einen Teppich.
 */
public class WeaverNestBlock extends Block implements EntityBlock {
    public static final EnumProperty<Phase> PHASE = EnumProperty.create("phase", Phase.class);
    private static final VoxelShape SHAPE = Block.box(1.0, 0.0, 1.0, 15.0, 7.0, 15.0);

    /** Die Weberin hängt in Andacht (oder ist noch nicht da), sie kämpft, sie ist besiegt. */
    public enum Phase implements StringRepresentable {
        SLEEPING(8), AWAKE(12), DEFEATED(0);

        public final int light;

        Phase(int light) {
            this.light = light;
        }

        @Override
        public String getSerializedName() {
            return this.name().toLowerCase(Locale.ROOT);
        }
    }

    public WeaverNestBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(PHASE, Phase.SLEEPING));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PHASE);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new WeaverNestBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide() || type != StarWeaver.WEAVER_NEST_ENTITY.get()) {
            return null;
        }
        return (lvl, pos, st, be) -> {
            if (lvl instanceof ServerLevel server && be instanceof WeaverNestBlockEntity nest) {
                nest.serverTick(server);
            }
        };
    }

    /** Ein Himmelseisenbarren im erloschenen Gelege: Die Eier glimmen auf, und eine neue Weberin lässt sich herab. */
    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hitResult) {
        if (!stack.is(VoidGear.SKY_IRON_INGOT.get()) || state.getValue(PHASE) != Phase.DEFEATED) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        if (level instanceof ServerLevel server && level.getBlockEntity(pos) instanceof WeaverNestBlockEntity nest) {
            if (!nest.recall(server, player)) {
                return InteractionResult.FAIL;
            }
            stack.consume(1, player);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide()) {
            String key = switch (state.getValue(PHASE)) {
                case SLEEPING -> "block.kingdomsatwar.weaver_nest.hint";
                case AWAKE -> "block.kingdomsatwar.weaver_nest.awake";
                case DEFEATED -> "block.kingdomsatwar.weaver_nest.defeated";
            };
            player.sendOverlayMessage(Component.translatable(key).withStyle(state.getValue(PHASE) == Phase.DEFEATED ? ChatFormatting.GRAY
                    : ChatFormatting.AQUA));
        }
        return InteractionResult.SUCCESS;
    }

    /** In den Eiern glimmt Sternenlicht; Funken steigen auf (erloschen nur ab und zu ein Staubkorn). */
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        Phase phase = state.getValue(PHASE);
        double x = pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.7;
        double z = pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.7;
        if (phase == Phase.DEFEATED) {
            if (random.nextInt(12) == 0) {
                level.addParticle(new DustParticleOptions(0x6A7088, 0.8f), x, pos.getY() + 0.4, z, 0.0, 0.01, 0.0);
            }
            return;
        }
        if (random.nextInt(phase == Phase.AWAKE ? 2 : 4) == 0) {
            level.addParticle(ParticleTypes.END_ROD, x, pos.getY() + 0.4, z, 0.0, 0.02, 0.0);
        }
        if (random.nextInt(3) == 0) {
            level.addParticle(new DustParticleOptions(0xB8D4FF, 0.7f), x, pos.getY() + 0.3, z, 0.0, 0.01, 0.0);
        }
    }
}
