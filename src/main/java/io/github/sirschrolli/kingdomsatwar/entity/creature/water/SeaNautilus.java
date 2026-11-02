package io.github.sirschrolli.kingdomsatwar.entity.creature.water;

import io.github.sirschrolli.kingdomsatwar.entity.creature.wild.Courtship;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.nautilus.Nautilus;
import net.minecraft.world.level.Level;

/**
 * Nautilus (statt Vanilla-Nautilus): cremefarbenes Gehäuse mit rostroten Tigerstreifen und perlmuttner Mündung,
 * zarte rosagraue Fangarme. Lässt sich zähmen, satteln, panzern und unter Wasser reiten.
 */
public class SeaNautilus extends Nautilus {
    public SeaNautilus(EntityType<? extends SeaNautilus> type, Level level) {
        super(type, level);
    }

    /** Das Vanilla-Gehirn sucht Partner nur unter Vanilla-Nautilussen – die Werbung übernimmt {@link Courtship}. */
    private final Courtship<SeaNautilus> courtship = new Courtship<>(SeaNautilus.class, 70);

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        this.courtship.tick(level, this, nautilus -> !nautilus.isPanicking());
    }
}
