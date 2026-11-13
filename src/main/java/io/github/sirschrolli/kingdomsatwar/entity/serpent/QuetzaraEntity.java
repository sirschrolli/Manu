package io.github.sirschrolli.kingdomsatwar.entity.serpent;

import io.github.sirschrolli.kingdomsatwar.block.serpent.SerpentAltarBlockEntity;
import io.github.sirschrolli.kingdomsatwar.entity.boss.sky.SkyTheme;
import io.github.sirschrolli.kingdomsatwar.entity.boss.sky.SkyWarden;
import io.github.sirschrolli.kingdomsatwar.item.serpent.SerpentFangItem;
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
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
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
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.control.MoveControl;
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
import net.neoforged.neoforge.entity.PartEntity;
import org.jspecify.annotations.Nullable;

/**
 * Quetzara, die Gefiederte Schlange (seit 1.10): die Hüterin des Schlangentempels – eine Riesenschlange mit smaragdgrünen
 * Schuppen und goldenem Bauch, auf dem Kopf ein Kamm aus Quetzalfedern, um den Hals eine Federkrause, die sie wie eine
 * Kobra spreizt, am Schwanz ein Federbusch. Sie schläft eingerollt um den Schlangenaltar in der Grube unter dem Tempel
 * ({@link SerpentAltarBlockEntity}); wer hinabsteigt, weckt sie: Sie richtet sich auf, spreizt die Krause und zischt.
 *
 * <p>Ihr Leib besteht aus {@link #SEGMENTS} Gliedern und dem Schwanz: Sie folgen der Spur, die ihr Nacken über den Boden
 * zieht – die Schlange windet sich (Trefferzonen siehe {@link QuetzaraPart}; der Kopf ist verwundbar, die Schuppen halten
 * viel ab). Sie kämpft so:
 * <ul>
 *     <li><b>Giftbiss</b> – sie zieht den Kopf zurück und schnellt vor: schwerer Schaden und starkes Gift.</li>
 *     <li><b>Würgegriff</b> – sie umkreist ihr Opfer, rollt sich um es und drückt zu; wer ihr in der Umschlingung genug Schaden
 *     zufügt, kommt frei (dann ist sie kurz benommen und ihr Kopf liegt am Boden).</li>
 *     <li><b>Schwanzpeitsche</b> – wer hinter ihr steht, den fegt der Schwanz davon.</li>
 *     <li><b>Federsturm</b> – hoch aufgerichtet spreizt sie die Krause und schleudert Fächer aus vergifteten Federn.</li>
 *     <li><b>Giftspucke</b> – ein Klumpen Gift im Bogen, der zu einer Giftwolke zerplatzt.</li>
 *     <li><b>Häutung</b> – unter halber Kraft streift sie ihre Haut ab: Die leere Haut bleibt als <b>Täuschung</b> liegen
 *     (sie zischt, und wer sie trifft, dem platzt sie als Giftwolke entgegen), während die Schlange selbst getarnt davongleitet
 *     und aus dem Hinterhalt zubeißt. Danach leuchtet ihr neues Schuppenkleid, und sie ist schneller und giftiger.</li>
 * </ul>
 * Findet sie niemanden mehr, kriecht sie zum Altar zurück, rollt sich ein und schläft (und heilt).
 */
public class QuetzaraEntity extends Monster implements SkyWarden {
    public static final float MAX_HEALTH = 780.0f;
    /** Erfahrung beim Sieg (wie bei allen Bossen, die wiederkommen). */
    public static final int XP_REWARD = 450;
    /** Ihre Farben: Jade (Schuppen, Leiste, Meldungen), Gold (Bauch, Federkiele) und das Grün ihres Gifts. */
    public static final int JADE = 0x3FD98A;
    public static final int GOLD = 0xF2C230;
    public static final int VENOM = SerpentFangItem.VENOM;
    /** Die Farbe ihrer abgestreiften Haut. */
    public static final int SKIN = 0xD9D0AE;

    // ---- der Leib
    public static final int SEGMENTS = 12;
    /** Dicke der Glieder (Index {@link #SEGMENTS}: der Schwanz): der Hals schlanker, der Leib voll, zum Schwanz hin dünner. */
    public static final float[] GIRTH = {0.82f, 0.9f, 0.98f, 1.0f, 1.0f, 1.0f, 0.97f, 0.9f, 0.82f, 0.73f, 0.64f, 0.55f, 0.48f};
    /** Abstand zweier Glieder der Dicke 1 (Blöcke). */
    public static final double LINK = 1.3;
    /** Vom Nacken (wo der Kopf ansetzt) bis zum ersten Glied. */
    public static final double NECK = 0.75;
    /** Höhe der Mitte eines Glieds der Dicke 1 über dem Boden. */
    public static final double BODY_Y = 0.72;
    /** Vom Nacken bis zur Schnauze. */
    public static final double HEAD_LENGTH = 2.1;
    /** Höhe des Nackens, wenn sie flach liegt. */
    public static final float LIE = 0.62f;
    /** Abstand jedes Glieds vom Nacken, die Spur entlang. */
    public static final double[] OFFSET = new double[SEGMENTS + 1];
    private static final int TRAIL = 640;
    private static final double TRAIL_STEP = 0.06;
    /** Die Spirale, in der sie schläft: innen am Nacken, außen am Schwanz. */
    public static final double COIL_INNER = 1.4;
    public static final double COIL_OUTER = 2.85;

    // ---- der Kampf
    /** Weiter als so weit (waagerecht) vom Altar folgt sie niemandem … */
    public static final double ARENA = 21.0;
    /** … und wer sie weiter hinauslockt, dem entkommt sie zum Altar zurück. */
    public static final double LEASH = 25.0;
    public static final int AWAKEN_TICKS = 100;
    public static final int DEATH_TICKS = 90;
    /** So lange ohne Gegner, dann kriecht sie zum Altar zurück. */
    public static final int RETURN_TICKS = 300;
    public static final float BITE_DAMAGE = 13.0f;
    public static final int BITE_LUNGE = 12;
    public static final double BITE_REACH = 1.45;
    public static final float WHIP_DAMAGE = 11.0f;
    /** Ab diesem Glied schwingt der Schwanz. */
    public static final int WHIP_FROM = 7;
    public static final int WHIP_STRIKE = 12;
    public static final float SQUEEZE_DAMAGE = 5.0f;
    public static final float CRUSH_DAMAGE = 12.0f;
    public static final double COIL_RADIUS = 1.75;
    public static final int HOLD_TICKS = 100;
    /** So viel Schaden (vor Rüstung und Schuppen) muss sie in der Umschlingung nehmen, dann lässt sie los. */
    public static final float ESCAPE_DAMAGE = 40.0f;
    public static final int CONSTRICT_COOLDOWN = 360;
    public static final int[] FEATHER_VOLLEYS = {20, 30, 40, 50};
    public static final int SPIT_RELEASE = 14;
    public static final int MOLT_SPLIT = 34;
    /** So lange bleibt sie nach der Häutung getarnt (oder bis sie zubeißt). */
    public static final int CAMO_TICKS = 140;
    /** Wie viel härter ihr Kopf getroffen wird, solange sie benommen ist. */
    public static final float STUN_FACTOR = 1.5f;

    private static final Identifier COIL_SLOW = Identifier.fromNamespaceAndPath("kingdomsatwar", "quetzara_coil_slow");
    private static final Identifier COIL_JUMP = Identifier.fromNamespaceAndPath("kingdomsatwar", "quetzara_coil_jump");

    static {
        OFFSET[0] = NECK;
        for (int i = 1; i <= SEGMENTS; i++) {
            OFFSET[i] = OFFSET[i - 1] + LINK * (GIRTH[i - 1] + GIRTH[i]) * 0.5;
        }
    }

    public enum Attack {
        NONE(0), SLEEP(0), AWAKEN(AWAKEN_TICKS), BITE(34), SPIT(36), FEATHERS(66), WHIP(38), CONSTRICT(220), MOLT(84), STUN(44), RETURN(0);

        public final int duration;

        Attack(int duration) {
            this.duration = duration;
        }
    }

