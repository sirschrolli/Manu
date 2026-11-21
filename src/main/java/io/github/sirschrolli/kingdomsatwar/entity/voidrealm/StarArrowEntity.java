package io.github.sirschrolli.kingdomsatwar.entity.voidrealm;

import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Ein Sternenpfeil (seit 0.99), vom Sternenbogen geschossen: schwerelos – er fliegt schnurgerade und zieht eine Spur aus
 * Sternenlicht hinter sich her; nach drei Sekunden im Flug erlischt er. Ein Netzpfeil ({@link #isWeb()}) spinnt sein Ziel ein:
 * Es kann sich eine Weile kaum rühren.
 */
public class StarArrowEntity extends AbstractArrow {
    /** So lange (Ticks) fliegt er, bevor er erlischt. */
    public static final int FLIGHT = 60;
    /** So lange (Ticks) hält das Netz eines Netzpfeils. */
    public static final int WEB_TICKS = 50;
    private static final EntityDataAccessor<Boolean> DATA_WEB = SynchedEntityData.defineId(StarArrowEntity.class, EntityDataSerializers.BOOLEAN);

    public StarArrowEntity(EntityType<? extends StarArrowEntity> type, Level level) {
        super(type, level);
    }

    public StarArrowEntity(Level level, LivingEntity owner, ItemStack pickup, @Nullable ItemStack weapon) {
        super(ModEntities.STAR_ARROW.get(), owner, level, pickup, weapon);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_WEB, false);
    }

    public void setWeb(boolean web) {
        this.entityData.set(DATA_WEB, web);
    }

    /** Ein Netzpfeil: spinnt sein Ziel ein. */
    public boolean isWeb() {
        return this.entityData.get(DATA_WEB);
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return new ItemStack(Items.ARROW);
    }

    @Override
    protected double getDefaultGravity() {
        return 0.0;
    }

    @Override
    public void tick() {
        super.tick();
        Level level = this.level();
        if (this.isInGround()) {
            return;
        }
        if (level.isClientSide()) {
            Vec3 v = this.getDeltaMovement();
            for (int i = 0; i < 2; i++) {
                double f = i * 0.5;
                level.addParticle(this.isWeb() ? new DustParticleOptions(0xE4F0FF, 0.9f) : new DustParticleOptions(0xB8D4FF, 0.8f),
                        this.getX() - v.x * f, this.getY() - v.y * f, this.getZ() - v.z * f, 0.0, 0.0, 0.0);
            }
            if (this.random.nextInt(3) == 0) {
                level.addParticle(ParticleTypes.END_ROD, this.getX(), this.getY(), this.getZ(), -v.x * 0.02, -v.y * 0.02, -v.z * 0.02);
            }
        } else if (this.tickCount > FLIGHT) {
            if (level instanceof ServerLevel server) {
                server.sendParticles(ParticleTypes.END_ROD, this.getX(), this.getY(), this.getZ(), 6, 0.1, 0.1, 0.1, 0.03);
            }
            this.discard();
        }
    }

    @Override
    protected void doPostHurtEffects(LivingEntity target) {
        super.doPostHurtEffects(target);
        if (this.isWeb() && this.level() instanceof ServerLevel level) {
            target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, WEB_TICKS, 5), this.getEffectSource());
            target.addEffect(new MobEffectInstance(MobEffects.GLOWING, WEB_TICKS, 0), this.getEffectSource());
            level.sendParticles(ParticleTypes.END_ROD, target.getX(), target.getY(0.5), target.getZ(), 16, 0.4, 0.6, 0.4, 0.02);
            level.sendParticles(new DustParticleOptions(0xE4F0FF, 1.2f), target.getX(), target.getY(0.5), target.getZ(), 20, 0.4, 0.6, 0.4, 0.0);
        }
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putBoolean("Web", this.isWeb());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setWeb(input.getBooleanOr("Web", false));
    }
}
