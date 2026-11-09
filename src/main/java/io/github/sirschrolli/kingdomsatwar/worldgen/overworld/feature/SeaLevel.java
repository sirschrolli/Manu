package io.github.sirschrolli.kingdomsatwar.worldgen.overworld.feature;

import net.minecraft.world.level.chunk.ChunkGenerator;
import org.jspecify.annotations.Nullable;

/**
 * Der Meeresspiegel, nach dem sich die Landschaften des Meeresgrunds richten (heiße Quellen und Leuchtfelder nur in der
 * Tiefe, Felsnadeln nur in mittlerer Tiefe, Muschelbänke nur im Seichten). In der flachen Testwelt liegt er bei -63,
 * unter jedem Becken – Tests geben ihn darum für ihren Thread vor.
 */
public final class SeaLevel {
    private static final ThreadLocal<Integer> OVERRIDE = new ThreadLocal<>();

    private SeaLevel() {}

    public static int of(ChunkGenerator generator) {
        Integer override = OVERRIDE.get();
        return override != null ? override : generator.getSeaLevel();
    }

    /** Nur für Tests: gibt den Meeresspiegel für diesen Thread vor ({@code null}: wieder der echte). */
    public static void override(@Nullable Integer sea) {
        if (sea == null) {
            OVERRIDE.remove();
        } else {
            OVERRIDE.set(sea);
        }
    }
}
