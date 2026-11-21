package io.github.sirschrolli.kingdomsatwar.worldgen.overworld.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.sirschrolli.kingdomsatwar.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;

/**
 * Ein umgestürzter Stamm: neben seinem Stumpf liegt der Baum der Länge nach am Boden, bemoost und von Leuchtkappen
 * besiedelt. Der Stamm folgt dem Gelände; wo der Boden wegbricht, endet er.
 */
public record FallenTrunkFeature(BlockState log, int minLength, int maxLength) implements Feature {
    public static final MapCodec<FallenTrunkFeature> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            BlockState.CODEC.fieldOf("log").forGetter(FallenTrunkFeature::log),
            Codec.intRange(2, 16).fieldOf("min_length").forGetter(FallenTrunkFeature::minLength),
            Codec.intRange(2, 16).fieldOf("max_length").forGetter(FallenTrunkFeature::maxLength)
    ).apply(i, FallenTrunkFeature::new));

    @Override
    public MapCodec<FallenTrunkFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
        if (!Kit.dryGround(level, origin)) {
            return false;
        }
        Direction dir = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        int length = this.minLength + random.nextInt(Math.max(1, this.maxLength - this.minLength + 1));
        // erst prüfen, ob der Stamm Platz hat: jeder Block braucht Boden darunter, das Gelände darf nur sanft fallen
        BlockPos start = origin.relative(dir, 2);
        int y = origin.getY();
        int fits = 0;
        for (int i = 0; i < length; i++) {
            BlockPos p = start.relative(dir, i);
            int top = Kit.surface(level, p.getX(), p.getZ());
            if (Math.abs(top - y) > 1 || !Kit.open(level.getBlockState(new BlockPos(p.getX(), top, p.getZ())))
                    || !Kit.ground(level.getBlockState(new BlockPos(p.getX(), top - 1, p.getZ())))) {
                break;
            }
            y = top;
            fits++;
        }
        if (fits < this.minLength) {
            return false;
        }
        // der Stumpf (ein, zwei Blöcke hoch, oben abgesplittert)
        BlockState upright = Kit.log(this.log, Direction.Axis.Y);
        int stump = 1 + random.nextInt(2);
        for (int k = 0; k < stump; k++) {
            Kit.setIfOpen(level, origin.above(k), upright);
        }
        if (random.nextFloat() < 0.6f) {
            Kit.setIfOpen(level, origin.above(stump), Blocks.MOSS_CARPET.defaultBlockState());
        }
        // der liegende Stamm
        BlockState lying = Kit.log(this.log, dir.getAxis());
        y = origin.getY();
        for (int i = 0; i < fits; i++) {
            BlockPos column = start.relative(dir, i);
            y = Kit.surface(level, column.getX(), column.getZ());
            BlockPos p = new BlockPos(column.getX(), y, column.getZ());
            level.setBlock(p, lying, 2);
            BlockPos top = p.above();
            if (!level.getBlockState(top).isAir()) {
                continue;
            }
            float r = random.nextFloat();
            if (r < 0.45f) {
                level.setBlock(top, Blocks.MOSS_CARPET.defaultBlockState(), 2);
            } else if (r < 0.6f) {
                BlockState cap = ModBlocks.GLOWCAP.get().defaultBlockState();
                if (cap.canSurvive(level, top)) {
                    level.setBlock(top, cap, 2);
                }
            }
        }
        // Moos und Laub rund um den Stamm
        for (int i = -1; i <= fits; i++) {
            BlockPos column = start.relative(dir, i);
            for (Direction side : new Direction[] {dir.getClockWise(), dir.getCounterClockWise()}) {
                BlockPos beside = column.relative(side);
                int top = Kit.surface(level, beside.getX(), beside.getZ());
                BlockPos at = new BlockPos(beside.getX(), top, beside.getZ());
                if (random.nextFloat() < 0.4f && level.getBlockState(at).isAir() && Kit.ground(level.getBlockState(at.below()))) {
                    level.setBlock(at, Blocks.MOSS_CARPET.defaultBlockState(), 2);
                }
            }
        }
        return true;
    }
}
