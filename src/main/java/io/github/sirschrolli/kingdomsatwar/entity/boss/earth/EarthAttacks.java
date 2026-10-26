package io.github.sirschrolli.kingdomsatwar.entity.boss.earth;

import io.github.sirschrolli.kingdomsatwar.entity.boss.BossAnims;
import io.github.sirschrolli.kingdomsatwar.entity.boss.BossAttack;
import io.github.sirschrolli.kingdomsatwar.entity.boss.BossFx;
import io.github.sirschrolli.kingdomsatwar.entity.boss.BossProjectile;
import io.github.sirschrolli.kingdomsatwar.entity.boss.DragonBossEntity;
import io.github.sirschrolli.kingdomsatwar.registry.ModBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.ModParticles;
import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import io.github.sirschrolli.kingdomsatwar.world.TemporaryBlocks;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PointedDripstoneBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SpeleothemThickness;
import net.minecraft.world.phys.Vec3;

/**
 * Terragons eigene Angriffe: Eingraben und Auftauchen, Stalagmitenreihe, Erdbeben-Stampfer, Steinschlag,
 * Felsbrocken-Wurf und Kristallpanzer.
 */
public final class EarthAttacks {
    private EarthAttacks() {}

    /**
     * Eingraben und Auftauchen: Terragon gräbt sich in den Boden (unverwundbar), wühlt sich als Erdwelle zu seinem
     * Ziel – Brocken und Staub verraten den Weg – und bricht unter ihm hervor, dass alles in die Luft fliegt.
     */
    public static final class Burrow extends BossAttack {
        private static final int DIG = 28;
        private static final int MAX_TRAVEL = 90;
        private static final int WARN = 18;
        private static final int RISE = 10;
        private final float damage;
        private int stage;
        private int stageTick;
        private Vec3 spot = Vec3.ZERO;
        private double startY;

        public Burrow(float damage) {
            super(BossAnims.SPECIAL_1, "burrow");
            this.damage = damage;
        }

        @Override
        public int duration() {
            return DIG + MAX_TRAVEL + WARN + RISE + 30;
        }

        @Override
        public int cooldown() {
            return 340;
        }

        @Override
        public float weight(DragonBossEntity boss, LivingEntity target, double distance, float angle) {
            return distance > 12 ? 6 : 2;
        }

        @Override
        public void start(ServerLevel level, DragonBossEntity boss, LivingEntity target) {
            this.stage = 0;
            this.stageTick = 0;
            this.startY = boss.getY();
            boss.noPhysics = true;
            if (boss instanceof TerragonEntity terragon) {
                terragon.setBurrowing(true);
            }
        }

        @Override
        public void tick(ServerLevel level, DragonBossEntity boss, LivingEntity target, int tick) {
            this.stageTick++;
            boss.setDeltaMovement(Vec3.ZERO);
            switch (this.stage) {
                case 0 -> this.dig(level, boss);
                case 1 -> this.travel(level, boss, target);
                case 2 -> this.warn(level, boss);
                case 3 -> this.rise(level, boss);
                default -> {
                    if (this.stageTick > 20) {
                        boss.finishAttack(level);
                    }
                }
            }
        }

        private void next() {
            this.stage++;
            this.stageTick = 0;
        }

