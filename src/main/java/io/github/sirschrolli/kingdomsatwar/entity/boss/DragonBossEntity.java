package io.github.sirschrolli.kingdomsatwar.entity.boss;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import io.github.sirschrolli.kingdomsatwar.entity.dragon.DragonEntity;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierEntity;
import io.github.sirschrolli.kingdomsatwar.network.ScreenShakePayload;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.entity.PartEntity;
import org.jspecify.annotations.Nullable;

/**
 * Grundgerüst der sechs Drachen-Bosse.
 *
 * <p>Ablauf: {@link State#AWAKENING Erwachen} (eigene Animation je Boss, unverwundbar) → {@link State#FIGHT Kampf}
 * in drei Phasen → {@link State#DYING Tod} (lange Todesanimation, danach Ei und Beute). Verlassen alle Gegner das
 * Nest, kehrt der Boss zurück ({@link State#RETURNING}) und schläft wieder ein ({@link State#SLUMBER}).
 *
 * <p>Der Boss besteht aus mehreren Trefferzonen ({@link BossPart}); die Unterklassen legen Größe und Lage fest,
 * dazu Angriffe, Erweckung und Todeseffekte. Bewegt wird ohne Wegfindung: am Boden lenkend, in der Luft mit
 * weichen Kurven um das Nest.
 */
public abstract class DragonBossEntity extends Mob implements Enemy, ElementalBoss {
    public enum State { AWAKENING, FIGHT, RETURNING, SLUMBER, DYING }

    public enum MoveMode { GROUND, AIR, WATER, BURROW }

