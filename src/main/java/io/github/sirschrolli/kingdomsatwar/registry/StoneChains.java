package io.github.sirschrolli.kingdomsatwar.registry;

import java.util.List;
import java.util.function.Supplier;
import net.minecraft.world.level.block.Block;

/**
 * Natürliches Gestein und sein Bruchstein – wie Stein und Bruchstein in Vanilla: Abgebaut gibt der Fels Bruchstein
 * (mit Behutsamkeit sich selbst), aus Bruchstein werden Steinwerkzeuge, Öfen, Brau- und Steinmetzwaren, und im Ofen wird
 * er wieder zu glattem Fels.
 */
public final class StoneChains {
    /** Ein Gestein und der Bruchstein, den es beim Abbau gibt. */
    public record Chain(Supplier<? extends Block> stone, BuildingSet cobble) {}

    private StoneChains() {}

    public static List<Chain> all() {
        return List.of(
                new Chain(ModBlocks.STORMSTONE.base(), ModBlocks.COBBLED_STORMSTONE),
                new Chain(ModBlocks.VOLCANIC_ROCK.base(), ModBlocks.COBBLED_VOLCANIC_ROCK),
                new Chain(ModBlocks.RIME_STONE.base(), ModBlocks.COBBLED_RIME_STONE),
                new Chain(ModBlocks.SHALE.base(), ModBlocks.COBBLED_SHALE),
                new Chain(ModBlocks.ABYSSAL_STONE.base(), ModBlocks.COBBLED_ABYSSAL_STONE),
                new Chain(ShadowBlocks.SHADOW_STONE.base(), ShadowBlocks.COBBLED_SHADOW_STONE),
                new Chain(ShadowBlocks.DEEP_SHADOW_STONE, ShadowBlocks.COBBLED_DEEP_SHADOW_STONE));
    }
}
