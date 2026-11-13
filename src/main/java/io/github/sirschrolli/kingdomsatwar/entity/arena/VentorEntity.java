package io.github.sirschrolli.kingdomsatwar.entity.arena;

import io.github.sirschrolli.kingdomsatwar.block.arena.ArenaSealBlockEntity;
import io.github.sirschrolli.kingdomsatwar.block.arena.GladiatorNetBlock;
import io.github.sirschrolli.kingdomsatwar.entity.boss.sky.SkyTheme;
import io.github.sirschrolli.kingdomsatwar.entity.boss.sky.SkyWarden;
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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileDeflection;
import net.minecraft.world.entity.projectile.hurtingprojectile.windcharge.AbstractWindCharge;
import net.minecraft.world.entity.projectile.hurtingprojectile.windcharge.WindCharge;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Ventor, der Gladiator der Winde (seit 1.9): der letzte Meister der Arena der Prüfungen – ein Standbild aus Bronze, das der
 * Wind füllt; statt der Beine trägt ihn ein Wirbelwind. Er schläft im Spawner der Böen auf dem Podest der Arena, bis alle
 * Käfige bezwungen sind ({@link ArenaSealBlockEntity}); dann steigt er auf, sobald jemand die Arena betritt, und grüßt die
 * Ränge mit erhobenem Dreizack.
 *
 * <p>Er kämpft wie ein Netzkämpfer – mit dem <b>Sturmdreizack</b> und dem <b>Wurfnetz</b>:
 * <ul>
 *     <li><b>Böe</b> – die Linke fegt nach außen: wer vor ihm steht, fliegt davon (prallt er an eine Wand, trifft es ihn
 *     doppelt). Solange sie weht, prallen Geschosse an ihm ab.</li>
 *     <li><b>Dreizackstoß</b> – zurücknehmen, dann ein Ausfall nach vorn; wer im Netz steckt, trifft er härter.</li>
 *     <li><b>Netzwurf</b> – über dem Kopf geschwungen, dann geworfen: Wen es trifft, der steckt eine Weile fest (gleich darauf
 *     folgt der Stoß). Ein Netz, das danebengeht, bleibt als Falle liegen.</li>
 *     <li><b>Dreizackwurf</b> – der Dreizack fliegt, entfesselt beim Aufschlag eine Böe und kehrt in seine Hand zurück
 *     (solange er fort ist, kämpft er nur mit Wind und Netz).</li>
 *     <li><b>Windkugeln</b> – aus den Zinken drei Windkugeln (im entfesselten Sturm je drei im Fächer).</li>
 *     <li><b>Windhosen</b> – die Arme hoch: drei Windhosen ziehen durch die Arena, reißen hoch, wen sie erfassen, und
 *     schleudern ihn fort.</li>
 *     <li><b>Sturzflug</b> – er steigt hoch über die Arena, zeichnet einen Kreis um sein Ziel und stürzt mit den Zinken voran
 *     hinein.</li>
 * </ul>
 * Unter halber Kraft <b>entfesselt er den Sturm</b>: Seine Pausen werden kürzer, alle acht Sekunden fegt eine Sturmböe quer
 * durch die Arena (man sieht sie kommen), und er dreht sich im <b>Zyklon</b> – mit ausgebreiteten Armen, alles ringsum zieht
 * es zu ihm, Geschosse prallen ab. Findet er niemanden mehr, sinkt er in den Spawner der Böen zurück.
 */
public class VentorEntity extends Monster implements SkyWarden {
    public static final float MAX_HEALTH = 740.0f;
    public static final int AWAKEN_TICKS = 80;
    /** So lange formt er sich beim Aufsteigen (das Modell wächst aus dem Wirbel). */
    public static final int FORM_TICKS = 30;
    public static final int RETREAT_TICKS = 40;
    public static final int RAGE_TICKS = 40;
    /** So lange (Ticks) dauert sein Sterben (der Wind erlahmt, das Standbild stürzt). */
    public static final int DEATH_TICKS = 70;
    /** Weiter als so weit (waagerecht) von der Mitte der Arena folgt er niemandem … */
    public static final double ARENA = 22.0;
    /** … und wer ihn weiter hinauslockt, dem entkommt er in die Mitte zurück. */
    public static final double LEASH = 25.0;
    /** So lange ohne Gegner, dann sinkt er in den Spawner der Böen zurück. */
    public static final int RETURN_TICKS = 600;
    /** Der Wind (Türkis) und die Bronze seines Standbilds. */
    public static final int WIND = 0x62DBE8;
    public static final int BRONZE = 0xD7B25E;

    // ---- die Böe
    public static final float GUST_DAMAGE = 6.0f;
    public static final float SLAM_DAMAGE = 6.0f;
    public static final int GUST_HIT = 14;
    public static final double GUST_RANGE = 9.0;
    /** Halber Öffnungswinkel des Fächers (Grad). */
    public static final double GUST_ARC = 42.0;
    // ---- der Dreizackstoß
    public static final float THRUST_DAMAGE = 12.0f;
    public static final int THRUST_LUNGE = 11;
    public static final int THRUST_HIT = 12;
    public static final double THRUST_RANGE = 4.8;
    /** So viel härter trifft er, wer im Netz steckt. */
    public static final float ENTANGLED_BONUS = 1.5f;
    // ---- das Netz und der Dreizackwurf
    public static final int NET_RELEASE = 21;
    /** So lange nach dem Wurf, dann hat er ein neues Netz in der Hand. */
    public static final int NET_REGAIN = 80;
    public static final int THROW_RELEASE = 18;
    public static final float THROW_DAMAGE = 11.0f;
    /** So lange darf der Dreizack fort sein, dann ruft ihn der Wind zurück (falls er unterwegs verloren ging). */
    public static final int TRIDENT_LOST = 140;
    // ---- Windkugeln und Windhosen
    public static final int[] CHARGE_SHOTS = {14, 20, 26};
    public static final int VORTEX_SPAWN = 22;
    public static final int VORTEX_MAX = 4;
    // ---- der Sturzflug
    public static final int DIVE_RISE = 6;
    public static final int DIVE_HOVER = 24;
    public static final int DIVE_MARK = 28;
    public static final int DIVE_LOCK = 40;
    public static final int DIVE_FALL = 44;
    public static final double DIVE_HEIGHT = 7.0;
    public static final double DIVE_RADIUS = 3.6;
    public static final float DIVE_DAMAGE = 14.0f;
    // ---- der Zyklon (nur im entfesselten Sturm)
    public static final int CYCLONE_FROM = 10;
    public static final int CYCLONE_TO = 62;
    public static final double CYCLONE_RADIUS = 2.8;
    public static final double CYCLONE_PULL = 6.5;
    public static final float CYCLONE_DAMAGE = 6.0f;
    // ---- die Sturmböen (im entfesselten Sturm)
    public static final int STORM_PERIOD = 160;
    public static final int STORM_WARN = 30;

    public enum Attack {
        NONE(0), AWAKEN(AWAKEN_TICKS), RAGE(RAGE_TICKS), RETREAT(RETREAT_TICKS), GUST(30), THRUST(26), NET(36), THROW(34),
        CHARGES(40), VORTEX(50), DIVE(72), CYCLONE(70);

        public final int duration;

