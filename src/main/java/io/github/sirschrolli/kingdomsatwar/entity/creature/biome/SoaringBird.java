package io.github.sirschrolli.kingdomsatwar.entity.creature.biome;

import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Ein Vogel, der am Boden geht und in der Luft segelt (0.85: Sturmmöwe, Greif). In der Luft lenkt er seine Geschwindigkeit
 * sanft auf ein Ziel zu (keine Wegsuche, keine Schwerkraft) und dreht dabei weite Kreise um einen Mittelpunkt in der Höhe;
 * nach einer Weile sucht er sich einen Landeplatz, sinkt darauf zu und setzt auf. Am Boden geht er umher, bis er wieder
 * auffliegt – aus Laune, oder weil ihm jemand zu nahe kommt. Fallschaden nimmt er nie.
 */
public abstract class SoaringBird extends Animal {
    private static final EntityDataAccessor<Boolean> DATA_FLYING = SynchedEntityData.defineId(SoaringBird.class, EntityDataSerializers.BOOLEAN);

    /** Mittelpunkt der Kreise in der Luft. */
    protected @Nullable Vec3 anchor;
    protected int airTicks;
    protected int groundTicks;
    /** So lange (Takte) bleibt er diesmal in der Luft bzw. am Boden. */
    protected int airBudget;
    protected int groundBudget;
    /** Eine eigene Aufgabe (Stehlen, Jagen) lenkt gerade – Kreisen und Landen ruhen. */
    protected boolean busy;

    protected SoaringBird(EntityType<? extends SoaringBird> type, Level level) {
        super(type, level);
        this.moveControl = new BirdMoveControl(this);
        this.airBudget = this.newAirBudget();
        this.groundBudget = this.newGroundBudget();
    }

    // ------------------------------------------------------------------ Eigenschaften der Art

    /** Reisegeschwindigkeit in der Luft (Blöcke je Takt). */
    protected abstract double cruiseSpeed();

    /** Halbmesser der Kreise in der Luft. */
    protected abstract double soarRadius();

    /** Höhe der Kreise über dem Boden. */
    protected abstract double soarHeight();

    protected abstract int newAirBudget();

    protected abstract int newGroundBudget();

    /** Ein guter Platz zum Landen (oben frei, Boden fest). */
    protected abstract boolean isGoodLandingSpot(BlockPos pos, BlockState ground);

    /** Wie weit er nach einem Landeplatz sucht. */
    protected int landingSearchRadius() {
        return 16;
    }

