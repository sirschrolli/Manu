package io.github.sirschrolli.kingdomsatwar.urmeer;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
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
 * Das Gelände des Urmeers (seit 0.89) – ein Meer ohne Küsten, so alt wie die Welt:
 * <ul>
 *     <li>der Meeresspiegel liegt hoch (Höhe 96), der Grund wellt sich um Höhe 56 – überall gut vierzig Blöcke Wasser;</li>
 *     <li>wo die Riff-Noise hoch liegt, steigen flache <b>Korallenbänke</b> bis knapp unter die Oberfläche;</li>
 *     <li>wo die Graben-Noise tief liegt, reißen <b>Tiefseegräben</b> den Grund auf, bis fast hinab zum Grundgestein;</li>
 *     <li>selten durchbrechen <b>Inseln</b> die Oberfläche – Felsen mit Stränden, auf denen Palmen stehen;</li>
 *     <li>überall ragen einzelne <b>Felsnadeln</b> vom Grund fast bis zum Licht; eine feine dreidimensionale Noise lässt
 *     Überhänge und Bögen entstehen.</li>
 * </ul>
 */
public final class UrmeerTerrain {
    public static final ResourceKey<NoiseGeneratorSettings> SETTINGS = ResourceKey.create(Registries.NOISE_SETTINGS, KingdomsAtWar.id("urmeer"));

    public static final ResourceKey<NormalNoise> REEF = noise("reef");
    public static final ResourceKey<NormalNoise> VEGETATION = noise("vegetation");
    public static final ResourceKey<NormalNoise> ISLANDS = noise("islands");
    public static final ResourceKey<NormalNoise> TRENCH = noise("trench");
    public static final ResourceKey<NormalNoise> HILLS = noise("hills");
    public static final ResourceKey<NormalNoise> SWELL = noise("swell");
    public static final ResourceKey<NormalNoise> SPIRES = noise("spires");
    public static final ResourceKey<NormalNoise> DETAIL = noise("detail");
    /** Flecken für die Oberflächenregeln. */
    public static final ResourceKey<NormalNoise> SURFACE = noise("surface");
    public static final ResourceKey<NormalNoise> PATCHES = noise("patches");

    public static final ResourceKey<DensityFunction> REEF_F = function("reef");
    public static final ResourceKey<DensityFunction> VEGETATION_F = function("vegetation");
    public static final ResourceKey<DensityFunction> ISLANDS_F = function("islands");
    public static final ResourceKey<DensityFunction> TRENCH_F = function("trench");
    public static final ResourceKey<DensityFunction> HEIGHT_F = function("height");
    public static final ResourceKey<DensityFunction> FINAL_DENSITY_F = function("final_density");

    public static final int MIN_Y = -32;
    public static final int HEIGHT = 288;
    /** Der Meeresspiegel. */
    public static final int SEA_LEVEL = 96;
    /** Der mittlere Grund. */
    public static final int FLOOR = 56;
    /** Ab diesen Werten: Inseln (Kontinentalität), Gräben (Erosion), Riffe (Temperatur) – auch für die Biome. */
    public static final float ISLAND_EDGE = 0.47f;
    public static final float TRENCH_EDGE = -0.36f;
    public static final float REEF_EDGE = 0.26f;

    private UrmeerTerrain() {}

    private static ResourceKey<NormalNoise> noise(String name) {
        return ResourceKey.create(Registries.NOISE, KingdomsAtWar.id("urmeer/" + name));
    }

    private static ResourceKey<DensityFunction> function(String name) {
        return ResourceKey.create(Registries.DENSITY_FUNCTION, KingdomsAtWar.id("urmeer/" + name));
    }

    public static void bootstrapNoises(BootstrapContext<NormalNoise> context) {
        context.register(REEF, NormalNoise.createParity(-9, 1.0, 0.6, 0.3));
        context.register(VEGETATION, NormalNoise.createParity(-8, 1.0, 0.5));
        context.register(ISLANDS, NormalNoise.createParity(-9, 1.0, 0.7, 0.35));
        context.register(TRENCH, NormalNoise.createParity(-9, 1.0, 0.5, 0.25));
        context.register(HILLS, NormalNoise.createParity(-7, 1.0, 0.55, 0.25));
        context.register(SWELL, NormalNoise.createParity(-5, 1.0, 0.5));
        context.register(SPIRES, NormalNoise.createParity(-4, 1.0, 0.4));
        context.register(DETAIL, NormalNoise.createParity(-5, 1.0, 0.6, 0.3));
        context.register(SURFACE, NormalNoise.createParity(-5, 1.0, 1.0));
        context.register(PATCHES, NormalNoise.createParity(-3, 1.0, 1.0));
    }

