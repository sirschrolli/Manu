package io.github.sirschrolli.kingdomsatwar.item.arena;

import io.github.sirschrolli.kingdomsatwar.entity.arena.StormTridentEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.Level;

/**
 * Der Sturmdreizack (seit 1.9), die Waffe Ventors, des Gladiators der Winde: im Nahkampf ein schwerer Dreizack. Hält man ihn
 * (mindestens eine halbe Sekunde) und lässt los, fliegt er davon – wo er aufschlägt, entfesselt er eine Böe (wer nah steht,
 * nimmt Schaden und fliegt) –, und er kehrt von selbst in die Hand zurück, auch ohne Treue.
 */
public class StormTridentItem extends Item {
    public static final int THROW_THRESHOLD = 10;
    public static final float SHOOT_POWER = 2.5f;

    public StormTridentItem(Properties properties) {
        super(properties);
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.TRIDENT;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity user) {
        return 72000;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.nextDamageWillBreak()) {
            return InteractionResult.FAIL;
        }
        player.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }

    @Override
    public boolean releaseUsing(ItemStack stack, Level level, LivingEntity entity, int remainingTime) {
        if (!(entity instanceof Player player) || this.getUseDuration(stack, entity) - remainingTime < THROW_THRESHOLD
                || stack.nextDamageWillBreak()) {
            return false;
        }
        player.awardStat(Stats.ITEM_USED.get(this));
        if (level instanceof ServerLevel server) {
            stack.hurtWithoutBreaking(1, player);
            ItemStack thrown = stack.consumeAndReturn(1, player);
            StormTridentEntity trident = Projectile.spawnProjectileFromRotation(StormTridentEntity::new, server, thrown, player, 0.0f, SHOOT_POWER,
                    1.0f);
            if (player.hasInfiniteMaterials()) {
                trident.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
            }
            level.playSound(null, trident, SoundEvents.TRIDENT_THROW.value(), SoundSource.PLAYERS, 1.0f, 0.9f);
        }
        return true;
    }
}
