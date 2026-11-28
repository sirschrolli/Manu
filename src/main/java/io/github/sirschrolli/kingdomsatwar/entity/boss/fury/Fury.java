package io.github.sirschrolli.kingdomsatwar.entity.boss.fury;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.entity.boss.ElementalBoss;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierEntity;
import io.github.sirschrolli.kingdomsatwar.magic.fx.SpellFxEntity;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

/**
 * Der Zorn eines Bosses (seit 1.14): zwei Zornangriffe in seinem Element, die neben seinen eigenen Angriffen herlaufen,
 * und ab halber Kraft die <b>Zornphase</b> – ein Wutausbruch (Druckwelle, Gebrüll), danach kommen die Zornangriffe
 * doppelt so oft, er ist schneller und härter, und die {@link FuryAttack#NOVA} kommt dazu. Lebt nur auf dem Server.
 */
public final class Fury {
    static final Identifier FURY_SPEED = KingdomsAtWar.id("fury_speed");
    static final Identifier FURY_DAMAGE = KingdomsAtWar.id("fury_damage");
    /** Ab diesem Anteil der Gesundheit gerät ein Boss in Zorn. */
    public static final float ENRAGE_AT = 0.5f;
    /** Erst so lange (Ticks) im Kampf, dann kommt der erste Zornangriff. */
    public static final int FIRST = 200;
    /** So schneller und härter ist der Boss im Zorn (Anteil). */
    public static final double FURY_BONUS = 0.12;
    /** So weit (Blöcke) reichen Zornangriffe; so weit hört man die Ansagen. */
    public static final double RANGE = 32.0;
    /** Schaden der Zornangriffe (vor Schwierigkeit und Härte). */
    public static final float RAIN_DAMAGE = 6.0f;
    public static final float WAVE_DAMAGE = 5.0f;
    public static final float LINE_DAMAGE = 6.0f;
    public static final float PULL_DAMAGE = 9.0f;
    public static final float BEAM_DAMAGE = 4.0f;
    public static final float SECTOR_DAMAGE = 6.0f;
    public static final float NOVA_DAMAGE = 12.0f;
    public static final float BURST_DAMAGE = 4.0f;
    // Zeiten der Angriffe (Ticks ab Beginn)
    public static final int RAIN_DELAY = 22;
    public static final int[] WAVE_STARTS = {14, 30, 46};
    public static final int WAVE_LIFE = 32;
    public static final float WAVE_START = 1.0f;
    public static final float WAVE_GROWTH = 0.45f;
    public static final int LINES_OPEN = 18;
    public static final int PULL_BLAST = 56;
    public static final double PULL_RADIUS = 6.5;
    public static final int BEAMS_ARM = 15;
    public static final int BEAMS_END = 86;
    public static final double BEAM_LENGTH = 15.0;
    public static final int SECTOR_A = 24;
    public static final int SECTOR_B = 50;
    public static final double SECTOR_RADIUS = 13.0;
    public static final int NOVA_BLAST = 62;
    public static final double NOVA_RADIUS = 16.0;

    final Mob boss;
    final FuryProfile profile;
    boolean enraged;
    /** Wartezeit bis zum nächsten Zornangriff (läuft erst, wenn er {@link #FIRST} Ticks im Kampf ist). */
    int cooldown;
    int novaCooldown;
    int engaged;
    int next;
    @Nullable FuryAttack current;
    int t;
    // ---- was der laufende Angriff braucht
    Vec3 center = Vec3.ZERO;
    double angle;
    double spin;
    final List<Vec3> spots = new ArrayList<>();
    final List<Integer> due = new ArrayList<>();
    final List<Vec3> dirs = new ArrayList<>();
    final IntSet struck = new IntOpenHashSet();
    final Int2IntOpenHashMap lastHit = new Int2IntOpenHashMap();
    @Nullable SpellFxEntity charge;
    /** Wen der laufende (oder letzte) Zornangriff getroffen hat. */
    final IntSet victims = new IntOpenHashSet();
    /** Nur Gegner in diesem Bereich (Tests: die Nachbarn bleiben verschont); null = überall. */
    @Nullable AABB confine;
    int idle;

    Fury(Mob boss, FuryProfile profile) {
        this.boss = boss;
        this.profile = profile;
        // nach dem Laden: schon unter halber Kraft heißt schon im Zorn (ohne neuen Ausbruch)
        if (boss.getHealth() <= boss.getMaxHealth() * ENRAGE_AT && boss.getHealth() > 0.0f) {
            this.enraged = true;
            this.bonus(true);
        }
    }

    public FuryProfile profile() {
        return this.profile;
    }

    public @Nullable FuryAttack current() {
        return this.current;
    }

