package io.github.sirschrolli.kingdomsatwar.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.sirschrolli.kingdomsatwar.rules.HardWorld;
import net.minecraft.world.inventory.EnchantmentMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * Teurere Verzauberung (seit 1.3, siehe {@link HardWorld#LAPIS}): Der Zaubertisch will doppelt so viel Lapislazuli – die
 * Erfahrungsstufen, die er kostet, bleiben dieselben.
 */
@Mixin(EnchantmentMenu.class)
public abstract class EnchantmentMenuMixin {
    /** Ob genug daliegt: in Ladungen zu je zwei Lapislazuli gezählt. */
    @ModifyExpressionValue(method = "clickMenuButton", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getCount()I"))
    private int kingdomsatwar$lapisLoads(int count) {
        return HardWorld.lapisUnits(count);
    }

    /** Was genommen wird: doppelt so viel wie die Stufe der Wahl. */
    @ModifyArg(method = "lambda$clickMenuButton$0", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/item/ItemStack;consume(ILnet/minecraft/world/entity/LivingEntity;)V"), index = 0)
    private int kingdomsatwar$lapisTaken(int cost) {
        return HardWorld.lapisCost(cost);
    }

    /** Was der Tisch anzeigt (welche Wahl möglich ist): ebenfalls in Ladungen. */
    @ModifyReturnValue(method = "getGoldCount", at = @At("RETURN"))
    private int kingdomsatwar$lapisShown(int count) {
        return HardWorld.lapisUnits(count);
    }
}
