package io.github.sirschrolli.kingdomsatwar.economy;

import io.github.sirschrolli.kingdomsatwar.registry.ModItems;
import net.minecraft.world.item.Item;
import org.jspecify.annotations.Nullable;

/**
 * Die drei Münzen des Königreichs: 10 Bronzemünzen sind eine Silbermünze, 10 Silbermünzen eine Goldmünze. Alle Preise
 * werden in Bronze gerechnet ({@link #value()}).
 */
public enum Coin {
    BRONZE("bronze", 1, 0xC2793D),
    SILVER("silver", 10, 0xC9D0DC),
    GOLD("gold", 100, 0xF2C23A);

    private final String id;
    private final int value;
    private final int color;

    Coin(String id, int value, int color) {
        this.id = id;
        this.value = value;
        this.color = color;
    }

    public String id() {
        return this.id;
    }

    /** Wert einer Münze in Bronze. */
    public int value() {
        return this.value;
    }

    /** Farbe für Texte (z. B. „3 Silber“). */
    public int color() {
        return this.color;
    }

    public Item item() {
        return switch (this) {
            case BRONZE -> ModItems.BRONZE_COIN.get();
            case SILVER -> ModItems.SILVER_COIN.get();
            case GOLD -> ModItems.GOLD_COIN.get();
        };
    }

    /** Die nächstgrößere Münze (Gold hat keine). */
    public @Nullable Coin larger() {
        return this == GOLD ? null : values()[this.ordinal() + 1];
    }

    /** Die nächstkleinere Münze (Bronze hat keine). */
    public @Nullable Coin smaller() {
        return this == BRONZE ? null : values()[this.ordinal() - 1];
    }

    /** Welche Münze ist dieser Gegenstand (oder keine)? */
    public static @Nullable Coin of(Item item) {
        for (Coin coin : values()) {
            if (coin.item() == item) {
                return coin;
            }
        }
        return null;
    }
}
