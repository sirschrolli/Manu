package io.github.sirschrolli.kingdomsatwar.entity.creature.biome;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ambient.AmbientCreature;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Schmetterlinge der Königsauen (0.86): sechs Arten (Tagpfauenauge, Zitronenfalter, Schwalbenschwanz, Bläuling,
 * Admiral, Monarch). Sie gaukeln in unruhigen Bögen über die Wiesen, setzen sich auf Blumen, öffnen und schließen dort
 * langsam die Flügel und fliegen weiter. Nachts und bei Regen sitzen sie mit zusammengeklappten Flügeln auf einer Blume
 * oder im Gras. Kommt man ihnen zu nahe, flattern sie auf. Sie sind harmlos und lautlos.
 */
public class ButterflyEntity extends AmbientCreature {
    public static final int VARIANTS = 6;
    private static final int[] WEIGHTS = {3, 3, 1, 3, 2, 1};

    private static final EntityDataAccessor<Integer> DATA_VARIANT = SynchedEntityData.defineId(ButterflyEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_PERCHED = SynchedEntityData.defineId(ButterflyEntity.class, EntityDataSerializers.BOOLEAN);

    private @Nullable Vec3 target;
    private @Nullable BlockPos flower;
    private int perchTicks;
    private int flightTicks;

    public ButterflyEntity(EntityType<? extends ButterflyEntity> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 2.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_VARIANT, 0);
        builder.define(DATA_PERCHED, false);
    }

    public int getVariant() {
        return this.entityData.get(DATA_VARIANT);
    }

    public void setVariant(int variant) {
        this.entityData.set(DATA_VARIANT, Mth.clamp(variant, 0, VARIANTS - 1));
    }

    /** Sitzt (auf einer Blume oder im Gras). */
    public boolean isPerched() {
        return this.entityData.get(DATA_PERCHED);
    }

    public void setPerched(boolean perched) {
        this.entityData.set(DATA_PERCHED, perched);
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason,
            @Nullable SpawnGroupData groupData) {
        int total = 0;
        for (int w : WEIGHTS) {
            total += w;
        }
        int pick = this.random.nextInt(total);
        int variant = 0;
        while (pick >= WEIGHTS[variant]) {
            pick -= WEIGHTS[variant];
            variant++;
        }
        this.setVariant(variant);
        return super.finalizeSpawn(level, difficulty, reason, groupData);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt("Variant", this.getVariant());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setVariant(input.getIntOr("Variant", 0));
    }

    // ------------------------------------------------------------------ Leib

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void doPush(Entity entity) {
    }

    @Override
    protected void pushEntities() {
    }

    @Override
    public boolean causeFallDamage(double fallDistance, float damageModifier, DamageSource damageSource) {
        return false;
    }

    @Override
    protected void checkFallDamage(double ya, boolean onGround, BlockState onState, BlockPos pos) {
    }

    @Override
    public boolean isIgnoringBlockTriggers() {
        return true;
    }

    @Override
    protected Entity.MovementEmission getMovementEmission() {
        return Entity.MovementEmission.NONE;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        this.setPerched(false);
        return super.hurtServer(level, source, amount);
    }

    // ------------------------------------------------------------------ Flug

