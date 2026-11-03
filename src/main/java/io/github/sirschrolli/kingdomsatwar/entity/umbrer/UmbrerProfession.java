package io.github.sirschrolli.kingdomsatwar.entity.umbrer;

import java.util.Set;
import net.minecraft.util.RandomSource;

/**
 * Die Berufe der Umbrer. Jeder Beruf hat eigene Farben, eigenes Zubehör am Modell und eigene Tauschangebote; die
 * Wächter tauschen nicht, sie schützen das Dorf.
 */
public enum UmbrerProfession {
    /** Mondschmied: Laternen, Ketten, Mondsilberklingen – und er verzaubert Nachtstahl. */
    MOONSMITH("moonsmith", Set.of("hood", "apron")),
    /** Kräuterkundige: Tränke, Glimmmoos; kauft Seelenbeeren und Geisterpilze. */
    HERBALIST("herbalist", Set.of("hood", "satchel", "sprig")),
    /** Kristallschleifer: Nachtkristall, Seelenkristall, Glas und Fernrohre. */
    CRYSTAL_CUTTER("crystal_cutter", Set.of("hood", "goggles", "pendant")),
    /** Seher: Bücher, Verzauberungen, die Chronik der Umbrer und (selten) die Abyssit-Schmiedevorlage. */
    SEER("seer", Set.of("hood", "seer_hat", "amulet")),
    /** Dämmerbauer: Dämmerkorn, Brot und Setzlinge. */
    FARMER("farmer", Set.of("straw_hat")),
    /** Wächter: Helm, Rüstung, Umhang und Speer – tauscht nicht. */
    GUARD("guard", Set.of("helmet", "armor", "pauldron_l", "pauldron_r", "spear", "cape"));

    /** Die Berufe, mit denen man tauschen kann. */
    public static final UmbrerProfession[] TRADERS = {MOONSMITH, HERBALIST, CRYSTAL_CUTTER, SEER, FARMER};
    /** Alles Zubehör, das je nach Beruf ein- oder ausgeblendet wird (Knochen-Namen im Modell). */
    public static final Set<String> ALL_GEAR = Set.of("hood", "apron", "satchel", "sprig", "goggles", "pendant", "seer_hat", "amulet",
            "straw_hat", "helmet", "armor", "pauldron_l", "pauldron_r", "spear", "cape");

    private final String id;
    private final Set<String> gear;

    UmbrerProfession(String id, Set<String> gear) {
        this.id = id;
        this.gear = gear;
    }

    public String id() {
        return this.id;
    }

    /** Trägt dieser Beruf das Zubehör (Knochen-Name)? */
    public boolean wears(String part) {
        return this.gear.contains(part);
    }

    public static UmbrerProfession byId(int ordinal) {
        UmbrerProfession[] values = values();
        return ordinal >= 0 && ordinal < values.length ? values[ordinal] : MOONSMITH;
    }

    public static UmbrerProfession randomTrader(RandomSource random) {
        return TRADERS[random.nextInt(TRADERS.length)];
    }
}
