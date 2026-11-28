package io.github.sirschrolli.kingdomsatwar.entity.combat;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.registry.ModAttachments;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Der Kampf eines Monsters: wählt seine besonderen Angriffe (nach Kampfstil, Abstand und Wartezeiten), führt sie durch
 * Vorwarnung, Ausführung und Erholung und kümmert sich um das, was sein Temperament ausmacht (Raserei, Ausweichen,
 * Kampfschrei). Lebt nur auf dem Server und nur, solange das Monster geladen ist.
 */
public final class Fight {
    static final Identifier ROOT = KingdomsAtWar.id("combat_root");
    static final Identifier STRIKE = KingdomsAtWar.id("combat_strike");
    static final Identifier FRENZY_SPEED = KingdomsAtWar.id("combat_frenzy_speed");
    static final Identifier FRENZY_DAMAGE = KingdomsAtWar.id("combat_frenzy_damage");
    /** Ab so viel Gesundheit (Anteil) gerät ein Rasender in Raserei. */
    public static final float FRENZY_AT = 0.5f;
    /** So viel schneller und stärker ist er dann (Anteil). */
    public static final double FRENZY_SPEED_BONUS = 0.3;
    public static final double FRENZY_DAMAGE_BONUS = 0.35;
    /** Erst so lange (Ticks) nach dem Erblicken des Ziels kommt der erste besondere Angriff. */
    static final int FIRST_DELAY = 24;

    enum Phase {
        IDLE, WAIT, WINDUP, ACT, RECOVER
    }

    public final Mob mob;
    public final Family family;
    public final boolean ranged;
    private Style style;

    Phase phase = Phase.IDLE;
    @Nullable MoveKind kind;
    int t;
    int windup;
    int recover;
    boolean locked;
    int global;
    private final Map<MoveKind, Integer> cooldowns = new EnumMap<>(MoveKind.class);
    private @Nullable LivingEntity lastTarget;
    int engaged;
    boolean frenzied;
    int dodgeCooldown;
    /** Ein erzwungener Angriff (Tests, Vorführungen) ohne Rücksicht auf Abstand und Wartezeit. */
    boolean forced;

    // ---- Arbeitsplatz der Angriffe (die Angriffe selbst haben keinen Zustand)
    public @Nullable LivingEntity target;
    public Vec3 a = Vec3.ZERO;
    public Vec3 b = Vec3.ZERO;
    public double d;
    public int n;
    public float yaw;
    public final IntSet hit = new IntOpenHashSet();
    public @Nullable Entity held;
    /** Hat die Deckung einen Treffer abgefangen (dann schlägt er zurück)? */
    public boolean parried;
    /** So viel Schaden hat das Monster seit Beginn des Angriffs genommen. */
    public float taken;

    Fight(Mob mob, Family family, Style style) {
        this.mob = mob;
        this.family = family;
        this.style = style;
        this.ranged = Family.ranged(mob, family);
        this.global = FIRST_DELAY;
    }

    public Style style() {
        return this.style;
    }

    void setStyle(Style style) {
        this.style = style;
    }

    public ServerLevel level() {
        return (ServerLevel) this.mob.level();
    }

    public RandomSource random() {
        return this.mob.getRandom();
    }

    public @Nullable MoveKind current() {
        return this.phase == Phase.IDLE ? null : this.kind;
    }

    /** Läuft gerade die Ausführung (nicht die Vorwarnung)? */
    public boolean acting() {
        return this.phase == Phase.ACT;
    }

    public boolean idle() {
        return this.phase == Phase.IDLE;
    }

    /** Übernimmt dieser Angriff Bewegung und Blick? (Bei Fliegern und Zauberern läuft alles nebenher.) */
    boolean controlled() {
        return this.kind != null && this.kind.control() && !this.family.overlay();
    }

    /** Braucht der Kampf gerade die Bewegung (für das Ziel, das sie sperrt)? */
    boolean wantsLock() {
        return this.phase != Phase.IDLE && this.controlled();
    }

    // =====================================================================================
    // Ablauf
    // =====================================================================================