    private static final EntityDataAccessor<Integer> DATA_ATTACK = SynchedEntityData.defineId(QuetzaraEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DATA_REAR = SynchedEntityData.defineId(QuetzaraEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_RUFF = SynchedEntityData.defineId(QuetzaraEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_JAW = SynchedEntityData.defineId(QuetzaraEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> DATA_MOLTED = SynchedEntityData.defineId(QuetzaraEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_CAMO = SynchedEntityData.defineId(QuetzaraEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_SIDE = SynchedEntityData.defineId(QuetzaraEntity.class, EntityDataSerializers.INT);
    /** Für Tests und die Vorführung: wie oft sie hingelegt wurde, und wie gekrümmt (Grad je Block). */
    private static final EntityDataAccessor<Integer> DATA_LAYOUT = SynchedEntityData.defineId(QuetzaraEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DATA_BEND = SynchedEntityData.defineId(QuetzaraEntity.class, EntityDataSerializers.FLOAT);

    private final ServerBossEvent bossBar = new ServerBossEvent(UUID.randomUUID(), Component.translatable("boss.kingdomsatwar.quetzara"),
            BossEvent.BossBarColor.GREEN, BossEvent.BossBarOverlay.NOTCHED_10);
    /** [0] der Kopf, [1 … SEGMENTS] die Glieder, [SEGMENTS + 1] der Schwanz. */
    private final QuetzaraPart[] parts = new QuetzaraPart[SEGMENTS + 2];
    // die Spur des Nackens über den Boden (Fußpunkte), neueste zuerst
    private final double[] trailX = new double[TRAIL];
    private final double[] trailY = new double[TRAIL];
    private final double[] trailZ = new double[TRAIL];
    private int trailStart;
    private int trailCount;
    /** Mittelpunkte der Glieder (Index SEGMENTS = Schwanz) und der Nacken – in diesem und im letzten Tick. */
    private final Vec3[] bodyNow = new Vec3[SEGMENTS + 1];
    private final Vec3[] bodyOld = new Vec3[SEGMENTS + 1];
    private Vec3 jointNow = Vec3.ZERO;
    private Vec3 jointOld = Vec3.ZERO;
    // weich nachgeführte Haltung (beide Seiten rechnen gleich)
    private float rear = LIE;
    private float rearO = LIE;
    private float ruff;
    private float ruffO;
    private float jaw;
    private float jawO;

    private Attack attack = Attack.NONE;
    private Attack lastAttack = Attack.NONE;
    private int attackTick;
    private int cooldown = 30;
    private int idleTicks;
    private int returnTicks;
    private @Nullable BlockPos altar;
    private Direction altarFacing = Direction.NORTH;
    private @Nullable Vec3 home;
    private final Set<UUID> struck = new HashSet<>();
    // der Würgegriff
    private @Nullable UUID coilVictim;
    private Vec3 coilCenter = Vec3.ZERO;
    private double coilAngle;
    private double coilTurned;
    private int coilSign = 1;
    private float coilDamage;
    private boolean holding;
    private int holdTick;
    private int lastConstrict = -CONSTRICT_COOLDOWN;
    // die Häutung
    private int camoTicks;
    private boolean ambush;
    private final List<UUID> skins = new ArrayList<>();
    private Vec3 moltGoal = Vec3.ZERO;
    private int clientAttackStart;
    private int sleepSeen;
    private int layoutSeen = -1;
    private boolean seeded;
    private Vec3 lastAnchor = Vec3.ZERO;
    /** Für Tests: nur wer so nah (waagerecht) ist, wird getroffen und gejagt. */
    private double confine = Double.MAX_VALUE;

    public QuetzaraEntity(EntityType<? extends QuetzaraEntity> type, Level level) {
        super(type, level);
        // die Zonen sofort anlegen: ihre Kennungen folgen auf die der Schlange (wie beim Enderdrachen)
        this.parts[0] = new QuetzaraPart(this, -1, 1.5f, 1.15f, 1.0f);
        for (int i = 0; i <= SEGMENTS; i++) {
            float g = GIRTH[i];
            this.parts[i + 1] = new QuetzaraPart(this, i, 1.5f * g, 1.4f * g, i == SEGMENTS ? 0.6f : 0.5f);
        }
        for (int i = 0; i <= SEGMENTS; i++) {
            this.bodyNow[i] = Vec3.ZERO;
            this.bodyOld[i] = Vec3.ZERO;
        }
        this.xpReward = XP_REWARD;
        this.lookControl = new HeadControl(this);
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, MAX_HEALTH)
                .add(Attributes.ARMOR, 10.0)
                .add(Attributes.ARMOR_TOUGHNESS, 4.0)
                .add(Attributes.ATTACK_DAMAGE, BITE_DAMAGE)
                .add(Attributes.MOVEMENT_SPEED, 0.27)
                .add(Attributes.FOLLOW_RANGE, 40.0)
                .add(Attributes.STEP_HEIGHT, 1.1)
                .add(Attributes.WATER_MOVEMENT_EFFICIENCY, 1.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ATTACK, Attack.NONE.ordinal());
        builder.define(DATA_REAR, LIE);
        builder.define(DATA_RUFF, 0.0f);
        builder.define(DATA_JAW, 0.0f);
        builder.define(DATA_MOLTED, false);
        builder.define(DATA_CAMO, false);
        builder.define(DATA_SIDE, 1);
        builder.define(DATA_LAYOUT, 0);
        builder.define(DATA_BEND, 0.0f);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (DATA_ATTACK.equals(key)) {
            this.clientAttackStart = this.tickCount;
            if (this.level().isClientSide()) {
                this.attack = this.currentAttack();
            }
        }
        if (DATA_LAYOUT.equals(key) && this.level().isClientSide()) {
            this.layoutSeen = 0;
        }
    }

    // ------------------------------------------------------------------ Zustand (für Bild und Tests)

    public Attack currentAttack() {
        int i = this.entityData.get(DATA_ATTACK);
        return i >= 0 && i < Attack.values().length ? Attack.values()[i] : Attack.NONE;
    }

    /** Ticks seit Beginn des Angriffs (im Bild weich). */
    public float attackTime(float partialTicks) {
        if (!this.level().isClientSide()) {
            return this.attackTick + partialTicks;
        }
        return this.tickCount - this.clientAttackStart + partialTicks;
    }

    public boolean isAsleep() {
        return this.currentAttack() == Attack.SLEEP;
    }

    public boolean isAwakening() {
        return this.currentAttack() == Attack.AWAKEN;
    }

    /** Hat sie sich gehäutet (neues, leuchtendes Schuppenkleid; schneller, giftiger)? */
    public boolean isMolted() {
        return this.entityData.get(DATA_MOLTED);
    }

    /** Ist sie nach der Häutung getarnt (kaum zu sehen)? */
    public boolean isCamouflaged() {
        return this.entityData.get(DATA_CAMO);
    }

    /** Nach welcher Seite der Schwanz peitscht (+1 / −1). */
    public int whipSide() {
        return this.entityData.get(DATA_SIDE);
    }

    public float rear(float partialTicks) {
        return Mth.lerp(partialTicks, this.rearO, this.rear);
    }

    public float ruff(float partialTicks) {
        return Mth.lerp(partialTicks, this.ruffO, this.ruff);
    }

    public float jaw(float partialTicks) {
        return Mth.lerp(partialTicks, this.jawO, this.jaw);
    }

    /** Mittelpunkt von Glied {@code i} (SEGMENTS = Schwanz) – weich zwischen zwei Ticks. */
    public Vec3 bodyPoint(int i, float partialTicks) {
        Vec3 a = this.bodyOld[i];
        Vec3 b = this.bodyNow[i];
        return new Vec3(Mth.lerp(partialTicks, a.x, b.x), Mth.lerp(partialTicks, a.y, b.y), Mth.lerp(partialTicks, a.z, b.z));
    }

    /** Der Nacken (wo der Kopf ansetzt) – weich zwischen zwei Ticks. */
    public Vec3 joint(float partialTicks) {
        return new Vec3(Mth.lerp(partialTicks, this.jointOld.x, this.jointNow.x), Mth.lerp(partialTicks, this.jointOld.y, this.jointNow.y),
                Mth.lerp(partialTicks, this.jointOld.z, this.jointNow.z));
    }

    /** Blickrichtung des Kopfes. */
    public Vec3 headDir() {
        return Vec3.directionFromRotation(this.getXRot(), this.getYHeadRot());
    }

    /** Die Schnauze (vorn am Kopf). */
    public Vec3 snout() {
        return this.jointNow.add(this.headDir().scale(HEAD_LENGTH));
    }

    public @Nullable BlockPos altar() {
        return this.altar;
    }

    public @Nullable Vec3 home() {
        return this.home;
    }

    public QuetzaraPart[] serpentParts() {
        return this.parts;
    }

    /** Für Tests: Ihre Angriffe treffen (und sie jagt) nur, wer so nah (waagerecht) bei ihr ist. */
    public void confine(double radius) {
        this.confine = radius;
    }

    public @Nullable UUID coilVictim() {
        return this.holding ? this.coilVictim : null;
    }

    public List<UUID> skins() {
        return this.skins;
    }

    // ------------------------------------------------------------------ Trefferzonen

    @Override
    public boolean isMultipartEntity() {
        return true;
    }

    @Override
    public PartEntity<?>[] getParts() {
        return this.parts;
    }

    @Override
    public void recreateFromPacket(ClientboundAddEntityPacket packet) {
        super.recreateFromPacket(packet);
        for (int i = 0; i < this.parts.length; i++) {
            this.parts[i].setId(i + packet.getId() + 1);
        }
    }

    // ------------------------------------------------------------------ die Spur und der Leib

    private void pushTrail(Vec3 p) {
        if (this.trailCount > 0) {
            double dx = p.x - this.trailX[this.trailStart];
            double dy = p.y - this.trailY[this.trailStart];
            double dz = p.z - this.trailZ[this.trailStart];
            if (dx * dx + dy * dy + dz * dz < TRAIL_STEP * TRAIL_STEP) {
                return;
            }
        }
        this.trailStart = (this.trailStart + TRAIL - 1) % TRAIL;
        this.trailX[this.trailStart] = p.x;
        this.trailY[this.trailStart] = p.y;
        this.trailZ[this.trailStart] = p.z;
        this.trailCount = Math.min(TRAIL, this.trailCount + 1);
    }

    /** Punkt auf der Spur {@code distance} Blöcke hinter {@code from} (über das Ende hinaus: gerade nach hinten verlängert). */
    private Vec3 trailPoint(Vec3 from, double distance) {
        double left = distance;
        double px = from.x;
        double py = from.y;
        double pz = from.z;
        for (int k = 0; k < this.trailCount; k++) {
            int idx = (this.trailStart + k) % TRAIL;
            double qx = this.trailX[idx];
            double qy = this.trailY[idx];
            double qz = this.trailZ[idx];
            double d = Math.sqrt((qx - px) * (qx - px) + (qy - py) * (qy - py) + (qz - pz) * (qz - pz));
            if (d >= left && d > 1.0e-6) {
                double t = left / d;
                return new Vec3(px + (qx - px) * t, py + (qy - py) * t, pz + (qz - pz) * t);
            }
            left -= d;
            px = qx;
            py = qy;
            pz = qz;
        }
        Vec3 back = this.bodyDir().scale(-1.0);
        if (this.trailCount >= 2) {
            int last = (this.trailStart + this.trailCount - 1) % TRAIL;
            int prev = (this.trailStart + this.trailCount - 2) % TRAIL;
            Vec3 dir = new Vec3(this.trailX[last] - this.trailX[prev], 0.0, this.trailZ[last] - this.trailZ[prev]);
            if (dir.lengthSqr() > 1.0e-6) {
                back = dir.normalize();
            }
        }
        return new Vec3(px, py, pz).add(back.scale(left));
    }

    /** Waagerechte Richtung des Leibs am Nacken (wohin sie kriecht). */
    private Vec3 bodyDir() {
        float yaw = this.getYRot() * Mth.DEG_TO_RAD;
        return new Vec3(-Mth.sin(yaw), 0.0, Mth.cos(yaw));
    }

    /**
     * Legt die Spur neu als Spirale, in der sie schläft: Der Nacken liegt innen, {@link #COIL_INNER} links von der Mitte (in
     * Blickrichtung des Leibs), der Leib windet sich gut eine Runde nach außen bis {@link #COIL_OUTER}. Beide Seiten rechnen
     * dasselbe aus Lage und Drehung des Leibs.
     */
    private void seedCoil() {
        Vec3 a = this.position();
        Vec3 fwd = this.bodyDir();
        Vec3 right = new Vec3(-fwd.z, 0.0, fwd.x);
        Vec3 c = a.add(right.scale(COIL_INNER));
        double phi0 = Math.atan2(a.z - c.z, a.x - c.x);
        // vorwärts (Blickrichtung) heißt: Winkel nimmt zu oder ab?
        double sign = Math.signum(fwd.x * -Math.sin(phi0) + fwd.z * Math.cos(phi0));
        if (sign == 0.0) {
            sign = 1.0;
        }
        double length = OFFSET[SEGMENTS] + 3.0;
        double k = (COIL_OUTER - COIL_INNER) / OFFSET[SEGMENTS];
        this.trailCount = 0;
        this.trailStart = 0;
        int steps = (int) Math.ceil(length / TRAIL_STEP);
        // vom Schwanzende zum Nacken (die neuesten Punkte zuletzt)
        for (int n = steps; n >= 0; n--) {
            double s = n * TRAIL_STEP;
            double turned;
            double r;
            if (s <= OFFSET[SEGMENTS]) {
                r = COIL_INNER + k * s;
                turned = Math.log(r / COIL_INNER) / k;
            } else {
                r = COIL_OUTER;
                turned = Math.log(COIL_OUTER / COIL_INNER) / k + (s - OFFSET[SEGMENTS]) / COIL_OUTER;
            }
            double phi = phi0 - sign * turned;
            this.trailStart = (this.trailStart + TRAIL - 1) % TRAIL;
            this.trailX[this.trailStart] = c.x + Math.cos(phi) * r;
            this.trailY[this.trailStart] = a.y;
            this.trailZ[this.trailStart] = c.z + Math.sin(phi) * r;
            this.trailCount = Math.min(TRAIL, this.trailCount + 1);
        }
        this.seeded = true;
        this.lastAnchor = a;
        for (int i = 0; i <= SEGMENTS; i++) {
            this.bodyNow[i] = Vec3.ZERO;
        }
        this.layBody();
    }

    /**
     * Für Tests und die Vorführung: legt sie bei {@code at} hin, der Leib in Blickrichtung {@code bodyYaw}, nach hinten in einem
     * Bogen ({@code bend}: Grad je Block, positiv: nach rechts gekrümmt). Beide Seiten legen die Spur gleich.
     */
    public void layOut(Vec3 at, float bodyYaw, float bend) {
        this.snapTo(at.x, at.y, at.z, bodyYaw, 0.0f);
        this.setYHeadRot(bodyYaw);
        this.yBodyRot = bodyYaw;
        this.setDeltaMovement(Vec3.ZERO);
        this.getNavigation().stop();
        this.entityData.set(DATA_BEND, bend);
        this.entityData.set(DATA_LAYOUT, this.entityData.get(DATA_LAYOUT) + 1);
        this.seedLine(bend);
    }

    private void seedLine(float bend) {
        Vec3 a = this.position();
        Vec3 back = this.bodyDir().scale(-1.0);
        double total = OFFSET[SEGMENTS] + 4.0;
        int steps = (int) Math.ceil(total / TRAIL_STEP);
        Vec3[] pts = new Vec3[steps + 1];
        Vec3 p = a;
        for (int n = 0; n <= steps; n++) {
            pts[n] = p;
            p = p.add(back.scale(TRAIL_STEP));
            back = back.yRot((float) (-bend * TRAIL_STEP * Mth.DEG_TO_RAD));
        }
        this.trailCount = 0;
        this.trailStart = 0;
        for (int n = steps; n >= 0; n--) {
            this.trailStart = (this.trailStart + TRAIL - 1) % TRAIL;
            this.trailX[this.trailStart] = pts[n].x;
            this.trailY[this.trailStart] = pts[n].y;
            this.trailZ[this.trailStart] = pts[n].z;
            this.trailCount = Math.min(TRAIL, this.trailCount + 1);
        }
        this.lastAnchor = a;
        for (int i = 0; i <= SEGMENTS; i++) {
            this.bodyNow[i] = Vec3.ZERO;
        }
        this.layBody();
    }

    /**
     * Legt die Glieder: vom Nacken einen Bogen hinab zum Boden (je höher sie sich aufrichtet, desto steiler und weiter hinten
     * setzt er auf), dann die Spur entlang. Beim Peitschen schwingt der Schwanz um Glied {@link #WHIP_FROM}.
     */
    private void layBody() {
        Vec3 anchor = this.position();
        // ein Sprung (Teleport): die alte Spur gilt nicht mehr
        if (this.lastAnchor != Vec3.ZERO && this.lastAnchor.distanceToSqr(anchor) > 9.0) {
            this.trailCount = 0;
        }
        this.lastAnchor = anchor;
        this.pushTrail(anchor);
        float r = this.rear;
        Vec3 joint = anchor.add(0.0, r, 0.0);
        double e = Math.max(0.0, r - LIE);
        double b = Math.min(e * 0.5, 2.3);
        Vec3 baseGround = this.trailPoint(anchor, b);
        Vec3 base = baseGround.add(0.0, BODY_Y * GIRTH[0], 0.0);
        // der Hals: eine Kurve vom Nacken (erst senkrecht hinab, je höher desto mehr) bis zum Aufsetzpunkt (dort die Spur entlang)
        Vec3 head = this.headDir();
        Vec3 headFlat = new Vec3(head.x, 0.0, head.z);
        headFlat = headFlat.lengthSqr() < 1.0e-4 ? this.bodyDir() : headFlat.normalize();
        double up = Mth.clamp(e / 1.6, 0.0, 1.0);
        Vec3 t0 = headFlat.scale(-(1.0 - up)).add(0.0, -up, 0.0);
        Vec3 along = anchor.subtract(baseGround).multiply(1.0, 0.0, 1.0);
        along = along.lengthSqr() < 1.0e-4 ? this.bodyDir() : along.normalize();
        double span = joint.distanceTo(base);
        Vec3 p0 = joint;
        Vec3 p1 = joint.add(t0.scale(span * 0.5));
        Vec3 p2 = base.add(along.scale(span * 0.45));
        Vec3 p3 = base;
        // Bogenlänge der Kurve (in Stücken)
        int n = 16;
        double[] cum = new double[n + 1];
        Vec3[] pts = new Vec3[n + 1];
        pts[0] = p0;
        for (int k = 1; k <= n; k++) {
            pts[k] = bezier(p0, p1, p2, p3, k / (double) n);
            cum[k] = cum[k - 1] + pts[k].distanceTo(pts[k - 1]);
        }
        double arc = e < 0.02 ? 0.0 : cum[n];
        Attack shown = this.currentAttack();
        float wt = this.attackTime(0.0f);
        Vec3 pivot = null;
        Vec3 prevRaw = null;
        Vec3 prevOut = null;
        for (int i = 0; i <= SEGMENTS; i++) {
            double d = OFFSET[i];
            Vec3 p;
            if (d <= arc) {
                int k = 1;
                while (k < n && cum[k] < d) {
                    k++;
                }
                double seg = cum[k] - cum[k - 1];
                double t = seg < 1.0e-6 ? 0.0 : (d - cum[k - 1]) / seg;
                p = pts[k - 1].lerp(pts[k], t);
            } else {
                p = this.trailPoint(anchor, b + d - arc).add(0.0, BODY_Y * GIRTH[i], 0.0);
            }
            // die Schwanzpeitsche: die Glieder ab WHIP_FROM drehen sich um das Glied davor, je weiter hinten desto mehr
            if (shown == Attack.WHIP && i >= WHIP_FROM && this.deathTime <= 0) {
                if (pivot == null) {
                    pivot = this.bodyNow[WHIP_FROM - 1];
                    prevRaw = pivot;
                    prevOut = pivot;
                }
                double angle = whipAngle(wt) * this.whipSide() * (i - WHIP_FROM + 1) / (double) (SEGMENTS - WHIP_FROM + 1);
                Vec3 v = p.subtract(prevRaw);
                double cs = Math.cos(angle);
                double sn = Math.sin(angle);
                Vec3 rv = new Vec3(v.x * cs - v.z * sn, v.y, v.x * sn + v.z * cs);
                prevRaw = p;
                p = prevOut.add(rv).add(0.0, whipLift(wt) * (i - WHIP_FROM + 1) / (double) (SEGMENTS - WHIP_FROM + 1) * 0.25, 0.0);
                prevOut = p;
            }
            this.bodyOld[i] = this.bodyNow[i] == Vec3.ZERO ? p : this.bodyNow[i];
            this.bodyNow[i] = p;
            QuetzaraPart part = this.parts[i + 1];
            part.setOldPosAndRot();
            part.setPos(p.x, p.y - part.getBbHeight() / 2.0, p.z);
        }
        this.jointOld = this.jointNow == Vec3.ZERO ? joint : this.jointNow;
        this.jointNow = joint;
        Vec3 hc = joint.add(head.scale(HEAD_LENGTH * 0.55));
        QuetzaraPart headPart = this.parts[0];
        headPart.setOldPosAndRot();
        headPart.setPos(hc.x, hc.y - headPart.getBbHeight() / 2.0, hc.z);
    }

    private static Vec3 bezier(Vec3 p0, Vec3 p1, Vec3 p2, Vec3 p3, double t) {
        double u = 1.0 - t;
        double a = u * u * u;
        double b = 3.0 * u * u * t;
        double c = 3.0 * u * t * t;
        double d = t * t * t;
        return new Vec3(a * p0.x + b * p1.x + c * p2.x + d * p3.x, a * p0.y + b * p1.y + c * p2.y + d * p3.y,
                a * p0.z + b * p1.z + c * p2.z + d * p3.z);
    }

    /** Winkel des Schwanzes (Bogenmaß) beim Peitschen: ausholen, durchziehen, zurück. */
    public static double whipAngle(float t) {
        if (t <= 0.0f) {
            return 0.0;
        }
        if (t < WHIP_STRIKE) {
            return -0.7 * smooth(t / WHIP_STRIKE);
        }
        if (t < WHIP_STRIKE + 7) {
            return -0.7 + 3.1 * smooth((t - WHIP_STRIKE) / 7.0);
        }
        if (t < 36.0f) {
            return 2.4 * (1.0 - smooth((t - WHIP_STRIKE - 7) / (36.0 - WHIP_STRIKE - 7)));
        }
        return 0.0;
    }

    /** Wie hoch der Schwanz beim Peitschen gehoben ist (0 … 1). */
    public static double whipLift(float t) {
        if (t <= 0.0f || t >= 36.0f) {
            return 0.0;
        }
        return Math.sin(Math.PI * t / 36.0);
    }

    private static double smooth(double x) {
        double c = Mth.clamp(x, 0.0, 1.0);
        return c * c * (3.0 - 2.0 * c);
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

    /** Ihr eigenes Gift kann ihr nichts anhaben. */
    @Override
    public boolean canBeAffected(MobEffectInstance effect) {
        return !effect.is(MobEffects.POISON) && super.canBeAffected(effect);
    }

    /** Der Kopf dreht sich frei (der Leib folgt der Spur). */
    @Override
    public int getMaxHeadYRot() {
        return 180;
    }

    @Override
    protected void tickHeadTurn(float yBodyRotT) {
        this.yBodyRot = this.getYRot();
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        if (this.tickCount % 3 == 0) {
            this.playSound(ModSounds.QUETZARA_SLITHER.get(), 0.6f, 0.9f + this.random.nextFloat() * 0.2f);
        }
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

    /** Schlafend, beim Erwachen und im Häuten unverwundbar; Stürze, Wände und Ertrinken schaden ihr nicht. */
    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        return this.hurtFrom(level, source, amount, 1.0f);
    }

    /** Ein Treffer auf eine Zone: mit ihrem Schadensfaktor (benommen ist der Kopf noch verwundbarer). */
    public boolean hurtFromPart(ServerLevel level, QuetzaraPart part, DamageSource source, float amount) {
        float factor = part.damageFactor;
        if (part.isHead() && this.attack == Attack.STUN) {
            factor *= STUN_FACTOR;
        }
        return this.hurtFrom(level, source, amount, factor);
    }

    private boolean hurtFrom(ServerLevel level, DamageSource source, float amount, float factor) {
        if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return super.hurtServer(level, source, amount);
        }
        if (this.deathTime > 0) {
            return false;
        }
        if (this.attack == Attack.SLEEP) {
            if (source.getEntity() instanceof Player player && !player.isCreative() && !player.isSpectator()) {
                this.wake(level);
            }
            return false;
        }
        if (this.attack == Attack.AWAKEN || this.attack == Attack.MOLT && this.attackTick <= MOLT_SPLIT
                || source.is(DamageTypes.IN_WALL) || source.is(DamageTypes.FALL) || source.is(DamageTypes.DROWN)
                || source.is(DamageTypes.FLY_INTO_WALL)) {
            return false;
        }
        if (this.attack == Attack.CONSTRICT && this.holding) {
            this.coilDamage += amount;
        }
        boolean hurt = super.hurtServer(level, source, amount * factor);
        if (hurt && source.getEntity() instanceof LivingEntity attacker && attacker != this && this.canStrike(attacker)
                && !(this.getTarget() instanceof Player)) {
            this.setTarget(attacker);
        }
        return hurt;
    }

    /** Wen ihre Angriffe treffen: alle Lebewesen außer ihr, ihrer Haut und Spielern im Kreativ-/Zuschauermodus. */
    public boolean canStrike(LivingEntity entity) {
        return entity != this && entity.isAlive() && !(entity instanceof QuetzaraEntity) && !(entity instanceof ArmorStand)
                && !(entity instanceof Player player && (player.isCreative() || player.isSpectator()))
                && entity.distanceToSqr(this.getX(), entity.getY(), this.getZ()) <= this.confine * this.confine;
    }

    // ------------------------------------------------------------------ Ablauf

    @Override
    public void tick() {
        super.tick();
        // die Haltung folgt weich den Vorgaben (auf beiden Seiten gleich)
        Attack shown = this.currentAttack();
        float t = this.attackTime(0.0f);
        boolean fast = shown == Attack.BITE && t >= BITE_LUNGE - 1 && t <= BITE_LUNGE + 6 || shown == Attack.SPIT && t >= SPIT_RELEASE - 2
                && t <= SPIT_RELEASE + 2;
        this.rearO = this.rear;
        this.rear += (this.entityData.get(DATA_REAR) - this.rear) * (fast ? 0.5f : 0.2f);
        this.ruffO = this.ruff;
        this.ruff += (this.entityData.get(DATA_RUFF) - this.ruff) * 0.25f;
        this.jawO = this.jaw;
        this.jaw += (this.entityData.get(DATA_JAW) - this.jaw) * (fast ? 0.6f : 0.3f);
        if (this.level().isClientSide()) {
            // schläft sie, liegt sie in ihrer Spirale (erst wenn die Lage feststeht)
            if (shown == Attack.SLEEP) {
                if (++this.sleepSeen == 3) {
                    this.seedCoil();
                }
            } else {
                this.sleepSeen = 0;
            }
            if (this.layoutSeen >= 0 && ++this.layoutSeen == 3) {
                this.layoutSeen = -1;
                this.seedLine(this.entityData.get(DATA_BEND));
            }
        } else if (this.attack == Attack.SLEEP && !this.seeded) {
            this.seedCoil();
        }
        if (this.isAlive() || this.deathTime > 0) {
            this.layBody();
        }
        if (this.level().isClientSide()) {
            this.clientParticles();
        }
    }

    private void clientParticles() {
        Level level = this.level();
        if (this.jaw > 0.4f && this.random.nextInt(3) == 0 && !this.isCamouflaged()) {
            Vec3 s = this.snout().add(this.headDir().scale(-0.4)).add(0.0, -0.3, 0.0);
            level.addParticle(new DustParticleOptions(VENOM, 0.7f), s.x, s.y, s.z, 0.0, -0.05, 0.0);
        }
        if (this.ruff > 0.6f && this.random.nextInt(4) == 0) {
            Vec3 j = this.jointNow;
            level.addParticle(new DustParticleOptions(this.random.nextBoolean() ? JADE : GOLD, 1.0f), j.x + (this.random.nextDouble() - 0.5) * 2.0,
                    j.y + (this.random.nextDouble() - 0.5) * 1.6, j.z + (this.random.nextDouble() - 0.5) * 2.0, 0.0, 0.0, 0.0);
        }
        if (this.isCamouflaged() && this.random.nextInt(3) == 0) {
            int i = this.random.nextInt(SEGMENTS + 1);
            Vec3 p = this.bodyNow[i];
            level.addParticle(ParticleTypes.WAX_OFF, p.x + (this.random.nextDouble() - 0.5), p.y + (this.random.nextDouble() - 0.5) * 0.6,
                    p.z + (this.random.nextDouble() - 0.5), 0.0, 0.0, 0.0);
        }
        if (this.currentAttack() == Attack.MOLT && this.attackTime(0.0f) < MOLT_SPLIT && this.random.nextInt(2) == 0) {
            int i = this.random.nextInt(SEGMENTS + 1);
            Vec3 p = this.bodyNow[i];
            level.addParticle(new DustParticleOptions(SKIN, 1.0f), p.x + (this.random.nextDouble() - 0.5), p.y + 0.4,
                    p.z + (this.random.nextDouble() - 0.5), 0.0, 0.02, 0.0);
        }
    }

    private void begin(Attack next) {
        if (this.attack == Attack.CONSTRICT) {
            this.release(this.level() instanceof ServerLevel level ? level : null);
        }
        this.attack = next;
        this.attackTick = 0;
        this.entityData.set(DATA_ATTACK, next.ordinal());
        if (next != Attack.NONE && next != Attack.SLEEP && next != Attack.AWAKEN && next != Attack.STUN && next != Attack.RETURN
                && next != Attack.MOLT) {
            this.lastAttack = next;
        }
        this.struck.clear();
        if (next != Attack.NONE && next != Attack.RETURN) {
            this.getNavigation().stop();
        }
    }

    private void pose(float rear, float ruff, float jaw) {
        this.entityData.set(DATA_REAR, rear);
        this.entityData.set(DATA_RUFF, ruff);
        this.entityData.set(DATA_JAW, jaw);
    }

    /** Beginnt sofort einen bestimmten Angriff (für Tests und die Vorführung; {@code NONE}: sie hält zwei Sekunden inne). */
    public void forceAttack(Attack next) {
        if (next == Attack.NONE) {
            this.cooldown = 40;
        }
        this.begin(next);
        if (next == Attack.SLEEP) {
            this.seedCoil();
            this.pose(0.95f, 0.0f, 0.0f);
        }
    }

    /** Für Tests und die Vorführung: gehäutet (neues Schuppenkleid, wütend), ohne die Häutung selbst. */
    public void setMolted(boolean molted) {
        this.entityData.set(DATA_MOLTED, molted);
    }

    /**
     * Legt sie schlafend um den Altar: der Nacken auf der Seite zum Eingang, die Spirale um den Altar, der Kopf zum Eingang
     * gedreht.
     */
    public void coilAround(BlockPos altarPos, Direction facing) {
        this.altar = altarPos.immutable();
        this.altarFacing = facing;
        Vec3 c = Vec3.atBottomCenterOf(altarPos);
        this.home = c;
        Vec3 f = new Vec3(facing.getStepX(), 0.0, facing.getStepZ());
        // der Nacken liegt COIL_INNER vor dem Altar; die Mitte der Spirale (rechts vom Leib) ist der Altar
        Vec3 anchor = c.add(f.scale(COIL_INNER));
        Vec3 fwd = new Vec3(-f.z, 0.0, f.x);
        float bodyYaw = (float) (Mth.atan2(fwd.z, fwd.x) * Mth.RAD_TO_DEG) - 90.0f;
        Vec3 look = fwd.add(f.scale(1.6)).normalize();
        float headYaw = (float) (Mth.atan2(look.z, look.x) * Mth.RAD_TO_DEG) - 90.0f;
        this.snapTo(anchor.x, anchor.y, anchor.z, bodyYaw, 8.0f);
        this.setYHeadRot(headYaw);
        this.yBodyRot = bodyYaw;
        this.attack = Attack.SLEEP;
        this.entityData.set(DATA_ATTACK, Attack.SLEEP.ordinal());
        this.pose(0.95f, 0.0f, 0.0f);
        this.rear = 0.95f;
        this.rearO = 0.95f;
        this.seedCoil();
    }

    /** Sie erwacht (richtet sich auf, spreizt die Krause, zischt). */
    public void awaken() {
        if (this.attack == Attack.SLEEP || this.attack == Attack.RETURN || this.attack == Attack.NONE) {
            this.begin(Attack.AWAKEN);
        }
    }

    private void wake(ServerLevel level) {
        if (this.altar != null && level.getBlockEntity(this.altar) instanceof SerpentAltarBlockEntity altarEntity) {
            altarEntity.awaken(level);
        } else {
            this.awaken();
        }
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        this.bossBar.setProgress(this.getHealth() / this.getMaxHealth());
        this.bossBar.setVisible(this.attack != Attack.SLEEP);
        if (this.home == null) {
            this.home = this.position();
        }
        if (this.camoTicks > 0 && --this.camoTicks == 0) {
            this.entityData.set(DATA_CAMO, false);
            this.ambush = false;
        }
        if (this.tickCount % 2 == 0 && this.attack != Attack.SLEEP) {
            this.shoveAside(level);
        }
        switch (this.attack) {
            case SLEEP -> {
                this.tickSleep(level);
                return;
            }
            case AWAKEN -> {
                this.tickAwaken(level);
                return;
            }
            case RETURN -> {
                this.tickReturn(level);
                return;
            }
            case MOLT -> {
                this.tickMolt(level);
                return;
            }
            case STUN -> {
                this.tickStun(level);
                return;
            }
            default -> {
            }
        }
        if (this.tickCount % 10 == 0 && this.attack != Attack.CONSTRICT && this.outOfBounds()) {
            this.begin(Attack.RETURN);
            return;
        }
        if (!this.isMolted() && this.attack == Attack.NONE && this.getHealth() < this.getMaxHealth() * 0.5f) {
            this.begin(Attack.MOLT);
            return;
        }
        LivingEntity target = this.findTarget(level);
        this.setTarget(target);
        if (this.attack == Attack.NONE) {
            if (target == null) {
                this.idle(level);
                return;
            }
            this.idleTicks = 0;
            this.approach(target);
            if (--this.cooldown <= 0) {
                this.begin(this.chooseAttack(level, target));
            }
            return;
        }
        this.idleTicks = 0;
        this.attackTick++;
        switch (this.attack) {
            case BITE -> this.tickBite(level, target);
            case SPIT -> this.tickSpit(level, target);
            case FEATHERS -> this.tickFeathers(level, target);
            case WHIP -> this.tickWhip(level);
            case CONSTRICT -> this.tickConstrict(level);
            default -> {
            }
        }
        if (this.attack != Attack.NONE && this.attack != Attack.CONSTRICT && this.attackTick >= this.attack.duration) {
            this.finishAttack();
        }
    }

    private void finishAttack() {
        Attack done = this.attack;
        this.begin(Attack.NONE);
        float pace = this.isMolted() ? 0.65f : 1.0f;
        this.cooldown = Math.round((14 + this.random.nextInt(18)) * pace);
        if (done == Attack.FEATHERS || done == Attack.CONSTRICT) {
            this.cooldown += 10;
        }
        this.pose(1.5f, 0.25f, 0.0f);
    }

    /** Wer in ihrem Leib steht, wird beiseitegeschoben (sie ist kein Nebel). */
    private void shoveAside(ServerLevel level) {
        for (int i = 0; i <= SEGMENTS; i++) {
            Vec3 p = this.bodyNow[i];
            double radius = 0.75 * GIRTH[i];
            for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, new AABB(p, p).inflate(radius + 0.3, radius, radius + 0.3),
                    e -> e != this && !(e instanceof QuetzaraEntity) && !e.isSpectator()
                            && !e.getUUID().equals(this.coilVictim()))) {
                Vec3 away = e.position().subtract(p).multiply(1.0, 0.0, 1.0);
                if (away.lengthSqr() < 1.0e-4) {
                    away = new Vec3(1.0, 0.0, 0.0);
                }
                away = away.normalize().scale(0.12);
                e.push(away.x, 0.0, away.z);
            }
        }
    }

    private boolean outOfBounds() {
        if (this.home == null) {
            return false;
        }
        double dx = this.getX() - this.home.x;
        double dz = this.getZ() - this.home.z;
        return dx * dx + dz * dz > LEASH * LEASH || this.getY() < this.home.y - 8.0 || this.getY() > this.home.y + 14.0;
    }

    private boolean inArena(Entity entity) {
        if (this.home == null) {
            return true;
        }
        double dx = entity.getX() - this.home.x;
        double dz = entity.getZ() - this.home.z;
        return dx * dx + dz * dz <= (ARENA + 3.0) * (ARENA + 3.0) && entity.getY() > this.home.y - 6.0 && entity.getY() < this.home.y + 12.0;
    }

    private @Nullable LivingEntity findTarget(ServerLevel level) {
        LivingEntity current = this.getTarget();
        if (current != null && current.isAlive() && current.distanceToSqr(this) < 40.0 * 40.0 && this.canStrike(current) && this.inArena(current)) {
            return current;
        }
        return level.getNearestPlayer(this.getX(), this.getY(), this.getZ(), Math.min(30.0, this.confine),
                e -> e instanceof Player p && !p.isCreative() && !p.isSpectator() && p.isAlive() && this.inArena(p));
    }

    /** Dreht den Kopf zu einem Punkt (höchstens {@code speed} Grad je Tick). */
    private void lookAt(Vec3 at, float speed) {
        Vec3 from = this.jointNow == Vec3.ZERO ? this.position().add(0.0, this.rear, 0.0) : this.jointNow;
        Vec3 d = at.subtract(from);
        double flat = Math.sqrt(d.x * d.x + d.z * d.z);
        if (flat < 1.0e-4 && Math.abs(d.y) < 1.0e-4) {
            return;
        }
        float yaw = (float) (Mth.atan2(d.z, d.x) * Mth.RAD_TO_DEG) - 90.0f;
        float pitch = Mth.clamp((float) (-(Mth.atan2(d.y, flat) * Mth.RAD_TO_DEG)), -35.0f, 60.0f);
        this.setYHeadRot(Mth.approachDegrees(this.getYHeadRot(), yaw, speed));
        this.setXRot(Mth.approach(this.getXRot(), pitch, speed * 0.6f));
    }

    private Vec3 toward(Vec3 to) {
        Vec3 dir = to.subtract(this.position()).multiply(1.0, 0.0, 1.0);
        return dir.lengthSqr() < 1.0e-4 ? this.bodyDir() : dir.normalize();
    }

    private void faceBody(Vec3 dir) {
        float yaw = (float) (Mth.atan2(dir.z, dir.x) * Mth.RAD_TO_DEG) - 90.0f;
        this.setYRot(yaw);
        this.yBodyRot = yaw;
    }

    private void sound(ServerLevel level, SoundEvent sound, float volume, float pitch) {
        level.playSound(null, this.getX(), this.getY(), this.getZ(), sound, SoundSource.HOSTILE, volume, pitch);
    }

    /** Sie kriecht heran (in Windungen), richtet sich halb auf und behält ihr Ziel im Blick. */
    private void approach(LivingEntity target) {
        this.lookAt(target.getEyePosition(), 14.0f);
        double distance = this.distanceTo(target);
        boolean far = distance > 3.2;
        this.entityData.set(DATA_REAR, far && distance > 7.0 ? 1.1f : 1.7f);
        this.entityData.set(DATA_RUFF, 0.25f);
        if (far && this.inArena(target)) {
            this.getNavigation().moveTo(target, this.isMolted() ? 1.25 : 1.05);
            this.slither();
        } else {
            this.getNavigation().stop();
        }
    }

    /** Statt geradeaus kriecht sie in Windungen: Das Ziel des Schritts pendelt nach links und rechts. */
    private void slither() {
        MoveControl move = this.getMoveControl();
        if (!move.hasWanted()) {
            return;
        }
        Vec3 to = new Vec3(move.getWantedX() - this.getX(), 0.0, move.getWantedZ() - this.getZ());
        if (to.lengthSqr() < 0.04) {
            return;
        }
        double wave = Math.sin(this.tickCount * 0.16) * 0.7;
        Vec3 d = to.normalize().yRot((float) wave);
        move.setWantedPosition(this.getX() + d.x * 2.0, move.getWantedY(), this.getZ() + d.z * 2.0, move.getSpeedModifier());
    }

    /** Ohne Gegner: Sie wartet eine Weile aufgerichtet, dann kriecht sie zum Altar zurück. */
    private void idle(ServerLevel level) {
        this.getNavigation().stop();
        this.pose(1.4f, 0.0f, 0.0f);
        if (++this.idleTicks > RETURN_TICKS && this.altar != null) {
            this.begin(Attack.RETURN);
        }
    }

    private Attack chooseAttack(ServerLevel level, LivingEntity target) {
        double distance = this.snout().distanceTo(target.getBoundingBox().getCenter());
        boolean storm = this.isMolted();
        if (this.ambush && distance < 7.0) {
            return Attack.BITE;
        }
        // wer hinter ihr steht (nah am Schwanz), den fegt der Schwanz fort
        for (int i = WHIP_FROM; i <= SEGMENTS; i++) {
            if (this.bodyNow[i].distanceToSqr(target.position().add(0.0, 0.6, 0.0)) < 3.4 * 3.4 && distance > 3.0
                    && this.lastAttack != Attack.WHIP) {
                return Attack.WHIP;
            }
        }
        List<Attack> options = new ArrayList<>();
        boolean coil = this.tickCount - this.lastConstrict > CONSTRICT_COOLDOWN && !target.isInWater() && target.onGround();
        if (distance < 4.5) {
            options.addAll(List.of(Attack.BITE, Attack.BITE, Attack.BITE));
            if (coil) {
                options.addAll(List.of(Attack.CONSTRICT, Attack.CONSTRICT));
            }
            options.add(Attack.SPIT);
        } else if (distance < 9.0) {
            options.addAll(List.of(Attack.BITE, Attack.BITE, Attack.SPIT, Attack.FEATHERS));
            if (coil && distance < 6.5) {
                options.add(Attack.CONSTRICT);
            }
        } else {
            options.addAll(List.of(Attack.SPIT, Attack.SPIT, Attack.FEATHERS, Attack.FEATHERS));
            if (storm) {
                options.add(Attack.FEATHERS);
            }
        }
        options.removeIf(a -> a == this.lastAttack);
        if (options.isEmpty()) {
            options.add(Attack.BITE);
        }
        return options.get(this.random.nextInt(options.size()));
    }

    // ------------------------------------------------------------------ Schlafen, Erwachen, Heimkehr

    private void tickSleep(ServerLevel level) {
        this.getNavigation().stop();
        this.setDeltaMovement(0.0, this.getDeltaMovement().y, 0.0);
        if (this.getHealth() < this.getMaxHealth() && this.tickCount % 10 == 0) {
            this.heal(4.0f);
        }
        if (this.tickCount % 60 == 0) {
            Vec3 s = this.snout();
            level.sendParticles(ParticleTypes.WHITE_SMOKE, s.x, s.y, s.z, 1, 0.05, 0.05, 0.05, 0.005);
        }
    }

    /** Sie erwacht: Die Augen öffnen sich, sie richtet sich hoch auf, spreizt die Krause und zischt; dann senkt sie den Kopf. */
    private void tickAwaken(ServerLevel level) {
        int t = ++this.attackTick;
        this.setDeltaMovement(0.0, this.getDeltaMovement().y, 0.0);
        Player nearest = level.getNearestPlayer(this, 40.0);
        if (nearest != null && t > 8) {
            this.lookAt(nearest.getEyePosition(), 6.0f);
        }
        if (t == 1) {
            this.pose(1.4f, 0.0f, 0.0f);
            this.sound(level, ModSounds.QUETZARA_AWAKEN.get(), 3.0f, 1.0f);
        }
        if (t == 18) {
            this.pose(4.6f, 0.4f, 0.2f);
        }
        if (t == 38) {
            this.pose(4.6f, 1.0f, 0.9f);
            this.sound(level, ModSounds.QUETZARA_SCREECH.get(), 3.5f, 1.0f);
        }
        if (t == 46) {
            Component title = Component.translatable("boss.kingdomsatwar.quetzara").withColor(JADE);
            Component subtitle = Component.translatable("boss.kingdomsatwar.quetzara.title").withStyle(ChatFormatting.GRAY);
            for (ServerPlayer player : level.players()) {
                if (player.distanceToSqr(this) < 64.0 * 64.0) {
                    player.connection.send(new ClientboundSetTitlesAnimationPacket(15, 60, 25));
                    player.connection.send(new ClientboundSetTitleTextPacket(title));
                    player.connection.send(new ClientboundSetSubtitleTextPacket(subtitle));
                }
            }
        }
        if (t == 70) {
            this.pose(2.2f, 0.5f, 0.0f);
            this.sound(level, ModSounds.QUETZARA_RATTLE.get(), 2.5f, 1.0f);
        }
        if (t >= AWAKEN_TICKS) {
            this.begin(Attack.NONE);
            this.cooldown = 12;
            this.idleTicks = 0;
            this.pose(1.6f, 0.3f, 0.0f);
        }
    }

    /** Niemand mehr da: Sie kriecht zum Altar zurück, rollt sich ein und schläft (und heilt). */
    private void tickReturn(ServerLevel level) {
        this.returnTicks++;
        this.pose(1.0f, 0.0f, 0.0f);
        LivingEntity target = this.findTarget(level);
        if (target != null && !this.outOfBounds()) {
            this.returnTicks = 0;
            this.begin(Attack.NONE);
            this.cooldown = 10;
            return;
        }
        if (this.altar == null || this.home == null) {
            this.returnTicks = 0;
            this.begin(Attack.NONE);
            return;
        }
        Vec3 f = new Vec3(this.altarFacing.getStepX(), 0.0, this.altarFacing.getStepZ());
        Vec3 nest = this.home.add(f.scale(COIL_INNER));
        double d = this.position().distanceToSqr(nest.x, this.getY(), nest.z);
        if (d > 2.5 * 2.5 && this.returnTicks < 600) {
            if (this.returnTicks % 10 == 1) {
                this.getNavigation().moveTo(nest.x, nest.y, nest.z, 0.9);
            }
            this.slither();
            this.lookAt(nest.add(0.0, 1.0, 0.0), 8.0f);
            return;
        }
        // eingerollt: zurück in die Spirale um den Altar
        level.sendParticles(new DustParticleOptions(JADE, 1.2f), this.getX(), this.getY() + 0.6, this.getZ(), 20, 1.5, 0.4, 1.5, 0.0);
        this.returnTicks = 0;
        this.coilAround(this.altar, this.altarFacing);
        this.entityData.set(DATA_CAMO, false);
        this.camoTicks = 0;
        if (level.getBlockEntity(this.altar) instanceof SerpentAltarBlockEntity altarEntity) {
            altarEntity.onSerpentSlept(level, this);
        }
        this.sound(level, ModSounds.QUETZARA_HISS.get(), 1.5f, 0.7f);
    }

    // ------------------------------------------------------------------ Giftbiss

    /** Sie zieht den Kopf zurück und schnellt vor: wer vor der Schnauze ist, den beißt sie (schwer, und das Gift wirkt lange). */
    private void tickBite(ServerLevel level, @Nullable LivingEntity target) {
        int t = this.attackTick;
        if (t == 1) {
            this.pose(2.7f, 0.6f, 0.35f);
            this.sound(level, ModSounds.QUETZARA_HISS.get(), 2.0f, 1.2f);
        }
        if (t < BITE_LUNGE && target != null) {
            this.lookAt(target.getBoundingBox().getCenter(), 25.0f);
            this.faceBody(this.toward(target.position()));
        }
        if (t == BITE_LUNGE) {
            this.pose(0.95f, 0.8f, 1.0f);
            this.sound(level, ModSounds.QUETZARA_STRIKE.get(), 2.0f, 1.0f);
        }
        if (t >= BITE_LUNGE && t <= BITE_LUNGE + 3) {
            Vec3 dir = target != null ? this.toward(target.position()) : this.bodyDir();
            double reach = target == null ? 3.0 : Mth.clamp(Math.sqrt(this.distanceToSqr(target.getX(), this.getY(), target.getZ())) - 1.4, 0.6, 4.2);
            Vec3 step = dir.scale(reach / 4.0);
            this.setDeltaMovement(step.x * 1.9, this.getDeltaMovement().y, step.z * 1.9);
            if (target != null) {
                this.lookAt(target.getBoundingBox().getCenter(), 30.0f);
            }
        }
        if (t >= BITE_LUNGE && t <= BITE_LUNGE + 6) {
            Vec3 s = this.snout();
            boolean bit = false;
            for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, new AABB(s, s).inflate(BITE_REACH + 1.0),
                    this::canStrike)) {
                if (this.struck.contains(victim.getUUID()) || victim.getBoundingBox().inflate(BITE_REACH * 0.5).distanceToSqr(s) > BITE_REACH * BITE_REACH) {
                    continue;
                }
                this.struck.add(victim.getUUID());
                float damage = (this.isMolted() ? BITE_DAMAGE + 2.0f : BITE_DAMAGE) * (this.ambush ? 1.5f : 1.0f);
                if (victim.hurtServer(level, this.damageSources().mobAttack(this), damage)) {
                    SerpentFangItem.poison(victim, this, this.isMolted() ? 2 : 1, 120);
                    Vec3 dir = this.headDir();
                    victim.push(dir.x * 0.5, 0.25, dir.z * 0.5);
                    victim.needsSync = true;
                    bit = true;
                }
            }
            if (bit) {
                this.sound(level, ModSounds.QUETZARA_BITE.get(), 2.0f, 1.0f);
                this.entityData.set(DATA_JAW, 0.05f);
                if (this.ambush) {
                    this.ambush = false;
                    this.camoTicks = 1;
                }
            }
        }
        if (t == BITE_LUNGE + 7) {
            this.pose(1.4f, 0.35f, 0.0f);
        }
    }

    // ------------------------------------------------------------------ Giftspucke

    /** Sie lehnt sich zurück und spuckt einen Klumpen Gift im Bogen (gehäutet drei); er zerplatzt zu einer Giftwolke. */
    private void tickSpit(ServerLevel level, @Nullable LivingEntity target) {
        int t = this.attackTick;
        if (t == 1) {
            this.pose(3.1f, 0.7f, 0.5f);
            this.sound(level, ModSounds.QUETZARA_RATTLE.get(), 2.0f, 1.2f);
        }
        if (target != null && t <= SPIT_RELEASE) {
            this.lookAt(target.getEyePosition(), 18.0f);
            this.faceBody(this.toward(target.position()));
        }
        if (t == SPIT_RELEASE) {
            this.pose(2.4f, 0.7f, 1.0f);
            this.sound(level, ModSounds.QUETZARA_SPIT.get(), 2.2f, 1.0f);
            Vec3 s = this.snout();
            Vec3 aim = target != null ? target.position().add(target.getDeltaMovement().scale(8.0)) : s.add(this.headDir().scale(8.0));
            int count = this.isMolted() ? 3 : 1;
            for (int k = 0; k < count; k++) {
                VenomGlobEntity glob = new VenomGlobEntity(level, this);
                glob.setPos(s.x, s.y, s.z);
                Vec3 to = aim.subtract(s);
                Vec3 side = new Vec3(-to.z, 0.0, to.x).normalize().scale((k - (count - 1) / 2.0) * 1.6);
                to = to.add(side);
                double flat = Math.sqrt(to.x * to.x + to.z * to.z);
                glob.shoot(to.x, to.y + flat * 0.22, to.z, (float) Mth.clamp(0.55 + flat * 0.06, 0.7, 1.6), 1.5f);
                level.addFreshEntity(glob);
            }
        }
        if (t == SPIT_RELEASE + 6) {
            this.pose(1.6f, 0.35f, 0.0f);
        }
    }

    // ------------------------------------------------------------------ Federsturm

    /** Hoch aufgerichtet spreizt sie die Krause und schleudert Fächer aus vergifteten Federn (gehäutet mehr und öfter). */
    private void tickFeathers(ServerLevel level, @Nullable LivingEntity target) {
        int t = this.attackTick;
        if (t == 1) {
            this.pose(4.3f, 1.0f, 0.6f);
            this.sound(level, ModSounds.QUETZARA_RATTLE.get(), 2.5f, 0.9f);
        }
        if (target != null) {
            this.lookAt(target.getEyePosition(), 10.0f);
            if (t < FEATHER_VOLLEYS[0]) {
                this.faceBody(this.toward(target.position()));
            }
        }
        int volleys = this.isMolted() ? 4 : 3;
        for (int v = 0; v < volleys; v++) {
            if (t == FEATHER_VOLLEYS[v]) {
                this.featherVolley(level, target, this.isMolted() ? 7 : 5, v);
            }
        }
        if (t == FEATHER_VOLLEYS[volleys - 1] + 6) {
            this.pose(1.6f, 0.3f, 0.0f);
        }
    }

    private void featherVolley(ServerLevel level, @Nullable LivingEntity target, int count, int volley) {
        this.sound(level, ModSounds.QUETZARA_FEATHERS.get(), 2.4f, 0.9f + volley * 0.08f);
        Vec3 from = this.jointNow.add(this.headDir().scale(-0.3));
        Vec3 aim = target != null ? target.getBoundingBox().getCenter() : from.add(this.headDir().scale(10.0));
        Vec3 to = aim.subtract(from);
        double flat = Math.sqrt(to.x * to.x + to.z * to.z);
        float spread = 64.0f;
        for (int k = 0; k < count; k++) {
            float yaw = (k - (count - 1) / 2.0f) * spread / (count - 1) + (volley % 2 == 0 ? 0.0f : spread / (count - 1) * 0.5f);
            Vec3 dir = to.yRot(yaw * Mth.DEG_TO_RAD).normalize();
            QuetzaraFeatherEntity feather = new QuetzaraFeatherEntity(level, this, from.add(dir.scale(0.8)));
            feather.shoot(dir.x, dir.y + flat * 0.012, dir.z, 1.35f, 2.0f);
            level.addFreshEntity(feather);
        }
        level.sendParticles(new DustParticleOptions(JADE, 1.3f), from.x, from.y, from.z, 14, 0.8, 0.6, 0.8, 0.0);
    }

    // ------------------------------------------------------------------ Schwanzpeitsche

    /** Wer hinter ihr steht, den fegt der Schwanz davon: ausholen, dann ein Hieb im weiten Bogen. */
    private void tickWhip(ServerLevel level) {
        int t = this.attackTick;
        if (t == 1) {
            this.pose(1.8f, 0.5f, 0.3f);
            // nach welcher Seite? zum Ziel hin
            LivingEntity target = this.getTarget();
            int side = 1;
            if (target != null) {
                Vec3 a = this.bodyNow[WHIP_FROM - 1];
                Vec3 b = this.bodyNow[SEGMENTS];
                Vec3 axis = b.subtract(a);
                Vec3 rel = target.position().subtract(a);
                side = axis.x * rel.z - axis.z * rel.x > 0.0 ? 1 : -1;
            }
            this.entityData.set(DATA_SIDE, side);
            this.sound(level, ModSounds.QUETZARA_RATTLE.get(), 2.2f, 1.3f);
        }
        if (t == WHIP_STRIKE) {
            this.sound(level, ModSounds.QUETZARA_WHIP.get(), 2.5f, 1.0f);
        }
        if (t >= WHIP_STRIKE && t <= WHIP_STRIKE + 8) {
            Vec3 pivot = this.bodyNow[WHIP_FROM - 1];
            for (int i = WHIP_FROM + 1; i <= SEGMENTS; i++) {
                Vec3 p = this.bodyNow[i];
                double reach = 0.75 * GIRTH[i] + 0.8;
                for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, new AABB(p, p).inflate(reach + 1.0), this::canStrike)) {
                    if (this.struck.contains(victim.getUUID()) || victim.getBoundingBox().distanceToSqr(p) > reach * reach) {
                        continue;
                    }
                    this.struck.add(victim.getUUID());
                    if (victim.hurtServer(level, this.damageSources().mobAttack(this), this.isMolted() ? WHIP_DAMAGE + 2.0f : WHIP_DAMAGE)) {
                        Vec3 out = victim.position().subtract(pivot).multiply(1.0, 0.0, 1.0);
                        out = out.lengthSqr() < 1.0e-4 ? this.bodyDir() : out.normalize();
                        victim.push(out.x * 1.7, 0.55, out.z * 1.7);
                        victim.needsSync = true;
                    }
                }
            }
        }
        if (t == 30) {
            this.pose(1.5f, 0.3f, 0.0f);
        }
    }

