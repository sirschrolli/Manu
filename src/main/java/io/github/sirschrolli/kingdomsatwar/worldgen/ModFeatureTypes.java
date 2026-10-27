package io.github.sirschrolli.kingdomsatwar.worldgen;

import com.mojang.serialization.MapCodec;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import net.minecraft.core.registries.Registries;
import io.github.sirschrolli.kingdomsatwar.worldgen.overworld.KingdomBiomeSource;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Eigene Arten von Weltgenerierungs-Features (Burgruinen, Bäume und Felsen der eigenen Biome), Platzierungen und Biomverteilungen. */
public final class ModFeatureTypes {
    public static final DeferredRegister<MapCodec<? extends Feature>> FEATURE_TYPES =
            DeferredRegister.create(Registries.FEATURE_TYPE, KingdomsAtWar.MODID);

    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<CastleRuinFeature>> CASTLE_RUIN =
            FEATURE_TYPES.register("castle_ruin", () -> CastleRuinFeature.CODEC);

    // --- Schattenwelt ---
    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<io.github.sirschrolli.kingdomsatwar.shadow.feature.ShadowTreeFeature>> SHADOW_TREE =
            FEATURE_TYPES.register("shadow_tree", () -> io.github.sirschrolli.kingdomsatwar.shadow.feature.ShadowTreeFeature.CODEC);
    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<io.github.sirschrolli.kingdomsatwar.shadow.feature.CrystalGrowthFeature>> CRYSTAL_GROWTH =
            FEATURE_TYPES.register("crystal_growth", () -> io.github.sirschrolli.kingdomsatwar.shadow.feature.CrystalGrowthFeature.CODEC);
    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<io.github.sirschrolli.kingdomsatwar.shadow.feature.RockSpireFeature>> ROCK_SPIRE =
            FEATURE_TYPES.register("rock_spire", () -> io.github.sirschrolli.kingdomsatwar.shadow.feature.RockSpireFeature.CODEC);
    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<io.github.sirschrolli.kingdomsatwar.shadow.feature.BoneArchFeature>> BONE_ARCH =
            FEATURE_TYPES.register("bone_arch", () -> io.github.sirschrolli.kingdomsatwar.shadow.feature.BoneArchFeature.CODEC);
    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<io.github.sirschrolli.kingdomsatwar.shadow.feature.HangingVinesFeature>> HANGING_VINES =
            FEATURE_TYPES.register("hanging_vines", () -> io.github.sirschrolli.kingdomsatwar.shadow.feature.HangingVinesFeature.CODEC);

    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<io.github.sirschrolli.kingdomsatwar.shadow.feature.RockBridgeFeature>> ROCK_BRIDGE =
            FEATURE_TYPES.register("rock_bridge", () -> io.github.sirschrolli.kingdomsatwar.shadow.feature.RockBridgeFeature.CODEC);