        Attack(int duration) {
            this.duration = duration;
        }
    }

    private static final EntityDataAccessor<Integer> DATA_ATTACK = SynchedEntityData.defineId(VentorEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_TRIDENT = SynchedEntityData.defineId(VentorEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_NET = SynchedEntityData.defineId(VentorEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_ENRAGED = SynchedEntityData.defineId(VentorEntity.class, EntityDataSerializers.BOOLEAN);

    private final ServerBossEvent bossBar = new ServerBossEvent(UUID.randomUUID(), Component.translatable("boss.kingdomsatwar.ventor"),
            BossEvent.BossBarColor.BLUE, BossEvent.BossBarOverlay.NOTCHED_10);
    private Attack attack = Attack.NONE;
    private Attack lastAttack = Attack.NONE;
    private int attackTick;
    private int cooldown = 30;
    private int idleTicks;
    /** Für Tests: nur wer so nah ist, wird getroffen (siehe {@link #confine}). */
    private double confine = Double.MAX_VALUE;
    /** Wo er aufstieg (oben auf dem Spawner der Böen) und das Siegel, das ihn rief. */
    private @Nullable BlockPos home;
    private @Nullable BlockPos seal;
    private final Set<UUID> struck = new HashSet<>();
    /** Wer von der Böe fortgeschleudert wurde (und wann): Prallt er an eine Wand, trifft es ihn noch einmal. */
    private final Map<UUID, Integer> flung = new HashMap<>();
    /** Die Windhosen, die er gerufen hat. */
    private final List<UUID> vortices = new ArrayList<>();
    /** Der Sturzflug: wo er aufstieg und wohin er stürzt. */
    private Vec3 diveStart = Vec3.ZERO;
    private Vec3 diveMark = Vec3.ZERO;
    private boolean landed;
    /** Wann er Dreizack und Netz zuletzt aus der Hand gab. */
    private int thrownAt;
    private int netAt;
    /** Nach einem Netztreffer: gleich der Stoß (oder der Wurf). */
    private boolean followUp;
    /** Die Sturmböen: Zähler und Richtung der nächsten. */
    private int stormTick;
    private Vec3 stormDir = Vec3.ZERO;
    private int clientAttackStart;

    public VentorEntity(EntityType<? extends VentorEntity> type, Level level) {
        super(type, level);
        this.xpReward = 450;
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, MAX_HEALTH)
                .add(Attributes.ARMOR, 10.0)
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
        builder.define(DATA_TRIDENT, true);
        builder.define(DATA_NET, true);
        builder.define(DATA_ENRAGED, false);
    }

    public Attack currentAttack() {
        int i = this.entityData.get(DATA_ATTACK);
        return i >= 0 && i < Attack.values().length ? Attack.values()[i] : Attack.NONE;
    }

    public int attackOrdinal() {
        return this.entityData.get(DATA_ATTACK);
    }

    public float attackTime(float partialTicks) {
        return this.tickCount - this.clientAttackStart + partialTicks;
    }

    /** Hat er den Sturmdreizack in der Hand (oder fliegt der gerade)? */
    public boolean hasTrident() {
        return this.entityData.get(DATA_TRIDENT);
    }

    /** Hat er ein Netz in der Hand? */
    public boolean hasNet() {
        return this.entityData.get(DATA_NET);
    }

    /** Hat er den Sturm entfesselt (unter halber Kraft)? */
    public boolean isEnraged() {
        return this.entityData.get(DATA_ENRAGED);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (DATA_ATTACK.equals(key)) {
            this.clientAttackStart = this.tickCount;
        }
    }

    /** Bindet ihn an die Arena: {@code home} oben auf dem Spawner der Böen, {@code seal} das Siegel, das ihn rief. */
    public void bindTo(BlockPos home, @Nullable BlockPos seal) {
        this.home = home.immutable();
        this.seal = seal == null ? null : seal.immutable();
    }

    /** Er steigt aus dem Wirbel auf (formt sich, breitet die Arme aus, grüßt die Ränge). */
    public void rise() {
        this.begin(Attack.AWAKEN);
    }

    public @Nullable BlockPos home() {
        return this.home;
    }

    public @Nullable BlockPos seal() {
        return this.seal;
    }

    /** Beginnt sofort einen bestimmten Angriff (für Tests und die Vorführung; {@code NONE}: er hält zwei Sekunden inne). */
    public void forceAttack(Attack next) {
        if (next == Attack.NONE) {
            this.cooldown = 40;
        }
        if (next == Attack.CYCLONE || next == Attack.RAGE) {
            this.setEnraged(true);
        }
        this.begin(next);
    }

    /** Für Tests und die Vorführung: den Sturm sofort entfesseln (ohne die Pause dazu). */
    public void setEnraged(boolean enraged) {
        this.entityData.set(DATA_ENRAGED, enraged);
    }

    /** Für Tests: Seine Angriffe treffen nur, wer so nah (waagerecht) bei ihm ist – sonst träfen sie die Nachbartests. */
    public void confine(double radius) {
        this.confine = radius;
    }

    /** Die Windhosen, die gerade durch die Arena ziehen. */
    public int vorticesAlive(ServerLevel level) {
        this.vortices.removeIf(id -> !(level.getEntity(id) instanceof WindVortexEntity v) || !v.isAlive());
        return this.vortices.size();
    }

    /** Wohin er im Sturzflug stürzt (für Tests). */
    public Vec3 diveMark() {
        return this.diveMark;
    }

    private void begin(Attack next) {
        this.attack = next;
        this.attackTick = 0;
        this.entityData.set(DATA_ATTACK, next.ordinal());
        if (next != Attack.NONE && next != Attack.AWAKEN && next != Attack.RAGE && next != Attack.RETREAT) {
            this.lastAttack = next;
        }
        this.struck.clear();
        this.landed = false;
        if (next != Attack.NONE) {
            this.getNavigation().stop();
        }
        if (next != Attack.DIVE && this.isNoGravity()) {
            this.setNoGravity(false);
        }
        if (next == Attack.RAGE) {
            this.setEnraged(true);
        }
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

    /** Er schwebt auf seinem Wirbel: keine Schritte. */
    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
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

    /** Solange die Böe weht oder er sich im Zyklon dreht, prallen Geschosse an ihm ab (Windkugeln nicht). */
    @Override
    public ProjectileDeflection deflection(Projectile projectile) {
        if (projectile instanceof AbstractWindCharge || projectile instanceof StormTridentEntity) {
            return ProjectileDeflection.NONE;
        }
        Attack a = this.currentAttack();
        boolean wind = a == Attack.GUST && this.attackTick >= 8 && this.attackTick <= 22
                || a == Attack.CYCLONE && this.attackTick >= CYCLONE_FROM && this.attackTick <= CYCLONE_TO;
        if (!wind) {
            return ProjectileDeflection.NONE;
        }
        return (p, entity, random, power) -> {
            if (entity != null) {
                entity.level().playSound(null, entity, SoundEvents.BREEZE_DEFLECT, entity.getSoundSource(), 1.4f, 0.8f);
            }
            ProjectileDeflection.REVERSE.deflect(p, entity, random, power);
        };
    }

    /**
     * Schaden: Beim Aufsteigen, beim Zurücksinken und solange er den Sturm entfesselt, ist er unverwundbar; Stürze, Wände und
     * Windkugeln schaden ihm nicht.
     */
    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return super.hurtServer(level, source, amount);
        }
        if (this.attack == Attack.AWAKEN || this.attack == Attack.RETREAT || this.attack == Attack.RAGE && this.attackTick < 24
                || source.is(DamageTypes.IN_WALL) || source.is(DamageTypes.FALL) || source.is(DamageTypes.FLY_INTO_WALL)
                || source.getDirectEntity() instanceof AbstractWindCharge) {
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
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        this.bossBar.setProgress(this.getHealth() / this.getMaxHealth());
        this.tickFlung(level);
        this.tickGear(level);
        if (this.attack == Attack.AWAKEN) {
            this.tickAwaken(level);
            return;
        }
        if (this.attack == Attack.RETREAT) {
            this.tickRetreat(level);
            return;
        }
        if (this.attack == Attack.RAGE) {
            this.tickRage(level);
            return;
        }
        if (this.tickCount % 10 == 0 && this.attack != Attack.DIVE) {
            this.keepInArena(level);
        }
        if (!this.isEnraged() && this.attack == Attack.NONE && this.getHealth() < this.getMaxHealth() * 0.5f) {
            this.begin(Attack.RAGE);
            return;
        }
        LivingEntity target = this.findTarget(level);
        this.setTarget(target);
        if (target != null && this.isEnraged()) {
            this.tickStorm(level);
        }
        if (this.attack == Attack.NONE) {
            if (target == null) {
                this.idle(level);
                return;
            }
            this.idleTicks = 0;
            this.getLookControl().setLookAt(target, 30.0f, 30.0f);
            this.approach(target);
            if (--this.cooldown <= 0) {
                this.begin(this.chooseAttack(level, target));
            }
            return;
        }
        this.idleTicks = 0;
        this.attackTick++;
        switch (this.attack) {
            case GUST -> this.tickGust(level, target);
            case THRUST -> this.tickThrust(level, target);
            case NET -> this.tickNet(level, target);
            case THROW -> this.tickThrow(level, target);
            case CHARGES -> this.tickCharges(level, target);
            case VORTEX -> this.tickVortex(level, target);
            case DIVE -> this.tickDive(level, target);
            case CYCLONE -> this.tickCyclone(level, target);
            default -> { }
        }
        if (this.attack != Attack.NONE && this.attackTick >= this.attack.duration) {
            this.finishAttack();
        }
    }

    private void finishAttack() {
        Attack done = this.attack;
        this.begin(Attack.NONE);
        float pace = this.isEnraged() ? 0.65f : 1.0f;
        this.cooldown = Math.round((16 + this.random.nextInt(16)) * pace);
        if (this.followUp) {
            this.followUp = false;
            this.cooldown = 3;
        }
        if (done == Attack.DIVE) {
            this.cooldown += 8;
        }
    }

    /** Wen seine Angriffe treffen: alle Lebewesen außer ihm, seinen Windhosen und Spielern im Kreativ-/Zuschauermodus. */
    public boolean canStrike(LivingEntity entity) {
        return entity != this && entity.isAlive() && !(entity instanceof VentorEntity) && !(entity instanceof ArmorStand)
                && !(entity instanceof Player player && (player.isCreative() || player.isSpectator()));
    }

    private List<LivingEntity> victims(ServerLevel level, AABB area) {
        return level.getEntitiesOfClass(LivingEntity.class, area, e -> this.canStrike(e)
                && e.distanceToSqr(this.getX(), e.getY(), this.getZ()) <= this.confine * this.confine);
    }

    /** Die Mitte der Arena (sonst wo er steht). */
    public Vec3 center() {
        if (this.home != null) {
            // er stieg oben auf dem Spawner auf; die Arena liegt drei Blöcke tiefer
            return new Vec3(this.home.getX() + 0.5, this.home.getY() - 3.0, this.home.getZ() + 0.5);
        }
        return this.position();
    }

    private boolean inArena(Entity entity) {
        if (this.home == null) {
            return true;
        }
        Vec3 c = this.center();
        double dx = entity.getX() - c.x;
        double dz = entity.getZ() - c.z;
        return dx * dx + dz * dz <= (ARENA + 3.0) * (ARENA + 3.0) && entity.getY() > c.y - 5.0 && entity.getY() < c.y + 16.0;
    }

    private @Nullable LivingEntity findTarget(ServerLevel level) {
        LivingEntity current = this.getTarget();
        if (current != null && current.isAlive() && current.distanceToSqr(this) < 40.0 * 40.0 && this.canStrike(current) && this.inArena(current)) {
            return current;
        }
        return level.getNearestPlayer(this.getX(), this.getY(), this.getZ(), Math.min(32.0, this.confine),
                e -> e instanceof Player p && !p.isCreative() && !p.isSpectator() && p.isAlive() && this.inArena(p));
    }

    public Vec3 forward() {
        float yaw = this.getYRot() * Mth.DEG_TO_RAD;
        return new Vec3(-Mth.sin(yaw), 0.0, Mth.cos(yaw));
    }

    /** Nach rechts (seine Rechte, mit dem Dreizack). */
    private Vec3 right() {
        Vec3 f = this.forward();
        return new Vec3(-f.z, 0.0, f.x);
    }

    private Vec3 toward(Entity target) {
        return this.toward(target.position());
    }

    private Vec3 toward(Vec3 to) {
        Vec3 dir = to.subtract(this.position()).multiply(1.0, 0.0, 1.0);
        return dir.lengthSqr() < 1.0e-4 ? this.forward() : dir.normalize();
    }

    private void face(Vec3 dir) {
        float yaw = (float) (Mth.atan2(dir.z, dir.x) * Mth.RAD_TO_DEG) - 90.0f;
        this.setYRot(yaw);
        this.yBodyRot = yaw;
        this.yHeadRot = yaw;
    }

    /** Wo seine Rechte (mit dem Dreizack) und seine Linke (mit dem Netz) gerade ungefähr sind. */
    private Vec3 hand(boolean right, double height) {
        Vec3 side = this.right().scale(right ? 0.75 : -0.75);
        return this.position().add(this.forward().scale(0.6)).add(side).add(0.0, height, 0.0);
    }

    private void sound(ServerLevel level, SoundEvent sound, float volume, float pitch) {
        level.playSound(null, this.getX(), this.getY(), this.getZ(), sound, SoundSource.HOSTILE, volume, pitch);
    }

    /** Er hält gern ein paar Schritte Abstand (sein Dreizack ist lang). */
    private void approach(LivingEntity target) {
        double distance = this.distanceTo(target);
        if (distance > 3.6) {
            this.getNavigation().moveTo(target, 1.0);
        } else {
            this.getNavigation().stop();
        }
    }

    /** Aus der Arena gelockt: Ein Windstoß trägt ihn zurück in die Mitte. */
    private void keepInArena(ServerLevel level) {
        if (this.home == null) {
            return;
        }
        Vec3 c = this.center();
        double dx = this.getX() - c.x;
        double dz = this.getZ() - c.z;
        if (dx * dx + dz * dz < LEASH * LEASH && this.getY() > c.y - 6.0 && this.getY() < c.y + 18.0) {
            return;
        }
        this.vanishTo(level, c.add(0.0, 1.0, 3.5));
    }

    private void vanishTo(ServerLevel level, Vec3 to) {
        level.sendParticles(ParticleTypes.GUST_EMITTER_SMALL, this.getX(), this.getY(0.4), this.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
        level.sendParticles(ParticleTypes.CLOUD, this.getX(), this.getY(0.4), this.getZ(), 30, 0.8, 1.2, 0.8, 0.03);
        this.teleportTo(to.x, to.y, to.z);
        this.setDeltaMovement(Vec3.ZERO);
        this.getNavigation().stop();
        level.sendParticles(ParticleTypes.CLOUD, to.x, to.y + 1.0, to.z, 30, 0.8, 1.2, 0.8, 0.03);
        this.sound(level, ModSounds.VENTOR_GATHER.get(), 2.0f, 1.2f);
    }

    /** Ohne Gegner: zurück auf das Podest; nach einer Weile sinkt er in den Spawner der Böen zurück. */
    private void idle(ServerLevel level) {
        this.idleTicks++;
        if (this.home != null) {
            Vec3 h = Vec3.atBottomCenterOf(this.home);
            double d = this.distanceToSqr(h);
            if (d > 2.0 && this.idleTicks % 20 == 1) {
                this.getNavigation().moveTo(h.x, h.y, h.z, 0.9);
            }
            if (d > 2.0 && this.idleTicks > 200 && this.idleTicks % 100 == 0) {
                this.vanishTo(level, h);
            }
            if (this.idleTicks > RETURN_TICKS) {
                this.begin(Attack.RETREAT);
            }
        }
    }

    private Attack chooseAttack(ServerLevel level, LivingEntity target) {
        double distance = this.distanceTo(target);
        boolean trident = this.hasTrident();
        boolean net = this.hasNet();
        boolean storm = this.isEnraged();
        if (GladiatorNetBlock.entangled(target) && trident) {
            return distance < 6.0 ? Attack.THRUST : Attack.THROW;
        }
        List<Attack> options = new ArrayList<>();
        boolean vortex = this.vorticesAlive(level) < (storm ? 2 : 1);
        if (distance < 4.6) {
            if (trident) {
                options.addAll(List.of(Attack.THRUST, Attack.THRUST));
            }
            options.addAll(List.of(Attack.GUST, Attack.GUST));
            if (net) {
                options.add(Attack.NET);
            }
            if (storm) {
                options.addAll(List.of(Attack.CYCLONE, Attack.CYCLONE));
            }
        } else if (distance < 11.0) {
            options.addAll(List.of(Attack.GUST, Attack.CHARGES));
            if (net) {
                options.addAll(List.of(Attack.NET, Attack.NET));
            }
            if (trident) {
                options.addAll(List.of(Attack.THROW, Attack.DIVE));
            }
            if (vortex) {
                options.add(Attack.VORTEX);
            }
            if (storm) {
                options.add(Attack.CYCLONE);
            }
        } else {
            options.addAll(List.of(Attack.CHARGES, Attack.CHARGES));
            if (trident) {
                options.addAll(List.of(Attack.THROW, Attack.THROW, Attack.DIVE));
            }
            if (vortex) {
                options.add(Attack.VORTEX);
            }
            if (net) {
                options.add(Attack.NET);
            }
        }
        options.removeIf(a -> a == this.lastAttack);
        if (options.isEmpty()) {
            options.add(Attack.GUST);
        }
        return options.get(this.random.nextInt(options.size()));
    }

    // ------------------------------------------------------------------ Aufsteigen, Zurücksinken, der entfesselte Sturm

    /** Er formt sich im Wirbel über dem Spawner, breitet die Arme aus und grüßt die Ränge mit erhobenem Dreizack. */
    private void tickAwaken(ServerLevel level) {
        int t = ++this.attackTick;
        this.setDeltaMovement(0.0, this.getDeltaMovement().y, 0.0);
        Player nearest = level.getNearestPlayer(this, 40.0);
        if (nearest != null && t > FORM_TICKS) {
            this.getLookControl().setLookAt(nearest, 12.0f, 30.0f);
            this.face(this.toward(nearest));
        }
        if (t == 1) {
            this.sound(level, ModSounds.VENTOR_AWAKEN.get(), 3.0f, 1.0f);
        }
        if (t < FORM_TICKS && t % 2 == 0) {
            // der Wirbel, in dem er sich formt
            double h = t / (double) FORM_TICKS;
            for (int k = 0; k < 3; k++) {
                double a = t * 0.5 + k * Math.PI * 2.0 / 3.0;
                double r = 1.4 - h * 0.6;
                level.sendParticles(ParticleTypes.CLOUD, this.getX() + Math.cos(a) * r, this.getY() + 0.3 + h * 2.8, this.getZ() + Math.sin(a) * r,
                        1, 0.05, 0.05, 0.05, 0.01);
            }
            level.sendParticles(ParticleTypes.SMALL_GUST, this.getX(), this.getY() + 1.2, this.getZ(), 2, 0.6, 1.0, 0.6, 0.0);
        }
        if (t == FORM_TICKS) {
            level.sendParticles(ParticleTypes.GUST_EMITTER_LARGE, this.getX(), this.getY() + 1.6, this.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
        }
        if (t == 46) {
            this.sound(level, ModSounds.VENTOR_SHOUT.get(), 3.0f, 1.0f);
        }
        if (t == 52) {
            Component title = Component.translatable("boss.kingdomsatwar.ventor").withColor(WIND);
            Component subtitle = Component.translatable("boss.kingdomsatwar.ventor.title").withStyle(ChatFormatting.GRAY);
            for (ServerPlayer player : level.players()) {
                if (player.distanceToSqr(this) < 64.0 * 64.0) {
                    player.connection.send(new ClientboundSetTitlesAnimationPacket(15, 60, 25));
                    player.connection.send(new ClientboundSetTitleTextPacket(title));
                    player.connection.send(new ClientboundSetSubtitleTextPacket(subtitle));
                }
            }
        }
        if (t == 62) {
            this.sound(level, ModSounds.VENTOR_CROWD.get(), 3.5f, 1.0f);
        }
        if (t >= AWAKEN_TICKS) {
            this.begin(Attack.NONE);
            this.cooldown = 10;
            this.idleTicks = 0;
        }
    }

    /** Er findet niemanden mehr: zurück auf den Spawner, der Wind trägt ihn hinab. */
    private void tickRetreat(ServerLevel level) {
        int t = ++this.attackTick;
        this.setDeltaMovement(0.0, this.getDeltaMovement().y, 0.0);
        if (t == 1) {
            if (this.home != null && this.distanceToSqr(Vec3.atBottomCenterOf(this.home)) > 2.0) {
                this.vanishTo(level, Vec3.atBottomCenterOf(this.home));
            }
            this.sound(level, ModSounds.VENTOR_AWAKEN.get(), 2.0f, 0.8f);
        }
        if (t % 2 == 0) {
            level.sendParticles(ParticleTypes.CLOUD, this.getX(), this.getY() + 1.0, this.getZ(), 4, 0.5, 1.0, 0.5, 0.02);
        }
        if (t >= RETREAT_TICKS) {
            if (this.seal != null && level.getBlockEntity(this.seal) instanceof ArenaSealBlockEntity sealEntity) {
                sealEntity.onVentorRetreated(level);
            }
            this.bossBar.removeAllPlayers();
            this.discard();
        }
    }

    /** Unter halber Kraft: Er reckt die Arme, die Ränge toben – der Sturm ist entfesselt. */
    private void tickRage(ServerLevel level) {
        int t = ++this.attackTick;
        this.setDeltaMovement(0.0, this.getDeltaMovement().y, 0.0);
        if (t == 1) {
            this.sound(level, ModSounds.VENTOR_RAGE.get(), 3.5f, 1.0f);
            Component text = Component.translatable("boss.kingdomsatwar.ventor.rage").withColor(WIND);
            for (ServerPlayer player : level.players()) {
                if (player.distanceToSqr(this) < 48.0 * 48.0) {
                    player.sendOverlayMessage(text);
                }
            }
        }
        if (t == 12) {
            this.sound(level, ModSounds.VENTOR_CROWD.get(), 3.0f, 1.1f);
        }
        if (t < 24 && t % 2 == 0) {
            for (int k = 0; k < 4; k++) {
                double a = t * 0.4 + k * Math.PI / 2.0;
                level.sendParticles(ParticleTypes.SMALL_GUST, this.getX() + Math.cos(a) * 2.0, this.getY() + 0.5 + t * 0.1,
                        this.getZ() + Math.sin(a) * 2.0, 1, 0.1, 0.1, 0.1, 0.0);
            }
        }
        if (t == 24) {
            // der Sturm bricht los: wer nah steht, fliegt
            level.sendParticles(ParticleTypes.GUST_EMITTER_LARGE, this.getX(), this.getY() + 1.5, this.getZ(), 2, 0.5, 0.5, 0.5, 0.0);
            for (LivingEntity victim : this.victims(level, this.getBoundingBox().inflate(5.0, 2.0, 5.0))) {
                Vec3 dir = this.toward(victim);
                victim.push(dir.x * 1.6, 0.5, dir.z * 1.6);
            }
        }
        if (t >= RAGE_TICKS) {
            this.begin(Attack.NONE);
            this.cooldown = 8;
            this.stormTick = 0;
        }
    }

    /**
     * Im entfesselten Sturm fegt alle {@value #STORM_PERIOD} Ticks eine Sturmböe quer durch die Arena: Erst ziehen Windfäden
     * in ihre Richtung (man sieht sie kommen), dann fliegt, wer in der Arena steht (geduckt nur halb so weit).
     */
    private void tickStorm(ServerLevel level) {
        this.stormTick++;
        int phase = this.stormTick % STORM_PERIOD;
        Vec3 c = this.home != null ? this.center() : this.position();
        if (phase == STORM_PERIOD - STORM_WARN) {
            double a = this.random.nextDouble() * Math.PI * 2.0;
            this.stormDir = new Vec3(Math.cos(a), 0.0, Math.sin(a));
            level.playSound(null, c.x, c.y + 2.0, c.z, ModSounds.VENTOR_STORM.get(), SoundSource.HOSTILE, 3.0f, 0.9f);
        }
        if (phase > STORM_PERIOD - STORM_WARN && phase % 3 == 0 && this.stormDir.lengthSqr() > 0.5) {
            // Windfäden ziehen quer durch die Arena (aus der Richtung, aus der die Böe kommt)
            Vec3 side = new Vec3(-this.stormDir.z, 0.0, this.stormDir.x);
            for (int k = 0; k < 6; k++) {
                double off = (this.random.nextDouble() - 0.5) * 22.0;
                double along = (this.random.nextDouble() - 0.5) * 22.0;
                Vec3 p = c.add(side.scale(off)).add(this.stormDir.scale(along)).add(0.0, 1.0 + this.random.nextDouble() * 2.5, 0.0);
                level.sendParticles(ParticleTypes.CLOUD, p.x, p.y, p.z, 0, this.stormDir.x, 0.0, this.stormDir.z, 0.6);
            }
        }
        if (phase == 0 && this.stormDir.lengthSqr() > 0.5) {
            level.playSound(null, c.x, c.y + 2.0, c.z, ModSounds.VENTOR_GUST.get(), SoundSource.HOSTILE, 3.0f, 0.7f);
            for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, new AABB(c, c).inflate(ARENA, 10.0, ARENA),
                    e -> this.canStrike(e) && e.distanceToSqr(this) <= this.confine * this.confine)) {
                double power = victim.isShiftKeyDown() ? 0.7 : 1.4;
                victim.push(this.stormDir.x * power, 0.35, this.stormDir.z * power);
                level.sendParticles(ParticleTypes.SMALL_GUST, victim.getX(), victim.getY(0.5), victim.getZ(), 3, 0.3, 0.4, 0.3, 0.0);
            }
            this.stormDir = Vec3.ZERO;
        }
    }

    /** Dreizack und Netz: das Netz wächst ihm nach, der Dreizack kommt zurück (notfalls ruft ihn der Wind). */
    private void tickGear(ServerLevel level) {
        if (!this.hasNet() && this.tickCount - this.netAt > NET_REGAIN) {
            this.entityData.set(DATA_NET, true);
        }
        if (!this.hasTrident() && this.tickCount - this.thrownAt > TRIDENT_LOST) {
            this.catchTrident();
            Vec3 h = this.hand(true, 1.9);
            level.sendParticles(ParticleTypes.SMALL_GUST, h.x, h.y, h.z, 6, 0.3, 0.3, 0.3, 0.0);
        }
    }

    /** Der zurückgekehrte Dreizack liegt wieder in seiner Hand. */
    public void catchTrident() {
        this.entityData.set(DATA_TRIDENT, true);
    }

    /** Sein Netz hat jemanden gefangen: gleich darauf der Stoß (oder der Wurf, wenn er zu fern steht). */
    public void onNetCaught(LivingEntity victim) {
        if (this.attack == Attack.NONE || this.attack == Attack.NET) {
            this.followUp = true;
            this.setTarget(victim);
        }
    }

    // ------------------------------------------------------------------ Böe

    /** Wer von der Böe fortgeschleudert wurde und gegen eine Wand prallt, den trifft es noch einmal. */
    private void tickFlung(ServerLevel level) {
        if (this.flung.isEmpty()) {
            return;
        }
        Iterator<Map.Entry<UUID, Integer>> it = this.flung.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, Integer> e = it.next();
            if (this.tickCount - e.getValue() > 20) {
                it.remove();
                continue;
            }
            if (this.tickCount - e.getValue() < 2 || !(level.getEntity(e.getKey()) instanceof LivingEntity victim) || !victim.isAlive()) {
                continue;
            }
            if (victim.horizontalCollision) {
                if (victim.hurtServer(level, this.damageSources().flyIntoWall(), SLAM_DAMAGE)) {
                    level.playSound(null, victim.getX(), victim.getY(), victim.getZ(), ModSounds.VENTOR_SLAM.get(), SoundSource.HOSTILE, 2.0f, 1.0f);
                    level.sendParticles(ParticleTypes.CLOUD, victim.getX(), victim.getY(0.5), victim.getZ(), 12, 0.3, 0.4, 0.3, 0.05);
                }
                it.remove();
            }
        }
    }

    /** Die Böe: Die Linke holt quer vor dem Leib aus und fegt nach außen – wer vor ihm steht, fliegt davon. */
    private void tickGust(ServerLevel level, @Nullable LivingEntity target) {
        int t = this.attackTick;
        if (t < GUST_HIT - 2 && target != null) {
            this.face(this.toward(target));
        }
        if (t == 2) {
            this.sound(level, ModSounds.VENTOR_GATHER.get(), 2.2f, 1.0f);
        }
        if (t < GUST_HIT && t % 2 == 0) {
            Vec3 h = this.hand(false, 1.7);
            level.sendParticles(ParticleTypes.SMALL_GUST, h.x, h.y, h.z, 2, 0.4, 0.4, 0.4, 0.0);
        }
        if (t != GUST_HIT) {
            return;
        }
        this.sound(level, ModSounds.VENTOR_GUST.get(), 3.0f, 1.0f);
        Vec3 f = this.forward();
        for (double d = 1.5; d <= GUST_RANGE; d += 1.5) {
            for (int k = -2; k <= 2; k++) {
                double a = Math.toRadians(k * GUST_ARC / 2.5);
                Vec3 dir = new Vec3(f.x * Math.cos(a) - f.z * Math.sin(a), 0.0, f.x * Math.sin(a) + f.z * Math.cos(a));
                Vec3 p = this.position().add(dir.scale(d)).add(0.0, 1.0, 0.0);
                level.sendParticles(d > GUST_RANGE - 2.0 ? ParticleTypes.GUST : ParticleTypes.SMALL_GUST, p.x, p.y, p.z, 1, 0.2, 0.3, 0.2, 0.0);
            }
        }
        double cos = Math.cos(Math.toRadians(GUST_ARC));
        for (LivingEntity victim : this.victims(level, this.getBoundingBox().inflate(GUST_RANGE, 3.0, GUST_RANGE))) {
            Vec3 to = victim.position().subtract(this.position()).multiply(1.0, 0.0, 1.0);
            double dist = to.length();
            if (dist > GUST_RANGE + victim.getBbWidth() * 0.5 || Math.abs(victim.getY() - this.getY()) > 4.0) {
                continue;
            }
            if (dist > 1.0 && to.normalize().dot(f) < cos) {
                continue;
            }
            Vec3 dir = dist < 1.0e-3 ? f : to.normalize();
            if (victim.hurtServer(level, this.damageSources().mobAttack(this), GUST_DAMAGE)) {
                double power = 2.1 * (1.0 - dist / GUST_RANGE * 0.4);
                victim.push(dir.x * power, 0.45, dir.z * power);
                this.flung.put(victim.getUUID(), this.tickCount);
            }
        }
    }

    // ------------------------------------------------------------------ Dreizackstoß

    /** Der Dreizackstoß: zurücknehmen, dann ein Ausfall nach vorn (wer im Netz steckt, den trifft er härter). */
    private void tickThrust(ServerLevel level, @Nullable LivingEntity target) {
        int t = this.attackTick;
        if (t < THRUST_LUNGE && target != null) {
            this.face(this.toward(target));
        }
        if (t == THRUST_LUNGE) {
            Vec3 f = this.forward();
            this.setDeltaMovement(f.x * 1.1, 0.12, f.z * 1.1);
            this.sound(level, ModSounds.VENTOR_THRUST.get(), 2.4f, 1.0f);
        }
        if (t < THRUST_HIT || t > THRUST_HIT + 3) {
            return;
        }
        Vec3 f = this.forward();
        for (LivingEntity victim : this.victims(level, this.getBoundingBox().inflate(THRUST_RANGE, 2.0, THRUST_RANGE))) {
            if (this.struck.contains(victim.getUUID())) {
                continue;
            }
            Vec3 to = victim.position().subtract(this.position()).multiply(1.0, 0.0, 1.0);
            double along = to.dot(f);
            double side = Math.abs(to.x * f.z - to.z * f.x);
            if (along < -0.5 || along > THRUST_RANGE || side > 1.3 + victim.getBbWidth() * 0.5 || Math.abs(victim.getY() - this.getY()) > 3.0) {
                continue;
            }
            this.struck.add(victim.getUUID());
            boolean caught = GladiatorNetBlock.entangled(victim);
            float damage = THRUST_DAMAGE * (caught ? ENTANGLED_BONUS : 1.0f);
            if (victim.hurtServer(level, this.damageSources().mobAttack(this), damage)) {
                victim.push(f.x * 1.2, 0.35, f.z * 1.2);
                this.sound(level, ModSounds.VENTOR_STAB.get(), 2.2f, caught ? 0.8f : 1.0f);
            }
        }
    }

    // ------------------------------------------------------------------ Netz, Wurf, Windkugeln

    /** Das Netz: über dem Kopf geschwungen, dann geworfen (mit Vorhalt, wohin das Ziel läuft). */
    private void tickNet(ServerLevel level, @Nullable LivingEntity target) {
        int t = this.attackTick;
        if (t < NET_RELEASE - 1 && target != null) {
            this.face(this.toward(target));
        }
        if (t == 6) {
            this.sound(level, ModSounds.VENTOR_NET.get(), 2.0f, 1.0f);
        }
        if (t == NET_RELEASE && target != null && this.hasNet()) {
            Vec3 from = this.hand(false, 3.1);
            Vec3 aim = target.position().add(target.getDeltaMovement().multiply(10.0, 0.0, 10.0)).add(0.0, target.getBbHeight() * 0.5, 0.0);
            GladiatorNetEntity net = new GladiatorNetEntity(level, this);
            net.launch(from, aim);
            level.addFreshEntity(net);
            this.entityData.set(DATA_NET, false);
            this.netAt = this.tickCount;
        }
    }

    /** Der Dreizackwurf: über der Schulter, dann geschleudert – er kehrt in die Hand zurück. */
    private void tickThrow(ServerLevel level, @Nullable LivingEntity target) {
        int t = this.attackTick;
        if (t < THROW_RELEASE - 1 && target != null) {
            this.face(this.toward(target));
        }
        if (t == 6) {
            this.sound(level, ModSounds.VENTOR_GATHER.get(), 1.8f, 1.4f);
        }
        if (t == THROW_RELEASE && target != null && this.hasTrident()) {
            Vec3 from = this.hand(true, 3.3);
            double ticks = from.distanceTo(target.getEyePosition()) / StormTridentEntity.SPEED;
            Vec3 aim = target.position().add(target.getDeltaMovement().multiply(ticks, 0.0, ticks)).add(0.0, target.getBbHeight() * 0.6, 0.0);
            StormTridentEntity trident = StormTridentEntity.thrownBy(level, this, from, aim, THROW_DAMAGE);
            level.addFreshEntity(trident);
            this.entityData.set(DATA_TRIDENT, false);
            this.thrownAt = this.tickCount;
            this.sound(level, ModSounds.VENTOR_THROW.get(), 2.6f, 1.0f);
        }
    }

    /** Windkugeln aus den Zinken: drei Schüsse (im entfesselten Sturm je drei im Fächer). */
    private void tickCharges(ServerLevel level, @Nullable LivingEntity target) {
        if (target != null) {
            this.face(this.toward(target));
        }
        for (int shot : CHARGE_SHOTS) {
            if (this.attackTick == shot && target != null) {
                Vec3 from = this.position().add(this.forward().scale(2.3)).add(0.0, 2.4, 0.0);
                Vec3 to = target.getEyePosition().subtract(0.0, 0.4, 0.0);
                Vec3 dir = to.subtract(from).normalize();
                int spread = this.isEnraged() ? 1 : 0;
                for (int k = -spread; k <= spread; k++) {
                    double a = Math.toRadians(k * 14.0);
                    Vec3 d = new Vec3(dir.x * Math.cos(a) - dir.z * Math.sin(a), dir.y, dir.x * Math.sin(a) + dir.z * Math.cos(a));
                    WindCharge charge = new WindCharge(level, from.x, from.y, from.z, d);
                    charge.setOwner(this);
                    charge.setDeltaMovement(d.scale(1.05));
                    level.addFreshEntity(charge);
                }
                this.sound(level, SoundEvents.BREEZE_SHOOT, 2.2f, 0.75f);
            }
        }
    }

    // ------------------------------------------------------------------ Windhosen

    /** Die Arme hoch: Windhosen erheben sich rings um sein Ziel und ziehen durch die Arena. */
    private void tickVortex(ServerLevel level, @Nullable LivingEntity target) {
        int t = this.attackTick;
        if (t == 4) {
            this.sound(level, ModSounds.VENTOR_VORTEX.get(), 3.0f, 1.0f);
        }
        if (t < VORTEX_SPAWN && t % 2 == 0) {
            level.sendParticles(ParticleTypes.SMALL_GUST, this.getX(), this.getY() + 3.4, this.getZ(), 3, 0.8, 0.4, 0.8, 0.0);
        }
        if (t != VORTEX_SPAWN) {
            return;
        }
        int count = Math.min(this.isEnraged() ? 4 : 3, VORTEX_MAX - this.vorticesAlive(level));
        Vec3 around = target != null ? target.position() : this.position();
        Vec3 c = this.center();
        for (int i = 0; i < count; i++) {
            double a = this.random.nextDouble() * Math.PI * 2.0;
            double d = 4.0 + this.random.nextDouble() * 3.0;
            Vec3 p = around.add(Math.cos(a) * d, 0.0, Math.sin(a) * d);
            if (this.home != null) {
                Vec3 off = p.subtract(c).multiply(1.0, 0.0, 1.0);
                double max = 10.5;
                if (off.length() > max) {
                    p = c.add(off.normalize().scale(max)).add(0.0, p.y - c.y, 0.0);
                }
            }
            WindVortexEntity vortex = new WindVortexEntity(level, this, ground(level, p));
            level.addFreshEntity(vortex);
            this.vortices.add(vortex.getUUID());
        }
    }

    /** Der Boden unter (oder knapp über) einem Punkt. */
    private static Vec3 ground(ServerLevel level, Vec3 p) {
        BlockPos pos = BlockPos.containing(p.x, p.y + 2.0, p.z);
        for (int i = 0; i < 8; i++) {
            BlockPos below = pos.below();
            if (!level.getBlockState(below).getCollisionShape(level, below).isEmpty() && level.getBlockState(pos).getCollisionShape(level, pos).isEmpty()) {
                return new Vec3(p.x, pos.getY(), p.z);
            }
            pos = below;
        }
        return p;
    }

    // ------------------------------------------------------------------ Sturzflug

    /** Der Sturzflug: hinauf, schweben (ein Kreis zeichnet sich um das Ziel), hinab mit den Zinken voran. */
    private void tickDive(ServerLevel level, @Nullable LivingEntity target) {
        int t = this.attackTick;
        if (t < DIVE_RISE && target != null) {
            this.face(this.toward(target));
        }
        if (t == DIVE_RISE) {
            this.setNoGravity(true);
            this.diveStart = this.position();
            this.diveMark = target != null ? ground(level, target.position()) : this.position();
            this.sound(level, ModSounds.VENTOR_RISE.get(), 2.6f, 1.0f);
            level.sendParticles(ParticleTypes.GUST_EMITTER_SMALL, this.getX(), this.getY() + 0.3, this.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
        }
        if (t >= DIVE_RISE && t < DIVE_HOVER) {
            double want = this.diveStart.y + DIVE_HEIGHT;
            double up = Math.min(0.5, Math.max(0.0, want - this.getY()));
            if (!level.noCollision(this, this.getBoundingBox().move(0.0, up + 0.2, 0.0))) {
                up = 0.0;
            }
            Vec3 drift = target != null ? this.toward(target).scale(0.1) : Vec3.ZERO;
            this.setDeltaMovement(drift.x, up, drift.z);
            if (t % 2 == 0) {
                level.sendParticles(ParticleTypes.CLOUD, this.getX(), this.getY(), this.getZ(), 3, 0.3, 0.2, 0.3, 0.02);
            }
        }
        if (t >= DIVE_HOVER && t < DIVE_FALL) {
            if (t < DIVE_LOCK && target != null) {
                this.diveMark = ground(level, target.position());
            }
            Vec3 over = this.diveMark.subtract(this.position()).multiply(1.0, 0.0, 1.0);
            Vec3 drift = over.lengthSqr() > 0.25 ? over.normalize().scale(Math.min(0.12, over.length() * 0.2)) : Vec3.ZERO;
            this.setDeltaMovement(drift.x, 0.0, drift.z);
            this.face(this.toward(this.diveMark));
            if (t >= DIVE_MARK && t % 2 == 0) {
                this.markRing(level);
            }
        }
        if (t == DIVE_FALL) {
            this.sound(level, ModSounds.VENTOR_DIVE.get(), 3.0f, 1.0f);
        }
        if (t >= DIVE_FALL && !this.landed) {
            Vec3 to = this.diveMark.subtract(this.position());
            double len = to.length();
            if (len < 0.8 || this.onGround() && t > DIVE_FALL + 1 || t > DIVE_FALL + 14) {
                this.land(level);
            } else {
                Vec3 v = to.normalize().scale(Math.min(1.6, len));
                this.setDeltaMovement(v);
                level.sendParticles(ParticleTypes.CLOUD, this.getX(), this.getY() + 1.0, this.getZ(), 2, 0.2, 0.4, 0.2, 0.0);
            }
        }
        if (this.landed) {
            this.setDeltaMovement(0.0, this.getDeltaMovement().y, 0.0);
        }
    }

    /** Der Kreis am Boden, in den er stürzen wird. */
    private void markRing(ServerLevel level) {
        DustParticleOptions dust = new DustParticleOptions(WIND, 1.3f);
        for (int k = 0; k < 20; k++) {
            double a = k * Math.PI * 2.0 / 20.0 + this.attackTick * 0.05;
            level.sendParticles(dust, this.diveMark.x + Math.cos(a) * DIVE_RADIUS, this.diveMark.y + 0.15, this.diveMark.z + Math.sin(a) * DIVE_RADIUS,
                    1, 0.0, 0.0, 0.0, 0.0);
        }
        level.sendParticles(ParticleTypes.SMALL_GUST, this.diveMark.x, this.diveMark.y + 0.3, this.diveMark.z, 1, 0.5, 0.1, 0.5, 0.0);
    }

    /** Der Aufschlag: eine Druckwelle – in der Mitte am härtesten; wer nah steht, fliegt. */
    private void land(ServerLevel level) {
        this.landed = true;
        this.setNoGravity(false);
        this.setDeltaMovement(0.0, -0.3, 0.0);
        Vec3 c = this.position();
        this.sound(level, ModSounds.VENTOR_IMPACT.get(), 3.2f, 1.0f);
        level.sendParticles(ParticleTypes.GUST_EMITTER_LARGE, c.x, c.y + 0.5, c.z, 1, 0.0, 0.0, 0.0, 0.0);
        BlockPos below = BlockPos.containing(c.x, c.y - 0.5, c.z);
        BlockState ground = level.getBlockState(below);
        if (!ground.isAir()) {
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, ground), c.x, c.y + 0.2, c.z, 60, DIVE_RADIUS * 0.5, 0.2,
                    DIVE_RADIUS * 0.5, 0.2);
        }
        for (int k = 0; k < 24; k++) {
            double a = k * Math.PI * 2.0 / 24.0;
            level.sendParticles(ParticleTypes.CLOUD, c.x + Math.cos(a) * 1.2, c.y + 0.2, c.z + Math.sin(a) * 1.2, 0, Math.cos(a), 0.05,
                    Math.sin(a), 0.45);
        }
        for (LivingEntity victim : this.victims(level, this.getBoundingBox().inflate(DIVE_RADIUS + 0.5, 2.0, DIVE_RADIUS + 0.5))) {
            Vec3 to = victim.position().subtract(c).multiply(1.0, 0.0, 1.0);
            double dist = to.length();
            if (dist > DIVE_RADIUS + victim.getBbWidth() * 0.5 || victim.getY() - c.y > 3.0 || c.y - victim.getY() > 1.5) {
                continue;
            }
            float damage = (float) (DIVE_DAMAGE * (1.0 - 0.5 * Math.min(1.0, dist / DIVE_RADIUS)));
            if (victim.hurtServer(level, this.damageSources().mobAttack(this), damage)) {
                Vec3 dir = dist < 1.0e-3 ? this.forward() : to.normalize();
                victim.push(dir.x * 1.0, 0.65, dir.z * 1.0);
            }
        }
    }

