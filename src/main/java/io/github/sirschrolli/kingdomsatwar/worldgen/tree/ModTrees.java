package io.github.sirschrolli.kingdomsatwar.worldgen.tree;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BlockStateProviders;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.features.TreeFeatures;
import net.minecraft.data.worldgen.features.VegetationFeatures;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.feature.featuresize.TwoLayersFeatureSize;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.WeightedStateProvider;
import net.minecraft.world.level.levelgen.feature.treedecorators.AlterGroundDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.BeehiveDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.CocoaDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.LeaveVineDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.PlaceOnGroundDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TrunkVineDecorator;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Die eigenen Baumformen: Sie treten an die Stelle der Vanilla-Bäume – in der Weltgenerierung (alle Biome, die diese
 * Bäume tragen) und beim Wachsen aus Setzlingen. Das Holz bleibt dasselbe, nur die Gestalt ist neu:
 *
 * <ul>
 *   <li>Eiche: ein Stamm, der sich oben in zwei, drei Äste teilt, jeder mit einer runden Laubwolke – breit und weich;
 *       die große Eiche höher, mit vier, fünf weit ausladenden Ästen.</li>
 *   <li>Birke: schlank und leicht geneigt, eine schmale, hohe Krone mit hängenden Spitzen.</li>
 *   <li>Fichte: ein Nadelkleid in abgesetzten Etagen mit herabhängenden Zweigen, unten ein kahles Stück Stamm; die
 *       Riesenfichte 2 × 2 dick, mit Wurzelanläufen.</li>
 *   <li>Kiefer: ein hoher, gebogener, kahler Stamm, oben flache Polster an kurzen Ästen; die Riesenkiefer ein Schirm
 *       auf einem mächtigen Stamm.</li>
 *   <li>Urwaldbaum: Brettwurzeln, ein hoher Stamm, oben flache Laubetagen; der Urwaldriese 2 × 2 mit weit
 *       ausgreifenden Ästen.</li>
 *   <li>Akazie: ein kurzer Stamm, der sich in drei, vier flach ausladende Äste mit Schirmen teilt.</li>
 *   <li>Schwarzeiche: ein knorriger Stamm 2 × 2 mit Wurzeln, schwere Äste mit dichten Wolken.</li>
 *   <li>Kirsche: eine hängende Kuppel aus Blüten, deren Zweige am Rand herabregnen.</li>
 *   <li>Sumpfeiche: geneigt, mit Wurzeln, eine hängende Krone voller Ranken.</li>
 * </ul>
 * Mangroven, Blasseichen, Azaleen und Pappeln behalten ihre Gestalt.
 */
public final class ModTrees {
    public static final DeferredRegister<TrunkPlacerType<?>> TRUNK_PLACERS = DeferredRegister.create(Registries.TRUNK_PLACER_TYPE, KingdomsAtWar.MODID);
    public static final DeferredRegister<FoliagePlacerType<?>> FOLIAGE_PLACERS = DeferredRegister.create(Registries.FOLIAGE_PLACER_TYPE,
            KingdomsAtWar.MODID);

    public static final DeferredHolder<TrunkPlacerType<?>, TrunkPlacerType<BranchedTrunkPlacer>> BRANCHED_TRUNK =
            TRUNK_PLACERS.register("branched_trunk_placer", () -> new TrunkPlacerType<>(BranchedTrunkPlacer.CODEC));
    public static final DeferredHolder<FoliagePlacerType<?>, FoliagePlacerType<CloudFoliagePlacer>> CLOUD_FOLIAGE =
            FOLIAGE_PLACERS.register("cloud_foliage_placer", () -> new FoliagePlacerType<>(CloudFoliagePlacer.CODEC));
    public static final DeferredHolder<FoliagePlacerType<?>, FoliagePlacerType<TieredFoliagePlacer>> TIERED_FOLIAGE =
            FOLIAGE_PLACERS.register("tiered_foliage_placer", () -> new FoliagePlacerType<>(TieredFoliagePlacer.CODEC));
    public static final DeferredHolder<FoliagePlacerType<?>, FoliagePlacerType<UmbrellaFoliagePlacer>> UMBRELLA_FOLIAGE =
            FOLIAGE_PLACERS.register("umbrella_foliage_placer", () -> new FoliagePlacerType<>(UmbrellaFoliagePlacer.CODEC));
    public static final DeferredHolder<FoliagePlacerType<?>, FoliagePlacerType<WeepingFoliagePlacer>> WEEPING_FOLIAGE =
            FOLIAGE_PLACERS.register("weeping_foliage_placer", () -> new FoliagePlacerType<>(WeepingFoliagePlacer.CODEC));
    public static final DeferredHolder<FoliagePlacerType<?>, FoliagePlacerType<MoundFoliagePlacer>> MOUND_FOLIAGE =
            FOLIAGE_PLACERS.register("mound_foliage_placer", () -> new FoliagePlacerType<>(MoundFoliagePlacer.CODEC));