    private static final EntityDataAccessor<Byte> DATA_STATE = SynchedEntityData.defineId(DragonBossEntity.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Integer> DATA_ATTACK = SynchedEntityData.defineId(DragonBossEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Byte> DATA_MOVE = SynchedEntityData.defineId(DragonBossEntity.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Byte> DATA_PHASE = SynchedEntityData.defineId(DragonBossEntity.class, EntityDataSerializers.BYTE);
    /** Gestärkt (Kristallpanzer, Aufladung …): die leuchtenden Teile glühen hell. */
    private static final EntityDataAccessor<Boolean> DATA_EMPOWERED = SynchedEntityData.defineId(DragonBossEntity.class, EntityDataSerializers.BOOLEAN);
    /** Tempo des laufenden Angriffs (wird beim Start festgelegt; je wütender, desto schneller). */
    private static final EntityDataAccessor<Float> DATA_TEMPO = SynchedEntityData.defineId(DragonBossEntity.class, EntityDataSerializers.FLOAT);
    private static final Identifier HEALTH_SCALE = KingdomsAtWar.id("boss_health_scale");

    /** Gegner werden bis zu dieser Entfernung vom Nest gesucht. */
    public static final double ARENA_RADIUS = 60.0;
    /** Entfernt sich der Boss weiter vom Nest, kehrt er um. */
    public static final double LEASH_RADIUS = 84.0;
    /** Schaden von Nicht-Spielern (Soldaten, Drachen, Golems) zählt nur zu diesem Teil. */
    public static final float NPC_DAMAGE_FACTOR = 0.4f;
    public static final int DEATH_TICKS = 170;
    private static final int SLUMBER_TICKS = 60;

    /** Drachenart des Bosses – {@code null} bei Nyxar, dem Schattendrachen (keine der sechs Arten). */
    protected final @Nullable DragonType element;
    protected final BossPart[] parts;
    private final List<BossAttack> attacks = new ArrayList<>();
    private final ServerBossEvent bossEvent;
    private final Set<ServerPlayer> barPlayers = new HashSet<>();
    private final Map<UUID, Float> threat = new HashMap<>();
    private final Set<UUID> participants = new HashSet<>();
    private @Nullable BossAttack currentAttack;
    private int attackTick;
    private int stateTicks;
    private int idleTicks = 40;
    private int noTargetTicks;
    private int modeTicks;
    private int retargetTicks;
    private @Nullable BlockPos home;
    private @Nullable BlockPos lairCore;
    private @Nullable DamageSource deathSource;
    private float circleAngle;
    private boolean awakeningAnnounced;
    /** Bruchteil eines Angriffs-Ticks, der durch das erhöhte Tempo noch aussteht. */
    private float tempoCarry;
    /** Phasenwechsel läuft (Ticks übrig): der Drache brüllt, eine Druckwelle fegt über die Arena, kurz unverwundbar. */
    private int transitionTicks;
    private int transitionPhase;
    /** Das letzte Aufbäumen (unter 12 % Leben) ist schon geschehen. */
    private boolean lastStandDone;
    private final BossHazards hazards = new BossHazards();

    // ------------------------------------------------------------------ Client (Animation)
    public int clientStateStart;
    public int clientAttackStart;
    public float flapPhase;
    public float flapPhaseO;
    /** Körper-Drehung der letzten Ticks (neueste zuerst) – Hals und Schwanz folgen verzögert. */
    public final float[] yawHistory = new float[48];
    public float bank;
    public float bankO;
    /** 0 = am Boden (Flügel angelegt), 1 = in der Luft (Flügel ausgebreitet) – weich überblendet. */
    public float airBlend;
    public float airBlendO;
    /** Laune in ruhigen Momenten (nur Client): Art, Beginn (Tick), Richtung, nächster frühester Zeitpunkt. */
    public int idleEvent;
    public int idleEventStart;
    public float idleSide = 1.0f;
    private int nextIdleEvent = 60;

    protected DragonBossEntity(EntityType<? extends DragonBossEntity> type, Level level, @Nullable DragonType element) {
        super(type, level);
        this.element = element;
        this.parts = this.createParts();
        this.bossEvent = new ServerBossEvent(Mth.createInsecureUUID(this.random),
                Component.translatable("boss.kingdomsatwar." + this.bossId()), BossEvent.BossBarColor.RED,
                BossEvent.BossBarOverlay.NOTCHED_10);
        this.bossEvent.setDarkenScreen(true);
        this.registerAttacks(this.attacks);
        this.xpReward = 0;
        this.setPersistenceRequired();
        // Bewegung und Blick steuert der Boss selbst (sonst setzen die Vanilla-Steuerungen alles zurück)
        this.moveControl = new MoveControl(this) {
            @Override
            public void tick() {
            }
        };
        this.lookControl = new LookControl(this) {
            @Override
            public void tick() {
            }
        };
    }

    public static AttributeSupplier.Builder createBossAttributes(double health, double armor, double speed) {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, health)
                .add(Attributes.ARMOR, armor)
                .add(Attributes.ARMOR_TOUGHNESS, 8.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(Attributes.MOVEMENT_SPEED, speed)
                .add(Attributes.FLYING_SPEED, 0.7)
                .add(Attributes.ATTACK_DAMAGE, 16.0)
                .add(Attributes.FOLLOW_RANGE, ARENA_RADIUS)
                .add(Attributes.STEP_HEIGHT, 2.6)
                .add(Attributes.CAMERA_DISTANCE, 14.0);
    }

    // =====================================================================================
    // Für die einzelnen Bosse
    // =====================================================================================

    /** Trefferzonen (Reihenfolge = Netzwerk-Nummern). */
    protected abstract BossPart[] createParts();

    /** Lage der Trefferzonen in Blöcken relativ zum Boss (x = links, y = oben, z = vorn), jeden Tick. */
    protected abstract void layoutParts();

    protected abstract void registerAttacks(List<BossAttack> list);

    /** Dauer der Erweckung in Ticks. */
    public abstract int awakeningTicks();

    /** Effekte und Bewegung während der Erweckung. */
    protected abstract void tickAwakening(ServerLevel level, int tick);

    /** Effekte während der Todesanimation (tick 0 … {@link #DEATH_TICKS}). */
    protected abstract void tickDeathEffects(ServerLevel level, int tick);

    /** Kann der Boss fliegen? (Erddrache nicht) */
    public boolean canFly() {
        return true;
    }

    /** Wie lange der Boss am Boden bzw. in der Luft bleibt, bevor er wechselt (Ticks). */
    protected int groundDuration() {
        return 420 + this.random.nextInt(260);
    }

    protected int airDuration() {
        return 300 + this.random.nextInt(200);
    }

    /** Flughöhe über dem Boden beim Kreisen. */
    protected double cruiseHeight() {
        return 18.0;
    }

    /** Lage des Mauls in der Welt (für Odem und Geschosse). */
    public abstract Vec3 mouthPosition();

    public @Nullable DragonType element() {
        return element;
    }

    /** Kennung für Übersetzungen und Texturen, z. B. {@code fire} oder {@code shadow}. */
    public String bossId() {
        return this.element != null ? this.element.id() : "shadow";
    }

    /** Farbe des Namens in Titeln und Meldungen. */
    public int bossColor() {
        return this.element != null ? this.element.color() : 0xB46CFF;
    }

    /** Partikel des Elements (Atem, Wut, Schlaf). */
    public net.minecraft.core.particles.ParticleOptions bossParticle() {
        return this.element != null ? this.element.particle() : net.minecraft.core.particles.ParticleTypes.SCULK_SOUL;
    }

    /** Wirkung des Elements auf Gegner beim Phasenwechsel. */
    protected void transitionEffect(LivingEntity e) {
        if (this.element == null) {
            e.addEffect(BossEffects.debuff(net.minecraft.world.effect.MobEffects.DARKNESS, 100, 0), this);
            e.addEffect(BossEffects.debuff(net.minecraft.world.effect.MobEffects.WITHER, 60, 1), this);
            return;
        }
        switch (this.element) {
            case FIRE -> BossEffects.burn(e, 4);
            case ICE -> BossEffects.freeze(e, 3);
            case LIGHTNING -> BossEffects.stun(e, 2);
            case NATURE -> BossEffects.poison(e, 4);
            case WATER -> BossEffects.soak(e, 4);
            case EARTH -> e.addEffect(BossEffects.debuff(net.minecraft.world.effect.MobEffects.SLOWNESS, 60, 2), this);
        }
    }

    /** Was nach dem Tod geschieht (Standard: Drachenei, Siegelsplitter, Nest wird still). */
    protected void onDefeated(ServerLevel level, BlockPos home) {
        BossLairs.onBossDefeated(level, this, home);
    }

    // =====================================================================================
    // Daten
    // =====================================================================================

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_STATE, (byte) State.AWAKENING.ordinal());
        builder.define(DATA_ATTACK, BossAnims.NONE);
        builder.define(DATA_MOVE, (byte) MoveMode.GROUND.ordinal());
        builder.define(DATA_PHASE, (byte) 1);
        builder.define(DATA_EMPOWERED, false);
        builder.define(DATA_TEMPO, 1.0f);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (DATA_STATE.equals(key)) {
            this.clientStateStart = this.tickCount;
        } else if (DATA_ATTACK.equals(key)) {
            this.clientAttackStart = this.tickCount;
        }
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        if (this.home != null) {
            output.store("BossHome", BlockPos.CODEC, this.home);
        }
        if (this.lairCore != null) {
            output.store("BossLairCore", BlockPos.CODEC, this.lairCore);
        }
        output.putInt("BossState", this.getBossState().ordinal());
        output.putInt("BossMove", this.getMoveMode().ordinal());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        input.read("BossHome", BlockPos.CODEC).ifPresent(pos -> this.home = pos);
        input.read("BossLairCore", BlockPos.CODEC).ifPresent(pos -> this.lairCore = pos);
        State state = State.values()[Mth.clamp(input.getIntOr("BossState", State.FIGHT.ordinal()), 0, State.values().length - 1)];
        // Nach dem Laden: ein unterbrochenes Erwachen oder Einschlafen geht als Kampf weiter (ohne Gegner kehrt er heim)
        this.setBossState(state == State.DYING || state == State.SLUMBER || state == State.AWAKENING ? State.FIGHT : state);
        this.setMoveMode(MoveMode.values()[Mth.clamp(input.getIntOr("BossMove", 0), 0, MoveMode.values().length - 1)]);
        this.awakeningAnnounced = true;
    }

    public State getBossState() {
        return State.values()[Mth.clamp(this.entityData.get(DATA_STATE), 0, State.values().length - 1)];
    }

    protected void setBossState(State state) {
        this.entityData.set(DATA_STATE, (byte) state.ordinal());
        this.stateTicks = 0;
    }

    public MoveMode getMoveMode() {
        return MoveMode.values()[Mth.clamp(this.entityData.get(DATA_MOVE), 0, MoveMode.values().length - 1)];
    }

    public void setMoveMode(MoveMode mode) {
        this.entityData.set(DATA_MOVE, (byte) mode.ordinal());
        this.setNoGravity(mode == MoveMode.AIR || mode == MoveMode.WATER);
        this.modeTicks = 0;
    }

    public boolean isFlying() {
        return this.getMoveMode() == MoveMode.AIR;
    }

    public int getAttackAnim() {
        return this.entityData.get(DATA_ATTACK);
    }

    public int getPhase() {
        return this.entityData.get(DATA_PHASE);
    }

    /** Gestärkt (z. B. Kristallpanzer): die leuchtenden Teile glühen hell. */
    public boolean isEmpowered() {
        return this.entityData.get(DATA_EMPOWERED);
    }

    public void setEmpowered(boolean empowered) {
        this.entityData.set(DATA_EMPOWERED, empowered);
    }

    // =====================================================================================
    // Wut: Je mehr Schaden der Drache nimmt, desto schneller, härter und angriffslustiger wird er
    // =====================================================================================

    /** 0 bei vollem Leben … 1 kurz vor dem Tod (nur im Kampf). */
    public float rage() {
        if (this.getBossState() != State.FIGHT || this.getMaxHealth() <= 0) {
            return 0.0f;
        }
        return Mth.clamp(1.0f - this.getHealth() / this.getMaxHealth(), 0.0f, 1.0f);
    }

    /** Schaden aller Angriffe (Nahkampf, Odem, Geschosse): bis zu 70 % mehr. */
    public float damageMultiplier() {
        return 1.0f + 0.55f * this.rage() + (this.getPhase() >= 3 ? 0.15f : 0.0f);
    }

    /** Tempo neuer Angriffe: bis zu 40 % schneller. */
    public float tempo() {
        return 1.0f + 0.32f * this.rage() + (this.getPhase() >= 3 ? 0.08f : 0.0f);
    }

    /** Laufen, Schwimmen und Fliegen: bis zu 30 % schneller. */
    public float speedMultiplier() {
        return 1.0f + 0.3f * this.rage();
    }

    /** Tempo des laufenden Angriffs (Server und Client: für Ablauf und Animation). */
    public float attackTempo() {
        return this.entityData.get(DATA_TEMPO);
    }

    /** Läuft gerade ein Phasenwechsel? */
    public boolean isInTransition() {
        return this.transitionTicks > 0;
    }

    /** Ist der Drache während der Erweckung noch verborgen (z. B. im Eiskristall)? Für die Darstellung. */
    public boolean hiddenDuringAwakening(int ticks) {
        return false;
    }

    /** Der große Angriff, den der Drache nach einem Phasenwechsel entfesselt (Name, null = keiner). */
    protected @Nullable String signatureAttack(int phase) {
        return null;
    }

    public @Nullable BlockPos getHome() {
        return this.home;
    }

    public void setHome(BlockPos home) {
        this.home = home;
    }

    public @Nullable BlockPos getLairCore() {
        return this.lairCore;
    }

    public void setLairCore(BlockPos core) {
        this.lairCore = core;
    }

    public int getStateTicks() {
        return this.stateTicks;
    }

    public Set<UUID> participants() {
        return this.participants;
    }

    // =====================================================================================
    // Erwachen
    // =====================================================================================

    /** Startet das Erwachen am Nest (Lebensleiste je nach Spielerzahl und Schwierigkeit, Titel, Beben). */
    public void beginAwakening(ServerLevel level, BlockPos home) {
        this.home = home;
        this.setBossState(State.AWAKENING);
        this.setMoveMode(MoveMode.GROUND);
        this.noPhysics = true;
        int players = Math.max(1, this.nearbyPlayers(level, ARENA_RADIUS * 1.4).size());
        double difficulty = switch (level.getDifficulty()) {
            case PEACEFUL, EASY -> 0.75;
            case NORMAL -> 1.0;
            case HARD -> 1.3;
        };
        double scale = difficulty * (1.0 + 0.35 * (players - 1)) - 1.0;
        AttributeInstance health = this.getAttribute(Attributes.MAX_HEALTH);
        if (health != null) {
            health.removeModifier(HEALTH_SCALE);
            if (Math.abs(scale) > 0.01) {
                health.addPermanentModifier(new AttributeModifier(HEALTH_SCALE, scale, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
            }
        }
        this.setHealth(this.getMaxHealth());
        this.announce(level);
    }

    private void announce(ServerLevel level) {
        this.awakeningAnnounced = true;
        Component title = Component.translatable("entity.kingdomsatwar." + this.bossId() + "_dragon_boss")
                .withColor(this.bossColor());
        Component subtitle = Component.translatable("boss.kingdomsatwar." + this.bossId() + ".title")
                .withStyle(ChatFormatting.GRAY);
        for (ServerPlayer player : this.nearbyPlayers(level, 110.0)) {
            player.connection.send(new ClientboundSetTitlesAnimationPacket(20, 70, 30));
            player.connection.send(new ClientboundSetTitleTextPacket(title));
            player.connection.send(new ClientboundSetSubtitleTextPacket(subtitle));
        }
        this.shake(level, 90.0, 0.7f, this.awakeningTicks());
    }

    /** Nur für Vorführungen: Bossleiste sofort auf den Stand bringen (auch ohne KI). */
    public void devBossBar(ServerLevel level) {
        this.updateBossBar(level);
    }

    /** Nur für Vorführungen: sofort im Kampfzustand (ohne Erwachen), am Boden oder in der Luft. */
    public void devPose(MoveMode mode) {
        this.noPhysics = false;
        this.awakeningAnnounced = true;
        this.setBossState(State.FIGHT);
        this.setMoveMode(mode);
    }

    private void finishAwakening(ServerLevel level) {
        this.noPhysics = false;
        this.setBossState(State.FIGHT);
        this.idleTicks = 20;
        this.modeTicks = 0;
    }

    // =====================================================================================
    // Hilfen für Angriffe und Effekte
    // =====================================================================================

    public List<ServerPlayer> nearbyPlayers(ServerLevel level, double radius) {
        Vec3 center = this.home != null ? Vec3.atCenterOf(this.home) : this.position();
        List<ServerPlayer> list = new ArrayList<>();
        for (ServerPlayer player : level.players()) {
            if (!player.isSpectator() && player.position().distanceToSqr(center) < radius * radius) {
                list.add(player);
            }
        }
        return list;
    }

    /** Lässt bei Spielern im Umkreis den Bildschirm beben. */
    public void shake(ServerLevel level, double radius, float strength, int ticks) {
        for (ServerPlayer player : level.players()) {
            double dist = player.distanceTo(this);
            if (dist < radius) {
                float s = strength * (float) (1.0 - dist / radius * 0.7);
                io.github.sirschrolli.kingdomsatwar.network.ModNetwork.send(player, new ScreenShakePayload(s, ticks));
            }
        }
    }

    /** Ist das ein Gegner dieses Bosses? (Spieler, Soldaten, gezähmte Drachen und alle, die ihn angegriffen haben) */
    public boolean isBossEnemy(@Nullable LivingEntity entity) {
        if (entity == null || !entity.isAlive() || entity == this || entity instanceof DragonBossEntity) {
            return false;
        }
        if (entity instanceof Player player) {
            return !player.isCreative() && !player.isSpectator();
        }
        if (entity instanceof SoldierEntity) {
            return true;
        }
        if (entity instanceof DragonEntity dragon) {
            return dragon.isTame();
        }
        return this.threat.containsKey(entity.getUUID());
    }

    /** Alle Gegner in einem Bereich (für Flächenangriffe). */
    public List<LivingEntity> enemiesIn(AABB box) {
        return this.level().getEntitiesOfClass(LivingEntity.class, box, this::isBossEnemy);
    }

    /** Alle Gegner im Umkreis (waagerecht) um einen Punkt. */
    public List<LivingEntity> enemiesAround(Vec3 center, double radius, double height) {
        AABB box = new AABB(center.x - radius, center.y - height, center.z - radius, center.x + radius, center.y + height, center.z + radius);
        List<LivingEntity> list = this.enemiesIn(box);
        list.removeIf(e -> e.position().subtract(center).horizontalDistanceSqr() > radius * radius);
        return list;
    }

    /** Fügt Schaden zu (Nahkampf zählt als Angriff des Bosses). */
    public boolean hurtTarget(ServerLevel level, LivingEntity target, float damage) {
        return target.hurtServer(level, this.damageSources().mobAttack(this), damage * this.damageMultiplier());
    }

    /** Stößt ein Ziel vom Punkt {@code from} weg (und etwas nach oben). */
    public static void pushAway(LivingEntity target, Vec3 from, double strength, double up) {
        Vec3 dir = target.position().subtract(from).multiply(1, 0, 1);
        if (dir.lengthSqr() < 1.0e-4) {
            dir = new Vec3(1, 0, 0);
        }
        dir = dir.normalize();
        double resist = 1.0 - target.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE) * 0.6;
        target.push(dir.x * strength * resist, up * resist, dir.z * strength * resist);
        target.needsSync = true;
    }

    /** Waagerechter Winkel (Grad) zwischen Blickrichtung des Körpers und einem Punkt: 0 = voraus, 180 = dahinter. */
    public float angleTo(Vec3 point) {
        Vec3 d = point.subtract(this.position());
        float yaw = (float) (Mth.atan2(d.z, d.x) * Mth.RAD_TO_DEG) - 90.0f;
        return Math.abs(Mth.wrapDegrees(yaw - this.yBodyRot));
    }

    /** Vorwärts-Richtung des Körpers (waagerecht). */
    public Vec3 forward() {
        return Vec3.directionFromRotation(0.0f, this.yBodyRot);
    }

    /** Punkt relativ zum Körper in Blöcken: x = links, y = oben, z = vorn. */
    public Vec3 bodyPoint(double x, double y, double z) {
        float yawRad = this.yBodyRot * Mth.DEG_TO_RAD;
        double sin = Mth.sin(yawRad);
        double cos = Mth.cos(yawRad);
        return new Vec3(this.getX() + x * cos - z * sin, this.getY() + y, this.getZ() + x * sin + z * cos);
    }

    public int groundY(double x, double z) {
        return this.level().getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Mth.floor(x), Mth.floor(z));
    }

    // =====================================================================================
    // Ticks
    // =====================================================================================

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            this.clientAnimTick();
        }
    }

    private void clientAnimTick() {
        System.arraycopy(this.yawHistory, 0, this.yawHistory, 1, this.yawHistory.length - 1);
        this.yawHistory[0] = this.yBodyRot;
        if (this.tickCount < 3) {
            java.util.Arrays.fill(this.yawHistory, this.yBodyRot);
        }
        this.flapPhaseO = this.flapPhase;
        boolean flying = this.isFlying() || (this.getAttackAnim() == BossAnims.TAKE_OFF);
        float speed = flying ? (this.getDeltaMovement().y < -0.15 ? 0.06f : 0.19f) : 0.0f;
        if (this.getAttackAnim() == BossAnims.RAIN && this.isFlying()) {
            speed = 0.15f;        // Schweben: gleichmäßige, kräftige Schläge
        }
        this.flapPhase += speed;
        if (flying && Mth.sin(this.flapPhase) > 0.97f && Mth.sin(this.flapPhaseO) <= 0.97f) {
            this.level().playLocalSound(this.getX(), this.getY(), this.getZ(),
                    io.github.sirschrolli.kingdomsatwar.registry.ModSounds.DRAGON_FLAP.get(), this.getSoundSource(), 3.0f, 0.55f, false);
        }
        this.airBlendO = this.airBlend;
        boolean airborne = this.isFlying() && !(this.getAttackAnim() == BossAnims.LAND && this.getY() - this.groundY(this.getX(), this.getZ()) < 2.5);
        if (this.getAttackAnim() == BossAnims.TAKE_OFF) {
            airborne = this.tickCount - this.clientAttackStart > 9;
        }
        this.airBlend = Mth.approach(this.airBlend, airborne ? 1.0f : 0.0f, 0.07f);
        this.bankO = this.bank;
        float turn = Mth.wrapDegrees(this.yawHistory[0] - this.yawHistory[1]);
        float wanted = this.isFlying() ? Mth.clamp(-turn * 6.0f, -40.0f, 40.0f) : 0.0f;
        this.bank += (wanted - this.bank) * 0.1f;
        this.tickMood();
        this.rageAura();
    }

    /** Wut-Aura: Ab Phase 2 steigen Teilchen des Elements vom Körper auf, in Phase 3 dicht und mit Glut in den Nüstern. */
    private void rageAura() {
        int phase = this.getPhase();
        if (phase < 2 || this.getBossState() != State.FIGHT || this.parts.length == 0) {
            return;
        }
        int every = phase >= 3 ? 2 : 6;
        if (this.tickCount % every != 0) {
            return;
        }
        BossPart part = this.parts[this.random.nextInt(this.parts.length)];
        double w = part.getBbWidth();
        this.level().addParticle(this.bossParticle(),
                part.getX() + (this.random.nextDouble() - 0.5) * w, part.getY() + this.random.nextDouble() * part.getBbHeight(),
                part.getZ() + (this.random.nextDouble() - 0.5) * w, 0.0, 0.04 + this.random.nextDouble() * 0.04, 0.0);
        if (phase >= 3 && this.tickCount % 16 == 0) {
            Vec3 mouth = this.mouthPosition();
            this.level().addParticle(net.minecraft.core.particles.ParticleTypes.LARGE_SMOKE, mouth.x, mouth.y + 0.3, mouth.z, 0.0, 0.06, 0.0);
        }
    }

    /** Launen in ruhigen Momenten: Schnauben, Kopfschütteln, Schwanzschlagen, Flügel lockern, Umsehen, Gähnen. */
    private void tickMood() {
        if (this.idleEvent != BossAnims.MOOD_NONE && this.tickCount - this.idleEventStart > BossAnims.MOOD_TICKS[this.idleEvent]) {
            this.idleEvent = BossAnims.MOOD_NONE;
        }
        State state = this.getBossState();
        boolean calm = this.getAttackAnim() == BossAnims.NONE && (state == State.FIGHT || state == State.RETURNING)
                && !this.isDeadOrDying();
        if (!calm) {
            if (this.getAttackAnim() != BossAnims.NONE) {
                this.idleEvent = BossAnims.MOOD_NONE;
            }
            return;
        }
        if (this.idleEvent == BossAnims.MOOD_NONE && this.tickCount >= this.nextIdleEvent) {
            int[] choices = this.isFlying()
                    ? new int[] {BossAnims.MOOD_GLANCE, BossAnims.MOOD_SHAKE, BossAnims.MOOD_SNORT}
                    : new int[] {BossAnims.MOOD_SNORT, BossAnims.MOOD_SNORT, BossAnims.MOOD_SHAKE, BossAnims.MOOD_TAIL_FLICK,
                            BossAnims.MOOD_TAIL_FLICK, BossAnims.MOOD_SHRUG, BossAnims.MOOD_GLANCE, BossAnims.MOOD_GLANCE,
                            BossAnims.MOOD_YAWN};
            this.idleEvent = choices[this.random.nextInt(choices.length)];
            this.idleEventStart = this.tickCount;
            this.idleSide = this.random.nextBoolean() ? 1.0f : -1.0f;
            this.nextIdleEvent = this.tickCount + 50 + this.random.nextInt(90);
        }
        if (this.idleEvent == BossAnims.MOOD_SNORT && this.tickCount - this.idleEventStart == 4) {
            this.snortParticles();
        }
    }

    /** Rauch (und je nach Element mehr) aus den Nüstern beim Schnauben. */
    protected void snortParticles() {
        Vec3 mouth = this.mouthPosition();
        Vec3 dir = Vec3.directionFromRotation(10.0f, this.yHeadRot);
        for (int i = 0; i < 6; i++) {
            double spread = 0.12;
            this.level().addParticle(i % 2 == 0 ? net.minecraft.core.particles.ParticleTypes.LARGE_SMOKE
                            : net.minecraft.core.particles.ParticleTypes.SMOKE,
                    mouth.x, mouth.y + 0.4, mouth.z,
                    dir.x * 0.25 + (this.random.nextDouble() - 0.5) * spread, dir.y * 0.25 + 0.03,
                    dir.z * 0.25 + (this.random.nextDouble() - 0.5) * spread);
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        this.layoutParts();
    }

    @Override
    protected void tickHeadTurn(float yBodyRotT) {
        // Körperdrehung steuert der Boss selbst; auf dem Client folgt der Körper der (geglätteten) Drehung vom Server
        if (this.level().isClientSide()) {
            this.yBodyRot = this.getYRot();
        }
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        this.stateTicks++;
        switch (this.getBossState()) {
            case AWAKENING -> {
                if (!this.awakeningAnnounced) {
                    this.announce(level);
                }
                this.tickAwakening(level, this.stateTicks);
                if (this.stateTicks >= this.awakeningTicks()) {
                    this.finishAwakening(level);
                }
            }
            case FIGHT -> this.tickFight(level);
            case RETURNING -> this.tickReturning(level);
            case SLUMBER -> {
                this.tickSlumber(level, this.stateTicks);
                if (this.stateTicks >= SLUMBER_TICKS) {
                    this.onSlumber(level);
                    this.discard();
                }
            }
            case DYING -> {
            }
        }
        if (this.tickCount % 10 == 0) {
            this.updateBossBar(level);
        }
    }

    // ------------------------------------------------------------------ Kampf

    private void tickFight(ServerLevel level) {
        this.updatePhase(level);
        this.modeTicks++;
        if (this.transitionTicks > 0) {
            this.tickTransition(level);
            return;
        }
        this.hazards.tick(level, this);
        LivingEntity target = this.getTarget();
        if (--this.retargetTicks <= 0 || !this.isValidTarget(target)) {
            target = this.pickTarget(level);
            this.setTarget(target);
            this.retargetTicks = 60 + this.random.nextInt(60);
        }
        for (BossAttack attack : this.attacks) {
            attack.coolDown();
        }
        if (target == null) {
            if (this.currentAttack != null) {
                this.endAttack(level);
            }
            if (++this.noTargetTicks > 200) {
                this.setBossState(State.RETURNING);
            }
            this.hoverOrIdle(level);
            return;
        }
        this.noTargetTicks = 0;
        if (this.home != null && this.position().subtract(Vec3.atCenterOf(this.home)).horizontalDistance() > LEASH_RADIUS) {
            this.setBossState(State.RETURNING);
            return;
        }
        this.lookAtTarget(target);
        if (this.currentAttack != null) {
            BossAttack attack = this.currentAttack;
            if (this.attackTick < attack.turnUntil()) {
                this.turnTowards(target.position(), 6.0f);
            }
            // Je wütender, desto schneller läuft der Angriff ab (manchmal zwei Angriffs-Ticks in einem Spiel-Tick)
            this.tempoCarry += this.attackTempo();
            while (this.tempoCarry >= 1.0f && this.currentAttack == attack) {
                this.tempoCarry -= 1.0f;
                attack.tick(level, this, target, this.attackTick);
                this.attackTick++;
                if (this.currentAttack == attack && this.attackTick >= attack.duration()) {
                    this.endAttack(level);
                }
            }
            if (attack.stopsMovement() && this.currentAttack == attack) {
                this.stopMoving();
            }   // sonst bewegt der Angriff den Boss selbst
            return;
        }
        this.moveInFight(level, target);
        if (--this.idleTicks <= 0) {
            this.tryStartAttack(level, target);
        }
    }

    private void updatePhase(ServerLevel level) {
        float hp = this.getHealth() / this.getMaxHealth();
        int phase = hp > 0.6f ? 1 : hp > 0.3f ? 2 : 3;
        if (phase > this.getPhase()) {
            this.entityData.set(DATA_PHASE, (byte) phase);
            this.onPhaseChange(level, phase);
        } else if (!this.lastStandDone && hp < 0.12f && this.transitionTicks <= 0) {
            this.lastStandDone = true;
            this.beginTransition(level, 4);
        }
    }

    /** Phasenwechsel: Der Drache bäumt sich brüllend auf, eine Druckwelle fegt über die Arena, dann entfesselt er seinen großen Angriff. */
    protected void onPhaseChange(ServerLevel level, int phase) {
        this.beginTransition(level, phase);
    }

    /** Dauer eines Phasenwechsels in Ticks. */
    public static final int TRANSITION_TICKS = 44;

    private void beginTransition(ServerLevel level, int phase) {
        if (this.currentAttack != null) {
            this.endAttack(level);
        }
        this.transitionTicks = TRANSITION_TICKS;
        this.transitionPhase = phase;
        this.entityData.set(DATA_TEMPO, 1.0f);
        this.entityData.set(DATA_ATTACK, BossAnims.ROAR);
        Component name = Component.translatable("entity.kingdomsatwar." + this.bossId() + "_dragon_boss").withColor(this.bossColor());
        String key = phase >= 4 ? "boss.kingdomsatwar.last_stand" : phase == 3 ? "boss.kingdomsatwar.enraged" : "boss.kingdomsatwar.angered";
        for (ServerPlayer player : this.nearbyPlayers(level, ARENA_RADIUS * 1.5)) {
            player.sendOverlayMessage(Component.translatable(key, name).withStyle(phase >= 3 ? ChatFormatting.RED : ChatFormatting.GOLD));
        }
    }

    private void tickTransition(ServerLevel level) {
        int t = TRANSITION_TICKS - this.transitionTicks;
        this.transitionTicks--;
        this.stopMoving();
        LivingEntity target = this.getTarget();
        if (target != null) {
            this.lookAtTarget(target);
            if (t < 10) {
                this.turnTowards(target.position(), 6.0f);
            }
        }
        if (t == 12) {
            Vec3 feet = this.position();
            BossFx.sound(level, feet, io.github.sirschrolli.kingdomsatwar.registry.ModSounds.DRAGON_ROAR.get(), 8.0f, 0.42f);
            BossFx.sound(level, feet, net.minecraft.sounds.SoundEvents.ENDER_DRAGON_GROWL, 6.0f, 0.5f);
            BossFx.sound(level, feet, net.minecraft.sounds.SoundEvents.GENERIC_EXPLODE.value(), 3.0f, 0.5f);
            this.shake(level, 70.0, this.transitionPhase >= 3 ? 1.0f : 0.7f, 30);
            double radius = this.transitionPhase >= 3 ? 14.0 : 11.0;
            for (int r = 3; r <= (int) radius; r += 3) {
                BossFx.ring(level, this.bossParticle(), feet.add(0, 0.4, 0), r, r * 7, 0.25);
            }
            BossFx.burst(level, this.bossParticle(), feet.add(0, 4, 0), 80, 4.0, 0.25);
            for (LivingEntity e : this.enemiesAround(feet, radius, 8)) {
                double d = e.position().subtract(feet).horizontalDistance();
                float f = (float) Math.max(0.35, 1.0 - d / radius);
                this.hurtTarget(level, e, 6.0f * f);
                pushAway(e, feet, 2.2 * f, 0.7 * f);
                this.transitionEffect(e);
            }
        }
        if (this.transitionTicks <= 0) {
            this.entityData.set(DATA_ATTACK, BossAnims.NONE);
            this.idleTicks = 2;
            String signature = this.signatureAttack(Math.min(3, this.transitionPhase));
            BossAttack attack = signature != null ? this.findAttack(signature) : null;
            if (attack != null && target != null && this.isWhere(attack.where())) {
                this.startAttack(level, attack, target);
            }
        }
    }

    private boolean isValidTarget(@Nullable LivingEntity target) {
        if (!this.isBossEnemy(target)) {
            return false;
        }
        Vec3 center = this.home != null ? Vec3.atCenterOf(this.home) : this.position();
        return target.position().subtract(center).horizontalDistance() < ARENA_RADIUS && Math.abs(target.getY() - center.y) < 48;
    }

    /** Wählt das Ziel: Spieler bevorzugt, wer viel Schaden macht, und wer nah ist. */
    private @Nullable LivingEntity pickTarget(ServerLevel level) {
        Vec3 center = this.home != null ? Vec3.atCenterOf(this.home) : this.position();
        AABB box = new AABB(center, center).inflate(ARENA_RADIUS, 48, ARENA_RADIUS);
        List<LivingEntity> candidates = level.getEntitiesOfClass(LivingEntity.class, box, this::isValidTarget);
        return candidates.stream().max(Comparator.comparingDouble(e -> {
            double score = (e instanceof Player ? 40.0 : 12.0) + this.threat.getOrDefault(e.getUUID(), 0.0f) * 0.4;
            score -= e.distanceTo(this) * 0.7;
            return score + this.random.nextDouble() * 6.0;
        })).orElse(null);
    }

    private void tryStartAttack(ServerLevel level, LivingEntity target) {
        // Wechsel zwischen Boden und Luft
        if (this.canFly() && this.getMoveMode() == MoveMode.GROUND && this.modeTicks > this.groundDuration() && this.onGround()) {
            BossAttack takeOff = this.findAttack(BossAnims.TAKE_OFF);
            if (takeOff != null) {
                this.startAttack(level, takeOff, target);
                return;
            }
        }
        if (this.getMoveMode() == MoveMode.AIR && this.modeTicks > this.airDuration()) {
            BossAttack land = this.findAttack(BossAnims.LAND);
            if (land != null) {
                this.startAttack(level, land, target);
                return;
            }
        }
        double distance = target.position().subtract(this.position()).horizontalDistance();
        float angle = this.angleTo(target.position());
        float total = 0;
        List<BossAttack> options = new ArrayList<>();
        List<Float> weights = new ArrayList<>();
        for (BossAttack attack : this.attacks) {
            if (!attack.ready() || attack.minPhase() > this.getPhase() || !this.isWhere(attack.where())
                    || attack.id() == BossAnims.TAKE_OFF || attack.id() == BossAnims.LAND) {
                continue;
            }
            float w = attack.weight(this, target, distance, angle);
            if (w > 0 && attack.minPhase() > 1) {
                w *= 1.0f + this.rage();   // die großen Angriffe kommen in Wut häufiger
            }
            if (w > 0) {
                options.add(attack);
                weights.add(w);
                total += w;
            }
        }
        if (options.isEmpty()) {
            this.idleTicks = 5;
            return;
        }
        float roll = this.random.nextFloat() * total;
        for (int i = 0; i < options.size(); i++) {
            roll -= weights.get(i);
            if (roll <= 0) {
                this.startAttack(level, options.get(i), target);
                return;
            }
        }
        this.startAttack(level, options.getLast(), target);
    }

    private boolean isWhere(BossAttack.Where where) {
        return switch (where) {
            case ANY -> true;
            // „am Boden“ heißt hier: nicht in der Luft (eine Seeschlange beißt und peitscht auch im Wasser)
            case GROUND -> this.getMoveMode() == MoveMode.GROUND || this.getMoveMode() == MoveMode.BURROW
                    || this.getMoveMode() == MoveMode.WATER;
            case AIR -> this.getMoveMode() == MoveMode.AIR;
            case WATER -> this.getMoveMode() == MoveMode.WATER;
        };
    }

    public @Nullable BossAttack findAttack(int anim) {
        for (BossAttack attack : this.attacks) {
            if (attack.id() == anim) {
                return attack;
            }
        }
        return null;
    }

    /** Angriff nach seinem Namen (für Vorführungen und Tests). */
    public @Nullable BossAttack findAttack(String name) {
        for (BossAttack attack : this.attacks) {
            if (attack.name().equals(name)) {
                return attack;
            }
        }
        return null;
    }

    public void startAttack(ServerLevel level, BossAttack attack, LivingEntity target) {
        if (this.currentAttack != null) {
            this.endAttack(level);
        }
        this.currentAttack = attack;
        this.attackTick = 0;
        this.tempoCarry = 0.0f;
        this.entityData.set(DATA_TEMPO, attack.id() == BossAnims.TAKE_OFF || attack.id() == BossAnims.LAND ? 1.0f : this.tempo());
        this.entityData.set(DATA_ATTACK, attack.id());
        attack.start(level, this, target);
    }

    /** Beendet den laufenden Angriff vorzeitig (z. B. wenn die Landung geglückt ist). */
    public void finishAttack(ServerLevel level) {
        this.endAttack(level);
    }

    private void endAttack(ServerLevel level) {
        BossAttack attack = this.currentAttack;
        this.currentAttack = null;
        this.entityData.set(DATA_ATTACK, BossAnims.NONE);
        float rage = this.rage();
        if (attack != null) {
            attack.stop(level, this);
            attack.resetCooldown(1.0f - 0.5f * rage);
        }
        int phase = this.getPhase();
        this.idleTicks = Math.round(Mth.lerp(rage, 16.0f, 3.0f)) + this.random.nextInt(Math.round(Mth.lerp(rage, 14.0f, 5.0f)) + 1);
        // Angriffsketten: ab Phase 2 folgt manchmal sofort der nächste Angriff
        float chain = phase >= 3 ? 0.45f : phase == 2 ? 0.2f : 0.0f;
        if (attack != null && attack.id() != BossAnims.TAKE_OFF && attack.id() != BossAnims.LAND && this.random.nextFloat() < chain) {
            this.idleTicks = this.random.nextInt(3);
        }
    }

    public @Nullable BossAttack currentAttack() {
        return this.currentAttack;
    }

    // ------------------------------------------------------------------ Bewegung

    private void lookAtTarget(LivingEntity target) {
        Vec3 eye = this.mouthPosition();
        Vec3 d = target.getEyePosition().subtract(eye);
        float yaw = (float) (Mth.atan2(d.z, d.x) * Mth.RAD_TO_DEG) - 90.0f;
        float rel = Mth.clamp(Mth.wrapDegrees(yaw - this.yBodyRot), -70.0f, 70.0f);
        this.yHeadRot = Mth.approachDegrees(this.yHeadRot, this.yBodyRot + rel, 8.0f);
        float pitch = (float) -(Mth.atan2(d.y, d.horizontalDistance()) * Mth.RAD_TO_DEG);
        this.setXRot(Mth.approach(this.getXRot(), Mth.clamp(pitch, -50.0f, 60.0f), 5.0f));
    }

    /** Dreht den Körper zum Punkt (höchstens {@code maxStep} Grad je Tick). */
    public void turnTowards(Vec3 point, float maxStep) {
        Vec3 d = point.subtract(this.position());
        if (d.horizontalDistanceSqr() < 1.0e-3) {
            return;
        }
        float yaw = (float) (Mth.atan2(d.z, d.x) * Mth.RAD_TO_DEG) - 90.0f;
        float body = Mth.approachDegrees(this.yBodyRot, yaw, maxStep);
        this.setYRot(body);
        this.yBodyRot = body;
        this.yBodyRotO = this.yBodyRotO + Mth.wrapDegrees(body - this.yBodyRotO);
    }

    public void stopMoving() {
        this.zza = 0;
        this.xxa = 0;
        if (this.isFlying()) {
            this.setDeltaMovement(this.getDeltaMovement().scale(0.8));
        } else {
            this.setDeltaMovement(this.getDeltaMovement().multiply(0.5, 1.0, 0.5));
        }
    }

    /** Am Boden: zum Ziel gehen und ihm zugewandt bleiben; in der Luft: um das Nest kreisen. */
    public void moveInFight(ServerLevel level, LivingEntity target) {
        if (this.isFlying()) {
            this.circleAround(level, target.position(), 26.0, this.cruiseHeight(), 0.65);
            return;
        }
        double distance = target.position().subtract(this.position()).horizontalDistance();
        float angle = this.angleTo(target.position());
        this.turnTowards(target.position(), 4.5f * (1.0f + 0.5f * this.rage()));
        double reach = 5.5 + this.getBbWidth() * 0.5;
        if (distance > reach && angle < 60.0f) {
            this.walkForward(distance > 18 ? 1.25 : 0.8);
        } else {
            this.zza = 0;
        }
    }

    public void walkForward(double speedFactor) {
        this.setSpeed((float) (this.getAttributeValue(Attributes.MOVEMENT_SPEED) * speedFactor * this.speedMultiplier()));
        this.zza = 1.0f;
    }

    /** Kreist in der Luft um einen Punkt (Ziel oder Nest). */
    public void circleAround(ServerLevel level, Vec3 center, double radius, double height, double speed) {
        speed *= this.speedMultiplier();
        this.circleAngle += (float) (speed / radius);
        double x = center.x + Math.cos(this.circleAngle) * radius;
        double z = center.z + Math.sin(this.circleAngle) * radius;
        double y = Math.max(center.y, this.groundY(x, z)) + height;
        this.flyTowards(new Vec3(x, y, z), speed, 0.08);
    }

    /** Fliegt weich zu einem Punkt und blickt in Flugrichtung. */
    public void flyTowards(Vec3 destination, double speed, double accel) {
        Vec3 to = destination.subtract(this.position());
        double dist = to.length();
        if (dist < 0.01) {
            return;
        }
        Vec3 desired = to.scale(Math.min(speed, dist * 0.2) / dist);
        Vec3 vel = this.getDeltaMovement();
        vel = vel.add(desired.subtract(vel).scale(accel));
        // nie zu tief über dem Boden
        double floor = this.groundY(this.getX(), this.getZ()) + 3.0;
        if (this.getY() < floor) {
            vel = new Vec3(vel.x, Math.max(vel.y, 0.25), vel.z);
        }
        this.setDeltaMovement(vel);
        if (vel.horizontalDistanceSqr() > 0.01) {
            this.turnTowards(this.position().add(vel.x, 0, vel.z), 5.0f);
        }
    }

    /** Ohne Ziel: in der Luft über dem Nest kreisen, sonst stehen bleiben. */
    protected void hoverOrIdle(ServerLevel level) {
        if (this.isFlying() && this.home != null) {
            this.circleAround(level, Vec3.atCenterOf(this.home), 22.0, this.cruiseHeight(), 0.5);
        } else {
            this.stopMoving();
        }
    }

    @Override
    public void travel(Vec3 input) {
        if (this.getMoveMode() == MoveMode.AIR || this.getMoveMode() == MoveMode.WATER || this.noPhysics) {
            if (this.isDeadOrDying()) {
                super.travel(input);
                return;
            }
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.calculateEntityAnimation(true);
        } else {
            super.travel(input);
        }
    }

    // ------------------------------------------------------------------ Heimkehr und Schlaf

    private void tickReturning(ServerLevel level) {
        if (this.home == null) {
            this.setBossState(State.SLUMBER);
            return;
        }
        if (this.currentAttack != null) {
            this.endAttack(level);
        }
        LivingEntity target = this.pickTarget(level);
        if (target != null) {
            this.setTarget(target);
            this.setBossState(State.FIGHT);
            return;
        }
        if (this.tickCount % 20 == 0) {
            this.heal(this.getMaxHealth() * 0.02f);
        }
        Vec3 home = Vec3.atBottomCenterOf(this.home);
        double dist = this.position().subtract(home).horizontalDistance();
        if (this.returnHome(level, home, dist)) {
            return;
        }
        if (this.canFly() && !this.isFlying() && dist > 12 && this.onGround()) {
            this.setMoveMode(MoveMode.AIR);
            this.setDeltaMovement(0, 0.8, 0);
        }
        if (this.isFlying()) {
            this.flyTowards(home.add(0, dist > 10 ? 10 : 0, 0), 0.6, 0.08);
            if (dist < 4 && this.getY() - home.y < 3) {
                this.setMoveMode(MoveMode.GROUND);
            }
        } else if (dist > 3) {
            this.turnTowards(home, 4.0f);
            this.walkForward(1.0);
        } else {
            this.stopMoving();
            if (this.getHealth() >= this.getMaxHealth() * 0.99f) {
                this.setBossState(State.SLUMBER);
            }
        }
    }

    /**
     * Eigene Heimkehr (z. B. schwimmend): {@code true}, wenn die Unterklasse die Bewegung übernimmt. Ist sie
     * angekommen und geheilt, ruft sie {@link #fallAsleep()}.
     */
    protected boolean returnHome(ServerLevel level, Vec3 home, double dist) {
        return false;
    }

    /** Schläft wieder ein (für eigene Heimkehr). */
    protected void fallAsleep() {
        this.setBossState(State.SLUMBER);
    }

    /** Effekte beim Einschlafen (Standard: Partikel). */
    protected void tickSlumber(ServerLevel level, int tick) {
        level.sendParticles(this.bossParticle(), this.getX(), this.getY() + 3, this.getZ(), 8, 3, 2, 3, 0.02);
    }

    /** Der Boss ist wieder eingeschlafen – das Nest wartet auf den nächsten Besucher. */
    protected void onSlumber(ServerLevel level) {
        BossLairs.onBossSlumber(level, this);
    }

    // =====================================================================================
    // Schaden
    // =====================================================================================

    public boolean hurtFromPart(ServerLevel level, BossPart part, DamageSource source, float damage) {
        return this.damageBoss(level, source, damage * part.damageFactor);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        return this.damageBoss(level, source, damage);
    }

    private boolean damageBoss(ServerLevel level, DamageSource source, float damage) {
        State state = this.getBossState();
        if (state == State.AWAKENING || state == State.SLUMBER || state == State.DYING) {
            return false;
        }
        if (this.transitionTicks > 0 && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            if (source.getEntity() instanceof Player) {
                level.sendParticles(net.minecraft.core.particles.ParticleTypes.ENCHANTED_HIT, this.getX(), this.getY() + 4, this.getZ(),
                        12, 2.0, 2.0, 2.0, 0.3);
            }
            return false;   // im Aufbäumen prallt alles ab
        }
        if (source.is(DamageTypes.IN_WALL) || source.is(DamageTypes.CRAMMING) || source.is(DamageTypeTags.IS_FALL)
                || source.is(DamageTypes.DROWN) || source.is(DamageTypes.FLY_INTO_WALL)) {
            return false;
        }
        Entity attacker = source.getEntity();
        if (attacker instanceof DragonBossEntity) {
            return false;
        }
        if (!(attacker instanceof Player) && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            damage *= NPC_DAMAGE_FACTOR;
        }
        damage = this.modifyIncomingDamage(source, damage);
        if (damage <= 0.0f) {
            return false;
        }
        if (attacker instanceof LivingEntity living) {
            this.threat.merge(living.getUUID(), damage, Float::sum);
            if (living instanceof Player player) {
                this.participants.add(player.getUUID());
            } else if (living instanceof SoldierEntity soldier && soldier.getOwner() instanceof Player owner) {
                this.participants.add(owner.getUUID());
            }
        }
        if (state == State.RETURNING) {
            this.setBossState(State.FIGHT);
        }
        boolean hurt = super.hurtServer(level, source, damage);
        // Jeder Treffer zählt (kein Abfangen gleichzeitiger Treffer eines ganzen Heeres)
        this.damageCooldownTime = Math.min(this.damageCooldownTime, 10);
        return hurt;
    }

    /** Für Unterklassen: Schaden abschwächen (Panzer) oder ganz abwehren (0 = kein Schaden, z. B. unter der Erde). */
    protected float modifyIncomingDamage(DamageSource source, float damage) {
        return damage;
    }

    @Override
    public void knockback(double power, double xd, double zd, DamageSource source, float damage, boolean comesFromEffect) {
    }

    @Override
    public boolean canBeAffected(MobEffectInstance effect) {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void doPush(Entity entity) {
    }

    @Override
    public boolean removeWhenFarAway(double distSqr) {
        return false;
    }

    @Override
    public boolean requiresCustomPersistence() {
        return true;
    }

    @Override
    public void checkDespawn() {
    }

    @Override
    public boolean canUsePortal(boolean ignorePassenger) {
        return false;
    }

    // =====================================================================================
    // Tod
    // =====================================================================================

    @Override
    protected void handleKillingBlow() {
        super.handleKillingBlow();
        this.entityData.set(DATA_STATE, (byte) State.DYING.ordinal());
        this.entityData.set(DATA_ATTACK, BossAnims.NONE);
        this.currentAttack = null;
        this.setNoGravity(this.getMoveMode() == MoveMode.WATER);
        this.noPhysics = false;
    }

    @Override
    protected void dropAllDeathLoot(ServerLevel level, DamageSource source) {
        // Beute erst am Ende der Todesanimation
        this.deathSource = source;
    }

    @Override
    protected void tickDeath() {
        this.deathTime++;
        if (!(this.level() instanceof ServerLevel level)) {
            return;
        }
        if (this.deathTime == 1) {
            this.shake(level, 90.0, 0.9f, 60);
            for (ServerPlayer player : this.nearbyPlayers(level, ARENA_RADIUS * 1.5)) {
                this.participants.add(player.getUUID());
            }
        }
        this.tickDeathEffects(level, this.deathTime);
        if (this.deathTime >= DEATH_TICKS && !this.isRemoved()) {
            this.finishDeath(level);
        }
    }

    private void finishDeath(ServerLevel level) {
        DamageSource source = this.deathSource != null ? this.deathSource : this.damageSources().generic();
        super.dropAllDeathLoot(level, source);
        int xp = 1200;
        for (int i = 0; i < 12; i++) {
            ExperienceOrb.award(level, this.position().add(0, 2, 0), xp / 12);
        }
        BlockPos eggPos = this.home != null ? this.home : this.blockPosition();
        this.onDefeated(level, eggPos);
        this.shake(level, 90.0, 0.6f, 20);
        this.remove(Entity.RemovalReason.KILLED);
    }

    // =====================================================================================
    // Bossleiste und Trefferzonen
    // =====================================================================================

    private void updateBossBar(ServerLevel level) {
        this.bossEvent.setProgress(this.getBossState() == State.AWAKENING
                ? Math.min(1.0f, (float) this.stateTicks / this.awakeningTicks())
                : this.getHealth() / this.getMaxHealth());
        Set<ServerPlayer> wanted = new HashSet<>();
        if (this.getBossState() != State.SLUMBER && this.isAlive()) {
            for (ServerPlayer player : level.players()) {
                if (player.distanceTo(this) < 96.0 && player.level() == level) {
                    wanted.add(player);
                }
            }
        }
        for (ServerPlayer player : List.copyOf(this.barPlayers)) {
            if (!wanted.contains(player)) {
                this.bossEvent.removePlayer(player);
                this.barPlayers.remove(player);
            }
        }
        for (ServerPlayer player : wanted) {
            if (this.barPlayers.add(player)) {
                this.bossEvent.addPlayer(player);
            }
        }
    }

    @Override
    public void remove(Entity.RemovalReason reason) {
        super.remove(reason);
        this.bossEvent.removeAllPlayers();
        this.barPlayers.clear();
    }

    /** Auch wenn der Chunk entladen wird (Spieler weit weg), verschwindet die Bossleiste von den Bildschirmen. */
    @Override
    public void onRemovedFromLevel() {
        super.onRemovedFromLevel();
        this.bossEvent.removeAllPlayers();
        this.barPlayers.clear();
    }

    @Override
    public boolean isMultipartEntity() {
        return true;
    }

    @Override
    public PartEntity<?>[] getParts() {
        return this.parts;
    }

    public BossPart[] bossParts() {
        return this.parts;
    }

    @Override
    public void recreateFromPacket(ClientboundAddEntityPacket packet) {
        super.recreateFromPacket(packet);
        for (int i = 0; i < this.parts.length; i++) {
            this.parts[i].setId(i + packet.getId() + 1);
        }
    }

    /** Setzt eine Trefferzone an einen Punkt relativ zum Körper (Blöcke: x links, y oben, z vorn). */
    protected void placePart(BossPart part, double x, double y, double z) {
        Vec3 p = this.bodyPoint(x, y, z);
        part.setOldPosAndRot();
        part.setPos(p.x, p.y - part.getBbHeight() / 2.0, p.z);
    }

    /** Für Angriffe, die den Kopf weit nach vorn bringen: Lage des Kopfes in Blöcken vor/über dem Körper. */
    protected float attackHeadReach() {
        int anim = this.getAttackAnim();
        return anim == BossAnims.BITE ? 2.5f : anim == BossAnims.BREATH ? 1.0f : 0.0f;
    }
}
