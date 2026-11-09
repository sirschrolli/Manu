package io.github.sirschrolli.kingdomsatwar.entity.glitch;

import io.github.sirschrolli.kingdomsatwar.block.glitch.ErrorCoreBlockEntity;
import io.github.sirschrolli.kingdomsatwar.magic.fx.SpellFxEntity;
import io.github.sirschrolli.kingdomsatwar.registry.ModParticles;
import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import java.util.ArrayList;
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
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
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
 * Anni der Troll: eine riesige Trollfrau, die in einer Fehlerzone der Oberwelt haust – dort, wo die Welt kaputtgegangen
 * ist, und Anni mit ihr. Sie glitcht wie ein Spielfehler: Sie flackert und ruckelt, hängt mitten in der Bewegung fest und
 * holt dann ruckartig auf, springt durch den Raum, zerfällt in Farben und läuft, wenn sie feststeckt, einfach durch Wände.
 * Manche Treffer werden nicht registriert. Sie kämpft mit ihrem Bannhammer und mit Fehlern:
 * <ul>
 *     <li><b>Lag-Hieb</b> – sie holt aus und hängt fest (über ihr dreht sich ein Ladekreis); wenn er verschwindet, kracht
 *     der Hammer herab, eine Schockwelle läuft über den Boden (springen!);</li>
 *     <li><b>Gummiband</b> – sie springt flackernd drei-, viermal um ihr Ziel herum und schlägt dann neben ihm zu;</li>
 *     <li><b>Fehler-Klone</b> – sie verdoppelt sich: rote, grüne und blaue Kopien rennen los und stürzen ab (ein Treffer lässt
 *     sie zerplatzen);</li>
 *     <li><b>Fehlende Textur</b> – unter ihrem Ziel wird der Boden zu Magenta und Schwarz: Wer darin steht, hängt fest und
 *     nimmt Schaden;</li>
 *     <li><b>Pixelregen</b> – leuchtende Pixel stürzen vom Himmel auf markierte Stellen;</li>
 *     <li><b>Rückruck</b> – sie merkt sich, wo alle stehen, und reißt sie drei Sekunden später dorthin zurück – dann stürmt sie
 *     los;</li>
 *     <li><b>Zeilenfehler</b> – eine leuchtende Zeile kreist um sie: die rote dicht über dem Boden (springen!), die blaue in
 *     Kopfhöhe (ducken!);</li>
 *     <li><b>Absturz</b> (einmal, unter halber Kraft) – „Anni.exe reagiert nicht.“: Sie erstarrt, dann startet sie neu – eine
 *     Welle aus Pixeln rollt über den Boden – und kämpft im <b>Fehlermodus</b> weiter: schneller, härter, mehr Klone.</li>
 * </ul>
 * Findet sie niemanden mehr, zerfällt sie nach einer halben Minute in Pixel und kehrt in ihren Fehlerkern zurück. Besiegt
 * stürzt sie endgültig ab und zerfällt in Pixel; der Kern erlischt (ein Redstoneblock startet ihn neu).
 */
public class AnniTrollEntity extends Monster {
    public static final float MAX_HEALTH = 1500.0f;
    public static final int SPAWN_TICKS = 90;
    /** In diesem Tick des Ladens setzt sie sich zusammen (davor fliegen nur Pixel). */
    public static final int SPAWN_ASSEMBLE = 46;
    public static final int DEATH_TICKS = 80;
    public static final int SMASH_RAISE = 12;
    public static final int RUBBER_HOPS = 3;
    public static final int RUBBER_FIRST = 6;
    public static final int RUBBER_STEP = 8;
    public static final int CLONES_SPLIT = 14;
    public static final int MISSING_SLAM = 16;
    public static final int RAIN_START = 18;
    public static final int RAIN_END = 66;
    public static final int ROLLBACK_MARK = 10;
    public static final int ROLLBACK_SNAP = 50;
    public static final int SCAN_LOW = 12;
    public static final int SCAN_HIGH = 36;
    public static final int CRASH_RESTART = 70;
    public static final int CRASH_WAVE = 80;
    private static final double LEASH = 40.0;
    private static final int RETURN_TICKS = 600;

    public enum Attack {
        NONE(0),
        /** Laden: Pixel fliegen zusammen, „Lädt …“, dann setzt sie sich zusammen und lacht. */
        SPAWN(SPAWN_TICKS),
        /** Lag-Hieb (die Dauer hängt davon ab, wie lange sie hängt). */
        SMASH(48),
        RUBBERBAND(56),
        CLONES(44),
        MISSING(40),
        PIXEL_RAIN(80),
        ROLLBACK(70),
        SCANLINE(96),
        /** Absturz und Neustart (einmal, unter halber Kraft). */
        CRASH(110),
        /** Troll-Lachen: eine kurze Pause zwischen den Angriffen. */
        LAUGH(30),
        /** Verbindung getrennt: Sie zerfällt in Pixel und kehrt in den Kern zurück. */
        EXIT(46);

        public final int duration;

        Attack(int duration) {
            this.duration = duration;
        }
    }

    private static final EntityDataAccessor<Integer> DATA_ATTACK = SynchedEntityData.defineId(AnniTrollEntity.class, EntityDataSerializers.INT);
    /** Hängt sie gerade (Ladekreis über dem Kopf)? */
    private static final EntityDataAccessor<Boolean> DATA_LAG = SynchedEntityData.defineId(AnniTrollEntity.class, EntityDataSerializers.BOOLEAN);
    /** Fehlermodus (nach dem Absturz): stärkere Glitches, schneller, härter. */
    private static final EntityDataAccessor<Boolean> DATA_GLITCH = SynchedEntityData.defineId(AnniTrollEntity.class,
            EntityDataSerializers.BOOLEAN);
    /** Kurz nicht zu sehen (beim Springen und vor dem Zusammensetzen). */
    private static final EntityDataAccessor<Boolean> DATA_HIDDEN = SynchedEntityData.defineId(AnniTrollEntity.class,
            EntityDataSerializers.BOOLEAN);
    static final ParticleOptions MAGENTA = new DustParticleOptions(0xF800F8, 1.4f);
    static final ParticleOptions CYAN = new DustParticleOptions(0x20E8FF, 1.4f);
    static final ParticleOptions SMOKE = new DustParticleOptions(0x1A1420, 2.4f);

    private final ServerBossEvent bossBar = new ServerBossEvent(UUID.randomUUID(), Component.translatable("boss.kingdomsatwar.anni"),
            BossEvent.BossBarColor.PINK, BossEvent.BossBarOverlay.NOTCHED_12);
    private Attack attack = Attack.NONE;
    private Attack lastAttack = Attack.NONE;
    private int attackTick;
    private int cooldown = 30;
    private int idleTicks;
    private boolean crashed;
    private @Nullable BlockPos home;
    // Lag-Hieb: wann sie aufhört zu hängen und wann der Hammer trifft
    private int lagUntil;
    private int slamAt;
    // Gummiband: wohin sie springt, zuletzt neben das Ziel
    private int hops;
    private Vec3 lunge = Vec3.ZERO;
    // Rückruck: wo die Spieler standen
    private final Map<UUID, Vec3> rollback = new HashMap<>();
    private @Nullable UUID charge;
    // Absturz: die Welle aus Pixeln (Halbmesser je Tick)
    private double wave = -1.0;
    private final Set<UUID> struck = new HashSet<>();
    // nebenbei: Ruckler beim Gehen, Feststecken und durch Wände laufen
    private int nextStutter = 160;
    private int stutter;
    private Vec3 lastSpot = Vec3.ZERO;
    private int stuckTicks;
    private int clipTicks;
    private int clientAttackStart;

