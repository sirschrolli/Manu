package io.github.sirschrolli.kingdomsatwar.client.fae;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.fae.FaeGate;
import io.github.sirschrolli.kingdomsatwar.registry.FaeBlocks;
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
 * Client-Seite des Feenreichs: Feengras und Feenhalme nehmen die Grasfarbe ihres Bioms an (mintgrün auf den Wiesen,
 * blaugrün im Pilzhain, dunkel in der Dornenwildnis). In anderen Welten bleiben sie mintgrün.
 */
@EventBusSubscriber(modid = KingdomsAtWar.MODID, value = Dist.CLIENT)
public final class FaeClient {
    /** Farbe außerhalb des Feenreichs (und ohne Welt, z. B. für fallende Blöcke). */
    private static final int GRASS_FALLBACK = 0xFF8FE6B4;

    private FaeClient() {}

    @SubscribeEvent
    static void registerBlockTints(RegisterColorHandlersEvent.BlockTintSources event) {
        event.register(List.of(grassTint(false)), FaeBlocks.FAE_GRASS.get());
        event.register(List.of(grassTint(true)), FaeBlocks.FAE_SPROUTS.get());
    }

    /** Grasfarbe aus dem Biom; beim Grasblock bleiben die Bruchstücke ungefärbt (wie beim Gras der Oberwelt). */
    private static BlockTintSource grassTint(boolean tintParticles) {
        return new BlockTintSource() {
            @Override
            public int color(BlockState state) {
                return GRASS_FALLBACK;
            }

            @Override
            public int colorInWorld(BlockState state, BlockAndTintGetter level, BlockPos pos) {
                ClientLevel world = Minecraft.getInstance().level;
                if (world == null || world.dimension() != FaeGate.FAE_REALM) {
                    return GRASS_FALLBACK;
                }
                return BiomeColors.getAverageGrassColor(level, pos);
            }

            @Override
            public int colorAsTerrainParticle(BlockState state, BlockAndTintGetter level, BlockPos pos) {
                return tintParticles ? this.colorInWorld(state, level, pos) : -1;
            }
        };
    }
}
