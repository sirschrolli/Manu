package io.github.sirschrolli.kingdomsatwar.relic;

import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

/** Ein Apfel der Macht: Was er gibt, steht an ihm (Minecraft verschweigt es bei seinen goldenen Äpfeln). */
public class PowerAppleItem extends Item {
    private final PowerApple apple;

    public PowerAppleItem(PowerApple apple, Item.Properties properties) {
        super(properties);
        this.apple = apple;
    }

    public PowerApple apple() {
        return this.apple;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
        builder.accept(Component.translatable("apple.kingdomsatwar.eaten").withColor(this.apple.color()));
        for (MobEffectInstance effect : this.apple.effects()) {
            MutableComponent line = Component.translatable(effect.getEffect().value().getDescriptionId());
            if (effect.getAmplifier() > 0) {
                // Stufen bis X (die Tränke von Minecraft kennen nur sechs)
                line.append(" ").append(Component.translatable("enchantment.level." + (effect.getAmplifier() + 1)));
            }
            line.append(" (" + MobEffectUtil.formatDuration(effect, 1.0f, context.tickRate()).getString() + ")");
            builder.accept(Component.literal(" ").append(line).withStyle(ChatFormatting.BLUE));
        }
        if (this.apple == PowerApple.IMMORTAL) {
            builder.accept(Component.translatable("apple.kingdomsatwar.second_life").withStyle(ChatFormatting.GOLD));
        }
    }
}