    public boolean enraged() {
        return this.enraged;
    }

    ServerLevel level() {
        return (ServerLevel) this.boss.level();
    }

    RandomSource random() {
        return this.boss.getRandom();
    }

    FuryElement element() {
        return this.profile.element();
    }

    // =====================================================================================
    // Ablauf
    // =====================================================================================

    void tick() {
        if (!this.boss.isAlive()) {
            this.stop();
            return;
        }
        LivingEntity target = this.boss.getTarget();
        boolean fighting = target != null && target.isAlive() && !target.isSpectator() && this.boss.distanceTo(target) < RANGE + 16.0
                && !(target instanceof Player p && p.isCreative());
        if (!fighting && this.current == null) {
            // ein kurzer Zielwechsel unterbricht den Kampf nicht
            if (++this.idle > 100) {
                this.engaged = 0;
            }
            return;
        }
        this.idle = 0;
        this.engaged++;
        if (!this.enraged && this.boss.getHealth() <= this.boss.getMaxHealth() * ENRAGE_AT && this.engaged > 20 && this.current == null) {
            this.enrage();
            return;
        }
        if (this.novaCooldown > 0) {
            this.novaCooldown--;
        }
        if (this.current != null) {
            boolean go = this.step(this.current, this.t++);
            if (!go || this.t > 400) {
                this.stop();
            }
            return;
        }
        if (this.engaged < FIRST || --this.cooldown > 0 || this.boss.isInvulnerable()) {
            return;
        }
        FuryAttack pick;
        if (this.enraged && this.novaCooldown <= 0) {
            pick = FuryAttack.NOVA;
        } else {
            pick = this.profile.attacks().get(this.next % this.profile.attacks().size());
            this.next++;
        }
        this.start(pick);
    }

    /** Beginnt einen Zornangriff (auch erzwungen, für Tests und Vorführungen). */
    void start(FuryAttack attack) {
        this.current = attack;
        this.t = 0;
        this.spots.clear();
        this.due.clear();
        this.dirs.clear();
        this.struck.clear();
        this.lastHit.clear();
        this.victims.clear();
        this.center = this.groundCenter();
        Component title = Component.translatable(attack.key(this.element())).withStyle(attack == FuryAttack.NOVA ? ChatFormatting.RED : ChatFormatting.YELLOW);
        this.announce(Component.translatable(attack == FuryAttack.NOVA ? "fury.kingdomsatwar.announce_nova" : "fury.kingdomsatwar.announce",
                this.boss.getDisplayName(), title).withStyle(ChatFormatting.GOLD));
        if (attack == FuryAttack.NOVA) {
            this.novaCooldown = 700 + this.random().nextInt(200);
        }
    }

    void stop() {
        if (this.charge != null) {
            this.charge.discard();
            this.charge = null;
        }
        if (this.current != null) {
            this.cooldown = this.enraged ? 180 + this.random().nextInt(100) : 320 + this.random().nextInt(140);
        }
        this.current = null;
        this.t = 0;
    }

