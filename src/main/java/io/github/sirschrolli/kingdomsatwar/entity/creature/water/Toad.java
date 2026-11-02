package io.github.sirschrolli.kingdomsatwar.entity.creature.water;

import io.github.sirschrolli.kingdomsatwar.entity.creature.wild.Courtship;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.frog.Frog;
import net.minecraft.world.level.Level;

/**
 * Frosch (statt Vanilla-Frosch): je nach Klima ein grüner Teichfrosch, eine Feuerkröte mit rotem Bauch oder ein
 * rostbrauner Grasfrosch. Springt, quakt, fängt mit der Zunge Schleime und Magmawürfel und lässt Froschlichter
 * entstehen wie der Vanilla-Frosch.
 */
public class Toad extends Frog {
    public Toad(EntityType<? extends net.minecraft.world.entity.animal.Animal> type, Level level) {
        super(type, level);
    }

    /** Das Vanilla-Gehirn sucht Partner nur unter Vanilla-Fröschen – die Werbung übernimmt {@link Courtship} (danach laicht das Weibchen wie gewohnt). */
    private final Courtship<Toad> courtship = new Courtship<>(Toad.class, 70);

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        this.courtship.tick(level, this, frog -> !frog.isPanicking());
    }
}
