package io.github.sirschrolli.kingdomsatwar.worldgen.overworld;

import com.mojang.datafixers.util.Pair;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterList;

/**
 * Wo die eigenen Biome in der Oberwelt liegen. Ausgangspunkt ist das Vanilla-Klima (Temperatur, Feuchte, Kontinentalität,
 * Erosion, Tiefe, Verwerfung): Wo es ein bestimmtes Vanilla-Biom vorsieht und zusätzlich ein Teilbereich des Klimas
 * zutrifft, liegt stattdessen ein eigenes Biom. So bleiben Gelände und Übergänge stimmig – die Aschefelder sind die
 * feuchtere Hälfte der Wüsten, die Königsauen die Hälfte der Ebenen jenseits der Verwerfungsgrenze und so fort.
 *
 * <ul>
 *     <li><b>Aschefelder</b> – Wüste, wo es etwas feuchter ist (dort, wo sonst schon Steppe läge)</li>
 *     <li><b>Frostgipfel</b> – alle vereisten Gipfel und die Hälfte der Schneehänge</li>
 *     <li><b>Sturmküste</b> – die felsigen Küsten</li>
 *     <li><b>Urhain</b> – die Hälfte der Wälder im kühlen Klima</li>
 *     <li><b>Felsschluchten</b> – Savannenhochebenen und zerklüftete Savannen</li>
 *     <li><b>Korallenküste</b> – die Strände im warmen Klima und das flache, lauwarme Meer davor</li>
 *     <li><b>Königsauen</b> – die Hälfte der Ebenen im gemäßigten Klima</li>
 *     <li><b>Nebelmoor</b> – die Sümpfe im kühlen Klima</li>
 * </ul>
 */
public final class OverworldRegions {
    private OverworldRegions() {}

    /** Eine Klimagröße. */
    enum Axis {
        TEMPERATURE, HUMIDITY, CONTINENTALNESS, EROSION, DEPTH, WEIRDNESS
    }

    /** Ein Wertebereich einer Klimagröße. */
    record Range(Axis axis, float min, float max) {}

    /** Wo das Vanilla-Klima {@code from} vorsieht und alle {@code ranges} zutreffen, liegt {@code to}. */
    record Rule(ResourceKey<Biome> from, ResourceKey<Biome> to, List<Range> ranges) {}

    private static Range range(Axis axis, float min, float max) {
        return new Range(axis, min, max);
    }

    private static Rule rule(ResourceKey<Biome> from, ResourceKey<Biome> to, Range... ranges) {
        return new Rule(from, to, List.of(ranges));
    }

    /** Alle Ersetzungen. Für ein Vanilla-Biom dürfen sich die Bereiche mehrerer Regeln nicht überschneiden. */
    static final List<Rule> RULES = List.of(
            rule(Biomes.DESERT, OverworldBiomes.ASH_FIELDS, range(Axis.HUMIDITY, 0.1f, 2.0f)),
            rule(Biomes.FROZEN_PEAKS, OverworldBiomes.FROST_PEAKS),
            rule(Biomes.SNOWY_SLOPES, OverworldBiomes.FROST_PEAKS, range(Axis.WEIRDNESS, 0.0f, 2.0f)),
            rule(Biomes.STONY_SHORE, OverworldBiomes.STORM_COAST),
            rule(Biomes.FOREST, OverworldBiomes.PRIMEVAL_FOREST, range(Axis.TEMPERATURE, -0.45f, -0.15f), range(Axis.WEIRDNESS, 0.0f, 2.0f)),
            rule(Biomes.SAVANNA_PLATEAU, OverworldBiomes.ROCK_GORGES),
            rule(Biomes.WINDSWEPT_SAVANNA, OverworldBiomes.ROCK_GORGES),
            rule(Biomes.BEACH, OverworldBiomes.CORAL_COAST, range(Axis.TEMPERATURE, 0.2f, 2.0f)),
            // dazu das flache, küstennahe Wasser davor: türkise Lagunen mit Riffen
            rule(Biomes.LUKEWARM_OCEAN, OverworldBiomes.CORAL_COAST, range(Axis.CONTINENTALNESS, -0.33f, -0.19f)),
            rule(Biomes.PLAINS, OverworldBiomes.ROYAL_MEADOWS, range(Axis.TEMPERATURE, -0.15f, 0.2f), range(Axis.WEIRDNESS, 0.0f, 2.0f)),
            rule(Biomes.SWAMP, OverworldBiomes.MIST_MOOR, range(Axis.TEMPERATURE, -2.0f, -0.15f)));

