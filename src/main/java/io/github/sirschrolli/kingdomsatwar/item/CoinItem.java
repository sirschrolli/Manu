package io.github.sirschrolli.kingdomsatwar.item;

import io.github.sirschrolli.kingdomsatwar.economy.Coin;
import io.github.sirschrolli.kingdomsatwar.economy.Coins;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

/**
 * Eine Münze. Rechtsklick wechselt zehn Münzen in die nächstgrößere (10 Bronze → 1 Silber, 10 Silber → 1 Gold),
 * Schleichen + Rechtsklick eine Münze in zehn kleinere.
 */
public class CoinItem extends Item {
    private final Coin coin;

    public CoinItem(Coin coin, Item.Properties properties) {
        super(properties);
        this.coin = coin;
    }

    public Coin coin() {
        return this.coin;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isSecondaryUseActive()) {
            Coin smaller = this.coin.smaller();
            if (smaller == null) {
                return InteractionResult.PASS;
            }
            if (!level.isClientSide()) {
                stack.shrink(1);
                // ausdrücklich zehn kleinere Münzen (Coins.give würde sie wieder zur großen zusammenfassen)
                player.getInventory().placeItemBackInInventory(new ItemStack(smaller.item(), 10), net.minecraft.util.Prediction.SERVER_ONLY);
                clink(level, player, 1.25f);
            }
            return InteractionResult.SUCCESS;
        }
        Coin larger = this.coin.larger();
        if (larger == null) {
            return InteractionResult.PASS;
        }
        if (stack.getCount() < 10) {
            if (!level.isClientSide()) {
                player.sendOverlayMessage(Component.translatable("message.kingdomsatwar.coin.need_ten").withStyle(ChatFormatting.GRAY));
            }
            return InteractionResult.FAIL;
        }
        if (!level.isClientSide()) {
            stack.shrink(10);
            player.getInventory().placeItemBackInInventory(new ItemStack(larger.item()), net.minecraft.util.Prediction.SERVER_ONLY);
            clink(level, player, 0.9f);
        }
        return InteractionResult.SUCCESS;
    }

    private static void clink(Level level, Player player, float pitch) {
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.CHAIN_HIT, SoundSource.PLAYERS, 0.7f, 1.6f * pitch);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.5f,
                1.4f * pitch);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder,
            TooltipFlag flag) {
        builder.accept(Component.translatable("item.kingdomsatwar.coin.value", Coins.format(this.coin.value()))
                .withStyle(ChatFormatting.GRAY));
        if (stack.getCount() > 1) {
            builder.accept(Component.translatable("item.kingdomsatwar.coin.stack_value", Coins.format(Coins.value(stack)))
                    .withStyle(ChatFormatting.GRAY));
        }
        if (this.coin.larger() != null) {
            builder.accept(Component.translatable("item.kingdomsatwar.coin.combine",
                    Component.translatable(this.coin.larger().item().getDescriptionId())).withStyle(ChatFormatting.DARK_GRAY));
        }
        if (this.coin.smaller() != null) {
            builder.accept(Component.translatable("item.kingdomsatwar.coin.split").withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}
