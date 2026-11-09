package io.github.sirschrolli.kingdomsatwar.entity.fae;

import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.ModParticles;
import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.particles.ColorParticleOption;
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
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * MissMystiks Zylinder beim Hut-Trick: Er fliegt im Bogen (sich überschlagend) zu ihrem Ziel und landet kopfüber. Dort
 * dreht er sich und saugt alles ringsum zu sich – Funken und Konfetti strudeln in ihn hinein. Dann ein Knall: wer dicht
 * am Hut steht, wird verletzt und in die Luft geworfen, und acht Karten fliegen im Kreis heraus. Zuletzt fliegt der Hut
 * zurück auf ihren Kopf. Wird nicht gespeichert.
 */
public class MagicHatEntity extends Entity {
    public static final int FLY_TICKS = 16;
    public static final int SPIN_TICKS = 54;
    /** In diesem Tick des Drehens (Sog) knallt es. */
    public static final int BURST = 48;
    public static final double PULL_RADIUS = 7.5;
    public static final double BURST_RADIUS = 3.8;

    public enum Phase { FLY, SPIN, RETURN, BACK }

    private static final EntityDataAccessor<Integer> DATA_PHASE = SynchedEntityData.defineId(MagicHatEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_PHASE_START = SynchedEntityData.defineId(MagicHatEntity.class,
            EntityDataSerializers.INT);

    private @Nullable UUID ownerId;
    private Vec3 from = Vec3.ZERO;
    private Vec3 land = Vec3.ZERO;
    private float damage = 8.0f;

    public MagicHatEntity(EntityType<? extends MagicHatEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public MagicHatEntity(Level level, MissMystikEntity owner, Vec3 from, Vec3 land, float damage) {
        this(ModEntities.MAGIC_HAT.get(), level);
        this.ownerId = owner.getUUID();
        this.from = from;
        this.land = land;
        this.damage = damage;
        this.setPos(from.x, from.y, from.z);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_PHASE, 0);
        builder.define(DATA_PHASE_START, 0);
    }

    public Phase phase() {
        int i = this.entityData.get(DATA_PHASE);
        return Phase.values()[Mth.clamp(i, 0, Phase.values().length - 1)];
    }

    /** Ticks seit Beginn der laufenden Phase. */
    public float phaseTime(float partialTicks) {
        return this.tickCount - this.entityData.get(DATA_PHASE_START) + partialTicks;
    }

    /** Ist er zurück bei MissMystik (sie setzt ihn wieder auf)? */
    public boolean isBack() {
        return this.phase() == Phase.BACK;
    }

    private void setPhase(Phase phase) {
        this.entityData.set(DATA_PHASE, phase.ordinal());
        this.entityData.set(DATA_PHASE_START, this.tickCount);
    }

    private @Nullable MissMystikEntity owner(ServerLevel level) {
        return this.ownerId != null && level.getEntity(this.ownerId) instanceof MissMystikEntity m && m.isAlive() ? m : null;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            this.clientEffects();
            return;
        }
        ServerLevel level = (ServerLevel) this.level();
        MissMystikEntity owner = this.owner(level);
        if (owner == null || this.tickCount > 240) {
            level.sendParticles(MissMystikEntity.SMOKE, this.getX(), this.getY() + 0.4, this.getZ(), 16, 0.3, 0.3, 0.3, 0.02);
            this.discard();
            return;
        }
        float t = this.phaseTime(0.0f);
        switch (this.phase()) {
            case FLY -> {
                float k = Math.min(1.0f, (t + 1.0f) / FLY_TICKS);
                Vec3 p = this.from.lerp(this.land, k).add(0.0, Math.sin(k * Math.PI) * 3.0, 0.0);
                this.setPos(p.x, p.y, p.z);
                if (k >= 1.0f) {
                    this.setPhase(Phase.SPIN);
                    level.playSound(null, this.getX(), this.getY(), this.getZ(), ModSounds.MYSTIK_VORTEX.get(), SoundSource.HOSTILE, 2.5f, 1.0f);
                    // ein Zauberkreis zeigt, wie weit der Sog reicht
                    io.github.sirschrolli.kingdomsatwar.magic.fx.SpellFxEntity.create(level,
                            io.github.sirschrolli.kingdomsatwar.magic.fx.SpellFxEntity.Kind.CIRCLE, this.position().add(0.0, 0.05, 0.0), null,
                            0xC890FF, BURST, (float) PULL_RADIUS).spawn();
                    level.sendParticles(ParticleTypes.POOF, this.getX(), this.getY() + 0.2, this.getZ(), 10, 0.3, 0.1, 0.3, 0.02);
                }
            }
            case SPIN -> this.tickSpin(level, owner, (int) t);
            case RETURN -> {
                Vec3 head = owner.position().add(0.0, 2.6, 0.0);
                Vec3 to = head.subtract(this.position());
                if (to.length() < 1.0) {
                    this.setPhase(Phase.BACK);
                    this.setDeltaMovement(Vec3.ZERO);
                } else {
                    this.setDeltaMovement(to.normalize().scale(Math.min(1.1, 0.3 + t * 0.06)));
                    this.move(MoverType.SELF, this.getDeltaMovement());
                }
            }
            case BACK -> {
            }
        }
    }

