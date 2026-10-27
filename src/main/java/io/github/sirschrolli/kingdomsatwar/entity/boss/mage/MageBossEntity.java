package io.github.sirschrolli.kingdomsatwar.entity.boss.mage;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import io.github.sirschrolli.kingdomsatwar.entity.boss.BossFx;
import io.github.sirschrolli.kingdomsatwar.entity.boss.DragonBossEntity;
import io.github.sirschrolli.kingdomsatwar.entity.boss.ElementalBoss;
import io.github.sirschrolli.kingdomsatwar.entity.dragon.DragonEntity;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierEntity;
import io.github.sirschrolli.kingdomsatwar.magic.Spell;
import io.github.sirschrolli.kingdomsatwar.network.ScreenShakePayload;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
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
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Ein Erzmagier: wacht meditierend in der Spitze seines Turms, bis jemand den Saal betritt. Dann erhebt er sich und
 * kämpft mit Geschossen aus reiner Magie, dem Zauber seines Elements, Sprüngen durch den Raum (Blinzeln), Spiegelbildern,
 * einem Schutzschild und – in seiner letzten Phase – einem gewaltigen Zauber seines Elements (Meteorregen, Blizzard,
 * Gewittersturm, Wildwuchs, Felsbeben, Mahlstrom). Besiegt hinterlässt er seine Zaubertafel.
 *
 * <p>Kampfphasen nach Lebensenergie: 1 (über 66 %), 2 (Spiegelbilder und Schild), 3 (unter 33 %, großer Zauber). Beim
 * Phasenwechsel stößt er alle von sich und ist einen Moment unverwundbar. Läuft man davon, kehrt er nach einer Weile
 * zurück, heilt sich und meditiert wieder.
 */
public class MageBossEntity extends Monster implements Enemy, ElementalBoss, MageLike {
    public enum State { DORMANT, AWAKENING, FIGHT, DYING }

    /** Gesten (Animationen), synchronisiert. */
    public static final byte ANIM_NONE = 0;
    public static final byte ANIM_CAST = 1;
    public static final byte ANIM_MISSILES = 2;
    public static final byte ANIM_ULTIMATE = 3;
    public static final byte ANIM_SHIELD = 4;
    public static final byte ANIM_SUMMON = 5;
    public static final byte ANIM_AWAKEN = 6;
    public static final byte ANIM_TRANSITION = 7;

    /** Größe des Saals: so weit darf er sich von seinem Platz entfernen. */
    public static final double ROOM_RADIUS = 7.5;
    /** Ab dieser Entfernung zum Platz zählen Spieler noch als Gegner (sonst gibt er nach einer Weile auf). */
    public static final double LEASH_RADIUS = 28.0;
    public static final int AWAKEN_TICKS = 50;
    public static final int DEATH_TICKS = 70;
    public static final int TRANSITION_TICKS = 30;
    public static final int SHIELD_TICKS = 120;
    private static final int SHIELD_HITS = 4;
    private static final Identifier HEALTH_SCALE = KingdomsAtWar.id("mage_health_scale");

