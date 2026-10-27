package io.github.sirschrolli.kingdomsatwar.entity.dragon;

import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import java.util.List;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Die Odem-Angriffe der sechs Drachenarten.
 *
 * <ul>
 *     <li>Feuer: setzt Gegner in Brand</li>
 *     <li>Eis: friert ein und verlangsamt</li>
 *     <li>Blitz: Schaden mit Blitzschlag am Hauptziel, Gegner leuchten auf</li>
 *     <li>Natur: vergiftet Gegner und heilt Verbündete im Strahl</li>
 *     <li>Erde: schwere Steinbrocken, schleudert zurück und betäubt</li>
 *     <li>Wasser: Wasserstrahl, stößt weg und löscht Feuer</li>
 * </ul>
 * Blöcke werden nicht verändert – der Odem zerstört keine Bauten.
 */
public final class DragonBreath {
    private static final double CONE_COS = Math.cos(Math.toRadians(22));

    private DragonBreath() {}

    static void tick(ServerLevel level, DragonEntity dragon, Vec3 mouth, Vec3 dir, int ticksLeft) {
        DragonType type = dragon.getDragonType();
        double range = dragon.getStage().breathRange();
        spawnParticles(level, type, mouth, dir, range, dragon.getStage().scale());
        if (ticksLeft % 4 != 0) {
            return;
        }
        float damage = (float) (dragon.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE) * 0.35);
        AABB box = new AABB(mouth, mouth.add(dir.scale(range))).inflate(range * 0.35);
        List<LivingEntity> hits = level.getEntitiesOfClass(LivingEntity.class, box, e -> e != dragon && e.isAlive()
                && !dragon.hasPassenger(e) && inCone(mouth, dir, e, range));
        boolean struck = false;
        for (LivingEntity target : hits) {
            boolean ally = isAlly(dragon, target);
            if (type == DragonType.NATURE && ally) {
                target.heal(1.5f);
                continue;
            }
            if (ally) {
                continue;
            }
            target.hurtServer(level, dragon.damageSources().mobAttack(dragon), damage);
            switch (type) {
                case FIRE -> target.igniteForSeconds(5);
                case ICE -> {
                    target.setTicksFrozen(Math.min(target.getTicksRequiredToFreeze() + 120, target.getTicksFrozen() + 40));
                    target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 2), dragon);
                }
                case LIGHTNING -> {
                    target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 100, 0), dragon);
                    if (!struck && ticksLeft % 12 == 0) {
                        struck = true;
                        LightningBolt bolt = EntityTypes.LIGHTNING_BOLT.create(level, EntitySpawnReason.TRIGGERED);
                        if (bolt != null) {
                            bolt.setVisualOnly(true);
                            bolt.snapTo(target.getX(), target.getY(), target.getZ());
                            level.addFreshEntity(bolt);
                            target.hurtServer(level, dragon.damageSources().lightningBolt(), damage * 2.5f);
                        }
                    }
                }
                case NATURE -> target.addEffect(new MobEffectInstance(MobEffects.POISON, 80, 1), dragon);
                case EARTH -> {
                    Vec3 push = target.position().subtract(dragon.position()).normalize();
                    target.knockback(0.9, -push.x, -push.z, dragon.damageSources().mobAttack(dragon), damage);
                    target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 4), dragon);
                }
                case WATER -> {
                    Vec3 push = target.position().subtract(dragon.position()).normalize();
                    target.knockback(1.2, -push.x, -push.z, dragon.damageSources().mobAttack(dragon), damage);
                    target.clearFire();
                }
            }
        }
    }

    private static boolean inCone(Vec3 mouth, Vec3 dir, Entity target, double range) {
        Vec3 to = target.getBoundingBox().getCenter().subtract(mouth);
        double dist = to.length();
        if (dist > range + target.getBbWidth() || dist < 0.01) {
            return dist < 0.01;
        }
        return to.normalize().dot(dir) > CONE_COS || dist < 1.5;
    }

    private static boolean isAlly(DragonEntity dragon, LivingEntity target) {
        LivingEntity owner = dragon.getOwner();
        if (owner == null) {
            return target instanceof DragonEntity;
        }
        if (target == owner) {
            return true;
        }
        if (target instanceof OwnableEntity ownable && ownable.getOwner() == owner) {
            return true;
        }
        return target.isAlliedTo(owner);
    }

    private static void spawnParticles(ServerLevel level, DragonType type, Vec3 mouth, Vec3 dir, double range, float scale) {
        var particle = switch (type) {
            case EARTH -> new BlockParticleOption(ParticleTypes.BLOCK, Blocks.COARSE_DIRT.defaultBlockState());
            default -> type.particle();
        };
        int steps = (int) (range * 1.5);
        for (int i = 1; i <= steps; i++) {
            double d = i / 1.5;
            Vec3 p = mouth.add(dir.scale(d));
            double spread = 0.1 + d * 0.18;
            level.sendParticles(particle, p.x, p.y, p.z, 2, spread, spread, spread, type == DragonType.FIRE ? 0.02 : 0.05);
        }
        if (type == DragonType.FIRE) {
            level.sendParticles(ParticleTypes.LARGE_SMOKE, mouth.x, mouth.y, mouth.z, 1, 0.1 * scale, 0.1 * scale, 0.1 * scale, 0.01);
        } else if (type == DragonType.WATER) {
            level.sendParticles(ParticleTypes.BUBBLE, mouth.x, mouth.y, mouth.z, 3, 0.2, 0.2, 0.2, 0.05);
        } else if (type == DragonType.NATURE) {
            level.sendParticles(ParticleTypes.HAPPY_VILLAGER, mouth.x, mouth.y, mouth.z, 2, 0.3, 0.3, 0.3, 0.0);
        }
    }
}
