package io.github.sirschrolli.kingdomsatwar.entity.abyss;

import io.github.sirschrolli.kingdomsatwar.item.abyss.TimewardenArmor;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Das Pendel des Stundenwächters (seit 1.1): Hoch über seinem Ziel erscheint ein riesiges Pendel aus Uhrbronze – eine lange
 * Stange, unten eine sichelförmige Klinge um eine Scheibe aus Chronit. Es hängt zuerst zur Seite gehoben (eine Linie aus
 * Bernstein am Boden zeigt seine Bahn), dann schwingt es dreimal über den Boden: Wer in seiner Bahn steht, wird getroffen und
 * mitgerissen – wer zur Seite tritt, nicht. Danach vergeht es. Das Wesen steht am Aufhängepunkt. Wird nicht gespeichert.
 */
public class ClockPendulumEntity extends Entity {
    /** So lange (Ticks) erscheint es (zur Seite gehoben) … */
    public static final int APPEAR = 26;
    /** … so lange dauert ein halber Schwung … */
    public static final int HALF_SWING = 20;
    /** … so oft schwingt es (halbe Schwünge) … */
    public static final int SWINGS = 3;
    /** … und so lange vergeht es danach. */
    public static final int FADE = 12;
    /** Die Stange: so lang (Blöcke vom Aufhängepunkt zur Mitte der Klinge). */
    public static final float LENGTH = 14.0f;
    /** So weit (Grad) schwingt es aus. */
    public static final float AMPLITUDE = 52.0f;
    /** Die Klinge: halbe Breite quer zur Bahn, halbe Dicke entlang der Bahn, halbe Höhe (Blöcke). */
    public static final double BLADE_HALF_WIDTH = 2.7;
    public static final double BLADE_HALF_DEPTH = 1.3;
    public static final double BLADE_HALF_HEIGHT = 1.6;

    private static final EntityDataAccessor<Float> DATA_DIR = SynchedEntityData.defineId(ClockPendulumEntity.class, EntityDataSerializers.FLOAT);

    private @Nullable HourWardenEntity owner;
    private float damage = HourWardenEntity.PENDULUM_DAMAGE;
    private final Map<UUID, Integer> hits = new HashMap<>();

    public ClockPendulumEntity(EntityType<? extends ClockPendulumEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    /** Hängt das Pendel über {@code ground} auf (dort ist der tiefste Punkt seiner Bahn); es schwingt entlang {@code dir}. */
    public void setup(HourWardenEntity owner, Vec3 ground, Vec3 dir, float damage) {
        this.owner = owner;
        this.damage = damage;
        Vec3 d = new Vec3(dir.x, 0.0, dir.z);
        d = d.lengthSqr() < 1.0e-6 ? new Vec3(0.0, 0.0, 1.0) : d.normalize();
        this.entityData.set(DATA_DIR, (float) (Mth.atan2(-d.x, d.z) * Mth.RAD_TO_DEG));
        this.setPos(ground.x, ground.y + 0.6 + LENGTH, ground.z);
    }

    public @Nullable HourWardenEntity owner() {
        return this.owner;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_DIR, 0.0f);
    }

    /** Die Richtung seiner Bahn (Grad wie eine Blickrichtung). */
    public float direction() {
        return this.entityData.get(DATA_DIR);
    }

    /** Die Richtung seiner Bahn als Einheitsvektor. */
    public Vec3 dirVector() {
        float yaw = this.direction() * Mth.DEG_TO_RAD;
        return new Vec3(-Mth.sin(yaw), 0.0, Mth.cos(yaw));
    }

    /** Der Ausschlag in diesem Augenblick (Grad; positiv: entlang der Bahn vorn). */
    public float swingAngle(float partialTicks) {
        float t = this.tickCount + partialTicks;
        if (t < APPEAR) {
            return AMPLITUDE;
        }
        float s = Math.min(t - APPEAR, SWINGS * HALF_SWING);
        return AMPLITUDE * Mth.cos((float) Math.PI * s / HALF_SWING);
    }

