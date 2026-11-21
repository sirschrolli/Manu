package io.github.sirschrolli.kingdomsatwar.entity.merfolk;

import io.github.sirschrolli.kingdomsatwar.urmeer.UrmeerBiomes;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;

/**
 * Die sechs Gestalten des Meervolks (seit 0.93): Haar-, Haut- und Schwanzfarben – je eine Textur
 * ({@code textures/entity/mermaid/<id>.png}); die Tiefsee-Gestalt leuchtet (Augen, Flecken, Flossenränder).
 */
public enum MermaidLook {
    /** Rotes Haar, korallenroter Schwanz. */
    CORAL("coral", false),
    /** Dunkelgrünes Haar, smaragdgrüner Schwanz. */
    KELP("kelp", false),
    /** Silbernes Haar, perlmuttfarbener Schwanz. */
    PEARL("pearl", false),
    /** Türkises Haar, aquablauer Schwanz. */
    LAGOON("lagoon", false),
    /** Goldblondes Haar, goldener Schwanz. */
    GOLD("gold", false),
    /** Nachtblaues Haar, tiefblauer Schwanz mit Leuchtflecken. */
    ABYSS("abyss", true);

    private final String id;
    private final boolean glows;

    MermaidLook(String id, boolean glows) {
        this.id = id;
        this.glows = glows;
    }

    public String id() {
        return this.id;
    }

    /** Hat die Textur eine Leuchtschicht ({@code <id>_glow.png})? */
    public boolean glows() {
        return this.glows;
    }

    public static MermaidLook byId(int ordinal) {
        MermaidLook[] values = values();
        return ordinal >= 0 && ordinal < values.length ? values[ordinal] : CORAL;
    }

    public static MermaidLook byName(String id) {
        for (MermaidLook look : values()) {
            if (look.id.equals(id)) {
                return look;
            }
        }
        return CORAL;
    }

    /** Wer wo wohnt: im Tangwald grün und türkis, an den Korallen rot und golden, über den Perlmuttgründen silbern … */
    public static MermaidLook forBiome(Holder<Biome> biome, RandomSource random) {
        MermaidLook[] pool;
        if (biome.is(UrmeerBiomes.KELP_FOREST)) {
            pool = new MermaidLook[] {KELP, KELP, LAGOON, GOLD};
        } else if (biome.is(UrmeerBiomes.CORAL_GARDEN) || biome.is(UrmeerBiomes.REEF)) {
            pool = new MermaidLook[] {CORAL, CORAL, GOLD, LAGOON};
        } else if (biome.is(UrmeerBiomes.PEARL_GROUNDS)) {
            pool = new MermaidLook[] {PEARL, PEARL, LAGOON, CORAL};
        } else if (biome.is(UrmeerBiomes.GLOW_DEPTHS) || biome.is(UrmeerBiomes.TRENCH)) {
            pool = new MermaidLook[] {ABYSS, ABYSS, PEARL};
        } else {
            pool = values();
        }
        return pool[random.nextInt(pool.length)];
    }
}
