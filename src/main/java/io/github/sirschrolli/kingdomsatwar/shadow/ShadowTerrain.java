package io.github.sirschrolli.kingdomsatwar.shadow;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.registry.ShadowBlocks;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.TerrainProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.NoiseRouter;
import net.minecraft.world.level.levelgen.NoiseRouterData;
import net.minecraft.world.level.levelgen.NoiseSettings;
import net.minecraft.world.level.levelgen.Noises;
import net.minecraft.world.level.levelgen.densityfunction.DensityFunction;
import net.minecraft.world.level.levelgen.densityfunction.DensityFunctions;
import net.minecraft.world.level.levelgen.densityfunction.op.SplineFunction;
import net.minecraft.world.level.levelgen.synth.BlendedNoise;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

/**
 * Das Gelände der Schattenwelt – ein eigener Generator mit eigenen Zufallswerten (unabhängig von der Oberwelt):
 * <ul>
 *     <li>Kontinente und tiefschwarze Meere, Hügel, Ebenen und Berge nach den Formkurven der Oberwelt – die Gipfel aber
 *     doppelt so zerklüftet, sodass schwarze <b>Felsnadeln</b> aufragen;</li>
 *     <li>wo sich in der Oberwelt Flusstäler winden, reißen in den wärmeren Gegenden <b>tiefe Abgründe</b> auf: steile
 *     Schluchten bis tief unter den Meeresspiegel, ohne Wasser (dort leuchten später die Nachtkristalle), in den
 *     kühleren Gegenden bleiben sie sumpfige Flüsse;</li>
 *     <li>hoch oben, zwischen Höhe 170 und 250, treiben <b>schwebende Inseln</b> mit flachen Kuppen und langen,
 *     spitz zulaufenden Unterseiten – nie über den Bergen;</li>
 *     <li>darunter Höhlen wie in der Oberwelt und ganz unten der fast schwarze Tiefschattenstein.</li>
 * </ul>
 */
public final class ShadowTerrain {
    public static final ResourceKey<NoiseGeneratorSettings> SETTINGS = ResourceKey.create(Registries.NOISE_SETTINGS,
            KingdomsAtWar.id("shadow_world"));

    // ---------------------------------------------------------------------------------------
    // Rauschen (eigene Schlüssel = eigene Zufallswerte)
    // ---------------------------------------------------------------------------------------

    public static final ResourceKey<NormalNoise> TEMPERATURE = noise("temperature");
    public static final ResourceKey<NormalNoise> VEGETATION = noise("vegetation");
    public static final ResourceKey<NormalNoise> CONTINENTALNESS = noise("continentalness");
    public static final ResourceKey<NormalNoise> EROSION = noise("erosion");
    public static final ResourceKey<NormalNoise> RIDGE = noise("ridge");
    public static final ResourceKey<NormalNoise> SHIFT = noise("offset");
    public static final ResourceKey<NormalNoise> JAGGED = noise("jagged");
    public static final ResourceKey<NormalNoise> ISLANDS = noise("islands");
    public static final ResourceKey<NormalNoise> ISLAND_MASK = noise("island_mask");
    /** Flecken für die Oberflächenregeln (Kies, Moos, Sand …). */
    public static final ResourceKey<NormalNoise> SURFACE = noise("surface");
    /** Feinere Flecken für die Oberflächenregeln. */
    public static final ResourceKey<NormalNoise> PATCHES = noise("patches");

    // ---------------------------------------------------------------------------------------
    // Dichtefunktionen
    // ---------------------------------------------------------------------------------------