    // ------------------------------------------------------------------ Zustand

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_FLYING, false);
    }

    public boolean isFlying() {
        return this.entityData.get(DATA_FLYING);
    }

    public void setFlying(boolean flying) {
        this.entityData.set(DATA_FLYING, flying);
        this.setNoGravity(flying);
        if (flying) {
            this.airTicks = 0;
            this.airBudget = this.newAirBudget();
        } else {
            this.groundTicks = 0;
            this.groundBudget = this.newGroundBudget();
        }
    }

    /** Auffliegen: ein kräftiger Schlag nach oben. */
    public void takeOff() {
        this.getNavigation().stop();
        this.setFlying(true);
        Vec3 look = Vec3.directionFromRotation(0.0f, this.getYRot());
        this.setDeltaMovement(look.x * 0.15, 0.42, look.z * 0.15);
        if (this.anchor == null || this.anchor.distanceToSqr(this.position()) > 64.0 * 64.0) {
            this.anchor = this.position();
        }
        this.onTakeOff();
    }

    protected void onTakeOff() {}

    /** Aufsetzen. */
    public void land() {
        this.setFlying(false);
        this.setDeltaMovement(this.getDeltaMovement().multiply(0.3, 0.0, 0.3));
        this.getMoveControl().setWantedPosition(this.getX(), this.getY(), this.getZ(), 0.0);
    }

    public void setAnchor(@Nullable Vec3 anchor) {
        this.anchor = anchor;
    }

    /** Fliegt auf einen Punkt zu ({@code factor} mal die Reisegeschwindigkeit). */
    public void flyTo(double x, double y, double z, double factor) {
        this.getMoveControl().setWantedPosition(x, y, z, factor);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putBoolean("Flying", this.isFlying());
        if (this.anchor != null) {
            output.putDouble("AnchorX", this.anchor.x);
            output.putDouble("AnchorY", this.anchor.y);
            output.putDouble("AnchorZ", this.anchor.z);
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setFlying(input.getBooleanOr("Flying", false));
        double ax = input.getDoubleOr("AnchorX", Double.NaN);
        if (!Double.isNaN(ax)) {
            this.anchor = new Vec3(ax, input.getDoubleOr("AnchorY", this.getY()), input.getDoubleOr("AnchorZ", this.getZ()));
        }
    }

    // ------------------------------------------------------------------ Bewegung

    @Override
    public void travel(Vec3 input) {
        if (this.isFlying() && !this.isInWater()) {
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().scale(0.94));
            this.calculateEntityAnimation(false);
        } else {
            super.travel(input);
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide()) {
            return;
        }
        if (this.isFlying()) {
            this.airTicks++;
            // gegen eine Wand: hinauf und weiter
            if (this.horizontalCollision) {
                this.setDeltaMovement(this.getDeltaMovement().add(0.0, 0.18, 0.0));
            }
            // im Wasser gelandet (Möwen schwimmen) oder sehr lange am Boden hängen geblieben
            if (this.isInWater() && this.canRestOnWater()) {
                this.land();
            }
        } else {
            this.groundTicks++;
        }
    }

    /** Möwen dürfen auf dem Wasser landen und schwimmen. */
    protected boolean canRestOnWater() {
        return false;
    }

    /** Höhe des Bodens unter einer Stelle (oberster fester Block + 1). */
    protected int groundY(double x, double z) {
        return this.level().getHeight(Heightmap.Types.MOTION_BLOCKING, Mth.floor(x), Mth.floor(z));
    }

    /** Sucht einen Landeplatz um den Vogel herum (null: keiner). */
    protected @Nullable BlockPos findLandingSpot() {
        int r = this.landingSearchRadius();
        BlockPos best = null;
        double bestScore = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < 24; i++) {
            int x = Mth.floor(this.getX()) + this.random.nextInt(r * 2 + 1) - r;
            int z = Mth.floor(this.getZ()) + this.random.nextInt(r * 2 + 1) - r;
            int y = this.level().getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            BlockPos pos = new BlockPos(x, y, z);
            BlockState ground = this.level().getBlockState(pos.below());
            if (!this.level().getBlockState(pos).isAir() && !this.level().getBlockState(pos).canBeReplaced()) {
                continue;
            }
            if (!this.isGoodLandingSpot(pos, ground)) {
                continue;
            }
            double score = this.landingScore(pos, ground);
            if (score > bestScore) {
                bestScore = score;
                best = pos;
            }
        }
        return best;
    }

    /** Wie gern er hier landet (höher ist besser). */
    protected double landingScore(BlockPos pos, BlockState ground) {
        return -pos.distSqr(this.blockPosition()) * 0.01 + this.random.nextDouble();
    }

    // ------------------------------------------------------------------ Leben

    @Override
    public boolean causeFallDamage(double fallDistance, float damageModifier, DamageSource damageSource) {
        return false;
    }

    @Override
    protected void checkFallDamage(double ya, boolean onGround, BlockState onState, BlockPos pos) {
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return false;
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
        return null;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        boolean hurt = super.hurtServer(level, source, amount);
        if (hurt && !this.isFlying() && this.onGround() && this.shouldFleeUpward()) {
            this.takeOff();
        }
        return hurt;
    }

    /** Fliegt auf, wenn man ihn trifft (Möwe ja, Greif nein). */
    protected boolean shouldFleeUpward() {
        return true;
    }

    // =====================================================================================
    // Steuerung in der Luft
    // =====================================================================================

    /** In der Luft: lenkt die Geschwindigkeit sanft auf den Zielpunkt; am Boden wie gewohnt. */
    static final class BirdMoveControl extends MoveControl<SoaringBird> {
        BirdMoveControl(SoaringBird bird) {
            super(bird);
        }

        @Override
        public void tick() {
            SoaringBird bird = this.mob;
            if (!bird.isFlying() || bird.isInWater()) {
                super.tick();
                return;
            }
            if (this.operation != Operation.MOVE_TO) {
                // ohne Ziel: weiter gleiten, langsam sinken
                bird.setDeltaMovement(bird.getDeltaMovement().add(0.0, -0.01, 0.0));
                this.face(bird.getDeltaMovement());
                return;
            }
            Vec3 to = new Vec3(this.wantedX - bird.getX(), this.wantedY - bird.getY(), this.wantedZ - bird.getZ());
            double dist = to.length();
            if (dist < 0.35) {
                this.operation = Operation.WAIT;
                bird.setDeltaMovement(bird.getDeltaMovement().scale(0.5));
                return;
            }
            double speed = bird.cruiseSpeed() * this.speedModifier;
            // kurz vor dem Ziel langsamer
            speed *= Mth.clamp(dist / 3.0, 0.25, 1.0);
            Vec3 wanted = to.scale(speed / dist);
            Vec3 v = bird.getDeltaMovement();
            bird.setDeltaMovement(v.add(wanted.subtract(v).scale(0.14)));
            this.face(bird.getDeltaMovement());
        }

        private void face(Vec3 v) {
            SoaringBird bird = this.mob;
            if (v.horizontalDistanceSqr() > 1.0e-4) {
                float yaw = (float) (Mth.atan2(v.z, v.x) * Mth.RAD_TO_DEG) - 90.0f;
                bird.setYRot(this.rotlerp(bird.getYRot(), yaw, 12.0f));
                bird.yBodyRot = bird.getYRot();
                bird.setYHeadRot(bird.getYRot());
            }
            float pitch = (float) (-Mth.atan2(v.y, Math.sqrt(v.horizontalDistanceSqr())) * Mth.RAD_TO_DEG);
            bird.setXRot(Mth.clamp(pitch, -40.0f, 40.0f));
        }
    }

    // =====================================================================================
    // Ziele
    // =====================================================================================

    /** Kreist in der Luft um den Mittelpunkt (weite Bögen in der Höhe), bis es Zeit zum Landen ist. */
    protected final class SoarGoal extends Goal {
        private double angle;
        private double radius;
        private double height;
        private int direction = 1;

        public SoarGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            SoaringBird bird = SoaringBird.this;
            return bird.isFlying() && !bird.busy && bird.airTicks < bird.airBudget;
        }

        @Override
        public void start() {
            SoaringBird bird = SoaringBird.this;
            if (bird.anchor == null) {
                bird.anchor = bird.position();
            }
            this.angle = Math.atan2(bird.getZ() - bird.anchor.z, bird.getX() - bird.anchor.x);
            this.radius = bird.soarRadius() * (0.7 + bird.random.nextDouble() * 0.6);
            this.height = bird.soarHeight() * (0.8 + bird.random.nextDouble() * 0.5);
            this.direction = bird.random.nextBoolean() ? 1 : -1;
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            SoaringBird bird = SoaringBird.this;
            Vec3 c = bird.anchor;
            if (c == null) {
                return;
            }
            // der Punkt auf dem Kreis ein Stück voraus
            double speed = bird.cruiseSpeed();
            this.angle += this.direction * speed / Math.max(4.0, this.radius) * 1.1;
            double x = c.x + Math.cos(this.angle) * this.radius;
            double z = c.z + Math.sin(this.angle) * this.radius;
            double ground = Math.max(bird.groundY(x, z), bird.groundY(bird.getX(), bird.getZ()));
            double y = Math.max(ground + this.height * 0.6, c.y + this.height) + Math.sin(this.angle * 2.0) * 1.5;
            bird.flyTo(x, y, z, 1.0);
            // ab und zu einen neuen Mittelpunkt nahebei, damit er nicht ewig denselben Kreis zieht
            if (bird.random.nextInt(600) == 0) {
                bird.anchor = c.add(bird.random.nextGaussian() * 12.0, 0.0, bird.random.nextGaussian() * 12.0);
                this.radius = bird.soarRadius() * (0.7 + bird.random.nextDouble() * 0.6);
            }
        }
    }

    /** Sucht einen Landeplatz, gleitet hin und setzt auf. */
    protected final class LandGoal extends Goal {
        private @Nullable BlockPos spot;
        private int ticks;

        public LandGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            SoaringBird bird = SoaringBird.this;
            if (!bird.isFlying() || bird.busy || bird.airTicks < bird.airBudget) {
                return false;
            }
            this.spot = bird.findLandingSpot();
            if (this.spot == null) {
                bird.airBudget += 100;                                      // keiner gefunden: noch eine Runde
                return false;
            }
            return true;
        }

        @Override
        public boolean canContinueToUse() {
            return SoaringBird.this.isFlying() && this.spot != null && this.ticks < 600 && !SoaringBird.this.busy;
        }

        @Override
        public void start() {
            this.ticks = 0;
        }

        @Override
        public void stop() {
            this.spot = null;
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            SoaringBird bird = SoaringBird.this;
            BlockPos s = this.spot;
            if (s == null) {
                return;
            }
            this.ticks++;
            double dx = s.getX() + 0.5 - bird.getX();
            double dz = s.getZ() + 0.5 - bird.getZ();
            double flat = Math.sqrt(dx * dx + dz * dz);
            if (flat > 3.0) {
                // im Gleitflug hin, langsam tiefer
                bird.flyTo(s.getX() + 0.5, s.getY() + Math.min(8.0, 2.0 + flat * 0.4), s.getZ() + 0.5, 0.9);
            } else {
                bird.flyTo(s.getX() + 0.5, s.getY() + 0.05, s.getZ() + 0.5, 0.45);
                if (bird.onGround() || bird.getY() - s.getY() < 0.3 && bird.verticalCollisionBelow) {
                    bird.land();
                }
            }
            if (this.ticks > 560) {
                bird.land();                                                // zur Not einfach fallen lassen
            }
        }
    }

    /** Am Boden: nach einer Weile wieder auffliegen. */
    protected final class TakeOffGoal extends Goal {
        @Override
        public boolean canUse() {
            SoaringBird bird = SoaringBird.this;
            return !bird.isFlying() && !bird.busy && !bird.isBaby() && bird.groundTicks > bird.groundBudget
                    && (bird.onGround() || bird.isInWater());
        }

        @Override
        public void start() {
            SoaringBird.this.takeOff();
        }

        @Override
        public boolean canContinueToUse() {
            return false;
        }
    }

    /** Ein Ziel, das nur am Boden läuft (Umhergehen, Umschauen). */
    protected final class OnGround extends Goal {
        private final Goal goal;

        public OnGround(Goal goal) {
            this.goal = goal;
            this.setFlags(goal.getFlags());
        }

        @Override
        public boolean canUse() {
            return !SoaringBird.this.isFlying() && !SoaringBird.this.busy && this.goal.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            return !SoaringBird.this.isFlying() && !SoaringBird.this.busy && this.goal.canContinueToUse();
        }

        @Override
        public void start() {
            this.goal.start();
        }

        @Override
        public void stop() {
            this.goal.stop();
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return this.goal.requiresUpdateEveryTick();
        }

        @Override
        public void tick() {
            this.goal.tick();
        }
    }
}
