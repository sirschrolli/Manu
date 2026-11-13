package io.github.sirschrolli.kingdomsatwar.entity.castle;

import io.github.sirschrolli.kingdomsatwar.block.castle.RavenBannerBlockEntity;
import io.github.sirschrolli.kingdomsatwar.entity.boss.sky.SkyTheme;
import io.github.sirschrolli.kingdomsatwar.entity.boss.sky.SkyWarden;
import io.github.sirschrolli.kingdomsatwar.entity.creature.farm.FarmHorse;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.Warhorse;
import io.github.sirschrolli.kingdomsatwar.magic.fx.SpellFxEntity;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
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
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.BlocksAttacks;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Kuno von Rabenfels, der Raubritter (seit 1.8): Herr der Raubritterburg, Schrecken der Handelsstraßen. Er hält Hof in seinem
 * Burghof – hoch zu Ross unter dem Rabenbanner seines Geschlechts, die Lanze aufrecht. Wer der Burg zu nahe kommt
 * ({@value #WAKE_BLOCKS} Blöcke) oder ihn angreift, den fordert er: Das Ross steigt, er reckt die Lanze und senkt sie ein.
 *
 * <p><b>Zu Ross</b> (bis zur halben Kraft):
 * <ul>
 *     <li><b>Lanzengang</b> – steht man ihm zu nah, reitet er erst fort; dann wendet er, legt die Lanze ein und sprengt in
 *     gerader Linie heran (wer getroffen wird, fliegt). Wer den Stoß mit einem <b>Schild</b> auffängt, an dem bricht die
 *     Lanze – er wankt im Sattel ({@value #STAGGER_SECONDS} Sekunden, mehr Schaden). Gegen eine Mauer hält er an.</li>
 *     <li><b>Lanzenstich</b> – aus dem Sattel, nah und schnell;</li>
 *     <li><b>Steigen</b> – das Ross steigt und schlägt mit der Vorderhand auf: wer am Boden steht, wird getroffen;</li>
 *     <li><b>Hornruf</b> – er bläst ins Horn, seine Leute kommen ihm zu Hilfe (höchstens {@value #MEN_MAX});</li>
 *     <li><b>Bolzenhagel</b> – er weist mit der Lanze auf sein Ziel: Rote Zeichen am Boden, dann hageln die Bolzen seiner
 *     Armbrustschützen dort nieder.</li>
 * </ul>
 * <b>Zu Fuß</b> (unter halber Kraft wirft ihn das Ross ab und flieht – es bleibt in der Nähe, man kann es zähmen): Er rafft
 * sich auf und zieht den <b>Morgenstern</b>:
 * <ul>
 *     <li><b>Morgensternschlag</b> – hoch über den Kopf, dann vor sich auf den Boden;</li>
 *     <li><b>Wirbel</b> – er dreht sich zweimal mit ausgestrecktem Morgenstern;</li>
 *     <li><b>Schildstoß</b> – geduckt hinter dem Schild, dann ein Stoß, der fortwirft (ein Schild, der ihn auffängt, ist danach
 *     eine Weile unbrauchbar);</li>
 *     <li><b>Schildwall</b> – der Schild hoch vor dem Leib: Von vorn trifft ihn nichts (Pfeile prallen ab) – von der Seite und
 *     von hinten sehr wohl;</li>
 *     <li>dazu Hornruf und Bolzenhagel.</li>
 * </ul>
 * Findet er niemanden mehr, kehrt er unter sein Banner zurück (geheilt und wieder zu Ross).
 */
public class RobberKnightEntity extends Monster implements SkyWarden {
    public static final float MAX_HEALTH = 720.0f;
    public static final int AWAKEN_TICKS = 70;
    /** So lange (Ticks) dauert sein Sterben (er sinkt in die Knie, dann fällt er). */
    public static final int DEATH_TICKS = 90;
    /** So nah (waagerecht) muss man seinem Platz kommen, damit er erwacht. */
    public static final double WAKE_RADIUS = 14.0;
    static final int WAKE_BLOCKS = 14;
    /** Weiter als so weit von seinem Platz folgt er niemandem … */
    private static final double LEASH = 40.0;
    /** … und wer ihn aus der Burg lockt, dem entkommt er zurück in den Hof. */
    public static final double ARENA = 28.0;
    private static final int RETURN_TICKS = 400;

    // ---- zu Ross
    public static final float CHARGE_DAMAGE = 14.0f;
    /** So lange wendet er (und reitet, wenn man ihm zu nah steht, erst fort), dann sprengt er los. */
    public static final int CHARGE_AIM = 16;
    /** Höchstens so lange sprengt er (Ticks) … */
    public static final int CHARGE_GALLOP = 40;
    /** … so schnell (Blöcke je Tick) … */
    public static final double CHARGE_SPEED = 0.8;
    /** … und so weit (Blöcke). */
    public static final double CHARGE_DISTANCE = 22.0;
    /** So weit vor seiner Mitte trifft die Lanzenspitze. */
    public static final double CHARGE_REACH = 3.4;
    public static final float THRUST_DAMAGE = 9.0f;
    public static final int THRUST_HIT = 12;
    public static final double THRUST_RANGE = 5.6;
    public static final float TRAMPLE_DAMAGE = 8.0f;
    public static final int TRAMPLE_HIT = 22;
    public static final double TRAMPLE_RADIUS = 5.5;
    public static final int HORN_CALL = 16;
    public static final int MEN_MAX = 4;
    /** Höchstens alle so viele Ticks bläst er ins Horn. */
    public static final int HORN_PAUSE = 600;
    public static final int VOLLEY_MARK = 10;
    public static final int VOLLEY_FIRE = 30;
    public static final int VOLLEY_SPOTS = 5;
    public static final double VOLLEY_RADIUS = 1.6;
    public static final float VOLLEY_DAMAGE = 5.0f;
    public static final int STAGGER_TICKS = 60;
    static final int STAGGER_SECONDS = 3;
    /** So viel mehr Schaden nimmt er, solange er im Sattel wankt. */
    public static final float STAGGER_WEAKNESS = 1.3f;
    /** Das Abwerfen: so lange (Ticks), und hier schlägt er auf (das Ross flieht). */
    public static final int UNHORSE_FALL = 20;
    public static final int UNHORSE_MACE = 50;
    // ---- zu Fuß
    public static final float SMASH_DAMAGE = 13.0f;
    public static final int SMASH_HIT = 20;
    public static final double SMASH_RANGE = 4.4;
    public static final float WHIRL_DAMAGE = 8.0f;
    public static final int[] WHIRL_HITS = {18, 34};
    public static final int WHIRL_FROM = 8;
    public static final int WHIRL_TO = 40;
    public static final double WHIRL_RADIUS = 3.9;
    public static final float BASH_DAMAGE = 6.0f;
    public static final int BASH_HIT = 12;
    public static final double BASH_RANGE = 2.6;
    public static final int WALL_FROM = 8;
    public static final int WALL_TO = 62;
    /** Der Schildwall deckt so weit nach jeder Seite (Grad). */
    public static final double WALL_ARC = 70.0;
    /** Das Gold seines Wappens. */
    public static final int GOLD = 0xE4C254;
    public static final int RED = 0xB02A2A;

    /** Zu Ross (breit und hoch) und zu Fuß. */
    private static final EntityDimensions MOUNTED = EntityDimensions.scalable(1.7f, 3.3f).withEyeHeight(2.95f);
    private static final EntityDimensions ON_FOOT = EntityDimensions.scalable(0.9f, 2.4f).withEyeHeight(2.1f);

    public enum Attack {
        NONE(0), GUARD(0), AWAKEN(AWAKEN_TICKS), CHARGE(CHARGE_AIM + CHARGE_GALLOP), HALT(30), THRUST(26), TRAMPLE(40), HORN(42),
        VOLLEY(50), STAGGER(STAGGER_TICKS), UNHORSE(64), SMASH(38), WHIRL(48), BASH(28), WALL(70);

        public final int duration;

        Attack(int duration) {
            this.duration = duration;
        }
    }

    private static final EntityDataAccessor<Integer> DATA_ATTACK = SynchedEntityData.defineId(RobberKnightEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_MOUNTED = SynchedEntityData.defineId(RobberKnightEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_LANCE = SynchedEntityData.defineId(RobberKnightEntity.class, EntityDataSerializers.BOOLEAN);

    private final ServerBossEvent bossBar = new ServerBossEvent(UUID.randomUUID(), Component.translatable("boss.kingdomsatwar.robber_knight"),
            BossEvent.BossBarColor.YELLOW, BossEvent.BossBarOverlay.NOTCHED_10);
    private Attack attack = Attack.NONE;
    private Attack lastAttack = Attack.NONE;
    private int attackTick;
    private int cooldown = 30;
    private int idleTicks;
    private long lastHorn = -HORN_PAUSE;
    /** Für Tests: nur wer so nah ist, wird getroffen (siehe {@link #confine}). */
    private double confine = Double.MAX_VALUE;
    private @Nullable BlockPos home;
    private @Nullable BlockPos banner;
    private float homeYaw;
    private final Set<UUID> struck = new HashSet<>();
    /** Lanzengang: wohin er sprengt und wo er losritt. */
    private Vec3 chargeDir = Vec3.ZERO;
    private Vec3 chargeStart = Vec3.ZERO;
    /** Bolzenhagel: wo die Zeichen am Boden liegen. */
    private final List<Vec3> marks = new ArrayList<>();
    /** Die Bolzen, die gerade fallen (sie verschwinden bald wieder). */
    private final List<AbstractArrow> bolts = new ArrayList<>();
    /** Seine Leute, die er herbeigerufen hat. */
    private final Set<UUID> men = new HashSet<>();
    private int clientAttackStart;

    public RobberKnightEntity(EntityType<? extends RobberKnightEntity> type, Level level) {
        super(type, level);
        this.xpReward = 450;
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, MAX_HEALTH)
                .add(Attributes.ARMOR, 12.0)
                .add(Attributes.ARMOR_TOUGHNESS, 4.0)
                .add(Attributes.ATTACK_DAMAGE, THRUST_DAMAGE)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.FOLLOW_RANGE, 40.0)
                .add(Attributes.STEP_HEIGHT, 1.5)
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
        builder.define(DATA_MOUNTED, true);
        builder.define(DATA_LANCE, true);
    }

    public Attack currentAttack() {
        int i = this.entityData.get(DATA_ATTACK);
        return i >= 0 && i < Attack.values().length ? Attack.values()[i] : Attack.NONE;
    }

    public int attackOrdinal() {
        return this.entityData.get(DATA_ATTACK);
    }

    /** Sitzt er im Sattel? (Unter halber Kraft wirft ihn das Ross ab.) */
    public boolean isMounted() {
        return this.entityData.get(DATA_MOUNTED);
    }

    /** Hat er seine Lanze (sie bricht, wenn jemand seinen Ansturm mit einem Schild auffängt)? */
    public boolean hasLance() {
        return this.entityData.get(DATA_LANCE);
    }

    /** Hält er Hof (unverwundbar, bis jemand kommt)? */
    public boolean isHoldingCourt() {
        return this.currentAttack() == Attack.GUARD;
    }

    public float attackTime(float partialTicks) {
        return this.tickCount - this.clientAttackStart + partialTicks;
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (DATA_ATTACK.equals(key)) {
            this.clientAttackStart = this.tickCount;
        }
        if (DATA_MOUNTED.equals(key)) {
            this.refreshDimensions();
        }
    }

    @Override
    public EntityDimensions getDefaultDimensions(Pose pose) {
        return this.isMounted() ? MOUNTED : ON_FOOT;
    }

    /** Bindet ihn an sein Banner: Er hält bei {@code home} Hof (Blick {@code yaw}), unverwundbar, bis jemand kommt. */
    public void bindTo(BlockPos home, float yaw, @Nullable BlockPos banner) {
        this.home = home.immutable();
        this.homeYaw = yaw;
        this.banner = banner == null ? null : banner.immutable();
        this.setMounted(true);
        this.begin(Attack.GUARD);
    }

    /** Neu gerufen (ein schwarzes Banner am leeren Pfahl): Er erwacht sofort. */
    public void rise(BlockPos home, float yaw, @Nullable BlockPos banner) {
        this.home = home.immutable();
        this.homeYaw = yaw;
        this.banner = banner == null ? null : banner.immutable();
        this.setMounted(true);
        this.begin(Attack.AWAKEN);
    }

    /** Er erwacht (wenn er Hof hält). */
    public void awaken() {
        if (this.attack == Attack.GUARD || this.attack == Attack.NONE && this.getTarget() == null && this.idleTicks > 0) {
            this.begin(Attack.AWAKEN);
        }
    }

    public @Nullable BlockPos home() {
        return this.home;
    }

    public @Nullable BlockPos banner() {
        return this.banner;
    }

    /** Beginnt sofort einen bestimmten Angriff (für Tests und die Vorführung). */
    public void forceAttack(Attack next) {
        if (next == Attack.CHARGE || next == Attack.HALT) {
            this.chargeDir = this.forward();
            this.chargeStart = this.position();
        }
        this.begin(next);
    }

    /** Sofort zu Fuß (für Tests und die Vorführung). */
    public void forceOnFoot() {
        this.setMounted(false);
    }

    /** Für Tests: seine Leute, die gerade leben, und die Zeichen des Bolzenhagels. */
    public int menAlive(ServerLevel level) {
        this.men.removeIf(id -> !(level.getEntity(id) instanceof LivingEntity man) || !man.isAlive());
        return this.men.size();
    }

    public List<Vec3> volleyMarks() {
        return List.copyOf(this.marks);
    }

    private void setMounted(boolean mounted) {
        this.entityData.set(DATA_MOUNTED, mounted);
        this.entityData.set(DATA_LANCE, true);
        this.refreshDimensions();
        var speed = this.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null) {
            speed.setBaseValue(mounted ? 0.3 : 0.25);
        }
    }

    private void begin(Attack next) {
        this.attack = next;
        this.attackTick = 0;
        this.entityData.set(DATA_ATTACK, next.ordinal());
        if (next != Attack.NONE && next != Attack.AWAKEN && next != Attack.GUARD && next != Attack.STAGGER && next != Attack.HALT
                && next != Attack.UNHORSE) {
            this.lastAttack = next;
        }
        this.struck.clear();
        if (next != Attack.VOLLEY) {
            this.marks.clear();
        }
        if (next != Attack.NONE) {
            this.getNavigation().stop();
        }
        this.bossBar.setVisible(next != Attack.GUARD);
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
    public boolean causeFallDamage(double distance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public boolean isPersistenceRequired() {
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
     * Schaden: Hält er Hof, ist er unverwundbar (ein Spieler, der ihn angreift, weckt ihn); beim Erwachen und während er
     * abgeworfen wird ebenso. Wankt er im Sattel, nimmt er mehr Schaden; hinter dem Schildwall trifft ihn von vorn nichts.
     */
    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return super.hurtServer(level, source, amount);
        }
        if (this.attack == Attack.GUARD) {
            if (source.getEntity() instanceof Player player && !player.isCreative() && !player.isSpectator()) {
                this.wakeBanner(level);
            }
            return false;
        }
        if (this.attack == Attack.AWAKEN || this.attack == Attack.UNHORSE && this.attackTick < UNHORSE_MACE
                || source.is(DamageTypes.IN_WALL) || source.is(DamageTypes.FALL)) {
            return false;
        }
        if (this.attack == Attack.WALL && this.attackTick >= WALL_FROM && this.attackTick <= WALL_TO && this.fromFront(source)) {
            // der Schildwall: Funken, ein dumpfer Schlag – und nichts geht durch
            level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.SHIELD_BLOCK, SoundSource.HOSTILE, 1.4f,
                    0.8f + this.random.nextFloat() * 0.2f);
            Vec3 f = this.forward();
            level.sendParticles(ParticleTypes.CRIT, this.getX() + f.x * 0.8, this.getY(0.6), this.getZ() + f.z * 0.8, 10, 0.3, 0.4, 0.3, 0.2);
            return false;
        }
        if (this.attack == Attack.STAGGER) {
            amount *= STAGGER_WEAKNESS;
        }
        boolean hurt = super.hurtServer(level, source, amount);
        if (hurt && source.getEntity() instanceof LivingEntity attacker && attacker != this && this.canStrike(attacker)
                && !(this.getTarget() instanceof Player)) {
            this.setTarget(attacker);
        }
        return hurt;
    }

    /** Kommt der Schaden von vorn (innerhalb des Schildwalls)? Schaden ohne Ursprung (Gift, Feuer …) nie. */
    private boolean fromFront(DamageSource source) {
        Vec3 from = source.getSourcePosition();
        if (from == null) {
            return false;
        }
        Vec3 to = from.subtract(this.position()).multiply(1.0, 0.0, 1.0);
        if (to.lengthSqr() < 1.0e-4) {
            return false;
        }
        return to.normalize().dot(this.forward()) > Math.cos(Math.toRadians(WALL_ARC));
    }

    private void wakeBanner(ServerLevel level) {
        if (this.banner != null && level.getBlockEntity(this.banner) instanceof RavenBannerBlockEntity raven && raven.owns(this)) {
            raven.awaken(level);
        } else {
            this.begin(Attack.AWAKEN);
        }
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        this.bossBar.setProgress(this.getHealth() / this.getMaxHealth());
        this.tickBolts();
        if (this.tickCount % 40 == 0 && this.strayed(level)) {
            return;
        }
        if (this.attack == Attack.GUARD) {
            this.tickGuard(level);
            return;
        }
        if (this.attack == Attack.AWAKEN) {
            this.tickAwaken(level);
            return;
        }
        if (this.tickCount % 10 == 0) {
            this.keepInCastle(level);
        }
        if (this.attack == Attack.STAGGER) {
            this.tickStagger(level);
            return;
        }
        if (this.attack == Attack.UNHORSE) {
            this.tickUnhorse(level);
            return;
        }
        if (this.isMounted() && this.attack != Attack.CHARGE && this.attack != Attack.HALT
                && this.getHealth() < this.getMaxHealth() * 0.5f) {
            this.begin(Attack.UNHORSE);
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
            this.approach(target);
            if (--this.cooldown <= 0) {
                this.begin(this.chooseAttack(level, target));
            }
            return;
        }
        this.attackTick++;
        if (this.attack != Attack.CHARGE && this.attack != Attack.HALT && this.attack != Attack.WHIRL) {
            this.lookAt(target, 30.0f, 30.0f);
        }
        switch (this.attack) {
            case CHARGE -> this.tickCharge(level, target);
            case HALT -> this.tickHalt(level);
            case THRUST -> this.tickThrust(level, target);
            case TRAMPLE -> this.tickTrample(level);
            case HORN -> this.tickHorn(level, target);
            case VOLLEY -> this.tickVolley(level, target);
            case SMASH -> this.tickSmash(level, target);
            case WHIRL -> this.tickWhirl(level, target);
            case BASH -> this.tickBash(level, target);
            case WALL -> this.tickWall(level, target);
            default -> { }
        }
        if (this.attack != Attack.NONE && this.attackTick >= this.attack.duration) {
            if (this.attack == Attack.CHARGE) {
                this.begin(Attack.HALT);
                return;
            }
            this.begin(Attack.NONE);
            this.cooldown = this.isMounted() ? 18 + this.random.nextInt(16) : 14 + this.random.nextInt(14);
        }
    }

    /** Gehört er (noch) zu seinem Banner? Ist dort inzwischen ein anderer Ritter gebunden, verschwindet dieser hier. */
    private boolean strayed(ServerLevel level) {
        if (this.banner == null || !level.isLoaded(this.banner)) {
            return false;
        }
        if (level.getBlockEntity(this.banner) instanceof RavenBannerBlockEntity raven) {
            if (!raven.owns(this)) {
                level.sendParticles(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY(0.5), this.getZ(), 30, 0.6, 1.2, 0.6, 0.02);
                this.discard();
                return true;
            }
            return false;
        }
        this.banner = null;
        if (this.attack == Attack.GUARD) {
            this.begin(Attack.NONE);
        }
        return false;
    }

    /** Wen seine Angriffe treffen: alle Lebewesen außer ihm, seinen Leuten und Spielern im Kreativ-/Zuschauermodus. */
    public boolean canStrike(LivingEntity entity) {
        return entity != this && entity.isAlive() && !(entity instanceof RobberKnightEntity) && !(entity instanceof ArmorStand)
                && !this.men.contains(entity.getUUID()) && !(entity instanceof FarmHorse)
                && !(entity instanceof Player player && (player.isCreative() || player.isSpectator()));
    }

    private List<LivingEntity> victims(ServerLevel level, AABB area) {
        return level.getEntitiesOfClass(LivingEntity.class, area, e -> this.canStrike(e)
                && e.distanceToSqr(this.getX(), e.getY(), this.getZ()) <= this.confine * this.confine);
    }

    /** Für Tests: Seine Angriffe treffen nur, wer so nah (waagerecht) bei ihm ist – sonst träfen sie die Nachbartests. */
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

    /** Sein Platz im Hof (sonst wo er steht). */
    public Vec3 center() {
        return this.home != null ? Vec3.atBottomCenterOf(this.home) : this.position();
    }

    public Vec3 forward() {
        float yaw = this.getYRot() * Mth.DEG_TO_RAD;
        return new Vec3(-Mth.sin(yaw), 0.0, Mth.cos(yaw));
    }

    private Vec3 toward(Entity target) {
        Vec3 dir = target.position().subtract(this.position()).multiply(1.0, 0.0, 1.0);
        return dir.lengthSqr() < 1.0e-4 ? this.forward() : dir.normalize();
    }

    private void face(Vec3 dir) {
        float yaw = (float) (Mth.atan2(dir.z, dir.x) * Mth.RAD_TO_DEG) - 90.0f;
        this.setYRot(yaw);
        this.yBodyRot = yaw;
        this.yHeadRot = yaw;
    }

    private void sound(ServerLevel level, SoundEvent sound, float volume, float pitch) {
        level.playSound(null, this.getX(), this.getY(), this.getZ(), sound, SoundSource.HOSTILE, volume, pitch);
    }

    /** Zu Ross hält er Abstand (für den Lanzengang), zu Fuß kommt er heran. */
    private void approach(LivingEntity target) {
        double distance = this.distanceTo(target);
        double want = this.isMounted() ? 6.0 : 3.0;
        if (distance > want + 0.6) {
            this.getNavigation().moveTo(target, this.isMounted() ? 1.15 : 1.0);
        } else {
            this.getNavigation().stop();
        }
    }

    /** Aus der Burg gelockt: Er reitet (zu Fuß: geht) zurück in seinen Hof – in einer Staubwolke. */
    private void keepInCastle(ServerLevel level) {
        if (this.home == null) {
            return;
        }
        Vec3 c = this.center();
        double dx = this.getX() - c.x;
        double dz = this.getZ() - c.z;
        if (dx * dx + dz * dz < ARENA * ARENA && this.getY() > c.y - 10.0 && this.getY() < c.y + 16.0) {
            return;
        }
        this.vanishTo(level, c);
    }

    private void vanishTo(ServerLevel level, Vec3 to) {
        level.sendParticles(ParticleTypes.CLOUD, this.getX(), this.getY(0.4), this.getZ(), 30, 0.8, 0.8, 0.8, 0.02);
        this.teleportTo(to.x, to.y, to.z);
        this.setDeltaMovement(Vec3.ZERO);
        this.getNavigation().stop();
        level.sendParticles(ParticleTypes.CLOUD, to.x, to.y + 1.0, to.z, 30, 0.8, 0.8, 0.8, 0.02);
    }

    /** Ohne Gegner: zurück unter das Banner; nach einer Weile hält er wieder Hof. */
    private void idle(ServerLevel level) {
        if (this.attack != Attack.NONE) {
            this.begin(Attack.NONE);
        }
        this.idleTicks++;
        if (this.home != null && this.distanceToSqr(Vec3.atBottomCenterOf(this.home)) > 2.0) {
            if (this.idleTicks % 20 == 1) {
                this.getNavigation().moveTo(this.home.getX() + 0.5, this.home.getY(), this.home.getZ() + 0.5, 0.9);
            }
            if (this.idleTicks > 160 && this.idleTicks % 80 == 0) {
                this.vanishTo(level, Vec3.atBottomCenterOf(this.home));
            }
        }
        if (this.home != null && this.banner != null && this.idleTicks > RETURN_TICKS
                && this.distanceToSqr(Vec3.atBottomCenterOf(this.home)) < 2.5) {
            this.returnToBanner(level);
        }
    }

    /** Er hält wieder Hof – geheilt und, falls er sein Ross verlor, wieder im Sattel (seine Knechte haben es eingefangen). */
    private void returnToBanner(ServerLevel level) {
        if (this.banner == null || !(level.getBlockEntity(this.banner) instanceof RavenBannerBlockEntity raven) || !raven.owns(this)) {
            return;
        }
        this.setHealth(this.getMaxHealth());
        this.setMounted(true);
        this.idleTicks = 0;
        this.faceHome();
        this.begin(Attack.GUARD);
        raven.onKnightReturned(level, this);
    }

    /** Er steht an seinem Platz und blickt zum Tor. */
    public void faceHome() {
        if (this.home == null) {
            return;
        }
        this.snapTo(this.home.getX() + 0.5, this.home.getY(), this.home.getZ() + 0.5, this.homeYaw, 0.0f);
        this.yBodyRot = this.homeYaw;
        this.yHeadRot = this.homeYaw;
    }

    private Attack chooseAttack(ServerLevel level, LivingEntity target) {
        double distance = this.distanceTo(target);
        List<Attack> options = new ArrayList<>();
        boolean horn = level.getGameTime() - this.lastHorn > HORN_PAUSE && this.menAlive(level) < MEN_MAX;
        if (this.isMounted()) {
            if (distance < 5.5) {
                options.addAll(List.of(Attack.THRUST, Attack.THRUST, Attack.TRAMPLE, Attack.TRAMPLE, Attack.CHARGE));
            } else if (distance < 16.0) {
                options.addAll(List.of(Attack.CHARGE, Attack.CHARGE, Attack.CHARGE, Attack.VOLLEY));
                if (distance < 7.0) {
                    options.add(Attack.THRUST);
                }
            } else {
                options.addAll(List.of(Attack.CHARGE, Attack.CHARGE, Attack.VOLLEY, Attack.VOLLEY));
            }
        } else {
            if (distance < 4.2) {
                options.addAll(List.of(Attack.SMASH, Attack.SMASH, Attack.WHIRL, Attack.WHIRL, Attack.BASH, Attack.WALL));
            } else if (distance < 10.0) {
                options.addAll(List.of(Attack.BASH, Attack.WALL, Attack.WALL, Attack.VOLLEY));
            } else {
                options.addAll(List.of(Attack.VOLLEY, Attack.VOLLEY, Attack.WALL));
            }
        }
        if (horn) {
            options.add(Attack.HORN);
        }
        options.removeIf(a -> a == this.lastAttack);
        if (options.isEmpty()) {
            options.add(this.isMounted() ? Attack.CHARGE : Attack.SMASH);
        }
        Attack next = options.get(this.random.nextInt(options.size()));
        if (next == Attack.CHARGE) {
            this.chargeDir = this.toward(target);
            this.chargeStart = this.position();
        }
        return next;
    }

    // ------------------------------------------------------------------ Hof halten und Erwachen

    /** Er hält Hof: Das Ross steht, schnaubt und scharrt; dann und wann flattert der Wimpel. */
    private void tickGuard(ServerLevel level) {
        int t = ++this.attackTick;
        this.setDeltaMovement(0.0, this.getDeltaMovement().y, 0.0);
        if (t % 160 == 40) {
            this.sound(level, SoundEvents.HORSE_BREATHE, 1.2f, 0.8f + this.random.nextFloat() * 0.15f);
        }
        if (t % 220 == 120) {
            this.sound(level, SoundEvents.HORSE_AMBIENT, 1.2f, 0.75f);
        }
    }

    /** Das Ross steigt, er reckt die Lanze empor, das Ross schlägt auf – dann senkt er die Lanze ein. */
    private void tickAwaken(ServerLevel level) {
        int t = ++this.attackTick;
        Player nearest = level.getNearestPlayer(this, 40.0);
        if (nearest != null && t > 6) {
            this.getLookControl().setLookAt(nearest, 20.0f, 30.0f);
            this.lookAt(nearest, 12.0f, 30.0f);
        }
        if (t == 4) {
            this.sound(level, SoundEvents.HORSE_ANGRY, 2.6f, 0.75f);
        }
        if (t == 20) {
            this.sound(level, ModSounds.ROBBER_KNIGHT_AWAKEN.get(), 3.0f, 1.0f);
        }
        if (t == 44) {
            this.sound(level, ModSounds.ROBBER_KNIGHT_STOMP.get(), 2.4f, 1.0f);
            this.dust(level, this.position().add(this.forward().scale(1.2)), 24, 1.2);
            Component title = Component.translatable("boss.kingdomsatwar.robber_knight").withColor(GOLD);
            Component subtitle = Component.translatable("boss.kingdomsatwar.robber_knight.title").withStyle(ChatFormatting.GRAY);
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
            this.cooldown = 10;
            this.idleTicks = 0;
        }
    }

    // ------------------------------------------------------------------ zu Ross

    /** Staub und Erdbrocken vom Boden, wo das Ross auftritt. */
    private void dust(ServerLevel level, Vec3 at, int count, double spread) {
        BlockPos below = BlockPos.containing(at.x, at.y - 0.5, at.z);
        BlockState ground = level.getBlockState(below);
        if (!ground.isAir()) {
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, ground), at.x, at.y + 0.1, at.z, count, spread, 0.1, spread, 0.15);
        }
        level.sendParticles(ParticleTypes.CLOUD, at.x, at.y + 0.2, at.z, count / 3, spread, 0.1, spread, 0.02);
    }

    /**
     * Lanzengang: Er wendet (steht man ihm zu nah, reitet er erst ein Stück fort), legt die Lanze ein und sprengt in gerader
     * Linie los – höchstens {@value #CHARGE_DISTANCE} Blöcke; an einer Mauer hält er an. Wen die Lanze trifft, der fliegt; wer
     * den Stoß mit einem Schild auffängt, an dem bricht die Lanze, und er wankt im Sattel.
     */
    private void tickCharge(ServerLevel level, LivingEntity target) {
        int t = this.attackTick;
        if (t < CHARGE_AIM) {
            Vec3 to = target.position().subtract(this.position()).multiply(1.0, 0.0, 1.0);
            if (to.length() < 9.0 && t < CHARGE_AIM - 4) {
                // zu nah: erst fort, um Anlauf zu nehmen
                Vec3 away = to.lengthSqr() < 1.0e-4 ? this.forward().scale(-1.0) : to.normalize().scale(-1.0);
                this.face(away);
                this.setDeltaMovement(away.x * 0.42, this.getDeltaMovement().y, away.z * 0.42);
            } else {
                this.face(this.toward(target));
                this.setDeltaMovement(0.0, this.getDeltaMovement().y, 0.0);
            }
            if (t == 4) {
                this.sound(level, SoundEvents.HORSE_ANGRY, 2.0f, 0.85f);
            }
            if (t == CHARGE_AIM - 1) {
                this.chargeDir = this.toward(target);
                this.chargeStart = this.position();
                this.face(this.chargeDir);
                this.sound(level, ModSounds.ROBBER_KNIGHT_CHARGE.get(), 2.8f, 1.0f);
            }
            return;
        }
        // er sprengt heran
        this.face(this.chargeDir);
        this.setDeltaMovement(this.chargeDir.x * CHARGE_SPEED, this.getDeltaMovement().y, this.chargeDir.z * CHARGE_SPEED);
        if (t % 3 == 0) {
            this.sound(level, SoundEvents.HORSE_GALLOP, 1.6f, 0.8f + this.random.nextFloat() * 0.1f);
            this.dust(level, this.position(), 8, 0.6);
        }
        Vec3 tip = this.position().add(this.chargeDir.scale(CHARGE_REACH)).add(0.0, 1.6, 0.0);
        AABB lance = new AABB(tip, tip).inflate(1.0, 1.4, 1.0).expandTowards(this.chargeDir.scale(-1.6));
        AABB horse = this.getBoundingBox().inflate(0.3, 0.0, 0.3);
        for (LivingEntity victim : this.victims(level, lance.minmax(horse))) {
            if (!this.struck.add(victim.getUUID())) {
                continue;
            }
            if (victim instanceof Player player && this.blocksWithShield(player)) {
                this.lanceBreaks(level, player);
                return;
            }
            if (victim.hurtServer(level, this.damageSources().mobAttack(this), CHARGE_DAMAGE)) {
                victim.push(this.chargeDir.x * 1.8, 0.6, this.chargeDir.z * 1.8);
                victim.needsSync = true;
                this.sound(level, ModSounds.ROBBER_KNIGHT_LANCE_HIT.get(), 2.0f, 1.0f);
            }
        }
        boolean far = this.position().subtract(this.chargeStart).horizontalDistance() > CHARGE_DISTANCE;
        if (this.horizontalCollision || far) {
            if (this.horizontalCollision) {
                this.sound(level, ModSounds.ROBBER_KNIGHT_STOMP.get(), 1.8f, 0.8f);
                this.dust(level, this.position().add(this.chargeDir.scale(1.0)), 16, 0.8);
            }
            this.begin(Attack.HALT);
        }
    }

    /** Fängt er den Ansturm mit einem Schild auf (erhoben, ihm zugewandt)? */
    private boolean blocksWithShield(Player player) {
        if (!player.isBlocking()) {
            return false;
        }
        Vec3 look = player.getViewVector(1.0f).multiply(1.0, 0.0, 1.0);
        Vec3 toMe = this.position().subtract(player.position()).multiply(1.0, 0.0, 1.0);
        return look.lengthSqr() > 1.0e-4 && toMe.lengthSqr() > 1.0e-4 && look.normalize().dot(toMe.normalize()) > 0.3;
    }

    /** Die Lanze bricht am Schild: Splitter, ein Krachen – er wankt im Sattel; der Schild hat gelitten und taugt kurz nichts. */
    private void lanceBreaks(ServerLevel level, Player player) {
        ItemStack shield = player.getItemBlockingWith();
        if (shield != null) {
            shield.hurtAndBreak(12, player, player.getUsedItemHand());
            BlocksAttacks blocks = shield.get(net.minecraft.core.component.DataComponents.BLOCKS_ATTACKS);
            if (blocks != null) {
                blocks.disable(level, player, 3.0f, shield);
            }
        }
        player.push(this.chargeDir.x * 0.9, 0.25, this.chargeDir.z * 0.9);
        player.needsSync = true;
        Vec3 tip = this.position().add(this.chargeDir.scale(CHARGE_REACH * 0.8)).add(0.0, 1.7, 0.0);
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, net.minecraft.world.level.block.Blocks.DARK_OAK_PLANKS.defaultBlockState()),
                tip.x, tip.y, tip.z, 40, 0.5, 0.4, 0.5, 0.2);
        level.sendParticles(new DustParticleOptions(GOLD, 1.2f), tip.x, tip.y, tip.z, 20, 0.5, 0.4, 0.5, 0.0);
        this.sound(level, ModSounds.ROBBER_KNIGHT_LANCE_BREAK.get(), 2.6f, 1.0f);
        this.entityData.set(DATA_LANCE, false);
        this.setDeltaMovement(this.chargeDir.x * -0.2, 0.0, this.chargeDir.z * -0.2);
        this.begin(Attack.STAGGER);
    }

    /** Er hält an: Das Ross steigt kurz und schlägt auf, dann wendet er. */
    private void tickHalt(ServerLevel level) {
        int t = this.attackTick;
        double slow = Math.max(0.0, 1.0 - t / 8.0);
        this.setDeltaMovement(this.chargeDir.x * CHARGE_SPEED * slow * 0.6, this.getDeltaMovement().y, this.chargeDir.z * CHARGE_SPEED * slow * 0.6);
        if (t == 2) {
            this.sound(level, SoundEvents.HORSE_ANGRY, 1.8f, 0.9f);
        }
        if (t == 20) {
            this.dust(level, this.position().add(this.forward().scale(1.2)), 12, 0.8);
        }
    }

    /** Lanzenstich: zurückziehen, dann aus dem Sattel nach vorn. */
    private void tickThrust(ServerLevel level, LivingEntity target) {
        int t = this.attackTick;
        if (t == 6) {
            this.sound(level, ModSounds.ROBBER_KNIGHT_SWING.get(), 1.6f, 1.2f);
        }
        if (t == THRUST_HIT) {
            Vec3 dir = this.toward(target);
            double cos = Math.cos(Math.toRadians(28.0));
            for (LivingEntity victim : this.victims(level, this.getBoundingBox().inflate(THRUST_RANGE, 2.0, THRUST_RANGE))) {
                Vec3 to = victim.position().subtract(this.position()).multiply(1.0, 0.0, 1.0);
                double d = to.length();
                if (d > THRUST_RANGE + victim.getBbWidth() * 0.5 || d > 1.0 && to.normalize().dot(dir) < cos) {
                    continue;
                }
                if (this.struck.add(victim.getUUID()) && victim.hurtServer(level, this.damageSources().mobAttack(this), THRUST_DAMAGE)) {
                    victim.push(dir.x * 0.9, 0.3, dir.z * 0.9);
                    victim.needsSync = true;
                    this.sound(level, ModSounds.ROBBER_KNIGHT_LANCE_HIT.get(), 1.6f, 1.15f);
                }
            }
        }
    }

    /** Steigen: Das Ross steigt und schlägt mit der Vorderhand auf – wer in der Nähe am Boden steht, wird getroffen. */
    private void tickTrample(ServerLevel level) {
        int t = this.attackTick;
        if (t == 6) {
            this.sound(level, SoundEvents.HORSE_ANGRY, 2.4f, 0.8f);
        }
        if (t == TRAMPLE_HIT) {
            this.sound(level, ModSounds.ROBBER_KNIGHT_STOMP.get(), 2.6f, 0.9f);
            Vec3 at = this.position().add(this.forward().scale(1.2));
            this.dust(level, at, 40, 1.6);
            SpellFxEntity.create(level, SpellFxEntity.Kind.SHOCKWAVE, this.position().add(0.0, 0.05, 0.0), null, GOLD, 14, 1.0f).param(0.45f).spawn();
            for (LivingEntity victim : this.victims(level, this.getBoundingBox().inflate(TRAMPLE_RADIUS, 1.5, TRAMPLE_RADIUS))) {
                double d = Math.sqrt(victim.distanceToSqr(this.getX(), victim.getY(), this.getZ()));
                boolean grounded = victim.onGround() && victim.getY() - this.getY() < 1.2;
                if (d > TRAMPLE_RADIUS + victim.getBbWidth() * 0.5 || !grounded) {
                    continue;
                }
                if (victim.hurtServer(level, this.damageSources().mobAttack(this), TRAMPLE_DAMAGE)) {
                    Vec3 out = victim.position().subtract(this.position()).multiply(1.0, 0.0, 1.0);
                    out = out.lengthSqr() < 1.0e-4 ? this.forward() : out.normalize();
                    victim.push(out.x * 0.9, 0.55, out.z * 0.9);
                    victim.needsSync = true;
                }
            }
        }
    }

    /** Hornruf: Er bläst ins Horn – seine Leute kommen gelaufen (Diener mit Äxten, Armbrustschützen). */
    private void tickHorn(ServerLevel level, LivingEntity target) {
        int t = this.attackTick;
        if (t == 10) {
            this.lastHorn = level.getGameTime();
            this.sound(level, ModSounds.ROBBER_KNIGHT_HORN.get(), 4.0f, 1.0f);
        }
        if (t == HORN_CALL) {
            int room = MEN_MAX - this.menAlive(level);
            int count = Math.min(2, room);
            for (int i = 0; i < count; i++) {
                EntityType<? extends Mob> type = i % 2 == 0 ? ModEntities.VINDICATOR.get() : ModEntities.PILLAGER.get();
                this.summonMan(level, type, target);
            }
        }
    }

    private void summonMan(ServerLevel level, EntityType<? extends Mob> type, LivingEntity target) {
        for (int attempt = 0; attempt < 10; attempt++) {
            double a = this.random.nextDouble() * Math.PI * 2.0;
            double r = 5.0 + this.random.nextDouble() * 4.0;
            Vec3 at = this.position().add(Math.cos(a) * r, 0.0, Math.sin(a) * r);
            BlockPos pos = BlockPos.containing(at);
            for (int dy = 2; dy >= -3; dy--) {
                BlockPos p = pos.above(dy);
                if (level.getBlockState(p.below()).isFaceSturdy(level, p.below(), net.minecraft.core.Direction.UP)
                        && level.getBlockState(p).getCollisionShape(level, p).isEmpty()
                        && level.getBlockState(p.above()).getCollisionShape(level, p.above()).isEmpty()) {
                    Mob man = type.create(level, EntitySpawnReason.MOB_SUMMONED);
                    if (man == null) {
                        return;
                    }
                    man.snapTo(p.getX() + 0.5, p.getY(), p.getZ() + 0.5, this.random.nextFloat() * 360.0f, 0.0f);
                    man.finalizeSpawn(level, level.getCurrentDifficultyAt(p), EntitySpawnReason.MOB_SUMMONED, null);
                    man.setTarget(target);
                    man.addTag("kaw_robber_men");
                    level.addFreshEntityWithPassengers(man);
                    this.men.add(man.getUUID());
                    level.sendParticles(ParticleTypes.POOF, man.getX(), man.getY(0.5), man.getZ(), 16, 0.3, 0.6, 0.3, 0.02);
                    return;
                }
            }
        }
    }

    /**
     * Bolzenhagel: Er weist mit der Lanze (zu Fuß: dem Morgenstern) auf sein Ziel; rote Zeichen erscheinen am Boden, dann
     * hageln dort die Bolzen seiner Armbrustschützen nieder.
     */
    private void tickVolley(ServerLevel level, LivingEntity target) {
        int t = this.attackTick;
        if (t == VOLLEY_MARK) {
            this.marks.clear();
            Vec3 c = target.position();
            this.marks.add(c);
            for (int i = 1; i < VOLLEY_SPOTS; i++) {
                double a = i * Math.PI * 2.0 / (VOLLEY_SPOTS - 1) + this.random.nextDouble() * 0.6;
                double r = 2.2 + this.random.nextDouble() * 1.4;
                this.marks.add(c.add(Math.cos(a) * r, 0.0, Math.sin(a) * r));
            }
            this.sound(level, SoundEvents.CROSSBOW_LOADING_END.value(), 2.4f, 0.8f);
        }
        if (t > VOLLEY_MARK && t < VOLLEY_FIRE && t % 2 == 0) {
            for (Vec3 m : this.marks) {
                for (int i = 0; i < 6; i++) {
                    double a = i * Math.PI / 3.0 + t * 0.2;
                    level.sendParticles(new DustParticleOptions(RED, 1.1f), m.x + Math.cos(a) * VOLLEY_RADIUS * 0.8, m.y + 0.15,
                            m.z + Math.sin(a) * VOLLEY_RADIUS * 0.8, 1, 0.0, 0.0, 0.0, 0.0);
                }
            }
        }
        if (t == VOLLEY_FIRE - 6) {
            this.sound(level, SoundEvents.CROSSBOW_SHOOT, 3.0f, 0.7f);
        }
        if (t == VOLLEY_FIRE) {
            for (Vec3 m : this.marks) {
                Arrow bolt = new Arrow(level, m.x + (this.random.nextDouble() - 0.5) * 0.4, m.y + 14.0, m.z + (this.random.nextDouble() - 0.5) * 0.4,
                        new ItemStack(Items.ARROW), null);
                bolt.setOwner(this);
                bolt.pickup = AbstractArrow.Pickup.DISALLOWED;
                bolt.setBaseDamage(0.0);
                bolt.setDeltaMovement(0.0, -2.6, 0.0);
                level.addFreshEntity(bolt);
                this.bolts.add(bolt);
            }
        }
        if (t == VOLLEY_FIRE + 5) {
            // die Bolzen schlagen ein: wer im Zeichen steht, wird getroffen (einmal je Zeichen)
            for (Vec3 m : this.marks) {
                level.sendParticles(ParticleTypes.CRIT, m.x, m.y + 0.3, m.z, 12, 0.4, 0.2, 0.4, 0.2);
                for (LivingEntity victim : this.victims(level, new AABB(m, m).inflate(VOLLEY_RADIUS + 0.5, 2.5, VOLLEY_RADIUS + 0.5))) {
                    double d = Math.sqrt(victim.distanceToSqr(m.x, victim.getY(), m.z));
                    if (d <= VOLLEY_RADIUS + victim.getBbWidth() * 0.5 && this.struck.add(victim.getUUID())) {
                        victim.hurtServer(level, this.damageSources().arrow(this.bolts.isEmpty() ? null : this.bolts.getLast(), this),
                                VOLLEY_DAMAGE);
                    }
                }
            }
            this.marks.clear();
        }
    }

    /** Die Bolzen des Hagels verschwinden bald wieder (sie stecken sonst überall im Hof). */
    private void tickBolts() {
        Iterator<AbstractArrow> it = this.bolts.iterator();
        while (it.hasNext()) {
            AbstractArrow bolt = it.next();
            if (bolt.isRemoved() || bolt.tickCount > 60) {
                bolt.discard();
                it.remove();
            }
        }
    }

    /** Die Lanze ist am Schild gebrochen: Er wankt im Sattel; danach reicht ihm sein Knappe eine neue (er hat sie wieder). */
    private void tickStagger(ServerLevel level) {
        int t = ++this.attackTick;
        this.getNavigation().stop();
        this.setDeltaMovement(this.getDeltaMovement().multiply(0.6, 1.0, 0.6));
        if (t == 4) {
            this.sound(level, SoundEvents.HORSE_ANGRY, 2.0f, 1.1f);
        }
        if (t % 8 == 0) {
            level.sendParticles(ParticleTypes.CRIT, this.getX(), this.getY() + 3.0, this.getZ(), 4, 0.4, 0.2, 0.4, 0.05);
        }
        if (t >= STAGGER_TICKS) {
            this.entityData.set(DATA_LANCE, true);
            this.begin(Attack.NONE);
            this.cooldown = 10;
        }
    }

    /**
     * Unter halber Kraft wirft ihn das Ross ab: Es steigt, er stürzt rücklings in den Staub (unverwundbar), das Ross flieht;
     * er rafft sich auf und zieht den Morgenstern.
     */
    private void tickUnhorse(ServerLevel level) {
        int t = ++this.attackTick;
        this.getNavigation().stop();
        this.setDeltaMovement(0.0, this.getDeltaMovement().y, 0.0);
        if (t == 4) {
            this.sound(level, SoundEvents.HORSE_ANGRY, 3.0f, 0.7f);
        }
        if (t == UNHORSE_FALL) {
            this.sound(level, ModSounds.ROBBER_KNIGHT_FALL.get(), 2.6f, 1.0f);
            this.dust(level, this.position().subtract(this.forward().scale(1.2)), 40, 1.4);
            Vec3 back = this.forward().scale(-1.0);
            this.setMounted(false);
            this.spawnFleeingHorse(level, back);
        }
        if (t == UNHORSE_MACE) {
            this.sound(level, ModSounds.ROBBER_KNIGHT_SWING.get(), 1.8f, 0.8f);
        }
        if (t >= Attack.UNHORSE.duration) {
            this.begin(Attack.NONE);
            this.cooldown = 8;
        }
    }

    /** Sein Rappe flieht (gesattelt, aber herrenlos: wer will, kann ihn zähmen). */
    private void spawnFleeingHorse(ServerLevel level, Vec3 back) {
        Warhorse horse = ModEntities.WARHORSE.get().create(level, EntitySpawnReason.EVENT);
        if (horse == null) {
            return;
        }
        horse.snapTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), 0.0f);
        horse.setBreed(FarmHorse.Breed.FRIESE);
        horse.setItemSlot(EquipmentSlot.SADDLE, new ItemStack(Items.SADDLE));
        horse.setDropChance(EquipmentSlot.SADDLE, 1.0f);
        horse.setPersistenceRequired();
        horse.addTag("kaw_robber_horse");
        level.addFreshEntity(horse);
        Vec3 away = this.position().add(back.scale(18.0));
        horse.getNavigation().moveTo(away.x, away.y, away.z, 2.2);
        level.playSound(null, horse.getX(), horse.getY(), horse.getZ(), SoundEvents.HORSE_GALLOP, SoundSource.NEUTRAL, 1.6f, 1.0f);
    }

    // ------------------------------------------------------------------ zu Fuß

    /** Ein Schlag in einem Bogen vor ihm (bei {@code arc} = π rundum); Rückstoß von ihm fort. */
    private void strike(ServerLevel level, Vec3 dir, double range, double arc, float damage, double push) {
        double cos = Math.cos(arc);
        for (LivingEntity victim : this.victims(level, this.getBoundingBox().inflate(range + 1.0, 2.0, range + 1.0))) {
            Vec3 to = victim.position().subtract(this.position()).multiply(1.0, 0.0, 1.0);
            double distance = to.length();
            if (distance > range + victim.getBbWidth() * 0.5 || distance > 0.8 && to.normalize().dot(dir) < cos) {
                continue;
            }
            if (this.struck.add(victim.getUUID()) && victim.hurtServer(level, this.damageSources().mobAttack(this), damage)) {
                Vec3 out = distance < 1.0e-3 ? dir : to.normalize();
                victim.push(out.x * push, 0.3 + push * 0.15, out.z * push);
                victim.needsSync = true;
            }
        }
    }

    /** Morgensternschlag: hoch über den Kopf, dann vor sich auf den Boden – der Boden reißt in einer Linie auf. */
    private void tickSmash(ServerLevel level, LivingEntity target) {
        int t = this.attackTick;
        if (t == 8) {
            this.sound(level, ModSounds.ROBBER_KNIGHT_SWING.get(), 1.8f, 0.8f);
        }
        if (t == SMASH_HIT) {
            Vec3 dir = this.toward(target);
            this.sound(level, ModSounds.ROBBER_KNIGHT_SMASH.get(), 2.6f, 1.0f);
            for (int i = 1; i <= 5; i++) {
                this.dust(level, this.position().add(dir.scale(i * 0.9)), 8, 0.3);
            }
            this.strike(level, dir, SMASH_RANGE, Math.toRadians(50.0), SMASH_DAMAGE, 0.7);
            for (LivingEntity victim : this.victims(level, this.getBoundingBox().inflate(SMASH_RANGE + 1.0, 2.0, SMASH_RANGE + 1.0))) {
                if (this.struck.contains(victim.getUUID())) {
                    victim.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 1), this);
                }
            }
        }
    }

    /** Wirbel: Er dreht sich zweimal mit ausgestrecktem Morgenstern (und rückt dabei langsam vor) – wer zu nah ist, wird getroffen. */
    private void tickWhirl(ServerLevel level, LivingEntity target) {
        int t = this.attackTick;
        if (t >= WHIRL_FROM && t <= WHIRL_TO) {
            float spin = 720.0f / (WHIRL_TO - WHIRL_FROM);
            float yaw = this.getYRot() + spin;
            this.setYRot(yaw);
            this.yBodyRot = yaw;
            this.yHeadRot = yaw;
            Vec3 to = this.toward(target);
            this.setDeltaMovement(to.x * 0.08, this.getDeltaMovement().y, to.z * 0.08);
            if ((t - WHIRL_FROM) % 8 == 0) {
                this.sound(level, ModSounds.ROBBER_KNIGHT_WHIRL.get(), 1.8f, 0.9f + (t - WHIRL_FROM) * 0.01f);
            }
        }
        for (int hit : WHIRL_HITS) {
            if (t == hit) {
                this.struck.clear();
                this.strike(level, this.forward(), WHIRL_RADIUS, Math.PI, WHIRL_DAMAGE, 0.9);
                level.sendParticles(ParticleTypes.SWEEP_ATTACK, this.getX(), this.getY(0.45), this.getZ(), 6, 1.6, 0.1, 1.6, 0.0);
            }
        }
        if (t == WHIRL_TO + 1) {
            this.lookAt(target, 360.0f, 30.0f);
        }
    }

    /** Schildstoß: geduckt hinter dem Schild, dann ein Satz nach vorn – wer getroffen wird, fliegt; ein Schild taugt danach kurz nichts. */
    private void tickBash(ServerLevel level, LivingEntity target) {
        int t = this.attackTick;
        Vec3 dir = this.toward(target);
        if (t == 6) {
            this.sound(level, ModSounds.ROBBER_KNIGHT_STEP.get(), 1.6f, 0.8f);
        }
        if (t >= BASH_HIT - 2 && t <= BASH_HIT + 3) {
            this.setDeltaMovement(dir.x * 0.6, this.getDeltaMovement().y, dir.z * 0.6);
        }
        if (t >= BASH_HIT && t <= BASH_HIT + 3) {
            for (LivingEntity victim : this.victims(level, this.getBoundingBox().inflate(BASH_RANGE, 1.0, BASH_RANGE))) {
                Vec3 to = victim.position().subtract(this.position()).multiply(1.0, 0.0, 1.0);
                if (to.length() > BASH_RANGE + victim.getBbWidth() * 0.5 || to.lengthSqr() > 0.5 && to.normalize().dot(dir) < 0.3
                        || !this.struck.add(victim.getUUID())) {
                    continue;
                }
                if (victim instanceof Player player && player.isBlocking()) {
                    ItemStack shield = player.getItemBlockingWith();
                    BlocksAttacks blocks = shield == null ? null : shield.get(net.minecraft.core.component.DataComponents.BLOCKS_ATTACKS);
                    if (blocks != null) {
                        blocks.disable(level, player, 5.0f, shield);
                    }
                }
                this.sound(level, ModSounds.ROBBER_KNIGHT_BASH.get(), 2.2f, 1.0f);
                if (victim.hurtServer(level, this.damageSources().mobAttack(this), BASH_DAMAGE)) {
                    victim.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 1), this);
                }
                victim.push(dir.x * 1.8, 0.5, dir.z * 1.8);
                victim.needsSync = true;
            }
        }
    }

    /** Schildwall: Der Schild hoch vor dem Leib – von vorn trifft ihn nichts; er rückt langsam vor. */
    private void tickWall(ServerLevel level, LivingEntity target) {
        int t = this.attackTick;
        if (t == WALL_FROM) {
            this.sound(level, ModSounds.ROBBER_KNIGHT_STEP.get(), 1.8f, 0.7f);
        }
        if (t > WALL_FROM && t < WALL_TO && this.distanceTo(target) > 2.6) {
            this.getNavigation().moveTo(target, 0.55);
        } else {
            this.getNavigation().stop();
        }
    }

    // ------------------------------------------------------------------ Tod, Speichern, Klänge

    @Override
    public void die(DamageSource source) {
        super.die(source);
        this.marks.clear();
        if (this.level() instanceof ServerLevel level && this.banner != null
                && level.getBlockEntity(this.banner) instanceof RavenBannerBlockEntity raven && raven.owns(this)) {
            raven.onKnightDefeated(level);
        }
    }

    /** Er sinkt in die Knie und fällt; dann bleibt nur ein Häuflein Staub (und seine Beute). */
    @Override
    protected void tickDeath() {
        this.deathTime++;
        if (!(this.level() instanceof ServerLevel level)) {
            return;
        }
        if (this.deathTime == 20) {
            level.playSound(null, this.getX(), this.getY(), this.getZ(), ModSounds.ROBBER_KNIGHT_FALL.get(), SoundSource.HOSTILE, 2.0f, 0.8f);
        }
        if (this.deathTime == DEATH_TICKS - 2) {
            level.sendParticles(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY(0.4), this.getZ(), 40, 0.6, 0.6, 0.6, 0.02);
            level.sendParticles(new DustParticleOptions(GOLD, 1.4f), this.getX(), this.getY(0.4), this.getZ(), 40, 0.6, 0.6, 0.6, 0.0);
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
        for (AbstractArrow bolt : this.bolts) {
            bolt.discard();
        }
        this.bolts.clear();
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        if (this.home != null) {
            output.putLong("Home", this.home.asLong());
        }
        if (this.banner != null) {
            output.putLong("Banner", this.banner.asLong());
        }
        output.putFloat("HomeYaw", this.homeYaw);
        output.putBoolean("Mounted", this.isMounted());
        output.putBoolean("HoldingCourt", this.attack == Attack.GUARD);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        input.getLong("Home").ifPresent(h -> this.home = BlockPos.of(h));
        input.getLong("Banner").ifPresent(b -> this.banner = BlockPos.of(b));
        this.homeYaw = input.getFloatOr("HomeYaw", 0.0f);
        this.setMounted(input.getBooleanOr("Mounted", true));
        if (input.getBooleanOr("HoldingCourt", false)) {
            this.begin(Attack.GUARD);
        }
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return this.isMounted() ? SoundEvents.HORSE_BREATHE : null;
    }

    @Override
    public int getAmbientSoundInterval() {
        return 200;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.ROBBER_KNIGHT_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.ROBBER_KNIGHT_DEATH.get();
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        if (this.isMounted()) {
            this.playSound(this.getDeltaMovement().horizontalDistanceSqr() > 0.04 ? SoundEvents.HORSE_GALLOP : SoundEvents.HORSE_STEP, 0.8f, 0.9f);
        } else {
            this.playSound(ModSounds.ROBBER_KNIGHT_STEP.get(), 0.7f, 0.9f + this.random.nextFloat() * 0.2f);
        }
    }

    @Override
    protected float getSoundVolume() {
        return 2.0f;
    }

    // ------------------------------------------------------------------ Himmelswacht

    @Override
    public SkyTheme skyTheme() {
        return SkyTheme.RAVEN;
    }
}
