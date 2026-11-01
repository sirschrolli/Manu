package io.github.sirschrolli.kingdomsatwar.entity.creature.wild;

import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.animal.panda.Panda;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/**
 * Panda – ersetzt den Vanilla-Panda. Gene, Wesenszüge (faul, besorgt, verspielt, schwach, aggressiv, braun), Bambus,
 * Niesen, Rollen und Rückenlage wie bei Vanilla; das eigene Fellbild zeigt das Gemüt im Gesicht.
 */
public class WildPanda extends Panda {
    public WildPanda(EntityType<? extends WildPanda> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Panda.createAttributes();
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
        WildPanda cub = ModEntities.PANDA.get().create(level, EntitySpawnReason.BREEDING);
        if (cub != null) {
            if (partner instanceof Panda other) {
                cub.setGeneFromParents(this, other);
            }
            cub.setAttributes();
        }
        return cub;
    }
}
