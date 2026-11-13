package io.github.sirschrolli.kingdomsatwar.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.github.sirschrolli.kingdomsatwar.rules.HardWorld;
import net.minecraft.client.gui.screens.inventory.EnchantmentScreen;
import net.minecraft.network.chat.MutableComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Der Zaubertisch nennt den wahren Preis an Lapislazuli (seit 1.3 doppelt so viel, siehe {@link HardWorld#lapisText}). */
@Mixin(EnchantmentScreen.class)
public abstract class EnchantmentScreenMixin {
    @ModifyExpressionValue(method = "extractRenderState", at = {
            @At(value = "INVOKE", target = "Lnet/minecraft/network/chat/Component;translatable(Ljava/lang/String;)Lnet/minecraft/network/chat/MutableComponent;"),
            @At(value = "INVOKE", target = "Lnet/minecraft/network/chat/Component;translatable(Ljava/lang/String;[Ljava/lang/Object;)Lnet/minecraft/network/chat/MutableComponent;")})
    private MutableComponent kingdomsatwar$lapisPrice(MutableComponent text) {
        return HardWorld.lapisText(text);
    }
}
