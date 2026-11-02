package io.github.sirschrolli.kingdomsatwar.worldgen.site;

import com.mojang.serialization.Codec;
import java.util.Locale;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.levelgen.GenerationStep;

/**
 * Die eigenen Bauwerke der Oberwelt (und der Unterwelt), die an die Stelle der Vanilla-Bauwerke treten: jedes ein ganz eigenes Gegenstück
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
    EMBER_GATE(GenerationStep.Decoration.SURFACE_STRUCTURES),
    /** Muschelfeld: Riesenmuscheln mit Perlen, Truhen und Scherben im Sand (an Stelle der Ozeanruinen). */
    CLAM_FIELD(GenerationStep.Decoration.SURFACE_STRUCTURES),
    /** Korallenpalast: Halle unter einer Glaskuppel, Korallentürme, Thron aus Gold, Schwammgrotte (an Stelle des Ozeanmonuments). */
    CORAL_PALACE(GenerationStep.Decoration.SURFACE_STRUCTURES),
    /** Verschüttetes Kloster: unter der Erde, nur der Glockenturm schaut heraus; Funde im Kies (an Stelle der Pfadruinen). */
    BURIED_ABBEY(GenerationStep.Decoration.UNDERGROUND_STRUCTURES),
    /** Stille Kathedrale: tief im Reich des Sculk, mit Orgel, Fensterrose und Kreischern (an Stelle der antiken Stadt). */
    SILENT_CATHEDRAL(GenerationStep.Decoration.UNDERGROUND_STRUCTURES),
    /** Arena der Prüfungen: ein Kolosseum in einer Höhle, Prüfungsspawner in Käfigen, Tresore auf der Tribüne (an Stelle der Prüfungskammern). */
    TRIAL_ARENA(GenerationStep.Decoration.UNDERGROUND_STRUCTURES),
    /** Raubritterburg: Burg auf einem Hügel im dunklen Wald, mit Bergfried, Verlies und Palas (an Stelle des Waldanwesens). */
    ROBBER_CASTLE(GenerationStep.Decoration.SURFACE_STRUCTURES),
    /**
     * Aschenesse: ein achteckiger Turm im Lavameer der Unterwelt mit Lavakern, Glutkäfigen, Warzengarten, Gebeinkammer und
     * Thronsaal, Brücken nach allen Seiten (an Stelle der Netherfestung). Ganz zuletzt gebaut, damit kein Bewuchs der
     * Unterwelt in ihr wächst.
     */
    ASH_FORGE(GenerationStep.Decoration.TOP_LAYER_MODIFICATION);

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
