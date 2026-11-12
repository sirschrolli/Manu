package io.github.sirschrolli.kingdomsatwar.entity.sea;

import io.github.sirschrolli.kingdomsatwar.block.sea.WitchCauldronBlockEntity;
import io.github.sirschrolli.kingdomsatwar.entity.fae.CrystalLine;
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
import net.minecraft.core.Direction;
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
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
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
 * Die Meerhexe (seit 0.97): eine verstoßene Meerjungfrau, die in der Hexengrotte am Grund des Urmeers über ihrem Kessel
 * schwebt und Zauber braut. Sie schläft in einer Trance, bis jemand näher als {@link #WAKE_RADIUS} Blöcke kommt (oder sie
 * schlägt) – dann umkreist sie ihre Gegner im Wasser und greift abwechselnd an:
 * <ul>
 *     <li><b>Tintengeschosse</b> – aus ihrer Stabkugel jagen drei (im Zorn fünf) Kugeln aus Hexentinte, die ihrem Ziel ein
 *     wenig folgen: Schaden und Blindheit;</li>
 *     <li><b>Strudel</b> – ein Kreis warnt, dann dreht sich das Wasser um den Gegner: er wird mitgerissen und zur Mitte gezogen
 *     (wer schnell hinausschwimmt, entkommt);</li>
 *     <li><b>Sirenengesang</b> – sie singt: wer sie dabei ansieht, wird zu ihr hingezogen (und am Ende gepackt) – also wegsehen;</li>
 *     <li><b>Blasengefängnis</b> – eine Blase schließt ihr Ziel ein, presst ihm die Luft ab und platzt;</li>
 *     <li><b>Haie</b> – sie ruft Haie aus der Tiefe (höchstens {@link #MAX_SHARKS});</li>
 *     <li><b>Korallenspieße</b> – sie stößt den Stab hinab, und Korallen brechen in einer Spur (im Zorn in dreien) aus dem
 *     Grund auf ihren Gegner zu;</li>
 *     <li><b>Schwanzhieb</b> – wer ihr zu nahe kommt, den fegt ihr Schwanz fort;</li>
 *     <li><b>Fluch der Tiefe</b> (im Zorn) – ein Hexenkreis, und alle in der Nähe werden schwach und müde; sie zehrt davon.</li>
 * </ul>
 * Unter halber Kraft schreit sie vor Zorn: eine Woge aus Tinte stößt alle fort und verdunkelt das Wasser – danach ist sie
 * schneller und wütender. Findet sie niemanden mehr, kehrt sie über ihren Kessel zurück und versinkt wieder in Trance (geheilt).
 * Besiegt zerfließt sie zu Tinte; der Kessel erkaltet, und eine Perle darin ruft sie zurück.
 */
public class SeaWitchEntity extends Monster implements io.github.sirschrolli.kingdomsatwar.entity.boss.sky.SkyWarden {
    public static final float MAX_HEALTH = 680.0f;
    /** So lange (Ticks) zerfließt sie im Tod. */
    public static final int DEATH_TICKS = 60;
    /** So nah muss man ihr kommen, damit sie aus der Trance erwacht. */
    public static final double WAKE_RADIUS = 10.0;
    /** Weiter als so weit von ihrem Kessel folgt sie niemandem … */
    private static final double LEASH = 30.0;
    /** … und wer sie weiter fortlockt, dem entkommt sie in einer Tintenwolke zurück in ihre Grotte. */
    private static final double ARENA = 24.0;
    /** So lange ohne Gegner, dann versinkt sie wieder in Trance. */
    private static final int RETURN_TICKS = 400;
    /** Blasengefängnis: so lange hält eine Blase. */
    public static final int BUBBLE_LIFE = 50;
    /** Strudel: so lange dreht er sich und so weit reicht er. */
    public static final int WHIRLPOOL_LIFE = 56;
    public static final float WHIRLPOOL_RADIUS = 3.6f;
    /** So viele Haie ruft sie höchstens zugleich. */
    public static final int MAX_SHARKS = 3;
    /** Kennzeichen ihrer Haie (sie greifen die Hexe nicht an und verschwinden mit ihr). */
    public static final String SHARK_TAG = "kingdomsatwar_witch_shark";
    /** Tintengeschosse: Schaden je Kugel. */
    public static final float INK_DAMAGE = 7.0f;
    /** Korallenspieße: Schaden je Spur. */
    public static final float CORAL_DAMAGE = 9.0f;
    /** Schwanzhieb: Schaden und Reichweite. */
    public static final float TAIL_DAMAGE = 11.0f;
    public static final double TAIL_RANGE = 4.6;

    public enum Attack {
        NONE(0), DORMANT(0), AWAKEN(70), INK(40), WHIRLPOOL(64), SONG(84), BUBBLE(40), SHARKS(44), CORAL(46), CURSE(52), TAIL(26), FURY(60);

        public final int duration;

        Attack(int duration) {
            this.duration = duration;
        }
    }

    private static final EntityDataAccessor<Integer> DATA_ATTACK = SynchedEntityData.defineId(SeaWitchEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_ENRAGED = SynchedEntityData.defineId(SeaWitchEntity.class,
            EntityDataSerializers.BOOLEAN);

    private final ServerBossEvent bossBar = new ServerBossEvent(UUID.randomUUID(), Component.translatable("boss.kingdomsatwar.sea_witch"),
            BossEvent.BossBarColor.GREEN, BossEvent.BossBarOverlay.NOTCHED_10);
    private Attack attack = Attack.NONE;
    private Attack lastAttack = Attack.NONE;
    private int attackTick;
    private int cooldown = 30;
    private int idleTicks;
    private int sharkCooldown;
    private float orbit;
    private int orbitDir = 1;
    private @Nullable BlockPos home;
    private @Nullable BlockPos lair;
    private @Nullable Vec3 whirlCenter;
    private final List<UUID> sharks = new ArrayList<>();
    /** Sirenengesang: wen sie gerade zu sich zieht. */
    private final Set<UUID> charmed = new HashSet<>();
    private int clientAttackStart;

    public SeaWitchEntity(EntityType<? extends SeaWitchEntity> type, Level level) {
        super(type, level);
        this.xpReward = 400;
        this.setPersistenceRequired();
        this.setNoGravity(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        // zwischen Karkinos (320 Lebenspunkte) und dem Kraken (1400)
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, MAX_HEALTH)
                .add(Attributes.ARMOR, 8.0)
                .add(Attributes.ARMOR_TOUGHNESS, 3.0)
                .add(Attributes.ATTACK_DAMAGE, 10.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.FOLLOW_RANGE, 48.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.9);
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

    public boolean isEnraged() {
        return this.entityData.get(DATA_ENRAGED);
    }

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

    /** Bindet sie an ihren Platz über dem Kessel und lässt sie in Trance schweben. */
    public void bindTo(BlockPos home, @Nullable BlockPos lair) {
        this.home = home.immutable();
        this.lair = lair != null ? lair.immutable() : null;
        this.begin(Attack.DORMANT);
    }

    /** Wie {@link #bindTo}, aber sie erwacht sofort (aus der Perle im Kessel gerufen). */
    public void rise(BlockPos home, @Nullable BlockPos lair) {
        this.home = home.immutable();
        this.lair = lair != null ? lair.immutable() : null;
        this.begin(Attack.AWAKEN);
    }

    public @Nullable BlockPos home() {
        return this.home;
    }

    public @Nullable BlockPos lair() {
        return this.lair;
    }

    /** Sie erwacht aus der Trance. */
    public void awaken() {
        if (this.attack == Attack.DORMANT) {
            this.begin(Attack.AWAKEN);
        }
    }

    /** Beginnt sofort einen bestimmten Angriff (für Tests und die Vorführung). */
    public void forceAttack(Attack next) {
        this.begin(next);
    }

    /** Sofort im Zorn (für Tests und die Vorführung). */
    public void forceEnraged() {
        this.entityData.set(DATA_ENRAGED, true);
    }

    private void begin(Attack next) {
        this.attack = next;
        this.attackTick = 0;
        this.entityData.set(DATA_ATTACK, next.ordinal());
        if (next != Attack.NONE && next != Attack.DORMANT && next != Attack.AWAKEN) {
            this.lastAttack = next;
        }
        if (next == Attack.SONG) {
            this.charmed.clear();
        }
        this.bossBar.setVisible(next != Attack.DORMANT);
    }

    // ------------------------------------------------------------------ Grundverhalten

    /** Sie schwimmt, wie es ihr gefällt: keine Strömung, kein Auftrieb, nur ihr eigener Schwung. */
    @Override
    public void travel(Vec3 input) {
        this.move(MoverType.SELF, this.getDeltaMovement());
        this.setDeltaMovement(this.getDeltaMovement().scale(0.86));
    }

    @Override
    public boolean isPushable() {
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

    /** In Trance weckt sie ein Schlag (unverwundbar); beim Erwachen ist sie unverwundbar; Stürze, Wände und Wasser schaden ihr nie. */
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
        if (this.attack == Attack.AWAKEN || source.is(DamageTypes.IN_WALL) || source.is(DamageTypes.FALL) || source.is(DamageTypes.DROWN)) {
            return false;
        }
        if (source.getEntity() != null && source.getEntity().entityTags().contains(SHARK_TAG)) {
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

    /** Leuchtende Tintenfunken steigen von ihr auf; in Trance nur wenige, im Zorn viele. */
    private void clientEffects() {
        Level level = this.level();
        Attack a = this.currentAttack();
        int chance = a == Attack.DORMANT ? 10 : this.isEnraged() ? 1 : 3;
        if (this.random.nextInt(chance) == 0) {
            level.addParticle(ParticleTypes.GLOW, this.getRandomX(0.9), this.getY(0.3 + this.random.nextDouble() * 0.7), this.getRandomZ(0.9),
                    0.0, 0.02, 0.0);
        }
        if (a != Attack.DORMANT && this.random.nextInt(4) == 0) {
            level.addParticle(ParticleTypes.BUBBLE, this.getRandomX(0.7), this.getY(0.2 + this.random.nextDouble() * 0.6), this.getRandomZ(0.7),
                    0.0, 0.05, 0.0);
        }
        if (this.isEnraged() && this.random.nextInt(3) == 0) {
            level.addParticle(ParticleTypes.SQUID_INK, this.getRandomX(0.8), this.getY(0.4 + this.random.nextDouble() * 0.5), this.getRandomZ(0.8),
                    0.0, 0.0, 0.0);
        }
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        this.bossBar.setProgress(this.getHealth() / this.getMaxHealth());
        if (this.sharkCooldown > 0) {
            this.sharkCooldown--;
        }
        if (this.tickCount % 20 == 0) {
            this.sharks.removeIf(id -> !(level.getEntity(id) instanceof LivingEntity shark) || !shark.isAlive());
        }
        if (this.attack == Attack.DORMANT) {
            this.tickDormant(level);
            return;
        }
        if (this.attack == Attack.AWAKEN) {
            this.tickAwaken(level);
            return;
        }
        if (this.tickCount % 10 == 0) {
            this.keepInGrotto(level);
        }
        if (!this.isEnraged() && this.attack != Attack.FURY && this.getHealth() < this.getMaxHealth() * 0.5f) {
            this.begin(Attack.FURY);
        }
        if (this.attack == Attack.FURY) {
            this.attackTick++;
            this.brake();
            this.tickFury(level);
            if (this.attackTick >= Attack.FURY.duration) {
                this.begin(Attack.NONE);
                this.cooldown = 10;
            }
            return;
        }
        LivingEntity target = this.findTarget(level);
        this.setTarget(target);
        if (target == null) {
            this.idle(level);
            return;
        }
        this.idleTicks = 0;
        this.face(target);
        if (this.attack == Attack.NONE) {
            this.hover(target);
            if (--this.cooldown <= 0) {
                this.begin(this.chooseAttack(level, target));
            }
            return;
        }
        this.attackTick++;
        this.brake();
        switch (this.attack) {
            case INK -> this.tickInk(level, target);
            case WHIRLPOOL -> this.tickWhirlpool(level, target);
            case SONG -> this.tickSong(level);
            case BUBBLE -> this.tickBubble(level, target);
            case SHARKS -> this.tickSharks(level, target);
            case CORAL -> this.tickCoral(level, target);
            case CURSE -> this.tickCurse(level);
            case TAIL -> this.tickTail(level, target);
            default -> { }
        }
        if (this.attackTick >= this.attack.duration) {
            this.begin(Attack.NONE);
            this.cooldown = this.isEnraged() ? 14 + this.random.nextInt(12) : 24 + this.random.nextInt(16);
        }
    }

    /** Wen ihre Angriffe treffen: alle Lebewesen außer ihr, ihren Haien und Spielern im Kreativ-/Zuschauermodus. */
    public boolean canStrike(LivingEntity entity) {
        return entity != this && entity.isAlive() && !(entity instanceof SeaWitchEntity) && !(entity instanceof ArmorStand)
                && !entity.entityTags().contains(SHARK_TAG)
                && !(entity instanceof Player player && (player.isCreative() || player.isSpectator()));
    }

    private List<LivingEntity> victims(ServerLevel level, AABB area) {
        return level.getEntitiesOfClass(LivingEntity.class, area, this::canStrike);
    }

    private @Nullable LivingEntity findTarget(ServerLevel level) {
        if (this.home != null && this.distanceToSqr(Vec3.atCenterOf(this.home)) > LEASH * LEASH) {
            return null;
        }
        LivingEntity current = this.getTarget();
        if (current != null && current.isAlive() && current.distanceToSqr(this) < 40.0 * 40.0 && this.canStrike(current)
                && (this.home == null || current.distanceToSqr(Vec3.atCenterOf(this.home)) < LEASH * LEASH)) {
            return current;
        }
        Vec3 from = this.home != null ? Vec3.atCenterOf(this.home) : this.position();
        return level.getNearestPlayer(from.x, from.y, from.z, 28.0, e -> e instanceof Player p && !p.isCreative() && !p.isSpectator() && p.isAlive());
    }

    /** Sie blickt ihren Gegner an. */
    private void face(Entity target) {
        double dx = target.getX() - this.getX();
        double dz = target.getZ() - this.getZ();
        double dy = target.getEyeY() - this.getEyeY();
        float yaw = (float) (Mth.atan2(dz, dx) * Mth.RAD_TO_DEG) - 90.0f;
        float pitch = (float) -(Mth.atan2(dy, Math.sqrt(dx * dx + dz * dz)) * Mth.RAD_TO_DEG);
        this.setYRot(Mth.approachDegrees(this.getYRot(), yaw, 12.0f));
        this.yBodyRot = this.getYRot();
        this.yHeadRot = Mth.approachDegrees(this.yHeadRot, yaw, 20.0f);
        this.setXRot(Mth.clamp(pitch, -40.0f, 40.0f));
    }

    /** Langsamer werden (während sie zaubert), dabei leicht auf und ab schweben. */
    private void brake() {
        Vec3 v = this.getDeltaMovement();
        this.setDeltaMovement(v.x * 0.7, v.y * 0.7 + Mth.sin(this.tickCount * 0.12f) * 0.006, v.z * 0.7);
    }

    /** Auf {@code goal} zuschwimmen (sanft beschleunigen, höchstens so schnell wie {@code speed} erlaubt). */
    private void swimTo(Vec3 goal, double speed) {
        Vec3 d = goal.subtract(this.position());
        double len = d.length();
        if (len < 0.2) {
            return;
        }
        Vec3 v = this.getDeltaMovement().add(d.scale(Math.min(1.0, len / 3.0) / len * 0.055 * speed));
        double max = 0.32 * speed;
        if (v.length() > max) {
            v = v.normalize().scale(max);
        }
        this.setDeltaMovement(v);
    }

    /** Ohne Angriff umkreist sie ihren Gegner, auf und ab wogend, und wechselt ab und zu die Richtung. */
    private void hover(LivingEntity target) {
        boolean fury = this.isEnraged();
        this.orbit += (fury ? 0.032f : 0.022f) * this.orbitDir;
        if (this.random.nextInt(150) == 0) {
            this.orbitDir = -this.orbitDir;
        }
        double radius = fury ? 6.0 : 7.5;
        Vec3 goal = target.position().add(Math.cos(this.orbit) * radius, 1.4 + Mth.sin(this.tickCount * 0.05f) * 1.3,
                Math.sin(this.orbit) * radius);
        this.swimTo(this.inGrotto(goal), fury ? 1.3 : 1.0);
    }

    /** Hält ein Ziel in ihrer Grotte (waagerecht und in der Höhe). */
    private Vec3 inGrotto(Vec3 goal) {
        if (this.home == null) {
            return goal;
        }
        Vec3 h = Vec3.atCenterOf(this.home);
        double dx = goal.x - h.x;
        double dz = goal.z - h.z;
        double flat = Math.sqrt(dx * dx + dz * dz);
        double max = ARENA - 6.0;
        if (flat > max) {
            dx *= max / flat;
            dz *= max / flat;
        }
        return new Vec3(h.x + dx, Mth.clamp(goal.y, h.y - 2.5, h.y + 6.0), h.z + dz);
    }

    /** Fortgelockt: Sie zerfließt in einer Tintenwolke und erscheint wieder über ihrem Kessel. */
    private void keepInGrotto(ServerLevel level) {
        if (this.home == null || this.distanceToSqr(Vec3.atCenterOf(this.home)) < ARENA * ARENA) {
            return;
        }
        this.inkCloud(level, 60);
        this.teleportTo(this.home.getX() + 0.5, this.home.getY(), this.home.getZ() + 0.5);
        this.setDeltaMovement(Vec3.ZERO);
        this.inkCloud(level, 60);
        this.sound(level, ModSounds.SEA_WITCH_LAUGH.get(), 2.0f, 1.1f);
    }

    /** Ohne Gegner: zurück über den Kessel; nach einer Weile versinkt sie wieder in Trance – geheilt, ohne Zorn. */
    private void idle(ServerLevel level) {
        if (this.attack != Attack.NONE) {
            this.begin(Attack.NONE);
        }
        this.idleTicks++;
        if (this.home != null && this.distanceToSqr(Vec3.atBottomCenterOf(this.home)) > 1.5) {
            this.swimTo(Vec3.atBottomCenterOf(this.home), 0.8);
        } else if (this.home != null && this.idleTicks > RETURN_TICKS) {
            this.fallAsleep(level);
            return;
        } else {
            this.brake();
        }
        if (this.idleTicks > 200 && this.tickCount % 20 == 0) {
            this.heal(8.0f);
        }
    }

    private void fallAsleep(ServerLevel level) {
        this.setHealth(this.getMaxHealth());
        this.entityData.set(DATA_ENRAGED, false);
        this.idleTicks = 0;
        this.setDeltaMovement(Vec3.ZERO);
        if (this.home != null) {
            this.snapTo(this.home.getX() + 0.5, this.home.getY(), this.home.getZ() + 0.5, this.getYRot(), 0.0f);
        }
        this.dismissSharks(level);
        this.sound(level, ModSounds.SEA_WITCH_AMBIENT.get(), 1.5f, 0.8f);
        this.begin(Attack.DORMANT);
    }

    private Attack chooseAttack(ServerLevel level, LivingEntity target) {
        double distance = this.distanceTo(target);
        List<Attack> options = new ArrayList<>();
        if (distance < TAIL_RANGE) {
            options.addAll(List.of(Attack.TAIL, Attack.TAIL, Attack.TAIL, Attack.BUBBLE, Attack.INK));
        } else if (distance < 11.0) {
            options.addAll(List.of(Attack.INK, Attack.INK, Attack.WHIRLPOOL, Attack.WHIRLPOOL, Attack.CORAL, Attack.CORAL, Attack.BUBBLE,
                    Attack.SONG));
        } else {
            options.addAll(List.of(Attack.INK, Attack.INK, Attack.INK, Attack.SONG, Attack.SONG, Attack.CORAL, Attack.WHIRLPOOL));
        }
        if (this.sharkCooldown <= 0 && this.sharks.size() < MAX_SHARKS - 1) {
            options.add(Attack.SHARKS);
            options.add(Attack.SHARKS);
        }
        if (this.isEnraged()) {
            options.add(Attack.CURSE);
            options.add(Attack.BUBBLE);
        }
        if (this.floorBelow(level) == null) {
            options.removeIf(a -> a == Attack.CORAL);
        }
        options.removeIf(a -> a == this.lastAttack);
        if (options.isEmpty()) {
            options.add(Attack.INK);
        }
        return options.get(this.random.nextInt(options.size()));
    }

    // ------------------------------------------------------------------ Trance und Erwachen

    private void tickDormant(ServerLevel level) {
        this.setTarget(null);
        if (this.home != null) {
            Vec3 h = Vec3.atBottomCenterOf(this.home).add(0.0, Mth.sin(this.tickCount * 0.04f) * 0.25, 0.0);
            this.swimTo(h, 0.3);
        } else {
            this.brake();
        }
        this.setYRot(this.getYRot() + 0.6f);
        this.yBodyRot = this.getYRot();
        this.yHeadRot = this.getYRot();
        if (this.tickCount % 10 != 0) {
            return;
        }
        for (ServerPlayer player : level.players()) {
            if (player.isCreative() || player.isSpectator() || !player.isAlive()) {
                continue;
            }
            if (player.distanceToSqr(this) < WAKE_RADIUS * WAKE_RADIUS) {
                this.awaken();
                return;
            }
        }
    }

    /** Sie öffnet die Augen, ihr Haar hebt sich, die Kugel auf ihrem Stab glüht auf – und sie lacht. */
    private void tickAwaken(ServerLevel level) {
        int t = ++this.attackTick;
        this.brake();
        Player nearest = level.getNearestPlayer(this, 32.0);
        if (nearest != null && t > 14) {
            this.face(nearest);
        }
        if (t == 2) {
            this.sound(level, ModSounds.SEA_WITCH_AWAKEN.get(), 3.0f, 1.0f);
        }
        if (t > 6 && t < 44 && t % 3 == 0) {
            level.sendParticles(ParticleTypes.GLOW_SQUID_INK, this.getX(), this.getY(0.6), this.getZ(), 3, 0.8, 0.9, 0.8, 0.02);
            level.sendParticles(ParticleTypes.BUBBLE_COLUMN_UP, this.getX(), this.getY(0.2), this.getZ(), 6, 1.0, 0.3, 1.0, 0.05);
        }
        if (t == 40) {
            this.sound(level, ModSounds.SEA_WITCH_LAUGH.get(), 4.0f, 1.0f);
            this.inkCloud(level, 50);
            Component title = Component.translatable("boss.kingdomsatwar.sea_witch").withColor(0x7AF0C8);
            Component subtitle = Component.translatable("boss.kingdomsatwar.sea_witch.title").withStyle(ChatFormatting.GRAY);
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

    /** Wo die Kugel ihres Stabs leuchtet (über ihrem Kopf, rechts). */
    public Vec3 staffTip() {
        float yaw = this.yBodyRot * Mth.DEG_TO_RAD;
        Vec3 right = new Vec3(-Mth.cos(yaw), 0.0, -Mth.sin(yaw));
        Vec3 forward = new Vec3(-Mth.sin(yaw), 0.0, Mth.cos(yaw));
        return this.position().add(0.0, this.getBbHeight() + 0.3, 0.0).add(right.scale(0.9)).add(forward.scale(0.4));
    }

    /** Tintengeschosse: drei (im Zorn fünf) Kugeln aus der Stabkugel, die ihrem Ziel ein wenig folgen. */
    private void tickInk(ServerLevel level, LivingEntity target) {
        int t = this.attackTick;
        if (t == 6) {
            this.sound(level, ModSounds.SEA_WITCH_CAST.get(), 2.0f, 1.0f);
        }
        boolean shot = t == 12 || t == 18 || t == 24 || (this.isEnraged() && (t == 15 || t == 21));
        if (shot) {
            Vec3 from = this.staffTip();
            Vec3 aim = target.getEyePosition().subtract(from).normalize();
            double spread = 0.18;
            aim = aim.add((this.random.nextDouble() - 0.5) * spread, (this.random.nextDouble() - 0.5) * spread * 0.5,
                    (this.random.nextDouble() - 0.5) * spread).normalize();
            InkBoltEntity bolt = new InkBoltEntity(level, this, aim, target, INK_DAMAGE, 0.6);
            bolt.setPos(from.x, from.y, from.z);
            level.addFreshEntity(bolt);
            this.sound(level, ModSounds.SEA_WITCH_INK.get(), 1.4f, 0.9f + this.random.nextFloat() * 0.3f);
            level.sendParticles(ParticleTypes.GLOW_SQUID_INK, from.x, from.y, from.z, 4, 0.2, 0.2, 0.2, 0.02);
        }
    }

    /** Strudel: ein Warnkreis um den Gegner, dann reißt das Wasser alles im Kreis herum und zieht es zur Mitte. */
    private void tickWhirlpool(ServerLevel level, LivingEntity target) {
        int t = this.attackTick;
        float radius = WHIRLPOOL_RADIUS * (this.isEnraged() ? 1.25f : 1.0f);
        if (t == 6) {
            this.whirlCenter = target.position().add(0.0, 0.6, 0.0);
            SpellFxEntity.create(level, SpellFxEntity.Kind.CIRCLE, target.position(), null, 0x5AE0F0, 14, radius).spawn();
            this.sound(level, ModSounds.SEA_WITCH_CAST.get(), 2.0f, 0.8f);
        }
        if (t == 18 && this.whirlCenter != null) {
            Vec3 center = this.whirlCenter;
            float damage = this.isEnraged() ? 4.0f : 3.0f;
            this.sound(level, ModSounds.SEA_WITCH_WHIRLPOOL.get(), 3.0f, 1.0f);
            SpellFxEntity.create(level, SpellFxEntity.Kind.VORTEX, center, null, 0x2FB8D0, WHIRLPOOL_LIFE, radius).onTick(fx -> {
                AABB area = new AABB(center, center).inflate(radius * 1.8, radius * 1.4, radius * 1.8);
                for (LivingEntity victim : this.victims(level, area)) {
                    Vec3 to = center.subtract(victim.position());
                    Vec3 flat = new Vec3(to.x, 0.0, to.z);
                    double d = flat.length();
                    if (d > radius * 1.8) {
                        continue;
                    }
                    Vec3 radial = d > 0.05 ? flat.scale(1.0 / d) : Vec3.ZERO;
                    Vec3 tangent = new Vec3(-radial.z, 0.0, radial.x);
                    victim.setDeltaMovement(victim.getDeltaMovement().scale(0.7).add(radial.scale(0.07)).add(tangent.scale(0.11))
                            .add(0.0, Mth.clamp(to.y * 0.02, -0.04, 0.03), 0.0));
                    victim.needsSync = true;
                    if (fx.tickCount % 10 == 0 && d < radius) {
                        victim.hurtServer(level, this.damageSources().indirectMagic(fx, this), damage);
                    }
                }
                if (fx.tickCount % 2 == 0) {
                    double a = fx.tickCount * 0.6;
                    for (int i = 0; i < 3; i++) {
                        double b = a + i * Mth.TWO_PI / 3.0;
                        level.sendParticles(ParticleTypes.BUBBLE, center.x + Math.cos(b) * radius * 0.9, center.y - 0.4 + i * 0.5,
                                center.z + Math.sin(b) * radius * 0.9, 2, 0.1, 0.1, 0.1, 0.02);
                    }
                    level.sendParticles(ParticleTypes.BUBBLE_COLUMN_UP, center.x, center.y - 0.6, center.z, 3, radius * 0.3, 0.2, radius * 0.3, 0.05);
                }
            }).spawn();
        }
    }

    /** Sirenengesang: wer sie ansieht, den zieht ihr Lied zu ihr hin – am Ende packt sie zu, wer ihr nahe genug ist. */
    private void tickSong(ServerLevel level) {
        int t = this.attackTick;
        if (t == 1) {
            this.sound(level, ModSounds.SEA_WITCH_SONG.get(), 4.0f, 1.0f);
            SpellFxEntity.create(level, SpellFxEntity.Kind.CAST_CIRCLE, this.position(), null, 0x9AFFD8, Attack.SONG.duration - 4, 2.6f)
                    .param(1.0f).anchor(this).spawn();
            for (ServerPlayer player : level.players()) {
                if (player.distanceToSqr(this) < 20.0 * 20.0 && this.canStrike(player)) {
                    player.sendOverlayMessage(Component.translatable("message.kingdomsatwar.sea_witch.song").withStyle(ChatFormatting.AQUA));
                }
            }
        }
        if (t % 4 == 0) {
            level.sendParticles(ParticleTypes.NOTE, this.getX(), this.getY(0.95), this.getZ(), 2, 0.8, 0.4, 0.8, 1.0);
        }
        if (t >= 14 && t <= 76 && t % 2 == 0) {
            Vec3 voice = this.getEyePosition();
            for (Player player : level.getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(18.0), this::canStrike)) {
                Vec3 toWitch = voice.subtract(player.getEyePosition());
                double dist = toWitch.length();
                if (dist < 1.0 || dist > 18.0 || !player.hasLineOfSight(this)) {
                    continue;
                }
                double look = player.getViewVector(1.0f).dot(toWitch.scale(1.0 / dist));
                if (look < 0.55) {
                    continue;                                       // wer wegsieht, hört nur das Lied
                }
                if (this.charmed.add(player.getUUID())) {
                    player.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 120, 0), this);
                }
                Vec3 pull = toWitch.scale(1.0 / dist).scale(dist > 3.0 ? 0.09 : 0.0);
                player.setDeltaMovement(player.getDeltaMovement().scale(0.6).add(pull));
                player.needsSync = true;
                if (t % 6 == 0) {
                    level.sendParticles(ParticleTypes.NOTE, player.getX(), player.getY(1.1), player.getZ(), 1, 0.3, 0.3, 0.3, 1.0);
                }
            }
        }
        if (t == 78) {
            for (LivingEntity victim : this.victims(level, this.getBoundingBox().inflate(3.4))) {
                if (this.charmed.contains(victim.getUUID()) && victim.hurtServer(level, this.damageSources().mobAttack(this), 8.0f)) {
                    victim.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 1), this);
                }
            }
            this.sound(level, ModSounds.SEA_WITCH_LAUGH.get(), 2.0f, 1.2f);
        }
    }

    /** Blasengefängnis: eine Blase umschließt ihr Ziel (im Zorn auch einen zweiten), hält es fest, presst die Luft ab – und platzt. */
    private void tickBubble(ServerLevel level, LivingEntity target) {
        if (this.attackTick != 12) {
            return;
        }
        List<LivingEntity> victims = new ArrayList<>();
        victims.add(target);
        if (this.isEnraged()) {
            level.getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(24.0), p -> p != target && this.canStrike(p)).stream()
                    .findFirst().ifPresent(victims::add);
        }
        this.sound(level, ModSounds.SEA_WITCH_BUBBLE.get(), 2.5f, 1.0f);
        for (LivingEntity victim : victims) {
            if (victim.distanceTo(this) < 30.0) {
                this.trap(level, victim);
            }
        }
    }

    /** Schließt ein Wesen in eine Blase ein. */
    public void trap(ServerLevel level, LivingEntity victim) {
        if (victim instanceof ServerPlayer player) {
            player.sendOverlayMessage(Component.translatable("message.kingdomsatwar.sea_witch.bubble").withStyle(ChatFormatting.AQUA));
        }
        float damage = this.isEnraged() ? 7.0f : 5.0f;
        SpellFxEntity.create(level, SpellFxEntity.Kind.BUBBLE, victim.position(), null, 0x8AE8FF, BUBBLE_LIFE,
                Math.max(2.4f, victim.getBbHeight() * 1.45f)).anchor(victim).onTick(fx -> {
                    if (!victim.isAlive()) {
                        fx.discard();
                        return;
                    }
                    victim.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 6, 6, false, false), this);
                    victim.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, 6, 2, false, false), this);
                    victim.setDeltaMovement(victim.getDeltaMovement().scale(0.3));
                    victim.needsSync = true;
                    if (victim.isUnderWater()) {
                        victim.setAirSupply(Math.max(-19, victim.getAirSupply() - 6));
                    }
                    if (fx.tickCount % 3 == 0) {
                        level.sendParticles(ParticleTypes.BUBBLE, victim.getX(), victim.getY(0.5), victim.getZ(), 4, 0.6, 0.8, 0.6, 0.02);
                    }
                }).onExpire(fx -> {
                    if (victim.isAlive()) {
                        victim.hurtServer(level, this.damageSources().indirectMagic(fx, this), damage);
                    }
                    level.playSound(null, victim.getX(), victim.getY(), victim.getZ(), ModSounds.SEA_WITCH_BUBBLE.get(), SoundSource.HOSTILE, 1.6f, 1.6f);
                    level.sendParticles(ParticleTypes.SPLASH, victim.getX(), victim.getY(0.5), victim.getZ(), 30, 0.8, 0.9, 0.8, 0.2);
                    level.sendParticles(ParticleTypes.BUBBLE_POP, victim.getX(), victim.getY(0.5), victim.getZ(), 30, 0.8, 0.9, 0.8, 0.05);
                }).spawn();
    }

    /** Haie: Sie hebt die Arme, die Tiefe antwortet – zwei (im Zorn drei) Haie schießen aus dem Dunkel. */
    private void tickSharks(ServerLevel level, LivingEntity target) {
        int t = this.attackTick;
        if (t == 8) {
            this.sound(level, ModSounds.SEA_WITCH_SUMMON.get(), 3.0f, 1.0f);
        }
        if (t != 22) {
            return;
        }
        int count = Math.min(this.isEnraged() ? 3 : 2, MAX_SHARKS - this.sharks.size());
        for (int i = 0; i < count; i++) {
            double a = this.random.nextDouble() * Mth.TWO_PI;
            Vec3 at = this.position().add(Math.cos(a) * 3.0, 0.8 + this.random.nextDouble(), Math.sin(a) * 3.0);
            if (!level.getBlockState(BlockPos.containing(at)).getCollisionShape(level, BlockPos.containing(at)).isEmpty()) {
                at = this.position().add(0.0, 1.0, 0.0);
            }
            LivingEntity shark = ModEntities.SHARK.get().create(level, EntitySpawnReason.MOB_SUMMONED);
            if (shark == null) {
                continue;
            }
            shark.snapTo(at.x, at.y, at.z, (float) (a * Mth.RAD_TO_DEG), 0.0f);
            shark.addTag(SHARK_TAG);
            if (shark instanceof net.minecraft.world.entity.Mob mob) {
                mob.setTarget(target);
                mob.setPersistenceRequired();
            }
            level.addFreshEntity(shark);
            this.sharks.add(shark.getUUID());
            this.inkCloud(level, at, 24);
        }
        this.sharkCooldown = 500;
    }

    /** Korallenspieße: Sie stößt den Stab hinab, und Korallen brechen in einer Spur (im Zorn in dreien) aus dem Grund. */
    private void tickCoral(ServerLevel level, LivingEntity target) {
        if (this.attackTick == 6) {
            this.sound(level, ModSounds.SEA_WITCH_CAST.get(), 2.0f, 0.7f);
        }
        if (this.attackTick != 16) {
            return;
        }
        Double floor = this.floorBelow(level);
        if (floor == null) {
            return;
        }
        Vec3 dir = new Vec3(target.getX() - this.getX(), 0.0, target.getZ() - this.getZ());
        dir = dir.lengthSqr() < 1.0e-4 ? Vec3.directionFromRotation(0.0f, this.getYRot()) : dir.normalize();
        double[] angles = this.isEnraged() ? new double[] {-24.0, 0.0, 24.0} : new double[] {0.0};
        float damage = this.isEnraged() ? CORAL_DAMAGE + 1.0f : CORAL_DAMAGE;
        for (double angle : angles) {
            Vec3 d = dir.yRot((float) (angle * Mth.DEG_TO_RAD));
            Vec3 start = new Vec3(this.getX(), floor, this.getZ()).add(d.scale(1.6));
            CrystalLine.start(level, start, d, 10, 1.6, 2, damage, 0.85f, this, this::canStrike, CrystalLine.Style.CORAL);
        }
        this.sound(level, ModSounds.SEA_WITCH_CORAL.get(), 2.5f, 1.0f);
    }

    /** Wo unter ihr der Grund liegt (Oberkante, bis 24 Blöcke tief) – oder null. */
    private @Nullable Double floorBelow(ServerLevel level) {
        BlockPos.MutableBlockPos pos = this.blockPosition().mutable();
        for (int i = 0; i < 24; i++) {
            BlockState state = level.getBlockState(pos);
            if (state.isFaceSturdy(level, pos, Direction.UP)) {
                return (double) pos.getY() + 1.0;
            }
            pos.move(Direction.DOWN);
        }
        return null;
    }

    /** Fluch der Tiefe (im Zorn): ein Hexenkreis um sie, dann legt sich der Fluch auf alle in der Nähe – sie zehrt davon. */
    private void tickCurse(ServerLevel level) {
        int t = this.attackTick;
        if (t == 1) {
            this.sound(level, ModSounds.SEA_WITCH_CURSE.get(), 3.0f, 1.0f);
            SpellFxEntity.create(level, SpellFxEntity.Kind.CAST_CIRCLE, this.position(), null, 0x8A4ADA, 34, 3.6f).param(1.0f).anchor(this).spawn();
        }
        if (t != 30) {
            return;
        }
        int cursed = 0;
        for (Player player : level.getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(16.0), this::canStrike)) {
            if (player.distanceTo(this) > 16.0) {
                continue;
            }
            player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200, 0), this);
            player.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, 200, 1), this);
            player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 200, 1), this);
            player.hurtServer(level, this.damageSources().indirectMagic(this, this), 4.0f);
            player.sendOverlayMessage(Component.translatable("message.kingdomsatwar.sea_witch.curse").withStyle(ChatFormatting.DARK_PURPLE));
            level.sendParticles(ParticleTypes.SQUID_INK, player.getX(), player.getY(0.6), player.getZ(), 20, 0.4, 0.6, 0.4, 0.05);
            cursed++;
        }
        if (cursed > 0) {
            this.heal(12.0f * cursed);
            level.sendParticles(ParticleTypes.GLOW_SQUID_INK, this.getX(), this.getY(0.6), this.getZ(), 30, 0.8, 1.0, 0.8, 0.05);
        }
    }

    /** Schwanzhieb: Sie wirbelt herum, und ihr Schwanz fegt alles in der Nähe fort. */
    private void tickTail(ServerLevel level, LivingEntity target) {
        int t = this.attackTick;
        if (t == 8) {
            this.sound(level, ModSounds.SEA_WITCH_TAIL.get(), 2.5f, 1.0f);
        }
        if (t != 12) {
            return;
        }
        float damage = this.isEnraged() ? TAIL_DAMAGE + 2.0f : TAIL_DAMAGE;
        for (LivingEntity victim : this.victims(level, this.getBoundingBox().inflate(TAIL_RANGE, 2.0, TAIL_RANGE))) {
            if (victim.distanceTo(this) > TAIL_RANGE + victim.getBbWidth() * 0.5) {
                continue;
            }
            if (victim.hurtServer(level, this.damageSources().mobAttack(this), damage)) {
                Vec3 away = new Vec3(victim.getX() - this.getX(), 0.0, victim.getZ() - this.getZ());
                away = away.lengthSqr() < 1.0e-4 ? new Vec3(1.0, 0.0, 0.0) : away.normalize();
                victim.setDeltaMovement(victim.getDeltaMovement().add(away.x * 1.1, 0.35, away.z * 1.1));
                victim.needsSync = true;
            }
        }
        level.sendParticles(ParticleTypes.SWEEP_ATTACK, this.getX(), this.getY(0.3), this.getZ(), 6, 1.8, 0.3, 1.8, 0.0);
        level.sendParticles(ParticleTypes.BUBBLE, this.getX(), this.getY(0.3), this.getZ(), 40, 2.0, 0.4, 2.0, 0.1);
    }

    /** Ihr Zorn (unter halber Kraft, einmal): ein Schrei, eine Woge aus Tinte stößt alle fort und verdunkelt das Wasser. */
    private void tickFury(ServerLevel level) {
        int t = this.attackTick;
        if (t == 1) {
            this.entityData.set(DATA_ENRAGED, true);
            this.sound(level, ModSounds.SEA_WITCH_FURY.get(), 4.0f, 1.0f);
            for (ServerPlayer player : level.players()) {
                if (player.distanceToSqr(this) < 32.0 * 32.0) {
                    player.sendOverlayMessage(Component.translatable("message.kingdomsatwar.sea_witch.fury").withStyle(ChatFormatting.DARK_AQUA));
                }
            }
        }
        if (t < 20 && t % 2 == 0) {
            level.sendParticles(ParticleTypes.GLOW_SQUID_INK, this.getX(), this.getY(0.6), this.getZ(), 4, 0.6, 0.8, 0.6, 0.02);
        }
        if (t == 20) {
            this.inkCloud(level, 160);
            for (LivingEntity victim : this.victims(level, this.getBoundingBox().inflate(14.0))) {
                double d = victim.distanceTo(this);
                if (d > 14.0) {
                    continue;
                }
                Vec3 away = victim.position().subtract(this.position());
                away = away.lengthSqr() < 1.0e-4 ? new Vec3(0.0, 1.0, 0.0) : away.normalize();
                double k = 1.6 * (1.0 - d / 16.0);
                victim.setDeltaMovement(victim.getDeltaMovement().add(away.scale(k)).add(0.0, 0.2, 0.0));
                victim.needsSync = true;
                victim.hurtServer(level, this.damageSources().indirectMagic(this, this), 6.0f);
                victim.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 80, 0), this);
            }
        }
    }

    /** Eine Wolke aus Hexentinte an ihrer Stelle. */
    private void inkCloud(ServerLevel level, int count) {
        this.inkCloud(level, this.position().add(0.0, this.getBbHeight() * 0.5, 0.0), count);
    }

    private void inkCloud(ServerLevel level, Vec3 at, int count) {
        level.sendParticles(ParticleTypes.SQUID_INK, at.x, at.y, at.z, count, 1.0, 1.0, 1.0, 0.08);
        level.sendParticles(ParticleTypes.GLOW_SQUID_INK, at.x, at.y, at.z, count / 3, 1.0, 1.0, 1.0, 0.06);
        level.sendParticles(new DustParticleOptions(0x2A6A5A, 1.6f), at.x, at.y, at.z, count / 4, 1.2, 1.2, 1.2, 0.0);
    }

    private void sound(ServerLevel level, SoundEvent sound, float volume, float pitch) {
        level.playSound(null, this.getX(), this.getY(), this.getZ(), sound, SoundSource.HOSTILE, volume, pitch);
    }

    /** Ihre Haie verschwinden in der Tiefe. */
    private void dismissSharks(ServerLevel level) {
        for (UUID id : this.sharks) {
            Entity shark = level.getEntity(id);
            if (shark != null && shark.isAlive()) {
                this.inkCloud(level, shark.position(), 16);
                shark.discard();
            }
        }
        this.sharks.clear();
    }

    /** Die Haie, die sie gerade gerufen hat (für Tests). */
    public int sharkCount() {
        return this.sharks.size();
    }

    // ------------------------------------------------------------------ Tod, Speichern, Klänge

    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (this.level() instanceof ServerLevel level) {
            this.dismissSharks(level);
            if (this.lair != null && level.getBlockEntity(this.lair) instanceof WitchCauldronBlockEntity cauldron && cauldron.owns(this)) {
                cauldron.onWitchDefeated(level);
            }
        }
    }

    /** Sie sinkt, ihr Licht erlischt – und sie zerfließt zu Tinte. */
    @Override
    protected void tickDeath() {
        this.deathTime++;
        this.setDeltaMovement(this.getDeltaMovement().multiply(0.6, 0.6, 0.6).add(0.0, -0.01, 0.0));
        if (!(this.level() instanceof ServerLevel level)) {
            return;
        }
        if (this.deathTime % 4 == 0) {
            level.sendParticles(ParticleTypes.SQUID_INK, this.getX(), this.getY(0.5), this.getZ(), 8, 0.6, 0.9, 0.6, 0.03);
            level.sendParticles(ParticleTypes.GLOW_SQUID_INK, this.getX(), this.getY(0.7), this.getZ(), 3, 0.5, 0.6, 0.5, 0.02);
        }
        if (this.deathTime == DEATH_TICKS - 4) {
            this.inkCloud(level, 180);
            level.sendParticles(ParticleTypes.BUBBLE_COLUMN_UP, this.getX(), this.getY(0.2), this.getZ(), 60, 1.2, 0.6, 1.2, 0.1);
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
        if (this.lair != null) {
            output.putLong("Lair", this.lair.asLong());
        }
        output.putBoolean("Enraged", this.isEnraged());
        output.putBoolean("Dormant", this.attack == Attack.DORMANT);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        input.getLong("Home").ifPresent(h -> this.home = BlockPos.of(h));
        input.getLong("Lair").ifPresent(l -> this.lair = BlockPos.of(l));
        this.entityData.set(DATA_ENRAGED, input.getBooleanOr("Enraged", false));
        if (input.getBooleanOr("Dormant", false)) {
            this.begin(Attack.DORMANT);
        }
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return this.attack == Attack.DORMANT ? null : ModSounds.SEA_WITCH_AMBIENT.get();
    }

    @Override
    public int getAmbientSoundInterval() {
        return 160;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.SEA_WITCH_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.SEA_WITCH_DEATH.get();
    }

    @Override
    protected float getSoundVolume() {
        return 2.5f;
    }

    @Override
    public io.github.sirschrolli.kingdomsatwar.entity.boss.sky.SkyTheme skyTheme() {
        return io.github.sirschrolli.kingdomsatwar.entity.boss.sky.SkyTheme.REEF;
    }
}
