package io.github.sirschrolli.kingdomsatwar.entity.creature.farm;

import io.github.sirschrolli.kingdomsatwar.worldgen.overworld.OverworldBiomes;
import java.util.Locale;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;

/**
 * Gemeinsames der eigenen Hof- und Weidetiere: Rassen nach Biom, Vererbung beim Züchten und Rassennamen.
 *
 * <p>Jedes Tier speichert seine Rasse als Zahl (synchronisiert) und im Spielstand als Name ("Breed").
 */
public final class FarmAnimals {
    private FarmAnimals() {}

    /** Gemeinsame Schnittstelle der Rassen-Aufzählungen. */
    public interface Breed {
        String name();

        int ordinal();

        default String id() {
            return this.name().toLowerCase(Locale.ROOT);
        }
    }

    public static <E extends Enum<E> & Breed> E byId(E[] values, int id) {
        return values[Mth.clamp(id, 0, values.length - 1)];
    }

    public static <E extends Enum<E> & Breed> E byName(E[] values, String name, E fallback) {
        for (E e : values) {
            if (e.id().equals(name)) {
                return e;
            }
        }
        return fallback;
    }

    /** Name eines Tieres mit seiner Rasse, z. B. "Fleckvieh" statt "Hausrind". */
    public static Component typeName(EntityType<?> type, Breed breed) {
        return Component.translatable(type.getDescriptionId() + "." + breed.id());
    }

    /**
     * Rasse des Jungtiers: meist die eines Elternteils, selten (1 zu 10) eine zufällige Rasse aus der Gegend –
     * so tauchen in einer Herde gelegentlich neue Fellfarben auf.
     */
    public static <E extends Enum<E> & Breed> E inherit(RandomSource random, E mother, E father, E local) {
        int roll = random.nextInt(20);
        if (roll < 9) {
            return mother;
        }
        return roll < 18 ? father : local;
    }

    // ------------------------------------------------------------------ Biome

    public static boolean is(Holder<Biome> biome, ResourceKey<Biome> key) {
        return biome.is(key);
    }

    public static boolean cold(Holder<Biome> biome) {
        return biome.is(BiomeTags.SPAWNS_COLD_VARIANT_FARM_ANIMALS) || biome.is(OverworldBiomes.FROST_PEAKS)
                || biome.is(BiomeTags.SPAWNS_WHITE_RABBITS);
    }

    public static boolean warm(Holder<Biome> biome) {
        return biome.is(BiomeTags.SPAWNS_WARM_VARIANT_FARM_ANIMALS) || biome.is(OverworldBiomes.ROCK_GORGES)
                || biome.is(OverworldBiomes.ASH_FIELDS);
    }

    /** Offenes Weideland: Ebenen, Wiesen, Kirschhaine, die königlichen Auen. */
    public static boolean pasture(Holder<Biome> biome) {
        return biome.is(Biomes.PLAINS) || biome.is(Biomes.SUNFLOWER_PLAINS) || biome.is(Biomes.MEADOW)
                || biome.is(Biomes.CHERRY_GROVE) || biome.is(Biomes.FLOWER_FOREST) || biome.is(OverworldBiomes.ROYAL_MEADOWS);
    }

    public static boolean mountains(Holder<Biome> biome) {
        return biome.is(BiomeTags.IS_MOUNTAIN) || biome.is(BiomeTags.IS_HILL) || biome.is(OverworldBiomes.FROST_PEAKS);
    }

    public static boolean forest(Holder<Biome> biome) {
        return biome.is(BiomeTags.IS_FOREST) || biome.is(BiomeTags.IS_JUNGLE) || biome.is(Biomes.SWAMP)
                || biome.is(Biomes.MANGROVE_SWAMP) || biome.is(OverworldBiomes.PRIMEVAL_FOREST) || biome.is(OverworldBiomes.MIST_MOOR);
    }

    /** Wählt nach Gewichten: {@code pick(random, 70, 30)} liefert 0 mit 70 % und 1 mit 30 %. */
    public static int pick(RandomSource random, int... weights) {
        int total = 0;
        for (int w : weights) {
            total += w;
        }
        int roll = random.nextInt(Math.max(1, total));
        for (int i = 0; i < weights.length; i++) {
            roll -= weights[i];
            if (roll < 0) {
                return i;
            }
        }
        return weights.length - 1;
    }
}