    // Büsche und junge Bäume für das Unterholz der Standard-Biome (eigene Namen; siehe bootstrapModFeatures)
    public static final ResourceKey<Feature> OAK_BUSH = key("bush/oak");
    public static final ResourceKey<Feature> BIRCH_BUSH = key("bush/birch");
    public static final ResourceKey<Feature> SPRUCE_BUSH = key("bush/spruce");
    public static final ResourceKey<Feature> DARK_OAK_BUSH = key("bush/dark_oak");
    public static final ResourceKey<Feature> ACACIA_BUSH = key("bush/acacia");
    public static final ResourceKey<Feature> CHERRY_BUSH = key("bush/cherry");
    public static final ResourceKey<Feature> YOUNG_OAK = key("young/oak");
    public static final ResourceKey<Feature> YOUNG_BIRCH = key("young/birch");
    public static final ResourceKey<Feature> YOUNG_SPRUCE = key("young/spruce");

    /** Alle Büsche und jungen Bäume (für Tests und die Vorführung). */
    public static final List<ResourceKey<Feature>> UNDERGROWTH = List.of(OAK_BUSH, BIRCH_BUSH, SPRUCE_BUSH, DARK_OAK_BUSH, ACACIA_BUSH,
            CHERRY_BUSH, YOUNG_OAK, YOUNG_BIRCH, YOUNG_SPRUCE);

    private static ResourceKey<Feature> key(String name) {
        return ResourceKey.create(Registries.FEATURE, KingdomsAtWar.id("tree/" + name));
    }

    private ModTrees() {}

    // =====================================================================================
    // Die Gestalten
    // =====================================================================================

    private static BranchedTrunkPlacer.Shape shape(IntProvider branches, IntProvider length, float start, float rise, IntProvider lean,
            IntProvider roots, boolean crown, boolean thick) {
        return new BranchedTrunkPlacer.Shape(branches, length, start, rise, lean, roots, crown, thick);
    }

    private static TreeFeature.Builder tree(Block log, BranchedTrunkPlacer trunk, Block leaves, FoliagePlacer foliage, TwoLayersFeatureSize size,
            Holder<BlockStateProvider> soil) {
        return new TreeFeature.Builder(BlockStateProvider.of(log), trunk, BlockStateProvider.of(leaves), foliage, size, soil);
    }

    static TreeFeature.Builder oak(Holder<BlockStateProvider> soil) {
        return tree(Blocks.OAK_LOG, new BranchedTrunkPlacer(4, 2, 1, shape(UniformInt.of(2, 3), ConstantInt.of(2), 0.65f, 0.9f,
                        UniformInt.of(0, 1), UniformInt.of(0, 2), true, false)),
                Blocks.OAK_LEAVES, new CloudFoliagePlacer(ConstantInt.of(2), ConstantInt.of(0), ConstantInt.of(4), 0.0f),
                new TwoLayersFeatureSize(1, 0, 1), soil).ignoreVines();
    }

    static TreeFeature.Builder fancyOak(Holder<BlockStateProvider> soil) {
        return tree(Blocks.OAK_LOG, new BranchedTrunkPlacer(6, 3, 2, shape(UniformInt.of(4, 5), UniformInt.of(3, 4), 0.45f, 0.75f,
                        UniformInt.of(0, 1), UniformInt.of(1, 3), true, false)),
                Blocks.OAK_LEAVES, new CloudFoliagePlacer(UniformInt.of(2, 3), ConstantInt.of(0), UniformInt.of(3, 4), 0.0f),
                new TwoLayersFeatureSize(1, 0, 2), soil).ignoreVines();
    }

