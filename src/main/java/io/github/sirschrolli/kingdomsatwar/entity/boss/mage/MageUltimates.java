package io.github.sirschrolli.kingdomsatwar.entity.boss.mage;

import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import io.github.sirschrolli.kingdomsatwar.entity.boss.BossEffects;
import io.github.sirschrolli.kingdomsatwar.entity.boss.BossFx;
import io.github.sirschrolli.kingdomsatwar.entity.boss.BossProjectile;
import io.github.sirschrolli.kingdomsatwar.entity.boss.DragonBossEntity;
import io.github.sirschrolli.kingdomsatwar.entity.boss.lightning.LightningAttacks;
import io.github.sirschrolli.kingdomsatwar.entity.boss.nature.VinelingEntity;
import io.github.sirschrolli.kingdomsatwar.registry.ModBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.world.TemporaryBlocks;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

/**
 * Die großen Zauber der Erzmagier (ab der zweiten Phase, in der letzten oft): Jeder Einschlag kündigt sich vorher mit
 * einem Kreis am Boden an – wer aufpasst, weicht aus.
 *
 * <ul>
 *   <li>Feuer: Meteorregen – glühende Brocken stürzen auf die Gegner.</li>
 *   <li>Eis: Blizzard – Schneesturm, der langsam macht und erstarren lässt, dazu fallende Eiszapfen.</li>
 *   <li>Blitz: Gewittersturm – Blitz um Blitz schlägt bei den Gegnern ein.</li>
 *   <li>Natur: Wildwuchs – Ranklinge brechen aus dem Boden, Wurzeln und Dornen halten fest, der Magier heilt sich.</li>
 *   <li>Erde: Felsbeben – Felsbrocken fliegen, zum Schluss bebt der ganze Saal.</li>
 *   <li>Wasser: Mahlstrom – ein Wirbel zieht alle zur Mitte, Wasserblasen schießen umher, am Ende schießt eine Fontäne.</li>
 * </ul>
 */
final class MageUltimates {
    private MageUltimates() {}

    static int duration(DragonType element) {
        return switch (element) {
            case FIRE -> 80;
            case ICE -> 90;
            case LIGHTNING -> 84;
            case NATURE -> 60;
            case EARTH -> 70;
            case WATER -> 90;
        };
    }

    static void tick(ServerLevel level, MageBossEntity mage, LivingEntity target, int t) {
        if (t == 0) {
            BossFx.sound(level, mage.position(), SoundEvents.EVOKER_PREPARE_WOLOLO, 2.4f, 0.5f);
            BossFx.sound(level, mage.position(), SoundEvents.BEACON_POWER_SELECT, 2.0f, 0.6f);
        }
        if (t < 16 && t % 2 == 0) {
            // er sammelt Kraft: ein Wirbel aus Funken um den erhobenen Stab
            Vec3 tip = mage.staffTip();
            for (int k = 0; k < 3; k++) {
                double a = t * 0.5 + k * Mth.TWO_PI / 3;
                level.sendParticles(new DustParticleOptions(mage.element().color(), 1.3f), tip.x + Math.cos(a) * 0.8, tip.y,
                        tip.z + Math.sin(a) * 0.8, 1, 0, 0, 0, 0);
            }
        }
        switch (mage.element()) {
            case FIRE -> meteors(level, mage, t);
            case ICE -> blizzard(level, mage, t);
            case LIGHTNING -> storm(level, mage, t);
            case NATURE -> overgrowth(level, mage, t);
            case EARTH -> quake(level, mage, t);
            case WATER -> maelstrom(level, mage, t);
        }
    }

    /** Ein Gegner des Magiers im Saal (zufällig), sonst null. */
    private static LivingEntity anyEnemy(MageBossEntity mage) {
        List<LivingEntity> enemies = mage.arenaEnemies();
        return enemies.isEmpty() ? null : enemies.get(mage.getRandom().nextInt(enemies.size()));
    }

