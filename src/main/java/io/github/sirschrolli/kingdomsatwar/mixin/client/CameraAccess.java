package io.github.sirschrolli.kingdomsatwar.mixin.client;

import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Das Sichtfeld, mit dem die Hand gezeichnet wird (für die Schnur an der Rutenspitze, siehe RodTip). */
@Mixin(Camera.class)
public interface CameraAccess {
    @Accessor("hudFov")
    float kingdomsatwar$hudFov();
}
