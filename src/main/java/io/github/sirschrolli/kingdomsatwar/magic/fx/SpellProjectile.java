package io.github.sirschrolli.kingdomsatwar.magic.fx;

import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import io.github.sirschrolli.kingdomsatwar.entity.boss.BossEffects;
import io.github.sirschrolli.kingdomsatwar.entity.boss.BossFx;
import io.github.sirschrolli.kingdomsatwar.entity.boss.DragonBossEntity;
import io.github.sirschrolli.kingdomsatwar.entity.boss.mage.MagicMissile;
import io.github.sirschrolli.kingdomsatwar.magic.Spell;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.ModParticles;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.EventHooks;
import org.jspecify.annotations.Nullable;

/**
 * Zaubergeschosse mit eigener Gestalt:
 * <ul>
 *   <li>Feuerblitz – der Feuerzauber des Stabs: ein glühender Ball aus Flammen (Stufe III zerbirst er in einer Feuerwolke).</li>
 *   <li>Phönix – Ignatius' Feuervogel: steigt auf, zieht einen Bogen und stürzt sich auf sein Ziel, wo er in einer
 *   Feuersäule zerbirst.</li>
 *   <li>Frostlanze – Isoldes Eisspeer: schnell und gerade, lässt erstarren.</li>
 * </ul>
 * Verbündete des Zaubernden fliegen sie vorbei; Blöcke setzen sie nie in Brand.
 */
public class SpellProjectile extends Projectile {
    public enum Kind {
        FIREBOLT(70),
        PHOENIX(130),
        ICE_LANCE(60);

        private final int maxLife;

        Kind(int maxLife) {
            this.maxLife = maxLife;
        }

        public static Kind of(int index) {
            return values()[Mth.clamp(index, 0, values().length - 1)];
        }
    }

    private static final EntityDataAccessor<Byte> DATA_KIND = SynchedEntityData.defineId(SpellProjectile.class, EntityDataSerializers.BYTE);

    private float damage = 5.0f;
    private int spellLevel = 1;
    private double speed = 1.0;
    private int life;
    private @Nullable UUID targetId;

    public SpellProjectile(EntityType<? extends SpellProjectile> type, Level level) {
        super(type, level);
    }