    // ------------------------------------------------------------------ Würgegriff

    /**
     * Der Würgegriff: Sie umkreist ihr Opfer (es wird dabei schwerfällig), schließt den Ring und drückt zu – alle Sekunde ein
     * Würgen, nach fünf Sekunden ein Biss. Wer ihr in der Umschlingung {@link #ESCAPE_DAMAGE} Schaden zufügt, kommt frei: Sie
     * ist dann kurz benommen.
     */
    private void tickConstrict(ServerLevel level) {
        int t = this.attackTick;
        LivingEntity victim = this.coilVictim != null && level.getEntity(this.coilVictim) instanceof LivingEntity e && e.isAlive() ? e : null;
        if (t == 1) {
            LivingEntity target = this.getTarget();
            if (target == null || !this.canStrike(target)) {
                this.finishAttack();
                return;
            }
            this.coilVictim = target.getUUID();
            this.coilCenter = target.position();
            Vec3 rel = this.position().subtract(this.coilCenter);
            this.coilAngle = Math.atan2(rel.z, rel.x);
            // im Kreis so herum, wie der Leib schon liegt
            Vec3 fwd = this.bodyDir();
            this.coilSign = fwd.x * -Math.sin(this.coilAngle) + fwd.z * Math.cos(this.coilAngle) >= 0.0 ? 1 : -1;
            this.coilTurned = 0.0;
            this.coilDamage = 0.0f;
            this.holding = false;
            this.holdTick = 0;
            this.lastConstrict = this.tickCount;
            this.pose(0.7f, 0.2f, 0.3f);
            this.sound(level, ModSounds.QUETZARA_HISS.get(), 2.2f, 0.9f);
            return;
        }
        if (victim == null || !this.canStrike(victim)) {
            this.release(level);
            this.finishAttack();
            return;
        }
        if (!this.holding) {
            // der Kreis um das Opfer (die Mitte folgt ihm ein wenig)
            this.coilCenter = this.coilCenter.lerp(victim.position(), 0.2);
            if (victim.position().distanceToSqr(this.coilCenter) > (COIL_RADIUS + 1.6) * (COIL_RADIUS + 1.6) || t > 60) {
                // entkommen, bevor der Ring geschlossen war
                this.release(level);
                this.sound(level, ModSounds.QUETZARA_HISS.get(), 1.8f, 0.7f);
                this.finishAttack();
                return;
            }
            victim.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 12, 3, false, false), this);
            double step = 0.34;
            this.coilAngle += step * this.coilSign;
            this.coilTurned += step;
            Vec3 want = this.coilCenter.add(Math.cos(this.coilAngle) * COIL_RADIUS, 0.0, Math.sin(this.coilAngle) * COIL_RADIUS);
            Vec3 delta = new Vec3(want.x - this.getX(), 0.0, want.z - this.getZ());
            if (delta.length() > 1.1) {
                delta = delta.normalize().scale(1.1);
            }
            this.move(MoverType.SELF, delta);
            Vec3 tangent = new Vec3(-Math.sin(this.coilAngle) * this.coilSign, 0.0, Math.cos(this.coilAngle) * this.coilSign);
            this.faceBody(tangent);
            this.lookAt(victim.getEyePosition(), 30.0f);
            if (this.coilTurned >= Math.PI * 2.0 * 1.1) {
                this.holding = true;
                this.holdTick = 0;
                this.pin(victim, true);
                this.pose(2.5f, 0.8f, 0.4f);
                this.sound(level, ModSounds.QUETZARA_SQUEEZE.get(), 2.4f, 0.8f);
            }
            return;
        }
        // der Griff: festhalten, drücken
        this.holdTick++;
        this.setDeltaMovement(0.0, this.getDeltaMovement().y, 0.0);
        this.lookAt(victim.getEyePosition(), 20.0f);
        Vec3 c = this.coilCenter;
        if (victim.position().distanceToSqr(c.x, victim.getY(), c.z) > 0.16) {
            victim.teleportTo(c.x, victim.getY(), c.z);
        }
        victim.setDeltaMovement(0.0, Math.min(0.0, victim.getDeltaMovement().y), 0.0);
        if (this.holdTick % 20 == 0 && this.holdTick < HOLD_TICKS) {
            if (victim.hurtServer(level, this.damageSources().mobAttack(this), SQUEEZE_DAMAGE)) {
                this.sound(level, ModSounds.QUETZARA_SQUEEZE.get(), 2.0f, 0.9f + this.random.nextFloat() * 0.2f);
                level.sendParticles(new DustParticleOptions(JADE, 1.0f), c.x, victim.getY(0.5), c.z, 10, 0.6, 0.4, 0.6, 0.0);
            }
        }
        if (this.coilDamage >= ESCAPE_DAMAGE) {
            // genug: Sie lässt los und ist benommen
            this.release(level);
            this.sound(level, ModSounds.QUETZARA_HURT.get(), 2.6f, 0.7f);
            this.begin(Attack.STUN);
            return;
        }
        if (this.holdTick == HOLD_TICKS - 10) {
            this.pose(1.4f, 1.0f, 1.0f);
        }
        if (this.holdTick >= HOLD_TICKS) {
            // zum Schluss der Biss
            if (victim.hurtServer(level, this.damageSources().mobAttack(this), CRUSH_DAMAGE)) {
                SerpentFangItem.poison(victim, this, this.isMolted() ? 2 : 1, 120);
            }
            this.sound(level, ModSounds.QUETZARA_BITE.get(), 2.2f, 0.9f);
            this.release(level);
            Vec3 out = victim.position().subtract(this.position()).multiply(1.0, 0.0, 1.0);
            out = out.lengthSqr() < 1.0e-4 ? this.bodyDir() : out.normalize();
            victim.push(out.x * 1.2, 0.6, out.z * 1.2);
            victim.needsSync = true;
            this.finishAttack();
        }
    }

    /** Hält das Opfer fest (kein Schritt, kein Sprung) oder lässt es los. */
    private static void pin(LivingEntity victim, boolean pinned) {
        AttributeInstance speed = victim.getAttribute(Attributes.MOVEMENT_SPEED);
        AttributeInstance jump = victim.getAttribute(Attributes.JUMP_STRENGTH);
        if (pinned) {
            if (speed != null && !speed.hasModifier(COIL_SLOW)) {
                speed.addTransientModifier(new AttributeModifier(COIL_SLOW, -1.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            }
            if (jump != null && !jump.hasModifier(COIL_JUMP)) {
                jump.addTransientModifier(new AttributeModifier(COIL_JUMP, -1.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            }
        } else {
            if (speed != null) {
                speed.removeModifier(COIL_SLOW);
            }
            if (jump != null) {
                jump.removeModifier(COIL_JUMP);
            }
        }
    }

    /** Lässt ein Opfer los (wenn sie eines hält). */
    private void release(@Nullable ServerLevel level) {
        if (this.coilVictim != null && level != null && level.getEntity(this.coilVictim) instanceof LivingEntity victim) {
            pin(victim, false);
        }
        this.holding = false;
        this.coilVictim = null;
    }

    /** Benommen (nach dem Befreiungsschlag): Der Kopf liegt am Boden – jetzt trifft man ihn hart. */
    private void tickStun(ServerLevel level) {
        int t = ++this.attackTick;
        this.getNavigation().stop();
        if (t == 1) {
            this.pose(0.35f, 0.0f, 0.25f);
            this.setXRot(15.0f);
        }
        if (t % 6 == 0) {
            Vec3 s = this.snout();
            level.sendParticles(ParticleTypes.CRIT, s.x, s.y + 0.6, s.z, 3, 0.3, 0.2, 0.3, 0.0);
        }
        if (t >= Attack.STUN.duration) {
            this.finishAttack();
            this.cooldown = 6;
        }
    }

    // ------------------------------------------------------------------ Häutung

    /**
     * Unter halber Kraft: Sie zittert, ihre Haut reißt am Kopf auf, und sie gleitet aus ihr heraus – die leere Haut bleibt
     * liegen (zischend: eine Täuschung), sie selbst ist eine Weile getarnt und lauert. Danach leuchtet ihr neues Schuppenkleid.
     */
    private void tickMolt(ServerLevel level) {
        int t = ++this.attackTick;
        if (t == 1) {
            this.getNavigation().stop();
            this.pose(1.2f, 0.0f, 0.6f);
            this.sound(level, ModSounds.QUETZARA_MOLT.get(), 3.0f, 1.0f);
            Component text = Component.translatable("boss.kingdomsatwar.quetzara.molt").withColor(JADE);
            for (ServerPlayer player : level.players()) {
                if (player.distanceToSqr(this) < 48.0 * 48.0) {
                    player.sendOverlayMessage(text);
                }
            }
        }
        if (t < MOLT_SPLIT) {
            this.setDeltaMovement(0.0, this.getDeltaMovement().y, 0.0);
            if (t % 3 == 0) {
                int i = this.random.nextInt(SEGMENTS + 1);
                Vec3 p = this.bodyNow[i];
                level.sendParticles(new DustParticleOptions(SKIN, 1.2f), p.x, p.y + 0.4, p.z, 4, 0.4, 0.3, 0.4, 0.0);
            }
            return;
        }
        if (t == MOLT_SPLIT) {
            // die Haut bleibt liegen, wo sie lag
            ShedSkinEntity skin = new ShedSkinEntity(level, this);
            level.addFreshEntity(skin);
            this.skins.add(skin.getUUID());
            this.entityData.set(DATA_MOLTED, true);
            this.entityData.set(DATA_CAMO, true);
            this.camoTicks = CAMO_TICKS;
            this.ambush = true;
            this.sound(level, ModSounds.QUETZARA_MOLT.get(), 2.5f, 1.3f);
            AttributeInstance speed = this.getAttribute(Attributes.MOVEMENT_SPEED);
            if (speed != null) {
                speed.setBaseValue(0.33);
            }
            // fort von ihrem Ziel, auf die andere Seite der Grube
            Vec3 c = this.home != null ? this.home : this.position();
            LivingEntity target = this.getTarget();
            Vec3 away = target != null ? c.subtract(target.position()).multiply(1.0, 0.0, 1.0) : this.bodyDir();
            away = away.lengthSqr() < 1.0e-3 ? this.bodyDir() : away.normalize();
            this.moltGoal = c.add(away.scale(8.0));
            this.pose(1.0f, 0.0f, 0.0f);
        }
        this.getNavigation().moveTo(this.moltGoal.x, this.moltGoal.y, this.moltGoal.z, 1.5);
        this.slither();
        if (t >= Attack.MOLT.duration) {
            this.finishAttack();
            this.cooldown = 20;
        }
    }

    // ------------------------------------------------------------------ Tod, Speichern, Klänge

    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (this.level() instanceof ServerLevel level) {
            this.release(level);
            for (UUID id : this.skins) {
                if (level.getEntity(id) instanceof ShedSkinEntity skin) {
                    skin.discard();
                }
            }
            this.skins.clear();
            if (this.altar != null && level.getBlockEntity(this.altar) instanceof SerpentAltarBlockEntity altarEntity) {
                altarEntity.onSerpentDefeated(level);
            }
        }
        this.entityData.set(DATA_CAMO, false);
        this.pose(3.0f, 1.0f, 1.0f);
    }

    /** Sie bäumt sich auf, schlägt um sich und sinkt nieder – dann zerfällt sie von der Schwanzspitze an zu Staub. */
    @Override
    protected void tickDeath() {
        this.deathTime++;
        if (this.level() instanceof ServerLevel level) {
            if (this.deathTime < 30) {
                this.setYHeadRot(this.getYHeadRot() + Mth.sin(this.deathTime * 0.9f) * 18.0f);
            }
            if (this.deathTime == 30) {
                this.pose(LIE - 0.25f, 0.0f, 0.7f);
                this.sound(level, ModSounds.QUETZARA_SLITHER.get(), 2.0f, 0.6f);
            }
            if (this.deathTime > 40 && this.deathTime % 3 == 0) {
                int i = SEGMENTS - (this.deathTime - 40) / 3;
                if (i >= 0) {
                    Vec3 p = this.bodyNow[i];
                    level.sendParticles(new DustParticleOptions(this.random.nextBoolean() ? JADE : GOLD, 1.4f), p.x, p.y, p.z, 12, 0.4, 0.3, 0.4, 0.0);
                }
            }
            if (this.deathTime >= DEATH_TICKS && !this.isRemoved()) {
                Vec3 j = this.jointNow;
                level.sendParticles(new DustParticleOptions(GOLD, 1.6f), j.x, j.y, j.z, 30, 0.6, 0.5, 0.6, 0.0);
                this.bossBar.removeAllPlayers();
                this.remove(RemovalReason.KILLED);
            }
        }
    }

    @Override
    public void remove(RemovalReason reason) {
        if (this.level() instanceof ServerLevel level) {
            this.release(level);
        }
        super.remove(reason);
        this.bossBar.removeAllPlayers();
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        if (this.altar != null) {
            output.putLong("Altar", this.altar.asLong());
        }
        output.putString("Facing", this.altarFacing.getSerializedName());
        if (this.home != null) {
            output.putBoolean("HasHome", true);
            output.putDouble("HomeX", this.home.x);
            output.putDouble("HomeY", this.home.y);
            output.putDouble("HomeZ", this.home.z);
        }
        output.putBoolean("Asleep", this.attack == Attack.SLEEP);
        output.putBoolean("Molted", this.isMolted());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        input.getLong("Altar").ifPresent(a -> this.altar = BlockPos.of(a));
        Direction facing = Direction.byName(input.getStringOr("Facing", "north"));
        this.altarFacing = facing != null && facing.getAxis().isHorizontal() ? facing : Direction.NORTH;
        if (input.getBooleanOr("HasHome", false)) {
            this.home = new Vec3(input.getDoubleOr("HomeX", 0.0), input.getDoubleOr("HomeY", 0.0), input.getDoubleOr("HomeZ", 0.0));
        }
        if (input.getBooleanOr("Asleep", false)) {
            this.attack = Attack.SLEEP;
            this.entityData.set(DATA_ATTACK, Attack.SLEEP.ordinal());
            this.pose(0.95f, 0.0f, 0.0f);
            this.seeded = false;
        }
        this.setMolted(input.getBooleanOr("Molted", false));
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return this.attack == Attack.SLEEP ? null : ModSounds.QUETZARA_AMBIENT.get();
    }

    @Override
    public int getAmbientSoundInterval() {
        return 140;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.QUETZARA_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.QUETZARA_DEATH.get();
    }

    @Override
    protected float getSoundVolume() {
        return 2.0f;
    }

    // ------------------------------------------------------------------ Himmelswacht

    @Override
    public SkyTheme skyTheme() {
        return SkyTheme.SERPENT;
    }

    /** Der Kopf wird von Hand geführt (siehe {@link #lookAt}): keine Vorgaben der Blicksteuerung. */
    private static final class HeadControl extends LookControl {
        HeadControl(QuetzaraEntity mob) {
            super(mob);
        }

        @Override
        public void tick() {
        }
    }
}
