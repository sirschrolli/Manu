package io.github.sirschrolli.kingdomsatwar.entity.boss.ice;

import io.github.sirschrolli.kingdomsatwar.entity.boss.BossAnims;
import io.github.sirschrolli.kingdomsatwar.entity.boss.BossAttack;
import io.github.sirschrolli.kingdomsatwar.entity.boss.BossEffects;
import io.github.sirschrolli.kingdomsatwar.entity.boss.BossFx;
import io.github.sirschrolli.kingdomsatwar.entity.boss.BossProjectile;
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
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/** Glaciaras eigene große Angriffe: die schwenkende Frostlanze und der Splittersturm. */
public final class FrostAttacks {
    private FrostAttacks() {}

    /** Kern der Frostlanze: gleißend weiß-blau. */
    private static final DustParticleOptions CORE = new DustParticleOptions(0xE8FBFF, 1.3f);
    /** Glitzern um den Strahl. */
    private static final DustParticleOptions GLINT = new DustParticleOptions(0x8FDcff, 0.8f);
    /** Die schwebenden Eiskristalle des Splittersturms. */
    private static final DustParticleOptions CRYSTAL = new DustParticleOptions(0xB8F0FF, 1.3f);
    /** Raureif am Boden. */
    private static final DustParticleOptions RIME = new DustParticleOptions(0xA8E4FF, 1.1f);

    // =====================================================================================
    // Frostlanze
    // =====================================================================================

    /**
     * Frostlanze: Glaciara zieht die Kälte im Maul zusammen (Eiskristalle strömen zu ihr), dann fährt ein gleißender Strahl
     * aus ihrem Maul auf den Boden und schwenkt in einem weiten Bogen über das Nest. Wer getroffen wird, erstarrt; wo der
     * Strahl entlangfuhr, liegt Raureif, auf dem man langsamer wird.
     */
    public static final class FrostBeam extends BossAttack {
        private static final int CHARGE = 22;
        private static final int SWEEP = 44;
        private static final double RANGE = 26.0;
        private final float damage;
        private final List<Vec3> rime = new ArrayList<>();
        private final Map<UUID, Integer> lastHit = new HashMap<>();
        private double centerAngle;
        private double reach = RANGE;
        private int direction = 1;

        public FrostBeam(float damage) {
            super(BossAnims.BREATH, "frost_beam");
            this.damage = damage;
        }

        @Override
        public int duration() {
            return CHARGE + SWEEP + 24;
        }

        @Override
        public int cooldown() {
            return 320;
        }

        @Override
        public int minPhase() {
            return 2;
        }

        @Override
        public int turnUntil() {
            return CHARGE;
        }

        @Override
        public float weight(DragonBossEntity boss, LivingEntity target, double distance, float angle) {
            return distance > 6.0 && distance < RANGE && angle < 60.0f ? 7 : 1;
        }

        @Override
        public void start(ServerLevel level, DragonBossEntity boss, LivingEntity target) {
            this.rime.clear();
            this.lastHit.clear();
            this.direction = boss.getRandom().nextBoolean() ? 1 : -1;
        }

