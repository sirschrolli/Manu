package io.github.sirschrolli.kingdomsatwar.entity.fae;

import io.github.sirschrolli.kingdomsatwar.block.fae.KeeperStoneBlockEntity;
import io.github.sirschrolli.kingdomsatwar.magic.fx.SpellFxEntity;
import io.github.sirschrolli.kingdomsatwar.registry.FaeBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.ModParticles;
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
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.decoration.ArmorStand;
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
 * Der Steinhüter (seit 0.96): der Wächter des alten Mondsteinbruchs im Feenreich. Das Volk, das hier lange vor den Feen
 * Mondstein brach, schichtete ihn aus Feenstein und band ihn mit Mondstein; seither kniet er am Grund des Bruchs, bemoost
 * wie ein Fels, und wacht. Tritt jemand an den Grund (oder schlägt ihn), erwacht er – und kämpft:
 * <ul>
 *     <li><b>Faustschlag</b> – beide Fäuste über den Kopf und vor sich in den Boden; der Boden reißt auf;</li>
 *     <li><b>Stampfer</b> – eine Erschütterung läuft als Ring über den Grund (wer im rechten Moment springt, entgeht ihr);</li>
 *     <li><b>Felswurf</b> – er reißt einen Felsbrocken aus dem Boden und schleudert ihn im Bogen;</li>
 *     <li><b>Kristallspur</b> – er rammt die Fäuste in den Boden, und Mondsteinkristalle brechen in einer Spur auf seinen Gegner
 *     zu hervor (unter halber Kraft in drei Spuren);</li>
 *     <li><b>Felssprung</b> – steht sein Gegner fern oder auf einer höheren Stufe des Bruchs, springt er ihm nach und landet
 *     mit Wucht;</li>
 *     <li><b>Kristallpanzer</b> – unter halber Kraft wachsen Kristalle über ihn: Er nimmt viel weniger Schaden, bis der Panzer
 *     zerspringt – dann ist er eine Weile benommen und verwundbarer als je;</li>
 *     <li><b>Mondstrahl</b> – unter halber Kraft öffnet sich das Hüterherz in seiner Brust, und ein Strahl aus Mondlicht folgt
 *     seinem Gegner (Deckung hilft).</li>
 * </ul>
 * Findet er niemanden mehr, kehrt er an seinen Platz zurück, kniet nieder und ist wieder heil. Besiegt zerfällt er zu Geröll;
 * ein Mondstein auf dem Hüterstein ruft einen neuen.
 */
public class StoneKeeperEntity extends Monster implements io.github.sirschrolli.kingdomsatwar.entity.boss.sky.SkyWarden {
    public static final float MAX_HEALTH = 440.0f;
    /** So lange (Ticks) dauert sein Sterben (er sinkt auf die Knie und zerfällt). */
    public static final int DEATH_TICKS = 70;
    /** So viel Schaden (vor dem Abzug) fängt der Kristallpanzer ab, bis er zerspringt. */
    public static final float ARMOR_HEALTH = 70.0f;
    /** Mit dem Kristallpanzer kommt nur dieser Teil des Schadens durch … */
    public static final float ARMOR_FACTOR = 0.4f;
    /** … und benommen trifft man ihn um die Hälfte härter. */
    public static final float STAGGER_FACTOR = 1.5f;
    /** So nah (waagerecht) muss man an ihn herantreten, damit er erwacht. */
    public static final double WAKE_RADIUS = 11.0;
    /** Weiter als so weit von seinem Platz folgt er niemandem. */
    private static final double LEASH = 34.0;
    /** Wer ihn weiter fort lockt (oder wohin er stürzt), dem entkommt er: Er zerfällt und setzt sich an seinem Platz neu zusammen. */
    private static final double ARENA = 27.0;
    /** So lange ohne Gegner, dann kniet er an seinem Platz nieder. */
    private static final int RETURN_TICKS = 400;
    /** Felssprung: so viele Ticks in der Luft. */
    public static final int LEAP_FLIGHT = 18;
    /** Mondstrahl: so weit reicht er. */
    public static final double BEAM_RANGE = 26.0;

    public enum Attack {
        NONE(0), DORMANT(0), AWAKEN(70), SMASH(36), STOMP(36), BOULDER(54), CRYSTALS(52), BEAM(82), LEAP(40), LAND(26), ARMOR(52),
        STAGGER(64);

        public final int duration;

        Attack(int duration) {
            this.duration = duration;
        }
    }

