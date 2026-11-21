package io.github.sirschrolli.kingdomsatwar.mixin;

import io.github.sirschrolli.kingdomsatwar.urmeer.UrmeerRules;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Im Urmeer lässt sich keine Lava ausgießen (seit 0.99, siehe {@link UrmeerRules}): Der Eimer bleibt voll, die Lava erlischt
 * zischend. Gilt für Spieler und Werfer, denn beide gießen über diese Methode aus.
 */
@Mixin(BucketItem.class)
public abstract class BucketItemMixin {
    @Shadow
    @Final
    public Fluid content;

    @Inject(method = "emptyContents(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/level/Level;"
            + "Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/BlockHitResult;Lnet/minecraft/world/item/ItemStack;)Z",
            at = @At("HEAD"), cancellable = true)
    private void kingdomsatwar$noLavaInUrmeer(@Nullable LivingEntity user, Level level, BlockPos pos, @Nullable BlockHitResult hitResult,
            @Nullable ItemStack containerItem, CallbackInfoReturnable<Boolean> cir) {
        if (this.content.is(FluidTags.LAVA) && UrmeerRules.forbidden(level)) {
            UrmeerRules.quench(user, level, pos);
            cir.setReturnValue(false);
        }
    }
}
