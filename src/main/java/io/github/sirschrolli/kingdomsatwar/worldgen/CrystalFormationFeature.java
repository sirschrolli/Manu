package io.github.sirschrolli.kingdomsatwar.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;

/**
 * Ein Herz- oder Manakristall ({@code crystal}) auf einem Höhlenboden: In der Säule um den Ausgangspunkt (bis
 * {@code search} Blöcke darüber und darunter) sucht er von oben her die erste Stelle mit Luft über festem Gestein, Platz
 * darüber und ohne Himmel – und schaut in eine zufällige Richtung.
 */
public record CrystalFormationFeature(BlockState crystal, int search) implements Feature {
    public static final MapCodec<CrystalFormationFeature> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            BlockState.CODEC.fieldOf("crystal").forGetter(CrystalFormationFeature::crystal),
            Codec.intRange(1, 48).fieldOf("search").forGetter(CrystalFormationFeature::search)
    ).apply(i, CrystalFormationFeature::new));

    @Override
    public MapCodec<CrystalFormationFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
        int surface = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, origin.getX(), origin.getZ());
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int dy = this.search; dy >= -this.search; dy--) {
            pos.set(origin.getX(), origin.getY() + dy, origin.getZ());
            if (pos.getY() < surface - 6 && fits(level, pos)) {
                BlockState state = this.crystal.hasProperty(HorizontalDirectionalBlock.FACING)
                        ? this.crystal.setValue(HorizontalDirectionalBlock.FACING, Direction.Plane.HORIZONTAL.getRandomDirection(random))
                        : this.crystal;
                level.setBlock(pos, state, Block.UPDATE_CLIENTS);
                return true;
            }
        }
        return false;
    }

    /** Luft (kein Wasser) über festem Gestein, darüber noch ein Block Luft. */
    private static boolean fits(WorldGenLevel level, BlockPos pos) {
        if (!level.getBlockState(pos).isAir() || !level.getFluidState(pos).isEmpty()) {
            return false;
        }
        BlockPos below = pos.below();
        BlockState ground = level.getBlockState(below);
        if (!ground.is(BlockTags.BASE_STONE_OVERWORLD) || !ground.isFaceSturdy(level, below, Direction.UP)) {
            return false;
        }
        BlockPos above = pos.above();
        return level.getBlockState(above).isAir() && level.getFluidState(above).isEmpty();
    }
}
