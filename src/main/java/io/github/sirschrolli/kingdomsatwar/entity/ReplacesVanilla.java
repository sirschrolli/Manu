package io.github.sirschrolli.kingdomsatwar.entity;

import io.github.sirschrolli.kingdomsatwar.entity.creature.farm.FarmAnimalConversion;
import net.minecraft.core.TypedInstance;
import net.minecraft.world.entity.EntityType;

/**
 * Für die eigenen Wesen, die ein Vanilla-Wesen ersetzen und dessen Klasse erweitern: Fragt Vanilla-Code nach dem Typ
 * ({@code entity.is(EntityTypes.HOGLIN)}), gilt das eigene Wesen als das ersetzte. So jagen Borstlinge die Glutkeiler
 * und weichen den Moderwesen aus, Eisengolems lassen Kriecher in Ruhe, Ziegen rammen keine Ziegen, Wölfe jagen Schafe
 * und Hasen, Tiere mit Vanilla-Gehirn finden ihre Partner, Lamas ziehen in Karawanen, und Karottenrute und Wirrpilzrute
 * treiben Schwein und Lavastelzer an – alles wie bei den Vanilla-Wesen.
 */
public interface ReplacesVanilla extends TypedInstance<EntityType<?>> {
    @Override
    default boolean is(EntityType<?> type) {
        EntityType<?> own = this.typeHolder().value();
        return own == type || type != null && FarmAnimalConversion.vanillaOf(own) == type;
    }
}
