package io.github.sirschrolli.kingdomsatwar.worldgen.overworld;

import com.mojang.serialization.MapCodec;
import io.github.sirschrolli.kingdomsatwar.worldgen.ModFeatureTypes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.attribute.AmbientParticle;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.ModifiableBiomeInfo;

/**
 * Die Standard-Biome im Stil der eigenen: Sie bekommen, was die eigenen Biome so lebendig macht – Findlinge und
 * Moosfelsen, umgestürzte Stämme mit Moos und Leuchtkappen, Farne, Hainglocken, Blumeninseln und hin und wieder eine
 * Königseiche, Felsnadeln in Wüste, Savanne und Tafelland, Oasen mit Palmen, Windkiefern auf den Hügeln, abgestorbene
 * Bäume und Schilf im Sumpf, Schilf an den Flüssen, Schneewehen, Eisnadeln und Eiszapfen in den Schneelanden –
 * dazu ein wenig Leben in der Luft: Glühwürmchen im dunklen Wald und im Sumpf, Pollen über Blumenwald und Bergwiese,
 * Schneeflocken auf den Gipfeln, Staub über dem Tafelland. Seit 0.32 wächst in Wäldern, Taiga, Savanne, Kirschhain und
 * auf den Ebenen auch Unterholz – Büsche aus Laub, die bis auf den Boden reichen, und junge Bäume –, und Gras und Laub
 * haben ein satteres Grün wie in den eigenen Biomen.
 *
 * <p>Die Features hängen hinter denen des Bioms, und zwar in der festen Reihenfolge von {@link OverworldFeatures#entries()};
 * jedes kommt nur in Standard-Biomen vor, nie in den eigenen – so bleibt die Reihenfolge der Features in allen Biomen
 * widerspruchsfrei.
 *
 * <p>JSON: {@code {"type": "kingdomsatwar:standard_biomes"}}
 */
public record StandardBiomes() implements BiomeModifier {
    public static final StandardBiomes INSTANCE = new StandardBiomes();
    public static final MapCodec<StandardBiomes> CODEC = MapCodec.unit(INSTANCE);

    /** Was jedes Standard-Biom dazubekommt. */
    private static final Map<ResourceKey<Biome>, Set<OverworldFeatures.Entry>> TOUCHES = new HashMap<>();
    /** Was in der Luft treibt. */
    private static final Map<ResourceKey<Biome>, List<AmbientParticle>> PARTICLES = new HashMap<>();

    /** Gras- und Laubfarbe eines Standard-Bioms (0: wie bei Vanilla). */
    public record Colors(int grass, int foliage) {}

    /** Die neuen Farben (vor dem static-Block, der sie füllt). */
    private static final Map<ResourceKey<Biome>, Colors> COLORS = new HashMap<>();

