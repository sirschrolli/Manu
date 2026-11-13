package io.github.sirschrolli.kingdomsatwar.entity.boss.lightning;

import io.github.sirschrolli.kingdomsatwar.entity.boss.BossAnims;
import io.github.sirschrolli.kingdomsatwar.entity.boss.BossAttack;
import io.github.sirschrolli.kingdomsatwar.entity.boss.BossEffects;
import io.github.sirschrolli.kingdomsatwar.entity.boss.BossFx;
import io.github.sirschrolli.kingdomsatwar.entity.boss.DragonBossEntity;
import io.github.sirschrolli.kingdomsatwar.registry.ModParticles;
import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/** Voltarions eigene große Angriffe: Kugelblitze, die ihre Gegner jagen, und der Blitzkäfig. */
public final class StormAttacks {
    private StormAttacks() {}

    /** Glühender Kern eines Kugelblitzes. */
    private static final DustParticleOptions GLOW = new DustParticleOptions(0xFFF6A8, 1.6f);
    /** Violettes Leuchten der Käfigsäulen. */
    private static final DustParticleOptions PYLON = new DustParticleOptions(0xB98CFF, 1.4f);

    // =====================================================================================
    // Kugelblitze
    // =====================================================================================

    /**
     * Kugelblitze: Voltarion speit knisternde Kugeln aus gebündeltem Blitz, drei bis fünf, die langsam auf ihre Gegner
     * zutreiben und ihnen nachstellen. Berührt eine Kugel einen Gegner – oder ist ihre Zeit um –, schlägt an ihrer Stelle ein
     * Blitz ein: Schaden ringsum und kurz betäubt. Wer läuft und Haken schlägt, lässt sie ins Leere platzen.
     */
    public static final class BallLightning extends BossAttack {
        private static final double SPEED = 0.2;
        private static final double TOUCH = 1.4;
        private final float damage;
        private final List<Orb> orbs = new ArrayList<>();

        private static final class Orb {
            Vec3 pos;
            Vec3 velocity;
            @Nullable UUID target;
            final int born;
            boolean done;

            Orb(Vec3 pos, Vec3 velocity, @Nullable UUID target, int born) {
                this.pos = pos;
                this.velocity = velocity;
                this.target = target;
                this.born = born;
            }
        }

        public BallLightning(float damage) {
            super(BossAnims.VOLLEY, "ball_lightning");
            this.damage = damage;
        }

        @Override
        public int duration() {
            return 170;
        }

        @Override
        public int cooldown() {
            return 360;
        }

        @Override
        public int turnUntil() {
            return 12;
        }

        @Override
        public boolean stopsMovement() {
            return true;
        }

        @Override
        public float weight(DragonBossEntity boss, LivingEntity target, double distance, float angle) {
            return distance > 7.0 ? 6 : 2;
        }

        @Override
        public void start(ServerLevel level, DragonBossEntity boss, LivingEntity target) {
            this.orbs.clear();
        }

