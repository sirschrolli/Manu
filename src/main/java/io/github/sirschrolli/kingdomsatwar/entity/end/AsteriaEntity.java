package io.github.sirschrolli.kingdomsatwar.entity.end;

import io.github.sirschrolli.kingdomsatwar.block.end.StarstoneBlockEntity;
import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
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
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Asteria, die Sternenreiterin: die letzte Reiterin des Sternenordens, der die Sternwarten im End erbaute. Sie wacht über
 * den Sternenhof; tritt jemand an den Sternstein heran, stößt sie in einer Säule aus Sternenlicht vom Himmel herab – auf
 * ihrem gepanzerten Sternenrochen, die Sternenlanze in der Hand. Sie umkreist ihre Gegner hoch in der Luft und greift
 * abwechselnd an:
 * <ul>
 *     <li><b>Tjost</b> – sie senkt die Lanze, eine Linie aus Sternfunken zeigt ihren Weg, dann reitet sie im Sturm quer über
 *     den Hof (wer in der Bahn steht, wird niedergeritten); danach wendet sie dicht über dem Boden;</li>
 *     <li><b>Sternenregen</b> – sie reckt die Lanze zum Himmel, leuchtende Ringe zeigen, wo die Sterne einschlagen;</li>
 *     <li><b>Sternbild</b> – fünf Sterne um ihr Ziel, Linien aus Licht verbinden sie zum Pentagramm; wer drinnen bleibt,
 *     wenn es zerspringt, wird verletzt und schwebt hilflos empor;</li>
 *     <li><b>Komet</b> – sie schleudert die Lanze als Kometen; der zerplatzt in Sternenlicht und kehrt in ihre Hand zurück
 *     (solange sie wartet, schwebt sie tief);</li>
 *     <li><b>Schwingenschlag</b> – der Rochen bäumt sich auf und schlägt die Schwingen: eine Druckwelle wirft alle fort und
 *     bläst Pfeile aus der Luft;</li>
 *     <li><b>Supernova</b> – unter halber Kraft steigt sie auf und sammelt das Licht der Sterne: wer sie dann sieht und
 *     nicht hinter einer Mauer, einem Pfeiler oder einem Turm Schutz sucht, wird geblendet und schwer getroffen. Danach
 *     strahlt sie: schneller, mehr Sterne.</li>
 * </ul>
 * Findet sie niemanden mehr, kehrt sie in den Himmel zurück (und kommt geheilt wieder). Besiegt sinkt sie in einer Spirale
 * hinab und vergeht in Sternenlicht; der Sternstein erlischt (ein Endkristall ruft sie zurück).
 */
public class AsteriaEntity extends Monster {
    public static final float MAX_HEALTH = 700.0f;
    public static final int AWAKEN_TICKS = 90;
    /** So lange (Ticks) dauert ihr Sterben. */
    public static final int DEATH_TICKS = 80;
    /** So hoch über dem Sternstein erscheint sie, bevor sie herabstößt. */
    public static final double DESCENT = 36.0;
    /** In diesem Tick des Kometen verlässt die Lanze ihre Hand, in diesem kehrt sie zurück. */
    public static final int COMET_THROW = 18;
    public static final int COMET_CATCH = 44;
    /** In diesem Tick der Tjost beginnt der Sturmritt, in diesem endet er. */
    public static final int TJOST_CHARGE = 24;
    public static final int TJOST_END = 48;
    /** In diesem Tick der Supernova bricht das Licht los. */
    public static final int NOVA_BLAST = 64;
    /** Weiter als so weit vom Sternstein folgt sie niemandem. */
    private static final double LEASH = 48.0;
    /** So lange ohne Gegner, dann kehrt sie in den Himmel zurück. */
    private static final int RETURN_TICKS = 600;
    private static final double NOVA_RADIUS = 20.0;

    public enum Attack {
        NONE(0), AWAKEN(AWAKEN_TICKS), TJOST(78), STARFALL(66), CONSTELLATION(66), COMET(58), WINGBEAT(42), SUPERNOVA(104), ASCEND(70);

        public final int duration;

        Attack(int duration) {
            this.duration = duration;
        }
    }

    private static final EntityDataAccessor<Integer> DATA_ATTACK = SynchedEntityData.defineId(AsteriaEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_RADIANT = SynchedEntityData.defineId(AsteriaEntity.class,
            EntityDataSerializers.BOOLEAN);
    /** Farbe der Sternbilder und Warnringe. */
    private static final ParticleOptions STAR_DUST = new DustParticleOptions(0x9AD8FF, 1.2f);
    private static final ParticleOptions STAR_DUST_BRIGHT = new DustParticleOptions(0xEAF6FF, 1.6f);

    private final ServerBossEvent bossBar = new ServerBossEvent(UUID.randomUUID(), Component.translatable("boss.kingdomsatwar.star"),
            BossEvent.BossBarColor.BLUE, BossEvent.BossBarOverlay.NOTCHED_10);
    private Attack attack = Attack.NONE;
    private Attack lastAttack = Attack.NONE;
    private int attackTick;
    private int cooldown = 30;
    private float orbit;
    private int idleTicks;
    private boolean novaDone;
    private @Nullable BlockPos home;
    // Tjost: Bahn (Anfang und Richtung) und wer schon getroffen ist
    private Vec3 lineStart = Vec3.ZERO;
    private Vec3 lineDir = new Vec3(0.0, 0.0, 1.0);
    private final Set<UUID> struck = new HashSet<>();
    // Sternenregen und Sternbild
    private final List<Vec3> marks = new ArrayList<>();
    private Vec3 constellation = Vec3.ZERO;
    private int clientAttackStart;
    /** Nur im Client: Phase des Flügelschlags und Neigung in Kurven (für die Animationen). */
    public float flap;
    public float flapO;
    public float bank;
    public float bankO;

    public AsteriaEntity(EntityType<? extends AsteriaEntity> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
        this.xpReward = 500;
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, MAX_HEALTH)
                .add(Attributes.ARMOR, 14.0)
                .add(Attributes.ARMOR_TOUGHNESS, 6.0)
                .add(Attributes.ATTACK_DAMAGE, 14.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.FLYING_SPEED, 0.7)
                .add(Attributes.FOLLOW_RANGE, 48.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ATTACK, 0);
        builder.define(DATA_RADIANT, false);
    }

