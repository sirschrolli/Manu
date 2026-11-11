package io.github.sirschrolli.kingdomsatwar.entity.glitch;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
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
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
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
 * Anni der Troll: ein riesiger Troll, der in einer Fehlerzone der Oberwelt haust – dort, wo die Welt kaputtgegangen ist,
 * und Anni mit ihr. Er glitcht wie ein Spielfehler: Er flackert und ruckelt, hängt mitten in der Bewegung fest und holt
 * dann ruckartig auf, springt durch den Raum, zerfällt in Farben und läuft, wenn er feststeckt, einfach durch Wände.
 * Manche Treffer werden nicht registriert. Er kämpft mit seinem Bannhammer und mit Fehlern:
 * <ul>
 *     <li><b>Lag-Hieb</b> – er holt aus und hängt fest (über ihm dreht sich ein Ladekreis); wenn der verschwindet, kracht
 *     der Hammer herab, eine Schockwelle läuft über den Boden (springen!);</li>
 *     <li><b>Gummiband</b> – er springt flackernd drei-, viermal um sein Ziel herum und schlägt dann neben ihm zu;</li>
 *     <li><b>Fehler-Klone</b> – er verdoppelt sich: rote, grüne und blaue Kopien rennen los und stürzen ab (ein Treffer lässt
 *     sie zerplatzen);</li>
 *     <li><b>Fehlende Textur</b> – unter seinem Ziel wird der Boden zu Magenta und Schwarz: Wer darin steht, hängt fest und
 *     nimmt Schaden;</li>
 *     <li><b>Pixelregen</b> – leuchtende Pixel stürzen vom Himmel auf markierte Stellen;</li>
 *     <li><b>Rückruck</b> – er merkt sich, wo alle stehen, und reißt sie drei Sekunden später dorthin zurück – dann stürmt er
 *     los;</li>
 *     <li><b>Zeilenfehler</b> – eine leuchtende Zeile kreist um ihn: die rote dicht über dem Boden (springen!), die blaue in
 *     Kopfhöhe (ducken!);</li>
 *     <li><b>Absturz</b> (einmal, unter halber Kraft) – „Anni.exe reagiert nicht.“: Er erstarrt, dann startet er neu – eine
 *     Welle aus Pixeln rollt über den Boden – und kämpft im <b>Fehlermodus</b> weiter: schneller, härter, mehr Klone;</li>
 *     <li><b>Wiederherstellung</b> (einmal, statt zu sterben) – „Schwerer Ausnahmefehler“: Er zerfällt in Pixel, lädt eine
 *     Sicherung und kehrt als <b>Anni 2.0</b> zurück – rote Augen, eine neue, volle Leiste, noch schneller und härter, mit
 *     dem <b>Speedhack</b> (dreimal rast er geradeaus durch die Arena) und verstärkten Angriffen.</li>
 * </ul>
 * Findet er niemanden mehr, zerfällt er nach einer halben Minute in Pixel und kehrt in seinen Fehlerkern zurück. Besiegt
 * stürzt er endgültig ab und zerfällt in Pixel; der Kern erlischt (ein Redstoneblock startet ihn neu).
 */
public class AnniTrollEntity extends Monster implements io.github.sirschrolli.kingdomsatwar.arena.BossLair.Bound, io.github.sirschrolli.kingdomsatwar.entity.boss.sky.SkyWarden {
    public static final float MAX_HEALTH = 1500.0f;
    public static final int SPAWN_TICKS = 90;
    /** In diesem Tick des Ladens setzt er sich zusammen (davor fliegen nur Pixel). */
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
    /** Wiederherstellung: In diesem Tick zerfällt er in Pixel … */
    public static final int REVIVE_SHATTER = 30;
    /** … hier ist die Sicherung gefunden, von hier bis REVIVE_FILLED füllt sich seine Leiste … */
    public static final int REVIVE_FOUND = 48;
    public static final int REVIVE_FILL = 56;
    public static final int REVIVE_FILLED = 104;
    /** … hier setzt er sich als Anni 2.0 zusammen, und hier rollt die Welle los. */
    public static final int REVIVE_ASSEMBLE = 112;
    public static final int REVIVE_WAVE = 118;
    /** Wie viel seiner Lebenskraft die Sicherung enthält (Anni 2.0 hat eine volle, aber kürzere Leiste). */
    public static final double BACKUP_HEALTH = 0.6;
    /** Speedhack (nur Anni 2.0): erster Ansturm, Abstand der Anstürme, Zielen und Rasen (Ticks), Zahl der Anstürme. */
    public static final int HACK_FIRST = 6;
    public static final int HACK_STEP = 26;
    public static final int HACK_AIM = 10;
    public static final int HACK_DASH = 12;
    public static final int HACK_DASHES = 3;
    private static final Identifier BACKUP_HEALTH_ID = KingdomsAtWar.id("anni_backup_health");
    private static final Identifier BACKUP_SPEED_ID = KingdomsAtWar.id("anni_backup_speed");
    private static final double LEASH = 40.0;
    private static final int RETURN_TICKS = 600;

    public enum Attack {
        NONE(0),
        /** Laden: Pixel fliegen zusammen, „Lädt …“, dann setzt er sich zusammen und lacht. */
        SPAWN(SPAWN_TICKS),
        /** Lag-Hieb (die Dauer hängt davon ab, wie lange er hängt). */
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
        /** Verbindung getrennt: Er zerfällt in Pixel und kehrt in den Kern zurück. */
        EXIT(46),
        /** Speedhack (nur Anni 2.0): Er zielt (Ladekreis) und rast dreimal geradeaus los. */
        SPEEDHACK(HACK_FIRST + HACK_DASHES * HACK_STEP + 14),
        /** Wiederherstellung (einmal, statt zu sterben): Er lädt eine Sicherung und kehrt als Anni 2.0 zurück. */
        REVIVE(150);

