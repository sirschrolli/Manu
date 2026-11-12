package io.github.sirschrolli.kingdomsatwar.entity.sea;

import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Ein Arm des Kraken, der beim Tentakelkäfig (seit 1.00) aus dem Wasser bricht: Sieben davon stehen im Kreis um sein Ziel
 * (einer fehlt – die Lücke), wiegen sich, kreisen und ziehen sich zusammen. Wer einen berührt, wird zurück in den Käfig
 * gestoßen; zuletzt beugen sich alle zur Mitte und schlagen zusammen (das Zermalmen rechnet der Kraken). Ein harter Treffer
 * ({@link #CUT_DAMAGE}) schlägt einen Arm ab – er zuckt zurück und versinkt, und der Kraken spürt die Hälfte davon.
 * Die Arme werden nicht gespeichert.
 */
public class KrakenTentacleEntity extends Entity {
    /** So lange bricht der Arm aus dem Wasser. */
    public static final int RISE = 14;
    /** Von hier bis {@link #CLOSE_TO} zieht sich der Käfig zusammen. */
    public static final int CLOSE_FROM = 14;
    public static final int CLOSE_TO = 54;
    /** Jetzt schlagen die Arme in der Mitte zusammen. */
    public static final int SLAM = 62;
    /** Danach versinken sie, und mit {@link #LIFE} sind sie fort. */
    public static final int SINK_FROM = 70;
    public static final int LIFE = 86;
    /** So eng wird der Käfig zuletzt. */
    public static final double END_RADIUS = 2.6;
    /** So viel Schaden mit einem Schlag trennt einen Arm ab. */
    public static final float CUT_DAMAGE = 8.0f;
    /** So lange zuckt und versinkt ein abgeschlagener Arm. */
    public static final int CUT_SINK = 14;

    private static final EntityDataAccessor<Integer> DATA_CUT = SynchedEntityData.defineId(KrakenTentacleEntity.class, EntityDataSerializers.INT);

    private @Nullable UUID owner;
    private Vec3 center = Vec3.ZERO;
    private double radius0 = 8.0;
    private double angle0;
    /** So weit kreist der Käfig je Tick (Grad). */
    private float spin;
    private final Map<UUID, Integer> touched = new HashMap<>();

    public KrakenTentacleEntity(EntityType<? extends KrakenTentacleEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    /** Stellt den Arm in den Käfig um {@code center}: Abstand, Winkel (Bogenmaß) und wie schnell der Käfig kreist. */
    public void setup(KrakenEntity kraken, Vec3 center, double radius, double angle, float spin) {
        this.owner = kraken.getUUID();
        this.center = center;
        this.radius0 = radius;
        this.angle0 = angle;
        this.spin = spin;
        this.place(0);
        this.yRotO = this.getYRot();
        this.xo = this.getX();
        this.yo = this.getY();
        this.zo = this.getZ();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_CUT, -1);
    }

    /** Wann (Alter in Ticks) der Arm abgeschlagen wurde, sonst -1. */
    public int cutAt() {
        return this.entityData.get(DATA_CUT);
    }

    public boolean isCut() {
        return this.cutAt() >= 0;
    }

    public Vec3 cageCenter() {
        return this.center;
    }

    /** Wie weit sich der Käfig nach {@code age} Ticks zusammengezogen hat (0 offen, 1 eng). */
    public static float closing(float age) {
        float k = Mth.clamp((age - CLOSE_FROM) / (float) (CLOSE_TO - CLOSE_FROM), 0.0f, 1.0f);
        return k * k * (3.0f - 2.0f * k);
    }

    private void place(int age) {
        double r = Mth.lerp(closing(age), this.radius0, END_RADIUS);
        double a = this.angle0 + Math.toRadians(this.spin * Math.min(age, SLAM));
        double x = this.center.x + Math.cos(a) * r;
        double z = this.center.z + Math.sin(a) * r;
        this.setPos(x, this.center.y - 0.5, z);
        // er wendet sich der Mitte zu
        this.setYRot(-((float) Mth.atan2(this.center.x - x, this.center.z - z)) * Mth.RAD_TO_DEG);
    }

    @Override
    public void tick() {
        super.tick();
        int age = this.tickCount;
        if (!(this.level() instanceof ServerLevel level)) {
            this.clientParticles(age);
            return;
        }
        KrakenEntity kraken = this.owner != null && level.getEntity(this.owner) instanceof KrakenEntity k && k.isAlive() ? k : null;
        if (kraken == null && !this.isCut() && age < SINK_FROM) {
            // ohne den Kraken versinkt der Arm
            this.entityData.set(DATA_CUT, age);
        }
        if (!this.isCut()) {
            this.place(age);
            if (kraken != null && age >= RISE / 2 && age < SLAM - 3) {
                this.touch(level, kraken);
            }
        }
        if (age == 2) {
            level.sendParticles(ParticleTypes.SPLASH, this.getX(), this.center.y + 0.9, this.getZ(), 60, 0.8, 0.3, 0.8, 0.4);
            level.sendParticles(ParticleTypes.BUBBLE_COLUMN_UP, this.getX(), this.center.y - 1.0, this.getZ(), 20, 0.6, 0.5, 0.6, 0.3);
        }
        if (age == SLAM) {
            level.sendParticles(ParticleTypes.SPLASH, this.center.x, this.center.y + 1.0, this.center.z, 30, 1.5, 0.4, 1.5, 0.5);
        }
        int end = this.isCut() ? this.cutAt() + CUT_SINK : LIFE;
        if (age >= end) {
            this.discard();
        }
    }

    private void clientParticles(int age) {
        if (this.isCut() || age > SINK_FROM + 6) {
            return;
        }
        // Gischt am Fuß, Tropfen, die vom Arm fallen
        if (this.random.nextInt(3) == 0) {
            double a = this.random.nextDouble() * Math.PI * 2.0;
            this.level().addParticle(ParticleTypes.SPLASH, this.getX() + Math.cos(a) * 0.9, this.center.y + 0.9, this.getZ() + Math.sin(a) * 0.9,
                    0.0, 0.1, 0.0);
        }
        if (age > RISE && this.random.nextInt(4) == 0) {
            this.level().addParticle(ParticleTypes.DRIPPING_WATER, this.getX() + (this.random.nextDouble() - 0.5) * 1.2,
                    this.getY() + 2.0 + this.random.nextDouble() * 4.5, this.getZ() + (this.random.nextDouble() - 0.5) * 1.2, 0.0, 0.0, 0.0);
        }
    }

    /** Wer den Arm berührt, wird getroffen und zurück in den Käfig gestoßen (jeder höchstens alle drei Viertelsekunden). */
    private void touch(ServerLevel level, KrakenEntity kraken) {
        AABB box = this.getBoundingBox().inflate(0.5, 0.5, 0.5);
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, box, kraken::isEnemy)) {
            Integer last = this.touched.get(e.getUUID());
            if (last != null && this.tickCount - last < 15) {
                continue;
            }
            this.touched.put(e.getUUID(), this.tickCount);
            float damage = (float) kraken.getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.4f;
            if (e.hurtServer(level, this.damageSources().mobAttack(kraken), damage)) {
                Vec3 in = this.center.subtract(e.position()).multiply(1.0, 0.0, 1.0);
                in = in.lengthSqr() < 1.0e-4 ? Vec3.ZERO : in.normalize();
                e.push(in.x * 0.9, 0.35, in.z * 0.9);
                e.needsSync = true;
                level.playSound(null, e.blockPosition(), ModSounds.KRAKEN_GRAB.get(), SoundSource.HOSTILE, 1.5f, 1.3f);
            }
        }
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        if (this.isCut() || this.tickCount < 4 || this.tickCount >= SINK_FROM || this.isInvulnerableToBase(source)
                || source.getEntity() instanceof KrakenEntity) {
            return false;
        }
        level.sendParticles(ParticleTypes.SQUID_INK, this.getX(), this.getY() + 3.0, this.getZ(), 12, 0.4, 1.2, 0.4, 0.03);
        if (amount < CUT_DAMAGE) {
            level.playSound(null, this.getX(), this.getY() + 2.0, this.getZ(), ModSounds.KRAKEN_HURT.get(), SoundSource.HOSTILE, 1.2f, 1.6f);
            return true;
        }
        // abgeschlagen: der Arm zuckt zurück und versinkt – und der Kraken spürt es
        this.entityData.set(DATA_CUT, this.tickCount);
        level.playSound(null, this.getX(), this.getY() + 2.0, this.getZ(), ModSounds.KRAKEN_HURT.get(), SoundSource.HOSTILE, 3.0f, 1.25f);
        level.sendParticles(ParticleTypes.SQUID_INK, this.getX(), this.getY() + 3.0, this.getZ(), 40, 0.6, 1.8, 0.6, 0.06);
        level.sendParticles(ParticleTypes.SPLASH, this.getX(), this.center.y + 0.9, this.getZ(), 50, 0.8, 0.3, 0.8, 0.4);
        if (this.owner != null && level.getEntity(this.owner) instanceof KrakenEntity kraken && kraken.isAlive()) {
            kraken.setInvulnerableTime(0);
            kraken.hurtServer(level, source, amount * 0.5f);
        }
        return true;
    }

    @Override
    public boolean isPickable() {
        return !this.isCut() && this.tickCount > 4 && this.tickCount < SINK_FROM;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
    }
}