    public Attack currentAttack() {
        int i = this.entityData.get(DATA_ATTACK);
        return i >= 0 && i < Attack.values().length ? Attack.values()[i] : Attack.NONE;
    }

    /** Strahlend (nach der Supernova, unter halber Kraft): schneller, mehr Sterne. */
    public boolean isRadiant() {
        return this.entityData.get(DATA_RADIANT);
    }

    /** Wie lange der laufende Angriff schon dauert (im Client, für die Animationen). */
    public float attackTime(float partialTicks) {
        return this.tickCount - this.clientAttackStart + partialTicks;
    }

    /** Hält sie die Lanze (oder fliegt die gerade als Komet)? */
    public boolean holdsLance(float attackTime) {
        return !(this.currentAttack() == Attack.COMET && attackTime >= COMET_THROW && attackTime < COMET_CATCH);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (DATA_ATTACK.equals(key)) {
            this.clientAttackStart = this.tickCount;
        }
    }

    public @Nullable BlockPos home() {
        return this.home;
    }

    /**
     * Bindet sie an ihren Sternstein (dort kreist sie, dorthin kehrt sie zurück); {@code angle}: aus welcher Richtung sie
     * herabstößt (im Bogenmaß, um den Stein herum).
     */
    public void bindTo(BlockPos stone, float angle) {
        this.home = stone.immutable();
        this.orbit = angle;
    }

    /** Sie stößt in einer Lichtsäule vom Himmel herab (unverwundbar, bis sie die Lanze hebt). */
    public void descend() {
        this.begin(Attack.AWAKEN);
    }

    /** Beginnt sofort einen bestimmten Angriff (für Tests und die Vorführung). */
    public void forceAttack(Attack next) {
        this.begin(next);
    }

    /** Wird strahlend (für Tests und die Vorführung). */
    public void forceRadiant() {
        this.entityData.set(DATA_RADIANT, true);
        this.novaDone = true;
    }

    private void begin(Attack next) {
        this.attack = next;
        this.attackTick = 0;
        this.entityData.set(DATA_ATTACK, next.ordinal());
        if (next != Attack.NONE && next != Attack.AWAKEN && next != Attack.SUPERNOVA && next != Attack.ASCEND) {
            this.lastAttack = next;
        }
        this.struck.clear();
        this.marks.clear();
    }

    // ------------------------------------------------------------------ Grundverhalten

    @Override
    public void travel(Vec3 input) {
        this.move(MoverType.SELF, this.getDeltaMovement());
        this.setDeltaMovement(this.getDeltaMovement().scale(0.9));
    }

    @Override
    protected void checkFallDamage(double ya, boolean onGround, BlockState onState, BlockPos pos) {
    }

