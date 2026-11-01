package io.github.sirschrolli.kingdomsatwar.worldgen.site;

import com.mojang.serialization.Codec;
import java.util.Locale;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.levelgen.GenerationStep;

/**
 * Die eigenen Bauwerke der Oberwelt, die an die Stelle der Vanilla-Bauwerke treten: jedes ein ganz eigenes Gegenstück
 * (andere Gestalt, anderes Thema), das dieselbe Rolle in der Welt spielt. Was man unbedingt braucht – das Portal ins
 * Ende, das Herz des Meeres –, findet man dort weiterhin.
 */
public enum SiteKind implements StringRepresentable {
    /** Sternensturz: ein Meteoritenkrater, in dessen Kern das Portal ins Ende liegt (an Stelle der Festung). */
    FALLEN_STAR(GenerationStep.Decoration.STRONGHOLDS),
    /** Wasserstollen der Alten: gewölbte Kanäle und Säulenzisternen unter der Erde (an Stelle der Mine). */
    AQUEDUCT(GenerationStep.Decoration.UNDERGROUND_STRUCTURES),
    /** Gezeitenschrein: ein kleiner Schrein am Strand, unter dem Altar das Herz des Meeres (an Stelle des Schatzes). */
    TIDE_SHRINE(GenerationStep.Decoration.SURFACE_STRUCTURES),
    /** Gebeine eines Seedrachen am Meeresgrund oder Strand, im Brustkorb die verschluckte Fracht (an Stelle des Wracks). */
    SEA_DRAGON(GenerationStep.Decoration.SURFACE_STRUCTURES),
    /** Karawanserei: Ruine einer Herberge in der Wüste mit Schatzkeller unter dem Brunnen (an Stelle des Wüstentempels). */
    CARAVANSERAI(GenerationStep.Decoration.SURFACE_STRUCTURES),
    /** Schlangentempel: ein steinerner Schlangenkopf im Dschungel, Falle im Schlund, Halle darunter (an Stelle des Dschungeltempels). */
    SERPENT_TEMPLE(GenerationStep.Decoration.TOP_LAYER_MODIFICATION),
    /** Hexenbaum: ein hohler, abgestorbener Riesenbaum im Sumpf, darin eine Hexe (an Stelle der Sumpfhütte). */
    WITCH_TREE(GenerationStep.Decoration.SURFACE_STRUCTURES),
    /** Frostgrotte: die Schneehöhle eines Einsiedlers mit verstecktem Keller (an Stelle des Iglus). */
    FROST_GROTTO(GenerationStep.Decoration.SURFACE_STRUCTURES),
    /** Räuberlager: eine Palisade der Wegelagerer mit Wachgerüst und einem Gefangenen (an Stelle des Plünderer-Außenpostens). */
    BANDIT_CAMP(GenerationStep.Decoration.TOP_LAYER_MODIFICATION),
    /** Köhlerei: ein rauchender Meiler auf einer Lichtung, Unterstand und Versteck (an Stelle des verlassenen Lagers). */
    CHARCOAL_CAMP(GenerationStep.Decoration.TOP_LAYER_MODIFICATION),
    /** Glutpforte: ein zerbrochenes Tor aus Obsidian zwischen Basaltsäulen, ringsum Glut (an Stelle der Portalruine). */
    EMBER_GATE(GenerationStep.Decoration.SURFACE_STRUCTURES);

    public static final Codec<SiteKind> CODEC = StringRepresentable.fromEnum(SiteKind::values);

    private final GenerationStep.Decoration step;

    SiteKind(GenerationStep.Decoration step) {
        this.step = step;
    }

    public String id() {
        return this.name().toLowerCase(Locale.ROOT);
    }

    public GenerationStep.Decoration step() {
        return this.step;
    }

    @Override
    public String getSerializedName() {
        return this.id();
    }
}
