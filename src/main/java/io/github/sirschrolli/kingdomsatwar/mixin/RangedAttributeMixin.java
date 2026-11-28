package io.github.sirschrolli.kingdomsatwar.mixin;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Heldenrüstung (seit 0.78): Minecraft deckelt die Rüstung bei 30 Punkten. Mit Ausrüstung des Schöpfers und dem Talent
 * Zähigkeit kommt ein Held weit darüber – bis {@value #ARMOR_CAP} Punkte zählen jetzt (die Rüstungsleiste zeigt sie in
 * Schichten). Mehr Rüstung hilft vor allem gegen gewaltige Treffer: Wie viel ein Treffer abgeschwächt wird, deckelt
 * Minecraft weiter bei vier Fünfteln.
 */
@Mixin(RangedAttribute.class)
public abstract class RangedAttributeMixin {
    @Unique
    private static final double ARMOR_CAP = 60.0;
    @Unique
    private Boolean kingdomsatwar$armor;

    @Inject(method = "sanitizeValue", at = @At("HEAD"), cancellable = true)
    private void kingdomsatwar$heroArmor(double value, CallbackInfoReturnable<Double> cir) {
        if (this.kingdomsatwar$armor == null) {
            this.kingdomsatwar$armor = "attribute.name.armor".equals(((Attribute) (Object) this).getDescriptionId());
        }
        if (this.kingdomsatwar$armor) {
            cir.setReturnValue(Double.isNaN(value) ? 0.0 : Mth.clamp(value, 0.0, ARMOR_CAP));
        }
    }

    @Inject(method = "getMaxValue", at = @At("HEAD"), cancellable = true)
    private void kingdomsatwar$heroArmorMax(CallbackInfoReturnable<Double> cir) {
        if (this.kingdomsatwar$armor == null) {
            this.kingdomsatwar$armor = "attribute.name.armor".equals(((Attribute) (Object) this).getDescriptionId());
        }
        if (this.kingdomsatwar$armor) {
            cir.setReturnValue(ARMOR_CAP);
        }
    }
}
