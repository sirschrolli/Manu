package io.github.sirschrolli.kingdomsatwar.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Hud;
import net.minecraft.network.chat.Component;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fStack;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Der große Titel in der Bildmitte (mit dem sich die Bosse vorstellen) und sein Untertitel werden nur so groß gezeichnet, dass
 * sie auf den Bildschirm passen: Lange Namen wie „Quetzara, die Gefiederte Schlange“ ragten bei großer Oberfläche sonst über
 * beide Ränder hinaus. Kurze Titel bleiben, wie sie sind.
 */
@Mixin(Hud.class)
public abstract class HudTitleMixin {
    @Shadow
    private @Nullable Component title;
    @Shadow
    private @Nullable Component subtitle;

    @WrapOperation(method = "extractTitle", at = @At(value = "INVOKE", target = "Lorg/joml/Matrix3x2fStack;scale(FF)Lorg/joml/Matrix3x2f;",
            ordinal = 0))
    private Matrix3x2f kingdomsatwar$fitTitle(Matrix3x2fStack pose, float x, float y, Operation<Matrix3x2f> original) {
        float scale = kingdomsatwar$fit(this.title, x);
        return original.call(pose, scale, scale);
    }

    @WrapOperation(method = "extractTitle", at = @At(value = "INVOKE", target = "Lorg/joml/Matrix3x2fStack;scale(FF)Lorg/joml/Matrix3x2f;",
            ordinal = 1))
    private Matrix3x2f kingdomsatwar$fitSubtitle(Matrix3x2fStack pose, float x, float y, Operation<Matrix3x2f> original) {
        float scale = kingdomsatwar$fit(this.subtitle, x);
        return original.call(pose, scale, scale);
    }

    /** Höchstens {@code scale}, aber nie breiter als der Bildschirm (mit etwas Rand); nie kleiner als einfach. */
    @Unique
    private static float kingdomsatwar$fit(@Nullable Component text, float scale) {
        if (text == null) {
            return scale;
        }
        Minecraft minecraft = Minecraft.getInstance();
        int width = minecraft.font.width(text);
        if (width <= 0) {
            return scale;
        }
        float fits = (minecraft.getWindow().getGuiScaledWidth() - 16) / (float) width;
        return Math.max(1.0f, Math.min(scale, fits));
    }
}
