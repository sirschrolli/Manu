package io.github.sirschrolli.kingdomsatwar.entity.sea;

import io.github.sirschrolli.kingdomsatwar.block.sea.AtollCoreBlockEntity;
import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import io.github.sirschrolli.kingdomsatwar.magic.fx.SpellFxEntity;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.ModParticles;
import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import io.github.sirschrolli.kingdomsatwar.registry.SeaBlocks;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.particles.BlockParticleOption;
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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Karkinos, der Riffkoloss: ein uralter Einsiedler der Tiefe, der die Kuppel des versunkenen Tempels als Gehäuse trägt –
 * und in ihr den Gezeiten-Speer. Er schläft im Sand der Lagune des Versunkenen Atolls; nur die Kuppel mit dem Speer
 * schaut heraus. Wer ihr zu nahe kommt, weckt ihn.
 * <ul>
 *     <li><b>Scherenschlag:</b> die Brecherschere hebt sich (ein Ring aus Blasen warnt) und schmettert herab – danach
 *     steckt sie kurz im Sand fest: jetzt ist er verwundbar.</li>
 *     <li><b>Zangengriff:</b> er stürmt vor, packt mit der Greifschere zu, kneift – und schleudert seine Beute weit fort.</li>
 *     <li><b>Blasenstrahl:</b> aus dem Maul schießt ein Strahl aus Wasser und Blasen, der seinem Ziel folgt und es
 *     zurückdrängt.</li>
 *     <li><b>Eingraben:</b> er gräbt sich ein, wühlt unter dem Sand auf sein Ziel zu (eine Spur verrät ihn) und bricht
 *     unter ihm hervor.</li>
 *     <li><b>Panzerwirbel:</b> er zieht sich in seine Kuppel zurück und rollt dreimal quer durch die Lagune – Pfeile prallen
 *     ab; danach ist er benommen.</li>
 *     <li><b>Mahlstrom:</b> er reckt Scheren und Speer und dreht sich – ein Strudel zieht alle zu ihm hin, die Scheren
 *     mahlen, wer zu nahe kommt; zuletzt schießt eine Wassersäule empor und wirft alle in die Luft.</li>
 *     <li><b>Blasenfalle:</b> er bläst schimmernde Blasen – wer darin gefangen ist, schwebt hilflos empor und stürzt
 *     herab, wenn sie platzt.</li>
 *     <li><b>Ruf der Brut</b> (bei zwei Dritteln und einem Drittel): Riesenkrabben brechen aus dem Sand.</li>
 *     <li><b>Gezeitenzorn</b> (unter 40 %): der Speer in seiner Kuppel erwacht – fortan rufen Flutlanzen Wassersäulen
 *     auf seine Feinde herab, und jeder Scherenschlag schickt eine Brandungswelle über die Lagune.</li>
 * </ul>
 * Er ist ein früher Boss: schwächer als die Erzmagier, die Drachen und der Zerbrochene König.
 */
public class ReefColossusEntity extends Monster implements io.github.sirschrolli.kingdomsatwar.entity.boss.sky.SkyWarden {
    public static final byte EVENT_AWAKEN = 100;
    public static final byte EVENT_SLAM = 101;
    public static final byte EVENT_GRAB = 102;
    public static final byte EVENT_THROW = 103;
    public static final byte EVENT_JET = 104;
    public static final byte EVENT_BURROW = 105;
    public static final byte EVENT_ERUPT = 106;
    public static final byte EVENT_ROLL = 107;
    public static final byte EVENT_SUMMON = 108;
    public static final byte EVENT_TIDE = 109;
    public static final byte EVENT_LANCE = 110;
    public static final byte EVENT_STUN = 111;

