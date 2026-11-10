package io.github.sirschrolli.kingdomsatwar.entity.voidrealm;

import io.github.sirschrolli.kingdomsatwar.block.voidrealm.ThroneSealBlockEntity;
import io.github.sirschrolli.kingdomsatwar.magic.fx.SpellFxEntity;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import io.github.sirschrolli.kingdomsatwar.registry.VoidBlocks;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Der Zerbrochene König: der letzte König der Alten, der Boss der Leere. Er schwebt über seinem Thron der Alten, seine
 * Hände schweben frei neben ihm.
 * <ul>
 *     <li><b>Erwachen:</b> Wer den Thronplatz betritt, sieht die Trümmer zusammenfliegen – der König erhebt sich.</li>
 *     <li><b>Faustschlag:</b> eine Hand steigt über ihr Ziel (ein leuchtender Kreis warnt) und schlägt herab.</li>
 *     <li><b>Wischhieb:</b> beide Hände fegen in einem Bogen vor ihm über den Platz.</li>
 *     <li><b>Kronensplitter:</b> die Krone schleudert einen Fächer aus Kristallsplittern.</li>
 *     <li><b>Schwerkraftbruch</b> (ab 60 %): die Schwere bricht – alle schweben empor und stürzen, wenn seine Hände
 *     zusammenschlagen.</li>
 *     <li><b>Ruf der Wächter</b> (bei 60 % und 25 %): zwei Splitterwächter erheben sich aus dem Platz.</li>
 *     <li><b>Sternenstrahl</b> (unter 25 %): aus seinem zersprungenen Herzen fegt ein Strahl im Kreis über den Platz –
 *     über ihn springen oder hinter Säulen ducken.</li>
 *     <li><b>Risse der Leere</b>: er gräbt die Klauen vor sich in den Boden und reißt ihn auf – Risse aus violettem Licht
 *     laufen fächerförmig auf sein Ziel zu (im Zorn fünf statt drei), dann bricht die Leere Säule um Säule aus ihnen
 *     hervor, von ihm fort; wer auf einem Riss steht, wird emporgeschleudert. Die Risse enden an Säulen und Wänden.</li>
 *     <li><b>Leerenbeben</b>: beide Fäuste hoch über dem Haupt, dann rammt er sie in den Boden – Ringe aus Leerenenergie
 *     rollen über den ganzen Platz (im Zorn vier statt drei); wer nicht im rechten Augenblick darüber springt (oder auf
 *     einem Block steht), wird getroffen und zurückgeworfen.</li>
 * </ul>
 * Er ist bewusst schwächer als die Erzmagier (und die Drachen-Bosse sind die stärksten von allen).
 */
public class ShatteredKingEntity extends Monster implements io.github.sirschrolli.kingdomsatwar.arena.BossLair.Bound {
    public static final byte EVENT_AWAKEN = 90;
    public static final byte EVENT_SLAM_LEFT = 91;
    public static final byte EVENT_SLAM_RIGHT = 92;
    public static final byte EVENT_SWEEP = 93;
    public static final byte EVENT_VOLLEY = 94;
    public static final byte EVENT_GRAVITY = 95;
    public static final byte EVENT_SUMMON = 96;
    public static final byte EVENT_BEAM = 97;
    public static final byte EVENT_RIFT = 98;
    public static final byte EVENT_QUAKE = 99;

