package io.github.sirschrolli.kingdomsatwar.client.rules;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.enchant.EnchantPower;
import io.github.sirschrolli.kingdomsatwar.rules.WeaponPower;
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
 * die Verzauberung bewirkt; verzauberte Waffen der Mod zeigen, was Schärfe, Bann und Nemesis auf ihnen bringen.
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

    /**
     * Waffen der Mod: was Schärfe, Bann und Nemesis hier bringen. Minecraft zeigt den Betrag nicht an – und auf diesen Waffen
     * ist er größer als gewohnt und stärkt auch ihre Fähigkeiten ({@link WeaponPower}).
     */
    private static void weaponEnchantments(ItemStack stack, java.util.List<Component> tooltip) {
        ItemEnchantments enchantments = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        if (enchantments.isEmpty() || !WeaponPower.ours(stack) || WeaponPower.baseDamage(stack) < 2.0f) {
            return;
        }
        float scale = WeaponPower.scale(stack);
        boolean ability = stack.getItem() instanceof io.github.sirschrolli.kingdomsatwar.rules.AbilityWeapon;
        for (var entry : enchantments.entrySet()) {
            Holder<Enchantment> enchantment = entry.getKey();
            int level = entry.getIntValue();
            String kind;
            float flat;
            if (enchantment.is(net.minecraft.world.item.enchantment.Enchantments.SHARPNESS)) {
                kind = "sharpness";
                flat = 0.5f * level + 0.5f + EnchantPower.SHARPNESS_EXTRA * level;
            } else if (enchantment.is(net.minecraft.world.item.enchantment.Enchantments.SMITE)) {
                kind = "smite";
                flat = (2.5f + EnchantPower.SLAYER_EXTRA) * level;
            } else if (enchantment.is(net.minecraft.world.item.enchantment.Enchantments.BANE_OF_ARTHROPODS)) {
                kind = "bane";
                flat = (2.5f + EnchantPower.SLAYER_EXTRA) * level;
            } else {
                continue;
            }
            String amount = net.minecraft.world.item.component.ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(flat * scale);
            tooltip.add(Component.translatable("tooltip.kingdomsatwar.enchant." + kind, amount).withStyle(ChatFormatting.DARK_GREEN));
            if (ability) {
                tooltip.add(Component.translatable("tooltip.kingdomsatwar.enchant.ability", Math.round(flat / WeaponPower.REFERENCE * 100.0f))
                        .withStyle(ChatFormatting.DARK_GREEN));
            }
        }
    }

    @SubscribeEvent
    static void onTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        weaponEnchantments(stack, event.getToolTip());
        ItemEnchantments stored = stack.get(DataComponents.STORED_ENCHANTMENTS);
        if (stored == null || stored.isEmpty()) {
            return;
        }
        boolean boosted = false;
        for (Holder<Enchantment> enchantment : stored.keySet()) {
            boosted |= enchantment.unwrapKey().filter(EnchantPower.BOOSTED::contains).isPresent();
            enchantment.unwrapKey()
                    .filter(key -> key.identifier().getNamespace().equals(KingdomsAtWar.MODID))
                    .ifPresent(key -> event.getToolTip().add(Component.translatable(
                            "enchantment." + KingdomsAtWar.MODID + "." + key.identifier().getPath() + ".desc").withStyle(ChatFormatting.DARK_GRAY)));
        }
        // Schärfe, Schutz, Effizienz … wirken in dieser Mod etwas stärker (EnchantPower)
        if (boosted) {
            event.getToolTip().add(Component.translatable("tooltip.kingdomsatwar.enchant.boost").withStyle(ChatFormatting.DARK_GREEN));
        }
    }
}
