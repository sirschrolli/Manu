package io.github.sirschrolli.kingdomsatwar.fishing;

import io.github.sirschrolli.kingdomsatwar.registry.FishingItems;
import net.minecraft.world.item.ItemStack;

/**
 * Köder: Wer beim Angeln einen in der anderen Hand hält, lockt bestimmte Fische (bei jedem Biss wird einer verbraucht).
 * <ul>
 *     <li><b>Köderwurm:</b> häufige Fische der Flüsse und Seen beißen eher – und überhaupt alles etwas schneller;</li>
 *     <li><b>Glühköder:</b> lockt, was im Dunkeln lebt: Nachtfische, die Tiefsee, Höhlen- und Lavafische.</li>
 * </ul>
 */
public enum Bait {
    NONE, WORM, GLOW;

    public static Bait of(ItemStack stack) {
        if (stack.is(FishingItems.BAIT_WORM.get())) {
            return WORM;
        }
        return stack.is(FishingItems.BAIT_GLOW.get()) ? GLOW : NONE;
    }

    /** Faktor auf die Wahrscheinlichkeit, dass diese Art beißt. */
    public float factor(FishSpecies species) {
        return switch (this) {
            case NONE -> 1.0f;
            case WORM -> species.habitats().contains(Habitat.FRESH) && species.rarity().ordinal() <= FishSpecies.Rarity.UNCOMMON.ordinal() ? 1.6f : 1.0f;
            case GLOW -> species.time() == FishSpecies.Time.NIGHT || species.habitats().contains(Habitat.DEEP) || species.habitats().contains(Habitat.CAVE)
                    || species.habitats().contains(Habitat.SCULK) || species.ofLava() ? 2.2f : 1.0f;
        };
    }

    /** Um so viel schneller kommt der nächste Biss (Anteil der Wartezeit, der wegfällt). */
    public float haste() {
        return this == NONE ? 0.0f : 0.25f;
    }
}
