package io.github.sirschrolli.kingdomsatwar.entity.deep;

import io.github.sirschrolli.kingdomsatwar.block.deep.OrganDeskBlockEntity;
import io.github.sirschrolli.kingdomsatwar.entity.boss.sky.SkyTheme;
import io.github.sirschrolli.kingdomsatwar.entity.boss.sky.SkyWarden;
import io.github.sirschrolli.kingdomsatwar.magic.fx.SpellFxEntity;
import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
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
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Sordin, der Kantor der Stille (seit 1.7): Er sang einst in der Stillen Kathedrale das Lied, das die Tiefe zum Schweigen
 * brachte, und sang so lange, bis der Sculk durch seine Kutte wuchs und die Pfeifen der Orgel in seinen Rücken. Nun steht er
 * am Spieltisch der Orgel und spielt, ohne dass ein Ton erklingt – unverwundbar, bis jemand näher als
 * {@value #WAKE_BLOCKS} Blöcke herankommt, ihn angreift, auf der Orgel spielt oder in seiner Nähe Lärm macht (er hört wie der
 * Tiefenhüter: wer schleicht oder die Wollsohlen trägt, den hört er nicht). Dann kämpft er mit Klang:
 * <ul>
 *     <li><b>Gabelhieb</b> – ein schwerer Hieb mit der Stimmgabel;</li>
 *     <li><b>Schallwelle</b> – er schlägt die Gabel an, zielt (eine feine Linie zeigt, wohin) und lässt den Ton los: ein
 *     Schallschlag, der durch jede Rüstung und durch Stein dringt – nur <b>Wolle</b> hält ihn auf;</li>
 *     <li><b>Akkord</b> – er dirigiert; bei jedem Schlag läuft ein Ring über den Boden (darüber springen!);</li>
 *     <li><b>Notenschwarm</b> – er singt, und aus den Pfeifen auf seinem Rücken steigen Noten, die ihre Opfer suchen;</li>
 *     <li><b>Stille</b> – er legt den Finger an den Mund: Dunkelheit fällt, und er verschwindet, um hinter seinem Opfer
 *     wieder aufzutauchen (wer die Echoortung der Lauscher kennt, sieht ihn);</li>
 *     <li><b>Fortissimo</b> – unter halber Kraft lassen alle Pfeifen zugleich einen Ton los, der alle fortwirft; danach ist er
 *     schneller, die Schallwelle teilt sich in drei, der Akkord hat vier Schläge, der Schwarm mehr Noten.</li>
 * </ul>
 * Spielt jemand während des Kampfs auf einem Notenblock (etwa auf der Orgel), hält er sich die Ohren zu und taumelt – dann
 * trifft man ihn leichter (höchstens alle {@value #STAGGER_PAUSE_SECONDS} Sekunden). Findet er niemanden mehr, kehrt er an die
 * Orgel zurück und spielt weiter (geheilt).
 */
public class SordinEntity extends Monster implements SkyWarden {
    public static final float MAX_HEALTH = 760.0f;
    public static final int AWAKEN_TICKS = 80;
    /** So lange (Ticks) dauert sein Sterben (er sinkt auf die Knie, die Pfeifen verstummen, er zerfällt zu Sculk). */
    public static final int DEATH_TICKS = 100;
    /** So nah (waagerecht) muss man ihm kommen, damit er erwacht. */
    public static final double WAKE_RADIUS = 10.0;
    static final int WAKE_BLOCKS = 10;
    /** So weit hört er Lärm (Schritte, Sprünge, abgebaute Blöcke …), wenn er an der Orgel steht. */
    public static final double HEAR_RADIUS = 24.0;
    /** Weiter als so weit von seinem Platz folgt er niemandem … */
    private static final double LEASH = 44.0;
    /** … und wer ihn aus der Kathedrale lockt, dem entkommt er zurück an die Orgel. */
    public static final double ARENA = 30.0;
    private static final int RETURN_TICKS = 400;

    public static final float SWING_DAMAGE = 10.0f;
    public static final double SWING_RANGE = 4.6;
    public static final int SWING_HIT = 14;
    public static final float SONIC_DAMAGE = 10.0f;
    public static final double SONIC_RANGE = 24.0;
    /** Ab hier zielt er nicht mehr nach (wer jetzt zur Seite tritt, entgeht dem Schlag) … */
    public static final int SONIC_LOCK = 20;
    /** … und hier lässt er den Ton los. */
    public static final int SONIC_FIRE = 27;
    /** So breit ist der Schallschlag (Blöcke, um die Linie). */
    public static final double SONIC_WIDTH = 1.0;
    /** Fortissimo: so weit fächern die beiden äußeren Schallwellen auf (Grad). */
    public static final float SONIC_FAN = 16.0f;
    public static final float CHORD_DAMAGE = 6.0f;
    public static final int[] CHORD_BEATS = {14, 28, 42};
    public static final int CHORD_EXTRA_BEAT = 54;
    /** So schnell laufen die Ringe des Akkords (Blöcke je Tick) und so lange (Ticks). */
    public static final float CHORD_SPEED = 0.42f;
    public static final int CHORD_LIFE = 30;
    public static final float NOTE_DAMAGE = 3.0f;
    public static final int NOTE_COUNT = 8;
    public static final int NOTE_COUNT_FORTISSIMO = 12;
    public static final int NOTE_FIRST = 12;
    public static final int NOTE_LIFE = 90;
    public static final double NOTE_SPEED = 0.42;
    public static final float SILENCE_DAMAGE = 12.0f;
    public static final int SILENCE_VANISH = 12;
    public static final int SILENCE_APPEAR = 44;
    public static final int SILENCE_HIT = 54;
    public static final int SILENCE_DARKNESS = 100;
    public static final float FORTISSIMO_DAMAGE = 5.0f;
    public static final int FORTISSIMO_BLAST = 40;
    public static final double FORTISSIMO_RADIUS = 9.0;
    public static final int STAGGER_TICKS = 60;
    /** So viel mehr Schaden nimmt er, solange er taumelt. */
    public static final float STAGGER_WEAKNESS = 1.3f;
    /** So lange (Ticks) nach einem Taumeln lässt ihn eine Orgel kalt. */
    public static final int STAGGER_PAUSE = 300;
    static final int STAGGER_PAUSE_SECONDS = 15;
    /** Die Farbe seines Lichts: das Türkis des Sculk. */
    public static final int SCULK = 0x29DFEB;
    /** Das blasse Blau des Hallkristalls (seine Stimmgabel). */
    public static final int CRYSTAL = 0x9CD8FF;

    public enum Attack {
        NONE(0), GUARD(0), AWAKEN(AWAKEN_TICKS), SWING(30), SONIC(50), CHORD(60), NOTES(60), SILENCE(70), FORTISSIMO(76),
        STAGGER(STAGGER_TICKS);

        public final int duration;

        Attack(int duration) {
            this.duration = duration;
        }
    }

    private static final EntityDataAccessor<Integer> DATA_ATTACK = SynchedEntityData.defineId(SordinEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_FORTISSIMO = SynchedEntityData.defineId(SordinEntity.class, EntityDataSerializers.BOOLEAN);

    private final ServerBossEvent bossBar = new ServerBossEvent(UUID.randomUUID(), Component.translatable("boss.kingdomsatwar.sordin"),
            BossEvent.BossBarColor.BLUE, BossEvent.BossBarOverlay.NOTCHED_10);
    private Attack attack = Attack.NONE;
    private Attack lastAttack = Attack.NONE;
    private int attackTick;
    private int cooldown = 30;
    private int idleTicks;
    private long lastStagger = -STAGGER_PAUSE;
    /** Für Tests: nur wer so nah ist, wird getroffen (siehe {@link #confine}). */
    private double confine = Double.MAX_VALUE;
    private @Nullable BlockPos home;
    private @Nullable BlockPos desk;
    private final Set<UUID> struck = new HashSet<>();
    /** Schallwelle: die Richtungen, in die er zielt (fest ab {@link #SONIC_LOCK}). */
    private final List<Vec3> aims = new ArrayList<>();
    /** Die Ringe des Akkords, die gerade laufen. */
    private final List<Ring> rings = new ArrayList<>();
    /** Die Noten des Schwarms, die gerade fliegen. */
    private final List<Note> notes = new ArrayList<>();
    private int clientAttackStart;

    /** Ein Ring des Akkords: Mitte, Start (Spielzeit) und wen er schon traf. */
    private record Ring(Vec3 center, long start, Set<UUID> hit) {}

    /** Eine Note des Schwarms: wo sie ist, wohin sie fliegt, wie alt sie ist, welche Farbe sie hat, wen sie sucht. */
    private static final class Note {
        Vec3 pos;
        Vec3 velocity;
        int age;
        final float hue;
        final @Nullable UUID target;

        Note(Vec3 pos, Vec3 velocity, float hue, @Nullable UUID target) {
            this.pos = pos;
            this.velocity = velocity;
            this.hue = hue;
            this.target = target;
        }
    }

    public SordinEntity(EntityType<? extends SordinEntity> type, Level level) {
        super(type, level);
        this.xpReward = 450;
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, MAX_HEALTH)
                .add(Attributes.ARMOR, 10.0)
                .add(Attributes.ARMOR_TOUGHNESS, 4.0)
                .add(Attributes.ATTACK_DAMAGE, SWING_DAMAGE)
                .add(Attributes.MOVEMENT_SPEED, 0.28)
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
        builder.define(DATA_FORTISSIMO, false);
    }

    public Attack currentAttack() {
        int i = this.entityData.get(DATA_ATTACK);
        return i >= 0 && i < Attack.values().length ? Attack.values()[i] : Attack.NONE;
    }

    public int attackOrdinal() {
        return this.entityData.get(DATA_ATTACK);
    }

    /** Hat er das Fortissimo schon losgelassen (unter halber Kraft: schneller, mehr Wellen, mehr Noten)? */
    public boolean isFortissimo() {
        return this.entityData.get(DATA_FORTISSIMO);
    }

    /** Steht er an der Orgel und spielt (unverwundbar)? */
    public boolean isPlaying() {
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
    }

    /** Bindet ihn an sein Pult: Er steht bei {@code home} an der Orgel und spielt (unverwundbar, bis er etwas hört). */
    public void bindTo(BlockPos home, @Nullable BlockPos desk) {
        this.home = home.immutable();
        this.desk = desk == null ? null : desk.immutable();
        this.begin(Attack.GUARD);
    }

    /** Neu gerufen (ein Hallkristall auf dem verstummten Pult): Er erwacht sofort. */
    public void rise(BlockPos home, @Nullable BlockPos desk) {
        this.home = home.immutable();
        this.desk = desk == null ? null : desk.immutable();
        this.begin(Attack.AWAKEN);
    }

    /** Er erwacht (wenn er spielt). */
    public void awaken() {
        if (this.attack == Attack.GUARD || this.attack == Attack.NONE && this.getTarget() == null) {
            this.begin(Attack.AWAKEN);
        }
    }

    public @Nullable BlockPos home() {
        return this.home;
    }

    public @Nullable BlockPos desk() {
        return this.desk;
    }

    /** Beginnt sofort einen bestimmten Angriff (für Tests und die Vorführung). */
    public void forceAttack(Attack next) {
        this.begin(next);
    }

    /** Sofort im Fortissimo (für Tests und die Vorführung). */
    public void forceFortissimo() {
        this.entityData.set(DATA_FORTISSIMO, true);
    }

    /** Für Tests: wie viele Noten gerade fliegen und wie viele Ringe laufen. */
    public int noteCount() {
        return this.notes.size();
    }

    public int ringCount() {
        return this.rings.size();
    }

    private void begin(Attack next) {
        this.attack = next;
        this.attackTick = 0;
        this.entityData.set(DATA_ATTACK, next.ordinal());
        if (next != Attack.NONE && next != Attack.AWAKEN && next != Attack.GUARD && next != Attack.FORTISSIMO && next != Attack.STAGGER) {
            this.lastAttack = next;
        }
        this.struck.clear();
        this.aims.clear();
        if (next != Attack.NONE) {
            this.getNavigation().stop();
        }
        if (this.isInvisible()) {
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

    /** Er hört, er sieht nicht: Dunkelheit und Blindheit kümmern ihn nicht. */
    @Override
    public boolean canBeAffected(MobEffectInstance effect) {
        return !effect.is(MobEffects.DARKNESS) && !effect.is(MobEffects.BLINDNESS) && super.canBeAffected(effect);
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
     * Beim Spielen, beim Erwachen und während des Fortissimo ist er unverwundbar (wer ihn beim Spielen angreift, weckt ihn).
     * Taumelt er, nimmt er mehr Schaden; wird er unsichtbar getroffen, taucht er auf.
     */
    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return super.hurtServer(level, source, amount);
        }
        if (this.attack == Attack.GUARD) {
            if (source.getEntity() instanceof Player player && !player.isCreative() && !player.isSpectator()) {
                this.wakeDesk(level);
            }
            return false;
        }
        if (this.attack == Attack.AWAKEN || this.attack == Attack.FORTISSIMO && this.attackTick < FORTISSIMO_BLAST + 4
                || source.is(DamageTypes.IN_WALL) || source.is(DamageTypes.FALL) || source.is(DamageTypes.SONIC_BOOM)) {
            return false;
        }
        if (this.attack == Attack.STAGGER) {
            amount *= STAGGER_WEAKNESS;
        }
        boolean hurt = super.hurtServer(level, source, amount);
        if (hurt && this.isInvisible()) {
            this.reveal(level);
        }
        if (hurt && source.getEntity() instanceof LivingEntity attacker && attacker != this && this.canStrike(attacker)
                && !(this.getTarget() instanceof Player)) {
            this.setTarget(attacker);
        }
        return hurt;
    }

    private void wakeDesk(ServerLevel level) {
        if (this.desk != null && level.getBlockEntity(this.desk) instanceof OrganDeskBlockEntity organ && organ.owns(this)) {
            organ.awaken(level);
        } else {
            this.begin(Attack.AWAKEN);
        }
    }

    /**
     * Jemand spielt auf einer Orgel (einem Notenblock) in seiner Nähe: Spielt er selbst, erwacht er; kämpft er, hält er sich die
     * Ohren zu und taumelt (höchstens alle {@value #STAGGER_PAUSE_SECONDS} Sekunden). Gibt zurück, ob er taumelt.
     */
    public boolean hearOrgan(ServerLevel level) {
        if (this.isDeadOrDying()) {
            return false;
        }
        if (this.attack == Attack.GUARD) {
            this.wakeDesk(level);
            return false;
        }
        if (this.attack == Attack.AWAKEN || this.attack == Attack.FORTISSIMO || this.attack == Attack.STAGGER
                || level.getGameTime() - this.lastStagger < STAGGER_PAUSE) {
            return false;
        }
        this.lastStagger = level.getGameTime();
        this.begin(Attack.STAGGER);
        this.sound(level, ModSounds.SORDIN_STAGGER.get(), 2.6f, 1.0f);
        level.sendParticles(ParticleTypes.SCULK_CHARGE_POP, this.getX(), this.getY(0.85), this.getZ(), 30, 0.6, 0.5, 0.6, 0.05);
        return true;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide()) {
            this.clientEffects();
        }
    }

    /** Sculk-Seelen steigen von seinem Saum auf, über den Pfeifen glimmt es (im Fortissimo mehr); unsichtbar kaum etwas. */
    private void clientEffects() {
        Level level = this.level();
        if (this.isInvisible()) {
            if (this.random.nextInt(6) == 0) {
                level.addParticle(ParticleTypes.SCULK_SOUL, this.getRandomX(0.6), this.getY() + 0.1, this.getRandomZ(0.6), 0.0, 0.01, 0.0);
            }
            return;
        }
        boolean loud = this.isFortissimo();
        if (this.random.nextInt(loud ? 3 : 8) == 0) {
            level.addParticle(ParticleTypes.SCULK_SOUL, this.getRandomX(0.7), this.getY() + 0.15, this.getRandomZ(0.7), 0.0, 0.02, 0.0);
        }
        if (this.random.nextInt(loud ? 2 : 6) == 0) {
            float yaw = this.yBodyRot * Mth.DEG_TO_RAD;
            double back = 0.55;
            level.addParticle(new DustParticleOptions(SCULK, 0.7f), this.getX() + Mth.sin(yaw) * back + (this.random.nextDouble() - 0.5) * 1.2,
                    this.getY() + 3.3 + this.random.nextDouble() * 1.0, this.getZ() - Mth.cos(yaw) * back + (this.random.nextDouble() - 0.5) * 1.2,
                    0.0, 0.03, 0.0);
        }
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        this.bossBar.setProgress(this.getHealth() / this.getMaxHealth());
        this.tickRings(level);
        this.tickNotes(level);
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
            this.keepInCathedral(level);
        }
        if (this.attack == Attack.STAGGER) {
            this.tickStagger(level);
            return;
        }
        if (!this.isFortissimo() && this.attack != Attack.FORTISSIMO && this.attack != Attack.SILENCE
                && this.getHealth() < this.getMaxHealth() * 0.5f) {
            this.begin(Attack.FORTISSIMO);
        }
        if (this.attack == Attack.FORTISSIMO) {
            this.tickFortissimo(level);
            return;
        }
        LivingEntity target = this.findTarget(level);
        this.setTarget(target);
        if (target == null) {
            this.idle(level);
            return;
        }
        this.idleTicks = 0;
        if (!this.isInvisible()) {
            this.getLookControl().setLookAt(target, 30.0f, 30.0f);
        }
        if (this.attack == Attack.NONE) {
            this.approach(target);
            if (--this.cooldown <= 0) {
                this.begin(this.chooseAttack(target));
            }
            return;
        }
        this.attackTick++;
        if (this.attack != Attack.SILENCE && (this.attack != Attack.SONIC || this.attackTick < SONIC_LOCK)) {
            this.lookAt(target, 40.0f, 40.0f);
        }
        switch (this.attack) {
            case SWING -> this.tickSwing(level, target);
            case SONIC -> this.tickSonic(level, target);
            case CHORD -> this.tickChord(level);
            case NOTES -> this.tickNotesAttack(level, target);
            case SILENCE -> this.tickSilence(level, target);
            default -> { }
        }
        if (this.attackTick >= this.attack.duration) {
            this.begin(Attack.NONE);
            this.cooldown = this.isFortissimo() ? 10 + this.random.nextInt(14) : 22 + this.random.nextInt(18);
        }
    }

    /** Gehört er (noch) zu seinem Pult? Ist dort inzwischen ein anderer Kantor gebunden, zerfällt dieser hier. */
    private boolean strayed(ServerLevel level) {
        if (this.desk == null || !level.isLoaded(this.desk)) {
            return false;
        }
        if (level.getBlockEntity(this.desk) instanceof OrganDeskBlockEntity organ) {
            if (!organ.owns(this)) {
                level.sendParticles(ParticleTypes.SCULK_SOUL, this.getX(), this.getY(0.5), this.getZ(), 40, 0.5, 1.5, 0.5, 0.05);
                this.discard();
                return true;
            }
            return false;
        }
        this.desk = null;
        if (this.attack == Attack.GUARD) {
            this.begin(Attack.NONE);
        }
        return false;
    }

    /** Wen seine Angriffe treffen: alle Lebewesen außer ihm und Spielern im Kreativ-/Zuschauermodus. */
    public boolean canStrike(LivingEntity entity) {
        return entity != this && entity.isAlive() && !(entity instanceof SordinEntity) && !(entity instanceof ArmorStand)
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

    /** Sein Platz an der Orgel (sonst wo er steht). */
    public Vec3 center() {
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

    private void sound(ServerLevel level, SoundEvent sound, float volume, float pitch) {
        level.playSound(null, this.getX(), this.getY(), this.getZ(), sound, SoundSource.HOSTILE, volume, pitch);
    }

    /** Er schreitet auf sein Ziel zu (bis auf gut drei Blöcke). */
    private void approach(LivingEntity target) {
        double distance = this.distanceTo(target);
        if (distance > 3.6) {
            this.getNavigation().moveTo(target, this.isFortissimo() ? 1.25 : 1.0);
        } else {
            this.getNavigation().stop();
        }
    }

    /** Aus der Kathedrale gelockt (oder in die Tiefe gestürzt): Er zerfließt zu Sculk und steht wieder an der Orgel. */
    private void keepInCathedral(ServerLevel level) {
        if (this.home == null || this.attack == Attack.SILENCE) {
            return;
        }
        Vec3 c = this.center();
        double dx = this.getX() - c.x;
        double dz = this.getZ() - c.z;
        if (dx * dx + dz * dz < ARENA * ARENA && this.getY() > c.y - 8.0 && this.getY() < c.y + 16.0) {
            return;
        }
        this.vanishTo(level, c);
    }

    private void vanishTo(ServerLevel level, Vec3 to) {
        level.sendParticles(ParticleTypes.SCULK_SOUL, this.getX(), this.getY(0.5), this.getZ(), 40, 0.5, 1.4, 0.5, 0.05);
        this.teleportTo(to.x, to.y, to.z);
        this.setDeltaMovement(Vec3.ZERO);
        this.getNavigation().stop();
        level.sendParticles(ParticleTypes.SCULK_SOUL, to.x, to.y + 1.5, to.z, 40, 0.5, 1.4, 0.5, 0.05);
        this.sound(level, ModSounds.SORDIN_HUSH.get(), 1.6f, 1.2f);
    }

    /** Ohne Gegner: zurück an die Orgel; nach einer Weile spielt er weiter. */
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
                this.vanishTo(level, Vec3.atBottomCenterOf(this.home));
            }
        }
        if (this.home != null && this.desk != null && this.idleTicks > RETURN_TICKS
                && this.distanceToSqr(Vec3.atBottomCenterOf(this.home)) < 2.5) {
            this.returnToOrgan(level);
        }
    }

    /** Er tritt wieder an die Orgel und spielt – geheilt, ohne Fortissimo. */
    private void returnToOrgan(ServerLevel level) {
        if (this.desk == null || !(level.getBlockEntity(this.desk) instanceof OrganDeskBlockEntity organ) || !organ.owns(this)) {
            return;
        }
        this.setHealth(this.getMaxHealth());
        this.entityData.set(DATA_FORTISSIMO, false);
        this.idleTicks = 0;
        this.faceOrgan();
        this.notes.clear();
        this.rings.clear();
        this.begin(Attack.GUARD);
        organ.onCantorReturned(level, this);
    }

    /** Er blickt zur Orgel (das Pult steht vor ihm). */
    public void faceOrgan() {
        if (this.home == null || this.desk == null) {
            return;
        }
        double dx = this.desk.getX() - this.home.getX();
        double dz = this.desk.getZ() - this.home.getZ();
        float yaw = (float) (Mth.atan2(dz, dx) * Mth.RAD_TO_DEG) - 90.0f;
        this.snapTo(this.home.getX() + 0.5, this.home.getY(), this.home.getZ() + 0.5, yaw, 0.0f);
        this.yBodyRot = yaw;
        this.yHeadRot = yaw;
    }

    private Attack chooseAttack(LivingEntity target) {
        double distance = this.distanceTo(target);
        List<Attack> options = new ArrayList<>();
        if (distance < 5.0) {
            options.addAll(List.of(Attack.SWING, Attack.SWING, Attack.CHORD, Attack.SILENCE, Attack.SONIC));
        } else if (distance < 12.0) {
            options.addAll(List.of(Attack.SONIC, Attack.SONIC, Attack.CHORD, Attack.NOTES, Attack.SILENCE));
        } else {
            options.addAll(List.of(Attack.SONIC, Attack.SONIC, Attack.NOTES, Attack.SILENCE));
        }
        if (this.isFortissimo()) {
            options.add(Attack.NOTES);
            options.add(Attack.CHORD);
        }
        options.removeIf(a -> a == this.lastAttack);
        if (options.isEmpty()) {
            options.add(distance < 5.0 ? Attack.SWING : Attack.SONIC);
        }
        return options.get(this.random.nextInt(options.size()));
    }

    // ------------------------------------------------------------------ Spielen und Erwachen

    /** Er steht an der Orgel und spielt, ohne dass ein Ton erklingt – nur ein leises Summen der Pfeifen, das man von fern hört. */
    private void tickGuard(ServerLevel level) {
        int t = ++this.attackTick;
        this.setDeltaMovement(0.0, this.getDeltaMovement().y, 0.0);
        if (t % 140 == 30) {
            this.sound(level, ModSounds.SORDIN_ORGAN.get(), 1.4f, 0.9f + this.random.nextFloat() * 0.2f);
        }
        if (t % 10 == 0) {
            float yaw = this.yBodyRot * Mth.DEG_TO_RAD;
            level.sendParticles(new DustParticleOptions(SCULK, 0.6f), this.getX() + Mth.sin(yaw) * 0.6, this.getY() + 4.0,
                    this.getZ() - Mth.cos(yaw) * 0.6, 2, 0.6, 0.4, 0.6, 0.0);
        }
    }

    /** Er hält inne, lauscht, wendet sich um, reckt die Stimmgabel empor – und schlägt sie an. */
    private void tickAwaken(ServerLevel level) {
        int t = ++this.attackTick;
        Player nearest = level.getNearestPlayer(this, 32.0);
        if (nearest != null && t > 20) {
            this.getLookControl().setLookAt(nearest, 20.0f, 30.0f);
            if (t > 30) {
                this.lookAt(nearest, 10.0f, 30.0f);
            }
        }
        if (t == 4) {
            this.sound(level, ModSounds.SORDIN_AWAKEN.get(), 3.0f, 1.0f);
        }
        if (t == 52) {
            this.sound(level, ModSounds.SORDIN_FORK.get(), 2.6f, 1.0f);
            level.sendParticles(new DustParticleOptions(CRYSTAL, 1.4f), this.getX(), this.getY() + 4.6, this.getZ(), 40, 0.3, 0.6, 0.3, 0.0);
            level.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, 0xFF29DFEB), this.getX(), this.getY(0.8), this.getZ(), 1, 0.0,
                    0.0, 0.0, 0.0);
            Component title = Component.translatable("boss.kingdomsatwar.sordin").withColor(SCULK);
            Component subtitle = Component.translatable("boss.kingdomsatwar.sordin.title").withStyle(ChatFormatting.GRAY);
            for (ServerPlayer player : level.players()) {
                if (player.distanceToSqr(this) < 64.0 * 64.0) {
                    player.connection.send(new ClientboundSetTitlesAnimationPacket(15, 60, 25));
                    player.connection.send(new ClientboundSetTitleTextPacket(title));
                    player.connection.send(new ClientboundSetSubtitleTextPacket(subtitle));
                }
            }
        }
        if (t == 72) {
            this.sound(level, ModSounds.SORDIN_SWING.get(), 2.2f, 0.85f);
            this.spawnRing(level, 1.0f, 0.5f, 18, false);
        }
        if (t >= AWAKEN_TICKS) {
            this.begin(Attack.NONE);
            this.cooldown = 12;
            this.idleTicks = 0;
        }
    }

    // ------------------------------------------------------------------ Angriffe

    /** Ein Hieb in einem Bogen vor ihm; Rückstoß von ihm fort. */
    private void cleave(ServerLevel level, Vec3 dir, double range, double arc, float damage) {
        for (int i = 0; i <= 12; i++) {
            double k = i / 12.0;
            double a = (k - 0.5) * arc * 2.0;
            Vec3 p = this.position().add(dir.yRot((float) a).scale(range * (0.55 + 0.45 * Math.sin(k * Math.PI)))).add(0.0, 1.2 + k * 0.9, 0.0);
            level.sendParticles(new DustParticleOptions(CRYSTAL, 1.1f), p.x, p.y, p.z, 2, 0.05, 0.05, 0.05, 0.0);
        }
        level.sendParticles(ParticleTypes.SWEEP_ATTACK, this.getX() + dir.x * 2.0, this.getY(0.4), this.getZ() + dir.z * 2.0, 1, 0.0, 0.0, 0.0, 0.0);
        double cos = Math.cos(arc);
        for (LivingEntity victim : this.victims(level, this.getBoundingBox().inflate(range + 1.0, 2.5, range + 1.0))) {
            Vec3 to = victim.position().subtract(this.position()).multiply(1.0, 0.0, 1.0);
            double distance = to.length();
            if (distance > range + victim.getBbWidth() * 0.5 || distance > 0.8 && to.normalize().dot(dir) < cos) {
                continue;
            }
            if (this.struck.add(victim.getUUID()) && victim.hurtServer(level, this.damageSources().mobAttack(this), damage)) {
                Vec3 out = distance < 1.0e-3 ? dir : to.normalize();
                victim.push(out.x * 0.8, 0.35, out.z * 0.8);
                victim.needsSync = true;
            }
        }
    }

    /** Gabelhieb: über die rechte Schulter ausholen, dann schräg hinab. */
    private void tickSwing(ServerLevel level, LivingEntity target) {
        int t = this.attackTick;
        if (t == 6) {
            this.sound(level, ModSounds.SORDIN_SWING.get(), 1.8f, 0.95f + this.random.nextFloat() * 0.1f);
        }
        if (t == SWING_HIT) {
            this.sound(level, ModSounds.SORDIN_FORK.get(), 1.6f, 1.25f);
            this.cleave(level, this.toward(target), SWING_RANGE, 1.1, SWING_DAMAGE + (this.isFortissimo() ? 2.0f : 0.0f));
        }
    }

    /** Wo die Zinken der Gabel sind, wenn er zielt (etwa): vor ihm in Brusthöhe. */
    private Vec3 forkTip() {
        Vec3 f = this.forward();
        return this.position().add(f.scale(1.8)).add(0.0, 2.9, 0.0);
    }

    /**
     * Schallwelle: Er schlägt die Gabel an (sie klingt), zielt – eine feine Linie aus Licht zeigt, wohin – und lässt den Ton
     * los. Ab {@link #SONIC_LOCK} zielt er nicht mehr nach. Im Fortissimo drei Wellen im Fächer.
     */
    private void tickSonic(ServerLevel level, LivingEntity target) {
        int t = this.attackTick;
        if (t == 10) {
            this.sound(level, ModSounds.SORDIN_FORK.get(), 2.0f, 1.0f);
            level.sendParticles(new DustParticleOptions(CRYSTAL, 1.2f), this.getX(), this.getY() + 3.6, this.getZ(), 20, 0.3, 0.4, 0.3, 0.0);
        }
        if (t <= SONIC_LOCK) {
            Vec3 from = this.forkTip();
            Vec3 to = target.getEyePosition();
            Vec3 dir = to.subtract(from);
            this.aims.clear();
            if (dir.lengthSqr() > 1.0e-4) {
                Vec3 main = dir.normalize();
                this.aims.add(main);
                if (this.isFortissimo()) {
                    this.aims.add(main.yRot(SONIC_FAN * Mth.DEG_TO_RAD));
                    this.aims.add(main.yRot(-SONIC_FAN * Mth.DEG_TO_RAD));
                }
            }
        }
        if (t > SONIC_LOCK - 6 && t < SONIC_FIRE && t % 2 == 0) {
            // die feine Linie: wohin der Ton gleich fliegt
            Vec3 from = this.forkTip();
            for (Vec3 dir : this.aims) {
                for (double d = 1.0; d < SONIC_RANGE; d += 1.5) {
                    Vec3 p = from.add(dir.scale(d));
                    level.sendParticles(new DustParticleOptions(SCULK, 0.6f), p.x, p.y, p.z, 1, 0.0, 0.0, 0.0, 0.0);
                }
            }
        }
        if (t == SONIC_FIRE) {
            this.sound(level, ModSounds.SORDIN_SONIC.get(), 3.0f, 1.0f);
            for (Vec3 dir : this.aims) {
                this.sonicBeam(level, this.forkTip(), dir);
            }
        }
    }

    /**
     * Ein Schallschlag entlang {@code dir}: dringt durch Stein und jede Rüstung (Schaden wie der Schallschlag des
     * Tiefenhüters), nur Wolle hält ihn auf. Gibt zurück, wie weit er kam.
     */
    public double sonicBeam(ServerLevel level, Vec3 from, Vec3 dir) {
        double reach = SONIC_RANGE;
        for (double d = 0.5; d < SONIC_RANGE; d += 0.5) {
            BlockPos p = BlockPos.containing(from.add(dir.scale(d)));
            BlockState state = level.getBlockState(p);
            if (state.is(BlockTags.WOOL) || state.is(BlockTags.OCCLUDES_VIBRATION_SIGNALS)) {
                reach = d;
                Vec3 at = from.add(dir.scale(d - 0.3));
                level.sendParticles(ParticleTypes.SCULK_CHARGE_POP, at.x, at.y, at.z, 16, 0.3, 0.3, 0.3, 0.02);
                break;
            }
        }
        for (double d = 1.0; d < reach; d += 1.0) {
            Vec3 p = from.add(dir.scale(d));
            level.sendParticles(ParticleTypes.SONIC_BOOM, p.x, p.y, p.z, 1, 0.0, 0.0, 0.0, 0.0);
        }
        Vec3 end = from.add(dir.scale(reach));
        AABB area = new AABB(from, end).inflate(SONIC_WIDTH + 1.0);
        for (LivingEntity victim : this.victims(level, area)) {
            Vec3 c = victim.getBoundingBox().getCenter();
            double along = c.subtract(from).dot(dir);
            if (along < 0.0 || along > reach + 0.5) {
                continue;
            }
            double off = c.subtract(from.add(dir.scale(along))).length();
            if (off > SONIC_WIDTH + victim.getBbWidth() * 0.5 + victim.getBbHeight() * 0.25) {
                continue;
            }
            if (this.struck.add(victim.getUUID()) && victim.hurtServer(level, this.damageSources().sonicBoom(this), SONIC_DAMAGE)) {
                victim.push(dir.x * 1.2, 0.35, dir.z * 1.2);
                victim.needsSync = true;
            }
        }
        return reach;
    }

    /** Ein Ring über den Boden: Akkord ({@code harm}) oder nur zum Schein (beim Erwachen). */
    private void spawnRing(ServerLevel level, float size, float speed, int life, boolean harm) {
        SpellFxEntity.create(level, SpellFxEntity.Kind.SHOCKWAVE, this.position().add(0.0, 0.05, 0.0), null, SCULK, life, size).param(speed)
                .spawn();
        if (harm) {
            this.rings.add(new Ring(this.position(), level.getGameTime(), new HashSet<>()));
        }
    }

    /** Akkord: Er dirigiert; bei jedem Schlag ein Ton und ein Ring, der über den Boden läuft (im Fortissimo vier Schläge). */
    private void tickChord(ServerLevel level) {
        int t = this.attackTick;
        int beat = -1;
        for (int i = 0; i < CHORD_BEATS.length; i++) {
            if (t == CHORD_BEATS[i]) {
                beat = i;
            }
        }
        if (this.isFortissimo() && t == CHORD_EXTRA_BEAT) {
            beat = 3;
        }
        if (beat >= 0) {
            float[] pitch = {0.8f, 1.0f, 1.2f, 1.6f};
            this.sound(level, ModSounds.SORDIN_CHORD.get(), 2.6f, pitch[beat]);
            this.spawnRing(level, 1.0f, CHORD_SPEED, CHORD_LIFE, true);
            level.sendParticles(ParticleTypes.SCULK_CHARGE_POP, this.getX(), this.getY() + 0.2, this.getZ(), 20, 1.0, 0.1, 1.0, 0.02);
        }
    }

    /** Die Ringe laufen: Wer am Boden steht, wenn einer vorbeiläuft, wird getroffen (einmal je Ring). */
    private void tickRings(ServerLevel level) {
        long now = level.getGameTime();
        Iterator<Ring> it = this.rings.iterator();
        while (it.hasNext()) {
            Ring ring = it.next();
            long age = now - ring.start();
            if (age > CHORD_LIFE) {
                it.remove();
                continue;
            }
            double r = 1.0 + CHORD_SPEED * age;
            AABB area = new AABB(ring.center(), ring.center()).inflate(r + 1.5, 2.0, r + 1.5);
            for (LivingEntity victim : this.victims(level, area)) {
                double d = Math.sqrt(victim.distanceToSqr(ring.center().x, victim.getY(), ring.center().z));
                boolean grounded = victim.getY() - ring.center().y < SpellFxEntity.SHOCKWAVE_HEIGHT;
                if (Math.abs(d - r) < 0.7 + victim.getBbWidth() * 0.5 && grounded && ring.hit().add(victim.getUUID())
                        && victim.hurtServer(level, this.damageSources().mobAttack(this), CHORD_DAMAGE)) {
                    Vec3 out = victim.position().subtract(ring.center()).multiply(1.0, 0.0, 1.0);
                    out = out.lengthSqr() < 1.0e-4 ? this.forward() : out.normalize();
                    victim.push(out.x * 0.6, 0.3, out.z * 0.6);
                    victim.needsSync = true;
                }
            }
        }
    }

    /** Notenschwarm: Er singt; aus den Pfeifen steigen Noten (im Fortissimo mehr), die ihre Opfer suchen. */
    private void tickNotesAttack(ServerLevel level, LivingEntity target) {
        int t = this.attackTick;
        if (t == NOTE_FIRST - 2) {
            this.sound(level, ModSounds.SORDIN_NOTES.get(), 2.6f, 1.0f);
        }
        int count = this.isFortissimo() ? NOTE_COUNT_FORTISSIMO : NOTE_COUNT;
        int every = this.isFortissimo() ? 2 : 3;
        if (t >= NOTE_FIRST && (t - NOTE_FIRST) % every == 0 && (t - NOTE_FIRST) / every < count) {
            int i = (t - NOTE_FIRST) / every;
            float yaw = this.yBodyRot * Mth.DEG_TO_RAD;
            // aus einer der sieben Pfeifen (von links nach rechts), nach oben und ein wenig zur Seite
            double side = ((i % 7) - 3) * 0.32;
            Vec3 right = new Vec3(Mth.cos(yaw), 0.0, Mth.sin(yaw));
            Vec3 back = new Vec3(Mth.sin(yaw), 0.0, -Mth.cos(yaw));
            Vec3 from = this.position().add(back.scale(0.5)).add(right.scale(side)).add(0.0, 4.2 + Math.abs(3 - (i % 7)) * -0.15, 0.0);
            Vec3 velocity = right.scale(side * 0.25).add(0.0, 0.35, 0.0).add(back.scale(-0.05));
            this.notes.add(new Note(from, velocity, 0.66f + this.random.nextFloat() * 0.14f, target.getUUID()));
        }
    }

    /** Die Noten fliegen, suchen ihr Opfer (mit begrenzter Wende) und zerplatzen an Wolle, an Wänden oder am Opfer. */
    private void tickNotes(ServerLevel level) {
        Iterator<Note> it = this.notes.iterator();
        while (it.hasNext()) {
            Note note = it.next();
            note.age++;
            LivingEntity target = note.target != null && level.getEntity(note.target) instanceof LivingEntity living && living.isAlive()
                    ? living : null;
            if (target != null && note.age > 6) {
                Vec3 want = target.getBoundingBox().getCenter().subtract(note.pos);
                if (want.lengthSqr() > 1.0e-4) {
                    Vec3 desired = want.normalize().scale(NOTE_SPEED);
                    note.velocity = note.velocity.add(desired.subtract(note.velocity).scale(0.12));
                }
            } else {
                note.velocity = note.velocity.scale(0.96);
            }
            if (note.velocity.length() > NOTE_SPEED) {
                note.velocity = note.velocity.normalize().scale(NOTE_SPEED);
            }
            note.pos = note.pos.add(note.velocity);
            level.sendParticles(ParticleTypes.NOTE, note.pos.x, note.pos.y, note.pos.z, 0, note.hue, 0.0, 0.0, 1.0);
            if (note.age % 3 == 0) {
                level.sendParticles(new DustParticleOptions(SCULK, 0.5f), note.pos.x, note.pos.y, note.pos.z, 1, 0.05, 0.05, 0.05, 0.0);
            }
            BlockPos at = BlockPos.containing(note.pos);
            BlockState state = level.getBlockState(at);
            boolean blocked = state.is(BlockTags.WOOL) || state.is(BlockTags.OCCLUDES_VIBRATION_SIGNALS)
                    || note.age > 8 && !state.getCollisionShape(level, at).isEmpty();
            if (blocked || note.age > NOTE_LIFE) {
                level.sendParticles(ParticleTypes.SCULK_CHARGE_POP, note.pos.x, note.pos.y, note.pos.z, 3, 0.1, 0.1, 0.1, 0.01);
                it.remove();
                continue;
            }
            for (LivingEntity victim : this.victims(level, new AABB(note.pos, note.pos).inflate(0.8))) {
                if (victim.getBoundingBox().inflate(0.35).contains(note.pos)) {
                    victim.hurtServer(level, this.damageSources().indirectMagic(this, this), NOTE_DAMAGE);
                    level.sendParticles(ParticleTypes.SCULK_CHARGE_POP, note.pos.x, note.pos.y, note.pos.z, 6, 0.15, 0.15, 0.15, 0.02);
                    it.remove();
                    break;
                }
            }
        }
    }

    /**
     * Stille: Er legt den Finger an den Mund – Dunkelheit fällt über alle in der Nähe –, verschwindet, und taucht hinter seinem
     * Opfer wieder auf, die Gabel schon erhoben.
     */
    private void tickSilence(ServerLevel level, LivingEntity target) {
        int t = this.attackTick;
        if (t == 8) {
            this.sound(level, ModSounds.SORDIN_HUSH.get(), 2.6f, 1.0f);
            for (ServerPlayer player : level.players()) {
                if (this.canStrike(player) && player.distanceToSqr(this) < 24.0 * 24.0) {
                    player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, SILENCE_DARKNESS, 0), this);
                }
            }
        }
        if (t == SILENCE_VANISH) {
            level.sendParticles(ParticleTypes.SCULK_SOUL, this.getX(), this.getY(0.5), this.getZ(), 40, 0.5, 1.4, 0.5, 0.05);
            this.setInvisible(true);
            this.getNavigation().stop();
        }
        if (t > SILENCE_VANISH && t < SILENCE_APPEAR - 4 && t % 10 == 0) {
            // er gleitet unsichtbar heran (nur ein paar Sculk-Seelen verraten ihn)
            this.getNavigation().moveTo(target, 1.3);
        }
        if (t == SILENCE_APPEAR - 4) {
            Vec3 behind = target.position().subtract(this.toward(target).scale(2.6));
            if (this.canStand(level, behind)) {
                this.teleportTo(behind.x, target.getY(), behind.z);
            }
            this.getNavigation().stop();
            this.lookAt(target, 360.0f, 30.0f);
        }
        if (t == SILENCE_APPEAR) {
            this.reveal(level);
            this.lookAt(target, 360.0f, 30.0f);
            this.sound(level, ModSounds.SORDIN_SWING.get(), 2.0f, 0.8f);
        }
        if (t == SILENCE_HIT) {
            this.sound(level, ModSounds.SORDIN_FORK.get(), 1.8f, 1.3f);
            this.cleave(level, this.toward(target), SWING_RANGE, 1.0, SILENCE_DAMAGE);
        }
    }

    private boolean canStand(ServerLevel level, Vec3 at) {
        AABB box = this.getDimensions(this.getPose()).makeBoundingBox(at);
        BlockPos below = BlockPos.containing(at.x, at.y - 0.5, at.z);
        return level.noCollision(this, box) && !level.getBlockState(below).getCollisionShape(level, below).isEmpty();
    }

    /** Er wird wieder sichtbar (am Ende der Stille oder wenn ihn etwas trifft). */
    private void reveal(ServerLevel level) {
        this.setInvisible(false);
        level.sendParticles(ParticleTypes.SCULK_SOUL, this.getX(), this.getY(0.5), this.getZ(), 30, 0.5, 1.4, 0.5, 0.05);
        level.sendParticles(new DustParticleOptions(SCULK, 1.2f), this.getX(), this.getY(0.6), this.getZ(), 30, 0.5, 1.2, 0.5, 0.0);
    }

    /**
     * Fortissimo (unter halber Kraft): Er reckt die Arme, alle Pfeifen holen Luft – und lassen zugleich einen Ton los, der
     * alle fortwirft. Danach bleibt er im Fortissimo.
     */
    private void tickFortissimo(ServerLevel level) {
        int t = ++this.attackTick;
        this.getNavigation().stop();
        if (t == 6) {
            this.sound(level, ModSounds.SORDIN_FORTISSIMO.get(), 3.5f, 1.0f);
        }
        if (t > 10 && t < FORTISSIMO_BLAST && t % 4 == 0) {
            level.sendParticles(new DustParticleOptions(SCULK, 1.0f), this.getX(), this.getY() + 3.8, this.getZ(), 8, 0.8, 0.6, 0.8, 0.0);
        }
        if (t == FORTISSIMO_BLAST) {
            this.entityData.set(DATA_FORTISSIMO, true);
            this.spawnRing(level, 1.0f, 0.75f, 16, false);
            level.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, 0xFF29DFEB), this.getX(), this.getY(0.8), this.getZ(), 1, 0.0, 0.0,
                    0.0, 0.0);
            for (int i = 0; i < 16; i++) {
                double a = i * Math.PI / 8.0;
                level.sendParticles(ParticleTypes.SONIC_BOOM, this.getX() + Math.cos(a) * 3.0, this.getY() + 1.2, this.getZ() + Math.sin(a) * 3.0, 1,
                        0.0, 0.0, 0.0, 0.0);
            }
            for (LivingEntity victim : this.victims(level, this.getBoundingBox().inflate(FORTISSIMO_RADIUS, 4.0, FORTISSIMO_RADIUS))) {
                if (victim.distanceToSqr(this) > FORTISSIMO_RADIUS * FORTISSIMO_RADIUS) {
                    continue;
                }
                victim.hurtServer(level, this.damageSources().sonicBoom(this), FORTISSIMO_DAMAGE);
                Vec3 out = victim.position().subtract(this.position()).multiply(1.0, 0.0, 1.0);
                out = out.lengthSqr() < 1.0e-4 ? this.forward() : out.normalize();
                victim.push(out.x * 1.6, 0.55, out.z * 1.6);
                victim.needsSync = true;
            }
        }
        if (t >= Attack.FORTISSIMO.duration) {
            this.begin(Attack.NONE);
            this.cooldown = 8;
        }
    }

    /** Jemand spielt auf der Orgel: Er hält sich die Ohren zu und taumelt. */
    private void tickStagger(ServerLevel level) {
        int t = ++this.attackTick;
        this.getNavigation().stop();
        this.setDeltaMovement(0.0, this.getDeltaMovement().y, 0.0);
        if (t % 6 == 0) {
            level.sendParticles(ParticleTypes.NOTE, this.getX() + (this.random.nextDouble() - 0.5) * 1.6, this.getY() + 3.6,
                    this.getZ() + (this.random.nextDouble() - 0.5) * 1.6, 0, this.random.nextDouble(), 0.0, 0.0, 1.0);
        }
        if (t >= STAGGER_TICKS) {
            this.begin(Attack.NONE);
            this.cooldown = 10;
        }
    }

    // ------------------------------------------------------------------ Tod, Speichern, Klänge

    @Override
    public void die(DamageSource source) {
        super.die(source);
        this.notes.clear();
        this.rings.clear();
        if (this.level() instanceof ServerLevel level && this.desk != null
                && level.getBlockEntity(this.desk) instanceof OrganDeskBlockEntity organ && organ.owns(this)) {
            organ.onCantorDefeated(level);
        }
    }

    /** Er sinkt auf die Knie, die Pfeifen verstummen – dann zerfällt er zu Sculk, und die Stille nimmt ihn zurück. */
    @Override
    protected void tickDeath() {
        this.deathTime++;
        if (!(this.level() instanceof ServerLevel level)) {
            return;
        }
        if (this.deathTime % 4 == 0) {
            level.sendParticles(ParticleTypes.SCULK_SOUL, this.getX(), this.getY(0.5), this.getZ(), 6, 0.5, 1.2, 0.5, 0.02);
            level.sendParticles(new DustParticleOptions(SCULK, 1.0f), this.getX(), this.getY(0.6), this.getZ(), 8, 0.5, 1.2, 0.5, 0.0);
        }
        if (this.deathTime == DEATH_TICKS - 2) {
            level.playSound(null, this.getX(), this.getY(), this.getZ(), ModSounds.SORDIN_HUSH.get(), SoundSource.HOSTILE, 3.0f, 0.6f);
            level.sendParticles(ParticleTypes.SCULK_SOUL, this.getX(), this.getY(0.5), this.getZ(), 120, 0.8, 1.6, 0.8, 0.08);
            level.sendParticles(ParticleTypes.SCULK_CHARGE_POP, this.getX(), this.getY(0.5), this.getZ(), 80, 1.0, 1.6, 1.0, 0.05);
            level.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, 0xFF29DFEB), this.getX(), this.getY(0.5), this.getZ(), 1, 0.0, 0.0,
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
        if (this.desk != null) {
            output.putLong("Desk", this.desk.asLong());
        }
        output.putBoolean("Fortissimo", this.isFortissimo());
        output.putBoolean("Playing", this.attack == Attack.GUARD);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        input.getLong("Home").ifPresent(h -> this.home = BlockPos.of(h));
        input.getLong("Desk").ifPresent(d -> this.desk = BlockPos.of(d));
        this.entityData.set(DATA_FORTISSIMO, input.getBooleanOr("Fortissimo", false));
        if (input.getBooleanOr("Playing", false)) {
            this.begin(Attack.GUARD);
        }
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return this.attack == Attack.GUARD || this.isInvisible() ? null : ModSounds.SORDIN_AMBIENT.get();
    }

    @Override
    public int getAmbientSoundInterval() {
        return 160;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.SORDIN_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.SORDIN_DEATH.get();
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        if (!this.isInvisible()) {
            this.playSound(ModSounds.SORDIN_STEP.get(), 0.8f, 0.9f + this.random.nextFloat() * 0.2f);
        }
    }

    @Override
    protected float getSoundVolume() {
        return 2.2f;
    }

    // ------------------------------------------------------------------ Himmelswacht

    @Override
    public SkyTheme skyTheme() {
        return SkyTheme.ECHO;
    }
}
