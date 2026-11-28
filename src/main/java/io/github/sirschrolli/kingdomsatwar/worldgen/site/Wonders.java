package io.github.sirschrolli.kingdomsatwar.worldgen.site;

import io.github.sirschrolli.kingdomsatwar.worldgen.umbrer.VillageBuilder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.Structure;
import org.jspecify.annotations.Nullable;

/**
 * Die Bauwerke von 2.0 – sechs neue in jeder Dimension – an einem Ort: wie jedes geplant und gebaut wird, sein Name (für
 * {@code /kaw structure} und die Karte), wie oft es in seiner Dimension gewählt wird, wie man es im Test vorbereitet und von
 * wo die Vorführung es zeigt. Alle Bauwerke einer Dimension teilen sich eine Verteilung (je Feld höchstens eines, passt das
 * gewürfelte nicht ins Gelände, wird das nächste versucht) – so überschneiden sie sich nie.
 */
public final class Wonders {
    /** Worauf ein Bauwerk steht – so bereitet der Test seinen Platz vor. */
    public enum Ground {
        /** fester Boden (Gras, Stein), darüber Luft */
        LAND,
        /** frei schwebend: nur Luft (das Bauwerk bringt seinen Fels mit) */
        AIR,
        /** Meeresgrund: Sand, darüber Wasser bis über das Bauwerk */
        SEABED,
        /** Wasserspiegel: Wasser bis y = -1, darüber Luft */
        WATER,
        /** Lavameer der Unterwelt: Lava bis y = -1, ein Ufer aus Netherrack hinten */
        LAVA,
        /** Felsdecke der Unterwelt: über dem Bauwerk Netherrack, darunter Luft und tief unten Lava */
        CEILING,
        /** Steilküste: vorn (+z) Meer, dahinter Strand, hinten (−z) eine Klippe zehn Blöcke über dem Meer */
        COAST
    }

    @FunctionalInterface
    interface Planner {
        Optional<Structure.GenerationStub> plan(Structure.GenerationContext context, SiteTerrain terrain, long seed);
    }

    @FunctionalInterface
    interface Builder {
        void build(VillageBuilder b, SitePiece piece);
    }

    @FunctionalInterface
    public interface Sampler {
        SitePiece sample(BlockPos origin);
    }

    /**
     * Ein Bauwerk: Art, Ort (Kennung für {@code /kaw structure}), Namen, Gewicht in der Verteilung seiner Dimension, wie man im
     * Test den Platz vorbereitet, wo man nach der Reise landet ({@code inside}: im Bauwerk, sonst davor) und die Blicke der
     * Vorführung (je Augenpunkt und Blickziel, in eigenen Koordinaten).
     */
    public record Wonder(SiteKind kind, String place, String english, String deutsch, int weight, Ground ground, boolean inside,
            Planner planner, Builder builder, Sampler sampler, double[][] views) {}

    private static final Map<SiteKind, Wonder> ALL = new EnumMap<>(SiteKind.class);

    static {
        // die Oberwelt
        Overworld.register();
        // die Unterwelt
        Nether.register();
    }

    private Wonders() {}

    static void add(Wonder wonder) {
        if (ALL.put(wonder.kind(), wonder) != null) {
            throw new IllegalStateException("Doppeltes Bauwerk: " + wonder.kind());
        }
    }

    public static @Nullable Wonder get(SiteKind kind) {
        return ALL.get(kind);
    }

    public static List<Wonder> all() {
        return Collections.unmodifiableList(new ArrayList<>(ALL.values()));
    }

    /** Die Bauwerke einer Dimension. */
    public static List<Wonder> of(SiteKind.World world) {
        return ALL.values().stream().filter(w -> w.kind().world() == world).toList();
    }

    static Optional<Structure.GenerationStub> plan(SiteKind kind, Structure.GenerationContext context, SiteTerrain terrain, long seed) {
        Wonder wonder = ALL.get(kind);
        return wonder == null ? Optional.empty() : wonder.planner().plan(context, terrain, seed);
    }

    static void build(SiteKind kind, VillageBuilder b, SitePiece piece) {
        Wonder wonder = ALL.get(kind);
        if (wonder != null) {
            wonder.builder().build(b, piece);
        }
    }

    /** Ein Blick der Vorführung: Augenpunkt und Blickziel in eigenen Koordinaten. */
    static double[] view(double eyeX, double eyeY, double eyeZ, double lookX, double lookY, double lookZ) {
        return new double[] {eyeX, eyeY, eyeZ, lookX, lookY, lookZ};
    }
}
