package io.github.sirschrolli.kingdomsatwar.item.armory;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;

/**
 * Draconium-Kriegshammer: schwer, mit glühender Rune im Kopf. Rechte Taste halten (mindestens {@link #CHARGE_TICKS} Ticks)
 * und loslassen: der <b>Erdstoß</b> – der Hammer fährt in den Boden, alle Gegner im Umkreis von {@link #RADIUS} Blöcken
 * nehmen {@link #SLAM_DAMAGE} Schaden, werden hochgeschleudert und fangen Feuer. Danach ruht er {@link #COOLDOWN} Ticks.
 */
public class WarhammerItem extends Item {
    public static final int CHARGE_TICKS = 16;
    public static final double RADIUS = 4.5;
    public static final float SLAM_DAMAGE = 8.0f;
    public static final int BURN_SECONDS = 4;
    public static final int COOLDOWN = 160;
    private static final int USE_DURATION = 72000;

    public WarhammerItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.TRIDENT;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity user) {
        return USE_DURATION;
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remaining) {
        int charged = USE_DURATION - remaining;
        if (level instanceof ServerLevel server) {
            if (charged == CHARGE_TICKS) {
                server.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 0.6f, 0.6f);
            }
            if (charged >= CHARGE_TICKS && charged % 4 == 0) {
                Vec3 at = entity.getEyePosition().add(entity.getLookAngle().scale(0.4)).add(0, 0.6, 0);
                server.sendParticles(ParticleTypes.FLAME, at.x, at.y, at.z, 2, 0.15, 0.15, 0.15, 0.01);
                server.sendParticles(ParticleTypes.SMALL_FLAME, at.x, at.y, at.z, 2, 0.2, 0.2, 0.2, 0.01);
            }
        }
    }

    @Override
    public boolean releaseUsing(ItemStack stack, Level level, LivingEntity entity, int remaining) {
        if (USE_DURATION - remaining < CHARGE_TICKS) {
            return false;
        }
        if (level instanceof ServerLevel server) {
            slam(server, entity);
            if (entity instanceof Player player) {
                player.getCooldowns().addCooldown(stack, COOLDOWN);
            }
            stack.hurtAndBreak(3, entity, entity.getUsedItemHand());
        }
        return true;
    }

    @Override
    public boolean canPerformAction(ItemInstance stack, ItemAbility ability) {
        return ability != ItemAbilities.SWORD_SWEEP && super.canPerformAction(stack, ability);
    }

    /** Der Erdstoß: trifft alle Gegner im Umkreis (auch ohne Ziel), mit Staub, Glut und Donner. Gibt die Getroffenen zurück. */
    public static List<LivingEntity> slam(ServerLevel level, LivingEntity user) {
        Vec3 center = user.position();
        List<LivingEntity> hit = new ArrayList<>();
        AABB box = new AABB(center, center).inflate(RADIUS, 2.5, RADIUS);
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, box, e -> ArmoryCombat.isEnemy(user, e))) {
            Vec3 away = target.position().subtract(center).multiply(1, 0, 1);
            double distance = away.length();
            if (distance > RADIUS) {
                continue;
            }
            if (target.hurtServer(level, ArmoryCombat.source(level, user), SLAM_DAMAGE)) {
                Vec3 dir = distance < 0.1 ? user.getLookAngle().multiply(1, 0, 1).normalize() : away.scale(1.0 / distance);
                double strength = 1.15 * (1.0 - 0.45 * distance / RADIUS);
                target.push(dir.x * strength, 0.55, dir.z * strength);
                if (!target.fireImmune()) {
                    target.igniteForSeconds(BURN_SECONDS);
                }
                hit.add(target);
            }
        }
        effects(level, user);
        return hit;
    }

    private static void effects(ServerLevel level, LivingEntity user) {
        Vec3 c = user.position();
        BlockPos below = BlockPos.containing(c.x, c.y - 0.2, c.z);
        BlockState ground = level.getBlockState(below);
        if (ground.isAir()) {
            ground = net.minecraft.world.level.block.Blocks.DIRT.defaultBlockState();
        }
        BlockParticleOption dust = new BlockParticleOption(ParticleTypes.BLOCK, ground);
        // Ringe aus Erde, Glut und Rauch
        for (int ring = 1; ring <= 4; ring++) {
            double r = RADIUS * ring / 4.0;
            int points = 10 + ring * 6;
            for (int i = 0; i < points; i++) {
                double a = Mth.TWO_PI * i / points;
                double x = c.x + Math.cos(a) * r;
                double z = c.z + Math.sin(a) * r;
                level.sendParticles(dust, x, c.y + 0.1, z, 3, 0.15, 0.05, 0.15, 0.15);
                if (i % 2 == 0) {
                    level.sendParticles(ParticleTypes.FLAME, x, c.y + 0.15, z, 1, 0.1, 0.05, 0.1, 0.03);
                }
                if (ring == 4 && i % 3 == 0) {
                    level.sendParticles(ParticleTypes.LARGE_SMOKE, x, c.y + 0.2, z, 1, 0.1, 0.1, 0.1, 0.02);
                }
            }
        }
        level.sendParticles(ParticleTypes.EXPLOSION, c.x, c.y + 0.3, c.z, 1, 0, 0, 0, 0);
        level.sendParticles(ParticleTypes.LAVA, c.x, c.y + 0.3, c.z, 12, 1.2, 0.2, 1.2, 0.1);
        level.playSound(null, c.x, c.y, c.z, SoundEvents.MACE_SMASH_GROUND_HEAVY, SoundSource.PLAYERS, 1.2f, 0.7f);
        level.playSound(null, c.x, c.y, c.z, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 0.7f, 0.6f);
    }
}
