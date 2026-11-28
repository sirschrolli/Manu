package io.github.sirschrolli.kingdomsatwar.economy.town;

import io.github.sirschrolli.kingdomsatwar.registry.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.food.FoodProperties;
import java.util.Map;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Essen im Lager der Siedlung. Bürger brauchen jeden Tag eine Mahlzeit ({@link #MEAL} Nährwert, etwas mehr als ein
 * Brot), Soldaten ihre Ration gleicher Größe. Was zählt, ist der Nährwert: ein Steak reicht für mehr als eine Möhre. Was
 * die Bürger selbst zubereiten, zählt wie das fertige Essen – Weizen wird Brot, Kartoffeln werden gebacken, Fleisch und
 * Fisch gebraten –, so ernährt die Ernte der Bauern die Siedlung. Verdorbenes, Giftiges und Kostbares (Goldäpfel,
 * goldene Karotten) nimmt niemand.
 */
public final class TownFood {
    /** Nährwert einer Mahlzeit bzw. einer Tagesration. */
    public static final int MEAL = 6;

    /** Roh im Lager, aber von den Bürgern (oder dem Koch im Feldlager) zubereitet: Nährwert des fertigen Essens. */
    private static final Map<Item, Integer> PREPARED = Map.of(
            Items.WHEAT, 2,          // drei Weizen sind eine Mahlzeit Brot
            Items.POTATO, 5,         // gebacken
            Items.BEEF, 8, Items.PORKCHOP, 8, Items.MUTTON, 6, Items.CHICKEN, 6, Items.RABBIT, 5,
            Items.COD, 5, Items.SALMON, 6);

    private TownFood() {}

    /** Nährwert eines einzelnen Gegenstands (0: kein brauchbares Essen). */
    public static int nutrition(ItemStack stack) {
        if (stack.isEmpty() || stack.is(ModTags.Items.UNFIT_FOOD)) {
            return 0;
        }
        Integer prepared = PREPARED.get(stack.getItem());
        if (prepared != null) {
            return prepared;
        }
        FoodProperties food = stack.get(DataComponents.FOOD);
        return food == null ? 0 : Math.max(0, food.nutrition());
    }

    /** Wie viel Nährwert das Lager der Siedlung hat (nur geladene Truhen und Fässer). */
    public static int pantry(ServerLevel level, Town town) {
        int total = 0;
        for (BlockPos pos : TownStorage.containers(level, town)) {
            Container c = TownStorage.container(level, pos);
            if (c == null) {
                continue;
            }
            for (int i = 0; i < c.getContainerSize(); i++) {
                ItemStack stack = c.getItem(i);
                total += nutrition(stack) * stack.getCount();
            }
        }
        return total;
    }

    /**
     * Nimmt Essen für (mindestens) {@code points} Nährwert aus dem Lager – erst das Einfache (wenig Nährwert je Stück),
     * damit Braten und Brot für später bleiben. Gibt zurück, wie viel es wirklich war.
     */
    public static int take(ServerLevel level, Town town, int points) {
        int got = 0;
        java.util.List<BlockPos> containers = TownStorage.containers(level, town);
        for (int max = 2; max <= 20 && got < points; max += 2) {
            for (BlockPos pos : containers) {
                Container c = TownStorage.container(level, pos);
                if (c == null) {
                    continue;
                }
                for (int i = 0; i < c.getContainerSize() && got < points; i++) {
                    ItemStack stack = c.getItem(i);
                    int each = nutrition(stack);
                    if (each <= 0 || each > max || each <= max - 2) {
                        continue;
                    }
                    int n = Math.min(stack.getCount(), (points - got + each - 1) / each);
                    stack.shrink(n);
                    got += n * each;
                    c.setChanged();
                }
            }
        }
        return got;
    }

    /** Für wie viele Tage das Essen im Lager die Bürger satt macht. */
    public static int days(int pantry, int eaters) {
        return eaters <= 0 ? 0 : pantry / (MEAL * eaters);
    }
}
