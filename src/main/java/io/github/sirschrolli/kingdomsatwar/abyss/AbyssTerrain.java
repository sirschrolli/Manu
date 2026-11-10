package io.github.sirschrolli.kingdomsatwar.abyss;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.registry.AbyssBlocks;
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
 * Das Gelände des Abgrunds: kein Boden, keine Ebene, keine gemeinsame Höhe – nur <b>Bruchstücke</b>, die in allen Höhen
 * und in allen Lagen durcheinandertreiben.
 * <ul>
 *     <li>Die meisten sind <b>gekippte Platten</b>: Sie entstehen entlang der Nullfläche eines groben, räumlichen Rauschens –
 *     schräge, gewölbte Scheiben, mal fast waagrecht, mal steil wie eine Wand –, die ein zweites Rauschen in Stücke bricht.</li>
 *     <li>Dazwischen treiben <b>Brocken</b>: dicke, unförmige Klumpen.</li>
 *     <li>Überall schweben einzelne Felsbrocken.</li>
 *     <li>Weite Strecken bleiben ganz leer – dort sieht man das Chaos in der Ferne treiben.</li>
 * </ul>
 * Alles liegt zwischen den Höhen 30 und 220; darüber hängen nur die Himmelsrisse.
 */
public final class AbyssTerrain {
    public static final ResourceKey<NoiseGeneratorSettings> SETTINGS = ResourceKey.create(Registries.NOISE_SETTINGS, KingdomsAtWar.id("the_abyss"));

    public static final ResourceKey<NormalNoise> SHEET = noise("sheet");
    public static final ResourceKey<NormalNoise> CHOP = noise("chop");
    public static final ResourceKey<NormalNoise> BLOB = noise("blob");
    public static final ResourceKey<NormalNoise> ROCKS = noise("rocks");
    public static final ResourceKey<NormalNoise> MASK = noise("mask");
    public static final ResourceKey<NormalNoise> TEMPERATURE = noise("temperature");
    public static final ResourceKey<NormalNoise> VEGETATION = noise("vegetation");
    /** Flecken für die Oberflächenregeln (glühende Nähte an den Unterseiten, Mischböden in der Naht). */
    public static final ResourceKey<NormalNoise> SURFACE = noise("surface");

    public static final ResourceKey<DensityFunction> TEMPERATURE_F = function("temperature");
    public static final ResourceKey<DensityFunction> VEGETATION_F = function("vegetation");
    public static final ResourceKey<DensityFunction> MASK_F = function("mask");
    public static final ResourceKey<DensityFunction> FINAL_DENSITY_F = function("final_density");

    public static final int BOTTOM = 30;
    public static final int TOP = 220;

    private AbyssTerrain() {}

    private static ResourceKey<NormalNoise> noise(String name) {
        return ResourceKey.create(Registries.NOISE, KingdomsAtWar.id("abyss/" + name));
    }

    private static ResourceKey<DensityFunction> function(String name) {
        return ResourceKey.create(Registries.DENSITY_FUNCTION, KingdomsAtWar.id("abyss/" + name));
    }

    public static void bootstrapNoises(BootstrapContext<NormalNoise> context) {
        context.register(SHEET, NormalNoise.createParity(-7, 1.0, 0.5, 0.15));
        context.register(CHOP, NormalNoise.createParity(-5, 1.0, 0.6, 0.3));
        context.register(BLOB, NormalNoise.createParity(-6, 1.0, 0.8, 0.4));
        context.register(ROCKS, NormalNoise.createParity(-3, 1.0, 0.5));
        context.register(MASK, NormalNoise.createParity(-8, 1.0, 0.6, 0.3));
        context.register(TEMPERATURE, NormalNoise.createParity(-9, 1.0, 0.7, 0.3));
        context.register(VEGETATION, NormalNoise.createParity(-9, 1.0, 0.6, 0.3));
        context.register(SURFACE, NormalNoise.createParity(-4, 1.0, 1.0));
    }

