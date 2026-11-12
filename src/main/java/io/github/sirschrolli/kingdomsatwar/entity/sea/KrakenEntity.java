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
 * Er bleibt in der Mitte und greift mit seinen Armen weit hinaus:
 * <ul>
 *     <li><b>Tentakelschlag:</b> ein Fangarm steigt hoch über ihn und schlägt dort nieder, wo eben noch ein Kreis aus Blasen
 *     aufstieg;</li>
 *     <li><b>Armfeger:</b> die Arme einer Seite fegen quer übers Wasser und schleudern davon, wer dort schwimmt oder steht;</li>
 *     <li><b>Würgegriff:</b> ein Fangarm packt sein Ziel, hält es fest und zieht es zum Schnabel – wer ihn in der Zeit hart
 *     genug trifft, kommt frei, sonst beißt er zu und schleudert die Beute fort;</li>
 *     <li><b>Tintenwolke:</b> er spuckt eine Wolke aus Tinte, die blendet und lähmt;</li>
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
    public static final int DEATH_TICKS = 110;
    /** So weit (waagerecht um die Mitte) reicht sein Ring: wer darin ist, ist sein Ziel. */
    public static final double ARENA_RADIUS = 34.0;

    public final AnimationState roarAnimation = new AnimationState();
    public final AnimationState slamLeftAnimation = new AnimationState();
    public final AnimationState slamRightAnimation = new AnimationState();
    public final AnimationState sweepLeftAnimation = new AnimationState();
    public final AnimationState sweepRightAnimation = new AnimationState();
    public final AnimationState grabAnimation = new AnimationState();
    public final AnimationState inkAnimation = new AnimationState();
    public final AnimationState whirlAnimation = new AnimationState();
    public final AnimationState waveAnimation = new AnimationState();

    private final ServerBossEvent bossBar = new ServerBossEvent(UUID.randomUUID(), Component.translatable("boss.kingdomsatwar.kraken"),
            BossEvent.BossBarColor.BLUE, BossEvent.BossBarOverlay.NOTCHED_10);

    private enum Attack { NONE, RISE, SLAM, SWEEP, GRAB, INK, WHIRL, CALL, WAVE }

    private Attack attack = Attack.NONE;
    private Attack last = Attack.NONE;
    private int attackTick;
    private int cooldown = 40;
    private boolean leftSide;
    private @Nullable Vec3 slamPoint;
    private @Nullable UUID grabbed;
    private float grabDamage;
    private final Set<UUID> hits = new HashSet<>();
    /** Wellenringe der Flutwelle: Alter in Ticks (laufen nach dem Angriff weiter). */
    private final List<int[]> waves = new ArrayList<>();
    private final List<UUID> sharks = new ArrayList<>();
    private int calls;
    private boolean enraged;
    private int lonely;
    private @Nullable BlockPos home;
    private @Nullable BlockPos core;
    private @Nullable LivingEntity debugTarget;
    private boolean debugHold;
    private float sinkO;
    private float sinkC;
    private float spinO;
    private float spinC;

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

    public float sink() {
        return this.entityData.get(DATA_SINK);
    }

    public float sink(float partialTicks) {
        return Mth.lerp(partialTicks, this.sinkO, this.sinkC);
    }

    public float spin(float partialTicks) {
        return Mth.lerp(partialTicks, this.spinO, this.spinC);
    }

    public boolean isEnraged() {
        return this.entityData.get(DATA_ENRAGED);
    }

    public int grabSide() {
        return this.entityData.get(DATA_GRAB_SIDE);
    }

    private void setSink(float sink) {
        this.entityData.set(DATA_SINK, Mth.clamp(sink, 0.0f, 1.0f));
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
        // er bleibt in seinem Schlund
        if (this.home != null && !this.level().isClientSide()) {
            Vec3 at = Vec3.atBottomCenterOf(this.home);
            if (this.position().distanceToSqr(at) > 0.01) {
                this.setPos(at);
            }
        }
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
            this.clientParticles();
        }
    }

    private void clientParticles() {
        Level level = this.level();
        if (this.sink() > 0.95f) {
            return;
        }
        // ringsum schäumt das Wasser um den Leib
        if (this.random.nextInt(2) == 0) {
            double a = this.random.nextDouble() * Math.PI * 2.0;
            double r = 5.5 + this.random.nextDouble() * 3.0;
            level.addParticle(ParticleTypes.SPLASH, this.getX() + Math.cos(a) * r, this.getY() + 0.9, this.getZ() + Math.sin(a) * r, 0.0, 0.1, 0.0);
        }
        if (this.random.nextInt(4) == 0) {
            level.addParticle(ModParticles.SEA_GLOW.get(), this.getRandomX(1.0), this.getY() + 2.0 + this.random.nextDouble() * 13.0,
                    this.getRandomZ(1.0), 0.0, 0.02, 0.0);
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
            if (++this.lonely > 600) {
                this.dive(level);
            }
            return;
        }
        this.lonely = 0;
        this.face(target.position(), 4.0f);
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
        if (hp < 0.67f) {
            options.add(Attack.WHIRL);
        }
        if (this.enraged) {
            options.add(Attack.WAVE);
            options.add(Attack.WAVE);
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
            default -> {
            }
        }
    }

    /**
     * Für Tests und die Vorführung: er greift nur noch auf Befehl an ({@code hold}) und richtet seine Angriffe auf
     * {@code target}. {@code name}: slam, sweep, grab, ink, whirl, call, wave (sonst nur das Ziel setzen).
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

    /** Welcher Angriff gerade läuft (für Tests): none, rise, slam, sweep, grab, ink, whirl, call, wave. */
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
        int base = this.enraged ? 18 : this.healthFraction() < 0.67f ? 26 : 32;
        this.cooldown = base + this.random.nextInt(16);
    }

    private void tickAttack(ServerLevel level, @Nullable LivingEntity target) {
        int t = ++this.attackTick;
        switch (this.attack) {
            case SLAM -> this.tickSlam(level, target, t);
            case SWEEP -> this.tickSweep(level, t);
            case GRAB -> this.tickGrab(level, target, t);
            case INK -> this.tickInk(level, target, t);
            case WHIRL -> this.tickWhirl(level, t);
            case CALL -> this.tickCall(level, target, t);
            case WAVE -> this.tickWave(level, t);
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
            this.waves.add(new int[] {0});
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
        for (int[] wave : this.waves) {
            wave[0]++;
            double r = 4.0 + wave[0] * 0.9;
            ring(level, this.position().add(0.0, 1.0, 0.0), r, ParticleTypes.SPLASH, (int) (r * 3));
            if (wave[0] % 2 == 0) {
                ring(level, this.position().add(0.0, 0.6, 0.0), r - 0.5, ParticleTypes.BUBBLE_POP, (int) (r * 2));
            }
            for (LivingEntity e : this.enemiesNear(level, r + 2.0)) {
                if (this.hits.contains(e.getUUID()) || Math.abs(e.getY() - this.getY()) > 4.0) {
                    continue;
                }
                double d = horizontalBetween(e.position(), this.position());
                if (Math.abs(d - r) < 1.6) {
                    this.hits.add(e.getUUID());
                    float damage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.9f;
                    if (e.hurtServer(level, this.damageSources().mobAttack(this), damage)) {
                        Vec3 away = e.position().subtract(this.position()).multiply(1.0, 0.0, 1.0).normalize();
                        e.push(away.x * 1.6, 0.7, away.z * 1.6);
                        e.needsSync = true;
                    }
                }
            }
        }
        this.waves.removeIf(w -> 4.0 + w[0] * 0.9 > WAVE_REACH);
    }

    // ------------------------------------------------------------------ Hilfen

    private boolean isEnemy(LivingEntity e) {
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
        return this.attack == Attack.RISE && this.sink() > 0.4f || super.isInvulnerableTo(level, source);
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
            default -> super.handleEntityEvent(id);
        }
    }

    /** Die gerufenen Haie ziehen wieder ab (wenn er stirbt oder hinabsinkt). */
    private void dismissSharks(ServerLevel level) {
        for (UUID id : this.sharks) {
            Entity shark = level.getEntity(id);
            if (shark != null && shark.isAlive()) {
                level.sendParticles(ParticleTypes.BUBBLE, shark.getX(), shark.getY() + 0.5, shark.getZ(), 20, 0.4, 0.4, 0.4, 0.05);
                shark.discard();
            }
        }
        this.sharks.clear();
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
