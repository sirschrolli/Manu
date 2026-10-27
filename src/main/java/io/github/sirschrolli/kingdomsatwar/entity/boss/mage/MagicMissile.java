package io.github.sirschrolli.kingdomsatwar.entity.boss.mage;

import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import io.github.sirschrolli.kingdomsatwar.entity.boss.BossEffects;
import io.github.sirschrolli.kingdomsatwar.entity.boss.BossFx;
import io.github.sirschrolli.kingdomsatwar.entity.boss.ElementalBoss;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.hurtingprojectile.AbstractHurtingProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Magisches Geschoss der Erzmagier: eine leuchtende Kugel in der Farbe des Elements, die ihrem Ziel folgt (nicht zu
 * scharf – wer quer läuft, weicht aus). Mit einem gut gezielten Schlag lässt sie sich zurückwerfen; dann fliegt sie
 * zum Magier zurück und trifft ihn selbst.
 */
public class MagicMissile extends AbstractHurtingProjectile {
    private static final EntityDataAccessor<Integer> DATA_ELEMENT = SynchedEntityData.defineId(MagicMissile.class, EntityDataSerializers.INT);
    private static final int LIFE = 110;

    private float damage = 4.0f;
    private int life;
    private double speed = 0.55;
    private @Nullable UUID targetId;
    private @Nullable UUID casterId;
    private boolean returned;

    public MagicMissile(EntityType<? extends MagicMissile> type, Level level) {
        super(type, level);
        this.accelerationPower = 0.0;
    }

