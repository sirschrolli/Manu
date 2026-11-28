package io.github.sirschrolli.kingdomsatwar.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

/**
 * Das Siegel selbst: ein Mosaik aus 5×5 Platten in der Mitte des Siegelkreises. Jede Platte zeigt ihr Stück des großen
 * Siegelbildes ({@link #PART}: Zeile × 5 + Spalte, von Nordwesten aus). Beim Siegelbruch zerspringen die Platten und
 * werden zum Tor in die Schattenwelt.
 */
public class SealPlateBlock extends Block {
    public static final int SIZE = 5;
    public static final IntegerProperty PART = IntegerProperty.create("part", 0, SIZE * SIZE - 1);

    public SealPlateBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(PART, 12));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PART);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(24) == 0) {
            level.addParticle(ParticleTypes.ENCHANT, pos.getX() + random.nextDouble(), pos.getY() + 1.6 + random.nextDouble(),
                    pos.getZ() + random.nextDouble(), 0.0, -0.8, 0.0);
        }
    }
}
