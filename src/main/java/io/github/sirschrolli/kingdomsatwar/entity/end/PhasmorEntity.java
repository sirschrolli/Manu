package io.github.sirschrolli.kingdomsatwar.entity.end;

import io.github.sirschrolli.kingdomsatwar.block.end.VoidThroneBlockEntity;
import io.github.sirschrolli.kingdomsatwar.item.end.VoidwalkerArmor;
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
import net.minecraft.world.entity.projectile.throwableitemprojectile.AbstractThrownPotion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Phasmor, der Leerwandlerkönig (seit 1.5): der älteste der Leerwandler, der Herr der Phasensprünge. Im Phasenheiligtum
 * auf den äußeren Inseln des Ends kniet er vor dem Leerenthron und hält Wache – die Hände auf dem Knauf der Phasenklinge,
 * deren Spitze im Boden steckt. Wer näher als {@link #WAKE_RADIUS} Blöcke herankommt (oder ihn angreift), weckt ihn:
 * <ul>
 *     <li><b>Klingenhieb</b> – ein weiter Hieb mit der Phasenklinge, schräg von der Schulter hinab;</li>
 *     <li><b>Phasenstoß</b> – er zerfließt zu einem türkisen Nachbild und steht im nächsten Augenblick hinter seinem Opfer;</li>
 *     <li><b>Trugbilder</b> – drei Abbilder seiner selbst treten aus der Leere, und er mischt sich unter sie (die Trugbilder
 *     schimmern ein wenig durchsichtig und zerfallen beim ersten Treffer);</li>
 *     <li><b>Blockwurf</b> – Brocken aus Endstein und Purpur steigen um ihn auf und fliegen nacheinander auf seine Gegner;</li>
 *     <li><b>Leerenrisse</b> – er stößt die Klinge in den Boden, unter seinen Gegnern öffnen sich Risse in die Leere; wer
 *     darin steht, wird verschluckt und hoch über dem Heiligtum wieder ausgespuckt;</li>
 *     <li><b>Krönung</b> – unter halber Kraft brüllt er auf, die Krone aus Phasit lodert: Danach ist er schneller, ruft mehr
 *     Trugbilder, öffnet mehr Risse und kennt den <b>Phasensturm</b> (fünf Sprünge, fünf Hiebe).</li>
 * </ul>
 * Pfeile und andere Geschosse durchdringen ihn oft nicht – er phast zur Seite. Aber <b>Wasser</b> ist sein Fluch: Steht er
 * darin oder trifft ihn ein Wurftrank aus Wasser, krümmt er sich (er kann eine Weile weder springen noch ausweichen).
 * Findet er niemanden mehr, kehrt er vor den Thron zurück und kniet wieder (geheilt, ohne Krone).
 */
public class PhasmorEntity extends Monster implements PhasmorLike, io.github.sirschrolli.kingdomsatwar.entity.boss.sky.SkyWarden {
    public static final float MAX_HEALTH = 800.0f;
    public static final int AWAKEN_TICKS = 80;
    /** So lange (Ticks) dauert sein Sterben (er sinkt auf die Knie, die Krone erlischt, er zerfällt zu türkisem Staub). */
    public static final int DEATH_TICKS = 90;
    /** So nah (waagerecht) muss man ihm kommen, damit er erwacht. */
    public static final double WAKE_RADIUS = 14.0;
    /** Weiter als so weit von seinem Platz folgt er niemandem … */
    private static final double LEASH = 40.0;
    /** … und wer ihn aus dem Heiligtum lockt, dem entkommt er mit einem Sprung vor den Thron. */
    public static final double ARENA = 17.0;
    /** So lange ohne Gegner, dann kniet er wieder vor dem Thron. */
    private static final int RETURN_TICKS = 400;
    public static final float SLASH_DAMAGE = 11.0f;
    public static final double SLASH_RANGE = 5.2;
    public static final int SLASH_HIT = 15;
    public static final float BLINK_DAMAGE = 13.0f;
    public static final int BLINK_VANISH = 8;
    public static final int BLINK_APPEAR = 14;
    public static final int BLINK_HIT = 19;
    public static final int ILLUSION_SPAWN = 16;
    public static final int MAX_ILLUSIONS = 6;
    public static final float BOULDER_DAMAGE = 8.0f;
    public static final int HURL_LIFT = 8;
    public static final int[] HURL_THROWS = {26, 36, 46};
    public static final float RIFT_DAMAGE = 9.0f;
    public static final double RIFT_RADIUS = 1.8;
    public static final int RIFT_OPEN = 14;
    public static final int RIFT_ERUPT = 40;
    /** So hoch spuckt ein Riss aus, wen er verschluckt hat. */
    public static final double RIFT_LIFT = 7.0;
    public static final float STORM_DAMAGE = 8.0f;
    public static final int[] STORM_BLINKS = {8, 22, 36, 50, 64};
    public static final int CROWN_FLARE = 34;
    public static final float WATER_DAMAGE = 8.0f;
    public static final int STAGGER_TICKS = 50;
    /** So oft phast er einem Geschoss davon (nicht, solange er sich krümmt). */
    public static final float DODGE_CHANCE = 0.5f;
    /** Die Farbe seines Lichts: Phasit-Türkis. */
    public static final int PHASIT = 0x3FE6CC;

    public enum Attack {
        NONE(0), GUARD(0), AWAKEN(AWAKEN_TICKS), SLASH(32), BLINK(40), ILLUSIONS(44), HURL(64), RIFTS(56), STORM(84), CROWN(72),
        STAGGER(STAGGER_TICKS);

        public final int duration;

        Attack(int duration) {
            this.duration = duration;
        }
    }

    private static final EntityDataAccessor<Integer> DATA_ATTACK = SynchedEntityData.defineId(PhasmorEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_CROWNED = SynchedEntityData.defineId(PhasmorEntity.class, EntityDataSerializers.BOOLEAN);

    private final ServerBossEvent bossBar = new ServerBossEvent(UUID.randomUUID(), Component.translatable("boss.kingdomsatwar.phasmor"),
            BossEvent.BossBarColor.BLUE, BossEvent.BossBarOverlay.NOTCHED_10);
    private Attack attack = Attack.NONE;
    private Attack lastAttack = Attack.NONE;
    private int attackTick;
    private int cooldown = 30;
    private int idleTicks;
    /** Für Tests: nur wer so nah ist, wird getroffen (siehe {@link #confine}). */
    private double confine = Double.MAX_VALUE;
    private @Nullable BlockPos home;
    private @Nullable BlockPos throne;
    private final Set<UUID> struck = new HashSet<>();
    /** Leerenrisse: wo sie klaffen. */
    private final List<Vec3> rifts = new ArrayList<>();
    /** Blockwurf: die Brocken, die um ihn kreisen. */
    private final List<UUID> boulders = new ArrayList<>();
    private int clientAttackStart;

    public PhasmorEntity(EntityType<? extends PhasmorEntity> type, Level level) {
        super(type, level);
        this.xpReward = 450;
        this.setPersistenceRequired();
        this.setPathfindingMalus(PathType.WATER, -1.0f);
        this.setPathfindingMalus(PathType.WATER_BORDER, 16.0f);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, MAX_HEALTH)
                .add(Attributes.ARMOR, 12.0)
                .add(Attributes.ARMOR_TOUGHNESS, 4.0)
                .add(Attributes.ATTACK_DAMAGE, 11.0)
                .add(Attributes.MOVEMENT_SPEED, 0.32)
                .add(Attributes.FOLLOW_RANGE, 40.0)
                .add(Attributes.STEP_HEIGHT, 1.2)
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
        builder.define(DATA_CROWNED, false);
    }

    public Attack currentAttack() {
        int i = this.entityData.get(DATA_ATTACK);
        return i >= 0 && i < Attack.values().length ? Attack.values()[i] : Attack.NONE;
    }

    @Override
    public int attackOrdinal() {
        return this.entityData.get(DATA_ATTACK);
    }

    @Override
    public boolean isCrowned() {
        return this.entityData.get(DATA_CROWNED);
    }

    /** Kniet er vor dem Thron und hält Wache (unverwundbar)? */
    public boolean isGuarding() {
        return this.currentAttack() == Attack.GUARD;
    }

    @Override
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

    /** Bindet ihn an seinen Thron: Er kniet bei {@code home} und hält Wache (unverwundbar, bis jemand näher kommt). */
    public void bindTo(BlockPos home, @Nullable BlockPos throne) {
        this.home = home.immutable();
        this.throne = throne == null ? null : throne.immutable();
        this.begin(Attack.GUARD);
    }

    /** Neu gerufen (ein Phasit auf dem leeren Thron): Er erwacht sofort. */
    public void rise(BlockPos home, @Nullable BlockPos throne) {
        this.home = home.immutable();
        this.throne = throne == null ? null : throne.immutable();
        this.begin(Attack.AWAKEN);
    }

    /** Er erwacht (wenn er Wache hält). */
    public void awaken() {
        if (this.attack == Attack.GUARD || this.attack == Attack.NONE && this.getTarget() == null) {
            this.begin(Attack.AWAKEN);
        }
    }

    public @Nullable BlockPos home() {
        return this.home;
    }

    public @Nullable BlockPos throne() {
        return this.throne;
    }

    /** Beginnt sofort einen bestimmten Angriff (für Tests und die Vorführung). */
    public void forceAttack(Attack next) {
        this.begin(next);
    }

    /** Sofort gekrönt (für Tests und die Vorführung). */
    public void forceCrowned() {
        this.entityData.set(DATA_CROWNED, true);
    }

    /** Die Leerenrisse, die gerade klaffen (für Tests). */
    public List<Vec3> rifts() {
        return List.copyOf(this.rifts);
    }

    private void begin(Attack next) {
        if (this.attack == Attack.HURL && next != Attack.HURL) {
            this.dropBoulders();
        }
        this.attack = next;
        this.attackTick = 0;
        this.entityData.set(DATA_ATTACK, next.ordinal());
        if (next != Attack.NONE && next != Attack.AWAKEN && next != Attack.GUARD && next != Attack.CROWN && next != Attack.STAGGER) {
            this.lastAttack = next;
        }
        this.struck.clear();
        if (next != Attack.RIFTS) {
            this.rifts.clear();
        }
        if (next != Attack.NONE) {
            this.getNavigation().stop();
        }
        if (this.isInvisible() && next != Attack.BLINK) {
            this.setInvisible(false);
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

    /** Wasser schmerzt ihn wie alle Leerwandler (siehe {@link #hurtServer}). */
    @Override
    public boolean isSensitiveToWater() {
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

    /** Ist das ein Treffer mit Wasser (er steht darin, oder ein Wurftrank aus Wasser zerspringt an ihm)? */
    static boolean water(DamageSource source) {
        return source.is(DamageTypes.DROWN) || source.getDirectEntity() instanceof AbstractThrownPotion;
    }

    /**
     * Beim Wachen, beim Erwachen und während der Krönung ist er unverwundbar (wer ihn beim Wachen angreift, weckt ihn). Wasser
     * lässt ihn sich krümmen; Geschossen phast er oft davon – nicht aber, solange er sich krümmt.
     */
    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return super.hurtServer(level, source, amount);
        }
        if (this.attack == Attack.GUARD) {
            if (source.getEntity() instanceof Player player && !player.isCreative() && !player.isSpectator()) {
                this.wakeThrone(level);
            }
            return false;
        }
        if (this.attack == Attack.AWAKEN || this.attack == Attack.CROWN && this.attackTick < CROWN_FLARE + 6 || source.is(DamageTypes.IN_WALL)
                || source.is(DamageTypes.FALL)) {
            return false;
        }
        if (water(source)) {
            boolean fresh = this.attack != Attack.STAGGER;
            if (fresh) {
                this.stagger(level);
            }
            return super.hurtServer(level, source, fresh ? WATER_DAMAGE : amount);
        }
        if (source.is(DamageTypeTags.IS_PROJECTILE) && this.attack != Attack.STAGGER && this.random.nextFloat() < DODGE_CHANCE
                && this.dodge(level, source.getEntity())) {
            return false;
        }
        boolean hurt = super.hurtServer(level, source, amount);
        if (hurt && source.getEntity() instanceof LivingEntity attacker && attacker != this && this.canStrike(attacker)
                && !(this.getTarget() instanceof Player)) {
            this.setTarget(attacker);
        }
        return hurt;
    }

    private void wakeThrone(ServerLevel level) {
        if (this.throne != null && level.getBlockEntity(this.throne) instanceof VoidThroneBlockEntity seat && seat.owns(this)) {
            seat.awaken(level);
        } else {
            this.begin(Attack.AWAKEN);
        }
    }

    /** Wasser! Er krümmt sich, kann weder springen noch ausweichen; die Trugbilder zerfallen. */
    private void stagger(ServerLevel level) {
        this.begin(Attack.STAGGER);
        this.sound(level, ModSounds.PHASMOR_STAGGER.get(), 2.5f, 1.0f);
        level.sendParticles(ParticleTypes.SPLASH, this.getX(), this.getY(0.6), this.getZ(), 40, 0.6, 1.0, 0.6, 0.1);
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, this.getX(), this.getY(0.6), this.getZ(), 30, 0.5, 1.0, 0.5, 0.05);
        this.dispelIllusions(level);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide()) {
            this.clientEffects();
        }
    }

    /** Türkise Funken um die Krone und die Klinge, Portalwirbel um die Füße (gekrönt mehr davon). */
    private void clientEffects() {
        if (this.isInvisible()) {
            return;
        }
        Level level = this.level();
        boolean crowned = this.isCrowned();
        if (this.random.nextInt(crowned ? 1 : 3) == 0) {
            level.addParticle(ParticleTypes.PORTAL, this.getRandomX(0.6), this.getRandomY() - 0.25, this.getRandomZ(0.6),
                    (this.random.nextDouble() - 0.5) * 2.0, -this.random.nextDouble(), (this.random.nextDouble() - 0.5) * 2.0);
        }
        if (this.random.nextInt(crowned ? 2 : 5) == 0) {
            level.addParticle(new DustParticleOptions(PHASIT, 0.8f), this.getRandomX(0.5), this.getY(1.02 + this.random.nextDouble() * 0.12),
                    this.getRandomZ(0.5), 0.0, 0.02, 0.0);
        }
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        this.bossBar.setProgress(this.getHealth() / this.getMaxHealth());
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
            this.keepInSanctuary(level);
        }
        if (this.attack == Attack.STAGGER) {
            this.tickStagger(level);
            return;
        }
        if (!this.isCrowned() && this.attack != Attack.CROWN && this.getHealth() < this.getMaxHealth() * 0.5f) {
            this.begin(Attack.CROWN);
        }
        if (this.attack == Attack.CROWN) {
            this.tickCrown(level);
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
        if (this.attack != Attack.BLINK && this.attack != Attack.STORM) {
            this.lookAt(target, 40.0f, 40.0f);
        }
        switch (this.attack) {
            case SLASH -> this.tickSlash(level, target);
            case BLINK -> this.tickBlink(level, target);
            case ILLUSIONS -> this.tickIllusions(level, target);
            case HURL -> this.tickHurl(level, target);
            case RIFTS -> this.tickRifts(level, target);
            case STORM -> this.tickStorm(level, target);
            default -> { }
        }
        if (this.attackTick >= this.attack.duration) {
            this.begin(Attack.NONE);
            this.cooldown = this.isCrowned() ? 10 + this.random.nextInt(14) : 20 + this.random.nextInt(18);
        }
    }

    /**
     * Gehört er (noch) zu seinem Thron? Ist dort inzwischen ein anderer König gebunden (etwa nach dem Neuladen der Welt),
     * zerfällt dieser hier; ist der Thron fort, bleibt er frei.
     */
    private boolean strayed(ServerLevel level) {
        if (this.throne == null || !level.isLoaded(this.throne)) {
            return false;
        }
        if (level.getBlockEntity(this.throne) instanceof VoidThroneBlockEntity seat) {
            if (!seat.owns(this)) {
                level.sendParticles(ParticleTypes.REVERSE_PORTAL, this.getX(), this.getY(0.5), this.getZ(), 60, 0.5, 1.5, 0.5, 0.05);
                this.discard();
                return true;
            }
            return false;
        }
        this.throne = null;
        if (this.attack == Attack.GUARD) {
            this.begin(Attack.NONE);
        }
        return false;
    }

    /** Wen seine Angriffe treffen: alle Lebewesen außer ihm, seinen Trugbildern und Spielern im Kreativ-/Zuschauermodus. */
    public boolean canStrike(LivingEntity entity) {
        return entity != this && entity.isAlive() && !(entity instanceof PhasmorEntity) && !(entity instanceof PhasmorIllusion)
                && !(entity instanceof ArmorStand) && !(entity instanceof Player player && (player.isCreative() || player.isSpectator()));
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

    /** Die Mitte des Heiligtums (vor dem Thron, sonst sein Platz, sonst wo er steht). */
    public Vec3 center() {
        if (this.home != null) {
            return Vec3.atBottomCenterOf(this.home);
        }
        return this.position();
    }

    private Vec3 forward() {
        float yaw = this.getYRot() * Mth.DEG_TO_RAD;
        return new Vec3(-Mth.sin(yaw), 0.0, Mth.cos(yaw));
    }

    private Vec3 toward(Entity target) {
        Vec3 dir = target.position().subtract(this.position()).multiply(1.0, 0.0, 1.0);
        return dir.lengthSqr() < 1.0e-4 ? this.forward() : dir.normalize();
    }

    private void sound(ServerLevel level, SoundEvent sound, float volume, float pitch) {
        level.playSound(null, this.getX(), this.getY(), this.getZ(), sound, SoundSource.HOSTILE, volume, pitch);
    }

    /** Er schreitet auf sein Ziel zu (bis auf gut drei Blöcke); ist es weit fort, springt er ihm nach. */
    private void approach(LivingEntity target) {
        double distance = this.distanceTo(target);
        if (distance > 3.4) {
            this.getNavigation().moveTo(target, this.isCrowned() ? 1.25 : 1.0);
        } else {
            this.getNavigation().stop();
        }
    }

    /** Aus dem Heiligtum gelockt (oder in die Tiefe gestürzt): Er springt vor den Thron zurück. */
    private void keepInSanctuary(ServerLevel level) {
        if (this.home == null || this.attack == Attack.BLINK || this.attack == Attack.STORM) {
            return;
        }
        Vec3 c = this.center();
        double dx = this.getX() - c.x;
        double dz = this.getZ() - c.z;
        if (dx * dx + dz * dz < ARENA * ARENA && this.getY() > c.y - 6.0 && this.getY() < c.y + 12.0) {
            return;
        }
        Vec3 from = this.position();
        this.teleportTo(c.x, c.y, c.z);
        this.setDeltaMovement(Vec3.ZERO);
        this.getNavigation().stop();
        VoidwalkerArmor.phaseEffects(level, this, from);
        this.sound(level, ModSounds.PHASMOR_BLINK.get(), 2.0f, 0.8f);
    }

    /** Ohne Gegner: zurück vor den Thron; nach einer Weile kniet er wieder nieder. */
    private void idle(ServerLevel level) {
        if (this.attack != Attack.NONE) {
            this.begin(Attack.NONE);
        }
        this.idleTicks++;
        if (this.home != null && this.distanceToSqr(Vec3.atBottomCenterOf(this.home)) > 2.0) {
            if (this.idleTicks % 20 == 1) {
                this.getNavigation().moveTo(this.home.getX() + 0.5, this.home.getY(), this.home.getZ() + 0.5, 0.8);
            }
            if (this.idleTicks > 160 && this.idleTicks % 80 == 0) {
                Vec3 from = this.position();
                this.teleportTo(this.home.getX() + 0.5, this.home.getY(), this.home.getZ() + 0.5);
                VoidwalkerArmor.phaseEffects(level, this, from);
            }
        }
        if (this.home != null && this.throne != null && this.idleTicks > RETURN_TICKS
                && this.distanceToSqr(Vec3.atBottomCenterOf(this.home)) < 2.5) {
            this.returnToThrone(level);
        }
    }

    /** Er kniet wieder vor dem Thron nieder – geheilt, ohne Krone. */
    private void returnToThrone(ServerLevel level) {
        if (this.throne == null || !(level.getBlockEntity(this.throne) instanceof VoidThroneBlockEntity seat) || !seat.owns(this)) {
            return;
        }
        this.setHealth(this.getMaxHealth());
        this.entityData.set(DATA_CROWNED, false);
        this.idleTicks = 0;
        this.faceThrone();
        this.dispelIllusions(level);
        this.begin(Attack.GUARD);
        seat.onKingReturned(level, this);
    }

    /** Er blickt vom Thron fort (der Thron steht hinter ihm, er bewacht den Zugang). */
    private void faceThrone() {
        if (this.home == null || this.throne == null) {
            return;
        }
        double dx = this.home.getX() - this.throne.getX();
        double dz = this.home.getZ() - this.throne.getZ();
        float yaw = (float) (Mth.atan2(dz, dx) * Mth.RAD_TO_DEG) - 90.0f;
        this.snapTo(this.home.getX() + 0.5, this.home.getY(), this.home.getZ() + 0.5, yaw, 0.0f);
        this.yBodyRot = yaw;
        this.yHeadRot = yaw;
    }

    private Attack chooseAttack(ServerLevel level, LivingEntity target) {
        double distance = this.distanceTo(target);
        List<Attack> options = new ArrayList<>();
        if (distance < 5.5) {
            options.addAll(List.of(Attack.SLASH, Attack.SLASH, Attack.RIFTS, Attack.BLINK));
        } else if (distance < 12.0) {
            options.addAll(List.of(Attack.BLINK, Attack.BLINK, Attack.HURL, Attack.RIFTS, Attack.SLASH));
        } else {
            options.addAll(List.of(Attack.BLINK, Attack.HURL, Attack.HURL, Attack.RIFTS));
        }
        if (this.illusions(level) < 2) {
            options.add(Attack.ILLUSIONS);
        }
        if (this.isCrowned()) {
            options.add(Attack.STORM);
            options.add(Attack.STORM);
        }
        options.removeIf(a -> a == this.lastAttack);
        if (options.isEmpty()) {
            options.add(distance < 5.5 ? Attack.SLASH : Attack.BLINK);
        }
        return options.get(this.random.nextInt(options.size()));
    }

    private int illusions(ServerLevel level) {
        return level.getEntitiesOfClass(PhasmorIllusion.class, this.getBoundingBox().inflate(32.0), e -> e.isAlive() && e.king() == this).size();
    }

    private void dispelIllusions(ServerLevel level) {
        for (PhasmorIllusion illusion : level.getEntitiesOfClass(PhasmorIllusion.class, this.getBoundingBox().inflate(40.0),
                e -> e.isAlive() && e.king() == this)) {
            illusion.dispel();
        }
    }

    // ------------------------------------------------------------------ Springen

    /** Kann er hier stehen (fester Boden darunter, Platz für seinen Leib, kein Wasser)? Gibt die Stelle zurück oder null. */
    private @Nullable Vec3 standable(ServerLevel level, double x, double y, double z) {
        BlockPos.MutableBlockPos p = BlockPos.containing(x, y + 2, z).mutable();
        for (int i = 0; i < 7; i++) {
            BlockPos below = p.below();
            BlockState ground = level.getBlockState(below);
            if (!ground.getCollisionShape(level, below).isEmpty() && level.getBlockState(p).getCollisionShape(level, p).isEmpty()) {
                Vec3 at = new Vec3(x, p.getY(), z);
                AABB box = this.getDimensions(this.getPose()).makeBoundingBox(at);
                if (level.noCollision(this, box) && !level.containsAnyLiquid(box)) {
                    return at;
                }
                return null;
            }
            p.move(0, -1, 0);
        }
        return null;
    }

    /** Springt (zerfließt, steht anderswo): zuerst dorthin, dann auf Kreisen ringsum. Gibt zurück, ob es gelang. */
    private boolean blinkNear(ServerLevel level, Vec3 goal, double spread) {
        Vec3 at = this.standable(level, goal.x, goal.y, goal.z);
        for (int i = 0; at == null && i < 14; i++) {
            double a = this.random.nextDouble() * Math.PI * 2.0;
            double r = 1.0 + this.random.nextDouble() * spread;
            at = this.standable(level, goal.x + Math.cos(a) * r, goal.y, goal.z + Math.sin(a) * r);
        }
        if (at == null) {
            return false;
        }
        Vec3 from = this.position();
        this.teleportTo(at.x, at.y, at.z);
        this.setDeltaMovement(Vec3.ZERO);
        this.getNavigation().stop();
        level.gameEvent(net.minecraft.world.level.gameevent.GameEvent.TELEPORT, from, net.minecraft.world.level.gameevent.GameEvent.Context.of(this));
        VoidwalkerArmor.phaseEffects(level, this, from);
        return true;
    }

    /** Einem Geschoss davonphasen: drei bis sechs Blöcke zur Seite. */
    private boolean dodge(ServerLevel level, @Nullable Entity shooter) {
        Vec3 side = shooter == null ? this.forward().yRot(Mth.HALF_PI) : this.toward(shooter).yRot(Mth.HALF_PI);
        for (int i = 0; i < 8; i++) {
            double dir = this.random.nextBoolean() ? 1.0 : -1.0;
            double r = 3.0 + this.random.nextDouble() * 3.0;
            Vec3 goal = this.position().add(side.scale(dir * r));
            Vec3 at = this.standable(level, goal.x, this.getY(), goal.z);
            if (at != null && at.distanceToSqr(this.center()) < ARENA * ARENA) {
                Vec3 from = this.position();
                this.teleportTo(at.x, at.y, at.z);
                VoidwalkerArmor.phaseEffects(level, this, from);
                this.sound(level, ModSounds.PHASMOR_BLINK.get(), 1.6f, 1.3f);
                return true;
            }
        }
        return false;
    }

    // ------------------------------------------------------------------ Wache und Erwachen

    /** Er kniet vor dem Thron, die Hände auf dem Knauf; dann und wann summt die Klinge, und die Krone glimmt. */
    private void tickGuard(ServerLevel level) {
        int t = ++this.attackTick;
        this.setDeltaMovement(0.0, this.getDeltaMovement().y, 0.0);
        if (t % 160 == 40) {
            this.sound(level, ModSounds.PHASMOR_GUARD.get(), 1.2f, 0.95f + this.random.nextFloat() * 0.1f);
        }
        if (t % 8 == 0) {
            Vec3 hilt = this.position().add(this.forward().scale(0.95)).add(0.0, 1.0, 0.0);
            level.sendParticles(new DustParticleOptions(PHASIT, 0.7f), hilt.x, hilt.y, hilt.z, 1, 0.08, 0.3, 0.08, 0.0);
        }
    }

    /** Er hebt das Haupt, steht auf, zieht die Klinge aus dem Boden, reckt sie empor – die Krone flammt auf – und senkt sie. */
    private void tickAwaken(ServerLevel level) {
        int t = ++this.attackTick;
        Player nearest = level.getNearestPlayer(this, 28.0);
        if (nearest != null && t > 16) {
            this.getLookControl().setLookAt(nearest, 20.0f, 30.0f);
            if (t > 40) {
                this.lookAt(nearest, 10.0f, 30.0f);
            }
        }
        if (t == 4) {
            this.sound(level, ModSounds.PHASMOR_AWAKEN.get(), 3.0f, 1.0f);
        }
        if (t == 30) {
            Vec3 tip = this.position().add(this.forward().scale(1.2));
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.END_STONE_BRICKS.defaultBlockState()), tip.x, this.getY() + 0.1,
                    tip.z, 30, 0.3, 0.1, 0.3, 0.15);
            this.sound(level, ModSounds.PHASMOR_SWING.get(), 2.0f, 0.7f);
        }
        if (t == 46) {
            this.sound(level, ModSounds.PHASMOR_CROWN.get(), 2.6f, 1.15f);
            level.sendParticles(new DustParticleOptions(PHASIT, 1.6f), this.getX(), this.getY(1.05), this.getZ(), 60, 0.6, 0.4, 0.6, 0.0);
            level.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, 0xFF3FE6CC), this.getX(), this.getY(0.8), this.getZ(), 1, 0.0,
                    0.0, 0.0, 0.0);
            Component title = Component.translatable("boss.kingdomsatwar.phasmor").withColor(PHASIT);
            Component subtitle = Component.translatable("boss.kingdomsatwar.phasmor.title").withStyle(ChatFormatting.GRAY);
            for (ServerPlayer player : level.players()) {
                if (player.distanceToSqr(this) < 64.0 * 64.0) {
                    player.connection.send(new ClientboundSetTitlesAnimationPacket(15, 60, 25));
                    player.connection.send(new ClientboundSetTitleTextPacket(title));
                    player.connection.send(new ClientboundSetSubtitleTextPacket(subtitle));
                }
            }
        }
        if (t == 68) {
            this.sound(level, ModSounds.PHASMOR_SLASH.get(), 2.2f, 0.85f);
        }
        if (t >= AWAKEN_TICKS) {
            this.begin(Attack.NONE);
            this.cooldown = 12;
            this.idleTicks = 0;
        }
    }

    // ------------------------------------------------------------------ Angriffe

    /** Ein Hieb in einem Bogen vor ihm (Ziel, Reichweite, Schaden); Rückstoß vom König fort. */
    private void cleave(ServerLevel level, Vec3 dir, double range, double arc, float damage) {
        for (int i = 0; i <= 14; i++) {
            double k = i / 14.0;
            double a = (k - 0.5) * arc * 2.0;
            Vec3 p = this.position().add(dir.yRot((float) a).scale(range * (0.55 + 0.45 * Math.sin(k * Math.PI)))).add(0.0, 1.4 + k * 0.8, 0.0);
            level.sendParticles(new DustParticleOptions(PHASIT, 1.2f), p.x, p.y, p.z, 2, 0.05, 0.05, 0.05, 0.0);
        }
        level.sendParticles(ParticleTypes.SWEEP_ATTACK, this.getX() + dir.x * 2.0, this.getY(0.45), this.getZ() + dir.z * 2.0, 1, 0.0, 0.0, 0.0, 0.0);
        double cos = Math.cos(arc);
        for (LivingEntity victim : this.victims(level, this.getBoundingBox().inflate(range + 1.0, 2.5, range + 1.0))) {
            Vec3 to = victim.position().subtract(this.position()).multiply(1.0, 0.0, 1.0);
            double distance = to.length();
            if (distance > range + victim.getBbWidth() * 0.5 || distance > 0.8 && to.normalize().dot(dir) < cos) {
                continue;
            }
            if (this.struck.add(victim.getUUID()) && victim.hurtServer(level, this.damageSources().mobAttack(this), damage)) {
                Vec3 out = distance < 1.0e-3 ? dir : to.normalize();
                victim.push(out.x * 0.7, 0.3, out.z * 0.7);
                victim.needsSync = true;
            }
        }
    }

    /** Klingenhieb: über die rechte Schulter ausholen, dann schräg hinab – ein weiter Bogen aus türkisem Licht. */
    private void tickSlash(ServerLevel level, LivingEntity target) {
        int t = this.attackTick;
        if (t == 6) {
            this.sound(level, ModSounds.PHASMOR_SWING.get(), 1.8f, 0.95f + this.random.nextFloat() * 0.1f);
        }
        if (t == SLASH_HIT) {
            this.sound(level, ModSounds.PHASMOR_SLASH.get(), 2.4f, 1.0f);
            this.cleave(level, this.toward(target), SLASH_RANGE, 1.15, SLASH_DAMAGE + (this.isCrowned() ? 3.0f : 0.0f));
        }
    }

    /** Phasenstoß: Er zerfließt zu einem Nachbild – und steht hinter seinem Opfer, die Klinge schon erhoben. */
    private void tickBlink(ServerLevel level, LivingEntity target) {
        int t = this.attackTick;
        if (t == BLINK_VANISH) {
            this.sound(level, ModSounds.PHASMOR_BLINK.get(), 2.2f, 1.0f);
            level.sendParticles(new DustParticleOptions(PHASIT, 1.5f), this.getX(), this.getY(0.5), this.getZ(), 70, 0.35, 1.3, 0.35, 0.0);
            this.setInvisible(true);
        }
        if (t == BLINK_APPEAR) {
            Vec3 behind = target.position().subtract(target.getLookAngle().multiply(1.0, 0.0, 1.0).normalize().scale(2.6));
            if (!this.blinkNear(level, behind, 2.5)) {
                this.blinkNear(level, target.position(), 3.5);
            }
            this.setInvisible(false);
            this.lookAt(target, 360.0f, 90.0f);
            this.yBodyRot = this.getYRot();
            this.yHeadRot = this.getYRot();
            this.sound(level, ModSounds.PHASMOR_BLINK.get(), 2.2f, 1.25f);
        }
        if (t == BLINK_HIT - 3) {
            this.sound(level, ModSounds.PHASMOR_SWING.get(), 2.0f, 1.1f);
        }
        if (t == BLINK_HIT) {
            this.sound(level, ModSounds.PHASMOR_SLASH.get(), 2.6f, 1.1f);
            this.cleave(level, this.toward(target), 4.4, 1.0, BLINK_DAMAGE + (this.isCrowned() ? 3.0f : 0.0f));
        }
    }

    /** Trugbilder: Er hebt die Linke – aus der Leere treten drei (gekrönt vier) Abbilder, und er mischt sich unter sie. */
    private void tickIllusions(ServerLevel level, LivingEntity target) {
        int t = this.attackTick;
        if (t == 6) {
            this.sound(level, ModSounds.PHASMOR_ILLUSION.get(), 2.4f, 1.0f);
        }
        if (t != ILLUSION_SPAWN) {
            return;
        }
        int count = Math.min(this.isCrowned() ? 4 : 3, MAX_ILLUSIONS - this.illusions(level));
        if (count <= 0) {
            return;
        }
        // Plätze auf einem Kreis um das Ziel: einer davon ist seiner
        double start = this.random.nextDouble() * Math.PI * 2.0;
        int slots = count + 1;
        int mine = this.random.nextInt(slots);
        for (int i = 0; i < slots; i++) {
            double a = start + i * Math.PI * 2.0 / slots;
            Vec3 spot = target.position().add(Math.cos(a) * 5.0, 0.0, Math.sin(a) * 5.0);
            if (i == mine) {
                this.blinkNear(level, spot, 1.5);
                continue;
            }
            Vec3 at = this.standable(level, spot.x, target.getY(), spot.z);
            if (at == null) {
                continue;
            }
            PhasmorIllusion illusion = ModEntities.PHASMOR_ILLUSION.get().create(level, EntitySpawnReason.MOB_SUMMONED);
            if (illusion == null) {
                continue;
            }
            illusion.snapTo(at.x, at.y, at.z, this.getYRot(), 0.0f);
            illusion.bind(this, this.isCrowned());
            illusion.setTarget(target);
            illusion.lookAt(target, 360.0f, 90.0f);
            illusion.yBodyRot = illusion.getYRot();
            level.addFreshEntity(illusion);
            VoidwalkerArmor.phaseEffects(level, illusion, at.add(0.0, 0.0, 0.0));
        }
    }

    /** Blockwurf: Brocken aus dem Heiligtum steigen um ihn auf und fliegen nacheinander auf seine Gegner. */
    private void tickHurl(ServerLevel level, LivingEntity target) {
        int t = this.attackTick;
        if (t == HURL_LIFT) {
            this.sound(level, ModSounds.PHASMOR_LIFT.get(), 2.4f, 1.0f);
            this.boulders.clear();
            BlockState[] stones = {Blocks.END_STONE_BRICKS.defaultBlockState(), Blocks.PURPUR_BLOCK.defaultBlockState(),
                Blocks.END_STONE.defaultBlockState(), Blocks.OBSIDIAN.defaultBlockState()};
            for (int i = 0; i < HURL_THROWS.length; i++) {
                PhaseBoulderEntity boulder = new PhaseBoulderEntity(level, this, stones[this.random.nextInt(stones.length)], i, HURL_THROWS.length);
                boulder.setDamage(BOULDER_DAMAGE + (this.isCrowned() ? 2.0f : 0.0f));
                level.addFreshEntity(boulder);
                this.boulders.add(boulder.getUUID());
                level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, boulder.blockState()), this.getX(), this.getY() + 0.1, this.getZ(), 20,
                        1.5, 0.1, 1.5, 0.15);
            }
        }
        for (int i = 0; i < HURL_THROWS.length; i++) {
            if (t != HURL_THROWS[i] || i >= this.boulders.size()) {
                continue;
            }
            if (level.getEntity(this.boulders.get(i)) instanceof PhaseBoulderEntity boulder && boulder.isHeld()) {
                List<LivingEntity> marks = this.victims(level, this.getBoundingBox().inflate(24.0, 10.0, 24.0));
                LivingEntity aim = marks.isEmpty() ? target : marks.get(this.random.nextInt(marks.size()));
                boulder.launchAt(aim);
                this.sound(level, ModSounds.PHASMOR_THROW.get(), 2.2f, 0.9f + this.random.nextFloat() * 0.2f);
            }
        }
    }

    /** Lässt die Brocken fallen, die noch um ihn kreisen (ein abgebrochener Wurf). */
    private void dropBoulders() {
        if (this.level() instanceof ServerLevel level) {
            for (UUID id : this.boulders) {
                if (level.getEntity(id) instanceof PhaseBoulderEntity boulder && boulder.isHeld()) {
                    boulder.shatter(level);
                }
            }
        }
        this.boulders.clear();
    }

    /**
     * Leerenrisse: Er stößt die Klinge in den Boden – unter seinen Gegnern (und daneben) öffnen sich Kreise in die Leere; wer
     * noch darin steht, wenn sie aufbrechen, wird verschluckt und hoch über dem Heiligtum wieder ausgespuckt.
     */
    private void tickRifts(ServerLevel level, LivingEntity target) {
        int t = this.attackTick;
        if (t == 8) {
            this.sound(level, ModSounds.PHASMOR_SWING.get(), 1.8f, 0.8f);
        }
        if (t == RIFT_OPEN) {
            this.sound(level, ModSounds.PHASMOR_RIFT_OPEN.get(), 2.6f, 1.0f);
            Vec3 tip = this.position().add(this.forward().scale(1.3));
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.END_STONE_BRICKS.defaultBlockState()), tip.x, this.getY() + 0.1,
                    tip.z, 30, 0.4, 0.1, 0.4, 0.15);
            this.rifts.clear();
            List<LivingEntity> marks = this.victims(level, this.getBoundingBox().inflate(22.0, 8.0, 22.0));
            marks.sort((a, b) -> Double.compare(a.distanceToSqr(this), b.distanceToSqr(this)));
            for (LivingEntity e : marks.subList(0, Math.min(this.isCrowned() ? 4 : 3, marks.size()))) {
                this.rifts.add(new Vec3(e.getX(), e.getY(), e.getZ()));
            }
            if (this.rifts.isEmpty()) {
                this.rifts.add(target.position());
            }
            Vec3 main = this.rifts.getFirst();
            for (int i = 0; i < (this.isCrowned() ? 2 : 1); i++) {
                double a = this.random.nextDouble() * Math.PI * 2.0;
                this.rifts.add(main.add(Math.cos(a) * 3.4, 0.0, Math.sin(a) * 3.4));
            }
            for (Vec3 p : this.rifts) {
                SpellFxEntity.create(level, SpellFxEntity.Kind.CIRCLE, p.add(0.0, 0.05, 0.0), null, PHASIT, RIFT_ERUPT - RIFT_OPEN + 2,
                        (float) RIFT_RADIUS).spawn();
            }
        }
        if (t > RIFT_OPEN && t < RIFT_ERUPT && t % 3 == 0) {
            for (Vec3 p : this.rifts) {
                level.sendParticles(ParticleTypes.PORTAL, p.x, p.y + 0.2, p.z, 6, RIFT_RADIUS * 0.4, 0.1, RIFT_RADIUS * 0.4, 0.4);
            }
        }
        if (t == RIFT_ERUPT) {
            this.sound(level, ModSounds.PHASMOR_RIFT_ERUPT.get(), 3.0f, 0.9f + this.random.nextFloat() * 0.2f);
            for (Vec3 p : this.rifts) {
                SpellFxEntity.create(level, SpellFxEntity.Kind.VORTEX, p.add(0.0, 0.1, 0.0), null, PHASIT, 22, (float) RIFT_RADIUS).spawn();
                SpellFxEntity.create(level, SpellFxEntity.Kind.PILLAR, p, null, PHASIT, 16, (float) RIFT_RADIUS * 0.7f).param(5.0f).spawn();
                for (LivingEntity victim : this.victims(level, new AABB(p, p).inflate(RIFT_RADIUS + 0.6, 3.0, RIFT_RADIUS + 0.6))) {
                    double r = RIFT_RADIUS + victim.getBbWidth() * 0.5;
                    if (victim.distanceToSqr(p.x, victim.getY(), p.z) > r * r || Math.abs(victim.getY() - p.y) > 2.5) {
                        continue;
                    }
                    if (victim.hurtServer(level, this.damageSources().mobAttack(this), RIFT_DAMAGE + (this.isCrowned() ? 2.0f : 0.0f))) {
                        swallow(level, victim);
                    }
                }
            }
            this.rifts.clear();
        }
    }

    /** Der Riss verschluckt sein Opfer und spuckt es hoch darüber wieder aus (wenn dort Platz ist). */
    static void swallow(ServerLevel level, LivingEntity victim) {
        Vec3 from = victim.position();
        double lift = RIFT_LIFT;
        AABB box = victim.getBoundingBox();
        while (lift > 1.0 && !level.noCollision(victim, box.move(0.0, lift, 0.0))) {
            lift -= 1.0;
        }
        level.sendParticles(ParticleTypes.PORTAL, from.x, from.y + 0.5, from.z, 50, 0.4, 0.6, 0.4, 0.8);
        if (victim instanceof ServerPlayer player) {
            player.teleportTo(level, from.x, from.y + lift, from.z, java.util.Set.of(), player.getYRot(), player.getXRot(), false);
        } else {
            victim.teleportTo(from.x, from.y + lift, from.z);
        }
        victim.setDeltaMovement(0.0, 0.2, 0.0);
        victim.needsSync = true;
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, from.x, from.y + lift + 0.5, from.z, 40, 0.4, 0.6, 0.4, 0.05);
    }

    /** Phasensturm (gekrönt): fünf Sprünge in rascher Folge, nach jedem ein Hieb rundum. */
    private void tickStorm(ServerLevel level, LivingEntity target) {
        int t = this.attackTick;
        for (int blink : STORM_BLINKS) {
            if (t == blink) {
                List<LivingEntity> marks = this.victims(level, this.getBoundingBox().inflate(22.0, 8.0, 22.0));
                LivingEntity aim = marks.isEmpty() ? target : marks.get(this.random.nextInt(marks.size()));
                double a = this.random.nextDouble() * Math.PI * 2.0;
                this.blinkNear(level, aim.position().add(Math.cos(a) * 2.2, 0.0, Math.sin(a) * 2.2), 2.0);
                this.lookAt(aim, 360.0f, 90.0f);
                this.yBodyRot = this.getYRot();
                this.yHeadRot = this.getYRot();
                this.sound(level, ModSounds.PHASMOR_BLINK.get(), 2.0f, 1.1f + this.random.nextFloat() * 0.3f);
                this.struck.clear();
            }
            if (t == blink + 2) {
                this.sound(level, ModSounds.PHASMOR_SLASH.get(), 2.2f, 1.2f);
                this.cleave(level, this.forward(), 3.8, Math.PI, STORM_DAMAGE);
            }
        }
    }

    /** Krönung: Er brüllt auf, die Krone aus Phasit lodert auf – und die Trugbilder zerfallen. */
    private void tickCrown(ServerLevel level) {
        int t = ++this.attackTick;
        this.getNavigation().stop();
        if (t == 10) {
            this.sound(level, ModSounds.PHASMOR_AWAKEN.get(), 3.2f, 0.8f);
        }
        if (t == CROWN_FLARE) {
            this.entityData.set(DATA_CROWNED, true);
            this.sound(level, ModSounds.PHASMOR_CROWN.get(), 3.0f, 0.9f);
            this.dispelIllusions(level);
            level.sendParticles(new DustParticleOptions(PHASIT, 2.0f), this.getX(), this.getY(1.05), this.getZ(), 90, 0.8, 0.5, 0.8, 0.0);
            level.sendParticles(ParticleTypes.REVERSE_PORTAL, this.getX(), this.getY(0.5), this.getZ(), 120, 1.5, 1.5, 1.5, 0.3);
            level.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, 0xFF3FE6CC), this.getX(), this.getY(0.8), this.getZ(), 1, 0.0,
                    0.0, 0.0, 0.0);
            // wer zu nah steht, wird fortgeschleudert
            for (LivingEntity victim : this.victims(level, this.getBoundingBox().inflate(4.5, 2.0, 4.5))) {
                Vec3 out = victim.position().subtract(this.position()).multiply(1.0, 0.0, 1.0);
                out = out.lengthSqr() < 1.0e-4 ? this.forward() : out.normalize();
                victim.push(out.x * 1.4, 0.5, out.z * 1.4);
                victim.needsSync = true;
            }
        }
        if (t >= Attack.CROWN.duration) {
            this.begin(Attack.NONE);
            this.cooldown = 8;
        }
    }

    /** Wasser! Er krümmt sich; danach springt er aus dem Wasser (wenn er noch darin steht). */
    private void tickStagger(ServerLevel level) {
        int t = ++this.attackTick;
        this.getNavigation().stop();
        this.setDeltaMovement(0.0, this.getDeltaMovement().y, 0.0);
        if (t % 5 == 0) {
            level.sendParticles(ParticleTypes.FALLING_WATER, this.getX(), this.getY(0.9), this.getZ(), 4, 0.4, 0.4, 0.4, 0.0);
        }
        if (t >= STAGGER_TICKS) {
            if (this.isInWater()) {
                this.blinkNear(level, this.center(), 6.0);
            }
            this.begin(Attack.NONE);
            this.cooldown = 10;
        }
    }

    // ------------------------------------------------------------------ Tod, Speichern, Klänge

    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (this.level() instanceof ServerLevel level) {
            this.dispelIllusions(level);
            this.dropBoulders();
            if (this.throne != null && level.getBlockEntity(this.throne) instanceof VoidThroneBlockEntity seat && seat.owns(this)) {
                seat.onKingDefeated(level);
            }
        }
    }

    /** Er sinkt auf die Knie, die Krone erlischt – dann zerfällt er zu türkisem Staub, und die Leere nimmt ihn zurück. */
    @Override
    protected void tickDeath() {
        this.deathTime++;
        if (!(this.level() instanceof ServerLevel level)) {
            return;
        }
        if (this.deathTime % 4 == 0) {
            level.sendParticles(new DustParticleOptions(PHASIT, 1.2f), this.getX(), this.getY(0.5), this.getZ(), 10, 0.5, 1.2, 0.5, 0.0);
            level.sendParticles(ParticleTypes.PORTAL, this.getX(), this.getY(0.5), this.getZ(), 12, 0.6, 1.2, 0.6, 0.6);
        }
        if (this.deathTime == DEATH_TICKS / 2) {
            level.playSound(null, this.getX(), this.getY(), this.getZ(), ModSounds.PHASMOR_BLINK.get(), SoundSource.HOSTILE, 3.0f, 0.5f);
        }
        if (this.deathTime == DEATH_TICKS - 2) {
            level.playSound(null, this.getX(), this.getY(), this.getZ(), ModSounds.PHASMOR_RIFT_ERUPT.get(), SoundSource.HOSTILE, 3.0f, 0.6f);
            level.sendParticles(new DustParticleOptions(PHASIT, 2.0f), this.getX(), this.getY(0.5), this.getZ(), 160, 0.8, 1.6, 0.8, 0.0);
            level.sendParticles(ParticleTypes.REVERSE_PORTAL, this.getX(), this.getY(0.5), this.getZ(), 160, 1.0, 1.6, 1.0, 0.2);
            level.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, 0xFF3FE6CC), this.getX(), this.getY(0.5), this.getZ(), 1, 0.0, 0.0,
                    0.0, 0.0);
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
        if (this.throne != null) {
            output.putLong("Throne", this.throne.asLong());
        }
        output.putBoolean("Crowned", this.isCrowned());
        output.putBoolean("Guarding", this.attack == Attack.GUARD);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        input.getLong("Home").ifPresent(h -> this.home = BlockPos.of(h));
        input.getLong("Throne").ifPresent(a -> this.throne = BlockPos.of(a));
        this.entityData.set(DATA_CROWNED, input.getBooleanOr("Crowned", false));
        if (input.getBooleanOr("Guarding", false)) {
            this.begin(Attack.GUARD);
        }
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return this.attack == Attack.GUARD ? null : ModSounds.PHASMOR_AMBIENT.get();
    }

    @Override
    public int getAmbientSoundInterval() {
        return 160;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.PHASMOR_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.PHASMOR_DEATH.get();
    }

    @Override
    protected float getSoundVolume() {
        return 2.2f;
    }

    // ------------------------------------------------------------------ Himmelswacht

    @Override
    public io.github.sirschrolli.kingdomsatwar.entity.boss.sky.SkyTheme skyTheme() {
        return io.github.sirschrolli.kingdomsatwar.entity.boss.sky.SkyTheme.PHASE;
    }

    /** Für Tests: wie viele Trugbilder um ihn wandeln. */
    public int illusionCount() {
        return this.level() instanceof ServerLevel level ? this.illusions(level) : 0;
    }

    /** Für Tests: klaffen gerade Leerenrisse? */
    public boolean riftsOpen() {
        return !this.rifts.isEmpty();
    }
}
