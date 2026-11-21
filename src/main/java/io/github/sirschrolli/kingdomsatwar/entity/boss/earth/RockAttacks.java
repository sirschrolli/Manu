package io.github.sirschrolli.kingdomsatwar.entity.boss.earth;

import io.github.sirschrolli.kingdomsatwar.entity.boss.BossAnims;
import io.github.sirschrolli.kingdomsatwar.entity.boss.BossAttack;
import io.github.sirschrolli.kingdomsatwar.entity.boss.BossFx;
import io.github.sirschrolli.kingdomsatwar.entity.boss.DragonBossEntity;
import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/** Terragons eigene große Angriffe: die Felslawine aus rollenden Brocken und der Erdspalter. */
public final class RockAttacks {
    private RockAttacks() {}

    /** Staub der Felsen. */
    private static final DustParticleOptions DUST = new DustParticleOptions(0x8A7A62, 1.5f);
    /** Glühende Risse im Gestein. */
    private static final DustParticleOptions CRACK = new DustParticleOptions(0xD08A3A, 1.2f);

    /** Gestein des Bodens bei x/z (für die Brocken-Teilchen), sonst Bruchstein. */
    private static BlockState rock(DragonBossEntity boss, double x, double z) {
        BlockPos below = BlockPos.containing(x, boss.groundY(x, z) - 0.5, z);
        BlockState state = boss.level().getBlockState(below);
        return state.isAir() || !state.isSolid() ? Blocks.COBBLESTONE.defaultBlockState() : state;
    }

    // =====================================================================================
    // Felslawine
    // =====================================================================================

    /**
     * Felslawine: Terragon schlägt mit dem Schwanz auf – vor ihm brechen drei bis fünf Felsbrocken aus dem Boden und
     * rollen schnurgerade auf die Gegner zu, über Stock und Stein, bis sie an einer Wand zerschellen oder ausrollen. Wer
     * zur Seite springt, lässt sie vorbei; wer getroffen wird, fliegt weit und ist kurz benommen.
     */
    public static final class RollingBoulders extends BossAttack {
        private static final double SPEED = 0.5;
        private final float damage;
        private final List<Vec3[]> pending = new ArrayList<>();

        public RollingBoulders(float damage) {
            super(BossAnims.TAIL_SWEEP, "rolling_boulders");
            this.damage = damage;
        }

        @Override
        public int duration() {
            return 46;
        }

        @Override
        public int cooldown() {
            return 300;
        }

        @Override
        public int turnUntil() {
            return 14;
        }

        @Override
        public float weight(DragonBossEntity boss, LivingEntity target, double distance, float angle) {
            return distance > 6.0 ? 6 : 2;
        }

        @Override
        public void start(ServerLevel level, DragonBossEntity boss, LivingEntity target) {
            this.pending.clear();
        }