    public AnniTrollEntity(EntityType<? extends AnniTrollEntity> type, Level level) {
        super(type, level);
        this.xpReward = 600;
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, MAX_HEALTH)
                .add(Attributes.ARMOR, 16.0)
                .add(Attributes.ARMOR_TOUGHNESS, 6.0)
                .add(Attributes.ATTACK_DAMAGE, 16.0)
                .add(Attributes.MOVEMENT_SPEED, 0.28)
                .add(Attributes.FOLLOW_RANGE, 48.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(Attributes.STEP_HEIGHT, 1.6);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ATTACK, 0);
        builder.define(DATA_LAG, false);
        builder.define(DATA_GLITCH, false);
        builder.define(DATA_HIDDEN, false);
    }

    // ------------------------------------------------------------------ Zustand

    public Attack currentAttack() {
        int i = this.entityData.get(DATA_ATTACK);
        return i >= 0 && i < Attack.values().length ? Attack.values()[i] : Attack.NONE;
    }

    /** Hängt sie gerade fest (Ladekreis)? */
    public boolean isLagging() {
        return this.entityData.get(DATA_LAG);
    }

    private void setLagging(boolean lag) {
        this.entityData.set(DATA_LAG, lag);
    }

    /** Fehlermodus (nach dem Absturz). */
    public boolean isGlitchMode() {
        return this.entityData.get(DATA_GLITCH);
    }

    public boolean isHidden() {
        return this.entityData.get(DATA_HIDDEN);
    }

    private void setHidden(boolean hidden) {
        this.entityData.set(DATA_HIDDEN, hidden);
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

    public @Nullable BlockPos home() {
        return this.home;
    }

    /** Bindet sie an ihren Fehlerkern (dort wird sie geladen, dorthin kehrt sie zurück). */
    public void bindTo(BlockPos core) {
        this.home = core.immutable();
    }

    /** Sie wird geladen: Pixel fliegen zusammen, dann setzt sie sich zusammen (bis dahin unverwundbar). */
    public void load() {
        this.setHidden(true);
        this.begin(Attack.SPAWN);
    }

    /** Beginnt sofort einen bestimmten Angriff (für Tests und die Vorführung). */
    public void forceAttack(Attack next) {
        this.setLagging(false);
        this.begin(next);
    }

    /** Sofort im Fehlermodus (für Tests und die Vorführung). */
    public void forceGlitchMode() {
        this.crashed = true;
        this.entityData.set(DATA_GLITCH, true);
    }

    private void begin(Attack next) {
        this.attack = next;
        this.attackTick = 0;
        this.entityData.set(DATA_ATTACK, next.ordinal());
        if (next != Attack.NONE && next != Attack.SPAWN && next != Attack.CRASH && next != Attack.EXIT && next != Attack.LAUGH) {
            this.lastAttack = next;
        }
        if (this.isHidden() && next != Attack.SPAWN && next != Attack.EXIT) {
            this.setHidden(false);
        }
        if (this.isLagging() && next != Attack.SMASH) {
            this.setLagging(false);
        }
        this.struck.clear();
        this.hops = 0;
        this.lagUntil = 0;
        this.slamAt = 0;
        this.wave = -1.0;
        this.charge = null;
        if (next != Attack.ROLLBACK) {
            this.rollback.clear();
        }
    }

    // ------------------------------------------------------------------ Grundverhalten

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
     * Beim Laden, Abstürzen (bis zum Neustart) und Gehen trifft sie nichts; manche Geschosse gehen einfach durch sie
     * hindurch („Treffer nicht registriert“). Ein Hieb lässt sie manchmal ein Stück zur Seite springen.
     */
    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return super.hurtServer(level, source, amount);
        }
        if (this.attack == Attack.SPAWN || this.attack == Attack.EXIT || this.isHidden() || source.is(DamageTypes.IN_WALL)
                || source.is(DamageTypes.FALL) || source.is(DamageTypes.FLY_INTO_WALL) || source.is(DamageTypes.CRAMMING)) {
            return false;
        }
        if (this.attack == Attack.CRASH && this.attackTick < CRASH_RESTART) {
            this.notRegistered(level, source.getSourcePosition() != null ? source.getSourcePosition() : this.position().add(0, 2, 0));
            return false;
        }
        if (source.getDirectEntity() instanceof Projectile shot && this.random.nextFloat() < (this.isGlitchMode() ? 0.35f : 0.25f)) {
            this.notRegistered(level, shot.position());
            return false;
        }
        boolean hurt = super.hurtServer(level, source, this.isGlitchMode() ? amount * 0.9f : amount);
        if (hurt && source.getEntity() instanceof LivingEntity attacker && attacker != this && this.canStrike(attacker)) {
            if (!(this.getTarget() instanceof Player)) {
                this.setTarget(attacker);
            }
            if (source.getDirectEntity() == attacker && this.attack == Attack.NONE && this.random.nextFloat() < 0.14f) {
                this.sidestep(level, attacker);
            }
        }
        return hurt;
    }

    /** „Treffer nicht registriert“: ein Pixelwölkchen, wo der Treffer hätte sitzen sollen. */
    private void notRegistered(ServerLevel level, Vec3 at) {
        level.sendParticles(ModParticles.GLITCH_PIXEL.get(), at.x, at.y, at.z, 10, 0.25, 0.25, 0.25, 0.08);
        this.sound(level, ModSounds.ANNI_MISS.get(), 1.2f, 0.9f + this.random.nextFloat() * 0.3f);
    }

    /** Sie springt drei, vier Blöcke zur Seite (ein Nachbild bleibt stehen). */
    private void sidestep(ServerLevel level, LivingEntity from) {
        Vec3 away = this.position().subtract(from.position()).multiply(1.0, 0.0, 1.0);
        away = away.lengthSqr() < 1.0e-4 ? this.forward().scale(-1.0) : away.normalize();
        Vec3 side = new Vec3(-away.z, 0.0, away.x).scale(this.random.nextBoolean() ? 1.0 : -1.0);
        this.blinkTo(level, this.position().add(side.scale(3.5)).add(away.scale(1.5)));
    }

    @Override
    public void tick() {
        // durch Wände laufen, solange sie „hindurchclippt“
        this.noPhysics = this.clipTicks > 0;
        super.tick();
        if (this.level().isClientSide()) {
            this.clientEffects();
        }
    }

    /** Pixel lösen sich von ihr; im Fehlermodus mehr, beim Hängen flimmern Farbsäume um sie. */
    private void clientEffects() {
        if (this.isHidden()) {
            return;
        }
        Level level = this.level();
        int chance = this.isGlitchMode() ? 2 : 4;
        if (this.random.nextInt(chance) == 0) {
            level.addParticle(ModParticles.GLITCH_PIXEL.get(), this.getRandomX(1.0), this.getY() + this.random.nextDouble() * 3.6,
                    this.getRandomZ(1.0), 0.0, 0.02, 0.0);
        }
        if (this.isLagging() && this.random.nextInt(2) == 0) {
            level.addParticle(this.random.nextBoolean() ? MAGENTA : CYAN, this.getRandomX(1.1), this.getY() + this.random.nextDouble() * 3.4,
                    this.getRandomZ(1.1), 0.0, 0.0, 0.0);
        }
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        this.bossBar.setProgress(this.getHealth() / this.getMaxHealth());
        if (this.clipTicks > 0 && --this.clipTicks == 0) {
            this.endClip(level);
        }
        if (this.tickCount % 40 == 0 && this.strayed(level)) {
            return;
        }
        if (this.attack == Attack.SPAWN) {
            this.tickSpawn(level);
            return;
        }
        if (this.attack == Attack.EXIT) {
            this.tickExit(level);
            return;
        }
        if (!this.crashed && this.getHealth() < this.getMaxHealth() * 0.5f && this.attack != Attack.SMASH) {
            this.crashed = true;
            this.getNavigation().stop();
            this.begin(Attack.CRASH);
        }
        if (this.attack == Attack.CRASH) {
            this.attackTick++;
            this.tickCrash(level);
            if (this.attackTick >= Attack.CRASH.duration) {
                this.begin(Attack.NONE);
                this.cooldown = 16;
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
            this.chase(level, target);
            if (--this.cooldown <= 0 && this.stutter <= 0 && this.clipTicks == 0) {
                this.getNavigation().stop();
                this.begin(this.chooseAttack(level, target));
            }
            return;
        }
        this.attackTick++;
        this.getNavigation().stop();
        switch (this.attack) {
            case SMASH -> this.tickSmash(level, target);
            case RUBBERBAND -> this.tickRubberband(level, target);
            case CLONES -> this.tickClones(level, target);
            case MISSING -> this.tickMissing(level, target);
            case PIXEL_RAIN -> this.tickPixelRain(level, target);
            case ROLLBACK -> this.tickRollback(level, target);
            case SCANLINE -> this.tickScanline(level, target);
            case LAUGH -> this.tickLaugh(level, target);
            default -> { }
        }
        boolean done = this.attack == Attack.SMASH ? this.slamAt > 0 && this.attackTick >= this.slamAt + 14
                : this.attackTick >= this.attack.duration;
        if (done) {
            this.begin(Attack.NONE);
            this.cooldown = this.isGlitchMode() ? 10 + this.random.nextInt(12) : 22 + this.random.nextInt(18);
        }
    }

    /**
     * Gehört sie (noch) zu ihrem Kern? Ist dort inzwischen eine andere Anni gebunden (etwa nach dem Neuladen der Welt),
     * vergeht diese hier; ist der Kern fort, bleibt sie frei.
     */
    private boolean strayed(ServerLevel level) {
        if (this.home == null || !level.isLoaded(this.home)) {
            return false;
        }
        if (level.getBlockEntity(this.home) instanceof ErrorCoreBlockEntity core) {
            if (!core.owns(this)) {
                this.pixelBurst(level, this.position().add(0.0, 2.0, 0.0), 60, 1.2);
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
     * keine Monster (außer sie greifen sie an), nicht sich selbst, nicht ihre Klone, keine Rüstungsständer.
     */
    boolean canStrike(LivingEntity entity) {
        if (entity == this || !entity.isAlive() || entity instanceof AnniTrollEntity || entity instanceof GlitchCloneEntity
                || entity instanceof ArmorStand) {
            return false;
        }
        if (entity instanceof Player player) {
            return !player.isCreative() && !player.isSpectator();
        }
        return !(entity instanceof Enemy) || entity == this.getTarget();
    }

    List<LivingEntity> victims(ServerLevel level, AABB area) {
        return level.getEntitiesOfClass(LivingEntity.class, area, this::canStrike);
    }

    private @Nullable LivingEntity findTarget(ServerLevel level) {
        if (this.home != null && this.distanceToSqr(Vec3.atCenterOf(this.home)) > LEASH * LEASH) {
            return null;
        }
        LivingEntity current = this.getTarget();
        if (current != null && current.isAlive() && current.distanceToSqr(this) < 48.0 * 48.0 && this.canStrike(current)
                && (this.home == null || current.distanceToSqr(Vec3.atCenterOf(this.home)) < (LEASH + 8.0) * (LEASH + 8.0))) {
            return current;
        }
        return level.getNearestPlayer(this.getX(), this.getY(), this.getZ(), 36.0,
                e -> e instanceof Player p && !p.isCreative() && !p.isSpectator() && p.isAlive());
    }

    /**
     * Niemand da: zurück zum Kern, erholen – und nach einer halben Minute „Verbindung getrennt“: Sie zerfällt in Pixel und
     * kehrt in den Kern zurück. Ist sie zu weit fortgelaufen, ruckt sie zurück.
     */
    private void idle(ServerLevel level) {
        if (this.attack != Attack.NONE) {
            this.begin(Attack.NONE);
        }
        if (this.clipTicks > 1) {
            this.clipTicks = 1;
        }
        this.idleTicks++;
        if (this.home != null) {
            Vec3 core = Vec3.atBottomCenterOf(this.home);
            if (this.distanceToSqr(core) > LEASH * LEASH) {
                this.blinkTo(level, this.standOn(core.add(3.0, 0.0, 0.0)));
            } else if (this.distanceToSqr(core) > 36.0 && this.tickCount % 20 == 0) {
                this.getNavigation().moveTo(core.x, core.y, core.z, 0.8);
            }
        }
        if (this.idleTicks > 200 && this.tickCount % 20 == 0) {
            this.heal(8.0f);
        }
        if (this.home != null && this.idleTicks > RETURN_TICKS) {
            this.begin(Attack.EXIT);
        }
    }

    /**
     * Auf ihr Ziel zu – stampfend, mit Rucklern: Alle paar Sekunden hängt sie fest (Ladekreis) und ruckt dann ein Stück
     * vorwärts. Kommt sie nicht weiter, läuft sie durch die Wand.
     */
    private void chase(ServerLevel level, LivingEntity target) {
        this.getLookControl().setLookAt(target, 30.0f, 30.0f);
        if (this.stutter > 0) {
            this.getNavigation().stop();
            if (--this.stutter == 0) {
                this.setLagging(false);
                Vec3 to = target.position().subtract(this.position()).multiply(1.0, 0.0, 1.0);
                if (to.length() > 4.0) {
                    this.blinkTo(level, this.position().add(to.normalize().scale(Math.min(3.5, to.length() - 3.0))));
                }
            }
            return;
        }
        if (--this.nextStutter <= 0) {
            this.nextStutter = (this.isGlitchMode() ? 70 : 140) + this.random.nextInt(80);
            this.stutter = 6 + this.random.nextInt(6);
            this.setLagging(true);
            this.sound(level, ModSounds.ANNI_LAG.get(), 1.4f, 1.0f);
            return;
        }
        double distance = this.distanceTo(target);
        if (distance > 3.6) {
            this.getNavigation().moveTo(target, this.isGlitchMode() ? 1.25 : 1.0);
        } else {
            this.getNavigation().stop();
        }
        // steckt sie fest? dann läuft sie durch die Wand
        if (this.tickCount % 20 == 0) {
            if (distance > 5.0 && this.position().distanceToSqr(this.lastSpot) < 1.0 && this.clipTicks == 0) {
                this.stuckTicks += 20;
            } else {
                this.stuckTicks = 0;
            }
            this.lastSpot = this.position();
            if (this.stuckTicks >= 60) {
                this.stuckTicks = 0;
                this.startClip(level, target);
            }
        }
        if (this.clipTicks > 0) {
            this.getNavigation().stop();
            Vec3 to = target.position().subtract(this.position());
            this.setDeltaMovement(to.normalize().scale(0.3));
            if (to.length() < 3.0 && this.clipTicks > 1) {
                this.clipTicks = 1;
            }
        }
    }

    /** Durch Wände: zwei Sekunden lang geht sie geradewegs auf ihr Ziel zu, durch alles hindurch. */
    private void startClip(ServerLevel level, LivingEntity target) {
        this.clipTicks = 40;
        this.getNavigation().stop();
        this.sound(level, ModSounds.ANNI_TELEPORT.get(), 1.5f, 0.6f);
        this.pixelBurst(level, this.position().add(0.0, 2.0, 0.0), 24, 1.0);
    }

    /** Ende des Durchclippens: steckt sie noch in Blöcken, schiebt es sie nach oben heraus (oder sie clippt noch ein Stück). */
    private void endClip(ServerLevel level) {
        this.noPhysics = false;
        if (level.noCollision(this)) {
            return;
        }
        for (int i = 1; i <= 12; i++) {
            if (level.noCollision(this, this.getBoundingBox().move(0, i, 0))) {
                this.teleportTo(this.getX(), this.getY() + i, this.getZ());
                return;
            }
        }
        this.clipTicks = 10;
    }

    private Attack chooseAttack(ServerLevel level, LivingEntity target) {
        double distance = this.distanceTo(target);
        List<Attack> options = new ArrayList<>();
        if (distance < 7.0) {
            options.add(Attack.SMASH);
            options.add(Attack.SMASH);
            options.add(Attack.SCANLINE);
        } else {
            options.add(Attack.RUBBERBAND);
            options.add(Attack.RUBBERBAND);
            options.add(Attack.PIXEL_RAIN);
        }
        options.add(Attack.MISSING);
        options.add(Attack.CLONES);
        options.add(Attack.PIXEL_RAIN);
        options.add(Attack.SCANLINE);
        if (level.getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(24.0), p -> !p.isSpectator() && !p.isCreative()).size() > 0) {
            options.add(Attack.ROLLBACK);
        }
        if (!this.isGlitchMode() && this.random.nextInt(4) == 0) {
            options.add(Attack.LAUGH);
        }
        if (level.getEntitiesOfClass(GlitchCloneEntity.class, this.getBoundingBox().inflate(40.0)).size() > 2) {
            options.removeIf(a -> a == Attack.CLONES);
        }
        options.removeIf(a -> a == this.lastAttack);
        return options.get(this.random.nextInt(options.size()));
    }

    // ------------------------------------------------------------------ Laden und Gehen

    /**
     * Laden: Pixel fliegen von allen Seiten herbei und kreisen um den Kern, „Lädt …“ – dann setzt sie sich mit einem Ruck
     * zusammen, flackert, reckt den Hammer und lacht. Der Titel erscheint.
     */
    private void tickSpawn(ServerLevel level) {
        int t = ++this.attackTick;
        this.getNavigation().stop();
        Vec3 c = this.position().add(0.0, 2.0, 0.0);
        if (t == 1) {
            this.sound(level, ModSounds.ANNI_BOOT.get(), 4.0f, 1.0f);
            for (ServerPlayer player : level.players()) {
                if (player.distanceToSqr(this) < 48.0 * 48.0) {
                    player.sendOverlayMessage(Component.translatable("entity.kingdomsatwar.anni_troll.loading").withColor(0x20E8FF));
                }
            }
        }
        if (t < SPAWN_ASSEMBLE && t % 2 == 0) {
            // Pixel strömen herbei
            for (int i = 0; i < 6; i++) {
                double a = this.random.nextDouble() * Math.PI * 2.0;
                double r = 6.0 + this.random.nextDouble() * 6.0;
                double y = this.random.nextDouble() * 5.0 - 1.0;
                Vec3 from = c.add(Math.cos(a) * r, y, Math.sin(a) * r);
                Vec3 v = c.subtract(from).scale(0.09);
                level.sendParticles(ModParticles.GLITCH_PIXEL.get(), from.x, from.y, from.z, 0, v.x, v.y, v.z, 1.0);
            }
        }
        if (t == SPAWN_ASSEMBLE) {
            this.setHidden(false);
            this.sound(level, ModSounds.ANNI_TELEPORT.get(), 3.0f, 0.7f);
            level.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, 0xFFF800F8), c.x, c.y, c.z, 1, 0.0, 0.0, 0.0, 0.0);
            this.pixelBurst(level, c, 90, 1.4);
        }
        if (t == SPAWN_ASSEMBLE + 18) {
            this.sound(level, ModSounds.ANNI_LAUGH.get(), 4.0f, 1.0f);
            Component title = Component.translatable("boss.kingdomsatwar.anni").withColor(0xF800F8);
            Component subtitle = Component.translatable("boss.kingdomsatwar.anni.title").withStyle(ChatFormatting.GRAY);
            for (ServerPlayer player : level.players()) {
                if (player.distanceToSqr(this) < 64.0 * 64.0) {
                    player.connection.send(new ClientboundSetTitlesAnimationPacket(8, 60, 20));
                    player.connection.send(new ClientboundSetTitleTextPacket(title));
                    player.connection.send(new ClientboundSetSubtitleTextPacket(subtitle));
                }
            }
        }
        Player nearest = level.getNearestPlayer(this, 40.0);
        if (nearest != null && t > SPAWN_ASSEMBLE) {
            this.getLookControl().setLookAt(nearest, 20.0f, 20.0f);
            this.turnTo((float) (Mth.atan2(nearest.getZ() - this.getZ(), nearest.getX() - this.getX()) * Mth.RAD_TO_DEG) - 90.0f, 8.0f);
        }
        if (t >= SPAWN_TICKS) {
            this.begin(Attack.NONE);
            this.cooldown = 20;
            this.idleTicks = 0;
        }
    }

    /** Verbindung getrennt: Sie flackert, zerfällt Stück für Stück in Pixel und ist fort; der Kern wartet wieder. */
    private void tickExit(ServerLevel level) {
        int t = ++this.attackTick;
        this.getNavigation().stop();
        if (t == 1) {
            this.sound(level, ModSounds.ANNI_DISCONNECT.get(), 3.0f, 1.0f);
            for (ServerPlayer player : level.players()) {
                if (player.distanceToSqr(this) < 48.0 * 48.0) {
                    player.sendOverlayMessage(Component.translatable("entity.kingdomsatwar.anni_troll.disconnect").withColor(0xA0A0A0));
                }
            }
        }
        if (t % 3 == 0) {
            level.sendParticles(ModParticles.GLITCH_PIXEL.get(), this.getX(), this.getY(0.3 + t / (double) Attack.EXIT.duration * 0.7), this.getZ(),
                    10, 0.8, 0.4, 0.8, 0.06);
        }
        if (t >= Attack.EXIT.duration) {
            this.pixelBurst(level, this.position().add(0.0, 2.0, 0.0), 60, 1.2);
            if (this.home != null && level.getBlockEntity(this.home) instanceof ErrorCoreBlockEntity core && core.owns(this)) {
                core.onTrollDeparted(level);
            }
            this.discard();
        }
    }

    // ------------------------------------------------------------------ Angriffe

    /**
     * Lag-Hieb: Sie holt aus – und hängt fest, über ihrem Kopf dreht sich ein Ladekreis (mal kurz, mal lang). Verschwindet
     * er, kracht der Hammer zwei Ticks später herab: Wer vor ihr steht, wird schwer getroffen, und eine Schockwelle läuft
     * über den Boden (wer springt, entgeht ihr).
     */
    private void tickSmash(ServerLevel level, LivingEntity target) {
        int t = this.attackTick;
        if (this.lagUntil == 0) {
            this.faceTarget(target, 14.0f);
        }
        if (t == 1) {
            this.sound(level, ModSounds.ANNI_WINDUP.get(), 2.0f, 1.0f);
        }
        if (t == SMASH_RAISE) {
            int lag = this.isGlitchMode() ? 3 + this.random.nextInt(9) : 5 + this.random.nextInt(14);
            this.lagUntil = t + lag;
            this.setLagging(true);
            this.sound(level, ModSounds.ANNI_LAG.get(), 1.6f, 1.0f);
        }
        if (this.lagUntil > 0 && t == this.lagUntil) {
            this.setLagging(false);
            this.slamAt = t + 2;
        }
        if (this.slamAt > 0 && t == this.slamAt) {
            Vec3 impact = this.ground(this.position().add(this.forward().scale(3.4)));
            this.sound(level, ModSounds.ANNI_SMASH.get(), 3.5f, 0.9f);
            level.sendParticles(ParticleTypes.EXPLOSION, impact.x, impact.y + 0.4, impact.z, 2, 0.4, 0.2, 0.4, 0.0);
            this.pixelBurst(level, impact.add(0.0, 0.5, 0.0), 50, 1.0);
            for (LivingEntity victim : this.victims(level, new AABB(impact, impact).inflate(2.8, 2.5, 2.8))) {
                if (victim.distanceToSqr(impact.x, victim.getY(), impact.z) < 2.8 * 2.8
                        && victim.hurtServer(level, this.damageSources().mobAttack(this), this.scaled(22.0f))) {
                    push(victim, victim.position().subtract(impact).multiply(1.0, 0.0, 1.0).normalize().scale(1.0).add(0.0, 0.55, 0.0));
                    this.struck.add(victim.getUUID());
                }
            }
            this.wave = 1.0;
        }
        if (this.wave > 0.0 && this.slamAt > 0) {
            // die Schockwelle läuft über den Boden
            Vec3 impact = this.ground(this.position().add(this.forward().scale(3.4)));
            double r0 = this.wave;
            this.wave += 0.7;
            this.ring(level, impact, this.wave, 28);
            if (this.wave <= 7.0) {
                for (LivingEntity victim : this.victims(level, new AABB(impact, impact).inflate(this.wave + 1.0, 2.0, this.wave + 1.0))) {
                    double d = Math.sqrt(victim.distanceToSqr(impact.x, victim.getY(), impact.z));
                    if (d >= r0 - 0.5 && d < this.wave + 0.5 && victim.onGround() && victim.getY() < impact.y + 1.2
                            && this.struck.add(victim.getUUID())) {
                        victim.hurtServer(level, this.damageSources().mobAttack(this), this.scaled(12.0f));
                        push(victim, new Vec3(0.0, 0.7, 0.0));
                    }
                }
            } else {
                this.wave = -1.0;
            }
        }
    }

    /**
     * Gummiband: Sie flackert und ist fort – und steht woanders, drei-, im Fehlermodus viermal, immer um ihr Ziel herum
     * (Nachbilder bleiben stehen). Dann taucht sie direkt neben ihm auf und schlägt zu.
     */
    private void tickRubberband(ServerLevel level, LivingEntity target) {
        int t = this.attackTick;
        int hopsTotal = this.isGlitchMode() ? RUBBER_HOPS + 1 : RUBBER_HOPS;
        int last = RUBBER_FIRST + (hopsTotal - 1) * RUBBER_STEP;
        this.faceTarget(target, 30.0f);
        if (t >= RUBBER_FIRST && t <= last && (t - RUBBER_FIRST) % RUBBER_STEP == 0) {
            double a = this.random.nextDouble() * Math.PI * 2.0;
            double r = 5.0 + this.random.nextDouble() * 2.5;
            this.blinkTo(level, this.standOn(target.position().add(Math.cos(a) * r, 0.0, Math.sin(a) * r)));
        }
        if (t == last + 5) {
            Vec3 to = this.position().subtract(target.position()).multiply(1.0, 0.0, 1.0);
            to = to.lengthSqr() < 1.0e-4 ? new Vec3(1, 0, 0) : to.normalize();
            this.blinkTo(level, this.standOn(target.position().add(to.scale(2.6))));
            this.faceTarget(target, 360.0f);
            this.sound(level, ModSounds.ANNI_WINDUP.get(), 2.0f, 1.3f);
        }
        if (t == last + 13) {
            this.sound(level, ModSounds.ANNI_SMASH.get(), 2.5f, 1.2f);
            Vec3 fwd = this.forward();
            for (LivingEntity victim : this.victims(level, this.getBoundingBox().inflate(4.5, 1.0, 4.5))) {
                Vec3 to = victim.position().subtract(this.position()).multiply(1.0, 0.0, 1.0);
                if (to.length() < 4.8 && (to.lengthSqr() < 1.0 || to.normalize().dot(fwd) > 0.2)
                        && victim.hurtServer(level, this.damageSources().mobAttack(this), this.scaled(18.0f))) {
                    push(victim, to.normalize().scale(1.3).add(0.0, 0.45, 0.0));
                }
            }
            this.pixelBurst(level, this.position().add(fwd.scale(2.5)).add(0.0, 1.0, 0.0), 30, 0.8);
        }
    }

    /**
     * Fehler-Klone: Sie schüttelt sich, die Farben laufen auseinander – und plötzlich sind da drei (im Fehlermodus fünf)
     * Kopien von ihr, rot, grün und blau. Sie rennen los und „stürzen ab“, wenn sie jemanden erreichen; ein Treffer lässt
     * eine Kopie zerplatzen.
     */
    private void tickClones(ServerLevel level, LivingEntity target) {
        int t = this.attackTick;
        this.faceTarget(target, 10.0f);
        if (t == 2) {
            this.sound(level, ModSounds.ANNI_CLONE.get(), 2.5f, 1.0f);
        }
        if (t == CLONES_SPLIT) {
            int count = this.isGlitchMode() ? 5 : 3;
            Vec3 fwd = this.forward();
            Vec3 side = new Vec3(-fwd.z, 0.0, fwd.x);
            for (int i = 0; i < count; i++) {
                double k = count == 1 ? 0.0 : (i / (count - 1.0) - 0.5) * 2.0;
                GlitchCloneEntity clone = new GlitchCloneEntity(level, this);
                Vec3 at = this.standOn(this.position().add(side.scale(k * 2.6)).add(fwd.scale(0.6)));
                clone.snapTo(at.x, at.y, at.z, this.getYRot(), 0.0f);
                clone.setup(target, i % 5, side.scale(k * 0.55));
                level.addFreshEntity(clone);
            }
            this.pixelBurst(level, this.position().add(0.0, 2.0, 0.0), 40, 1.2);
        }
        if (t == CLONES_SPLIT + 8) {
            this.sound(level, ModSounds.ANNI_LAUGH.get(), 2.5f, 1.1f);
        }
    }

    /**
     * Fehlende Textur: Sie hebt den Hammer mit beiden Händen und rammt ihn in den Boden – unter ihrem Ziel (im Fehlermodus
     * auch unter einem zweiten) wird der Boden zu Magenta und Schwarz. Wer darin steht, hängt fest und nimmt Schaden.
     */
    private void tickMissing(ServerLevel level, LivingEntity target) {
        int t = this.attackTick;
        this.faceTarget(target, 12.0f);
        if (t == 2) {
            this.sound(level, ModSounds.ANNI_WINDUP.get(), 2.0f, 0.8f);
        }
        if (t == MISSING_SLAM) {
            this.sound(level, ModSounds.ANNI_SMASH.get(), 2.5f, 0.7f);
            Vec3 impact = this.ground(this.position().add(this.forward().scale(2.8)));
            this.pixelBurst(level, impact.add(0.0, 0.4, 0.0), 30, 0.8);
            List<Vec3> spots = new ArrayList<>();
            spots.add(this.ground(target.position()));
            if (this.isGlitchMode()) {
                Player other = level.getNearestPlayer(target.getX(), target.getY(), target.getZ(), 20.0,
                        e -> e != target && e instanceof Player p && this.canStrike(p));
                spots.add(other != null ? this.ground(other.position())
                        : this.ground(target.position().add(this.random.nextBoolean() ? 7.0 : -7.0, 0.0, this.random.nextBoolean() ? 6.0 : -6.0)));
            }
            List<MissingZoneEntity> zones = level.getEntitiesOfClass(MissingZoneEntity.class, this.getBoundingBox().inflate(48.0));
            for (int i = 0; i < zones.size() - 2; i++) {
                zones.get(i).discard();
            }
            for (Vec3 spot : spots) {
                MissingZoneEntity zone = new MissingZoneEntity(level, this, spot, this.isGlitchMode() ? 5 : 4, this.scaled(4.0f));
                level.addFreshEntity(zone);
            }
            this.sound(level, ModSounds.ANNI_MISSING.get(), 2.5f, 1.0f);
        }
    }

    /**
     * Pixelregen: Sie reckt den Hammer zum Himmel – leuchtende Pixel stürzen herab, auf Stellen, die ein Ring am Boden
     * zeigt (meist dort, wo ihre Gegner gleich sein werden).
     */
    private void tickPixelRain(ServerLevel level, LivingEntity target) {
        int t = this.attackTick;
        this.faceTarget(target, 8.0f);
        if (t == 4) {
            this.sound(level, ModSounds.ANNI_RAIN.get(), 3.0f, 1.0f);
        }
        int every = this.isGlitchMode() ? 2 : 3;
        if (t >= RAIN_START && t <= RAIN_END && t % every == 0) {
            List<LivingEntity> watchers = this.victims(level, this.getBoundingBox().inflate(24.0, 10.0, 24.0));
            Vec3 spot;
            if (!watchers.isEmpty() && this.random.nextInt(5) < 3) {
                LivingEntity who = watchers.get(this.random.nextInt(watchers.size()));
                Vec3 lead = who.position().add(who.getDeltaMovement().multiply(14.0, 0.0, 14.0));
                spot = lead.add((this.random.nextDouble() - 0.5) * 3.0, 0.0, (this.random.nextDouble() - 0.5) * 3.0);
            } else {
                double a = this.random.nextDouble() * Math.PI * 2.0;
                double r = 3.0 + this.random.nextDouble() * 11.0;
                spot = this.position().add(Math.cos(a) * r, 0.0, Math.sin(a) * r);
            }
            Vec3 ground = this.ground(spot);
            PixelCubeEntity pixel = new PixelCubeEntity(level, this, ground, this.random.nextInt(6), this.scaled(7.0f));
            level.addFreshEntity(pixel);
            SpellFxEntity.create(level, SpellFxEntity.Kind.CIRCLE, ground.add(0.0, 0.05, 0.0), null, PixelCubeEntity.COLORS[pixel.colorIndex()],
                    PixelCubeEntity.FALL_TICKS, 1.3f).spawn();
        }
    }

    /**
     * Rückruck: Sie hebt den zerfallenen Arm, ihr Bildschirmauge flackert – und sie merkt sich, wo alle stehen (ein
     * flimmerndes Abbild bleibt dort). Drei Sekunden später reißt es alle dorthin zurück (kurz schwindlig); dann stürmt sie
     * auf den Nächsten los und schlägt zu.
     */
    private void tickRollback(ServerLevel level, LivingEntity target) {
        int t = this.attackTick;
        if (this.charge == null) {
            this.faceTarget(target, 10.0f);
        }
        if (t == 2) {
            this.sound(level, ModSounds.ANNI_ROLLBACK_MARK.get(), 2.5f, 1.0f);
        }
        if (t == ROLLBACK_MARK) {
            this.rollback.clear();
            for (LivingEntity victim : this.victims(level, this.getBoundingBox().inflate(24.0, 12.0, 24.0))) {
                if (victim instanceof Player || victim == target) {
                    this.rollback.put(victim.getUUID(), victim.position());
                    AfterimageEntity mark = AfterimageEntity.ofEntity(level, victim, ROLLBACK_SNAP - ROLLBACK_MARK + 4);
                    level.addFreshEntity(mark);
                }
            }
        }
        if (t == ROLLBACK_SNAP) {
            this.sound(level, ModSounds.ANNI_ROLLBACK.get(), 3.0f, 1.0f);
            for (Map.Entry<UUID, Vec3> entry : this.rollback.entrySet()) {
                if (!(level.getEntity(entry.getKey()) instanceof LivingEntity victim) || !victim.isAlive() || !this.canStrike(victim)) {
                    continue;
                }
                Vec3 back = entry.getValue();
                if (victim.distanceToSqr(back) > 48.0 * 48.0) {
                    continue;
                }
                this.pixelBurst(level, victim.position().add(0.0, 1.0, 0.0), 20, 0.5);
                if (victim instanceof ServerPlayer player) {
                    player.teleportTo(level, back.x, back.y, back.z, Set.of(), player.getYRot(), player.getXRot(), true);
                    player.sendOverlayMessage(Component.translatable("entity.kingdomsatwar.anni_troll.rollback").withColor(0x20E8FF));
                } else {
                    victim.teleportTo(back.x, back.y, back.z);
                }
                victim.setDeltaMovement(Vec3.ZERO);
                victim.needsSync = true;
                victim.hurtServer(level, this.damageSources().indirectMagic(this, this), this.scaled(4.0f));
                victim.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 70, 0), this);
                victim.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 20, 1), this);
                this.pixelBurst(level, back.add(0.0, 1.0, 0.0), 20, 0.5);
                if (this.charge == null || victim.distanceToSqr(this) < this.distanceToSqr(back)) {
                    this.charge = victim.getUUID();
                    this.lunge = back;
                }
            }
        }
        if (t > ROLLBACK_SNAP && this.charge != null && t < ROLLBACK_SNAP + 10) {
            // sie stürmt auf die Stelle zu, an die es den Nächsten zurückgerissen hat
            Vec3 to = this.lunge.subtract(this.position()).multiply(1.0, 0.0, 1.0);
            if (to.length() > 2.4) {
                this.setDeltaMovement(to.normalize().scale(0.75).add(0.0, this.getDeltaMovement().y, 0.0));
            }
            this.faceTowards(this.lunge, 30.0f);
            if (t == ROLLBACK_SNAP + 2) {
                this.sound(level, ModSounds.ANNI_WINDUP.get(), 2.0f, 1.2f);
            }
        }
        if (t == ROLLBACK_SNAP + 12 && this.charge != null) {
            Vec3 impact = this.ground(this.position().add(this.forward().scale(2.4)));
            this.sound(level, ModSounds.ANNI_SMASH.get(), 3.0f, 1.0f);
            this.pixelBurst(level, impact.add(0.0, 0.5, 0.0), 40, 0.9);
            level.sendParticles(ParticleTypes.EXPLOSION, impact.x, impact.y + 0.4, impact.z, 1, 0.0, 0.0, 0.0, 0.0);
            for (LivingEntity victim : this.victims(level, new AABB(impact, impact).inflate(3.0, 2.5, 3.0))) {
                if (victim.distanceToSqr(impact.x, victim.getY(), impact.z) < 3.0 * 3.0
                        && victim.hurtServer(level, this.damageSources().mobAttack(this), this.scaled(16.0f))) {
                    push(victim, victim.position().subtract(impact).multiply(1.0, 0.0, 1.0).normalize().scale(0.9).add(0.0, 0.5, 0.0));
                }
            }
        }
    }

    /**
     * Zeilenfehler: Sie stampft auf – eine leuchtende Zeile kreist um sie, die rote dicht über dem Boden (springen!), dann
     * eine blaue in Kopfhöhe (ducken!). Im Fehlermodus drehen sie sich schneller.
     */
    private void tickScanline(ServerLevel level, LivingEntity target) {
        int t = this.attackTick;
        this.faceTarget(target, 6.0f);
        if (t == 2) {
            this.sound(level, ModSounds.ANNI_WINDUP.get(), 2.0f, 0.9f);
        }
        if (t == SCAN_LOW || t == SCAN_HIGH) {
            boolean low = t == SCAN_LOW;
            this.sound(level, ModSounds.ANNI_SMASH.get(), 2.2f, low ? 0.8f : 1.1f);
            this.sound(level, ModSounds.ANNI_SCAN.get(), 3.0f, low ? 0.9f : 1.15f);
            float toTarget = (float) (Mth.atan2(target.getZ() - this.getZ(), target.getX() - this.getX()) * Mth.RAD_TO_DEG);
            // so, dass die Zeile das Ziel erst nach einer Viertel- bis halben Drehung erreicht
            float dir = low ? 1.0f : -1.0f;
            float speed = (this.isGlitchMode() ? 11.0f : 8.0f) * dir;
            float start = toTarget - dir * (100.0f + this.random.nextFloat() * 60.0f);
            ScanlineEntity line = new ScanlineEntity(level, this, this.ground(this.position()), low ? 0.45f : 1.62f, low, start, speed,
                    this.scaled(12.0f));
            level.addFreshEntity(line);
        }
    }

    /** Troll-Lachen: Sie wirft den Kopf zurück und lacht (eine kurze Pause). */
    private void tickLaugh(ServerLevel level, LivingEntity target) {
        this.faceTarget(target, 8.0f);
        if (this.attackTick == 3) {
            this.sound(level, ModSounds.ANNI_LAUGH.get(), 3.0f, 0.95f + this.random.nextFloat() * 0.1f);
        }
    }

    /**
     * Absturz: „Anni.exe reagiert nicht.“ Sie erstarrt in einer verdrehten Haltung, flackert und ist nicht zu treffen. Dann
     * „wird neu gestartet“ – sie richtet sich auf, und eine Welle aus Pixeln rollt über den Boden (springen!). Danach kämpft
     * sie im Fehlermodus weiter.
     */
    private void tickCrash(ServerLevel level) {
        int t = this.attackTick;
        this.getNavigation().stop();
        this.setDeltaMovement(this.getDeltaMovement().multiply(0.0, 1.0, 0.0));
        if (t == 1) {
            this.sound(level, ModSounds.ANNI_CRASH.get(), 4.0f, 1.0f);
            this.setLagging(false);
            for (ServerPlayer player : level.players()) {
                if (player.distanceToSqr(this) < 64.0 * 64.0) {
                    player.sendOverlayMessage(Component.translatable("entity.kingdomsatwar.anni_troll.crash").withColor(0xFF5050));
                }
            }
        }
        if (t < CRASH_RESTART && t % 4 == 0) {
            this.pixelBurst(level, this.position().add((this.random.nextDouble() - 0.5) * 3.0, 1.0 + this.random.nextDouble() * 3.0,
                    (this.random.nextDouble() - 0.5) * 3.0), 8, 0.4);
        }
        if (t == CRASH_RESTART) {
            this.sound(level, ModSounds.ANNI_RESTART.get(), 4.0f, 1.0f);
            for (ServerPlayer player : level.players()) {
                if (player.distanceToSqr(this) < 64.0 * 64.0) {
                    player.sendOverlayMessage(Component.translatable("entity.kingdomsatwar.anni_troll.restart").withColor(0x20E8FF));
                }
            }
        }
        if (t == CRASH_WAVE) {
            this.entityData.set(DATA_GLITCH, true);
            this.sound(level, ModSounds.ANNI_SMASH.get(), 4.0f, 0.6f);
            level.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, 0xFF20E8FF), this.getX(), this.getY(1.0), this.getZ(), 1, 0.0,
                    0.0, 0.0, 0.0);
            this.wave = 1.5;
            this.struck.clear();
        }
        if (this.wave > 0.0) {
            double r0 = this.wave;
            this.wave += 0.65;
            Vec3 c = this.position();
            this.ring(level, c, this.wave, 44);
            for (LivingEntity victim : this.victims(level, this.getBoundingBox().inflate(this.wave + 1.0, 3.0, this.wave + 1.0))) {
                double d = Math.sqrt(victim.distanceToSqr(c.x, victim.getY(), c.z));
                if (d >= r0 - 0.6 && d < this.wave + 0.6 && victim.onGround() && victim.getY() < c.y + 2.0 && this.struck.add(victim.getUUID())) {
                    victim.hurtServer(level, this.damageSources().indirectMagic(this, this), 18.0f);
                    push(victim, victim.position().subtract(c).multiply(1.0, 0.0, 1.0).normalize().scale(0.8).add(0.0, 0.9, 0.0));
                }
            }
            if (this.wave > 16.0) {
                this.wave = -1.0;
            }
        }
        if (t == CRASH_WAVE + 10) {
            this.sound(level, ModSounds.ANNI_LAUGH.get(), 4.0f, 0.8f);
        }
    }

    // ------------------------------------------------------------------ Hilfen

    /** Ein flackernder Sprung: ein Nachbild bleibt stehen, Pixel stieben, und sie steht woanders. */
    void blinkTo(ServerLevel level, Vec3 to) {
        Vec3 from = this.position();
        level.addFreshEntity(AfterimageEntity.ofTroll(level, this, 14));
        this.pixelBurst(level, from.add(0.0, 2.0, 0.0), 20, 0.9);
        this.teleportTo(to.x, to.y, to.z);
        this.setDeltaMovement(Vec3.ZERO);
        this.getNavigation().stop();
        this.pixelBurst(level, to.add(0.0, 2.0, 0.0), 20, 0.9);
        this.sound(level, ModSounds.ANNI_TELEPORT.get(), 2.0f, 0.9f + this.random.nextFloat() * 0.3f);
    }

    /** Ein Ring aus Pixeln am Boden (die Schockwelle). */
    private void ring(ServerLevel level, Vec3 c, double radius, int points) {
        for (int i = 0; i < points; i++) {
            double a = i * Math.PI * 2.0 / points + this.random.nextDouble() * 0.1;
            double x = c.x + Math.cos(a) * radius;
            double z = c.z + Math.sin(a) * radius;
            level.sendParticles(ModParticles.GLITCH_PIXEL.get(), x, c.y + 0.25, z, 1, 0.05, 0.1, 0.05, 0.02);
            if (i % 4 == 0) {
                level.sendParticles(i % 8 == 0 ? MAGENTA : CYAN, x, c.y + 0.4, z, 1, 0.1, 0.1, 0.1, 0.0);
            }
        }
    }

    void pixelBurst(ServerLevel level, Vec3 at, int count, double spread) {
        level.sendParticles(ModParticles.GLITCH_PIXEL.get(), at.x, at.y, at.z, count, spread, spread * 0.8, spread, 0.12);
    }

    /** Wohin sie an einer Stelle tritt: auf den Boden (nicht in Blöcke, nicht in die Luft). */
    Vec3 standOn(Vec3 at) {
        Vec3 g = this.ground(at);
        BlockPos pos = BlockPos.containing(g);
        Level level = this.level();
        for (int i = 0; i < 6 && !level.noCollision(this, this.getBoundingBox().move(g.subtract(this.position()))); i++) {
            pos = pos.above();
            g = new Vec3(g.x, pos.getY(), g.z);
        }
        return g;
    }

    /** Der Boden unter einer Stelle (oder die Stelle selbst, wenn der Boden weit entfernt liegt). */
    Vec3 ground(Vec3 at) {
        BlockPos start = BlockPos.containing(at.x, at.y + 3.0, at.z);
        HitResult hit = this.level().clip(new ClipContext(Vec3.atCenterOf(start), new Vec3(at.x, at.y - 10.0, at.z), ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE, this));
        if (hit.getType() == HitResult.Type.BLOCK) {
            return new Vec3(at.x, hit.getLocation().y, at.z);
        }
        int y = this.level().getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Mth.floor(at.x), Mth.floor(at.z));
        if (y <= this.level().getMinY() || y > at.y + 6.0 || y < at.y - 12.0) {
            return at;
        }
        return new Vec3(at.x, y, at.z);
    }

    Vec3 forward() {
        float yaw = this.getYRot() * Mth.DEG_TO_RAD;
        return new Vec3(-Mth.sin(yaw), 0.0, Mth.cos(yaw));
    }

    private void faceTarget(LivingEntity target, float step) {
        this.faceTowards(target.position(), step);
        this.getLookControl().setLookAt(target, 30.0f, 30.0f);
    }

    private void faceTowards(Vec3 at, float step) {
        this.turnTo((float) (Mth.atan2(at.z - this.getZ(), at.x - this.getX()) * Mth.RAD_TO_DEG) - 90.0f, step);
    }

    private void turnTo(float yaw, float step) {
        float next = Mth.approachDegrees(this.getYRot(), yaw, step);
        this.setYRot(next);
        this.yBodyRot = next;
        this.yHeadRot = next;
    }

    void sound(ServerLevel level, SoundEvent sound, float volume, float pitch) {
        level.playSound(null, this.getX(), this.getY(), this.getZ(), sound, SoundSource.HOSTILE, volume, pitch);
    }

    static void push(LivingEntity entity, Vec3 push) {
        entity.push(push.x, push.y, push.z);
        entity.needsSync = true;
    }

    float scaled(float damage) {
        return this.isGlitchMode() ? damage * 1.2f : damage;
    }

    // ------------------------------------------------------------------ Klänge und Tod

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return this.attack == Attack.NONE ? ModSounds.ANNI_AMBIENT.get() : null;
    }

    @Override
    public int getAmbientSoundInterval() {
        return 160;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.ANNI_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.ANNI_DEATH.get();
    }

    @Override
    protected float getSoundVolume() {
        return 2.5f;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(ModSounds.ANNI_STEP.get(), 1.2f, 0.9f + this.random.nextFloat() * 0.2f);
    }

    /** Sie stürzt endgültig ab: Sie erstarrt, flackert, zerfällt in Pixel; der Kern erlischt. */
    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (this.level() instanceof ServerLevel level) {
            level.getEntitiesOfClass(GlitchCloneEntity.class, this.getBoundingBox().inflate(48.0), c -> c.isCloneOf(this)).forEach(Entity::discard);
            level.getEntitiesOfClass(MissingZoneEntity.class, this.getBoundingBox().inflate(48.0)).forEach(Entity::discard);
            for (ServerPlayer player : level.players()) {
                if (player.distanceToSqr(this) < 64.0 * 64.0) {
                    player.sendOverlayMessage(Component.translatable("entity.kingdomsatwar.anni_troll.game_over").withColor(0xF800F8));
                }
            }
            if (this.home != null && level.getBlockEntity(this.home) instanceof ErrorCoreBlockEntity core && core.owns(this)) {
                core.onTrollDefeated(level);
            }
        }
    }

    @Override
    protected void tickDeath() {
        this.deathTime++;
        this.setDeltaMovement(this.getDeltaMovement().multiply(0.0, 1.0, 0.0));
        if (this.level() instanceof ServerLevel level) {
            if (this.deathTime % 3 == 0) {
                double k = this.deathTime / (double) DEATH_TICKS;
                level.sendParticles(ModParticles.GLITCH_PIXEL.get(), this.getX(), this.getY(0.2 + k * 0.8), this.getZ(), 6 + (int) (k * 10), 0.9,
                        0.5, 0.9, 0.08);
            }
            if (this.deathTime == DEATH_TICKS - 6) {
                this.pixelBurst(level, this.position().add(0.0, 2.2, 0.0), 200, 1.6);
                level.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, 0xFFF800F8), this.getX(), this.getY(0.5), this.getZ(), 2, 0.3,
                        0.3, 0.3, 0.0);
            }
            if (this.deathTime >= DEATH_TICKS && !this.isRemoved()) {
                this.remove(Entity.RemovalReason.KILLED);
            }
        }
    }

    // ------------------------------------------------------------------ Speichern

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        if (this.home != null) {
            output.putLong("Home", this.home.asLong());
        }
        output.putBoolean("Crashed", this.crashed);
        output.putBoolean("GlitchMode", this.isGlitchMode());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.home = input.getLong("Home").map(BlockPos::of).orElse(null);
        this.crashed = input.getBooleanOr("Crashed", false);
        this.entityData.set(DATA_GLITCH, input.getBooleanOr("GlitchMode", false));
        if (this.hasCustomName()) {
            this.bossBar.setName(this.getDisplayName());
        }
    }
}
