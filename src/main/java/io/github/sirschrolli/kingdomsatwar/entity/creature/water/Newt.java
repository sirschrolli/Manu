package io.github.sirschrolli.kingdomsatwar.entity.creature.water;

import io.github.sirschrolli.kingdomsatwar.entity.creature.wild.Courtship;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.level.Level;

/**
 * Molch (statt Axolotl): fünf eigene Arten an Stelle der fünf Vanilla-Farben – blassrosa Grottenolm, gefleckter
 * Wildmolch, Goldmolch, Eismolch und der seltene Blaumolch mit leuchtenden Kiemen. Jagt mit dem Spieler unter Wasser,
 * stellt sich tot und kommt in den Eimer wie das Axolotl.
 */
public class Newt extends Axolotl {
    public Newt(EntityType<? extends Newt> type, Level level) {
        super(type, level);
    }

    /** Das Vanilla-Gehirn sucht Partner nur unter Vanilla-Axolotln – die Werbung übernimmt {@link Courtship}. */
    private final Courtship<Newt> courtship = new Courtship<>(Newt.class, 70);

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        this.courtship.tick(level, this, newt -> !newt.isPanicking() && !newt.isPlayingDead());
    }
}
