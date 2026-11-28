package io.github.sirschrolli.kingdomsatwar.mixin;

import io.github.sirschrolli.kingdomsatwar.enchant.EnchantPower;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Stärkere Verzauberungen von Minecraft (seit 0.87, siehe {@link EnchantPower}): Hier rechnet Minecraft für jeden Schlag,
 * Pfeil und Dreizack aus, was die Verzauberungen der Waffe dazugeben, und wie viel Schutz die Rüstung bietet – für Spieler
 * wie für Wesen, und für die Fähigkeiten der Waffen der Mod gleich mit.
 */
@Mixin(EnchantmentHelper.class)
public abstract class EnchantmentHelperMixin {
    @Inject(method = "modifyDamage", at = @At("RETURN"), cancellable = true)
    private static void kingdomsatwar$strongerDamage(ServerLevel level, ItemStack weapon, Entity victim, DamageSource source, float damage,
            CallbackInfoReturnable<Float> cir) {
        float extra = EnchantPower.extraDamage(weapon, victim, source);
        if (extra > 0.0f) {
            cir.setReturnValue(cir.getReturnValue() + extra);
        }
    }

    @Inject(method = "getDamageProtection", at = @At("RETURN"), cancellable = true)
    private static void kingdomsatwar$strongerProtection(ServerLevel level, LivingEntity victim, DamageSource source, CallbackInfoReturnable<Float> cir) {
        float protection = cir.getReturnValue();
        if (protection > 0.0f) {
            cir.setReturnValue(protection * EnchantPower.PROTECTION);
        }
    }
}
