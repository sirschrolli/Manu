package io.github.sirschrolli.kingdomsatwar.client.hero;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.hero.CrystalItem;
import io.github.sirschrolli.kingdomsatwar.hero.Crystals;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

/**
 * Auf dem Bildschirm: Am Herz- oder Manakristall steht, wie viele man schon aufgenommen hat; über der Schnellleiste
 * erscheint kurz, wie viel Heldenerfahrung dazukam.
 */
@EventBusSubscriber(modid = KingdomsAtWar.MODID, value = Dist.CLIENT)
public final class HeroClient {
    private HeroClient() {}

    @SubscribeEvent
    static void registerLayers(net.neoforged.neoforge.client.event.RegisterGuiLayersEvent event) {
        event.registerAbove(net.neoforged.neoforge.client.gui.VanillaGuiLayers.ARMOR_LEVEL, HeroHud.LAYER, HeroHud::render);
    }

    @SubscribeEvent
    static void onTooltip(ItemTooltipEvent event) {
        Player player = event.getEntity();
        if (player == null || !(event.getItemStack().getItem() instanceof CrystalItem crystal)) {
            return;
        }
        int absorbed = Crystals.of(player).of(crystal.kind());
        event.getToolTip().add(Component.translatable("tooltip.kingdomsatwar.crystal.count", absorbed, Crystals.MAX)
                .withStyle(absorbed >= Crystals.MAX ? ChatFormatting.DARK_GRAY : crystal.kind() == Crystals.Kind.HEART ? ChatFormatting.RED
                        : ChatFormatting.AQUA));
    }
}
