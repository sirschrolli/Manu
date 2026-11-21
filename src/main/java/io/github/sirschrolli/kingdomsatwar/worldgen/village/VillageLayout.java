package io.github.sirschrolli.kingdomsatwar.worldgen.village;

import java.util.Locale;
import net.minecraft.util.RandomSource;

/**
 * Der Grundriss eines Dorfs – so sehen nicht alle Dörfer gleich aus:
 * <ul>
 *     <li><b>Haufendorf:</b> ein gepflasterter Platz, von dem drei oder vier Wege ausgehen, dazu Seitengassen;</li>
 *     <li><b>Straßendorf:</b> eine lange Dorfstraße mit einem kleinen Marktplatz in der Mitte und kurzen Quergassen;</li>
 *     <li><b>Angerdorf:</b> ein langer Anger (Wiese mit Teich, Bäumen und Brunnen), an beiden Seiten eine Straße, die
 *     Häuser schauen auf den Anger; an den Enden geht es hinaus (im Lehmland eine Oase);</li>
 *     <li><b>Rundling:</b> eine große runde Dorfwiese mit der Dorflinde, ringsum der Dorfring mit den Häusern, nur ein
 *     oder zwei Wege führen hinaus zu den Feldern.</li>
 * </ul>
 * Pfahldörfer im Moor sind Haufen- oder Straßendörfer (für eine Wiese fehlt dort der Boden).
 */
public enum VillageLayout {
    HAUFENDORF, STRASSENDORF, ANGERDORF, RUNDLING;

    public String id() {
        return this.name().toLowerCase(Locale.ROOT);
    }

    /** Taugt der Grundriss für diese Bauweise? */
    public boolean suits(VillageStyle style) {
        return style != VillageStyle.STILT || this == HAUFENDORF || this == STRASSENDORF;
    }

    /** Ein Grundriss nach den Gewohnheiten der Bauweise. */
    static VillageLayout pick(VillageStyle style, RandomSource random) {
        int[] weights = switch (style) {
            case TIMBER -> new int[] {30, 25, 25, 20};
            case NORDIC -> new int[] {35, 30, 15, 20};
            case CLAY -> new int[] {35, 25, 20, 20};
            case STILT -> new int[] {55, 45, 0, 0};
        };
        int total = 0;
        for (int w : weights) {
            total += w;
        }
        int roll = random.nextInt(total);
        for (VillageLayout layout : values()) {
            roll -= weights[layout.ordinal()];
            if (roll < 0) {
                return layout;
            }
        }
        return HAUFENDORF;
    }
}
