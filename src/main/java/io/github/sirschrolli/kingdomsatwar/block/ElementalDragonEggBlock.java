package io.github.sirschrolli.kingdomsatwar.block;

import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import io.github.sirschrolli.kingdomsatwar.entity.dragon.DragonEntity;
import io.github.sirschrolli.kingdomsatwar.entity.dragon.DragonStage;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

/**
 * Ein Drachenei einer bestimmten {@link DragonType Drachenart}.
 *
 * <p>Liegt das Ei an einem passenden Ort, brütet es (Partikel steigen auf) und bekommt nach und nach Risse
 * ({@link #HATCH} 0–3). Danach schlüpft ein Drache, der sich den nächsten Spieler (bis 16 Blöcke) als
 * Besitzer aussucht. Brutbedingungen:
 * <ul>
 *     <li>Feuer: Lava, Feuer, Magma oder ein brennendes Lagerfeuer in der Nähe</li>
 *     <li>Eis: auf oder neben Eis oder Schnee</li>
 *     <li>Blitz: unter freiem Himmel bei Gewitter oder neben einem Blitzableiter (Blitzeinschläge helfen)</li>
 *     <li>Natur: hell, auf Gras, Moos oder Erde und mit Pflanzen in der Nähe</li>
 *     <li>Erde: unter der Erde auf Stein</li>
 *     <li>Wasser: unter Wasser</li>
 * </ul>
 */
public class ElementalDragonEggBlock extends Block implements SimpleWaterloggedBlock {
    public static final IntegerProperty HATCH = IntegerProperty.create("hatch", 0, 3);
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    private static final VoxelShape SHAPE = Block.box(1.0, 0.0, 1.0, 15.0, 16.0, 15.0);

    private final DragonType dragonType;

    public ElementalDragonEggBlock(DragonType dragonType, BlockBehaviour.Properties properties) {
        super(properties);
        this.dragonType = dragonType;
        this.registerDefaultState(this.stateDefinition.any().setValue(HATCH, 0).setValue(WATERLOGGED, false));
    }

    public DragonType getDragonType() {
        return dragonType;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HATCH, WATERLOGGED);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        boolean water = context.getLevel().getFluidState(context.getClickedPos()).is(Fluids.WATER);
        return this.defaultBlockState().setValue(WATERLOGGED, water);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction direction,
            BlockPos neighbourPos, BlockState neighbourState, RandomSource random) {
        if (state.getValue(WATERLOGGED)) {
            ticks.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        return super.updateShape(state, level, ticks, pos, direction, neighbourPos, neighbourState, random);
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType type) {
        return false;
    }

    // =====================================================================================
    // Brüten und Schlüpfen
    // =====================================================================================

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!canIncubate(level, pos, state) || random.nextInt(2) != 0) {
            return;
        }
        advance(level, pos, state, 1);
    }

    /** Bringt das Brüten um {@code steps} Stufen voran; bei voller Stufe schlüpft der Drache. */
    public void advance(ServerLevel level, BlockPos pos, BlockState state, int steps) {
        int hatch = state.getValue(HATCH) + steps;
        if (hatch > 3) {
            this.hatch(level, pos, state);
            return;
        }
        level.setBlock(pos, state.setValue(HATCH, hatch), Block.UPDATE_ALL);
        level.playSound(null, pos, ModSounds.DRAGON_EGG_CRACK.get(), SoundSource.BLOCKS, 0.8f, 0.9f + random(level) * 0.2f);
    }

    private static float random(ServerLevel level) {
        return level.getRandom().nextFloat();
    }

    private void hatch(ServerLevel level, BlockPos pos, BlockState state) {
        level.setBlock(pos, state.getValue(WATERLOGGED) ? Blocks.WATER.defaultBlockState() : Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        level.playSound(null, pos, ModSounds.DRAGON_EGG_HATCH.get(), SoundSource.BLOCKS, 1.0f, 1.0f);
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, state.setValue(WATERLOGGED, false)), pos.getX() + 0.5, pos.getY() + 0.5,
                pos.getZ() + 0.5, 30, 0.25, 0.3, 0.25, 0.08);
        level.sendParticles(this.dragonType.particle(), pos.getX() + 0.5, pos.getY() + 0.6, pos.getZ() + 0.5, 20, 0.4, 0.4, 0.4, 0.05);
        DragonEntity dragon = ModEntities.DRAGONS.get(this.dragonType).get().create(level, EntitySpawnReason.TRIGGERED);
        if (dragon == null) {
            return;
        }
        dragon.applyStage(DragonStage.HATCHLING, true);
        dragon.snapTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, level.getRandom().nextFloat() * 360.0f, 0.0f);
        Player player = level.getNearestPlayer(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 16.0, false);
        if (player != null && !player.isSpectator()) {
            dragon.tame(player);
            dragon.setMode(DragonEntity.MODE_FOLLOW);
            dragon.getLookControl().setLookAt(player);
            player.sendOverlayMessage(Component.translatable("message.kingdomsatwar.dragon_hatched", dragon.getType().getDescription())
                    .withStyle(ChatFormatting.GOLD));
        }
        dragon.setPersistenceRequired();
        level.addFreshEntity(dragon);
    }

    /** Liegt das Ei an einem Ort, an dem es brüten kann? */
    public boolean canIncubate(Level level, BlockPos pos, BlockState state) {
        return switch (this.dragonType) {
            case FIRE -> anyNearby(level, pos, 2, s -> s.is(Blocks.MAGMA_BLOCK) || s.is(BlockTags.FIRE)
                    || s.getFluidState().is(FluidTags.LAVA) || s.getBlock() instanceof CampfireBlock && CampfireBlock.isLitCampfire(s));
            case ICE -> anyNearby(level, pos, 1, s -> s.is(BlockTags.ICE) || s.is(BlockTags.SNOW) || s.is(Blocks.POWDER_SNOW));
            case LIGHTNING -> (level.isThundering() && level.canSeeSky(pos.above()))
                    || anyNearby(level, pos, 3, s -> s.is(BlockTags.LIGHTNING_RODS));
            case NATURE -> level.getMaxLocalRawBrightness(pos) >= 9
                    && level.getBlockState(pos.below()).is(BlockTags.SUBSTRATE_OVERWORLD)
                    && anyNearby(level, pos, 2, s -> s.is(BlockTags.LEAVES) || s.is(BlockTags.FLOWERS) || s.is(BlockTags.MOSS_BLOCKS)
                    || s.is(Blocks.MOSS_CARPET) || s.is(BlockTags.SAPLINGS));
            case EARTH -> !level.canSeeSky(pos.above()) && level.getBlockState(pos.below()).is(BlockTags.BASE_STONE_OVERWORLD);
            case WATER -> state.getValue(WATERLOGGED);
        };
    }

    private static boolean anyNearby(Level level, BlockPos center, int radius, java.util.function.Predicate<BlockState> test) {
        for (BlockPos p : BlockPos.betweenClosed(center.offset(-radius, -radius, -radius), center.offset(radius, radius, radius))) {
            if (!p.equals(center) && test.test(level.getBlockState(p))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(3) == 0 && this.canIncubate(level, pos, state)) {
            double x = pos.getX() + 0.25 + random.nextDouble() * 0.5;
            double z = pos.getZ() + 0.25 + random.nextDouble() * 0.5;
            level.addParticle(this.dragonType.particle(), x, pos.getY() + 0.9 + random.nextDouble() * 0.3, z, 0.0, 0.02, 0.0);
        }
    }
}
