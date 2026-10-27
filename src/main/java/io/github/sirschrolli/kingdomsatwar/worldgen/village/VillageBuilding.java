package io.github.sirschrolli.kingdomsatwar.worldgen.village;

import java.util.Locale;

/**
 * Die Gebäude eines Dorfs mit ihrer Grundfläche (Breite × Tiefe; vorn liegt die Tür zur Straße) und ihrer Bauhöhe über
 * dem Fußboden. {@code outskirts}: gehört an den Dorfrand (Felder, Mühle, Scheune, Schafweide, Fischerhütte).
 */
public enum VillageBuilding {
    HOUSE_SMALL(7, 7, 11, false),
    HOUSE(9, 9, 15, false),
    HOUSE_LARGE(11, 9, 17, false),
    SMITHY(11, 9, 13, false),
    BAKERY(9, 9, 14, false),
    TAVERN(13, 11, 17, false),
    CHAPEL(9, 15, 22, false),
    SCRIPTORIUM(9, 9, 15, false),
    MAP_HOUSE(7, 9, 14, false),
    HERB_HOUSE(9, 9, 13, false),
    BUTCHERY(9, 7, 12, false),
    TANNERY(9, 9, 12, false),
    MASONRY(11, 9, 12, false),
    BOWYER(9, 9, 13, false),
    SHEEPFOLD(13, 11, 11, true),
    FIELD(11, 13, 5, true),
    BARN(11, 9, 14, true),
    WINDMILL(9, 9, 26, true),
    WATCHTOWER(5, 5, 17, true),
    /** Fischerhütte am Ufer: vorn die Hütte, hinten ein Steg ins Wasser. */
    FISHING_HUT(7, 10, 10, true),
    MARKET_STALL(5, 4, 6, false);

    public final int width;
    public final int depth;
    public final int height;
    public final boolean outskirts;

    VillageBuilding(int width, int depth, int height, boolean outskirts) {
        this.width = width;
        this.depth = depth;
        this.height = height;
        this.outskirts = outskirts;
    }

    public String id() {
        return this.name().toLowerCase(Locale.ROOT);
    }
}
