package io.github.sirschrolli.kingdomsatwar.entity.abyss;

import io.github.sirschrolli.kingdomsatwar.abyss.hoard.HoardPiece;
import io.github.sirschrolli.kingdomsatwar.abyss.WorldSavior;
import io.github.sirschrolli.kingdomsatwar.block.abyss.HoardSealBlockEntity;
import io.github.sirschrolli.kingdomsatwar.registry.AbyssBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.entity.PartEntity;
import org.jspecify.annotations.Nullable;

/**
 * Der Weltenfresser – das stärkste Wesen der Mod, versiegelt unter dem Schlund der Welten im Abgrund. Eine kolossale
 * Schlange aus Chaosgestein: ein Kopf mit einem runden Rachen voller Zähne, zwölf gepanzerte Glieder, auf deren Rücken
 * Stücke verschlungener Welten liegen, ein Schwanz mit einer Flosse aus Kristall. Sein Leib folgt genau der Bahn des Kopfes.
 * <ul>
 *     <li><b>Erwachen:</b> Die sieben Kronensplitter um den Schlund zerspringen nacheinander, dann steigt er brüllend auf.</li>
 *     <li><b>Biss:</b> Er bäumt sich über seinem Opfer auf – ein Glutkreis warnt – und stößt mit offenem Rachen herab.</li>
 *     <li><b>Weltenhunger:</b> Am Rand des Rings reißt er den Rachen auf und saugt alles an (Schleichen hält dagegen),
 *     dann schnappt er zu.</li>
 *     <li><b>Weltenspucke:</b> Er speit Brocken verschlungener Welten: Glut (Brand), Frost (Langsamkeit), End (Schweben),
 *     Schatten (Blindheit).</li>
 *     <li><b>Leibhieb:</b> Er fegt tief über den Ring, und sein ganzer Leib folgt – wer auf der Bahn steht, wird
 *     davongeschleudert.</li>
 *     <li><b>Rissatem</b> (unter 75 %): Hoch über dem Schlund speit er einen Strahl aus dem Riss in seinem Rachen, der
 *     kreisend über den Ring fegt.</li>
 *     <li><b>Schwerkraftsturz</b> (unter 70 %): Alle auf dem Ring schweben empor – wer schleicht, bleibt unten –, dann
 *     stürzen sie.</li>
 *     <li><b>Ruf der Welten</b> (bei 66 % und 33 %): Aus Rissen im Ring kommen Rissjäger und Zerrlinge.</li>
 *     <li><b>Zorn</b> (einmal bei 50 %): Der Himmel flammt auf, der äußere Rand des Rings bricht weg, und er wird schneller.</li>
 * </ul>
 * Besiegt zerbricht er Glied um Glied; wer dabei war, ist fortan <b>Weltenretter</b>.
 */
public class WorldEaterEntity extends Monster {
    public static final byte EVENT_AWAKEN = 90;
    public static final byte EVENT_ROAR = 91;
    public static final byte EVENT_WRATH = 92;
    public static final byte EVENT_BITE = 93;

    public static final int SEGMENTS = 12;
    /** Abstände entlang der Bahn (Blöcke): Kopf → erstes Glied, Glied → Glied, letztes Glied → Schwanz. */
    public static final double HEAD_GAP = 5.4;
    public static final double SPACING = 3.9;
    public static final double TAIL_GAP = 4.2;
    /** Kopfmitte über der Unterkante des Trefferquaders. */
    public static final double HEAD_CENTER = 2.25;
    private static final int TRAIL = 900;

    public static final int AWAKEN_TICKS = 160;
    /** Ab diesem Tick des Erwachens steigt er empor (nach dem letzten Kronensplitter). */
    public static final int RISE_TICK = 84;
    public static final int BITE_TICKS = 72;
    public static final int BITE_STRIKE = 34;
    public static final int DEVOUR_TICKS = 104;
    public static final int DEVOUR_SNAP = 80;
    public static final int SPIT_TICKS = 62;
    public static final int SWEEP_TICKS = 100;
    public static final int BREATH_TICKS = 124;
    public static final int BREATH_START = 34;
    public static final int BREATH_STOP = 104;
    public static final int GRAVITY_TICKS = 100;
    public static final int GRAVITY_DROP = 66;
    public static final int CALL_TICKS = 64;
    public static final int WRATH_TICKS = 70;
    public static final int DEATH_TICKS = 170;
    public static final double ORBIT = 40.0;
    /** Erfahrung beim Sieg (wie beim Zerbrochenen König und bei Asteria). */
    public static final int XP_REWARD = 500;