    /**
     * Startpunkt eines Geschosses, das von {@code wanted} aus auf {@code impact} herabstürzt – im Turm (oder in einer
     * Höhle) knapp unter der Decke, damit es nicht schon im Dach zerschellt.
     */
    private static Vec3 skyFrom(ServerLevel level, MageBossEntity mage, Vec3 impact, Vec3 wanted) {
        Vec3 start = impact.add(0, 0.6, 0);
        net.minecraft.world.phys.BlockHitResult hit = level.clip(new net.minecraft.world.level.ClipContext(start, wanted,
                net.minecraft.world.level.ClipContext.Block.COLLIDER, net.minecraft.world.level.ClipContext.Fluid.NONE, mage));
        if (hit.getType() == net.minecraft.world.phys.HitResult.Type.MISS) {
            return wanted;
        }
        Vec3 dir = wanted.subtract(start).normalize();
        Vec3 from = hit.getLocation().subtract(dir.scale(1.2));
        return from.y - impact.y < 3.0 ? impact.add(0, 3.0, 0) : from;
    }

    /** Stelle beim Gegner (etwas vor ihm in Laufrichtung, mit Streuung), auf dem Boden. */
    private static Vec3 spotNear(MageBossEntity mage, LivingEntity e, double spread) {
        Vec3 lead = e.getDeltaMovement().multiply(6, 0, 6);
        Vec3 at = e.position().add(lead).add((mage.getRandom().nextDouble() - 0.5) * spread, 0, (mage.getRandom().nextDouble() - 0.5) * spread);
        return new Vec3(at.x, mage.groundY(at.x, at.z), at.z);
    }

    // ------------------------------------------------------------------ Feuer

    private static void meteors(ServerLevel level, MageBossEntity mage, int t) {
        if (t < 16 || t > 72 || t % 6 != 0) {
            return;
        }
        LivingEntity e = anyEnemy(mage);
        if (e == null) {
            return;
        }
        Vec3 impact = spotNear(mage, e, 5.0);
        BossFx.warnDisc(level, ParticleTypes.LARGE_SMOKE, impact, 2.2, 12);
        BossFx.ring(level, ParticleTypes.FLAME, impact, 2.6, 16, 0.15);
        Vec3 from = skyFrom(level, mage, impact,
                impact.add((mage.getRandom().nextDouble() - 0.5) * 6, 20, (mage.getRandom().nextDouble() - 0.5) * 6));
        BossProjectile.shoot(level, mage, BossProjectile.Kind.METEOR, from, impact.subtract(from), 1.0, 9.0f);
        if (t % 18 == 0) {
            BossFx.sound(level, mage.position(), SoundEvents.BLAZE_SHOOT, 2.0f, 0.5f);
        }
    }

    // ------------------------------------------------------------------ Eis

    private static void blizzard(ServerLevel level, MageBossEntity mage, int t) {
        Vec3 center = mage.center();
        if (t >= 10 && t % 2 == 0) {
            for (int k = 0; k < 10; k++) {
                double a = mage.getRandom().nextDouble() * Mth.TWO_PI;
                double r = mage.getRandom().nextDouble() * 10.0;
                level.sendParticles(ParticleTypes.SNOWFLAKE, center.x + Math.cos(a) * r, center.y + 1 + mage.getRandom().nextDouble() * 4,
                        center.z + Math.sin(a) * r, 0, Math.cos(a + 1.6) * 0.6, -0.15, Math.sin(a + 1.6) * 0.6, 1.0);
            }
        }
        if (t == 12) {
            BossFx.sound(level, center, SoundEvents.POWDER_SNOW_STEP, 2.5f, 0.5f);
            BossFx.sound(level, center, SoundEvents.ELYTRA_FLYING, 0.8f, 1.6f);
        }
        if (t >= 14 && t % 20 == 0) {
            for (LivingEntity e : mage.enemiesAround(center, 12.0, 6.0)) {
                BossEffects.freeze(e, 2);
                mage.hurtTarget(level, e, 2.0f);
            }
        }
        if (t >= 16 && t <= 80 && t % 7 == 0) {
            LivingEntity e = anyEnemy(mage);
            if (e != null) {
                Vec3 impact = spotNear(mage, e, 3.0);
                BossFx.ring(level, mage.element().particle(), impact, 1.8, 14, 0.15);
                Vec3 from = skyFrom(level, mage, impact, impact.add(0, 16, 0));
                BossProjectile.shoot(level, mage, BossProjectile.Kind.ICICLE, from, impact.subtract(from), 1.1, 7.0f);
            }
        }
    }

    // ------------------------------------------------------------------ Blitz

    private static final int STRIKE_FUSE = 16;

