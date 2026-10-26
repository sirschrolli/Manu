package io.github.sirschrolli.kingdomsatwar.worldgen.lair;

import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import java.util.EnumMap;
import java.util.Map;

/** Die Nest-Entwürfe der sechs Drachenarten. */
public final class LairDesigns {
    private static final Map<DragonType, LairDesign> DESIGNS = new EnumMap<>(DragonType.class);

    static {
        DESIGNS.put(DragonType.FIRE, new FireLairDesign());
        DESIGNS.put(DragonType.ICE, new IceLairDesign());
        DESIGNS.put(DragonType.NATURE, new NatureLairDesign());
        DESIGNS.put(DragonType.EARTH, new EarthLairDesign());
        DESIGNS.put(DragonType.LIGHTNING, new LightningLairDesign());
    }

    private LairDesigns() {}

    public static LairDesign of(DragonType element) {
        return DESIGNS.getOrDefault(element, DESIGNS.get(DragonType.FIRE));
    }

    /** Arten, deren Nest schon gebaut werden kann (die übrigen folgen mit ihren Bossen). */
    public static boolean available(DragonType element) {
        return DESIGNS.containsKey(element);
    }
}
