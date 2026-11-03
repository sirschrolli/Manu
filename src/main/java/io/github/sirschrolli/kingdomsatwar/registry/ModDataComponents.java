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
    /** Bauplan: welcher der Entwürfe des Gebäudes gebaut wird (beim Nehmen zufällig gewählt). */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> BUILD_DESIGN =
            COMPONENTS.registerComponentType("build_design", builder -> builder
                    .persistent(com.mojang.serialization.Codec.INT)
                    .networkSynchronized(net.minecraft.network.codec.ByteBufCodecs.VAR_INT));

    /** Meisterschaft eines Aetherit-Stabs (gesammelte Erfahrung beim Zaubern). */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> STAFF_MASTERY =
            COMPONENTS.registerComponentType("staff_mastery", builder -> builder
                    .persistent(com.mojang.serialization.Codec.intRange(0, Integer.MAX_VALUE))
                    .networkSynchronized(net.minecraft.network.codec.ByteBufCodecs.VAR_INT));

    /** Bausatz einer Belagerungswaffe: Zustand der zerlegten Waffe (0–1, fehlt: unversehrt). */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Float>> SIEGE_CONDITION =
            COMPONENTS.registerComponentType("siege_condition", builder -> builder
                    .persistent(com.mojang.serialization.Codec.floatRange(0.0f, 1.0f))
                    .networkSynchronized(net.minecraft.network.codec.ByteBufCodecs.FLOAT));

    /** Aetherit-Stab: welche Zauber er auf welcher Stufe kennt und welcher gewählt ist. */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<io.github.sirschrolli.kingdomsatwar.magic.KnownSpells>> SPELLS =
            COMPONENTS.registerComponentType("spells", builder -> builder
                    .persistent(io.github.sirschrolli.kingdomsatwar.magic.KnownSpells.CODEC)
                    .networkSynchronized(io.github.sirschrolli.kingdomsatwar.magic.KnownSpells.STREAM_CODEC));

    private ModDataComponents() {}
}