        @Override
        public void tick(ServerLevel level, DragonBossEntity boss, LivingEntity target, int tick) {
            int count = 2 + boss.getPhase();
            if (tick == 16) {
                BossFx.sound(level, boss.position(), SoundEvents.GENERIC_EXPLODE.value(), 4.0f, 0.45f);
                BossFx.sound(level, boss.position(), ModSounds.DRAGON_ROAR.get(), 4.0f, 0.6f);
                boss.shake(level, 50, 0.5f, 18);
                List<LivingEntity> enemies = boss.enemiesAround(BossFx.arena(boss), DragonBossEntity.ARENA_RADIUS + 6.0, 40);
                for (int i = 0; i < count; i++) {
                    // vor dem Drachen nebeneinander, jeder rollt auf einen Gegner zu (die übrigen fächern auf)
                    double side = (i - (count - 1) / 2.0) * 3.4;
                    Vec3 start = boss.bodyPoint(side, 0.0, 7.0);
                    start = BossFx.ground(boss, start.x, start.z);
                    Vec3 dir;
                    if (!enemies.isEmpty() && i < enemies.size()) {
                        LivingEntity e = enemies.get(i);
                        dir = new Vec3(e.getX() - start.x, 0.0, e.getZ() - start.z);
                    } else {
                        double spread = (i - (count - 1) / 2.0) * 0.32;
                        Vec3 f = boss.forward();
                        dir = new Vec3(f.x * Math.cos(spread) - f.z * Math.sin(spread), 0.0, f.z * Math.cos(spread) + f.x * Math.sin(spread));
                    }
                    if (dir.lengthSqr() < 1.0e-4) {
                        dir = boss.forward();
                    }
                    this.pending.add(new Vec3[] {start, dir.normalize()});
                    // der Brocken bricht aus dem Boden
                    BlockParticleOption chunk = new BlockParticleOption(ParticleTypes.BLOCK, rock(boss, start.x, start.z));
                    BossFx.burst(level, chunk, start.add(0, 0.8, 0), 40, 1.0, 0.25);
                    BossFx.groundDust(level, start, 2.5, 40);
                }
            }
            // kurz danach rollen sie los, einer nach dem anderen
            if (tick >= 22 && tick < 22 + this.pending.size() * 3 && (tick - 22) % 3 == 0) {
                Vec3[] p = this.pending.get((tick - 22) / 3);
                var boulder = io.github.sirschrolli.kingdomsatwar.entity.boss.BossProjectile.shoot(level, boss,
                        io.github.sirschrolli.kingdomsatwar.entity.boss.BossProjectile.Kind.ROLLING, p[0].add(0.0, 0.05, 0.0), p[1], SPEED,
                        this.damage);
                boulder.accelerationPower = 0.0;
                BossFx.sound(level, p[0], SoundEvents.STONE_BREAK, 3.0f, 0.5f);
                BossFx.sound(level, p[0], SoundEvents.GRINDSTONE_USE, 2.0f, 0.4f);
            }
        }

        @Override
        public void stop(ServerLevel level, DragonBossEntity boss) {
            this.pending.clear();
        }
    }

    // =====================================================================================
    // Erdspalter
    // =====================================================================================

    /**
     * Erdspalter: Terragon schlägt die Pranken in den Boden – ein Riss läuft im Zickzack auf das Ziel zu, Gestein platzt
     * auf, und kurz hinter der Spitze schießen Felsnadeln empor, die jeden hochschleudern, der auf dem Riss steht. In der
     * letzten Phase laufen zwei Risse schräg nebenher.
     */
    public static final class Earthsplitter extends BossAttack {
        private static final int IMPACT = 18;
        private static final int LENGTH = 30;
        private static final int DELAY = 7;
        private final float damage;
        private final List<List<Vec3>> cracks = new ArrayList<>();
        private final Set<UUID> hit = new HashSet<>();
        private double aim;

        public Earthsplitter(float damage) {
            super(BossAnims.ERUPT, "earthsplitter");
            this.damage = damage;
        }

        @Override
        public int duration() {
            return IMPACT + LENGTH + DELAY + 14;
        }

        @Override
        public int cooldown() {
            return 280;
        }

        @Override
        public int turnUntil() {
            return IMPACT - 2;
        }

        @Override
        public float weight(DragonBossEntity boss, LivingEntity target, double distance, float angle) {
            return distance > 5.0 && distance < LENGTH + 4 && angle < 50.0f ? 7 : 2;
        }

        @Override
        public void start(ServerLevel level, DragonBossEntity boss, LivingEntity target) {
            this.cracks.clear();
            this.hit.clear();
            this.aim = Math.atan2(target.getZ() - boss.getZ(), target.getX() - boss.getX());
            int n = boss.getPhase() >= 3 ? 3 : 1;
            for (int i = 0; i < n; i++) {
                this.cracks.add(new ArrayList<>());
            }
        }