    private static final ResourceKey<DensityFunction> SHIFT_X = function("shift_x");
    private static final ResourceKey<DensityFunction> SHIFT_Z = function("shift_z");
    public static final ResourceKey<DensityFunction> TEMPERATURE_F = function("temperature");
    public static final ResourceKey<DensityFunction> VEGETATION_F = function("vegetation");
    public static final ResourceKey<DensityFunction> CONTINENTS_F = function("continents");
    public static final ResourceKey<DensityFunction> EROSION_F = function("erosion");
    public static final ResourceKey<DensityFunction> RIDGES_F = function("ridges");
    public static final ResourceKey<DensityFunction> RIDGES_FOLDED_F = function("ridges_folded");
    public static final ResourceKey<DensityFunction> OFFSET_F = function("offset");
    public static final ResourceKey<DensityFunction> FACTOR_F = function("factor");
    public static final ResourceKey<DensityFunction> JAGGEDNESS_F = function("jaggedness");
    public static final ResourceKey<DensityFunction> DEPTH_F = function("depth");
    private static final ResourceKey<DensityFunction> BASE_3D_NOISE_F = function("base_3d_noise");
    /** 0 außerhalb, 1 in der Mitte eines Abgrunds (auch für die Biome und die Grundwasser-Sperre). */
    public static final ResourceKey<DensityFunction> CHASM_F = function("chasm");
    public static final ResourceKey<DensityFunction> SLOPED_CHEESE_F = function("sloped_cheese");
    public static final ResourceKey<DensityFunction> ISLANDS_F = function("islands");
    public static final ResourceKey<DensityFunction> PRELIMINARY_SURFACE_F = function("preliminary_surface_level");
    public static final ResourceKey<DensityFunction> CHUNK_SURFACE_F = function("chunk_surface_level");
    public static final ResourceKey<DensityFunction> FINAL_DENSITY_F = function("final_density");

    /** Meeresspiegel der Schattenwelt. */
    public static final int SEA_LEVEL = 63;
    /** Grenzen der schwebenden Inseln. */
    public static final int ISLAND_BOTTOM = 170;
    public static final int ISLAND_CENTER = 214;
    public static final int ISLAND_TOP = 250;

    private ShadowTerrain() {}

    private static ResourceKey<NormalNoise> noise(String name) {
        return ResourceKey.create(Registries.NOISE, KingdomsAtWar.id("shadow/" + name));
    }

    private static ResourceKey<DensityFunction> function(String name) {
        return ResourceKey.create(Registries.DENSITY_FUNCTION, KingdomsAtWar.id("shadow/" + name));
    }

    private static ResourceKey<DensityFunction> vanilla(String name) {
        return ResourceKey.create(Registries.DENSITY_FUNCTION, Identifier.withDefaultNamespace(name));
    }

    // =======================================================================================
    // Rauschen
    // =======================================================================================

    public static void bootstrapNoises(BootstrapContext<NormalNoise> context) {
        // Klima: Temperatur und Feuchte eine Oktave gröber als in der Oberwelt – die Biome werden größer
        context.register(TEMPERATURE, NormalNoise.createParity(-11, 1.5, 0.0, 1.0, 0.0, 0.0, 0.0));
        context.register(VEGETATION, NormalNoise.createParity(-9, 1.0, 1.0, 0.0, 0.0, 0.0, 0.0));
        context.register(CONTINENTALNESS, NormalNoise.createParity(-9, 1.0, 1.0, 2.0, 2.0, 2.0, 1.0, 1.0, 1.0, 1.0));
        context.register(EROSION, NormalNoise.createParity(-9, 1.0, 1.0, 0.0, 1.0, 1.0));
        context.register(RIDGE, NormalNoise.createParity(-7, 1.0, 2.0, 1.0, 0.0, 0.0, 0.0));
        context.register(SHIFT, NormalNoise.createParity(-3, 1.0, 1.0, 1.0, 0.0));
        context.register(JAGGED, NormalNoise.createParity(-16, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0));
        context.register(ISLANDS, NormalNoise.createParity(-6, 1.0, 1.0, 0.6, 0.3));
        context.register(ISLAND_MASK, NormalNoise.createParity(-8, 1.0, 1.0));
        context.register(SURFACE, NormalNoise.createParity(-6, 1.0, 1.0, 1.0));
        context.register(PATCHES, NormalNoise.createParity(-4, 1.0, 1.0));
    }

    // =======================================================================================
    // Dichtefunktionen
    // =======================================================================================

