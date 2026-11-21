package io.github.sirschrolli.kingdomsatwar.magic;

import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

/**
 * Zaubertafel: eine Steintafel aus dem Turm eines Erzmagiers, in die ein Zauber seines Elements geritzt ist. Am
 * Magietisch schreibt man sie in den Aetherit-Stab ein – der Stab lernt den Zauber oder steigt eine Stufe auf.
 */
public class SpellTabletItem extends Item {
    private final Spell spell;

    public SpellTabletItem(Spell spell, Properties properties) {
        super(properties);
        this.spell = spell;
    }

    public Spell spell() {
        return this.spell;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder,
            TooltipFlag flag) {
        builder.accept(this.spell.displayName().withColor(this.spell.color()));
        builder.accept(this.spell.description().withStyle(ChatFormatting.GRAY));
        builder.accept(Component.translatable("tooltip.kingdomsatwar.spell_tablet.use").withStyle(ChatFormatting.DARK_PURPLE));
    }
}
