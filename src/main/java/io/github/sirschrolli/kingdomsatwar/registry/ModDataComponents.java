package io.github.sirschrolli.kingdomsatwar.registry;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.item.DragonCompassItem;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Eigene Datenkomponenten an Gegenständen. */
public final class ModDataComponents {
    public static final DeferredRegister.DataComponents COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, KingdomsAtWar.MODID);

    /** Drachenkompass: welches Nest er sucht und ob dessen Drache schon besiegt ist. */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<DragonCompassItem.Target>> COMPASS_TARGET =
            COMPONENTS.registerComponentType("compass_target", builder -> builder
                    .persistent(DragonCompassItem.Target.CODEC)
                    .networkSynchronized(DragonCompassItem.Target.STREAM_CODEC));

    private ModDataComponents() {}
}