    public static void bootstrapDensityFunctions(BootstrapContext<DensityFunction> context) {
        HolderGetter<NormalNoise> noises = context.lookup(Registries.NOISE);
        wrap(context, TEMPERATURE_F, DensityFunctions.noise(noises.getOrThrow(TEMPERATURE), 1.0, 0.0));
        wrap(context, VEGETATION_F, DensityFunctions.noise(noises.getOrThrow(VEGETATION), 1.0, 0.0));
        DensityFunction mask = wrap(context, MASK_F, DensityFunctions.cache(DensityFunctions.noise(noises.getOrThrow(MASK), 1.0, 0.0)));

        // Höhenband: innen 0, nach oben und unten hin stark abnehmend
        DensityFunction low = DensityFunctions.yClampedGradient(BOTTOM, BOTTOM + 30, -1.6f, 0.0f);
        DensityFunction high = DensityFunctions.yClampedGradient(TOP - 30, TOP, 0.0f, -1.6f);
        DensityFunction band = low.add(high);

        // Wo überhaupt etwas treibt (die leeren Strecken liegen bei niedrigem Maskenwert)
        DensityFunction gate = DensityFunctions.clampedMap(mask, -0.25f, 0.25f, -1.2f, 0.25f);

        // --- Gekippte Platten: die Nullfläche des groben Rauschens, in Stücke gebrochen ---
        DensityFunction sheet = DensityFunctions.noise(noises.getOrThrow(SHEET), 1.0, 1.0);
        DensityFunction slab = DensityFunctions.constant(0.075f).sub(sheet.abs()).mul(DensityFunctions.constant(9.0f));
        DensityFunction chop = DensityFunctions.clampedMap(DensityFunctions.noise(noises.getOrThrow(CHOP), 1.0, 1.0), -0.2f, 0.3f, -0.9f, 0.35f);
        DensityFunction plates = slab.add(DensityFunctions.min(gate, chop)).add(band);

        // --- Brocken: dicke Klumpen ---
        DensityFunction blobs = DensityFunctions.noise(noises.getOrThrow(BLOB), 1.0, 1.2).mul(DensityFunctions.constant(1.3f))
                .add(gate).add(band).add(-0.62f);

        // --- Einzelne Felsbrocken: kleine, harte Kerne (in den leeren Strecken häufiger) ---
        DensityFunction emptiness = DensityFunctions.clampedMap(mask, -0.4f, 0.1f, 0.1f, -0.15f);
        DensityFunction rocks = DensityFunctions.noise(noises.getOrThrow(ROCKS), 1.0, 1.0).add(-0.8f).add(emptiness).add(band.mul(DensityFunctions.constant(0.5f)))
                .mul(DensityFunctions.constant(3.0f));

        DensityFunction combined = DensityFunctions.max(DensityFunctions.max(plates, blobs), rocks);
        DensityFunction floor = DensityFunctions.yClampedGradient(-10, 10, -2.0f, 0.0f);
        DensityFunction ceiling = DensityFunctions.yClampedGradient(TOP + 10, TOP + 26, 0.0f, -3.0f);
        context.register(FINAL_DENSITY_F, DensityFunctions.interpolated(combined.add(floor).add(ceiling), 4, 4).squeeze());
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
                NoiseRouterData.getFunction(functions, MASK_F),
                zero,
                zero,
                zero,
                DensityFunctions.constant(64.0f),
                NoiseRouterData.getFunction(functions, FINAL_DENSITY_F));
        context.register(SETTINGS, new NoiseGeneratorSettings(
                NoiseSettings.create(-64, 384),
                AbyssBlocks.CHAOS_STONE.base().get().defaultBlockState(),
                Blocks.AIR.defaultBlockState(),
                router,
                context.lookup(Registries.MATERIAL_RULE).getOrThrow(AbyssSurface.SURFACE),
                List.of(),
                -64,
                false,
                Optional.empty(),
                false,
                new NoiseGeneratorSettings.DebugFunctions(List.of(
                        new NoiseGeneratorSettings.DebugFunctionEntry("N", router.finalDensity()),
                        new NoiseGeneratorSettings.DebugFunctionEntry("T", router.temperature()),
                        new NoiseGeneratorSettings.DebugFunctionEntry("V", router.vegetation()),
                        new NoiseGeneratorSettings.DebugFunctionEntry("C", router.continents())))));
    }
}
