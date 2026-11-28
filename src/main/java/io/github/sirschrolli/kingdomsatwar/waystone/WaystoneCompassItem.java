package io.github.sirschrolli.kingdomsatwar.waystone;

import io.github.sirschrolli.kingdomsatwar.network.ModNetwork;
import io.github.sirschrolli.kingdomsatwar.network.OpenWaystoneCompassPayload;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/**
 * Der Wegkompass (seit 1.12) ersetzt den Kompass von Minecraft (der lässt sich nicht mehr herstellen): Er merkt sich bis zu acht
 * Wegsteine – einen Wegstein mit ihm anklicken – und zeigt zu dem, den man im Kompassfenster wählt (Rechtsklick); ohne
 * Ziel zeigt er zum Spawn. Steht ein gewählter Wegstein nicht mehr, merkt er es, sobald man in seine Nähe kommt.
 */
public class WaystoneCompassItem extends Item {
    public WaystoneCompassItem(Properties properties) {
        super(properties);
    }

    /** Rechtsklick in die Luft: das Kompassfenster mit den gemerkten Wegsteinen. */
    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (player instanceof ServerPlayer serverPlayer) {
            ModNetwork.send(serverPlayer, new OpenWaystoneCompassPayload(hand == InteractionHand.MAIN_HAND));
        }
        return InteractionResult.SUCCESS;
    }

    /** Alle zwei Sekunden: Steht das Ziel noch? (Nur in seiner Nähe prüfbar.) */
    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity owner, @Nullable EquipmentSlot slot) {
        if (owner.tickCount % 40 != 17 || !(owner instanceof ServerPlayer player)) {
            return;
        }
        Waypoints waypoints = WaystoneCompass.of(stack);
        Waypoints.Waypoint target = waypoints.target();
        if (target != null && target.pos().dimension().equals(level.dimension()) && target.pos().pos().closerToCenterThan(player.position(), 24.0)
                && WaystoneCompass.gone(level.getServer(), target)) {
            WaystoneCompass.set(stack, waypoints.without(waypoints.selected()));
            player.sendOverlayMessage(Component.translatable("message.kingdomsatwar.waystone.gone", target.name()).withStyle(ChatFormatting.RED));
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder,
            TooltipFlag flag) {
        Waypoints waypoints = WaystoneCompass.of(stack);
        builder.accept(Component.translatable("item.kingdomsatwar.waystone_compass.count", waypoints.entries().size(), Waypoints.MAX)
                .withStyle(ChatFormatting.GRAY));
        Waypoints.Waypoint target = waypoints.target();
        builder.accept(target == null ? Component.translatable("item.kingdomsatwar.waystone_compass.spawn").withStyle(ChatFormatting.DARK_GRAY)
                : Component.translatable("item.kingdomsatwar.waystone_compass.target", target.name().copy().withStyle(ChatFormatting.AQUA))
                        .withStyle(ChatFormatting.GRAY));
        builder.accept(Component.translatable("item.kingdomsatwar.waystone_compass.hint").withStyle(ChatFormatting.DARK_GRAY));
    }
}
