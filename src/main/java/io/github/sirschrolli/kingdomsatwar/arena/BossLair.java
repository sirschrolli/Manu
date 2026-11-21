package io.github.sirschrolli.kingdomsatwar.arena;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import org.jspecify.annotations.Nullable;

/**
 * Ein Hort, dessen Boss seine Gegner in seine Kampfdimension zieht (der Zauberzylinder, der Glutamboss, der Sternstein, der
 * Fehlerkern, das Thronsiegel und das Siegel des Horts). Das Original bleibt in der Welt und wartet, solange der Kampf
 * drüben läuft; in der Kampfdimension steht ein Abbild – dieselbe Blockentität, als Abbild markiert –, an das der Boss
 * dort gebunden ist. Ein Abbild zieht niemanden fort und hat keine Folgen für die Welt (etwa den Riss hinter dem Thron).
 */
public interface BossLair {
    /** Welche Kampfdimension zu diesem Hort gehört. */
    BossArenaKind arenaKind();

    /** Die Mitte des Kampfs: wer nahe genug ist, wird mitgezogen. */
    BlockPos arenaFocus();

    /** Wo die Beute nach dem Sieg landet (frei, auf festem Boden). */
    BlockPos arenaLootSpot();

    /** Ist der Boss dieses Horts besiegt? */
    boolean arenaDefeated();

    /** Der Boss dieses Horts, wenn er lebt und in der Nähe ist. */
    @Nullable LivingEntity arenaBoss(ServerLevel level);

    /** Lässt den Boss sofort erwachen (das Abbild in der Kampfdimension, gleich nach dem Hineinziehen). */
    @Nullable LivingEntity arenaAwaken(ServerLevel level);

    /** Original: Der Kampf läuft drüben – der Hort wartet (ein Boss, der hier noch kniete oder schlief, verschwindet). */
    void arenaAway(ServerLevel level);

    /** Original: Der Boss ist in seiner Kampfdimension gefallen. */
    void arenaVictory(ServerLevel level);

    /** Original: Der Kampf ist ohne Sieg zu Ende – der Hort ruht wieder, und der nächste Boss erwacht unversehrt. */
    void arenaReset(ServerLevel level);

    /** Macht diese Blockentität zum Abbild in der Kampfdimension (ruhend, ohne Boss). */
    void arenaPrepareReplica(ServerLevel level);

    /** Ist das ein Abbild in einer Kampfdimension? */
    boolean isArenaReplica();

    /** Ein Boss, der an einen Hort gebunden ist (für das Zurücksetzen nach dem Tod seiner Gegner). */
    interface Bound {
        /** Wo seine Blockentität steht (oder {@code null}, wenn er frei ist). */
        @Nullable BlockPos lairPos();
    }
}