        private void dig(ServerLevel level, DragonBossEntity boss) {
            double depth = 8.0 * this.stageTick / DIG;
            boss.setPos(boss.getX(), this.startY - depth, boss.getZ());
            Vec3 c = new Vec3(boss.getX(), this.startY, boss.getZ());
            BlockState ground = level.getBlockState(BlockPos.containing(c.x, c.y - 0.5, c.z));
            if (!ground.isAir()) {
                level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, ground), c.x, c.y + 0.5, c.z, 24, 3.0, 0.6, 3.0, 0.2);
            }
            BossFx.burst(level, ModParticles.BOSS_DUST.get(), c.add(0, 1, 0), 6, 3.5, 0.03);
            if (this.stageTick == 2) {
                BossFx.sound(level, c, ModSounds.DRAGON_ROAR.get(), 5.0f, 0.5f);
            }
            if (this.stageTick % 5 == 0) {
                BossFx.sound(level, c, SoundEvents.GRAVEL_BREAK, 3.0f, 0.5f);
                BossFx.sound(level, c, SoundEvents.ROOTED_DIRT_BREAK, 3.0f, 0.4f);
                boss.shake(level, 40, 0.35f, 6);
            }
            if (this.stageTick >= DIG) {
                boss.setMoveMode(DragonBossEntity.MoveMode.BURROW);
                this.next();
            }
        }

        private void travel(ServerLevel level, DragonBossEntity boss, LivingEntity target) {
            Vec3 to = target.position().subtract(boss.position()).multiply(1, 0, 1);
            double dist = to.length();
            if (dist > 0.5) {
                Vec3 step = to.scale(Math.min(0.55, dist) / dist);
                double x = boss.getX() + step.x;
                double z = boss.getZ() + step.z;
                boss.setPos(x, boss.groundY(x, z) - 8, z);
                boss.turnTowards(target.position(), 8.0f);
            }
            int gy = boss.groundY(boss.getX(), boss.getZ());
            BlockState ground = level.getBlockState(BlockPos.containing(boss.getX(), gy - 1, boss.getZ()));
            if (!ground.isAir()) {
                level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, ground), boss.getX(), gy + 0.2, boss.getZ(), 10,
                        1.4, 0.2, 1.4, 0.15);
            }
            level.sendParticles(ModParticles.BOSS_DUST.get(), boss.getX(), gy + 0.5, boss.getZ(), 2, 1.0, 0.2, 1.0, 0.02);
            if (this.stageTick % 6 == 0) {
                BossFx.sound(level, new Vec3(boss.getX(), gy, boss.getZ()), SoundEvents.GRAVEL_STEP, 3.0f, 0.4f);
                boss.shake(level, 20, 0.25f, 6);
            }
            if (dist < 2.0 || this.stageTick >= MAX_TRAVEL) {
                this.spot = new Vec3(boss.getX(), gy, boss.getZ());
                this.next();
            }
        }

        private void warn(ServerLevel level, DragonBossEntity boss) {
            BossFx.warnDisc(level, ModParticles.BOSS_DUST.get(), this.spot, 4.5, 8);
            BlockState ground = level.getBlockState(BlockPos.containing(this.spot.x, this.spot.y - 1, this.spot.z));
            if (!ground.isAir()) {
                level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, ground), this.spot.x, this.spot.y + 0.2, this.spot.z, 8,
                        2.5, 0.1, 2.5, 0.1);
            }
            if (this.stageTick % 4 == 0) {
                BossFx.sound(level, this.spot, SoundEvents.STONE_BREAK, 2.5f, 0.5f);
            }
            if (this.stageTick >= WARN) {
                this.next();
            }
        }

        private void rise(ServerLevel level, DragonBossEntity boss) {
            double u = (double) this.stageTick / RISE;
            boss.setPos(this.spot.x, this.spot.y - 8 + 8 * Math.min(1.0, u), this.spot.z);
            if (this.stageTick == 1) {
                BossFx.sound(level, this.spot, SoundEvents.GENERIC_EXPLODE.value(), 5.0f, 0.5f);
                BossFx.sound(level, this.spot, ModSounds.DRAGON_ROAR.get(), 6.0f, 0.5f);
                boss.shake(level, 50, 1.0f, 18);
                BossFx.burst(level, ParticleTypes.EXPLOSION, this.spot.add(0, 1, 0), 5, 3.0, 0.0);
                BlockState ground = level.getBlockState(BlockPos.containing(this.spot.x, this.spot.y - 1, this.spot.z));
                if (!ground.isAir()) {
                    level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, ground), this.spot.x, this.spot.y + 1, this.spot.z, 120,
                            3.0, 1.5, 3.0, 0.4);
                }
                BossFx.burst(level, ModParticles.BOSS_DUST.get(), this.spot.add(0, 2, 0), 40, 4.0, 0.1);
                for (LivingEntity e : boss.enemiesAround(this.spot, 5.5, 4.0)) {
                    boss.hurtTarget(level, e, this.damage);
                    e.push(0, 1.4, 0);
                    DragonBossEntity.pushAway(e, this.spot, 0.8, 0.0);
                    e.needsSync = true;
                }
            }
            if (this.stageTick >= RISE) {
                boss.noPhysics = false;
                boss.setMoveMode(DragonBossEntity.MoveMode.GROUND);
                if (boss instanceof TerragonEntity terragon) {
                    terragon.setBurrowing(false);
                }
                this.next();
            }
        }

        @Override
        public void stop(ServerLevel level, DragonBossEntity boss) {
            boss.noPhysics = false;
            if (boss.getMoveMode() == DragonBossEntity.MoveMode.BURROW) {
                boss.setMoveMode(DragonBossEntity.MoveMode.GROUND);
            }
            int gy = boss.groundY(boss.getX(), boss.getZ());
            if (boss.getY() < gy - 0.5) {
                boss.setPos(boss.getX(), gy, boss.getZ());   // nie unter der Erde stecken bleiben
            }
            if (boss instanceof TerragonEntity terragon) {
                terragon.setBurrowing(false);
            }
        }
    }

    /**
     * Stalagmitenreihe: Terragon schlägt mit dem Schädel auf den Boden, und eine Reihe spitzer Tropfsteine schießt
     * auf das Ziel zu aus der Erde (ab Phase 2 drei Reihen im Fächer).
     */
    public static final class StalagmiteRow extends BossAttack {
        private final float damage;
        private final List<Vec3> dirs = new ArrayList<>();
        private final Set<UUID> hit = new HashSet<>();
        private Vec3 origin = Vec3.ZERO;

        public StalagmiteRow(float damage) {
            super(BossAnims.ERUPT, "stalagmite_row");
            this.damage = damage;
        }

        @Override
        public int duration() {
            return 54;
        }

        @Override
        public int cooldown() {
            return 160;
        }

        @Override
        public int turnUntil() {
            return 12;
        }

        @Override
        public float weight(DragonBossEntity boss, LivingEntity target, double distance, float angle) {
            return distance > 6 && distance < 32 && angle < 50 ? 6 : 0;
        }

        @Override
        public void start(ServerLevel level, DragonBossEntity boss, LivingEntity target) {
            this.dirs.clear();
            this.hit.clear();
        }

        @Override
        public void tick(ServerLevel level, DragonBossEntity boss, LivingEntity target, int tick) {
            if (tick == 13) {
                this.origin = boss.bodyPoint(0, 0, 6);
                Vec3 to = target.position().subtract(this.origin).multiply(1, 0, 1);
                Vec3 d = to.lengthSqr() < 1 ? boss.forward() : to.normalize();
                this.dirs.add(d);
                if (boss.getPhase() >= 2) {
                    this.dirs.add(d.yRot(22 * Mth.DEG_TO_RAD));
                    this.dirs.add(d.yRot(-22 * Mth.DEG_TO_RAD));
                }
                BossFx.sound(level, this.origin, SoundEvents.GENERIC_EXPLODE.value(), 3.0f, 0.45f);
                boss.shake(level, 45, 0.6f, 14);
                BossFx.groundDust(level, this.origin, 3, 50);
            }
            if (tick < 14 || tick > 44) {
                return;
            }
            double dist = 3 + (tick - 14) * 1.0;
            for (Vec3 d : this.dirs) {
                Vec3 p = this.origin.add(d.scale(dist));
                int gy = boss.groundY(p.x, p.z);
                BlockPos base = BlockPos.containing(p.x, gy, p.z);
                int height = 2 + level.getRandom().nextInt(3);
                boolean any = false;
                for (int y = 0; y < height; y++) {
                    BlockState state;
                    if (y < height - 2) {
                        state = Blocks.DRIPSTONE_BLOCK.defaultBlockState();
                    } else {
                        SpeleothemThickness thickness = y == height - 1 ? SpeleothemThickness.TIP : SpeleothemThickness.FRUSTUM;
                        state = Blocks.POINTED_DRIPSTONE.defaultBlockState().setValue(PointedDripstoneBlock.TIP_DIRECTION, Direction.UP)
                                .setValue(PointedDripstoneBlock.THICKNESS, thickness);
                    }
                    // die Spitze verschwindet zuerst (sonst bräche sie ab)
                    any |= TemporaryBlocks.place(level, base.above(y), state, 100 - y * 8);
                }
                if (any) {
                    level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.DRIPSTONE_BLOCK.defaultBlockState()), p.x, gy + 1,
                            p.z, 14, 0.4, 0.4, 0.4, 0.15);
                    if (tick % 2 == 0) {
                        BossFx.sound(level, p, SoundEvents.POINTED_DRIPSTONE_LAND, 2.0f, 0.6f);
                    }
                }
                for (LivingEntity e : boss.enemiesAround(new Vec3(p.x, gy + 1, p.z), 1.7, 3.0)) {
                    if (this.hit.add(e.getUUID())) {
                        boss.hurtTarget(level, e, this.damage);
                        e.push(d.x * 0.3, 1.1, d.z * 0.3);
                        e.needsSync = true;
                    }
                }
            }
        }
    }

    /**
     * Erdbeben-Stampfer: Terragon bäumt sich auf und lässt sich mit voller Wucht fallen. Risse laufen sternförmig über
     * den Boden, die Stoßwelle schleudert alles in der Nähe davon – und von oben prasseln Steine herab.
     */
    public static final class Quake extends BossAttack {
        private final float damage;
        private final List<Vec3> rocks = new ArrayList<>();
        private Vec3 center = Vec3.ZERO;

        public Quake(float damage) {
            super(BossAnims.STOMP, "quake");
            this.damage = damage;
        }

        @Override
        public int duration() {
            return 60;
        }

        @Override
        public int cooldown() {
            return 190;
        }

        @Override
        public float weight(DragonBossEntity boss, LivingEntity target, double distance, float angle) {
            int close = boss.enemiesAround(boss.position(), 14, 5).size();
            return close >= 2 ? 7 : distance < 12 ? 4 : 0;
        }

        @Override
        public void tick(ServerLevel level, DragonBossEntity boss, LivingEntity target, int tick) {
            if (tick == 20) {
                this.center = new Vec3(boss.getX(), boss.getY(), boss.getZ());
                BossFx.sound(level, this.center, SoundEvents.GENERIC_EXPLODE.value(), 5.0f, 0.4f);
                BossFx.sound(level, this.center, ModSounds.DRAGON_STEP.get(), 5.0f, 0.3f);
                boss.shake(level, 70, 1.1f, 25);
                BossFx.shockwave(level, boss, this.center, 14, this.damage, 1.0);
                this.rocks.clear();
                for (LivingEntity e : boss.enemiesAround(this.center, 40, 12)) {
                    this.rocks.add(e.position());
                }
            }
            if (tick > 20 && tick < 32) {
                // Risse laufen sternförmig nach außen
                double r = (tick - 20) * 1.4;
                for (int k = 0; k < 8; k++) {
                    double a = k * Mth.TWO_PI / 8 + 0.3;
                    double x = this.center.x + Math.cos(a) * r;
                    double z = this.center.z + Math.sin(a) * r;
                    int gy = boss.groundY(x, z);
                    BlockState ground = level.getBlockState(BlockPos.containing(x, gy - 1, z));
                    if (!ground.isAir()) {
                        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, ground), x, gy + 0.1, z, 6, 0.3, 0.05, 0.3, 0.12);
                    }
                    level.sendParticles(ModParticles.BOSS_DUST.get(), x, gy + 0.3, z, 1, 0.2, 0.1, 0.2, 0.01);
                }
            }
            if (tick >= 28 && tick <= 52 && tick % 4 == 0) {
                // Steinschlag über den Gegnern
                for (Vec3 p : this.rocks) {
                    if (boss.getRandom().nextFloat() < 0.6f) {
                        Vec3 spot = p.add((boss.getRandom().nextDouble() - 0.5) * 6, 0, (boss.getRandom().nextDouble() - 0.5) * 6);
                        Vec3 from = spot.add(0, 20, 0);
                        BossFx.warnDisc(level, ModParticles.BOSS_DUST.get(), new Vec3(spot.x, boss.groundY(spot.x, spot.z), spot.z), 1.8, 6);
                        BossProjectile.shoot(level, boss, BossProjectile.Kind.BOULDER, from, new Vec3(0, -1, 0), 0.6, this.damage * 0.7f);
                    }
                }
            }
        }
    }

    /**
     * Felsbrocken: Terragon reißt Brocken aus dem Boden und schleudert sie im hohen Bogen auf seine Gegner.
     */
    public static final class BoulderThrow extends BossAttack {
        private final int boulders;
        private final float damage;

        public BoulderThrow(int boulders, float damage) {
            super(BossAnims.VOLLEY, "boulder_throw");
            this.boulders = boulders;
            this.damage = damage;
        }

        @Override
        public int duration() {
            return 20 + this.boulders * 12;
        }

        @Override
        public int cooldown() {
            return 130;
        }

        @Override
        public int turnUntil() {
            return this.duration();
        }

        @Override
        public float weight(DragonBossEntity boss, LivingEntity target, double distance, float angle) {
            return distance > 10 ? 6 : 1;
        }

        @Override
        public void tick(ServerLevel level, DragonBossEntity boss, LivingEntity target, int tick) {
            if (tick < 16 || (tick - 16) % 12 != 0 || (tick - 16) / 12 >= this.boulders) {
                if (tick % 6 == 0 && tick < 16) {
                    BossFx.groundDust(level, boss.bodyPoint(0, 0, 6), 2, 20);
                }
                return;
            }
            Vec3 mouth = boss.mouthPosition();
            Vec3 aim = target.position().add(target.getDeltaMovement().multiply(12, 0, 12));
            aim = aim.add((boss.getRandom().nextDouble() - 0.5) * 3, 0, (boss.getRandom().nextDouble() - 0.5) * 3);
            Vec3 d = aim.subtract(mouth);
            double horizontal = Math.max(1.0, d.horizontalDistance());
            double speed = 1.1;
            double time = horizontal / speed;
            double vy = (d.y + 0.5 * 0.05 * time * time) / time;
            Vec3 velocity = new Vec3(d.x / horizontal * speed, vy, d.z / horizontal * speed);
            BossProjectile.shoot(level, boss, BossProjectile.Kind.BOULDER, mouth, velocity, velocity.length(), this.damage);
            BossFx.sound(level, mouth, SoundEvents.STONE_BREAK, 3.0f, 0.4f);
            BossFx.sound(level, mouth, SoundEvents.RAVAGER_ATTACK, 2.0f, 0.5f);
        }
    }

    /**
     * Kristallpanzer: Terragon brüllt, aus seinen Platten wachsen leuchtende Bernsteinkristalle – zehn Sekunden lang
     * prallt fast aller Schaden ab. Dann zerspringt der Panzer, und Kristallsplitter fliegen in alle Richtungen.
     */
    public static final class CrystalArmor extends BossAttack {
        public CrystalArmor() {
            super(BossAnims.SPECIAL_2, "crystal_armor");
        }

        @Override
        public int duration() {
            return 50;
        }

        @Override
        public int cooldown() {
            return 620;
        }

        @Override
        public int minPhase() {
            return 2;
        }

        @Override
        public float weight(DragonBossEntity boss, LivingEntity target, double distance, float angle) {
            return boss.getHealth() < boss.getMaxHealth() * 0.7f ? 6 : 0;
        }

        @Override
        public void tick(ServerLevel level, DragonBossEntity boss, LivingEntity target, int tick) {
            Vec3 back = boss.bodyPoint(0, 5.0, 0);
            if (tick == 4) {
                BossFx.sound(level, back, ModSounds.DRAGON_ROAR.get(), 6.0f, 0.45f);
                BossFx.sound(level, back, SoundEvents.AMETHYST_BLOCK_RESONATE, 4.0f, 0.5f);
            }
            if (tick < 40) {
                level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, ModBlocks.AMBER_CRYSTAL.get().defaultBlockState()),
                        back.x, back.y, back.z, 6, 3.0, 1.5, 3.0, 0.05);
                BossFx.burst(level, ParticleTypes.WAX_ON, back, 4, 3.5, 0.05);
                if (tick % 8 == 0) {
                    BossFx.sound(level, back, SoundEvents.AMETHYST_CLUSTER_PLACE, 3.0f, 0.5f + tick * 0.01f);
                }
            }
            if (tick == 36 && boss instanceof TerragonEntity terragon) {
                terragon.armorUp(200);
            }
        }
    }

    /** Wirkung des Staubodems: blendet kurz und verlangsamt. */
    public static void dust(DragonBossEntity boss, LivingEntity e) {
        e.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 50, 0));
        e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 50, 1));
    }
}
