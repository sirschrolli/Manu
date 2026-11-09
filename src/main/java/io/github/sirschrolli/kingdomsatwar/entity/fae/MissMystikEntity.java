package io.github.sirschrolli.kingdomsatwar.entity.fae;

import io.github.sirschrolli.kingdomsatwar.block.fae.StageHatBlockEntity;
import io.github.sirschrolli.kingdomsatwar.magic.fx.SpellFxEntity;
import io.github.sirschrolli.kingdomsatwar.registry.ModParticles;
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
 * MissMystik, die Feen-Zauberkünstlerin: Einst unterhielt sie den Feenhof mit ihren Tricks; seit das Siegel zerbrach, ist
 * ihr Publikum fort, und ihre Illusionen sind gefährlich geworden. Auf ihrer verlassenen Zauberbühne wartet sie in einem
 * riesigen Zylinder – wer an ihn herantritt, wird Teil ihrer letzten Vorstellung: Trommelwirbel, ein Knall, Konfetti, und
 * sie springt heraus. Sie schwebt um ihre Zuschauer und zeigt Trick um Trick:
 * <ul>
 *     <li><b>Kartenfächer</b> – ein Fächer leuchtender Karten fliegt im Bogen auf ihr Ziel;</li>
 *     <li><b>Lichttauben</b> – ein Fingerschnippen, und Tauben aus Licht flattern auf, kreisen und stürzen sich auf ihr Ziel;</li>
 *     <li><b>Verschwindetrick</b> – eine Rauchwolke, sie ist fort … und taucht hinter ihrem Ziel wieder auf (Funken
 *     verraten, wo) und sticht mit dem Zauberstab zu;</li>
 *     <li><b>Spiegelkabinett</b> – sie teilt sich in Spiegelbilder, die ihr Ziel umkreisen und Karten werfen; ein
 *     Spiegelbild zerplatzt beim ersten Treffer (und rächt sich mit einem Knall), ein Treffer auf die echte MissMystik
 *     lässt alle zerspringen;</li>
 *     <li><b>Hut-Trick</b> – sie wirft ihren Zylinder; er landet kopfüber, ein Sog zieht alle zu ihm, dann wirft ein Knall
 *     sie in die Luft, und der Hut fliegt zurück;</li>
 *     <li><b>Schwerterkiste</b> – um ihr Ziel bildet sich eine Kiste aus Licht, ringsum erscheinen Schwerter – wer dann
 *     noch drin steht, wenn sie zustoßen …;</li>
 *     <li><b>Zieh eine Karte!</b> – eine riesige Karte über ihrem Ziel dreht sich um: Herz heilt sie, Pik fällt als Klinge
 *     herab, Karo regnet Splitter, Kreuz verflucht;</li>
 *     <li><b>Rampenlicht</b> (Zugabe) – Lichtkegel jagen ihre Zuschauer und brennen zu;</li>
 *     <li><b>Großes Finale</b> (einmal, wenn sie fast besiegt ist) – sie steigt über die Bühne, Feuerwerk regnet herab, und
 *     ein letzter Knall trifft jeden, der sie sehen kann und keine Deckung sucht.</li>
 * </ul>
 * Unter 60 % ihrer Kraft gibt sie eine <b>Zugabe</b>: schneller, mehr Karten, mehr Tauben, das Rampenlicht. Pfeile fängt
 * sie manchmal mit einer Karte ab. Findet sie niemanden mehr, verbeugt sie sich und verschwindet in ihrem Zylinder; besiegt
 * verbeugt sie sich ein letztes Mal und vergeht in einem Regen aus Konfetti – der Zylinder ist dann leer (Feenstaub ruft
 * sie zurück).
 */
public class MissMystikEntity extends Monster {
    public static final float MAX_HEALTH = 520.0f;
    /** Ablauf des Auftritts: Trommelwirbel, im Tick {@link #APPEAR_BURST} springt sie heraus, danach die Verbeugung. */
    public static final int APPEAR_TICKS = 72;
    public static final int APPEAR_BURST = 32;
    /** So lange (Ticks) dauert ihre letzte Verbeugung. */
    public static final int DEATH_TICKS = 70;
    /** Kartenfächer: in diesem Tick fliegen die Karten. */
    public static final int CARDS_THROW = 14;
    /** Lichttauben: in diesem Tick schnippt sie. */
    public static final int DOVES_SNAP = 12;
    /** Verschwindetrick: Rauchwolke, Wiederauftauchen, Stich. */
    public static final int VANISH_POOF = 9;
    public static final int VANISH_BACK = 27;
    public static final int VANISH_STRIKE = 31;
    /** Spiegelkabinett: in diesem Tick erscheinen die Spiegelbilder. */
    public static final int MIRRORS_SPLIT = 18;
    /** Hut-Trick: in diesem Tick fliegt der Zylinder. */
    public static final int HAT_THROW = 12;
    /** Schwerterkiste: in diesem Tick stoßen die Schwerter zu. */
    public static final int SWORDS_STAB = 32;
    /** Zieh eine Karte: in diesem Tick dreht sich die Karte um. */
    public static final int TRICK_FLIP = 30;
    /** Großes Finale: in diesem Tick der letzte Knall. */
    public static final int FINALE_BLAST = 112;
    /** Weiter als so weit von ihrem Zylinder folgt sie niemandem. */
    private static final double LEASH = 34.0;
    /** So lange ohne Zuschauer, dann verbeugt sie sich und verschwindet. */
    private static final int RETURN_TICKS = 600;
    private static final double FINALE_RADIUS = 16.0;

    public enum Attack {
        NONE(0), APPEAR(APPEAR_TICKS), CARDS(34), DOVES(46), VANISH(46), MIRRORS(38), HAT(110), SWORDS(56), SPOTLIGHT(112),
        CARD_TRICK(66), FINALE(132), EXIT(46);

        public final int duration;

        Attack(int duration) {
            this.duration = duration;
        }
    }

    /** Die vier Kartenfarben des Tricks „Zieh eine Karte!“. */
    public enum Suit { HEART, SPADE, DIAMOND, CLUB }

