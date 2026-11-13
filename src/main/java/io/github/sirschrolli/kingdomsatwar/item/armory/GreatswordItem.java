package io.github.sirschrolli.kingdomsatwar.item.armory;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;

/**
 * Abyssit-Großschwert: lang, schwer und langsam, mit glühender Rune und Kristallen an der Parierstange. Ein voller Schlag
 * <b>spaltet</b>: Er trifft alles im Halbkreis vor dem Träger (bis {@link #RADIUS} Blöcke, {@link #HALF_ANGLE}° zu jeder
 * Seite) mit {@link #CLEAVE_FACTOR} des Schadens. Es schwingt nicht wie ein Schwert – der Hieb ersetzt den Schwung.
 */
public class GreatswordItem extends Item {
    public static final double RADIUS = 3.5;
    public static final double HALF_ANGLE = 70.0;
    public static final float CLEAVE_FACTOR = 0.75f;
    private static final double HALF_COS = Math.cos(Math.toRadians(HALF_ANGLE));

    public GreatswordItem(Properties properties) {
        super(properties);
    }

    @Override
    public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        super.hurtEnemy(stack, target, attacker);
        if (attacker.level() instanceof ServerLevel level && ArmoryCombat.fullSwing(attacker)) {
            cleave(level, target, attacker);
        }
    }

    @Override
    public boolean canPerformAction(ItemInstance stack, ItemAbility ability) {
        return ability != ItemAbilities.SWORD_SWEEP && super.canPerformAction(stack, ability);
    }

    /** Alle Gegner im Halbkreis vor dem Träger (ohne das eigentliche Ziel). */
    public static List<LivingEntity> arc(ServerLevel level, LivingEntity target, LivingEntity attacker) {
        Vec3 from = attacker.position();
        Vec3 look = attacker.getLookAngle().multiply(1, 0, 1);
        if (look.lengthSqr() < 1.0e-4) {
            look = Vec3.directionFromRotation(0, attacker.getYRot());
        }
        Vec3 dir = look.normalize();
        List<LivingEntity> found = new ArrayList<>();
        AABB box = new AABB(from, from).inflate(RADIUS + 1.0, 1.5, RADIUS + 1.0);
        for (LivingEntity other : level.getEntitiesOfClass(LivingEntity.class, box, e -> e != target && ArmoryCombat.isEnemy(attacker, e))) {
            Vec3 to = other.position().subtract(from).multiply(1, 0, 1);
            double distance = to.length() - other.getBbWidth() / 2.0;
            if (distance > RADIUS || Math.abs(other.getY() - attacker.getY()) > 2.0) {
                continue;
            }
            if (to.lengthSqr() < 0.04 || to.normalize().dot(dir) >= HALF_COS) {
                found.add(other);
            }
        }
        return found;
    }

    /** Der Spaltschlag: Nebentreffer, ein Bogen aus Funken und Seelenfeuer. */
    public static List<LivingEntity> cleave(ServerLevel level, LivingEntity target, LivingEntity attacker) {
        List<LivingEntity> hit = new ArrayList<>();
        float damage = ArmoryCombat.share(attacker, CLEAVE_FACTOR);
        Vec3 look = attacker.getLookAngle();
        for (LivingEntity other : arc(level, target, attacker)) {
            var source = ArmoryCombat.source(level, attacker);
            if (other.hurtServer(level, source, damage)) {
                Vec3 away = other.position().subtract(attacker.position()).multiply(1, 0, 1);
                Vec3 push = away.lengthSqr() < 1.0e-4 ? look : away.normalize();
                other.knockback(0.5, -push.x, -push.z, source, damage);
                hit.add(other);
            }
        }
        // der Bogen des Hiebs
        float yaw = attacker.getYRot() * Mth.DEG_TO_RAD;
        double y = attacker.getY(0.55);
        for (int i = -7; i <= 7; i++) {
            double a = yaw + Math.toRadians(HALF_ANGLE) * i / 7.0;
            double r = RADIUS * 0.75;
            double x = attacker.getX() - Math.sin(a) * r;
            double z = attacker.getZ() + Math.cos(a) * r;
            level.sendParticles(ParticleTypes.SWEEP_ATTACK, x, y, z, 0, 0, 0, 0, 0);
            level.sendParticles(ParticleTypes.REVERSE_PORTAL, x, y, z, 3, 0.1, 0.15, 0.1, 0.02);
        }
        level.playSound(null, attacker.getX(), attacker.getY(), attacker.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0f, 0.6f);
        if (!hit.isEmpty()) {
            level.playSound(null, attacker.getX(), attacker.getY(), attacker.getZ(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS,
                    0.8f, 0.5f);
        }
        return hit;
    }
}