    /** Wie sichtbar es ist (blendet ein und aus). */
    public float alpha(float partialTicks) {
        float t = this.tickCount + partialTicks;
        float in = Mth.clamp(t / 12.0f, 0.0f, 1.0f);
        float end = APPEAR + SWINGS * HALF_SWING;
        float out = 1.0f - Mth.clamp((t - end) / FADE, 0.0f, 1.0f);
        return Math.min(in, out);
    }

    /** Wo die Mitte der Klinge gerade ist. */
    public Vec3 bladeCenter(float partialTicks) {
        double a = Math.toRadians(this.swingAngle(partialTicks));
        Vec3 d = this.dirVector();
        return this.position().add(d.scale(LENGTH * Math.sin(a))).add(0.0, -LENGTH * Math.cos(a), 0.0);
    }

    @Override
    public void tick() {
        super.tick();
        int t = this.tickCount;
        int end = APPEAR + SWINGS * HALF_SWING;
        if (this.level().isClientSide()) {
            if (t < end && this.random.nextInt(2) == 0) {
                Vec3 b = this.bladeCenter(0.0f);
                this.level().addParticle(TimewardenArmor.AMBER, b.x + (this.random.nextDouble() - 0.5) * 3.0, b.y + (this.random.nextDouble() - 0.5),
                        b.z + (this.random.nextDouble() - 0.5) * 3.0, 0.0, 0.0, 0.0);
            }
            return;
        }
        ServerLevel level = (ServerLevel) this.level();
        if (t > end + FADE || this.owner == null || !this.owner.isAlive()) {
            this.discard();
            return;
        }
        Vec3 d = this.dirVector();
        Vec3 ground = this.position().add(0.0, -LENGTH - 0.5, 0.0);
        // die Bahn am Boden: eine Linie aus Bernstein
        if (t % (t < APPEAR ? 3 : 6) == 0 && t < end) {
            for (int i = -14; i <= 14; i++) {
                Vec3 p = ground.add(d.scale(i * 0.55));
                level.sendParticles(TimewardenArmor.AMBER, p.x, p.y + 0.15, p.z, 1, 0.05, 0.0, 0.05, 0.0);
            }
        }
        if (t == APPEAR) {
            level.playSound(null, ground.x, ground.y, ground.z, ModSounds.HOUR_WARDEN_PENDULUM.get(), SoundSource.HOSTILE, 3.0f, 0.8f);
        }
        if (t < APPEAR || t > end) {
            return;
        }
        // in der Mitte jedes Schwungs rauscht es am lautesten
        if ((t - APPEAR) % HALF_SWING == HALF_SWING / 2) {
            level.playSound(null, ground.x, ground.y, ground.z, ModSounds.HOUR_WARDEN_PENDULUM.get(), SoundSource.HOSTILE, 2.5f, 1.0f);
        }
        Vec3 blade = this.bladeCenter(0.0f);
        Vec3 across = new Vec3(-d.z, 0.0, d.x);
        int half = (t - APPEAR) / HALF_SWING;
        float sign = half % 2 == 0 ? -1.0f : 1.0f;                     // es schwingt zuerst von vorn nach hinten
        for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, new AABB(blade, blade).inflate(3.2, 2.5, 3.2), this.owner::canStrike)) {
            Vec3 rel = victim.position().add(0.0, victim.getBbHeight() * 0.5, 0.0).subtract(blade);
            if (Math.abs(rel.dot(d)) > BLADE_HALF_DEPTH + victim.getBbWidth() * 0.5 || Math.abs(rel.dot(across)) > BLADE_HALF_WIDTH
                    || Math.abs(rel.y) > BLADE_HALF_HEIGHT + victim.getBbHeight() * 0.5) {
                continue;
            }
            Integer last = this.hits.get(victim.getUUID());
            if (last != null && last == half) {
                continue;
            }
            this.hits.put(victim.getUUID(), half);
            if (victim.hurtServer(level, this.damageSources().mobAttack(this.owner), this.damage)) {
                victim.push(d.x * sign * 1.3, 0.45, d.z * sign * 1.3);
                victim.needsSync = true;
                level.sendParticles(ParticleTypes.CRIT, victim.getX(), victim.getY(0.6), victim.getZ(), 12, 0.3, 0.4, 0.3, 0.3);
            }
        }
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 160.0 * 160.0;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        return false;
    }

    @Override
    public boolean isPickable() {
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