    @Override
    public void tick() {
        super.tick();
        if (this.isPerched()) {
            this.setDeltaMovement(Vec3.ZERO);
        } else {
            this.setDeltaMovement(this.getDeltaMovement().multiply(0.92, 0.85, 0.92));
        }
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        boolean calm = level.isBrightOutside() && !level.isRainingAt(this.blockPosition());
        if (this.isPerched()) {
            this.perchTicks--;
            Player near = level.getNearestPlayer(this, 2.5);
            boolean disturbed = near != null && !near.isDiscrete() && !near.isSpectator();
            boolean gone = this.flower != null && !isPerch(level.getBlockState(this.flower));
            if (disturbed || gone || calm && this.perchTicks <= 0) {
                this.setPerched(false);
                this.flower = null;
                this.flightTicks = 0;
                this.setDeltaMovement(0.0, 0.12, 0.0);
            }
            return;
        }
        this.flightTicks++;
        // nach einer Weile (und nachts oder bei Regen sofort) eine Blume suchen
        if (this.flower == null && (!calm || this.flightTicks > 140 && this.random.nextInt(30) == 0)) {
            this.flower = this.findPerch(level);
        }
        if (this.flower != null) {
            if (!isPerch(level.getBlockState(this.flower))) {
                this.flower = null;
                return;
            }
            Vec3 seat = Vec3.atBottomCenterOf(this.flower).add(0.0, perchHeight(level.getBlockState(this.flower)), 0.0);
            this.target = seat;
            if (this.position().distanceToSqr(seat) < 0.12 * 0.12) {
                this.setPerched(true);
                this.snapTo(seat.x, seat.y, seat.z, this.random.nextFloat() * 360.0f, 0.0f);
                this.perchTicks = calm ? 100 + this.random.nextInt(300) : 400;
                return;
            }
        } else if (this.target == null || this.random.nextInt(40) == 0 || this.position().distanceToSqr(this.target) < 1.0) {
            int x = Mth.floor(this.getX()) + this.random.nextInt(11) - 5;
            int z = Mth.floor(this.getZ()) + this.random.nextInt(11) - 5;
            int ground = level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z);
            this.target = new Vec3(x + 0.5, ground + 0.6 + this.random.nextDouble() * 2.2, z + 0.5);
        }
        // gaukeln: auf das Ziel zu, mit Flattern auf und ab und kleinen Schlenkern
        Vec3 to = this.target.subtract(this.position());
        double dist = to.length();
        Vec3 dir = dist < 1.0e-3 ? Vec3.ZERO : to.scale(1.0 / dist);
        double speed = this.flower != null ? Math.min(0.11, 0.03 + dist * 0.08) : 0.11;
        double bob = Mth.sin(this.tickCount * 0.9f) * 0.05;
        double wob = (this.random.nextDouble() - 0.5) * 0.06;
        Vec3 v = this.getDeltaMovement();
        Vec3 wanted = new Vec3(dir.x * speed + wob, dir.y * speed + bob, dir.z * speed - wob);
        this.setDeltaMovement(v.add(wanted.subtract(v).scale(0.25)));
        Vec3 m = this.getDeltaMovement();
        if (m.horizontalDistanceSqr() > 1.0e-5) {
            float yaw = (float) (Mth.atan2(m.z, m.x) * Mth.RAD_TO_DEG) - 90.0f;
            this.setYRot(Mth.approachDegrees(this.getYRot(), yaw, 25.0f));
            this.yBodyRot = this.getYRot();
        }
    }

    /** Eine Blume oder ein Grasbüschel in der Nähe, auf der man sitzen kann. */
    private @Nullable BlockPos findPerch(ServerLevel level) {
        BlockPos best = null;
        double bestScore = Double.MAX_VALUE;
        BlockPos here = this.blockPosition();
        for (int i = 0; i < 40; i++) {
            BlockPos p = here.offset(this.random.nextInt(13) - 6, this.random.nextInt(7) - 4, this.random.nextInt(13) - 6);
            BlockState s = level.getBlockState(p);
            if (!isPerch(s)) {
                continue;
            }
            double score = p.distSqr(here) - (s.is(BlockTags.FLOWERS) ? 40.0 : 0.0);
            if (score < bestScore) {
                bestScore = score;
                best = p.immutable();
            }
        }
        return best;
    }

    private static boolean isPerch(BlockState state) {
        return state.is(BlockTags.FLOWERS) || state.is(BlockTags.SMALL_FLOWERS) || state.is(net.minecraft.world.level.block.Blocks.SHORT_GRASS)
                || state.is(net.minecraft.world.level.block.Blocks.TALL_GRASS);
    }

    /** Wie hoch über dem Blockboden die Blüte sitzt. */
    private static double perchHeight(BlockState state) {
        if (state.is(BlockTags.FLOWERS) && !state.is(BlockTags.SMALL_FLOWERS) || state.is(net.minecraft.world.level.block.Blocks.TALL_GRASS)) {
            return 0.95;
        }
        return 0.62;
    }

    // ------------------------------------------------------------------ Auftreten

    /** Auf Gras unter freiem Himmel. */
    public static boolean checkButterflySpawnRules(EntityType<ButterflyEntity> type, LevelAccessor level, EntitySpawnReason reason, BlockPos pos,
            RandomSource random) {
        return level.getBlockState(pos.below()).is(BlockTags.ANIMALS_SPAWNABLE_ON) && level.getBrightness(LightLayer.SKY, pos) > 10;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return !this.isPersistenceRequired() && !this.hasCustomName();
    }
}
