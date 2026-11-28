package io.github.sirschrolli.kingdomsatwar.block;

import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Glutschote (seit 1.16): ein niedriger Strauch der Unterwelt mit glühend roten Schoten. Er wächst wie Netherwarze – in
 * vier Stufen, langsam, ohne Licht und ohne Wasser –, aber auf Seelenerde (und auf Seelensand); die Schreiter-Hirten
 * ziehen ihn in Beeten am Lavaufer. Reif trägt er zwei bis vier Schoten.
 */
public class EmberPepperBlock extends NetherWartBlock {
    private final Supplier<? extends ItemLike> pepper;

    public EmberPepperBlock(Supplier<? extends ItemLike> pepper, Properties properties) {
        super(properties);
        this.pepper = pepper;
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(Blocks.SOUL_SOIL) || state.is(BlockTags.SUPPORTS_NETHER_WART);
    }

    @Override
    protected ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
        return new ItemStack(this.pepper.get());
    }
}
