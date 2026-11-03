package io.github.sirschrolli.kingdomsatwar.entity.creature.golem;

import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierEntity;
import io.github.sirschrolli.kingdomsatwar.entity.villager.VillagerEntity;
import java.util.EnumSet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.Nullable;

/** Ziele der eigenen Golems. */
final class GolemGoals {
    private GolemGoals() {}

    /**
     * Wie Vanillas DefendVillageTargetGoal, aber für die Dorfbewohner der Mod: wer in den letzten Sekunden einen Bewohner
     * in der Nähe angegriffen hat, wird zum Ziel. Ein von Spielern gebauter Wächter greift keine Spieler an; Spieler im
     * Kreativ- oder Zuschauermodus bleiben ohnehin verschont.
     */
    static final class DefendVillagers extends TargetGoal {
        private final IronGolem golem;
        private @Nullable LivingEntity attacker;

        DefendVillagers(IronGolem golem) {
            super(golem, false, true);
            this.golem = golem;
            this.setFlags(EnumSet.of(Flag.TARGET));
        }

        @Override
        public boolean canUse() {
            AABB area = this.golem.getBoundingBox().inflate(16.0, 8.0, 16.0);
            for (VillagerEntity villager : this.golem.level().getEntitiesOfClass(VillagerEntity.class, area)) {
                LivingEntity culprit = villager.getLastHurtByMob();
                if (culprit == null || !culprit.isAlive() || villager.tickCount - villager.getLastHurtByMobTimestamp() > 100) {
                    continue;
                }
                if (culprit == this.golem || culprit instanceof VillagerEntity || culprit instanceof IronGolem || culprit instanceof SoldierEntity) {
                    continue;
                }
                if (culprit instanceof Player player && (this.golem.isPlayerCreated() || player.isCreative() || player.isSpectator())) {
                    continue;
                }
                this.attacker = culprit;
                return true;
            }
            return false;
        }

        @Override
        public void start() {
            this.golem.setTarget(this.attacker);
            super.start();
        }
    }
}