    static TreeFeature.Builder birch(Holder<BlockStateProvider> soil, int extra) {
        return tree(Blocks.BIRCH_LOG, new BranchedTrunkPlacer(6, 2, extra, shape(UniformInt.of(1, 2), ConstantInt.of(1), 0.6f, 1.4f,
                        UniformInt.of(0, 1), ConstantInt.of(0), true, false)),
                Blocks.BIRCH_LEAVES, new CloudFoliagePlacer(ConstantInt.of(2), ConstantInt.of(0), UniformInt.of(4, 5), 0.3f),
                new TwoLayersFeatureSize(1, 0, 1), soil).ignoreVines();
    }

    static TreeFeature.Builder spruce(Holder<BlockStateProvider> soil) {
        return tree(Blocks.SPRUCE_LOG, new BranchedTrunkPlacer(7, 3, 2, shape(ConstantInt.of(0), ConstantInt.of(1), 0.5f, 0.5f,
                        ConstantInt.of(0), UniformInt.of(0, 1), true, false)),
                Blocks.SPRUCE_LEAVES, new TieredFoliagePlacer(ConstantInt.of(3), ConstantInt.of(0), UniformInt.of(1, 2), 2, 0.5f),
                new TwoLayersFeatureSize(2, 0, 2), soil).ignoreVines();
    }

    static TreeFeature.Builder pine(Holder<BlockStateProvider> soil) {
        return tree(Blocks.SPRUCE_LOG, new BranchedTrunkPlacer(9, 4, 1, shape(UniformInt.of(2, 3), UniformInt.of(1, 2), 0.8f, 0.8f,
                        UniformInt.of(0, 1), ConstantInt.of(0), true, false)),
                Blocks.SPRUCE_LEAVES, new UmbrellaFoliagePlacer(ConstantInt.of(2), ConstantInt.of(0), ConstantInt.of(2)),
                new TwoLayersFeatureSize(2, 0, 2), soil).ignoreVines();
    }

    static TreeFeature.Builder megaSpruce(Holder<BlockStateProvider> soil) {
        return tree(Blocks.SPRUCE_LOG, new BranchedTrunkPlacer(13, 2, 12, shape(ConstantInt.of(0), ConstantInt.of(1), 0.5f, 0.5f,
                        ConstantInt.of(0), UniformInt.of(3, 5), true, true)),
                Blocks.SPRUCE_LEAVES, new TieredFoliagePlacer(UniformInt.of(4, 5), ConstantInt.of(0), UniformInt.of(3, 6), 3, 0.7f),
                new TwoLayersFeatureSize(1, 1, 2), soil);
    }

    static TreeFeature.Builder megaPine(Holder<BlockStateProvider> soil) {
        return tree(Blocks.SPRUCE_LOG, new BranchedTrunkPlacer(13, 2, 12, shape(UniformInt.of(3, 4), UniformInt.of(2, 3), 0.8f, 0.6f,
                        ConstantInt.of(0), UniformInt.of(3, 5), true, true)),
                Blocks.SPRUCE_LEAVES, new UmbrellaFoliagePlacer(UniformInt.of(2, 3), ConstantInt.of(0), ConstantInt.of(3)),
                new TwoLayersFeatureSize(1, 1, 2), soil);
    }

    static TreeFeature.Builder jungle(Holder<BlockStateProvider> soil) {
        return tree(Blocks.JUNGLE_LOG, new BranchedTrunkPlacer(7, 6, 0, shape(UniformInt.of(2, 3), UniformInt.of(2, 3), 0.6f, 0.5f,
                        ConstantInt.of(0), UniformInt.of(2, 4), true, false)),
                Blocks.JUNGLE_LEAVES, new UmbrellaFoliagePlacer(UniformInt.of(2, 3), ConstantInt.of(0), ConstantInt.of(2)),
                new TwoLayersFeatureSize(1, 0, 1), soil);
    }

    static TreeFeature.Builder megaJungle(Holder<BlockStateProvider> soil) {
        return tree(Blocks.JUNGLE_LOG, new BranchedTrunkPlacer(10, 2, 19, shape(UniformInt.of(3, 5), UniformInt.of(3, 4), 0.55f, 0.45f,
                        ConstantInt.of(0), UniformInt.of(4, 7), true, true)),
                Blocks.JUNGLE_LEAVES, new UmbrellaFoliagePlacer(UniformInt.of(3, 4), ConstantInt.of(0), ConstantInt.of(2)),
                new TwoLayersFeatureSize(1, 1, 2), soil);
    }

