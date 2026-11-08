package io.github.sirschrolli.kingdomsatwar.voidrealm;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.registry.VoidBlocks;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.NoiseRouter;
import net.minecraft.world.level.levelgen.NoiseRouterData;
import net.minecraft.world.level.levelgen.NoiseSettings;
import net.minecraft.world.level.levelgen.densityfunction.DensityFunction;
import net.minecraft.world.level.levelgen.densityfunction.DensityFunctions;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

/**
 * Das Gelände der Leere: kein Boden, kein Meer – nur <b>Bruchstücke zerbrochener Welten</b>, die im Sternennichts treiben.
 * <ul>
 *     <li>Die großen Bruchstücke treiben in Gruppen (Archipele), zwischen denen weite, leere Strecken liegen. Jedes hat eine
 *     fast ebene Oberseite und eine lange, zerklüftete Unterseite, die spitz ins Nichts hängt; ihre Höhe wechselt von Gruppe
 *     zu Gruppe zwischen etwa 50 und 160.</li>
 *     <li>Hoch darüber, um Höhe 200, treiben kleinere, flache Splitter.</li>
 *     <li>Überall dazwischen schweben einzelne Felsbrocken – in den leeren Strecken mehr als bei den Archipelen.</li>
 * </ul>
 */
public final class VoidTerrain {
    public static final ResourceKey<NoiseGeneratorSettings> SETTINGS = ResourceKey.create(Registries.NOISE_SETTINGS, KingdomsAtWar.id("the_void"));

    public static final ResourceKey<NormalNoise> SHAPE = noise("shape");
    public static final ResourceKey<NormalNoise> SHAPE_HIGH = noise("shape_high");
    public static final ResourceKey<NormalNoise> ROCKS = noise("rocks");
    public static final ResourceKey<NormalNoise> MASK = noise("mask");
    public static final ResourceKey<NormalNoise> MASK_HIGH = noise("mask_high");
    public static final ResourceKey<NormalNoise> HEIGHT = noise("height");
    public static final ResourceKey<NormalNoise> TEMPERATURE = noise("temperature");
    public static final ResourceKey<NormalNoise> VEGETATION = noise("vegetation");
    /** Flecken für die Oberflächenregeln. */
    public static final ResourceKey<NormalNoise> SURFACE = noise("surface");
    /** Welche fremde Welt ein Bruchstück der Weltenreste einmal war (Wiese, Wüste, Eis). */
    public static final ResourceKey<NormalNoise> REMNANT = noise("remnant");
    /** Wo die Weltenreste treiben: seltene, abgegrenzte Gebiete (statt schmaler Streifen zwischen den anderen Biomen). */
    public static final ResourceKey<NormalNoise> ZONE = noise("zone");

    public static final ResourceKey<DensityFunction> TEMPERATURE_F = function("temperature");
    public static final ResourceKey<DensityFunction> VEGETATION_F = function("vegetation");
    public static final ResourceKey<DensityFunction> ARCHIPELAGO_F = function("archipelago");
    public static final ResourceKey<DensityFunction> ZONE_F = function("zone");
    public static final ResourceKey<DensityFunction> CENTER_F = function("center");
    public static final ResourceKey<DensityFunction> FINAL_DENSITY_F = function("final_density");

    /** Mittlere Höhe der großen Bruchstücke und ihre Schwankung von Gruppe zu Gruppe. */
    public static final int CENTER = 105;
    public static final int CENTER_SPREAD = 55;
    public static final int HIGH_CENTER = 200;

    private VoidTerrain() {}

    private static ResourceKey<NormalNoise> noise(String name) {
        return ResourceKey.create(Registries.NOISE, KingdomsAtWar.id("void/" + name));
    }

    private static ResourceKey<DensityFunction> function(String name) {
        return ResourceKey.create(Registries.DENSITY_FUNCTION, KingdomsAtWar.id("void/" + name));
    }

    public static void bootstrapNoises(BootstrapContext<NormalNoise> context) {
        context.register(SHAPE, NormalNoise.createParity(-6, 1.0, 1.0, 0.55, 0.3));
        context.register(SHAPE_HIGH, NormalNoise.createParity(-5, 1.0, 0.8, 0.4));
        context.register(ROCKS, NormalNoise.createParity(-3, 1.0, 0.5));
        context.register(MASK, NormalNoise.createParity(-9, 1.0, 0.7, 0.3));
        context.register(MASK_HIGH, NormalNoise.createParity(-7, 1.0, 0.6));
        context.register(HEIGHT, NormalNoise.createParity(-9, 1.0, 0.5));
        context.register(TEMPERATURE, NormalNoise.createParity(-10, 1.0, 0.7, 0.3));
        context.register(VEGETATION, NormalNoise.createParity(-9, 1.0, 0.6, 0.3));
        context.register(SURFACE, NormalNoise.createParity(-5, 1.0, 1.0));
        context.register(REMNANT, NormalNoise.createParity(-7, 1.0, 0.5));
        context.register(ZONE, NormalNoise.createParity(-8, 1.0, 0.6));
    }