    private static void storm(ServerLevel level, MageBossEntity mage, int t) {
        if (t == 10) {
            BossFx.sound(level, mage.position(), SoundEvents.LIGHTNING_BOLT_THUNDER, 3.0f, 0.7f);
        }
        // alle 8 Ticks ein neuer Einschlag, der STRIKE_FUSE Ticke vorher angekündigt wird
        for (int k = 0; k < 8; k++) {
            int mark = 14 + k * 8;
            if (t == mark) {
                LivingEntity e = anyEnemy(mage);
                mage.strikes[k] = e != null ? spotNear(mage, e, 3.0) : mage.center();
            }
            if (t >= mark && t <= mark + STRIKE_FUSE && mage.strikes[k] != null) {
                Vec3 spot = mage.strikes[k];
                int fuse = mark + STRIKE_FUSE - t;
                if (fuse > 0 && fuse % 2 == 0) {
                    BossFx.ring(level, ParticleTypes.ELECTRIC_SPARK, spot.add(0, 0.1, 0), 0.8 + 1.6 * fuse / STRIKE_FUSE, 14, 0.0);
                }
                if (fuse == 0) {
                    LightningAttacks.visualBolt(level, spot);
                    BossFx.sound(level, spot, SoundEvents.LIGHTNING_BOLT_IMPACT, 2.0f, 1.0f);
                    for (LivingEntity victim : mage.enemiesAround(spot, 2.6, 4.0)) {
                        mage.hurtTarget(level, victim, 8.0f);
                        BossEffects.stun(victim, 1);
                    }
                }
            }
        }
    }

    // ------------------------------------------------------------------ Natur