    /** Wie tief er im Sand steckt (0 = ganz oben, 1 = ganz vergraben) – im Bild sinkt das Modell entsprechend ab. */
    private static final EntityDataAccessor<Float> DATA_SINK = SynchedEntityData.defineId(ReefColossusEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> DATA_ROLLING = SynchedEntityData.defineId(ReefColossusEntity.class,
            EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_JETTING = SynchedEntityData.defineId(ReefColossusEntity.class,
            EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> DATA_JET_YAW = SynchedEntityData.defineId(ReefColossusEntity.class,
            EntityDataSerializers.FLOAT);
    /** Ob der Speer noch in der Kuppel steckt (im Tod bricht er heraus). */
    private static final EntityDataAccessor<Boolean> DATA_SPEAR = SynchedEntityData.defineId(ReefColossusEntity.class,
            EntityDataSerializers.BOOLEAN);
    /** Gezeitenzorn: der Speer leuchtet heller. */
    private static final EntityDataAccessor<Boolean> DATA_TIDE = SynchedEntityData.defineId(ReefColossusEntity.class,
            EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_STUNNED = SynchedEntityData.defineId(ReefColossusEntity.class,
            EntityDataSerializers.BOOLEAN);

    public static final int AWAKEN_TICKS = 80;
    public static final int SLAM_TICKS = 34;
    public static final int SLAM_IMPACT = 20;
    public static final int STUCK_TICKS = 20;
    public static final int GRAB_TICKS = 44;
    public static final int GRAB_CATCH = 12;
    public static final int GRAB_THROW = 32;
    public static final int JET_TICKS = 76;
    public static final int JET_START = 18;
    public static final int JET_END = 66;
    public static final double JET_RANGE = 13.0;
    public static final int BURROW_TICKS = 104;
    public static final int BURROW_DOWN = 20;
    public static final int BURROW_LOCK = 70;
    public static final int BURROW_ERUPT = 86;
    public static final int ROLL_TICKS = 104;
    public static final int ROLL_START = 14;
    public static final int ROLL_CHARGE = 30;
    public static final int DIZZY_TICKS = 26;
    public static final int SUMMON_TICKS = 46;
    public static final int TIDE_TICKS = 50;
    public static final int LANCE_TICKS = 60;
    public static final int LANCE_STRIKE = 36;
    public static final int DEATH_TICKS = 100;
    /** Mahlstrom: Dauer, Beginn des Strudels, Wassersäule. */
    public static final int MAELSTROM_TICKS = 76;
    public static final int MAELSTROM_START = 12;
    public static final int MAELSTROM_BURST = 62;
    public static final double MAELSTROM_RADIUS = 14.0;
    /** Blasenfalle: Dauer, Ausblasen, wie lange eine Blase hält. */
    public static final int BUBBLE_TICKS = 44;
    public static final int BUBBLE_BLOW = 14;
    public static final int BUBBLE_LIFE = 64;
    /** So weit (waagerecht) reicht die Lagune um ihre Mitte. */
    public static final double ARENA_RADIUS = 24.0;

    public final AnimationState awakenAnimation = new AnimationState();
    public final AnimationState slamAnimation = new AnimationState();
    public final AnimationState grabAnimation = new AnimationState();
    public final AnimationState throwAnimation = new AnimationState();
    public final AnimationState jetAnimation = new AnimationState();
    public final AnimationState burrowAnimation = new AnimationState();
    public final AnimationState eruptAnimation = new AnimationState();
    public final AnimationState rollAnimation = new AnimationState();
    public final AnimationState summonAnimation = new AnimationState();
    public final AnimationState tideAnimation = new AnimationState();
    public final AnimationState lanceAnimation = new AnimationState();
    public final AnimationState stunAnimation = new AnimationState();

    private final ServerBossEvent bossBar = new ServerBossEvent(UUID.randomUUID(), Component.translatable("boss.kingdomsatwar.reef"),
            BossEvent.BossBarColor.BLUE, BossEvent.BossBarOverlay.NOTCHED_10);

    private enum Attack { NONE, AWAKEN, SLAM, STUCK, GRAB, JET, BURROW, ROLL, DIZZY, SUMMON, TIDE, LANCE, MAELSTROM, BUBBLES }

    private Attack attack = Attack.NONE;
    private Attack last = Attack.NONE;
    private int attackTick;
    private int cooldown = 40;
    private @Nullable Vec3 slamPoint;
    private @Nullable UUID grabbed;
    private Vec3 rollDir = Vec3.ZERO;
    private final Set<UUID> rollHits = new HashSet<>();
    private final Set<UUID> waveHits = new HashSet<>();
    private @Nullable Vec3 eruptPoint;
    private final List<Vec3> lances = new ArrayList<>();
    /** Blasenfalle: wen die Blasen fangen sollen. */
    private final List<UUID> bubbleVictims = new ArrayList<>();
    /** Brandungswellen nach Scherenschlägen im Gezeitenzorn: Mitte, Alter (Ticks). */
    private final List<double[]> waves = new ArrayList<>();
    private int summons;
    private boolean tide;
    private int lonely;
    private int strafe;
    private @Nullable BlockPos home;
    private @Nullable BlockPos core;
    private final List<UUID> brood = new ArrayList<>();
    /** Für Tests und die Vorführung: ein festes Ziel und keine selbst gewählten Angriffe (siehe {@link #forceAttack}). */
    private @Nullable LivingEntity debugTarget;
    private boolean debugHold;
    private float sinkClientO;
    private float sinkClient;
    private float jetYawO;
    private float jetYawC;

    public ReefColossusEntity(EntityType<? extends ReefColossusEntity> type, Level level) {
        super(type, level);
        this.xpReward = 300;
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        // ein früher Boss: deutlich schwächer als die Erzmagier (420), Drachen und der König
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 320.0)
                .add(Attributes.ARMOR, 10.0)
                .add(Attributes.ARMOR_TOUGHNESS, 2.0)
                .add(Attributes.ATTACK_DAMAGE, 8.0)
                .add(Attributes.FOLLOW_RANGE, 48.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(Attributes.STEP_HEIGHT, 1.5)
                .add(Attributes.MOVEMENT_SPEED, 0.28);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_SINK, 0.0f);
        builder.define(DATA_ROLLING, false);
        builder.define(DATA_JETTING, false);
        builder.define(DATA_JET_YAW, 0.0f);
        builder.define(DATA_SPEAR, true);
        builder.define(DATA_TIDE, false);
        builder.define(DATA_STUNNED, false);
    }

    /** Verknüpft ihn mit seiner Lagune (Mitte) und dem Herz des Atolls darunter. */
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

    /** Wie tief er im Bild im Sand steckt (weich zwischen zwei Ticks). */
    public float sink(float partialTicks) {
        return Mth.lerp(partialTicks, this.sinkClientO, this.sinkClient);
    }

    public boolean isRolling() {
        return this.entityData.get(DATA_ROLLING);
    }

    public boolean isJetting() {
        return this.entityData.get(DATA_JETTING);
    }

    public float jetYaw(float partialTicks) {
        return Mth.rotLerp(partialTicks, this.jetYawO, this.jetYawC);
    }

    public boolean hasSpear() {
        return this.entityData.get(DATA_SPEAR);
    }

    public boolean inTide() {
        return this.entityData.get(DATA_TIDE);
    }

    public boolean isStunned() {
        return this.entityData.get(DATA_STUNNED);
    }

    private void setSink(float sink) {
        this.entityData.set(DATA_SINK, Mth.clamp(sink, 0.0f, 1.0f));
    }

    // ------------------------------------------------------------------ Erwachen

    /** Lässt ihn erwachen: er steckt noch ganz im Sand und bricht hervor. */
    public void beginAwakening(ServerLevel level) {
        this.attack = Attack.AWAKEN;
        this.attackTick = 0;
        this.setSink(1.0f);
        level.broadcastEntityEvent(this, EVENT_AWAKEN);
        level.playSound(null, this.blockPosition(), ModSounds.REEF_COLOSSUS_BURROW.get(), SoundSource.HOSTILE, 4.0f, 0.8f);
    }

    public boolean isAwakening() {
        return this.attack == Attack.AWAKEN;
    }

    private void tickAwakening(ServerLevel level) {
        int t = ++this.attackTick;
        this.setDeltaMovement(Vec3.ZERO);
        float rise = Mth.clamp((t - 14) / 46.0f, 0.0f, 1.0f);
        this.setSink(1.0f - rise * rise * (3.0f - 2.0f * rise));
        if (t % 3 == 0) {
            this.sandBurst(level, this.position(), 2.6, 18, 0.18);
            level.sendParticles(ParticleTypes.BUBBLE_COLUMN_UP, this.getX(), this.getY() + 0.5, this.getZ(), 10, 2.0, 0.4, 2.0, 0.1);
        }
        if (t == 14 || t == 34) {
            level.playSound(null, this.blockPosition(), ModSounds.REEF_COLOSSUS_ERUPT.get(), SoundSource.HOSTILE, 3.0f, 0.8f);
            this.shake(level, 48.0, 0.4f, 20);
        }
        if (t == AWAKEN_TICKS - 18) {
            level.playSound(null, this.blockPosition(), ModSounds.REEF_COLOSSUS_ROAR.get(), SoundSource.HOSTILE, 4.0f, 0.9f);
            Component title = Component.translatable("boss.kingdomsatwar.reef").withColor(0x7AE8FF);
            Component subtitle = Component.translatable("boss.kingdomsatwar.reef.title").withStyle(net.minecraft.ChatFormatting.GRAY);
            for (ServerPlayer player : level.players()) {
                if (player.distanceToSqr(this) < 80.0 * 80.0) {
                    player.connection.send(new net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket(20, 70, 30));
                    player.connection.send(new net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket(title));
                    player.connection.send(new net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket(subtitle));
                }
            }
            this.shake(level, 60.0, 0.55f, 30);
        }
        if (t >= AWAKEN_TICKS) {
            this.setSink(0.0f);
            this.attack = Attack.NONE;
            this.cooldown = 30;
        }
    }

    // ------------------------------------------------------------------ Bewegung und Grundverhalten

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
        return this.sink() < 0.6f && super.isPickable();
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
        if (this.level().isClientSide()) {
            this.sinkClientO = this.sinkClient;
            this.sinkClient = this.sink();
            this.jetYawO = this.isJetting() ? this.jetYawC : this.entityData.get(DATA_JET_YAW);
            this.jetYawC = this.entityData.get(DATA_JET_YAW);
            this.clientParticles();
        }
    }

    private void clientParticles() {
        Level level = this.level();
        if (this.inTide() && this.hasSpear() && this.random.nextInt(2) == 0) {
            Vec3 tip = spearTip(this.position(), this.yBodyRot);
            level.addParticle(ModParticles.SEA_GLOW.get(), tip.x + (this.random.nextDouble() - 0.5) * 0.6, tip.y,
                    tip.z + (this.random.nextDouble() - 0.5) * 0.6, 0.0, 0.03, 0.0);
        }
        if (this.isStunned() && this.random.nextInt(2) == 0) {
            level.addParticle(ParticleTypes.ENCHANTED_HIT, this.getRandomX(1.6), this.getY() + 2.4 + this.random.nextDouble(),
                    this.getRandomZ(1.6), 0.0, 0.05, 0.0);
        }
        if (this.isJetting()) {
            Vec3 from = this.mouthPosition();
            Vec3 dir = jetDirection(this.entityData.get(DATA_JET_YAW));
            boolean underwater = level.getFluidState(BlockPos.containing(from)).is(net.minecraft.tags.FluidTags.WATER);
            // der Strahl: Wasserballen schießen aus dem Maul und weiten sich auf ihrem Weg
            for (int i = 0; i < 4; i++) {
                double sx = (this.random.nextDouble() - 0.5) * 0.3;
                double sy = (this.random.nextDouble() - 0.5) * 0.3;
                double sz = (this.random.nextDouble() - 0.5) * 0.3;
                double speed = 1.0 + this.random.nextDouble() * 0.3;
                level.addParticle(ModParticles.BOSS_WATER.get(), from.x + sx, from.y + sy, from.z + sz, dir.x * speed + sx * 0.15,
                        dir.y * speed + 0.03, dir.z * speed + sz * 0.15);
            }
            // Gischt und Blasen entlang des Strahls
            for (int i = 0; i < 7; i++) {
                double d = this.random.nextDouble() * JET_RANGE;
                Vec3 p = from.add(dir.scale(d));
                double spread = 0.3 + d * 0.08;
                var particle = underwater ? ParticleTypes.BUBBLE : i % 3 == 0 ? ParticleTypes.BUBBLE_POP : ParticleTypes.SPLASH;
                level.addParticle(particle, p.x + (this.random.nextDouble() - 0.5) * spread, p.y + (this.random.nextDouble() - 0.5) * spread,
                        p.z + (this.random.nextDouble() - 0.5) * spread, dir.x * 0.35, dir.y * 0.35 + 0.03, dir.z * 0.35);
            }
            // wo er auftrifft, schäumt es
            Vec3 end = from.add(dir.scale(JET_RANGE * (0.8 + this.random.nextDouble() * 0.2)));
            for (int i = 0; i < 3; i++) {
                level.addParticle(ParticleTypes.SPLASH, end.x + (this.random.nextDouble() - 0.5) * 1.6, end.y + 0.2,
                        end.z + (this.random.nextDouble() - 0.5) * 1.6, 0.0, 0.25, 0.0);
            }
            if (this.random.nextInt(3) == 0) {
                level.addParticle(ModParticles.SEA_GLOW.get(), from.x, from.y, from.z, dir.x * 0.6, dir.y * 0.6 + 0.05, dir.z * 0.6);
            }
        }
    }

    /** Wo die Spitze des Speers in der Kuppel steckt (für Leuchten und das Herausbrechen im Tod). */
    public static Vec3 spearTip(Vec3 position, float bodyYaw) {
        float yaw = bodyYaw * Mth.DEG_TO_RAD;
        // Modell: Speer rechts hinten über der Kuppel (×2,5), gedreht mit dem Körper
        double lx = -1.75;
        double lz = 0.75;
        double x = lx * Mth.cos(yaw) - lz * Mth.sin(yaw);
        double z = lx * Mth.sin(yaw) + lz * Mth.cos(yaw);
        return position.add(x, 7.0, z);
    }

    /** Das Maul (Ursprung des Blasenstrahls). */
    public Vec3 mouthPosition() {
        float yaw = this.yBodyRot * Mth.DEG_TO_RAD;
        return this.position().add(-Mth.sin(yaw) * 2.5, 1.5, Mth.cos(yaw) * 2.5);
    }

    public static Vec3 jetDirection(float yawDegrees) {
        float yaw = yawDegrees * Mth.DEG_TO_RAD;
        return new Vec3(-Mth.sin(yaw), -0.08, Mth.cos(yaw)).normalize();
    }

    /** Wo die Greifschere zupackt (vor ihm, auf Hüfthöhe). */
    private Vec3 clawPoint() {
        float yaw = this.yBodyRot * Mth.DEG_TO_RAD;
        return this.position().add(-Mth.sin(yaw) * 4.5 - Mth.cos(yaw) * 1.1, 1.75, Mth.cos(yaw) * 4.5 - Mth.sin(yaw) * 1.1);
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        this.bossBar.setProgress(this.getHealth() / this.getMaxHealth());
        if (this.home == null) {
            this.home = this.blockPosition();
        }
        this.tickWaves(level);
        if (this.attack == Attack.AWAKEN) {
            this.tickAwakening(level);
            return;
        }
        LivingEntity target = this.debugTarget != null && this.debugTarget.isAlive() ? this.debugTarget : this.pickTarget(level);
        this.setTarget(target);
        if (this.attack != Attack.NONE) {
            this.lonely = 0;
            this.tickAttack(level, target);
            return;
        }
        if (this.debugHold) {
            this.getNavigation().stop();
            if (target != null) {
                this.face(target.position(), 6.0f);
            }
            return;
        }
        if (target == null) {
            // niemand mehr in der Lagune: nach einer halben Minute gräbt er sich wieder ein
            this.walkTo(Vec3.atBottomCenterOf(this.home), 0.8);
            if (++this.lonely > 600) {
                this.slumber(level);
            }
            return;
        }
        this.lonely = 0;
        double dist = this.distanceTo(target);
        if (dist > 5.0) {
            this.walkTo(target.position(), 1.0);
        } else {
            // seitwärts trippeln wie eine Krabbe, das Ziel immer vor sich
            if (--this.strafe <= 0) {
                this.strafe = 40 + this.random.nextInt(40) * (this.random.nextBoolean() ? 1 : -1);
            }
            Vec3 away = this.position().subtract(target.position()).multiply(1.0, 0.0, 1.0).normalize();
            Vec3 side = new Vec3(-away.z, 0.0, away.x).scale(this.strafe > 0 ? 3.0 : -3.0);
            this.walkTo(this.clampToArena(target.position().add(away.scale(4.0)).add(side)), 0.7);
            this.face(target.position(), 8.0f);
        }
        if (--this.cooldown <= 0) {
            this.chooseAttack(level, target);
        }
    }

    private void walkTo(Vec3 pos, double speed) {
        Vec3 p = this.clampToArena(pos);
        this.getMoveControl().setWantedPosition(p.x, p.y, p.z, speed);
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
        AABB arena = new AABB(h).inflate(ARENA_RADIUS + 8, 16, ARENA_RADIUS + 8);
        List<LivingEntity> candidates = new ArrayList<>(level.getEntitiesOfClass(Player.class, arena,
                p -> p.isAlive() && !p.isSpectator() && !p.isCreative()));
        if (candidates.isEmpty()) {
            candidates.addAll(level.getEntitiesOfClass(io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierEntity.class, arena,
                    LivingEntity::isAlive));
        }
        LivingEntity current = this.getTarget();
        if (current != null && current.isAlive() && candidates.contains(current) && this.random.nextInt(100) != 0) {
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
        double max = ARENA_RADIUS - 3.0;
        if (d.length() > max) {
            d = d.normalize().scale(max);
        }
        return new Vec3(c.x + d.x, wanted.y, c.z + d.z);
    }

    // ------------------------------------------------------------------ Angriffe

    private float healthFraction() {
        return this.getHealth() / this.getMaxHealth();
    }

    private void chooseAttack(ServerLevel level, LivingEntity target) {
        float hp = this.healthFraction();
        if (!this.tide && hp < 0.4f) {
            this.tide = true;
            this.entityData.set(DATA_TIDE, true);
            this.start(level, Attack.TIDE, EVENT_TIDE);
            return;
        }
        if (this.summons == 0 && hp < 0.66f || this.summons == 1 && hp < 0.33f) {
            this.summons++;
            this.start(level, Attack.SUMMON, EVENT_SUMMON);
            return;
        }
        double dist = this.distanceTo(target);
        List<Attack> options = new ArrayList<>();
        if (dist < 8.0) {
            options.add(Attack.SLAM);
            options.add(Attack.SLAM);
            options.add(Attack.GRAB);
            options.add(Attack.GRAB);
        } else {
            options.add(Attack.JET);
            options.add(Attack.JET);
            options.add(Attack.GRAB);
        }
        if (dist > 5.0) {
            options.add(Attack.BURROW);
        }
        options.add(Attack.JET);
        if (hp < 0.85f) {
            options.add(Attack.ROLL);
        }
        if (this.tide) {
            options.add(Attack.LANCE);
            options.add(Attack.LANCE);
        }
        if (dist < MAELSTROM_RADIUS - 2.0) {
            options.add(Attack.MAELSTROM);
        }
        if (dist > 4.0) {
            options.add(Attack.BUBBLES);
        }
        options.removeIf(a -> a == this.last && options.stream().anyMatch(b -> b != this.last));
        Attack next = options.get(this.random.nextInt(options.size()));
        this.begin(level, next, target);
    }

    private void begin(ServerLevel level, Attack next, @Nullable LivingEntity target) {
        switch (next) {
            case SLAM -> {
                this.slamPoint = target != null ? target.position() : this.position().add(this.getLookAngle().multiply(4.0, 0.0, 4.0));
                this.start(level, Attack.SLAM, EVENT_SLAM);
            }
            case GRAB -> this.start(level, Attack.GRAB, EVENT_GRAB);
            case JET -> {
                this.entityData.set(DATA_JET_YAW, this.getYRot());
                this.start(level, Attack.JET, EVENT_JET);
            }
            case BURROW -> this.start(level, Attack.BURROW, EVENT_BURROW);
            case ROLL -> {
                this.rollHits.clear();
                this.start(level, Attack.ROLL, EVENT_ROLL);
            }
            case SUMMON -> this.start(level, Attack.SUMMON, EVENT_SUMMON);
            case TIDE -> this.start(level, Attack.TIDE, EVENT_TIDE);
            case LANCE -> {
                this.lances.clear();
                this.start(level, Attack.LANCE, EVENT_LANCE);
            }
            // der Mahlstrom: Scheren und Speer erhoben (wie im Gezeitenzorn), dann dreht er sich
            case MAELSTROM -> this.start(level, Attack.MAELSTROM, EVENT_TIDE);
            // die Blasenfalle: er bläst (wie beim Blasenstrahl)
            case BUBBLES -> this.start(level, Attack.BUBBLES, EVENT_JET);
            default -> {
            }
        }
    }

    /**
     * Für Tests und die Vorführung: er greift nur noch auf Befehl an ({@code hold}) und richtet seine Angriffe auf
     * {@code target}. {@code name}: slam, grab, jet, burrow, roll, summon, tide, lance, maelstrom, bubbles (sonst nur Ziel
     * setzen).
     */
    public void forceAttack(ServerLevel level, String name, @Nullable LivingEntity target, boolean hold) {
        this.debugTarget = target;
        this.debugHold = hold;
        if (target != null) {
            this.setTarget(target);
        }
        Attack next = switch (name) {
            case "slam" -> Attack.SLAM;
            case "grab" -> Attack.GRAB;
            case "jet" -> Attack.JET;
            case "burrow" -> Attack.BURROW;
            case "roll" -> Attack.ROLL;
            case "summon" -> Attack.SUMMON;
            case "tide" -> Attack.TIDE;
            case "lance" -> Attack.LANCE;
            case "maelstrom" -> Attack.MAELSTROM;
            case "bubbles" -> Attack.BUBBLES;
            default -> Attack.NONE;
        };
        if (next == Attack.TIDE) {
            this.tide = true;
            this.entityData.set(DATA_TIDE, true);
        }
        if (next != Attack.NONE) {
            this.begin(level, next, target);
        }
    }

    /** Welcher Angriff gerade läuft (für Tests): none, awaken, slam, stuck, grab, jet, burrow, roll, dizzy, summon, tide, lance, maelstrom, bubbles. */
    public String currentAttack() {
        return this.attack.name().toLowerCase(java.util.Locale.ROOT);
    }

    private void start(ServerLevel level, Attack attack, byte event) {
        if (this.attack == Attack.BURROW) {
            // ein Eingraben, das unterbrochen wird (nur durch Befehle): wieder an die Oberfläche
            this.noPhysics = false;
            this.setNoGravity(false);
            this.setSink(0.0f);
        }
        this.attack = attack;
        this.attackTick = 0;
        this.grabbed = null;
        this.entityData.set(DATA_JETTING, false);
        this.entityData.set(DATA_ROLLING, false);
        this.entityData.set(DATA_STUNNED, false);
        this.getNavigation().stop();
        level.broadcastEntityEvent(this, event);
    }

    private void finish() {
        this.last = this.attack;
        this.attack = Attack.NONE;
        this.entityData.set(DATA_JETTING, false);
        this.entityData.set(DATA_ROLLING, false);
        this.entityData.set(DATA_STUNNED, false);
        float hp = this.healthFraction();
        int base = this.tide ? 22 : hp < 0.66f ? 28 : 34;
        this.cooldown = base + this.random.nextInt(18);
    }

    /** Wechselt ohne Pause in eine Folge-Phase (festgesteckt, benommen). */
    private void follow(ServerLevel level, Attack next) {
        this.attack = next;
        this.attackTick = 0;
        if (next == Attack.STUCK || next == Attack.DIZZY) {
            this.entityData.set(DATA_STUNNED, true);
            level.broadcastEntityEvent(this, EVENT_STUN);
        }
    }

    private void tickAttack(ServerLevel level, @Nullable LivingEntity target) {
        int t = ++this.attackTick;
        switch (this.attack) {
            case SLAM -> this.tickSlam(level, target, t);
            case STUCK -> {
                this.getNavigation().stop();
                if (t >= STUCK_TICKS) {
                    this.finish();
                }
            }
            case GRAB -> this.tickGrab(level, target, t);
            case JET -> this.tickJet(level, target, t);
            case BURROW -> this.tickBurrow(level, target, t);
            case ROLL -> this.tickRoll(level, target, t);
            case DIZZY -> {
                this.getNavigation().stop();
                if (t >= DIZZY_TICKS) {
                    this.finish();
                }
            }
            case SUMMON -> this.tickSummon(level, target, t);
            case TIDE -> this.tickTide(level, t);
            case LANCE -> this.tickLance(level, target, t);
            case MAELSTROM -> this.tickMaelstrom(level, target, t);
            case BUBBLES -> this.tickBubbles(level, target, t);
            default -> this.finish();
        }
    }

    /** Scherenschlag: die Brecherschere steigt, ein Ring aus Blasen warnt, sie schmettert herab und bleibt stecken. */
    private void tickSlam(ServerLevel level, @Nullable LivingEntity target, int t) {
        Vec3 p = this.slamPoint != null ? this.slamPoint : this.position();
        // nahe genug heran, damit die Schere die Stelle erreicht
        if (t < SLAM_IMPACT - 6 && this.position().distanceTo(p) > 5.4) {
            this.walkTo(p, 1.3);
        } else {
            this.getNavigation().stop();
        }
        this.face(p, 10.0f);
        if (t < SLAM_IMPACT && t % 3 == 0) {
            ring(level, p, 3.2, t % 6 == 0 ? ParticleTypes.BUBBLE_POP : ParticleTypes.SPLASH, 24);
        }
        if (t == 4) {
            level.playSound(null, this.blockPosition(), ModSounds.REEF_COLOSSUS_CLACK.get(), SoundSource.HOSTILE, 2.0f, 0.8f);
        }
        if (t == SLAM_IMPACT) {
            this.impact(level, p, 3.5, (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE) * 1.5f, 0.8, 0.6);
            if (this.tide) {
                this.waves.add(new double[] {p.x, p.y, p.z, 0});
            }
        }
        if (t >= SLAM_TICKS - 8) {
            this.follow(level, Attack.STUCK);
        }
    }

    /** Zangengriff: vorstürmen, zupacken, kneifen – und die Beute weit fortschleudern. */
    private void tickGrab(ServerLevel level, @Nullable LivingEntity target, int t) {
        if (t <= GRAB_CATCH) {
            if (target != null) {
                this.face(target.position(), 12.0f);
            }
            if (t >= 4) {
                Vec3 forward = this.getLookAngle().multiply(1.0, 0.0, 1.0).normalize();
                this.setDeltaMovement(forward.scale(0.42).add(0.0, this.getDeltaMovement().y, 0.0));
            }
        }
        if (t == GRAB_CATCH) {
            level.playSound(null, this.blockPosition(), ModSounds.GIANT_CRAB_PINCH.get(), SoundSource.HOSTILE, 3.0f, 0.5f);
            Vec3 claw = this.clawPoint();
            LivingEntity caught = this.enemiesNear(level, 8.0).stream()
                    .filter(e -> e.position().add(0.0, e.getBbHeight() * 0.5, 0.0).distanceTo(claw) < 3.2)
                    .min(Comparator.comparingDouble(e -> e.position().distanceTo(claw))).orElse(null);
            if (caught != null) {
                this.grabbed = caught.getUUID();
                caught.hurtServer(level, this.damageSources().mobAttack(this), (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.5f);
            }
        }
        LivingEntity held = this.grabbed != null && level.getEntity(this.grabbed) instanceof LivingEntity e && e.isAlive() ? e : null;
        if (held != null && t > GRAB_CATCH && t < GRAB_THROW) {
            Vec3 claw = this.clawPoint();
            held.teleportTo(claw.x, claw.y - held.getBbHeight() * 0.4, claw.z);
            held.setDeltaMovement(Vec3.ZERO);
            held.fallDistance = 0.0f;
            held.needsSync = true;
            if (t == 20 || t == 27) {
                held.hurtServer(level, this.damageSources().mobAttack(this), (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.35f);
                level.playSound(null, held.blockPosition(), ModSounds.GIANT_CRAB_PINCH.get(), SoundSource.HOSTILE, 2.0f, 0.7f);
            }
        }
        if (t == GRAB_THROW) {
            level.broadcastEntityEvent(this, EVENT_THROW);
            if (held != null) {
                Vec3 forward = this.getLookAngle().multiply(1.0, 0.0, 1.0).normalize();
                Vec3 fling = forward.yRot((this.random.nextBoolean() ? 1 : -1) * 0.9f).scale(1.6);
                held.setDeltaMovement(fling.x, 0.95, fling.z);
                held.needsSync = true;
                held.needsSync = true;
                level.playSound(null, held.blockPosition(), ModSounds.REEF_COLOSSUS_CLACK.get(), SoundSource.HOSTILE, 2.0f, 1.0f);
            }
            this.grabbed = null;
        }
        if (t >= GRAB_TICKS || held == null && t > GRAB_CATCH + 12) {
            this.grabbed = null;
            this.finish();
        }
    }

    /** Blasenstrahl: aus dem Maul schießt Wasser, das seinem Ziel langsam folgt und alles zurückdrängt. */
    private void tickJet(ServerLevel level, @Nullable LivingEntity target, int t) {
        this.getNavigation().stop();
        float yaw = this.entityData.get(DATA_JET_YAW);
        if (target != null) {
            float wanted = -((float) Mth.atan2(target.getX() - this.getX(), target.getZ() - this.getZ())) * Mth.RAD_TO_DEG;
            yaw = Mth.approachDegrees(yaw, wanted, t < JET_START ? 8.0f : 2.4f);
            this.entityData.set(DATA_JET_YAW, yaw);
        }
        this.setYRot(yaw);
        this.yBodyRot = yaw;
        this.yHeadRot = yaw;
        if (t == 2) {
            level.playSound(null, this.blockPosition(), ModSounds.REEF_COLOSSUS_ROAR.get(), SoundSource.HOSTILE, 2.0f, 1.3f);
        }
        if (t < JET_START) {
            Vec3 m = this.mouthPosition();
            level.sendParticles(ParticleTypes.BUBBLE_POP, m.x, m.y, m.z, 4, 0.4, 0.3, 0.4, 0.05);
            return;
        }
        if (t == JET_START) {
            this.entityData.set(DATA_JETTING, true);
            level.playSound(null, this.blockPosition(), ModSounds.REEF_COLOSSUS_JET.get(), SoundSource.HOSTILE, 3.0f, 1.0f);
        }
        if (t == JET_START + 26) {
            level.playSound(null, this.blockPosition(), ModSounds.REEF_COLOSSUS_JET.get(), SoundSource.HOSTILE, 3.0f, 0.9f);
        }
        if (t < JET_END && t % 3 == 0) {
            Vec3 from = this.mouthPosition();
            Vec3 dir = jetDirection(yaw);
            float damage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.3f;
            for (LivingEntity e : this.enemiesNear(level, JET_RANGE + 2)) {
                Vec3 c = e.position().add(0.0, e.getBbHeight() * 0.5, 0.0).subtract(from);
                double along = c.dot(dir);
                if (along < 0.0 || along > JET_RANGE) {
                    continue;
                }
                double off = c.subtract(dir.scale(along)).length();
                if (off < 1.0 + along * 0.06 + e.getBbWidth() * 0.5) {
                    e.hurtServer(level, this.damageSources().mobAttack(this), damage);
                    e.push(dir.x * 0.55, 0.12, dir.z * 0.55);
                    e.needsSync = true;
                    e.clearFire();
                    e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 0), this);
                }
            }
            Vec3 end = from.add(dir.scale(JET_RANGE));
            level.sendParticles(ParticleTypes.SPLASH, end.x, end.y, end.z, 10, 0.8, 0.3, 0.8, 0.2);
        }
        if (t == JET_END) {
            this.entityData.set(DATA_JETTING, false);
        }
        if (t >= JET_TICKS) {
            this.finish();
        }
    }

    /** Eingraben: er versinkt im Sand, wühlt auf sein Ziel zu und bricht unter ihm hervor. */
    private void tickBurrow(ServerLevel level, @Nullable LivingEntity target, int t) {
        this.getNavigation().stop();
        if (t == 1) {
            level.playSound(null, this.blockPosition(), ModSounds.REEF_COLOSSUS_BURROW.get(), SoundSource.HOSTILE, 3.0f, 1.0f);
        }
        if (t <= BURROW_DOWN) {
            this.setSink(t / (float) BURROW_DOWN);
            this.sandBurst(level, this.position(), 2.4, 10, 0.12);
            return;
        }
        if (t < BURROW_LOCK) {
            this.noPhysics = true;
            this.setNoGravity(true);
            if (target != null) {
                Vec3 to = target.position().subtract(this.position()).multiply(1.0, 0.0, 1.0);
                double len = to.length();
                Vec3 step = len < 0.3 ? Vec3.ZERO : to.scale(Math.min(0.34, len) / len);
                Vec3 next = this.clampToArena(this.position().add(step));
                int ground = level.getHeight(Heightmap.Types.OCEAN_FLOOR, Mth.floor(next.x), Mth.floor(next.z));
                this.setPos(next.x, ground, next.z);
            }
            this.setDeltaMovement(Vec3.ZERO);
            // die Spur: aufgewühlter Sand und Blasen
            this.sandBurst(level, this.position(), 1.0, 6, 0.08);
            level.sendParticles(ParticleTypes.BUBBLE_COLUMN_UP, this.getX(), this.getY() + 0.3, this.getZ(), 3, 0.6, 0.2, 0.6, 0.05);
            if (t % 10 == 0) {
                level.playSound(null, this.blockPosition(), ModSounds.REEF_COLOSSUS_BURROW.get(), SoundSource.HOSTILE, 1.6f, 0.7f);
            }
            return;
        }
        if (t == BURROW_LOCK) {
            this.eruptPoint = target != null ? target.position() : this.position();
        }
        if (t < BURROW_ERUPT) {
            Vec3 p = this.eruptPoint != null ? this.eruptPoint : this.position();
            Vec3 to = p.subtract(this.position()).multiply(1.0, 0.0, 1.0);
            if (to.length() > 0.2) {
                Vec3 next = this.position().add(to.scale(0.35));
                int ground = level.getHeight(Heightmap.Types.OCEAN_FLOOR, Mth.floor(next.x), Mth.floor(next.z));
                this.setPos(next.x, ground, next.z);
            }
            // die Warnung: der Boden bebt, Sand spritzt
            this.sandBurst(level, this.position(), 2.6, 14, 0.2);
            if (t % 4 == 0) {
                ring(level, this.position(), 3.2, ParticleTypes.BUBBLE_POP, 20);
            }
            if (t == BURROW_LOCK + 2) {
                this.shake(level, 24.0, 0.3f, BURROW_ERUPT - BURROW_LOCK);
            }
            return;
        }
        if (t == BURROW_ERUPT) {
            this.noPhysics = false;
            this.setNoGravity(false);
            level.broadcastEntityEvent(this, EVENT_ERUPT);
            level.playSound(null, this.blockPosition(), ModSounds.REEF_COLOSSUS_ERUPT.get(), SoundSource.HOSTILE, 4.0f, 0.9f);
            this.impact(level, this.position(), 3.4, (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE) * 1.7f, 0.6, 1.2);
            this.sandBurst(level, this.position(), 3.0, 80, 0.4);
        }
        if (t >= BURROW_ERUPT) {
            this.setSink(Math.max(0.0f, 1.0f - (t - BURROW_ERUPT) / 6.0f));
        }
        if (t >= BURROW_TICKS) {
            this.setSink(0.0f);
            this.finish();
        }
    }

    /** Panzerwirbel: zurückgezogen in seine Kuppel rollt er dreimal quer durch die Lagune. */
    private void tickRoll(ServerLevel level, @Nullable LivingEntity target, int t) {
        this.getNavigation().stop();
        if (t == ROLL_START) {
            this.entityData.set(DATA_ROLLING, true);
        }
        if (t < ROLL_START) {
            return;
        }
        int k = (t - ROLL_START) / ROLL_CHARGE;
        int c = (t - ROLL_START) % ROLL_CHARGE;
        if (k >= 3) {
            this.entityData.set(DATA_ROLLING, false);
            this.setDeltaMovement(Vec3.ZERO);
            level.playSound(null, this.blockPosition(), SoundEventsHolder.stun(), SoundSource.HOSTILE, 2.0f, 0.7f);
            this.follow(level, Attack.DIZZY);
            return;
        }
        if (c == 0) {
            Vec3 to = target != null ? target.position().subtract(this.position()) : this.getLookAngle();
            to = to.multiply(1.0, 0.0, 1.0);
            this.rollDir = to.lengthSqr() < 1.0e-4 ? new Vec3(0.0, 0.0, 1.0) : to.normalize();
            this.rollHits.clear();
            level.playSound(null, this.blockPosition(), ModSounds.REEF_COLOSSUS_ROLL.get(), SoundSource.HOSTILE, 3.0f, 1.0f);
        }
        if (c < 24) {
            double speed = Math.min(0.66, 0.2 + c * 0.06);
            this.setDeltaMovement(this.rollDir.x * speed, this.getDeltaMovement().y, this.rollDir.z * speed);
            float yaw = -((float) Mth.atan2(this.rollDir.x, this.rollDir.z)) * Mth.RAD_TO_DEG;
            this.setYRot(yaw);
            this.yBodyRot = yaw;
            if (t % 2 == 0) {
                this.sandBurst(level, this.position(), 1.6, 6, 0.15);
            }
            // gegen Fels oder an den Rand der Lagune: abprallen
            boolean edge = this.home != null && this.position().subtract(Vec3.atBottomCenterOf(this.home)).multiply(1.0, 0.0, 1.0).length()
                    > ARENA_RADIUS - 2.0;
            if (c > 3 && (this.horizontalCollision || edge)) {
                level.playSound(null, this.blockPosition(), ModSounds.REEF_COLOSSUS_SLAM.get(), SoundSource.HOSTILE, 2.0f, 1.2f);
                this.shake(level, 20.0, 0.25f, 8);
                this.setDeltaMovement(this.rollDir.scale(-0.3));
                this.attackTick += 24 - c;
            }
            float damage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE) * 1.2f;
            for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(0.6), this::isEnemy)) {
                if (this.rollHits.add(e.getUUID()) && e.hurtServer(level, this.damageSources().mobAttack(this), damage)) {
                    Vec3 away = e.position().subtract(this.position()).multiply(1.0, 0.0, 1.0);
                    away = away.lengthSqr() < 1.0e-4 ? this.rollDir : away.normalize();
                    e.push(away.x * 1.4, 0.5, away.z * 1.4);
                    e.needsSync = true;
                }
            }
        } else {
            this.setDeltaMovement(this.getDeltaMovement().multiply(0.6, 1.0, 0.6));
        }
    }