    public static void bootstrapDensityFunctions(BootstrapContext<DensityFunction> context) {
        HolderGetter<NormalNoise> noises = context.lookup(Registries.NOISE);
        DensityFunction temperature = wrap(context, TEMPERATURE_F, DensityFunctions.noise(noises.getOrThrow(TEMPERATURE), 1.0, 0.0));
        wrap(context, VEGETATION_F, DensityFunctions.noise(noises.getOrThrow(VEGETATION), 1.0, 0.0));
        wrap(context, ZONE_F, DensityFunctions.noise(noises.getOrThrow(ZONE), 1.0, 0.0));
        DensityFunction mask = wrap(context, ARCHIPELAGO_F, DensityFunctions.cache(DensityFunctions.noise(noises.getOrThrow(MASK), 1.0, 0.0)));
        DensityFunction center = wrap(context, CENTER_F, DensityFunctions.cache(
                DensityFunctions.noise(noises.getOrThrow(HEIGHT), 1.0, 0.0).mul(DensityFunctions.constant(CENTER_SPREAD)).add(CENTER)));
        DensityFunction y = DensityFunctions.yClampedGradient(-64, 320, -64.0f, 320.0f);

        // --- Die großen Bruchstücke: flache Oberseite (steiler Abfall darüber), lange Unterseite ---
        DensityFunction relative = y.sub(center);
        DensityFunction above = DensityFunctions.max(relative, DensityFunctions.zero()).mul(DensityFunctions.constant(1.0f / 4.5f));
        DensityFunction below = DensityFunctions.max(relative.mul(DensityFunctions.constant(-1.0f)), DensityFunctions.zero())
                .mul(DensityFunctions.constant(1.0f / 20.0f));
        DensityFunction gate = DensityFunctions.clampedMap(mask, -0.2f, 0.32f, -1.4f, 0.5f);
        DensityFunction shape = DensityFunctions.noise(noises.getOrThrow(SHAPE), 1.0, 1.5);
        DensityFunction islands = shape.mul(DensityFunctions.constant(1.15f)).add(gate).sub(above).sub(below).add(-0.14f);

        // --- Hoch oben: kleinere, flache Splitter ---
        DensityFunction maskHigh = DensityFunctions.noise(noises.getOrThrow(MASK_HIGH), 1.0, 0.0);
        DensityFunction gateHigh = DensityFunctions.clampedMap(maskHigh, 0.15f, 0.5f, -1.6f, 0.25f);
        DensityFunction band = DensityFunctions.yClampedGradient(HIGH_CENTER - 30, HIGH_CENTER + 30, -30.0f, 30.0f).abs()
                .mul(DensityFunctions.constant(1.0f / 6.0f));
        DensityFunction high = DensityFunctions.noise(noises.getOrThrow(SHAPE_HIGH), 1.5, 2.2).add(gateHigh).sub(band).add(-0.2f);

        // --- Einzelne Felsbrocken im Nichts: kleine, harte Kerne (in den leeren Strecken häufiger) ---
        DensityFunction emptiness = DensityFunctions.clampedMap(mask, -0.4f, 0.1f, 0.1f, -0.15f);
        DensityFunction rockBand = DensityFunctions.yClampedGradient(30, 220, -1.0f, 1.0f).abs().mul(DensityFunctions.constant(-0.6f));
        DensityFunction rocks = DensityFunctions.noise(noises.getOrThrow(ROCKS), 1.0, 1.0).add(-0.78f).add(emptiness).add(rockBand)
                .mul(DensityFunctions.constant(3.0f));

        DensityFunction combined = DensityFunctions.max(DensityFunctions.max(islands, high), rocks);
        // nichts unter Höhe 0 und über Höhe 250
        DensityFunction floor = DensityFunctions.yClampedGradient(-10, 10, -2.0f, 0.0f);
        DensityFunction ceiling = DensityFunctions.yClampedGradient(240, 256, 0.0f, -2.0f);
        DensityFunction bounded = combined.add(floor).add(ceiling);
        context.register(FINAL_DENSITY_F, DensityFunctions.interpolated(bounded, 4, 4).squeeze());
    }

    private static DensityFunction wrap(BootstrapContext<DensityFunction> context, ResourceKey<DensityFunction> key, DensityFunction value) {
        return new DensityFunctions.HolderHolder(context.register(key, value));
    }

    public static void bootstrapSettings(BootstrapContext<NoiseGeneratorSettings> context) {
        HolderGetter<DensityFunction> functions = context.lookup(Registries.DENSITY_FUNCTION);
        DensityFunction zero = DensityFunctions.zero();
        NoiseRouter router = new NoiseRouter(
                NoiseRouterData.getFunction(functions, TEMPERATURE_F),
                NoiseRouterData.getFunction(functions, VEGETATION_F),
                NoiseRouterData.getFunction(functions, ARCHIPELAGO_F),
                NoiseRouterData.getFunction(functions, ZONE_F),
                zero,
                zero,
                DensityFunctions.constant(64.0f),
                NoiseRouterData.getFunction(functions, FINAL_DENSITY_F));
        context.register(SETTINGS, new NoiseGeneratorSettings(
                NoiseSettings.create(-64, 384),
                VoidBlocks.VOID_STONE.base().get().defaultBlockState(),
                Blocks.AIR.defaultBlockState(),
                router,
                context.lookup(Registries.MATERIAL_RULE).getOrThrow(VoidSurface.SURFACE),
                List.of(),
                -64,
                false,
                Optional.empty(),
                false,
                new NoiseGeneratorSettings.DebugFunctions(List.of(
                        new NoiseGeneratorSettings.DebugFunctionEntry("N", router.finalDensity()),
                        new NoiseGeneratorSettings.DebugFunctionEntry("T", router.temperature()),
                        new NoiseGeneratorSettings.DebugFunctionEntry("V", router.vegetation()),
                        new NoiseGeneratorSettings.DebugFunctionEntry("C", router.continents()),
                        new NoiseGeneratorSettings.DebugFunctionEntry("E", router.erosion())))));
    }
}