    void tick() {
        ServerLevel level = this.level();
        if (!this.mob.isAlive() || this.mob.isNoAi()) {
            if (this.phase != Phase.IDLE) {
                this.stop(true);
            }
            return;
        }
        LivingEntity target = valid(this.mob.getTarget());
        if (target != this.lastTarget) {
            this.lastTarget = target;
            this.engaged = 0;
        }
        if (target != null) {
            this.engaged++;
        }
        this.cooldowns.replaceAll((k, v) -> v > 0 ? v - 1 : 0);
        if (this.global > 0) {
            this.global--;
        }
        if (this.dodgeCooldown > 0) {
            this.dodgeCooldown--;
        }
        this.temper(level, target);
        switch (this.phase) {
            case IDLE -> {
                if (target != null && this.engaged >= FIRST_DELAY && this.global <= 0 && !this.mob.isPassenger()) {
                    this.choose(target);
                }
            }
            case WAIT -> {
                if (this.locked) {
                    this.begin();
                } else if (++this.t > 16 || this.target == null || !this.target.isAlive()) {
                    this.stop(true);
                }
            }
            case WINDUP -> {
                if (this.target == null || !this.target.isAlive() || this.target.isRemoved() || this.controlled() && !this.locked) {
                    this.stop(true);
                    return;
                }
                Move move = this.kind.impl();
                this.face(move.tracks(this, this.t));
                move.warn(this, this.t);
                if (++this.t >= this.windup) {
                    this.phase = Phase.ACT;
                    this.t = 0;
                }
            }
            case ACT -> {
                if (this.controlled() && !this.locked) {
                    this.stop(true);
                    return;
                }
                boolean go = this.kind.impl().act(this, this.t++);
                if (!go || this.t > 200) {
                    this.phase = Phase.RECOVER;
                    this.t = 0;
                    this.recover = this.kind.impl().recover(this);
                }
            }
            case RECOVER -> {
                if (++this.t >= this.recover) {
                    this.stop(false);
                }
            }
        }
    }

    private void choose(LivingEntity target) {
        double dist = this.mob.distanceTo(target);
        List<MoveKind> options = new ArrayList<>();
        List<Float> weights = new ArrayList<>();
        float total = 0.0f;
        this.target = target;
        for (MoveKind k : this.style.moves()) {
            if (this.cooldowns.getOrDefault(k, 0) > 0) {
                continue;
            }
            Move move = k.impl();
            if (!move.ready(this, dist)) {
                continue;
            }
            float w = move.weight(this, dist);
            if (w <= 0.0f) {
                continue;
            }
            options.add(k);
            weights.add(w);
            total += w;
        }
        if (options.isEmpty()) {
            this.target = null;
            this.global = 6;
            return;
        }
        float r = this.random().nextFloat() * total;
        MoveKind pick = options.getLast();
        for (int i = 0; i < options.size(); i++) {
            r -= weights.get(i);
            if (r < 0.0f) {
                pick = options.get(i);
                break;
            }
        }
        this.start(pick, target);
    }

    /** Beginnt einen Angriff (auch erzwungen). */
    void start(MoveKind pick, LivingEntity target) {
        this.kind = pick;
        this.target = target;
        this.t = 0;
        this.hit.clear();
        this.held = null;
        this.parried = false;
        this.taken = 0.0f;
        this.n = 0;
        this.d = 0.0;
        this.a = Vec3.ZERO;
        this.b = Vec3.ZERO;
        if (this.controlled() && !this.locked) {
            this.phase = Phase.WAIT;
        } else {
            this.begin();
        }
    }

    private void begin() {
        Move move = this.kind.impl();
        this.phase = Phase.WINDUP;
        this.t = 0;
        this.taken = 0.0f;
        this.windup = Math.max(1, move.windup(this));
        this.yaw = this.target != null ? yawTo(this.mob.position(), this.target.position()) : this.mob.getYRot();
        if (this.controlled()) {
            this.mob.getNavigation().stop();
            if (this.kind.root()) {
                this.root(true);
            }
        }
        move.begin(this);
        this.mob.setData(ModAttachments.COMBAT_ACT.get(), new Act(this.kind.ordinal(), this.windup, this.level().getGameTime()));
    }