        @Override
        public void tick(ServerLevel level, DragonBossEntity boss, LivingEntity target, int tick) {
            Vec3 mouth = boss.mouthPosition();
            if (tick < CHARGE) {
                // Kälte sammelt sich: Kristalle fliegen von allen Seiten ins Maul
                for (int i = 0; i < 6; i++) {
                    double a = boss.getRandom().nextDouble() * Mth.TWO_PI;
                    double u = boss.getRandom().nextDouble() * 2.0 - 1.0;
                    double r = 3.5;
                    Vec3 from = mouth.add(Math.cos(a) * r * Math.sqrt(1 - u * u), u * r, Math.sin(a) * r * Math.sqrt(1 - u * u));
                    Vec3 v = mouth.subtract(from);
                    level.sendParticles(ModParticles.BOSS_FROST.get(), from.x, from.y, from.z, 0, v.x, v.y, v.z, 0.16);
                }
                level.sendParticles(ParticleTypes.END_ROD, mouth.x, mouth.y, mouth.z, 2, 0.2, 0.2, 0.2, 0.01);
                if (tick % 6 == 0) {
                    BossFx.sound(level, mouth, SoundEvents.AMETHYST_BLOCK_RESONATE, 3.0f, 0.6f + tick * 0.03f);
                }
                if (tick == CHARGE - 1) {
                    this.centerAngle = Math.atan2(target.getZ() - boss.getZ(), target.getX() - boss.getX());
                    // der Strahl fegt in der Entfernung des Ziels über den Boden
                    this.reach = Mth.clamp(Math.sqrt(boss.distanceToSqr(target.getX(), boss.getY(), target.getZ())), 8.0, RANGE);
                }
                return;
            }
            int t = tick - CHARGE;
            if (t < SWEEP) {
                // der Strahl schwenkt von −55° bis +55° um das Ziel
                double sweep = Math.toRadians(55.0) * (2.0 * t / (SWEEP - 1.0) - 1.0) * this.direction;
                double a = this.centerAngle + sweep;
                Vec3 far = new Vec3(boss.getX() + Math.cos(a) * this.reach, 0.0, boss.getZ() + Math.sin(a) * this.reach);
                Vec3 end = this.landing(boss, mouth, far);
                // Strahl: heller Kern, Frostteilchen drumherum
                BossFx.line(level, CORE, mouth, end, 0.35, 0.03);
                BossFx.line(level, GLINT, mouth, end, 0.6, 0.12);
                BossFx.line(level, ParticleTypes.SNOWFLAKE, mouth, end, 1.2, 0.2);
                level.sendParticles(ParticleTypes.SNOWFLAKE, end.x, end.y + 0.3, end.z, 10, 0.6, 0.3, 0.6, 0.08);
                level.sendParticles(ParticleTypes.END_ROD, end.x, end.y + 0.2, end.z, 3, 0.3, 0.1, 0.3, 0.03);
                if (t % 3 == 0) {
                    level.sendParticles(ModParticles.BOSS_FROST.get(), end.x, end.y + 0.3, end.z, 1, 0.3, 0.1, 0.3, 0.0);
                }
                if (t % 2 == 0) {
                    this.rime.add(end);
                }
                if (t % 5 == 0) {
                    BossFx.sound(level, end, SoundEvents.GLASS_BREAK, 2.0f, 0.5f + boss.getRandom().nextFloat() * 0.3f);
                }
                if (t == 0) {
                    BossFx.sound(level, mouth, ModSounds.DRAGON_BREATH.get(io.github.sirschrolli.kingdomsatwar.dragon.DragonType.ICE).get(),
                            5.0f, 0.6f);
                }
                for (LivingEntity e : boss.enemiesAround(boss.position(), RANGE + 4.0, 30.0)) {
                    Vec3 body = e.position().add(0.0, e.getBbHeight() * 0.5, 0.0);
                    boolean atEnd = e.position().subtract(end).horizontalDistanceSqr() < 2.2 * 2.2 && Math.abs(e.getY() - end.y) < 2.5;
                    if (!atEnd && BossFx.distanceToSegment(body, mouth, end) > 1.5) {
                        continue;
                    }
                    Integer last = this.lastHit.get(e.getUUID());
                    if (last == null || tick - last >= 6) {
                        this.lastHit.put(e.getUUID(), tick);
                        boss.hurtTarget(level, e, this.damage);
                        BossEffects.freeze(e, 3);
                    }
                }
            }
            // Raureif: glitzert und macht langsam, bis der Angriff vorbei ist
            if (tick % 4 == 0) {
                for (Vec3 p : this.rime) {
                    level.sendParticles(RIME, p.x, p.y + 0.1, p.z, 2, 0.6, 0.02, 0.6, 0.0);
                    if (boss.getRandom().nextInt(3) == 0) {
                        level.sendParticles(ParticleTypes.SNOWFLAKE, p.x, p.y + 0.2, p.z, 1, 0.5, 0.05, 0.5, 0.01);
                    }
                }
                for (LivingEntity e : boss.enemiesAround(boss.position(), RANGE + 4.0, 30.0)) {
                    for (Vec3 p : this.rime) {
                        if (e.position().distanceToSqr(p) < 1.6 * 1.6) {
                            e.addEffect(BossEffects.debuff(MobEffects.SLOWNESS, 30, 1), boss);
                            break;
                        }
                    }
                }
            }
        }

        /** Wo der Strahl auf den Boden trifft: entlang der Linie vom Maul zum fernen Punkt, dort wo der Boden ist. */
        private Vec3 landing(DragonBossEntity boss, Vec3 mouth, Vec3 far) {
            Vec3 ground = BossFx.ground(boss, far.x, far.z);
            // nicht weiter als bis an den Rand der Arena
            return BossFx.keepInArena(boss, ground, DragonBossEntity.ARENA_RADIUS);
        }

        @Override
        public void stop(ServerLevel level, DragonBossEntity boss) {
            this.rime.clear();
            this.lastHit.clear();
        }
    }

    // =====================================================================================
    // Splittersturm
    // =====================================================================================

