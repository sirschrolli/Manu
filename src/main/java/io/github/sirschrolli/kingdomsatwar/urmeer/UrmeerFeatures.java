package io.github.sirschrolli.kingdomsatwar.urmeer;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.registry.SeaBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.SeaGear;
import java.util.List;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.BlockReplacement;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.OreFeature;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.HeightmapPlacement;
import net.minecraft.world.level.levelgen.placement.RarityFilter;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockMatchTest;

/**
 * Was im Urmeer zusätzlich entsteht (seit 0.97): Adern aus Aquamarinerz im Gezeitenstein – überall unter dem Grund, offen an
 * Felsnadeln, steilen Hängen und Inseln – und auf dem Meeresgrund Findlinge aus Gezeitenstein, aus denen Aquamarin schimmert.
 */
public final class UrmeerFeatures {
    public static final ResourceKey<Feature> ORE_AQUAMARINE = ResourceKey.create(Registries.FEATURE, KingdomsAtWar.id("urmeer/ore_aquamarine"));
    public static final ResourceKey<PlacedFeature> ORE_AQUAMARINE_PLACED = ResourceKey.create(Registries.PLACED_FEATURE,
            KingdomsAtWar.id("urmeer/ore_aquamarine"));
    public static final ResourceKey<Feature> AQUAMARINE_ROCKS = ResourceKey.create(Registries.FEATURE, KingdomsAtWar.id("urmeer/aquamarine_rocks"));
    public static final ResourceKey<PlacedFeature> AQUAMARINE_ROCKS_PLACED = ResourceKey.create(Registries.PLACED_FEATURE,
            KingdomsAtWar.id("urmeer/aquamarine_rocks"));
    /** Adern je Chunk und ihre Größe. */
    public static final int VEINS = 12;
    public static final int VEIN_SIZE = 6;

    private UrmeerFeatures() {}

    public static void bootstrapFeatures(BootstrapContext<Feature> context) {
        context.register(ORE_AQUAMARINE, new OreFeature(List.of(BlockReplacement.replace(new BlockMatchTest(SeaBlocks.TIDE_STONE.base().get()),
                SeaGear.AQUAMARINE_ORE.get().defaultBlockState())), VEIN_SIZE, 0.0f));
        // Findlinge am Grund: Gezeitenstein, darin hier und da Aquamarinerz
        context.register(AQUAMARINE_ROCKS, new io.github.sirschrolli.kingdomsatwar.worldgen.overworld.feature.BoulderFeature(
                SeaBlocks.TIDE_STONE.base().get().defaultBlockState(), SeaBlocks.TIDE_STONE.base().get().defaultBlockState(), 0.0f,
                SeaGear.AQUAMARINE_ORE.get().defaultBlockState(), 0.16f, Blocks.AIR.defaultBlockState(), 1.3f, 2.3f, true));
    }

    public static void bootstrapPlacedFeatures(BootstrapContext<PlacedFeature> context) {
        HolderGetter<Feature> features = context.lookup(Registries.FEATURE);
        context.register(ORE_AQUAMARINE_PLACED, new PlacedFeature(features.getOrThrow(ORE_AQUAMARINE), List.of(CountPlacement.of(VEINS),
                InSquarePlacement.spread(), HeightRangePlacement.uniform(VerticalAnchor.absolute(UrmeerTerrain.MIN_Y + 4),
                        VerticalAnchor.absolute(UrmeerTerrain.SEA_LEVEL + 16)), BiomeFilter.biome())));
        context.register(AQUAMARINE_ROCKS_PLACED, new PlacedFeature(features.getOrThrow(AQUAMARINE_ROCKS), List.of(
                RarityFilter.onAverageOnceEvery(2), InSquarePlacement.spread(), HeightmapPlacement.onHeightmap(Heightmap.Types.OCEAN_FLOOR),
                BiomeFilter.biome())));
    }
}