    /** Beendet den laufenden Angriff (abgebrochen: kürzere Wartezeit). */
    void stop(boolean aborted) {
        MoveKind done = this.kind;
        if (done != null && this.phase != Phase.IDLE && this.phase != Phase.WAIT) {
            done.impl().end(this);
        }
        this.root(false);
        this.phase = Phase.IDLE;
        this.kind = null;
        this.t = 0;
        this.held = null;
        this.target = null;
        this.forced = false;
        if (this.mob.hasData(ModAttachments.COMBAT_ACT.get())) {
            this.mob.setData(ModAttachments.COMBAT_ACT.get(), Act.NONE);
        }
        if (done != null) {
            int cooldown = Math.round(done.impl().cooldown(this.random()) * this.tempo());
            this.cooldowns.put(done, aborted ? cooldown / 4 : cooldown);
            this.global = Math.round((aborted ? 20 : 46 + this.random().nextInt(44)) * this.tempo());
        }
    }

    /** Faktor auf alle Wartezeiten: eigenes Tempo, Temperament, Raserei. */
    public float tempo() {
        return this.style.tempo() * this.style.temper().tempo() * (this.frenzied ? 0.6f : 1.0f);
    }

    /** Der Kampf endet (das Monster wird entladen oder stirbt). */
    void dispose() {
        if (this.phase != Phase.IDLE) {
            this.stop(true);
        }
    }

    // =====================================================================================
    // Temperament
    // =====================================================================================

    private void temper(ServerLevel level, @Nullable LivingEntity target) {
        Temper temper = this.style.temper();
        if (temper == Temper.RASEND) {
            if (!this.frenzied && this.mob.getHealth() < this.mob.getMaxHealth() * FRENZY_AT) {
                this.frenzy(true);
            } else if (this.frenzied && this.mob.tickCount % 8 == 0) {
                // die Raserei sieht man: rote Funken, dampfender Atem
                Vec3 p = this.mob.position();
                level.sendParticles(new DustParticleOptions(0xD01010, 1.1f), p.x, p.y + this.mob.getBbHeight() * 0.7, p.z, 2,
                        this.mob.getBbWidth() * 0.4, this.mob.getBbHeight() * 0.25, this.mob.getBbWidth() * 0.4, 0.0);
            }
        }
        if (temper == Temper.LAUERND && target != null && this.phase == Phase.IDLE && this.dodgeCooldown <= 0 && this.mob.onGround()
                && this.mob.tickCount % 3 == 0) {
            this.dodgeProjectiles(level);
        }
    }

    /** Raserei an oder aus (an: einmal ein Wutausbruch, dann schneller, stärker, angriffslustiger). */
    void frenzy(boolean on) {
        this.frenzied = on;
        modifier(this.mob, Attributes.MOVEMENT_SPEED, FRENZY_SPEED, on && !Family.ownPace(this.mob) ? FRENZY_SPEED_BONUS : 0.0);
        modifier(this.mob, Attributes.ATTACK_DAMAGE, FRENZY_DAMAGE, on ? FRENZY_DAMAGE_BONUS : 0.0);
        if (on) {
            ServerLevel level = this.level();
            Vec3 p = this.mob.position();
            level.sendParticles(ParticleTypes.ANGRY_VILLAGER, p.x, p.y + this.mob.getBbHeight() + 0.3, p.z, 3, 0.3, 0.15, 0.3, 0.0);
            level.sendParticles(new DustParticleOptions(0xE01818, 1.6f), p.x, p.y + this.mob.getBbHeight() * 0.5, p.z, 26,
                    this.mob.getBbWidth() * 0.6, this.mob.getBbHeight() * 0.4, this.mob.getBbWidth() * 0.6, 0.0);
            this.mob.playAmbientSound();
            this.sound(SoundEvents.RAVAGER_ROAR, 0.6f, 1.6f + this.random().nextFloat() * 0.2f);
            this.global = Math.min(this.global, 10);
        }
    }

