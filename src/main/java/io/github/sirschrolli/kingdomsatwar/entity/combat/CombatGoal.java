package io.github.sirschrolli.kingdomsatwar.entity.combat;

import java.util.EnumSet;
import net.minecraft.world.entity.ai.goal.DoorInteractGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.MoveThroughVillageGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsTargetGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.RangedBowAttackGoal;
import net.minecraft.world.entity.ai.goal.RangedCrossbowAttackGoal;
import net.minecraft.world.entity.ai.goal.RestrictSunGoal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;

/**
 * Hält Bewegung, Blick und Sprung, solange ein besonderer Angriff läuft – dann ruht das gewohnte Laufen und Zuschlagen.
 * Es greift nur ein, wenn das Monster gerade nichts Eigenes tut (eigene Angriffe der Monster, Flucht vor Sonne oder
 * Wölfen, Zauber und Ähnliches werden nie unterbrochen), und lässt sich selbst nicht unterbrechen.
 */
final class CombatGoal extends Goal {
    private final Fight fight;

    CombatGoal(Fight fight) {
        this.fight = fight;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK, Goal.Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        return this.fight.phase == Fight.Phase.WAIT && this.fight.controlled() && this.othersAllow();
    }

    @Override
    public boolean canContinueToUse() {
        return this.fight.wantsLock();
    }

    @Override
    public boolean isInterruptable() {
        return false;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void start() {
        this.fight.locked = true;
        this.fight.mob.getNavigation().stop();
    }

    @Override
    public void tick() {
        if (this.fight.kind != null && this.fight.kind.root()) {
            this.fight.mob.getNavigation().stop();
        }
    }

    @Override
    public void stop() {
        this.fight.locked = false;
    }

    /** Läuft gerade nichts, das nicht unterbrochen werden darf? */
    private boolean othersAllow() {
        for (WrappedGoal wrapped : this.fight.mob.goalSelector.getAvailableGoals()) {
            if (!wrapped.isRunning() || wrapped.getGoal() == this) {
                continue;
            }
            EnumSet<Goal.Flag> flags = wrapped.getFlags();
            if (!flags.contains(Goal.Flag.MOVE) && !flags.contains(Goal.Flag.LOOK) && !flags.contains(Goal.Flag.JUMP)) {
                continue;
            }
            if (!interruptible(wrapped.getGoal())) {
                return false;
            }
        }
        return true;
    }

    /** Das gewohnte Laufen, Umsehen und Zuschlagen darf ruhen. */
    static boolean interruptible(Goal goal) {
        if (goal instanceof MeleeAttackGoal || goal instanceof RangedBowAttackGoal<?> || goal instanceof RangedCrossbowAttackGoal<?>
                || goal instanceof RangedAttackGoal || goal instanceof LeapAtTargetGoal || goal instanceof MoveTowardsTargetGoal
                || goal instanceof RandomStrollGoal || goal instanceof LookAtPlayerGoal || goal instanceof RandomLookAroundGoal
                || goal instanceof MoveThroughVillageGoal || goal instanceof DoorInteractGoal || goal instanceof RestrictSunGoal) {
            return true;
        }
        // eigene Angriffs- und Laufziele einiger Vanilla-Monster (innere Klassen)
        String name = goal.getClass().getName();
        return name.endsWith("HoldGroundAttackGoal") || name.endsWith("RaiderMoveThroughVillageGoal")
                || name.endsWith("CubeMobAttackGoal") || name.endsWith("CubeMobRandomDirectionGoal")
                || name.endsWith("CubeMobKeepOnJumpingGoal");
    }
}
