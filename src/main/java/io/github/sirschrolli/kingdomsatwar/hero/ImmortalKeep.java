package io.github.sirschrolli.kingdomsatwar.hero;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.registry.ModAttachments;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/**
 * Das Licht der Unsterblichkeit bewahrt auch das Hab und Gut (seit 0.78): Wer in einer Kampfdimension fällt, verliert dort
 * sonst alles, was er bei sich trägt – ein Unsterblicher (Heldenstufe {@value HeroLevel#MAX_LEVEL}) nicht. Beim Tod wird sein
 * Inventar (mit Rüstung, Zweithand, dem Gegenstand am Mauszeiger und dem Handwerksfeld) verwahrt, damit nichts zu Boden
 * fällt, und beim Wiedererscheinen liegt alles wieder an seinem Platz.
 */
@EventBusSubscriber(modid = KingdomsAtWar.MODID)
public final class ImmortalKeep {
    /** Ein verwahrter Gegenstand und sein Platz im Inventar (-1: irgendwo, etwa vom Mauszeiger). */
    public record Slot(int slot, ItemStack stack) {
        public static final Codec<Slot> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.INT.fieldOf("slot").forGetter(Slot::slot),
                ItemStack.CODEC.fieldOf("stack").forGetter(Slot::stack)).apply(i, Slot::new));
    }

    /** Was verwahrt ist. */
    public record Kept(List<Slot> items) {
        public static final Kept NONE = new Kept(List.of());
        public static final Codec<Kept> CODEC = Slot.CODEC.listOf().xmap(Kept::new, Kept::items);

        public boolean isEmpty() {
            return this.items.isEmpty();
        }
    }

    private ImmortalKeep() {}

    /** Behält dieser Spieler sein Inventar, wenn er in einer Kampfdimension fällt? */
    public static boolean keeps(Player player) {
        return HeroLevel.of(player).level() >= HeroLevel.MAX_LEVEL;
    }

    /** Verwahrt alles, was der Fallende bei sich trägt (danach ist sein Inventar leer – es fällt nichts zu Boden). */
    public static void keep(ServerPlayer player) {
        Inventory inventory = player.getInventory();
        List<Slot> items = new ArrayList<>();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (!stack.isEmpty()) {
                items.add(new Slot(i, stack.copy()));
            }
        }
        ItemStack carried = player.containerMenu.getCarried();
        if (!carried.isEmpty()) {
            items.add(new Slot(-1, carried.copy()));
        }
        var craft = player.inventoryMenu.getCraftSlots();
        for (int i = 0; i < craft.getContainerSize(); i++) {
            if (!craft.getItem(i).isEmpty()) {
                items.add(new Slot(-1, craft.getItem(i).copy()));
            }
        }
        // was schon verwahrt war (etwa zweimal hintereinander gefallen, ohne wiederzukehren), bleibt dabei
        List<Slot> all = new ArrayList<>(player.getData(ModAttachments.KEPT_INVENTORY).items());
        all.addAll(items);
        player.setData(ModAttachments.KEPT_INVENTORY, new Kept(all));
        inventory.clearContent();
        player.containerMenu.setCarried(ItemStack.EMPTY);
        craft.clearContent();
        player.sendSystemMessage(Component.translatable("hero.kingdomsatwar.immortal.kept").withStyle(ChatFormatting.GOLD));
    }

    /** Beim Wiedererscheinen liegt alles wieder an seinem Platz (was dort nicht passt, findet einen freien). */
    @SubscribeEvent
    static void onClone(PlayerEvent.Clone event) {
        if (!event.isWasDeath()) {
            return;
        }
        Player original = event.getOriginal();
        Kept kept = original.getData(ModAttachments.KEPT_INVENTORY);
        if (kept.isEmpty()) {
            return;
        }
        Player player = event.getEntity();
        restore(player, kept);
        original.setData(ModAttachments.KEPT_INVENTORY, Kept.NONE);
        player.setData(ModAttachments.KEPT_INVENTORY, Kept.NONE);
    }

    /** Legt das Verwahrte zurück ins Inventar. */
    public static void restore(Player player, Kept kept) {
        Inventory inventory = player.getInventory();
        List<ItemStack> loose = new ArrayList<>();
        for (Slot slot : kept.items()) {
            if (slot.slot() >= 0 && slot.slot() < inventory.getContainerSize() && inventory.getItem(slot.slot()).isEmpty()) {
                inventory.setItem(slot.slot(), slot.stack().copy());
            } else {
                loose.add(slot.stack().copy());
            }
        }
        for (ItemStack stack : loose) {
            if (!inventory.add(stack)) {
                player.drop(stack, false, net.minecraft.util.Prediction.SERVER_ONLY);
            }
        }
    }
}