        public final int duration;

        Attack(int duration) {
            this.duration = duration;
        }
    }

    private static final EntityDataAccessor<Integer> DATA_ATTACK = SynchedEntityData.defineId(AnniTrollEntity.class, EntityDataSerializers.INT);
    /** Hängt er gerade (Ladekreis über dem Kopf)? */
    private static final EntityDataAccessor<Boolean> DATA_LAG = SynchedEntityData.defineId(AnniTrollEntity.class, EntityDataSerializers.BOOLEAN);
    /** Fehlermodus (nach dem Absturz): stärkere Glitches, schneller, härter. */
    private static final EntityDataAccessor<Boolean> DATA_GLITCH = SynchedEntityData.defineId(AnniTrollEntity.class,
            EntityDataSerializers.BOOLEAN);
    /** Kurz nicht zu sehen (beim Springen und vor dem Zusammensetzen). */
    private static final EntityDataAccessor<Boolean> DATA_HIDDEN = SynchedEntityData.defineId(AnniTrollEntity.class,
            EntityDataSerializers.BOOLEAN);
    /** Anni 2.0 (aus der Sicherung wiederhergestellt): rote Augen, noch schneller, noch härter. */
    private static final EntityDataAccessor<Boolean> DATA_V2 = SynchedEntityData.defineId(AnniTrollEntity.class, EntityDataSerializers.BOOLEAN);
    static final ParticleOptions MAGENTA = new DustParticleOptions(0xF800F8, 1.4f);
    static final ParticleOptions CYAN = new DustParticleOptions(0x20E8FF, 1.4f);
    static final ParticleOptions SMOKE = new DustParticleOptions(0x1A1420, 2.4f);
    static final ParticleOptions RED = new DustParticleOptions(0xFF2040, 1.4f);

