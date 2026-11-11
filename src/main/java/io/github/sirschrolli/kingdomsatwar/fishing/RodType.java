package io.github.sirschrolli.kingdomsatwar.fishing;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;

/**
 * Die Angeln der Mod (seit 0.87). Jede hat ihre Stärke – neben der gewöhnlichen Angel von Minecraft, die alles kann, aber
 * nichts besonders gut:
 * <ul>
 *     <li><b>Eisenangel</b>: hält dreimal so lange, und die Fangzone im Drill ist größer (leichter einzuholen)</li>
 *     <li><b>Goldangel</b>: bringt Glück – seltenere Fische, öfter ein Schatz; zerbrechlich, aber gut zu verzaubern</li>
 *     <li><b>Drachenangel</b>: von allem etwas – schnellerer Biss, größere Zone, mehr Glück; hält am längsten, feuerfest</li>
 *     <li><b>Schattenangel</b>: stark, wo es dunkel ist – nachts, unter Tage und in der Leere</li>
 *     <li><b>Glutangel</b>: für die Lava gemacht – dort beißt es doppelt so schnell und der Drill ist viel leichter; feuerfest</li>
 *     <li><b>Sternenangel</b>: für das End und die Leere – dort schnell und leicht; überall ein wenig Glück</li>
 * </ul>
 * Die Vorteile wirken zusammen mit Ködern und Verzauberungen (Köder, Glück des Meeres, Drillmeister).
 */
public enum RodType {
    IRON("iron_fishing_rod", 192, 14, Rarity.COMMON, false),
    GOLD("gold_fishing_rod", 48, 22, Rarity.UNCOMMON, false),
    DRAGON("dragon_fishing_rod", 640, 15, Rarity.EPIC, true),
    SHADOW("shadow_fishing_rod", 384, 15, Rarity.RARE, false),
    GLUT("glut_fishing_rod", 512, 15, Rarity.RARE, true),
    STAR("star_fishing_rod", 512, 18, Rarity.RARE, false);

    /**
     * Wo die Angel ausgeworfen ist: worin der Schwimmer treibt, ob es dort dunkel ist (Nacht, unter Tage, Leere – die
     * Lava leuchtet selbst), ob im End.
     */
    public record Place(Medium medium, boolean dark, boolean end) {
        public static Place of(Level level, BlockPos pos, Medium medium) {
            boolean dark = medium == Medium.VOID
                    || medium == Medium.WATER && (!level.isBrightOutside() || level.getBrightness(LightLayer.SKY, pos.above()) < 8);
            return new Place(medium, dark, level.dimension() == Level.END || medium == Medium.VOID);
        }
    }

    private final String id;
    private final int durability;
    private final int enchantability;
    private final Rarity rarity;
    private final boolean fireproof;

    RodType(String id, int durability, int enchantability, Rarity rarity, boolean fireproof) {
        this.id = id;
        this.durability = durability;
        this.enchantability = enchantability;
        this.rarity = rarity;
        this.fireproof = fireproof;
    }

    public String id() {
        return this.id;
    }

    public int durability() {
        return this.durability;
    }

    public int enchantability() {
        return this.enchantability;
    }

    public Rarity rarity() {
        return this.rarity;
    }

    public boolean fireproof() {
        return this.fireproof;
    }

    /** Um welchen Anteil der Fisch hier schneller anbeißt (die Wartezeit wird um so viel kürzer). */
    public float haste(Place place) {
        return switch (this) {
            case DRAGON -> 0.3f;
            case SHADOW -> place.dark() ? 0.4f : 0.0f;
            case GLUT -> place.medium() == Medium.LAVA ? 0.5f : 0.0f;
            case STAR -> place.end() ? 0.5f : 0.0f;
            default -> 0.0f;
        };
    }

    /** Um welchen Anteil die Fangzone im Drill hier größer ist. */
    public float zone(Place place) {
        return switch (this) {
            case IRON -> 0.1f;
            case DRAGON -> 0.15f;
            case SHADOW -> place.dark() ? 0.1f : 0.0f;
            case GLUT -> place.medium() == Medium.LAVA ? 0.25f : 0.0f;
            case STAR -> place.end() ? 0.25f : 0.0f;
            default -> 0.0f;
        };
    }

    /** Wie viel Glück die Angel hier bringt (seltenere Fische, öfter ein Schatz, seltener Kram). */
    public int luck(Place place) {
        return switch (this) {
            case GOLD -> 2;
            case DRAGON, STAR -> 1;
            case SHADOW -> place.dark() ? 2 : 0;
            case GLUT -> place.medium() == Medium.LAVA ? 1 : 0;
            default -> 0;
        };
    }
}
