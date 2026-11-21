package io.github.sirschrolli.kingdomsatwar.fae;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.registry.FaeBlocks;
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
 * Das Gelände des Feenreichs – eine sanfte Märchenlandschaft:
 * <ul>
 *     <li>weite, wellige Hügel um Höhe 70, mal flach wie eine Wiese, mal zu runden Kuppen getürmt;</li>
 *     <li>hier und da steile, runde <b>Feenhügel</b>, wie Hügelgräber aus alter Zeit;</li>
 *     <li>wo die Seen-Noise tief liegt, sinkt das Land unter den Wasserspiegel (Höhe 62): die <b>Mondseen</b> mit ihren
 *     Inseln und Halbinseln;</li>
 *     <li>hoch darüber, um Höhe 130, treiben vereinzelt kleine <b>schwebende Inseln</b> mit flachen Kuppen und spitzen
 *     Unterseiten;</li>
 *     <li>eine feine dreidimensionale Noise lässt die Hänge ein wenig überhängen. Höhlen gibt es keine.</li>
 * </ul>
 */
public final class FaeTerrain {
    public static final ResourceKey<NoiseGeneratorSettings> SETTINGS = ResourceKey.create(Registries.NOISE_SETTINGS,
            KingdomsAtWar.id("fae_realm"));

    public static final ResourceKey<NormalNoise> TEMPERATURE = noise("temperature");
    public static final ResourceKey<NormalNoise> VEGETATION = noise("vegetation");
    public static final ResourceKey<NormalNoise> LAKES = noise("lakes");
    public static final ResourceKey<NormalNoise> ZONE = noise("zone");
    public static final ResourceKey<NormalNoise> HILLS = noise("hills");
    public static final ResourceKey<NormalNoise> RUGGED = noise("rugged");
    public static final ResourceKey<NormalNoise> BUMPS = noise("bumps");
    public static final ResourceKey<NormalNoise> MOUNDS = noise("mounds");
    public static final ResourceKey<NormalNoise> DETAIL = noise("detail");
    public static final ResourceKey<NormalNoise> ISLES = noise("isles");
    public static final ResourceKey<NormalNoise> ISLE_MASK = noise("isle_mask");
    /** Flecken für die Oberflächenregeln. */
    public static final ResourceKey<NormalNoise> SURFACE = noise("surface");
    /** Feinere Flecken für die Oberflächenregeln. */
    public static final ResourceKey<NormalNoise> PATCHES = noise("patches");

    public static final ResourceKey<DensityFunction> TEMPERATURE_F = function("temperature");
    public static final ResourceKey<DensityFunction> VEGETATION_F = function("vegetation");
    public static final ResourceKey<DensityFunction> LAKES_F = function("lakes");
    public static final ResourceKey<DensityFunction> ZONE_F = function("zone");
    public static final ResourceKey<DensityFunction> HEIGHT_F = function("height");
    public static final ResourceKey<DensityFunction> FINAL_DENSITY_F = function("final_density");

    /** Wasserspiegel der Mondseen. */
    public static final int SEA_LEVEL = 62;
    /** Mittlere Geländehöhe. */
    public static final int BASE = 70;
    /** Die schwebenden Inseln. */
    public static final int ISLE_BOTTOM = 112;
    public static final int ISLE_CENTER = 132;
    public static final int ISLE_TOP = 146;
    /** Unterhalb dieses Seen-Werts beginnen die Mondseen (für die Biome). */
    public static final float LAKE_EDGE = -0.22f;

    private FaeTerrain() {}

    private static ResourceKey<NormalNoise> noise(String name) {
        return ResourceKey.create(Registries.NOISE, KingdomsAtWar.id("fae/" + name));
    }

    private static ResourceKey<DensityFunction> function(String name) {
        return ResourceKey.create(Registries.DENSITY_FUNCTION, KingdomsAtWar.id("fae/" + name));
    }

    public static void bootstrapNoises(BootstrapContext<NormalNoise> context) {
        context.register(TEMPERATURE, NormalNoise.createParity(-10, 1.0, 0.7, 0.3));
        context.register(VEGETATION, NormalNoise.createParity(-9, 1.0, 0.6, 0.3));
        context.register(LAKES, NormalNoise.createParity(-9, 1.0, 0.8, 0.4));
        context.register(ZONE, NormalNoise.createParity(-9, 1.0, 0.5));
        context.register(HILLS, NormalNoise.createParity(-8, 1.0, 0.55, 0.25));
        context.register(RUGGED, NormalNoise.createParity(-9, 1.0, 0.4));
        context.register(BUMPS, NormalNoise.createParity(-6, 1.0, 0.5));
        context.register(MOUNDS, NormalNoise.createParity(-6, 1.0, 0.3));
        context.register(DETAIL, NormalNoise.createParity(-5, 1.0, 0.6, 0.3));
        context.register(ISLES, NormalNoise.createParity(-6, 1.0, 1.0, 0.5));
        context.register(ISLE_MASK, NormalNoise.createParity(-8, 1.0, 0.6));
        context.register(SURFACE, NormalNoise.createParity(-5, 1.0, 1.0));
        context.register(PATCHES, NormalNoise.createParity(-3, 1.0, 1.0));
    }

