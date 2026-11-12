package io.github.sirschrolli.kingdomsatwar.mixin.client;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.FishingHookRenderer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Die Schnur hängt an der Spitze der Angeln in 3D (seit 1.1.1): Von außen gesehen (andere Spieler, die Ansichten mit F5) setzt
 * Minecraft die Schnur dort an, wo er die Spitze seiner flachen Angel vermutet – ein gutes Stück hinter und über der Spitze der
 * ausgeworfenen Ruten in 3D, so begann die Schnur in der Luft. Für die Angeln mit 3D-Modell (die der Mod und die von Minecraft)
 * beginnt sie jetzt an der Spitze der ausgeworfenen Rute: nachgerechnet über Arm (Haltung ITEM, Spieler 15/16 groß), Hand und
 * die Lage des Modells in der Hand. Aus der eigenen Sicht passte sie schon.
 */
@Mixin(FishingHookRenderer.class)
public abstract class FishingHookRendererMixin {
    /** Die Spitze der ausgeworfenen Rute vom Auge aus (Blöcke, bei Größe 1): zur Seite der Hand, nach unten, nach vorn. */
    private static final double KINGDOMSATWAR_SIDE = 0.35;
    private static final double KINGDOMSATWAR_DOWN = 0.50;
    private static final double KINGDOMSATWAR_FORWARD = 0.93;

    @Inject(method = "getPlayerHandPos", at = @At("HEAD"), cancellable = true)
    private void kingdomsatwar$rodTip(Player owner, float swing, float partialTicks, CallbackInfoReturnable<Vec3> cir) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.options.getCameraType().isFirstPerson() && owner == minecraft.player) {
            return;
        }
        HumanoidArm arm = FishingHookRenderer.getHoldingArm(owner);
        if (!kingdomsatwar$hasModel(owner.getItemHeldByArm(arm))) {
            return;
        }
        int invert = arm == HumanoidArm.RIGHT ? 1 : -1;
        float yaw = Mth.lerp(partialTicks, owner.yBodyRotO, owner.yBodyRot) * Mth.DEG_TO_RAD;
        double sin = Mth.sin(yaw);
        double cos = Mth.cos(yaw);
        float scale = owner.getScale();
        double side = invert * KINGDOMSATWAR_SIDE * scale;
        double forward = KINGDOMSATWAR_FORWARD * scale;
        float crouch = owner.isCrouching() ? -0.1875f : 0.0f;
        cir.setReturnValue(owner.getEyePosition(partialTicks).add(-cos * side - sin * forward, crouch - KINGDOMSATWAR_DOWN * scale,
                -sin * side + cos * forward));
    }

    /** Hat diese Angel ein Modell in 3D (die Angeln der Mod und die von Minecraft)? */
    private static boolean kingdomsatwar$hasModel(ItemStack rod) {
        return rod.getItem() instanceof FishingRodItem && (rod.is(Items.FISHING_ROD)
                || KingdomsAtWar.MODID.equals(BuiltInRegistries.ITEM.getKey(rod.getItem()).getNamespace()));
    }
}
