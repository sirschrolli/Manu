package io.github.sirschrolli.kingdomsatwar.mixin.client;

import io.github.sirschrolli.kingdomsatwar.client.renderer.RodTip;
import net.minecraft.client.renderer.entity.FishingHookRenderer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Die Schnur hängt an der Spitze der Angeln in 3D: Minecraft setzt sie dort an, wo es die Spitze seiner flachen Angel
 * vermutet – bei den Ruten in 3D begann sie daneben in der Luft (von außen seit 1.1.1 an einer festen Stelle, die beim Gehen
 * nicht mitging; aus der eigenen Sicht ein Stück über der Spitze, bei anderem Seitenverhältnis weit daneben). Seit 1.1.3
 * rechnet {@link RodTip} die Spitze in jeder Sicht genau nach.
 */
@Mixin(FishingHookRenderer.class)
public abstract class FishingHookRendererMixin {
    @Inject(method = "getPlayerHandPos", at = @At("HEAD"), cancellable = true)
    private void kingdomsatwar$rodTip(Player owner, float swing, float partialTicks, CallbackInfoReturnable<Vec3> cir) {
        HumanoidArm arm = FishingHookRenderer.getHoldingArm(owner);
        if (!RodTip.hasModel(owner.getItemHeldByArm(arm))) {
            return;
        }
        Vec3 tip = RodTip.tip(owner, arm, partialTicks);
        if (tip != null) {
            cir.setReturnValue(tip);
        }
    }
}
