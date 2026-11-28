package io.github.sirschrolli.kingdomsatwar.block;

import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;

/**
 * Eingemeißeltes Zeichen eines Elements im Siegelkreis: die Runen an den Monolithen und die Einlagen im Boden, die
 * vom Monolithen zur Mitte führen. Ist der Siegelsplitter des Elements eingesetzt, leuchtet das Zeichen in seiner
 * Farbe ({@link #LIT}).
 */
public class SealMarkBlock extends Block {
    public static final EnumProperty<DragonType> ELEMENT = EnumProperty.create("element", DragonType.class);
    public static final BooleanProperty LIT = BooleanProperty.create("lit");

    public SealMarkBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(ELEMENT, DragonType.FIRE).setValue(LIT, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ELEMENT, LIT);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (state.getValue(LIT) && random.nextInt(5) == 0) {
            DragonType element = state.getValue(ELEMENT);
            double x = pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 1.2;
            double y = pos.getY() + 0.5 + (random.nextDouble() - 0.5) * 1.2;
            double z = pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 1.2;
            level.addParticle(random.nextInt(3) == 0 ? element.particle() : ParticleTypes.END_ROD, x, y, z, 0.0, 0.015, 0.0);
        }
    }
}
