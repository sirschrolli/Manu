package io.github.sirschrolli.kingdomsatwar.entity.combat;

import com.mojang.serialization.Codec;
import java.util.Locale;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;

/**
 * Das Temperament eines einzelnen Monsters – sein Kampfstil, ausgewürfelt, wenn es in die Welt kommt, und gespeichert.
 * Zwei Wiedergänger derselben Art kämpfen dadurch ganz verschieden:
 * <ul>
 *     <li><b>Ungestüm</b> – schneller auf den Beinen und greift öfter mit seinen besonderen Angriffen an.</li>
 *     <li><b>Lauernd</b> – umkreist seine Beute und stößt aus der Flanke zu, weicht Pfeilen aus und springt nach einem
 *     Treffer zurück.</li>
 *     <li><b>Zäh</b> – langsamer, aber gepanzert und standfest; deckt sich und schlägt zurück, wenn man in die Deckung
 *     schlägt.</li>
 *     <li><b>Rasend</b> – gerät verwundet in Raserei: schneller, stärker, angriffslustiger.</li>
 *     <li><b>Anführer</b> – selten, kräftiger als die anderen; ruft mit einem Kampfschrei seinesgleichen herbei und
 *     stachelt sie an.</li>
 * </ul>
 */
public enum Temper implements StringRepresentable {
    UNGESTUEM(26),
    LAUERND(22),
    ZAEH(22),
    RASEND(20),
    ANFUEHRER(10);

    public static final Codec<Temper> CODEC = StringRepresentable.fromEnum(Temper::values);

    private final int weight;

    Temper(int weight) {
        this.weight = weight;
    }

    @Override
    public String getSerializedName() {
        return this.name().toLowerCase(Locale.ROOT);
    }

    /** Zufälliges Temperament nach den Gewichten. */
    public static Temper roll(RandomSource random) {
        int total = 0;
        for (Temper t : values()) {
            total += t.weight;
        }
        int r = random.nextInt(total);
        for (Temper t : values()) {
            r -= t.weight;
            if (r < 0) {
                return t;
            }
        }
        return UNGESTUEM;
    }

    /** Wie viel schneller die besonderen Angriffe wiederkommen (Faktor auf die Wartezeit). */
    public float tempo() {
        return switch (this) {
            case UNGESTUEM -> 0.72f;
            case LAUERND -> 0.95f;
            case ZAEH -> 1.1f;
            case RASEND -> 1.0f;
            case ANFUEHRER -> 0.9f;
        };
    }
}