        @Override
        public void tick(ServerLevel level, DragonBossEntity boss, LivingEntity target, int tick) {
            int count = 2 + boss.getPhase();
            // ausspeien: alle vier Ticks eine Kugel, fächerförmig nach vorn
            if (tick >= 12 && tick < 12 + count * 4 && (tick - 12) % 4 == 0) {
                int i = (tick - 12) / 4;
                List<LivingEntity> enemies = boss.enemiesAround(BossFx.arena(boss), DragonBossEntity.ARENA_RADIUS + 6.0, 40);
                LivingEntity chase = enemies.isEmpty() ? target : enemies.get(i % enemies.size());
                Vec3 mouth = boss.mouthPosition();
                double spread = (i - (count - 1) / 2.0) * 0.45;
                Vec3 fwd = boss.forward();
                Vec3 dir = new Vec3(fwd.x * Math.cos(spread) - fwd.z * Math.sin(spread), 0.15, fwd.z * Math.cos(spread) + fwd.x * Math.sin(spread));
                this.orbs.add(new Orb(mouth, dir.normalize().scale(0.45), chase.getUUID(), tick));
                BossFx.sound(level, mouth, ModSounds.DRAGON_BREATH.get(io.github.sirschrolli.kingdomsatwar.dragon.DragonType.LIGHTNING).get(),
                        3.0f, 1.3f);
                BossFx.sound(level, mouth, SoundEvents.FIREWORK_ROCKET_LAUNCH, 3.0f, 0.6f);
            }
            List<LivingEntity> enemies = boss.enemiesAround(BossFx.arena(boss), DragonBossEntity.ARENA_RADIUS + 6.0, 40);
            for (Orb orb : this.orbs) {
                if (orb.done) {
                    continue;
                }
                LivingEntity chase = null;
                for (LivingEntity e : enemies) {
                    if (e.getUUID().equals(orb.target)) {
                        chase = e;
                    }
                }
                if (chase == null && !enemies.isEmpty()) {
                    chase = enemies.get(0);
                    orb.target = chase.getUUID();
                }
                if (chase != null) {
                    // langsam nachsteuern (weich, damit Haken schlagen hilft), auf Brusthöhe schweben
                    Vec3 goal = chase.position().add(0.0, chase.getBbHeight() * 0.6 + Math.sin((tick + orb.born) * 0.3) * 0.3, 0.0);
                    Vec3 want = goal.subtract(orb.pos);
                    if (want.lengthSqr() > 1.0e-4) {
                        want = want.normalize().scale(SPEED);
                    }
                    orb.velocity = orb.velocity.scale(0.9).add(want.scale(0.1));
                }
                orb.pos = orb.pos.add(orb.velocity);
                // knistern
                level.sendParticles(GLOW, orb.pos.x, orb.pos.y, orb.pos.z, 2, 0.12, 0.12, 0.12, 0.0);
                BossFx.sphere(level, ParticleTypes.ELECTRIC_SPARK, orb.pos, 0.7, 6, 0.02);
                level.sendParticles(ModParticles.BOSS_SPARK.get(), orb.pos.x, orb.pos.y, orb.pos.z, 1, 0.2, 0.2, 0.2, 0.0);
                if ((tick + orb.born) % 9 == 0) {
                    Vec3 floor = BossFx.ground(boss, orb.pos.x + (boss.getRandom().nextDouble() - 0.5) * 3.0,
                            orb.pos.z + (boss.getRandom().nextDouble() - 0.5) * 3.0);
                    LightningAttacks.arc(level, orb.pos, floor);
                    BossFx.sound(level, orb.pos, SoundEvents.FIREWORK_ROCKET_TWINKLE, 1.2f, 1.6f);
                }
                boolean touched = chase != null && chase.position().add(0.0, chase.getBbHeight() * 0.5, 0.0).distanceTo(orb.pos) < TOUCH;
                boolean expired = tick - orb.born > 130 || tick >= this.duration() - 2;
                if (touched || expired) {
                    this.burst(level, boss, orb);
                }
            }
        }

        private void burst(ServerLevel level, DragonBossEntity boss, Orb orb) {
            orb.done = true;
            Vec3 floor = BossFx.ground(boss, orb.pos.x, orb.pos.z);
            LightningAttacks.visualBolt(level, floor);
            BossFx.burst(level, ParticleTypes.ELECTRIC_SPARK, orb.pos, 40, 1.0, 0.4);
            BossFx.burst(level, ModParticles.BOSS_SPARK.get(), orb.pos, 12, 0.8, 0.1);
            for (LivingEntity e : boss.enemiesAround(orb.pos, 2.8, 3.0)) {
                boss.hurtTarget(level, e, this.damage);
                BossEffects.stun(e, 1);
                DragonBossEntity.pushAway(e, orb.pos, 0.6, 0.3);
            }
        }

        @Override
        public void stop(ServerLevel level, DragonBossEntity boss) {
            this.orbs.clear();
        }
    }

    // =====================================================================================
    // Blitzkäfig
    // =====================================================================================

