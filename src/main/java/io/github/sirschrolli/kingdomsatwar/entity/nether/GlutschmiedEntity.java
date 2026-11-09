package io.github.sirschrolli.kingdomsatwar.entity.nether;

import io.github.sirschrolli.kingdomsatwar.block.nether.GlutAnvilBlockEntity;
import io.github.sirschrolli.kingdomsatwar.entity.creature.nether.Brandgebein;
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
 * Malleus, der Glutschmied: der Meisterschmied der Brandgebeine. Auf dem Dach der Aschenesse kniet er an seinem Amboss
 * und schmiedet am Glutspeer; wer ihm zu nahe kommt, den empfängt er mit Hammer und Speer:
 * <ul>
 *     <li><b>Speerstoß</b> – drei schnelle Stöße mit dem Glutspeer, die in Brand setzen;</li>
 *     <li><b>Hammerschlag</b> – er schmettert den Hammer auf den Boden, eine Schockwelle läuft als Ring über das Dach
 *     (wer im rechten Moment springt, entgeht ihr);</li>
 *     <li><b>Funkenflug</b> – Hammer auf Klinge: ein Kegel aus glühenden Funken;</li>
 *     <li><b>Glutwurf</b> – mit dem Hammer schleudert er Klumpen geschmolzenen Metalls im Bogen;</li>
 *     <li><b>Ruf der Gesellen</b> – Brandgebeine treten aus den Flammen und eilen ihm zu Hilfe;</li>
 *     <li><b>Esse anfachen</b> – unter halber Kraft stößt er den Speer in das Dach und wird weißglühend: schneller,
 *     härter, und wer ihm zu nahe kommt, verbrennt.</li>
 * </ul>
 * Findet er niemanden mehr, kehrt er an den Amboss zurück, legt den Speer darauf und schmiedet weiter.
 */
public class GlutschmiedEntity extends Monster {
    public static final float MAX_HEALTH = 560.0f;
    public static final int AWAKEN_TICKS = 60;
    /** So lange (Ticks) dauert sein Sterben. */
    public static final int DEATH_TICKS = 60;
    /** In diesem Tick des Erwachens nimmt er den Speer vom Amboss. */
    public static final int TAKE_SPEAR = 34;
    /** Alle so viele Ticks fällt am Amboss ein Hammerschlag (in Tick 15 des Takts). */
    public static final int FORGE_BEAT = 26;
    /** Weiter als so weit von seinem Platz am Amboss folgt er niemandem. */
    private static final double LEASH = 40.0;
    /** Das Dach der Esse: wer weiter fort gerät (oder hinunterfällt), kehrt in einer Flammensäule zurück. */
    private static final double ROOF = 19.0;
    /** So lange ohne Gegner, dann kehrt er an den Amboss zurück und schmiedet weiter. */
    private static final int RETURN_TICKS = 600;

    public enum Attack {
        NONE(0), FORGE(0), AWAKEN(AWAKEN_TICKS), THRUST(40), SLAM(36), SPARKS(50), GLOBS(42), CALL(30), STOKE(50);

        public final int duration;

        Attack(int duration) {
            this.duration = duration;
        }
    }

    private static final EntityDataAccessor<Integer> DATA_ATTACK = SynchedEntityData.defineId(GlutschmiedEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_STOKED = SynchedEntityData.defineId(GlutschmiedEntity.class,
            EntityDataSerializers.BOOLEAN);

    private final ServerBossEvent bossBar = new ServerBossEvent(UUID.randomUUID(), Component.translatable("boss.kingdomsatwar.forge"),
            BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.NOTCHED_10);
    private Attack attack = Attack.NONE;
    private Attack lastAttack = Attack.NONE;
    private int attackTick;
    private int cooldown = 30;
    private int calls;
    private int idleTicks;
    private @Nullable BlockPos home;
    private @Nullable BlockPos anvil;
    private final Set<UUID> slamHit = new HashSet<>();
    private int clientAttackStart;

    public GlutschmiedEntity(EntityType<? extends GlutschmiedEntity> type, Level level) {
        super(type, level);
        this.xpReward = 400;
        this.setPersistenceRequired();
        this.setPathfindingMalus(PathType.LAVA, -1.0f);
        this.setPathfindingMalus(PathType.DAMAGING, 0.0f);
        this.setPathfindingMalus(PathType.FIRE, 0.0f);
        this.setPathfindingMalus(PathType.FIRE_IN_NEIGHBOR, 0.0f);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, MAX_HEALTH)
                .add(Attributes.ARMOR, 12.0)
                .add(Attributes.ARMOR_TOUGHNESS, 4.0)
                .add(Attributes.ATTACK_DAMAGE, 12.0)
                .add(Attributes.MOVEMENT_SPEED, 0.26)
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
        builder.define(DATA_STOKED, false);
    }

    public Attack currentAttack() {
        int i = this.entityData.get(DATA_ATTACK);
        return i >= 0 && i < Attack.values().length ? Attack.values()[i] : Attack.NONE;
    }

