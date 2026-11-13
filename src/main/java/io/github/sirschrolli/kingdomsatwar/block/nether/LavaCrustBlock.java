package io.github.sirschrolli.kingdomsatwar.block.nether;

import io.github.sirschrolli.kingdomsatwar.item.nether.EmberwalkerArmor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.Nullable;

/**
 * Glutkruste (seit 1.1.6): erstarrte Lava unter den Stiefeln der Glutwandler. Sie trägt, verbrennt niemanden und glüht in
 * ihren Rissen; alle ein, zwei Sekunden bricht sie weiter auf ({@link #AGE}), bis sie wieder zu Lava schmilzt – nur nicht,
 * solange jemand mit den Stiefeln der Glutwandler auf ihr steht. Wer sie zerschlägt, steht wieder vor Lava (es geht keine
 * Lava verloren, und es entsteht keine).
 */
public class LavaCrustBlock extends Block {
    public static final int MAX_AGE = 3;
    public static final IntegerProperty AGE = BlockStateProperties.AGE_3;

    public LavaCrustBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(AGE, 0));
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        if (!oldState.is(this)) {
            level.scheduleTick(pos, this, Mth.nextInt(level.getRandom(), 40, 70));
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (carried(level, pos)) {
            // wer mit den Glutsohlen darauf steht, hält die Kruste – sie heilt sogar ein wenig
            if (state.getValue(AGE) > 0 && random.nextInt(3) == 0) {
                level.setBlock(pos, state.setValue(AGE, state.getValue(AGE) - 1), Block.UPDATE_CLIENTS);
            }
            level.scheduleTick(pos, this, Mth.nextInt(random, 20, 40));
            return;
        }
        int age = state.getValue(AGE);
        if (age < MAX_AGE) {
            level.setBlock(pos, state.setValue(AGE, age + 1), Block.UPDATE_CLIENTS);
            level.scheduleTick(pos, this, Mth.nextInt(random, 20, 35));
            return;
        }
        melt(level, pos);
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        // falls ein geplanter Tick verloren ging (Chunk entladen): die Kruste hält nicht ewig
        if (!level.getBlockTicks().hasScheduledTick(pos, this)) {
            level.scheduleTick(pos, this, Mth.nextInt(random, 20, 40));
        }
    }

    /** Steht jemand mit den Stiefeln der Glutwandler auf (oder dicht über) dieser Kruste? */
    private static boolean carried(Level level, BlockPos pos) {
        AABB above = new AABB(pos.getX() - 0.3, pos.getY() + 0.5, pos.getZ() - 0.3, pos.getX() + 1.3, pos.getY() + 2.5, pos.getZ() + 1.3);
        return !level.getEntitiesOfClass(Player.class, above, EmberwalkerArmor::hasBoots).isEmpty();
    }

    /** Die Kruste schmilzt: wieder Lava, mit einem Blubbern. */
    public static void melt(Level level, BlockPos pos) {
        level.setBlockAndUpdate(pos, Blocks.LAVA.defaultBlockState());
        level.playSound(null, pos, SoundEvents.LAVA_POP, SoundSource.BLOCKS, 0.5f, 0.8f + level.getRandom().nextFloat() * 0.4f);
        if (level instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.LAVA, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 1, 0.2, 0.0, 0.2, 0.0);
        }
    }

    /** Wer sie zerschlägt, legt die Lava darunter wieder frei. */
    @Override
    public void playerDestroy(ServerLevel level, net.minecraft.server.level.ServerPlayer player, BlockPos pos, BlockState state,
            @Nullable BlockEntity blockEntity, ItemStack tool) {
        super.playerDestroy(level, player, pos, state, blockEntity, tool);
        level.setBlockAndUpdate(pos, Blocks.LAVA.defaultBlockState());
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        // in den Rissen glüht es; je älter die Kruste, desto öfter steigt ein Funke auf
        if (random.nextInt(8 - state.getValue(AGE) * 2) == 0) {
            level.addParticle(ParticleTypes.SMALL_FLAME, pos.getX() + random.nextDouble(), pos.getY() + 1.02, pos.getZ() + random.nextDouble(),
                    0.0, 0.01, 0.0);
        }
        if (state.getValue(AGE) >= 2 && random.nextInt(6) == 0) {
            level.addParticle(ParticleTypes.SMOKE, pos.getX() + random.nextDouble(), pos.getY() + 1.02, pos.getZ() + random.nextDouble(),
                    0.0, 0.02, 0.0);
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }

    @Override
    protected ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
        return ItemStack.EMPTY;
    }
}
