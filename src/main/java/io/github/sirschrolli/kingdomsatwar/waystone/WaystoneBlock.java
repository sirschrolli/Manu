package io.github.sirschrolli.kingdomsatwar.waystone;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

/**
 * Der Wegstein (seit 1.12): ein kleiner Obelisk aus Stein mit leuchtender Rune und einem Kristall obenauf. Mit dem
 * Wegkompass angeklickt, merkt sich der Kompass ihn (bis zu acht) und zeigt auf Wunsch zu ihm. Ein Namensschild gibt ihm einen
 * Namen; ungenannt heißt er nach seinem Biom. Er leuchtet ein wenig, und ab und zu steigt ein Funke auf.
 */
public class WaystoneBlock extends Block implements EntityBlock {
    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(1, 0, 1, 15, 2, 15),
            Block.box(2, 2, 2, 14, 3, 14),
            Block.box(3, 3, 3, 13, 12, 13),
            Block.box(2.5, 12, 2.5, 13.5, 13, 13.5),
            Block.box(3, 13, 3, 13, 14, 13),
            Block.box(4, 14, 4, 12, 15, 12),
            Block.box(4.5, 15, 4.5, 11.5, 16, 11.5));

    public WaystoneBlock(Properties properties) {
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
        return new WaystoneBlockEntity(pos, state);
    }

    /** Ungenannt heißt er nach seinem Biom: „Wegstein (Birkenwald)“. */
    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level instanceof ServerLevel && level.getBlockEntity(pos) instanceof WaystoneBlockEntity stone && stone.getCustomName() == null) {
            stone.setName(autoName(level, pos));
        }
    }

    /** „Wegstein (Birkenwald)“ – als übersetzbarer Text, so liest jeder ihn in seiner Sprache. */
    public static Component autoName(Level level, BlockPos pos) {
        Component biome = level.getBiome(pos).unwrapKey().map(key -> {
            String path = key.identifier().getPath();
            // ohne Übersetzung (manche Biome der Reiche): der Name aus der Kennung, „Arcane Sphere“
            StringBuilder fallback = new StringBuilder();
            for (String word : path.split("[_/]")) {
                if (!word.isEmpty()) {
                    fallback.append(fallback.isEmpty() ? "" : " ").append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
                }
            }
            return (Component) Component.translatableWithFallback("biome." + key.identifier().getNamespace() + "." + path.replace('/', '.'),
                    fallback.toString());
        }).orElse(Component.translatable("block.kingdomsatwar.waystone"));
        return Component.translatable("block.kingdomsatwar.waystone.named", biome);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof WaystoneBlockEntity stone)) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        if (stack.getItem() instanceof WaystoneCompassItem) {
            if (level instanceof ServerLevel server && player instanceof ServerPlayer serverPlayer) {
                WaystoneCompass.remember(serverPlayer, stack, server, pos, stone);
            }
            return InteractionResult.SUCCESS;
        }
        Component custom = stack.get(DataComponents.CUSTOM_NAME);
        if (stack.is(Items.NAME_TAG) && custom != null) {
            // ein Namensschild tauft den Wegstein (Kompasse, die ihn kennen, lernen den Namen beim nächsten Besuch)
            if (level instanceof ServerLevel server && player instanceof ServerPlayer serverPlayer) {
                stone.setName(custom);
                stack.consume(1, serverPlayer);
                server.playSound(null, pos, SoundEvents.BOOK_PAGE_TURN, SoundSource.BLOCKS, 1.0f, 1.0f);
                server.sendParticles(ParticleTypes.ENCHANT, pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5, 20, 0.3, 0.3, 0.3, 0.4);
                serverPlayer.sendOverlayMessage(Component.translatable("message.kingdomsatwar.waystone.renamed", custom).withStyle(ChatFormatting.GOLD));
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    /**
     * Ohne Kompass in der Haupthand: Liegt einer in der anderen Hand, merkt er sich den Stein (Minecraft fragt die andere Hand
     * hier gar nicht mehr); sonst nennt der Stein seinen Namen und wie man ihn sich merkt.
     */
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (player instanceof ServerPlayer serverPlayer && level instanceof ServerLevel server
                && level.getBlockEntity(pos) instanceof WaystoneBlockEntity stone) {
            ItemStack offhand = player.getOffhandItem();
            if (offhand.getItem() instanceof WaystoneCompassItem) {
                WaystoneCompass.remember(serverPlayer, offhand, server, pos, stone);
            } else {
                serverPlayer.sendOverlayMessage(Component.translatable("message.kingdomsatwar.waystone.info", stone.getName().copy()
                        .withStyle(ChatFormatting.AQUA)).withStyle(ChatFormatting.GRAY));
            }
        }
        return InteractionResult.SUCCESS;
    }

    /** Ab und zu steigt ein Funke von der Rune auf, Runenglyphen schweben zum Kristall. */
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(4) == 0) {
            // Glyphen fliegen von einem Kreis um den Stein in den schwebenden Kristall (sie enden 1,2 Blöcke unter dem Ziel)
            double a = random.nextDouble() * Math.PI * 2;
            level.addParticle(ParticleTypes.ENCHANT, pos.getX() + 0.5, pos.getY() + 2.37, pos.getZ() + 0.5, Math.cos(a) * 1.3,
                    -0.8 - random.nextDouble() * 0.5, Math.sin(a) * 1.3);
        }
        if (random.nextInt(10) == 0) {
            level.addParticle(ParticleTypes.END_ROD, pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.3, pos.getY() + 1.05,
                    pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.3, 0.0, 0.012 + random.nextDouble() * 0.01, 0.0);
        }
    }
}