    /** Schießt ein Geschoss, das {@code target} verfolgt. */
    public static MagicMissile shoot(ServerLevel level, LivingEntity caster, @Nullable LivingEntity target, Vec3 from, Vec3 dir,
            double speed, float damage) {
        MagicMissile missile = new MagicMissile(ModEntities.MAGIC_MISSILE.get(), level);
        missile.setOwner(caster);
        missile.casterId = caster.getUUID();
        missile.targetId = target != null ? target.getUUID() : null;
        missile.damage = damage;
        missile.speed = speed;
        DragonType element = caster instanceof MageLike mage ? mage.element() : DragonType.FIRE;
        missile.entityData.set(DATA_ELEMENT, element.ordinal());
        missile.setPos(from.x, from.y, from.z);
        missile.setDeltaMovement(dir.normalize().scale(speed));
        level.addFreshEntity(missile);
        return missile;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ELEMENT, 0);
    }

    public DragonType element() {
        return DragonType.values()[Mth.clamp(this.entityData.get(DATA_ELEMENT), 0, DragonType.values().length - 1)];
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt("Element", this.entityData.get(DATA_ELEMENT));
        output.putFloat("Damage", this.damage);
        if (this.targetId != null) {
            output.store("Target", UUIDUtil.CODEC, this.targetId);
        }
        if (this.casterId != null) {
            output.store("Caster", UUIDUtil.CODEC, this.casterId);
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.entityData.set(DATA_ELEMENT, input.getIntOr("Element", 0));
        this.damage = input.getFloatOr("Damage", 4.0f);
        this.targetId = input.read("Target", UUIDUtil.CODEC).orElse(null);
        this.casterId = input.read("Caster", UUIDUtil.CODEC).orElse(null);
    }

    @Override
    public void tick() {
        if (!this.level().isClientSide()) {
            if (++this.life > LIFE) {
                this.fizzle((ServerLevel) this.level());
                return;
            }
            this.steer((ServerLevel) this.level());
        }
        super.tick();
        if (this.level().isClientSide()) {
            this.trail();
        }
    }

    /** Folgt dem Ziel (zurückgeworfen: dem Magier) mit begrenzter Wendigkeit. */
    private void steer(ServerLevel level) {
        UUID follow = this.returned ? this.casterId : this.targetId;
        if (follow == null || !(level.getEntity(follow) instanceof LivingEntity goal) || !goal.isAlive()) {
            return;
        }
        Vec3 want = goal.getBoundingBox().getCenter().subtract(this.position());
        if (want.lengthSqr() < 1.0e-4) {
            return;
        }
        Vec3 v = this.getDeltaMovement();
        double turn = this.returned ? 0.35 : 0.085;
        Vec3 dir = v.normalize().lerp(want.normalize(), turn).normalize();
        this.setDeltaMovement(dir.scale(this.returned ? this.speed * 1.5 : this.speed));
    }

    private void trail() {
        if (BossFx.nearEyes(this.level(), this.position())) {
            return;                         // kurz vor dem Gesicht keine Spur – sonst hängen bunte Klötze vor den Augen
        }
        DragonType element = this.element();
        this.level().addParticle(new DustParticleOptions(element.color(), 1.1f), this.getX(), this.getY() + 0.15, this.getZ(), 0, 0, 0);
        if (this.random.nextInt(3) == 0) {
            ParticleOptions particle = element.particle();
            this.level().addParticle(particle, this.getX(), this.getY() + 0.15, this.getZ(), 0, 0.01, 0);
        }
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        if (entity instanceof MagicMissile || entity instanceof MageMinion || !super.canHitEntity(entity)) {
            return false;
        }
        if (this.returned) {
            return true;                    // zurückgeworfen: trifft alles außer dem Spieler, der es geschlagen hat
        }
        if (entity instanceof ElementalBoss || !(entity instanceof LivingEntity living)) {
            return false;
        }
        ElementalBoss boss = this.authority();
        return boss == null ? entity instanceof Player : boss.isBossEnemy(living);
    }

    /** Wessen Gegner dieses Geschoss trifft: der Magier selbst oder der Herr eines Spiegelbilds. */
    private @Nullable ElementalBoss authority() {
        Entity owner = this.getOwner();
        if (owner instanceof ElementalBoss boss) {
            return boss;
        }
        if (owner instanceof MageImageEntity image && this.level() instanceof ServerLevel level) {
            return image.master(level);
        }
        return null;
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!(this.level() instanceof ServerLevel level)) {
            return;
        }
        if (result instanceof EntityHitResult hit && hit.getEntity() instanceof LivingEntity victim) {
            Entity owner = this.getOwner();
            DamageSource source = owner instanceof LivingEntity living
                    ? this.damageSources().indirectMagic(this, living) : this.damageSources().magic();
            float amount = this.returned ? this.damage * 3.0f : this.damage;
            if (victim.hurtServer(level, source, amount) && !this.returned) {
                this.elementEffect(victim);
            }
        }
        this.fizzle(level);
    }

    /** Kleine Wirkung des Elements beim Treffer (kurz brennen, frieren, betäuben …). */
    private void elementEffect(LivingEntity victim) {
        switch (this.element()) {
            case FIRE -> BossEffects.burn(victim, 2);
            case ICE -> BossEffects.freeze(victim, 1);
            case LIGHTNING -> BossEffects.stun(victim, 1);
            case NATURE -> BossEffects.poison(victim, 2);
            case WATER -> BossEffects.soak(victim, 2);
            case EARTH -> victim.push(0.0, 0.3, 0.0);
        }
    }

    private void fizzle(ServerLevel level) {
        DragonType element = this.element();
        Vec3 at = this.position().add(0, 0.15, 0);
        BossFx.impact(level, new DustParticleOptions(element.color(), 1.4f), at, 18, 0.25, 0.25, 0.25, 0.05);
        BossFx.impact(level, ParticleTypes.ENCHANTED_HIT, at, 6, 0.2, 0.2, 0.2, 0.2);
        level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.AMETHYST_CLUSTER_BREAK, SoundSource.HOSTILE, 0.9f,
                1.3f + this.random.nextFloat() * 0.3f);
        this.discard();
    }

    /** Ein Schlag wirft das Geschoss zurück zum Magier. */
    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        if (!this.returned && source.getEntity() instanceof Player player && (this.getOwner() instanceof ElementalBoss
                || this.getOwner() instanceof MageMinion)) {
            this.returned = true;
            this.setOwner(player);
            this.life = Math.min(this.life, LIFE - 60);
            this.setDeltaMovement(player.getLookAngle().scale(this.speed * 1.5));
            level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.AMETHYST_BLOCK_HIT, SoundSource.PLAYERS, 1.4f, 1.6f);
            level.sendParticles(ParticleTypes.ENCHANTED_HIT, this.getX(), this.getY(), this.getZ(), 10, 0.2, 0.2, 0.2, 0.3);
            return true;
        }
        return false;
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public float getPickRadius() {
        return 0.6f;
    }

    @Override
    protected boolean shouldBurn() {
        return false;
    }

    @Override
    protected @Nullable ParticleOptions getTrailParticle() {
        return null;
    }

    @Override
    public boolean isOnFire() {
        return false;
    }

    @Override
    protected float getInertia() {
        return 1.0f;
    }
}