    /**
     * Splittersturm: Um Glaciara bildet sich ein Kranz aus zwölf schwebenden Eiskristallen, der sich immer schneller dreht –
     * dann schießen die Kristalle als Eisspeere nach allen Seiten davon, eine zweite Welle versetzt in die Lücken, in der
     * letzten Phase eine dritte genau auf jeden Gegner. Wer die Lücken findet, bleibt heil.
     */
    public static final class ShardNova extends BossAttack {
        private static final int SHARDS = 12;
        private final float damage;
        private double spin;

        public ShardNova(float damage) {
            super(BossAnims.RING, "shard_nova");
            this.damage = damage;
        }

        @Override
        public int duration() {
            return 76;
        }

        @Override
        public int cooldown() {
            return 260;
        }

        @Override
        public float weight(DragonBossEntity boss, LivingEntity target, double distance, float angle) {
            return distance < 20.0 ? 6 : 3;
        }

        @Override
        public void start(ServerLevel level, DragonBossEntity boss, LivingEntity target) {
            this.spin = boss.getRandom().nextDouble() * Mth.TWO_PI;
        }

        /** Wo die Kristalle kreisen: Radius und Höhe über den Füßen des Drachen. */
        private Vec3 shard(DragonBossEntity boss, int i, double offset, double radius) {
            double a = this.spin + offset + i * Mth.TWO_PI / SHARDS;
            return new Vec3(boss.getX() + Math.cos(a) * radius, boss.getY() + 1.4, boss.getZ() + Math.sin(a) * radius);
        }

        @Override
        public void tick(ServerLevel level, DragonBossEntity boss, LivingEntity target, int tick) {
            double radius = 4.5 + Math.min(tick, 24) * 0.12;
            if (tick <= 26) {
                this.spin += 0.04 + tick * 0.006;
                for (int i = 0; i < SHARDS; i++) {
                    Vec3 p = this.shard(boss, i, 0.0, radius);
                    // ein Kristall: eine kleine Säule aus hellem Eis mit glitzernder Spitze
                    for (double h = -0.45; h <= 0.45; h += 0.3) {
                        level.sendParticles(CRYSTAL, p.x, p.y + h, p.z, 1, 0.03, 0.03, 0.03, 0.0);
                    }
                    level.sendParticles(ParticleTypes.END_ROD, p.x, p.y + 0.6, p.z, 1, 0.02, 0.02, 0.02, 0.0);
                    if (tick % 3 == 0) {
                        level.sendParticles(ParticleTypes.SNOWFLAKE, p.x, p.y, p.z, 1, 0.2, 0.2, 0.2, 0.01);
                    }
                }
                if (tick % 6 == 0) {
                    BossFx.sound(level, boss.position(), SoundEvents.AMETHYST_CLUSTER_PLACE, 3.0f, 0.7f + tick * 0.03f);
                }
            }
            if (tick == 28) {
                this.wave(level, boss, radius, 0.0);
            }
            if (tick == 42 && boss.getPhase() >= 2) {
                this.wave(level, boss, radius, Mth.PI / SHARDS);
            }
            if (tick == 56 && boss.getPhase() >= 3) {
                // die dritte Welle zielt
                for (LivingEntity e : boss.enemiesAround(boss.position(), 34.0, 20.0)) {
                    Vec3 from = new Vec3(boss.getX(), boss.getY() + 1.4, boss.getZ());
                    Vec3 dir = e.position().add(0.0, e.getBbHeight() * 0.5, 0.0).subtract(from);
                    from = from.add(dir.normalize().scale(radius));
                    BossProjectile.shoot(level, boss, BossProjectile.Kind.ICE_SPEAR, from, dir.normalize(), 1.15, this.damage);
                }
                BossFx.sound(level, boss.position(), SoundEvents.GLASS_BREAK, 4.0f, 0.7f);
            }
        }

        private void wave(ServerLevel level, DragonBossEntity boss, double radius, double offset) {
            for (int i = 0; i < SHARDS; i++) {
                Vec3 p = this.shard(boss, i, offset, radius);
                Vec3 dir = new Vec3(p.x - boss.getX(), 0.0, p.z - boss.getZ()).normalize();
                BossProjectile.shoot(level, boss, BossProjectile.Kind.ICE_SPEAR, p, dir, 0.95, this.damage);
                level.sendParticles(ParticleTypes.SNOWFLAKE, p.x, p.y, p.z, 4, 0.2, 0.2, 0.2, 0.05);
            }
            BossFx.sound(level, boss.position(), SoundEvents.GLASS_BREAK, 4.0f, 0.55f);
            BossFx.sound(level, boss.position(), SoundEvents.TRIDENT_THROW.value(), 4.0f, 0.6f);
        }
    }
}
