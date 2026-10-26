package io.github.sirschrolli.kingdomsatwar.economy;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Prediction;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Rechnen und Bezahlen mit Münzen. Bezahlt wird mit allen Münzen im Inventar: erst mit Bronze, dann mit Silber, zuletzt
 * mit Gold – reicht es nicht genau, wird eine größere Münze gewechselt und das Wechselgeld zurückgegeben. Wer im
 * Kreativmodus spielt, bezahlt nichts.
 */
public final class Coins {
    private Coins() {}

    /** Wert eines Stapels in Bronze (0, wenn es keine Münzen sind). */
    public static int value(ItemStack stack) {
        Coin coin = Coin.of(stack.getItem());
        return coin == null ? 0 : coin.value() * stack.getCount();
    }

    /** Wie viele Münzen einer Art im Inventar (Hauptinventar und zweite Hand) liegen. */
    public static int count(Player player, Coin coin) {
        int count = 0;
        for (ItemStack stack : slots(player)) {
            if (stack.is(coin.item())) {
                count += stack.getCount();
            }
        }
        return count;
    }

    /** Gesamtwert aller Münzen im Inventar, in Bronze. */
    public static int total(Player player) {
        int total = 0;
        for (ItemStack stack : slots(player)) {
            total += value(stack);
        }
        return total;
    }

    public static boolean canAfford(Player player, int price) {
        return price <= 0 || player.hasInfiniteMaterials() || total(player) >= price;
    }

    /**
     * Bezahlt {@code price} Bronze (mit Wechselgeld). Gibt {@code false} zurück und nimmt nichts, wenn das Geld nicht
     * reicht.
     */
    public static boolean pay(Player player, int price) {
        if (price <= 0 || player.hasInfiniteMaterials()) {
            return true;
        }
        int bronze = count(player, Coin.BRONZE);
        int silver = count(player, Coin.SILVER);
        int gold = count(player, Coin.GOLD);
        if (bronze + silver * 10 + gold * 100 < price) {
            return false;
        }
        int rest = price;
        int useBronze = Math.min(bronze, rest);
        rest -= useBronze;
        int useSilver = Math.min(silver, ceilDiv(Math.max(rest, 0), 10));
        rest -= useSilver * 10;
        int useGold = Math.min(gold, ceilDiv(Math.max(rest, 0), 100));
        rest -= useGold * 100;
        if (rest > 0) {
            return false;   // kann bei genug Geld nicht vorkommen, aber sicher ist sicher
        }
        remove(player, Coin.BRONZE, useBronze);
        remove(player, Coin.SILVER, useSilver);
        remove(player, Coin.GOLD, useGold);
        give(player, -rest);
        return true;
    }

    /** Gibt dem Spieler {@code amount} Bronze in möglichst großen Münzen (was nicht passt, fällt vor seine Füße). */
    public static void give(Player player, int amount) {
        for (ItemStack stack : stacks(amount)) {
            player.getInventory().placeItemBackInInventory(stack, Prediction.SERVER_ONLY);
        }
    }

    /** Ein Betrag als Münzstapel (größte Münzen zuerst; mehr als 64 Münzen einer Art ergeben mehrere Stapel). */
    public static List<ItemStack> stacks(int amount) {
        List<ItemStack> stacks = new ArrayList<>();
        int rest = Math.max(0, amount);
        for (int i = Coin.values().length - 1; i >= 0; i--) {
            Coin coin = Coin.values()[i];
            int count = rest / coin.value();
            rest -= count * coin.value();
            while (count > 0) {
                int n = Math.min(count, coin.item().getDefaultMaxStackSize());
                stacks.add(new ItemStack(coin.item(), n));
                count -= n;
            }
        }
        return stacks;
    }

    /** Ein Betrag als Text, z. B. „1 Gold 4 Silber 5 Bronze“ (jede Münze in ihrer Farbe), „0 Bronze“ für nichts. */
    public static MutableComponent format(int amount) {
        MutableComponent text = Component.empty();
        int rest = Math.max(0, amount);
        boolean any = false;
        for (int i = Coin.values().length - 1; i >= 0; i--) {
            Coin coin = Coin.values()[i];
            int count = rest / coin.value();
            rest -= count * coin.value();
            if (count > 0) {
                if (any) {
                    text.append(" ");
                }
                text.append(Component.translatable("coin.kingdomsatwar." + coin.id(), count).withColor(coin.color()));
                any = true;
            }
        }
        if (!any) {
            text.append(Component.translatable("coin.kingdomsatwar.bronze", 0).withColor(Coin.BRONZE.color()));
        }
        return text;
    }

    private static void remove(Player player, Coin coin, int count) {
        Inventory inventory = player.getInventory();
        int rest = count;
        for (int slot = 0; slot < inventory.getContainerSize() && rest > 0; slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.is(coin.item())) {
                int take = Math.min(rest, stack.getCount());
                stack.shrink(take);
                rest -= take;
            }
        }
        inventory.setChanged();
    }

    /** Hauptinventar (mit Schnellleiste) und zweite Hand – Rüstungsplätze zählen nicht. */
    private static List<ItemStack> slots(Player player) {
        List<ItemStack> list = new ArrayList<>(player.getInventory().getNonEquipmentItems());
        list.add(player.getOffhandItem());
        return list;
    }

    private static int ceilDiv(int a, int b) {
        return (a + b - 1) / b;
    }
}