    // ------------------------------------------------------------------ Zyklon

    /** Der Zyklon: Er dreht sich mit ausgebreiteten Armen auf sein Ziel zu; alles ringsum zieht es zu ihm. */
    private void tickCyclone(ServerLevel level, @Nullable LivingEntity target) {
        int t = this.attackTick;
        if (t == CYCLONE_FROM) {
            this.sound(level, ModSounds.VENTOR_CYCLONE.get(), 3.0f, 1.0f);
        }
        if (t < CYCLONE_FROM || t >= CYCLONE_TO) {
            if (t < CYCLONE_FROM && target != null) {
                this.face(this.toward(target));
            }
            return;
        }
        if (target != null) {
            Vec3 dir = this.toward(target);
            this.setDeltaMovement(dir.x * 0.17, this.getDeltaMovement().y, dir.z * 0.17);
        }
        if (t % 2 == 0) {
            for (int k = 0; k < 3; k++) {
                double a = t * 0.6 + k * Math.PI * 2.0 / 3.0;
                level.sendParticles(ParticleTypes.SMALL_GUST, this.getX() + Math.cos(a) * 2.4, this.getY() + 0.6 + (k * 0.9),
                        this.getZ() + Math.sin(a) * 2.4, 1, 0.05, 0.1, 0.05, 0.0);
            }
        }
        for (LivingEntity victim : this.victims(level, this.getBoundingBox().inflate(CYCLONE_PULL, 2.0, CYCLONE_PULL))) {
            Vec3 to = this.position().subtract(victim.position()).multiply(1.0, 0.0, 1.0);
            double dist = to.length();
            if (dist > CYCLONE_PULL || dist < 1.0e-3) {
                continue;
            }
            Vec3 pull = to.normalize().scale(0.05 * (1.0 - dist / CYCLONE_PULL) + 0.015);
            victim.push(pull.x, 0.0, pull.z);
            if (t % 8 == 0 && dist < CYCLONE_RADIUS + victim.getBbWidth() * 0.5
                    && victim.hurtServer(level, this.damageSources().mobAttack(this), CYCLONE_DAMAGE)) {
                Vec3 out = to.normalize().scale(-1.0);
                Vec3 tangent = new Vec3(-out.z, 0.0, out.x);
                victim.push(out.x * 0.9 + tangent.x * 0.5, 0.55, out.z * 0.9 + tangent.z * 0.5);
                this.sound(level, ModSounds.VENTOR_STAB.get(), 1.6f, 1.3f);
            }
        }
    }

