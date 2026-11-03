package io.github.sirschrolli.kingdomsatwar.client.rules;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

/**
 * Die Regeln auf dem Client: Die Debug-Anzeige (F3) bleibt immer verkürzt – keine Koordinaten, kein Kopieren der Position
 * –, auch wenn ein Server das Signal nicht schickt. Verzauberte Bücher mit den Verzauberungen dieser Mod erklären, was
 * die Verzauberung bewirkt.
 */
@EventBusSubscriber(modid = KingdomsAtWar.MODID, value = Dist.CLIENT)
public final class RulesClient {
    private RulesClient() {}

    @SubscribeEvent
    static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null && !minecraft.player.isReducedDebugInfo()) {
            minecraft.player.setReducedDebugInfo(true);
        }
    }

    @SubscribeEvent
    static void onTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        ItemEnchantments stored = stack.get(DataComponents.STORED_ENCHANTMENTS);
        if (stored == null || stored.isEmpty()) {
            return;
        }
        for (Holder<Enchantment> enchantment : stored.keySet()) {
            enchantment.unwrapKey()
                    .filter(key -> key.identifier().getNamespace().equals(KingdomsAtWar.MODID))
                    .ifPresent(key -> event.getToolTip().add(Component.translatable(
                            "enchantment." + KingdomsAtWar.MODID + "." + key.identifier().getPath() + ".desc").withStyle(ChatFormatting.DARK_GRAY)));
        }
    }
}