        @Override
        public void tick(ServerLevel level, DragonBossEntity boss, LivingEntity target, int tick) {
            if (tick == IMPACT) {
                BossFx.sound(level, boss.position(), SoundEvents.GENERIC_EXPLODE.value(), 4.0f, 0.4f);
                boss.shake(level, 60, 0.7f, 20);
                BossFx.groundDust(level, boss.bodyPoint(0.0, 0.0, 5.0), 4.0, 60);
            }
            if (tick > IMPACT && tick <= IMPACT + LENGTH) {
                int step = tick - IMPACT;
                for (int i = 0; i < this.cracks.size(); i++) {
                    double a = this.aim + (i == 0 ? 0.0 : (i == 1 ? 0.35 : -0.35));
                    double zig = (step % 4 < 2 ? 1 : -1) * 0.6;
                    double r = 4.0 + step;
                    double x = boss.getX() + Math.cos(a) * r - Math.sin(a) * zig;
                    double z = boss.getZ() + Math.sin(a) * r + Math.cos(a) * zig;
                    Vec3 p = BossFx.ground(boss, x, z);
                    if (BossFx.arena(boss).distanceTo(p) > DragonBossEntity.ARENA_RADIUS) {
                        continue;
                    }
                    this.cracks.get(i).add(p);
                    BlockParticleOption chunk = new BlockParticleOption(ParticleTypes.BLOCK, rock(boss, x, z));
                    level.sendParticles(chunk, p.x, p.y + 0.2, p.z, 8, 0.3, 0.1, 0.3, 0.15);
                    level.sendParticles(CRACK, p.x, p.y + 0.1, p.z, 2, 0.2, 0.02, 0.2, 0.0);
                }
                if (step % 3 == 0) {
                    BossFx.sound(level, this.cracks.get(0).isEmpty() ? boss.position() : this.cracks.get(0).getLast(), SoundEvents.STONE_BREAK,
                            3.0f, 0.5f);
                }
            }
            // Felsnadeln schießen kurz hinter der Spitze empor
            int spike = tick - IMPACT - DELAY;
            if (spike >= 1) {
                for (List<Vec3> crack : this.cracks) {
                    if (spike - 1 >= crack.size()) {
                        continue;
                    }
                    Vec3 p = crack.get(spike - 1);
                    BlockParticleOption chunk = new BlockParticleOption(ParticleTypes.BLOCK, rock(boss, p.x, p.z));
                    for (double h = 0.2; h < 3.2; h += 0.4) {
                        double w = 0.45 * (1.0 - h / 3.4);
                        level.sendParticles(chunk, p.x, p.y + h, p.z, 3, w, 0.1, w, 0.0);
                    }
                    level.sendParticles(DUST, p.x, p.y + 0.4, p.z, 4, 0.5, 0.2, 0.5, 0.02);
                    // echte Felsnadeln, die nach zwei Sekunden wieder zerbröseln (nicht dort, wo jemand steht)
                    this.spike(level, boss, BlockPos.containing(p.x, p.y, p.z), spike);
                    for (LivingEntity e : boss.enemiesAround(p, 1.6, 3.0)) {
                        if (this.hit.add(e.getUUID())) {
                            boss.hurtTarget(level, e, this.damage);
                            e.push(0.0, 1.05, 0.0);
                            e.needsSync = true;
                        }
                    }
                    if (spike % 3 == 0) {
                        BossFx.sound(level, p, SoundEvents.POINTED_DRIPSTONE_LAND, 3.0f, 0.6f);
                    }
                }
            }
        }

        /** Eine Felsnadel aus Tropfstein: unten ein Block, oben die Spitze (jede dritte nur die Spitze). */
        private void spike(ServerLevel level, DragonBossEntity boss, BlockPos base, int index) {
            if (!level.getEntitiesOfClass(LivingEntity.class, new net.minecraft.world.phys.AABB(base).expandTowards(0, 2, 0),
                    e -> !(e instanceof DragonBossEntity)).isEmpty()) {
                return;
            }
            BlockState tip = Blocks.POINTED_DRIPSTONE.defaultBlockState()
                    .setValue(net.minecraft.world.level.block.PointedDripstoneBlock.TIP_DIRECTION, net.minecraft.core.Direction.UP)
                    .setValue(net.minecraft.world.level.block.PointedDripstoneBlock.THICKNESS,
                            net.minecraft.world.level.block.state.properties.SpeleothemThickness.TIP);
            int ticks = 46 + (index % 4) * 3;
            if (index % 3 == 0) {
                io.github.sirschrolli.kingdomsatwar.world.TemporaryBlocks.place(level, base, tip, ticks);
            } else if (io.github.sirschrolli.kingdomsatwar.world.TemporaryBlocks.place(level, base, Blocks.DRIPSTONE_BLOCK.defaultBlockState(), ticks)) {
                io.github.sirschrolli.kingdomsatwar.world.TemporaryBlocks.place(level, base.above(), tip, ticks);
            }
        }

        @Override
        public void stop(ServerLevel level, DragonBossEntity boss) {
            this.cracks.clear();
            this.hit.clear();
        }
    }
}