    @Override
    public boolean causeFallDamage(double distance, float multiplier, DamageSource source) {
        return false;
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
    protected boolean canRide(Entity vehicle) {
        return false;
    }

    @Override
    public boolean isFlyingVehicle() {
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

    /** Beim Herabstoßen und Aufsteigen ist sie unverwundbar; Wände, Stürze und die Leere schaden ihr nie. */
    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return super.hurtServer(level, source, amount);
        }
        if (this.attack == Attack.AWAKEN || this.attack == Attack.ASCEND || source.is(DamageTypes.IN_WALL)
                || source.is(DamageTypes.FALL) || source.is(DamageTypes.FLY_INTO_WALL)) {
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
    public void tick() {
        // sie gleitet durch Türme und Pfeiler hindurch (sonst verfinge sich der Rochen mit seinen Schwingen)
        this.noPhysics = true;
        super.tick();
        if (this.level().isClientSide()) {
            Vec3 v = this.getDeltaMovement();
            this.flapO = this.flap;
            // ruhig: weite, langsame Schläge; schnell oder steigend: kräftiger; im Sturmritt gleitet sie
            Attack a = this.currentAttack();
            float rate = 0.07f + (float) Math.min(0.1, v.horizontalDistance() * 0.12) + (float) Math.max(0.0, v.y) * 0.3f;
            if (a == Attack.ASCEND || a == Attack.WINGBEAT) {
                rate += 0.12f;
            }
            if (this.deathTime > 0) {
                rate *= Math.max(0.0f, 1.0f - this.deathTime / (float) DEATH_TICKS);
            }
            this.flap += rate;
            this.bankO = this.bank;
            float turn = Mth.wrapDegrees(this.getYRot() - this.yRotO);
            this.bank += (Mth.clamp(turn * 3.5f, -28.0f, 28.0f) - this.bank) * 0.1f;
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide()) {
            this.clientEffects();
        }
    }

    /** Sternenstaub rieselt von den Schwingen; strahlend funkelt sie ringsum. */
    private void clientEffects() {
        Level level = this.level();
        float yaw = this.yBodyRot * Mth.DEG_TO_RAD;
        boolean radiant = this.isRadiant();
        if (this.random.nextInt(radiant ? 2 : 3) == 0) {
            double side = (this.random.nextBoolean() ? 1 : -1) * (1.0 + this.random.nextDouble() * 3.4);
            level.addParticle(ParticleTypes.END_ROD, this.getX() + Mth.cos(yaw) * side, this.getY() + 0.5,
                    this.getZ() + Mth.sin(yaw) * side, 0.0, -0.02, 0.0);
        }
        if (radiant && this.random.nextInt(3) == 0) {
            level.addParticle(ParticleTypes.FIREWORK, this.getRandomX(1.6), this.getY(0.3 + this.random.nextDouble() * 0.8),
                    this.getRandomZ(1.6), 0.0, 0.0, 0.0);
        }
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        this.bossBar.setProgress(this.getHealth() / this.getMaxHealth());
        if (this.tickCount % 40 == 0 && this.strayed(level)) {
            return;
        }
        if (this.attack == Attack.AWAKEN) {
            this.tickAwaken(level);
            return;
        }
        if (this.attack == Attack.ASCEND) {
            this.tickAscend(level);
            return;
        }
        if (!this.novaDone && this.attack != Attack.SUPERNOVA && this.getHealth() < this.getMaxHealth() * 0.5f) {
            this.begin(Attack.SUPERNOVA);
        }
        if (this.attack == Attack.SUPERNOVA) {
            this.attackTick++;
            this.tickSupernova(level);
            if (this.attackTick >= Attack.SUPERNOVA.duration) {
                this.begin(Attack.NONE);
                this.cooldown = 20;
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
        if (this.attack == Attack.NONE) {
            this.circle(target);
            this.faceMotion(target);
            if (--this.cooldown <= 0) {
                this.begin(this.chooseAttack(target));
            }
            return;
        }
        this.attackTick++;
        switch (this.attack) {
            case TJOST -> this.tickTjost(level, target);
            case STARFALL -> this.tickStarfall(level, target);
            case CONSTELLATION -> this.tickConstellation(level, target);
            case COMET -> this.tickComet(level, target);
            case WINGBEAT -> this.tickWingbeat(level, target);
            default -> { }
        }
        if (this.attackTick >= this.attack.duration) {
            this.begin(Attack.NONE);
            this.cooldown = this.isRadiant() ? 14 + this.random.nextInt(14) : 26 + this.random.nextInt(20);
        }
    }

    /**
     * Gehört sie (noch) zu ihrem Sternstein? Ist dort inzwischen eine andere Reiterin gebunden (etwa nach dem Neuladen der
     * Welt), vergeht diese hier; ist der Stein fort, bleibt sie frei.
     */
    private boolean strayed(ServerLevel level) {
        if (this.home == null || !level.isLoaded(this.home)) {
            return false;
        }
        if (level.getBlockEntity(this.home) instanceof StarstoneBlockEntity stone) {
            if (!stone.owns(this)) {
                level.sendParticles(ParticleTypes.END_ROD, this.getX(), this.getY(0.5), this.getZ(), 60, 1.2, 0.8, 1.2, 0.08);
                this.discard();
                return true;
            }
            return false;
        }
        this.home = null;
        return false;
    }

    /**
     * Wen ihre Angriffe treffen: Spieler (nicht im Kreativ- oder Zuschauermodus), ihre Begleiter und Soldaten, Tiere – aber
     * keine anderen Wesen des Ends (außer sie greifen sie an), keine Rüstungsständer.
     */
    private boolean canStrike(LivingEntity entity) {
        if (entity == this || !entity.isAlive() || entity instanceof AsteriaEntity || entity instanceof ArmorStand) {
            return false;
        }
        if (entity instanceof Player player) {
            return !player.isCreative() && !player.isSpectator();
        }
        return !(entity instanceof Enemy) || entity == this.getTarget();
    }

    private List<LivingEntity> victims(ServerLevel level, AABB area) {
        return level.getEntitiesOfClass(LivingEntity.class, area, this::canStrike);
    }

    private @Nullable LivingEntity findTarget(ServerLevel level) {
        if (this.home != null && this.distanceToSqr(Vec3.atCenterOf(this.home)) > LEASH * LEASH) {
            return null;
        }
        LivingEntity current = this.getTarget();
        if (current != null && current.isAlive() && current.distanceToSqr(this) < 56.0 * 56.0 && this.canStrike(current)
                && (this.home == null || current.distanceToSqr(Vec3.atCenterOf(this.home)) < (LEASH + 8.0) * (LEASH + 8.0))) {
            return current;
        }
        return level.getNearestPlayer(this.getX(), this.getY(), this.getZ(), 40.0,
                e -> e instanceof Player p && !p.isCreative() && !p.isSpectator() && p.isAlive());
    }

    /** Ohne Gegner: hoch über dem Sternstein kreisen, sich heilen – und nach einer halben Minute in den Himmel zurückkehren. */
    private void idle(ServerLevel level) {
        if (this.attack != Attack.NONE) {
            this.begin(Attack.NONE);
        }
        this.idleTicks++;
        Vec3 center = this.home != null ? Vec3.atCenterOf(this.home) : this.position();
        this.orbit += 0.012f;
        Vec3 want = center.add(Mth.cos(this.orbit) * 11.0, 14.0, Mth.sin(this.orbit) * 11.0);
        this.steer(want, 0.05, 0.4);
        this.faceMotion(null);
        if (this.idleTicks > 200 && this.tickCount % 20 == 0) {
            this.heal(6.0f);
        }
        if (this.home != null && this.idleTicks > RETURN_TICKS) {
            this.begin(Attack.ASCEND);
        }
    }

    /** Kreist in sieben bis elf Blöcken Abstand und fünf bis neun Blöcken Höhe um das Ziel. */
    private void circle(LivingEntity target) {
        boolean radiant = this.isRadiant();
        this.orbit += radiant ? 0.03f : 0.022f;
        double radius = 9.0 + Mth.sin(this.tickCount * 0.017f) * 2.0;
        double height = 7.0 + Mth.sin(this.tickCount * 0.043f) * 2.0;
        Vec3 want = target.position().add(Mth.cos(this.orbit) * radius, height, Mth.sin(this.orbit) * radius);
        this.steer(want, 0.06, radiant ? 0.85 : 0.7);
    }

    private void steer(Vec3 want, double accel, double max) {
        Vec3 to = want.subtract(this.position());
        Vec3 v = this.getDeltaMovement().add(to.normalize().scale(Math.min(to.length(), 4.0) * accel * 0.25));
        if (v.length() > max) {
            v = v.normalize().scale(max);
        }
        this.setDeltaMovement(this.keepAboveGround(v, 1.4));
    }

    /** Nie in den Boden: liegt ihr nächster Schritt nicht {@code clearance} Blöcke über dem Grund, steigt sie. */
    private Vec3 keepAboveGround(Vec3 v, double clearance) {
        Vec3 next = this.position().add(v);
        int ground = this.level().getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Mth.floor(next.x), Mth.floor(next.z));
        if (ground > this.level().getMinY() && next.y < ground + clearance && next.y > ground - 8.0) {
            return new Vec3(v.x, Math.max(v.y, ground + clearance - this.getY()) * 0.5 + 0.05, v.z);
        }
        return v;
    }

    /** Blickt in Flugrichtung (der Rochen dreht sich in die Kurve); steht sie still, zum Ziel. */
    private void faceMotion(@Nullable LivingEntity target) {
        Vec3 v = this.getDeltaMovement();
        float want;
        if (v.horizontalDistanceSqr() > 0.01) {
            want = (float) (Mth.atan2(v.z, v.x) * Mth.RAD_TO_DEG) - 90.0f;
        } else if (target != null) {
            want = (float) (Mth.atan2(target.getZ() - this.getZ(), target.getX() - this.getX()) * Mth.RAD_TO_DEG) - 90.0f;
        } else {
            return;
        }
        this.turnTo(want, 8.0f);
    }

    private void faceTarget(LivingEntity target, float step) {
        this.turnTo((float) (Mth.atan2(target.getZ() - this.getZ(), target.getX() - this.getX()) * Mth.RAD_TO_DEG) - 90.0f, step);
    }

    private void turnTo(float yaw, float step) {
        float next = Mth.approachDegrees(this.getYRot(), yaw, step);
        this.setYRot(next);
        this.yBodyRot = next;
        this.yHeadRot = next;
    }

    private Attack chooseAttack(LivingEntity target) {
        double distance = this.distanceTo(target);
        List<Attack> options = new ArrayList<>(List.of(Attack.TJOST, Attack.TJOST, Attack.STARFALL, Attack.CONSTELLATION));
        if (distance < 11.0) {
            options.add(Attack.WINGBEAT);
            options.add(Attack.WINGBEAT);
        } else {
            options.add(Attack.COMET);
            options.add(Attack.COMET);
        }
        if (distance < 20.0) {
            options.add(Attack.COMET);
        }
        options.removeIf(a -> a == this.lastAttack);
        return options.get(this.random.nextInt(options.size()));
    }

    private Vec3 forward() {
        float yaw = this.getYRot() * Mth.DEG_TO_RAD;
        return new Vec3(-Mth.sin(yaw), 0.0, Mth.cos(yaw));
    }

    /** Wo ihre rechte Hand die Lanze hält. */
    private Vec3 lanceHand() {
        float yaw = this.getYRot() * Mth.DEG_TO_RAD;
        return this.position().add(-Mth.cos(yaw) * 0.85, 2.0, -Mth.sin(yaw) * 0.85);
    }

    private void sound(ServerLevel level, SoundEvent sound, float volume, float pitch) {
        level.playSound(null, this.getX(), this.getY(), this.getZ(), sound, SoundSource.HOSTILE, volume, pitch);
    }

    private static void push(LivingEntity entity, Vec3 push) {
        entity.push(push.x, push.y, push.z);
        entity.needsSync = true;
    }

    /** Der Boden unter einer Stelle (oder die Stelle selbst, wenn darunter Leere ist oder der Boden weit entfernt liegt). */
    private Vec3 ground(Vec3 at) {
        int y = this.level().getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Mth.floor(at.x), Mth.floor(at.z));
        if (y <= this.level().getMinY() || y > at.y + 6.0 || y < at.y - 12.0) {
            return at;
        }
        return new Vec3(at.x, y, at.z);
    }

    // ------------------------------------------------------------------ Herabstoßen und Aufsteigen

    /** In einer Säule aus Sternenlicht stößt sie in weiter Spirale herab, hebt die Lanze und ruft ihren Namen. */
    private void tickAwaken(ServerLevel level) {
        int t = ++this.attackTick;
        Vec3 center = this.home != null ? Vec3.atBottomCenterOf(this.home) : this.position().subtract(0.0, 9.0, 0.0);
        if (t <= 72) {
            float k = Math.min(1.0f, t / 72.0f);
            float ease = 1.0f - (1.0f - k) * (1.0f - k);
            double height = Mth.lerp(ease, DESCENT, 9.0);
            double radius = Mth.lerp(ease, 12.0, 6.0);
            float angle = this.orbit + k * 4.4f;
            Vec3 want = center.add(Mth.cos(angle) * radius, height, Mth.sin(angle) * radius);
            Vec3 to = want.subtract(this.position());
            this.setDeltaMovement(to.length() > 3.0 ? to.normalize().scale(3.0) : to.scale(0.6));
            this.faceMotion(null);
        } else {
            this.setDeltaMovement(this.getDeltaMovement().scale(0.6));
            Player nearest = level.getNearestPlayer(this, 40.0);
            if (nearest != null) {
                this.faceTarget(nearest, 10.0f);
            }
        }
        if (t == 1) {
            this.sound(level, ModSounds.ASTERIA_DESCENT.get(), 4.0f, 1.0f);
        }
        if (t <= 60 && t % 2 == 0) {
            // die Lichtsäule vom Sternstein bis zu ihr hinauf
            double top = this.getY() - center.y;
            for (int i = 0; i < 6; i++) {
                double y = center.y + 1.0 + this.random.nextDouble() * Math.max(2.0, top);
                level.sendParticles(ParticleTypes.END_ROD, center.x + (this.random.nextDouble() - 0.5) * 0.9, y,
                        center.z + (this.random.nextDouble() - 0.5) * 0.9, 1, 0.0, 0.0, 0.0, 0.0);
            }
        }
        if (t == 76) {
            this.sound(level, ModSounds.ASTERIA_CRY.get(), 4.5f, 1.0f);
            level.sendParticles(ParticleTypes.END_ROD, this.getX(), this.getY(0.8), this.getZ(), 36, 1.4, 1.0, 1.4, 0.12);
            level.sendParticles(ParticleTypes.FIREWORK, this.getX(), this.getY(0.8), this.getZ(), 16, 1.0, 0.8, 1.0, 0.08);
            level.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, 0xFFB8E2FF), this.getX(), this.getY(1.2), this.getZ(), 1, 0.0, 0.0,
                    0.0, 0.0);
            Component title = Component.translatable("boss.kingdomsatwar.star").withColor(0x9AD8FF);
            Component subtitle = Component.translatable("boss.kingdomsatwar.star.title").withStyle(ChatFormatting.GRAY);
            for (ServerPlayer player : level.players()) {
                if (player.distanceToSqr(this) < 80.0 * 80.0) {
                    player.connection.send(new ClientboundSetTitlesAnimationPacket(15, 70, 25));
                    player.connection.send(new ClientboundSetTitleTextPacket(title));
                    player.connection.send(new ClientboundSetSubtitleTextPacket(subtitle));
                }
            }
        }
        if (t >= AWAKEN_TICKS) {
            this.begin(Attack.NONE);
            this.cooldown = 24;
            this.idleTicks = 0;
        }
    }