    private static final EntityDataAccessor<Integer> DATA_ATTACK = SynchedEntityData.defineId(StoneKeeperEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_ARMORED = SynchedEntityData.defineId(StoneKeeperEntity.class,
            EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_ENRAGED = SynchedEntityData.defineId(StoneKeeperEntity.class,
            EntityDataSerializers.BOOLEAN);

    private final ServerBossEvent bossBar = new ServerBossEvent(UUID.randomUUID(), Component.translatable("boss.kingdomsatwar.keeper"),
            BossEvent.BossBarColor.BLUE, BossEvent.BossBarOverlay.NOTCHED_10);
    private Attack attack = Attack.NONE;
    private Attack lastAttack = Attack.NONE;
    private int attackTick;
    private int cooldown = 30;
    private int idleTicks;
    private float armorHealth;
    private @Nullable BlockPos home;
    private @Nullable BlockPos lair;
    private final Set<UUID> slamHit = new HashSet<>();
    private @Nullable Vec3 leapFrom;
    private @Nullable Vec3 leapTo;
    private @Nullable Vec3 beamDir;
    private @Nullable SpellFxEntity beamFx;
    private int clientAttackStart;

    public StoneKeeperEntity(EntityType<? extends StoneKeeperEntity> type, Level level) {
        super(type, level);
        this.xpReward = 300;
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, MAX_HEALTH)
                .add(Attributes.ARMOR, 10.0)
                .add(Attributes.ARMOR_TOUGHNESS, 3.0)
                .add(Attributes.ATTACK_DAMAGE, 11.0)
                .add(Attributes.MOVEMENT_SPEED, 0.24)
                .add(Attributes.FOLLOW_RANGE, 40.0)
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
        builder.define(DATA_ARMORED, false);
        builder.define(DATA_ENRAGED, false);
    }

    public Attack currentAttack() {
        int i = this.entityData.get(DATA_ATTACK);
        return i >= 0 && i < Attack.values().length ? Attack.values()[i] : Attack.NONE;
    }

    /** Trägt er den Kristallpanzer? */
    public boolean isArmored() {
        return this.entityData.get(DATA_ARMORED);
    }

    /** Unter halber Kraft: Kristallspur in drei Spuren, Mondstrahl, schnellere Angriffe. */
    public boolean isEnraged() {
        return this.entityData.get(DATA_ENRAGED);
    }

    /** Kniet er schlafend an seinem Platz? */
    public boolean isDormant() {
        return this.currentAttack() == Attack.DORMANT;
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

    /** Bindet ihn an seinen Platz am Grund des Bruchs (und den Hüterstein davor): Er kniet und schläft. */
    public void bindTo(BlockPos home, @Nullable BlockPos lair) {
        this.home = home.immutable();
        this.lair = lair == null ? null : lair.immutable();
        this.begin(Attack.DORMANT);
    }

    /** Er setzt sich gerade erst zusammen (gerufen mit einem Mondstein): sofort wach. */
    public void assemble(BlockPos home, @Nullable BlockPos lair) {
        this.home = home.immutable();
        this.lair = lair == null ? null : lair.immutable();
        this.begin(Attack.AWAKEN);
    }

    public @Nullable BlockPos home() {
        return this.home;
    }

    public @Nullable BlockPos lair() {
        return this.lair;
    }

    /** Er erwacht (wenn er schläft). */
    public void awaken() {
        if (this.attack == Attack.DORMANT) {
            this.begin(Attack.AWAKEN);
        }
    }

    /** Beginnt sofort einen bestimmten Angriff (für Tests und die Vorführung). */
    public void forceAttack(Attack next) {
        if (next == Attack.ARMOR) {
            this.entityData.set(DATA_ENRAGED, true);
        }
        this.begin(next);
    }

    /** Unter halber Kraft (für Tests und die Vorführung). */
    public void forceEnraged() {
        this.entityData.set(DATA_ENRAGED, true);
    }

    /** Legt den Kristallpanzer an (für Tests und die Vorführung). */
    public void forceArmor() {
        this.entityData.set(DATA_ENRAGED, true);
        this.entityData.set(DATA_ARMORED, true);
        this.armorHealth = ARMOR_HEALTH;
    }

    private void begin(Attack next) {
        if (this.attack == Attack.BEAM && next != Attack.BEAM) {
            this.stopBeam();
        }
        this.attack = next;
        this.attackTick = 0;
        this.entityData.set(DATA_ATTACK, next.ordinal());
        if (next != Attack.NONE && next != Attack.AWAKEN && next != Attack.DORMANT && next != Attack.ARMOR && next != Attack.STAGGER
                && next != Attack.LAND) {
            this.lastAttack = next;
        }
        this.slamHit.clear();
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
     * Schlafend und beim Erwachen ist er unverwundbar (wer ihn im Schlaf schlägt, weckt ihn); Stürze und Wände schaden ihm
     * nie. Der Kristallpanzer lässt nur zwei Fünftel durch und fängt den Rest, bis er zerspringt; benommen trifft man ihn
     * härter.
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
        if (this.attack == Attack.AWAKEN || source.is(DamageTypes.IN_WALL) || source.is(DamageTypes.FALL)) {
            return false;
        }
        float dealt = amount;
        if (this.isArmored()) {
            dealt = amount * ARMOR_FACTOR;
            this.armorHealth -= amount;
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, FaeBlocks.MOONSTONE_BLOCK.get().defaultBlockState()), this.getX(),
                    this.getY(0.6), this.getZ(), 10, 0.8, 0.9, 0.8, 0.1);
            level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.AMETHYST_BLOCK_HIT, SoundSource.HOSTILE, 2.0f, 0.7f);
            if (this.armorHealth <= 0.0f) {
                this.shatter(level);
            }
        } else if (this.attack == Attack.STAGGER) {
            dealt = amount * STAGGER_FACTOR;
        }
        boolean hurt = super.hurtServer(level, source, dealt);
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

