package io.github.sirschrolli.kingdomsatwar.block;

import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;

/**
 * Bannsiegel eines Erzmagiers: eine schimmernde Wand in der Farbe seines Elements. Er versiegelt damit den Zugang zu seinem
 * Sanktum, sobald der Kampf beginnt – erst sein Tod (oder sein Rückzug, wenn kein Gegner mehr im Saal ist) löst es wieder.
 * Unzerstörbar, ohne Gegenstand.
 */
public class ArcaneBarrierBlock extends Block {
    public static final EnumProperty<DragonType> ELEMENT = EnumProperty.create("element", DragonType.class);

    public ArcaneBarrierBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(ELEMENT, DragonType.FIRE));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ELEMENT);
    }

    /** Wie Glas: zwischen zwei Siegelblöcken keine Innenflächen. */
    @Override
    protected boolean skipRendering(BlockState state, BlockState adjacent, Direction direction) {
        return adjacent.is(this) || super.skipRendering(state, adjacent, direction);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        DragonType element = state.getValue(ELEMENT);
        if (random.nextInt(3) == 0) {
            level.addParticle(new DustParticleOptions(element.color(), 0.8f), pos.getX() + random.nextDouble(), pos.getY() + 1.02,
                    pos.getZ() + random.nextDouble(), 0.0, 0.02, 0.0);
        }
        if (random.nextInt(6) == 0) {
            level.addParticle(ParticleTypes.ENCHANT, pos.getX() + random.nextDouble(), pos.getY() + 1.3, pos.getZ() + random.nextDouble(),
                    (random.nextDouble() - 0.5) * 0.4, -0.6, (random.nextDouble() - 0.5) * 0.4);
        }
    }
}