    public static void bootstrapDensityFunctions(BootstrapContext<DensityFunction> context) {
        HolderGetter<NormalNoise> noises = context.lookup(Registries.NOISE);
        wrap(context, TEMPERATURE_F, DensityFunctions.noise(noises.getOrThrow(TEMPERATURE), 1.0, 0.0));
        wrap(context, VEGETATION_F, DensityFunctions.noise(noises.getOrThrow(VEGETATION), 1.0, 0.0));
        wrap(context, ZONE_F, DensityFunctions.noise(noises.getOrThrow(ZONE), 1.0, 0.0));
        DensityFunction lakes = wrap(context, LAKES_F, DensityFunctions.cache(DensityFunctions.noise(noises.getOrThrow(LAKES), 1.0, 0.0)));

        // --- die Höhe der Landschaft (zweidimensional) ---
        DensityFunction rugged = DensityFunctions.clampedMap(DensityFunctions.noise(noises.getOrThrow(RUGGED), 1.0, 0.0),
                -0.45f, 0.5f, 5.0f, 17.0f);
        DensityFunction hills = DensityFunctions.noise(noises.getOrThrow(HILLS), 1.0, 0.0).mul(rugged);
        DensityFunction bumps = DensityFunctions.noise(noises.getOrThrow(BUMPS), 1.0, 0.0).mul(2.6f);
        // Feenhügel: seltene, steile, runde Kuppen
        DensityFunction mound = DensityFunctions.clampedMap(DensityFunctions.noise(noises.getOrThrow(MOUNDS), 1.0, 0.0), 0.5f, 0.78f,
                0.0f, 1.0f);
        DensityFunction mounds = mound.mul(mound).mul(DensityFunctions.constant(3.0f).sub(mound.mul(2.0f))).mul(13.0f);
        // die Seen: das Land sinkt bis 15 Blöcke tief ab, zum Seeinneren hin immer flacher werdend
        DensityFunction lakeDepth = DensityFunctions.clampedMap(lakes, -0.46f, LAKE_EDGE + 0.02f, 15.0f, 0.0f);
        DensityFunction lakeCalm = DensityFunctions.clampedMap(lakes, -0.5f, LAKE_EDGE, 0.45f, 1.0f);
        DensityFunction height = wrap(context, HEIGHT_F, DensityFunctions.cache(
                hills.add(bumps).mul(lakeCalm).add(mounds).sub(lakeDepth).add((float) BASE)));

        // --- Dichte: über der Höhe Luft, darunter Fels, mit leichten Überhängen ---
        DensityFunction y = DensityFunctions.yClampedGradient(0, 256, 0.0f, 256.0f);
        DensityFunction ground = height.sub(y).mul(1.0f / 7.0f);
        DensityFunction detail = DensityFunctions.noise(noises.getOrThrow(DETAIL), 1.0, 1.2).mul(0.32f);
        DensityFunction land = ground.add(detail);

        // --- schwebende Inseln: flache Kuppen, spitze Unterseiten, selten ---
        DensityFunction isleNoise = DensityFunctions.noise(noises.getOrThrow(ISLES), 1.2, 1.6);
        DensityFunction gate = DensityFunctions.clampedMap(DensityFunctions.noise(noises.getOrThrow(ISLE_MASK), 1.0, 0.0), 0.22f, 0.55f,
                -2.0f, 0.35f);
        DensityFunction above = DensityFunctions.yClampedGradient(ISLE_CENTER, ISLE_TOP, 0.0f, 2.6f);
        DensityFunction below = DensityFunctions.yClampedGradient(ISLE_BOTTOM, ISLE_CENTER, 2.0f, 0.0f);
        DensityFunction isles = isleNoise.mul(1.4f).add(gate).sub(above).sub(below).add(-0.25f);

        DensityFunction combined = DensityFunctions.max(land, isles);
        // ganz oben nichts mehr
        DensityFunction ceiling = DensityFunctions.yClampedGradient(220, 240, 0.0f, -3.0f);
        context.register(FINAL_DENSITY_F, DensityFunctions.interpolated(combined.add(ceiling), 4, 4).squeeze());
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
                NoiseRouterData.getFunction(functions, LAKES_F),
                NoiseRouterData.getFunction(functions, ZONE_F),
                zero,
                zero,
                NoiseRouterData.getFunction(functions, HEIGHT_F),
                NoiseRouterData.getFunction(functions, FINAL_DENSITY_F));
        context.register(SETTINGS, new NoiseGeneratorSettings(
                NoiseSettings.create(0, 256),
                FaeBlocks.FAESTONE.base().get().defaultBlockState(),
                Blocks.WATER.defaultBlockState(),
                router,
                context.lookup(Registries.MATERIAL_RULE).getOrThrow(FaeSurface.SURFACE),
                List.of(),
                SEA_LEVEL,
                false,
                Optional.empty(),
                false,
                new NoiseGeneratorSettings.DebugFunctions(List.of(
                        new NoiseGeneratorSettings.DebugFunctionEntry("N", router.finalDensity()),
                        new NoiseGeneratorSettings.DebugFunctionEntry("T", router.temperature()),
                        new NoiseGeneratorSettings.DebugFunctionEntry("V", router.vegetation()),
                        new NoiseGeneratorSettings.DebugFunctionEntry("C", router.continents()),
                        new NoiseGeneratorSettings.DebugFunctionEntry("E", router.erosion()),
                        new NoiseGeneratorSettings.DebugFunctionEntry("H", NoiseRouterData.getFunction(functions, HEIGHT_F))))));
    }
}
