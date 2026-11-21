package io.github.sirschrolli.kingdomsatwar.relic;

import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

/** Ein Totem der Mod: Was es in der Zweithand und beim Tod tut, steht an ihm selbst. */
public class TotemItem extends Item {
    private final TotemKind kind;

    public TotemItem(TotemKind kind, Item.Properties properties) {
        super(properties);
        this.kind = kind;
    }

    public TotemKind kind() {
        return this.kind;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
        if (this.kind.passiveLines() > 0) {
            builder.accept(Component.translatable("totem.kingdomsatwar.offhand").withColor(this.kind.color()));
            for (int i = 1; i <= this.kind.passiveLines(); i++) {
                builder.accept(Component.literal(" ").append(Component.translatable(this.kind.key("passive." + i))).withStyle(ChatFormatting.GRAY));
            }
        }
        builder.accept(Component.translatable("totem.kingdomsatwar.death").withColor(this.kind.color()));
        for (int i = 1; i <= this.kind.reviveLines(); i++) {
            builder.accept(Component.literal(" ").append(Component.translatable(this.kind.key("revive." + i))).withStyle(ChatFormatting.GRAY));
        }
        if (this.kind.charges() > 1) {
            int left = stack.getMaxDamage() - stack.getDamageValue();
            builder.accept(Component.translatable("totem.kingdomsatwar.charges", left, stack.getMaxDamage()).withStyle(ChatFormatting.GOLD));
        }
    }
}