    static {
        // Ebenen und Bergwiese: Findlinge, Blumeninseln, hin und wieder eine Königseiche
        touch(List.of(Biomes.PLAINS, Biomes.SUNFLOWER_PLAINS), OverworldFeatures.STD_FIELD_BOULDERS, OverworldFeatures.STD_GRAND_OAKS,
                OverworldFeatures.STD_FLOWER_PATCHES);
        touch(List.of(Biomes.MEADOW), OverworldFeatures.STD_MEADOW_BOULDERS, OverworldFeatures.STD_GRAND_OAKS,
                OverworldFeatures.STD_FLOWER_PATCHES, OverworldFeatures.STD_GROVE_BELLS);
        // Wälder: Moosfelsen, umgestürzte Stämme, Farne, Moos
        touch(List.of(Biomes.FOREST, Biomes.FLOWER_FOREST), OverworldFeatures.STD_MOSSY_BOULDERS, OverworldFeatures.STD_FALLEN_OAKS,
                OverworldFeatures.STD_FERNS, OverworldFeatures.STD_MOSS, OverworldFeatures.STD_BRAMBLES);
        touch(List.of(Biomes.FLOWER_FOREST), OverworldFeatures.STD_GROVE_BELLS);
        touch(List.of(Biomes.BIRCH_FOREST, Biomes.OLD_GROWTH_BIRCH_FOREST), OverworldFeatures.STD_MOSSY_BOULDERS,
                OverworldFeatures.STD_FALLEN_BIRCHES, OverworldFeatures.STD_FERNS, OverworldFeatures.STD_GROVE_BELLS,
                OverworldFeatures.STD_BRAMBLES);
        touch(List.of(Biomes.DARK_FOREST), OverworldFeatures.STD_MOSSY_BOULDERS, OverworldFeatures.STD_FALLEN_DARK_OAKS,
                OverworldFeatures.STD_FERNS, OverworldFeatures.STD_MOSS, OverworldFeatures.STD_GLOWCAPS);
        touch(List.of(Biomes.PALE_GARDEN), OverworldFeatures.STD_PALE_BOULDERS, OverworldFeatures.STD_FALLEN_PALE_OAKS);
        touch(List.of(Biomes.TAIGA, Biomes.OLD_GROWTH_PINE_TAIGA, Biomes.OLD_GROWTH_SPRUCE_TAIGA), OverworldFeatures.STD_MOSSY_BOULDERS,
                OverworldFeatures.STD_FALLEN_SPRUCES, OverworldFeatures.STD_FERNS, OverworldFeatures.STD_MOSS, OverworldFeatures.STD_BRAMBLES);
        touch(List.of(Biomes.OLD_GROWTH_PINE_TAIGA, Biomes.OLD_GROWTH_SPRUCE_TAIGA), OverworldFeatures.STD_GLOWCAPS);
        // Schneelande und Gipfel: verschneite Felsen, Schneewehen, Eisnadeln und Eiszapfen
        touch(List.of(Biomes.SNOWY_TAIGA), OverworldFeatures.STD_SNOW_BOULDERS, OverworldFeatures.STD_FALLEN_SPRUCES,
                OverworldFeatures.STD_SNOW_DRIFTS);
        touch(List.of(Biomes.SNOWY_PLAINS, Biomes.ICE_SPIKES), OverworldFeatures.STD_SNOW_BOULDERS, OverworldFeatures.STD_SNOW_DRIFTS);
        touch(List.of(Biomes.SNOWY_SLOPES, Biomes.GROVE), OverworldFeatures.STD_SNOW_BOULDERS, OverworldFeatures.STD_ICICLES,
                OverworldFeatures.STD_SNOW_DRIFTS);
        touch(List.of(Biomes.JAGGED_PEAKS), OverworldFeatures.STD_ICE_SPIKES, OverworldFeatures.STD_ICICLES, OverworldFeatures.STD_SNOW_DRIFTS);
        // Wüste, Savanne, Tafelland: Felsnadeln, Brocken, Oasen, Bernstein
        touch(List.of(Biomes.DESERT), OverworldFeatures.STD_OASES, OverworldFeatures.STD_DESERT_SPIRES, OverworldFeatures.STD_DESERT_BOULDERS);
        touch(List.of(Biomes.SAVANNA), OverworldFeatures.STD_SAVANNA_SPIRES,
                OverworldFeatures.STD_SAVANNA_BOULDERS, OverworldFeatures.STD_AMBER);
        touch(List.of(Biomes.BADLANDS, Biomes.WOODED_BADLANDS, Biomes.ERODED_BADLANDS), OverworldFeatures.STD_BADLANDS_SPIRES,
                OverworldFeatures.STD_BADLANDS_BOULDERS, OverworldFeatures.STD_AMBER);
        // Sumpf und Dschungel
        touch(List.of(Biomes.SWAMP), OverworldFeatures.STD_MOSSY_BOULDERS, OverworldFeatures.STD_SWAMP_DEAD_TREES, OverworldFeatures.STD_FERNS,
                OverworldFeatures.STD_MOSS, OverworldFeatures.STD_REEDS);
        touch(List.of(Biomes.MANGROVE_SWAMP), OverworldFeatures.STD_REEDS);
        touch(List.of(Biomes.JUNGLE, Biomes.SPARSE_JUNGLE, Biomes.BAMBOO_JUNGLE), OverworldFeatures.STD_MOSSY_BOULDERS,
                OverworldFeatures.STD_FALLEN_JUNGLE, OverworldFeatures.STD_FERNS, OverworldFeatures.STD_MOSS, OverworldFeatures.STD_GLOWCAPS);
        // Hügel, Gipfel, Kirschhain
        touch(List.of(Biomes.WINDSWEPT_HILLS, Biomes.WINDSWEPT_GRAVELLY_HILLS, Biomes.WINDSWEPT_FOREST), OverworldFeatures.STD_HILL_BOULDERS,
                OverworldFeatures.STD_WINDSWEPT_TREES);
        touch(List.of(Biomes.WINDSWEPT_HILLS, Biomes.STONY_PEAKS), OverworldFeatures.STD_PEAK_SPIRES);
        touch(List.of(Biomes.STONY_PEAKS), OverworldFeatures.STD_HILL_BOULDERS);
        touch(List.of(Biomes.WINDSWEPT_FOREST), OverworldFeatures.STD_FALLEN_SPRUCES);
        touch(List.of(Biomes.CHERRY_GROVE), OverworldFeatures.STD_MEADOW_BOULDERS, OverworldFeatures.STD_FALLEN_CHERRIES,
                OverworldFeatures.STD_GROVE_BELLS);
        // Flüsse (die Steinküsten, vereisten Gipfel, Savannen-Hochebenen und warmen Strände sind schon eigene Biome)
        touch(List.of(Biomes.RIVER), OverworldFeatures.STD_REEDS);

        // Unterholz: Büsche aus Laub und junge Bäume zwischen den großen
        touch(List.of(Biomes.PLAINS, Biomes.SUNFLOWER_PLAINS, Biomes.MEADOW), OverworldFeatures.STD_FIELD_BUSHES);
        touch(List.of(Biomes.FOREST, Biomes.FLOWER_FOREST), OverworldFeatures.STD_OAK_BUSHES, OverworldFeatures.STD_BIRCH_BUSHES,
                OverworldFeatures.STD_YOUNG_OAKS);
        touch(List.of(Biomes.BIRCH_FOREST, Biomes.OLD_GROWTH_BIRCH_FOREST), OverworldFeatures.STD_BIRCH_BUSHES,
                OverworldFeatures.STD_YOUNG_BIRCHES);
        touch(List.of(Biomes.DARK_FOREST), OverworldFeatures.STD_DARK_OAK_BUSHES);
        touch(List.of(Biomes.TAIGA, Biomes.OLD_GROWTH_PINE_TAIGA, Biomes.OLD_GROWTH_SPRUCE_TAIGA), OverworldFeatures.STD_SPRUCE_BUSHES,
                OverworldFeatures.STD_YOUNG_SPRUCES);
        touch(List.of(Biomes.SNOWY_TAIGA, Biomes.WINDSWEPT_FOREST), OverworldFeatures.STD_SPARSE_SPRUCE_BUSHES);
        touch(List.of(Biomes.SAVANNA), OverworldFeatures.STD_ACACIA_BUSHES);
        touch(List.of(Biomes.CHERRY_GROVE), OverworldFeatures.STD_CHERRY_BUSHES);

        // Farben: satteres, frischeres Grün wie in den eigenen Biomen (Gras, Laub); die Taiga kühler, der dunkle Wald tiefer
        paint(List.of(Biomes.PLAINS, Biomes.SUNFLOWER_PLAINS), 0x86C35C, 0x66B43C);
        paint(List.of(Biomes.MEADOW), 0x7EC064, 0x5CAE44);
        paint(List.of(Biomes.FOREST, Biomes.FLOWER_FOREST), 0x6BBA4E, 0x4CA62E);
        paint(List.of(Biomes.BIRCH_FOREST, Biomes.OLD_GROWTH_BIRCH_FOREST), 0x82C063, 0x64AE42);
        paint(List.of(Biomes.DARK_FOREST), 0, 0x3F8A2A);
        paint(List.of(Biomes.TAIGA, Biomes.OLD_GROWTH_PINE_TAIGA, Biomes.OLD_GROWTH_SPRUCE_TAIGA), 0x6FA56C, 0x508F52);
        paint(List.of(Biomes.WINDSWEPT_HILLS, Biomes.WINDSWEPT_GRAVELLY_HILLS, Biomes.WINDSWEPT_FOREST), 0x7EAE80, 0x5E9A62);

        air(List.of(Biomes.DARK_FOREST), new AmbientParticle(ParticleTypes.FIREFLY, 0.0006f));
        air(List.of(Biomes.SWAMP, Biomes.MANGROVE_SWAMP), new AmbientParticle(ParticleTypes.FIREFLY, 0.0008f));
        air(List.of(Biomes.FLOWER_FOREST, Biomes.MEADOW), new AmbientParticle(ParticleTypes.SPORE_BLOSSOM_AIR, 0.0008f));
        air(List.of(Biomes.SNOWY_SLOPES, Biomes.GROVE, Biomes.JAGGED_PEAKS),
                new AmbientParticle(ParticleTypes.SNOWFLAKE, 0.002f));
        air(List.of(Biomes.BADLANDS, Biomes.WOODED_BADLANDS, Biomes.ERODED_BADLANDS), new AmbientParticle(ParticleTypes.WHITE_ASH, 0.0008f));
    }