    private static final EntityDataAccessor<Integer> DATA_ATTACK = SynchedEntityData.defineId(MissMystikEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_ENCORE = SynchedEntityData.defineId(MissMystikEntity.class,
            EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_HAT = SynchedEntityData.defineId(MissMystikEntity.class, EntityDataSerializers.BOOLEAN);
    /** Verschwunden (im Hut vor dem Auftritt, beim Verschwindetrick, beim Abgang) – nicht zu sehen, nicht zu treffen. */
    private static final EntityDataAccessor<Boolean> DATA_HIDDEN = SynchedEntityData.defineId(MissMystikEntity.class,
            EntityDataSerializers.BOOLEAN);
    static final ParticleOptions MAGIC_DUST = new DustParticleOptions(0xC88CFF, 1.3f);
    static final ParticleOptions GOLD_DUST = new DustParticleOptions(0xFFD86A, 1.2f);
    static final ParticleOptions SMOKE = new DustParticleOptions(0x6A4A9A, 2.6f);

    private final ServerBossEvent bossBar = new ServerBossEvent(UUID.randomUUID(), Component.translatable("boss.kingdomsatwar.mystic"),
            BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.NOTCHED_10);
    private Attack attack = Attack.NONE;
    private Attack lastAttack = Attack.NONE;
    private int attackTick;
    private int cooldown = 30;
    private float orbit;
    private int idleTicks;
    private boolean finaleDone;
    private int sinceTrick = 2;
    private int sinceMirrors = 2;
    private @Nullable BlockPos home;
    // Verschwindetrick: wohin sie zurückkehrt
    private Vec3 vanishTo = Vec3.ZERO;
    // Schwerterkiste: Mitte der Kiste(n)
    private final List<Vec3> boxes = new ArrayList<>();
    // Rampenlicht: Lichtkegel (Lage, Säule) und wann sie zubrennen
    private final List<Vec3> spots = new ArrayList<>();
    private final List<SpellFxEntity> pillars = new ArrayList<>();
    // Finale und Kartentrick: markierte Stellen und Zeitpunkte
    private final List<Vec3> marks = new ArrayList<>();
    private final List<Integer> markTicks = new ArrayList<>();
    private @Nullable Suit suit;
    private Vec3 trickAt = Vec3.ZERO;
    private Vec3 finaleCenter = Vec3.ZERO;
    private @Nullable MagicCardEntity bigCard;
    private @Nullable UUID hatId;
    private final Set<UUID> struck = new HashSet<>();
    private int clientAttackStart;
    /** Nur im Client: Phase des Flügelschlags (für die Animationen). */
    public float flap;
    public float flapO;

    public MissMystikEntity(EntityType<? extends MissMystikEntity> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
        this.xpReward = 350;
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, MAX_HEALTH)
                .add(Attributes.ARMOR, 10.0)
                .add(Attributes.ARMOR_TOUGHNESS, 4.0)
                .add(Attributes.ATTACK_DAMAGE, 10.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.FLYING_SPEED, 0.5)
                .add(Attributes.FOLLOW_RANGE, 40.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ATTACK, 0);
        builder.define(DATA_ENCORE, false);
        builder.define(DATA_HAT, true);
        builder.define(DATA_HIDDEN, false);
    }

    /** Ist sie gerade verschwunden (nicht zu sehen, nicht zu treffen)? */
    public boolean isHidden() {
        return this.entityData.get(DATA_HIDDEN);
    }

    private void setHidden(boolean hidden) {
        this.entityData.set(DATA_HIDDEN, hidden);
    }

    public Attack currentAttack() {
        int i = this.entityData.get(DATA_ATTACK);
        return i >= 0 && i < Attack.values().length ? Attack.values()[i] : Attack.NONE;
    }

    /** Zugabe (unter 60 % ihrer Kraft): schneller, mehr Karten und Tauben, das Rampenlicht. */
    public boolean isEncore() {
        return this.entityData.get(DATA_ENCORE);
    }

    /** Trägt sie ihren Zylinder (oder fliegt der gerade beim Hut-Trick)? */
    public boolean wearsHat() {
        return this.entityData.get(DATA_HAT);
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

    /** Bindet sie an ihren Zylinder auf der Zauberbühne (dort tritt sie auf, dorthin kehrt sie zurück). */
    public void bindTo(BlockPos hat) {
        this.home = hat.immutable();
    }

    /** Ihr Auftritt: Trommelwirbel, dann springt sie aus dem Zylinder (unverwundbar bis nach der Verbeugung). */
    public void appear() {
        this.setHidden(true);
        this.begin(Attack.APPEAR);
    }

    /** Beginnt sofort einen bestimmten Trick (für Tests und die Vorführung). */
    public void forceAttack(Attack next) {
        this.begin(next);
    }

    /** Gibt sofort ihre Zugabe (für Tests und die Vorführung). */
    public void forceEncore() {
        this.entityData.set(DATA_ENCORE, true);
    }

    /** Zieht bei „Zieh eine Karte!“ diese Farbe (für Tests und die Vorführung); null = zufällig. */
    public void forceSuit(@Nullable Suit suit) {
        this.forcedSuit = suit;
    }

    private @Nullable Suit forcedSuit;

    private void begin(Attack next) {
        // was der vorige Trick zurückließ, verschwindet
        this.clearPillars();
        if (this.bigCard != null && this.bigCard.isAlive()) {
            this.bigCard.discard();
        }
        this.bigCard = null;
        if (this.isHidden() && next != Attack.APPEAR) {
            this.setHidden(false);
        }
        this.attack = next;
        this.attackTick = 0;
        this.entityData.set(DATA_ATTACK, next.ordinal());
        if (next != Attack.NONE && next != Attack.APPEAR && next != Attack.FINALE && next != Attack.EXIT) {
            this.lastAttack = next;
            this.sinceTrick = next == Attack.CARD_TRICK ? 0 : this.sinceTrick + 1;
            this.sinceMirrors = next == Attack.MIRRORS ? 0 : this.sinceMirrors + 1;
        }
        this.struck.clear();
        this.marks.clear();
        this.markTicks.clear();
        this.boxes.clear();
        this.spots.clear();
    }

    private void clearPillars() {
        for (SpellFxEntity fx : this.pillars) {
            if (fx.isAlive()) {
                fx.discard();
            }
        }
        this.pillars.clear();
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
     * Beim Auftritt, Abgang und während sie verschwunden ist, trifft sie nichts; Pfeile fängt sie manchmal mit einer Karte
     * ab. Wird sie selbst getroffen, zerspringen ihre Spiegelbilder (sie ist entlarvt).
     */
    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return super.hurtServer(level, source, amount);
        }
        if (this.attack == Attack.APPEAR || this.attack == Attack.EXIT || this.isHidden() || source.is(DamageTypes.IN_WALL)
                || source.is(DamageTypes.FALL) || source.is(DamageTypes.FLY_INTO_WALL)) {
            return false;
        }
        if (source.getDirectEntity() instanceof Projectile shot && !(shot instanceof MagicCardEntity)
                && this.random.nextFloat() < (this.isEncore() ? 0.4f : 0.28f)) {
            // Kartenschild: eine Karte schnappt den Pfeil aus der Luft
            Vec3 p = shot.position();
            level.sendParticles(ModParticles.CONFETTI.get(), p.x, p.y, p.z, 10, 0.2, 0.2, 0.2, 0.15);
            level.sendParticles(ParticleTypes.ENCHANTED_HIT, p.x, p.y, p.z, 8, 0.2, 0.2, 0.2, 0.2);
            this.sound(level, ModSounds.MYSTIK_CARD_HIT.get(), 1.4f, 1.4f);
            return false;
        }
        if (this.attack == Attack.FINALE && this.attackTick < 24) {
            amount *= 0.5f;
        }
        boolean hurt = super.hurtServer(level, source, amount);
        if (hurt) {
            this.dispelImages(level, true);
            if (source.getEntity() instanceof LivingEntity attacker && attacker != this && this.canStrike(attacker)
                    && !(this.getTarget() instanceof Player)) {
                this.setTarget(attacker);
            }
        }
        return hurt;
    }

    @Override
    public void tick() {
        // sie schwebt durch Kulissen und Vorhänge hindurch (Feenzauber)
        this.noPhysics = true;
        super.tick();
        if (this.level().isClientSide()) {
            this.flapO = this.flap;
            Attack a = this.currentAttack();
            float rate = 0.32f + (float) Math.min(0.2, this.getDeltaMovement().length() * 0.5);
            if (a == Attack.FINALE || a == Attack.APPEAR || a == Attack.SPOTLIGHT) {
                rate += 0.15f;
            }
            if (this.deathTime > 0) {
                rate *= Math.max(0.15f, 1.0f - this.deathTime / (float) DEATH_TICKS);
            }
            this.flap += rate;
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide() && !this.isHidden()) {
            // Feenfunken rieseln von den Flügeln
            Level level = this.level();
            if (this.random.nextInt(this.isEncore() ? 2 : 3) == 0) {
                float yaw = this.yBodyRot * Mth.DEG_TO_RAD;
                double side = (this.random.nextBoolean() ? 1 : -1) * (0.4 + this.random.nextDouble() * 0.9);
                level.addParticle(ModParticles.FAE_SPARK.get(), this.getX() + Mth.cos(yaw) * side + Mth.sin(yaw) * 0.3,
                        this.getY() + 1.4 + this.random.nextDouble() * 0.8, this.getZ() + Mth.sin(yaw) * side - Mth.cos(yaw) * 0.3, 0.0, -0.02,
                        0.0);
            }
        }
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        this.bossBar.setProgress(this.getHealth() / this.getMaxHealth());
        if (this.tickCount % 40 == 0 && this.strayed(level)) {
            return;
        }
        if (this.attack == Attack.APPEAR) {
            this.tickAppear(level);
            return;
        }
        if (this.attack == Attack.EXIT) {
            this.tickExit(level);
            return;
        }
        if (!this.isEncore() && this.getHealth() < this.getMaxHealth() * 0.6f && this.attack == Attack.NONE) {
            this.startEncore(level);
        }
        if (!this.finaleDone && this.isEncore() && this.getHealth() < this.getMaxHealth() * 0.3f && this.attack != Attack.FINALE
                && this.attack != Attack.HAT) {
            this.begin(Attack.FINALE);
            this.finaleDone = true;
        }
        if (this.attack == Attack.FINALE) {
            this.attackTick++;
            this.tickFinale(level);
            if (this.attackTick >= Attack.FINALE.duration) {
                this.begin(Attack.NONE);
                this.cooldown = 24;
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
            this.hover(target);
            this.faceTarget(target, 10.0f);
            if (--this.cooldown <= 0) {
                this.begin(this.chooseAttack(level, target));
            }
            return;
        }
        this.attackTick++;
        switch (this.attack) {
            case CARDS -> this.tickCards(level, target);
            case DOVES -> this.tickDoves(level, target);
            case VANISH -> this.tickVanish(level, target);
            case MIRRORS -> this.tickMirrors(level, target);
            case HAT -> this.tickHat(level, target);
            case SWORDS -> this.tickSwords(level, target);
            case SPOTLIGHT -> this.tickSpotlight(level, target);
            case CARD_TRICK -> this.tickCardTrick(level, target);
            default -> { }
        }
        if (this.attackTick >= this.attack.duration) {
            if (this.attack == Attack.HAT) {
                this.catchHat(level);
            }
            this.begin(Attack.NONE);
            this.cooldown = this.isEncore() ? 16 + this.random.nextInt(14) : 28 + this.random.nextInt(18);
        }
    }

    /** Zugabe! Sie ruft es in die Runde, Konfetti regnet – und sie beginnt gleich mit dem Rampenlicht. */
    private void startEncore(ServerLevel level) {
        this.entityData.set(DATA_ENCORE, true);
        this.sound(level, ModSounds.MYSTIK_TADA.get(), 3.0f, 1.15f);
        confetti(level, this.position().add(0.0, 2.6, 0.0), 60, 1.2, 0.35);
        for (ServerPlayer player : level.players()) {
            if (player.distanceToSqr(this) < 48.0 * 48.0) {
                player.sendOverlayMessage(Component.translatable("entity.kingdomsatwar.miss_mystik.encore").withColor(0xFF8AD8));
            }
        }
        this.begin(Attack.SPOTLIGHT);
    }

    /**
     * Gehört sie (noch) zu ihrem Zylinder? Ist dort inzwischen eine andere MissMystik gebunden (etwa nach dem Neuladen der
     * Welt), vergeht diese hier; ist der Zylinder fort, bleibt sie frei.
     */
    private boolean strayed(ServerLevel level) {
        if (this.home == null || !level.isLoaded(this.home)) {
            return false;
        }
        if (level.getBlockEntity(this.home) instanceof StageHatBlockEntity hat) {
            if (!hat.owns(this)) {
                level.sendParticles(SMOKE, this.getX(), this.getY(1.0), this.getZ(), 40, 0.6, 0.8, 0.6, 0.02);
                this.dispelImages(level, false);
                this.discard();
                return true;
            }
            return false;
        }
        this.home = null;
        return false;
    }

    /**
     * Wen ihre Tricks treffen: Spieler (nicht im Kreativ- oder Zuschauermodus), ihre Begleiter und Soldaten, Tiere – aber
     * keine Monster (außer sie greifen sie an), nicht sich selbst, nicht ihre Spiegelbilder, keine Rüstungsständer.
     */
    boolean canStrike(LivingEntity entity) {
        if (entity == this || !entity.isAlive() || entity instanceof MissMystikEntity || entity instanceof MystikImageEntity
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
                && (this.home == null || current.distanceToSqr(Vec3.atCenterOf(this.home)) < (LEASH + 6.0) * (LEASH + 6.0))) {
            return current;
        }
        return level.getNearestPlayer(this.getX(), this.getY(), this.getZ(), 32.0,
                e -> e instanceof Player p && !p.isCreative() && !p.isSpectator() && p.isAlive());
    }

    /** Ohne Zuschauer: über der Bühne schweben, sich erholen – und nach einer halben Minute verbeugt sie sich und geht. */
    private void idle(ServerLevel level) {
        if (this.attack != Attack.NONE) {
            if (this.attack == Attack.HAT) {
                this.catchHat(level);
            }
            this.begin(Attack.NONE);
        }
        this.idleTicks++;
        Vec3 center = this.home != null ? Vec3.atBottomCenterOf(this.home) : this.position();
        this.orbit += 0.015f;
        Vec3 want = center.add(Mth.cos(this.orbit) * 4.0, 3.5 + Mth.sin(this.tickCount * 0.05f) * 0.5, Mth.sin(this.orbit) * 4.0);
        this.steer(want, 0.05, 0.25);
        this.faceMotion();
        if (this.idleTicks > 200 && this.tickCount % 20 == 0) {
            this.heal(5.0f);
        }
        if (this.home != null && this.idleTicks > RETURN_TICKS) {
            this.begin(Attack.EXIT);
        }
    }

    /** Schwebt in sechs bis neun Blöcken Abstand um ihr Ziel, zwei bis vier Blöcke über dem Boden, sanft auf und ab. */
    private void hover(LivingEntity target) {
        this.orbit += this.isEncore() ? 0.026f : 0.018f;
        double radius = 7.5 + Mth.sin(this.tickCount * 0.021f) * 1.5;
        Vec3 around = target.position().add(Mth.cos(this.orbit) * radius, 0.0, Mth.sin(this.orbit) * radius);
        Vec3 ground = this.ground(around);
        double height = 2.6 + Mth.sin(this.tickCount * 0.07f) * 0.7;
        this.steer(new Vec3(around.x, Math.max(ground.y, target.getY()) + height, around.z), 0.05, this.isEncore() ? 0.45 : 0.36);
    }

    private void steer(Vec3 want, double accel, double max) {
        Vec3 to = want.subtract(this.position());
        Vec3 v = this.getDeltaMovement().add(to.normalize().scale(Math.min(to.length(), 4.0) * accel * 0.25));
        if (v.length() > max) {
            v = v.normalize().scale(max);
        }
        this.setDeltaMovement(this.keepAboveGround(v, 1.0));
    }

    /** Nie in den Boden: liegt ihr nächster Schritt nicht {@code clearance} Blöcke über dem Grund, steigt sie. */
    private Vec3 keepAboveGround(Vec3 v, double clearance) {
        Vec3 next = this.position().add(v);
        int ground = this.level().getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Mth.floor(next.x), Mth.floor(next.z));
        if (ground > this.level().getMinY() && next.y < ground + clearance && next.y > ground - 6.0) {
            return new Vec3(v.x, Math.max(v.y, ground + clearance - this.getY()) * 0.5 + 0.03, v.z);
        }
        return v;
    }

    private void faceMotion() {
        Vec3 v = this.getDeltaMovement();
        if (v.horizontalDistanceSqr() > 0.004) {
            this.turnTo((float) (Mth.atan2(v.z, v.x) * Mth.RAD_TO_DEG) - 90.0f, 6.0f);
        }
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

    private Attack chooseAttack(ServerLevel level, LivingEntity target) {
        double distance = this.distanceTo(target);
        List<Attack> options = new ArrayList<>(List.of(Attack.CARDS, Attack.CARDS, Attack.DOVES, Attack.SWORDS));
        if (this.wearsHat()) {
            options.add(Attack.HAT);
        }
        if (distance > 7.0 || this.random.nextBoolean()) {
            options.add(Attack.VANISH);
        }
        if (this.sinceMirrors >= 3 && this.images(level).isEmpty()) {
            options.add(Attack.MIRRORS);
        }
        if (this.sinceTrick >= 3) {
            options.add(Attack.CARD_TRICK);
            options.add(Attack.CARD_TRICK);
        }
        if (this.isEncore()) {
            options.add(Attack.SPOTLIGHT);
            options.add(Attack.DOVES);
        }
        options.removeIf(a -> a == this.lastAttack);
        return options.get(this.random.nextInt(options.size()));
    }

    private Vec3 forward() {
        float yaw = this.getYRot() * Mth.DEG_TO_RAD;
        return new Vec3(-Mth.sin(yaw), 0.0, Mth.cos(yaw));
    }

    /** Wo ihre linke Hand die Karten hält (etwa auf Schulterhöhe, vorn). */
    Vec3 cardHand() {
        float yaw = this.getYRot() * Mth.DEG_TO_RAD;
        return this.position().add(Mth.cos(yaw) * 0.5 - Mth.sin(yaw) * 0.5, 1.9, Mth.sin(yaw) * 0.5 + Mth.cos(yaw) * 0.5);
    }

    /** Die Spitze ihres Zauberstabs. */
    Vec3 wandTip() {
        float yaw = this.getYRot() * Mth.DEG_TO_RAD;
        return this.position().add(-Mth.cos(yaw) * 0.45 - Mth.sin(yaw) * 0.9, 1.6, -Mth.sin(yaw) * 0.45 + Mth.cos(yaw) * 0.9);
    }

    void sound(ServerLevel level, SoundEvent sound, float volume, float pitch) {
        level.playSound(null, this.getX(), this.getY(), this.getZ(), sound, SoundSource.HOSTILE, volume, pitch);
    }

    static void push(LivingEntity entity, Vec3 push) {
        entity.push(push.x, push.y, push.z);
        entity.needsSync = true;
    }

    static void confetti(ServerLevel level, Vec3 at, int count, double spread, double speed) {
        level.sendParticles(ModParticles.CONFETTI.get(), at.x, at.y, at.z, count, spread, spread * 0.6, spread, speed);
    }

    /** Der Boden unter einer Stelle (oder die Stelle selbst, wenn der Boden weit entfernt liegt). */
    Vec3 ground(Vec3 at) {
        int y = this.level().getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Mth.floor(at.x), Mth.floor(at.z));
        if (y <= this.level().getMinY() || y > at.y + 6.0 || y < at.y - 12.0) {
            return at;
        }
        return new Vec3(at.x, y, at.z);
    }

    private float scaled(float damage) {
        return this.isEncore() ? damage * 1.2f : damage;
    }

    /** Wo das Ziel in {@code ticks} Ticks sein wird (es läuft weiter). */
    private static Vec3 lead(LivingEntity target, double ticks) {
        Vec3 v = target.getDeltaMovement();
        return target.position().add(v.x * ticks, 0.0, v.z * ticks);
    }

    // ------------------------------------------------------------------ Auftritt und Abgang

    /**
     * Auftritt: Trommelwirbel, Funken wirbeln um den Zylinder – dann ein Knall, Konfetti, und sie springt heraus, wirbelt
     * empor und verbeugt sich. Der Titel erscheint.
     */
    private void tickAppear(ServerLevel level) {
        int t = ++this.attackTick;
        Vec3 hat = this.home != null ? Vec3.atBottomCenterOf(this.home) : this.position();
        if (t == 1) {
            this.sound(level, ModSounds.MYSTIK_DRUMROLL.get(), 3.5f, 1.0f);
        }
        if (t < APPEAR_BURST) {
            this.setPos(hat.x, hat.y + 0.2, hat.z);
            this.setDeltaMovement(Vec3.ZERO);
            if (t % 2 == 0) {
                double a = t * 0.5;
                double r = 1.2 - t * 0.025;
                for (int i = 0; i < 3; i++) {
                    double b = a + i * Math.PI * 2.0 / 3.0;
                    level.sendParticles(ModParticles.FAE_SPARK.get(), hat.x + Math.cos(b) * r, hat.y + 0.4 + t * 0.03, hat.z + Math.sin(b) * r,
                            1, 0.0, 0.0, 0.0, 0.0);
                }
                level.sendParticles(MAGIC_DUST, hat.x, hat.y + 1.2, hat.z, 2, 0.25, 0.1, 0.25, 0.0);
            }
            return;
        }
        if (t == APPEAR_BURST) {
            this.setHidden(false);
            this.sound(level, ModSounds.MYSTIK_TADA.get(), 4.0f, 1.0f);
            this.setPos(hat.x, hat.y + 0.6, hat.z);
            this.setDeltaMovement(0.0, 0.55, 0.0);
            confetti(level, hat.add(0.0, 1.4, 0.0), 120, 0.6, 0.45);
            level.sendParticles(ParticleTypes.FIREWORK, hat.x, hat.y + 1.2, hat.z, 30, 0.3, 0.3, 0.3, 0.2);
            level.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, 0xFFFF9AE6), hat.x, hat.y + 1.5, hat.z, 1, 0.0, 0.0, 0.0, 0.0);
            level.sendParticles(SMOKE, hat.x, hat.y + 1.0, hat.z, 30, 0.5, 0.4, 0.5, 0.05);
        }
        if (t > APPEAR_BURST && t < APPEAR_BURST + 16) {
            // wirbelnd empor
            this.setDeltaMovement(this.getDeltaMovement().multiply(0.0, 0.82, 0.0));
            this.setYRot(this.getYRot() + 24.0f);
            this.yBodyRot = this.getYRot();
            this.yHeadRot = this.getYRot();
        } else if (t >= APPEAR_BURST + 16) {
            this.setDeltaMovement(this.getDeltaMovement().scale(0.5));
            Player nearest = level.getNearestPlayer(this, 40.0);
            if (nearest != null) {
                this.faceTarget(nearest, 14.0f);
            }
        }
        if (t == APPEAR_BURST + 22) {
            this.sound(level, ModSounds.MYSTIK_LAUGH.get(), 3.0f, 1.0f);
            Component title = Component.translatable("boss.kingdomsatwar.mystic").withColor(0xE88AFF);
            Component subtitle = Component.translatable("boss.kingdomsatwar.mystic.title").withStyle(ChatFormatting.GRAY);
            for (ServerPlayer player : level.players()) {
                if (player.distanceToSqr(this) < 64.0 * 64.0) {
                    player.connection.send(new ClientboundSetTitlesAnimationPacket(12, 60, 20));
                    player.connection.send(new ClientboundSetTitleTextPacket(title));
                    player.connection.send(new ClientboundSetSubtitleTextPacket(subtitle));
                }
            }
        }
        if (t >= APPEAR_TICKS) {
            this.begin(Attack.NONE);
            this.cooldown = 20;
            this.idleTicks = 0;
        }
    }

    /** Niemand mehr da: sie verbeugt sich, verschwindet in einer Rauchwolke und kehrt in ihren Zylinder zurück. */
    private void tickExit(ServerLevel level) {
        int t = ++this.attackTick;
        this.setDeltaMovement(this.getDeltaMovement().scale(0.6));
        if (t == 4) {
            this.sound(level, ModSounds.MYSTIK_LAUGH.get(), 2.0f, 0.9f);
        }
        if (t == 30) {
            this.sound(level, ModSounds.MYSTIK_VANISH.get(), 2.5f, 1.0f);
            level.sendParticles(SMOKE, this.getX(), this.getY(1.0), this.getZ(), 50, 0.6, 0.9, 0.6, 0.03);
            confetti(level, this.position().add(0.0, 1.6, 0.0), 30, 0.5, 0.2);
            this.setHidden(true);
        }
        if (t >= Attack.EXIT.duration) {
            if (this.home != null && level.getBlockEntity(this.home) instanceof StageHatBlockEntity hat && hat.owns(this)) {
                hat.onPerformerDeparted(level);
            }
            this.dispelImages(level, false);
            this.discard();
        }
    }

    // ------------------------------------------------------------------ Tricks

    /** Kartenfächer: sie fächert die Karten auf, holt aus – und fünf (in der Zugabe sieben, dann noch drei) fliegen im Bogen. */
    private void tickCards(ServerLevel level, LivingEntity target) {
        int t = this.attackTick;
        this.setDeltaMovement(this.getDeltaMovement().scale(0.8));
        this.faceTarget(target, 12.0f);
        if (t == 4) {
            this.sound(level, ModSounds.MYSTIK_CARD_TRICK.get(), 1.5f, 1.6f);
        }
        if (t == CARDS_THROW) {
            this.fan(level, target, this.isEncore() ? 7 : 5, this.isEncore() ? 30.0f : 24.0f);
        }
        if (t == CARDS_THROW + 9 && this.isEncore()) {
            this.fan(level, target, 3, 10.0f);
        }
    }

    private void fan(ServerLevel level, LivingEntity target, int count, float spread) {
        Vec3 from = this.cardHand();
        Vec3 aim = lead(target, from.distanceTo(target.position()) / MagicCardEntity.SPEED).add(0.0, target.getBbHeight() * 0.5, 0.0);
        Vec3 dir = aim.subtract(from).normalize();
        double baseYaw = Math.atan2(dir.z, dir.x);
        double pitch = Math.asin(Mth.clamp(dir.y, -1.0, 1.0));
        for (int i = 0; i < count; i++) {
            double off = count == 1 ? 0.0 : (i / (count - 1.0) - 0.5) * 2.0 * spread * Mth.DEG_TO_RAD;
            double yaw = baseYaw + off;
            Vec3 d = new Vec3(Math.cos(yaw) * Math.cos(pitch), Math.sin(pitch), Math.sin(yaw) * Math.cos(pitch));
            MagicCardEntity card = new MagicCardEntity(level, this);
            card.setSuit(this.random.nextInt(4));
            card.setDamage(this.scaled(5.0f));
            card.setPos(from.x, from.y, from.z);
            card.setDeltaMovement(d.scale(MagicCardEntity.SPEED));
            level.addFreshEntity(card);
        }
        this.sound(level, ModSounds.MYSTIK_CARD_THROW.get(), 2.0f, 1.0f + this.random.nextFloat() * 0.1f);
    }

    /** Lichttauben: sie zieht mit dem Stab einen Kreis, schnippt – Tauben aus Licht flattern auf und stürzen sich auf ihr Ziel. */
    private void tickDoves(ServerLevel level, LivingEntity target) {
        int t = this.attackTick;
        this.setDeltaMovement(this.getDeltaMovement().scale(0.8));
        this.faceTarget(target, 10.0f);
        if (t < DOVES_SNAP && t % 2 == 0) {
            Vec3 tip = this.wandTip();
            level.sendParticles(ParticleTypes.END_ROD, tip.x, tip.y, tip.z, 1, 0.05, 0.05, 0.05, 0.01);
        }
        if (t != DOVES_SNAP) {
            return;
        }
        this.sound(level, ModSounds.MYSTIK_DOVES.get(), 2.5f, 1.0f);
        int count = this.isEncore() ? 8 : 6;
        // aus ihrer Hand (über dem Kopf geschnippt) – ein Wölkchen, Konfetti, und die Tauben flattern auf
        Vec3 c = this.cardHand().add(0.0, 0.4, 0.0);
        level.sendParticles(ParticleTypes.CLOUD, c.x, c.y, c.z, 6, 0.2, 0.15, 0.2, 0.03);
        confetti(level, c, 24, 0.3, 0.25);
        for (int i = 0; i < count; i++) {
            double a = i * Math.PI * 2.0 / count + this.random.nextDouble() * 0.3;
            LightDoveEntity dove = new LightDoveEntity(level, this);
            dove.setPos(c.x + Math.cos(a) * 0.6, c.y + this.random.nextDouble() * 0.4, c.z + Math.sin(a) * 0.6);
            dove.setDeltaMovement(Math.cos(a) * 0.45, 0.3 + this.random.nextDouble() * 0.2, Math.sin(a) * 0.45);
            dove.setHunt(target, 14 + i * 3, this.scaled(3.0f));
            level.addFreshEntity(dove);
        }
    }

    /**
     * Verschwindetrick: Sie wirbelt herum und tippt an den Hut – Rauchwolke, sie ist fort. Funken zeigen, wo sie
     * wiederkommt: hinter ihrem Ziel. Dann taucht sie auf und sticht mit dem Zauberstab zu.
     */
    private void tickVanish(ServerLevel level, LivingEntity target) {
        int t = this.attackTick;
        if (t < VANISH_POOF) {
            this.setDeltaMovement(this.getDeltaMovement().scale(0.7));
            this.setYRot(this.getYRot() + 30.0f);
            this.yBodyRot = this.getYRot();
            return;
        }
        if (t == VANISH_POOF) {
            this.sound(level, ModSounds.MYSTIK_VANISH.get(), 2.5f, 1.0f);
            level.sendParticles(SMOKE, this.getX(), this.getY(1.0), this.getZ(), 60, 0.6, 1.0, 0.6, 0.03);
            level.sendParticles(ParticleTypes.POOF, this.getX(), this.getY(1.0), this.getZ(), 20, 0.5, 0.8, 0.5, 0.05);
            confetti(level, this.position().add(0.0, 1.5, 0.0), 30, 0.5, 0.2);
            this.setHidden(true);
            this.setDeltaMovement(Vec3.ZERO);
        }
        if (t > VANISH_POOF && t < VANISH_BACK) {
            // wohin? hinter das Ziel (gegen seine Blickrichtung), einen Block über dem Boden
            Vec3 look = target.getLookAngle();
            Vec3 back = new Vec3(-look.x, 0.0, -look.z);
            back = back.lengthSqr() < 1.0e-4 ? this.forward().scale(-1.0) : back.normalize();
            Vec3 spot = target.position().add(back.scale(2.4));
            this.vanishTo = this.ground(spot).add(0.0, 0.6, 0.0);
            if (t >= VANISH_BACK - 10 && t % 2 == 0) {
                Vec3 s = this.vanishTo;
                level.sendParticles(ModParticles.FAE_SPARK.get(), s.x, s.y + 1.2, s.z, 6, 0.3, 0.7, 0.3, 0.02);
                level.sendParticles(GOLD_DUST, s.x, s.y + 1.0, s.z, 3, 0.25, 0.6, 0.25, 0.0);
            }
            return;
        }
        if (t == VANISH_BACK) {
            this.setHidden(false);
            this.teleportTo(this.vanishTo.x, this.vanishTo.y, this.vanishTo.z);
            this.setDeltaMovement(Vec3.ZERO);
            this.faceTarget(target, 360.0f);
            this.sound(level, ModSounds.MYSTIK_REAPPEAR.get(), 2.5f, 1.0f);
            confetti(level, this.position().add(0.0, 1.5, 0.0), 50, 0.5, 0.3);
            level.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, 0xFFE8B8FF), this.getX(), this.getY(1.2), this.getZ(), 1, 0.0,
                    0.0, 0.0, 0.0);
            return;
        }
        this.faceTarget(target, 20.0f);
        this.setDeltaMovement(this.getDeltaMovement().scale(0.6));
        if (t == VANISH_STRIKE) {
            Vec3 tip = this.wandTip();
            level.sendParticles(ParticleTypes.ENCHANTED_HIT, tip.x, tip.y, tip.z, 14, 0.2, 0.2, 0.2, 0.3);
            for (LivingEntity victim : this.victims(level, new AABB(tip, tip).inflate(2.2, 1.6, 2.2))) {
                if (victim.hurtServer(level, this.damageSources().mobAttack(this), this.scaled(10.0f))) {
                    push(victim, this.forward().scale(1.2).add(0.0, 0.35, 0.0));
                    victim.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 30, 1), this);
                    this.sound(level, ModSounds.MYSTIK_STAB.get(), 1.8f, 1.3f);
                }
            }
        }
    }

    /**
     * Spiegelkabinett: Sie dreht sich mit ausgebreiteten Armen, Spiegelsplitter funkeln – dann steht sie dreifach (in der
     * Zugabe vierfach) um ihr Ziel herum, und sie selbst irgendwo dazwischen.
     */
    private void tickMirrors(ServerLevel level, LivingEntity target) {
        int t = this.attackTick;
        this.setDeltaMovement(this.getDeltaMovement().scale(0.7));
        if (t < MIRRORS_SPLIT) {
            this.setYRot(this.getYRot() + 20.0f);
            this.yBodyRot = this.getYRot();
            if (t % 2 == 0) {
                level.sendParticles(ParticleTypes.END_ROD, this.getX(), this.getY(1.2), this.getZ(), 3, 0.8, 0.8, 0.8, 0.02);
            }
            if (t == 4) {
                this.sound(level, ModSounds.MYSTIK_MIRRORS.get(), 2.5f, 1.0f);
            }
            return;
        }
        if (t != MIRRORS_SPLIT) {
            this.faceTarget(target, 10.0f);
            return;
        }
        int images = this.isEncore() ? 4 : 3;
        int slots = images + 1;
        int mine = this.random.nextInt(slots);
        double base = this.random.nextDouble() * Math.PI * 2.0;
        level.sendParticles(SMOKE, this.getX(), this.getY(1.0), this.getZ(), 30, 0.5, 0.8, 0.5, 0.02);
        for (int i = 0; i < slots; i++) {
            double a = base + i * Math.PI * 2.0 / slots;
            Vec3 at = this.ground(target.position().add(Math.cos(a) * 7.0, 0.0, Math.sin(a) * 7.0)).add(0.0, 2.2, 0.0);
            confetti(level, at.add(0.0, 1.4, 0.0), 16, 0.4, 0.15);
            level.sendParticles(ParticleTypes.END_ROD, at.x, at.y + 1.2, at.z, 10, 0.3, 0.8, 0.3, 0.03);
            if (i == mine) {
                this.teleportTo(at.x, at.y, at.z);
                this.setDeltaMovement(Vec3.ZERO);
                this.faceTarget(target, 360.0f);
                continue;
            }
            MystikImageEntity image = new MystikImageEntity(level, this);
            image.snapTo(at.x, at.y, at.z, this.getYRot(), 0.0f);
            image.setup(target, (float) a, this.isEncore());
            level.addFreshEntity(image);
        }
    }

    List<MystikImageEntity> images(ServerLevel level) {
        return level.getEntitiesOfClass(MystikImageEntity.class, this.getBoundingBox().inflate(48.0), image -> image.isImageOf(this));
    }

    /** Lässt alle Spiegelbilder zerspringen (wenn sie entlarvt ist oder geht). */
    void dispelImages(ServerLevel level, boolean loud) {
        List<MystikImageEntity> images = this.images(level);
        for (MystikImageEntity image : images) {
            image.shatter(level, false);
        }
        if (loud && !images.isEmpty()) {
            this.sound(level, ModSounds.MYSTIK_MIRRORS.get(), 2.5f, 0.7f);
        }
    }

    /**
     * Hut-Trick: Sie nimmt den Zylinder ab und wirft ihn; er landet kopfüber bei ihrem Ziel, ein Sog zieht alle zu ihm, ein
     * Knall wirft sie in die Luft – dann fliegt er zurück auf ihren Kopf.
     */
    private void tickHat(ServerLevel level, LivingEntity target) {
        int t = this.attackTick;
        this.faceTarget(target, 8.0f);
        if (t < HAT_THROW) {
            this.setDeltaMovement(this.getDeltaMovement().scale(0.7));
            return;
        }
        if (t == HAT_THROW) {
            Vec3 from = this.position().add(0.0, 2.5, 0.0);
            Vec3 land = this.ground(lead(target, 10.0));
            MagicHatEntity hat = new MagicHatEntity(level, this, from, land, this.scaled(8.0f));
            level.addFreshEntity(hat);
            this.hatId = hat.getUUID();
            this.entityData.set(DATA_HAT, false);
            this.sound(level, ModSounds.MYSTIK_HAT_THROW.get(), 2.0f, 1.0f);
            return;
        }
        // ohne Hut: sie hält Abstand und wirft dann und wann eine einzelne Karte
        this.hover(target);
        if (this.isEncore() && t % 30 == 0) {
            this.fan(level, target, 1, 0.0f);
        }
        MagicHatEntity hat = this.hat(level);
        if (hat == null || hat.isBack()) {
            this.catchHat(level);
            this.attackTick = Attack.HAT.duration;
        }
    }

    private @Nullable MagicHatEntity hat(ServerLevel level) {
        return this.hatId != null && level.getEntity(this.hatId) instanceof MagicHatEntity hat && hat.isAlive() ? hat : null;
    }

    /** Der Zylinder ist zurück (oder fort): wieder auf den Kopf. */
    private void catchHat(ServerLevel level) {
        MagicHatEntity hat = this.hat(level);
        if (hat != null) {
            hat.discard();
        }
        this.hatId = null;
        if (!this.wearsHat()) {
            this.entityData.set(DATA_HAT, true);
            level.sendParticles(ModParticles.FAE_SPARK.get(), this.getX(), this.getY() + 2.6, this.getZ(), 10, 0.3, 0.2, 0.3, 0.02);
        }
    }

    /**
     * Schwerterkiste: Um ihr Ziel bildet sich eine Kiste aus Licht, ringsum erscheinen acht Schwerter. Wer drin steht, wenn
     * sie zustoßen, wird schwer getroffen. In der Zugabe zwei Kisten.
     */
    private void tickSwords(ServerLevel level, LivingEntity target) {
        int t = this.attackTick;
        this.setDeltaMovement(this.getDeltaMovement().scale(0.8));
        this.faceTarget(target, 10.0f);
        if (t == 6) {
            this.boxes.add(this.ground(target.position()));
            if (this.isEncore()) {
                Player other = level.getNearestPlayer(target.getX(), target.getY(), target.getZ(), 16.0,
                        e -> e != target && e instanceof Player p && this.canStrike(p));
                Vec3 second = other != null ? other.position()
                        : target.position().add(this.random.nextBoolean() ? 3.5 : -3.5, 0.0, this.random.nextBoolean() ? 3.5 : -3.5);
                this.boxes.add(this.ground(second));
            }
            this.sound(level, ModSounds.MYSTIK_SWORDS.get(), 2.5f, 1.0f);
            for (Vec3 box : this.boxes) {
                for (int i = 0; i < 8; i++) {
                    double a = i * Math.PI / 4.0 + Math.PI / 8.0;
                    Vec3 start = box.add(Math.cos(a) * 3.4, 1.0 + (i % 2) * 0.7, Math.sin(a) * 3.4);
                    MagicSwordEntity sword = new MagicSwordEntity(level, start, box.add(0.0, 1.0 + (i % 2) * 0.7, 0.0), SWORDS_STAB - 6 + i % 2);
                    level.addFreshEntity(sword);
                }
            }
        }
        if (t >= 6 && t < SWORDS_STAB && t % 2 == 0) {
            for (Vec3 box : this.boxes) {
                boxFrame(level, box, 1.6, t > SWORDS_STAB - 10 ? GOLD_DUST : MAGIC_DUST);
            }
        }
        if (t == SWORDS_STAB) {
            this.sound(level, ModSounds.MYSTIK_STAB.get(), 3.0f, 0.9f);
            for (Vec3 box : this.boxes) {
                level.sendParticles(ParticleTypes.CRIT, box.x, box.y + 1.2, box.z, 30, 0.6, 0.6, 0.6, 0.4);
                level.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, 0xFFD8B8FF), box.x, box.y + 1.2, box.z, 1, 0.0, 0.0, 0.0,
                        0.0);
                for (LivingEntity victim : this.victims(level, new AABB(box.x - 1.7, box.y - 0.5, box.z - 1.7, box.x + 1.7, box.y + 3.2,
                        box.z + 1.7))) {
                    if (this.struck.add(victim.getUUID())) {
                        victim.hurtServer(level, this.damageSources().indirectMagic(this, this), this.scaled(13.0f));
                    }
                }
            }
        }
    }

    /** Die zwölf Kanten einer Kiste aus Licht (halbe Breite {@code half}, drei Blöcke hoch). */
    static void boxFrame(ServerLevel level, Vec3 c, double half, ParticleOptions particle) {
        double y0 = c.y + 0.05;
        double y1 = c.y + 3.0;
        double[] xs = {c.x - half, c.x + half};
        double[] zs = {c.z - half, c.z + half};
        for (double s = 0.0; s <= 1.0001; s += 0.2) {
            double x = c.x - half + s * half * 2.0;
            double z = c.z - half + s * half * 2.0;
            double y = y0 + s * (y1 - y0);
            for (double xx : xs) {
                for (double zz : zs) {
                    level.sendParticles(particle, xx, y, zz, 1, 0.0, 0.0, 0.0, 0.0);
                }
            }
            for (double yy : new double[] {y0, y1}) {
                for (double zz : zs) {
                    level.sendParticles(particle, x, yy, zz, 1, 0.0, 0.0, 0.0, 0.0);
                }
                for (double xx : xs) {
                    level.sendParticles(particle, xx, yy, z, 1, 0.0, 0.0, 0.0, 0.0);
                }
            }
        }
    }

    /**
     * Rampenlicht (Zugabe): Sie steigt über die Bühne, die Arme erhoben; drei Lichtkegel jagen ihre Zuschauer und brennen
     * nacheinander zu (ein Lichtkreis am Boden warnt kurz vorher).
     */
    private void tickSpotlight(ServerLevel level, LivingEntity target) {
        int t = this.attackTick;
        Vec3 stage = this.home != null ? Vec3.atBottomCenterOf(this.home) : target.position();
        // auf der Bühne schwebt sie über dem Bühnenrand vor dem Vorhang, sonst über ihrem Ziel
        Vec3 want = new Vec3(stage.x, Math.max(stage.y, target.getY()) + (this.home != null ? 5.0 : 6.0), stage.z + (this.home != null ? 4.0 : 0.0));
        this.steer(want, 0.05, 0.4);
        this.faceTarget(target, 8.0f);
        if (t == 4) {
            this.sound(level, ModSounds.MYSTIK_SPOTLIGHT.get(), 3.0f, 1.0f);
            List<LivingEntity> watchers = new ArrayList<>(this.victims(level, this.getBoundingBox().inflate(28.0)));
            watchers.removeIf(e -> !(e instanceof Player) && e != target);
            if (watchers.isEmpty()) {
                watchers.add(target);
            }
            for (int i = 0; i < 3; i++) {
                LivingEntity who = watchers.get(i % watchers.size());
                double a = this.random.nextDouble() * Math.PI * 2.0;
                Vec3 p = this.ground(who.position().add(Math.cos(a) * 5.0, 0.0, Math.sin(a) * 5.0));
                this.spots.add(p);
                SpellFxEntity pillar = SpellFxEntity.create(level, SpellFxEntity.Kind.PILLAR, p, null, 0xFFF0C8, 200, 1.4f).param(14.0f);
                pillar.spawn();
                this.pillars.add(pillar);
            }
        }
        if (this.spots.size() < 3) {
            return;
        }
        for (int i = 0; i < 3; i++) {
            int strike = 44 + i * 20;
            Vec3 p = this.spots.get(i);
            if (t < strike - 14) {
                // jagen: der Kegel gleitet auf seinen Zuschauer zu (etwas langsamer, als man rennt)
                LivingEntity who = this.nearestWatcher(level, p, target);
                Vec3 to = new Vec3(who.getX() - p.x, 0.0, who.getZ() - p.z);
                double step = Math.min(to.length(), this.isEncore() ? 0.2 : 0.17);
                if (to.lengthSqr() > 1.0e-4) {
                    p = this.ground(p.add(to.normalize().scale(step)));
                }
                this.spots.set(i, p);
                SpellFxEntity pillar = this.pillars.get(i);
                if (pillar.isAlive()) {
                    pillar.setPos(p.x, p.y, p.z);
                }
            } else if (t == strike - 14) {
                SpellFxEntity.create(level, SpellFxEntity.Kind.CIRCLE, p.add(0.0, 0.05, 0.0), null, 0xFFE89A, 14, 1.7f).spawn();
            } else if (t == strike) {
                this.sound(level, ModSounds.MYSTIK_FIREWORK.get(), 2.5f, 1.3f);
                level.sendParticles(ParticleTypes.END_ROD, p.x, p.y + 0.5, p.z, 40, 0.6, 1.5, 0.6, 0.1);
                level.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, 0xFFFFF0C8), p.x, p.y + 1.0, p.z, 1, 0.0, 0.0, 0.0, 0.0);
                confetti(level, p.add(0.0, 1.0, 0.0), 20, 0.5, 0.25);
                for (LivingEntity victim : this.victims(level, new AABB(p.x - 1.9, p.y - 0.5, p.z - 1.9, p.x + 1.9, p.y + 3.0, p.z + 1.9))) {
                    if (victim.hurtServer(level, this.damageSources().indirectMagic(this, this), this.scaled(9.0f))) {
                        victim.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 30, 0), this);
                    }
                }
                SpellFxEntity pillar = this.pillars.get(i);
                if (pillar.isAlive()) {
                    pillar.discard();
                }
            }
        }
    }

    private LivingEntity nearestWatcher(ServerLevel level, Vec3 p, LivingEntity fallback) {
        Player near = level.getNearestPlayer(p.x, p.y, p.z, 24.0, e -> e instanceof Player pl && this.canStrike(pl));
        return near != null ? near : fallback;
    }

    /**
     * Zieh eine Karte! Über ihrem Ziel erscheint eine riesige Karte und dreht sich: Herz heilt sie, Pik fällt als Klinge auf
     * die markierte Stelle, Karo lässt Splitter in einem Ring regnen, Kreuz verflucht alle Nahen (langsam und schwach).
     */
    private void tickCardTrick(ServerLevel level, LivingEntity target) {
        int t = this.attackTick;
        this.setDeltaMovement(this.getDeltaMovement().scale(0.8));
        this.faceTarget(target, 10.0f);
        if (t == 4) {
            this.trickAt = this.ground(target.position());
            Suit[] suits = Suit.values();
            this.suit = this.forcedSuit != null ? this.forcedSuit : suits[this.random.nextInt(suits.length)];
            // in der Zugabe nie Herz (sie heilt sich nicht mehr)
            if (this.forcedSuit == null && this.isEncore() && this.suit == Suit.HEART) {
                this.suit = Suit.SPADE;
            }
            MagicCardEntity card = new MagicCardEntity(level, this);
            card.showAt(this.trickAt.add(0.0, 4.2, 0.0), this.suit.ordinal(), TRICK_FLIP - 4);
            level.addFreshEntity(card);
            this.bigCard = card;
            this.sound(level, ModSounds.MYSTIK_CARD_TRICK.get(), 3.0f, 1.0f);
            for (ServerPlayer player : level.players()) {
                if (player.distanceToSqr(this) < 40.0 * 40.0) {
                    player.sendOverlayMessage(Component.translatable("entity.kingdomsatwar.miss_mystik.pick_a_card").withColor(0xFFC8F0));
                }
            }
        }
        if (this.suit == null) {
            return;
        }
        Vec3 c = this.trickAt;
        if (t == TRICK_FLIP) {
            this.sound(level, ModSounds.MYSTIK_TADA.get(), 2.0f, 1.3f);
            confetti(level, c.add(0.0, 4.2, 0.0), 30, 0.8, 0.2);
        }
        switch (this.suit) {
            case HEART -> {
                if (t > TRICK_FLIP && t <= TRICK_FLIP + 20 && t % 2 == 0) {
                    this.heal(this.getMaxHealth() * 0.006f);
                    Vec3 from = c.add(0.0, 4.2, 0.0);
                    Vec3 to = this.position().add(0.0, 1.6, 0.0).subtract(from);
                    level.sendParticles(ParticleTypes.HEART, from.x + to.x * (t - TRICK_FLIP) / 20.0, from.y + to.y * (t - TRICK_FLIP) / 20.0,
                            from.z + to.z * (t - TRICK_FLIP) / 20.0, 2, 0.2, 0.2, 0.2, 0.0);
                }
            }
            case SPADE -> {
                if (t == TRICK_FLIP + 2) {
                    SpellFxEntity.create(level, SpellFxEntity.Kind.CIRCLE, c.add(0.0, 0.05, 0.0), null, 0xC8A0FF, 16, 2.4f).spawn();
                }
                if (this.bigCard != null && this.bigCard.isAlive() && t > TRICK_FLIP + 2 && t <= TRICK_FLIP + 18) {
                    // die Karte fällt wie ein Fallbeil
                    double k = (t - TRICK_FLIP - 2) / 16.0;
                    this.bigCard.setPos(c.x, c.y + 4.2 - 4.0 * k * k, c.z);
                }
                if (t == TRICK_FLIP + 18) {
                    this.sound(level, ModSounds.MYSTIK_STAB.get(), 3.0f, 0.7f);
                    level.sendParticles(ParticleTypes.CRIT, c.x, c.y + 0.5, c.z, 40, 1.2, 0.3, 1.2, 0.4);
                    level.sendParticles(ParticleTypes.SWEEP_ATTACK, c.x, c.y + 0.6, c.z, 4, 1.0, 0.1, 1.0, 0.0);
                    for (LivingEntity victim : this.victims(level, new AABB(c.x - 2.6, c.y - 0.5, c.z - 2.6, c.x + 2.6, c.y + 3.0, c.z + 2.6))) {
                        if (victim.distanceToSqr(c.x, victim.getY(), c.z) < 2.6 * 2.6) {
                            victim.hurtServer(level, this.damageSources().indirectMagic(this, this), this.scaled(14.0f));
                        }
                    }
                    if (this.bigCard != null && this.bigCard.isAlive()) {
                        this.bigCard.discard();
                    }
                }
            }
            case DIAMOND -> {
                if (t == TRICK_FLIP + 4) {
                    double base = this.random.nextDouble() * Math.PI * 2.0;
                    for (int i = 0; i < 9; i++) {
                        double r = i == 0 ? 0.0 : 3.2;
                        double a = base + i * Math.PI * 2.0 / 8.0;
                        this.marks.add(this.ground(c.add(Math.cos(a) * r, 0.0, Math.sin(a) * r)));
                        this.markTicks.add(TRICK_FLIP + 16 + (i % 3) * 3);
                        SpellFxEntity.create(level, SpellFxEntity.Kind.CIRCLE, this.marks.get(i).add(0.0, 0.05, 0.0), null, 0xFF7AB0,
                                12 + (i % 3) * 3, 1.2f).spawn();
                    }
                }
                for (int i = 0; i < this.marks.size(); i++) {
                    if (t == this.markTicks.get(i) - 8) {
                        // ein Splitter (eine kleine Karokarte) schießt aus der großen Karte auf die Stelle
                        Vec3 from = c.add(0.0, 4.2, 0.0);
                        Vec3 to = this.marks.get(i).add(0.0, 0.2, 0.0);
                        MagicCardEntity shard = new MagicCardEntity(level, this);
                        shard.setSuit(Suit.DIAMOND.ordinal());
                        shard.setDamage(this.scaled(6.0f));
                        shard.setPos(from.x, from.y, from.z);
                        shard.setDeltaMovement(to.subtract(from).scale(1.0 / 8.0));
                        level.addFreshEntity(shard);
                    }
                    if (t == this.markTicks.get(i)) {
                        Vec3 m = this.marks.get(i);
                        level.sendParticles(ParticleTypes.CRIT, m.x, m.y + 0.3, m.z, 10, 0.4, 0.2, 0.4, 0.3);
                        for (LivingEntity victim : this.victims(level, new AABB(m, m).inflate(1.3, 1.5, 1.3))) {
                            if (this.struck.add(victim.getUUID())) {
                                victim.hurtServer(level, this.damageSources().indirectMagic(this, this), this.scaled(6.0f));
                            }
                        }
                    }
                }
            }
            case CLUB -> {
                if (t == TRICK_FLIP + 6) {
                    this.sound(level, ModSounds.MYSTIK_LAUGH.get(), 2.5f, 0.8f);
                    level.sendParticles(ParticleTypes.WITCH, c.x, c.y + 1.5, c.z, 60, 3.0, 1.0, 3.0, 0.05);
                    for (LivingEntity victim : this.victims(level, new AABB(c, c).inflate(6.0, 3.0, 6.0))) {
                        victim.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 120, 1), this);
                        victim.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 120, 0), this);
                        victim.addEffect(new MobEffectInstance(MobEffects.GLOWING, 120, 0), this);
                    }
                }
            }
        }
        if (t == Attack.CARD_TRICK.duration - 4 && this.bigCard != null && this.bigCard.isAlive()) {
            confetti(level, this.bigCard.position(), 20, 0.6, 0.15);
            this.bigCard.discard();
        }
    }

    /**
     * Das große Finale: Sie steigt über die Bühne; Feuerwerk steigt auf und regnet als Funken herab (Lichtkreise warnen), dann
     * der letzte Knall – wer sie sehen kann und nicht hinter einer Säule, einer Bank oder dem Vorhang Schutz sucht, wird
     * schwer getroffen.
     */
    private void tickFinale(ServerLevel level) {
        int t = this.attackTick;
        if (t == 1) {
            // die Mitte der Vorstellung: ihr Zylinder (ohne Bühne: der Boden unter ihr, als das Finale begann)
            this.finaleCenter = this.home != null ? Vec3.atBottomCenterOf(this.home) : this.ground(this.position());
        }
        Vec3 stage = this.finaleCenter;
        // auf der Bühne: hoch über dem Bühnenrand vor dem Vorhang (dort sehen sie alle, auf der Bühne und im Zuschauerrund)
        Vec3 want = this.home != null ? stage.add(0.0, 6.0, 4.5) : stage.add(0.0, 7.5, 0.0);
        Vec3 to = want.subtract(this.position());
        this.setDeltaMovement(to.length() > 0.5 ? to.normalize().scale(0.5) : to.scale(0.3));
        LivingEntity target = this.getTarget();
        if (target != null) {
            this.faceTarget(target, 6.0f);
        }
        if (t == 2) {
            this.sound(level, ModSounds.MYSTIK_DRUMROLL.get(), 4.0f, 1.0f);
            for (ServerPlayer player : level.players()) {
                if (player.distanceToSqr(this) < 48.0 * 48.0) {
                    player.sendOverlayMessage(Component.translatable("entity.kingdomsatwar.miss_mystik.finale").withColor(0xFFD86A));
                }
            }
        }
        if (t >= 22 && t < 96 && t % 6 == 0) {
            // eine Rakete steigt vom Bühnenrand auf und regnet auf zwei Stellen herab
            double a = this.random.nextDouble() * Math.PI * 2.0;
            Vec3 rocket = stage.add(Math.cos(a) * 9.0, 1.0, Math.sin(a) * 9.0);
            level.sendParticles(ParticleTypes.FIREWORK, rocket.x, rocket.y, rocket.z, 0, 0.0, 1.0, 0.0, 1.2);
            level.sendParticles(ParticleTypes.FIREWORK, rocket.x, rocket.y + 9.0, rocket.z, 40, 1.2, 1.2, 1.2, 0.18);
            confetti(level, rocket.add(0.0, 9.0, 0.0), 24, 1.5, 0.25);
            this.sound(level, ModSounds.MYSTIK_FIREWORK.get(), 3.0f, 0.9f + this.random.nextFloat() * 0.3f);
            List<LivingEntity> watchers = this.victims(level, new AABB(stage, stage).inflate(22.0, 10.0, 22.0));
            for (int k = 0; k < 2; k++) {
                Vec3 m;
                if (!watchers.isEmpty() && this.random.nextInt(3) > 0) {
                    LivingEntity who = watchers.get(this.random.nextInt(watchers.size()));
                    m = this.ground(lead(who, 12.0));
                } else {
                    double b = this.random.nextDouble() * Math.PI * 2.0;
                    double r = 3.0 + this.random.nextDouble() * 10.0;
                    m = this.ground(stage.add(Math.cos(b) * r, 0.0, Math.sin(b) * r));
                }
                this.marks.add(m);
                this.markTicks.add(t + 20);
                SpellFxEntity.create(level, SpellFxEntity.Kind.CIRCLE, m.add(0.0, 0.05, 0.0), null, 0xFF9AE6, 20, 2.0f).spawn();
            }
        }
        for (int i = 0; i < this.marks.size(); i++) {
            if (this.markTicks.get(i) == t) {
                Vec3 m = this.marks.get(i);
                level.sendParticles(ParticleTypes.FIREWORK, m.x, m.y + 0.5, m.z, 20, 0.6, 0.3, 0.6, 0.15);
                level.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, 0xFFFF9AE6), m.x, m.y + 0.6, m.z, 1, 0.0, 0.0, 0.0, 0.0);
                for (LivingEntity victim : this.victims(level, new AABB(m, m).inflate(2.1, 2.0, 2.1))) {
                    victim.hurtServer(level, this.damageSources().indirectMagic(this, this), 7.0f);
                }
            }
        }
        if (t == FINALE_BLAST - 20) {
            this.sound(level, ModSounds.MYSTIK_TADA.get(), 4.0f, 0.8f);
        }
        if (t > FINALE_BLAST - 20 && t < FINALE_BLAST && t % 2 == 0) {
            Vec3 c = this.position().add(0.0, 1.4, 0.0);
            for (int i = 0; i < 6; i++) {
                double a = this.random.nextDouble() * Math.PI * 2.0;
                double b = (this.random.nextDouble() - 0.5) * Math.PI;
                Vec3 dir = new Vec3(Math.cos(a) * Math.cos(b), Math.sin(b), Math.sin(a) * Math.cos(b));
                Vec3 p = c.add(dir.scale(7.0));
                level.sendParticles(ModParticles.FAE_SPARK.get(), p.x, p.y, p.z, 0, -dir.x, -dir.y, -dir.z, 0.4);
            }
        }
        if (t != FINALE_BLAST) {
            return;
        }
        Vec3 c = this.position().add(0.0, 1.4, 0.0);
        this.sound(level, ModSounds.MYSTIK_FIREWORK.get(), 6.0f, 0.6f);
        level.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, 0xFFFFFFFF), c.x, c.y, c.z, 3, 0.5, 0.5, 0.5, 0.0);
        level.sendParticles(ParticleTypes.FIREWORK, c.x, c.y, c.z, 160, 0.5, 0.5, 0.5, 0.7);
        confetti(level, c, 200, 2.5, 0.6);
        for (LivingEntity victim : this.victims(level, this.getBoundingBox().inflate(FINALE_RADIUS))) {
            Vec3 eye = victim.getEyePosition();
            if (eye.distanceTo(c) > FINALE_RADIUS) {
                continue;
            }
            HitResult block = level.clip(new ClipContext(c, eye, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
            if (block.getType() != HitResult.Type.MISS) {
                continue;
            }
            victim.hurtServer(level, this.damageSources().indirectMagic(this, this), 13.0f);
            push(victim, new Vec3(eye.x - c.x, 0.0, eye.z - c.z).normalize().scale(1.1).add(0.0, 0.45, 0.0));
        }
    }

    // ------------------------------------------------------------------ Tod

    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (this.level() instanceof ServerLevel level) {
            this.clearPillars();
            this.dispelImages(level, false);
            MagicHatEntity hat = this.hat(level);
            if (hat != null) {
                hat.discard();
            }
            this.entityData.set(DATA_HAT, true);
            if (this.home != null && level.getBlockEntity(this.home) instanceof StageHatBlockEntity stageHat && stageHat.owns(this)) {
                stageHat.onPerformerDefeated(level);
            }
        }
    }

    /** Ihre letzte Verbeugung: sie sinkt zur Bühne, verneigt sich tief – Applaus – und vergeht in einem Regen aus Konfetti. */
    @Override
    protected void tickDeath() {
        this.deathTime++;
        Vec3 low = this.ground(this.position()).add(0.0, 0.8, 0.0);
        this.setDeltaMovement(0.0, Mth.clamp(low.y - this.getY(), -0.12, 0.05), 0.0);
        if (!(this.level() instanceof ServerLevel level)) {
            return;
        }
        if (this.deathTime % 4 == 0) {
            level.sendParticles(ModParticles.FAE_SPARK.get(), this.getX(), this.getY(1.0), this.getZ(), 6, 0.6, 0.8, 0.6, 0.02);
        }
        if (this.deathTime == 20) {
            this.sound(level, ModSounds.MYSTIK_APPLAUSE.get(), 4.0f, 1.0f);
        }
        if (this.deathTime == DEATH_TICKS - 4) {
            this.sound(level, ModSounds.MYSTIK_TADA.get(), 3.5f, 1.2f);
            confetti(level, this.position().add(0.0, 1.6, 0.0), 220, 1.6, 0.5);
            level.sendParticles(SMOKE, this.getX(), this.getY(1.0), this.getZ(), 60, 0.7, 1.0, 0.7, 0.03);
            level.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, 0xFFFF9AE6), this.getX(), this.getY(1.2), this.getZ(), 1, 0.0, 0.0,
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
        this.clearPillars();
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        if (this.home != null) {
            output.putLong("Home", this.home.asLong());
        }
        output.putBoolean("Encore", this.isEncore());
        output.putBoolean("Finale", this.finaleDone);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        input.getLong("Home").ifPresent(h -> this.home = BlockPos.of(h));
        this.entityData.set(DATA_ENCORE, input.getBooleanOr("Encore", false));
        this.finaleDone = input.getBooleanOr("Finale", false);
        // nach dem Laden: Hut auf, sichtbar, kein halber Trick
        this.entityData.set(DATA_HAT, true);
        this.setHidden(false);
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return this.isHidden() ? null : ModSounds.MYSTIK_AMBIENT.get();
    }

    @Override
    public int getAmbientSoundInterval() {
        return 160;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.MYSTIK_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.MYSTIK_DEATH.get();
    }

    @Override
    protected float getSoundVolume() {
        return 2.5f;
    }
}
