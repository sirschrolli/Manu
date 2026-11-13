package io.github.sirschrolli.kingdomsatwar.entity.sea;

import io.github.sirschrolli.kingdomsatwar.block.sea.KrakenCoreBlockEntity;
import io.github.sirschrolli.kingdomsatwar.magic.fx.SpellFxEntity;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.ModParticles;
import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
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
 * Der Kraken (seit 0.91): der Herr des Urmeers – stärker als Asteria, knapp schwächer als Anni der Troll. Er liegt im Schlund in der Mitte des Krakenschlunds und steigt empor,
 * sobald jemand den Felsenring betritt – dann ragen Kopf und Mantel turmhoch aus dem Wasser, ringsum wogen seine Arme.
 * Seit 1.00 <b>schwimmt</b> er durch sein Becken: schwer und träge zieht er Kreise um sein Ziel (in Reichweite seiner Arme),
 * nie über den Felsenring hinaus ({@link #SWIM_RADIUS} um die Mitte), und greift aus der Bewegung heraus an:
 * <ul>
 *     <li><b>Tentakelschlag:</b> ein Fangarm steigt hoch über ihn und schlägt dort nieder, wo eben noch ein Kreis aus Blasen
 *     aufstieg;</li>
 *     <li><b>Armfeger:</b> die Arme einer Seite fegen quer übers Wasser und schleudern davon, wer dort schwimmt oder steht;</li>
 *     <li><b>Würgegriff:</b> ein Fangarm packt sein Ziel, hält es fest und zieht es zum Schnabel – wer ihn in der Zeit hart
 *     genug trifft, kommt frei, sonst beißt er zu und schleudert die Beute fort;</li>
 *     <li><b>Tintenwolke:</b> er spuckt eine Wolke aus Tinte, die blendet und lähmt;</li>
 *     <li><b>Rammstoß</b> (seit 1.00): er saugt Wasser in den Mantel – Warnkreise zeigen die Bahn – und schießt mit einem
 *     Wasserstrahl quer durchs Becken; wer im Weg ist, wird überrollt und fortgeschleudert;</li>
 *     <li><b>Ausbruch aus der Tiefe</b> (seit 1.00): er taucht ab (unverwundbar), nur Blasen und ein dunkler Schatten verraten,
 *     wo er schwimmt; unter seinem Ziel steigt ein Ring aus Blasen auf – dann bricht er hoch aus dem Wasser und schleudert alles
 *     empor;</li>
 *     <li><b>Geysirfeld</b> (seit 1.00): er schlägt aufs Wasser, rings um seine Feinde brodelt es – und Geysire schießen empor
 *     (verletzt zweimal hintereinander);</li>
 *     <li><b>Tentakelkäfig</b> (seit 1.00, ab zwei Dritteln): seine Fangarme tauchen ab, und ringsum das Ziel brechen sieben
 *     Arme aus dem Wasser ({@link KrakenTentacleEntity}) – sie kreisen und ziehen sich zusammen; wer die Lücke nicht findet
 *     oder Arme abschlägt, den zermalmen sie zuletzt;</li>
 *     <li><b>Strudel</b> (ab zwei Dritteln): er dreht sich, das Wasser kreist und zieht alles zum Schlund, bis es in der
 *     Mitte hochschießt;</li>
 *     <li><b>Ruf der Tiefe</b> (bei zwei Dritteln und einem Drittel): er brüllt, und Haie kommen aus der Tiefe;</li>
 *     <li><b>Flutwelle</b> (in der Raserei, unter einem Drittel): alle Arme schlagen zugleich aufs Wasser, eine Welle läuft
 *     als Ring nach außen.</li>
 * </ul>
 * Ist niemand mehr im Ring, sinkt er nach einer halben Minute zurück in den Schlund. Besiegt sinkt er tot hinab; das Herz
 * des Schlunds merkt es sich, nur das Gezeitenhorn (mit einem Herz des Meeres) ruft ihn zurück.
 */
public class KrakenEntity extends Monster implements io.github.sirschrolli.kingdomsatwar.entity.boss.sky.SkyWarden {
    public static final byte EVENT_ROAR = 100;
    public static final byte EVENT_SLAM_LEFT = 101;
    public static final byte EVENT_SLAM_RIGHT = 102;
    public static final byte EVENT_SWEEP_LEFT = 103;
    public static final byte EVENT_SWEEP_RIGHT = 104;
    public static final byte EVENT_GRAB_LEFT = 105;
    public static final byte EVENT_GRAB_RIGHT = 106;
    public static final byte EVENT_INK = 107;
    public static final byte EVENT_WHIRL = 108;
    public static final byte EVENT_WAVE = 109;
    public static final byte EVENT_RELEASE = 110;
    public static final byte EVENT_CHARGE = 111;
    public static final byte EVENT_DIVE = 112;
    public static final byte EVENT_BREACH = 113;
    public static final byte EVENT_GEYSER = 114;
    public static final byte EVENT_CAGE = 115;

    private static final EntityDataAccessor<Float> DATA_SINK = SynchedEntityData.defineId(KrakenEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> DATA_ENRAGED = SynchedEntityData.defineId(KrakenEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> DATA_SPIN_SPEED = SynchedEntityData.defineId(KrakenEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> DATA_GRAB_SIDE = SynchedEntityData.defineId(KrakenEntity.class, EntityDataSerializers.INT);

    public static final double MAX_HEALTH = 1400.0;
    public static final int RISE_TICKS = 100;
    public static final int SLAM_TICKS = 52;
    public static final int SLAM_HIT = 26;
    public static final double SLAM_RADIUS = 3.8;
    /** So viele Ticks vor dem Einschlag steht das Ziel fest (und der Warnkreis füllt sich). */
    public static final int SLAM_WARN = 12;
    public static final int SWEEP_TICKS = 44;
    public static final int SWEEP_FROM = 12;
    public static final int SWEEP_TO = 24;
    public static final double SWEEP_REACH = 17.0;
    public static final int GRAB_TICKS = 72;
    public static final int GRAB_CATCH = 10;
    public static final int GRAB_BITE = 60;
    public static final double GRAB_REACH = 19.0;
    /** So viel Schaden muss er nehmen, während er jemanden hält, damit der freikommt. */
    public static final float GRAB_BREAK = 24.0f;
    public static final int INK_TICKS = 40;
    public static final int INK_SPIT = 18;
    public static final int WHIRL_TICKS = 130;
    public static final int WHIRL_FROM = 16;
    public static final int WHIRL_BURST = 112;
    public static final double WHIRL_RADIUS = 26.0;
    public static final int CALL_TICKS = 60;
    public static final int WAVE_TICKS = 60;
    public static final int WAVE_SLAM = 30;
    public static final double WAVE_REACH = 30.0;
    /** Rammstoß: Ausholen (Wasser in den Mantel), dann der Stoß, dann gleitet er aus. */
    public static final int CHARGE_TICKS = 66;
    public static final int CHARGE_WINDUP = 24;
    public static final int CHARGE_DASH = 16;
    /** So weit schießt er beim Rammstoß (Blöcke, höchstens bis an den Rand seines Beckens). */
    public static final double CHARGE_LENGTH = 16.0;
    /** So weit um seinen Leib trifft der Rammstoß. */
    public static final double CHARGE_HIT = 5.5;
    /** Ausbruch aus der Tiefe: abtauchen, unter Wasser zum Ziel, Blasenring als Warnung, Ausbruch, wieder aufrichten. */
    public static final int BREACH_TICKS = 112;
    public static final int BREACH_DIVE = 22;
    public static final int BREACH_WARN = 64;
    public static final int BREACH_ERUPT = 82;
    public static final double BREACH_RADIUS = 6.5;
    /** Geysirfeld: der Schlag aufs Wasser, dann brodelt es {@link #GEYSER_WARN} Ticks lang, bevor die Geysire aufsteigen. */
    public static final int GEYSER_TICKS = 88;
    public static final int GEYSER_FIRST = 14;
    public static final int GEYSER_SECOND = 46;
    public static final int GEYSER_WARN = 20;
    public static final double GEYSER_RADIUS = 2.4;
    /** Tentakelkäfig: wann die Arme ringsum aus dem Wasser brechen, wie weit vom Ziel. */
    public static final int CAGE_SPAWN = 12;
    public static final int CAGE_TICKS = CAGE_SPAWN + KrakenTentacleEntity.LIFE + 4;
    public static final double CAGE_RADIUS = 8.0;
    public static final int CAGE_ARMS = 8;
    /** So weit um die Mitte des Käfigs trifft das Zermalmen. */
    public static final double CAGE_CRUSH = 3.4;
    public static final int DEATH_TICKS = 110;
    /** So weit (waagerecht um die Mitte) reicht sein Ring: wer darin ist, ist sein Ziel. */
    public static final double ARENA_RADIUS = 34.0;
    /** So weit (waagerecht um die Mitte) schwimmt er umher – der Felsenring (ab gut 27 Blöcken) bleibt frei. */
    public static final double SWIM_RADIUS = 18.0;
    /** Wie schnell er schwimmt (Blöcke je Tick), in der Raserei schneller. */
    public static final double SWIM_SPEED = 0.16;
    public static final double SWIM_SPEED_ENRAGED = 0.22;

    public final AnimationState roarAnimation = new AnimationState();
    public final AnimationState slamLeftAnimation = new AnimationState();
    public final AnimationState slamRightAnimation = new AnimationState();
    public final AnimationState sweepLeftAnimation = new AnimationState();
    public final AnimationState sweepRightAnimation = new AnimationState();
    public final AnimationState grabAnimation = new AnimationState();
    public final AnimationState inkAnimation = new AnimationState();
    public final AnimationState whirlAnimation = new AnimationState();
    public final AnimationState waveAnimation = new AnimationState();
    public final AnimationState chargeAnimation = new AnimationState();
    public final AnimationState diveAnimation = new AnimationState();
    public final AnimationState breachAnimation = new AnimationState();
    public final AnimationState geyserAnimation = new AnimationState();
    public final AnimationState cageAnimation = new AnimationState();

    private final ServerBossEvent bossBar = new ServerBossEvent(UUID.randomUUID(), Component.translatable("boss.kingdomsatwar.kraken"),
            BossEvent.BossBarColor.BLUE, BossEvent.BossBarOverlay.NOTCHED_10);

    private enum Attack { NONE, RISE, SLAM, SWEEP, GRAB, INK, WHIRL, CALL, WAVE, CHARGE, BREACH, GEYSER, CAGE }

    /** Ein Ring der Flutwelle: wo er losging und wie alt er ist (er läuft weiter, auch wenn der Kraken weiterschwimmt). */
    private static final class Wave {
        final Vec3 center;
        int age;

        Wave(Vec3 center) {
            this.center = center;
        }
    }

    private Attack attack = Attack.NONE;
    private Attack last = Attack.NONE;
    private int attackTick;
    private int cooldown = 40;
    private boolean leftSide;
    private @Nullable Vec3 slamPoint;
    private @Nullable UUID grabbed;
    private float grabDamage;
    private final Set<UUID> hits = new HashSet<>();
    private final List<Wave> waves = new ArrayList<>();
    private final List<UUID> sharks = new ArrayList<>();
    private final List<UUID> tentacles = new ArrayList<>();
    private int calls;
    private boolean enraged;
    private int lonely;
    /** Angriffe seit dem letzten Ausbruch aus der Tiefe (er taucht nicht ständig ab). */
    private int sinceBreach = 3;
    private @Nullable BlockPos home;
    private @Nullable BlockPos core;
    private @Nullable LivingEntity debugTarget;
    private boolean debugHold;
    private boolean debugSwim;
    // Schwimmen (nur auf dem Server)
    private Vec3 swimVel = Vec3.ZERO;
    private @Nullable Vec3 swimGoal;
    private int swimGoalTicks;
    private int orbit = 1;
    private int strokeTicks;
    // Rammstoß, Ausbruch, Geysire, Käfig
    private Vec3 chargeDir = new Vec3(0.0, 0.0, 1.0);
    private double chargeLeft;
    private @Nullable Vec3 breachAt;
    private final List<Vec3> geysers = new ArrayList<>();
    private int geyserAt = -1;
    private @Nullable Vec3 cageCenter;
    // nur im Client
    private float sinkO;
    private float sinkC;
    private float spinO;
    private float spinC;
    private float swimO;
    private float swimC;

    public KrakenEntity(EntityType<? extends KrakenEntity> type, Level level) {
        super(type, level);
        this.xpReward = 550;
        this.setPersistenceRequired();
        this.setNoGravity(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        // knapp unter Anni dem Troll (1500 Lebenspunkte, Rüstung 16, Härte 6, Schaden 16)
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, MAX_HEALTH)
                .add(Attributes.ARMOR, 15.0)
                .add(Attributes.ARMOR_TOUGHNESS, 6.0)
                .add(Attributes.ATTACK_DAMAGE, 15.0)
                .add(Attributes.FOLLOW_RANGE, 64.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(Attributes.MOVEMENT_SPEED, 0.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_SINK, 0.0f);
        builder.define(DATA_ENRAGED, false);
        builder.define(DATA_SPIN_SPEED, 0.0f);
        builder.define(DATA_GRAB_SIDE, 0);
    }

    /** Verknüpft ihn mit seinem Schlund (Mitte an der Wasseroberfläche) und dem Herz des Schlunds darunter. */
    public void bindTo(BlockPos home, @Nullable BlockPos core) {
        this.home = home.immutable();
        this.core = core != null ? core.immutable() : null;
    }

    public @Nullable BlockPos home() {
        return this.home;
    }

    /** Wie tief er versunken ist: 0 aufgetaucht, 1 ganz unten; kurz unter 0, wenn er aus dem Wasser bricht. */
    public float sink() {
        return this.entityData.get(DATA_SINK);
    }

    public float sink(float partialTicks) {
        return Mth.lerp(partialTicks, this.sinkO, this.sinkC);
    }

    public float spin(float partialTicks) {
        return Mth.lerp(partialTicks, this.spinO, this.spinC);
    }

    /** Wie kräftig er gerade schwimmt (0 ruhig, 1 volle Fahrt; im Client aus seiner Bewegung). */
    public float swim(float partialTicks) {
        return Mth.lerp(partialTicks, this.swimO, this.swimC);
    }

    public boolean isEnraged() {
        return this.entityData.get(DATA_ENRAGED);
    }

    public int grabSide() {
        return this.entityData.get(DATA_GRAB_SIDE);
    }

    private void setSink(float sink) {
        this.entityData.set(DATA_SINK, Mth.clamp(sink, -0.35f, 1.0f));
    }

    // ------------------------------------------------------------------ Grundverhalten

    @Override
    public boolean isAffectedByFluids() {
        return false;
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    /** Abgetaucht schiebt sein (unsichtbarer) Leib niemanden an der Oberfläche fort. */
    @Override
    protected void pushEntities() {
        if (this.sink() < 0.5f) {
            super.pushEntities();
        }
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
    protected void checkFallDamage(double ya, boolean onGround, BlockState onState, BlockPos pos) {
    }

    @Override
    public boolean isPickable() {
        return this.sink() < 0.5f && super.isPickable();
    }

    /** Leib und Kopf zeigen stets in seine Blickrichtung (der Server dreht ihn: zum Ziel oder in Schwimmrichtung). */
    @Override
    protected void tickHeadTurn(float yBodyRotT) {
        this.yBodyRot = this.getYRot();
        this.yHeadRot = this.getYRot();
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

    @Override
    public void aiStep() {
        super.aiStep();
        this.setDeltaMovement(Vec3.ZERO);
        if (this.level().isClientSide()) {
            this.sinkO = this.sinkC;
            this.sinkC = this.sink();
            this.spinO = this.spinC;
            this.spinC += this.entityData.get(DATA_SPIN_SPEED);
            if (this.entityData.get(DATA_SPIN_SPEED) == 0.0f) {
                // zurück in die Blickrichtung
                float back = Mth.wrapDegrees(this.spinC);
                this.spinC -= back * 0.1f;
                this.spinO = this.spinC;
            }
            // wie kräftig er schwimmt: aus der Strecke dieses Ticks, weich nachgeführt
            double dx = this.getX() - this.xo;
            double dz = this.getZ() - this.zo;
            float speed = (float) Math.sqrt(dx * dx + dz * dz);
            this.swimO = this.swimC;
            this.swimC += (Mth.clamp(speed / (float) SWIM_SPEED, 0.0f, 1.5f) - this.swimC) * 0.12f;
            this.clientParticles();
        }
    }

    private void clientParticles() {
        Level level = this.level();
        float sink = this.sink();
        if (sink > 0.95f) {
            return;
        }
        // ringsum schäumt das Wasser um den Leib
        if (this.random.nextInt(2) == 0) {
            double a = this.random.nextDouble() * Math.PI * 2.0;
            double r = 5.5 + this.random.nextDouble() * 3.0;
            level.addParticle(ParticleTypes.SPLASH, this.getX() + Math.cos(a) * r, this.getY() + 0.9, this.getZ() + Math.sin(a) * r, 0.0, 0.1, 0.0);
        }
        if (this.random.nextInt(4) == 0 && sink < 0.5f) {
            level.addParticle(ModParticles.SEA_GLOW.get(), this.getRandomX(1.0), this.getY() + 2.0 + this.random.nextDouble() * 13.0,
                    this.getRandomZ(1.0), 0.0, 0.02, 0.0);
        }
        // schwimmt er, schiebt er eine Bugwelle vor sich her und zieht Kielwasser hinter sich
        if (this.swimC > 0.15f && sink < 0.5f) {
            Vec3 look = Vec3.directionFromRotation(0.0f, this.getYRot());
            Vec3 side = new Vec3(-look.z, 0.0, look.x);
            int n = 1 + (int) (this.swimC * 3.0f);
            for (int i = 0; i < n; i++) {
                double s = (this.random.nextDouble() - 0.5) * 9.0;
                Vec3 bow = this.position().add(look.scale(6.0 + this.random.nextDouble())).add(side.scale(s));
                level.addParticle(ParticleTypes.SPLASH, bow.x, this.getY() + 0.95, bow.z, look.x * 0.2, 0.15, look.z * 0.2);
                double w = (this.random.nextDouble() - 0.5) * 7.0;
                Vec3 wake = this.position().add(look.scale(-7.0 - this.random.nextDouble() * 6.0)).add(side.scale(w));
                level.addParticle(ParticleTypes.BUBBLE_POP, wake.x, this.getY() + 0.9, wake.z, 0.0, 0.02, 0.0);
                if (this.random.nextInt(3) == 0) {
                    level.addParticle(ParticleTypes.CLOUD, wake.x, this.getY() + 1.0, wake.z, -look.x * 0.05, 0.01, -look.z * 0.05);
                }
            }
        }
        if (this.entityData.get(DATA_SPIN_SPEED) != 0.0f) {
            for (int i = 0; i < 6; i++) {
                double a = this.random.nextDouble() * Math.PI * 2.0;
                double r = 6.0 + this.random.nextDouble() * (WHIRL_RADIUS - 6.0);
                double x = this.getX() + Math.cos(a) * r;
                double z = this.getZ() + Math.sin(a) * r;
                // die Strömung kreist und zieht nach innen
                double vx = -Math.sin(a) * 0.4 - Math.cos(a) * 0.15;
                double vz = Math.cos(a) * 0.4 - Math.sin(a) * 0.15;
                level.addParticle(ParticleTypes.BUBBLE_POP, x, this.getY() + 1.0, z, vx, 0.0, vz);
                level.addParticle(ParticleTypes.SPLASH, x, this.getY() + 1.0, z, vx, 0.05, vz);
            }
        }
    }

    // ------------------------------------------------------------------ Erwachen

    /** Lässt ihn emporsteigen: Er liegt noch tief im Schlund und bricht durch die Oberfläche. */
    public void beginRising(ServerLevel level) {
        this.attack = Attack.RISE;
        this.attackTick = 0;
        this.setSink(1.0f);
        this.swimVel = Vec3.ZERO;
        level.playSound(null, this.blockPosition(), ModSounds.KRAKEN_RISE.get(), SoundSource.HOSTILE, 5.0f, 0.8f);
    }

    public boolean isRising() {
        return this.attack == Attack.RISE;
    }

    private void tickRise(ServerLevel level) {
        int t = ++this.attackTick;
        float rise = Mth.clamp((t - 10) / 60.0f, 0.0f, 1.0f);
        this.setSink(1.0f - rise * rise * (3.0f - 2.0f * rise));
        if (t % 2 == 0 && t < 80) {
            level.sendParticles(ParticleTypes.BUBBLE_COLUMN_UP, this.getX(), this.getY() - 2.0, this.getZ(), 24, 5.0, 1.0, 5.0, 0.2);
            ring(level, this.position().add(0.0, 0.9, 0.0), 6.0 + t * 0.08, ParticleTypes.SPLASH, 32);
        }
        if (t == 10) {
            fx(level, SpellFxEntity.Kind.CAST_CIRCLE, this.position().add(0.0, 0.1, 0.0), 0x40B8FF, 70, 11.0f).param(0.9f).spawn();
        }
        if (t == 56) {
            // der Durchbruch: eine Wassersäule um den Leib
            fx(level, SpellFxEntity.Kind.PILLAR, this.position(), 0x60C8FF, 22, 7.0f).param(14.0f).spawn();
            level.sendParticles(ParticleTypes.SPLASH, this.getX(), this.getY() + 2.0, this.getZ(), 400, 6.0, 3.0, 6.0, 0.6);
        }
        if (t == 30) {
            level.playSound(null, this.blockPosition(), ModSounds.KRAKEN_WAVE.get(), SoundSource.HOSTILE, 4.0f, 0.7f);
            this.shake(level, 64.0, 0.45f, 30);
        }
        if (t == 72) {
            level.broadcastEntityEvent(this, EVENT_ROAR);
            level.playSound(null, this.blockPosition(), ModSounds.KRAKEN_ROAR.get(), SoundSource.HOSTILE, 5.0f, 0.8f);
            Component title = Component.translatable("boss.kingdomsatwar.kraken").withColor(0x5AD8E8);
            Component subtitle = Component.translatable("boss.kingdomsatwar.kraken.title").withStyle(net.minecraft.ChatFormatting.GRAY);
            for (ServerPlayer player : level.players()) {
                if (player.distanceToSqr(this) < 90.0 * 90.0) {
                    player.connection.send(new net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket(20, 70, 30));
                    player.connection.send(new net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket(title));
                    player.connection.send(new net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket(subtitle));
                }
            }
            this.shake(level, 70.0, 0.6f, 36);
        }
        if (t >= RISE_TICKS) {
            this.setSink(0.0f);
            this.attack = Attack.NONE;
            this.cooldown = 30;
        }
    }

    // ------------------------------------------------------------------ Denken

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        this.bossBar.setProgress(this.getHealth() / this.getMaxHealth());
        if (this.home == null) {
            this.home = this.blockPosition();
        }
        this.tickWaves(level);
        if (this.attack == Attack.RISE) {
            this.tickRise(level);
            return;
        }
        LivingEntity target = this.debugTarget != null && this.debugTarget.isAlive() ? this.debugTarget : this.pickTarget(level);
        this.setTarget(target);
        if (!this.enraged && this.healthFraction() < 0.34f) {
            this.enraged = true;
            this.entityData.set(DATA_ENRAGED, true);
        }
        if (this.attack != Attack.NONE) {
            this.lonely = 0;
            this.tickAttack(level, target);
            return;
        }
        if (target == null) {
            this.swim(level, null, this.debugHold && !this.debugSwim ? 0.0 : 0.5);
            this.faceSwim(null);
            if (++this.lonely > 600) {
                this.dive(level);
            }
            return;
        }
        this.lonely = 0;
        this.swim(level, target, this.debugHold && !this.debugSwim ? 0.0 : 1.0);
        this.faceSwim(target);
        if (this.debugHold) {
            return;
        }
        if (--this.cooldown <= 0) {
            this.chooseAttack(level, target);
        }
    }

    private void face(Vec3 at, float maxTurn) {
        double dx = at.x - this.getX();
        double dz = at.z - this.getZ();
        if (dx * dx + dz * dz > 1.0e-3) {
            float yaw = -((float) Mth.atan2(dx, dz)) * Mth.RAD_TO_DEG;
            this.setYRot(Mth.approachDegrees(this.getYRot(), yaw, maxTurn));
            this.yBodyRot = this.getYRot();
            this.yHeadRot = this.getYRot();
        }
    }

    /** In voller Fahrt blickt er dorthin, wohin er schwimmt; sonst zu seinem Ziel. */
    private void faceSwim(@Nullable LivingEntity target) {
        double speed = this.swimVel.horizontalDistance();
        if (speed > 0.05) {
            this.face(this.position().add(this.swimVel.scale(10.0 / speed)), 2.0f + (float) (speed * 10.0));
        } else if (target != null) {
            this.face(target.position(), 4.0f);
        }
    }

    private @Nullable LivingEntity pickTarget(ServerLevel level) {
        BlockPos h = this.home;
        if (h == null) {
            return null;
        }
        AABB arena = new AABB(h).inflate(ARENA_RADIUS, 24, ARENA_RADIUS);
        List<LivingEntity> candidates = new ArrayList<>(level.getEntitiesOfClass(Player.class, arena,
                p -> p.isAlive() && !p.isSpectator() && !p.isCreative() && horizontal(p.position(), h) <= ARENA_RADIUS));
        if (candidates.isEmpty()) {
            candidates.addAll(level.getEntitiesOfClass(io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierEntity.class, arena,
                    e -> e.isAlive() && horizontal(e.position(), h) <= ARENA_RADIUS));
        }
        LivingEntity current = this.getTarget();
        if (current != null && current.isAlive() && candidates.contains(current) && this.random.nextInt(120) != 0) {
            return current;
        }
        return candidates.stream().min(Comparator.comparingDouble(this::distanceToSqr)).orElse(null);
    }

    private static double horizontal(Vec3 p, BlockPos center) {
        double dx = p.x - (center.getX() + 0.5);
        double dz = p.z - (center.getZ() + 0.5);
        return Math.sqrt(dx * dx + dz * dz);
    }

    private float healthFraction() {
        return this.getHealth() / this.getMaxHealth();
    }

    // ------------------------------------------------------------------ Schwimmen

    /** Die Mitte seines Beckens (an der Wasseroberfläche). */
    private Vec3 center() {
        return this.home != null ? Vec3.atBottomCenterOf(this.home) : this.position();
    }

    /** Ein Punkt, höchstens {@code margin} Blöcke vor dem Rand seines Schwimmbereichs (waagerecht um die Mitte). */
    private Vec3 clampToSwim(Vec3 p, double margin) {
        Vec3 c = this.center();
        Vec3 off = new Vec3(p.x - c.x, 0.0, p.z - c.z);
        double max = SWIM_RADIUS - margin;
        double r = off.length();
        if (r > max) {
            off = off.scale(max / r);
        }
        return new Vec3(c.x + off.x, c.y, c.z + off.z);
    }

    /**
     * Schwer und träge: Er zieht Kreise um sein Ziel (gut in Reichweite seiner Arme) und wechselt dabei mal die Richtung; ohne
     * Ziel kreist er gemächlich um die Mitte. {@code effort}: 1 volle Fahrt, 0 bremsen (im Angriff).
     */
    private void swim(ServerLevel level, @Nullable LivingEntity target, double effort) {
        double max = (this.enraged ? SWIM_SPEED_ENRAGED : SWIM_SPEED) * effort;
        if (max > 0.0) {
            if (--this.swimGoalTicks <= 0 || this.swimGoal == null || horizontalBetween(this.position(), this.swimGoal) < 3.0) {
                this.swimGoal = this.nextSwimGoal(target);
                this.swimGoalTicks = 80 + this.random.nextInt(60);
            }
            Vec3 to = this.swimGoal.subtract(this.position()).multiply(1.0, 0.0, 1.0);
            double dist = to.length();
            Vec3 desired = dist < 1.0e-3 ? Vec3.ZERO : to.scale(max * Math.min(1.0, dist / 6.0) / dist);
            this.swimVel = this.swimVel.add(desired.subtract(this.swimVel).scale(0.045));
        } else {
            this.swimVel = this.swimVel.scale(0.88);
        }
        this.moveSwim();
        // ein Ruderschlag seiner Arme (alle anderthalb Sekunden, wenn er Fahrt macht)
        if (this.swimVel.horizontalDistance() > SWIM_SPEED * 0.6 && ++this.strokeTicks >= 34) {
            this.strokeTicks = 0;
            level.playSound(null, this.blockPosition(), ModSounds.KRAKEN_SWIM.get(), SoundSource.HOSTILE, 2.5f, 0.85f + this.random.nextFloat() * 0.2f);
        }
    }

    private Vec3 nextSwimGoal(@Nullable LivingEntity target) {
        Vec3 c = this.center();
        if (target == null) {
            double a = Math.atan2(this.getZ() - c.z, this.getX() - c.x) + this.orbit * (0.6 + this.random.nextDouble() * 0.5);
            double r = 5.0 + this.random.nextDouble() * 8.0;
            return this.clampToSwim(new Vec3(c.x + Math.cos(a) * r, c.y, c.z + Math.sin(a) * r), 1.0);
        }
        if (this.random.nextInt(4) == 0) {
            this.orbit = -this.orbit;
        }
        Vec3 tp = target.position();
        double a = Math.atan2(this.getZ() - tp.z, this.getX() - tp.x) + this.orbit * (0.6 + this.random.nextDouble() * 0.7);
        double r = 8.0 + this.random.nextDouble() * 6.0;
        return this.clampToSwim(new Vec3(tp.x + Math.cos(a) * r, c.y, tp.z + Math.sin(a) * r), 1.0);
    }

    /** Ein Schritt mit der Schwimmgeschwindigkeit; am Rand seines Bereichs gleitet er entlang. Gibt zurück, ob er anstieß. */
    private boolean moveSwim() {
        if (this.home == null) {
            return false;
        }
        Vec3 c = this.center();
        double x = this.getX() + this.swimVel.x;
        double z = this.getZ() + this.swimVel.z;
        double ox = x - c.x;
        double oz = z - c.z;
        double r = Math.sqrt(ox * ox + oz * oz);
        boolean edge = false;
        if (r > SWIM_RADIUS) {
            double nx = ox / r;
            double nz = oz / r;
            x = c.x + nx * SWIM_RADIUS;
            z = c.z + nz * SWIM_RADIUS;
            double out = this.swimVel.x * nx + this.swimVel.z * nz;
            if (out > 0.0) {
                this.swimVel = this.swimVel.subtract(nx * out, 0.0, nz * out);
            }
            edge = true;
        }
        if (Math.abs(x - this.getX()) > 1.0e-6 || Math.abs(z - this.getZ()) > 1.0e-6 || Math.abs(this.getY() - c.y) > 1.0e-6) {
            this.setPos(x, c.y, z);
        }
        return edge;
    }

    // ------------------------------------------------------------------ Angriffe

    private void chooseAttack(ServerLevel level, LivingEntity target) {
        float hp = this.healthFraction();
        if (this.calls == 0 && hp < 0.67f || this.calls == 1 && hp < 0.34f) {
            this.calls++;
            this.begin(level, Attack.CALL, target);
            return;
        }
        double dist = this.horizontalTo(target);
        List<Attack> options = new ArrayList<>();
        options.add(Attack.SLAM);
        options.add(Attack.SLAM);
        if (dist < SWEEP_REACH - 1.0) {
            options.add(Attack.SWEEP);
            options.add(Attack.SWEEP);
        }
        if (dist < GRAB_REACH) {
            options.add(Attack.GRAB);
        }
        options.add(Attack.INK);
        options.add(Attack.CHARGE);
        if (dist > 8.0) {
            options.add(Attack.CHARGE);
        }
        options.add(Attack.GEYSER);
        if (this.sinceBreach >= 3) {
            options.add(Attack.BREACH);
        }
        if (hp < 0.67f) {
            options.add(Attack.WHIRL);
            options.add(Attack.CAGE);
            options.add(Attack.GEYSER);
        }
        if (this.enraged) {
            options.add(Attack.WAVE);
            options.add(Attack.WAVE);
            options.add(Attack.CAGE);
        }
        options.removeIf(a -> a == this.last && options.stream().anyMatch(b -> b != this.last));
        this.begin(level, options.get(this.random.nextInt(options.size())), target);
    }

    private double horizontalTo(Entity e) {
        double dx = e.getX() - this.getX();
        double dz = e.getZ() - this.getZ();
        return Math.sqrt(dx * dx + dz * dz);
    }

    /** Liegt das Ziel links vor ihm (vom Kraken aus gesehen)? */
    private boolean onLeft(Vec3 p) {
        Vec3 look = Vec3.directionFromRotation(0.0f, this.getYRot());
        Vec3 to = p.subtract(this.position());
        // im Bild: links ist dort, wohin das Kreuzprodukt von Blick und oben zeigt
        return look.x * to.z - look.z * to.x < 0.0;
    }

    private void begin(ServerLevel level, Attack next, @Nullable LivingEntity target) {
        this.attack = next;
        this.attackTick = 0;
        this.hits.clear();
        this.grabbed = null;
        this.grabDamage = 0.0f;
        this.sinceBreach = next == Attack.BREACH ? 0 : this.sinceBreach + 1;
        Vec3 aim = target != null ? target.position() : this.position().add(this.getLookAngle().scale(8.0));
        this.leftSide = this.onLeft(aim);
        switch (next) {
            case SLAM -> {
                this.slamPoint = aim;
                level.broadcastEntityEvent(this, this.leftSide ? EVENT_SLAM_LEFT : EVENT_SLAM_RIGHT);
            }
            case SWEEP -> level.broadcastEntityEvent(this, this.leftSide ? EVENT_SWEEP_LEFT : EVENT_SWEEP_RIGHT);
            case GRAB -> {
                this.entityData.set(DATA_GRAB_SIDE, this.leftSide ? 0 : 1);
                level.broadcastEntityEvent(this, this.leftSide ? EVENT_GRAB_LEFT : EVENT_GRAB_RIGHT);
            }
            case INK -> level.broadcastEntityEvent(this, EVENT_INK);
            case WHIRL -> {
                level.broadcastEntityEvent(this, EVENT_WHIRL);
                level.playSound(null, this.blockPosition(), ModSounds.KRAKEN_WHIRL.get(), SoundSource.HOSTILE, 4.0f, 0.8f);
            }
            case CALL -> {
                level.broadcastEntityEvent(this, EVENT_ROAR);
                level.playSound(null, this.blockPosition(), ModSounds.KRAKEN_ROAR.get(), SoundSource.HOSTILE, 5.0f, 0.9f);
            }
            case WAVE -> level.broadcastEntityEvent(this, EVENT_WAVE);
            case CHARGE -> {
                Vec3 to = aim.subtract(this.position()).multiply(1.0, 0.0, 1.0);
                this.chargeDir = to.lengthSqr() < 1.0e-4 ? this.getLookAngle().multiply(1.0, 0.0, 1.0).normalize() : to.normalize();
                this.chargeLeft = 0.0;
                level.broadcastEntityEvent(this, EVENT_CHARGE);
                level.playSound(null, this.blockPosition(), ModSounds.KRAKEN_CHARGE.get(), SoundSource.HOSTILE, 4.5f, 0.9f);
            }
            case BREACH -> {
                this.breachAt = null;
                level.broadcastEntityEvent(this, EVENT_DIVE);
                level.playSound(null, this.blockPosition(), ModSounds.KRAKEN_DIVE.get(), SoundSource.HOSTILE, 4.5f, 0.9f);
            }
            case GEYSER -> {
                this.geysers.clear();
                this.geyserAt = -1;
                level.broadcastEntityEvent(this, EVENT_GEYSER);
            }
            case CAGE -> {
                this.cageCenter = new Vec3(aim.x, this.getY(), aim.z);
                level.broadcastEntityEvent(this, EVENT_CAGE);
                level.playSound(null, this.blockPosition(), ModSounds.KRAKEN_GRAB.get(), SoundSource.HOSTILE, 3.5f, 0.6f);
            }
            default -> {
            }
        }
    }

    /**
     * Für Tests und die Vorführung: er greift nur noch auf Befehl an ({@code hold}) und richtet seine Angriffe auf
     * {@code target}. {@code name}: slam, sweep, grab, ink, whirl, call, wave, charge, breach, geyser, cage (sonst nur das Ziel
     * setzen). Solange er auf Befehle wartet, schwimmt er nicht (siehe {@link #forceSwim}).
     */
    public void forceAttack(ServerLevel level, String name, @Nullable LivingEntity target, boolean hold) {
        this.debugTarget = target;
        this.debugHold = hold;
        if (target != null) {
            this.setTarget(target);
        }
        Attack next = switch (name) {
            case "slam" -> Attack.SLAM;
            case "sweep" -> Attack.SWEEP;
            case "grab" -> Attack.GRAB;
            case "ink" -> Attack.INK;
            case "whirl" -> Attack.WHIRL;
            case "call" -> Attack.CALL;
            case "wave" -> Attack.WAVE;
            case "charge" -> Attack.CHARGE;
            case "breach" -> Attack.BREACH;
            case "geyser" -> Attack.GEYSER;
            case "cage" -> Attack.CAGE;
            default -> Attack.NONE;
        };
        if (this.attack == Attack.RISE) {
            this.setSink(0.0f);
        }
        if (next != Attack.NONE) {
            this.begin(level, next, target);
        } else if (this.attack == Attack.RISE) {
            this.attack = Attack.NONE;
        }
    }

    /** Für Tests und die Vorführung: er schwimmt um {@code target}, auch während er auf Befehle wartet ({@code swim}). */
    public void forceSwim(@Nullable LivingEntity target, boolean swim) {
        this.debugTarget = target;
        this.debugHold = true;
        this.debugSwim = swim;
        if (target != null) {
            this.setTarget(target);
        }
    }

    /** Für Tests und die Vorführung: zurück in die Mitte, ruhig und aufgetaucht. */
    public void debugReturnHome() {
        Vec3 c = this.center();
        this.setPos(c.x, c.y, c.z);
        this.swimVel = Vec3.ZERO;
        this.swimGoal = null;
        this.setSink(0.0f);
    }

    /** Wie schnell er gerade schwimmt (für Tests, Blöcke je Tick). */
    public double swimSpeed() {
        return this.swimVel.horizontalDistance();
    }

    /** Welcher Angriff gerade läuft (für Tests): none, rise, slam, sweep, grab, ink, whirl, call, wave, charge, breach, geyser, cage. */
    public String currentAttack() {
        return this.attack.name().toLowerCase(java.util.Locale.ROOT);
    }

    /** Wen er gerade festhält (für Tests). */
    public @Nullable UUID grabbedId() {
        return this.grabbed;
    }

    private void finish() {
        this.last = this.attack;
        this.attack = Attack.NONE;
        this.entityData.set(DATA_SPIN_SPEED, 0.0f);
        this.grabbed = null;
        if (this.sink() != 0.0f) {
            this.setSink(0.0f);
        }
        int base = this.enraged ? 18 : this.healthFraction() < 0.67f ? 26 : 32;
        this.cooldown = base + this.random.nextInt(16);
        this.swimGoal = null;
    }

    /** Wie viel Fahrt er im Angriff noch macht (er treibt weiter, bremst beim Zupacken und Strudeln). */
    private double effort() {
        if (this.debugHold && !this.debugSwim) {
            return 0.0;
        }
        return switch (this.attack) {
            case SLAM, INK -> 0.45;
            case SWEEP, GEYSER, CAGE -> 0.25;
            default -> 0.0;
        };
    }

    private void tickAttack(ServerLevel level, @Nullable LivingEntity target) {
        int t = ++this.attackTick;
        if (this.attack != Attack.CHARGE && this.attack != Attack.BREACH) {
            this.swim(level, target, this.effort());
        }
        switch (this.attack) {
            case SLAM -> this.tickSlam(level, target, t);
            case SWEEP -> this.tickSweep(level, t);
            case GRAB -> this.tickGrab(level, target, t);
            case INK -> this.tickInk(level, target, t);
            case WHIRL -> this.tickWhirl(level, t);
            case CALL -> this.tickCall(level, target, t);
            case WAVE -> this.tickWave(level, t);
            case CHARGE -> this.tickCharge(level, target, t);
            case BREACH -> this.tickBreach(level, target, t);
            case GEYSER -> this.tickGeyser(level, target, t);
            case CAGE -> this.tickCage(level, target, t);
            default -> this.finish();
        }
    }

    /** Tentakelschlag: ein Kreis aus Blasen warnt, wo der Fangarm niederfährt (er zielt bis kurz vorher nach). */
    private void tickSlam(ServerLevel level, @Nullable LivingEntity target, int t) {
        if (target != null && t < SLAM_HIT - SLAM_WARN) {
            this.slamPoint = target.position();
            this.face(target.position(), 8.0f);
        }
        Vec3 p = this.slamPoint != null ? this.slamPoint : this.position();
        double dist = Math.min(GRAB_REACH + 2.0, horizontalBetween(this.position(), p));
        // weiter als der Arm reicht, schlägt er nicht – dann trifft er den Rand seiner Reichweite
        Vec3 dir = p.subtract(this.position()).multiply(1.0, 0.0, 1.0);
        Vec3 hit = dir.lengthSqr() < 1.0e-4 ? p : this.position().add(dir.normalize().scale(dist)).add(0.0, p.y - this.getY(), 0.0);
        if (t < SLAM_HIT && t % 3 == 0) {
            ring(level, hit.add(0.0, 0.2, 0.0), SLAM_RADIUS, ParticleTypes.BUBBLE_POP, 20);
            level.sendParticles(ParticleTypes.BUBBLE_COLUMN_UP, hit.x, hit.y, hit.z, 6, SLAM_RADIUS * 0.4, 0.2, SLAM_RADIUS * 0.4, 0.1);
        }
        if (t == 6) {
            level.playSound(null, this.blockPosition(), ModSounds.KRAKEN_SWEEP.get(), SoundSource.HOSTILE, 3.0f, 0.7f);
        }
        if (t == SLAM_HIT - SLAM_WARN) {
            fx(level, SpellFxEntity.Kind.CIRCLE, hit.add(0.0, 0.05, 0.0), 0x40B8FF, SLAM_WARN, (float) SLAM_RADIUS).spawn();
        }
        if (t == SLAM_HIT) {
            float damage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE) * 1.25f;
            this.impact(level, hit, SLAM_RADIUS, damage, 1.3, 0.55);
        }
        if (t >= SLAM_TICKS) {
            this.finish();
        }
    }

    private static double horizontalBetween(Vec3 a, Vec3 b) {
        double dx = a.x - b.x;
        double dz = a.z - b.z;
        return Math.sqrt(dx * dx + dz * dz);
    }

    /** Armfeger: die Arme einer Seite fegen herum; wer auf dieser Seite nah am Wasser ist, fliegt davon. */
    private void tickSweep(ServerLevel level, int t) {
        if (t == 8) {
            level.playSound(null, this.blockPosition(), ModSounds.KRAKEN_SWEEP.get(), SoundSource.HOSTILE, 4.0f, 0.6f);
        }
        if (t >= SWEEP_FROM && t <= SWEEP_TO) {
            // der Arm streicht von hinten (160 Grad) bis knapp über die Mitte vorn (-10 Grad) über die Seite;
            // größerer Gierwinkel = nach rechts gedreht
            float side = this.leftSide ? -1.0f : 1.0f;
            float before = (t - 1 - SWEEP_FROM) / (float) (SWEEP_TO - SWEEP_FROM);
            float progress = (t - SWEEP_FROM) / (float) (SWEEP_TO - SWEEP_FROM);
            float a0 = side * Mth.lerp(Math.max(0.0f, before), 160.0f, -10.0f);
            float a1 = side * Mth.lerp(progress, 160.0f, -10.0f);
            Vec3 dir = Vec3.directionFromRotation(0.0f, this.getYRot() + a1);
            for (int i = 0; i < 6; i++) {
                double d = 6.0 + i * (SWEEP_REACH - 6.0) / 5.0;
                Vec3 p = this.position().add(dir.scale(d));
                level.sendParticles(ParticleTypes.SPLASH, p.x, p.y + 0.9, p.z, 8, 0.6, 0.2, 0.6, 0.2);
            }
            float lo = Math.min(a0, a1) - 12.0f;
            float hi = Math.max(a0, a1) + 12.0f;
            for (LivingEntity e : this.enemiesNear(level, SWEEP_REACH)) {
                if (this.hits.contains(e.getUUID()) || Math.abs(e.getY() - this.getY()) > 5.0) {
                    continue;
                }
                Vec3 to = e.position().subtract(this.position()).multiply(1.0, 0.0, 1.0);
                double d = to.length();
                if (d < 2.0 || d > SWEEP_REACH) {
                    continue;
                }
                // wo das Ziel liegt (Winkel zur Blickrichtung): getroffen, wenn der Arm in diesem Tick darüber streicht
                float bearing = -((float) Mth.atan2(to.x, to.z)) * Mth.RAD_TO_DEG;
                float rel = Mth.wrapDegrees(bearing - this.getYRot());
                if (rel >= lo && rel <= hi) {
                    this.hits.add(e.getUUID());
                    float damage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.8f;
                    if (e.hurtServer(level, this.damageSources().mobAttack(this), damage)) {
                        // weg vom Kraken und in Fegerichtung
                        Vec3 away = to.normalize();
                        Vec3 sweep = new Vec3(-dir.z, 0.0, dir.x).scale(-side);
                        Vec3 push = away.scale(1.1).add(sweep.scale(0.7));
                        e.push(push.x, 0.6, push.z);
                        e.needsSync = true;
                    }
                }
            }
        }
        if (t >= SWEEP_TICKS) {
            this.finish();
        }
    }

    /**
     * Würgegriff: ein Fangarm schnellt zum Ziel und packt es; er hält es fest und zieht es zum Schnabel. Wer ihn in der Zeit
     * hart genug trifft ({@link #GRAB_BREAK}), befreit die Beute; sonst beißt er zu und schleudert sie fort.
     */
    private void tickGrab(ServerLevel level, @Nullable LivingEntity target, int t) {
        if (t <= GRAB_CATCH && target != null) {
            this.face(target.position(), 12.0f);
        }
        if (t == 4) {
            level.playSound(null, this.blockPosition(), ModSounds.KRAKEN_GRAB.get(), SoundSource.HOSTILE, 3.0f, 0.8f);
        }
        if (t == GRAB_CATCH && target != null && this.horizontalTo(target) <= GRAB_REACH && Math.abs(target.getY() - this.getY()) < 10.0) {
            this.grabbed = target.getUUID();
            this.grabDamage = 0.0f;
            target.hurtServer(level, this.damageSources().mobAttack(this), (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.3f);
            level.sendParticles(ParticleTypes.SPLASH, target.getX(), target.getY() + 1.0, target.getZ(), 40, 0.6, 0.6, 0.6, 0.3);
        }
        LivingEntity held = this.grabbed != null && level.getEntity(this.grabbed) instanceof LivingEntity e && e.isAlive() ? e : null;
        if (held != null && t > GRAB_CATCH && t < GRAB_BITE) {
            // zum Schnabel ziehen: vor ihm, knapp über dem Wasser
            Vec3 beak = this.position().add(this.getLookAngle().multiply(1.0, 0.0, 1.0).normalize().scale(5.5)).add(0.0, 2.0, 0.0);
            Vec3 now = held.position();
            Vec3 next = now.add(beak.subtract(now).scale(0.08));
            held.teleportTo(next.x, next.y, next.z);
            held.setDeltaMovement(Vec3.ZERO);
            held.fallDistance = 0.0f;
            held.needsSync = true;
            if (t % 20 == 0) {
                held.hurtServer(level, this.damageSources().mobAttack(this), 3.0f);
                level.sendParticles(ParticleTypes.BUBBLE, held.getX(), held.getY() + 1.0, held.getZ(), 10, 0.3, 0.5, 0.3, 0.05);
            }
            if (this.grabDamage >= GRAB_BREAK) {
                // befreit: der Fangarm zuckt zurück
                level.broadcastEntityEvent(this, EVENT_RELEASE);
                level.playSound(null, this.blockPosition(), ModSounds.KRAKEN_HURT.get(), SoundSource.HOSTILE, 4.0f, 1.2f);
                held.setDeltaMovement(this.getLookAngle().multiply(1.0, 0.0, 1.0).normalize().scale(0.8).add(0.0, 0.5, 0.0));
                held.needsSync = true;
                this.grabbed = null;
                this.finish();
                this.cooldown += 20;
                return;
            }
        }
        if (t == GRAB_BITE && held != null) {
            float damage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE) * 1.2f;
            held.hurtServer(level, this.damageSources().mobAttack(this), damage);
            level.playSound(null, held.blockPosition(), ModSounds.KRAKEN_BITE.get(), SoundSource.HOSTILE, 3.0f, 0.8f);
            Vec3 forward = this.getLookAngle().multiply(1.0, 0.0, 1.0).normalize();
            Vec3 fling = forward.yRot((this.random.nextBoolean() ? 1 : -1) * 0.8f).scale(1.8);
            held.setDeltaMovement(fling.x, 1.0, fling.z);
            held.needsSync = true;
            this.grabbed = null;
        }
        if (t >= GRAB_TICKS || held == null && t > GRAB_CATCH + 10) {
            this.finish();
        }
    }

    /** Tintenwolke: eine Wolke aus Tinte, die blendet und lähmt, dort, wo das Ziel ist. */
    private void tickInk(ServerLevel level, @Nullable LivingEntity target, int t) {
        if (target != null && t < INK_SPIT) {
            this.face(target.position(), 8.0f);
        }
        if (t == INK_SPIT) {
            Vec3 at = target != null ? target.position() : this.position().add(this.getLookAngle().scale(10.0));
            level.playSound(null, this.blockPosition(), ModSounds.KRAKEN_INK.get(), SoundSource.HOSTILE, 4.0f, 0.6f);
            Vec3 from = this.position().add(0.0, 2.0, 0.0);
            Vec3 path = at.subtract(from);
            for (int i = 0; i < 24; i++) {
                Vec3 p = from.add(path.scale(i / 24.0));
                level.sendParticles(ParticleTypes.SQUID_INK, p.x, p.y, p.z, 6, 0.3, 0.3, 0.3, 0.02);
            }
            level.sendParticles(ParticleTypes.SQUID_INK, at.x, at.y + 1.0, at.z, 160, 2.5, 1.2, 2.5, 0.08);
            AreaEffectCloud cloud = new AreaEffectCloud(level, at.x, at.y, at.z);
            cloud.setOwner(this);
            cloud.setRadius(4.5f);
            cloud.setRadiusOnUse(0.0f);
            cloud.setWaitTime(0);
            cloud.setDuration(160);
            cloud.setRadiusPerTick(-0.005f);
            cloud.setCustomParticle(ParticleTypes.SQUID_INK);
            cloud.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 70, 0));
            cloud.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 90, 1));
            level.addFreshEntity(cloud);
        }
        if (t >= INK_TICKS) {
            this.finish();
        }
    }

    /** Strudel: er dreht sich, das Wasser kreist und zieht alles zum Schlund – zuletzt schießt es in der Mitte hoch. */
    private void tickWhirl(ServerLevel level, int t) {
        if (t == WHIRL_FROM) {
            this.entityData.set(DATA_SPIN_SPEED, 6.0f);
            fx(level, SpellFxEntity.Kind.CAST_CIRCLE, this.position().add(0.0, 0.1, 0.0), 0x40B8FF, WHIRL_BURST - WHIRL_FROM + 4,
                    (float) WHIRL_RADIUS).param(0.9f).anchor(this).spawn();
        }
        if (t >= WHIRL_FROM && t < WHIRL_BURST && t % 2 == 0) {
            // Spiralen aus Gischt und Blasen, die nach innen laufen
            Vec3 c = this.position();
            for (int i = 0; i < 8; i++) {
                double a = t * 0.3 + i * Math.PI / 4.0;
                double r = WHIRL_RADIUS - ((t * 0.7 + i * 3.1) % (WHIRL_RADIUS - 6.0));
                double x = c.x + Math.cos(a) * r;
                double z = c.z + Math.sin(a) * r;
                level.sendParticles(ParticleTypes.SPLASH, x, c.y + 0.3, z, 4, 0.4, 0.1, 0.4, 0.05);
                level.sendParticles(ParticleTypes.BUBBLE_POP, x, c.y + 0.5, z, 2, 0.3, 0.2, 0.3, 0.0);
                level.sendParticles(ParticleTypes.CLOUD, x, c.y + 0.4, z, 1, 0.2, 0.05, 0.2, 0.0);
            }
        }
        if (t >= WHIRL_FROM && t < WHIRL_BURST) {
            for (LivingEntity e : this.enemiesNear(level, WHIRL_RADIUS)) {
                if (Math.abs(e.getY() - this.getY()) > 6.0) {
                    continue;
                }
                Vec3 to = this.position().subtract(e.position()).multiply(1.0, 0.0, 1.0);
                double d = to.length();
                if (d < 1.0 || d > WHIRL_RADIUS) {
                    continue;
                }
                Vec3 in = to.normalize();
                Vec3 round = new Vec3(-in.z, 0.0, in.x);
                double pull = 0.035 + 0.03 * (1.0 - d / WHIRL_RADIUS);
                e.push(in.x * pull + round.x * 0.03, 0.0, in.z * pull + round.z * 0.03);
                e.needsSync = true;
            }
            if (t % 10 == 0) {
                level.playSound(null, this.blockPosition(), net.minecraft.sounds.SoundEvents.BUBBLE_COLUMN_WHIRLPOOL_AMBIENT, SoundSource.HOSTILE, 3.0f,
                        0.6f);
            }
        }
        if (t == WHIRL_BURST) {
            this.entityData.set(DATA_SPIN_SPEED, 0.0f);
            fx(level, SpellFxEntity.Kind.PILLAR, this.position(), 0x60C8FF, 26, 6.5f).param(18.0f).spawn();
            level.sendParticles(ParticleTypes.SPLASH, this.getX(), this.getY() + 1.0, this.getZ(), 300, 5.0, 2.0, 5.0, 0.8);
            level.sendParticles(ParticleTypes.BUBBLE_COLUMN_UP, this.getX(), this.getY(), this.getZ(), 120, 4.0, 1.0, 4.0, 0.5);
            level.playSound(null, this.blockPosition(), ModSounds.KRAKEN_WAVE.get(), SoundSource.HOSTILE, 4.0f, 1.0f);
            for (LivingEntity e : this.enemiesNear(level, 8.0)) {
                if (horizontalBetween(e.position(), this.position()) < 8.0) {
                    float damage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE);
                    if (e.hurtServer(level, this.damageSources().mobAttack(this), damage)) {
                        Vec3 away = e.position().subtract(this.position()).multiply(1.0, 0.0, 1.0);
                        away = away.lengthSqr() < 1.0e-4 ? new Vec3(1.0, 0.0, 0.0) : away.normalize();
                        e.push(away.x * 1.2, 1.1, away.z * 1.2);
                        e.needsSync = true;
                    }
                }
            }
            this.shake(level, 40.0, 0.4f, 16);
        }
        if (t >= WHIRL_TICKS) {
            this.finish();
        }
    }

    /** Ruf der Tiefe: er brüllt, und Haie kommen aus der Tiefe. */
    private void tickCall(ServerLevel level, @Nullable LivingEntity target, int t) {
        if (t == 24) {
            int wanted = this.calls >= 2 ? 3 : 2;
            for (int i = 0; i < wanted; i++) {
                double a = this.random.nextDouble() * Math.PI * 2.0;
                double r = 12.0 + this.random.nextDouble() * 10.0;
                double x = this.getX() + Math.cos(a) * r;
                double z = this.getZ() + Math.sin(a) * r;
                double y = this.getY() - 2.0;
                Mob shark = ModEntities.SHARK.get().create(level, EntitySpawnReason.MOB_SUMMONED);
                if (shark == null) {
                    continue;
                }
                shark.snapTo(x, y, z, this.random.nextFloat() * 360.0f, 0.0f);
                shark.setPersistenceRequired();
                if (target != null) {
                    shark.setTarget(target);
                }
                level.addFreshEntity(shark);
                this.sharks.add(shark.getUUID());
                level.sendParticles(ParticleTypes.BUBBLE_COLUMN_UP, x, y, z, 30, 0.6, 1.0, 0.6, 0.2);
            }
        }
        if (t >= CALL_TICKS) {
            this.finish();
        }
    }

    /** Flutwelle: alle Arme schlagen aufs Wasser, eine Welle läuft als Ring nach außen. */
    private void tickWave(ServerLevel level, int t) {
        if (t == 8) {
            level.playSound(null, this.blockPosition(), ModSounds.KRAKEN_ROAR.get(), SoundSource.HOSTILE, 4.0f, 1.1f);
        }
        if (t == WAVE_SLAM) {
            level.playSound(null, this.blockPosition(), ModSounds.KRAKEN_WAVE.get(), SoundSource.HOSTILE, 5.0f, 0.7f);
            this.shake(level, 50.0, 0.55f, 24);
            this.waves.add(new Wave(this.position()));
            this.hits.clear();
            fx(level, SpellFxEntity.Kind.SHOCKWAVE, this.position().add(0.0, 0.15, 0.0), 0x60C8FF, (int) ((WAVE_REACH - 4.0) / 0.9) + 2, 4.0f)
                    .param(0.9f).spawn();
        }
        if (t >= WAVE_TICKS) {
            this.finish();
        }
    }

    /** Die Ringe der Flutwelle laufen nach außen (ein Block je Tick); wen die Front erfasst, den reißt sie mit. */
    private void tickWaves(ServerLevel level) {
        if (this.waves.isEmpty()) {
            return;
        }
        for (Wave wave : this.waves) {
            wave.age++;
            double r = 4.0 + wave.age * 0.9;
            ring(level, wave.center.add(0.0, 1.0, 0.0), r, ParticleTypes.SPLASH, (int) (r * 3));
            if (wave.age % 2 == 0) {
                ring(level, wave.center.add(0.0, 0.6, 0.0), r - 0.5, ParticleTypes.BUBBLE_POP, (int) (r * 2));
            }
            for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, new AABB(wave.center, wave.center).inflate(r + 2.0, 12.0, r + 2.0),
                    this::isEnemy)) {
                if (this.hits.contains(e.getUUID()) || Math.abs(e.getY() - wave.center.y) > 4.0) {
                    continue;
                }
                double d = horizontalBetween(e.position(), wave.center);
                if (Math.abs(d - r) < 1.6) {
                    this.hits.add(e.getUUID());
                    float damage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.9f;
                    if (e.hurtServer(level, this.damageSources().mobAttack(this), damage)) {
                        Vec3 away = e.position().subtract(wave.center).multiply(1.0, 0.0, 1.0);
                        away = away.lengthSqr() < 1.0e-4 ? new Vec3(1.0, 0.0, 0.0) : away.normalize();
                        e.push(away.x * 1.6, 0.7, away.z * 1.6);
                        e.needsSync = true;
                    }
                }
            }
        }
        this.waves.removeIf(w -> 4.0 + w.age * 0.9 > WAVE_REACH);
    }

    /**
     * Rammstoß: er saugt Wasser in den Mantel und richtet sich auf sein Ziel aus (Warnkreise zeigen die Bahn), dann schießt er
     * mit einem Wasserstrahl {@link #CHARGE_LENGTH} Blöcke weit durchs Becken. Wen er dabei streift, den überrollt er und
     * schleudert ihn zur Seite; danach gleitet er aus.
     */
    private void tickCharge(ServerLevel level, @Nullable LivingEntity target, int t) {
        if (t < CHARGE_WINDUP - 8 && target != null) {
            Vec3 to = target.position().subtract(this.position()).multiply(1.0, 0.0, 1.0);
            if (to.lengthSqr() > 1.0e-3) {
                this.chargeDir = to.normalize();
            }
        }
        if (t < CHARGE_WINDUP + CHARGE_DASH) {
            this.face(this.position().add(this.chargeDir.scale(10.0)), t < CHARGE_WINDUP ? 9.0f : 4.0f);
        }
        if (t < CHARGE_WINDUP) {
            // er holt aus: bremst, saugt Wasser ein (Blasen strömen zu ihm)
            this.swimVel = this.swimVel.scale(0.8);
            this.moveSwim();
            if (t % 2 == 0) {
                for (int i = 0; i < 6; i++) {
                    double a = this.random.nextDouble() * Math.PI * 2.0;
                    double r = 7.0 + this.random.nextDouble() * 3.0;
                    double x = this.getX() + Math.cos(a) * r;
                    double z = this.getZ() + Math.sin(a) * r;
                    level.sendParticles(ParticleTypes.BUBBLE, x, this.getY() + 0.4, z, 0, -Math.cos(a), 0.0, -Math.sin(a), 0.35);
                }
            }
            if (t == CHARGE_WINDUP - 10) {
                // die Bahn: Warnkreise, die sich bis zum Stoß füllen
                Vec3 end = this.clampToSwim(this.position().add(this.chargeDir.scale(CHARGE_LENGTH)), 0.0);
                double len = horizontalBetween(this.position(), end);
                for (double d = 4.0; d <= len + 2.0; d += 4.5) {
                    Vec3 p = this.position().add(this.chargeDir.scale(d));
                    fx(level, SpellFxEntity.Kind.CIRCLE, p.add(0.0, 0.05, 0.0), 0x40B8FF, 10, (float) (CHARGE_HIT * 0.62)).spawn();
                }
            }
            return;
        }
        if (t < CHARGE_WINDUP + CHARGE_DASH) {
            if (t == CHARGE_WINDUP) {
                this.chargeLeft = CHARGE_LENGTH;
                this.shake(level, 30.0, 0.35f, 12);
                level.sendParticles(ParticleTypes.BUBBLE_COLUMN_UP, this.getX(), this.getY(), this.getZ(), 80, 3.0, 0.6, 3.0, 0.4);
            }
            double step = Math.min(1.1, this.chargeLeft);
            this.chargeLeft -= step;
            this.swimVel = this.chargeDir.scale(step);
            if (this.moveSwim() || step <= 1.0e-3) {
                this.chargeLeft = 0.0;
            }
            // Bugwelle und Strahl hinter ihm
            Vec3 front = this.position().add(this.chargeDir.scale(5.5));
            Vec3 back = this.position().subtract(this.chargeDir.scale(6.0));
            level.sendParticles(ParticleTypes.SPLASH, front.x, this.getY() + 1.0, front.z, 30, 2.5, 0.3, 2.5, 0.35);
            level.sendParticles(ParticleTypes.BUBBLE_COLUMN_UP, back.x, this.getY() - 0.5, back.z, 16, 1.5, 0.4, 1.5, 0.3);
            level.sendParticles(ParticleTypes.CLOUD, back.x, this.getY() + 0.8, back.z, 4, 1.0, 0.2, 1.0, 0.05);
            float damage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE) * 1.3f;
            for (LivingEntity e : this.enemiesNear(level, CHARGE_HIT + 1.0)) {
                if (this.hits.contains(e.getUUID()) || Math.abs(e.getY() - this.getY()) > 7.0
                        || horizontalBetween(e.position(), this.position()) > CHARGE_HIT) {
                    continue;
                }
                this.hits.add(e.getUUID());
                if (e.hurtServer(level, this.damageSources().mobAttack(this), damage)) {
                    // zur Seite der Bahn und ein Stück mit
                    Vec3 to = e.position().subtract(this.position()).multiply(1.0, 0.0, 1.0);
                    Vec3 side = new Vec3(-this.chargeDir.z, 0.0, this.chargeDir.x);
                    double s = to.dot(side) >= 0.0 ? 1.0 : -1.0;
                    Vec3 push = side.scale(s * 1.2).add(this.chargeDir.scale(0.9));
                    e.push(push.x, 0.6, push.z);
                    e.needsSync = true;
                    level.playSound(null, e.blockPosition(), ModSounds.KRAKEN_SLAM.get(), SoundSource.HOSTILE, 2.5f, 1.1f);
                }
            }
            return;
        }
        // ausgleiten
        this.swimVel = this.swimVel.scale(0.86);
        this.moveSwim();
        if (t >= CHARGE_TICKS) {
            this.finish();
        }
    }

    /**
     * Ausbruch aus der Tiefe: er taucht ab (dann ist er unverwundbar), schwimmt unter Wasser zu seinem Ziel – nur Blasen und
     * ein dunkler Schatten verraten ihn –, ein Ring aus Blasen warnt, dann bricht er hoch aus dem Wasser und schleudert alles
     * im Umkreis von {@link #BREACH_RADIUS} Blöcken empor.
     */
    private void tickBreach(ServerLevel level, @Nullable LivingEntity target, int t) {
        if (t <= BREACH_DIVE) {
            float k = t / (float) BREACH_DIVE;
            this.setSink(k * k * (3.0f - 2.0f * k));
            this.swimVel = this.swimVel.scale(0.85);
            this.moveSwim();
            if (t % 2 == 0) {
                ring(level, this.position().add(0.0, 0.9, 0.0), 6.0 + t * 0.12, ParticleTypes.SPLASH, 28);
                level.sendParticles(ParticleTypes.BUBBLE_COLUMN_UP, this.getX(), this.getY() - 1.0, this.getZ(), 20, 4.0, 1.0, 4.0, 0.2);
            }
            return;
        }
        if (t < BREACH_WARN) {
            // unter Wasser zum Ziel
            this.setSink(1.0f);
            Vec3 goal = this.clampToSwim(target != null ? target.position() : this.position(), 0.0);
            Vec3 to = goal.subtract(this.position()).multiply(1.0, 0.0, 1.0);
            double dist = to.length();
            Vec3 desired = dist < 1.0e-3 ? Vec3.ZERO : to.scale(Math.min(0.55, dist * 0.18) / dist);
            this.swimVel = this.swimVel.add(desired.subtract(this.swimVel).scale(0.25));
            this.moveSwim();
            if (dist > 0.5) {
                this.face(goal, 6.0f);
            }
            level.sendParticles(ParticleTypes.BUBBLE_COLUMN_UP, this.getX(), this.getY() - 1.5, this.getZ(), 6, 2.0, 0.5, 2.0, 0.2);
            level.sendParticles(ParticleTypes.BUBBLE_POP, this.getX(), this.getY() + 0.9, this.getZ(), 4, 2.5, 0.05, 2.5, 0.02);
            if (t % 3 == 0) {
                level.sendParticles(ParticleTypes.SQUID_INK, this.getX(), this.getY() - 0.8, this.getZ(), 8, 2.5, 0.2, 2.5, 0.0);
            }
            return;
        }
        if (t < BREACH_ERUPT) {
            this.swimVel = this.swimVel.scale(0.6);
            this.moveSwim();
            if (t == BREACH_WARN) {
                this.breachAt = this.position();
                fx(level, SpellFxEntity.Kind.CIRCLE, this.position().add(0.0, 0.05, 0.0), 0x40B8FF, BREACH_ERUPT - BREACH_WARN, (float) BREACH_RADIUS)
                        .spawn();
                level.playSound(null, this.blockPosition(), ModSounds.KRAKEN_DIVE.get(), SoundSource.HOSTILE, 3.5f, 0.6f);
            }
            Vec3 c = this.breachAt != null ? this.breachAt : this.position();
            ring(level, c.add(0.0, 0.8, 0.0), BREACH_RADIUS * (0.5 + 0.5 * this.random.nextDouble()), ParticleTypes.BUBBLE_POP, 16);
            level.sendParticles(ParticleTypes.BUBBLE_COLUMN_UP, c.x, c.y - 1.0, c.z, 24, BREACH_RADIUS * 0.5, 0.6, BREACH_RADIUS * 0.5, 0.35);
            return;
        }
        if (t == BREACH_ERUPT) {
            this.erupt(level);
        }
        int e = t - BREACH_ERUPT;
        if (e < 5) {
            this.setSink(Mth.lerp((e + 1) / 5.0f, 1.0f, -0.3f));
        } else if (e < 22) {
            float k = (e - 5) / 17.0f;
            this.setSink(-0.3f * (1.0f - k * k * (3.0f - 2.0f * k)));
        } else {
            this.setSink(0.0f);
        }
        if (t >= BREACH_TICKS) {
            this.finish();
        }
    }

    /** Er bricht aus dem Wasser: eine Säule aus Gischt, und wer darüber war, fliegt hoch empor. */
    private void erupt(ServerLevel level) {
        Vec3 c = this.position();
        level.broadcastEntityEvent(this, EVENT_BREACH);
        level.playSound(null, this.blockPosition(), ModSounds.KRAKEN_BREACH.get(), SoundSource.HOSTILE, 5.0f, 0.9f);
        fx(level, SpellFxEntity.Kind.PILLAR, c, 0x60C8FF, 26, (float) BREACH_RADIUS).param(20.0f).spawn();
        fx(level, SpellFxEntity.Kind.SHOCKWAVE, c.add(0.0, 0.15, 0.0), 0x60C8FF, 16, (float) BREACH_RADIUS).param(0.8f).spawn();
        level.sendParticles(ParticleTypes.SPLASH, c.x, c.y + 3.0, c.z, 500, 5.0, 4.0, 5.0, 0.8);
        level.sendParticles(ParticleTypes.BUBBLE_COLUMN_UP, c.x, c.y, c.z, 150, 5.0, 1.0, 5.0, 0.6);
        level.sendParticles(ParticleTypes.CLOUD, c.x, c.y + 2.0, c.z, 40, 4.0, 2.0, 4.0, 0.08);
        this.shake(level, 60.0, 0.7f, 30);
        float damage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE) * 1.5f;
        for (LivingEntity e : this.enemiesNear(level, BREACH_RADIUS + 1.0)) {
            double d = horizontalBetween(e.position(), c);
            if (d > BREACH_RADIUS + 0.5 || e.getY() < c.y - 8.0 || e.getY() > c.y + 12.0) {
                continue;
            }
            if (e.hurtServer(level, this.damageSources().mobAttack(this), damage)) {
                Vec3 away = e.position().subtract(c).multiply(1.0, 0.0, 1.0);
                away = away.lengthSqr() < 1.0e-4 ? new Vec3(1.0, 0.0, 0.0) : away.normalize();
                e.push(away.x * 0.9, 1.35, away.z * 0.9);
                e.needsSync = true;
            }
        }
    }

    /**
     * Geysirfeld: er schlägt mit den vorderen Armen aufs Wasser; rings um seine Feinde brodelt es {@link #GEYSER_WARN} Ticks
     * lang – dann schießen Geysire empor und schleudern hoch, wer darauf steht. Ab zwei Dritteln (und in der Raserei) folgt
     * gleich ein zweites Feld.
     */
    private void tickGeyser(ServerLevel level, @Nullable LivingEntity target, int t) {
        if (target != null && t < GEYSER_FIRST) {
            this.face(target.position(), 8.0f);
        }
        if (t == 10) {
            level.playSound(null, this.blockPosition(), ModSounds.KRAKEN_SLAM.get(), SoundSource.HOSTILE, 3.5f, 0.85f);
            Vec3 front = this.position().add(this.getLookAngle().multiply(1.0, 0.0, 1.0).normalize().scale(7.0));
            level.sendParticles(ParticleTypes.SPLASH, front.x, this.getY() + 1.0, front.z, 120, 3.0, 0.4, 3.0, 0.5);
            this.shake(level, 30.0, 0.3f, 10);
        }
        boolean second = this.enraged || this.healthFraction() < 0.67f || this.debugHold;
        if (t == GEYSER_FIRST || t == GEYSER_SECOND && second) {
            this.pickGeysers(level, target);
            this.geyserAt = t + GEYSER_WARN;
        }
        if (this.geyserAt > t && t % 2 == 0) {
            for (Vec3 g : this.geysers) {
                level.sendParticles(ParticleTypes.BUBBLE_COLUMN_UP, g.x, g.y - 1.5, g.z, 5, GEYSER_RADIUS * 0.4, 0.4, GEYSER_RADIUS * 0.4, 0.25);
                ring(level, g.add(0.0, 0.8, 0.0), GEYSER_RADIUS * (0.6 + 0.4 * this.random.nextDouble()), ParticleTypes.BUBBLE_POP, 8);
            }
        }
        if (t == this.geyserAt) {
            this.eruptGeysers(level);
        }
        if (t >= GEYSER_TICKS) {
            this.finish();
        }
    }

    private void pickGeysers(ServerLevel level, @Nullable LivingEntity target) {
        this.geysers.clear();
        this.hits.clear();
        double y = this.getY();
        if (target != null) {
            Vec3 at = target.position();
            this.geysers.add(new Vec3(at.x, y, at.z));
            int more = this.enraged ? 5 : 3;
            double a0 = this.random.nextDouble() * Math.PI * 2.0;
            for (int i = 0; i < more; i++) {
                double a = a0 + i * Math.PI * 2.0 / more + (this.random.nextDouble() - 0.5) * 0.6;
                double r = 3.6 + this.random.nextDouble() * 3.4;
                this.geysers.add(new Vec3(at.x + Math.cos(a) * r, y, at.z + Math.sin(a) * r));
            }
        }
        // auch unter den anderen im Ring brodelt es
        BlockPos h = this.home;
        if (h != null) {
            int others = 0;
            for (Player p : level.getEntitiesOfClass(Player.class, new AABB(h).inflate(ARENA_RADIUS, 24, ARENA_RADIUS),
                    p -> p != target && this.isEnemy(p) && horizontal(p.position(), h) <= ARENA_RADIUS)) {
                if (others++ >= 3) {
                    break;
                }
                this.geysers.add(new Vec3(p.getX(), y, p.getZ()));
            }
        }
        for (Vec3 g : this.geysers) {
            fx(level, SpellFxEntity.Kind.CIRCLE, g.add(0.0, 0.05, 0.0), 0x40B8FF, GEYSER_WARN, (float) GEYSER_RADIUS).spawn();
        }
        if (!this.geysers.isEmpty()) {
            Vec3 g = this.geysers.get(0);
            level.playSound(null, g.x, g.y, g.z, net.minecraft.sounds.SoundEvents.BUBBLE_COLUMN_UPWARDS_AMBIENT, SoundSource.HOSTILE, 3.0f, 0.7f);
        }
    }

    private void eruptGeysers(ServerLevel level) {
        float damage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.9f;
        int i = 0;
        for (Vec3 g : this.geysers) {
            fx(level, SpellFxEntity.Kind.PILLAR, g, 0x7FD8FF, 24, (float) (GEYSER_RADIUS * 0.8)).param(13.0f).spawn();
            level.sendParticles(ParticleTypes.SPLASH, g.x, g.y + 2.0, g.z, 90, GEYSER_RADIUS * 0.5, 2.5, GEYSER_RADIUS * 0.5, 0.6);
            level.sendParticles(ParticleTypes.CLOUD, g.x, g.y + 4.0, g.z, 8, 0.8, 2.0, 0.8, 0.05);
            level.playSound(null, g.x, g.y, g.z, ModSounds.KRAKEN_GEYSER.get(), SoundSource.HOSTILE, i++ < 3 ? 3.5f : 1.5f,
                    0.9f + this.random.nextFloat() * 0.2f);
            for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, new AABB(g, g).inflate(GEYSER_RADIUS + 1.0, 14.0, GEYSER_RADIUS + 1.0),
                    this::isEnemy)) {
                if (this.hits.contains(e.getUUID()) || horizontalBetween(e.position(), g) > GEYSER_RADIUS + 0.4 || e.getY() < g.y - 3.0
                        || e.getY() > g.y + 14.0) {
                    continue;
                }
                this.hits.add(e.getUUID());
                if (e.hurtServer(level, this.damageSources().mobAttack(this), damage)) {
                    e.push((this.random.nextDouble() - 0.5) * 0.4, 1.3, (this.random.nextDouble() - 0.5) * 0.4);
                    e.needsSync = true;
                }
            }
        }
    }

    /**
     * Tentakelkäfig: seine Fangarme tauchen ab, und rings um das Ziel brechen sieben Arme aus dem Wasser (einer fehlt – die
     * Lücke); sie kreisen und ziehen sich zusammen, wer sie berührt, wird zurückgestoßen. Zuletzt schlagen sie in der Mitte
     * zusammen – je mehr Arme man vorher abgeschlagen hat, desto schwächer.
     */
    private void tickCage(ServerLevel level, @Nullable LivingEntity target, int t) {
        if (target != null && t < CAGE_SPAWN) {
            this.face(target.position(), 8.0f);
            this.cageCenter = new Vec3(target.getX(), this.getY(), target.getZ());
        }
        if (t == CAGE_SPAWN) {
            this.spawnCage(level);
        }
        if (t == CAGE_SPAWN + KrakenTentacleEntity.SLAM) {
            this.crush(level);
        }
        if (t >= CAGE_TICKS) {
            this.finish();
        }
    }

    private void spawnCage(ServerLevel level) {
        Vec3 c = this.cageCenter != null ? this.cageCenter : this.position();
        int gap = this.random.nextInt(CAGE_ARMS);
        float spin = (this.random.nextBoolean() ? 1.0f : -1.0f) * (this.enraged ? 2.4f : 1.8f);
        double a0 = this.random.nextDouble() * Math.PI * 2.0;
        this.tentacles.removeIf(id -> !(level.getEntity(id) instanceof KrakenTentacleEntity e) || !e.isAlive());
        for (int i = 0; i < CAGE_ARMS; i++) {
            if (i == gap) {
                continue;
            }
            KrakenTentacleEntity arm = ModEntities.KRAKEN_TENTACLE.get().create(level, EntitySpawnReason.MOB_SUMMONED);
            if (arm == null) {
                continue;
            }
            arm.setup(this, c, CAGE_RADIUS, a0 + i * Math.PI * 2.0 / CAGE_ARMS, spin);
            level.addFreshEntity(arm);
            this.tentacles.add(arm.getUUID());
        }
        fx(level, SpellFxEntity.Kind.CAST_CIRCLE, c.add(0.0, 0.1, 0.0), 0x40B8FF, KrakenTentacleEntity.SLAM, (float) CAGE_RADIUS).param(0.6f).spawn();
        level.playSound(null, c.x, c.y, c.z, ModSounds.KRAKEN_TENTACLE.get(), SoundSource.HOSTILE, 4.5f, 0.9f);
    }

    /** Die Arme schlagen in der Mitte des Käfigs zusammen. */
    private void crush(ServerLevel level) {
        Vec3 c = this.cageCenter != null ? this.cageCenter : this.position();
        int alive = 0;
        for (UUID id : this.tentacles) {
            if (level.getEntity(id) instanceof KrakenTentacleEntity arm && arm.isAlive() && !arm.isCut()) {
                alive++;
            }
        }
        if (alive == 0) {
            return;
        }
        float strength = 0.4f + 0.6f * alive / (CAGE_ARMS - 1.0f);
        level.playSound(null, c.x, c.y, c.z, ModSounds.KRAKEN_SLAM.get(), SoundSource.HOSTILE, 5.0f, 0.7f);
        level.sendParticles(ParticleTypes.SPLASH, c.x, c.y + 1.0, c.z, 300, 2.5, 1.0, 2.5, 0.7);
        level.sendParticles(ParticleTypes.BUBBLE_COLUMN_UP, c.x, c.y, c.z, 60, 2.0, 0.5, 2.0, 0.4);
        fx(level, SpellFxEntity.Kind.SHOCKWAVE, c.add(0.0, 0.15, 0.0), 0x60C8FF, 12, 1.0f).param(0.5f).spawn();
        this.shake(level, 30.0, 0.5f * strength, 16);
        float damage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE) * 1.6f * strength;
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, new AABB(c, c).inflate(CAGE_CRUSH + 1.0, 8.0, CAGE_CRUSH + 1.0),
                this::isEnemy)) {
            if (horizontalBetween(e.position(), c) > CAGE_CRUSH || e.getY() < c.y - 3.0 || e.getY() > c.y + 8.0) {
                continue;
            }
            if (e.hurtServer(level, this.damageSources().mobAttack(this), damage)) {
                e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 2), this);
                e.setDeltaMovement(e.getDeltaMovement().multiply(0.2, 0.0, 0.2).add(0.0, -0.4, 0.0));
                e.needsSync = true;
            }
        }
    }

    // ------------------------------------------------------------------ Hilfen

    /** Wen er (und seine Arme) angreift: alle Lebewesen außer ihm selbst, seinen Haien und Spielern im Kreativ- oder Zuschauermodus. */
    boolean isEnemy(LivingEntity e) {
        return e != this && e.isAlive() && !(e instanceof KrakenEntity) && !this.sharks.contains(e.getUUID())
                && !(e instanceof Player p && (p.isCreative() || p.isSpectator()));
    }

    private List<LivingEntity> enemiesNear(ServerLevel level, double radius) {
        return level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(radius, 12.0, radius), this::isEnemy);
    }

    private void shake(ServerLevel level, double radius, float strength, int ticks) {
        for (ServerPlayer player : level.players()) {
            double dist = player.distanceTo(this);
            if (dist < radius) {
                float s = strength * (float) (1.0 - dist / radius * 0.7);
                io.github.sirschrolli.kingdomsatwar.network.ModNetwork.send(player,
                        new io.github.sirschrolli.kingdomsatwar.network.ScreenShakePayload(s, ticks));
            }
        }
    }

    /** Einschlag des Fangarms: Schaden nach Nähe, Gegner fliegen auf und weg. */
    private void impact(ServerLevel level, Vec3 c, double radius, float damage, double push, double up) {
        this.shake(level, 30.0, 0.45f, 14);
        level.playSound(null, c.x, c.y, c.z, ModSounds.KRAKEN_SLAM.get(), SoundSource.HOSTILE, 4.0f, 0.8f);
        level.sendParticles(ParticleTypes.SPLASH, c.x, c.y + 0.4, c.z, 140, radius * 0.5, 0.3, radius * 0.5, 0.45);
        level.sendParticles(ParticleTypes.BUBBLE_COLUMN_UP, c.x, c.y, c.z, 40, radius * 0.4, 0.4, radius * 0.4, 0.3);
        level.sendParticles(ParticleTypes.CLOUD, c.x, c.y + 0.6, c.z, 20, radius * 0.3, 0.2, radius * 0.3, 0.05);
        ring(level, c.add(0.0, 0.2, 0.0), radius, ParticleTypes.SPLASH, 36);
        for (LivingEntity e : this.enemiesNear(level, 40.0)) {
            double d = horizontalBetween(e.position(), c);
            if (d > radius + 0.6 || Math.abs(e.getY() - c.y) > 4.0) {
                continue;
            }
            float f = (float) Math.max(0.45, 1.0 - d / (radius + 1.0));
            if (e.hurtServer(level, this.damageSources().mobAttack(this), damage * f)) {
                Vec3 away = e.position().subtract(c).multiply(1.0, 0.0, 1.0);
                away = away.lengthSqr() < 1.0e-4 ? new Vec3(1.0, 0.0, 0.0) : away.normalize();
                e.push(away.x * push * f, up * f, away.z * push * f);
                e.needsSync = true;
            }
        }
    }

    /** Ein Wassereffekt (Kreis, Säule, Ring) in der Farbe des Meeres. */
    private static SpellFxEntity fx(ServerLevel level, SpellFxEntity.Kind kind, Vec3 at, int color, int life, float size) {
        return SpellFxEntity.create(level, kind, at, io.github.sirschrolli.kingdomsatwar.dragon.DragonType.WATER, color, life, size);
    }

    private static void ring(ServerLevel level, Vec3 c, double radius, ParticleOptions particle, int count) {
        for (int i = 0; i < count; i++) {
            double a = i / (double) count * Math.PI * 2.0;
            level.sendParticles(particle, c.x + Math.cos(a) * radius, c.y, c.z + Math.sin(a) * radius, 1, 0.0, 0.0, 0.0, 0.0);
        }
    }

    // ------------------------------------------------------------------ Schaden

    @Override
    public boolean isInvulnerableTo(ServerLevel level, DamageSource source) {
        return (this.attack == Attack.RISE || this.attack == Attack.BREACH) && this.sink() > 0.4f || super.isInvulnerableTo(level, source);
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        boolean hurt = super.hurtServer(level, source, amount);
        if (hurt && this.attack == Attack.GRAB && this.grabbed != null) {
            this.grabDamage += amount;
        }
        return hurt;
    }

    // ------------------------------------------------------------------ Ereignisse, Rückzug und Tod

    @Override
    public void handleEntityEvent(byte id) {
        switch (id) {
            case EVENT_ROAR -> this.roarAnimation.start(this.tickCount);
            case EVENT_SLAM_LEFT -> this.slamLeftAnimation.start(this.tickCount);
            case EVENT_SLAM_RIGHT -> this.slamRightAnimation.start(this.tickCount);
            case EVENT_SWEEP_LEFT -> this.sweepLeftAnimation.start(this.tickCount);
            case EVENT_SWEEP_RIGHT -> this.sweepRightAnimation.start(this.tickCount);
            case EVENT_GRAB_LEFT, EVENT_GRAB_RIGHT -> this.grabAnimation.start(this.tickCount);
            case EVENT_RELEASE -> this.grabAnimation.stop();
            case EVENT_INK -> this.inkAnimation.start(this.tickCount);
            case EVENT_WHIRL -> this.whirlAnimation.start(this.tickCount);
            case EVENT_WAVE -> this.waveAnimation.start(this.tickCount);
            case EVENT_CHARGE -> this.chargeAnimation.start(this.tickCount);
            case EVENT_DIVE -> this.diveAnimation.start(this.tickCount);
            case EVENT_BREACH -> {
                this.diveAnimation.stop();
                this.breachAnimation.start(this.tickCount);
            }
            case EVENT_GEYSER -> this.geyserAnimation.start(this.tickCount);
            case EVENT_CAGE -> this.cageAnimation.start(this.tickCount);
            default -> super.handleEntityEvent(id);
        }
    }

    /** Die gerufenen Haie ziehen wieder ab, die Arme des Käfigs versinken (wenn er stirbt oder hinabsinkt). */
    private void dismissSharks(ServerLevel level) {
        for (UUID id : this.sharks) {
            Entity shark = level.getEntity(id);
            if (shark != null && shark.isAlive()) {
                level.sendParticles(ParticleTypes.BUBBLE, shark.getX(), shark.getY() + 0.5, shark.getZ(), 20, 0.4, 0.4, 0.4, 0.05);
                shark.discard();
            }
        }
        this.sharks.clear();
        for (UUID id : this.tentacles) {
            if (level.getEntity(id) instanceof KrakenTentacleEntity arm && arm.isAlive()) {
                arm.discard();
            }
        }
        this.tentacles.clear();
    }

    /** Niemand mehr im Ring: er sinkt zurück in den Schlund (und kann erneut emporsteigen). */
    private void dive(ServerLevel level) {
        this.dismissSharks(level);
        level.sendParticles(ParticleTypes.BUBBLE_COLUMN_UP, this.getX(), this.getY(), this.getZ(), 80, 3.0, 1.0, 3.0, 0.3);
        level.playSound(null, this.blockPosition(), ModSounds.KRAKEN_WAVE.get(), SoundSource.HOSTILE, 3.0f, 1.2f);
        if (this.core != null && level.getBlockEntity(this.core) instanceof KrakenCoreBlockEntity heart) {
            heart.onKrakenDive(level);
        }
        this.bossBar.removeAllPlayers();
        this.discard();
    }

    @Override
    protected void tickDeath() {
        this.deathTime++;
        if (this.level() instanceof ServerLevel level) {
            this.setSink(Math.min(1.0f, this.deathTime / (float) DEATH_TICKS) * 0.9f);
            this.entityData.set(DATA_SPIN_SPEED, 0.0f);
            if (this.deathTime == 1) {
                level.playSound(null, this.blockPosition(), ModSounds.KRAKEN_DEATH.get(), SoundSource.HOSTILE, 5.0f, 0.9f);
                this.grabbed = null;
                this.swimVel = Vec3.ZERO;
            }
            if (this.deathTime % 4 == 0) {
                level.sendParticles(ParticleTypes.SQUID_INK, this.getX(), this.getY() + 3.0, this.getZ(), 30, 3.0, 2.0, 3.0, 0.05);
                level.sendParticles(ParticleTypes.BUBBLE_COLUMN_UP, this.getX(), this.getY(), this.getZ(), 20, 3.0, 1.0, 3.0, 0.2);
            }
            if (this.deathTime == DEATH_TICKS / 2) {
                this.dismissSharks(level);
            }
            if (this.deathTime == DEATH_TICKS - 6) {
                fx(level, SpellFxEntity.Kind.PILLAR, this.position(), 0x60C8FF, 22, 7.0f).param(10.0f).spawn();
                this.shake(level, 60.0, 0.5f, 24);
                level.sendParticles(ParticleTypes.SPLASH, this.getX(), this.getY() + 1.0, this.getZ(), 300, 5.0, 1.0, 5.0, 0.5);
            }
            if (this.deathTime >= DEATH_TICKS && !this.isRemoved()) {
                this.bossBar.removeAllPlayers();
                if (this.core != null && level.getBlockEntity(this.core) instanceof KrakenCoreBlockEntity heart) {
                    heart.onKrakenDefeated(level);
                }
                this.remove(Entity.RemovalReason.KILLED);
            }
        }
    }

    @Override
    public void remove(Entity.RemovalReason reason) {
        if (this.level() instanceof ServerLevel level && !this.tentacles.isEmpty()) {
            for (UUID id : this.tentacles) {
                if (level.getEntity(id) instanceof KrakenTentacleEntity arm && arm.isAlive()) {
                    arm.discard();
                }
            }
            this.tentacles.clear();
        }
        super.remove(reason);
        this.bossBar.removeAllPlayers();
    }

    // ------------------------------------------------------------------ Speichern und Geräusche

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        if (this.home != null) {
            output.putLong("Home", this.home.asLong());
        }
        if (this.core != null) {
            output.putLong("Core", this.core.asLong());
        }
        output.putInt("Calls", this.calls);
        output.putBoolean("Enraged", this.enraged);
        output.store("Sharks", UUIDUtil.CODEC.listOf(), this.sharks);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.home = input.getLong("Home").map(BlockPos::of).orElse(null);
        this.core = input.getLong("Core").map(BlockPos::of).orElse(null);
        this.calls = input.getIntOr("Calls", 0);
        this.enraged = input.getBooleanOr("Enraged", false);
        this.entityData.set(DATA_ENRAGED, this.enraged);
        this.sharks.clear();
        input.read("Sharks", UUIDUtil.CODEC.listOf()).ifPresent(this.sharks::addAll);
        this.setSink(0.0f);
        this.bossBar.setProgress(this.getHealth() / this.getMaxHealth());
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return this.sink() > 0.5f ? null : ModSounds.KRAKEN_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.KRAKEN_HURT.get();
    }

    @Override
    protected @Nullable SoundEvent getDeathSound() {
        return null;
    }

    @Override
    protected float getSoundVolume() {
        return 4.0f;
    }

    @Override
    public int getAmbientSoundInterval() {
        return 160;
    }

    // ------------------------------------------------------------------ Himmelswacht

    @Override
    public io.github.sirschrolli.kingdomsatwar.entity.boss.sky.SkyTheme skyTheme() {
        return io.github.sirschrolli.kingdomsatwar.entity.boss.sky.SkyTheme.REEF;
    }

    @Override
    public boolean guardsSky() {
        return this.attack != Attack.RISE && this.getTarget() != null && this.getTarget().isAlive();
    }
}