    private static void touch(List<ResourceKey<Biome>> biomes, OverworldFeatures.Entry... entries) {
        for (ResourceKey<Biome> biome : biomes) {
            TOUCHES.computeIfAbsent(biome, key -> new LinkedHashSet<>()).addAll(List.of(entries));
        }
    }

    private static void paint(List<ResourceKey<Biome>> biomes, int grass, int foliage) {
        for (ResourceKey<Biome> biome : biomes) {
            COLORS.put(biome, new Colors(grass, foliage));
        }
    }

    /** Die neuen Farben eines Standard-Bioms (oder {@code null}) – für Tests. */
    public static @org.jspecify.annotations.Nullable Colors colors(ResourceKey<Biome> biome) {
        return COLORS.get(biome);
    }

    private static void air(List<ResourceKey<Biome>> biomes, AmbientParticle particle) {
        for (ResourceKey<Biome> biome : biomes) {
            PARTICLES.computeIfAbsent(biome, key -> new ArrayList<>()).add(particle);
        }
    }

    /** Was dieses Standard-Biom dazubekommt (leer: nichts) – für Tests und die Vorführung. */
    public static Set<OverworldFeatures.Entry> touches(ResourceKey<Biome> biome) {
        return TOUCHES.getOrDefault(biome, Set.of());
    }

