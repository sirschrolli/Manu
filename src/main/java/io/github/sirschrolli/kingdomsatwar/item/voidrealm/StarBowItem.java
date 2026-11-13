package io.github.sirschrolli.kingdomsatwar.item.voidrealm;

import io.github.sirschrolli.kingdomsatwar.entity.voidrealm.StarArrowEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/**
 * Der Sternenbogen (seit 0.99), die Beute der Sternenweberin: zwei Arme aus Himmelseisen, gespannt mit einer Sehne aus
 * Sternenseide. Seine Pfeile sind schwerelos – sie fliegen schneller als gewöhnliche und schnurgerade, mit einer Spur aus
 * Sternenlicht (siehe {@link StarArrowEntity}); ganz gespannt und schleichend geschossen wird daraus ein Netzpfeil, der sein
 * Ziel einspinnt (dann ruht der Bogen {@link #WEB_COOLDOWN} Ticks). Er lässt sich wie jeder Bogen verzaubern.
 */
public class StarBowItem extends BowItem {
    /** So viel schneller fliegen seine Pfeile als die eines gewöhnlichen Bogens. */
    public static final float SPEED = 1.3f;
    /** Ruhezeit nach einem Netzpfeil (Ticks). */
    public static final int WEB_COOLDOWN = 160;

    public StarBowItem(Properties properties) {
        super(properties);
    }

    @Override
    protected Projectile createProjectile(Level level, LivingEntity shooter, ItemStack weapon, ItemStack projectile, boolean isCrit) {
        StarArrowEntity arrow = new StarArrowEntity(level, shooter, projectile.copyWithCount(1), weapon);
        if (isCrit) {
            arrow.setCritArrow(true);
            if (shooter instanceof Player player && player.isShiftKeyDown() && !player.getCooldowns().isOnCooldown(weapon)) {
                arrow.setWeb(true);
                player.getCooldowns().addCooldown(weapon, WEB_COOLDOWN);
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 1.0f,
                        1.4f);
            }
        }
        return arrow;
    }

    @Override
    protected void shootProjectile(LivingEntity shooter, Projectile projectile, int index, float power, float uncertainty, float angle,
            @Nullable LivingEntity targetOverride) {
        projectile.shootFromRotation(shooter, shooter.getXRot(), shooter.getYRot() + angle, 0.0f, power * SPEED, uncertainty * 0.5f);
    }

    @Override
    public boolean releaseUsing(ItemStack stack, Level level, LivingEntity entity, int remainingTime) {
        boolean shot = super.releaseUsing(stack, level, entity, remainingTime);
        if (shot && level instanceof ServerLevel server) {
            server.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.8f, 1.6f);
        }
        return shot;
    }
}
