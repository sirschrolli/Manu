package io.github.sirschrolli.kingdomsatwar.entity.merfolk;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.util.RandomSource;

/**
 * Die Berufe des Meervolks (seit 0.93). Jeder Beruf trägt eigenes Zubehör am Modell (die Knochen-Namen stimmen mit
 * tools/entitygen/models/mermaid.py überein) und hat eigene Tauschangebote; die Tritonwachen tauschen nicht, sie schützen
 * das Dorf.
 */
public enum MermaidProfession {
    /** Perlentaucherin: Stirnband mit Perle, Netzbeutel – Perlmutt, Nautilusschalen, selten ein Herz des Meeres. */
    PEARL_DIVER("pearl_diver", "diver_band", "pearl_net"),
    /** Korallengärtnerin: Korallenkrone, Seeanemone – Meeresbaum-Setzlinge, Korallen, Meeresgurken, Riesentang. */
    CORAL_GARDENER("coral_gardener", "coral_crown", "flower"),
    /** Muschelbauerin: Muschelkamm, Werkzeuggürtel – Meereshölzer, Türen, Laternen, Prismarin und Gezeitenstein. */
    SHELL_BUILDER("shell_builder", "shell_comb", "tool_belt"),
    /** Gezeitensängerin: Seestern im Haar, Muschelhorn – Tränke, Bücher mit den Verzauberungen des Wassers, ein Aquisitor. */
    TIDE_SINGER("tide_singer", "starfish", "conch"),
    /** Tritonwache: Helm, Panzer, Schulterstücke, Dreizack – tauscht nicht. */
    GUARD("guard");

    /** Die Berufe, mit denen man tauschen kann. */
    public static final MermaidProfession[] TRADERS = {PEARL_DIVER, CORAL_GARDENER, SHELL_BUILDER, TIDE_SINGER};

    private final String id;
    private final Set<String> gear;

    MermaidProfession(String id, String... own) {
        this.id = id;
        Set<String> gear = new HashSet<>(own.length == 0 ? guardGear() : mermaidGear());
        gear.addAll(Set.of(own));
        this.gear = Set.copyOf(gear);
    }

    /** Was jede Meerjungfrau trägt (die Wachen nicht). */
    private static Set<String> mermaidGear() {
        return Set.of("top", "necklace", "locks_l", "locks_r", "hair_back", "bracelet_l", "bracelet_r");
    }

    private static Set<String> guardGear() {
        return Set.of("helmet", "armor", "pauldron_l", "pauldron_r", "trident", "bracelet_l", "bracelet_r");
    }

    public String id() {
        return this.id;
    }

    /** Trägt dieser Beruf das Zubehör (Knochen-Name im Modell)? */
    public boolean wears(String part) {
        return this.gear.contains(part);
    }

    /** Alles Zubehör, das je nach Beruf ein- oder ausgeblendet wird. */
    public static Set<String> allGear() {
        Set<String> all = new HashSet<>();
        for (MermaidProfession profession : values()) {
            all.addAll(profession.gear);
        }
        return all;
    }

    public static MermaidProfession byId(int ordinal) {
        MermaidProfession[] values = values();
        return ordinal >= 0 && ordinal < values.length ? values[ordinal] : PEARL_DIVER;
    }

    public static MermaidProfession byName(String id) {
        for (MermaidProfession profession : values()) {
            if (profession.id.equals(id)) {
                return profession;
            }
        }
        return PEARL_DIVER;
    }

    public static MermaidProfession randomTrader(RandomSource random) {
        return TRADERS[random.nextInt(TRADERS.length)];
    }
}