    static TreeFeature.Builder acacia(Holder<BlockStateProvider> soil) {
        return tree(Blocks.ACACIA_LOG, new BranchedTrunkPlacer(3, 2, 1, shape(UniformInt.of(3, 4), UniformInt.of(3, 4), 0.85f, 0.55f,
                        UniformInt.of(0, 1), ConstantInt.of(0), false, false)),
                Blocks.ACACIA_LEAVES, new UmbrellaFoliagePlacer(UniformInt.of(2, 3), ConstantInt.of(0), ConstantInt.of(2)),
                new TwoLayersFeatureSize(1, 0, 2), soil).ignoreVines();
    }

    static TreeFeature.Builder darkOak(Holder<BlockStateProvider> soil) {
        return tree(Blocks.DARK_OAK_LOG, new BranchedTrunkPlacer(5, 2, 1, shape(UniformInt.of(3, 4), UniformInt.of(2, 3), 0.5f, 0.5f,
                        UniformInt.of(0, 1), UniformInt.of(2, 4), true, true)),
                Blocks.DARK_OAK_LEAVES, new CloudFoliagePlacer(ConstantInt.of(3), ConstantInt.of(0), ConstantInt.of(3), 0.0f),
                new TwoLayersFeatureSize(1, 1, 2), soil).ignoreVines();
    }

    static TreeFeature.Builder cherry(Holder<BlockStateProvider> soil) {
        return tree(Blocks.CHERRY_LOG, new BranchedTrunkPlacer(5, 2, 1, shape(UniformInt.of(2, 3), UniformInt.of(3, 4), 0.55f, 0.5f,
                        UniformInt.of(0, 1), ConstantInt.of(0), true, false)),
                Blocks.CHERRY_LEAVES, new WeepingFoliagePlacer(ConstantInt.of(3), ConstantInt.of(0), ConstantInt.of(2), 0.6f),
                new TwoLayersFeatureSize(1, 0, 2), soil).ignoreVines();
    }

    static TreeFeature.Builder swampOak(Holder<BlockStateProvider> soil) {
        return tree(Blocks.OAK_LOG, new BranchedTrunkPlacer(4, 3, 0, shape(UniformInt.of(2, 3), UniformInt.of(2, 3), 0.5f, 0.45f,
                        UniformInt.of(1, 2), UniformInt.of(1, 3), true, false)),
                Blocks.OAK_LEAVES, new WeepingFoliagePlacer(ConstantInt.of(3), ConstantInt.of(0), ConstantInt.of(2), 0.35f),
                new TwoLayersFeatureSize(1, 0, 1), soil);
    }

    /**
     * Ein Busch: ein Stummel aus einem Block, ganz verdeckt von einem Hügel aus Laub, der bis auf den Boden reicht
     * (Halbmesser {@code radius}, {@code height} Schichten hoch).
     */
    static TreeFeature.Builder bush(Block log, Block leaves, IntProvider radius, IntProvider height, Holder<BlockStateProvider> soil) {
        return tree(log, new BranchedTrunkPlacer(1, 0, 0, shape(ConstantInt.of(0), ConstantInt.of(1), 0.5f, 1.0f, ConstantInt.of(0),
                        ConstantInt.of(0), true, false)),
                leaves, new MoundFoliagePlacer(radius, ConstantInt.of(0), height), new TwoLayersFeatureSize(0, 0, 0), soil).ignoreVines();
    }

    /** Eine junge Eiche: zwei, drei Blöcke Stamm, manchmal ein kurzer Ast, eine kleine runde Krone. */
    static TreeFeature.Builder youngOak(Holder<BlockStateProvider> soil) {
        return tree(Blocks.OAK_LOG, new BranchedTrunkPlacer(2, 1, 0, shape(UniformInt.of(0, 1), ConstantInt.of(1), 0.7f, 1.0f,
                        ConstantInt.of(0), ConstantInt.of(0), true, false)),
                Blocks.OAK_LEAVES, new CloudFoliagePlacer(ConstantInt.of(1), ConstantInt.of(0), ConstantInt.of(3), 0.0f),
                new TwoLayersFeatureSize(1, 0, 1), soil).ignoreVines();
    }