    public static void bootstrapDensityFunctions(BootstrapContext<DensityFunction> context) {
        HolderGetter<NormalNoise> noises = context.lookup(Registries.NOISE);
        DensityFunction reef = wrap(context, REEF_F, DensityFunctions.cache(DensityFunctions.noise(noises.getOrThrow(REEF), 1.0, 0.0)));
        wrap(context, VEGETATION_F, DensityFunctions.noise(noises.getOrThrow(VEGETATION), 1.0, 0.0));
        DensityFunction islands = wrap(context, ISLANDS_F, DensityFunctions.cache(DensityFunctions.noise(noises.getOrThrow(ISLANDS), 1.0, 0.0)));
        DensityFunction trench = wrap(context, TRENCH_F, DensityFunctions.cache(DensityFunctions.noise(noises.getOrThrow(TRENCH), 1.0, 0.0)));

        // --- die Höhe des Grunds (zweidimensional) ---
        DensityFunction hills = DensityFunctions.noise(noises.getOrThrow(HILLS), 1.0, 0.0).mul(11.0f);
        DensityFunction swellNoise = DensityFunctions.noise(noises.getOrThrow(SWELL), 1.0, 0.0);
        DensityFunction swell = swellNoise.mul(2.6f);
        // Korallenbänke: der Grund steigt sanft bis vier Blöcke unter die Oberfläche
        DensityFunction reefLift = DensityFunctions.clampedMap(reef, REEF_EDGE - 0.1f, REEF_EDGE + 0.1f, 0.0f, 1.0f);
        DensityFunction reefHeight = reefLift.mul((float) (SEA_LEVEL - 4 - FLOOR));
        // Inseln: steigen über die Oberfläche, zur Mitte hin bis knapp zwanzig Blöcke hoch (ringsum ein flacher Sockel)
        DensityFunction isle = DensityFunctions.clampedMap(islands, ISLAND_EDGE - 0.08f, ISLAND_EDGE + 0.12f, 0.0f, 1.0f);
        DensityFunction islandHeight = isle.mul((float) (SEA_LEVEL + 18 - FLOOR)).add(isle.mul(hills).mul(0.6f));
        // Gräben: der Grund reißt bis Höhe -14 auf (nicht unter Inseln)
        DensityFunction trenchDepth = DensityFunctions.clampedMap(trench, TRENCH_EDGE - 0.12f, TRENCH_EDGE + 0.03f, 1.0f, 0.0f);
        DensityFunction trenchCut = trenchDepth.mul(DensityFunctions.constant(1.0f).sub(isle)).mul((float) (FLOOR + 14));
        DensityFunction base = hills.mul(DensityFunctions.constant(1.0f).sub(reefLift.mul(0.7f))).add(swell).add(reefHeight).add(islandHeight)
                .sub(trenchCut).add((float) FLOOR);
        // Felsnadeln: schmale, steile Säulen vom Grund bis knapp unter die Oberfläche (nie darüber, nicht auf Riffen und Inseln,
        // nicht in den Gräben), verschieden hoch
        DensityFunction spire = DensityFunctions.clampedMap(DensityFunctions.noise(noises.getOrThrow(SPIRES), 1.0, 0.0), 0.6f, 0.68f, 0.0f, 1.0f);
        DensityFunction room = DensityFunctions.max(DensityFunctions.constant((float) (SEA_LEVEL - 7)).sub(base), DensityFunctions.zero());
        DensityFunction reach = DensityFunctions.clampedMap(swellNoise, -0.5f, 0.5f, 0.45f, 1.0f);
        DensityFunction spireHeight = spire.mul(room).mul(reach).mul(DensityFunctions.constant(1.0f).sub(trenchDepth));
        DensityFunction height = wrap(context, HEIGHT_F, DensityFunctions.cache(base.add(spireHeight)));

        // --- Dichte: unter der Höhe Fels, mit Überhängen und Bögen ---
        DensityFunction y = DensityFunctions.yClampedGradient(MIN_Y, MIN_Y + HEIGHT, (float) MIN_Y, (float) (MIN_Y + HEIGHT));
        DensityFunction ground = height.sub(y).mul(1.0f / 8.0f);
        DensityFunction detail = DensityFunctions.noise(noises.getOrThrow(DETAIL), 1.0, 1.3).mul(0.3f);
        DensityFunction ceiling = DensityFunctions.yClampedGradient(220, 240, 0.0f, -3.0f);
        context.register(FINAL_DENSITY_F, DensityFunctions.interpolated(ground.add(detail).add(ceiling), 4, 4).squeeze());
    }

    private static DensityFunction wrap(BootstrapContext<DensityFunction> context, ResourceKey<DensityFunction> key, DensityFunction value) {
        return new DensityFunctions.HolderHolder(context.register(key, value));
    }

    public static void bootstrapSettings(BootstrapContext<NoiseGeneratorSettings> context) {
        HolderGetter<DensityFunction> functions = context.lookup(Registries.DENSITY_FUNCTION);
        DensityFunction zero = DensityFunctions.zero();
        NoiseRouter router = new NoiseRouter(
                NoiseRouterData.getFunction(functions, REEF_F),
                NoiseRouterData.getFunction(functions, VEGETATION_F),
                NoiseRouterData.getFunction(functions, ISLANDS_F),
                NoiseRouterData.getFunction(functions, TRENCH_F),
                zero,
                zero,
                NoiseRouterData.getFunction(functions, HEIGHT_F),
                NoiseRouterData.getFunction(functions, FINAL_DENSITY_F));
        context.register(SETTINGS, new NoiseGeneratorSettings(
                NoiseSettings.create(MIN_Y, HEIGHT),
                io.github.sirschrolli.kingdomsatwar.registry.SeaBlocks.TIDE_STONE.base().get().defaultBlockState(),
                Blocks.WATER.defaultBlockState(),
                router,
                context.lookup(Registries.MATERIAL_RULE).getOrThrow(UrmeerSurface.SURFACE),
                List.of(),
                SEA_LEVEL,
                false,
                Optional.empty(),
                false,
                new NoiseGeneratorSettings.DebugFunctions(List.of(
                        new NoiseGeneratorSettings.DebugFunctionEntry("N", router.finalDensity()),
                        new NoiseGeneratorSettings.DebugFunctionEntry("T", router.temperature()),
                        new NoiseGeneratorSettings.DebugFunctionEntry("C", router.continents()),
                        new NoiseGeneratorSettings.DebugFunctionEntry("E", router.erosion()),
                        new NoiseGeneratorSettings.DebugFunctionEntry("H", NoiseRouterData.getFunction(functions, HEIGHT_F))))));
    }
}