    private static final EntityDataAccessor<Float> DATA_BEAM_YAW = SynchedEntityData.defineId(ShatteredKingEntity.class,
            EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> DATA_BEAMING = SynchedEntityData.defineId(ShatteredKingEntity.class,
            EntityDataSerializers.BOOLEAN);
    /** Wohin der nächste Faustschlag fällt (damit die Hand im Bild genau dort aufschlägt). */
    private static final EntityDataAccessor<org.joml.Vector3fc> DATA_SLAM_POINT = SynchedEntityData.defineId(ShatteredKingEntity.class,
            EntityDataSerializers.VECTOR3);

    public static final int AWAKEN_TICKS = 70;
    public static final int SLAM_TICKS = 34;
    public static final int SLAM_IMPACT = 22;
    public static final int SWEEP_TICKS = 30;
    public static final int SWEEP_HIT = 16;
    public static final int VOLLEY_TICKS = 36;
    public static final int GRAVITY_TICKS = 70;
    public static final int GRAVITY_DROP = 52;
    public static final int SUMMON_TICKS = 50;
    public static final int BEAM_TICKS = 110;
    /** Risse der Leere: Dauer, in diesem Tick reißt der Boden auf, in diesem bricht die Leere hervor. */
    public static final int RIFT_TICKS = 64;
    public static final int RIFT_TEAR = 18;
    public static final int RIFT_ERUPT = 36;
    /** So lang wird ein Riss (Blöcke), so weit liegen seine Ausbrüche auseinander. */
    public static final double RIFT_LENGTH = 22.0;
    public static final double RIFT_SPACING = 1.6;
    /** Leerenbeben: Dauer, der erste Schlag, der Abstand der Ringe, ihre Geschwindigkeit (Blöcke je Tick) und Lebensdauer. */
    public static final int QUAKE_TICKS = 72;
    public static final int QUAKE_SLAM = 22;
    public static final int QUAKE_STEP = 10;
    public static final float QUAKE_SPEED = 0.55f;
    public static final int QUAKE_LIFE = 42;
    public static final int DEATH_TICKS = 80;
    /** So weit (waagerecht) reicht der Thronplatz um die Mitte. */
    public static final double ARENA_RADIUS = 24.0;
    /** Länge des Sternenstrahls (er endet früher, wo er auf Stein trifft). */
    public static final double BEAM_RANGE = 26.0;

    public final AnimationState awakenAnimation = new AnimationState();
    public final AnimationState slamLeftAnimation = new AnimationState();
    public final AnimationState slamRightAnimation = new AnimationState();
    public final AnimationState sweepAnimation = new AnimationState();
    public final AnimationState volleyAnimation = new AnimationState();
    public final AnimationState gravityAnimation = new AnimationState();
    public final AnimationState summonAnimation = new AnimationState();
    public final AnimationState beamAnimation = new AnimationState();
    public final AnimationState riftAnimation = new AnimationState();
    public final AnimationState quakeAnimation = new AnimationState();
    private static final int RIFT_COLOR = 0xA070FF;
    /** Die Säulen aus Leere sind tiefer gefärbt (dicht an dicht überstrahlen sie sonst zu Weiß). */
    private static final int RIFT_PILLAR_COLOR = 0x6A2CE0;
    private static final int QUAKE_COLOR = 0xB98CFF;
    private static final DustParticleOptions RIFT_DUST = new DustParticleOptions(0xB98CFF, 1.3f);

    private final ServerBossEvent bossBar = new ServerBossEvent(java.util.UUID.randomUUID(),
            Component.translatable("boss.kingdomsatwar.void"), BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.NOTCHED_10);

    private enum Attack { NONE, AWAKEN, SLAM, SWEEP, VOLLEY, GRAVITY, SUMMON, BEAM, RIFT, QUAKE }

    private Attack attack = Attack.NONE;
    private int attackTick;
    private int cooldown = 30;
    private boolean leftHand;
    private @Nullable Vec3 slamPoint;
    private float beamStart;
    private int summons;
    /** Ob der laufende Faustschlag schon der zweite einer Doppelfaust ist (im Zorn). */
    private boolean chained;
    /** Zorn: unter der Hälfte seiner Leben (einmal ausgerufen). */
    private boolean wrath;
    /** Risse der Leere: die Punkte jedes Risses (am Boden, von ihm fort) und wen die Leere schon emporgeschleudert hat. */
    private final List<Vec3[]> rifts = new ArrayList<>();
    private final Set<UUID> riftStruck = new HashSet<>();
    /** Wo seine Fäuste beim Leerenbeben (oder seine Klauen bei den Rissen) in den Boden fahren. */
    private Vec3 groundPoint = Vec3.ZERO;
    private int lonely;
    private @Nullable BlockPos home;
    private @Nullable BlockPos seal;
    /** Die gerufenen Splitterwächter: sie bleiben, solange der König lebt, und zerfallen mit ihm. */
    private final List<java.util.UUID> guards = new ArrayList<>();
    /** Für Tests und die Vorführung: ein festes Ziel und keine selbst gewählten Angriffe (siehe {@link #forceAttack}). */
    private @Nullable LivingEntity debugTarget;
    private boolean debugHold;
    /** Richtung des Strahls im Bild (weich zwischen den Ticks). */
    private float beamYawClient;
    private float beamYawClientO;

    public ShatteredKingEntity(EntityType<? extends ShatteredKingEntity> type, Level level) {
        super(type, level);
        this.xpReward = 500;
        this.setNoGravity(true);
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                // seit 0.61 der stärkste Gegner vor dem Abgrund: mehr Leben als jeder Drache (Nyxar 1400), schwer gepanzert
                .add(Attributes.MAX_HEALTH, 1800.0)
                .add(Attributes.ARMOR, 18.0)
                .add(Attributes.ARMOR_TOUGHNESS, 10.0)
                .add(Attributes.ATTACK_DAMAGE, 12.0)
                .add(Attributes.FOLLOW_RANGE, 48.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(Attributes.FLYING_SPEED, 0.4)
                .add(Attributes.MOVEMENT_SPEED, 0.3);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_BEAM_YAW, 0.0f);
        builder.define(DATA_BEAMING, false);
        builder.define(DATA_SLAM_POINT, new org.joml.Vector3f());
    }

    /** Wo sein Thronsiegel steht (für die Kampfdimension und das Zurücksetzen nach dem Tod seiner Gegner). */
    @Override
    public @Nullable BlockPos lairPos() {
        return this.seal;
    }

    /** Verknüpft den König mit seinem Thron (Mitte des Platzes) und dem Siegel darunter. */
    public void bindTo(BlockPos home, BlockPos seal) {
        this.home = home.immutable();
        this.seal = seal.immutable();
    }

    public @Nullable BlockPos home() {
        return this.home;
    }

    public boolean isBeaming() {
        return this.entityData.get(DATA_BEAMING);
    }

    /** Richtung des Sternenstrahls (Grad, wie die Blickrichtung). */
    public float beamYaw() {
        return this.entityData.get(DATA_BEAM_YAW);
    }

    /** Richtung des Sternenstrahls im Bild, weich zwischen zwei Ticks. */
    public float beamYaw(float partialTicks) {
        return Mth.rotLerp(partialTicks, this.beamYawClientO, this.beamYawClient);
    }

    /** Wohin der laufende (oder letzte) Faustschlag fällt. */
    public Vec3 slamPoint() {
        org.joml.Vector3fc v = this.entityData.get(DATA_SLAM_POINT);
        return new Vec3(v.x(), v.y(), v.z());
    }

    /** Neigung des Sternenstrahls nach unten (Anteil je Block). */
    public static final double BEAM_DROP = 0.42;

    /** Richtung des Sternenstrahls bei {@code yawDegrees}. */
    public static Vec3 beamDirection(float yawDegrees) {
        float yaw = yawDegrees * Mth.DEG_TO_RAD;
        return new Vec3(-Mth.sin(yaw), -BEAM_DROP, Mth.cos(yaw)).normalize();
    }

    /** Lässt den König erwachen: erst unverwundbar, die Trümmer fliegen zusammen. */
    public void beginAwakening(ServerLevel level) {
        this.attack = Attack.AWAKEN;
        this.attackTick = 0;
        level.broadcastEntityEvent(this, EVENT_AWAKEN);
        level.playSound(null, this.blockPosition(), ModSounds.SHATTERED_KING_AWAKEN.get(), SoundSource.HOSTILE, 4.0f, 1.0f);
    }

    public boolean isAwakening() {
        return this.attack == Attack.AWAKEN;
    }

    // ------------------------------------------------------------------ Schweben

    @Override
    public void travel(Vec3 input) {
        this.travelFlying(input, 0.02f);
    }

    @Override
    protected void checkFallDamage(double ya, boolean onGround, BlockState onState, BlockPos pos) {
    }

    @Override
    public boolean onClimbable() {
        return false;
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

    // ------------------------------------------------------------------ Bossleiste

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

    // ------------------------------------------------------------------ Ablauf

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide()) {
            this.beamYawClientO = this.isBeaming() ? this.beamYawClient : this.beamYaw();
            this.beamYawClient = this.beamYaw();
            this.clientParticles();
        }
    }

    private void clientParticles() {
        if (this.random.nextInt(3) == 0) {
            // Funken aus der zersprungenen Brust
            float yaw = this.yBodyRot * Mth.DEG_TO_RAD;
            double fx = -Mth.sin(yaw) * 0.6;
            double fz = Mth.cos(yaw) * 0.6;
            this.level().addParticle(ParticleTypes.END_ROD, this.getX() + fx, this.getY() + 3.8, this.getZ() + fz, fx * 0.05, 0.02, fz * 0.05);
        }
        if (this.random.nextInt(4) == 0) {
            this.level().addParticle(ParticleTypes.REVERSE_PORTAL, this.getRandomX(1.2), this.getY() + this.random.nextDouble() * 1.4,
                    this.getRandomZ(1.2), 0.0, -0.03, 0.0);
        }
        if (this.isBeaming()) {
            Vec3 from = this.heartPosition();
            Vec3 dir = beamDirection(this.beamYaw());
            for (int i = 0; i < 6; i++) {
                double d = this.random.nextDouble() * 18.0;
                Vec3 p = from.add(dir.scale(d));
                this.level().addParticle(i % 3 == 0 ? ParticleTypes.END_ROD : ParticleTypes.REVERSE_PORTAL, p.x, p.y, p.z, 0.0, 0.0, 0.0);
            }
        }
    }

    /** Wo das Herz in der Brust schwebt (Ursprung des Sternenstrahls). */
    public Vec3 heartPosition() {
        return heartPosition(this.position(), this.yBodyRot);
    }

    /** Lage des Herzens bei {@code position} und Körperdrehung {@code bodyYaw} (auch für die Darstellung). */
    public static Vec3 heartPosition(Vec3 position, float bodyYaw) {
        float yaw = bodyYaw * Mth.DEG_TO_RAD;
        return position.add(-Mth.sin(yaw) * 0.35, 3.8, Mth.cos(yaw) * 0.35);
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        this.bossBar.setProgress(this.getHealth() / this.getMaxHealth());
        if (this.home == null) {
            // ohne Thron (z. B. aus dem Spawn-Ei): wo er erscheint, ist sein Platz
            this.home = this.blockPosition();
        }
        if (this.attack == Attack.AWAKEN) {
            this.tickAwakening(level);
            return;
        }
        LivingEntity target = this.debugTarget != null && this.debugTarget.isAlive() ? this.debugTarget : this.pickTarget(level);
        this.setTarget(target);
        if (this.attack != Attack.NONE) {
            // ein begonnener Angriff wird zu Ende geführt, auch wenn sein Ziel inzwischen gefallen ist
            this.lonely = 0;
            this.tickAttack(level, target);
            return;
        }
        if (this.debugHold) {
            // Vorführung/Test: an seinem Platz schweben und nur auf Befehl angreifen
            this.hover(Vec3.atBottomCenterOf(this.home).add(0.0, 2.5, 0.0), target);
            return;
        }
        if (target == null) {
            // niemand mehr auf dem Platz: nach einer halben Minute zieht er sich zurück
            this.hover(Vec3.atBottomCenterOf(this.home).add(0.0, 5.0, 0.0), null);
            if (++this.lonely > 600 && this.seal != null) {
                this.slumber(level);
            }
            return;
        }
        this.lonely = 0;
        // Abstand halten: vor seinem Ziel schweben, etwa sechs Blöcke entfernt, ein Stück über dem Boden
        Vec3 away = this.position().subtract(target.position()).multiply(1.0, 0.0, 1.0);
        away = away.lengthSqr() < 1.0e-4 ? new Vec3(0.0, 0.0, 1.0) : away.normalize();
        Vec3 wanted = target.position().add(away.scale(6.0)).add(0.0, 2.5, 0.0);
        this.hover(this.clampToArena(wanted), target);
        if (--this.cooldown <= 0) {
            this.chooseAttack(level, target);
        }
    }

