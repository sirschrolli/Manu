package io.github.sirschrolli.kingdomsatwar.entity.boss.ice;

import io.github.sirschrolli.kingdomsatwar.entity.boss.BossAnims;
import io.github.sirschrolli.kingdomsatwar.entity.boss.BossAttack;
import io.github.sirschrolli.kingdomsatwar.entity.boss.BossEffects;
import io.github.sirschrolli.kingdomsatwar.entity.boss.BossFx;
import io.github.sirschrolli.kingdomsatwar.entity.boss.BossProjectile;
import io.github.sirschrolli.kingdomsatwar.entity.boss.DragonBossEntity;
import io.github.sirschrolli.kingdomsatwar.registry.ModBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.ModParticles;
import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import io.github.sirschrolli.kingdomsatwar.world.TemporaryBlocks;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Glaciaras eigene Angriffe: Eiszapfenregen, Eisspeere aus dem Boden, Schneesturm und Eisgefängnis.
 */
public final class IceAttacks {
    private IceAttacks() {}

    /** Eiszapfenregen: Glaciara steigt auf und ruft einen Schauer aus Eiszapfen herab (Frost am Boden warnt). */
    public static final class IcicleRain extends BossAttack {
        private final float damage;

        public IcicleRain(float damage) {
            super(BossAnims.RAIN, "icicle_rain");
            this.damage = damage;
        }

        @Override
        public int duration() {
            return 110;
        }

        @Override
        public int cooldown() {
            return 300;
        }

        @Override
        public int minPhase() {
            return 2;
        }

        @Override
        public Where where() {
            return Where.AIR;
        }

        @Override
        public float weight(DragonBossEntity boss, LivingEntity target, double distance, float angle) {
            return 5;
        }

        @Override
        public void tick(ServerLevel level, DragonBossEntity boss, LivingEntity target, int tick) {
            if (tick == 8) {
                BossFx.sound(level, boss.position(), ModSounds.DRAGON_ROAR.get(), 6.0f, 0.55f);
                BossFx.sound(level, boss.position(), SoundEvents.AMETHYST_BLOCK_RESONATE, 4.0f, 0.5f);
                boss.shake(level, 70, 0.3f, 25);
            }
            if (tick < 18 || tick > 98 || tick % 4 != 0) {
                return;
            }
            Vec3 center = boss.getHome() != null ? Vec3.atCenterOf(boss.getHome()) : boss.position();
            List<LivingEntity> enemies = boss.enemiesAround(center, DragonBossEntity.ARENA_RADIUS, 40);
            Vec3 spot;
            if (!enemies.isEmpty() && boss.getRandom().nextFloat() < 0.75f) {
                LivingEntity victim = enemies.get(boss.getRandom().nextInt(enemies.size()));
                spot = victim.position().add((boss.getRandom().nextDouble() - 0.5) * 6, 0, (boss.getRandom().nextDouble() - 0.5) * 6);
            } else {
                double a = boss.getRandom().nextDouble() * Mth.TWO_PI;
                double r = 4 + boss.getRandom().nextDouble() * 24;
                spot = center.add(Math.cos(a) * r, 0, Math.sin(a) * r);
            }
            Vec3 impact = new Vec3(spot.x, boss.groundY(spot.x, spot.z), spot.z);
            BossFx.warnDisc(level, ParticleTypes.SNOWFLAKE, impact, 2.0, 16);
            BossFx.ring(level, ModParticles.BOSS_FROST.get(), impact, 2.4, 14, 0.2);
            Vec3 from = impact.add(2 * (boss.getRandom().nextDouble() - 0.5), 30, 2 * (boss.getRandom().nextDouble() - 0.5));
            BossProjectile.shoot(level, boss, BossProjectile.Kind.ICICLE, from, impact.subtract(from).normalize(), 1.1, this.damage);
        }
    }

    /**
     * Eisspeere: Glaciara schlägt mit den Vorderbeinen auf, unter allen Gegnern friert der Boden – dann brechen
     * Säulen aus Eis empor (sie schmelzen nach einigen Sekunden wieder).
     */
    public static final class IceSpikes extends BossAttack {
        private final float damage;
        private final int waves;
        private final List<Vec3> spots = new ArrayList<>();

        public IceSpikes(float damage, int waves) {
            super(BossAnims.ERUPT, "ice_spikes");
            this.damage = damage;
            this.waves = waves;
        }

        @Override
        public int duration() {
            return 30 + this.waves * 22;
        }

        @Override
        public int cooldown() {
            return 190;
        }

        @Override
        public int minPhase() {
            return 2;
        }

        @Override
        public int turnUntil() {
            return 10;
        }

        @Override
        public float weight(DragonBossEntity boss, LivingEntity target, double distance, float angle) {
            return distance < 34 ? 5 : 0;
        }