    /** Niemand mehr da: sie steigt in einer Lichtsäule in den Himmel und vergeht zwischen den Sternen. */
    private void tickAscend(ServerLevel level) {
        int t = ++this.attackTick;
        this.setDeltaMovement(0.0, Math.min(1.6, 0.15 + t * 0.03), 0.0);
        if (t == 1) {
            this.sound(level, ModSounds.ASTERIA_DESCENT.get(), 4.0f, 1.3f);
        }
        if (t % 2 == 0) {
            level.sendParticles(ParticleTypes.END_ROD, this.getX(), this.getY(0.4), this.getZ(), 8, 0.6, 1.4, 0.6, 0.02);
        }
        if (t >= Attack.ASCEND.duration) {
            if (this.home != null && level.getBlockEntity(this.home) instanceof StarstoneBlockEntity stone && stone.owns(this)) {
                stone.onRiderDeparted(level);
            }
            level.sendParticles(ParticleTypes.FIREWORK, this.getX(), this.getY(0.5), this.getZ(), 40, 1.4, 1.0, 1.4, 0.1);
            this.discard();
        }
    }

    // ------------------------------------------------------------------ Angriffe

    /**
     * Tjost: Sie fliegt an den Anfang einer Bahn quer durch ihr Ziel und senkt die Lanze; Sternfunken zeigen die Bahn. Dann
     * stürmt sie die Bahn entlang – wer darin steht, wird niedergeritten (einmal je Ritt) – und wendet danach dicht über dem
     * Boden.
     */
    private void tickTjost(ServerLevel level, LivingEntity target) {
        int t = this.attackTick;
        if (t == 1) {
            Vec3 aim = target.position().add(0.0, 1.0, 0.0);
            Vec3 flat = new Vec3(aim.x - this.getX(), 0.0, aim.z - this.getZ());
            this.lineDir = flat.lengthSqr() < 1.0 ? this.forward() : flat.normalize();
            // die Bahn verläuft dicht über dem Boden, auf dem ihr Ziel steht: der Rochen streift fast den Grund, die Lanze
            // trifft in Kopfhöhe
            Vec3 start = aim.subtract(this.lineDir.scale(16.0));
            this.lineStart = new Vec3(start.x, target.getY() + 0.5, start.z);
        }
        Vec3 end = this.lineStart.add(this.lineDir.scale(34.0));
        if (t < TJOST_CHARGE) {
            // an den Anfang der Bahn, die Lanze senken
            Vec3 to = this.lineStart.subtract(this.position());
            this.setDeltaMovement(to.length() > 1.6 ? to.normalize().scale(1.6) : to.scale(0.5));
            this.turnTo((float) (Mth.atan2(this.lineDir.z, this.lineDir.x) * Mth.RAD_TO_DEG) - 90.0f, 14.0f);
            if (t >= 6 && t % 3 == 0) {
                for (double s = 0.0; s <= 34.0; s += 0.8) {
                    Vec3 p = this.lineStart.add(this.lineDir.scale(s));
                    Vec3 g = this.ground(p);
                    level.sendParticles(STAR_DUST, p.x, Math.max(g.y + 0.15, p.y - 0.9), p.z, 1, 0.08, 0.02, 0.08, 0.0);
                }
            }
            if (t == 8) {
                this.sound(level, ModSounds.ASTERIA_CRY.get(), 3.0f, 1.25f);
            }
            return;
        }
        if (t == TJOST_CHARGE) {
            this.sound(level, ModSounds.ASTERIA_CHARGE.get(), 4.0f, 1.0f);
        }
        if (t < TJOST_END) {
            double speed = this.isRadiant() ? 1.45 : 1.25;
            Vec3 along = this.lineDir.scale(speed);
            double dy = Mth.clamp(this.lineStart.y - this.getY(), -0.4, 0.4);
            this.setDeltaMovement(this.keepAboveGround(new Vec3(along.x, dy, along.z), 0.4));
            Vec3 tip = this.position().add(this.lineDir.scale(2.6)).add(0.0, 1.6, 0.0);
            level.sendParticles(ParticleTypes.END_ROD, this.getX(), this.getY(0.5), this.getZ(), 2, 1.2, 0.4, 1.2, 0.01);
            level.sendParticles(STAR_DUST_BRIGHT, tip.x, tip.y, tip.z, 3, 0.2, 0.2, 0.2, 0.0);
            AABB hit = this.getBoundingBox().inflate(0.8, 0.6, 0.8).minmax(new AABB(tip, tip).inflate(1.6));
            for (LivingEntity victim : this.victims(level, hit)) {
                if (!this.struck.add(victim.getUUID())) {
                    continue;
                }
                if (victim.hurtServer(level, this.damageSources().mobAttack(this), this.isRadiant() ? 18.0f : 15.0f)) {
                    push(victim, this.lineDir.scale(2.0).add(0.0, 0.65, 0.0));
                    this.sound(level, ModSounds.ASTERIA_IMPACT.get(), 2.5f, 0.9f + this.random.nextFloat() * 0.2f);
                    level.sendParticles(ParticleTypes.CRIT, victim.getX(), victim.getY(0.6), victim.getZ(), 20, 0.4, 0.4, 0.4, 0.3);
                }
            }
            if (this.position().distanceToSqr(end) < 2.0) {
                this.attackTick = TJOST_END;
            }
            return;
        }
        // ausrollen und dicht über dem Boden wenden (die Gelegenheit für Nahkämpfer)
        Vec3 low = this.ground(this.position()).add(0.0, 1.6, 0.0);
        this.setDeltaMovement(this.getDeltaMovement().multiply(0.8, 0.0, 0.8).add(0.0, Mth.clamp(low.y - this.getY(), -0.2, 0.2), 0.0));
        this.faceTarget(target, 6.0f);
    }