    /** Ziel: der nächste Spieler (oder Soldat) auf dem Thronplatz, der angreifbar ist. */
    private @Nullable LivingEntity pickTarget(ServerLevel level) {
        BlockPos h = this.home;
        if (h == null) {
            return null;
        }
        AABB arena = new AABB(h).inflate(ARENA_RADIUS + 6, 20, ARENA_RADIUS + 6);
        List<LivingEntity> candidates = new ArrayList<>(level.getEntitiesOfClass(Player.class, arena,
                p -> p.isAlive() && !p.isSpectator() && !p.isCreative()));
        if (candidates.isEmpty()) {
            candidates.addAll(level.getEntitiesOfClass(io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierEntity.class, arena,
                    LivingEntity::isAlive));
        }
        LivingEntity current = this.getTarget();
        if (current != null && current.isAlive() && candidates.contains(current) && this.random.nextInt(80) != 0) {
            return current;
        }
        return candidates.stream().min(Comparator.comparingDouble(this::distanceToSqr)).orElse(null);
    }

    private Vec3 clampToArena(Vec3 wanted) {
        if (this.home == null) {
            return wanted;
        }
        Vec3 c = Vec3.atBottomCenterOf(this.home);
        Vec3 d = wanted.subtract(c).multiply(1.0, 0.0, 1.0);
        double max = ARENA_RADIUS - 4.0;
        if (d.length() > max) {
            d = d.normalize().scale(max);
        }
        double y = Mth.clamp(wanted.y, c.y + 2.0, c.y + 9.0);
        return new Vec3(c.x + d.x, y, c.z + d.z);
    }

    /** Gleitet zum Wunschpunkt und blickt dabei sein Ziel an. */
    private void hover(Vec3 wanted, @Nullable LivingEntity look) {
        Vec3 delta = wanted.subtract(this.position());
        double length = delta.length();
        Vec3 v = this.getDeltaMovement();
        Vec3 want = length < 0.3 ? Vec3.ZERO : delta.scale(Math.min(0.22, length * 0.08) / length);
        // sanftes Wogen auf und ab
        want = want.add(0.0, Mth.sin(this.tickCount * 0.06f) * 0.015, 0.0);
        this.setDeltaMovement(v.add(want.subtract(v).scale(0.12)));
        double lx = look != null ? look.getX() - this.getX() : delta.x;
        double lz = look != null ? look.getZ() - this.getZ() : delta.z;
        if (lx * lx + lz * lz > 1.0e-3) {
            float yaw = -((float) Mth.atan2(lx, lz)) * Mth.RAD_TO_DEG;
            this.setYRot(Mth.approachDegrees(this.getYRot(), yaw, 5.0f));
            this.yBodyRot = this.getYRot();
            this.yHeadRot = this.getYRot();
        }
    }