        @Override
        public void tick(ServerLevel level, DragonBossEntity boss, LivingEntity target, int tick) {
            if (tick == 12) {
                BossFx.sound(level, boss.position(), SoundEvents.GLASS_BREAK, 3.0f, 0.4f);
                BossFx.sound(level, boss.position(), SoundEvents.GENERIC_EXPLODE.value(), 2.0f, 0.6f);
                boss.shake(level, 50, 0.45f, 12);
                BossFx.burst(level, ParticleTypes.SNOWFLAKE, boss.bodyPoint(0, 0.5, 4), 40, 3.0, 0.1);
            }
            int local = tick - 12;
            if (local < 0 || local / 22 >= this.waves) {
                return;
            }
            int inWave = local % 22;
            if (inWave == 0) {
                this.spots.clear();
                for (LivingEntity e : boss.enemiesAround(boss.position(), 34, 10)) {
                    this.spots.add(new Vec3(e.getX(), boss.groundY(e.getX(), e.getZ()), e.getZ()));
                }
                for (int i = 0; i < 4; i++) {
                    double a = boss.getRandom().nextDouble() * Mth.TWO_PI;
                    double r = 5 + boss.getRandom().nextDouble() * 18;
                    double x = boss.getX() + Math.cos(a) * r;
                    double z = boss.getZ() + Math.sin(a) * r;
                    this.spots.add(new Vec3(x, boss.groundY(x, z), z));
                }
            }
            if (inWave < 15) {
                for (Vec3 spot : this.spots) {
                    BossFx.warnDisc(level, ModParticles.BOSS_FROST.get(), spot, 1.8, 3);
                    if (inWave % 5 == 0) {
                        BossFx.sound(level, spot, SoundEvents.POWDER_SNOW_STEP, 1.5f, 0.6f);
                    }
                }
            } else if (inWave == 15) {
                for (Vec3 spot : this.spots) {
                    spike(level, BlockPos.containing(spot), 3 + boss.getRandom().nextInt(3));
                    BossFx.sound(level, spot, SoundEvents.GLASS_BREAK, 2.0f, 0.5f);
                    for (LivingEntity e : boss.enemiesAround(spot, 1.9, 3.0)) {
                        boss.hurtTarget(level, e, this.damage);
                        BossEffects.freeze(e, 3);
                        e.push(0, 1.0, 0);
                        e.needsSync = true;
                    }
                }
            }
        }

