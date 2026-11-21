package io.github.sirschrolli.kingdomsatwar.mixin;

import io.github.sirschrolli.kingdomsatwar.urmeer.UrmeerRules;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SpongeBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Im Urmeer legt kein Schwamm das Meer trocken (seit 0.99, siehe {@link UrmeerRules#soak}): Er saugt sich voll, aber das Wasser
 * bleibt.
 */
@Mixin(SpongeBlock.class)
public abstract class SpongeBlockMixin {
    @Inject(method = "tryAbsorbWater(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)V", at = @At("HEAD"), cancellable = true)
    private void kingdomsatwar$noDrainingTheUrmeer(Level level, BlockPos pos, CallbackInfo ci) {
        if (UrmeerRules.forbidden(level)) {
            UrmeerRules.soak(level, pos);
            ci.cancel();
        }
    }
}