    public static void bootstrapDensityFunctions(BootstrapContext<DensityFunction> context) {
        HolderGetter<NormalNoise> noises = context.lookup(Registries.NOISE);
        HolderGetter<DensityFunction> functions = context.lookup(Registries.DENSITY_FUNCTION);

        // --- Klima und Großformen ---
        DensityFunction shiftX = wrap(context, SHIFT_X, DensityFunctions.cache(DensityFunctions.shiftA(noises.getOrThrow(SHIFT))));
        DensityFunction shiftZ = wrap(context, SHIFT_Z, DensityFunctions.cache(DensityFunctions.shiftB(noises.getOrThrow(SHIFT))));
        DensityFunction temperature = wrap(context, TEMPERATURE_F,
                DensityFunctions.shiftedNoise2d(shiftX, shiftZ, 0.25, noises.getOrThrow(TEMPERATURE)));
        wrap(context, VEGETATION_F, DensityFunctions.shiftedNoise2d(shiftX, shiftZ, 0.25, noises.getOrThrow(VEGETATION)));
        DensityFunction continents = wrap(context, CONTINENTS_F,
                DensityFunctions.cache(DensityFunctions.shiftedNoise2d(shiftX, shiftZ, 0.25, noises.getOrThrow(CONTINENTALNESS))));
        DensityFunction erosion = wrap(context, EROSION_F,
                DensityFunctions.cache(DensityFunctions.shiftedNoise2d(shiftX, shiftZ, 0.25, noises.getOrThrow(EROSION))));
        DensityFunction ridges = wrap(context, RIDGES_F,
                DensityFunctions.cache(DensityFunctions.shiftedNoise2d(shiftX, shiftZ, 0.25, noises.getOrThrow(RIDGE))));
        DensityFunction folded = wrap(context, RIDGES_FOLDED_F, NoiseRouterData.peaksAndValleys(ridges));

        // --- Höhe, Steilheit und Zacken nach den Formkurven der Oberwelt (Zacken wie in der „zerklüfteten“ Welt) ---
        SplineFunction.Coordinate c = new SplineFunction.Coordinate(continents);
        SplineFunction.Coordinate e = new SplineFunction.Coordinate(erosion);
        SplineFunction.Coordinate w = new SplineFunction.Coordinate(ridges);
        SplineFunction.Coordinate pv = new SplineFunction.Coordinate(folded);
        DensityFunction offset = wrap(context, OFFSET_F, DensityFunctions.cache(
                DensityFunctions.spline(TerrainProvider.overworldOffset(c, e, pv, false)).add(-0.50375f)));
        DensityFunction factor = wrap(context, FACTOR_F, DensityFunctions.cache(
                DensityFunctions.spline(TerrainProvider.overworldFactor(c, e, w, pv, false))));
        DensityFunction depth = wrap(context, DEPTH_F, DensityFunctions.yClampedGradient(-64, 320, 1.5f, -1.5f).add(offset));
        DensityFunction jaggedness = wrap(context, JAGGEDNESS_F, DensityFunctions.cache(
                DensityFunctions.spline(TerrainProvider.overworldJaggedness(c, e, w, pv, true))));
        DensityFunction jaggedNoise = DensityFunctions.noise(noises.getOrThrow(JAGGED), 1500.0, 0.0);
        DensityFunction jagged = DensityFunctions.cache(jaggedness.mul(jaggedNoise.halfNegative()));
        DensityFunction initial = gradient(depth.add(jagged), factor);
        DensityFunction base3d = wrap(context, BASE_3D_NOISE_F, new BlendedNoise(0.25, 0.125, 80.0, 160.0, 8.0));
        DensityFunction sloped = DensityFunctions.cache(initial.add(base3d));

        // --- Abgründe: in den warmen Flusstälern, landeinwärts, bis tief unter den Meeresspiegel ---
        DensityFunction chasmLine = DensityFunctions.clampedMap(ridges.abs(), 0.0f, 0.075f, 1.0f, 0.0f);
        DensityFunction chasmClimate = DensityFunctions.clampedMap(temperature, -0.08f, 0.06f, 0.0f, 1.0f);
        DensityFunction chasmInland = DensityFunctions.clampedMap(continents, -0.16f, -0.04f, 0.0f, 1.0f);
        DensityFunction chasm = wrap(context, CHASM_F, DensityFunctions.cache(chasmLine.mul(chasmClimate).mul(chasmInland)));
        // in der Mitte steil (Wurzel), unten läuft die Schlucht in einem Grund um Höhe −20 aus
        DensityFunction floorFade = DensityFunctions.yClampedGradient(-36, -4, 0.0f, 1.0f);
        DensityFunction carve = chasm.sqrt().mul(floorFade).mul(-26.0f);
        DensityFunction terrain = wrap(context, SLOPED_CHEESE_F, DensityFunctions.cache(sloped.add(carve)));

        // --- Höhlen (wie in der Oberwelt aus Käse-, Spaghetti- und Nudelhöhlen) ---
        DensityFunction entrances = NoiseRouterData.getFunction(functions, vanilla("overworld/caves/entrances"));
        DensityFunction spaghetti2d = NoiseRouterData.getFunction(functions, vanilla("overworld/caves/spaghetti_2d"));
        DensityFunction roughness = NoiseRouterData.getFunction(functions, vanilla("overworld/caves/spaghetti_roughness_function"));
        DensityFunction pillarsRaw = NoiseRouterData.getFunction(functions, vanilla("overworld/caves/pillars"));
        DensityFunction noodle = NoiseRouterData.getFunction(functions, vanilla("overworld/caves/noodle"));
        DensityFunction cheese = DensityFunctions.noise(noises.getOrThrow(Noises.CAVE_CHEESE), 0.6666666666666666);
        DensityFunction layers = DensityFunctions.noise(noises.getOrThrow(Noises.CAVE_LAYER), 8.0).square().mul(4.0f);
        DensityFunction solidCheese = cheese.add(0.27f).clamp(-1.0f, 1.0f).add(terrain.mul(-0.64f).add(1.5f).clamp(0.0f, 0.5f));
        DensityFunction subtractions = DensityFunctions.min(DensityFunctions.min(layers.add(solidCheese), entrances),
                spaghetti2d.add(roughness));
        DensityFunction pillars = DensityFunctions.rangeChoice(pillarsRaw, -1000000.0f, 0.03f, DensityFunctions.constant(-1000000.0f), pillarsRaw);
        DensityFunction underground = DensityFunctions.max(subtractions, pillars);
        DensityFunction caves = DensityFunctions.rangeChoice(terrain, -1000000.0f, 1.5625f,
                DensityFunctions.min(terrain, entrances.mul(5.0f)), underground);

        // --- Schwebende Inseln: flache Kuppen, lange spitze Unterseiten, in Gruppen, nie über Bergen ---
        DensityFunction islandNoise = DensityFunctions.noise(noises.getOrThrow(ISLANDS), 1.0, 1.4);
        DensityFunction islandMask = DensityFunctions.noise(noises.getOrThrow(ISLAND_MASK), 1.0, 0.0);
        DensityFunction gate = DensityFunctions.clampedMap(islandMask, 0.0f, 0.42f, -2.0f, 0.3f);
        DensityFunction notOverMountains = DensityFunctions.clampedMap(erosion, -0.5f, -0.3f, -2.0f, 0.0f);
        DensityFunction above = DensityFunctions.yClampedGradient(ISLAND_CENTER, ISLAND_TOP, 0.0f, 2.4f);
        DensityFunction below = DensityFunctions.yClampedGradient(ISLAND_BOTTOM, ISLAND_CENTER, 1.8f, 0.0f);
        DensityFunction islands = wrap(context, ISLANDS_F,
                islandNoise.mul(1.5f).add(gate).add(notOverMountains).sub(above).sub(below).add(-0.2f));

        // --- Zusammensetzen: Gelände (oben und unten sanft begrenzt), Inseln, Zellen-Interpolation, Nudelhöhlen ---
        DensityFunction combined = DensityFunctions.max(slide(caves), islands);
        DensityFunction post = DensityFunctions.interpolated(combined.mul(0.64f), 4, 8).squeeze();
        context.register(FINAL_DENSITY_F, DensityFunctions.min(post, noodle).add(DensityFunctions.beardifier()));

        // --- Vorläufige Oberfläche (ohne Abgründe und Höhlen; für Grundwasser und Oberflächenregeln) ---
        DensityFunction preliminaryDensity = slide(gradient(depth, factor).add(-0.703125f).clamp(-64.0f, 64.0f)).add(-0.390625f);
        DensityFunction upperBound = offset.mul(128.0f).add(128.0f + 96.0f).clamp(-40.0f, 320.0f);
        DensityFunction surfaceLevel = wrap(context, PRELIMINARY_SURFACE_F,
                DensityFunctions.findTopSurface(preliminaryDensity, upperBound, -64, 8));
        context.register(CHUNK_SURFACE_F, DensityFunctions.interpolated(surfaceLevel, 16, 1));
    }

