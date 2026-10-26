package io.github.sirschrolli.kingdomsatwar.shadow.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.sirschrolli.kingdomsatwar.block.GlimmerVinesBlock;
import io.github.sirschrolli.kingdomsatwar.registry.ShadowBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;

/**
 * Glimmerranken: Von Laub oder einer Höhlendecke in der Nähe des Startpunkts hängen mehrere Ranken herab, jede zwei bis
 * {@code maxLength} Blöcke lang, mit glimmender Spitze. Gesucht wird nach unten (unter Baumkronen) und nach oben
 * (Höhlendecken).
 */
public record HangingVinesFeature(int tries, int maxLength) implements Feature {
    public static final MapCodec<HangingVinesFeature> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Codec.intRange(1, 64).fieldOf("tries").forGetter(HangingVinesFeature::tries),
            Codec.intRange(2, 24).fieldOf("max_length").forGetter(HangingVinesFeature::maxLength)
    ).apply(i, HangingVinesFeature::new));

    @Override
    public MapCodec<HangingVinesFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
        int placed = 0;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int i = 0; i < this.tries; i++) {
            pos.set(origin.getX() + random.nextInt(9) - 4, origin.getY() + random.nextInt(5) - 2, origin.getZ() + random.nextInt(9) - 4);
            BlockPos start = findHangingSpot(level, pos);
            if (start != null) {
                hang(level, random, start);
                placed++;
            }
        }
        return placed > 0;
    }

    /** Luft direkt unter einer Decke (Laub oder fester Stein), höchstens 12 Blöcke über oder unter {@code from}. */
    private static BlockPos findHangingSpot(WorldGenLevel level, BlockPos from) {
        BlockPos.MutableBlockPos p = from.mutable();
        for (int k = 0; k < 12; k++) {
            if (isSpot(level, p)) {
                return p.immutable();
            }
            p.move(Direction.DOWN);
        }
        p.set(from);
        for (int k = 0; k < 12; k++) {
            if (isSpot(level, p)) {
                return p.immutable();
            }
            p.move(Direction.UP);
        }
        return null;
    }

    private static boolean isSpot(WorldGenLevel level, BlockPos pos) {
        if (!level.isEmptyBlock(pos) || !level.isEmptyBlock(pos.below())) {
            return false;
        }
        BlockState ceiling = level.getBlockState(pos.above());
        return ceiling.is(BlockTags.LEAVES) || ceiling.isFaceSturdy(level, pos.above(), Direction.DOWN);
    }

    private void hang(WorldGenLevel level, RandomSource random, BlockPos start) {
        int length = 2 + random.nextInt(Math.max(1, this.maxLength - 1));
        BlockPos.MutableBlockPos p = start.mutable();
        int made = 0;
        while (made < length && level.isEmptyBlock(p) && level.isEmptyBlock(p.below())) {
            made++;
            p.move(Direction.DOWN);
        }
        // von oben nach unten: Strang, zuletzt die glimmende Spitze
        BlockPos.MutableBlockPos q = start.mutable();
        for (int k = 0; k < made; k++) {
            BlockState state = k == made - 1
                    ? ShadowBlocks.GLIMMER_VINES.get().defaultBlockState().setValue(GlimmerVinesBlock.AGE, 17 + random.nextInt(8))
                    : ShadowBlocks.GLIMMER_VINES_PLANT.get().defaultBlockState();
            level.setBlock(q, state, 2);
            q.move(Direction.DOWN);
        }
    }
}
