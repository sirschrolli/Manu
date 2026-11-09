package io.github.sirschrolli.kingdomsatwar.entity.fae;

import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Ein Spiegelbild MissMystiks (Spiegelkabinett): sieht genau aus wie sie (nur ein wenig durchscheinend, wer genau
 * hinsieht), umkreist ihr Ziel und wirft dann und wann eine einzelne Karte. Beim ersten Treffer zerspringt es mit einem
 * Knall, der den Angreifer zurückwirft; wird die echte MissMystik getroffen, zerspringen alle. Nach zwölf Sekunden vergeht
 * es von selbst. Keine Beute, keine Erfahrung; wird nicht gespeichert.
 */
public class MystikImageEntity extends Monster {
    private static final EntityDataAccessor<Integer> DATA_THROW = SynchedEntityData.defineId(MystikImageEntity.class, EntityDataSerializers.INT);
    private @Nullable UUID ownerId;
    private @Nullable LivingEntity target;
    private float orbit;
    private boolean encore;
    private int life = 240;
    private int nextThrow = 40;
    /** Nur im Client: Phase des Flügelschlags. */
    public float flap;
    public float flapO;

    public MystikImageEntity(EntityType<? extends MystikImageEntity> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
        this.xpReward = 0;
    }

    public MystikImageEntity(Level level, MissMystikEntity owner) {
        this(ModEntities.MYSTIK_IMAGE.get(), level);
        this.ownerId = owner.getUUID();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 1.0).add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.FOLLOW_RANGE, 32.0).add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_THROW, -100);
    }

    /** Wen es umkreist, wo es beginnt (Winkel) und ob die Zugabe läuft (dann wirft es öfter). */
    public void setup(LivingEntity target, float angle, boolean encore) {
        this.target = target;
        this.orbit = angle;
        this.encore = encore;
        this.nextThrow = 30 + this.random.nextInt(30);
    }

    public boolean isImageOf(MissMystikEntity owner) {
        return owner.getUUID().equals(this.ownerId);
    }

    /** Ticks seit dem letzten Kartenwurf (für die Animation). */
    public float sinceThrow(float partialTicks) {
        return this.tickCount - this.entityData.get(DATA_THROW) + partialTicks;
    }

    private @Nullable MissMystikEntity owner(ServerLevel level) {
        return this.ownerId != null && level.getEntity(this.ownerId) instanceof MissMystikEntity m && m.isAlive() ? m : null;
    }

    @Override
    public void travel(Vec3 input) {
        this.move(MoverType.SELF, this.getDeltaMovement());
        this.setDeltaMovement(this.getDeltaMovement().scale(0.85));
    }

    @Override
    public void tick() {
        this.noPhysics = true;
        super.tick();
        if (this.level().isClientSide()) {
            this.flapO = this.flap;
            this.flap += 0.32f;
            if (this.random.nextInt(4) == 0) {
                this.level().addParticle(ParticleTypes.END_ROD, this.getRandomX(0.8), this.getRandomY(), this.getRandomZ(0.8), 0.0, 0.01, 0.0);
            }
        }
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        MissMystikEntity owner = this.owner(level);
        if (owner == null || --this.life <= 0 || this.target == null || !this.target.isAlive()) {
            this.shatter(level, false);
            return;
        }
        // um das Ziel kreisen, zwei Blöcke über dem Boden, ihm zugewandt
        this.orbit += this.encore ? 0.022f : 0.016f;
        Vec3 around = this.target.position().add(Mth.cos(this.orbit) * 7.0, 0.0, Mth.sin(this.orbit) * 7.0);
        int ground = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Mth.floor(around.x), Mth.floor(around.z));
        double y = Math.max(ground, this.target.getY()) + 2.2 + Mth.sin(this.tickCount * 0.08f) * 0.4;
        Vec3 to = new Vec3(around.x, y, around.z).subtract(this.position());
        Vec3 v = this.getDeltaMovement().add(to.normalize().scale(Math.min(to.length(), 4.0) * 0.0125));
        this.setDeltaMovement(v.length() > 0.4 ? v.normalize().scale(0.4) : v);
        float yaw = (float) (Mth.atan2(this.target.getZ() - this.getZ(), this.target.getX() - this.getX()) * Mth.RAD_TO_DEG) - 90.0f;
        float next = Mth.approachDegrees(this.getYRot(), yaw, 12.0f);
        this.setYRot(next);
        this.yBodyRot = next;
        this.yHeadRot = next;
        if (--this.nextThrow <= 0) {
            this.nextThrow = (this.encore ? 40 : 55) + this.random.nextInt(25);
            this.entityData.set(DATA_THROW, this.tickCount);
        }
        if (this.tickCount - this.entityData.get(DATA_THROW) == MissMystikEntity.CARDS_THROW) {
            Vec3 from = this.position().add(0.0, 1.9, 0.0);
            Vec3 aim = this.target.position().add(0.0, this.target.getBbHeight() * 0.5, 0.0);
            MagicCardEntity card = new MagicCardEntity(level, this);
            card.setSuit(this.random.nextInt(4));
            card.setDamage(3.0f);
            card.setPos(from.x, from.y, from.z);
            card.setDeltaMovement(aim.subtract(from).normalize().scale(MagicCardEntity.SPEED));
            level.addFreshEntity(card);
            level.playSound(null, this.getX(), this.getY(), this.getZ(), ModSounds.MYSTIK_CARD_THROW.get(), SoundSource.HOSTILE, 1.2f, 1.2f);
        }
    }

    /** Getroffen: zerspringt (mit einem Knall, der den Angreifer zurückwirft). */
    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        if (this.isRemoved()) {
            return false;
        }
        Entity attacker = source.getEntity();
        if (attacker instanceof MissMystikEntity || attacker instanceof MystikImageEntity) {
            return false;
        }
        this.shatter(level, true);
        if (attacker instanceof LivingEntity living && living.distanceToSqr(this) < 6.0 * 6.0
                && !(living instanceof Player p && (p.isCreative() || p.isSpectator()))) {
            MissMystikEntity owner = this.owner(level);
            living.hurtServer(level, this.damageSources().indirectMagic(this, owner != null ? owner : this), 3.0f);
            Vec3 away = living.position().subtract(this.position()).multiply(1.0, 0.0, 1.0);
            away = away.lengthSqr() < 1.0e-4 ? Vec3.ZERO : away.normalize();
            living.push(away.x * 0.9, 0.35, away.z * 0.9);
            living.needsSync = true;
        }
        return true;
    }

    /** Zerspringt wie ein Spiegel: Splitter, Konfetti, ein heller Klang. */
    public void shatter(ServerLevel level, boolean loud) {
        if (this.isRemoved()) {
            return;
        }
        level.sendParticles(ParticleTypes.END_ROD, this.getX(), this.getY(1.0), this.getZ(), 20, 0.4, 0.9, 0.4, 0.08);
        level.sendParticles(ParticleTypes.ENCHANTED_HIT, this.getX(), this.getY(1.0), this.getZ(), 16, 0.4, 0.9, 0.4, 0.2);
        MissMystikEntity.confetti(level, this.position().add(0.0, 1.5, 0.0), loud ? 30 : 12, 0.4, 0.2);
        if (loud) {
            level.playSound(null, this.getX(), this.getY(), this.getZ(), ModSounds.MYSTIK_MIRRORS.get(), SoundSource.HOSTILE, 2.0f, 1.5f);
        }
        this.discard();
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    /** Es vergeht nach zwölf Sekunden von selbst (oder wenn MissMystik fort ist) – nicht, weil niemand in der Nähe ist. */
    @Override
    public boolean removeWhenFarAway(double distanceSqr) {
        return false;
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    @Override
    protected void checkFallDamage(double ya, boolean onGround, BlockState onState, BlockPos pos) {
    }

    @Override
    public boolean causeFallDamage(double distance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    protected @Nullable SoundEvent getHurtSound(DamageSource source) {
        return null;
    }

    @Override
    protected @Nullable SoundEvent getDeathSound() {
        return null;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
    }
}
