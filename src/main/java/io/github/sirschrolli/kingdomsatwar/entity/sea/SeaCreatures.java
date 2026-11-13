package io.github.sirschrolli.kingdomsatwar.entity.sea;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;

/** Kleine Hilfen für die Wesen des Meeres: Wasser, Oberfläche, Grund und die Art des Meeres. */
public final class SeaCreatures {
    private SeaCreatures() {}

    public static boolean isWater(BlockGetter level, BlockPos pos) {
        return level.getFluidState(pos).is(FluidTags.WATER);
    }

    /**
     * Die Höhe der Wasseroberfläche über {@code pos} (die Unterkante des ersten Blocks ohne Wasser), höchstens
     * {@code max} Blöcke darüber gesucht; {@link Integer#MIN_VALUE}, wenn {@code pos} nicht im Wasser liegt oder die
     * Oberfläche weiter oben liegt.
     */
    public static int surfaceAbove(BlockGetter level, BlockPos pos, int max) {
        if (!isWater(level, pos)) {
            return Integer.MIN_VALUE;
        }
        BlockPos.MutableBlockPos p = pos.mutable();
        for (int i = 1; i <= max; i++) {
            p.setY(pos.getY() + i);
            if (!isWater(level, p)) {
                return level.getBlockState(p).isAir() ? p.getY() : Integer.MIN_VALUE;
            }
        }
        return Integer.MIN_VALUE;
    }

    /** Wie tief es unter {@code pos} noch Wasser hat (Blöcke bis zum Grund, höchstens {@code max}). */
    public static int depthBelow(BlockGetter level, BlockPos pos, int max) {
        BlockPos.MutableBlockPos p = pos.mutable();
        for (int i = 1; i <= max; i++) {
            p.setY(pos.getY() - i);
            if (!isWater(level, p)) {
                return i - 1;
            }
        }
        return max;
    }

    /** Ein Block Wasser, über und unter dem auch Wasser steht (genug Platz für ein schwimmendes Wesen). */
    public static boolean openWater(LevelReader level, BlockPos pos) {
        return isWater(level, pos) && isWater(level, pos.above()) && isWater(level, pos.below());
    }

    /** Warme Meere (warm, lauwarm und ihre Tiefen). */
    public static boolean warm(Holder<Biome> biome) {
        return biome.is(Biomes.WARM_OCEAN) || biome.is(Biomes.LUKEWARM_OCEAN) || biome.is(Biomes.DEEP_LUKEWARM_OCEAN);
    }

    /** Kalte und gefrorene Meere. */
    public static boolean cold(Holder<Biome> biome) {
        return biome.is(Biomes.COLD_OCEAN) || biome.is(Biomes.DEEP_COLD_OCEAN) || biome.is(Biomes.FROZEN_OCEAN)
                || biome.is(Biomes.DEEP_FROZEN_OCEAN);
    }

    public static boolean deep(Holder<Biome> biome) {
        return biome.is(BiomeTags.IS_DEEP_OCEAN);
    }
}