    /** Der Wutausbruch zu Beginn der Zornphase. */
    void enrage() {
        this.enraged = true;
        this.bonus(true);
        ServerLevel level = this.level();
        Vec3 c = this.boss.position();
        this.sound(SoundEvents.RAVAGER_ROAR, 4.0f, 0.55f);
        this.sound(SoundEvents.ENDER_DRAGON_GROWL, 2.5f, 1.3f);
        Vec3 g = this.groundCenter();
        SpellFxEntity.create(level, SpellFxEntity.Kind.SHOCKWAVE, g.add(0.0, 0.05, 0.0), this.element().dragon(), this.element().color(), 16, 1.5f)
                .param(0.75f).spawn();
        level.sendParticles(this.element().particle(), c.x, c.y + this.boss.getBbHeight() * 0.5, c.z, 80, this.boss.getBbWidth(),
                this.boss.getBbHeight() * 0.5, this.boss.getBbWidth(), 0.25);
        level.sendParticles(this.element().dust(2.0f), c.x, c.y + this.boss.getBbHeight() * 0.5, c.z, 60, this.boss.getBbWidth() * 1.2,
                this.boss.getBbHeight() * 0.6, this.boss.getBbWidth() * 1.2, 0.0);
        for (LivingEntity victim : this.fighters()) {
            if (victim.distanceTo(this.boss) <= 9.0 + this.boss.getBbWidth() * 0.5) {
                this.hurt(victim, BURST_DAMAGE);
                this.knock(victim, g, 1.4, 0.45);
            }
        }
        this.announce(Component.translatable("fury.kingdomsatwar.enraged", this.boss.getDisplayName()).withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
        this.cooldown = 60;
        this.novaCooldown = 160;
    }

    /** Schneller und härter im Zorn (nicht gespeichert – nach dem Laden setzt der Konstruktor es wieder). */
    void bonus(boolean on) {
        for (Holder<Attribute> attribute : List.of(Attributes.MOVEMENT_SPEED, Attributes.FLYING_SPEED)) {
            modifier(attribute, FURY_SPEED, on ? FURY_BONUS : 0.0);
        }
        modifier(Attributes.ATTACK_DAMAGE, FURY_DAMAGE, on ? FURY_BONUS : 0.0);
    }

    private void modifier(Holder<Attribute> attribute, Identifier id, double amount) {
        AttributeInstance instance = this.boss.getAttribute(attribute);
        if (instance == null) {
            return;
        }
        instance.removeModifier(id);
        if (amount != 0.0) {
            instance.addTransientModifier(new AttributeModifier(id, amount, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        }
    }

    private boolean step(FuryAttack attack, int t) {
        return switch (attack) {
            case RAIN -> this.rain(t);
            case WAVES -> this.waves(t);
            case LINES -> this.lines(t);
            case PULL -> this.pull(t);
            case BEAMS -> this.beams(t);
            case SECTORS -> this.sectors(t);
            case NOVA -> this.nova(t);
        };
    }

    // =====================================================================================
    // Die Angriffe
    // =====================================================================================

    /** Einschläge prasseln herab: erst ein Warnkreis unter jedem Spieler (und um ihn herum), dann der Einschlag. */
    private boolean rain(int t) {
        ServerLevel level = this.level();
        FuryElement e = this.element();
        if (t == 0) {
            this.castCircle(3.0f, 14);
        }
        if (t >= 4 && t <= 46 && (t - 4) % 6 == 0) {
            boolean first = t == 4;
            for (LivingEntity victim : this.fighters()) {
                Vec3 spot = victim.position();
                if (!first) {
                    double a = this.random().nextDouble() * Mth.TWO_PI;
                    double r = 1.5 + this.random().nextDouble() * 3.0;
                    spot = spot.add(Math.cos(a) * r, 0.0, Math.sin(a) * r).add(victim.getDeltaMovement().scale(6.0));
                }
                Vec3 g = ground(level, spot, 8);
                if (g == null) {
                    continue;
                }
                this.spots.add(g);
                this.due.add(t + RAIN_DELAY);
                SpellFxEntity.create(level, SpellFxEntity.Kind.CIRCLE, g.add(0.0, 0.02, 0.0), e.dragon(), e.color(), RAIN_DELAY, 1.6f).spawn();
            }
        }
        int last = 0;
        for (int i = 0; i < this.spots.size(); i++) {
            int d = this.due.get(i);
            last = Math.max(last, d);
            if (d != t) {
                continue;
            }
            Vec3 g = this.spots.get(i);
            e.impact(level, g, 1.3f);
            if (e == FuryElement.STEEL) {
                // Pfeilhagel: ein Schwarm Pfeile prasselt herab
                level.sendParticles(ParticleTypes.CRIT, g.x, g.y + 2.5, g.z, 40, 0.7, 1.8, 0.7, 0.0);
                level.playSound(null, g.x, g.y, g.z, SoundEvents.ARROW_HIT, SoundSource.HOSTILE, 1.2f, 0.7f + this.random().nextFloat() * 0.4f);
            }
            for (LivingEntity victim : this.fighters()) {
                if (flat(victim.position(), g) <= 1.8 + victim.getBbWidth() * 0.5 && victim.getY() - g.y > -1.5 && victim.getY() - g.y < 3.0) {
                    this.hurt(victim, RAIN_DAMAGE);
                    e.afflict(victim, this.boss);
                }
            }
            level.playSound(null, g.x, g.y, g.z, e.impactSound(), SoundSource.HOSTILE, 1.4f, 0.8f + this.random().nextFloat() * 0.3f);
        }
        return t < Math.max(last + 2, 50);
    }

    /** Drei Ringe breiten sich nacheinander vom Boss aus – wer nicht darüber springt, wird getroffen. */
    private boolean waves(int t) {
        ServerLevel level = this.level();
        FuryElement e = this.element();
        if (t == 0) {
            this.castCircle(3.5f, WAVE_STARTS[0]);
            this.sound(SoundEvents.WARDEN_SONIC_CHARGE, 2.0f, 1.2f);
        }
        for (int k = 0; k < WAVE_STARTS.length; k++) {
            int age = t - WAVE_STARTS[k];
            if (age == 0) {
                SpellFxEntity.create(level, SpellFxEntity.Kind.SHOCKWAVE, this.center.add(0.0, 0.05, 0.0), e.dragon(), e.color(), WAVE_LIFE, WAVE_START)
                        .param(WAVE_GROWTH).spawn();
                this.sound(SoundEvents.MACE_SMASH_GROUND_HEAVY, 1.6f, 0.6f + k * 0.1f);
            }
            if (age < 0 || age > WAVE_LIFE) {
                continue;
            }
            double r = WAVE_START + WAVE_GROWTH * age;
            for (LivingEntity victim : this.fighters()) {
                double d = flat(victim.position(), this.center);
                int key = victim.getId() * 4 + k;
                if (Math.abs(d - r) > 0.7 + victim.getBbWidth() * 0.5 || victim.getY() - this.center.y > SpellFxEntity.SHOCKWAVE_HEIGHT
                        || this.struck.contains(key)) {
                    continue;
                }
                this.struck.add(key);
                this.hurt(victim, WAVE_DAMAGE);
                this.knock(victim, this.center, 0.7, 0.25);
                e.afflict(victim, this.boss);
            }
        }
        return t < WAVE_STARTS[WAVE_STARTS.length - 1] + WAVE_LIFE + 1;
    }

    /** Risse laufen sternförmig vom Boss aus (einer auf jeden Spieler), dann bricht das Element an ihnen entlang hervor. */
    private boolean lines(int t) {
        ServerLevel level = this.level();
        FuryElement e = this.element();
        if (t == 0) {
            List<LivingEntity> fighters = this.fighters();
            double base = this.random().nextDouble() * Mth.TWO_PI;
            for (int i = 0; i < Math.min(3, fighters.size()); i++) {
                Vec3 to = fighters.get(i).position().subtract(this.center).multiply(1.0, 0.0, 1.0);
                if (to.lengthSqr() > 1.0e-3) {
                    this.dirs.add(to.normalize());
                    base = Math.atan2(to.z, to.x);
                }
            }
            for (int i = this.dirs.size(); i < 5; i++) {
                double a = base + Mth.TWO_PI * i / 5.0 + 0.3;
                this.dirs.add(new Vec3(Math.cos(a), 0.0, Math.sin(a)));
            }
            for (Vec3 dir : this.dirs) {
                Vec3 from = this.center.add(dir.scale(1.5));
                Vec3 to = this.center.add(dir.scale(19.0));
                SpellFxEntity.create(level, SpellFxEntity.Kind.FISSURE, from.add(0.0, 0.03, 0.0), e.dragon(), e.color(), 44, 0.8f).param(4.0f).end(to)
                        .spawn();
            }
            this.sound(SoundEvents.WARDEN_DIG, 2.0f, 0.8f);
        }
        int k = (t - LINES_OPEN) / 2;
        if (t >= LINES_OPEN && (t - LINES_OPEN) % 2 == 0 && k <= 11) {
            double s = 2.0 + 1.5 * k;
            for (Vec3 dir : this.dirs) {
                Vec3 p = this.center.add(dir.scale(s));
                Vec3 g = ground(level, p.add(0.0, 2.0, 0.0), 6);
                if (g == null) {
                    continue;
                }
                e.impact(level, g, 0.9f);
                for (LivingEntity victim : this.fighters()) {
                    if (flat(victim.position(), g) <= 1.3 + victim.getBbWidth() * 0.5 && victim.getY() - g.y > -1.5 && victim.getY() - g.y < 3.0
                            && this.struck.add(victim.getId())) {
                        this.hurt(victim, LINE_DAMAGE);
                        e.afflict(victim, this.boss);
                    }
                }
            }
            if (k % 2 == 0) {
                this.sound(e.impactSound(), 1.2f, 0.9f + k * 0.03f);
            }
        }
        return t < LINES_OPEN + 26;
    }

    /** Ein Sog zieht alle zum Boss (gegen ihn anlaufen!) – dann explodiert die Mitte. */
    private boolean pull(int t) {
        ServerLevel level = this.level();
        FuryElement e = this.element();
        // das schwarze Loch schwebt über dem Boss
        Vec3 hole = new Vec3(this.center.x, Math.min(this.boss.getY() + this.boss.getBbHeight() + 1.6, this.center.y + 6.0), this.center.z);
        if (t == 0) {
            SpellFxEntity.create(level, SpellFxEntity.Kind.VORTEX, hole, null, e.color(), PULL_BLAST, 2.4f).spawn();
            // der Kreis am Boden zeigt, wie weit die Explosion reicht
            this.castCircle((float) PULL_RADIUS, PULL_BLAST);
            this.sound(SoundEvents.WARDEN_SONIC_CHARGE, 2.5f, 0.6f);
        }
        if (t >= 6 && t < PULL_BLAST) {
            for (LivingEntity victim : this.fighters()) {
                Vec3 to = this.center.subtract(victim.position()).multiply(1.0, 0.0, 1.0);
                double d = to.length();
                if (d < 0.6 || d > 18.0) {
                    continue;
                }
                double resist = 1.0 - victim.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE) * 0.6;
                Vec3 pull = to.normalize().scale(0.075 * resist);
                victim.setDeltaMovement(victim.getDeltaMovement().add(pull));
                victim.needsSync = true;
            }
            for (int i = 0; i < 6; i++) {
                double a = this.random().nextDouble() * Mth.TWO_PI;
                double r = 3.0 + this.random().nextDouble() * 9.0;
                Vec3 p = this.center.add(Math.cos(a) * r, 0.3 + this.random().nextDouble() * 1.5, Math.sin(a) * r);
                Vec3 v = hole.subtract(p).normalize().scale(0.45);
                level.sendParticles(i % 2 == 0 ? e.particle() : e.dust(1.0f), p.x, p.y, p.z, 0, v.x, v.y, v.z, 1.0);
            }
        }
        if (t == PULL_BLAST) {
            SpellFxEntity.create(level, SpellFxEntity.Kind.SHOCKWAVE, this.center.add(0.0, 0.05, 0.0), e.dragon(), e.color(), 10, 1.0f).param(0.6f).spawn();
            level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, this.center.x, this.center.y + 1.0, this.center.z, 1, 0.0, 0.0, 0.0, 0.0);
            level.sendParticles(e.particle(), this.center.x, this.center.y + 1.0, this.center.z, 60, 2.5, 1.0, 2.5, 0.3);
            this.sound(SoundEvents.GENERIC_EXPLODE.value(), 2.5f, 0.8f);
            for (LivingEntity victim : this.fighters()) {
                if (flat(victim.position(), this.center) <= PULL_RADIUS + victim.getBbWidth() * 0.5 && Math.abs(victim.getY() - this.center.y) < 4.0) {
                    this.hurt(victim, PULL_DAMAGE);
                    this.knock(victim, this.center, 1.3, 0.4);
                    e.afflict(victim, this.boss);
                }
            }
        }
        return t < PULL_BLAST + 4;
    }

    /** Strahlen am Boden drehen sich um den Boss – darüber springen oder vor ihnen herlaufen. */
    private boolean beams(int t) {
        ServerLevel level = this.level();
        FuryElement e = this.element();
        int n = 4;
        if (t == 0) {
            List<LivingEntity> fighters = this.fighters();
            this.angle = fighters.isEmpty() ? 0.0 : Math.atan2(fighters.getFirst().getZ() - this.center.z, fighters.getFirst().getX() - this.center.x)
                    + Math.PI / n;
            this.spin = (this.random().nextBoolean() ? 1.0 : -1.0) * Math.toRadians(2.2);
            this.castCircle(2.5f, BEAMS_ARM);
            this.sound(SoundEvents.BEACON_ACTIVATE, 2.0f, 1.4f);
        }
        double a0 = this.angle + this.spin * Math.max(0, t - BEAMS_ARM);
        Vec3 origin = this.center.add(0.0, 0.55, 0.0);
        for (int i = 0; i < n; i++) {
            double a = a0 + Mth.TWO_PI * i / n;
            Vec3 dir = new Vec3(Math.cos(a), 0.0, Math.sin(a));
            Vec3 end = origin.add(dir.scale(BEAM_LENGTH));
            if (t < BEAMS_ARM) {
                if (t % 2 == 0) {
                    for (double s = 1.0; s < BEAM_LENGTH; s += 1.0) {
                        Vec3 p = origin.add(dir.scale(s));
                        level.sendParticles(e.dust(0.7f), p.x, p.y, p.z, 1, 0.0, 0.0, 0.0, 0.0);
                    }
                }
                continue;
            }
            if (t % 2 == 0) {
                SpellFxEntity.create(level, SpellFxEntity.Kind.BEAM, origin, e.dragon(), e.color(), 3, 0.4f).end(end).spawn();
            }
            for (LivingEntity victim : this.fighters()) {
                Vec3 p = victim.position();
                Vec3 rel = p.subtract(origin).multiply(1.0, 0.0, 1.0);
                double along = rel.dot(dir);
                if (along < 0.0 || along > BEAM_LENGTH) {
                    continue;
                }
                double off = rel.subtract(dir.scale(along)).length();
                if (off > 0.6 + victim.getBbWidth() * 0.5 || victim.getY() - this.center.y > SpellFxEntity.SHOCKWAVE_HEIGHT
                        || victim.getY() - this.center.y < -1.5) {
                    continue;
                }
                int last = this.lastHit.getOrDefault(victim.getId(), -100);
                if (t - last < 12) {
                    continue;
                }
                this.lastHit.put(victim.getId(), t);
                this.hurt(victim, BEAM_DAMAGE);
                e.afflict(victim, this.boss);
            }
        }
        if (t >= BEAMS_ARM && t % 10 == 0) {
            this.sound(SoundEvents.BEACON_AMBIENT, 2.0f, 1.6f);
        }
        return t < BEAMS_END;
    }

    /** Der Boden rings um den Boss teilt sich in acht Felder: erst brechen die einen auf, dann die anderen. */
    private boolean sectors(int t) {
        ServerLevel level = this.level();
        FuryElement e = this.element();
        if (t == 0) {
            this.angle = this.random().nextDouble() * Mth.TWO_PI;
            this.castCircle(3.0f, SECTOR_A);
            this.sound(SoundEvents.WARDEN_SONIC_CHARGE, 1.8f, 1.5f);
            this.sound(SoundEvents.WARDEN_DIG, 1.6f, 1.2f);
            // Risse teilen den Boden in acht Felder
            for (int k = 0; k < 8; k++) {
                double a = this.angle + Math.PI / 4.0 * k;
                Vec3 dir = new Vec3(Math.cos(a), 0.0, Math.sin(a));
                SpellFxEntity.create(level, SpellFxEntity.Kind.FISSURE, this.center.add(dir.scale(1.6)).add(0.0, 0.03, 0.0), e.dragon(), e.color(),
                        SECTOR_B + 6, 0.6f).param(2.0f).end(this.center.add(dir.scale(SECTOR_RADIUS))).spawn();
            }
        }
        boolean phaseA = t < SECTOR_A + 2;
        int warnFrom = phaseA ? 0 : SECTOR_A + 2;
        int blast = phaseA ? SECTOR_A : SECTOR_B;
        if (t >= warnFrom && t < blast && t % 3 == 0) {
            // die Felder, die gleich aufbrechen, glühen
            for (int s = phaseA ? 0 : 1; s < 8; s += 2) {
                for (int i = 0; i < 18; i++) {
                    double a = this.angle + Math.PI / 4.0 * (s + 0.05 + this.random().nextDouble() * 0.9);
                    double r = 1.5 + Math.sqrt(this.random().nextDouble()) * (SECTOR_RADIUS - 1.5);
                    Vec3 p = this.center.add(Math.cos(a) * r, 0.15, Math.sin(a) * r);
                    level.sendParticles(e.dust(1.4f), p.x, p.y, p.z, 1, 0.0, 0.02, 0.0, 0.0);
                }
                if (t % 6 == 0) {
                    double a = this.angle + Math.PI / 4.0 * (s + 0.5);
                    double r = 3.0 + this.random().nextDouble() * (SECTOR_RADIUS - 5.0);
                    Vec3 p = this.center.add(Math.cos(a) * r, 0.3, Math.sin(a) * r);
                    level.sendParticles(e.particle(), p.x, p.y, p.z, 4, 1.0, 0.1, 1.0, 0.02);
                }
            }
        }
        if (t == SECTOR_A || t == SECTOR_B) {
            int first = t == SECTOR_A ? 0 : 1;
            for (int s = first; s < 8; s += 2) {
                for (int i = 0; i < 6; i++) {
                    // gleichmäßig über das Feld: zwei innen, vier außen
                    double a = this.angle + Math.PI / 4.0 * (s + (i < 2 ? 0.3 + 0.4 * i : 0.12 + 0.25 * (i - 2)));
                    double r = i < 2 ? 3.5 + this.random().nextDouble() * 1.5 : 8.0 + this.random().nextDouble() * 3.5;
                    Vec3 g = ground(level, this.center.add(Math.cos(a) * r, 2.0, Math.sin(a) * r), 6);
                    if (g != null) {
                        e.impact(level, g, 1.1f);
                    }
                }
            }
            for (LivingEntity victim : this.fighters()) {
                Vec3 rel = victim.position().subtract(this.center);
                double d = Math.sqrt(rel.x * rel.x + rel.z * rel.z);
                if (d > SECTOR_RADIUS || d < 1.0 || Math.abs(rel.y) > 4.0) {
                    continue;
                }
                double a = Mth.positiveModulo(Math.atan2(rel.z, rel.x) - this.angle, Mth.TWO_PI);
                int sector = (int) (a / (Math.PI / 4.0)) % 8;
                if (sector % 2 == first) {
                    this.hurt(victim, SECTOR_DAMAGE);
                    e.afflict(victim, this.boss);
                    victim.setDeltaMovement(victim.getDeltaMovement().add(0.0, 0.45, 0.0));
                    victim.needsSync = true;
                }
            }
            this.sound(e.impactSound(), 2.0f, 0.7f);
            this.sound(SoundEvents.GENERIC_EXPLODE.value(), 1.2f, 1.1f);
        }
        return t < SECTOR_B + 4;
    }

    /** Die Vernichtung (nur im Zorn): der Boss lädt – wer nicht hinter Blöcken Deckung sucht, wird von der Explosion erfasst. */
    private boolean nova(int t) {
        ServerLevel level = this.level();
        FuryElement e = this.element();
        Vec3 eye = this.boss.position().add(0.0, this.boss.getBbHeight() * 0.6, 0.0);
        if (t == 0) {
            this.charge = SpellFxEntity.create(level, SpellFxEntity.Kind.CAST_CIRCLE, this.center.add(0.0, 0.05, 0.0), e.dragon(), e.color(), NOVA_BLAST + 2,
                    4.5f).param(0.0f).spawn();
            this.sound(SoundEvents.WARDEN_SONIC_CHARGE, 3.0f, 0.5f);
        }
        if (t < NOVA_BLAST) {
            if (this.charge != null) {
                this.charge.param(Math.min(1.0f, t / (float) NOVA_BLAST));
            }
            for (int i = 0; i < 6; i++) {
                double a = this.random().nextDouble() * Mth.TWO_PI;
                double r = 5.0 + this.random().nextDouble() * 6.0;
                Vec3 p = eye.add(Math.cos(a) * r, (this.random().nextDouble() - 0.3) * 4.0, Math.sin(a) * r);
                Vec3 v = eye.subtract(p).normalize().scale(0.5);
                level.sendParticles(e.particle(), p.x, p.y, p.z, 0, v.x, v.y, v.z, 1.0);
            }
            if (t == 30) {
                this.sound(SoundEvents.WARDEN_SONIC_CHARGE, 3.0f, 0.8f);
            }
            if (t % 10 == 0) {
                level.sendParticles(e.dust(2.0f), eye.x, eye.y, eye.z, 10 + t / 3, 0.4 + t / 60.0, 0.4 + t / 60.0, 0.4 + t / 60.0, 0.0);
            }
            return true;
        }
        if (t == NOVA_BLAST) {
            SpellFxEntity.create(level, SpellFxEntity.Kind.SHOCKWAVE, this.center.add(0.0, 0.05, 0.0), e.dragon(), e.color(), 16, 1.0f).param(1.1f).spawn();
            level.sendParticles(ParticleTypes.SONIC_BOOM, eye.x, eye.y, eye.z, 1, 0.0, 0.0, 0.0, 0.0);
            level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, eye.x, eye.y, eye.z, 2, 1.0, 1.0, 1.0, 0.0);
            level.sendParticles(e.particle(), eye.x, eye.y, eye.z, 120, 3.0, 2.0, 3.0, 0.5);
            this.sound(SoundEvents.GENERIC_EXPLODE.value(), 4.0f, 0.6f);
            this.sound(SoundEvents.WARDEN_SONIC_BOOM, 3.0f, 0.8f);
            for (LivingEntity victim : this.fighters()) {
                if (victim.distanceTo(this.boss) > NOVA_RADIUS + this.boss.getBbWidth() * 0.5) {
                    continue;
                }
                if (this.covered(eye, victim)) {
                    // Deckung: die Welle bricht sich an den Blöcken
                    Vec3 p = victim.getEyePosition();
                    level.sendParticles(ParticleTypes.CRIT, p.x, p.y, p.z, 12, 0.3, 0.3, 0.3, 0.2);
                    continue;
                }
                this.hurt(victim, NOVA_DAMAGE);
                this.knock(victim, this.boss.position(), 1.6, 0.5);
                e.afflict(victim, this.boss);
            }
            if (this.charge != null) {
                this.charge.discard();
                this.charge = null;
            }
        }
        return t < NOVA_BLAST + 4;
    }

