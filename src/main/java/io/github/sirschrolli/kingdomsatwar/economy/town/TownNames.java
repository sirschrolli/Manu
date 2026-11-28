package io.github.sirschrolli.kingdomsatwar.economy.town;

import java.util.List;
import net.minecraft.util.RandomSource;

/** Namen für Siedlungen und ihre Bürger (mittelalterlich, deutsch). */
public final class TownNames {
    private static final List<String> FIRST = List.of("Eichen", "Linden", "Falken", "Raben", "Wolfs", "Hohen", "Grün", "Stein",
            "Rosen", "Adler", "Bären", "Silber", "Kron", "Mühl", "Brunnen", "Weiden", "Hirsch", "Schwanen", "Löwen", "Sonnen",
            "Buchen", "Birken", "Tannen", "Ahorn", "Wald", "Berg", "Fels", "Gold", "Hafer", "Rotten");
    private static final List<String> LAST = List.of("furt", "au", "stein", "horst", "heim", "burg", "wald", "bach", "thal", "berg",
            "fels", "feld", "hausen", "hof", "dorf", "brück", "eck", "hain", "rode", "wiesen");
    private static final List<String> MEN = List.of("Hans", "Konrad", "Heinrich", "Ulrich", "Friedrich", "Albrecht", "Gottfried",
            "Wilhelm", "Otto", "Dietrich", "Bernhard", "Leopold", "Matthis", "Jakob", "Lorenz", "Veit", "Kaspar", "Balthasar",
            "Ruprecht", "Eberhard", "Gerhard", "Arnold", "Wolfram", "Hartmann", "Benedikt", "Florian", "Martin", "Nikolaus");
    private static final List<String> WOMEN = List.of("Agnes", "Gertrud", "Mechthild", "Adelheid", "Hildegard", "Kunigunde",
            "Elisabeth", "Margarete", "Katharina", "Barbara", "Irmgard", "Walburga", "Ursula", "Anna", "Clara", "Sophia",
            "Brigitte", "Gisela", "Hedwig", "Liesel", "Marthe", "Veronika", "Odilia", "Wiltrud");

    private TownNames() {}

    public static String town(RandomSource random) {
        String first = FIRST.get(random.nextInt(FIRST.size()));
        String last = LAST.get(random.nextInt(LAST.size()));
        // „Eichenau“ statt „Eichenau“-Doppelungen wie „Steinstein“
        if (first.toLowerCase().endsWith(last)) {
            last = "heim";
        }
        return first + last;
    }

    public static String citizen(RandomSource random, boolean feminine) {
        List<String> names = feminine ? WOMEN : MEN;
        return names.get(random.nextInt(names.size()));
    }
}