    /** Alle Standard-Biome, die etwas dazubekommen. */
    public static Set<ResourceKey<Biome>> biomes() {
        return TOUCHES.keySet();
    }

    @Override
    public void modify(RegistryAccess registries, Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
        ResourceKey<Biome> key = biome.unwrapKey().orElse(null);
        if (key == null) {
            return;
        }
        if (phase == Phase.ADD) {
            Set<OverworldFeatures.Entry> wanted = TOUCHES.get(key);
            if (wanted == null) {
                return;
            }
            HolderGetter<PlacedFeature> placed = registries.lookupOrThrow(Registries.PLACED_FEATURE);
            for (OverworldFeatures.Entry entry : OverworldFeatures.entries()) {
                if (wanted.contains(entry)) {
                    builder.getGenerationSettings().addFeature(entry.step(), placed.getOrThrow(entry.placedKey()));
                }
            }
        } else if (phase == Phase.MODIFY) {
            Colors colors = COLORS.get(key);
            if (colors != null) {
                // nur, wo das Biom keine eigenen Farben hat (und das Gras nicht ohnehin abgewandelt wird, wie im dunklen Wald)
                var effects = builder.getSpecialEffects();
                if (colors.grass() != 0 && effects.getGrassColorOverride().isEmpty()
                        && effects.getGrassColorModifier() == net.minecraft.world.level.biome.BiomeSpecialEffects.GrassColorModifier.NONE) {
                    effects.grassColorOverride(java.util.Optional.of(colors.grass()));
                }
                if (colors.foliage() != 0 && effects.getFoliageColorOverride().isEmpty()) {
                    effects.foliageColorOverride(java.util.Optional.of(colors.foliage()));
                }
            }
            List<AmbientParticle> particles = PARTICLES.get(key);
            if (particles != null && builder.getAttributes().get(EnvironmentAttributes.AMBIENT_PARTICLES) == null) {
                builder.getAttributes().set(EnvironmentAttributes.AMBIENT_PARTICLES, List.copyOf(particles));
            }
        }
    }

    @Override
    public MapCodec<? extends BiomeModifier> codec() {
        return ModFeatureTypes.STANDARD_BIOMES.get();
    }
}
