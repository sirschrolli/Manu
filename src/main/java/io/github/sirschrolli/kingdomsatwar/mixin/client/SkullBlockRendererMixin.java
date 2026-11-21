package io.github.sirschrolli.kingdomsatwar.mixin.client;

import io.github.sirschrolli.kingdomsatwar.client.renderer.OwnHeads;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.object.skull.SkullModelBase;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.world.level.block.SkullBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Die Köpfe von Vanilla (Skelett, Witherskelett, Zombie, Creeper, Piglin, Drache) bekommen die Modelle der eigenen Wesen
 * ({@link OwnHeads}) – überall, wo das Spiel ein Kopfmodell baut: für den Block, den Gegenstand und den getragenen Kopf.
 * NeoForge erlaubt eigene Modelle nur für eigene Kopfarten, darum dieser eine Eingriff.
 */
@Mixin(SkullBlockRenderer.class)
public abstract class SkullBlockRendererMixin {
    @Inject(method = "createModel", at = @At("HEAD"), cancellable = true)
    private static void kingdomsatwar$ownHeads(EntityModelSet models, SkullBlock.Type type, CallbackInfoReturnable<SkullModelBase> cir) {
        SkullModelBase own = OwnHeads.create(models, type);
        if (own != null) {
            cir.setReturnValue(own);
        }
    }
}
