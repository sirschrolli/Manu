package io.github.sirschrolli.kingdomsatwar.entity.miniboss;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import org.jspecify.annotations.Nullable;

/**
 * Ein Mini-Boss (seit 1.15): der Wächter eines unterirdischen Bauwerks der Oberwelt – die Brutmutter in der Brutgrotte,
 * Morvan, der Gruftkönig, in der Gruft der Vergessenen, der Runenwächter in der Zwergenhalle und Grumbold, der
 * Trollvater, in der Trollhöhle. Er schläft an seinem Platz, bis jemand naht; dann erwacht er mit eigener Bossleiste und
 * kämpft mit drei, vier besonderen Angriffen, die sich alle ankündigen. Ab {@link MiniBossCore#ENRAGE_AT} seiner Kraft ist
 * er in Rage. Läuft man davon, kehrt er an seinen Platz zurück und schläft wieder ein. Er ist kein großer Boss (kein Zorn,
 * keine Gruppenstärke, nicht in {@code c:bosses}), zählt für die Härte aber wie einer.
 */
public interface MiniBoss {
    /** Der gemeinsame Teil (Schlaf, Bossleiste, Heimplatz, Diener, besondere Angriffe) – nur auf dem Server benutzt. */
    MiniBossCore miniBoss();

    /** Ist er wach (auch auf dem Client bekannt, für das Aussehen)? */
    boolean isAwake();

    void setAwake(boolean awake);

    /** In Rage (unter {@link MiniBossCore#ENRAGE_AT} seiner Kraft; auch auf dem Client bekannt)? */
    boolean isEnraged();

    void setEnraged(boolean enraged);

    /** Er erwacht (Gebrüll, Teilchen; {@code cause}: wer ihn geweckt hat). */
    default void onWake(ServerLevel level, @Nullable LivingEntity cause) {}

    /** Er gerät in Rage. */
    default void onEnrage(ServerLevel level) {}

    /** Niemand mehr da: er ist heimgekehrt und schläft wieder. */
    default void onSleep(ServerLevel level) {}

    /** Das Wesen selbst. */
    default Mob bossMob() {
        return (Mob) this;
    }
}
