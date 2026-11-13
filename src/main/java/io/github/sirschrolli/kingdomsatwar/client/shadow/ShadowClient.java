package io.github.sirschrolli.kingdomsatwar.client.shadow;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.registry.ShadowBlocks;
import io.github.sirschrolli.kingdomsatwar.world.ShadowGate;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

/**
 * Client-Seite der Schattenwelt: Schattengras, Schattenhalme und Schattenlaub nehmen die Farben ihres Bioms an (im
 * Wald tiefes Violett, auf den Glimmerweiten Lavendel, im Seelensumpf ein fahles Grün). In anderen Welten bleiben sie
 * violett.
 */
@EventBusSubscriber(modid = KingdomsAtWar.MODID, value = Dist.CLIENT)
public final class ShadowClient {
    /** Farben außerhalb einer Welt (zum Beispiel für fallende Blöcke). */
    private static final int GRASS_FALLBACK = 0xFF5A3E8E;
    private static final int LEAVES_FALLBACK = 0xFF6C3EA8;

    private ShadowClient() {}

    @SubscribeEvent
    static void registerBlockTints(RegisterColorHandlersEvent.BlockTintSources event) {
        event.register(List.of(biomeTint(GRASS_FALLBACK, false, false)), ShadowBlocks.SHADOW_GRASS.get());
        event.register(List.of(biomeTint(GRASS_FALLBACK, false, true)), ShadowBlocks.SHADOW_SPROUTS.get());
        event.register(List.of(biomeTint(LEAVES_FALLBACK, true, true)), ShadowBlocks.SHADOW_LEAVES.get());
    }

    /**
     * Farbe aus dem Biom (Gras- oder Laubfarbe). Beim Grasblock bleiben die Bruchstücke ungefärbt, weil seine Seite
     * schon die Grundfarbe trägt – genau wie beim Gras der Oberwelt.
     */
    private static BlockTintSource biomeTint(int fallback, boolean foliage, boolean tintParticles) {
        return new BlockTintSource() {
            @Override
            public int color(BlockState state) {
                return fallback;
            }

            @Override
            public int colorInWorld(BlockState state, BlockAndTintGetter level, BlockPos pos) {
                // Außerhalb der Schattenwelt (z. B. als Mitbringsel in der Oberwelt) behalten die Pflanzen ihr Violett
                ClientLevel world = Minecraft.getInstance().level;
                if (world == null || world.dimension() != ShadowGate.SHADOW_WORLD) {
                    return fallback;
                }
                return foliage ? BiomeColors.getAverageFoliageColor(level, pos) : BiomeColors.getAverageGrassColor(level, pos);
            }

            @Override
            public int colorAsTerrainParticle(BlockState state, BlockAndTintGetter level, BlockPos pos) {
                return tintParticles ? this.colorInWorld(state, level, pos) : -1;
            }
        };
    }
}
