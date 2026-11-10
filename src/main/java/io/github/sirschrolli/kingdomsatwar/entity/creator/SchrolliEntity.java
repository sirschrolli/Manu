package io.github.sirschrolli.kingdomsatwar.entity.creator;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.block.creator.CreatorAltarBlockEntity;
import io.github.sirschrolli.kingdomsatwar.creator.Godslayer;
import io.github.sirschrolli.kingdomsatwar.magic.fx.SpellFxEntity;
import io.github.sirschrolli.kingdomsatwar.registry.ModDamageTypes;
import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * <b>Schrolli der Schöpfer</b> – der stärkste Gegner der Mod: der Schöpfer aller Welten, ein schwebender Seraph aus Licht
 * und Gold mit sechs Schwingen, Heiligenschein, Lichtkrone, Zepter und Weltkugel. Er wird am Altar des Schöpfers gerufen
 * und zieht seine Herausforderer in seinen eigenen Hof, die Schöpfung.
 *
 * <p>Er kämpft in zwei Gestalten, jede mit einer eigenen, vollen Leiste (je 3.200 Lebenspunkte, mehr als jeder andere
 * Gegner – mit jedem weiteren Spieler ein Viertel mehr):
 * <ol>
 *     <li><b>Schrolli der Schöpfer</b> – sechzehn Künste: Lichtspeere, Heiligenschein-Klinge, Säulen des Lichts,
 *     Sternenregen, Zepterschlag, Strahl der Schöpfung, Weltkugel, Federsturm, Göttliches Urteil, Seraphim, Lichtsprung,
 *     Schwingensturm, Sonnenrad, Engelschor, „Es werde Licht!“ und „Die Zeit steht still“;</li>
 *     <li>statt zu sterben zerbricht seine Gestalt, und er steigt als <b>Wahre Gestalt</b> wieder auf (wie Anni 2.0): das
 *     Gewand ein Weltall, schillernde Schwingen, zwei Räder voller Augen um ihn, größer, schneller, härter – mit zehn
 *     weiteren Künsten: Weltenwurf, Supernova, Urknall, Sternbild, Doppelklinge, Jüngstes Gericht, Augenstrahlen,
 *     Kometensturz, Weltenriss und Neuschöpfung.</li>
 * </ol>
 * Sein Licht geht durch jede Rüstung (Verzauberungen schützen weiter). Wer fällt, verliert in seinem Hof alles. Wer ihn
 * besiegt, trägt fortan den Titel „Gottbezwinger“.
 */
public class SchrolliEntity extends Monster implements io.github.sirschrolli.kingdomsatwar.arena.BossLair.Bound {
    /** Lebenskraft jeder der beiden Gestalten (die Leiste von Minecraft fasst nur 1.024 – siehe BossHealth). */
    public static final float DESIGNED_HEALTH = 3200.0f;
    /** Erfahrung beim Sieg (fest, wie bei den anderen großen Gegnern). */
    public static final int XP_REWARD = 1000;
    /** Um so viel wächst er in der Wahren Gestalt. */
    public static final double TRUE_GROWTH = 0.3;
    public static final int AWAKEN_TICKS = 150;
    public static final int AWAKEN_LAND = 92;
    public static final int ASCEND_TICKS = 230;
    /** Aufstieg: hier zerbricht seine Gestalt … */
    public static final int ASCEND_SHATTER = 70;
    /** … hier erscheint die Wahre Gestalt, bis hierhin füllt sich die neue Leiste … */
    public static final int ASCEND_REVEAL = 110;
    public static final int ASCEND_FILLED = 180;
    /** … und hier rollt die Welle los. */
    public static final int ASCEND_BLAST = 190;
    public static final int DEATH_TICKS = 260;
    /** Die Zeit steht still: hier erstarren alle, hier läuft sie weiter (Ticks der Kunst). */
    public static final int TIME_FREEZE = 20;
    public static final int TIME_RESUME = 84;
    /** „Es werde Licht!“: der Blitz. Supernova: der Knall. Urknall: der Knall. */
    public static final int WORD_FLASH = 56;
    public static final int NOVA_BLAST = 140;
    public static final int GENESIS_BANG = 110;
    /** Wie weit (waagerecht) um die Mitte er Gegner sucht. */
    public static final double ARENA_RADIUS = 96.0;

    public enum Attack {
        NONE(0),
        AWAKEN(AWAKEN_TICKS),
        ASCEND(ASCEND_TICKS),
        // ---------------------------------------------------------------- die Künste des Schöpfers
        LANCES(72),
        HALO(90),
        PILLARS(100),
        STARFALL(112),
        SMITE(64),
        SUNBEAM(128),
        ORB(110),
        FEATHERS(80),
        JUDGMENT(100),
        SERAPHS(64),
        BLINK(48),
        GALE(66),
        SUNWHEEL(172),
        CHOIR(108),
        WORD(86),
        TIMESTOP(132),
        // ---------------------------------------------------------------- nur in der Wahren Gestalt
        PLANETS(132),
        SUPERNOVA(178),
        GENESIS(158),
        CONSTELLATION(128),
        TWIN_HALOS(124),
        APOCALYPSE(212),
        EYE_BEAMS(138),
        COMET(118),
        WORLD_SPLIT(100),
        RECREATION(172),
        // ---------------------------------------------------------------- Zustände
        STUNNED(80),
        EXIT(64);

        public final int duration;

        Attack(int duration) {
            this.duration = duration;
        }

        /** Gehört diese Kunst nur der Wahren Gestalt? */
        public boolean trueOnly() {
            return this.ordinal() >= PLANETS.ordinal() && this.ordinal() <= RECREATION.ordinal();
        }

        public boolean isArt() {
            return this.ordinal() >= LANCES.ordinal() && this.ordinal() <= RECREATION.ordinal();
        }
    }

    public static final int FLAG_HALO = 1;
    public static final int FLAG_ORB = 2;
    public static final int FLAG_HIDDEN = 4;
    public static final int FLAG_CHANNEL = 8;
    public static final int FLAG_STUN = 16;
    public static final int FLAG_GLARE = 32;

