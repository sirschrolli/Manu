package io.github.sirschrolli.kingdomsatwar.entity.boss;

import io.github.sirschrolli.kingdomsatwar.entity.boss.lightning.LightningAttacks;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/**
 * Die Arena wehrt sich mit: Ab der zweiten Kampfphase schlagen in regelmäßigen Abständen Gefahren des Elements bei den
 * Gegnern ein – Meteore, Eiszapfen, Blitze, Dornensaat, Felsbrocken oder Wasserfontänen. Jeder Einschlag kündigt sich
 * zwei Sekunden vorher mit einem leuchtenden Kreis am Boden an; wer aufpasst, kann ausweichen. In der letzten Phase
 * kommen sie doppelt so oft und zu zweit.
 */
final class BossHazards {
    /** Vorwarnzeit in Ticks. */
    static final int FUSE = 34;

    private record Strike(Vec3 pos, int fuse) {}

    private final List<Strike> pending = new ArrayList<>();
    private int timer = 80;

    void tick(ServerLevel level, DragonBossEntity boss) {
        int phase = boss.getPhase();
        if (phase < 2) {
            this.pending.clear();
            return;
        }
        if (--this.timer <= 0) {
            float rage = boss.rage();
            this.timer = Math.round((phase >= 3 ? 70 : 130) * (1.15f - 0.35f * rage)) + boss.getRandom().nextInt(30);
            int count = phase >= 3 ? 2 : 1;
            List<LivingEntity> enemies = boss.enemiesAround(boss.getHome() != null ? Vec3.atCenterOf(boss.getHome()) : boss.position(),
                    DragonBossEntity.ARENA_RADIUS, 24);
            for (int i = 0; i < count && !enemies.isEmpty(); i++) {
                LivingEntity e = enemies.get(boss.getRandom().nextInt(enemies.size()));
                // etwas vor das Ziel (in Laufrichtung), damit Stehenbleiben nicht hilft
                Vec3 lead = e.getDeltaMovement().multiply(8, 0, 8);
                Vec3 at = e.position().add(lead).add((boss.getRandom().nextDouble() - 0.5) * 3, 0, (boss.getRandom().nextDouble() - 0.5) * 3);
                double ground = boss.groundY(at.x, at.z);
                if (Math.abs(ground - e.getY()) < 6) {
                    at = new Vec3(at.x, ground, at.z);
                }
                this.pending.add(new Strike(at, FUSE));
            }
        }
        List<Strike> next = new ArrayList<>();
        for (Strike strike : this.pending) {
            int fuse = strike.fuse() - 1;
            this.telegraph(level, boss, strike.pos(), fuse);
            if (fuse == fallTime(boss)) {
                this.launch(level, boss, strike.pos());
            }
            if (fuse <= 0) {
                this.strike(level, boss, strike.pos());
            } else {
                next.add(new Strike(strike.pos(), fuse));
            }
        }
        this.pending.clear();
        this.pending.addAll(next);
    }

    /** Ticks vor dem Einschlag, zu denen ein fallendes Geschoss losfliegt (0 = keins). */
    private static int fallTime(DragonBossEntity boss) {
        return switch (boss.element()) {
            case FIRE, ICE, EARTH, NATURE -> 16;
            default -> -1;
        };
    }

    private static ParticleOptions particle(DragonBossEntity boss) {
        return boss.element().particle();
    }

    /** Leuchtender, schrumpfender Kreis am Boden, dazu Funken in der Mitte. */
    private void telegraph(ServerLevel level, DragonBossEntity boss, Vec3 pos, int fuse) {
        if (fuse % 2 == 0) {
            double radius = 1.2 + 2.0 * fuse / (double) FUSE;
            BossFx.ring(level, particle(boss), pos.add(0, 0.15, 0), radius, 14, 0.0);
        }
        if (fuse % 6 == 0) {
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK, pos.x, pos.y + 0.3, pos.z, 3, 0.3, 0.1, 0.3, 0.02);
        }
        if (fuse == FUSE - 1) {
            BossFx.sound(level, pos, SoundEvents.NOTE_BLOCK_BASEDRUM.value(), 1.5f, 0.5f);
        }
    }

    /** Fallende Gefahren: Meteor, Eiszapfen, Felsbrocken oder Dornensaat stürzen vom Himmel auf die Markierung. */
    private void launch(ServerLevel level, DragonBossEntity boss, Vec3 pos) {
        BossProjectile.Kind kind = switch (boss.element()) {
            case FIRE -> BossProjectile.Kind.METEOR;
            case ICE -> BossProjectile.Kind.ICICLE;
            case EARTH -> BossProjectile.Kind.BOULDER;
            default -> BossProjectile.Kind.SEED;
        };
        Vec3 from = pos.add((boss.getRandom().nextDouble() - 0.5) * 4, 24, (boss.getRandom().nextDouble() - 0.5) * 4);
        BossProjectile.shoot(level, boss, kind, from, pos.subtract(from), 1.5, 7.0f);
    }

    /** Was im Moment des Einschlags geschieht (bei fallenden Gefahren erledigt das Geschoss den Rest). */
    private void strike(ServerLevel level, DragonBossEntity boss, Vec3 pos) {
        switch (boss.element()) {
            case LIGHTNING -> {
                LightningAttacks.visualBolt(level, pos);
                for (LivingEntity e : boss.enemiesAround(pos, 2.8, 4)) {
                    boss.hurtTarget(level, e, 7.0f);
                    BossEffects.stun(e, 1);
                }
            }
            case WATER -> {
                BossFx.sound(level, pos, SoundEvents.GENERIC_SPLASH, 2.5f, 0.6f);
                for (int k = 0; k < 8; k++) {
                    level.sendParticles(ParticleTypes.SPLASH, pos.x, pos.y + k * 0.8, pos.z, 12, 0.5, 0.2, 0.5, 0.05);
                    level.sendParticles(ParticleTypes.BUBBLE_COLUMN_UP, pos.x, pos.y + k * 0.8, pos.z, 6, 0.4, 0.2, 0.4, 0.1);
                }
                for (LivingEntity e : boss.enemiesAround(pos, 2.6, 4)) {
                    boss.hurtTarget(level, e, 6.0f);
                    e.push(0, 1.1, 0);
                    e.needsSync = true;
                    BossEffects.soak(e, 4);
                }
            }
            default -> {
            }
        }
    }
}
