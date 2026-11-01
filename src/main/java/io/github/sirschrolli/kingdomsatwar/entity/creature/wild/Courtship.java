package io.github.sirschrolli.kingdomsatwar.entity.creature.wild;

import java.util.Comparator;
import java.util.function.Predicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.animal.Animal;
import org.jspecify.annotations.Nullable;

/**
 * Werbung für Tiere mit Vanilla-Gehirn (Kamel, Gürteltier): Deren Paarungs-Verhalten sucht Partner nur unter dem
 * Vanilla-Typ. Hier sucht sich ein verliebtes Tier einen verliebten Artgenossen in der Nähe, beide gehen aufeinander
 * zu, schauen sich an und bekommen nach einer Weile ein Junges – wie bei Vanilla.
 */
public final class Courtship<T extends Animal> {
    private final Class<T> type;
    private final int ticks;
    private @Nullable T partner;
    private int progress;

    public Courtship(Class<T> type, int ticks) {
        this.type = type;
        this.ticks = ticks;
    }

    /** Jeden Server-Tick aufrufen. {@code ready} sagt, ob ein Tier gerade werben kann (nicht eingerollt, nicht in Panik …). */
    public void tick(ServerLevel level, T self, Predicate<T> ready) {
        if (!self.isInLove() || self.isBaby() || !ready.test(self)) {
            this.reset();
            return;
        }
        T current = this.partner;
        if (current == null || !current.isAlive() || !self.canMate(current) || self.distanceToSqr(current) > 16.0 * 16.0) {
            current = level.getEntitiesOfClass(this.type, self.getBoundingBox().inflate(8.0),
                            other -> other != self && self.canMate(other) && ready.test(other))
                    .stream().min(Comparator.comparingDouble(self::distanceToSqr)).orElse(null);
            this.partner = current;
            this.progress = 0;
            if (current == null) {
                return;
            }
        }
        BehaviorUtils.lockGazeAndWalkToEachOther(self, current, 1.0f, 2);
        if (self.closerThan(current, 3.0) && ++this.progress >= this.ticks && self.getId() < current.getId()) {
            self.spawnChildFromBreeding(level, current);
            this.reset();
        }
    }

    private void reset() {
        this.partner = null;
        this.progress = 0;
    }
}