    /** Mondfunken steigen von seinen Kristallen auf; schlafend kaum, mit dem Kristallpanzer viele. */
    private void clientEffects() {
        Level level = this.level();
        Attack a = this.currentAttack();
        if (a == Attack.DORMANT) {
            if (this.random.nextInt(14) == 0) {
                level.addParticle(ModParticles.FAE_SPARK.get(), this.getRandomX(1.0), this.getY(0.4 + this.random.nextDouble() * 0.5),
                        this.getRandomZ(1.0), 0.0, 0.01, 0.0);
            }
            return;
        }
        if (this.random.nextInt(this.isArmored() ? 1 : 4) == 0) {
            level.addParticle(ParticleTypes.END_ROD, this.getRandomX(0.9), this.getY(0.55 + this.random.nextDouble() * 0.45), this.getRandomZ(0.9),
                    0.0, 0.02, 0.0);
        }
        if (this.walkAnimation.speed() > 0.2f && this.random.nextInt(3) == 0) {
            BlockState below = level.getBlockState(this.blockPosition().below());
            if (!below.isAir()) {
                level.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, below), this.getRandomX(0.8), this.getY() + 0.1, this.getRandomZ(0.8),
                        0.0, 0.1, 0.0);
            }
        }
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        this.bossBar.setProgress(this.getHealth() / this.getMaxHealth());
        if (this.attack == Attack.DORMANT) {
            this.tickDormant(level);
            return;
        }
        if (this.attack == Attack.AWAKEN) {
            this.tickAwaken(level);
            return;
        }
        if (this.tickCount % 10 == 0 && this.attack != Attack.LEAP) {
            this.keepInQuarry(level);
        }
        if (!this.isEnraged() && this.attack != Attack.ARMOR && this.attack != Attack.LEAP && this.attack != Attack.LAND
                && this.getHealth() < this.getMaxHealth() * 0.5f) {
            this.getNavigation().stop();
            this.begin(Attack.ARMOR);
        }
        if (this.attack == Attack.ARMOR) {
            this.tickArmor(level);
            return;
        }
        if (this.attack == Attack.STAGGER) {
            this.tickStagger(level);
            return;
        }
        if (this.attack == Attack.LEAP) {
            this.tickLeap(level);
            return;
        }
        if (this.attack == Attack.LAND) {
            this.tickLand(level);
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
            double distance = this.flatDistance(target);
            if (distance > 3.8) {
                this.getNavigation().moveTo(target, this.isEnraged() ? 1.15 : 1.0);
            } else {
                this.getNavigation().stop();
            }
            if (--this.cooldown <= 0) {
                this.getNavigation().stop();
                this.begin(this.chooseAttack(target));
            }
            return;
        }
        this.attackTick++;
        this.getNavigation().stop();
        this.lookAt(target, 30.0f, 30.0f);
        switch (this.attack) {
            case SMASH -> this.tickSmash(level);
            case STOMP -> this.tickStomp(level);
            case BOULDER -> this.tickBoulder(level, target);
            case CRYSTALS -> this.tickCrystals(level, target);
            case BEAM -> this.tickBeam(level, target);
            default -> { }
        }
        if (this.attackTick >= this.attack.duration) {
            this.begin(Attack.NONE);
            this.cooldown = this.isEnraged() ? 12 + this.random.nextInt(14) : 22 + this.random.nextInt(18);
        }
    }

    /** Wen seine Angriffe treffen: alle Lebewesen außer ihm und Spielern im Kreativ-/Zuschauermodus. */
    boolean canStrike(LivingEntity entity) {
        return entity != this && entity.isAlive() && !(entity instanceof StoneKeeperEntity) && !(entity instanceof ArmorStand)
                && !(entity instanceof Player player && (player.isCreative() || player.isSpectator()));
    }

    private List<LivingEntity> victims(ServerLevel level, AABB area) {
        return level.getEntitiesOfClass(LivingEntity.class, area, this::canStrike);
    }

    private double flatDistance(Entity other) {
        return Math.hypot(other.getX() - this.getX(), other.getZ() - this.getZ());
    }

    private @Nullable LivingEntity findTarget(ServerLevel level) {
        if (this.home != null && this.distanceToSqr(Vec3.atBottomCenterOf(this.home)) > LEASH * LEASH) {
            return null;
        }
        LivingEntity current = this.getTarget();
        if (current != null && current.isAlive() && current.distanceToSqr(this) < 44.0 * 44.0 && this.canStrike(current)
                && (this.home == null || current.distanceToSqr(Vec3.atBottomCenterOf(this.home)) < LEASH * LEASH)) {
            return current;
        }
        Vec3 from = this.home != null ? Vec3.atBottomCenterOf(this.home) : this.position();
        return level.getNearestPlayer(from.x, from.y, from.z, 30.0,
                e -> e instanceof Player p && !p.isCreative() && !p.isSpectator() && p.isAlive());
    }

    /** Fortgelockt oder hinabgestürzt: Er zerfällt zu Geröll und setzt sich an seinem Platz neu zusammen. */
    private void keepInQuarry(ServerLevel level) {
        if (this.home == null) {
            return;
        }
        double dx = this.getX() - (this.home.getX() + 0.5);
        double dz = this.getZ() - (this.home.getZ() + 0.5);
        if (dx * dx + dz * dz < ARENA * ARENA && this.getY() > this.home.getY() - 6.0) {
            return;
        }
        this.crumble(level, 60);
        this.getNavigation().stop();
        this.teleportTo(this.home.getX() + 0.5, this.home.getY(), this.home.getZ() + 0.5);
        this.setDeltaMovement(Vec3.ZERO);
        this.crumble(level, 80);
        this.sound(level, ModSounds.STONE_KEEPER_AWAKEN.get(), 2.5f, 1.2f);
    }

    /** Ohne Gegner: zurück an seinen Platz; nach einer Weile kniet er nieder – geheilt, ohne Panzer. */
    private void idle(ServerLevel level) {
        if (this.attack != Attack.NONE) {
            this.begin(Attack.NONE);
        }
        this.idleTicks++;
        if (this.home != null && this.distanceToSqr(Vec3.atBottomCenterOf(this.home)) > 2.0) {
            this.getNavigation().moveTo(this.home.getX() + 0.5, this.home.getY(), this.home.getZ() + 0.5, 0.9);
        } else if (this.home != null && this.idleTicks > RETURN_TICKS) {
            this.fallAsleep(level);
            return;
        }
        if (this.idleTicks > 200 && this.tickCount % 20 == 0) {
            this.heal(5.0f);
        }
    }

    /** Er kniet nieder und schläft wieder – ganz geheilt, ohne Panzer, nicht mehr im Zorn. */
    private void fallAsleep(ServerLevel level) {
        this.getNavigation().stop();
        this.setHealth(this.getMaxHealth());
        this.entityData.set(DATA_ARMORED, false);
        this.entityData.set(DATA_ENRAGED, false);
        this.idleTicks = 0;
        if (this.home != null) {
            this.snapTo(this.home.getX() + 0.5, this.home.getY(), this.home.getZ() + 0.5, this.getYRot(), 0.0f);
        }
        if (this.lair != null) {
            // er blickt auf den Hüterstein
            double dx = this.lair.getX() + 0.5 - this.getX();
            double dz = this.lair.getZ() + 0.5 - this.getZ();
            float yaw = (float) (Mth.atan2(dz, dx) * Mth.RAD_TO_DEG) - 90.0f;
            this.setYRot(yaw);
            this.yBodyRot = yaw;
            this.yHeadRot = yaw;
        }
        this.sound(level, ModSounds.STONE_KEEPER_AMBIENT.get(), 2.0f, 0.6f);
        this.begin(Attack.DORMANT);
    }

    private Attack chooseAttack(LivingEntity target) {
        double distance = this.flatDistance(target);
        double rise = target.getY() - this.getY();
        List<Attack> options = new ArrayList<>();
        if ((rise > 2.5 && distance < 26.0 || distance > 13.0) && this.lastAttack != Attack.LEAP) {
            options.add(Attack.LEAP);
            options.add(Attack.LEAP);
        }
        if (distance < 5.0) {
            options.addAll(List.of(Attack.SMASH, Attack.SMASH, Attack.STOMP, Attack.CRYSTALS));
        } else if (distance < 10.0) {
            options.addAll(List.of(Attack.STOMP, Attack.CRYSTALS, Attack.CRYSTALS, Attack.BOULDER));
        } else {
            options.addAll(List.of(Attack.BOULDER, Attack.BOULDER, Attack.CRYSTALS));
        }
        if (this.isEnraged() && distance > 4.0) {
            options.add(Attack.BEAM);
            options.add(Attack.BEAM);
        }
        options.removeIf(a -> a == this.lastAttack);
        if (options.isEmpty()) {
            options.add(distance < 6.0 ? Attack.STOMP : Attack.BOULDER);
        }
        return options.get(this.random.nextInt(options.size()));
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

    private BlockParticleOption dust(ServerLevel level) {
        BlockState floor = level.getBlockState(this.blockPosition().below());
        return new BlockParticleOption(ParticleTypes.BLOCK, floor.isAir() ? FaeBlocks.FAESTONE.base().get().defaultBlockState() : floor);
    }

    /** Er zerfällt (oder setzt sich zusammen): Geröll und Mondsteinsplitter fliegen. */
    private void crumble(ServerLevel level, int count) {
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, FaeBlocks.FAESTONE.base().get().defaultBlockState()), this.getX(),
                this.getY(0.5), this.getZ(), count, 0.9, 1.2, 0.9, 0.15);
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, FaeBlocks.MOONSTONE_BLOCK.get().defaultBlockState()), this.getX(),
                this.getY(0.6), this.getZ(), count / 3, 0.8, 1.0, 0.8, 0.12);
        level.sendParticles(ParticleTypes.END_ROD, this.getX(), this.getY(0.6), this.getZ(), count / 4, 0.8, 1.0, 0.8, 0.05);
    }

    // ------------------------------------------------------------------ Schlaf und Erwachen

    /** Er kniet und schläft: Wer an den Grund tritt (näher als elf Blöcke), weckt ihn. */
    private void tickDormant(ServerLevel level) {
        this.getNavigation().stop();
        this.setDeltaMovement(this.getDeltaMovement().multiply(0.0, 1.0, 0.0));
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
            if (dx * dx + dz * dz < WAKE_RADIUS * WAKE_RADIUS && dy > -4.0 && dy < 9.0) {
                this.awaken();
                return;
            }
        }
    }

    /** Er hebt den Kopf, die Augen glimmen auf, er stemmt sich hoch, Moos und Steine rieseln – und er brüllt. */
    private void tickAwaken(ServerLevel level) {
        int t = ++this.attackTick;
        this.getNavigation().stop();
        Player nearest = level.getNearestPlayer(this, 32.0);
        if (nearest != null && t > 10) {
            this.getLookControl().setLookAt(nearest, 10.0f, 30.0f);
            if (t > 30) {
                this.lookAt(nearest, 8.0f, 30.0f);
            }
        }
        if (t == 2) {
            this.sound(level, ModSounds.STONE_KEEPER_AWAKEN.get(), 3.0f, 1.0f);
        }
        if (t > 8 && t < 40 && t % 3 == 0) {
            level.sendParticles(this.dust(level), this.getX(), this.getY(0.9), this.getZ(), 6, 0.9, 0.3, 0.9, 0.05);
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, FaeBlocks.FAE_MOSS.get().defaultBlockState()), this.getX(),
                    this.getY(0.95), this.getZ(), 3, 0.8, 0.2, 0.8, 0.02);
        }
        if (t == 40) {
            this.sound(level, ModSounds.STONE_KEEPER_ROAR.get(), 4.0f, 1.0f);
            level.sendParticles(ParticleTypes.END_ROD, this.getX(), this.getY(0.7), this.getZ(), 60, 1.0, 1.2, 1.0, 0.1);
            level.sendParticles(this.dust(level), this.getX(), this.getY() + 0.2, this.getZ(), 40, 2.0, 0.2, 2.0, 0.1);
            Component title = Component.translatable("boss.kingdomsatwar.keeper").withColor(0xA8D4FF);
            Component subtitle = Component.translatable("boss.kingdomsatwar.keeper.title").withStyle(ChatFormatting.GRAY);
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
            this.cooldown = 16;
            this.idleTicks = 0;
        }
    }

    // ------------------------------------------------------------------ Angriffe

    /** Faustschlag: beide Fäuste hoch – und vor sich in den Boden; der Boden reißt auf, wer davor steht, fliegt. */
    private void tickSmash(ServerLevel level) {
        int t = this.attackTick;
        if (t == 4) {
            this.sound(level, ModSounds.STONE_KEEPER_ROAR.get(), 1.6f, 1.3f);
        }
        if (t != 16) {
            return;
        }
        Vec3 dir = this.forward();
        Vec3 hit = this.position().add(dir.scale(2.8));
        this.sound(level, ModSounds.STONE_KEEPER_SMASH.get(), 3.5f, 0.95f);
        level.sendParticles(ParticleTypes.EXPLOSION, hit.x, this.getY() + 0.3, hit.z, 2, 0.5, 0.0, 0.5, 0.0);
        level.sendParticles(this.dust(level), hit.x, this.getY() + 0.2, hit.z, 50, 1.2, 0.2, 1.2, 0.2);
        SpellFxEntity.create(level, SpellFxEntity.Kind.FISSURE, hit.add(0.0, 0.05, 0.0), null, 0x8EC8FF, 34, 0.7f).param(0.0f)
                .end(hit.add(dir.scale(4.5)).add(0.0, 0.05, 0.0)).spawn();
        SpellFxEntity.create(level, SpellFxEntity.Kind.SHOCKWAVE, new Vec3(hit.x, this.getY() + 0.05, hit.z), null, 0xB8DCFF, 8, 1.0f)
                .param(0.5f).spawn();
        float damage = this.isEnraged() ? 14.0f : 12.0f;
        for (LivingEntity victim : this.victims(level, new AABB(hit, hit).inflate(3.4, 2.5, 3.4))) {
            if (victim.distanceToSqr(hit.x, victim.getY(), hit.z) > 3.4 * 3.4 || victim.getY() < this.getY() - 1.5) {
                continue;
            }
            if (victim.hurtServer(level, this.damageSources().mobAttack(this), damage)) {
                Vec3 out = victim.position().subtract(this.position()).multiply(1.0, 0.0, 1.0);
                out = out.lengthSqr() < 1.0e-4 ? dir : out.normalize();
                victim.push(out.x * 1.0, 0.5, out.z * 1.0);
                victim.needsSync = true;
            }
        }
    }

    /** Stampfer: Er stampft auf, und eine Erschütterung läuft als Ring über den Grund – wer darin steht, fliegt. */
    private void tickStomp(ServerLevel level) {
        int t = this.attackTick;
        if (t == 16) {
            this.sound(level, ModSounds.STONE_KEEPER_STOMP.get(), 3.5f, 0.9f);
            SpellFxEntity.create(level, SpellFxEntity.Kind.SHOCKWAVE, this.position().add(0.0, 0.05, 0.0), null, 0xC8E4FF, 14, 1.6f)
                    .param(0.62f).spawn();
            level.sendParticles(ParticleTypes.EXPLOSION, this.getX(), this.getY() + 0.2, this.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
        }
        if (t < 16 || t > 30) {
            return;
        }
        double radius = 1.6 + (t - 16) * 0.62;
        BlockParticleOption dust = this.dust(level);
        int points = 10 + (int) (radius * 5);
        for (int i = 0; i < points; i++) {
            double a = i * Math.PI * 2.0 / points;
            level.sendParticles(dust, this.getX() + Math.cos(a) * radius, this.getY() + 0.1, this.getZ() + Math.sin(a) * radius, 1, 0.1, 0.05, 0.1,
                    0.1);
        }
        float damage = this.isEnraged() ? 10.0f : 8.0f;
        for (LivingEntity victim : this.victims(level, this.getBoundingBox().inflate(radius + 1.0, 2.0, radius + 1.0))) {
            double d = this.flatDistance(victim);
            boolean grounded = victim.onGround() || victim.getY() - this.getY() < SpellFxEntity.SHOCKWAVE_HEIGHT;
            if (Math.abs(d - radius) > 0.95 || !grounded || !this.slamHit.add(victim.getUUID())) {
                continue;
            }
            if (victim.hurtServer(level, this.damageSources().mobAttack(this), damage)) {
                Vec3 out = new Vec3(victim.getX() - this.getX(), 0.0, victim.getZ() - this.getZ()).normalize();
                victim.push(out.x * 0.5, 0.75, out.z * 0.5);
                victim.needsSync = true;
            }
        }
    }

    /** Wo der Felsbrocken über seinem Kopf schwebt (zum Werfen). */
    private Vec3 boulderHold() {
        return this.position().add(this.forward().scale(0.6)).add(0.0, 5.4, 0.0);
    }

    /** Felswurf: Er reißt einen Felsbrocken aus dem Boden, stemmt ihn über den Kopf und schleudert ihn im Bogen. */
    private void tickBoulder(ServerLevel level, LivingEntity target) {
        int t = this.attackTick;
        Vec3 dir = this.forward();
        Vec3 right = dir.yRot(-Mth.HALF_PI);
        if (t == 10) {
            this.sound(level, ModSounds.STONE_KEEPER_DIG.get(), 3.0f, 0.9f);
        }
        if (t >= 10 && t <= 18 && t % 2 == 0) {
            Vec3 hand = this.position().add(dir.scale(2.4)).add(right.scale(1.2));
            level.sendParticles(this.dust(level), hand.x, this.getY() + 0.2, hand.z, 14, 0.5, 0.2, 0.5, 0.15);
        }
        if (t == 36) {
            this.sound(level, ModSounds.STONE_KEEPER_THROW.get(), 3.0f, 0.85f);
            KeeperBoulderEntity boulder = new KeeperBoulderEntity(level, this);
            boulder.setDamage(this.isEnraged() ? 12.0f : 10.0f);
            boulder.setSplit(this.isEnraged());
            Vec3 from = this.boulderHold();
            Vec3 lead = target.position().add(target.getDeltaMovement().multiply(8.0, 0.0, 8.0));
            double flat = Math.hypot(lead.x - from.x, lead.z - from.z);
            boulder.lob(from, lead, Mth.clamp(0.75 + flat * 0.03, 0.8, 1.5));
            level.addFreshEntity(boulder);
            level.sendParticles(this.dust(level), from.x, from.y, from.z, 20, 0.8, 0.6, 0.8, 0.05);
        }
    }

    /** Kristallspur: Er rammt die Fäuste in den Boden – Mondsteinkristalle brechen in einer Spur (im Zorn in drei) hervor. */
    private void tickCrystals(ServerLevel level, LivingEntity target) {
        int t = this.attackTick;
        if (t == 4) {
            this.sound(level, ModSounds.STONE_KEEPER_ROAR.get(), 1.8f, 1.15f);
        }
        if (t != 16) {
            return;
        }
        this.sound(level, ModSounds.STONE_KEEPER_SMASH.get(), 3.0f, 1.15f);
        Vec3 dir = this.toward(target);
        Vec3 start = this.position().add(dir.scale(2.4));
        level.sendParticles(this.dust(level), start.x, this.getY() + 0.2, start.z, 40, 1.0, 0.2, 1.0, 0.2);
        float damage = this.isEnraged() ? 9.0f : 8.0f;
        int lines = this.isEnraged() ? 3 : 1;
        for (int i = 0; i < lines; i++) {
            Vec3 d = lines == 1 ? dir : dir.yRot((float) Math.toRadians((i - 1) * 24.0));
            CrystalLine.start(level, new Vec3(start.x, this.getY(), start.z), d, 12, 1.45, 2, damage, 0.8f, this, this::canStrike);
        }
    }

    /** Wo das Hüterherz sitzt (Ursprung des Mondstrahls): auf der Brust, gut zweieinhalb Blöcke hoch. */
    private Vec3 heart() {
        return this.position().add(this.forward().scale(0.9)).add(0.0, 2.55, 0.0);
    }

    /**
     * Mondstrahl (nur im Zorn): Das Hüterherz sammelt Licht – dann bricht ein Strahl daraus hervor und folgt seinem Gegner,
     * langsamer, als der laufen kann. Wände halten ihn auf.
     */
    private void tickBeam(ServerLevel level, LivingEntity target) {
        int t = this.attackTick;
        Vec3 heart = this.heart();
        if (t == 2) {
            this.sound(level, ModSounds.STONE_KEEPER_BEAM_CHARGE.get(), 3.0f, 1.0f);
        }
        if (t < 34) {
            // das Licht strömt ins Herz
            for (int i = 0; i < 3; i++) {
                Vec3 from = heart.add((this.random.nextDouble() - 0.5) * 5.0, (this.random.nextDouble() - 0.5) * 4.0,
                        (this.random.nextDouble() - 0.5) * 5.0);
                Vec3 v = heart.subtract(from).scale(0.12);
                level.sendParticles(ParticleTypes.END_ROD, from.x, from.y, from.z, 0, v.x, v.y, v.z, 1.0);
            }
            return;
        }
        Vec3 aim = target.getEyePosition().subtract(heart).normalize();
        if (t == 34) {
            this.beamDir = aim;
            this.sound(level, ModSounds.STONE_KEEPER_BEAM.get(), 3.5f, 1.0f);
            level.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, 0xFFB8DCFF), heart.x, heart.y, heart.z, 1, 0.0, 0.0, 0.0, 0.0);
            this.beamFx = SpellFxEntity.create(level, SpellFxEntity.Kind.BEAM, heart, null, 0x9ED0FF, Attack.BEAM.duration - 34, 0.55f)
                    .end(heart.add(aim.scale(4.0))).onTick(fx -> {
                        if (this.attack != Attack.BEAM || this.isRemoved() || !this.isAlive()) {
                            fx.discard();
                        }
                    }).spawn();
        }
        if (t > 70 || this.beamDir == null) {
            this.stopBeam();
            return;
        }
        // der Strahl schwenkt dem Ziel nach (höchstens 3,5° je Tick)
        Vec3 dir = this.beamDir;
        double angle = Math.acos(Mth.clamp(dir.dot(aim), -1.0, 1.0));
        double max = Math.toRadians(this.isArmored() ? 3.0 : 3.5);
        if (angle > 1.0e-4) {
            double k = Math.min(1.0, max / angle);
            dir = dir.add(aim.subtract(dir).scale(k)).normalize();
        }
        this.beamDir = dir;
        Vec3 far = heart.add(dir.scale(BEAM_RANGE));
        var hit = level.clip(new ClipContext(heart, far, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        Vec3 end = hit.getType() == HitResult.Type.MISS ? far : hit.getLocation();
        if (this.beamFx != null && !this.beamFx.isRemoved()) {
            this.beamFx.setPos(heart);
            this.beamFx.end(end);
        }
        level.sendParticles(ParticleTypes.END_ROD, end.x, end.y, end.z, 3, 0.2, 0.2, 0.2, 0.05);
        if (hit.getType() != HitResult.Type.MISS && t % 3 == 0) {
            BlockState state = level.getBlockState(BlockPos.containing(end.subtract(dir.scale(0.05))));
            if (!state.isAir()) {
                level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, state), end.x, end.y, end.z, 4, 0.2, 0.2, 0.2, 0.1);
            }
        }
        if (t % 4 != 2) {
            return;
        }
        float damage = this.isEnraged() ? 5.0f : 4.0f;
        AABB area = new AABB(heart, end).inflate(1.2);
        for (LivingEntity victim : this.victims(level, area)) {
            Vec3 center = victim.position().add(0.0, victim.getBbHeight() * 0.5, 0.0);
            double along = center.subtract(heart).dot(dir);
            if (along < 0.0 || along > heart.distanceTo(end) + 0.4) {
                continue;
            }
            Vec3 closest = heart.add(dir.scale(along));
            if (closest.distanceTo(center) > 0.8 + victim.getBbWidth() * 0.5) {
                continue;
            }
            if (victim.hurtServer(level, this.damageSources().indirectMagic(this, this), damage)) {
                victim.addEffect(new MobEffectInstance(MobEffects.GLOWING, 60, 0), this);
                victim.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 0), this);
            }
        }
    }

    private void stopBeam() {
        if (this.beamFx != null) {
            this.beamFx.discard();
            this.beamFx = null;
        }
        this.beamDir = null;
    }

    /** Felssprung: tief in die Knie – und er springt seinem Gegner nach (auch auf die Stufen des Bruchs). */
    private void tickLeap(ServerLevel level) {
        int t = ++this.attackTick;
        this.getNavigation().stop();
        LivingEntity target = this.getTarget();
        if (t == 6) {
            this.sound(level, ModSounds.STONE_KEEPER_ROAR.get(), 2.0f, 1.2f);
        }
        if (t < 14) {
            if (target != null) {
                this.lookAt(target, 30.0f, 30.0f);
            }
            return;
        }
        if (t == 14) {
            if (target == null || !target.isAlive()) {
                this.begin(Attack.NONE);
                this.cooldown = 10;
                return;
            }
            this.leapFrom = this.position();
            Vec3 to = target.position().add(target.getDeltaMovement().multiply(6.0, 0.0, 6.0));
            Vec3 flat = to.subtract(this.leapFrom).multiply(1.0, 0.0, 1.0);
            double length = flat.length();
            if (length > 24.0) {
                to = this.leapFrom.add(flat.scale(24.0 / length)).add(0.0, to.y - this.leapFrom.y, 0.0);
            }
            this.leapTo = to;
            this.sound(level, ModSounds.STONE_KEEPER_STOMP.get(), 2.5f, 1.3f);
            level.sendParticles(this.dust(level), this.getX(), this.getY() + 0.2, this.getZ(), 40, 1.2, 0.2, 1.2, 0.2);
        }
        if (this.leapFrom == null || this.leapTo == null) {
            this.begin(Attack.NONE);
            return;
        }
        float u = Math.min(1.0f, (t - 14) / (float) LEAP_FLIGHT);
        double height = 4.0 + Math.min(5.0, this.leapFrom.distanceTo(this.leapTo) * 0.22) + Math.max(0.0, this.leapTo.y - this.leapFrom.y);
        Vec3 next = this.leapFrom.lerp(this.leapTo, u).add(0.0, height * 4.0 * u * (1.0 - u), 0.0);
        this.setDeltaMovement(next.subtract(this.position()));
        this.needsSync = true;
        this.fallDistance = 0.0;
        if (u >= 1.0f && (this.onGround() || t > 14 + LEAP_FLIGHT + 6)) {
            this.begin(Attack.LAND);
        }
    }

    /** Landung: Der Boden bebt, wer nahe steht, fliegt. */
    private void tickLand(ServerLevel level) {
        int t = ++this.attackTick;
        this.getNavigation().stop();
        this.setDeltaMovement(this.getDeltaMovement().multiply(0.2, 1.0, 0.2));
        if (t == 2) {
            this.sound(level, ModSounds.STONE_KEEPER_SMASH.get(), 4.0f, 0.75f);
            level.sendParticles(ParticleTypes.EXPLOSION, this.getX(), this.getY() + 0.3, this.getZ(), 3, 1.0, 0.1, 1.0, 0.0);
            level.sendParticles(this.dust(level), this.getX(), this.getY() + 0.2, this.getZ(), 70, 2.2, 0.2, 2.2, 0.25);
            SpellFxEntity.create(level, SpellFxEntity.Kind.SHOCKWAVE, this.position().add(0.0, 0.05, 0.0), null, 0xC8E4FF, 10, 2.0f).param(0.55f)
                    .spawn();
            float damage = this.isEnraged() ? 12.0f : 10.0f;
            for (LivingEntity victim : this.victims(level, this.getBoundingBox().inflate(4.2, 2.0, 4.2))) {
                if (this.flatDistance(victim) > 4.6) {
                    continue;
                }
                if (victim.hurtServer(level, this.damageSources().mobAttack(this), damage)) {
                    Vec3 out = new Vec3(victim.getX() - this.getX(), 0.0, victim.getZ() - this.getZ());
                    out = out.lengthSqr() < 1.0e-4 ? this.forward() : out.normalize();
                    victim.push(out.x * 0.9, 0.6, out.z * 0.9);
                    victim.needsSync = true;
                }
            }
            this.leapFrom = null;
            this.leapTo = null;
        }
        if (t >= Attack.LAND.duration) {
            this.begin(Attack.NONE);
            this.cooldown = this.isEnraged() ? 10 : 18;
        }
    }

    /** Kristallpanzer: Er ballt die Fäuste und brüllt – Kristalle wachsen über Brust, Arme, Beine und Kopf. */
    private void tickArmor(ServerLevel level) {
        int t = ++this.attackTick;
        this.getNavigation().stop();
        this.entityData.set(DATA_ENRAGED, true);
        if (t == 6) {
            this.sound(level, ModSounds.STONE_KEEPER_ROAR.get(), 4.0f, 0.85f);
        }
        if (t >= 12 && t <= 40 && t % 2 == 0) {
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, FaeBlocks.MOONSTONE_BLOCK.get().defaultBlockState()), this.getX(),
                    this.getY(0.5), this.getZ(), 8, 1.0, 1.2, 1.0, 0.05);
            level.sendParticles(ParticleTypes.END_ROD, this.getX(), this.getY(0.5), this.getZ(), 4, 1.0, 1.4, 1.0, 0.03);
        }
        if (t == 20) {
            this.sound(level, ModSounds.STONE_KEEPER_ARMOR.get(), 3.5f, 1.0f);
            this.entityData.set(DATA_ARMORED, true);
            this.armorHealth = ARMOR_HEALTH;
            for (LivingEntity victim : this.victims(level, this.getBoundingBox().inflate(3.5, 1.5, 3.5))) {
                if (victim.hurtServer(level, this.damageSources().mobAttack(this), 5.0f)) {
                    Vec3 out = new Vec3(victim.getX() - this.getX(), 0.0, victim.getZ() - this.getZ());
                    out = out.lengthSqr() < 1.0e-4 ? this.forward() : out.normalize();
                    victim.push(out.x * 1.1, 0.4, out.z * 1.1);
                    victim.needsSync = true;
                }
            }
        }
        if (t >= Attack.ARMOR.duration) {
            this.begin(Attack.NONE);
            this.cooldown = 10;
        }
    }

    /** Der Kristallpanzer zerspringt: Splitter fliegen, und er ist eine Weile benommen. */
    private void shatter(ServerLevel level) {
        this.entityData.set(DATA_ARMORED, false);
        this.armorHealth = 0.0f;
        this.sound(level, ModSounds.STONE_KEEPER_SHATTER.get(), 4.0f, 1.0f);
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, FaeBlocks.MOONSTONE_BLOCK.get().defaultBlockState()), this.getX(),
                this.getY(0.5), this.getZ(), 120, 1.2, 1.4, 1.2, 0.3);
        level.sendParticles(ParticleTypes.END_ROD, this.getX(), this.getY(0.6), this.getZ(), 50, 1.2, 1.4, 1.2, 0.15);
        level.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, 0xFFC8E4FF), this.getX(), this.getY(0.6), this.getZ(), 1, 0.0, 0.0, 0.0,
                0.0);
        this.getNavigation().stop();
        this.begin(Attack.STAGGER);
    }

    /** Benommen: Er sinkt halb auf ein Knie und schwankt (keine Angriffe). */
    private void tickStagger(ServerLevel level) {
        int t = ++this.attackTick;
        this.getNavigation().stop();
        if (t % 8 == 0) {
            level.sendParticles(ParticleTypes.CRIT, this.getX(), this.getY(1.0) + 0.3, this.getZ(), 4, 0.4, 0.2, 0.4, 0.05);
        }
        if (t >= Attack.STAGGER.duration) {
            this.begin(Attack.NONE);
            this.cooldown = 10;
        }
    }

    // ------------------------------------------------------------------ Tod, Speichern, Klänge

    @Override
    public void die(DamageSource source) {
        this.stopBeam();
        super.die(source);
        if (this.level() instanceof ServerLevel level && this.lair != null && level.getBlockEntity(this.lair) instanceof KeeperStoneBlockEntity stone
                && stone.owns(this)) {
            stone.onKeeperDefeated(level);
        }
    }

    /** Er sinkt auf die Knie, das Licht in ihm erlischt – und er zerfällt zu Geröll. */
    @Override
    protected void tickDeath() {
        this.deathTime++;
        this.setDeltaMovement(0.0, this.getDeltaMovement().y, 0.0);
        if (!(this.level() instanceof ServerLevel level)) {
            return;
        }
        if (this.deathTime % 5 == 0) {
            level.sendParticles(this.dust(level), this.getX(), this.getY(0.6), this.getZ(), 8, 0.8, 1.0, 0.8, 0.05);
        }
        if (this.deathTime == DEATH_TICKS / 2) {
            level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.AMETHYST_BLOCK_BREAK, SoundSource.HOSTILE, 3.0f, 0.5f);
        }
        if (this.deathTime == DEATH_TICKS - 2) {
            level.playSound(null, this.getX(), this.getY(), this.getZ(), ModSounds.STONE_KEEPER_DEATH.get(), SoundSource.HOSTILE, 4.0f, 1.0f);
            this.crumble(level, 160);
            level.sendParticles(ParticleTypes.CLOUD, this.getX(), this.getY(0.3), this.getZ(), 30, 1.0, 0.5, 1.0, 0.03);
        }
        if (this.deathTime >= DEATH_TICKS && !this.isRemoved()) {
            this.bossBar.removeAllPlayers();
            this.remove(RemovalReason.KILLED);
        }
    }

    @Override
    public void remove(RemovalReason reason) {
        this.stopBeam();
        super.remove(reason);
        this.bossBar.removeAllPlayers();
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        if (this.home != null) {
            output.putLong("Home", this.home.asLong());
        }
        if (this.lair != null) {
            output.putLong("Lair", this.lair.asLong());
        }
        output.putBoolean("Enraged", this.isEnraged());
        output.putBoolean("Armored", this.isArmored());
        output.putFloat("ArmorHealth", this.armorHealth);
        output.putBoolean("Dormant", this.attack == Attack.DORMANT);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        input.getLong("Home").ifPresent(h -> this.home = BlockPos.of(h));
        input.getLong("Lair").ifPresent(l -> this.lair = BlockPos.of(l));
        this.entityData.set(DATA_ENRAGED, input.getBooleanOr("Enraged", false));
        this.entityData.set(DATA_ARMORED, input.getBooleanOr("Armored", false));
        this.armorHealth = input.getFloatOr("ArmorHealth", 0.0f);
        if (input.getBooleanOr("Dormant", false)) {
            this.begin(Attack.DORMANT);
        }
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return this.attack == Attack.DORMANT ? null : ModSounds.STONE_KEEPER_AMBIENT.get();
    }

    @Override
    public int getAmbientSoundInterval() {
        return 180;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.STONE_KEEPER_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.STONE_KEEPER_DEATH.get();
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(ModSounds.STONE_KEEPER_STEP.get(), 1.0f, 0.9f + this.random.nextFloat() * 0.2f);
    }

    @Override
    protected float getSoundVolume() {
        return 2.5f;
    }

    // ------------------------------------------------------------------ Himmelswacht

    @Override
    public io.github.sirschrolli.kingdomsatwar.entity.boss.sky.SkyTheme skyTheme() {
        return io.github.sirschrolli.kingdomsatwar.entity.boss.sky.SkyTheme.KEEPER;
    }

}
