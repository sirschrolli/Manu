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
}
