package io.github.sirschrolli.kingdomsatwar.mixin.client;

import net.minecraft.client.player.FirstPersonHandsAndItems;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Wie weit die Hände aus der eigenen Sicht gerade gehoben sind (für die Schnur an der Rutenspitze, siehe RodTip). */
@Mixin(FirstPersonHandsAndItems.class)
public interface FirstPersonHandsAccess {
    @Accessor("mainHandHeight")
    float kingdomsatwar$mainHandHeight();

    @Accessor("oMainHandHeight")
    float kingdomsatwar$oMainHandHeight();

    @Accessor("offHandHeight")
    float kingdomsatwar$offHandHeight();

    @Accessor("oOffHandHeight")
    float kingdomsatwar$oOffHandHeight();
}
