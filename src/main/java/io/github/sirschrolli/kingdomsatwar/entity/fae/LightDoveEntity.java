package io.github.sirschrolli.kingdomsatwar.entity.fae;

import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.ModParticles;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Eine Lichttaube: MissMystik lässt sie mit einem Fingerschnippen aufflattern. Erst steigt sie und kreist kurz, dann stürzt
 * sie sich auf ihr Ziel und folgt ihm (sie wendet nicht beliebig schnell – wer zur Seite springt, entkommt). Beim Treffer
 * zerstiebt sie in Federn aus Licht; wen sie trifft, der leuchtet eine Weile (man sieht ihn durch Wände). Nach gut fünf
 * Sekunden verglimmt sie von selbst.
 */
public class LightDoveEntity extends ThrowableProjectile {
    private @Nullable LivingEntity target;
    private int huntAfter = 14;
    private float damage = 3.0f;

    public LightDoveEntity(EntityType<? extends LightDoveEntity> type, Level level) {
        super(type, level);
    }

    public LightDoveEntity(Level level, LivingEntity owner) {
        super(ModEntities.LIGHT_DOVE.get(), level);
        this.setOwner(owner);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    /** Jagt {@code target} ab Tick {@code after} (bis dahin flattert sie auf) und trifft mit {@code damage}. */
    public void setHunt(LivingEntity target, int after, float damage) {
        this.target = target;
        this.huntAfter = after;
        this.damage = damage;
    }

    @Override
    protected double getDefaultGravity() {
        return 0.0;
    }

    @Override
    public void tick() {
        Level level = this.level();
        if (!level.isClientSide()) {
            Vec3 v = this.getDeltaMovement();
            if (this.tickCount < this.huntAfter || this.target == null || !this.target.isAlive()) {
                // aufflattern: langsamer werden, sanft steigen und kreisen
                Vec3 side = new Vec3(-v.z, 0.0, v.x).scale(0.06);
                this.setDeltaMovement(v.scale(0.9).add(side).add(0.0, 0.012, 0.0));
            } else {
                Vec3 to = this.target.getEyePosition().subtract(0.0, 0.3, 0.0).subtract(this.position());
                double speed = 0.62;
                Vec3 want = to.normalize().scale(speed);
                this.setDeltaMovement(v.add(want.subtract(v).scale(0.12)));
            }
            this.needsSync = true;
            if (this.tickCount > 110) {
                this.poof((ServerLevel) level);
                return;
            }
        }
        super.tick();
        if (level.isClientSide() && this.random.nextInt(2) == 0) {
            level.addParticle(ModParticles.FAE_SPARK.get(), this.getX(), this.getY() + 0.1, this.getZ(), 0.0, -0.01, 0.0);
        }
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        if (!super.canHitEntity(entity) || entity instanceof MissMystikEntity || entity instanceof MystikImageEntity
                || entity instanceof LightDoveEntity || entity instanceof MagicCardEntity || entity instanceof ArmorStand) {
            return false;
        }
        return !(entity instanceof Player p && (p.isCreative() || p.isSpectator()));
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (!(this.level() instanceof ServerLevel level)) {
            return;
        }
        Entity hit = result.getEntity();
        if (hit.hurtServer(level, this.damageSources().indirectMagic(this, this.getOwner()), this.damage) && hit instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(MobEffects.GLOWING, 80, 0));
        }
        this.poof(level);
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (this.level() instanceof ServerLevel level) {
            this.poof(level);
        }
    }

    /** Zerstiebt in Federn aus Licht. */
    private void poof(ServerLevel level) {
        level.sendParticles(ParticleTypes.CLOUD, this.getX(), this.getY(), this.getZ(), 6, 0.15, 0.15, 0.15, 0.03);
        level.sendParticles(ParticleTypes.END_ROD, this.getX(), this.getY(), this.getZ(), 8, 0.2, 0.2, 0.2, 0.06);
        level.sendParticles(ModParticles.FAE_SPARK.get(), this.getX(), this.getY(), this.getZ(), 6, 0.2, 0.2, 0.2, 0.03);
        this.discard();
    }
}