    /** Das Vanilla-Klima der Oberwelt mit den eigenen Biomen. */
    public static List<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> parameters() {
        Climate.ParameterList<ResourceKey<Biome>> vanilla = MultiNoiseBiomeSourceParameterList.knownPresets()
                .get(MultiNoiseBiomeSourceParameterList.Preset.OVERWORLD);
        List<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> result = new ArrayList<>();
        for (Pair<Climate.ParameterPoint, ResourceKey<Biome>> entry : vanilla.values()) {
            result.addAll(apply(entry));
        }
        return result;
    }

    /** Teilt einen Klimapunkt nach den Regeln seines Bioms auf. */
    static List<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> apply(Pair<Climate.ParameterPoint, ResourceKey<Biome>> entry) {
        ResourceKey<Biome> vanilla = entry.getSecond();
        List<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> parts = new ArrayList<>(List.of(entry));
        for (Rule rule : RULES) {
            if (rule.from() != vanilla) {
                continue;
            }
            List<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> next = new ArrayList<>();
            for (Pair<Climate.ParameterPoint, ResourceKey<Biome>> part : parts) {
                if (part.getSecond() != vanilla) {
                    next.add(part);             // schon von einer früheren Regel ersetzt
                    continue;
                }
                List<Climate.ParameterPoint> inside = List.of(part.getFirst());
                List<Climate.ParameterPoint> outside = new ArrayList<>();
                for (Range range : rule.ranges()) {
                    List<Climate.ParameterPoint> narrowed = new ArrayList<>();
                    for (Climate.ParameterPoint point : inside) {
                        split(point, range, narrowed, outside);
                    }
                    inside = narrowed;
                }
                inside.forEach(point -> next.add(Pair.of(point, rule.to())));
                outside.forEach(point -> next.add(Pair.of(point, vanilla)));
            }
            parts = next;
        }
        return parts;
    }

    /** Schneidet einen Klimapunkt an den Grenzen des Bereichs: das Innere nach {@code inside}, der Rest nach {@code outside}. */
    private static void split(Climate.ParameterPoint point, Range range, List<Climate.ParameterPoint> inside, List<Climate.ParameterPoint> outside) {
        Climate.Parameter value = get(point, range.axis());
        long low = Math.max(value.min(), Climate.quantizeCoord(range.min()));
        long high = Math.min(value.max(), Climate.quantizeCoord(range.max()));
        if (low >= high) {
            outside.add(point);
            return;
        }
        inside.add(with(point, range.axis(), new Climate.Parameter(low, high)));
        if (value.min() < low) {
            outside.add(with(point, range.axis(), new Climate.Parameter(value.min(), low)));
        }
        if (high < value.max()) {
            outside.add(with(point, range.axis(), new Climate.Parameter(high, value.max())));
        }
    }

    private static Climate.Parameter get(Climate.ParameterPoint p, Axis axis) {
        return switch (axis) {
            case TEMPERATURE -> p.temperature();
            case HUMIDITY -> p.humidity();
            case CONTINENTALNESS -> p.continentalness();
            case EROSION -> p.erosion();
            case DEPTH -> p.depth();
            case WEIRDNESS -> p.weirdness();
        };
    }

    private static Climate.ParameterPoint with(Climate.ParameterPoint p, Axis axis, Climate.Parameter value) {
        return new Climate.ParameterPoint(
                axis == Axis.TEMPERATURE ? value : p.temperature(),
                axis == Axis.HUMIDITY ? value : p.humidity(),
                axis == Axis.CONTINENTALNESS ? value : p.continentalness(),
                axis == Axis.EROSION ? value : p.erosion(),
                axis == Axis.DEPTH ? value : p.depth(),
                axis == Axis.WEIRDNESS ? value : p.weirdness(),
                p.offset());
    }
}
