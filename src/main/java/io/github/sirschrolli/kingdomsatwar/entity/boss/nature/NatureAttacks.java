package io.github.sirschrolli.kingdomsatwar.entity.boss.nature;

import io.github.sirschrolli.kingdomsatwar.entity.boss.BossAnims;
import io.github.sirschrolli.kingdomsatwar.entity.boss.BossAttack;
import io.github.sirschrolli.kingdomsatwar.entity.boss.BossEffects;
import io.github.sirschrolli.kingdomsatwar.entity.boss.BossFx;
import io.github.sirschrolli.kingdomsatwar.entity.boss.BossProjectile;
import io.github.sirschrolli.kingdomsatwar.entity.boss.DragonBossEntity;
import io.github.sirschrolli.kingdomsatwar.registry.ModBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
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
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Verdantis' eigene Angriffe: Wurzelfessel, Rankensäulen, Giftsporen, Dornensaat, Rankendiener und die Heilung aus
 * den Lebensblüten des Hains.
 */
public final class NatureAttacks {
    private NatureAttacks() {}

    /**
     * Wurzelfessel: Verdantis bäumt sich auf und schlägt die Pranken in den Boden. Unter allen Gegnern grollt die Erde
     * – dann brechen knorrige Wurzeln hervor, umschlingen die Beine (drei Sekunden gefesselt) und stechen zu.
     */
    public static final class RootGrasp extends BossAttack {
        private final float damage;
        private final List<Vec3> spots = new ArrayList<>();

        public RootGrasp(float damage) {
            super(BossAnims.SPECIAL_1, "root_grasp");
            this.damage = damage;
        }

        @Override
        public int duration() {
            return 64;
        }

        @Override
        public int cooldown() {
            return 230;
        }

        @Override
        public int turnUntil() {
            return 14;
        }

        @Override
        public float weight(DragonBossEntity boss, LivingEntity target, double distance, float angle) {
            return distance < 34 ? 5 : 0;
        }

        @Override
        public void start(ServerLevel level, DragonBossEntity boss, LivingEntity target) {
            this.spots.clear();
        }