    /** Eine junge Birke: dünn und gerade, eine schmale Krone mit ein paar hängenden Blättern. */
    static TreeFeature.Builder youngBirch(Holder<BlockStateProvider> soil) {
        return tree(Blocks.BIRCH_LOG, new BranchedTrunkPlacer(3, 1, 0, shape(ConstantInt.of(0), ConstantInt.of(1), 0.7f, 1.0f,
                        ConstantInt.of(0), ConstantInt.of(0), true, false)),
                Blocks.BIRCH_LEAVES, new CloudFoliagePlacer(ConstantInt.of(1), ConstantInt.of(0), UniformInt.of(3, 4), 0.25f),
                new TwoLayersFeatureSize(1, 0, 1), soil).ignoreVines();
    }

    /** Eine junge Fichte: ein kleiner Kegel in zwei Etagen, der bis fast auf den Boden reicht. */
    static TreeFeature.Builder youngSpruce(Holder<BlockStateProvider> soil) {
        return tree(Blocks.SPRUCE_LOG, new BranchedTrunkPlacer(3, 1, 0, shape(ConstantInt.of(0), ConstantInt.of(1), 0.7f, 1.0f,
                        ConstantInt.of(0), ConstantInt.of(0), true, false)),
                Blocks.SPRUCE_LEAVES, new TieredFoliagePlacer(ConstantInt.of(2), ConstantInt.of(0), ConstantInt.of(1), 2, 0.3f),
                new TwoLayersFeatureSize(1, 0, 1), soil).ignoreVines();
    }

    /** Büsche und junge Bäume für das Unterholz (unter eigenen Namen; die Standard-Biome setzen sie in ihre Wälder). */
    public static void bootstrapModFeatures(BootstrapContext<Feature> context) {
        Holder<BlockStateProvider> soil = context.lookup(Registries.BLOCK_STATE_PROVIDER).getOrThrow(BlockStateProviders.SOIL_BENEATH_TREE);
        context.register(OAK_BUSH, bush(Blocks.OAK_LOG, Blocks.OAK_LEAVES, UniformInt.of(1, 2), UniformInt.of(2, 3), soil).build());
        context.register(BIRCH_BUSH, bush(Blocks.BIRCH_LOG, Blocks.BIRCH_LEAVES, ConstantInt.of(1), UniformInt.of(2, 3), soil).build());
        context.register(SPRUCE_BUSH, bush(Blocks.SPRUCE_LOG, Blocks.SPRUCE_LEAVES, ConstantInt.of(1), UniformInt.of(2, 3), soil).build());
        context.register(DARK_OAK_BUSH, bush(Blocks.DARK_OAK_LOG, Blocks.DARK_OAK_LEAVES, UniformInt.of(1, 2), ConstantInt.of(2), soil).build());
        context.register(ACACIA_BUSH, bush(Blocks.ACACIA_LOG, Blocks.ACACIA_LEAVES, UniformInt.of(1, 2), UniformInt.of(1, 2), soil).build());
        context.register(CHERRY_BUSH, bush(Blocks.CHERRY_LOG, Blocks.CHERRY_LEAVES, ConstantInt.of(1), ConstantInt.of(2), soil).build());
        context.register(YOUNG_OAK, youngOak(soil).build());
        context.register(YOUNG_BIRCH, youngBirch(soil).build());
        context.register(YOUNG_SPRUCE, youngSpruce(soil).build());
    }

    // =====================================================================================
    // An Stelle der Vanilla-Bäume
    // =====================================================================================