    /**
     * Schießt ein Geschoss.
     *
     * @param target wem der Phönix nachjagt (sonst null)
     * @param spellLevel Stufe des Zaubers (Feuerblitz III zerbirst)
     */
    public static SpellProjectile shoot(ServerLevel level, LivingEntity caster, Kind kind, Vec3 from, Vec3 dir, double speed, float damage,
            @Nullable LivingEntity target, int spellLevel) {
        SpellProjectile p = new SpellProjectile(ModEntities.SPELL_PROJECTILE.get(), level);
        p.entityData.set(DATA_KIND, (byte) kind.ordinal());
        p.setOwner(caster);
        p.damage = damage;
        p.speed = speed;
        p.spellLevel = spellLevel;
        p.targetId = target != null ? target.getUUID() : null;
        p.setPos(from.x, from.y, from.z);
        Vec3 v = dir.normalize().scale(speed);
        p.setDeltaMovement(v);
        p.setYRot((float) (Mth.atan2(v.x, v.z) * Mth.RAD_TO_DEG));
        p.setXRot((float) (Mth.atan2(v.y, v.horizontalDistance()) * Mth.RAD_TO_DEG));
        p.yRotO = p.getYRot();
        p.xRotO = p.getXRot();
        level.addFreshEntity(p);
        return p;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_KIND, (byte) 0);
    }

    public Kind kind() {
        return Kind.of(this.entityData.get(DATA_KIND));
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt("Kind", this.kind().ordinal());
        output.putFloat("Damage", this.damage);
        output.putInt("SpellLevel", this.spellLevel);
        output.putDouble("Speed", this.speed);
        if (this.targetId != null) {
            output.store("Target", UUIDUtil.CODEC, this.targetId);
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.entityData.set(DATA_KIND, (byte) input.getIntOr("Kind", 0));
        this.damage = input.getFloatOr("Damage", 5.0f);
        this.spellLevel = input.getIntOr("SpellLevel", 1);
        this.speed = input.getDoubleOr("Speed", 1.0);
        this.targetId = input.read("Target", UUIDUtil.CODEC).orElse(null);
    }

    // ------------------------------------------------------------------ Flug

    @Override
    public void tick() {
        if (this.level() instanceof ServerLevel level) {
            if (++this.life > this.kind().maxLife) {
                this.burst(level, this.position(), null);
                this.discard();
                return;
            }
            this.steer(level);
        }
        HitResult hit = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
        boolean impact = hit.getType() != HitResult.Type.MISS && !EventHooks.onProjectileImpact(this, hit);
        this.setPos(impact ? hit.getLocation() : this.position().add(this.getDeltaMovement()));
        this.updateRotation();
        super.tick();
        if (impact && this.isAlive()) {
            this.hitTargetOrDeflectSelf(hit);
        }
        if (this.level().isClientSide()) {
            this.trail();
        }
    }

    /** Der Phönix steigt erst auf und zieht einen Bogen, dann stößt er auf sein Ziel herab. */
    private void steer(ServerLevel level) {
        if (this.kind() != Kind.PHOENIX || this.targetId == null || !(level.getEntity(this.targetId) instanceof LivingEntity target)
                || !target.isAlive()) {
            return;
        }
        Vec3 want = target.getBoundingBox().getCenter().subtract(this.position());
        if (want.lengthSqr() < 1.3 * 1.3) {
            // dicht am Ziel: er zerbirst, statt vorbeizufliegen
            this.burst(level, this.position(), target);
            this.discard();
            return;
        }
        double turn = this.life < 8 ? 0.05 : this.life < 24 ? 0.11 : 0.18;
        Vec3 dir = this.getDeltaMovement().normalize().lerp(want.normalize(), turn).normalize();
        double speed = this.speed * (this.life < 12 ? 0.8 : 1.0 + Math.min(0.5, this.life * 0.01));
        this.setDeltaMovement(dir.scale(speed));
    }

    private void trail() {
        if (BossFx.nearEyes(this.level(), this.position())) {
            return;
        }
        Vec3 back = this.position().subtract(this.getDeltaMovement().scale(0.5));
        switch (this.kind()) {
            case FIREBOLT -> {
                this.level().addParticle(ModParticles.BOSS_FLAME.get(), back.x, back.y + 0.1, back.z, 0.0, 0.01, 0.0);
                if (this.random.nextInt(2) == 0) {
                    this.level().addParticle(ParticleTypes.SMOKE, back.x, back.y + 0.1, back.z, 0.0, 0.02, 0.0);
                }
            }
            case PHOENIX -> {
                for (int i = 0; i < 2; i++) {
                    this.level().addParticle(ModParticles.BOSS_FLAME.get(), back.x + (this.random.nextDouble() - 0.5) * 0.8,
                            back.y + (this.random.nextDouble() - 0.5) * 0.5, back.z + (this.random.nextDouble() - 0.5) * 0.8, 0.0, 0.02, 0.0);
                }
                if (this.random.nextInt(3) == 0) {
                    this.level().addParticle(ParticleTypes.LAVA, back.x, back.y, back.z, 0.0, 0.0, 0.0);
                }
            }
            case ICE_LANCE -> {
                this.level().addParticle(ParticleTypes.SNOWFLAKE, back.x, back.y, back.z, 0.0, 0.0, 0.0);
                if (this.random.nextInt(2) == 0) {
                    this.level().addParticle(ModParticles.BOSS_FROST.get(), back.x, back.y, back.z, 0.0, 0.0, 0.0);
                }
            }
        }
    }

    // ------------------------------------------------------------------ Treffer

    @Override
    protected boolean canHitEntity(Entity entity) {
        if (!super.canHitEntity(entity) || entity instanceof SpellProjectile || entity instanceof MagicMissile
                || !(entity instanceof LivingEntity)) {
            return false;
        }
        return !(this.getOwner() instanceof LivingEntity caster) || !Spell.isAlly(caster, entity);
    }

    @Override
    protected void onHitEntity(EntityHitResult hit) {
        if (!(this.level() instanceof ServerLevel level) || !(hit.getEntity() instanceof LivingEntity victim)) {
            return;
        }
        switch (this.kind()) {
            case FIREBOLT -> {
                int fire = victim.getRemainingFireTicks();
                victim.igniteForSeconds(5.0f);
                if (!victim.hurtServer(level, this.fireSource(), this.damage)) {
                    victim.setRemainingFireTicks(fire);
                }
            }
            case PHOENIX -> this.burst(level, hit.getLocation(), victim);
            case ICE_LANCE -> {
                if (victim.hurtServer(level, this.damageSources().indirectMagic(this, this.getOwner()), this.damage)) {
                    BossEffects.freeze(victim, 2);
                    victim.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 2), this.getOwner());
                }
            }
        }
    }

    @Override
    protected void onHit(HitResult hit) {
        super.onHit(hit);
        if (!(this.level() instanceof ServerLevel level)) {
            return;
        }
        Vec3 at = hit.getLocation();
        switch (this.kind()) {
            case FIREBOLT -> {
                level.sendParticles(ModParticles.BOSS_FLAME.get(), at.x, at.y, at.z, 14, 0.25, 0.25, 0.25, 0.05);
                level.sendParticles(ParticleTypes.SMOKE, at.x, at.y, at.z, 8, 0.15, 0.15, 0.15, 0.02);
                if (this.spellLevel >= 3) {
                    this.fireCloud(level, at, hit instanceof EntityHitResult e ? e.getEntity() : null);
                }
            }
            case PHOENIX -> {
                if (!(hit instanceof EntityHitResult)) {
                    this.burst(level, at, null);
                }
            }
            case ICE_LANCE -> {
                level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.PACKED_ICE.defaultBlockState()), at.x, at.y, at.z, 16, 0.2,
                        0.2, 0.2, 0.1);
                level.sendParticles(ParticleTypes.SNOWFLAKE, at.x, at.y, at.z, 10, 0.3, 0.3, 0.3, 0.05);
                level.playSound(null, at.x, at.y, at.z, SoundEvents.GLASS_BREAK, SoundSource.HOSTILE, 0.8f, 1.3f + this.random.nextFloat() * 0.3f);
            }
        }
        this.discard();
    }

    /** Feuerblitz III: eine Feuerwolke um den Einschlag. */
    private void fireCloud(ServerLevel level, Vec3 at, @Nullable Entity direct) {
        level.sendParticles(ParticleTypes.EXPLOSION, at.x, at.y, at.z, 1, 0, 0, 0, 0);
        level.sendParticles(ModParticles.BOSS_FLAME.get(), at.x, at.y, at.z, 30, 1.0, 0.6, 1.0, 0.06);
        level.playSound(null, at.x, at.y, at.z, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 0.9f, 1.3f);
        if (!(this.getOwner() instanceof LivingEntity owner)) {
            return;
        }
        DamageSource source = this.fireSource();
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(3.0),
                e -> e != direct && e.isAlive() && !Spell.isAlly(owner, e) && e.distanceToSqr(at) < 9.0)) {
            e.igniteForSeconds(4.0f);
            e.hurtServer(level, source, this.damage * 0.6f);
        }
    }

    /** Der Phönix zerbirst: Feuersäule, Flammen ringsum, alle Gegner in der Nähe brennen. */
    private void burst(ServerLevel level, Vec3 at, @Nullable LivingEntity direct) {
        if (this.kind() != Kind.PHOENIX) {
            return;
        }
        SpellFxEntity.create(level, SpellFxEntity.Kind.PILLAR, at.add(0, -0.6, 0), DragonType.FIRE, 0xFF8A2A, 16, 1.5f).param(5.0f).spawn();
        SpellFxEntity.create(level, SpellFxEntity.Kind.CIRCLE, at.add(0, -0.55, 0), DragonType.FIRE, 0xFF5A1A, 26, 3.0f).spawn();
        BossFx.impact(level, ParticleTypes.EXPLOSION, at, 2, 0.6, 0.3, 0.6, 0);
        BossFx.impact(level, ModParticles.BOSS_FLAME.get(), at, 50, 1.4, 0.8, 1.4, 0.1);
        BossFx.impact(level, ParticleTypes.LAVA, at, 10, 0.8, 0.3, 0.8, 0.1);
        level.playSound(null, at.x, at.y, at.z, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 1.6f, 1.1f);
        level.playSound(null, at.x, at.y, at.z, SoundEvents.BLAZE_SHOOT, SoundSource.HOSTILE, 1.4f, 0.5f);
        Entity owner = this.getOwner();
        double radius = 3.5;
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(radius + 1.0), LivingEntity::isAlive)) {
            if (owner instanceof LivingEntity caster && Spell.isAlly(caster, e)) {
                continue;
            }
            double d = e.getBoundingBox().getCenter().distanceTo(at);
            if (d > radius + 0.5 && e != direct) {
                continue;
            }
            float f = e == direct ? 1.0f : (float) Math.max(0.5, 1.0 - d / (radius + 0.5));
            DamageSource source = this.fireSource();
            e.hurtServer(level, source, this.damage * f);
            BossEffects.burn(e, 4);
            DragonBossEntity.pushAway(e, at, 0.8 * f, 0.4 * f);
        }
    }

    /** Feuerschaden wie von einem Feuerball (zählt als Feuer und als Geschoss), dem Zaubernden zugeschrieben. */
    private DamageSource fireSource() {
        return this.damageSources().source(net.minecraft.world.damagesource.DamageTypes.FIREBALL, this, this.getOwner());
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        return false;
    }

    @Override
    public boolean isOnFire() {
        return false;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 128.0 * 128.0;
    }
}