    /** Der Sog: alle in der Nähe gleiten auf den Hut zu (je näher, desto stärker); dann der Knall. */
    private void tickSpin(ServerLevel level, MissMystikEntity owner, int t) {
        Vec3 c = this.position();
        if (t < BURST) {
            AABB area = new AABB(c, c).inflate(PULL_RADIUS, 3.0, PULL_RADIUS);
            for (LivingEntity victim : owner.victims(level, area)) {
                Vec3 to = new Vec3(c.x - victim.getX(), 0.0, c.z - victim.getZ());
                double d = to.length();
                if (d > PULL_RADIUS || d < 0.4) {
                    continue;
                }
                double strength = 0.035 + 0.05 * (1.0 - d / PULL_RADIUS) + t * 0.0008;
                Vec3 pull = to.scale(strength / d);
                victim.push(pull.x, 0.0, pull.z);
                victim.needsSync = true;
            }
            if (t % 2 == 0) {
                // Funken und Konfetti strudeln hinein
                for (int i = 0; i < 4; i++) {
                    double a = t * 0.4 + i * Math.PI / 2.0;
                    double r = PULL_RADIUS * (0.4 + 0.6 * ((t * 7 + i * 13) % 10) / 10.0);
                    level.sendParticles(ModParticles.FAE_SPARK.get(), c.x + Math.cos(a) * r, c.y + 0.3, c.z + Math.sin(a) * r, 0,
                            -Math.cos(a - 0.6), 0.02, -Math.sin(a - 0.6), 0.25);
                }
                level.sendParticles(MissMystikEntity.MAGIC_DUST, c.x, c.y + 0.5, c.z, 3, 0.3, 0.2, 0.3, 0.0);
            }
            return;
        }
        if (t == BURST) {
            level.playSound(null, c.x, c.y, c.z, ModSounds.MYSTIK_TADA.get(), SoundSource.HOSTILE, 3.0f, 1.4f);
            MissMystikEntity.confetti(level, c.add(0.0, 0.8, 0.0), 80, 0.5, 0.5);
            level.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, 0xFFFF9AE6), c.x, c.y + 0.8, c.z, 1, 0.0, 0.0, 0.0, 0.0);
            level.sendParticles(ParticleTypes.FIREWORK, c.x, c.y + 0.6, c.z, 30, 0.3, 0.3, 0.3, 0.25);
            DamageSource source = this.damageSources().indirectMagic(this, owner);
            for (LivingEntity victim : owner.victims(level, new AABB(c, c).inflate(BURST_RADIUS, 2.5, BURST_RADIUS))) {
                if (victim.distanceToSqr(c) > BURST_RADIUS * BURST_RADIUS + 4.0) {
                    continue;
                }
                if (victim.hurtServer(level, source, this.damage)) {
                    MissMystikEntity.push(victim, new Vec3(0.0, 1.05, 0.0));
                }
            }
            // acht Karten fliegen im Kreis heraus
            for (int i = 0; i < 8; i++) {
                double a = i * Math.PI / 4.0;
                MagicCardEntity card = new MagicCardEntity(level, owner);
                card.setSuit(i);
                card.setDamage(4.0f);
                card.setPos(c.x, c.y + 0.7, c.z);
                card.setDeltaMovement(Math.cos(a) * 0.8, 0.05, Math.sin(a) * 0.8);
                level.addFreshEntity(card);
            }
        }
        if (t >= SPIN_TICKS) {
            this.setPhase(Phase.RETURN);
            level.playSound(null, c.x, c.y, c.z, ModSounds.MYSTIK_HAT_THROW.get(), SoundSource.HOSTILE, 1.5f, 1.3f);
        }
    }

    private void clientEffects() {
        if (this.phase() == Phase.SPIN && this.phaseTime(0.0f) < BURST) {
            // der Strudel: drei Arme aus Funken und Zauberstaub kreisen nach innen, je näher dem Knall, desto schneller
            float t = this.phaseTime(0.0f);
            double spin = t * (0.22 + t * 0.004);
            for (int i = 0; i < 3; i++) {
                double frac = (t * 0.045 + i / 3.0 + this.random.nextDouble() * 0.05) % 1.0;
                double r = PULL_RADIUS * (1.0 - frac) + 0.35;
                double a = spin + i * Math.PI * 2.0 / 3.0 + frac * 5.0;
                double x = this.getX() + Math.cos(a) * r;
                double z = this.getZ() + Math.sin(a) * r;
                double tangential = 0.12 + 0.18 * frac;
                double vx = -Math.sin(a) * tangential - Math.cos(a) * 0.08;
                double vz = Math.cos(a) * tangential - Math.sin(a) * 0.08;
                this.level().addParticle(ModParticles.FAE_SPARK.get(), x, this.getY() + 0.15 + frac * 0.9, z, vx, 0.01, vz);
                if (this.random.nextInt(2) == 0) {
                    this.level().addParticle(MissMystikEntity.MAGIC_DUST, x, this.getY() + 0.2 + frac * 0.9, z, 0.0, 0.0, 0.0);
                }
            }
        }
        if (this.phase() == Phase.SPIN && this.random.nextInt(2) == 0) {
            double a = this.random.nextDouble() * Math.PI * 2.0;
            this.level().addParticle(ParticleTypes.END_ROD, this.getX() + Math.cos(a) * 0.4, this.getY() + 0.7, this.getZ() + Math.sin(a) * 0.4,
                    0.0, 0.06, 0.0);
        }
        if (this.phase() != Phase.SPIN && this.random.nextInt(2) == 0) {
            this.level().addParticle(ModParticles.FAE_SPARK.get(), this.getX(), this.getY() + 0.3, this.getZ(), 0.0, 0.0, 0.0);
        }
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
        this.ownerId = input.read("Owner", UUIDUtil.CODEC).orElse(null);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        if (this.ownerId != null) {
            output.store("Owner", UUIDUtil.CODEC, this.ownerId);
        }
    }
}
