package io.github.sirschrolli.kingdomsatwar.block.crop;

import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealSource;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Ein Fruchtstrauch der fremden Welten (seit 2.0): vier Stufen wie der Süßbeerenstrauch, aber ohne Dornen – und er reift
 * auch dort, wo nie die Sonne scheint. Halbreif gibt er beim Pflücken ein bis zwei Früchte, reif zwei bis drei, und trägt
 * danach von neuem. Worauf er wächst, sagt {@code soil}: die Schwebebeere auf Endstein, die Sternfrucht auf Leerenmoos und
 * Sternenstaub, die Chaosbohne auf allem, woraus die Splitter des Abgrunds sind, die Feenbeere auf Feengras.
 */
public class BerryBushBlock extends VegetationBlock implements BonemealableBlock {
    public static final IntegerProperty AGE = BlockStateProperties.AGE_3;
    public static final int MAX_AGE = 3;
    private static final VoxelShape SAPLING = Block.column(10.0, 0.0, 8.0);
    private static final VoxelShape BUSH = Block.column(14.0, 0.0, 16.0);

    private final Supplier<? extends ItemLike> fruit;
    private final Predicate<BlockState> soil;

    public BerryBushBlock(Supplier<? extends ItemLike> fruit, Predicate<BlockState> soil, BlockBehaviour.Properties properties) {
        super(properties);
        this.fruit = fruit;
        this.soil = soil;
        this.registerDefaultState(this.stateDefinition.any().setValue(AGE, 0));
    }

    /** Die Frucht des Strauchs (zugleich das, womit man ihn pflanzt). */
    public ItemLike fruit() {
        return this.fruit.get();
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return this.soil.test(state);
    }

    @Override
    protected ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
        return new ItemStack(this.fruit.get());
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(AGE) == 0 ? SAPLING : BUSH;
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return state.getValue(AGE) < MAX_AGE;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int age = state.getValue(AGE);
        // wie die Süßbeeren (über NeoForge, damit auch hier gilt, dass Pflanzen seit 1.3 halb so schnell wachsen)
        if (age < MAX_AGE && net.neoforged.neoforge.common.CommonHooks.canCropGrow(level, pos, state, random.nextInt(5) == 0)) {
            BlockState grown = state.setValue(AGE, age + 1);
            level.setBlock(pos, grown, Block.UPDATE_CLIENTS);
            level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(grown));
            net.neoforged.neoforge.common.CommonHooks.fireCropGrowPost(level, pos, state);
        }
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hitResult) {
        boolean ripe = state.getValue(AGE) == MAX_AGE;
        return !ripe && stack.is(Items.BONE_MEAL) ? InteractionResult.PASS : super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        int age = state.getValue(AGE);
        if (age < 2) {
            return super.useWithoutItem(state, level, pos, player, hitResult);
        }
        if (level instanceof ServerLevel server) {
            int count = (age == MAX_AGE ? 2 : 1) + server.getRandom().nextInt(2);
            Block.popResource(server, pos, new ItemStack(this.fruit.get(), count));
            server.playSound(null, pos, SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, SoundSource.BLOCKS, 1.0f,
                    0.8f + server.getRandom().nextFloat() * 0.4f);
            BlockState picked = state.setValue(AGE, 1);
            server.setBlock(pos, picked, Block.UPDATE_CLIENTS);
            server.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, picked));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state, BonemealSource source) {
        return state.getValue(AGE) < MAX_AGE;
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state, BonemealSource source) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state, BonemealSource source) {
        level.setBlock(pos, state.setValue(AGE, Math.min(MAX_AGE, state.getValue(AGE) + 1)), Block.UPDATE_CLIENTS);
    }
}
