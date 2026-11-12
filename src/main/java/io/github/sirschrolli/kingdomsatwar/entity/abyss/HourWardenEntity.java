package io.github.sirschrolli.kingdomsatwar.entity.abyss;

import io.github.sirschrolli.kingdomsatwar.block.abyss.ClockHeartBlockEntity;
import io.github.sirschrolli.kingdomsatwar.item.abyss.TimewardenArmor;
import io.github.sirschrolli.kingdomsatwar.magic.fx.SpellFxEntity;
import io.github.sirschrolli.kingdomsatwar.registry.ClockTower;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ColorParticleOption;
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
 * Der Stundenwächter (seit 1.01): Als die Welten zerbrachen, blieb in einer von ihnen die große Uhr stehen – eine Minute vor
 * Mitternacht. Ihr Turm stürzte in den Abgrund, wo die Zeit stillsteht, und mit ihm ihr Wächter: ein Ritter aus Uhrbronze, so
 * groß wie zwei Männer, hinter dem Kopf ein Zifferblatt wie ein Heiligenschein, in der Brust ein Uhrwerk, am Gürtel ein Pendel
 * und zwei Uhrgewichte, in den Händen zwei Klingen wie Zeiger – rechts der lange Minutenzeiger, links der breite Stundenzeiger.
 * Er steht erstarrt über dem Uhrwerkherz in der Mitte des großen Zifferblatts; wer näher als {@link #WAKE_RADIUS} Blöcke kommt
 * (oder ihn trifft), setzt die Zeiger in Gang – und kämpft gegen ihn:
 * <ul>
 *     <li><b>Zeigerhieb</b> – ein weiter Hieb mit dem Minutenzeiger (im Zorn schlägt der Stundenzeiger nach);</li>
 *     <li><b>Stundenschlag</b> – beide Zeiger über dem Kopf, dann ins Zifferblatt: ein Glockenschlag rollt als Ring davon;</li>
 *     <li><b>Zahnradwurf</b> – drei Zahnräder rollen über das Zifferblatt und prallen vom Rand ab;</li>
 *     <li><b>Zeitstopp</b> – er hebt die Hand, die Uhr tickt immer schneller … dann steht die Zeit still: Wer ihn in diesem
 *     Augenblick ansieht, erstarrt (wer wegschaut, nicht) – und er schlägt zu;</li>
 *     <li><b>Pendelschwung</b> – hoch über seinem Ziel erscheint ein riesiges Pendel und schwingt dreimal über den Boden;</li>
 *     <li><b>Sanduhr</b> – um seine Gegner rinnt Sand: Wer im Kreis bleibt, wird langsam, und ist die Zeit abgelaufen, birst er;</li>
 *     <li><b>Zeitsprung</b> – er vergeht in Bernsteinlicht und schlägt von hinten zu; wo er stand, hallt sein Schlag nach;</li>
 *     <li><b>Rückspulen</b> – angeschlagen kniet er nieder und dreht seine Zeit zurück (er heilt) – wer ihn dabei hart genug
 *     trifft, bringt sein Uhrwerk aus dem Takt, und er taumelt;</li>
 *     <li><b>Die Zwölfte Stunde</b> (im Zorn, unter halber Kraft) – die Zeiger springen auf zwölf, und die große Glocke schlägt
 *     zwölfmal: Jeder dritte Schlag rollt als Ring über das Zifferblatt (springen!).</li>
 * </ul>
 * Findet er niemanden mehr, kehrt er auf das Herz zurück und erstarrt wieder (geheilt). Besiegt sinkt er auf die Knie, sein
 * Uhrwerk läuft aus, die Zeiger fallen – dann zerspringt er in Zahnräder; das Herz bleibt stehen, und ein Chronit darin weckt
 * einen neuen Wächter.
 */
public class HourWardenEntity extends Monster implements io.github.sirschrolli.kingdomsatwar.entity.boss.sky.SkyWarden {
    public static final float MAX_HEALTH = 1800.0f;
    /** So lange (Ticks) dauert sein Sterben (er sinkt auf die Knie, das Uhrwerk läuft aus, er zerspringt). */
    public static final int DEATH_TICKS = 100;
    /** So nah (waagerecht) muss man ihm kommen, damit er erwacht. */
    public static final double WAKE_RADIUS = 13.0;
    /** Weiter als so weit von seinem Platz folgt er niemandem … */
    private static final double LEASH = 30.0;
    /** … und wer ihn vom Zifferblatt lockt, dem entkommt er mit einem Zeitsprung zurück auf die Nabe. */
    public static final double ARENA = 16.0;
    /** So lange ohne Gegner, dann erstarrt er wieder auf dem Herzen. */
    private static final int RETURN_TICKS = 400;
    public static final float SWEEP_DAMAGE = 16.0f;
    public static final double SWEEP_RADIUS = 5.6;
    public static final float STRIKE_DAMAGE = 20.0f;
    public static final double STRIKE_RADIUS = 3.2;
    public static final float WAVE_DAMAGE = 9.0f;
    public static final float GEAR_DAMAGE = 9.0f;
    /** Zeitstopp: so weit reicht sein Blick, so lange (Ticks) erstarrt, wer ihn ansieht (mit der Rüstung der Zeitwächter kürzer). */
    public static final double STOP_RANGE = 26.0;
    public static final int FREEZE_TICKS = 50;
    public static final int FREEZE_TICKS_WARDEN = 18;
    public static final float STOP_DAMAGE = 14.0f;
    public static final float PENDULUM_DAMAGE = 15.0f;
    /** Sanduhr: Halbmesser des Kreises, wie lange der Sand rinnt, Schaden, wenn er birst. */
    public static final double HOURGLASS_RADIUS = 3.2;
    public static final int HOURGLASS_TICKS = 100;
    public static final float HOURGLASS_DAMAGE = 12.0f;
    public static final float BLINK_DAMAGE = 12.0f;
    public static final float ECHO_DAMAGE = 10.0f;
    /** Rückspulen: so viel seiner Kraft holt er sich zurück, so viel Schaden bringt ihn aus dem Takt (Anteile seiner Lebenskraft). */
    public static final float REWIND_HEAL = 0.15f;
    public static final float REWIND_BREAK = 0.05f;
    private static final int REWIND_COOLDOWN = 900;
    private static final int MAX_REWINDS = 2;
    /** Die Zwölfte Stunde: Schaden eines Rings, Wachstum je Tick. */
    public static final float TOLL_DAMAGE = 9.0f;
    public static final float TOLL_SPEED = 0.6f;
    /** Bernstein: die Farbe seiner Zeit. */
    public static final int AMBER = 0xFFB347;

    public enum Attack {
        NONE(0), DORMANT(0), AWAKEN(72), SWEEP(28), STRIKE(44), GEARS(40), TIME_STOP(78), PENDULUM(92), HOURGLASS(44), BLINK(36),
        REWIND(84), STAGGER(56), FURY(30), TOLLS(128);

        public final int duration;

        Attack(int duration) {
            this.duration = duration;
        }
    }

    /** Zeitstopp: hier steht die Zeit still (Tick im Angriff). */
    public static final int STOP_AT = 34;
    /** Die Zwölfte Stunde: Abstand der Glockenschläge (Ticks). */
    public static final int TOLL_GAP = 9;

    private static final EntityDataAccessor<Integer> DATA_ATTACK = SynchedEntityData.defineId(HourWardenEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_ENRAGED = SynchedEntityData.defineId(HourWardenEntity.class,
            EntityDataSerializers.BOOLEAN);

    private final ServerBossEvent bossBar = new ServerBossEvent(UUID.randomUUID(), Component.translatable("boss.kingdomsatwar.hour_warden"),
            BossEvent.BossBarColor.YELLOW, BossEvent.BossBarOverlay.NOTCHED_12);
    private Attack attack = Attack.NONE;
    private Attack lastAttack = Attack.NONE;
    private int attackTick;
    private int cooldown = 30;
    private int idleTicks;
    private @Nullable BlockPos home;
    private @Nullable BlockPos heart;
    private final Set<UUID> struck = new HashSet<>();
    /** Zeitstopp: wer erstarrt ist – wo er steht und bis wann (Spielzeit). */
    private final Map<UUID, Vec3> frozenAt = new HashMap<>();
    private final Map<UUID, Long> frozenUntil = new HashMap<>();
    /** Sanduhren: Mitte und Ende (Spielzeit). */
    private final List<Hourglass> hourglasses = new ArrayList<>();
    /** Bebenringe des Stundenschlags und der Zwölften Stunde: Mitte, Beginn (Tick), Schaden und wen sie schon trafen. */
    private final List<Ring> rings = new ArrayList<>();
    private @Nullable Vec3 strikeMark;
    private @Nullable Vec3 echo;
    private int echoAt;
    private float rewindTaken;
    private int rewinds;
    private long lastRewind = Long.MIN_VALUE;
    private long lastStop = Long.MIN_VALUE;
    private long lastPendulum = Long.MIN_VALUE;
    private int clientAttackStart;
    /** Im Client: die Stellung des Minutenzeigers und der Zahnräder (Grad), damit sie stetig laufen. */
    private float clientMinute = -6.0f;
    private float clientMinuteO = -6.0f;
    private float clientGears;
    private float clientGearsO;

    /** Eine Sanduhr am Boden. */
    public record Hourglass(Vec3 center, long end) {}

    /** Ein Bebenring am Boden. */
    private record Ring(Vec3 center, int born, float damage, Set<UUID> hit) {}

    public HourWardenEntity(EntityType<? extends HourWardenEntity> type, Level level) {
        super(type, level);
        this.xpReward = 700;
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        // nach der Sternenweberin, wie der Zerbrochene König
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, MAX_HEALTH)
                .add(Attributes.ARMOR, 14.0)
                .add(Attributes.ARMOR_TOUGHNESS, 6.0)
                .add(Attributes.ATTACK_DAMAGE, SWEEP_DAMAGE)
                .add(Attributes.MOVEMENT_SPEED, 0.27)
                .add(Attributes.FOLLOW_RANGE, 48.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(Attributes.STEP_HEIGHT, 1.5);
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

    /** Unter halber Kraft (nach der Zwölften Stunde): schneller, härter. */
    public boolean isEnraged() {
        return this.entityData.get(DATA_ENRAGED);
    }

    /** Steht er erstarrt auf dem Herzen? */
    public boolean isDormant() {
        return this.currentAttack() == Attack.DORMANT;
    }

    /** Wie lange der laufende Angriff schon dauert (im Client, für die Animationen). */
    public float attackTime(float partialTicks) {
        return this.tickCount - this.clientAttackStart + partialTicks;
    }

    /** Im Client: Stellung des Minutenzeigers (Grad, 0 = zwölf Uhr, im Uhrzeigersinn). */
    public float minuteAngle(float partialTicks) {
        return Mth.lerp(partialTicks, this.clientMinuteO, this.clientMinute);
    }

    /** Im Client: wie weit sich die Zahnräder gedreht haben (Grad). */
    public float gearAngle(float partialTicks) {
        return Mth.lerp(partialTicks, this.clientGearsO, this.clientGears);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (DATA_ATTACK.equals(key)) {
            this.clientAttackStart = this.tickCount;
        }
    }

    /** Bindet ihn an seinen Platz über dem Herzen: Er steht erstarrt. */
    public void bindTo(BlockPos home, @Nullable BlockPos heart) {
        this.home = home.immutable();
        this.heart = heart == null ? null : heart.immutable();
        this.begin(Attack.DORMANT);
        this.snapTo(home.getX() + 0.5, home.getY(), home.getZ() + 0.5, 0.0f, 0.0f);
        this.yBodyRot = 0.0f;
        this.yHeadRot = 0.0f;
    }

    /** Aufgezogen (ein Chronit im stillstehenden Herzen): Er erwacht sofort. */
    public void rise(BlockPos home, @Nullable BlockPos heart) {
        this.home = home.immutable();
        this.heart = heart == null ? null : heart.immutable();
        this.snapTo(home.getX() + 0.5, home.getY(), home.getZ() + 0.5, 0.0f, 0.0f);
        this.begin(Attack.AWAKEN);
    }

    public @Nullable BlockPos home() {
        return this.home;
    }

    public @Nullable BlockPos heart() {
        return this.heart;
    }

    /** Er erwacht (wenn er erstarrt steht). */
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

    /** Ist dieses Wesen gerade in der Zeit erstarrt? */
    public boolean isFrozen(LivingEntity entity) {
        return this.frozenAt.containsKey(entity.getUUID());
    }

    /** Die Sanduhren, die gerade rinnen (für Tests). */
    public List<Hourglass> hourglasses() {
        return List.copyOf(this.hourglasses);
    }

    /** Wie oft er in diesem Kampf schon zurückgespult hat (für Tests). */
    public int rewinds() {
        return this.rewinds;
    }

    private void begin(Attack next) {
        Attack previous = this.attack;
        this.attack = next;
        this.attackTick = 0;
        this.entityData.set(DATA_ATTACK, next.ordinal());
        if (next != Attack.NONE && next != Attack.DORMANT && next != Attack.AWAKEN && next != Attack.FURY && next != Attack.TOLLS
                && next != Attack.STAGGER && next != Attack.REWIND) {
            this.lastAttack = next;
        }
        this.struck.clear();
        this.strikeMark = null;
        if (previous == Attack.REWIND || next == Attack.REWIND) {
            this.rewindTaken = 0.0f;
        }
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
    public boolean onClimbable() {
        return false;
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
     * Erstarrt weckt ihn ein Treffer (er ist dabei unverwundbar); beim Erwachen ist er unverwundbar; Stürze und Wände schaden ihm
     * nie. Beim Rückspulen zählt jeder Treffer – wer genug Schaden anrichtet, bringt ihn aus dem Takt; taumelnd nimmt er mehr.
     */
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
        if (this.attack == Attack.AWAKEN || source.is(DamageTypes.IN_WALL) || source.is(DamageTypes.FALL) || source.is(DamageTypes.DROWN)
                || source.is(DamageTypes.FELL_OUT_OF_WORLD)) {
            return false;
        }
        if (source.getEntity() == this || source.getDirectEntity() instanceof ClockGearEntity || source.getDirectEntity() instanceof ClockPendulumEntity) {
            return false;
        }
        float taken = this.attack == Attack.STAGGER ? amount * 1.25f : amount;
        float before = this.getHealth();
        boolean hurt = super.hurtServer(level, source, taken);
        if (hurt && this.attack == Attack.REWIND) {
            this.rewindTaken += Math.max(0.0f, before - this.getHealth());
            if (this.rewindTaken >= this.getMaxHealth() * REWIND_BREAK) {
                this.interruptRewind(level);
            }
        }
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
            this.clientClock();
            this.clientEffects();
        }
    }

    /**
     * Im Client: Zeiger und Zahnräder laufen stetig – erstarrt stehen sie (eine Minute vor zwölf), beim Erwachen springt der
     * Zeiger auf zwölf, beim Rückspulen rasen sie rückwärts, in der Zwölften Stunde stehen sie auf zwölf, im Sterben laufen sie
     * aus.
     */
    private void clientClock() {
        this.clientMinuteO = this.clientMinute;
        this.clientGearsO = this.clientGears;
        Attack a = this.currentAttack();
        float t = this.attackTime(0.0f);
        float gearSpeed;
        if (this.deathTime > 0) {
            float k = Math.max(0.0f, 1.0f - this.deathTime / (DEATH_TICKS * 0.5f));
            gearSpeed = 4.0f * k;
            this.clientMinute += 0.3f * k;
        } else {
            switch (a) {
                case DORMANT -> {
                    this.clientMinute = -6.0f;
                    gearSpeed = 0.0f;
                }
                case AWAKEN -> {
                    // die letzte Minute: erst Stille, dann springt der Zeiger auf zwölf
                    this.clientMinute = t < 34 ? -6.0f : t < 38 ? Mth.lerp((t - 34) / 4.0f, -6.0f, 0.0f) : (t - 38) * 0.6f;
                    gearSpeed = t < 20 ? t * 0.2f : 4.0f;
                }
                case REWIND -> {
                    this.clientMinute -= 14.0f;
                    gearSpeed = -16.0f;
                }
                case STAGGER -> {
                    this.clientMinute += (this.random.nextFloat() - 0.5f) * 8.0f;
                    gearSpeed = (this.random.nextFloat() - 0.4f) * 20.0f;
                }
                case FURY, TOLLS -> {
                    float target = Math.round(this.clientMinute / 360.0f) * 360.0f;
                    this.clientMinute += Mth.clamp(target - this.clientMinute, -20.0f, 20.0f);
                    gearSpeed = 9.0f;
                }
                case TIME_STOP -> {
                    if (t < STOP_AT) {
                        this.clientMinute += 0.6f + t * 0.12f;
                        gearSpeed = 4.0f + t * 0.3f;
                    } else {
                        gearSpeed = 0.0f;                                   // die Zeit steht
                    }
                }
                default -> {
                    this.clientMinute += this.isEnraged() ? 1.2f : 0.6f;
                    gearSpeed = this.isEnraged() ? 7.0f : 4.0f;
                }
            }
        }
        this.clientGears += gearSpeed;
    }

    /** Bernsteinstaub rieselt aus seinem Uhrwerk; im Zorn sprühen Funken; beim Rückspulen fließt Licht in ihn zurück. */
    private void clientEffects() {
        Level level = this.level();
        Attack a = this.currentAttack();
        if (a != Attack.DORMANT && this.random.nextInt(4) == 0) {
            level.addParticle(TimewardenArmor.AMBER, this.getRandomX(0.6), this.getY(0.45 + this.random.nextDouble() * 0.4), this.getRandomZ(0.6),
                    0.0, -0.02, 0.0);
        }
        if (this.isEnraged() && this.random.nextInt(3) == 0) {
            level.addParticle(ParticleTypes.WAX_ON, this.getRandomX(0.8), this.getY(0.85), this.getRandomZ(0.8), 0.0, 0.06, 0.0);
        }
        if (a == Attack.REWIND) {
            for (int i = 0; i < 2; i++) {
                double ang = this.random.nextDouble() * Mth.TWO_PI;
                double r = 3.0 + this.random.nextDouble() * 3.0;
                double x = this.getX() + Math.cos(ang) * r;
                double z = this.getZ() + Math.sin(ang) * r;
                double y = this.getY() + 0.5 + this.random.nextDouble() * 3.0;
                level.addParticle(ParticleTypes.REVERSE_PORTAL, x, y, z, (this.getX() - x) * 0.06, (this.getY(0.5) - y) * 0.06, (this.getZ() - z) * 0.06);
            }
        }
        if (a == Attack.DORMANT && this.random.nextInt(14) == 0) {
            level.addParticle(new DustParticleOptions(0x8A7A62, 0.7f), this.getRandomX(0.6), this.getY(0.3 + this.random.nextDouble() * 0.6),
                    this.getRandomZ(0.6), 0.0, -0.01, 0.0);
        }
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        this.bossBar.setProgress(this.getHealth() / this.getMaxHealth());
        this.tickFrozen(level);
        this.tickHourglasses(level);
        this.tickRings(level);
        this.tickEcho(level);
        if (this.attack == Attack.DORMANT) {
            this.tickDormant(level);
            return;
        }
        if (this.attack == Attack.AWAKEN) {
            this.tickAwaken(level);
            return;
        }
        if (this.tickCount % 10 == 0 && this.attack != Attack.BLINK && this.attack != Attack.TIME_STOP) {
            this.keepOnDial(level);
        }
        if (!this.isEnraged() && this.attack != Attack.FURY && this.attack != Attack.TOLLS && this.attack != Attack.REWIND
                && this.getHealth() < this.getMaxHealth() * 0.5f) {
            this.getNavigation().stop();
            this.begin(Attack.FURY);
        }
        if (this.attack == Attack.FURY || this.attack == Attack.TOLLS || this.attack == Attack.REWIND || this.attack == Attack.STAGGER) {
            this.attackTick++;
            this.getNavigation().stop();
            switch (this.attack) {
                case FURY -> this.tickFury(level);
                case TOLLS -> this.tickTolls(level);
                case REWIND -> this.tickRewind(level);
                default -> this.tickStagger(level);
            }
            if (this.attackTick >= this.attack.duration) {
                if (this.attack == Attack.FURY) {
                    this.begin(Attack.TOLLS);
                } else {
                    boolean staggered = this.attack == Attack.STAGGER;
                    this.begin(Attack.NONE);
                    this.cooldown = staggered ? 6 : 14;
                }
            }
            return;
        }
        LivingEntity target = this.findTarget(level);
        this.setTarget(target);
        if (target == null && this.attack != Attack.PENDULUM && this.attack != Attack.HOURGLASS) {
            this.idle(level);
            return;
        }
        this.idleTicks = 0;
        if (target != null) {
            this.getLookControl().setLookAt(target, 30.0f, 30.0f);
        }
        if (this.attack == Attack.NONE) {
            this.move(target);
            if (this.wantsRewind(level)) {
                this.getNavigation().stop();
                this.begin(Attack.REWIND);
                return;
            }
            if (--this.cooldown <= 0) {
                this.getNavigation().stop();
                this.begin(this.chooseAttack(level, target));
            }
            return;
        }
        this.attackTick++;
        this.getNavigation().stop();
        if (target != null && this.attack != Attack.BLINK && this.attack != Attack.TIME_STOP) {
            this.lookAt(target, 30.0f, 30.0f);
        }
        switch (this.attack) {
            case SWEEP -> this.tickSweep(level, target);
            case STRIKE -> this.tickStrike(level, target);
            case GEARS -> this.tickGears(level, target);
            case TIME_STOP -> this.tickTimeStop(level, target);
            case PENDULUM -> this.tickPendulum(level, target);
            case HOURGLASS -> this.tickHourglass(level);
            case BLINK -> this.tickBlink(level, target);
            default -> { }
        }
        if (this.attackTick >= this.attack.duration) {
            this.begin(Attack.NONE);
            this.cooldown = this.isEnraged() ? 10 + this.random.nextInt(10) : 16 + this.random.nextInt(16);
        }
    }

    /** Wen seine Angriffe treffen: alle Lebewesen außer ihm, anderen Wächtern und Spielern im Kreativ-/Zuschauermodus. */
    public boolean canStrike(LivingEntity entity) {
        return entity != this && entity.isAlive() && !(entity instanceof HourWardenEntity) && !(entity instanceof ArmorStand)
                && !(entity instanceof Player player && (player.isCreative() || player.isSpectator()));
    }

    List<LivingEntity> victims(ServerLevel level, AABB area) {
        return level.getEntitiesOfClass(LivingEntity.class, area, this::canStrike);
    }

    private double flatDistance(Entity other) {
        return Math.hypot(other.getX() - this.getX(), other.getZ() - this.getZ());
    }

    private @Nullable LivingEntity findTarget(ServerLevel level) {
        Vec3 center = this.center();
        if (this.home != null && this.distanceToSqr(center) > LEASH * LEASH) {
            return null;
        }
        LivingEntity current = this.getTarget();
        if (current != null && current.isAlive() && current.distanceToSqr(this) < 48.0 * 48.0 && this.canStrike(current)
                && (this.home == null || current.distanceToSqr(center) < LEASH * LEASH)) {
            return current;
        }
        return level.getNearestPlayer(center.x, center.y, center.z, 30.0, e -> e instanceof Player p && !p.isCreative() && !p.isSpectator()
                && p.isAlive());
    }

    /** Die Mitte seines Zifferblatts (sein Platz, sonst wo er steht). */
    public Vec3 center() {
        return this.home != null ? Vec3.atBottomCenterOf(this.home) : this.position();
    }

    private Vec3 forward() {
        float yaw = this.getYRot() * Mth.DEG_TO_RAD;
        return new Vec3(-Mth.sin(yaw), 0.0, Mth.cos(yaw));
    }

    private Vec3 toward(Entity target) {
        Vec3 dir = target.position().subtract(this.position()).multiply(1.0, 0.0, 1.0);
        return dir.lengthSqr() < 1.0e-4 ? this.forward() : dir.normalize();
    }

    /** Die Höhe des Bodens, auf dem er kämpft (sein Platz, sonst wo er steht). */
    double floorY() {
        return this.home != null ? this.home.getY() : this.getY();
    }

    /** Er schreitet heran (Abstand drei bis vier Blöcke). */
    private void move(@Nullable LivingEntity target) {
        if (target == null) {
            return;
        }
        double distance = this.flatDistance(target);
        if (distance > 3.6) {
            this.getNavigation().moveTo(target, this.isEnraged() ? 1.2 : 1.0);
        } else {
            this.getNavigation().stop();
        }
    }

    /** Fortgelockt (oder vom Zifferblatt gefallen): Ein Zeitsprung trägt ihn zurück auf die Nabe. */
    private void keepOnDial(ServerLevel level) {
        if (this.home == null) {
            return;
        }
        double dx = this.getX() - (this.home.getX() + 0.5);
        double dz = this.getZ() - (this.home.getZ() + 0.5);
        if (dx * dx + dz * dz < (ARENA + 2.0) * (ARENA + 2.0) && this.getY() > this.home.getY() - 3.0 && this.getY() < this.home.getY() + 12.0) {
            return;
        }
        this.vanish(level, this.position());
        this.getNavigation().stop();
        Vec3 back = Vec3.atBottomCenterOf(this.home);
        this.teleportTo(back.x, back.y, back.z);
        this.setDeltaMovement(Vec3.ZERO);
        this.appear(level, back);
    }

    /** Ohne Gegner: zurück auf die Nabe; nach einer Weile erstarrt er wieder – geheilt, ohne Zorn. */
    private void idle(ServerLevel level) {
        if (this.attack != Attack.NONE) {
            this.begin(Attack.NONE);
        }
        this.idleTicks++;
        if (this.home != null && this.distanceToSqr(Vec3.atBottomCenterOf(this.home)) > 2.0) {
            this.getNavigation().moveTo(this.home.getX() + 0.5, this.home.getY(), this.home.getZ() + 0.5, 0.9);
        } else if (this.home != null && this.idleTicks > RETURN_TICKS) {
            this.freeze(level);
            return;
        }
        if (this.idleTicks > 200 && this.tickCount % 20 == 0) {
            this.heal(12.0f);
        }
    }

    /** Er erstarrt wieder auf dem Herzen – ganz geheilt, nicht mehr im Zorn. */
    private void freeze(ServerLevel level) {
        this.getNavigation().stop();
        this.setHealth(this.getMaxHealth());
        this.entityData.set(DATA_ENRAGED, false);
        this.idleTicks = 0;
        this.rewinds = 0;
        this.sound(level, ModSounds.HOUR_WARDEN_TIME_STOP.get(), 1.5f, 0.7f);
        this.begin(Attack.DORMANT);
        if (this.home != null) {
            this.snapTo(this.home.getX() + 0.5, this.home.getY(), this.home.getZ() + 0.5, 0.0f, 0.0f);
            this.yBodyRot = 0.0f;
            this.yHeadRot = 0.0f;
        }
        this.setDeltaMovement(Vec3.ZERO);
    }

    private boolean wantsRewind(ServerLevel level) {
        return this.rewinds < MAX_REWINDS && this.getHealth() < this.getMaxHealth() * 0.65f
                && level.getGameTime() - this.lastRewind > REWIND_COOLDOWN && this.random.nextInt(40) == 0;
    }

    private Attack chooseAttack(ServerLevel level, @Nullable LivingEntity target) {
        if (target == null) {
            return Attack.HOURGLASS;
        }
        double distance = this.flatDistance(target);
        List<Attack> options = new ArrayList<>();
        if (distance < 5.2) {
            options.addAll(List.of(Attack.SWEEP, Attack.SWEEP, Attack.SWEEP, Attack.STRIKE, Attack.STRIKE, Attack.BLINK, Attack.HOURGLASS));
        } else if (distance < 11.0) {
            options.addAll(List.of(Attack.STRIKE, Attack.GEARS, Attack.GEARS, Attack.PENDULUM, Attack.HOURGLASS, Attack.BLINK, Attack.BLINK,
                    Attack.TIME_STOP));
        } else {
            options.addAll(List.of(Attack.GEARS, Attack.GEARS, Attack.PENDULUM, Attack.PENDULUM, Attack.TIME_STOP, Attack.BLINK, Attack.BLINK));
        }
        if (this.isEnraged()) {
            options.add(Attack.TIME_STOP);
            options.add(Attack.PENDULUM);
        }
        long now = level.getGameTime();
        if (now - this.lastStop < 400) {
            options.removeIf(a -> a == Attack.TIME_STOP);
        }
        if (now - this.lastPendulum < 200) {
            options.removeIf(a -> a == Attack.PENDULUM);
        }
        if (!this.hourglasses.isEmpty()) {
            options.removeIf(a -> a == Attack.HOURGLASS);
        }
        options.removeIf(a -> a == this.lastAttack);
        if (options.isEmpty()) {
            options.add(distance < 5.0 ? Attack.SWEEP : Attack.GEARS);
        }
        return options.get(this.random.nextInt(options.size()));
    }

    void sound(ServerLevel level, SoundEvent sound, float volume, float pitch) {
        level.playSound(null, this.getX(), this.getY(), this.getZ(), sound, SoundSource.HOSTILE, volume, pitch);
    }

    /** Ein Hauch Bernsteinstaub mit Funken an einer Stelle. */
    void amber(ServerLevel level, Vec3 at, int count, double spread) {
        level.sendParticles(TimewardenArmor.AMBER, at.x, at.y, at.z, count, spread, spread * 0.7, spread, 0.0);
        level.sendParticles(ParticleTypes.WAX_ON, at.x, at.y, at.z, Math.max(1, count / 4), spread, spread * 0.7, spread, 0.4);
    }

    /** Er vergeht in Bernsteinlicht … */
    private void vanish(ServerLevel level, Vec3 at) {
        this.sound(level, ModSounds.HOUR_WARDEN_BLINK.get(), 2.5f, 1.0f);
        this.amber(level, at.add(0.0, 2.0, 0.0), 40, 0.6);
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, at.x, at.y + 2.0, at.z, 40, 0.5, 1.2, 0.5, 0.1);
    }

    /** … und erscheint anderswo: ein Ring aus Bernstein wie ein Zifferblatt. */
    private void appear(ServerLevel level, Vec3 at) {
        for (int i = 0; i < 12; i++) {
            double a = i * Mth.TWO_PI / 12.0;
            level.sendParticles(TimewardenArmor.AMBER, at.x + Math.cos(a) * 1.6, at.y + 0.2, at.z + Math.sin(a) * 1.6, 3, 0.05, 0.05, 0.05, 0.0);
        }
        level.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, 0xFF000000 | AMBER), at.x, at.y + 2.0, at.z, 1, 0.0, 0.0, 0.0, 0.0);
        this.amber(level, at.add(0.0, 2.0, 0.0), 30, 0.5);
    }

    // ------------------------------------------------------------------ Erstarrt und Erwachen

    /** Er steht erstarrt auf dem Herzen: Wer näher als dreizehn Blöcke kommt, setzt seine Zeiger in Gang. */
    private void tickDormant(ServerLevel level) {
        this.getNavigation().stop();
        this.setDeltaMovement(0.0, Math.min(0.0, this.getDeltaMovement().y), 0.0);
        if (this.home != null && this.distanceToSqr(this.home.getX() + 0.5, this.home.getY(), this.home.getZ() + 0.5) > 0.25) {
            this.snapTo(this.home.getX() + 0.5, this.home.getY(), this.home.getZ() + 0.5, 0.0f, 0.0f);
        }
        this.setYRot(0.0f);
        this.yBodyRot = 0.0f;
        this.yHeadRot = 0.0f;
        this.setXRot(0.0f);
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
            if (dx * dx + dz * dz < WAKE_RADIUS * WAKE_RADIUS && dy > -3.0 && dy < 8.0) {
                this.awaken();
                return;
            }
        }
    }

    /** Das Uhrwerk zieht sich auf, die Augen glimmen auf, der Zeiger springt auf zwölf – die Glocke schlägt, und er hebt die Klingen. */
    private void tickAwaken(ServerLevel level) {
        int t = ++this.attackTick;
        this.getNavigation().stop();
        Player nearest = level.getNearestPlayer(this, 40.0);
        if (t == 2) {
            this.sound(level, ModSounds.HOUR_WARDEN_AWAKEN.get(), 3.5f, 1.0f);
            this.amber(level, this.position().add(0.0, 2.2, 0.0), 30, 0.8);
        }
        if (t == 36) {
            this.sound(level, ModSounds.HOUR_WARDEN_TOLL.get(), 4.0f, 1.0f);
            SpellFxEntity.create(level, SpellFxEntity.Kind.SHOCKWAVE, this.position().add(0.0, 0.05, 0.0), null, AMBER, 18, 1.2f).param(0.8f).spawn();
            this.amber(level, this.position().add(0.0, 3.4, 0.0), 50, 1.4);
        }
        if (nearest != null && t > 24) {
            this.getLookControl().setLookAt(nearest, 8.0f, 30.0f);
            if (t > 40) {
                this.lookAt(nearest, 6.0f, 30.0f);
            }
        }
        if (t == 42) {
            Component title = Component.translatable("boss.kingdomsatwar.hour_warden").withColor(0xFFC86A);
            Component subtitle = Component.translatable("boss.kingdomsatwar.hour_warden.title").withStyle(ChatFormatting.GRAY);
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
            this.cooldown = 12;
            this.idleTicks = 0;
        }
    }

    // ------------------------------------------------------------------ Angriffe

    /** Zeigerhieb: ein weiter Hieb mit dem Minutenzeiger vor ihm (im Zorn schlägt der Stundenzeiger nach). */
    private void tickSweep(ServerLevel level, @Nullable LivingEntity target) {
        int t = this.attackTick;
        if (t == 4) {
            this.sound(level, ModSounds.HOUR_WARDEN_STEP.get(), 1.5f, 0.8f);
        }
        if (t == 12) {
            this.slash(level, SWEEP_DAMAGE + (this.isEnraged() ? 3.0f : 0.0f), SWEEP_RADIUS, 75.0, 1.0f);
        }
        if (t == 21 && this.isEnraged()) {
            this.struck.clear();
            this.slash(level, SWEEP_DAMAGE * 0.65f, SWEEP_RADIUS - 1.0, 70.0, -1.0f);
        }
    }

    /** Ein Hieb im Bogen vor ihm: wer darin steht, wird getroffen und seitlich geschleudert ({@code side} = Richtung des Hiebs). */
    private void slash(ServerLevel level, float damage, double radius, double halfAngle, float side) {
        this.sound(level, ModSounds.HOUR_WARDEN_SWEEP.get(), 2.6f, side > 0 ? 1.0f : 1.2f);
        Vec3 fwd = this.forward();
        Vec3 right = new Vec3(-fwd.z, 0.0, fwd.x);
        for (int i = -6; i <= 6; i++) {
            double a = Math.toRadians(halfAngle * i / 6.0);
            Vec3 dir = fwd.scale(Math.cos(a)).add(right.scale(Math.sin(a) * side));
            Vec3 at = this.position().add(dir.scale(radius * 0.8)).add(0.0, 1.4 + i * 0.03 * side, 0.0);
            level.sendParticles(TimewardenArmor.AMBER, at.x, at.y, at.z, 2, 0.08, 0.08, 0.08, 0.0);
            if (i % 3 == 0) {
                level.sendParticles(ParticleTypes.SWEEP_ATTACK, at.x, at.y, at.z, 1, 0.0, 0.0, 0.0, 0.0);
            }
        }
        for (LivingEntity victim : this.victims(level, this.getBoundingBox().inflate(radius, 2.0, radius))) {
            Vec3 to = victim.position().subtract(this.position()).multiply(1.0, 0.0, 1.0);
            double d = to.length();
            if (d > radius + victim.getBbWidth() * 0.5 || victim.getY() > this.getY() + 4.0 || victim.getY() < this.getY() - 2.0) {
                continue;
            }
            double angle = d < 0.5 ? 0.0 : Math.toDegrees(Math.acos(Mth.clamp(to.normalize().dot(fwd), -1.0, 1.0)));
            if (angle > halfAngle + 10.0 || !this.struck.add(victim.getUUID())) {
                continue;
            }
            if (victim.hurtServer(level, this.damageSources().mobAttack(this), damage)) {
                Vec3 push = right.scale(side * 0.8).add(fwd.scale(0.5));
                victim.push(push.x, 0.35, push.z);
                victim.needsSync = true;
            }
        }
    }

    /** Stundenschlag: beide Zeiger über dem Kopf, dann ins Zifferblatt – ein Glockenschlag rollt als Ring davon. */
    private void tickStrike(ServerLevel level, @Nullable LivingEntity target) {
        int t = this.attackTick;
        if (t == 2) {
            Vec3 aim = target != null && this.flatDistance(target) < 6.5 ? target.position() : this.position().add(this.forward().scale(3.2));
            Vec3 from = this.position();
            Vec3 dir = aim.subtract(from).multiply(1.0, 0.0, 1.0);
            double d = Math.min(3.4, Math.max(2.4, dir.length()));
            Vec3 at = from.add(dir.lengthSqr() < 1.0e-4 ? this.forward().scale(d) : dir.normalize().scale(d));
            this.strikeMark = new Vec3(at.x, this.floorY() + 0.05, at.z);
            SpellFxEntity.create(level, SpellFxEntity.Kind.CIRCLE, this.strikeMark, null, AMBER, 20, (float) STRIKE_RADIUS).spawn();
            this.sound(level, ModSounds.HOUR_WARDEN_GEARS.get(), 1.4f, 0.6f);
        }
        if (this.strikeMark != null && t < 20) {
            this.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.FEET, this.strikeMark);
        }
        if (t == 20 && this.strikeMark != null) {
            Vec3 at = this.strikeMark;
            this.sound(level, ModSounds.HOUR_WARDEN_STRIKE.get(), 4.0f, 1.0f);
            level.sendParticles(ParticleTypes.EXPLOSION, at.x, at.y + 0.3, at.z, 2, 0.4, 0.1, 0.4, 0.0);
            this.amber(level, at.add(0.0, 0.4, 0.0), 40, 1.0);
            float damage = STRIKE_DAMAGE + (this.isEnraged() ? 3.0f : 0.0f);
            for (LivingEntity victim : this.victims(level, new AABB(at, at).inflate(STRIKE_RADIUS, 2.5, STRIKE_RADIUS))) {
                double d = Math.hypot(victim.getX() - at.x, victim.getZ() - at.z);
                if (d > STRIKE_RADIUS + victim.getBbWidth() * 0.5 || !this.struck.add(victim.getUUID())) {
                    continue;
                }
                if (victim.hurtServer(level, this.damageSources().mobAttack(this), damage)) {
                    Vec3 out = d < 1.0e-3 ? this.forward() : new Vec3(victim.getX() - at.x, 0.0, victim.getZ() - at.z).normalize();
                    victim.push(out.x * 0.6, 0.7, out.z * 0.6);
                    victim.needsSync = true;
                }
            }
            this.ring(level, at, 1.6f, WAVE_DAMAGE);
        }
    }

    /** Ein Bebenring vom Punkt {@code at} aus (Glockenschlag): wer nicht darüber springt, wird getroffen. */
    private void ring(ServerLevel level, Vec3 at, float start, float damage) {
        Vec3 c = new Vec3(at.x, this.floorY() + 0.05, at.z);
        SpellFxEntity.create(level, SpellFxEntity.Kind.SHOCKWAVE, c, null, AMBER, 26, start).param(TOLL_SPEED).spawn();
        this.rings.add(new Ring(c, this.tickCount - Math.round(start / TOLL_SPEED), damage, new HashSet<>()));
    }

    /** Die Bebenringe rollen: Wer am Boden steht, wenn der Ring vorbeirollt, wird getroffen. */
    private void tickRings(ServerLevel level) {
        Iterator<Ring> it = this.rings.iterator();
        while (it.hasNext()) {
            Ring ring = it.next();
            float r = (this.tickCount - ring.born()) * TOLL_SPEED;
            if (r > ARENA + 4.0f) {
                it.remove();
                continue;
            }
            for (LivingEntity victim : this.victims(level, new AABB(ring.center(), ring.center()).inflate(r + 1.5, 3.0, r + 1.5))) {
                double d = Math.hypot(victim.getX() - ring.center().x, victim.getZ() - ring.center().z);
                boolean grounded = victim.getY() - ring.center().y < SpellFxEntity.SHOCKWAVE_HEIGHT;
                if (Math.abs(d - r) > 0.9 || !grounded || ring.hit().contains(victim.getUUID())) {
                    continue;
                }
                ring.hit().add(victim.getUUID());
                if (victim.hurtServer(level, this.damageSources().indirectMagic(this, this), ring.damage())) {
                    Vec3 out = d < 1.0e-3 ? this.forward() : new Vec3(victim.getX() - ring.center().x, 0.0, victim.getZ() - ring.center().z).normalize();
                    victim.push(out.x * 0.5, 0.55, out.z * 0.5);
                    victim.needsSync = true;
                }
            }
        }
    }

    /** Zahnradwurf: Er greift hinter sich und schleudert drei Zahnräder, die über das Zifferblatt rollen. */
    private void tickGears(ServerLevel level, @Nullable LivingEntity target) {
        int t = this.attackTick;
        if (t == 6) {
            this.sound(level, ModSounds.HOUR_WARDEN_GEARS.get(), 2.5f, 1.0f);
        }
        if (t != 14 && t != 19 && t != 24) {
            return;
        }
        int k = (t - 14) / 5;
        Vec3 aim = target != null ? this.toward(target) : this.forward();
        double spread = this.isEnraged() ? 24.0 : 18.0;
        double a = Math.toRadians((k - 1) * spread);
        Vec3 dir = new Vec3(aim.x * Math.cos(a) - aim.z * Math.sin(a), 0.0, aim.x * Math.sin(a) + aim.z * Math.cos(a)).normalize();
        Vec3 from = this.position().add(dir.scale(1.6)).add(0.0, 0.1, 0.0);
        ClockGearEntity gear = new ClockGearEntity(ModEntities.CLOCK_GEAR.get(), level);
        gear.launch(this, from, dir.scale(this.isEnraged() ? 0.62 : 0.52), this.home, GEAR_DAMAGE + (this.isEnraged() ? 2.0f : 0.0f));
        level.addFreshEntity(gear);
        this.sound(level, ModSounds.HOUR_WARDEN_GEARS.get(), 1.8f, 1.2f + k * 0.1f);
        this.amber(level, from.add(0.0, 0.8, 0.0), 10, 0.3);
    }

    /**
     * Zeitstopp: Er hebt die Hand, die Uhr tickt immer schneller – dann steht die Zeit still: Wer ihn in diesem Augenblick ansieht
     * (und ihn sehen kann), erstarrt. Dann tritt er hinter einen Erstarrten und schlägt zu.
     */
    private void tickTimeStop(ServerLevel level, @Nullable LivingEntity target) {
        int t = this.attackTick;
        if (t == 2) {
            this.lastStop = level.getGameTime();
            for (ServerPlayer player : level.players()) {
                if (player.distanceToSqr(this) < (STOP_RANGE + 6.0) * (STOP_RANGE + 6.0) && !player.isSpectator()) {
                    player.sendOverlayMessage(Component.translatable("message.kingdomsatwar.hour_warden.stop_warning").withColor(0xFFC86A));
                }
            }
        }
        if (t < STOP_AT) {
            int gap = t < 12 ? 8 : t < 24 ? 4 : 2;
            if (t % gap == 0) {
                this.sound(level, ModSounds.HOUR_WARDEN_AMBIENT.get(), 1.6f, 1.0f + t * 0.02f);
            }
            if (t % 3 == 0) {
                this.amber(level, this.position().add(0.0, 3.6, 0.0), 6, 0.9);
            }
            if (target != null) {
                this.lookAt(target, 30.0f, 30.0f);
            }
            return;
        }
        if (t == STOP_AT) {
            this.sound(level, ModSounds.HOUR_WARDEN_TIME_STOP.get(), 4.0f, 1.0f);
            level.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, 0xFF000000 | AMBER), this.getX(), this.getY() + 3.4, this.getZ(), 1, 0.0,
                    0.0, 0.0, 0.0);
            SpellFxEntity.create(level, SpellFxEntity.Kind.SHOCKWAVE, this.position().add(0.0, 0.05, 0.0), null, AMBER, 30, 1.0f).param(1.0f).spawn();
            Vec3 eye = this.position().add(0.0, 3.0, 0.0);
            for (LivingEntity victim : this.victims(level, this.getBoundingBox().inflate(STOP_RANGE, 8.0, STOP_RANGE))) {
                if (victim.distanceTo(this) > STOP_RANGE) {
                    continue;
                }
                boolean caught;
                if (victim instanceof Player player) {
                    Vec3 look = player.getViewVector(1.0f);
                    Vec3 to = eye.subtract(player.getEyePosition()).normalize();
                    caught = look.dot(to) > Math.cos(Math.toRadians(55.0)) && player.hasLineOfSight(this);
                } else {
                    caught = victim.distanceTo(this) < 10.0;
                }
                if (caught) {
                    this.freezeEntity(level, victim);
                }
            }
            return;
        }
        // hinter einen Erstarrten treten und zuschlagen
        if (t == STOP_AT + 6) {
            LivingEntity frozen = this.frozenTarget(level, target);
            if (frozen != null) {
                Vec3 behind = frozen.position().subtract(frozen.getLookAngle().multiply(1.0, 0.0, 1.0).normalize().scale(2.4));
                if (this.canStand(level, behind)) {
                    this.vanish(level, this.position());
                    this.teleportTo(behind.x, this.floorY(), behind.z);
                    this.appear(level, this.position());
                    this.setTarget(frozen);
                }
            }
        }
        if (t > STOP_AT + 6 && target != null) {
            this.lookAt(target, 40.0f, 30.0f);
        }
        if (t == STOP_AT + 18) {
            LivingEntity frozen = this.frozenTarget(level, target);
            if (frozen != null && frozen.distanceTo(this) < 4.6) {
                this.sound(level, ModSounds.HOUR_WARDEN_SWEEP.get(), 2.6f, 0.8f);
                if (frozen.hurtServer(level, this.damageSources().mobAttack(this), STOP_DAMAGE + (this.isEnraged() ? 2.0f : 0.0f))) {
                    this.amber(level, frozen.position().add(0.0, 1.0, 0.0), 20, 0.4);
                }
            }
        }
    }

    private @Nullable LivingEntity frozenTarget(ServerLevel level, @Nullable LivingEntity target) {
        if (target != null && this.isFrozen(target)) {
            return target;
        }
        LivingEntity best = null;
        for (UUID id : this.frozenAt.keySet()) {
            if (level.getEntity(id) instanceof LivingEntity living && living.isAlive() && (best == null || living.distanceTo(this) < best.distanceTo(this))) {
                best = living;
            }
        }
        return best;
    }

    /** Steht dort (auf seinem Boden) genug Raum für ihn? */
    private boolean canStand(ServerLevel level, Vec3 at) {
        if (this.home != null && Math.hypot(at.x - (this.home.getX() + 0.5), at.z - (this.home.getZ() + 0.5)) > ARENA - 0.5) {
            return false;
        }
        AABB box = this.getDimensions(this.getPose()).makeBoundingBox(at.x, this.floorY(), at.z);
        return level.noCollision(this, box) && !level.getBlockState(BlockPos.containing(at.x, this.floorY() - 0.5, at.z)).isAir();
    }

    /** Die Zeit steht still um {@code victim} (mit dem vollen Satz der Zeitwächter nur kurz). Auch für Tests. */
    public void freezeEntity(ServerLevel level, LivingEntity victim) {
        int ticks = TimewardenArmor.fullSet(victim) ? FREEZE_TICKS_WARDEN : FREEZE_TICKS;
        this.frozenAt.put(victim.getUUID(), victim.position());
        this.frozenUntil.put(victim.getUUID(), level.getGameTime() + ticks);
        victim.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, ticks, 9, false, false, false), this);
        victim.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, ticks, 3, false, false, false), this);
        victim.setDeltaMovement(Vec3.ZERO);
        victim.needsSync = true;
        level.playSound(null, victim.getX(), victim.getY(), victim.getZ(), ModSounds.HOUR_WARDEN_FROZEN.get(), SoundSource.HOSTILE, 1.6f, 1.0f);
        if (victim instanceof ServerPlayer player) {
            player.sendOverlayMessage(Component.translatable("message.kingdomsatwar.hour_warden.frozen").withColor(0xFFC86A));
        }
    }

    /** Erstarrte bleiben, wo sie sind (wer sich doch bewegt, steht gleich wieder dort); ein Ring aus Bernstein umgibt sie. */
    private void tickFrozen(ServerLevel level) {
        if (this.frozenAt.isEmpty()) {
            return;
        }
        long now = level.getGameTime();
        Iterator<Map.Entry<UUID, Vec3>> it = this.frozenAt.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, Vec3> entry = it.next();
            Entity entity = level.getEntity(entry.getKey());
            Long until = this.frozenUntil.get(entry.getKey());
            if (!(entity instanceof LivingEntity living) || !living.isAlive() || until == null || now >= until || this.isDeadOrDying()) {
                if (entity instanceof LivingEntity living && living.isAlive()) {
                    living.removeEffect(MobEffects.SLOWNESS);
                    living.removeEffect(MobEffects.MINING_FATIGUE);
                }
                this.frozenUntil.remove(entry.getKey());
                it.remove();
                continue;
            }
            Vec3 at = entry.getValue();
            if (living.position().distanceToSqr(at) > 0.04) {
                if (living instanceof ServerPlayer player) {
                    player.teleportTo(level, at.x, at.y, at.z, Set.of(), player.getYRot(), player.getXRot(), false);
                } else {
                    living.teleportTo(at.x, at.y, at.z);
                }
            }
            living.setDeltaMovement(0.0, Math.min(0.0, living.getDeltaMovement().y), 0.0);
            if ((now + living.getId()) % 4 == 0) {
                double a = (now % 48) / 48.0 * Mth.TWO_PI;
                for (int i = 0; i < 4; i++) {
                    double b = a + i * Math.PI / 2.0;
                    level.sendParticles(TimewardenArmor.AMBER, at.x + Math.cos(b) * 0.8, at.y + 1.0, at.z + Math.sin(b) * 0.8, 1, 0.0, 0.0, 0.0, 0.0);
                }
            }
        }
    }

    /** Pendelschwung: Er weist nach oben – hoch über seinem Ziel erscheint ein Pendel und schwingt dreimal über den Boden. */
    private void tickPendulum(ServerLevel level, @Nullable LivingEntity target) {
        int t = this.attackTick;
        if (t == 8) {
            this.lastPendulum = level.getGameTime();
            Vec3 at = target != null ? target.position() : this.position().add(this.forward().scale(6.0));
            Vec3 dir = target != null ? this.toward(target) : this.forward();
            ClockPendulumEntity pendulum = new ClockPendulumEntity(ModEntities.CLOCK_PENDULUM.get(), level);
            pendulum.setup(this, new Vec3(at.x, this.floorY(), at.z), dir, PENDULUM_DAMAGE + (this.isEnraged() ? 3.0f : 0.0f));
            level.addFreshEntity(pendulum);
            this.sound(level, ModSounds.HOUR_WARDEN_PENDULUM.get(), 3.0f, 0.7f);
        }
    }

    /** Sanduhr: Er stößt den Stundenzeiger in den Boden – um jeden Gegner beginnt Sand zu rinnen. */
    private void tickHourglass(ServerLevel level) {
        int t = this.attackTick;
        if (t == 16) {
            this.sound(level, ModSounds.HOUR_WARDEN_HOURGLASS.get(), 3.0f, 1.0f);
            List<LivingEntity> foes = new ArrayList<>(this.victims(level, this.getBoundingBox().inflate(ARENA + 4.0, 6.0, ARENA + 4.0)));
            foes.removeIf(e -> !(e instanceof Player) && !(e instanceof net.minecraft.world.entity.Mob mob && mob.getTarget() == this));
            foes.sort((a, b) -> Double.compare(a.distanceToSqr(this), b.distanceToSqr(this)));
            int count = Math.min(this.isEnraged() ? 4 : 3, foes.size());
            List<Vec3> spots = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                spots.add(foes.get(i).position());
            }
            // nicht genug Gegner: weitere Sanduhren zwischen ihm und ihnen
            while (spots.size() < 2) {
                double a = this.random.nextDouble() * Mth.TWO_PI;
                double r = 4.0 + this.random.nextDouble() * 6.0;
                spots.add(this.center().add(Math.cos(a) * r, 0.0, Math.sin(a) * r));
            }
            for (Vec3 s : spots) {
                this.addHourglass(level, new Vec3(s.x, this.floorY() + 0.05, s.z));
            }
        }
    }

    /** Eine Sanduhr: Ein Kreis aus Bernstein füllt sich, Sand rinnt von oben; ist die Zeit abgelaufen, birst sie. Auch für Tests. */
    public void addHourglass(ServerLevel level, Vec3 at) {
        this.hourglasses.add(new Hourglass(at, level.getGameTime() + HOURGLASS_TICKS));
        SpellFxEntity.create(level, SpellFxEntity.Kind.CIRCLE, at, null, AMBER, HOURGLASS_TICKS, (float) HOURGLASS_RADIUS).spawn();
    }

    /** Die Sanduhren rinnen: Wer im Kreis ist, wird langsam; läuft sie ab, birst sie. */
    private void tickHourglasses(ServerLevel level) {
        if (this.hourglasses.isEmpty()) {
            return;
        }
        long now = level.getGameTime();
        BlockParticleOption sand = new BlockParticleOption(ParticleTypes.FALLING_DUST, net.minecraft.world.level.block.Blocks.SAND.defaultBlockState());
        Iterator<Hourglass> it = this.hourglasses.iterator();
        while (it.hasNext()) {
            Hourglass glass = it.next();
            Vec3 c = glass.center();
            AABB area = new AABB(c, c).inflate(HOURGLASS_RADIUS, 3.0, HOURGLASS_RADIUS);
            if (now % 2 == 0) {
                level.sendParticles(sand, c.x, c.y + 4.2, c.z, 3, 0.15, 0.05, 0.15, 0.0);
                double a = (now % 40) / 40.0 * Mth.TWO_PI;
                level.sendParticles(TimewardenArmor.AMBER, c.x + Math.cos(a) * HOURGLASS_RADIUS, c.y + 0.2, c.z + Math.sin(a) * HOURGLASS_RADIUS,
                        1, 0.0, 0.0, 0.0, 0.0);
            }
            for (LivingEntity victim : this.victims(level, area)) {
                if (Math.hypot(victim.getX() - c.x, victim.getZ() - c.z) <= HOURGLASS_RADIUS && now % 10 == 0) {
                    victim.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 20, 1, false, false, true), this);
                }
            }
            if (now < glass.end()) {
                continue;
            }
            it.remove();
            level.playSound(null, c.x, c.y, c.z, ModSounds.HOUR_WARDEN_HOURGLASS_BURST.get(), SoundSource.HOSTILE, 2.5f, 1.0f);
            level.sendParticles(sand, c.x, c.y + 0.5, c.z, 60, HOURGLASS_RADIUS * 0.5, 0.6, HOURGLASS_RADIUS * 0.5, 0.0);
            level.sendParticles(ParticleTypes.EXPLOSION, c.x, c.y + 0.6, c.z, 1, 0.0, 0.0, 0.0, 0.0);
            this.amber(level, c.add(0.0, 0.8, 0.0), 30, 1.2);
            for (LivingEntity victim : this.victims(level, area)) {
                if (Math.hypot(victim.getX() - c.x, victim.getZ() - c.z) <= HOURGLASS_RADIUS + victim.getBbWidth() * 0.5) {
                    if (victim.hurtServer(level, this.damageSources().indirectMagic(this, this), HOURGLASS_DAMAGE + (this.isEnraged() ? 2.0f : 0.0f))) {
                        victim.push(0.0, 0.5, 0.0);
                        victim.needsSync = true;
                    }
                }
            }
        }
    }

    /** Zeitsprung: Er vergeht in Bernsteinlicht und tritt hinter sein Ziel; wo er stand, hallt sein Schlag nach. */
    private void tickBlink(ServerLevel level, @Nullable LivingEntity target) {
        int t = this.attackTick;
        if (target == null) {
            return;
        }
        if (t == 6) {
            Vec3 back = target.getLookAngle().multiply(1.0, 0.0, 1.0);
            back = back.lengthSqr() < 1.0e-4 ? this.toward(target) : back.normalize();
            Vec3 behind = target.position().subtract(back.scale(2.6));
            if (!this.canStand(level, behind)) {
                behind = target.position().add(this.toward(target).scale(2.6));
            }
            if (this.canStand(level, behind)) {
                this.echo = this.position();
                this.echoAt = this.tickCount + 14;
                this.vanish(level, this.position());
                this.teleportTo(behind.x, this.floorY(), behind.z);
                this.appear(level, this.position());
            }
        }
        if (t > 6) {
            this.lookAt(target, 60.0f, 30.0f);
        }
        if (t == 15 && target.distanceTo(this) < 4.8) {
            this.sound(level, ModSounds.HOUR_WARDEN_SWEEP.get(), 2.6f, 1.15f);
            if (target.hurtServer(level, this.damageSources().mobAttack(this), BLINK_DAMAGE + (this.isEnraged() ? 2.0f : 0.0f))) {
                Vec3 out = this.toward(target);
                target.push(out.x * 0.7, 0.3, out.z * 0.7);
                target.needsSync = true;
            }
        }
    }

    /** Das Echo: wo er vor dem Zeitsprung stand, hallt sein Schlag nach (ein Kreis um die alte Stelle). */
    private void tickEcho(ServerLevel level) {
        if (this.echo == null) {
            return;
        }
        Vec3 at = this.echo;
        if (this.tickCount % 2 == 0) {
            level.sendParticles(TimewardenArmor.AMBER, at.x, at.y + 2.0, at.z, 6, 0.35, 1.2, 0.35, 0.0);
        }
        if (this.tickCount < this.echoAt) {
            return;
        }
        this.echo = null;
        level.playSound(null, at.x, at.y, at.z, ModSounds.HOUR_WARDEN_SWEEP.get(), SoundSource.HOSTILE, 2.0f, 0.7f);
        SpellFxEntity.create(level, SpellFxEntity.Kind.SHOCKWAVE, new Vec3(at.x, this.floorY() + 0.05, at.z), null, AMBER, 8, 0.6f).param(0.35f).spawn();
        for (LivingEntity victim : this.victims(level, new AABB(at, at).inflate(3.0, 3.0, 3.0))) {
            if (Math.hypot(victim.getX() - at.x, victim.getZ() - at.z) <= 3.0 + victim.getBbWidth() * 0.5) {
                victim.hurtServer(level, this.damageSources().indirectMagic(this, this), ECHO_DAMAGE);
            }
        }
    }

    /** Rückspulen: Er kniet nieder, seine Zeiger rasen rückwärts – er holt sich seine Kraft zurück, wenn ihn niemand stört. */
    private void tickRewind(ServerLevel level) {
        int t = this.attackTick;
        if (t == 1) {
            this.rewinds++;
            this.lastRewind = level.getGameTime();
            this.sound(level, ModSounds.HOUR_WARDEN_REWIND.get(), 3.5f, 1.0f);
            for (ServerPlayer player : level.players()) {
                if (player.distanceToSqr(this) < 40.0 * 40.0) {
                    player.sendOverlayMessage(Component.translatable("message.kingdomsatwar.hour_warden.rewind").withColor(0xFFC86A));
                }
            }
        }
        if (t >= 12 && t < 72) {
            this.heal(this.getMaxHealth() * REWIND_HEAL / 60.0f);
            if (t % 4 == 0) {
                this.amber(level, this.position().add(0.0, 2.0, 0.0), 6, 1.2);
            }
        }
        if (t == 40) {
            this.sound(level, ModSounds.HOUR_WARDEN_REWIND.get(), 2.5f, 1.2f);
        }
    }

    /** Wer ihn beim Rückspulen hart genug trifft, bringt sein Uhrwerk aus dem Takt: Er taumelt (und nimmt mehr Schaden). */
    private void interruptRewind(ServerLevel level) {
        this.begin(Attack.STAGGER);
        this.sound(level, ModSounds.HOUR_WARDEN_STAGGER.get(), 3.5f, 1.0f);
        level.sendParticles(ParticleTypes.CRIT, this.getX(), this.getY(0.6), this.getZ(), 40, 0.6, 0.8, 0.6, 0.4);
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, ClockTower.CLOCK_BRONZE.base().get().defaultBlockState()), this.getX(),
                this.getY(0.6), this.getZ(), 30, 0.6, 0.8, 0.6, 0.2);
        for (ServerPlayer player : level.players()) {
            if (player.distanceToSqr(this) < 40.0 * 40.0) {
                player.sendOverlayMessage(Component.translatable("message.kingdomsatwar.hour_warden.stagger").withColor(0xFFE7A0));
            }
        }
    }

    /** Er taumelt: Funken sprühen aus seinem Uhrwerk. */
    private void tickStagger(ServerLevel level) {
        if (this.attackTick % 5 == 0) {
            level.sendParticles(ParticleTypes.WAX_ON, this.getX(), this.getY(0.6), this.getZ(), 6, 0.5, 0.6, 0.5, 0.4);
            level.sendParticles(ParticleTypes.SMOKE, this.getX(), this.getY(0.7), this.getZ(), 4, 0.4, 0.4, 0.4, 0.02);
        }
    }

    /** Die Zwölfte Stunde (unter halber Kraft, einmal): Er hebt die Klingen, seine Zeiger springen auf zwölf. */
    private void tickFury(ServerLevel level) {
        int t = this.attackTick;
        if (t == 1) {
            this.entityData.set(DATA_ENRAGED, true);
            this.sound(level, ModSounds.HOUR_WARDEN_FURY.get(), 4.0f, 1.0f);
            for (ServerPlayer player : level.players()) {
                if (player.distanceToSqr(this) < 40.0 * 40.0) {
                    player.sendOverlayMessage(Component.translatable("message.kingdomsatwar.hour_warden.fury").withColor(0xFFC86A));
                }
            }
        }
        if (t == 14) {
            this.amber(level, this.position().add(0.0, 3.4, 0.0), 80, 1.6);
            level.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, 0xFF000000 | AMBER), this.getX(), this.getY() + 3.4, this.getZ(), 1, 0.0,
                    0.0, 0.0, 0.0);
        }
    }

    /** Die zwölf Glockenschläge: Jeder dritte (die Viertelstunden) rollt als Ring über das Zifferblatt, der zwölfte am stärksten. */
    private void tickTolls(ServerLevel level) {
        int t = this.attackTick - 6;
        if (t < 0 || t % TOLL_GAP != 0) {
            return;
        }
        int toll = t / TOLL_GAP + 1;
        if (toll > 12) {
            return;
        }
        this.sound(level, ModSounds.HOUR_WARDEN_TOLL.get(), 4.0f, toll == 12 ? 0.8f : 1.0f);
        this.amber(level, this.position().add(0.0, 3.6, 0.0), 12, 0.8);
        if (toll % 3 == 0) {
            this.ring(level, this.position(), 1.2f, toll == 12 ? TOLL_DAMAGE + 5.0f : TOLL_DAMAGE);
        }
    }

    // ------------------------------------------------------------------ Tod, Speichern, Klänge

    /** Löst alle Erstarrten und lässt Zahnräder und Pendel vergehen. */
    private void releaseAll(ServerLevel level) {
        for (UUID id : List.copyOf(this.frozenUntil.keySet())) {
            this.frozenUntil.put(id, 0L);
        }
        this.tickFrozen(level);
        this.hourglasses.clear();
        this.rings.clear();
        this.echo = null;
        for (ClockGearEntity gear : level.getEntitiesOfClass(ClockGearEntity.class, this.getBoundingBox().inflate(48.0), g -> g.owner() == this)) {
            gear.shatter(level);
        }
        for (ClockPendulumEntity pendulum : level.getEntitiesOfClass(ClockPendulumEntity.class, this.getBoundingBox().inflate(48.0, 40.0, 48.0),
                p -> p.owner() == this)) {
            pendulum.discard();
        }
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (this.level() instanceof ServerLevel level) {
            this.releaseAll(level);
            if (this.heart != null && level.getBlockEntity(this.heart) instanceof ClockHeartBlockEntity heartEntity && heartEntity.owns(this)) {
                heartEntity.onWardenDefeated(level);
            }
        }
    }

    /** Er sinkt auf die Knie (im Modell), sein Uhrwerk läuft aus, die Zeiger fallen – dann zerspringt er in Zahnräder. */
    @Override
    protected void tickDeath() {
        this.deathTime++;
        this.setDeltaMovement(0.0, Math.min(0.0, this.getDeltaMovement().y), 0.0);
        if (!(this.level() instanceof ServerLevel level)) {
            return;
        }
        if (this.deathTime % 5 == 0) {
            level.sendParticles(ParticleTypes.SMOKE, this.getX(), this.getY(0.6), this.getZ(), 4, 0.5, 0.6, 0.5, 0.02);
            if (this.deathTime < DEATH_TICKS / 2) {
                level.sendParticles(ParticleTypes.WAX_ON, this.getX(), this.getY(0.6), this.getZ(), 3, 0.5, 0.6, 0.5, 0.3);
            }
        }
        if (this.deathTime == DEATH_TICKS - 4) {
            this.sound(level, ModSounds.HOUR_WARDEN_TOLL.get(), 4.0f, 0.6f);
            BlockState bronze = ClockTower.CLOCK_BRONZE.base().get().defaultBlockState();
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, bronze), this.getX(), this.getY(0.5), this.getZ(), 160, 0.9, 1.4, 0.9, 0.3);
            this.amber(level, this.position().add(0.0, 2.0, 0.0), 120, 1.4);
            level.sendParticles(ParticleTypes.EXPLOSION, this.getX(), this.getY(0.5), this.getZ(), 3, 0.6, 0.8, 0.6, 0.0);
            level.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, 0xFF000000 | AMBER), this.getX(), this.getY() + 2.0, this.getZ(), 1, 0.0,
                    0.0, 0.0, 0.0);
        }
        if (this.deathTime >= DEATH_TICKS && !this.isRemoved()) {
            this.bossBar.removeAllPlayers();
            this.remove(RemovalReason.KILLED);
        }
    }

    @Override
    public void remove(RemovalReason reason) {
        if (this.level() instanceof ServerLevel level && reason != RemovalReason.UNLOADED_TO_CHUNK && reason != RemovalReason.UNLOADED_WITH_PLAYER) {
            this.releaseAll(level);
        }
        super.remove(reason);
        this.bossBar.removeAllPlayers();
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        if (this.home != null) {
            output.putLong("Home", this.home.asLong());
        }
        if (this.heart != null) {
            output.putLong("Heart", this.heart.asLong());
        }
        output.putBoolean("Enraged", this.isEnraged());
        output.putBoolean("Dormant", this.attack == Attack.DORMANT);
        output.putInt("Rewinds", this.rewinds);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        input.getLong("Home").ifPresent(h -> this.home = BlockPos.of(h));
        input.getLong("Heart").ifPresent(n -> this.heart = BlockPos.of(n));
        this.entityData.set(DATA_ENRAGED, input.getBooleanOr("Enraged", false));
        this.rewinds = input.getIntOr("Rewinds", 0);
        if (input.getBooleanOr("Dormant", false)) {
            this.begin(Attack.DORMANT);
        }
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return this.attack == Attack.DORMANT ? null : ModSounds.HOUR_WARDEN_AMBIENT.get();
    }

    @Override
    public int getAmbientSoundInterval() {
        return 120;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.HOUR_WARDEN_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.HOUR_WARDEN_DEATH.get();
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(ModSounds.HOUR_WARDEN_STEP.get(), 0.9f, 0.9f + this.random.nextFloat() * 0.2f);
    }

    @Override
    protected float getSoundVolume() {
        return 2.5f;
    }

    // ------------------------------------------------------------------ Himmelswacht

    @Override
    public io.github.sirschrolli.kingdomsatwar.entity.boss.sky.SkyTheme skyTheme() {
        return io.github.sirschrolli.kingdomsatwar.entity.boss.sky.SkyTheme.TIME;
    }
}
