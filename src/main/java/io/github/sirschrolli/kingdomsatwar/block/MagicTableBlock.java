package io.github.sirschrolli.kingdomsatwar.block;

import io.github.sirschrolli.kingdomsatwar.block.entity.MagicTableBlockEntity;
import io.github.sirschrolli.kingdomsatwar.magic.MagicTableMenu;
import io.github.sirschrolli.kingdomsatwar.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

/**
 * Der Magietisch: ein Sockel aus dunklem Arkanstein mit goldenen Bändern, darauf eine Platte mit eingelegtem
 * Siegelkreis. Über ihm kreisen leuchtende Runen; liegt ein Aetherit-Stab darin, schwebt er über der Platte. Ein Klick
 * öffnet das Fenster, in dem man Zaubertafeln in den Stab einschreibt.
 */
public class MagicTableBlock extends Block implements EntityBlock {
    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(1, 0, 1, 15, 4, 15),
            Block.box(3.5, 4, 3.5, 12.5, 10, 12.5),
            Block.box(0, 10, 0, 16, 13, 16));

    public MagicTableBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType type) {
        return false;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MagicTableBlockEntity(pos, state);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (!level.isClientSide() || type != ModBlockEntities.MAGIC_TABLE.get()) {
            return null;
        }
        return (lvl, pos, st, be) -> MagicTableBlockEntity.clientTick(lvl, pos, st, (MagicTableBlockEntity) be);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (player instanceof ServerPlayer serverPlayer && level.getBlockEntity(pos) instanceof MagicTableBlockEntity table) {
            serverPlayer.openMenu(new SimpleMenuProvider(
                    (id, inventory, p) -> new MagicTableMenu(id, inventory, table, ContainerLevelAccess.create(level, pos), pos),
                    Component.translatable("container.kingdomsatwar.magic_table")), buf -> buf.writeBlockPos(pos));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected boolean triggerEvent(BlockState state, Level level, BlockPos pos, int id, int param) {
        super.triggerEvent(state, level, pos, id, param);
        BlockEntity entity = level.getBlockEntity(pos);
        return entity != null && entity.triggerEvent(id, param);
    }

    /** Glyphen schweben aus der Luft zur Platte, ab und zu glimmt ein Funke Aether auf. */
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(3) == 0) {
            double a = random.nextDouble() * Math.PI * 2;
            double r = 1.2 + random.nextDouble() * 0.8;
            level.addParticle(ParticleTypes.ENCHANT, pos.getX() + 0.5, pos.getY() + 1.4, pos.getZ() + 0.5,
                    Math.cos(a) * r, random.nextDouble() * 0.6 - 0.8, Math.sin(a) * r);
        }
        if (random.nextInt(14) == 0) {
            double a = random.nextDouble() * Math.PI * 2;
            level.addParticle(ParticleTypes.END_ROD, pos.getX() + 0.5 + Math.cos(a) * 0.4, pos.getY() + 0.85,
                    pos.getZ() + 0.5 + Math.sin(a) * 0.4, 0.0, 0.008 + random.nextDouble() * 0.008, 0.0);
        }
    }
}
