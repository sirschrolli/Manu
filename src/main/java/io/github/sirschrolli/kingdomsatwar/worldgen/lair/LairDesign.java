package io.github.sirschrolli.kingdomsatwar.worldgen.lair;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.Structure;

/** Entwurf eines Drachennests: Größe, Bodenhöhe und der eigentliche Bau (Chunk für Chunk über {@link LairContext}). */
public interface LairDesign {
    /** Waagerechte Ausdehnung um die Mitte (Blöcke). */
    int radius();

    /** So weit reicht der Bau unter die Mitte. */
    int depthBelow();

    /** So weit reicht der Bau über die Mitte. */
    int heightAbove();

    /** Höhe des Nestbodens (Mitte), aus dem Gelände berechnet. */
    int floorHeight(Structure.GenerationContext context, int x, int z);

    /** Baut den Teil des Nests, der im aktuellen Chunk liegt. */
    void build(LairContext ctx);

    /** Lage des Nestkerns (wacht über das Erwachen des Bosses). */
    default BlockPos corePos(BlockPos center) {
        return center.below(7);
    }

    /** Größter Radius des Nest-Bioms (höchstens 124 Blöcke – weiter reicht ein Bauwerk nicht). */
    default int zoneRadius() {
        return this.radius();
    }

    /** Rand des Nest-Bioms in dieser Richtung (unregelmäßig, aber in jedem Chunk gleich). */
    default double zoneEdge(LairContext ctx, double angle) {
        int r = this.zoneRadius();
        return r <= this.radius() ? r : r - 20 + 20 * ctx.angleNoise(angle, 3.0, 61);
    }

    /** Liegt der Punkt im Nest-Biom? */
    default boolean inZone(LairContext ctx, int x, int z) {
        double dx = x - ctx.center.getX();
        double dz = z - ctx.center.getZ();
        return Math.sqrt(dx * dx + dz * dz) < this.zoneEdge(ctx, Math.atan2(dz, dx));
    }

    /** Gestaltet die Landschaft des Nest-Bioms außerhalb des eigentlichen Nests (nur im aktuellen Chunk). */
    default void buildZone(LairContext ctx) {
    }
}
