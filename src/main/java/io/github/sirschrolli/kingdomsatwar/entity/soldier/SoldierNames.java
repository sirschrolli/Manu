package io.github.sirschrolli.kingdomsatwar.entity.soldier;

import java.util.List;
import net.minecraft.util.RandomSource;

/** Das Namensbuch der Soldaten – mittelalterliche Namen, die auf Deutsch und Englisch passen. */
public final class SoldierNames {
    private static final List<String> MASCULINE = List.of(
            "Albrecht", "Konrad", "Heinrich", "Gunther", "Lothar", "Wolfram", "Dietrich", "Friedrich", "Ulrich", "Bernhard",
            "Gottfried", "Rupert", "Siegfried", "Walther", "Eberhard", "Leopold", "Arnold", "Roland", "Tristan", "Parzival",
            "Gawain", "Edmund", "Aldric", "Cedric", "Godric", "Oswin", "Berthold", "Hartmut", "Ludwig", "Otto",
            "Reinhard", "Volker", "Hagen", "Kuno", "Ortwin", "Benno", "Anselm", "Egon", "Theobald", "Wilhelm");
    private static final List<String> FEMININE = List.of(
            "Hildegard", "Mathilda", "Adelheid", "Gisela", "Brunhild", "Kunigunde", "Irmgard", "Ida", "Gertrud", "Elsbeth",
            "Richardis", "Hedwig", "Isolde", "Walburga", "Gunda", "Rosamund");

    private SoldierNames() {}

    public static String random(RandomSource random, boolean feminine) {
        List<String> names = feminine ? FEMININE : MASCULINE;
        return names.get(random.nextInt(names.size()));
    }
}