        @Override
        public void tick(ServerLevel level, DragonBossEntity boss, LivingEntity target, int tick) {
            if (tick == 4) {
                BossFx.sound(level, boss.position(), ModSounds.DRAGON_ROAR.get(), 5.0f, 0.7f);
            }
            if (tick == 22) {
                BossFx.sound(level, boss.position(), SoundEvents.ROOTED_DIRT_BREAK, 4.0f, 0.5f);
                BossFx.sound(level, boss.position(), SoundEvents.GENERIC_EXPLODE.value(), 2.0f, 0.6f);
                boss.shake(level, 50, 0.5f, 15);
                BossFx.groundDust(level, boss.bodyPoint(0, 0, 5), 4, 50);
                for (LivingEntity e : boss.enemiesAround(boss.position(), 34, 10)) {
                    this.spots.add(new Vec3(e.getX(), e.getY(), e.getZ()));
                }
            }
            if (tick > 22 && tick < 38) {
                // die Erde grollt unter den Gegnern (sie können noch ausweichen)
                for (Vec3 spot : this.spots) {
                    BlockState ground = level.getBlockState(BlockPos.containing(spot.x, spot.y - 0.5, spot.z));
                    if (!ground.isAir()) {
                        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, ground), spot.x, spot.y + 0.1, spot.z, 4,
                                0.8, 0.05, 0.8, 0.05);
                    }
                    if (tick % 5 == 0) {
                        BossFx.sound(level, spot, SoundEvents.ROOTED_DIRT_STEP, 1.5f, 0.5f);
                    }
                }
            }
            if (tick == 38) {
                for (Vec3 spot : this.spots) {
                    this.erupt(level, boss, spot);
                }
            }
        }

        private void erupt(ServerLevel level, DragonBossEntity boss, Vec3 spot) {
            BlockPos feet = BlockPos.containing(spot);
            BlockState roots = ModBlocks.GNARLED_ROOTS.get().defaultBlockState();
            for (Direction dir : Direction.Plane.HORIZONTAL) {
                TemporaryBlocks.place(level, feet.relative(dir), roots, 70);
                if (level.getRandom().nextBoolean()) {
                    TemporaryBlocks.place(level, feet.relative(dir).above(), roots, 60);
                }
            }
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.ROOTED_DIRT.defaultBlockState()), spot.x, spot.y + 0.5,
                    spot.z, 30, 0.6, 0.6, 0.6, 0.15);
            BossFx.sound(level, spot, SoundEvents.MANGROVE_ROOTS_BREAK, 2.0f, 0.5f);
            for (LivingEntity e : boss.enemiesAround(spot, 1.8, 2.5)) {
                if (e instanceof Player player && (player.isCreative() || player.isSpectator())) {
                    continue;
                }
                boss.hurtTarget(level, e, this.damage);
                e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 4));
                e.setDeltaMovement(Vec3.ZERO);
                e.needsSync = true;
            }
        }
    }

    /**
     * Rankensäulen: Verdantis schlägt auf den Boden, und eine Reihe dorniger Wurzelsäulen bricht in einer Linie auf das
     * Ziel zu aus der Erde (in der letzten Phase drei Linien im Fächer). Wer getroffen wird, fliegt hoch.
     */
    public static final class VinePillars extends BossAttack {
        private final float damage;
        private final List<Vec3> dirs = new ArrayList<>();
        private final Set<UUID> hit = new HashSet<>();
        private Vec3 origin = Vec3.ZERO;

        public VinePillars(float damage) {
            super(BossAnims.ERUPT, "vine_pillars");
            this.damage = damage;
        }

        @Override
        public int duration() {
            return 52;
        }

        @Override
        public int cooldown() {
            return 170;
        }

        @Override
        public int turnUntil() {
            return 12;
        }

        @Override
        public float weight(DragonBossEntity boss, LivingEntity target, double distance, float angle) {
            return distance > 6 && distance < 30 && angle < 50 ? 6 : 0;
        }

        @Override
        public void start(ServerLevel level, DragonBossEntity boss, LivingEntity target) {
            this.dirs.clear();
            this.hit.clear();
        }

        @Override
        public void tick(ServerLevel level, DragonBossEntity boss, LivingEntity target, int tick) {
            if (tick == 12) {
                this.origin = boss.bodyPoint(0, 0, 5);
                Vec3 to = target.position().subtract(this.origin).multiply(1, 0, 1);
                Vec3 d = to.lengthSqr() < 1 ? boss.forward() : to.normalize();
                this.dirs.add(d);
                if (boss.getPhase() >= 3) {
                    this.dirs.add(d.yRot(25 * Mth.DEG_TO_RAD));
                    this.dirs.add(d.yRot(-25 * Mth.DEG_TO_RAD));
                }
                BossFx.sound(level, this.origin, SoundEvents.GENERIC_EXPLODE.value(), 2.5f, 0.5f);
                boss.shake(level, 40, 0.5f, 12);
                BossFx.groundDust(level, this.origin, 3, 40);
            }
            if (tick < 13 || tick > 42) {
                return;
            }
            double dist = 3 + (tick - 13) * 1.0;
            for (Vec3 d : this.dirs) {
                Vec3 p = this.origin.add(d.scale(dist));
                int gy = boss.groundY(p.x, p.z);
                BlockPos base = BlockPos.containing(p.x, gy, p.z);
                int height = 2 + level.getRandom().nextInt(3);
                int placed = 0;
                for (int y = 0; y < height; y++) {
                    BlockState state = y == height - 1 ? ModBlocks.BRAMBLE.get().defaultBlockState()
                            : ModBlocks.GNARLED_ROOTS.get().defaultBlockState();
                    // oben zuerst wieder weg (sonst fiele das Gestrüpp ab)
                    if (TemporaryBlocks.place(level, base.above(y), state, 90 - y * 6)) {
                        placed++;
                    }
                }
                if (placed > 0) {
                    level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.ROOTED_DIRT.defaultBlockState()), p.x, gy + 1,
                            p.z, 14, 0.4, 0.4, 0.4, 0.15);
                    if (tick % 2 == 0) {
                        BossFx.sound(level, p, SoundEvents.MANGROVE_ROOTS_BREAK, 1.6f, 0.6f);
                    }
                }
                for (LivingEntity e : boss.enemiesAround(new Vec3(p.x, gy + 1, p.z), 1.7, 3.0)) {
                    if (this.hit.add(e.getUUID())) {
                        boss.hurtTarget(level, e, this.damage);
                        e.push(d.x * 0.3, 1.0, d.z * 0.3);
                        e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 1));
                        e.needsSync = true;
                    }
                }
            }
        }
    }

    /**
     * Giftsporen: Verdantis schüttelt sich, die Blüten auf ihrem Rücken platzen auf – eine grüne Sporenwolke legt sich
     * um sie und vergiftet alles, was darin bleibt (gut acht Sekunden lang).
     */
    public static final class SporeCloud extends BossAttack {
        public SporeCloud() {
            super(BossAnims.RING, "spore_cloud");
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
        public float weight(DragonBossEntity boss, LivingEntity target, double distance, float angle) {
            int close = boss.enemiesAround(boss.position(), 11, 5).size();
            return close >= 2 ? 6 : distance < 10 ? 3 : 0;
        }

        @Override
        public void tick(ServerLevel level, DragonBossEntity boss, LivingEntity target, int tick) {
            Vec3 back = boss.bodyPoint(0, 4.5, -1);
            if (tick < 20) {
                BossFx.burst(level, ParticleTypes.SPORE_BLOSSOM_AIR, back, 8, 2.5, 0.02);
                if (tick % 5 == 0) {
                    BossFx.sound(level, back, SoundEvents.AZALEA_LEAVES_HIT, 2.0f, 0.5f);
                }
                return;
            }
            if (tick == 20) {
                BossFx.sound(level, back, SoundEvents.PUFFER_FISH_BLOW_OUT, 4.0f, 0.4f);
                BossFx.sound(level, back, SoundEvents.SPORE_BLOSSOM_BREAK, 3.0f, 0.5f);
                BossFx.burst(level, ModParticles.BOSS_SPORE.get(), back, 60, 4.0, 0.15);
                AreaEffectCloud cloud = new AreaEffectCloud(level, boss.getX(), boss.getY() + 0.2, boss.getZ());
                cloud.setOwner(boss);
                cloud.setRadius(10.0f);
                cloud.setRadiusOnUse(0.0f);
                cloud.setWaitTime(0);
                cloud.setDuration(170);
                cloud.setRadiusPerTick(-0.02f);
                cloud.setCustomParticle(ModParticles.BOSS_SPORE.get());
                cloud.addEffect(new MobEffectInstance(MobEffects.POISON, 80, 1));
                cloud.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 80, 0));
                level.addFreshEntity(cloud);
            }
            if (tick < 40) {
                BossFx.ring(level, ModParticles.BOSS_SPORE.get(), boss.position(), 2 + (tick - 20) * 0.45, 24, 0.6);
            }
        }
    }

    /**
     * Dornensaat: Verdantis schwebt über dem Nest und schüttelt Samen aus den Flügeln. Wo sie einschlagen, platzen
     * Giftsporen auf und Dornengestrüpp schießt für einige Sekunden aus dem Boden.
     */
    public static final class SeedRain extends BossAttack {
        private final float damage;

        public SeedRain(float damage) {
            super(BossAnims.RAIN, "seed_rain");
            this.damage = damage;
        }

        @Override
        public int duration() {
            return 100;
        }

        @Override
        public int cooldown() {
            return 260;
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
            if (tick == 6) {
                BossFx.sound(level, boss.position(), ModSounds.DRAGON_ROAR.get(), 6.0f, 0.65f);
            }
            if (tick < 16 || tick > 88 || tick % 3 != 0) {
                return;
            }
            Vec3 center = boss.getHome() != null ? Vec3.atCenterOf(boss.getHome()) : boss.position();
            List<LivingEntity> enemies = boss.enemiesAround(center, DragonBossEntity.ARENA_RADIUS, 40);
            Vec3 spot;
            if (!enemies.isEmpty() && boss.getRandom().nextFloat() < 0.7f) {
                LivingEntity victim = enemies.get(boss.getRandom().nextInt(enemies.size()));
                spot = victim.position().add((boss.getRandom().nextDouble() - 0.5) * 7, 0, (boss.getRandom().nextDouble() - 0.5) * 7);
            } else {
                double a = boss.getRandom().nextDouble() * Mth.TWO_PI;
                double r = 4 + boss.getRandom().nextDouble() * 24;
                spot = center.add(Math.cos(a) * r, 0, Math.sin(a) * r);
            }
            Vec3 from = boss.position().add((boss.getRandom().nextDouble() - 0.5) * 8, 2, (boss.getRandom().nextDouble() - 0.5) * 8);
            Vec3 impact = new Vec3(spot.x, boss.groundY(spot.x, spot.z), spot.z);
            Vec3 dir = impact.subtract(from);
            BossProjectile.shoot(level, boss, BossProjectile.Kind.SEED, from, dir.normalize(), 0.9, this.damage);
            BossFx.burst(level, ParticleTypes.SPORE_BLOSSOM_AIR, from, 4, 1.0, 0.02);
        }
    }

    /**
     * Rankendiener: Verdantis stampft auf, und rund um sie und ihre Gegner wachsen Ranklinge aus dem Boden (höchstens
     * sechs zugleich).
     */
    public static final class VineServants extends BossAttack {
        private static final int MAX_ALIVE = 6;

        public VineServants() {
            super(BossAnims.STOMP, "vine_servants");
        }

        @Override
        public int duration() {
            return 44;
        }

        @Override
        public int cooldown() {
            return 480;
        }

        @Override
        public int minPhase() {
            return 2;
        }

        @Override
        public float weight(DragonBossEntity boss, LivingEntity target, double distance, float angle) {
            return alive(boss) < MAX_ALIVE - 2 ? 4 : 0;
        }

        private static int alive(DragonBossEntity boss) {
            return boss.level().getEntitiesOfClass(VinelingEntity.class, boss.getBoundingBox().inflate(70, 30, 70)).size();
        }

        @Override
        public void tick(ServerLevel level, DragonBossEntity boss, LivingEntity target, int tick) {
            if (tick != 20) {
                return;
            }
            Vec3 center = boss.bodyPoint(0, 0, 3);
            BossFx.sound(level, center, SoundEvents.GENERIC_EXPLODE.value(), 3.0f, 0.5f);
            BossFx.sound(level, center, SoundEvents.ROOTED_DIRT_BREAK, 4.0f, 0.4f);
            boss.shake(level, 40, 0.6f, 15);
            BossFx.groundDust(level, center, 5, 50);
            int count = Math.min(MAX_ALIVE - alive(boss), 2 + boss.getPhase());
            List<LivingEntity> enemies = boss.enemiesAround(boss.position(), 40, 12);
            for (int i = 0; i < count; i++) {
                Vec3 near = !enemies.isEmpty() && i % 2 == 0 ? enemies.get(boss.getRandom().nextInt(enemies.size())).position() : boss.position();
                double a = boss.getRandom().nextDouble() * Mth.TWO_PI;
                double r = near == boss.position() ? 7 + boss.getRandom().nextDouble() * 5 : 3 + boss.getRandom().nextDouble() * 3;
                double x = near.x + Math.cos(a) * r;
                double z = near.z + Math.sin(a) * r;
                int y = boss.groundY(x, z);
                VinelingEntity vineling = ModEntities.VINELING.get().create(level, EntitySpawnReason.MOB_SUMMONED);
                if (vineling == null) {
                    continue;
                }
                vineling.snapTo(x, y, z, boss.getRandom().nextFloat() * 360.0f, 0.0f);
                level.addFreshEntity(vineling);
                level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.ROOTED_DIRT.defaultBlockState()), x, y + 0.3, z, 20,
                        0.4, 0.2, 0.4, 0.12);
            }
        }
    }

    /**
     * Heilung aus den Blüten des Hains: Verdantis verwurzelt sich, hebt den Kopf, und aus den Lebensblüten rund um das
     * Nest strömt grünes Licht zu ihr – jede Blüte schenkt ihr Leben und welkt danach. Wer genug Schaden austeilt,
     * unterbricht sie; wer die Blüten zertritt, nimmt ihr die Heilung ganz.
     */
    public static final class BlossomHeal extends BossAttack {
        private static final int CHANNEL = 100;
        private final List<BlockPos> blossoms = new ArrayList<>();
        private float lastHealth;
        private float damageTaken;

        public BlossomHeal() {
            super(BossAnims.SPECIAL_2, "blossom_heal");
        }

        @Override
        public int duration() {
            return CHANNEL + 20;
        }

        @Override
        public int cooldown() {
            return 640;
        }

        @Override
        public int minPhase() {
            return 2;
        }

        @Override
        public float weight(DragonBossEntity boss, LivingEntity target, double distance, float angle) {
            if (boss.getHealth() > boss.getMaxHealth() * 0.62f || !(boss.level() instanceof ServerLevel level)) {
                return 0;
            }
            return findBlossoms(level, boss, 3).isEmpty() ? 0 : 7;
        }

        /** Lebensblüten im Nest (die nächsten zuerst). */
        static List<BlockPos> findBlossoms(ServerLevel level, DragonBossEntity boss, int max) {
            BlockPos home = boss.getHome() != null ? boss.getHome() : boss.blockPosition();
            List<BlockPos> found = new ArrayList<>();
            for (BlockPos pos : BlockPos.betweenClosed(home.offset(-30, -6, -30), home.offset(30, 8, 30))) {
                if (level.getBlockState(pos).is(ModBlocks.LIFE_BLOSSOM.get())) {
                    found.add(pos.immutable());
                }
            }
            found.sort((a, b) -> Double.compare(a.distSqr(boss.blockPosition()), b.distSqr(boss.blockPosition())));
            return found.size() > max ? found.subList(0, max) : found;
        }

        @Override
        public void start(ServerLevel level, DragonBossEntity boss, LivingEntity target) {
            this.blossoms.clear();
            this.blossoms.addAll(findBlossoms(level, boss, 3));
            this.lastHealth = boss.getHealth();
            this.damageTaken = 0;
        }

        @Override
        public void tick(ServerLevel level, DragonBossEntity boss, LivingEntity target, int tick) {
            float lost = this.lastHealth - boss.getHealth();
            if (lost > 0) {
                this.damageTaken += lost;
            }
            Vec3 chest = boss.bodyPoint(0, 3.5, 2);
            if (tick == 4) {
                BossFx.sound(level, chest, SoundEvents.BEACON_POWER_SELECT, 3.0f, 0.6f);
                BossFx.sound(level, chest, ModSounds.DRAGON_AMBIENT.get(), 5.0f, 0.6f);
            }
            if (this.damageTaken > boss.getMaxHealth() * 0.07f && tick < CHANNEL) {
                // unterbrochen: die Hüterin taumelt
                BossFx.sound(level, chest, ModSounds.DRAGON_HURT.get(), 6.0f, 0.5f);
                BossFx.sound(level, chest, SoundEvents.AZALEA_LEAVES_BREAK, 3.0f, 0.5f);
                BossFx.burst(level, ParticleTypes.SMOKE, chest, 30, 2.0, 0.05);
                boss.finishAttack(level);
                return;
            }
            if (tick >= 8 && tick < CHANNEL) {
                boolean any = false;
                for (BlockPos pos : this.blossoms) {
                    if (!level.getBlockState(pos).is(ModBlocks.LIFE_BLOSSOM.get())) {
                        continue;   // zertreten!
                    }
                    any = true;
                    Vec3 from = Vec3.atCenterOf(pos);
                    Vec3 dir = chest.subtract(from);
                    double len = dir.length();
                    // Lichtstrom von der Blüte zur Brust
                    double t0 = (tick * 0.9) % 3.0;
                    for (double t = t0; t < len; t += 3.0) {
                        Vec3 p = from.add(dir.scale(t / len));
                        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, p.x, p.y, p.z, 1, 0.1, 0.1, 0.1, 0.0);
                        level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, p.x, p.y, p.z, 1, 0.15, 0.15, 0.15, 0.0);
                    }
                    level.sendParticles(ParticleTypes.COMPOSTER, from.x, from.y + 0.3, from.z, 2, 0.2, 0.2, 0.2, 0.01);
                    if (tick % 10 == 0) {
                        boss.heal(boss.getMaxHealth() * 0.0028f);
                    }
                }
                if (any && tick % 20 == 8) {
                    BossFx.sound(level, chest, SoundEvents.AMETHYST_BLOCK_CHIME, 2.5f, 0.7f);
                }
            }
            if (tick == CHANNEL) {
                // die ausgeschöpften Blüten welken
                for (BlockPos pos : this.blossoms) {
                    if (level.getBlockState(pos).is(ModBlocks.LIFE_BLOSSOM.get())) {
                        level.setBlock(pos, Blocks.DEAD_BUSH.defaultBlockState(), net.minecraft.world.level.block.Block.UPDATE_ALL);
                        level.sendParticles(ParticleTypes.SMOKE, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 8, 0.2, 0.2, 0.2, 0.01);
                    }
                }
                BossFx.sound(level, chest, ModSounds.DRAGON_ROAR.get(), 6.0f, 0.6f);
                BossFx.burst(level, ParticleTypes.HAPPY_VILLAGER, chest, 40, 3.0, 0.1);
            }
            this.lastHealth = boss.getHealth();
        }

        @Override
        public void stop(ServerLevel level, DragonBossEntity boss) {
            this.blossoms.clear();
        }
    }

    /** Lässt die Lebensblüten im Nest wieder sprießen (beim Erwachen). */
    public static int regrowBlossoms(ServerLevel level, BlockPos home) {
        int grown = 0;
        for (BlockPos spot : io.github.sirschrolli.kingdomsatwar.worldgen.lair.NatureLairDesign.blossomRing(home)) {
            BlockPos pos = spot;
            for (int dy = 4; dy >= -4; dy--) {
                BlockPos p = spot.above(dy);
                BlockState ground = level.getBlockState(p.below());
                BlockState here = level.getBlockState(p);
                if ((here.isAir() || here.canBeReplaced() || here.is(Blocks.DEAD_BUSH)) && here.getFluidState().isEmpty()
                        && (ground.is(Blocks.GRASS_BLOCK) || ground.is(Blocks.MOSS_BLOCK) || ground.is(Blocks.DIRT)
                                || ground.is(Blocks.PODZOL) || ground.is(Blocks.ROOTED_DIRT) || ground.is(Blocks.COARSE_DIRT))) {
                    pos = p;
                    if (!here.is(ModBlocks.LIFE_BLOSSOM.get())) {
                        level.setBlock(pos, ModBlocks.LIFE_BLOSSOM.get().defaultBlockState(), net.minecraft.world.level.block.Block.UPDATE_ALL);
                        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 12, 0.4, 0.4,
                                0.4, 0.02);
                        grown++;
                    }
                    break;
                }
                if (here.is(ModBlocks.LIFE_BLOSSOM.get())) {
                    break;
                }
            }
        }
        return grown;
    }

    /** Wirkung der Sporen auf getroffene Gegner. */
    public static void spores(DragonBossEntity boss, LivingEntity e) {
        BossEffects.poison(e, 4);
    }
}