    /** Ein Lauernder sieht ein Geschoss kommen und springt zur Seite. */
    private void dodgeProjectiles(ServerLevel level) {
        Vec3 me = this.mob.position().add(0.0, this.mob.getBbHeight() * 0.5, 0.0);
        for (Projectile shot : level.getEntitiesOfClass(Projectile.class, this.mob.getBoundingBox().inflate(7.0))) {
            Entity owner = shot.getOwner();
            if (owner == this.mob || owner instanceof Enemy || shot.onGround()) {
                continue;
            }
            Vec3 v = shot.getDeltaMovement();
            double speed = v.length();
            if (speed < 0.4) {
                continue;
            }
            Vec3 rel = me.subtract(shot.position());
            double along = rel.dot(v) / speed;
            if (along <= 0.0 || along > 7.0) {
                continue;
            }
            Vec3 closest = shot.position().add(v.scale(along / speed));
            if (closest.distanceTo(me) > 1.3) {
                continue;
            }
            this.dodgeCooldown = 50;
            if (this.random().nextFloat() > 0.65f) {
                return;
            }
            Vec3 side = new Vec3(-v.z, 0.0, v.x).normalize();
            if (this.random().nextBoolean()) {
                side = side.scale(-1.0);
            }
            this.mob.setDeltaMovement(side.x * 0.75, 0.32, side.z * 0.75);
            this.mob.needsSync = true;
            level.sendParticles(ParticleTypes.CLOUD, this.mob.getX(), this.mob.getY() + 0.1, this.mob.getZ(), 6, 0.25, 0.05, 0.25, 0.02);
            this.sound(SoundEvents.BREEZE_JUMP, 0.5f, 1.4f);
            return;
        }
    }

    /** Nach einem eigenen Treffer: Ein Lauernder springt oft gleich wieder zurück (zuschlagen und weg). */
    void afterHit(LivingEntity victim) {
        if (this.style.temper() != Temper.LAUERND || this.phase != Phase.IDLE || !this.mob.onGround() || !this.family.melee()
                || this.family.brain() || this.random().nextFloat() > 0.4f) {
            return;
        }
        Vec3 away = this.mob.position().subtract(victim.position()).multiply(1.0, 0.0, 1.0);
        if (away.lengthSqr() < 1.0e-4) {
            return;
        }
        away = away.normalize();
        this.mob.setDeltaMovement(away.x * 0.62, 0.3, away.z * 0.62);
        this.mob.needsSync = true;
        this.level().sendParticles(ParticleTypes.POOF, this.mob.getX(), this.mob.getY() + 0.1, this.mob.getZ(), 4, 0.2, 0.05, 0.2, 0.01);
    }

    // =====================================================================================
    // Werkzeuge der Angriffe
    // =====================================================================================

    /** Ein brauchbares Ziel (lebt, ist da, kein Zuschauer). */
    public static @Nullable LivingEntity valid(@Nullable LivingEntity target) {
        if (target == null || !target.isAlive() || target.isRemoved() || target instanceof ArmorStand) {
            return null;
        }
        if (target instanceof Player player && (player.isCreative() || player.isSpectator())) {
            return null;
        }
        return target;
    }

    /** Wen ein Flächenangriff trifft: das Ziel und alle, die keine Monster sind (keine Artgenossen, nicht es selbst). */
    public List<LivingEntity> victims(AABB box) {
        return this.level().getEntitiesOfClass(LivingEntity.class, box, e -> e != this.mob && e.isAlive() && valid(e) != null
                && (e == this.target || !(e instanceof Enemy) && !this.mob.isAlliedTo(e))
                && e != this.mob.getVehicle() && !this.mob.hasPassenger(e));
    }