    private final ServerBossEvent bossBar = new ServerBossEvent(UUID.randomUUID(), Component.translatable("boss.kingdomsatwar.anni"),
            BossEvent.BossBarColor.PINK, BossEvent.BossBarOverlay.NOTCHED_12);
    private Attack attack = Attack.NONE;
    private Attack lastAttack = Attack.NONE;
    private int attackTick;
    private int cooldown = 30;
    private int idleTicks;
    private boolean crashed;
    /** Hat er seine Sicherung schon geladen (dann stirbt er beim nächsten Mal wirklich)? */
    private boolean revived;
    private @Nullable BlockPos home;
    // Speedhack: wohin er rast
    private Vec3 dash = Vec3.ZERO;
    // Lag-Hieb: wann er aufhört zu hängen und wann der Hammer trifft
    private int lagUntil;
    private int slamAt;
    // Gummiband: wohin er springt, zuletzt neben das Ziel
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
    /** Nur im Client: wann er zuletzt zu hängen begann und wann er wieder losließ (für die Animationen). */
    private int clientLagStart = -100;
    private int clientLagEnd = -100;

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
        builder.define(DATA_V2, false);
    }

    // ------------------------------------------------------------------ Zustand

    public Attack currentAttack() {
        int i = this.entityData.get(DATA_ATTACK);
        return i >= 0 && i < Attack.values().length ? Attack.values()[i] : Attack.NONE;
    }

    /** Hängt er gerade fest (Ladekreis)? */
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

    /** Anni 2.0 (nach der Wiederherstellung)? */
    public boolean isVersion2() {
        return this.entityData.get(DATA_V2);
    }

    /** Hat er seine Sicherung schon verbraucht? */
    public boolean isRevived() {
        return this.revived;
    }

    private void setHidden(boolean hidden) {
        this.entityData.set(DATA_HIDDEN, hidden);
    }

    /** Wie lange der laufende Angriff schon dauert (im Client, für die Animationen). */
    public float attackTime(float partialTicks) {
        return this.tickCount - this.clientAttackStart + partialTicks;
    }

    /** Ticks, seit er zuletzt losließ (nach dem Hängen; im Client, für den Hieb des Lag-Hiebs). */
    public float sinceLag(float partialTicks) {
        return this.isLagging() ? -1.0f : this.tickCount - this.clientLagEnd + partialTicks;
    }

    /** Ticks, seit er zu hängen begann (im Client). */
    public float lagTime(float partialTicks) {
        return this.isLagging() ? this.tickCount - this.clientLagStart + partialTicks : -1.0f;
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (DATA_ATTACK.equals(key)) {
            this.clientAttackStart = this.tickCount;
        }
        if (DATA_LAG.equals(key)) {
            if (this.isLagging()) {
                this.clientLagStart = this.tickCount;
            } else {
                this.clientLagEnd = this.tickCount;
            }
        }
    }

    public @Nullable BlockPos home() {
        return this.home;
    }

    /** Wo sein Fehlerkern steht (für die Kampfdimension und das Zurücksetzen nach dem Tod seiner Gegner). */
    @Override
    public @Nullable BlockPos lairPos() {
        return this.home;
    }

    /** Bindet ihn an seinen Fehlerkern (dort wird er geladen, dorthin kehrt er zurück). */
    public void bindTo(BlockPos core) {
        this.home = core.immutable();
    }

    /** Er wird geladen: Pixel fliegen zusammen, dann setzt er sich zusammen (bis dahin unverwundbar). */
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

    /** Sofort Anni 2.0 mit voller Leiste (für Tests und die Vorführung). */
    public void forceVersion2() {
        this.revived = true;
        this.forceGlitchMode();
        this.becomeVersion2();
        this.setHealth(this.getMaxHealth());
    }

    /** Die Sicherung ist geladen: kürzere Leiste, schneller, rote Augen, ein neuer Name über der Leiste. */
    private void becomeVersion2() {
        AttributeInstance health = this.getAttribute(Attributes.MAX_HEALTH);
        if (health != null && !health.hasModifier(BACKUP_HEALTH_ID)) {
            health.addPermanentModifier(new AttributeModifier(BACKUP_HEALTH_ID, BACKUP_HEALTH - 1.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
        AttributeInstance speed = this.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null && !speed.hasModifier(BACKUP_SPEED_ID)) {
            speed.addPermanentModifier(new AttributeModifier(BACKUP_SPEED_ID, 0.2, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        }
        this.entityData.set(DATA_GLITCH, true);
        this.entityData.set(DATA_V2, true);
        this.nameBossBar();
    }

    private void nameBossBar() {
        if (this.hasCustomName()) {
            this.bossBar.setName(this.getDisplayName());
        } else {
            this.bossBar.setName(Component.translatable(this.isVersion2() ? "boss.kingdomsatwar.anni2" : "boss.kingdomsatwar.anni"));
        }
        this.bossBar.setColor(this.isVersion2() ? BossEvent.BossBarColor.RED : BossEvent.BossBarColor.PINK);
    }

    private void begin(Attack next) {
        this.attack = next;
        this.attackTick = 0;
        this.entityData.set(DATA_ATTACK, next.ordinal());
        if (next != Attack.NONE && next != Attack.SPAWN && next != Attack.CRASH && next != Attack.EXIT && next != Attack.LAUGH
                && next != Attack.REVIVE) {
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
     * Beim Laden, Abstürzen (bis zum Neustart) und Gehen trifft ihn nichts; manche Geschosse gehen einfach durch ihn
     * hindurch („Treffer nicht registriert“). Ein Hieb lässt ihn manchmal ein Stück zur Seite springen.
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
        if (this.attack == Attack.CRASH && this.attackTick < CRASH_RESTART || this.attack == Attack.REVIVE) {
            if (!this.isHidden()) {
                this.notRegistered(level, source.getSourcePosition() != null ? source.getSourcePosition() : this.position().add(0, 2, 0));
            }
            return false;
        }
        if (source.getDirectEntity() instanceof Projectile shot && this.random.nextFloat() < (this.isGlitchMode() ? 0.35f : 0.25f)) {
            this.notRegistered(level, shot.position());
            return false;
        }
        // mehr Lebenskraft, als die Leiste von Minecraft fasst (höchstens 1024): Treffer zählen entsprechend weniger
        amount *= this.healthScale();
        boolean hurt = super.hurtServer(level, source, this.isVersion2() ? amount * 0.85f : this.isGlitchMode() ? amount * 0.9f : amount);
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

    /** Er springt drei, vier Blöcke zur Seite (ein Nachbild bleibt stehen). */
    private void sidestep(ServerLevel level, LivingEntity from) {
        Vec3 away = this.position().subtract(from.position()).multiply(1.0, 0.0, 1.0);
        away = away.lengthSqr() < 1.0e-4 ? this.forward().scale(-1.0) : away.normalize();
        Vec3 side = new Vec3(-away.z, 0.0, away.x).scale(this.random.nextBoolean() ? 1.0 : -1.0);
        this.blinkTo(level, this.position().add(side.scale(3.5)).add(away.scale(1.5)));
    }

    @Override
    public void tick() {
        // durch Wände laufen, solange er „hindurchclippt“
        this.noPhysics = this.clipTicks > 0;
        super.tick();
        if (this.level().isClientSide()) {
            this.clientEffects();
        }
    }

    /** Pixel lösen sich von ihm; im Fehlermodus mehr, beim Hängen flimmern Farbsäume um ihn. */
    private void clientEffects() {
        if (this.isHidden()) {
            return;
        }
        Level level = this.level();
        int chance = this.isVersion2() ? 1 : this.isGlitchMode() ? 2 : 4;
        if (this.random.nextInt(chance) == 0) {
            level.addParticle(ModParticles.GLITCH_PIXEL.get(), this.getRandomX(1.0), this.getY() + this.random.nextDouble() * 3.6,
                    this.getRandomZ(1.0), 0.0, 0.02, 0.0);
        }
        if (this.isVersion2() && this.random.nextInt(3) == 0) {
            level.addParticle(RED, this.getRandomX(1.2), this.getY() + this.random.nextDouble() * 3.8, this.getRandomZ(1.2), 0.0, 0.0, 0.0);
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
        if (this.attack == Attack.REVIVE) {
            this.attackTick++;
            this.tickRevive(level);
            if (this.attackTick >= Attack.REVIVE.duration) {
                this.begin(Attack.NONE);
                this.cooldown = 10;
            }
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
            case SPEEDHACK -> this.tickSpeedhack(level, target);
            default -> { }
        }
        boolean done = this.attack == Attack.SMASH ? this.slamAt > 0 && this.attackTick >= this.slamAt + 14
                : this.attackTick >= this.attack.duration;
        if (done) {
            this.begin(Attack.NONE);
            this.cooldown = this.isVersion2() ? 6 + this.random.nextInt(10) : this.isGlitchMode() ? 10 + this.random.nextInt(12)
                    : 22 + this.random.nextInt(18);
        }
    }

    /**
     * Gehört er (noch) zu seinem Kern? Ist dort inzwischen eine andere Anni gebunden (etwa nach dem Neuladen der Welt),
     * vergeht dieser hier; ist der Kern fort, bleibt er frei.
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
     * Wen seine Angriffe treffen: Spieler (nicht im Kreativ- oder Zuschauermodus), ihre Begleiter und Soldaten, Tiere – aber
     * keine Monster (außer sie greifen ihn an), nicht sich selbst, nicht seine Klone, keine Rüstungsständer.
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
     * Niemand da: zurück zum Kern, erholen – und nach einer halben Minute „Verbindung getrennt“: Er zerfällt in Pixel und
     * kehrt in den Kern zurück. Ist er zu weit fortgelaufen, ruckt er zurück.
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
     * Auf sein Ziel zu – stampfend, mit Rucklern: Alle paar Sekunden hängt er fest (Ladekreis) und ruckt dann ein Stück
     * vorwärts. Kommt er nicht weiter, läuft er durch die Wand.
     */
    private void chase(ServerLevel level, LivingEntity target) {
        this.getLookControl().setLookAt(target, 30.0f, 30.0f);
        if (this.stutter > 0) {
            this.getNavigation().stop();
            if (--this.stutter == 0) {
                this.setLagging(false);
                Vec3 to = target.position().subtract(this.position()).multiply(1.0, 0.0, 1.0);
                if (to.length() > 4.0) {
                    this.blinkTo(level, this.position().add(to.normalize().scale(Math.min(this.isVersion2() ? 5.0 : 3.5, to.length() - 3.0))));
                }
            }
            return;
        }
        if (--this.nextStutter <= 0) {
            this.nextStutter = (this.isVersion2() ? 50 : this.isGlitchMode() ? 70 : 140) + this.random.nextInt(this.isVersion2() ? 60 : 80);
            this.stutter = 6 + this.random.nextInt(6);
            this.setLagging(true);
            this.sound(level, ModSounds.ANNI_LAG.get(), 1.4f, 1.0f);
            return;
        }
        double distance = this.distanceTo(target);
        if (distance > 3.6) {
            this.getNavigation().moveTo(target, this.isVersion2() ? 1.3 : this.isGlitchMode() ? 1.25 : 1.0);
        } else {
            this.getNavigation().stop();
        }
        // steckt er fest? dann läuft er durch die Wand
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

    /** Durch Wände: zwei Sekunden lang geht er geradewegs auf sein Ziel zu, durch alles hindurch. */
    private void startClip(ServerLevel level, LivingEntity target) {
        this.clipTicks = 40;
        this.getNavigation().stop();
        this.sound(level, ModSounds.ANNI_TELEPORT.get(), 1.5f, 0.6f);
        this.pixelBurst(level, this.position().add(0.0, 2.0, 0.0), 24, 1.0);
    }

    /** Ende des Durchclippens: steckt er noch in Blöcken, schiebt es ihn nach oben heraus (oder er clippt noch ein Stück). */
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
        if (this.isVersion2()) {
            options.add(Attack.SPEEDHACK);
            if (distance > 6.0) {
                options.add(Attack.SPEEDHACK);
            }
        }
        if (level.getEntitiesOfClass(GlitchCloneEntity.class, this.getBoundingBox().inflate(40.0)).size() > 2) {
            options.removeIf(a -> a == Attack.CLONES);
        }
        options.removeIf(a -> a == this.lastAttack);
        return options.get(this.random.nextInt(options.size()));
    }

    // ------------------------------------------------------------------ Laden und Gehen

    /**
     * Laden: Pixel fliegen von allen Seiten herbei und kreisen um den Kern, „Lädt …“ – dann setzt er sich mit einem Ruck
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
            this.streamPixels(level, c, 6);
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

    /** Verbindung getrennt: Er flackert, zerfällt Stück für Stück in Pixel und ist fort; der Kern wartet wieder. */
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
     * Lag-Hieb: Er holt aus – und hängt fest, über seinem Kopf dreht sich ein Ladekreis (mal kurz, mal lang). Verschwindet
     * der, kracht der Hammer zwei Ticks später herab: Wer vor ihm steht, wird schwer getroffen, und eine Schockwelle läuft
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
            int lag = this.isVersion2() ? 2 + this.random.nextInt(7) : this.isGlitchMode() ? 3 + this.random.nextInt(9) : 5 + this.random.nextInt(14);
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
            if (this.wave <= (this.isVersion2() ? 9.5 : 7.0)) {
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
     * Seine Lebenskraft, wie sie gedacht ist – ohne die Obergrenze, die Minecraft der Lebensleiste setzt (1024): Grundwert
     * mit allen Zuschlägen (etwa der Härte von Kingdoms at War und der kürzeren Leiste von Anni 2.0).
     */
    public double designedMaxHealth() {
        AttributeInstance health = this.getAttribute(Attributes.MAX_HEALTH);
        if (health == null) {
            return this.getMaxHealth();
        }
        double add = health.getBaseValue();
        double base = 0.0;
        double total = 1.0;
        for (AttributeModifier modifier : health.getModifiers()) {
            switch (modifier.operation()) {
                case ADD_VALUE -> add += modifier.amount();
                case ADD_MULTIPLIED_BASE -> base += modifier.amount();
                case ADD_MULTIPLIED_TOTAL -> total *= 1.0 + modifier.amount();
            }
        }
        return Math.max(1.0, add * (1.0 + base) * total);
    }

    /** Um wie viel ein Treffer zählt: die echte Leiste geteilt durch die gedachte (1, solange sie hineinpasst). */
    public float healthScale() {
        return (float) Math.min(1.0, this.getMaxHealth() / this.designedMaxHealth());
    }

    /** Wie oft er beim Gummiband springt (im Fehlermodus einmal mehr, als Anni 2.0 zweimal mehr). */
    public static int hops(boolean glitchMode, boolean version2) {
        return RUBBER_HOPS + (version2 ? 2 : glitchMode ? 1 : 0);
    }

    /**
     * Gummiband: Er flackert und ist fort – und steht woanders, drei-, im Fehlermodus viermal, immer um sein Ziel herum
     * (Nachbilder bleiben stehen). Dann taucht er direkt neben ihm auf und schlägt zu.
     */
    private void tickRubberband(ServerLevel level, LivingEntity target) {
        int t = this.attackTick;
        int hopsTotal = hops(this.isGlitchMode(), this.isVersion2());
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
     * Fehler-Klone: Er schüttelt sich, die Farben laufen auseinander – und plötzlich sind da drei (im Fehlermodus fünf)
     * Kopien von ihm, rot, grün und blau. Sie rennen los und „stürzen ab“, wenn sie jemanden erreichen; ein Treffer lässt
     * eine Kopie zerplatzen.
     */
    private void tickClones(ServerLevel level, LivingEntity target) {
        int t = this.attackTick;
        this.faceTarget(target, 10.0f);
        if (t == 2) {
            this.sound(level, ModSounds.ANNI_CLONE.get(), 2.5f, 1.0f);
        }
        if (t == CLONES_SPLIT) {
            int count = this.isVersion2() ? 6 : this.isGlitchMode() ? 5 : 3;
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
     * Fehlende Textur: Er hebt den Hammer mit beiden Händen und rammt ihn in den Boden – unter seinem Ziel (im Fehlermodus
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
            if (this.isVersion2()) {
                // Anni 2.0: ein drittes Feld – dort, wohin sein Ziel gerade läuft
                Vec3 lead = target.position().add(target.getDeltaMovement().multiply(30.0, 0.0, 30.0));
                spots.add(this.ground(lead.distanceToSqr(target.position()) < 16.0 ? target.position().add(this.forward().scale(-8.0)) : lead));
            }
            List<MissingZoneEntity> zones = level.getEntitiesOfClass(MissingZoneEntity.class, this.getBoundingBox().inflate(48.0));
            for (int i = 0; i < zones.size() - 3; i++) {
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
     * Pixelregen: Er reckt den Hammer zum Himmel – leuchtende Pixel stürzen herab, auf Stellen, die ein Ring am Boden
     * zeigt (meist dort, wo seine Gegner gleich sein werden).
     */
    private void tickPixelRain(ServerLevel level, LivingEntity target) {
        int t = this.attackTick;
        this.faceTarget(target, 8.0f);
        if (t == 4) {
            this.sound(level, ModSounds.ANNI_RAIN.get(), 3.0f, 1.0f);
        }
        int every = this.isGlitchMode() ? 2 : 3;
        if (t >= RAIN_START && t <= RAIN_END && t % every == 0) {
            this.dropPixel(level);
            if (this.isVersion2() && this.random.nextInt(5) < 2) {
                this.dropPixel(level);
            }
        }
    }

    /** Ein Pixelblock stürzt herab: meist dorthin, wo gleich jemand sein wird, sonst irgendwo um ihn herum. */
    private void dropPixel(ServerLevel level) {
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

    /**
     * Rückruck: Er hebt den zerfallenen Arm, sein Bildschirmauge flackert – und er merkt sich, wo alle stehen (ein
     * flimmerndes Abbild bleibt dort). Drei Sekunden später reißt es alle dorthin zurück (kurz schwindlig); dann stürmt er
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
            // er stürmt auf die Stelle zu, an die es den Nächsten zurückgerissen hat
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
     * Zeilenfehler: Er stampft auf – eine leuchtende Zeile kreist um ihn, die rote dicht über dem Boden (springen!), dann
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
            if (!low && this.isVersion2()) {
                // Anni 2.0: mit der blauen Zeile kreist zugleich eine zweite rote, gegenläufig (erst springen, dann ducken – oder umgekehrt)
                float second = toTarget + (100.0f + this.random.nextFloat() * 60.0f) * (this.random.nextBoolean() ? 1.0f : -1.0f) + 180.0f;
                level.addFreshEntity(new ScanlineEntity(level, this, this.ground(this.position()), 0.45f, true, second, -speed * 0.85f,
                        this.scaled(12.0f)));
            }
        }
    }

    /** Troll-Lachen: Er wirft den Kopf zurück und lacht (eine kurze Pause). */
    private void tickLaugh(ServerLevel level, LivingEntity target) {
        this.faceTarget(target, 8.0f);
        if (this.attackTick == 3) {
            this.sound(level, ModSounds.ANNI_LAUGH.get(), 3.0f, 0.95f + this.random.nextFloat() * 0.1f);
        }
    }

    /**
     * Absturz: „Anni.exe reagiert nicht.“ Er erstarrt in einer verdrehten Haltung, flackert und ist nicht zu treffen. Dann
     * „wird neu gestartet“ – er richtet sich auf, und eine Welle aus Pixeln rollt über den Boden (springen!). Danach kämpft
     * er im Fehlermodus weiter.
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
        this.rollWave(level, 16.0, 18.0f);
        if (t == CRASH_WAVE + 10) {
            this.sound(level, ModSounds.ANNI_LAUGH.get(), 4.0f, 0.8f);
        }
    }

    /** Die Welle aus Pixeln rollt von ihm aus über den Boden bis zum Halbmesser maxRadius (wer springt, entgeht ihr). */
    private void rollWave(ServerLevel level, double maxRadius, float damage) {
        if (this.wave <= 0.0) {
            return;
        }
        double r0 = this.wave;
        this.wave += 0.65;
        Vec3 c = this.position();
        this.ring(level, c, this.wave, 44);
        for (LivingEntity victim : this.victims(level, this.getBoundingBox().inflate(this.wave + 1.0, 3.0, this.wave + 1.0))) {
            double d = Math.sqrt(victim.distanceToSqr(c.x, victim.getY(), c.z));
            if (d >= r0 - 0.6 && d < this.wave + 0.6 && victim.onGround() && victim.getY() < c.y + 2.0 && this.struck.add(victim.getUUID())) {
                victim.hurtServer(level, this.damageSources().indirectMagic(this, this), damage);
                push(victim, victim.position().subtract(c).multiply(1.0, 0.0, 1.0).normalize().scale(0.8).add(0.0, 0.9, 0.0));
            }
        }
        if (this.wave > maxRadius) {
            this.wave = -1.0;
        }
    }

    /**
     * Wiederherstellung – statt zu sterben: „Schwerer Ausnahmefehler“. Er sackt zusammen, erstarrt und zerfällt in Pixel.
     * „Suche nach einer Sicherung …“ – „Sicherung gefunden“: Die Pixel strömen von allen Seiten zurück, seine Leiste füllt
     * sich Stück für Stück (bis dahin ist er nicht zu treffen). Dann setzt er sich als Anni 2.0 zusammen – rote Augen, ein
     * neuer Name über der Leiste –, schlägt den Hammer in den Boden (eine Welle rollt los: springen!) und lacht.
     */
    private void tickRevive(ServerLevel level) {
        int t = this.attackTick;
        this.getNavigation().stop();
        this.setDeltaMovement(this.getDeltaMovement().multiply(0.0, 1.0, 0.0));
        Vec3 c = this.position().add(0.0, 2.0, 0.0);
        if (t == 1) {
            this.sound(level, ModSounds.ANNI_REVIVE.get(), 4.0f, 1.0f);
            this.tell(level, Component.translatable("entity.kingdomsatwar.anni_troll.fatal").withColor(0xFF3030));
        }
        if (t < REVIVE_SHATTER && t % 3 == 0) {
            double k = t / (double) REVIVE_SHATTER;
            level.sendParticles(ModParticles.GLITCH_PIXEL.get(), this.getX(), this.getY(0.2 + k * 0.8), this.getZ(), 4 + (int) (k * 12), 0.9, 0.5,
                    0.9, 0.08);
        }
        if (t == REVIVE_SHATTER) {
            this.setHidden(true);
            this.pixelBurst(level, c, 180, 1.6);
            level.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, 0xFFFF2040), c.x, c.y, c.z, 2, 0.3, 0.3, 0.3, 0.0);
            this.tell(level, Component.translatable("entity.kingdomsatwar.anni_troll.searching").withColor(0xA0A0A0));
            AttributeInstance health = this.getAttribute(Attributes.MAX_HEALTH);
            if (health != null && !health.hasModifier(BACKUP_HEALTH_ID)) {
                health.addPermanentModifier(new AttributeModifier(BACKUP_HEALTH_ID, BACKUP_HEALTH - 1.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            }
            this.setHealth(1.0f);
        }
        if (t > REVIVE_SHATTER && t < REVIVE_ASSEMBLE && t % 2 == 0) {
            this.streamPixels(level, c, t > REVIVE_FOUND ? 8 : 3);
        }
        if (t == REVIVE_FOUND) {
            this.tell(level, Component.translatable("entity.kingdomsatwar.anni_troll.found").withColor(0x20E8FF));
        }
        if (t >= REVIVE_FILL && t <= REVIVE_FILLED) {
            float k = (t - REVIVE_FILL) / (float) (REVIVE_FILLED - REVIVE_FILL);
            this.setHealth(Math.max(1.0f, this.getMaxHealth() * k));
            if ((t - REVIVE_FILL) % 8 == 0) {
                int percent = Math.round(k * 100.0f);
                this.tell(level, Component.translatable("entity.kingdomsatwar.anni_troll.restoring", bar(k) + " " + percent + " %").withColor(0x20E8FF));
            }
        }
        if (t == REVIVE_ASSEMBLE) {
            this.becomeVersion2();
            this.setHealth(this.getMaxHealth());
            this.setHidden(false);
            this.sound(level, ModSounds.ANNI_TELEPORT.get(), 3.0f, 0.6f);
            level.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, 0xFFFF2040), c.x, c.y, c.z, 2, 0.2, 0.2, 0.2, 0.0);
            this.pixelBurst(level, c, 120, 1.5);
            Component title = Component.translatable("boss.kingdomsatwar.anni2").withColor(0xFF2040);
            Component subtitle = Component.translatable("boss.kingdomsatwar.anni2.title").withStyle(ChatFormatting.GRAY);
            for (ServerPlayer player : level.players()) {
                if (player.distanceToSqr(this) < 64.0 * 64.0) {
                    player.connection.send(new ClientboundSetTitlesAnimationPacket(6, 60, 20));
                    player.connection.send(new ClientboundSetTitleTextPacket(title));
                    player.connection.send(new ClientboundSetSubtitleTextPacket(subtitle));
                }
            }
        }
        if (t == REVIVE_WAVE) {
            this.sound(level, ModSounds.ANNI_SMASH.get(), 4.0f, 0.55f);
            level.sendParticles(ParticleTypes.EXPLOSION, this.getX(), this.getY() + 0.5, this.getZ(), 3, 1.2, 0.2, 1.2, 0.0);
            this.wave = 1.5;
            this.struck.clear();
        }
        this.rollWave(level, 18.0, 20.0f);
        if (t == REVIVE_WAVE + 12) {
            this.sound(level, ModSounds.ANNI_LAUGH.get(), 4.0f, 0.75f);
        }
        Player nearest = level.getNearestPlayer(this, 40.0);
        if (nearest != null && t > REVIVE_ASSEMBLE) {
            this.getLookControl().setLookAt(nearest, 20.0f, 20.0f);
            this.turnTo((float) (Mth.atan2(nearest.getZ() - this.getZ(), nearest.getX() - this.getX()) * Mth.RAD_TO_DEG) - 90.0f, 10.0f);
        }
    }

    /** Ein Ladebalken aus zehn Kästchen. */
    private static String bar(float k) {
        int full = Mth.clamp(Math.round(k * 10.0f), 0, 10);
        return "█".repeat(full) + "░".repeat(10 - full);
    }

    /** Pixel strömen von allen Seiten auf ihn zu (beim Laden und bei der Wiederherstellung). */
    private void streamPixels(ServerLevel level, Vec3 c, int count) {
        for (int i = 0; i < count; i++) {
            double a = this.random.nextDouble() * Math.PI * 2.0;
            double r = 6.0 + this.random.nextDouble() * 6.0;
            double y = this.random.nextDouble() * 5.0 - 1.0;
            Vec3 from = c.add(Math.cos(a) * r, y, Math.sin(a) * r);
            Vec3 v = c.subtract(from).scale(0.09);
            level.sendParticles(ModParticles.GLITCH_PIXEL.get(), from.x, from.y, from.z, 0, v.x, v.y, v.z, 1.0);
        }
    }

    /** Eine Zeile über der Schnellleiste für alle in der Nähe. */
    private void tell(ServerLevel level, Component message) {
        for (ServerPlayer player : level.players()) {
            if (player.distanceToSqr(this) < 64.0 * 64.0) {
                player.sendOverlayMessage(message);
            }
        }
    }

    /**
     * Speedhack (nur Anni 2.0): Er zielt – ein Ladekreis dreht sich über ihm, eine Spur aus Farbpunkten zeigt, wohin –,
     * dann rast er geradeaus los, schneller als jedes Pferd, bis er gegen etwas prallt. Wen er erwischt, den schleudert
     * er zur Seite; Nachbilder und kleine Felder fehlender Textur bleiben auf seiner Bahn zurück. Dreimal hintereinander.
     */
    private void tickSpeedhack(ServerLevel level, LivingEntity target) {
        int t = this.attackTick;
        if (t < HACK_FIRST) {
            this.faceTarget(target, 20.0f);
            return;
        }
        int k = (t - HACK_FIRST) / HACK_STEP;
        int p = (t - HACK_FIRST) % HACK_STEP;
        if (k >= HACK_DASHES) {
            if (t == HACK_FIRST + HACK_DASHES * HACK_STEP + 2) {
                this.sound(level, ModSounds.ANNI_LAUGH.get(), 2.5f, 1.15f);
            }
            this.faceTarget(target, 10.0f);
            return;
        }
        if (p == 0) {
            this.setLagging(true);
            this.sound(level, ModSounds.ANNI_LAG.get(), 1.6f, 1.2f);
            this.struck.clear();
        }
        if (p < HACK_AIM) {
            this.faceTarget(target, 40.0f);
            Vec3 to = target.position().subtract(this.position()).multiply(1.0, 0.0, 1.0);
            this.dash = to.lengthSqr() < 1.0e-4 ? this.forward() : to.normalize();
            if (p % 2 == 0) {
                for (int i = 2; i <= 14; i += 2) {
                    Vec3 at = this.ground(this.position().add(this.dash.scale(i)));
                    level.sendParticles(i % 4 == 0 ? RED : CYAN, at.x, at.y + 0.3, at.z, 1, 0.12, 0.05, 0.12, 0.0);
                }
            }
            return;
        }
        if (p == HACK_AIM) {
            this.setLagging(false);
            this.sound(level, ModSounds.ANNI_TELEPORT.get(), 2.5f, 0.6f);
            this.sound(level, ModSounds.ANNI_WINDUP.get(), 2.0f, 1.4f);
        }
        if (p < HACK_AIM + HACK_DASH) {
            Vec3 v = this.dash.scale(1.15);
            this.setDeltaMovement(v.x, this.getDeltaMovement().y, v.z);
            this.faceTowards(this.position().add(this.dash), 360.0f);
            if (p % 2 == 0) {
                level.addFreshEntity(AfterimageEntity.ofTroll(level, this, 10));
            }
            if (p % 4 == 1) {
                level.addFreshEntity(new MissingZoneEntity(level, this, this.ground(this.position()), 1, this.scaled(3.0f)));
            }
            Vec3 side = new Vec3(-this.dash.z, 0.0, this.dash.x);
            for (LivingEntity victim : this.victims(level, this.getBoundingBox().inflate(1.2, 0.5, 1.2))) {
                if (this.struck.add(victim.getUUID()) && victim.hurtServer(level, this.damageSources().mobAttack(this), this.scaled(15.0f))) {
                    double sign = side.dot(victim.position().subtract(this.position())) < 0.0 ? -1.0 : 1.0;
                    push(victim, side.scale(1.2 * sign).add(this.dash.scale(0.5)).add(0.0, 0.55, 0.0));
                    this.pixelBurst(level, victim.position().add(0.0, 1.0, 0.0), 24, 0.6);
                }
            }
            if (this.horizontalCollision && p > HACK_AIM + 1) {
                // gegen etwas geprallt: ein Stoß, Pixel – und dieser Ansturm ist vorbei
                this.sound(level, ModSounds.ANNI_SMASH.get(), 2.0f, 1.3f);
                this.pixelBurst(level, this.position().add(this.dash.scale(1.4)).add(0.0, 1.6, 0.0), 30, 0.8);
                this.setDeltaMovement(0.0, this.getDeltaMovement().y, 0.0);
                this.attackTick = HACK_FIRST + k * HACK_STEP + HACK_AIM + HACK_DASH;
            }
            return;
        }
        if (p == HACK_AIM + HACK_DASH) {
            this.setDeltaMovement(this.getDeltaMovement().multiply(0.2, 1.0, 0.2));
        }
        this.faceTarget(target, 12.0f);
    }

    // ------------------------------------------------------------------ Hilfen

    /** Ein flackernder Sprung: ein Nachbild bleibt stehen, Pixel stieben, und er steht woanders. */
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

    /** Wohin er an einer Stelle tritt: auf den Boden (nicht in Blöcke, nicht in die Luft). */
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
        return this.isVersion2() ? damage * 1.4f : this.isGlitchMode() ? damage * 1.2f : damage;
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

    /** Kein Todesklang beim tödlichen Treffer: Ob er wirklich stirbt oder seine Sicherung lädt, entscheidet sich erst in die(). */
    @Override
    protected @Nullable SoundEvent getDeathSound() {
        return null;
    }

    @Override
    protected float getSoundVolume() {
        return 2.5f;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(ModSounds.ANNI_STEP.get(), 1.2f, 0.9f + this.random.nextFloat() * 0.2f);
    }

    /**
     * Beim ersten tödlichen Treffer stirbt er nicht: Er lädt seine Sicherung und kehrt als Anni 2.0 zurück (nicht bei
     * Schaden, der jede Unverwundbarkeit durchschlägt, etwa /kill oder die Leere). Beim zweiten Mal stürzt er endgültig ab:
     * Er erstarrt, flackert, zerfällt in Pixel; der Kern erlischt.
     */
    @Override
    public void die(DamageSource source) {
        if (!this.revived && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY) && this.level() instanceof ServerLevel level
                && !this.isRemoved()) {
            this.revive(level);
            return;
        }
        if (this.level() instanceof ServerLevel level) {
            this.sound(level, ModSounds.ANNI_DEATH.get(), 4.0f, 1.0f);
        }
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

    /** Statt zu sterben: Leben auf eins, Klone und Felder fort – und die Wiederherstellung beginnt. */
    private void revive(ServerLevel level) {
        this.revived = true;
        this.crashed = true;
        this.setHealth(1.0f);
        this.deathTime = 0;
        this.clipTicks = 0;
        this.noPhysics = false;
        this.stutter = 0;
        this.setLagging(false);
        this.getNavigation().stop();
        level.getEntitiesOfClass(GlitchCloneEntity.class, this.getBoundingBox().inflate(48.0), c -> c.isCloneOf(this)).forEach(Entity::discard);
        level.getEntitiesOfClass(MissingZoneEntity.class, this.getBoundingBox().inflate(48.0)).forEach(Entity::discard);
        this.begin(Attack.REVIVE);
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
        output.putBoolean("Revived", this.revived || this.attack == Attack.REVIVE);
        output.putBoolean("Version2", this.isVersion2() || this.attack == Attack.REVIVE);
        output.putBoolean("Reviving", this.attack == Attack.REVIVE);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.home = input.getLong("Home").map(BlockPos::of).orElse(null);
        this.crashed = input.getBooleanOr("Crashed", false);
        this.entityData.set(DATA_GLITCH, input.getBooleanOr("GlitchMode", false));
        this.revived = input.getBooleanOr("Revived", false);
        if (input.getBooleanOr("Version2", false)) {
            this.becomeVersion2();
        }
        if (input.getBooleanOr("Reviving", false)) {
            // mitten in der Wiederherstellung gespeichert: die Sicherung ist nun ganz geladen
            this.setHealth(this.getMaxHealth());
        }
        this.nameBossBar();
    }

    // ------------------------------------------------------------------ Himmelswacht (0.82)

    @Override
    public io.github.sirschrolli.kingdomsatwar.entity.boss.sky.SkyTheme skyTheme() {
        return io.github.sirschrolli.kingdomsatwar.entity.boss.sky.SkyTheme.GLITCH;
    }

    @Override
    public boolean guardsSky() {
        return !this.isHidden() && this.getTarget() != null && this.getTarget().isAlive();
    }
}