    /**
     * Blitzkäfig: Voltarion schlägt mit den Pranken auf – um bis zu zwei Gegner (in der letzten Phase drei) fahren sieben
     * Blitze in den Boden und bleiben als violett glühende Säulen stehen, zwischen denen knisternde Bögen einen Käfig
     * spannen. Eine Lücke bleibt offen. Wer den Bögen zu nahe kommt, bekommt einen Schlag; wer nach zwei Sekunden noch im
     * Käfig steht, den trifft der große Blitz in der Mitte.
     */
    public static final class StormCage extends BossAttack {
        private static final int POSTS = 8;
        private static final double RADIUS = 4.5;
        private static final int STRIKE = 72;
        private final float damage;
        private final List<Cage> cages = new ArrayList<>();
        private final Map<UUID, Integer> zapped = new HashMap<>();

        private record Cage(Vec3 center, List<Vec3> posts, int gap) {}

        public StormCage(float damage) {
            super(BossAnims.ERUPT, "storm_cage");
            this.damage = damage;
        }

        @Override
        public int duration() {
            return STRIKE + 20;
        }

        @Override
        public int cooldown() {
            return 380;
        }

        @Override
        public int minPhase() {
            return 2;
        }

        @Override
        public float weight(DragonBossEntity boss, LivingEntity target, double distance, float angle) {
            return 5;
        }

        @Override
        public void start(ServerLevel level, DragonBossEntity boss, LivingEntity target) {
            this.cages.clear();
            this.zapped.clear();
        }