    private static final EntityDataAccessor<Byte> DATA_STATE = SynchedEntityData.defineId(MageBossEntity.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Byte> DATA_GESTURE = SynchedEntityData.defineId(MageBossEntity.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Byte> DATA_PHASE = SynchedEntityData.defineId(MageBossEntity.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Integer> DATA_SHIELD = SynchedEntityData.defineId(MageBossEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_BLINK = SynchedEntityData.defineId(MageBossEntity.class, EntityDataSerializers.INT);

    /** Angriffe des Magiers. */
    enum Attack { MISSILES, SPELL, BLINK, IMAGES, SHIELD, ULTIMATE }

    private final DragonType element;
    private final ServerBossEvent bossEvent;
    private final Set<ServerPlayer> barPlayers = new HashSet<>();
    private final Map<UUID, Float> threat = new HashMap<>();
    private final Set<UUID> participants = new HashSet<>();
    private final Map<Attack, Integer> cooldowns = new EnumMap<>(Attack.class);
    private final List<UUID> images = new ArrayList<>();
    private @Nullable BlockPos home;
    private int stateTicks;
    private @Nullable Attack attack;
    private int attackTicks;
    private int idle = 30;
    private int lostTicks;
    private int transition;
    private int shieldHits;
    private int pressure;
    private int pressureTimer;
    private @Nullable LivingEntity target;
    private @Nullable Vec3 blinkTo;
    private float strafe = 1.0f;
    private int strafeTimer;
    private @Nullable DamageSource deathSource;
    /** Einschlagstellen des Gewittersturms (siehe {@link MageUltimates}). */
    final @Nullable Vec3[] strikes = new Vec3[8];

    // nur auf dem Bildschirm
    public final AnimationState gestureAnimation = new AnimationState();
    private int blinkO;
    private int shieldO;

    public MageBossEntity(EntityType<? extends MageBossEntity> type, Level level, DragonType element) {
        super(type, level);
        this.element = element;
        this.bossEvent = new ServerBossEvent(Mth.createInsecureUUID(this.random),
                Component.translatable("boss.kingdomsatwar.mage_" + element.id()), BossEvent.BossBarColor.PURPLE,
                BossEvent.BossBarOverlay.NOTCHED_10);
        this.bossEvent.setDarkenScreen(true);
        this.xpReward = 0;
        this.setPersistenceRequired();
        this.setNoGravity(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 300.0)
                .add(Attributes.ARMOR, 10.0)
                .add(Attributes.ARMOR_TOUGHNESS, 4.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.FLYING_SPEED, 0.4)
                .add(Attributes.ATTACK_DAMAGE, 6.0)
                .add(Attributes.FOLLOW_RANGE, LEASH_RADIUS);
    }

    // =====================================================================================
    // Daten
    // =====================================================================================

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_STATE, (byte) State.DORMANT.ordinal());
        builder.define(DATA_GESTURE, ANIM_NONE);
        builder.define(DATA_PHASE, (byte) 1);
        builder.define(DATA_SHIELD, 0);
        builder.define(DATA_BLINK, 0);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (DATA_GESTURE.equals(key) && this.level().isClientSide()) {
            if (this.gesture() == ANIM_NONE) {
                this.gestureAnimation.stop();
            } else {
                this.gestureAnimation.start(this.tickCount);
            }
        }
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        if (this.home != null) {
            output.store("MageHome", BlockPos.CODEC, this.home);
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        input.read("MageHome", BlockPos.CODEC).ifPresent(pos -> this.home = pos);
        // nach dem Laden meditiert er wieder an seinem Platz (ein unterbrochener Kampf beginnt von vorn)
        this.setState(State.DORMANT);
        this.setHealth(this.getMaxHealth());
    }

    @Override
    public DragonType element() {
        return this.element;
    }

    public String bossId() {
        return "mage_" + this.element.id();
    }

    public State state() {
        return State.values()[Mth.clamp(this.entityData.get(DATA_STATE), 0, State.values().length - 1)];
    }

    private void setState(State state) {
        this.entityData.set(DATA_STATE, (byte) state.ordinal());
        this.stateTicks = 0;
    }

    @Override
    public byte gesture() {
        return this.entityData.get(DATA_GESTURE);
    }

    private void setGesture(byte gesture) {
        this.entityData.set(DATA_GESTURE, gesture);
    }

    @Override
    public AnimationState gestureAnimation() {
        return this.gestureAnimation;
    }

    public int phase() {
        return this.entityData.get(DATA_PHASE);
    }

    @Override
    public float visibility(float partialTicks) {
        float blink = Mth.lerp(partialTicks, this.blinkO, this.entityData.get(DATA_BLINK));
        return Mth.clamp(1.0f - blink / 6.0f, 0.0f, 1.0f);
    }

    @Override
    public float shield(float partialTicks) {
        float s = Mth.lerp(partialTicks, this.shieldO, this.entityData.get(DATA_SHIELD));
        return Mth.clamp(s / 10.0f, 0.0f, 1.0f);
    }

    public boolean shielded() {
        return this.entityData.get(DATA_SHIELD) > 0;
    }

    @Override
    public boolean dormant() {
        return this.state() == State.DORMANT;
    }

    @Override
    public int dyingTicks() {
        return this.state() == State.DYING ? this.deathTime : 0;
    }

    @Override
    public boolean isImage() {
        return false;
    }

    public @Nullable BlockPos home() {
        return this.home;
    }

    /** Platz im Turm, an dem der Magier meditiert (beim Erzeugen des Turms gesetzt). */
    public void setHome(BlockPos home) {
        this.home = home;
    }

    public Set<UUID> participants() {
        return this.participants;
    }

    public @Nullable LivingEntity target() {
        return this.target;
    }

    /** Mitte des Saals (sein Platz, sonst wo er steht). */
    public Vec3 center() {
        return this.home != null ? Vec3.atBottomCenterOf(this.home) : this.position();
    }

    // =====================================================================================
    // Gegner und Schaden
    // =====================================================================================

    @Override
    public boolean isBossEnemy(@Nullable LivingEntity entity) {
        if (entity == null || !entity.isAlive() || entity == this || entity instanceof ElementalBoss || entity instanceof MageMinion) {
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

    @Override
    public List<LivingEntity> enemiesIn(AABB box) {
        return this.level().getEntitiesOfClass(LivingEntity.class, box, e -> this.isBossEnemy(e) && this.inArena(e));
    }

    @Override
    public List<LivingEntity> enemiesAround(Vec3 center, double radius, double height) {
        AABB box = new AABB(center.x - radius, center.y - height, center.z - radius, center.x + radius, center.y + height, center.z + radius);
        List<LivingEntity> list = this.enemiesIn(box);
        list.removeIf(e -> e.position().subtract(center).horizontalDistanceSqr() > radius * radius);
        return list;
    }

    @Override
    public boolean hurtTarget(ServerLevel level, LivingEntity target, float damage) {
        return target.hurtServer(level, this.damageSources().indirectMagic(this, this), damage * this.damageMultiplier());
    }

    /** Je Phase stärker: Phase 2 +20 %, Phase 3 +45 %. */
    @Override
    public float damageMultiplier() {
        return switch (this.phase()) {
            case 2 -> 1.2f;
            case 3 -> 1.45f;
            default -> 1.0f;
        };
    }

    /** Wie schnell er angreift: in Phase 3 deutlich schneller. */
    private float tempo() {
        return switch (this.phase()) {
            case 2 -> 0.85f;
            case 3 -> 0.65f;
            default -> 1.0f;
        };
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        State state = this.state();
        if (state == State.DYING || source.is(DamageTypes.IN_WALL) || source.is(DamageTypes.CRAMMING) || source.is(DamageTypeTags.IS_FALL)
                || source.is(DamageTypes.DROWN) || source.is(DamageTypes.FLY_INTO_WALL) || this.immuneTo(source)) {
            return false;
        }
        Entity attacker = source.getEntity();
        if (attacker instanceof ElementalBoss || attacker instanceof MageMinion) {
            return false;
        }
        if (state == State.DORMANT && attacker instanceof LivingEntity living && this.isBossEnemy(living)) {
            this.beginAwakening(level);
            return false;
        }
        if (state != State.FIGHT || (this.transition > 0 && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY))) {
            this.deflect(level);
            return false;
        }
        if (this.shielded() && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            this.deflect(level);
            if (++this.shieldHits >= SHIELD_HITS) {
                this.breakShield(level);
            }
            return false;
        }
        if (!(attacker instanceof Player) && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            damage *= DragonBossEntity.NPC_DAMAGE_FACTOR;
        }
        if (attacker instanceof LivingEntity living) {
            this.threat.merge(living.getUUID(), damage, Float::sum);
            if (living instanceof Player player) {
                this.participants.add(player.getUUID());
            } else if (living instanceof SoldierEntity soldier && soldier.getOwner() instanceof Player owner) {
                this.participants.add(owner.getUUID());
            }
            // wer ihm zu nahe kommt und zuschlägt, den lässt er nicht lange gewähren
            if (living.distanceToSqr(this) < 16.0) {
                this.pressure++;
                this.pressureTimer = 60;
            }
        }
        boolean hurt = super.hurtServer(level, source, damage);
        this.setInvulnerableTime(Math.min(this.getInvulnerableTime(), 10));
        return hurt;
    }

    /** Gegen sein eigenes Element ist er gefeit. */
    private boolean immuneTo(DamageSource source) {
        return switch (this.element) {
            case FIRE -> source.is(DamageTypeTags.IS_FIRE);
            case ICE -> source.is(DamageTypeTags.IS_FREEZING);
            case LIGHTNING -> source.is(DamageTypeTags.IS_LIGHTNING);
            case WATER -> source.is(DamageTypeTags.IS_DROWNING);
            case NATURE -> source.is(DamageTypes.SWEET_BERRY_BUSH) || source.is(DamageTypes.CACTUS);
            case EARTH -> source.is(DamageTypes.FALLING_STALACTITE) || source.is(DamageTypes.FALLING_BLOCK);
        };
    }

    private void deflect(ServerLevel level) {
        level.sendParticles(ParticleTypes.ENCHANTED_HIT, this.getX(), this.getY(0.6), this.getZ(), 10, 0.5, 0.7, 0.5, 0.25);
        this.playSound(SoundEvents.AMETHYST_BLOCK_HIT, 1.0f, 1.4f + this.random.nextFloat() * 0.3f);
    }

    @Override
    public boolean canBeAffected(MobEffectInstance effect) {
        return effect.getEffect().value().isBeneficial();
    }

    @Override
    public void knockback(double power, double xd, double zd, DamageSource source, float damage, boolean comesFromEffect) {
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

    @Override
    public boolean causeFallDamage(double fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    // =====================================================================================
    // Ticks
    // =====================================================================================

    @Override
    public void tick() {
        this.blinkO = this.entityData.get(DATA_BLINK);
        this.shieldO = this.entityData.get(DATA_SHIELD);
        super.tick();
        if (this.level().isClientSide()) {
            this.clientEffects();
        }
    }

    /** Leuchtende Funken des Elements um den Magier, im Kampf mehr als beim Meditieren. */
    private void clientEffects() {
        if (this.state() == State.DYING) {
            return;
        }
        int rate = this.dormant() ? 6 : 2;
        if (this.random.nextInt(rate) == 0) {
            double a = this.random.nextDouble() * Mth.TWO_PI;
            double r = 0.6 + this.random.nextDouble() * 0.5;
            this.level().addParticle(new DustParticleOptions(this.element.color(), 0.9f), this.getX() + Math.cos(a) * r,
                    this.getY() + 0.2 + this.random.nextDouble() * 2.2, this.getZ() + Math.sin(a) * r, 0.0, 0.02, 0.0);
        }
        if (!this.dormant() && this.random.nextInt(4) == 0) {
            this.level().addParticle(this.element.particle(), this.getX() + (this.random.nextDouble() - 0.5) * 0.8,
                    this.getY() + 0.1, this.getZ() + (this.random.nextDouble() - 0.5) * 0.8, 0.0, 0.02, 0.0);
        }
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        this.stateTicks++;
        int blink = this.entityData.get(DATA_BLINK);
        if (blink > 0 && this.attack != Attack.BLINK) {
            this.entityData.set(DATA_BLINK, Math.max(0, blink - 1));
        }
        int shield = this.entityData.get(DATA_SHIELD);
        if (shield > 0) {
            this.entityData.set(DATA_SHIELD, shield - 1);
            if (shield == 1) {
                this.shieldHits = 0;
            }
        }
        switch (this.state()) {
            case DORMANT -> this.tickDormant(level);
            case AWAKENING -> this.tickAwakening(level);
            case FIGHT -> this.tickFight(level);
            case DYING -> {
            }
        }
        this.hover(level);
        if (this.tickCount % 10 == 0) {
            this.updateBossBar(level);
        }
    }

    /** Schweben: eine knappe Handbreit über dem Boden, sanft auf und ab. */
    private void hover(ServerLevel level) {
        if (this.state() == State.DYING) {
            this.setDeltaMovement(0.0, 0.03, 0.0);
            return;
        }
        double floor = this.floorBelow(level);
        double want = floor + (this.dormant() ? 0.35 : 0.55) + Mth.sin(this.tickCount * 0.08f) * 0.12;
        Vec3 v = this.getDeltaMovement();
        double dy = Mth.clamp((want - this.getY()) * 0.15, -0.25, 0.25);
        this.setDeltaMovement(v.x, dy, v.z);
    }

    /** Höhe des Bodens unter dem Magier (höchstens vier Blöcke tief, sonst seine Höhe). */
    private double floorBelow(ServerLevel level) {
        Vec3 from = this.position().add(0, 0.5, 0);
        HitResult hit = level.clip(new ClipContext(from, from.add(0, -4.5, 0), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        return hit.getType() == HitResult.Type.MISS ? this.getY() - 0.4 : hit.getLocation().y;
    }

    // ------------------------------------------------------------------ Meditation und Erwachen

    private void tickDormant(ServerLevel level) {
        this.setDeltaMovement(0, this.getDeltaMovement().y, 0);
        if (this.home != null && this.position().distanceToSqr(Vec3.atBottomCenterOf(this.home)) > 4.0) {
            this.snapTo(Vec3.atBottomCenterOf(this.home));
        }
        if (this.tickCount % 10 != 0) {
            return;
        }
        // Erwacht, sobald ein Gegner den Saal betritt (nah genug und in Sichtweite)
        for (ServerPlayer player : level.players()) {
            if (this.isBossEnemy(player) && player.distanceToSqr(this) < 10.0 * 10.0 && Math.abs(player.getY() - this.getY()) < 5.0
                    && this.hasLineOfSight(player)) {
                this.beginAwakening(level);
                return;
            }
        }
    }

    /** Erwacht: Lebensenergie je nach Spielerzahl und Schwierigkeit, Titel auf dem Bildschirm, Runen wirbeln auf. */
    public void beginAwakening(ServerLevel level) {
        if (this.state() != State.DORMANT) {
            return;
        }
        if (this.home == null) {
            this.home = this.blockPosition();
        }
        this.setState(State.AWAKENING);
        this.setGesture(ANIM_AWAKEN);
        int players = Math.max(1, this.nearbyPlayers(level, LEASH_RADIUS).size());
        double difficulty = switch (level.getDifficulty()) {
            case PEACEFUL, EASY -> 0.75;
            case NORMAL -> 1.0;
            case HARD -> 1.3;
        };
        double scale = difficulty * (1.0 + 0.4 * (players - 1)) - 1.0;
        AttributeInstance health = this.getAttribute(Attributes.MAX_HEALTH);
        if (health != null) {
            health.removeModifier(HEALTH_SCALE);
            if (Math.abs(scale) > 0.01) {
                health.addPermanentModifier(new AttributeModifier(HEALTH_SCALE, scale, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
            }
        }
        this.setHealth(this.getMaxHealth());
        this.entityData.set(DATA_PHASE, (byte) 1);
        Component title = Component.translatable(this.getType().getDescriptionId()).withColor(this.element.color());
        Component subtitle = Component.translatable("boss.kingdomsatwar." + this.bossId() + ".title").withStyle(ChatFormatting.GRAY);
        for (ServerPlayer player : this.nearbyPlayers(level, 40.0)) {
            player.connection.send(new ClientboundSetTitlesAnimationPacket(10, 60, 20));
            player.connection.send(new ClientboundSetTitleTextPacket(title));
            player.connection.send(new ClientboundSetSubtitleTextPacket(subtitle));
        }
        level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.EVOKER_PREPARE_SUMMON, SoundSource.HOSTILE, 2.0f, 0.6f);
        level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.BEACON_ACTIVATE, SoundSource.HOSTILE, 2.0f, 0.7f);
    }

    private void tickAwakening(ServerLevel level) {
        int t = this.stateTicks;
        LivingEntity near = this.pickTarget(level);
        if (near != null) {
            this.face(near.position(), 6.0f);
        }
        if (t % 3 == 0) {
            double a = t * 0.35;
            for (int k = 0; k < 3; k++) {
                double ang = a + k * Mth.TWO_PI / 3;
                double r = 2.4 - t / (double) AWAKEN_TICKS * 1.6;
                level.sendParticles(new DustParticleOptions(this.element.color(), 1.4f), this.getX() + Math.cos(ang) * r,
                        this.getY() + 0.2 + t * 0.04, this.getZ() + Math.sin(ang) * r, 1, 0, 0, 0, 0);
            }
            level.sendParticles(ParticleTypes.ENCHANT, this.getX(), this.getY() + 1.6, this.getZ(), 6, 1.2, 0.8, 1.2, 0.6);
        }
        if (t == AWAKEN_TICKS - 8) {
            level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.EVOKER_CAST_SPELL, SoundSource.HOSTILE, 2.0f, 0.7f);
            BossFx.impact(level, this.element.particle(), this.position().add(0, 1.2, 0), 60, 1.2, 1.0, 1.2, 0.08);
            this.shake(level, 20.0, 0.4f, 12);
        }
        if (t >= AWAKEN_TICKS) {
            this.setState(State.FIGHT);
            this.setGesture(ANIM_NONE);
            this.idle = 10;
        }
    }

    // ------------------------------------------------------------------ Kampf

    private void tickFight(ServerLevel level) {
        this.updatePhase(level);
        this.cooldowns.replaceAll((k, v) -> Math.max(0, v - 1));
        if (this.pressureTimer > 0 && --this.pressureTimer == 0) {
            this.pressure = 0;
        }
        this.images.removeIf(id -> !(level.getEntity(id) instanceof MageImageEntity image) || !image.isAlive());
        if (this.transition > 0) {
            this.tickTransition(level);
            return;
        }
        LivingEntity target = this.pickTarget(level);
        this.target = target;
        if (target == null) {
            this.setDeltaMovement(this.getDeltaMovement().multiply(0.6, 1.0, 0.6));
            if (++this.lostTicks > 160) {
                this.resetToHome(level);
            }
            return;
        }
        this.lostTicks = 0;
        this.face(target.getEyePosition(), 12.0f);
        if (this.attack != null) {
            this.tickAttack(level, target);
        } else {
            this.move(level, target);
            if (this.pressure >= 3 && this.cooldowns.getOrDefault(Attack.BLINK, 0) <= 0) {
                this.pressure = 0;
                this.start(Attack.BLINK);
            } else if (--this.idle <= 0) {
                this.chooseAttack(level, target);
            }
        }
    }

    /**
     * Steht der Gegner im Saal des Magiers? Im Turm zählt nur, wer im Sanktum ist – nicht, wer in den Geschossen darunter
     * steht (dorthin reichen weder seine Blicke noch seine Zauber).
     */
    public boolean inArena(LivingEntity entity) {
        if (this.home == null) {
            return true;
        }
        double dy = entity.getY() - this.home.getY();
        return dy > -2.0 && dy < 10.0;
    }

    /** Alle Gegner im Saal ({@link #enemiesIn} lässt nur Gegner im Saal zu). */
    public List<LivingEntity> arenaEnemies() {
        return this.enemiesAround(this.center(), LEASH_RADIUS, 12.0);
    }

    /** Ziel: der nächste Gegner im Saal (wer ihn angreift, wird bevorzugt). */
    private @Nullable LivingEntity pickTarget(ServerLevel level) {
        LivingEntity best = null;
        double bestScore = Double.MAX_VALUE;
        for (LivingEntity e : this.arenaEnemies()) {
            double score = e.distanceToSqr(this) - this.threat.getOrDefault(e.getUUID(), 0.0f) * 3.0 - (e instanceof Player ? 30.0 : 0.0);
            if (score < bestScore) {
                bestScore = score;
                best = e;
            }
        }
        return best;
    }

    /** Hält Abstand (sieben bis elf Blöcke), umkreist das Ziel und bleibt im Saal. */
    private void move(ServerLevel level, LivingEntity target) {
        if (--this.strafeTimer <= 0) {
            this.strafeTimer = 40 + this.random.nextInt(60);
            this.strafe = this.random.nextBoolean() ? 1.0f : -1.0f;
        }
        Vec3 to = target.position().subtract(this.position()).multiply(1, 0, 1);
        double dist = to.length();
        Vec3 dir = dist > 1.0e-3 ? to.scale(1.0 / dist) : Vec3.ZERO;
        Vec3 side = new Vec3(-dir.z, 0, dir.x).scale(this.strafe);
        double approach = dist > 11.0 ? 1.0 : dist < 6.0 ? -1.0 : 0.0;
        Vec3 want = dir.scale(approach).add(side.scale(0.6));
        // im Saal bleiben
        Vec3 center = this.center();
        Vec3 fromCenter = this.position().subtract(center).multiply(1, 0, 1);
        double radius = this.home != null ? ROOM_RADIUS : 12.0;
        if (fromCenter.length() > radius - 1.5) {
            want = want.add(fromCenter.normalize().scale(-1.5));
        }
        if (want.lengthSqr() > 1.0e-4) {
            want = want.normalize().scale(0.11);
        }
        Vec3 v = this.getDeltaMovement();
        this.setDeltaMovement(v.x * 0.7 + want.x, v.y, v.z * 0.7 + want.z);
    }

    /** Dreht Körper und Kopf zum Punkt (höchstens {@code step} Grad je Tick). */
    private void face(Vec3 point, float step) {
        Vec3 d = point.subtract(this.getEyePosition());
        float yaw = (float) (Mth.atan2(d.z, d.x) * Mth.RAD_TO_DEG) - 90.0f;
        float pitch = (float) -(Mth.atan2(d.y, d.horizontalDistance()) * Mth.RAD_TO_DEG);
        this.setYRot(Mth.approachDegrees(this.getYRot(), yaw, step));
        this.setXRot(Mth.clamp(pitch, -60.0f, 60.0f));
        this.yBodyRot = this.getYRot();
        this.yHeadRot = this.getYRot();
    }

    /** Sofort auf das Ziel richten (vor einem Zauber, damit er trifft). */
    private void aimAt(LivingEntity target) {
        Vec3 d = target.getBoundingBox().getCenter().subtract(this.getEyePosition());
        float yaw = (float) (Mth.atan2(d.z, d.x) * Mth.RAD_TO_DEG) - 90.0f;
        float pitch = (float) -(Mth.atan2(d.y, d.horizontalDistance()) * Mth.RAD_TO_DEG);
        this.setYRot(yaw);
        this.setXRot(pitch);
        this.yBodyRot = yaw;
        this.yHeadRot = yaw;
    }

    private void updatePhase(ServerLevel level) {
        float f = this.getHealth() / this.getMaxHealth();
        int phase = f < 0.33f ? 3 : f < 0.66f ? 2 : 1;
        if (phase > this.phase()) {
            this.entityData.set(DATA_PHASE, (byte) phase);
            this.beginTransition(level);
        }
    }

    /** Phasenwechsel: unverwundbar, stößt alle von sich, ein Ruf in der Farbe seines Elements. */
    private void beginTransition(ServerLevel level) {
        this.transition = TRANSITION_TICKS;
        this.attack = null;
        this.setGesture(ANIM_TRANSITION);
        level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.EVOKER_PREPARE_WOLOLO, SoundSource.HOSTILE, 2.2f, 0.6f);
        level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.BEACON_POWER_SELECT, SoundSource.HOSTILE, 2.0f, 0.5f);
        Component line = Component.translatable("boss.kingdomsatwar." + this.bossId() + ".phase" + this.phase()).withColor(this.element.color());
        for (ServerPlayer player : this.nearbyPlayers(level, LEASH_RADIUS)) {
            player.sendOverlayMessage(line);
        }
    }

    private void tickTransition(ServerLevel level) {
        int t = TRANSITION_TICKS - this.transition;
        this.setDeltaMovement(0, this.getDeltaMovement().y, 0);
        if (t % 2 == 0) {
            BossFx.ring(level, new DustParticleOptions(this.element.color(), 1.6f), this.position().add(0, 0.2, 0), 0.6 + t * 0.25,
                    20 + t, 0.0);
        }
        if (t == 12) {
            for (LivingEntity e : this.enemiesAround(this.position(), 7.0, 4.0)) {
                DragonBossEntity.pushAway(e, this.position(), 1.4, 0.5);
                this.hurtTarget(level, e, 4.0f);
            }
            BossFx.impact(level, this.element.particle(), this.position().add(0, 1.0, 0), 80, 2.5, 1.0, 2.5, 0.12);
            BossFx.impact(level, ColorParticleOption.create(ParticleTypes.FLASH, 0xFF000000 | this.element.color()), this.position().add(0, 1.2, 0),
                    1, 0.0, 0.0, 0.0, 0.0);
            level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 1.6f, 1.2f);
            this.shake(level, 24.0, 0.6f, 16);
        }
        if (--this.transition <= 0) {
            this.setGesture(ANIM_NONE);
            this.idle = 6;
        }
    }

    // ------------------------------------------------------------------ Angriffe

    private int duration(Attack attack) {
        return switch (attack) {
            case MISSILES -> this.phase() >= 3 ? 40 : this.phase() == 2 ? 34 : 28;
            case SPELL -> 26;
            case BLINK -> 16;
            case IMAGES -> 30;
            case SHIELD -> 22;
            case ULTIMATE -> MageUltimates.duration(this.element);
        };
    }

    private int cooldownOf(Attack attack) {
        return switch (attack) {
            case MISSILES -> 50;
            case SPELL -> 70;
            case BLINK -> 120;
            case IMAGES -> 420;
            case SHIELD -> 380;
            case ULTIMATE -> this.phase() >= 3 ? 320 : 520;
        };
    }

    private void chooseAttack(ServerLevel level, LivingEntity target) {
        double dist = target.distanceTo(this);
        Map<Attack, Float> weights = new EnumMap<>(Attack.class);
        weights.put(Attack.MISSILES, 4.0f);
        if (this.spellReaches(dist)) {
            weights.put(Attack.SPELL, 4.0f);
        }
        weights.put(Attack.BLINK, dist < 4.0 ? 5.0f : 1.0f);
        if (this.phase() >= 2) {
            weights.put(Attack.IMAGES, this.images.isEmpty() ? 3.0f : 0.0f);
            weights.put(Attack.SHIELD, this.shielded() ? 0.0f : 2.5f);
            weights.put(Attack.ULTIMATE, this.phase() >= 3 ? 6.0f : 2.0f);
        }
        float total = 0.0f;
        for (Map.Entry<Attack, Float> e : weights.entrySet()) {
            if (this.cooldowns.getOrDefault(e.getKey(), 0) > 0) {
                e.setValue(0.0f);
            }
            total += e.getValue();
        }
        if (total <= 0.0f) {
            this.idle = 10;
            return;
        }
        float roll = this.random.nextFloat() * total;
        for (Map.Entry<Attack, Float> e : weights.entrySet()) {
            roll -= e.getValue();
            if (roll <= 0.0f && e.getValue() > 0.0f) {
                this.start(e.getKey());
                return;
            }
        }
    }

    /** Reicht der Zauber des Elements bis zum Ziel? (Frost, Erdstoß und Woge nur aus der Nähe) */
    private boolean spellReaches(double dist) {
        return switch (this.element) {
            case ICE -> dist < 8.0;
            case EARTH -> dist < 5.5;
            case WATER -> dist < 7.0;
            case NATURE -> true;
            default -> dist < 30.0;
        };
    }

    private void start(Attack attack) {
        this.attack = attack;
        this.attackTicks = 0;
        this.setGesture(switch (attack) {
            case MISSILES -> ANIM_MISSILES;
            case SPELL -> ANIM_CAST;
            case BLINK -> ANIM_NONE;
            case IMAGES -> ANIM_SUMMON;
            case SHIELD -> ANIM_SHIELD;
            case ULTIMATE -> ANIM_ULTIMATE;
        });
        if (attack == Attack.BLINK) {
            this.blinkTo = null;
        }
    }

    private void finishAttack() {
        if (this.attack != null) {
            this.cooldowns.put(this.attack, Math.round(this.cooldownOf(this.attack) * this.tempo()));
        }
        this.attack = null;
        this.setGesture(ANIM_NONE);
        this.idle = Math.round((14 + this.random.nextInt(20)) * this.tempo());
    }

    private void tickAttack(ServerLevel level, LivingEntity target) {
        int t = this.attackTicks++;
        Attack current = this.attack;
        if (current == null) {
            return;
        }
        if (current != Attack.BLINK && current != Attack.ULTIMATE) {
            this.setDeltaMovement(this.getDeltaMovement().multiply(0.5, 1.0, 0.5));
        }
        switch (current) {
            case MISSILES -> this.tickMissiles(level, target, t);
            case SPELL -> this.tickSpell(level, target, t);
            case BLINK -> this.tickBlink(level, target, t);
            case IMAGES -> this.tickImages(level, target, t);
            case SHIELD -> this.tickShield(level, t);
            case ULTIMATE -> MageUltimates.tick(level, this, target, t);
        }
        if (this.attack == current && t + 1 >= this.duration(current)) {
            this.finishAttack();
        }
    }

    /** Geschosse aus reiner Magie, eins nach dem anderen, die ihr Ziel verfolgen. */
    private void tickMissiles(ServerLevel level, LivingEntity target, int t) {
        int count = 3 + (this.phase() - 1);
        if (t >= 8 && (t - 8) % 6 == 0 && (t - 8) / 6 < count) {
            Vec3 from = this.staffTip();
            Vec3 dir = target.getBoundingBox().getCenter().subtract(from).normalize();
            Vec3 spread = new Vec3(-dir.z, 0.35, dir.x).scale((this.random.nextDouble() - 0.5) * 0.9);
            MagicMissile.shoot(level, this, target, from, dir.add(spread), 0.55, 4.5f * this.damageMultiplier());
            level.playSound(null, from.x, from.y, from.z, SoundEvents.ILLUSIONER_CAST_SPELL, SoundSource.HOSTILE, 1.2f,
                    1.2f + this.random.nextFloat() * 0.3f);
        }
    }

    /** Der Zauber seines Elements (auf der Stufe seiner Kampfphase). */
    private void tickSpell(ServerLevel level, LivingEntity target, int t) {
        if (t == 4) {
            level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.EVOKER_PREPARE_ATTACK, SoundSource.HOSTILE, 1.5f, 1.1f);
        }
        if (t < 12 && t % 2 == 0) {
            Vec3 tip = this.staffTip();
            level.sendParticles(new DustParticleOptions(this.element.color(), 1.0f), tip.x, tip.y, tip.z, 4, 0.2, 0.2, 0.2, 0.0);
        }
        if (t == 12) {
            this.aimAt(target);
            Spell spell = Spell.of(this.element);
            if (!spell.cast(level, this, this.phase())) {
                Spell.AETHER_BOLT.cast(level, this, this.phase());
            }
        }
    }

    /** Blinzeln: löst sich in Funken auf und taucht an anderer Stelle im Saal wieder auf. */
    private void tickBlink(ServerLevel level, LivingEntity target, int t) {
        this.setDeltaMovement(0, this.getDeltaMovement().y, 0);
        if (t < 6) {
            this.entityData.set(DATA_BLINK, t + 1);
        }
        if (t == 0) {
            this.blinkTo = this.findBlinkSpot(level, target);
            level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ILLUSIONER_MIRROR_MOVE, SoundSource.HOSTILE, 1.6f, 1.0f);
        }
        if (t == 6 && this.blinkTo != null) {
            this.burst(level);
            this.snapTo(this.blinkTo.x, this.blinkTo.y, this.blinkTo.z, this.getYRot(), this.getXRot());
            this.burst(level);
            level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 1.0f, 1.5f);
        }
        if (t > 6) {
            this.entityData.set(DATA_BLINK, Math.max(0, 12 - t));
        }
    }

    private void burst(ServerLevel level) {
        BossFx.impact(level, new DustParticleOptions(this.element.color(), 1.5f), new Vec3(this.getX(), this.getY(1.0), this.getZ()), 40, 0.4, 0.9,
                0.4, 0.05);
        BossFx.impact(level, ParticleTypes.REVERSE_PORTAL, new Vec3(this.getX(), this.getY(0.5), this.getZ()), 30, 0.4, 0.8, 0.4, 0.1);
    }

    /** Ein freier Platz im Saal, nicht zu nah am Ziel. */
    public Vec3 findBlinkSpot(ServerLevel level, @Nullable LivingEntity target) {
        Vec3 center = this.center();
        double radius = this.home != null ? ROOM_RADIUS - 1.5 : 8.0;
        for (int i = 0; i < 24; i++) {
            double a = this.random.nextDouble() * Mth.TWO_PI;
            double r = 1.5 + this.random.nextDouble() * (radius - 1.5);
            Vec3 spot = new Vec3(center.x + Math.cos(a) * r, this.getY(), center.z + Math.sin(a) * r);
            if (target != null && spot.distanceToSqr(target.position()) < 25.0) {
                continue;
            }
            BlockPos pos = BlockPos.containing(spot);
            if (level.noCollision(this, this.getBoundingBox().move(spot.subtract(this.position())))
                    && !level.getBlockState(pos.below()).isAir()) {
                return spot;
            }
        }
        return this.position();
    }

    /** Spiegelbilder: zwei (in der letzten Phase drei) Abbilder, die mit schwachen Geschossen angreifen. */
    private void tickImages(ServerLevel level, LivingEntity target, int t) {
        if (t == 2) {
            level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ILLUSIONER_PREPARE_MIRROR, SoundSource.HOSTILE, 2.0f, 1.0f);
        }
        if (t == 16) {
            int count = this.phase() >= 3 ? 3 : 2;
            for (int i = 0; i < count; i++) {
                Vec3 spot = this.findBlinkSpot(level, target);
                MageImageEntity image = MageImageEntity.spawn(level, this, spot);
                if (image != null) {
                    this.images.add(image.getUUID());
                }
            }
            // und er selbst springt zwischen seine Abbilder
            this.burst(level);
            Vec3 spot = this.findBlinkSpot(level, target);
            this.snapTo(spot.x, spot.y, spot.z, this.getYRot(), this.getXRot());
            this.burst(level);
            level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ILLUSIONER_MIRROR_MOVE, SoundSource.HOSTILE, 2.0f, 0.8f);
        }
    }

    /** Schild aus kreisenden Runen: fängt Schläge ab, bis er zerbricht oder verglimmt. */
    private void tickShield(ServerLevel level, int t) {
        if (t == 8) {
            this.entityData.set(DATA_SHIELD, SHIELD_TICKS);
            this.shieldHits = 0;
            level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.HOSTILE, 2.0f, 0.6f);
            level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.BEACON_POWER_SELECT, SoundSource.HOSTILE, 1.4f, 1.6f);
        }
    }

    private void breakShield(ServerLevel level) {
        this.entityData.set(DATA_SHIELD, 0);
        this.shieldHits = 0;
        level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.GLASS_BREAK, SoundSource.HOSTILE, 2.0f, 0.7f);
        BossFx.impact(level, new DustParticleOptions(this.element.color(), 1.6f), new Vec3(this.getX(), this.getY(1.0), this.getZ()), 50, 0.9, 1.0,
                0.9, 0.1);
    }

    /** Spitze des Stabs (rechte Hand, über dem Kopf). */
    public Vec3 staffTip() {
        float yaw = this.yBodyRot * Mth.DEG_TO_RAD;
        double sx = -Mth.cos(yaw) * 0.55 * 1.3;
        double sz = -Mth.sin(yaw) * 0.55 * 1.3;
        Vec3 forward = Vec3.directionFromRotation(0.0f, this.yBodyRot).scale(0.4);
        return this.position().add(sx + forward.x, 2.75, sz + forward.z);
    }

    // ------------------------------------------------------------------ Rückkehr

    /** Kein Gegner mehr im Saal: zurück an den Platz, heilen, wieder meditieren. */
    private void resetToHome(ServerLevel level) {
        this.burst(level);
        if (this.home != null) {
            this.snapTo(Vec3.atBottomCenterOf(this.home));
        }
        this.burst(level);
        for (UUID id : this.images) {
            if (level.getEntity(id) instanceof MageImageEntity image) {
                image.vanish(level);
            }
        }
        this.images.clear();
        this.threat.clear();
        this.participants.clear();
        this.attack = null;
        this.target = null;
        this.entityData.set(DATA_SHIELD, 0);
        this.entityData.set(DATA_PHASE, (byte) 1);
        this.setHealth(this.getMaxHealth());
        this.setGesture(ANIM_NONE);
        this.setState(State.DORMANT);
        this.lostTicks = 0;
    }

    // =====================================================================================
    // Tod
    // =====================================================================================

    @Override
    protected void handleKillingBlow() {
        super.handleKillingBlow();
        this.entityData.set(DATA_STATE, (byte) State.DYING.ordinal());
        this.entityData.set(DATA_SHIELD, 0);
        this.setGesture(ANIM_NONE);
        this.attack = null;
    }

    @Override
    protected void dropAllDeathLoot(ServerLevel level, DamageSource source) {
        this.deathSource = source;      // Beute erst am Ende der Todesanimation
    }

    @Override
    protected void tickDeath() {
        this.deathTime++;
        if (!(this.level() instanceof ServerLevel level)) {
            return;
        }
        if (this.deathTime == 1) {
            for (ServerPlayer player : this.nearbyPlayers(level, LEASH_RADIUS)) {
                this.participants.add(player.getUUID());
            }
            for (UUID id : this.images) {
                if (level.getEntity(id) instanceof MageImageEntity image) {
                    image.vanish(level);
                }
            }
            level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.EVOKER_DEATH, SoundSource.HOSTILE, 2.0f, 0.6f);
        }
        if (this.deathTime % 3 == 0) {
            Vec3 body = new Vec3(this.getX(), this.getY(0.5), this.getZ());
            BossFx.impact(level, new DustParticleOptions(this.element.color(), 1.8f), body, 12, 0.5, 1.0, 0.5, 0.05);
            BossFx.impact(level, this.element.particle(), body, 6, 0.6, 1.0, 0.6, 0.06);
        }
        if (this.deathTime >= DEATH_TICKS && !this.isRemoved()) {
            this.finishDeath(level);
        }
    }

    private void finishDeath(ServerLevel level) {
        DamageSource source = this.deathSource != null ? this.deathSource : this.damageSources().generic();
        super.dropAllDeathLoot(level, source);
        for (int i = 0; i < 8; i++) {
            ExperienceOrb.award(level, this.position().add(0, 1, 0), 50);
        }
        Vec3 body = new Vec3(this.getX(), this.getY(0.5), this.getZ());
        BossFx.impact(level, ParticleTypes.EXPLOSION_EMITTER, new Vec3(this.getX(), this.getY(1.0), this.getZ()), 1, 0, 0, 0, 0);
        BossFx.impact(level, this.element.particle(), body, 120, 1.5, 1.5, 1.5, 0.15);
        BossFx.impact(level, new DustParticleOptions(this.element.color(), 2.0f), body, 80, 1.2, 1.5, 1.2, 0.1);
        level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 2.0f, 1.3f);
        level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.BEACON_DEACTIVATE, SoundSource.HOSTILE, 2.0f, 0.8f);
        Component line = Component.translatable("boss.kingdomsatwar.mage.defeated",
                Component.translatable(this.getType().getDescriptionId()).withColor(this.element.color())).withStyle(ChatFormatting.GRAY);
        for (ServerPlayer player : level.players()) {
            if (this.participants.contains(player.getUUID())) {
                player.sendSystemMessage(line);
            }
        }
        this.shake(level, 30.0, 0.5f, 14);
        this.remove(Entity.RemovalReason.KILLED);
    }

    // =====================================================================================
    // Hilfen
    // =====================================================================================

    public List<ServerPlayer> nearbyPlayers(ServerLevel level, double radius) {
        Vec3 center = this.center();
        List<ServerPlayer> list = new ArrayList<>();
        for (ServerPlayer player : level.players()) {
            if (!player.isSpectator() && player.position().distanceToSqr(center) < radius * radius) {
                list.add(player);
            }
        }
        return list;
    }

    public void shake(ServerLevel level, double radius, float strength, int ticks) {
        for (ServerPlayer player : level.players()) {
            double dist = player.distanceTo(this);
            if (dist < radius) {
                io.github.sirschrolli.kingdomsatwar.network.ModNetwork.send(player,
                        new ScreenShakePayload(strength * (float) (1.0 - dist / radius * 0.7), ticks));
            }
        }
    }

    public int groundY(double x, double z) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(Mth.floor(x), Mth.floor(this.getY() + 2), Mth.floor(z));
        for (int i = 0; i < 10 && this.level().getBlockState(pos).isAir(); i++) {
            pos.move(0, -1, 0);
        }
        return pos.getY() + 1;
    }

    // =====================================================================================
    // Bossleiste
    // =====================================================================================

    private void updateBossBar(ServerLevel level) {
        State state = this.state();
        this.bossEvent.setProgress(state == State.AWAKENING ? Math.min(1.0f, this.stateTicks / (float) AWAKEN_TICKS)
                : this.getHealth() / this.getMaxHealth());
        Set<ServerPlayer> wanted = new HashSet<>();
        if (state != State.DORMANT && this.isAlive()) {
            for (ServerPlayer player : level.players()) {
                if (player.distanceTo(this) < 48.0 && player.level() == level) {
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

    /** Nur für Vorführungen und Tests: sofort im Kampf. */
    public void devFight(ServerLevel level) {
        if (this.home == null) {
            this.home = this.blockPosition();
        }
        this.setState(State.FIGHT);
        this.setGesture(ANIM_NONE);
        this.updateBossBar(level);
    }

    /** Nur für Vorführungen: eine Geste zeigen. */
    public void devGesture(byte gesture) {
        this.setGesture(gesture);
    }

    /** Nur für Vorführungen: Schild an. */
    public void devShield() {
        this.entityData.set(DATA_SHIELD, SHIELD_TICKS);
    }

    /**
     * Nur für Vorführungen: sofort einen Angriff beginnen (0 Geschosse, 1 Zauber, 2 Blinzeln, 3 Spiegelbilder, 4 Schild,
     * 5 großer Zauber).
     */
    public void devAttack(int index) {
        if (this.state() == State.FIGHT && this.transition <= 0 && this.target != null) {
            this.start(Attack.values()[Mth.clamp(index, 0, Attack.values().length - 1)]);
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
}
