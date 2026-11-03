package io.github.sirschrolli.kingdomsatwar.entity.creature.wild;

import io.github.sirschrolli.kingdomsatwar.entity.ReplacesVanilla;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.equine.Llama;
import net.minecraft.world.entity.animal.equine.TraderLlama;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/**
 * Händlerlama – ersetzt das Lama des fahrenden Händlers: gleiches Modell wie das eigene Lama, mit der bunten
 * Händlerdecke. Bleibt beim Händler an der Leine, verteidigt ihn und verschwindet mit ihm.
 */
public class WildTraderLlama extends TraderLlama implements ReplacesVanilla {
    public WildTraderLlama(EntityType<? extends WildTraderLlama> type, Level level) {
        super(type, level);
    }

    @Override
    protected @Nullable Llama makeNewLlama() {
        WildTraderLlama baby = ModEntities.TRADER_LLAMA.get().create(this.level(), EntitySpawnReason.BREEDING);
        if (baby != null) {
            baby.setPersistenceRequired();
        }
        return baby;
    }
}