        @Override
        public void tick(ServerLevel level, DragonBossEntity boss, LivingEntity target, int tick) {
            if (tick == 12) {
                int limit = boss.getPhase() >= 3 ? 3 : 2;
                List<LivingEntity> caught = new ArrayList<>();
                caught.add(target);
                for (LivingEntity e : boss.enemiesAround(BossFx.arena(boss), DragonBossEntity.ARENA_RADIUS + 6.0, 40)) {
                    // zwei Käfige nicht übereinander: nur wer weit genug von den anderen steht
                    if (caught.size() < limit && caught.stream().noneMatch(c -> c == e || c.distanceTo(e) < RADIUS * 2.0)) {
                        caught.add(e);
                    }
                }
                for (LivingEntity e : caught) {
                    Vec3 c = BossFx.ground(boss, e.getX(), e.getZ());
                    // die Lücke liegt auf der dem Drachen abgewandten Seite – man flieht von ihm weg
                    double away = Math.atan2(c.z - boss.getZ(), c.x - boss.getX());
                    int gap = Math.floorMod((int) Math.round(away / (Mth.TWO_PI / POSTS)), POSTS);
                    List<Vec3> posts = new ArrayList<>();
                    for (int i = 0; i < POSTS; i++) {
                        double a = i * Mth.TWO_PI / POSTS;
                        posts.add(BossFx.ground(boss, c.x + Math.cos(a) * RADIUS, c.z + Math.sin(a) * RADIUS));
                    }
                    this.cages.add(new Cage(c, posts, gap));
                }
                BossFx.sound(level, boss.position(), SoundEvents.LIGHTNING_BOLT_THUNDER, 6.0f, 0.7f);
            }
            if (this.cages.isEmpty()) {
                return;
            }
            // die Säulen schlagen nacheinander ein
            if (tick >= 12 && tick < 12 + POSTS) {
                int i = tick - 12;
                for (Cage cage : this.cages) {
                    if (i != cage.gap()) {
                        LightningAttacks.visualBolt(level, cage.posts().get(i));
                    }
                }
            }
            if (tick >= 12 && tick < STRIKE) {
                for (Cage cage : this.cages) {
                    for (int i = 0; i < POSTS; i++) {
                        if (i == cage.gap()) {
                            continue;
                        }
                        Vec3 p = cage.posts().get(i);
                        if (tick % 2 == 0) {
                            for (double h = 0.2; h < 3.2; h += 0.5) {
                                level.sendParticles(PYLON, p.x, p.y + h, p.z, 1, 0.08, 0.05, 0.08, 0.0);
                            }
                            level.sendParticles(ParticleTypes.ELECTRIC_SPARK, p.x, p.y + 3.2, p.z, 2, 0.15, 0.15, 0.15, 0.05);
                        }
                        int next = (i + 1) % POSTS;
                        if (next != cage.gap() && tick >= 12 + POSTS && tick % 3 == i % 3) {
                            Vec3 a = p.add(0.0, 1.0 + (tick % 2) * 0.8, 0.0);
                            Vec3 b = cage.posts().get(next).add(0.0, 1.0 + ((tick + 1) % 2) * 0.8, 0.0);
                            LightningAttacks.arc(level, a, b);
                        }
                    }
                    // Warnung in der Mitte: ein glühender Fleck, der wächst
                    float charge = Mth.clamp((tick - 20) / (float) (STRIKE - 20), 0.0f, 1.0f);
                    if (tick % 3 == 0 && charge > 0.0f) {
                        BossFx.warnDisc(level, ModParticles.BOSS_SPARK.get(), cage.center(), 0.6 + charge * (RADIUS - 1.0), 8 + (int) (charge * 14));
                    }
                }
                if (tick % 10 == 0) {
                    BossFx.sound(level, this.cages.get(0).center(), SoundEvents.BEACON_AMBIENT, 2.5f, 1.9f);
                }
                // die Bögen schlagen: wer ihnen zu nahe kommt
                if (tick >= 12 + POSTS) {
                    for (LivingEntity e : boss.enemiesAround(BossFx.arena(boss), DragonBossEntity.ARENA_RADIUS + 6.0, 40)) {
                        for (Cage cage : this.cages) {
                            if (this.touchesFence(cage, e.position())) {
                                Integer last = this.zapped.get(e.getUUID());
                                if (last == null || tick - last >= 10) {
                                    this.zapped.put(e.getUUID(), tick);
                                    boss.hurtTarget(level, e, this.damage * 0.35f);
                                    // zurück in den Käfig gestoßen
                                    Vec3 in = cage.center().subtract(e.position()).multiply(1.0, 0.0, 1.0);
                                    if (in.lengthSqr() > 1.0e-4) {
                                        in = in.normalize();
                                        e.push(in.x * 0.5, 0.2, in.z * 0.5);
                                        e.needsSync = true;
                                    }
                                    BossFx.burst(level, ParticleTypes.ELECTRIC_SPARK, e.position().add(0, 1, 0), 12, 0.4, 0.2);
                                }
                            }
                        }
                    }
                }
            }
            if (tick == STRIKE) {
                for (Cage cage : this.cages) {
                    LightningAttacks.visualBolt(level, cage.center());
                    BossFx.burst(level, ParticleTypes.ELECTRIC_SPARK, cage.center().add(0, 1, 0), 60, 2.0, 0.5);
                    BossFx.ring(level, ModParticles.BOSS_SPARK.get(), cage.center(), RADIUS, 32, 0.3);
                    for (LivingEntity e : boss.enemiesAround(cage.center(), RADIUS - 0.3, 4.0)) {
                        boss.hurtTarget(level, e, this.damage);
                        BossEffects.stun(e, 2);
                    }
                }
                BossFx.sound(level, this.cages.get(0).center(), SoundEvents.LIGHTNING_BOLT_IMPACT, 6.0f, 0.6f);
            }
        }

        /** Steht der Punkt nah an einem der Bögen (zwischen zwei Säulen, ohne die Lücke)? */
        private boolean touchesFence(Cage cage, Vec3 pos) {
            for (int i = 0; i < POSTS; i++) {
                int next = (i + 1) % POSTS;
                if (i == cage.gap() || next == cage.gap()) {
                    continue;
                }
                Vec3 a = cage.posts().get(i);
                Vec3 b = cage.posts().get(next);
                Vec3 flat = new Vec3(pos.x, a.y, pos.z);
                if (BossFx.distanceToSegment(flat, a, new Vec3(b.x, a.y, b.z)) < 0.7 && pos.y - a.y < 3.0 && pos.y - a.y > -1.5) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public void stop(ServerLevel level, DragonBossEntity boss) {
            this.cages.clear();
            this.zapped.clear();
        }
    }
}