    /** Weißglühend (nach dem Anfachen der Esse, unter halber Kraft). */
    public boolean isStoked() {
        return this.entityData.get(DATA_STOKED);
    }

    /** Wie lange der laufende Angriff schon dauert (im Client, für die Animationen). */
    public float attackTime(float partialTicks) {
        return this.tickCount - this.clientAttackStart + partialTicks;
    }

    /** Trägt er den Glutspeer (oder liegt der noch auf dem Amboss)? */
    public boolean holdsSpear(float attackTime) {
        Attack a = this.currentAttack();
        return a != Attack.FORGE && !(a == Attack.AWAKEN && attackTime < TAKE_SPEAR);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (DATA_ATTACK.equals(key)) {
            this.clientAttackStart = this.tickCount;
        }
    }

    /**
     * Bindet ihn an seinen Amboss: Er kniet bei {@code home} und schmiedet (unverwundbar, bis jemand näher kommt oder ihn
     * angreift).
     */
    public void bindTo(BlockPos home, BlockPos anvil) {
        this.home = home.immutable();
        this.anvil = anvil.immutable();
        this.begin(Attack.FORGE);
    }

    /** Er erwacht: richtet sich auf, nimmt den Speer vom Amboss und brüllt. */
    public void awaken() {
        if (this.attack == Attack.FORGE || this.attack == Attack.NONE && this.getTarget() == null) {
            this.begin(Attack.AWAKEN);
        }
    }

    public @Nullable BlockPos home() {
        return this.home;
    }

    public @Nullable BlockPos anvil() {
        return this.anvil;
    }

    /** Beginnt sofort einen bestimmten Angriff (für Tests und die Vorführung). */
    public void forceAttack(Attack next) {
        this.begin(next);
    }

    /** Wird weißglühend (für Tests und die Vorführung). */
    public void forceStoked() {
        this.entityData.set(DATA_STOKED, true);
    }