    /** Ruf der Brut: er klappert mit beiden Scheren, und Riesenkrabben brechen aus dem Sand. */
    private void tickSummon(ServerLevel level, @Nullable LivingEntity target, int t) {
        this.getNavigation().stop();
        if (t == 6 || t == 16) {
            level.playSound(null, this.blockPosition(), ModSounds.REEF_COLOSSUS_CLACK.get(), SoundSource.HOSTILE, 3.0f, 0.7f);
        }
        if (t == 24) {
            int wanted = this.summons >= 2 ? 4 : 3;
            int placed = 0;
            for (int attempt = 0; attempt < 30 && placed < wanted; attempt++) {
                double a = this.random.nextDouble() * Math.PI * 2.0;
                double r = 5.0 + this.random.nextDouble() * 5.0;
                double x = this.getX() + Math.cos(a) * r;
                double z = this.getZ() + Math.sin(a) * r;
                int y = level.getHeight(Heightmap.Types.OCEAN_FLOOR, Mth.floor(x), Mth.floor(z));
                BlockPos feet = BlockPos.containing(x, y, z);
                if (!level.getBlockState(feet.below()).isSolid() || !level.getBlockState(feet).getCollisionShape(level, feet).isEmpty()) {
                    continue;
                }
                GiantCrabEntity crab = ModEntities.GIANT_CRAB.get().create(level, EntitySpawnReason.MOB_SUMMONED);
                if (crab == null) {
                    continue;
                }
                crab.snapTo(x, y, z, this.random.nextFloat() * 360.0f, 0.0f);
                crab.finalizeSpawn(level, level.getCurrentDifficultyAt(feet), EntitySpawnReason.MOB_SUMMONED, null);
                crab.setPersistenceRequired();
                if (target != null) {
                    crab.setTarget(target);
                }
                level.addFreshEntity(crab);
                this.brood.add(crab.getUUID());
                this.sandBurst(level, crab.position(), 0.8, 24, 0.15);
                placed++;
            }
            level.playSound(null, this.blockPosition(), ModSounds.REEF_COLOSSUS_ERUPT.get(), SoundSource.HOSTILE, 2.0f, 1.4f);
        }
        if (t >= SUMMON_TICKS) {
            this.finish();
        }
    }

