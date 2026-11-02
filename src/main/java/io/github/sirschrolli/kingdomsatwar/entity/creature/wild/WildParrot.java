package io.github.sirschrolli.kingdomsatwar.entity.creature.wild;

import com.mojang.logging.LogUtils;
import io.github.sirschrolli.kingdomsatwar.entity.ReplacesVanilla;
import io.github.sirschrolli.kingdomsatwar.entity.creature.farm.FarmAnimals;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.animal.parrot.Parrot;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.TagValueOutput;
import org.slf4j.Logger;

/**
 * Papagei – ersetzt den Vanilla-Papagei. Die fünf Vanilla-Farben werden fünf Arten: Hellroter Ara, Hyazinthara,
 * Amazone, Gelbbrustara und Graupapagei. Fliegen, Tanzen, Nachahmen von Monstern und das Sitzen auf der Schulter wie
 * bei Vanilla.
 */
public class WildParrot extends Parrot implements ReplacesVanilla {
    private static final Logger LOGGER = LogUtils.getLogger();

    /** Die Arten in der Reihenfolge der Vanilla-Varianten (rot-blau, blau, grün, gelb-blau, grau). */
    public enum Breed implements FarmAnimals.Breed { ARA, HYAZINTH, AMAZONE, GELBBRUST, GRAU }

    public WildParrot(EntityType<? extends WildParrot> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Parrot.createAttributes();
    }

    public static Breed breedOf(Parrot.Variant variant) {
        return FarmAnimals.byId(Breed.values(), variant.getId());
    }

    public Breed getBreed() {
        return breedOf(this.getVariant());
    }

    /**
     * Auf die Schulter: Der Spieler zeigt nur Vanilla-Papageien an (er merkt sich die Farbe aus "Variant"), also sitzt der
     * Papagei dort als Vanilla-Papagei – gezeichnet mit dem eigenen Modell, siehe WildParrotOnShoulderLayer. Steigt er
     * wieder ab, wird er sofort wieder zum eigenen Papagei.
     */
    @Override
    public boolean setEntityOnShoulder(ServerPlayer player) {
        if (player.setEntityOnShoulder(this.shoulderTag())) {
            this.discard();
            return true;
        }
        return false;
    }

    /** Der Papagei, wie er auf der Schulter gespeichert wird: als Vanilla-Papagei mit seiner Farbe. */
    public CompoundTag shoulderTag() {
        try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(this.problemPath(), LOGGER)) {
            TagValueOutput output = TagValueOutput.createWithContext(reporter, this.registryAccess());
            this.saveWithoutId(output);
            output.putString("id", EntityType.getKey(EntityTypes.PARROT).toString());
            return output.buildResult();
        }
    }

    @Override
    protected Component getTypeName() {
        return FarmAnimals.typeName(this.getType(), this.getBreed());
    }
}
