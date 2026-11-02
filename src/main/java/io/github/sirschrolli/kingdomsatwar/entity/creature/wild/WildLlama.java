package io.github.sirschrolli.kingdomsatwar.entity.creature.wild;

import io.github.sirschrolli.kingdomsatwar.entity.ReplacesVanilla;
import io.github.sirschrolli.kingdomsatwar.entity.creature.farm.FarmAnimals;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.animal.equine.Llama;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/**
 * Lama – ersetzt das Vanilla-Lama. Die vier Vanilla-Farben (creme, weiß, braun, grau) bleiben, das Modell ist neu:
 * dichtes Vlies, Bananenohren, gespaltene Oberlippe. Spucken, Karawanen, Teppiche und Packtaschen wie bei Vanilla.
 */
public class WildLlama extends Llama implements ReplacesVanilla {
    /** Die Farben in der Reihenfolge der Vanilla-Varianten. */
    public enum Breed implements FarmAnimals.Breed { CREME, WEISS, BRAUN, GRAU }

    public WildLlama(EntityType<? extends WildLlama> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Llama.createAttributes();
    }

    public static Breed breedOf(Llama.Variant variant) {
        return FarmAnimals.byId(Breed.values(), variant.getId());
    }

    /** Vanilla setzt beim Züchten Stärke und Farbe des Fohlens – es muss nur vom eigenen Typ sein. */
    @Override
    protected @Nullable Llama makeNewLlama() {
        return ModEntities.LLAMA.get().create(this.level(), EntitySpawnReason.BREEDING);
    }

    @Override
    protected Component getTypeName() {
        return FarmAnimals.typeName(this.getType(), breedOf(this.getVariant()));
    }
}
