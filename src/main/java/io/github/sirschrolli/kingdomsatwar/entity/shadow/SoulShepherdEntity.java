package io.github.sirschrolli.kingdomsatwar.entity.shadow;

import io.github.sirschrolli.kingdomsatwar.block.shadow.SoulBellBlockEntity;
import io.github.sirschrolli.kingdomsatwar.magic.fx.SpellFxEntity;
import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
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
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
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
 * Der Seelenhirte (seit 0.98): Im Seelensumpf der Schattenwelt hütet er im Seelenpferch die Seelen der Verlorenen – ein
 * hagerer Riese in zerschlissener Kutte, unter der Kapuze ein Widderschädel mit leuchtenden Augen, in der Hand ein
 * Hirtenstab, an dessen Haken eine Laterne voll Seelenfeuer hängt; Irrlichter kreisen um ihn. Er steht in Andacht neben
 * seiner Seelenglocke, bis jemand näher als {@link #WAKE_RADIUS} Blöcke kommt (oder ihn schlägt) – dann kämpft er:
 * <ul>
 *     <li><b>Hakenzug</b> – der Haken seines Stabs schnellt an einer Kette aus Seelenlicht vor und reißt sein Ziel zu ihm,
 *     dann schlägt er zu;</li>
 *     <li><b>Laternenschwung</b> – er schwingt die Laterne in weitem Bogen: Seelenfeuer, wer davor steht, brennt;</li>
 *     <li><b>Irrlichter</b> – er lässt seine Irrlichter frei; sie kreisen kurz und jagen dann ihrem Ziel nach;</li>
 *     <li><b>Seelenglocke</b> – er läutet die Handglocke in seiner Linken: ein Ring läuft über den Grund (wer springt, entgeht ihm),
 *     wer getroffen wird, ist langsam und schwach;</li>
 *     <li><b>Nebelgang</b> – er zerfließt zu Sumpfnebel und steht im nächsten Augenblick hinter seinem Ziel;</li>
 *     <li><b>Pferch</b> – ein Kreis aus Seelenfeuer schließt sein Ziel ein: wer hinaus will, verbrennt;</li>
 *     <li><b>Hirtenstoß</b> – der Stab fährt mit aller Wucht vor ihm in den Boden;</li>
 *     <li><b>Seelensturm</b> (im Zorn) – die Seelen wirbeln um ihn und ziehen alle in seine Nähe.</li>
 * </ul>
 * Unter halber Kraft läutet er dreimal (sein Zorn) – danach ist er schneller, und mehr Irrlichter folgen ihm. Findet er
 * niemanden mehr, kehrt er zu seiner Glocke zurück und versinkt wieder in Andacht (geheilt). Besiegt zerfällt er zu Nebel;
 * die Glocke verstummt, und ein Seelenkristall an ihr ruft ihn zurück.
 */
public class SoulShepherdEntity extends Monster implements io.github.sirschrolli.kingdomsatwar.entity.boss.sky.SkyWarden {
    public static final float MAX_HEALTH = 900.0f;
    /** So lange (Ticks) dauert sein Sterben (er sinkt zusammen und zerfällt zu Nebel). */
    public static final int DEATH_TICKS = 70;
    /** So nah (waagerecht) muss man ihm kommen, damit er erwacht. */
    public static final double WAKE_RADIUS = 12.0;
    /** Weiter als so weit von seinem Platz folgt er niemandem … */
    private static final double LEASH = 34.0;
    /** … und wer ihn weiter fortlockt, dem entkommt er im Nebel zurück zu seiner Glocke. */
    private static final double ARENA = 27.0;
    /** So lange ohne Gegner, dann versinkt er wieder in Andacht. */
    private static final int RETURN_TICKS = 400;
    /** Hakenzug: so weit reicht die Kette. */
    public static final double HOOK_RANGE = 18.0;
    /** Laternenschwung: Schaden und Reichweite. */
    public static final float LANTERN_DAMAGE = 10.0f;
    public static final double LANTERN_RANGE = 5.8;
    /** Irrlichter: Schaden je Licht. */
    public static final float WISP_DAMAGE = 6.0f;
    /** Seelenglocke: Schaden des Rings. */
    public static final float BELL_DAMAGE = 8.0f;
    /** Pferch: Halbmesser des Feuerkreises und wie lange er brennt. */
    public static final double PEN_RADIUS = 4.0;
    public static final int PEN_LIFE = 60;
    /** Hirtenstoß: Schaden. */
    public static final float CRASH_DAMAGE = 13.0f;
    /** Seelensturm: so weit zieht er. */
    public static final double STORM_RADIUS = 8.0;

    public enum Attack {
        NONE(0), DORMANT(0), AWAKEN(72), HOOK(42), LANTERN(30), WISPS(44), BELL(40), FOG(36), PEN(40), CRASH(32), STORM(72), FURY(56);

        public final int duration;

        Attack(int duration) {
            this.duration = duration;
        }
    }

    private static final EntityDataAccessor<Integer> DATA_ATTACK = SynchedEntityData.defineId(SoulShepherdEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_ENRAGED = SynchedEntityData.defineId(SoulShepherdEntity.class,
            EntityDataSerializers.BOOLEAN);

    private final ServerBossEvent bossBar = new ServerBossEvent(UUID.randomUUID(), Component.translatable("boss.kingdomsatwar.soul_shepherd"),
            BossEvent.BossBarColor.BLUE, BossEvent.BossBarOverlay.NOTCHED_10);
    private Attack attack = Attack.NONE;
    private Attack lastAttack = Attack.NONE;
    private int attackTick;
    private int cooldown = 30;
    private int idleTicks;
    private @Nullable BlockPos home;
    private @Nullable BlockPos lair;
    private final Set<UUID> struck = new HashSet<>();
    private @Nullable UUID hooked;
    private @Nullable Vec3 penCenter;
    private int penTicks;
    private int clientAttackStart;

    public SoulShepherdEntity(EntityType<? extends SoulShepherdEntity> type, Level level) {
        super(type, level);
        this.xpReward = 450;
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        // nach den Drachen, vor Asteria
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, MAX_HEALTH)
                .add(Attributes.ARMOR, 10.0)
                .add(Attributes.ARMOR_TOUGHNESS, 4.0)
                .add(Attributes.ATTACK_DAMAGE, 12.0)
                .add(Attributes.MOVEMENT_SPEED, 0.27)
                .add(Attributes.FOLLOW_RANGE, 44.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.95)
                .add(Attributes.STEP_HEIGHT, 1.25);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ATTACK, 0);
        builder.define(DATA_ENRAGED, false);
    }

    public Attack currentAttack() {
        int i = this.entityData.get(DATA_ATTACK);
        return i >= 0 && i < Attack.values().length ? Attack.values()[i] : Attack.NONE;
    }

    /** Unter halber Kraft: schneller, mehr Irrlichter, der Seelensturm. */
    public boolean isEnraged() {
        return this.entityData.get(DATA_ENRAGED);
    }

    /** Steht er in Andacht an seiner Glocke? */
    public boolean isDormant() {
        return this.currentAttack() == Attack.DORMANT;
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

    /** Bindet ihn an seinen Platz bei der Glocke: Er steht in Andacht. */
    public void bindTo(BlockPos home, @Nullable BlockPos lair) {
        this.home = home.immutable();
        this.lair = lair == null ? null : lair.immutable();
        this.begin(Attack.DORMANT);
    }

    /** Gerufen (ein Seelenkristall an der verstummten Glocke): sofort wach. */
    public void rise(BlockPos home, @Nullable BlockPos lair) {
        this.home = home.immutable();
        this.lair = lair == null ? null : lair.immutable();
        this.begin(Attack.AWAKEN);
    }

    public @Nullable BlockPos home() {
        return this.home;
    }

    public @Nullable BlockPos lair() {
        return this.lair;
    }

    /** Er erwacht (wenn er in Andacht steht). */
    public void awaken() {
        if (this.attack == Attack.DORMANT) {
            this.begin(Attack.AWAKEN);
        }
    }

    /** Beginnt sofort einen bestimmten Angriff (für Tests und die Vorführung). */
    public void forceAttack(Attack next) {
        this.getNavigation().stop();
        this.begin(next);
    }

    /** Sofort im Zorn (für Tests und die Vorführung). */
    public void forceEnraged() {
        this.entityData.set(DATA_ENRAGED, true);
    }

    /** Wo der Feuerkreis des Pferchs gerade brennt (oder null). */
    public @Nullable Vec3 penCenter() {
        return this.penTicks > 0 ? this.penCenter : null;
    }

    private void begin(Attack next) {
        if (this.attack == Attack.FOG && next != Attack.FOG) {
            this.setInvisible(false);
        }
        this.attack = next;
        this.attackTick = 0;
        this.entityData.set(DATA_ATTACK, next.ordinal());
        if (next != Attack.NONE && next != Attack.DORMANT && next != Attack.AWAKEN && next != Attack.FURY) {
            this.lastAttack = next;
        }
        this.struck.clear();
        this.hooked = null;
        this.bossBar.setVisible(next != Attack.DORMANT);
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

    /** In Andacht weckt ihn ein Schlag (unverwundbar); beim Erwachen und als Nebel ist er unverwundbar; Stürze und Wände schaden ihm nie. */
    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return super.hurtServer(level, source, amount);
        }
        if (this.attack == Attack.DORMANT) {
            if (source.getEntity() instanceof Player player && !player.isCreative() && !player.isSpectator()) {
                this.awaken();
            }
            return false;
        }
        if (this.attack == Attack.AWAKEN || (this.attack == Attack.FOG && this.isInvisible()) || source.is(DamageTypes.IN_WALL)
                || source.is(DamageTypes.FALL) || source.is(DamageTypes.DROWN)) {
            return false;
        }
        if (source.getDirectEntity() instanceof SoulWispEntity) {
            return false;
        }
        boolean hurt = super.hurtServer(level, source, amount);
        if (hurt && source.getEntity() instanceof LivingEntity attacker && attacker != this && this.canStrike(attacker)
                && !(this.getTarget() instanceof Player)) {
            this.setTarget(attacker);
        }
        return hurt;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide()) {
            this.clientEffects();
        }
    }

    /** Seelenfeuer flackert in der Laterne, Seelen steigen von ihm auf; in Andacht nur wenige, im Zorn viele. */
    private void clientEffects() {
        if (this.isInvisible()) {
            return;
        }
        Level level = this.level();
        Attack a = this.currentAttack();
        Vec3 lantern = this.lanternPos();
        if (this.random.nextInt(a == Attack.DORMANT ? 6 : 2) == 0) {
            level.addParticle(ParticleTypes.SOUL_FIRE_FLAME, lantern.x + (this.random.nextDouble() - 0.5) * 0.25, lantern.y - 0.1,
                    lantern.z + (this.random.nextDouble() - 0.5) * 0.25, 0.0, 0.015, 0.0);
        }
        int soul = a == Attack.DORMANT ? 24 : this.isEnraged() ? 3 : 8;
        if (this.random.nextInt(soul) == 0) {
            level.addParticle(ParticleTypes.SOUL, this.getRandomX(0.9), this.getY(0.2 + this.random.nextDouble() * 0.6), this.getRandomZ(0.9),
                    0.0, 0.04, 0.0);
        }
        if (this.walkAnimation.speed() > 0.2f && this.random.nextInt(3) == 0) {
            level.addParticle(new DustParticleOptions(0x6A6E78, 1.4f), this.getRandomX(0.8), this.getY() + 0.15, this.getRandomZ(0.8), 0.0, 0.0, 0.0);
        }
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        this.bossBar.setProgress(this.getHealth() / this.getMaxHealth());
        this.tickPen(level);
        if (this.attack == Attack.DORMANT) {
            this.tickDormant(level);
            return;
        }
        if (this.attack == Attack.AWAKEN) {
            this.tickAwaken(level);
            return;
        }
        if (this.tickCount % 10 == 0 && this.attack != Attack.FOG) {
            this.keepInPen(level);
        }
        if (!this.isEnraged() && this.attack != Attack.FURY && this.getHealth() < this.getMaxHealth() * 0.5f) {
            this.getNavigation().stop();
            this.begin(Attack.FURY);
        }
        if (this.attack == Attack.FURY) {
            this.attackTick++;
            this.getNavigation().stop();
            this.tickFury(level);
            if (this.attackTick >= Attack.FURY.duration) {
                this.begin(Attack.NONE);
                this.cooldown = 10;
            }
            return;
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
            double distance = this.flatDistance(target);
            if (distance > 3.6) {
                this.getNavigation().moveTo(target, this.isEnraged() ? 1.2 : 1.0);
            } else {
                this.getNavigation().stop();
            }
            if (--this.cooldown <= 0) {
                this.getNavigation().stop();
                this.begin(this.chooseAttack(target));
            }
            return;
        }
        this.attackTick++;
        this.getNavigation().stop();
        if (this.attack != Attack.FOG) {
            this.lookAt(target, 30.0f, 30.0f);
        }
        switch (this.attack) {
            case HOOK -> this.tickHook(level, target);
            case LANTERN -> this.tickLantern(level);
            case WISPS -> this.tickWisps(level, target);
            case BELL -> this.tickBell(level, BELL_DAMAGE, 14);
            case FOG -> this.tickFog(level, target);
            case PEN -> this.tickPenCast(level, target);
            case CRASH -> this.tickCrash(level);
            case STORM -> this.tickStorm(level);
            default -> { }
        }
        if (this.attackTick >= this.attack.duration) {
            this.begin(Attack.NONE);
            this.cooldown = this.isEnraged() ? 12 + this.random.nextInt(12) : 20 + this.random.nextInt(16);
        }
    }

    /** Wen seine Angriffe treffen: alle Lebewesen außer ihm, seinen Irrlichtern und Spielern im Kreativ-/Zuschauermodus. */
    public boolean canStrike(LivingEntity entity) {
        return entity != this && entity.isAlive() && !(entity instanceof SoulShepherdEntity) && !(entity instanceof ArmorStand)
                && !(entity instanceof Player player && (player.isCreative() || player.isSpectator()));
    }

    private List<LivingEntity> victims(ServerLevel level, AABB area) {
        return level.getEntitiesOfClass(LivingEntity.class, area, this::canStrike);
    }

    private double flatDistance(Entity other) {
        return Math.hypot(other.getX() - this.getX(), other.getZ() - this.getZ());
    }

    private @Nullable LivingEntity findTarget(ServerLevel level) {
        if (this.home != null && this.distanceToSqr(Vec3.atBottomCenterOf(this.home)) > LEASH * LEASH) {
            return null;
        }
        LivingEntity current = this.getTarget();
        if (current != null && current.isAlive() && current.distanceToSqr(this) < 44.0 * 44.0 && this.canStrike(current)
                && (this.home == null || current.distanceToSqr(Vec3.atBottomCenterOf(this.home)) < LEASH * LEASH)) {
            return current;
        }
        Vec3 from = this.home != null ? Vec3.atBottomCenterOf(this.home) : this.position();
        return level.getNearestPlayer(from.x, from.y, from.z, 30.0,
                e -> e instanceof Player p && !p.isCreative() && !p.isSpectator() && p.isAlive());
    }

    private Vec3 forward() {
        float yaw = this.getYRot() * Mth.DEG_TO_RAD;
        return new Vec3(-Mth.sin(yaw), 0.0, Mth.cos(yaw));
    }

    private Vec3 toward(Entity target) {
        Vec3 dir = target.position().subtract(this.position()).multiply(1.0, 0.0, 1.0);
        return dir.lengthSqr() < 1.0e-4 ? this.forward() : dir.normalize();
    }

    /** Wo die Laterne am Haken seines Stabs hängt (rechts vor ihm, in Kopfhöhe – wie im Modell). */
    public Vec3 lanternPos() {
        float yaw = this.yBodyRot * Mth.DEG_TO_RAD;
        Vec3 fwd = new Vec3(-Mth.sin(yaw), 0.0, Mth.cos(yaw));
        Vec3 right = new Vec3(-Mth.cos(yaw), 0.0, -Mth.sin(yaw));
        return this.position().add(fwd.scale(1.3)).add(right.scale(0.87)).add(0.0, 3.1, 0.0);
    }

    /** Fortgelockt: Er zerfließt zu Nebel und steht wieder an seiner Glocke. */
    private void keepInPen(ServerLevel level) {
        if (this.home == null) {
            return;
        }
        double dx = this.getX() - (this.home.getX() + 0.5);
        double dz = this.getZ() - (this.home.getZ() + 0.5);
        if (dx * dx + dz * dz < ARENA * ARENA && this.getY() > this.home.getY() - 8.0) {
            return;
        }
        this.fog(level, this.position(), 50);
        this.getNavigation().stop();
        this.teleportTo(this.home.getX() + 0.5, this.home.getY(), this.home.getZ() + 0.5);
        this.setDeltaMovement(Vec3.ZERO);
        this.fog(level, this.position(), 60);
        this.sound(level, ModSounds.SOUL_SHEPHERD_FOG.get(), 2.5f, 0.9f);
    }

    /** Ohne Gegner: zurück an seine Glocke; nach einer Weile versinkt er wieder in Andacht – geheilt, ohne Zorn. */
    private void idle(ServerLevel level) {
        if (this.attack != Attack.NONE) {
            this.begin(Attack.NONE);
        }
        this.idleTicks++;
        if (this.home != null && this.distanceToSqr(Vec3.atBottomCenterOf(this.home)) > 2.0) {
            this.getNavigation().moveTo(this.home.getX() + 0.5, this.home.getY(), this.home.getZ() + 0.5, 0.85);
        } else if (this.home != null && this.idleTicks > RETURN_TICKS) {
            this.fallAsleep(level);
            return;
        }
        if (this.idleTicks > 200 && this.tickCount % 20 == 0) {
            this.heal(8.0f);
        }
    }

    /** Er senkt den Kopf und steht wieder in Andacht – ganz geheilt, nicht mehr im Zorn. */
    private void fallAsleep(ServerLevel level) {
        this.getNavigation().stop();
        this.setHealth(this.getMaxHealth());
        this.entityData.set(DATA_ENRAGED, false);
        this.idleTicks = 0;
        if (this.home != null) {
            this.snapTo(this.home.getX() + 0.5, this.home.getY(), this.home.getZ() + 0.5, this.getYRot(), 0.0f);
        }
        this.faceLair();
        this.sound(level, ModSounds.SOUL_SHEPHERD_AMBIENT.get(), 2.0f, 0.7f);
        this.begin(Attack.DORMANT);
    }

    /** Er steht vor seiner Glocke und blickt über den Pferch (von der Glocke weg, zum Haupttor). */
    public void faceLair() {
        if (this.lair == null || this.home == null) {
            return;
        }
        double dx = this.home.getX() - this.lair.getX();
        double dz = this.home.getZ() - this.lair.getZ();
        if (dx * dx + dz * dz < 0.01) {
            return;
        }
        float yaw = (float) (Mth.atan2(dz, dx) * Mth.RAD_TO_DEG) - 90.0f;
        this.setYRot(yaw);
        this.yBodyRot = yaw;
        this.yHeadRot = yaw;
    }

    private Attack chooseAttack(LivingEntity target) {
        double distance = this.flatDistance(target);
        List<Attack> options = new ArrayList<>();
        if (distance < 4.5) {
            options.addAll(List.of(Attack.LANTERN, Attack.LANTERN, Attack.CRASH, Attack.CRASH, Attack.BELL, Attack.FOG));
        } else if (distance < 10.0) {
            options.addAll(List.of(Attack.HOOK, Attack.HOOK, Attack.WISPS, Attack.BELL, Attack.PEN, Attack.FOG, Attack.LANTERN));
        } else {
            options.addAll(List.of(Attack.HOOK, Attack.HOOK, Attack.WISPS, Attack.WISPS, Attack.PEN, Attack.FOG));
        }
        if (this.isEnraged()) {
            options.add(Attack.STORM);
            options.add(Attack.WISPS);
        }
        if (this.penTicks > 0) {
            options.removeIf(a -> a == Attack.PEN);
        }
        options.removeIf(a -> a == this.lastAttack);
        if (options.isEmpty()) {
            options.add(distance < 5.0 ? Attack.LANTERN : Attack.HOOK);
        }
        return options.get(this.random.nextInt(options.size()));
    }

    private void sound(ServerLevel level, SoundEvent sound, float volume, float pitch) {
        level.playSound(null, this.getX(), this.getY(), this.getZ(), sound, SoundSource.HOSTILE, volume, pitch);
    }

    /** Sumpfnebel und Seelen an einer Stelle. */
    private void fog(ServerLevel level, Vec3 at, int count) {
        level.sendParticles(ParticleTypes.POOF, at.x, at.y + 1.2, at.z, count / 3, 0.7, 1.0, 0.7, 0.02);
        level.sendParticles(ParticleTypes.SOUL, at.x, at.y + 1.4, at.z, count / 3, 0.8, 1.2, 0.8, 0.04);
        level.sendParticles(new DustParticleOptions(0x4FB8B0, 1.6f), at.x, at.y + 1.2, at.z, count / 2, 1.0, 1.2, 1.0, 0.0);
        level.sendParticles(new DustParticleOptions(0x8A8C96, 2.2f), at.x, at.y + 0.8, at.z, count / 3, 1.0, 0.8, 1.0, 0.0);
    }

    // ------------------------------------------------------------------ Andacht und Erwachen

    /** Er steht in Andacht an seiner Glocke: Wer näher als zwölf Blöcke tritt, weckt ihn. */
    private void tickDormant(ServerLevel level) {
        this.getNavigation().stop();
        this.setDeltaMovement(this.getDeltaMovement().multiply(0.0, 1.0, 0.0));
        if (this.tickCount % 10 != 0) {
            return;
        }
        for (ServerPlayer player : level.players()) {
            if (player.isCreative() || player.isSpectator() || !player.isAlive()) {
                continue;
            }
            double dx = player.getX() - this.getX();
            double dz = player.getZ() - this.getZ();
            double dy = player.getY() - this.getY();
            if (dx * dx + dz * dz < WAKE_RADIUS * WAKE_RADIUS && dy > -5.0 && dy < 9.0) {
                this.awaken();
                return;
            }
        }
    }

    /** Er hebt den Kopf, die Augen im Schädel glimmen auf, die Laterne lodert – und er läutet seine Glocke. */
    private void tickAwaken(ServerLevel level) {
        int t = ++this.attackTick;
        this.getNavigation().stop();
        Player nearest = level.getNearestPlayer(this, 32.0);
        if (nearest != null && t > 12) {
            this.getLookControl().setLookAt(nearest, 10.0f, 30.0f);
            if (t > 30) {
                this.lookAt(nearest, 8.0f, 30.0f);
            }
        }
        if (t == 2) {
            this.sound(level, ModSounds.SOUL_SHEPHERD_AWAKEN.get(), 3.0f, 1.0f);
        }
        if (t > 8 && t < 44 && t % 3 == 0) {
            Vec3 l = this.lanternPos();
            level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, l.x, l.y, l.z, 4, 0.2, 0.2, 0.2, 0.02);
            level.sendParticles(ParticleTypes.SOUL, this.getX(), this.getY(0.5), this.getZ(), 3, 0.9, 1.0, 0.9, 0.03);
        }
        if (t == 44) {
            this.sound(level, ModSounds.SOUL_SHEPHERD_BELL.get(), 4.0f, 0.85f);
            SpellFxEntity.create(level, SpellFxEntity.Kind.SHOCKWAVE, this.position().add(0.0, 0.05, 0.0), null, 0x6FE8D8, 14, 1.2f)
                    .param(0.6f).spawn();
            level.sendParticles(ParticleTypes.SOUL, this.getX(), this.getY(0.6), this.getZ(), 40, 1.2, 1.4, 1.2, 0.06);
            Component title = Component.translatable("boss.kingdomsatwar.soul_shepherd").withColor(0x7AE8DC);
            Component subtitle = Component.translatable("boss.kingdomsatwar.soul_shepherd.title").withStyle(ChatFormatting.GRAY);
            for (ServerPlayer player : level.players()) {
                if (player.distanceToSqr(this) < 64.0 * 64.0) {
                    player.connection.send(new ClientboundSetTitlesAnimationPacket(15, 60, 25));
                    player.connection.send(new ClientboundSetTitleTextPacket(title));
                    player.connection.send(new ClientboundSetSubtitleTextPacket(subtitle));
                }
            }
        }
        if (t >= Attack.AWAKEN.duration) {
            this.begin(Attack.NONE);
            this.cooldown = 14;
            this.idleTicks = 0;
        }
    }

    // ------------------------------------------------------------------ Angriffe

    /** Hakenzug: Der Haken schnellt an einer Kette aus Seelenlicht vor und reißt das Ziel heran – dann ein Schlag. */
    private void tickHook(ServerLevel level, LivingEntity target) {
        int t = this.attackTick;
        if (t == 4) {
            this.sound(level, ModSounds.SOUL_SHEPHERD_HOOK.get(), 2.5f, 1.0f);
        }
        if (t == 12 && target.distanceTo(this) < HOOK_RANGE && this.hasLineOfSight(target)) {
            Vec3 from = this.lanternPos().add(0.0, 0.4, 0.0);
            SpellFxEntity.create(level, SpellFxEntity.Kind.BEAM, from, null, 0x6FE8D8, 12, 0.18f).end(target.getEyePosition()).spawn();
            level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, target.getX(), target.getY(0.6), target.getZ(), 12, 0.3, 0.4, 0.3, 0.04);
            Vec3 pull = this.position().subtract(target.position()).multiply(1.0, 0.0, 1.0);
            double d = pull.length();
            if (d > 1.0e-3) {
                double k = Math.min(2.2, 0.6 + d * 0.11);
                target.setDeltaMovement(target.getDeltaMovement().scale(0.2).add(pull.scale(k / d)).add(0.0, 0.42, 0.0));
                target.needsSync = true;
            }
            target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 1), this);
            this.hooked = target.getUUID();
        }
        if (t == 24 && this.hooked != null && target.getUUID().equals(this.hooked) && target.distanceTo(this) < 4.2) {
            float damage = this.isEnraged() ? 11.0f : 9.0f;
            if (target.hurtServer(level, this.damageSources().mobAttack(this), damage)) {
                Vec3 out = this.toward(target);
                target.push(out.x * 0.6, 0.25, out.z * 0.6);
                target.needsSync = true;
            }
        }
    }

    /** Laternenschwung: Er schwingt die Laterne in weitem Bogen – Seelenfeuer, wer davor steht, brennt. */
    private void tickLantern(ServerLevel level) {
        int t = this.attackTick;
        if (t == 6) {
            this.sound(level, ModSounds.SOUL_SHEPHERD_LANTERN.get(), 2.5f, 1.0f);
        }
        if (t != 14) {
            return;
        }
        Vec3 dir = this.forward();
        for (int i = 0; i <= 18; i++) {
            Vec3 d = dir.yRot((float) Math.toRadians(-75.0 + i * (150.0 / 18.0)));
            for (double r = 2.0; r <= LANTERN_RANGE; r += 1.3) {
                Vec3 p = this.position().add(d.scale(r));
                level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, p.x, this.getY() + 1.0 + r * 0.1, p.z, 2, 0.15, 0.25, 0.15, 0.03);
            }
        }
        float damage = this.isEnraged() ? LANTERN_DAMAGE + 2.0f : LANTERN_DAMAGE;
        for (LivingEntity victim : this.victims(level, this.getBoundingBox().inflate(LANTERN_RANGE, 2.0, LANTERN_RANGE))) {
            Vec3 to = victim.position().subtract(this.position()).multiply(1.0, 0.0, 1.0);
            double d = to.length();
            if (d > LANTERN_RANGE || (d > 1.2 && to.normalize().dot(dir) < Math.cos(Math.toRadians(78.0)))) {
                continue;
            }
            if (victim.hurtServer(level, this.damageSources().mobAttack(this), damage)) {
                victim.igniteForSeconds(5.0f);
                Vec3 out = d < 1.0e-3 ? dir : to.normalize();
                victim.push(out.x * 0.9, 0.3, out.z * 0.9);
                victim.needsSync = true;
            }
        }
    }

    /** Irrlichter: Er lässt seine Irrlichter frei – sie kreisen kurz um ihn und jagen dann dem Ziel nach. */
    private void tickWisps(ServerLevel level, LivingEntity target) {
        int t = this.attackTick;
        if (t == 6) {
            this.sound(level, ModSounds.SOUL_SHEPHERD_WISPS.get(), 2.5f, 1.0f);
        }
        int every = this.isEnraged() ? 4 : 6;
        if (t < 12 || t > 32 || (t - 12) % every != 0) {
            return;
        }
        Vec3 from = this.lanternPos();
        double a = this.random.nextDouble() * Mth.TWO_PI;
        Vec3 out = new Vec3(Math.cos(a) * 0.35, 0.3 + this.random.nextDouble() * 0.15, Math.sin(a) * 0.35);
        SoulWispEntity wisp = new SoulWispEntity(level, this, out, target, this.isEnraged() ? WISP_DAMAGE + 1.0f : WISP_DAMAGE);
        wisp.setPos(from.x, from.y, from.z);
        level.addFreshEntity(wisp);
        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, from.x, from.y, from.z, 6, 0.15, 0.15, 0.15, 0.03);
    }

    /**
     * Seelenglocke: Er läutet die Handglocke in seiner Linken – ein Ring läuft über den Grund; wer darin steht (und nicht springt),
     * wird getroffen, ist langsam und schwach. Beginnt im Tick {@code start}.
     */
    private void tickBell(ServerLevel level, float damage, int start) {
        int t = this.attackTick;
        if (t == start) {
            this.sound(level, ModSounds.SOUL_SHEPHERD_BELL.get(), 4.0f, 0.95f);
            SpellFxEntity.create(level, SpellFxEntity.Kind.SHOCKWAVE, this.position().add(0.0, 0.05, 0.0), null, 0x6FE8D8, 16, 1.4f)
                    .param(0.72f).spawn();
            level.sendParticles(ParticleTypes.SOUL, this.getX(), this.getY(0.4), this.getZ(), 20, 0.6, 0.4, 0.6, 0.05);
            this.struck.clear();
        }
        if (t < start || t > start + 18) {
            return;
        }
        double radius = 1.4 + (t - start) * 0.72;
        for (LivingEntity victim : this.victims(level, this.getBoundingBox().inflate(radius + 1.0, 2.0, radius + 1.0))) {
            double d = this.flatDistance(victim);
            boolean grounded = victim.onGround() || victim.getY() - this.getY() < SpellFxEntity.SHOCKWAVE_HEIGHT;
            if (Math.abs(d - radius) > 0.95 || !grounded || !this.struck.add(victim.getUUID())) {
                continue;
            }
            if (victim.hurtServer(level, this.damageSources().indirectMagic(this, this), damage)) {
                victim.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 1), this);
                victim.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 80, 0), this);
                Vec3 out = new Vec3(victim.getX() - this.getX(), 0.0, victim.getZ() - this.getZ()).normalize();
                victim.push(out.x * 0.4, 0.45, out.z * 0.4);
                victim.needsSync = true;
            }
        }
    }

    /** Nebelgang: Er zerfließt zu Sumpfnebel und steht im nächsten Augenblick hinter seinem Ziel – und schlägt zu. */
    private void tickFog(ServerLevel level, LivingEntity target) {
        int t = this.attackTick;
        if (t == 4) {
            this.sound(level, ModSounds.SOUL_SHEPHERD_FOG.get(), 2.5f, 1.0f);
            this.fog(level, this.position(), 50);
        }
        if (t == 8) {
            this.setInvisible(true);
        }
        if (t == 20) {
            Vec3 look = target.getLookAngle().multiply(1.0, 0.0, 1.0);
            Vec3 behind = look.lengthSqr() < 1.0e-4 ? this.toward(target) : look.normalize();
            Vec3 at = target.position().subtract(behind.scale(2.4));
            AABB box = this.getDimensions(this.getPose()).makeBoundingBox(at);
            if (level.noCollision(this, box)) {
                this.teleportTo(at.x, at.y, at.z);
            }
            this.fog(level, this.position(), 60);
            this.setInvisible(false);
            this.lookAt(target, 180.0f, 30.0f);
            this.yBodyRot = this.getYRot();
        }
        if (t == 24 && target.distanceTo(this) < 4.0) {
            this.lookAt(target, 180.0f, 30.0f);
            if (target.hurtServer(level, this.damageSources().mobAttack(this), this.isEnraged() ? 11.0f : 9.0f)) {
                target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 30, 0), this);
                Vec3 out = this.toward(target);
                target.push(out.x * 0.7, 0.3, out.z * 0.7);
                target.needsSync = true;
            }
        }
    }

    /** Pferch: Ein Warnkreis um das Ziel, dann schließt ihn ein Kreis aus Seelenfeuer ein. */
    private void tickPenCast(ServerLevel level, LivingEntity target) {
        int t = this.attackTick;
        if (t == 4) {
            this.penCenter = target.position();
            this.sound(level, ModSounds.SOUL_SHEPHERD_PEN.get(), 3.0f, 0.9f);
            SpellFxEntity.create(level, SpellFxEntity.Kind.CIRCLE, this.penCenter.add(0.0, 0.05, 0.0), null, 0x6FE8D8, 18, (float) PEN_RADIUS)
                    .spawn();
        }
        if (t == 22 && this.penCenter != null) {
            this.penTicks = PEN_LIFE;
            int pillars = 14;
            for (int i = 0; i < pillars; i++) {
                double a = i * Mth.TWO_PI / pillars;
                Vec3 p = this.penCenter.add(Math.cos(a) * PEN_RADIUS, 0.0, Math.sin(a) * PEN_RADIUS);
                SpellFxEntity.create(level, SpellFxEntity.Kind.PILLAR, p, null, 0x5FE0E8, PEN_LIFE, 0.5f).param(2.6f).spawn();
            }
            level.playSound(null, this.penCenter.x, this.penCenter.y, this.penCenter.z, ModSounds.SOUL_SHEPHERD_PEN.get(), SoundSource.HOSTILE,
                    3.0f, 1.2f);
        }
    }

    /** Der Feuerkreis brennt: Wer ihn berührt, verbrennt und wird zurück in den Pferch gestoßen. */
    private void tickPen(ServerLevel level) {
        if (this.penTicks <= 0 || this.penCenter == null) {
            return;
        }
        this.penTicks--;
        if (this.penTicks % 4 != 0) {
            return;
        }
        Vec3 c = this.penCenter;
        for (LivingEntity victim : this.victims(level, new AABB(c, c).inflate(PEN_RADIUS + 1.5, 3.0, PEN_RADIUS + 1.5))) {
            double d = Math.hypot(victim.getX() - c.x, victim.getZ() - c.z);
            if (Math.abs(d - PEN_RADIUS) > 0.85 || victim.getY() > c.y + 2.8) {
                continue;
            }
            victim.hurtServer(level, this.damageSources().indirectMagic(this, this), 3.0f);
            victim.igniteForSeconds(3.0f);
            Vec3 in = new Vec3(c.x - victim.getX(), 0.0, c.z - victim.getZ());
            in = in.lengthSqr() < 1.0e-4 ? Vec3.ZERO : in.normalize();
            victim.push(in.x * 0.55, 0.15, in.z * 0.55);
            victim.needsSync = true;
        }
    }

    /** Hirtenstoß: Der Stab fährt mit aller Wucht vor ihm in den Boden. */
    private void tickCrash(ServerLevel level) {
        int t = this.attackTick;
        if (t == 6) {
            this.sound(level, ModSounds.SOUL_SHEPHERD_HOOK.get(), 1.6f, 0.6f);
        }
        if (t != 16) {
            return;
        }
        Vec3 dir = this.forward();
        Vec3 hit = this.position().add(dir.scale(2.4));
        this.sound(level, ModSounds.SOUL_SHEPHERD_CRASH.get(), 3.5f, 0.95f);
        SpellFxEntity.create(level, SpellFxEntity.Kind.SHOCKWAVE, new Vec3(hit.x, this.getY() + 0.05, hit.z), null, 0x8EF0E4, 8, 0.8f)
                .param(0.45f).spawn();
        SpellFxEntity.create(level, SpellFxEntity.Kind.FISSURE, hit.add(0.0, 0.05, 0.0), null, 0x6FE8D8, 30, 0.6f).param(0.0f)
                .end(hit.add(dir.scale(3.5)).add(0.0, 0.05, 0.0)).spawn();
        level.sendParticles(ParticleTypes.SOUL, hit.x, this.getY() + 0.3, hit.z, 24, 0.8, 0.2, 0.8, 0.06);
        level.sendParticles(ParticleTypes.POOF, hit.x, this.getY() + 0.3, hit.z, 12, 0.8, 0.2, 0.8, 0.03);
        float damage = this.isEnraged() ? CRASH_DAMAGE + 2.0f : CRASH_DAMAGE;
        for (LivingEntity victim : this.victims(level, new AABB(hit, hit).inflate(3.0, 2.5, 3.0))) {
            if (victim.distanceToSqr(hit.x, victim.getY(), hit.z) > 3.0 * 3.0 || victim.getY() < this.getY() - 1.5) {
                continue;
            }
            if (victim.hurtServer(level, this.damageSources().mobAttack(this), damage)) {
                Vec3 out = victim.position().subtract(this.position()).multiply(1.0, 0.0, 1.0);
                out = out.lengthSqr() < 1.0e-4 ? dir : out.normalize();
                victim.push(out.x * 0.7, 0.62, out.z * 0.7);
                victim.needsSync = true;
            }
        }
    }

    /** Seelensturm (im Zorn): Die Seelen wirbeln um ihn und ziehen alle in seine Nähe; wer ihm zu nahe ist, verdorrt. */
    private void tickStorm(ServerLevel level) {
        int t = this.attackTick;
        if (t == 6) {
            this.sound(level, ModSounds.SOUL_SHEPHERD_STORM.get(), 3.5f, 1.0f);
            SpellFxEntity.create(level, SpellFxEntity.Kind.VORTEX, this.position().add(0.0, 1.2, 0.0), null, 0x6FE8D8, 60, 3.2f).spawn();
        }
        if (t < 10 || t > 64) {
            return;
        }
        for (int i = 0; i < 4; i++) {
            double a = (t * 0.35 + i * Mth.HALF_PI);
            double r = 2.0 + (t % 20) * 0.25;
            level.sendParticles(ParticleTypes.SOUL, this.getX() + Math.cos(a) * r, this.getY() + 0.6 + i * 0.4, this.getZ() + Math.sin(a) * r, 1,
                    0.05, 0.05, 0.05, 0.01);
        }
        for (LivingEntity victim : this.victims(level, this.getBoundingBox().inflate(STORM_RADIUS, 3.0, STORM_RADIUS))) {
            double d = this.flatDistance(victim);
            if (d > STORM_RADIUS) {
                continue;
            }
            Vec3 in = new Vec3(this.getX() - victim.getX(), 0.0, this.getZ() - victim.getZ());
            if (in.lengthSqr() > 1.0e-4) {
                Vec3 swirl = in.normalize().yRot(Mth.HALF_PI).scale(0.035);
                victim.setDeltaMovement(victim.getDeltaMovement().add(in.normalize().scale(0.06)).add(swirl));
                victim.needsSync = true;
            }
            if (t % 10 == 0 && d < 3.5) {
                victim.hurtServer(level, this.damageSources().indirectMagic(this, this), 4.0f);
                victim.addEffect(new MobEffectInstance(MobEffects.WITHER, 60, 0), this);
            }
        }
    }

    /** Sein Zorn (unter halber Kraft, einmal): Er läutet dreimal – jeder Schlag ein Ring über den Grund. */
    private void tickFury(ServerLevel level) {
        int t = this.attackTick;
        if (t == 1) {
            this.entityData.set(DATA_ENRAGED, true);
            this.sound(level, ModSounds.SOUL_SHEPHERD_FURY.get(), 4.0f, 1.0f);
            for (ServerPlayer player : level.players()) {
                if (player.distanceToSqr(this) < 32.0 * 32.0) {
                    player.sendOverlayMessage(Component.translatable("message.kingdomsatwar.soul_shepherd.fury").withColor(0x7AE8DC));
                }
            }
        }
        for (int start : new int[] {10, 26, 42}) {
            if (t >= start && t <= start + 14) {
                this.tickBell(level, 6.0f, start);
            }
        }
    }

    // ------------------------------------------------------------------ Tod, Speichern, Klänge

    @Override
    public void die(DamageSource source) {
        this.setInvisible(false);
        super.die(source);
        if (this.level() instanceof ServerLevel level && this.lair != null && level.getBlockEntity(this.lair) instanceof SoulBellBlockEntity bell
                && bell.owns(this)) {
            bell.onShepherdDefeated(level);
        }
    }

    /** Er sinkt in sich zusammen, die Laterne erlischt – und er zerfällt zu Nebel; die Seelen steigen frei auf. */
    @Override
    protected void tickDeath() {
        this.deathTime++;
        this.setDeltaMovement(0.0, this.getDeltaMovement().y, 0.0);
        if (!(this.level() instanceof ServerLevel level)) {
            return;
        }
        if (this.deathTime % 4 == 0) {
            level.sendParticles(ParticleTypes.SOUL, this.getX(), this.getY(0.6), this.getZ(), 4, 0.6, 0.9, 0.6, 0.05);
        }
        if (this.deathTime == DEATH_TICKS / 2) {
            this.sound(level, ModSounds.SOUL_SHEPHERD_BELL.get(), 2.5f, 0.6f);
        }
        if (this.deathTime == DEATH_TICKS - 2) {
            this.sound(level, ModSounds.SOUL_SHEPHERD_DEATH.get(), 4.0f, 1.0f);
            this.fog(level, this.position(), 160);
            level.sendParticles(ParticleTypes.SOUL, this.getX(), this.getY(0.5), this.getZ(), 60, 1.0, 1.4, 1.0, 0.12);
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
        if (this.lair != null) {
            output.putLong("Lair", this.lair.asLong());
        }
        output.putBoolean("Enraged", this.isEnraged());
        output.putBoolean("Dormant", this.attack == Attack.DORMANT);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        input.getLong("Home").ifPresent(h -> this.home = BlockPos.of(h));
        input.getLong("Lair").ifPresent(l -> this.lair = BlockPos.of(l));
        this.entityData.set(DATA_ENRAGED, input.getBooleanOr("Enraged", false));
        this.setInvisible(false);
        if (input.getBooleanOr("Dormant", false)) {
            this.begin(Attack.DORMANT);
        }
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return this.attack == Attack.DORMANT ? null : ModSounds.SOUL_SHEPHERD_AMBIENT.get();
    }

    @Override
    public int getAmbientSoundInterval() {
        return 160;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.SOUL_SHEPHERD_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.SOUL_SHEPHERD_DEATH.get();
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(ModSounds.SOUL_SHEPHERD_STEP.get(), 0.8f, 0.9f + this.random.nextFloat() * 0.2f);
    }

    @Override
    protected float getSoundVolume() {
        return 2.5f;
    }

    // ------------------------------------------------------------------ Himmelswacht

    @Override
    public io.github.sirschrolli.kingdomsatwar.entity.boss.sky.SkyTheme skyTheme() {
        return io.github.sirschrolli.kingdomsatwar.entity.boss.sky.SkyTheme.ASH;
    }
}