    /** Dichte aus Tiefe und Steilheit: über der Oberfläche schwach negativ, darunter stark positiv. */
    private static DensityFunction gradient(DensityFunction depth, DensityFunction factor) {
        return depth.mul(factor).quarterNegative().mul(4.0f);
    }

    /** Ganz oben (ab Höhe 280) läuft das Gelände in Luft aus, ganz unten (bis Höhe −40) in festen Stein. */
    private static DensityFunction slide(DensityFunction density) {
        DensityFunction top = DensityFunctions.yClampedGradient(280, 304, 1.0f, 0.0f);
        DensityFunction bottom = DensityFunctions.yClampedGradient(-64, -40, 0.0f, 1.0f);
        return DensityFunctions.lerp(bottom, 0.1171875f, DensityFunctions.lerp(top, -0.078125f, density));
    }

    private static DensityFunction wrap(BootstrapContext<DensityFunction> context, ResourceKey<DensityFunction> key, DensityFunction value) {
        return new DensityFunctions.HolderHolder(context.register(key, value));
    }

    // =======================================================================================
    // Generator-Einstellungen
    // =======================================================================================

    public static void bootstrapSettings(BootstrapContext<NoiseGeneratorSettings> context) {
        HolderGetter<DensityFunction> functions = context.lookup(Registries.DENSITY_FUNCTION);
        HolderGetter<NormalNoise> noises = context.lookup(Registries.NOISE);
        NoiseRouter router = new NoiseRouter(
                NoiseRouterData.getFunction(functions, TEMPERATURE_F),
                NoiseRouterData.getFunction(functions, VEGETATION_F),
                NoiseRouterData.getFunction(functions, CONTINENTS_F),
                NoiseRouterData.getFunction(functions, EROSION_F),
                NoiseRouterData.getFunction(functions, DEPTH_F),
                NoiseRouterData.getFunction(functions, RIDGES_F),
                NoiseRouterData.getFunction(functions, CHUNK_SURFACE_F),
                NoiseRouterData.getFunction(functions, FINAL_DENSITY_F));
        // In den Abgründen bleibt das Grundwasser aus (sonst stünden sie bis zum Meeresspiegel voll)
        DensityFunction exclusion = NoiseRouterData.getFunction(functions, CHASM_F).add(-0.02f);
        Aquifer.Config aquifers = new Aquifer.Config(
                DensityFunctions.noise(noises.getOrThrow(Noises.AQUIFER_BARRIER), 0.5),
                DensityFunctions.noise(noises.getOrThrow(Noises.AQUIFER_FLUID_LEVEL_FLOODEDNESS), 0.67),
                DensityFunctions.noise(noises.getOrThrow(Noises.AQUIFER_FLUID_LEVEL_SPREAD), 0.7142857142857143),
                DensityFunctions.noise(noises.getOrThrow(Noises.AQUIFER_LAVA)),
                exclusion,
                NoiseRouterData.getFunction(functions, PRELIMINARY_SURFACE_F));
        context.register(SETTINGS, new NoiseGeneratorSettings(
                NoiseSettings.create(-64, 384),
                ShadowBlocks.SHADOW_STONE.base().get().defaultBlockState(),
                Blocks.WATER.defaultBlockState(),
                router,
                context.lookup(Registries.MATERIAL_RULE).getOrThrow(ShadowSurface.SURFACE),
                List.of(),
                SEA_LEVEL,
                false,
                Optional.of(aquifers),
                false,
                new NoiseGeneratorSettings.DebugFunctions(List.of(
                        new NoiseGeneratorSettings.DebugFunctionEntry("N", router.finalDensity()),
                        new NoiseGeneratorSettings.DebugFunctionEntry("T", router.temperature()),
                        new NoiseGeneratorSettings.DebugFunctionEntry("V", router.vegetation()),
                        new NoiseGeneratorSettings.DebugFunctionEntry("C", router.continents()),
                        new NoiseGeneratorSettings.DebugFunctionEntry("E", router.erosion()),
                        new NoiseGeneratorSettings.DebugFunctionEntry("D", router.depth()),
                        new NoiseGeneratorSettings.DebugFunctionEntry("W", router.ridges()),
                        new NoiseGeneratorSettings.DebugFunctionEntry("A", NoiseRouterData.getFunction(functions, CHASM_F))))));
    }
}
