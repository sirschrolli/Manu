package io.github.sirschrolli.kingdomsatwar.economy.citizen;

import io.github.sirschrolli.kingdomsatwar.economy.town.Town;
import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/** Die Tagesabläufe der Bürger: schlafen, arbeiten, Kunden bedienen und in der Siedlung bleiben. */
final class CitizenGoals {
    private CitizenGoals() {}

    /** Tageszeit (0 = Sonnenaufgang … 24000). */
    static long time(CitizenEntity citizen) {
        return Math.floorMod(citizen.level().getOverworldClockTime(), 24000L);
    }

    /** Arbeitszeit: vom Vormittag bis zum Abend. */
    static boolean workHours(CitizenEntity citizen) {
        long t = time(citizen);
        return t >= 500 && t < 11500;
    }

    /** Schlafenszeit (mit dem Heimweg am Abend). */
    static boolean bedtime(CitizenEntity citizen) {
        long t = time(citizen);
        return t >= 12000 && t < 23500;
    }

    // =====================================================================================
    // Schlafen
    // =====================================================================================

    /** Abends ins eigene Bett, morgens wieder auf. Wer (noch) kein Bett hat, bleibt nachts am Lager beim Banner. */
    static final class Sleep extends Goal {
        private final CitizenEntity citizen;
        private int repath;

        Sleep(CitizenEntity citizen) {
            this.citizen = citizen;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
        }

        @Override
        public boolean canUse() {
            return bedtime(this.citizen) && this.target() != null;
        }

        @Override
        public boolean canContinueToUse() {
            return bedtime(this.citizen) && this.target() != null;
        }

        /** Das Bett (Kopfende) oder – ohne Bett – ein Platz am Lagerfeuer beim Banner. */
        private @Nullable BlockPos target() {
            BlockPos bed = this.citizen.getBed();
            if (bed != null) {
                BlockState state = this.citizen.level().getBlockState(bed);
                if (state.getBlock() instanceof BedBlock) {
                    return bed;
                }
            }
            Town town = this.citizen.town();
            return town != null && town.hasBanner() ? town.banner() : null;
        }

        @Override
        public void start() {
            this.repath = 0;
            this.citizen.setWorking(false);
        }

        @Override
        public void tick() {
            BlockPos target = this.target();
            if (target == null || this.citizen.isSleeping()) {
                return;
            }
            boolean bed = this.citizen.level().getBlockState(target).getBlock() instanceof BedBlock;
            double dist = this.citizen.position().distanceToSqr(Vec3.atCenterOf(target));
            if (bed && dist < 2.6 * 2.6) {
                if (!this.citizen.level().getBlockState(target).getValue(BedBlock.OCCUPIED)) {
                    this.citizen.getNavigation().stop();
                    this.citizen.startSleeping(target);
                }
                return;
            }
            if (!bed && dist < 5 * 5) {
                this.citizen.getNavigation().stop();
                return;
            }
            if (--this.repath <= 0) {
                this.repath = 40;
                this.citizen.getNavigation().moveTo(target.getX() + 0.5, target.getY(), target.getZ() + 0.5, 0.75);
            }
        }

        @Override
        public void stop() {
            if (this.citizen.isSleeping()) {
                this.citizen.stopSleeping();
            }
        }
    }

    // =====================================================================================
    // Kunden bedienen
    // =====================================================================================

    /** Solange ein Spieler mit dem Bürger handelt, bleibt er stehen und schaut ihn an. */
    static final class Serve extends Goal {
        private final CitizenEntity citizen;

        Serve(CitizenEntity citizen) {
            this.citizen = citizen;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
        }

        @Override
        public boolean canUse() {
            return this.citizen.getCustomer() != null && !this.citizen.isSleeping();
        }

        @Override
        public void start() {
            this.citizen.getNavigation().stop();
        }

        @Override
        public void tick() {
            Player customer = this.citizen.getCustomer();
            if (customer != null) {
                this.citizen.getLookControl().setLookAt(customer, 30.0f, 30.0f);
            }
        }
    }

    // =====================================================================================
    // Arbeiten
    // =====================================================================================

    /** Tagsüber die Arbeit des Berufs (siehe {@link Jobs}). */
    static final class Work extends Goal {
        private final CitizenEntity citizen;
        private Jobs.@Nullable Job job;

        Work(CitizenEntity citizen) {
            this.citizen = citizen;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        private Jobs.Job job() {
            if (this.job == null || this.job.profession() != this.citizen.getProfession()) {
                this.job = Jobs.create(this.citizen);
            }
            return this.job;
        }

        @Override
        public boolean canUse() {
            return workHours(this.citizen) && !this.citizen.isSleeping() && this.citizen.town() != null && this.job().canStart();
        }

        @Override
        public boolean canContinueToUse() {
            return workHours(this.citizen) && !this.citizen.isSleeping() && this.job().running();
        }

        @Override
        public void start() {
            this.job().start();
        }

        @Override
        public void tick() {
            this.job().tick();
        }

        @Override
        public void stop() {
            this.job().stop();
            this.citizen.setWorking(false);
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }
    }

    // =====================================================================================
    // In der Siedlung bleiben
    // =====================================================================================

    /** Wer sich zu weit vom Banner entfernt hat, geht zurück in die Siedlung. */
    static final class StayInTown extends Goal {
        private final CitizenEntity citizen;

        StayInTown(CitizenEntity citizen) {
            this.citizen = citizen;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            Town town = this.citizen.town();
            if (town == null || !town.hasBanner() || this.citizen.tickCount % 40 != 0) {
                return false;
            }
            double r = town.radius() * 0.9;
            return this.citizen.distanceToSqr(Vec3.atCenterOf(town.banner())) > r * r;
        }

        @Override
        public void start() {
            Town town = this.citizen.town();
            if (town != null) {
                BlockPos banner = town.banner();
                this.citizen.getNavigation().moveTo(banner.getX() + 0.5, banner.getY(), banner.getZ() + 0.5, 0.8);
            }
        }

        @Override
        public boolean canContinueToUse() {
            return !this.citizen.getNavigation().isDone();
        }
    }
}