    /** Die Vanilla-Bäume mit neuer Gestalt (gleiche Namen, gleiche Bienenstöcke, Laubstreu und Ranken wie zuvor). */
    public static void bootstrapVanillaOverride(BootstrapContext<Feature> context) {
        HolderGetter<BlockStateProvider> providers = context.lookup(Registries.BLOCK_STATE_PROVIDER);
        Holder<BlockStateProvider> soil = providers.getOrThrow(BlockStateProviders.SOIL_BENEATH_TREE);
        Holder<BlockStateProvider> podzol = providers.getOrThrow(BlockStateProviders.PODZOL_BENEATH_TREE);
        TreeDecorator bees0002 = new BeehiveDecorator(0.002f);
        TreeDecorator bees002 = new BeehiveDecorator(0.02f);
        TreeDecorator bees005 = new BeehiveDecorator(0.05f);
        TreeDecorator bees = new BeehiveDecorator(1.0f);
        TreeDecorator sparseLitter = new PlaceOnGroundDecorator(96, 4, 2,
                Holder.direct(new WeightedStateProvider(VegetationFeatures.leafLitterPatchBuilder(1, 3))));
        TreeDecorator thickLitter = new PlaceOnGroundDecorator(150, 2, 2,
                Holder.direct(new WeightedStateProvider(VegetationFeatures.leafLitterPatchBuilder(1, 4))));

        context.register(TreeFeatures.OAK, oak(soil).build());
        context.register(TreeFeatures.OAK_BEES_0002_LEAF_LITTER, oak(soil).decorators(List.of(bees0002, sparseLitter, thickLitter)).build());
        context.register(TreeFeatures.OAK_BEES_002, oak(soil).decorators(List.of(bees002)).build());
        context.register(TreeFeatures.OAK_BEES_005, oak(soil).decorators(List.of(bees005)).build());
        context.register(TreeFeatures.OAK_LEAF_LITTER, oak(soil).decorators(List.of(sparseLitter, thickLitter)).build());

        context.register(TreeFeatures.FANCY_OAK, fancyOak(soil).build());
        context.register(TreeFeatures.FANCY_OAK_BEES, fancyOak(soil).decorators(List.of(bees)).build());
        context.register(TreeFeatures.FANCY_OAK_BEES_0002_LEAF_LITTER, fancyOak(soil).decorators(List.of(bees0002, sparseLitter, thickLitter)).build());
        context.register(TreeFeatures.FANCY_OAK_BEES_002, fancyOak(soil).decorators(List.of(bees002)).build());
        context.register(TreeFeatures.FANCY_OAK_BEES_005, fancyOak(soil).decorators(List.of(bees005)).build());
        context.register(TreeFeatures.FANCY_OAK_LEAF_LITTER, fancyOak(soil).decorators(List.of(sparseLitter, thickLitter)).build());

        context.register(TreeFeatures.BIRCH, birch(soil, 1).build());
        context.register(TreeFeatures.BIRCH_BEES_0002, birch(soil, 1).decorators(List.of(bees0002)).build());
        context.register(TreeFeatures.BIRCH_BEES_0002_LEAF_LITTER, birch(soil, 1).decorators(List.of(bees0002, sparseLitter, thickLitter)).build());
        context.register(TreeFeatures.BIRCH_BEES_002, birch(soil, 1).decorators(List.of(bees002)).build());
        context.register(TreeFeatures.BIRCH_BEES_005, birch(soil, 1).decorators(List.of(bees005)).build());
        context.register(TreeFeatures.BIRCH_LEAF_LITTER, birch(soil, 1).decorators(List.of(sparseLitter, thickLitter)).build());
        context.register(TreeFeatures.SUPER_BIRCH_BEES, birch(soil, 6).decorators(List.of(bees)).build());
        context.register(TreeFeatures.SUPER_BIRCH_BEES_0002, birch(soil, 6).decorators(List.of(bees0002)).build());

        context.register(TreeFeatures.SPRUCE, spruce(soil).build());
        context.register(TreeFeatures.PINE, pine(soil).build());
        context.register(TreeFeatures.MEGA_SPRUCE, megaSpruce(soil).decorators(List.of(new AlterGroundDecorator(podzol))).build());
        context.register(TreeFeatures.MEGA_PINE, megaPine(soil).decorators(List.of(new AlterGroundDecorator(podzol))).build());

        context.register(TreeFeatures.JUNGLE_TREE, jungle(soil)
                .decorators(List.of(new CocoaDecorator(0.2f), TrunkVineDecorator.INSTANCE, new LeaveVineDecorator(0.25f))).ignoreVines().build());
        context.register(TreeFeatures.JUNGLE_TREE_NO_VINE, jungle(soil).ignoreVines().build());
        context.register(TreeFeatures.MEGA_JUNGLE_TREE, megaJungle(soil)
                .decorators(List.of(TrunkVineDecorator.INSTANCE, new LeaveVineDecorator(0.25f))).build());

        context.register(TreeFeatures.ACACIA, acacia(soil).build());
        context.register(TreeFeatures.DARK_OAK, darkOak(soil).build());
        context.register(TreeFeatures.DARK_OAK_LEAF_LITTER, darkOak(soil).decorators(List.of(sparseLitter, thickLitter)).build());
        context.register(TreeFeatures.CHERRY, cherry(soil).build());
        context.register(TreeFeatures.CHERRY_BEES_005, cherry(soil).decorators(List.of(bees005)).build());
        context.register(TreeFeatures.SWAMP_OAK, swampOak(soil).decorators(List.of(new LeaveVineDecorator(0.25f))).build());
    }
}