    private static final EntityDataAccessor<Integer> DATA_ATTACK = SynchedEntityData.defineId(SchrolliEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_TRUE = SynchedEntityData.defineId(SchrolliEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Byte> DATA_FLAGS = SynchedEntityData.defineId(SchrolliEntity.class, EntityDataSerializers.BYTE);
    private static final Identifier GROWTH_ID = KingdomsAtWar.id("schrolli_true_form");
    private static final Identifier PARTY_ID = KingdomsAtWar.id("schrolli_party");

    private final ServerBossEvent bossBar = new ServerBossEvent(UUID.randomUUID(), Component.translatable("boss.kingdomsatwar.schrolli"),
            BossEvent.BossBarColor.YELLOW, BossEvent.BossBarOverlay.NOTCHED_20);
    Attack attack = Attack.NONE;
    int attackTick;
    int cooldown = 40;
    private int idleTicks;
    private final Deque<Attack> recent = new ArrayDeque<>();
    private int sinceTimestop = 99;
    private int sinceGenesis = 99;
    /** Der Altar, an den er gebunden ist, und die Mitte seines Hofs. */
    private @Nullable BlockPos altar;
    private @Nullable Vec3 center;
    /** Ist er schon (oder gerade) in die Wahre Gestalt aufgestiegen? Dann stirbt er beim nächsten Mal wirklich. */
    private boolean ascended;
    private boolean said70;
    private boolean said40;
    private boolean said50True;
    boolean novaDone;
    boolean apocalypseDone;
    boolean recreationDone;
    float channelDamage;
    private int targetSwitch;
    final List<UUID> minions = new ArrayList<>();

    // ------------------------------------------------------------------ Zustand der laufenden Kunst (für SchrolliAttacks)
    Vec3 mark = Vec3.ZERO;
    Vec3 aim = Vec3.ZERO;
    double sweep;
    double sweepBase;
    double sweepDir = 1.0;
    final List<Integer> ids = new ArrayList<>();
    final List<DivineShotEntity> held = new ArrayList<>();
    final List<Vec3> marks = new ArrayList<>();
    final List<Vec3> points = new ArrayList<>();
    final List<SpellFxEntity> beams = new ArrayList<>();
    final Map<UUID, Integer> hitCooldown = new HashMap<>();
    final Set<UUID> struck = new HashSet<>();
    @Nullable UUID marked;
    @Nullable SpellFxEntity markFx;
    @Nullable DivineShotEntity sun;

    // ------------------------------------------------------------------ nur im Client
    private int clientAttackStart;
    public float wingO;
    public float wing;

    public SchrolliEntity(EntityType<? extends SchrolliEntity> type, Level level) {
        super(type, level);
        this.xpReward = 0;
        this.setPersistenceRequired();
        this.setNoGravity(true);
        this.noPhysics = true;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, DESIGNED_HEALTH)
                .add(Attributes.ARMOR, 24.0)
                .add(Attributes.ARMOR_TOUGHNESS, 16.0)
                .add(Attributes.ATTACK_DAMAGE, 22.0)
                .add(Attributes.MOVEMENT_SPEED, 0.32)
                .add(Attributes.FLYING_SPEED, 0.6)
                .add(Attributes.FOLLOW_RANGE, 128.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(Attributes.GRAVITY, 0.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ATTACK, 0);
        builder.define(DATA_TRUE, false);
        builder.define(DATA_FLAGS, (byte) 0);
    }

    // ------------------------------------------------------------------ Zustand

    public Attack currentAttack() {
        int i = this.entityData.get(DATA_ATTACK);
        return i >= 0 && i < Attack.values().length ? Attack.values()[i] : Attack.NONE;
    }

    /** Die Wahre Gestalt (nach dem Aufstieg). */
    public boolean isTrueForm() {
        return this.entityData.get(DATA_TRUE);
    }

    public boolean hasFlag(int flag) {
        return (this.entityData.get(DATA_FLAGS) & flag) != 0;
    }

    void setFlag(int flag, boolean on) {
        byte b = this.entityData.get(DATA_FLAGS);
        byte next = (byte) (on ? b | flag : b & ~flag);
        if (next != b) {
            this.entityData.set(DATA_FLAGS, next);
        }
    }

    /** Wie lange die laufende Kunst schon dauert (im Client, für die Animationen). */
    public float attackTime(float partialTicks) {
        return this.tickCount - this.clientAttackStart + partialTicks;
    }

    /** Wie weit der Aufstieg in die Wahre Gestalt ist (0 – 1; auch im Client). */
    public float ascension(float partialTicks) {
        if (this.currentAttack() == Attack.ASCEND) {
            return Mth.clamp(this.attackTime(partialTicks) / ASCEND_REVEAL, 0.0f, 1.0f);
        }
        return this.isTrueForm() ? 1.0f : 0.0f;
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (DATA_ATTACK.equals(key)) {
            this.clientAttackStart = this.tickCount;
        }
        if (DATA_TRUE.equals(key)) {
            this.refreshDimensions();
        }
    }

    @Override
    public @Nullable BlockPos lairPos() {
        return this.altar;
    }

    public @Nullable BlockPos altar() {
        return this.altar;
    }

    /** Bindet ihn an seinen Altar; {@code center} ist die Mitte seines Hofs (dort kämpft er). */
    public void bindTo(@Nullable BlockPos altar, Vec3 center) {
        this.altar = altar == null ? null : altar.immutable();
        this.center = center;
    }

    /** Die Mitte seines Hofs (der Boden). */
    public Vec3 center() {
        if (this.center == null) {
            this.center = this.ground(this.position());
        }
        return this.center;
    }

    /** Mit mehr Herausforderern wird er stärker: ein Viertel mehr Lebenskraft je weiterem Spieler. */
    public void scaleForChallengers(int players) {
        AttributeInstance health = this.getAttribute(Attributes.MAX_HEALTH);
        if (health != null) {
            health.removeModifier(PARTY_ID);
            if (players > 1) {
                health.addPermanentModifier(new AttributeModifier(PARTY_ID, 0.25 * (players - 1), AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
            }
            this.setHealth(this.getMaxHealth());
        }
    }

    /** Er erscheint: in einer Säule aus Licht steigt er hoch über seinem Hof herab (bis dahin unverwundbar). */
    public void awaken() {
        this.begin(Attack.AWAKEN);
    }

    /** Beginnt sofort eine bestimmte Kunst (für Tests und die Vorführung). */
    public void forceAttack(Attack next) {
        this.begin(next);
    }

    /** Sofort in der Wahren Gestalt mit voller Leiste (für Tests und die Vorführung). */
    public void forceTrueForm() {
        this.ascended = true;
        this.becomeTrue();
        this.setHealth(this.getMaxHealth());
    }

    /** Die Wahre Gestalt: größer, neue Leiste, neuer Name. */
    void becomeTrue() {
        this.entityData.set(DATA_TRUE, true);
        AttributeInstance scale = this.getAttribute(Attributes.SCALE);
        if (scale != null && !scale.hasModifier(GROWTH_ID)) {
            scale.addPermanentModifier(new AttributeModifier(GROWTH_ID, TRUE_GROWTH, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        }
        this.refreshDimensions();
        this.nameBossBar();
    }

    private void nameBossBar() {
        if (this.hasCustomName()) {
            this.bossBar.setName(this.getDisplayName());
        } else {
            this.bossBar.setName(Component.translatable(this.isTrueForm() ? "boss.kingdomsatwar.schrolli_true" : "boss.kingdomsatwar.schrolli"));
        }
        this.bossBar.setColor(this.isTrueForm() ? BossEvent.BossBarColor.PURPLE : BossEvent.BossBarColor.YELLOW);
    }

    void begin(Attack next) {
        Attack previous = this.attack;
        this.attack = next;
        this.attackTick = 0;
        this.entityData.set(DATA_ATTACK, next.ordinal());
        if (next.isArt()) {
            this.recent.addFirst(next);
            while (this.recent.size() > 4) {
                this.recent.removeLast();
            }
            this.sinceTimestop = next == Attack.TIMESTOP ? 0 : this.sinceTimestop + 1;
            this.sinceGenesis = next == Attack.GENESIS ? 0 : this.sinceGenesis + 1;
        }
        // was die vorige Kunst zurückließ (Strahlen, Markierungen), endet mit ihr
        if (previous != next) {
            this.clearArtState();
        }
        this.setFlag(FLAG_HIDDEN, false);
        this.setFlag(FLAG_CHANNEL, false);
        this.setFlag(FLAG_GLARE, false);
        if (next != Attack.STUNNED) {
            this.setFlag(FLAG_STUN, false);
        }
    }

    /** Räumt den Zustand der laufenden Kunst ab (Strahlen verlöschen, Markierungen verschwinden). */
    void clearArtState() {
        for (SpellFxEntity fx : this.beams) {
            if (!fx.isRemoved()) {
                fx.discard();
            }
        }
        this.beams.clear();
        if (this.markFx != null && !this.markFx.isRemoved()) {
            this.markFx.discard();
        }
        this.markFx = null;
        if (this.sun != null && !this.sun.isRemoved()) {
            this.sun.discard();
        }
        this.sun = null;
        // Welten, die noch über ihm kreisen, vergehen
        for (DivineShotEntity shot : this.held) {
            if (!shot.isRemoved() && shot.state() == DivineShotEntity.HOLD) {
                shot.discard();
            }
        }
        this.held.clear();
        this.ids.clear();
        this.marked = null;
        this.marks.clear();
        this.points.clear();
        this.hitCooldown.clear();
        this.struck.clear();
        this.sweep = 0.0;
    }

    // ------------------------------------------------------------------ Grundverhalten

    @Override
    public void travel(Vec3 input) {
        this.move(MoverType.SELF, this.getDeltaMovement());
        this.setDeltaMovement(this.getDeltaMovement().scale(0.88));
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
    public boolean canBeAffected(net.minecraft.world.effect.MobEffectInstance effect) {
        // weder Gift noch Schwäche noch Starre: nur das Leuchten wirkt auf ihn
        return effect.getEffect().is(net.minecraft.world.effect.MobEffects.GLOWING);
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

    @Override
    public void setCustomName(@Nullable Component name) {
        super.setCustomName(name);
        this.nameBossBar();
    }

    /** Unverwundbar beim Erscheinen, Aufsteigen, Gehen, im Lichtsprung und solange die Zeit stillsteht. */
    boolean untouchable() {
        Attack a = this.attack;
        return a == Attack.AWAKEN || a == Attack.ASCEND || a == Attack.EXIT || this.hasFlag(FLAG_HIDDEN)
                || a == Attack.TIMESTOP && this.attackTick >= SchrolliAttacks.TIME_FREEZE && this.attackTick < SchrolliAttacks.TIME_RESUME;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return super.hurtServer(level, source, amount);
        }
        if (this.untouchable() || this.isDeadOrDying() || source.is(DamageTypes.IN_WALL) || source.is(DamageTypes.FALL)
                || source.is(DamageTypes.FLY_INTO_WALL) || source.is(DamageTypes.CRAMMING) || source.is(DamageTypes.DROWN)
                || source.is(ModDamageTypes.DIVINE) || source.getEntity() instanceof SeraphlingEntity || source.getEntity() == this) {
            if (this.untouchable() && source.getEntity() instanceof Player) {
                DivineFx.sparks(level, this.position().add(0.0, this.getBbHeight() * 0.5, 0.0), 6, 0.6, 0.05);
            }
            return false;
        }
        // die Räder der Wahren Gestalt lenken manches Geschoss ab
        if (this.isTrueForm() && source.getDirectEntity() instanceof Projectile shot && this.random.nextFloat() < 0.15f) {
            this.deflect(level, shot);
            return false;
        }
        // kein Treffer zählt mehr als ein Zwanzigstel seiner Kraft (gegen Waffen, die alles auf einmal fällen)
        amount = Math.min(amount, DESIGNED_HEALTH * 0.05f);
        if (this.attack == Attack.STUNNED) {
            amount *= 1.5f;
        }
        if (this.attack == Attack.RECREATION) {
            this.channelDamage += amount;
        }
        boolean hurt = super.hurtServer(level, source, amount);
        if (hurt && source.getEntity() instanceof LivingEntity attacker && attacker != this && this.canStrike(attacker)
                && !(this.getTarget() instanceof Player) && attacker instanceof Player) {
            this.setTarget(attacker);
        }
        return hurt;
    }

    /** Ein Geschoss prallt an den Rädern ab und fliegt zurück. */
    private void deflect(ServerLevel level, Projectile shot) {
        Vec3 v = shot.getDeltaMovement();
        shot.setDeltaMovement(v.scale(-0.8).add(0.0, 0.2, 0.0));
        shot.setOwner(this);
        shot.needsSync = true;
        DivineFx.sparks(level, shot.position(), 12, 0.3, 0.1);
        this.sound(level, net.minecraft.sounds.SoundEvents.AMETHYST_BLOCK_RESONATE, 1.6f, 1.6f);
    }

    // ------------------------------------------------------------------ Takt

    @Override
    public void tick() {
        this.noPhysics = true;
        this.setNoGravity(true);
        super.tick();
        if (this.level().isClientSide()) {
            this.wingO = this.wing;
            Attack a = this.currentAttack();
            float rate = 0.06f + (float) Math.min(0.08, this.getDeltaMovement().length() * 0.08);
            if (a == Attack.GALE || a == Attack.COMET || a == Attack.FEATHERS) {
                rate += 0.08f;
            }
            if (this.deathTime > 0) {
                rate *= Math.max(0.0f, 1.0f - this.deathTime / (float) DEATH_TICKS);
            }
            this.wing += rate;
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide()) {
            this.clientEffects();
        }
    }

    /** Licht rieselt von den Schwingen; die Wahre Gestalt umgibt ein Schimmer aus Sternen. */
    private void clientEffects() {
        Level level = this.level();
        if (this.hasFlag(FLAG_HIDDEN) || this.deathTime > 0) {
            return;
        }
        float s = this.getScale();
        float yaw = this.yBodyRot * Mth.DEG_TO_RAD;
        if (this.random.nextInt(2) == 0) {
            double side = (this.random.nextBoolean() ? 1 : -1) * (1.2 + this.random.nextDouble() * 3.5) * s * 0.5;
            level.addParticle(io.github.sirschrolli.kingdomsatwar.registry.ModParticles.DIVINE_SPARK.get(), this.getX() + Mth.cos(yaw) * side,
                    this.getY() + (2.6 + this.random.nextDouble() * 2.0) * s * 0.5, this.getZ() + Mth.sin(yaw) * side, 0.0, -0.01, 0.0);
        }
        if (this.random.nextInt(9) == 0) {
            level.addParticle(io.github.sirschrolli.kingdomsatwar.registry.ModParticles.DIVINE_FEATHER.get(), this.getRandomX(1.5),
                    this.getY(0.4 + this.random.nextDouble() * 0.4), this.getRandomZ(1.5), 0.0, -0.02, 0.0);
        }
        if (this.isTrueForm() && this.random.nextInt(2) == 0) {
            level.addParticle(this.random.nextBoolean() ? ParticleTypes.END_ROD : ParticleTypes.REVERSE_PORTAL, this.getRandomX(2.2),
                    this.getY(this.random.nextDouble()), this.getRandomZ(2.2), 0.0, 0.0, 0.0);
        }
        // unter ihm: ein Schein aus Licht auf dem Boden
        if (this.tickCount % 3 == 0) {
            Vec3 g = this.ground(this.position());
            if (this.getY() - g.y < 14.0) {
                double a = this.random.nextDouble() * Math.PI * 2.0;
                double r = 1.5 + this.random.nextDouble() * 2.5;
                level.addParticle(ParticleTypes.END_ROD, g.x + Math.cos(a) * r, g.y + 0.1, g.z + Math.sin(a) * r, 0.0, 0.02, 0.0);
            }
        }
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        if (this.attack != Attack.AWAKEN) {
            this.bossBar.setProgress(Mth.clamp(this.getHealth() / this.getMaxHealth(), 0.0f, 1.0f));
        }
        this.minions.removeIf(id -> !(level.getEntity(id) instanceof SeraphlingEntity s) || !s.isAlive());
        this.hitCooldown.replaceAll((id, t) -> t - 1);
        this.hitCooldown.values().removeIf(t -> t <= 0);
        switch (this.attack) {
            case AWAKEN -> {
                this.tickAwaken(level);
                return;
            }
            case ASCEND -> {
                this.tickAscend(level);
                return;
            }
            case EXIT -> {
                this.tickExit(level);
                return;
            }
            case STUNNED -> {
                this.tickStunned(level);
                return;
            }
            default -> {
            }
        }
        this.milestones(level);
        LivingEntity target = this.findTarget(level);
        if (target != null) {
            this.setTarget(target);
        }
        if (this.attack == Attack.NONE) {
            if (target == null) {
                this.idle(level);
                return;
            }
            this.idleTicks = 0;
            this.hover(target, 11.0, 8.0);
            this.face(target.position(), 12.0f);
            Attack forced = this.forcedArt();
            if (forced != null) {
                this.begin(forced);
            } else if (--this.cooldown <= 0) {
                this.begin(this.choose(target));
            }
            return;
        }
        this.attackTick++;
        SchrolliAttacks.tick(this, level, target);
        if (this.attack != Attack.NONE && this.attackTick >= this.attack.duration) {
            this.endArt();
        }
    }

    /** Eine Kunst ist zu Ende: kurze Pause (je schwächer, desto kürzer; in der Wahren Gestalt kaum). */
    void endArt() {
        this.begin(Attack.NONE);
        float k = this.getHealth() / this.getMaxHealth();
        int base = this.isTrueForm() ? 12 : k > 0.7f ? 30 : k > 0.4f ? 22 : 16;
        this.cooldown = base + this.random.nextInt(12);
    }

    /** Ohne Gegner: über der Mitte schweben und sich langsam erholen; draußen in der Welt geht er nach einer Minute. */
    private void idle(ServerLevel level) {
        this.idleTicks++;
        Vec3 c = this.center();
        double a = this.tickCount * 0.01;
        this.steer(c.add(Math.cos(a) * 6.0, 12.0 + Math.sin(this.tickCount * 0.03) * 1.5, Math.sin(a) * 6.0), 0.05, 0.35);
        if (this.idleTicks > 200 && this.tickCount % 20 == 0) {
            this.heal(this.getMaxHealth() * 0.02f);
        }
        if (this.idleTicks > 1200 && !io.github.sirschrolli.kingdomsatwar.arena.BossArenaKind.isArena(level)) {
            this.begin(Attack.EXIT);
        }
    }

    /** Was er an Wendepunkten sagt (bei 70 % und 40 % der ersten Gestalt, bei 50 % der Wahren Gestalt). */
    private void milestones(ServerLevel level) {
        float k = this.getHealth() / this.getMaxHealth();
        if (!this.isTrueForm()) {
            if (!this.said70 && k < 0.7f) {
                this.said70 = true;
                this.say(level, "dust");
            }
            if (!this.said40 && k < 0.4f) {
                this.said40 = true;
                this.say(level, "enough");
            }
        } else if (!this.said50True && k < 0.5f) {
            this.said50True = true;
            this.say(level, "stars");
        }
    }

    /** Künste, die zu ihrer Zeit kommen müssen: Supernova (60 %), Jüngstes Gericht (45 %), Neuschöpfung (25 %). */
    private @Nullable Attack forcedArt() {
        if (!this.isTrueForm()) {
            return null;
        }
        float k = this.getHealth() / this.getMaxHealth();
        if (!this.novaDone && k < 0.6f) {
            this.novaDone = true;
            return Attack.SUPERNOVA;
        }
        if (!this.apocalypseDone && k < 0.45f) {
            this.apocalypseDone = true;
            return Attack.APOCALYPSE;
        }
        if (!this.recreationDone && k < 0.25f) {
            this.recreationDone = true;
            return Attack.RECREATION;
        }
        return null;
    }

    /** Wählt die nächste Kunst: nach Gestalt, Kraft und Abstand gewichtet, nie eine der letzten drei. */
    private Attack choose(LivingEntity target) {
        float k = this.getHealth() / this.getMaxHealth();
        double distance = DivineFx.flat(this.position(), target.position());
        boolean truly = this.isTrueForm();
        List<Attack> pool = new ArrayList<>();
        add(pool, Attack.LANCES, truly ? 2 : 3);
        add(pool, Attack.HALO, 2);
        add(pool, Attack.PILLARS, 2);
        add(pool, Attack.STARFALL, 2);
        add(pool, Attack.SUNBEAM, 2);
        add(pool, Attack.ORB, 2);
        add(pool, Attack.FEATHERS, 2);
        add(pool, Attack.BLINK, 2);
        if (target.onGround() || distance < 20.0) {
            add(pool, Attack.SMITE, 2);
        }
        if (distance < 14.0) {
            add(pool, Attack.GALE, 2);
        }
        if (this.minions.size() < (truly ? 5 : 3)) {
            add(pool, Attack.SERAPHS, 1);
        }
        if (k < 0.7f || truly) {
            add(pool, Attack.JUDGMENT, 2);
            add(pool, Attack.SUNWHEEL, 1);
            add(pool, Attack.CHOIR, 2);
        }
        if (k < 0.4f || truly) {
            add(pool, Attack.WORD, 2);
            if (this.sinceTimestop > 6) {
                add(pool, Attack.TIMESTOP, 1);
            }
        }
        if (truly) {
            add(pool, Attack.PLANETS, 3);
            add(pool, Attack.CONSTELLATION, 2);
            add(pool, Attack.TWIN_HALOS, 2);
            add(pool, Attack.EYE_BEAMS, 2);
            add(pool, Attack.COMET, 2);
            add(pool, Attack.WORLD_SPLIT, 2);
            if (this.sinceGenesis > 5) {
                add(pool, Attack.GENESIS, 1);
            }
        }
        pool.removeIf(a -> this.recent.stream().limit(3).anyMatch(r -> r == a));
        if (pool.isEmpty()) {
            return Attack.LANCES;
        }
        return pool.get(this.random.nextInt(pool.size()));
    }

    private static void add(List<Attack> pool, Attack attack, int weight) {
        for (int i = 0; i < weight; i++) {
            pool.add(attack);
        }
    }

    // ------------------------------------------------------------------ Gegner

    /**
     * Wen seine Künste treffen: Spieler (nicht im Kreativ- oder Zuschauermodus), ihre Soldaten und Begleiter – nie sich
     * selbst, seine Seraphlinge oder Rüstungsständer.
     */
    boolean canStrike(LivingEntity entity) {
        if (entity == this || !entity.isAlive() || entity instanceof SchrolliEntity || entity instanceof SeraphlingEntity
                || entity instanceof ArmorStand) {
            return false;
        }
        if (entity instanceof Player player) {
            return !player.isCreative() && !player.isSpectator();
        }
        if (entity instanceof OwnableEntity pet && pet.getOwnerReference() != null) {
            return true;
        }
        if (entity instanceof io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierEntity) {
            return true;
        }
        return !(entity instanceof Enemy) && entity == this.getTarget();
    }

    /** Alle Gegner im Hof. */
    List<LivingEntity> enemies(ServerLevel level) {
        Vec3 c = this.center();
        return level.getEntitiesOfClass(LivingEntity.class, new AABB(c, c).inflate(ARENA_RADIUS, 96.0, ARENA_RADIUS), this::canStrike);
    }

    /** Gegner in einer Kugel. */
    List<LivingEntity> enemiesNear(ServerLevel level, Vec3 c, double radius) {
        return level.getEntitiesOfClass(LivingEntity.class, new AABB(c, c).inflate(radius), e -> this.canStrike(e)
                && e.position().add(0.0, e.getBbHeight() * 0.5, 0.0).distanceToSqr(c) <= radius * radius);
    }

    /** Nur die Spieler unter den Gegnern. */
    List<ServerPlayer> challengers(ServerLevel level) {
        List<ServerPlayer> out = new ArrayList<>();
        for (LivingEntity e : this.enemies(level)) {
            if (e instanceof ServerPlayer p) {
                out.add(p);
            }
        }
        return out;
    }

    private @Nullable LivingEntity findTarget(ServerLevel level) {
        LivingEntity current = this.getTarget();
        Vec3 c = this.center();
        boolean valid = current != null && current.isAlive() && this.canStrike(current) && DivineFx.flat(current.position(), c) < ARENA_RADIUS
                && Math.abs(current.getY() - c.y) < 80.0;
        // ab und zu ein anderes Ziel, damit jeder etwas abbekommt
        if (valid && --this.targetSwitch > 0) {
            return current;
        }
        this.targetSwitch = 160 + this.random.nextInt(120);
        List<ServerPlayer> players = this.challengers(level);
        if (!players.isEmpty()) {
            return players.get(this.random.nextInt(players.size()));
        }
        List<LivingEntity> all = this.enemies(level);
        return valid ? current : all.isEmpty() ? null : all.get(this.random.nextInt(all.size()));
    }

    /** Ein Treffer: göttliches Licht (geht durch jede Rüstung) oder ein körperlicher Schlag. */
    boolean strike(ServerLevel level, LivingEntity victim, float amount, boolean divine) {
        DamageSource source = divine ? level.damageSources().source(ModDamageTypes.DIVINE, this, this) : this.damageSources().mobAttack(this);
        return victim.hurtServer(level, source, amount * this.power());
    }

    /** In der Wahren Gestalt trifft alles härter. */
    float power() {
        return this.isTrueForm() ? 1.35f : 1.0f;
    }

    /** Stößt ein Wesen von {@code from} fort. */
    static void knock(LivingEntity victim, Vec3 from, double horizontal, double up) {
        Vec3 away = victim.position().subtract(from).multiply(1.0, 0.0, 1.0);
        away = away.lengthSqr() < 1.0e-4 ? new Vec3(1.0, 0.0, 0.0) : away.normalize();
        victim.push(away.x * horizontal, up, away.z * horizontal);
        victim.needsSync = true;
    }

    // ------------------------------------------------------------------ Bewegung

    void steer(Vec3 want, double accel, double max) {
        Vec3 to = want.subtract(this.position());
        Vec3 v = this.getDeltaMovement().add(to.normalize().scale(Math.min(to.length(), 4.0) * accel * 0.25));
        if (v.length() > max) {
            v = v.normalize().scale(max);
        }
        this.setDeltaMovement(v);
    }

    /** Schwebt in {@code radius} Blöcken Abstand und {@code height} Blöcken Höhe um das Ziel. */
    void hover(LivingEntity target, double radius, double height) {
        double a = this.tickCount * (this.isTrueForm() ? 0.022 : 0.016);
        Vec3 g = this.ground(target.position());
        Vec3 want = new Vec3(target.getX() + Math.cos(a) * radius, g.y + height + Math.sin(this.tickCount * 0.05) * 1.2,
                target.getZ() + Math.sin(a) * radius);
        // nicht zu weit vom Hof fort
        Vec3 c = this.center();
        Vec3 off = want.subtract(c);
        double flat = Math.sqrt(off.x * off.x + off.z * off.z);
        if (flat > 60.0) {
            want = new Vec3(c.x + off.x / flat * 60.0, want.y, c.z + off.z / flat * 60.0);
        }
        this.steer(want, 0.06, this.isTrueForm() ? 0.85 : 0.65);
    }

    void face(Vec3 at, float step) {
        this.turnTo((float) DivineFx.yaw(this.position(), at), step);
    }

    void turnTo(float yaw, float step) {
        float next = Mth.approachDegrees(this.getYRot(), yaw, step);
        this.setYRot(next);
        this.yBodyRot = next;
        this.yHeadRot = next;
    }

    /** Waagrecht nach vorn. */
    Vec3 forward() {
        return DivineFx.dir(this.getYRot());
    }

    /** Waagrecht nach rechts (aus seiner Sicht). */
    Vec3 right() {
        float yaw = this.getYRot() * Mth.DEG_TO_RAD;
        return new Vec3(-Mth.cos(yaw), 0.0, -Mth.sin(yaw));
    }

    /** Eine Stelle an seinem Körper (rechts, hoch, vor – in Blöcken bei normaler Größe). */
    Vec3 local(double right, double up, double fwd) {
        double s = this.getScale();
        return this.position().add(this.right().scale(right * s)).add(0.0, up * s, 0.0).add(this.forward().scale(fwd * s));
    }

    /** Der Stern seines Zepters. */
    Vec3 scepterTip() {
        return this.local(0.95, 5.0, 0.8);
    }

    /** Die Weltkugel in seiner Linken. */
    Vec3 orbHand() {
        return this.local(-1.0, 3.4, 1.2);
    }

    /** Seine Augen. */
    Vec3 eyes() {
        return this.local(0.0, 5.55, 0.5);
    }

    /** Der Boden unter einer Stelle (bis 48 Blöcke tief; sonst die Höhe der Mitte). */
    Vec3 ground(Vec3 at) {
        BlockHitResult hit = this.level().clip(new ClipContext(at.add(0.0, 3.0, 0.0), at.add(0.0, -48.0, 0.0), ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE, this));
        if (hit.getType() == HitResult.Type.BLOCK) {
            return hit.getLocation();
        }
        double y = this.center != null ? this.center.y : at.y;
        return new Vec3(at.x, y, at.z);
    }

    void sound(ServerLevel level, SoundEvent sound, float volume, float pitch) {
        level.playSound(null, this.getX(), this.getY() + 3.0, this.getZ(), sound, SoundSource.HOSTILE, volume, pitch);
    }

    /** Eine Zeile Schrollis, für alle im Hof (im Chat, golden), dazu ein Chorakkord. */
    void say(ServerLevel level, String line) {
        Component text = Component.empty()
                .append(Component.translatable("boss.kingdomsatwar.schrolli.speaker").withColor(this.isTrueForm() ? 0xD8A8FF : 0xFFD24A)
                        .withStyle(ChatFormatting.BOLD))
                .append(Component.literal(" "))
                .append(Component.translatable("entity.kingdomsatwar.schrolli.line." + line).withColor(0xFFF2C4).withStyle(ChatFormatting.ITALIC));
        for (ServerPlayer player : level.players()) {
            if (player.distanceToSqr(this) < 160.0 * 160.0) {
                player.sendSystemMessage(text);
            }
        }
        this.sound(level, ModSounds.SCHROLLI_VOICE.get(), 4.0f, this.isTrueForm() ? 0.8f : 1.0f);
    }

    /** Groß auf den Bildschirm aller im Hof. */
    void announce(ServerLevel level, Component title, @Nullable Component subtitle, int stay) {
        for (ServerPlayer player : level.players()) {
            if (player.distanceToSqr(this) < 160.0 * 160.0) {
                player.connection.send(new ClientboundSetTitlesAnimationPacket(8, stay, 20));
                player.connection.send(new ClientboundSetTitleTextPacket(title));
                if (subtitle != null) {
                    player.connection.send(new ClientboundSetSubtitleTextPacket(subtitle));
                }
            }
        }
    }

    /** Eine Zeile über der Schnellleiste für alle im Hof. */
    void tell(ServerLevel level, Component message) {
        for (ServerPlayer player : level.players()) {
            if (player.distanceToSqr(this) < 160.0 * 160.0) {
                player.sendOverlayMessage(message);
            }
        }
    }

    // ------------------------------------------------------------------ Erscheinen, Aufsteigen, Gehen, Taumeln

    /**
     * Erscheinen: In einer Säule aus Licht schwebt er aus dem Himmel herab, entfaltet die Schwingen, hebt das Zepter und
     * spricht. Die Leiste füllt sich dabei.
     */
    private void tickAwaken(ServerLevel level) {
        int t = ++this.attackTick;
        Vec3 c = this.center();
        if (t == 1) {
            this.setPos(c.x, c.y + 44.0, c.z);
            this.sound(level, ModSounds.SCHROLLI_AWAKEN.get(), 6.0f, 1.0f);
            DivineFx.fx(level, SpellFxEntity.Kind.PILLAR, c, DivineFx.HOLY, AWAKEN_LAND + 10, 4.0f).param(70.0f).spawn();
            DivineFx.fx(level, SpellFxEntity.Kind.CIRCLE, c.add(0.0, 0.05, 0.0), DivineFx.GOLD, AWAKEN_LAND, 7.0f).spawn();
            this.say(level, "awaken");
        }
        if (t <= AWAKEN_LAND) {
            float k = t / (float) AWAKEN_LAND;
            float ease = 1.0f - (1.0f - k) * (1.0f - k) * (1.0f - k);
            double y = Mth.lerp(ease, c.y + 44.0, c.y + 9.0);
            this.setPos(c.x, y, c.z);
            this.setDeltaMovement(Vec3.ZERO);
            this.setYRot(this.getYRot() + (1.0f - k) * 6.0f);
            this.yBodyRot = this.getYRot();
            if (t % 2 == 0) {
                DivineFx.gather(level, this.position().add(0.0, 3.0, 0.0), 6.0, 4);
            }
        }
        this.bossBar.setProgress(Mth.clamp((t - 20) / 100.0f, 0.0f, 1.0f));
        if (t == AWAKEN_LAND) {
            this.sound(level, ModSounds.SCHROLLI_BLAST.get(), 5.0f, 0.7f);
            DivineFx.flash(level, this.position().add(0.0, 4.0, 0.0), DivineFx.WHITE);
            DivineFx.sparks(level, this.position().add(0.0, 4.0, 0.0), 120, 3.0, 0.2);
            DivineFx.fx(level, SpellFxEntity.Kind.SHOCKWAVE, c.add(0.0, 0.1, 0.0), DivineFx.GOLD, 40, 2.0f).param(1.0f).spawn();
            this.say(level, "creator");
            this.announce(level, Component.translatable("boss.kingdomsatwar.schrolli").withColor(0xFFD24A).withStyle(ChatFormatting.BOLD),
                    Component.translatable("boss.kingdomsatwar.schrolli.title").withColor(0xFFF2C4), 70);
        }
        Player nearest = level.getNearestPlayer(this, 80.0);
        if (nearest != null && t > 60) {
            this.face(nearest.position(), 6.0f);
        }
        if (t >= AWAKEN_TICKS) {
            this.bossBar.setProgress(1.0f);
            this.begin(Attack.NONE);
            this.cooldown = 20;
        }
    }

    /**
     * Statt zu sterben: Aufstieg in die Wahre Gestalt. Er steigt empor, Licht strömt in ihn, seine Gestalt zerbricht in
     * einem Blitz – aus dem Licht tritt die Wahre Gestalt: größer, das Gewand ein Weltall, die Räder voller Augen. Ihre
     * Leiste füllt sich, dann rollt eine Welle über den Hof (springen!).
     */
    private void tickAscend(ServerLevel level) {
        int t = ++this.attackTick;
        Vec3 c = this.center();
        if (t == 1) {
            this.sound(level, ModSounds.SCHROLLI_ASCEND.get(), 6.0f, 1.0f);
            this.say(level, "shattered");
            this.discardMinions(level);
            this.setFlag(FLAG_HALO, false);
            this.setFlag(FLAG_ORB, false);
        }
        if (t < ASCEND_SHATTER) {
            this.steer(new Vec3(c.x, c.y + 18.0, c.z), 0.05, 0.3);
            if (t % 2 == 0) {
                DivineFx.gather(level, this.position().add(0.0, 3.0, 0.0), 9.0, 6);
            }
            if (t % 10 == 0) {
                DivineFx.flash(level, this.position().add(0.0, 3.0 + this.random.nextDouble() * 2.0, 0.0), DivineFx.HOLY);
                level.sendParticles(ParticleTypes.END_ROD, this.getX(), this.getY(0.5), this.getZ(), 30, 1.2, 1.6, 1.2, 0.15);
            }
        } else {
            this.setDeltaMovement(this.getDeltaMovement().scale(0.5));
        }
        if (t == ASCEND_SHATTER) {
            // die Gestalt zerbricht in Licht: an ihrer Stelle eine kleine Sonne
            this.setFlag(FLAG_HIDDEN, true);
            Vec3 heart = this.position().add(0.0, 3.0, 0.0);
            DivineFx.flash(level, heart, DivineFx.WHITE);
            DivineFx.sparks(level, heart, 300, 4.0, 0.4);
            level.sendParticles(ParticleTypes.END_ROD, heart.x, heart.y, heart.z, 200, 2.0, 2.0, 2.0, 0.5);
            this.sound(level, ModSounds.SCHROLLI_BLAST.get(), 6.0f, 0.6f);
            this.sun = DivineShotEntity.sun(level, this, heart, 1.4f);
        }
        if (t > ASCEND_SHATTER && t < ASCEND_REVEAL) {
            if (this.sun != null) {
                this.sun.setVisualScale(1.4f + (t - ASCEND_SHATTER) / (float) (ASCEND_REVEAL - ASCEND_SHATTER) * 2.4f);
            }
            if (t % 2 == 0) {
                DivineFx.gather(level, this.position().add(0.0, 3.0, 0.0), 14.0, 10);
            }
        }
        if (t == ASCEND_REVEAL) {
            if (this.sun != null) {
                this.sun.discard();
                this.sun = null;
            }
            this.becomeTrue();
            this.setHealth(1.0f);
            this.setFlag(FLAG_HIDDEN, false);
            Vec3 heart = this.position().add(0.0, 3.5, 0.0);
            DivineFx.flash(level, heart, DivineFx.COSMIC);
            DivineFx.flash(level, heart, DivineFx.WHITE);
            level.sendParticles(ParticleTypes.REVERSE_PORTAL, heart.x, heart.y, heart.z, 200, 3.0, 3.0, 3.0, 0.4);
            this.sound(level, ModSounds.SCHROLLI_SUPERNOVA.get(), 5.0f, 1.2f);
            this.say(level, "true_face");
            this.announce(level, Component.translatable("boss.kingdomsatwar.schrolli_true").withColor(0xD8A8FF).withStyle(ChatFormatting.BOLD),
                    Component.translatable("boss.kingdomsatwar.schrolli_true.title").withColor(0xA8E8FF), 80);
        }
        if (t > ASCEND_REVEAL && t <= ASCEND_FILLED) {
            float k = (t - ASCEND_REVEAL) / (float) (ASCEND_FILLED - ASCEND_REVEAL);
            this.setHealth(Math.max(1.0f, this.getMaxHealth() * k));
            if (t % 3 == 0) {
                DivineFx.gather(level, this.position().add(0.0, 4.0, 0.0), 10.0, 5);
            }
        }
        if (t == ASCEND_BLAST) {
            this.setHealth(this.getMaxHealth());
            this.sound(level, ModSounds.SCHROLLI_IMPACT.get(), 6.0f, 0.6f);
            Vec3 g = this.ground(this.position());
            SpellFxEntity wave = DivineFx.fx(level, SpellFxEntity.Kind.SHOCKWAVE, g.add(0.0, 0.1, 0.0), DivineFx.COSMIC, 50, 2.0f).param(0.9f);
            Set<UUID> hit = new HashSet<>();
            wave.onTick(fx -> SchrolliAttacks.ringHits(this, level, fx, g.y, 14.0f, false, hit)).spawn();
            DivineFx.flash(level, g.add(0.0, 1.0, 0.0), DivineFx.COSMIC);
        }
        Player nearest = level.getNearestPlayer(this, 96.0);
        if (nearest != null && t > ASCEND_REVEAL) {
            this.face(nearest.position(), 8.0f);
        }
        if (t >= ASCEND_TICKS) {
            this.begin(Attack.NONE);
            this.cooldown = 16;
        }
    }

    /** Niemand mehr da (außerhalb seines Hofs): Er steigt in einer Säule aus Licht empor und ist fort. */
    private void tickExit(ServerLevel level) {
        int t = ++this.attackTick;
        if (t == 1) {
            this.sound(level, ModSounds.SCHROLLI_ASCEND.get(), 4.0f, 1.4f);
            DivineFx.fx(level, SpellFxEntity.Kind.PILLAR, this.ground(this.position()), DivineFx.HOLY, 60, 3.0f).param(70.0f).spawn();
        }
        this.setDeltaMovement(0.0, 0.25 + t * 0.02, 0.0);
        if (t >= Attack.EXIT.duration) {
            DivineFx.flash(level, this.position().add(0.0, 3.0, 0.0), DivineFx.WHITE);
            this.discardMinions(level);
            if (this.altar != null && level.getBlockEntity(this.altar) instanceof CreatorAltarBlockEntity altarBe && altarBe.owns(this)) {
                altarBe.onSchrolliLeft(level);
            }
            this.discard();
        }
    }

    /** Die Neuschöpfung wurde unterbrochen: er stürzt zu Boden, kniet und ist verwundbarer. */
    private void tickStunned(ServerLevel level) {
        int t = ++this.attackTick;
        Vec3 g = this.ground(this.position());
        if (t == 1) {
            this.setFlag(FLAG_STUN, true);
            this.sound(level, ModSounds.SCHROLLI_STUN.get(), 5.0f, 1.0f);
            this.say(level, "impossible");
            this.tell(level, Component.translatable("entity.kingdomsatwar.schrolli.stunned").withColor(0xFFD24A));
        }
        if (this.getY() > g.y + 0.2) {
            this.setDeltaMovement(0.0, -Math.min(1.2, 0.2 + t * 0.06), 0.0);
        } else {
            this.setDeltaMovement(Vec3.ZERO);
            this.setPos(this.getX(), g.y, this.getZ());
        }
        if (t % 6 == 0) {
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK, this.getX(), this.getY(0.6), this.getZ(), 6, 1.0, 1.0, 1.0, 0.1);
        }
        if (t >= Attack.STUNNED.duration) {
            this.setFlag(FLAG_STUN, false);
            this.endArt();
        }
    }

    void discardMinions(ServerLevel level) {
        for (UUID id : this.minions) {
            if (level.getEntity(id) instanceof SeraphlingEntity s) {
                s.vanish(level);
            }
        }
        this.minions.clear();
        for (DivineShotEntity shot : level.getEntitiesOfClass(DivineShotEntity.class, this.getBoundingBox().inflate(ARENA_RADIUS),
                s -> s.getOwner() == this)) {
            shot.discard();
        }
        for (LivingEntity e : this.enemies(level)) {
            if (TimeFreeze.isFrozen(e)) {
                TimeFreeze.unfreeze(e);
            }
        }
    }

    // ------------------------------------------------------------------ Tod

    @Override
    protected @Nullable SoundEvent getDeathSound() {
        return null;
    }

    /**
     * Beim ersten tödlichen Treffer stirbt er nicht: er steigt in die Wahre Gestalt auf (nicht bei Schaden, der jede
     * Unverwundbarkeit durchschlägt, etwa /kill). Beim zweiten Mal vergeht er wirklich.
     */
    @Override
    public void die(DamageSource source) {
        if (!this.ascended && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY) && this.level() instanceof ServerLevel level
                && !this.isRemoved()) {
            this.ascended = true;
            this.setHealth(1.0f);
            this.deathTime = 0;
            this.clearArtState();
            this.begin(Attack.ASCEND);
            return;
        }
        super.die(source);
        if (this.level() instanceof ServerLevel level) {
            this.clearArtState();
            this.discardMinions(level);
            this.sound(level, ModSounds.SCHROLLI_DEATH.get(), 6.0f, 1.0f);
            this.say(level, "defeated");
            this.bossBar.setProgress(0.0f);
            if (this.altar != null && level.getBlockEntity(this.altar) instanceof CreatorAltarBlockEntity altarBe && altarBe.owns(this)) {
                altarBe.onSchrolliDefeated(level);
            }
        }
    }

    /**
     * Er vergeht: Risse aus Licht brechen aus ihm, die Räder lösen sich, er sinkt langsam herab und spricht ein letztes
     * Mal; dann steigt eine Säule aus Licht in den Himmel, ein gleißender Blitz – und er ist fort. Seine Beute liegt vor
     * dem Thron.
     */
    @Override
    protected void tickDeath() {
        this.deathTime++;
        this.setDeltaMovement(0.0, this.deathTime < 200 ? -0.02 : 0.0, 0.0);
        if (!(this.level() instanceof ServerLevel level)) {
            return;
        }
        int t = this.deathTime;
        Vec3 heart = this.position().add(0.0, this.getBbHeight() * 0.55, 0.0);
        if (t % 8 == 0) {
            Vec3 at = heart.add((this.random.nextDouble() - 0.5) * 3.0, (this.random.nextDouble() - 0.5) * 4.0, (this.random.nextDouble() - 0.5) * 3.0);
            DivineFx.flash(level, at, this.isTrueForm() ? DivineFx.COSMIC : DivineFx.HOLY);
            DivineFx.sparks(level, at, 20, 0.5, 0.15);
            this.sound(level, ModSounds.SCHROLLI_BLAST.get(), 1.5f + t / 100.0f, 1.4f - t / 400.0f);
        }
        if (t % 3 == 0) {
            level.sendParticles(ParticleTypes.END_ROD, heart.x, heart.y, heart.z, 4 + t / 20, 1.2, 1.8, 1.2, 0.1 + t * 0.001);
        }
        if (t == 120) {
            this.say(level, "farewell");
        }
        if (t == 200) {
            DivineFx.fx(level, SpellFxEntity.Kind.PILLAR, this.ground(this.position()), DivineFx.WHITE, 60, 5.0f).param(160.0f).spawn();
            this.sound(level, ModSounds.SCHROLLI_ASCEND.get(), 6.0f, 0.8f);
        }
        if (t > 200 && t % 2 == 0) {
            DivineFx.gather(level, heart, 10.0, 12);
        }
        if (t == DEATH_TICKS - 16) {
            DivineFx.flash(level, heart, DivineFx.WHITE);
            DivineFx.flash(level, heart, DivineFx.GOLD);
            DivineFx.sparks(level, heart, 400, 5.0, 0.6);
            level.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, heart.x, heart.y, heart.z, 160, 2.0, 2.0, 2.0, 0.6);
            this.sound(level, ModSounds.SCHROLLI_SUPERNOVA.get(), 6.0f, 1.4f);
            DivineFx.fx(level, SpellFxEntity.Kind.SHOCKWAVE, this.ground(this.position()).add(0.0, 0.1, 0.0), DivineFx.WHITE, 50, 2.0f)
                    .param(1.4f).spawn();
            this.dropTreasure(level);
            Godslayer.grantAround(level, this.center(), 160.0);
        }
        if (t >= DEATH_TICKS && !this.isRemoved()) {
            this.remove(Entity.RemovalReason.KILLED);
        }
    }

    /** Wo seine Beute landet: vor dem Thron (am Altar) oder unter ihm. */
    private Vec3 lootSpot(ServerLevel level) {
        if (this.altar != null && level.getBlockEntity(this.altar) instanceof CreatorAltarBlockEntity altarBe) {
            return Vec3.atBottomCenterOf(altarBe.arenaLootSpot()).add(0.0, 0.2, 0.0);
        }
        return this.ground(this.position()).add(0.0, 0.5, 0.0);
    }

    private void dropTreasure(ServerLevel level) {
        Vec3 spot = this.lootSpot(level);
        ResourceKey<LootTable> key = this.getType().getDefaultLootTable().orElse(null);
        if (key != null) {
            LootParams params = new LootParams.Builder(level)
                    .withParameter(LootContextParams.THIS_ENTITY, this)
                    .withParameter(LootContextParams.ORIGIN, spot)
                    .withParameter(LootContextParams.DAMAGE_SOURCE, this.getLastDamageSource() != null ? this.getLastDamageSource()
                            : this.damageSources().generic())
                    .create(LootContextParamSets.ENTITY);
            for (ItemStack stack : level.getServer().reloadableRegistries().getLootTable(key).getRandomItems(params)) {
                ItemEntity item = new ItemEntity(level, spot.x + (this.random.nextDouble() - 0.5) * 2.0, spot.y, spot.z + (this.random.nextDouble() - 0.5) * 2.0,
                        stack);
                item.setDeltaMovement(0.0, 0.25, 0.0);
                item.setUnlimitedLifetime();
                level.addFreshEntity(item);
            }
        }
        net.minecraft.world.entity.ExperienceOrb.award(level, spot, XP_REWARD);
        level.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, spot.x, spot.y + 1.0, spot.z, 80, 1.0, 1.5, 1.0, 0.4);
        Component message = Component.translatable("message.kingdomsatwar.schrolli.defeated").withColor(0xFFD24A);
        for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
            player.sendSystemMessage(message);
        }
    }

    @Override
    protected boolean shouldDropLoot(ServerLevel level) {
        return false;
    }

    @Override
    public void remove(Entity.RemovalReason reason) {
        if (this.level() instanceof ServerLevel level) {
            this.clearArtState();
            for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(ARENA_RADIUS), TimeFreeze::isFrozen)) {
                TimeFreeze.unfreeze(e);
            }
        }
        super.remove(reason);
        this.bossBar.removeAllPlayers();
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason,
            @Nullable SpawnGroupData data) {
        SpawnGroupData out = super.finalizeSpawn(level, difficulty, reason, data);
        if (reason == EntitySpawnReason.SPAWN_ITEM_USE || reason == EntitySpawnReason.COMMAND) {
            // aus dem Ei oder per Befehl: hier ist sein Hof
            this.center = this.position();
            this.awaken();
        }
        return out;
    }

