package io.github.sirschrolli.kingdomsatwar.entity.nether;

import io.github.sirschrolli.kingdomsatwar.block.nether.FlameAltarBlockEntity;
import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import io.github.sirschrolli.kingdomsatwar.entity.creature.nether.Glutgeist;
import io.github.sirschrolli.kingdomsatwar.magic.fx.SpellFxEntity;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Ignara, die Flammenpriesterin (seit 1.2): die letzte Priesterin der Feuerdiener, die einst durch die Verlorene Pforte in
 * die Unterwelt kamen. Im Flammenheiligtum mitten im Lavameer hütet sie das ewige Feuer auf dem Flammenaltar – sie schwebt
 * davor, das Haupt gesenkt, und betet. Wer näher als {@link #WAKE_RADIUS} Blöcke herantritt, weckt sie:
 * <ul>
 *     <li><b>Peitschenhieb</b> – ein weiter Hieb mit der Flammenpeitsche (in der Phönixgestalt zwei);</li>
 *     <li><b>Flammenschlinge</b> – die Peitsche schlingt sich um ihr Opfer (achtzehn Blöcke weit, freie Sicht) und reißt es
 *     heran; dann schlägt sie zu;</li>
 *     <li><b>Lavawoge</b> – sie reißt die Arme hoch und schlägt die Feuerkugel auf den Boden: Ein Ring aus Glut rollt über das
 *     Heiligtum (darüber springen!);</li>
 *     <li><b>Feuersäulen</b> – unter ihren Gegnern glühen Kreise auf, dann brechen Säulen aus Feuer hervor;</li>
 *     <li><b>Opalregen</b> – das Rad hinter ihren Schultern dreht sich schneller, und Splitter aus Feueropal regnen auf ihre
 *     Gegner herab;</li>
 *     <li><b>Ruf der Glutgeister</b> – sie ruft zwei Glutgeister aus dem Feuer;</li>
 *     <li><b>Phönixgestalt</b> – unter halber Kraft steigt sie auf, Schwingen aus Flammen brechen aus ihrem Rücken; danach ist
 *     sie schneller und härter, wer ihr zu nahe kommt, verbrennt, und sie kennt den <b>Phönixsturz</b>: aus der Höhe stößt
 *     sie in einer Linie durch das Heiligtum und zieht eine Spur aus Feuer.</li>
 * </ul>
 * Findet sie niemanden mehr, kehrt sie vor den Altar zurück und betet wieder (geheilt, ohne Schwingen).
 */
public class IgnaraEntity extends Monster implements io.github.sirschrolli.kingdomsatwar.entity.boss.sky.SkyWarden {
    public static final float MAX_HEALTH = 900.0f;
    public static final int AWAKEN_TICKS = 70;
    /** So lange (Ticks) dauert ihr Sterben (sie steigt auf, die Flammen verlöschen, sie zerfällt zu Glut und Asche). */
    public static final int DEATH_TICKS = 80;
    /** So nah (waagerecht) muss man ihr kommen, damit sie erwacht. */
    public static final double WAKE_RADIUS = 12.0;
    /** Weiter als so weit von ihrem Platz folgt sie niemandem … */
    private static final double LEASH = 36.0;
    /** … und wer sie aus dem Heiligtum lockt, dem entkommt sie in einer Flammensäule zurück vor den Altar. */
    public static final double ARENA = 17.0;
    /** So lange ohne Gegner, dann betet sie wieder vor dem Altar. */
    private static final int RETURN_TICKS = 400;
    /** So hoch schwebt sie über dem Boden. */
    public static final double HOVER = 0.6;
    public static final float LASH_DAMAGE = 12.0f;
    public static final double LASH_RANGE = 6.8;
    public static final double SNARE_RANGE = 18.0;
    public static final int SNARE_THROW = 16;
    public static final int SNARE_STRIKE = 34;
    public static final float SNARE_DAMAGE = 14.0f;
    public static final float WAVE_DAMAGE = 9.0f;
    public static final int WAVE_HIT = 18;
    public static final float WAVE_START = 1.5f;
    public static final float WAVE_SPEED = 0.55f;
    public static final int WAVE_LIFE = 26;
    public static final float PILLAR_DAMAGE = 12.0f;
    public static final double PILLAR_RADIUS = 1.7;
    public static final int PILLAR_ERUPT = 32;
    public static final float SHARD_DAMAGE = 7.0f;
    public static final float DIVE_DAMAGE = 14.0f;
    public static final float PHOENIX_BURST = 10.0f;
    /** Die Farbe ihres Feuers. */
    public static final int FLAME = 0xFF7A1E;

    public enum Attack {
        NONE(0), PRAY(0), AWAKEN(AWAKEN_TICKS), LASH(32), SNARE(56), WAVE(50), PILLARS(62), OPAL_RAIN(66), CALL(36), PHOENIX(70),
        DIVE(46);

        public final int duration;

        Attack(int duration) {
            this.duration = duration;
        }
    }

    private static final EntityDataAccessor<Integer> DATA_ATTACK = SynchedEntityData.defineId(IgnaraEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_PHOENIX = SynchedEntityData.defineId(IgnaraEntity.class, EntityDataSerializers.BOOLEAN);

    private final ServerBossEvent bossBar = new ServerBossEvent(UUID.randomUUID(), Component.translatable("boss.kingdomsatwar.ignara"),
            BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.NOTCHED_10);
    private Attack attack = Attack.NONE;
    private Attack lastAttack = Attack.NONE;
    private int attackTick;
    private int cooldown = 30;
    private int calls;
    private int idleTicks;
    /** Für Tests: nur wer so nah ist, wird getroffen (siehe {@link #confine}). */
    private double confine = Double.MAX_VALUE;
    private int orbit = 1;
    private @Nullable BlockPos home;
    private @Nullable BlockPos altar;
    private final Set<UUID> struck = new HashSet<>();
    private final Set<UUID> struckSecond = new HashSet<>();
    /** Flammenschlinge: wen die Peitsche erwischt hat. */
    private @Nullable UUID snared;
    /** Feuersäulen: wo die Kreise glühen. */
    private final List<Vec3> pillars = new ArrayList<>();
    /** Phönixsturz: Anfang und Ende der Bahn. */
    private @Nullable Vec3 diveFrom;
    private @Nullable Vec3 diveTo;
    private int clientAttackStart;

    public IgnaraEntity(EntityType<? extends IgnaraEntity> type, Level level) {
        super(type, level);
        this.xpReward = 450;
        this.setPersistenceRequired();
        this.setNoGravity(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, MAX_HEALTH)
                .add(Attributes.ARMOR, 10.0)
                .add(Attributes.ARMOR_TOUGHNESS, 3.0)
                .add(Attributes.ATTACK_DAMAGE, 12.0)
                .add(Attributes.MOVEMENT_SPEED, 0.28)
                .add(Attributes.FLYING_SPEED, 0.28)
                .add(Attributes.FOLLOW_RANGE, 40.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ATTACK, 0);
        builder.define(DATA_PHOENIX, false);
    }

    public Attack currentAttack() {
        int i = this.entityData.get(DATA_ATTACK);
        return i >= 0 && i < Attack.values().length ? Attack.values()[i] : Attack.NONE;
    }

    /** In der Phönixgestalt (unter halber Kraft): Schwingen aus Flammen, schneller, härter. */
    public boolean isPhoenix() {
        return this.entityData.get(DATA_PHOENIX);
    }

    /** Betet sie vor dem Altar (unverwundbar)? */
    public boolean isPraying() {
        return this.currentAttack() == Attack.PRAY;
    }

    /** Wie lange der laufende Angriff schon dauert (im Client, für die Animationen). */
    public float attackTime(float partialTicks) {
        return this.tickCount - this.clientAttackStart + partialTicks;
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (DATA_ATTACK.equals(key)) {
            this.clientAttackStart = this.tickCount;
        }
    }

    /** Bindet sie an ihren Altar: Sie schwebt bei {@code home} und betet (unverwundbar, bis jemand näher kommt). */
    public void bindTo(BlockPos home, @Nullable BlockPos altar) {
        this.home = home.immutable();
        this.altar = altar == null ? null : altar.immutable();
        this.begin(Attack.PRAY);
    }

    /** Neu gerufen (ein Feueropal im erloschenen Altar): Sie erwacht sofort. */
    public void rise(BlockPos home, @Nullable BlockPos altar) {
        this.home = home.immutable();
        this.altar = altar == null ? null : altar.immutable();
        this.begin(Attack.AWAKEN);
    }

    /** Sie erwacht (wenn sie betet). */
    public void awaken() {
        if (this.attack == Attack.PRAY || this.attack == Attack.NONE && this.getTarget() == null) {
            this.begin(Attack.AWAKEN);
        }
    }

    public @Nullable BlockPos home() {
        return this.home;
    }

    public @Nullable BlockPos altar() {
        return this.altar;
    }

    /** Beginnt sofort einen bestimmten Angriff (für Tests und die Vorführung). */
    public void forceAttack(Attack next) {
        this.begin(next);
    }

    /** Sofort in der Phönixgestalt (für Tests und die Vorführung). */
    public void forcePhoenix() {
        this.entityData.set(DATA_PHOENIX, true);
    }

    /** Die Kreise der Feuersäulen, die gerade glühen (für Tests). */
    public List<Vec3> pillars() {
        return List.copyOf(this.pillars);
    }

    /** Wie oft sie in diesem Kampf schon Glutgeister gerufen hat (für Tests). */
    public int calls() {
        return this.calls;
    }

    private void begin(Attack next) {
        this.attack = next;
        this.attackTick = 0;
        this.entityData.set(DATA_ATTACK, next.ordinal());
        if (next != Attack.NONE && next != Attack.AWAKEN && next != Attack.PRAY && next != Attack.PHOENIX) {
            this.lastAttack = next;
        }
        this.struck.clear();
        this.struckSecond.clear();
        if (next != Attack.PILLARS) {
            this.pillars.clear();
        }
        this.bossBar.setVisible(next != Attack.PRAY);
    }

    // ------------------------------------------------------------------ Grundverhalten

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double distanceSqr) {
        return false;
    }

    @Override
    public boolean canBeLeashed() {
        return false;
    }

    @Override
    protected boolean canRide(Entity vehicle) {
        return false;
    }

    @Override
    public boolean causeFallDamage(double distance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public boolean isPersistenceRequired() {
        return true;
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        this.bossBar.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.bossBar.removePlayer(player);
    }

    /**
     * Im Gebet, beim Erwachen und während sie zum Phönix wird, ist sie unverwundbar (wer sie im Gebet angreift, weckt sie);
     * Feuer, Lava und Wände schaden ihr nie.
     */
    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return super.hurtServer(level, source, amount);
        }
        if (this.attack == Attack.PRAY) {
            if (source.getEntity() instanceof Player player && !player.isCreative() && !player.isSpectator()) {
                this.wakeAltar(level);
            }
            return false;
        }
        if (this.attack == Attack.AWAKEN || this.attack == Attack.PHOENIX && this.attackTick < 26 || source.is(DamageTypes.IN_WALL)
                || source.is(DamageTypeTags.IS_FIRE)) {
            return false;
        }
        boolean hurt = super.hurtServer(level, source, amount);
        if (hurt && source.getEntity() instanceof LivingEntity attacker && attacker != this && this.canStrike(attacker)
                && !(this.getTarget() instanceof Player)) {
            this.setTarget(attacker);
        }
        return hurt;
    }

    private void wakeAltar(ServerLevel level) {
        if (this.altar != null && level.getBlockEntity(this.altar) instanceof FlameAltarBlockEntity flame && flame.owns(this)) {
            flame.awaken(level);
        } else {
            this.begin(Attack.AWAKEN);
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide()) {
            this.clientEffects();
        }
    }

    /** Glut steigt aus dem Saum und dem Haar; in der Phönixgestalt lodert sie, Lava tropft aus den Schwingen. */
    private void clientEffects() {
        Level level = this.level();
        boolean phoenix = this.isPhoenix();
        if (this.random.nextInt(phoenix ? 1 : 2) == 0) {
            level.addParticle(ParticleTypes.FLAME, this.getRandomX(0.8), this.getY() + this.random.nextDouble() * 0.4, this.getRandomZ(0.8),
                    0.0, 0.03, 0.0);
        }
        if (this.random.nextInt(3) == 0) {
            level.addParticle(ParticleTypes.SMALL_FLAME, this.getRandomX(0.4), this.getY(0.95 + this.random.nextDouble() * 0.15),
                    this.getRandomZ(0.4), 0.0, 0.05, 0.0);
        }
        if (phoenix && this.random.nextInt(3) == 0) {
            float yaw = this.yBodyRot * Mth.DEG_TO_RAD;
            double side = (this.random.nextBoolean() ? 1.0 : -1.0) * (1.0 + this.random.nextDouble() * 1.8);
            level.addParticle(ParticleTypes.DRIPPING_LAVA, this.getX() + Math.cos(yaw) * side + Math.sin(yaw) * 0.6,
                    this.getY(0.7), this.getZ() + Math.sin(yaw) * side - Math.cos(yaw) * 0.6, 0.0, 0.0, 0.0);
        }
        if (this.random.nextInt(5) == 0) {
            level.addParticle(ParticleTypes.SMOKE, this.getRandomX(0.5), this.getY(1.05), this.getRandomZ(0.5), 0.0, 0.04, 0.0);
        }
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        this.bossBar.setProgress(this.getHealth() / this.getMaxHealth());
        if (this.tickCount % 40 == 0 && this.strayed(level)) {
            return;
        }
        if (this.attack == Attack.PRAY) {
            this.tickPray(level);
            return;
        }
        if (this.attack == Attack.AWAKEN) {
            this.tickAwaken(level);
            return;
        }
        if (this.tickCount % 10 == 0) {
            this.keepInSanctuary(level);
        }
        if (!this.isPhoenix() && this.attack != Attack.PHOENIX && this.getHealth() < this.getMaxHealth() * 0.5f) {
            this.begin(Attack.PHOENIX);
        }
        if (this.attack == Attack.PHOENIX) {
            this.tickPhoenix(level);
            return;
        }
        if (this.isPhoenix() && this.tickCount % 20 == 0) {
            this.aura(level);
        }
        LivingEntity target = this.findTarget(level);
        this.setTarget(target);
        if (target == null) {
            this.idle(level);
            return;
        }
        this.idleTicks = 0;
        this.getLookControl().setLookAt(target, 30.0f, 30.0f);
        if (this.attack == Attack.NONE) {
            this.hover(target, 1.0);
            if (--this.cooldown <= 0) {
                this.begin(this.chooseAttack(level, target));
            }
            return;
        }
        this.attackTick++;
        this.lookAt(target, 40.0f, 40.0f);
        if (this.attack != Attack.DIVE) {
            this.hover(target, 0.25);
        }
        switch (this.attack) {
            case LASH -> this.tickLash(level, target);
            case SNARE -> this.tickSnare(level, target);
            case WAVE -> this.tickWave(level);
            case PILLARS -> this.tickPillars(level, target);
            case OPAL_RAIN -> this.tickOpalRain(level, target);
            case CALL -> this.tickCall(level);
            case DIVE -> this.tickDive(level, target);
            default -> { }
        }
        if (this.attackTick >= this.attack.duration) {
            this.begin(Attack.NONE);
            this.cooldown = this.isPhoenix() ? 12 + this.random.nextInt(14) : 22 + this.random.nextInt(18);
            if (this.random.nextInt(3) == 0) {
                this.orbit = -this.orbit;
            }
        }
    }

    /**
     * Gehört sie (noch) zu ihrem Altar? Ist dort inzwischen eine andere Priesterin gebunden (etwa nach dem Neuladen der Welt),
     * verglüht diese hier; ist der Altar fort, bleibt sie frei.
     */
    private boolean strayed(ServerLevel level) {
        if (this.altar == null || !level.isLoaded(this.altar)) {
            return false;
        }
        if (level.getBlockEntity(this.altar) instanceof FlameAltarBlockEntity flame) {
            if (!flame.owns(this)) {
                level.sendParticles(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY(0.5), this.getZ(), 30, 0.5, 1.0, 0.5, 0.02);
                this.discard();
                return true;
            }
            return false;
        }
        this.altar = null;
        if (this.attack == Attack.PRAY) {
            this.begin(Attack.NONE);
        }
        return false;
    }

    /** Wen ihre Angriffe treffen: alle Lebewesen außer ihr, ihren Glutgeistern und Spielern im Kreativ-/Zuschauermodus. */
    public boolean canStrike(LivingEntity entity) {
        return entity != this && entity.isAlive() && !(entity instanceof Glutgeist) && !(entity instanceof IgnaraEntity)
                && !(entity instanceof ArmorStand) && !(entity instanceof Player player && (player.isCreative() || player.isSpectator()));
    }

    private List<LivingEntity> victims(ServerLevel level, AABB area) {
        return level.getEntitiesOfClass(LivingEntity.class, area, e -> this.canStrike(e)
                && e.distanceToSqr(this.getX(), e.getY(), this.getZ()) <= this.confine * this.confine);
    }

    /** Für Tests: Ihre Angriffe treffen nur, wer so nah (waagerecht) bei ihr ist – sonst träfen sie die Nachbartests. */
    public void confine(double radius) {
        this.confine = radius;
    }

    private @Nullable LivingEntity findTarget(ServerLevel level) {
        if (this.home != null && this.distanceToSqr(Vec3.atCenterOf(this.home)) > LEASH * LEASH) {
            return null;
        }
        LivingEntity current = this.getTarget();
        if (current != null && current.isAlive() && current.distanceToSqr(this) < 44.0 * 44.0 && this.canStrike(current)) {
            return current;
        }
        return level.getNearestPlayer(this.getX(), this.getY(), this.getZ(), Math.min(32.0, this.confine),
                e -> e instanceof Player p && !p.isCreative() && !p.isSpectator() && p.isAlive());
    }

    /** Die Mitte des Heiligtums (der Altar, sonst ihr Platz, sonst wo sie schwebt). */
    public Vec3 center() {
        if (this.altar != null) {
            return Vec3.atBottomCenterOf(this.altar);
        }
        return this.home != null ? Vec3.atBottomCenterOf(this.home) : this.position();
    }

    /** Die Höhe des Bodens, über dem sie schwebt (ihr Platz; ohne Platz – etwa aus dem Spawn-Ei – der Boden unter ihr). */
    public double floorY() {
        if (this.home != null) {
            return this.home.getY();
        }
        BlockPos.MutableBlockPos p = this.blockPosition().mutable();
        for (int i = 0; i < 12; i++) {
            p.move(net.minecraft.core.Direction.DOWN);
            if (!this.level().getBlockState(p).getCollisionShape(this.level(), p).isEmpty()) {
                return p.getY() + 1;
            }
        }
        return this.getY() - HOVER;
    }

    private Vec3 forward() {
        float yaw = this.getYRot() * Mth.DEG_TO_RAD;
        return new Vec3(-Mth.sin(yaw), 0.0, Mth.cos(yaw));
    }

    private Vec3 toward(Entity target) {
        Vec3 dir = target.position().subtract(this.position()).multiply(1.0, 0.0, 1.0);
        return dir.lengthSqr() < 1.0e-4 ? this.forward() : dir.normalize();
    }

    /** Wo sie die Peitsche hält (die rechte Hand, auf Brusthöhe vor ihr). */
    private Vec3 whipHand() {
        Vec3 right = this.forward().yRot(-Mth.HALF_PI);
        return this.position().add(this.forward().scale(0.8)).add(right.scale(0.7)).add(0.0, 1.9, 0.0);
    }

    /**
     * Sie schwebt um ihr Ziel (gut fünf Blöcke Abstand, mal links-, mal rechtsherum), dicht über dem Boden und sacht auf und
     * ab; nie aus dem Heiligtum hinaus.
     */
    private void hover(@Nullable LivingEntity target, double scale) {
        Vec3 pos = this.position();
        Vec3 goal;
        if (target != null) {
            Vec3 away = new Vec3(pos.x - target.getX(), 0.0, pos.z - target.getZ());
            double d = away.length();
            away = d < 1.0e-3 ? this.forward().scale(-1.0) : away.scale(1.0 / d);
            Vec3 tangent = new Vec3(-away.z, 0.0, away.x).scale(this.orbit);
            goal = new Vec3(target.getX(), 0.0, target.getZ()).add(away.scale(5.5)).add(tangent.scale(2.2));
        } else {
            goal = this.home != null ? Vec3.atBottomCenterOf(this.home) : pos;
        }
        Vec3 c = this.center();
        Vec3 off = new Vec3(goal.x - c.x, 0.0, goal.z - c.z);
        double limit = ARENA - 3.0;
        if (off.length() > limit) {
            off = off.normalize().scale(limit);
            goal = new Vec3(c.x + off.x, 0.0, c.z + off.z);
        }
        double wantY = this.floorY() + HOVER + Math.sin(this.tickCount * 0.08) * 0.15;
        Vec3 flat = new Vec3(goal.x - pos.x, 0.0, goal.z - pos.z);
        double len = flat.length();
        double speed = (this.isPhoenix() ? 0.26 : 0.17) * scale;
        Vec3 move = len > 0.3 ? flat.scale(Math.min(speed, len * 0.25) / len) : Vec3.ZERO;
        this.setDeltaMovement(move.x, Mth.clamp((wantY - pos.y) * 0.2, -0.35, 0.35), move.z);
    }

    /** Aus dem Heiligtum gelockt (oder in die Lava gestürzt): Sie kehrt in einer Flammensäule vor den Altar zurück. */
    private void keepInSanctuary(ServerLevel level) {
        if (this.home == null || this.attack == Attack.DIVE) {
            return;
        }
        Vec3 c = this.center();
        double dx = this.getX() - c.x;
        double dz = this.getZ() - c.z;
        if (dx * dx + dz * dz < ARENA * ARENA && Math.abs(this.getY() - this.floorY()) < 9.0) {
            return;
        }
        level.sendParticles(ParticleTypes.FLAME, this.getX(), this.getY(0.5), this.getZ(), 60, 0.6, 1.4, 0.6, 0.05);
        this.teleportTo(this.home.getX() + 0.5, this.home.getY() + HOVER, this.home.getZ() + 0.5);
        this.setDeltaMovement(Vec3.ZERO);
        SpellFxEntity.create(level, SpellFxEntity.Kind.PILLAR, this.position(), DragonType.FIRE, FLAME, 18, 1.0f).param(4.0f).spawn();
        this.sound(level, ModSounds.IGNARA_PILLAR.get(), 2.0f, 1.3f);
    }

    /** Ohne Gegner: zurück vor den Altar; nach einer Weile heilt das ewige Feuer sie, und sie betet wieder. */
    private void idle(ServerLevel level) {
        if (this.attack != Attack.NONE) {
            this.begin(Attack.NONE);
        }
        this.idleTicks++;
        this.hover(null, 0.8);
        if (this.home != null && this.altar != null && this.idleTicks > RETURN_TICKS
                && this.distanceToSqr(Vec3.atBottomCenterOf(this.home).add(0.0, HOVER, 0.0)) < 2.5) {
            this.returnToAltar(level);
            return;
        }
        if (this.idleTicks > 200 && this.tickCount % 20 == 0) {
            this.heal(5.0f);
        }
    }

    /** Sie senkt das Haupt und betet wieder vor dem Altar – geheilt, ohne Schwingen. */
    private void returnToAltar(ServerLevel level) {
        if (this.altar == null || !(level.getBlockEntity(this.altar) instanceof FlameAltarBlockEntity flame) || !flame.owns(this)) {
            return;
        }
        this.setHealth(this.getMaxHealth());
        this.entityData.set(DATA_PHOENIX, false);
        this.calls = 0;
        this.idleTicks = 0;
        this.faceAltar();
        this.begin(Attack.PRAY);
        flame.onPriestessReturned(level, this);
    }

    private void faceAltar() {
        if (this.home == null || this.altar == null) {
            return;
        }
        double dx = this.altar.getX() - this.home.getX();
        double dz = this.altar.getZ() - this.home.getZ();
        float yaw = (float) (Mth.atan2(dz, dx) * Mth.RAD_TO_DEG) - 90.0f;
        this.snapTo(this.home.getX() + 0.5, this.home.getY() + HOVER, this.home.getZ() + 0.5, yaw, 0.0f);
        this.yBodyRot = yaw;
        this.yHeadRot = yaw;
    }

    private Attack chooseAttack(ServerLevel level, LivingEntity target) {
        double distance = this.distanceTo(target);
        List<Attack> options = new ArrayList<>();
        if (distance < 6.0) {
            options.addAll(List.of(Attack.LASH, Attack.LASH, Attack.WAVE, Attack.PILLARS));
        } else if (distance < 12.0) {
            options.addAll(List.of(Attack.SNARE, Attack.WAVE, Attack.PILLARS, Attack.OPAL_RAIN, Attack.LASH));
        } else {
            options.addAll(List.of(Attack.SNARE, Attack.SNARE, Attack.OPAL_RAIN, Attack.OPAL_RAIN, Attack.PILLARS));
        }
        if (this.isPhoenix()) {
            options.add(Attack.DIVE);
            options.add(Attack.DIVE);
        }
        if (this.calls < 3 && this.getHealth() < this.getMaxHealth() * 0.8f && this.spirits(level) < 2) {
            options.add(Attack.CALL);
        }
        options.removeIf(a -> a == this.lastAttack);
        if (options.isEmpty()) {
            options.add(distance < 6.0 ? Attack.LASH : Attack.PILLARS);
        }
        return options.get(this.random.nextInt(options.size()));
    }

    private int spirits(ServerLevel level) {
        return level.getEntitiesOfClass(Glutgeist.class, this.getBoundingBox().inflate(24.0), LivingEntity::isAlive).size();
    }

    private void sound(ServerLevel level, SoundEvent sound, float volume, float pitch) {
        level.playSound(null, this.getX(), this.getY(), this.getZ(), sound, SoundSource.HOSTILE, volume, pitch);
    }

    // ------------------------------------------------------------------ Gebet und Erwachen

    /** Sie schwebt vor dem Altar, das Haupt gesenkt, die Hände vor der Brust; dann und wann singt sie leise. */
    private void tickPray(ServerLevel level) {
        int t = ++this.attackTick;
        if (this.home != null) {
            double wantY = this.home.getY() + HOVER + Math.sin(this.tickCount * 0.05) * 0.1;
            this.setDeltaMovement(0.0, Mth.clamp((wantY - this.getY()) * 0.2, -0.2, 0.2), 0.0);
        }
        if (t % 140 == 30) {
            this.sound(level, ModSounds.IGNARA_PRAY.get(), 1.2f, 0.95f + this.random.nextFloat() * 0.1f);
        }
        if (t % 6 == 0 && this.altar != null) {
            Vec3 top = Vec3.atBottomCenterOf(this.altar).add(0.0, 1.4, 0.0);
            level.sendParticles(ParticleTypes.SMALL_FLAME, top.x, top.y, top.z, 1, 0.25, 0.15, 0.25, 0.01);
        }
    }

    /** Sie hebt das Haupt, breitet die Arme aus, die Flammen ihres Haars lodern auf, die Peitsche entrollt sich – und sie ruft. */
    private void tickAwaken(ServerLevel level) {
        int t = ++this.attackTick;
        Player nearest = level.getNearestPlayer(this, 24.0);
        if (nearest != null && t > 10) {
            this.getLookControl().setLookAt(nearest, 20.0f, 30.0f);
            if (t > 30) {
                this.lookAt(nearest, 12.0f, 30.0f);
            }
        }
        double wantY = this.floorY() + HOVER + (t < 40 ? t / 40.0 * 1.2 : Math.max(0.0, 1.2 - (t - 40) / 25.0 * 1.2));
        this.setDeltaMovement(0.0, Mth.clamp((wantY - this.getY()) * 0.2, -0.25, 0.25), 0.0);
        if (t == 4) {
            this.sound(level, ModSounds.IGNARA_AWAKEN.get(), 3.0f, 1.0f);
        }
        if (t == 30) {
            this.sound(level, ModSounds.IGNARA_SWING.get(), 2.0f, 0.8f);
        }
        if (t == 44) {
            this.sound(level, ModSounds.IGNARA_LASH.get(), 3.0f, 0.9f);
            level.sendParticles(ParticleTypes.FLAME, this.getX(), this.getY(0.8), this.getZ(), 90, 1.0, 0.8, 1.0, 0.12);
            level.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, 0xFFFFB050), this.getX(), this.getY(0.6), this.getZ(), 1,
                    0.0, 0.0, 0.0, 0.0);
            Component title = Component.translatable("boss.kingdomsatwar.ignara").withColor(0xFF9A3A);
            Component subtitle = Component.translatable("boss.kingdomsatwar.ignara.title").withStyle(ChatFormatting.GRAY);
            for (ServerPlayer player : level.players()) {
                if (player.distanceToSqr(this) < 64.0 * 64.0) {
                    player.connection.send(new ClientboundSetTitlesAnimationPacket(15, 60, 25));
                    player.connection.send(new ClientboundSetTitleTextPacket(title));
                    player.connection.send(new ClientboundSetSubtitleTextPacket(subtitle));
                }
            }
        }
        if (t >= AWAKEN_TICKS) {
            this.begin(Attack.NONE);
            this.cooldown = 14;
            this.idleTicks = 0;
        }
    }

    // ------------------------------------------------------------------ Angriffe

    /** Peitschenhieb: ein weiter Bogen aus Flammen vor ihr (in der Phönixgestalt schlägt sie zurück). */
    private void tickLash(ServerLevel level, LivingEntity target) {
        int t = this.attackTick;
        if (t == 4) {
            this.sound(level, ModSounds.IGNARA_SWING.get(), 1.8f, 1.0f + this.random.nextFloat() * 0.1f);
        }
        boolean second = this.isPhoenix() && t == 22;
        if (t != 12 && !second) {
            return;
        }
        Vec3 dir = this.toward(target);
        this.sound(level, ModSounds.IGNARA_LASH.get(), 2.4f, second ? 1.15f : 1.0f);
        for (int i = 0; i <= 16; i++) {
            double k = i / 16.0;
            double a = (k - 0.5) * (second ? -1.8 : 1.8);
            double r = 1.6 + Math.sin(k * Math.PI) * (LASH_RANGE - 1.6);
            Vec3 p = this.position().add(dir.yRot((float) a).scale(r)).add(0.0, 1.0 + Math.sin(k * Math.PI) * 0.5, 0.0);
            level.sendParticles(ParticleTypes.FLAME, p.x, p.y, p.z, 2, 0.06, 0.06, 0.06, 0.01);
            if (i % 4 == 2) {
                level.sendParticles(ParticleTypes.LAVA, p.x, p.y, p.z, 1, 0.0, 0.0, 0.0, 0.0);
            }
        }
        float damage = LASH_DAMAGE + (this.isPhoenix() ? 3.0f : 0.0f);
        for (LivingEntity victim : this.victims(level, this.getBoundingBox().inflate(LASH_RANGE + 1.0, 2.5, LASH_RANGE + 1.0))) {
            Vec3 to = victim.position().subtract(this.position()).multiply(1.0, 0.0, 1.0);
            double distance = to.length();
            if (distance > LASH_RANGE + victim.getBbWidth() * 0.5 || distance > 0.6 && to.normalize().dot(dir) < 0.3) {
                continue;
            }
            if (victim.hurtServer(level, this.damageSources().mobAttack(this), damage)) {
                victim.igniteForSeconds(5.0f);
                victim.push(dir.x * 0.6, 0.25, dir.z * 0.6);
                victim.needsSync = true;
            }
        }
    }

    /**
     * Flammenschlinge: Sie lässt die Peitsche über dem Kopf kreisen und schleudert sie; wen sie erwischt (in achtzehn Blöcken,
     * freie Sicht), den reißt sie zu sich heran – und empfängt ihn mit einem Hieb. Wer hinter Deckung geht, entgeht ihr.
     */
    private void tickSnare(ServerLevel level, LivingEntity target) {
        int t = this.attackTick;
        Vec3 hand = this.whipHand();
        if (t == 2) {
            this.sound(level, ModSounds.IGNARA_SWING.get(), 2.0f, 0.8f);
        }
        if (t < SNARE_THROW) {
            for (int i = 0; i < 3; i++) {
                double a = t * 0.9 + i * Math.PI * 2.0 / 3.0;
                level.sendParticles(ParticleTypes.FLAME, hand.x + Math.cos(a) * 1.5, hand.y + 1.2, hand.z + Math.sin(a) * 1.5, 1, 0.0, 0.0, 0.0, 0.0);
            }
            return;
        }
        if (t == SNARE_THROW) {
            this.sound(level, ModSounds.IGNARA_LASH.get(), 2.5f, 0.85f);
            this.snared = null;
            if (target.isAlive() && target.distanceTo(this) < SNARE_RANGE && this.hasLineOfSight(target)) {
                this.snared = target.getUUID();
                target.igniteForSeconds(2.0f);
                LivingEntity victim = target;
                SpellFxEntity.create(level, SpellFxEntity.Kind.BEAM, hand, DragonType.FIRE, FLAME, SNARE_STRIKE - SNARE_THROW + 2, 0.28f)
                        .end(victim.position().add(0.0, victim.getBbHeight() * 0.6, 0.0)).onTick(fx -> {
                            if (this.attack != Attack.SNARE || this.attackTick >= SNARE_STRIKE || !victim.isAlive() || this.isRemoved()) {
                                fx.discard();
                                return;
                            }
                            fx.setPos(this.whipHand());
                            fx.moveEnd(victim.position().add(0.0, victim.getBbHeight() * 0.6, 0.0));
                        }).spawn();
            } else {
                Vec3 miss = hand.add(target.position().add(0.0, 1.0, 0.0).subtract(hand).normalize().scale(SNARE_RANGE * 0.6));
                SpellFxEntity.create(level, SpellFxEntity.Kind.BEAM, hand, DragonType.FIRE, FLAME, 6, 0.22f).end(miss).spawn();
            }
        }
        LivingEntity caught = this.snared != null && level.getEntity(this.snared) instanceof LivingEntity e && e.isAlive() ? e : null;
        if (caught != null && t > SNARE_THROW && t < SNARE_STRIKE) {
            Vec3 to = this.position().subtract(caught.position());
            double d = to.multiply(1.0, 0.0, 1.0).length();
            if (d > 2.4) {
                Vec3 pull = to.normalize().scale(Math.min(1.25, 0.35 + d * 0.08));
                caught.setDeltaMovement(pull.x, Math.max(caught.getDeltaMovement().y, 0.12), pull.z);
                caught.needsSync = true;
                caught.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 20, 2), this);
            }
            if (t % 3 == 0) {
                level.sendParticles(ParticleTypes.FLAME, caught.getX(), caught.getY(0.5), caught.getZ(), 4, 0.3, 0.4, 0.3, 0.01);
            }
        }
        if (t == SNARE_STRIKE) {
            this.sound(level, ModSounds.IGNARA_LASH.get(), 3.0f, 1.2f);
            Vec3 dir = this.forward();
            Vec3 hit = this.position().add(dir.scale(1.8));
            level.sendParticles(ParticleTypes.FLAME, hit.x, this.getY() + 1.0, hit.z, 30, 0.8, 0.5, 0.8, 0.06);
            float damage = SNARE_DAMAGE + (this.isPhoenix() ? 3.0f : 0.0f);
            for (LivingEntity victim : this.victims(level, this.getBoundingBox().inflate(3.4, 1.8, 3.4))) {
                if (victim.hurtServer(level, this.damageSources().mobAttack(this), damage)) {
                    Vec3 out = victim.position().subtract(this.position()).multiply(1.0, 0.0, 1.0);
                    out = out.lengthSqr() < 1.0e-4 ? dir : out.normalize();
                    victim.push(out.x * 0.9, 0.55, out.z * 0.9);
                    victim.needsSync = true;
                    victim.igniteForSeconds(4.0f);
                }
            }
            this.snared = null;
        }
    }

    /**
     * Lavawoge: Sie reißt die Arme hoch und schlägt die Feuerkugel auf den Boden – ein Ring aus Glut rollt über das ganze
     * Heiligtum; wer am Boden steht, wenn er durchrollt, brennt und fliegt (darüber springen!). In der Phönixgestalt folgt
     * ein zweiter.
     */
    private void tickWave(ServerLevel level) {
        int t = this.attackTick;
        if (t == 6) {
            this.sound(level, ModSounds.IGNARA_CHARGE.get(), 2.0f, 0.9f);
        }
        Vec3 c = new Vec3(this.getX(), this.floorY(), this.getZ());
        if (t == WAVE_HIT || this.isPhoenix() && t == WAVE_HIT + 12) {
            this.sound(level, ModSounds.IGNARA_WAVE.get(), 3.5f, t == WAVE_HIT ? 0.9f : 1.05f);
            SpellFxEntity.create(level, SpellFxEntity.Kind.SHOCKWAVE, c, DragonType.FIRE, FLAME, WAVE_LIFE, WAVE_START).param(WAVE_SPEED).spawn();
            level.sendParticles(ParticleTypes.LAVA, c.x, c.y + 0.3, c.z, 18, 0.8, 0.1, 0.8, 0.0);
            level.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, 0xFFFF9A3A), c.x, c.y + 0.5, c.z, 1, 0.0, 0.0, 0.0, 0.0);
        }
        this.waveRing(level, c, t - WAVE_HIT, this.struck);
        if (this.isPhoenix()) {
            this.waveRing(level, c, t - WAVE_HIT - 12, this.struckSecond);
        }
    }

    private void waveRing(ServerLevel level, Vec3 c, int age, Set<UUID> hit) {
        if (age < 0 || age > WAVE_LIFE) {
            return;
        }
        double radius = WAVE_START + age * WAVE_SPEED;
        int points = 10 + (int) (radius * 5);
        for (int i = 0; i < points; i += 3) {
            double a = i * Math.PI * 2.0 / points + age * 0.1;
            level.sendParticles(ParticleTypes.FLAME, c.x + Math.cos(a) * radius, c.y + 0.25, c.z + Math.sin(a) * radius, 1, 0.05, 0.1, 0.05, 0.02);
        }
        for (LivingEntity victim : this.victims(level, new AABB(c, c).inflate(radius + 1.5, 3.0, radius + 1.5))) {
            double d = Math.hypot(victim.getX() - c.x, victim.getZ() - c.z);
            boolean grounded = victim.getY() - c.y < SpellFxEntity.SHOCKWAVE_HEIGHT;
            if (Math.abs(d - radius) > 0.95 || !grounded || !hit.add(victim.getUUID())) {
                continue;
            }
            if (victim.hurtServer(level, this.damageSources().mobAttack(this), WAVE_DAMAGE + (this.isPhoenix() ? 2.0f : 0.0f))) {
                Vec3 out = new Vec3(victim.getX() - c.x, 0.0, victim.getZ() - c.z);
                out = out.lengthSqr() < 1.0e-4 ? this.forward() : out.normalize();
                victim.push(out.x * 0.5, 0.7, out.z * 0.5);
                victim.needsSync = true;
                victim.igniteForSeconds(4.0f);
            }
        }
    }

    /**
     * Feuersäulen: Unter jedem Gegner (und daneben) glüht ein Kreis auf, der sich füllt – dann brechen Säulen aus Feuer hervor
     * und schleudern hoch, wer noch darin steht. In der Phönixgestalt folgt eine zweite Reihe, dort, wo die Gegner dann sind.
     */
    private void tickPillars(ServerLevel level, LivingEntity target) {
        int t = this.attackTick;
        if (t == 6 || this.isPhoenix() && t == 36) {
            this.sound(level, ModSounds.IGNARA_CHARGE.get(), 2.0f, 1.1f);
            this.pillars.clear();
            double floor = this.floorY();
            List<LivingEntity> marks = this.victims(level, this.getBoundingBox().inflate(24.0, 8.0, 24.0));
            marks.sort((a, b) -> Double.compare(a.distanceToSqr(this), b.distanceToSqr(this)));
            for (LivingEntity e : marks.subList(0, Math.min(3, marks.size()))) {
                this.pillars.add(new Vec3(e.getX(), floor, e.getZ()));
            }
            if (this.pillars.isEmpty()) {
                this.pillars.add(new Vec3(target.getX(), floor, target.getZ()));
            }
            // dazu zwei Säulen neben dem Hauptziel (dorthin, wohin man ausweichen möchte)
            Vec3 main = this.pillars.getFirst();
            for (int i = 0; i < 2; i++) {
                double a = this.random.nextDouble() * Math.PI * 2.0;
                this.pillars.add(main.add(Math.cos(a) * 3.2, 0.0, Math.sin(a) * 3.2));
            }
            for (Vec3 p : this.pillars) {
                SpellFxEntity.create(level, SpellFxEntity.Kind.CIRCLE, p.add(0.0, 0.05, 0.0), DragonType.FIRE, FLAME, 26, (float) PILLAR_RADIUS).spawn();
            }
        }
        if (t == PILLAR_ERUPT || this.isPhoenix() && t == PILLAR_ERUPT + 30) {
            this.sound(level, ModSounds.IGNARA_PILLAR.get(), 3.2f, 0.9f + this.random.nextFloat() * 0.2f);
            for (Vec3 p : this.pillars) {
                SpellFxEntity.create(level, SpellFxEntity.Kind.PILLAR, p, DragonType.FIRE, FLAME, 20, (float) PILLAR_RADIUS * 0.85f).param(6.0f).spawn();
                level.sendParticles(ParticleTypes.LAVA, p.x, p.y + 0.3, p.z, 8, 0.6, 0.1, 0.6, 0.0);
                for (LivingEntity victim : this.victims(level, new AABB(p, p).inflate(PILLAR_RADIUS + 0.6, 6.0, PILLAR_RADIUS + 0.6))) {
                    if (victim.distanceToSqr(p.x, victim.getY(), p.z) > (PILLAR_RADIUS + victim.getBbWidth() * 0.5) * (PILLAR_RADIUS + victim.getBbWidth() * 0.5)
                            || victim.getY() > p.y + 6.0) {
                        continue;
                    }
                    if (victim.hurtServer(level, this.damageSources().mobAttack(this), PILLAR_DAMAGE)) {
                        victim.igniteForSeconds(5.0f);
                        victim.push(0.0, 0.9, 0.0);
                        victim.needsSync = true;
                    }
                }
            }
        }
    }

    /**
     * Opalregen: Das Rad hinter ihren Schultern dreht sich schneller, sie reckt die Peitsche empor – und Splitter aus
     * Feueropal stürzen aus der Höhe auf ihre Gegner (dorthin, wo sie gleich sein werden) und zerspringen in Flammen.
     */
    private void tickOpalRain(ServerLevel level, LivingEntity target) {
        int t = this.attackTick;
        if (t == 6) {
            this.sound(level, ModSounds.IGNARA_CHARGE.get(), 2.0f, 1.3f);
        }
        int step = this.isPhoenix() ? 2 : 3;
        if (t < 20 || t > 56 || (t - 20) % step != 0) {
            return;
        }
        List<LivingEntity> marks = this.victims(level, this.getBoundingBox().inflate(22.0, 8.0, 22.0));
        LivingEntity aim = marks.isEmpty() ? target : marks.get(this.random.nextInt(marks.size()));
        Vec3 lead = aim.position().add(aim.getDeltaMovement().multiply(8.0, 0.0, 8.0))
                .add((this.random.nextDouble() - 0.5) * 3.0, 0.0, (this.random.nextDouble() - 0.5) * 3.0);
        Vec3 from = lead.add((this.random.nextDouble() - 0.5) * 2.0, 11.0 + this.random.nextDouble() * 3.0, (this.random.nextDouble() - 0.5) * 2.0);
        OpalShardEntity shard = new OpalShardEntity(level, this);
        shard.setDamage(SHARD_DAMAGE + (this.isPhoenix() ? 2.0f : 0.0f));
        shard.setPos(from.x, from.y, from.z);
        Vec3 v = lead.subtract(from).normalize().scale(1.05);
        shard.setDeltaMovement(v);
        level.addFreshEntity(shard);
        if ((t - 20) % (step * 3) == 0) {
            this.sound(level, ModSounds.IGNARA_SHARD.get(), 1.6f, 0.9f + this.random.nextFloat() * 0.3f);
        }
    }

    /** Ruf der Glutgeister: Sie reckt die Feuerkugel empor – und zwei Glutgeister treten aus den Flammen. */
    private void tickCall(ServerLevel level) {
        int t = this.attackTick;
        if (t == 4) {
            this.sound(level, ModSounds.IGNARA_CALL.get(), 3.0f, 1.0f);
        }
        if (t != 18) {
            return;
        }
        this.calls++;
        for (int i = 0; i < 2; i++) {
            Glutgeist spirit = ModEntities.BLAZE.get().create(level, EntitySpawnReason.MOB_SUMMONED);
            if (spirit == null) {
                continue;
            }
            float yaw = this.getYRot() * Mth.DEG_TO_RAD;
            double a = yaw + (i == 0 ? 1.0 : -1.0) * 1.6;
            double x = this.getX() - Math.sin(a) * 3.0;
            double z = this.getZ() + Math.cos(a) * 3.0;
            spirit.snapTo(x, this.floorY() + 1.2, z, this.getYRot(), 0.0f);
            spirit.finalizeSpawn(level, level.getCurrentDifficultyAt(spirit.blockPosition()), EntitySpawnReason.MOB_SUMMONED, null);
            spirit.setPersistenceRequired();
            if (this.getTarget() != null) {
                spirit.setTarget(this.getTarget());
            }
            level.addFreshEntity(spirit);
            level.sendParticles(ParticleTypes.FLAME, x, this.floorY() + 1.2, z, 50, 0.4, 0.9, 0.4, 0.05);
            level.sendParticles(ParticleTypes.LARGE_SMOKE, x, this.floorY() + 1.2, z, 12, 0.3, 0.6, 0.3, 0.02);
        }
    }

    /**
     * Phönixgestalt: Sie steigt auf, das Feuer sammelt sich um sie, dann brechen Schwingen aus Flammen aus ihrem Rücken – eine
     * Druckwelle aus Feuer wirft alle in der Nähe fort; danach sinkt sie zurück.
     */
    private void tickPhoenix(ServerLevel level) {
        int t = ++this.attackTick;
        double floor = this.floorY();
        double wantY = t < 26 ? floor + HOVER + t / 26.0 * 4.0 : floor + HOVER + Math.max(0.0, 4.0 - (t - 30) / 30.0 * 4.0);
        this.setDeltaMovement(0.0, Mth.clamp((wantY - this.getY()) * 0.25, -0.3, 0.3), 0.0);
        if (t == 2) {
            this.sound(level, ModSounds.IGNARA_CHARGE.get(), 3.0f, 0.7f);
        }
        if (t < 26 && t % 2 == 0) {
            for (int i = 0; i < 6; i++) {
                double a = this.random.nextDouble() * Math.PI * 2.0;
                double r = 3.0 + this.random.nextDouble() * 2.0;
                Vec3 p = this.position().add(Math.cos(a) * r, 1.0 + this.random.nextDouble() * 2.0, Math.sin(a) * r);
                Vec3 v = this.position().add(0.0, 1.5, 0.0).subtract(p).scale(0.12);
                level.sendParticles(ParticleTypes.FLAME, p.x, p.y, p.z, 0, v.x, v.y, v.z, 1.0);
            }
        }
        if (t == 26) {
            this.entityData.set(DATA_PHOENIX, true);
            this.sound(level, ModSounds.IGNARA_PHOENIX.get(), 4.0f, 1.0f);
            level.sendParticles(ParticleTypes.FLAME, this.getX(), this.getY(0.6), this.getZ(), 160, 1.6, 1.2, 1.6, 0.2);
            level.sendParticles(ParticleTypes.LAVA, this.getX(), this.getY(0.6), this.getZ(), 30, 1.4, 0.6, 1.4, 0.0);
            level.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, 0xFFFFE0A0), this.getX(), this.getY(0.6), this.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
            SpellFxEntity.create(level, SpellFxEntity.Kind.SHOCKWAVE, new Vec3(this.getX(), floor, this.getZ()), DragonType.FIRE, FLAME, 12, 1.0f).param(0.5f).spawn();
            for (LivingEntity victim : this.victims(level, this.getBoundingBox().inflate(6.0, 4.0, 6.0))) {
                if (victim.distanceToSqr(this) > 7.0 * 7.0) {
                    continue;
                }
                if (victim.hurtServer(level, this.damageSources().mobAttack(this), PHOENIX_BURST)) {
                    Vec3 out = victim.position().subtract(this.position()).multiply(1.0, 0.0, 1.0);
                    out = out.lengthSqr() < 1.0e-4 ? this.forward() : out.normalize();
                    victim.push(out.x * 1.2, 0.6, out.z * 1.2);
                    victim.needsSync = true;
                    victim.igniteForSeconds(6.0f);
                }
            }
        }
        if (t >= Attack.PHOENIX.duration) {
            this.begin(Attack.NONE);
            this.cooldown = 10;
        }
    }

    /**
     * Phönixsturz (nur in der Phönixgestalt): Sie steigt hoch über ihr Ziel, dann stößt sie in einer geraden Linie durch das
     * Heiligtum und zieht eine Spur aus Feuer; wen sie unterwegs streift, den trifft es schwer.
     */
    private void tickDive(ServerLevel level, LivingEntity target) {
        int t = this.attackTick;
        double floor = this.floorY();
        if (t <= 12) {
            Vec3 dir = this.toward(target);
            double wantY = floor + HOVER + t / 12.0 * 5.0;
            Vec3 back = dir.scale(-0.08);
            this.setDeltaMovement(back.x, Mth.clamp((wantY - this.getY()) * 0.3, -0.3, 0.45), back.z);
            if (t == 2) {
                this.sound(level, ModSounds.IGNARA_PHOENIX.get(), 2.5f, 1.25f);
            }
            if (t == 12) {
                Vec3 to = new Vec3(target.getX(), floor + HOVER, target.getZ());
                Vec3 flat = to.subtract(this.position()).multiply(1.0, 0.0, 1.0);
                Vec3 over = flat.lengthSqr() < 1.0e-4 ? this.forward() : flat.normalize();
                Vec3 end = to.add(over.scale(3.0));
                // nicht aus dem Heiligtum hinaus
                Vec3 c = this.center();
                Vec3 off = new Vec3(end.x - c.x, 0.0, end.z - c.z);
                if (off.length() > ARENA - 2.0) {
                    off = off.normalize().scale(ARENA - 2.0);
                    end = new Vec3(c.x + off.x, end.y, c.z + off.z);
                }
                this.diveFrom = this.position();
                this.diveTo = end;
                this.sound(level, ModSounds.IGNARA_DIVE.get(), 3.5f, 1.0f);
            }
            return;
        }
        if (t <= 26 && this.diveFrom != null && this.diveTo != null) {
            // waagerecht gleichmäßig beschleunigt und gebremst, senkrecht stürzt sie gleich zu Beginn hinab (sonst flöge sie über
            // ihr Ziel hinweg)
            double k = (t - 12) / 14.0;
            double s = k * k * (3.0 - 2.0 * k);
            double down = 1.0 - (1.0 - k) * (1.0 - k) * (1.0 - k);
            Vec3 at = new Vec3(Mth.lerp(s, this.diveFrom.x, this.diveTo.x), Mth.lerp(down, this.diveFrom.y, this.diveTo.y),
                    Mth.lerp(s, this.diveFrom.z, this.diveTo.z));
            this.setDeltaMovement(at.subtract(this.position()));
            level.sendParticles(ParticleTypes.FLAME, this.getX(), this.getY(0.5), this.getZ(), 10, 0.5, 0.5, 0.5, 0.04);
            if (t % 2 == 0) {
                SpellFxEntity.create(level, SpellFxEntity.Kind.PILLAR, new Vec3(this.getX(), floor, this.getZ()), DragonType.FIRE, FLAME, 14, 0.8f)
                        .param(2.5f).spawn();
            }
            for (LivingEntity victim : this.victims(level, this.getBoundingBox().inflate(1.2, 1.0, 1.2))) {
                if (this.struck.add(victim.getUUID()) && victim.hurtServer(level, this.damageSources().mobAttack(this), DIVE_DAMAGE)) {
                    Vec3 dir = this.diveTo.subtract(this.diveFrom).multiply(1.0, 0.0, 1.0);
                    dir = dir.lengthSqr() < 1.0e-4 ? this.forward() : dir.normalize();
                    victim.push(dir.x * 1.0, 0.5, dir.z * 1.0);
                    victim.needsSync = true;
                    victim.igniteForSeconds(6.0f);
                }
            }
            return;
        }
        if (t == 27) {
            this.diveFrom = null;
            this.diveTo = null;
            this.sound(level, ModSounds.IGNARA_WAVE.get(), 2.0f, 1.3f);
            level.sendParticles(ParticleTypes.LAVA, this.getX(), floor + 0.3, this.getZ(), 10, 0.6, 0.1, 0.6, 0.0);
        }
        this.hover(target, 0.3);
    }

    /** In der Phönixgestalt: wer ihr zu nahe kommt, verbrennt. */
    private void aura(ServerLevel level) {
        for (int i = 0; i < 16; i++) {
            double a = i * Math.PI * 2.0 / 16;
            level.sendParticles(ParticleTypes.FLAME, this.getX() + Math.cos(a) * 2.6, this.getY() + 0.4, this.getZ() + Math.sin(a) * 2.6, 1,
                    0.0, 0.1, 0.0, 0.01);
        }
        for (LivingEntity victim : this.victims(level, this.getBoundingBox().inflate(2.6, 1.5, 2.6))) {
            victim.hurtServer(level, this.damageSources().onFire(), 2.0f);
            victim.igniteForSeconds(2.0f);
        }
    }

    // ------------------------------------------------------------------ Tod, Speichern, Klänge

    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (this.level() instanceof ServerLevel level) {
            level.sendParticles(ParticleTypes.FLAME, this.getX(), this.getY(0.5), this.getZ(), 120, 1.0, 1.4, 1.0, 0.1);
            if (this.altar != null && level.getBlockEntity(this.altar) instanceof FlameAltarBlockEntity flame && flame.owns(this)) {
                flame.onPriestessDefeated(level);
            }
        }
    }

    /** Sie steigt auf, ihre Flammen verlöschen eine nach der anderen – dann zerfällt sie zu Glut und Asche. */
    @Override
    protected void tickDeath() {
        this.deathTime++;
        this.setDeltaMovement(0.0, this.deathTime < DEATH_TICKS - 20 ? 0.025 : 0.0, 0.0);
        if (!(this.level() instanceof ServerLevel level)) {
            return;
        }
        if (this.deathTime % 4 == 0) {
            level.sendParticles(ParticleTypes.FLAME, this.getX(), this.getY(0.6), this.getZ(), 8, 0.5, 0.9, 0.5, 0.03);
            level.sendParticles(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY(0.7), this.getZ(), 4, 0.4, 0.7, 0.4, 0.03);
        }
        if (this.deathTime == DEATH_TICKS / 2) {
            level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.FIRE_EXTINGUISH, SoundSource.HOSTILE, 3.0f, 0.6f);
        }
        if (this.deathTime == DEATH_TICKS - 2) {
            level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.FIRE_EXTINGUISH, SoundSource.HOSTILE, 3.0f, 0.4f);
            level.sendParticles(ParticleTypes.LAVA, this.getX(), this.getY(0.5), this.getZ(), 30, 0.8, 1.0, 0.8, 0.0);
            level.sendParticles(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY(0.5), this.getZ(), 60, 0.8, 1.2, 0.8, 0.05);
            level.sendParticles(ParticleTypes.WHITE_ASH, this.getX(), this.getY(0.5), this.getZ(), 120, 1.2, 1.4, 1.2, 0.05);
            level.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, 0xFFFFC070), this.getX(), this.getY(0.5), this.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
        }
        if (this.deathTime >= DEATH_TICKS && !this.isRemoved()) {
            this.bossBar.removeAllPlayers();
            this.remove(RemovalReason.KILLED);
        }
    }

    @Override
    public void remove(RemovalReason reason) {
        super.remove(reason);
        this.bossBar.removeAllPlayers();
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        if (this.home != null) {
            output.putLong("Home", this.home.asLong());
        }
        if (this.altar != null) {
            output.putLong("Altar", this.altar.asLong());
        }
        output.putBoolean("Phoenix", this.isPhoenix());
        output.putBoolean("Praying", this.attack == Attack.PRAY);
        output.putInt("Calls", this.calls);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        input.getLong("Home").ifPresent(h -> this.home = BlockPos.of(h));
        input.getLong("Altar").ifPresent(a -> this.altar = BlockPos.of(a));
        this.entityData.set(DATA_PHOENIX, input.getBooleanOr("Phoenix", false));
        this.calls = input.getIntOr("Calls", 0);
        if (input.getBooleanOr("Praying", false)) {
            this.begin(Attack.PRAY);
        }
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return this.attack == Attack.PRAY ? null : ModSounds.IGNARA_AMBIENT.get();
    }

    @Override
    public int getAmbientSoundInterval() {
        return 180;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.IGNARA_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.IGNARA_DEATH.get();
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        // sie schwebt: keine Schritte
    }

    @Override
    protected float getSoundVolume() {
        return 2.2f;
    }

    // ------------------------------------------------------------------ Himmelswacht

    @Override
    public io.github.sirschrolli.kingdomsatwar.entity.boss.sky.SkyTheme skyTheme() {
        return io.github.sirschrolli.kingdomsatwar.entity.boss.sky.SkyTheme.PRIEST;
    }
}
