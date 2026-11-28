package io.github.sirschrolli.kingdomsatwar.worldgen.overworld.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.github.sirschrolli.kingdomsatwar.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;

/**
 * Eiszapfen unter Überhängen: sucht rund um den Ursprung Stellen, an denen festes Gestein oder Eis über freier Luft
 * hängt (Felsdächer, Klippenränder, Höhleneingänge nahe der Oberfläche), und lässt dort ein bis drei Zapfen herabwachsen.
 */
public record IcicleFeature(int tries) implements Feature {
    public static final MapCodec<IcicleFeature> CODEC = Codec.intRange(1, 128).fieldOf("tries").xmap(IcicleFeature::new, IcicleFeature::tries);

    @Override
    public MapCodec<IcicleFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
        BlockState icicle = ModBlocks.ICICLE.get().defaultBlockState();
        boolean placed = false;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int t = 0; t < this.tries; t++) {
            int x = origin.getX() + random.nextInt(13) - 6;
            int z = origin.getZ() + random.nextInt(13) - 6;
            int top = Kit.surface(level, x, z);
            // von der Oberfläche abwärts die erste Unterseite eines Überhangs suchen
            boolean solidAbove = false;
            for (int y = top - 1; y > top - 18; y--) {
                pos.set(x, y, z);
                BlockState here = level.getBlockState(pos);
                if (here.isAir()) {
                    if (solidAbove) {
                        BlockPos ceiling = pos.above();
                        BlockState roof = level.getBlockState(ceiling);
                        if (!roof.is(BlockTags.LEAVES) && roof.isFaceSturdy(level, ceiling, Direction.DOWN)) {
                            int length = 1 + random.nextInt(3);
                            for (int k = 0; k < length; k++) {
                                BlockPos at = pos.below(k);
                                if (!level.getBlockState(at).isAir() || !level.getBlockState(at.below()).isAir()) {
                                    break;
                                }
                                level.setBlock(at, icicle, 2);
                                placed = true;
                            }
                        }
                        break;
                    }
                } else if (!here.getFluidState().isEmpty()) {
                    break;              // Wasser oder Lava: hier wachsen keine Zapfen
                } else {
                    solidAbove = true;
                }
            }
        }
        return placed;
    }
}