    // --- Oberwelt (eigene Biome) ---
    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<io.github.sirschrolli.kingdomsatwar.worldgen.overworld.feature.BoulderFeature>> BOULDER =
            FEATURE_TYPES.register("boulder", () -> io.github.sirschrolli.kingdomsatwar.worldgen.overworld.feature.BoulderFeature.CODEC);
    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<io.github.sirschrolli.kingdomsatwar.worldgen.overworld.feature.SpireFeature>> SPIRE =
            FEATURE_TYPES.register("spire", () -> io.github.sirschrolli.kingdomsatwar.worldgen.overworld.feature.SpireFeature.CODEC);
    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<io.github.sirschrolli.kingdomsatwar.worldgen.overworld.feature.DuneFeature>> DUNE =
            FEATURE_TYPES.register("dune", () -> io.github.sirschrolli.kingdomsatwar.worldgen.overworld.feature.DuneFeature.CODEC);
    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<io.github.sirschrolli.kingdomsatwar.worldgen.overworld.feature.VentFeature>> VENT =
            FEATURE_TYPES.register("vent", () -> io.github.sirschrolli.kingdomsatwar.worldgen.overworld.feature.VentFeature.CODEC);
    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<io.github.sirschrolli.kingdomsatwar.worldgen.overworld.feature.FissureFeature>> FISSURE =
            FEATURE_TYPES.register("fissure", () -> io.github.sirschrolli.kingdomsatwar.worldgen.overworld.feature.FissureFeature.CODEC);
    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<io.github.sirschrolli.kingdomsatwar.worldgen.overworld.feature.CharredTreeFeature>> CHARRED_TREE =
            FEATURE_TYPES.register("charred_tree", () -> io.github.sirschrolli.kingdomsatwar.worldgen.overworld.feature.CharredTreeFeature.CODEC);
    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<io.github.sirschrolli.kingdomsatwar.worldgen.overworld.feature.AncientTreeFeature>> ANCIENT_TREE =
            FEATURE_TYPES.register("ancient_tree", () -> io.github.sirschrolli.kingdomsatwar.worldgen.overworld.feature.AncientTreeFeature.CODEC);
    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<io.github.sirschrolli.kingdomsatwar.worldgen.overworld.feature.FallenTrunkFeature>> FALLEN_TRUNK =
            FEATURE_TYPES.register("fallen_trunk", () -> io.github.sirschrolli.kingdomsatwar.worldgen.overworld.feature.FallenTrunkFeature.CODEC);
    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<io.github.sirschrolli.kingdomsatwar.worldgen.overworld.feature.PalmFeature>> PALM =
            FEATURE_TYPES.register("palm", () -> io.github.sirschrolli.kingdomsatwar.worldgen.overworld.feature.PalmFeature.CODEC);
    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<io.github.sirschrolli.kingdomsatwar.worldgen.overworld.feature.ReefFeature>> REEF =
            FEATURE_TYPES.register("reef", () -> io.github.sirschrolli.kingdomsatwar.worldgen.overworld.feature.ReefFeature.CODEC);
    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<io.github.sirschrolli.kingdomsatwar.worldgen.overworld.feature.GrandOakFeature>> GRAND_OAK =
            FEATURE_TYPES.register("grand_oak", () -> io.github.sirschrolli.kingdomsatwar.worldgen.overworld.feature.GrandOakFeature.CODEC);
    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<io.github.sirschrolli.kingdomsatwar.worldgen.overworld.feature.WindsweptTreeFeature>> WINDSWEPT_TREE =
            FEATURE_TYPES.register("windswept_tree", () -> io.github.sirschrolli.kingdomsatwar.worldgen.overworld.feature.WindsweptTreeFeature.CODEC);
    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<io.github.sirschrolli.kingdomsatwar.worldgen.overworld.feature.MoorTreeFeature>> MOOR_TREE =
            FEATURE_TYPES.register("moor_tree", () -> io.github.sirschrolli.kingdomsatwar.worldgen.overworld.feature.MoorTreeFeature.CODEC);
    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<io.github.sirschrolli.kingdomsatwar.worldgen.overworld.feature.BogPoolFeature>> BOG_POOL =
            FEATURE_TYPES.register("bog_pool", () -> io.github.sirschrolli.kingdomsatwar.worldgen.overworld.feature.BogPoolFeature.CODEC);
    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<io.github.sirschrolli.kingdomsatwar.worldgen.overworld.feature.ReedBedFeature>> REED_BED =
            FEATURE_TYPES.register("reed_bed", () -> io.github.sirschrolli.kingdomsatwar.worldgen.overworld.feature.ReedBedFeature.CODEC);
    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<io.github.sirschrolli.kingdomsatwar.worldgen.overworld.feature.IceSpikeFeature>> ICE_SPIKE =
            FEATURE_TYPES.register("ice_spike", () -> io.github.sirschrolli.kingdomsatwar.worldgen.overworld.feature.IceSpikeFeature.CODEC);
    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<io.github.sirschrolli.kingdomsatwar.worldgen.overworld.feature.IcicleFeature>> ICICLES =
            FEATURE_TYPES.register("icicles", () -> io.github.sirschrolli.kingdomsatwar.worldgen.overworld.feature.IcicleFeature.CODEC);

    public static final DeferredRegister<MapCodec<? extends PlacementModifier>> PLACEMENT_TYPES =
            DeferredRegister.create(Registries.PLACEMENT_MODIFIER_TYPE, KingdomsAtWar.MODID);

    /** Bauwerke mit Mindestabstand zueinander und zu den Drachennestern. */
    public static final DeferredHolder<MapCodec<? extends PlacementModifier>, MapCodec<SpacedPlacement>> SPACED =
            PLACEMENT_TYPES.register("spaced", () -> SpacedPlacement.CODEC);

    /** Nur außerhalb der Dörfer (Bäume, Felsen, Teiche und Ruinen halten Abstand zu Häusern und Wegen). */
    public static final DeferredHolder<MapCodec<? extends PlacementModifier>, MapCodec<io.github.sirschrolli.kingdomsatwar.worldgen.village.VillageClearing.Filter>>
            NOT_IN_VILLAGE = PLACEMENT_TYPES.register("not_in_village", () -> io.github.sirschrolli.kingdomsatwar.worldgen.village.VillageClearing.Filter.CODEC);

    public static final DeferredRegister<MapCodec<? extends BiomeSource>> BIOME_SOURCES =
            DeferredRegister.create(Registries.BIOME_SOURCE, KingdomsAtWar.MODID);

    /** Die Biomverteilung der Oberwelt mit den eigenen Biomen. */
    public static final DeferredHolder<MapCodec<? extends BiomeSource>, MapCodec<KingdomBiomeSource>> OVERWORLD_BIOMES =
            BIOME_SOURCES.register("overworld", () -> KingdomBiomeSource.CODEC);

    private ModFeatureTypes() {}
}
