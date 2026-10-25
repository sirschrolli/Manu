package io.github.sirschrolli.kingdomsatwar.worldgen;

import com.mojang.serialization.MapCodec;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Eigene Arten von Weltgenerierungs-Features: Drachennester und Burgruinen. */
public final class ModFeatureTypes {
    public static final DeferredRegister<MapCodec<? extends Feature>> FEATURE_TYPES =
            DeferredRegister.create(Registries.FEATURE_TYPE, KingdomsAtWar.MODID);

    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<DragonNestFeature>> DRAGON_NEST =
            FEATURE_TYPES.register("dragon_nest", () -> DragonNestFeature.CODEC);
    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<CastleRuinFeature>> CASTLE_RUIN =
            FEATURE_TYPES.register("castle_ruin", () -> CastleRuinFeature.CODEC);

    private ModFeatureTypes() {}
}
