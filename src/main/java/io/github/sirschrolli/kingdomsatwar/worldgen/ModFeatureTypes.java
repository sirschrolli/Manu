package io.github.sirschrolli.kingdomsatwar.worldgen;

import com.mojang.serialization.MapCodec;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Eigene Arten von Weltgenerierungs-Features (Burgruinen) und Platzierungen (mit Abstand). */
public final class ModFeatureTypes {
    public static final DeferredRegister<MapCodec<? extends Feature>> FEATURE_TYPES =
            DeferredRegister.create(Registries.FEATURE_TYPE, KingdomsAtWar.MODID);

    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<CastleRuinFeature>> CASTLE_RUIN =
            FEATURE_TYPES.register("castle_ruin", () -> CastleRuinFeature.CODEC);

    public static final DeferredRegister<MapCodec<? extends PlacementModifier>> PLACEMENT_TYPES =
            DeferredRegister.create(Registries.PLACEMENT_MODIFIER_TYPE, KingdomsAtWar.MODID);

    /** Bauwerke mit Mindestabstand zueinander und zu den Drachennestern. */
    public static final DeferredHolder<MapCodec<? extends PlacementModifier>, MapCodec<SpacedPlacement>> SPACED =
            PLACEMENT_TYPES.register("spaced", () -> SpacedPlacement.CODEC);

    private ModFeatureTypes() {}
}