    private static void overgrowth(ServerLevel level, MageBossEntity mage, int t) {
        if (t == 16) {
            BossFx.sound(level, mage.position(), SoundEvents.ROOTED_DIRT_BREAK, 2.5f, 0.5f);
            int count = mage.phase() >= 3 ? 3 : 2;
            for (int i = 0; i < count; i++) {
                LivingEntity e = anyEnemy(mage);
                Vec3 near = e != null ? e.position() : mage.center();
                double a = mage.getRandom().nextDouble() * Mth.TWO_PI;
                double x = near.x + Math.cos(a) * 3.0;
                double z = near.z + Math.sin(a) * 3.0;
                int y = mage.groundY(x, z);
                VinelingEntity vineling = ModEntities.VINELING.get().create(level, EntitySpawnReason.MOB_SUMMONED);
                if (vineling == null) {
                    continue;
                }
                vineling.snapTo(x, y, z, mage.getRandom().nextFloat() * 360.0f, 0.0f);
                level.addFreshEntity(vineling);
                level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.ROOTED_DIRT.defaultBlockState()), x, y + 0.3, z, 20,
                        0.4, 0.2, 0.4, 0.12);
            }
        }
        if (t == 28) {
            // Wurzeln und Dornen um jeden Gegner
            for (LivingEntity e : mage.enemiesAround(mage.center(), 12.0, 6.0)) {
                e.addEffect(BossEffects.debuff(MobEffects.SLOWNESS, 70, 4), mage);
                BossEffects.poison(e, 3);
                BlockPos base = e.blockPosition();
                for (int dx = -2; dx <= 2; dx++) {
                    for (int dz = -2; dz <= 2; dz++) {
                        int d = Math.abs(dx) + Math.abs(dz);
                        if (d < 2 || d > 3 || mage.getRandom().nextFloat() > 0.6f) {
                            continue;
                        }
                        BlockPos pos = base.offset(dx, 0, dz);
                        if (TemporaryBlocks.canReplace(level, pos) && !level.getBlockState(pos.below()).isAir()) {
                            TemporaryBlocks.place(level, pos, ModBlocks.BRAMBLE.get().defaultBlockState(), 160);
                        }
                    }
                }
                level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.ROOTED_DIRT.defaultBlockState()), e.getX(), e.getY() + 0.2,
                        e.getZ(), 30, 0.6, 0.2, 0.6, 0.1);
            }
            BossFx.sound(level, mage.position(), SoundEvents.SWEET_BERRY_BUSH_PLACE, 2.0f, 0.6f);
            mage.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 2), mage);
            level.sendParticles(ParticleTypes.HAPPY_VILLAGER, mage.getX(), mage.getY(1.0), mage.getZ(), 30, 0.8, 1.0, 0.8, 0.0);
        }
    }

    // ------------------------------------------------------------------ Erde

    private static void quake(ServerLevel level, MageBossEntity mage, int t) {
        if (t == 14 || t == 28 || t == 42) {
            for (LivingEntity e : mage.arenaEnemies()) {
                Vec3 from = mage.staffTip();
                Vec3 to = e.position().add(0, 0.5, 0);
                Vec3 flat = to.subtract(from).multiply(1, 0, 1);
                double dist = flat.length();
                // im Bogen: etwas nach oben, damit der Brocken über Hindernisse fliegt
                Vec3 dir = flat.normalize().scale(0.9).add(0, 0.25 + dist * 0.02, 0);
                BossProjectile.shoot(level, mage, BossProjectile.Kind.BOULDER, from, dir, 0.75 + dist * 0.015, 8.0f);
            }
            BossFx.sound(level, mage.position(), SoundEvents.STONE_BREAK, 2.0f, 0.5f);
        }
        if (t >= 46 && t < 58 && t % 3 == 0) {
            BossFx.ring(level, ParticleTypes.CLOUD, mage.position().add(0, 0.1, 0), 1.0 + (t - 46) * 0.7, 30, 0.0);
        }
        if (t == 58) {
            BossFx.shockwave(level, mage, mage.position(), 9.0, 8.0f, 0.9);
            BossFx.sound(level, mage.position(), SoundEvents.GENERIC_EXPLODE.value(), 2.5f, 0.5f);
            mage.shake(level, 30.0, 0.8f, 24);
        }
    }

    // ------------------------------------------------------------------ Wasser

    private static void maelstrom(ServerLevel level, MageBossEntity mage, int t) {
        Vec3 center = mage.center();
        if (t == 12) {
            BossFx.sound(level, center, SoundEvents.BUBBLE_COLUMN_WHIRLPOOL_INSIDE, 3.0f, 0.6f);
        }
        if (t >= 12 && t < 80) {
            if (t % 2 == 0) {
                for (int k = 0; k < 4; k++) {
                    double a = t * 0.3 + k * Mth.HALF_PI;
                    double r = 9.0 - (t % 20) * 0.4;
                    level.sendParticles(ParticleTypes.SPLASH, center.x + Math.cos(a) * r, center.y + 0.2, center.z + Math.sin(a) * r, 3, 0.2, 0.1,
                            0.2, 0.05);
                    level.sendParticles(ParticleTypes.BUBBLE_POP, center.x + Math.cos(a) * r * 0.6, center.y + 0.3,
                            center.z + Math.sin(a) * r * 0.6, 2, 0.2, 0.1, 0.2, 0.02);
                }
            }
            for (LivingEntity e : mage.enemiesAround(center, 12.0, 5.0)) {
                Vec3 in = center.subtract(e.position()).multiply(1, 0, 1);
                double d = in.length();
                if (d > 0.8) {
                    Vec3 pull = in.normalize().scale(0.06).add(new Vec3(-in.z, 0, in.x).normalize().scale(0.04));
                    e.push(pull.x, 0.0, pull.z);
                    e.needsSync = true;
                }
                if (t % 20 == 0) {
                    mage.hurtTarget(level, e, 2.5f);
                    BossEffects.soak(e, 2);
                }
            }
            if (t % 15 == 0) {
                LivingEntity e = anyEnemy(mage);
                if (e != null) {
                    Vec3 from = mage.staffTip();
                    BossProjectile.shoot(level, mage, BossProjectile.Kind.BUBBLE, from, e.getBoundingBox().getCenter().subtract(from), 0.8, 6.0f);
                }
            }
        }
        if (t == 82) {
            for (int k = 0; k < 10; k++) {
                level.sendParticles(ParticleTypes.SPLASH, center.x, center.y + k * 0.7, center.z, 20, 0.6, 0.2, 0.6, 0.1);
                level.sendParticles(ParticleTypes.BUBBLE_COLUMN_UP, center.x, center.y + k * 0.7, center.z, 8, 0.5, 0.2, 0.5, 0.1);
            }
            for (LivingEntity e : mage.enemiesAround(center, 4.0, 5.0)) {
                mage.hurtTarget(level, e, 7.0f);
                DragonBossEntity.pushAway(e, center, 0.6, 1.2);
            }
            BossFx.sound(level, center, SoundEvents.GENERIC_SPLASH, 3.0f, 0.5f);
        }
    }
}
