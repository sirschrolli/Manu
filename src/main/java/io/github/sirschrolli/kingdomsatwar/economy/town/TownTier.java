package io.github.sirschrolli.kingdomsatwar.economy.town;

import com.mojang.serialization.Codec;
import java.util.Locale;
import net.minecraft.util.StringRepresentable;

/**
 * Die Stufen einer Siedlung – mit der Zahl der Bewohner wächst sie vom Lager über Weiler, Dorf und Marktflecken zur
 * Stadt. Jede Stufe erlaubt größere Bauten und reicht weiter ins Land (Radius um das Stadtbanner).
 */
public enum TownTier implements StringRepresentable {
    CAMP(0, 32),
    HAMLET(4, 44),
    VILLAGE(8, 56),
    MARKET_TOWN(14, 68),
    CITY(22, 84);

    public static final Codec<TownTier> CODEC = StringRepresentable.fromEnum(TownTier::values);

    private final int residents;
    private final int radius;

    TownTier(int residents, int radius) {
        this.residents = residents;
        this.radius = radius;
    }

    /** Ab wie vielen Bewohnern die Siedlung diese Stufe erreicht. */
    public int residents() {
        return this.residents;
    }

    /** Wie weit die Siedlung um das Banner reicht (Bauplätze, Arbeit der Bewohner). */
    public int radius() {
        return this.radius;
    }

    public String id() {
        return this.name().toLowerCase(Locale.ROOT);
    }

    public String translationKey() {
        return "town.kingdomsatwar.tier." + this.id();
    }

    @Override
    public String getSerializedName() {
        return this.id();
    }

    /** Die nächste Stufe (die Stadt hat keine). */
    public TownTier next() {
        return this == CITY ? CITY : values()[this.ordinal() + 1];
    }

    public static TownTier of(int residents) {
        TownTier tier = CAMP;
        for (TownTier t : values()) {
            if (residents >= t.residents) {
                tier = t;
            }
        }
        return tier;
    }
}