    private void begin(Attack next) {
        this.attack = next;
        this.attackTick = 0;
        this.entityData.set(DATA_ATTACK, next.ordinal());
        if (next != Attack.NONE && next != Attack.AWAKEN && next != Attack.STOKE && next != Attack.FORGE) {
            this.lastAttack = next;
        }
        if (next == Attack.SLAM) {
            this.slamHit.clear();
        }
        this.bossBar.setVisible(next != Attack.FORGE);
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
     * Am Amboss und beim Erwachen ist er unverwundbar (wer ihn beim Schmieden angreift, weckt ihn); Feuer, Lava und Wände
     * schaden ihm nie.
     */
    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return super.hurtServer(level, source, amount);
        }
        if (this.attack == Attack.FORGE) {
            if (source.getEntity() instanceof Player player && !player.isCreative() && !player.isSpectator()) {
                this.wakeAnvil(level);
            }
            return false;
        }
        if (this.attack == Attack.AWAKEN || source.is(DamageTypes.IN_WALL) || source.is(DamageTypeTags.IS_FIRE)) {
            return false;
        }
        boolean hurt = super.hurtServer(level, source, amount);
        if (hurt && source.getEntity() instanceof LivingEntity attacker && attacker != this && this.canStrike(attacker)
                && !(this.getTarget() instanceof Player)) {
            this.setTarget(attacker);
        }
        return hurt;
    }

    private void wakeAnvil(ServerLevel level) {
        if (this.anvil != null && level.getBlockEntity(this.anvil) instanceof GlutAnvilBlockEntity forge && forge.owns(this)) {
            forge.awaken(level);
        } else {
            this.begin(Attack.AWAKEN);
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide()) {
            this.clientEffects();
        }
    }

    /** Glut steigt von ihm auf; weißglühend lodern Flammen ringsum. */
    private void clientEffects() {
        Level level = this.level();
        boolean stoked = this.isStoked();
        if (this.random.nextInt(stoked ? 1 : 3) == 0) {
            level.addParticle(stoked ? ParticleTypes.FLAME : ParticleTypes.SMALL_FLAME, this.getRandomX(0.9),
                    this.getY(0.35 + this.random.nextDouble() * 0.6), this.getRandomZ(0.9), 0.0, 0.02, 0.0);
        }
        if (this.random.nextInt(stoked ? 3 : 7) == 0) {
            level.addParticle(ParticleTypes.LAVA, this.getRandomX(0.6), this.getY(0.7), this.getRandomZ(0.6), 0.0, 0.0, 0.0);
        }
        if (this.random.nextInt(4) == 0) {
            level.addParticle(ParticleTypes.SMOKE, this.getRandomX(0.5), this.getY(0.95), this.getRandomZ(0.5), 0.0, 0.04, 0.0);
        }
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        this.bossBar.setProgress(this.getHealth() / this.getMaxHealth());
        if (this.tickCount % 40 == 0 && this.strayed(level)) {
            return;
        }
        if (this.attack == Attack.FORGE) {
            this.tickForge(level);
            return;
        }
        if (this.attack == Attack.AWAKEN) {
            this.tickAwaken(level);
            return;
        }
        if (this.tickCount % 10 == 0) {
            this.keepOnRoof(level);
        }
        if (!this.isStoked() && this.attack != Attack.STOKE && this.getHealth() < this.getMaxHealth() * 0.5f) {
            this.getNavigation().stop();
            this.begin(Attack.STOKE);
        }
        if (this.attack == Attack.STOKE) {
            this.tickStoke(level);
            return;
        }
        if (this.isStoked() && this.tickCount % 20 == 0) {
            this.aura(level);
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
            double distance = this.distanceTo(target);
            if (distance > 3.4) {
                this.getNavigation().moveTo(target, this.isStoked() ? 1.25 : 1.0);
            } else {
                this.getNavigation().stop();
            }
            if (--this.cooldown <= 0) {
                this.getNavigation().stop();
                this.begin(this.chooseAttack(level, target));
            }
            return;
        }
        this.attackTick++;
        this.getNavigation().stop();
        this.lookAt(target, 40.0f, 40.0f);
        switch (this.attack) {
            case THRUST -> this.tickThrust(level, target);
            case SLAM -> this.tickSlam(level);
            case SPARKS -> this.tickSparks(level, target);
            case GLOBS -> this.tickGlobs(level, target);
            case CALL -> this.tickCall(level);
            default -> { }
        }
        if (this.attackTick >= this.attack.duration) {
            this.begin(Attack.NONE);
            this.cooldown = this.isStoked() ? 12 + this.random.nextInt(14) : 24 + this.random.nextInt(20);
        }
    }

    /**
     * Gehört er (noch) zu seinem Amboss? Ist dort inzwischen ein anderer Schmied gebunden (etwa nach dem Neuladen der
     * Welt), verglüht dieser hier; ist der Amboss fort, bleibt er frei.
     */
    private boolean strayed(ServerLevel level) {
        if (this.anvil == null || !level.isLoaded(this.anvil)) {
            return false;
        }
        if (level.getBlockEntity(this.anvil) instanceof GlutAnvilBlockEntity forge) {
            if (!forge.owns(this)) {
                level.sendParticles(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY(0.5), this.getZ(), 30, 0.6, 1.0, 0.6, 0.02);
                this.discard();
                return true;
            }
            return false;
        }
        this.anvil = null;
        if (this.attack == Attack.FORGE) {
            this.begin(Attack.NONE);
        }
        return false;
    }

    /** Wen seine Angriffe treffen: alle Lebewesen außer ihm, seinen Brandgebeinen und Spielern im Kreativ-/Zuschauermodus. */
    private boolean canStrike(LivingEntity entity) {
        return entity != this && entity.isAlive() && !(entity instanceof Brandgebein) && !(entity instanceof GlutschmiedEntity)
                && !(entity instanceof ArmorStand) && !(entity instanceof Player player && (player.isCreative() || player.isSpectator()));
    }

    private List<LivingEntity> victims(ServerLevel level, AABB area) {
        return level.getEntitiesOfClass(LivingEntity.class, area, this::canStrike);
    }

    private @Nullable LivingEntity findTarget(ServerLevel level) {
        if (this.home != null && this.distanceToSqr(Vec3.atCenterOf(this.home)) > LEASH * LEASH) {
            return null;
        }
        LivingEntity current = this.getTarget();
        if (current != null && current.isAlive() && current.distanceToSqr(this) < 48.0 * 48.0 && this.canStrike(current)) {
            return current;
        }
        return level.getNearestPlayer(this.getX(), this.getY(), this.getZ(), 32.0,
                e -> e instanceof Player p && !p.isCreative() && !p.isSpectator() && p.isAlive());
    }

    /** Vom Dach gestürzt oder zu weit fort: er kehrt in einer Flammensäule an seinen Platz zurück. */
    private void keepOnRoof(ServerLevel level) {
        if (this.home == null) {
            return;
        }
        double dx = this.getX() - (this.home.getX() + 0.5);
        double dz = this.getZ() - (this.home.getZ() + 0.5);
        if (dx * dx + dz * dz < ROOF * ROOF && this.getY() > this.home.getY() - 3.5) {
            return;
        }
        level.sendParticles(ParticleTypes.FLAME, this.getX(), this.getY(0.5), this.getZ(), 60, 0.6, 1.4, 0.6, 0.05);
        level.sendParticles(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY(0.5), this.getZ(), 20, 0.5, 1.0, 0.5, 0.02);
        this.getNavigation().stop();
        this.teleportTo(this.home.getX() + 0.5, this.home.getY(), this.home.getZ() + 0.5);
        this.setDeltaMovement(Vec3.ZERO);
        level.sendParticles(ParticleTypes.FLAME, this.getX(), this.getY(0.5), this.getZ(), 80, 0.6, 1.6, 0.6, 0.06);
        level.sendParticles(ParticleTypes.LAVA, this.getX(), this.getY() + 0.3, this.getZ(), 16, 0.8, 0.2, 0.8, 0.0);
        this.sound(level, ModSounds.GLUTSCHMIED_STOKE.get(), 2.5f, 1.3f);
    }

    /** Ohne Gegner: zurück an den Amboss; nach einer Weile heilt ihn die Glut, und er schmiedet weiter. */
    private void idle(ServerLevel level) {
        if (this.attack != Attack.NONE) {
            this.begin(Attack.NONE);
        }
        this.idleTicks++;
        if (this.home != null && this.distanceToSqr(Vec3.atBottomCenterOf(this.home)) > 1.5) {
            this.getNavigation().moveTo(this.home.getX() + 0.5, this.home.getY(), this.home.getZ() + 0.5, 0.9);
        } else if (this.home != null && this.anvil != null && this.idleTicks > RETURN_TICKS) {
            this.returnToForge(level);
            return;
        }
        if (this.idleTicks > 200 && this.tickCount % 20 == 0) {
            this.heal(4.0f);
        }
    }

    /** Legt den Speer zurück auf den Amboss, kniet nieder und schmiedet weiter – geheilt und abgekühlt. */
    private void returnToForge(ServerLevel level) {
        if (this.anvil == null || !(level.getBlockEntity(this.anvil) instanceof GlutAnvilBlockEntity forge) || !forge.owns(this)) {
            return;
        }
        this.getNavigation().stop();
        this.setHealth(this.getMaxHealth());
        this.entityData.set(DATA_STOKED, false);
        this.calls = 0;
        this.idleTicks = 0;
        BlockPos h = this.home;
        if (h != null) {
            float yaw = forge.facing().getOpposite().toYRot();
            this.snapTo(h.getX() + 0.5, h.getY(), h.getZ() + 0.5, yaw, 0.0f);
            this.yBodyRot = yaw;
            this.yHeadRot = yaw;
        }
        this.begin(Attack.FORGE);
        forge.onSmithReturned(level, this);
    }

    private Attack chooseAttack(ServerLevel level, LivingEntity target) {
        double distance = this.distanceTo(target);
        List<Attack> options = new ArrayList<>();
        if (distance < 5.5) {
            options.addAll(List.of(Attack.THRUST, Attack.THRUST, Attack.SLAM, Attack.SPARKS));
        } else if (distance < 11.0) {
            options.addAll(List.of(Attack.SLAM, Attack.SPARKS, Attack.GLOBS, Attack.GLOBS));
        } else {
            options.addAll(List.of(Attack.GLOBS, Attack.GLOBS, Attack.GLOBS));
        }
        if (this.calls < 3 && this.getHealth() < this.getMaxHealth() * 0.85f && this.apprentices(level) < 2) {
            options.add(Attack.CALL);
            options.add(Attack.CALL);
        }
        options.removeIf(a -> a == this.lastAttack);
        if (options.isEmpty()) {
            options.add(distance < 6.0 ? Attack.SLAM : Attack.GLOBS);
        }
        return options.get(this.random.nextInt(options.size()));
    }

    private int apprentices(ServerLevel level) {
        return level.getEntitiesOfClass(Brandgebein.class, this.getBoundingBox().inflate(24.0), LivingEntity::isAlive).size();
    }

    private Vec3 forward() {
        float yaw = this.getYRot() * Mth.DEG_TO_RAD;
        return new Vec3(-Mth.sin(yaw), 0.0, Mth.cos(yaw));
    }

    private void sound(ServerLevel level, SoundEvent sound, float volume, float pitch) {
        level.playSound(null, this.getX(), this.getY(), this.getZ(), sound, SoundSource.HOSTILE, volume, pitch);
    }

    /** Wo die Hammerschläge auf den Amboss fallen. */
    private Vec3 anvilTop() {
        if (this.anvil != null) {
            return Vec3.atBottomCenterOf(this.anvil).add(0.0, 1.02, 0.0);
        }
        return this.position().add(this.forward().scale(2.0)).add(0.0, 1.0, 0.0);
    }

    // ------------------------------------------------------------------ Schmieden und Erwachen

    /** Er kniet am Amboss und schmiedet: Hammerschläge mit Funkenregen, dann und wann zischt das Werkstück. */
    private void tickForge(ServerLevel level) {
        this.getNavigation().stop();
        this.setDeltaMovement(this.getDeltaMovement().multiply(0.0, 1.0, 0.0));
        int t = ++this.attackTick;
        if (t % FORGE_BEAT == 15) {
            Vec3 top = this.anvilTop();
            this.sound(level, ModSounds.GLUTSCHMIED_CLANG.get(), 1.8f, 0.92f + this.random.nextFloat() * 0.16f);
            level.sendParticles(ParticleTypes.LAVA, top.x, top.y, top.z, 4, 0.2, 0.05, 0.2, 0.0);
            level.sendParticles(ParticleTypes.FLAME, top.x, top.y + 0.1, top.z, 10, 0.25, 0.1, 0.25, 0.06);
            level.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, 0xFFFFB050), top.x, top.y + 0.1, top.z, 1, 0.0, 0.0, 0.0, 0.0);
        }
        if (t % (FORGE_BEAT * 5) == 2) {
            Vec3 top = this.anvilTop();
            level.playSound(null, top.x, top.y, top.z, SoundEvents.LAVA_EXTINGUISH, SoundSource.HOSTILE, 0.6f, 1.4f);
            level.sendParticles(ParticleTypes.LARGE_SMOKE, top.x, top.y + 0.2, top.z, 6, 0.2, 0.2, 0.2, 0.02);
        }
    }

    /** Er hält inne, richtet sich auf, nimmt den Speer vom Amboss, reckt Hammer und Speer empor und brüllt. */
    private void tickAwaken(ServerLevel level) {
        int t = ++this.attackTick;
        this.getNavigation().stop();
        Player nearest = level.getNearestPlayer(this, 24.0);
        if (nearest != null && t > 6) {
            this.getLookControl().setLookAt(nearest, 20.0f, 30.0f);
            if (t > TAKE_SPEAR + 4) {
                this.lookAt(nearest, 12.0f, 30.0f);
            }
        }
        if (t == 4) {
            this.sound(level, ModSounds.GLUTSCHMIED_AMBIENT.get(), 3.0f, 0.7f);
        }
        if (t == TAKE_SPEAR) {
            this.sound(level, ModSounds.GLUTSCHMIED_THRUST.get(), 2.0f, 0.7f);
            if (this.anvil != null && level.getBlockEntity(this.anvil) instanceof GlutAnvilBlockEntity forge) {
                forge.onSpearTaken(level);
            }
        }
        if (t == 46) {
            this.sound(level, ModSounds.GLUTSCHMIED_ROAR.get(), 4.0f, 1.0f);
            level.sendParticles(ParticleTypes.FLAME, this.getX(), this.getY(0.9), this.getZ(), 80, 0.9, 0.6, 0.9, 0.12);
            level.sendParticles(ParticleTypes.LAVA, this.getX(), this.getY(0.6), this.getZ(), 20, 1.0, 0.4, 1.0, 0.0);
            Component title = Component.translatable("boss.kingdomsatwar.forge").withColor(0xFF8A30);
            Component subtitle = Component.translatable("boss.kingdomsatwar.forge.title").withStyle(ChatFormatting.GRAY);
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
            this.cooldown = 16;
            this.idleTicks = 0;
        }
    }

    // ------------------------------------------------------------------ Angriffe

    /** Speerstoß: drei schnelle Stöße nach vorn (gut fünf Blöcke weit), jeder setzt in Brand. */
    private void tickThrust(ServerLevel level, LivingEntity target) {
        int t = this.attackTick;
        if (t != 9 && t != 19 && t != 29) {
            return;
        }
        Vec3 dir = target.position().subtract(this.position()).multiply(1.0, 0.0, 1.0);
        dir = dir.lengthSqr() < 1.0e-4 ? this.forward() : dir.normalize();
        this.setDeltaMovement(dir.scale(0.45).add(0.0, this.getDeltaMovement().y, 0.0));
        this.sound(level, ModSounds.GLUTSCHMIED_THRUST.get(), 2.0f, 0.95f + this.random.nextFloat() * 0.1f);
        Vec3 from = this.position().add(0.0, 2.2, 0.0);
        for (int i = 2; i <= 10; i++) {
            Vec3 p = from.add(dir.scale(i * 0.55));
            level.sendParticles(ParticleTypes.FLAME, p.x, p.y, p.z, 2, 0.05, 0.05, 0.05, 0.01);
        }
        float damage = this.isStoked() ? 12.0f : 9.0f;
        for (LivingEntity victim : this.victims(level, this.getBoundingBox().inflate(5.0, 1.5, 5.0))) {
            Vec3 to = victim.position().subtract(this.position()).multiply(1.0, 0.0, 1.0);
            double distance = to.length();
            if (distance > 5.6 || distance > 0.5 && to.normalize().dot(dir) < 0.55) {
                continue;
            }
            if (victim.hurtServer(level, this.damageSources().mobAttack(this), damage)) {
                victim.igniteForSeconds(4.0f);
                victim.push(dir.x * 0.5, 0.15, dir.z * 0.5);
                victim.needsSync = true;
            }
        }
    }

    /** Hammerschlag: Ausholen, dann läuft eine Schockwelle als Ring über das Dach – wer darin steht, fliegt. */
    private void tickSlam(ServerLevel level) {
        int t = this.attackTick;
        if (t == 17) {
            this.sound(level, ModSounds.GLUTSCHMIED_SLAM.get(), 3.5f, 0.9f);
            Vec3 hit = this.position().add(this.forward().scale(2.2));
            level.sendParticles(ParticleTypes.EXPLOSION, hit.x, this.getY() + 0.2, hit.z, 2, 0.4, 0.0, 0.4, 0.0);
            level.sendParticles(ParticleTypes.LAVA, hit.x, this.getY() + 0.2, hit.z, 14, 0.6, 0.1, 0.6, 0.0);
        }
        if (t < 17 || t > 31) {
            return;
        }
        double radius = 1.4 + (t - 17) * 0.72;
        BlockState floor = level.getBlockState(this.blockPosition().below());
        BlockParticleOption dust = new BlockParticleOption(ParticleTypes.BLOCK, floor.isAir() ? Blocks.BLACKSTONE.defaultBlockState() : floor);
        int points = 10 + (int) (radius * 5);
        for (int i = 0; i < points; i++) {
            double a = i * Math.PI * 2.0 / points;
            double x = this.getX() + Math.cos(a) * radius;
            double z = this.getZ() + Math.sin(a) * radius;
            level.sendParticles(dust, x, this.getY() + 0.1, z, 2, 0.1, 0.05, 0.1, 0.1);
            if (i % 3 == 0) {
                level.sendParticles(ParticleTypes.FLAME, x, this.getY() + 0.2, z, 1, 0.05, 0.05, 0.05, 0.02);
            }
        }
        float damage = this.isStoked() ? 10.0f : 8.0f;
        for (LivingEntity victim : this.victims(level, this.getBoundingBox().inflate(radius + 1.0, 2.0, radius + 1.0))) {
            double d = Math.hypot(victim.getX() - this.getX(), victim.getZ() - this.getZ());
            boolean grounded = victim.onGround() || victim.getY() - this.getY() < 0.6;
            if (Math.abs(d - radius) > 0.95 || !grounded || !this.slamHit.add(victim.getUUID())) {
                continue;
            }
            if (victim.hurtServer(level, this.damageSources().mobAttack(this), damage)) {
                Vec3 out = new Vec3(victim.getX() - this.getX(), 0.0, victim.getZ() - this.getZ()).normalize();
                victim.push(out.x * 0.5, 0.65, out.z * 0.5);
                victim.needsSync = true;
            }
        }
    }

    /** Funkenflug: Hammer auf Klinge – ein Kegel glühender Funken in Blickrichtung (gut acht Blöcke weit). */
    private void tickSparks(ServerLevel level, LivingEntity target) {
        int t = this.attackTick;
        if (t < 10 || t > 42) {
            return;
        }
        Vec3 dir = target.position().subtract(this.position()).multiply(1.0, 0.0, 1.0);
        dir = dir.lengthSqr() < 1.0e-4 ? this.forward() : dir.normalize();
        if (t % 12 == 10) {
            this.sound(level, ModSounds.GLUTSCHMIED_SPARKS.get(), 2.5f, 0.9f + this.random.nextFloat() * 0.2f);
        }
        Vec3 from = this.position().add(dir.scale(1.3)).add(0.0, 2.0, 0.0);
        for (int i = 0; i < 14; i++) {
            double spread = (this.random.nextDouble() - 0.5) * 1.1;
            Vec3 v = dir.yRot((float) spread).scale(0.45 + this.random.nextDouble() * 0.5).add(0.0, (this.random.nextDouble() - 0.55) * 0.18, 0.0);
            level.sendParticles(i % 3 == 0 ? ParticleTypes.LAVA : ParticleTypes.FLAME, from.x, from.y, from.z, 0, v.x, v.y, v.z, 1.0);
        }
        if (t % 3 != 0) {
            return;
        }
        for (LivingEntity victim : this.victims(level, this.getBoundingBox().inflate(8.5, 3.0, 8.5))) {
            Vec3 to = victim.position().subtract(this.position()).multiply(1.0, 0.0, 1.0);
            double distance = to.length();
            if (distance > 8.8 || distance > 0.5 && to.normalize().dot(dir) < 0.82) {
                continue;
            }
            if (victim.hurtServer(level, this.damageSources().mobAttack(this), this.isStoked() ? 4.0f : 3.0f)) {
                victim.igniteForSeconds(3.0f);
            }
        }
    }

    /** Glutwurf: mit dem Hammer schleudert er Klumpen geschmolzenen Metalls im Bogen dorthin, wo das Ziel gleich sein wird. */
    private void tickGlobs(ServerLevel level, LivingEntity target) {
        int t = this.attackTick;
        boolean throwNow = t == 12 || t == 20 || t == 28 || this.isStoked() && t == 36;
        if (!throwNow) {
            return;
        }
        this.sound(level, ModSounds.GLUTSCHMIED_THROW.get(), 2.0f, 0.9f + this.random.nextFloat() * 0.2f);
        GlutklumpenEntity glob = new GlutklumpenEntity(level, this);
        glob.setDamage(this.isStoked() ? 8.0f : 6.0f);
        // der Hammer (in der linken Hand) schwingt von unten nach vorn oben
        Vec3 left = this.forward().yRot(Mth.HALF_PI);
        Vec3 hand = this.position().add(this.forward().scale(0.9)).add(left.scale(1.1)).add(0.0, 3.1, 0.0);
        Vec3 lead = target.position().add(target.getDeltaMovement().multiply(10.0, 0.0, 10.0));
        if (t != 12) {
            // die späteren Klumpen streuen ein wenig um das Ziel
            lead = lead.add((this.random.nextDouble() - 0.5) * 3.0, 0.0, (this.random.nextDouble() - 0.5) * 3.0);
        }
        double flat = Math.hypot(lead.x - hand.x, lead.z - hand.z);
        glob.lob(hand, lead, Mth.clamp(0.7 + flat * 0.028, 0.75, 1.35));
        level.addFreshEntity(glob);
        level.sendParticles(ParticleTypes.FLAME, hand.x, hand.y, hand.z, 12, 0.2, 0.2, 0.2, 0.05);
    }

    /** Ruf der Gesellen: er reckt den Speer empor, stößt ihn auf das Dach – und zwei Brandgebeine treten aus den Flammen. */
    private void tickCall(ServerLevel level) {
        int t = this.attackTick;
        if (t == 4) {
            this.sound(level, ModSounds.GLUTSCHMIED_CALL.get(), 3.5f, 1.0f);
        }
        if (t != 15) {
            return;
        }
        this.calls++;
        level.sendParticles(ParticleTypes.LAVA, this.getX(), this.getY() + 0.2, this.getZ(), 10, 0.6, 0.1, 0.6, 0.0);
        for (int i = 0; i < 2; i++) {
            Brandgebein apprentice = ModEntities.WITHER_SKELETON.get().create(level, EntitySpawnReason.MOB_SUMMONED);
            if (apprentice == null) {
                continue;
            }
            Vec3 spot = this.freeSpot(level, apprentice, i == 0 ? 1.0 : -1.0);
            apprentice.snapTo(spot.x, spot.y, spot.z, this.getYRot(), 0.0f);
            apprentice.finalizeSpawn(level, level.getCurrentDifficultyAt(apprentice.blockPosition()), EntitySpawnReason.MOB_SUMMONED, null);
            apprentice.setPersistenceRequired();
            if (this.getTarget() != null) {
                apprentice.setTarget(this.getTarget());
            }
            level.addFreshEntity(apprentice);
            level.sendParticles(ParticleTypes.FLAME, spot.x, spot.y + 1.0, spot.z, 50, 0.4, 0.9, 0.4, 0.05);
            level.sendParticles(ParticleTypes.LARGE_SMOKE, spot.x, spot.y + 1.0, spot.z, 12, 0.3, 0.6, 0.3, 0.02);
        }
    }

    /** Ein freier Platz neben ihm (links oder rechts), auf dem ein Brandgebein stehen kann. */
    private Vec3 freeSpot(ServerLevel level, Brandgebein apprentice, double side) {
        float yaw = this.getYRot() * Mth.DEG_TO_RAD;
        for (int i = 0; i < 8; i++) {
            double a = yaw + side * (1.2 + i * 0.45);
            for (double r = 3.0; r >= 2.0; r -= 0.5) {
                double x = this.getX() - Math.sin(a) * r;
                double z = this.getZ() + Math.cos(a) * r;
                BlockPos below = BlockPos.containing(x, this.getY() - 0.5, z);
                AABB box = apprentice.getType().getSpawnAABB(x, this.getY(), z);
                if (level.getBlockState(below).isFaceSturdy(level, below, Direction.UP) && level.noCollision(apprentice, box)) {
                    return new Vec3(x, this.getY(), z);
                }
            }
        }
        return this.position();
    }

    /** Esse anfachen: er stößt den Speer ins Dach, Flammen brechen ringsum hervor, und er wird weißglühend. */
    private void tickStoke(ServerLevel level) {
        int t = ++this.attackTick;
        this.getNavigation().stop();
        if (t == 4) {
            this.sound(level, ModSounds.GLUTSCHMIED_ROAR.get(), 4.0f, 0.8f);
        }
        if (t == 18) {
            this.sound(level, ModSounds.GLUTSCHMIED_STOKE.get(), 4.0f, 1.0f);
            this.entityData.set(DATA_STOKED, true);
            for (int i = 0; i < 48; i++) {
                double a = i * Math.PI * 2.0 / 48;
                for (double r = 1.5; r <= 4.5; r += 1.5) {
                    level.sendParticles(ParticleTypes.FLAME, this.getX() + Math.cos(a) * r, this.getY() + 0.2, this.getZ() + Math.sin(a) * r, 2,
                            0.05, 0.3, 0.05, 0.05);
                }
            }
            level.sendParticles(ParticleTypes.LAVA, this.getX(), this.getY() + 0.5, this.getZ(), 30, 1.5, 0.3, 1.5, 0.0);
            level.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, 0xFFFFE0A0), this.getX(), this.getY(0.5), this.getZ(), 1, 0.0,
                    0.0, 0.0, 0.0);
            for (LivingEntity victim : this.victims(level, this.getBoundingBox().inflate(4.5, 2.0, 4.5))) {
                if (victim.hurtServer(level, this.damageSources().mobAttack(this), 6.0f)) {
                    victim.igniteForSeconds(5.0f);
                    Vec3 out = new Vec3(victim.getX() - this.getX(), 0.0, victim.getZ() - this.getZ()).normalize();
                    victim.push(out.x, 0.5, out.z);
                    victim.needsSync = true;
                }
            }
        }
        if (t >= Attack.STOKE.duration) {
            this.begin(Attack.NONE);
            this.cooldown = 10;
        }
    }

    /** Weißglühend: wer ihm zu nahe kommt, verbrennt. */
    private void aura(ServerLevel level) {
        for (int i = 0; i < 16; i++) {
            double a = i * Math.PI * 2.0 / 16;
            level.sendParticles(ParticleTypes.FLAME, this.getX() + Math.cos(a) * 3.0, this.getY() + 0.3, this.getZ() + Math.sin(a) * 3.0, 1,
                    0.0, 0.1, 0.0, 0.01);
        }
        for (LivingEntity victim : this.victims(level, this.getBoundingBox().inflate(3.2, 1.5, 3.2))) {
            victim.hurtServer(level, this.damageSources().onFire(), 2.0f);
            victim.igniteForSeconds(2.0f);
        }
    }

    // ------------------------------------------------------------------ Tod, Speichern, Klänge

    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (this.level() instanceof ServerLevel level) {
            level.sendParticles(ParticleTypes.LAVA, this.getX(), this.getY(1.0), this.getZ(), 40, 1.2, 1.0, 1.2, 0.0);
            level.sendParticles(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY(0.5), this.getZ(), 60, 1.0, 1.4, 1.0, 0.03);
            level.sendParticles(ParticleTypes.FLAME, this.getX(), this.getY(0.5), this.getZ(), 120, 1.2, 1.5, 1.2, 0.1);
            if (this.anvil != null && level.getBlockEntity(this.anvil) instanceof GlutAnvilBlockEntity forge && forge.owns(this)) {
                forge.onSmithDefeated(level);
            }
        }
    }

    /** Er sinkt auf die Knie; Glut und Rauch steigen aus ihm auf, bis die Esse in seiner Brust erlischt und er zerfällt. */
    @Override
    protected void tickDeath() {
        this.deathTime++;
        this.setDeltaMovement(0.0, this.getDeltaMovement().y, 0.0);
        if (!(this.level() instanceof ServerLevel level)) {
            return;
        }
        if (this.deathTime % 4 == 0) {
            level.sendParticles(ParticleTypes.FLAME, this.getX(), this.getY(0.5), this.getZ(), 10, 0.7, 1.0, 0.7, 0.03);
            level.sendParticles(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY(0.6), this.getZ(), 6, 0.6, 0.8, 0.6, 0.03);
            level.sendParticles(ParticleTypes.LAVA, this.getX(), this.getY(0.4), this.getZ(), 2, 0.6, 0.4, 0.6, 0.0);
        }
        if (this.deathTime == DEATH_TICKS / 2) {
            level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.FIRE_EXTINGUISH, SoundSource.HOSTILE, 3.0f, 0.5f);
        }
        if (this.deathTime == DEATH_TICKS - 2) {
            level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.BASALT_BREAK, SoundSource.HOSTILE, 3.0f, 0.5f);
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.BASALT.defaultBlockState()), this.getX(), this.getY(0.4),
                    this.getZ(), 120, 0.8, 1.0, 0.8, 0.2);
            level.sendParticles(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY(0.4), this.getZ(), 40, 0.8, 1.0, 0.8, 0.05);
            level.sendParticles(ParticleTypes.LAVA, this.getX(), this.getY(0.4), this.getZ(), 24, 0.8, 0.6, 0.8, 0.0);
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
        if (this.anvil != null) {
            output.putLong("Anvil", this.anvil.asLong());
        }
        output.putBoolean("Stoked", this.isStoked());
        output.putBoolean("Forging", this.attack == Attack.FORGE);
        output.putInt("Calls", this.calls);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        input.getLong("Home").ifPresent(h -> this.home = BlockPos.of(h));
        input.getLong("Anvil").ifPresent(a -> this.anvil = BlockPos.of(a));
        this.entityData.set(DATA_STOKED, input.getBooleanOr("Stoked", false));
        this.calls = input.getIntOr("Calls", 0);
        if (input.getBooleanOr("Forging", false)) {
            this.begin(Attack.FORGE);
        }
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return this.attack == Attack.FORGE ? null : ModSounds.GLUTSCHMIED_AMBIENT.get();
    }

    @Override
    public int getAmbientSoundInterval() {
        return 160;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.GLUTSCHMIED_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.GLUTSCHMIED_DEATH.get();
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(ModSounds.GLUTSCHMIED_STEP.get(), 0.9f, 0.9f + this.random.nextFloat() * 0.2f);
    }

    @Override
    protected float getSoundVolume() {
        return 2.5f;
    }
}
