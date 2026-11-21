package io.github.sirschrolli.kingdomsatwar.entity.fae;

import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.ModParticles;
import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Eine Zauberkarte: MissMystik (und ihre Spiegelbilder) werfen sie im Fächer, das Kartendeck der Mystik dreifach. Sie
 * fliegt flach und wirbelnd wie ein Wurfstern, leuchtet und zerplatzt beim Treffer in Funken. Auf der Vorderseite eine der
 * vier Farben (Herz, Pik, Karo, Kreuz), hinten das violett-goldene Muster der Zauberkarten.
 *
 * <p>Als <b>Schaukarte</b> ({@link #showAt}) ist sie die riesige Karte des Tricks „Zieh eine Karte!“: Sie schwebt über dem
 * Ziel, zeigt erst den Rücken und dreht sich dann um; sie trifft niemanden (MissMystik bewegt sie selbst).
 */
public class MagicCardEntity extends ThrowableProjectile {
    public static final double SPEED = 1.15;
    private static final EntityDataAccessor<Integer> DATA_SUIT = SynchedEntityData.defineId(MagicCardEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_SHOW = SynchedEntityData.defineId(MagicCardEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_FLIP = SynchedEntityData.defineId(MagicCardEntity.class, EntityDataSerializers.INT);
    private float damage = 5.0f;

    public MagicCardEntity(EntityType<? extends MagicCardEntity> type, Level level) {
        super(type, level);
    }

    public MagicCardEntity(Level level, LivingEntity owner) {
        super(ModEntities.MAGIC_CARD.get(), level);
        this.setOwner(owner);
        this.setPos(owner.getX(), owner.getEyeY() - 0.2, owner.getZ());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_SUIT, 0);
        builder.define(DATA_SHOW, false);
        builder.define(DATA_FLIP, 0);
    }

    /** Farbe der Karte: 0 Herz, 1 Pik, 2 Karo, 3 Kreuz. */
    public void setSuit(int suit) {
        this.entityData.set(DATA_SUIT, Math.floorMod(suit, 4));
    }

    public int suit() {
        return this.entityData.get(DATA_SUIT);
    }

    public void setDamage(float damage) {
        this.damage = damage;
    }

    /** Wird zur riesigen Schaukarte über {@code pos}, die sich nach {@code flipAfter} Ticks umdreht. */
    public void showAt(Vec3 pos, int suit, int flipAfter) {
        this.setPos(pos.x, pos.y, pos.z);
        this.setDeltaMovement(Vec3.ZERO);
        this.setSuit(suit);
        this.entityData.set(DATA_SHOW, true);
        this.entityData.set(DATA_FLIP, flipAfter);
    }

    public boolean isShow() {
        return this.entityData.get(DATA_SHOW);
    }

    /** Wie weit sich die Schaukarte umgedreht hat (0 = Rücken, 1 = Vorderseite). */
    public float flip(float partialTicks) {
        float t = this.tickCount + partialTicks - this.entityData.get(DATA_FLIP);
        return Math.max(0.0f, Math.min(1.0f, t / 6.0f));
    }

    @Override
    protected double getDefaultGravity() {
        return 0.0;
    }

    @Override
    public void tick() {
        if (this.isShow()) {
            this.baseTick();
            if (this.level().isClientSide()) {
                if (this.random.nextInt(2) == 0) {
                    this.level().addParticle(ModParticles.FAE_SPARK.get(), this.getRandomX(1.4), this.getRandomY(), this.getRandomZ(1.4), 0.0,
                            -0.01, 0.0);
                }
            } else if (this.tickCount > 120) {
                this.discard();
            }
            return;
        }
        super.tick();
        Level level = this.level();
        if (level.isClientSide()) {
            if (this.random.nextInt(2) == 0) {
                Vec3 v = this.getDeltaMovement();
                level.addParticle(ModParticles.FAE_SPARK.get(), this.getX() - v.x * 0.4, this.getY() - v.y * 0.4 + 0.1, this.getZ() - v.z * 0.4,
                        0.0, 0.0, 0.0);
            }
        } else if (this.tickCount > 70) {
            this.poof((ServerLevel) level);
        }
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        if (this.isShow() || !super.canHitEntity(entity) || entity instanceof MagicCardEntity || entity instanceof LightDoveEntity
                || entity instanceof ArmorStand) {
            return false;
        }
        Entity owner = this.getOwner();
        if (owner instanceof Player player) {
            return entity != player && !(entity instanceof OwnableEntity pet && pet.getOwner() == player)
                    && !(entity instanceof Player other && (other.isCreative() || other.isSpectator() || !player.canHarmPlayer(other)));
        }
        if (entity instanceof MissMystikEntity || entity instanceof MystikImageEntity) {
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
        Entity target = result.getEntity();
        Entity owner = this.getOwner();
        boolean byPlayer = owner instanceof Player;
        var source = byPlayer ? this.damageSources().thrown(this, owner) : this.damageSources().indirectMagic(this, owner);
        if (target.hurtServer(level, source, this.damage)) {
            Vec3 v = this.getDeltaMovement().multiply(1.0, 0.0, 1.0);
            if (target instanceof LivingEntity living && v.lengthSqr() > 1.0e-4) {
                Vec3 push = v.normalize().scale(0.25);
                living.push(push.x, 0.08, push.z);
                living.needsSync = true;
            }
        }
        level.playSound(null, this.getX(), this.getY(), this.getZ(), ModSounds.MYSTIK_CARD_HIT.get(), byPlayer ? SoundSource.PLAYERS
                : SoundSource.HOSTILE, 0.9f, 1.0f + this.random.nextFloat() * 0.3f);
        this.poof(level);
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (this.level() instanceof ServerLevel level) {
            this.poof(level);
        }
    }

    private void poof(ServerLevel level) {
        level.sendParticles(ParticleTypes.ENCHANTED_HIT, this.getX(), this.getY(), this.getZ(), 6, 0.1, 0.1, 0.1, 0.15);
        level.sendParticles(ModParticles.FAE_SPARK.get(), this.getX(), this.getY(), this.getZ(), 4, 0.15, 0.15, 0.15, 0.02);
        this.discard();
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt("Suit", this.suit());
        output.putFloat("Damage", this.damage);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setSuit(input.getIntOr("Suit", 0));
        this.damage = input.getFloatOr("Damage", 5.0f);
    }
}