    /** Gezeitenzorn: er bäumt sich auf, der Speer in seiner Kuppel erwacht. */
    private void tickTide(ServerLevel level, int t) {
        this.getNavigation().stop();
        if (t == 4) {
            level.playSound(null, this.blockPosition(), ModSounds.REEF_COLOSSUS_TIDE.get(), SoundSource.HOSTILE, 4.0f, 0.8f);
        }
        if (t == 18) {
            level.playSound(null, this.blockPosition(), ModSounds.REEF_COLOSSUS_ROAR.get(), SoundSource.HOSTILE, 4.0f, 0.8f);
            this.shake(level, 48.0, 0.6f, 30);
            Vec3 tip = spearTip(this.position(), this.yBodyRot);
            level.sendParticles(ModParticles.SEA_GLOW.get(), tip.x, tip.y, tip.z, 80, 1.5, 1.5, 1.5, 0.2);
            level.sendParticles(ParticleTypes.SPLASH, this.getX(), this.getY() + 1.0, this.getZ(), 120, 4.0, 0.5, 4.0, 0.3);
            for (ServerPlayer player : level.players()) {
                if (player.distanceTo(this) < 64.0) {
                    player.sendOverlayMessage(Component.translatable("message.kingdomsatwar.reef_colossus.tide")
                            .withStyle(net.minecraft.ChatFormatting.AQUA, net.minecraft.ChatFormatting.BOLD));
                }
            }
        }
        if (t >= TIDE_TICKS) {
            this.finish();
        }
    }

