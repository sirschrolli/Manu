package io.github.sirschrolli.kingdomsatwar.entity.boss.sky;

import net.minecraft.world.entity.Mob;

/**
 * Ein Boss, der über den Himmel wacht: Wer in seinem Kampf zu lange gleitet, den nimmt er ins Visier und holt ihn mit
 * einem Bannpfeil herunter (siehe {@link SkyWard}).
 */
public interface SkyWarden {
    /** Farbe, Funken und Stärke seiner Himmelswacht. */
    SkyTheme skyTheme();

    /** Kämpft er gerade? Nur dann wacht er – ein schlafender Boss lässt jeden über sich hinwegziehen. */
    default boolean guardsSky() {
        return this instanceof Mob mob && mob.getTarget() != null && mob.getTarget().isAlive();
    }
}
