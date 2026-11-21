package io.github.sirschrolli.kingdomsatwar.entity.voidrealm;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.block.voidrealm.WeaverNestBlockEntity;
import io.github.sirschrolli.kingdomsatwar.entity.end.StarfallEntity;
import io.github.sirschrolli.kingdomsatwar.magic.fx.SpellFxEntity;
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
import net.minecraft.resources.Identifier;
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
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
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
 * Die Sternenweberin (seit 0.99): Als die Welten zerbrachen, fing sie die fallenden Sterne in ihrem Netz – seither hängt sie
 * im Sternennetz der Leere über ihrem Gelege und webt aus ihrem Licht neue Sternbilder. Eine riesige Spinne mit acht
 * Sternenaugen, auf dem Hinterleib eine Spiralgalaxie, aus dem Rücken wachsen Kristalle der Leere. In Andacht hängt sie
 * kopfüber an einem Faden {@link #HANG} Blöcke über ihrem Platz und dreht sich langsam; wer näher als {@link #WAKE_RADIUS}
 * Blöcke kommt (oder sie trifft), weckt sie – sie lässt sich fallen und kämpft:
 * <ul>
 *     <li><b>Giftbiss</b> – ein Satz nach vorn, die Giftzähne schlagen zu;</li>
 *     <li><b>Fangfaden</b> – ein Faden aus Sternenlicht trifft ihr Ziel und spult es zu ihren Zähnen;</li>
 *     <li><b>Netzwurf</b> – sie spuckt einen Ballen Sternennetz: wo er landet, klebt man fest;</li>
 *     <li><b>Sternbild</b> – um ihr Ziel leuchten Sterne auf, ein Faden verbindet sie, dann flammen die Linien auf;</li>
 *     <li><b>Sternschnuppen</b> – sie trommelt auf ihr Netz, und die Sterne, die darin hängen, fallen;</li>
 *     <li><b>Umgarnen</b> – wer ihr zu nahe ist, wird eingesponnen und kann sich eine Weile nicht rühren;</li>
 *     <li><b>Brut</b> – aus ihrem Hinterleib krabbeln Sternspinnlinge;</li>
 *     <li><b>Abseilen</b> – sie klettert an einem Faden hinauf, hängt über ihrem Ziel und lässt sich darauf fallen;</li>
 *     <li><b>Die Spindel</b> (im Zorn) – sie dreht sich, und Fäden fegen über den ganzen Grund: wer nicht springt, wird getroffen.</li>
 * </ul>
 * Unter halber Kraft kreischt sie auf (ihr Zorn) – die Sterne auf ihrem Rücken flammen, sie wird schneller, und gleich darauf
 * dreht sich die Spindel. Findet sie niemanden mehr, kehrt sie zu ihrem Platz zurück und klettert wieder an ihren Faden
 * (geheilt). Besiegt rollt sie die Beine ein und zerfällt zu Sternenstaub; das Gelege erlischt, und ein Himmelseisenbarren
 * darin lockt eine neue Weberin.
 */
public class StarWeaverEntity extends Monster implements io.github.sirschrolli.kingdomsatwar.entity.boss.sky.SkyWarden {
    public static final float MAX_HEALTH = 1600.0f;
    /** So lange (Ticks) dauert ihr Sterben (sie rollt die Beine ein und zerfällt zu Sternenstaub). */
    public static final int DEATH_TICKS = 80;
    /** So nah (waagerecht) muss man ihr kommen, damit sie erwacht. */
    public static final double WAKE_RADIUS = 14.0;
    /** So hoch hängt sie in Andacht über ihrem Platz. */
    public static final double HANG = 7.0;
    /** Weiter als so weit von ihrem Platz folgt sie niemandem … */
    private static final double LEASH = 32.0;
    /** … und wer sie weiter fortlockt, dem entkommt sie an einem Faden zurück auf ihr Netz. */
    private static final double ARENA = 17.0;
    /** So lange ohne Gegner, dann klettert sie wieder an ihren Faden. */
    private static final int RETURN_TICKS = 400;
    public static final float BITE_DAMAGE = 13.0f;
    /** Fangfaden: so weit reicht er, so viel schadet der Biss am Ende. */
    public static final double THREAD_RANGE = 22.0;
    public static final float THREAD_DAMAGE = 9.0f;
    /** Sternbild: Halbmesser des Kreises, auf dem die Sterne stehen, und der Schaden der Linien. */
    public static final double CONSTELLATION_RADIUS = 4.6;
    public static final float CONSTELLATION_DAMAGE = 12.0f;
    /** Sternschnuppen: Schaden je Stern. */
    public static final float STARFALL_DAMAGE = 10.0f;
    /** Umgarnen: so lange (Ticks) ist man eingesponnen. */
    public static final int COCOON_TICKS = 50;
    /** Abseilen: Schaden und Halbmesser des Aufpralls. */
    public static final float DROP_DAMAGE = 16.0f;
    public static final double DROP_RADIUS = 3.6;
    /** Die Spindel: so weit fegen die Fäden, so viel schadet ein Treffer. */
    public static final double SPINDLE_RADIUS = 14.5;
    public static final float SPINDLE_DAMAGE = 8.0f;
    /** Höchstens so viele Sternspinnlinge zugleich. */
    public static final int MAX_BROOD = 6;
    private static final Identifier ROOT_ID = KingdomsAtWar.id("star_weaver_cocoon");

    public enum Attack {
        NONE(0), DORMANT(0), AWAKEN(64), BITE(22), THREAD(44), WEB(30), CONSTELLATION(72), STARFALL(66), COCOON(56), BROOD(40),
        DROP(88), SPINDLE(150), FURY(36);

        public final int duration;

        Attack(int duration) {
            this.duration = duration;
        }
    }

    /** Abseilen: hinauf (bis {@link #DROP_HANG}), hängen und über das Ziel gleiten (bis {@link #DROP_FALL}), fallen. */
    public static final int DROP_HANG = 22;
    public static final int DROP_FALL = 46;

    private static final EntityDataAccessor<Integer> DATA_ATTACK = SynchedEntityData.defineId(StarWeaverEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_ENRAGED = SynchedEntityData.defineId(StarWeaverEntity.class,
            EntityDataSerializers.BOOLEAN);

    private final ServerBossEvent bossBar = new ServerBossEvent(UUID.randomUUID(), Component.translatable("boss.kingdomsatwar.star_weaver"),
            BossEvent.BossBarColor.BLUE, BossEvent.BossBarOverlay.NOTCHED_10);
    private Attack attack = Attack.NONE;
    private Attack lastAttack = Attack.NONE;
    private int attackTick;
    private int cooldown = 30;
    private int idleTicks;
    private int leapCooldown = 60;
    private @Nullable BlockPos home;
    private @Nullable BlockPos nest;
    private final Set<UUID> struck = new HashSet<>();
    private @Nullable UUID reeled;
    private final List<Vec3> stars = new ArrayList<>();
    /** Angekündigte Sterne: wo (x, y, z) und wann (Tick) sie fallen. */
    private final List<double[]> starfall = new ArrayList<>();
    private @Nullable Vec3 dropMark;
    private final Map<UUID, Integer> rooted = new HashMap<>();
    private final Map<UUID, Integer> spindleHits = new HashMap<>();
    private float spindleAngle;
    private int clientAttackStart;

    public StarWeaverEntity(EntityType<? extends StarWeaverEntity> type, Level level) {
        super(type, level);
        this.xpReward = 600;
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        // nach Anni dem Troll, vor dem Zerbrochenen König
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, MAX_HEALTH)
                .add(Attributes.ARMOR, 12.0)
                .add(Attributes.ARMOR_TOUGHNESS, 5.0)
                .add(Attributes.ATTACK_DAMAGE, BITE_DAMAGE)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.FOLLOW_RANGE, 48.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.95)
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

    /** Unter halber Kraft: schneller, mehr Sterne, die Spindel. */
    public boolean isEnraged() {
        return this.entityData.get(DATA_ENRAGED);
    }

    /** Hängt sie in Andacht an ihrem Faden? */
    public boolean isDormant() {
        return this.currentAttack() == Attack.DORMANT;
    }

    /** Hängt sie gerade kopfüber an einem Faden (in Andacht oder beim Abseilen)? */
    public boolean isHanging() {
        Attack a = this.currentAttack();
        return a == Attack.DORMANT || (a == Attack.DROP && this.attackTime(0.0f) < DROP_FALL);
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

    /** Bindet sie an ihren Platz über dem Gelege: Sie hängt in Andacht an ihrem Faden. */
    public void bindTo(BlockPos home, @Nullable BlockPos nest) {
        this.home = home.immutable();
        this.nest = nest == null ? null : nest.immutable();
        this.begin(Attack.DORMANT);
        this.snapTo(home.getX() + 0.5, home.getY() + HANG, home.getZ() + 0.5, this.getYRot(), 0.0f);
    }

    /** Gelockt (ein Himmelseisenbarren im erloschenen Gelege): Sie lässt sich sofort fallen. */
    public void rise(BlockPos home, @Nullable BlockPos nest) {
        this.home = home.immutable();
        this.nest = nest == null ? null : nest.immutable();
        this.snapTo(home.getX() + 0.5, home.getY() + HANG, home.getZ() + 0.5, this.getYRot(), 0.0f);
        this.begin(Attack.AWAKEN);
    }

    public @Nullable BlockPos home() {
        return this.home;
    }

    public @Nullable BlockPos nest() {
        return this.nest;
    }

    /** Sie erwacht (wenn sie in Andacht hängt). */
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

    /** Die Sterne des laufenden Sternbilds (für Tests). */
    public List<Vec3> constellation() {
        return List.copyOf(this.stars);
    }

    /** Ist dieses Wesen gerade eingesponnen? */
    public boolean isRooted(LivingEntity entity) {
        return this.rooted.containsKey(entity.getUUID());
    }

    /** Wie weit die Fäden der Spindel gerade gedreht sind (Grad). */
    public float spindleAngle() {
        return this.spindleAngle;
    }

    private void begin(Attack next) {
        Attack previous = this.attack;
        this.attack = next;
        this.attackTick = 0;
        this.entityData.set(DATA_ATTACK, next.ordinal());
        if (next != Attack.NONE && next != Attack.DORMANT && next != Attack.AWAKEN && next != Attack.FURY) {
            this.lastAttack = next;
        }
        this.struck.clear();
        this.reeled = null;
        if (next != Attack.CONSTELLATION) {
            this.stars.clear();
        }
        this.setNoGravity(next == Attack.DORMANT);
        if (previous == Attack.DROP || next == Attack.DROP) {
            this.dropMark = null;
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
    public void makeStuckInBlock(BlockState state, Vec3 speedMultiplier) {
        // ihr eigenes Netz hält sie nicht
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

    /** In Andacht weckt sie ein Treffer (unverwundbar); beim Erwachen ist sie unverwundbar; Stürze und Wände schaden ihr nie. */
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
        if (source.getEntity() instanceof StarSpiderlingEntity || source.getEntity() == this) {
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

    /** Sternenstaub rieselt von ihr, die Spinnwarzen glimmen; im Zorn sprühen Funken von den Kristallen. */
    private void clientEffects() {
        Level level = this.level();
        Attack a = this.currentAttack();
        if (this.random.nextInt(a == Attack.DORMANT ? 10 : 4) == 0) {
            level.addParticle(new DustParticleOptions(0xB8D4FF, 0.9f), this.getRandomX(1.0), this.getY(0.3 + this.random.nextDouble() * 0.6),
                    this.getRandomZ(1.0), 0.0, -0.02, 0.0);
        }
        if (this.isEnraged() && this.random.nextInt(3) == 0) {
            level.addParticle(ParticleTypes.END_ROD, this.getRandomX(0.8), this.getY(0.95), this.getRandomZ(0.8), 0.0, 0.05, 0.0);
        }
        if (a == Attack.DORMANT && this.random.nextInt(4) == 0) {
            // der Faden, an dem sie hängt
            double y = this.getY() + 1.6 + this.random.nextDouble() * 8.0;
            level.addParticle(ParticleTypes.END_ROD, this.getX(), y, this.getZ(), 0.0, 0.0, 0.0);
        }
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        this.bossBar.setProgress(this.getHealth() / this.getMaxHealth());
        this.tickRooted(level);
        this.tickStarfall(level);
        if (this.attack == Attack.DORMANT) {
            this.tickDormant(level);
            return;
        }
        if (this.attack == Attack.AWAKEN) {
            this.tickAwaken(level);
            return;
        }
        if (this.tickCount % 10 == 0 && this.attack != Attack.DROP) {
            this.keepOnWeb(level);
        }
        if (!this.isEnraged() && this.attack != Attack.FURY && this.attack != Attack.DROP && this.getHealth() < this.getMaxHealth() * 0.5f) {
            this.getNavigation().stop();
            this.begin(Attack.FURY);
        }
        if (this.attack == Attack.FURY) {
            this.attackTick++;
            this.getNavigation().stop();
            this.tickFury(level);
            if (this.attackTick >= Attack.FURY.duration) {
                this.begin(Attack.SPINDLE);
            }
            return;
        }
        LivingEntity target = this.findTarget(level);
        this.setTarget(target);
        if (target == null && this.attack != Attack.DROP && this.attack != Attack.SPINDLE) {
            this.idle(level);
            return;
        }
        this.idleTicks = 0;
        if (target != null) {
            this.getLookControl().setLookAt(target, 30.0f, 30.0f);
        }
        if (this.attack == Attack.NONE) {
            this.move(level, target);
            if (--this.cooldown <= 0) {
                this.getNavigation().stop();
                this.begin(this.chooseAttack(target));
            }
            return;
        }
        this.attackTick++;
        this.getNavigation().stop();
        if (target != null && this.attack != Attack.SPINDLE && this.attack != Attack.DROP) {
            this.lookAt(target, 30.0f, 30.0f);
        }
        switch (this.attack) {
            case BITE -> this.tickBite(level, target);
            case THREAD -> this.tickThread(level, target);
            case WEB -> this.tickWeb(level, target);
            case CONSTELLATION -> this.tickConstellation(level, target);
            case STARFALL -> this.tickStarfallCast(level, target);
            case COCOON -> this.tickCocoon(level, target);
            case BROOD -> this.tickBrood(level, target);
            case DROP -> this.tickDrop(level, target);
            case SPINDLE -> this.tickSpindle(level);
            default -> { }
        }
        if (this.attackTick >= this.attack.duration) {
            this.setNoGravity(false);
            this.begin(Attack.NONE);
            this.cooldown = this.isEnraged() ? 10 + this.random.nextInt(12) : 18 + this.random.nextInt(16);
        }
    }

    /** Wen ihre Angriffe treffen: alle Lebewesen außer ihr, ihrer Brut und Spielern im Kreativ-/Zuschauermodus. */
    public boolean canStrike(LivingEntity entity) {
        return entity != this && entity.isAlive() && !(entity instanceof StarWeaverEntity) && !(entity instanceof StarSpiderlingEntity)
                && !(entity instanceof ArmorStand) && !(entity instanceof Player player && (player.isCreative() || player.isSpectator()));
    }

    private List<LivingEntity> victims(ServerLevel level, AABB area) {
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

    /** Die Mitte ihres Netzes (ihr Platz, sonst wo sie steht). */
    private Vec3 center() {
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

    /** Wo ihr Maul ist (vorn zwischen den Kieferklauen, wie im Modell). */
    public Vec3 mouthPos() {
        float yaw = this.yBodyRot * Mth.DEG_TO_RAD;
        Vec3 fwd = new Vec3(-Mth.sin(yaw), 0.0, Mth.cos(yaw));
        return this.position().add(fwd.scale(1.15)).add(0.0, 0.95, 0.0);
    }

    /** Wo ihre Spinnwarzen sind (hinten oben am Hinterleib). */
    public Vec3 spinneretPos() {
        float yaw = this.yBodyRot * Mth.DEG_TO_RAD;
        Vec3 fwd = new Vec3(-Mth.sin(yaw), 0.0, Mth.cos(yaw));
        return this.position().add(fwd.scale(-2.9)).add(0.0, 1.15, 0.0);
    }

    /** Ein Sprung wie eine Spinne: Ist das Ziel fern, setzt sie mit einem Satz heran; sonst läuft sie (Abstand drei bis vier Blöcke). */
    private void move(ServerLevel level, @Nullable LivingEntity target) {
        if (target == null) {
            return;
        }
        double distance = this.flatDistance(target);
        if (--this.leapCooldown <= 0 && distance > 8.0 && distance < 18.0 && this.onGround() && this.random.nextInt(12) == 0) {
            Vec3 dir = this.toward(target);
            double k = Math.min(1.25, 0.55 + distance * 0.05);
            this.setDeltaMovement(dir.x * k, 0.62, dir.z * k);
            this.needsSync = true;
            this.leapCooldown = 70;
            this.sound(level, ModSounds.STAR_WEAVER_STEP.get(), 2.0f, 0.7f);
            return;
        }
        if (distance > 3.4) {
            this.getNavigation().moveTo(target, this.isEnraged() ? 1.25 : 1.05);
        } else {
            this.getNavigation().stop();
        }
    }

    /** Fortgelockt (oder vom Netz gefallen): Sie schießt einen Faden und zieht sich zurück auf ihr Netz. */
    private void keepOnWeb(ServerLevel level) {
        if (this.home == null) {
            return;
        }
        double dx = this.getX() - (this.home.getX() + 0.5);
        double dz = this.getZ() - (this.home.getZ() + 0.5);
        if (dx * dx + dz * dz < ARENA * ARENA && this.getY() > this.home.getY() - 6.0 && this.getY() < this.home.getY() + 14.0) {
            return;
        }
        Vec3 back = Vec3.atBottomCenterOf(this.home);
        SpellFxEntity.create(level, SpellFxEntity.Kind.BEAM, this.position().add(0.0, 1.0, 0.0), null, 0xCFE4FF, 10, 0.08f)
                .end(back.add(0.0, 1.0, 0.0)).spawn();
        level.sendParticles(ParticleTypes.END_ROD, this.getX(), this.getY(0.5), this.getZ(), 20, 0.8, 0.6, 0.8, 0.04);
        this.getNavigation().stop();
        this.setNoGravity(false);
        this.teleportTo(back.x, back.y, back.z);
        this.setDeltaMovement(Vec3.ZERO);
        level.sendParticles(ParticleTypes.END_ROD, back.x, back.y + 1.0, back.z, 30, 1.0, 0.6, 1.0, 0.05);
        this.sound(level, ModSounds.STAR_WEAVER_THREAD.get(), 2.5f, 0.8f);
    }

    /** Ohne Gegner: zurück zu ihrem Platz; nach einer Weile klettert sie wieder an ihren Faden – geheilt, ohne Zorn. */
    private void idle(ServerLevel level) {
        if (this.attack != Attack.NONE) {
            this.setNoGravity(false);
            this.begin(Attack.NONE);
        }
        this.idleTicks++;
        if (this.home != null && this.distanceToSqr(Vec3.atBottomCenterOf(this.home)) > 2.0) {
            this.getNavigation().moveTo(this.home.getX() + 0.5, this.home.getY(), this.home.getZ() + 0.5, 0.9);
        } else if (this.home != null && this.idleTicks > RETURN_TICKS) {
            this.climbUp(level);
            return;
        }
        if (this.idleTicks > 200 && this.tickCount % 20 == 0) {
            this.heal(10.0f);
        }
    }

    /** Sie klettert an ihren Faden hinauf und hängt wieder in Andacht – ganz geheilt, nicht mehr im Zorn. */
    private void climbUp(ServerLevel level) {
        this.getNavigation().stop();
        this.setHealth(this.getMaxHealth());
        this.entityData.set(DATA_ENRAGED, false);
        this.idleTicks = 0;
        this.sound(level, ModSounds.STAR_WEAVER_THREAD.get(), 2.0f, 0.7f);
        this.begin(Attack.DORMANT);
        if (this.home != null) {
            this.snapTo(this.home.getX() + 0.5, this.home.getY() + HANG, this.home.getZ() + 0.5, this.getYRot(), 0.0f);
        }
        this.setDeltaMovement(Vec3.ZERO);
    }

    private Attack chooseAttack(@Nullable LivingEntity target) {
        if (target == null) {
            return Attack.STARFALL;
        }
        double distance = this.flatDistance(target);
        List<Attack> options = new ArrayList<>();
        if (distance < 4.6) {
            options.addAll(List.of(Attack.BITE, Attack.BITE, Attack.COCOON, Attack.COCOON, Attack.CONSTELLATION, Attack.BROOD));
        } else if (distance < 11.0) {
            options.addAll(List.of(Attack.THREAD, Attack.WEB, Attack.WEB, Attack.CONSTELLATION, Attack.STARFALL, Attack.DROP, Attack.BROOD));
        } else {
            options.addAll(List.of(Attack.THREAD, Attack.THREAD, Attack.WEB, Attack.STARFALL, Attack.STARFALL, Attack.DROP));
        }
        if (this.isEnraged()) {
            options.add(Attack.SPINDLE);
            options.add(Attack.STARFALL);
        }
        if (this.brood((ServerLevel) this.level()) >= MAX_BROOD - 2) {
            options.removeIf(a -> a == Attack.BROOD);
        }
        options.removeIf(a -> a == this.lastAttack);
        if (options.isEmpty()) {
            options.add(distance < 5.0 ? Attack.BITE : Attack.THREAD);
        }
        return options.get(this.random.nextInt(options.size()));
    }

    private int brood(ServerLevel level) {
        return level.getEntitiesOfClass(StarSpiderlingEntity.class, this.getBoundingBox().inflate(40.0), Entity::isAlive).size();
    }

    private void sound(ServerLevel level, SoundEvent sound, float volume, float pitch) {
        level.playSound(null, this.getX(), this.getY(), this.getZ(), sound, SoundSource.HOSTILE, volume, pitch);
    }

    /** Ein Hauch Sternenstaub an einer Stelle. */
    private void stardust(ServerLevel level, Vec3 at, int count, double spread) {
        level.sendParticles(new DustParticleOptions(0xB8D4FF, 1.4f), at.x, at.y, at.z, count, spread, spread * 0.6, spread, 0.0);
        level.sendParticles(ParticleTypes.END_ROD, at.x, at.y, at.z, count / 3, spread, spread * 0.6, spread, 0.03);
    }

    // ------------------------------------------------------------------ Andacht und Erwachen

    /** Sie hängt kopfüber an ihrem Faden und dreht sich langsam: Wer näher als vierzehn Blöcke kommt, weckt sie. */
    private void tickDormant(ServerLevel level) {
        this.getNavigation().stop();
        this.setDeltaMovement(Vec3.ZERO);
        if (this.home != null && this.distanceToSqr(this.home.getX() + 0.5, this.home.getY() + HANG, this.home.getZ() + 0.5) > 0.25) {
            this.snapTo(this.home.getX() + 0.5, this.home.getY() + HANG, this.home.getZ() + 0.5, this.getYRot(), 0.0f);
        }
        float yaw = this.getYRot() + 0.6f;
        this.setYRot(yaw);
        this.yBodyRot = yaw;
        this.yHeadRot = yaw;
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
            if (dx * dx + dz * dz < WAKE_RADIUS * WAKE_RADIUS && dy > -HANG - 6.0 && dy < 8.0) {
                this.awaken();
                return;
            }
        }
    }

    /** Ihre Augen flammen auf, der Faden reißt – sie stürzt auf ihr Netz, kreischt und richtet sich auf. */
    private void tickAwaken(ServerLevel level) {
        int t = ++this.attackTick;
        this.getNavigation().stop();
        Player nearest = level.getNearestPlayer(this, 40.0);
        if (t == 2) {
            this.sound(level, ModSounds.STAR_WEAVER_AWAKEN.get(), 3.5f, 1.0f);
            this.stardust(level, this.position().add(0.0, 1.0, 0.0), 30, 1.0);
        }
        if (t == 10) {
            this.setNoGravity(false);
            this.setDeltaMovement(0.0, -0.6, 0.0);
            this.needsSync = true;
        }
        if (t > 10 && t < 40 && this.onGround() && !this.struck.contains(this.getUUID())) {
            this.struck.add(this.getUUID());
            this.landing(level, 0.0f);
        }
        if (nearest != null && t > 20) {
            this.getLookControl().setLookAt(nearest, 10.0f, 30.0f);
            if (t > 30) {
                this.lookAt(nearest, 8.0f, 30.0f);
            }
        }
        if (t == 40) {
            Component title = Component.translatable("boss.kingdomsatwar.star_weaver").withColor(0xB8D4FF);
            Component subtitle = Component.translatable("boss.kingdomsatwar.star_weaver.title").withStyle(ChatFormatting.GRAY);
            for (ServerPlayer player : level.players()) {
                if (player.distanceToSqr(this) < 64.0 * 64.0) {
                    player.connection.send(new ClientboundSetTitlesAnimationPacket(15, 60, 25));
                    player.connection.send(new ClientboundSetTitleTextPacket(title));
                    player.connection.send(new ClientboundSetSubtitleTextPacket(subtitle));
                }
            }
        }
        if (t >= Attack.AWAKEN.duration) {
            this.setNoGravity(false);
            this.begin(Attack.NONE);
            this.cooldown = 14;
            this.idleTicks = 0;
        }
    }

    /** Der Aufprall auf dem Netz: ein Ring aus Sternenlicht, Staub; wer nah ist, wird getroffen ({@code damage} > 0). */
    private void landing(ServerLevel level, float damage) {
        this.sound(level, ModSounds.STAR_WEAVER_DROP.get(), 3.5f, 0.9f);
        SpellFxEntity.create(level, SpellFxEntity.Kind.SHOCKWAVE, this.position().add(0.0, 0.05, 0.0), null, 0xB8D4FF, 16, 1.6f)
                .param(0.6f).spawn();
        this.stardust(level, this.position().add(0.0, 0.3, 0.0), 60, 1.6);
        level.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, 0xFFB8D4FF), this.getX(), this.getY() + 0.8, this.getZ(), 1, 0.0, 0.0,
                0.0, 0.0);
        if (damage <= 0.0f) {
            return;
        }
        for (LivingEntity victim : this.victims(level, this.getBoundingBox().inflate(DROP_RADIUS, 2.0, DROP_RADIUS))) {
            double d = this.flatDistance(victim);
            if (d > DROP_RADIUS + 0.8 || victim.getY() < this.getY() - 1.5 || victim.getY() > this.getY() + 3.0) {
                continue;
            }
            if (victim.hurtServer(level, this.damageSources().mobAttack(this), damage)) {
                Vec3 out = d < 1.0e-3 ? this.forward() : new Vec3(victim.getX() - this.getX(), 0.0, victim.getZ() - this.getZ()).normalize();
                victim.push(out.x * 1.0, 0.6, out.z * 1.0);
                victim.needsSync = true;
            }
        }
    }

    // ------------------------------------------------------------------ Angriffe

    /** Giftbiss: ein Satz nach vorn, die Giftzähne schlagen zu (Gift). */
    private void tickBite(ServerLevel level, @Nullable LivingEntity target) {
        int t = this.attackTick;
        if (target == null) {
            return;
        }
        if (t == 6) {
            Vec3 dir = this.toward(target);
            this.setDeltaMovement(dir.x * 0.55, 0.12, dir.z * 0.55);
            this.needsSync = true;
        }
        if (t == 11) {
            this.sound(level, ModSounds.STAR_WEAVER_BITE.get(), 2.5f, 1.0f);
            if (target.distanceTo(this) < 4.6) {
                float damage = this.isEnraged() ? BITE_DAMAGE + 2.0f : BITE_DAMAGE;
                if (target.hurtServer(level, this.damageSources().mobAttack(this), damage)) {
                    target.addEffect(new MobEffectInstance(MobEffects.POISON, 80, 1), this);
                    Vec3 out = this.toward(target);
                    target.push(out.x * 0.5, 0.2, out.z * 0.5);
                    target.needsSync = true;
                }
            }
        }
    }

    /** Fangfaden: Ein Faden aus Sternenlicht trifft das Ziel und spult es zu ihren Zähnen – am Ende beißt sie zu. */
    private void tickThread(ServerLevel level, @Nullable LivingEntity target) {
        int t = this.attackTick;
        if (t == 6) {
            this.sound(level, ModSounds.STAR_WEAVER_THREAD.get(), 2.5f, 1.0f);
        }
        if (target == null) {
            return;
        }
        if (t == 12 && target.distanceTo(this) < THREAD_RANGE && this.hasLineOfSight(target)) {
            this.reeled = target.getUUID();
            level.sendParticles(ParticleTypes.END_ROD, target.getX(), target.getY(0.6), target.getZ(), 14, 0.3, 0.4, 0.3, 0.04);
        }
        if (this.reeled == null || !target.getUUID().equals(this.reeled)) {
            return;
        }
        if (t >= 12 && t <= 32) {
            if (t % 2 == 0) {
                SpellFxEntity.create(level, SpellFxEntity.Kind.BEAM, this.mouthPos(), null, 0xE4F0FF, 3, 0.07f).end(target.getEyePosition().add(0.0,
                        -0.4, 0.0)).spawn();
            }
            Vec3 to = this.mouthPos().subtract(target.position()).multiply(1.0, 0.0, 1.0);
            double d = to.length();
            if (d > 2.4) {
                double k = Math.min(0.9, 0.3 + d * 0.05);
                target.setDeltaMovement(to.scale(k / d).add(0.0, target.onGround() ? 0.18 : 0.0, 0.0));
                target.needsSync = true;
                target.resetFallDistance();
            }
            if (t == 12) {
                target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 2), this);
            }
        }
        if (t == 34 && target.distanceTo(this) < 4.4) {
            this.sound(level, ModSounds.STAR_WEAVER_BITE.get(), 2.5f, 1.1f);
            float damage = this.isEnraged() ? THREAD_DAMAGE + 2.0f : THREAD_DAMAGE;
            if (target.hurtServer(level, this.damageSources().mobAttack(this), damage)) {
                target.addEffect(new MobEffectInstance(MobEffects.POISON, 60, 0), this);
                Vec3 out = this.toward(target);
                target.push(out.x * 0.6, 0.25, out.z * 0.6);
                target.needsSync = true;
            }
        }
    }

    /** Netzwurf: Sie spuckt einen Ballen Sternennetz (im Zorn drei im Fächer): wo er landet, klebt man fest. */
    private void tickWeb(ServerLevel level, @Nullable LivingEntity target) {
        int t = this.attackTick;
        if (t == 6) {
            this.sound(level, ModSounds.STAR_WEAVER_WEB.get(), 2.5f, 1.0f);
        }
        if (t != 12 || target == null) {
            return;
        }
        int shots = this.isEnraged() ? 3 : 1;
        Vec3 from = this.mouthPos();
        for (int i = 0; i < shots; i++) {
            Vec3 aim = target.position().add(target.getDeltaMovement().scale(8.0));
            if (i > 0) {
                Vec3 side = this.toward(target).yRot(Mth.HALF_PI).scale(i == 1 ? 3.0 : -3.0);
                aim = aim.add(side);
            }
            StarWebShotEntity shot = new StarWebShotEntity(level, this);
            shot.setPos(from.x, from.y, from.z);
            Vec3 d = aim.subtract(from);
            double flat = Math.hypot(d.x, d.z);
            shot.shoot(d.x, d.y + flat * 0.22, d.z, (float) Math.min(1.6, 0.8 + flat * 0.06), 1.0f);
            level.addFreshEntity(shot);
        }
        level.sendParticles(ParticleTypes.END_ROD, from.x, from.y, from.z, 8, 0.2, 0.2, 0.2, 0.05);
    }

    /**
     * Sternbild: Um ihr Ziel leuchten nacheinander Sterne auf, ein Faden verbindet sie zu einem Sternbild – dann flammen die
     * Linien auf: Wer eine berührt, wird getroffen.
     */
    private void tickConstellation(ServerLevel level, @Nullable LivingEntity target) {
        int t = this.attackTick;
        if (t == 4) {
            this.sound(level, ModSounds.STAR_WEAVER_CONSTELLATION.get(), 3.0f, 1.0f);
            Vec3 center = target != null ? target.position() : this.position();
            int n = this.isEnraged() ? 7 : 6;
            double start = this.random.nextDouble() * Mth.TWO_PI;
            this.stars.clear();
            for (int i = 0; i < n; i++) {
                double a = start + i * Mth.TWO_PI / n + (this.random.nextDouble() - 0.5) * 0.5;
                double r = CONSTELLATION_RADIUS * (0.75 + this.random.nextDouble() * 0.4);
                Vec3 p = center.add(Math.cos(a) * r, 0.0, Math.sin(a) * r);
                this.stars.add(new Vec3(p.x, this.groundAt(level, p), p.z));
            }
            // ein Stern in der Mitte – dorthin führt der letzte Faden
            this.stars.add(new Vec3(center.x, this.groundAt(level, center), center.z));
        }
        if (this.stars.isEmpty()) {
            return;
        }
        int lit = Math.min(this.stars.size(), (t - 6) / 4 + 1);
        if (t >= 6 && t <= 6 + this.stars.size() * 4 && (t - 6) % 4 == 0) {
            Vec3 s = this.stars.get(lit - 1);
            level.sendParticles(ParticleTypes.END_ROD, s.x, s.y + 0.6, s.z, 10, 0.1, 0.3, 0.1, 0.03);
            level.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, 0xFFE4F0FF), s.x, s.y + 0.6, s.z, 1, 0.0, 0.0, 0.0, 0.0);
            if (lit >= 2) {
                Vec3 p = this.stars.get(lit - 2);
                SpellFxEntity.create(level, SpellFxEntity.Kind.BEAM, p.add(0.0, 0.5, 0.0), null, 0x9AB8EE, 50 - (t - 6), 0.05f)
                        .end(s.add(0.0, 0.5, 0.0)).spawn();
            }
        }
        if (t > 6 && t % 3 == 0) {
            for (int i = 0; i < lit; i++) {
                Vec3 s = this.stars.get(i);
                level.sendParticles(ParticleTypes.END_ROD, s.x, s.y + 0.6, s.z, 1, 0.05, 0.05, 0.05, 0.0);
            }
        }
        int flare = 8 + this.stars.size() * 4 + 12;
        if (t == flare) {
            this.sound(level, ModSounds.STAR_WEAVER_CONSTELLATION.get(), 3.5f, 1.4f);
            float damage = this.isEnraged() ? CONSTELLATION_DAMAGE + 2.0f : CONSTELLATION_DAMAGE;
            for (int i = 1; i < this.stars.size(); i++) {
                Vec3 a = this.stars.get(i - 1);
                Vec3 b = this.stars.get(i);
                SpellFxEntity.create(level, SpellFxEntity.Kind.BEAM, a.add(0.0, 0.5, 0.0), null, 0xCFE4FF, 12, 0.32f).end(b.add(0.0, 0.5, 0.0))
                        .spawn();
                level.sendParticles(ParticleTypes.END_ROD, b.x, b.y + 0.6, b.z, 12, 0.2, 0.3, 0.2, 0.08);
            }
            for (LivingEntity victim : this.victims(level, this.boxAround(this.stars).inflate(1.5, 2.5, 1.5))) {
                if (this.struck.contains(victim.getUUID())) {
                    continue;
                }
                for (int i = 1; i < this.stars.size(); i++) {
                    Vec3 a = this.stars.get(i - 1);
                    Vec3 b = this.stars.get(i);
                    double dy = victim.getY() - Math.min(a.y, b.y);
                    if (dy > 2.4 || dy < -1.2 || segmentDistance(victim.getX(), victim.getZ(), a, b) > 0.95) {
                        continue;
                    }
                    if (victim.hurtServer(level, this.damageSources().indirectMagic(this, this), damage)) {
                        victim.addEffect(new MobEffectInstance(MobEffects.GLOWING, 100, 0), this);
                        victim.push(0.0, 0.5, 0.0);
                        victim.needsSync = true;
                    }
                    this.struck.add(victim.getUUID());
                    break;
                }
            }
        }
    }

    /** Waagerechter Abstand eines Punkts von einer Strecke. */
    public static double segmentDistance(double x, double z, Vec3 a, Vec3 b) {
        double dx = b.x - a.x;
        double dz = b.z - a.z;
        double len = dx * dx + dz * dz;
        double k = len < 1.0e-6 ? 0.0 : Mth.clamp(((x - a.x) * dx + (z - a.z) * dz) / len, 0.0, 1.0);
        return Math.hypot(x - (a.x + dx * k), z - (a.z + dz * k));
    }

    private AABB boxAround(List<Vec3> points) {
        AABB box = new AABB(points.getFirst(), points.getFirst());
        for (Vec3 p : points) {
            box = box.minmax(new AABB(p, p));
        }
        return box;
    }

    /** Der Grund unter einer Stelle (ihr Netz oder Fels), höchstens ein paar Blöcke über oder unter ihr. */
    private double groundAt(ServerLevel level, Vec3 at) {
        BlockPos pos = BlockPos.containing(at.x, this.getY() + 3.0, at.z);
        for (int i = 0; i < 9; i++) {
            if (!level.getBlockState(pos.below()).getCollisionShape(level, pos.below()).isEmpty()) {
                return pos.getY();
            }
            pos = pos.below();
        }
        return this.getY();
    }

    /** Sternschnuppen: Sie trommelt auf ihr Netz – die Sterne, die darin hängen, fallen auf leuchtende Kreise. */
    private void tickStarfallCast(ServerLevel level, @Nullable LivingEntity target) {
        int t = this.attackTick;
        if (t == 4) {
            this.sound(level, ModSounds.STAR_WEAVER_STARFALL.get(), 3.0f, 1.0f);
        }
        if (t >= 6 && t <= 44 && t % 4 == 0) {
            // sie trommelt mit den Vorderbeinen auf ihr Netz
            Vec3 f = this.position().add(this.forward().scale(1.8));
            level.sendParticles(ParticleTypes.END_ROD, f.x, this.getY() + 0.2, f.z, 3, 0.6, 0.05, 0.6, 0.04);
        }
        int every = this.isEnraged() ? 3 : 4;
        if (t < 10 || t > 42 || (t - 10) % every != 0) {
            return;
        }
        List<LivingEntity> marks = this.victims(level, new AABB(this.center(), this.center()).inflate(ARENA + 4.0, 10.0, ARENA + 4.0));
        marks.removeIf(e -> !(e instanceof Player));
        LivingEntity aim = marks.isEmpty() ? target : marks.get(this.random.nextInt(marks.size()));
        Vec3 base = aim != null ? aim.position() : this.position();
        double a = this.random.nextDouble() * Mth.TWO_PI;
        double r = this.random.nextDouble() < 0.35 ? 0.0 : 1.5 + this.random.nextDouble() * 3.5;
        Vec3 mark = base.add(Math.cos(a) * r, 0.0, Math.sin(a) * r);
        mark = new Vec3(mark.x, this.groundAt(level, mark), mark.z);
        SpellFxEntity.create(level, SpellFxEntity.Kind.CIRCLE, mark.add(0.0, 0.05, 0.0), null, 0xB8D4FF, 18, (float) StarfallEntity.RADIUS)
                .spawn();
        this.starfall.add(new double[] {mark.x, mark.y, mark.z, this.tickCount + 6});
    }

    /** Die angekündigten Sterne fallen (sechs Ticks nach ihrem Kreis, aus großer Höhe – sie schlagen ein, wenn er voll ist). */
    private void tickStarfall(ServerLevel level) {
        if (this.starfall.isEmpty()) {
            return;
        }
        Iterator<double[]> it = this.starfall.iterator();
        while (it.hasNext()) {
            double[] s = it.next();
            if (this.tickCount < s[3]) {
                continue;
            }
            StarfallEntity star = new StarfallEntity(level, this);
            star.setDamage(this.isEnraged() ? STARFALL_DAMAGE + 2.0f : STARFALL_DAMAGE);
            star.dropOnto(new Vec3(s[0], s[1], s[2]), 15.0 + this.random.nextDouble() * 3.0);
            level.addFreshEntity(star);
            it.remove();
        }
    }

    /** Umgarnen: Sie packt ihr Ziel und spinnt es ein – es kann sich eine Weile nicht rühren und verliert Kraft. */
    private void tickCocoon(ServerLevel level, @Nullable LivingEntity target) {
        int t = this.attackTick;
        if (t == 6) {
            this.sound(level, ModSounds.STAR_WEAVER_COCOON.get(), 2.5f, 1.0f);
        }
        if (t == 14 && target != null && target.distanceTo(this) < 6.5) {
            this.root(level, target, this.isEnraged() ? COCOON_TICKS + 10 : COCOON_TICKS);
        }
    }

    /** Spinnt ein Wesen ein: Es kann sich nicht rühren (nicht laufen, nicht springen), bis die Zeit um ist. */
    public void root(ServerLevel level, LivingEntity victim, int ticks) {
        this.rooted.put(victim.getUUID(), this.tickCount + ticks);
        victim.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, ticks, 6, false, false, true), this);
        AttributeInstance jump = victim.getAttribute(Attributes.JUMP_STRENGTH);
        if (jump != null && !jump.hasModifier(ROOT_ID)) {
            jump.addTransientModifier(new AttributeModifier(ROOT_ID, -1.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
        victim.setDeltaMovement(0.0, Math.min(0.0, victim.getDeltaMovement().y), 0.0);
        victim.needsSync = true;
        level.sendParticles(ParticleTypes.END_ROD, victim.getX(), victim.getY(0.5), victim.getZ(), 16, 0.4, 0.6, 0.4, 0.02);
    }

    /** Die Eingesponnenen: Fäden wirbeln um sie, alle halbe Sekunde ein wenig Schaden; dann reißen die Fäden. */
    private void tickRooted(ServerLevel level) {
        if (this.rooted.isEmpty()) {
            return;
        }
        Iterator<Map.Entry<UUID, Integer>> it = this.rooted.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, Integer> e = it.next();
            Entity entity = level.getEntity(e.getKey());
            boolean over = !(entity instanceof LivingEntity victim) || !victim.isAlive() || this.tickCount >= e.getValue() || this.isRemoved()
                    || this.isDeadOrDying();
            if (over) {
                if (entity instanceof LivingEntity victim) {
                    AttributeInstance jump = victim.getAttribute(Attributes.JUMP_STRENGTH);
                    if (jump != null) {
                        jump.removeModifier(ROOT_ID);
                    }
                    level.sendParticles(ParticleTypes.END_ROD, victim.getX(), victim.getY(0.5), victim.getZ(), 12, 0.4, 0.6, 0.4, 0.06);
                }
                it.remove();
                continue;
            }
            LivingEntity victim = (LivingEntity) entity;
            int left = e.getValue() - this.tickCount;
            double a = this.tickCount * 0.6;
            for (int k = 0; k < 2; k++) {
                double h = ((this.tickCount + k * 7) % 14) / 14.0 * victim.getBbHeight();
                double aa = a + k * Math.PI;
                level.sendParticles(ParticleTypes.END_ROD, victim.getX() + Math.cos(aa) * 0.55, victim.getY() + h, victim.getZ() + Math.sin(aa) * 0.55,
                        1, 0.0, 0.0, 0.0, 0.0);
            }
            if (left % 10 == 0) {
                victim.hurtServer(level, this.damageSources().indirectMagic(this, this), 3.0f);
                victim.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 0), this);
            }
        }
    }

    /** Brut: Ihr Hinterleib pulsiert, und Sternspinnlinge krabbeln heraus. */
    private void tickBrood(ServerLevel level, @Nullable LivingEntity target) {
        int t = this.attackTick;
        if (t == 6) {
            this.sound(level, ModSounds.STAR_WEAVER_BROOD.get(), 2.5f, 1.0f);
        }
        if (t < 14 || t > 30 || (t - 14) % 8 != 0 || this.brood(level) >= MAX_BROOD) {
            return;
        }
        Vec3 from = this.spinneretPos();
        StarSpiderlingEntity spiderling = ModEntities.STAR_SPIDERLING.get().create(level, EntitySpawnReason.MOB_SUMMONED);
        if (spiderling == null) {
            return;
        }
        double a = this.random.nextDouble() * Mth.TWO_PI;
        spiderling.snapTo(from.x, from.y - 0.6, from.z, (float) Math.toDegrees(a), 0.0f);
        spiderling.setDeltaMovement(Math.cos(a) * 0.35, 0.35, Math.sin(a) * 0.35);
        if (target != null) {
            spiderling.setTarget(target);
        }
        level.addFreshEntity(spiderling);
        this.stardust(level, from, 12, 0.4);
    }

    /**
     * Abseilen: Sie schießt einen Faden hinauf und klettert daran empor, hängt kopfüber, gleitet über ihr Ziel (ein Kreis am
     * Grund zeigt, wohin) und lässt sich fallen.
     */
    private void tickDrop(ServerLevel level, @Nullable LivingEntity target) {
        int t = this.attackTick;
        Vec3 c = this.center();
        if (t == 2) {
            this.sound(level, ModSounds.STAR_WEAVER_THREAD.get(), 2.5f, 0.8f);
            this.setNoGravity(true);
            SpellFxEntity.create(level, SpellFxEntity.Kind.BEAM, this.position().add(0.0, 1.0, 0.0), null, 0xE4F0FF, DROP_FALL, 0.06f)
                    .end(this.position().add(0.0, 16.0, 0.0)).spawn();
        }
        if (t < DROP_HANG) {
            double top = (this.home != null ? this.home.getY() : this.getY()) + 10.0;
            this.setDeltaMovement(0.0, this.getY() < top ? 0.45 : 0.0, 0.0);
            this.needsSync = true;
            return;
        }
        if (t == DROP_HANG) {
            Vec3 aim = target != null ? target.position() : c;
            Vec3 mark = new Vec3(aim.x, this.groundAt(level, new Vec3(aim.x, this.getY() - 8.0, aim.z)), aim.z);
            // nur auf ihr Netz (sonst stürzte sie ins Nichts)
            BlockPos below = BlockPos.containing(mark.x, mark.y - 0.5, mark.z);
            if (this.home != null && (level.getBlockState(below).getCollisionShape(level, below).isEmpty()
                    || Math.hypot(mark.x - c.x, mark.z - c.z) > ARENA - 2.0)) {
                mark = new Vec3(c.x, c.y, c.z);
            }
            this.dropMark = mark;
            SpellFxEntity.create(level, SpellFxEntity.Kind.CIRCLE, mark.add(0.0, 0.05, 0.0), null, 0xB8D4FF, DROP_FALL - DROP_HANG + 4,
                    (float) DROP_RADIUS).spawn();
        }
        if (t < DROP_FALL && this.dropMark != null) {
            // über das Ziel gleiten (es bleibt der Kreis am Grund)
            Vec3 to = new Vec3(this.dropMark.x - this.getX(), 0.0, this.dropMark.z - this.getZ());
            double d = to.length();
            Vec3 step = d > 0.6 ? to.scale(Math.min(0.55, d) / d) : Vec3.ZERO;
            this.setDeltaMovement(step.x, 0.0, step.z);
            this.needsSync = true;
            return;
        }
        if (t == DROP_FALL) {
            this.setNoGravity(false);
            this.setDeltaMovement(0.0, -1.6, 0.0);
            this.needsSync = true;
            this.sound(level, ModSounds.STAR_WEAVER_DROP.get(), 2.0f, 1.3f);
        }
        if (t > DROP_FALL && !this.struck.contains(this.getUUID()) && (this.onGround() || t > DROP_FALL + 24)) {
            this.struck.add(this.getUUID());
            this.landing(level, this.isEnraged() ? DROP_DAMAGE + 2.0f : DROP_DAMAGE);
        }
    }

    /** Die Spindel (im Zorn): Sie dreht sich, und vier Fäden fegen dicht über den Grund – wer nicht springt, wird getroffen. */
    private void tickSpindle(ServerLevel level) {
        int t = this.attackTick;
        if (t == 2) {
            this.sound(level, ModSounds.STAR_WEAVER_SPINDLE.get(), 3.5f, 1.0f);
            this.spindleAngle = this.random.nextFloat() * 90.0f;
            this.spindleHits.clear();
        }
        if (t < 10 || t > Attack.SPINDLE.duration - 6) {
            return;
        }
        float speed = t < 30 ? 1.2f + (t - 10) * 0.05f : 2.4f;
        this.spindleAngle = (this.spindleAngle + speed) % 360.0f;
        float yaw = this.spindleAngle + 45.0f;
        this.setYRot(yaw);
        this.yBodyRot = yaw;
        this.yHeadRot = yaw;
        double y = this.getY() + 0.45;
        if (t % 2 == 0) {
            for (int i = 0; i < 4; i++) {
                double a = Math.toRadians(this.spindleAngle + i * 90.0);
                Vec3 end = new Vec3(this.getX() + Math.cos(a) * SPINDLE_RADIUS, y, this.getZ() + Math.sin(a) * SPINDLE_RADIUS);
                SpellFxEntity.create(level, SpellFxEntity.Kind.BEAM, new Vec3(this.getX(), y, this.getZ()), null, 0xE4F0FF, 3, 0.09f).end(end).spawn();
            }
        }
        if (t % 6 == 0) {
            this.sound(level, ModSounds.STAR_WEAVER_SPINDLE.get(), 1.2f, 1.6f);
        }
        for (LivingEntity victim : this.victims(level, this.getBoundingBox().inflate(SPINDLE_RADIUS + 1.0, 2.0, SPINDLE_RADIUS + 1.0))) {
            double dx = victim.getX() - this.getX();
            double dz = victim.getZ() - this.getZ();
            double d = Math.hypot(dx, dz);
            if (d > SPINDLE_RADIUS + 0.4 || d < 1.6 || victim.getY() > this.getY() + 0.85 || victim.getY() < this.getY() - 1.5) {
                continue;
            }
            Integer last = this.spindleHits.get(victim.getUUID());
            if (last != null && this.tickCount - last < 12) {
                continue;
            }
            double va = Math.toDegrees(Math.atan2(dz, dx));
            boolean hit = false;
            for (int i = 0; i < 4 && !hit; i++) {
                double diff = Mth.wrapDegrees(va - (this.spindleAngle + i * 90.0));
                hit = Math.abs(Math.toRadians(diff)) * d < 0.75 + victim.getBbWidth() * 0.5;
            }
            if (!hit) {
                continue;
            }
            this.spindleHits.put(victim.getUUID(), this.tickCount);
            if (victim.hurtServer(level, this.damageSources().indirectMagic(this, this), SPINDLE_DAMAGE)) {
                Vec3 tangent = new Vec3(-dz, 0.0, dx).normalize();
                victim.push(tangent.x * 0.7, 0.42, tangent.z * 0.7);
                victim.needsSync = true;
            }
        }
    }

    /** Ihr Zorn (unter halber Kraft, einmal): Sie kreischt, die Sterne auf ihrem Rücken flammen auf – dann dreht sich die Spindel. */
    private void tickFury(ServerLevel level) {
        int t = this.attackTick;
        if (t == 1) {
            this.entityData.set(DATA_ENRAGED, true);
            this.sound(level, ModSounds.STAR_WEAVER_FURY.get(), 4.0f, 1.0f);
            for (ServerPlayer player : level.players()) {
                if (player.distanceToSqr(this) < 40.0 * 40.0) {
                    player.sendOverlayMessage(Component.translatable("message.kingdomsatwar.star_weaver.fury").withColor(0xB8D4FF));
                }
            }
        }
        if (t == 12) {
            SpellFxEntity.create(level, SpellFxEntity.Kind.SHOCKWAVE, this.position().add(0.0, 0.05, 0.0), null, 0xB8D4FF, 18, 1.4f)
                    .param(0.8f).spawn();
            this.stardust(level, this.position().add(0.0, 1.6, 0.0), 80, 1.4);
            level.sendParticles(ParticleTypes.FIREWORK, this.getX(), this.getY(0.8), this.getZ(), 40, 1.2, 0.8, 1.2, 0.12);
        }
    }

    // ------------------------------------------------------------------ Tod, Speichern, Klänge

    @Override
    public void die(DamageSource source) {
        this.setNoGravity(false);
        super.die(source);
        if (this.level() instanceof ServerLevel level) {
            for (UUID id : List.copyOf(this.rooted.keySet())) {
                this.rooted.put(id, 0);
            }
            this.tickRooted(level);
            if (this.nest != null && level.getBlockEntity(this.nest) instanceof WeaverNestBlockEntity nestEntity && nestEntity.owns(this)) {
                nestEntity.onWeaverDefeated(level);
            }
            for (StarSpiderlingEntity spiderling : level.getEntitiesOfClass(StarSpiderlingEntity.class, this.getBoundingBox().inflate(48.0))) {
                spiderling.vanish();
            }
        }
    }

    /** Sie rollt die Beine ein (im Modell), Sternenstaub rieselt – dann zerfällt sie zu Sternenlicht. */
    @Override
    protected void tickDeath() {
        this.deathTime++;
        this.setDeltaMovement(0.0, Math.min(0.0, this.getDeltaMovement().y), 0.0);
        if (!(this.level() instanceof ServerLevel level)) {
            return;
        }
        if (this.deathTime % 4 == 0) {
            this.stardust(level, this.position().add(0.0, 0.8, 0.0), 6, 1.0);
        }
        if (this.deathTime == DEATH_TICKS - 2) {
            this.sound(level, ModSounds.STAR_WEAVER_DEATH.get(), 4.0f, 1.0f);
            this.stardust(level, this.position().add(0.0, 1.0, 0.0), 160, 1.8);
            level.sendParticles(ParticleTypes.FIREWORK, this.getX(), this.getY(0.6), this.getZ(), 60, 1.2, 0.8, 1.2, 0.15);
            level.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, 0xFFB8D4FF), this.getX(), this.getY() + 1.0, this.getZ(), 1, 0.0,
                    0.0, 0.0, 0.0);
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
        if (this.nest != null) {
            output.putLong("Nest", this.nest.asLong());
        }
        output.putBoolean("Enraged", this.isEnraged());
        output.putBoolean("Dormant", this.attack == Attack.DORMANT);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        input.getLong("Home").ifPresent(h -> this.home = BlockPos.of(h));
        input.getLong("Nest").ifPresent(n -> this.nest = BlockPos.of(n));
        this.entityData.set(DATA_ENRAGED, input.getBooleanOr("Enraged", false));
        if (input.getBooleanOr("Dormant", false)) {
            this.begin(Attack.DORMANT);
        } else {
            this.setNoGravity(false);
        }
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return this.attack == Attack.DORMANT ? null : ModSounds.STAR_WEAVER_AMBIENT.get();
    }

    @Override
    public int getAmbientSoundInterval() {
        return 140;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.STAR_WEAVER_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.STAR_WEAVER_DEATH.get();
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(ModSounds.STAR_WEAVER_STEP.get(), 0.7f, 0.9f + this.random.nextFloat() * 0.25f);
    }

    @Override
    protected float getSoundVolume() {
        return 2.5f;
    }

    // ------------------------------------------------------------------ Himmelswacht

    @Override
    public io.github.sirschrolli.kingdomsatwar.entity.boss.sky.SkyTheme skyTheme() {
        return io.github.sirschrolli.kingdomsatwar.entity.boss.sky.SkyTheme.STAR;
    }
}