    private static final EntityDataAccessor<Float> DATA_MAW = SynchedEntityData.defineId(WorldEaterEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> DATA_BREATH = SynchedEntityData.defineId(WorldEaterEntity.class,
            EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<org.joml.Vector3fc> DATA_BREATH_POINT = SynchedEntityData.defineId(WorldEaterEntity.class,
            EntityDataSerializers.VECTOR3);
    private static final EntityDataAccessor<Boolean> DATA_WRATH = SynchedEntityData.defineId(WorldEaterEntity.class, EntityDataSerializers.BOOLEAN);

    private enum Phase { AWAKEN, CIRCLE, BITE, DEVOUR, SPIT, SWEEP, BREATH, GRAVITY, CALL, WRATH }

    private final ServerBossEvent bossBar = new ServerBossEvent(UUID.randomUUID(), Component.translatable("boss.kingdomsatwar.world_eater"),
            BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.NOTCHED_20);
    private final WorldEaterPart[] parts = new WorldEaterPart[SEGMENTS + 1];
    // die Bahn des Kopfes (Mittelpunkte), neueste zuerst
    private final double[] trailX = new double[TRAIL];
    private final double[] trailY = new double[TRAIL];
    private final double[] trailZ = new double[TRAIL];
    private int trailStart;
    private int trailCount;
    /** Lage der Glieder (Mittelpunkte, Index SEGMENTS = Schwanz) in diesem und im letzten Tick – für Treffer und Bild. */
    private final Vec3[] bodyNow = new Vec3[SEGMENTS + 1];
    private final Vec3[] bodyOld = new Vec3[SEGMENTS + 1];

    private Phase phase = Phase.AWAKEN;
    private int phaseTick;
    private int cooldown = 40;
    private Vec3 heading = new Vec3(0.0, 1.0, 0.0);
    private double orbitAngle;
    private int orbitDir = 1;
    private int dive = -1;
    private int circles;
    private @Nullable BlockPos home;
    private @Nullable BlockPos seal;
    private int lonely;
    private boolean wrath;
    private int calls;
    private int gravityCooldown;
    private float mawClient;
    private float mawClientO;
    // laufender Angriff
    private Vec3 aim = Vec3.ZERO;
    private Vec3 aim2 = Vec3.ZERO;
    private int volley;
    private float breathAngle;
    private final Set<UUID> struck = new HashSet<>();
    private final List<UUID> summoned = new ArrayList<>();
    /** Für Tests und die Vorführung: feste Ziele und keine selbst gewählten Angriffe. */
    private @Nullable LivingEntity debugTarget;
    private boolean debugHold;

    public WorldEaterEntity(EntityType<? extends WorldEaterEntity> type, Level level) {
        super(type, level);
        // die Glieder sofort anlegen: ihre Kennungen folgen auf die des Kopfes (wie beim Enderdrachen)
        for (int i = 0; i < SEGMENTS; i++) {
            this.parts[i] = new WorldEaterPart(this, i, 4.4f, 4.4f, 0.55f);
        }
        this.parts[SEGMENTS] = new WorldEaterPart(this, SEGMENTS, 3.4f, 3.4f, 0.8f);
        this.xpReward = 0;
        this.setNoGravity(true);
        this.noPhysics = true;
        this.setPersistenceRequired();
        for (int i = 0; i <= SEGMENTS; i++) {
            this.bodyNow[i] = Vec3.ZERO;
            this.bodyOld[i] = Vec3.ZERO;
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                // das stärkste Wesen der Mod: mehr Leben und Panzer als der Zerbrochene König (1800, Rüstung 18)
                .add(Attributes.MAX_HEALTH, 2600.0)
                .add(Attributes.ARMOR, 20.0)
                .add(Attributes.ARMOR_TOUGHNESS, 12.0)
                .add(Attributes.ATTACK_DAMAGE, 16.0)
                .add(Attributes.FOLLOW_RANGE, 96.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_MAW, 0.15f);
        builder.define(DATA_BREATH, false);
        builder.define(DATA_BREATH_POINT, new org.joml.Vector3f());
        builder.define(DATA_WRATH, false);
    }

    // ------------------------------------------------------------------ Glieder

    @Override
    public boolean isMultipartEntity() {
        return true;
    }

    @Override
    public PartEntity<?>[] getParts() {
        return this.parts;
    }

    public WorldEaterPart[] eaterParts() {
        return this.parts;
    }

    @Override
    public void recreateFromPacket(ClientboundAddEntityPacket packet) {
        super.recreateFromPacket(packet);
        for (int i = 0; i < this.parts.length; i++) {
            this.parts[i].setId(i + packet.getId() + 1);
        }
    }

    /** Mittelpunkt des Kopfes. */
    public Vec3 headCenter() {
        return this.position().add(0.0, HEAD_CENTER, 0.0);
    }

    /** Wo der Rachen ist (vor der Kopfmitte, in Blickrichtung). */
    public Vec3 mawPosition() {
        return this.headCenter().add(this.heading().scale(3.6));
    }

    /** Blickrichtung des Kopfes. */
    public Vec3 heading() {
        return Vec3.directionFromRotation(this.getXRot(), this.getYRot());
    }

    /** Mittelpunkt von Glied {@code i} (SEGMENTS = Schwanz) – weich zwischen zwei Ticks. */
    public Vec3 bodyPoint(int i, float partialTicks) {
        Vec3 a = this.bodyOld[i];
        Vec3 b = this.bodyNow[i];
        return new Vec3(Mth.lerp(partialTicks, a.x, b.x), Mth.lerp(partialTicks, a.y, b.y), Mth.lerp(partialTicks, a.z, b.z));
    }

    /** Wie weit der Rachen offen ist (0 … 1), im Bild weich. */
    public float maw(float partialTicks) {
        return Mth.lerp(partialTicks, this.mawClientO, this.mawClient);
    }

    public boolean isBreathing() {
        return this.entityData.get(DATA_BREATH);
    }

    public Vec3 breathPoint() {
        org.joml.Vector3fc v = this.entityData.get(DATA_BREATH_POINT);
        return new Vec3(v.x(), v.y(), v.z());
    }

    public boolean isWrathful() {
        return this.entityData.get(DATA_WRATH);
    }

    public boolean isAwakening() {
        return this.phase == Phase.AWAKEN;
    }

    private void pushTrail(Vec3 p) {
        if (this.trailCount > 0) {
            double dx = p.x - this.trailX[this.trailStart];
            double dy = p.y - this.trailY[this.trailStart];
            double dz = p.z - this.trailZ[this.trailStart];
            if (dx * dx + dy * dy + dz * dz < 0.0025) {
                return;
            }
        }
        this.trailStart = (this.trailStart + TRAIL - 1) % TRAIL;
        this.trailX[this.trailStart] = p.x;
        this.trailY[this.trailStart] = p.y;
        this.trailZ[this.trailStart] = p.z;
        this.trailCount = Math.min(TRAIL, this.trailCount + 1);
    }

    /** Punkt auf der Bahn {@code distance} Blöcke hinter dem Kopf (über das Ende hinaus: gerade nach hinten verlängert). */
    private Vec3 trailPoint(Vec3 head, double distance) {
        double left = distance;
        double px = head.x;
        double py = head.y;
        double pz = head.z;
        for (int k = 0; k < this.trailCount; k++) {
            int idx = (this.trailStart + k) % TRAIL;
            double qx = this.trailX[idx];
            double qy = this.trailY[idx];
            double qz = this.trailZ[idx];
            double d = Math.sqrt((qx - px) * (qx - px) + (qy - py) * (qy - py) + (qz - pz) * (qz - pz));
            if (d >= left && d > 1.0e-6) {
                double t = left / d;
                return new Vec3(px + (qx - px) * t, py + (qy - py) * t, pz + (qz - pz) * t);
            }
            left -= d;
            px = qx;
            py = qy;
            pz = qz;
        }
        Vec3 back = this.heading().scale(-1.0);
        if (this.trailCount >= 2) {
            int last = (this.trailStart + this.trailCount - 1) % TRAIL;
            int prev = (this.trailStart + this.trailCount - 2) % TRAIL;
            Vec3 dir = new Vec3(this.trailX[last] - this.trailX[prev], this.trailY[last] - this.trailY[prev], this.trailZ[last] - this.trailZ[prev]);
            if (dir.lengthSqr() > 1.0e-6) {
                back = dir.normalize();
            }
        }
        return new Vec3(px, py, pz).add(back.scale(left));
    }

    /** Legt die Glieder entlang der Bahn und setzt die Trefferzonen dorthin. */
    private void updateBody() {
        Vec3 head = this.headCenter();
        this.pushTrail(head);
        for (int i = 0; i <= SEGMENTS; i++) {
            double d = i < SEGMENTS ? HEAD_GAP + i * SPACING : HEAD_GAP + (SEGMENTS - 1) * SPACING + TAIL_GAP;
            Vec3 p = this.trailPoint(head, d);
            this.bodyOld[i] = this.bodyNow[i] == Vec3.ZERO ? p : this.bodyNow[i];
            this.bodyNow[i] = p;
            WorldEaterPart part = this.parts[i];
            part.setOldPosAndRot();
            part.setPos(p.x, p.y - part.getBbHeight() / 2.0, p.z);
        }
    }

    // ------------------------------------------------------------------ Bossleiste und Grundwerte

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
    public boolean causeFallDamage(double distance, float multiplier, DamageSource source) {
        return false;
    }

    /** Er fliegt nicht von selbst: Jede Bewegung macht {@link #steer}. */
    @Override
    public void travel(Vec3 input) {
    }

    @Override
    public boolean isInvulnerableTo(ServerLevel level, DamageSource source) {
        return this.phase == Phase.AWAKEN || super.isInvulnerableTo(level, source);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        // der offene Rachen ist die verwundbarste Stelle
        float factor = this.entityData.get(DATA_MAW) > 0.6f ? 1.5f : 1.0f;
        return super.hurtServer(level, source, damage * factor);
    }

    /** Ein Treffer auf ein Glied. */
    public boolean hurtFromPart(ServerLevel level, WorldEaterPart part, DamageSource source, float damage) {
        if (this.phase == Phase.AWAKEN || this.deathTime > 0) {
            return false;
        }
        return super.hurtServer(level, source, damage * part.damageFactor);
    }

    /** Verknüpft ihn mit dem Schlund der Welten (Mitte des Rings, ein Block über dem Boden) und dem Siegel. */
    public void bindTo(BlockPos home, BlockPos seal) {
        this.home = home.immutable();
        this.seal = seal.immutable();
    }

    public @Nullable BlockPos home() {
        return this.home;
    }

    private Vec3 center() {
        BlockPos h = this.home != null ? this.home : this.blockPosition();
        return new Vec3(h.getX() + 0.5, h.getY(), h.getZ() + 0.5);
    }

    /** Lässt ihn erwachen: unter dem Schlund, unverwundbar; die Kronensplitter zerspringen nacheinander. */
    public void beginAwakening(ServerLevel level) {
        this.phase = Phase.AWAKEN;
        this.phaseTick = 0;
        Vec3 c = this.center();
        this.snapTo(c.x, c.y - 36.0, c.z, 0.0f, -90.0f);
        this.heading = new Vec3(0.0, 1.0, 0.0);
        this.setXRot(-90.0f);
        this.trailCount = 0;
        for (int i = 0; i < 40; i++) {
            this.pushTrail(this.headCenter().add(0.0, -i * 1.5, 0.0));
        }
        level.broadcastEntityEvent(this, EVENT_AWAKEN);
    }

    // ------------------------------------------------------------------ Ablauf

    @Override
    public void tick() {
        super.tick();
        this.updateBody();
        if (this.level().isClientSide()) {
            this.mawClientO = this.mawClient;
            this.mawClient += (this.entityData.get(DATA_MAW) - this.mawClient) * 0.35f;
            this.clientParticles();
        }
    }

    @Override
    public void handleEntityEvent(byte id) {
        switch (id) {
            case EVENT_WRATH -> {
                if (this.level().isClientSide()) {
                    io.github.sirschrolli.kingdomsatwar.abyss.SkyFlare.flare(this.level().getGameTime());
                }
            }
            case EVENT_AWAKEN, EVENT_ROAR, EVENT_BITE -> {
            }
            default -> super.handleEntityEvent(id);
        }
    }

    private void clientParticles() {
        Level level = this.level();
        Vec3 maw = this.mawPosition();
        float open = this.entityData.get(DATA_MAW);
        if (open > 0.3f && this.random.nextInt(2) == 0) {
            level.addParticle(ParticleTypes.FLAME, maw.x + (this.random.nextDouble() - 0.5), maw.y + (this.random.nextDouble() - 0.5),
                    maw.z + (this.random.nextDouble() - 0.5), 0.0, 0.02, 0.0);
        }
        // Glut aus den Nähten des Leibs
        if (this.random.nextInt(2) == 0) {
            int i = this.random.nextInt(SEGMENTS + 1);
            Vec3 p = this.bodyNow[i];
            level.addParticle(ParticleTypes.LAVA, p.x + (this.random.nextDouble() - 0.5) * 3.0, p.y + (this.random.nextDouble() - 0.5) * 3.0,
                    p.z + (this.random.nextDouble() - 0.5) * 3.0, 0.0, 0.0, 0.0);
        }
        if (this.isBreathing()) {
            Vec3 to = this.breathPoint();
            Vec3 d = to.subtract(maw);
            for (int k = 0; k < 5; k++) {
                Vec3 p = maw.add(d.scale(this.random.nextDouble()));
                level.addParticle(k % 2 == 0 ? ParticleTypes.FLAME : ParticleTypes.SMALL_FLAME, p.x, p.y, p.z, 0.0, 0.02, 0.0);
            }
            level.addParticle(ParticleTypes.LAVA, to.x, to.y + 0.2, to.z, 0.0, 0.0, 0.0);
        }
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        this.bossBar.setProgress(this.getHealth() / this.getMaxHealth());
        if (this.home == null) {
            this.home = this.blockPosition().above(20);
        }
        this.phaseTick++;
        if (this.gravityCooldown > 0) {
            this.gravityCooldown--;
        }
        LivingEntity target = this.findTarget(level);
        if (target == null && this.phase == Phase.CIRCLE && !this.debugHold) {
            if (++this.lonely > 600) {
                this.slumber(level);
                return;
            }
        } else {
            this.lonely = 0;
        }
        switch (this.phase) {
            case AWAKEN -> this.tickAwaken(level);
            case CIRCLE -> this.tickCircle(level, target);
            case BITE -> this.tickBite(level, target);
            case DEVOUR -> this.tickDevour(level);
            case SPIT -> this.tickSpit(level, target);
            case SWEEP -> this.tickSweep(level);
            case BREATH -> this.tickBreath(level);
            case GRAVITY -> this.tickGravity(level);
            case CALL -> this.tickCall(level);
            case WRATH -> this.tickWrath(level);
        }
    }

    /** Spieler auf oder über dem Ring (oder das feste Ziel der Tests). */
    private @Nullable LivingEntity findTarget(ServerLevel level) {
        if (this.debugTarget != null) {
            return this.debugTarget.isAlive() ? this.debugTarget : null;
        }
        Vec3 c = this.center();
        LivingEntity best = null;
        double bestD = Double.MAX_VALUE;
        for (ServerPlayer p : level.players()) {
            if (p.isSpectator() || p.isCreative() || !p.isAlive()) {
                continue;
            }
            double dx = p.getX() - c.x;
            double dz = p.getZ() - c.z;
            double dy = p.getY() - c.y;
            if (dx * dx + dz * dz < 70.0 * 70.0 && dy > -40 && dy < 50) {
                double d = p.distanceToSqr(this);
                if (d < bestD) {
                    bestD = d;
                    best = p;
                }
            }
        }
        return best;
    }

    private List<Player> playersNear(ServerLevel level, double radius) {
        Vec3 c = this.center();
        return level.getEntitiesOfClass(Player.class, new AABB(c, c).inflate(radius, 40.0, radius),
                p -> p.isAlive() && !p.isSpectator() && !p.isCreative());
    }

    private void setPhase(Phase phase) {
        this.phase = phase;
        this.phaseTick = 0;
        this.struck.clear();
        this.entityData.set(DATA_BREATH, false);
    }

    private void setMaw(float open) {
        this.entityData.set(DATA_MAW, open);
    }

    // ------------------------------------------------------------------ Fliegen

    /** Lenkt den Kopf auf {@code goal} zu: höchstens {@code turn} Grad Wendung je Tick, {@code speed} Blöcke je Tick. */
    private void steer(Vec3 goal, double speed, double turn) {
        Vec3 head = this.headCenter();
        Vec3 want = goal.subtract(head);
        double dist = want.length();
        if (dist < 1.0e-3) {
            return;
        }
        want = want.scale(1.0 / dist);
        double cos = Mth.clamp(this.heading.dot(want), -1.0, 1.0);
        double angle = Math.acos(cos);
        double max = Math.toRadians(turn);
        Vec3 dir;
        if (angle <= max) {
            dir = want;
        } else {
            double t = max / angle;
            dir = this.heading.add(want.subtract(this.heading).scale(t));
            if (dir.lengthSqr() < 1.0e-6) {
                dir = this.heading.add(0.1, 0.0, 0.1);
            }
            dir = dir.normalize();
        }
        this.heading = dir;
        double step = Math.min(speed * (this.wrath ? 1.18 : 1.0), dist);
        Vec3 next = this.position().add(dir.scale(step));
        this.setPos(next.x, next.y, next.z);
        this.setDeltaMovement(dir.scale(step));
        float yaw = (float) (Mth.atan2(-dir.x, dir.z) * Mth.RAD_TO_DEG);
        float pitch = (float) (-Math.asin(Mth.clamp(dir.y, -1.0, 1.0)) * Mth.RAD_TO_DEG);
        this.setYRot(yaw);
        this.setXRot(pitch);
        this.yBodyRot = yaw;
        this.yHeadRot = yaw;
    }

    /** Ein Punkt der Umlaufbahn um den Ring. */
    private Vec3 orbitPoint(double angle, double height) {
        Vec3 c = this.center();
        return new Vec3(c.x + Math.cos(angle) * ORBIT, c.y + height, c.z + Math.sin(angle) * ORBIT);
    }

    private void tickCircle(ServerLevel level, @Nullable LivingEntity target) {
        this.setMaw(0.12f);
        Vec3 c = this.center();
        if (this.dive >= 0) {
            // ein Tauchgang: unter den Ring, durch den Schlund empor, zurück auf die Bahn
            Vec3[] path = {c.add(0.0, -26.0, 0.0), c.add(0.0, 26.0, 0.0)};
            Vec3 goal = path[Math.min(this.dive, 1)];
            this.steer(goal, 1.05, 6.5);
            if (this.headCenter().distanceTo(goal) < 4.0) {
                this.dive++;
                if (this.dive > 1) {
                    this.dive = -1;
                    Vec3 h = this.headCenter();
                    this.orbitAngle = Math.atan2(h.z - c.z, h.x - c.x);
                }
            }
            return;
        }
        this.orbitAngle += this.orbitDir * 0.95 / ORBIT;
        double height = 6.0 + 8.0 * Math.sin(this.orbitAngle * 2.0 + this.circles);
        this.steer(this.orbitPoint(this.orbitAngle + this.orbitDir * 0.35, height), 0.95, 7.0);
        if (Math.abs(this.orbitAngle) > Mth.TWO_PI * (this.circles + 1)) {
            this.circles++;
            if (this.circles % 3 == 2 && target != null) {
                this.dive = 0;
            }
        }
        if (this.debugHold || target == null) {
            return;
        }
        // Zorn und Ruf der Welten kommen, wenn ihre Zeit da ist; sonst der nächste Angriff
        float hp = this.getHealth() / this.getMaxHealth();
        if (!this.wrath && hp < 0.5f) {
            this.wrath = true;
            this.entityData.set(DATA_WRATH, true);
            this.setPhase(Phase.WRATH);
            return;
        }
        if (this.calls == 0 && hp < 0.66f || this.calls == 1 && hp < 0.33f) {
            this.calls++;
            this.setPhase(Phase.CALL);
            return;
        }
        if (--this.cooldown > 0) {
            return;
        }
        this.cooldown = (this.wrath ? 24 : 40) + this.random.nextInt(this.wrath ? 20 : 30);
        this.startAttack(level, target, hp);
    }

    private void startAttack(ServerLevel level, LivingEntity target, float hp) {
        List<Phase> choices = new ArrayList<>();
        choices.add(Phase.BITE);
        choices.add(Phase.BITE);
        choices.add(Phase.SPIT);
        choices.add(Phase.SPIT);
        choices.add(Phase.DEVOUR);
        choices.add(Phase.SWEEP);
        if (hp < 0.75f) {
            choices.add(Phase.BREATH);
            choices.add(Phase.BREATH);
        }
        if (hp < 0.7f && this.gravityCooldown <= 0) {
            choices.add(Phase.GRAVITY);
        }
        Phase next = choices.get(this.random.nextInt(choices.size()));
        this.beginAttack(level, next, target);
    }

    private void beginAttack(ServerLevel level, Phase next, LivingEntity target) {
        this.setPhase(next);
        Vec3 c = this.center();
        switch (next) {
            case BITE -> this.aim = target.position();
            case DEVOUR -> {
                Vec3 out = new Vec3(target.getX() - c.x, 0.0, target.getZ() - c.z);
                out = out.lengthSqr() < 1.0 ? new Vec3(1.0, 0.0, 0.0) : out.normalize();
                this.aim = c.add(out.scale(HoardPiece.R_OUT + 6.0)).add(0.0, 3.0, 0.0);
            }
            case SPIT -> {
                this.aim = target.position();
                this.volley = 0;
            }
            case SWEEP -> {
                Vec3 across = new Vec3(target.getX() - c.x, 0.0, target.getZ() - c.z);
                across = across.lengthSqr() < 1.0 ? new Vec3(0.0, 0.0, 1.0) : across.normalize();
                // die Bahn läuft quer zum Ziel durch die Nähe der Mitte – wer auf ihr steht, muss zur Seite
                Vec3 side = new Vec3(-across.z, 0.0, across.x);
                double off = target.position().subtract(c).dot(across);
                Vec3 mid = c.add(across.scale(Mth.clamp(off, -14.0, 14.0)));
                this.aim = mid.add(side.scale(HoardPiece.R_OUT + 14.0)).add(0.0, 2.6, 0.0);
                this.aim2 = mid.subtract(side.scale(HoardPiece.R_OUT + 14.0)).add(0.0, 2.6, 0.0);
            }
            case BREATH -> this.breathAngle = (float) Math.atan2(target.getZ() - c.z, target.getX() - c.x);
            case GRAVITY -> this.gravityCooldown = 600;
            default -> {
            }
        }
    }

    private void backToOrbit() {
        Vec3 h = this.headCenter();
        Vec3 c = this.center();
        this.orbitAngle = Math.atan2(h.z - c.z, h.x - c.x);
        this.orbitDir = this.random.nextBoolean() ? 1 : -1;
        this.setPhase(Phase.CIRCLE);
    }

    // ------------------------------------------------------------------ Erwachen

    private void tickAwaken(ServerLevel level) {
        Vec3 c = this.center();
        int t = this.phaseTick;
        // die sieben Kronensplitter zerspringen nacheinander
        if (t >= 10 && t <= 10 + 6 * 12 && (t - 10) % 12 == 0 && this.home != null) {
            int k = (t - 10) / 12;
            BlockPos shard = HoardPiece.shardPosition(this.home, k);
            if (level.getBlockState(shard).is(AbyssBlocks.SEAL_SHARD.get())) {
                level.setBlock(shard, Blocks.AIR.defaultBlockState(), 3);
            }
            Vec3 p = Vec3.atCenterOf(shard);
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, AbyssBlocks.SEAL_SHARD.get().defaultBlockState()), p.x, p.y, p.z, 40,
                    0.4, 0.8, 0.4, 0.2);
            level.sendParticles(ParticleTypes.END_ROD, p.x, p.y, p.z, 30, 0.3, 0.6, 0.3, 0.15);
            level.playSound(null, shard, ModSounds.WORLD_EATER_SHATTER.get(), SoundSource.HOSTILE, 3.0f, 0.8f + k * 0.06f);
            this.shake(level, 90.0, 0.25f + k * 0.04f, 10);
        }
        if (t == 2) {
            level.playSound(null, BlockPos.containing(c), ModSounds.WORLD_EATER_AWAKEN.get(), SoundSource.HOSTILE, 6.0f, 1.0f);
        }
        if (t < RISE_TICK) {
            // er wartet tief unter dem Schlund; aus der Tiefe steigt Glut auf
            if (t % 3 == 0) {
                level.sendParticles(ParticleTypes.LAVA, c.x, c.y - 2.0, c.z, 6, HoardPiece.R_IN * 0.4, 1.0, HoardPiece.R_IN * 0.4, 0.0);
                level.sendParticles(ParticleTypes.LARGE_SMOKE, c.x, c.y - 4.0, c.z, 10, HoardPiece.R_IN * 0.4, 2.0, HoardPiece.R_IN * 0.4, 0.04);
            }
            return;
        }
        // er steigt durch den Schlund empor (der Kopf bricht nach gut 26 Ticks durch), beschreibt einen Bogen und brüllt
        this.setMaw(t > RISE_TICK + 36 ? 1.0f : 0.4f);
        boolean ascending = t < RISE_TICK + 54;
        Vec3 goal = ascending ? c.add(0.0, 30.0, 0.0) : this.orbitPoint(0.0, 10.0);
        this.steer(goal, ascending ? 1.3 : 1.0, 9.0);
        Vec3 head = this.headCenter();
        if (t > RISE_TICK + 16 && head.y > c.y - 6.0 && head.y < c.y + 4.0) {
            // der Durchbruch: Glut und Brocken spritzen aus dem Schlund
            level.sendParticles(ParticleTypes.LAVA, head.x, c.y, head.z, 14, 3.0, 0.5, 3.0, 0.0);
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, AbyssBlocks.CHAOS_STONE.base().get().defaultBlockState()), head.x, c.y,
                    head.z, 30, 4.0, 1.0, 4.0, 0.3);
            level.sendParticles(ParticleTypes.LARGE_SMOKE, head.x, c.y, head.z, 12, 4.0, 1.0, 4.0, 0.05);
        }
        if (t == RISE_TICK + 42) {
            this.roar(level, 2.0f);
        }
        if (t >= AWAKEN_TICKS) {
            this.backToOrbit();
        }
    }

    // ------------------------------------------------------------------ Biss

    private void tickBite(ServerLevel level, @Nullable LivingEntity target) {
        int t = this.phaseTick;
        Vec3 c = this.center();
        if (t < BITE_STRIKE - 8 && target != null) {
            this.aim = target.position();
        }
        if (t < BITE_STRIKE) {
            // aufbäumen: hoch über dem Ziel, von der Mitte her
            Vec3 away = this.aim.subtract(c);
            away = new Vec3(away.x, 0.0, away.z);
            away = away.lengthSqr() < 1.0 ? new Vec3(1.0, 0.0, 0.0) : away.normalize();
            Vec3 high = this.aim.add(away.scale(-7.0)).add(0.0, 15.0, 0.0);
            this.steer(high, t < 20 ? 1.4 : 0.6, 10.0);
            this.setMaw(Mth.clamp((t - 14) / 14.0f, 0.2f, 1.0f));
            if (t % 3 == 0) {
                this.warnRing(level, this.aim, 4.0);
            }
            if (t == 16) {
                level.playSound(null, BlockPos.containing(this.headCenter()), ModSounds.WORLD_EATER_ROAR.get(), SoundSource.HOSTILE, 3.0f, 1.25f);
            }
            return;
        }
        if (t < BITE_STRIKE + 10) {
            this.steer(this.aim.add(0.0, 1.0, 0.0), 2.4, 24.0);
            this.setMaw(1.0f);
            if (this.headCenter().distanceTo(this.aim) < 3.2 || t == BITE_STRIKE + 9) {
                if (this.struck.isEmpty()) {
                    this.struck.add(this.getUUID());
                    this.setMaw(0.05f);
                    Vec3 hit = this.headCenter();
                    level.playSound(null, BlockPos.containing(hit), ModSounds.WORLD_EATER_BITE.get(), SoundSource.HOSTILE, 4.0f, 1.0f);
                    level.sendParticles(ParticleTypes.EXPLOSION, hit.x, hit.y, hit.z, 3, 1.5, 0.5, 1.5, 0.0);
                    level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, AbyssBlocks.CHAOS_STONE.base().get().defaultBlockState()),
                            this.aim.x, this.aim.y + 0.2, this.aim.z, 60, 2.0, 0.2, 2.0, 0.2);
                    this.impact(level, this.aim, 4.2, this.wrath ? 20.0f : 16.0f, 1.4, 0.6);
                    this.shake(level, 40.0, 0.5f, 12);
                }
            }
            return;
        }
        this.setMaw(0.15f);
        this.steer(this.orbitPoint(Math.atan2(this.headCenter().z - c.z, this.headCenter().x - c.x), 12.0), 1.0, 8.0);
        if (t >= BITE_TICKS) {
            this.backToOrbit();
        }
    }

    // ------------------------------------------------------------------ Weltenhunger

    private void tickDevour(ServerLevel level) {
        int t = this.phaseTick;
        Vec3 c = this.center();
        if (t < 26) {
            this.steer(this.aim, 1.4, 10.0);
            this.setMaw(0.4f);
            return;
        }
        if (t < DEVOUR_SNAP) {
            // am Rand verharren, den Rachen zur Mitte gewandt, und alles ansaugen
            Vec3 face = c.add(0.0, 1.5, 0.0);
            this.steer(this.aim.add(face.subtract(this.aim).normalize().scale(0.6)), 0.08, 14.0);
            this.setMaw(1.0f);
            if (t == 28) {
                level.playSound(null, BlockPos.containing(this.headCenter()), ModSounds.WORLD_EATER_INHALE.get(), SoundSource.HOSTILE, 4.0f, 1.0f);
            }
            Vec3 maw = this.mawPosition();
            for (Player player : this.playersNear(level, HoardPiece.R_OUT + 10.0)) {
                Vec3 to = maw.subtract(player.position().add(0.0, 1.0, 0.0));
                double d = to.length();
                if (d > 28.0 || d < 0.5) {
                    continue;
                }
                double pull = (player.isShiftKeyDown() ? 0.035 : 0.095) * (this.wrath ? 1.2 : 1.0) * (1.0 - d / 34.0);
                player.setDeltaMovement(player.getDeltaMovement().scale(0.9).add(to.scale(pull / d * 2.2)));
                player.needsSync = true;
                player.resetFallDistance();
            }
            if (t % 2 == 0) {
                for (int k = 0; k < 6; k++) {
                    double a = this.random.nextDouble() * Mth.TWO_PI;
                    double r = 4.0 + this.random.nextDouble() * 16.0;
                    Vec3 p = maw.add(this.heading().scale(r)).add(Math.cos(a) * r * 0.4, (this.random.nextDouble() - 0.5) * 4.0, Math.sin(a) * r * 0.4);
                    Vec3 v = maw.subtract(p).scale(0.12);
                    level.sendParticles(ParticleTypes.CLOUD, p.x, p.y, p.z, 0, v.x, v.y, v.z, 1.0);
                }
            }
            return;
        }
        if (t == DEVOUR_SNAP) {
            this.setMaw(0.0f);
            Vec3 maw = this.mawPosition();
            level.playSound(null, BlockPos.containing(maw), ModSounds.WORLD_EATER_SNAP.get(), SoundSource.HOSTILE, 4.0f, 1.0f);
            level.sendParticles(ParticleTypes.EXPLOSION, maw.x, maw.y, maw.z, 2, 0.8, 0.8, 0.8, 0.0);
            for (Player player : this.playersNear(level, HoardPiece.R_OUT + 12.0)) {
                if (player.position().add(0.0, 1.0, 0.0).distanceTo(maw) < 6.0) {
                    player.hurtServer(level, this.damageSources().mobAttack(this), this.wrath ? 26.0f : 22.0f);
                    player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 80, 0));
                    player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 30, 0));
                    player.setDeltaMovement(player.position().subtract(maw).normalize().scale(1.2).add(0.0, 0.5, 0.0));
                    player.needsSync = true;
                }
            }
            this.shake(level, 40.0, 0.45f, 10);
        }
        this.setMaw(0.1f);
        this.steer(this.orbitPoint(Math.atan2(this.headCenter().z - c.z, this.headCenter().x - c.x), 8.0), 1.0, 8.0);
        if (t >= DEVOUR_TICKS) {
            this.backToOrbit();
        }
    }

    // ------------------------------------------------------------------ Weltenspucke

    private void tickSpit(ServerLevel level, @Nullable LivingEntity target) {
        int t = this.phaseTick;
        Vec3 c = this.center();
        if (target != null) {
            this.aim = target.position().add(0.0, 1.0, 0.0);
        }
        // vor dem Ziel schweben, außerhalb des Rings
        Vec3 out = new Vec3(this.aim.x - c.x, 0.0, this.aim.z - c.z);
        out = out.lengthSqr() < 1.0 ? new Vec3(1.0, 0.0, 0.0) : out.normalize();
        Vec3 spot = c.add(out.scale(HoardPiece.R_OUT + 12.0)).add(0.0, 12.0, 0.0);
        if (t < 18) {
            this.steer(spot, 1.3, 9.0);
        } else {
            Vec3 h = this.headCenter();
            this.steer(h.add(this.aim.subtract(h).normalize().scale(0.3)), 0.05, 12.0);
        }
        int shots = this.wrath ? 5 : 4;
        int first = 20;
        this.setMaw(t >= first - 4 && t < first + shots * 8 ? 0.85f : 0.25f);
        if (t >= first && (t - first) % 8 == 0 && this.volley < shots) {
            WorldChunkEntity.Kind kind = WorldChunkEntity.Kind.values()[(this.volley + this.random.nextInt(2)) % WorldChunkEntity.Kind.values().length];
            Vec3 from = this.mawPosition();
            Vec3 lead = target != null ? target.getDeltaMovement().scale(10.0) : Vec3.ZERO;
            Vec3 dir = this.aim.add(lead.x, 0.0, lead.z).subtract(from);
            double dist = dir.length();
            double speed = 1.5;
            Vec3 vel = dir.normalize().scale(speed).add(0.0, dist * 0.012, 0.0);
            WorldChunkEntity.spit(level, this, kind, from, vel, this.wrath ? 11.0f : 9.0f);
            level.playSound(null, BlockPos.containing(from), ModSounds.WORLD_EATER_SPIT.get(), SoundSource.HOSTILE, 3.0f, 0.9f + this.random.nextFloat() * 0.2f);
            this.volley++;
        }
        if (t >= SPIT_TICKS) {
            this.backToOrbit();
        }
    }

    // ------------------------------------------------------------------ Leibhieb

    private void tickSweep(ServerLevel level) {
        int t = this.phaseTick;
        if (t < 34) {
            this.steer(this.aim, 1.5, 9.0);
            this.setMaw(0.3f);
            if (t == 20) {
                this.roar(level, 1.0f);
            }
            if (t % 4 == 0) {
                // die Bahn glüht auf dem Ring auf
                Vec3 a = this.aim;
                Vec3 b = this.aim2;
                for (int k = 0; k <= 16; k++) {
                    Vec3 p = a.add(b.subtract(a).scale(k / 16.0));
                    level.sendParticles(new DustParticleOptions(0xFF5A1E, 2.0f), p.x, this.center().y + 0.2, p.z, 2, 0.6, 0.05, 0.6, 0.0);
                }
            }
            return;
        }
        if (t < 78) {
            if (t == 34) {
                level.playSound(null, BlockPos.containing(this.headCenter()), ModSounds.WORLD_EATER_SWEEP.get(), SoundSource.HOSTILE, 4.0f, 1.0f);
            }
            Vec3 beyond = this.aim2.add(this.aim2.subtract(this.aim).normalize().scale(20.0));
            this.steer(t < 66 ? this.aim2 : beyond, 1.75, 4.0);
            this.setMaw(0.7f);
            this.sweepHits(level);
            return;
        }
        this.sweepHits(level);
        Vec3 c = this.center();
        this.steer(this.orbitPoint(Math.atan2(this.headCenter().z - c.z, this.headCenter().x - c.x), 10.0), 1.1, 7.0);
        if (t >= SWEEP_TICKS) {
            this.backToOrbit();
        }
    }

    /** Wen Kopf oder Leib streifen, der fliegt davon (einmal je Leibhieb). */
    private void sweepHits(ServerLevel level) {
        for (Player player : this.playersNear(level, HoardPiece.R_OUT + 16.0)) {
            if (this.struck.contains(player.getUUID())) {
                continue;
            }
            boolean hit = this.getBoundingBox().inflate(0.6).intersects(player.getBoundingBox());
            for (int i = 0; i <= SEGMENTS && !hit; i++) {
                hit = this.parts[i].getBoundingBox().inflate(0.4).intersects(player.getBoundingBox());
            }
            if (hit) {
                this.struck.add(player.getUUID());
                player.hurtServer(level, this.damageSources().mobAttack(this), this.wrath ? 15.0f : 12.0f);
                Vec3 push = player.position().subtract(this.headCenter());
                push = new Vec3(push.x, 0.0, push.z);
                push = push.lengthSqr() < 1.0e-4 ? new Vec3(1.0, 0.0, 0.0) : push.normalize();
                player.setDeltaMovement(push.scale(1.6).add(0.0, 0.8, 0.0));
                player.needsSync = true;
            }
        }
    }

    // ------------------------------------------------------------------ Rissatem

    private void tickBreath(ServerLevel level) {
        int t = this.phaseTick;
        Vec3 c = this.center();
        Vec3 perch = c.add(Math.cos(this.breathAngle + Math.PI) * 9.0, 20.0, Math.sin(this.breathAngle + Math.PI) * 9.0);
        if (t < BREATH_START) {
            Vec3 h = this.headCenter();
            if (h.distanceTo(perch) > 1.5) {
                this.steer(perch, 1.4, 10.0);
            } else {
                this.steer(h.add(c.subtract(h).normalize().scale(0.2)), 0.02, 10.0);
            }
            this.setMaw(Mth.clamp((t - 14) / 16.0f, 0.2f, 1.0f));
            if (t == 16) {
                level.playSound(null, BlockPos.containing(this.headCenter()), ModSounds.WORLD_EATER_BREATH.get(), SoundSource.HOSTILE, 5.0f, 1.0f);
            }
            if (t > 18) {
                Vec3 maw = this.mawPosition();
                level.sendParticles(ParticleTypes.FLAME, maw.x, maw.y, maw.z, 6, 0.6, 0.6, 0.6, 0.02);
            }
            return;
        }
        if (t < BREATH_STOP) {
            // der Strahl fegt im Kreis über den Ring (gut anderthalb Runden)
            float progress = (t - BREATH_START) / (float) (BREATH_STOP - BREATH_START);
            double a = this.breathAngle + progress * Mth.TWO_PI * 1.45;
            double r = 8.0 + 10.0 * (0.5 + 0.5 * Math.sin(progress * Math.PI * 3.0));
            Vec3 point = new Vec3(c.x + Math.cos(a) * r, c.y + 0.1, c.z + Math.sin(a) * r);
            this.entityData.set(DATA_BREATH, true);
            this.entityData.set(DATA_BREATH_POINT, new org.joml.Vector3f((float) point.x, (float) point.y, (float) point.z));
            Vec3 h = this.headCenter();
            this.steer(h.add(point.subtract(h).normalize().scale(0.25)), 0.02, 16.0);
            this.setMaw(1.0f);
            if (t % 4 == 0) {
                Vec3 maw = this.mawPosition();
                for (Player player : this.playersNear(level, HoardPiece.R_OUT + 8.0)) {
                    Vec3 p = player.position().add(0.0, 0.9, 0.0);
                    if (distanceToSegment(p, maw, point) < 1.9) {
                        player.hurtServer(level, this.damageSources().mobAttack(this), this.wrath ? 8.0f : 7.0f);
                        player.igniteForSeconds(3.0f);
                    }
                }
                level.sendParticles(ParticleTypes.LAVA, point.x, point.y + 0.2, point.z, 3, 0.4, 0.1, 0.4, 0.0);
            }
            return;
        }
        this.entityData.set(DATA_BREATH, false);
        this.setMaw(0.15f);
        this.steer(this.orbitPoint(Math.atan2(this.headCenter().z - c.z, this.headCenter().x - c.x), 10.0), 1.0, 7.0);
        if (t >= BREATH_TICKS) {
            this.backToOrbit();
        }
    }

    private static double distanceToSegment(Vec3 p, Vec3 a, Vec3 b) {
        Vec3 ab = b.subtract(a);
        double len = ab.lengthSqr();
        double t = len < 1.0e-6 ? 0.0 : Mth.clamp(p.subtract(a).dot(ab) / len, 0.0, 1.0);
        return p.distanceTo(a.add(ab.scale(t)));
    }

    // ------------------------------------------------------------------ Schwerkraftsturz

    private void tickGravity(ServerLevel level) {
        int t = this.phaseTick;
        Vec3 c = this.center();
        Vec3 high = c.add(0.0, 26.0, 0.0);
        Vec3 h = this.headCenter();
        if (h.distanceTo(high) > 2.0 && t < GRAVITY_DROP) {
            this.steer(high, 1.3, 10.0);
        } else {
            this.steer(h.add(0.0, -0.3, 0.0), 0.01, 10.0);
        }
        this.setMaw(t > 12 && t < GRAVITY_DROP ? 0.8f : 0.2f);
        if (t == 12) {
            level.playSound(null, BlockPos.containing(h), ModSounds.WORLD_EATER_GRAVITY.get(), SoundSource.HOSTILE, 5.0f, 1.0f);
            this.roar(level, 0.6f);
        }
        if (t > 16 && t < GRAVITY_DROP) {
            for (Player player : this.playersNear(level, HoardPiece.R_OUT + 6.0)) {
                if (player.isShiftKeyDown()) {
                    continue;
                }
                Vec3 v = player.getDeltaMovement();
                player.setDeltaMovement(v.x * 0.9, Math.min(0.45, v.y + 0.09), v.z * 0.9);
                player.needsSync = true;
                player.resetFallDistance();
                if (t % 4 == 0) {
                    level.sendParticles(new DustParticleOptions(0xFF7AA0, 1.2f), player.getX(), player.getY(), player.getZ(), 3, 0.3, 0.2, 0.3, 0.0);
                }
            }
            if (t % 3 == 0) {
                level.sendParticles(ParticleTypes.REVERSE_PORTAL, c.x, c.y + 1.0, c.z, 30, HoardPiece.R_OUT * 0.5, 0.5, HoardPiece.R_OUT * 0.5, 0.05);
            }
        }
        if (t == GRAVITY_DROP) {
            // die Schwere kehrt mit aller Wucht zurück
            level.playSound(null, BlockPos.containing(h), ModSounds.WORLD_EATER_ROAR.get(), SoundSource.HOSTILE, 5.0f, 0.7f);
            for (Player player : this.playersNear(level, HoardPiece.R_OUT + 6.0)) {
                if (player.getY() > c.y + 1.5) {
                    player.setDeltaMovement(player.getDeltaMovement().x, -1.6, player.getDeltaMovement().z);
                    player.needsSync = true;
                }
            }
            this.shake(level, 50.0, 0.4f, 14);
        }
        if (t >= GRAVITY_TICKS) {
            this.backToOrbit();
        }
    }

    // ------------------------------------------------------------------ Ruf der Welten

    private void tickCall(ServerLevel level) {
        int t = this.phaseTick;
        Vec3 c = this.center();
        Vec3 high = c.add(0.0, 22.0, 0.0);
        Vec3 h = this.headCenter();
        this.steer(h.distanceTo(high) > 2.0 ? high : h.add(this.heading.scale(0.05)), h.distanceTo(high) > 2.0 ? 1.3 : 0.02, 10.0);
        this.setMaw(t > 10 && t < 50 ? 0.9f : 0.2f);
        if (t == 10) {
            level.playSound(null, BlockPos.containing(h), ModSounds.WORLD_EATER_CALL.get(), SoundSource.HOSTILE, 5.0f, 1.0f);
            this.roar(level, 0.8f);
        }
        int count = this.wrath ? 4 : 3;
        if (t >= 20 && t < 20 + count * 9 && (t - 20) % 9 == 0) {
            double a = this.random.nextDouble() * Mth.TWO_PI;
            double r = HoardPiece.R_IN + 3.0 + this.random.nextDouble() * (HoardPiece.R_OUT - HoardPiece.R_IN - 6.0);
            Vec3 p = new Vec3(c.x + Math.cos(a) * r, c.y, c.z + Math.sin(a) * r);
            level.sendParticles(ParticleTypes.REVERSE_PORTAL, p.x, p.y + 1.0, p.z, 60, 0.4, 1.2, 0.4, 0.15);
            level.sendParticles(ParticleTypes.FLAME, p.x, p.y + 0.2, p.z, 20, 0.6, 0.1, 0.6, 0.02);
            level.playSound(null, BlockPos.containing(p), ModSounds.WORLD_EATER_RIFT.get(), SoundSource.HOSTILE, 2.0f, 1.0f);
            EntityType<? extends Mob> type = (t / 9) % 3 == 0 ? ModEntities.DISTORTLING.get() : ModEntities.RIFT_HOUND.get();
            Mob mob = type.create(level, EntitySpawnReason.MOB_SUMMONED);
            if (mob != null) {
                mob.snapTo(p.x, p.y, p.z, this.random.nextFloat() * 360.0f, 0.0f);
                mob.setPersistenceRequired();
                level.addFreshEntity(mob);
                this.summoned.add(mob.getUUID());
            }
        }
        if (t >= CALL_TICKS) {
            this.backToOrbit();
        }
    }

    // ------------------------------------------------------------------ Zorn

    private void tickWrath(ServerLevel level) {
        int t = this.phaseTick;
        Vec3 c = this.center();
        Vec3 high = c.add(0.0, 30.0, 0.0);
        Vec3 h = this.headCenter();
        this.steer(h.distanceTo(high) > 2.0 ? high : h.add(0.0, 0.2, 0.0), h.distanceTo(high) > 2.0 ? 1.4 : 0.02, 10.0);
        this.setMaw(t > 14 && t < 54 ? 1.0f : 0.3f);
        if (t == 16) {
            level.broadcastEntityEvent(this, EVENT_WRATH);
            this.roar(level, 2.0f);
            for (Player player : this.playersNear(level, 90.0)) {
                player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 50, 0));
                player.sendSystemMessage(Component.translatable("message.kingdomsatwar.world_eater.wrath").withColor(0xFF6A3A));
            }
            this.shake(level, 120.0, 0.7f, 40);
        }
        if (t >= 22 && t < 52 && t % 2 == 0 && this.home != null) {
            // der äußere Rand des Rings bricht weg und stürzt ins Nichts
            for (int k = 0; k < 3; k++) {
                double a = this.random.nextDouble() * Mth.TWO_PI;
                double r = HoardPiece.R_OUT - 3.0 + this.random.nextDouble() * 3.5;
                BlockPos pos = BlockPos.containing(c.x + Math.cos(a) * r, this.home.getY() - 1, c.z + Math.sin(a) * r);
                for (int dy = 0; dy >= -2; dy--) {
                    BlockPos p = pos.above(dy);
                    BlockState state = level.getBlockState(p);
                    if (!state.isAir() && !state.is(AbyssBlocks.HOARD_SEAL.get()) && state.getDestroySpeed(level, p) >= 0.0f) {
                        // die Brocken stürzen ins Nichts: wo sie aufschlagen, zerschellen sie (kein Block, kein Gegenstand)
                        FallingBlockEntity.fall(level, p, state).disableDrop();
                    }
                }
                BlockPos above = pos.above();
                if (!level.getBlockState(above).isAir() && level.getBlockState(above).getDestroySpeed(level, above) >= 0.0f) {
                    FallingBlockEntity.fall(level, above, level.getBlockState(above)).disableDrop();
                }
            }
        }
        if (t >= WRATH_TICKS) {
            this.backToOrbit();
        }
    }

    // ------------------------------------------------------------------ Hilfen

    private void roar(ServerLevel level, float strength) {
        level.broadcastEntityEvent(this, EVENT_ROAR);
        level.playSound(null, BlockPos.containing(this.headCenter()), ModSounds.WORLD_EATER_ROAR.get(), SoundSource.HOSTILE, 6.0f, 1.0f);
        this.shake(level, 90.0, 0.3f * strength, 24);
    }

    private void warnRing(ServerLevel level, Vec3 at, double radius) {
        for (int k = 0; k < 18; k++) {
            double a = k * Mth.TWO_PI / 18.0;
            level.sendParticles(new DustParticleOptions(0xFF3A1E, 1.6f), at.x + Math.cos(a) * radius, at.y + 0.15, at.z + Math.sin(a) * radius, 1, 0.0,
                    0.0, 0.0, 0.0);
        }
        level.sendParticles(ParticleTypes.FLAME, at.x, at.y + 0.2, at.z, 2, radius * 0.4, 0.0, radius * 0.4, 0.01);
    }

    /** Einschlag: Schaden nach Nähe, Gegner fliegen auf und weg. */
    private void impact(ServerLevel level, Vec3 c, double radius, float damage, double push, double up) {
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, new AABB(c, c).inflate(radius, 3.0, radius),
                e -> e != this && e.isAlive() && !(e instanceof WorldEaterEntity))) {
            double d = e.position().distanceTo(c);
            if (d > radius) {
                continue;
            }
            float f = (float) (1.0 - d / radius * 0.5);
            e.hurtServer(level, this.damageSources().mobAttack(this), damage * f);
            Vec3 dir = e.position().subtract(c);
            dir = new Vec3(dir.x, 0.0, dir.z);
            dir = dir.lengthSqr() < 1.0e-4 ? new Vec3(1.0, 0.0, 0.0) : dir.normalize();
            e.setDeltaMovement(dir.scale(push * f).add(0.0, up, 0.0));
            if (e instanceof Player player) {
                player.needsSync = true;
            }
        }
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

    private void dismissSummoned(ServerLevel level) {
        for (UUID id : this.summoned) {
            Entity e = level.getEntity(id);
            if (e != null && e.isAlive()) {
                level.sendParticles(ParticleTypes.REVERSE_PORTAL, e.getX(), e.getY() + 1.0, e.getZ(), 20, 0.3, 0.6, 0.3, 0.1);
                e.discard();
            }
        }
        this.summoned.clear();
    }

    /** Niemand mehr am Ring: er sinkt zurück in den Schlund (er kann erneut erwachen). */
    private void slumber(ServerLevel level) {
        this.dismissSummoned(level);
        if (this.seal != null && level.getBlockEntity(this.seal) instanceof HoardSealBlockEntity hoard) {
            hoard.onSlumber();
        }
        this.bossBar.removeAllPlayers();
        this.discard();
    }

    // ------------------------------------------------------------------ Tests und Vorführung

    /** Für Tests und die Vorführung: ein festes Ziel, und er wählt keine Angriffe selbst. */
    public void debugHold(@Nullable LivingEntity target) {
        this.debugTarget = target;
        this.debugHold = true;
    }

    /** Für Tests und die Vorführung: beginnt einen Angriff ({@code bite}, {@code devour}, {@code spit}, {@code sweep},
     * {@code breath}, {@code gravity}, {@code call}, {@code wrath}) gegen {@code target}. */
    public void forceAttack(ServerLevel level, String name, LivingEntity target) {
        this.debugTarget = target;
        this.debugHold = true;
        Phase next = Phase.valueOf(name.toUpperCase(java.util.Locale.ROOT));
        if (next == Phase.WRATH) {
            this.wrath = true;
            this.entityData.set(DATA_WRATH, true);
        }
        this.beginAttack(level, next, target);
    }

    /** Für die Vorführung: sofort wach auf seiner Bahn (ohne Erwachen). */
    public void skipAwakening() {
        this.backToOrbit();
    }

    public String phaseName() {
        return this.phase.name().toLowerCase(java.util.Locale.ROOT);
    }

    // ------------------------------------------------------------------ Tod

    @Override
    protected void tickDeath() {
        this.deathTime++;
        this.entityData.set(DATA_BREATH, false);
        if (!(this.level() instanceof ServerLevel level)) {
            return;
        }
        // er windet sich und sinkt langsam; Glied um Glied zerbricht, vom Schwanz zum Kopf
        Vec3 h = this.headCenter();
        this.steer(h.add(this.heading.scale(0.5)).add(Math.sin(this.deathTime * 0.4) * 0.6, -0.25, Math.cos(this.deathTime * 0.4) * 0.6), 0.12,
                12.0);
        this.setMaw(0.5f + 0.5f * Mth.sin(this.deathTime * 0.5f));
        if (this.deathTime == 1) {
            level.playSound(null, BlockPos.containing(h), ModSounds.WORLD_EATER_DEATH.get(), SoundSource.HOSTILE, 6.0f, 1.0f);
            this.dismissSummoned(level);
            this.shake(level, 120.0, 0.6f, 60);
        }
        int burst = (this.deathTime - 10) / 9;
        if (this.deathTime >= 10 && (this.deathTime - 10) % 9 == 0 && burst <= SEGMENTS) {
            int i = SEGMENTS - burst;
            Vec3 p = this.bodyNow[i];
            BlockState chunk = segmentWorld(i);
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, chunk), p.x, p.y, p.z, 70, 1.6, 1.6, 1.6, 0.25);
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, AbyssBlocks.CHAOS_STONE.base().get().defaultBlockState()), p.x, p.y, p.z,
                    50, 1.6, 1.6, 1.6, 0.25);
            level.sendParticles(ParticleTypes.EXPLOSION, p.x, p.y, p.z, 2, 0.8, 0.8, 0.8, 0.0);
            level.sendParticles(ParticleTypes.LAVA, p.x, p.y, p.z, 12, 1.2, 1.2, 1.2, 0.0);
            level.playSound(null, BlockPos.containing(p), ModSounds.WORLD_EATER_CRUMBLE.get(), SoundSource.HOSTILE, 4.0f, 0.9f + this.random.nextFloat() * 0.2f);
            for (int k = 0; k < 3; k++) {
                FallingBlockEntity falling = FallingBlockEntity.fall(level, BlockPos.containing(p), k == 0 ? chunk
                        : AbyssBlocks.CHAOS_STONE.base().get().defaultBlockState());
                falling.setDeltaMovement((this.random.nextDouble() - 0.5) * 0.8, 0.3 + this.random.nextDouble() * 0.4,
                        (this.random.nextDouble() - 0.5) * 0.8);
                falling.dropItem = false;
                falling.disableDrop();
            }
        }
        if (this.deathTime == DEATH_TICKS - 12) {
            level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, h.x, h.y, h.z, 2, 1.0, 1.0, 1.0, 0.0);
            level.sendParticles(ParticleTypes.END_ROD, h.x, h.y, h.z, 160, 3.0, 3.0, 3.0, 0.35);
            this.shake(level, 120.0, 0.8f, 30);
        }
        if (this.deathTime >= DEATH_TICKS && !this.isRemoved()) {
            this.bossBar.removeAllPlayers();
            this.dropHoard(level);
            if (this.seal != null && level.getBlockEntity(this.seal) instanceof HoardSealBlockEntity hoard) {
                hoard.onDefeated(level);
            }
            WorldSavior.grantAround(level, this.center(), 96.0);
            this.remove(Entity.RemovalReason.KILLED);
        }
    }

    /** Welches Stück einer Welt Glied {@code i} auf dem Rücken trägt (0 Wiese, 1 Nether, 2 End, 3 Eis). */
    public static int segmentWorldIndex(int i) {
        return (i * 7 + 1) % 4;
    }

    private static BlockState segmentWorld(int i) {
        if (i >= SEGMENTS) {
            return AbyssBlocks.WORLD_SEAM.get().defaultBlockState();
        }
        return switch (segmentWorldIndex(i)) {
            case 0 -> Blocks.GRASS_BLOCK.defaultBlockState();
            case 1 -> Blocks.NETHERRACK.defaultBlockState();
            case 2 -> Blocks.END_STONE.defaultBlockState();
            default -> Blocks.PACKED_ICE.defaultBlockState();
        };
    }

    /** Die Beute fällt nicht ins Nichts: Sie liegt auf dem Ring im Norden, über dem Siegel. */
    private void dropHoard(ServerLevel level) {
        Vec3 c = this.center();
        Vec3 spot = new Vec3(c.x, c.y + 0.5, c.z - (HoardPiece.R_IN + HoardPiece.R_OUT) / 2.0);
        ResourceKey<LootTable> key = this.getType().getDefaultLootTable().orElse(null);
        if (key != null) {
            LootParams params = new LootParams.Builder(level)
                    .withParameter(LootContextParams.THIS_ENTITY, this)
                    .withParameter(LootContextParams.ORIGIN, spot)
                    .withParameter(LootContextParams.DAMAGE_SOURCE, this.getLastDamageSource() != null ? this.getLastDamageSource()
                            : this.damageSources().generic())
                    .create(LootContextParamSets.ENTITY);
            for (ItemStack stack : level.getServer().reloadableRegistries().getLootTable(key).getRandomItems(params)) {
                ItemEntity item = new ItemEntity(level, spot.x + (this.random.nextDouble() - 0.5) * 2.0, spot.y, spot.z + (this.random.nextDouble() - 0.5) * 2.0,
                        stack);
                item.setDeltaMovement(0.0, 0.25, 0.0);
                item.setUnlimitedLifetime();
                level.addFreshEntity(item);
            }
        }
        // Erfahrung wie bei den anderen großen Gegnern (Zerbrochener König, Asteria): fest 500, auf dem Ring statt über dem Nichts
        net.minecraft.world.entity.ExperienceOrb.award(level, spot, XP_REWARD);
        level.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, spot.x, spot.y + 1.0, spot.z, 80, 1.0, 1.5, 1.0, 0.4);
    }

    @Override
    protected boolean shouldDropLoot(ServerLevel level) {
        return false;
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
        output.putInt("Calls", this.calls);
        output.putBoolean("Wrath", this.wrath);
        output.store("Summoned", net.minecraft.core.UUIDUtil.CODEC.listOf(), this.summoned);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.home = input.getLong("Home").map(BlockPos::of).orElse(null);
        this.seal = input.getLong("Seal").map(BlockPos::of).orElse(null);
        this.calls = input.getIntOr("Calls", 0);
        this.wrath = input.getBooleanOr("Wrath", false);
        this.entityData.set(DATA_WRATH, this.wrath);
        this.summoned.clear();
        input.read("Summoned", net.minecraft.core.UUIDUtil.CODEC.listOf()).ifPresent(this.summoned::addAll);
        // nach dem Laden gleich weiter auf seiner Bahn
        this.phase = Phase.CIRCLE;
        this.phaseTick = 0;
        this.bossBar.setProgress(this.getHealth() / this.getMaxHealth());
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return ModSounds.WORLD_EATER_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.WORLD_EATER_HURT.get();
    }

    @Override
    protected @Nullable SoundEvent getDeathSound() {
        return null;
    }

    @Override
    protected float getSoundVolume() {
        return 5.0f;
    }

    @Override
    public int getAmbientSoundInterval() {
        return 140;
    }

    @Override
    public SoundSource getSoundSource() {
        return SoundSource.HOSTILE;
    }
}
