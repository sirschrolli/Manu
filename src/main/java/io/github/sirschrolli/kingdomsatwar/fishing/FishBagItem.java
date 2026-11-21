package io.github.sirschrolli.kingdomsatwar.fishing;

import java.util.List;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

/**
 * Der Fischbeutel (seit 0.81): Wer ihn bei sich trägt, dem wandert jeder geangelte Fisch hinein. Rechtsklick mit dem Beutel
 * in der Hand öffnet ihn – dort nimmt man die Fische wieder heraus. Im Inventar lassen sich Fische auch von Hand hineinlegen
 * (Fisch auf den Beutel klicken oder mit dem Beutel auf den Fisch). Wie viel er fasst, bestimmt seine Stufe ({@link FishBag}).
 */
public class FishBagItem extends Item {
    private static final int BAR_COLOR = 0xFF5AB4E0;
    private static final int FULL_COLOR = 0xFFE05A4A;

    public FishBagItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (player instanceof ServerPlayer serverPlayer) {
            FishBag.open(serverPlayer, hand);
        }
        return InteractionResult.SUCCESS;
    }

    /** Ein Fisch (am Mauszeiger) wird auf den Beutel gelegt. */
    @Override
    public boolean overrideOtherStackedOnMe(ItemStack self, ItemStack other, Slot slot, ClickAction clickAction, Player player, SlotAccess carriedItem) {
        FishSpecies species = FishBag.speciesOf(other);
        if (species == null || !slot.allowModification(player)) {
            return false;
        }
        int put = FishBag.insert(self, species, clickAction == ClickAction.SECONDARY ? 1 : other.getCount());
        if (put > 0) {
            other.shrink(put);
            this.sound(player, true);
        } else {
            this.sound(player, false);
        }
        return true;
    }

    /** Der Beutel (am Mauszeiger) wird auf einen Fisch im Inventar gesetzt: Der Stapel wandert hinein. */
    @Override
    public boolean overrideStackedOnOther(ItemStack self, Slot slot, ClickAction clickAction, Player player) {
        ItemStack other = slot.getItem();
        FishSpecies species = FishBag.speciesOf(other);
        if (species == null || !slot.mayPickup(player)) {
            return false;
        }
        int room = Math.min(clickAction == ClickAction.SECONDARY ? 1 : other.getCount(), FishBag.of(self).free());
        if (room > 0) {
            // regelgerecht aus dem Fach nehmen (wie ein Bündel), dann hinein
            ItemStack taken = slot.safeTake(other.getCount(), room, player);
            FishBag.insert(self, species, taken.getCount());
            this.sound(player, true);
        } else {
            this.sound(player, false);
        }
        return true;
    }

    private void sound(Player player, boolean success) {
        player.playSound(success ? SoundEvents.BUNDLE_INSERT : SoundEvents.BUNDLE_INSERT_FAIL, 0.8f, 0.8f + player.getRandom().nextFloat() * 0.4f);
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return FishBag.of(stack).total() > 0;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        FishBag bag = FishBag.of(stack);
        return Math.min(13, 1 + Math.round(12.0f * bag.total() / Math.max(1, bag.capacity())));
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return FishBag.of(stack).free() == 0 ? FULL_COLOR : BAR_COLOR;
    }

    /** Der Inhalt ändert sich bei jedem Fang – der Beutel in der Hand soll dabei nicht jedes Mal neu „gezogen“ werden. */
    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged || !oldStack.is(newStack.getItem());
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
        FishBag bag = FishBag.of(stack);
        if (FishItem.isDisplay(stack)) {
            // das Bild der Erweiterung im Handelsfenster des Fischers
            builder.accept(Component.translatable("item.kingdomsatwar.fish_bag.upgrade_hint").withStyle(ChatFormatting.GRAY));
            return;
        }
        builder.accept(Component.translatable("item.kingdomsatwar.fish_bag.level", bag.level() + 1).withStyle(ChatFormatting.AQUA)
                .append(Component.literal("  ").append(Component.translatable("item.kingdomsatwar.fish_bag.fill", bag.total(), bag.capacity())
                        .withStyle(bag.free() == 0 ? ChatFormatting.RED : ChatFormatting.GRAY))));
        List<FishBag.Entry> entries = bag.entries();
        int shown = 0;
        for (FishBag.Entry entry : entries) {
            if (shown == 6) {
                builder.accept(Component.translatable("item.kingdomsatwar.fish_bag.more", entries.size() - shown).withStyle(ChatFormatting.DARK_GRAY));
                break;
            }
            builder.accept(Component.literal(" " + entry.count() + "× ").withStyle(ChatFormatting.GRAY)
                    .append(new ItemStack(entry.species().item()).getHoverName().copy().withColor(entry.species().rarity().color)));
            shown++;
        }
        builder.accept(Component.translatable("item.kingdomsatwar.fish_bag.hint.catch").withStyle(ChatFormatting.DARK_GREEN));
        builder.accept(Component.translatable("item.kingdomsatwar.fish_bag.hint.open").withStyle(ChatFormatting.DARK_GREEN));
        builder.accept(Component.translatable("item.kingdomsatwar.fish_bag.hint.upgrade").withStyle(ChatFormatting.DARK_GRAY));
    }
}