    /** Flutlanzen: Wassersäulen stürzen auf seine Feinde – Blasenringe zeigen vorher, wo. */
    private void tickLance(ServerLevel level, @Nullable LivingEntity target, int t) {
        this.getNavigation().stop();
        if (target != null) {
            this.face(target.position(), 6.0f);
        }
        if (t == 6) {
            // sein Ziel immer, dazu bis zu fünf weitere Feinde
            if (target != null) {
                this.lances.add(target.position());
            }
            for (LivingEntity e : this.enemiesNear(level, ARENA_RADIUS + 4)) {
                if (this.lances.size() < 6 && e != target) {
                    this.lances.add(e.position());
                }
            }
            if (target != null) {
                for (int i = 0; i < 2; i++) {
                    double a = this.random.nextDouble() * Math.PI * 2.0;
                    this.lances.add(target.position().add(Math.cos(a) * 3.5, 0.0, Math.sin(a) * 3.5));
                }
            }
            level.playSound(null, this.blockPosition(), ModSounds.REEF_COLOSSUS_TIDE.get(), SoundSource.HOSTILE, 2.5f, 1.2f);
        }
        if (t > 6 && t < LANCE_STRIKE) {
            for (Vec3 p : this.lances) {
                if (t % 3 == 0) {
                    ring(level, p, 2.2, ParticleTypes.BUBBLE_POP, 16);
                }
                if (t > LANCE_STRIKE - 12) {
                    level.sendParticles(ParticleTypes.FALLING_WATER, p.x, p.y + 8.0, p.z, 6, 0.6, 2.0, 0.6, 0.0);
                }
            }
        }
        if (t == LANCE_STRIKE) {
            float damage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE) * 1.1f;
            for (Vec3 p : this.lances) {
                level.playSound(null, p.x, p.y, p.z, ModSounds.REEF_COLOSSUS_LANCE.get(), SoundSource.HOSTILE, 2.0f, 0.9f + this.random.nextFloat() * 0.2f);
                for (int h = 0; h < 12; h++) {
                    level.sendParticles(ModParticles.BOSS_WATER.get(), p.x, p.y + h * 0.8, p.z, 2, 0.3, 0.3, 0.3, 0.02);
                }
                level.sendParticles(ParticleTypes.SPLASH, p.x, p.y + 0.2, p.z, 60, 1.6, 0.3, 1.6, 0.3);
                for (LivingEntity e : this.enemiesNear(level, ARENA_RADIUS + 8)) {
                    if (e.position().distanceTo(p) < 2.3) {
                        if (e.hurtServer(level, this.damageSources().mobAttack(this), damage)) {
                            e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 50, 1), this);
                            e.push(0.0, 0.4, 0.0);
                            e.needsSync = true;
                        }
                    }
                }
            }
        }
        if (t >= LANCE_TICKS) {
            this.finish();
        }
    }

    /**
     * Mahlstrom: Er reckt Scheren und Speer und beginnt sich zu drehen, immer schneller. Um ihn kreist ein Strudel (ein
     * Wasserkreis, Blasen und Gischt in Spiralen): Er zieht alle in vierzehn Blöcken zu ihm hin und um ihn herum; wer ihm zu
     * nahe kommt, gerät zwischen die mahlenden Scheren. Zuletzt schießt um ihn eine Wassersäule empor und wirft alle in die
     * Luft.
     */
    private void tickMaelstrom(ServerLevel level, @Nullable LivingEntity target, int t) {
        this.getNavigation().stop();
        this.setDeltaMovement(this.getDeltaMovement().multiply(0.0, 1.0, 0.0));
        Vec3 c = this.position();
        if (t == 2) {
            level.playSound(null, this.blockPosition(), ModSounds.REEF_COLOSSUS_ROAR.get(), SoundSource.HOSTILE, 3.0f, 0.9f);
        }
        if (t == MAELSTROM_START) {
            level.playSound(null, this.blockPosition(), ModSounds.REEF_COLOSSUS_TIDE.get(), SoundSource.HOSTILE, 3.5f, 1.2f);
            SpellFxEntity.create(level, SpellFxEntity.Kind.CAST_CIRCLE, c.add(0.0, 0.05, 0.0), DragonType.WATER, 0x40B8FF,
                    MAELSTROM_BURST - MAELSTROM_START + 4, (float) MAELSTROM_RADIUS).param(0.9f).anchor(this).spawn();
        }
        if (t >= MAELSTROM_START && t < MAELSTROM_BURST) {
            // immer schneller drehen
            float spin = 6.0f + 16.0f * Math.min(1.0f, (t - MAELSTROM_START) / 20.0f);
            float yaw = this.getYRot() + spin;
            this.setYRot(yaw);
            this.yBodyRot = yaw;
            this.yHeadRot = yaw;
            if ((t - MAELSTROM_START) % 24 == 0) {
                level.playSound(null, this.blockPosition(), ModSounds.REEF_COLOSSUS_JET.get(), SoundSource.HOSTILE, 2.5f, 0.7f);
            }
            if (t % 2 == 0) {
                // Spiralen aus Blasen und Gischt, die nach innen laufen
                for (int i = 0; i < 6; i++) {
                    double a = t * 0.32 + i * Math.PI / 3.0;
                    double r = MAELSTROM_RADIUS - ((t * 0.55 + i * 2.3) % (MAELSTROM_RADIUS - 2.0));
                    double x = c.x + Math.cos(a) * r;
                    double z = c.z + Math.sin(a) * r;
                    level.sendParticles(ParticleTypes.SPLASH, x, c.y + 0.3, z, 3, 0.3, 0.1, 0.3, 0.05);
                    level.sendParticles(ParticleTypes.BUBBLE_POP, x, c.y + 0.5, z, 1, 0.2, 0.2, 0.2, 0.0);
                }
            }
            for (LivingEntity e : this.enemiesNear(level, MAELSTROM_RADIUS)) {
                Vec3 to = c.subtract(e.position()).multiply(1.0, 0.0, 1.0);
                double d = to.length();
                if (d > MAELSTROM_RADIUS || d < 1.0e-3) {
                    continue;
                }
                Vec3 in = to.normalize();
                Vec3 around = new Vec3(-in.z, 0.0, in.x);
                double pull = 0.035 + 0.05 * (1.0 - d / MAELSTROM_RADIUS);
                e.push(in.x * pull + around.x * 0.045, 0.0, in.z * pull + around.z * 0.045);
                e.needsSync = true;
                if (d < 4.2 && t % 8 == 0) {
                    float damage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.45f;
                    if (e.hurtServer(level, this.damageSources().mobAttack(this), damage)) {
                        e.push(around.x * 0.6, 0.2, around.z * 0.6);
                    }
                }
            }
        }
        if (t == MAELSTROM_BURST) {
            SpellFxEntity.create(level, SpellFxEntity.Kind.PILLAR, c, DragonType.WATER, 0x60C8FF, 22, 4.5f).param(11.0f).spawn();
            level.playSound(null, this.blockPosition(), ModSounds.REEF_COLOSSUS_LANCE.get(), SoundSource.HOSTILE, 3.5f, 0.7f);
            this.impact(level, c, 7.5, (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE) * 1.0f, 1.3, 1.1);
        }
        if (t >= MAELSTROM_TICKS) {
            this.finish();
        }
    }

    /**
     * Blasenfalle: Er bläst – schimmernde Blasen schweben auf seine Feinde zu (bis zu drei). Wer darin gefangen ist, schwebt
     * langsam empor und kommt kaum vom Fleck, die Luft wird knapp; nach gut drei Sekunden platzt die Blase, und er stürzt
     * herab.
     */
    private void tickBubbles(ServerLevel level, @Nullable LivingEntity target, int t) {
        this.getNavigation().stop();
        if (target != null) {
            this.face(target.position(), 10.0f);
        }
        if (t == 2) {
            level.playSound(null, this.blockPosition(), ModSounds.REEF_COLOSSUS_ROAR.get(), SoundSource.HOSTILE, 2.0f, 1.5f);
        }
        if (t < BUBBLE_BLOW) {
            Vec3 m = this.mouthPosition();
            level.sendParticles(ParticleTypes.BUBBLE_POP, m.x, m.y, m.z, 3, 0.5, 0.3, 0.5, 0.03);
            return;
        }
        if (t == BUBBLE_BLOW) {
            this.bubbleVictims.clear();
            level.playSound(null, this.blockPosition(), ModSounds.REEF_COLOSSUS_JET.get(), SoundSource.HOSTILE, 2.5f, 1.6f);
            if (target != null && this.isEnemy(target)) {
                this.bubbleVictims.add(target.getUUID());
            }
            for (LivingEntity e : this.enemiesNear(level, 22.0)) {
                if (this.bubbleVictims.size() < 3 && !this.bubbleVictims.contains(e.getUUID()) && e instanceof Player) {
                    this.bubbleVictims.add(e.getUUID());
                }
            }
        }
        int flight = 10;
        if (t > BUBBLE_BLOW && t < BUBBLE_BLOW + flight) {
            // die Blasen schweben vom Maul zu den Opfern
            Vec3 m = this.mouthPosition();
            double k = (t - BUBBLE_BLOW) / (double) flight;
            for (UUID id : this.bubbleVictims) {
                if (level.getEntity(id) instanceof LivingEntity e) {
                    Vec3 at = m.add(e.position().add(0.0, 1.0, 0.0).subtract(m).scale(k));
                    level.sendParticles(ParticleTypes.BUBBLE, at.x, at.y, at.z, 6, 0.4, 0.4, 0.4, 0.02);
                    level.sendParticles(ParticleTypes.BUBBLE_POP, at.x, at.y, at.z, 2, 0.5, 0.5, 0.5, 0.0);
                }
            }
        }
        if (t == BUBBLE_BLOW + flight) {
            for (UUID id : this.bubbleVictims) {
                if (level.getEntity(id) instanceof LivingEntity e && e.isAlive() && e.distanceTo(this) < 30.0) {
                    this.trap(level, e);
                }
            }
        }
        if (t >= BUBBLE_TICKS) {
            this.finish();
        }
    }

    /** Eine Blase um ein Wesen: Es schwebt empor, kommt kaum vom Fleck und verliert Luft – bis sie platzt. */
    private void trap(ServerLevel level, LivingEntity e) {
        level.playSound(null, e.getX(), e.getY(), e.getZ(), ModSounds.REEF_COLOSSUS_JET.get(), SoundSource.HOSTILE, 1.5f, 2.0f);
        float damage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.12f;
        SpellFxEntity.create(level, SpellFxEntity.Kind.BUBBLE, e.position(), DragonType.WATER, 0x9AE8FF, BUBBLE_LIFE,
                Math.max(2.2f, e.getBbHeight() * 1.25f)).anchor(e).onTick(fx -> {
                    if (!e.isAlive()) {
                        fx.discard();
                        return;
                    }
                    e.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 6, 1, false, false), this);
                    e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 6, 3, false, false), this);
                    if (fx.tickCount % 16 == 8) {
                        e.hurtServer(level, this.damageSources().drown(), damage);
                    }
                }).onExpire(fx -> {
                    e.removeEffect(MobEffects.LEVITATION);
                    level.playSound(null, e.getX(), e.getY(), e.getZ(), ModSounds.REEF_COLOSSUS_LANCE.get(), SoundSource.HOSTILE, 1.2f, 1.8f);
                    level.sendParticles(ParticleTypes.SPLASH, e.getX(), e.getY(0.5), e.getZ(), 50, 0.8, 0.8, 0.8, 0.3);
                    level.sendParticles(ParticleTypes.BUBBLE_POP, e.getX(), e.getY(0.5), e.getZ(), 30, 0.9, 0.9, 0.9, 0.1);
                }).spawn();
    }

    /** Brandungswellen: Ringe, die sich von einem Scherenschlag aus über die Lagune ausbreiten – wer am Boden steht, wird getroffen. */
    private void tickWaves(ServerLevel level) {
        if (this.waves.isEmpty()) {
            return;
        }
        float damage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.6f;
        for (var it = this.waves.iterator(); it.hasNext(); ) {
            double[] w = it.next();
            w[3] += 1;
            double r = 2.0 + w[3] * 0.45;
            if (r > 13.0) {
                it.remove();
                continue;
            }
            Vec3 c = new Vec3(w[0], w[1], w[2]);
            if ((int) w[3] % 2 == 0) {
                ring(level, c, r, ParticleTypes.SPLASH, (int) (12 + r * 3));
                ring(level, c.add(0.0, 0.4, 0.0), r, ParticleTypes.BUBBLE_POP, (int) (8 + r * 2));
            }
            for (LivingEntity e : this.enemiesNear(level, 16.0)) {
                double d = e.position().subtract(c).multiply(1.0, 0.0, 1.0).length();
                if (Math.abs(d - r) < 0.6 && e.onGround() && Math.abs(e.getY() - c.y) < 1.5 && this.waveHits.add(e.getUUID())) {
                    if (e.hurtServer(level, this.damageSources().mobAttack(this), damage)) {
                        Vec3 away = e.position().subtract(c).multiply(1.0, 0.0, 1.0).normalize();
                        e.push(away.x * 0.9, 0.35, away.z * 0.9);
                        e.needsSync = true;
                    }
                }
            }
            if (w[3] == 1) {
                this.waveHits.clear();
            }
        }
    }

    private boolean isEnemy(LivingEntity e) {
        return e != this && e.isAlive() && !(e instanceof GiantCrabEntity) && !(e instanceof ReefColossusEntity)
                && !(e instanceof Player p && (p.isCreative() || p.isSpectator()));
    }

    private List<LivingEntity> enemiesNear(ServerLevel level, double radius) {
        return level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(radius, 10.0, radius), this::isEnemy);
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

    /** Einschlag: Schaden nach Nähe, Gegner fliegen auf und weg. */
    private void impact(ServerLevel level, Vec3 c, double radius, float damage, double push, double up) {
        this.shake(level, 26.0, 0.4f, 12);
        level.playSound(null, c.x, c.y, c.z, ModSounds.REEF_COLOSSUS_SLAM.get(), SoundSource.HOSTILE, 3.0f, 0.8f);
        level.sendParticles(ParticleTypes.EXPLOSION, c.x, c.y + 0.3, c.z, 2, 0.6, 0.1, 0.6, 0.0);
        level.sendParticles(ParticleTypes.SPLASH, c.x, c.y + 0.3, c.z, 80, radius * 0.4, 0.2, radius * 0.4, 0.3);
        this.sandBurst(level, c, radius * 0.5, 40, 0.25);
        ring(level, c, radius, ParticleTypes.BUBBLE_POP, 28);
        for (LivingEntity e : this.enemiesNear(level, 30.0)) {
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

    /** Aufgewühlter Sand (Blockpartikel des Bodens unter der Stelle). */
    private void sandBurst(ServerLevel level, Vec3 at, double spread, int count, double speed) {
        BlockPos below = BlockPos.containing(at.x, at.y - 0.5, at.z);
        BlockState ground = level.getBlockState(below);
        if (ground.isAir() || !ground.getFluidState().isEmpty()) {
            ground = Blocks.SAND.defaultBlockState();
        }
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, ground), at.x, at.y + 0.2, at.z, count, spread, 0.3, spread, speed);
    }

    private static void ring(ServerLevel level, Vec3 c, double radius, ParticleOptions particle, int count) {
        for (int i = 0; i < count; i++) {
            double a = i / (double) count * Math.PI * 2.0;
            level.sendParticles(particle, c.x + Math.cos(a) * radius, c.y + 0.15, c.z + Math.sin(a) * radius, 1, 0.0, 0.0, 0.0, 0.0);
        }
    }

    // ------------------------------------------------------------------ Schaden

    @Override
    public boolean isInvulnerableTo(ServerLevel level, DamageSource source) {
        return this.attack == Attack.AWAKEN || this.attack == Attack.BURROW && this.sink() > 0.5f || super.isInvulnerableTo(level, source);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        if (this.isRolling()) {
            // in der Kuppel: Pfeile prallen ab, Schläge richten wenig aus
            if (source.getDirectEntity() instanceof Projectile) {
                level.playSound(null, this.blockPosition(), net.minecraft.sounds.SoundEvents.SHIELD_BLOCK.value(), SoundSource.HOSTILE, 1.5f, 0.8f);
                return false;
            }
            amount *= 0.25f;
        } else if (this.isStunned()) {
            amount *= 1.5f;
        }
        return super.hurtServer(level, source, amount);
    }

    // ------------------------------------------------------------------ Ereignisse, Rückzug und Tod

    @Override
    public void handleEntityEvent(byte id) {
        switch (id) {
            case EVENT_AWAKEN -> this.awakenAnimation.start(this.tickCount);
            case EVENT_SLAM -> this.slamAnimation.start(this.tickCount);
            case EVENT_GRAB -> this.grabAnimation.start(this.tickCount);
            case EVENT_THROW -> this.throwAnimation.start(this.tickCount);
            case EVENT_JET -> this.jetAnimation.start(this.tickCount);
            case EVENT_BURROW -> this.burrowAnimation.start(this.tickCount);
            case EVENT_ERUPT -> this.eruptAnimation.start(this.tickCount);
            case EVENT_ROLL -> this.rollAnimation.start(this.tickCount);
            case EVENT_SUMMON -> this.summonAnimation.start(this.tickCount);
            case EVENT_TIDE -> this.tideAnimation.start(this.tickCount);
            case EVENT_LANCE -> this.lanceAnimation.start(this.tickCount);
            case EVENT_STUN -> this.stunAnimation.start(this.tickCount);
            default -> super.handleEntityEvent(id);
        }
    }

    /** Die Brut gräbt sich wieder ein (wenn er stirbt oder sich zurückzieht). */
    private void dismissBrood(ServerLevel level) {
        for (UUID id : this.brood) {
            if (level.getEntity(id) instanceof GiantCrabEntity crab && crab.isAlive()) {
                this.sandBurst(level, crab.position(), 0.6, 20, 0.1);
                crab.discard();
            }
        }
        this.brood.clear();
    }

    /** Niemand mehr in der Lagune: er gräbt sich wieder ein (er kann erneut erwachen). */
    private void slumber(ServerLevel level) {
        this.dismissBrood(level);
        this.sandBurst(level, this.position(), 3.0, 80, 0.3);
        level.playSound(null, this.blockPosition(), ModSounds.REEF_COLOSSUS_BURROW.get(), SoundSource.HOSTILE, 3.0f, 0.8f);
        if (this.core != null && level.getBlockEntity(this.core) instanceof AtollCoreBlockEntity atoll) {
            atoll.onColossusSlumber(level);
        }
        this.bossBar.removeAllPlayers();
        this.discard();
    }

    @Override
    protected void tickDeath() {
        this.deathTime++;
        this.setDeltaMovement(0.0, this.getDeltaMovement().y, 0.0);
        if (this.level() instanceof ServerLevel level) {
            this.setSink(Math.min(0.32f, this.deathTime / 160.0f));
            if (this.deathTime == 1) {
                level.playSound(null, this.blockPosition(), ModSounds.REEF_COLOSSUS_DEATH.get(), SoundSource.HOSTILE, 4.0f, 1.0f);
                this.entityData.set(DATA_JETTING, false);
                this.entityData.set(DATA_ROLLING, false);
            }
            if (this.deathTime % 5 == 0) {
                this.sandBurst(level, this.position(), 2.4, 16, 0.15);
                level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.PRISMARINE_BRICKS.defaultBlockState()),
                        this.getX(), this.getY() + 3.2, this.getZ(), 10, 1.6, 1.0, 1.6, 0.1);
            }
            if (this.deathTime == 55 && this.hasSpear()) {
                // der Gezeiten-Speer bricht aus der Kuppel und fliegt empor
                this.entityData.set(DATA_SPEAR, false);
                Vec3 tip = spearTip(this.position(), this.yBodyRot);
                ItemEntity spear = new ItemEntity(level, tip.x, tip.y, tip.z, new ItemStack(SeaBlocks.TIDE_SPEAR.get()));
                spear.setDeltaMovement((this.random.nextDouble() - 0.5) * 0.15, 0.55, (this.random.nextDouble() - 0.5) * 0.15);
                spear.setPickUpDelay(30);
                spear.setGlowingTag(true);
                spear.setUnlimitedLifetime();
                level.addFreshEntity(spear);
                level.sendParticles(ModParticles.SEA_GLOW.get(), tip.x, tip.y, tip.z, 120, 1.2, 1.2, 1.2, 0.3);
                level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.PRISMARINE.defaultBlockState()), tip.x, tip.y - 1.0,
                        tip.z, 60, 1.0, 0.6, 1.0, 0.2);
                level.playSound(null, BlockPos.containing(tip), net.minecraft.sounds.SoundEvents.TRIDENT_RETURN, SoundSource.HOSTILE, 3.0f, 0.8f);
                level.playSound(null, BlockPos.containing(tip), ModSounds.REEF_COLOSSUS_TIDE.get(), SoundSource.HOSTILE, 3.0f, 1.3f);
            }
            if (this.deathTime == DEATH_TICKS / 2) {
                this.dismissBrood(level);
            }
            if (this.deathTime == DEATH_TICKS - 4) {
                this.shake(level, 48.0, 0.5f, 24);
                level.sendParticles(ParticleTypes.SPLASH, this.getX(), this.getY() + 1.0, this.getZ(), 200, 3.0, 1.0, 3.0, 0.4);
                level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, this.getX(), this.getY() + 2.0, this.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
            }
            if (this.deathTime >= DEATH_TICKS && !this.isRemoved()) {
                this.bossBar.removeAllPlayers();
                if (this.core != null && level.getBlockEntity(this.core) instanceof AtollCoreBlockEntity atoll) {
                    atoll.onColossusDefeated(level);
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
        output.putInt("Summons", this.summons);
        output.putBoolean("Tide", this.tide);
        output.putBoolean("Spear", this.hasSpear());
        output.store("Brood", UUIDUtil.CODEC.listOf(), this.brood);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.home = input.getLong("Home").map(BlockPos::of).orElse(null);
        this.core = input.getLong("Core").map(BlockPos::of).orElse(null);
        this.summons = input.getIntOr("Summons", 0);
        this.tide = input.getBooleanOr("Tide", false);
        this.entityData.set(DATA_TIDE, this.tide);
        this.entityData.set(DATA_SPEAR, input.getBooleanOr("Spear", true));
        this.brood.clear();
        input.read("Brood", UUIDUtil.CODEC.listOf()).ifPresent(this.brood::addAll);
        this.setSink(0.0f);
        this.bossBar.setProgress(this.getHealth() / this.getMaxHealth());
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return this.sink() > 0.5f ? null : ModSounds.REEF_COLOSSUS_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.REEF_COLOSSUS_HURT.get();
    }

    @Override
    protected @Nullable SoundEvent getDeathSound() {
        return null;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(ModSounds.REEF_COLOSSUS_STEP.get(), 1.2f, 0.8f + this.random.nextFloat() * 0.2f);
    }

    @Override
    protected float getSoundVolume() {
        return 3.0f;
    }

    @Override
    public int getAmbientSoundInterval() {
        return 140;
    }

    /** Vanilla-Klänge, die es nur als Holder gibt. */
    private static final class SoundEventsHolder {
        static SoundEvent stun() {
            return net.minecraft.sounds.SoundEvents.RAVAGER_STUNNED;
        }
    }

    // ------------------------------------------------------------------ Himmelswacht (0.82)

    @Override
    public io.github.sirschrolli.kingdomsatwar.entity.boss.sky.SkyTheme skyTheme() {
        return io.github.sirschrolli.kingdomsatwar.entity.boss.sky.SkyTheme.REEF;
    }

    @Override
    public boolean guardsSky() {
        return this.attack != Attack.AWAKEN && this.getTarget() != null && this.getTarget().isAlive();
    }
}
