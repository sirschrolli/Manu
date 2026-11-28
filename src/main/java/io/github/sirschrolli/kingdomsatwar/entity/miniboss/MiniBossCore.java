package io.github.sirschrolli.kingdomsatwar.entity.miniboss;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Was alle Mini-Bosse gemeinsam haben (seit 1.15), auf dem Server: Schlaf und Erwachen, die Bossleiste, der Heimplatz
 * (läuft man davon, kehrt er heim und schläft wieder ein – geheilt), die Rage ab {@link #ENRAGE_AT} seiner Kraft, Diener,
 * die mit ihm vergehen, und die besonderen Angriffe ({@link MiniSpecial}), von denen immer nur einer läuft. Die Wesen
 * rufen {@link #tick} am Ende ihres eigenen Denkens auf.
 */
public final class MiniBossCore {
    /** In Rage ab diesem Anteil der Gesundheit. */
    public static final float ENRAGE_AT = 0.4f;
    /** So nah muss man kommen (mit Blick auf ihn), damit er erwacht. */
    public static final double WAKE_RADIUS = 11.0;
    /** So weit sieht man die Bossleiste. */
    public static final double BAR_RADIUS = 40.0;
    /** Weiter als so weit von seinem Platz verfolgt er niemanden. */
    public static final double LEASH = 32.0;
    /** Ist so lange (Ticks) niemand mehr in der Nähe, kehrt er heim und schläft wieder ein. */
    public static final int GIVE_UP = 400;
    static final Identifier ROOT = KingdomsAtWar.id("mini_boss_root");

    private final Mob mob;
    private final MiniBoss boss;
    private final ServerBossEvent bar;
    private final List<MiniSpecial> specials = new ArrayList<>();
    private @Nullable MiniSpecial current;
    private int t;
    private @Nullable LivingEntity specialTarget;
    private int global = 50;
    private @Nullable BlockPos home;
    private final List<UUID> minions = new ArrayList<>();
    private int lonely;
    private int homeward;

    public MiniBossCore(Mob mob, BossEvent.BossBarColor color) {
        this.mob = mob;
        this.boss = (MiniBoss) mob;
        this.bar = new ServerBossEvent(UUID.randomUUID(), mob.getDisplayName(), color, BossEvent.BossBarOverlay.NOTCHED_10);
        this.bar.setVisible(false);
    }

    public MiniBossCore special(MiniSpecial special) {
        this.specials.add(special);
        return this;
    }

    public List<MiniSpecial> specials() {
        return this.specials;
    }

    public @Nullable MiniSpecial current() {
        return this.current;
    }

    /** Wie weit der laufende Angriff ist (Ticks). */
    public int progress() {
        return this.t;
    }

    public @Nullable BlockPos home() {
        return this.home;
    }

    public void setHome(BlockPos home) {
        this.home = home.immutable();
    }

    // =====================================================================================
    // Ablauf
    // =====================================================================================

    public void tick(ServerLevel level) {
        if (!this.mob.isAlive()) {
            return;
        }
        if (this.home == null) {
            this.home = this.mob.blockPosition();
        }
        if (this.mob.tickCount % 5 == 0) {
            this.bar.setProgress(this.mob.getHealth() / this.mob.getMaxHealth());
            this.bar.setName(this.mob.getDisplayName());
        }
        if (!this.boss.isAwake()) {
            this.sleep(level);
            return;
        }
        if (!this.boss.isEnraged() && this.mob.getHealth() <= this.mob.getMaxHealth() * ENRAGE_AT) {
            this.boss.setEnraged(true);
            this.announce(level, Component.translatable("miniboss.kingdomsatwar.enraged", this.mob.getDisplayName())
                    .withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
            this.boss.onEnrage(level);
            this.global = Math.min(this.global, 10);
        }
        LivingEntity target = this.mob.getTarget();
        if (target != null && (!this.valid(target) || this.home.distToCenterSqr(target.position()) > LEASH * LEASH)) {
            this.mob.setTarget(null);
            target = null;
        }
        if (this.mob.tickCount % 20 == 0) {
            this.minions.removeIf(id -> !(level.getEntity(id) instanceof LivingEntity m) || !m.isAlive());
            if (this.fighterNear(level)) {
                this.lonely = 0;
            } else if ((this.lonely += 20) >= GIVE_UP) {
                this.giveUp(level);
                return;
            }
        }
        for (MiniSpecial special : this.specials) {
            if (special.cooldown > 0) {
                special.cooldown--;
            }
        }
        if (this.current != null) {
            LivingEntity aim = this.specialTarget != null && this.specialTarget.isAlive() ? this.specialTarget : target;
            if (aim == null) {
                this.stopSpecial(level);
            } else {
                if (this.current.roots()) {
                    this.mob.getNavigation().stop();
                }
                this.current.tick(level, aim, this.t++);
                if (this.t > this.current.duration()) {
                    this.stopSpecial(level);
                }
            }
            return;
        }
        if (target == null) {
            // heim an seinen Platz
            if (this.mob.blockPosition().distSqr(this.home) > 16 && ++this.homeward % 40 == 1) {
                this.mob.getNavigation().moveTo(this.home.getX() + 0.5, this.home.getY(), this.home.getZ() + 0.5, 1.0);
            }
            return;
        }
        this.homeward = 0;
        if (--this.global > 0) {
            return;
        }
        MiniSpecial pick = this.choose(target);
        if (pick != null) {
            this.startSpecial(level, pick, target);
        } else {
            this.global = 10;
        }
    }

    private @Nullable MiniSpecial choose(LivingEntity target) {
        double distance = this.mob.distanceTo(target);
        int total = 0;
        List<MiniSpecial> ready = new ArrayList<>();
        for (MiniSpecial special : this.specials) {
            if (special.cooldown <= 0 && (!special.enragedOnly() || this.boss.isEnraged()) && special.ready(target, distance)) {
                ready.add(special);
                total += special.weight();
            }
        }
        if (ready.isEmpty()) {
            return null;
        }
        int roll = this.mob.getRandom().nextInt(total);
        for (MiniSpecial special : ready) {
            roll -= special.weight();
            if (roll < 0) {
                return special;
            }
        }
        return ready.getLast();
    }

    private void startSpecial(ServerLevel level, MiniSpecial special, LivingEntity target) {
        this.current = special;
        this.specialTarget = target;
        this.t = 0;
        this.root(special.roots());
        this.mob.getLookControl().setLookAt(target, 60.0f, 60.0f);
        special.start(level, target);
    }

    private void stopSpecial(ServerLevel level) {
        MiniSpecial special = this.current;
        if (special == null) {
            return;
        }
        special.stop(level);
        int recharge = special.recharge(this.mob.getRandom());
        special.cooldown = this.boss.isEnraged() ? recharge * 2 / 3 : recharge;
        this.global = (this.boss.isEnraged() ? 22 : 36) + this.mob.getRandom().nextInt(24);
        this.current = null;
        this.specialTarget = null;
        this.t = 0;
        this.root(false);
    }

    /** Beginnt einen besonderen Angriff sofort (Tests und Vorführungen). */
    public boolean force(ServerLevel level, String id, LivingEntity target) {
        for (MiniSpecial special : this.specials) {
            if (special.id().equals(id)) {
                if (!this.boss.isAwake()) {
                    this.wake(level, null);
                }
                this.stopSpecial(level);
                this.mob.setTarget(target);
                this.startSpecial(level, special, target);
                return true;
            }
        }
        return false;
    }

    /** Hält die besonderen Angriffe so lange zurück (Tests). */
    public void hold(int ticks) {
        this.global = Math.max(this.global, ticks);
    }

    // =====================================================================================
    // Schlaf
    // =====================================================================================

    private void sleep(ServerLevel level) {
        this.mob.setTarget(null);
        this.mob.getNavigation().stop();
        if (this.mob.tickCount % 10 != 0) {
            return;
        }
        if (this.mob.getLastHurtByMob() != null && this.mob.getLastHurtByMob().isAlive()) {
            this.wake(level, this.mob.getLastHurtByMob());
            return;
        }
        for (ServerPlayer player : level.players()) {
            if (fighter(player) && player.distanceToSqr(this.mob) < WAKE_RADIUS * WAKE_RADIUS && Math.abs(player.getY() - this.mob.getY()) < 8.0
                    && (player.distanceToSqr(this.mob) < 25.0 || this.mob.hasLineOfSight(player))) {
                this.wake(level, player);
                return;
            }
        }
    }

    /** Er erwacht: Bossleiste, Gebrüll, und wer ihn geweckt hat, ist sein erstes Ziel. */
    public void wake(ServerLevel level, @Nullable LivingEntity cause) {
        if (this.boss.isAwake()) {
            return;
        }
        this.boss.setAwake(true);
        this.bar.setVisible(true);
        this.lonely = 0;
        this.global = 40;
        if (cause != null && this.valid(cause)) {
            this.mob.setTarget(cause);
        }
        this.announce(level, Component.translatable("miniboss.kingdomsatwar.awake", this.mob.getDisplayName()).withStyle(ChatFormatting.GOLD));
        this.boss.onWake(level, cause);
    }

    /** Niemand mehr da: zurück an den Platz, geheilt, die Diener vergehen, er schläft wieder ein. */
    private void giveUp(ServerLevel level) {
        if (this.current != null) {
            this.stopSpecial(level);
        }
        this.mob.setTarget(null);
        this.dismiss(level);
        if (this.home != null && this.mob.blockPosition().distSqr(this.home) > 9) {
            this.mob.teleportTo(this.home.getX() + 0.5, this.home.getY(), this.home.getZ() + 0.5);
        }
        this.mob.setHealth(this.mob.getMaxHealth());
        this.boss.setEnraged(false);
        this.boss.setAwake(false);
        this.bar.setVisible(false);
        this.lonely = 0;
        this.boss.onSleep(level);
    }

    // =====================================================================================
    // Diener
    // =====================================================================================

    public void addMinion(Mob minion) {
        this.minions.add(minion.getUUID());
    }

    public int minionCount(ServerLevel level) {
        int n = 0;
        for (UUID id : this.minions) {
            if (level.getEntity(id) instanceof LivingEntity m && m.isAlive()) {
                n++;
            }
        }
        return n;
    }

    public boolean isMinion(Entity entity) {
        return this.minions.contains(entity.getUUID());
    }

    /** Die Diener vergehen (sein Tod, oder er schläft wieder ein). */
    public void dismiss(ServerLevel level) {
        for (UUID id : this.minions) {
            if (level.getEntity(id) instanceof Mob m && m.isAlive()) {
                level.sendParticles(net.minecraft.core.particles.ParticleTypes.POOF, m.getX(), m.getY(0.5), m.getZ(), 12, 0.3, 0.4, 0.3, 0.02);
                m.discard();
            }
        }
        this.minions.clear();
    }

    // =====================================================================================
    // Werkzeuge
    // =====================================================================================

    /** Ein Spieler, der kämpfen kann (Überleben oder Abenteuer, lebendig)? */
    public static boolean fighter(Player player) {
        return player.isAlive() && !player.isCreative() && !player.isSpectator();
    }

    private boolean fighterNear(ServerLevel level) {
        for (ServerPlayer player : level.players()) {
            if (fighter(player) && player.distanceToSqr(this.mob) < BAR_RADIUS * BAR_RADIUS) {
                return true;
            }
        }
        return false;
    }

    /** Ein gültiges Ziel? */
    public boolean valid(LivingEntity target) {
        return target.isAlive() && !(target instanceof Player p && !fighter(p)) && target.level() == this.mob.level();
    }

    /** Wen seine Angriffe treffen: Spieler, Soldaten, Tiere und Dorfbewohner – keine Monster, nicht seine Diener. */
    public boolean foe(LivingEntity e) {
        if (e == this.mob || !e.isAlive() || e.isSpectator() || this.isMinion(e) || this.mob.isAlliedTo(e)) {
            return false;
        }
        if (e instanceof Player player) {
            return fighter(player);
        }
        return !(e instanceof Enemy);
    }

    public List<LivingEntity> foes(AABB box) {
        return this.mob.level().getEntitiesOfClass(LivingEntity.class, box, this::foe);
    }

    /** Die Gegner im Umkreis (waagerecht, mit Höhenspanne). */
    public List<LivingEntity> foesAround(Vec3 center, double radius, double below, double above) {
        AABB box = new AABB(center.x - radius, center.y - below, center.z - radius, center.x + radius, center.y + above, center.z + radius);
        List<LivingEntity> list = new ArrayList<>();
        for (LivingEntity e : this.foes(box)) {
            double dx = e.getX() - center.x;
            double dz = e.getZ() - center.z;
            if (dx * dx + dz * dz <= (radius + e.getBbWidth() * 0.5) * (radius + e.getBbWidth() * 0.5)) {
                list.add(e);
            }
        }
        return list;
    }

    public boolean hurt(ServerLevel level, LivingEntity victim, float amount) {
        return victim.hurtServer(level, this.mob.damageSources().mobAttack(this.mob), amount);
    }

    /**
     * Die waagerechte Startgeschwindigkeit für einen Sprung über {@code h} Blöcke in {@code ticks} Ticks (der erste Tick
     * am Boden bremst stärker, danach die Luft).
     */
    public static double leap(Mob mob, double h, int ticks) {
        BlockPos below = mob.getBlockPosBelowThatAffectsMyMovement();
        double ground = mob.onGround() ? mob.level().getBlockState(below).getFriction(mob.level(), below, mob) * 0.91 : 0.91;
        double air = (1.0 - Math.pow(0.91, ticks - 1)) / (1.0 - 0.91);
        return h / (1.0 + ground * air);
    }

    public static void knock(LivingEntity victim, Vec3 from, double power, double lift) {
        double resist = Math.max(0.0, 1.0 - victim.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
        Vec3 away = victim.position().subtract(from).multiply(1.0, 0.0, 1.0);
        if (away.lengthSqr() < 1.0e-4) {
            away = new Vec3(1.0, 0.0, 0.0);
        }
        away = away.normalize().scale(power * resist);
        victim.setDeltaMovement(victim.getDeltaMovement().multiply(0.4, 0.0, 0.4).add(away.x, lift * Math.max(0.3, resist), away.z));
        victim.needsSync = true;
    }

    public void sound(SoundEvent sound, float volume, float pitch) {
        this.mob.level().playSound(null, this.mob.getX(), this.mob.getY(), this.mob.getZ(), sound, SoundSource.HOSTILE, volume, pitch);
    }

    public void announce(ServerLevel level, Component message) {
        for (ServerPlayer player : level.players()) {
            if (player.distanceToSqr(this.mob) < BAR_RADIUS * BAR_RADIUS) {
                player.sendOverlayMessage(message);
            }
        }
    }

    /** Steht still, solange ein Angriff es verlangt. */
    private void root(boolean on) {
        AttributeInstance speed = this.mob.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed == null) {
            return;
        }
        speed.removeModifier(ROOT);
        if (on) {
            speed.addTransientModifier(new AttributeModifier(ROOT, -1.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
    }

    // =====================================================================================
    // Bossleiste, Tod, Speichern
    // =====================================================================================

    public void seenBy(ServerPlayer player) {
        this.bar.addPlayer(player);
    }

    public void unseenBy(ServerPlayer player) {
        this.bar.removePlayer(player);
    }

    /** Sein Tod (oder er verschwindet): Leiste weg, Diener vergehen. */
    public void gone(ServerLevel level) {
        if (this.current != null) {
            this.stopSpecial(level);
        }
        this.bar.removeAllPlayers();
        this.dismiss(level);
    }

    public void save(ValueOutput output) {
        if (this.home != null) {
            output.putLong("MiniHome", this.home.asLong());
        }
        output.putBoolean("MiniAwake", this.boss.isAwake());
        output.putBoolean("MiniEnraged", this.boss.isEnraged());
        output.store("MiniMinions", UUIDUtil.CODEC.listOf(), List.copyOf(this.minions));
    }

    public void load(ValueInput input) {
        input.getLong("MiniHome").ifPresent(h -> this.home = BlockPos.of(h));
        this.boss.setAwake(input.getBooleanOr("MiniAwake", false));
        this.boss.setEnraged(input.getBooleanOr("MiniEnraged", false));
        this.bar.setVisible(this.boss.isAwake());
        this.minions.clear();
        input.read("MiniMinions", UUIDUtil.CODEC.listOf()).ifPresent(this.minions::addAll);
    }
}
