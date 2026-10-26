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

    /** Bauplan: welches Gebäude und in welcher Drehung (0–3, im Uhrzeigersinn) es aufgestellt wird. */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<io.github.sirschrolli.kingdomsatwar.economy.town.BuildingType>>
            BUILDING = COMPONENTS.registerComponentType("building", builder -> builder
                    .persistent(io.github.sirschrolli.kingdomsatwar.economy.town.BuildingType.CODEC)
                    .networkSynchronized(net.minecraft.network.codec.ByteBufCodecs.idMapper(
                            i -> io.github.sirschrolli.kingdomsatwar.economy.town.BuildingType.values()[i], Enum::ordinal)));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> BUILD_TURN =
            COMPONENTS.registerComponentType("build_turn", builder -> builder
                    .persistent(com.mojang.serialization.Codec.INT)
                    .networkSynchronized(net.minecraft.network.codec.ByteBufCodecs.VAR_INT));

    private ModDataComponents() {}
}
