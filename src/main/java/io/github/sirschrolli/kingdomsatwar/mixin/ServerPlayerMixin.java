package io.github.sirschrolli.kingdomsatwar.mixin;

import io.github.sirschrolli.kingdomsatwar.rules.WeaponPower;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Verzauberungen im Nahkampf (seit 0.79): Was Schärfe, Bann und Nemesis einem Schlag dazugeben, wächst auf den Waffen der
 * Mod mit deren Stärke – siehe {@link WeaponPower}. Minecraft rechnet den Betrag hier aus; nur an dieser einen Stelle lässt
 * er sich anpassen, ohne den Angriff selbst anzufassen.
 */
@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin {
    @Inject(method = "getEnchantedDamage", at = @At("RETURN"), cancellable = true)
    private void kingdomsatwar$enchantedDamage(Entity entity, float damage, DamageSource source, CallbackInfoReturnable<Float> cir) {
        ServerPlayer self = (ServerPlayer) (Object) this;
        float scaled = WeaponPower.melee(self.getWeaponItem(), damage, cir.getReturnValue());
        if (scaled != cir.getReturnValue()) {
            cir.setReturnValue(scaled);
        }
    }
}