    /**
     * Ein Treffer mit dem eigenen Angriff (mit allem, was dazugehört: Verzauberungen, Gift, Verdorren …), {@code factor} mal
     * so stark, dazu Rückstoß. Wer keinen Nahkampfschaden kennt, trifft mit {@code fallback}.
     */
    public boolean strike(LivingEntity victim, float factor, double knock, float fallback) {
        AttributeInstance damage = this.mob.getAttribute(Attributes.ATTACK_DAMAGE);
        boolean hurt;
        if (damage != null && damage.getValue() > 0.0) {
            damage.removeModifier(STRIKE);
            damage.addTransientModifier(new AttributeModifier(STRIKE, factor - 1.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            try {
                hurt = this.mob.doHurtTarget(this.level(), victim);
            } finally {
                damage.removeModifier(STRIKE);
            }
        } else {
            hurt = victim.hurtServer(this.level(), this.mob.damageSources().mobAttack(this.mob), fallback * factor);
        }
        if (hurt && knock > 0.0) {
            this.knock(victim, knock, 0.0);
        }
        return hurt;
    }

    /** Stößt jemanden vom Monster weg ({@code lift} = zusätzlich nach oben). */
    public void knock(LivingEntity victim, double power, double lift) {
        double resist = victim.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE);
        double scale = Math.max(0.0, 1.0 - resist);
        Vec3 away = victim.position().subtract(this.mob.position()).multiply(1.0, 0.0, 1.0);
        if (away.lengthSqr() < 1.0e-4) {
            away = Vec3.directionFromRotation(0.0f, this.mob.getYRot());
        }
        away = away.normalize().scale(power * scale);
        Vec3 v = victim.getDeltaMovement();
        victim.setDeltaMovement(v.x * 0.5 + away.x, Math.max(v.y, 0.0) + (0.22 + lift) * scale, v.z * 0.5 + away.z);
        victim.needsSync = true;
    }

    /** Dreht das Monster zum Ziel (oder hält die Richtung vom Beginn). */
    public void face(boolean track) {
        if (track && this.target != null) {
            this.yaw = yawTo(this.mob.position(), this.target.position());
            this.mob.getLookControl().setLookAt(this.target, 30.0f, 30.0f);
        }
        if (this.controlled()) {
            float y = Mth.approachDegrees(this.mob.getYRot(), this.yaw, 24.0f);
            this.mob.setYRot(y);
            this.mob.setYBodyRot(y);
            this.mob.setYHeadRot(y);
        }
    }

    public static float yawTo(Vec3 from, Vec3 to) {
        return (float) (Mth.atan2(to.z - from.z, to.x - from.x) * Mth.RAD_TO_DEG) - 90.0f;
    }

    /** Waagerechte Richtung, in die das Monster schaut (oder die beim Beginn festgelegte). */
    public Vec3 forward() {
        return Vec3.directionFromRotation(0.0f, this.yaw);
    }

    public void sound(SoundEvent sound, float volume, float pitch) {
        this.level().playSound(null, this.mob.getX(), this.mob.getY(), this.mob.getZ(), sound, SoundSource.HOSTILE, volume, pitch);
    }

    public void sound(Holder<SoundEvent> sound, float volume, float pitch) {
        this.sound(sound.value(), volume, pitch);
    }

    /** Teilchen in der Farbe der Familie. */
    public ParticleOptions dust(float size) {
        return new DustParticleOptions(this.family.color(), size);
    }

    public void particles(ParticleOptions particle, Vec3 at, int count, double spread, double speed) {
        this.level().sendParticles(particle, at.x, at.y, at.z, count, spread, spread, spread, speed);
    }

    /** Hält das Monster fest (es läuft nicht von selbst) oder lässt es wieder los. */
    void root(boolean on) {
        for (Holder<Attribute> attribute : List.of(Attributes.MOVEMENT_SPEED, Attributes.FLYING_SPEED)) {
            AttributeInstance instance = this.mob.getAttribute(attribute);
            if (instance == null) {
                continue;
            }
            instance.removeModifier(ROOT);
            if (on) {
                instance.addTransientModifier(new AttributeModifier(ROOT, -1.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            }
        }
    }

    static void modifier(Mob mob, Holder<Attribute> attribute, Identifier id, double amount) {
        AttributeInstance instance = mob.getAttribute(attribute);
        if (instance == null) {
            return;
        }
        instance.removeModifier(id);
        if (amount != 0.0) {
            // auf das Ergebnis (mal), wie das Tempo des Temperaments
            instance.addTransientModifier(new AttributeModifier(id, amount, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
    }

    /** Für Tests: Wartezeiten des Angriffs (Ticks). */
    int cooldown(MoveKind kind) {
        return this.cooldowns.getOrDefault(kind, 0);
    }

    void clearCooldowns() {
        this.cooldowns.clear();
        this.global = 0;
        this.engaged = FIRST_DELAY;
    }
}