    /** Liegen Blöcke zwischen dem Boss und dem Opfer (an Augen und an der Brust)? */
    boolean covered(Vec3 from, LivingEntity victim) {
        for (Vec3 to : List.of(victim.getEyePosition(), victim.position().add(0.0, victim.getBbHeight() * 0.5, 0.0))) {
            BlockHitResult hit = this.level().clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this.boss));
            if (hit.getType() == HitResult.Type.MISS) {
                return false;
            }
        }
        return true;
    }

    // =====================================================================================
    // Werkzeuge
    // =====================================================================================

    /** Wen die Zornangriffe treffen: Spieler (nicht im Kreativmodus), Soldaten, Tiere der Spieler, das Ziel – keine Monster. */
    List<LivingEntity> fighters() {
        AABB box = this.boss.getBoundingBox().inflate(RANGE, 16.0, RANGE);
        if (this.confine != null) {
            box = box.intersect(this.confine);
        }
        List<LivingEntity> list = this.level().getEntitiesOfClass(LivingEntity.class, box, this::foe);
        list.sort((a, b) -> Double.compare(a.distanceToSqr(this.boss), b.distanceToSqr(this.boss)));
        return list.size() > 10 ? list.subList(0, 10) : list;
    }

    boolean foe(LivingEntity e) {
        if (e == this.boss || !e.isAlive() || e.isSpectator() || this.boss.isAlliedTo(e)) {
            return false;
        }
        if (e instanceof Player player) {
            return !player.isCreative();
        }
        if (this.boss instanceof ElementalBoss elemental) {
            return elemental.isBossEnemy(e);
        }
        return e == this.boss.getTarget() || e instanceof SoldierEntity || e instanceof OwnableEntity owned && owned.getOwner() instanceof Player;
    }

    void hurt(LivingEntity victim, float damage) {
        this.victims.add(victim.getId());
        if (this.boss instanceof ElementalBoss elemental) {
            elemental.hurtTarget(this.level(), victim, damage);
        } else {
            victim.hurtServer(this.level(), this.boss.damageSources().mobAttack(this.boss), damage);
        }
    }

    void knock(LivingEntity victim, Vec3 from, double power, double lift) {
        double resist = Math.max(0.0, 1.0 - victim.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
        Vec3 away = victim.position().subtract(from).multiply(1.0, 0.0, 1.0);
        if (away.lengthSqr() < 1.0e-4) {
            away = new Vec3(1.0, 0.0, 0.0);
        }
        away = away.normalize().scale(power * resist);
        victim.setDeltaMovement(victim.getDeltaMovement().multiply(0.4, 0.0, 0.4).add(away.x, (0.25 + lift) * resist, away.z));
        victim.needsSync = true;
    }

    /** Ein Zauberkreis unter dem Boss (er holt aus). */
    void castCircle(float size, int life) {
        SpellFxEntity.create(this.level(), SpellFxEntity.Kind.CAST_CIRCLE, this.center.add(0.0, 0.05, 0.0), this.element().dragon(), this.element().color(),
                life, size).param(1.0f).spawn();
    }

    /** Wo die Angriffe ihre Mitte haben: der Boden unter dem Boss – fliegt er hoch darüber, der Boden unter seinem Ziel. */
    Vec3 groundCenter() {
        ServerLevel level = this.level();
        Vec3 g = ground(level, this.boss.position(), 12);
        if (g != null) {
            return g;
        }
        LivingEntity target = this.boss.getTarget();
        if (target != null) {
            Vec3 tg = ground(level, target.position(), 8);
            if (tg != null) {
                return tg;
            }
        }
        return this.boss.position();
    }

    String name() {
        return this.boss.getDisplayName().getString();
    }

    void announce(Component message) {
        for (ServerPlayer player : this.level().players()) {
            if (player.distanceTo(this.boss) <= RANGE + 16.0) {
                player.sendOverlayMessage(message);
            }
        }
    }

    void sound(SoundEvent sound, float volume, float pitch) {
        this.level().playSound(null, this.boss.getX(), this.boss.getY(), this.boss.getZ(), sound, SoundSource.HOSTILE, volume, pitch);
    }

    void sound(Holder<SoundEvent> sound, float volume, float pitch) {
        this.sound(sound.value(), volume, pitch);
    }

    static double flat(Vec3 a, Vec3 b) {
        double dx = a.x - b.x;
        double dz = a.z - b.z;
        return Math.sqrt(dx * dx + dz * dz);
    }

    /** Die Oberkante des Bodens unter (oder bei) einem Punkt, höchstens {@code depth} Blöcke tiefer (sonst null). */
    static @Nullable Vec3 ground(ServerLevel level, Vec3 at, int depth) {
        BlockPos.MutableBlockPos pos = BlockPos.containing(at.x, at.y + 1.0, at.z).mutable();
        for (int i = 0; i < depth + 2; i++) {
            BlockPos below = pos.below();
            BlockState under = level.getBlockState(below);
            BlockState here = level.getBlockState(pos);
            VoxelShape shape = under.getCollisionShape(level, below);
            boolean free = here.getCollisionShape(level, pos).isEmpty() && here.getFluidState().isEmpty();
            if (!shape.isEmpty() && free) {
                return new Vec3(at.x, below.getY() + shape.max(Direction.Axis.Y), at.z);
            }
            if (free && !under.getFluidState().isEmpty()) {
                // auf dem Wasser (Krake, Meerhexe): die Oberfläche
                return new Vec3(at.x, below.getY() + under.getFluidState().getHeight(level, below), at.z);
            }
            pos.move(0, -1, 0);
        }
        return null;
    }
}
