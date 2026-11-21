package io.github.sirschrolli.kingdomsatwar.item.armory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.AttackRange;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;

/**
 * Stahl-Hellebarde: eine lange Stange mit Axtblatt, Haken und Stoßklinge. Sie reicht {@link #REACH} Blöcke weit (ein Schwert
 * drei) und <b>durchbohrt</b>: Ein voller Schlag trifft auch bis zu {@link #EXTRA_TARGETS} Gegner, die dahinter in derselben
 * Richtung stehen (schmaler Kegel, {@link #CONE_DEGREES}°), mit {@link #PIERCE_FACTOR} des Schadens. Sie schwingt nicht
 * seitlich wie ein Schwert.
 */
public class HalberdItem extends Item {
    public static final float REACH = 4.5f;
    public static final AttackRange RANGE = new AttackRange(0.0f, REACH, 0.0f, 6.5f, 0.125f, 1.0f);
    public static final int EXTRA_TARGETS = 2;
    public static final float PIERCE_FACTOR = 0.6f;
    public static final double CONE_DEGREES = 22.0;
    private static final double CONE_COS = Math.cos(Math.toRadians(CONE_DEGREES));

    public HalberdItem(Properties properties) {
        super(properties);
    }

    @Override
    public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        super.hurtEnemy(stack, target, attacker);
        if (attacker.level() instanceof ServerLevel level && ArmoryCombat.fullSwing(attacker)) {
            pierce(level, target, attacker);
        }
    }

    @Override
    public boolean canPerformAction(ItemInstance stack, ItemAbility ability) {
        return ability != ItemAbilities.SWORD_SWEEP && super.canPerformAction(stack, ability);
    }

    /** Die Gegner hinter dem Ziel in Blickrichtung (die nächsten zuerst). */
    public static List<LivingEntity> behind(ServerLevel level, LivingEntity target, LivingEntity attacker) {
        Vec3 eye = attacker.getEyePosition();
        Vec3 look = attacker.getLookAngle();
        double reach = REACH + 1.0;
        AABB box = new AABB(eye, eye.add(look.scale(reach))).inflate(1.5);
        List<LivingEntity> found = new ArrayList<>();
        for (LivingEntity other : level.getEntitiesOfClass(LivingEntity.class, box, e -> e != target && ArmoryCombat.isEnemy(attacker, e))) {
            Vec3 to = other.getBoundingBox().getCenter().subtract(eye);
            double distance = to.length();
            if (distance < 0.5 || distance > reach) {
                continue;
            }
            if (to.normalize().dot(look) >= CONE_COS) {
                found.add(other);
            }
        }
        found.sort(Comparator.comparingDouble(e -> e.distanceToSqr(eye)));
        return found.size() > EXTRA_TARGETS ? found.subList(0, EXTRA_TARGETS) : found;
    }

    /** Durchbohren: Schaden, ein kleiner Stoß nach hinten, Funken entlang der Stoßlinie. */
    public static List<LivingEntity> pierce(ServerLevel level, LivingEntity target, LivingEntity attacker) {
        List<LivingEntity> hit = new ArrayList<>();
        Vec3 look = attacker.getLookAngle();
        float damage = ArmoryCombat.share(attacker, PIERCE_FACTOR);
        Vec3 from = target.getBoundingBox().getCenter();
        for (LivingEntity other : behind(level, target, attacker)) {
            var source = ArmoryCombat.source(level, attacker);
            if (other.hurtServer(level, source, damage)) {
                other.knockback(0.35, -look.x, -look.z, source, damage);
                hit.add(other);
            }
            Vec3 to = other.getBoundingBox().getCenter();
            for (int i = 1; i <= 6; i++) {
                Vec3 p = from.lerp(to, i / 7.0);
                level.sendParticles(ParticleTypes.CRIT, p.x, p.y, p.z, 2, 0.05, 0.05, 0.05, 0.05);
            }
            from = to;
        }
        if (!hit.isEmpty()) {
            level.playSound(null, attacker.getX(), attacker.getY(), attacker.getZ(), SoundEvents.TRIDENT_HIT, SoundSource.PLAYERS, 0.8f, 1.3f);
        }
        return hit;
    }
}