        /** Eine Eissäule mit Frostkristall an der Spitze (verschwindet nach 6 Sekunden). */
        private static void spike(ServerLevel level, BlockPos ground, int height) {
            BlockState ice = Blocks.PACKED_ICE.defaultBlockState();
            for (int y = 0; y < height; y++) {
                TemporaryBlocks.place(level, ground.above(y), y == height - 1 ? ModBlocks.FROST_CRYSTAL.get().defaultBlockState() : ice,
                        120 + y * 4);
            }
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, ice), ground.getX() + 0.5, ground.getY() + 1, ground.getZ() + 0.5,
                    30, 0.5, 1.0, 0.5, 0.2);
        }
    }

    /**
     * Schneesturm: Glaciara steigt auf die Hinterbeine und fächelt einen Sturm heran. Wer in ihrer Nähe ist, wird
     * im Kreis herumgeweht, friert ein und nimmt Schaden – Abstand halten oder hinter Deckung!
     */
    public static final class Blizzard extends BossAttack {
        private final double radius;
        private final float damage;

        public Blizzard(double radius, float damage) {
            super(BossAnims.SPECIAL_1, "blizzard");
            this.radius = radius;
            this.damage = damage;
        }

        @Override
        public int duration() {
            return 100;
        }

        @Override
        public int cooldown() {
            return 280;
        }

        @Override
        public float weight(DragonBossEntity boss, LivingEntity target, double distance, float angle) {
            return distance < this.radius * 0.8 ? 4 : 0;
        }

        @Override
        public void tick(ServerLevel level, DragonBossEntity boss, LivingEntity target, int tick) {
            Vec3 c = boss.position();
            if (tick == 6) {
                BossFx.sound(level, c, ModSounds.DRAGON_ROAR.get(), 5.0f, 0.6f);
            }
            if (tick < 16 || tick > 92) {
                return;
            }
            if (tick % 10 == 6) {
                BossFx.sound(level, c, SoundEvents.ELYTRA_FLYING, 3.0f, 0.5f);
            }
            // wirbelnder Schnee rund um den Drachen
            float spin = tick * 0.12f;
            for (int i = 0; i < 24; i++) {
                double a = spin + i * Mth.TWO_PI / 24;
                double r = 4 + (i % 4) * this.radius / 4.5;
                double x = c.x + Math.cos(a) * r;
                double z = c.z + Math.sin(a) * r;
                level.sendParticles(i % 3 == 0 ? ModParticles.BOSS_FROST.get() : ParticleTypes.SNOWFLAKE, x, c.y + 1 + (i % 5), z, 1,
                        -Math.sin(a) * 0.6, 0.0, Math.cos(a) * 0.6, 0.35);
            }
            level.sendParticles(ParticleTypes.WHITE_ASH, c.x, c.y + 4, c.z, 30, this.radius * 0.5, 3, this.radius * 0.5, 0.05);
            if (tick % 10 == 0) {
                for (LivingEntity e : boss.enemiesAround(c, this.radius, 8)) {
                    Vec3 d = e.position().subtract(c);
                    double dist = Math.max(1.0, d.horizontalDistance());
                    double fall = 1.0 - Math.min(1.0, dist / this.radius) * 0.5;
                    // tangentialer Wind (im Kreis herum) und ein wenig nach außen
                    double tx = -d.z / dist;
                    double tz = d.x / dist;
                    e.push(tx * 0.45 * fall + d.x / dist * 0.1, 0.05, tz * 0.45 * fall + d.z / dist * 0.1);
                    e.needsSync = true;
                    boss.hurtTarget(level, e, (float) (this.damage * fall));
                    BossEffects.freeze(e, 2);
                }
            }
        }
    }

    /** Eisgefängnis: Frost wirbelt um das Ziel – dann schließt sich eine Hülle aus Eis um es (vier Sekunden). */
    public static final class IcePrison extends BossAttack {
        private LivingEntity victim;

        public IcePrison() {
            super(BossAnims.SPECIAL_2, "ice_prison");
        }

        @Override
        public int duration() {
            return 50;
        }

        @Override
        public int cooldown() {
            return 260;
        }

        @Override
        public int turnUntil() {
            return 30;
        }

        @Override
        public Where where() {
            return Where.ANY;
        }

        @Override
        public float weight(DragonBossEntity boss, LivingEntity target, double distance, float angle) {
            return distance > 6 && distance < 30 && angle < 60 ? 4 : 0;
        }

        @Override
        public void start(ServerLevel level, DragonBossEntity boss, LivingEntity target) {
            this.victim = target;
        }

        @Override
        public void tick(ServerLevel level, DragonBossEntity boss, LivingEntity target, int tick) {
            LivingEntity v = this.victim != null && this.victim.isAlive() ? this.victim : target;
            Vec3 p = v.position();
            if (tick < 30) {
                // Frost windet sich spiralförmig um das Ziel
                for (int i = 0; i < 3; i++) {
                    double a = tick * 0.5 + i * Mth.TWO_PI / 3;
                    double y = (tick % 15) / 15.0 * 2.2;
                    level.sendParticles(ModParticles.BOSS_FROST.get(), p.x + Math.cos(a) * 1.1, p.y + y, p.z + Math.sin(a) * 1.1, 1, 0, 0, 0, 0);
                    level.sendParticles(ParticleTypes.SNOWFLAKE, p.x + Math.cos(a + 1) * 1.3, p.y + y, p.z + Math.sin(a + 1) * 1.3, 1, 0, 0, 0, 0);
                }
                // Hauch aus dem Maul zum Ziel
                Vec3 mouth = boss.mouthPosition();
                Vec3 dir = p.add(0, 1, 0).subtract(mouth).normalize();
                BossFx.stream(level, ParticleTypes.SNOWFLAKE, mouth, dir, 8, 0.6, 1, 0.8);
                if (tick == 4) {
                    BossFx.sound(level, mouth, SoundEvents.PLAYER_HURT_FREEZE, 3.0f, 0.5f);
                }
                return;
            }
            if (tick == 30) {
                if (v instanceof Player player && (player.isCreative() || player.isSpectator())) {
                    return;
                }
                BlockPos feet = v.blockPosition();
                BlockState ice = Blocks.PACKED_ICE.defaultBlockState();
                int height = v.getBbHeight() > 1.0f ? 2 : 1;
                for (int y = 0; y < height; y++) {
                    for (Direction dir : Direction.Plane.HORIZONTAL) {
                        TemporaryBlocks.place(level, feet.relative(dir).above(y), ice, 80);
                    }
                }
                TemporaryBlocks.place(level, feet.above(height), ice, 80);
                TemporaryBlocks.place(level, feet.below(), ice, 80);
                v.teleportTo(feet.getX() + 0.5, v.getY(), feet.getZ() + 0.5);
                v.setDeltaMovement(Vec3.ZERO);
                BossEffects.freeze(v, 4);
                BossFx.sound(level, p, SoundEvents.GLASS_PLACE, 3.0f, 0.5f);
                BossFx.sound(level, p, SoundEvents.AMETHYST_BLOCK_BREAK, 2.0f, 0.6f);
                level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, ice), p.x, p.y + 1, p.z, 40, 0.8, 1.0, 0.8, 0.1);
            }
        }

        @Override
        public void stop(ServerLevel level, DragonBossEntity boss) {
            this.victim = null;
        }
    }
}