    // ------------------------------------------------------------------ Speichern und Klänge

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        if (this.altar != null) {
            output.putLong("Altar", this.altar.asLong());
        }
        if (this.center != null) {
            output.store("Center", Vec3.CODEC, this.center);
        }
        output.putBoolean("Ascended", this.ascended || this.attack == Attack.ASCEND);
        output.putBoolean("TrueForm", this.isTrueForm() || this.attack == Attack.ASCEND);
        output.putBoolean("NovaDone", this.novaDone);
        output.putBoolean("ApocalypseDone", this.apocalypseDone);
        output.putBoolean("RecreationDone", this.recreationDone);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.altar = input.getLong("Altar").map(BlockPos::of).orElse(null);
        this.center = input.read("Center", Vec3.CODEC).orElse(null);
        this.ascended = input.getBooleanOr("Ascended", false);
        this.novaDone = input.getBooleanOr("NovaDone", false);
        this.apocalypseDone = input.getBooleanOr("ApocalypseDone", false);
        this.recreationDone = input.getBooleanOr("RecreationDone", false);
        if (input.getBooleanOr("TrueForm", false)) {
            this.becomeTrue();
            this.setHealth(Math.max(this.getHealth(), 1.0f));
        }
        this.nameBossBar();
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return this.attack == Attack.NONE ? ModSounds.SCHROLLI_AMBIENT.get() : null;
    }

    @Override
    public int getAmbientSoundInterval() {
        return 180;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.SCHROLLI_HURT.get();
    }

    @Override
    protected float getSoundVolume() {
        return 4.0f;
    }

    @Override
    public SoundSource getSoundSource() {
        return SoundSource.HOSTILE;
    }
}