    // ------------------------------------------------------------------ Tod, Speichern, Klänge

    @Override
    public void die(DamageSource source) {
        super.die(source);
        this.setNoGravity(false);
        if (this.level() instanceof ServerLevel level) {
            for (UUID id : this.vortices) {
                if (level.getEntity(id) instanceof WindVortexEntity vortex) {
                    vortex.discard();
                }
            }
            this.vortices.clear();
            if (this.seal != null && level.getBlockEntity(this.seal) instanceof ArenaSealBlockEntity sealEntity) {
                sealEntity.onVentorDefeated(level);
            }
        }
    }

    /** Der Wind erlahmt: Er sinkt in sich zusammen, das Standbild stürzt – dann verweht der Rest. */
    @Override
    protected void tickDeath() {
        this.deathTime++;
        if (!(this.level() instanceof ServerLevel level)) {
            return;
        }
        if (this.deathTime < 40 && this.deathTime % 3 == 0) {
            level.sendParticles(ParticleTypes.CLOUD, this.getX(), this.getY() + 0.6, this.getZ(), 3, 0.4, 0.4, 0.4, 0.03);
        }
        if (this.deathTime == 52) {
            this.sound(level, ModSounds.VENTOR_FALL.get(), 2.6f, 1.0f);
            level.sendParticles(new DustParticleOptions(BRONZE, 1.4f), this.getX(), this.getY() + 0.4, this.getZ(), 30, 0.8, 0.2, 0.8, 0.0);
        }
        if (this.deathTime == DEATH_TICKS - 2) {
            level.sendParticles(ParticleTypes.GUST_EMITTER_LARGE, this.getX(), this.getY() + 0.6, this.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
            level.sendParticles(new DustParticleOptions(WIND, 1.4f), this.getX(), this.getY() + 0.6, this.getZ(), 40, 0.8, 0.5, 0.8, 0.0);
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
        if (this.seal != null) {
            output.putLong("Seal", this.seal.asLong());
        }
        output.putBoolean("Enraged", this.isEnraged());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        input.getLong("Home").ifPresent(h -> this.home = BlockPos.of(h));
        input.getLong("Seal").ifPresent(s -> this.seal = BlockPos.of(s));
        this.setEnraged(input.getBooleanOr("Enraged", false));
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return ModSounds.VENTOR_AMBIENT.get();
    }

    @Override
    public int getAmbientSoundInterval() {
        return 160;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.VENTOR_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.VENTOR_DEATH.get();
    }

    @Override
    protected float getSoundVolume() {
        return 2.0f;
    }

    // ------------------------------------------------------------------ Himmelswacht

    @Override
    public SkyTheme skyTheme() {
        return SkyTheme.WIND;
    }
}
