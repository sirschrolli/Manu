package io.github.sirschrolli.kingdomsatwar.fae.feature;

import io.github.sirschrolli.kingdomsatwar.registry.FaeBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.GrowingPlantHeadBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * Bauhilfen für die Features des Feenreichs: nur innerhalb der Chunks schreiben, die ein Feature verändern darf (der
 * eigene und die acht Nachbarn), Luft und Pflanzen ersetzen, festen Fels in Ruhe lassen, Boden erkennen und Leuchtranken
 * aufhängen.
 */
public final class FaeBuild {
    private FaeBuild() {}

    /** Darf an dieser Stelle geschrieben werden (eigener Chunk oder ein Nachbar)? */
    static boolean inReach(BlockPos origin, BlockPos pos) {
        return Math.abs(SectionPos.blockToSectionCoord(pos.getX()) - SectionPos.blockToSectionCoord(origin.getX())) <= 1
                && Math.abs(SectionPos.blockToSectionCoord(pos.getZ()) - SectionPos.blockToSectionCoord(origin.getZ())) <= 1
                && pos.getY() > 1 && pos.getY() < 254;
    }

    /** Ist hier Platz (Luft, Gras, Blumen, Schnee – aber kein Wasser)? */
    static boolean open(BlockState state) {
        return state.isAir() || state.canBeReplaced() && state.getFluidState().isEmpty() || state.is(BlockTags.REPLACEABLE_BY_TREES);
    }

    /** Wie {@link #open}, aber auch Wasser. */
    static boolean openOrWater(BlockState state) {
        return open(state) || state.is(Blocks.WATER);
    }

    /** Setzt den Block, wenn hier Platz ist. */
    static boolean place(WorldGenLevel level, BlockPos origin, BlockPos pos, BlockState state) {
        if (!inReach(origin, pos) || !open(level.getBlockState(pos))) {
            return false;
        }
        level.setBlock(pos, state, 2);
        return true;
    }

    /** Setzt den Block auf jeden Fall (auch in Fels), solange die Stelle erreichbar ist. */
    static boolean force(WorldGenLevel level, BlockPos origin, BlockPos pos, BlockState state) {
        if (!inReach(origin, pos)) {
            return false;
        }
        level.setBlock(pos, state, 2);
        return true;
    }

    /** Boden, auf dem im Feenreich etwas wächst (Feengras, Feenerde, Feenmoos – und gewöhnliche Erde). */
    public static boolean soil(BlockState state) {
        return state.is(FaeBlocks.FAE_GRASS.get()) || state.is(FaeBlocks.FAE_SOIL.get()) || state.is(FaeBlocks.FAE_MOSS.get())
                || state.is(BlockTags.DIRT) || state.is(BlockTags.SUBSTRATE_OVERWORLD);
    }

    /** Fester Grund für Felsen und Ruinen (Boden oder Feenstein). */
    static boolean ground(BlockState state) {
        return soil(state) || state.is(FaeBlocks.FAESTONE.base().get()) || state.is(Blocks.SAND) || state.is(Blocks.CALCITE)
                || state.is(BlockTags.BASE_STONE_OVERWORLD);
    }

    /** Oberkante des Bodens (ohne Laub) in dieser Spalte: die erste Luft über dem Grund. */
    static int surface(WorldGenLevel level, int x, int z) {
        return level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
    }

    /** Stellt die Pflanze auf, wenn sie dort wachsen kann. */
    static boolean plant(WorldGenLevel level, BlockPos origin, BlockPos pos, BlockState plant) {
        if (!inReach(origin, pos) || !level.getBlockState(pos).isAir() || !plant.canSurvive(level, pos)) {
            return false;
        }
        level.setBlock(pos, plant, 2);
        return true;
    }

    /**
     * Lässt unter {@code from} eine Leuchtranke herabhängen: ein bis {@code max} Blöcke lang, nie bis auf den Boden, mit
     * leuchtender Spitze. Sie wächst nicht weiter (sonst hingen die Wälder bald bis zum Grund voll).
     */
    static int hangVines(WorldGenLevel level, BlockPos origin, BlockPos from, int max, RandomSource random) {
        int length = 1 + random.nextInt(Math.max(1, max));
        BlockPos.MutableBlockPos pos = from.mutable().move(Direction.DOWN);
        int made = 0;
        while (made < length && inReach(origin, pos) && level.getBlockState(pos).isAir() && level.getBlockState(pos.below()).isAir()) {
            made++;
            pos.move(Direction.DOWN);
        }
        if (made == 0) {
            return 0;
        }
        BlockState body = FaeBlocks.GLOW_VINES_PLANT.get().defaultBlockState();
        BlockState head = FaeBlocks.GLOW_VINES.get().defaultBlockState().setValue(GrowingPlantHeadBlock.AGE, GrowingPlantHeadBlock.MAX_AGE);
        BlockPos.MutableBlockPos at = from.mutable();
        for (int i = 1; i <= made; i++) {
            at.move(Direction.DOWN);
            level.setBlock(at, i == made ? head : body, 2);
        }
        return made;
    }
}