    private void tickAwakening(ServerLevel level) {
        this.attackTick++;
        this.setDeltaMovement(0.0, 0.035, 0.0);
        if (this.attackTick % 6 == 0) {
            // die Trümmer fliegen von allen Seiten zusammen
            for (int i = 0; i < 12; i++) {
                double a = this.random.nextDouble() * Math.PI * 2.0;
                double r = 6.0 + this.random.nextDouble() * 6.0;
                Vec3 from = this.position().add(Math.cos(a) * r, this.random.nextDouble() * 5.0, Math.sin(a) * r);
                Vec3 v = this.position().add(0.0, 3.0, 0.0).subtract(from).scale(0.08);
                level.sendParticles(ParticleTypes.REVERSE_PORTAL, from.x, from.y, from.z, 0, v.x, v.y, v.z, 1.0);
            }
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, VoidBlocks.ASTRAL_MARBLE.base().get().defaultBlockState()),
                    this.getX(), this.getY() + 2.5, this.getZ(), 12, 1.4, 1.6, 1.4, 0.05);
        }
        if (this.attackTick == AWAKEN_TICKS - 14) {
            // der Schrei: Name und Titel für alle in der Nähe, der Boden bebt
            level.playSound(null, this.blockPosition(), ModSounds.SHATTERED_KING_ROAR.get(), SoundSource.HOSTILE, 4.0f, 0.9f);
            Component title = Component.translatable("boss.kingdomsatwar.void").withColor(0xD8C4FF);
            Component subtitle = Component.translatable("boss.kingdomsatwar.void.title").withStyle(net.minecraft.ChatFormatting.GRAY);
            for (ServerPlayer player : level.players()) {
                if (player.distanceToSqr(this) < 80.0 * 80.0) {
                    player.connection.send(new net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket(20, 70, 30));
                    player.connection.send(new net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket(title));
                    player.connection.send(new net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket(subtitle));
                }
            }
            this.shake(level, 60.0, 0.55f, 30);
        }
        if (this.attackTick >= AWAKEN_TICKS) {
            this.attack = Attack.NONE;
            this.cooldown = 30;
            level.sendParticles(ParticleTypes.END_ROD, this.getX(), this.getY() + 3.0, this.getZ(), 60, 1.6, 2.0, 1.6, 0.12);
        }
    }

    @Override
    public boolean isInvulnerableTo(ServerLevel level, DamageSource source) {
        return this.attack == Attack.AWAKEN || super.isInvulnerableTo(level, source);
    }

    // ------------------------------------------------------------------ Angriffe

    private float healthFraction() {
        return this.getHealth() / this.getMaxHealth();
    }

    private void chooseAttack(ServerLevel level, LivingEntity target) {
        float hp = this.healthFraction();
        double d = this.distanceTo(target);
        if (!this.wrath && hp < 0.5f) {
            // Zorn: ein gewaltiger Schrei, die Erde bebt – ab jetzt schlägt er schneller und doppelt zu
            this.wrath = true;
            level.playSound(null, this.blockPosition(), ModSounds.SHATTERED_KING_ROAR.get(), SoundSource.HOSTILE, 4.0f, 0.85f);
            this.shake(level, 48.0, 1.4f, 40);
            Vec3 h = this.heartPosition();
            level.sendParticles(ParticleTypes.REVERSE_PORTAL, h.x, h.y, h.z, 120, 2.5, 2.5, 2.5, 0.4);
            for (ServerPlayer player : level.players()) {
                if (player.distanceTo(this) < 64.0) {
                    player.sendOverlayMessage(net.minecraft.network.chat.Component.translatable("message.kingdomsatwar.shattered_king.wrath")
                            .withStyle(net.minecraft.ChatFormatting.LIGHT_PURPLE, net.minecraft.ChatFormatting.BOLD));
                }
            }
            this.cooldown = 30;
            return;
        }
        // die Wächter rufen: unter 70 %, unter 45 % und noch einmal unter 20 %
        if (this.summons == 0 && hp < 0.7f || this.summons == 1 && hp < 0.45f || this.summons == 2 && hp < 0.2f) {
            this.summons++;
            this.start(level, Attack.SUMMON, EVENT_SUMMON);
            return;
        }
        List<Attack> options = new ArrayList<>(List.of(Attack.SLAM, Attack.SLAM, Attack.VOLLEY, Attack.RIFT, Attack.QUAKE));
        if (d < 9.0) {
            options.add(Attack.SWEEP);
            options.add(Attack.SWEEP);
        }
        if (hp < 0.7f) {
            options.add(Attack.GRAVITY);
        }
        if (hp < 0.5f) {
            // Zorn: der Schwerkraftbruch kommt öfter, der Sternenstrahl kommt dazu
            options.add(Attack.GRAVITY);
            options.add(Attack.BEAM);
        }
        if (hp < 0.25f) {
            options.add(Attack.BEAM);
        }
        Attack next = options.get(this.random.nextInt(options.size()));
        switch (next) {
            case SLAM -> {
                this.leftHand = !this.leftHand;
                this.slamPoint = target.position();
                this.entityData.set(DATA_SLAM_POINT, this.slamPoint.toVector3f());
                this.start(level, Attack.SLAM, this.leftHand ? EVENT_SLAM_LEFT : EVENT_SLAM_RIGHT);
            }
            case SWEEP -> this.start(level, Attack.SWEEP, EVENT_SWEEP);
            case VOLLEY -> this.start(level, Attack.VOLLEY, EVENT_VOLLEY);
            case GRAVITY -> this.start(level, Attack.GRAVITY, EVENT_GRAVITY);
            case BEAM -> {
                this.beamStart = this.getYRot() - 100.0f;
                this.start(level, Attack.BEAM, EVENT_BEAM);
            }
            case RIFT -> this.startGroundAttack(level, Attack.RIFT, EVENT_RIFT, 4.0);
            case QUAKE -> this.startGroundAttack(level, Attack.QUAKE, EVENT_QUAKE, 3.5);
            default -> {
            }
        }
    }

    /** Risse oder Beben: die Stelle vor ihm am Boden, wo seine Hände hineinfahren, steht fest, bevor sie sich heben. */
    private void startGroundAttack(ServerLevel level, Attack attack, byte event, double ahead) {
        Vec3 forward = this.getLookAngle().multiply(1.0, 0.0, 1.0);
        forward = forward.lengthSqr() < 1.0e-4 ? new Vec3(0.0, 0.0, 1.0) : forward.normalize();
        Vec3 at = this.position().add(forward.scale(ahead));
        LivingEntity target = this.getTarget();
        this.groundPoint = new Vec3(at.x, this.floorBelow(level, at, target != null ? target.getY() : this.getY() - 2.5), at.z);
        this.entityData.set(DATA_SLAM_POINT, this.groundPoint.toVector3f());
        this.rifts.clear();
        this.riftStruck.clear();
        this.start(level, attack, event);
    }

    /**
     * Für Tests und die Vorführung: der König greift nur noch auf Befehl an ({@code hold}) und richtet seine Angriffe auf
     * {@code target}. {@code name}: slam_left, slam_right, sweep, volley, gravity, summon, beam (sonst nur Ziel setzen).
     */
    public void forceAttack(ServerLevel level, String name, @Nullable LivingEntity target, boolean hold) {
        this.debugTarget = target;
        this.debugHold = hold;
        if (target != null) {
            this.setTarget(target);
        }
        switch (name) {
            case "slam_left", "slam_right" -> {
                this.leftHand = name.equals("slam_left");
                this.slamPoint = target != null ? target.position()
                        : this.position().add(this.getLookAngle().multiply(1.0, 0.0, 1.0).normalize().scale(5.0)).add(0.0, -2.5, 0.0);
                this.entityData.set(DATA_SLAM_POINT, this.slamPoint.toVector3f());
                this.start(level, Attack.SLAM, this.leftHand ? EVENT_SLAM_LEFT : EVENT_SLAM_RIGHT);
            }
            case "sweep" -> this.start(level, Attack.SWEEP, EVENT_SWEEP);
            case "volley" -> this.start(level, Attack.VOLLEY, EVENT_VOLLEY);
            case "gravity" -> this.start(level, Attack.GRAVITY, EVENT_GRAVITY);
            case "summon" -> this.start(level, Attack.SUMMON, EVENT_SUMMON);
            case "beam" -> {
                this.beamStart = this.getYRot() - 100.0f;
                this.start(level, Attack.BEAM, EVENT_BEAM);
            }
            case "rift" -> {
                this.faceNow(target);
                this.startGroundAttack(level, Attack.RIFT, EVENT_RIFT, 4.0);
            }
            case "quake" -> {
                this.faceNow(target);
                this.startGroundAttack(level, Attack.QUAKE, EVENT_QUAKE, 3.5);
            }
            default -> {
            }
        }
    }

    /** Dreht sich sofort zu {@code target} (für Tests und die Vorführung). */
    private void faceNow(@Nullable LivingEntity target) {
        if (target != null) {
            float yaw = -((float) Mth.atan2(target.getX() - this.getX(), target.getZ() - this.getZ())) * Mth.RAD_TO_DEG;
            this.setYRot(yaw);
            this.yBodyRot = yaw;
            this.yHeadRot = yaw;
        }
    }

    /** Welcher Angriff gerade läuft (für Tests): none, awaken, slam, sweep, volley, gravity, summon, beam, rift, quake. */
    public String currentAttack() {
        return this.attack.name().toLowerCase(java.util.Locale.ROOT);
    }

    private void start(ServerLevel level, Attack attack, byte event) {
        this.attack = attack;
        this.attackTick = 0;
        level.broadcastEntityEvent(this, event);
    }

    private void finish() {
        float hp = this.healthFraction();
        // im Zorn (unter der Hälfte) folgt jedem Faustschlag sofort der Schlag der anderen Hand
        if (this.attack == Attack.SLAM && hp < 0.5f && !this.chained && this.getTarget() != null && this.level() instanceof ServerLevel level) {
            this.chained = true;
            LivingEntity target = this.getTarget();
            this.leftHand = !this.leftHand;
            this.slamPoint = target.position();
            this.entityData.set(DATA_SLAM_POINT, this.slamPoint.toVector3f());
            this.start(level, Attack.SLAM, this.leftHand ? EVENT_SLAM_LEFT : EVENT_SLAM_RIGHT);
            return;
        }
        this.chained = false;
        // in den späteren Phasen schlägt er schneller zu
        int base = hp < 0.25f ? 14 : hp < 0.5f ? 20 : 30;
        this.cooldown = base + this.random.nextInt(hp < 0.5f ? 14 : 18);
        this.attack = Attack.NONE;
        this.entityData.set(DATA_BEAMING, false);
    }

    private void tickAttack(ServerLevel level, @Nullable LivingEntity target) {
        int t = ++this.attackTick;
        switch (this.attack) {
            case SLAM -> this.tickSlam(level, target, t);
            case SWEEP -> this.tickSweep(level, target, t);
            case VOLLEY -> this.tickVolley(level, target, t);
            case GRAVITY -> this.tickGravity(level, t);
            case SUMMON -> this.tickSummon(level, t);
            case BEAM -> this.tickBeam(level, t);
            case RIFT -> this.tickRift(level, target, t);
            case QUAKE -> this.tickQuake(level, t);
            default -> this.finish();
        }
    }

    /** Faustschlag: die Hand steigt über die Stelle, an der das Ziel stand (ein Kreis warnt), und schlägt herab. */
    private void tickSlam(ServerLevel level, @Nullable LivingEntity target, int t) {
        if (this.slamPoint == null && target == null) {
            this.finish();
            return;
        }
        Vec3 p = this.slamPoint != null ? this.slamPoint : target.position();
        // vor der Stelle schweben, damit die Hand sie erreicht
        Vec3 away = this.position().subtract(p).multiply(1.0, 0.0, 1.0);
        away = away.lengthSqr() < 1.0e-4 ? new Vec3(0.0, 0.0, 1.0) : away.normalize();
        this.hover(this.clampToArena(p.add(away.scale(5.0)).add(0.0, 2.5, 0.0)), null);
        float yaw = -((float) Mth.atan2(p.x - this.getX(), p.z - this.getZ())) * Mth.RAD_TO_DEG;
        this.setYRot(Mth.approachDegrees(this.getYRot(), yaw, 8.0f));
        this.yBodyRot = this.getYRot();
        if (t < SLAM_IMPACT && t % 3 == 0) {
            ring(level, p, 3.2, t % 6 == 0 ? ParticleTypes.END_ROD : ParticleTypes.REVERSE_PORTAL);
        }
        if (t == 6) {
            level.playSound(null, p.x, p.y, p.z, ModSounds.SHARD_WARDEN_CHARGE.get(), SoundSource.HOSTILE, 1.6f, 0.6f);
        }
        if (t == SLAM_IMPACT) {
            this.impact(level, p, 3.6, (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE) * 1.45f, 0.7, 0.9);
        }
        if (t >= SLAM_TICKS) {
            this.finish();
        }
    }

    /** Wischhieb: beide Hände fegen im Bogen vor ihm über den Platz. */
    private void tickSweep(ServerLevel level, @Nullable LivingEntity target, int t) {
        this.hover(this.clampToArena(this.position()), target);
        if (t == 8) {
            level.playSound(null, this.blockPosition(), ModSounds.SHATTERED_KING_SWEEP.get(), SoundSource.HOSTILE, 2.0f, 0.9f);
        }
        if (t == SWEEP_HIT) {
            Vec3 forward = this.getLookAngle().multiply(1.0, 0.0, 1.0).normalize();
            float damage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE) * 1.15f;
            for (LivingEntity e : this.enemiesNear(level, 8.5)) {
                Vec3 to = e.position().subtract(this.position()).multiply(1.0, 0.0, 1.0);
                double dist = to.length();
                if (dist < 0.5 || to.normalize().dot(forward) < 0.15 || dist > 8.5) {
                    continue;
                }
                if (e.hurtServer(level, this.damageSources().mobAttack(this), damage)) {
                    Vec3 push = to.normalize().scale(1.3);
                    e.push(push.x, 0.45, push.z);
                    e.needsSync = true;
                }
            }
            Vec3 c = this.position().add(forward.scale(4.0));
            level.sendParticles(ParticleTypes.SWEEP_ATTACK, c.x, this.getY() + 0.8, c.z, 6, 2.5, 0.3, 2.5, 0.0);
            level.sendParticles(ParticleTypes.END_ROD, c.x, this.getY() + 0.8, c.z, 20, 3.0, 0.4, 3.0, 0.05);
        }
        if (t >= SWEEP_TICKS) {
            this.finish();
        }
    }

    /** Kronensplitter: ein Fächer aus fünf Kristallsplittern, in der letzten Phase sieben. */
    private void tickVolley(ServerLevel level, @Nullable LivingEntity target, int t) {
        this.hover(this.clampToArena(this.position()), target);
        if (t == 4) {
            level.playSound(null, this.blockPosition(), ModSounds.SHARD_WARDEN_CHARGE.get(), SoundSource.HOSTILE, 2.0f, 1.2f);
        }
        if (t == 18 || t == 26) {
            Vec3 from = this.position().add(0.0, 6.5, 0.0);
            Vec3 aim = target != null ? target.getEyePosition().subtract(from)
                    : this.getLookAngle().multiply(1.0, 0.0, 1.0).normalize().scale(10.0).add(0.0, -4.0, 0.0);
            int count = this.healthFraction() < 0.25f ? 9 : this.healthFraction() < 0.5f ? 7 : 5;
            float spread = 11.0f;
            for (int i = 0; i < count; i++) {
                float off = (i - (count - 1) / 2.0f) * spread * Mth.DEG_TO_RAD;
                Vec3 dir = aim.yRot(off);
                WardenShardEntity.shoot(level, this, from, dir, 1.05, (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.85f);
            }
            level.playSound(null, this.blockPosition(), ModSounds.SHARD_WARDEN_SHOOT.get(), SoundSource.HOSTILE, 2.0f, 0.8f);
        }
        if (t >= VOLLEY_TICKS) {
            this.finish();
        }
    }

    /** Schwerkraftbruch: alle Gegner auf dem Platz schweben empor – und stürzen, wenn die Hände zusammenschlagen. */
    private void tickGravity(ServerLevel level, int t) {
        this.hover(this.clampToArena(this.position()), this.getTarget());
        if (t == 10) {
            level.playSound(null, this.blockPosition(), ModSounds.SHATTERED_KING_GRAVITY.get(), SoundSource.HOSTILE, 3.0f, 1.0f);
            for (LivingEntity e : this.enemiesNear(level, ARENA_RADIUS + 4)) {
                e.addEffect(new MobEffectInstance(MobEffects.LEVITATION, GRAVITY_DROP - 10, 2), this);
                level.sendParticles(ParticleTypes.REVERSE_PORTAL, e.getX(), e.getY(), e.getZ(), 20, 0.4, 0.2, 0.4, 0.05);
            }
        }
        if (t == GRAVITY_DROP) {
            for (LivingEntity e : this.enemiesNear(level, ARENA_RADIUS + 12)) {
                e.removeEffect(MobEffects.LEVITATION);
                e.push(0.0, -1.2, 0.0);
                e.needsSync = true;
            }
            level.playSound(null, this.blockPosition(), ModSounds.SHARD_WARDEN_SLAM.get(), SoundSource.HOSTILE, 3.0f, 0.5f);
            level.sendParticles(ParticleTypes.END_ROD, this.getX(), this.getY() + 2.0, this.getZ(), 40, 1.0, 1.0, 1.0, 0.25);
            this.shake(level, 40.0, 0.5f, 14);
        }
        if (t >= GRAVITY_TICKS) {
            this.finish();
        }
    }

    /** Ruf der Wächter: zwei Splitterwächter erheben sich aus dem Platz. */
    private void tickSummon(ServerLevel level, int t) {
        this.hover(this.clampToArena(this.position()), this.getTarget());
        if (t == 20 && this.home != null) {
            level.playSound(null, this.blockPosition(), ModSounds.SHARD_WARDEN_AWAKEN.get(), SoundSource.HOSTILE, 3.0f, 0.8f);
            int placed = 0;
            int wanted = this.summons >= 2 ? 3 : 2;
            for (int attempt = 0; attempt < 30 && placed < wanted; attempt++) {
                double a = this.random.nextDouble() * Math.PI * 2.0;
                // erst in einem weiten Ring um den Thron, auf engem Raum näher heran
                double r = attempt < 12 ? 4.5 + this.random.nextDouble() * 4.0 : 2.0 + this.random.nextDouble() * 2.5;
                BlockPos at = this.home.offset((int) Math.round(Math.cos(a) * r), 0, (int) Math.round(Math.sin(a) * r));
                for (int dy = 4; dy >= -3; dy--) {
                    BlockPos feet = at.above(dy);
                    if (level.getBlockState(feet.below()).isSolid() && level.getBlockState(feet).isAir() && level.getBlockState(feet.above()).isAir()
                            && level.getBlockState(feet.above(2)).isAir()) {
                        ShardWardenEntity warden = ModEntities.SHARD_WARDEN.get().create(level, EntitySpawnReason.MOB_SUMMONED);
                        if (warden != null) {
                            warden.snapTo(feet.getX() + 0.5, feet.getY(), feet.getZ() + 0.5, this.random.nextFloat() * 360.0f, 0.0f);
                            warden.finalizeSpawn(level, level.getCurrentDifficultyAt(feet), EntitySpawnReason.MOB_SUMMONED, null);
                            warden.setDormant(true);
                            warden.setTarget(this.getTarget());
                            warden.setPersistenceRequired();
                            level.addFreshEntity(warden);
                            this.guards.add(warden.getUUID());
                            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, VoidBlocks.ASTRAL_MARBLE.base().get().defaultBlockState()),
                                    feet.getX() + 0.5, feet.getY() + 0.3, feet.getZ() + 0.5, 40, 0.6, 0.4, 0.6, 0.12);
                            placed++;
                        }
                        break;
                    }
                }
            }
        }
        if (t >= SUMMON_TICKS) {
            this.finish();
        }
    }

    /** Sternenstrahl: aus dem Herzen fegt ein Strahl in einem weiten Bogen über den Platz. */
    private void tickBeam(ServerLevel level, int t) {
        this.setDeltaMovement(this.getDeltaMovement().scale(0.8));
        int charge = 30;
        if (t == 2) {
            level.playSound(null, this.blockPosition(), ModSounds.SHARD_WARDEN_CHARGE.get(), SoundSource.HOSTILE, 3.0f, 0.5f);
        }
        if (t < charge) {
            Vec3 h = this.heartPosition();
            level.sendParticles(ParticleTypes.REVERSE_PORTAL, h.x, h.y, h.z, 6, 0.6, 0.6, 0.6, 0.1);
            this.entityData.set(DATA_BEAM_YAW, this.beamStart);
            return;
        }
        if (t == charge) {
            this.entityData.set(DATA_BEAMING, true);
            level.playSound(null, this.blockPosition(), ModSounds.SHATTERED_KING_BEAM.get(), SoundSource.HOSTILE, 3.0f, 1.0f);
        }
        float progress = (t - charge) / (float) (BEAM_TICKS - charge);
        float yaw = this.beamStart + progress * 200.0f;
        this.entityData.set(DATA_BEAM_YAW, yaw);
        this.setYRot(yaw);
        this.yBodyRot = yaw;
        this.yHeadRot = yaw;
        if (t % 2 == 0) {
            Vec3 from = this.heartPosition();
            Vec3 dir = beamDirection(yaw);
            Vec3 end = from.add(dir.scale(BEAM_RANGE));
            HitResult hit = level.clip(new ClipContext(from, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
            Vec3 stop = hit.getType() == HitResult.Type.MISS ? end : hit.getLocation();
            double reach = stop.distanceTo(from);
            float damage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.9f;
            for (LivingEntity e : this.enemiesNear(level, 26.0)) {
                Vec3 c = e.position().add(0.0, e.getBbHeight() * 0.5, 0.0).subtract(from);
                double along = c.dot(dir);
                if (along < 0.0 || along > reach) {
                    continue;
                }
                double off = c.subtract(dir.scale(along)).length();
                if (off < 0.9 + e.getBbWidth() * 0.5) {
                    if (e.hurtServer(level, this.damageSources().indirectMagic(this, this), damage)) {
                        e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 30, 1), this);
                    }
                }
            }
            level.sendParticles(ParticleTypes.END_ROD, stop.x, stop.y + 0.1, stop.z, 4, 0.2, 0.1, 0.2, 0.08);
        }
        if (t >= BEAM_TICKS) {
            this.finish();
        }
    }

    /**
     * Risse der Leere: er gräbt die Klauen vor sich in den Boden und reißt ihn auf – Risse aus violettem Licht laufen
     * fächerförmig auf sein Ziel zu. Kurz darauf bricht die Leere aus ihnen hervor, Säule um Säule, von ihm fort.
     */
    private void tickRift(ServerLevel level, @Nullable LivingEntity target, int t) {
        this.hover(this.clampToArena(this.position()), target);
        if (t == 4) {
            level.playSound(null, this.groundPoint.x, this.groundPoint.y, this.groundPoint.z, ModSounds.SHATTERED_KING_RIFT.get(),
                    SoundSource.HOSTILE, 3.0f, 1.0f);
        }
        if (t == RIFT_TEAR) {
            this.tearRifts(level, target);
        }
        if (t > RIFT_TEAR && t < RIFT_ERUPT && t % 2 == 0) {
            // die Risse glimmen und knistern
            for (Vec3[] line : this.rifts) {
                for (Vec3 p : line) {
                    level.sendParticles(t % 4 == 0 ? ParticleTypes.REVERSE_PORTAL : RIFT_DUST, p.x, p.y + 0.12, p.z, 1, 0.2, 0.02, 0.2, 0.01);
                }
            }
        }
        int k = t - RIFT_ERUPT;
        if (k >= 0 && !this.rifts.isEmpty()) {
            List<LivingEntity> near = this.enemiesNear(level, RIFT_LENGTH + 8.0);
            for (Vec3[] line : this.rifts) {
                if (k < line.length) {
                    this.erupt(level, line[k], near, k == 0);
                }
            }
        }
        if (t >= RIFT_TICKS) {
            this.rifts.clear();
            this.finish();
        }
    }

    /** Die Risse: vom Punkt vor ihm fächerförmig auf sein Ziel zu, über den Boden, bis zu einer Säule, Wand oder Kante. */
    private void tearRifts(ServerLevel level, @Nullable LivingEntity target) {
        this.rifts.clear();
        this.riftStruck.clear();
        Vec3 origin = this.groundPoint;
        Vec3 aim = target != null ? target.position().subtract(origin) : this.getLookAngle();
        double base = Math.atan2(aim.z, aim.x);
        int count = this.healthFraction() < 0.5f ? 5 : 3;
        double spread = 21.0 * Mth.DEG_TO_RAD;
        BlockState ground = level.getBlockState(BlockPos.containing(origin).below());
        if (!ground.isAir()) {
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, ground), origin.x, origin.y + 0.2, origin.z, 40, 1.2, 0.2, 1.2, 0.2);
        }
        this.shake(level, 30.0, 0.35f, 10);
        for (int i = 0; i < count; i++) {
            double a = base + (i - (count - 1) / 2.0) * spread;
            Vec3 dir = new Vec3(Math.cos(a), 0.0, Math.sin(a));
            Vec3 side = new Vec3(-dir.z, 0.0, dir.x);
            List<Vec3> points = new ArrayList<>();
            double y = origin.y;
            double wobble = 0.0;
            for (double d = 1.0; d <= RIFT_LENGTH; d += RIFT_SPACING) {
                // ein Riss ist nie gerade: er zuckt hin und her
                wobble = Mth.clamp(wobble + (this.random.nextDouble() - 0.5) * 0.9, -1.0, 1.0);
                Vec3 p = origin.add(dir.scale(d)).add(side.scale(wobble));
                Double floor = floorAt(level, p.x, p.z, y);
                if (floor == null) {
                    break;
                }
                y = floor;
                points.add(new Vec3(p.x, y, p.z));
            }
            if (points.size() < 2) {
                continue;
            }
            Vec3[] line = points.toArray(new Vec3[0]);
            this.rifts.add(line);
            // der Boden reißt auf: Stück um Stück, von ihm fort – bis die Leere aus dem Spalt hervorbricht
            Vec3 from = origin;
            int piece = 0;
            for (int j = 2; j < line.length + 2; j += 3) {
                int end = Math.min(j, line.length - 1);
                Vec3 to = line[end];
                SpellFxEntity.create(level, SpellFxEntity.Kind.FISSURE, from, null, RIFT_COLOR, RIFT_ERUPT - RIFT_TEAR + end + 5, 0.85f)
                        .end(to).param(piece * 2.0f).spawn();
                from = to;
                piece++;
                if (j >= line.length - 1) {
                    break;
                }
            }
            // ein knisternder Blitz läuft den Riss entlang
            SpellFxEntity.create(level, SpellFxEntity.Kind.ARC, origin.add(0.0, 0.25, 0.0), null, RIFT_COLOR, 8, 0.18f)
                    .end(line[line.length - 1].add(0.0, 0.25, 0.0)).spawn();
        }
    }

    /** Die Leere bricht aus einem Riss hervor: eine Säule aus violettem Licht; wer darauf steht, fliegt empor. */
    private void erupt(ServerLevel level, Vec3 p, List<LivingEntity> near, boolean first) {
        SpellFxEntity.create(level, SpellFxEntity.Kind.PILLAR, p, null, RIFT_PILLAR_COLOR, 14, 0.8f).param(4.5f).spawn();
        BlockState floor = level.getBlockState(BlockPos.containing(p).below());
        if (!floor.isAir()) {
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, floor), p.x, p.y + 0.2, p.z, 10, 0.4, 0.1, 0.4, 0.15);
        }
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, p.x, p.y + 0.6, p.z, 8, 0.3, 0.6, 0.3, 0.12);
        if (first) {
            level.playSound(null, p.x, p.y, p.z, ModSounds.SHATTERED_KING_ERUPT.get(), SoundSource.HOSTILE, 2.5f, 0.9f + this.random.nextFloat() * 0.2f);
        }
        float damage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.95f;
        for (LivingEntity e : near) {
            double dx = e.getX() - p.x;
            double dz = e.getZ() - p.z;
            double reach = 1.25 + e.getBbWidth() * 0.5;
            if (dx * dx + dz * dz > reach * reach || e.getY() < p.y - 0.6 || e.getY() > p.y + 4.0 || !e.isAlive()) {
                continue;
            }
            if (!this.riftStruck.add(e.getUUID())) {
                continue;
            }
            if (e.hurtServer(level, this.damageSources().indirectMagic(this, this), damage)) {
                e.push(0.0, 1.05, 0.0);
                e.needsSync = true;
                e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 1), this);
            }
        }
    }

    /**
     * Leerenbeben: beide Fäuste hoch über dem Haupt (ein Kreis zeigt, wo sie einschlagen), dann rammt er sie in den Boden –
     * Ringe aus Leerenenergie rollen über den ganzen Platz. Darüber springen!
     */
    private void tickQuake(ServerLevel level, int t) {
        this.hover(this.clampToArena(this.position()), this.getTarget());
        if (t == 1) {
            level.playSound(null, this.blockPosition(), ModSounds.SHATTERED_KING_QUAKE.get(), SoundSource.HOSTILE, 4.0f, 1.0f);
            SpellFxEntity.create(level, SpellFxEntity.Kind.CIRCLE, this.groundPoint.add(0.0, 0.05, 0.0), null, QUAKE_COLOR, QUAKE_SLAM - 1, 3.0f)
                    .spawn();
        }
        int rings = this.healthFraction() < 0.5f ? 4 : 3;
        for (int i = 0; i < rings; i++) {
            if (t == QUAKE_SLAM + i * QUAKE_STEP) {
                this.quakeRing(level, i);
            }
        }
        if (t >= QUAKE_TICKS) {
            this.finish();
        }
    }

    private void quakeRing(ServerLevel level, int index) {
        Vec3 c = this.groundPoint;
        Set<UUID> hit = new HashSet<>();
        if (index == 0) {
            this.shake(level, 48.0, 0.9f, 18);
            level.playSound(null, c.x, c.y, c.z, ModSounds.SHARD_WARDEN_SLAM.get(), SoundSource.HOSTILE, 4.0f, 0.5f);
            level.sendParticles(ParticleTypes.EXPLOSION, c.x, c.y + 0.3, c.z, 3, 0.8, 0.1, 0.8, 0.0);
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, VoidBlocks.VOID_CRYSTAL.get().defaultBlockState()), c.x, c.y + 0.2, c.z,
                    60, 1.5, 0.2, 1.5, 0.25);
            // wer genau dort steht, wo die Fäuste einschlagen, trifft es am härtesten (der erste Ring verschont ihn dann)
            float fists = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE) * 1.1f;
            for (LivingEntity e : this.enemiesNear(level, 30.0)) {
                double dx = e.getX() - c.x;
                double dz = e.getZ() - c.z;
                if (dx * dx + dz * dz > 2.3 * 2.3 || e.getY() < c.y - 1.0 || e.getY() > c.y + 2.5) {
                    continue;
                }
                hit.add(e.getUUID());
                if (e.hurtServer(level, this.damageSources().mobAttack(this), fists)) {
                    e.push(0.0, 0.6, 0.0);
                    e.needsSync = true;
                }
            }
        } else {
            this.shake(level, 40.0, 0.4f, 8);
            level.playSound(null, c.x, c.y, c.z, ModSounds.SHARD_WARDEN_SLAM.get(), SoundSource.HOSTILE, 2.5f, 0.6f + index * 0.08f);
        }
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, c.x, c.y + 0.3, c.z, 30, 0.6, 0.1, 0.6, 0.3);
        float damage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.8f;
        SpellFxEntity.create(level, SpellFxEntity.Kind.SHOCKWAVE, c.add(0.0, 0.05, 0.0), null, QUAKE_COLOR, QUAKE_LIFE, 1.2f).param(QUAKE_SPEED)
                .onTick(fx -> this.ringHits(level, fx, hit, damage)).spawn();
    }

    /** Wen der Bebenring gerade erreicht: wer auf dem Boden steht (nicht im Sprung darüber, nicht auf einem Block) – jeden einmal. */
    private void ringHits(ServerLevel level, SpellFxEntity fx, Set<UUID> hit, float damage) {
        double r = fx.ringRadius(0.0f);
        Vec3 c = fx.position();
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, new AABB(c, c).inflate(r + 1.5, 3.0, r + 1.5), this::canQuake)) {
            if (hit.contains(e.getUUID())) {
                continue;
            }
            double dx = e.getX() - c.x;
            double dz = e.getZ() - c.z;
            double d = Math.sqrt(dx * dx + dz * dz);
            double up = e.getY() - c.y;
            if (Math.abs(d - r) > 0.75 + e.getBbWidth() * 0.5 || up < -1.0 || up > SpellFxEntity.SHOCKWAVE_HEIGHT) {
                continue;
            }
            hit.add(e.getUUID());
            if (e.hurtServer(level, this.damageSources().indirectMagic(this, this), damage)) {
                Vec3 away = d < 1.0e-3 ? new Vec3(1.0, 0.0, 0.0) : new Vec3(dx / d, 0.0, dz / d);
                e.push(away.x * 0.75, 0.45, away.z * 0.75);
                e.needsSync = true;
            }
        }
    }

    private boolean canQuake(LivingEntity e) {
        return e != this && e.isAlive() && !(e instanceof ShardWardenEntity) && !(e instanceof ShatteredKingEntity)
                && !(e instanceof Player p && (p.isCreative() || p.isSpectator()));
    }

    /** Die Höhe des Bodens bei (x, z) nahe {@code y}, wo man stehen kann – oder null (Säule, Wand oder Abgrund). */
    private static @Nullable Double floorAt(ServerLevel level, double x, double z, double y) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int bx = Mth.floor(x);
        int bz = Mth.floor(z);
        int by = Mth.floor(y + 0.5);
        for (int dy = 1; dy >= -2; dy--) {
            pos.set(bx, by + dy, bz);
            if (!level.getBlockState(pos).getCollisionShape(level, pos).isEmpty()) {
                continue;
            }
            BlockPos below = pos.below();
            if (level.getBlockState(below).getCollisionShape(level, below).isEmpty()) {
                continue;
            }
            BlockPos above = pos.above();
            return level.getBlockState(above).getCollisionShape(level, above).isEmpty() ? (double) pos.getY() : null;
        }
        return null;
    }

    /** Der Boden unter einer Stelle (bis zwölf Blöcke tief), sonst {@code fallback}. */
    private double floorBelow(ServerLevel level, Vec3 at, double fallback) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(Mth.floor(at.x), Mth.floor(at.y), Mth.floor(at.z));
        for (int i = 0; i < 12; i++) {
            BlockPos below = pos.below();
            if (level.getBlockState(pos).getCollisionShape(level, pos).isEmpty()
                    && !level.getBlockState(below).getCollisionShape(level, below).isEmpty()) {
                return pos.getY();
            }
            pos.move(0, -1, 0);
        }
        return fallback;
    }

    private List<LivingEntity> enemiesNear(ServerLevel level, double radius) {
        return level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(radius, 12.0, radius), this::canQuake);
    }

    /** Lässt den Bildschirm der Spieler in der Nähe beben (schwächer mit der Entfernung). */
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

    /** Einschlag einer Hand: Schaden nach Nähe, Gegner fliegen auf und weg. */
    private void impact(ServerLevel level, Vec3 c, double radius, float damage, double push, double up) {
        this.shake(level, 26.0, 0.35f, 10);
        level.playSound(null, c.x, c.y, c.z, ModSounds.SHARD_WARDEN_SLAM.get(), SoundSource.HOSTILE, 3.0f, 0.6f);
        level.sendParticles(ParticleTypes.EXPLOSION, c.x, c.y + 0.3, c.z, 2, 0.6, 0.1, 0.6, 0.0);
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, VoidBlocks.VOID_CRYSTAL.get().defaultBlockState()), c.x, c.y + 0.2, c.z,
                60, radius * 0.4, 0.2, radius * 0.4, 0.2);
        ring(level, c, radius, ParticleTypes.END_ROD);
        for (LivingEntity e : this.enemiesNear(level, 40.0)) {
            double d = e.position().distanceTo(c);
            if (d > radius + 0.5) {
                continue;
            }
            float f = (float) Math.max(0.4, 1.0 - d / (radius + 1.0));
            if (e.hurtServer(level, this.damageSources().mobAttack(this), damage * f)) {
                Vec3 away = e.position().subtract(c).multiply(1.0, 0.0, 1.0);
                away = away.lengthSqr() < 1.0e-4 ? new Vec3(1.0, 0.0, 0.0) : away.normalize();
                e.push(away.x * push * f, up * f, away.z * push * f);
                e.needsSync = true;
            }
        }
    }

    private static void ring(ServerLevel level, Vec3 c, double radius, net.minecraft.core.particles.ParticleOptions particle) {
        for (int i = 0; i < 24; i++) {
            double a = i / 24.0 * Math.PI * 2.0;
            level.sendParticles(particle, c.x + Math.cos(a) * radius, c.y + 0.15, c.z + Math.sin(a) * radius, 1, 0.0, 0.0, 0.0, 0.0);
        }
    }

    // ------------------------------------------------------------------ Ereignisse, Rückzug und Tod

    @Override
    public void handleEntityEvent(byte id) {
        switch (id) {
            case EVENT_AWAKEN -> this.awakenAnimation.start(this.tickCount);
            case EVENT_SLAM_LEFT -> this.slamLeftAnimation.start(this.tickCount);
            case EVENT_SLAM_RIGHT -> this.slamRightAnimation.start(this.tickCount);
            case EVENT_SWEEP -> this.sweepAnimation.start(this.tickCount);
            case EVENT_VOLLEY -> this.volleyAnimation.start(this.tickCount);
            case EVENT_GRAVITY -> this.gravityAnimation.start(this.tickCount);
            case EVENT_SUMMON -> this.summonAnimation.start(this.tickCount);
            case EVENT_BEAM -> this.beamAnimation.start(this.tickCount);
            case EVENT_RIFT -> this.riftAnimation.start(this.tickCount);
            case EVENT_QUAKE -> this.quakeAnimation.start(this.tickCount);
            default -> super.handleEntityEvent(id);
        }
    }

    /** Die gerufenen Wächter zerfallen (wenn der König stirbt oder sich zurückzieht). */
    private void dismissGuards(ServerLevel level) {
        for (java.util.UUID id : this.guards) {
            if (level.getEntity(id) instanceof ShardWardenEntity warden && warden.isAlive()) {
                level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, VoidBlocks.ASTRAL_MARBLE.base().get().defaultBlockState()),
                        warden.getX(), warden.getY() + 1.2, warden.getZ(), 40, 0.6, 0.9, 0.6, 0.15);
                level.sendParticles(ParticleTypes.REVERSE_PORTAL, warden.getX(), warden.getY() + 1.2, warden.getZ(), 30, 0.5, 0.8, 0.5, 0.1);
                warden.discard();
            }
        }
        this.guards.clear();
    }

    /** Niemand mehr auf dem Platz: der König zerfällt wieder zu Trümmern (er kann erneut erwachen). */
    private void slumber(ServerLevel level) {
        this.dismissGuards(level);
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, this.getX(), this.getY() + 3.0, this.getZ(), 120, 1.5, 2.5, 1.5, 0.2);
        if (this.seal != null && level.getBlockEntity(this.seal) instanceof ThroneSealBlockEntity throne) {
            throne.onKingSlumber();
        }
        this.bossBar.removeAllPlayers();
        this.discard();
    }

    @Override
    protected void tickDeath() {
        this.deathTime++;
        this.setDeltaMovement(0.0, 0.01, 0.0);
        if (this.level() instanceof ServerLevel level) {
            if (this.deathTime % 4 == 0) {
                level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, VoidBlocks.ASTRAL_MARBLE.base().get().defaultBlockState()),
                        this.getX(), this.getY() + 3.0, this.getZ(), 20, 1.4, 2.4, 1.4, 0.15);
                level.sendParticles(new DustParticleOptions(0xB98CFF, 1.6f), this.getX(), this.getY() + 3.0, this.getZ(), 12, 1.2, 2.0, 1.2, 0.0);
            }
            if (this.deathTime == 1) {
                level.playSound(null, this.blockPosition(), ModSounds.SHATTERED_KING_DEATH.get(), SoundSource.HOSTILE, 4.0f, 1.0f);
            }
            if (this.deathTime == DEATH_TICKS - 2) {
                this.shake(level, 60.0, 0.6f, 30);
                level.sendParticles(ParticleTypes.END_ROD, this.getX(), this.getY() + 3.0, this.getZ(), 120, 2.0, 3.0, 2.0, 0.3);
                level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, this.getX(), this.getY() + 3.0, this.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
            }
            if (this.deathTime == DEATH_TICKS / 2) {
                this.dismissGuards(level);
            }
            if (this.deathTime >= DEATH_TICKS && !this.isRemoved()) {
                this.bossBar.removeAllPlayers();
                if (this.seal != null && level.getBlockEntity(this.seal) instanceof ThroneSealBlockEntity throne) {
                    throne.onKingDefeated(level);
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
        if (this.seal != null) {
            output.putLong("Seal", this.seal.asLong());
        }
        output.putInt("Summons", this.summons);
        output.putBoolean("Wrath", this.wrath);
        output.store("Guards", net.minecraft.core.UUIDUtil.CODEC.listOf(), this.guards);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.home = input.getLong("Home").map(BlockPos::of).orElse(null);
        this.seal = input.getLong("Seal").map(BlockPos::of).orElse(null);
        this.summons = input.getIntOr("Summons", 0);
        this.wrath = input.getBooleanOr("Wrath", false);
        this.guards.clear();
        input.read("Guards", net.minecraft.core.UUIDUtil.CODEC.listOf()).ifPresent(this.guards::addAll);
        this.bossBar.setProgress(this.getHealth() / this.getMaxHealth());
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return ModSounds.SHATTERED_KING_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.SHATTERED_KING_HURT.get();
    }

    @Override
    protected @Nullable SoundEvent getDeathSound() {
        return null;
    }

    @Override
    protected float getSoundVolume() {
        return 3.0f;
    }

    @Override
    public int getAmbientSoundInterval() {
        return 160;
    }

    @Override
    public SoundSource getSoundSource() {
        return SoundSource.HOSTILE;
    }
}