    /** Sternenregen: Sie reckt die Lanze zum Himmel; Lichtringe zeigen, wo die Sterne einschlagen – dann fallen sie. */
    private void tickStarfall(ServerLevel level, LivingEntity target) {
        int t = this.attackTick;
        Vec3 want = target.position().add(this.forward().scale(-8.0)).add(0.0, 10.0, 0.0);
        this.steer(want, 0.05, 0.4);
        this.faceTarget(target, 8.0f);
        if (t == 6) {
            this.sound(level, ModSounds.ASTERIA_CRY.get(), 3.0f, 1.4f);
            int count = this.isRadiant() ? 10 : 6;
            this.marks.add(this.ground(target.position()));
            for (Player other : level.getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(24.0), this::canStrike)) {
                if (other != target && this.marks.size() < count) {
                    this.marks.add(this.ground(other.position()));
                }
            }
            for (int tries = 0; this.marks.size() < count && tries < 40; tries++) {
                double a = this.random.nextDouble() * Math.PI * 2.0;
                double r = 2.5 + this.random.nextDouble() * 6.0;
                Vec3 p = this.ground(target.position().add(Math.cos(a) * r, 0.0, Math.sin(a) * r));
                if (this.marks.stream().allMatch(m -> m.distanceToSqr(p) > 6.0)) {
                    this.marks.add(p);
                }
            }
        }
        if (t >= 6 && t < 46 && t % 4 == 0) {
            for (Vec3 m : this.marks) {
                ring(level, m.add(0.0, 0.12, 0.0), StarfallEntity.RADIUS, STAR_DUST, 18);
                level.sendParticles(ParticleTypes.END_ROD, m.x, m.y + 0.3, m.z, 1, 0.1, 0.1, 0.1, 0.01);
            }
        }
        if (t == 24) {
            this.sound(level, ModSounds.ASTERIA_STARFALL.get(), 3.5f, 1.0f);
        }
        for (int i = 0; i < this.marks.size(); i++) {
            if (t == 26 + i * 2) {
                Vec3 m = this.marks.get(i);
                StarfallEntity star = new StarfallEntity(level, this);
                star.setDamage(this.isRadiant() ? 12.0f : 10.0f);
                star.dropOnto(m, 20.0 + this.random.nextDouble() * 4.0);
                level.addFreshEntity(star);
            }
        }
    }

    /**
     * Sternbild: fünf Sterne um ihr Ziel, Linien aus Licht verbinden sie Zug um Zug zum Pentagramm. Wer drinnen bleibt, wenn
     * es zerspringt, wird verletzt und schwebt hilflos empor.
     */
    private void tickConstellation(ServerLevel level, LivingEntity target) {
        int t = this.attackTick;
        this.circle(target);
        this.faceTarget(target, 6.0f);
        double radius = 4.2;
        if (t == 4) {
            this.constellation = this.ground(target.position());
            double base = this.random.nextDouble() * Math.PI * 2.0;
            for (int i = 0; i < 5; i++) {
                double a = base + i * Math.PI * 2.0 / 5.0;
                this.marks.add(this.constellation.add(Math.cos(a) * radius, 1.0, Math.sin(a) * radius));
            }
            this.sound(level, ModSounds.ASTERIA_CONSTELLATION.get(), 3.0f, 1.0f);
        }
        if (this.marks.size() < 5) {
            return;
        }
        if (t >= 4 && t < 46 && t % 3 == 0) {
            for (Vec3 p : this.marks) {
                level.sendParticles(STAR_DUST_BRIGHT, p.x, p.y, p.z, 3, 0.1, 0.1, 0.1, 0.0);
                level.sendParticles(ParticleTypes.END_ROD, p.x, p.y, p.z, 1, 0.05, 0.05, 0.05, 0.0);
            }
            // die Linien des Pentagramms, eine nach der anderen (0 → 2 → 4 → 1 → 3 → 0)
            int lines = Math.min(5, (t - 4) / 6 + 1);
            for (int k = 0; k < lines; k++) {
                Vec3 a = this.marks.get((k * 2) % 5);
                Vec3 b = this.marks.get((k * 2 + 2) % 5);
                double len = a.distanceTo(b);
                for (double s = 0.0; s <= len; s += 0.45) {
                    Vec3 p = a.add(b.subtract(a).scale(s / len));
                    level.sendParticles(STAR_DUST, p.x, p.y, p.z, 1, 0.0, 0.0, 0.0, 0.0);
                }
            }
            ring(level, this.constellation.add(0.0, 0.1, 0.0), radius, STAR_DUST, 28);
        }
        if (t == 34) {
            this.sound(level, ModSounds.ASTERIA_CONSTELLATION.get(), 3.0f, 1.5f);
        }
        if (t == 46) {
            this.sound(level, ModSounds.ASTERIA_STAR_IMPACT.get(), 3.0f, 1.2f);
            for (Vec3 p : this.marks) {
                level.sendParticles(ParticleTypes.END_ROD, p.x, p.y, p.z, 24, 0.3, 0.3, 0.3, 0.18);
                level.sendParticles(ParticleTypes.FIREWORK, p.x, p.y, p.z, 10, 0.2, 0.2, 0.2, 0.12);
                level.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, 0xFFB8E2FF), p.x, p.y, p.z, 1, 0.0, 0.0, 0.0, 0.0);
            }
            Set<UUID> hit = new HashSet<>();
            for (Vec3 p : this.marks) {
                for (LivingEntity victim : this.victims(level, new AABB(p, p).inflate(1.6))) {
                    if (hit.add(victim.getUUID())) {
                        victim.hurtServer(level, this.damageSources().indirectMagic(this, this), 7.0f);
                    }
                }
            }
            Vec3 c = this.constellation;
            for (LivingEntity victim : this.victims(level, new AABB(c, c).inflate(radius + 0.4, 3.0, radius + 0.4))) {
                double dx = victim.getX() - c.x;
                double dz = victim.getZ() - c.z;
                if (dx * dx + dz * dz > (radius + 0.3) * (radius + 0.3)) {
                    continue;
                }
                victim.hurtServer(level, this.damageSources().indirectMagic(this, this), this.isRadiant() ? 10.0f : 8.0f);
                victim.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 40, 1), this);
                level.sendParticles(ParticleTypes.END_ROD, victim.getX(), victim.getY(0.5), victim.getZ(), 12, 0.3, 0.6, 0.3, 0.04);
            }
        }
    }

    /**
     * Komet: Sie holt aus und schleudert die Lanze; als Komet zerplatzt sie in Sternenlicht und kehrt in ihre Hand zurück.
     * Bis dahin schwebt sie tief und wartet.
     */
    private void tickComet(ServerLevel level, LivingEntity target) {
        int t = this.attackTick;
        this.faceTarget(target, 10.0f);
        if (t < COMET_THROW) {
            this.setDeltaMovement(this.getDeltaMovement().scale(0.75));
            if (t == 4) {
                this.sound(level, ModSounds.ASTERIA_CRY.get(), 2.5f, 1.5f);
            }
            if (t % 3 == 0) {
                Vec3 h = this.lanceHand().add(0.0, 1.0, 0.0);
                level.sendParticles(ParticleTypes.END_ROD, h.x, h.y, h.z, 3, 0.4, 0.6, 0.4, 0.02);
            }
            return;
        }
        if (t == COMET_THROW) {
            Vec3 from = this.lanceHand().add(0.0, 0.8, 0.0);
            Vec3 aim = target.getEyePosition().subtract(0.0, 0.6, 0.0);
            double flight = from.distanceTo(aim) / CometLanceEntity.SPEED;
            aim = aim.add(target.getDeltaMovement().multiply(1.0, 0.0, 1.0).scale(Math.min(flight, 20.0)));
            CometLanceEntity comet = new CometLanceEntity(level, this);
            comet.setDamage(this.isRadiant() ? 15.0f : 12.0f);
            comet.launch(from, aim);
            level.addFreshEntity(comet);
            this.sound(level, ModSounds.ASTERIA_COMET_THROW.get(), 3.5f, 1.0f);
        }
        if (t == COMET_CATCH) {
            Vec3 h = this.lanceHand().add(0.0, 0.8, 0.0);
            level.sendParticles(ParticleTypes.END_ROD, h.x, h.y, h.z, 30, 0.3, 0.8, 0.3, 0.1);
            level.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, 0xFFB8E2FF), h.x, h.y, h.z, 1, 0.0, 0.0, 0.0, 0.0);
            this.sound(level, ModSounds.ASTERIA_IMPACT.get(), 1.6f, 1.6f);
        }
        // ohne Lanze schwebt sie tief (die Gelegenheit für Nahkämpfer)
        Vec3 low = this.ground(this.position()).add(0.0, 2.2, 0.0);
        this.setDeltaMovement(this.getDeltaMovement().multiply(0.7, 0.0, 0.7).add(0.0, Mth.clamp(low.y - this.getY(), -0.25, 0.25), 0.0));
    }

    /** Schwingenschlag: Der Rochen bäumt sich auf und schlägt die Schwingen – eine Druckwelle wirft alle fort, Pfeile mit. */
    private void tickWingbeat(ServerLevel level, LivingEntity target) {
        int t = this.attackTick;
        this.setDeltaMovement(this.getDeltaMovement().scale(0.7));
        this.faceTarget(target, 10.0f);
        if (t == 6) {
            this.sound(level, ModSounds.ASTERIA_WINGBEAT.get(), 1.5f, 0.7f);
        }
        if (t != 20) {
            return;
        }
        this.sound(level, ModSounds.ASTERIA_WINGBEAT.get(), 4.0f, 1.0f);
        double reach = 9.5;
        Vec3 c = this.position().add(0.0, 0.6, 0.0);
        for (int i = 0; i < 48; i++) {
            double a = i * Math.PI * 2.0 / 48.0;
            level.sendParticles(ParticleTypes.CLOUD, c.x + Math.cos(a) * 1.5, c.y, c.z + Math.sin(a) * 1.5, 0, Math.cos(a), 0.05, Math.sin(a),
                    0.9);
            level.sendParticles(ParticleTypes.END_ROD, c.x + Math.cos(a) * 2.0, c.y + 0.3, c.z + Math.sin(a) * 2.0, 0, Math.cos(a), 0.0,
                    Math.sin(a), 0.6);
        }
        for (LivingEntity victim : this.victims(level, this.getBoundingBox().inflate(reach))) {
            Vec3 to = victim.position().subtract(c);
            double d = to.length();
            if (d > reach) {
                continue;
            }
            Vec3 away = d < 1.0e-3 ? this.forward() : new Vec3(to.x, 0.0, to.z).normalize();
            double strength = 0.8 + 2.0 * (1.0 - d / reach);
            victim.hurtServer(level, this.damageSources().mobAttack(this), 6.0f);
            push(victim, away.scale(strength).add(0.0, 0.55, 0.0));
        }
        // Pfeile und andere Geschosse in der Nähe werden fortgeblasen
        for (Projectile shot : level.getEntitiesOfClass(Projectile.class, this.getBoundingBox().inflate(reach),
                p -> !(p.getOwner() instanceof AsteriaEntity))) {
            Vec3 away = shot.position().subtract(c);
            shot.setDeltaMovement(away.normalize().scale(1.4).add(0.0, 0.3, 0.0));
            shot.needsSync = true;
        }
    }

    /**
     * Supernova: Sie steigt über den Hof, sammelt das Licht der Sterne – und es bricht los. Wer sie dann sehen kann (nicht
     * hinter einer Mauer, einem Pfeiler, einem Turm), wird geblendet und schwer getroffen. Danach strahlt sie.
     */
    private void tickSupernova(ServerLevel level) {
        int t = this.attackTick;
        Vec3 center = this.home != null ? Vec3.atCenterOf(this.home).add(0.0, 13.0, 0.0) : this.position();
        if (t < NOVA_BLAST) {
            Vec3 to = center.subtract(this.position());
            this.setDeltaMovement(to.length() > 0.8 ? to.normalize().scale(0.8) : to.scale(0.3));
            LivingEntity target = this.getTarget();
            if (target != null) {
                this.faceTarget(target, 4.0f);
            }
        } else {
            this.setDeltaMovement(this.getDeltaMovement().scale(0.5));
        }
        if (t == 2) {
            this.sound(level, ModSounds.ASTERIA_SUPERNOVA_CHARGE.get(), 5.0f, 1.0f);
            for (ServerPlayer player : level.players()) {
                if (player.distanceToSqr(this) < 48.0 * 48.0) {
                    player.sendOverlayMessage(Component.translatable("entity.kingdomsatwar.asteria.supernova").withColor(0xB8E2FF));
                }
            }
        }
        if (t < NOVA_BLAST && t % 2 == 0) {
            // das Licht strömt von allen Seiten zu ihr
            Vec3 c = this.position().add(0.0, 1.4, 0.0);
            for (int i = 0; i < 6; i++) {
                double a = this.random.nextDouble() * Math.PI * 2.0;
                double b = (this.random.nextDouble() - 0.5) * Math.PI;
                Vec3 dir = new Vec3(Math.cos(a) * Math.cos(b), Math.sin(b), Math.sin(a) * Math.cos(b));
                Vec3 p = c.add(dir.scale(9.0));
                level.sendParticles(ParticleTypes.END_ROD, p.x, p.y, p.z, 0, -dir.x, -dir.y, -dir.z, 0.45);
            }
        }
        if (t != NOVA_BLAST) {
            return;
        }
        Vec3 c = this.position().add(0.0, 1.4, 0.0);
        this.sound(level, ModSounds.ASTERIA_SUPERNOVA.get(), 6.0f, 1.0f);
        level.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, 0xFFFFFFFF), c.x, c.y, c.z, 3, 0.5, 0.5, 0.5, 0.0);
        level.sendParticles(ParticleTypes.END_ROD, c.x, c.y, c.z, 200, 0.5, 0.5, 0.5, 0.9);
        level.sendParticles(ParticleTypes.FIREWORK, c.x, c.y, c.z, 80, 0.5, 0.5, 0.5, 0.6);
        for (LivingEntity victim : this.victims(level, this.getBoundingBox().inflate(NOVA_RADIUS))) {
            Vec3 eye = victim.getEyePosition();
            if (eye.distanceTo(c) > NOVA_RADIUS) {
                continue;
            }
            // wer hinter einem Block steht, ist geschützt
            HitResult block = level.clip(new ClipContext(c, eye, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
            if (block.getType() != HitResult.Type.MISS) {
                continue;
            }
            victim.hurtServer(level, this.damageSources().indirectMagic(this, this), 16.0f);
            victim.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0), this);
            push(victim, new Vec3(eye.x - c.x, 0.0, eye.z - c.z).normalize().scale(1.2).add(0.0, 0.4, 0.0));
        }
        this.novaDone = true;
        this.entityData.set(DATA_RADIANT, true);
    }

    /** Ein Ring aus Teilchen am Boden. */
    static void ring(ServerLevel level, Vec3 c, double radius, ParticleOptions particle, int points) {
        for (int i = 0; i < points; i++) {
            double a = i * Math.PI * 2.0 / points;
            level.sendParticles(particle, c.x + Math.cos(a) * radius, c.y, c.z + Math.sin(a) * radius, 1, 0.0, 0.0, 0.0, 0.0);
        }
    }

    // ------------------------------------------------------------------ Tod

    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (this.level() instanceof ServerLevel level) {
            level.sendParticles(ParticleTypes.END_ROD, this.getX(), this.getY(0.6), this.getZ(), 80, 1.6, 1.0, 1.6, 0.08);
            if (this.home != null && level.getBlockEntity(this.home) instanceof StarstoneBlockEntity stone && stone.owns(this)) {
                stone.onRiderDefeated(level);
            }
        }
    }

    /** Sie sinkt in einer langsamen Spirale hinab; Sternenlicht löst sich von ihr, bis sie ganz darin vergeht. */
    @Override
    protected void tickDeath() {
        this.deathTime++;
        float yaw = (this.getYRot() + 4.0f) * Mth.DEG_TO_RAD;
        this.setYRot(this.getYRot() + 4.0f);
        this.yBodyRot = this.getYRot();
        Vec3 glide = new Vec3(-Mth.sin(yaw) * 0.22, -0.07, Mth.cos(yaw) * 0.22);
        Vec3 next = this.position().add(glide);
        int ground = this.level().getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Mth.floor(next.x), Mth.floor(next.z));
        if (ground > this.level().getMinY() && next.y < ground + 0.6) {
            glide = new Vec3(glide.x * 0.3, 0.0, glide.z * 0.3);
        }
        this.setDeltaMovement(glide);
        if (!(this.level() instanceof ServerLevel level)) {
            return;
        }
        if (this.deathTime % 3 == 0) {
            level.sendParticles(ParticleTypes.END_ROD, this.getX(), this.getY(0.5), this.getZ(), 8, 2.0, 0.6, 2.0, 0.03);
            level.sendParticles(STAR_DUST, this.getX(), this.getY(0.6), this.getZ(), 6, 1.6, 0.8, 1.6, 0.0);
        }
        if (this.deathTime == DEATH_TICKS / 2) {
            this.sound(level, ModSounds.ASTERIA_CONSTELLATION.get(), 3.0f, 0.6f);
        }
        if (this.deathTime == DEATH_TICKS - 2) {
            this.sound(level, ModSounds.ASTERIA_SUPERNOVA.get(), 3.0f, 1.6f);
            level.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, 0xFFB8E2FF), this.getX(), this.getY(0.6), this.getZ(), 1, 0.0, 0.0,
                    0.0, 0.0);
            level.sendParticles(ParticleTypes.END_ROD, this.getX(), this.getY(0.6), this.getZ(), 160, 1.2, 0.8, 1.2, 0.3);
            level.sendParticles(ParticleTypes.FIREWORK, this.getX(), this.getY(0.6), this.getZ(), 60, 1.0, 0.6, 1.0, 0.2);
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
        output.putBoolean("Radiant", this.isRadiant());
        output.putBoolean("Nova", this.novaDone);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        input.getLong("Home").ifPresent(h -> this.home = BlockPos.of(h));
        this.entityData.set(DATA_RADIANT, input.getBooleanOr("Radiant", false));
        this.novaDone = input.getBooleanOr("Nova", false);
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return ModSounds.ASTERIA_AMBIENT.get();
    }

    @Override
    public int getAmbientSoundInterval() {
        return 200;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.ASTERIA_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.ASTERIA_DEATH.get();
    }

    @Override
    protected float getSoundVolume() {
        return 3.0f;
    }

    /** Aus der Leere gefallen (etwa am Rand der Insel)? Dann zurück über den Hof. */
    @Override
    protected void onBelowWorld() {
        if (this.home != null) {
            this.teleportTo(this.home.getX() + 0.5, this.home.getY() + 14.0, this.home.getZ() + 0.5);
            this.setDeltaMovement(Vec3.ZERO);
        } else {
            super.onBelowWorld();
        }
    }
}
